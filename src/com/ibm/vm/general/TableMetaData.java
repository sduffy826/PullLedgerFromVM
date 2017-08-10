package com.ibm.vm.general;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;

public class TableMetaData {
  private static final String className = "ShowMetaData";
  
  public synchronized static void show(Connection _dbConn, String _tableName) throws Exception {
    String query = "SELECT * FROM " + _tableName;
    PreparedStatement stmt = null;
    final String methodName = "showMetaData()";
    
    stmt = _dbConn.prepareStatement(query);
    
    // If debugging then this will display
    Logger.log.fine("(" + className + "." + methodName + ") Before prepareStatement");

    // execute the query 
    ResultSet rs = stmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int numberOfColumns = rsmd.getColumnCount();

    Logger.log.info("(" + className + "." + methodName + ") getSchemaName: " + rsmd.getSchemaName(1));
    Logger.log.info("(" + className + "." + methodName + ") getTableName: " + rsmd.getTableName(1));
    
    for (int i = 1; i <= numberOfColumns; i++)
    {
      Logger.log.info("(" + className + "." + methodName + ") getColumnLabel: " + rsmd.getColumnLabel(i));
      Logger.log.info("(" + className + "." + methodName + ") getColumnName: " + rsmd.getColumnName(i));
      Logger.log.info("(" + className + "." + methodName + ") getColumnType: " + rsmd.getColumnType(i));
      Logger.log.info("(" + className + "." + methodName + ") getColumnTypeName: " + rsmd.getColumnTypeName(i));
      Logger.log.info("(" + className + "." + methodName + ") getPrecision: " + rsmd.getPrecision(i));
      Logger.log.info("(" + className + "." + methodName + ") getScale: " + rsmd.getScale(i));
      Logger.log.info(" ");
    }
    rs.close();
    stmt.close();
    Logger.log.fine("(" + className + "." + methodName + ") Leaving method");
        
    return;
  }
}
