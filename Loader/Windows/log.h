#ifndef _LDRLOG_
#define _LDRLOG_


#ifdef _DBG

#include <stdio.h>
#include <time.h>
#include <direct.h>
#include <stdarg.h>

#define LOG_FILE			"C:\\jldr.log"
#define LOG_PATH			"C:\\"

	
void Log(char* format, ...);

#endif

#endif