package com.ibm.vm.general;

import com.corti.javalogger.*;

public class Logger {
  
  public static final LoggerUtils logUtils = new com.corti.javalogger.LoggerUtils();
  
  public static final java.util.logging.Logger log = logUtils.getLogger("PullLedgerLogger", "RevLW.log");
    
  protected Logger() { }
  
  public static void traceOn() {
    // Set log level to fine for tracing
    logUtils.setLogLevel(log,java.util.logging.Level.FINE);
  }
  
}
