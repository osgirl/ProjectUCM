/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppenderBase;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class IdcAppendableWriter
/*     */   implements IdcAppendable
/*     */ {
/*  28 */   public Writer m_writer = null;
/*  29 */   public IOException m_exception = null;
/*     */ 
/*     */   public IdcAppendableWriter(Writer w)
/*     */   {
/*  33 */     this.m_writer = w;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(char c)
/*     */   {
/*     */     try
/*     */     {
/*  40 */       this.m_writer.write(c);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  44 */       this.m_exception = e;
/*     */     }
/*  46 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(char[] srcArray, int start, int length)
/*     */   {
/*     */     try
/*     */     {
/*  53 */       this.m_writer.write(srcArray, start, length);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  57 */       this.m_exception = e;
/*     */     }
/*  59 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(CharSequence seq)
/*     */   {
/*     */     try
/*     */     {
/*  66 */       this.m_writer.write(seq.toString());
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  70 */       this.m_exception = e;
/*     */     }
/*  72 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(CharSequence seq, int start, int length)
/*     */   {
/*     */     try
/*     */     {
/*  79 */       seq = new IdcSubSequence(seq, start, start + length);
/*  80 */       this.m_writer.write(seq.toString());
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  84 */       this.m_exception = e;
/*     */     }
/*  86 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(IdcAppender appendable)
/*     */   {
/*  91 */     appendable.appendTo(this);
/*  92 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable append(IdcAppenderBase appendable)
/*     */   {
/*  97 */     appendable.appendTo(this);
/*  98 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendable appendObject(Object obj)
/*     */   {
/*     */     try
/*     */     {
/* 105 */       this.m_writer.write(obj.toString());
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 109 */       this.m_exception = e;
/*     */     }
/* 111 */     return this;
/*     */   }
/*     */ 
/*     */   public void checkForException() throws IOException
/*     */   {
/* 116 */     if (this.m_exception == null)
/*     */       return;
/* 118 */     throw this.m_exception;
/*     */   }
/*     */ 
/*     */   public boolean truncate(int l)
/*     */   {
/* 124 */     ServiceException e = new ServiceException(null, "syTruncateNotSupported", new Object[] { this.m_writer.getClass().getName() });
/*     */ 
/* 126 */     e.m_isWrapped = true;
/* 127 */     IOException ioe = new IOException();
/* 128 */     ioe.initCause(e);
/* 129 */     this.m_exception = ioe;
/* 130 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71949 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcAppendableWriter
 * JD-Core Version:    0.5.4
 */