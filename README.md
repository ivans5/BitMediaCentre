# BitMediaCentre
Simple Media Centre PC Build Using [Fedora-IOT](https://getfedora.org/en/iot/)

## Features:

  * Wayland-only system with Sway 1.x compositor

  * RPMFusion repository (for mpv, etc)

  * firewalld and SELinux not disabled ;)

  * transmission-daemon downloading engine

  * no login screen, boots straight into a Simple "Commander"-style app, for managing media content:

![mymc-mpv.png](mymc-mpv.png) 

![mymc-youtube-dl.png](mymc-youtube-dl.png)

  * Remote Control Android App:

![resize_Screenshot_20200517-074115.png](resize_Screenshot_20200517-074115.png)

  * Launch new downloads by clicking on any `magnet` link whilst using the browser included in the app!
    
![resize_Screenshot_20200517-134658.png](resize_Screenshot_20200517-134658.png)

*here, 200 OK response means that the pod was accepted for scheduling by the cloud service*


## (Bare Metal) Installation Steps (EFI):
0. start with a blank USB stick, and download latest Fedora-IoT (OSTree, iso, x86_64) iso
1. create GPT partition  table (gdisk or fdisk)
2. create FAT32 partitions:
   partition 1: size 500M, type 1 (EFT);  parition 2: type 11 (Microsoft basic)
3. format FAT32 parition (mkfs.fat -F 32 /dev/sd?1)
4. mount FAT32 parition (to /mnt)
5. install grub2-efi-x64-modules.noarch
6. grub2-install --target=x86_64-efi --efi-directory=/mnt --bootloader-id=BOOT  #(Note: Secure boot!)
7. replace files in /EFI/BOOT with the one's from the ISO
8. copy initrd.img and vmlinuz from /images/pxeboot/ from the iso to the /mnt 
9. copy and modify the Kickstart file `NEW-KS.CFG` to /mnt:
wifi (search for "REDACTED")  
installation hard drive selection!
timezone
enable optional features  
any other customisation (eg. kparam)  
10. write the Fedora-IoT .iso file (eg. Fedora-IoT-ostree-x86_64-39-20231103.1.iso) to partition 2 of usb stick: dd if=bleh.iso of=/dev/sd?2 bs=1M status=progress
11. Edit the grub.cfg on new EFI partition and add something like this entry:
```
menuentry 'NEW-KS.CFG' --class fedora --class gnu-linux --class gnu --class os {
        set root='(hd0,gpt1)'
        linuxefi /vmlinuz inst.repo=hd:/dev/sda2 nomodeset inst.ks=hd:/dev/sda1:/NEW-KS.CFG
        initrdefi /initrd.img
}

```

Finally, Boot the USB stick in the TARGET computer (by selecting the id specified in step#6) and choose the grub option from step #11 (*CAUTION:* Will wipe hard drive without prompting!!)


