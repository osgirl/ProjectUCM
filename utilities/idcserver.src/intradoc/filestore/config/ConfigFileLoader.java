/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ConfigFileLoader
/*     */ {
/*     */   public static ConfigFileUtilities m_defaultCFU;
/*  44 */   static String[][] DEFAULT_PROVIDER_DATA = { { "ProviderType", "FileStore" }, { "IsEnabled", "1" }, { "IsPrimaryFileStore", "0" }, { "IsSystemProvider", "0" }, { "ProviderClass", "intradoc.filestore.config.ConfigFileStore" }, { "ProviderConfig", "intradoc.provider.ProviderConfigImpl" }, { "AccessImplementor", "intradoc.filestore.config.ConfigAccessImplementor" }, { "EventImplementor", "intradoc.filestore.config.ConfigEventImplementor" }, { "MetadataImplementor", "intradoc.filestore.config.ConfigMetadataImplementor" }, { "DescriptorImplementor", "intradoc.filestore.config.ConfigDescriptorImplementor" } };
/*     */ 
/*     */   static DataBinder createProviderBinder(String id)
/*     */   {
/*  69 */     String prName = "DefaultConfigFileStore";
/*  70 */     String prDesc = "Default ConfigFileStore Provider";
/*  71 */     if ((null == id) || (id.length() < 1))
/*     */     {
/*  73 */       id = "";
/*     */     }
/*     */     else
/*     */     {
/*  77 */       prName = "ConfigFileStore_" + id;
/*  78 */       prDesc = "ConfigFileStore Provider for " + id;
/*     */     }
/*  80 */     DataBinder prData = new DataBinder();
/*     */ 
/*  82 */     prData.putLocal("ProviderName", prName);
/*     */ 
/*  84 */     prData.putLocal("ProviderDescription", prDesc);
/*     */ 
/*  86 */     for (int i = 0; i < DEFAULT_PROVIDER_DATA.length; ++i)
/*     */     {
/*  88 */       String key = DEFAULT_PROVIDER_DATA[i][0];
/*  89 */       String value = DEFAULT_PROVIDER_DATA[i][1];
/*  90 */       prData.putLocal(key, value);
/*     */     }
/*  92 */     return prData;
/*     */   }
/*     */ 
/*     */   public static ConfigFileStore createNewConfigFileStore(String id, Map prObjects)
/*     */     throws DataException, ServiceException
/*     */   {
/* 107 */     if (null == id)
/*     */     {
/* 109 */       id = "";
/*     */     }
/* 111 */     ServerData serverData = null;
/* 112 */     if (null != prObjects)
/*     */     {
/* 114 */       serverData = (ServerData)prObjects.get("ServerData");
/*     */     }
/* 116 */     if (null == serverData)
/*     */     {
/* 118 */       serverData = new ServerData();
/*     */     }
/*     */ 
/* 127 */     DataBinder prData = createProviderBinder(id);
/* 128 */     Provider provider = new Provider(prData);
/* 129 */     provider.addProviderObject("ServerData", serverData);
/*     */ 
/* 131 */     if (null != prObjects)
/*     */     {
/* 133 */       Iterator prObjIter = prObjects.keySet().iterator();
/* 134 */       while (prObjIter.hasNext())
/*     */       {
/* 136 */         String key = (String)prObjIter.next();
/* 137 */         if (!key.equals("ServerData"))
/*     */         {
/* 139 */           provider.addProviderObject(key, prObjects.get(key));
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 145 */     provider.init();
/*     */ 
/* 147 */     provider.configureProvider();
/* 148 */     ConfigFileStore CFS = (ConfigFileStore)provider.getProvider();
/* 149 */     CFS.initImplementors();
/*     */ 
/* 151 */     SharedObjects.putObject("ConfigFileStore", id, CFS);
/* 152 */     return CFS;
/*     */   }
/*     */ 
/*     */   public static void init(ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 166 */     if (null == cxt)
/*     */     {
/* 168 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 172 */     ConfigFileStore CFS = createNewConfigFileStore("", null);
/*     */ 
/* 174 */     m_defaultCFU = ConfigFileUtilities.createConfigFileUtilities(CFS, cxt);
/*     */   }
/*     */ 
/*     */   public static void cachePropertiesFromFile(IdcFileDescriptor desc, boolean allowOverride)
/*     */     throws DataException
/*     */   {
/* 181 */     Properties props = new Properties();
/* 182 */     String path = null;
/*     */     try
/*     */     {
/* 185 */       path = m_defaultCFU.m_CFS.getFilesystemPath(desc, m_defaultCFU.m_context);
/* 186 */       ConfigFileUtils.loadPropertiesFromFile(m_defaultCFU.m_CFS, desc, props);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 190 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadProps", e.getMessage(), path);
/*     */ 
/* 192 */       throw new DataException(msg);
/*     */     }
/* 194 */     DataLoader.cachePropertiesEx(props, null, allowOverride);
/*     */   }
/*     */ 
/*     */   public static void cachePropertiesFromFileByName(String pathname, boolean allowOverride)
/*     */     throws DataException
/*     */   {
/* 200 */     Map args = null;
/*     */     IdcFileDescriptor desc;
/*     */     try
/*     */     {
/* 204 */       desc = m_defaultCFU.createDescriptorByName(pathname, args);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 208 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadProps", e.getMessage(), pathname);
/*     */ 
/* 210 */       throw new DataException(msg);
/*     */     }
/* 212 */     cachePropertiesFromFile(desc, allowOverride);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 219 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigFileLoader
 * JD-Core Version:    0.5.4
 */