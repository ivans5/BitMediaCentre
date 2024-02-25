/*-------------------------------------------------------------------*/
/*                                                                   */
/*   Turbo Vision Demo                                               */
/*                                                                   */
/*   Gadgets.cpp:  Gadgets for the Turbo Vision Demo.  Includes a    */
/*        heap view and a clock view which display the clock at the  */
/*        right end of the menu bar and the current heap space at    */
/*        the right end of the status line.                          */
/*                                                                   */
/*-------------------------------------------------------------------*/
/*
 *      Turbo Vision - Version 2.0
 *
 *      Copyright (c) 1994 by Borland International
 *      All Rights Reserved.
 *
 */
/*
 * Modified by Sergio Sigala <sergio@sigala.it>
 */

#define Uses_TRect
#define Uses_TView
#define Uses_TDrawBuffer
#include <tvision/tv.h>

#include <cstring>
#include <cstdlib>
#include <cctype>
#include <sstream>
#include <iomanip>
#include <ctime>

#include "gadgets.h"

//extern "C" unsigned long farcoreleft( void );

//
// ------------- Heap Viewer functions
//

THeapView::THeapView(TRect& r) : TView( r )
{
    oldMem = 0;
    newMem = heapSize();

	/* SS: now resizing under X works well */
	growMode = gfGrowLoX | gfGrowLoY | gfGrowHiX | gfGrowHiY;
}


void THeapView::draw()
{
    TDrawBuffer buf;
    char c = getColor(2);

    buf.moveChar(0, ' ', c, (short)size.x);
    buf.moveStr(0, heapStr, c);
    writeLine(0, 0, (short)size.x, 1, buf);
}


void THeapView::update()
{
    //if( (newMem = heapSize()) != oldMem )
    //    {
    //    oldMem = newMem;
        newMem = heapSize();  //<-- TODO: only call drawView if free space has changed:
        drawView();
   //     }
}

//XXX -free space feature
#include <stdio.h>
#include <sys/statvfs.h>
double getAvailableSpace()  {  //arg is "/" - https://gist.github.com/vgerak/8539104
    const unsigned int GB = (1024 * 1024) * 1024;
    struct statvfs buffer;
    int ret = statvfs("/" /* argv[1] */, &buffer);

    if (!ret) {
        const double total = (double)(buffer.f_blocks * buffer.f_frsize) / GB;
        const double available = (double)(buffer.f_bfree * buffer.f_frsize) / GB;
        const double used = total - available;
        const double usedPercentage = (double)(used / total) * (double)100;
        return available;
        //printf("Total: %f --> %.0f\n", total, total);
        //printf("Available: %f --> %.0f\n", available, available);
        //printf("Used: %f --> %.1f\n", used, used);
        //printf("Used Percentage: %f --> %.0f\n", usedPercentage, usedPercentage);
    }
    return -1.0;
}


long THeapView::heapSize()
{
	/* SS: changed */
#if 0
//#if !defined( __DPMI32__ )
//    long total = farcoreleft();
//#else
    long total = 0;
//#endif

#if !defined( __DPMI16__ ) && !defined( __DPMI32__ )
    struct farheapinfo heap;
#endif

    ostrstream totalStr( heapStr, sizeof heapStr);

//#if defined( __DPMI32__ )
//    switch( _HEAPEMPTY )
//#else
    switch( heapcheck() )
//#endif
        {
        case _HEAPEMPTY:
            strcpy(heapStr, "     No heap");
            total = -1;
            break;

        case _HEAPCORRUPT:
            strcpy(heapStr, "Heap corrupt");
            total = -2;
            break;

        case _HEAPOK:
#if !defined( __DPMI16__ ) && !defined( __DPMI32__ )
            heap.ptr = NULL;
            while(farheapwalk(&heap) != _HEAPEND)
                if(!heap.in_use)
                    total += heap.size;
#endif
            totalStr << std::setw(12) << total << std::ends;
            break;
        }
    return(total);
#endif

	//strcpy(heapStr, "Hello world!");
	//XXX this runs once at startup ...
	    char c[1000];
    FILE *fptr;
    if ((fptr = fopen("/start/my-machine-id", "r")) == NULL) {
        //printf("Error! opening file");
        // Program exits if file pointer returns NULL.
        //exit(1);
	strcpy(heapStr, "ID: UNKNOWN");
        return -1;
    }

    // reads text until newline is encountered
    fscanf(fptr, "%[^\n]", c);
    fclose(fptr);

    double freeSpace = getAvailableSpace();

    sprintf(heapStr, "ID: %s %.02fG FREE", c, freeSpace);
	return -1;
}


//
// -------------- Clock Viewer functions
//

TClockView::TClockView( TRect& r ) : TView( r )
{
    strcpy(lastTime, "        ");
    strcpy(curTime, "        ");

	/* SS: now resizing under X works well */
	growMode = gfGrowLoX | gfGrowHiX;
}


void TClockView::draw()
{
    TDrawBuffer buf;
    char c = getColor(2);

    buf.moveChar(0, ' ', c, (short)size.x);
    buf.moveStr(0, curTime, c);
    writeLine(0, 0, (short)size.x, 1, buf);
}


void TClockView::update()
{
    time_t t = time(0);
    char *date = ctime(&t);

    date[19] = '\0';
    char tmpCurTime[9];

    strcpy(tmpCurTime, &date[11]);        /* Extract time. */
    sprintf(curTime,"%s",tmpCurTime);

    if( strcmp(lastTime, curTime) )
        {
        drawView();
        strcpy(lastTime, curTime);
        }
}
