/*    */ package intradoc.common;
/*    */ 
/*    */ public class ConfigFileDescriptorFactorySimpleImplementor
/*    */   implements ConfigFileDescriptorFactory
/*    */ {
/*    */   public void setWorkspace(Object workspace)
/*    */   {
/*    */   }
/*    */ 
/*    */   public ConfigFileDescriptor getFileDescriptor(String path)
/*    */   {
/* 37 */     ConfigFileDescriptor cfgDescriptor = new ConfigFileDescriptorFileImplementor();
/* 38 */     return cfgDescriptor;
/*    */   }
/*    */ 
/*    */   public int getStoreLocation(String path)
/*    */   {
/* 43 */     return 1;
/*    */   }
/*    */ 
/*    */   public boolean storeInDB(String path)
/*    */   {
/* 48 */     return false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 53 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97497 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ConfigFileDescriptorFactorySimpleImplementor
 * JD-Core Version:    0.5.4
 */