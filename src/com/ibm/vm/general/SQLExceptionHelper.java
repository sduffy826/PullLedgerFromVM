package com.ibm.vm.general;

import java.sql.SQLException;
import com.ibm.vm.general.Logger;

public class SQLExceptionHelper {
  public synchronized static void dumpInfo(SQLException e, String _className, String _methodName) {
    if (e != null) {
      Logger.log.severe("Message: " + e.getMessage());
      Logger.log.severe("SQLState: " + e.getSQLState());
      Logger.log.severe("ErrorCode: " + e.getErrorCode());
      e.printStackTrace();  // Dump to console too
    }
  }

}
