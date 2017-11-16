/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.io.PrintStream;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DefaultReportHandler
/*     */   implements ReportHandler
/*     */ {
/*     */   public static final int TRACER_ENABLED = 1;
/*     */   public static final int TRACER_REPORT_ERROR = 2;
/*     */   public static final int TRACER_TOLERATE_ERROR = 4;
/*     */   public static final int TRACER_IN_ERROR_STATE = 8;
/*     */   public static final int METHOD_TRACE = 0;
/*     */   public static final int METHOD_TRACE_WITH_DATE = 1;
/*     */   public static final int METHOD_TRACE_DIRECT = 2;
/*     */   public static final int METHOD_TRACE_BYTES = 3;
/*     */   public static final int METHOD_DUMP_EXCEPTION = 4;
/*     */   public static final int METHOD_TRACE_DUMP_EXCEPTION = 5;
/*     */   public Hashtable m_activePatterns;
/*     */   public HashMap m_activeSectionCache;
/*     */   public HashMap m_activeTracePatternLists;
/*     */   public String m_tracePrefix;
/*     */   public boolean m_isDefaultTracingActive;
/*     */   public int m_defaultTracerIndex;
/*     */   public TraceImplementor[] m_tracers;
/*     */   public int[] m_tracerFlags;
/*     */   public TraceSection[] m_nullSectionData;
/*     */   public TraceSection[] m_zeroLengthSectionData;
/*     */   public int m_manditoryTraceThreshold;
/*     */   public int m_autoSectionThreshold;
/*     */   public String m_autoSectionName;
/*     */ 
/*     */   public DefaultReportHandler()
/*     */   {
/*  47 */     this.m_activePatterns = new Hashtable();
/*  48 */     this.m_activeSectionCache = new HashMap();
/*  49 */     this.m_activeTracePatternLists = new HashMap();
/*  50 */     this.m_tracePrefix = null;
/*  51 */     this.m_isDefaultTracingActive = true;
/*     */ 
/*  59 */     this.m_defaultTracerIndex = -1;
/*     */ 
/*  61 */     this.m_tracers = new TraceImplementor[0];
/*  62 */     this.m_tracerFlags = new int[0];
/*  63 */     this.m_nullSectionData = new TraceSection[0];
/*  64 */     this.m_zeroLengthSectionData = new TraceSection[0];
/*     */ 
/*  71 */     this.m_manditoryTraceThreshold = 5000;
/*     */ 
/*  77 */     this.m_autoSectionThreshold = 0;
/*     */ 
/*  83 */     this.m_autoSectionName = "system";
/*     */   }
/*     */ 
/*     */   public void init(Map settings) {
/*  87 */     configureTracing();
/*     */   }
/*     */ 
/*     */   public void configureTracing()
/*     */   {
/*  92 */     String prefix = "idc.trace";
/*  93 */     String defaultKey = prefix + ".default.implementor";
/*  94 */     boolean usingDefault = false;
/*     */ 
/*  96 */     Properties props = SystemUtils.getSystemPropertiesClone();
/*  97 */     if (props.getProperty(defaultKey) == null)
/*     */     {
/*  99 */       props.put(defaultKey, "intradoc.common.DefaultTraceImplementor");
/* 100 */       usingDefault = true;
/*     */     }
/*     */ 
/* 103 */     Map[] settingsArray = MapUtils.computeAttributeSets(props, prefix, "order", "0", '.');
/*     */ 
/* 105 */     TraceImplementor[] tracers = new TraceImplementor[settingsArray.length];
/* 106 */     int[] tracerFlags = new int[settingsArray.length];
/* 107 */     TraceSection[] nullSectionData = new TraceSection[settingsArray.length];
/*     */ 
/* 109 */     for (int i = 0; i < settingsArray.length; ++i)
/*     */     {
/* 111 */       Map settings = settingsArray[i];
/* 112 */       String name = (String)settings.get(prefix);
/* 113 */       if ((usingDefault) && (name.equals("default")))
/*     */       {
/* 115 */         this.m_defaultTracerIndex = i;
/*     */       }
/* 117 */       String tracerClassName = (String)settings.get("implementor");
/* 118 */       if (tracerClassName == null)
/*     */       {
/* 120 */         System.err.println("TraceImplementor '" + name + "' is missing the implementor key.  Skipping.");
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 126 */           TraceImplementor impl = (TraceImplementor)(TraceImplementor)Class.forName(tracerClassName).newInstance();
/*     */ 
/* 128 */           settings.put("DefaultReportHandler", this);
/* 129 */           impl.init(settings);
/* 130 */           tracers[i] = impl;
/*     */ 
/* 132 */           int flags = 1;
/* 133 */           String reportError = (String)settings.get("reporterror");
/* 134 */           String tolerateError = (String)settings.get("toleratererror");
/* 135 */           if (StringUtils.convertToBool(reportError, false))
/*     */           {
/* 137 */             flags |= 2;
/*     */           }
/* 139 */           if (StringUtils.convertToBool(tolerateError, false))
/*     */           {
/* 141 */             flags |= 4;
/*     */           }
/* 143 */           tracerFlags[i] = flags;
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 147 */           System.err.println("Unable to configure trace implementor " + tracerClassName + ":");
/*     */ 
/* 149 */           e.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/* 153 */     this.m_tracers = tracers;
/* 154 */     this.m_tracerFlags = tracerFlags;
/* 155 */     this.m_nullSectionData = nullSectionData;
/*     */   }
/*     */ 
/*     */   public boolean isActiveSection(String section)
/*     */   {
/* 164 */     if (section == null)
/*     */     {
/* 166 */       return true;
/*     */     }
/* 168 */     TraceSection[] data = getSectionData(section);
/* 169 */     return data.length > 0;
/*     */   }
/*     */ 
/*     */   public TraceSection[] getSectionData(String section)
/*     */   {
/* 180 */     if (section == null)
/*     */     {
/* 182 */       return null;
/*     */     }
/* 184 */     TraceSection[] data = (TraceSection[])(TraceSection[])this.m_activeSectionCache.get(section);
/*     */ 
/* 186 */     if (data != null)
/*     */     {
/* 188 */       return data;
/*     */     }
/* 190 */     String[] list = (String[])(String[])this.m_activeTracePatternLists.get("system");
/* 191 */     if (list != null)
/*     */     {
/* 193 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/* 195 */         boolean isMatch = StringUtils.matchEx(section, list[i], false, true);
/*     */ 
/* 197 */         if (!isMatch)
/*     */           continue;
/* 199 */         data = new TraceSection[this.m_tracers.length];
/* 200 */         for (int j = 0; j < data.length; ++j)
/*     */         {
/* 202 */           if ((this.m_tracerFlags[j] & 0x1) <= 0)
/*     */             continue;
/* 204 */           data[j] = this.m_tracers[j].makeSectionData(section);
/*     */         }
/*     */ 
/* 207 */         putSectionCache(section, data);
/* 208 */         return data;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 213 */     data = new TraceSection[0];
/* 214 */     this.m_activeSectionCache.put(section, data);
/* 215 */     return data;
/*     */   }
/*     */ 
/*     */   protected synchronized void putSectionCache(String section, TraceSection[] data)
/*     */   {
/* 220 */     this.m_activeSectionCache.put(section, data);
/*     */   }
/*     */ 
/*     */   public synchronized void addAsActivePattern(String pattern)
/*     */   {
/* 229 */     if (pattern.equalsIgnoreCase("all"))
/*     */     {
/* 231 */       pattern = "*";
/*     */     }
/* 233 */     if (this.m_activePatterns.get(pattern) != null)
/*     */       return;
/* 235 */     String[] activePatternList = (String[])(String[])this.m_activeTracePatternLists.get("system");
/* 236 */     if (activePatternList == null)
/*     */     {
/* 238 */       this.m_activeTracePatternLists.put("system", new String[0]);
/* 239 */       activePatternList = (String[])(String[])this.m_activeTracePatternLists.get("system");
/*     */     }
/* 241 */     String[] newSection = new String[activePatternList.length + 1];
/* 242 */     System.arraycopy(activePatternList, 0, newSection, 0, activePatternList.length);
/*     */ 
/* 244 */     newSection[activePatternList.length] = pattern;
/*     */ 
/* 246 */     this.m_activeTracePatternLists.put("system", newSection);
/* 247 */     this.m_activePatterns.put(pattern, pattern);
/* 248 */     this.m_activeSectionCache = new HashMap();
/* 249 */     this.m_isDefaultTracingActive = false;
/*     */   }
/*     */ 
/*     */   public synchronized void addAsDefaultPattern(String pattern)
/*     */   {
/* 255 */     if (!this.m_isDefaultTracingActive)
/*     */       return;
/* 257 */     addAsActivePattern(pattern);
/*     */ 
/* 260 */     this.m_isDefaultTracingActive = true;
/*     */   }
/*     */ 
/*     */   public synchronized void removeAsActivePattern(String pattern)
/*     */   {
/* 269 */     if (pattern.equalsIgnoreCase("all"))
/*     */     {
/* 271 */       pattern = "*";
/*     */     }
/* 273 */     if (this.m_activePatterns.get(pattern) == null)
/*     */       return;
/* 275 */     String[] activePatternList = (String[])(String[])this.m_activeTracePatternLists.get("system");
/* 276 */     if (activePatternList == null)
/*     */     {
/* 278 */       return;
/*     */     }
/* 280 */     String[] newPatternList = new String[activePatternList.length - 1];
/* 281 */     int i = 0; for (int j = 0; i < activePatternList.length; ++j)
/*     */     {
/* 283 */       if (activePatternList[i].equals(pattern))
/*     */       {
/* 285 */         --j;
/*     */       }
/*     */       else
/*     */       {
/* 289 */         newPatternList[j] = activePatternList[i];
/*     */       }
/* 281 */       ++i;
/*     */     }
/*     */ 
/* 292 */     this.m_activeTracePatternLists.put("system", newPatternList);
/* 293 */     this.m_activePatterns.remove(pattern);
/* 294 */     this.m_activeSectionCache = new HashMap();
/* 295 */     this.m_isDefaultTracingActive = false;
/*     */   }
/*     */ 
/*     */   public synchronized void setActivePatterns(Vector patternList)
/*     */   {
/* 305 */     setActivePatterns(patternList, "system");
/*     */ 
/* 307 */     this.m_activeSectionCache = new HashMap();
/*     */   }
/*     */ 
/*     */   public synchronized void setActivePatterns(Vector patternList, String traceType)
/*     */   {
/* 315 */     Hashtable hash = new Hashtable();
/* 316 */     if (patternList == null)
/*     */     {
/* 318 */       patternList = new IdcVector();
/*     */     }
/* 320 */     String[] activeTraceList = new String[patternList.size()];
/* 321 */     for (int i = 0; i < activeTraceList.length; ++i)
/*     */     {
/* 323 */       String pattern = ((String)patternList.elementAt(i)).trim();
/* 324 */       if (pattern.equalsIgnoreCase("all"))
/*     */       {
/* 326 */         pattern = "*";
/*     */       }
/* 328 */       hash.put(pattern, pattern);
/* 329 */       activeTraceList[i] = pattern;
/*     */     }
/* 331 */     if (traceType == "system")
/*     */     {
/* 333 */       this.m_activePatterns = hash;
/*     */     }
/* 335 */     this.m_activeTracePatternLists.put(traceType, activeTraceList);
/* 336 */     this.m_isDefaultTracingActive = false;
/*     */   }
/*     */ 
/*     */   public Vector getActivePatterns()
/*     */   {
/* 345 */     return getActivePatterns("system");
/*     */   }
/*     */ 
/*     */   public Vector getActivePatterns(String traceType)
/*     */   {
/* 353 */     Vector v = new IdcVector();
/* 354 */     String[] activePatternList = null;
/* 355 */     activePatternList = (String[])(String[])this.m_activeTracePatternLists.get(traceType);
/* 356 */     if (activePatternList != null)
/*     */     {
/* 358 */       for (int i = 0; i < activePatternList.length; ++i)
/*     */       {
/* 360 */         v.addElement(activePatternList[i]);
/*     */       }
/*     */     }
/* 363 */     return v;
/*     */   }
/*     */ 
/*     */   public boolean isActiveSection(String name, String traceType)
/*     */   {
/* 372 */     if (name == null)
/*     */     {
/* 374 */       return true;
/*     */     }
/*     */ 
/* 377 */     String[] list = null;
/*     */ 
/* 379 */     list = (String[])(String[])this.m_activeTracePatternLists.get(traceType);
/* 380 */     if (list == null)
/*     */     {
/* 382 */       return true;
/*     */     }
/* 384 */     for (int i = 0; i < list.length; ++i)
/*     */     {
/* 386 */       boolean isMatch = StringUtils.matchEx(name, list[i], false, true);
/* 387 */       if (isMatch)
/*     */       {
/* 389 */         return true;
/*     */       }
/*     */     }
/* 392 */     return false;
/*     */   }
/*     */ 
/*     */   public void message(String app, String section, int level, IdcMessage message, byte[] bytes, int start, int length, Throwable throwable, Date d)
/*     */   {
/* 399 */     Map map = new HashMap();
/* 400 */     TraceImplementor[] tracers = this.m_tracers;
/* 401 */     int method = 0;
/* 402 */     if (bytes != null)
/*     */     {
/* 404 */       method = 3;
/*     */     }
/* 406 */     if (d != null)
/*     */     {
/* 408 */       method = 1;
/*     */     }
/* 410 */     if ((throwable != null) && (message == null))
/*     */     {
/* 412 */       method = 4;
/*     */     }
/* 414 */     if ((throwable != null) && (message != null))
/*     */     {
/* 416 */       method = 5;
/*     */     }
/*     */ 
/* 421 */     TraceSection[] data = this.m_nullSectionData;
/* 422 */     if (section != null)
/*     */     {
/* 424 */       data = getSectionData(section);
/*     */     }
/* 426 */     if ((level < this.m_autoSectionThreshold) && (section == null))
/*     */     {
/* 431 */       section = this.m_autoSectionName;
/* 432 */       data = this.m_zeroLengthSectionData;
/*     */     }
/* 434 */     ArrayList reportList = null;
/* 435 */     ServiceException error = null;
/*     */ 
/* 439 */     String messageString = null;
/* 440 */     boolean isMsgIdcException = false;
/* 441 */     if ((message == null) && (throwable instanceof IdcException))
/*     */     {
/* 443 */       IdcException idcException = (IdcException)throwable;
/* 444 */       if (idcException.m_message != null)
/*     */       {
/* 446 */         message = idcException.m_message;
/* 447 */         isMsgIdcException = true;
/*     */       }
/*     */     }
/* 450 */     if (message != null)
/*     */     {
/* 452 */       if ((message.m_prior == null) && (message.m_msgSimple != null) && (level >= this.m_manditoryTraceThreshold))
/*     */       {
/* 455 */         messageString = message.m_msgSimple;
/*     */       }
/*     */       else
/*     */       {
/* 459 */         messageString = LocaleUtils.encodeMessage(message);
/*     */       }
/*     */     }
/* 462 */     for (int i = 0; i < tracers.length; ++i)
/*     */     {
/* 464 */       int flags = this.m_tracerFlags[i];
/* 465 */       if ((flags & 0x1) == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 470 */       TraceImplementor tracer = tracers[i];
/* 471 */       int threshold = this.m_manditoryTraceThreshold;
/* 472 */       if (tracer instanceof TraceParameterImplementor)
/*     */       {
/* 474 */         TraceParameterImplementor tpi = (TraceParameterImplementor)tracer;
/* 475 */         threshold = tpi.getIntegerParameter("ManditoryTraceThreshold", this.m_manditoryTraceThreshold);
/*     */       }
/*     */ 
/* 478 */       if ((section != null) && (data.length <= 0) && (level >= threshold))
/*     */         continue;
/* 480 */       TraceSection tracerData = null;
/* 481 */       if (data.length > 0)
/*     */       {
/* 483 */         tracerData = data[i];
/*     */       }
/* 485 */       else if (section != null)
/*     */       {
/* 487 */         tracerData = tracer.makeSectionData(section);
/*     */       }
/*     */       else
/*     */       {
/* 491 */         tracerData = this.m_nullSectionData[i];
/*     */       }
/*     */       try
/*     */       {
/* 495 */         switch (method)
/*     */         {
/*     */         case 0:
/* 498 */           tracer.trace(tracerData, level, messageString, map);
/* 499 */           break;
/*     */         case 1:
/* 501 */           tracer.traceWithDate(tracerData, level, messageString, d, map);
/* 502 */           break;
/*     */         case 3:
/* 505 */           break;
/*     */         case 4:
/* 507 */           tracer.dumpException(tracerData, level, throwable, map);
/* 508 */           break;
/*     */         case 5:
/* 510 */           tracer.traceDumpException(tracerData, level, messageString, throwable, map);
/*     */ 
/* 512 */           break;
/*     */         case 2:
/*     */         default:
/* 514 */           throw new AssertionError("!$Unknown method index " + method);
/*     */         }
/* 516 */         if (bytes != null)
/*     */         {
/* 518 */           tracer.traceBytes(tracerData, level, bytes, start, length, map);
/*     */         }
/*     */ 
/* 521 */         this.m_tracerFlags[i] &= -9;
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 527 */         if (((flags & 0x2) > 0) && ((flags & 0x8) == 0))
/*     */         {
/* 532 */           if (reportList == null)
/*     */           {
/* 534 */             reportList = new ArrayList();
/*     */           }
/* 536 */           reportList.add(t);
/*     */         }
/* 538 */         if ((flags & 0x4) == 0)
/*     */         {
/* 540 */           if (error == null)
/*     */           {
/* 542 */             error = new ServiceException("!$Unable to trace successfully.");
/*     */ 
/* 544 */             SystemUtils.setExceptionCause(error, t);
/*     */           }
/* 546 */           error.addCause(t);
/* 547 */           System.err.println("Exception during tracing: ");
/* 548 */           t.printStackTrace();
/* 549 */           if (message != null)
/*     */           {
/* 551 */             System.err.println("The original message was " + message);
/*     */           }
/* 553 */           if (throwable != null)
/*     */           {
/* 555 */             System.err.print("The original throwable was ");
/* 556 */             throwable.printStackTrace();
/*     */           }
/*     */         }
/* 559 */         this.m_tracerFlags[i] |= 8;
/*     */       }
/*     */     }
/*     */ 
/* 563 */     if (level <= 5000)
/*     */     {
/* 565 */       int messageType = 0;
/* 566 */       if (level <= 1000) messageType = 3;
/* 567 */       else if (level <= 3000) messageType = 2;
/* 568 */       else if (level <= 4000) messageType = 1;
/* 569 */       else if (level <= 5000) messageType = 0;
/*     */ 
/* 573 */       if (isMsgIdcException)
/*     */       {
/* 577 */         messageString = null;
/*     */       }
/* 579 */       Log.addMessage(messageType, messageString, app, throwable);
/*     */     }
/* 581 */     if (error == null)
/*     */     {
/*     */       return;
/*     */     }
/*     */ 
/* 586 */     throw new AssertionError(error);
/*     */   }
/*     */ 
/*     */   public void setTraceParameter(String key, String value)
/*     */   {
/* 592 */     for (int i = 0; i < this.m_tracers.length; ++i)
/*     */     {
/* 594 */       if (!this.m_tracers[i] instanceof TraceParameterImplementor)
/*     */         continue;
/* 596 */       TraceParameterImplementor tpi = (TraceParameterImplementor)this.m_tracers[i];
/* 597 */       tpi.setParameter(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 604 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102942 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DefaultReportHandler
 * JD-Core Version:    0.5.4
 */