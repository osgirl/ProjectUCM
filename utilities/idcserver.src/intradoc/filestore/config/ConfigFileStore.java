/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ConfigFileStore extends BaseFileStore
/*     */ {
/*     */   public static final String CFM_FILENAME = "filename";
/*     */   public static final String CFM_THIS_SERVER_STORE = "thisServerStore";
/*     */   public static final String CFM_USE_WRAPPER = "useWrapper";
/*     */   public static final String CFM_SERVER_PATH = "IntradocDir";
/*     */   public static final String CFM_FILE_PATH = "pathname";
/*     */   public static final String CFM_PARENT_PATH = "containerPath";
/*     */   public static final String CFM_EXEC_CONTEXT = "ExecutionContext";
/*  63 */   public static String O_REQUIRE_EXISTENCE = "mustExist";
/*     */ 
/*  68 */   public static String O_LOCK_CONTAINER = "lockContainer";
/*  69 */   public static String O_UNLOCK_CONTAINER = "unlockContainer";
/*     */ 
/*  73 */   public static String F_CREATE_LOCKFILE = "noCreateLockfile";
/*     */   String[][] DEFAULT_CAPABILITIES;
/*     */   String[][] DEFAULT_CONFIG_VALUES;
/*     */ 
/*     */   public ConfigFileStore()
/*     */   {
/*  78 */     this.DEFAULT_CAPABILITIES = new String[][] { { "version_info", "" } };
/*     */ 
/*  84 */     this.DEFAULT_CONFIG_VALUES = new String[][] { { "isForce", "1" }, { "isDoBackup", "1" }, { "isDoTemp", "1" }, { "doCreateContainers", "1" }, { F_CREATE_LOCKFILE, "1" }, { "isUpdateCache", "1" }, { "useWrapper", "0" }, { "useLegacyStat", "1" }, { O_LOCK_CONTAINER, "1" }, { O_UNLOCK_CONTAINER, "1" } };
/*     */   }
/*     */ 
/*     */   Map createCapabilitiesMap()
/*     */   {
/* 107 */     HashMap capabilities = new HashMap();
/* 108 */     HashMap storageClasses = new HashMap();
/* 109 */     storageClasses.put("config", "1");
/* 110 */     capabilities.put("storage_classes", storageClasses);
/* 111 */     for (int i = 0; i < this.DEFAULT_CAPABILITIES.length; ++i)
/*     */     {
/* 113 */       String key = this.DEFAULT_CAPABILITIES[i][0];
/* 114 */       String value = this.DEFAULT_CAPABILITIES[i][1];
/* 115 */       capabilities.put(key, value);
/*     */     }
/* 117 */     return capabilities;
/*     */   }
/*     */ 
/*     */   void initializeDefaults(Map defaults)
/*     */   {
/* 129 */     this.m_defaultArgs = new HashMap();
/* 130 */     for (int i = 0; i < this.DEFAULT_CONFIG_VALUES.length; ++i)
/*     */     {
/* 132 */       String key = this.DEFAULT_CONFIG_VALUES[i][0];
/* 133 */       String value = this.DEFAULT_CONFIG_VALUES[i][1];
/* 134 */       if (null != defaults)
/*     */       {
/* 136 */         String defValue = (String)defaults.get(key);
/* 137 */         if ((null != defValue) && (defValue.length() > 0))
/*     */         {
/* 139 */           value = defValue;
/*     */         }
/*     */       }
/* 142 */       this.m_defaultArgs.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(Provider provider)
/*     */     throws DataException
/*     */   {
/* 149 */     Map capabilities = createCapabilitiesMap();
/* 150 */     provider.addProviderObject("Capabilities", capabilities);
/* 151 */     Map defaults = (Map)provider.getProviderObject("DefaultArgs");
/* 152 */     initializeDefaults(defaults);
/* 153 */     super.init(provider);
/*     */   }
/*     */ 
/*     */   public static ConfigFileStore lookupConfigFileStore(String id)
/*     */   {
/* 166 */     if (null == id)
/*     */     {
/* 168 */       id = "";
/*     */     }
/* 170 */     Object cfs = SharedObjects.getObject("ConfigFileStore", id);
/* 171 */     return (ConfigFileStore)cfs;
/*     */   }
/*     */ 
/*     */   public static ConfigFileStore lookupConfigFileStoreMustExist(String id)
/*     */     throws ServiceException
/*     */   {
/* 185 */     ConfigFileStore cfs = lookupConfigFileStore(id);
/* 186 */     if (null == cfs)
/*     */     {
/*     */       String msg;
/*     */       String msg;
/* 189 */       if ((id != null) && (id.length() > 0))
/*     */       {
/* 191 */         msg = LocaleUtils.encodeMessage("csCFSNameNotFound", null, id);
/*     */       }
/*     */       else
/*     */       {
/* 195 */         msg = LocaleUtils.encodeMessage("csCFSDefaultNotFound", null);
/*     */       }
/* 197 */       throw new ServiceException(msg);
/*     */     }
/* 199 */     return cfs;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 207 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ConfigFileStore
 * JD-Core Version:    0.5.4
 */