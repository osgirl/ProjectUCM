/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.ArrayList;
/*     */ 
/*     */ public class ServiceException extends IdcException
/*     */ {
/*     */ 
/*     */   @Deprecated
/*  47 */   public ArrayList<Throwable> m_associatedExceptions = this.m_causeList;
/*     */ 
/*     */   public ServiceException(String errMsg)
/*     */   {
/*  55 */     init(null, -1, IdcMessageFactory.lc(), errMsg);
/*     */   }
/*     */ 
/*     */   public ServiceException(int errorCode, String errMsg)
/*     */   {
/*  61 */     init(null, errorCode, IdcMessageFactory.lc(), errMsg);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t)
/*     */   {
/*  68 */     init(t, 0, IdcMessageFactory.lc(), null);
/*     */   }
/*     */ 
/*     */   public ServiceException(int errorCode, Throwable t)
/*     */   {
/*  74 */     init(t, errorCode, IdcMessageFactory.lc(), null);
/*     */   }
/*     */ 
/*     */   public ServiceException(String msg, Throwable t)
/*     */   {
/*  81 */     init(t, 0, IdcMessageFactory.lc(), msg);
/*     */   }
/*     */ 
/*     */   public ServiceException(int errorCode, String msg, Throwable t)
/*     */   {
/*  87 */     init(t, errorCode, IdcMessageFactory.lc(), msg);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t, String key, Object[] args)
/*     */   {
/* 103 */     init(t, 0, IdcMessageFactory.lc(key, args), null);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t, IdcMessage msg)
/*     */   {
/* 110 */     init(t, 0, msg, null);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t, int errorCode, String key, Object[] args)
/*     */   {
/* 116 */     init(t, errorCode, IdcMessageFactory.lc(key, args), null);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t, int errorCode, IdcMessage msg)
/*     */   {
/* 122 */     init(t, errorCode, msg, null);
/*     */   }
/*     */ 
/*     */   public ServiceException(Throwable t, int errorCode)
/*     */   {
/* 128 */     init(t, errorCode, null, null);
/*     */   }
/*     */ 
/*     */   public void initFactory()
/*     */   {
/* 135 */     this.m_factory = IdcStringBuilderFactory.m_defaultFactory;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void initExceptionCause(Throwable t)
/*     */   {
/* 142 */     if (t == null)
/*     */     {
/* 144 */       return;
/*     */     }
/*     */ 
/* 147 */     if (t instanceof ServiceException)
/*     */     {
/* 149 */       ServiceException se = (ServiceException)t;
/* 150 */       this.m_errorCode = se.m_errorCode;
/* 151 */       this.m_protocolCode = se.m_protocolCode;
/*     */     }
/*     */ 
/* 154 */     SystemUtils.setExceptionCause(this, t);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getFullErrorMessage(Throwable t)
/*     */   {
/* 161 */     IdcMessage msg = IdcMessageFactory.lc(t);
/* 162 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 167 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ServiceException
 * JD-Core Version:    0.5.4
 */