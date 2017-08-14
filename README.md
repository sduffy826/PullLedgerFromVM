Ledger Processing
=================
We needed to check the ledger data (existance of) frequently.  I 
scheduled the check outside of infosphere/director.  You can still see
(and run) the job from director but scheduling is done thru cron.

Shell 'runProdFromCron.sh' kicks off the exec 'execute_dstagejob.sh'
with parms of '-start' and properties file of 'revlw_ip0.properties'.
I used this shell to make it easier... in cron just schedule this exec,
if you need to change properties/parms then change the exec.

Note I'd setup cron with following:
00,15,30,45 9-18 * * 1-5 /opt/IBM/InformationServer/Server/Projects/live/runProdFromCron.sh

This says to run the exec Monday thru Friday (1-5), between hours of 9am-6pm (9-18) and every 15 minutes (at: 00,15,30,45)

The runProdFromCron shell uses property file revlw_ip0.properties, that file
has all of the parameters for the job.  You'll need to update this file when
the password on VM changes... should be obvious (do the change with vi, if
need help ask :))

**Note:** under the sandbox-media3 folder on ralbz001027 are property files for the
other systems, if you want to test make the appropriate change there... I only
tested from that folder since didn't make sense to schedule a cron job from
any other repository.

**Debugging:**  If you have a problem and want to do debugging then run the job from within datastage 
(job sj_REVJOB), when you run it enter 'Y' for Debug Flag, then in the output pane double click on the line 
'Executed ./runREV...', that will show you detailed information.  If you want to test against the
dev vm system (stf...), port is 3070 and db is SQLDEV, use your id/pw to connect (only place that's in
the parms is on your dev repository).
