/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class LengthLimitedInputStream extends FilterInputStream
/*     */ {
/*     */   public long m_length;
/*     */   public long m_numLeft;
/*     */ 
/*     */   public LengthLimitedInputStream(InputStream is, long length)
/*     */   {
/*  37 */     super(is);
/*  38 */     this.m_length = length;
/*  39 */     this.m_numLeft = length;
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  45 */     if (this.m_numLeft <= 0L)
/*     */     {
/*  47 */       return -1;
/*     */     }
/*     */ 
/*  50 */     long numToRead = this.m_numLeft;
/*  51 */     if (numToRead > len)
/*     */     {
/*  53 */       numToRead = len;
/*     */     }
/*     */ 
/*  56 */     int numRead = super.read(b, off, (int)numToRead);
/*  57 */     if (numRead > 0)
/*     */     {
/*  59 */       this.m_numLeft -= numRead;
/*     */     }
/*     */ 
/*  62 */     return numRead;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  68 */     if (this.m_numLeft <= 0L)
/*     */     {
/*  70 */       return -1;
/*     */     }
/*     */ 
/*  73 */     int ret = super.read();
/*  74 */     if (ret == -1)
/*     */     {
/*  76 */       this.m_numLeft = 0L;
/*     */     }
/*     */     else
/*     */     {
/*  80 */       this.m_numLeft -= 1L;
/*     */     }
/*     */ 
/*  83 */     return ret;
/*     */   }
/*     */ 
/*     */   public int available()
/*     */     throws IOException
/*     */   {
/*  89 */     if (this.m_numLeft <= 0L)
/*     */     {
/*  91 */       return 0;
/*     */     }
/*     */ 
/*  94 */     int available = super.available();
/*  95 */     if (available > this.m_numLeft)
/*     */     {
/*  97 */       available = (int)this.m_numLeft;
/*     */     }
/*     */ 
/* 100 */     return available;
/*     */   }
/*     */ 
/*     */   public long skip(long n)
/*     */     throws IOException
/*     */   {
/* 106 */     if (this.m_numLeft <= 0L)
/*     */     {
/* 108 */       return 0L;
/*     */     }
/*     */ 
/* 111 */     long numToSkip = this.m_numLeft;
/* 112 */     if (numToSkip > n)
/*     */     {
/* 114 */       numToSkip = n;
/*     */     }
/*     */ 
/* 117 */     long numSkipped = super.skip(numToSkip);
/* 118 */     if (numSkipped > 0L)
/*     */     {
/* 120 */       this.m_numLeft -= numSkipped;
/*     */     }
/*     */ 
/* 123 */     return numSkipped;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=dev,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LengthLimitedInputStream
 * JD-Core Version:    0.5.4
 */