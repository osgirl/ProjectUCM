/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestInitializer;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.cache.IdcCacheFactory;
/*     */ import intradoc.server.publish.WebPublisher;
/*     */ import intradoc.server.subject.ServerStartupSubjectCallback;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class IdcServerManager extends IdcManagerBase
/*     */ {
/*     */   public WebPublisher m_publisher;
/*     */ 
/*     */   protected int getThreadCount()
/*     */   {
/*  52 */     return SystemUtils.getThreadCount();
/*     */   }
/*     */ 
/*     */   public String getServiceDisplayName()
/*     */   {
/*  61 */     String msg = LocaleResources.getString("csContentServerServiceDisplayName", new ExecutionContextAdaptor(), SharedObjects.getEnvironmentValue("InstanceMenuLabel"));
/*     */ 
/*  64 */     return msg;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  73 */     boolean isStartupSuccessful = false;
/*     */     try
/*     */     {
/*  77 */       int defaultDatabaseConnections = 15;
/*     */ 
/*  80 */       IntervalData interval = new IntervalData("startup");
/*  81 */       IdcServerOutput.init();
/*  82 */       interval.traceAndRestart("startup", "IdcServerOutput.init()");
/*  83 */       if (StringUtils.convertToBool(System.getenv("TraceIsVerbose"), false))
/*     */       {
/*  85 */         Report.m_verbose = SystemUtils.m_verbose = 1;
/*  86 */         Report.trace(null, "TraceIsVerbose enabled, from environment", null);
/*     */       }
/*  88 */       String sections = System.getenv("TraceSectionsList");
/*  89 */       if (sections != null)
/*     */       {
/*  91 */         String[] sectionsArray = StringUtils.makeStringArrayFromSequence(sections);
/*  92 */         for (int a = sectionsArray.length - 1; a >= 0; --a)
/*     */         {
/*  94 */           SystemUtils.addAsActiveTrace(sectionsArray[a]);
/*     */         }
/*  96 */         Report.trace(null, "enabled tracing sections from environment: " + sections, null);
/*     */       }
/*     */ 
/* 100 */       IdcSystemConfig.loadInitialConfig();
/* 101 */       interval.traceAndRestart("startup", "IdcSystemConfig.loadInitialConfig()");
/* 102 */       IdcSystemConfig.loadAppConfigInfo();
/* 103 */       interval.traceAndRestart("startup", "IdcSystemConfig.loadAppConfigInfo()");
/*     */ 
/* 107 */       boolean hasSocketPort = IdcSystemConfig.checkAppServerConfig();
/*     */ 
/* 110 */       boolean isServerMode = SharedObjects.getEnvValueAsBoolean("IsServerMode", true);
/* 111 */       SharedObjects.putEnvironmentValue("IsServerMode", "" + isServerMode);
/* 112 */       if ((!isServerMode) && (!EnvUtils.isHostedInAppServer()))
/*     */       {
/* 114 */         defaultDatabaseConnections = 3;
/*     */       }
/*     */ 
/* 118 */       SharedObjects.putEnvironmentValue("IsAutoProviderMonitor", "1");
/*     */ 
/* 121 */       boolean useSocketPort = (isServerMode) && (hasSocketPort);
/* 122 */       IdcSystemLoader.init(useSocketPort);
/* 123 */       interval.traceAndRestart("startup", "IdcSystemLoader.init(" + isServerMode + ")");
/*     */ 
/* 126 */       this.m_workspace = IdcSystemLoader.loadDatabase(defaultDatabaseConnections);
/* 127 */       IdcSystemLoader.loadSystemUserDatabase(defaultDatabaseConnections);
/*     */ 
/* 129 */       Report.trace("startup", null, "csMonitorDefaultDbConnections", new Object[] { Integer.valueOf(defaultDatabaseConnections) });
/*     */ 
/* 132 */       interval.traceAndRestart("startup", "IdcSytemLoader.loadDatabase(" + defaultDatabaseConnections + ")");
/*     */ 
/* 135 */       boolean isAutoSuggestEnabled = SharedObjects.getEnvValueAsBoolean("EnableAutoSuggest", false);
/* 136 */       if ((isAutoSuggestEnabled == true) && (this.m_workspace != null))
/*     */       {
/* 138 */         AutoSuggestInitializer.initEnvironment(this.m_workspace);
/*     */       }
/* 140 */       IdcCacheFactory.init();
/* 141 */       IdcSystemLoader.loadCaches(this.m_workspace);
/* 142 */       interval.traceAndRestart("startup", "IdcSystemLoader.loadCaches()");
/*     */ 
/* 144 */       if (isServerMode)
/*     */       {
/* 147 */         IdcSystemLoader.initWebFilterConfig();
/* 148 */         interval.traceAndRestart("startup", "IdcSystemLoader.initWebFilterConfig()");
/*     */       }
/*     */ 
/* 152 */       if (SharedObjects.getEnvValueAsBoolean("IsExalogicOptimizationsEnabled", false))
/*     */       {
/* 154 */         IdcSystemLoader.initAlertMonitor();
/* 155 */         interval.traceAndRestart("startup", "IdcSystemLoader.initAlertMonitor()");
/*     */       }
/*     */       else
/*     */       {
/* 159 */         IdcSystemLoader.clearServerRestartAlert();
/*     */       }
/*     */ 
/* 162 */       IdcSystemLoader.validateStandardDirectories();
/* 163 */       interval.traceAndRestart("startup", "IdcSystemLoader.validateStandardDirectories()");
/* 164 */       IdcSystemLoader.loadPageBuilderConfig();
/* 165 */       interval.traceAndRestart("startup", "IdcSystemLoader.loadPageBuilderConfig()");
/*     */ 
/* 167 */       boolean isAuto = !SharedObjects.getEnvValueAsBoolean("NoAutomation", !isServerMode);
/*     */ 
/* 171 */       IdcSystemLoader.initArchiver(this.m_workspace, isAuto);
/* 172 */       interval.traceAndRestart("startup", "IdcSystemLoader.initArchiver()");
/*     */ 
/* 176 */       this.m_publisher = IdcSystemLoader.performStartupWebPublishing(this.m_workspace, null);
/*     */ 
/* 179 */       interval.traceAndRestart("startup", "IdcSystemLoader.performStartupWebPublishing()");
/* 180 */       IdcSystemLoader.startCacheMonitoring(this.m_workspace);
/* 181 */       interval.traceAndRestart("startup", "IdcSystemLoader.startCacheMonitoring()");
/* 182 */       IdcSystemLoader.initReplicationData();
/* 183 */       interval.traceAndRestart("startup", "IdcSystemLoader.initReplicationData()");
/* 184 */       if (isAuto)
/*     */       {
/* 186 */         IdcSystemLoader.initQueueProcessing(this.m_workspace);
/* 187 */         interval.traceAndRestart("startup", "IdcSystemLoader.initQueueProcessing()");
/*     */       }
/* 189 */       IdcSystemLoader.initSearchIndexer(this.m_workspace, isAuto);
/* 190 */       interval.traceAndRestart("startup", "IdcSystemLoader.initSearchIndexer()");
/* 191 */       if (isAuto)
/*     */       {
/* 193 */         IdcSystemLoader.initWorkQueue(this.m_workspace);
/* 194 */         interval.traceAndRestart("startup", "IdcSystemLoader.initWorkQueue()");
/*     */       }
/* 196 */       IdcSystemLoader.initWorkflow(this.m_workspace, isAuto);
/* 197 */       interval.traceAndRestart("startup", "IdcSystemLoader.initWorkflow()");
/*     */ 
/* 200 */       IdcSystemLoader.loadServiceData();
/* 201 */       interval.traceAndRestart("startup", "IdcSystemLoader.loadServiceData()");
/*     */ 
/* 204 */       IdcSystemLoader.loadServerTemplates(this.m_workspace);
/* 205 */       interval.traceAndRestart("startup", "IdcSystemLoader.loadServerTemplates()");
/*     */ 
/* 208 */       ScheduledSystemEvents sysEvents = IdcSystemLoader.getOrCreateScheduledSystemEvents(this.m_workspace);
/*     */ 
/* 210 */       String[] sEvents = ResourceContainerUtils.getDynamicFieldListResource("ScheduledSystemEventList");
/*     */ 
/* 212 */       sysEvents.setScheduledEventsActiveCategories(sEvents);
/* 213 */       interval.traceAndRestart("startup", "ScheduledSystemEvents.setScheduledEventsActiveCategories()");
/*     */ 
/* 216 */       if ((isAutoSuggestEnabled == true) && (this.m_workspace != null))
/*     */       {
/* 218 */         AutoSuggestInitializer autoSuggestInitializer = new AutoSuggestInitializer(this.m_workspace);
/* 219 */         autoSuggestInitializer.init();
/*     */       }
/*     */ 
/* 223 */       IdcSystemLoader.registerProviders();
/* 224 */       interval.trace("startup", "IdcSystemLoader.registerProviders()");
/*     */ 
/* 228 */       ServerConfigurationAnalyzer.analyzeCurrentConfiguration(this.m_workspace);
/* 229 */       interval.trace("startup", "ServerConfigurationAnalyzer.analyzeCurrentConfiguration()");
/*     */ 
/* 233 */       IdcSystemLoader.m_extendedLoader.executeFilter("postInitialize", this.m_workspace);
/* 234 */       interval.trace("startup", "postInitialize filter");
/* 235 */       SharedLoader.postInitialize();
/* 236 */       interval.trace("startup", "SharedLoader.postInitialize()");
/*     */ 
/* 239 */       SubjectCallbackAdapter startupSubjectCallback = new ServerStartupSubjectCallback();
/* 240 */       SubjectManager.registerCallback("serverstartup", startupSubjectCallback);
/* 241 */       SubjectManager.notifyChanged("serverstartup");
/*     */ 
/* 243 */       interval.stop();
/* 244 */       isStartupSuccessful = true;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 258 */       if (this.m_workspace != null)
/*     */       {
/* 260 */         Providers.releaseConnections();
/*     */       }
/* 262 */       if ((!isStartupSuccessful) && (this.m_publisher != null))
/*     */       {
/* 265 */         this.m_publisher.abort(500L);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void serviceStart(int numArgs, int pArgs)
/*     */     throws DataException, ServiceException
/*     */   {
/* 273 */     super.serviceStart(numArgs, pArgs);
/* 274 */     String licenseString = "!csServerIsUnlicensed";
/*     */     try
/*     */     {
/* 277 */       licenseString = Service.checkFeatureAllowed(null);
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 281 */       ignore.printStackTrace();
/*     */     }
/* 283 */     if (licenseString == null)
/*     */       return;
/* 285 */     String msg = LocaleResources.localizeMessage(licenseString, null);
/* 286 */     SystemUtils.outln(msg);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 293 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105194 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcServerManager
 * JD-Core Version:    0.5.4
 */