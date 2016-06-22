#ifndef _JARLDR_
#define _JARLDR_

#include <Windows.h>
#include <string>
#include "include/jni.h"

//#define START_CLASS_NAME	"stager/Stager"
//#define IGNORE_SIG          "-Xrs"
//#define JAVADIR				"C:\\Program Files\\Java"
//#define JAVADIR_X64  		"C:\\Program Files (x86)\\Java"
//#define JVM_NAME            "\\jvm.dll"
//#define JVM_CLIENT_PATH		"\\bin\\client"
//#define JVM_SERVER_PATH		"\\bin\\server"
//#define MSVCR_PATH          "\\msvcr10.dll"
//#define JAVA_VERSION_CMD	"java -version"

typedef jint (JNICALL *CreateJavaVM)(JavaVM **pvm, void **penv, void *args);
typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);

BOOL WINAPI InvokeMain( std::string*, std::string );
VOID WINAPI InvokeShutdown();

//BOOL PrepareJava(std::string, std::string &);
//BOOL LoadJava(std::string);
//void GetJavaVersion(std::string &);
BOOL FileExists(std::string aString);

char *GetJvmPath();

#endif