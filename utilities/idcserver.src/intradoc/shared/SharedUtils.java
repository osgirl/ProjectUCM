/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.util.IdcMessage;
/*     */ 
/*     */ public class SharedUtils
/*     */ {
/*     */   public static final int F_LOG_ERROR = 1;
/*     */   public static final int F_LOG_ONLY = 3;
/*     */   public static final int F_ALWAYS_WRAP_EXCEPTION = 4;
/*     */   public static final int F_WRAP_UNCOMMON_EXCEPTIONS = 8;
/*     */ 
/*     */   public static void logCommonException(int flags, Exception e, String key, Object[] args)
/*     */   {
/*  36 */     IdcMessage msg = IdcMessageFactory.lc(key, args);
/*  37 */     handleCommonExceptionInternal(flags | 0x3, e, msg);
/*     */   }
/*     */ 
/*     */   public static void logCommonException(int flags, Exception e, IdcMessage msg)
/*     */   {
/*  42 */     handleCommonExceptionInternal(flags | 0x3, e, msg);
/*     */   }
/*     */ 
/*     */   public static void logCommonException(Exception exception, String msg, int flags)
/*     */   {
/*  47 */     IdcMessage idcmsg = null;
/*  48 */     if (msg != null)
/*     */     {
/*  50 */       idcmsg = IdcMessageFactory.lc();
/*  51 */       idcmsg.m_msgEncoded = msg;
/*     */     }
/*  53 */     handleCommonExceptionInternal(flags | 0x3, exception, idcmsg);
/*     */   }
/*     */ 
/*     */   public static void handleCommonException(int flags, Exception e, String key, Object[] args)
/*     */     throws DataException, ServiceException
/*     */   {
/*  59 */     IdcMessage msg = IdcMessageFactory.lc(key, args);
/*  60 */     handleCommonException(flags, e, msg);
/*     */   }
/*     */ 
/*     */   public static void handleCommonException(int flags, Exception exception, IdcMessage msg)
/*     */     throws DataException, ServiceException
/*     */   {
/*  66 */     Exception e = handleCommonExceptionInternal(flags, exception, msg);
/*  67 */     if (e == null)
/*     */       return;
/*  69 */     if (e instanceof ServiceException)
/*     */     {
/*  71 */       throw ((ServiceException)e);
/*     */     }
/*  73 */     if (e instanceof DataException)
/*     */     {
/*  75 */       throw ((DataException)e);
/*     */     }
/*  77 */     AssertionError err = new AssertionError(msg);
/*  78 */     SystemUtils.setExceptionCause(err, e);
/*  79 */     throw err;
/*     */   }
/*     */ 
/*     */   public static void handleCommonException(Exception exception, String msg, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/*  86 */     IdcMessage idcmsg = null;
/*  87 */     if (msg != null)
/*     */     {
/*  89 */       idcmsg = IdcMessageFactory.lc();
/*  90 */       idcmsg.m_msgEncoded = msg;
/*     */     }
/*  92 */     idcmsg.m_msgEncoded = msg;
/*  93 */     handleCommonException(flags, exception, idcmsg);
/*     */   }
/*     */ 
/*     */   public static Exception handleCommonExceptionInternal(int flags, Exception e, IdcMessage msg)
/*     */   {
/*  98 */     if (e == null)
/*     */     {
/* 100 */       return null;
/*     */     }
/* 102 */     if ((flags & 0x1) != 0)
/*     */     {
/* 104 */       Report.error(null, e, msg);
/* 105 */       if ((flags & 0x3) == 3)
/*     */       {
/* 107 */         return null;
/*     */       }
/*     */     }
/* 110 */     if ((flags & 0x4) != 0)
/*     */     {
/* 112 */       ServiceException se = new ServiceException(e, msg);
/* 113 */       return se;
/*     */     }
/* 115 */     if ((e instanceof DataException) || (e instanceof ServiceException))
/*     */     {
/* 117 */       return e;
/*     */     }
/* 119 */     if (msg == null)
/*     */     {
/* 121 */       msg = IdcMessageFactory.lc("syUnknownError", new Object[0]);
/*     */     }
/* 123 */     if ((flags & 0x8) != 0)
/*     */     {
/* 125 */       ServiceException se = new ServiceException(e, msg);
/* 126 */       return se;
/*     */     }
/* 128 */     return e;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74647 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SharedUtils
 * JD-Core Version:    0.5.4
 */