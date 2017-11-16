/*     */ package intradoc.common.filter;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class InputStreamExtension extends InputStream
/*     */ {
/*     */   InputStream[] m_streams;
/*     */   public boolean m_closeInternalStreams;
/*     */   public int m_streamIndex;
/*     */ 
/*     */   public InputStreamExtension(InputStream[] streams)
/*     */   {
/*  46 */     this.m_streams = streams;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  52 */     if ((this.m_streamIndex < 0) || (this.m_streams == null) || (this.m_streamIndex >= this.m_streams.length))
/*     */     {
/*  54 */       return -1;
/*     */     }
/*     */ 
/*  60 */     int byteRead = -1;
/*  61 */     while (this.m_streamIndex < this.m_streams.length)
/*     */     {
/*  63 */       InputStream in = this.m_streams[this.m_streamIndex];
/*  64 */       if (in != null)
/*     */       {
/*  66 */         byteRead = in.read();
/*     */       }
/*  68 */       if (byteRead >= 0) {
/*     */         break;
/*     */       }
/*     */ 
/*  72 */       this.m_streamIndex += 1;
/*     */     }
/*  74 */     return byteRead;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  79 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  85 */     if ((this.m_streamIndex < 0) || (this.m_streams == null) || (this.m_streamIndex >= this.m_streams.length))
/*     */     {
/*  87 */       return -1;
/*     */     }
/*  89 */     int numRead = -1;
/*  90 */     while (this.m_streamIndex < this.m_streams.length)
/*     */     {
/*  92 */       InputStream in = this.m_streams[this.m_streamIndex];
/*  93 */       if (in != null)
/*     */       {
/*  95 */         numRead = in.read(b, off, len);
/*     */       }
/*  97 */       if (numRead > 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 101 */       this.m_streamIndex += 1;
/*     */     }
/* 103 */     return numRead;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 109 */     if ((!this.m_closeInternalStreams) || (this.m_streams == null))
/*     */       return;
/* 111 */     for (int i = 0; i < this.m_streams.length; ++i)
/*     */     {
/* 113 */       InputStream in = this.m_streams[i];
/* 114 */       if (in == null)
/*     */         continue;
/* 116 */       in.close();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.filter.InputStreamExtension
 * JD-Core Version:    0.5.4
 */