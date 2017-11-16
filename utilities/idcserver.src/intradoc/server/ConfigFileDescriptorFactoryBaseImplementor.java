/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ConfigFileDescriptor;
/*    */ import intradoc.common.ConfigFileDescriptorFactorySimpleImplementor;
/*    */ import intradoc.common.ConfigFileDescriptorFileImplementor;
/*    */ 
/*    */ public class ConfigFileDescriptorFactoryBaseImplementor extends ConfigFileDescriptorFactorySimpleImplementor
/*    */ {
/*    */   public void setWorkspace(Object workspace)
/*    */   {
/*    */   }
/*    */ 
/*    */   public ConfigFileDescriptor getFileDescriptor(String path)
/*    */   {
/* 35 */     ConfigFileDescriptor cfgDescriptor = null;
/*    */ 
/* 37 */     switch (getStoreLocation(path))
/*    */     {
/*    */     case 1:
/* 40 */       cfgDescriptor = new ConfigFileDescriptorFileImplementor();
/* 41 */       break;
/*    */     case 4:
/* 43 */       cfgDescriptor = new ConfigFileDescriptorDataSourceImplementor();
/*    */     }
/*    */ 
/* 47 */     return cfgDescriptor;
/*    */   }
/*    */ 
/*    */   public int getStoreLocation(String path)
/*    */   {
/* 53 */     if (path.startsWith("idc://idcproviders/"))
/*    */     {
/* 55 */       return 4;
/*    */     }
/* 57 */     return 1;
/*    */   }
/*    */ 
/*    */   public boolean storeInDB(String path)
/*    */   {
/* 63 */     switch (getStoreLocation(path))
/*    */     {
/*    */     case 4:
/* 66 */       return true;
/*    */     case 1:
/* 68 */       return false;
/*    */     }
/* 70 */     return false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 75 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100902 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ConfigFileDescriptorFactoryBaseImplementor
 * JD-Core Version:    0.5.4
 */