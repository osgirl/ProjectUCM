/*     */ package intradoc.server.chunker;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ChunkedRequestSessionManager
/*     */ {
/*     */   protected Hashtable m_sessions;
/*     */   protected Vector m_timeoutQueue;
/*     */   protected Thread m_monitor;
/*     */ 
/*     */   public ChunkedRequestSessionManager()
/*     */   {
/*  35 */     this.m_sessions = new Hashtable();
/*  36 */     this.m_timeoutQueue = new IdcVector();
/*     */   }
/*     */ 
/*     */   public synchronized boolean register(ChunkedRequestSession s, int timeOut)
/*     */   {
/*  41 */     Report.trace("chunkedrequest", "Register new entry in ChunkSessionManager.", null);
/*  42 */     Report.trace("chunkedrequest", "SessionID: " + s.getSessionID() + "TimeOut: " + timeOut + "ms.", null);
/*     */ 
/*  44 */     String id = s.getSessionID();
/*  45 */     if (this.m_sessions.get(id) != null)
/*     */     {
/*  47 */       return false;
/*     */     }
/*  49 */     this.m_sessions.put(id, s);
/*  50 */     insertTimeoutQueue(id, timeOut);
/*  51 */     return true;
/*     */   }
/*     */ 
/*     */   public synchronized ChunkedRequestSession retrieve(String id)
/*     */   {
/*  56 */     Report.trace("chunkedrequest", "Retrieve Session: " + id, null);
/*  57 */     removeTimeoutQueue(id);
/*  58 */     return (ChunkedRequestSession)this.m_sessions.remove(id);
/*     */   }
/*     */ 
/*     */   public void idle(String id, int timeout)
/*     */   {
/*  63 */     if (timeout < 1)
/*  64 */       return;
/*  65 */     if (this.m_sessions.get(id) == null) {
/*  66 */       return;
/*     */     }
/*  68 */     insertTimeoutQueue(id, timeout);
/*     */   }
/*     */ 
/*     */   protected void insertTimeoutQueue(String id, int timeout)
/*     */   {
/*  73 */     Long t = new Long(System.currentTimeMillis() + timeout);
/*  74 */     Object[] entry = { id, t };
/*     */ 
/*  76 */     int index = 0;
/*  77 */     int size = this.m_timeoutQueue.size();
/*  78 */     for (index = 0; index < size; ++index)
/*     */     {
/*  80 */       if (t.longValue() > ((Long)((Object[])(Object[])this.m_timeoutQueue.elementAt(index))[1]).longValue())
/*     */         break;
/*     */     }
/*  83 */     this.m_timeoutQueue.insertElementAt(entry, index);
/*     */ 
/*  85 */     Report.trace("chunkedrequest", "Session " + id + " is inserted in timeout queue at index " + index, null);
/*     */ 
/*  88 */     if (index == 0)
/*  89 */       startMonitor();
/*     */   }
/*     */ 
/*     */   protected void removeTimeoutQueue(String id)
/*     */   {
/*  95 */     int size = this.m_timeoutQueue.size();
/*     */ 
/*  97 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  99 */       if (!id.equals(((Object[])(Object[])this.m_timeoutQueue.elementAt(i))[0]))
/*     */         continue;
/* 101 */       this.m_timeoutQueue.removeElementAt(i);
/* 102 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void startMonitor()
/*     */   {
/* 109 */     if ((this.m_monitor != null) && (this.m_monitor.isAlive()))
/*     */     {
/* 111 */       synchronized (this.m_monitor)
/*     */       {
/* 113 */         this.m_monitor.notify();
/*     */       }
/* 115 */       return;
/*     */     }
/*     */ 
/* 118 */     this.m_monitor = new Thread("Chunker manager")
/*     */     {
/*     */       public void run()
/*     */       {
/* 123 */         while (ChunkedRequestSessionManager.this.m_timeoutQueue.size() > 0) {
/*     */           long waitTime;
/*     */           do { Long time = new Long(System.currentTimeMillis());
/*     */ 
/* 127 */             while (time.longValue() >= ((Long)((Object[])(Object[])ChunkedRequestSessionManager.this.m_timeoutQueue.elementAt(0))[1]).longValue())
/*     */             {
/* 129 */               Object[] obj = (Object[])(Object[])ChunkedRequestSessionManager.this.m_timeoutQueue.elementAt(0);
/* 130 */               ChunkedRequestSessionManager.this.m_timeoutQueue.removeElementAt(0);
/* 131 */               Report.trace("chunkedrequest", "Session " + (String)obj[0] + "  timed out.", null);
/*     */ 
/* 133 */               ChunkedRequestSessionManager.this.closeSession((String)obj[0]);
/* 134 */               if (ChunkedRequestSessionManager.this.m_timeoutQueue.size() == 0) {
/*     */                 break;
/*     */               }
/*     */             }
/* 138 */             waitTime = -1L;
/* 139 */             if (ChunkedRequestSessionManager.this.m_timeoutQueue.size() <= 0)
/*     */               continue;
/* 141 */             waitTime = ((Long)((Object[])(Object[])ChunkedRequestSessionManager.this.m_timeoutQueue.elementAt(0))[1]).longValue() - System.currentTimeMillis(); }
/*     */ 
/*     */ 
/* 144 */           while (waitTime <= 0L);
/*     */ 
/* 147 */           synchronized (this)
/*     */           {
/*     */             try
/*     */             {
/* 151 */               super.wait(waitTime);
/*     */             }
/*     */             catch (InterruptedException e)
/*     */             {
/* 155 */               Report.trace(null, null, e);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 162 */     this.m_monitor.start();
/*     */   }
/*     */ 
/*     */   protected void closeSession(String id)
/*     */   {
/* 168 */     ChunkedRequestSession s = (ChunkedRequestSession)this.m_sessions.get(id);
/*     */ 
/* 170 */     if (s == null)
/*     */     {
/* 172 */       Report.trace("chunkedrequest", "Cannot close session. No session related with ID" + id, null);
/*     */ 
/* 174 */       return;
/*     */     }
/* 176 */     if (s instanceof ChunkedUploadSession)
/*     */     {
/* 178 */       ChunkedUploadSession cu = (ChunkedUploadSession)s;
/* 179 */       long tranSize = cu.getTranedSize();
/* 180 */       long fileSize = cu.getFileSize();
/* 181 */       long size = (fileSize + 512L) / 1024L;
/* 182 */       String sizeLabel = " KB";
/* 183 */       if (size > 1024L)
/*     */       {
/* 185 */         size = (size + 512L) / 1024L;
/* 186 */         sizeLabel = " MB";
/*     */       }
/*     */ 
/* 189 */       Report.error(null, LocaleUtils.encodeMessage("csChunkingSessionTimeout", null, id, "" + 100L * tranSize / fileSize, "" + size + sizeLabel), null);
/*     */     }
/*     */ 
/* 196 */     s.closeSession();
/* 197 */     Report.trace("chunkedrequest", "Session " + id + " is closed", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 202 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.chunker.ChunkedRequestSessionManager
 * JD-Core Version:    0.5.4
 */