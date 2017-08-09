#!/bin/bash
# startTime="080000"
# endTime="170000"
# sleepMins=1
if [ "$#" -ne 3 ]; then
  echo "Must pass startTime (HHMMSS), endTime and sleepMins(NN)"
  exit 97
fi

startTime=$1
endTime=$2
sleepMins=$3

# Check that the length of start/end is 6 bytes (hhmmss)
if [[ ( ${#startTime} -ne 6 ) || ( ${#endTime} -ne 6 ) ]]; then
  echo "Invalid arguments, start: $startTime end: $endTime sleep: $sleepMins"
  exit 98
else
  if [[  $startTime > $endTime ]]; then
    echo "Start time cannot exceed end time"
    exit 90
  else
    if [[ ( $sleepMins -le 0 ) || ( $sleepMins -gt 120 ) ]]; then
      echo "Sleep mins must be between 1 and 120"
      exit 99
    fi
  fi
fi

# Loop while we're within start/end interval, sleep for minutes specified 
currentTime=`date +"%H%M%S"`
while [[ ! ( $currentTime < $startTime || $currentTime > $endTime ) ]];
do
  echo $currentTime
  sleep "$sleepMins"m
  currentTime=`date +"%H%M%S"`
done
exitRC=$?
echo "Return code: $exitRC"
