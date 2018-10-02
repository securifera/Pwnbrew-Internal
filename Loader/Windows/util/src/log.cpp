
#ifdef _DBG

#include "log.h"

std::string log_path;

//=========================================================================
/**
	Log the string
*/
void SetLogPath(std::string log_path_param ) {
	log_path.assign(log_path_param);
}

//=========================================================================
/**
	Log the string
*/
void Log(char* format, ...) {

	char dateStr[9],timeStr[9];
	errno_t err;

	//Check if log path is set
	if( log_path.empty() )
		return;

	_strdate_s(dateStr, sizeof(dateStr));
    _strtime_s( timeStr, sizeof(timeStr));

	// write error or other information into log file
	FILE* pFile = 0;
	va_list ap;
	if( (err = fopen_s(&pFile, log_path.c_str(),"a")) != 0){
		return;
	}

	if(pFile != 0) {
		va_start (ap, format);
		fprintf(pFile, "[%s]",timeStr);
		fprintf(pFile, "[%s] ",dateStr);
	    vfprintf (pFile, format, ap);
		va_end (ap);
		fclose(pFile);
	} 
	
}
#endif