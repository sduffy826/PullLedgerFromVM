#!/bin/bash

# Go to project directory
cd /opt/IBM/InformationServer/Server/Projects/sandbox-media3

# Execute script and specify properties file to use
bash ./execute_dstagejob.sh -start revlw_ii0.properties
exitRC=$?
echo "Return code: $exitRC"
