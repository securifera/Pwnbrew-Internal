#include "utilities.h"

//Determine if the process is 32 or 64 bit
BOOL IsWow64(){
    BOOL bIsWow64 = FALSE;

    //IsWow64Process is not available on all supported versions of Windows.
    //Use GetModuleHandle to get a handle to the DLL that contains the function
    //and GetProcAddress to get a pointer to the function if available.
    
	LPFN_ISWOW64PROCESS fnIsWow64Process = (LPFN_ISWOW64PROCESS) GetProcAddress( GetModuleHandle(TEXT("kernel32")),"IsWow64Process");

    if(NULL != fnIsWow64Process){
        fnIsWow64Process(GetCurrentProcess(),&bIsWow64);
    }
    return bIsWow64;
}