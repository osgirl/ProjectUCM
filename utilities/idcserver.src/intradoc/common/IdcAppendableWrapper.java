/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import intradoc.util.IdcAppenderBase;
/*     */ import java.io.IOException;
/*     */ 
/*     */ public class IdcAppendableWrapper
/*     */   implements IdcAppendable
/*     */ {
/*     */   public Appendable m_appendable;
/*     */   public ServiceException m_exception;
/*     */ 
/*     */   public IdcAppendableWrapper(Appendable appendable)
/*     */   {
/*  35 */     this.m_appendable = appendable;
/*     */   }
/*     */ 
/*     */   public static IdcAppendable wrap(Appendable appendable)
/*     */   {
/*  40 */     if (appendable instanceof IdcAppendable)
/*     */     {
/*  42 */       return (IdcAppendable)appendable;
/*     */     }
/*  44 */     return new IdcAppendableWrapper(appendable);
/*     */   }
/*     */ 
/*     */   public void handleException(Throwable t)
/*     */   {
/*  49 */     this.m_exception = new ServiceException(t);
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(char c)
/*     */   {
/*  56 */     if (this.m_exception != null)
/*     */     {
/*  58 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  63 */       this.m_appendable.append(c);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  67 */       handleException(e);
/*     */     }
/*  69 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(char[] srcArray, int srcBegin, int length)
/*     */   {
/*  74 */     if (this.m_exception != null)
/*     */     {
/*  76 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  81 */       int end = srcBegin + length;
/*  82 */       for (int i = srcBegin; i < end; ++i)
/*     */       {
/*  84 */         char c = srcArray[i];
/*  85 */         this.m_appendable.append(c);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  90 */       handleException(e);
/*     */     }
/*  92 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(CharSequence seq)
/*     */   {
/*  97 */     if (this.m_exception != null)
/*     */     {
/*  99 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 104 */       if (seq instanceof IdcAppenderBase)
/*     */       {
/* 106 */         ((IdcAppenderBase)seq).appendTo(this);
/*     */       }
/*     */       else
/*     */       {
/* 110 */         this.m_appendable.append(seq);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 115 */       handleException(e);
/*     */     }
/* 117 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(CharSequence seq, int srcBegin, int length)
/*     */   {
/* 122 */     if (this.m_exception != null)
/*     */     {
/* 124 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 129 */       int end = srcBegin + length;
/* 130 */       for (int i = srcBegin; i < end; ++i)
/*     */       {
/* 132 */         char c = seq.charAt(i);
/* 133 */         this.m_appendable.append(c);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 138 */       handleException(e);
/*     */     }
/* 140 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(IdcAppenderBase appender)
/*     */   {
/* 145 */     if (this.m_exception != null)
/*     */     {
/* 147 */       return this;
/*     */     }
/*     */ 
/* 150 */     appender.appendTo(this);
/* 151 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase appendObject(Object o)
/*     */   {
/* 156 */     if (this.m_exception != null)
/*     */     {
/* 158 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 163 */       if (o instanceof IdcAppender)
/*     */       {
/* 165 */         ((IdcAppender)o).appendTo(this);
/*     */       }
/*     */       else
/*     */       {
/* 169 */         this.m_appendable.append(o.toString());
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 174 */       handleException(e);
/*     */     }
/* 176 */     return this;
/*     */   }
/*     */ 
/*     */   public boolean truncate(int l)
/*     */   {
/* 181 */     ServiceException e = new ServiceException(null, "syTruncateNotSupported", new Object[] { this.m_appendable.getClass().getName() });
/*     */ 
/* 183 */     this.m_exception = e;
/* 184 */     return false;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 190 */     return this.m_appendable.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 197 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcAppendableWrapper
 * JD-Core Version:    0.5.4
 */