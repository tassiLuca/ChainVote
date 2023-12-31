#!/bin/bash
# 
# Script which automatize the startup of the whole system.

set -e  # Exit immediately if some command (simple or compound) returns a non-zero status

function print_help() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  up      Startup the blockchain network, the API service and the frontend application"
    echo "  down    Stop all the services without removing blockchain artifacts"
    echo "  clean   Stop all the services and remove blockchain artifacts"
    echo ""
    echo "Pre-requisites:"
    echo "  - Unix-like operating system (macOS or Linux)"
    echo "  - Docker"
    echo "  - Java"
    echo "  - npm"
}

function check_prerequisites() {
    local os_name=$(uname -s)
    if [[ "$os_name" != "Linux" && "$os_name" != "Darwin" ]]; then # Check if the host is a Unix-like operating system
        echo "Error: this script requires a Unix-like operating system (either macOS or Linux)."
        exit 1
    elif ! command -v docker &> /dev/null; then # Check if Docker is installed
        echo "Error: Docker is not installed. Please install Docker before running this script."
        exit 1
    elif ! command -v npm &> /dev/null; then # Check if npm is installed
        echo "Error: npm is not installed. Please install npm before running this script."
        exit 1
    elif ! command -v java &> /dev/null; then # Check if Java is installed
        echo "Error: Java is not installed. Please install Java before running this script."
        exit 1
    fi
}

function startup() {
    check_prerequisites
    pushd smart-contracts
    echo "Upping network and deploying smart contracts..."
    ./gradlew upAndDeploy
    popd
    pushd api-service
    echo "Upping API service..."
    ./startup.sh up
    popd
    pushd frontend
    echo "Frontend setup..."
    docker compose up -d --build
    popd
}

function shutdown() {
    echo "Stopping all services..."
    pushd smart-contracts
    ./gradlew downNetwork
    popd
    pushd api-service
    ./startup.sh down
    popd
    pushd frontend
    docker compose down
    popd
}

function clean() {
    pushd smart-contracts
    ./gradlew downNetworkAndClean
    popd
}

if [[ "$1" == "up" ]]; then
    startup
elif [[ "$1" == "down" ]]; then
    shutdown
elif [[ "$1" == "clean" ]]; then
    clean
else
    print_help
fi
