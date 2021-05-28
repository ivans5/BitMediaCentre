%define _topdir 	%(echo $PWD)/rpmbuild
%define _pwd            %(echo $PWD)
%define name 		bitmediacentre-base
%define release 	el6
%define version		1.0
%define buildroot %{_topdir}/%{name}-%{version}-root

BuildRoot: %{buildroot}
Summary:	bitmediacentre-base
License:	GPL
Name:		%{name}
Version:	%{version}
Release:	%{release}
Prefix:		/
%description
bitmediacentre-base rpm

#%prep
#%setup -q

#%build
#./configure
#make

%install
mkdir -p $RPM_BUILD_ROOT/etc/systemd/system/
mkdir -p $RPM_BUILD_ROOT/lib/systemd/system-preset/
mkdir -p $RPM_BUILD_ROOT/etc/sway/config.d/
mkdir -p $RPM_BUILD_ROOT/usr/start/youtube
mkdir -p $RPM_BUILD_ROOT/usr/start/bin
mkdir -p $RPM_BUILD_ROOT/usr/start/etc
mkdir -p $RPM_BUILD_ROOT/etc/NetworkManager/dispatcher.d/
mkdir -p $RPM_BUILD_ROOT/etc/mytransmission-daemon/
cp %{_pwd}/bitmediacentre-start.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/50-bitmediacentre.preset $RPM_BUILD_ROOT/lib/systemd/system-preset/
cp %{_pwd}/compositor2@7.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/terminal.timer $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/terminal.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/mysway.config $RPM_BUILD_ROOT/etc/sway/config.d/
cp %{_pwd}/mydbus.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/mydbus.socket $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/pulseaudio.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/k3s-agent.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/generate-machine-id-and-keypair.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/setup-gnome-terminal.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/install-upgrade-youtube-dl.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/rc.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/myevemu-device.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/fakekeyboard.prop $RPM_BUILD_ROOT/usr/start/etc/
cp %{_pwd}/youtube-dl/*.sh $RPM_BUILD_ROOT/usr/start/youtube
cp %{_pwd}/../mymc/mymc/mymc $RPM_BUILD_ROOT/usr/start/bin/
cp %{_pwd}/rc-server2.py $RPM_BUILD_ROOT/usr/start/bin/
cp %{_pwd}/00rcserver $RPM_BUILD_ROOT/etc/NetworkManager/dispatcher.d/
cp %{_pwd}/env.sh $RPM_BUILD_ROOT/usr/start/etc/
cp %{_pwd}/generate-machine-id-and-keypair.sh $RPM_BUILD_ROOT/usr/start/bin/
cp %{_pwd}/setup-gnome-terminal.sh $RPM_BUILD_ROOT/usr/start/bin/
cp %{_pwd}/configure-firewalld.sh $RPM_BUILD_ROOT/usr/start/bin/
cp %{_pwd}/configure-firewalld.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/settings.json $RPM_BUILD_ROOT/etc/mytransmission-daemon/
cp %{_pwd}/mytransmission-daemon.service $RPM_BUILD_ROOT/etc/systemd/system/
exit 0 #https://stackoverflow.com/questions/30317213/how-to-remove-pyo-anc-pyc-from-an-rpm

%files
%defattr(-,root,root)
/etc/systemd/system/bitmediacentre-start.service
/lib/systemd/system-preset/50-bitmediacentre.preset
/etc/systemd/system/compositor2@7.service
/etc/systemd/system/mydbus.service
/etc/systemd/system/mydbus.socket
/etc/systemd/system/terminal.service
/etc/systemd/system/terminal.timer
/etc/systemd/system/k3s-agent.service
/etc/systemd/system/rc.service
/etc/systemd/system/myevemu-device.service
/etc/systemd/system/pulseaudio.service
/etc/systemd/system/generate-machine-id-and-keypair.service
/etc/systemd/system/setup-gnome-terminal.service
/etc/systemd/system/install-upgrade-youtube-dl.service
/etc/systemd/system/configure-firewalld.service
/etc/sway/config.d/mysway.config
/usr/start/youtube/*.sh
/usr/start/bin/mymc
/usr/start/bin/rc-server2.py
/usr/start/etc/fakekeyboard.prop
/usr/start/bin/generate-machine-id-and-keypair.sh
/usr/start/bin/setup-gnome-terminal.sh
/usr/start/bin/configure-firewalld.sh
/usr/start/etc/env.sh
/etc/NetworkManager/dispatcher.d/00rcserver
/etc/mytransmission-daemon/settings.json
/etc/systemd/system/mytransmission-daemon.service

%post
systemctl preset bitmediacentre-start.service
#systemctl preset compositor2@7.service DOESNT WORK
ln -sf /etc/systemd/system/compositor2@7.service /etc/systemd/system/graphical.target.wants/compositor2\@7.service
systemctl preset mydbus.service
systemctl preset mydbus.socket
#systemctl preset terminal.service
systemctl preset terminal.timer
systemctl preset k3s-agent.service
systemctl preset rc.service
systemctl preset generate-machine-id-and-keypair.service
#Donot run as root: https://forums.linuxmint.com/viewtopic.php?t=197157:
systemctl preset setup-gnome-terminal.service
systemctl preset install-upgrade-youtube-dl.service
systemctl preset pulseaudio.service
systemctl preset configure-firewalld.service
systemctl preset mytransmission-daemon.service
