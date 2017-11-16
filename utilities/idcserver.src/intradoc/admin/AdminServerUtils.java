/*     */ package intradoc.admin;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.filestore.config.ConfigFileLoader;
/*     */ import intradoc.filestore.config.ConfigFileStore;
/*     */ import intradoc.filestore.config.ConfigFileUtilities;
/*     */ import intradoc.filestore.config.ConfigFileUtils;
/*     */ import intradoc.filestore.config.ServerData;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedUtils;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class AdminServerUtils
/*     */ {
/*  47 */   public static boolean m_isSimplifiedServer = false;
/*     */   public static AdminDirectServerActions m_directActionsInterface;
/*     */   public static Map<String, String> m_administratedServerProperties;
/*     */ 
/*     */   public static void setupFileStoreForAdminServer()
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     ServerData data = ConfigFileLoader.m_defaultCFU.getServerData();
/*  66 */     String[][] pathPairs = (m_isSimplifiedServer) ? AdminDirectoryLocator.ADMIN_SIMPLE_KEYPATH_PAIRS : AdminDirectoryLocator.ADMIN_KEYPATH_PAIRS;
/*     */ 
/*  68 */     data.processKeyPathStrings(pathPairs);
/*     */ 
/*  71 */     Properties env = SharedObjects.getSecureEnvironment();
/*  72 */     ConfigFileLoader.m_defaultCFU.reprocessKeyPathPairsFromEnv(env);
/*     */   }
/*     */ 
/*     */   public static void loadComponentData()
/*     */     throws DataException, ServiceException
/*     */   {
/*  80 */     ComponentLoader.sortComponents();
/*  81 */     loadCachedHtmlData();
/*  82 */     IdcSystemLoader.loadResources();
/*  83 */     IdcSystemLoader.mergeResourceTables();
/*  84 */     IdcSystemLoader.loadTemplatePages();
/*     */   }
/*     */ 
/*     */   public static void loadCachedHtmlData()
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     DataLoader.cacheTemplateTables();
/*     */   }
/*     */ 
/*     */   public static void loadAllServerData()
/*     */     throws ServiceException, DataException
/*     */   {
/* 102 */     AdminServerData servers = new AdminServerData();
/*     */ 
/* 105 */     if (m_isSimplifiedServer)
/*     */     {
/* 107 */       servers.initAsSimpleServer();
/*     */     }
/*     */     else
/*     */     {
/* 111 */       servers.load(ConfigFileLoader.m_defaultCFU);
/* 112 */       setupConfigFileStoresForAllServers(servers);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initAdminDataBinder(DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 124 */     boolean isSimplifiedServer = m_isSimplifiedServer;
/*     */ 
/* 127 */     binder.putLocal("isSimplifiedServer", (isSimplifiedServer) ? "1" : "");
/* 128 */     binder.putLocal("hasSimplifiedAlterStatus", (m_directActionsInterface != null) ? "1" : "");
/* 129 */     String originalCgiName = SharedObjects.getEnvironmentValue("OriginalCgiFileName");
/* 130 */     if (!m_isSimplifiedServer) {
/*     */       return;
/*     */     }
/* 133 */     Properties secureEnv = SharedObjects.getSecureEnvironment();
/* 134 */     IdcProperties curEnv = (IdcProperties)binder.getEnvironment();
/* 135 */     curEnv.setDefaults(secureEnv);
/*     */ 
/* 138 */     String toContentServerPath = null;
/* 139 */     if (originalCgiName == null)
/*     */     {
/* 141 */       toContentServerPath = DirectoryLocator.getEnterpriseCgiWebUrl(false);
/*     */     }
/*     */     else
/*     */     {
/* 145 */       toContentServerPath = DirectoryLocator.getRelativeCgiRoot() + originalCgiName;
/*     */     }
/*     */ 
/* 149 */     binder.putLocal("RemoteAbsoluteHttpCgiPath", toContentServerPath);
/* 150 */     String relativeWebRoot = DirectoryLocator.getWebRoot(false);
/* 151 */     binder.putLocal("RemoteAbsoluteWebRoot", relativeWebRoot);
/*     */   }
/*     */ 
/*     */   public static void setupConfigFileStoresForAllServers(AdminServerData adminServerData)
/*     */     throws DataException, ServiceException
/*     */   {
/* 165 */     boolean isWin = EnvUtils.isFamily("windows");
/*     */ 
/* 168 */     if (null == adminServerData)
/*     */     {
/* 170 */       adminServerData = (AdminServerData)SharedObjects.getTable("ServerDefinition");
/*     */     }
/* 172 */     if (adminServerData.isEmpty())
/*     */     {
/* 174 */       return;
/*     */     }
/*     */ 
/* 177 */     int idIndex = ResultSetUtils.getIndexMustExist(adminServerData, "IDC_Id");
/* 178 */     for (adminServerData.first(); adminServerData.isRowPresent(); adminServerData.next())
/*     */     {
/* 180 */       String serverName = adminServerData.getStringValue(idIndex);
/* 181 */       DataBinder serverBinder = (DataBinder)adminServerData.m_serverMap.get(serverName);
/*     */ 
/* 184 */       String useWrapper = (isWin) ? "0" : "1";
/* 185 */       Map defaultArgs = new HashMap();
/* 186 */       defaultArgs.put("useWrapper", useWrapper);
/*     */       try
/*     */       {
/* 191 */         setupConfigFileStoreForServer(serverName, serverBinder, defaultArgs);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 195 */         SharedUtils.logCommonException(0, e, "csAdminUnableToLoadData", new Object[0]);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 199 */         SharedUtils.logCommonException(0, e, "csAdminUnableToLoadData", new Object[0]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static ConfigFileUtilities setupConfigFileStoreForServer(String serverName, DataBinder serverBinder, Map defaultArgs)
/*     */     throws DataException, ServiceException
/*     */   {
/* 226 */     Map prObjects = new HashMap();
/* 227 */     prObjects.put("DefaultArgs", defaultArgs);
/*     */ 
/* 230 */     ServerData serverData = new ServerData(serverBinder);
/*     */ 
/* 233 */     prObjects.put("ServerData", serverData);
/*     */ 
/* 236 */     ExecutionContext cxt = ConfigFileLoader.m_defaultCFU.m_context;
/*     */ 
/* 239 */     ConfigFileStore CFS = ConfigFileLoader.createNewConfigFileStore(serverName, prObjects);
/*     */ 
/* 241 */     ConfigFileUtilities CFU = ConfigFileUtilities.createConfigFileUtilities(CFS, cxt);
/*     */ 
/* 247 */     Properties props = new Properties();
/* 248 */     Map args = null;
/* 249 */     String path = null;
/*     */     try
/*     */     {
/* 253 */       args = CFU.createMapFromOptionsString("", ',');
/* 254 */       path = "$IntradocDir/config/config.cfg";
/* 255 */       IdcFileDescriptor desc = CFU.createDescriptorByName(path, args);
/* 256 */       ConfigFileUtils.loadPropertiesFromFile(CFS, desc, props);
/*     */ 
/* 258 */       path = "$BinDir/intradoc.cfg";
/* 259 */       desc = CFU.createDescriptorByName(path, args);
/* 260 */       ConfigFileUtils.loadPropertiesFromFile(CFS, desc, props);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 264 */       throw new DataException(e, "csUnableToLoadProps", new Object[] { path });
/*     */     }
/*     */ 
/* 267 */     CFU.reprocessKeyPathPairsFromEnv(props);
/*     */ 
/* 269 */     return CFU;
/*     */   }
/*     */ 
/*     */   public static String determineAdministratedContentServerType()
/*     */   {
/* 280 */     String contentServerType = null;
/* 281 */     if (m_administratedServerProperties == null)
/*     */     {
/* 283 */       return "partial";
/*     */     }
/* 285 */     contentServerType = (String)m_administratedServerProperties.get("AdministratedContentServerType");
/* 286 */     if (contentServerType == null)
/*     */     {
/* 290 */       String productName = (String)m_administratedServerProperties.get("AdministratedProductName");
/* 291 */       if ((productName != null) && (productName.indexOf("ibr") < 0))
/*     */       {
/* 293 */         contentServerType = "server";
/*     */       }
/*     */     }
/* 296 */     return contentServerType;
/*     */   }
/*     */ 
/*     */   public static String determineDefaultStartupClass(String productName)
/*     */   {
/* 301 */     if ((productName != null) && (productName.indexOf("ibr") >= 0))
/*     */     {
/* 303 */       return "docrefinery.server.RefServerManager";
/*     */     }
/* 305 */     return "intradoc.server.IdcServerManager";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 310 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminServerUtils
 * JD-Core Version:    0.5.4
 */