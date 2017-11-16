/*    */ package intradoc.provider;
/*    */ 
/*    */ public class WorkspaceProviderConfigUtils
/*    */ {
/*    */   public static Object retrieveConfig(ProviderInterface ws, String key)
/*    */   {
/* 24 */     Provider provider = ws.getProvider();
/* 25 */     WorkspaceConfigImplementor cfg = (WorkspaceConfigImplementor)provider.getProviderObject("WorkspaceConfig");
/* 26 */     return cfg.retrieveConfigurationObject(key);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 31 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.WorkspaceProviderConfigUtils
 * JD-Core Version:    0.5.4
 */