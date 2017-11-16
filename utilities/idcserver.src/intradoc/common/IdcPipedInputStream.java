/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class IdcPipedInputStream extends InputStream
/*     */ {
/*     */   protected IdcPipedOutputStream m_pos;
/*     */   protected boolean m_connected;
/*  27 */   protected byte[] m_buf = new byte[IdcPipedOutputStream.PIPE_SIZE];
/*  28 */   protected int m_position = -1;
/*  29 */   protected int m_end = 0;
/*  30 */   protected boolean m_streamClosed = false;
/*     */ 
/*     */   public IdcPipedInputStream()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcPipedInputStream(IdcPipedOutputStream ipos)
/*     */   {
/*  40 */     connect(ipos);
/*     */   }
/*     */ 
/*     */   public void connect(IdcPipedOutputStream ipos)
/*     */   {
/*  45 */     if (ipos != null)
/*  46 */       this.m_connected = true;
/*  47 */     this.m_pos = ipos;
/*     */ 
/*  49 */     this.m_pos.connect(this);
/*     */   }
/*     */ 
/*     */   public int available()
/*     */     throws IOException
/*     */   {
/*  55 */     if (this.m_position == -1)
/*     */     {
/*  57 */       if (!this.m_pos.m_bufferEmpty)
/*     */       {
/*  59 */         if (!fill())
/*  60 */           return 0;
/*     */       }
/*     */       else
/*  63 */         return 0;
/*     */     }
/*  65 */     return this.m_end - this.m_position;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  71 */     byte[] b = new byte[1];
/*  72 */     int len = read(b, 0, 1);
/*  73 */     return (len == 1) ? b[0] : -1;
/*     */   }
/*     */ 
/*     */   public int read(byte[] b)
/*     */     throws IOException
/*     */   {
/*  79 */     return read(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  85 */     if (b == null) {
/*  86 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingPISNullTargetInRead", null));
/*     */     }
/*  88 */     if ((off < 0) || (len < 0) || (off + len > b.length)) {
/*  89 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingOutRange", null));
/*     */     }
/*  91 */     if (!this.m_connected) {
/*  92 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingNotConnected", null));
/*     */     }
/*     */ 
/*  95 */     if (len == 0) {
/*  96 */       return 0;
/*     */     }
/*  98 */     ensureOpen();
/*  99 */     if ((this.m_position == -1) && 
/* 100 */       (!fill())) {
/* 101 */       return -1;
/*     */     }
/*     */ 
/* 104 */     int avail = available();
/* 105 */     int copyLen = (len > avail) ? avail : len;
/*     */ 
/* 107 */     System.arraycopy(this.m_buf, this.m_position, b, off, copyLen);
/*     */ 
/* 109 */     this.m_position += copyLen;
/* 110 */     if (copyLen == avail)
/*     */     {
/* 112 */       this.m_position = -1;
/*     */     }
/*     */ 
/* 115 */     return copyLen;
/*     */   }
/*     */ 
/*     */   protected boolean fill() throws IOException
/*     */   {
/*     */     do
/*     */     {
/* 122 */       if (!this.m_pos.m_bufferEmpty)
/*     */         break label69;
/* 124 */       notifySender();
/*     */       try
/*     */       {
/* 127 */         synchronized (this)
/*     */         {
/* 129 */           super.wait(1000L);
/*     */         }
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/* 134 */         e.printStackTrace();
/*     */       }
/* 136 */       ensureOpen();
/*     */     }
/* 138 */     while ((!this.m_pos.m_streamClosed) || (!this.m_pos.m_bufferEmpty));
/* 139 */     return false;
/*     */ 
/* 142 */     label69: this.m_pos.getLock();
/*     */ 
/* 144 */     this.m_position = 0;
/* 145 */     this.m_end = this.m_pos.m_position;
/* 146 */     System.arraycopy(this.m_pos.m_buf, 0, this.m_buf, 0, this.m_end);
/*     */ 
/* 149 */     this.m_pos.m_position = 0;
/* 150 */     this.m_pos.m_bufferEmpty = true;
/* 151 */     this.m_pos.m_bufferFull = false;
/*     */ 
/* 153 */     this.m_pos.releaseLock();
/* 154 */     notifySender();
/*     */ 
/* 157 */     return true;
/*     */   }
/*     */ 
/*     */   protected void ensureOpen()
/*     */     throws IOException
/*     */   {
/* 164 */     if (this.m_streamClosed)
/* 165 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingStreamClosed", null));
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 172 */     if (this.m_streamClosed) {
/* 173 */       throw new IOException(LocaleResources.localizeMessage("!csChunkingStreamHasClosed", null));
/*     */     }
/* 175 */     this.m_streamClosed = true;
/* 176 */     notifySender();
/*     */   }
/*     */ 
/*     */   protected void notifySender() throws IOException
/*     */   {
/* 181 */     if (this.m_pos == null)
/*     */       return;
/* 183 */     synchronized (this.m_pos)
/*     */     {
/* 185 */       this.m_pos.notifyAll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 193 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcPipedInputStream
 * JD-Core Version:    0.5.4
 */