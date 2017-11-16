/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class IdcPipedOutputStream extends OutputStream
/*     */ {
/*  26 */   protected static int PIPE_SIZE = 2048;
/*  27 */   protected byte[] m_buf = new byte[PIPE_SIZE];
/*  28 */   protected int m_position = 0;
/*     */   protected IdcPipedInputStream m_pis;
/*  30 */   protected boolean m_bufferFull = false;
/*  31 */   protected boolean m_bufferEmpty = true;
/*  32 */   protected boolean m_streamClosed = false;
/*  33 */   protected boolean m_lock = false;
/*     */   Thread m_currentThread;
/*     */ 
/*     */   public IdcPipedOutputStream()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcPipedOutputStream(IdcPipedInputStream pis)
/*     */   {
/*  43 */     connect(pis);
/*     */   }
/*     */ 
/*     */   public void connect(IdcPipedInputStream pis)
/*     */   {
/*  48 */     this.m_pis = pis;
/*     */ 
/*  50 */     pis.m_connected = true;
/*  51 */     pis.m_pos = this;
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  57 */     if (b == null)
/*     */     {
/*  59 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingPOSNullSource", null));
/*     */     }
/*     */ 
/*  63 */     if ((off < 0) || (len < 0) || (off + len > b.length))
/*     */     {
/*  65 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingOutRange", null));
/*     */     }
/*     */ 
/*  68 */     if (len == 0)
/*  69 */       return;
/*  70 */     if (this.m_pis == null)
/*     */     {
/*  72 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingNotConnected", null));
/*     */     }
/*     */ 
/*  75 */     while (len > 0)
/*     */     {
/*  77 */       while (this.m_bufferFull)
/*     */       {
/*  79 */         notifyReader();
/*     */         try
/*     */         {
/*  83 */           synchronized (this)
/*     */           {
/*  85 */             super.wait(1000L);
/*     */           }
/*     */         }
/*     */         catch (InterruptedException e)
/*     */         {
/*  90 */           e.printStackTrace();
/*     */         }
/*     */ 
/*  93 */         ensureOpen();
/*     */       }
/*     */ 
/*  96 */       getLock();
/*     */ 
/*  98 */       int avail = PIPE_SIZE - this.m_position;
/*  99 */       int copyLen = (len > avail) ? avail : len;
/* 100 */       System.arraycopy(b, off, this.m_buf, this.m_position, copyLen);
/* 101 */       len -= copyLen;
/* 102 */       this.m_position += copyLen;
/* 103 */       off += copyLen;
/*     */ 
/* 105 */       if (this.m_position >= PIPE_SIZE)
/*     */       {
/* 107 */         this.m_bufferFull = true;
/*     */       }
/* 109 */       if ((this.m_bufferEmpty) && (copyLen > 0)) {
/* 110 */         this.m_bufferEmpty = false;
/*     */       }
/* 112 */       releaseLock();
/* 113 */       notifyReader();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/* 121 */     byte[] ba = new byte[1];
/* 122 */     ba[0] = (byte)(0xFF & b);
/* 123 */     write(ba);
/*     */   }
/*     */ 
/*     */   public void write(byte[] b)
/*     */     throws IOException
/*     */   {
/* 129 */     write(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 136 */     if (this.m_pis == null)
/*     */       return;
/* 138 */     synchronized (this.m_pis)
/*     */     {
/* 140 */       this.m_pis.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 148 */     if (this.m_streamClosed) {
/* 149 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingStreamHasClosed", null));
/*     */     }
/* 151 */     this.m_streamClosed = true;
/* 152 */     notifyReader();
/*     */   }
/*     */ 
/*     */   protected synchronized void getLock()
/*     */     throws IOException
/*     */   {
/* 161 */     int timeOut = 0;
/*     */     do { if (!this.m_lock)
/*     */         break label36;
/* 164 */       SystemUtils.sleep(50L);
/* 165 */       ++timeOut; }
/*     */ 
/* 167 */     while (timeOut <= 600);
/* 168 */     throw new IOException("Time out for lock");
/*     */ 
/* 170 */     label36: this.m_lock = true;
/*     */   }
/*     */ 
/*     */   protected boolean releaseLock()
/*     */     throws IOException
/*     */   {
/* 176 */     this.m_lock = false;
/* 177 */     return true;
/*     */   }
/*     */ 
/*     */   protected void notifyReader() throws IOException
/*     */   {
/* 182 */     if (this.m_pis == null)
/*     */       return;
/* 184 */     synchronized (this.m_pis)
/*     */     {
/* 186 */       this.m_pis.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void ensureOpen()
/*     */     throws IOException
/*     */   {
/* 193 */     if (this.m_streamClosed)
/* 194 */       throw new IOException("Stream closed.");
/* 195 */     if (this.m_pis.m_streamClosed)
/* 196 */       throw new IOException("Stream closed by peer.");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 202 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcPipedOutputStream
 * JD-Core Version:    0.5.4
 */