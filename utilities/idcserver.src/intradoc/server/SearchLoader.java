/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SortUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.indexer.IndexerCollectionHandler;
/*      */ import intradoc.indexer.IndexerCollectionManager;
/*      */ import intradoc.indexer.IndexerConfig;
/*      */ import intradoc.indexer.IndexerWorkObject;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.search.SearchCache;
/*      */ import intradoc.search.SearchUpdateChangeUtils;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.proxy.ProviderUtils;
/*      */ import intradoc.shared.ActiveIndexState;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.IndexerCollectionData;
/*      */ import intradoc.shared.SearchCollections;
/*      */ import intradoc.shared.SearchFieldInfo;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.util.Collections;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SearchLoader
/*      */ {
/*      */   public static final int F_IS_SEARCH_RULES = 1;
/*      */   public static final int F_IS_INDEX_RULES = 2;
/*   51 */   public static final String[][] COLLECTION_ID_MAP = { { "verity.1", "index1/" }, { "verity.2", "index2/" }, { "dre.1", "dre1/" }, { "dre.2", "dre2/" }, { "tamino.1", "tamino1/" }, { "tamino.2", "tamino2/" }, { "databasesearch.1", "databasesearch1/" }, { "databasesearch.2", "databasesearch2/" } };
/*      */ 
/*   62 */   public static int m_autonomyBasePort = 0;
/*   63 */   public static int m_autonomy1BasePort = 0;
/*   64 */   public static int m_autonomy2BasePort = 0;
/*   65 */   public static int m_autonomy1QueryPort = 0;
/*   66 */   public static int m_autonomy1IndexPort = 0;
/*   67 */   public static int m_autonomy1ControlPort = 0;
/*   68 */   public static int m_autonomy2QueryPort = 0;
/*   69 */   public static int m_autonomy2IndexPort = 0;
/*   70 */   public static int m_autonomy2ControlPort = 0;
/*      */   public static int m_softCacheLimit;
/*      */   public static int m_hardCacheLimit;
/*   75 */   public static Workspace m_ws = null;
/*   76 */   public static Hashtable m_collectionMap = new Hashtable();
/*   77 */   public static Hashtable m_searchMap = new Hashtable();
/*   78 */   public static SearchFieldInfo m_searchFieldInfos = null;
/*      */   public static String m_locale;
/*      */   public static String m_encoding;
/*   83 */   public static boolean m_useVdkLegacySearch = false;
/*   84 */   public static boolean m_encodeVdkKeyForSearch = true;
/*   85 */   public static boolean m_useRolesAndAccountsForLocalSearchCacheKey = false;
/*      */ 
/*   87 */   public static HashMap m_optionalFields = null;
/*      */ 
/*   89 */   public static boolean m_inited = false;
/*      */ 
/*      */   public static void init() throws ServiceException
/*      */   {
/*   93 */     initEx(null);
/*      */   }
/*      */ 
/*      */   public static void initEx(Workspace ws) throws ServiceException
/*      */   {
/*   98 */     if (m_inited)
/*      */     {
/*  100 */       return;
/*      */     }
/*  102 */     m_inited = true;
/*  103 */     m_ws = ws;
/*      */ 
/*  105 */     m_softCacheLimit = SharedObjects.getEnvironmentInt("SearchCacheSoftLimit", 25);
/*  106 */     m_hardCacheLimit = SharedObjects.getEnvironmentInt("SearchCacheHardLimit", 200);
/*  107 */     m_useVdkLegacySearch = SharedObjects.getEnvValueAsBoolean("UseVdkLegacySearch", false);
/*      */ 
/*  109 */     m_autonomyBasePort = SharedObjects.getEnvironmentInt("AutonomyBasePort", 0);
/*      */ 
/*  111 */     m_autonomy1BasePort = SharedObjects.getEnvironmentInt("Autonomy1BasePort", 0);
/*  112 */     m_autonomy1QueryPort = SharedObjects.getEnvironmentInt("Autonomy1QueryPort", 0);
/*  113 */     m_autonomy1IndexPort = SharedObjects.getEnvironmentInt("Autonomy1IndexPort", 0);
/*  114 */     m_autonomy1ControlPort = SharedObjects.getEnvironmentInt("Autonomy1ControlPort", 0);
/*      */ 
/*  116 */     m_autonomy2BasePort = SharedObjects.getEnvironmentInt("Autonomy2BasePort", 0);
/*  117 */     m_autonomy2QueryPort = SharedObjects.getEnvironmentInt("Autonomy2QueryPort", 0);
/*  118 */     m_autonomy2IndexPort = SharedObjects.getEnvironmentInt("Autonomy2IndexPort", 0);
/*  119 */     m_autonomy2ControlPort = SharedObjects.getEnvironmentInt("Autonomy2ControlPort", 0);
/*      */ 
/*  121 */     if (m_autonomyBasePort == 0)
/*      */     {
/*  123 */       int port = SharedObjects.getEnvironmentInt("IntradocServerPort", 4444);
/*  124 */       port -= 4444;
/*  125 */       port *= 10;
/*  126 */       port += 14400;
/*  127 */       m_autonomyBasePort = port;
/*      */     }
/*      */ 
/*  130 */     if (m_autonomy1BasePort == 0)
/*  131 */       m_autonomy1BasePort = m_autonomyBasePort;
/*  132 */     if (m_autonomy1QueryPort == 0)
/*  133 */       m_autonomy1QueryPort = m_autonomy1BasePort;
/*  134 */     if (m_autonomy1IndexPort == 0)
/*  135 */       m_autonomy1IndexPort = m_autonomy1BasePort + 1;
/*  136 */     if (m_autonomy1ControlPort == 0) {
/*  137 */       m_autonomy1ControlPort = m_autonomy1BasePort + 2;
/*      */     }
/*  139 */     if (m_autonomy2BasePort == 0)
/*  140 */       m_autonomy2BasePort = m_autonomyBasePort + 5;
/*  141 */     if (m_autonomy2QueryPort == 0)
/*  142 */       m_autonomy2QueryPort = m_autonomy2BasePort;
/*  143 */     if (m_autonomy2IndexPort == 0)
/*  144 */       m_autonomy2IndexPort = m_autonomy2BasePort + 1;
/*  145 */     if (m_autonomy2ControlPort == 0) {
/*  146 */       m_autonomy2ControlPort = m_autonomy2BasePort + 2;
/*      */     }
/*  148 */     m_searchFieldInfos = (SearchFieldInfo)ComponentClassFactory.createClassInstance("SearchFieldInfo", "intradoc.shared.SearchFieldInfo", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "SearchFieldInfo"));
/*      */ 
/*  152 */     m_searchFieldInfos.init(ws);
/*      */   }
/*      */ 
/*      */   public static void processExternalReleasedDocumentsChange(Provider searchProvider, ExecutionContext cxt)
/*      */   {
/*  157 */     processReleasedDocumentsChange(searchProvider, cxt, SearchCache.F_FLUSH_CACHE);
/*      */   }
/*      */ 
/*      */   public static void processApiChangeReleasedDocumentsChange(Provider searchProvider, ExecutionContext cxt)
/*      */   {
/*  162 */     processReleasedDocumentsChange(searchProvider, cxt, SearchCache.F_FLUSH_CACHE | SearchCache.F_FULL_FLUSH);
/*      */   }
/*      */ 
/*      */   public static void processUsersChange(Provider searchProvider, ExecutionContext cxt)
/*      */   {
/*  168 */     processReleasedDocumentsChange(searchProvider, cxt, SearchCache.F_USER_SECURITY_CHANGED);
/*      */   }
/*      */ 
/*      */   public static void processReleasedDocumentsChange(Provider searchProvider, ExecutionContext cxt, int flags)
/*      */   {
/*  174 */     SearchCache cache = (SearchCache)SharedObjects.getObject("globalObjects", "SearchCache");
/*  175 */     if (cache == null)
/*      */       return;
/*      */     try
/*      */     {
/*  179 */       SearchUpdateChangeUtils.processReleasedDocumentsChange(searchProvider, cache, m_ws, cxt, flags);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  184 */       String msg = "!csSearchErrorProcessingChangeToReleasedDocuments";
/*  185 */       Report.error("system", msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cacheSearchCollections()
/*      */     throws ServiceException, DataException
/*      */   {
/*  192 */     String dir = DirectoryLocator.getAppDataDirectory() + "search/";
/*  193 */     FileUtils.reserveDirectory(dir);
/*  194 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */     try
/*      */     {
/*  197 */       FileUtils.checkOrCreateDirectory(dir, 0);
/*  198 */       ActiveIndexState.load();
/*  199 */       String activeIndex = ActiveIndexState.getActiveProperty("ActiveIndex");
/*  200 */       if (activeIndex == null)
/*      */       {
/*  203 */         activeIndex = COLLECTION_ID_MAP[6][0];
/*  204 */         ActiveIndexState.setActiveProperty("ActiveIndex", activeIndex);
/*      */       }
/*      */ 
/*  207 */       SharedObjects.putEnvironmentValue("ActiveIndex", activeIndex);
/*  208 */       DataResultSet activeColls = (DataResultSet)ActiveIndexState.getSearchCollections();
/*      */ 
/*  210 */       boolean isCreateDefault = false;
/*      */ 
/*  212 */       if ((activeColls == null) || (activeColls.isEmpty()))
/*      */       {
/*  214 */         isCreateDefault = true;
/*      */       }
/*      */       else
/*      */       {
/*  218 */         Vector v = activeColls.findRow(0, idcName);
/*      */ 
/*  220 */         if (v == null)
/*      */         {
/*  222 */           isCreateDefault = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  227 */       DataBinder binder = new DataBinder();
/*  228 */       DataResultSet externalColls = new DataResultSet(SearchCollections.COLUMNS);
/*  229 */       String filename = "search_collections.hda";
/*  230 */       File file = FileUtilsCfgBuilder.getCfgFile(dir + filename, "Search", false);
/*  231 */       if (file.exists())
/*      */       {
/*  233 */         ResourceUtils.serializeDataBinder(dir, filename, binder, false, false);
/*      */       }
/*      */       else
/*      */       {
/*  237 */         binder.addResultSet("SearchCollections", externalColls);
/*  238 */         ResourceUtils.serializeDataBinder(dir, filename, binder, true, false);
/*      */       }
/*      */ 
/*  241 */       externalColls = (DataResultSet)binder.getResultSet("SearchCollections");
/*      */ 
/*  243 */       SearchCollections colls = new SearchCollections();
/*  244 */       load(colls, activeColls, externalColls, isCreateDefault);
/*      */ 
/*  246 */       ActiveIndexState.setSearchCollections(colls);
/*  247 */       SharedObjects.putTable(colls.getTableName(), colls);
/*  248 */       ActiveIndexState.save();
/*      */ 
/*  251 */       DataResultSet optColls = new DataResultSet(SearchCollections.COLUMNS);
/*  252 */       optColls.copySimpleFiltered(colls, "sFlag", "optional");
/*      */ 
/*  254 */       if (!optColls.isEmpty())
/*      */       {
/*  256 */         SharedObjects.putTable("OptionalSearchCollections", optColls);
/*      */       }
/*      */ 
/*  261 */       FieldInfo[] info = ResultSetUtils.createInfoList(colls, new String[] { "sCollectionID", "sLocation" }, true);
/*      */ 
/*  263 */       Vector v = colls.findRow(info[0].m_index, idcName);
/*  264 */       if (v != null)
/*      */       {
/*  266 */         String id = (String)v.elementAt(info[1].m_index);
/*  267 */         String location = getCollectionPath(id);
/*  268 */         if (!location.endsWith("intradocbasic/"))
/*      */         {
/*  270 */           location = location + "intradocbasic/";
/*      */         }
/*  272 */         v.setElementAt(location, info[1].m_index);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  277 */       FileUtils.releaseDirectory(dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static DataResultSet getSearchableProviderList(UserData userData, boolean includeImplicit, boolean calculateAllowedAccounts)
/*      */   {
/*  286 */     DataResultSet userCollections = ProviderUtils.createProviderListForUser("outgoing", "IsSearchable", "Search", userData, false, SearchService.EXTRA_SEARCH_FIELDS);
/*      */ 
/*  295 */     String selfSearch = SharedObjects.getEnvironmentValue("EnterpriseSearchLocalServer");
/*  296 */     if (selfSearch == null)
/*      */     {
/*  298 */       selfSearch = "Explicit";
/*      */     }
/*      */ 
/*  301 */     if (!selfSearch.equalsIgnoreCase("Disabled"))
/*      */     {
/*  304 */       String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  305 */       String idcMenuLabel = SharedObjects.getEnvironmentValue("InstanceMenuLabel");
/*  306 */       String idcDescription = SharedObjects.getEnvironmentValue("InstanceDescription");
/*      */ 
/*  308 */       Properties props = new Properties();
/*  309 */       props.put("ProviderName", idcName);
/*  310 */       props.put("ProviderDescription", "!csProviderLocalContentServerLabel");
/*  311 */       props.put("IDC_Name", idcName);
/*  312 */       props.put("InstanceMenuLabel", idcMenuLabel);
/*  313 */       props.put("InstanceDescription", idcDescription);
/*  314 */       props.put("IntradocServerHostName", SharedObjects.getEnvironmentValue("HttpServerAddress"));
/*  315 */       props.put("HttpRelativeWebRoot", DirectoryLocator.getWebRoot(false));
/*  316 */       props.put("IsImplicitlySearched", (selfSearch.equals("Implicit")) ? "1" : "");
/*  317 */       props.put("IsLocalCollection", "true");
/*      */ 
/*  320 */       String accounts = "";
/*      */ 
/*  322 */       if ((calculateAllowedAccounts) && (m_useRolesAndAccountsForLocalSearchCacheKey))
/*      */       {
/*  324 */         Vector tmpAccounts = SecurityUtils.getUserAccountsWithPrivilege(userData, 1, true);
/*      */ 
/*  326 */         String[] allowedAccounts = new String[tmpAccounts.size()];
/*  327 */         tmpAccounts.copyInto(allowedAccounts);
/*  328 */         allowedAccounts = SortUtils.sortCaseInsensitiveStringList(allowedAccounts, (String[][])null);
/*  329 */         tmpAccounts.clear();
/*  330 */         Collections.addAll(tmpAccounts, (Object[])allowedAccounts);
/*      */ 
/*  333 */         Vector finalAccounts = new IdcVector();
/*  334 */         SecurityUtils.addAccountsFiltered(userData, finalAccounts, tmpAccounts, allowedAccounts, true, false, null);
/*      */ 
/*  342 */         accounts = StringUtils.createString(finalAccounts, ',', '^');
/*      */       }
/*  344 */       props.put("UserAccounts", accounts);
/*      */ 
/*  346 */       int fieldCount = userCollections.getNumFields();
/*  347 */       Vector v = new IdcVector();
/*  348 */       FieldInfo fieldInfo = new FieldInfo();
/*  349 */       for (int i = 0; i < fieldCount; ++i)
/*      */       {
/*  351 */         String name = userCollections.getFieldName(i);
/*  352 */         userCollections.getFieldInfo(name, fieldInfo);
/*  353 */         String value = props.getProperty(name);
/*  354 */         if (value == null)
/*      */         {
/*  356 */           value = "";
/*      */         }
/*  358 */         v.addElement(value);
/*      */       }
/*  360 */       userCollections.insertRowAt(v, 0);
/*      */     }
/*      */ 
/*  363 */     if (!includeImplicit)
/*      */     {
/*  365 */       DataResultSet rset = new DataResultSet();
/*  366 */       rset.copySimpleFiltered(userCollections, "IsImplicitlySearched", "1");
/*      */     }
/*      */ 
/*  369 */     return userCollections;
/*      */   }
/*      */ 
/*      */   public static void createSearchCache()
/*      */   {
/*      */     try
/*      */     {
/*  376 */       SearchCache cache = (SearchCache)ComponentClassFactory.createClassInstance("SearchCache", "intradoc.search.SearchCache", "!csSearchUnableToInit");
/*      */ 
/*  378 */       SharedObjects.putObject("globalObjects", "SearchCache", cache);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  382 */       Report.trace(null, e, LocaleResources.localizeMessage("!csSearchCacheInitFailure", null), new Object[0]);
/*      */ 
/*  384 */       Report.warning(null, e, "csSearchCacheInitFailure", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void load(DataResultSet sColls, DataResultSet activeColls, DataResultSet externalColls, boolean isCreateDefault)
/*      */     throws DataException
/*      */   {
/*  396 */     if (activeColls != null)
/*      */     {
/*  398 */       sColls.copySimpleFiltered(activeColls, "sProfile", "local");
/*      */     }
/*      */ 
/*  401 */     if ((externalColls != null) && (!externalColls.isEmpty()))
/*      */     {
/*  403 */       sColls.mergeFields(externalColls);
/*  404 */       sColls.merge("sCollectionID", externalColls, false);
/*      */     }
/*      */ 
/*  407 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */ 
/*  410 */     if (isCreateDefault)
/*      */     {
/*  412 */       String activeIndex = SharedObjects.getEnvironmentValue("ActiveIndex");
/*      */ 
/*  414 */       if ((activeIndex == null) || (activeIndex.length() == 0))
/*      */       {
/*  416 */         String msg = LocaleUtils.encodeMessage("csFieldNotDefined", null, "ActiveIndex");
/*  417 */         throw new DataException(msg);
/*      */       }
/*      */ 
/*  420 */       Vector v = sColls.createEmptyRow();
/*      */ 
/*  422 */       FieldInfo[] info = ResultSetUtils.createInfoList(sColls, SearchCollections.COLUMNS, true);
/*  423 */       v.setElementAt(idcName, info[0].m_index);
/*  424 */       v.setElementAt("!csSearchDefaultSearchCollection", info[1].m_index);
/*  425 */       v.setElementAt(computeSearchLocale(), info[2].m_index);
/*  426 */       v.setElementAt("local", info[3].m_index);
/*  427 */       String location = activeIndex;
/*      */ 
/*  434 */       if (activeIndex.indexOf("/") > 0)
/*      */       {
/*  436 */         String dir = getCollectionPath(activeIndex);
/*  437 */         location = dir + "intradocbasic";
/*      */       }
/*  439 */       v.setElementAt(location, info[4].m_index);
/*  440 */       v.setElementAt("enabled", info[5].m_index);
/*  441 */       v.setElementAt("<$URL$>", info[6].m_index);
/*  442 */       sColls.addRow(v);
/*      */     }
/*      */ 
/*  445 */     FieldInfo[] info = ResultSetUtils.createInfoList(sColls, new String[] { "sCollectionID", "sVerityLocale" }, true);
/*      */ 
/*  447 */     Vector v = sColls.findRow(info[0].m_index, idcName);
/*  448 */     if (v != null)
/*      */     {
/*  450 */       m_locale = sColls.getStringValue(info[1].m_index);
/*      */     }
/*      */ 
/*  453 */     m_encoding = computeVerityEncoding(m_locale);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String computeVerityLocale()
/*      */     throws DataException
/*      */   {
/*  462 */     SystemUtils.reportDeprecatedUsage("SearchLoader.computeVerityLocale()");
/*  463 */     return computeSearchLocale();
/*      */   }
/*      */ 
/*      */   public static String computeSearchLocale() throws DataException
/*      */   {
/*  468 */     String systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  469 */     String verityLocale = SharedObjects.getEnvironmentValue("SearchLocale");
/*  470 */     if (verityLocale == null)
/*      */     {
/*  472 */       verityLocale = SharedObjects.getEnvironmentValue("VerityLocale");
/*      */     }
/*      */ 
/*  475 */     DataResultSet vcConfig = SharedObjects.getTable("SearchLocaleConfig");
/*  476 */     if (vcConfig == null)
/*      */     {
/*  478 */       throw new DataException(LocaleUtils.encodeMessage("csIndexerSearchLocaleConfigMissing", null, DirectoryLocator.getResourcesDirectory()));
/*      */     }
/*      */ 
/*  485 */     String customParams = SharedObjects.getEnvironmentValue("AdditionalIndexBuildParams");
/*  486 */     if ((((verityLocale == null) || (verityLocale.length() == 0))) && (customParams != null) && (customParams.indexOf("-locale") >= 0))
/*      */     {
/*  489 */       Vector v = StringUtils.parseArray(customParams, ' ', '%');
/*  490 */       if (v.size() > 0)
/*      */       {
/*  492 */         for (int j = 0; j < v.size(); ++j)
/*      */         {
/*  494 */           if ((!v.elementAt(j).equals("-locale")) || 
/*  496 */             (j + 1 >= v.size()))
/*      */             continue;
/*  498 */           verityLocale = (String)v.elementAt(j + 1);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  505 */     if ((((verityLocale == null) || (verityLocale.length() == 0))) && (systemLocale != null) && (systemLocale.length() > 0))
/*      */     {
/*  508 */       verityLocale = ResultSetUtils.findValue(vcConfig, "lcLocaleId", systemLocale, "lcSearchLocale");
/*      */     }
/*      */ 
/*  511 */     if (verityLocale == null)
/*      */     {
/*  513 */       throw new DataException("!csUnableToDetermineVerityLocale");
/*      */     }
/*      */ 
/*  516 */     return verityLocale;
/*      */   }
/*      */ 
/*      */   public static String computeVerityEncoding(String verityLocale) throws DataException
/*      */   {
/*  521 */     String engineName = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*  522 */     return computeVerityEncodingEx(verityLocale, engineName);
/*      */   }
/*      */ 
/*      */   public static String computeVerityEncodingEx(String verityLocale, String engineName)
/*      */     throws DataException
/*      */   {
/*  528 */     String verityEncoding = SharedObjects.getEnvironmentValue("VerityEncoding");
/*  529 */     if ((verityLocale != null) && (verityLocale.length() > 0) && ((
/*  531 */       (verityEncoding == null) || (verityEncoding.length() == 0))))
/*      */     {
/*  533 */       if ((engineName != null) && (engineName.length() > 0))
/*      */       {
/*  535 */         DataResultSet searchLocaleMap = SharedObjects.getTable("SearchLocaleMap");
/*  536 */         FieldInfo[] fis = ResultSetUtils.createInfoList(searchLocaleMap, new String[] { "slmSearchLocale", "slmNewSearchLocale" }, true);
/*      */ 
/*  539 */         String newLocale = null;
/*  540 */         while ((engineName.length() > 0) && (newLocale == null))
/*      */         {
/*  542 */           for (searchLocaleMap.first(); searchLocaleMap.isRowPresent(); searchLocaleMap.next())
/*      */           {
/*  544 */             String tmpNewLocale = searchLocaleMap.getStringValue(fis[1].m_index);
/*  545 */             if (!tmpNewLocale.equals(verityLocale))
/*      */               continue;
/*  547 */             String tmpSearchLocale = searchLocaleMap.getStringValue(fis[0].m_index);
/*  548 */             String engineString = "(" + engineName + ")";
/*  549 */             if (!tmpSearchLocale.startsWith(engineString))
/*      */               continue;
/*  551 */             newLocale = tmpSearchLocale.substring(engineString.length());
/*  552 */             break;
/*      */           }
/*      */ 
/*  557 */           int index = engineName.lastIndexOf(46);
/*  558 */           if (index > 0)
/*      */           {
/*  560 */             engineName = engineName.substring(0, index);
/*      */           }
/*      */           else
/*      */           {
/*  564 */             engineName = "";
/*      */           }
/*      */         }
/*      */ 
/*  568 */         if (newLocale != null)
/*      */         {
/*  570 */           verityLocale = newLocale;
/*      */         }
/*      */       }
/*      */ 
/*  574 */       if (verityLocale.equalsIgnoreCase("uni"))
/*      */       {
/*  576 */         verityEncoding = "UTF8";
/*      */       }
/*      */       else
/*      */       {
/*  580 */         String locale = null;
/*  581 */         String isoEncoding = null;
/*  582 */         DataResultSet vcConfig = SharedObjects.getTable("SearchLocaleConfig");
/*  583 */         DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*      */ 
/*  585 */         if (vcConfig == null)
/*      */         {
/*  587 */           throw new DataException(LocaleUtils.encodeMessage("csIndexerSearchLocaleConfigMissing", null, DirectoryLocator.getResourcesDirectory()));
/*      */         }
/*      */ 
/*  594 */         locale = ResultSetUtils.findValue(vcConfig, "lcSearchLocale", verityLocale, "lcLocaleId");
/*      */ 
/*  596 */         if ((locale != null) && (locale.length() > 0))
/*      */         {
/*  598 */           isoEncoding = ResultSetUtils.findValue(localeConfig, "lcLocaleId", locale, "lcIsoEncoding");
/*      */         }
/*      */ 
/*  603 */         if ((isoEncoding != null) && (isoEncoding.length() > 0))
/*      */         {
/*  605 */           if (isoEncoding.equalsIgnoreCase("iso-8859-1"))
/*      */           {
/*  607 */             verityEncoding = (EnvUtils.isFamily("windows")) ? "Cp1252" : "iso-8859-1";
/*      */           }
/*  609 */           else if (isoEncoding.equalsIgnoreCase("Cp1252"))
/*      */           {
/*  613 */             verityEncoding = (EnvUtils.isFamily("windows")) ? "iso-8859-1" : "Cp1252";
/*      */           }
/*      */           else
/*      */           {
/*  617 */             verityEncoding = DataSerializeUtils.getJavaEncoding(isoEncoding);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  624 */     return verityEncoding;
/*      */   }
/*      */ 
/*      */   public static String computeVerityInstallDir(String indexerPath)
/*      */   {
/*  629 */     String instDir = FileUtils.getParent(indexerPath);
/*      */ 
/*  632 */     if (instDir != null)
/*      */     {
/*  634 */       instDir = FileUtils.getParent(instDir);
/*      */     }
/*      */ 
/*  637 */     if (instDir != null)
/*      */     {
/*  639 */       instDir = FileUtils.getParent(instDir);
/*      */     }
/*      */ 
/*  642 */     if (instDir != null)
/*      */     {
/*  644 */       instDir = instDir + "/common";
/*      */     }
/*      */ 
/*  647 */     return instDir;
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData retrieveIndexDesign(String engineName) throws ServiceException
/*      */   {
/*  652 */     DataBinder searchDesignBinder = serializeIndexDesign(engineName);
/*  653 */     return retrieveIndexDesign(engineName, searchDesignBinder, true);
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData retrieveIndexDesignWithCycleId(String engineName, String cycleId) throws ServiceException
/*      */   {
/*  658 */     DataBinder searchDesignBinder = serializeIndexDesign(engineName);
/*  659 */     searchDesignBinder.putLocal("indexerCycleId", cycleId);
/*  660 */     return retrieveIndexDesign(engineName, searchDesignBinder, true);
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData retrieveIndexDesign(String engineName, boolean retrieveIndexerConfigKeys) throws ServiceException
/*      */   {
/*  665 */     DataBinder searchDesignBinder = serializeIndexDesign(engineName);
/*  666 */     return retrieveIndexDesign(engineName, searchDesignBinder, retrieveIndexerConfigKeys);
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData retrieveIndexDesign(String engineName, DataBinder searchDesignBinder, boolean retrieveIndexerConfigKeys)
/*      */     throws ServiceException
/*      */   {
/*  672 */     if (m_searchFieldInfos == null)
/*      */     {
/*  674 */       m_searchFieldInfos = (SearchFieldInfo)ComponentClassFactory.createClassInstance("SearchFieldInfo", "intradoc.shared.SearchFieldInfo", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "SearchFieldInfo"));
/*      */     }
/*      */ 
/*  681 */     IndexerCollectionData collectionDef = null;
/*      */     try
/*      */     {
/*  684 */       if (retrieveIndexerConfigKeys == true)
/*      */       {
/*  686 */         boolean isSupportZoneSearch = false;
/*  687 */         IndexerConfig cfg = SearchIndexerUtils.getIndexerConfig(null, "update");
/*  688 */         isSupportZoneSearch = cfg.getBoolean(engineName, "IsSupportZoneSearch", false);
/*  689 */         searchDesignBinder.putLocal("isSupportZoneSearch", "" + isSupportZoneSearch);
/*      */       }
/*      */ 
/*  692 */       collectionDef = m_searchFieldInfos.loadFieldInfo(engineName, searchDesignBinder);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  696 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  699 */     return collectionDef;
/*      */   }
/*      */ 
/*      */   public static DataBinder serializeIndexDesign(String engineName) throws ServiceException
/*      */   {
/*  704 */     DataBinder binder = new DataBinder();
/*      */ 
/*  706 */     String dataDir = DirectoryLocator.getAppDataDirectory();
/*  707 */     String dir = dataDir + "search/" + engineName.toLowerCase();
/*      */ 
/*  709 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 2, true);
/*  710 */     FileUtils.reserveDirectory(dir);
/*  711 */     ResourceUtils.serializeDataBinder(dir, "design.hda", binder, false, false);
/*  712 */     FileUtils.releaseDirectory(dir);
/*      */ 
/*  714 */     return binder;
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData retrieveSearchDesign(String engineName)
/*      */     throws ServiceException
/*      */   {
/*  720 */     if (m_searchFieldInfos == null)
/*      */     {
/*  722 */       m_searchFieldInfos = (SearchFieldInfo)ComponentClassFactory.createClassInstance("SearchFieldInfo", "intradoc.shared.SearchFieldInfo", LocaleUtils.encodeMessage("csUnableToCreateObject", null, "SearchFieldInfo"));
/*      */     }
/*      */ 
/*  728 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/*  730 */     if (collectionDef == null)
/*      */     {
/*  732 */       collectionDef = retrieveIndexDesign(engineName);
/*  733 */       if (collectionDef != null)
/*      */       {
/*  735 */         setCurrentIndexDesign(engineName, collectionDef);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  743 */     IndexerCollectionData searchDef = null;
/*  744 */     if (collectionDef != null)
/*      */     {
/*  746 */       searchDef = m_searchFieldInfos.loadSearchFieldInfo(engineName, collectionDef, m_ws);
/*      */ 
/*  749 */       setCurrentSearchableFields(engineName, searchDef);
/*      */     }
/*      */ 
/*  752 */     return searchDef;
/*      */   }
/*      */ 
/*      */   public static void refreshCollectionData(IndexerWorkObject worker)
/*      */     throws ServiceException
/*      */   {
/*  759 */     String currentSearchEngine = SearchIndexerUtils.getSearchEngineName(worker);
/*      */ 
/*  761 */     boolean correct = true;
/*  762 */     IndexerCollectionData collectionDesignDef = retrieveIndexDesign(currentSearchEngine);
/*  763 */     IndexerCollectionData collectionDef = null;
/*      */ 
/*  765 */     if ((worker != null) && (worker.m_config.getBoolean("IndexCollectionDesignNeedsSync", false)))
/*      */     {
/*  767 */       collectionDef = getCurrentIndexDesign(currentSearchEngine);
/*  768 */       if (collectionDef == null)
/*      */       {
/*  771 */         correct = worker.m_indexCollectionManager.checkCollectionExistence();
/*  772 */         if (correct)
/*      */         {
/*  775 */           collectionDef = new IndexerCollectionData();
/*  776 */           correct = worker.m_indexCollectionManager.loadCollectionDefinition(collectionDef);
/*      */         }
/*  778 */         if (correct)
/*      */         {
/*  781 */           setCurrentIndexDesign(currentSearchEngine, collectionDef);
/*      */         }
/*      */         else
/*      */         {
/*  785 */           collectionDef = null;
/*      */         }
/*      */       }
/*  788 */       if (correct)
/*      */       {
/*  790 */         correct = compareConfigurations(collectionDesignDef, collectionDef);
/*      */       }
/*      */     }
/*  793 */     SharedObjects.putEnvironmentValue("IndexCollectionSynced", (correct) ? "1" : "0");
/*      */ 
/*  795 */     if (worker != null)
/*      */     {
/*  798 */       IndexerCollectionHandler collectionHandler = worker.m_indexCollectionManager.getHandler();
/*  799 */       collectionHandler.validateConfiguration();
/*      */     }
/*      */ 
/*  802 */     setOrResetIndexerCollectionSyncAlert(worker);
/*      */ 
/*  804 */     if (collectionDef == null)
/*      */     {
/*  807 */       setCurrentIndexDesign(currentSearchEngine, collectionDesignDef);
/*      */     }
/*      */ 
/*  812 */     DataResultSet drset = (DataResultSet)collectionDesignDef.m_binder.getResultSet("SearchDesignInfo");
/*  813 */     if (drset != null)
/*      */     {
/*  815 */       SharedObjects.putTable("SearchDesignInfo", drset);
/*      */     }
/*      */ 
/*  819 */     Hashtable collectionMap = m_collectionMap;
/*  820 */     Hashtable searchMap = m_searchMap;
/*  821 */     for (Enumeration en = collectionMap.keys(); en.hasMoreElements(); )
/*      */     {
/*  823 */       String engineName = (String)en.nextElement();
/*  824 */       searchMap.remove(engineName);
/*      */ 
/*  826 */       if (engineName.equals(currentSearchEngine))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  831 */       collectionMap.remove(engineName);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void setOrResetIndexerCollectionSyncAlert(IndexerWorkObject worker)
/*      */   {
/*  837 */     String alertId = "csIndexerStateMsg_IndexColSyncRequired";
/*  838 */     boolean disableSyncAlert = SharedObjects.getEnvValueAsBoolean("DisableIndexCollectionSyncAlert", false);
/*      */     try
/*      */     {
/*  842 */       boolean existsSyncAlert = AlertUtils.existsAlert(alertId, 1);
/*  843 */       boolean isInSync = SharedObjects.getEnvValueAsBoolean("IndexCollectionSynced", true);
/*      */ 
/*  845 */       if ((disableSyncAlert == true) || (isInSync == true))
/*      */       {
/*  847 */         if (existsSyncAlert == true)
/*      */         {
/*  849 */           AlertUtils.deleteAlertSimple(alertId);
/*      */         }
/*  851 */         return;
/*      */       }
/*      */ 
/*  858 */       boolean existsRebuildWarning = AlertUtils.existsAlert("AutoIndexerRebuildWarning", 1);
/*      */ 
/*  860 */       boolean validConfiguration = true;
/*  861 */       if (worker != null)
/*      */       {
/*  863 */         validConfiguration = worker.m_config.getBoolean("SearchEngineValidConfig", true);
/*      */       }
/*      */ 
/*  867 */       if ((!existsSyncAlert) && (!existsRebuildWarning) && (validConfiguration == true))
/*      */       {
/*  869 */         DataBinder binder = new DataBinder();
/*  870 */         binder.putLocal("alertId", alertId);
/*      */ 
/*  872 */         IdcMessage message = new IdcMessage(alertId, new Object[0]);
/*  873 */         Report.warning(null, null, message);
/*      */ 
/*  875 */         binder.putLocal("alertMsg", "<$lcMessage('" + LocaleUtils.encodeMessage(message) + "')$>");
/*  876 */         binder.putLocal("flags", "1");
/*  877 */         DataResultSet trigRset = new DataResultSet(AlertUtils.TRIGGER_RSET_COLS);
/*      */ 
/*  879 */         Vector trigRow = new Vector();
/*  880 */         trigRow.add(0, "role");
/*  881 */         trigRow.add(1, "admin");
/*  882 */         trigRset.addRow(trigRow);
/*      */ 
/*  884 */         binder.addResultSet("AlertTriggers", trigRset);
/*  885 */         AlertUtils.setAlert(binder);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  891 */       Report.trace(null, "Unable to set or reset the alert csIndexerStateMsg_IndexColSyncRequired", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData getCurrentIndexDesign(String engineName)
/*      */   {
/*  899 */     engineName = SearchIndexerUtils.getProperEngineName(engineName);
/*  900 */     IndexerCollectionData collectionDef = (IndexerCollectionData)m_collectionMap.get(engineName);
/*  901 */     return collectionDef;
/*      */   }
/*      */ 
/*      */   public static void setCurrentIndexDesign(String engineName, IndexerCollectionData collectionDef)
/*      */   {
/*  908 */     engineName = SearchIndexerUtils.getProperEngineName(engineName);
/*  909 */     m_collectionMap.put(engineName, collectionDef);
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData getCurrentSearchableFields(String engineName)
/*      */   {
/*  914 */     return (IndexerCollectionData)m_searchMap.get(engineName);
/*      */   }
/*      */ 
/*      */   public static IndexerCollectionData getCurrentSearchableFieldsAllowInit(String engineName)
/*      */     throws ServiceException
/*      */   {
/*  920 */     if (engineName == null)
/*      */     {
/*  922 */       engineName = SearchIndexerUtils.getSearchEngineName(null);
/*      */     }
/*      */ 
/*  925 */     IndexerCollectionData icd = getCurrentSearchableFields(engineName);
/*  926 */     if (icd == null)
/*      */     {
/*  928 */       retrieveSearchDesign(engineName);
/*  929 */       icd = getCurrentSearchableFields(engineName);
/*      */     }
/*  931 */     return icd;
/*      */   }
/*      */ 
/*      */   public static Vector loadFieldInfo(ExecutionContext ctxt, DataBinder binder) throws ServiceException
/*      */   {
/*  936 */     String engineName = SearchIndexerUtils.getSearchEngineName(ctxt);
/*      */ 
/*  938 */     IndexerCollectionData collectionDef = getCurrentSearchableFields(engineName);
/*  939 */     if (collectionDef == null)
/*      */     {
/*  941 */       collectionDef = retrieveSearchDesign(engineName);
/*  942 */       if (collectionDef == null)
/*      */       {
/*  945 */         Report.error(null, "!csSearchNoCollection", null);
/*  946 */         return null;
/*      */       }
/*      */     }
/*      */ 
/*  950 */     Hashtable fields = collectionDef.m_fieldInfos;
/*  951 */     Hashtable designMap = collectionDef.m_fieldDesignMap;
/*  952 */     Vector fieldNames = new IdcVector();
/*      */ 
/*  954 */     for (Enumeration en = fields.keys(); en.hasMoreElements(); )
/*      */     {
/*  956 */       String key = (String)en.nextElement();
/*  957 */       FieldInfo fi = (FieldInfo)fields.get(key);
/*  958 */       Properties props = (Properties)designMap.get(key);
/*  959 */       if (props != null)
/*      */       {
/*  961 */         boolean isInResults = StringUtils.convertToBool(props.getProperty("isInSearchResult"), true);
/*      */ 
/*  963 */         if (!isInResults) {
/*      */           continue;
/*      */         }
/*      */       }
/*      */ 
/*  968 */       if (fi.m_type == 5)
/*      */       {
/*  970 */         binder.setFieldType(key, "date");
/*  971 */         binder.setFieldType(key.toLowerCase(), "date");
/*      */       }
/*  973 */       else if (fi.m_type == 3)
/*      */       {
/*  975 */         binder.setFieldType(key, "int");
/*  976 */         binder.setFieldType(key.toLowerCase(), "int");
/*      */       }
/*  978 */       else if (fi.m_type == 11)
/*      */       {
/*  980 */         binder.setFieldType(key, "decimal");
/*  981 */         binder.setFieldType(key.toLowerCase(), "decimal");
/*      */       }
/*  983 */       fieldNames.addElement(key);
/*      */     }
/*      */ 
/*  987 */     String extraFields = SharedObjects.getEnvironmentValue("SearchExtraFields");
/*  988 */     Vector v = StringUtils.parseArray(extraFields, ',', '^');
/*  989 */     for (int i = 0; i < v.size(); ++i)
/*      */     {
/*  991 */       String temp = (String)v.elementAt(i);
/*  992 */       if ((temp == null) || (temp.length() <= 0)) {
/*      */         continue;
/*      */       }
/*  995 */       fieldNames.addElement(v.elementAt(i));
/*      */     }
/*      */ 
/* 1000 */     return fieldNames;
/*      */   }
/*      */ 
/*      */   public static void setCurrentSearchableFields(String engineName, IndexerCollectionData collectionDef)
/*      */   {
/* 1005 */     m_searchMap.put(engineName, collectionDef);
/*      */   }
/*      */ 
/*      */   public static boolean compareConfigurations(IndexerCollectionData currentDesignDef, IndexerCollectionData collectionDef)
/*      */   {
/* 1011 */     HashMap opFields = getOptionalFields();
/* 1012 */     return SearchFieldInfo.compareConfigurationsEx(currentDesignDef, collectionDef, opFields);
/*      */   }
/*      */ 
/*      */   public static boolean isSecurityField(String name, ExecutionContext cxt)
/*      */   {
/* 1017 */     String engineName = SearchIndexerUtils.getSearchEngineName(cxt);
/* 1018 */     return isSecurityFieldInEngine(name, engineName);
/*      */   }
/*      */ 
/*      */   public static boolean isSecurityFieldInEngine(String name, String engineName)
/*      */   {
/* 1023 */     boolean result = false;
/* 1024 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/* 1026 */     if (collectionDef != null)
/*      */     {
/* 1028 */       Object info = collectionDef.m_securityInfos.get(name);
/* 1029 */       if (info != null)
/*      */       {
/* 1031 */         result = true;
/*      */       }
/*      */     }
/* 1034 */     return result;
/*      */   }
/*      */ 
/*      */   public static boolean isZoneField(String name, ExecutionContext cxt)
/*      */   {
/* 1040 */     boolean result = false;
/* 1041 */     String engineName = SearchIndexerUtils.getSearchEngineName(cxt);
/* 1042 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/* 1044 */     if (collectionDef != null)
/*      */     {
/* 1046 */       Properties props = (Properties)collectionDef.m_fieldDesignMap.get(name);
/* 1047 */       if (props != null)
/*      */       {
/* 1049 */         result = StringUtils.convertToBool(props.getProperty("isZone"), false);
/*      */       }
/*      */     }
/* 1052 */     return result;
/*      */   }
/*      */ 
/*      */   public static boolean isZoneSearchField(String name, ExecutionContext cxt)
/*      */   {
/* 1057 */     boolean result = false;
/* 1058 */     String engineName = SearchIndexerUtils.getSearchEngineName(cxt);
/* 1059 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/* 1061 */     if (collectionDef != null)
/*      */     {
/* 1063 */       Properties props = (Properties)collectionDef.m_fieldDesignMap.get(name);
/* 1064 */       if (props != null)
/*      */       {
/* 1066 */         result = StringUtils.convertToBool(props.getProperty("isZoneSearch"), false);
/*      */       }
/*      */     }
/* 1069 */     return result;
/*      */   }
/*      */ 
/*      */   public static boolean isZoneQuickSearch(ExecutionContext cxt)
/*      */   {
/* 1074 */     boolean result = false;
/* 1075 */     String engineName = SearchIndexerUtils.getSearchEngineName(cxt);
/* 1076 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/* 1078 */     if (collectionDef != null)
/*      */     {
/* 1080 */       DataBinder binder = collectionDef.m_binder;
/* 1081 */       result = StringUtils.convertToBool(binder.getLocal("isZoneQuickSearch"), false);
/*      */     }
/* 1083 */     return result;
/*      */   }
/*      */ 
/*      */   public static boolean isCaseInsensitiveField(String name, String engineName)
/*      */   {
/* 1088 */     boolean result = false;
/* 1089 */     IndexerCollectionData collectionDef = getCurrentIndexDesign(engineName);
/*      */ 
/* 1091 */     if (collectionDef != null)
/*      */     {
/* 1093 */       Properties props = (Properties)collectionDef.m_fieldDesignMap.get(name);
/* 1094 */       if (props != null)
/*      */       {
/* 1096 */         result = StringUtils.convertToBool(props.getProperty("isCaseInsensitive"), false);
/*      */       }
/*      */     }
/* 1099 */     return result;
/*      */   }
/*      */ 
/*      */   public static HashMap getOptionalFields()
/*      */   {
/* 1104 */     if (m_optionalFields == null)
/*      */     {
/* 1106 */       initOptionalFields();
/*      */     }
/* 1108 */     return m_optionalFields;
/*      */   }
/*      */ 
/*      */   protected static synchronized void initOptionalFields()
/*      */   {
/* 1113 */     if (m_optionalFields != null)
/*      */     {
/* 1115 */       return;
/*      */     }
/* 1117 */     m_optionalFields = new HashMap();
/* 1118 */     String optionalFieldStr = SharedObjects.getEnvironmentValue("OptionalSearchFields");
/* 1119 */     Vector fieldList = StringUtils.parseArray(optionalFieldStr, ',', ',');
/* 1120 */     int size = fieldList.size();
/* 1121 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1123 */       String field = (String)fieldList.elementAt(i);
/* 1124 */       m_optionalFields.put(field.toLowerCase(), "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean isOptionalField(String fieldName)
/*      */   {
/* 1130 */     if (fieldName == null)
/*      */     {
/* 1132 */       return false;
/*      */     }
/* 1134 */     if (m_optionalFields == null)
/*      */     {
/* 1136 */       initOptionalFields();
/*      */     }
/* 1138 */     return m_optionalFields.get(fieldName.toLowerCase()) != null;
/*      */   }
/*      */ 
/*      */   public static String getCollectionPath(String id)
/*      */   {
/* 1143 */     String searchDir = DirectoryLocator.getSearchDirectory();
/* 1144 */     File path = new File(id);
/* 1145 */     for (int i = 0; i < COLLECTION_ID_MAP.length; ++i)
/*      */     {
/* 1147 */       if (!id.equals(COLLECTION_ID_MAP[i][0]))
/*      */         continue;
/* 1149 */       String dir = COLLECTION_ID_MAP[i][1];
/* 1150 */       path = new File(dir);
/* 1151 */       break;
/*      */     }
/*      */ 
/* 1154 */     if (!path.isAbsolute())
/*      */     {
/* 1156 */       path = new File(searchDir + path.getPath());
/*      */     }
/*      */ 
/* 1159 */     String pathName = path.getPath();
/* 1160 */     return FileUtils.directorySlashes(pathName);
/*      */   }
/*      */ 
/*      */   public static String prepareSearchIndexKey(String str, String primaryKeyEncodingType, CommonSearchConfig searchConfig, int flags)
/*      */   {
/* 1174 */     if ((primaryKeyEncodingType != null) && (primaryKeyEncodingType.equals("hex")))
/*      */     {
/* 1176 */       return str.toLowerCase();
/*      */     }
/* 1178 */     return str.toUpperCase();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void encodeToHexString(String str, IdcStringBuilder buff)
/*      */     throws UnsupportedEncodingException
/*      */   {
/* 1185 */     StringUtils.appendAsHex(buff, str);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void encodeToHexString(String str, StringBuffer buff)
/*      */     throws UnsupportedEncodingException
/*      */   {
/* 1192 */     SystemUtils.reportDeprecatedUsage("SearchLoader.encodeToHexString()");
/* 1193 */     byte[] b = str.getBytes("UTF8");
/* 1194 */     int len = b.length;
/* 1195 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1196 */     for (int j = 0; j < len; ++j)
/*      */     {
/* 1198 */       NumberUtils.appendHexByte(builder, b[j]);
/*      */     }
/* 1200 */     buff.append(builder.toString());
/*      */   }
/*      */ 
/*      */   public static String getDreHost(String collection)
/*      */   {
/* 1205 */     String host = SharedObjects.getEnvironmentValue("Autonomy1Host");
/* 1206 */     if (collection.equals("dre.2"))
/*      */     {
/* 1208 */       host = SharedObjects.getEnvironmentValue("Autonomy2Host");
/*      */     }
/* 1210 */     if (host == null)
/*      */     {
/* 1212 */       host = SharedObjects.getEnvironmentValue("AutonomyHost");
/*      */     }
/* 1214 */     if (host == null)
/*      */     {
/* 1216 */       host = "localhost";
/*      */     }
/*      */ 
/* 1219 */     return host;
/*      */   }
/*      */ 
/*      */   public static int getDreQueryPort(String collection)
/*      */   {
/* 1224 */     int port = m_autonomy1QueryPort;
/* 1225 */     if (collection.equals("dre.2"))
/*      */     {
/* 1227 */       port = m_autonomy2QueryPort;
/*      */     }
/*      */ 
/* 1230 */     return port;
/*      */   }
/*      */ 
/*      */   public static int getDreIndexPort(String collection)
/*      */   {
/* 1235 */     int port = m_autonomy1IndexPort;
/* 1236 */     if (collection.equals("dre.2"))
/*      */     {
/* 1238 */       port = m_autonomy2IndexPort;
/*      */     }
/*      */ 
/* 1241 */     return port;
/*      */   }
/*      */ 
/*      */   public static void writeDesign(String engineName, DataResultSet designSet, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1252 */     String dir = DirectoryLocator.getAppDataDirectory() + "search/" + engineName.toLowerCase();
/* 1253 */     FileUtils.reserveDirectory(dir);
/*      */     try
/*      */     {
/* 1256 */       Hashtable fieldMap = SearchFieldInfo.loadCollectionDesign(engineName, null, null);
/*      */ 
/* 1264 */       DataResultSet drset = new DataResultSet(SearchFieldInfo.DESIGN_COLUMNS);
/*      */ 
/* 1266 */       binder.addResultSet("SearchDesignOptions", designSet);
/* 1267 */       int nameIndex = ResultSetUtils.getIndexMustExist(designSet, "fieldName");
/* 1268 */       int optIndex = ResultSetUtils.getIndexMustExist(designSet, "advOptions");
/*      */ 
/* 1270 */       for (designSet.first(); designSet.isRowPresent(); designSet.next())
/*      */       {
/* 1272 */         String name = designSet.getStringValue(nameIndex);
/* 1273 */         Object obj = fieldMap.get(name);
/* 1274 */         if (obj == null)
/*      */           continue;
/* 1276 */         String val = designSet.getStringValue(optIndex);
/* 1277 */         if (val.length() <= 0)
/*      */           continue;
/* 1279 */         Vector row = drset.createRow(binder);
/* 1280 */         drset.addRow(row);
/*      */       }
/*      */ 
/* 1284 */       binder.addResultSet("SearchDesignOptions", drset);
/* 1285 */       ResourceUtils.serializeDataBinder(dir, "design.hda", binder, true, false);
/*      */     }
/*      */     finally
/*      */     {
/* 1289 */       FileUtils.releaseDirectory(dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1295 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99181 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchLoader
 * JD-Core Version:    0.5.4
 */