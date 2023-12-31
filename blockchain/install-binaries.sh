#!/bin/bash
#
# This script downloads the necessary binaries that are needed to create the blockchain
# network and places them in the ./bin/ directory.

set -e  # Exit immediately if some command (simple or compound) returns a non-zero status

if [[ -d ./bin ]]; then 
    echo "Binaries already into $PWD/bin. Exiting."
    exit
fi;

curl -sSL https://raw.githubusercontent.com/hyperledger/fabric/master/scripts/bootstrap.sh | bash -s
mv ./fabric-samples/bin ./bin/
rm -rf ./fabric-samples
echo "Done. Binaries downloaded into $PWD/bin directory."
