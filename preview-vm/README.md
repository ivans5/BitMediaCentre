# Preview using VirtualBox VM

## Usage

To use, simply Import appliance file into VirtualBox programme.

## Build steps

1. get Alpine Linux "virt" iso from: https://alpinelinux.org/downloads/

2. Create a new VM and add an extra 20G hdd

3. boot disc and run `setup-alpine` to install to the first hdd, reboot into newly installed system

4. replace syslinux with grub2, copy grub.cfg.tmpl file over to /boot/grub:
```
apk del syslinux
apk add grub grub-bios
grub-install /dev/sda
```

5. install newt and real wget:
```
apk add -U newt wget
```

6. Install downloader script to /etc/init.d/downloader and enable in OpenRC
```
rc-update add downloader default
```



