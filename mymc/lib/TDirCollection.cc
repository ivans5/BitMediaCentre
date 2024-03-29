/*
 * TDirCollection.cc
 *
 * Turbo Vision - Version 2.0
 *
 * Copyright (c) 1994 by Borland International
 * All Rights Reserved.
 *
 * Modified by Sergio Sigala <sergio@sigala.it>
 */

#define Uses_TDirCollection
#define Uses_TDirEntry
#define Uses_opstream
#define Uses_ipstream
#include <tvision/tv.h>

#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

//XXX for getenv():
#include <stdlib.h>

Boolean isDir( const char *str )
{
	/* SS: all changed */
	struct stat s;

	if (stat(str, &s) == 0 && S_ISDIR(s.st_mode)) return True;
	return False;
}

Boolean pathValid( const char *path )
{
	/* SS: all changed */
	char dir[PATH_MAX];
	char name[PATH_MAX];

	expandPath(path, dir, name);
	if (strcmp(dir, "/") == 0) strcat(dir, ".");
	else strcat(dir, "/.");
	return isDir(dir);
}

Boolean validFileName( const char *fileName )
{
	/* SS: all changed */
	FILE *f;

	/*
	 * Patch from: Vasily Egoshin <wasa@nica.marstu.mari.su>
	 * Date: Thu, 9 Jan 1997 16:36:10 +0300 (MSK)
	 */
	if ((f = fopen(fileName, "r")) != NULL)
	{
		/* the file exists and is readable ===> file name ok */

		fclose(f);
		return True;
	}
	if ((f = fopen(fileName, "w")) != NULL)
	{
		/* file don't exists but it is writable ===> file name ok */

		fclose(f);
		remove(fileName);
		return True;
	}
	return False;	/* illegal file name */
}

void getCurDir( char *dir )
{
	/* SS: all changed */
        //XXX - https://stackoverflow.com/questions/1542803/is-there-a-version-of-os-getcwd-that-doesnt-dereference-symlinks

	//getcwd(dir, PATH_MAX);
        char * pwdvalue = getenv("WORKING_DIRECTORY");

        if ( pwdvalue != NULL ) {
          strcpy(dir, pwdvalue);
        } else {
          strcpy(dir, "/homepcuser"); //default value
        }

	if (strcmp(dir, "/") != 0) strcat(dir, "/");
}

Boolean isWild( const char *f )
{
    return Boolean( strpbrk( f, "?*" ) != 0 );
}


TStreamable *TDirCollection::build()
{
    return new TDirCollection( streamableInit );
}

void TDirCollection::writeItem( void *obj, opstream& os )
{
    TDirEntry *item = (TDirEntry *)obj;
    os.writeString( item->text() );
    os.writeString( item->dir() );
}

void *TDirCollection::readItem( ipstream& is )
{
    char *txt = is.readString();
    char *dir = is.readString();
    TDirEntry *entry = new TDirEntry( txt, dir );
    delete txt;
    delete dir;
    return entry;
}
