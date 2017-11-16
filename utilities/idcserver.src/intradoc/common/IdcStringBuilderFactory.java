/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcAppendableBase;
/*    */ import intradoc.util.IdcAppendableFactory;
/*    */ 
/*    */ public class IdcStringBuilderFactory
/*    */   implements IdcAppendableFactory
/*    */ {
/* 25 */   public static IdcAppendableFactory m_defaultFactory = new IdcStringBuilderFactory();
/*    */ 
/*    */   public IdcAppendable getIdcAppendable(Object target, int flags)
/*    */   {
/* 29 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 30 */     builder.m_disableToStringReleaseBuffers = ((flags & 0x1000) != 0);
/* 31 */     return builder;
/*    */   }
/*    */ 
/*    */   public void releaseIdcAppendable(IdcAppendableBase a)
/*    */   {
/* 36 */     if ((!SystemUtils.m_isDevelopmentEnvironment) && (!a instanceof IdcStringBuilder))
/*    */       return;
/* 38 */     ((IdcStringBuilder)a).releaseBuffers();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 44 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcStringBuilderFactory
 * JD-Core Version:    0.5.4
 */