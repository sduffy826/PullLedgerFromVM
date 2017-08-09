package com.ibm.vm.getledger;

/**
 * This class handles the actions related to the table_status table... this table is used to identify
 * which application/process is processing a table... thought being that while one system is populating
 * a table it marks it as in_use that way other apps don't try to process it.  Basically an app checks
 * if it's in use, if not then it will mark it as in use... if it was in use then app should sleep for
 * a period and try again.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ibm.vm.general.*;

public class Table_Status
{
  Connection dbConn = null;
  private static final String TABLESTATUSNAME = "BRSGUIDE.TABLE_STATUS";
  private final String className;

  protected Table_Status() {
    super();
    className = this.getClass().getName();
  }
  
  /**
   * Public constructor, requires database connection 
   * @param _target
   */
  Table_Status(Connection _target) {
    super();
    dbConn = _target;
    className = this.getClass().getName();
  }
  
  /**
   * Get the table attributes (in_use, in_use_by) for a given table name (passed in),
   *   database connection (dbConn) should already be set prior to this call
   * @param _tableName - The table we want to get the attributes for (type String)
   * Creation date: (8/10/2016 7:29:24 PM)
   */
  public Table_Status_Attributes getAttributes(String _tableName) throws Exception {
    Table_Status_Attributes tableStatusAttributes = null;
    PreparedStatement stmt = null;
    String query = "SELECT IN_USE, IN_USE_BY " +
                   "FROM " + TABLESTATUSNAME + " " +
                   "WHERE TABLE_NAME = ?";

    // set constants for use in logger/trace calls  
    final String methodName = "getAttributes(String _tableName)";
    
    // If tracing then write out info
    Logger.log.fine("(" + className + "." + methodName + ") Table to get status for: " + _tableName);    
    
    // Prepare statement and set argument (table_name) for query
    stmt = dbConn.prepareStatement(query);
    stmt.setString(1, _tableName);
      
    // Execute query and get results
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      tableStatusAttributes = new Table_Status_Attributes(rs.getString(1),   // In_Use
                                                          rs.getString(2));  // In_Use_ By
    }
    rs.close();  // Close result set
    stmt.close();
    
    // For debugging dump attributes
    Logger.log.fine("(" + className + "." + methodName + ") tableStatusAttributes: " + tableStatusAttributes);
    
    return tableStatusAttributes;
  }
  
  /**
   * Set the in_use and in_use by values in brsguide.table_status for the
   *   args passed in... we use this to say that the table is in use
   *   by a particular process... other jobs can then interrogate this
   *   and ensure there's no contention on the table.
   * @param _tableName - The table we want to mark in use (type String)
   * @param _useBy - A string representing who's using the table
   * @return - boolean, true if successful
   * @throws Exception
   */
  public synchronized boolean markInUse(String _tableName, String _useBy) throws Exception {
    return this.updateInUse(_tableName, "Y", _useBy, " ", " ");
  }
  
  /**
   * Clear the in_use and the in_use_by values in brsguide.table_status for the given
   *   table name passed in.  This is really a wrapper for the method that does the 
   *   real work.
   * @param _tableName - The table we want to clear the in use indicator (type String)
   * @param _useBy - A string representing who was using the table, we only clear the flag
   *                 if was in use by this person
   * @return - boolean, true if successful
   * @throws Exception
   */
  public synchronized boolean clearInUse(String _tableName, String _wasUsedBy) throws Exception {
    return this.updateInUse(_tableName, " ", " ", "Y", _wasUsedBy);
  }
  
  /**
   * This method handles setting the in_use and in_use_by values for a given table, it is a private 
   * method as it's intented to be called by mark/clear methods 
   * @param _tableName - String representing table to process
   * @param _inUse - The in_use value to set
   * @param _inUseBy - The in_use_by value to use
   * @param _oldInUse - The old 'in_use' by value, when locking this is typically ' ', when clearing
   *                    this is usually 'Y'.
   * @param _oldInUseBy - The old 'in_use_by' value
   * @return - boolean indicating whether successful
   * @throws Exception
   */
  private boolean updateInUse(String _tableName, String _inUse, String _inUseBy,
                                                 String _oldInUse, String _oldInUseBy) throws Exception {
    
    PreparedStatement stmt = null;
    int rowsAffected = 0;
    
    String updStmt = "UPDATE " + TABLESTATUSNAME + " SET IN_USE = ?, IN_USE_BY = ? " + 
                       "WHERE TABLE_NAME = ? AND IN_USE = ? AND IN_USE_BY = ?";
            
    // set constants for use in logger/trace calls  
    final String methodName = "updateInUse(....)";
    
    // If tracing then write out info
    Logger.log.fine("(" + className + "." + methodName + ") Table to set InUse for: " + _tableName);
    
    // Prepare statement and set arguments
    stmt = dbConn.prepareStatement(updStmt);
    stmt.setString(1, _inUse);
    stmt.setString(2, _inUseBy);
    stmt.setString(3, _tableName);
    stmt.setString(4, _oldInUse);
    stmt.setString(5, _oldInUseBy);
      
    rowsAffected = stmt.executeUpdate();
    stmt.close();        
    
    // If tracing the message that we're leaving
    Logger.log.fine("(" + className + "." + methodName + ") SetInUse for table:" + _tableName + 
                    " to: " + _inUse + " by: " + _inUseBy);
    
    return (rowsAffected > 0);
  }
 
  /**
   * Helper method to call the static one to show meta data
   */
  public void showMetaData() throws Exception {    
    TableMetaData.show(dbConn, TABLESTATUSNAME);
  }
}