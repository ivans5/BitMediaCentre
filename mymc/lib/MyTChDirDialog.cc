/*
 * MyTChDirDialog.cc
 *
 * Turbo Vision - Version 2.0
 *
 * Copyright (c) 1994 by Borland International
 * All Rights Reserved.
 *
 * Modified by Sergio Sigala <sergio@sigala.it>
 */

#define Uses_MsgBox
#define Uses_MyTChDirDialog
#define Uses_TChDirDialog
#define Uses_TRect
#define Uses_TInputLine
#define Uses_TLabel
#define Uses_THistory
#define Uses_TScrollBar
#define Uses_TDirListBox
#define Uses_TButton
#define Uses_TEvent
#define Uses_TDirEntry
#define Uses_TDirCollection
#define Uses_MyTChDirDialog
#define Uses_TStaticText
#define Uses_opstream
#define Uses_ipstream
#include <tvision/tv.h>

#include <ctype.h>
#include <limits.h>
#include <string.h>
#include <unistd.h>

//XXX- for system():
#include <stdlib.h>
//XXX
class TDeskTop;

MyTChDirDialog::MyTChDirDialog( ushort opts, ushort histId ) :
//    TDialog( TRect( 16, 2, 64, 20 ), changeDirTitle ),
//    TDialog( TRect( 0,0,64,20 ), "Play video"),
    TDialog( TRect( 0,0,64,18 ), "Play video"),
    TWindowInit( &MyTChDirDialog::initFrame )
{
    options |= ofCentered;

    dirInput = new TInputLine( TRect( 3, 3, 30, 4 ), 68 );
    //XXX - Disabled these 2 to make navigation a bit easier...
    //insert( dirInput );
    //insert( new TLabel( TRect( 2+1, 2, 17+1, 3 ), "Extensions", dirInput ));
//    insert( new THistory( TRect( 30, 3, 33, 4 ), dirInput, histId ) );
    insert( new TStaticText( TRect( 2+2, 2, 17+2, 3 ), "Extensions:"));
    insert( new TStaticText( TRect( 2+2, 3, 17+2, 4 ), "mkv,mp4,avi"));

    TScrollBar *sb = new TScrollBar( TRect( 32+16, 6, 33+16, 16 ) );
    insert( sb );
    //dirList = new MyTDirListBox( TRect( 3, 6, 32, 16 ), sb );
    dirList = new MyTDirListBox( TRect( 3, 6, 32+16, 16 ), sb );
    insert( dirList );
    insert( new TLabel( TRect( 2+1, 5, 17+1, 6 ), dirTreeText, dirList ) );

    //XXX
    //okButton = new TButton( TRect( 35, 6, 45, 8 ), okText, cmOK, bfDefault );
    //insert( okButton );
    chDirButton = new TButton( TRect( 35+16, 9, 45+16, 11 ), "Play", cmChangeDir, bfNormal );
    insert( chDirButton );
    //insert( new TButton( TRect( 35, 12, 45, 14 ), revertText, cmRevert, bfNormal ) ); //XXX
    if( (opts & cdHelpButton) != 0 )
        insert( new TButton( TRect( 35, 15, 45, 17 ), helpText, cmHelp, bfNormal ) );
    if( (opts & cdNoLoadDir) == 0 )
        setUpDialog();
    selectNext( False );
}

ushort MyTChDirDialog::dataSize()
{
    return 0;
}

void MyTChDirDialog::shutDown()
{
    dirList = 0;
    dirInput = 0;
    okButton = 0;
    chDirButton = 0;
    TDialog::shutDown();
}

void MyTChDirDialog::getData( void * )
{
}


void MyTChDirDialog::handleEvent( TEvent& event )
{
    TDialog::handleEvent( event );
    switch( event.what )
        {
        case evCommand:
            {
            char curDir[PATH_MAX];
            char command[PATH_MAX]; //XXX
            switch( event.message.command )
                {
                case cmRevert:
                    //getCurDir( curDir ); //XXX
                    break;
                case cmChangeDir:
                    {
                    TDirEntry *p = dirList->list()->at( dirList->focused );
                    strcpy( curDir, p->dir() );
			/* SS: changed */
//                        if( curDir[strlen(curDir)-1] != '/' )  //XXX
//                            strcat( curDir, "/" );
//printf("curDir=%s\n",curDir);
//sprintf(command,"/bin/bash -c 'set -a; . /env.sh;/usr/bin/nice -20 /usr/bin/mpv --ontop \"%s\"'", curDir);
sprintf(command,"/usr/bin/mpv -fs --title=mpvFullscreen \"%s\"  > /tmp/mpv.log 2> /tmp/mpv.log.stderr |cat", curDir);
system(command);
/*
 * TODO: figure out how to redraw everything...
 */
/*
//redraw();
//drawView();
//TView::drawView();
//TVDemo::cascade();
      deskTop->redraw();
              deskTop->drawView();
*/

                    break;
                    }
#ifndef __UNPATCHED
		//!! Handle directory selection.
                case cmDirSelection:
                    chDirButton->makeDefault( Boolean( long(
			event.message.infoPtr ) ) );
                    return;     // NOTE: THIS IS RETURN NOT BREAK!!
#endif
                default:
                    return;
                }
/*
            dirList->newDirectory( curDir );
            int len = strlen( curDir );
	    // SS: changed
            if( len > 0 && curDir[len-1] == '/' )
                curDir[len-1] = EOS;
            strcpy( dirInput->data, curDir );
            dirInput->drawView();
            dirList->select();
*/
            clearEvent( event );
            }
        default:
            break;
        }
}

void MyTChDirDialog::setData( void * )
{
}

//XXX
void MyTChDirDialog::updateMyTDirListBox()
{
  //printf("updateMyTDirListBox called...\n");
  //system("play -n -c1 synth 0.6 sine 500 >/dev/null 2>/dev/null");//XXX
  dirList->doUpdate();
}

//TODO: remove this and dirInput, they are not being used anymore...
void MyTChDirDialog::setUpDialog()
{
    if( dirList != 0 )
        {
        char curDir[PATH_MAX];
        getCurDir( curDir );
        dirList->newDirectory( curDir );
	/*
        if( dirInput != 0 )
            {
            int len = strlen( curDir );
	     //SS: changed 
            if( len > 0 && curDir[len-1] == '/' )
                curDir[len-1] = EOS;
            strcpy( dirInput->data, curDir );
            dirInput->drawView();
            } 
        */
    } 
    strcpy(dirInput->data, "mkv,mp4");
}

static int changeDir( const char *path )
{
    /* SS: changed */
    //XXX
    //return chdir( path );
    return 0;
}

Boolean MyTChDirDialog::valid( ushort command )
{

    if( command != cmOK )
        return True;

    char path[PATH_MAX];
    strcpy( path, dirInput->data );

#ifndef __UNPATCHED
    // BUG FIX - EFW - Tue 05/16/95
    // Ignore "Drives" line if switching drives.
    if(!strcmp(path, drivesText))
        path[0] = EOS;

    // If it was "Drives" or the input line was blank, issue a
    // cmChangeDir event to select the current drive/directory.
    if(!path[0])
    {
        TEvent event;
        event.what = evCommand;
        event.message.command = cmChangeDir;
        putEvent(event);
        return False;
    }

    // Otherwise, expand and check the path.
#endif
    fexpand( path );

    int len = strlen( path );
    /* SS: changed */
    if( len > 0 && path[len-1] == '/' )
        path[len-1] = EOS;

    if( changeDir( path ) != 0 )
        {
        messageBox( invalidText, mfError | mfOKButton );
        return False;
        }
    return True;
}

#if !defined(NO_STREAMABLE)

void MyTChDirDialog::write( opstream& os )
{
    TDialog::write( os );
    os << dirList << dirInput << okButton << chDirButton;
}

void *MyTChDirDialog::read( ipstream& is )
{
    TDialog::read( is );
    is >> dirList >> dirInput >> okButton >> chDirButton;
    setUpDialog();
    return this;
}

TStreamable *MyTChDirDialog::build()
{
    return new MyTChDirDialog( streamableInit );
}

#endif
