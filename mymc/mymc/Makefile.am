AUTOMAKE_OPTIONS = foreign

noinst_PROGRAMS = mymc

mymc_SOURCES = ascii.cc calc.cc calendar.cc fileview.cc gadgets.cc \
mousedlg.cc puzzle.cc tvdemo1.cc tvdemo2.cc tvdemo3.cc \
ascii.h calc.h calendar.h demohelp.h fileview.h gadgets.h mousedlg.h \
puzzle.h tvcmds.h tvdemo.h

LDADD = ../lib/libtvision.a

AM_CPPFLAGS = -I../lib

EXTRA_DIST = DEMOHELP.H32
