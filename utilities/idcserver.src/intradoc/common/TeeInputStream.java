/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class TeeInputStream extends FilterInputStream
/*     */ {
/*  35 */   OutputStream[] m_out = null;
/*     */ 
/*  42 */   boolean m_canWriteThrowException = true;
/*     */ 
/*     */   public TeeInputStream(InputStream m_in)
/*     */   {
/*  47 */     super(m_in);
/*     */   }
/*     */ 
/*     */   public void addOutputStream(OutputStream out)
/*     */   {
/*  52 */     int length = (null == this.m_out) ? 0 : this.m_out.length;
/*  53 */     OutputStream[] newStreams = new OutputStream[length + 1];
/*  54 */     if (null != this.m_out)
/*     */     {
/*  56 */       System.arraycopy(this.m_out, 0, newStreams, 0, length);
/*     */     }
/*  58 */     newStreams[length] = out;
/*  59 */     this.m_out = newStreams;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  70 */     throw new IOException("use read(byte[]) instead of read()");
/*     */   }
/*     */ 
/*     */   public int read(byte[] b)
/*     */     throws IOException
/*     */   {
/*  77 */     return read(b, 0, b.length);
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/*  84 */     int returnValue = this.in.read(b, off, len);
/*  85 */     if ((returnValue > 0) && (null != this.m_out))
/*     */     {
/*  87 */       for (int i = 0; i < this.m_out.length; ++i)
/*     */       {
/*  89 */         if (null == this.m_out[i])
/*     */           continue;
/*  91 */         if (this.m_canWriteThrowException)
/*     */         {
/*  93 */           this.m_out[i].write(b, off, returnValue);
/*     */         }
/*     */         else
/*     */         {
/*     */           try
/*     */           {
/*  99 */             this.m_out[i].write(b, off, returnValue);
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/* 103 */             if (SystemUtils.m_verbose)
/*     */             {
/* 105 */               Report.debug("system", null, ignore);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 112 */     return returnValue;
/*     */   }
/*     */ 
/*     */   public void finalize()
/*     */     throws Throwable
/*     */   {
/* 120 */     this.m_out = null;
/* 121 */     super.finalize();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 127 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TeeInputStream
 * JD-Core Version:    0.5.4
 */