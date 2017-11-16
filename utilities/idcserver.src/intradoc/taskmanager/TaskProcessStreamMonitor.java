/*     */ package intradoc.taskmanager;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TaskProcessStreamMonitor
/*     */   implements Runnable
/*     */ {
/*     */   protected Process m_process;
/*     */   protected int m_processId;
/*     */   protected boolean m_isErrorThread;
/*     */   protected boolean m_maintainProc;
/*     */   protected Vector m_cmdLineOptions;
/*     */   protected byte[][] m_eods;
/*     */   protected String m_traceSubject;
/*     */   protected volatile boolean m_finishedReading;
/*     */   protected volatile boolean m_abortRequested;
/*     */   protected volatile boolean m_hasNew;
/*     */   protected long m_lastReadTime;
/*     */   protected IdcStringBuilder m_buffer;
/*     */   protected ServiceException m_error;
/*     */   protected BufferedInputStream m_inputStream;
/*     */   protected boolean m_hasOutput;
/*     */   protected Object m_lock;
/*     */ 
/*     */   public TaskProcessStreamMonitor()
/*     */   {
/*  31 */     this.m_processId = 0;
/*     */ 
/*  35 */     this.m_maintainProc = false;
/*     */ 
/*  41 */     this.m_finishedReading = false;
/*  42 */     this.m_abortRequested = false;
/*  43 */     this.m_hasNew = false;
/*     */ 
/*  47 */     this.m_error = null;
/*     */ 
/*  49 */     this.m_hasOutput = false;
/*     */   }
/*     */ 
/*     */   public void init(TaskLauncher launcher, Process runningProcess, boolean isErrorThread, boolean maintainProc, Object lock)
/*     */     throws ServiceException
/*     */   {
/*  56 */     this.m_process = runningProcess;
/*  57 */     this.m_isErrorThread = isErrorThread;
/*  58 */     this.m_maintainProc = maintainProc;
/*  59 */     this.m_lock = lock;
/*  60 */     this.m_traceSubject = launcher.getTraceSubject();
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  65 */     while ((!this.m_abortRequested) && (!SystemUtils.m_isServerStopped)) {
/*     */       while (true) {
/*  67 */         if (!this.m_hasNew)
/*     */         {
/*     */           try
/*     */           {
/*  71 */             synchronized (this)
/*     */             {
/*  73 */               super.wait(5000L);
/*     */             }
/*     */           }
/*     */           catch (InterruptedException e)
/*     */           {
/*  78 */             Report.trace("taskmanager", null, e);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*  83 */         this.m_hasNew = false;
/*     */ 
/*  85 */         if (!this.m_isErrorThread)
/*     */           break;
/*  87 */         runStdErrThread();
/*     */       }
/*     */ 
/*  91 */       runStdOutThread();
/*     */     }
/*     */ 
/*  97 */     if (this.m_hasNew)
/*     */     {
/*  99 */       if (this.m_isErrorThread)
/*     */       {
/* 101 */         runStdErrThread();
/*     */       }
/*     */       else
/*     */       {
/* 105 */         runStdOutThread();
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 111 */       if (this.m_inputStream != null)
/*     */       {
/* 113 */         this.m_inputStream.close();
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 118 */       if (!SystemUtils.m_verbose) {
/*     */         return;
/*     */       }
/* 121 */       Report.debug("taskmanager", null, t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setCommandOptions(Vector commandLineOptions, byte[][] eods)
/*     */   {
/* 128 */     this.m_cmdLineOptions = commandLineOptions;
/* 129 */     this.m_eods = eods;
/*     */   }
/*     */ 
/*     */   public void markNewWork()
/*     */   {
/* 135 */     synchronized (this)
/*     */     {
/* 137 */       this.m_hasNew = true;
/* 138 */       if (this.m_buffer != null)
/*     */       {
/* 140 */         this.m_buffer.releaseBuffers();
/*     */       }
/* 142 */       this.m_buffer = new IdcStringBuilder();
/* 143 */       this.m_hasOutput = false;
/* 144 */       this.m_error = null;
/* 145 */       super.notifyAll();
/*     */     }
/* 147 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 149 */     Report.debug("taskmanager", "Notified of new work", null);
/*     */   }
/*     */ 
/*     */   public void abort()
/*     */   {
/* 155 */     this.m_abortRequested = true;
/* 156 */     synchronized (this)
/*     */     {
/* 158 */       super.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean hasNewTask()
/*     */   {
/* 164 */     return this.m_hasNew;
/*     */   }
/*     */ 
/*     */   public boolean isFinished()
/*     */   {
/* 169 */     return this.m_finishedReading;
/*     */   }
/*     */ 
/*     */   public void setFinished(boolean finished)
/*     */   {
/* 174 */     this.m_finishedReading = finished;
/*     */   }
/*     */ 
/*     */   public long getLastReadTime()
/*     */   {
/* 179 */     return this.m_lastReadTime;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getErrorMessage()
/*     */   {
/* 186 */     IdcMessage msg = IdcMessageFactory.lc(this.m_error);
/* 187 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public ServiceException getError()
/*     */   {
/* 192 */     return this.m_error;
/*     */   }
/*     */ 
/*     */   public String getOutput()
/*     */   {
/* 197 */     return this.m_buffer.toStringNoRelease();
/*     */   }
/*     */ 
/*     */   public boolean hasOutput()
/*     */   {
/* 202 */     return this.m_hasOutput;
/*     */   }
/*     */ 
/*     */   public int getProcessId()
/*     */   {
/* 207 */     return this.m_processId;
/*     */   }
/*     */ 
/*     */   protected void runStdOutThread()
/*     */   {
/*     */     try
/*     */     {
/* 214 */       if (SystemUtils.m_verbose)
/*     */       {
/* 216 */         Report.debug("taskmanager", "start output", null);
/*     */       }
/* 218 */       if (this.m_inputStream == null)
/*     */       {
/* 220 */         InputStream tmpProcIn = this.m_process.getInputStream();
/* 221 */         if (tmpProcIn instanceof BufferedInputStream)
/*     */         {
/* 223 */           this.m_inputStream = ((BufferedInputStream)tmpProcIn);
/*     */         }
/*     */         else
/*     */         {
/* 227 */           this.m_inputStream = new BufferedInputStream(tmpProcIn);
/*     */         }
/*     */       }
/*     */ 
/* 231 */       this.m_finishedReading = false;
/*     */ 
/* 233 */       OutputStream procOut = this.m_process.getOutputStream();
/* 234 */       if (this.m_maintainProc)
/*     */       {
/* 236 */         IdcStringBuilder cmdBuf = new IdcStringBuilder();
/* 237 */         if (this.m_cmdLineOptions != null)
/*     */         {
/* 239 */           int len = this.m_cmdLineOptions.size();
/* 240 */           for (int i = 0; i < len; ++i)
/*     */           {
/* 242 */             cmdBuf.append((String)this.m_cmdLineOptions.elementAt(i));
/* 243 */             cmdBuf.append("\n");
/*     */           }
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 249 */           byte[] cmdByteBuf = cmdBuf.toString().getBytes();
/* 250 */           procOut.write(cmdByteBuf);
/* 251 */           if (this.m_eods != null)
/*     */           {
/* 253 */             procOut.write(this.m_eods[0]);
/*     */           }
/* 255 */           procOut.write("\n".getBytes());
/* 256 */           procOut.flush();
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 260 */           String msg = LocaleUtils.encodeMessage("csTaskManagerUnableToSendCommand", e.getMessage(), cmdBuf);
/*     */ 
/* 262 */           throw new ServiceException(msg);
/*     */         }
/*     */       }
/*     */ 
/* 266 */       this.m_buffer = readInputStream(this.m_eods[1]);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 270 */       Report.trace("taskmanager", null, e);
/* 271 */       if (e instanceof ServiceException)
/*     */       {
/* 273 */         this.m_error = ((ServiceException)e);
/*     */       }
/*     */       else
/*     */       {
/* 277 */         this.m_error = new ServiceException(e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 282 */       synchronized (this.m_lock)
/*     */       {
/* 284 */         this.m_finishedReading = true;
/* 285 */         this.m_hasOutput = true;
/* 286 */         this.m_lock.notifyAll();
/*     */       }
/* 288 */       if ((this.m_buffer != null) && 
/* 291 */         (this.m_buffer.toString().length() > 0))
/*     */       {
/* 293 */         Report.trace(this.m_traceSubject, this.m_buffer.toString(), null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcStringBuilder readInputStream(byte[] eod)
/*     */     throws IOException
/*     */   {
/* 301 */     byte[] buf = new byte[8192];
/* 302 */     int nread = 0;
/* 303 */     boolean endData = true;
/* 304 */     if (this.m_maintainProc)
/*     */     {
/* 306 */       endData = false;
/* 307 */       this.m_inputStream.mark(buf.length);
/*     */     }
/* 309 */     int[] match = { 0, -1 };
/* 310 */     int available = 0;
/*     */     try
/*     */     {
/* 313 */       available = this.m_inputStream.available();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 317 */       available = 0;
/*     */ 
/* 320 */       if (this.m_abortRequested)
/*     */       {
/* 322 */         Report.debug("taskmanager", "abort requested - exiting ", null);
/*     */       }
/*     */       else
/*     */       {
/* 326 */         Report.debug("taskmanager", "error reading available bytes from input stream", e);
/*     */       }
/*     */     }
/* 329 */     while ((available >= 0) && (!this.m_abortRequested))
/*     */     {
/* 331 */       if ((this.m_maintainProc) && (available == 0))
/*     */       {
/* 334 */         if (this.m_finishedReading) {
/*     */           break;
/*     */         }
/*     */ 
/* 338 */         SystemUtils.sleep(2L);
/*     */         try
/*     */         {
/* 343 */           available = this.m_inputStream.available();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 347 */           available = 0;
/* 348 */           Report.debug("taskmanager", "error reading available bytes from input stream", e);
/*     */ 
/* 350 */           if (!this.m_abortRequested)
/*     */           {
/* 352 */             abort();
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 358 */       nread = this.m_inputStream.read(buf);
/* 359 */       if (nread < 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 363 */       this.m_lastReadTime = System.currentTimeMillis();
/* 364 */       if (SystemUtils.m_verbose)
/*     */       {
/* 366 */         String temp = new String(buf, 0, nread);
/* 367 */         Report.debug("taskmanager", "read " + nread + " bytes: " + temp, null);
/*     */       }
/* 369 */       int len = nread;
/* 370 */       if (!endData)
/*     */       {
/* 372 */         match = byteArraySearch(buf, 0, nread, eod, match);
/*     */ 
/* 374 */         if (match[1] >= 0)
/*     */         {
/* 376 */           endData = true;
/* 377 */           len = match[1] + 1;
/*     */         }
/*     */         else
/*     */         {
/* 381 */           this.m_inputStream.mark(buf.length);
/*     */         }
/*     */       }
/* 384 */       String output = new String(buf, 0, len);
/* 385 */       if (this.m_buffer.length() == 0)
/*     */       {
/* 389 */         String key = "idclaunchprocessid=";
/* 390 */         int launchIndex = output.indexOf(key);
/* 391 */         if (launchIndex >= 0)
/*     */         {
/* 393 */           String processId = output.substring(launchIndex + key.length());
/* 394 */           int endIndex = processId.indexOf("\n");
/* 395 */           if (endIndex >= 0)
/*     */           {
/* 397 */             processId = processId.substring(0, endIndex);
/*     */           }
/* 399 */           processId = processId.trim();
/* 400 */           if (processId.length() > 0)
/*     */           {
/* 402 */             this.m_processId = NumberUtils.parseInteger(processId, this.m_processId);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 411 */       if (len > 0)
/*     */       {
/* 413 */         this.m_hasOutput = true;
/*     */       }
/* 415 */       this.m_buffer.append(output);
/* 416 */       if ((endData) && 
/* 418 */         (this.m_maintainProc))
/*     */       {
/* 420 */         if (len == nread)
/*     */           break;
/* 422 */         this.m_inputStream.reset();
/* 423 */         this.m_inputStream.skip(len); break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 429 */     if (SystemUtils.m_verbose)
/*     */     {
/* 431 */       Report.debug("taskmanager", "Finish reading.", null);
/*     */     }
/* 433 */     return this.m_buffer;
/*     */   }
/*     */ 
/*     */   protected void runStdErrThread()
/*     */   {
/*     */     try
/*     */     {
/* 440 */       if (this.m_inputStream == null)
/*     */       {
/* 442 */         this.m_inputStream = new BufferedInputStream(this.m_process.getErrorStream());
/*     */       }
/*     */ 
/* 445 */       this.m_finishedReading = false;
/* 446 */       readInputStream(this.m_eods[2]);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 450 */       Report.debug(null, null, ignore);
/*     */     }
/*     */     finally
/*     */     {
/* 454 */       synchronized (this.m_lock)
/*     */       {
/* 456 */         this.m_finishedReading = true;
/* 457 */         this.m_lock.notifyAll();
/*     */       }
/* 459 */       if ((this.m_buffer != null) && 
/* 462 */         (this.m_buffer.toString().length() > 0))
/*     */       {
/* 464 */         Report.trace("taskmanager", this.m_buffer.toString(), null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected int[] byteArraySearch(byte[] src, int beginIndex, int endIndex, byte[] searchArray, int[] match)
/*     */   {
/* 481 */     for (int i = beginIndex; i < endIndex; ++i)
/*     */     {
/* 483 */       if (src[i] == searchArray[match[0]])
/*     */       {
/* 485 */         match[0] += 1;
/*     */       }
/*     */       else
/*     */       {
/* 489 */         match[0] = 0;
/*     */       }
/*     */ 
/* 492 */       if (match[0] != searchArray.length)
/*     */         continue;
/* 494 */       match[1] = i;
/* 495 */       break;
/*     */     }
/*     */ 
/* 498 */     return match;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 502 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90218 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.taskmanager.TaskProcessStreamMonitor
 * JD-Core Version:    0.5.4
 */