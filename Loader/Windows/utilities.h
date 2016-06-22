#ifndef _LDRUTIL_

#include <Windows.h>

BOOL IsWow64();

typedef BOOL (WINAPI *LPFN_ISWOW64PROCESS) (HANDLE, PBOOL);

#endif