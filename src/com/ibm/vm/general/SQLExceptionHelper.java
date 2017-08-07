package com.ibm.vm.general;

import java.sql.SQLException;
import com.ibm.vm.general.Log;

public class SQLExceptionHelper implements com.ibm.logging.IRecordType {
  public synchronized static void dumpInfo(SQLException e, String _className, String _methodName) {
    if (e != null) {
      Log.logger.message(TYPE_ERROR,_className,_methodName,"LITERAL",
        "Message: " + e.getMessage());
      Log.logger.message(TYPE_ERROR,_className,_methodName,"LITERAL",
          "SQLState: " + e.getSQLState());
      Log.logger.message(TYPE_ERROR,_className,_methodName,"LITERAL",
          "ErrorCode: " + e.getErrorCode());
      e.printStackTrace();  // Dump to console too
    }
  }

}
