/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.OutputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.regex.Matcher;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class DefaultTraceImplementor
/*     */   implements TraceImplementor, TraceParameterImplementor
/*     */ {
/*     */   public DefaultTraceParameters m_traceParameters;
/*     */   public PrintStream m_output;
/*     */   public String m_tracePrefix;
/*     */   public DefaultReportHandler m_reportHandler;
/*     */   public Map<String, String> m_settings;
/*     */   public ForkedOutputStream m_forkedOutput;
/*     */   public TriggerOutputStream m_triggeredOutputStream;
/*     */   public OutputStream m_traceOutputStream;
/*     */   public boolean m_triggerThreadDump;
/*  43 */   public static Map<String, String> m_redirectMessages = new HashMap();
/*     */ 
/*     */   public DefaultTraceImplementor()
/*     */   {
/*  30 */     this.m_output = System.err;
/*     */ 
/*  32 */     this.m_tracePrefix = null;
/*  33 */     this.m_reportHandler = null;
/*     */ 
/*  36 */     this.m_forkedOutput = null;
/*     */ 
/*  41 */     this.m_triggerThreadDump = false;
/*     */   }
/*     */ 
/*     */   public void init(Map settings)
/*     */   {
/*  47 */     this.m_settings = settings;
/*  48 */     this.m_reportHandler = ((DefaultReportHandler)settings.get("DefaultReportHandler"));
/*  49 */     this.m_traceParameters = new DefaultTraceParameters();
/*     */   }
/*     */ 
/*     */   public void configureTrace(List flags, Map env)
/*     */   {
/*  54 */     if (SystemUtils.m_captureOutStream == null)
/*     */     {
/*  57 */       return;
/*     */     }
/*     */ 
/*  61 */     this.m_traceParameters.configure(flags, env);
/*  62 */     setTriggerOutputStream(env);
/*  63 */     this.m_triggerThreadDump = StringUtils.convertToBool((String)env.get("EventFileTriggerAddThreadDump"), false);
/*     */ 
/*  66 */     OutputStream[] streams = null;
/*     */ 
/*  70 */     boolean toStdErr = TracerReportUtils.getBooleanFlag(flags, "traceToStdErr", false);
/*  71 */     boolean toStdOut = TracerReportUtils.getBooleanFlag(flags, "traceToStdOut", false);
/*  72 */     if ((TracerReportUtils.m_traceToConsole) || (toStdErr) || (toStdOut))
/*     */     {
/*  77 */       OutputStream traceOutput = SystemUtils.m_err;
/*  78 */       if (toStdOut)
/*     */       {
/*  80 */         traceOutput = SystemUtils.m_out;
/*     */       }
/*  82 */       streams = new OutputStream[] { traceOutput, this.m_triggeredOutputStream };
/*     */     }
/*     */     else
/*     */     {
/*  86 */       OutputStream traceOutput = createRollingOutputStream(env, "Trace");
/*     */ 
/*  89 */       FileUtils.closeObject(this.m_traceOutputStream);
/*  90 */       this.m_traceOutputStream = traceOutput;
/*     */ 
/*  92 */       streams = new OutputStream[] { this.m_traceOutputStream, SystemUtils.m_captureOutStream, this.m_triggeredOutputStream };
/*     */     }
/*     */ 
/*  97 */     this.m_forkedOutput = new ForkedOutputStream(streams);
/*  98 */     this.m_output = new PrintStream(this.m_forkedOutput);
/*     */   }
/*     */ 
/*     */   public OutputStream createRollingOutputStream(Map<String, String> settings, String prefix)
/*     */   {
/* 103 */     String outDir = (String)settings.get(prefix + "Directory");
/* 104 */     if (outDir == null)
/*     */     {
/* 106 */       String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", prefix + "Directory");
/*     */ 
/* 108 */       throw new NullPointerException(msg);
/*     */     }
/*     */     try
/*     */     {
/* 112 */       FileUtils.checkOrCreateDirectory(outDir, 2);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 118 */       trace(null, 6000, "Unable to check/create output directory:" + outDir, null);
/*     */     }
/* 120 */     int flags = 2;
/* 121 */     if (StringUtils.convertToBool(getSetting(settings, prefix, "FileRotation"), false))
/*     */     {
/* 123 */       flags |= 1;
/*     */     }
/* 125 */     if (StringUtils.convertToBool(getSetting(settings, prefix, "UseCurrentTimestamp"), false))
/*     */     {
/* 127 */       flags |= 4;
/*     */     }
/* 129 */     if (StringUtils.convertToBool(getSetting(settings, prefix, "Use8601Timestamp"), false))
/*     */     {
/* 131 */       flags |= 8;
/*     */     }
/* 133 */     if (StringUtils.convertToBool(getSetting(settings, prefix, "UseUTCTimestamp"), false))
/*     */     {
/* 135 */       flags |= 16;
/*     */     }
/* 137 */     if (StringUtils.convertToBool(getSetting(settings, prefix, "SleepToEnforceSize"), false))
/*     */     {
/* 139 */       flags |= 32;
/*     */     }
/*     */ 
/* 143 */     String outputPrefix = (String)settings.get("IdcProductName") + "_";
/* 144 */     String clusterNodeName = (String)settings.get("IDC_Id");
/* 145 */     if (clusterNodeName != null)
/*     */     {
/* 147 */       outputPrefix = outputPrefix + clusterNodeName + "_";
/*     */     }
/* 149 */     String suffix = ".log";
/* 150 */     RollingFileOutputStream r = new RollingFileOutputStream(outDir, outputPrefix, suffix, flags);
/*     */ 
/* 152 */     String maxSizeString = getSetting(settings, prefix, "FileSizeLimit");
/* 153 */     r.m_maxSize = NumberUtils.parseLong(maxSizeString, r.m_maxSize);
/*     */ 
/* 155 */     String maxFilesString = getSetting(settings, prefix, "FileCountLimit");
/* 156 */     r.m_maxFiles = (int)NumberUtils.parseLong(maxFilesString, r.m_maxFiles);
/*     */ 
/* 158 */     String maxAge = getSetting(settings, prefix, "FileAgeLimit");
/* 159 */     r.m_maxAge = (int)NumberUtils.parseLong(maxAge, r.m_maxAge);
/*     */ 
/* 161 */     String maxIdle = getSetting(settings, prefix, "FileIdleLimit");
/* 162 */     r.m_maxIdle = (int)NumberUtils.parseLong(maxIdle, r.m_maxIdle);
/*     */ 
/* 164 */     return r;
/*     */   }
/*     */ 
/*     */   protected String getSetting(Map<String, String> settings, String prefix, String setting)
/*     */   {
/* 169 */     String value = (String)settings.get(prefix + setting);
/* 170 */     if (value == null)
/*     */     {
/* 172 */       value = (String)settings.get(setting);
/*     */     }
/* 174 */     return value;
/*     */   }
/*     */ 
/*     */   public void setTriggerOutputStream(Map<String, String> settings)
/*     */   {
/* 179 */     OutputStream out = createRollingOutputStream(settings, "Event");
/* 180 */     TriggerOutputStream tos = new TriggerOutputStream();
/* 181 */     tos.init(SystemUtils.m_captureOutStream, out);
/* 182 */     String trigger = (String)settings.get("EventFileTrigger");
/* 183 */     if (trigger == null)
/*     */     {
/* 185 */       trigger = (String)settings.get("TraceFileTrigger");
/* 186 */       Report.deprecatedUsage("Legacy setting TraceFileTrigger used");
/*     */     }
/* 188 */     tos.setTrigger(trigger);
/*     */ 
/* 191 */     FileUtils.closeObject(this.m_triggeredOutputStream);
/*     */ 
/* 193 */     this.m_triggeredOutputStream = tos;
/*     */   }
/*     */ 
/*     */   public TraceSection makeSectionData(String section)
/*     */   {
/* 198 */     return new TraceSection(section);
/*     */   }
/*     */ 
/*     */   public void trace(TraceSection section, int level, String message, Map params)
/*     */   {
/* 208 */     traceWithOptions(section, level, message, null, params, true);
/*     */   }
/*     */ 
/*     */   public void traceWithDate(TraceSection section, int level, String message, Date d, Map params)
/*     */   {
/* 218 */     traceWithOptions(section, level, message, d, params, true);
/*     */   }
/*     */ 
/*     */   public void traceDirectToOutput(TraceSection section, int level, String message, Map params)
/*     */   {
/* 228 */     traceWithOptions(section, level, message, null, params, false);
/*     */   }
/*     */ 
/*     */   public void traceWithOptions(TraceSection section, int level, String message, Date d, Map params, boolean isPrepend)
/*     */   {
/* 234 */     if (!shouldOutput(section, message))
/*     */       return;
/* 236 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 237 */     buf.m_disableToStringReleaseBuffers = true;
/* 238 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/* 239 */     appendTraceMessage(sbos, section, level, message, d, isPrepend);
/* 240 */     if (this.m_traceParameters.m_alwaysIncludeStack)
/*     */     {
/* 242 */       appendStackTrace(sbos, new StackTrace());
/*     */     }
/*     */ 
/* 245 */     if (this.m_triggeredOutputStream != null)
/*     */     {
/* 247 */       String tmpMsg = buf.toString();
/* 248 */       boolean isTriggered = this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/* 249 */       if ((isTriggered) && (this.m_triggerThreadDump))
/*     */       {
/* 251 */         dumpThreadInfo();
/* 252 */         this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/*     */       }
/*     */     }
/*     */ 
/* 256 */     if (isPrepend)
/*     */     {
/* 258 */       outln(buf.m_charArray, 0, buf.m_length);
/*     */     }
/*     */     else
/*     */     {
/* 262 */       out(buf.m_charArray, 0, buf.m_length);
/*     */     }
/*     */ 
/* 265 */     buf.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public boolean shouldOutput(TraceSection section, String message)
/*     */   {
/* 275 */     String serviceName = IdcThreadLocalUtils.get("IdcServiceName");
/* 276 */     if (!this.m_reportHandler.isActiveSection(serviceName, "Services"))
/*     */     {
/* 278 */       return false;
/*     */     }
/*     */ 
/* 281 */     String threadName = Thread.currentThread().getName();
/* 282 */     if (!this.m_reportHandler.isActiveSection(threadName, "Threads"))
/*     */     {
/* 284 */       return false;
/*     */     }
/*     */ 
/* 287 */     if (section == null)
/*     */     {
/* 289 */       return true;
/*     */     }
/*     */ 
/* 292 */     String substring = section.m_substring;
/* 293 */     Pattern regex = section.m_regex;
/*     */ 
/* 296 */     if (substring != null)
/*     */     {
/* 298 */       boolean isMatch = message.indexOf(substring) > -1;
/* 299 */       if (isMatch != section.m_outputMatches)
/*     */       {
/* 301 */         return false;
/*     */       }
/*     */     }
/* 304 */     else if (regex != null)
/*     */     {
/* 306 */       boolean isMatch = regex.matcher(message).matches();
/* 307 */       if (isMatch != section.m_outputMatches)
/*     */       {
/* 309 */         return false;
/*     */       }
/*     */     }
/* 312 */     return true;
/*     */   }
/*     */ 
/*     */   public void traceBytes(TraceSection section, int level, byte[] b, int start, int len, Map params)
/*     */   {
/* 321 */     this.m_output.write(b, start, len);
/*     */   }
/*     */ 
/*     */   public void appendStackTrace(OutputStream out, Throwable t)
/*     */   {
/* 329 */     if (t == null)
/*     */     {
/* 331 */       if (!this.m_traceParameters.m_alwaysIncludeStack)
/*     */       {
/* 333 */         return;
/*     */       }
/* 335 */       t = new StackTrace();
/*     */     }
/* 337 */     if (this.m_traceParameters.m_dumpExceptionFull)
/*     */     {
/* 339 */       Throwable newT = new StackTrace();
/* 340 */       SystemUtils.setExceptionCause(newT, t);
/* 341 */       t = newT;
/*     */     }
/* 343 */     PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
/* 344 */     if ((t instanceof StackTrace) && (out instanceof StringBufferOutputStream))
/*     */     {
/* 346 */       StringBufferOutputStream sbos = (StringBufferOutputStream)out;
/* 347 */       sbos.m_skipUntil = '\t';
/*     */     }
/* 349 */     TraceUtils.printStackTrace(t, "", writer);
/*     */   }
/*     */ 
/*     */   public void appendStackTrace(StringBuffer buf, Throwable t)
/*     */   {
/* 357 */     if (t == null)
/*     */     {
/* 359 */       if (!this.m_traceParameters.m_alwaysIncludeStack)
/*     */       {
/* 361 */         return;
/*     */       }
/* 363 */       t = new StackTrace();
/*     */     }
/* 365 */     if (this.m_traceParameters.m_dumpExceptionFull)
/*     */     {
/* 367 */       Throwable newT = new Throwable();
/* 368 */       SystemUtils.setExceptionCause(newT, t);
/* 369 */       t = newT;
/*     */     }
/* 371 */     PrintWriter writer = new PrintWriter(new OutputStreamWriter(new StringBufferOutputStream(buf)));
/*     */ 
/* 373 */     TraceUtils.printStackTrace(t, "", writer);
/*     */   }
/*     */ 
/*     */   public void dumpException(TraceSection section, int level, Throwable t, Map params)
/*     */   {
/* 382 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 383 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/* 384 */     buf.m_disableToStringReleaseBuffers = true;
/*     */     String msg;
/*     */     String msg;
/* 386 */     if (t.getMessage() == null)
/*     */     {
/* 388 */       msg = "";
/*     */     }
/*     */     else
/*     */     {
/*     */       String msg;
/* 390 */       if (t instanceof StackTrace)
/*     */       {
/* 392 */         msg = t.getMessage() + " informational backtrace:";
/*     */       }
/*     */       else
/*     */       {
/*     */         String msg;
/* 394 */         if (t instanceof Error)
/*     */         {
/* 396 */           msg = t.getMessage() + " error backtrace:";
/*     */         }
/*     */         else
/*     */         {
/* 400 */           msg = t.getMessage() + " exception backtrace:";
/*     */         }
/*     */       }
/*     */     }
/* 402 */     appendTraceMessage(sbos, section, level, msg, null, true);
/* 403 */     appendStackTrace(sbos, t);
/*     */ 
/* 406 */     if (this.m_triggeredOutputStream != null)
/*     */     {
/* 408 */       String tmpMsg = buf.toString();
/* 409 */       boolean isTriggered = this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/* 410 */       if ((isTriggered) && (this.m_triggerThreadDump))
/*     */       {
/* 412 */         dumpThreadInfo();
/* 413 */         this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/*     */       }
/*     */     }
/* 416 */     out(buf.m_charArray, 0, buf.m_length);
/* 417 */     buf.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public void dumpThreadInfo()
/*     */   {
/* 422 */     List threadDump = ThreadInfoUtils.retrieveCurrentThreadDump(true);
/* 423 */     IdcStringBuilder builder = new IdcStringBuilder();
/*     */ 
/* 425 */     builder.append("TotalThread: " + threadDump.size() + "\r\n");
/* 426 */     builder.append("______________________________________________________\r\n");
/* 427 */     for (List row : threadDump)
/*     */     {
/* 429 */       builder.append((String)row.get(0));
/* 430 */       builder.append('(');
/* 431 */       builder.append((String)row.get(1));
/* 432 */       builder.append(")\r\n");
/* 433 */       builder.append((String)row.get(2));
/* 434 */       builder.append("\r\n");
/* 435 */       builder.append((String)row.get(3));
/* 436 */       builder.append("\r\n");
/*     */     }
/* 438 */     builder.append("______________________________________________________\r\n");
/* 439 */     this.m_output.println(builder.toString());
/*     */   }
/*     */ 
/*     */   public void traceDumpException(TraceSection section, int level, String message, Throwable t, Map params)
/*     */   {
/* 449 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 450 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/* 451 */     buf.m_disableToStringReleaseBuffers = true;
/* 452 */     if (message == null)
/*     */     {
/* 454 */       message = "<Internal> ";
/*     */     }
/* 456 */     appendTraceMessage(sbos, section, level, message, null, true);
/* 457 */     buf.append(' ');
/* 458 */     appendStackTrace(sbos, t);
/*     */ 
/* 461 */     if (this.m_triggeredOutputStream != null)
/*     */     {
/* 463 */       String tmpMsg = buf.toString();
/* 464 */       boolean isTriggered = this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/* 465 */       if ((isTriggered) && (this.m_triggerThreadDump))
/*     */       {
/* 467 */         dumpThreadInfo();
/* 468 */         this.m_triggeredOutputStream.checkTrigger(tmpMsg);
/*     */       }
/*     */     }
/* 471 */     out(buf.m_charArray, 0, buf.m_length);
/* 472 */     buf.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public void appendTraceMessage(IdcAppendable buf, TraceSection section, int level, String message, Date d, boolean fullTraceInfo)
/*     */   {
/* 483 */     String name = "";
/* 484 */     if (section != null) {
/* 485 */       name = section.m_name;
/*     */     }
/* 487 */     if (fullTraceInfo)
/*     */     {
/* 489 */       buf.append('>');
/* 490 */       if ((this.m_tracePrefix != null) && (this.m_tracePrefix.length() > 0))
/*     */       {
/* 492 */         buf.append(this.m_tracePrefix);
/* 493 */         buf.append(": ");
/*     */       }
/* 495 */       if (name.length() > 0)
/*     */       {
/* 497 */         buf.append(section.m_name);
/*     */       }
/*     */       else
/*     */       {
/* 501 */         buf.append("(internal)");
/*     */       }
/* 503 */       buf.append('/');
/* 504 */       buf.append("" + level / 1000);
/* 505 */       buf.append('\t');
/* 506 */       if (!this.m_traceParameters.m_traceWithoutTimestamp)
/*     */       {
/* 508 */         if (d == null)
/*     */         {
/* 510 */           d = new Date();
/*     */         }
/* 512 */         String sd = formatDate(d);
/* 513 */         buf.append(sd);
/* 514 */         buf.append("\t");
/*     */       }
/* 516 */       buf.append(getThreadId());
/* 517 */       buf.append("\t");
/* 518 */       if (buf instanceof StringBufferOutputStream)
/*     */       {
/* 520 */         StringBufferOutputStream sout = (StringBufferOutputStream)buf;
/* 521 */         IdcStringBuilder builder = sout.getBuilder();
/* 522 */         char[] tempBuf = new char[builder.m_length];
/* 523 */         builder.getChars(0, builder.m_length, tempBuf, 0);
/*     */ 
/* 526 */         tempBuf[0] = ' ';
/* 527 */         sout.setPrefixBuffer(tempBuf, 0, tempBuf.length);
/*     */       }
/*     */     }
/* 530 */     if (message != null)
/*     */     {
/* 532 */       buf.append(message);
/*     */     }
/*     */     else
/*     */     {
/* 536 */       buf.append("<null trace message>");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getThreadId()
/*     */   {
/* 542 */     return SystemUtils.getCurrentReportingThreadID(0);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void appendTraceMessage(StringBuffer buf, TraceSection section, String message, Date d, boolean fullTraceInfo)
/*     */   {
/* 553 */     String name = "";
/* 554 */     if (section != null) {
/* 555 */       name = section.m_name;
/*     */     }
/* 557 */     if ((fullTraceInfo) && (this.m_tracePrefix != null) && (this.m_tracePrefix.length() > 0))
/*     */     {
/* 559 */       buf.append(this.m_tracePrefix);
/* 560 */       buf.append(": ");
/*     */     }
/* 562 */     if (fullTraceInfo)
/*     */     {
/* 564 */       if ((this.m_tracePrefix != null) && (this.m_tracePrefix.length() > 0))
/*     */       {
/* 566 */         buf.append(this.m_tracePrefix);
/* 567 */         buf.append(": ");
/*     */       }
/* 569 */       if (name.length() > 0)
/*     */       {
/* 571 */         buf.append(section.m_name);
/* 572 */         buf.append("\t");
/*     */       }
/*     */       else
/*     */       {
/* 576 */         buf.append("(internal)\t");
/*     */       }
/* 578 */       if (!this.m_traceParameters.m_traceWithoutTimestamp)
/*     */       {
/* 580 */         if (d == null)
/*     */         {
/* 582 */           d = new Date();
/*     */         }
/* 584 */         String sd = formatDate(d);
/* 585 */         buf.append(sd);
/* 586 */         buf.append("\t");
/*     */       }
/* 588 */       buf.append(getThreadId());
/* 589 */       buf.append("\t");
/*     */     }
/* 591 */     if (message != null)
/*     */     {
/* 593 */       buf.append(message);
/*     */     }
/*     */     else
/*     */     {
/* 597 */       buf.append("<null trace message>");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String formatDate(Date d)
/*     */   {
/* 603 */     return LocaleUtils.debugDate(d);
/*     */   }
/*     */ 
/*     */   public void outln(char[] message, int start, int length)
/*     */   {
/* 612 */     int[] lengthBuf = new int[1];
/* 613 */     byte[] b = StringUtils.convertCharsToBytesSafe(message, start, length, null, lengthBuf, 1);
/*     */ 
/* 615 */     this.m_output.write(b, 0, lengthBuf[0]);
/*     */   }
/*     */ 
/*     */   public void outln(String message)
/*     */   {
/* 624 */     int[] lengthBuf = new int[1];
/* 625 */     char[] chars = new char[message.length()];
/* 626 */     message.getChars(0, chars.length, chars, 0);
/* 627 */     byte[] b = StringUtils.convertCharsToBytesSafe(chars, 0, chars.length, null, lengthBuf, 1);
/*     */ 
/* 629 */     this.m_output.write(b, 0, lengthBuf[0]);
/*     */   }
/*     */ 
/*     */   public void out(char[] message, int start, int length)
/*     */   {
/* 638 */     int[] lengthBuf = new int[1];
/* 639 */     byte[] b = StringUtils.convertCharsToBytesSafe(message, start, length, null, lengthBuf, 0);
/* 640 */     this.m_output.write(b, 0, lengthBuf[0]);
/*     */   }
/*     */ 
/*     */   public void out(String message)
/*     */   {
/* 649 */     int[] lengthBuf = new int[1];
/* 650 */     char[] chars = new char[message.length()];
/* 651 */     message.getChars(0, chars.length, chars, 0);
/* 652 */     byte[] b = StringUtils.convertCharsToBytesSafe(chars, 0, chars.length, null, lengthBuf, 0);
/*     */ 
/* 654 */     this.m_output.write(b, 0, lengthBuf[0]);
/*     */   }
/*     */ 
/*     */   public void setParameter(String key, Object value)
/*     */   {
/* 662 */     this.m_traceParameters.setParameter(key, value);
/*     */   }
/*     */ 
/*     */   public Object getParameter(String key)
/*     */   {
/* 667 */     return this.m_traceParameters.getParameter(key);
/*     */   }
/*     */ 
/*     */   public String getStringParameter(String key, String defValue)
/*     */   {
/* 672 */     return this.m_traceParameters.getStringParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public int getIntegerParameter(String key, int defValue)
/*     */   {
/* 677 */     return this.m_traceParameters.getIntegerParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public boolean getBooleanParameter(String key, boolean defValue)
/*     */   {
/* 682 */     return this.m_traceParameters.getBooleanParameter(key, defValue);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 687 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105081 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DefaultTraceImplementor
 * JD-Core Version:    0.5.4
 */