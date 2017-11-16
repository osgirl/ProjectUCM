/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.DateUtils;
/*      */ import intradoc.common.DynamicData;
/*      */ import intradoc.common.DynamicDataParser;
/*      */ import intradoc.common.DynamicHtmlStatic;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FeaturesInterface;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcLocalizationStrings;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Log;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ResourceObject;
/*      */ import intradoc.common.ResourceTrace;
/*      */ import intradoc.common.ScriptContext;
/*      */ import intradoc.common.ScriptExtensions;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.configpage.ConfigPageUtils;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DatabaseTypes;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.FieldInfoUtils;
/*      */ import intradoc.data.IdcCounterUtils;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.filestore.FileStoreProviderLoader;
/*      */ import intradoc.indexer.IndexerConfig;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.refinery.RefineryUtils;
/*      */ import intradoc.resource.ComponentData;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.resource.TableMergeRule;
/*      */ import intradoc.search.SearchCache;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.archive.ArchiveUtils;
/*      */ import intradoc.server.archive.ArchiverMonitor;
/*      */ import intradoc.server.archive.ReplicationData;
/*      */ import intradoc.server.filter.WebFilterConfigUtils;
/*      */ import intradoc.server.jobs.ScheduledJobManager;
/*      */ import intradoc.server.project.Projects;
/*      */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*      */ import intradoc.server.proxy.ProviderFileUtils;
/*      */ import intradoc.server.publish.WebPublishUtils;
/*      */ import intradoc.server.publish.WebPublisher;
/*      */ import intradoc.server.script.ScriptExtensionUtils;
/*      */ import intradoc.server.script.ServerScriptExtensions;
/*      */ import intradoc.server.subject.ArchiveCollectionsSubjectCallback;
/*      */ import intradoc.server.subject.CollaborationsSubjectCallback;
/*      */ import intradoc.server.subject.DocClassSubjectCallback;
/*      */ import intradoc.server.subject.DynamicQueriesSubjectCallback;
/*      */ import intradoc.server.subject.MetaDataSubjectCallback;
/*      */ import intradoc.server.subject.RegisteredProjectsSubjectCallback;
/*      */ import intradoc.server.subject.ReportsSubjectCallback;
/*      */ import intradoc.server.subject.SchemaSubjectCallback;
/*      */ import intradoc.server.subject.SearchApiSubjectCallback;
/*      */ import intradoc.server.subject.SearchConfigSubjectCallback;
/*      */ import intradoc.server.subject.TemplatesSubjectCallback;
/*      */ import intradoc.server.utils.ComponentInstaller;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.server.utils.ComponentUninstallHelper;
/*      */ import intradoc.server.utils.CustomSecurityRightsUtils;
/*      */ import intradoc.server.workflow.WfCompanionManager;
/*      */ import intradoc.server.workflow.WfDesignManager;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.CustomSecurityRightsData;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.LocaleLoader;
/*      */ import intradoc.shared.LogInfo;
/*      */ import intradoc.shared.MetaFieldUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.ResourceDataParser;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.SharedPageMergerData;
/*      */ import intradoc.shared.SharedUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.util.MapUtils;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.net.ServerSocket;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Observable;
/*      */ import java.util.Observer;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class IdcSystemLoader
/*      */ {
/*  166 */   public static IdcExtendedLoader m_extendedLoader = null;
/*      */ 
/*  169 */   protected static ExecutionContext m_cxt = new ExecutionContextAdaptor();
/*      */ 
/*  171 */   public static ReportProgress m_progress = new IdcSystemLoaderReportProgress(m_cxt);
/*      */ 
/*  175 */   static boolean m_isInitArchive = false;
/*  176 */   static boolean m_isLoadPageBuilderConfig = false;
/*  177 */   static boolean m_isInitSearchIndexer = false;
/*  178 */   static boolean m_isInitWorkflow = false;
/*      */   public static final int STANDARD_PROVIDERS = 0;
/*      */   public static final int CREATE_INCOMING_PROVIDER = 1;
/*      */   public static final int NO_SYSTEM_PROVIDERS = 2;
/*      */   public static final int F_SKIP_JDBC_FEATURE_CHECK = 1;
/*      */   protected static boolean m_resourcesAlreadyLoaded;
/*      */ 
/*      */   public static ScheduledSystemEvents getOrCreateScheduledSystemEvents(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/*  194 */     Object obj = SharedObjects.getObject("globalObjects", "ScheduledSystemEvents");
/*  195 */     ScheduledSystemEvents sysEvents = null;
/*  196 */     if ((obj == null) || (!obj instanceof ScheduledSystemEvents))
/*      */     {
/*  198 */       sysEvents = (ScheduledSystemEvents)ComponentClassFactory.createClassInstance("ScheduledSystemEvents", "intradoc.server.ScheduledSystemEvents", "!csCannotCreateHandlerForSystemEvents");
/*      */ 
/*  200 */       DataResultSet events = SharedObjects.getTable("IdcScheduledSystemEvents");
/*  201 */       SharedObjects.putObject("globalObjects", "ScheduledSystemEvents", sysEvents);
/*      */ 
/*  204 */       sysEvents.init(events, workspace, m_cxt);
/*      */     }
/*      */     else
/*      */     {
/*  208 */       sysEvents = (ScheduledSystemEvents)obj;
/*      */     }
/*      */ 
/*  211 */     if (workspace != null)
/*      */     {
/*  213 */       ScheduledJobManager.initProcessing(workspace);
/*      */     }
/*  215 */     return sysEvents;
/*      */   }
/*      */ 
/*      */   public static PeriodicTasks getOrCreatePeriodicTasks() throws ServiceException
/*      */   {
/*  220 */     Object obj = SharedObjects.getObject("globalObjects", "PeriodicTasks");
/*  221 */     PeriodicTasks periodicTasks = null;
/*  222 */     if ((obj == null) || (!obj instanceof PeriodicTasks))
/*      */     {
/*  224 */       periodicTasks = (PeriodicTasks)ComponentClassFactory.createClassInstance("PeriodicTasks", "intradoc.server.PeriodicTasks", "!csCannotCreateHandlerForPeriodicTasks");
/*      */ 
/*  226 */       SharedObjects.putObject("globalObjects", "PeriodicTasks", periodicTasks);
/*  227 */       periodicTasks.init();
/*      */     }
/*      */     else
/*      */     {
/*  231 */       periodicTasks = (PeriodicTasks)obj;
/*      */     }
/*      */ 
/*  234 */     return periodicTasks;
/*      */   }
/*      */ 
/*      */   public static void setExtendedLoader(IdcExtendedLoader loader)
/*      */   {
/*  239 */     m_extendedLoader = loader;
/*      */   }
/*      */ 
/*      */   public static IdcExtendedLoader getExtendedLoader()
/*      */   {
/*  244 */     return m_extendedLoader;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadInitialConfig()
/*      */     throws DataException
/*      */   {
/*  256 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.loadInitialConfig has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  258 */     IdcSystemConfig.loadInitialConfig();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadAppConfigInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  267 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.loadAppConfigInfo has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  269 */     IdcSystemConfig.loadAppConfigInfo();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void initLocalization()
/*      */     throws DataException, ServiceException
/*      */   {
/*  278 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.initLocalization has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  280 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void configLocalization()
/*      */     throws DataException, ServiceException
/*      */   {
/*  289 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.configLocalization has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  291 */     IdcSystemConfig.configLocalization();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadSystemEncodingInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  300 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.loadSystemEncodingInfo has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  302 */     IdcSystemConfig.loadSystemEncodingInfo();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void loadEncodingMap(String resDir)
/*      */     throws ServiceException, DataException
/*      */   {
/*  311 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.loadEncodingMap has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  313 */     IdcSystemConfig.loadEncodingMap(resDir);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void initIdcName(Properties props, boolean issueWarnings)
/*      */   {
/*  322 */     SystemUtils.reportDeprecatedUsage("IdcSystemLoader.initIdcName has moved to IdcSystemConfig, please update your code.");
/*      */ 
/*  324 */     SharedLoader.initIdcName(props, (issueWarnings) ? 1 : 0);
/*      */   }
/*      */ 
/*      */   public static void init(boolean hasSocketProvider)
/*      */     throws DataException, ServiceException
/*      */   {
/*  331 */     IntervalData interval = new IntervalData("systemload");
/*  332 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/*  333 */     interval.traceAndRestart("startup", "initLocalization()");
/*  334 */     IdcSystemConfig.configLocalization();
/*  335 */     interval.traceAndRestart("startup", "configLocalization()");
/*  336 */     finishInit(hasSocketProvider);
/*  337 */     interval.traceAndRestart("startup", "finishInit()");
/*      */   }
/*      */ 
/*      */   public static void finishInit(boolean hasSocketProvider)
/*      */     throws DataException, ServiceException
/*      */   {
/*  344 */     IntervalData interval = new IntervalData("finishInit");
/*  345 */     initLogInfo();
/*  346 */     interval.traceAndRestart("startup", "initLogInfo()");
/*      */ 
/*  348 */     IdcInstallInfo installInfo = (IdcInstallInfo)SharedObjects.getObject("InstallInfo", "IdcInstallInfo");
/*      */ 
/*  350 */     if (installInfo != null)
/*      */     {
/*  354 */       installInfo.buildComponentList();
/*      */     }
/*      */ 
/*  357 */     for (String key : IdcInstallInfo.CAPTURED_APP_SERVER_PROPERTIES)
/*      */     {
/*  359 */       SharedObjects.addSecureEnvironmentKey(key);
/*      */     }
/*      */ 
/*  362 */     m_extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csCustomInitializerConstructionError");
/*      */ 
/*  367 */     ComponentUninstallHelper cmpUninstall = new ComponentUninstallHelper();
/*  368 */     cmpUninstall.doUninstallComponentCleanup();
/*      */ 
/*  371 */     ComponentLoader.m_quiet = (!hasSocketProvider) && (!EnvUtils.isHostedInAppServer());
/*  372 */     initComponentData();
/*  373 */     interval.traceAndRestart("startup", "initComponentData()");
/*  374 */     String failOnError = SharedObjects.getEnvironmentValue("FailOnComponentLoadError");
/*      */ 
/*  376 */     boolean failOnErrorBoolean = StringUtils.convertToBool(failOnError, false);
/*  377 */     if ((failOnErrorBoolean) || ((hasSocketProvider) && (failOnError == null)))
/*      */     {
/*  380 */       Hashtable errors = (Hashtable)SharedObjects.getObject("Errors", "ComponentLoader");
/*      */ 
/*  382 */       if ((errors != null) && (errors.size() > 0))
/*      */       {
/*  384 */         Enumeration en = errors.keys();
/*  385 */         String componentName = (String)en.nextElement();
/*  386 */         DataException anError = (DataException)errors.get(componentName);
/*      */ 
/*  389 */         throw new ServiceException(anError);
/*      */       }
/*      */     }
/*  392 */     interval.traceAndRestart("startup", "componentloading");
/*  393 */     loadComponentDataEx(true);
/*      */ 
/*  395 */     if (installInfo != null)
/*      */     {
/*  399 */       installInfo.finishInstall();
/*      */     }
/*      */ 
/*  402 */     managePasswords();
/*      */ 
/*  404 */     interval.traceAndRestart("startup", "loadComponentDataEx()");
/*  405 */     int flags = (hasSocketProvider) ? 1 : 0;
/*  406 */     initProviders(flags);
/*  407 */     interval.traceAndRestart("startup", "initProviders()");
/*      */ 
/*  409 */     SharedLoader.configureComponentTracing();
/*  410 */     interval.traceAndRestart("startup", "configureComponentTracing()");
/*      */ 
/*  412 */     loadServerProcessID();
/*  413 */     interval.traceAndRestart("startup", "loadServerProcessID");
/*      */ 
/*  416 */     DataResultSet secureKeys = SharedObjects.getTable("SecureEnvironmentKeys");
/*  417 */     int keyIndex = ResultSetUtils.getIndexMustExist(secureKeys, "key");
/*  418 */     for (secureKeys.first(); secureKeys.isRowPresent(); secureKeys.next())
/*      */     {
/*  420 */       String key = secureKeys.getStringValue(keyIndex);
/*  421 */       SharedObjects.addSecureEnvironmentKey(key);
/*      */     }
/*      */ 
/*  424 */     interval.traceAndRestart("startup", "setup secure environment");
/*      */ 
/*  427 */     loadCustomEnvironmentValues();
/*  428 */     interval.traceAndRestart("startup", "loadCustomEnvironmentValues()");
/*      */ 
/*  431 */     computeWebPathEnvironment();
/*  432 */     interval.traceAndRestart("startup", "computeWebPathEnvironment()");
/*      */ 
/*  437 */     DataLoader.translateExternalPublishedStrings();
/*      */ 
/*  440 */     ActiveState.load();
/*  441 */     interval.traceAndRestart("startup", "ActiveState.load()");
/*      */ 
/*  444 */     configureAppObjects();
/*  445 */     interval.traceAndRestart("startup", "configureAppObjects()");
/*      */ 
/*  448 */     configDocObjects();
/*  449 */     interval.traceAndRestart("startup", "configDocObjects()");
/*      */ 
/*  452 */     ConfigPageUtils.loadConfigurationOptions();
/*  453 */     interval.traceAndRestart("startup", "ConfigPageUtils.loadConfigurationOptions()");
/*      */ 
/*  464 */     extraAfterConfigInit();
/*  465 */     interval.traceAndRestart("startup", "extraAfterConfigInit()");
/*  466 */     interval.stop();
/*  467 */     SharedObjects.logMessages(null);
/*  468 */     interval.trace("startup", "SharedObjects.logMessages()");
/*  469 */     SecurityUtils.init();
/*  470 */     interval.trace("startup", "SecurityUtils.init()");
/*      */ 
/*  474 */     if (installInfo == null)
/*      */       return;
/*  476 */     installInfo.makeBackupIntradocCfg();
/*      */   }
/*      */ 
/*      */   public static void extraAfterConfigInit()
/*      */     throws DataException, ServiceException
/*      */   {
/*  482 */     m_extendedLoader.extraAfterConfigInit();
/*      */   }
/*      */ 
/*      */   public static void initComponentData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  488 */     ResourceTrace.m_traceResourceConflict = (SharedObjects.getEnvValueAsBoolean("TraceResourceConflict", false)) || (SystemUtils.isActiveTrace("componentloader"));
/*      */ 
/*  491 */     ResourceTrace.m_traceResourceOverride = (SharedObjects.getEnvValueAsBoolean("TraceResourceOverride", false)) || ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("componentloader")));
/*      */ 
/*  494 */     ResourceTrace.m_traceResourceLoad = (SharedObjects.getEnvValueAsBoolean("TraceResourceLoad", false)) || ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("componentloader")));
/*      */ 
/*  498 */     ComponentLoader.initDefaults();
/*  499 */     ComponentLoader.load();
/*      */   }
/*      */ 
/*      */   public static void clearComponentUpdateAlert() throws DataException, ServiceException
/*      */   {
/*  504 */     String alertId = "csComponentUpdateNeedRestart";
/*  505 */     boolean componentUpdateAlertExists = AlertUtils.existsAlert(alertId, 1);
/*  506 */     if (!componentUpdateAlertExists)
/*      */       return;
/*  508 */     AlertUtils.deleteAlertSimple(alertId);
/*      */   }
/*      */ 
/*      */   public static void loadComponentData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  517 */     loadComponentDataEx(false);
/*      */   }
/*      */ 
/*      */   public static void loadComponentDataEx(boolean throwResourceError)
/*      */     throws DataException, ServiceException
/*      */   {
/*  525 */     ComponentLoader.sortComponents();
/*  526 */     loadCachedHtmlData();
/*  527 */     loadResourcesEx(throwResourceError);
/*      */ 
/*  531 */     initOSSettingsHelper();
/*      */ 
/*  533 */     if (SharedObjects.getEnvValueAsBoolean("LoadScriptExtensions", true))
/*      */     {
/*  537 */       loadIdocScriptExtensions();
/*      */     }
/*      */ 
/*  541 */     DataResultSet features = SharedObjects.getTable("CoreFeatures");
/*  542 */     features = filterFeatures(features);
/*  543 */     FeaturesInterface f = Features.newFeaturesObject(null, null);
/*  544 */     Features.registerFeaturesFromResultSet(f, features, null);
/*  545 */     SharedObjects.putObject("Features", "InitialCoreFeatures", f);
/*      */ 
/*  547 */     DataResultSet mergeSet = SharedObjects.getTable("CoreMergeRules");
/*  548 */     if (mergeSet != null)
/*      */     {
/*  550 */       DataBinder binder = new DataBinder();
/*  551 */       binder.addResultSet("CoreMergeRules", mergeSet);
/*  552 */       ComponentLoader.loadRules(binder, "CoreMergeRules");
/*      */     }
/*      */ 
/*  557 */     ComponentLoader.sortComponents();
/*      */ 
/*  559 */     mergeResourceTables();
/*      */ 
/*  561 */     if (SharedObjects.getEnvValueAsBoolean("LoadScriptExtensions", true))
/*      */     {
/*  564 */       loadIdocScriptExtensions();
/*      */     }
/*      */ 
/*  567 */     features = SharedObjects.getTable("CoreFeatures");
/*  568 */     registerFeatures(features);
/*      */ 
/*  570 */     DataResultSet filterSet = SharedObjects.getTable("CoreFilters");
/*  571 */     if (filterSet != null)
/*      */     {
/*  573 */       DataBinder filterBinder = new DataBinder();
/*  574 */       filterBinder.addResultSet("CoreFilters", filterSet);
/*  575 */       Vector list = PluginFilterLoader.cacheFilters(filterBinder, "CoreFilters");
/*  576 */       PluginFilters.registerFilters(list);
/*      */     }
/*      */ 
/*  580 */     PluginFilters.sortFilters();
/*      */ 
/*  582 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/*  583 */     PluginFilters.filter("loadComponentDataPostFilters", null, null, cxt);
/*      */ 
/*  585 */     loadTemplatePages();
/*      */ 
/*  588 */     ComponentListManager.updateLegacyInfo();
/*      */ 
/*  590 */     ComponentLoader.verifyFeatures();
/*      */ 
/*  593 */     prepareComponentPublishing();
/*      */ 
/*  595 */     clearComponentUpdateAlert();
/*      */   }
/*      */ 
/*      */   public static DataResultSet filterFeatures(DataResultSet features)
/*      */     throws ServiceException
/*      */   {
/*  606 */     ScriptContext serverContext = new ScriptContext();
/*  607 */     ServerScriptExtensions extensions = new ServerScriptExtensions();
/*  608 */     extensions.load(serverContext);
/*  609 */     serverContext.registerExtension(extensions);
/*  610 */     serverContext.addContext((ScriptContext)AppObjectRepository.getObject("DefaultScriptContext"));
/*      */ 
/*  612 */     AppObjectRepository.putObject("ServerScriptContext", serverContext);
/*  613 */     m_cxt.setCachedObject("ScriptContext", serverContext);
/*      */ 
/*  615 */     DataResultSet finalFeatures = new DataResultSet();
/*  616 */     finalFeatures.copyFieldInfo(features);
/*  617 */     FieldInfo nameInfo = new FieldInfo();
/*  618 */     FieldInfo scriptInfo = new FieldInfo();
/*  619 */     DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  620 */     PageMerger merger = ScriptExtensionUtils.getOrCreatePageMerger(binder, m_cxt);
/*      */ 
/*  624 */     features.getFieldInfo("idcFeatureName", nameInfo);
/*  625 */     features.getFieldInfo("idcFeatureEnabled", scriptInfo);
/*  626 */     for (features.first(); features.isRowPresent(); features.next())
/*      */     {
/*  628 */       String name = features.getStringValue(nameInfo.m_index);
/*  629 */       String isEnabledString = SharedObjects.getEnvironmentValue("EnableFeature_" + name);
/*  630 */       if ((isEnabledString == null) && (scriptInfo.m_index >= 0))
/*      */       {
/*  632 */         isEnabledString = features.getStringValue(scriptInfo.m_index);
/*      */       }
/*  634 */       if ((isEnabledString == null) || (isEnabledString.length() == 0))
/*      */       {
/*  636 */         finalFeatures.addRow(features.getCurrentRowValues());
/*      */       }
/*      */       else
/*      */         try
/*      */         {
/*  641 */           String isEnabledValue = merger.evaluateScriptReportError(isEnabledString);
/*  642 */           if (SystemUtils.m_verbose)
/*      */           {
/*  644 */             Report.debug("startup", "check for feature " + name + " with script " + isEnabledString + " result is " + isEnabledValue, null);
/*      */           }
/*      */ 
/*  647 */           if (StringUtils.convertToBool(isEnabledValue, false))
/*      */           {
/*  649 */             finalFeatures.addRow(features.getCurrentRowValues());
/*      */           }
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  654 */           SharedUtils.logCommonException(e, null, 1);
/*      */         }
/*      */         catch (IllegalArgumentException e)
/*      */         {
/*  658 */           SharedUtils.logCommonException(e, null, 1);
/*      */         }
/*      */         catch (ParseSyntaxException e)
/*      */         {
/*  662 */           SharedUtils.logCommonException(e, null, 1);
/*      */         }
/*      */     }
/*  665 */     merger.releaseAllTemporary();
/*  666 */     return finalFeatures;
/*      */   }
/*      */ 
/*      */   public static void registerFeatures(DataResultSet features)
/*      */     throws ServiceException
/*      */   {
/*  676 */     DataResultSet finalFeatures = filterFeatures(features);
/*  677 */     Features.registerFromResultSet(finalFeatures, null);
/*      */   }
/*      */ 
/*      */   public static void computeWebPathEnvironment()
/*      */   {
/*  685 */     boolean doProxyCgi = SharedObjects.getEnvValueAsBoolean("IsProxiedServer", false);
/*  686 */     if (!doProxyCgi)
/*      */       return;
/*  688 */     String cgiName = DirectoryLocator.getCgiFileName();
/*  689 */     String relativeWebRoot = DocumentPathBuilder.getRelativeWebRoot();
/*  690 */     String proxyCgiName = DirectoryLocator.createProxiedCgiFileName(cgiName, relativeWebRoot);
/*  691 */     SharedObjects.putEnvironmentValue("CgiFileName", proxyCgiName);
/*      */   }
/*      */ 
/*      */   public static void loadIdocScriptExtensions()
/*      */     throws DataException, ServiceException
/*      */   {
/*  697 */     DataResultSet drset = SharedObjects.getTable("IdocScriptExtensions");
/*  698 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/*  700 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "IdocScriptExtensions");
/*      */ 
/*  702 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  705 */     int count = 0;
/*  706 */     Vector v = new IdcVector();
/*  707 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  709 */       Properties row = drset.getCurrentRowProps();
/*  710 */       v.addElement(row);
/*  711 */       ++count;
/*      */     }
/*      */ 
/*  714 */     IdcComparator comp = new Object()
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/*  718 */         Properties p1 = (Properties)obj1;
/*  719 */         Properties p2 = (Properties)obj2;
/*  720 */         String s1 = p1.getProperty("loadOrder");
/*  721 */         String s2 = p2.getProperty("loadOrder");
/*      */ 
/*  723 */         if ((s1 == null) || (s2 == null))
/*      */         {
/*  725 */           return 0;
/*      */         }
/*      */ 
/*  728 */         int i1 = Integer.parseInt(s1);
/*  729 */         int i2 = Integer.parseInt(s2);
/*      */ 
/*  731 */         return i1 - i2;
/*      */       }
/*      */     };
/*  735 */     Sort.sortVector(v, comp);
/*      */ 
/*  737 */     ScriptContext context = new ScriptContext();
/*  738 */     for (int i = 0; i < count; ++i)
/*      */     {
/*  740 */       Properties row = (Properties)v.elementAt(i);
/*  741 */       String name = row.getProperty("name");
/*  742 */       String classname = row.getProperty("class");
/*  743 */       String compName = row.getProperty("idcComponentName");
/*  744 */       String msg = LocaleUtils.encodeMessage("csUnableToInstantiateClassForScriptExtensions", null, classname, compName);
/*      */       try
/*      */       {
/*  749 */         Report.trace("componentloader", "loading IdocScriptExtensions from " + classname, null);
/*      */ 
/*  751 */         ScriptExtensions extensions = (ScriptExtensions)ComponentClassFactory.createClassInstance(name, classname, msg);
/*      */ 
/*  753 */         extensions.load(context);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  757 */         Report.trace("componentloader", "failed IdocScriptExtensions from " + classname, e);
/*      */ 
/*  759 */         if (SharedObjects.getEnvValueAsBoolean("IgnoreComponentLoadError", false))
/*      */         {
/*  761 */           Report.error(null, null, e);
/*  762 */           IdcMessage idcmsg = IdcMessageFactory.lc(e);
/*  763 */           String msgText = LocaleResources.localizeMessage(null, idcmsg, null).toString();
/*  764 */           SystemUtils.errln(msgText);
/*      */         }
/*      */         else
/*      */         {
/*  768 */           throw e;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  773 */     AppObjectRepository.putObject("DefaultScriptContext", context);
/*      */   }
/*      */ 
/*      */   public static void initLogInfo()
/*      */   {
/*  779 */     LogInfo logs = new LogInfo();
/*  780 */     logs.init();
/*  781 */     SharedObjects.putTable(LogInfo.m_tableName, logs);
/*      */ 
/*  784 */     if (SharedObjects.getEnvironmentValue("PageCharset") != null)
/*      */       return;
/*  786 */     String msg = LocaleUtils.encodeMessage("csPageCharacterSetIsUndefined", null, FileUtils.m_javaSystemEncoding);
/*  787 */     Report.trace("system", LocaleResources.localizeMessage(msg, null), null);
/*  788 */     Report.warning("system", msg, null);
/*      */   }
/*      */ 
/*      */   public static void configureAppObjects()
/*      */     throws ServiceException
/*      */   {
/*  794 */     String productLabel = SharedObjects.getEnvironmentValue("ProductLabel");
/*  795 */     if ((productLabel == null) || (productLabel.length() == 0))
/*      */     {
/*  797 */       if (productLabel == null)
/*      */       {
/*  799 */         ExecutionContextAdaptor englishContext = new ExecutionContextAdaptor();
/*  800 */         englishContext.setParentContext(m_cxt);
/*  801 */         Properties props = new IdcProperties();
/*  802 */         props.put("lcLanguageId", "en");
/*  803 */         IdcLocale englishLocale = new IdcLocale("English-US");
/*  804 */         englishLocale.init(props);
/*  805 */         englishContext.setCachedObject("UserLocale", englishLocale);
/*  806 */         productLabel = LocaleResources.getString("wwHeaderProductName", englishContext);
/*      */       }
/*  808 */       if (productLabel == null)
/*      */       {
/*  811 */         productLabel = "wwHeaderProductName";
/*      */       }
/*  813 */       SharedObjects.putEnvironmentValue("ProductLabel", productLabel);
/*      */     }
/*  815 */     boolean isForce4DigitYear = SharedObjects.getEnvValueAsBoolean("UseFourDigitYear", true);
/*  816 */     DateUtils.setForce4DigitYear(isForce4DigitYear);
/*      */ 
/*  818 */     boolean noDataLoadDelay = SharedObjects.getEnvValueAsBoolean("NoDataLoadDelay", false);
/*  819 */     DataLoader.m_doResourceCacheChecking = (noDataLoadDelay) || (!SharedObjects.getEnvValueAsBoolean("DisableSharedCacheChecking", false));
/*      */ 
/*  821 */     SystemUtils.addAsDefaultTrace("resourceloader");
/*  822 */     DocumentPathBuilder.m_allLowerCase = !SharedObjects.getEnvValueAsBoolean("DisablePathsLowerCase", false);
/*      */ 
/*  825 */     int resourceAgeTimeout = 0;
/*  826 */     if (!noDataLoadDelay)
/*      */     {
/*  828 */       resourceAgeTimeout = ResourceCacheState.getAgeResourceFilesTimeoutInSecs();
/*  829 */       resourceAgeTimeout = SharedObjects.getTypedEnvironmentInt("AgeResourceFilesTimeoutInSecs", resourceAgeTimeout, 24, 24);
/*      */     }
/*      */ 
/*  835 */     ResourceCacheState.setAgeResourceFilesTimeoutInSecs(resourceAgeTimeout);
/*  836 */     int removeResourceFilesTimeout = ResourceCacheState.getRemoveResourceFilesTimeoutInMins();
/*  837 */     removeResourceFilesTimeout = SharedObjects.getTypedEnvironmentInt("RemoveResourceFilesTimeoutInMins", removeResourceFilesTimeout, 19, 19);
/*      */ 
/*  840 */     ResourceCacheState.setRemoveResourceFilesTimeoutInMins(removeResourceFilesTimeout);
/*      */ 
/*  843 */     int maximumSizeMillions = ResourceCacheState.getMaxSizeResourceFileCacheMillions();
/*  844 */     maximumSizeMillions = SharedObjects.getTypedEnvironmentInt("MaxSizeResourceFileCacheMillions", maximumSizeMillions, 1, 1);
/*      */ 
/*  847 */     ResourceCacheState.setMaxSizeResourceFileCacheMillions(maximumSizeMillions);
/*      */ 
/*  849 */     FileUtils.m_lockTimeout = SharedObjects.getTypedEnvironmentInt("DirectoryLockingTimeoutInSeconds", FileUtils.m_lockTimeout, 24, 24);
/*      */ 
/*  852 */     FileUtils.m_minLockTimeout = SharedObjects.getTypedEnvironmentInt("DirectoryMinLockingTimeoutInSeconds", FileUtils.m_minLockTimeout, 24, 24);
/*      */ 
/*  855 */     FileUtils.m_validateRenames = EnvUtils.isFamily("windows");
/*  856 */     FileUtils.m_validateRenames = SharedObjects.getEnvValueAsBoolean("DirectoryLockingValidateRenames", FileUtils.m_validateRenames);
/*      */ 
/*  858 */     FileUtils.m_touchMonitorInterval = SharedObjects.getTypedEnvironmentInt("LongTermLockTouchMonitorInterval", FileUtils.m_lockTimeout * 250, 24, 24);
/*      */ 
/*  861 */     FileUtils.updateLockDirectoryConfiguration();
/*      */ 
/*  863 */     boolean isReduceWsDisabled = SharedObjects.getEnvValueAsBoolean("ScriptParsingReduceWhitespaceDisabled", false);
/*  864 */     if (isReduceWsDisabled)
/*      */     {
/*  866 */       ParseOutput parseOutput = new ParseOutput();
/*  867 */       DynamicHtmlStatic.checkInit(parseOutput);
/*  868 */       DynamicHtmlStatic.m_defaultDirectives &= -2;
/*  869 */       parseOutput.releaseBuffers();
/*      */     }
/*      */ 
/*  874 */     SharedObjects.conditionallySetEnvBool("AllowProxyingServers", !EnvUtils.isHostedInAppServer(), "configureAppObjects");
/*      */   }
/*      */ 
/*      */   public static void configDocObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*  880 */     RevisionSpec.initImplementor();
/*      */ 
/*  883 */     String defVal = SharedObjects.getEnvironmentValue("NumAdditionalRendition");
/*  884 */     if (defVal != null)
/*      */       return;
/*  886 */     SharedObjects.putEnvironmentValue("NumAdditionalRenditions", "2");
/*      */   }
/*      */ 
/*      */   public static void initWebFilterConfig()
/*      */     throws ServiceException
/*      */   {
/*  892 */     String[] excludedKeys = { "NtlmSecurityEnabled", "blDateFormat", "blFieldTypes" };
/*  893 */     DataBinder filterBinder = WebFilterConfigUtils.readFilterConfigFromFile();
/*      */ 
/*  895 */     Enumeration en = filterBinder.m_localData.propertyNames();
/*  896 */     while (en.hasMoreElements())
/*      */     {
/*  898 */       String key = (String)en.nextElement();
/*  899 */       String config = SharedObjects.getEnvironmentValue(key);
/*  900 */       if (config != null)
/*      */       {
/*  902 */         if (StringUtils.findStringIndex(excludedKeys, key) >= 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  906 */         SharedObjects.putEnvironmentValue(key + ":overrideFilterAdmin", "1");
/*      */ 
/*  910 */         String msg = LocaleUtils.encodeMessage("csDuplicateConfigWarningMessage", null, key);
/*  911 */         Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*  912 */         Log.warn(msg);
/*      */       }
/*      */     }
/*      */ 
/*  916 */     if (WebFilterConfigUtils.checkForHomePageUpdate(filterBinder))
/*      */     {
/*  918 */       String ntlmString = "0";
/*  919 */       if (SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false))
/*      */       {
/*  921 */         ntlmString = "1";
/*      */       }
/*  923 */       filterBinder.putLocal("NtlmSecurityEnabled", ntlmString);
/*  924 */       WebFilterConfigUtils.writeFilterConfigToFile(filterBinder);
/*      */     }
/*  926 */     WebFilterConfigUtils.updateServerEnvironment(filterBinder);
/*  927 */     WebFilterConfigUtils.updateWebFilterConfig(filterBinder);
/*      */   }
/*      */ 
/*      */   public static void loadLogInfo() throws ServiceException
/*      */   {
/*  932 */     ResultSet rset = SharedObjects.getTable(LogInfo.m_tableName);
/*  933 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  935 */       String app = rset.getStringValue(0);
/*  936 */       String indexPage = rset.getStringValue(1);
/*  937 */       String prefix = rset.getStringValue(2);
/*  938 */       String title = rset.getStringValue(3);
/*      */ 
/*  940 */       String dir = DirectoryLocator.getLogDirectory(app);
/*  941 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 0, true);
/*  942 */       Log.setLogInfo(app, dir, indexPage, prefix, title);
/*      */ 
/*  944 */       boolean linkFileExist = FileUtils.checkFile(dir + "/" + indexPage, true, false) == 0;
/*  945 */       if (linkFileExist)
/*      */         continue;
/*  947 */       String msg = LocaleUtils.encodeMessage("csAppLogInitiated", null, app);
/*  948 */       Log.infoEx(msg, app);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Workspace loadDatabase(int numConnections)
/*      */     throws DataException, ServiceException
/*      */   {
/*  956 */     return loadDatabaseWithFlags(numConnections, 0);
/*      */   }
/*      */ 
/*      */   public static Workspace loadDatabaseWithFlags(int numConnections, int flags) throws DataException, ServiceException
/*      */   {
/*  961 */     IdcSystemRunTimeConfig.releaseRawDBConnection();
/*      */ 
/*  963 */     Workspace workspace = null;
/*  964 */     if (((flags & 0x1) == 0) && (!Features.checkLevel("JDBC", null)))
/*      */     {
/*  966 */       return null;
/*      */     }
/*  968 */     boolean isJdbc = SharedObjects.getEnvValueAsBoolean("IsJdbc", true);
/*      */     try
/*      */     {
/*  971 */       Provider provider = Providers.getProvider("SystemDatabase");
/*  972 */       workspace = (Workspace)provider.getProvider();
/*      */ 
/*  974 */       DataBinder providerData = provider.getProviderData();
/*      */ 
/*  976 */       prepareQueryModifiers(providerData.getLocalData());
/*      */ 
/*  978 */       provider.configureProvider();
/*      */ 
/*  980 */       numConnections = SharedObjects.getEnvironmentInt("NumConnections", numConnections);
/*  981 */       providerData.putLocal("NumConnections", "" + numConnections);
/*      */ 
/*  983 */       provider.startProvider();
/*      */ 
/*  988 */       String dbType = workspace.getProperty("DatabaseType");
/*  989 */       String dbVersion = workspace.getProperty("DatabaseVersion");
/*  990 */       String dbName = workspace.getProperty("DatabaseName");
/*  991 */       String dbPreserveCase = workspace.getProperty("DatabasePreserveCase");
/*      */ 
/*  993 */       if (dbType != null)
/*      */       {
/*  995 */         SharedObjects.putEnvironmentValue("SystemDatabaseType", dbType);
/*      */       }
/*  997 */       if (dbVersion != null)
/*      */       {
/*  999 */         SharedObjects.putEnvironmentValue("SystemDatabaseVersion", dbVersion);
/*      */       }
/* 1001 */       if (dbName != null)
/*      */       {
/* 1003 */         SharedObjects.putEnvironmentValue("SystemDatabaseName", dbName);
/*      */       }
/* 1005 */       if (dbPreserveCase != null)
/*      */       {
/* 1007 */         SharedObjects.putEnvironmentValue("DatabasePreserveCase", dbPreserveCase);
/*      */       }
/*      */ 
/* 1014 */       String wildCards = (isJdbc) ? "%_" : "*?";
/* 1015 */       SharedObjects.putEnvironmentValue("DatabaseWildcards", wildCards);
/*      */ 
/* 1017 */       provider.markState("ready");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1021 */       throw new ServiceException("!csUnableToInstantiateSystemDatabase", e);
/*      */     }
/*      */ 
/* 1024 */     IdcSystemConfig.initConfigFileWorkspace(workspace);
/*      */ 
/* 1026 */     WorkspaceUtils.addWorkspace("system", workspace);
/*      */ 
/* 1028 */     return workspace;
/*      */   }
/*      */ 
/*      */   public static Workspace loadSystemUserDatabase(int numConnections)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1034 */     return loadSystemUserDatabaseWithFlags(numConnections, 0);
/*      */   }
/*      */ 
/*      */   public static Workspace loadSystemUserDatabaseWithFlags(int numConnections, int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1040 */     Workspace workspace = null;
/* 1041 */     if (((flags & 0x1) == 0) && (!Features.checkLevel("JDBC", null)))
/*      */     {
/* 1043 */       return null;
/*      */     }
/*      */     try
/*      */     {
/* 1047 */       Provider provider = null;
/* 1048 */       if (SharedObjects.getEnvValueAsBoolean("IsUserDatabase", false))
/*      */       {
/* 1050 */         provider = Providers.getProvider("SystemUserDatabase");
/*      */       }
/*      */       else
/*      */       {
/* 1054 */         provider = Providers.getProvider("SystemDatabase");
/*      */       }
/* 1056 */       workspace = (Workspace)provider.getProvider();
/*      */ 
/* 1058 */       DataBinder providerData = provider.getProviderData();
/*      */ 
/* 1060 */       provider.configureProvider();
/*      */ 
/* 1062 */       numConnections = SharedObjects.getEnvironmentInt("NumConnections", numConnections);
/*      */ 
/* 1064 */       numConnections = SharedObjects.getEnvironmentInt("SystemUserDatabase:NumConnections", numConnections);
/* 1065 */       providerData.putLocal("NumConnections", "" + numConnections);
/*      */ 
/* 1067 */       provider.startProvider();
/*      */ 
/* 1069 */       provider.markState("ready");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1073 */       throw new ServiceException("!csUnableToInstantiateSystemUserDatabase", e);
/*      */     }
/*      */ 
/* 1076 */     WorkspaceUtils.addWorkspace("user", workspace);
/* 1077 */     return workspace;
/*      */   }
/*      */ 
/*      */   public static void prepareQueryModifiers(PropParameters params)
/*      */   {
/* 1082 */     prepareQueryModifiers(params.m_properties);
/*      */   }
/*      */ 
/*      */   protected static void prepareQueryModifiers(Properties props)
/*      */   {
/* 1088 */     DataResultSet rset = SharedObjects.getTable("QueryModifiers");
/* 1089 */     if (rset == null)
/*      */       return;
/* 1091 */     DataResultSet filtered = new DataResultSet();
/* 1092 */     filtered.copySimpleFiltered(rset, "qmType", "database");
/* 1093 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1094 */     for (filtered.first(); filtered.isRowPresent(); filtered.next())
/*      */     {
/* 1096 */       String modifierClass = ResultSetUtils.getValue(filtered, "qmClassName");
/* 1097 */       if (builder.length() != 0)
/*      */       {
/* 1099 */         builder.append(',');
/*      */       }
/* 1101 */       builder.append(modifierClass);
/*      */     }
/* 1103 */     props.put("JdbcQueryModifierList", builder.toString());
/*      */   }
/*      */ 
/*      */   public static void loadDatabaseFieldInfo(Workspace ws, String sql)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1110 */     if (!Features.checkLevel("JDBC", null))
/*      */     {
/* 1112 */       return;
/*      */     }
/* 1114 */     DataBinder binder = new DataBinder();
/* 1115 */     binder.putLocal("DatabaseFieldInfoQuery", sql);
/* 1116 */     int rc = PluginFilters.filter("loadDatabaseFieldInfoQuery", ws, binder, m_extendedLoader);
/*      */ 
/* 1118 */     if (rc == 1)
/*      */     {
/* 1120 */       return;
/*      */     }
/* 1122 */     sql = binder.getLocal("DatabaseFieldInfoQuery");
/* 1123 */     ResultSet rset = (ResultSet)m_extendedLoader.getCachedObject("DatabaseFieldInfoResultSet");
/* 1124 */     if (rset == null)
/*      */     {
/* 1126 */       rset = ws.createResultSetSQL(sql);
/*      */     }
/* 1128 */     DataResultSet drset = new DataResultSet();
/* 1129 */     drset.copy(rset);
/*      */ 
/* 1131 */     int numFields = drset.getNumFields();
/* 1132 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1134 */       FieldInfo info = new FieldInfo();
/* 1135 */       drset.getIndexFieldInfo(i, info);
/* 1136 */       String fieldStr = info.m_name + ":maxLength";
/*      */ 
/* 1138 */       int maxLength = info.m_maxLen;
/* 1139 */       if (maxLength == 0)
/*      */       {
/* 1141 */         maxLength = SharedObjects.getEnvironmentInt("MemoFieldSize", 2000);
/*      */       }
/* 1143 */       SharedObjects.putEnvironmentValue(fieldStr, Integer.toString(maxLength));
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadServiceData() throws DataException, ServiceException
/*      */   {
/* 1149 */     ServiceManager sman = new ServiceManager();
/* 1150 */     sman.loadServiceScripts();
/*      */ 
/* 1155 */     m_extendedLoader.extraAfterServicesLoadInit();
/*      */ 
/* 1159 */     ResourceTrace.reset();
/*      */   }
/*      */ 
/*      */   public static void loadServerTemplates(Workspace workspace)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/* 1165 */     DataResultSet templates = SharedObjects.getTable("ViewURLTemplates");
/* 1166 */     if (templates != null)
/*      */     {
/* 1168 */       DataBinder binder = new DataBinder();
/* 1169 */       PageMerger pageMerger = new PageMerger(binder, null);
/*      */ 
/* 1171 */       for (templates.first(); templates.isRowPresent(); templates.next())
/*      */       {
/* 1173 */         String name = templates.getStringValueByName("TemplateName");
/* 1174 */         String value = SharedObjects.getEnvironmentValue(name);
/* 1175 */         if ((value != null) && (value.length() != 0))
/*      */           continue;
/* 1177 */         value = "";
/* 1178 */         String relativeURL = templates.getStringValueByName("RelativeURL");
/* 1179 */         if ((relativeURL != null) && (relativeURL.length() > 0))
/*      */         {
/*      */           try
/*      */           {
/* 1183 */             relativeURL = (String)pageMerger.computeValue(relativeURL, false);
/*      */           }
/*      */           catch (IllegalArgumentException e)
/*      */           {
/*      */           }
/*      */ 
/* 1189 */           value = value + relativeURL;
/*      */         }
/*      */ 
/* 1192 */         String parameters = templates.getStringValueByName("Parameters");
/* 1193 */         if ((parameters != null) && (parameters.length() > 0))
/*      */         {
/* 1195 */           value = value + parameters;
/*      */         }
/*      */ 
/* 1198 */         SharedObjects.putEnvironmentValue(name, value);
/*      */       }
/*      */     }
/*      */ 
/* 1202 */     PluginFilters.filter("loadServerTemplates", workspace, null, m_extendedLoader);
/*      */   }
/*      */ 
/*      */   public static void loadCaches(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1210 */     int rc = PluginFilters.filter("loadCaches", workspace, null, m_extendedLoader);
/* 1211 */     if (rc == 1)
/*      */     {
/* 1213 */       return;
/*      */     }
/*      */ 
/* 1217 */     loadSystemVariables();
/*      */ 
/* 1219 */     IntervalData interval = new IntervalData("startup-loadcaches");
/* 1220 */     loadLogInfo();
/* 1221 */     interval.traceAndRestart("startup", "loadLogInfo");
/*      */ 
/* 1223 */     if (!Features.checkLevel("JDBC", null))
/*      */     {
/* 1230 */       String tempDir = DirectoryLocator.getTempDirectory();
/* 1231 */       FileUtils.checkOrCreateDirectory(tempDir, 1);
/* 1232 */       DataBinder.setTemporaryDirectory(tempDir);
/* 1233 */       DataBinder.setFileCounterStart();
/*      */ 
/* 1236 */       PluginFilters.filter("initSubjects", workspace, null, m_extendedLoader);
/* 1237 */       return;
/*      */     }
/*      */ 
/* 1241 */     IdcCounterUtils.loadIdcCounters(workspace);
/* 1242 */     interval.traceAndRestart("startup", "loadIdcCounters");
/*      */ 
/* 1245 */     m_extendedLoader.extraBeforeCacheLoadInit(workspace);
/* 1246 */     interval.traceAndRestart("startup", "extraBeforeCacheLoadInit");
/*      */ 
/* 1250 */     initNlsSortValue(workspace);
/*      */ 
/* 1253 */     initSearchIndexerConfig();
/*      */ 
/* 1256 */     String[][] sqlStrs = { { "SELECT * from Revisions WHERE dID=0", "system" }, { "SELECT * from Users WHERE dName='0'", "user" }, { "SELECT * from RegisteredProjects WHERE dProjectID=''", "system" }, { "SELECT * from Documents WHERE dID=0", "system" } };
/*      */ 
/* 1263 */     for (int i = 0; i < sqlStrs.length; ++i)
/*      */     {
/* 1265 */       loadDatabaseFieldInfo(WorkspaceUtils.getWorkspace(sqlStrs[i][1]), sqlStrs[i][0]);
/*      */     }
/*      */ 
/* 1270 */     CustomSecurityRightsData.init(false, null);
/* 1271 */     CustomSecurityRightsUtils.setDefaultCustomPrivileges(WorkspaceUtils.getWorkspace("user"));
/* 1272 */     initCollaborations(workspace);
/* 1273 */     interval.traceAndRestart("startup", "initCollaborations");
/* 1274 */     loadCachedTables(workspace);
/* 1275 */     interval.traceAndRestart("startup", "loadCachedTables");
/* 1276 */     loadDynamicMetaDataQueries(workspace);
/* 1277 */     loadAdminTargetedQuickSearches();
/*      */ 
/* 1279 */     cacheFileStoreProvider(workspace);
/* 1280 */     interval.traceAndRestart("startup", "cacheFileStoreProvider");
/*      */ 
/* 1283 */     String tempDir = DirectoryLocator.getTempDirectory();
/* 1284 */     FileUtils.checkOrCreateDirectory(tempDir, 1);
/* 1285 */     DataBinder.setTemporaryDirectory(tempDir);
/* 1286 */     DataBinder.setFileCounterStart();
/*      */ 
/* 1288 */     initProjects(workspace);
/* 1289 */     interval.traceAndRestart("startup", "initProjects");
/*      */   }
/*      */ 
/*      */   public static void cacheFileStoreProvider(Workspace workspace)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1300 */     FileStoreProviderLoader.cacheFileStoreProvider(workspace, m_cxt);
/*      */   }
/*      */ 
/*      */   public static void loadSystemVariables()
/*      */   {
/* 1305 */     Map map = new ConcurrentHashMap();
/* 1306 */     DynamicData ddField = ResourceContainerUtils.getDynamicDataResource("SystemVariables");
/* 1307 */     if ((ddField != null) && (ddField.m_hasMergedTable))
/*      */     {
/* 1310 */       for (int i = 0; i < ddField.m_mergedTable.m_rows.size(); ++i)
/*      */       {
/* 1312 */         String[] row = ddField.m_mergedTable.getRow(i);
/* 1313 */         FieldInfo fi = MetaFieldUtils.createFieldInfo(row[0], row[1]);
/* 1314 */         if (row[2].length() > 0)
/*      */         {
/* 1316 */           fi.m_maxLen = NumberUtils.parseInteger(row[2], 0);
/*      */         }
/* 1318 */         for (int j = 0; j < row.length; ++j)
/*      */         {
/* 1320 */           FieldInfoUtils.setFieldOption(fi, ddField.m_mergedTable.m_colNames[j], row[j]);
/*      */         }
/*      */ 
/* 1323 */         map.put(row[0], fi);
/*      */       }
/*      */     }
/* 1326 */     SharedObjects.putObject("SystemVariables", "SystemVariables", map);
/*      */   }
/*      */ 
/*      */   public static void initSearchIndexerConfig()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1332 */     if (!Features.checkLevel("Search", null))
/*      */     {
/* 1334 */       return;
/*      */     }
/* 1336 */     SearchIndexerUtils.initSearchIndexerConfig();
/*      */   }
/*      */ 
/*      */   public static void loadCachedTables(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1342 */     if (!Features.checkLevel("ContentManagement", null))
/*      */     {
/* 1345 */       PluginFilters.filter("initSubjects", workspace, null, m_extendedLoader);
/* 1346 */       return;
/*      */     }
/*      */ 
/* 1354 */     Vector alreadyLoadedSubjects = getAlreadyLoadedSubjectsList();
/*      */ 
/* 1356 */     SubjectCallbackAdapter schemaSubjectCallback = new SchemaSubjectCallback();
/* 1357 */     schemaSubjectCallback.setWorkspace(workspace);
/* 1358 */     schemaSubjectCallback.refresh("schema");
/* 1359 */     alreadyLoadedSubjects.addElement("schema");
/*      */ 
/* 1361 */     if (Features.checkLevel("ContentManagement", null))
/*      */     {
/* 1363 */       SubjectCallbackAdapter metaDataCallback = new MetaDataSubjectCallback();
/* 1364 */       metaDataCallback.setWorkspace(workspace);
/* 1365 */       metaDataCallback.refresh("metadata");
/* 1366 */       alreadyLoadedSubjects.addElement("metadata");
/*      */ 
/* 1368 */       SubjectCallbackAdapter docClassCallback = new DocClassSubjectCallback();
/* 1369 */       docClassCallback.setWorkspace(workspace);
/* 1370 */       docClassCallback.refresh("docclasses");
/* 1371 */       alreadyLoadedSubjects.addElement("docclasses");
/*      */ 
/* 1373 */       SubjectCallbackAdapter queryCallback = new DynamicQueriesSubjectCallback();
/* 1374 */       queryCallback.setWorkspace(workspace);
/* 1375 */       queryCallback.refresh("dynamicqueries");
/* 1376 */       alreadyLoadedSubjects.addElement("dynamicqueries");
/*      */ 
/* 1379 */       IdcCacheLoader.loadMonitoredTables(workspace);
/*      */     }
/*      */ 
/* 1383 */     ProfileCache.init();
/*      */ 
/* 1391 */     SharedPageMergerData.init();
/*      */ 
/* 1394 */     PluginFilters.filter("initSubjects", workspace, null, m_extendedLoader);
/*      */   }
/*      */ 
/*      */   public static Vector getAlreadyLoadedSubjectsList()
/*      */   {
/* 1399 */     Vector alreadyLoadedSubjects = (Vector)SharedObjects.getObject("globalObjects", "alreadyLoadedSubjectsList");
/*      */ 
/* 1401 */     if (alreadyLoadedSubjects == null)
/*      */     {
/* 1403 */       alreadyLoadedSubjects = new IdcVector();
/* 1404 */       SharedObjects.putObject("globalObjects", "alreadyLoadedSubjectsList", alreadyLoadedSubjects);
/*      */     }
/*      */ 
/* 1407 */     return alreadyLoadedSubjects;
/*      */   }
/*      */ 
/*      */   public static void loadCachedHtmlData()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1418 */     DataLoader.cacheTemplateTables();
/*      */ 
/* 1420 */     boolean isLoadRefinery = SharedObjects.getEnvValueAsBoolean("LoadRefinery", false);
/* 1421 */     if (!isLoadRefinery)
/*      */     {
/* 1423 */       String product = SharedObjects.getEnvironmentValue("IdcProductName");
/* 1424 */       isLoadRefinery = (product != null) && (product.equals("idcibr"));
/*      */     }
/* 1426 */     if (isLoadRefinery)
/*      */       return;
/* 1428 */     SubjectCallbackAdapter templatesCallback = new TemplatesSubjectCallback();
/* 1429 */     templatesCallback.setLists(new String[] { "IntradocTemplates", "CurrentVerityTemplates" }, null);
/*      */ 
/* 1431 */     SubjectManager.registerCallback("templates", templatesCallback);
/*      */ 
/* 1434 */     SubjectCallbackAdapter reportsCallback = new ReportsSubjectCallback();
/* 1435 */     reportsCallback.setLists(new String[] { "ReportsToLoad" }, null);
/* 1436 */     SubjectManager.registerCallback("reports", reportsCallback);
/*      */   }
/*      */ 
/*      */   public static void loadTemplatePages()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1445 */     DataLoader.cacheTemplateFiles();
/*      */   }
/*      */ 
/*      */   public static void loadPageBuilderConfig()
/*      */     throws ServiceException
/*      */   {
/* 1451 */     if (m_isLoadPageBuilderConfig)
/*      */     {
/* 1453 */       return;
/*      */     }
/*      */ 
/* 1457 */     String pDir = DirectoryLocator.getAppDataDirectory() + "pages/";
/* 1458 */     FileUtils.checkOrCreateDirectory(pDir, 2, 1);
/* 1459 */     FileUtils.validateDirectory(pDir, "!csErrorPageDataDir");
/*      */ 
/* 1462 */     String resultCount = SharedObjects.getEnvironmentValue("ResultCount");
/* 1463 */     if (resultCount == null)
/*      */     {
/* 1465 */       SharedObjects.putEnvironmentValue("ResultCount", "20");
/*      */     }
/*      */ 
/* 1468 */     SubjectCallback pagelistCallback = new SubjectCallbackAdapter();
/* 1469 */     SubjectManager.registerCallback("pagelist", pagelistCallback);
/*      */ 
/* 1471 */     m_isLoadPageBuilderConfig = true;
/*      */   }
/*      */ 
/*      */   public static void loadDynamicMetaDataQueries(Workspace workspace) throws ServiceException
/*      */   {
/* 1476 */     SearchLoader.initEx(workspace);
/*      */ 
/* 1479 */     String errMsg = "!csErrorCachingSearchCollections";
/*      */     try
/*      */     {
/* 1482 */       errMsg = "Failed to cache SearchCollections table. ";
/* 1483 */       SearchLoader.cacheSearchCollections();
/* 1484 */       if (SharedObjects.getEnvValueAsBoolean("CacheSearchResults", true))
/*      */       {
/* 1486 */         errMsg = "!csErrorCreatingSearchCache";
/* 1487 */         SearchLoader.createSearchCache();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1492 */       Report.error(null, errMsg, e);
/*      */     }
/*      */ 
/* 1496 */     SubjectCallbackAdapter dynQueriesCallback = new DynamicQueriesSubjectCallback();
/* 1497 */     dynQueriesCallback.setWorkspace(workspace);
/* 1498 */     String[] tables = { "SearchDesignInfo" };
/* 1499 */     dynQueriesCallback.setListsWithEnv(tables, null, new String[] { "IndexCollectionSynced", "IndexDesignRebuildRequired" });
/* 1500 */     dynQueriesCallback.setOptionalKeys(tables);
/* 1501 */     SubjectManager.registerCallback("dynamicqueries", dynQueriesCallback);
/*      */   }
/*      */ 
/*      */   public static void loadAdminTargetedQuickSearches()
/*      */   {
/* 1506 */     SubjectCallback searchConfigCallback = new SearchConfigSubjectCallback();
/* 1507 */     SubjectManager.registerCallback("searchconfig", searchConfigCallback);
/* 1508 */     SubjectEventMonitor searchConfigMonitor = new SubjectEventMonitor()
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 1513 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/*      */       }
/*      */     };
/* 1522 */     SubjectManager.addSubjectMonitor("searchapi", searchConfigMonitor);
/*      */   }
/*      */ 
/*      */   public static void loadResources() throws ServiceException
/*      */   {
/* 1527 */     loadResourcesEx(false);
/*      */   }
/*      */ 
/*      */   public static void loadResourcesEx(boolean throwError)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1535 */       if (m_resourcesAlreadyLoaded)
/*      */       {
/* 1537 */         Report.deprecatedUsage("loadResources was called twice...  bad developer!");
/*      */       }
/* 1539 */       ResourceTrace.msg("!csComponentLoadResources");
/* 1540 */       ResourceContainer res = SharedObjects.getResources();
/*      */ 
/* 1547 */       DynamicDataParser dataParser = new ResourceDataParser();
/* 1548 */       DynamicData.addParser(dataParser);
/* 1549 */       IntervalData timer = new IntervalData("loadResourcesEx()");
/*      */ 
/* 1551 */       List infos = new ArrayList();
/* 1552 */       Vector resourceData = ComponentLoader.m_resources;
/* 1553 */       int size = resourceData.size();
/* 1554 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 1556 */         ComponentData data = (ComponentData)resourceData.elementAt(i);
/* 1557 */         String fileName = data.m_file;
/* 1558 */         List info = null;
/* 1559 */         if (fileName.endsWith(".hda"))
/*      */         {
/* 1561 */           Report.trace("componentloader", "loading file " + fileName, null);
/* 1562 */           DataLoader.cacheDataBinderFromFileWithFlags(fileName, data, 16);
/*      */         }
/*      */         else
/*      */         {
/*      */           try
/*      */           {
/* 1569 */             String str = "!csComponentLoadSystemResource";
/* 1570 */             if (!data.m_componentName.equalsIgnoreCase("default"))
/*      */             {
/* 1572 */               str = LocaleUtils.encodeMessage("csComponentLoadName", null, data.m_componentName);
/*      */             }
/*      */ 
/* 1575 */             ResourceTrace.msg(str);
/* 1576 */             res.m_resourceList = null;
/* 1577 */             DataLoader.cacheResourceFile(res, data.m_file);
/* 1578 */             info = res.m_resourceList;
/*      */           }
/*      */           catch (ServiceException e)
/*      */           {
/* 1582 */             if (throwError)
/*      */             {
/* 1584 */               throw new ServiceException(e, "csErrorLoadingResourceFile", new Object[] { fileName });
/*      */             }
/* 1586 */             Report.error(null, e, "csErrorLoadingResourceFile", new Object[] { fileName });
/*      */           }
/*      */         }
/* 1589 */         infos.add(info);
/*      */       }
/* 1591 */       timer.trace("startup", "Loaded " + size + " resource files");
/*      */ 
/* 1595 */       Map clonedTableMap = MapUtils.cloneMap(res.m_tables);
/* 1596 */       for (int i = size - 1; i >= 0; --i)
/*      */       {
/* 1598 */         ComponentData data = (ComponentData)resourceData.elementAt(i);
/* 1599 */         Vector resList = (Vector)infos.get(i);
/* 1600 */         if (resList == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1605 */         int num = resList.size();
/* 1606 */         for (int j = 0; j < num; ++j)
/*      */         {
/* 1608 */           ResourceObject resObj = (ResourceObject)resList.elementAt(j);
/* 1609 */           if ((resObj.m_type != 1) && (resObj.m_type != 4)) {
/*      */             continue;
/*      */           }
/* 1612 */           String name = resObj.m_name;
/* 1613 */           clonedTableMap.remove(name);
/* 1614 */           if (SharedObjects.getTable(name) != null) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1618 */           Table tble = (Table)resObj.m_resource;
/* 1619 */           DataResultSet rset = new DataResultSet();
/* 1620 */           rset.init(tble);
/* 1621 */           ComponentLoader.addExtraComponentColumn(name, rset, data);
/* 1622 */           SharedObjects.putTable(name, rset);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1628 */       Set keys = clonedTableMap.keySet();
/* 1629 */       for (Iterator iter = keys.iterator(); iter.hasNext(); )
/*      */       {
/* 1631 */         String name = (String)iter.next();
/* 1632 */         if (SharedObjects.getTable(name) != null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1636 */         Table tble = (Table)clonedTableMap.get(name);
/* 1637 */         DataResultSet rset = new DataResultSet();
/* 1638 */         rset.init(tble);
/* 1639 */         ComponentLoader.addExtraComponentColumn(name, rset, null);
/* 1640 */         SharedObjects.putTable(name, rset);
/*      */       }
/* 1642 */       res.m_isFullyLoadedSharedResources = true;
/* 1643 */       m_resourcesAlreadyLoaded = true;
/*      */     }
/*      */     finally
/*      */     {
/*      */       ResourceContainer res;
/*      */       Object handler;
/*      */       IdcLocalizationStrings stringIndex;
/* 1647 */       ResourceContainer res = SharedObjects.getResources();
/* 1648 */       if (res.m_handler != null)
/*      */       {
/* 1650 */         Object handler = res.m_handler;
/* 1651 */         res.m_handler = null;
/* 1652 */         if (handler instanceof IdcLocalizationStrings)
/*      */         {
/* 1654 */           IdcLocalizationStrings stringIndex = (IdcLocalizationStrings)handler;
/* 1655 */           if (m_resourcesAlreadyLoaded)
/*      */           {
/*      */             try
/*      */             {
/* 1659 */               stringIndex.finishIncrementalUpdate();
/* 1660 */               LocaleResources.m_stringData = stringIndex;
/*      */ 
/* 1662 */               SubjectManager.notifyChanged("serverstartup");
/* 1663 */               res.resetStrings();
/* 1664 */               LocaleResources.m_stringObjMap = new HashMap();
/*      */             }
/*      */             catch (IOException e)
/*      */             {
/* 1668 */               throw new ServiceException(e);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void initOSSettingsHelper()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1679 */     int rc = PluginFilters.filter("initOSSettingsHelper", null, null, m_extendedLoader);
/* 1680 */     if (rc == 1) {
/*      */       return;
/*      */     }
/*      */ 
/* 1684 */     ServerOSSettingsHelper helper = new ServerOSSettingsHelper();
/* 1685 */     helper.m_substitutionMap = SharedObjects.getSecureEnvironment();
/* 1686 */     EnvUtils.m_osHelper = helper;
/*      */   }
/*      */ 
/*      */   public static void loadServerProcessID()
/*      */   {
/*      */     try
/*      */     {
/* 1694 */       NativeOsUtils nou = new NativeOsUtils();
/* 1695 */       int pid = nou.getPid();
/* 1696 */       SharedObjects.putEnvironmentValue("ServerProcessID", String.valueOf(pid));
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1700 */       Report.trace("system", null, t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadCustomEnvironmentValues() throws ServiceException
/*      */   {
/* 1706 */     Vector envData = ComponentLoader.m_environments;
/* 1707 */     int size = envData.size();
/* 1708 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1710 */       ComponentData data = (ComponentData)envData.elementAt(i);
/* 1711 */       String fileName = data.m_file;
/* 1712 */       loadPropertiesFromFileWithoutOverwrite(fileName, null);
/*      */     }
/*      */ 
/* 1715 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/*      */ 
/* 1719 */     Map idToComponent = ComponentLoader.m_idToComponent;
/* 1720 */     Vector installData = ComponentLoader.m_installData;
/* 1721 */     size = installData.size();
/* 1722 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1724 */       String instId = (String)installData.elementAt(i);
/*      */ 
/* 1726 */       boolean exists = false;
/* 1727 */       if ((instId == null) || (instId.length() <= 0))
/*      */         continue;
/* 1729 */       String instDir = dataDir + "components/" + instId;
/* 1730 */       String fileName = FileUtils.getAbsolutePath(instDir, "install.cfg");
/*      */ 
/* 1732 */       if (FileUtils.checkFile(fileName, true, false) == 0)
/*      */       {
/* 1734 */         loadPropertiesFromFileWithOverwrite(fileName, null);
/* 1735 */         exists = true;
/*      */       }
/*      */ 
/* 1738 */       fileName = FileUtils.getAbsolutePath(instDir, "config.cfg");
/* 1739 */       if (FileUtils.checkFile(fileName, true, false) == 0)
/*      */       {
/* 1741 */         loadPropertiesFromFileWithOverwrite(fileName, null);
/* 1742 */         exists = true;
/*      */       }
/* 1744 */       if (exists)
/*      */         continue;
/* 1746 */       String name = (String)idToComponent.get(instId);
/*      */       try
/*      */       {
/* 1749 */         DataBinder cmpInfo = (DataBinder)ComponentLoader.m_components.get(name);
/*      */ 
/* 1752 */         DataBinder binder = new DataBinder();
/* 1753 */         PageMerger pageMerger = new PageMerger(binder, null);
/*      */ 
/* 1755 */         ComponentInstaller installer = new ComponentInstaller();
/* 1756 */         installer.initEx(name, cmpInfo, binder, new HashMap());
/*      */ 
/* 1758 */         ComponentPreferenceData prefData = installer.retrieveDefaultPreferenceData(cmpInfo, name, binder, pageMerger, null, false, null);
/*      */ 
/* 1761 */         if (prefData != null)
/*      */         {
/* 1763 */           prefData.save();
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1768 */         Report.trace("system", "Unable to create component install and config files for the component" + name + " with install id " + instId, e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1777 */     String fileName = SystemUtils.getCfgFilePath();
/* 1778 */     loadPropertiesFromFileWithOverwrite(fileName, null);
/*      */ 
/* 1782 */     LegacyDirectoryLocator.buildRootDirectories();
/* 1783 */     LocaleLoader.updateLocaleSettingsFromConfig();
/*      */   }
/*      */ 
/*      */   public static void validateStandardDirectories()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1789 */     int rc = PluginFilters.filter("validateStandardDirectories", null, null, m_extendedLoader);
/*      */ 
/* 1791 */     if (rc == 1)
/*      */     {
/* 1793 */       Report.trace("system", "validateStandardDirectories filter returned finished, skipping validation.", null);
/*      */ 
/* 1795 */       return;
/*      */     }
/*      */ 
/* 1801 */     int maxDepth = SharedObjects.getEnvironmentInt("MaxCreationDepth", 9);
/*      */ 
/* 1804 */     String vaultDir = LegacyDirectoryLocator.getVaultDirectory();
/* 1805 */     FileUtils.checkOrCreateDirectory(vaultDir, maxDepth);
/* 1806 */     FileUtils.validateDirectory(vaultDir, IdcMessageFactory.lc("csUnableToAccessVault", new Object[0]), 256);
/*      */ 
/* 1808 */     String webDir = LegacyDirectoryLocator.getWebGroupRootDirectory("public");
/* 1809 */     FileUtils.checkOrCreateDirectory(webDir, maxDepth);
/* 1810 */     FileUtils.validateDirectory(webDir, IdcMessageFactory.lc("csUnableToAccessWebLayout", new Object[0]), 256);
/*      */ 
/* 1813 */     String alertId = "csWeblayout8dot3Error";
/* 1814 */     String alertMsg = "<$lcMessage('!csWeblayout8dot3Error')$>";
/* 1815 */     if (!SharedObjects.getEnvValueAsBoolean("AllowUnsafe8dot3Filenames", false))
/*      */     {
/*      */       try
/*      */       {
/* 1819 */         FileUtils.validateDirectory(webDir, IdcMessageFactory.lc("csUnableToAccessWebLayout", new Object[0]), 512);
/*      */ 
/* 1821 */         AlertUtils.deleteAlertSimple(alertId);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1825 */         Report.warning(null, null, e);
/* 1826 */         SharedObjects.putEnvironmentValue("Found8dot3Weblayout", "true");
/* 1827 */         AlertUtils.setAlertSimple(alertId, alertMsg, null, 1);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1832 */       AlertUtils.deleteAlertSimple(alertId);
/*      */     }
/*      */ 
/* 1835 */     String secureDir = LegacyDirectoryLocator.getWebGroupRootDirectory("secure");
/* 1836 */     String logDir = FileUtils.getAbsolutePath(secureDir, "logs");
/* 1837 */     FileUtils.checkOrCreateDirectory(logDir, maxDepth, 1);
/* 1838 */     FileUtils.validateDirectory(logDir, "!csUnableToAccessWebLayout");
/*      */ 
/* 1840 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/* 1841 */     FileUtils.checkOrCreateDirectory(dataDir, maxDepth);
/* 1842 */     FileUtils.validateDirectory(dataDir, "!csUnableToAccessDataDir");
/*      */ 
/* 1844 */     String subjectsDir = DirectoryLocator.getSubjectsDirectory();
/* 1845 */     FileUtils.checkOrCreateDirectory(subjectsDir, 2, 1);
/* 1846 */     FileUtils.validateDirectory(subjectsDir, "!csUnableToAccessSubjectMarkers");
/*      */ 
/* 1848 */     String userDir = DirectoryLocator.getUserCacheDir();
/* 1849 */     FileUtils.checkOrCreateDirectory(userDir, 2);
/*      */ 
/* 1851 */     if (!DirectoryLocator.hasSeparateUserPublishDir())
/*      */       return;
/* 1853 */     String userPublishDir = DirectoryLocator.getUserPublishCacheDir();
/* 1854 */     FileUtils.checkOrCreateDirectory(userPublishDir, 2);
/*      */   }
/*      */ 
/*      */   public static void initQueueProcessing(Workspace workspace)
/*      */     throws ServiceException
/*      */   {
/* 1863 */     if ((!SharedObjects.getEnvValueAsBoolean("IsAutoQueue", true)) || (!Features.checkLevel("ContentManagement", null)))
/*      */     {
/* 1866 */       return;
/*      */     }
/*      */ 
/* 1869 */     QueueProcessor.init();
/*      */ 
/* 1873 */     Workspace ws = workspace;
/* 1874 */     int defaultTimeout = SharedObjects.getTypedEnvironmentInt("IndexerAutoWorkInterval", 300, 24, 24);
/*      */ 
/* 1878 */     SubjectEventMonitor releasedDocumentsMonitor = new SubjectEventMonitor(ws, defaultTimeout)
/*      */     {
/*      */       protected int m_reportCount;
/*      */       protected int m_noChangeCount;
/*      */ 
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/*      */         try
/*      */         {
/* 1890 */           boolean[] isReleaseChanged = new boolean[1];
/* 1891 */           isReleaseChanged[0] = false;
/* 1892 */           if (QueueProcessor.processConvertedDocuments(this.val$ws, isReleaseChanged, IdcSystemLoader.m_cxt))
/*      */           {
/* 1894 */             SubjectManager.notifyChanged("documents");
/*      */           }
/* 1896 */           if (isReleaseChanged[0] == 0)
/*      */           {
/* 1901 */             this.m_noChangeCount += 1;
/* 1902 */             int maxIndexWaitCount = this.val$defaultTimeout / 3;
/* 1903 */             if ((IndexerMonitor.isWorkPending()) && (maxIndexWaitCount > 10))
/*      */             {
/* 1905 */               maxIndexWaitCount = 10;
/*      */             }
/* 1907 */             if (this.m_noChangeCount < maxIndexWaitCount)
/*      */             {
/* 1909 */               return false;
/*      */             }
/*      */           }
/* 1912 */           this.m_noChangeCount = 0;
/*      */ 
/* 1915 */           boolean isAuto = SharedObjects.getEnvValueAsBoolean("IsAutoSearch", false);
/*      */ 
/* 1917 */           boolean retVal = isReleaseChanged[0];
/* 1918 */           if ((isAuto) && 
/* 1922 */             (IndexerMonitor.startAutomatedWork()))
/*      */           {
/* 1924 */             retVal = false;
/*      */           }
/*      */ 
/* 1928 */           return retVal;
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/* 1932 */           if (this.m_reportCount % 1000 == 0)
/*      */           {
/* 1934 */             String msg = LocaleUtils.encodeMessage("csBackgroudProcessingError", null);
/* 1935 */             Report.error(null, msg, t);
/*      */           }
/*      */           else
/*      */           {
/* 1939 */             Report.trace("system", null, t);
/*      */           }
/* 1941 */           this.m_reportCount += 1;
/*      */         }
/* 1943 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 1950 */         boolean isAuto = SharedObjects.getEnvValueAsBoolean("IsAutoSearch", false);
/*      */ 
/* 1952 */         if (!isAuto)
/*      */           return;
/*      */         try
/*      */         {
/* 1956 */           IndexerMonitor.startAutomatedWork();
/*      */         }
/*      */         catch (ServiceException ignore)
/*      */         {
/* 1960 */           Report.trace("indexermonitor", null, ignore);
/*      */         }
/*      */       }
/*      */     };
/* 1966 */     SubjectManager.addSubjectMonitor("indexerwork", releasedDocumentsMonitor);
/*      */   }
/*      */ 
/*      */   public static void initSearchIndexer(Workspace workspace, boolean isAuto)
/*      */     throws ServiceException
/*      */   {
/* 1974 */     if ((!Features.checkLevel("Search", null)) || (m_isInitSearchIndexer))
/*      */     {
/* 1976 */       return;
/*      */     }
/*      */ 
/* 1979 */     SearchLoader.initEx(workspace);
/*      */ 
/* 1982 */     Observer obs = new Observer()
/*      */     {
/*      */       public void update(Observable o, Object arg)
/*      */       {
/* 1986 */         SubjectManager.notifyChanged("documents");
/*      */       }
/*      */     };
/* 1991 */     IndexerMonitor.init(DirectoryLocator.getSearchDirectory(), workspace, obs);
/*      */ 
/* 1994 */     SubjectCallback indexerStatusCallback = new SubjectCallback()
/*      */     {
/*      */       public void refresh(String subject)
/*      */       {
/* 1998 */         IndexerMonitor.updateIndexerConfig();
/*      */       }
/*      */ 
/*      */       public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*      */       {
/*      */         try
/*      */         {
/* 2006 */           IndexerMonitor.getIndexerStatus(binder);
/*      */         }
/*      */         catch (ServiceException ignore)
/*      */         {
/* 2010 */           ignore.printStackTrace();
/*      */         }
/*      */         catch (DataException ignore)
/*      */         {
/* 2014 */           ignore.printStackTrace();
/*      */         }
/*      */       }
/*      */     };
/* 2018 */     SubjectManager.registerCallback("indexerstatus", indexerStatusCallback);
/*      */ 
/* 2021 */     String engineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/* 2022 */     engineName = SearchIndexerUtils.getProperEngineName(engineName);
/*      */     try
/*      */     {
/* 2026 */       boolean hasIndexerChanged = SearchIndexerUtils.hasIndexerChanged(engineName);
/* 2027 */       boolean allowAutoRebuild = SharedObjects.getEnvValueAsBoolean("AllowAutoRebuildOnIndexerChange", true);
/*      */ 
/* 2029 */       boolean doRebuild = (hasIndexerChanged) && (allowAutoRebuild) && (!engineName.startsWith("DATABASE.METADATA"));
/*      */ 
/* 2031 */       if (doRebuild)
/*      */       {
/* 2034 */         int docCount = WorkspaceUtils.getRowCount("RevClasses", null, workspace);
/* 2035 */         if (docCount < 100)
/*      */         {
/* 2037 */           SubjectCallbackAdapter indexerRebuildCallback = new SubjectCallbackAdapter()
/*      */           {
/*      */             public void refresh(String subject)
/*      */               throws DataException, ServiceException
/*      */             {
/* 2042 */               Map defaultArgs = new HashMap();
/* 2043 */               IndexerMonitor.adjustIndexing("rebuild", 4, defaultArgs, true);
/*      */             }
/*      */           };
/* 2047 */           SubjectManager.registerCallback("indexerrebuild", indexerRebuildCallback);
/*      */         }
/*      */         else
/*      */         {
/* 2051 */           String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/*      */ 
/* 2053 */           IdcMessage msg = new IdcMessage("csIndexerRebuildWarning", new Object[] { activeIndex, engineName });
/* 2054 */           Report.warning("indexer", null, msg);
/* 2055 */           boolean disableRebuildIndexAlert = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("DisableRebuildIndexAlert"), false);
/*      */ 
/* 2057 */           boolean rebuildIndexAlertExists = AlertUtils.existsAlert("AutoIndexerRebuildWarning", 1);
/*      */ 
/* 2059 */           DataBinder alertBinder = new DataBinder();
/* 2060 */           alertBinder.putLocal("alertId", "AutoIndexerRebuildWarning");
/* 2061 */           alertBinder.putLocal("alertMsg", "<$lcMessage('" + LocaleUtils.encodeMessage(msg) + "')$>");
/*      */ 
/* 2064 */           if ((!disableRebuildIndexAlert) && (!rebuildIndexAlertExists))
/*      */           {
/* 2066 */             AlertUtils.setAlert(alertBinder);
/*      */           }
/*      */ 
/* 2070 */           if ((disableRebuildIndexAlert) && (rebuildIndexAlertExists))
/*      */           {
/* 2072 */             AlertUtils.deleteAlert(alertBinder);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 2080 */       Report.error("indexer", "csIndexerRebuildOnStartupError", t);
/*      */     }
/*      */ 
/* 2084 */     SubjectEventMonitor searchMonitor = new SubjectEventMonitor()
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 2088 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 2094 */         if (!isExternal)
/*      */           return;
/* 2096 */         IndexerMonitor.handleExternalIndexEvent(curTime);
/*      */       }
/*      */     };
/* 2101 */     SubjectManager.addSubjectMonitor("indexer", searchMonitor);
/*      */ 
/* 2103 */     if (isAuto)
/*      */     {
/* 2105 */       String curValue = SharedObjects.getEnvironmentValue("IsAutoSearch");
/* 2106 */       if (curValue == null)
/*      */       {
/* 2108 */         SharedObjects.putEnvironmentValue("IsAutoSearch", "1");
/*      */       }
/*      */     }
/* 2111 */     if ((!isAuto) || (!SharedObjects.getEnvValueAsBoolean("IsAutoQueue", true)))
/*      */     {
/* 2113 */       SubjectEventMonitor doNothing = new SubjectEventMonitor()
/*      */       {
/*      */         public boolean checkForChange(String subject, long curTime)
/*      */         {
/* 2117 */           return false;
/*      */         }
/*      */ 
/*      */         public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */         {
/*      */         }
/*      */       };
/* 2126 */       IndexerMonitor.setDisableAutoIndexing(true);
/* 2127 */       SubjectManager.addSubjectMonitor("indexerwork", doNothing);
/*      */     }
/*      */ 
/* 2130 */     boolean releaseConnectionsOnReleaseDocs = !SharedObjects.getEnvValueAsBoolean("NoSearchConnectionCloseOnDocChange", false);
/*      */ 
/* 2132 */     SubjectEventMonitor releasedDocsMonitor = new Object(releaseConnectionsOnReleaseDocs)
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 2136 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 2142 */         if (this.val$releaseConnectionsOnReleaseDocs)
/*      */         {
/* 2144 */           Object obj = SharedObjects.getObject("globalObjects", "SearchManager");
/* 2145 */           if (obj != null)
/*      */           {
/* 2147 */             SearchManager sManager = (SearchManager)obj;
/*      */             try
/*      */             {
/* 2150 */               sManager.forceRefreshCurrentConnections(false);
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/* 2154 */               e.printStackTrace();
/*      */             }
/*      */           }
/*      */         }
/* 2158 */         SearchLoader.processReleasedDocumentsChange(null, null, SearchCache.F_LOCAL_CACHE_CHANGED);
/*      */       }
/*      */     };
/* 2161 */     SubjectManager.addSubjectMonitor("releaseddocuments", releasedDocsMonitor);
/*      */ 
/* 2163 */     SubjectEventMonitor usersMonitor = new SubjectEventMonitor()
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 2167 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 2173 */         SearchLoader.processUsersChange(null, null);
/*      */       }
/*      */     };
/* 2176 */     SubjectManager.addSubjectMonitor("users", usersMonitor);
/*      */ 
/* 2178 */     SubjectCallback searchApiCallback = new SearchApiSubjectCallback();
/* 2179 */     SubjectManager.registerCallback("searchapi", searchApiCallback);
/* 2180 */     SubjectEventMonitor searchApiMonitor = new Object()
/*      */     {
/*      */       protected int m_checkCount;
/*      */ 
/*      */       public boolean checkForChange(String subject, long curTime) {
/* 2185 */         if (this.m_checkCount++ % 5 == 0)
/*      */         {
/* 2187 */           Object obj = SharedObjects.getObject("globalObjects", "SearchManager");
/* 2188 */           if (obj != null)
/*      */           {
/* 2190 */             SearchManager sManager = (SearchManager)obj;
/*      */             try
/*      */             {
/* 2193 */               sManager.forceRefreshCurrentConnections(true);
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/* 2198 */               Report.error(null, null, e);
/*      */             }
/*      */           }
/*      */         }
/* 2202 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/*      */       }
/*      */     };
/* 2211 */     SubjectManager.addSubjectMonitor("searchapi", searchApiMonitor);
/*      */     try
/*      */     {
/* 2217 */       PluginFilters.filter("initSearchIndexer", workspace, null, m_extendedLoader);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2221 */       throw new ServiceException("!csIndexerInitFilterError", e);
/*      */     }
/*      */ 
/* 2224 */     m_isInitSearchIndexer = true;
/*      */   }
/*      */ 
/*      */   public static void initProviders(int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2234 */     if ((flags & 0x2) == 0)
/*      */     {
/* 2236 */       createSystemProviders(flags);
/*      */     }
/* 2238 */     loadProviderConfiguration();
/* 2239 */     loadProviders();
/*      */   }
/*      */ 
/*      */   public static void createSystemProviders(int flags) throws DataException, ServiceException
/*      */   {
/* 2244 */     if (PluginFilters.filter("createSystemProviders", null, null, m_extendedLoader) == 1)
/*      */     {
/* 2247 */       return;
/*      */     }
/* 2249 */     if (Features.checkLevel("JDBC", null))
/*      */     {
/* 2251 */       createSystemDatabase();
/* 2252 */       if (SharedObjects.getEnvValueAsBoolean("IsUserDatabase", false))
/*      */       {
/* 2254 */         createSystemUserDatabase();
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2259 */       Report.debug("startup", "JDBC feature not present, not creating systemdatabase provider.", null);
/*      */     }
/*      */ 
/* 2263 */     boolean traceSockets = SharedObjects.getEnvValueAsBoolean("TraceSocketRequests", false);
/* 2264 */     if (traceSockets)
/*      */     {
/* 2266 */       SystemUtils.reportDeprecatedUsage("TraceSocketRequests is deprecated.  Activate the socketrequests tracing section.");
/*      */ 
/* 2269 */       SystemUtils.addAsDefaultTrace("socketrequests");
/*      */     }
/*      */ 
/* 2272 */     if (SharedObjects.getEnvValueAsBoolean("IsJspServerEnabled", false))
/*      */     {
/* 2274 */       DataBinder binder = new DataBinder();
/* 2275 */       binder.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/* 2276 */       binder.putLocal("ProviderName", "SystemJspServer");
/* 2277 */       binder.putLocal("ProviderClass", "intradoc.server.jsp.JspProvider");
/* 2278 */       binder.putLocal("ProviderDescription", "System Jsp Server");
/* 2279 */       binder.putLocal("ProviderType", "jsp");
/* 2280 */       binder.putLocal("IsSystemProvider", "1");
/* 2281 */       Providers.addProviderData("SystemJspServer", binder);
/*      */     }
/* 2283 */     if ((flags & 0x1) == 0)
/*      */       return;
/* 2285 */     String[][] providerClassKeys = (String[][])(String[][])m_extendedLoader.getCachedObject("SystemIncomingProviderKeys");
/*      */ 
/* 2287 */     if (providerClassKeys == null)
/*      */     {
/* 2289 */       providerClassKeys = new String[][] { { "SystemIncomingProviderClass", "ProviderClass", "intradoc.provider.SocketIncomingProvider" }, { "SystemIncomingProviderConnection", "ProviderConnection", "intradoc.provider.SocketIncomingConnection" } };
/*      */     }
/*      */ 
/* 2295 */     initSystemSocketProvider(providerClassKeys);
/*      */   }
/*      */ 
/*      */   public static void createSystemDatabase()
/*      */   {
/* 2305 */     DataBinder data = new DataBinder();
/* 2306 */     data.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/*      */ 
/* 2308 */     boolean isJdbc = SharedObjects.getEnvValueAsBoolean("IsJdbc", true);
/*      */ 
/* 2311 */     if (SharedObjects.getEnvValueAsBoolean("IsJdbcQueryTrace", false))
/*      */     {
/* 2313 */       SystemUtils.addAsDefaultTrace("systemdatabase");
/* 2314 */       if (SharedObjects.getEnvValueAsBoolean("IsJdbcLockTrace", false))
/*      */       {
/* 2316 */         SystemUtils.reportDeprecatedUsage("IsJdbcLockTrace is deprecated.  Activate systemdatabase tracing and verbose tracing.");
/*      */ 
/* 2319 */         SystemUtils.m_verbose = true;
/*      */       }
/*      */       else
/*      */       {
/* 2323 */         SystemUtils.reportDeprecatedUsage("IsJdbcLockTrace is deprecated.  Activate systemdatabase tracing.");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2330 */     String workspaceClass = SharedObjects.getEnvironmentValue("SystemDatabaseProviderClass");
/* 2331 */     if (workspaceClass == null)
/*      */     {
/* 2333 */       workspaceClass = "intradoc.dao.DaoWorkspace";
/* 2334 */       if (isJdbc)
/*      */       {
/* 2336 */         workspaceClass = "intradoc.jdbc.JdbcWorkspace";
/*      */       }
/*      */     }
/*      */ 
/* 2340 */     data.putLocal("ProviderName", "SystemDatabase");
/* 2341 */     data.putLocal("ProviderDescription", "System Database");
/* 2342 */     data.putLocal("ProviderClass", workspaceClass);
/* 2343 */     data.putLocal("ProviderType", "database");
/* 2344 */     data.putLocal("IsSystemProvider", "1");
/*      */ 
/* 2346 */     Providers.addProviderData("SystemDatabase", data);
/*      */   }
/*      */ 
/*      */   public static void createSystemUserDatabase()
/*      */   {
/* 2352 */     DataBinder data = new DataBinder();
/* 2353 */     data.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/*      */ 
/* 2355 */     boolean isJdbc = SharedObjects.getEnvValueAsBoolean("IsJdbc", true);
/*      */ 
/* 2358 */     String workspaceClass = SharedObjects.getEnvironmentValue("UserDatabaseProviderClass");
/* 2359 */     if (workspaceClass == null)
/*      */     {
/* 2361 */       workspaceClass = "intradoc.dao.DaoWorkspace";
/* 2362 */       if (isJdbc)
/*      */       {
/* 2364 */         workspaceClass = "intradoc.jdbc.JdbcWorkspace";
/*      */       }
/*      */     }
/*      */ 
/* 2368 */     data.putLocal("ProviderName", "SystemUserDatabase");
/* 2369 */     data.putLocal("ProviderDescription", "System User Database");
/* 2370 */     data.putLocal("ProviderClass", workspaceClass);
/* 2371 */     data.putLocal("ProviderType", "database");
/* 2372 */     data.putLocal("IsSystemProvider", "1");
/*      */ 
/* 2375 */     String value = SharedObjects.getEnvironmentValue("UserJdbcConnectionString");
/* 2376 */     if (value != null)
/*      */     {
/* 2378 */       data.putLocal("JdbcConnectionString", value);
/*      */     }
/*      */ 
/* 2381 */     value = SharedObjects.getEnvironmentValue("UserJdbcDriver");
/* 2382 */     if (value != null)
/*      */     {
/* 2384 */       data.putLocal("JdbcDriver", value);
/*      */     }
/*      */ 
/* 2387 */     value = SharedObjects.getEnvironmentValue("UserJdbcUser");
/* 2388 */     if (value != null)
/*      */     {
/* 2390 */       data.putLocal("JdbcUser", value);
/*      */     }
/*      */ 
/* 2393 */     value = SharedObjects.getEnvironmentValue("UserJdbcPassword");
/* 2394 */     if (value != null)
/*      */     {
/* 2396 */       data.putLocal("JdbcPassword", value);
/*      */     }
/*      */ 
/* 2399 */     value = SharedObjects.getEnvironmentValue("UserJdbcPasswordEncoding");
/* 2400 */     if (value != null)
/*      */     {
/* 2402 */       data.putLocal("JdbcPasswordEncoding", value);
/*      */     }
/*      */ 
/* 2405 */     Providers.addProviderData("SystemUserDatabase", data);
/*      */   }
/*      */ 
/*      */   public static void initSystemSocketProvider(String[][] classKeys)
/*      */   {
/* 2411 */     DataBinder cd = new DataBinder(SharedObjects.getSecureEnvironment());
/*      */ 
/* 2414 */     int port = SharedObjects.getEnvironmentInt("IntradocServerPort", 0);
/* 2415 */     cd.putLocal("ServerPort", String.valueOf(port));
/* 2416 */     String bindAddress = SharedObjects.getEnvironmentValue("IdcServerBindAddress");
/*      */ 
/* 2418 */     if (bindAddress != null)
/*      */     {
/* 2420 */       cd.putLocal("ServerBindAddress", bindAddress);
/*      */     }
/* 2422 */     String nodeAddress = SharedObjects.getEnvironmentValue("ClusterNodeAddress");
/*      */ 
/* 2424 */     if (nodeAddress != null)
/*      */     {
/* 2426 */       cd.putLocal("ClusterNodeAddress", nodeAddress);
/*      */     }
/* 2428 */     String socketQueue = SharedObjects.getEnvironmentValue("IdcServerSocketQueueDepth");
/*      */ 
/* 2430 */     if (socketQueue != null)
/*      */     {
/* 2432 */       cd.putLocal("ServerQueueDepth", socketQueue);
/*      */     }
/*      */ 
/* 2435 */     cd.putLocal("ProviderName", "SystemServerSocket");
/* 2436 */     cd.putLocal("ProviderDescription", "System Server Socket");
/*      */ 
/* 2438 */     for (int i = 0; i < classKeys.length; ++i)
/*      */     {
/* 2440 */       String value = SharedObjects.getEnvironmentValue(classKeys[i][0]);
/* 2441 */       if (value == null)
/*      */       {
/* 2443 */         value = classKeys[i][2];
/*      */       }
/* 2445 */       cd.putLocal(classKeys[i][1], value);
/*      */     }
/* 2447 */     cd.putLocal("ProviderType", "incoming");
/* 2448 */     cd.putLocal("IsSystemProvider", "1");
/*      */ 
/* 2450 */     Providers.addProviderData("SystemServerSocket", cd);
/*      */   }
/*      */ 
/*      */   public static void loadProviderConfiguration()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2456 */     String dir = DirectoryLocator.getProviderDirectory();
/*      */ 
/* 2459 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 0, true);
/*      */ 
/* 2461 */     FileUtils.reserveDirectory(dir);
/*      */     try
/*      */     {
/* 2464 */       ProviderFileUtils.init(dir);
/* 2465 */       ProviderFileUtils.loadProviders();
/*      */ 
/* 2468 */       DataResultSet provSet = Providers.getResultSet();
/* 2469 */       SharedObjects.putTable("Providers", provSet);
/*      */ 
/* 2471 */       String[][] table = ResultSetUtils.createStringTable(provSet, Providers.COLUMNS);
/* 2472 */       int num = table.length;
/* 2473 */       for (int i = 0; i < num; ++i)
/*      */       {
/* 2475 */         String name = table[i][0];
/* 2476 */         String description = table[i][1];
/* 2477 */         DataBinder data = null;
/*      */         try
/*      */         {
/* 2480 */           data = ProviderFileUtils.readProviderFile(name, false);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 2484 */           String msg = LocaleUtils.encodeMessage("csErrorReadingProviderDef", null, name);
/*      */ 
/* 2486 */           Report.error(null, msg, e);
/*      */         }
/*      */         finally
/*      */         {
/* 2490 */           if (data == null)
/*      */           {
/* 2492 */             data = new DataBinder();
/* 2493 */             data.putLocal("IsEnabled", "0");
/*      */           }
/* 2495 */           data.setEnvironment(new Properties(SharedObjects.getSecureEnvironment()));
/* 2496 */           data.putLocal("ProviderName", name);
/* 2497 */           data.putLocal("ProviderDescription", description);
/* 2498 */           Providers.addProviderData(name, data);
/*      */         }
/*      */       }
/* 2501 */       FileStoreProviderLoader.initDefaultFileStore(m_cxt);
/*      */     }
/*      */     finally
/*      */     {
/* 2506 */       FileUtils.releaseDirectory(dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadProviders() throws DataException, ServiceException
/*      */   {
/* 2512 */     Vector provData = Providers.getProviderDataList();
/*      */ 
/* 2515 */     HashMap componentDataBundle = new HashMap();
/* 2516 */     componentDataBundle.put("ComponentQueries", ComponentLoader.m_queries);
/* 2517 */     componentDataBundle.put("ComponentResources", ComponentLoader.m_resources);
/* 2518 */     componentDataBundle.put("ComponentServices", ComponentLoader.m_services);
/* 2519 */     componentDataBundle.put("ComponentTemplates", ComponentLoader.m_templates);
/* 2520 */     componentDataBundle.put("Components", ComponentLoader.m_components);
/*      */ 
/* 2522 */     int num = provData.size();
/* 2523 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2525 */       DataBinder data = (DataBinder)provData.elementAt(i);
/*      */ 
/* 2529 */       boolean isEnabled = StringUtils.convertToBool(data.getLocal("IsEnabled"), true);
/*      */ 
/* 2531 */       Provider prov = new Provider(data, componentDataBundle);
/* 2532 */       String name = prov.getName();
/* 2533 */       if (isEnabled)
/*      */       {
/* 2535 */         boolean isRetry = false;
/*      */         try
/*      */         {
/* 2538 */           prov.markState("enabled");
/* 2539 */           prov.init();
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 2544 */           if (prov.isSystemProvider())
/*      */           {
/* 2546 */             isRetry = name.equals("DefaultFileStore");
/* 2547 */             if (!isRetry)
/*      */             {
/* 2549 */               throw new ServiceException(e, "csProviderUnableToInitialize", new Object[] { prov.getName() });
/*      */             }
/*      */           }
/* 2552 */           prov.markErrorState(-26, e);
/* 2553 */           String msg = LocaleUtils.encodeMessage("csProviderConfigError", null, name);
/*      */ 
/* 2555 */           Report.error(null, msg, e);
/*      */         }
/* 2557 */         if (isRetry)
/*      */         {
/*      */           try
/*      */           {
/* 2561 */             DataBinder binder = FileStoreProviderLoader.createFileStoreDefaults(null);
/* 2562 */             prov.setProviderData(binder);
/* 2563 */             prov.init();
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 2567 */             String msg = LocaleUtils.encodeMessage("csProviderUnableToInitialize", null, prov.getName());
/*      */ 
/* 2569 */             throw new ServiceException(msg, e);
/*      */           }
/*      */         }
/*      */ 
/* 2573 */         Providers.addProvider(name, prov);
/*      */       }
/*      */       else
/*      */       {
/* 2577 */         prov.markState("disabled");
/*      */       }
/* 2579 */       Providers.addToAllProviderList(name, prov);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void registerProviders() throws DataException, ServiceException
/*      */   {
/* 2585 */     Vector providers = Providers.getProviderList();
/* 2586 */     int num = providers.size();
/* 2587 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2589 */       Provider prov = (Provider)providers.elementAt(i);
/* 2590 */       if (prov.isInError()) continue; if (!prov.isEnabled())
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 2595 */       Exception exception = null;
/* 2596 */       String name = prov.getName();
/*      */       try
/*      */       {
/* 2599 */         prov.configureProvider();
/* 2600 */         Providers.registerProvider(name, prov);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 2604 */         exception = e;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 2608 */         exception = e;
/*      */       }
/*      */ 
/* 2611 */       if (exception == null)
/*      */         continue;
/* 2613 */       prov.markErrorState(-26, exception);
/* 2614 */       String msg = LocaleUtils.encodeMessage("csUnableToRegisterProvider", null, name);
/*      */ 
/* 2616 */       SharedUtils.handleCommonException(exception, msg, (prov.isSystemProvider()) ? 0 : 3);
/*      */     }
/*      */ 
/* 2623 */     String relativeCgiUrl = DirectoryLocator.getEnterpriseCgiWebUrl(false);
/* 2624 */     boolean allowsMultipleMastersSingleHost = relativeCgiUrl.endsWith("/idcplg");
/* 2625 */     String val = SharedObjects.getEnvironmentValue("AllowsMultipleMastersSingleHost");
/* 2626 */     if ((val == null) && (allowsMultipleMastersSingleHost))
/*      */     {
/* 2628 */       SharedObjects.putEnvironmentValue("AllowsMultipleMastersSingleHost", "1");
/*      */     }
/*      */ 
/* 2631 */     String realm = SharedObjects.getEnvironmentValue("IdcRealm");
/*      */ 
/* 2633 */     if (realm != null)
/*      */       return;
/* 2635 */     realm = SharedObjects.getEnvironmentValue("IntradocRealm");
/* 2636 */     if ((realm == null) && 
/* 2640 */       (allowsMultipleMastersSingleHost))
/*      */     {
/* 2642 */       if (SharedObjects.getEnvValueAsBoolean("AllowsLocalizedIdcRealm", false))
/*      */       {
/* 2644 */         String msg = LocaleUtils.encodeMessage("csSecurityRealm", null, relativeCgiUrl);
/* 2645 */         realm = LocaleResources.localizeMessage(msg, null);
/*      */       }
/*      */       else
/*      */       {
/* 2649 */         realm = "Idc Security " + relativeCgiUrl;
/*      */       }
/*      */     }
/*      */ 
/* 2653 */     if (realm == null)
/*      */       return;
/* 2655 */     SharedObjects.putEnvironmentValue("IdcRealm", realm);
/*      */   }
/*      */ 
/*      */   public static void startProviders(boolean isOnDemand)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2662 */     prepareStartMonitorProviders(isOnDemand);
/* 2663 */     startMonitoringProviders();
/*      */   }
/*      */ 
/*      */   public static void prepareStartMonitorProviders(boolean isOnDemand)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2669 */     Vector providers = Providers.getProviderList();
/* 2670 */     Vector incomingProviders = new Vector();
/* 2671 */     for (Iterator i$ = providers.iterator(); i$.hasNext(); ) { Object provObj = i$.next();
/*      */ 
/* 2673 */       Provider prov = (Provider)provObj;
/* 2674 */       if (prov.isProviderOfType("incoming"))
/*      */       {
/* 2678 */         incomingProviders.add(prov);
/*      */       }
/*      */ 
/* 2681 */       startProvider(prov, isOnDemand, false); }
/*      */ 
/* 2683 */     for (Iterator i$ = incomingProviders.iterator(); i$.hasNext(); ) { Object provObj = i$.next();
/*      */ 
/* 2685 */       startProvider((Provider)provObj, isOnDemand, true); }
/*      */ 
/* 2687 */     Report.trace("startup", "Started all providers", null);
/*      */ 
/* 2689 */     OutgoingProviderMonitor.init(isOnDemand);
/*      */ 
/* 2691 */     boolean isContentManagement = Features.checkLevel("ContentManagement", null);
/* 2692 */     if (isContentManagement)
/*      */     {
/* 2697 */       boolean isServerMode = SharedObjects.getEnvValueAsBoolean("IsServerMode", false);
/* 2698 */       boolean isStartRefineryThreadsWithApps = SharedObjects.getEnvValueAsBoolean("StartStandAloneRefineryQueueMonitorThreads", false);
/* 2699 */       if ((isServerMode) || (isStartRefineryThreadsWithApps))
/*      */       {
/* 2701 */         boolean startRefineryThread = SharedObjects.getEnvValueAsBoolean("StartRefineryQueueMonitorThreads", true);
/*      */ 
/* 2703 */         if (startRefineryThread)
/*      */         {
/* 2706 */           RefineryUtils.startRefineryQueueMonitor();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2711 */     if (!isOnDemand)
/*      */     {
/* 2714 */       getOrCreatePeriodicTasks();
/*      */     }
/*      */ 
/* 2718 */     String msg = LocaleUtils.encodeMessage(VersionInfo.getProductCopyright(), null);
/* 2719 */     msg = LocaleUtils.appendMessage(msg, "\n");
/* 2720 */     String versionInfoKey = SharedObjects.getEnvironmentValue("VersionReportStringKey");
/* 2721 */     if ((versionInfoKey == null) || (versionInfoKey.length() == 0))
/*      */     {
/* 2723 */       versionInfoKey = "csProductVersion";
/*      */     }
/* 2725 */     msg = LocaleUtils.encodeMessage(versionInfoKey, msg, VersionInfo.getProductVersion());
/* 2726 */     msg = LocaleResources.localizeMessage(msg, null);
/* 2727 */     SystemUtils.outln(msg);
/*      */   }
/*      */ 
/*      */   public static void startProvider(Provider prov, boolean isOnDemand, boolean isIncoming)
/*      */     throws ServiceException
/*      */   {
/* 2733 */     if ((prov.isInError()) || (!prov.isEnabled()) || ((isOnDemand) && (prov.isProviderOfType("outgoing"))))
/*      */     {
/* 2736 */       return;
/*      */     }
/*      */     try
/*      */     {
/* 2740 */       Report.trace("startup", "Starting provider " + prov.getName(), null);
/* 2741 */       prov.startProvider(isOnDemand);
/*      */ 
/* 2743 */       String str = prov.getReportString("startup");
/* 2744 */       if (str != null)
/*      */       {
/* 2746 */         SystemUtils.outln(LocaleResources.localizeMessage(str, null));
/*      */       }
/* 2748 */       DataBinder providerData = prov.getProviderData();
/* 2749 */       if (providerData.getLocal("ProviderName").equals("SystemServerSocket"))
/*      */       {
/* 2751 */         ServerSocket socket = (ServerSocket)prov.getProviderObject("ServerSocket");
/* 2752 */         int port = socket.getLocalPort();
/* 2753 */         SharedObjects.putEnvironmentValue("IntradocServerPort", "" + port);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2758 */       int errCode = -25;
/* 2759 */       if (e instanceof ServiceException)
/*      */       {
/* 2761 */         ServiceException se = (ServiceException)e;
/* 2762 */         errCode = se.m_errorCode;
/*      */       }
/*      */ 
/* 2765 */       boolean isSystemProvider = prov.isSystemProvider();
/* 2766 */       if (isSystemProvider)
/*      */       {
/* 2768 */         String msg = LocaleUtils.encodeMessage("csProviderUnableToStartSystem", null, prov.getName());
/*      */ 
/* 2771 */         ServiceException se = new ServiceException(msg, e);
/* 2772 */         throw se;
/*      */       }
/* 2774 */       prov.markErrorState(errCode, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void startMonitoringProviders()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2781 */     m_extendedLoader.extraAfterProvidersStartedInit();
/* 2782 */     OutgoingProviderMonitor.startMonitor();
/*      */   }
/*      */ 
/*      */   public static void initReplicationData() throws ServiceException
/*      */   {
/* 2787 */     ArchiveUtils.createDefaultCollection();
/* 2788 */     ReplicationData.loadFromFile();
/*      */   }
/*      */ 
/*      */   public static void initArchiver(Workspace ws, boolean isAuto)
/*      */     throws ServiceException
/*      */   {
/* 2794 */     if ((m_isInitArchive) || (!Features.checkLevel("ContentManagement", null)))
/*      */     {
/* 2796 */       return;
/*      */     }
/*      */ 
/* 2800 */     String dir = DirectoryLocator.getCollectionsDirectory();
/*      */ 
/* 2802 */     ArchiverMonitor.init(dir, ws, null);
/* 2803 */     MonikerWatcher.init();
/*      */ 
/* 2806 */     String isAutoStr = SharedObjects.getEnvironmentValue("IsAutoArchiver");
/* 2807 */     if ((isAuto) && (isAutoStr == null))
/*      */     {
/* 2809 */       isAutoStr = "1";
/* 2810 */       SharedObjects.putEnvironmentValue("IsAutoArchiver", isAutoStr);
/*      */     }
/*      */ 
/* 2813 */     SubjectCallbackAdapter colCallback = new ArchiveCollectionsSubjectCallback();
/* 2814 */     colCallback.setLists(new String[] { "ArchiveCollections" }, null);
/* 2815 */     SubjectManager.registerCallback("collections", colCallback);
/*      */ 
/* 2818 */     boolean isTraceTransfer = SharedObjects.getEnvValueAsBoolean("TraceArchiveTransferMonitor", false);
/* 2819 */     if (isTraceTransfer)
/*      */     {
/* 2821 */       SystemUtils.addAsDefaultTrace("transfermonitor");
/*      */     }
/* 2823 */     boolean isArchiveTrace = SharedObjects.getEnvValueAsBoolean("ArchiverDebugTrace", false);
/* 2824 */     if (isArchiveTrace)
/*      */     {
/* 2826 */       SystemUtils.addAsDefaultTrace("archiver");
/*      */     }
/* 2828 */     boolean traceLocks = SharedObjects.getEnvValueAsBoolean("ArchiverTraceLocks", false);
/* 2829 */     if (traceLocks)
/*      */     {
/* 2831 */       SystemUtils.addAsDefaultTrace("archiverlocks");
/* 2832 */       boolean traceLockMaintenance = SharedObjects.getEnvValueAsBoolean("ArchiverTraceLockMaintenance", false);
/* 2833 */       if (traceLockMaintenance)
/*      */       {
/* 2835 */         SystemUtils.m_verbose = true;
/*      */       }
/*      */     }
/*      */ 
/* 2839 */     if (StringUtils.convertToBool(isAutoStr, isAuto))
/*      */     {
/* 2843 */       SubjectEventMonitor replicationMonitor = new SubjectEventMonitor()
/*      */       {
/*      */         public boolean checkForChange(String subject, long curTime)
/*      */         {
/* 2847 */           boolean hasWorkToDo = false;
/*      */           try
/*      */           {
/* 2850 */             hasWorkToDo = ArchiverMonitor.checkForWork();
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/* 2855 */             String msg = LocaleUtils.encodeMessage("csUnableToCheckForWork", t.getMessage());
/*      */ 
/* 2857 */             Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*      */           }
/* 2859 */           return hasWorkToDo;
/*      */         }
/*      */ 
/*      */         public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */         {
/* 2865 */           ArchiverMonitor.doWork();
/*      */         }
/*      */       };
/* 2869 */       SubjectManager.addSubjectMonitor("archiver", replicationMonitor);
/*      */     }
/* 2871 */     m_isInitArchive = true;
/*      */   }
/*      */ 
/*      */   public static void initWorkQueue(Workspace workspace)
/*      */     throws ServiceException
/*      */   {
/* 2877 */     if (!Features.checkLevel("ContentManagement", null))
/*      */     {
/* 2879 */       return;
/*      */     }
/*      */ 
/* 2883 */     WorkMonitor.init(workspace, null);
/*      */ 
/* 2886 */     SubjectEventMonitor workMonitor = new SubjectEventMonitor()
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 2890 */         return WorkMonitor.isWorkToDo(curTime);
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 2896 */         if (isExternal)
/*      */         {
/* 2898 */           WorkMonitor.handleExternal(curTime);
/*      */         }
/*      */         else
/*      */         {
/* 2902 */           WorkMonitor.watchWork(curTime);
/*      */         }
/*      */       }
/*      */     };
/* 2907 */     SubjectManager.addSubjectMonitor("work", workMonitor);
/*      */   }
/*      */ 
/*      */   public static void initAlertMonitor() throws ServiceException, DataException
/*      */   {
/* 2912 */     AlertUtils.init();
/*      */ 
/* 2915 */     SubjectEventMonitor alertmonitor = new SubjectEventMonitor()
/*      */     {
/*      */       public boolean checkForChange(String subject, long curTime)
/*      */       {
/* 2919 */         return false;
/*      */       }
/*      */ 
/*      */       public void handleChange(String subject, boolean isExternal, long counter, long curTime)
/*      */       {
/* 2925 */         AlertUtils.reloadAlertData();
/*      */       }
/*      */     };
/* 2929 */     SubjectManager.addSubjectMonitor("alertmonitor", alertmonitor);
/*      */ 
/* 2931 */     clearServerRestartAlert();
/*      */   }
/*      */ 
/*      */   public static void clearServerRestartAlert() throws DataException, ServiceException
/*      */   {
/* 2936 */     String alertId = "csRestartServerToApplyChanges";
/* 2937 */     boolean componentUpdateAlertExists = AlertUtils.existsAlert(alertId, 1);
/* 2938 */     if (!componentUpdateAlertExists)
/*      */       return;
/* 2940 */     AlertUtils.deleteAlertSimple(alertId);
/*      */   }
/*      */ 
/*      */   public static void initWorkflow(Workspace ws, boolean isAuto)
/*      */   {
/* 2946 */     if (!Features.checkLevel("ContentManagement", null))
/*      */     {
/* 2948 */       return;
/*      */     }
/*      */     try
/*      */     {
/* 2952 */       WfDesignManager.init();
/* 2953 */       WfCompanionManager.init();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2957 */       Report.error(null, "!csUnableToCreateWorkflowDir", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void initProjects(Workspace workspace)
/*      */     throws ServiceException, DataException
/*      */   {
/* 2964 */     String dir = DirectoryLocator.getProjectDirectory();
/* 2965 */     FileUtils.checkOrCreateDirectory(dir, 2);
/*      */ 
/* 2968 */     SubjectCallbackAdapter prjCallback = new RegisteredProjectsSubjectCallback();
/* 2969 */     prjCallback.setWorkspace(workspace);
/* 2970 */     prjCallback.setLists(new String[] { "RegisteredProjects" }, null);
/* 2971 */     SubjectManager.registerCallback("projects", prjCallback);
/*      */ 
/* 2973 */     Projects.init();
/*      */   }
/*      */ 
/*      */   public static void initCollaborations(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2979 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/* 2982 */       loadDatabaseFieldInfo(workspace, "SELECT * from Collaborations WHERE dClbraName='0'");
/*      */ 
/* 2984 */       String dir = DirectoryLocator.getAppDataDirectory();
/* 2985 */       Collaborations.init(dir);
/*      */ 
/* 2988 */       String sGroups = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 2989 */       if (sGroups == null)
/*      */       {
/* 2993 */         SharedObjects.putEnvironmentValue("AllSpecialAuthGroups", "1");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2999 */     if (SecurityUtils.m_useEntitySecurity)
/*      */     {
/* 3006 */       if (SharedObjects.getEnvironmentValue("UseSearchCache") == null)
/*      */       {
/* 3008 */         SharedObjects.putEnvironmentValue("UseSearchCache", "0");
/*      */       }
/*      */ 
/*      */       boolean useZonedSecurityForACLs;
/*      */       try
/*      */       {
/* 3015 */         IndexerConfig config = SearchIndexerUtils.getIndexerConfig(null, "update");
/* 3016 */         useZonedSecurityForACLs = config.getBoolean("IsZoneRequiredForEntitySecurity", false);
/*      */       }
/*      */       catch (DataException de)
/*      */       {
/* 3020 */         Report.warning(null, de, "csIndexerUnableToLoadConfig", new Object[0]);
/* 3021 */         useZonedSecurityForACLs = false;
/*      */       }
/* 3023 */       if (useZonedSecurityForACLs)
/*      */       {
/* 3025 */         String zonedSecurityFields = SharedObjects.getEnvironmentValue("ZonedSecurityFields");
/* 3026 */         Vector fields = StringUtils.parseArray(zonedSecurityFields, ',', ',');
/* 3027 */         if (!fields.contains("xClbraUserList"))
/*      */         {
/* 3029 */           fields.add("xClbraUserList");
/*      */         }
/* 3031 */         if (!fields.contains("xClbraAliasList"))
/*      */         {
/* 3033 */           fields.add("xClbraAliasList");
/*      */         }
/* 3035 */         zonedSecurityFields = StringUtils.createString(fields, ',', ',');
/* 3036 */         SharedObjects.putEnvironmentValue("ZonedSecurityFields", zonedSecurityFields);
/*      */       }
/*      */     }
/*      */ 
/* 3040 */     PluginFilters.filter("initCollaborations", workspace, null, m_extendedLoader);
/*      */ 
/* 3044 */     SubjectCallbackAdapter clbraCallback = new CollaborationsSubjectCallback();
/* 3045 */     clbraCallback.setWorkspace(workspace);
/* 3046 */     clbraCallback.setLists(new String[] { "Collaborations" }, null);
/* 3047 */     SubjectManager.registerCallback("collaborations", clbraCallback);
/*      */   }
/*      */ 
/*      */   public static void initSchema(Workspace workspace)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */   }
/*      */ 
/*      */   public static WebPublisher performStartupWebPublishing(Workspace ws, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 3072 */     if (!Features.checkLevel("Publishing", null))
/*      */     {
/* 3074 */       return null;
/*      */     }
/* 3076 */     if (null == cxt)
/*      */     {
/* 3078 */       cxt = (null != m_extendedLoader) ? m_extendedLoader : m_cxt;
/*      */     }
/* 3080 */     int flags = 1024;
/* 3081 */     boolean useThread = SharedObjects.getEnvValueAsBoolean("UseStartupPublishThread", true);
/* 3082 */     if (useThread)
/*      */     {
/* 3084 */       flags |= 256;
/*      */     }
/* 3086 */     return WebPublishUtils.doPublish(ws, cxt, flags);
/*      */   }
/*      */ 
/*      */   public static void startCacheMonitoring(Workspace workspace)
/*      */     throws ServiceException
/*      */   {
/* 3096 */     String subjectUrl = "file://" + LegacyDirectoryLocator.getSubjectsDirectory();
/* 3097 */     SubjectManager.init(subjectUrl);
/* 3098 */     SubjectManager.m_osHasCachedTimestamps = SharedObjects.getEnvValueAsBoolean("SubjectManagerAggressiveTimestampCheck", false);
/* 3099 */     SubjectManager.checkForChange();
/* 3100 */     Vector alreadyLoadedSubjects = (Vector)SharedObjects.getObject("globalObjects", "alreadyLoadedSubjectsList");
/* 3101 */     if (alreadyLoadedSubjects != null)
/*      */     {
/* 3105 */       for (int i = 0; i < alreadyLoadedSubjects.size(); ++i)
/*      */       {
/* 3107 */         String alreadyLoadedSubject = (String)alreadyLoadedSubjects.elementAt(i);
/* 3108 */         SubjectManager.clearExternalChanged(alreadyLoadedSubject);
/*      */       }
/* 3110 */       alreadyLoadedSubjects.removeAllElements();
/*      */     }
/*      */ 
/* 3113 */     SubjectManager.refreshChanged();
/*      */ 
/* 3115 */     if (SharedObjects.getEnvValueAsBoolean("DisableSubjectMonitoringThread", false))
/*      */       return;
/* 3117 */     boolean fastScheduledEvents = SharedObjects.getEnvValueAsBoolean("DoDebugFastScheduledEvents", false);
/* 3118 */     int numScheduledEventsCallbackCounter = (fastScheduledEvents) ? 10 : 100;
/*      */ 
/* 3120 */     Workspace ws = workspace;
/* 3121 */     SubjectManagerListener listener = new SubjectManagerListener(numScheduledEventsCallbackCounter, ws)
/*      */     {
/*      */       public int m_counter;
/*      */ 
/*      */       public void handleManagerEvent(int eventType) {
/* 3126 */         if (eventType == 0)
/*      */         {
/* 3128 */           if (this.m_counter % this.val$numScheduledEventsCallbackCounter == 0)
/*      */           {
/*      */             try
/*      */             {
/* 3132 */               ScheduledSystemEvents systemEvents = IdcSystemLoader.getOrCreateScheduledSystemEvents(this.val$ws);
/* 3133 */               systemEvents.checkScheduledEvents();
/*      */             }
/*      */             catch (Throwable e)
/*      */             {
/* 3137 */               Report.error(null, null, e);
/*      */             }
/*      */           }
/* 3140 */           this.m_counter += 1;
/*      */         }
/* 3142 */         if ((eventType != 1) || 
/* 3144 */           (this.val$ws == null))
/*      */           return;
/* 3146 */         this.val$ws.releaseConnection();
/*      */       }
/*      */     };
/* 3151 */     SubjectManager.setListener(listener);
/* 3152 */     SubjectManager.startMonitoringThread();
/*      */   }
/*      */ 
/*      */   public static void checkCache()
/*      */     throws ServiceException
/*      */   {
/* 3158 */     SubjectManager.monitor();
/*      */   }
/*      */ 
/*      */   protected static boolean checkQueryFileTimestamp(String filePath)
/*      */   {
/* 3166 */     File curFile = new File(filePath);
/* 3167 */     long lastModified = curFile.lastModified();
/* 3168 */     if (lastModified > 0L)
/*      */     {
/* 3170 */       Date prevVal = ActiveState.getResultSetDate("QueryFileTimestamps", filePath);
/* 3171 */       if ((prevVal != null) && 
/* 3173 */         (prevVal.getTime() == lastModified))
/*      */       {
/* 3175 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3180 */     ActiveState.setResultSetValue("QueryFileTimestamps", filePath, Long.toString(lastModified), LocaleResources.getString("csLoadedAt", null, new Date()));
/*      */ 
/* 3183 */     return true;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected static void loadPropertiesFromFile(String fileName)
/*      */   {
/* 3189 */     Report.deprecatedUsage("loadPropertiesFromFileWithOverwrite() should be used instead of loadPropertiesFromFile().");
/*      */     try
/*      */     {
/* 3194 */       DataLoader.cachePropertiesFromFile(fileName, null);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3198 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadEnvironment", null, fileName);
/*      */ 
/* 3200 */       Report.error("system", msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static Properties loadPropertiesFromFileWithOverwrite(String fileName, Map args)
/*      */   {
/* 3206 */     return loadPropertiesFromFileImplementor(fileName, args, true);
/*      */   }
/*      */ 
/*      */   protected static Properties loadPropertiesFromFileWithoutOverwrite(String fileName, Map args)
/*      */   {
/* 3211 */     return loadPropertiesFromFileImplementor(fileName, args, false);
/*      */   }
/*      */ 
/*      */   protected static Properties loadPropertiesFromFileImplementor(String fileName, Map args, boolean overwrite)
/*      */   {
/* 3217 */     IdcStringBuilder builder = null;
/* 3218 */     Properties newProps = null;
/*      */     try
/*      */     {
/* 3221 */       newProps = DataLoader.cachePropertiesFromFileImplementor(fileName, args, overwrite);
/* 3222 */       if (!overwrite)
/*      */       {
/* 3224 */         Enumeration en = newProps.keys();
/* 3225 */         List l = new ArrayList();
/* 3226 */         while (en.hasMoreElements())
/*      */         {
/* 3228 */           String key = (String)en.nextElement();
/* 3229 */           String value = newProps.getProperty(key);
/* 3230 */           String actualValue = SharedObjects.getEnvironmentValue(key);
/* 3231 */           if (!actualValue.equals(value))
/*      */           {
/* 3233 */             if (builder == null)
/*      */             {
/* 3235 */               builder = new IdcStringBuilder();
/*      */             }
/*      */             else
/*      */             {
/* 3239 */               builder.setLength(0);
/*      */             }
/* 3241 */             builder.append2(key, '=');
/* 3242 */             builder.append(value);
/* 3243 */             l.add(builder.toStringNoRelease());
/*      */           }
/*      */         }
/*      */ 
/* 3247 */         if (builder != null)
/*      */         {
/* 3249 */           Report.warning("startup", null, "csComponentLoadIgnoredSettings", new Object[] { fileName, l });
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3255 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadEnvironment", null, fileName);
/*      */ 
/* 3257 */       Report.error("system", msg, e);
/*      */     }
/*      */     finally
/*      */     {
/* 3261 */       if (builder != null)
/*      */       {
/* 3263 */         builder.releaseBuffers();
/*      */       }
/*      */     }
/* 3266 */     return newProps;
/*      */   }
/*      */ 
/*      */   public static void mergeResourceTables() throws DataException, ServiceException
/*      */   {
/* 3271 */     boolean useVeritySearchApi = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseVeritySearchAPI"), true);
/*      */ 
/* 3274 */     Vector mergeRules = ComponentLoader.m_mergeRules;
/* 3275 */     int size = mergeRules.size();
/* 3276 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 3278 */       TableMergeRule rule = (TableMergeRule)mergeRules.elementAt(i);
/*      */ 
/* 3281 */       if ((useVeritySearchApi) && (rule.m_toTable.equals("VeritySearchAPITemplates")))
/*      */       {
/* 3283 */         rule.m_toTable = "SearchResultTemplates";
/*      */ 
/* 3285 */         Report.info(null, null, "csVeritySearchAPITemplatesDeprecation", new Object[0]);
/* 3286 */         Report.trace("system", null, "csVeritySearchAPITemplatesDeprecation", new Object[0]);
/*      */       }
/*      */ 
/* 3289 */       DataLoader.mergeTables(rule.m_fromTable, rule.m_toTable, rule.m_column);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void managePasswords()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3296 */     DataResultSet drset = SharedObjects.getTable("SecurityCategories");
/* 3297 */     if (drset == null)
/*      */     {
/* 3299 */       return;
/*      */     }
/* 3301 */     String configDir = LegacyDirectoryLocator.getConfigDirectory();
/* 3302 */     String privateDir = FileUtils.getAbsolutePath(configDir, "private");
/* 3303 */     if ((FileUtils.checkFile(privateDir, 0) != 0) && (!Features.checkLevel("NativeOsUtils", null)))
/*      */     {
/* 3306 */       Report.trace("system", "not initializing password management because NativeOsUtils is missing.", null);
/*      */ 
/* 3308 */       return;
/*      */     }
/* 3310 */     boolean isInitialized = CryptoPasswordUtils.loadPasswordManagement(drset);
/* 3311 */     if (!isInitialized)
/*      */     {
/* 3313 */       return;
/*      */     }
/* 3315 */     boolean doUpdate = CryptoPasswordUtils.needsUpdate();
/* 3316 */     if ((!doUpdate) && 
/* 3318 */       (EnvUtils.isHostedInAppServer()) && (!CryptoPasswordUtils.hasMasterKeys()) && (!SharedObjects.getEnvValueAsBoolean("IsProvisionalServer", false)))
/*      */     {
/* 3322 */       Report.trace("system", "Creating first master keys", null);
/* 3323 */       doUpdate = true;
/*      */     }
/*      */ 
/* 3326 */     if (doUpdate)
/*      */     {
/* 3328 */       Report.trace("system", "Password system reports needing an update, updating master key list", null);
/* 3329 */       HashMap args = new HashMap();
/* 3330 */       CryptoPasswordUtils.updateExpiredKeys(args);
/* 3331 */       CryptoPasswordUtils.updateExpiredPasswords(args);
/*      */     }
/*      */ 
/* 3334 */     DataResultSet scSet = SharedObjects.getTable("SecurityConfigFields");
/* 3335 */     if (scSet == null)
/*      */     {
/* 3337 */       scSet = CryptoPasswordUtils.createSecuritySet();
/*      */     }
/*      */ 
/* 3341 */     Map args = new HashMap();
/* 3342 */     args.put("PasswordScope", "system");
/*      */ 
/* 3347 */     Properties secureEnvironment = SharedObjects.getSecureEnvironment();
/* 3348 */     CryptoPasswordUtils.populatePasswordSet(secureEnvironment, scSet, "SharedObjects", args, true);
/*      */ 
/* 3350 */     Properties safeEnvironment = SharedObjects.getSafeEnvironment();
/* 3351 */     CryptoPasswordUtils.populatePasswordSet(safeEnvironment, scSet, "SharedObjects", args, true);
/*      */ 
/* 3353 */     if (!scSet.isEmpty())
/*      */     {
/* 3355 */       CryptoPasswordUtils.updatePasswords(scSet, null, args);
/*      */ 
/* 3358 */       for (scSet.first(); scSet.isRowPresent(); scSet.next())
/*      */       {
/* 3360 */         Map map = scSet.getCurrentRowMap();
/* 3361 */         boolean isUpdated = StringUtils.convertToBool((String)map.get("isUpdated"), false);
/* 3362 */         if (!isUpdated)
/*      */           continue;
/* 3364 */         String field = (String)map.get("field");
/* 3365 */         String source = (String)map.get("source");
/* 3366 */         String scope = (String)map.get("scope");
/* 3367 */         String encField = (String)map.get("encodingField");
/*      */ 
/* 3369 */         SharedObjects.putEnvironmentValueAllowOverwrite(field, "managed", source, true);
/* 3370 */         SharedObjects.putEnvironmentValueAllowOverwrite(encField, "managed", source, true);
/*      */ 
/* 3375 */         Report.trace(null, null, "csPasswordUnmanaged", new Object[] { field, source, scope });
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3383 */     SharedObjects.removeTable("SecurityConfigFields");
/*      */   }
/*      */ 
/*      */   public static void initNlsSortValue(Workspace workspace)
/*      */     throws DataException
/*      */   {
/* 3399 */     String nlsSortValue = "BINARY";
/* 3400 */     String dbType = workspace.getProperty("DatabaseType");
/*      */ 
/* 3402 */     nlsSortValue = workspace.getProperty("DatabaseCollation");
/*      */ 
/* 3404 */     if (dbType.equalsIgnoreCase(DatabaseTypes.MSSQL))
/*      */     {
/* 3406 */       nlsSortValue = sqlServerCollationToNlsSort(nlsSortValue);
/*      */     }
/*      */ 
/* 3409 */     if (dbType.equalsIgnoreCase(DatabaseTypes.DB2))
/*      */     {
/* 3411 */       nlsSortValue = db2CollationToNlsSort(nlsSortValue, workspace);
/*      */     }
/*      */ 
/* 3414 */     if ((nlsSortValue == null) || (nlsSortValue.equals("")))
/*      */     {
/* 3416 */       nlsSortValue = "BINARY";
/*      */     }
/*      */ 
/* 3419 */     intradoc.common.IdcLinguisticComparatorAdapter.m_defaultRule = nlsSortValue;
/*      */   }
/*      */ 
/*      */   public static String sqlServerCollationToNlsSort(String collation)
/*      */   {
/* 3431 */     String nlsSortValue = "";
/* 3432 */     String nlsSortOrder = "";
/*      */ 
/* 3434 */     DataResultSet SQLServerSortOrders = SharedObjects.getTable("SQLServerSortOrders");
/*      */ 
/* 3436 */     int indexOfUnderScore = collation.lastIndexOf("_");
/*      */ 
/* 3438 */     while (indexOfUnderScore != -1)
/*      */     {
/* 3440 */       String sortOrder = collation.substring(indexOfUnderScore + 1);
/* 3441 */       if (SQLServerSortOrders.findRow(0, sortOrder) != null)
/*      */       {
/* 3443 */         if (sortOrder.equalsIgnoreCase("BIN"))
/*      */         {
/* 3445 */           nlsSortValue = "BINARY";
/*      */         }
/* 3447 */         if (sortOrder.equalsIgnoreCase("BIN2"))
/*      */         {
/* 3449 */           nlsSortValue = "UNICODE_BINARY";
/*      */         }
/* 3451 */         if ((sortOrder.equalsIgnoreCase("CI")) && 
/* 3453 */           (nlsSortOrder.equals("")))
/*      */         {
/* 3455 */           nlsSortOrder = "CI";
/*      */         }
/*      */ 
/* 3458 */         if (sortOrder.equalsIgnoreCase("AI"))
/*      */         {
/* 3460 */           nlsSortOrder = "AI";
/*      */         }
/* 3462 */         collation = collation.substring(0, indexOfUnderScore);
/* 3463 */         indexOfUnderScore = collation.lastIndexOf("_");
/*      */       }
/*      */       else
/*      */       {
/* 3468 */         indexOfUnderScore = -1;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3473 */     if (nlsSortValue.equals(""))
/*      */     {
/* 3475 */       nlsSortValue = findOraNLSFromNLSSortMap(collation, 1);
/*      */     }
/*      */ 
/* 3479 */     if ((nlsSortValue == null) || (nlsSortValue.equals("")))
/*      */     {
/* 3481 */       nlsSortValue = "BINARY";
/*      */     }
/*      */ 
/* 3485 */     if (!nlsSortOrder.equals(""))
/*      */     {
/* 3487 */       nlsSortValue = nlsSortValue + "_" + nlsSortOrder;
/*      */     }
/*      */ 
/* 3490 */     return nlsSortValue;
/*      */   }
/*      */ 
/*      */   public static String findOraNLSFromNLSSortMap(String collation, int colIndex)
/*      */   {
/* 3500 */     String oraNLSSortValue = "BINARY";
/*      */ 
/* 3502 */     if ((collation != null) && (!collation.equals("")))
/*      */     {
/* 3504 */       DataResultSet nlsSortDataResultSet = SharedObjects.getTable("NLSSortMap");
/*      */ 
/* 3506 */       for (int row = 0; row < nlsSortDataResultSet.getNumRows(); ++row)
/*      */       {
/* 3508 */         Vector rowValues = nlsSortDataResultSet.getRowValues(row);
/*      */ 
/* 3510 */         String collationsSupported = (String)rowValues.get(colIndex);
/* 3511 */         if (collationsSupported == null) continue; if (collationsSupported.equals(""))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 3516 */         List listOfCollations = StringUtils.makeListFromSequenceSimple(collationsSupported);
/*      */ 
/* 3520 */         boolean isCollationFound = false;
/*      */ 
/* 3522 */         for (int lc = 0; lc < listOfCollations.size(); ++lc)
/*      */         {
/* 3524 */           String collationListed = (String)listOfCollations.get(lc);
/*      */ 
/* 3526 */           if (!collation.equalsIgnoreCase(collationListed))
/*      */             continue;
/* 3528 */           isCollationFound = true;
/* 3529 */           break;
/*      */         }
/*      */ 
/* 3533 */         if (isCollationFound != true) {
/*      */           continue;
/*      */         }
/* 3536 */         oraNLSSortValue = (String)rowValues.get(0);
/* 3537 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3543 */     if ((oraNLSSortValue == null) || (oraNLSSortValue.equals("")))
/*      */     {
/* 3545 */       oraNLSSortValue = "BINARY";
/*      */     }
/*      */ 
/* 3548 */     return oraNLSSortValue;
/*      */   }
/*      */ 
/*      */   public static String db2CollationToNlsSort(String collation, Workspace workspace)
/*      */     throws DataException
/*      */   {
/* 3558 */     String nlsSortValue = "BINARY";
/* 3559 */     String territory = "";
/*      */ 
/* 3561 */     int indexOfLastUnderScore = collation.lastIndexOf("_");
/*      */ 
/* 3563 */     if (indexOfLastUnderScore != -1)
/*      */     {
/* 3565 */       territory = collation.substring(indexOfLastUnderScore + 1);
/*      */     }
/*      */ 
/* 3568 */     if ((territory == null) || (territory.equals("")))
/*      */     {
/* 3570 */       ResultSet territoryRset = workspace.createResultSet("Qterritory", null);
/*      */ 
/* 3572 */       if (territoryRset.isRowPresent())
/*      */       {
/* 3574 */         territory = ResultSetUtils.getValue(territoryRset, "value");
/*      */       }
/*      */     }
/*      */ 
/* 3578 */     nlsSortValue = findOraNLSFromNLSSortMap(territory, 2);
/*      */ 
/* 3580 */     if ((nlsSortValue == null) || (nlsSortValue.equals("")))
/*      */     {
/* 3582 */       nlsSortValue = "BINARY";
/*      */     }
/*      */ 
/* 3585 */     return nlsSortValue;
/*      */   }
/*      */ 
/*      */   public static ExecutionContext getInitializationContext()
/*      */   {
/* 3595 */     return m_cxt;
/*      */   }
/*      */ 
/*      */   public static void prepareComponentPublishing()
/*      */   {
/* 3605 */     Map componentBinders = ComponentLoader.m_components;
/* 3606 */     DataResultSet publishedStaticFiles = SharedObjects.getTable("PublishedStaticFiles");
/* 3607 */     boolean didChange = false;
/*      */ 
/* 3610 */     Map m = new HashMap();
/* 3611 */     Parameters params = new MapParameters(m);
/* 3612 */     m.put("class", "autopublish");
/* 3613 */     m.put("loadOrder", "0");
/* 3614 */     m.put("doPublishScript", "<$doPublish = 1$>");
/* 3615 */     m.put("canDeleteDir", "0");
/*      */ 
/* 3617 */     for (String compName : componentBinders.keySet())
/*      */     {
/* 3619 */       DataBinder compBinder = (DataBinder)componentBinders.get(compName);
/*      */       try
/*      */       {
/* 3622 */         String dir = ComponentLocationUtils.computeAbsoluteComponentDirectory(compName) + "/idcautopublish";
/*      */ 
/* 3624 */         File publishDir = new File(dir);
/* 3625 */         if ((publishDir.exists()) && (publishDir.isDirectory()))
/*      */         {
/* 3627 */           m.put("idcComponentName", compName);
/* 3628 */           File[] subDirs = publishDir.listFiles();
/* 3629 */           for (File f : subDirs)
/*      */           {
/* 3631 */             if (!f.isDirectory())
/*      */               continue;
/* 3633 */             String dirName = f.getName();
/* 3634 */             if (dirName.equals("weblayout"))
/*      */             {
/* 3636 */               m.put("srcPath", "idcautopublish/weblayout");
/* 3637 */               m.put("path", "");
/*      */             }
/*      */             else
/*      */             {
/* 3641 */               m.put("srcPath", "idcautopublish/" + dirName);
/* 3642 */               m.put("path", dirName);
/*      */             }
/*      */ 
/* 3645 */             publishedStaticFiles.addRow(publishedStaticFiles.createRow(params));
/* 3646 */             compBinder.putLocal("hasPublishedStaticFiles", "1");
/* 3647 */             didChange = true;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 3654 */         Report.trace("system", "Unable to autopublish files for component: " + compName, e);
/*      */       }
/*      */     }
/*      */ 
/* 3658 */     if (!didChange)
/*      */       return;
/* 3660 */     SharedObjects.putTable("PublishedStaticFiles", publishedStaticFiles);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3666 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102961 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcSystemLoader
 * JD-Core Version:    0.5.4
 */