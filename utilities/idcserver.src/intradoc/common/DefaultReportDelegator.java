/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DefaultReportDelegator
/*     */   implements ReportDelegator
/*     */ {
/*  37 */   public static ReportHandler[] m_handlers = new ReportHandler[0];
/*  38 */   public static int m_defaultHandlerIndex = -1;
/*  39 */   public static int[] m_handlerFlags = new int[0];
/*     */   public static final int REPORTER_ENABLED = 1;
/*     */   public static final int REPORTER_REPORT_ERROR = 2;
/*     */   public static final int REPORTER_TOLERATE_ERROR = 4;
/*     */   public static final int REPORTER_IN_ERROR_STATE = 8;
/*     */   protected Map m_messageCache;
/*     */   protected long m_messageCacheExpireTime;
/*     */   public long m_messageCacheExpireInterval;
/*     */ 
/*     */   public DefaultReportDelegator()
/*     */   {
/* 246 */     this.m_messageCache = null;
/* 247 */     this.m_messageCacheExpireTime = 0L;
/* 248 */     this.m_messageCacheExpireInterval = 900000L;
/*     */   }
/*     */ 
/*     */   public void init(Map options)
/*     */   {
/*  50 */     configureReporting(options);
/*     */   }
/*     */ 
/*     */   public void configureReporting(Map options)
/*     */   {
/*  61 */     String prefix = "idc.report";
/*  62 */     String defaultKey = prefix + ".default.implementor";
/*  63 */     boolean usingDefault = false;
/*     */ 
/*  65 */     Properties props = SystemUtils.getSystemPropertiesClone();
/*  66 */     if (props.getProperty(defaultKey) == null)
/*     */     {
/*  68 */       props.put(defaultKey, "intradoc.common.DefaultReportHandler");
/*  69 */       usingDefault = true;
/*     */     }
/*     */ 
/*  72 */     Map[] handlers = MapUtils.computeAttributeSets(props, prefix, "order", "0", '.');
/*     */ 
/*  74 */     m_handlers = new ReportHandler[handlers.length];
/*  75 */     m_handlerFlags = new int[handlers.length];
/*     */ 
/*  77 */     for (int i = 0; i < handlers.length; ++i)
/*     */     {
/*  79 */       Map settings = handlers[i];
/*  80 */       String name = (String)settings.get(prefix);
/*  81 */       if ((usingDefault) && (name.equals("default")))
/*     */       {
/*  83 */         m_defaultHandlerIndex = i;
/*     */       }
/*  85 */       String handlerClassName = (String)settings.get("implementor");
/*  86 */       if (handlerClassName == null)
/*     */       {
/*  88 */         System.err.println("Report handler '" + name + "' is missing the implementor key.  Skipping.");
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/*  94 */           ReportHandler impl = (ReportHandler)(ReportHandler)Class.forName(handlerClassName).newInstance();
/*     */ 
/*  96 */           impl.init(settings);
/*  97 */           m_handlers[i] = impl;
/*     */ 
/*  99 */           int flags = 1;
/* 100 */           String reportError = (String)settings.get("reporterror");
/* 101 */           String tolerateError = (String)settings.get("toleratererror");
/* 102 */           if (StringUtils.convertToBool(reportError, false))
/*     */           {
/* 104 */             flags |= 2;
/*     */           }
/* 106 */           if (StringUtils.convertToBool(tolerateError, false))
/*     */           {
/* 108 */             flags |= 4;
/*     */           }
/* 110 */           m_handlerFlags[i] = flags;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 114 */           System.err.println("Unable to configure handler " + handlerClassName + ":");
/*     */ 
/* 116 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void message(String app, String section, int detailLevel, IdcMessage msg, byte[] bytes, int start, int length, Throwable throwable, Date d)
/*     */   {
/* 131 */     List reportList = null;
/* 132 */     ServiceException error = null;
/* 133 */     for (int i = 0; i < m_handlers.length; ++i)
/*     */     {
/* 135 */       int flags = m_handlerFlags[i];
/*     */       try
/*     */       {
/* 138 */         if ((flags & 0x1) != 0)
/*     */         {
/* 140 */           m_handlers[i].message(app, section, detailLevel, msg, bytes, start, length, throwable, d);
/*     */ 
/* 142 */           m_handlerFlags[i] = (flags & 0xFFFFFFF7);
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 147 */         if (((flags & 0x2) > 0) && ((flags & 0x8) == 0))
/*     */         {
/* 150 */           if (reportList == null)
/*     */           {
/* 152 */             reportList = new ArrayList();
/*     */           }
/* 154 */           reportList.add(t);
/*     */         }
/* 156 */         if ((flags & 0x4) == 0)
/*     */         {
/* 158 */           if (error == null)
/*     */           {
/* 160 */             error = new ServiceException("!$Unable to report messages successfully.");
/*     */ 
/* 162 */             SystemUtils.setExceptionCause(error, t);
/*     */           }
/* 164 */           error.addCause(t);
/* 165 */           System.err.println("Exception during tracing: ");
/* 166 */           t.printStackTrace();
/* 167 */           if (msg != null)
/*     */           {
/* 169 */             System.err.println("Original message was " + msg);
/*     */           }
/* 171 */           if (throwable != null)
/*     */           {
/* 173 */             System.err.print("Original throwable was ");
/* 174 */             throwable.printStackTrace();
/*     */           }
/*     */         }
/* 177 */         m_handlerFlags[i] = (flags | 0x8);
/*     */       }
/*     */     }
/* 180 */     if (error == null)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/* 185 */     throw new AssertionError(error);
/*     */   }
/*     */ 
/*     */   public boolean isActiveSection(String section)
/*     */   {
/* 191 */     List reportList = null;
/* 192 */     ServiceException error = null;
/* 193 */     for (int i = 0; i < m_handlers.length; ++i)
/*     */     {
/* 195 */       int flags = m_handlerFlags[i];
/*     */       try
/*     */       {
/* 198 */         if (((flags & 0x1) != 0) && 
/* 200 */           (m_handlers[i].isActiveSection(section)))
/*     */         {
/* 202 */           return true;
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 208 */         if (((flags & 0x2) > 0) && ((flags & 0x8) == 0))
/*     */         {
/* 211 */           if (reportList == null)
/*     */           {
/* 213 */             reportList = new ArrayList();
/*     */           }
/* 215 */           reportList.add(t);
/*     */         }
/* 217 */         if ((flags & 0x4) == 0)
/*     */         {
/* 219 */           if (error == null)
/*     */           {
/* 221 */             error = new ServiceException("!$Unable to report messages successfully.");
/*     */ 
/* 223 */             SystemUtils.setExceptionCause(error, t);
/*     */           }
/* 225 */           error.addCause(t);
/* 226 */           System.err.println("Exception during tracing: ");
/* 227 */           t.printStackTrace();
/*     */         }
/* 229 */         m_handlerFlags[i] = (flags | 0x8);
/*     */       }
/*     */     }
/* 232 */     return false;
/*     */   }
/*     */ 
/*     */   public ReportHandler getDefaultReportHandler()
/*     */   {
/* 237 */     if (m_defaultHandlerIndex >= 0)
/*     */     {
/* 239 */       return m_handlers[m_defaultHandlerIndex];
/*     */     }
/* 241 */     return null;
/*     */   }
/*     */ 
/*     */   public void deprecatedUsage(String msg)
/*     */   {
/* 258 */     long now = System.currentTimeMillis();
/* 259 */     if (now >= this.m_messageCacheExpireTime)
/*     */     {
/* 261 */       this.m_messageCacheExpireTime = (now + this.m_messageCacheExpireInterval);
/* 262 */       this.m_messageCache = new HashMap();
/*     */     }
/* 264 */     Map cache = this.m_messageCache;
/* 265 */     IdcMessage idcMsg = IdcMessageFactory.lc();
/* 266 */     idcMsg.m_msgSimple = msg;
/* 267 */     if ((Report.m_verbose) || (SystemUtils.m_isDevelopmentEnvironment))
/*     */     {
/* 269 */       StackTrace e = new StackTrace();
/* 270 */       IdcCharArrayWriter w = new IdcCharArrayWriter();
/* 271 */       PrintWriter pw = new PrintWriter(w);
/* 272 */       e.printStackTrace(pw);
/* 273 */       String text = msg + w.toString();
/* 274 */       if (cache.get(text) == null)
/*     */       {
/* 276 */         cache.put(text, text);
/* 277 */         message(null, "deprecation", 7000, idcMsg, null, -1, -1, e, null);
/*     */       }
/* 279 */       w.releaseBuffers();
/*     */     }
/*     */     else
/*     */     {
/* 283 */       if (cache.get(msg) != null)
/*     */         return;
/* 285 */       cache.put(msg, msg);
/* 286 */       message(null, "deprecation", 6000, idcMsg, null, -1, -1, null, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 293 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DefaultReportDelegator
 * JD-Core Version:    0.5.4
 */