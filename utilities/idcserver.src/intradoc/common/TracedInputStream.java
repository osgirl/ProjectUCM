/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class TracedInputStream extends InputStream
/*     */ {
/*  33 */   protected OutputStream m_traceStream = null;
/*     */ 
/*  38 */   protected InputStream m_inStream = null;
/*     */ 
/*     */   protected TracedInputStream()
/*     */   {
/*     */   }
/*     */ 
/*     */   public TracedInputStream(InputStream in, OutputStream trace)
/*     */   {
/*  54 */     this.m_inStream = in;
/*  55 */     this.m_traceStream = trace;
/*     */   }
/*     */ 
/*     */   public synchronized int read()
/*     */     throws IOException
/*     */   {
/*  68 */     int b = this.m_inStream.read();
/*  69 */     if ((this.m_traceStream != null) && (b != -1))
/*     */     {
/*  71 */       this.m_traceStream.write(b);
/*     */     }
/*  73 */     return b;
/*     */   }
/*     */ 
/*     */   public synchronized int read(byte[] arg)
/*     */     throws IOException
/*     */   {
/*  84 */     int numRead = this.m_inStream.read(arg);
/*  85 */     if ((this.m_traceStream != null) && (numRead != -1))
/*     */     {
/*  87 */       this.m_traceStream.write(arg, 0, numRead);
/*     */     }
/*  89 */     return numRead;
/*     */   }
/*     */ 
/*     */   public synchronized int read(byte[] arg, int offset, int len)
/*     */     throws IOException
/*     */   {
/* 101 */     int numRead = this.m_inStream.read(arg, offset, len);
/* 102 */     if ((this.m_traceStream != null) && (numRead != -1))
/*     */     {
/* 104 */       this.m_traceStream.write(arg, offset, numRead);
/*     */     }
/* 106 */     return numRead;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 111 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TracedInputStream
 * JD-Core Version:    0.5.4
 */