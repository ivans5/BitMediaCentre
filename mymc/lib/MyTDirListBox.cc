/*
 * MyTDirListBox.cc
 *
 */

#define Uses_MyTDirListBox
//XXX -  - need to #define Uses_TDirListBox so that stddlg.h gets included from tv.h:
#define Uses_TDirListBox
#define Uses_TEvent
#define Uses_TDirCollection
#define Uses_TChDirDialog
#define Uses_TMyChDirDialog
#define Uses_TDirEntry
#define Uses_TButton
#include <tvision/tv.h>

#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <stdio.h>
#include <string.h>

//XXX
#include <list>
#include <dirent.h>
#include <cstdio>
#include <sys/stat.h>
#include <cstdlib>

//For getcwd():
#include <unistd.h>
#include <limits.h>


MyTDirListBox::MyTDirListBox( const TRect& bounds, TScrollBar *aScrollBar ) :
    TListBox( bounds, 1, aScrollBar ),
    cur( 0 )
{
    *dir = EOS;
}

MyTDirListBox::~MyTDirListBox()
{
   if ( list() )
      destroy( list() );
}

void MyTDirListBox::getText( char *text, short item, short maxChars )
{
    strncpy( text, list()->at(item)->text(), maxChars );
    text[maxChars] = '\0';
}

void MyTDirListBox::selectItem( short item )
{
    message( owner, evCommand, cmChangeDir, list()->at(item) );
}

/*
void MyTDirListBox::handleEvent( TEvent& event )
{
    if( event.what == evMouseDown && (event.mouse.eventFlags & meDoubleClick) )
        {
        event.what = evCommand;
        event.message.command = cmChangeDir;
        putEvent( event );
        clearEvent( event );
        }
    else
       TListBox::handleEvent( event );
}
*/

Boolean MyTDirListBox::isSelected( short item )
{
    return Boolean( item == cur );
}

void MyTDirListBox::showDrives( TDirCollection* )
{
	/* SS: do nothing */
}

//#include <stdlib.h>     /* system, NULL, EXIT_FAILURE */ //XXX
int endsWithFoo( char *string )
{
  string = strrchr(string, '.');

  if( string != NULL )  {
    //return( strcmp(string, ".mkv") );
    //TODO: load this from the value of the "Extensions" TInputLine --> XXX makes navigation by arrow keys more difficult..
    if(strcmp(string, ".mkv") == 0 || strcmp(string, ".mp4") == 0 || strcmp(string, ".avi") == 0)
      return 0;
  }

  return( -1 );
}

bool mycompare (const dirent *first, const dirent *second)
{
struct stat s1,s2;
stat(first->d_name, &s1);
stat(second->d_name, &s2);
if (s1.st_mtime > s2.st_mtime) return true;
return false;
}
int aria2_file_exist (char *path)
{
	  char thepath[255];
	  struct stat   buffer;   
	  strcpy(thepath, path);
          strcat(thepath, ".aria2");
	  //printf("thepath is %s\n",thepath);
          return (stat (thepath, &buffer) == 0);
}
//XXX
std::list<dirent *> getListOfDirents(char *dir_)
{
	DIR *dp;
	char path[PATH_MAX];
	dirent *de;
	struct stat s;
        //XXX Make list of dirents:
        std::list<dirent *> listOfDirents;
        DIR *dp2;
        dirent *de2;

	sprintf(path, "%s.", dir_);
	if ((dp = opendir(path)) != NULL)
	{
		while ((de = readdir(dp)) != NULL)  
                {
                        dirent *de_heap = (dirent *) malloc(sizeof(dirent));
                        memcpy(de_heap, de, sizeof(dirent));
			sprintf(path, "%s%s", dir_, de->d_name);

                        //XXX - is it a mkv/mp4/avi file:
			if (stat(path, &s) == 0 && !S_ISDIR(s.st_mode) &&
                            endsWithFoo(de->d_name)==0 && 
			    !aria2_file_exist(path))  {
                          listOfDirents.push_back(de_heap);
                        }
                        //XXX - is it a directory?
			if (stat(path, &s) == 0 && S_ISDIR(s.st_mode))  {
                          if ((dp2 = opendir(path)) != NULL) //for the last item in the subtree:
                          {
                                  while ((de2 = readdir(dp2)) != NULL)
                                  {
                                    if(endsWithFoo(de2->d_name)==0 &&
                 			    !aria2_file_exist(path))  {
                                      listOfDirents.push_back(de_heap);
                                      break;
                                    }
                                  }
                          }
                          closedir(dp2);
                        }
                }
        }
        closedir(dp);
        //XXX -sort by date
        listOfDirents.sort(mycompare);
        return listOfDirents;
}

//XXX
//TODO: figure out if this can be done thru some automatic garbage collection?
void freeMemory(std::list<dirent *> listOfDirents)
{
	dirent *de;
        std::list<dirent *>::iterator it = listOfDirents.begin();
        while(it != listOfDirents.end())
		{
                        de = *it;
                        free(de);
                        it++; 
                }
}


static int compare_fun (const void *p, const void *q)
{
    char *l= (char *)p;
    char *r= (char *)q;
    int cmp;

    cmp= strcmp (l, r);
    return cmp;
}

void MyTDirListBox::showDirs( TDirCollection *dirs )
{
//XXX - why did i need to copy this? ,why did i need to change this?
//char *                   pathDir   = "\x80\x84\x82";
char *pathDir = "\xC0\xC4\xC2";
//char *                        firstDir  =   "\x90\x82\x84";
//char *firstDir = "\xC0\xC2\xC4";
char *firstDir = "\xC3\xC4\xC4";
//char *                       middleDir =   " \x83\x84";
//char *middleDir = " \xC3\xC4";    //XXX
char *middleDir = "\xC3\xC4\xC4";    //XXX
char *middleDir2= "\xC3\xC4\xC2"; //XXX
char *middleDir3                ="\xB3 \xF5\xC4";
char *middleDir3_last           ="\xB3 \xF6\xC4";
//char *                      lastDir   =   " \x80\x84";
char *lastDir = " \xC0\xC4";
//char *                     graphics = "\x80\x83\x84";
char *graphics = "\xC0\xC3\xC4";
	/* SS: changed */

	char buf[PATH_MAX * 2];
	char *curDir = dir; //XXX: 'dir' is a member variable
	char *end;
	char *name = buf + sizeof(buf) / 2;
	const int indentSize = 2;
	int indent = 0, len;

	/* extract directories from path string */

	memset(buf, ' ', sizeof(buf));
	strcpy(name, pathDir);
	len = strlen(pathDir);
	while((end = strchr(curDir, '/' )) != NULL)
	{
		/* special case: root directory */

		if (end == dir) dirs->insert(new TDirEntry("/", ""));
		else
		{
			memcpy(name + len, curDir, end - curDir);
			name[len + end - curDir] = EOS;
			*end = EOS;
			dirs->insert(new TDirEntry(name - indent, dir));
			*end = '/';
			indent += indentSize;
		}
		curDir = end + 1;
	}
	cur = dirs->getCount() - 1;

	/* read subdirectories in the current directory */

	Boolean isFirst = True;
	DIR *dp;
	char path[PATH_MAX];
	dirent *de;
	struct stat s;

        //XXX Make list of dirents:
        std::list<dirent *> listOfDirents;
        Boolean isFirst2 = True, isLast=False; 
        DIR *dp2;
        dirent *de2;
        char path2[PATH_MAX];

        listOfDirents = getListOfDirents(dir);

        /* XXX for sorting the contents of subdirectories alphabetticaly (readdir doesnt return in any order...) */
        char idata[250][250]; //for simplicity in this example
        unsigned n=0,j;

        std::list<dirent *>::iterator it = listOfDirents.begin();
        while(it != listOfDirents.end())
		{
                        de = *it;
                        it++; 

                        if (it == listOfDirents.end()) { 
                          isLast=True; 
                        }

			/* we don't want these directories */

			if (strcmp(de->d_name, ".") == 0 ||
				strcmp(de->d_name, "..") == 0) continue;

			/* is it NOT a directory ? */

			sprintf(path, "%s%s", dir, de->d_name);

                        //XXX - is it a mkv/mp4 file:
			if (stat(path, &s) == 0 && !S_ISDIR(s.st_mode) &&
                            endsWithFoo(de->d_name)==0)
			{
				if (isFirst)
				{
					isFirst = False;
					strcpy(name, firstDir);
					len = strlen(firstDir);
				}
				else
				{
					strcpy(name, middleDir);
					len = strlen(middleDir);
				}
				strcpy(name + len, de->d_name);
                                //strcpy(name + len, "helloworld");

				dirs->insert(new TDirEntry(name - indent,
					path));
			}

                        //XXX - is it a directory?
			if (stat(path, &s) == 0 && S_ISDIR(s.st_mode))  {
                          int count=0,i=0;
                          if ((dp2 = opendir(path)) != NULL) //for the last item in the subtree:
                          {
                                  while ((de2 = readdir(dp2)) != NULL)
                                  {
                                    if(endsWithFoo(de2->d_name)==0)  
                                      count++;
                                  }
                          }
                          closedir(dp2);
                          if (count > 0 && (dp2 = opendir(path)) != NULL)
                          {
                                  isFirst2= True;
                                  //XXX sort the elements of de2 alphabettically:
                                  n=0;

                                  while (((de2 = readdir(dp2)) != NULL)) { // &&
                                    if (endsWithFoo(de2->d_name)==0)  {
	                                 //TODO: Reinstante: && (n < sizeof idata / sizeof idata[0])) {....

                                          strncpy(idata[n++],de2->d_name, 250);    
                                    }
                                  }

                                  if (n>1)  {
                                    	qsort (idata, n, sizeof idata[0], compare_fun); 
                                  }


//Resume normal code path:
                                  for (j=0;j<n;j++)  {
                                      char * de_d_name = idata[j];
                                  
                                      //DELETEME: sprintf(path2,"%s/%s",path,de2->d_name);
                                      sprintf(path2,"%s/%s",path,de_d_name);
                                      if (isFirst2)  {
                                        isFirst2 = False;
                                        //XXX    - if first time, insert:   |-+- directory name
					strcpy(name, middleDir2);
                                        if (isLast) {
                                          name[0]='\xF6';
                                        }
					len = strlen(middleDir2);
                                        strcpy(name + len, de->d_name);
                			dirs->insert(new TDirEntry(name - indent,
				   	   path2));
                                      }
                                      //XXX    - insert: | |_ file name
                                      i++;
                                      char *selection;
                                      if(i==count) { selection = middleDir3_last; }
                                      else { selection = middleDir3; }
			              strcpy(name, selection);
                                      if (isLast) {
                                        name[0]=' ';
                                      }
				      len = strlen(selection);
                                      //DELETEME:strcpy(name + len, de2->d_name);
                                      strcpy(name + len, de_d_name);
                	              dirs->insert(new TDirEntry(name - indent,
				        path2));
                                    
                                  } 
                	  	closedir(dp2);
                	  } //else { printf ("couldnt open dir\n"); }
                       }
                  } //while ((de = readdir(dp)) != NULL)
     //} //if ((dp = opendir(path)) != NULL)

    freeMemory(listOfDirents); //XXX

	/* old code */

    char *p = dirs->at(dirs->getCount()-1)->text();
    char *i = strchr( p, graphics[0] );
    if( i == 0 )
        {
        i = strchr( p, graphics[1] );
        if( i != 0 )
            *i = graphics[0];
        }
    else
        {
        *(i+1) = graphics[2];
        *(i+2) = graphics[2];
        }
}


//XXX - new file detection only covers files in the home folder and subfolders being added/removed...
//std::list<dirent *> oldListOfDirents;
time_t old_st_mtime = 0;
void MyTDirListBox::doUpdate() 
{
    //std::list<dirent *> newListOfDirents = getListOfDirents(dir);

    struct stat s1;
    char cwd[PATH_MAX];
    strcpy(cwd, dir);
    cwd[strlen(cwd)-1]='\0';  //there was an extra trailing slash causing problem

    char tmp[255];
    sprintf(tmp,"echo cwd is %s >> /tmp/bleh",cwd);
    //system(tmp);

    // /homepcuser is a symlink:
    char dereffed[1024];
    int c = readlink(cwd, dereffed, 1024);  
    if (c <= 0)  {
        system("play -n -c1 synth 0.6 sine 600 >/dev/null 2>/dev/null");//XXX
        return;
    }
    dereffed[c] = '\0';

    sprintf(tmp,"echo dereffed is %s >> /tmp/bleh",dereffed);
    //system(tmp);

    stat(dereffed, &s1);
  
    //if (newListOfDirents != oldListOfDirents)  //<-- TODO??
    if (old_st_mtime != 0 && s1.st_mtime != old_st_mtime)
    {
        //system("play -n -c1 synth 0.6 sine 500 >/dev/null 2>/dev/null");//XXX
    	TDirCollection *dirs = new TDirCollection( 5, 5 );
    	showDirs( dirs ); //TODO: release memory from old TDirCollection?
    	newList( dirs );
    	focusItem( cur );
    }
    //freeMemory(oldListOfDirents); //XXX-TODO reenable...
    //oldListOfDirents = newListOfDirents;
    old_st_mtime = s1.st_mtime;
}

void MyTDirListBox::newDirectory( const char *str )
{
	/* SS: changed */

	strcpy( dir, str );
	TDirCollection *dirs = new TDirCollection( 5, 5 );
	showDirs( dirs );
	newList( dirs );
	focusItem( cur );
}

void MyTDirListBox::setState( ushort nState, Boolean enable )
{
    TListBox::setState( nState, enable );
    if( (nState & sfFocused) != 0 )
#ifndef __UNPATCHED
        message(owner, evCommand, cmDirSelection, (void *)enable);  //!!
#else
        ((TChDirDialog *)owner)->chDirButton->makeDefault( enable );
#endif
}

#if !defined(NO_STREAMABLE)

TStreamable *MyTDirListBox::build()
{
    return new MyTDirListBox( streamableInit );
}

#endif
