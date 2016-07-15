#ifndef _LDRLOG_
#define _LDRLOG_


#ifdef _DBG

#include <stdio.h>
#include <time.h>
#include <direct.h>
#include <stdarg.h>
#include <string>

void SetLogPath(std::string log_path_param ) ;
void Log(char* format, ...);

#endif

#endif