package com.ibm.vm.getledger;

/**
 * This class is for logging when the pc application runs; I log this in a table on the host,
 * did this so that the host could ensure that the pc application is running on a scheduled
 * basis.  If the host doesn't see activity then it'll send a notice to the developer.
 */
import java.sql.*;
import com.ibm.vm.general.*;

public class Log_Ping 
{
  Connection dbConn = null;
  private static final String TABLESTATUSNAME = "BRSGUIDE.PING_STATUS";
  private final String className;

  protected Log_Ping() {
    super();
    className = this.getClass().getName();
  }
  
  /**
   * Public constructor, requires database connection 
   * @param _target (database connection)
   */
  Log_Ping(Connection _target) {
    super();
    dbConn = _target;
    className = this.getClass().getName();
  }
  
  /**
   * Utility method to return the current timestamp.  Note coulda made with less
   *   instructions but did it this way to make debugging easier :)
   */
  public java.sql.Timestamp getCurrentTimestamp() {
    // Get calendar object (uses users timezone/locale)
    java.util.Calendar calendar = java.util.Calendar.getInstance();

    // 2) get a java.util.Date from the calendar instance.
    //    this date will represent the current instant, or "now".
    java.util.Date now = calendar.getTime();
        
    // 3) a java current time (now) instance
    java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(now.getTime());
    return currentTimestamp;
  } 
  
  /** 
   * Insert a record into the ping_status table, you need to pass the in_use by
   *   string, if you don't pass the timestamp (giving it null) then this routine
   *   will get the current timestamp.
   * @param _inUse - String representing who's running (i.e. PC_LEDGER)
   * @param _ts - Timestamp, pass null if want this routine to get it
   * @return - Boolean true if successful
   * @throws Exception
   */
  public boolean insertPing(String _inUse, Timestamp _ts) throws Exception {
    
    PreparedStatement stmt = null;
    int rowsAffected = 0;
    
    if (_ts == null) _ts = getCurrentTimestamp();
    
    String insStmt = "INSERT INTO " + TABLESTATUSNAME + " VALUES(?, ?)";
            
    // set constants for use in logger/trace calls  
    final String methodName = "insertPing(in_use, timestamp)";
    
    // If tracing then write out info
    Logger.log.fine("(" + className + "." + methodName + ") Before prepareStatement");
    Logger.log.fine("(" + className + "." + methodName + ") InUse: " + _inUse + " Timestamp: " + _ts.toString());
    
    // Prepare statement and set arguments
    stmt = dbConn.prepareStatement(insStmt);
    stmt.setString(1, _inUse);
    stmt.setTimestamp(2, _ts);
    rowsAffected = stmt.executeUpdate();
    stmt.close();        
    
    Logger.log.fine("(" + className + "." + methodName + ") Done rowsAffected: " + rowsAffected);
    
    return (rowsAffected > 0);
  }
 
  /**
   * Helper method to call the static one to show meta data
   */
  public void showMetaData() throws Exception {    
    TableMetaData.show(dbConn, TABLESTATUSNAME);
  }
}