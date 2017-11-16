/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.IdcStringBuilderFactory;
/*    */ import intradoc.common.SystemUtils;
/*    */ import intradoc.util.IdcException;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class DataException extends IdcException
/*    */ {
/*    */   public DataException(String errMsg)
/*    */   {
/* 48 */     init(null, -1, IdcMessageFactory.lc(), errMsg);
/*    */   }
/*    */ 
/*    */   public DataException(String errMsg, Throwable t)
/*    */   {
/* 54 */     init(t, -1, IdcMessageFactory.lc(), errMsg);
/*    */   }
/*    */ 
/*    */   public DataException(Throwable t, String key, Object[] args)
/*    */   {
/* 60 */     super(t, -1, IdcMessageFactory.lc(key, args));
/*    */   }
/*    */ 
/*    */   public DataException(Throwable t, IdcMessage msg)
/*    */   {
/* 65 */     super(t, msg);
/*    */   }
/*    */ 
/*    */   public DataException(Throwable t, int errorCode, String key, Object[] args)
/*    */   {
/* 71 */     super(t, errorCode, IdcMessageFactory.lc(key, args));
/*    */   }
/*    */ 
/*    */   public DataException(Throwable t, int errorCode, IdcMessage msg)
/*    */   {
/* 76 */     super(t, errorCode, msg);
/*    */   }
/*    */ 
/*    */   public void initFactory()
/*    */   {
/* 82 */     this.m_factory = IdcStringBuilderFactory.m_defaultFactory;
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   public void initExceptionCause(Throwable t)
/*    */   {
/* 89 */     if (t == null)
/*    */     {
/* 91 */       return;
/*    */     }
/* 93 */     SystemUtils.setExceptionCause(this, t);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 99 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 74169 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataException
 * JD-Core Version:    0.5.4
 */