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

//Leaks a memory allocation, must clean up after use
char * decode_split( const char * input, unsigned int str_len ){

	char *test;
	unsigned int i =0, j = 0;
	if( str_len % 2 == 0 ){

		test = (char *)calloc((str_len/2) + 1, 1);
		for( i = 0, j = 0; i < str_len; i += 2, j++ ){
			test[j] = input[i] << 4;
			test[j] += input[i+1];
		}
		
	} else {

		test = (char *)calloc(0, 1);
	}

	return test;
}