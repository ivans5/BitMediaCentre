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
mkdir -p $RPM_BUILD_ROOT/var/start/youtube
cp %{_pwd}/bitmediacentre-start.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/50-bitmediacentre.preset $RPM_BUILD_ROOT/lib/systemd/system-preset/
cp %{_pwd}/compositor.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/terminal.timer $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/terminal.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/mysway.config $RPM_BUILD_ROOT/etc/sway/config.d/
cp %{_pwd}/mydbus.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/k3s-agent.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/generate-machine-id-and-keypair.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/rc.service $RPM_BUILD_ROOT/etc/systemd/system/
cp %{_pwd}/*.sh $RPM_BUILD_ROOT/var/start/youtube
cp %{_pwd}/../mymc/mymc/mymc $RPM_BUILD_ROOT/var/start/
cp %{_pwd}/../rc-server/rc-server.py $RPM_BUILD_ROOT/var/start/
exit 0 #https://stackoverflow.com/questions/30317213/how-to-remove-pyo-anc-pyc-from-an-rpm

%files
%defattr(-,root,root)
/etc/systemd/system/bitmediacentre-start.service
/lib/systemd/system-preset/50-bitmediacentre.preset
/etc/systemd/system/compositor.service
/etc/systemd/system/mydbus.service
/etc/systemd/system/terminal.service
/etc/systemd/system/terminal.timer
/etc/systemd/system/k3s-agent.service
/etc/systemd/system/rc.service
/etc/systemd/system/generate-machine-id-and-keypair.service
/etc/sway/config.d/mysway.config
/var/start/youtube/*.sh
/var/start/mymc
/var/start/rc-server.py

%post
systemctl preset bitmediacentre-start.service
systemctl preset compositor.service
systemctl preset mydbus.service
systemctl preset terminal.service
systemctl preset terminal.timer
systemctl preset k3s-agent.service
systemctl preset rc.service
systemctl preset generate-machine-id-and-keypair.service
