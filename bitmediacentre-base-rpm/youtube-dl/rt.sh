#!/bin/sh
#F:
#mpv --title=mpvFullscreen -fs $(/usr/local/bin/youtube-dl -f best -g "ytsearch1:RT News: On-air livestream 24/7")
#
#
#DOESNT WORK:
#. /start/youtube/playURL.sh
#playURL https://rt.com/on-air/ #--> network tab,Method GET:,Initiatior:xhr,Type:vnd.apple.mpegurl
#


mpv $MPV_EXTRA_OPTS -fs --stream-buffer-size=1024768 /start/youtube/resources/splash.mp4 https://rt-glb.rttv.com/dvr/rtnews/playlist_1600Kb.m3u8 2>&1 |tee /tmp/rt.log


