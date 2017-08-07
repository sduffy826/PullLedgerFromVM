package com.ibm.vm.getledger;

/**
 * This class handles the actions related to the revlw_data table... this table is populate with
 * the ledger data once the use on VM is done with processing... the file used to be ftp'd to sap
 * but due to security restrictions we had to stop using ftp.. I wrote java programs to pull the
 * table down instead, we'll then use scp to get it into sap.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.Vector;

import com.ibm.vm.general.Log;
import com.ibm.vm.general.SQLExceptionHelper;
import com.ibm.vm.general.TableMetaData;

public class Revlw_Data implements com.ibm.logging.IRecordType 
{
  Connection dbConn = null;
  public static final String TABLENAME = "BRSGUIDE.REVLW_DATA";
  private final String className;
  
  protected Revlw_Data() {
    super();
    className = this.getClass().getName();
  }

  // Constructor, we pass is the database connection that's been established
  public Revlw_Data(Connection _target) {
    super();
    dbConn = _target;	
    className = this.getClass().getName();
  }
  
  /**
   * Returns the number of records in the REVLW_DATA table
   * @return - int representing number of records in the table
   * @throws Exception - we want this, caller needs to handle proper cleanup/shutdown
   */
  public int getNumRecords() throws Exception {
    PreparedStatement stmt = null;
    String query = "SELECT COUNT(*) FROM " + TABLENAME;
    int numRecs = 0;
    
    // set constants for use in logger/trace calls  
    final String methodName = "numRecords()";
    
    // trace entry to method
    if (Log.trace.isLogging) {
        Log.trace.entry(TYPE_LEVEL2, className, methodName);        
    }
         
    stmt = dbConn.prepareStatement(query);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      numRecs = rs.getInt(1);
    }
    rs.close();  // Close result set
    stmt.close();
    
    // trace msg/exit
    if (Log.trace.isLogging)
    {
       Log.trace.text(TYPE_LEVEL2, className, methodName,
                      "Number of records: " + Integer.toString(numRecs));
       Log.trace.exit(TYPE_LEVEL2, className, methodName);
    }
    
    return numRecs;
  }
    
  /**
   * Populates a string vector with all the records from the revlw_data table
   * @return Vector<String> of all the records
   * @throws Exception
   */
  public Vector<String> getRecords() throws Exception {
    PreparedStatement stmt = null;
    String query = "SELECT RECORD FROM " + TABLENAME;
    
    Vector<String> vectorOfData = new Vector<String>();
    int numRecs = 0;
    
    // set constants for use in logger/trace calls  
    final String methodName = "getRecords()";
    
    // trace entry to method
    if (Log.trace.isLogging) {
        Log.trace.entry(TYPE_LEVEL2, className, methodName);        
    }
           
    stmt = dbConn.prepareStatement(query);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
      vectorOfData.add(rs.getString(1));
      numRecs++;
    }
    rs.close();
    stmt.close();
    
    if (Log.trace.isLogging)
    {
       Log.trace.text(TYPE_LEVEL2, className, methodName,
                      "Records pulled: " + vectorOfData.size());
       Log.trace.exit(TYPE_LEVEL2, className, methodName);
    }
    
    return vectorOfData;
  }
 
  /**
   * Purge all the records from the revlw_data table
   * @return - int representing number of records deleted
   * @throws Exception
   */
  public int purgeTable() throws Exception {
    PreparedStatement stmt = null;
    int rowsDeleted = 0;
    String delCmd = "DELETE FROM " + TABLENAME;
    
    // set constants for use in logger/trace calls  
    final String methodName = "purgeTable()";
    
    // trace entry to method
    if (Log.trace.isLogging) {
        Log.trace.entry(TYPE_LEVEL2, className, methodName);        
    }
           
    stmt = dbConn.prepareStatement(delCmd);
    rowsDeleted = stmt.executeUpdate();      
    stmt.close();
    
    Log.logger.message(TYPE_INFO,className,methodName,"LITERAL",
        "Purged " + Integer.toString(rowsDeleted) + " from REVLW_DATA table");
    
    if (Log.trace.isLogging)
    {     
       Log.trace.exit(TYPE_LEVEL2, className, methodName);
    }
    
    return rowsDeleted;
  }
    
  /**
   * Show the meta data for the revlw_data table
   */
  public void showMetaData() throws Exception {    
    TableMetaData.show(dbConn, TABLENAME);
  }
  
  /**
   * Write the contents of the table to the filename passed in, if the file
   *   exists then it will be purged first.
   * @param _fileName
   * @return boolean true if successful
   */
  public void writeToFile(String _fileName) throws Exception {
    Vector<String> theRecords = this.getRecords();

    // See if output file exists, if so erase it
    File file = new File(_fileName);
    if (file.exists()) {
      file.delete();
    }

    // Create writers and output each record
    FileWriter fw = new FileWriter(file, false);
    BufferedWriter out = new BufferedWriter(fw);
    for (int i = 0; i < theRecords.size(); i++) {
      out.write((String) theRecords.elementAt(i));
      out.newLine();
    }
    out.close();
    return;
  }
}
