#!/bin/bash
set -e

firewall-cmd --zone=public --add-masquerade --permanent
firewall-cmd --zone=public --add-interface=cni0 --permanent
#for rc.service:
firewall-cmd --add-port=10000/udp --permanent
#for transmission:
firewall-cmd --zone=public --add-port=51413/udp --permanent
firewall-cmd --zone=public --add-port=9091/tcp --permanent

firewall-cmd --reload
