package com.ibm.vm.general;

/**
 * Provides access to logging and tracing objects
 * for use by all classes in this package.
 */
import com.ibm.logging.*;
import com.ibm.bcrs.logging.*;
// import com.ibm.logging.mgr.*;

public class Log implements IRecordType {
	public final static String pkg = "com.ibm.vm.general";
	public final static MessageLogger logger =
		(MessageLogger) BCRSLogManager.getManager().getMessageLogger(
			pkg + ".MessageLogger");
	public final static MessageCatalog catalog =
		logger == null ? null : new MessageCatalog(logger.getMessageFile());
	public final static TraceLogger trace =
		(TraceLogger) BCRSLogManager.getManager().getTraceLogger(
			pkg + ".TraceLogger");
/**
 * Creation date: (12/11/00 1:30:45 PM)
 */
protected Log() {}
}
