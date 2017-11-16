/*     */ package intradoc.process;
/*     */ 
/*     */ import intradoc.common.EventInputStream;
/*     */ import intradoc.common.EventOutputStream;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StreamEventHandler;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class ProcessLogger extends Thread
/*     */   implements StreamEventHandler
/*     */ {
/*     */   protected BufferedReader m_input;
/*     */   protected String m_headerMsg;
/*  50 */   protected int m_logType = 6000;
/*     */   protected String m_appContext;
/*     */   protected String m_traceSection;
/*     */   protected Process m_process;
/*     */   protected EventInputStream m_eventInputStream;
/*     */   protected EventOutputStream m_eventOutputStream;
/*     */   protected IdcStringBuilder m_outputBuffer;
/*     */   protected boolean m_streamFinished;
/*     */ 
/*     */   public ProcessLogger()
/*     */   {
/*  62 */     super("unknown InputStream consumer for " + Thread.currentThread().getName());
/*  63 */     this.m_outputBuffer = new IdcStringBuilder();
/*     */   }
/*     */ 
/*     */   public ProcessLogger(InputStream stream)
/*     */   {
/*  73 */     super("InputStream consumer for " + Thread.currentThread().getName());
/*     */ 
/*  75 */     setInputStream(stream);
/*  76 */     this.m_outputBuffer = new IdcStringBuilder();
/*     */   }
/*     */ 
/*     */   public ProcessLogger(Process proc)
/*     */   {
/*  87 */     super("Process ErrorStream consumer for " + Thread.currentThread().getName());
/*  88 */     this.m_process = proc;
/*  89 */     setInputStream(proc.getErrorStream());
/*  90 */     this.m_outputBuffer = new IdcStringBuilder();
/*     */   }
/*     */ 
/*     */   public OutputStream getLinkedStdinStream(int flags)
/*     */   {
/* 101 */     OutputStream baseStream = this.m_process.getOutputStream();
/* 102 */     this.m_eventOutputStream = new EventOutputStream(baseStream);
/* 103 */     this.m_eventOutputStream.setTraceSection(this.m_traceSection);
/* 104 */     this.m_eventOutputStream.setFlags(129);
/* 105 */     this.m_eventOutputStream.addStreamEventHandler(this, this);
/* 106 */     start();
/* 107 */     return this.m_eventOutputStream;
/*     */   }
/*     */ 
/*     */   public InputStream getLinkedStdoutStream(int flags)
/*     */   {
/* 118 */     InputStream baseStream = this.m_process.getInputStream();
/* 119 */     this.m_eventInputStream = new EventInputStream(baseStream);
/* 120 */     this.m_eventInputStream.setTraceSection(this.m_traceSection);
/* 121 */     this.m_eventInputStream.setFlags(129);
/* 122 */     this.m_eventInputStream.addStreamEventHandler(this, this);
/* 123 */     start();
/* 124 */     return this.m_eventInputStream;
/*     */   }
/*     */ 
/*     */   public void setInputStream(InputStream stream)
/*     */   {
/* 134 */     this.m_input = new BufferedReader(new InputStreamReader(stream));
/*     */   }
/*     */ 
/*     */   public void setLogType(int logType)
/*     */   {
/* 145 */     this.m_logType = logType;
/*     */   }
/*     */ 
/*     */   public void setHeaderMessage(String msg)
/*     */   {
/* 156 */     this.m_headerMsg = msg;
/* 157 */     if (null == this.m_headerMsg)
/*     */       return;
/* 159 */     writeToLog(this.m_headerMsg);
/*     */   }
/*     */ 
/*     */   public void setAppContext(String cxt)
/*     */   {
/* 170 */     this.m_appContext = cxt;
/*     */   }
/*     */ 
/*     */   public void setTraceSection(String section)
/*     */   {
/* 180 */     this.m_traceSection = section;
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/* 189 */     if (null == this.m_input)
/*     */     {
/* 191 */       return;
/*     */     }
/*     */ 
/* 194 */     IOException finalException = null;
/*     */     try
/*     */     {
/* 198 */       boolean hasErrorData = false;
/*     */       try
/*     */       {
/* 201 */         while (null != (line = this.m_input.readLine()))
/*     */         {
/*     */           String line;
/* 203 */           writeToLog(line + "\n");
/* 204 */           hasErrorData = true;
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 209 */         if (SystemUtils.m_verbose)
/*     */         {
/* 211 */           Report.debug(this.m_traceSection, null, e);
/*     */         }
/* 213 */         Report.message(this.m_appContext, this.m_traceSection, this.m_logType, null, null, -1, -1, e, null);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 218 */         this.m_input.close();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 222 */         Report.trace(this.m_traceSection, null, e);
/*     */       }
/* 224 */       this.m_input = null;
/* 225 */       if (hasErrorData)
/*     */       {
/* 227 */         Report.message(this.m_appContext, this.m_traceSection, this.m_logType, getOutputString(), null, -1, -1, null, null);
/*     */       }
/*     */ 
/* 230 */       if ((this.m_process != null) && (((this.m_eventInputStream != null) || (this.m_eventOutputStream != null))))
/*     */       {
/*     */         try
/*     */         {
/* 234 */           this.m_process.waitFor();
/* 235 */           int rc = this.m_process.exitValue();
/* 236 */           synchronized (this)
/*     */           {
/* 238 */             if (!this.m_streamFinished)
/*     */             {
/* 240 */               SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/*     */               try
/*     */               {
/* 243 */                 super.wait();
/*     */               }
/*     */               finally
/*     */               {
/* 247 */                 SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(this);
/*     */               }
/*     */             }
/*     */           }
/* 251 */           if ((rc != 0) || (hasErrorData))
/*     */           {
/* 253 */             IdcMessage msg = new IdcMessage("syProcessErrorCode", new Object[] { "" + rc });
/*     */ 
/* 256 */             String logText = getOutputString();
/* 257 */             if (logText.length() > 0)
/*     */             {
/* 260 */               if (null != this.m_headerMsg)
/*     */               {
/* 262 */                 logText = logText.substring(this.m_headerMsg.length());
/*     */               }
/* 264 */               msg.m_prior = new IdcMessage("syProcessErrorData", new Object[] { logText });
/*     */             }
/* 266 */             ServiceException e = new ServiceException(null, msg);
/* 267 */             e.setContainerAttribute("isWrapped", "true");
/* 268 */             finalException = new IOException();
/* 269 */             finalException.initCause(e);
/*     */           }
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/* 274 */           finalException = new IOException();
/* 275 */           finalException.initCause(e);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 281 */       Report.trace("fileaccess", "Error in process logger.", t);
/*     */     }
/*     */     finally
/*     */     {
/* 286 */       if (this.m_eventInputStream != null)
/*     */       {
/* 288 */         this.m_eventInputStream.notifyFinished(finalException);
/*     */       }
/* 290 */       if (this.m_eventOutputStream != null)
/*     */       {
/* 292 */         this.m_eventOutputStream.notifyFinished(finalException);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void writeToLog(String data)
/*     */   {
/* 299 */     this.m_outputBuffer.append(data);
/*     */   }
/*     */ 
/*     */   public synchronized String getOutputString()
/*     */   {
/* 304 */     return this.m_outputBuffer.toString();
/*     */   }
/*     */ 
/*     */   public synchronized void handleStreamEvent(String type, Object stream, Object data)
/*     */   {
/* 311 */     if ((type != "eof") && (type != "close"))
/*     */       return;
/* 313 */     this.m_streamFinished = true;
/* 314 */     super.notify();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 322 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75337 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.process.ProcessLogger
 * JD-Core Version:    0.5.4
 */