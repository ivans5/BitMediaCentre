#!/bin/bash
#TODO: set working directory in systemd unit...
set -e

if [ ! -f /start/my-machine-id ] || [ ! -f /start/private_key.pem ] || [ ! -f /start/public_key.der ]; then

    rm -fv /start/my-machine-id /start/private_key.pem /start/public_key.der* 

    python3 -c 'import random;print ("".join(random.choice("ABCDEFGHJKMNPQRTUVWXY346789") for _ in range(6)))' > /start/my-machine-id
    
    # generate a 2048-bit RSA private key
    openssl genrsa -out /start/private_key.pem 1024
    chmod 644 /start/private_key.pem
    
    # convert private Key to PKCS#8 format (so Java can read it)
    #openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem \
    #    -out /start/private_key.der -nocrypt
    
    # output public key portion in DER format (so Java can read it)
    openssl rsa -in /start/private_key.pem -pubout -outform DER -out /start/public_key.der
    
    #Unfortunately there is a 63 char limit on labels, and i cant figure out how to use annotations instead,
    #Also, base64 encoding cannot be used for label values :(

    #hexdump dat der for user in node label
    python3 -c 'print ("".join("{:02x}".format(b) for b in open("/start/public_key.der","rb").read()), end="")' > /start/public_key.der.hexdump
    split -b62 /start/public_key.der.hexdump /start/public_key.der.hexdump.

else
    echo keys already generated, skipping
fi
