#!/bin/bash
#Setup gnome-terminal with transparency,etc:
set -a

PROFILE_ID=9cd613fb-3e56-45ab-8895-58a9214fa002
dconfdir=/org/gnome/terminal/legacy/profiles:

dconf write "$dconfdir/:$PROFILE_ID"/visible-name "'myprofile'"
dconf write /org/gnome/terminal/legacy/profiles:/list "['9cd613fb-3e56-45ab-8895-58a9214fa002']"
dconf write /org/gnome/terminal/legacy/profiles:/default "'9cd613fb-3e56-45ab-8895-58a9214fa002'"
#Not sure if needed:
gsettings set org.gnome.Terminal.ProfilesList default 9cd613fb-3e56-45ab-8895-58a9214fa002

dconf write /org/gnome/terminal/legacy/profiles:/:$PROFILE_ID/background-transparency-percent 40
dconf write /org/gnome/terminal/legacy/profiles:/:9cd613fb-3e56-45ab-8895-58a9214fa002/use-transparent-background true
dconf write /org/gnome/terminal/legacy/profiles:/:9cd613fb-3e56-45ab-8895-58a9214fa002/scrollbar-policy "'never'"

