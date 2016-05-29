#include "log.h"

//=========================================================================
/**
	Log the string
*/
void Log(char* format, ...) {

	char dateStr[9],timeStr[9];
	errno_t err;

	_strdate_s(dateStr, sizeof(dateStr));
    _strtime_s( timeStr, sizeof(timeStr));

	// write error or other information into log file
	FILE* pFile = 0;
	va_list ap;
	if( (err = fopen_s(&pFile, LOG_FILE,"a")) != 0){
		//Directory doesn't exist, create it
		if( err == 2){
		   _mkdir(LOG_PATH);
		   fopen_s(&pFile, LOG_FILE,"a");
		}

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