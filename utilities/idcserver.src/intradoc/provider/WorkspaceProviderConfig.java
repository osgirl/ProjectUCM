/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ResourceTrace;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ComponentData;
/*     */ import intradoc.resource.ComponentDataUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkspaceProviderConfig extends ProviderConfigImpl
/*     */ {
/*     */   protected boolean m_inited;
/*     */   protected boolean m_loaded;
/*     */   protected Workspace m_workspace;
/*     */ 
/*     */   public WorkspaceProviderConfig()
/*     */   {
/*  46 */     this.m_inited = false;
/*  47 */     this.m_loaded = false;
/*  48 */     this.m_workspace = null;
/*     */   }
/*     */ 
/*     */   public void init(Provider provider) throws DataException, ServiceException
/*     */   {
/*  53 */     if (this.m_inited)
/*     */     {
/*  55 */       return;
/*     */     }
/*  57 */     super.init(provider);
/*     */ 
/*  59 */     this.m_workspace = ((Workspace)provider.getProvider());
/*  60 */     ProviderPoolManager manager = (ProviderPoolManager)this.m_workspace.getManagerObject();
/*     */ 
/*  62 */     WorkspaceConfigImplementor cfg = manager.createConfig(provider);
/*  63 */     cfg.validateAndUpdateConfiguration(provider, manager);
/*  64 */     provider.addProviderObject("WorkspaceConfig", cfg);
/*  65 */     loadResources();
/*  66 */     provider.markState("ready");
/*  67 */     this.m_inited = true;
/*     */   }
/*     */ 
/*     */   public void loadResources()
/*     */     throws DataException, ServiceException
/*     */   {
/*  73 */     if (this.m_loaded)
/*     */     {
/*  75 */       return;
/*     */     }
/*  77 */     DataBinder providerData = this.m_provider.getProviderData();
/*  78 */     String keys = providerData.getAllowMissing("ExtraStorageKeys");
/*  79 */     List list = StringUtils.makeListFromSequence(keys, ',', '^', 0);
/*     */ 
/*  81 */     DataResultSet extraStorage = SharedObjects.getTable("ExternalResourceFiles");
/*  82 */     if (list.size() == 0)
/*     */     {
/*  84 */       boolean useSystemDatabaseDefaults = SharedObjects.getEnvValueAsBoolean("UseSystemDatabaseDefaultsForProvider", true);
/*     */ 
/*  86 */       boolean isSystemDatabase = this.m_provider.getName().equalsIgnoreCase("systemdatabase");
/*  87 */       boolean skipResourceLoad = DataBinderUtils.getBoolean(providerData, "SkipResourceLoadForSystemDatabase", false);
/*     */ 
/*  89 */       if ((useSystemDatabaseDefaults) || ((isSystemDatabase) && (!skipResourceLoad)))
/*     */       {
/*  91 */         list.add("system");
/*     */       }
/*     */     }
/*     */ 
/*  95 */     ResourceContainer rc = new ResourceContainer();
/*  96 */     for (String key : list)
/*     */     {
/*  98 */       if (key.equalsIgnoreCase("system"))
/*     */       {
/* 100 */         loadSystemQueries();
/*     */       }
/*     */ 
/* 103 */       String type = ResultSetUtils.findValue(extraStorage, "esfKey", key, "esfType");
/* 104 */       String path = ResultSetUtils.findValue(extraStorage, "esfKey", key, "esfFile");
/* 105 */       String tableNames = ResultSetUtils.findValue(extraStorage, "esfKey", key, "esfTables");
/* 106 */       String compName = ResultSetUtils.findValue(extraStorage, "esfKey", key, "idcCompnentName");
/* 107 */       if (type.equalsIgnoreCase("query"))
/*     */       {
/* 109 */         String filePath = getComponentFilePath(path, compName);
/* 110 */         ResourceLoader.loadQueries(filePath, tableNames, this.m_workspace, compName);
/*     */       }
/* 112 */       if (type.equalsIgnoreCase("resource"))
/*     */       {
/* 114 */         String filePath = getComponentFilePath(path, compName);
/* 115 */         ResourceLoader.loadResourceFile(rc, filePath);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 120 */     ResultSet rset = SharedObjects.getTable("ColumnTranslation");
/* 121 */     if (rset != null)
/*     */     {
/* 123 */       providerData.addResultSet("ColumnMap", rset);
/*     */     }
/* 125 */     for (String key : rc.m_tables.keySet())
/*     */     {
/* 127 */       Table table = (Table)rc.m_tables.get(key);
/* 128 */       DataResultSet drset = new DataResultSet();
/* 129 */       drset.init(table);
/* 130 */       if ((key.equalsIgnoreCase("ColumnTranslation")) || (key.equalsIgnoreCase("ColumnMap")))
/*     */       {
/* 132 */         ResultSet columnMap = providerData.getResultSet("ColumnMap");
/* 133 */         if (columnMap != null)
/*     */         {
/* 135 */           drset.merge("column", columnMap, false);
/*     */         }
/* 137 */         providerData.addResultSet("ColumnMap", drset);
/*     */       }
/*     */       else
/*     */       {
/* 141 */         providerData.addResultSet(key, drset);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 146 */     String queryFile = providerData.getLocal("QueryResourceFile");
/* 147 */     if (queryFile != null)
/*     */     {
/* 149 */       String location = this.m_provider.getLocation();
/*     */ 
/* 151 */       String absResPath = FileUtils.getAbsolutePath(location, queryFile);
/* 152 */       String tableNames = providerData.getLocal("QueryResourceTables");
/* 153 */       String cmptName = this.m_provider.getName();
/* 154 */       ResourceLoader.loadQueries(absResPath, tableNames, this.m_workspace, cmptName);
/*     */     }
/*     */ 
/* 157 */     this.m_loaded = true;
/*     */   }
/*     */ 
/*     */   protected String getComponentFilePath(String path, String compName)
/*     */   {
/* 162 */     HashMap components = (HashMap)this.m_provider.getProviderObject("Components");
/* 163 */     DataBinder binder = (DataBinder)components.get(compName);
/* 164 */     String dir = binder.getLocal("ComponentDir");
/*     */ 
/* 166 */     return dir + "/" + path;
/*     */   }
/*     */ 
/*     */   public void loadSystemQueries() throws ServiceException, DataException
/*     */   {
/* 171 */     ResourceTrace.msg("!csComponentLoadQueries");
/* 172 */     Vector queryData = (Vector)this.m_provider.getProviderObject("ComponentQueries");
/* 173 */     int num = queryData.size();
/* 174 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 176 */       ComponentData data = (ComponentData)queryData.elementAt(i);
/*     */ 
/* 178 */       String str = "!csComponentLoadSystemQuery";
/* 179 */       if (!data.m_componentName.equalsIgnoreCase("default"))
/*     */       {
/* 181 */         str = LocaleUtils.encodeMessage("csComponentLoadName", null, data.m_componentName);
/*     */       }
/*     */ 
/* 184 */       ResourceTrace.msg(str);
/*     */ 
/* 186 */       boolean queryTablesMustExist = true;
/* 187 */       ResourceContainer res = ComponentDataUtils.getOrLoadQueryResources(data);
/*     */ 
/* 189 */       Vector tables = data.m_tables;
/* 190 */       int numTables = tables.size();
/* 191 */       for (int j = 0; j < numTables; ++j)
/*     */       {
/* 193 */         QueryUtils.addQueryTable(this.m_workspace, res, (String)tables.elementAt(j), queryTablesMustExist, data.m_componentName);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 201 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79050 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.WorkspaceProviderConfig
 * JD-Core Version:    0.5.4
 */