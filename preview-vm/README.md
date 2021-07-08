# Preview using VirtualBox 6+ VM

## Usage

Download and Import appliance file into VirtualBox (6+!) programme.
Next, ensure the following settings:
* Enable 3D Acceleration (it's in the Display settings for VM)
* Max out VRAM
* Switch networking to "Bridged" mode

Steps:
1. Start the VM, wait for the download to complete, reboot VM when prompted...
2. Choose the NEW-KS.CFG grub entry to launch Fedora Anaconda installer
3. Once Fedora-IoT install complete, reboot to the BitMediaCentre *(Second)* hard drive

## Build the VM Appliance from scatch:

1. get Alpine Linux "virt" iso from: https://alpinelinux.org/downloads/

2. Create a new VM and add an extra 20G hdd (to the SATA controller)

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


