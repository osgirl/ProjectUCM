/*     */ package intradoc.admin;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.IdcManagerBase;
/*     */ import intradoc.server.IdcServerOutput;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedPageMergerData;
/*     */ 
/*     */ public class IdcAdminManager extends IdcManagerBase
/*     */ {
/*     */   protected int getThreadCount()
/*     */   {
/*  37 */     return SystemUtils.getThreadCount();
/*     */   }
/*     */ 
/*     */   public String getServiceDisplayName()
/*     */   {
/*  46 */     return "IDC Content Admin Service";
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  56 */     IdcServerOutput.init();
/*     */ 
/*  58 */     AdminDirectoryLocator.init();
/*  59 */     IdcSystemConfig.initConfigEarly(IdcSystemConfig.F_ADMIN_SERVER);
/*     */ 
/*  61 */     AdminServerUtils.m_isSimplifiedServer = EnvUtils.isHostedInAppServer();
/*     */ 
/*  63 */     String isSimplifiedServer = SharedObjects.getEnvironmentValue("IsSimplifiedAdminServer");
/*  64 */     if ((isSimplifiedServer != null) && (isSimplifiedServer.length() > 0))
/*     */     {
/*  66 */       AdminServerUtils.m_isSimplifiedServer = StringUtils.convertToBool(isSimplifiedServer, AdminServerUtils.m_isSimplifiedServer);
/*     */     }
/*     */ 
/*  69 */     SharedObjects.putEnvironmentValue("IsSimplifiedAdminServer", (AdminServerUtils.m_isSimplifiedServer) ? "1" : "");
/*     */ 
/*  71 */     if (AdminServerUtils.m_directActionsInterface != null)
/*     */     {
/*  73 */       SharedObjects.putEnvironmentValue("HasAppServerAdminActions", "1");
/*     */     }
/*  75 */     if (AdminServerUtils.m_isSimplifiedServer)
/*     */     {
/*  78 */       String contentServerType = AdminServerUtils.determineAdministratedContentServerType();
/*  79 */       SharedObjects.putEnvironmentValue("contentServerType", contentServerType);
/*     */     }
/*     */ 
/*  84 */     IdcAdminSystemLoader.loadConfigInfo();
/*  85 */     setMessagePrefix();
/*     */ 
/*  88 */     SharedObjects.putEnvironmentValue("IsAutoProviderMonitor", "1");
/*     */ 
/*  91 */     SharedObjects.putEnvironmentValue("StartRefineryQueueMonitorThread", "0");
/*     */ 
/*  94 */     SharedObjects.putEnvironmentValue("isUseComponentDBInstallToCreateTable", "0");
/*     */ 
/*  97 */     boolean useSocket = !EnvUtils.isHostedInAppServer();
/*  98 */     IdcAdminSystemLoader.init(useSocket);
/*  99 */     IdcAdminSystemLoader.loadLogInfo();
/* 100 */     IdcAdminSystemLoader.validateStandardDirectories();
/*     */ 
/* 103 */     SharedPageMergerData.init();
/*     */ 
/* 105 */     AdminServerUtils.loadAllServerData();
/*     */ 
/* 107 */     IdcAdminSystemLoader.startDefaultListener();
/*     */ 
/* 110 */     IdcAdminSystemLoader.loadServiceData();
/*     */ 
/* 113 */     IdcAdminSystemLoader.registerProviders();
/*     */   }
/*     */ 
/*     */   protected void setMessagePrefix()
/*     */   {
/* 121 */     String prefix = SharedObjects.getEnvironmentValue("LogMessagePrefix");
/* 122 */     if (prefix == null) {
/* 123 */       prefix = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*     */     }
/* 125 */     if ((prefix != null) && (prefix.length() > 0))
/* 126 */       prefix = prefix + " IdcAdmin";
/*     */     else {
/* 128 */       prefix = "IdcAdmin";
/*     */     }
/* 130 */     LoggingUtils.setLogFileMsgPrefix(prefix);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 135 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82028 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.IdcAdminManager
 * JD-Core Version:    0.5.4
 */