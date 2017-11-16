/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class EventStreamHelper
/*     */ {
/*     */   protected Object m_stream;
/*     */   protected List m_filterList;
/*     */   protected IOException m_externalException;
/*     */   protected boolean m_finished;
/*     */   protected String m_section;
/*     */   public int m_flags;
/*     */ 
/*     */   public EventStreamHelper(Object stream)
/*     */   {
/*  40 */     this.m_stream = stream;
/*  41 */     this.m_filterList = new ArrayList();
/*  42 */     this.m_section = "fileaccess";
/*     */   }
/*     */ 
/*     */   public synchronized void notifyFinished(IOException e)
/*     */   {
/*  47 */     if ((this.m_externalException != null) && (e != null))
/*     */     {
/*  49 */       throw new IllegalStateException("!$Exception already set.");
/*     */     }
/*  51 */     if (e != null)
/*     */     {
/*  53 */       this.m_externalException = e;
/*     */     }
/*  55 */     this.m_finished = true;
/*  56 */     Report.trace(this.m_section, "notifying " + this, null);
/*     */ 
/*  58 */     this.m_flags &= -129;
/*  59 */     super.notify();
/*     */   }
/*     */ 
/*     */   public synchronized int setFlags(int flags)
/*     */   {
/*  68 */     this.m_flags |= flags;
/*  69 */     return this.m_flags;
/*     */   }
/*     */ 
/*     */   public synchronized int clearFlags(int flags)
/*     */   {
/*  78 */     this.m_flags &= (flags ^ 0xFFFFFFFF);
/*  79 */     return this.m_flags;
/*     */   }
/*     */ 
/*     */   public IOException getExternalException()
/*     */   {
/*  84 */     return this.m_externalException;
/*     */   }
/*     */ 
/*     */   public synchronized void checkExternalException(IOException currentException, int flags)
/*     */     throws IOException
/*     */   {
/*  90 */     Report.trace(this.m_section, "starting checkExternalException flags: " + flags + " m_flags: " + this.m_flags, null);
/*     */ 
/*  92 */     if (((flags & 0x1) == (this.m_flags & 0x1)) && (this.m_externalException != null))
/*     */     {
/*  95 */       IOException e = this.m_externalException;
/*  96 */       this.m_externalException = null;
/*  97 */       if (currentException != null)
/*     */       {
/*  99 */         if (currentException.getCause() == null)
/*     */         {
/* 101 */           currentException.initCause(e);
/* 102 */           e = currentException;
/*     */         }
/*     */         else
/*     */         {
/* 106 */           Report.trace(this.m_section, "suppressing exception", currentException);
/*     */         }
/*     */       }
/*     */ 
/* 110 */       throw e;
/*     */     }
/* 112 */     if ((this.m_finished) || ((flags & 0x1) != 1) || ((this.m_flags & 0x1) != 1)) {
/*     */       return;
/*     */     }
/*     */ 
/* 116 */     int size = this.m_filterList.size();
/* 117 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 119 */       Object[] callback = (Object[])(Object[])this.m_filterList.get(i);
/* 120 */       if (!callback[0] instanceof StreamEventHandler)
/*     */         continue;
/* 122 */       StreamEventHandler h = (StreamEventHandler)callback[0];
/* 123 */       h.handleStreamEvent("eof", this.m_stream, callback[1]);
/*     */     }
/*     */ 
/* 126 */     if ((this.m_flags & 0x80) != 0)
/*     */     {
/*     */       try
/*     */       {
/* 130 */         SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/* 131 */         Report.trace(this.m_section, "waiting for external notification on " + this, null);
/* 132 */         super.wait();
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/* 136 */         IOException newIOE = new IOException();
/* 137 */         newIOE.initCause(e);
/* 138 */         if (currentException != null)
/*     */         {
/* 140 */           if (currentException.getCause() == null)
/*     */           {
/* 142 */             currentException.initCause(newIOE);
/* 143 */             newIOE = currentException;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 150 */         throw newIOE;
/*     */       }
/*     */       finally
/*     */       {
/* 154 */         SystemUtils.unregisterSynchronizationObjectToNotifyOnStop(this);
/*     */       }
/*     */     }
/* 157 */     if (this.m_externalException == null)
/*     */       return;
/* 159 */     IOException e = this.m_externalException;
/* 160 */     if ((currentException != null) && (currentException.getCause() == null))
/*     */     {
/* 162 */       currentException.initCause(e);
/* 163 */       e = currentException;
/*     */     }
/* 165 */     else if (currentException != null)
/*     */     {
/* 167 */       Report.trace(this.m_section, "suppressing exception", currentException);
/*     */     }
/*     */ 
/* 170 */     this.m_externalException = null;
/* 171 */     throw e;
/*     */   }
/*     */ 
/*     */   public void addStreamEventHandler(StreamEventHandler h, Object data)
/*     */   {
/* 178 */     this.m_filterList.add(new Object[] { h, data });
/*     */   }
/*     */ 
/*     */   public synchronized void close() throws IOException
/*     */   {
/* 183 */     int size = this.m_filterList.size();
/* 184 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 186 */       Object[] callback = (Object[])(Object[])this.m_filterList.get(i);
/* 187 */       if (!callback[0] instanceof StreamEventHandler)
/*     */         continue;
/* 189 */       StreamEventHandler h = (StreamEventHandler)callback[0];
/* 190 */       h.handleStreamEvent("close", this.m_stream, callback[1]);
/*     */     }
/*     */ 
/* 193 */     this.m_filterList.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 198 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90870 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EventStreamHelper
 * JD-Core Version:    0.5.4
 */