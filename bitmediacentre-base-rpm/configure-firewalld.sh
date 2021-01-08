#!/bin/bash
set -e

firewall-cmd --zone=public --add-masquerade --permanent
firewall-cmd --zone=public --add-interface=cni0 --permanent
firewall-cmd --add-port=10000/udp --permanent
firewall-cmd --reload
