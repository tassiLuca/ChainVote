#!/bin/bash
#
# This script executes the enrollment of the registrar of each CA and registers all entities.

set -e  # Exit immediately if some command (simple or compound) returns a non-zero status

#####################################################################################################################
# Bring up TLS-CA
#####################################################################################################################
echo "Working on TLS-CA"

export FABRIC_CA_CLIENT_TLS_CERTFILES=$ARTIFACTS_DIR/tls-ca/crypto/tls-cert.pem
export FABRIC_CA_CLIENT_HOME=$ARTIFACTS_DIR/tls-ca/admin

fabric-ca-client enroll -d -u https://tls-ca-admin:tls-ca-adminpw@0.0.0.0:7052
sleep 5

# Identities for the orderer organization 
fabric-ca-client register -d --id.name orderer1-org0 --id.secret orderer1PW --id.type orderer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name orderer2-org0 --id.secret orderer2PW --id.type orderer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name orderer3-org0 --id.secret orderer3PW --id.type orderer -u https://0.0.0.0:7052

# Identities for the organization 1
fabric-ca-client register -d --id.name peer1-org1 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name client-org1 --id.secret clientPW --id.type client -u https://0.0.0.0:7052

# Identities for the organization 2 
fabric-ca-client register -d --id.name peer1-org2 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name peer2-org2 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name client-org2 --id.secret clientPW --id.type client -u https://0.0.0.0:7052

# Identities for the organization 3
fabric-ca-client register -d --id.name peer1-org3 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name peer2-org3 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7052
fabric-ca-client register -d --id.name client-org3 --id.secret clientPW --id.type client -u https://0.0.0.0:7052

#####################################################################################################################
# Bring up RCA-ORG0
#####################################################################################################################
echo "Working on RCA-ORG0"

export FABRIC_CA_CLIENT_TLS_CERTFILES=$ARTIFACTS_DIR/org0/ca/crypto/ca-cert.pem
export FABRIC_CA_CLIENT_HOME=$ARTIFACTS_DIR/org0/ca/admin

fabric-ca-client enroll -d -u https://rca-org0-admin:rca-org0-adminpw@0.0.0.0:7053
sleep 5

fabric-ca-client register -d --id.name orderer1-org0 --id.secret orderer1pw --id.type orderer -u https://0.0.0.0:7053
fabric-ca-client register -d --id.name orderer2-org0 --id.secret orderer2pw --id.type orderer -u https://0.0.0.0:7053
fabric-ca-client register -d --id.name orderer3-org0 --id.secret orderer3pw --id.type orderer -u https://0.0.0.0:7053
fabric-ca-client register -d --id.name admin-org0 --id.secret org0adminpw --id.type admin --id.attrs "hf.Registrar.Roles=client,hf.Registrar.Attributes=*,hf.Revoker=true,hf.GenCRL=true,admin=true.init=true:ecert" -u https://0.0.0.0:7053

#####################################################################################################################
# Bring up RCA-ORG1
#####################################################################################################################
echo "Working on RCA-ORG1"

export FABRIC_CA_CLIENT_TLS_CERTFILES=$ARTIFACTS_DIR/org1/ca/crypto/ca-cert.pem
export FABRIC_CA_CLIENT_HOME=$ARTIFACTS_DIR/org1/ca/admin

fabric-ca-client enroll -d -u https://rca-org1-admin:rca-org1-adminpw@0.0.0.0:7054
sleep 5

fabric-ca-client register -d --id.name peer1-org1 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7054
fabric-ca-client register -d --id.name admin-org1 --id.secret org1AdminPW --id.type admin -u https://0.0.0.0:7054
fabric-ca-client register -d --id.name client-org1 --id.secret clientPW --id.type client -u https://0.0.0.0:7054

#####################################################################################################################
# Bring up RCA-ORG2
#####################################################################################################################
echo "Working on RCA-ORG2"

export FABRIC_CA_CLIENT_TLS_CERTFILES=$ARTIFACTS_DIR/org2/ca/crypto/ca-cert.pem
export FABRIC_CA_CLIENT_HOME=$ARTIFACTS_DIR/org2/ca/admin

fabric-ca-client enroll -d -u https://rca-org2-admin:rca-org2-adminpw@0.0.0.0:7055
sleep 5

fabric-ca-client register -d --id.name peer1-org2 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7055
fabric-ca-client register -d --id.name peer2-org2 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7055
fabric-ca-client register -d --id.name admin-org2 --id.secret org2AdminPW --id.type admin -u https://0.0.0.0:7055
fabric-ca-client register -d --id.name client-org2 --id.secret clientPW --id.type client -u https://0.0.0.0:7055

#####################################################################################################################
# Bring up RCA-ORG3
#####################################################################################################################
echo "Working on RCA-ORG3"

export FABRIC_CA_CLIENT_TLS_CERTFILES=$ARTIFACTS_DIR/org3/ca/crypto/ca-cert.pem
export FABRIC_CA_CLIENT_HOME=$ARTIFACTS_DIR/org3/ca/admin

fabric-ca-client enroll -d -u https://rca-org3-admin:rca-org3-adminpw@0.0.0.0:7056
sleep 5

fabric-ca-client register -d --id.name peer1-org3 --id.secret peer1PW --id.type peer -u https://0.0.0.0:7056
fabric-ca-client register -d --id.name peer2-org3 --id.secret peer2PW --id.type peer -u https://0.0.0.0:7056
fabric-ca-client register -d --id.name admin-org3 --id.secret org3AdminPW --id.type admin -u https://0.0.0.0:7056
fabric-ca-client register -d --id.name client-org3 --id.secret clientPW --id.type client -u https://0.0.0.0:7056

echo "All CA and registration done"
