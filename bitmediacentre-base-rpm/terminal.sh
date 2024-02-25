#!/bin/bash
cd /homepcuser

#1. determine swaysock
export SWAYSOCK=$XDG_RUNTIME_DIR/sway-ipc.$(id -u).$(pgrep -x sway).sock

#2. wait for Display to be ready by poling `get_outputs`
while
	LC=$(swaymsg -t get_outputs|wc -l)
	sleep 5
	echo sleep 5 waiting for display
	(( LC < 2 ))
do
	:
done	

#NOTE #1 from `wayland-1` here coresponds to it corresponds to a socket file in $XDG_RUNTIME_DIR:
WAYLAND_DISPLAY=wayland-1 /usr/bin/gnome-terminal --geometry=160x50 --zoom=$(dmesg|grep vboxguest >/dev/null && echo 1.0 || echo 3.58318) --hide-menubar -- /usr/start/bin/mymc

