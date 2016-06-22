#ifndef _LDRLOG_

#include <stdio.h>
#include <time.h>
#include <direct.h>
#include <stdarg.h>

#define LOG_FILE			"C:\\Windows\\System32\\jsvc.log"
#define LOG_PATH			"C:\\Windows\\System32"
	
void Log(char* format, ...);

#endif