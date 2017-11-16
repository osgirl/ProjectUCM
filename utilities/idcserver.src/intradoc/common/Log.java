/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class Log
/*     */   implements IdcComparator
/*     */ {
/*     */   public static final int m_infoType = 0;
/*     */   public static final int m_warningType = 1;
/*     */   public static final int m_errorType = 2;
/*     */   public static final int m_fatalType = 3;
/*     */   protected static String m_logFileMsgPrefix;
/*  46 */   protected static Hashtable m_logDirInfos = new Hashtable();
/*     */   protected static final int m_maxFiles = 30;
/*  56 */   public static LogDirInfo m_defaultInfo = new LogDirInfo("system");
/*     */ 
/*  61 */   public static int m_attemptingSystemErrorCount = 0;
/*     */ 
/*  66 */   public static boolean[] m_syncObject = { true };
/*     */ 
/*     */   public static void setLogDirectory(String dir)
/*     */     throws ServiceException
/*     */   {
/*  74 */     FileUtils.validateDirectory(dir, "!csLogUnableToAccessDir");
/*  75 */     m_defaultInfo.m_dir = dir;
/*     */   }
/*     */ 
/*     */   public static void setLogInfo(String app, String dir, String indexPage, String prefix, String header)
/*     */     throws ServiceException
/*     */   {
/*  86 */     FileUtils.validateDirectory(dir, LocaleUtils.encodeMessage("csLogUnableToAccessDirForApp", null, app));
/*     */ 
/*  88 */     LogDirInfo info = new LogDirInfo(app);
/*  89 */     info.m_dir = dir;
/*  90 */     info.m_indexPage = indexPage;
/*  91 */     info.m_prefix = prefix;
/*  92 */     info.m_header = header;
/*  93 */     info.buildExtraLogWriter();
/*     */ 
/*  95 */     m_logDirInfos.put(app, info);
/*     */   }
/*     */ 
/*     */   public static void setDefaultLogInfo(String dir, String indexPage, String prefix, String header)
/*     */     throws ServiceException
/*     */   {
/* 105 */     FileUtils.validateDirectory(dir, "!csLogUnableToAccessDirForDefault");
/*     */ 
/* 107 */     LogDirInfo info = new LogDirInfo("system");
/* 108 */     info.m_dir = dir;
/* 109 */     info.m_indexPage = indexPage;
/* 110 */     info.m_prefix = prefix;
/* 111 */     info.m_header = header;
/* 112 */     info.buildExtraLogWriter();
/*     */ 
/* 114 */     m_defaultInfo = info;
/*     */   }
/*     */ 
/*     */   public static LogDirInfo getDefaultLogDirInfo()
/*     */   {
/* 123 */     if ((m_defaultInfo != null) && (m_defaultInfo.m_prefix != null) && (m_defaultInfo.m_prefix.length() == 0))
/*     */     {
/* 126 */       m_defaultInfo.m_indexPage = "IdcLnLog.htm";
/* 127 */       m_defaultInfo.m_prefix = "IdcLog";
/* 128 */       m_defaultInfo.m_header = "Content Server";
/* 129 */       m_defaultInfo.buildExtraLogWriter();
/*     */     }
/* 131 */     return m_defaultInfo;
/*     */   }
/*     */ 
/*     */   public static void info(String msg)
/*     */   {
/* 140 */     infoEx(msg, null);
/*     */   }
/*     */ 
/*     */   public static void infoEx(String msg, String app)
/*     */   {
/* 149 */     infoEx2(msg, app, null);
/*     */   }
/*     */ 
/*     */   public static void infoEx2(String msg, String app, Throwable t)
/*     */   {
/* 158 */     addMessage(0, msg, app, t);
/*     */   }
/*     */ 
/*     */   public static void warn(String msg)
/*     */   {
/* 166 */     warnEx(msg, null);
/*     */   }
/*     */ 
/*     */   public static void warnEx(String msg, String app)
/*     */   {
/* 174 */     warnEx2(msg, app, null);
/*     */   }
/*     */ 
/*     */   public static void warnEx2(String msg, String app, Throwable t)
/*     */   {
/* 182 */     addMessage(1, msg, app, t);
/*     */   }
/*     */ 
/*     */   public static void error(String msg)
/*     */   {
/*     */     try
/*     */     {
/* 192 */       if (m_attemptingSystemErrorCount++ > 5)
/*     */       {
/* 194 */         String tmpMsg = LocaleUtils.encodeMessage("csLogBusy", null, new Date(), msg);
/* 195 */         Report.trace(null, LocaleResources.localizeMessage(tmpMsg, null), null);
/*     */       }
/*     */       else
/*     */       {
/* 199 */         errorEx(msg, null);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 204 */       m_attemptingSystemErrorCount -= 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void errorEx(String msg, String app)
/*     */   {
/* 213 */     errorEx2(msg, app, null);
/*     */   }
/*     */ 
/*     */   public static void errorEx2(String msg, String app, Throwable t)
/*     */   {
/* 221 */     addMessage(2, msg, app, t);
/*     */   }
/*     */ 
/*     */   public static void fatal(String msg)
/*     */   {
/* 230 */     fatalEx(msg, null);
/*     */   }
/*     */ 
/*     */   public static void fatalEx(String msg, String app)
/*     */   {
/* 239 */     fatalEx2(msg, app, null);
/*     */   }
/*     */ 
/*     */   public static void fatalEx2(String msg, String app, Throwable t)
/*     */   {
/* 248 */     addMessage(3, msg, app, t);
/*     */   }
/*     */ 
/*     */   protected static void addMessage(int messageType, String msg, String app, Throwable t)
/*     */   {
/* 257 */     synchronized (m_syncObject)
/*     */     {
/* 261 */       if (m_logFileMsgPrefix != null)
/*     */       {
/* 263 */         msg = LocaleUtils.appendMessage(msg, m_logFileMsgPrefix + ": ");
/*     */       }
/*     */ 
/* 266 */       LogDirInfo logDirInfo = getLogDirInfo(app);
/*     */ 
/* 268 */       if ((logDirInfo == null) || (logDirInfo.m_logWriters == null))
/*     */       {
/* 270 */         return;
/*     */       }
/* 272 */       int len = logDirInfo.m_logWriters.length;
/* 273 */       for (int j = 0; j < len; ++j)
/*     */       {
/* 275 */         Map logWriterInfo = logDirInfo.m_logWriters[j];
/* 276 */         LogWriter logWriter = (LogWriter)logWriterInfo.get("writer");
/* 277 */         if (logWriter == null)
/*     */           continue;
/* 279 */         logWriter.doMessageAppend(messageType, msg, logDirInfo, t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void addMessage(int messageType, String msg, LogDirInfo logDirInfo, Throwable t)
/*     */   {
/* 288 */     synchronized (m_syncObject)
/*     */     {
/* 290 */       int len = logDirInfo.m_logWriters.length;
/* 291 */       for (int j = 0; j < len; ++j)
/*     */       {
/* 293 */         Map logWriterInfo = logDirInfo.m_logWriters[j];
/* 294 */         IdcLogWriter logWriter = (IdcLogWriter)logWriterInfo.get("writer");
/* 295 */         logWriter.doMessageAppend(messageType, msg, logDirInfo, t);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static LogDirInfo getLogDirInfo(String app)
/*     */   {
/* 302 */     LogDirInfo logDirInfo = getDefaultLogDirInfo();
/* 303 */     if (app != null)
/*     */     {
/* 305 */       logDirInfo = (LogDirInfo)m_logDirInfos.get(app);
/* 306 */       if (logDirInfo == null)
/*     */       {
/* 308 */         logDirInfo = m_defaultInfo;
/*     */       }
/*     */     }
/* 311 */     return logDirInfo;
/*     */   }
/*     */ 
/*     */   public int compare(Object obj1, Object obj2)
/*     */   {
/* 320 */     LogFileInfo info1 = (LogFileInfo)obj1;
/* 321 */     LogFileInfo info2 = (LogFileInfo)obj2;
/* 322 */     if (info1.m_timeStamp.after(info2.m_timeStamp))
/* 323 */       return -1;
/* 324 */     if (info1.m_timeStamp.before(info2.m_timeStamp))
/* 325 */       return 1;
/* 326 */     return 0;
/*     */   }
/*     */ 
/*     */   public static String getRawDesc(int messageType)
/*     */   {
/* 331 */     String rawDesc = null;
/* 332 */     switch (messageType)
/*     */     {
/*     */     case 1:
/* 335 */       rawDesc = "Warning";
/* 336 */       break;
/*     */     case 2:
/* 338 */       rawDesc = "Error";
/* 339 */       break;
/*     */     case 3:
/* 341 */       rawDesc = "Fatal";
/* 342 */       break;
/*     */     default:
/* 344 */       rawDesc = "Info";
/*     */     }
/*     */ 
/* 347 */     return rawDesc;
/*     */   }
/*     */ 
/*     */   public static String getLogFileMsgPrefix()
/*     */   {
/* 352 */     return m_logFileMsgPrefix;
/*     */   }
/*     */ 
/*     */   public static void setLogFileMsgPrefix(String prefix)
/*     */   {
/* 357 */     if (prefix.length() > 0)
/* 358 */       m_logFileMsgPrefix = prefix;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 363 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94145 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Log
 * JD-Core Version:    0.5.4
 */