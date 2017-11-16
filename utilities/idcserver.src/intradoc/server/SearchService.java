/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IntervalData;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseLocationInfo;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.DocClassUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.DataTransformationUtils;
/*      */ import intradoc.search.CacheObject;
/*      */ import intradoc.search.CacheUpdateParameters;
/*      */ import intradoc.search.EnterpriseSearchThread;
/*      */ import intradoc.search.ParsedQueryElements;
/*      */ import intradoc.search.SearchCache;
/*      */ import intradoc.search.SearchCacheUtils;
/*      */ import intradoc.search.SearchListItem;
/*      */ import intradoc.search.SearchQueryUtils;
/*      */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.SharedPageMergerData;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.shared.schema.SchemaFieldData;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.StringReader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SearchService extends Service
/*      */ {
/*   44 */   public static final String[] EXTRA_SEARCH_FIELDS = { "IsLocalCollection", "Selected" };
/*      */ 
/*   49 */   public static final String[] SEARCH_RESULTS_LOCAL_VALUES = { "TotalRows", "TotalDocsProcessed", "StartRow", "ResultCount" };
/*      */ 
/*   55 */   public String m_fieldNames = null;
/*   56 */   public int m_numFieldNames = 0;
/*      */ 
/*   58 */   protected String m_result = null;
/*   59 */   protected DataBinder m_resultBinder = null;
/*      */ 
/*   61 */   protected int m_searchActionType = 0;
/*   62 */   protected SearchManager m_sManager = null;
/*   63 */   protected DataResultSet m_searchColls = null;
/*   64 */   protected SearchCache m_searchCache = null;
/*      */ 
/*   66 */   protected boolean m_useCache = false;
/*   67 */   protected boolean m_updateCache = false;
/*   68 */   protected long m_sharedSearchTime = 0L;
/*   69 */   protected int m_startRow = 0;
/*   70 */   protected int m_rowCount = 0;
/*   71 */   protected boolean m_isAppSearch = false;
/*      */ 
/*   73 */   protected CommonSearchConfig m_queryConfig = null;
/*   74 */   protected List<String> m_targetDocClasses = null;
/*      */ 
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*   84 */     super.createHandlersForService();
/*   85 */     createHandlers("SearchService");
/*      */   }
/*      */ 
/*      */   public void initDelegatedObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*   91 */     super.initDelegatedObjects();
/*   92 */     this.m_sharedSearchTime = DocumentInfoCacheUtils.getSharedDocTimestamp();
/*      */   }
/*      */ 
/*      */   public void copyShallow(Service service)
/*      */   {
/*   98 */     super.copyShallow(service);
/*   99 */     if (service instanceof SearchService)
/*      */     {
/*  101 */       SearchService sService = (SearchService)service;
/*  102 */       this.m_sManager = sService.m_sManager;
/*      */ 
/*  106 */       this.m_cachedData = ((HashMap)this.m_cachedData.clone());
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/*  112 */         getOrCreateSearchManager();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  116 */         Report.warning(null, e, "csUnableToCreateManager", new Object[0]);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/*  121 */       loadCommonSearchConfig(this.m_binder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  125 */       Report.warning(null, e, "csUnableToLoadCommonSearchConfig", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData)
/*      */     throws DataException
/*      */   {
/*  133 */     super.init(ws, output, binder, serviceData);
/*      */   }
/*      */ 
/*      */   public void preActions()
/*      */     throws ServiceException
/*      */   {
/*  140 */     super.preActions();
/*      */     try
/*      */     {
/*  143 */       loadCommonSearchConfig(this.m_binder);
/*  144 */       getOrCreateSearchManager();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  148 */       throw new ServiceException(e);
/*      */     }
/*  150 */     loadFieldInfo();
/*      */   }
/*      */ 
/*      */   protected void getOrCreateSearchManager()
/*      */     throws DataException, ServiceException
/*      */   {
/*  156 */     this.m_sManager = SearchIndexerUtils.getOrCreateSearchManager();
/*      */   }
/*      */ 
/*      */   public void loadCommonSearchConfig(DataBinder binder) throws DataException
/*      */   {
/*  161 */     CommonSearchConfig config = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*  162 */     String defaultEngineName = config.getCurrentEngineName();
/*  163 */     this.m_queryConfig = config.shallowClone();
/*      */ 
/*  165 */     String label = binder.getLocal("Repository");
/*  166 */     String engineName = null;
/*  167 */     if (label != null)
/*      */     {
/*  169 */       engineName = config.getStringFromTable("SearchRepository", "srID", label, "srEngineName");
/*      */ 
/*  171 */       this.m_queryConfig.setCurrentConfig(engineName);
/*      */     }
/*  173 */     else if (((label = binder.getLocal("SearchEngineName")) != null) && (label.length() > 0))
/*      */     {
/*  175 */       engineName = label;
/*  176 */       this.m_queryConfig.setCurrentConfig(engineName);
/*      */     }
/*      */ 
/*  179 */     boolean isDefault = false;
/*  180 */     if (engineName != null)
/*      */     {
/*  182 */       engineName = SearchIndexerUtils.getProperEngineName(engineName);
/*      */ 
/*  184 */       if (defaultEngineName.startsWith(engineName + "."))
/*      */       {
/*  186 */         engineName = defaultEngineName;
/*  187 */         isDefault = true;
/*      */       }
/*      */       else
/*      */       {
/*  191 */         isDefault = defaultEngineName.equals(engineName);
/*      */       }
/*  193 */       if ((!isDefault) && (!engineName.equals("DATABASE.METADATA")) && (!engineName.startsWith("DATABASE.METADATA.")))
/*      */       {
/*  196 */         throw new DataException(LocaleUtils.encodeMessage("csSearchEngineNotSupportedWithCurrentConfig", null, engineName));
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  201 */       engineName = defaultEngineName;
/*      */     }
/*  203 */     this.m_binder.putLocal("ComputedSearchEngineName", engineName);
/*  204 */     this.m_binder.putLocal("SearchEngineIsDefault", (isDefault) ? "1" : "");
/*  205 */     setCachedObject("CommonSearchConfig", this.m_queryConfig);
/*      */   }
/*      */ 
/*      */   public void loadSearchCache()
/*      */   {
/*  210 */     if (this.m_searchCache != null) {
/*      */       return;
/*      */     }
/*  213 */     String str = this.m_binder.getAllowMissing("UseSearchCache");
/*  214 */     if (str == null)
/*      */     {
/*  216 */       str = this.m_binder.getAllowMissing("UseCache");
/*      */     }
/*      */ 
/*  219 */     if (str == null)
/*      */     {
/*  221 */       String engineName = this.m_binder.getAllowMissing("SearchEngineName");
/*  222 */       str = this.m_queryConfig.getEngineValue(engineName, "UseSearchCache");
/*      */     }
/*      */ 
/*  225 */     this.m_useCache = StringUtils.convertToBool(str, true);
/*      */ 
/*  227 */     this.m_updateCache = StringUtils.convertToBool(this.m_binder.getAllowMissing("UpdateSearchCache"), true);
/*  228 */     this.m_searchCache = ((SearchCache)SharedObjects.getObject("globalObjects", "SearchCache"));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getSearchResults()
/*      */     throws ServiceException, DataException
/*      */   {
/*  237 */     checkFeatureAllowed("Search");
/*      */ 
/*  239 */     String repo = this.m_binder.getLocal("Repository");
/*  240 */     CommonSearchConfig config = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*      */ 
/*  244 */     if (repo != null)
/*      */     {
/*  246 */       String privilege = config.getStringFromTable("SearchRepository", "srID", repo, "srPrivilege");
/*      */ 
/*  249 */       if (privilege != null)
/*      */       {
/*  251 */         int intPrivilege = Integer.parseInt(privilege);
/*  252 */         if (getServiceData().m_accessLevel != intPrivilege)
/*      */         {
/*  254 */           getServiceData().m_accessLevel = intPrivilege;
/*  255 */           globalSecurityCheck();
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  261 */     setConditionVar("IgnoreAccounts", false);
/*  262 */     String queryText = this.m_binder.get("QueryText");
/*  263 */     if (queryText != null)
/*      */     {
/*  265 */       queryText = queryText.trim();
/*      */     }
/*      */ 
/*  268 */     IntervalData queryInterval = null;
/*  269 */     boolean isSearchQueryTrace = SystemUtils.isActiveTrace("searchquery");
/*  270 */     if (isSearchQueryTrace)
/*      */     {
/*  272 */       queryInterval = new IntervalData("searchquery");
/*  273 */       if (SystemUtils.m_verbose)
/*      */       {
/*  275 */         this.m_sManager.debugTraceStart(this.m_binder);
/*      */       }
/*      */     }
/*      */ 
/*  279 */     this.m_binder.putLocal("OriginalUnencodedQueryText", queryText);
/*  280 */     String originalQueryText = WebRequestUtils.encodeUrlSegmentForBrowser(queryText, this.m_binder, this);
/*  281 */     this.m_binder.putLocal("OriginalQueryText", originalQueryText);
/*      */ 
/*  285 */     String originalSecurityGroup = this.m_binder.getAllowMissing("dSecurityGroup");
/*  286 */     if ((originalSecurityGroup != null) && (originalSecurityGroup.length() > 0))
/*      */     {
/*  288 */       this.m_binder.putLocal("IdcOriginalSecurityGroup", originalSecurityGroup);
/*      */     }
/*      */ 
/*  292 */     String clientControlled = this.m_binder.getLocal("ClientControlled");
/*  293 */     if ((clientControlled != null) && (clientControlled.indexOf("query") >= 0) && (this.m_binder.getLocal("CaptureQueryText") == null))
/*      */     {
/*  296 */       this.m_binder.putLocal("CaptureQueryText", queryText);
/*      */     }
/*      */ 
/*  300 */     boolean hasSortSpec = false;
/*      */     String userQueryText;
/*      */     try
/*      */     {
/*  304 */       if (this.m_pageMerger.m_isReportErrorStack)
/*      */       {
/*  306 */         String msg = LocaleUtils.encodeMessage("csDynHTMLEvalVariableInMethod", null, "SearchService.getSearchResults", "QueryText");
/*  307 */         this.m_pageMerger.pushStackMessage(msg);
/*      */       }
/*  309 */       if (this.m_binder.getLocal("SortSpec") != null)
/*      */       {
/*  312 */         hasSortSpec = true;
/*      */       }
/*  314 */       queryText = this.m_queryConfig.prepareQueryText(queryText, this.m_binder, this);
/*      */ 
/*  318 */       userQueryText = queryText;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       String msg;
/*  328 */       if (this.m_pageMerger.m_isReportErrorStack)
/*      */       {
/*  330 */         this.m_pageMerger.popStack();
/*      */       }
/*      */     }
/*      */ 
/*  334 */     this.m_binder.putLocal("QueryText", queryText);
/*  335 */     this.m_binder.putLocal("UnparsedQueryText", queryText);
/*      */ 
/*  338 */     loadSearchCache();
/*      */ 
/*  341 */     this.m_startRow = DataBinderUtils.getLocalInteger(this.m_binder, "StartRow", 1);
/*  342 */     this.m_rowCount = DataBinderUtils.getLocalInteger(this.m_binder, "ResultCount", 20);
/*  343 */     if (this.m_startRow < 1)
/*      */     {
/*  345 */       String msg = LocaleUtils.encodeMessage("csSearchItemNotPositive", null, "StartRow");
/*      */ 
/*  347 */       throw new ServiceException(msg);
/*      */     }
/*  349 */     if (this.m_rowCount < 1)
/*      */     {
/*  351 */       String msg = LocaleUtils.encodeMessage("csSearchItemNotPositive", null, "ResultCount");
/*      */ 
/*  353 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  358 */     normalizeTargetDocClasses();
/*  359 */     String request = computeQueryCacheKey(this.m_binder);
/*      */ 
/*  366 */     String securityGroupCacheContext = null;
/*  367 */     String explicitGroup = this.m_binder.getLocal("dSecurityGroup");
/*  368 */     String roles = null;
/*  369 */     String localSecurityClause = null;
/*      */ 
/*  372 */     String explicitAccount = this.m_binder.getLocal("dDocAccount");
/*  373 */     if ((explicitAccount != null) && (explicitAccount.length() == 0))
/*      */     {
/*  375 */       explicitAccount = null;
/*      */     }
/*      */ 
/*  379 */     this.m_binder.putLocal("FullRequest", request);
/*      */ 
/*  381 */     int rowsPerResult = DataBinderUtils.getInteger(this.m_binder, "EnterpriseSearchMaxRows", 4);
/*  382 */     this.m_binder.putLocal("EnterpriseSearchMaxRows", "" + rowsPerResult);
/*  383 */     if (this.m_binder.getLocal("StartRow") != null)
/*      */     {
/*  385 */       rowsPerResult = this.m_rowCount;
/*      */     }
/*      */ 
/*  388 */     DataResultSet searchableProviders = SearchLoader.getSearchableProviderList(this.m_userData, true, explicitAccount == null);
/*      */ 
/*  391 */     int count = searchableProviders.getNumRows();
/*  392 */     String searchProviders = this.m_binder.getLocal("SearchProviders");
/*  393 */     FieldInfo[] infos = ResultSetUtils.createInfoList(searchableProviders, new String[] { "ProviderName", "IsLocalCollection", "IsImplicitlySearched", "HttpRelativeWebRoot", "UserAccounts" }, true);
/*      */ 
/*  397 */     List updateParametersList = new ArrayList(count);
/*      */ 
/*  399 */     String[] searchProvidersList = StringUtils.makeStringArrayFromSequence(searchProviders);
/*      */ 
/*  401 */     Hashtable runningThreads = new Hashtable();
/*  402 */     CacheUpdateParameters localUpdateParameters = null;
/*  403 */     int localIndex = -1;
/*  404 */     boolean searchedRemote = false;
/*  405 */     int serverCount = 0;
/*  406 */     String newSearchProviderList = "";
/*  407 */     ParsedQueryElements queryElts = null;
/*  408 */     IdcDateFormat searchDateFormat = LocaleResources.m_iso8601Format;
/*      */ 
/*  410 */     boolean checkedLicense = false;
/*      */     try
/*      */     {
/*  414 */       for (int i = 0; i < count; ++i)
/*      */       {
/*  416 */         Vector values = searchableProviders.getRowValues(i);
/*  417 */         String name = (String)values.elementAt(infos[0].m_index);
/*  418 */         String isLocal = (String)values.elementAt(infos[1].m_index);
/*  419 */         String isImplicit = (String)values.elementAt(infos[2].m_index);
/*  420 */         String accounts = (String)values.elementAt(infos[4].m_index);
/*  421 */         boolean isLocalSearch = StringUtils.convertToBool(isLocal, false);
/*  422 */         String securityCacheContext = "";
/*  423 */         if (isLocalSearch)
/*      */         {
/*  428 */           if ((this.m_securityImpl != null) && (this.m_userData != null))
/*      */           {
/*  430 */             queryElts = new ParsedQueryElements();
/*  431 */             setCachedObject("ParsedQueryElements", queryElts);
/*  432 */             localSecurityClause = determineDocumentWhereClause(this.m_userData, this.m_serviceData.m_accessLevel, true);
/*      */ 
/*  434 */             if (localSecurityClause != null)
/*      */             {
/*  436 */               securityCacheContext = localSecurityClause;
/*      */             }
/*  438 */             SearchQueryUtils.processSecurityClauseElements(queryElts, this.m_binder, this);
/*  439 */             if (queryElts.m_isError)
/*      */             {
/*  441 */               String errMsg = SearchQueryUtils.createErrorReport(queryElts);
/*  442 */               throw new ServiceException(errMsg);
/*      */             }
/*      */           }
/*      */ 
/*  446 */           localIndex = i;
/*      */         }
/*      */         else
/*      */         {
/*  451 */           if (securityGroupCacheContext == null)
/*      */           {
/*  453 */             roles = SecurityUtils.getRolePackagedList(this.m_userData);
/*  454 */             if ((explicitGroup != null) && (explicitGroup.length() > 0))
/*      */             {
/*  456 */               securityGroupCacheContext = "%" + explicitGroup;
/*      */             }
/*      */             else
/*      */             {
/*  461 */               explicitGroup = null;
/*  462 */               securityGroupCacheContext = roles;
/*      */             }
/*      */           }
/*  465 */           securityCacheContext = securityGroupCacheContext;
/*      */         }
/*      */ 
/*  468 */         boolean skip = true;
/*  469 */         if (((searchProviders != null) && (StringUtils.findStringIndexEx(searchProvidersList, name, true) >= 0)) || (StringUtils.convertToBool(isImplicit, false)) || ((searchProviders == null) && (isLocalSearch)))
/*      */         {
/*  473 */           skip = false;
/*      */         }
/*      */ 
/*  476 */         if (skip)
/*      */         {
/*  479 */           CacheUpdateParameters emptyUpdateParameters = new CacheUpdateParameters(null, this.m_binder, null, null, this.m_sharedSearchTime, null, this);
/*      */ 
/*  481 */           updateParametersList.add(emptyUpdateParameters);
/*      */         }
/*      */         else {
/*  484 */           ++serverCount;
/*      */ 
/*  486 */           if (newSearchProviderList.length() > 0)
/*      */           {
/*  488 */             newSearchProviderList = newSearchProviderList + ",";
/*      */           }
/*  490 */           newSearchProviderList = newSearchProviderList + name;
/*      */ 
/*  493 */           Provider p = null;
/*  494 */           if (!isLocalSearch)
/*      */           {
/*  496 */             p = Providers.getProvider(name);
/*      */           }
/*      */ 
/*  499 */           if (explicitAccount != null)
/*      */           {
/*  501 */             accounts = explicitAccount.toLowerCase();
/*      */           }
/*      */ 
/*  505 */           int flags = (isLocalSearch) ? 1 : 0;
/*  506 */           String query = this.m_queryConfig.getCacheKey(securityCacheContext, accounts, request, this.m_binder, flags, this);
/*      */ 
/*  508 */           CacheUpdateParameters updateParameters = new CacheUpdateParameters(query, this.m_binder, name, p, this.m_sharedSearchTime, this.m_queryConfig.getCurrentEngineConfig(), this);
/*      */ 
/*  510 */           if (!hasSortSpec)
/*      */           {
/*  512 */             updateParameters.m_parsedQueryElements = queryElts;
/*      */ 
/*  516 */             loadSortInformation(updateParameters);
/*      */           }
/*  518 */           updateParametersList.add(updateParameters);
/*  519 */           if (isLocalSearch)
/*      */           {
/*  521 */             localUpdateParameters = updateParameters;
/*  522 */             localUpdateParameters.m_isLocalSearch = true;
/*  523 */             localUpdateParameters.m_isSecondarySearchEngine = DataBinderUtils.getLocalBoolean(this.m_binder, "SearchEngineIsDefault", false);
/*      */ 
/*  527 */             localUpdateParameters.m_searchResults = prepareLocalSearch(searchDateFormat);
/*      */           }
/*  529 */           if ((this.m_searchCache != null) && (this.m_useCache))
/*      */           {
/*  531 */             updateParameters.m_allowOldCache = (!DataBinderUtils.getLocalBoolean(this.m_binder, "noOldSearchCache", false));
/*  532 */             updateParameters.m_preferOldCache = DataBinderUtils.getLocalBoolean(this.m_binder, "preferOldCache", false);
/*  533 */             int rowCount = (isLocalSearch) ? this.m_rowCount : rowsPerResult;
/*  534 */             this.m_searchCache.checkCache(updateParameters, this.m_startRow, rowCount);
/*      */ 
/*  537 */             if ((!isLocalSearch) && (!updateParameters.m_cacheIsComplete))
/*      */             {
/*  539 */               updateParameters.m_isValidCache = false;
/*      */             }
/*      */           }
/*      */ 
/*  543 */           if (p != null)
/*      */           {
/*  545 */             searchedRemote = true;
/*      */           }
/*      */ 
/*  549 */           if (updateParameters.m_isValidCache)
/*      */           {
/*  551 */             if (!SystemUtils.isActiveTrace("searchquery"))
/*      */               continue;
/*  553 */             String msg = " [" + this.m_startRow + "," + (this.m_startRow + this.m_rowCount - 1) + "]";
/*  554 */             boolean isOldCache = updateParameters.m_isOldCache;
/*  555 */             String reportQuery = query;
/*  556 */             if (isOldCache)
/*      */             {
/*  558 */               msg = "query(old cache): " + reportQuery + msg;
/*      */             }
/*      */             else
/*      */             {
/*  562 */               msg = "query(cache): " + reportQuery + msg;
/*      */             }
/*  564 */             this.m_sManager.debugTrace(msg);
/*  565 */             SearchListItem[] missingNames = updateParameters.m_missingNames;
/*  566 */             if ((missingNames != null) && (missingNames.length > 0))
/*      */             {
/*  568 */               IdcStringBuilder builder = new IdcStringBuilder(100);
/*  569 */               builder.append("missing names: ");
/*  570 */               for (int j = 0; j < missingNames.length; ++j)
/*      */               {
/*  572 */                 if (j > 0)
/*      */                 {
/*  574 */                   builder.append(',');
/*      */                 }
/*  576 */                 builder.append(missingNames[j].m_key);
/*      */               }
/*  578 */               this.m_sManager.debugTrace(builder.toString());
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*      */             try
/*      */             {
/*  586 */               if (!isLocalSearch)
/*      */               {
/*  588 */                 if (!checkedLicense)
/*      */                 {
/*  590 */                   checkFeatureAllowed("EnterpriseSearch");
/*  591 */                   checkedLicense = true;
/*      */                 }
/*      */ 
/*  594 */                 Properties requestProps = new Properties();
/*  595 */                 String user = "";
/*  596 */                 if (!isConditionVarTrue("IsProxyLocalLogin"))
/*      */                 {
/*  598 */                   requestProps.put("EXTERNAL_ROLES", roles);
/*  599 */                   requestProps.put("EXTERNAL_ACCOUNTS", accounts);
/*  600 */                   user = this.m_userData.m_name;
/*      */                 }
/*  602 */                 requestProps.put("REMOTE_USER", user);
/*  603 */                 if (explicitGroup != null)
/*      */                 {
/*  605 */                   requestProps.put("dSecurityGroup", explicitGroup);
/*      */                 }
/*  607 */                 if (explicitAccount != null)
/*      */                 {
/*  609 */                   requestProps.put("dDocAccount", explicitAccount);
/*      */                 }
/*      */ 
/*  612 */                 DataBinder requestBinder = new DataBinder();
/*  613 */                 requestBinder.setEnvironment(this.m_binder.getEnvironment());
/*  614 */                 requestBinder.setLocalData((Properties)this.m_binder.getLocalData().clone());
/*  615 */                 requestBinder.putLocal("IdcService", "GET_SEARCH_RESULTS");
/*  616 */                 requestBinder.m_blDateFormat = LocaleResources.m_searchFormat;
/*  617 */                 requestBinder.removeLocal("SearchProviders");
/*      */ 
/*  621 */                 requestBinder.putLocal("QueryText", queryText);
/*      */ 
/*  623 */                 if ((this.m_searchCache != null) && (this.m_useCache) && (this.m_updateCache))
/*      */                 {
/*  625 */                   setRangeForCaching(requestBinder);
/*      */                 }
/*      */                 else
/*      */                 {
/*  629 */                   requestBinder.putLocal("StartRow", "" + this.m_startRow);
/*  630 */                   requestBinder.putLocal("ResultCount", "" + this.m_rowCount);
/*      */                 }
/*      */ 
/*  633 */                 DataBinder results = new DataBinder(SharedObjects.getSecureEnvironment());
/*  634 */                 updateParameters.m_hasSearchResults = true;
/*  635 */                 updateParameters.m_searchResults = results;
/*      */ 
/*  637 */                 Thread thread = new EnterpriseSearchThread(requestProps, p, requestBinder, results, runningThreads, this);
/*      */ 
/*  639 */                 runningThreads.put(name, thread);
/*  640 */                 thread.start();
/*      */               }
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/*  645 */               if (e instanceof ServiceException)
/*      */               {
/*  647 */                 ServiceException se = (ServiceException)e;
/*  648 */                 if ((se.m_errorCode & 0xFFF0) == 65440)
/*      */                 {
/*  651 */                   throw se;
/*      */                 }
/*      */               }
/*  654 */               updateParameters.m_isError = true;
/*  655 */               updateParameters.m_exception = e;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  661 */       this.m_binder.putLocal("SearchProviders", newSearchProviderList);
/*  662 */       if (serverCount == 0)
/*      */       {
/*  664 */         createServiceException(null, "!csSearchNoInstancesWithProviders," + searchProviders);
/*      */       }
/*      */ 
/*  667 */       if (searchedRemote)
/*      */       {
/*  669 */         this.m_binder.putLocal("searchedRemote", "1");
/*      */       }
/*      */ 
/*  672 */       if ((localUpdateParameters != null) && (!localUpdateParameters.m_cacheIsComplete))
/*      */       {
/*  675 */         if (localUpdateParameters.m_isValidCache)
/*      */         {
/*  677 */           DataBinder docInfoRequestBinder = new DataBinder();
/*      */ 
/*  679 */           if (localUpdateParameters.m_missingNames != null)
/*      */           {
/*  681 */             String[] missingNameStrs = new String[localUpdateParameters.m_missingNames.length];
/*  682 */             for (int j = 0; j < missingNameStrs.length; ++j)
/*      */             {
/*  684 */               missingNameStrs[j] = localUpdateParameters.m_missingNames[j].m_key;
/*      */             }
/*      */ 
/*  687 */             searchForDocumentMetadataImplement(missingNameStrs, docInfoRequestBinder, searchDateFormat);
/*      */           }
/*      */ 
/*  690 */           localUpdateParameters.m_updateType = CacheObject.CACHE_IS_DOC_INFO;
/*  691 */           this.m_searchCache.updateCache(localUpdateParameters, docInfoRequestBinder);
/*      */         }
/*  693 */         if (!localUpdateParameters.m_cacheIsComplete)
/*      */         {
/*  695 */           String securityClause = (localSecurityClause != null) ? localSecurityClause : "";
/*  696 */           localUpdateParameters.m_searchResults.putLocal("CommonSearchSecurityFilter", securityClause);
/*  697 */           doLocalSearch(localUpdateParameters.m_searchResults);
/*      */ 
/*  702 */           boolean isSearchRetried = validateAndRetrySearch(localUpdateParameters.m_searchResults);
/*      */ 
/*  704 */           if (isSearchRetried == true) {
/*      */             boolean isSearchRetried;
/*      */             int i;
/*      */             CacheUpdateParameters updateParameters;
/*      */             return;
/*  709 */           }localUpdateParameters.m_hasSearchResults = true;
/*      */         }
/*      */       }
/*      */ 
/*  713 */       long haltTime = System.currentTimeMillis() + 900000L;
/*  714 */       synchronized (runningThreads)
/*      */       {
/*  716 */         while ((runningThreads.size() > 0) && (System.currentTimeMillis() < haltTime))
/*      */         {
/*      */           try
/*      */           {
/*  720 */             runningThreads.wait(10000L);
/*      */           }
/*      */           catch (InterruptedException ignore)
/*      */           {
/*  724 */             Report.trace(null, null, ignore);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  729 */       if ((this.m_binder.getLocal("StartRow") == null) && (serverCount > 1))
/*      */       {
/*  731 */         this.m_rowCount = rowsPerResult;
/*      */       }
/*      */ 
/*  734 */       Table capturedResultFieldsTable = SearchCacheUtils.getCapturedResultFields();
/*  735 */       String[] capturedResultFields = capturedResultFieldsTable.m_colNames;
/*  736 */       String[] defaultsRow = null;
/*  737 */       List rows = capturedResultFieldsTable.m_rows;
/*  738 */       if (rows.size() > 0)
/*      */       {
/*  740 */         defaultsRow = (String[])(String[])rows.get(0);
/*      */       }
/*      */ 
/*  743 */       int last = searchableProviders.getNumFields();
/*  744 */       int numHardwiredResultSummaryFields = 4;
/*  745 */       String[] fields = new String[last + numHardwiredResultSummaryFields + capturedResultFields.length];
/*  746 */       for (int i = 0; i < last; ++i)
/*      */       {
/*  748 */         fields[i] = searchableProviders.getFieldName(i);
/*      */       }
/*      */ 
/*  754 */       fields[(last++)] = "StatusCode";
/*  755 */       fields[(last++)] = "StatusMessage";
/*  756 */       fields[(last++)] = "ResultSetName";
/*  757 */       fields[(last++)] = "SearchCgiWebUrl";
/*      */ 
/*  759 */       for (int i = 0; i < capturedResultFields.length; ++i)
/*      */       {
/*  761 */         fields[(last++)] = capturedResultFields[i];
/*      */       }
/*  763 */       DataResultSet results = new DataResultSet(fields);
/*      */ 
/*  765 */       int totalRows = 0;
/*  766 */       int totalProcessed = 0;
/*  767 */       boolean noMatches = true;
/*  768 */       boolean hasError = false;
/*  769 */       boolean hasRowAndDocCount = true;
/*  770 */       for (int i = 0; i < count; ++i)
/*      */       {
/*  772 */         CacheUpdateParameters updateParameters = (CacheUpdateParameters)updateParametersList.get(i);
/*  773 */         if (updateParameters.m_query == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  779 */         Vector row = (Vector)searchableProviders.getRowValues(i).clone();
/*  780 */         String name = (String)row.elementAt(infos[0].m_index);
/*      */ 
/*  782 */         String resultCount = "0";
/*  783 */         String processed = "0";
/*      */ 
/*  785 */         String pathRoot = this.m_binder.getEnvironmentValue("HTTP_CGIPATHROOT");
/*  786 */         String cgiWebUrl = null;
/*  787 */         if (pathRoot != null)
/*      */         {
/*  789 */           cgiWebUrl = pathRoot + (String)row.elementAt(infos[3].m_index) + "pxs";
/*      */         }
/*      */         else
/*      */         {
/*  793 */           cgiWebUrl = DirectoryLocator.getExternalProxiedCgiWebUrl(false, (String)row.elementAt(infos[3].m_index));
/*      */         }
/*      */ 
/*  796 */         boolean hasMoreRows = false;
/*      */ 
/*  798 */         if (!updateParameters.m_isError)
/*      */         {
/*  800 */           DataBinder binder = null;
/*  801 */           localUpdateParameters.m_updateType = CacheObject.CACHE_IS_DOC_LIST;
/*  802 */           if ((updateParameters.m_cacheIsComplete) && (updateParameters.m_isValidCache))
/*      */           {
/*  805 */             binder = new DataBinder();
/*  806 */             binder.m_environment = this.m_binder.m_environment;
/*  807 */             SearchCacheUtils.fillDataBinderWithCacheData(updateParameters, binder);
/*      */           }
/*  809 */           else if (updateParameters.m_hasSearchResults)
/*      */           {
/*  812 */             binder = updateParameters.m_searchResults;
/*      */           }
/*  814 */           DataResultSet rset = null;
/*  815 */           if (binder != null)
/*      */           {
/*  817 */             rset = (DataResultSet)binder.getResultSet("SearchResults");
/*      */           }
/*  819 */           if (rset != null)
/*      */           {
/*  823 */             if ((this.m_updateCache) && (updateParameters.m_hasSearchResults) && (updateParameters.m_providerCache != null))
/*      */             {
/*  825 */               this.m_searchCache.updateCache(updateParameters, binder);
/*      */             }
/*      */ 
/*  828 */             resultCount = binder.getLocal("TotalRows");
/*  829 */             processed = binder.getLocal("TotalDocsProcessed");
/*  830 */             hasMoreRows = DataBinderUtils.getBoolean(binder, "HasMoreRows", false);
/*  831 */             if ((hasRowAndDocCount) && (resultCount == null))
/*      */             {
/*  833 */               hasRowAndDocCount = false;
/*      */             }
/*      */ 
/*  836 */             int nresults = NumberUtils.parseInteger(resultCount, ((DataResultSet)binder.getResultSet("SearchResults")).getNumRows());
/*  837 */             if (resultCount == null)
/*      */             {
/*  839 */               int tmpTotalRow = nresults + this.m_startRow;
/*  840 */               if (!hasMoreRows)
/*      */               {
/*  842 */                 --tmpTotalRow;
/*      */               }
/*  844 */               resultCount = "" + tmpTotalRow;
/*      */             }
/*  846 */             totalRows += nresults;
/*  847 */             if ((processed == null) || (processed.length() == 0))
/*      */             {
/*  850 */               processed = "";
/*  851 */               totalProcessed += nresults;
/*      */             }
/*      */             else
/*      */             {
/*  855 */               totalProcessed += NumberUtils.parseInteger(processed, 0);
/*      */             }
/*      */ 
/*  858 */             DataResultSet localResults = getResults(binder, serverCount == 1);
/*      */ 
/*  862 */             boolean captureLiveMetadata = false;
/*  863 */             if (i == localIndex)
/*      */             {
/*  865 */               captureLiveMetadata = DataBinderUtils.getBoolean(this.m_binder, "SearchResultsAugmentWithLiveData", false);
/*      */ 
/*  867 */               if (!captureLiveMetadata)
/*      */               {
/*  869 */                 captureLiveMetadata = (clientControlled != null) && (clientControlled.length() > 0);
/*      */               }
/*      */             }
/*      */ 
/*  873 */             if (captureLiveMetadata)
/*      */             {
/*  875 */               this.m_binder.putLocal("CapturedLiveMetadata", "1");
/*  876 */               int docsPerBatch = SharedObjects.getEnvironmentInt("ODMADocInfoDocsPerBatch", 25);
/*  877 */               String[][] sqlInfo = DataUtils.lookupSQL("ODMADocInfo");
/*  878 */               String sql = sqlInfo[0][0];
/*      */ 
/*  880 */               FieldInfo fi = new FieldInfo();
/*  881 */               localResults.getFieldInfo("dID", fi);
/*      */ 
/*  883 */               boolean mergedFields = false;
/*      */ 
/*  885 */               localResults.first();
/*  886 */               while (localResults.isRowPresent())
/*      */               {
/*  888 */                 StringBuffer whereClause = new StringBuffer(" AND dID IN (");
/*  889 */                 for (int j = 0; (j < docsPerBatch) && (localResults.isRowPresent()); ++j)
/*      */                 {
/*  891 */                   String dID = localResults.getStringValue(fi.m_index);
/*  892 */                   if (j == 0)
/*      */                   {
/*  894 */                     whereClause.append(dID);
/*      */                   }
/*      */                   else
/*      */                   {
/*  898 */                     whereClause.append(", " + dID);
/*      */                   }
/*  900 */                   localResults.next();
/*      */                 }
/*  902 */                 whereClause.append(")");
/*      */ 
/*  904 */                 ResultSet tempset = this.m_workspace.createResultSetSQL(sql + whereClause.toString());
/*  905 */                 DataResultSet odmaInfo = new DataResultSet();
/*  906 */                 odmaInfo.copy(tempset);
/*      */ 
/*  908 */                 if (!mergedFields)
/*      */                 {
/*  910 */                   localResults.mergeFields(odmaInfo);
/*      */                 }
/*  912 */                 localResults.merge("dID", odmaInfo, true);
/*      */               }
/*      */             }
/*  915 */             localResults.first();
/*      */ 
/*  917 */             boolean useOriginalName = true;
/*  918 */             if ((serverCount == 1) && (!searchedRemote))
/*      */             {
/*  920 */               this.m_binder.addResultSet("SearchResults", localResults);
/*  921 */               if (this.m_binder.getAllowMissing("ResultSet") != null)
/*      */               {
/*  923 */                 this.m_binder.addResultSet(this.m_binder.getAllowMissing("ResultSet"), rset);
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  928 */               this.m_binder.addResultSet(name, localResults);
/*  929 */               useOriginalName = false;
/*      */             }
/*      */ 
/*  932 */             Map rsets = binder.getResultSets();
/*  933 */             for (Map.Entry entry : rsets.entrySet())
/*      */             {
/*  935 */               String key = (String)entry.getKey();
/*  936 */               if (key.equalsIgnoreCase("SearchResults"))
/*      */               {
/*      */                 continue;
/*      */               }
/*      */ 
/*  941 */               if (!useOriginalName)
/*      */               {
/*  943 */                 key = name + key;
/*      */               }
/*  945 */               this.m_binder.addResultSet(key, (ResultSet)entry.getValue());
/*      */             }
/*      */ 
/*  948 */             if (rset.isRowPresent())
/*      */             {
/*  950 */               noMatches = false;
/*      */             }
/*      */ 
/*  954 */             boolean isDynConvEnabled = SharedObjects.getEnvValueAsBoolean("IsDynamicConverterEnabled", false);
/*      */ 
/*  956 */             if (isDynConvEnabled)
/*      */             {
/*  959 */               boolean isProxiedServer = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("IsProxiedServer"), false);
/*      */ 
/*  961 */               if (isProxiedServer)
/*      */               {
/*  963 */                 String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  964 */                 this.m_binder.putLocal("DCProxy", idcName);
/*      */ 
/*  967 */                 String conversionFormat = SharedObjects.getEnvironmentValue("conversionFormat");
/*  968 */                 if (conversionFormat == null)
/*      */                 {
/*  970 */                   conversionFormat = "";
/*      */                 }
/*  972 */                 this.m_binder.putLocal("DCProxy:conversionFormat", conversionFormat);
/*      */               }
/*      */               else
/*      */               {
/*  977 */                 String idcName = binder.getLocal("DCProxy");
/*  978 */                 if (idcName != null)
/*      */                 {
/*  980 */                   String conversionFormat = binder.getLocal("DCProxy:conversionFormat");
/*  981 */                   this.m_binder.putLocal(idcName + ":conversionFormat", conversionFormat);
/*      */                 }
/*      */               }
/*      */             }
/*      */ 
/*  986 */             computeResultsSideEffects(binder);
/*      */           }
/*      */           else
/*      */           {
/*  991 */             updateParameters.m_isError = true;
/*  992 */             String errMsg = updateParameters.m_errMsg;
/*  993 */             if (errMsg == null)
/*      */             {
/*  996 */               errMsg = "";
/*      */             }
/*  998 */             if (binder != null)
/*      */             {
/* 1000 */               updateParameters.m_errMsg = (binder.getAllowMissing("StatusMessage") + errMsg);
/* 1001 */               updateParameters.m_statusCode = DataBinderUtils.getInteger(binder, "StatusCode", -1);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1007 */           if ((!updateParameters.m_hasSearchResults) && (binder != null))
/*      */           {
/* 1009 */             updateParameters.m_hasSearchResults = true;
/* 1010 */             updateParameters.m_searchResults = binder;
/*      */           }
/*      */         }
/*      */ 
/* 1014 */         String status = "!csSuccess";
/* 1015 */         String capturedWebUrl = cgiWebUrl;
/* 1016 */         DataBinder binder = updateParameters.m_searchResults;
/* 1017 */         if (updateParameters.m_isError)
/*      */         {
/* 1019 */           if (updateParameters.m_statusCode >= 0)
/*      */           {
/* 1021 */             updateParameters.m_statusCode = -1;
/*      */           }
/*      */ 
/* 1024 */           String errMsg = updateParameters.m_errMsg;
/* 1025 */           if (updateParameters.m_exception != null)
/*      */           {
/* 1027 */             if (errMsg == null)
/*      */             {
/* 1029 */               errMsg = "";
/*      */             }
/* 1031 */             errMsg = updateParameters.m_exception.getMessage() + errMsg;
/*      */           }
/* 1033 */           if (errMsg == null)
/*      */           {
/* 1035 */             errMsg = LocaleUtils.encodeMessage("csResultSetMissing", null, "SearchResults");
/*      */           }
/*      */ 
/* 1038 */           status = errMsg;
/* 1039 */           hasError = true;
/*      */         }
/*      */ 
/* 1042 */         row.addElement("" + updateParameters.m_statusCode);
/* 1043 */         row.addElement(status);
/* 1044 */         row.addElement(name);
/* 1045 */         row.addElement(capturedWebUrl);
/* 1046 */         for (int j = 0; j < capturedResultFields.length; ++j)
/*      */         {
/* 1048 */           String val = null;
/* 1049 */           String key = capturedResultFields[j];
/* 1050 */           if (key.equals("TotalRows"))
/*      */           {
/* 1052 */             val = resultCount;
/*      */           }
/* 1054 */           else if (key.equals("HasMoreRows"))
/*      */           {
/* 1056 */             val = (hasMoreRows) ? "1" : "";
/*      */           }
/* 1058 */           else if (binder != null)
/*      */           {
/* 1060 */             val = binder.getLocal(key);
/*      */           }
/* 1062 */           if (val == null)
/*      */           {
/* 1064 */             if (defaultsRow != null)
/*      */             {
/* 1066 */               val = defaultsRow[j];
/*      */             }
/*      */             else
/*      */             {
/* 1070 */               val = "";
/*      */             }
/*      */           }
/* 1073 */           row.addElement(val);
/*      */         }
/* 1075 */         results.addRow(row);
/*      */       }
/*      */ 
/* 1078 */       this.m_binder.putLocal("TotalRows", "" + totalRows);
/* 1079 */       this.m_binder.putLocal("TotalDocsProcessed", "" + totalProcessed);
/* 1080 */       this.m_binder.putLocal("HasRowAndDocCount", "" + hasRowAndDocCount);
/* 1081 */       this.m_binder.addResultSet("EnterpriseSearchResults", results);
/* 1082 */       String userQueryTextEncoded = WebRequestUtils.encodeUrlSegmentForBrowser(userQueryText, this.m_binder, this);
/*      */ 
/* 1084 */       this.m_binder.putLocal("QueryText", userQueryTextEncoded);
/* 1085 */       if (noMatches)
/*      */       {
/* 1087 */         this.m_binder.putLocal("EmptyResult", "1");
/*      */       }
/* 1089 */       if (hasError)
/*      */       {
/* 1091 */         this.m_binder.putLocal("hasSearchError", "1");
/*      */       }
/*      */ 
/* 1094 */       computeSearchResultsPresentation();
/*      */ 
/* 1096 */       if (queryInterval != null)
/*      */       {
/* 1098 */         long t = queryInterval.getInterval();
/* 1099 */         IdcStringBuilder queryTimeStrBuf = new IdcStringBuilder(100);
/* 1100 */         queryTimeStrBuf.append("Execution time is ");
/* 1101 */         queryInterval.appendValueWithUnits(t, queryTimeStrBuf);
/* 1102 */         String msg = queryTimeStrBuf.toString();
/* 1103 */         this.m_sManager.debugTrace(msg);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*      */       boolean isSearchRetried;
/*      */       int i;
/*      */       CacheUpdateParameters updateParameters;
/* 1110 */       if (localUpdateParameters != null)
/*      */       {
/* 1112 */         boolean isSearchRetried = validateAndRetrySearch(localUpdateParameters.m_searchResults);
/*      */ 
/* 1114 */         if (isSearchRetried == true)
/*      */         {
/* 1116 */           return;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1126 */       for (int i = 0; i < updateParametersList.size(); ++i)
/*      */       {
/* 1128 */         CacheUpdateParameters updateParameters = (CacheUpdateParameters)updateParametersList.get(i);
/* 1129 */         if ((updateParameters.m_query != null) && (((updateParameters.m_queryDocInfoPending) || (updateParameters.m_queryDocListPending))))
/*      */         {
/* 1132 */           this.m_searchCache.releaseCache(updateParameters);
/*      */         }
/* 1134 */         updateParameters.release();
/*      */       }
/*      */ 
/* 1139 */       if (!DataBinderUtils.getBoolean(this.m_binder, "SearchNoDatabaseRelease", false))
/*      */       {
/* 1141 */         this.m_workspace.releaseConnection();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean validateAndRetrySearch(DataBinder searchResultsBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1149 */     boolean doRetrySearchQuery = false;
/*      */ 
/* 1151 */     if (searchResultsBinder != null)
/*      */     {
/* 1153 */       String statusMessage = searchResultsBinder.getAllowMissing("StatusMessage");
/* 1154 */       String exceptionMessage = searchResultsBinder.getAllowMissing("ExceptionMessage");
/* 1155 */       doRetrySearchQuery = checkRetrySearch(statusMessage, exceptionMessage, this.m_binder);
/*      */ 
/* 1157 */       if (doRetrySearchQuery == true)
/*      */       {
/* 1159 */         this.m_binder.putLocal("QueryText", this.m_binder.getLocal("OriginalUnencodedQueryText"));
/* 1160 */         this.m_binder.putLocal("IsRetry", "true");
/*      */ 
/* 1162 */         Report.trace("systemdatabase", "Retrying search as reserved word/character search error code received", null);
/*      */         try
/*      */         {
/* 1165 */           getSearchResults();
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1169 */           String msg = LocaleUtils.encodeMessage("csSearchRetryFailed", e.getMessage());
/*      */ 
/* 1171 */           throw new ServiceException(msg, e);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1176 */     return doRetrySearchQuery;
/*      */   }
/*      */ 
/*      */   protected boolean checkRetrySearch(String statusMsg, String exceptionMessage, DataBinder binder)
/*      */   {
/* 1181 */     if ((statusMsg == null) && (exceptionMessage == null))
/*      */     {
/* 1183 */       return false;
/*      */     }
/*      */ 
/* 1186 */     boolean doRetrySearch = false;
/* 1187 */     boolean isAlreadyRetried = DataBinderUtils.getBoolean(binder, "IsRetry", false);
/*      */ 
/* 1189 */     if (!isAlreadyRetried)
/*      */     {
/* 1191 */       CommonSearchConfig commonSearchConfigObject = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*      */ 
/* 1194 */       String supportReservedExpProcessingString = commonSearchConfigObject.getEngineValue("SupportReservedExpProcessing");
/* 1195 */       boolean supportReservedExpProcessing = StringUtils.convertToBool(supportReservedExpProcessingString, false);
/*      */ 
/* 1197 */       if (supportReservedExpProcessing == true)
/*      */       {
/* 1202 */         boolean isReservedExpError = false;
/*      */ 
/* 1204 */         isReservedExpError = commonSearchConfigObject.hasReservedExpErrorCode(statusMsg, exceptionMessage);
/*      */ 
/* 1207 */         if (isReservedExpError == true)
/*      */         {
/* 1209 */           binder.putLocal("escapeReservedStringsInSearch", "true");
/*      */         }
/*      */ 
/* 1212 */         if (isReservedExpError == true)
/*      */         {
/* 1214 */           doRetrySearch = true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1219 */     return doRetrySearch;
/*      */   }
/*      */ 
/*      */   public String computeQueryCacheKey(DataBinder binder)
/*      */   {
/* 1224 */     StringBuffer buf = new StringBuffer();
/* 1225 */     String[] queryFields = this.m_sManager.getCacheQueryFields();
/* 1226 */     for (int i = 0; i < queryFields.length; ++i)
/*      */     {
/* 1228 */       String text = this.m_binder.getAllowMissing(queryFields[i]);
/* 1229 */       if (text == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1233 */       buf.append('&');
/* 1234 */       buf.append(queryFields[i]);
/* 1235 */       buf.append('=');
/* 1236 */       buf.append(StringUtils.urlEncode(text));
/*      */     }
/*      */ 
/* 1239 */     String request = buf.toString();
/* 1240 */     return request;
/*      */   }
/*      */ 
/*      */   public void loadSortInformation(CacheUpdateParameters updateParameters)
/*      */   {
/* 1245 */     String sortField = this.m_binder.getLocal("SortField");
/* 1246 */     if ((sortField == null) || (sortField.length() <= 0))
/*      */       return;
/* 1248 */     String sortOrder = this.m_binder.getLocal("SortOrder");
/* 1249 */     boolean isAscending = true;
/* 1250 */     if ((sortOrder != null) && (sortOrder.length() > 0))
/*      */     {
/* 1252 */       char ch = sortOrder.charAt(0);
/* 1253 */       if ((ch == 'd') || (ch == 'D'))
/*      */       {
/* 1255 */         isAscending = false;
/*      */       }
/*      */     }
/*      */ 
/* 1259 */     updateParameters.m_sortField = sortField;
/* 1260 */     updateParameters.m_isSorted = true;
/* 1261 */     updateParameters.m_sortIsAscending = isAscending;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String fixupQuerySyntax(String queryText)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1273 */       queryText = SearchUtils.fixVerityDateFields(queryText, LocaleResources.m_searchFormat, this.m_binder.m_blDateFormat, this.m_binder.getFieldTypes());
/*      */ 
/* 1276 */       this.m_binder.putLocal("QueryText", queryText);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1280 */       String msg = LocaleUtils.encodeMessage("csSearchUnableFixupDates", null, queryText);
/*      */ 
/* 1282 */       createServiceException(e, msg);
/*      */     }
/*      */ 
/* 1285 */     return queryText;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeSearchResultsPresentation()
/*      */     throws ServiceException
/*      */   {
/* 1295 */     this.m_requestImplementor.m_isAfterActions = true;
/* 1296 */     this.m_requestImplementor.checkForFilterDataInput(this);
/*      */ 
/* 1302 */     String resultTemplateName = this.m_binder.getAllowMissing("ResultTemplate");
/* 1303 */     if (resultTemplateName != null)
/*      */     {
/* 1305 */       if (SharedObjects.getHtmlPage(resultTemplateName) != null)
/*      */       {
/* 1308 */         this.m_serviceData.m_htmlPage = resultTemplateName;
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1313 */       resultTemplateName = this.m_serviceData.m_htmlPage;
/*      */     }
/* 1315 */     if ((resultTemplateName == null) || 
/* 1317 */       (SharedPageMergerData.loadResultTemplateData(resultTemplateName, this.m_binder.getLocalData())))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 1324 */     String msg = LocaleUtils.encodeMessage("csMissingSearchTemplate", null, resultTemplateName);
/* 1325 */     msg = encodeRefererMessage(msg);
/* 1326 */     Report.trace(null, LocaleResources.localizeMessage(msg, null), null);
/*      */ 
/* 1329 */     if ((this.m_serviceData.m_htmlPage == null) || (this.m_serviceData.m_htmlPage.equals(resultTemplateName)))
/*      */       return;
/* 1331 */     SharedPageMergerData.loadResultTemplateData(this.m_serviceData.m_htmlPage, this.m_binder.getLocalData());
/*      */   }
/*      */ 
/*      */   public DataBinder prepareLocalSearch(IdcDateFormat searchDateFormat)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1343 */     String preparsedQueryText = this.m_binder.get("QueryText");
/* 1344 */     String queryText = this.m_queryConfig.fixUpAndValidateQuery(preparsedQueryText, this.m_binder, this);
/*      */ 
/* 1346 */     DataBinder requestBinder = new DataBinder();
/* 1347 */     requestBinder.copyLocalDataStateClone(this.m_binder);
/* 1348 */     prepareLocalSearchEnvironment(requestBinder, searchDateFormat);
/*      */ 
/* 1350 */     requestBinder.putLocal("PreparsedQueryText", queryText);
/* 1351 */     requestBinder.putLocal("QueryText", queryText);
/*      */ 
/* 1353 */     if ((this.m_searchCache != null) && (this.m_useCache) && (this.m_updateCache))
/*      */     {
/* 1355 */       setRangeForCaching(requestBinder);
/*      */     }
/*      */     else
/*      */     {
/* 1359 */       requestBinder.putLocal("StartRow", "" + this.m_startRow);
/* 1360 */       requestBinder.putLocal("ResultCount", "" + this.m_rowCount);
/*      */     }
/* 1362 */     if (DataBinderUtils.getLocalBoolean(this.m_binder, "IsJava", false))
/*      */     {
/* 1364 */       requestBinder.putLocal("isAppRequest", "1");
/* 1365 */       this.m_isAppSearch = true;
/*      */     }
/*      */ 
/* 1368 */     PluginFilters.filter("prepareSearchRequestBinder", this.m_workspace, this.m_binder, this);
/* 1369 */     return requestBinder;
/*      */   }
/*      */ 
/*      */   public void prepareLocalSearchEnvironment(DataBinder requestBinder, IdcDateFormat searchDateFormat)
/*      */   {
/* 1379 */     Properties env = this.m_binder.getEnvironment();
/* 1380 */     Properties requestEnv = new Properties(env);
/*      */ 
/* 1385 */     setCachedObject("RequestBinder", requestBinder);
/*      */ 
/* 1387 */     Map cfg = this.m_queryConfig.getEngineRules();
/* 1388 */     DataBinder.mergeHashTables(requestEnv, cfg);
/* 1389 */     requestBinder.setEnvironment(requestEnv);
/* 1390 */     String[] queryFields = this.m_sManager.getCacheQueryFields();
/* 1391 */     CollectionUtils.mergeMaps(this.m_binder.getLocalData(), requestBinder.getLocalData(), queryFields);
/* 1392 */     requestBinder.m_blDateFormat = searchDateFormat;
/*      */   }
/*      */ 
/*      */   public void doLocalSearch(DataBinder binder) throws ServiceException, DataException
/*      */   {
/* 1397 */     if (this.m_sManager.getDebugIsWaitSearch())
/*      */     {
/* 1399 */       int debugWaitTime = DataBinderUtils.getInteger(this.m_binder, "Wait", 0);
/* 1400 */       if (debugWaitTime > 0)
/*      */       {
/* 1402 */         Report.trace("searchquery", "Artificially waiting " + debugWaitTime + " milliseconds", null);
/* 1403 */         SystemUtils.sleep(debugWaitTime);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1409 */     if ((this.m_targetDocClasses != null) && (!this.m_targetDocClasses.isEmpty()))
/*      */     {
/* 1411 */       IdcStringBuilder docClassIn = new IdcStringBuilder();
/*      */ 
/* 1413 */       for (String dclass : this.m_targetDocClasses)
/*      */       {
/* 1415 */         if (docClassIn.length() > 0)
/*      */         {
/* 1417 */           docClassIn.append(',');
/*      */         }
/* 1419 */         QueryUtils.appendValueWithQuotes(docClassIn, dclass);
/*      */       }
/*      */ 
/* 1422 */       IdcStringBuilder DocMetaSetSelect = new IdcStringBuilder();
/* 1423 */       IdcStringBuilder DocMetaSetFrom = new IdcStringBuilder();
/* 1424 */       IdcStringBuilder DocMetaSetWhere = new IdcStringBuilder();
/*      */ 
/* 1426 */       if (docClassIn.length() > 0)
/*      */       {
/* 1428 */         DocMetaSetWhere.append(" AND dDocClass IN(");
/* 1429 */         DocMetaSetWhere.append(docClassIn);
/* 1430 */         DocMetaSetWhere.append(')');
/*      */       }
/*      */ 
/* 1433 */       Iterator commonSetsIter = DocClassUtils.getCommonTableSets(this.m_targetDocClasses);
/* 1434 */       while (commonSetsIter.hasNext())
/*      */       {
/* 1436 */         String table = (String)commonSetsIter.next();
/*      */ 
/* 1438 */         DocMetaSetSelect.append(", ");
/* 1439 */         DocMetaSetSelect.append(table);
/* 1440 */         DocMetaSetSelect.append(".*");
/*      */ 
/* 1442 */         DocMetaSetFrom.append(", ");
/* 1443 */         DocMetaSetFrom.append(table);
/*      */ 
/* 1445 */         DocMetaSetWhere.append(" AND Revisions.dID=");
/* 1446 */         DocMetaSetWhere.append(table);
/* 1447 */         DocMetaSetWhere.append(".dID");
/*      */       }
/*      */ 
/* 1450 */       binder.putLocal("DocMetaSetSelect", DocMetaSetSelect.toString());
/* 1451 */       binder.putLocal("DocMetaSetFrom", DocMetaSetFrom.toString());
/* 1452 */       binder.putLocal("DocMetaSetWhere", DocMetaSetWhere.toString());
/*      */     }
/*      */ 
/* 1455 */     this.m_searchActionType = 0;
/* 1456 */     if (PluginFilters.filter("doLocalSearch", this.m_workspace, binder, this) == 0)
/*      */     {
/* 1458 */       retrieveSearchInfo(binder);
/*      */     }
/* 1460 */     DataResultSet drset = (DataResultSet)binder.getResultSet("SearchResults");
/* 1461 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/* 1463 */       String warningValues = this.m_binder.getLocal("EmptyResultWarningValues");
/* 1464 */       String queryText = binder.getLocal("PreparsedQueryText");
/* 1465 */       if ((warningValues != null) && (!DataBinderUtils.getBoolean(this.m_binder, "SuppressEmptyResultWarning", false)))
/*      */       {
/* 1467 */         setConditionVar("IsWarningPage", true);
/* 1468 */         String valueExample = this.m_binder.getLocal("EmptyResultWarningExample");
/* 1469 */         String msg = LocaleResources.getString("csSearchEmptyResultValueWarning", this, new Object[] { queryText, warningValues, valueExample });
/*      */ 
/* 1471 */         this.m_binder.putLocal("WarningMessage", msg);
/* 1472 */         this.m_binder.putLocal("WarningPageTitle", "wwSearchEmptyResultWarning");
/* 1473 */         throw new ServiceException(msg);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1478 */       String isClientControlled = this.m_binder.getAllowMissing("ClientControlled");
/* 1479 */       if ((isClientControlled == null) || (!isClientControlled.equals("WebDAVClient")))
/*      */         return;
/* 1481 */       SchemaFieldConfig m_fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/* 1482 */       SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*      */ 
/* 1484 */       int numFields = drset.getNumFields();
/* 1485 */       List myList = new ArrayList();
/* 1486 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1488 */         Vector values = new Vector();
/* 1489 */         Map args = new HashMap();
/* 1490 */         for (int x = 0; x < numFields; ++x)
/*      */         {
/* 1492 */           String fieldName = drset.getFieldName(x);
/* 1493 */           String fieldValue = drset.getStringValue(x);
/* 1494 */           String result = fieldValue;
/* 1495 */           SchemaFieldData field = (SchemaFieldData)m_fields.getData(fieldName);
/* 1496 */           if ((field != null) && (field.get("dIsOptionList").equals("1")))
/*      */           {
/* 1498 */             String viewName = field.get("dOptionListKey");
/* 1499 */             if ((viewName != null) && (viewName.startsWith("view://")))
/*      */             {
/* 1501 */               viewName = viewName.substring(7);
/* 1502 */               SchemaViewData viewData = (SchemaViewData)views.getData(viewName);
/* 1503 */               if (viewData != null)
/*      */               {
/* 1505 */                 args.put("primaryKey", new String[] { fieldValue });
/* 1506 */                 ResultSet rset = viewData.getViewValues(args);
/* 1507 */                 if (rset != null)
/*      */                 {
/* 1509 */                   result = viewData.computeDisplayValue(rset, this, 1);
/* 1510 */                   if ((result == null) || (result.length() < 1))
/*      */                   {
/* 1512 */                     result = fieldValue;
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/* 1518 */           values.add(result);
/*      */         }
/* 1520 */         myList.add(values);
/*      */       }
/* 1522 */       drset.setRows(myList);
/* 1523 */       binder.addResultSet("SearchResults", drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void searchForDocumentMetadata()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1537 */     loadSearchCache();
/*      */ 
/* 1539 */     String docListStr = this.m_binder.getLocal("docNameList");
/* 1540 */     if ((docListStr == null) || (docListStr.length() == 0))
/*      */     {
/* 1542 */       this.m_binder.putLocal("TotalRows", "0");
/* 1543 */       return;
/*      */     }
/* 1545 */     String[] docList = StringUtils.makeStringArrayFromSequence(docListStr);
/* 1546 */     DataBinder binder = new DataBinder();
/* 1547 */     searchForDocumentMetadataImplement(docList, binder, LocaleResources.m_iso8601Format);
/* 1548 */     ResultSet results = binder.getResultSet("SearchResults");
/* 1549 */     if (results == null)
/*      */       return;
/* 1551 */     this.m_binder.addResultSet("SearchResults", results);
/*      */   }
/*      */ 
/*      */   public void searchForDocumentMetadataImplement(String[] docList, DataBinder searchBinder, IdcDateFormat searchDateFormat)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1566 */     String queryText = this.m_queryConfig.createQueryForPrimaryKeys(null, docList);
/* 1567 */     prepareLocalSearchEnvironment(searchBinder, searchDateFormat);
/* 1568 */     searchBinder.putLocal("QueryText", queryText);
/* 1569 */     searchBinder.putLocal("StartRow", "1");
/* 1570 */     searchBinder.putLocal("ResultCount", "" + docList.length);
/*      */ 
/* 1572 */     this.m_searchActionType = 0;
/* 1573 */     retrieveSearchInfo(searchBinder);
/*      */   }
/*      */ 
/*      */   public DataResultSet getResults(DataBinder binder, boolean isOnlyResult)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1580 */     String curPage = this.m_binder.getAllowMissing("PageNumber");
/* 1581 */     int totalRows = DataBinderUtils.getInteger(binder, "TotalRows", -1);
/* 1582 */     int rsetStartRow = this.m_startRow - DataBinderUtils.getLocalInteger(binder, "StartRow", 1) + 1;
/*      */ 
/* 1584 */     if (totalRows == 0)
/*      */     {
/* 1586 */       return new DataResultSet();
/*      */     }
/*      */ 
/* 1589 */     int numPages = NumberUtils.parseInteger(curPage, 1);
/* 1590 */     int lastPageRecords = 0;
/*      */ 
/* 1592 */     if (isOnlyResult)
/*      */     {
/* 1594 */       if (totalRows >= 0)
/*      */       {
/* 1597 */         if (totalRows > 0)
/* 1598 */           totalRows = CommonSearchConfig.computeRowCount(binder, totalRows, false);
/*      */         else {
/* 1600 */           totalRows = CommonSearchConfig.computeRowCount(binder, totalRows, true);
/*      */         }
/* 1602 */         numPages = totalRows / this.m_rowCount;
/* 1603 */         lastPageRecords = totalRows % this.m_rowCount;
/*      */ 
/* 1605 */         if (lastPageRecords > 0)
/*      */         {
/* 1607 */           ++numPages;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1612 */         int maxResults = CommonSearchConfig.computeRowCount(binder, totalRows, true);
/* 1613 */         int recordsDisplayed = numPages * this.m_rowCount;
/* 1614 */         if (DataBinderUtils.getBoolean(binder, "HasMoreRows", false))
/*      */         {
/* 1616 */           if (recordsDisplayed < maxResults)
/*      */           {
/* 1618 */             ++numPages;
/*      */           }
/*      */ 
/* 1621 */           int totalRecordsBeingDisplayed = numPages * this.m_rowCount;
/* 1622 */           if (totalRecordsBeingDisplayed > maxResults)
/*      */           {
/* 1624 */             lastPageRecords = maxResults % this.m_rowCount;
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1630 */           DataResultSet srset = (DataResultSet)binder.getResultSet("SearchResults");
/* 1631 */           int rows = srset.getNumRows();
/* 1632 */           if ((rows > this.m_rowCount) && (recordsDisplayed < maxResults))
/*      */           {
/* 1634 */             ++numPages;
/*      */           }
/*      */ 
/* 1637 */           int totalRecordsBeingDisplayed = numPages * this.m_rowCount;
/* 1638 */           if (totalRecordsBeingDisplayed > maxResults)
/*      */           {
/* 1640 */             lastPageRecords = maxResults % this.m_rowCount;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1646 */       this.m_binder.putLocal("NumPages", Integer.toString(numPages));
/*      */ 
/* 1649 */       if (curPage == null)
/*      */       {
/* 1652 */         int startRow = DataBinderUtils.getInteger(this.m_binder, "StartRow", -1);
/* 1653 */         int rowCount = DataBinderUtils.getInteger(this.m_binder, "ResultCount", -1);
/* 1654 */         if ((startRow > 0) && (rowCount > 0))
/*      */         {
/* 1656 */           int pageNumber = startRow / rowCount + 1;
/* 1657 */           curPage = Integer.toString(pageNumber);
/*      */         }
/* 1659 */         if (startRow <= 0)
/*      */         {
/* 1661 */           startRow = 1;
/* 1662 */           curPage = "1";
/*      */         }
/* 1664 */         this.m_binder.putLocal("StartRow", "" + startRow);
/* 1665 */         this.m_binder.putLocal("EndRow", "" + this.m_rowCount);
/* 1666 */         this.m_binder.putLocal("PageNumber", curPage);
/*      */       }
/*      */ 
/* 1670 */       String[] pageNumListFields = { "HeaderPageNumber", "PageReference", "PageNumber", "StartRow", "EndRow" };
/*      */ 
/* 1674 */       DataResultSet drset = new DataResultSet(pageNumListFields);
/* 1675 */       for (int i = 0; i < numPages; ++i)
/*      */       {
/* 1677 */         Vector v = drset.createEmptyRow();
/* 1678 */         String pageNum = Integer.toString(i + 1);
/* 1679 */         v.setElementAt(pageNum, 0);
/* 1680 */         v.setElementAt(pageNum, 1);
/* 1681 */         v.setElementAt(curPage, 2);
/*      */ 
/* 1683 */         int startRow = this.m_rowCount * i + 1;
/* 1684 */         int endRow = this.m_rowCount * (i + 1);
/*      */ 
/* 1686 */         if ((endRow > totalRows) && (totalRows != -1) && (totalRows != 0))
/*      */         {
/* 1688 */           endRow = totalRows;
/*      */         }
/*      */ 
/* 1692 */         if ((i == numPages - 1) && (lastPageRecords > 0))
/*      */         {
/* 1694 */           endRow = startRow + lastPageRecords - 1;
/*      */         }
/*      */ 
/* 1697 */         v.setElementAt(Integer.toString(startRow), 3);
/* 1698 */         v.setElementAt(Integer.toString(endRow), 4);
/*      */ 
/* 1700 */         drset.addRow(v);
/*      */       }
/* 1702 */       this.m_binder.addResultSet("NavigationPages", drset);
/*      */     }
/*      */ 
/* 1706 */     DataResultSet raw = (DataResultSet)binder.getResultSet("SearchResults");
/* 1707 */     DataResultSet cooked = new DataResultSet();
/* 1708 */     cooked.copyFieldInfo(raw);
/* 1709 */     cooked.setDateFormat(raw.getDateFormat());
/*      */ 
/* 1715 */     int currentPage = NumberUtils.parseInteger(curPage, 1);
/*      */     int noOfRowsToDispOnPage;
/*      */     int noOfRowsToDispOnPage;
/* 1716 */     if ((currentPage == numPages) && (lastPageRecords > 0))
/*      */     {
/* 1718 */       noOfRowsToDispOnPage = lastPageRecords;
/*      */     }
/*      */     else
/*      */     {
/* 1722 */       noOfRowsToDispOnPage = this.m_rowCount;
/*      */     }
/*      */ 
/* 1725 */     int endRow = this.m_startRow + noOfRowsToDispOnPage - 1;
/* 1726 */     this.m_binder.putLocal("EndRow", "" + endRow);
/*      */ 
/* 1728 */     int count = raw.getNumRows();
/* 1729 */     int endCount = rsetStartRow + noOfRowsToDispOnPage;
/* 1730 */     if (count < endCount)
/*      */     {
/* 1733 */       endCount = count + 1;
/*      */     }
/*      */ 
/* 1736 */     for (int i = rsetStartRow; i < endCount; ++i)
/*      */     {
/* 1738 */       Vector v = raw.getRowValues(i - 1);
/* 1739 */       cooked.addRow((Vector)v.clone());
/*      */     }
/*      */ 
/* 1742 */     return cooked;
/*      */   }
/*      */ 
/*      */   public void setRangeForCaching(DataBinder binder)
/*      */   {
/* 1747 */     int end = this.m_startRow + this.m_rowCount - 1;
/* 1748 */     int start = this.m_startRow;
/* 1749 */     int count = this.m_rowCount;
/*      */ 
/* 1753 */     if (end > SearchLoader.m_hardCacheLimit)
/*      */     {
/* 1755 */       this.m_updateCache = false;
/*      */     }
/*      */     else
/*      */     {
/* 1759 */       start = 1;
/* 1760 */       if (end <= SearchLoader.m_softCacheLimit)
/*      */       {
/* 1762 */         count = SearchLoader.m_softCacheLimit;
/*      */       }
/*      */       else
/*      */       {
/* 1766 */         count = SearchLoader.m_hardCacheLimit;
/*      */       }
/*      */     }
/*      */ 
/* 1770 */     binder.putLocal("StartRow", "" + start);
/* 1771 */     binder.putLocal("ResultCount", "" + count);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getChangedSearchDocItems()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1803 */     loadSearchCache();
/* 1804 */     boolean isDelete = DataBinderUtils.getLocalBoolean(this.m_binder, "isDeletedItems", false);
/* 1805 */     String resultSetKey = this.m_binder.getLocal("searchResultsName");
/* 1806 */     if ((resultSetKey == null) || (resultSetKey.length() == 0))
/*      */     {
/* 1808 */       resultSetKey = (isDelete) ? "DocDeleted" : "DocsAdded";
/*      */     }
/*      */ 
/* 1811 */     if (isDelete)
/*      */     {
/* 1813 */       computeHistoryTime("startTime");
/* 1814 */       computeHistoryTime("endTime");
/*      */     }
/*      */ 
/* 1817 */     int maxRows = DataBinderUtils.getInteger(this.m_binder, "MaxChangedSearchRows", 200);
/* 1818 */     if (isDelete)
/*      */     {
/* 1820 */       executeLimitedQuery("QdocDeletedOrExpired", resultSetKey, maxRows);
/*      */     }
/*      */     else
/*      */     {
/* 1824 */       executeLimitedQuery("QdocCurrentChanged", resultSetKey, maxRows);
/*      */ 
/* 1826 */       retrieveSearchMetadataForExplicitlyReferencedDocuments(resultSetKey);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void computeHistoryTime(String key)
/*      */     throws DataException
/*      */   {
/* 1842 */     String historyVal = DocumentInfoCacheUtils.getActionDateConvertedTimestamp(this.m_binder, key, true);
/*      */ 
/* 1844 */     this.m_binder.putLocal(key + "History", historyVal);
/*      */   }
/*      */ 
/*      */   public void retrieveSearchMetadataForExplicitlyReferencedDocuments(String resultSetName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1857 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(resultSetName);
/* 1858 */     if (drset.isEmpty())
/*      */     {
/* 1860 */       return;
/*      */     }
/* 1862 */     String providerName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1863 */     String[][] values = ResultSetUtils.createStringTable(drset, new String[] { "dDocName", "dID", "dActionDate" });
/*      */ 
/* 1865 */     int nameIndex = 0;
/*      */ 
/* 1867 */     int maxDocNamesPerQuery = 50;
/* 1868 */     List cacheRows = new ArrayList(values.length);
/* 1869 */     for (int i = 0; i < values.length; i += maxDocNamesPerQuery)
/*      */     {
/* 1871 */       int endRange = i + maxDocNamesPerQuery;
/* 1872 */       if (endRange > values.length)
/*      */       {
/* 1874 */         endRange = values.length;
/*      */       }
/* 1876 */       String[] newArray = new String[endRange - i];
/* 1877 */       for (int j = 0; j < endRange - i; ++j)
/*      */       {
/* 1879 */         newArray[j] = values[(i + j)][nameIndex];
/*      */       }
/* 1881 */       DataBinder binder = new DataBinder();
/* 1882 */       CacheUpdateParameters updateParameters = new CacheUpdateParameters("", this.m_binder, providerName, null, this.m_sharedSearchTime, this.m_queryConfig.getCurrentEngineConfig(), this);
/*      */ 
/* 1884 */       updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_INFO;
/* 1885 */       updateParameters.m_captureCacheRows = true;
/* 1886 */       setCachedObject("CacheUpdateParameters", updateParameters);
/* 1887 */       searchForDocumentMetadataImplement(newArray, binder, LocaleResources.m_iso8601Format);
/* 1888 */       DataResultSet results = (DataResultSet)binder.getResultSet("SearchResults");
/* 1889 */       int numRowsRetrieved = 0;
/* 1890 */       if (results != null)
/*      */       {
/* 1892 */         numRowsRetrieved = results.getNumRows();
/* 1893 */         this.m_searchCache.updateCache(updateParameters, binder);
/*      */       }
/* 1895 */       if ((numRowsRetrieved != newArray.length) && (this.m_useCache))
/*      */       {
/* 1897 */         Report.warning("searchcache", null, "csSearchDidNotFillInNewRowsWithMetadata", new Object[] { Integer.valueOf(newArray.length), "" + numRowsRetrieved });
/*      */       }
/*      */ 
/* 1900 */       if (updateParameters.m_capturedCacheRows == null)
/*      */         continue;
/* 1902 */       Collections.addAll(cacheRows, updateParameters.m_capturedCacheRows);
/*      */     }
/*      */ 
/* 1905 */     setCachedObject("SearchCacheRows", cacheRows);
/* 1906 */     setCachedObject("SearchDocValues", values);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getHighlightInfo() throws ServiceException, DataException
/*      */   {
/* 1912 */     String queryText = this.m_binder.getLocal("QueryText");
/* 1913 */     String oldQueryText = queryText;
/* 1914 */     queryText = this.m_queryConfig.prepareQueryText(queryText, this.m_binder, this);
/* 1915 */     queryText = this.m_queryConfig.fixUpAndValidateQuery(queryText, this.m_binder, this);
/* 1916 */     if (queryText != null)
/*      */     {
/* 1918 */       this.m_binder.putLocal("QueryText", queryText);
/*      */ 
/* 1920 */       this.m_binder.putLocal("OriginalQueryText", oldQueryText);
/*      */     }
/* 1922 */     this.m_searchActionType = 1;
/* 1923 */     retrieveSearchInfo(this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getExternalSecurityInfo() throws ServiceException
/*      */   {
/* 1929 */     retrieveDocInfo(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getExternalDocInfo() throws ServiceException
/*      */   {
/* 1935 */     retrieveDocInfo(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setExternalDocInfoFields() throws ServiceException
/*      */   {
/* 1941 */     Vector v = StringUtils.parseArray(this.m_fieldNames, ',', '^');
/* 1942 */     if (v == null)
/*      */       return;
/* 1944 */     for (int i = 0; i < v.size(); ++i)
/*      */     {
/* 1946 */       String field = (String)v.elementAt(i);
/* 1947 */       if (field == null)
/*      */         continue;
/* 1949 */       String val = this.m_binder.getAllowMissing(field);
/* 1950 */       setConditionVar(field, (val != null) && (val.length() > 0));
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void viewDoc()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1960 */     String queryText = this.m_binder.getLocal("QueryText");
/* 1961 */     String oldQueryText = queryText;
/* 1962 */     queryText = this.m_queryConfig.prepareQueryText(queryText, this.m_binder, this);
/* 1963 */     queryText = this.m_queryConfig.fixUpAndValidateQuery(queryText, this.m_binder, this);
/* 1964 */     if (queryText != null)
/*      */     {
/* 1966 */       this.m_binder.putLocal("QueryText", queryText);
/*      */ 
/* 1968 */       this.m_binder.putLocal("OriginalQueryText", oldQueryText);
/*      */     }
/* 1970 */     this.m_searchActionType = 2;
/* 1971 */     retrieveSearchInfo(this.m_binder);
/*      */   }
/*      */ 
/*      */   public void computeResultsSideEffects(DataBinder binder) throws ServiceException, DataException
/*      */   {
/* 1976 */     PluginFilters.filter("getEnterpriseSearchResults", this.m_workspace, binder, this);
/*      */   }
/*      */ 
/*      */   protected void retrieveSearchInfo(DataBinder binder)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1982 */     checkConfiguration();
/*      */ 
/* 1984 */     if (this.m_fieldNames == null)
/*      */     {
/* 1986 */       throw new AssertionError("m_fieldNames is null in SearchService");
/*      */     }
/*      */     try
/*      */     {
/* 1990 */       this.m_resultBinder = this.m_sManager.retrieveSearchInfoAsBinder(binder, this.m_fieldNames, this.m_numFieldNames, this.m_searchActionType, this);
/*      */ 
/* 1994 */       boolean readResult = true;
/*      */ 
/* 1996 */       if (this.m_searchActionType == 1)
/*      */       {
/* 1998 */         String hlType = binder.getLocal("HighlightType");
/*      */ 
/* 2000 */         if ((hlType != null) && (hlType.length() > 0) && (!hlType.equals("PdfHighlight")))
/*      */         {
/* 2004 */           checkForDynamicContent(binder);
/* 2005 */           readResult = false;
/*      */         }
/*      */       }
/* 2008 */       else if (this.m_searchActionType == 2)
/*      */       {
/* 2011 */         checkForDynamicContent(binder);
/* 2012 */         readResult = false;
/*      */       }
/*      */ 
/* 2015 */       if (!readResult)
/*      */       {
/* 2017 */         return;
/*      */       }
/*      */ 
/* 2020 */       DataBinder tmpBinder = binder.createShallowCopy();
/* 2021 */       tmpBinder.merge(this.m_resultBinder);
/*      */ 
/* 2023 */       if (this.m_searchActionType == 0)
/*      */       {
/* 2025 */         DataResultSet drset = (DataResultSet)tmpBinder.getResultSet("SearchResults");
/* 2026 */         if (drset != null)
/*      */         {
/* 2028 */           drset.setDateFormat(LocaleResources.m_iso8601Format);
/*      */         }
/*      */ 
/* 2031 */         if (StringUtils.convertToBool(tmpBinder.getLocal("SortSearchResultAfterQuery"), false))
/*      */         {
/* 2033 */           sortResult(drset, tmpBinder);
/*      */         }
/*      */ 
/* 2037 */         if (this.m_searchCache != null)
/*      */         {
/* 2039 */           this.m_searchCache.updateSearchData(drset, this.m_rowCount);
/*      */         }
/*      */       }
/*      */ 
/* 2043 */       this.m_result = null;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2047 */       createServiceException(e, null);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 2052 */       int line = e.m_parseInfo.m_parseLine;
/* 2053 */       int charOffset = e.m_parseInfo.m_parseCharOffset;
/* 2054 */       String msg = LocaleUtils.encodeMessage("csSearchUnableToParseScript", e.getMessage(), "" + (line + 1), "" + (charOffset + 1));
/*      */ 
/* 2058 */       if (this.m_result != null)
/*      */       {
/* 2060 */         BufferedReader reader = null;
/*      */         try
/*      */         {
/* 2063 */           reader = new BufferedReader(new StringReader(this.m_result));
/* 2064 */           for (int i = 0; i < line; ++i)
/*      */           {
/* 2066 */             reader.readLine();
/*      */           }
/* 2068 */           for (int j = 0; j < 3; ++j)
/*      */           {
/* 2070 */             if (!reader.ready())
/*      */               continue;
/* 2072 */             msg = LocaleUtils.appendMessage("\n->" + reader.readLine(), msg);
/*      */           }
/*      */ 
/* 2075 */           reader.close();
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/* 2079 */           ignore.printStackTrace();
/*      */         }
/*      */       }
/*      */ 
/* 2083 */       createServiceException(null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkForDynamicContent(DataBinder binder)
/*      */     throws IOException, ParseSyntaxException, DataException, ServiceException
/*      */   {
/* 2090 */     String format = binder.getAllowMissing("dWebExtension");
/* 2091 */     if ((format == null) || (!format.equals("hcsp")))
/*      */       return;
/* 2093 */     DynamicHtml dynHtml = new DynamicHtml();
/* 2094 */     StringReader sReader = new StringReader(this.m_result);
/* 2095 */     dynHtml.loadHtml(sReader, null, true);
/*      */     try
/*      */     {
/* 2099 */       DataTransformationUtils.mergeInDynamicData(this.m_workspace, this.m_binder, dynHtml);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2103 */       String msg = LocaleUtils.encodeMessage("csSearchDynamicFileError", e.getMessage(), dynHtml.m_fileName);
/*      */ 
/* 2105 */       this.m_binder.putLocal("StatusCode", "-1");
/* 2106 */       this.m_binder.putLocal("StatusMessageKey", msg);
/* 2107 */       this.m_binder.putLocal("StatusMessage", msg);
/*      */     }
/*      */ 
/* 2110 */     setCachedObject("ParsedResponseTemplate", dynHtml);
/*      */ 
/* 2112 */     this.m_binder.putLocal("TemplateClass", "IdcDynamicFile");
/* 2113 */     this.m_binder.putLocal("TemplateType", format);
/* 2114 */     this.m_result = null;
/*      */ 
/* 2118 */     Action currentAction = getCurrentAction();
/* 2119 */     ResultSet rset = null;
/* 2120 */     if (currentAction.getNumParams() > 0)
/*      */     {
/* 2122 */       String rsetName = currentAction.getParamAt(0);
/* 2123 */       rset = this.m_binder.removeResultSet(rsetName);
/* 2124 */       if ((rset != null) && (rset.isRowPresent()))
/*      */       {
/* 2126 */         int n = rset.getNumFields();
/* 2127 */         FieldInfo fi = new FieldInfo();
/* 2128 */         for (int i = 0; i < n; ++i)
/*      */         {
/* 2130 */           rset.getIndexFieldInfo(i, fi);
/* 2131 */           String refFieldName = "ref:" + fi.m_name;
/* 2132 */           this.m_binder.putLocal(refFieldName, rset.getStringValue(i));
/*      */         }
/*      */ 
/* 2136 */         String docName = this.m_binder.getLocal("dDocName");
/* 2137 */         if (docName != null)
/*      */         {
/* 2139 */           this.m_binder.putLocal("ref:dDocName", docName);
/* 2140 */           this.m_binder.removeLocal("dDocName");
/*      */         }
/* 2142 */         String webExtension = this.m_binder.getLocal("dWebExtension");
/* 2143 */         if (webExtension != null)
/*      */         {
/* 2145 */           this.m_binder.putLocal("ref:dWebExtension", webExtension);
/* 2146 */           this.m_binder.removeLocal("dWebExtesion");
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2152 */     executeService("LOAD_DOC_ENVIRONMENT");
/*      */   }
/*      */ 
/*      */   protected void retrieveDocInfo(boolean isSecurityInfo)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 2160 */       String docName = this.m_binder.get("dDocName");
/* 2161 */       if (docName.length() == 0)
/*      */       {
/* 2163 */         String msg = LocaleUtils.encodeMessage("csFieldNotFound", null, "dDocName");
/*      */ 
/* 2165 */         createServiceException(null, msg);
/*      */       }
/*      */ 
/* 2168 */       this.m_resultBinder = this.m_sManager.retrieveDocInfo(this.m_binder, this.m_fieldNames, this.m_numFieldNames, docName, isSecurityInfo);
/*      */ 
/* 2171 */       DataBinder binder = this.m_binder.createShallowCopy();
/* 2172 */       binder.merge(this.m_resultBinder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2176 */       createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkConfiguration() throws ServiceException
/*      */   {
/* 2182 */     DataResultSet drset = SharedObjects.getTable("SearchCollections");
/* 2183 */     if (drset == null)
/*      */     {
/* 2185 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "SearchCollections");
/*      */ 
/* 2187 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 2190 */     this.m_searchColls = drset;
/*      */   }
/*      */ 
/*      */   public void notifyNextRow(String rsetName, boolean hasNext)
/*      */     throws IOException
/*      */   {
/* 2198 */     if (!hasNext)
/*      */     {
/* 2200 */       return;
/*      */     }
/*      */ 
/* 2205 */     SearchUtils.loadCollectionInfo(this.m_binder.getActiveAllowMissing("sCollectionID"), this.m_binder);
/*      */ 
/* 2207 */     this.m_binder.putLocal("collectionLoaded", "1");
/*      */ 
/* 2210 */     super.notifyNextRow(rsetName, hasNext);
/*      */   }
/*      */ 
/*      */   public void doResponse(boolean isError, ServiceException err)
/*      */     throws ServiceException
/*      */   {
/* 2222 */     Object[] filterParams = { new Boolean(isError), err };
/* 2223 */     setCachedObject("preDoResponse:parameters", filterParams);
/* 2224 */     if (!executeFilter("preDoSearchResponse"))
/*      */     {
/* 2226 */       return;
/*      */     }
/* 2228 */     isError = ((Boolean)filterParams[0]).booleanValue();
/* 2229 */     err = (ServiceException)filterParams[1];
/*      */ 
/* 2231 */     boolean isPdfHighlight = false;
/*      */ 
/* 2233 */     if (this.m_searchActionType == 1)
/*      */     {
/* 2235 */       String hlType = this.m_binder.getAllowMissing("HighlightType");
/*      */ 
/* 2237 */       if ((hlType != null) && (hlType.length() > 0) && (hlType.equals("PdfHighlight")))
/*      */       {
/* 2240 */         isPdfHighlight = true;
/*      */       }
/*      */     }
/* 2243 */     if ((this.m_searchActionType == 0) || (isPdfHighlight) || (this.m_result == null))
/*      */     {
/* 2245 */       super.doResponse(isError, err);
/* 2246 */       return;
/*      */     }
/* 2248 */     if ((isError) || (this.m_binder.m_isJava))
/*      */     {
/* 2250 */       if (this.m_binder.m_isJava)
/*      */       {
/* 2252 */         sendDataBinder(isError);
/*      */       }
/*      */       else
/*      */       {
/* 2256 */         buildResponsePage(isError);
/*      */       }
/* 2258 */       return;
/*      */     }
/*      */ 
/* 2261 */     IdcCharArrayWriter writer = this.m_pageMerger.getTemporaryWriter();
/*      */     try
/*      */     {
/* 2266 */       boolean addTextHeaderAndBottom = true;
/*      */ 
/* 2268 */       if (this.m_searchActionType == 1)
/*      */       {
/* 2270 */         String hlType = this.m_binder.getAllowMissing("HighlightType");
/* 2271 */         if ((hlType != null) && (hlType.length() > 0) && (hlType.equals("HtmlHighlight")))
/*      */         {
/* 2273 */           addTextHeaderAndBottom = false;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2278 */         String viewType = this.m_binder.getAllowMissing("ViewType");
/* 2279 */         if ((viewType != null) && (viewType.length() > 0) && (viewType.equals("ViewHtml")))
/*      */         {
/* 2281 */           addTextHeaderAndBottom = false;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2286 */       DynamicHtml dynHtml = null;
/* 2287 */       if (addTextHeaderAndBottom)
/*      */       {
/* 2289 */         dynHtml = SharedObjects.getHtmlResource("searchapi_text_header");
/* 2290 */         if (dynHtml != null)
/*      */         {
/* 2292 */           dynHtml.outputHtml(writer, this.m_pageMerger);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2297 */       writer.write(this.m_result);
/*      */ 
/* 2300 */       if (addTextHeaderAndBottom)
/*      */       {
/* 2302 */         dynHtml = SharedObjects.getHtmlResource("searchapi_text_bottom");
/* 2303 */         if (dynHtml != null)
/*      */         {
/* 2305 */           dynHtml.outputHtml(writer, this.m_pageMerger);
/*      */         }
/*      */       }
/* 2308 */       this.m_htmlPage = writer.toString();
/* 2309 */       String encoding = DataSerializeUtils.determineEncoding(this.m_binder, null);
/* 2310 */       this.m_htmlPageAsBytes = StringUtils.getBytes(this.m_htmlPage, encoding);
/*      */ 
/* 2313 */       String binderHeaderStr = this.m_binder.getContentType() + "\r\nContent-Length: " + this.m_htmlPageAsBytes.length;
/*      */ 
/* 2315 */       this.m_binder.setContentType(binderHeaderStr);
/* 2316 */       this.m_headerStr = createHttpResponseHeader();
/* 2317 */       sendResponse();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2321 */       createServiceException(e, "");
/*      */     }
/*      */     finally
/*      */     {
/* 2325 */       this.m_pageMerger.releaseTemporaryWriter(writer);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadFieldInfo()
/*      */     throws ServiceException
/*      */   {
/* 2334 */     Vector fieldNames = SearchLoader.loadFieldInfo(this, this.m_binder);
/* 2335 */     if (fieldNames == null)
/*      */     {
/* 2337 */       return;
/*      */     }
/* 2339 */     this.m_fieldNames = StringUtils.createString(fieldNames, ',', '^');
/* 2340 */     this.m_numFieldNames = fieldNames.size();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected void validateFieldQueryLength(String queryText)
/*      */     throws ServiceException
/*      */   {
/* 2351 */     boolean insideLiteral = false;
/* 2352 */     char literalChar = '-';
/* 2353 */     int first = 0;
/* 2354 */     int next = 0;
/* 2355 */     int length = queryText.length();
/* 2356 */     boolean isFirst = false;
/* 2357 */     int maxlen = 350;
/*      */ 
/* 2359 */     char[] temp = queryText.toCharArray();
/*      */ 
/* 2362 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2364 */       char tch = temp[i];
/*      */ 
/* 2366 */       if (insideLiteral)
/*      */       {
/* 2368 */         if (tch != literalChar)
/*      */           continue;
/* 2370 */         insideLiteral = false;
/*      */       }
/*      */       else
/*      */       {
/* 2375 */         if (tch == '>')
/*      */         {
/* 2377 */           if (!isFirst)
/*      */           {
/* 2379 */             isFirst = true;
/* 2380 */             first = i;
/*      */           }
/*      */           else
/*      */           {
/* 2384 */             next = i;
/* 2385 */             if (next - first > maxlen)
/*      */             {
/* 2387 */               String errMsg = LocaleUtils.encodeMessage("csSearchQueryTooLong", null, queryText);
/*      */ 
/* 2389 */               createServiceException(null, errMsg);
/*      */             }
/* 2391 */             first = next;
/* 2392 */             next = 0;
/*      */           }
/*      */         }
/*      */ 
/* 2396 */         if (tch != '`')
/*      */           continue;
/* 2398 */         insideLiteral = true;
/* 2399 */         literalChar = tch;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2404 */     if (insideLiteral)
/*      */     {
/* 2406 */       String msg = LocaleUtils.encodeMessage("csSearchUnmatchedLiteral", null, "" + literalChar);
/*      */ 
/* 2408 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 2411 */     if (length - first <= maxlen)
/*      */       return;
/* 2413 */     String errMsg = LocaleUtils.encodeMessage("csSearchQueryTooLong", null, queryText);
/*      */ 
/* 2415 */     createServiceException(null, errMsg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void searchCacheReport()
/*      */   {
/* 2422 */     this.m_searchCache = ((SearchCache)SharedObjects.getObject("globalObjects", "SearchCache"));
/* 2423 */     if (this.m_searchCache == null)
/*      */       return;
/* 2425 */     this.m_searchCache.outputCacheReport(this.m_binder);
/*      */   }
/*      */ 
/*      */   public void sortResult(DataResultSet drset, DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 2431 */     if ((drset == null) || (binder == null))
/*      */     {
/* 2433 */       return;
/*      */     }
/* 2435 */     String sortField = binder.getLocal("SortField");
/* 2436 */     String sortOrder = binder.getLocal("SortOrder");
/* 2437 */     if ((sortField == null) || (sortOrder == null) || (!drset.getFieldInfo(sortField, new FieldInfo())))
/*      */     {
/* 2441 */       return;
/*      */     }
/*      */ 
/* 2444 */     boolean orderAsc = sortOrder.equalsIgnoreCase("asc");
/* 2445 */     String sortKey = sortField;
/* 2446 */     int index = ResultSetUtils.getIndexMustExist(drset, sortKey);
/*      */ 
/* 2448 */     IdcComparator cmp = new IdcComparator(index)
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/* 2452 */         Vector v1 = (Vector)obj1;
/* 2453 */         Vector v2 = (Vector)obj2;
/* 2454 */         int result = 0;
/* 2455 */         String s1 = null;
/* 2456 */         String s2 = null;
/* 2457 */         s1 = (String)v1.elementAt(this.val$index);
/* 2458 */         s2 = (String)v2.elementAt(this.val$index);
/* 2459 */         result = s1.toLowerCase().compareTo(s2.toLowerCase());
/* 2460 */         return result;
/*      */       }
/*      */     };
/* 2464 */     int num = drset.getNumRows();
/* 2465 */     Vector[] s = new IdcVector[num];
/*      */ 
/* 2467 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2469 */       s[i] = drset.getRowValues(i);
/*      */     }
/*      */ 
/* 2472 */     Sort.sort(s, 0, num - 1, cmp);
/*      */ 
/* 2474 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 2476 */       drset.setRowValues(s[i], (orderAsc) ? i : num - i - 1);
/*      */     }
/*      */   }
/*      */ 
/*      */   public long getSharedSearchTime()
/*      */   {
/* 2490 */     return this.m_sharedSearchTime;
/*      */   }
/*      */ 
/*      */   public void clearNewDataOfShallowCopy()
/*      */   {
/* 2496 */     if (this.m_queryConfig != null)
/*      */     {
/* 2498 */       this.m_queryConfig.clear();
/*      */     }
/* 2500 */     super.clearNewDataOfShallowCopy();
/*      */   }
/*      */ 
/*      */   private void normalizeTargetDocClasses()
/*      */   {
/* 2505 */     String targetDocClasses = this.m_binder.getAllowMissing("SearchTargetDocClasses");
/* 2506 */     if ((targetDocClasses == null) || (targetDocClasses.length() == 0))
/*      */     {
/* 2508 */       return;
/*      */     }
/*      */ 
/* 2511 */     this.m_targetDocClasses = StringUtils.makeListFromSequence(targetDocClasses, ',', ',', 96);
/* 2512 */     Collections.sort(this.m_targetDocClasses);
/* 2513 */     this.m_binder.putLocal("SearchTargetDocClasses", StringUtils.createString(this.m_targetDocClasses, ',', ','));
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2518 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103825 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchService
 * JD-Core Version:    0.5.4
 */