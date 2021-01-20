FROM fedora:latest

RUN dnf install -y aria2 procps-ng wget bind-utils net-tools openssl

COPY download_wrapper.sh /download_wrapper.sh

ENTRYPOINT /bin/bash /download_wrapper.sh

