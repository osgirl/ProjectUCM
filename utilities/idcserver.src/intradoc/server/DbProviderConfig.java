/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.common.NumberUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.provider.Provider;
/*    */ import intradoc.provider.WorkspaceProviderConfig;
/*    */ import intradoc.resource.ResourceLoader;
/*    */ 
/*    */ public class DbProviderConfig extends WorkspaceProviderConfig
/*    */ {
/*    */   public void init(Provider pr)
/*    */     throws DataException, ServiceException
/*    */   {
/* 35 */     super.init(pr);
/*    */   }
/*    */ 
/*    */   public void loadResources()
/*    */     throws DataException, ServiceException
/*    */   {
/* 41 */     if (this.m_loaded)
/*    */     {
/* 43 */       return;
/*    */     }
/*    */ 
/* 46 */     int num = NumberUtils.parseInteger(this.m_provider.getProviderData().getLocal("NumConnections"), 2);
/*    */ 
/* 49 */     Workspace workspace = (Workspace)this.m_provider.getProvider();
/* 50 */     workspace.initConnectionPoolAndConfiguration(num, 0, null);
/*    */ 
/* 52 */     DataBinder providerData = this.m_provider.getProviderData();
/* 53 */     String queryFile = providerData.getLocal("QueryResourceFile");
/* 54 */     if (queryFile == null)
/*    */     {
/* 56 */       return;
/*    */     }
/*    */ 
/* 59 */     String location = this.m_provider.getLocation();
/* 60 */     if ((location == null) || (location.equals("")))
/*    */     {
/* 62 */       location = providerData.getLocal("DatabaseDir");
/*    */     }
/* 64 */     if ((location == null) || (location.equals("")))
/*    */     {
/* 66 */       location = LegacyDirectoryLocator.getIntradocDir() + "bin/";
/*    */     }
/*    */ 
/* 69 */     String absResPath = FileUtils.getAbsolutePath(location, queryFile);
/* 70 */     String tableNames = providerData.getLocal("QueryResourceTables");
/* 71 */     String cmptName = this.m_provider.getName();
/* 72 */     ResourceLoader.loadQueries(absResPath, tableNames, workspace, cmptName);
/* 73 */     this.m_loaded = true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 78 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 68471 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DbProviderConfig
 * JD-Core Version:    0.5.4
 */