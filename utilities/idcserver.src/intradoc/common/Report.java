/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class Report
/*     */ {
/*     */   public static final int LEVEL_FATAL = 1000;
/*     */   public static final int LEVEL_ALERT = 2000;
/*     */   public static final int LEVEL_ERROR = 3000;
/*     */   public static final int LEVEL_WARNING = 4000;
/*     */   public static final int LEVEL_INFO = 5000;
/*     */   public static final int LEVEL_TRACE = 6000;
/*     */   public static final int LEVEL_DEBUG = 7000;
/*     */   public static final int LEVEL_MINUTIA = 8000;
/*     */   public static final int DEPR_ONCE = 1;
/*  46 */   public static boolean m_verbose = false;
/*     */ 
/*  51 */   public static boolean m_needsInit = true;
/*     */   protected static ReportDelegator m_delegator;
/* 642 */   protected static Map m_messageCache = null;
/* 643 */   protected static long m_messageCacheExpireTime = 0L;
/* 644 */   public static long m_messageCacheExpireInterval = 900000L;
/*     */ 
/*     */   public static ReportDelegator getDelegator()
/*     */   {
/*  58 */     if (m_needsInit)
/*     */     {
/*  60 */       configureReporting(null);
/*     */     }
/*  62 */     return m_delegator;
/*     */   }
/*     */ 
/*     */   public static void setDelegator(ReportDelegator delegator)
/*     */   {
/*  67 */     m_delegator = delegator;
/*     */   }
/*     */ 
/*     */   public static synchronized void configureReporting(Map settings)
/*     */   {
/*  77 */     if (!m_needsInit)
/*     */     {
/*  79 */       return;
/*     */     }
/*  81 */     if (settings == null)
/*     */     {
/*  83 */       settings = new HashMap();
/*     */     }
/*  85 */     String handlerClassName = (String)settings.get("idc.report.delegator");
/*  86 */     if (handlerClassName == null)
/*     */     {
/*     */       try
/*     */       {
/*  90 */         handlerClassName = System.getProperty("idc.report.delegator");
/*     */       }
/*     */       catch (SecurityException ignore)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/*  97 */     if (handlerClassName == null)
/*     */     {
/*  99 */       handlerClassName = "intradoc.common.DefaultReportDelegator";
/*     */     }
/*     */     try
/*     */     {
/* 103 */       ReportDelegator impl = (ReportDelegator)(ReportDelegator)Class.forName(handlerClassName).newInstance();
/*     */ 
/* 105 */       impl.init(settings);
/* 106 */       m_delegator = impl;
/* 107 */       m_needsInit = false;
/*     */     }
/*     */     catch (InstantiationException e)
/*     */     {
/* 111 */       e.printStackTrace();
/*     */     }
/*     */     catch (IllegalAccessException e)
/*     */     {
/* 115 */       e.printStackTrace();
/*     */     }
/*     */     catch (ClassNotFoundException e)
/*     */     {
/* 119 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void message(String app, String section, int detailLevel, String msg, byte[] bytes, int start, int length, Throwable throwable, Date d)
/*     */   {
/* 132 */     IdcMessage idcMsg = null;
/* 133 */     if (msg != null)
/*     */     {
/* 135 */       idcMsg = IdcMessageFactory.lc();
/* 136 */       if (msg.startsWith("!"))
/*     */       {
/* 138 */         idcMsg.m_msgEncoded = msg;
/*     */       }
/*     */       else
/*     */       {
/* 142 */         idcMsg.m_msgSimple = msg;
/*     */       }
/*     */     }
/* 145 */     messageInternal(app, section, detailLevel, idcMsg, bytes, start, length, throwable, d);
/*     */   }
/*     */ 
/*     */   public static void message(String app, String section, int detailLevel, IdcMessage idcMsg, Throwable throwable, Date d)
/*     */   {
/* 154 */     messageInternal(app, section, detailLevel, idcMsg, null, -1, -1, throwable, d);
/*     */   }
/*     */ 
/*     */   public static void messageInternal(String app, String section, int detailLevel, IdcMessage msg, byte[] bytes, int start, int length, Throwable throwable, Date d)
/*     */   {
/*     */     try
/*     */     {
/* 168 */       if (m_needsInit)
/*     */       {
/* 170 */         configureReporting(null);
/*     */       }
/* 172 */       m_delegator.message(app, section, detailLevel, msg, bytes, start, length, throwable, d);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 180 */       System.out.println("Unable to write message:" + t);
/* 181 */       t.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void debug(String section, String message, Throwable t)
/*     */   {
/* 193 */     message(null, section, 7000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void debug(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 202 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 203 */     message(null, section, 7000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void debug(String section, Throwable t, IdcMessage message)
/*     */   {
/* 212 */     message(null, section, 7000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appDebug(String app, String section, String message, Throwable t)
/*     */   {
/* 221 */     message(app, section, 7000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appDebug(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 230 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 231 */     message(app, section, 7000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void trace(String section, String message, Throwable t)
/*     */   {
/* 240 */     message(null, section, 6000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void trace(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 249 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 250 */     message(null, section, 6000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void trace(String section, Throwable t, IdcMessage message)
/*     */   {
/* 259 */     message(null, section, 6000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appTrace(String app, String section, String message, Throwable t)
/*     */   {
/* 268 */     message(app, section, 6000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appTrace(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 277 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 278 */     message(app, section, 6000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appTrace(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 287 */     message(app, section, 6000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void info(String section, String message, Throwable t)
/*     */   {
/* 296 */     message(null, section, 5000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void info(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 305 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 306 */     message(null, section, 5000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void info(String section, Throwable t, IdcMessage message)
/*     */   {
/* 315 */     message(null, section, 5000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appInfo(String app, String section, String message, Throwable t)
/*     */   {
/* 324 */     message(app, section, 5000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appInfo(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 333 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 334 */     message(app, section, 5000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appInfo(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 343 */     message(app, section, 5000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void warning(String section, String message, Throwable t)
/*     */   {
/* 352 */     message(null, section, 4000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void warning(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 361 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 362 */     message(null, section, 4000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void warning(String section, Throwable t, IdcMessage message)
/*     */   {
/* 371 */     message(null, section, 4000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appWarning(String app, String section, String message, Throwable t)
/*     */   {
/* 380 */     message(app, section, 4000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appWarning(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 389 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 390 */     message(app, section, 4000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appWarning(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 399 */     message(app, section, 4000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void error(String section, String message, Throwable t)
/*     */   {
/* 408 */     message(null, section, 3000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void error(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 417 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 418 */     message(null, section, 3000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void error(String section, Throwable t, IdcMessage message)
/*     */   {
/* 427 */     message(null, section, 3000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appError(String app, String section, String message, Throwable t)
/*     */   {
/* 436 */     message(app, section, 3000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appError(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 445 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 446 */     message(app, section, 3000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appError(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 455 */     message(app, section, 3000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void alert(String section, String message, Throwable t)
/*     */   {
/* 464 */     message(null, section, 2000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void alert(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 473 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 474 */     message(null, section, 2000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void alert(String section, Throwable t, IdcMessage message)
/*     */   {
/* 483 */     message(null, section, 2000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appAlert(String app, String section, String message, Throwable t)
/*     */   {
/* 492 */     message(app, section, 2000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appAlert(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 501 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 502 */     message(app, section, 2000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appAlert(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 511 */     message(app, section, 2000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void fatal(String section, String message, Throwable t)
/*     */   {
/* 520 */     message(null, section, 1000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void fatal(String section, Throwable t, String key, Object[] args)
/*     */   {
/* 529 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 530 */     message(null, section, 1000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void fatal(String section, Throwable t, IdcMessage message)
/*     */   {
/* 539 */     message(null, section, 1000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appFatal(String app, String section, String message, Throwable t)
/*     */   {
/* 548 */     message(app, section, 1000, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appFatal(String app, String section, Throwable t, String key, Object[] args)
/*     */   {
/* 557 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 558 */     message(app, section, 1000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appFatal(String app, String section, Throwable t, IdcMessage message)
/*     */   {
/* 567 */     message(app, section, 1000, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void simpleMessage(String section, int level, String message, Throwable t)
/*     */   {
/* 578 */     message(null, section, level, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void simpleMessage(String section, int level, Throwable t, String key, Object[] args)
/*     */   {
/* 589 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 590 */     message(null, section, level, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void simpleMessage(String section, int level, Throwable t, IdcMessage message)
/*     */   {
/* 601 */     message(null, section, level, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appSimpleMessage(String app, String section, int level, String message, Throwable t)
/*     */   {
/* 612 */     message(app, section, level, message, null, -1, -1, t, null);
/*     */   }
/*     */ 
/*     */   public static void appSimpleMessage(String app, String section, int level, Throwable t, String key, Object[] args)
/*     */   {
/* 623 */     IdcMessage message = IdcMessageFactory.lc(key, args);
/* 624 */     message(app, section, level, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void appSimpleMessage(String app, String section, int level, Throwable t, IdcMessage message)
/*     */   {
/* 635 */     message(app, section, level, message, t, null);
/*     */   }
/*     */ 
/*     */   public static void deprecatedUsage(String msg)
/*     */   {
/* 654 */     if (m_needsInit)
/*     */     {
/* 656 */       configureReporting(null);
/*     */     }
/* 658 */     m_delegator.deprecatedUsage(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 665 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87221 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Report
 * JD-Core Version:    0.5.4
 */