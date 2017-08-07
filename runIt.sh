#!/bin/bash
bash runREVLW.sh //<hostipaddress:PORT/databaseName> <connectUserid> <password> N <scpId@sapHost:./156> <fileName>
exitRC=$?
echo "Return code: $exitRC"
