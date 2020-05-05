install
url --url="http://mirror.csclub.uwaterloo.ca/fedora/linux/releases/32/Everything/x86_64/os/"
repo --name=base --baseurl=http://mirror.csclub.uwaterloo.ca/fedora/linux/releases/32/Everything/x86_64/os/
repo --name=updates-released-f32 --baseurl=http://mirror.csclub.uwaterloo.ca/fedora/linux/updates/32/Everything/x86_64/
#repo --name=testing-released-f32 --baseurl=http://mirror.csclub.uwaterloo.ca/fedora/linux/updates/testing/32/Everything/x86_64/
repo --name=rpmfusion-free --baseurl=http://download1.rpmfusion.org/free/fedora/releases/$releasever/Everything/$basearch/os
repo --name=rpmfusion-nonfree --baseurl=http://download1.rpmfusion.org/nonfree/fedora/releases/$releasever/Everything/$basearch/os/
repo --name=rpmfusion-free-updates --baseurl=http://download1.rpmfusion.org/free/fedora/updates/$releasever/$basearch

lang en_US.UTF-8
keyboard us
timezone America/Vancouver
auth --useshadow --enablemd5
selinux --disabled
firewall --disabled
services --enabled=NetworkManager,sshd
network --hostname=bitmediacentre
eula --agreed
ignoredisk --only-use=sda
text

#--append="video=HDMI-A-1:d video=DP-1:1920x1080@60+32":
bootloader --location=mbr 
zerombr
clearpart --all --initlabel
part /boot --size=512 --asprimary --ondrive=sda --fstype=xfs
#part swap --size=10240 --ondrive=sda 
part / --size=8192 --grow --asprimary --ondrive=sda --fstype=xfs 

user --name=pcuser --password=pcuser --plaintext --groups=wheel
rootpw --lock

%packages 
@core
@networkmanager-submodules
@hardware-support
kernel
kernel-modules
kernel-modules-extra
#cmdline:
net-tools
wget
tar
-vim-minimal
vim
tree
w3m
aria2
youtube-dl
python3-streamlink
mc
#sound:
sox
pulseaudio
pulseaudio-utils
#alsa-utils
#video player:
mpv
ffmpeg
#minimal wayland systemd target:
@basic-desktop
-awesome
#-lightdm
sway
#TODO: delete this:
#terminology
#terminus-fonts
#weston
gnome-terminal
#gnome-backgrounds-extras
epiphany
grim
#
#@base-x
#@fonts
#@gnome-desktop
#Docker:
docker
libcgroup-tools
#for rc-server:
python3-evdev
python3-netifaces
%end

#%pre
###XXX - HACK TO USE ANACONDA OVER WIFI:
#nmcli r wifi on
#nmcli d wifi connect REDACTED_SSID password REDACTED_PSK
#ifconfig enp0s25 1.2.3.4 netmask 255.255.255.255 up
#sleep 5
#%end

%post
cat > /etc/sudoers <<END
Defaults   !visiblepw
Defaults    env_reset
Defaults    env_keep =  "COLORS DISPLAY HOSTNAME HISTSIZE KDEDIR LS_COLORS"
Defaults    env_keep += "MAIL PS1 PS2 QTDIR USERNAME LANG LC_ADDRESS LC_CTYPE"
Defaults    env_keep += "LC_COLLATE LC_IDENTIFICATION LC_MEASUREMENT LC_MESSAGES"
Defaults    env_keep += "LC_MONETARY LC_NAME LC_NUMERIC LC_PAPER LC_TELEPHONE"
Defaults    env_keep += "LC_TIME LC_ALL LANGUAGE LINGUAS _XKB_CHARSET XAUTHORITY"
Defaults    secure_path = /sbin:/bin:/usr/sbin:/usr/bin
root	ALL=(ALL) 	ALL
%wheel	ALL=(ALL)	NOPASSWD: ALL
END
chmod 0440 /etc/sudoers
#XXX - Kickstart can only append to grub cmdline, not replace it...
#cat > /etc/default/grub <<END
#GRUB_TIMEOUT=0
#GRUB_DISTRIBUTOR="$(sed 's, release .*$,,g' /etc/system-release)"
#GRUB_DEFAULT=saved
#GRUB_DISABLE_SUBMENU=true
#GRUB_TERMINAL_OUTPUT="console"
#GRUB_CMDLINE_LINUX="video=HDMI-A-1:d video=DP-1:1920x1080@60+32 systemd.unified_cgroup_hierarchy=0"
#GRUB_DISABLE_RECOVERY="true"
#END
#grub2-mkconfig > /boot/grub2/grub.cfg

#TODO: couldnt get this working from the network --device= linux above...
#nmcli dev wifi connect REDACTED password 'REDACTED'
cat > /etc/sysconfig/network-scripts/ifcfg-REDACTED_SSID <<END
ESSID=REDACTED_SSID
MODE=Managed
KEY_MGMT=WPA-PSK
SECURITYMODE=open
MAC_ADDRESS_RANDOMIZATION=default
TYPE=Wireless
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=dhcp
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=REDACTED_SSID
ONBOOT=yes
END
cat > /etc/sysconfig/network-scripts/keys-REDACTED_SSID <<END
WPA_PSK="REDACTED_PSK"
END
chmod 600 /etc/sysconfig/network-scripts/keys-REDACTED_SSID
cat > /etc/systemd/system/pulseaudio.service <<END
[Unit]

[Service]
ExecStart=/usr/bin/pulseaudio --disallow-exit --exit-idle-time=-1
EnvironmentFile=/env.sh

[Install]
WantedBy=graphical.target
END
cat > /etc/systemd/system/compositor.service <<END
[Unit]

[Service]
#ExecStart=/bin/openvt -v -w -s -- /bin/sway
#TODO: figure out how to run sway as root, like it was before with sway0.15:
ExecStart=/bin/openvt -v -w -s -- su -m pcuser /bin/sh -c 'set -a;. /env.sh;exec /bin/sway'
EnvironmentFile=/env.sh
CPUShares=1500

[Install]
WantedBy=graphical.target
END
systemctl set-default graphical.target
systemctl disable accounts-daemon.service
systemctl disable lightdm.service
systemctl enable compositor.service
systemctl enable pulseaudio.service

mkdir -p /start/youtube
ln -s /home/pcuser /start/pcuser
cat > /start/youtube/aljazeera2.sh <<END
mpv --title=mpvFullscreen -fs \$(youtube-dl -f best -g "ytsearch1:al jazeera english live")
END
cat > /start/youtube/bloomberg.sh <<END
mpv --title=mpvFullscreen -fs \$(youtube-dl -f best -g "ytsearch1:Bloomberg Global News")
END
cat > /start/youtube/gk-pikes.sh <<END
mpv --title=mpvFullscreen -fs https://www.youtube.com/watch?v=Hg6L_7qLIEQ
END
cat > /start/youtube/rt.sh <<END
mpv --title=mpvFullscreen -fs \$(youtube-dl -f best -g "ytsearch1:RT News: On-air livestream 24/7")
END
chmod +x /start/*.sh /start/youtube/*.sh

cat > /env.sh <<END
HOME=/
XDG_RUNTIME_DIR=/tmp
XDG_CONFIG_HOME=/
DBUS_SESSION_BUS_ADDRESS=unix:abstract=/tmp/dbus-ZcOrhvdyeA,guid=4236f3efbf061cee0b6776865b8c7190
WLC_XWAYLAND=0
END

#TODO: delete this:
#gunzip -dc /usr/share/fonts/terminus/ter-x28b.pcf.gz > /usr/share/terminology/fonts/ter-x28b.pcf
#gunzip -dc /usr/share/fonts/terminus/ter-x32b.pcf.gz > /usr/share/terminology/fonts/ter-x32b.pcf

mv /usr/libexec/mc/ext.d/video.sh /usr/libexec/mc/ext.d/video.sh.orig
cat > /usr/libexec/mc/ext.d/video.sh <<END
#!/bin/sh
mpv -fs "\${MC_EXT_FILENAME}"
exit 0
END
chmod 755 /usr/libexec/mc/ext.d/video.sh

cat > /etc/systemd/system/terminal.timer <<END
[Unit]
After=compositor.service

[Timer]
OnActiveSec=4s
Unit=terminal.service

[Install]
WantedBy=graphical.target
END

cat > /etc/systemd/system/terminal.service <<END
[Unit]
After=mydbus.service

[Service]
Type=oneshot
EnvironmentFile=/env.sh
#ExecStart=/bin/terminology --fullscreen --theme=black --login=true --font=ter-x32b.pcf --visual-bell=false --exec 'cd /start && /bin/mc'
#ExecStart=/usr/bin/gnome-terminal --zoom=3.0 --maximize --full-screen --hide-menubar -- /home/pcuser/tvision.new3/demo/demo
ExecStart=/usr/bin/gnome-terminal --zoom=3.0 --hide-menubar -- /start/mymc
WorkingDirectory=/home/pcuser
END
systemctl enable terminal.timer

#Keep sounds card woke:
/bin/cp /etc/pulse/default.pa /etc/pulse/default.pa.orig -v
sed -i 's/^load-module module-suspend-on-idle/#&/' /etc/pulse/default.pa

systemctl enable docker.service

cat <<EOF | base64 -d - | gzip -dc - > /usr/share/icons/Adwaita/cursors/black.xcur
H4sICBcljFsAA2JsYWNrLnhjdXIA7dwxisJQEAbgcVHEYsVSuxQeQLZfyAE8gGXAC8jClt7Cw3gY
r7K4L74JeAQlX+DL+5uQdpg3zOH4+7OK/pnErLw/4u++Luc+c1PO06LmXcmXTc1tydfvmruSb+eI
7dP3k2Kdpunr8Z+4AwCMyVAjNVFrpCbNkhoJAAAAxmHoEfT3LX2PYJfmSY8AAAAAAAAAAADe2zAj
1EadEWrTIpkRAgAAAAAAAAAAAAAAAAAAgNcw7Ajoou4I6NKy+Aw7AgAAAAAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAGI9/MhNhNQAOAQA=
EOF
#mv /usr/share/icons/Adwaita/cursors/xterm /usr/share/icons/Adwaita/cursors/xterm.old
#ln -s black.xcur /usr/share/icons/Adwaita/cursors/xterm
mv /usr/share/icons/Adwaita/cursors/left_ptr /usr/share/icons/Adwaita/cursors/left_ptr.old
ln -s black.xcur /usr/share/icons/Adwaita/cursors/left_ptr

cat > /bin/cgrun <<END
set -a
. /env.sh
set +a
cgcreate -g cpu,cpuacct:system.slice/mystress.service
cgset -r cpu.shares=1500 system.slice/mystress.service
cgexec -g cpu,cpuacct:system.slice/mystress.service  "\$@"
END
chmod +x /bin/cgrun

chmod 777 /tmp

cat > /etc/sysctl.d/squelch.conf <<END
# Uncomment the following to stop low-level messages on console
kernel.printk = 3 4 1 3
END
mkdir -p /etc/sway/config.d
cat > /etc/sway/config.d/mysway.config <<END

for_window [title="mpvFullscreen"] fullscreen enable, border none, floating toggle, move window to position 0 0
for_window [title="Terminal"] border none, floating toggle, move window to position 0 0
END
cat > /etc/systemd/system/mydbus.service <<END
[Unit]

[Service]
EnvironmentFile=/env.sh
ExecStart=/usr/bin/dbus-daemon --session --print-pid=1 --print-address=1 --nofork --address=unix:abstract=/tmp/dbus-ZcOrhvdyeA,guid=4236f3efbf061cee0b6776865b8c7190
CPUShares=200000

[Install]
WantedBy=graphical.target
END
systemctl enable mydbus.service

#DroidMote Server:
mkdir -p /etc/init.d  #legacy support for droidmote server...
curl -Ls https://www.videomap.it/script/install_droidmote_ubuntu.sh | sh &
/etc/init.d/droidmote stop
cat > /etc/systemd/system/droidmote.service <<END
[Unit]

[Service]
ExecStart=/usr/bin/droidmote 2302 password

[Install]
WantedBy=graphical.target
END

#Setup gnome-terminal with transparency,etc:
set -a
source /env.sh
/usr/bin/dbus-daemon --session --print-pid=1 --print-address=1  --address=unix:abstract=/tmp/dbus-ZcOrhvdyeA,guid=4236f3efbf061cee0b6776865b8c7190 --fork

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


#Download latest "mymc" from github:
wget -O /start/mymc https://github.com/ivans5/BitMediaCentre/raw/master/mymc/mymc/mymc
chmod +x /start/mymc

#Download latest "rc-server" from github:
wget -O /start/rc-server.py https://github.com/ivans5/BitMediaCentre/raw/master/rc-server/rc-server.py
cat > /etc/systemd/system/rc.service <<END
[Unit]
After=network-online.target

[Service]
ExecStart=/bin/python3 /start/rc-server.py

[Install]
WantedBy=graphical.target
END

#Dont kill user processes:
echo KillUserProcesses=no >> /etc/systemd/logind.conf

#XXX - ENABLE OPTIONAL FEATURES HERE:
#systemctl enable droidmote.service
#systemctl enable rc.service
%end

