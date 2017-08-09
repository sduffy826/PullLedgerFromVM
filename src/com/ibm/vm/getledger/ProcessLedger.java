package com.ibm.vm.getledger;

/** This class is the mainline code to handle the logic to process the revlw_data table, that
 * table is populated on vm and pulled to a file by this class so that it can be sent to
 * SAP for processing.  This was developed to remove the dependency on ftp.
 * 
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.ibm.vm.general.*;

public class ProcessLedger {

  private Connection dbConnection = null;
  private Table_Status tableStatusObj = null;
  private Log_Ping pingObj = null;
  private String fileOutName = null;
  private final String className;
  private boolean purgeOnSuccess;
  private static final String inUseBy = "PC_LEDGER";
  private boolean forceTrace;
  
  static {
    try {
      // register the db2 jdbc driver with DriverManager
      Class.forName("COM.ibm.db2.jdbc.app.DB2Driver").newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * ProcessLedger constructor, can only be instantiated locally
   */
  private ProcessLedger() {
    super();
    className = this.getClass().getName();
  }

  /**
   * Starts the application.
   * 
   * @param args
   *          - an array of command-line arguments
   */
  public static void main(java.lang.String[] args) {
    // Instantiate object and call run method
    try {
      ProcessLedger app = new ProcessLedger();
      app.run(args);
    } catch (Exception e) {
      if (e instanceof SQLException)
        SQLExceptionHelper.dumpInfo((SQLException) e, "ProcessLedger", "main");

      Logger.log.severe("Error raised in mainline");
      e.printStackTrace();
       
      System.exit(-1);
    }
  }

  // Process the command line arguments (validate them) and initialize required
  // variables
  // database connection is setup here
  private boolean processArgs(String[] args) throws SQLException, Exception {
    final String methodName = "processArgs(String[] args)";
    boolean returnValue = false;
    
    if ((args.length < 4) || (args.length > 6))
      Logger.log.severe("(" + className + "." + methodName + ") Invalid arguments, must pass " + 
                         "urlToDatabase(path:port/db) userid passord outputFile [purgeFlag(Y/N)] [traceFlag(Y/N)]");     
    else {
      String pathNPort;
      String uid;
      String pwd;

      pathNPort = args[0];
      uid = args[1];
      pwd = args[2];
      fileOutName = args[3];

      // Fifth argument is optional, if Y then user wants the revlw_data table
      // purged
      // after we write the output.
      purgeOnSuccess = false; // Default it off
      if (args.length >= 5) {
        // If passed fifth arg then see if Y, if so then we'll purge
        purgeOnSuccess = args[4].equalsIgnoreCase("Y");
      }

      // Sixth argument is optional, Y means turn trace flag on (more logging detail) 
      forceTrace = false;
      if (args.length == 6) {
        forceTrace = args[5].equalsIgnoreCase("Y");
      }
      
      // Turn on tracing if applicable
      if (forceTrace) {
        Logger.logUtils.setLogLevel(Logger.log, java.util.logging.Level.FINE);       
        Logger.log.fine("(" + className + "." + methodName + ") Tracing forced ON by program argument");
      }
      
      dbConnection = DriverManager.getConnection("jdbc:db2:" + pathNPort, uid, pwd);
      
      Logger.log.fine("(" + className + "." + methodName + ") Connected to " + pathNPort + " using userid " + uid);
      returnValue = true;
    }
    
    // When tracing is on we want to see classpath and libpath
    Logger.log.fine("(" + className + "." + methodName + ") Classpath: " + System.getProperty("java.class.path"));
    Logger.log.fine("(" + className + "." + methodName + ") Library path: " + System.getProperty("java.library.path"));    
    
    Logger.log.fine("(" + className + "." + methodName + ") Leaving method");

    return returnValue;
  }

  /**
   * Run the mainline of the application
   */
  private void run(String[] args) throws Exception {
    final String methodName = "run(String[] args)";
    
    if (processArgs(args)) {
      // Get object to handle the table_status
      tableStatusObj = new Table_Status(dbConnection);
      pingObj = new Log_Ping(dbConnection);
      pingObj.insertPing(inUseBy, null);
      
      // Was here just while testing, uncomment if want to see table attributes
      // tableStatusObj.showMetaData();

      // Get attributes related to the REVLW_DATA table
      Table_Status_Attributes tableStatusAttributes = tableStatusObj.getAttributes("REVLW_DATA");

      // If tracing then write out info      
      Logger.log.fine("(" + className + "." + methodName + ") TableStatusAttributes: " + 
        tableStatusAttributes.toString());

      // See if in use
      if (tableStatusAttributes.getInUse().trim().length() == 0) {
        // Not in use see if there's data in the table, we first instantiate the
        // object to handle the revlw_data table.
        Revlw_Data revLwObj = new Revlw_Data(dbConnection);
        if (revLwObj.getNumRecords() > 0) {
          // Has data, mark the table in use
          tableStatusObj.markInUse("REVLW_DATA", inUseBy);
            
          // Write the table contents to a file
          revLwObj.writeToFile(fileOutName);
          Logger.log.info("(" + className + "." + methodName + ") Wrote ledger file: " + fileOutName);

          if (purgeOnSuccess) {
            revLwObj.purgeTable();  // Returns number purged but we don't care about here
            Logger.log.fine("(" + className + "." + methodName + ") Purged revlw data");
          }
          else {
            Logger.log.fine("(" + className + "." + methodName + ") Did not purge revlw data");
          }
            
          tableStatusObj.clearInUse("REVLW_DATA", inUseBy);           
        } else {
          // If tracing then write out info
          Logger.log.fine("(" + className + "." + methodName + ") No data in revlw to process");          
        }
      } else {
        Logger.log.info("(" + className + "." + methodName + ") Table in use... ignore this run");
      }        
    }
  }
}