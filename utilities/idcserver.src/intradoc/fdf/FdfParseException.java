/*    */ package intradoc.fdf;
/*    */ 
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.IdcStringBuilderFactory;
/*    */ import intradoc.util.IdcException;
/*    */ 
/*    */ public class FdfParseException extends IdcException
/*    */ {
/*    */   FdfParseException()
/*    */   {
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   FdfParseException(String msg)
/*    */   {
/* 39 */     init(null, 0, IdcMessageFactory.lc(), msg);
/*    */   }
/*    */ 
/*    */   FdfParseException(String key, Object[] args)
/*    */   {
/* 44 */     init(null, 0, IdcMessageFactory.lc(key, args), null);
/*    */   }
/*    */ 
/*    */   public void initFactory()
/*    */   {
/* 50 */     this.m_factory = IdcStringBuilderFactory.m_defaultFactory;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.fdf.FdfParseException
 * JD-Core Version:    0.5.4
 */