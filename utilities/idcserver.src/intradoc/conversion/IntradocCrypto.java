/*    */ package intradoc.conversion;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ @Deprecated
/*    */ public class IntradocCrypto
/*    */ {
/*    */   protected SecurityObjects m_securityObjects;
/*    */ 
/*    */   @Deprecated
/*    */   public byte[] encrypt(byte[] data, byte[] key)
/*    */   {
/* 37 */     init();
/* 38 */     return this.m_securityObjects.crypt(data, key, true);
/*    */   }
/*    */ 
/*    */   @Deprecated
/*    */   public byte[] decrypt(byte[] data, byte[] key)
/*    */   {
/* 45 */     init();
/* 46 */     return this.m_securityObjects.crypt(data, key, false);
/*    */   }
/*    */ 
/*    */   protected void init()
/*    */   {
/* 51 */     if (this.m_securityObjects != null)
/*    */       return;
/*    */     try
/*    */     {
/* 55 */       this.m_securityObjects = new SecurityObjects();
/* 56 */       this.m_securityObjects.init();
/*    */     }
/*    */     catch (ServiceException e)
/*    */     {
/* 60 */       Report.trace("system", "Unable to initialize security objects for encrypt/decrypt", e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 68 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71470 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.IntradocCrypto
 * JD-Core Version:    0.5.4
 */