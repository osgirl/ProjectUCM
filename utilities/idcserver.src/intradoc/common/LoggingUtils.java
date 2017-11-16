/*     */ package intradoc.common;
/*     */ 
/*     */ public class LoggingUtils
/*     */ {
/*  25 */   protected static Log m_logger = null;
/*  26 */   protected static boolean m_isInitialized = false;
/*     */ 
/*     */   public static String getLogFileMsgPrefix()
/*     */   {
/*  30 */     return Log.getLogFileMsgPrefix();
/*     */   }
/*     */ 
/*     */   public static void setLogFileMsgPrefix(String prefix)
/*     */   {
/*  35 */     Log.setLogFileMsgPrefix(prefix);
/*     */   }
/*     */ 
/*     */   public static Log getLogger()
/*     */   {
/*  40 */     return m_logger;
/*     */   }
/*     */ 
/*     */   public static void setLogger(Log logger)
/*     */   {
/*  45 */     m_logger = logger;
/*     */   }
/*     */ 
/*     */   public static void reset()
/*     */   {
/*  50 */     setLogFileMsgPrefix(null);
/*  51 */     m_isInitialized = false;
/*     */   }
/*     */ 
/*     */   protected static void logMessage(Throwable t, int type, String msg, String app)
/*     */   {
/*  56 */     switch (type)
/*     */     {
/*     */     case 0:
/*  59 */       Log.infoEx2(msg, app, t);
/*  60 */       break;
/*     */     case 1:
/*  63 */       Log.warnEx2(msg, app, t);
/*  64 */       break;
/*     */     case 2:
/*  67 */       Log.errorEx2(msg, app, t);
/*  68 */       break;
/*     */     case 3:
/*  71 */       Log.fatalEx2(msg, app, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void warning(Throwable t, String msg, String app)
/*     */   {
/*  95 */     logMessage(t, 1, msg, app);
/*     */   }
/*     */ 
/*     */   public static void error(Throwable t, String msg, String app)
/*     */   {
/* 101 */     if ((t == null) && (msg == null))
/*     */     {
/* 103 */       msg = "!syUnknownError";
/*     */     }
/* 105 */     logMessage(t, 2, msg, app);
/*     */   }
/*     */ 
/*     */   public static void fatalEx(Throwable t, String msg, String app)
/*     */   {
/* 111 */     logMessage(t, 3, msg, app);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 116 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90519 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LoggingUtils
 * JD-Core Version:    0.5.4
 */