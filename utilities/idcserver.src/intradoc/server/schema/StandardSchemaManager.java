/*      */ package intradoc.server.schema;
/*      */ 
/*      */ import intradoc.common.ConfigFileParameters;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.gui.iwt.ComparatorImplementor;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.SerializeTable;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.FilterImplementor;
/*      */ import intradoc.shared.LRUManager;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.shared.schema.SchemaLoader;
/*      */ import intradoc.shared.schema.SchemaRelationData;
/*      */ import intradoc.shared.schema.SchemaResultSet;
/*      */ import intradoc.shared.schema.SchemaTableConfig;
/*      */ import intradoc.shared.schema.SchemaTableData;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class StandardSchemaManager
/*      */   implements ServerSchemaManager, FilterImplementor
/*      */ {
/*      */   protected String m_schemaDir;
/*      */   protected Hashtable m_storageClasses;
/*      */   protected Hashtable m_columnTranslation;
/*      */   protected SchemaPublisher m_publisher;
/*      */   protected SchemaPublisherThread m_publisherThread;
/*      */   protected SchemaUtils m_utils;
/*      */   protected SchemaHelper m_helper;
/*      */   protected boolean m_isInitialized;
/*      */   protected boolean m_isSubjectInitFinished;
/*      */   public ExecutionContext m_context;
/*      */   protected Workspace m_workspace;
/*      */   protected SchemaInitUtils m_initUtils;
/*      */ 
/*      */   public StandardSchemaManager()
/*      */   {
/*   36 */     this.m_schemaDir = null;
/*      */ 
/*   39 */     this.m_columnTranslation = new Hashtable();
/*   40 */     this.m_publisher = null;
/*   41 */     this.m_publisherThread = null;
/*   42 */     this.m_utils = null;
/*   43 */     this.m_helper = null;
/*      */ 
/*   45 */     this.m_isInitialized = false;
/*   46 */     this.m_isSubjectInitFinished = false;
/*      */ 
/*   49 */     this.m_initUtils = null;
/*      */   }
/*      */ 
/*      */   public void init(Workspace ws) throws DataException, ServiceException
/*      */   {
/*   54 */     if (this.m_isInitialized)
/*      */       return;
/*   56 */     this.m_workspace = ws;
/*   57 */     this.m_context = new ExecutionContextAdaptor();
/*   58 */     this.m_context.setCachedObject("ServerSchemaManager", this);
/*   59 */     this.m_schemaDir = (DirectoryLocator.getAppDataDirectory() + "schema/");
/*      */ 
/*   61 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir, 2, true);
/*   62 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "views/", 2, true);
/*   63 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "tables/", 2, true);
/*   64 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "relations/", 2, true);
/*   65 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "fields/", 2, true);
/*   66 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "targets/", 2, true);
/*   67 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_schemaDir + "publishlock/", 2, true);
/*   68 */     String webDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*   69 */     webDir = FileUtils.directorySlashes(webDir + "resources/");
/*      */ 
/*   71 */     this.m_utils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null));
/*      */ 
/*   74 */     this.m_context.setCachedObject("SchemaUtils", this.m_utils);
/*      */ 
/*   76 */     this.m_helper = ((SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null));
/*      */ 
/*   79 */     this.m_context.setCachedObject("SchemaHelper", this.m_helper);
/*      */ 
/*   83 */     if (this.m_publisher == null)
/*      */     {
/*   85 */       this.m_publisher = ((SchemaPublisher)ComponentClassFactory.createClassInstance("SchemaPublisher", "intradoc.server.schema.StandardSchemaPublisher", null));
/*      */     }
/*      */ 
/*   90 */     if (this.m_publisherThread == null)
/*      */     {
/*   92 */       this.m_publisherThread = ((SchemaPublisherThread)ComponentClassFactory.createClassInstance("SchemaPublisherThread", "intradoc.server.schema.StandardSchemaPublisherThread", null));
/*      */     }
/*      */ 
/*  100 */     String notifyDir = this.m_schemaDir + "publishlock";
/*  101 */     this.m_publisher.init(ws, webDir, this.m_context);
/*  102 */     this.m_publisherThread.init(notifyDir, this.m_context);
/*      */ 
/*  104 */     int publishingTestThreads = SharedObjects.getEnvironmentInt("SchemaPublisherTestThreads", 0);
/*      */ 
/*  106 */     for (int i = 0; i < publishingTestThreads; ++i)
/*      */     {
/*  108 */       ExecutionContext context = new ExecutionContextAdaptor();
/*  109 */       context.setCachedObject("ServerSchemaManager", this);
/*  110 */       SchemaPublisher publisher = (SchemaPublisher)ComponentClassFactory.createClassInstance("SchemaPublisher", "intradoc.server.schema.StandardSchemaPublisher", null);
/*      */ 
/*  113 */       SchemaPublisherThread publisherThread = (SchemaPublisherThread)ComponentClassFactory.createClassInstance("SchemaPublisherThread", "intradoc.server.schema.StandardSchemaPublisherThread", null);
/*      */ 
/*  116 */       notifyDir = this.m_schemaDir + "publishlock" + i;
/*  117 */       String myWebDir = SharedObjects.getEnvironmentValue("SchemaPublisherTestThreadWebDir" + i);
/*      */ 
/*  119 */       if (myWebDir == null)
/*      */       {
/*  121 */         myWebDir = webDir + "publish" + i;
/*      */       }
/*      */       else
/*      */       {
/*  125 */         webDir = FileUtils.getAbsolutePath(DirectoryLocator.getIntradocDir(), webDir);
/*      */       }
/*      */ 
/*  128 */       context.setCachedObject("SchemaUtils", this.m_utils);
/*  129 */       context.setCachedObject("SchemaHelper", this.m_helper);
/*  130 */       publisher.init(ws, myWebDir, context);
/*  131 */       publisherThread.init(notifyDir, context);
/*  132 */       publisherThread.setEnabled(true);
/*      */     }
/*      */ 
/*  135 */     DataResultSet schemaTypes = SharedObjects.getTable("SchemaTypes");
/*  136 */     this.m_storageClasses = new Hashtable();
/*  137 */     Hashtable initData = new Hashtable();
/*  138 */     for (schemaTypes.first(); schemaTypes.isRowPresent(); schemaTypes.next())
/*      */     {
/*  140 */       Properties props = schemaTypes.getCurrentRowProps();
/*  141 */       String shortName = props.getProperty("ShortName");
/*  142 */       String name = props.getProperty("SourceName");
/*  143 */       String cacheClass = props.getProperty("CacheClassName");
/*  144 */       String storageClass = props.getProperty("StorageClassName");
/*  145 */       String dirSuffix = props.getProperty("StorageDirSuffix");
/*      */ 
/*  147 */       if (SystemUtils.m_verbose)
/*      */       {
/*  149 */         Report.debug("schemastorage", "registering type " + name + " cached by class " + cacheClass + " and storage " + storageClass, null);
/*      */       }
/*      */ 
/*  153 */       SchemaResultSet set = (SchemaResultSet)ComponentClassFactory.createClassInstance(name, cacheClass, null);
/*      */ 
/*  156 */       SharedObjects.putTable(name, set);
/*  157 */       SchemaStorage storage = (SchemaStorage)ComponentClassFactory.createClassInstance(name + "Storage", storageClass, null);
/*      */ 
/*  160 */       storage.init(set, shortName, this.m_schemaDir + dirSuffix, this.m_schemaDir);
/*  161 */       this.m_storageClasses.put(name, storage);
/*  162 */       this.m_storageClasses.put(shortName, storage);
/*      */ 
/*  164 */       initData.put(name, set);
/*      */     }
/*      */ 
/*  167 */     LRUManager viewLRUManager = (LRUManager)ComponentClassFactory.createClassInstance("LRUManager", "intradoc.shared.LRUManager", null);
/*      */ 
/*  170 */     viewLRUManager.setTracingSection("schemacache");
/*  171 */     viewLRUManager.setUsage(SharedObjects.getTypedEnvironmentInt("SchemaViewCacheMaxSize", 10485760, 5, 1));
/*      */ 
/*  174 */     viewLRUManager.setMaximumAge(SharedObjects.getTypedEnvironmentInt("SchemaViewCacheMaxAge", 0, 18, 24));
/*      */ 
/*  177 */     SharedObjects.putObject("globalObjects", "SchemaViewLRUManager", viewLRUManager);
/*      */ 
/*  179 */     Report.trace("schemacache", null, "csMonitorSchemaCacheSize", new Object[] { viewLRUManager.getMaximumUsage() + " bytes" });
/*      */ 
/*  182 */     if (ws != null)
/*      */     {
/*  184 */       initData.put("Workspace", ws);
/*      */     }
/*  186 */     initData.put("SchemaPublisher", this.m_publisher);
/*  187 */     initData.put("SchemaPublisherThread", this.m_publisherThread);
/*      */ 
/*  189 */     SchemaViewConfig viewSet = (SchemaViewConfig)initData.get("SchemaViewConfig");
/*      */ 
/*  191 */     Vector loaders = buildLoaderList("SchemaLoaders", initData);
/*  192 */     for (int i = 0; i < loaders.size(); ++i)
/*      */     {
/*  194 */       viewSet.addLoader((SchemaLoader)loaders.elementAt(i));
/*      */     }
/*      */ 
/*  197 */     SubjectManager.addInitListener(this);
/*  198 */     if (ws != null)
/*      */     {
/*  200 */       ws.releaseConnection();
/*      */     }
/*      */ 
/*  203 */     this.m_initUtils = new SchemaInitUtils();
/*  204 */     this.m_initUtils.init(this);
/*      */ 
/*  206 */     if (Features.checkLevel("ContentManagement", "0"))
/*      */     {
/*  208 */       SubjectManager.addSubjectMonitor("users", this);
/*  209 */       SubjectManager.addSubjectMonitor("accounts", this);
/*  210 */       SubjectManager.addSubjectMonitor("docformats", this);
/*  211 */       SubjectManager.addSubjectMonitor("doctypes", this);
/*  212 */       SubjectManager.addSubjectMonitor("metadata", this);
/*  213 */       SubjectManager.addSubjectMonitor("metaoptlists", this);
/*  214 */       SubjectManager.addSubjectMonitor("schema", this);
/*  215 */       SubjectManager.addSubjectMonitor("users", this);
/*      */     }
/*  217 */     this.m_isInitialized = true;
/*      */   }
/*      */ 
/*      */   public SchemaStorage getStorageImplementor(String type)
/*      */   {
/*  223 */     if (this.m_storageClasses == null)
/*      */     {
/*  225 */       return null;
/*      */     }
/*  227 */     return (SchemaStorage)this.m_storageClasses.get(type);
/*      */   }
/*      */ 
/*      */   protected Vector buildLoaderList(String table, Hashtable initData)
/*      */     throws ServiceException
/*      */   {
/*  234 */     DataResultSet loaderSet = SharedObjects.getTable(table);
/*  235 */     Vector loaders = new IdcVector();
/*  236 */     for (loaderSet.first(); loaderSet.isRowPresent(); loaderSet.next())
/*      */     {
/*  238 */       Properties props = loaderSet.getCurrentRowProps();
/*  239 */       String name = props.getProperty("name");
/*  240 */       String classLocation = props.getProperty("class");
/*      */ 
/*  242 */       String loadOrder = props.getProperty("loadOrder");
/*      */ 
/*  244 */       if (classLocation.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  250 */         SchemaLoader loader = (SchemaLoader)ComponentClassFactory.createClassInstance(name, classLocation, null);
/*      */ 
/*  253 */         if (loader.init(initData))
/*      */         {
/*  258 */           Integer index = new Integer(loaderSet.getCurrentRow());
/*  259 */           Long loadOrderLong = new Long(loadOrder);
/*  260 */           Object[] loaderArray = { loadOrderLong, index, loader };
/*      */ 
/*  262 */           loaders.addElement(loaderArray);
/*      */         }
/*      */       }
/*      */       catch (Throwable t) {
/*  266 */         Report.trace("schemainit", "unable to init loader '" + name + "'", t);
/*      */ 
/*  268 */         String msg = LocaleUtils.encodeMessage("apSchemaHelperInitError", null, name);
/*      */ 
/*  270 */         Report.error("schemainit", msg, t);
/*      */       }
/*      */     }
/*      */ 
/*  274 */     ComparatorImplementor comparator = new ComparatorImplementor(2);
/*      */ 
/*  276 */     Sort.sortVector(loaders, comparator);
/*  277 */     Vector finalLoaders = new IdcVector();
/*  278 */     for (int i = 0; i < loaders.size(); ++i)
/*      */     {
/*  280 */       Object[] obj = (Object[])(Object[])loaders.elementAt(i);
/*  281 */       finalLoaders.addElement(obj[2]);
/*      */     }
/*  283 */     Report.trace("schemainit", "found " + finalLoaders.size() + " loaders in table " + table + ".", null);
/*      */ 
/*  285 */     return finalLoaders;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public SchemaStorage getTablesStorage()
/*      */   {
/*  293 */     SystemUtils.reportDeprecatedUsage("SchemaStorage.getTablesStorage()");
/*  294 */     return getStorageImplementor("SchemaTableConfig");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public SchemaStorage getViewsStorage()
/*      */   {
/*  302 */     SystemUtils.reportDeprecatedUsage("SchemaStorage.getViewsStorage()");
/*  303 */     return getStorageImplementor("SchemaViewConfig");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public SchemaStorage getRelationsStorage()
/*      */   {
/*  311 */     SystemUtils.reportDeprecatedUsage("SchemaStorage.getRelationsStorage()");
/*  312 */     return getStorageImplementor("SchemaRelationConfig");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public SchemaStorage getFieldsStorage()
/*      */   {
/*  320 */     SystemUtils.reportDeprecatedUsage("SchemaStorage.getFieldsStorage()");
/*  321 */     return getStorageImplementor("SchemaFieldConfig");
/*      */   }
/*      */ 
/*      */   public void refresh(Workspace ws) throws DataException, ServiceException
/*      */   {
/*  326 */     refresh(ws, 0);
/*      */   }
/*      */ 
/*      */   public void refresh(Workspace ws, int flags) throws DataException, ServiceException
/*      */   {
/*  331 */     if (!this.m_isInitialized)
/*      */     {
/*      */       try
/*      */       {
/*  335 */         init(ws);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  339 */         Report.trace("system", null, e);
/*  340 */         throw e;
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  344 */         Report.trace("system", null, e);
/*  345 */         throw e;
/*      */       }
/*      */     }
/*      */ 
/*  349 */     FileUtils.reserveDirectory(this.m_schemaDir, true);
/*      */     try
/*      */     {
/*  352 */       Report.trace("schemainit", "refreshing schema", null);
/*      */ 
/*  355 */       DataResultSet schemaObjectPermissions = new DataResultSet(new String[] { "schObjectKey", "modify", "rename", "synchronize", "delete" });
/*      */ 
/*  373 */       Enumeration en = this.m_storageClasses.keys();
/*  374 */       while (en.hasMoreElements())
/*      */       {
/*  376 */         String name = (String)en.nextElement();
/*  377 */         SchemaStorage storage = getStorageImplementor(name);
/*  378 */         storage.load(flags);
/*      */       }
/*  380 */       SharedObjects.putTable("SchemaObjectPermissions", schemaObjectPermissions);
/*  381 */       updateColumnMap(ws);
/*  382 */       promoteOptionLists();
/*      */     }
/*      */     finally
/*      */     {
/*  386 */       FileUtils.releaseDirectory(this.m_schemaDir, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void handleManagerEvent(int type) throws ServiceException
/*      */   {
/*  392 */     if (type != 2)
/*      */       return;
/*  394 */     this.m_isSubjectInitFinished = true;
/*      */     try
/*      */     {
/*  399 */       DataBinder binder = new DataBinder();
/*  400 */       PluginFilters.filter("createSchemaObjects", this.m_workspace, binder, this.m_context);
/*      */ 
/*  402 */       if (DataBinderUtils.getBoolean(binder, "IsChanged", false))
/*      */       {
/*  404 */         SubjectManager.notifyChanged("schema");
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  409 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  412 */     boolean enableSchemaPublish = SharedObjects.getEnvValueAsBoolean("EnableSchemaPublish", true);
/*      */ 
/*  414 */     this.m_publisherThread.setEnabled(enableSchemaPublish);
/*  415 */     publishAtStartup();
/*      */   }
/*      */ 
/*      */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext context)
/*      */     throws DataException, ServiceException
/*      */   {
/*  424 */     StandardSchemaManager mgr = (StandardSchemaManager)context.getCachedObject("ServerSchemaManager");
/*      */ 
/*  426 */     String schemaDir = mgr.m_schemaDir;
/*  427 */     FileUtils.reserveDirectory(schemaDir, true);
/*  428 */     Report.trace("schemainit", "doing filter in StandardSchemaManager", null);
/*      */     try
/*      */     {
/*  431 */       if (mgr.createFields())
/*      */       {
/*  433 */         binder.putLocal("IsChanged", "1");
/*      */       }
/*  435 */       if (mgr.promoteOptionLists())
/*      */       {
/*  437 */         binder.putLocal("IsChanged", "1");
/*      */       }
/*  439 */       if (mgr.createCoreObjects())
/*      */       {
/*  441 */         binder.putLocal("IsChanged", "1");
/*      */       }
/*  443 */       if (mgr.mergeInViewChanges())
/*      */       {
/*  445 */         binder.putLocal("IsChanged", "1");
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  450 */       FileUtils.releaseDirectory(schemaDir, true);
/*      */     }
/*      */ 
/*  453 */     return 0;
/*      */   }
/*      */ 
/*      */   protected void publishAtStartup()
/*      */   {
/*  460 */     boolean publishOnStartup = SharedObjects.getEnvValueAsBoolean("SchemaPublishOnStartup", false);
/*      */ 
/*  462 */     boolean schemaExists = FileUtils.checkFile(this.m_publisher.getPublishDirectory() + "/schema/", false, false) == 0;
/*      */ 
/*  464 */     if ((!publishOnStartup) && (schemaExists))
/*      */       return;
/*  466 */     Report.trace("schemainit", "publishAtStartup(): calling publish()", null);
/*      */     try
/*      */     {
/*  469 */       publish(0L, true);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  473 */       String msg = "!csSchemaUnableToRequestPublish";
/*  474 */       Report.error("schemainit", msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean createFields()
/*      */   {
/*  481 */     MetaFieldData metaFields = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*      */ 
/*  483 */     boolean notify = false;
/*  484 */     if (metaFields == null)
/*      */     {
/*  487 */       Report.trace("schemainit", "createFields(): DocMetaDefinition table is missing", null);
/*      */     }
/*  492 */     else if (createFieldDefinitions(metaFields, "dName", "DocMetaField"))
/*      */     {
/*  494 */       notify = true;
/*      */     }
/*      */ 
/*  497 */     if (createOverrideFieldDefinitions())
/*      */     {
/*  499 */       notify = true;
/*      */     }
/*      */ 
/*  502 */     DataResultSet userMeta = SharedObjects.getTable("UserMetaDefinition");
/*  503 */     if (userMeta == null)
/*      */     {
/*  506 */       Report.trace("schemainit", "createFields(): UserMetaDefinition table is missing", null);
/*      */     }
/*      */ 
/*  516 */     return notify;
/*      */   }
/*      */ 
/*      */   public boolean createFieldDefinitions(DataResultSet drset, String fieldNameField, String type)
/*      */   {
/*  522 */     boolean notify = false;
/*  523 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  525 */       Properties props = drset.getCurrentRowProps();
/*  526 */       String name = props.getProperty(fieldNameField);
/*  527 */       props.put("schFieldName", name);
/*  528 */       props.put("Is" + type, "true");
/*      */ 
/*  530 */       SchemaStorage fieldStorage = getStorageImplementor("SchemaFieldConfig");
/*  531 */       SchemaData data = fieldStorage.getSchemaData(name);
/*  532 */       if (data != null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  537 */       DataBinder binder = new DataBinder();
/*  538 */       binder.setLocalData(props);
/*  539 */       Exception theException = null;
/*      */       try
/*      */       {
/*  542 */         fieldStorage.createOrUpdate(name, binder, false);
/*  543 */         notify = true;
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  547 */         theException = e;
/*  548 */         if (e.m_errorCode == -17)
/*      */         {
/*  551 */           Report.trace("schemainit", "when creating field '" + name + "' it already existed", e);
/*      */ 
/*  553 */           theException = null;
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  558 */         theException = e;
/*      */       }
/*  560 */       if (theException == null)
/*      */         continue;
/*  562 */       String msg = LocaleUtils.encodeMessage("csSchemaUnableToPromoteField", null, name);
/*      */ 
/*  564 */       Report.error(null, msg, theException);
/*      */     }
/*      */ 
/*  567 */     return notify;
/*      */   }
/*      */ 
/*      */   public boolean createOverrideFieldDefinitions()
/*      */   {
/*  572 */     boolean notify = false;
/*  573 */     SchemaStorage fieldStorage = getStorageImplementor("SchemaFieldConfig");
/*      */ 
/*  575 */     DataBinder binder = new DataBinder();
/*  576 */     binder.putLocal("schFieldName", "primaryOverrideFormat");
/*  577 */     binder.putLocal("dType", "Text");
/*  578 */     binder.putLocal("dOptionListType", "choice");
/*  579 */     binder.putLocal("DefaultOptionLabel", "wwEmptyFormatOption");
/*  580 */     binder.putLocal("UseViewList", "1");
/*  581 */     binder.putLocal("OptionViewKey", "enabledDocFormats");
/*  582 */     binder.putLocal("dCaption", "wwFormat");
/*  583 */     binder.putLocal("dIsOptionList", "1");
/*      */ 
/*  585 */     if (createFieldDefinition(fieldStorage, binder))
/*      */     {
/*  587 */       notify = true;
/*      */     }
/*      */ 
/*  590 */     binder.putLocal("schFieldName", "alternateOverrideFormat");
/*  591 */     if (createFieldDefinition(fieldStorage, binder))
/*      */     {
/*  593 */       notify = true;
/*      */     }
/*      */ 
/*  596 */     return notify;
/*      */   }
/*      */ 
/*      */   public boolean createFieldDefinition(SchemaStorage fieldStorage, DataBinder binder)
/*      */   {
/*  601 */     boolean notify = false;
/*  602 */     String name = binder.getLocal("schFieldName");
/*  603 */     if (fieldStorage.getSchemaData(name) != null)
/*      */     {
/*  606 */       return notify;
/*      */     }
/*  608 */     Exception theException = null;
/*      */     try
/*      */     {
/*  611 */       fieldStorage.createOrUpdate(name, binder, false);
/*  612 */       notify = true;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  616 */       theException = e;
/*  617 */       if (e.m_errorCode == -17)
/*      */       {
/*  620 */         Report.trace("schemainit", "when creating field '" + name + "' it already existed", e);
/*      */ 
/*  622 */         theException = null;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  627 */       theException = e;
/*      */     }
/*  629 */     if (theException != null)
/*      */     {
/*  631 */       String msg = LocaleUtils.encodeMessage("csSchemaUnableToPromoteField", null, name);
/*      */ 
/*  633 */       Report.error(null, msg, theException);
/*      */     }
/*      */ 
/*  636 */     return notify;
/*      */   }
/*      */ 
/*      */   public void publish(long timer, boolean isImmediate)
/*      */     throws ServiceException
/*      */   {
/*  642 */     DataBinder settings = new DataBinder();
/*  643 */     publish(timer, isImmediate, settings);
/*      */   }
/*      */ 
/*      */   public void publish(long timer, boolean isImmediate, DataBinder settings)
/*      */     throws ServiceException
/*      */   {
/*  649 */     this.m_publisherThread.publish(timer, isImmediate, settings);
/*      */   }
/*      */ 
/*      */   public void resetPublishingTimers()
/*      */   {
/*  654 */     this.m_publisherThread.resetTimers();
/*      */   }
/*      */ 
/*      */   public void updateColumnMap(Workspace ws) throws DataException
/*      */   {
/*  659 */     SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/*  661 */     for (tables.first(); tables.isRowPresent(); tables.next())
/*      */     {
/*  663 */       SchemaTableData data = (SchemaTableData)tables.getData();
/*  664 */       DataResultSet drset = data.getResultSet("TableDefinition");
/*  665 */       if (drset == null)
/*      */       {
/*  667 */         Report.trace("schemainit", "Not loading column map for " + data.m_name + ": TableDefinition is null.", null);
/*      */       }
/*      */       else
/*      */       {
/*  671 */         FieldInfo info = new FieldInfo();
/*  672 */         if (!drset.getFieldInfo("ColumnName", info))
/*      */         {
/*  674 */           Report.trace("schemainit", "Not loading column map for " + data.m_name + ": ColumnName is missing.", null);
/*      */         }
/*      */         else
/*      */         {
/*  680 */           Vector columnList = new IdcVector();
/*  681 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*      */           {
/*  683 */             String name = drset.getStringValue(info.m_index);
/*  684 */             String exName = ws.checkOrUpdateColumnAlias(name, false);
/*  685 */             if (exName == null)
/*      */             {
/*  687 */               columnList.addElement(name);
/*      */             }
/*      */             else
/*      */             {
/*  691 */               drset.setCurrentValue(info.m_index, exName);
/*      */             }
/*      */           }
/*  694 */           loadColumnMap(ws, columnList);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkForExColumnMap(DataBinder binder, Workspace ws, String tableName) throws DataException {
/*  700 */     DataResultSet drset = (DataResultSet)binder.getResultSet("TableDefinition");
/*  701 */     if (drset == null)
/*      */       return;
/*  703 */     FieldInfo info = new FieldInfo();
/*  704 */     if (!drset.getFieldInfo("ColumnName", info))
/*      */     {
/*  706 */       Report.trace("schema", "Not loading column map for " + tableName + ": ColumnName is missing.", null);
/*      */     }
/*      */ 
/*  709 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  711 */       String name = drset.getStringValue(info.m_index);
/*  712 */       String exName = ws.checkOrUpdateColumnAlias(name, false);
/*  713 */       if ((exName == null) || (exName.equals(name)))
/*      */         continue;
/*  715 */       String errMsg = LocaleUtils.encodeMessage("!csSchemaRenameNotPermitted", null, exName, name);
/*  716 */       String locMsg = LocaleResources.localizeMessage(errMsg, null);
/*  717 */       throw new DataException(locMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean promoteOptionLists()
/*      */     throws ServiceException
/*      */   {
/*  739 */     if (!this.m_isInitialized)
/*      */     {
/*  741 */       Report.trace("schemainit", "skipping promoteOptionLists() because m_isInitialized is false", null);
/*  742 */       return false;
/*      */     }
/*      */ 
/*  745 */     DataResultSet drset = SharedObjects.getTable("OptionsList");
/*  746 */     if (drset == null)
/*      */     {
/*  748 */       Report.trace("schemainit", "skipping promoteOptionLists() because OptionsList is false", null);
/*      */ 
/*  750 */       return false;
/*      */     }
/*  752 */     FieldInfo keyInfo = new FieldInfo();
/*  753 */     drset.getFieldInfo("dKey", keyInfo);
/*      */ 
/*  755 */     String nonPromotableLists = SharedObjects.getEnvironmentValue("SchemaNonPromotableOptionLists");
/*      */ 
/*  757 */     if (nonPromotableLists == null)
/*      */     {
/*  759 */       nonPromotableLists = "docgifs:docaccounts:securitygroups:users:optionlists";
/*      */     }
/*      */ 
/*  762 */     nonPromotableLists = ":" + nonPromotableLists.toLowerCase() + ":";
/*      */ 
/*  764 */     boolean notify = false;
/*  765 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  767 */       String listName = drset.getStringValue(keyInfo.m_index);
/*  768 */       String lowerCaseName = listName.toLowerCase();
/*  769 */       if ((listName.startsWith("view://")) || (listName.startsWith("tree://")) || (nonPromotableLists.indexOf(":" + lowerCaseName + ":") >= 0))
/*      */       {
/*  772 */         if (!SystemUtils.m_verbose)
/*      */           continue;
/*  774 */         Report.debug("schemainit", "skipping option list \"" + listName + "\"", null);
/*      */       }
/*      */       else
/*      */       {
/*  778 */         SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/*  779 */         SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(listName);
/*  780 */         if (view != null)
/*      */           continue;
/*  782 */         Report.trace("schemainit", "promoting option list " + listName, null);
/*  783 */         String type = "optionList";
/*  784 */         if (drset.findRow(keyInfo.m_index, listName) != null)
/*      */         {
/*  786 */           type = "table";
/*      */         }
/*      */         try
/*      */         {
/*  790 */           this.m_initUtils.promoteOptionList(listName, type, false);
/*  791 */           notify = true;
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  795 */           String msg = LocaleUtils.encodeMessage("csUnableToPromoteOptionList", null, listName);
/*      */ 
/*  797 */           Report.error("schemainit", msg, e);
/*      */         }
/*      */       }
/*      */     }
/*  801 */     return notify;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void createCoreViews()
/*      */     throws ServiceException
/*      */   {
/*  809 */     createCoreObjects();
/*      */   }
/*      */ 
/*      */   public boolean createCoreObjects()
/*      */     throws ServiceException
/*      */   {
/*  816 */     boolean rc = false;
/*  817 */     boolean hasContentManagement = Features.checkLevel("ContentManagement", null);
/*      */ 
/*  819 */     if ((hasContentManagement) && (addDocTypesTable()))
/*      */     {
/*  821 */       rc = true;
/*      */     }
/*  823 */     if ((hasContentManagement) && (addDocTypesView()))
/*      */     {
/*  825 */       rc = true;
/*      */     }
/*  827 */     if ((hasContentManagement) && (addDocFormatsTable()))
/*      */     {
/*  829 */       rc = true;
/*      */     }
/*  831 */     if ((hasContentManagement) && (addOrUpdateDocFormatsView()))
/*      */     {
/*  833 */       rc = true;
/*      */     }
/*  835 */     if ((hasContentManagement) && (addOrUpdateEnabledDocFormatsView()))
/*      */     {
/*  837 */       rc = true;
/*      */     }
/*  839 */     if (addApplicationFieldView())
/*      */     {
/*  841 */       rc = true;
/*      */     }
/*  843 */     if (this.m_initUtils.addArrayView("intradoc.shared.TableFields", "METAFIELD_TYPES_OPTIONSLIST", "FieldTypes"))
/*      */     {
/*  846 */       rc = true;
/*      */     }
/*  848 */     if (this.m_initUtils.addArrayView("intradoc.shared.TableFields", "METAFIELD_OPTIONLISTTYPE_OPTIONSLIST", "OptionListTypes"))
/*      */     {
/*  851 */       rc = true;
/*      */     }
/*  853 */     if (this.m_initUtils.addSharedObjectsTableView("SchemaViewConfig", "UserViews", "schViewName", "schViewDescription", new String[] { "schCriteriaField0=schIsSystemObject", "schCriteriaValue0=false" }, null))
/*      */     {
/*  860 */       rc = true;
/*      */     }
/*  862 */     if ((hasContentManagement) && (addSecurityObjects()))
/*      */     {
/*  864 */       rc = true;
/*      */     }
/*  866 */     if ((hasContentManagement) && (addOptionsListTable()))
/*      */     {
/*  868 */       rc = true;
/*      */     }
/*  870 */     if ((hasContentManagement) && (addDocMetaTarget()))
/*      */     {
/*  872 */       rc = true;
/*      */     }
/*  874 */     if (addUserMetaTarget())
/*      */     {
/*  876 */       rc = true;
/*      */     }
/*  878 */     if (addApplicationTarget())
/*      */     {
/*  880 */       rc = true;
/*      */     }
/*  882 */     if ((hasContentManagement) && (addProfileTriggerValuesTable()))
/*      */     {
/*  884 */       rc = true;
/*      */     }
/*  886 */     if ((hasContentManagement) && (addOrUpdateProfileTriggerValuesView()))
/*      */     {
/*  888 */       rc = true;
/*      */     }
/*  890 */     DataResultSet drset = SharedObjects.getTable("SchemaSharedObjectsTableViews");
/*  891 */     if (drset != null)
/*      */     {
/*  893 */       SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/*  894 */       FieldInfo[] fi = null;
/*      */       try
/*      */       {
/*  897 */         fi = ResultSetUtils.createInfoList(drset, new String[] { "tableName", "viewName", "internalField", "displayField", "properties", "isEnabled", "version" }, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  903 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  906 */       IdcStringBuilder defaultExpr = null;
/*  907 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  909 */         String enabledStr = drset.getStringValue(fi[5].m_index);
/*  910 */         if (!StringUtils.convertToBool(enabledStr, true))
/*      */           continue;
/*  912 */         String tableName = drset.getStringValue(fi[0].m_index);
/*  913 */         String viewName = drset.getStringValue(fi[1].m_index);
/*  914 */         String version = drset.getStringValue(fi[6].m_index);
/*  915 */         boolean isUpdate = false;
/*  916 */         SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(viewName);
/*  917 */         if (view != null)
/*      */         {
/*  919 */           String key = view.get("schDesignSourceVersion");
/*  920 */           if (!SystemUtils.isOlderVersion(key, version))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  926 */           isUpdate = true;
/*      */         }
/*  928 */         String internalField = drset.getStringValue(fi[2].m_index);
/*  929 */         String displayField = drset.getStringValue(fi[3].m_index);
/*  930 */         String propertiesListStr = drset.getStringValue(fi[4].m_index);
/*  931 */         List propertiesList = StringUtils.makeListFromSequenceSimple(propertiesListStr);
/*  932 */         if (displayField.length() == 0)
/*      */         {
/*  934 */           displayField = internalField;
/*      */         }
/*  936 */         if (!internalField.equals(displayField))
/*      */         {
/*  938 */           if (defaultExpr == null)
/*      */           {
/*  940 */             defaultExpr = new IdcStringBuilder();
/*      */           }
/*      */           else
/*      */           {
/*  944 */             defaultExpr.setLength(0);
/*      */           }
/*      */ 
/*  947 */           if (StringUtils.findStringListIndexEx(propertiesList, "schDefaultDisplayExpression", 2) < 0)
/*      */           {
/*  950 */             defaultExpr.append("schDefaultDisplayExpression=<$");
/*  951 */             defaultExpr.append(displayField);
/*  952 */             defaultExpr.append("$>");
/*  953 */             String temp = defaultExpr.toStringNoRelease();
/*  954 */             propertiesList.add(temp);
/*      */           }
/*  956 */           addDefaultIfMissing(propertiesList, "schLocalizeWhenDisplayed", "schLocalizeWhenDisplayed=1");
/*  957 */           addDefaultIfMissing(propertiesList, "schIsServerSorted", "schIsServerSorted=false");
/*  958 */           addDefaultIfMissing(propertiesList, "schIsClientSorted", "schIsClientSorted=0");
/*  959 */           propertiesList.add("schDesignSourceVersion=" + version);
/*      */         }
/*  961 */         String componentName = drset.getStringValueByName("idcComponentName");
/*  962 */         if ((componentName != null) && (componentName.length() > 0))
/*      */         {
/*  964 */           propertiesList.add("schDesignComponentSourceName=" + componentName);
/*      */         }
/*  966 */         String[] properties = new String[propertiesList.size()];
/*  967 */         propertiesList.toArray(properties);
/*  968 */         if (!this.m_initUtils.addSharedObjectsTableViewToStorage(viewStorage, view, tableName, viewName, internalField, displayField, properties, null, isUpdate)) {
/*      */           continue;
/*      */         }
/*  971 */         rc = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  977 */     drset = SharedObjects.getTable("SchemaViewRelations");
/*  978 */     if (drset != null)
/*      */     {
/*  980 */       SchemaStorage relationStorage = getStorageImplementor("SchemaRelationConfig");
/*  981 */       FieldInfo[] fi = null;
/*      */       try
/*      */       {
/*  984 */         fi = ResultSetUtils.createInfoList(drset, new String[] { "relationName", "view1Name", "view1Column", "view2Name", "view2Column", "properties", "version", "isEnabled" }, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  990 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/*  993 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  995 */         String enabledStr = drset.getStringValue(fi[7].m_index);
/*  996 */         if (!StringUtils.convertToBool(enabledStr, true))
/*      */           continue;
/*  998 */         String relationName = drset.getStringValue(fi[0].m_index);
/*  999 */         String version = drset.getStringValue(fi[6].m_index);
/*      */ 
/* 1001 */         boolean isUpdate = false;
/* 1002 */         SchemaRelationData relation = (SchemaRelationData)relationStorage.getSchemaData(relationName);
/*      */ 
/* 1004 */         if (relation != null)
/*      */         {
/* 1006 */           String key = relation.get("schDesignSourceVersion");
/* 1007 */           if (!SystemUtils.isOlderVersion(key, version)) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1011 */           isUpdate = true;
/*      */         }
/*      */ 
/* 1014 */         DataBinder data = new DataBinder();
/* 1015 */         data.putLocal("schRelationName", relationName);
/* 1016 */         data.putLocal("schRelationType", "view");
/* 1017 */         data.putLocal("schView1Name", drset.getStringValue(fi[1].m_index));
/* 1018 */         data.putLocal("schView1Column", drset.getStringValue(fi[2].m_index));
/* 1019 */         data.putLocal("schView2Name", drset.getStringValue(fi[3].m_index));
/* 1020 */         data.putLocal("schView2Column", drset.getStringValue(fi[4].m_index));
/* 1021 */         String propertiesListStr = drset.getStringValue(fi[5].m_index);
/* 1022 */         List propertiesList = StringUtils.makeListFromSequenceSimple(propertiesListStr);
/*      */ 
/* 1024 */         for (String settingString : propertiesList)
/*      */         {
/* 1026 */           int index = settingString.indexOf(61);
/* 1027 */           if (index == -1)
/*      */           {
/* 1029 */             Report.trace("schemainit", "setting string '" + settingString + "' for relation " + relationName + " is missing an =", null);
/*      */           }
/*      */ 
/* 1034 */           String key = settingString.substring(0, index);
/* 1035 */           String value = settingString.substring(index + 1);
/* 1036 */           data.putLocal(key, value);
/*      */         }
/*      */ 
/* 1039 */         String componentName = drset.getStringValueByName("idcComponentName");
/* 1040 */         if ((componentName != null) && (componentName.length() > 0))
/*      */         {
/* 1042 */           data.putLocal("schDesignComponentSourceName", componentName);
/*      */         }
/*      */ 
/* 1045 */         if (!this.m_initUtils.addGenericObject("Relation", relationName, data, isUpdate))
/*      */           continue;
/* 1047 */         rc = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1053 */     drset = SharedObjects.getTable("SchemaTableRelations");
/* 1054 */     if (drset != null)
/*      */     {
/* 1056 */       SchemaStorage relationStorage = getStorageImplementor("SchemaRelationConfig");
/* 1057 */       FieldInfo[] fi = null;
/*      */       try
/*      */       {
/* 1060 */         fi = ResultSetUtils.createInfoList(drset, new String[] { "relationName", "table1Name", "table1Column", "table2Name", "table2Column", "properties", "version", "isEnabled" }, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1066 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/* 1069 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1071 */         String enabledStr = drset.getStringValue(fi[7].m_index);
/* 1072 */         if (!StringUtils.convertToBool(enabledStr, true))
/*      */           continue;
/* 1074 */         String relationName = drset.getStringValue(fi[0].m_index);
/* 1075 */         String version = drset.getStringValue(fi[6].m_index);
/*      */ 
/* 1077 */         boolean isUpdate = false;
/* 1078 */         SchemaRelationData relation = (SchemaRelationData)relationStorage.getSchemaData(relationName);
/*      */ 
/* 1080 */         if (relation != null)
/*      */         {
/* 1082 */           String key = relation.get("schDesignSourceVersion");
/* 1083 */           if (!SystemUtils.isOlderVersion(key, version)) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1087 */           isUpdate = true;
/*      */         }
/*      */ 
/* 1090 */         DataBinder data = new DataBinder();
/* 1091 */         data.putLocal("schRelationName", relationName);
/* 1092 */         data.putLocal("schTable1Table", drset.getStringValue(fi[1].m_index));
/* 1093 */         data.putLocal("schTable1Column", drset.getStringValue(fi[2].m_index));
/* 1094 */         data.putLocal("schTable2Table", drset.getStringValue(fi[3].m_index));
/* 1095 */         data.putLocal("schTable2Column", drset.getStringValue(fi[4].m_index));
/* 1096 */         String propertiesListStr = drset.getStringValue(fi[5].m_index);
/* 1097 */         List propertiesList = StringUtils.makeListFromSequenceSimple(propertiesListStr);
/*      */ 
/* 1099 */         for (String settingString : propertiesList)
/*      */         {
/* 1101 */           int index = settingString.indexOf(61);
/* 1102 */           if (index == -1)
/*      */           {
/* 1104 */             Report.trace("schemainit", "setting string '" + settingString + "' for relation " + relationName + " is missing an =", null);
/*      */           }
/*      */ 
/* 1109 */           String key = settingString.substring(0, index);
/* 1110 */           String value = settingString.substring(index + 1);
/* 1111 */           data.putLocal(key, value);
/*      */         }
/*      */ 
/* 1114 */         String componentName = drset.getStringValueByName("idcComponentName");
/* 1115 */         if ((componentName != null) && (componentName.length() > 0))
/*      */         {
/* 1117 */           data.putLocal("schDesignComponentSourceName", componentName);
/*      */         }
/*      */ 
/* 1120 */         if (!this.m_initUtils.addGenericObject("Relation", relationName, data, isUpdate))
/*      */           continue;
/* 1122 */         rc = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1128 */     return rc;
/*      */   }
/*      */ 
/*      */   public boolean mergeInViewChanges()
/*      */     throws ServiceException
/*      */   {
/* 1139 */     boolean rc = false;
/* 1140 */     DataResultSet drset = SharedObjects.getTable("SchemaViewDefinitionChanges");
/* 1141 */     if (drset != null)
/*      */     {
/* 1143 */       SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1144 */       FieldInfo[] fi = null;
/*      */       try
/*      */       {
/* 1147 */         fi = ResultSetUtils.createInfoList(drset, new String[] { "viewName", "propertiesToMerge", "isEnabled", "version" }, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1152 */         throw new ServiceException(e);
/*      */       }
/* 1154 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1156 */         String enabledStr = drset.getStringValue(fi[2].m_index);
/* 1157 */         if (!StringUtils.convertToBool(enabledStr, true))
/*      */           continue;
/* 1159 */         String viewName = drset.getStringValue(fi[0].m_index);
/* 1160 */         String version = drset.getStringValue(fi[3].m_index);
/* 1161 */         SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(viewName);
/* 1162 */         if (view == null)
/*      */         {
/* 1164 */           Report.trace("schemainit", "Merge rule into view " + viewName + " merged into view that does not exist", null);
/*      */         }
/*      */         else {
/* 1167 */           String key = view.get("schDesignMergeComponentSourceName");
/* 1168 */           if ((key != null) && (key.length() > 0) && (!SystemUtils.isOlderVersion(key, version)))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1174 */           String propertiesToMergeListStr = drset.getStringValue(fi[1].m_index);
/* 1175 */           List propertiesToMergeList = StringUtils.makeListFromSequenceSimple(propertiesToMergeListStr);
/* 1176 */           String componentName = drset.getStringValueByName("idcComponentName");
/* 1177 */           if ((componentName != null) && (componentName.length() > 0))
/*      */           {
/* 1179 */             propertiesToMergeList.add("schDesignMergeComponentSourceName=" + componentName);
/*      */           }
/* 1181 */           String[] propertiesToMerge = new String[propertiesToMergeList.size()];
/* 1182 */           propertiesToMergeList.toArray(propertiesToMerge);
/* 1183 */           DataBinder curData = view.getData();
/* 1184 */           if (!this.m_initUtils.mergeUpdateViewProperties(viewName, curData, viewStorage, propertiesToMerge))
/*      */             continue;
/* 1186 */           rc = true;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1192 */     return rc;
/*      */   }
/*      */ 
/*      */   public void addDefaultIfMissing(List propertiesList, String key, String expression)
/*      */   {
/* 1197 */     if (StringUtils.findStringListIndexEx(propertiesList, key, 2) >= 0)
/*      */       return;
/* 1199 */     propertiesList.add(expression);
/*      */   }
/*      */ 
/*      */   public boolean addDefaultTable(String tableName)
/*      */     throws ServiceException
/*      */   {
/* 1206 */     return this.m_initUtils.addDefaultTable(tableName, this.m_workspace);
/*      */   }
/*      */ 
/*      */   public boolean addDocTypesTable() throws ServiceException
/*      */   {
/* 1211 */     return addDefaultTable("DocTypes");
/*      */   }
/*      */ 
/*      */   public boolean addDocTypesView() throws ServiceException
/*      */   {
/* 1216 */     boolean doWork = false;
/* 1217 */     boolean isUpdate = false;
/* 1218 */     DataBinder binder = new DataBinder();
/*      */ 
/* 1220 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1221 */     SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData("docTypes");
/* 1222 */     if (view == null)
/*      */     {
/* 1224 */       doWork = true;
/* 1225 */       binder.putLocal("schViewName", "docTypes");
/* 1226 */       binder.putLocal("schViewDescription", "Document Types");
/* 1227 */       binder.putLocal("schViewType", "table");
/* 1228 */       binder.putLocal("schTableName", "DocTypes");
/* 1229 */       binder.putLocal("schViewColumns", "dDocType,dDescription,dGif");
/* 1230 */       binder.putLocal("schIsServerSorted", "true");
/* 1231 */       binder.putLocal("schSortOrder", "ascending");
/* 1232 */       binder.putLocal("schSortField", "dDocType");
/* 1233 */       binder.putLocal("schLabelColumn", "dDocType");
/* 1234 */       binder.putLocal("schInternalColumn", "dDocType");
/* 1235 */       binder.putLocal("schDefaultDisplayExpression", "<$dDescription$>");
/* 1236 */       binder.putLocal("schIsSystemObject", "true");
/* 1237 */       binder.putLocal("schLocalizeWhenDisplayed", "1");
/*      */     }
/*      */     else
/*      */     {
/* 1241 */       isUpdate = true;
/* 1242 */       view.populateBinder(binder);
/*      */ 
/* 1244 */       String curVersion = binder.getLocal("schDesignSourceVersion");
/* 1245 */       if (curVersion == null)
/*      */       {
/* 1247 */         curVersion = "1";
/*      */       }
/*      */ 
/* 1250 */       if (SystemUtils.isOlderVersion(curVersion, "2"))
/*      */       {
/* 1252 */         doWork = true;
/* 1253 */         binder.putLocal("schLocalizeWhenDisplayed", "1");
/* 1254 */         binder.putLocal("schDefaultDisplayExpression", "<$dDescription$>");
/*      */       }
/*      */     }
/*      */ 
/* 1258 */     if (doWork)
/*      */     {
/* 1260 */       binder.putLocal("schDesignSourceVersion", "2");
/*      */       try
/*      */       {
/* 1263 */         viewStorage.createOrUpdate("docTypes", binder, isUpdate);
/* 1264 */         return true;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1268 */         String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "docTypes");
/*      */ 
/* 1270 */         Report.error("schemainit", msg, e);
/*      */       }
/*      */     }
/*      */ 
/* 1274 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean addApplicationFieldView() throws ServiceException
/*      */   {
/* 1279 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1280 */     SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData("ApplicationFields");
/* 1281 */     if (view == null)
/*      */     {
/* 1283 */       DataBinder binder = new DataBinder();
/* 1284 */       binder.putLocal("schViewName", "ApplicationFields");
/* 1285 */       binder.putLocal("schViewDescription", "Application fields");
/* 1286 */       binder.putLocal("schViewType", "SharedObjectsTable");
/* 1287 */       binder.putLocal("schTableName", "SchemaFieldConfig");
/* 1288 */       binder.putLocal("schViewColumns", "schFieldName,schFieldType,schFieldCaption,dIsRequired,dIsOptionList,dOptionListType,dOptionListKey,schOrder,schFieldTarget");
/* 1289 */       binder.putLocal("schIsServerSorted", "true");
/* 1290 */       binder.putLocal("schSortOrder", "ascending");
/* 1291 */       binder.putLocal("schSortField", "schOrder");
/* 1292 */       binder.putLocal("schLabelColumn", "schFieldName");
/* 1293 */       binder.putLocal("schInternalColumn", "schFieldName");
/* 1294 */       binder.putLocal("schIsSystemObject", "true");
/* 1295 */       binder.putLocal("schCriteriaField0", "schFieldTarget");
/* 1296 */       binder.putLocal("schCriteriaValue0", "application");
/*      */       try
/*      */       {
/* 1299 */         viewStorage.createOrUpdate("ApplicationFields", binder, false);
/* 1300 */         return true;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1304 */         String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "ApplicationFields");
/*      */ 
/* 1306 */         Report.error("schemainit", msg, e);
/*      */       }
/*      */     }
/* 1309 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean addDocFormatsTable() throws ServiceException
/*      */   {
/* 1314 */     return addDefaultTable("DocFormats");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean addDocFormatsView()
/*      */     throws ServiceException
/*      */   {
/* 1321 */     return addOrUpdateDocFormatsView();
/*      */   }
/*      */ 
/*      */   public boolean addOrUpdateDocFormatsView() throws ServiceException
/*      */   {
/* 1326 */     return addOrUpdateDocFormatsViewInternal("docFormats");
/*      */   }
/*      */ 
/*      */   public boolean addOrUpdateEnabledDocFormatsView() throws ServiceException
/*      */   {
/* 1331 */     return addOrUpdateDocFormatsViewInternal("enabledDocFormats");
/*      */   }
/*      */ 
/*      */   protected boolean addOrUpdateDocFormatsViewInternal(String viewName) throws ServiceException
/*      */   {
/* 1336 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1337 */     SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(viewName);
/* 1338 */     boolean doWork = false;
/* 1339 */     DataBinder binder = new DataBinder();
/* 1340 */     boolean isUpdate = false;
/* 1341 */     if (view == null)
/*      */     {
/* 1343 */       doWork = true;
/* 1344 */       binder.putLocal("schViewName", viewName);
/* 1345 */       binder.putLocal("schViewDescription", "Document Formats");
/* 1346 */       binder.putLocal("schViewType", "table");
/* 1347 */       binder.putLocal("schTableName", "DocFormats");
/* 1348 */       binder.putLocal("schViewColumns", "dFormat,dConversion,dDescription,dIsEnabled");
/* 1349 */       binder.putLocal("schIsServerSorted", "true");
/* 1350 */       binder.putLocal("schSortOrder", "ascending");
/* 1351 */       binder.putLocal("schSortField", "dFormat");
/* 1352 */       binder.putLocal("schLabelColumn", "dFormat");
/* 1353 */       binder.putLocal("schInternalColumn", "dFormat");
/* 1354 */       binder.putLocal("schIsSystemObject", "true");
/* 1355 */       if (viewName.equals("enabledDocFormats"))
/*      */       {
/* 1357 */         binder.putLocal("schViewDescription", "Enabled Document Formats");
/* 1358 */         binder.putLocal("schViewType", "SharedObjectsTable");
/* 1359 */         binder.putLocal("schCriteriaField0", "dIsEnabled");
/* 1360 */         binder.putLocal("schCriteriaValue0", "1");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1365 */       isUpdate = true;
/* 1366 */       view.populateBinder(binder);
/* 1367 */       if (binder.getLocal("schDefaultDisplayExpression") == null)
/*      */       {
/* 1369 */         doWork = true;
/*      */       }
/* 1371 */       String columns = binder.getLocal("schViewColumns");
/* 1372 */       if (columns.indexOf(",dIsEnabled") < 0)
/*      */       {
/* 1374 */         columns = columns + ",dIsEnabled";
/* 1375 */         binder.putLocal("schViewColumns", columns);
/* 1376 */         doWork = true;
/*      */       }
/*      */     }
/*      */ 
/* 1380 */     if (doWork)
/*      */     {
/* 1382 */       binder.putLocal("schDefaultDisplayExpression", "<$if dDescription$><$lc(dDescription, dFormat)$><$else$><$dFormat$><$endif$>");
/*      */       try
/*      */       {
/* 1386 */         viewStorage.createOrUpdate(viewName, binder, isUpdate);
/* 1387 */         return true;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1391 */         String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "docFormats");
/*      */ 
/* 1393 */         Report.error("schemainit", msg, e);
/*      */       }
/*      */     }
/* 1396 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean addSecurityObjects() throws ServiceException
/*      */   {
/* 1401 */     boolean rc = false;
/* 1402 */     if (SharedObjects.getEnvValueAsBoolean("UseSchemaForSecurityFields", true))
/*      */     {
/* 1405 */       if (addSecurityGroupTable())
/*      */       {
/* 1407 */         rc = true;
/*      */       }
/* 1409 */       if (addSecurityGroupView())
/*      */       {
/* 1411 */         rc = true;
/*      */       }
/* 1413 */       if (addUsersTable())
/*      */       {
/* 1415 */         rc = true;
/*      */       }
/* 1417 */       if (addDocumentAuthorsView())
/*      */       {
/* 1419 */         rc = true;
/*      */       }
/* 1421 */       if (SharedObjects.getEnvValueAsBoolean("UseAccounts", false))
/*      */       {
/* 1423 */         if (addDocumentAccountsTable())
/*      */         {
/* 1425 */           rc = true;
/*      */         }
/* 1427 */         if (addDocumentAccountsView())
/*      */         {
/* 1429 */           rc = true;
/*      */         }
/*      */       }
/*      */     }
/* 1433 */     return rc;
/*      */   }
/*      */ 
/*      */   public boolean addSecurityGroupTable() throws ServiceException
/*      */   {
/* 1438 */     return addDefaultTable("SecurityGroups");
/*      */   }
/*      */ 
/*      */   public boolean addDocumentAccountsTable() throws ServiceException
/*      */   {
/* 1443 */     return addDefaultTable("DocumentAccounts");
/*      */   }
/*      */ 
/*      */   public boolean addUsersTable() throws ServiceException
/*      */   {
/* 1448 */     return addDefaultTable("Users");
/*      */   }
/*      */ 
/*      */   public boolean addOptionsListTable() throws ServiceException
/*      */   {
/* 1453 */     return addDefaultTable("OptionsList");
/*      */   }
/*      */ 
/*      */   public boolean addSecurityGroupView() throws ServiceException
/*      */   {
/* 1458 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1459 */     SchemaViewData viewDef = (SchemaViewData)viewStorage.getSchemaData("SecurityGroups");
/*      */ 
/* 1461 */     if (viewDef != null)
/*      */     {
/* 1463 */       return false;
/*      */     }
/* 1465 */     DataBinder binder = new DataBinder();
/* 1466 */     binder.putLocal("schViewName", "SecurityGroups");
/* 1467 */     binder.putLocal("schViewType", "table");
/* 1468 */     binder.putLocal("schTableName", "SecurityGroups");
/* 1469 */     binder.putLocal("schTablePrimaryKey", "dGroupName");
/* 1470 */     binder.putLocal("schLabelColumn", "dDescription");
/* 1471 */     binder.putLocal("schViewColumns", "dGroupName,dDescription");
/* 1472 */     binder.putLocal("schInternalColumn", "dGroupName");
/* 1473 */     binder.putLocal("schIsServerSorted", "true");
/* 1474 */     binder.putLocal("schSortField", "dGroupName");
/* 1475 */     binder.putLocal("schSortOrder", "ascending");
/* 1476 */     binder.putLocal("schIsClientSorted", "1");
/* 1477 */     binder.putLocal("schClientSortOrder", "ascending");
/* 1478 */     binder.putLocal("schClientSortField", "localizedDisplay");
/* 1479 */     binder.putLocal("schDefaultDisplayExpression", "<$dGroupName$>");
/* 1480 */     binder.putLocal("PublishViewData", "0");
/* 1481 */     binder.putLocal("schSecurityImplementor", "intradoc.server.schema.StandardSchemaSecurityFilter");
/*      */ 
/* 1483 */     binder.putLocal("schSecurityImplementorColumnMap", "dGroupName:dSecurityGroup");
/*      */ 
/* 1485 */     binder.putLocal("schIsSystemObject", "true");
/*      */     try
/*      */     {
/* 1488 */       viewStorage.createOrUpdate("SecurityGroups", binder, false);
/* 1489 */       return true;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1493 */       Report.trace("schemainit", null, e);
/* 1494 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "SecurityGroups");
/*      */ 
/* 1496 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addDocumentAccountsView() throws ServiceException
/*      */   {
/* 1502 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1503 */     SchemaViewData viewDef = (SchemaViewData)viewStorage.getSchemaData("DocumentAccounts");
/*      */ 
/* 1505 */     if (viewDef != null)
/*      */     {
/* 1507 */       return false;
/*      */     }
/* 1509 */     DataBinder binder = new DataBinder();
/* 1510 */     binder.putLocal("schViewName", "DocumentAccounts");
/* 1511 */     binder.putLocal("schViewType", "table");
/* 1512 */     binder.putLocal("schTableName", "DocumentAccounts");
/* 1513 */     binder.putLocal("schTablePrimaryKey", "dDocAccount");
/* 1514 */     binder.putLocal("schLabelColumn", "dDocAccount");
/* 1515 */     binder.putLocal("schViewColumns", "dDocAccount");
/* 1516 */     binder.putLocal("schInternalColumn", "dDocAccount");
/* 1517 */     binder.putLocal("schIsServerSorted", "true");
/* 1518 */     binder.putLocal("schSortField", "dDocAccount");
/* 1519 */     binder.putLocal("schSortOrder", "ascending");
/* 1520 */     binder.putLocal("schIsClientSorted", "1");
/* 1521 */     binder.putLocal("schClientSortField", "localizedDisplay");
/* 1522 */     binder.putLocal("schClientSortOrder", "ascending");
/* 1523 */     binder.putLocal("schDefaultDisplayExpression", "<$dDocAccount$>");
/* 1524 */     binder.putLocal("PublishViewData", "0");
/* 1525 */     binder.putLocal("schSecurityImplementor", "intradoc.server.schema.StandardSchemaSecurityFilter");
/*      */ 
/* 1527 */     binder.putLocal("schIsSystemObject", "true");
/*      */     try
/*      */     {
/* 1530 */       viewStorage.createOrUpdate("DocumentAccounts", binder, false);
/* 1531 */       return true;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1535 */       Report.trace("schemainit", null, e);
/* 1536 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "DocumentAccounts");
/*      */ 
/* 1538 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addDocumentAuthorsView() throws ServiceException
/*      */   {
/* 1544 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1545 */     SchemaViewData viewDef = (SchemaViewData)viewStorage.getSchemaData("docAuthors");
/*      */ 
/* 1547 */     boolean isUpdate = false;
/* 1548 */     if (viewDef != null)
/*      */     {
/* 1550 */       if (!viewDef.get("schViewType", "optionList").equals("optionList"))
/*      */       {
/* 1552 */         return false;
/*      */       }
/* 1554 */       Report.trace("schemainit", "upgrading docAuthors view.", null);
/* 1555 */       isUpdate = true;
/*      */     }
/*      */ 
/* 1558 */     DataBinder binder = new DataBinder();
/* 1559 */     binder.putLocal("schViewName", "docAuthors");
/* 1560 */     binder.putLocal("schViewType", "table");
/* 1561 */     binder.putLocal("schTableName", "Users");
/* 1562 */     binder.putLocal("schTablePrimaryKey", "dName");
/* 1563 */     binder.putLocal("schLabelColumn", "dName");
/* 1564 */     binder.putLocal("schViewColumns", "dName,dFullName,dEmail,dUserType,dUserAuthType,dUserOrgPath,dUserSourceOrgPath,dUserSourceFlags,dUserArriveDate,dUserChangeDate,dUserLocale,dUserTimeZone");
/*      */ 
/* 1569 */     binder.putLocal("schInternalColumn", "dName");
/* 1570 */     binder.putLocal("schIsServerSorted", "true");
/* 1571 */     binder.putLocal("schSortField", "dName");
/* 1572 */     binder.putLocal("schSortOrder", "ascending");
/* 1573 */     binder.putLocal("schIsClientSorted", "1");
/* 1574 */     binder.putLocal("schClientSortField", "localizedDisplay");
/* 1575 */     binder.putLocal("schClientSortOrder", "ascending");
/* 1576 */     binder.putLocal("schDefaultDisplayExpression", "<$dName$>");
/* 1577 */     binder.putLocal("PublishViewData", "0");
/* 1578 */     binder.putLocal("schSecurityImplementor", "intradoc.server.schema.DocAuthorsSecurityFilter");
/*      */ 
/* 1580 */     binder.putLocal("schIsSystemObject", "true");
/*      */     try
/*      */     {
/* 1583 */       viewStorage.createOrUpdate("docAuthors", binder, isUpdate);
/* 1584 */       return true;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1588 */       Report.trace("schemainit", null, e);
/* 1589 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "docAuthors");
/*      */ 
/* 1591 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addProfileTriggerValuesTable() throws ServiceException
/*      */   {
/* 1597 */     return addDefaultTable("ProfileTriggerValues");
/*      */   }
/*      */ 
/*      */   public boolean addOrUpdateProfileTriggerValuesView() throws ServiceException
/*      */   {
/* 1602 */     SchemaStorage viewStorage = getStorageImplementor("SchemaViewConfig");
/* 1603 */     SchemaViewData viewDef = (SchemaViewData)viewStorage.getSchemaData("ProfileTriggerValues");
/*      */ 
/* 1605 */     if (viewDef != null)
/*      */     {
/* 1607 */       return false;
/*      */     }
/*      */ 
/* 1610 */     DataBinder binder = new DataBinder();
/* 1611 */     binder.putLocal("schViewName", "ProfileTriggerValues");
/* 1612 */     binder.putLocal("schViewDescription", "Profile Trigger Values");
/* 1613 */     binder.putLocal("schViewType", "table");
/* 1614 */     binder.putLocal("schTableName", "ProfileTriggerValues");
/* 1615 */     binder.putLocal("schTablePrimaryKey", "dProfileTriggerValue");
/* 1616 */     binder.putLocal("schLabelColumn", "dProfileTriggerValue");
/* 1617 */     binder.putLocal("schViewColumns", "dProfileTriggerValue,dProfileTriggerOrder");
/* 1618 */     binder.putLocal("schInternalColumn", "dProfileTriggerValue");
/* 1619 */     binder.putLocal("schIsDatabaseSorted", "true");
/* 1620 */     binder.putLocal("schSortField", "dProfileTriggerOrder");
/* 1621 */     binder.putLocal("schSortOrder", "ascending");
/* 1622 */     binder.putLocal("schDefaultDisplayExpression", "<$lcPrefix(\"wwProfile_\", dProfileTriggerValue)$>");
/* 1623 */     binder.putLocal("schLocalizeWhenDisplayed", "1");
/* 1624 */     binder.putLocal("schIsSystemObject", "true");
/*      */     try
/*      */     {
/* 1628 */       viewStorage.createOrUpdate("ProfileTriggerValues", binder, false);
/* 1629 */       return true;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1633 */       Report.trace("schemainit", null, e);
/* 1634 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, "ProfileTriggerValues");
/*      */ 
/* 1636 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addDocMetaTarget() throws ServiceException
/*      */   {
/* 1642 */     DataBinder binder = new DataBinder();
/*      */ 
/* 1644 */     binder.putLocal("schTargetName", "DocMeta");
/* 1645 */     binder.putLocal("schTargetType", "table");
/* 1646 */     binder.putLocal("schTargetTable", "DocMeta");
/* 1647 */     binder.putLocal("schIsSystemObject", "true");
/* 1648 */     binder.putLocal("schTargetDisplayName", "DocMeta");
/* 1649 */     binder.putLocal("schTargetFieldTable", "DocMetaDefinition");
/* 1650 */     binder.putLocal("schTargetFieldInsertQuery", "Imetadef");
/* 1651 */     binder.putLocal("schTargetFieldUpdateQuery", "Umetadef");
/* 1652 */     binder.putLocal("schTargetFieldDeleteQuery", "Dmetadef");
/* 1653 */     binder.putLocal("schKeyFieldName", "dName");
/* 1654 */     binder.putLocal("schOrderFieldName", "dOrder");
/* 1655 */     binder.putLocal("schPrefix", "d");
/*      */ 
/* 1657 */     DataResultSet info = createTargetFieldInfo(binder);
/* 1658 */     addRow(info, "dName,schFieldName,text,apLabelFieldName");
/* 1659 */     addRow(info, "dType,,view://FieldTypes,apLabelFieldType");
/* 1660 */     addRow(info, "dIsRequired,,boolean,apTitleRequired");
/* 1661 */     addRow(info, "dIsEnabled,dIsEnabled,boolean,apTitleEnabled");
/* 1662 */     addRow(info, "dIsSearchable,,boolean,apLabelEnableForSearchIndex");
/* 1663 */     addRow(info, "dIsOptionList,dIsOptionList,boolean,apLabelOptionList");
/* 1664 */     addRow(info, "dOptionListType,dOptionListType,view://OptionListTypes,apLabelOptionListType,dIsOptionList");
/* 1665 */     addRow(info, "dOptionListKey,dOptionListKey,view://UserViews,apLabelOptionListKey,dIsOptionList");
/* 1666 */     addRow(info, "dDefaultValue,dDefaultValue,text,apLabelDefaultValue");
/*      */ 
/* 1668 */     return this.m_initUtils.addGenericObject("Target", "DocMeta", binder, false);
/*      */   }
/*      */ 
/*      */   public boolean addUserMetaTarget() throws ServiceException
/*      */   {
/* 1673 */     DataBinder binder = new DataBinder();
/* 1674 */     binder.putLocal("schTargetName", "UserMeta");
/* 1675 */     binder.putLocal("schTargetType", "table");
/* 1676 */     binder.putLocal("schTargetTable", "Users");
/* 1677 */     binder.putLocal("schIsSystemObject", "true");
/* 1678 */     binder.putLocal("schPrefix", "u");
/* 1679 */     binder.putLocal("schTargetFieldResourceTable", "UserMetaDefinition");
/* 1680 */     String prefix = "";
/* 1681 */     String location = ConfigFileParameters.getLoadFromLocation();
/* 1682 */     if (location.equalsIgnoreCase("Database"))
/*      */     {
/* 1684 */       prefix = "vault/~system/";
/*      */     }
/* 1686 */     binder.putLocal("schTargetFieldResourceFile", prefix + "data/users/usermeta.hda");
/* 1687 */     binder.putLocal("schTargetDisplayName", "UserMeta");
/* 1688 */     binder.putLocal("schKeyFieldName", "umdName");
/* 1689 */     binder.putLocal("schOrderFieldName", "umdOrder");
/*      */ 
/* 1691 */     DataResultSet info = createTargetFieldInfo(binder);
/* 1692 */     addRow(info, "umdName,schFieldName,text,apLabelFieldName");
/* 1693 */     addRow(info, "umdType,,view://FieldTypes,apLabelFieldType");
/* 1694 */     addRow(info, "umdCaption,,text,apLabelFieldCaption");
/* 1695 */     addRow(info, "umdIsOptionList,dIsOptionList,boolean,apLabelEnableOptionList");
/* 1696 */     addRow(info, "umdOptionListType,dOptionListType,view://OptionListTypes,apLabelOptionListType,umdIsOptionList");
/* 1697 */     addRow(info, "umdOptionListKey,dOptionListKey,view://UserViews,apLabelOptionListKey,umdIsOptionList");
/* 1698 */     addRow(info, "umdIsAdminEdit,,boolean,apTitleAdminOnly");
/* 1699 */     addRow(info, "umdIsViewOnly,,boolean,apTitleViewOnly");
/* 1700 */     addRow(info, "umdOverrideBitFlag,,bitmask:OverrideMask,apLabelOverrideFlag");
/* 1701 */     return this.m_initUtils.addGenericObject("Target", "UserMeta", binder, false);
/*      */   }
/*      */ 
/*      */   public boolean addApplicationTarget() throws ServiceException
/*      */   {
/* 1706 */     Report.trace("schemainit", "adding Application target", null);
/* 1707 */     DataBinder binder = new DataBinder();
/* 1708 */     binder.putLocal("schTargetName", "Application");
/* 1709 */     binder.putLocal("schTargetType", "generic");
/* 1710 */     binder.putLocal("schIsSystemObject", "true");
/* 1711 */     binder.putLocal("schPrefix", "");
/* 1712 */     binder.putLocal("schTargetDisplayName", "General purpose application fields that target database tables.");
/*      */ 
/* 1714 */     binder.putLocal("schKeyFieldName", "schFieldName");
/* 1715 */     binder.putLocal("schOrderFieldName", "schOrder");
/*      */ 
/* 1717 */     DataResultSet info = createTargetFieldInfo(binder);
/* 1718 */     addRow(info, "schFieldName,schFieldName,text,apLabelFieldName");
/* 1719 */     addRow(info, "schFieldType,,view://FieldTypes,apLabelFieldType");
/* 1720 */     addRow(info, "schFieldCaption,,text,apLabelFieldCaption");
/* 1721 */     addRow(info, "dIsOptionList,dIsOptionList,boolean,apLabelEnableOptionList");
/* 1722 */     addRow(info, "schIsPlaceholderField,,boolean,apLabelPlaceholderField");
/* 1723 */     return this.m_initUtils.addGenericObject("Target", "Application", binder, false);
/*      */   }
/*      */ 
/*      */   public DataResultSet createTargetFieldInfo(DataBinder binder)
/*      */   {
/* 1728 */     DataResultSet info = new DataResultSet(new String[] { "schFieldName", "schSchemaField", "schFieldType", "schFieldCaption", "schEnableCriteria", "DependentOnField", "DependentRelationship" });
/*      */ 
/* 1734 */     binder.addResultSet("TargetFieldInfo", info);
/* 1735 */     return info;
/*      */   }
/*      */ 
/*      */   public void addRow(DataResultSet drset, String list)
/*      */   {
/* 1740 */     Vector row = StringUtils.parseArray(list, ',', '^');
/* 1741 */     while (row.size() < drset.getNumFields())
/*      */     {
/* 1743 */       row.addElement("");
/*      */     }
/* 1745 */     drset.addRow(row);
/*      */   }
/*      */ 
/*      */   public void loadColumnMap(Workspace ws, Vector list)
/*      */   {
/* 1750 */     if (ws == null)
/*      */       return;
/* 1752 */     DataResultSet columnMapSet = new DataResultSet(new String[] { "column", "alias" });
/*      */ 
/* 1755 */     int size = list.size();
/* 1756 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1758 */       String columnName = (String)list.elementAt(i);
/* 1759 */       Vector v = columnMapSet.createEmptyRow();
/* 1760 */       v.setElementAt(columnName.toUpperCase(), 0);
/* 1761 */       v.setElementAt(columnName, 1);
/* 1762 */       columnMapSet.addRow(v);
/*      */     }
/*      */ 
/* 1765 */     ws.loadColumnMap(columnMapSet);
/*      */   }
/*      */ 
/*      */   public void validateTableOperation(Workspace ws, String tableName, String operation)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1772 */     String[] reason = new String[1];
/* 1773 */     int rc = checkTableOperation(ws, tableName, operation, reason);
/* 1774 */     if (rc == 0)
/*      */       return;
/* 1776 */     throw new ServiceException(rc, reason[0]);
/*      */   }
/*      */ 
/*      */   public int checkTableOperation(Workspace ws, String tableName, String operation, String[] reason)
/*      */   {
/* 1783 */     if (reason == null)
/*      */     {
/* 1785 */       reason = new String[1];
/*      */     }
/*      */ 
/* 1788 */     if ((operation.equals("synchronize")) || (operation.equals("rename")))
/*      */     {
/* 1791 */       return 0;
/*      */     }
/*      */ 
/* 1796 */     DataResultSet drset = SharedObjects.getTable("SystemTables");
/* 1797 */     Vector row = drset.findRow(0, tableName);
/* 1798 */     if (row != null)
/*      */     {
/* 1800 */       reason[0] = LocaleUtils.encodeMessage("apSchemaObjectExists_system", null, tableName);
/*      */ 
/* 1802 */       return -17;
/*      */     }
/*      */ 
/* 1809 */     boolean isCreate = operation.equals("create");
/*      */ 
/* 1813 */     if (isCreate)
/*      */     {
/* 1817 */       SchemaStorage tables = getStorageImplementor("SchemaTableConfig");
/* 1818 */       SchemaTableData tableData = (SchemaTableData)tables.getSchemaData(tableName);
/*      */ 
/* 1820 */       if (tableData != null)
/*      */       {
/* 1822 */         reason[0] = LocaleUtils.encodeMessage("apSchemaObjectExists_table", null, tableName);
/*      */ 
/* 1824 */         return -16;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1829 */     boolean tableExists = WorkspaceUtils.doesTableExist(ws, tableName, null);
/* 1830 */     if ((isCreate) && (tableExists))
/*      */     {
/* 1832 */       reason[0] = LocaleUtils.encodeMessage("apSchemaObjectExists_tableInDB", null, tableName);
/*      */ 
/* 1834 */       return -17;
/*      */     }
/* 1836 */     if ((!isCreate) && (!tableExists))
/*      */     {
/* 1838 */       reason[0] = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_tableInDB", null, tableName);
/*      */ 
/* 1840 */       return -16;
/*      */     }
/*      */ 
/* 1843 */     return 0;
/*      */   }
/*      */ 
/*      */   public void addSchemaExistingTable(Workspace ws, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1849 */     FileUtils.reserveDirectory(this.m_schemaDir);
/*      */     try
/*      */     {
/* 1853 */       String tableName = binder.getLocal("schTableName");
/* 1854 */       DataResultSet drset = this.m_utils.createTableDefinition(ws, tableName);
/* 1855 */       binder.addResultSet("TableDefinition", drset);
/*      */ 
/* 1857 */       SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/* 1860 */       SchemaTableData newDefinition = new SchemaTableData();
/* 1861 */       newDefinition.init(tables);
/* 1862 */       newDefinition.update(binder);
/*      */ 
/* 1864 */       SchemaStorage tableStorage = getStorageImplementor("SchemaTableConfig");
/* 1865 */       checkForExColumnMap(binder, ws, tableName);
/* 1866 */       tableStorage.createOrUpdate(tableName, binder, true);
/* 1867 */       SubjectManager.notifyChanged("schema");
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */       String msg;
/* 1872 */       throw new ServiceException(msg);
/*      */     }
/*      */     finally
/*      */     {
/* 1876 */       FileUtils.releaseDirectory(this.m_schemaDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addSchemaTable(Workspace ws, DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1883 */     FileUtils.reserveDirectory(this.m_schemaDir);
/*      */     try
/*      */     {
/* 1887 */       SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/* 1889 */       String name = binder.getLocal("schTableName");
/* 1890 */       SchemaStorage tableStorage = getStorageImplementor("SchemaTableConfig");
/* 1891 */       checkForExColumnMap(binder, ws, name);
/* 1892 */       SchemaTableData def = new SchemaTableData();
/* 1893 */       def.init(tables);
/* 1894 */       def.update(binder);
/* 1895 */       validateTableOperation(ws, def.m_name, "create");
/*      */ 
/* 1898 */       String[] primaryKeys = new String[def.m_primaryKeyColumns.length];
/* 1899 */       for (int i = 0; i < primaryKeys.length; ++i)
/*      */       {
/* 1901 */         primaryKeys[i] = def.m_primaryKeyColumns[i].m_name;
/*      */       }
/* 1903 */       ws.createTable(def.m_name, def.m_columns, primaryKeys);
/*      */ 
/* 1906 */       tableStorage.createOrUpdate(name, binder, false);
/* 1907 */       SubjectManager.notifyChanged("schema");
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */       String msg;
/* 1912 */       throw new ServiceException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 1916 */       FileUtils.releaseDirectory(this.m_schemaDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void editSchemaTable(Workspace ws, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/* 1923 */     String tableName = null;
/* 1924 */     FileUtils.reserveDirectory(this.m_schemaDir);
/*      */     try
/*      */     {
/* 1927 */       SchemaTableConfig tables = (SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig");
/*      */ 
/* 1930 */       SchemaTableData newDefinition = new SchemaTableData();
/* 1931 */       newDefinition.init(tables);
/* 1932 */       newDefinition.update(binder);
/* 1933 */       tableName = newDefinition.m_name;
/*      */ 
/* 1935 */       String op = "rename";
/*      */ 
/* 1937 */       String tmp = binder.getLocal("_AddedColumns");
/* 1938 */       Vector newColumns = StringUtils.parseArray(tmp, ',', '^');
/* 1939 */       tmp = binder.getLocal("_DeletedColumns");
/* 1940 */       Vector deletedColumns = StringUtils.parseArray(tmp, ',', '^');
/* 1941 */       tmp = binder.getLocal("_ModifiedColumns");
/* 1942 */       Vector changedColumns = StringUtils.parseArray(tmp, ',', '^');
/*      */ 
/* 1944 */       FieldInfo[] oldInfos = ws.getColumnList(tableName);
/*      */ 
/* 1946 */       SchemaTableData oldDefinition = (SchemaTableData)tables.getData(tableName);
/*      */ 
/* 1948 */       String outOfSyncMessage = LocaleUtils.encodeMessage("csSchemaTableDefinitionOutOfDate", null, tableName);
/*      */ 
/* 1951 */       boolean isOutOfSync = false;
/* 1952 */       if (oldDefinition != null)
/*      */       {
/* 1954 */         Vector mismatchedColumns = oldDefinition.checkSynchronization(oldInfos);
/*      */ 
/* 1956 */         if (mismatchedColumns.size() > 0)
/*      */         {
/* 1958 */           isOutOfSync = true;
/*      */         }
/*      */       }
/*      */ 
/* 1962 */       Hashtable oldInfoMap = new Hashtable();
/* 1963 */       for (int i = 0; i < oldInfos.length; ++i)
/*      */       {
/* 1965 */         FieldInfo info = oldInfos[i];
/* 1966 */         oldInfoMap.put(info.m_name.toLowerCase(), info);
/*      */       }
/*      */ 
/* 1969 */       for (int i = 0; i < newColumns.size(); ++i)
/*      */       {
/* 1971 */         String newColumn = (String)newColumns.elementAt(i);
/* 1972 */         FieldInfo oldInfo = (FieldInfo)oldInfoMap.get(newColumn.toLowerCase());
/* 1973 */         if (oldInfo == null)
/*      */           continue;
/* 1975 */         throw new ServiceException(-23, outOfSyncMessage);
/*      */       }
/*      */ 
/* 1979 */       String[] primaryKeys = comparePrimaryKeys(tableName, newDefinition, ws);
/* 1980 */       changedColumns = computeChangedColumns(newDefinition, changedColumns, oldInfoMap, outOfSyncMessage);
/*      */ 
/* 1983 */       int size = newColumns.size();
/* 1984 */       FieldInfo[] addCols = new FieldInfo[size];
/* 1985 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1987 */         String fieldName = (String)newColumns.elementAt(i);
/* 1988 */         FieldInfo oldInfo = (FieldInfo)oldInfoMap.get(fieldName.toLowerCase());
/* 1989 */         if (oldInfo != null)
/*      */         {
/* 1991 */           String msg = LocaleUtils.encodeMessage("csSchemaTableDefinitionDuplicateColumn", null, fieldName);
/*      */ 
/* 1994 */           throw new ServiceException(msg);
/*      */         }
/* 1996 */         addCols[i] = newDefinition.getFieldInfo(fieldName);
/*      */       }
/*      */ 
/* 1999 */       size = deletedColumns.size();
/* 2000 */       String[] dropCols = new String[size];
/* 2001 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 2003 */         dropCols[i] = ((String)deletedColumns.elementAt(i));
/* 2004 */         FieldInfo info = (FieldInfo)oldInfoMap.get(dropCols[i].toLowerCase());
/* 2005 */         dropCols[i] = info.m_name;
/*      */       }
/*      */ 
/* 2008 */       if (dropCols.length + addCols.length + changedColumns.size() > 0)
/*      */       {
/* 2010 */         if (isOutOfSync)
/*      */         {
/* 2012 */           throw new ServiceException(-23, outOfSyncMessage);
/*      */         }
/*      */ 
/* 2016 */         op = "modify";
/*      */       }
/* 2018 */       validateTableOperation(ws, tableName, op);
/*      */ 
/* 2020 */       if ((deletedColumns.size() > 0) || (newColumns.size() > 0))
/*      */       {
/* 2022 */         ws.alterTable(newDefinition.m_name, addCols, dropCols, primaryKeys);
/* 2023 */         op = "modify";
/*      */       }
/*      */ 
/* 2027 */       size = changedColumns.size();
/* 2028 */       if (size > 0)
/*      */       {
/* 2030 */         String[] fields = new String[size];
/* 2031 */         int[] newLen = new int[size];
/* 2032 */         for (int i = 0; i < size; ++i)
/*      */         {
/* 2034 */           String columnName = (String)changedColumns.elementAt(i);
/* 2035 */           FieldInfo info = newDefinition.getFieldInfo(columnName);
/* 2036 */           fields[i] = info.m_name;
/* 2037 */           newLen[i] = info.m_maxLen;
/*      */         }
/* 2039 */         changeFieldLength(ws, tableName, primaryKeys, fields, newLen);
/*      */       }
/* 2041 */       SchemaStorage tableStorage = getStorageImplementor("SchemaTableConfig");
/* 2042 */       checkForExColumnMap(binder, ws, tableName);
/* 2043 */       tableStorage.createOrUpdate(tableName, binder, true);
/* 2044 */       SubjectManager.notifyChanged("schema");
/* 2045 */       if (tableName.equalsIgnoreCase("optionslist"))
/*      */       {
/* 2047 */         SubjectManager.forceRefresh("metaoptlists");
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */       String msg;
/* 2053 */       throw new ServiceException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 2057 */       FileUtils.releaseDirectory(this.m_schemaDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String[] comparePrimaryKeys(String tableName, SchemaTableData newDefinition, Workspace ws)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2066 */     Hashtable pkMap = new Hashtable();
/* 2067 */     int len = newDefinition.m_primaryKeyColumns.length;
/* 2068 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2070 */       String lowerName = newDefinition.m_primaryKeyColumns[i].m_name.toLowerCase();
/* 2071 */       pkMap.put(lowerName, newDefinition.m_primaryKeyColumns[i].m_name);
/*      */     }
/*      */ 
/* 2074 */     String[] pkColumns = ws.getPrimaryKeys(tableName);
/*      */ 
/* 2077 */     boolean isError = false;
/* 2078 */     len = pkColumns.length;
/* 2079 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2081 */       String key = pkColumns[i];
/* 2082 */       String lowerKey = key.toLowerCase();
/* 2083 */       Object obj = pkMap.get(lowerKey);
/* 2084 */       if (obj == null)
/*      */       {
/* 2086 */         Report.trace("schema", "comparePrimaryKeys(): column '" + key + "' is missing from the new primary key list.", null);
/*      */ 
/* 2089 */         isError = true;
/* 2090 */         break;
/*      */       }
/* 2092 */       pkMap.remove(lowerKey);
/*      */     }
/* 2094 */     if ((!pkMap.isEmpty()) || (isError))
/*      */     {
/* 2096 */       Enumeration en = pkMap.keys();
/* 2097 */       while (en.hasMoreElements())
/*      */       {
/* 2099 */         String key = (String)en.nextElement();
/* 2100 */         String val = (String)pkMap.get(key);
/* 2101 */         Report.trace("schema", "comparePrimaryKeys(): column '" + val + "' is added to the primary key list.", null);
/*      */       }
/*      */ 
/* 2105 */       String msg = LocaleUtils.encodeMessage("csSchemaTablePrimaryColumnsChanged", null);
/* 2106 */       throw new ServiceException(msg);
/*      */     }
/* 2108 */     return pkColumns;
/*      */   }
/*      */ 
/*      */   protected Vector computeChangedColumns(SchemaTableData newDefinition, Vector changedColumns, Hashtable oldInfoMap, String outOfSyncMessage)
/*      */     throws ServiceException
/*      */   {
/* 2115 */     Vector chngedColumns = new IdcVector();
/* 2116 */     for (int i = 0; i < changedColumns.size(); ++i)
/*      */     {
/* 2118 */       String changedColumn = (String)changedColumns.elementAt(i);
/* 2119 */       FieldInfo changedInfo = newDefinition.getFieldInfo(changedColumn);
/* 2120 */       FieldInfo oldInfo = (FieldInfo)oldInfoMap.get(changedColumn.toLowerCase());
/* 2121 */       if (oldInfo == null)
/*      */       {
/* 2123 */         String msg = LocaleUtils.encodeMessage("csSchemaTableDefinitionMissingColumn", null, changedColumn);
/*      */ 
/* 2125 */         msg = LocaleUtils.appendMessage(msg, outOfSyncMessage);
/* 2126 */         throw new ServiceException(-23, msg);
/*      */       }
/* 2128 */       if (oldInfo.m_type != changedInfo.m_type)
/*      */       {
/* 2130 */         String msg = LocaleUtils.encodeMessage("csRefuseToChangeFieldType", null, changedColumn);
/*      */ 
/* 2132 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/* 2138 */       if ((oldInfo.m_maxLen != changedInfo.m_maxLen) && (oldInfo.m_type == 2))
/*      */       {
/* 2141 */         String msg = LocaleUtils.encodeMessage("csRefuseToChangeFieldLength", null, changedColumn);
/*      */ 
/* 2143 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/* 2146 */       if ((oldInfo.m_type == 1) || (oldInfo.m_type == 5) || (oldInfo.m_type == 4)) continue; if (oldInfo.m_type == 3)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 2153 */       chngedColumns.addElement(changedColumn);
/*      */     }
/* 2155 */     return chngedColumns;
/*      */   }
/*      */ 
/*      */   public void changeFieldLength(Workspace ws, String tablename, String[] pk, String[] fields, int[] newLen)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2165 */     boolean pkChanged = false;
/* 2166 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/* 2168 */       if (StringUtils.findStringIndex(pk, fields[i]) < 0)
/*      */         continue;
/* 2170 */       pkChanged = true;
/* 2171 */       break;
/*      */     }
/*      */ 
/* 2175 */     if ((pkChanged) || (!ws.supportsSqlColumnChange()))
/*      */     {
/* 2178 */       String query = "select * from " + tablename;
/* 2179 */       ResultSet rset = ws.createResultSetSQL(query);
/* 2180 */       DataResultSet drset = new DataResultSet();
/* 2181 */       drset.copyFieldInfo(rset);
/* 2182 */       rset.closeInternals();
/* 2183 */       int nTableFields = drset.getNumFields();
/*      */ 
/* 2186 */       FieldInfo[] newTableFields = new FieldInfo[nTableFields];
/*      */ 
/* 2188 */       for (int i = 0; i < fields.length; ++i)
/*      */       {
/* 2190 */         for (int j = 0; j < nTableFields; ++j)
/*      */         {
/* 2192 */           FieldInfo fi = null;
/* 2193 */           if (i == 0)
/*      */           {
/* 2195 */             fi = new FieldInfo();
/* 2196 */             newTableFields[j] = fi;
/* 2197 */             drset.getIndexFieldInfo(j, fi);
/*      */           }
/*      */           else
/*      */           {
/* 2201 */             fi = newTableFields[j];
/*      */           }
/*      */ 
/* 2205 */           if (!fi.m_name.equals(fields[i]))
/*      */             continue;
/* 2207 */           fi.m_maxLen = newLen[i];
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2213 */       SerializeTable st = new SerializeTable(ws, null);
/* 2214 */       String dir = DirectoryLocator.getAppDataDirectory();
/* 2215 */       String name = tablename + "_upgrade.hda";
/* 2216 */       String tmpName = tablename + "_upgrade_temp.hda";
/* 2217 */       name = name.toLowerCase();
/* 2218 */       tmpName = tmpName.toLowerCase();
/* 2219 */       String filePath = dir + name;
/* 2220 */       if (FileUtils.checkFile(filePath, false, false) != 0)
/*      */       {
/* 2222 */         st.serialize(dir, tmpName, tablename, null, true);
/* 2223 */         FileUtils.renameFile(dir + tmpName, dir + name);
/*      */       }
/* 2225 */       ws.deleteTable(tablename);
/* 2226 */       ws.createTable(tablename, newTableFields, pk);
/* 2227 */       st.serialize(dir, name, tablename, null, false);
/* 2228 */       FileUtils.deleteFile(dir + name);
/*      */     }
/*      */     else
/*      */     {
/* 2232 */       FieldInfo[] fiNew = createFieldInfo(fields, 30);
/* 2233 */       for (int i = 0; i < newLen.length; ++i)
/*      */       {
/* 2235 */         fiNew[i].m_maxLen = newLen[i];
/*      */       }
/*      */ 
/* 2239 */       ws.alterTable(tablename, fiNew, null, pk);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteSchemaTable(Workspace ws, DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2247 */     SchemaTableData def = new SchemaTableData();
/* 2248 */     String tableName = binder.getLocal(def.getNameField());
/* 2249 */     String lockDir = this.m_schemaDir;
/* 2250 */     FileUtils.reserveDirectory(lockDir);
/*      */     try
/*      */     {
/* 2253 */       boolean dropTable = DataBinderUtils.getBoolean(binder, "DropTable", false);
/* 2254 */       if (dropTable)
/*      */       {
/* 2256 */         validateTableOperation(ws, tableName, "delete");
/* 2257 */         ws.deleteTable(tableName);
/*      */       }
/* 2259 */       SchemaStorage tables = getStorageImplementor("SchemaTableConfig");
/* 2260 */       tables.delete(tableName);
/* 2261 */       SubjectManager.notifyChanged("schema");
/*      */     }
/*      */     finally
/*      */     {
/* 2265 */       FileUtils.releaseDirectory(lockDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void synchronizeSchemaTableDefinition(Workspace ws, String tableName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2276 */     SchemaStorage tables = getStorageImplementor("SchemaTableConfig");
/* 2277 */     String storageDir = tables.getStorageDirectory();
/*      */     try
/*      */     {
/* 2280 */       FileUtils.reserveDirectory(storageDir);
/*      */ 
/* 2283 */       SchemaTableData tableData = (SchemaTableData)tables.getSchemaData(tableName);
/*      */ 
/* 2285 */       if (tableData == null)
/*      */       {
/* 2287 */         String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_table", null, tableName);
/*      */ 
/* 2289 */         throw new DataException(msg);
/*      */       }
/*      */ 
/* 2293 */       validateTableOperation(ws, tableName, "synchronize");
/*      */ 
/* 2295 */       FieldInfo[] dbInfos = ws.getColumnList(tableName);
/* 2296 */       String[] primaryKeys = ws.getPrimaryKeys(tableName);
/*      */ 
/* 2298 */       tableData = (SchemaTableData)tables.load(tableName, true);
/* 2299 */       DataBinder binder = tableData.getData();
/* 2300 */       DataResultSet columns = (DataResultSet)binder.getResultSet("TableDefinition");
/*      */ 
/* 2302 */       FieldInfo[] infos = ResultSetUtils.createInfoList(columns, SchemaTableData.TABLE_DEFINITION_COLUMNS, true);
/*      */ 
/* 2305 */       Hashtable dbInfoMap = new Hashtable();
/* 2306 */       for (int i = 0; i < dbInfos.length; ++i)
/*      */       {
/* 2308 */         dbInfoMap.put(dbInfos[i].m_name, dbInfos[i]);
/*      */       }
/*      */ 
/* 2311 */       String[][] specialFields = { { "schTableRowCreateTimestamp", null }, { "schTableRowModifyTimestamp", null } };
/*      */ 
/* 2316 */       for (int i = 0; i < specialFields.length; ++i)
/*      */       {
/* 2318 */         specialFields[i][1] = tableData.get(specialFields[i][0]);
/*      */       }
/*      */ 
/* 2321 */       for (int i = 0; i < tableData.m_columns.length; ++i)
/*      */       {
/* 2323 */         FieldInfo schemaInfo = tableData.m_columns[i];
/* 2324 */         String columnName = schemaInfo.m_name;
/* 2325 */         FieldInfo dbInfo = (FieldInfo)dbInfoMap.get(columnName);
/* 2326 */         dbInfoMap.remove(columnName);
/*      */ 
/* 2328 */         Vector row = columns.findRow(infos[SchemaTableData.COLUMN_NAME_INDEX].m_index, columnName);
/*      */ 
/* 2331 */         if (row == null)
/*      */         {
/* 2333 */           row = columns.createEmptyRow();
/* 2334 */           row.setElementAt(schemaInfo.m_name, SchemaTableData.COLUMN_NAME_INDEX);
/* 2335 */           columns.addRow(row);
/*      */         }
/*      */ 
/* 2338 */         if (dbInfo == null)
/*      */         {
/* 2340 */           if (SystemUtils.m_verbose)
/*      */           {
/* 2342 */             Report.debug("schemastorage", "removing row '" + columnName + "' in table '" + tableName + "'", null);
/*      */           }
/*      */ 
/* 2346 */           for (int j = 0; j < specialFields.length; ++j)
/*      */           {
/* 2348 */             if ((specialFields[j][1] == null) || (!schemaInfo.m_name.equals(specialFields[j][1]))) {
/*      */               continue;
/*      */             }
/* 2351 */             binder.removeLocal(specialFields[j][0]);
/*      */           }
/*      */ 
/* 2354 */           columns.deleteCurrentRow();
/*      */         }
/*      */         else
/*      */         {
/* 2359 */           if (dbInfo.m_maxLen != schemaInfo.m_maxLen)
/*      */           {
/* 2361 */             row.setElementAt("" + dbInfo.m_maxLen, infos[SchemaTableData.COLUMN_LENGTH_INDEX].m_index);
/*      */ 
/* 2363 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2365 */               Report.debug("schemastorage", "changing length of '" + columnName + "' in table '" + tableName + "'", null);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 2370 */           if (dbInfo.m_type != schemaInfo.m_type)
/*      */           {
/* 2372 */             String newTypeString = QueryUtils.convertInfoTypeToString(dbInfo.m_type);
/*      */ 
/* 2375 */             row.setElementAt(newTypeString, infos[SchemaTableData.COLUMN_TYPE_INDEX].m_index);
/*      */ 
/* 2377 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2379 */               Report.debug("schemastorage", "changing type of '" + schemaInfo.m_name + "' in table '" + tableName + "'", null);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 2385 */           boolean isPrimaryKey = false;
/* 2386 */           for (int j = 0; j < primaryKeys.length; ++j)
/*      */           {
/* 2388 */             if (!primaryKeys[j].equalsIgnoreCase(columnName))
/*      */               continue;
/* 2390 */             isPrimaryKey = true;
/* 2391 */             break;
/*      */           }
/*      */ 
/* 2394 */           row.setElementAt("" + isPrimaryKey, infos[SchemaTableData.PRIMARY_KEY_INDEX].m_index);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2400 */       Enumeration en = dbInfoMap.keys();
/* 2401 */       while (en.hasMoreElements())
/*      */       {
/* 2403 */         String columnName = (String)en.nextElement();
/* 2404 */         FieldInfo info = (FieldInfo)dbInfoMap.get(columnName);
/* 2405 */         if (SystemUtils.m_verbose)
/*      */         {
/* 2407 */           Report.debug("schemastorage", "adding column '" + columnName + "' in table '" + tableName + "'", null);
/*      */         }
/*      */ 
/* 2410 */         Vector row = columns.createEmptyRow();
/* 2411 */         String columnTypeString = QueryUtils.convertInfoTypeToString(info.m_type);
/*      */ 
/* 2414 */         row.setElementAt(columnName, infos[SchemaTableData.COLUMN_NAME_INDEX].m_index);
/*      */ 
/* 2416 */         row.setElementAt(columnTypeString, infos[SchemaTableData.COLUMN_TYPE_INDEX].m_index);
/*      */ 
/* 2418 */         row.setElementAt("" + info.m_maxLen, infos[SchemaTableData.COLUMN_LENGTH_INDEX].m_index);
/*      */ 
/* 2420 */         boolean isPrimaryKey = false;
/* 2421 */         for (int i = 0; i < primaryKeys.length; ++i)
/*      */         {
/* 2423 */           if (!primaryKeys[i].equalsIgnoreCase(columnName))
/*      */             continue;
/* 2425 */           isPrimaryKey = true;
/* 2426 */           break;
/*      */         }
/*      */ 
/* 2429 */         row.setElementAt("" + isPrimaryKey, infos[SchemaTableData.PRIMARY_KEY_INDEX].m_index);
/*      */ 
/* 2431 */         columns.addRow(row);
/*      */       }
/*      */ 
/* 2434 */       tables.createOrUpdateInternal(tableName, binder, true);
/*      */ 
/* 2436 */       SubjectManager.notifyChanged("schema");
/* 2437 */       getSchemaData(binder, "SchemaTableConfig");
/*      */     }
/*      */     finally
/*      */     {
/* 2441 */       FileUtils.releaseDirectory(storageDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void getSchemaData(DataBinder binder, String dataClass)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2448 */     SchemaResultSet serverConfig = (SchemaResultSet)SharedObjects.getTable(dataClass);
/*      */ 
/* 2450 */     DataResultSet remoteConfig = (DataResultSet)binder.getResultSet(dataClass);
/*      */ 
/* 2452 */     DataResultSet configSet = (DataResultSet)binder.getResultSet("SchemaConfigData");
/*      */ 
/* 2454 */     String[] ops = { "modify", "rename", "synchronize", "delete" };
/*      */ 
/* 2461 */     if (configSet == null)
/*      */     {
/* 2463 */       configSet = new DataResultSet(new String[] { "schDataClass", "schObjectName", "schData", "schError_modify", "schError_rename", "schError_synchronize", "schError_delete" });
/*      */ 
/* 2471 */       if (DataBinderUtils.getBoolean(binder, "LocalizeErrors", false))
/*      */       {
/* 2473 */         for (String op : ops)
/*      */         {
/* 2475 */           binder.m_blFieldTypes.put("schError_" + op, "message");
/*      */         }
/*      */       }
/* 2478 */       binder.addResultSet("SchemaConfigData", configSet);
/*      */     }
/*      */ 
/* 2481 */     if (serverConfig == null)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 2488 */     String className = serverConfig.getClass().getName();
/* 2489 */     Report.trace("schemacache", "Using client ResultSet for uptodate checks (className=" + className + ").", null);
/*      */ 
/* 2491 */     int nameIndex = -2;
/* 2492 */     int timestampIndex = -2;
/* 2493 */     if (remoteConfig != null)
/*      */     {
/* 2495 */       FieldInfo[] infos = ResultSetUtils.createInfoList(remoteConfig, serverConfig.m_columns, false);
/*      */ 
/* 2497 */       nameIndex = infos[serverConfig.m_nameIndex].m_index;
/* 2498 */       timestampIndex = infos[serverConfig.m_timestampIndex].m_index;
/*      */     }
/*      */ 
/* 2501 */     if ((remoteConfig == null) && (serverConfig.getNumRows() == 0))
/*      */     {
/* 2504 */       Vector row = new IdcVector();
/* 2505 */       row.addElement(className);
/* 2506 */       row.addElement("");
/* 2507 */       row.addElement("");
/* 2508 */       for (int i = 0; i < ops.length; ++i)
/*      */       {
/* 2510 */         row.addElement("");
/*      */       }
/* 2512 */       configSet.addRow(row);
/*      */     }
/*      */     else
/*      */     {
/* 2516 */       for (serverConfig.first(); serverConfig.isRowPresent(); serverConfig.next())
/*      */       {
/* 2518 */         String name = serverConfig.getName();
/* 2519 */         String ts = serverConfig.getTimestamp();
/* 2520 */         Vector v = null;
/* 2521 */         if (remoteConfig != null)
/*      */         {
/* 2523 */           v = remoteConfig.findRow(nameIndex, name);
/*      */         }
/* 2525 */         String remoteTs = null;
/* 2526 */         if (v != null)
/*      */         {
/* 2528 */           remoteTs = (String)v.elementAt(timestampIndex);
/*      */         }
/* 2530 */         if ((remoteTs == null) || (!ts.trim().equals(remoteTs.trim())))
/*      */         {
/* 2533 */           SchemaData data = serverConfig.getData(name);
/*      */           try
/*      */           {
/* 2536 */             String def = data.createStringRepresentation();
/* 2537 */             Vector row = new IdcVector();
/* 2538 */             row.addElement(className);
/* 2539 */             row.addElement(data.m_name);
/* 2540 */             row.addElement(def);
/* 2541 */             String[] reason = new String[1];
/* 2542 */             for (String op : ops)
/*      */             {
/* 2544 */               if ((data instanceof SchemaTableData) && (checkTableOperation(this.m_workspace, data.m_name, op, reason) != 0))
/*      */               {
/* 2548 */                 row.addElement(reason[0]);
/*      */               }
/*      */               else
/*      */               {
/* 2552 */                 row.addElement("");
/*      */               }
/*      */             }
/* 2555 */             configSet.addRow(row);
/* 2556 */             if (SystemUtils.m_verbose)
/*      */             {
/* 2558 */               Report.debug("schemamanager", "Adding data with name \"" + data.m_name + "\".", null);
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 2565 */             String msg = LocaleUtils.encodeMessage("csSchemaUnableToSerialize_" + dataClass, null, name);
/*      */ 
/* 2567 */             Report.error(null, msg, e);
/*      */           }
/*      */         } else {
/* 2570 */           if (!SystemUtils.m_verbose)
/*      */             continue;
/* 2572 */           Report.debug("schemamanager", "Client data for " + name + " is up to date", null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2577 */       if (remoteConfig == null) {
/*      */         return;
/*      */       }
/* 2580 */       String nameField = serverConfig.getFieldName(serverConfig.m_nameIndex);
/* 2581 */       FieldInfo nameInfo = new FieldInfo();
/* 2582 */       remoteConfig.getFieldInfo(nameField, nameInfo);
/* 2583 */       for (remoteConfig.first(); remoteConfig.isRowPresent(); remoteConfig.next())
/*      */       {
/* 2585 */         String name = remoteConfig.getStringValue(nameInfo.m_index);
/* 2586 */         SchemaData data = serverConfig.getData(name);
/* 2587 */         if (data != null) {
/*      */           continue;
/*      */         }
/* 2590 */         Vector row = new IdcVector();
/* 2591 */         row.addElement(className);
/* 2592 */         row.addElement(name);
/* 2593 */         row.addElement("");
/* 2594 */         for (int i = 0; i < ops.length; ++i)
/*      */         {
/* 2596 */           row.addElement("");
/*      */         }
/* 2598 */         configSet.addRow(row);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public FieldInfo[] createFieldInfo(String[] fields, int defLen)
/*      */   {
/* 2608 */     FieldInfo[] fi = new FieldInfo[fields.length];
/* 2609 */     for (int i = 0; i < fi.length; ++i)
/*      */     {
/* 2611 */       fi[i] = new FieldInfo();
/* 2612 */       fi[i].m_name = fields[i];
/* 2613 */       fi[i].m_isFixedLen = (defLen > 0);
/* 2614 */       fi[i].m_maxLen = defLen;
/*      */     }
/* 2616 */     return fi;
/*      */   }
/*      */ 
/*      */   public void addRowDefinition(DataResultSet drset, String columnName, String columnType, String columnLength, String primaryKey)
/*      */   {
/* 2623 */     Vector row = new IdcVector();
/* 2624 */     row.addElement(columnName);
/* 2625 */     row.addElement(columnType);
/* 2626 */     row.addElement(columnLength);
/* 2627 */     row.addElement(primaryKey);
/* 2628 */     drset.addRow(row);
/*      */   }
/*      */ 
/*      */   public boolean checkForChange(String subject, long curTime)
/*      */   {
/* 2633 */     return false;
/*      */   }
/*      */ 
/*      */   public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */   {
/*      */     try
/*      */     {
/* 2641 */       publish(curTime, !isExternal);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2645 */       String msg = "!csSchemaUnableToRequestPublish";
/* 2646 */       Report.error("schema", msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2652 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101214 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.StandardSchemaManager
 * JD-Core Version:    0.5.4
 */