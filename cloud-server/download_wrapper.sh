#!/bin/bash
set -e

KEY_SPEC=$(echo -n $ENCRYPTED_KEY_SPEC|base64 --decode|openssl rsautl -decrypt -inkey /homepcuser/private_key.pem -raw)
IV_SPEC=$(echo -n $ENCRYPTED_IV_SPEC|base64 --decode|openssl rsautl -decrypt -inkey /homepcuser/private_key.pem -raw)
PAYLOAD=$(echo -n $ENCRYPTED_PAYLOAD|base64 --decode|openssl aes-128-cbc -nosalt -K $KEY_SPEC -iv $IV_SPEC -d)

echo STARTING

cd /homepcuser && exec aria2c "$PAYLOAD" --seed-time=0
