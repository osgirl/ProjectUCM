/*     */ package intradoc.admin;
/*     */ 
/*     */ import intradoc.common.Browser;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.GuiUtils;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.config.ConfigFileLoader;
/*     */ import intradoc.filestore.config.ConfigFileUtilities;
/*     */ import intradoc.filestore.config.ConfigFileUtils;
/*     */ import intradoc.filestore.config.ServerData;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcExtendedLoader;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.server.SubjectManagerListener;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DocumentPathBuilder;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class IdcAdminSystemLoader
/*     */ {
/*  38 */   protected static IdcExtendedLoader m_extendedLoader = null;
/*     */ 
/*     */   public static void init(boolean hasSocketProvider)
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     SharedObjects.putEnvironmentValue("IdcProductName", "idcadmin");
/*  52 */     SharedObjects.putEnvironmentValue("VersionReportStringKey", "csAdminServerVersion");
/*     */ 
/*  55 */     initLocalization();
/*     */ 
/*  58 */     IdcSystemLoader.loadServerProcessID();
/*     */ 
/*  61 */     SharedObjects.putEnvironmentValue("IdcResourcesDir", DirectoryLocator.getResourcesDirectory());
/*     */ 
/*  63 */     SharedObjects.putEnvironmentValue("PrimaryResourceFile", "$IdcResourcesDir/admin/tables/resource_files.htm");
/*     */ 
/*  65 */     ComponentLoader.initDefaults();
/*  66 */     initProviders(hasSocketProvider);
/*  67 */     AdminServerUtils.loadComponentData();
/*     */ 
/*  70 */     IdcSystemConfig.configLocalization();
/*     */ 
/*  73 */     loadIdocScriptExtensions();
/*     */ 
/*  76 */     IdcSystemLoader.loadCustomEnvironmentValues();
/*     */ 
/*  79 */     computeWebPathEnvironment();
/*     */ 
/*  82 */     IdcSystemLoader.initLogInfo();
/*     */ 
/*  85 */     IdcSystemLoader.configureAppObjects();
/*     */ 
/*  88 */     SystemUtils.addAsActiveTrace("idcadmin");
/*     */     try
/*     */     {
/*  92 */       IdcSystemLoader.managePasswords();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  97 */       Report.trace("system", "Failed to manage passwords for admin server", e);
/*     */     }
/*     */ 
/* 101 */     m_extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csCustomInitializerConstructionError");
/*     */ 
/* 105 */     m_extendedLoader.extraAfterConfigInit();
/*     */ 
/* 107 */     IdcSystemLoader.setExtendedLoader(m_extendedLoader);
/*     */   }
/*     */ 
/*     */   public static void initLocalization()
/*     */     throws DataException, ServiceException
/*     */   {
/* 113 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_ADMIN_SERVER);
/*     */   }
/*     */ 
/*     */   public static void loadConfigInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/* 125 */     String clusterIdcDir = null;
/* 126 */     String filePath = null;
/* 127 */     boolean isSimplifiedServer = AdminServerUtils.m_isSimplifiedServer;
/* 128 */     if (!isSimplifiedServer)
/*     */     {
/* 130 */       filePath = "$IntradocDir/admin/config/config.cfg";
/* 131 */       ConfigFileLoader.cachePropertiesFromFileByName(filePath, false);
/*     */ 
/* 133 */       clusterIdcDir = SharedObjects.getEnvironmentValue("ClusterNodeIntradocDir");
/* 134 */       if (null != clusterIdcDir)
/*     */       {
/* 136 */         Map pathPairs = ConfigFileLoader.m_defaultCFU.getServerData().getKeyPathMap();
/* 137 */         pathPairs.put("ClusterNodeIntradocDir", clusterIdcDir);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 160 */     Throwable masterLoadErr = null;
/*     */     try
/*     */     {
/* 163 */       if ((!isSimplifiedServer) && (SharedObjects.getEnvironmentValue("ConfigFileList") == null))
/*     */       {
/* 165 */         ArrayList files = new ArrayList();
/* 166 */         filePath = "$IntradocDir/bin/intradoc.cfg";
/* 167 */         if (null != clusterIdcDir)
/*     */         {
/* 170 */           filePath = "$ClusterNodeIntradocDir/bin/intradoc.cfg";
/*     */         }
/* 172 */         files.add(filePath);
/*     */ 
/* 174 */         filePath = "$IntradocDir/config/config.cfg";
/* 175 */         files.add(filePath);
/*     */ 
/* 177 */         filePath = "$IntradocDir/config/state.cfg";
/* 178 */         files.add(filePath);
/*     */ 
/* 180 */         String list = StringUtils.createString(files, ',', '^');
/* 181 */         SharedObjects.putEnvironmentValue("ConfigFileList", list);
/*     */       }
/*     */ 
/* 186 */       intradoc.common.TracerReportUtils.m_tracingHasRuntimeEditingUserInterface = false;
/*     */ 
/* 190 */       IdcSystemConfig.loadAppConfigInfo();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 194 */       masterLoadErr = t;
/*     */     }
/*     */ 
/* 198 */     LegacyDirectoryLocator.buildRootDirectories();
/*     */ 
/* 201 */     GuiUtils.setGifDirectoryRoot(LegacyDirectoryLocator.getImagesDirectory());
/*     */ 
/* 204 */     String logDir = LegacyDirectoryLocator.getLogDirectory();
/*     */     try
/*     */     {
/* 208 */       Log.setLogDirectory(logDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 212 */       Report.trace("idctrace", null, e);
/*     */     }
/*     */ 
/* 216 */     if (masterLoadErr != null)
/*     */     {
/* 218 */       Report.error(null, new ServiceException(masterLoadErr), "csAdminUnableToLoadMasterConfig", new Object[0]);
/*     */     }
/*     */ 
/* 222 */     IdcSystemConfig.applyClusterNodeAddressOverrides();
/*     */ 
/* 225 */     LegacyDirectoryLocator.buildWebRoots();
/*     */ 
/* 228 */     String wPath = SharedObjects.getEnvironmentValue("WebBrowserPath");
/* 229 */     if (wPath == null)
/*     */     {
/* 231 */       wPath = SharedObjects.getEnvironmentValue("webBrowserPath");
/*     */     }
/* 233 */     String baseUrlPath = DocumentPathBuilder.getBaseAbsoluteRoot();
/* 234 */     Browser.setPaths(wPath, baseUrlPath);
/* 235 */     if (Help.getUseBuiltInHelpSystem())
/*     */     {
/* 237 */       Help.setPaths(LegacyDirectoryLocator.getHelpDirectory());
/*     */     }
/*     */ 
/* 240 */     AdminServerUtils.setupFileStoreForAdminServer();
/*     */   }
/*     */ 
/*     */   public static void validateStandardDirectories()
/*     */     throws DataException, ServiceException
/*     */   {
/* 251 */     IdcFileDescriptor desc = ConfigFileLoader.m_defaultCFU.createDescriptorByName("$SystemTemplatesDir", null);
/* 252 */     ConfigFileUtils.validateDescriptor(ConfigFileLoader.m_defaultCFU.m_CFS, desc, "rd", "!csUnableToAccessTemplatePages");
/*     */ 
/* 255 */     if (AdminServerUtils.m_isSimplifiedServer)
/*     */       return;
/* 257 */     desc = ConfigFileLoader.m_defaultCFU.createDescriptorByName("$AdminDataDir", null);
/* 258 */     FileUtils.checkOrCreateDirectory(desc.getProperty("pathname"), 4);
/* 259 */     ConfigFileUtils.validateDescriptor(ConfigFileLoader.m_defaultCFU.m_CFS, desc, "rwd", "!csUnableToAccessDataDir");
/*     */   }
/*     */ 
/*     */   public static void initProviders(boolean hasSocketProvider)
/*     */     throws DataException, ServiceException
/*     */   {
/* 271 */     createSystemProviders(hasSocketProvider);
/* 272 */     IdcSystemLoader.loadProviders();
/*     */   }
/*     */ 
/*     */   public static void createSystemProviders(boolean hasSocketProvider)
/*     */   {
/* 283 */     if (!hasSocketProvider) {
/*     */       return;
/*     */     }
/* 286 */     DataBinder cd = new DataBinder();
/* 287 */     cd.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/*     */ 
/* 290 */     int port = getIntradocAdminServerPort();
/* 291 */     cd.putLocal("ServerPort", String.valueOf(port));
/*     */ 
/* 293 */     String bindAddress = SharedObjects.getEnvironmentValue("IdcAdminServerBindAddress");
/*     */ 
/* 295 */     if (bindAddress != null)
/*     */     {
/* 297 */       cd.putLocal("ServerBindAddress", bindAddress);
/*     */     }
/* 299 */     String socketQueue = SharedObjects.getEnvironmentValue("IdcAdminServerSocketQueueDepth");
/*     */ 
/* 301 */     if (socketQueue != null)
/*     */     {
/* 303 */       cd.putLocal("ServerQueueDepth", socketQueue);
/*     */     }
/*     */ 
/* 306 */     cd.putLocal("ProviderName", "SystemServerSocket");
/* 307 */     cd.putLocal("ProviderClass", "intradoc.provider.SocketIncomingProvider");
/* 308 */     cd.putLocal("ProviderConnection", "intradoc.provider.SocketIncomingConnection");
/* 309 */     cd.putLocal("ProviderType", "incoming");
/* 310 */     cd.putLocal("IsSystemProvider", "1");
/* 311 */     cd.putLocal("ProviderReportStringKey", "csAdminServerWaitingForConnectionLogMessage");
/*     */ 
/* 313 */     Providers.addProviderData("SystemServerSocket", cd);
/*     */   }
/*     */ 
/*     */   public static void startDefaultListener()
/*     */     throws ServiceException
/*     */   {
/* 324 */     SubjectManager.init("file://" + LegacyDirectoryLocator.getSubjectsDirectory());
/* 325 */     SubjectManager.monitor();
/* 326 */     SubjectManagerListener listener = new SubjectManagerListener()
/*     */     {
/*     */       public void handleManagerEvent(int eventType)
/*     */       {
/*     */       }
/*     */     };
/* 333 */     SubjectManager.setListener(listener);
/* 334 */     SubjectManager.startMonitoringThread();
/*     */   }
/*     */ 
/*     */   public static int getIntradocAdminServerPort()
/*     */   {
/* 343 */     String str = SharedObjects.getEnvironmentValue("IdcAdminServerPort");
/* 344 */     if ((str == null) || (str.length() == 0))
/*     */     {
/* 346 */       return 4440;
/*     */     }
/* 348 */     return Integer.parseInt(str.trim());
/*     */   }
/*     */ 
/*     */   public static void computeWebPathEnvironment()
/*     */   {
/* 356 */     boolean doProxyCgi = SharedObjects.getEnvValueAsBoolean("IsProxiedServer", true);
/* 357 */     String originalCgiName = LegacyDirectoryLocator.getCgiFileName();
/* 358 */     if (doProxyCgi)
/*     */     {
/* 360 */       String relativeWebRoot = LegacyDirectoryLocator.getRelativeAdminRoot();
/* 361 */       String proxyCgiName = LegacyDirectoryLocator.createProxiedCgiFileName(originalCgiName, relativeWebRoot);
/*     */ 
/* 363 */       SharedObjects.putEnvironmentValue("CgiFileName", proxyCgiName);
/*     */     }
/* 365 */     SharedObjects.putEnvironmentValue("OriginalCgiFileName", originalCgiName);
/*     */   }
/*     */ 
/*     */   public static void loadIdocScriptExtensions()
/*     */     throws ServiceException, DataException
/*     */   {
/* 374 */     IdcSystemLoader.loadIdocScriptExtensions();
/*     */   }
/*     */ 
/*     */   public static void loadLogInfo() throws ServiceException
/*     */   {
/* 379 */     IdcSystemLoader.loadLogInfo();
/*     */   }
/*     */ 
/*     */   public static void loadServiceData() throws ServiceException, DataException
/*     */   {
/* 384 */     IdcSystemLoader.loadServiceData();
/*     */   }
/*     */ 
/*     */   public static void registerProviders() throws ServiceException, DataException
/*     */   {
/* 389 */     IdcSystemLoader.registerProviders();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 395 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82634 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.IdcAdminSystemLoader
 * JD-Core Version:    0.5.4
 */