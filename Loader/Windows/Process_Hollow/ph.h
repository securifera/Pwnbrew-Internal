#ifndef _PH
#define _PH

#include <stdio.h>
#include <Windows.h>
#include <DbgHelp.h>


void LoadWatchDog();
void LoadPayload();

#define BUFFER_SIZE 0x2000

#if !defined NTSTATUS
typedef LONG NTSTATUS;
#endif

#define STATUS_SUCCESS 0

#if !defined PROCESSINFOCLASS
typedef LONG PROCESSINFOCLASS;
#endif

#if !defined PPEB
typedef struct _PEB *PPEB;
#endif

#if !defined PROCESS_BASIC_INFORMATION
typedef struct _PROCESS_BASIC_INFORMATION {
    PVOID Reserved1;
    PPEB PebBaseAddress;
    PVOID Reserved2[2];
    ULONG_PTR UniqueProcessId;
    PVOID Reserved3;
} PROCESS_BASIC_INFORMATION;
#endif;

typedef struct _PEB_LDR_DATA
{
    ULONG         Length;                            /* Size of structure, used by ntdll.dll as structure version ID */
    BOOLEAN       Initialized;                       /* If set, loader data section for current process is initialized */
    PVOID         SsHandle;
    LIST_ENTRY    InLoadOrderModuleList;             /* Pointer to LDR_DATA_TABLE_ENTRY structure. Previous and next module in load order */
    LIST_ENTRY    InMemoryOrderModuleList;           /* Pointer to LDR_DATA_TABLE_ENTRY structure. Previous and next module in memory placement order */
    LIST_ENTRY    InInitializationOrderModuleList;   /* Pointer to LDR_DATA_TABLE_ENTRY structure. Previous and next module in initialization order */
} PEB_LDR_DATA,*PPEB_LDR_DATA;

typedef struct _PEB_PARTIAL {
	BYTE InheritedAddressSpace;
    BYTE ReadImageFileExecOptions;
    BYTE BeingDebugged;
    BYTE SpareBool;
    void* Mutant;
    void* ImageBaseAddress;
    PEB_LDR_DATA* Ldr;
	void* ProcessParameters;
    void* SubSystemData;
    void* ProcessHeap;
} PEB_PARTIAL, *PPEB_PARTIAL;


typedef LONG NTSTATUS, *PNTSTATUS;
typedef struct _UNICODE_STRING {
  USHORT Length;
  USHORT MaximumLength;
  PWSTR  Buffer;
} UNICODE_STRING, *PUNICODE_STRING;

typedef struct _OBJECT_ATTRIBUTES {
  ULONG Length;
  HANDLE RootDirectory;
  PUNICODE_STRING ObjectName;
  ULONG Attributes;
  PVOID SecurityDescriptor;
  PVOID SecurityQualityOfService;
} OBJECT_ATTRIBUTES, *POBJECT_ATTRIBUTES;

typedef NTSTATUS (WINAPI * PFN_ZWQUERYINFORMATIONPROCESS)(HANDLE, PROCESSINFOCLASS,
    PVOID, ULONG, PULONG);

NTSTATUS (__stdcall *ZwQueryInformationProcess)(
  HANDLE  ProcessHandle,
  PROCESSINFOCLASS  ProcessInformationClass,
  PVOID  ProcessInformation,
  ULONG  ProcessInformationLength,
  PULONG  ReturnLength  OPTIONAL
  );

NTSTATUS (__stdcall *ZwCreateSection)(
     PHANDLE  SectionHandle,
     ACCESS_MASK  DesiredAccess,
     PDWORD  ObjectAttributes OPTIONAL,
     PLARGE_INTEGER  MaximumSize OPTIONAL,
     ULONG  SectionPageProtection,
     ULONG  AllocationAttributes,
     HANDLE  FileHandle OPTIONAL
    );

NTSTATUS (__stdcall *ZwMapViewOfSection) (
HANDLE SectionHandle,
HANDLE ProcessHandle,
OUT PVOID *BaseAddress,
ULONG_PTR ZeroBits,
SIZE_T CommitSize,
PLARGE_INTEGER SectionOffset,
PSIZE_T ViewSize,
DWORD InheritDisposition,
ULONG AllocationType,
ULONG Win32Protect
);

NTSTATUS (__stdcall *ZwUnmapViewOfSection)(
	HANDLE ProcessHandle,
	PVOID BaseAddress
	);


PLOADED_IMAGE ReadRemoteImage(HANDLE hProcess, LPCVOID lpImageBaseAddress)
{
	BYTE* lpBuffer = new BYTE[BUFFER_SIZE];
	BOOL bSuccess = ReadProcessMemory( hProcess, lpImageBaseAddress, lpBuffer, BUFFER_SIZE, 0 );

	if (!bSuccess)
		return 0;	
	
	PIMAGE_DOS_HEADER pDOSHeader = (PIMAGE_DOS_HEADER)lpBuffer;

	PLOADED_IMAGE pImage = new LOADED_IMAGE();

	#ifdef _M_IX86
		pImage->FileHeader = (PIMAGE_NT_HEADERS32)(lpBuffer + pDOSHeader->e_lfanew);
	#endif 

	#ifdef _M_X64
		pImage->FileHeader = (PIMAGE_NT_HEADERS64)(lpBuffer + pDOSHeader->e_lfanew);
	#endif 

	pImage->NumberOfSections = pImage->FileHeader->FileHeader.NumberOfSections;

	#ifdef _M_IX86
		int size = sizeof(IMAGE_NT_HEADERS32);
		pImage->Sections = (PIMAGE_SECTION_HEADER)(lpBuffer + pDOSHeader->e_lfanew + sizeof(IMAGE_NT_HEADERS32));
	#endif 

	#ifdef _M_X64
		int size = sizeof(IMAGE_FILE_HEADER) + pImage->FileHeader->FileHeader.SizeOfOptionalHeader + 4;
		pImage->Sections = (PIMAGE_SECTION_HEADER)(lpBuffer + pDOSHeader->e_lfanew + size);
	#endif 

	return pImage;
}

PLOADED_IMAGE GetLoadedImage(BYTE *dwImageBase)
{
	PIMAGE_DOS_HEADER pDosHeader = (PIMAGE_DOS_HEADER)dwImageBase;
	PLOADED_IMAGE pImage = new LOADED_IMAGE();
	
	#ifdef _M_IX86
		pImage->FileHeader = (PIMAGE_NT_HEADERS32)(dwImageBase + pDosHeader->e_lfanew);
	#endif 

	#ifdef _M_X64
		pImage->FileHeader = (PIMAGE_NT_HEADERS64)(dwImageBase + pDosHeader->e_lfanew);
	#endif 

	pImage->NumberOfSections = pImage->FileHeader->FileHeader.NumberOfSections;

	#ifdef _M_IX86
		pImage->Sections = (PIMAGE_SECTION_HEADER)(dwImageBase + pDosHeader->e_lfanew + sizeof(IMAGE_NT_HEADERS32));
	#endif 

	#ifdef _M_X64
		int size = sizeof(IMAGE_FILE_HEADER) + pImage->FileHeader->FileHeader.SizeOfOptionalHeader + 4;
		pImage->Sections = (PIMAGE_SECTION_HEADER)(dwImageBase + pDosHeader->e_lfanew + size);
	#endif 

	return pImage;
}

typedef struct BASE_RELOCATION_BLOCK {
	DWORD PageAddress;
	DWORD BlockSize;
} BASE_RELOCATION_BLOCK, *PBASE_RELOCATION_BLOCK;

typedef struct BASE_RELOCATION_ENTRY {
	USHORT Offset : 12;
	USHORT Type : 4;
} BASE_RELOCATION_ENTRY, *PBASE_RELOCATION_ENTRY;

#define CountRelocationEntries(dwBlockSize)		\
	(dwBlockSize -								\
	sizeof(BASE_RELOCATION_BLOCK)) /			\
	sizeof(BASE_RELOCATION_ENTRY)


#endif