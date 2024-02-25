#!/bin/bash
mpv $MPV_EXTRA_OPTS --title=mpvFullscreen -fs $(yt-dlp -f best -g "ytsearch1:al jazeera english live")
