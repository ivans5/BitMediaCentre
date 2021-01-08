#!/bin/bash
set -e

    rm -fv .//my-machine-id .//private_key.pem .//public_key.der* 

    python3 -c 'import random;print ("".join(random.choice("ABCDEFGHJKMNPQRTUVWXY346789") for _ in range(6)))' > .//my-machine-id
    
    # generate a 2048-bit RSA private key
    openssl genrsa -out .//private_key.pem 1024
    chmod 644 .//private_key.pem
    
    # convert private Key to PKCS#8 format (so Java can read it)
    #openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem \
    #    -out .//private_key.der -nocrypt
    
    # output public key portion in DER format (so Java can read it)
    openssl rsa -in .//private_key.pem -pubout -outform DER -out .//public_key.der
    
    #Unfortunately there is a 63 char limit on labels, and i cant figure out how to use annotations instead,
    #Also, base64 encoding cannot be used for label values :(

    #hexdump dat der for user in node label
    python3 -c 'print ("".join("{:02x}".format(b) for b in open(".//public_key.der","rb").read()), end="")' > .//public_key.der.hexdump
    split -b62 .//public_key.der.hexdump .//public_key.der.hexdump.

cp -v ./my-machine-id /start/  #for mymc... 

/bin/chcon -t container_file_t /home/pcuser/*


