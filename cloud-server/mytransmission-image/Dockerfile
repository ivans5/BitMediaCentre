FROM fedora:latest

RUN dnf install -y transmission transmission-cli transmission-daemon procps-ng wget bind-utils net-tools openssl

COPY download_wrapper.sh /download_wrapper.sh

ENTRYPOINT /bin/bash /download_wrapper.sh

