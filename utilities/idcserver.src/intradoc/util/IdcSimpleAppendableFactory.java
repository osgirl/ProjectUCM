/*    */ package intradoc.util;
/*    */ 
/*    */ public class IdcSimpleAppendableFactory
/*    */   implements IdcAppendableFactory
/*    */ {
/*    */   public IdcAppendableBase getIdcAppendable(Object target, int flags)
/*    */   {
/* 24 */     Appendable app = null;
/*    */     try
/*    */     {
/* 27 */       app = new StringBuilder();
/*    */     }
/*    */     catch (Throwable ignore)
/*    */     {
/* 31 */       app = new StringBuffer();
/*    */     }
/*    */ 
/* 34 */     return IdcAppendableBaseWrapper.wrap(app);
/*    */   }
/*    */ 
/*    */   public void releaseIdcAppendable(IdcAppendableBase a)
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcSimpleAppendableFactory
 * JD-Core Version:    0.5.4
 */