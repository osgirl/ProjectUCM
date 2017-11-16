/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ 
/*    */ public class ProgressStateUtils
/*    */ {
/*    */   public static void traceProgress(ProgressState pState, String section, String msg, Throwable t)
/*    */   {
/* 27 */     Report.trace(section, msg, t);
/* 28 */     pState.reportProgress(3, msg, t);
/*    */   }
/*    */ 
/*    */   public static void reportWarning(ProgressState pState, String section, Throwable t, String key, Object[] args)
/*    */   {
/* 34 */     Report.warning(section, t, key, args);
/* 35 */     pState.reportProgress(3, t, key, args);
/*    */   }
/*    */ 
/*    */   public static void reportAppError(ProgressState pState, String appName, String section, Throwable t, String key, Object[] args)
/*    */   {
/* 41 */     Report.appError(appName, section, t, key, args);
/* 42 */     pState.reportProgress(-1, t, key, args);
/*    */   }
/*    */ 
/*    */   public static void reportError(ProgressState pState, String section, Throwable t, String key, Object[] args)
/*    */   {
/* 48 */     Report.error(section, t, key, args);
/* 49 */     pState.reportProgress(-1, t, key, args);
/*    */   }
/*    */ 
/*    */   public static void reportError(ProgressState pState, String section, String msg, Throwable t)
/*    */   {
/* 54 */     Report.error(section, msg, t);
/* 55 */     pState.reportProgress(-1, msg, t);
/*    */   }
/*    */ 
/*    */   public static void reportProgress(ProgressState pState, String appName, String section, int type, Throwable t, String key, Object[] args)
/*    */   {
/* 61 */     Report.appTrace(appName, section, t, key, args);
/* 62 */     pState.reportProgress(type, t, key, args);
/*    */   }
/*    */ 
/*    */   public static void reportProgress(ProgressState pState, String appName, String section, int type, String msg, Throwable t)
/*    */   {
/* 68 */     Report.appTrace(appName, section, msg, t);
/* 69 */     pState.reportProgress(type, msg, t);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 74 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74220 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ProgressStateUtils
 * JD-Core Version:    0.5.4
 */