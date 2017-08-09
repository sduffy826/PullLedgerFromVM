#!/bin/bash
usage()
{
   echo "Script usage options.."
   echo "1) #Run a datastage job#"
   echo "   Usage: $0 -start script_properties_file"
   echo "2) #Stop currently running datastage job#"
   echo "   Usage: $0 -stop script_properties_file"
   echo "3) #Retrieve log details of latest job#"
   echo "   Usage: $0 -lognewest script_properties_file"
   echo
}

checkfolder()
{
    # If folder does not exist or is not writable, print an error message and exit with given status
    exitstatus=$1
    folder=$2
    if [ ! -d "$folder" ]; then
      echo "ERROR: $folder cannot be found"
      exit $exitstatus
    elif [ ! -w "$folder" ]; then
      echo "ERROR: $folder does not have write permission"
      exit $exitstatus
    fi
}

sendmail()
{
    # If email id is not blank then send mail content to the recepient address
    email_id=$1
    subject=$2
    mailfile=$3
    if [ -n "$email_id" ]; then
        echo "Sending mail to $email_id"
        mail -s "$subject" "$email_id" < "$mailfile"
    fi
}

if [ $# -lt 2 ]; then
    usage
    exit 1
fi

action=$1
propertiesfile=`readlink -f "$2"`          # Absolute path to this file

if [ ! -f "$propertiesfile" ]; then
    usage
    exit 1
fi
source $propertiesfile

if [ ! -f "$dsengine_env" ]; then
    echo "The dsenv profile cannot be found. Please assign the correct path to 'dsengine_env' parameter in $propertiesfile file"
    exit 1
fi

if [ ! -n "$datastageproj" ]; then
    echo "Please assign the datastage project name to 'datastageproj' parameter in $propertiesfile file"
    exit 1
fi

if [ ! -n "$datastagejob" ]; then
    echo "Please assign the datastage job name to 'datastagejob' parameter in $propertiesfile file"
    exit 1
fi

if [ ! -n "$outputfolder" ]; then
    echo "Please specify folder name in 'outputfolder' parameter in $propertiesfile file for logging job run information"
    exit 1
fi

mkdir -p "$outputfolder"
checkfolder 1 $outputfolder

mailfile="$outputfolder/dstagemail"
logfilename="dslastrun_`date "+%Y%m%d-%H.%M.%S"`.log"
allparam=""

source $dsengine_env

if [ "$action" = "-start" ]; then
    # Get all the parameters into allparam
    for (( i=1; i<=$paramcount; i++ ))
    do
       param=$(eval echo $(eval echo '\$\{param$i\}'))
       valueforparam=$(eval echo $(eval echo '\$\{valueforparam$i\}'))
       if [ ! -n "$param" ] || [ ! -n "$valueforparam" ]; then
	    echo "Please make sure all parameters from param1 to param$paramcount and valueforparam1 to valueforparam$paramcount \
are assigned proper values in $propertiesfile file"
          exit 1
       fi
       allparam="$allparam -param $param=$valueforparam "
    done
elif [ "$action" = "-lognewest" ]; then
    echo "Log details of latest job will be written to $outputfolder/$logfilename" 
    $DSHOME/bin/dsjob -logdetail $datastageproj $datastagejob -wave 0 1>"$outputfolder/$logfilename" 2>&1
    
    sendmail "$email" "Log details of latest job (PROJECT:$datastageproj JOB:$datastagejob)" "$outputfolder/$logfilename"
    exit
elif [ ! "$action" = "-stop" ]; then
    echo "Error: invalid option '$action'";echo
    usage
    exit 1
fi

#Job Status      : RUNNING (0) ;* This is the only status that means the job is actually running
#Job Status      : RUN OK (1) ;* Job finished a normal run with no warnings
#Job Status      : RUN with WARNINGS (2) ;* Job finished a normal run with warnings
#Job Status      : RUN FAILED (3) ;* Job finished a normal run with a fatal error

echo "Checking status of previous execution for job $datastagejob" | tee "$outputfolder/$logfilename"
jresult=`$DSHOME/bin/dsjob -jobinfo $datastageproj $datastagejob 2>>"$outputfolder/$logfilename"` 

# If project name or job name is invalid then dsjob command will fail
if [ $? -ne 0 ]; then
    echo "Unable to get job information for job $datastagejob" 
    echo "Please check $outputfolder/$logfilename for more information." > "$mailfile"
    sendmail "$email" "Unable to get job information" "$mailfile"
    exit
fi

# dsjob command was successfull and we can now extract Job Status from jresult
jstatus=`echo $jresult | head -1 | cut -d"(" -f2 | cut -d")" -f1`
if [ "$jstatus" = "0" ]; then
    if [ "$action" = "-stop" ]; then
       echo "Stopping job $datastagejob.." >> "$outputfolder/$logfilename"
       # Please note that only a stop job command without wait option is issued and so the control returns immediately
       $DSHOME/bin/dsjob -stop $datastageproj $datastagejob 1>>"$outputfolder/$logfilename" 2>&1
    else   
       echo "The job $datastagejob is already running. Please wait until it completes" 
    fi
    echo "exiting"
    exit
elif [ "$action" = "-stop" ]; then
    echo "The $datastagejob job is not running" | tee -a "$outputfolder/$logfilename"
    exit
elif [ "$jstatus" = "3" ]; then
    echo "Resetting job $datastagejob.." >> "$outputfolder/$logfilename"
    $DSHOME/bin/dsjob -run -mode RESET -wait $datastageproj $datastagejob 1>>"$outputfolder/$logfilename" 2>&1
fi

echo "Starting job $datastagejob.." | tee -a "$outputfolder/$logfilename"
$DSHOME/bin/dsjob -run -mode NORMAL -warn 0 -jobstatus $allparam $datastageproj $datastagejob 1>>"$outputfolder/$logfilename" 2>&1
return=$?

if [ "$return" = "1" ] || [ "$return" = "2" ]; then
    echo "Job finished successfully" | tee -a "$outputfolder/$logfilename"
    echo "Please check $outputfolder/$logfilename for more information." | tee "$mailfile"
    
    # if there are warnings, you may want to look at the job log
    if [ "$return" = "2" ]; then
       echo "The job $datastagejob completed successfully but with WARNINGS!!." | tee -a "$mailfile"
       echo "-------------------log start-------------------------------------------------">> "$outputfolder/$logfilename"
       $DSHOME/bin/dsjob -logdetail $datastageproj $datastagejob -wave 0 1>>"$outputfolder/$logfilename" 2>&1
    fi
    sendmail "$email" "Job $datastagejob completed successfully" "$mailfile"
else
    echo "Job $datastagejob failed" | tee -a "$outputfolder/$logfilename"
    echo "Please check log for details." 
    
    # if job ran normally but failed due to processing issues, you may want to look at the job log
    if [ "$return" = "3" ]; then
       echo "-------------------log start-------------------------------------------------">> "$outputfolder/$logfilename"
       $DSHOME/bin/dsjob -logdetail $datastageproj $datastagejob -wave 0 1>>"$outputfolder/$logfilename" 2>&1
    fi
    sendmail "$email" "Job $datastagejob failed" "$outputfolder/$logfilename"
fi
