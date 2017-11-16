/*    */ package intradoc.provider;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class ProviderConfigImpl
/*    */   implements ProviderConfig
/*    */ {
/*    */   protected Provider m_provider;
/*    */ 
/*    */   public ProviderConfigImpl()
/*    */   {
/* 29 */     this.m_provider = null;
/*    */   }
/*    */ 
/*    */   public void init(Provider pr) throws DataException, ServiceException {
/* 33 */     this.m_provider = pr;
/*    */   }
/*    */ 
/*    */   public void loadResources()
/*    */     throws DataException, ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 43 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProviderConfigImpl
 * JD-Core Version:    0.5.4
 */