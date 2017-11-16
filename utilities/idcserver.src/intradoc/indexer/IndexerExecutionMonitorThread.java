/*     */ package intradoc.indexer;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
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
/*     */ public class IndexerExecutionMonitorThread extends Thread
/*     */ {
/*     */   protected IndexerExecution m_execution;
/*     */   protected IndexerConfig m_config;
/*     */   protected NativeOsUtils m_nativeOsUtils;
/*     */   protected Process m_process;
/*     */   protected boolean m_isErrorThread;
/*     */   protected IndexerExecutionMonitorThread m_stdOutMonitor;
/*     */   protected Vector m_cmdLine;
/*     */   protected boolean m_finishedReading;
/*     */   protected StringBuffer m_buffer;
/*     */   protected String m_errorMessage;
/*     */   protected boolean m_abortRequested;
/*     */   protected BufferedInputStream m_inputStream;
/*     */   protected long m_lastReadTime;
/*     */   protected Object m_lock;
/*     */ 
/*     */   public IndexerExecutionMonitorThread()
/*     */   {
/*  46 */     this.m_finishedReading = false;
/*     */ 
/*  48 */     this.m_errorMessage = null;
/*  49 */     this.m_abortRequested = false;
/*     */   }
/*     */ 
/*     */   public void init(IndexerExecution execution, Process runningProcess, IndexerExecutionMonitorThread stdOutMonitor, Object lock)
/*     */   {
/*  58 */     this.m_execution = execution;
/*  59 */     this.m_process = runningProcess;
/*  60 */     this.m_isErrorThread = (stdOutMonitor != null);
/*  61 */     this.m_stdOutMonitor = stdOutMonitor;
/*  62 */     this.m_lock = lock;
/*     */ 
/*  64 */     this.m_config = execution.m_config;
/*  65 */     this.m_nativeOsUtils = execution.m_nativeOsUtils;
/*     */ 
/*  67 */     this.m_buffer = new StringBuffer();
/*     */   }
/*     */ 
/*     */   public void setCommand(Vector commandLine)
/*     */   {
/*  72 */     this.m_cmdLine = commandLine;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  78 */     if (this.m_isErrorThread)
/*     */     {
/*  80 */       setName("stderr");
/*  81 */       runStdErrThread();
/*     */     }
/*     */     else
/*     */     {
/*  85 */       setName("stdout");
/*  86 */       runStdOutThread();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void abort()
/*     */   {
/*  92 */     this.m_abortRequested = true;
/*     */   }
/*     */ 
/*     */   public boolean isFinished()
/*     */   {
/*  97 */     return this.m_finishedReading;
/*     */   }
/*     */ 
/*     */   public long getLastReadTime()
/*     */   {
/* 102 */     return this.m_lastReadTime;
/*     */   }
/*     */ 
/*     */   public String getErrorMessage()
/*     */   {
/* 107 */     return this.m_errorMessage;
/*     */   }
/*     */ 
/*     */   public String getOutput()
/*     */   {
/* 112 */     return this.m_buffer.toString();
/*     */   }
/*     */ 
/*     */   protected void runStdOutThread()
/*     */   {
/*     */     try
/*     */     {
/* 119 */       InputStream tmpProcIn = this.m_process.getInputStream();
/* 120 */       if (tmpProcIn instanceof BufferedInputStream)
/*     */       {
/* 122 */         this.m_inputStream = ((BufferedInputStream)tmpProcIn);
/*     */       }
/*     */       else
/*     */       {
/* 126 */         this.m_inputStream = new BufferedInputStream(tmpProcIn);
/*     */       }
/*     */ 
/* 129 */       byte[] buf = new byte[8192];
/* 130 */       int nread = 0;
/* 131 */       this.m_finishedReading = false;
/*     */ 
/* 133 */       OutputStream procOut = this.m_process.getOutputStream();
/* 134 */       if (this.m_execution.m_maintainIndexProcess)
/*     */       {
/* 136 */         if (this.m_cmdLine == null)
/*     */         {
/* 139 */           throw new ServiceException("!$IndexerExecutionMonitorThread missing command line.");
/*     */         }
/*     */ 
/* 143 */         String cmdBuf = "";
/* 144 */         int len = this.m_cmdLine.size();
/* 145 */         for (int i = 0; i < len; ++i)
/*     */         {
/* 147 */           cmdBuf = cmdBuf + (String)this.m_cmdLine.elementAt(i) + "\n";
/*     */         }
/*     */ 
/* 150 */         cmdBuf = cmdBuf + this.m_config.getValue("IndexerEOD") + "\n";
/*     */ 
/* 153 */         if ((this.m_nativeOsUtils != null) && (this.m_nativeOsUtils.isSemaphoreSupported()) && (this.m_execution.m_semaphoreHandle > 0L))
/*     */         {
/* 157 */           this.m_nativeOsUtils.releaseSemaphore(this.m_execution.m_semaphoreHandle);
/*     */         }
/*     */ 
/* 160 */         byte[] cmdByteBuf = cmdBuf.getBytes();
/* 161 */         if (this.m_process == null)
/*     */         {
/* 163 */           Report.trace("indexer", LocaleUtils.encodeMessage("csNullProcess", null), null);
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 168 */           Report.trace("indexer", cmdBuf, null);
/* 169 */           procOut.write(cmdByteBuf);
/* 170 */           procOut.flush();
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 174 */           String msg = LocaleUtils.encodeMessage("csIndexerUnableToSendCommand", e.getMessage());
/*     */ 
/* 176 */           throw new ServiceException(msg);
/*     */         }
/*     */       }
/*     */ 
/* 180 */       boolean endData = true;
/* 181 */       byte[] eod = null;
/* 182 */       if (this.m_execution.m_maintainIndexProcess)
/*     */       {
/* 184 */         endData = false;
/* 185 */         eod = this.m_config.getValue("IndexerEOD").getBytes();
/* 186 */         this.m_inputStream.mark(buf.length);
/*     */       }
/* 188 */       int[] match = { 0, -1 };
/* 189 */       while (((nread = this.m_inputStream.read(buf)) > 0) && (!this.m_abortRequested))
/*     */       {
/* 191 */         this.m_lastReadTime = System.currentTimeMillis();
/* 192 */         if (SystemUtils.m_verbose)
/*     */         {
/* 194 */           Report.debug("indexer", "read " + nread + " bytes", null);
/*     */         }
/* 196 */         int len = nread;
/* 197 */         if (!endData)
/*     */         {
/* 199 */           match = byteArraySearch(buf, 0, nread, eod, match);
/*     */ 
/* 201 */           if (match[1] >= 0)
/*     */           {
/* 203 */             endData = true;
/* 204 */             len = match[1] + 1;
/*     */           }
/*     */           else
/*     */           {
/* 208 */             this.m_inputStream.mark(buf.length);
/*     */           }
/*     */         }
/* 211 */         String output = new String(buf, 0, len);
/* 212 */         if (this.m_buffer.length() == 0)
/*     */         {
/* 216 */           String key = "idclaunchprocessid=";
/* 217 */           int launchIndex = output.indexOf(key);
/* 218 */           if (launchIndex >= 0)
/*     */           {
/* 220 */             String processId = output.substring(launchIndex + key.length());
/* 221 */             int endIndex = processId.indexOf("\n");
/* 222 */             if (endIndex >= 0)
/*     */             {
/* 224 */               processId = processId.substring(0, endIndex);
/*     */             }
/* 226 */             processId = processId.trim();
/* 227 */             if (processId.length() > 0)
/*     */             {
/* 229 */               this.m_execution.saveProcessID(processId);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 238 */         Report.trace("indexer", output.trim(), null);
/*     */ 
/* 240 */         this.m_buffer.append(output);
/* 241 */         if ((endData) && 
/* 243 */           (this.m_execution.m_maintainIndexProcess))
/*     */         {
/* 245 */           if (len == nread)
/*     */             break;
/* 247 */           this.m_inputStream.reset();
/* 248 */           this.m_inputStream.skip(len); break;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 254 */       synchronized (this.m_lock)
/*     */       {
/* 256 */         this.m_finishedReading = true;
/*     */         try
/*     */         {
/* 259 */           this.m_lock.notify();
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 263 */           ignore.printStackTrace();
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 269 */       Report.trace("indexer", null, e);
/* 270 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 271 */       this.m_errorMessage = LocaleUtils.encodeMessage(msg);
/*     */     }
/*     */     finally
/*     */     {
/* 275 */       synchronized (this.m_lock)
/*     */       {
/* 277 */         this.m_finishedReading = true;
/* 278 */         this.m_lock.notify();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void runStdErrThread()
/*     */   {
/*     */     try
/*     */     {
/* 287 */       this.m_inputStream = new BufferedInputStream(this.m_process.getErrorStream());
/*     */ 
/* 289 */       byte[] errbuf = new byte[1024];
/* 290 */       int count = -1;
/* 291 */       int waitCount = 5;
/* 292 */       boolean haveData = false;
/* 293 */       while (!this.m_abortRequested)
/*     */       {
/* 295 */         if ((count = this.m_inputStream.read(errbuf)) > 0)
/*     */         {
/* 297 */           this.m_lastReadTime = System.currentTimeMillis();
/* 298 */           if (SystemUtils.m_verbose)
/*     */           {
/* 300 */             Report.debug("indexer", "read " + count + " bytes", null);
/*     */           }
/*     */ 
/* 303 */           Report.trace("indexer", new String(errbuf, 0, count), null);
/* 304 */           this.m_buffer.append(new String(errbuf, 0, count));
/* 305 */           haveData = true;
/*     */         } else {
/* 307 */           if ((!this.m_execution.m_maintainIndexProcess) && 
/* 316 */             (this.m_stdOutMonitor.isFinished())) {
/*     */             break;
/*     */           }
/*     */         }
/*     */ 
/* 321 */         if (this.m_execution.m_maintainIndexProcess)
/*     */         {
/* 326 */           if ((!haveData) && (waitCount < 1))
/*     */           {
/*     */             break;
/*     */           }
/*     */ 
/* 331 */           if (haveData)
/*     */           {
/* 333 */             waitCount = 5;
/* 334 */             haveData = false;
/*     */           }
/*     */           else
/*     */           {
/* 339 */             --waitCount;
/* 340 */             if (waitCount < 1) {
/*     */               break;
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 347 */         SystemUtils.sleep(10L);
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 352 */       ignore.printStackTrace();
/*     */     }
/*     */     finally
/*     */     {
/* 356 */       this.m_finishedReading = true;
/* 357 */       synchronized (this.m_lock)
/*     */       {
/* 359 */         this.m_lock.notify();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected int[] byteArraySearch(byte[] src, int beginIndex, int endIndex, byte[] searchArray, int[] match)
/*     */   {
/* 375 */     for (int i = beginIndex; i < endIndex; ++i)
/*     */     {
/* 377 */       if (src[i] == searchArray[match[0]])
/*     */       {
/* 379 */         match[0] += 1;
/*     */       }
/*     */       else
/*     */       {
/* 383 */         match[0] = 0;
/*     */       }
/*     */ 
/* 386 */       if (match[0] != searchArray.length)
/*     */         continue;
/* 388 */       match[1] = i;
/* 389 */       break;
/*     */     }
/*     */ 
/* 392 */     return match;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 397 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerExecutionMonitorThread
 * JD-Core Version:    0.5.4
 */