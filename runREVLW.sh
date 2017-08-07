#!/bin/bash

# This shell script runs the job to create the ledger data file for
# sap.  If the file is created it'll copy the file to sap

# invoke the db2profile to setup environment
. /home/db2inst1/sqllib/db2profile

revFile="revlw.txt"
exitCode=0
if [[ $# -eq 6 ]]; then
  # Args: url:port/db, userid, pw, purgeFlag, sapDestination, sapFileName(noExtension)"
  dbURL=$1 
  uid=$2
  pw=$3
  purgeAfter=$4
  targetPath=$5
  targetFN=$6

  # Erase output file if existing
  if [[ -f $revFile ]]; then
    rm $revFile
  fi
  
  # Run the job pass in the database url, userid, password, output file, purge indicator
  java -cp ./processVMLedger.jar com.ibm.vm.getledger.ProcessLedger $dbURL $uid $pw $revFile $purgeAfter
  exitCode=$?
  if [[ $exitCode -ne 0 ]]; then
    echo "java program returned invalid rc of: $exitCode"
  else
    # Program ran successfully
    if [[ -f $revFile ]]; then
      # Set permissions
      chmod 660 $revFile

      # Output file was created, copy the file
      echo "scp $revFile $targetPath/$targetFN.dat"
    
      # Dumb to me but control file just has the data file name in it
      echo "$targetFN.dat" > $targetFN.ctl
      chmod 660 $targetFN.ctl

      # Copy data file to target
      scp -p $revFile $targetPath/$targetFN.dat
      exitCode=$?
      if [[ $exitCode -eq 0 ]]; then
        # Copy the control file to target
        echo "scp $targetFN.ctl $targetPath/$targetFN.ctl"
        scp -p $targetFN.ctl $targetPath/$targetFN.ctl
        exitCode=$?
      fi
      if [[ $exitCode -eq 0 ]]; then
        echo "Successfully copies data and ctl file to sap"
      else
        echo "Error during copyfile, rc: $exitCode"
      fi
    fi
  fi
else
  echo "Must pass hostURL:Port/DB userid password purgeFlag sapDestination sapFileName(no extension)"
  exitCode=99
fi
exit "$exitCode"
