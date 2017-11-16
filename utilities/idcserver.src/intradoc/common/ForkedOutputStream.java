/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class ForkedOutputStream extends OutputStream
/*     */ {
/*  34 */   public boolean m_closeStreams = false;
/*     */ 
/*  38 */   public boolean m_closeFirstStream = false;
/*     */ 
/*  43 */   protected OutputStream[] m_streams = null;
/*     */ 
/*     */   protected ForkedOutputStream()
/*     */   {
/*     */   }
/*     */ 
/*     */   public ForkedOutputStream(OutputStream[] streams)
/*     */   {
/*  59 */     this.m_streams = streams;
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/*  71 */     if (this.m_streams == null)
/*     */       return;
/*  73 */     for (int i = 0; i < this.m_streams.length; ++i)
/*     */     {
/*  75 */       this.m_streams[i].write(b);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  91 */     if (this.m_streams == null)
/*     */       return;
/*  93 */     for (int i = 0; i < this.m_streams.length; ++i)
/*     */     {
/*  95 */       this.m_streams[i].write(b, off, len);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 103 */     if (this.m_streams == null)
/*     */       return;
/* 105 */     for (int i = 0; i < this.m_streams.length; ++i)
/*     */     {
/* 107 */       this.m_streams[i].flush();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 115 */     if (this.m_streams == null)
/*     */       return;
/* 117 */     for (int i = 0; i < this.m_streams.length; ++i)
/*     */     {
/* 119 */       if ((this.m_closeStreams) || ((this.m_closeFirstStream) && (i == 0)))
/*     */       {
/* 121 */         this.m_streams[i].close();
/*     */       }
/*     */       else
/*     */       {
/* 125 */         this.m_streams[i].flush();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ForkedOutputStream
 * JD-Core Version:    0.5.4
 */