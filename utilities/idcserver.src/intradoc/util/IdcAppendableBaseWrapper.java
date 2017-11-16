/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.lang.reflect.Method;
/*     */ 
/*     */ public class IdcAppendableBaseWrapper
/*     */   implements IdcAppendableBase
/*     */ {
/*  27 */   public IdcMessageFactoryInterface m_factory = new IdcMessageUtils();
/*     */   public Appendable m_appendable;
/*  31 */   public boolean m_triedReflection = false;
/*     */   public Method m_truncateMethod;
/*     */   public IdcException m_exception;
/*     */ 
/*     */   public IdcAppendableBaseWrapper(Appendable appendable)
/*     */   {
/*  39 */     this.m_appendable = appendable;
/*     */   }
/*     */ 
/*     */   public static IdcAppendableBase wrap(Appendable appendable)
/*     */   {
/*  44 */     if (appendable instanceof IdcAppendableBase)
/*     */     {
/*  46 */       return (IdcAppendableBase)appendable;
/*     */     }
/*  48 */     return new IdcAppendableBaseWrapper(appendable);
/*     */   }
/*     */ 
/*     */   public void handleException(Throwable t)
/*     */   {
/*  53 */     this.m_exception = new IdcException(t, null);
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(char c)
/*     */   {
/*  60 */     if (this.m_exception != null)
/*     */     {
/*  62 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  67 */       this.m_appendable.append(c);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  71 */       handleException(e);
/*     */     }
/*  73 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(char[] srcArray, int srcBegin, int length)
/*     */   {
/*  78 */     if (this.m_exception != null)
/*     */     {
/*  80 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  85 */       int end = srcBegin + length;
/*  86 */       for (int i = srcBegin; i < end; ++i)
/*     */       {
/*  88 */         char c = srcArray[i];
/*  89 */         this.m_appendable.append(c);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  94 */       handleException(e);
/*     */     }
/*  96 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(CharSequence seq)
/*     */   {
/* 101 */     if (this.m_exception != null)
/*     */     {
/* 103 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 108 */       if (seq instanceof IdcAppenderBase)
/*     */       {
/* 110 */         ((IdcAppenderBase)seq).appendTo(this);
/*     */       }
/*     */       else
/*     */       {
/* 114 */         this.m_appendable.append(seq);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 119 */       handleException(e);
/*     */     }
/* 121 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(CharSequence seq, int srcBegin, int length)
/*     */   {
/* 126 */     if (this.m_exception != null)
/*     */     {
/* 128 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 133 */       int end = srcBegin + length;
/* 134 */       for (int i = srcBegin; i < end; ++i)
/*     */       {
/* 136 */         char c = seq.charAt(i);
/* 137 */         this.m_appendable.append(c);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 142 */       handleException(e);
/*     */     }
/* 144 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase append(IdcAppenderBase appender)
/*     */   {
/* 149 */     if (this.m_exception != null)
/*     */     {
/* 151 */       return this;
/*     */     }
/*     */ 
/* 154 */     appender.appendTo(this);
/* 155 */     return this;
/*     */   }
/*     */ 
/*     */   public IdcAppendableBase appendObject(Object o)
/*     */   {
/* 160 */     if (this.m_exception != null)
/*     */     {
/* 162 */       return this;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 167 */       if (o instanceof IdcAppenderBase)
/*     */       {
/* 169 */         ((IdcAppenderBase)o).appendTo(this);
/*     */       }
/*     */       else
/*     */       {
/* 173 */         this.m_appendable.append(o.toString());
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 178 */       handleException(e);
/*     */     }
/* 180 */     return this;
/*     */   }
/*     */ 
/*     */   public boolean truncate(int l)
/*     */   {
/* 185 */     if (this.m_exception != null)
/*     */     {
/* 187 */       return false;
/*     */     }
/*     */ 
/* 190 */     if (this.m_appendable instanceof IdcAppendableBase)
/*     */     {
/* 192 */       return ((IdcAppendableBase)this.m_appendable).truncate(l);
/*     */     }
/* 194 */     if (this.m_appendable instanceof StringBuilder)
/*     */     {
/* 196 */       ((StringBuilder)this.m_appendable).setLength(l);
/* 197 */       return true;
/*     */     }
/*     */     try
/*     */     {
/* 201 */       if (this.m_appendable instanceof StringBuffer)
/*     */       {
/* 203 */         ((StringBuffer)this.m_appendable).setLength(l);
/* 204 */         return true;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/*     */ 
/* 212 */     Exception exception = null;
/* 213 */     if (!this.m_triedReflection)
/*     */     {
/* 215 */       this.m_triedReflection = true;
/*     */     }
/*     */     try {
/* 218 */       Class cl = this.m_appendable.getClass();
/* 219 */       Integer integer = new Integer(l);
/* 220 */       Method m = cl.getMethod("truncate", new Class[] { Integer.TYPE });
/* 221 */       m.invoke(this.m_appendable, new Object[] { integer });
/* 222 */       this.m_truncateMethod = m;
/* 223 */       return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 227 */       exception = e;
/*     */       Integer integer;
/* 230 */       if (this.m_truncateMethod != null)
/*     */       {
/* 232 */         integer = new Integer(l);
/*     */       }
/*     */       try {
/* 235 */         this.m_truncateMethod.invoke(this.m_appendable, new Object[] { integer });
/* 236 */         return true;
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 240 */         exception = e;
/*     */ 
/* 244 */         IdcException e = new IdcException(null, this.m_factory.newIdcMessage("syTruncateNotSupported", new Object[] { this.m_appendable.getClass().getName() }));
/*     */ 
/* 246 */         if (exception != null)
/*     */         {
/* 248 */           e.addCause(exception);
/*     */         }
/* 250 */         this.m_exception = e;
/*     */       }
/* 251 */     }return false;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 257 */     return this.m_appendable.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 264 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcAppendableBaseWrapper
 * JD-Core Version:    0.5.4
 */