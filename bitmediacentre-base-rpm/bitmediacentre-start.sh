#!/bin/bash

[ -e /env.sh ] || ln -s /usr/start/etc/env.sh /env.sh

#Note:You can break the link to /usr/start/youtube if you want to customize that folder:
[ -e /start ] || { mkdir -p /var/start/; ln -s /usr/start/youtube /var/start/youtube; ln -s /var/start /start; ln -s /var/home/pcuser /homepcuser; }


#Note: according https://www.freedesktop.org/wiki/Software/PulseAudio/FAQ/
#XDG_RUNTIME_DIR is supposed to be on ramdisk!

rm -rf /tmp/xdg_runtime_dir
mkdir -p /tmp/xdg_runtime_dir
chown pcuser:pcuser /tmp/xdg_runtime_dir
chmod 700 /tmp/xdg_runtime_dir
chcon -u system_u -t user_tmp_t /tmp/xdg_runtime_dir

exit 0
