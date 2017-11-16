/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ExecuteProcessCommand
/*     */ {
/*  32 */   protected Process m_process = null;
/*  33 */   protected OutputStream m_outStream = null;
/*  34 */   protected InputStream m_inStream = null;
/*  35 */   protected int m_timeout = 30000;
/*  36 */   protected boolean m_isDone = false;
/*  37 */   protected boolean m_stopBackground = false;
/*  38 */   protected boolean m_failed = false;
/*  39 */   protected boolean m_processIsDone = false;
/*  40 */   protected boolean m_isActive = false;
/*  41 */   protected InputStream m_errorStream = null;
/*  42 */   protected String m_bgExceptionMsg = null;
/*  43 */   protected String m_exePath = null;
/*  44 */   protected Vector m_commandLineParams = null;
/*  45 */   protected String m_curCommand = null;
/*  46 */   protected Vector m_resultsParam = null;
/*     */ 
/*  48 */   protected boolean m_isTrace = false;
/*     */   static final String EOB_STR = "\n<<EOB>>";
/*     */   static final String EOD_STR = "\n<<EOD>>";
/*  53 */   protected byte[] m_eob = null;
/*  54 */   protected byte[] m_eod = null;
/*     */ 
/*  56 */   protected String m_encoding = null;
/*     */ 
/*     */   public ExecuteProcessCommand()
/*     */   {
/*  60 */     this.m_eob = toRawBytes("\n<<EOB>>");
/*  61 */     this.m_eod = toRawBytes("\n<<EOD>>");
/*     */   }
/*     */ 
/*     */   public void setEncoding(String encoding)
/*     */   {
/*  66 */     this.m_encoding = encoding;
/*     */   }
/*     */ 
/*     */   public byte[] toRawBytes(String val)
/*     */   {
/*  71 */     byte[] b = new byte[val.length()];
/*  72 */     for (int i = 0; i < val.length(); ++i)
/*     */     {
/*  74 */       b[i] = (byte)val.charAt(i);
/*     */     }
/*  76 */     return b;
/*     */   }
/*     */ 
/*     */   public void startProcess(String exePath, Vector commandLineParams) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/*  83 */       int nparams = 0;
/*  84 */       if (commandLineParams != null)
/*     */       {
/*  86 */         nparams = commandLineParams.size();
/*     */       }
/*  88 */       Runtime run = Runtime.getRuntime();
/*  89 */       String[] cmdLine = new String[nparams + 1];
/*  90 */       cmdLine[0] = exePath;
/*  91 */       for (int i = 1; i < nparams + 1; ++i)
/*     */       {
/*  93 */         cmdLine[i] = ((String)commandLineParams.elementAt(i - 1));
/*     */       }
/*  95 */       this.m_exePath = exePath;
/*  96 */       this.m_commandLineParams = commandLineParams;
/*  97 */       if (SystemUtils.isActiveTrace("search"))
/*     */       {
/*  99 */         IdcStringBuilder tmp = new IdcStringBuilder("starting command ");
/* 100 */         for (int i = 0; i < cmdLine.length; ++i)
/*     */         {
/* 102 */           if (i > 0)
/*     */           {
/* 104 */             tmp.append(' ');
/*     */           }
/* 106 */           tmp.append(cmdLine[i]);
/*     */         }
/* 108 */         Report.trace("search", tmp.toString(), null);
/*     */       }
/* 110 */       this.m_process = run.exec(cmdLine);
/* 111 */       this.m_errorStream = this.m_process.getErrorStream();
/* 112 */       this.m_outStream = this.m_process.getOutputStream();
/* 113 */       this.m_inStream = this.m_process.getInputStream();
/* 114 */       this.m_processIsDone = false;
/* 115 */       this.m_isActive = true;
/*     */ 
/* 118 */       Thread errThread = new Thread()
/*     */       {
/*     */         public void run()
/*     */         {
/*     */           try
/*     */           {
/* 125 */             byte[] errbuf = new byte[1024];
/*     */ 
/* 127 */             InputStream errStream = ExecuteProcessCommand.this.m_errorStream;
/* 128 */             while ((errStream != null) && (ExecuteProcessCommand.this.isRunning()) && ((count = errStream.read(errbuf)) > 0))
/*     */             {
/*     */               int count;
/* 130 */               String t = new String(errbuf, 0, count, ExecuteProcessCommand.this.m_encoding);
/* 131 */               ExecuteProcessCommand.this.traceMessage(t);
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/* 136 */             Report.trace("search", null, ignore);
/*     */           }
/*     */ 
/* 139 */           ExecuteProcessCommand.this.m_isActive = false;
/*     */         }
/*     */       };
/* 143 */       errThread.start();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 148 */       throw new ServiceException("Unable to start executable " + this.m_exePath + ".", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isProcessActive()
/*     */   {
/* 155 */     return this.m_isActive;
/*     */   }
/*     */ 
/*     */   public void executeCommand(String command, int timeout, Vector results, boolean isLast) throws ServiceException
/*     */   {
/* 160 */     if (isLast)
/*     */     {
/* 162 */       command = command + "\n<<EOF>>\n";
/*     */     }
/*     */     else
/*     */     {
/* 166 */       command = command + "\n<<EOD>>\n";
/*     */     }
/* 168 */     this.m_curCommand = command;
/*     */ 
/* 170 */     this.m_timeout = timeout;
/* 171 */     this.m_isDone = false;
/*     */ 
/* 175 */     this.m_resultsParam = results;
/*     */ 
/* 177 */     Runnable run = new Object()
/*     */     {
/*     */       public void run()
/*     */       {
/*     */         try
/*     */         {
/* 183 */           ExecuteProcessCommand.this.traceMessage("stdout packet\n" + ExecuteProcessCommand.this.m_curCommand);
/* 184 */           byte[] cmdBuf = ExecuteProcessCommand.this.m_curCommand.getBytes(ExecuteProcessCommand.this.m_encoding);
/*     */           try
/*     */           {
/* 187 */             ExecuteProcessCommand.this.m_outStream.write(cmdBuf);
/* 188 */             ExecuteProcessCommand.this.m_outStream.flush();
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 192 */             throw new ServiceException("Unable to send command to search executable.");
/*     */           }
/*     */ 
/* 195 */           byte[] buf = new byte[2000];
/* 196 */           int nread = 0;
/* 197 */           boolean endData = false;
/* 198 */           int matchCount = 0;
/* 199 */           int start = 0;
/* 200 */           int curOffset = 0;
/* 201 */           boolean matchEodOnly = false;
/*     */ 
/* 203 */           while ((ExecuteProcessCommand.this.isRunning()) && ((nread = ExecuteProcessCommand.this.m_inStream.read(buf, curOffset, buf.length - curOffset)) > 0))
/*     */           {
/* 205 */             if (ExecuteProcessCommand.this.m_stopBackground) {
/*     */               return;
/*     */             }
/*     */ 
/* 209 */             int end = curOffset + nread;
/* 210 */             for (int i = curOffset; i < end; ++i)
/*     */             {
/* 212 */               if ((!matchEodOnly) && (ExecuteProcessCommand.this.m_eob[matchCount] == buf[i]))
/*     */               {
/* 214 */                 ++matchCount;
/*     */               }
/* 216 */               else if (ExecuteProcessCommand.this.m_eod[matchCount] == buf[i])
/*     */               {
/* 218 */                 matchEodOnly = true;
/* 219 */                 ++matchCount;
/*     */               }
/*     */               else
/*     */               {
/* 223 */                 matchCount = 0;
/* 224 */                 matchEodOnly = false;
/* 225 */                 if (ExecuteProcessCommand.this.m_eob[matchCount] == buf[i])
/*     */                 {
/* 227 */                   ++matchCount;
/*     */                 }
/*     */               }
/* 230 */               if (matchCount != ExecuteProcessCommand.this.m_eob.length) {
/*     */                 continue;
/*     */               }
/* 233 */               String val = new String(buf, start, i - matchCount - start, ExecuteProcessCommand.this.m_encoding);
/* 234 */               ExecuteProcessCommand.this.m_resultsParam.addElement(val);
/*     */ 
/* 236 */               ExecuteProcessCommand.this.traceMessage("stdin packet\n" + val);
/* 237 */               if (matchEodOnly)
/*     */               {
/* 239 */                 ExecuteProcessCommand.this.traceMessage("<<EOD>>");
/*     */               }
/*     */               else
/*     */               {
/* 243 */                 ExecuteProcessCommand.this.traceMessage("<<EOB>>");
/*     */               }
/*     */ 
/* 247 */               if (matchEodOnly)
/*     */               {
/* 249 */                 endData = true;
/* 250 */                 break;
/*     */               }
/* 252 */               matchCount = 0;
/* 253 */               start = i + 1;
/*     */             }
/*     */ 
/* 256 */             if (endData) {
/*     */               break;
/*     */             }
/*     */ 
/* 260 */             if (end >= buf.length)
/*     */             {
/* 262 */               byte[] tempBuf = new byte[buf.length * 2];
/* 263 */               curOffset = buf.length - start;
/* 264 */               System.arraycopy(buf, start, tempBuf, 0, curOffset);
/* 265 */               String msg = "curOffset = " + curOffset + " length = " + buf.length + " start = " + start;
/*     */ 
/* 267 */               ExecuteProcessCommand.this.traceMessage(msg);
/* 268 */               start = 0;
/* 269 */               buf = tempBuf;
/*     */             }
/*     */             else
/*     */             {
/* 273 */               curOffset = end;
/*     */             }
/*     */           }
/*     */ 
/* 277 */           if (!endData)
/*     */           {
/* 279 */             ExecuteProcessCommand.this.m_failed = true;
/* 280 */             ExecuteProcessCommand.this.m_processIsDone = true;
/*     */           }
/* 282 */           ExecuteProcessCommand.this.m_isDone = true;
/*     */         }
/*     */         catch (Throwable e)
/*     */         {
/* 286 */           ExecuteProcessCommand.this.m_failed = true;
/* 287 */           ExecuteProcessCommand.this.m_isDone = true;
/* 288 */           ExecuteProcessCommand.this.m_processIsDone = true;
/* 289 */           IdcMessage idcMsg = IdcMessageFactory.lc(e);
/* 290 */           ExecuteProcessCommand.this.m_bgExceptionMsg = LocaleUtils.encodeMessage(idcMsg);
/*     */         }
/*     */         finally
/*     */         {
/* 294 */           synchronized (this)
/*     */           {
/* 296 */             ExecuteProcessCommand.this.m_isDone = true;
/* 297 */             super.notify();
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 303 */     Thread bgThread = new Thread(run);
/* 304 */     bgThread.setDaemon(true);
/* 305 */     bgThread.start();
/*     */ 
/* 307 */     long timeAllowed = this.m_timeout;
/*     */ 
/* 309 */     long startTime = System.currentTimeMillis();
/* 310 */     boolean isTimedOut = false;
/*     */ 
/* 312 */     while (!this.m_isDone)
/*     */     {
/* 314 */       long left = timeAllowed - (System.currentTimeMillis() - startTime);
/*     */ 
/* 316 */       if (left > 0L)
/*     */       {
/* 318 */         synchronized (run)
/*     */         {
/* 320 */           if (!this.m_isDone)
/*     */           {
/*     */             try
/*     */             {
/* 324 */               run.wait(left);
/*     */             }
/*     */             catch (Throwable ignore)
/*     */             {
/* 328 */               Report.trace(null, null, ignore);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 335 */         this.m_failed = true;
/* 336 */         this.m_isDone = true;
/* 337 */         isTimedOut = true;
/*     */       }
/*     */ 
/* 340 */       if (this.m_bgExceptionMsg != null)
/*     */       {
/* 343 */         String msg = LocaleUtils.encodeMessage("csVeritySrchExeErrorWaiting", null, this.m_exePath, this.m_bgExceptionMsg);
/*     */ 
/* 345 */         throw new ServiceException(msg);
/*     */       }
/*     */     }
/*     */ 
/* 349 */     if ((this.m_failed) || (results.size() == 0))
/*     */     {
/* 351 */       this.m_stopBackground = true;
/* 352 */       clearExe();
/*     */ 
/* 354 */       String errMsg = null;
/* 355 */       if (isTimedOut)
/*     */       {
/* 357 */         errMsg = LocaleUtils.encodeMessage("csVeritySrchExeTimeOut", null, this.m_exePath);
/*     */       }
/*     */       else
/*     */       {
/* 362 */         errMsg = LocaleUtils.encodeMessage("csVeritySrchExeAborted", null, this.m_exePath);
/*     */       }
/*     */ 
/* 365 */       throw new ServiceException(errMsg);
/*     */     }
/* 367 */     if (isRunning())
/*     */       return;
/* 369 */     clearExe();
/*     */   }
/*     */ 
/*     */   public synchronized void clearExe()
/*     */   {
/* 375 */     if (this.m_process == null)
/*     */       return;
/* 377 */     this.m_processIsDone = true;
/* 378 */     this.m_process.destroy();
/* 379 */     this.m_process = null;
/* 380 */     closeStream(this.m_inStream);
/* 381 */     this.m_inStream = null;
/* 382 */     closeStream(this.m_outStream);
/* 383 */     this.m_outStream = null;
/* 384 */     closeStream(this.m_errorStream);
/* 385 */     this.m_errorStream = null;
/*     */   }
/*     */ 
/*     */   public void closeStream(Object obj)
/*     */   {
/* 391 */     if (obj == null)
/*     */     {
/* 393 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 397 */       if (obj instanceof InputStream)
/*     */       {
/* 399 */         ((InputStream)obj).close();
/*     */       }
/* 401 */       else if (obj instanceof OutputStream)
/*     */       {
/* 403 */         ((OutputStream)obj).close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 408 */       Report.trace(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized boolean isRunning()
/*     */   {
/* 414 */     return (this.m_process != null) && (!this.m_processIsDone);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void setIsTrace(boolean isTrace)
/*     */   {
/* 423 */     SystemUtils.reportDeprecatedUsage("intradoc.search.ExecuteProcessCommand.setIsTrace() is deprecated.  Enable the search tracing section.");
/*     */ 
/* 426 */     if (isTrace)
/*     */     {
/* 428 */       SystemUtils.addAsDefaultTrace("search");
/*     */     }
/* 430 */     this.m_isTrace = isTrace;
/*     */   }
/*     */ 
/*     */   public void traceMessage(String msg)
/*     */   {
/* 435 */     Report.trace("search", msg, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 440 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.ExecuteProcessCommand
 * JD-Core Version:    0.5.4
 */