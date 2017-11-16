/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcMessageFactoryInterface;
/*     */ 
/*     */ public class IdcMessageFactory
/*     */   implements IdcMessageFactoryInterface
/*     */ {
/*     */   public static GenericTracingCallback m_traceCallback;
/*     */   public static IdcMessageFactory m_defaultFactory;
/*     */ 
/*     */   public static IdcMessageFactory getFactory(ExecutionContext context)
/*     */   {
/*  31 */     if (m_defaultFactory == null)
/*     */     {
/*  33 */       init();
/*     */     }
/*  35 */     return m_defaultFactory;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc()
/*     */   {
/*  40 */     IdcMessageFactory factory = getFactory(null);
/*  41 */     return factory.newIdcMessage();
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage()
/*     */   {
/*  46 */     IdcMessage msg = new IdcMessage(m_traceCallback, 0, null, null, null, null, null, null, (Object[])null);
/*     */ 
/*  50 */     return msg;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(Throwable cause)
/*     */   {
/*  55 */     IdcMessageFactory factory = getFactory(null);
/*  56 */     return factory.newIdcMessage(cause);
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(Throwable cause)
/*     */   {
/*  61 */     if (m_traceCallback == null)
/*     */     {
/*  63 */       init();
/*     */     }
/*  65 */     IdcMessage msg = new IdcMessage(m_traceCallback, 0, cause, null, null, null, null, null, (Object[])null);
/*     */ 
/*  69 */     return msg;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(String key, Object[] args)
/*     */   {
/*  74 */     IdcMessageFactory factory = getFactory(null);
/*  75 */     return factory.newIdcMessage(key, args);
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(String key, Object[] args)
/*     */   {
/*  80 */     if (m_traceCallback == null)
/*     */     {
/*  82 */       init();
/*     */     }
/*  84 */     IdcMessage msg = new IdcMessage(m_traceCallback, 0, null, null, null, null, null, key, args);
/*     */ 
/*  88 */     return msg;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(IdcMessage prior, String key, Object[] args)
/*     */   {
/*  93 */     IdcMessageFactory factory = getFactory(null);
/*  94 */     return factory.newIdcMessage(prior, key, args);
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(IdcMessage prior, String key, Object[] args)
/*     */   {
/*  99 */     if (m_traceCallback == null)
/*     */     {
/* 101 */       init();
/*     */     }
/* 103 */     IdcMessage msg = new IdcMessage(m_traceCallback, 0, null, prior, null, null, null, key, args);
/*     */ 
/* 107 */     return msg;
/*     */   }
/*     */ 
/*     */   public static IdcMessage lc(Throwable prior, String key, Object[] args)
/*     */   {
/* 112 */     IdcMessageFactory factory = getFactory(null);
/* 113 */     return factory.newIdcMessage(prior, key, args);
/*     */   }
/*     */ 
/*     */   public IdcMessage newIdcMessage(Throwable prior, String key, Object[] args)
/*     */   {
/* 118 */     if (m_traceCallback == null)
/*     */     {
/* 120 */       init();
/*     */     }
/* 122 */     IdcMessage msg = new IdcMessage(m_traceCallback, 0, null, null, prior, null, null, key, args);
/*     */ 
/* 126 */     return msg;
/*     */   }
/*     */ 
/*     */   public static synchronized void init()
/*     */   {
/* 131 */     if (m_traceCallback == null)
/*     */     {
/*     */       try
/*     */       {
/* 135 */         m_traceCallback = (GenericTracingCallback)Class.forName("intradoc.common.ReportTracingCallback").newInstance();
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 140 */         ignore.printStackTrace();
/*     */       }
/*     */     }
/* 143 */     if (m_defaultFactory != null)
/*     */       return;
/* 145 */     m_defaultFactory = new IdcMessageFactory();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 151 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcMessageFactory
 * JD-Core Version:    0.5.4
 */