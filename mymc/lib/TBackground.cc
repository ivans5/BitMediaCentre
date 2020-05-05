/*
 * TBackground.cc
 *
 * Turbo Vision - Version 2.0
 *
 * Copyright (c) 1994 by Borland International
 * All Rights Reserved.
 *
 * Modified by Sergio Sigala <sergio@sigala.it>
 */

#define Uses_TBackground
#define Uses_TDrawBuffer
#define Uses_opstream
#define Uses_ipstream
#include <tvision/tv.h>

#define cpBackground "\x01"      // background palette

TBackground::TBackground( const TRect& bounds, char aPattern ) :
    TView(bounds),
    pattern( aPattern )
{
    growMode = gfGrowHiX | gfGrowHiY;
}

int i=0;
void TBackground::draw()
{
    TDrawBuffer b;

    //b.moveChar( 0, pattern, getColor(0x0302), size.x );
    b.moveChar( 0, pattern,17, size.x ); // <-- a nice blue :)
    //b.moveChar( 0, pattern,24, size.x ); <--- grey on dark blue
    //b.moveChar( 0, pattern,112, size.x ); <-- black on gray
    //b.moveChar( 0, pattern,115, size.x ); <-- cyan on gray
    //b.moveChar( 0, pattern,120, size.x ); <-- dark grey on gray
    //
    //pattern='\xF4';
    //b.moveChar( 0, pattern,120, size.x );
    writeLine( 0, 0, size.x, size.y, b );//XXX - transparent backgrnd  - this will not take affect...
}

TPalette& TBackground::getPalette() const
{
    static TPalette palette( cpBackground, sizeof( cpBackground )-1 );
    return palette;
}

#if !defined(NO_STREAMABLE)

TBackground::TBackground( StreamableInit ) : TView( streamableInit )
{
}

void TBackground::write( opstream& os )
{
    TView::write( os );
    os << pattern;
}

void *TBackground::read( ipstream& is )
{
    TView::read( is );
    is >> pattern;
    return this;
}

TStreamable *TBackground::build()
{
    return new TBackground( streamableInit );
}

#endif
