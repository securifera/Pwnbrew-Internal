#include "jarldr.h"
#include "..\log.h"
#include "..\utilities.h"

//JVM DLL instance
HINSTANCE hDllInstance;
JavaVM *vm;


int WINAPI WinMain(HINSTANCE hInstance,HINSTANCE hPrevInstance,LPSTR lpCmdLine,int nCmdShow)
{
	std::string jarPath("");
	InvokeMain( nullptr, jarPath);
}


//=========================================================================
/**
	Gets the installed java version
*/
void GetJavaVersion(std::string& sJavaVersion){
	char   psBuffer[128];
	std::string javaVersionArray[5];
	FILE   *pPipe;

	/* Run DIR so that it writes its output to a pipe. Open this
	* pipe with read text attribute so that we can read it 
	* like a text file. 
	*/

	if( (pPipe = _popen( "java -version 2>&1", "rt" )) == NULL )
      exit( 1 );

	/* Read pipe until end of file, or an error occurs. */
	int i = 0;
	while(fgets(psBuffer, 128, pPipe)){
		javaVersionArray[i] = std::string(psBuffer);
		i++;
	}

	/* Close pipe and print return value of pPipe. */
	if (feof(pPipe))
		_pclose( pPipe) ;
    else
		Log( "Error: Failed to read the pipe to the end.\n");

	//Parse out the version
	int firstQuote  = (int)javaVersionArray[0].find_first_of("\"");
	int secondQuote = (int)javaVersionArray[0].find_last_of("\"");

	sJavaVersion = javaVersionArray[0].substr(firstQuote + 1, (secondQuote - firstQuote) - 1);


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
	boolean libraryFound = false;

	GetJavaVersion(sJavaVersion);
	libraryFound =PrepareJava(sJavaVersion, sJavaRoot);
		  
	
	//If the file couldn't be found or loaded
	if( libraryFound == false || hDllInstance == NULL) {
		std::string sFailed = std::string("Failed to Load JVM DLL, Path: ");
		if( !libraryFound ){
			sFailed.append("Library not found.").append("\n");
		} else {
			sFailed.append("LoadLibrary call failed.").append("\n");
		}
		Log((char *)sFailed.c_str());
		
		//Print the last error
		Log("Error code = %d\n", GetLastError());					
		return FALSE;	
	}
		
	//Log("Successfully loaded jvm dll.");

	//resolve the function pointer JNI_CreateJVM
	CreateJavaVM createJVM = (CreateJavaVM)GetProcAddress(hDllInstance, "JNI_CreateJavaVM");
	
	std::string classPath = "-Djava.class.path=";
	classPath.append(adsPath);
	
	options[0].optionString = const_cast<char*>(classPath.c_str()); // application class path 
	options[1].optionString = IGNORE_SIG; // ignore os signals
	
	vm_args.version = JNI_VERSION_1_4; //JNI Version 1.4 and above
	vm_args.options = options;
	vm_args.nOptions = 2;
	vm_args.ignoreUnrecognized = JNI_FALSE;

	//Create the JVM
	res = createJVM(&vm, (void **)&env, &vm_args);
	if (res < 0)  {
		Log( "Error creating JVM\n");
		return FALSE;
	}

	//Find the java class
    cls = env->FindClass(START_CLASS_NAME);
	// invoke the main method
    mid = env->GetStaticMethodID(cls, "main", "([Ljava/lang/String;)V");

    //Set arguments
	std::string arg1;
	if(serviceName != nullptr)
		arg1.assign(*serviceName);

	jstr = env->NewStringUTF(arg1.c_str());
    if (jstr == NULL) {
		Log( "Unable to create string\n");
        return FALSE;
    }

    stringClass = env->FindClass("java/lang/String");
    args = env->NewObjectArray(1, stringClass, env->NewStringUTF(""));
	env->SetObjectArrayElement(args,0,jstr);

    env->CallStaticVoidMethod(cls, mid, args);
	
	//if there is any exception log
	if(env->ExceptionCheck()) {
		Log( "Exception occured..\n");
		env->ExceptionDescribe();
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


//=========================================================================
/**
	Attempts to find the java install
*/
BOOL PrepareJava(std::string pJavaVersion, std::string& sJavaPath ){
    
	WIN32_FIND_DATA fdata;
    HANDLE found;
	std::string fileName;
	std::string javaVersionInt;
	std::string genericRuntime;
	std::string genericRuntime2;
	boolean foundJava = false;
	
	int firstPeriod  = (int)pJavaVersion.find_first_of(".");
		
	//Get the java version
	if( firstPeriod != std::string::npos ) {	
		javaVersionInt.assign(pJavaVersion.substr(firstPeriod + 1, 1)); 
		genericRuntime.append("jre").append(javaVersionInt);
	} else {
		genericRuntime.append("jre");
	}

	//Set to runtime 7
	genericRuntime2.append("jre7");	

	std::string javaPath;
    //Set to x86 dir if 64bit since we are x86
	if( IsWow64() && FileExists(JAVADIR_X64)){
		javaPath = JAVADIR_X64;
	}
	
	//Check if default path exists
	if(!foundJava){
	
		//Find Image Files
		if(SetCurrentDirectory(javaPath.c_str())){

			found = FindFirstFile("*",&fdata);
			if (found != INVALID_HANDLE_VALUE)
			{
				do
				{
					if (fdata.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY){
						if(strstr(fdata.cFileName,pJavaVersion.c_str()) != NULL){
							fileName = std::string(fdata.cFileName);
						
							//If the path is a jdk instead of a jre then append the correct path
							if(javaPath.find("jdk") == std::string::npos){
							   javaPath.append("\\").append(fdata.cFileName).append("\\jre");
							} else {
							   javaPath.append("\\").append(fdata.cFileName);
							}

							sJavaPath.assign(javaPath);
						
							//Ensure file exists
							foundJava=true;							
							break;

						} else if(strcmp(genericRuntime.c_str(), fdata.cFileName) == 0){
						   
							javaPath.append("\\").append(fdata.cFileName);
							sJavaPath.assign(javaPath);
							
							foundJava=true;
							break;

						} else if(strcmp(genericRuntime2.c_str(), fdata.cFileName) == 0){
						   
							javaPath.append("\\").append(fdata.cFileName);
							sJavaPath.assign(javaPath);
							
							foundJava=true;
							break;

						}
					}
					else
						continue;
				}
				while (FindNextFile(found, &fdata));

				FindClose(found);
			}
		}
		Log((char *)javaPath.c_str());
		sJavaPath = javaPath;
		
		//If we found a java path then load it
		if(foundJava){
		   foundJava = LoadJava(sJavaPath);	 
		}
    }

	return foundJava;
}

//=========================================================================
/**
	Loads java
*/
BOOL LoadJava(std::string sJavaPath){
    
	std::string sJavaDll;
	std::string sLibPath;

	//Load the JVM Dll
	sJavaDll.assign(sJavaPath).append(JVM_CLIENT_PATH).append(JVM_NAME);
		
	//Check if client path exists
	if( !FileExists(sJavaDll)){ 
		
		//Close and try the server path
		sJavaDll.assign(sJavaPath).append(JVM_SERVER_PATH).append(JVM_NAME);
		if( !FileExists(sJavaDll)){ 			   
			//Write error log
			std::string sFailed = std::string("Failed to find jvm dll, Path: ");
			sFailed.append(sJavaDll).append("\n");
			Log((char *)sFailed.c_str());
			return FALSE;
		}
	}

    //Set the dir
	SetCurrentDirectory(sJavaDll.c_str());
		
	//Close the file handle
	hDllInstance = LoadLibrary(sJavaDll.c_str());

	//If didn't load, try to load msvcr100 first
	if(hDllInstance == NULL){
			
		//Check if default path exists
		sLibPath.assign(LOG_PATH).append(MSVCR_PATH);
	    if( FileExists(sLibPath)){ 

			hDllInstance = LoadLibrary(sLibPath.c_str());
			
			//Try and load the jvm dll again
			if(hDllInstance != NULL){
			    hDllInstance = LoadLibrary(sJavaDll.c_str());
			} else {

			   	//Write error log
				std::string sFailed = std::string("Failed to load C++ Runtime, Path: ");
				sFailed.append(sJavaPath).append("\n");
				Log((char *)sFailed.c_str());
				return FALSE;
			}
		} 
	}
			
	return (hDllInstance != NULL);

}

//Returns whether a file exists
BOOL FileExists(std::string thePath){
	return GetFileAttributes(thePath.c_str()) != INVALID_FILE_ATTRIBUTES;
}
