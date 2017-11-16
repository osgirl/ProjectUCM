/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FileStoreProviderLoader
/*     */ {
/*     */   public static void initDefaultFileStore(ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  38 */     if (!Features.checkLevel("ContentManagement", null))
/*     */     {
/*  41 */       return;
/*     */     }
/*     */ 
/*  44 */     int rc = PluginFilters.filter("initDefaultFileStore", null, null, cxt);
/*  45 */     if (rc == 1)
/*     */     {
/*  47 */       return;
/*     */     }
/*     */ 
/*  51 */     DataBinder defData = Providers.getProviderData("DefaultFileStore");
/*  52 */     boolean isWebAssetDefined = false;
/*  53 */     boolean isDefaultFileStoreDefined = false;
/*  54 */     if (defData != null)
/*     */     {
/*  56 */       isDefaultFileStoreDefined = true;
/*  57 */       ResultSet defStorageRules = defData.getResultSet("StorageRules");
/*  58 */       if ((defStorageRules != null) && 
/*  60 */         (ResultSetUtils.findValue(defStorageRules, "StorageRule", "webasset", "StorageRule") != null))
/*     */       {
/*  62 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  67 */     if (!isDefaultFileStoreDefined)
/*     */     {
/*  69 */       DataBinder binder = createFileStoreDefaults(cxt);
/*     */ 
/*  72 */       DataResultSet drset = new DataResultSet(new String[] { "name", "location" });
/*  73 */       binder.addResultSet("FSImplementors", drset);
/*     */ 
/*  75 */       Vector row = new IdcVector();
/*  76 */       row.addElement("WebLocationParser");
/*  77 */       row.addElement("intradoc.filestore.filesystem.WebLocationParser");
/*  78 */       drset.addRow(row);
/*     */ 
/*  80 */       String[] tables = { "FSImplementors", "StorageRules", "PathConstruction", "PathMetaData" };
/*  81 */       for (int i = 0; i < tables.length; ++i)
/*     */       {
/*  83 */         String table = tables[i];
/*  84 */         DataResultSet rset = createSetFromDefault(table, true, null, null);
/*     */ 
/*  88 */         if (rset == null)
/*     */           continue;
/*  90 */         binder.addResultSet(table, rset);
/*     */       }
/*     */ 
/*  93 */       Providers.addProviderData("DefaultFileStore", binder);
/*     */     } else {
/*  95 */       if (isWebAssetDefined)
/*     */         return;
/*  97 */       String[] tables = { "WebAssetStorageRules", "WebAssetPathConstruction", "PathMetaData" };
/*  98 */       String[] mergeTables = { "StorageRules", "PathConstruction", "PathMetaData" };
/*  99 */       String[] mergeKeys = { "StorageRule", "FileStore", "FieldName" };
/* 100 */       for (int i = 0; i < tables.length; ++i)
/*     */       {
/* 102 */         String table = tables[i];
/* 103 */         String mergeTable = mergeTables[i];
/* 104 */         String mergeKey = mergeKeys[i];
/* 105 */         DataResultSet rset = createSetFromDefault(table, true, null, null);
/*     */ 
/* 109 */         if (rset == null)
/*     */           continue;
/* 111 */         DataResultSet origSet = (DataResultSet)defData.getResultSet(mergeTable);
/* 112 */         if (origSet == null)
/*     */           continue;
/* 114 */         origSet.merge(mergeKey, rset, false);
/*     */       }
/*     */ 
/* 118 */       Report.trace("filestore", "WebAsset storage rule added to FileStoreProvider", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static DataResultSet createSetFromDefault(String name, boolean isCopy, ResultSetFilter filter, String key)
/*     */     throws DataException
/*     */   {
/* 130 */     DataResultSet rset = SharedObjects.getTable(name + "Defaults");
/*     */ 
/* 134 */     if (rset == null)
/*     */     {
/* 136 */       return null;
/*     */     }
/*     */ 
/* 139 */     List clmns = new ArrayList();
/*     */ 
/* 141 */     if ((isCopy) && (filter != null))
/*     */     {
/* 143 */       DataResultSet filteredSet = new DataResultSet();
/* 144 */       filteredSet.copyFiltered(rset, key, filter);
/* 145 */       rset = filteredSet;
/* 146 */       rset.first();
/*     */     }
/*     */ 
/* 149 */     int numFields = rset.getNumFields();
/* 150 */     for (int j = 0; j < numFields; ++j)
/*     */     {
/* 152 */       String clmn = rset.getFieldName(j);
/* 153 */       if ((clmn.equals(key)) || (clmn.equals("idcComponentName")))
/*     */         continue;
/* 155 */       clmns.add(clmn);
/*     */     }
/*     */ 
/* 158 */     String[] fields = new String[clmns.size()];
/* 159 */     clmns.toArray(fields);
/*     */ 
/* 161 */     DataResultSet drset = new DataResultSet(fields);
/* 162 */     if (isCopy)
/*     */     {
/* 164 */       drset.merge(null, rset, false);
/*     */     }
/*     */ 
/* 167 */     return drset;
/*     */   }
/*     */ 
/*     */   public static DataBinder createFileStoreDefaults(ExecutionContext cxt)
/*     */   {
/* 172 */     DataBinder binder = new DataBinder();
/* 173 */     binder.putLocal("ProviderName", "DefaultFileStore");
/* 174 */     binder.putLocal("pName", "DefaultFileStore");
/* 175 */     String description = "csFsDefaultFileStore";
/* 176 */     binder.putLocal("ProviderDescription", description);
/* 177 */     binder.putLocal("pDescription", description);
/* 178 */     binder.putLocal("ProviderType", "FileStore");
/* 179 */     binder.putLocal("ProviderClass", "intradoc.filestore.BaseFileStore");
/* 180 */     binder.putLocal("IsPrimaryFileStore", "1");
/* 181 */     binder.putLocal("IsEnabled", "true");
/* 182 */     binder.putLocal("IsSystemProvider", "1");
/*     */ 
/* 184 */     boolean readyForUpgrade = checkFileStoreUpgradeRequirements();
/*     */ 
/* 187 */     boolean isUpgrade = SharedObjects.getEnvValueAsBoolean("FsAutoConfigure", true);
/* 188 */     if ((isUpgrade) && (readyForUpgrade))
/*     */     {
/* 190 */       binder.putLocal("isEdit", "1");
/* 191 */       binder.putLocal("pType", "FileStore");
/* 192 */       binder.putLocal("ProviderConfig", "intradoc.filestore.filesystem.FileSystemProviderConfig");
/* 193 */       binder.putLocal("AccessImplementor", "intradoc.filestore.filesystem.FileSystemAccessImplementor");
/* 194 */       binder.putLocal("DescriptorImplementor", "intradoc.filestore.filesystem.FileSystemDescriptorImplementor");
/* 195 */       binder.putLocal("EventImplementor", "intradoc.filestore.filesystem.FileSystemEventImplementor");
/* 196 */       binder.putLocal("MetadataImplementor", "intradoc.filestore.filesystem.FileSystemMetadataImplementor");
/*     */     }
/*     */ 
/* 200 */     binder.putLocal("IsAllowConfigSystemProvider", "1");
/* 201 */     return binder;
/*     */   }
/*     */ 
/*     */   public static boolean checkFileStoreUpgradeRequirements()
/*     */   {
/* 212 */     boolean readyForUpgrade = true;
/*     */ 
/* 214 */     DataBinder componentBinder = ComponentLoader.getComponentBinder("FileStoreProvider");
/*     */ 
/* 216 */     if (componentBinder == null)
/*     */     {
/* 218 */       String msg = LocaleUtils.encodeMessage("csFsUpgradeSkipped", null);
/* 219 */       Report.info("filestore", msg, null);
/* 220 */       readyForUpgrade = false;
/*     */     }
/*     */ 
/* 223 */     return readyForUpgrade;
/*     */   }
/*     */ 
/*     */   public static void cacheFileStoreProvider(Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 232 */     Vector list = Providers.getProvidersOfType("FileStore");
/*     */ 
/* 234 */     Provider primaryProvider = null;
/* 235 */     String primaryProviderName = SharedObjects.getEnvironmentValue("PrimaryFileStoreProvider");
/* 236 */     if (primaryProviderName != null)
/*     */     {
/* 238 */       primaryProvider = Providers.getProvider(primaryProviderName);
/*     */     }
/* 240 */     if (primaryProvider == null)
/*     */     {
/* 242 */       boolean isFound = false;
/* 243 */       for (int i = 0; i < list.size(); ++i)
/*     */       {
/* 245 */         Provider p = (Provider)list.elementAt(i);
/* 246 */         DataBinder binder = p.getProviderData();
/* 247 */         boolean isSystem = DataBinderUtils.getBoolean(binder, "IsPrimaryFileStore", false);
/* 248 */         if (!isSystem)
/*     */           continue;
/* 250 */         if (isFound)
/*     */         {
/* 252 */           throw new ServiceException("!csFsMultiplePrimaryFileStoreProviders");
/*     */         }
/* 254 */         isFound = true;
/* 255 */         primaryProvider = p;
/*     */       }
/*     */     }
/*     */ 
/* 259 */     if (primaryProvider == null)
/*     */     {
/* 261 */       ServiceException e = new ServiceException("!csFsNoPrimaryFileStoreProvider");
/* 262 */       Report.trace("filestore", null, e);
/* 263 */       throw e;
/*     */     }
/*     */ 
/* 267 */     primaryProvider.configureProvider();
/*     */ 
/* 269 */     FileStoreProvider fileStoreProvider = (FileStoreProvider)primaryProvider.getProvider();
/* 270 */     if (fileStoreProvider != null)
/*     */     {
/* 272 */       AppObjectRepository.putObject("FileStoreProvider", fileStoreProvider);
/*     */ 
/* 274 */       String enableDirectoryLocator = SharedObjects.getEnvironmentValue("EnableDirectoryLocator");
/*     */ 
/* 276 */       if (enableDirectoryLocator == null)
/*     */       {
/* 278 */         Map capabilities = fileStoreProvider.getCapabilities(null);
/* 279 */         enableDirectoryLocator = (String)capabilities.get("legacy_compatible");
/*     */       }
/*     */ 
/* 282 */       intradoc.server.DirectoryLocator.m_disableServices = !StringUtils.convertToBool(enableDirectoryLocator, false);
/*     */ 
/* 284 */       intradoc.server.DirectoryLocator.m_fileStore = fileStoreProvider;
/* 285 */       if (ws != null)
/*     */       {
/* 287 */         intradoc.server.DirectoryLocator.m_workspace = ws;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 292 */       throw new ServiceException("!csFsNoPrimaryFileStoreProvider");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static FileStoreProvider initFileStore(ExecutionContext context)
/*     */     throws ServiceException, DataException
/*     */   {
/* 308 */     FileStoreProvider fileStore = null;
/* 309 */     if (context != null)
/*     */     {
/* 311 */       fileStore = (FileStoreProvider)context.getCachedObject("FileStoreProvider");
/*     */     }
/* 313 */     if (fileStore == null)
/*     */     {
/* 315 */       fileStore = (FileStoreProvider)AppObjectRepository.getObject("FileStoreProvider");
/*     */     }
/* 317 */     if ((fileStore != null) && (context != null))
/*     */     {
/* 319 */       IdcDescriptorState state = (IdcDescriptorState)context.getCachedObject("DescriptorStates");
/*     */ 
/* 321 */       if (state == null)
/*     */       {
/* 323 */         state = new IdcDescriptorStateImplementor();
/* 324 */         context.setCachedObject("DescriptorStates", state);
/*     */       }
/*     */ 
/* 327 */       if (fileStore instanceof BaseFileStore)
/*     */       {
/* 329 */         BaseFileStore bfs = (BaseFileStore)fileStore;
/* 330 */         String providerName = bfs.m_providerName;
/* 331 */         String rollbackLog = "FileStore:" + providerName + ":rollbackLog";
/* 332 */         Object obj = context.getCachedObject(rollbackLog);
/* 333 */         if (obj == null)
/*     */         {
/* 335 */           context.setCachedObject(rollbackLog, new ArrayList());
/* 336 */           context.setCachedObject("FileStore:" + providerName + ":commitLog", new ArrayList());
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 342 */     PluginFilters.filter("initFileStore", null, null, context);
/* 343 */     return fileStore;
/*     */   }
/*     */ 
/*     */   public static void prepareAndClearContext(FileStoreProvider fileStore, ExecutionContext context)
/*     */   {
/* 348 */     if ((fileStore == null) || (context == null))
/*     */       return;
/* 350 */     IdcDescriptorState state = new IdcDescriptorStateImplementor();
/* 351 */     context.setCachedObject("DescriptorStates", state);
/*     */ 
/* 353 */     if (!fileStore instanceof BaseFileStore)
/*     */       return;
/* 355 */     BaseFileStore bfs = (BaseFileStore)fileStore;
/* 356 */     String providerName = bfs.m_providerName;
/* 357 */     String rollbackLog = "FileStore:" + providerName + ":rollbackLog";
/* 358 */     context.setCachedObject(rollbackLog, new ArrayList());
/* 359 */     context.setCachedObject("FileStore:" + providerName + ":commitLog", new ArrayList());
/*     */ 
/* 363 */     context.setCachedObject("JdbcCacheOptimizer", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 370 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 106683 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreProviderLoader
 * JD-Core Version:    0.5.4
 */