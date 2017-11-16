/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.DefaultTraceParameters;
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringBufferOutputStream;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TraceImplementor;
/*     */ import intradoc.common.TraceParameterImplementor;
/*     */ import intradoc.common.TraceSection;
/*     */ import intradoc.common.TracerReportUtils;
/*     */ import intradoc.server.utils.MessageLoggerUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcLoggerUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.logging.Level;
/*     */ import java.util.logging.Logger;
/*     */ 
/*     */ public class ServletLogger
/*     */   implements TraceImplementor, TraceParameterImplementor
/*     */ {
/*     */   protected Object m_logger;
/*     */   protected int m_manditoryStackDumpThreshold;
/*     */   protected boolean m_haveInitRelativeWebRoot;
/*     */   protected String m_relativeWebRoot;
/*     */   public DefaultTraceParameters m_traceParameters;
/*     */   protected String m_bundleName;
/*     */ 
/*     */   public ServletLogger()
/*     */   {
/*  54 */     this.m_logger = null;
/*     */ 
/*  57 */     this.m_manditoryStackDumpThreshold = 3000;
/*  58 */     this.m_haveInitRelativeWebRoot = false;
/*  59 */     this.m_relativeWebRoot = null;
/*     */ 
/*  62 */     this.m_bundleName = "intradoc.util.IdcWrapperBundle";
/*     */   }
/*     */ 
/*     */   public void init(Map params) {
/*  66 */     this.m_traceParameters = new DefaultTraceParameters("Servlet");
/*     */ 
/*  68 */     String implName = (String)params.get("implementor");
/*  69 */     this.m_logger = TracerReportUtils.m_integratorMap.get(implName);
/*  70 */     if (this.m_logger == null)
/*     */       return;
/*  72 */     ClassHelperUtils.executeMethodSuppressException(this.m_logger, "configure", new Object[] { params });
/*     */   }
/*     */ 
/*     */   public void configureTrace(List flags, Map env)
/*     */   {
/*  78 */     this.m_traceParameters.configure(flags, env);
/*     */ 
/*  81 */     Map flagMap = this.m_traceParameters.m_flags;
/*  82 */     if (flagMap.get("ManditoryTraceThreshold") == null)
/*     */     {
/*  84 */       this.m_traceParameters.m_flags.put("ManditoryTraceThreshold", "6000");
/*     */     }
/*     */ 
/*  87 */     this.m_manditoryStackDumpThreshold = getIntegerParameter("ManditoryStackDumpThreshold", 3000);
/*     */ 
/*  92 */     boolean defToConsole = TracerReportUtils.getTraceToConsole();
/*  93 */     TracerReportUtils.m_traceToConsole = getBooleanParameter("defaultTraceToConsole", defToConsole);
/*     */   }
/*     */ 
/*     */   public TraceSection makeSectionData(String section)
/*     */   {
/*  99 */     return new TraceSection(section);
/*     */   }
/*     */ 
/*     */   public void trace(TraceSection section, int level, String message, Map params)
/*     */   {
/* 104 */     if ((message == null) || (message.length() == 0))
/*     */     {
/* 107 */       message = "(no message)";
/*     */     }
/* 109 */     if (SystemUtils.m_isServerStopped)
/*     */     {
/* 112 */       return;
/*     */     }
/*     */ 
/* 115 */     ClassLoader prevThreadClassLoader = Thread.currentThread().getContextClassLoader();
/*     */     try
/*     */     {
/* 118 */       ClassLoader thisObjClassLoader = super.getClass().getClassLoader();
/* 119 */       Thread.currentThread().setContextClassLoader(thisObjClassLoader);
/* 120 */       if (this.m_logger == null)
/*     */       {
/* 122 */         IdcMessage idcMsg = null;
/* 123 */         if (message.startsWith("!"))
/*     */         {
/* 125 */           idcMsg = LocaleUtils.parseMessage(message);
/*     */         }
/*     */         else
/*     */         {
/* 129 */           idcMsg = IdcMessageFactory.lc();
/* 130 */           idcMsg.m_msgSimple = message;
/*     */         }
/* 132 */         Level lvl = computeLevel(level);
/*     */ 
/* 134 */         Logger serverLogger = Logger.getLogger(MessageLoggerUtils.m_loggerName, this.m_bundleName);
/* 135 */         if (serverLogger.isLoggable(lvl))
/*     */         {
/* 137 */           IdcMessage currentMsg = idcMsg;
/* 138 */           for (; currentMsg != null; currentMsg = currentMsg.m_prior)
/*     */           {
/* 140 */             String msgKey = currentMsg.m_stringKey;
/* 141 */             String msg = null;
/* 142 */             Object[] args = null;
/* 143 */             boolean foundIt = false;
/* 144 */             if (msgKey == null)
/*     */             {
/* 148 */               msg = currentMsg.m_msgSimple;
/*     */             }
/*     */             else
/*     */             {
/* 154 */               String number = null;
/* 155 */               String prefix = null;
/* 156 */               DynamicData messages = ResourceContainerUtils.getDynamicDataResource("UCM_MessageKeys");
/* 157 */               if (messages != null)
/*     */               {
/* 159 */                 Table rows = messages.getIndexedTable("name", msgKey);
/*     */ 
/* 162 */                 if ((rows != null) && (rows.getNumRows() > 0))
/*     */                 {
/* 164 */                   int i = 0;
/* 165 */                   for (String colName : rows.m_colNames)
/*     */                   {
/* 167 */                     if (colName.equals("number"))
/*     */                     {
/* 169 */                       number = rows.getString(0, i);
/*     */                     }
/* 171 */                     else if (colName.equals("prefix"))
/*     */                     {
/* 173 */                       prefix = rows.getString(0, i);
/*     */                     }
/* 175 */                     ++i;
/*     */                   }
/*     */                 }
/* 178 */                 foundIt = (number != null) && (prefix != null);
/*     */               }
/*     */ 
/* 181 */               if (foundIt)
/*     */               {
/* 183 */                 args = determineArgs(currentMsg);
/* 184 */                 msgKey = MessageLoggerUtils.calculateLoggerMessageKey(msgKey, prefix, number);
/*     */               }
/*     */               else
/*     */               {
/* 191 */                 IdcMessage prior = currentMsg.m_prior;
/* 192 */                 currentMsg.m_prior = null;
/* 193 */                 msg = LocaleResources.localizeMessage(null, currentMsg, null).toString();
/*     */ 
/* 195 */                 currentMsg.m_prior = prior;
/*     */               }
/*     */             }
/* 198 */             if (foundIt)
/*     */             {
/* 200 */               if (msgKey != null)
/*     */               {
/* 202 */                 msgKey = msgKey.trim();
/*     */               }
/* 204 */               if ((msgKey != null) && (msgKey.length() > 0))
/*     */               {
/* 206 */                 serverLogger.log(lvl, msgKey, args);
/*     */               }
/*     */               else
/*     */               {
/* 210 */                 if (!IdcLoggerUtils.m_internalDebug)
/*     */                   continue;
/* 212 */                 System.err.println("Got empty key");
/*     */               }
/*     */ 
/*     */             }
/*     */             else
/*     */             {
/* 218 */               logGenericMessage(serverLogger, lvl, msg);
/*     */             }
/*     */           }
/* 221 */           Throwable paramThrowable = null;
/* 222 */           if (params != null)
/*     */           {
/* 224 */             paramThrowable = (Throwable)params.get("throwable");
/*     */           }
/* 226 */           if (paramThrowable != null)
/*     */           {
/* 228 */             logThrowable(serverLogger, lvl, paramThrowable);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 234 */         traceByWrapper(section, level, message, params);
/*     */       }
/*     */     }
/*     */     catch (Throwable tt)
/*     */     {
/* 239 */       SystemUtils.m_err.println("Unable to use ServletLogger to trace error:" + message);
/* 240 */       tt.printStackTrace();
/*     */     }
/*     */     finally
/*     */     {
/* 244 */       Thread.currentThread().setContextClassLoader(prevThreadClassLoader);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void logGenericMessage(Logger serverLogger, Level lvl, String msg)
/*     */   {
/* 250 */     String genericExceptionKey = MessageLoggerUtils.getHardWiredGeneralExceptionKey();
/* 251 */     serverLogger.log(lvl, genericExceptionKey, new Object[] { msg });
/*     */   }
/*     */ 
/*     */   public void logThrowable(Logger serverLogger, Level lvl, Throwable t)
/*     */   {
/* 266 */     String genericExceptionKey = MessageLoggerUtils.getHardWiredGeneralExceptionKey();
/* 267 */     serverLogger.log(lvl, genericExceptionKey, t);
/*     */   }
/*     */ 
/*     */   public Object[] determineArgs(IdcMessage msg)
/*     */   {
/* 272 */     Object[] iArgs = null;
/* 273 */     Map typeArgMap = new HashMap();
/* 274 */     ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/* 275 */     context.setCachedObject("ConvertToJavaStandardForm", "1");
/* 276 */     context.setCachedObject("ConvertToJavaStandardTypes", typeArgMap);
/*     */ 
/* 278 */     IdcMessage prior = msg.m_prior;
/* 279 */     msg.m_prior = null;
/*     */     try
/*     */     {
/* 282 */       LocaleResources.localizeMessage(null, msg, context);
/*     */ 
/* 284 */       int size = msg.m_args.length;
/* 285 */       iArgs = new Object[size];
/* 286 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 288 */         Object value = msg.m_args[i];
/* 289 */         String typeArg = (String)typeArgMap.get("" + i);
/* 290 */         if (typeArg != null)
/*     */         {
/* 292 */           char type = typeArg.charAt(0);
/* 293 */           String arg = typeArg.substring(1);
/* 294 */           value = LocaleResources.handleArgumentRaw(value, arg, type, context);
/*     */         }
/*     */ 
/* 297 */         iArgs[i] = value;
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 302 */       msg.m_prior = prior;
/*     */     }
/* 304 */     return iArgs;
/*     */   }
/*     */ 
/*     */   protected void traceByWrapper(TraceSection section, int level, String message, Map params)
/*     */   {
/* 317 */     String key = "csTraceMessage";
/* 318 */     String locMessage = null;
/* 319 */     char firstChar = message.charAt(0);
/* 320 */     if (firstChar == '!')
/*     */     {
/* 322 */       locMessage = LocaleResources.localizeMessage(message, null);
/* 323 */       int cIndex = message.indexOf(",");
/* 324 */       if (cIndex >= 0)
/*     */       {
/* 326 */         key = message.substring(1, cIndex);
/*     */       }
/*     */       else
/*     */       {
/* 330 */         cIndex = message.indexOf(" ");
/* 331 */         if (cIndex < 0)
/*     */         {
/* 333 */           key = message.substring(1);
/*     */         }
/*     */       }
/*     */     }
/* 337 */     if (locMessage == null)
/*     */     {
/* 339 */       locMessage = message;
/*     */     }
/* 341 */     params.put("localizedMessage", locMessage);
/* 342 */     params.put("messsageKey", key);
/*     */ 
/* 344 */     if ((this.m_relativeWebRoot == null) && (SharedObjects.isInit()))
/*     */     {
/* 346 */       this.m_relativeWebRoot = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*     */     }
/* 348 */     if (this.m_relativeWebRoot != null)
/*     */     {
/* 350 */       params.put("HttpRelativeWebRoot", this.m_relativeWebRoot);
/*     */     }
/*     */ 
/* 353 */     String sName = getSectionName(section);
/* 354 */     ClassHelperUtils.executeMethodSuppressException(this.m_logger, "trace", new Object[] { sName, Integer.valueOf(level), message, params });
/*     */   }
/*     */ 
/*     */   protected String getSectionName(TraceSection section)
/*     */   {
/* 360 */     String sName = "internal";
/* 361 */     if (section != null)
/*     */     {
/* 363 */       sName = section.m_name;
/*     */     }
/* 365 */     return sName;
/*     */   }
/*     */ 
/*     */   public void traceWithDate(TraceSection section, int level, String message, Date d, Map params)
/*     */   {
/* 370 */     trace(section, level, message, params);
/*     */   }
/*     */ 
/*     */   public void traceDirectToOutput(TraceSection section, int level, String message, Map params)
/*     */   {
/* 375 */     trace(section, level, message, params);
/*     */   }
/*     */ 
/*     */   public void traceBytes(TraceSection section, int level, byte[] b, int start, int len, Map params)
/*     */   {
/* 381 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void traceDumpException(TraceSection section, int level, String message, Throwable t, Map params)
/*     */   {
/* 387 */     if (message == null)
/*     */     {
/* 389 */       message = "<Internal> ";
/*     */     }
/*     */ 
/* 392 */     params.put("throwable", t);
/* 393 */     trace(section, level, message, params);
/*     */   }
/*     */ 
/*     */   public void dumpException(TraceSection section, int level, Throwable t, Map params)
/*     */   {
/*     */     String msg;
/*     */     String msg;
/* 399 */     if ((t instanceof StackTrace) || (t.getMessage() == null))
/*     */     {
/* 401 */       msg = "";
/*     */     }
/*     */     else
/*     */     {
/* 405 */       msg = t.getMessage() + " exception stack\n";
/*     */     }
/*     */ 
/* 408 */     params.put("throwable", t);
/* 409 */     trace(section, level, msg, params);
/*     */   }
/*     */ 
/*     */   public void appendStackTrace(TraceSection section, OutputStream out, Throwable t)
/*     */   {
/* 417 */     if (t == null)
/*     */     {
/* 419 */       if (this.m_traceParameters.m_alwaysIncludeStack)
/*     */       {
/* 421 */         t = new StackTrace();
/*     */       }
/* 423 */       return;
/*     */     }
/* 425 */     if (this.m_traceParameters.m_dumpExceptionFull)
/*     */     {
/* 427 */       Throwable newT = new StackTrace();
/* 428 */       SystemUtils.setExceptionCause(newT, t);
/* 429 */       t = newT;
/*     */     }
/* 431 */     PrintStream printStream = new PrintStream(out);
/* 432 */     if ((t instanceof StackTrace) && (out instanceof StringBufferOutputStream))
/*     */     {
/* 434 */       StringBufferOutputStream sbos = (StringBufferOutputStream)out;
/* 435 */       sbos.m_skipUntil = '\t';
/*     */     }
/* 437 */     t.printStackTrace(printStream);
/*     */   }
/*     */ 
/*     */   public void appendStackTrace(OutputStream out, Throwable t)
/*     */   {
/* 442 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void appendTraceMessage(StringBuffer buf, TraceSection section, String msg, Date d, boolean fullTraceInfo)
/*     */   {
/* 451 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void appendTraceMessage(IdcAppendable buf, TraceSection section, int level, String msg, Date d, boolean fullTraceInfo)
/*     */   {
/* 457 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void out(String message)
/*     */   {
/* 462 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void out(char[] message, int start, int length)
/*     */   {
/* 467 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void outln(String message)
/*     */   {
/* 472 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void outln(char[] message, int start, int length)
/*     */   {
/* 477 */     throw new UnsupportedOperationException();
/*     */   }
/*     */ 
/*     */   public void setParameter(String key, Object value)
/*     */   {
/* 485 */     this.m_traceParameters.setParameter(key, value);
/*     */   }
/*     */ 
/*     */   public Object getParameter(String key)
/*     */   {
/* 490 */     return this.m_traceParameters.getParameter(key);
/*     */   }
/*     */ 
/*     */   public String getStringParameter(String key, String defValue)
/*     */   {
/* 495 */     return this.m_traceParameters.getStringParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public int getIntegerParameter(String key, int defValue)
/*     */   {
/* 500 */     return this.m_traceParameters.getIntegerParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public boolean getBooleanParameter(String key, boolean defValue)
/*     */   {
/* 505 */     return this.m_traceParameters.getBooleanParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public Level computeLevel(int level)
/*     */   {
/* 510 */     Level lvl = Level.FINEST;
/*     */ 
/* 513 */     if (level <= 1000)
/*     */     {
/* 515 */       lvl = Level.SEVERE;
/*     */     }
/* 517 */     else if (level <= 3000)
/*     */     {
/* 519 */       lvl = Level.SEVERE;
/*     */     }
/* 521 */     else if (level <= 4000)
/*     */     {
/* 523 */       lvl = Level.WARNING;
/*     */     }
/* 525 */     else if (level <= 5000)
/*     */     {
/* 527 */       lvl = Level.INFO;
/*     */     }
/* 529 */     else if (level <= 6000)
/*     */     {
/* 531 */       lvl = Level.FINE;
/*     */     }
/*     */     else
/*     */     {
/* 535 */       lvl = Level.FINER;
/*     */     }
/* 537 */     return lvl;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 542 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87581 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletLogger
 * JD-Core Version:    0.5.4
 */