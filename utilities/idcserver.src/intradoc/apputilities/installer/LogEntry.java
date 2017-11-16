/*    */ package intradoc.apputilities.installer;
/*    */ 
/*    */ class LogEntry
/*    */ {
/*    */   public int m_severity;
/*    */   public String m_message;
/*    */ 
/*    */   public LogEntry(int severity, String message)
/*    */   {
/* 30 */     this.m_severity = severity;
/* 31 */     this.m_message = message;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.LogEntry
 * JD-Core Version:    0.5.4
 */