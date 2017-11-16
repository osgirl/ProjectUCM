/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderConfig;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class FileStoreProviderConfig
/*     */   implements ProviderConfig
/*     */ {
/*     */   public Provider m_provider;
/*     */   public String[] m_vaultMetaData;
/*     */   public String[] m_vaultExtension;
/*     */   public String[] m_webMetaData;
/*     */   public String[] m_webExtension;
/*     */   public String[] m_webIndexMetaData;
/*     */   public String[] m_optionalFields;
/*     */ 
/*     */   public FileStoreProviderConfig()
/*     */   {
/*  30 */     this.m_provider = null;
/*     */ 
/*  35 */     this.m_vaultMetaData = new String[] { "RenditionId", "dDocName", "dDocType", "dRevLabel", "dSecurityGroup", "dDocAccount" };
/*     */ 
/*  37 */     this.m_vaultExtension = new String[] { "dID", "dExtension" };
/*     */ 
/*  42 */     this.m_webMetaData = new String[] { "RenditionId", "dDocName", "dDocType", "dRevLabel", "dSecurityGroup", "dDocAccount" };
/*     */ 
/*  44 */     this.m_webExtension = new String[] { "dWebExtension" };
/*     */ 
/*  46 */     this.m_webIndexMetaData = new String[] { "FileName", "dSecurityGroup", "dDocAccount" };
/*     */ 
/*  50 */     this.m_optionalFields = new String[] { "dReleaseState", "dStatus", "dOriginalName", "fileNamePrefix", "dWebExtension", "dExtension" };
/*     */   }
/*     */ 
/*     */   public void init(Provider pr)
/*     */     throws DataException, ServiceException
/*     */   {
/*  62 */     if (this.m_provider == pr)
/*     */     {
/*  65 */       return;
/*     */     }
/*  67 */     this.m_provider = pr;
/*     */ 
/*  69 */     Map metaData = new Hashtable();
/*  70 */     metaData.put("vault", this.m_vaultMetaData);
/*  71 */     metaData.put("vault_ext", this.m_vaultExtension);
/*  72 */     metaData.put("web", this.m_webMetaData);
/*  73 */     metaData.put("web_ext", this.m_webExtension);
/*  74 */     metaData.put("webIndex", this.m_webIndexMetaData);
/*  75 */     metaData.put("optionalFields", this.m_optionalFields);
/*     */ 
/*  78 */     initCapabilities(metaData);
/*     */ 
/*  80 */     this.m_provider.addProviderObject("MetaData", metaData);
/*  81 */     Object providerInterface = this.m_provider.getProvider();
/*  82 */     initProviderImplementors(providerInterface);
/*     */ 
/*  84 */     initStorageMap();
/*  85 */     loadResources();
/*     */   }
/*     */ 
/*     */   public void initCapabilities(Map metaData)
/*     */   {
/*  90 */     Map capabilities = new Hashtable();
/*  91 */     this.m_provider.addProviderObject("Capabilities", capabilities);
/*  92 */     addStockCapabilities(capabilities, metaData);
/*  93 */     addExtendedCapabilities(capabilities);
/*     */   }
/*     */ 
/*     */   public void addStockCapabilities(Map capabilities, Map metaData)
/*     */   {
/*  98 */     Object[][] stockCapabilities = { { "version_info", "StandardFileStore:1.0.0.0" }, { "legacy_compatible", "1" }, { "constructs_urls", "1" }, { "can_purge", "1" }, { "is_enumerable", "1" }, { "storage_classes", metaData } };
/*     */ 
/* 106 */     addCapabilities(stockCapabilities, capabilities);
/*     */   }
/*     */ 
/*     */   public void addCapabilities(Object[][] stockCapabilities, Map capabilities)
/*     */   {
/* 112 */     for (int i = 0; i < stockCapabilities.length; ++i)
/*     */     {
/* 114 */       Object[] cap = stockCapabilities[i];
/* 115 */       capabilities.put(cap[0], cap[1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addExtendedCapabilities(Map capabilities)
/*     */   {
/* 121 */     DataBinder providerData = this.m_provider.getProviderData();
/* 122 */     DataResultSet drset = (DataResultSet)providerData.getResultSet("Capabilities");
/*     */ 
/* 124 */     if (drset == null)
/*     */       return;
/* 126 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 128 */       Map props = drset.getCurrentRowMap();
/* 129 */       String key = (String)props.get("name");
/* 130 */       String value = (String)props.get("value");
/* 131 */       capabilities.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initProviderImplementors(Object providerInterface)
/*     */   {
/* 143 */     BaseFileStore fileStoreProvider = (BaseFileStore)providerInterface;
/* 144 */     fileStoreProvider.initImplementors();
/*     */   }
/*     */ 
/*     */   public void initStorageMap()
/*     */   {
/* 153 */     String[][] info = { { "primaryFile", "vault" }, { "webViewableFile", "web" }, { "alternateFile", "vault" }, { "rendition", "web" }, { "webIndex", "webIndex" } };
/*     */ 
/* 162 */     Map storageMap = new Hashtable();
/* 163 */     for (int i = 0; i < info.length; ++i)
/*     */     {
/* 165 */       storageMap.put(info[i][0], info[i][1]);
/*     */     }
/*     */ 
/* 168 */     this.m_provider.addProviderObject("RenditionToStorage", storageMap);
/*     */   }
/*     */ 
/*     */   public void loadResources()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 178 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69776 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.FileStoreProviderConfig
 * JD-Core Version:    0.5.4
 */