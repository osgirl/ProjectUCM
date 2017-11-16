/*    */ package intradoc.server.utils;
/*    */ 
/*    */ public class MessageLoggerUtils
/*    */ {
/* 24 */   public static String m_loggerNamePrefix = "oracle.ucm";
/*    */ 
/* 29 */   public static String m_loggerName = "oracle.ucm.idccs";
/*    */ 
/*    */   public static String calculateLoggerMessageKey(String name, String prefix, String number)
/*    */   {
/* 41 */     return prefix + "-" + number;
/*    */   }
/*    */ 
/*    */   public static String getHardWiredGeneralExceptionKey()
/*    */   {
/* 50 */     return calculateLoggerMessageKey("syGeneralException", "UCM-CS", "000001");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.MessageLoggerUtils
 * JD-Core Version:    0.5.4
 */