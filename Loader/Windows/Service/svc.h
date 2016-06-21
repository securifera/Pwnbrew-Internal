#ifndef _xsvc_

    #include <windows.h>
	#include <string>
    //#include <wchar.h>
    #include "resource.h"
    #include "ntundoc.h"
	#include <stdio.h>
	#include <winsvc.h>
	#include <fstream>
	#include <vector>
     
	#define SHUTDOWN_TIMEOUT    10000 //10 seconds
	#define ADDRESS_HOLDER -1

    bool InstallService( const char*,  char* );
	void UnInstall();

	void ExtractStager( char* );
	void ReadServiceName( std::string* );
	void ReadJavaPath( char* );
	
	DWORD WINAPI InvokeMainWrapper(LPVOID lpParam);
	DWORD WINAPI InvokeShutdownWrapper( LPVOID lpParam );

	void WINAPI ServiceMain(DWORD dwArgc, LPTSTR *lpszArgv);
	void WINAPI ServiceHandler(DWORD fdwControl);
	BOOL SetTheServiceStatus(DWORD dwStatus);
	void UpdateServiceName( char* passedName, std::string passedJava );
	void UpdateStringTableBuffer( wchar_t** buffer, UINT rsrcId, char * newString, DWORD *size );
			

	// NT_QUERY_INFORMATION for pure 32 and 64-bit processes
	typedef NTSTATUS (NTAPI *_NtQueryInformationProcess)(
		IN HANDLE ProcessHandle,
		ULONG ProcessInformationClass,
		OUT PVOID ProcessInformation,
		IN ULONG ProcessInformationLength,
		OUT PULONG ReturnLength OPTIONAL
	);

	// PROCESS_BASIC_INFORMATION for pure 32 and 64-bit processes
	typedef struct _PROCESS_BASIC_INFORMATION {
		PVOID Reserved1;
		PVOID PebBaseAddress;
		PVOID Reserved2[2];
		ULONG_PTR UniqueProcessId;
		PVOID Reserved3;
	} PROCESS_BASIC_INFORMATION;
	

	//Handles
	SERVICE_STATUS_HANDLE   hServiceStatusHandle; 
	SERVICE_STATUS          ServiceStatus;

#endif