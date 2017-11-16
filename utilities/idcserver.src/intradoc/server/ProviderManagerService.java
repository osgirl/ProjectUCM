/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.HashVector;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderInterface;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*     */ import intradoc.server.proxy.ProviderFileUtils;
/*     */ import intradoc.server.proxy.ProviderStateUtils;
/*     */ import intradoc.server.proxy.ProviderUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProviderManagerService extends Service
/*     */ {
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  42 */     super.createHandlersForService();
/*  43 */     createHandlers("ProviderManagerService");
/*     */   }
/*     */ 
/*     */   public void requestSecurityInfo() throws DataException, ServiceException
/*     */   {
/*  48 */     DataBinder securityData = (DataBinder)AppObjectRepository.getObject("SecurityInfo");
/*  49 */     if (securityData == null)
/*     */     {
/*  52 */       String userCacheDir = DirectoryLocator.getUserPublishCacheDir();
/*  53 */       String securityInfoName = "SecurityInfo.hda";
/*     */ 
/*  55 */       securityData = ResourceUtils.readDataBinder(userCacheDir, securityInfoName);
/*     */     }
/*     */ 
/*  58 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/*  61 */       securityData.sendEx(sw, false);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  65 */       createServiceException(e, "!csUnableToSendSecurityInfo");
/*     */     }
/*     */ 
/*  68 */     String str = sw.toStringRelease();
/*  69 */     if (str.length() == 0)
/*     */     {
/*  71 */       createServiceException(null, "!csReadSecurityUnknownError");
/*     */     }
/*     */ 
/*  74 */     this.m_binder.putLocal("SecurityInfo", str);
/*  75 */     this.m_binder.putLocal("users:counter", String.valueOf(SubjectManager.getCounter("users")));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyChange() throws DataException, ServiceException
/*     */   {
/*  81 */     OutgoingProviderMonitor.requestRefresh(this.m_binder, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void forceRefresh() throws DataException, ServiceException
/*     */   {
/*  87 */     OutgoingProviderMonitor.forceRefresh(this.m_binder, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void testProvider() throws DataException, ServiceException
/*     */   {
/*  93 */     String name = this.m_binder.getLocal("pName");
/*  94 */     if (name == null)
/*     */     {
/*  96 */       throw new DataException("csNameNotSpecified");
/*     */     }
/*  98 */     Provider provider = Providers.getProvider(name);
/*  99 */     if (provider == null)
/*     */     {
/* 101 */       provider = Providers.getFromAllProviders(name);
/* 102 */       String msg = null;
/* 103 */       if (provider == null)
/*     */       {
/* 105 */         msg = LocaleUtils.encodeMessage("csProviderNotConfigured", null, name);
/*     */       }
/*     */       else
/*     */       {
/* 109 */         boolean isEnabled = provider.isEnabled();
/* 110 */         boolean isInError = provider.isInError();
/* 111 */         if (!isEnabled)
/*     */         {
/* 113 */           msg = "!csProviderDisabled";
/*     */         }
/* 115 */         else if (isInError)
/*     */         {
/* 117 */           msg = "!csProviderError";
/*     */         }
/*     */         else
/*     */         {
/* 121 */           msg = "!csProviderUnavailable";
/*     */         }
/*     */       }
/* 124 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 127 */     ProviderStateUtils.testConnection(provider);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveNonSystemDatabaseProviderList()
/*     */   {
/* 133 */     String[] cols = { "ProviderName", "ProviderDescription", "ConnectionState" };
/* 134 */     DataResultSet drset = new DataResultSet(cols);
/* 135 */     drset.setDateFormat(LocaleResources.m_odbcFormat);
/*     */ 
/* 138 */     Vector providers = Providers.getProvidersOfType("database");
/* 139 */     int num = providers.size();
/* 140 */     int numColumns = cols.length;
/* 141 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 144 */       Provider provider = (Provider)providers.elementAt(i);
/* 145 */       provider.pollConnectionState();
/* 146 */       DataBinder provData = provider.getProviderData();
/*     */ 
/* 148 */       Properties props = new Properties();
/* 149 */       buildProviderInfo(props, provData, provider.getProviderState());
/* 150 */       if (props.getProperty("ProviderName").equalsIgnoreCase("SystemDatabase")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 154 */       Vector row = new IdcVector(numColumns);
/* 155 */       for (int j = 0; j < numColumns; ++j)
/*     */       {
/* 157 */         String value = props.getProperty(cols[j]);
/* 158 */         if (value == null)
/*     */         {
/* 160 */           value = "";
/*     */         }
/* 162 */         row.addElement(value);
/*     */       }
/* 164 */       drset.addRow(row);
/*     */     }
/* 166 */     setConnectionLocaleStrings();
/*     */ 
/* 168 */     this.m_binder.addResultSet("DatabaseProviders", drset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveAllProviderInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/* 175 */     DataResultSet typeSet = SharedObjects.getTable("ProviderTypes");
/* 176 */     this.m_binder.addResultSet("ProviderTypes", typeSet);
/*     */ 
/* 179 */     HashVector columns = new HashVector();
/* 180 */     String[] requiredColumns = { "ProviderName", "ProviderDescription", "ProviderType", "ConnectionState", "IsBadConnection", "IsNew", "LastActivityTs", "pInfoService", "pAddService", "IsEnabled" };
/*     */ 
/* 183 */     int len = requiredColumns.length;
/* 184 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 186 */       columns.addValue(requiredColumns[i]);
/*     */     }
/*     */ 
/* 190 */     Vector provInfo = new IdcVector();
/* 191 */     Vector providers = Providers.getAllProviderList();
/* 192 */     int num = providers.size();
/*     */     Enumeration en;
/* 193 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 195 */       Provider provider = (Provider)providers.elementAt(i);
/* 196 */       provider.pollConnectionState();
/* 197 */       DataBinder provData = provider.getProviderData();
/*     */ 
/* 199 */       Properties props = new Properties();
/* 200 */       buildProviderInfo(props, provData, provider.getProviderState());
/*     */ 
/* 202 */       String type = props.getProperty("ProviderType");
/* 203 */       if (type != null)
/*     */       {
/* 205 */         Vector row = typeSet.findRow(0, type);
/* 206 */         if (row != null)
/*     */         {
/* 208 */           Properties typeProps = typeSet.getCurrentRowProps();
/* 209 */           DataBinder.mergeHashTables(props, typeProps);
/*     */         }
/*     */       }
/*     */ 
/* 213 */       provInfo.addElement(props);
/* 214 */       for (en = props.keys(); en.hasMoreElements(); )
/*     */       {
/* 216 */         String key = (String)en.nextElement();
/* 217 */         columns.addValue(key);
/*     */       }
/*     */     }
/*     */ 
/* 221 */     String[] cols = StringUtils.convertListToArray(columns.m_values);
/* 222 */     DataResultSet drset = new DataResultSet(cols);
/* 223 */     drset.setDateFormat(LocaleResources.m_odbcFormat);
/*     */ 
/* 225 */     num = provInfo.size();
/* 226 */     int numColumns = cols.length;
/* 227 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 229 */       Properties props = (Properties)provInfo.elementAt(i);
/* 230 */       Vector row = new IdcVector(numColumns);
/* 231 */       for (int j = 0; j < numColumns; ++j)
/*     */       {
/* 233 */         String value = props.getProperty(cols[j]);
/* 234 */         if (value == null)
/*     */         {
/* 236 */           value = "";
/*     */         }
/* 238 */         row.addElement(value);
/*     */       }
/* 240 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 243 */     setConnectionLocaleStrings();
/*     */ 
/* 245 */     this.m_binder.addResultSet("Providers", drset);
/*     */   }
/*     */ 
/*     */   protected void buildProviderInfo(Properties props, DataBinder provData, Properties provState)
/*     */   {
/* 252 */     DataBinder.mergeHashTables(props, (Properties)provData.getLocalData().clone());
/* 253 */     DataBinder.mergeHashTables(props, (Properties)provState.clone());
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveProviderInfo() throws DataException, ServiceException
/*     */   {
/* 259 */     String name = this.m_binder.getLocal("pName");
/* 260 */     if (name == null)
/*     */     {
/* 262 */       throw new DataException("!csProviderNameRequired");
/*     */     }
/* 264 */     Provider provider = Providers.getFromAllProviders(name);
/* 265 */     if (provider == null)
/*     */     {
/* 267 */       createServiceException(null, "!csProviderDoesNotExist");
/*     */     }
/*     */ 
/* 270 */     Properties localData = this.m_binder.getLocalData();
/* 271 */     buildProviderInfo(localData, provider.getProviderData(), provider.getProviderState());
/*     */ 
/* 273 */     boolean isSystemProvider = provider.isSystemProvider();
/* 274 */     this.m_binder.putLocal("IsSystemProvider", String.valueOf(isSystemProvider));
/*     */ 
/* 276 */     setConnectionLocaleStrings();
/*     */ 
/* 278 */     String type = this.m_binder.getLocal("ProviderType");
/* 279 */     if (type == null)
/*     */     {
/* 281 */       createServiceException(null, "!csProviderBadConfig");
/*     */     }
/* 283 */     if (type.equals("outgoing"))
/*     */     {
/* 285 */       retrieveOutgoingProviderInfo();
/*     */     }
/*     */ 
/* 288 */     PluginFilters.filter("retrieveProviderInfo", this.m_workspace, this.m_binder, this);
/*     */ 
/* 290 */     fixupPasswordFields(this.m_binder);
/*     */   }
/*     */ 
/*     */   protected void setConnectionLocaleStrings()
/*     */   {
/* 295 */     this.m_binder.setFieldType("ConnectionState", "message");
/* 296 */     this.m_binder.setFieldType("LastConnectionErrorMsg", "message");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void retrieveOutgoingProviderInfo()
/*     */     throws DataException
/*     */   {
/* 307 */     boolean isProxiedServer = StringUtils.convertToBool(this.m_binder.getLocal("IsProxiedServer"), false);
/* 308 */     boolean isNotifyTarget = StringUtils.convertToBool(this.m_binder.getLocal("IsNotifyTarget"), false);
/* 309 */     if (isProxiedServer)
/*     */     {
/* 311 */       String[] columns = { "subject", "isNotified", "lastNotifiedTs", "lastRefreshTs" };
/* 312 */       String str = this.m_binder.getLocal("NotifiedSubjects");
/* 313 */       DataResultSet drset = createSubjectSet(str, columns, this.m_binder.getLocalData());
/* 314 */       this.m_binder.addResultSet("NotifiedSubjects", drset);
/*     */     }
/*     */ 
/* 317 */     if (!isNotifyTarget)
/*     */       return;
/* 319 */     String[] columns = { "subject", "isNotify", "lastNotifyTs", "lastNotifySuccessTs" };
/* 320 */     String str = this.m_binder.getLocal("NotifySubjects");
/* 321 */     DataResultSet drset = createSubjectSet(str, columns, this.m_binder.getLocalData());
/* 322 */     this.m_binder.addResultSet("NotifySubjects", drset);
/*     */   }
/*     */ 
/*     */   protected DataResultSet createSubjectSet(String subjectStr, String[] columns, Properties provState)
/*     */   {
/* 328 */     DataResultSet drset = new DataResultSet(columns);
/*     */ 
/* 330 */     Vector subjects = StringUtils.parseArray(subjectStr, ',', ',');
/* 331 */     int num = subjects.size();
/* 332 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 334 */       String subject = (String)subjects.elementAt(i);
/*     */ 
/* 336 */       Vector row = new IdcVector();
/* 337 */       row.addElement(subject);
/* 338 */       for (int j = 1; j < columns.length; ++j)
/*     */       {
/* 340 */         String value = provState.getProperty(subject + ":" + columns[j]);
/* 341 */         if (value == null)
/*     */         {
/* 343 */           value = "";
/*     */         }
/* 345 */         row.addElement(value);
/*     */       }
/* 347 */       drset.addRow(row);
/*     */     }
/* 349 */     return drset;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void createAddEditProviderForm()
/*     */     throws DataException, ServiceException
/*     */   {
/* 356 */     String type = this.m_binder.getLocal("pType");
/* 357 */     if (type == null)
/*     */     {
/* 359 */       throw new DataException("!csProviderUnableToAdd");
/*     */     }
/*     */ 
/* 362 */     DataResultSet typeSet = SharedObjects.getTable("ProviderTypes");
/*     */ 
/* 364 */     Vector row = typeSet.findRow(0, type);
/* 365 */     if (row == null)
/*     */     {
/* 368 */       String msg = LocaleUtils.encodeMessage("csProviderTypeInvalid", null, type);
/*     */ 
/* 370 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 373 */     Properties props = typeSet.getCurrentRowProps();
/* 374 */     String aeTemplate = props.getProperty("pAddEditTemplate");
/* 375 */     this.m_binder.putLocal("ResourceTemplate", aeTemplate);
/*     */ 
/* 377 */     Properties localData = this.m_binder.getLocalData();
/* 378 */     DataBinder.mergeHashTables(localData, props);
/*     */ 
/* 380 */     prepareProviderData(type);
/*     */ 
/* 382 */     boolean isEdit = StringUtils.convertToBool(this.m_binder.getLocal("isEdit"), false);
/* 383 */     if (!isEdit) {
/*     */       return;
/*     */     }
/* 386 */     String name = this.m_binder.getLocal("pName");
/* 387 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 389 */       throw new DataException("!csProviderNameRequired");
/*     */     }
/* 391 */     Provider provider = Providers.getFromAllProviders(name);
/* 392 */     if (provider == null)
/*     */     {
/* 394 */       String msg = LocaleUtils.encodeMessage("csProviderDoesNotExist2", null, name);
/*     */ 
/* 396 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 399 */     buildProviderInfo(localData, Providers.getProviderData(name), provider.getProviderState());
/*     */ 
/* 401 */     setCachedObject(name, provider);
/* 402 */     boolean isStandard = DataBinderUtils.getBoolean(this.m_binder, "IsAllowConfigSystemProvider", false);
/* 403 */     if (!isStandard)
/*     */     {
/* 405 */       executeFilter("createAddEditProviderForm");
/* 406 */       DataBinder provData = ProviderFileUtils.readProviderFile(name, true);
/* 407 */       this.m_binder.merge(provData);
/*     */     }
/*     */ 
/* 410 */     fixupPasswordFields(this.m_binder);
/*     */   }
/*     */ 
/*     */   protected void prepareProviderData(String type)
/*     */     throws DataException
/*     */   {
/* 416 */     if (!type.equals("database"))
/*     */     {
/* 418 */       return;
/*     */     }
/*     */ 
/* 421 */     DataResultSet drset = new DataResultSet(new String[] { "dbName", "driver", "connStr", "testQuery" });
/* 422 */     String curDBType = SharedObjects.getEnvironmentValue("SystemDatabaseName");
/* 423 */     String databaseList = getDatabaseConfigValue(curDBType, "ListableDatabases");
/*     */ 
/* 425 */     List list = StringUtils.makeListFromSequence(databaseList, ',', '^', 0);
/* 426 */     for (String db : list)
/*     */     {
/* 428 */       Vector row = new IdcVector();
/* 429 */       String driver = getDatabaseConfigValue(db, "DefaultJdbcDriver");
/* 430 */       String connStr = getDatabaseConfigValue(db, "DefaultJdbcConnectionString");
/* 431 */       String testQuery = getDatabaseConfigValue(db, "TestQuery");
/* 432 */       row.addElement(db);
/* 433 */       row.addElement(driver);
/* 434 */       row.addElement(connStr);
/* 435 */       row.addElement(testQuery);
/* 436 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 439 */     this.m_binder.addResultSet("DatabaseDefaults", drset);
/*     */   }
/*     */ 
/*     */   protected String getDatabaseConfigValue(String type, String key) throws DataException
/*     */   {
/* 444 */     if ((type == null) || (key == null))
/*     */     {
/* 446 */       return null;
/*     */     }
/* 448 */     type = type.toUpperCase();
/* 449 */     DataResultSet drset = SharedObjects.getTable("DatabaseConnectionConfigurations");
/* 450 */     String value = ResultSetUtils.findValue(drset, "dccKey", "(" + type + ")" + key, "dccValue");
/* 451 */     if (value == null)
/*     */     {
/* 453 */       value = ResultSetUtils.findValue(drset, "dccKey", key, "dccValue");
/*     */     }
/* 455 */     return value;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addOrEditProvider() throws DataException, ServiceException
/*     */   {
/* 461 */     ProviderUtils.addOrEditProvider(true, null, this.m_binder, this.m_workspace, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void enableDisableProvider()
/*     */     throws DataException, ServiceException
/*     */   {
/* 469 */     String name = this.m_binder.getLocal("pName");
/* 470 */     boolean isEnable = StringUtils.convertToBool(this.m_binder.getLocal("IsEnabled"), true);
/* 471 */     if (!isEnable)
/*     */     {
/* 474 */       Providers.enableProvider(name, isEnable);
/*     */     }
/*     */ 
/* 478 */     String[][] keysToChange = { { "IsEnabled", "bool" } };
/*     */ 
/* 482 */     Vector keys = new IdcVector();
/* 483 */     for (int i = 0; i < keysToChange.length; ++i)
/*     */     {
/* 485 */       keys.addElement(keysToChange[i]);
/*     */     }
/* 487 */     ProviderUtils.addOrEditProvider(false, keys, this.m_binder, this.m_workspace, this);
/*     */ 
/* 489 */     if (!isEnable) {
/*     */       return;
/*     */     }
/* 492 */     Providers.enableProvider(name, isEnable);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteProvider()
/*     */     throws DataException, ServiceException
/*     */   {
/* 499 */     String pName = this.m_binder.get("pName").toLowerCase();
/* 500 */     if (Providers.isReservedName(pName))
/*     */     {
/* 502 */       String msg = LocaleUtils.encodeMessage("csProviderUnableToDelete", null, pName);
/*     */ 
/* 504 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 507 */     String dir = DirectoryLocator.getProviderDirectory();
/* 508 */     FileUtils.reserveDirectory(dir, true);
/*     */     try
/*     */     {
/* 511 */       DataBinder binder = ProviderFileUtils.checkAndLoadProviders();
/* 512 */       DataResultSet drset = (DataResultSet)binder.getResultSet("Providers");
/* 513 */       Vector row = drset.findRow(0, pName);
/*     */ 
/* 515 */       if (row != null)
/*     */       {
/* 517 */         drset.deleteCurrentRow();
/* 518 */         ProviderFileUtils.writeProvidersFile(binder, false);
/*     */       }
/*     */ 
/* 522 */       String dirDateFormat = "yy-MMM-dd_HH.mm.ss";
/* 523 */       SimpleDateFormat dirFrmt = new SimpleDateFormat(dirDateFormat);
/* 524 */       dirFrmt.setTimeZone(TimeZone.getDefault());
/* 525 */       String tsDir = dirFrmt.format(new Date()).toLowerCase();
/*     */ 
/* 527 */       File fromDir = FileUtilsCfgBuilder.getCfgFile(dir + pName, "Provider", true);
/* 528 */       File toDir = FileUtilsCfgBuilder.getCfgFile(dir + pName + "_old_" + tsDir, "Provider", true);
/* 529 */       FileUtils.copyDirectoryWithFlags(fromDir, toDir, 1, null, 1);
/*     */ 
/* 532 */       FileUtils.deleteDirectory(fromDir, true);
/*     */     }
/*     */     finally
/*     */     {
/* 536 */       FileUtils.releaseDirectory(dir, true);
/*     */     }
/*     */ 
/* 540 */     Providers.removeProvider(pName);
/*     */   }
/*     */ 
/*     */   public void fixupPasswordFields(DataBinder provData)
/*     */     throws DataException
/*     */   {
/* 548 */     Properties props = (Properties)provData.getLocalData().clone();
/* 549 */     Set set = props.keySet();
/*     */ 
/* 551 */     for (Iterator i$ = set.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 553 */       String k = (String)key;
/* 554 */       String[] encKey = new String[1];
/* 555 */       boolean isPasswordField = CryptoPasswordUtils.isPasswordField(k, encKey);
/* 556 */       if (!isPasswordField) {
/*     */         continue;
/*     */       }
/*     */ 
/* 560 */       provData.putLocal(k, "*****");
/* 561 */       provData.putLocal(encKey[0], "ClearText"); }
/*     */ 
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void appendDatabaseAuditMessage()
/*     */   {
/* 568 */     String dataMsg = null;
/* 569 */     if (!this.m_workspace instanceof ProviderInterface)
/*     */       return;
/* 571 */     ProviderInterface dbProvider = (ProviderInterface)this.m_workspace;
/* 572 */     dataMsg = dbProvider.getReportString("audit");
/* 573 */     if (dataMsg == null)
/*     */       return;
/* 575 */     if (dataMsg.indexOf("@Properties LocalData") < 0)
/*     */     {
/* 577 */       this.m_binder.putLocal("sysdb:auditReport", dataMsg);
/*     */     }
/*     */     else
/*     */     {
/* 581 */       BufferedReader reader = new BufferedReader(new StringReader(dataMsg));
/* 582 */       DataBinder data = new DataBinder();
/*     */       try
/*     */       {
/* 585 */         data.receive(reader);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 589 */         Report.trace("system", null, e);
/*     */       }
/*     */ 
/* 593 */       DataBinder.mergeHashTables(this.m_binder.getFieldTypes(), data.getFieldTypes());
/*     */ 
/* 596 */       Properties localData = data.getLocalData();
/* 597 */       Enumeration keys = localData.keys();
/* 598 */       while (keys.hasMoreElements())
/*     */       {
/* 600 */         String key = (String)keys.nextElement();
/* 601 */         if ((!key.startsWith("blF")) && (!key.startsWith("blD")))
/*     */         {
/* 603 */           String value = localData.getProperty(key);
/* 604 */           this.m_binder.putLocal("sysdb:" + key, value);
/*     */         }
/*     */       }
/*     */ 
/* 608 */       Enumeration rsKeys = data.getResultSetList();
/* 609 */       while (rsKeys.hasMoreElements())
/*     */       {
/* 611 */         String key = (String)rsKeys.nextElement();
/* 612 */         DataResultSet drset = (DataResultSet)data.getResultSet(key);
/* 613 */         this.m_binder.addResultSet("sysdb:" + key, drset);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 622 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104839 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProviderManagerService
 * JD-Core Version:    0.5.4
 */