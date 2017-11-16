/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ConfigFileDescriptor;
/*    */ import intradoc.common.ConfigFileDescriptorFactorySimpleImplementor;
/*    */ import intradoc.common.ConfigFileDescriptorFileImplementor;
/*    */ import intradoc.common.ConfigFileParameters;
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.data.Workspace;
/*    */ 
/*    */ public class ConfigFileDescriptorFactoryDBImplementor extends ConfigFileDescriptorFactorySimpleImplementor
/*    */ {
/*    */   public static Workspace m_workspace;
/*    */ 
/*    */   public void setWorkspace(Object workspace)
/*    */   {
/* 34 */     m_workspace = (Workspace)workspace;
/* 35 */     ConfigFileDescriptorSysDBImplementor.m_workspace = (Workspace)workspace;
/*    */   }
/*    */ 
/*    */   public ConfigFileDescriptor getFileDescriptor(String path)
/*    */   {
/* 41 */     ConfigFileDescriptor cfgDescriptor = null;
/* 42 */     switch (getStoreLocation(path))
/*    */     {
/*    */     case 1:
/* 45 */       cfgDescriptor = new ConfigFileDescriptorFileImplementor();
/* 46 */       break;
/*    */     case 2:
/* 48 */       cfgDescriptor = new ConfigFileDescriptorSysDBImplementor();
/* 49 */       break;
/*    */     case 4:
/* 51 */       cfgDescriptor = new ConfigFileDescriptorDataSourceImplementor();
/*    */     case 3:
/*    */     }
/*    */ 
/* 55 */     return cfgDescriptor;
/*    */   }
/*    */ 
/*    */   public int getStoreLocation(String path)
/*    */   {
/* 61 */     path = FileUtils.directorySlashes(path);
/*    */ 
/* 63 */     if (path.startsWith("idc://idcproviders/"))
/*    */     {
/* 65 */       return 4;
/*    */     }
/* 67 */     if (ConfigFileParameters.isSharedDirectory(path))
/*    */     {
/* 70 */       return 2;
/*    */     }
/* 72 */     return 1;
/*    */   }
/*    */ 
/*    */   public boolean storeInDB(String path)
/*    */   {
/* 78 */     switch (getStoreLocation(path)) {
/*    */     case 2:
/*    */     case 4:
/* 82 */       return true;
/*    */     case 1:
/* 84 */       return false;
/*    */     case 3:
/*    */     }
/* 86 */     return false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 91 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100902 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ConfigFileDescriptorFactoryDBImplementor
 * JD-Core Version:    0.5.4
 */