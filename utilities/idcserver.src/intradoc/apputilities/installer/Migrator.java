/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.client.ClientUtils;
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseStringException;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ReportSubProgress;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.TracerReportUtils;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.utils.MessageLoggerUtils;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageUtils;
/*      */ import intradoc.util.MapUtils;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.lang.reflect.Field;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.lang.reflect.Method;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */ 
/*      */ public class Migrator
/*      */ {
/*      */   public static final int F_INIT_CLIENT = 1;
/*      */   public static final int F_INIT_LOGGER = 2;
/*      */   public static final int F_LOAD_RESOURCES = 4;
/*      */   public static final int F_INIT_DATABASE = 8;
/*      */   public static final int F_COMPUTE_ITEMS = 128;
/*      */   public static final int F_INIT_ITEMS = 256;
/*      */   public static final int F_MIGRATE_DATABASE = 1024;
/*      */   public static final int F_MIGRATE_FILESYSTEM = 2048;
/*      */   public MigrationEnvironment m_environment;
/*  938 */   protected static final String[] MIGRATE_STATE_COLUMN_NAMES = { "id", "version" };
/*      */ 
/*      */   public Migrator()
/*      */   {
/*  109 */     this.m_environment = new MigrationEnvironment();
/*      */   }
/*      */ 
/*      */   public void init(int flags, Object[] args)
/*      */     throws DataException, ServiceException
/*      */   {
/*  126 */     boolean doDatabase = (flags & 0x400) != 0;
/*  127 */     boolean doFilesystem = (flags & 0x800) != 0;
/*  128 */     MigrationEnvironment env = this.m_environment;
/*  129 */     env.m_migrateFlags = flags;
/*  130 */     String migrateType = env.m_migrateType;
/*  131 */     if (migrateType == null)
/*      */     {
/*  133 */       env.m_migrateType = (migrateType = "Migrate");
/*      */     }
/*      */ 
/*  137 */     initDate();
/*  138 */     env.m_reportSubProgresses = new ArrayList();
/*  139 */     env.m_other = new HashMap();
/*  140 */     if ((flags & 0x2) != 0)
/*      */     {
/*  142 */       initLogger();
/*      */     }
/*  144 */     if ((flags & 0x1) != 0)
/*      */     {
/*  146 */       initClient();
/*      */     }
/*  148 */     DataBinder binder = env.m_binder = new DataBinder();
/*  149 */     ExecutionContext context = env.m_context = new ExecutionContextAdaptor();
/*  150 */     context.setCachedObject("DataBinder", binder);
/*  151 */     env.m_merger = new PageMerger(binder, context);
/*  152 */     context.setCachedObject("PageMerger", env.m_merger);
/*      */ 
/*  154 */     loadSourceEnvironment();
/*  155 */     String dataDir = env.m_sourceEnvironment.getProperty("DataDir");
/*  156 */     SharedObjects.putEnvironmentValue("DataDir", dataDir);
/*  157 */     computeResources();
/*      */ 
/*  159 */     if ((flags & 0x4) != 0)
/*      */     {
/*  161 */       loadResources(this.m_environment.m_resourceFilenames);
/*      */     }
/*  163 */     if ((flags & 0x8) != 0)
/*      */     {
/*  165 */       env.m_workspace = initSystemDatabase();
/*      */     }
/*  167 */     if (doFilesystem)
/*      */     {
/*  169 */       prepareFilesystem();
/*      */     }
/*  171 */     if (SystemUtils.m_verbose)
/*      */     {
/*  173 */       Report.trace("installer", env.toString(), null);
/*      */     }
/*      */ 
/*  176 */     if ((flags & 0x80) != 0)
/*      */     {
/*  178 */       if (doDatabase)
/*      */       {
/*  180 */         computeDatabaseItems();
/*      */       }
/*  182 */       if (doFilesystem)
/*      */       {
/*  184 */         computeFilesystemItems();
/*      */       }
/*      */     }
/*  187 */     if ((flags & 0x100) == 0)
/*      */       return;
/*  189 */     initItemsForMigrate();
/*      */   }
/*      */ 
/*      */   public void initDate()
/*      */     throws ServiceException
/*      */   {
/*  200 */     MigrationEnvironment env = this.m_environment;
/*  201 */     Calendar startDate = env.m_startDate;
/*  202 */     if (startDate == null)
/*      */     {
/*  204 */       startDate = env.m_startDate = Calendar.getInstance();
/*      */     }
/*  206 */     IdcDateFormat format = env.m_timestampFormat;
/*  207 */     if (format == null)
/*      */     {
/*  209 */       format = env.m_timestampFormat = new IdcDateFormat();
/*      */       try
/*      */       {
/*  212 */         format.init("yyyyMMdd-hhmm");
/*      */       }
/*      */       catch (ParseStringException pse)
/*      */       {
/*  216 */         throw new ServiceException(pse);
/*      */       }
/*      */     }
/*  219 */     env.m_startDateString = format.format(startDate.getTime());
/*      */   }
/*      */ 
/*      */   public void initLogger()
/*      */   {
/*  227 */     TracerReportUtils.m_traceToConsole = false;
/*  228 */     intradoc.common.Log.m_defaultInfo = null;
/*      */ 
/*  230 */     TracerReportUtils.updateDefaultTracer("intradoc.idcwls.ServletLogger");
/*  231 */     Logger logger = Logger.getLogger(MessageLoggerUtils.m_loggerName, "intradoc.util.IdcWrapperBundle");
/*  232 */     if ((logger == null) || 
/*  234 */       (!SystemUtils.m_verbose))
/*      */       return;
/*  236 */     logger.setLevel(Level.FINER);
/*      */   }
/*      */ 
/*      */   public void initClient()
/*      */     throws ServiceException
/*      */   {
/*  248 */     IdcMessageUtils.init();
/*  249 */     ClientUtils.init();
/*  250 */     String[] defaultTraceSections = { "componentloader", "datastoredesign", "installer", "startup" };
/*  251 */     for (int s = defaultTraceSections.length - 1; s >= 0; --s)
/*      */     {
/*  253 */       SystemUtils.addAsDefaultTrace(defaultTraceSections[s]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadSourceEnvironment()
/*      */     throws DataException, ServiceException
/*      */   {
/*  264 */     MigrationEnvironment env = this.m_environment;
/*  265 */     env.m_sourceBinDir = FileUtils.directorySlashes(env.m_sourceBinDir);
/*  266 */     String sourceIntradocCfg = env.m_sourceBinDir + "intradoc.cfg";
/*  267 */     SystemPropertiesEditor sourceEditor = env.m_sourceSysPropsEditor = new SystemPropertiesEditor(sourceIntradocCfg);
/*      */ 
/*  269 */     sourceEditor.loadProperties();
/*  270 */     Properties props = env.m_sourceEnvironment = createSystemProperties(sourceEditor);
/*  271 */     env.m_sourceIntradocDir = sourceEditor.searchForValue("IntradocDir");
/*      */ 
/*  277 */     String productName = props.getProperty("IdcProductName");
/*  278 */     if (productName == null)
/*      */     {
/*  280 */       IdcMessage msg = IdcMessageFactory.lc("csUnableToFindValue", new Object[] { "IdcProductName" });
/*  281 */       Report.warning("install", null, msg);
/*      */ 
/*  283 */       productName = "idccs";
/*  284 */       props.setProperty("IdcProductName", productName);
/*      */     }
/*  286 */     else if (!productName.startsWith("idc"))
/*      */     {
/*  288 */       IdcMessage msg = IdcMessageFactory.lc("csMigrateBadProduct", new Object[] { productName });
/*  289 */       throw new ServiceException(null, msg);
/*      */     }
/*      */ 
/*  292 */     if (env.m_productName == null)
/*      */     {
/*  294 */       env.m_productName = productName.substring(3);
/*      */     }
/*      */ 
/*  297 */     env.m_sourceComponents = MigrateUtils.createComponentListEditor(props);
/*      */   }
/*      */ 
/*      */   public void computeResources()
/*      */     throws ServiceException
/*      */   {
/*  305 */     MigrationEnvironment env = this.m_environment;
/*  306 */     String specificProductName = env.m_specificProductName;
/*  307 */     if (specificProductName == null)
/*      */     {
/*  309 */       specificProductName = env.m_productName;
/*      */     }
/*  311 */     String productPrefix = "idc" + specificProductName;
/*  312 */     List resourceFilenames = env.m_resourceFilenames;
/*      */ 
/*  314 */     if (resourceFilenames == null)
/*      */     {
/*  316 */       resourceFilenames = env.m_resourceFilenames = new ArrayList();
/*  317 */       resourceFilenames.add("core/idoc/std_data_store_design.idoc");
/*  318 */       resourceFilenames.add("data/upgrade/" + productPrefix + "_upgrade_info.idoc");
/*      */     }
/*      */ 
/*  321 */     if ((env.m_migrateFlags & 0x8) != 0)
/*      */     {
/*  323 */       resourceFilenames.add("core/tables/query.htm");
/*  324 */       resourceFilenames.add("core/tables/upper_clmns_map.htm");
/*      */     }
/*  326 */     if ((env.m_migrateFlags & 0x400) != 0)
/*      */     {
/*  328 */       resourceFilenames.add("data/upgrade/" + productPrefix + "_data_store_design.idoc");
/*      */     }
/*  330 */     if ((env.m_migrateFlags & 0x800) == 0)
/*      */       return;
/*  332 */     resourceFilenames.add("data/upgrade/all_components.hda");
/*      */   }
/*      */ 
/*      */   public void loadResources(List<String> resourceFilenamesList)
/*      */     throws DataException, ServiceException
/*      */   {
/*  350 */     ClassLoader cl = super.getClass().getClassLoader();
/*  351 */     ResourceContainer rc = SharedObjects.getResources();
/*  352 */     int numFiles = resourceFilenamesList.size();
/*  353 */     for (int i = 0; i < numFiles; ++i)
/*      */     {
/*  355 */       String resourceFilename = (String)resourceFilenamesList.get(i);
/*  356 */       boolean isBinder = resourceFilename.endsWith(".hda");
/*  357 */       boolean isAbsolute = FileUtils.isAbsolutePath(resourceFilename);
/*  358 */       if (SystemUtils.m_verbose)
/*      */       {
/*  360 */         String msg = "loading resources from " + resourceFilename;
/*  361 */         Report.trace("installer", msg, null);
/*      */       }
/*      */       InputStream is;
/*  364 */       if (isAbsolute)
/*      */       {
/*      */         try
/*      */         {
/*  368 */           is = new FileInputStream(resourceFilename);
/*      */         }
/*      */         catch (IOException ioe)
/*      */         {
/*  372 */           IdcMessage msg = new IdcMessage("csCouldNotLoadFile", new Object[] { resourceFilename });
/*  373 */           throw new ServiceException(ioe, msg);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  378 */         is = cl.getResourceAsStream(resourceFilename);
/*  379 */         if (is == null)
/*      */         {
/*  381 */           IdcMessage msg = new IdcMessage("csCouldNotLoadFile", new Object[] { resourceFilename });
/*  382 */           throw new ServiceException(null, msg);
/*      */         }
/*      */       }
/*  385 */       BufferedInputStream bis = new BufferedInputStream(is);
/*      */       Exception e;
/*      */       try
/*      */       {
/*      */         Map rsets;
/*  389 */         if (isBinder)
/*      */         {
/*  391 */           DataBinder binder = new DataBinder();
/*  392 */           String encoding = DataSerializeUtils.detectEncoding(binder, bis, null);
/*  393 */           BufferedReader br = FileUtils.openDataReader(bis, encoding);
/*  394 */           binder.receiveEx(br, false);
/*  395 */           Properties props = binder.getEnvironment();
/*  396 */           Set names = props.stringPropertyNames();
/*  397 */           for (String name : names)
/*      */           {
/*  399 */             String value = props.getProperty(name);
/*  400 */             SharedObjects.putEnvironmentValue(name, value);
/*      */           }
/*  402 */           props = binder.getLocalData();
/*  403 */           names = props.stringPropertyNames();
/*  404 */           for (String name : names)
/*      */           {
/*  406 */             String value = props.getProperty(name);
/*  407 */             SharedObjects.putEnvironmentValue(name, value);
/*      */           }
/*  409 */           rsets = binder.getResultSets();
/*  410 */           for (String tableName : rsets.keySet())
/*      */           {
/*  412 */             DataResultSet rset = (DataResultSet)rsets.get(tableName);
/*  413 */             String[] columnNames = ResultSetUtils.getFieldListAsStringArray(rset);
/*  414 */             String[][] elements = ResultSetUtils.createStringTable(rset, null);
/*  415 */             List rows = CollectionUtils.copyToList(elements);
/*  416 */             Table table = new Table(columnNames, rows);
/*  417 */             rc.m_tables.put(tableName, table);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  422 */           BufferedReader br = FileUtils.openDataReader(bis, null);
/*  423 */           rc.parseAndAddResources(br, resourceFilename);
/*      */         }
/*  425 */         e = null;
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*  429 */         e = ioe;
/*      */       }
/*      */       catch (ParseSyntaxException pse)
/*      */       {
/*  433 */         e = pse;
/*      */       }
/*  435 */       if (e == null)
/*      */         continue;
/*  437 */       IdcMessage msg = new IdcMessage("csCouldNotLoadFile", new Object[] { resourceFilename });
/*  438 */       throw new ServiceException(e, msg);
/*      */     }
/*      */ 
/*  441 */     for (String name : rc.m_tables.keySet())
/*      */     {
/*  443 */       Table table = rc.getTable(name);
/*  444 */       DataResultSet drset = new DataResultSet();
/*  445 */       drset.init(table);
/*  446 */       SharedObjects.putTable(name, drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Workspace initSystemDatabase()
/*      */     throws DataException, ServiceException
/*      */   {
/*  463 */     MigrationEnvironment env = this.m_environment;
/*  464 */     SystemPropertiesEditor editor = env.m_sourceSysPropsEditor;
/*  465 */     String[] envVariables = { "JdbcConnectionString", "JdbcDriver", "JdbcUser", "JdbcPassword", "JdbcPasswordEncoding" };
/*      */ 
/*  471 */     ResourceContainer rc = SharedObjects.getResources();
/*  472 */     Table securityCategoriesTable = (Table)rc.m_tables.get("SecurityCategories");
/*  473 */     if (securityCategoriesTable == null)
/*      */     {
/*  475 */       IdcMessage msg = new IdcMessage("syResultSetMissing", new Object[] { "SecurityCategories" });
/*  476 */       throw new ServiceException(null, msg);
/*      */     }
/*  478 */     DataResultSet securityCategories = new DataResultSet();
/*  479 */     securityCategories.init(securityCategoriesTable);
/*  480 */     CryptoPasswordUtils.setEnvironment(env.m_sourceEnvironment);
/*  481 */     env.m_sourceEnvironment.put("SkipCreatePrivateDirectoryForSecurity", "1");
/*  482 */     if (!CryptoPasswordUtils.loadPasswordManagement(securityCategories))
/*      */     {
/*  484 */       IdcMessage msg = new IdcMessage("csInitCryptoPasswordError", new Object[0]);
/*  485 */       throw new ServiceException(null, msg);
/*      */     }
/*      */ 
/*  489 */     IdcSystemLoader.createSystemDatabase();
/*  490 */     DataBinder prBinder = Providers.getProviderData("SystemDatabase");
/*  491 */     for (int v = envVariables.length - 1; v >= 0; --v)
/*      */     {
/*  493 */       String varName = envVariables[v];
/*  494 */       String fieldName = "m_" + Character.toLowerCase(varName.charAt(0)) + varName.substring(1);
/*      */       Field field;
/*      */       String value;
/*      */       try
/*      */       {
/*  499 */         field = MigrationEnvironment.class.getField(fieldName);
/*  500 */         value = (String)field.get(env);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  504 */         field = null;
/*  505 */         value = null;
/*      */       }
/*  507 */       if (value == null)
/*      */       {
/*  509 */         value = SharedObjects.getEnvironmentValue(varName);
/*  510 */         if (value == null)
/*      */         {
/*  512 */           value = editor.searchForValue(varName);
/*  513 */           if (value == null)
/*      */           {
/*  515 */             IdcMessage msg = new IdcMessage("syParameterNotFound", new Object[] { varName });
/*  516 */             throw new DataException(null, msg);
/*      */           }
/*      */         }
/*      */       }
/*  520 */       if (field != null)
/*      */       {
/*      */         try
/*      */         {
/*  524 */           field.set(env, value);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  528 */           Report.trace("system", "Unable to set " + varName, e);
/*      */         }
/*      */       }
/*  531 */       SharedObjects.putEnvironmentValue(varName, value);
/*  532 */       prBinder.putLocal(varName, value);
/*      */     }
/*  534 */     SharedObjects.putEnvironmentValue("UseSystemDatabaseDefaultsForProvider", "0");
/*  535 */     SharedObjects.putEnvironmentValue("UseSecureFiles", "0");
/*  536 */     prBinder.putLocal("SkipResourceLoadForSystemDatabase", "1");
/*  537 */     Provider pr = new Provider(prBinder, null);
/*  538 */     pr.init();
/*  539 */     Providers.addProvider("SystemDatabase", pr);
/*  540 */     Workspace ws = IdcSystemLoader.loadDatabaseWithFlags(2, 1);
/*  541 */     IdcSystemLoader.loadSystemUserDatabaseWithFlags(2, 1);
/*      */ 
/*  543 */     DataResultSet queries = MigrateUtils.getTable(env, "QueryTable");
/*  544 */     ws.addQueryDefs(queries);
/*  545 */     return ws;
/*      */   }
/*      */ 
/*      */   protected void prepareFilesystem()
/*      */     throws DataException, ServiceException
/*      */   {
/*  556 */     MigrationEnvironment env = this.m_environment;
/*  557 */     String migrateType = env.m_migrateType.toLowerCase();
/*  558 */     String productName = env.m_productName;
/*      */ 
/*  560 */     env.m_targetHomeDir = FileUtils.directorySlashes(env.m_targetHomeDir);
/*  561 */     env.m_targetBinDir = FileUtils.directorySlashes(env.m_targetBinDir);
/*  562 */     if (env.m_targetIntradocDir == null)
/*      */     {
/*  564 */       env.m_targetIntradocDir = FileUtils.getParent(env.m_targetBinDir);
/*      */     }
/*  566 */     String targetIntradocDir = FileUtils.directorySlashes(env.m_targetIntradocDir);
/*  567 */     env.m_targetIntradocDir = targetIntradocDir;
/*  568 */     env.m_targetBackupDir = FileUtils.directorySlashes(targetIntradocDir + "backup");
/*  569 */     env.m_targetMigrateDir = FileUtils.directorySlashes(targetIntradocDir + migrateType);
/*      */ 
/*  572 */     if (env.m_targetBinDir == null)
/*      */     {
/*  574 */       env.m_targetBinDir = (FileUtils.directorySlashes(env.m_targetDomainDir) + "ucm/" + productName + "/bin/");
/*      */     }
/*  576 */     SystemPropertiesEditor sourceEditor = env.m_sourceSysPropsEditor;
/*  577 */     SystemPropertiesEditor targetEditor = env.m_targetSysPropsEditor = sourceEditor.clone();
/*  578 */     targetEditor.setFilepaths(env.m_targetBinDir + "intradoc.cfg", targetIntradocDir + "config/config.cfg");
/*  579 */     Properties newProps = targetEditor.getIdcProperties();
/*  580 */     newProps.setProperty("IntradocDir", targetIntradocDir);
/*  581 */     if (targetEditor.searchForValue("IdcHomeDir") == null)
/*      */     {
/*  583 */       newProps.setProperty("IdcHomeDir", env.m_targetHomeDir);
/*      */     }
/*      */ 
/*  587 */     env.m_targetEnvironment = createSystemProperties(targetEditor);
/*      */ 
/*  589 */     Properties cfgProps = env.m_targetSysPropsEditor.getCfgProperties();
/*  590 */     cfgProps.setProperty("IdcProductName", "idc" + productName);
/*      */ 
/*  592 */     env.m_targetMigrateStateFilename = (migrateType + "_state.hda");
/*  593 */     env.m_tryRenameBackupFirst = SharedObjects.getEnvValueAsBoolean("TryRenameBackupFirst", true);
/*  594 */     env.m_tryDeleteAfterZip = SharedObjects.getEnvValueAsBoolean("TryDeleteAfterZip", true);
/*  595 */     env.m_shouldAbortIfDeleteAfterZipFails = SharedObjects.getEnvValueAsBoolean("ShouldAbortIfDeleteAfterZipFails", true);
/*      */   }
/*      */ 
/*      */   protected Properties createSystemProperties(SystemPropertiesEditor sysProps)
/*      */     throws ServiceException
/*      */   {
/*  609 */     Properties cfgProps = sysProps.getCfgProperties();
/*  610 */     Properties idcProps = sysProps.getIdcProperties();
/*  611 */     IdcProperties readOnlyProps = new IdcProperties(cfgProps, idcProps);
/*  612 */     IdcProperties props = new IdcProperties(readOnlyProps);
/*      */ 
/*  614 */     ExecutionContext cxt = this.m_environment.m_context;
/*  615 */     String intradocDir = props.getProperty("IntradocDir");
/*  616 */     String[][] keyDirs = LegacyDirectoryLocator.DEFAULTS;
/*  617 */     for (int i = 0; i < keyDirs.length; ++i)
/*      */     {
/*  619 */       String key = keyDirs[i][0];
/*  620 */       String subdir = keyDirs[i][1];
/*  621 */       String dirPath = props.getProperty(key);
/*  622 */       if (dirPath == null)
/*      */       {
/*  624 */         dirPath = PathUtils.substitutePathVariables(subdir, props, null, 0, cxt);
/*  625 */         dirPath = FileUtils.getAbsolutePath(intradocDir, dirPath);
/*      */       }
/*  627 */       dirPath = FileUtils.directorySlashes(dirPath);
/*  628 */       props.setProperty(key, dirPath);
/*      */     }
/*      */ 
/*  631 */     return props;
/*      */   }
/*      */ 
/*      */   public void computeDatabaseItems()
/*      */     throws DataException, ServiceException
/*      */   {
/*  644 */     MigrationEnvironment env = this.m_environment;
/*  645 */     String tableName = env.m_migrateType + "DatabaseTasks";
/*  646 */     DataResultSet tasks = MigrateUtils.getTable(env, tableName);
/*  647 */     Map defaultParams = new HashMap();
/*  648 */     MapUtils.fillMapFromOptionsString(defaultParams, "noItemsTableEntry=1");
/*  649 */     createAndAppendItems(tasks, defaultParams);
/*      */   }
/*      */ 
/*      */   public void computeFilesystemItems()
/*      */     throws DataException, ServiceException
/*      */   {
/*  660 */     MigrationEnvironment env = this.m_environment;
/*  661 */     String tableName = env.m_migrateType + "Tasks";
/*  662 */     DataResultSet tasks = MigrateUtils.getTable(env, tableName);
/*  663 */     createAndAppendItems(tasks, null);
/*      */   }
/*      */ 
/*      */   public void createAndAppendItems(DataResultSet tasks, Map<String, String> defaultParams)
/*      */     throws DataException, ServiceException
/*      */   {
/*  677 */     MigrationEnvironment env = this.m_environment;
/*  678 */     List items = env.m_items;
/*  679 */     if (items == null)
/*      */     {
/*  681 */       env.m_items = (items = new ArrayList(tasks.getNumRows()));
/*      */     }
/*  683 */     String[] tasksColumnNames = { "id", "version", "order", "initMethods", "workMethods", "params", "initScript" };
/*      */ 
/*  687 */     FieldInfo[] tasksColumns = ResultSetUtils.createInfoList(tasks, tasksColumnNames, true);
/*  688 */     ResultSetTreeSort sorter = new ResultSetTreeSort(tasks, tasksColumns[2].m_index, false);
/*  689 */     sorter.m_fieldSortType = 3;
/*  690 */     sorter.sort();
/*  691 */     for (tasks.first(); tasks.isRowPresent(); tasks.next())
/*      */     {
/*  693 */       String id = tasks.getStringValue(tasksColumns[0].m_index);
/*  694 */       String taskVersion = tasks.getStringValue(tasksColumns[1].m_index);
/*  695 */       String initMethodNames = tasks.getStringValue(tasksColumns[3].m_index);
/*  696 */       String workMethodNames = tasks.getStringValue(tasksColumns[4].m_index);
/*  697 */       String paramsString = tasks.getStringValue(tasksColumns[5].m_index);
/*  698 */       String initScript = tasks.getStringValue(tasksColumns[6].m_index);
/*      */ 
/*  700 */       List initMethods = lookupMethodsByName(initMethodNames);
/*  701 */       List workMethods = lookupMethodsByName(workMethodNames);
/*  702 */       List paramsList = StringUtils.parseArray(paramsString, '\n', '\n');
/*  703 */       int numParams = paramsList.size();
/*  704 */       Map params = (defaultParams == null) ? new HashMap() : new HashMap(defaultParams);
/*  705 */       for (int p = 0; p < numParams; ++p)
/*      */       {
/*  707 */         String paramString = (String)paramsList.get(p);
/*  708 */         int equalsIndex = paramString.indexOf(61);
/*      */         String paramValue;
/*      */         String paramValue;
/*  710 */         if (equalsIndex < 0)
/*      */         {
/*  712 */           String paramName = paramString;
/*  713 */           paramValue = "";
/*      */         }
/*      */         else
/*      */         {
/*  717 */           paramName = paramString.substring(0, equalsIndex);
/*  718 */           paramValue = paramString.substring(equalsIndex + 1);
/*      */         }
/*  720 */         String paramName = StringUtils.strip(paramName);
/*  721 */         if (paramName.length() <= 0)
/*      */           continue;
/*  723 */         params.put(paramName, paramValue);
/*      */       }
/*      */ 
/*  727 */       String taskID = id.replaceAll("/", "").replaceAll("[^0-9A-Za-z_]", "_");
/*  728 */       MigrateItem item = new MigrateItem(env, taskID, initMethods, workMethods, params);
/*  729 */       if (taskVersion.length() > 0)
/*      */       {
/*  731 */         item.m_version = taskVersion;
/*      */       }
/*  733 */       String key = MigrateUtils.lcFindKeyForItemWithSuffix(item, "nameKey", "_name");
/*  734 */       item.m_name = MigrateUtils.lcValueForItemWithKey(item, key, taskID);
/*  735 */       String taskSpec = (String)params.get("spec");
/*  736 */       item.m_spec = ((taskSpec != null) ? taskSpec : id);
/*  737 */       String taskUnits = (String)params.get("units");
/*  738 */       item.m_progressUnits = NumberUtils.parseInteger(taskUnits, 0);
/*  739 */       String detailsListString = (String)params.get("detailsList");
/*  740 */       if (detailsListString != null)
/*      */       {
/*  742 */         List list = StringUtils.makeListFromSequenceSimple(detailsListString);
/*  743 */         key = MigrateUtils.lcFindKeyForItemWithSuffix(item, "detailsKey", "_displayElement");
/*  744 */         for (String value : list)
/*      */         {
/*  746 */           MigrateUtils.lcAppendToDetailsListForItemWithKey(item, key, value);
/*      */         }
/*      */       }
/*  749 */       String summaryListString = (String)params.get("summaryList");
/*  750 */       if (summaryListString != null)
/*      */       {
/*  752 */         List list = StringUtils.makeListFromSequenceSimple(summaryListString);
/*  753 */         key = MigrateUtils.lcFindKeyForItemWithSuffix(item, "summaryKey", "_displayElement");
/*  754 */         for (String value : list)
/*      */         {
/*  756 */           MigrateUtils.lcAppendToDetailsListForItemWithKey(item, key, value);
/*      */         }
/*      */       }
/*  759 */       item.m_initScript = initScript;
/*      */ 
/*  761 */       items.add(item);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initItemsForMigrate()
/*      */     throws DataException, ServiceException
/*      */   {
/*  773 */     MigrationEnvironment env = this.m_environment;
/*  774 */     DataBinder binder = env.m_binder;
/*  775 */     List items = env.m_items;
/*  776 */     int totalProgressUnits = 0;
/*  777 */     int numItems = items.size();
/*  778 */     for (int i = 0; i < numItems; ++i)
/*      */     {
/*  780 */       MigrateItem item = (MigrateItem)items.get(i);
/*  781 */       String initScript = item.m_initScript;
/*  782 */       boolean isSkipItem = false;
/*  783 */       if (initScript.length() > 0)
/*      */       {
/*      */         try
/*      */         {
/*  787 */           env.m_merger.evaluateScript(initScript);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  791 */           IdcMessage msg = new IdcMessage("csUnableToEvalScriptForField2", new Object[] { "initScript", initScript });
/*  792 */           Report.info("installer", e, msg);
/*      */         }
/*  794 */         String skipItem = binder.getLocal("skipItem");
/*  795 */         binder.removeLocal("skipItem");
/*  796 */         isSkipItem = StringUtils.convertToBool(skipItem, false);
/*      */       }
/*  798 */       if (!isSkipItem)
/*      */       {
/*  800 */         List methods = item.m_initMethods;
/*  801 */         callMethods(methods, item);
/*  802 */         String skipItem = binder.getLocal("skipItem");
/*  803 */         binder.removeLocal("skipItem");
/*  804 */         isSkipItem = StringUtils.convertToBool(skipItem, false);
/*      */       }
/*  806 */       if (isSkipItem)
/*      */       {
/*  808 */         if (SystemUtils.m_verbose)
/*      */         {
/*  810 */           Report.trace("installer", "skipping item " + item.m_id, null);
/*      */         }
/*  812 */         items.remove(i--);
/*  813 */         --numItems;
/*      */       }
/*      */       else {
/*  816 */         List summaryList = item.m_summaryList;
/*  817 */         if (summaryList.size() < 1)
/*      */         {
/*  819 */           summaryList.add(item.m_name);
/*      */         }
/*  821 */         int itemProgressUnits = item.m_progressUnits;
/*  822 */         if (itemProgressUnits == 0)
/*      */         {
/*  824 */           itemProgressUnits = item.m_detailsList.size();
/*  825 */           if (itemProgressUnits == 0)
/*      */           {
/*  827 */             itemProgressUnits = 1;
/*      */           }
/*  829 */           item.m_progressUnits = itemProgressUnits;
/*      */         }
/*  831 */         totalProgressUnits += itemProgressUnits;
/*      */       }
/*      */     }
/*  833 */     env.m_maximumProgressUnits = totalProgressUnits;
/*      */ 
/*  835 */     for (ReportSubProgress progress : env.m_reportSubProgresses)
/*      */     {
/*  837 */       progress.m_maxProgress = totalProgressUnits;
/*      */     }
/*  839 */     String msg = LocaleResources.getString("csMigrateStarted", env.m_context);
/*  840 */     env.m_reportProgress.reportProgress(4, msg, 0.0F, totalProgressUnits);
/*      */   }
/*      */ 
/*      */   public void doNextMigrateItem()
/*      */     throws DataException, ServiceException
/*      */   {
/*  853 */     MigrationEnvironment env = this.m_environment;
/*      */ 
/*  855 */     int numItems = env.m_items.size();
/*  856 */     int itemNumber = env.m_currentItemIndex;
/*  857 */     if (itemNumber >= numItems)
/*      */     {
/*  859 */       return;
/*      */     }
/*      */ 
/*  862 */     MigrateItem item = (MigrateItem)env.m_items.get(itemNumber);
/*  863 */     boolean alreadyMigrated = false;
/*  864 */     int nameIndex = -1; int versionIndex = -1;
/*  865 */     boolean noItemsTableEntry = StringUtils.convertToBool((String)item.m_params.get("noItemsTableEntry"), false);
/*  866 */     if (!noItemsTableEntry)
/*      */     {
/*  868 */       if (env.m_migrateState == null)
/*      */       {
/*  870 */         loadMigrateState();
/*      */       }
/*      */ 
/*  873 */       DataResultSet itemsTable = env.m_itemsTable;
/*  874 */       FieldInfo[] fields = ResultSetUtils.createInfoList(itemsTable, MIGRATE_STATE_COLUMN_NAMES, true);
/*  875 */       nameIndex = fields[0].m_index;
/*  876 */       versionIndex = fields[1].m_index;
/*      */ 
/*  878 */       itemsTable.last();
/*  879 */       int lastRow = itemsTable.getCurrentRow();
/*  880 */       if (itemsTable.findRow(nameIndex, item.m_id, lastRow, 1) != null)
/*      */       {
/*  882 */         String migratedVersion = itemsTable.getStringValue(versionIndex);
/*  883 */         if ((item.m_version != null) && (item.m_version.length() > 0) && (SystemUtils.compareVersions(migratedVersion, item.m_version) >= 0))
/*      */         {
/*  886 */           alreadyMigrated = true;
/*      */         }
/*      */       }
/*      */     }
/*  890 */     if (!alreadyMigrated)
/*      */     {
/*  892 */       if (item.m_workMethods.size() > 0)
/*      */       {
/*  894 */         MigrateUtils.updateProgress(item, 0, new Object[0]);
/*  895 */         String initScript = item.m_initScript;
/*  896 */         if (initScript.length() > 0)
/*      */         {
/*      */           try
/*      */           {
/*  900 */             env.m_merger.evaluateScript(initScript);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*  904 */             IdcMessage msg = new IdcMessage("csUnableToEvalScriptForField2", new Object[] { "initScript", initScript });
/*  905 */             Report.info("installer", e, msg);
/*      */           }
/*      */         }
/*  908 */         callMethods(item.m_workMethods, item);
/*      */       }
/*  910 */       if (!noItemsTableEntry)
/*      */       {
/*  912 */         if ((item.m_version != null) && (item.m_version.length() > 0))
/*      */         {
/*  914 */           DataResultSet itemsTable = env.m_itemsTable;
/*  915 */           List row = itemsTable.createEmptyRowAsList();
/*  916 */           row.set(nameIndex, item.m_id);
/*  917 */           row.set(versionIndex, item.m_version);
/*  918 */           itemsTable.addRowWithList(row);
/*      */         }
/*  920 */         saveMigrateState();
/*      */       }
/*      */     }
/*  923 */     env.m_currentItemIndex = (itemNumber + 1);
/*      */   }
/*      */ 
/*      */   public void finishMigrate()
/*      */   {
/*  931 */     MigrationEnvironment env = this.m_environment;
/*  932 */     float progress = env.m_maximumProgressUnits;
/*  933 */     String msg = LocaleResources.getString("csMigrateComplete", env.m_context);
/*  934 */     env.m_reportProgress.reportProgress(2, msg, progress, progress);
/*      */   }
/*      */ 
/*      */   protected void loadMigrateState()
/*      */     throws ServiceException
/*      */   {
/*  947 */     MigrationEnvironment env = this.m_environment;
/*  948 */     DataBinder binder = env.m_migrateState = new DataBinder();
/*  949 */     ResourceUtils.serializeDataBinder(env.m_targetMigrateDir, env.m_targetMigrateStateFilename, binder, false, false);
/*  950 */     env.m_itemsTable = ((DataResultSet)binder.getResultSet("MigratedItems"));
/*  951 */     if (env.m_itemsTable == null)
/*      */     {
/*  953 */       env.m_itemsTable = new DataResultSet(MIGRATE_STATE_COLUMN_NAMES);
/*  954 */       binder.addResultSet("MigratedItems", env.m_itemsTable);
/*  955 */       FileUtils.checkOrCreateDirectory(env.m_targetMigrateDir, 0);
/*      */     }
/*  957 */     DataBinder state = env.m_migrateState;
/*  958 */     state.putLocal("date", env.m_startDateString);
/*      */   }
/*      */ 
/*      */   protected void saveMigrateState()
/*      */     throws ServiceException
/*      */   {
/*  968 */     MigrationEnvironment env = this.m_environment;
/*  969 */     ResourceUtils.serializeDataBinder(env.m_targetMigrateDir, env.m_targetMigrateStateFilename, env.m_migrateState, true, false);
/*      */   }
/*      */ 
/*      */   protected List<Method> lookupMethodsByName(String methodNames)
/*      */     throws ServiceException
/*      */   {
/*  981 */     List methodNamesList = StringUtils.parseArray(methodNames, ',', '^');
/*  982 */     int numMethods = methodNamesList.size();
/*  983 */     List methodList = new ArrayList(numMethods);
/*  984 */     for (int m = 0; m < numMethods; ++m)
/*      */     {
/*  986 */       String methodName = (String)methodNamesList.get(m);
/*  987 */       Method method = lookupMethodByName(methodName);
/*  988 */       methodList.add(method);
/*      */     }
/*  990 */     return methodList;
/*      */   }
/*      */ 
/*      */   protected Method lookupMethodByName(String methodName)
/*      */     throws ServiceException
/*      */   {
/* 1003 */     int lastDot = methodName.lastIndexOf(46);
/*      */     String className;
/*      */     String className;
/* 1004 */     if (lastDot < 0)
/*      */     {
/* 1006 */       className = "intradoc.apputilities.installer.CoreMigrateHandler";
/*      */     }
/*      */     else
/*      */     {
/* 1010 */       className = methodName.substring(0, lastDot);
/* 1011 */       methodName = methodName.substring(lastDot + 1);
/*      */     }
/* 1013 */     Class[] argTypes = { MigrateItem.class };
/*      */     Class cl;
/*      */     try
/*      */     {
/* 1017 */       cl = Class.forName(className);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */       try
/*      */       {
/* 1023 */         className = "intradoc.apputilities.installer." + className;
/* 1024 */         cl = Class.forName(className);
/*      */       }
/*      */       catch (Exception e2)
/*      */       {
/* 1028 */         throw new ServiceException(e, null);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/* 1033 */       Method m = cl.getMethod(methodName, argTypes);
/* 1034 */       return m;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1038 */       IdcMessage msg = new IdcMessage("syClassHelperMethodDoesNotExist", new Object[] { methodName, className });
/* 1039 */       throw new ServiceException(e, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void callMethods(List<Method> methods, MigrateItem item)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1053 */     if (methods == null)
/*      */     {
/* 1055 */       return;
/*      */     }
/* 1057 */     Object[] args = { item };
/* 1058 */     for (int m = 0; m < methods.size(); ++m)
/*      */     {
/* 1060 */       Method method = (Method)methods.get(m);
/* 1061 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1063 */         String methodName = method.getName();
/* 1064 */         String msg = "calling method " + methodName + "(" + item.m_id + ")";
/* 1065 */         Report.trace("installer", msg, null);
/*      */       }
/*      */       try
/*      */       {
/* 1069 */         method.invoke(null, args);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1073 */         if (t instanceof InvocationTargetException)
/*      */         {
/* 1075 */           InvocationTargetException ite = (InvocationTargetException)t;
/* 1076 */           Throwable target = ite.getTargetException();
/* 1077 */           if (target != null)
/*      */           {
/* 1079 */             t = target;
/*      */           }
/*      */         }
/* 1082 */         if (t instanceof DataException)
/*      */         {
/* 1084 */           throw ((DataException)t);
/*      */         }
/* 1086 */         if (t instanceof ServiceException)
/*      */         {
/* 1088 */           throw ((ServiceException)t);
/*      */         }
/* 1090 */         throw new ServiceException(t);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1098 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103336 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.Migrator
 * JD-Core Version:    0.5.4
 */