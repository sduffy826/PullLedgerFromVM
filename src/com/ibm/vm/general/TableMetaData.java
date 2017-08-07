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

    // execute the query 
    ResultSet rs = stmt.executeQuery();
    ResultSetMetaData rsmd = rs.getMetaData();
    int numberOfColumns = rsmd.getColumnCount();

    System.out.println("getSchemaName: " + rsmd.getSchemaName(1));
    System.out.println("getTableName: " + rsmd.getTableName(1));
    System.out.println("");

    for (int i = 1; i <= numberOfColumns; i++)
    {
      System.out.println("getColumnLabel: " + rsmd.getColumnLabel(i));
      System.out.println("getColumnName: " + rsmd.getColumnName(i));
      System.out.println("getColumnType: " + rsmd.getColumnType(i));
      System.out.println("getColumnTypeName: " + rsmd.getColumnTypeName(i));
      System.out.println("getPrecision: " + rsmd.getPrecision(i));
      System.out.println("getScale: " + rsmd.getScale(i));
      System.out.println("");     
    }
    rs.close();
    stmt.close();
        
    return;
  }
}
