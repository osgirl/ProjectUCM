/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.indexer.IndexerConfig;
/*     */ import intradoc.indexer.IndexerExecution;
/*     */ import intradoc.indexer.IndexerExecutionHandler;
/*     */ import intradoc.indexer.IndexerWorkObject;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchIndexerUtils
/*     */ {
/*  31 */   public static final String[][] DEFAULT_ENGINE_NAME_MAP = { { "VERITY", "VERITY.VDK.4" }, { "DATABASE", "DATABASE.METADATA" }, { "DATABASEFULLTEXT", "DATABASE.FULLTEXT" }, { "FAST", "FAST.4" } };
/*     */ 
/*  35 */   public static final String[] m_searchOperatorRSNames = { "SearchTextField", "SearchDateField", "SearchBooleanField", "SearchIntegerField", "SearchZoneField" };
/*     */ 
/*  38 */   public static final String[] m_searchDefaultOperatorKeys = { "SearchFullTextQueryDef", "SearchConjunction", "SearchOrConjunction", "SearchNotOperator", "DefaultSearchOperator", "DefaultNBFieldSearchOperator" };
/*     */ 
/*     */   public static void initSearchIndexerConfig()
/*     */     throws DataException, ServiceException
/*     */   {
/*  45 */     String name = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*  46 */     if (name == null)
/*     */     {
/*  48 */       name = "DATABASE.METADATA";
/*     */     }
/*     */     else
/*     */     {
/*  52 */       name = getProperEngineName(name);
/*     */     }
/*     */ 
/*  56 */     ActiveIndexState.m_fileDir = LegacyDirectoryLocator.getSearchDirectory();
/*     */ 
/*  60 */     CommonSearchConfig searchConfig = new CommonSearchConfig();
/*  61 */     ExecutionContext ctxt = new ExecutionContextAdaptor();
/*  62 */     PageMerger pm = new PageMerger();
/*  63 */     ctxt.setCachedObject("PageMerger", pm);
/*  64 */     searchConfig.init(name, ctxt);
/*  65 */     SharedObjects.putObject("globalObjects", "CommonSearchConfig", searchConfig);
/*  66 */     populateSiteMap();
/*     */ 
/*  68 */     IndexerConfig indexerConfig = new IndexerConfig();
/*  69 */     indexerConfig.init();
/*  70 */     indexerConfig.setCurrentConfig(name);
/*     */ 
/*  73 */     initEnvironmentVariables(name, searchConfig, indexerConfig);
/*     */ 
/*  76 */     IndexerConfig rebuildIndexerConfig = null;
/*     */ 
/*  78 */     String rebuildEngineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName:Rebuild");
/*  79 */     if (rebuildEngineName != null)
/*     */     {
/*  81 */       rebuildEngineName = rebuildEngineName.toUpperCase();
/*  82 */       if (!rebuildEngineName.equalsIgnoreCase(name))
/*     */       {
/*  84 */         rebuildIndexerConfig = new IndexerConfig();
/*  85 */         rebuildIndexerConfig.init();
/*  86 */         rebuildIndexerConfig.setCurrentConfig(rebuildEngineName);
/*     */       }
/*     */     }
/*     */ 
/*  90 */     if (rebuildIndexerConfig == null)
/*     */     {
/*  92 */       rebuildIndexerConfig = indexerConfig;
/*     */     }
/*     */ 
/*  95 */     Hashtable indexerConfigs = new Hashtable();
/*  96 */     indexerConfigs.put("update", indexerConfig);
/*  97 */     indexerConfigs.put("rebuild", rebuildIndexerConfig);
/*     */ 
/*  99 */     SharedObjects.putObject("globalObjects", "IndexerConfigs", indexerConfigs);
/*     */ 
/* 101 */     SubjectCallback scb = new SubjectCallbackAdapter()
/*     */     {
/*     */       public void loadBinder(String name_ignore, DataBinder binder, ExecutionContext cxt)
/*     */       {
/* 106 */         String queryFormat = SearchIndexerUtils.getSearchQueryFormat(binder, cxt);
/* 107 */         Hashtable containers = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/* 108 */         Properties clientConfigs = (Properties)containers.get(queryFormat);
/*     */ 
/* 110 */         DataResultSet opStrMap = (DataResultSet)clientConfigs.get("SearchQueryOpStrMap");
/* 111 */         DataResultSet clause = (DataResultSet)clientConfigs.get("SearchQueryOpMap");
/* 112 */         DataResultSet sortFieldsRset = (DataResultSet)clientConfigs.get("SearchSortFields");
/* 113 */         binder.addResultSet("SearchQueryOpStrMap", opStrMap);
/* 114 */         binder.addResultSet("SearchQueryOpMap", clause);
/*     */ 
/* 116 */         String conjunction = (String)clientConfigs.get("SearchConjunction");
/* 117 */         String fieldComparisonIndex = (String)clientConfigs.get("FieldComparisonIndex");
/* 118 */         String escapeChars = (String)clientConfigs.get("EscapeChars");
/* 119 */         String sortFields = "";
/*     */         try
/*     */         {
/* 123 */           if ((conjunction != null) && (conjunction.length() != 0))
/*     */           {
/* 125 */             SharedLoader.addEnvVariableToTable(binder, "SearchConjunction", conjunction);
/*     */           }
/* 127 */           if ((fieldComparisonIndex != null) && (fieldComparisonIndex.length() != 0))
/*     */           {
/* 129 */             SharedLoader.addEnvVariableToTable(binder, "FieldComparisonIndex", fieldComparisonIndex);
/*     */           }
/* 131 */           if ((escapeChars != null) && (escapeChars.length() != 0))
/*     */           {
/* 133 */             SharedLoader.addEnvVariableToTable(binder, "EscapeChars", escapeChars);
/*     */           }
/* 135 */           IndexerConfig indexerCfg = SearchIndexerUtils.getIndexerConfig(null, "update");
/*     */ 
/* 137 */           String[] additionalConfigs = { "IndexerDebugLevels", "IndexerAvailableBatchSizes", "IndexerAvailableCheckPointCounts" };
/*     */ 
/* 139 */           for (int i = 0; i < additionalConfigs.length; ++i)
/*     */           {
/* 141 */             String configValue = indexerCfg.getValue(additionalConfigs[i]);
/* 142 */             if ((configValue == null) || (configValue.length() == 0))
/*     */               continue;
/* 144 */             SharedLoader.addEnvVariableToTable(binder, additionalConfigs[i], configValue);
/*     */           }
/*     */ 
/* 148 */           for (int fieldNo = 0; fieldNo < sortFieldsRset.getNumRows(); ++fieldNo)
/*     */           {
/* 150 */             Vector sortFieldRow = sortFieldsRset.getRowValues(fieldNo);
/* 151 */             String sortFieldName = (String)sortFieldRow.get(0);
/*     */ 
/* 153 */             if (sortFields.contains(sortFieldName + ","))
/*     */               continue;
/* 155 */             sortFields = sortFields + sortFieldName + ",";
/*     */           }
/*     */ 
/* 158 */           SharedLoader.addEnvVariableToTable(binder, "SearchSortFieldsList", sortFields);
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/*     */         }
/*     */       }
/*     */     };
/* 167 */     SubjectManager.registerCallback("searchapi", scb);
/*     */   }
/*     */ 
/*     */   protected static void initEnvironmentVariables(String name, CommonSearchConfig searchCfg, IndexerConfig indexerCfg)
/*     */   {
/* 173 */     SharedObjects.putEnvironmentValue("SearchIndexerEngineName", name);
/* 174 */     String queryFormat = getSearchQueryFormat(null, null);
/* 175 */     SharedObjects.putEnvironmentValue("SearchQueryFormat", queryFormat);
/*     */ 
/* 177 */     String[] additionalConfigs = { "IndexerDebugLevels", "IndexerAvailableBatchSizes", "IndexerAvailableCheckPointCounts", "SupportAdvanceConfigOptions", "SupportFastRebuild" };
/*     */ 
/* 179 */     for (int i = 0; i < additionalConfigs.length; ++i)
/*     */     {
/* 181 */       String configValue = indexerCfg.getValue(additionalConfigs[i]);
/* 182 */       if ((configValue == null) || (configValue.length() == 0))
/*     */         continue;
/* 184 */       SharedObjects.putEnvironmentValue(additionalConfigs[i], configValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void populateSiteMap()
/*     */   {
/* 191 */     HashMap siteMap = new HashMap();
/* 192 */     String siteMapStr = SharedObjects.getEnvironmentValue("QuickSearchSiteMap");
/* 193 */     Vector tmpVector = StringUtils.parseArray(siteMapStr, ',', ',');
/* 194 */     String sitePair = null;
/* 195 */     int size = tmpVector.size();
/* 196 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 198 */       sitePair = (String)tmpVector.elementAt(i);
/* 199 */       int index = sitePair.indexOf(58);
/* 200 */       if ((index < 1) || (index > sitePair.length() - 2))
/*     */       {
/* 203 */         if (!SystemUtils.m_verbose)
/*     */           continue;
/* 205 */         Report.debug("system", "Quick search site map format error: " + sitePair, null);
/*     */       }
/*     */       else
/*     */       {
/* 209 */         String site = sitePair.substring(0, index);
/* 210 */         String provider = sitePair.substring(index + 1);
/*     */ 
/* 212 */         siteMap.put(site, provider);
/*     */       }
/*     */     }
/* 214 */     SharedObjects.putObject("globalObjects", "QuickSearchSiteMap", siteMap);
/*     */   }
/*     */ 
/*     */   public static IndexerExecution getIndexerExecution(IndexerConfig indexerConfig, IndexerWorkObject data)
/*     */     throws ServiceException, DataException
/*     */   {
/* 220 */     IndexerExecution exec = null;
/*     */ 
/* 222 */     exec = (IndexerExecution)data.getCachedObject("IndexerExecution");
/* 223 */     if (exec == null)
/*     */     {
/* 225 */       String className = "intradoc.indexer.IndexerExecution";
/*     */ 
/* 227 */       exec = (IndexerExecution)ComponentClassFactory.createClassInstance("IndexerExecution", className, "");
/*     */ 
/* 229 */       exec.init(data);
/* 230 */       data.setCachedObject("IndexerExecution", exec);
/*     */     }
/*     */ 
/* 233 */     return exec;
/*     */   }
/*     */ 
/*     */   public static IndexerExecutionHandler createExecutionHandler(IndexerConfig indexerConfig, IndexerExecution exec)
/*     */     throws ServiceException
/*     */   {
/* 239 */     String className = indexerConfig.getConfigValue("IndexerExecutionHandler");
/* 240 */     IndexerExecutionHandler handler = (IndexerExecutionHandler)ComponentClassFactory.createClassInstance("IndexerExecutionHandler", className, "");
/*     */ 
/* 242 */     handler.init(exec);
/* 243 */     return handler;
/*     */   }
/*     */ 
/*     */   public static String getIndexerImplementorClassName(IndexerConfig indexerConfig)
/*     */   {
/* 248 */     return indexerConfig.getConfigValue("IndexerImplementor");
/*     */   }
/*     */ 
/*     */   public static String getSearchImplementorClassName(CommonSearchConfig config)
/*     */   {
/* 254 */     String searchImpl = config.getEngineValue("SearchImplementorName");
/* 255 */     if (searchImpl != null)
/*     */     {
/* 257 */       String className = config.getEngineValue(searchImpl);
/* 258 */       if ((className != null) && (className.length() != 0))
/*     */       {
/* 260 */         return className;
/*     */       }
/* 262 */       Report.trace("search", "unable to find search implementor for " + searchImpl + ", using default.", null);
/*     */     }
/*     */ 
/* 266 */     String className = config.getStringFromTableWithInheritance("SearchEngineClasses", "SearchEngineName", config.getCurrentEngineName(), "SearchImplementor");
/*     */ 
/* 268 */     return className;
/*     */   }
/*     */ 
/*     */   public static IndexerConfig getIndexerConfig(IndexerWorkObject data, String cycleId)
/*     */     throws DataException
/*     */   {
/* 274 */     IndexerConfig indexerConfig = null;
/*     */ 
/* 276 */     if (data != null)
/*     */     {
/* 278 */       indexerConfig = (IndexerConfig)data.getCachedObject("IndexerConfig");
/*     */     }
/* 280 */     if (indexerConfig == null)
/*     */     {
/* 282 */       Hashtable configs = (Hashtable)SharedObjects.getObject("globalObjects", "IndexerConfigs");
/* 283 */       if ((configs != null) && (cycleId != null))
/*     */       {
/* 285 */         indexerConfig = (IndexerConfig)configs.get(cycleId);
/*     */       }
/* 287 */       if (indexerConfig == null)
/*     */       {
/* 289 */         throw new DataException(LocaleUtils.encodeMessage(null, "!csNoIndexerConfigMatchingCycleId", cycleId));
/*     */       }
/*     */ 
/* 292 */       indexerConfig = indexerConfig.shallowClone();
/* 293 */       if (data != null)
/*     */       {
/* 295 */         data.setCachedObject("IndexerConfig", indexerConfig);
/*     */       }
/*     */     }
/* 298 */     return indexerConfig;
/*     */   }
/*     */ 
/*     */   public static CommonSearchConfig retrieveSearchConfig(ExecutionContext ctxt)
/*     */   {
/* 303 */     CommonSearchConfig csc = null;
/* 304 */     if (ctxt != null)
/*     */     {
/* 306 */       csc = (CommonSearchConfig)ctxt.getCachedObject("CommonSearchConfig");
/*     */     }
/*     */ 
/* 310 */     if (csc == null)
/*     */     {
/* 312 */       csc = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*     */     }
/*     */ 
/* 315 */     return csc;
/*     */   }
/*     */ 
/*     */   public static String getSearchEngineName(ExecutionContext ctxt)
/*     */   {
/* 320 */     String engineName = null;
/* 321 */     if (ctxt != null)
/*     */     {
/* 324 */       DataBinder binder = (DataBinder)ctxt.getCachedObject("DataBinder");
/* 325 */       if (binder != null)
/*     */       {
/* 327 */         engineName = binder.getLocal("SearchEngineName");
/*     */       }
/*     */     }
/* 330 */     if (engineName == null)
/*     */     {
/* 332 */       CommonSearchConfig config = retrieveSearchConfig(ctxt);
/* 333 */       engineName = config.getCurrentEngineName();
/*     */     }
/*     */     else
/*     */     {
/* 337 */       engineName = getProperEngineName(engineName);
/*     */     }
/* 339 */     return engineName;
/*     */   }
/*     */ 
/*     */   public static String getConversionEngineName(ExecutionContext ctxt)
/*     */   {
/* 344 */     String engineName = null;
/* 345 */     if (ctxt != null)
/*     */     {
/* 347 */       DataBinder binder = (DataBinder)ctxt.getCachedObject("DATABINDER");
/* 348 */       if (binder != null)
/*     */       {
/* 350 */         engineName = binder.getAllowMissing("IndexerConversionEngine");
/*     */       }
/*     */     }
/* 353 */     if (engineName == null)
/*     */     {
/* 355 */       engineName = "TextIndexerFilter";
/*     */     }
/* 357 */     return engineName;
/*     */   }
/*     */ 
/*     */   public static synchronized SearchManager getOrCreateSearchManager()
/*     */     throws DataException, ServiceException
/*     */   {
/* 363 */     SearchManager searchManager = (SearchManager)SharedObjects.getObject("globalObjects", "SearchManager");
/*     */ 
/* 365 */     if (searchManager == null)
/*     */     {
/* 367 */       searchManager = (SearchManager)ComponentClassFactory.createClassInstance("SearchManager", "intradoc.server.SearchManager", "!csUnableToCreateManager2");
/*     */ 
/* 370 */       searchManager.configure();
/* 371 */       SharedObjects.putObject("globalObjects", "SearchManager", searchManager);
/*     */     }
/* 373 */     return searchManager;
/*     */   }
/*     */ 
/*     */   public static String getSearchQueryFormat(DataBinder binder, ExecutionContext ctxt)
/*     */   {
/* 378 */     String format = null;
/* 379 */     if ((binder == null) && (ctxt != null))
/*     */     {
/* 381 */       binder = (DataBinder)ctxt.getCachedObject("DataBinder");
/*     */     }
/* 383 */     if (binder != null)
/*     */     {
/* 385 */       format = binder.getLocal("SearchQueryFormat");
/*     */     }
/* 387 */     if (format == null)
/*     */     {
/* 389 */       format = SharedObjects.getEnvironmentValue("SearchQueryFormat");
/* 390 */       if (format == null)
/*     */       {
/* 392 */         format = "UNIVERSAL";
/*     */       }
/*     */     }
/* 395 */     return convertToProperSearchQueryFormat(format);
/*     */   }
/*     */ 
/*     */   public static String convertToProperSearchQueryFormat(String format)
/*     */   {
/* 400 */     if (format == null)
/*     */     {
/* 402 */       return format;
/*     */     }
/* 404 */     if (format.equalsIgnoreCase("native"))
/*     */     {
/* 406 */       format = getSearchEngineName(null);
/*     */     }
/*     */     else
/*     */     {
/* 410 */       format = format.toUpperCase();
/*     */     }
/* 412 */     return format;
/*     */   }
/*     */ 
/*     */   public static CommonSearchConfig getCommonSearchConfig(ExecutionContext ctxt) throws DataException
/*     */   {
/* 417 */     CommonSearchConfig csc = (CommonSearchConfig)ctxt.getCachedObject("CommonSearchConfig");
/* 418 */     if (csc == null)
/*     */     {
/* 420 */       DataBinder binder = (DataBinder)ctxt.getCachedObject("DataBinder");
/* 421 */       String searchEngineName = binder.getLocal("SearchEngineName");
/* 422 */       csc = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/* 423 */       if (csc == null)
/*     */       {
/* 426 */         throw new DataException("!csSearchCommonSearchConfigUnInitialized");
/*     */       }
/* 428 */       csc = csc.shallowClone();
/* 429 */       if (searchEngineName != null)
/*     */       {
/* 431 */         csc.setCurrentConfig(searchEngineName);
/*     */       }
/*     */     }
/* 434 */     return csc;
/*     */   }
/*     */ 
/*     */   public static String getProperEngineName(String engineName)
/*     */   {
/* 439 */     if (engineName == null)
/*     */     {
/* 441 */       return null;
/*     */     }
/*     */ 
/* 444 */     engineName = engineName.toUpperCase();
/* 445 */     String tmp = StringUtils.findString(DEFAULT_ENGINE_NAME_MAP, engineName, 0, 1);
/* 446 */     if (tmp != null)
/*     */     {
/* 448 */       engineName = tmp;
/*     */     }
/* 450 */     return engineName;
/*     */   }
/*     */ 
/*     */   public static boolean hasIndexerChanged(String engineName)
/*     */     throws DataException
/*     */   {
/* 456 */     boolean hasIndexerChanged = false;
/*     */ 
/* 458 */     String indexID = ActiveIndexState.getActiveProperty("ActiveIndex");
/* 459 */     IndexerConfig cfg = getIndexerConfig(null, "update");
/* 460 */     DataResultSet rset = cfg.getTable(engineName, "CollectionID");
/* 461 */     if (rset != null)
/*     */     {
/* 463 */       String label = ResultSetUtils.findValue(rset, "IndexerLabel", indexID, "IndexerLabel");
/* 464 */       if (label == null)
/*     */       {
/* 466 */         hasIndexerChanged = true;
/*     */       }
/*     */     }
/* 469 */     return hasIndexerChanged;
/*     */   }
/*     */ 
/*     */   public static void getSearchOperators(DataBinder binder, ExecutionContext ctxt) throws ServiceException
/*     */   {
/* 474 */     String queryFormat = getSearchQueryFormat(binder, ctxt);
/* 475 */     String engineName = getSearchEngineName(ctxt);
/*     */ 
/* 477 */     Hashtable clientConfigs = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*     */ 
/* 480 */     Properties clientConfig = (Properties)clientConfigs.get(queryFormat + "." + engineName);
/*     */ 
/* 482 */     if (clientConfig == null)
/*     */     {
/* 484 */       clientConfig = (Properties)clientConfigs.get(queryFormat);
/*     */     }
/*     */ 
/* 487 */     if (clientConfig == null)
/*     */     {
/* 489 */       CommonSearchConfig searchConfig = null;
/*     */       try
/*     */       {
/* 492 */         searchConfig = getCommonSearchConfig(ctxt);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 496 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 499 */       queryFormat = searchConfig.getStringFromTable("SearchRepository", "srID", queryFormat, "srEngineName");
/* 500 */       clientConfig = (Properties)clientConfigs.get(queryFormat);
/*     */     }
/*     */ 
/* 503 */     for (String resultSetName : m_searchOperatorRSNames)
/*     */     {
/* 505 */       DataResultSet drset = (DataResultSet)clientConfig.get(resultSetName);
/* 506 */       if (drset == null)
/*     */         continue;
/* 508 */       binder.addResultSet(resultSetName, drset.shallowClone());
/*     */     }
/*     */ 
/* 512 */     for (String key : m_searchDefaultOperatorKeys)
/*     */     {
/* 514 */       String value = clientConfig.getProperty(key);
/* 515 */       if ((value == null) || (value.length() == 0))
/*     */         continue;
/* 517 */       binder.putLocal(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 524 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99484 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchIndexerUtils
 * JD-Core Version:    0.5.4
 */