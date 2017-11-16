/*      */ package intradoc.shared;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcCharSequence;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.QueryUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.filterdata.HtmlFilterUtils;
/*      */ import intradoc.server.SearchLoader;
/*      */ import intradoc.server.SubjectCallback;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.StringReader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.LinkedList;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class CommonSearchConfig
/*      */ {
/*      */   public static final int F_IS_LOCAL_SEARCH = 1;
/*      */   protected static final String DEFAULT_CONFIGLABEL = "COMMON";
/*      */   protected String m_currentCfgLabel;
/*      */   protected CommonSearchEngineConfig m_currentSearchEngineConfig;
/*      */   protected Map<String, DataResultSet> m_configs;
/*      */   protected Map<String, Properties> m_whereClauseDefs;
/*      */   protected Map<String, Properties> m_parsedQueries;
/*      */   protected Map<String, CommonSearchConfigCompanion> m_configCompanions;
/*      */   protected Map<String, Map> m_queryConfigs;
/*      */   protected Map<String, Properties> m_engineRules;
/*      */   protected Map<String, CommonSearchEngineConfig> m_engineConfig;
/*      */   protected Map<String, Properties> m_searchFieldAliasName;
/*      */   protected Map<String, QueryElementField> m_queryFieldConfigs;
/*      */   protected Map<String, String> m_opNameMap;
/*      */   protected DynamicHtmlMerger m_dhtml;
/*      */   protected Properties m_operatorTypes;
/*      */   protected Map<String, Map> m_reservedRules;
/*      */   protected Map<String, Map> m_operatorCategories;
/*   68 */   protected static String[] m_reservedKeysList = { "SearchReservedWords", "SearchReservedChars", "SearchReservedProcessOperators", "SearchReservedExpErrorCodes", "SearchReservedProcessDecorators" };
/*      */ 
/*   70 */   protected static String[] m_operatorCategoryList = { "SearchPrefixOperators", "SearchSuffixOperators" };
/*      */ 
/*   72 */   protected static int m_maxAppResults = 10000;
/*      */ 
/*   74 */   protected static boolean CLIENT_TABLE_INITED = false;
/*      */ 
/*   76 */   static String[][] DATA_TYPE_LABEL_MAP = { { "text", "SearchTextField" }, { "date", "SearchDateField" }, { "bool", "SearchBooleanField" }, { "number", "SearchIntegerField" }, { "zone", "SearchZoneField" } };
/*      */   final String[][] OTHER_CLIENT_VALUES;
/*      */ 
/*      */   public CommonSearchConfig()
/*      */   {
/*   42 */     this.m_configs = new Hashtable();
/*      */ 
/*   44 */     this.m_whereClauseDefs = new Hashtable();
/*   45 */     this.m_parsedQueries = new Hashtable();
/*   46 */     this.m_configCompanions = new Hashtable();
/*      */ 
/*   48 */     this.m_queryConfigs = new Hashtable();
/*   49 */     this.m_engineRules = new Hashtable();
/*      */ 
/*   51 */     this.m_engineConfig = new Hashtable();
/*      */ 
/*   53 */     this.m_searchFieldAliasName = new Hashtable();
/*   54 */     this.m_queryFieldConfigs = new Hashtable();
/*   55 */     this.m_opNameMap = new ConcurrentHashMap();
/*      */ 
/*   60 */     this.m_dhtml = null;
/*      */ 
/*   63 */     this.m_operatorTypes = new Properties();
/*      */ 
/*   66 */     this.m_reservedRules = new Hashtable();
/*   67 */     this.m_operatorCategories = new Hashtable();
/*      */ 
/*   80 */     this.OTHER_CLIENT_VALUES = new String[][] { { "DefaultSearchOperator", "DefaultSearchOperator" }, { "DefaultNBFieldSearchOperator", "DefaultNBFieldSearchOperator" } };
/*      */   }
/*      */ 
/*      */   public void init(String label, ExecutionContext ctxt) throws DataException, ServiceException
/*      */   {
/*   85 */     String[] tableNames = { "SearchQueryDefinition", "SearchOperatorMap", "SearchOperatorStrings", "SearchFieldInfo", "SearchRepository", "SearchEngineClasses", "SearchEngineRules", "SearchFieldAliasName" };
/*      */ 
/*   88 */     for (int i = 0; i < tableNames.length; ++i)
/*      */     {
/*   90 */       DataResultSet drset = SharedObjects.getTable(tableNames[i]);
/*   91 */       this.m_configs.put(tableNames[i], drset);
/*      */     }
/*      */ 
/*   95 */     this.m_dhtml = ((DynamicHtmlMerger)ctxt.getCachedObject("PageMerger"));
/*      */ 
/*   98 */     Map[] allProps = { this.m_whereClauseDefs, this.m_parsedQueries, this.m_queryConfigs, this.m_engineRules, this.m_searchFieldAliasName };
/*      */ 
/*  100 */     Properties[] lastProps = new Properties[allProps.length];
/*  101 */     Properties[] defaultProps = new Properties[allProps.length];
/*  102 */     for (int i = 0; i < allProps.length; ++i)
/*      */     {
/*  104 */       defaultProps[i] = new Properties();
/*  105 */       allProps[i].put("COMMON", defaultProps[i]);
/*      */     }
/*      */ 
/*  109 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  110 */     FieldInfo seId = new FieldInfo();
/*  111 */     searchEngines.getFieldInfo("seId", seId);
/*  112 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/*  114 */       for (int i = 0; i < defaultProps.length; ++i)
/*      */       {
/*  116 */         lastProps[i] = defaultProps[i];
/*      */       }
/*      */ 
/*  119 */       String id = searchEngines.getStringValue(seId.m_index);
/*      */ 
/*  121 */       boolean finished = false;
/*  122 */       int index = id.indexOf(46);
/*  123 */       while (!finished)
/*      */       {
/*      */         String name;
/*      */         String name;
/*  126 */         if (index >= 0)
/*      */         {
/*  128 */           name = id.substring(0, index);
/*      */         }
/*      */         else
/*      */         {
/*  132 */           name = id;
/*  133 */           finished = true;
/*      */         }
/*      */ 
/*  136 */         for (int i = 0; i < allProps.length; ++i)
/*      */         {
/*  138 */           Properties map = (Properties)allProps[i].get(name);
/*  139 */           if (map == null)
/*      */           {
/*  141 */             map = new Properties(lastProps[i]);
/*  142 */             allProps[i].put(name, map);
/*      */           }
/*  144 */           lastProps[i] = map;
/*      */         }
/*      */ 
/*  147 */         index = id.indexOf(46, index + 1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  154 */     for (int i = 0; i < allProps.length; ++i)
/*      */     {
/*  156 */       if (!SharedObjects.getEnvValueAsBoolean("DisableDatabaseFullTextConfigurationSync", false))
/*      */       {
/*  158 */         allProps[i].put("DATABASEFULLTEXT", allProps[i].get("DATABASE.FULLTEXT"));
/*      */       }
/*  160 */       if (SharedObjects.getEnvValueAsBoolean("DisableDatabaseFullTextConfigurationSync", false))
/*      */         continue;
/*  162 */       allProps[i].put("DATABASE", allProps[i].get("DATABASE.METADATA"));
/*      */     }
/*      */ 
/*  166 */     preParseHtml((DataResultSet)this.m_configs.get("SearchQueryDefinition"));
/*  167 */     prepareSearchEngineRules();
/*  168 */     prepareSearchEngineConfig();
/*      */ 
/*  170 */     this.m_engineConfig.put("DATABASE", this.m_engineConfig.get("DATABASE.METADATA"));
/*  171 */     this.m_engineConfig.put("DATABASEFULLTEXT", this.m_engineConfig.get("DATABASE.FULLTEXT"));
/*      */ 
/*  173 */     preParseQuery("SearchOperatorMap");
/*  174 */     prepareOperatorTypes();
/*  175 */     prepareSearchFieldAliasName();
/*      */ 
/*  178 */     setCurrentConfig(label);
/*      */ 
/*  182 */     String curEngine = getCurrentEngineName();
/*  183 */     String maxAppResults = getEngineValue(curEngine, "MaxAppSearchResults");
/*  184 */     if ((maxAppResults != null) && (!maxAppResults.equals("")))
/*      */     {
/*  186 */       m_maxAppResults = NumberUtils.parseInteger(maxAppResults, m_maxAppResults);
/*      */     }
/*      */ 
/*  191 */     IndexerCollectionData collectionDesignDef = SearchLoader.retrieveIndexDesign(this.m_currentCfgLabel, false);
/*      */ 
/*  193 */     String supportSearchDesignOpString = getEngineValue(curEngine, "SupportSearchDesignSortFieldsUpdate");
/*  194 */     boolean supportSearchDesignSortFieldsUpdate = StringUtils.convertToBool(supportSearchDesignOpString, false);
/*  195 */     boolean isRebuildNeeded = false;
/*  196 */     boolean isSearchDesignUpdated = false;
/*  197 */     boolean isSearchFieldUpdateSuccessful = false;
/*      */ 
/*  201 */     DataBinder searchDesignBinder = collectionDesignDef.m_binder;
/*  202 */     if (searchDesignBinder != null)
/*      */     {
/*  204 */       String isSearchDesignUpdatedString = searchDesignBinder.getAllowMissing("isSearchDesignUpdated");
/*  205 */       isSearchDesignUpdated = StringUtils.convertToBool(isSearchDesignUpdatedString, false);
/*      */     }
/*      */ 
/*  208 */     if ((supportSearchDesignSortFieldsUpdate == true) && 
/*  210 */       (searchDesignBinder != null) && 
/*  214 */       (isSearchDesignUpdated == true))
/*      */     {
/*  216 */       String isRebuildNeededString = searchDesignBinder.getAllowMissing("isRebuildNeeded");
/*  217 */       isRebuildNeeded = StringUtils.convertToBool(isRebuildNeededString, false);
/*      */ 
/*  219 */       if (!isRebuildNeeded)
/*      */       {
/*  221 */         isSearchFieldUpdateSuccessful = updateSearchFieldInfoUsingSearchDesign(supportSearchDesignSortFieldsUpdate, collectionDesignDef, null);
/*      */       }
/*      */       else
/*      */       {
/*  227 */         String validSortFields = searchDesignBinder.getAllowMissing("validSortFields");
/*  228 */         String universalSortFields = getEngineValue(curEngine, "UniversalSortFields");
/*      */ 
/*  230 */         if ((universalSortFields != null) && (universalSortFields.length() > 0))
/*      */         {
/*  232 */           if (universalSortFields.startsWith("|"))
/*      */           {
/*  234 */             universalSortFields = universalSortFields.substring(1);
/*      */           }
/*  236 */           universalSortFields = universalSortFields.replace("|", ",");
/*      */         }
/*  238 */         if ((validSortFields != null) && (validSortFields.length() > 0))
/*      */         {
/*  240 */           validSortFields = validSortFields + universalSortFields;
/*      */ 
/*  242 */           isSearchFieldUpdateSuccessful = updateSearchFieldInfoUsingSearchDesign(supportSearchDesignSortFieldsUpdate, collectionDesignDef, validSortFields);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  253 */     updateDrillDownFields(collectionDesignDef, curEngine, "DrillDownFields", true);
/*      */ 
/*  257 */     updateDrillDownFields(collectionDesignDef, curEngine, "SearchDesignDrillDownFields", false);
/*      */ 
/*  260 */     setIndexDesignRebuildFlag(collectionDesignDef);
/*      */ 
/*  262 */     prepareClientTables(ctxt);
/*      */ 
/*  266 */     prepareReservedRules();
/*      */ 
/*  268 */     prepareOperatorCategories();
/*      */ 
/*  272 */     if ((supportSearchDesignSortFieldsUpdate == true) && (isSearchFieldUpdateSuccessful == true))
/*      */     {
/*  274 */       Map containers = (Map)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*      */ 
/*  276 */       updateUniversalSortFields(containers);
/*      */     }
/*      */ 
/*  279 */     CommonSearchSubjectCallback scb = new CommonSearchSubjectCallback();
/*  280 */     scb.init(this);
/*      */ 
/*  282 */     SubjectManager.registerCallback("searchapi", scb);
/*  283 */     SubjectManager.registerCallback("metadata", scb);
/*      */   }
/*      */ 
/*      */   public void updateDrillDownFields(IndexerCollectionData collectionDesignDef, String curEngine, String sharedObjectsKey, boolean isFetchRestrictCheckRequired)
/*      */   {
/*  297 */     String supportDrillDownFieldsString = getEngineValue(curEngine, "SupportDrillDownFields");
/*  298 */     boolean supportDrillDownFields = StringUtils.convertToBool(supportDrillDownFieldsString, false);
/*  299 */     String drillDownFields = "";
/*  300 */     String fetchRestrictKey = null;
/*      */ 
/*  302 */     if (isFetchRestrictCheckRequired == true)
/*      */     {
/*  304 */       fetchRestrictKey = "isMetaRebuildNeeded";
/*      */     }
/*      */ 
/*  307 */     if (supportDrillDownFields != true)
/*      */       return;
/*  309 */     drillDownFields = retrieveEnabledFieldsForProp(collectionDesignDef, "DrillDownFields", "IsInSearchResultFilterCategory", fetchRestrictKey, "validDrillDownFields");
/*  310 */     if (drillDownFields == null)
/*      */       return;
/*  312 */     SharedObjects.putEnvironmentValue(sharedObjectsKey, drillDownFields);
/*      */   }
/*      */ 
/*      */   public void setIndexDesignRebuildFlag(IndexerCollectionData collectionDesignDef)
/*      */   {
/*  326 */     if (collectionDesignDef == null)
/*      */     {
/*  328 */       return;
/*      */     }
/*      */ 
/*  331 */     DataBinder searchDesignBinder = collectionDesignDef.m_binder;
/*  332 */     String isRebuildNeededString = searchDesignBinder.getAllowMissing("isRebuildNeeded");
/*  333 */     boolean isRebuildNeeded = StringUtils.convertToBool(isRebuildNeededString, false);
/*      */ 
/*  335 */     String isMetaRebuildNeededString = searchDesignBinder.getAllowMissing("isMetaRebuildNeeded");
/*  336 */     boolean isMetaRebuildNeeded = StringUtils.convertToBool(isMetaRebuildNeededString, false);
/*      */ 
/*  338 */     if ((isRebuildNeeded != true) && (isMetaRebuildNeeded != true))
/*      */       return;
/*  340 */     SharedObjects.putEnvironmentValue("IndexDesignRebuildRequired", "1");
/*      */   }
/*      */ 
/*      */   public void setIndexDesignRebuildFlag(boolean indexDesignRebuildFlag)
/*      */   {
/*  346 */     SharedObjects.putEnvironmentValue("IndexDesignRebuildRequired", Boolean.toString(indexDesignRebuildFlag));
/*      */   }
/*      */ 
/*      */   protected void preParseHtml(DataResultSet drset) throws DataException
/*      */   {
/*  351 */     String[] preParseColumns = { "QueryAssembly", "QuerySelection", "QueryCollection", "WhereClause", "SortSpec" };
/*      */ 
/*  353 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  355 */       String key = drset.getStringValue(0);
/*  356 */       Properties config = (Properties)this.m_queryConfigs.get(key);
/*      */ 
/*  358 */       int len = drset.getNumFields();
/*  359 */       for (int i = 1; i < len; ++i)
/*      */       {
/*  361 */         String col = drset.getFieldName(i);
/*  362 */         String value = drset.getStringValue(i);
/*  363 */         if (value != null)
/*      */         {
/*  365 */           config.put(col, value);
/*      */         }
/*  367 */         if (StringUtils.findStringIndex(preParseColumns, col) == -1)
/*      */           continue;
/*  369 */         DynamicHtml dh = parseHtml(value);
/*  370 */         if (dh == null)
/*      */           continue;
/*  372 */         config.put(col + "_PAGE", dh);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  378 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  379 */     FieldInfo fi = new FieldInfo();
/*  380 */     searchEngines.getFieldInfo("seId", fi);
/*  381 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/*  383 */       String id = searchEngines.getStringValue(fi.m_index);
/*  384 */       inheritPropertiesObjects(id, this.m_queryConfigs);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DynamicHtml parseHtml(String src) throws DataException
/*      */   {
/*  390 */     if ((src == null) || (src.length() == 0))
/*      */     {
/*  392 */       return null;
/*      */     }
/*  394 */     DynamicHtml dh = new DynamicHtml();
/*  395 */     StringReader sr = new StringReader(src);
/*      */     try
/*      */     {
/*  398 */       dh.loadHtml(sr, null, false);
/*  399 */       dh.m_resourceString = src;
/*  400 */       return dh;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  404 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void preParseQuery(String label) throws ServiceException
/*      */   {
/*  410 */     DataResultSet queryWhereClause = (DataResultSet)this.m_configs.get(label);
/*  411 */     for (queryWhereClause.first(); queryWhereClause.isRowPresent(); queryWhereClause.next())
/*      */     {
/*  413 */       String value = queryWhereClause.getStringValue(1);
/*      */ 
/*  415 */       String name = queryWhereClause.getStringValue(0);
/*  416 */       String type = queryWhereClause.getStringValue(2);
/*  417 */       Properties curConfig = getCurrentConfigFromKey(name);
/*  418 */       Properties curParsedQueries = getCurrentParsedQueriesFromKey(name);
/*      */ 
/*  420 */       String engineName = getLabel(name, "(", ")");
/*  421 */       int quoteIndex = name.indexOf(41);
/*  422 */       if (quoteIndex != -1)
/*      */       {
/*  424 */         name = name.substring(quoteIndex + 1);
/*      */       }
/*  426 */       name = name.toLowerCase();
/*  427 */       curConfig.setProperty(name, value);
/*      */ 
/*  429 */       SearchOperatorParsedElements queryElements = parseQuery(name, value, type, engineName);
/*  430 */       curParsedQueries.put(name, queryElements);
/*      */     }
/*      */ 
/*  433 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  434 */     FieldInfo fi = new FieldInfo();
/*  435 */     searchEngines.getFieldInfo("seId", fi);
/*  436 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/*  438 */       String id = searchEngines.getStringValue(fi.m_index);
/*  439 */       inheritPropertiesObjects(id, this.m_parsedQueries);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void inheritPropertiesObjects(String key, Map props)
/*      */   {
/*  447 */     Properties mainProps = (Properties)props.get(key);
/*      */ 
/*  449 */     int index = key.lastIndexOf(".");
/*  450 */     while (index >= 0)
/*      */     {
/*  452 */       if (key.equalsIgnoreCase("DATABASE.FULLTEXT"))
/*      */       {
/*  454 */         key = "DATABASEFULLTEXT";
/*      */       }
/*      */       else
/*      */       {
/*  458 */         key = key.substring(0, index);
/*      */       }
/*  460 */       Properties p = (Properties)props.get(key);
/*      */ 
/*  462 */       for (Enumeration en = p.keys(); en.hasMoreElements(); )
/*      */       {
/*  464 */         String elemKey = (String)en.nextElement();
/*  465 */         if (!mainProps.containsKey(elemKey))
/*      */         {
/*  467 */           mainProps.put(elemKey, p.get(elemKey));
/*      */         }
/*      */       }
/*      */ 
/*  471 */       index = key.lastIndexOf(".");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected SearchOperatorParsedElements parseQuery(String name, String query, String type, String engineName)
/*      */   {
/*  477 */     char[] queryArray = query.toCharArray();
/*  478 */     int[] index = new int[1024];
/*  479 */     int[] valueActionIndex = new int[1024];
/*  480 */     List elements = new ArrayList();
/*  481 */     int arrayLen = 1;
/*  482 */     index[0] = 0;
/*  483 */     valueActionIndex[0] = -1;
/*  484 */     int len = queryArray.length;
/*  485 */     SearchOperatorParsedElement element = new SearchOperatorParsedElement();
/*      */ 
/*  487 */     if (Report.m_verbose)
/*      */     {
/*  489 */       Report.trace("searchqueryparse", "Parse Query to SearchOperatorParsedElement List for name : " + name + " query: " + query + " type: " + type + " with engine " + engineName, null);
/*      */     }
/*      */ 
/*  493 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  495 */       if ((queryArray[i] != '%') || (i + 1 >= len) || (
/*  497 */         (queryArray[(i + 1)] != 'F') && (queryArray[(i + 1)] != 'V')))
/*      */         continue;
/*  499 */       if (index[(arrayLen - 1)] != i)
/*      */       {
/*  502 */         elements.add(element);
/*  503 */         element.m_parsedElement = new String(queryArray, index[(arrayLen - 1)], i - index[(arrayLen - 1)]);
/*      */ 
/*  505 */         element.m_parsedElementType = 0;
/*  506 */         valueActionIndex[arrayLen] = -1;
/*  507 */         index[arrayLen] = i;
/*  508 */         ++arrayLen;
/*  509 */         element = new SearchOperatorParsedElement();
/*      */       }
/*  511 */       if (i + 1 >= len)
/*      */         continue;
/*  513 */       int end = i + 2;
/*      */ 
/*  515 */       boolean isValue = queryArray[(i + 1)] == 'V';
/*  516 */       List enumSet = null;
/*  517 */       if ((i + 2 < len) && (queryArray[(i + 2)] == '.'))
/*      */       {
/*  519 */         for (int j = i + 2; j < queryArray.length; ++j)
/*      */         {
/*  521 */           if ((queryArray[j] != '.') && (!Character.isLetter(queryArray[j]))) {
/*      */             break;
/*      */           }
/*      */ 
/*  525 */           ++end;
/*      */         }
/*      */ 
/*  528 */         String actionStr = new String(queryArray, i + 2, end - i - 2);
/*  529 */         enumSet = SearchOperatorParsedElements.Action.getActions(actionStr);
/*      */ 
/*  531 */         if (Report.m_verbose)
/*      */         {
/*  533 */           Report.trace("searchqueryparse", "Action String with the query is " + actionStr, null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  538 */       if ((isValue) && (type != null) && (((type.equalsIgnoreCase("text")) || (type.equalsIgnoreCase("zone")))))
/*      */       {
/*  541 */         if (enumSet != null)
/*      */         {
/*  543 */           if (!enumSet.contains(SearchOperatorParsedElements.Action.ESCAPE))
/*      */           {
/*  545 */             enumSet.add(SearchOperatorParsedElements.Action.ESCAPE);
/*      */           }
/*  549 */           else if (Report.m_verbose)
/*      */           {
/*  551 */             Report.trace("searchqueryparse", "Escape action was already present in the action string of the query and hence did not add a duplicate", null);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  557 */           enumSet = SearchOperatorParsedElements.Action.getActions(SearchOperatorParsedElements.Action.ESCAPE.getActionString());
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  562 */       elements.add(element);
/*  563 */       element.m_parsedElement = new String(queryArray, index[(arrayLen - 1)], end - index[(arrayLen - 1)]);
/*      */ 
/*  565 */       element.m_parsedElementType = ((isValue) ? 2 : 1);
/*      */ 
/*  567 */       element.m_actions = enumSet;
/*  568 */       index[arrayLen] = end;
/*  569 */       ++arrayLen;
/*  570 */       element = new SearchOperatorParsedElement();
/*      */     }
/*      */ 
/*  575 */     if ((arrayLen < 2) || (queryArray.length > index[(arrayLen - 1)]))
/*      */     {
/*  578 */       elements.add(element);
/*  579 */       element.m_parsedElement = new String(queryArray, index[(arrayLen - 1)], queryArray.length - index[(arrayLen - 1)]);
/*      */ 
/*  581 */       element.m_parsedElementType = 0;
/*      */     }
/*      */ 
/*  596 */     String[][] escapeMap = (String[][])null;
/*  597 */     CommonSearchEngineConfig engineCfg = retrieveEngineConfig(engineName);
/*  598 */     escapeMap = engineCfg.m_escapeCharMap;
/*  599 */     String skippedChar = getEngineValue(engineName, "WildCardAny");
/*  600 */     SearchOperatorParsedElements sope = new SearchOperatorParsedElements();
/*  601 */     sope.init(name, type, elements, engineName, escapeMap, skippedChar);
/*  602 */     sope.m_prefix = getEngineValue(engineName, "SearchOperatorFieldPrefix");
/*  603 */     sope.m_suffix = getEngineValue(engineName, "SearchOperatorFieldSuffix");
/*  604 */     return sope;
/*      */   }
/*      */ 
/*      */   protected Properties getCurrentConfigFromKey(String name)
/*      */   {
/*  609 */     return getCurrentConfig(this.m_whereClauseDefs, getLabel(name, "(", ")"));
/*      */   }
/*      */ 
/*      */   protected Properties getCurrentParsedQueriesFromKey(String name)
/*      */   {
/*  614 */     return getCurrentConfig(this.m_parsedQueries, getLabel(name, "(", ")"));
/*      */   }
/*      */ 
/*      */   protected String getLabel(String str, String beginStr, String endStr)
/*      */   {
/*  619 */     return StringUtils.getLabel(str, beginStr, endStr);
/*      */   }
/*      */ 
/*      */   protected Properties getCurrentConfig(Map<String, Properties> configs, String configLabel)
/*      */   {
/*  624 */     return getCurrentConfig(configs, configLabel, false);
/*      */   }
/*      */ 
/*      */   protected Properties getCurrentConfig(Map<String, Properties> configs, String configLabel, boolean allowDefault)
/*      */   {
/*  629 */     if (configLabel == null)
/*      */     {
/*  631 */       configLabel = "COMMON";
/*      */     }
/*      */ 
/*  634 */     Properties curConfig = (Properties)configs.get(configLabel);
/*      */ 
/*  637 */     if (curConfig == null)
/*      */     {
/*  639 */       int index = configLabel.lastIndexOf(".");
/*  640 */       if (index >= 0)
/*      */       {
/*  642 */         String tmpLabel = configLabel.substring(0, index);
/*  643 */         Properties p = (Properties)configs.get(tmpLabel);
/*      */ 
/*  645 */         if (p != null)
/*      */         {
/*  647 */           curConfig = new Properties(p);
/*  648 */           configs.put(configLabel, curConfig);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  653 */     if (curConfig == null)
/*      */     {
/*  655 */       if ((configLabel.equals("COMMON")) || (!allowDefault))
/*      */       {
/*  657 */         curConfig = new Properties();
/*      */       }
/*      */       else
/*      */       {
/*  661 */         Properties defaultProps = getCurrentConfig(configs, "COMMON");
/*  662 */         curConfig = new Properties(defaultProps);
/*      */       }
/*  664 */       configs.put(configLabel, curConfig);
/*      */     }
/*      */ 
/*  667 */     return curConfig;
/*      */   }
/*      */ 
/*      */   protected void prepareSearchEngineRules() throws DataException
/*      */   {
/*  672 */     DataResultSet drset = (DataResultSet)this.m_configs.get("SearchEngineRules");
/*  673 */     FieldInfo fi = new FieldInfo();
/*  674 */     drset.getFieldInfo("ComputeAtInit", fi);
/*  675 */     prepareSearchEngineConfiguration(this.m_engineRules, drset, fi);
/*      */   }
/*      */ 
/*      */   protected void prepareSearchFieldAliasName() throws DataException
/*      */   {
/*  680 */     DataResultSet drset = (DataResultSet)this.m_configs.get("SearchFieldAliasName");
/*  681 */     prepareSearchEngineConfiguration(this.m_searchFieldAliasName, drset, null);
/*      */   }
/*      */ 
/*      */   protected void prepareSearchEngineConfiguration(Map<String, Properties> configContainer, DataResultSet drset, FieldInfo scriptEvalFieldInfo)
/*      */     throws DataException
/*      */   {
/*  687 */     boolean useScript = false;
/*  688 */     if ((scriptEvalFieldInfo != null) && (scriptEvalFieldInfo.m_index != -1))
/*      */     {
/*  690 */       ExecutionContextAdaptor ec = new ExecutionContextAdaptor();
/*      */ 
/*  692 */       DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*      */ 
/*  694 */       ec.setCachedObject("DataBinder", binder);
/*  695 */       this.m_dhtml.init(ec);
/*  696 */       useScript = true;
/*      */     }
/*  698 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  700 */       String value = drset.getStringValue(1);
/*  701 */       if (useScript)
/*      */       {
/*  703 */         String tmp = drset.getStringValue(scriptEvalFieldInfo.m_index);
/*  704 */         boolean isEval = StringUtils.convertToBool(tmp, false);
/*  705 */         if (isEval)
/*      */         {
/*      */           try
/*      */           {
/*  709 */             value = this.m_dhtml.evaluateScript(value);
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*  713 */             throw new DataException(e.getMessage());
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  719 */       String name = drset.getStringValue(0);
/*  720 */       String engineName = getLabel(name, "(", ")");
/*  721 */       Properties curConfig = getCurrentConfig(configContainer, engineName, false);
/*      */ 
/*  723 */       int quoteIndex = name.indexOf(41);
/*  724 */       if (quoteIndex != -1)
/*      */       {
/*  726 */         name = name.substring(quoteIndex + 1);
/*      */       }
/*      */ 
/*  729 */       curConfig.setProperty(name, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareSearchEngineConfig()
/*      */   {
/*  735 */     for (String label : this.m_queryConfigs.keySet())
/*      */     {
/*  739 */       CommonSearchEngineConfig sConfig = (CommonSearchEngineConfig)this.m_engineConfig.get(label);
/*  740 */       if (sConfig == null)
/*      */       {
/*  742 */         prepareSearchEngineConfig(label);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected CommonSearchEngineConfig prepareSearchEngineConfig(String label)
/*      */   {
/*  749 */     CommonSearchEngineConfig sConfig = new CommonSearchEngineConfig();
/*  750 */     sConfig.m_engineName = label;
/*  751 */     String escapeChars = getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", label, "EscapeChars");
/*      */ 
/*  753 */     String additionalEscapeChars = getEngineValue(label, "AdditionalEscapeChars", true, ',');
/*      */ 
/*  755 */     if ((escapeChars == null) || (escapeChars.trim().length() == 0))
/*      */     {
/*  757 */       escapeChars = additionalEscapeChars;
/*      */     }
/*  759 */     else if ((additionalEscapeChars != null) && (additionalEscapeChars.trim().length() > 0))
/*      */     {
/*  761 */       escapeChars = escapeChars + "," + additionalEscapeChars;
/*      */     }
/*  763 */     if ((escapeChars != null) && (escapeChars.length() > 0))
/*      */     {
/*  765 */       List escapeCharList = StringUtils.makeListFromSequenceSimple(escapeChars);
/*  766 */       sConfig.m_escapeCharMap = new String[escapeCharList.size()][2];
/*  767 */       for (int i = 0; i < sConfig.m_escapeCharMap.length; ++i)
/*      */       {
/*  769 */         String pair = (String)escapeCharList.get(i);
/*  770 */         List mapping = StringUtils.makeListFromSequence(pair, ':', ':', 32);
/*  771 */         if (mapping.size() < 2)
/*      */         {
/*  773 */           Report.trace("search", "Escape char error: " + pair, null);
/*      */         }
/*      */         else {
/*  776 */           sConfig.m_escapeCharMap[i][0] = ((String)mapping.get(0));
/*  777 */           sConfig.m_escapeCharMap[i][1] = ((String)mapping.get(1));
/*      */         }
/*      */       }
/*      */     }
/*  780 */     sConfig.m_sortFieldDefault = getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", label, "SortFieldDefault");
/*      */ 
/*  783 */     sConfig.m_sortOrderDefault = getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", label, "SortOrderDefault");
/*      */ 
/*  786 */     String allowUpdate = getEngineValue(label, "SearchCacheAllowDiffUpdates");
/*  787 */     sConfig.m_allowCacheDiffUpdates = StringUtils.convertToBool(allowUpdate, true);
/*  788 */     String altKey = "SearchCacheDiffUpdateLevel:" + label;
/*  789 */     String updateLevel = SharedObjects.getEnvironmentValue(altKey);
/*  790 */     if ((updateLevel == null) || (updateLevel.length() == 0))
/*      */     {
/*  792 */       updateLevel = getEngineValue(label, "SearchCacheDiffUpdateLevel");
/*      */     }
/*  794 */     if (updateLevel == null)
/*      */     {
/*  796 */       updateLevel = "repair";
/*      */     }
/*  798 */     sConfig.setDiffUpdateLevel(updateLevel);
/*      */ 
/*  801 */     ArrayList fullTextFields = new ArrayList();
/*  802 */     addAdditionalFields(label, "DrillDownFields", fullTextFields);
/*  803 */     sConfig.m_numNavigationFields = fullTextFields.size();
/*      */ 
/*  805 */     addAdditionalFields(label, "FullTextFields", fullTextFields);
/*  806 */     sConfig.m_fullTextFields = ((String[])fullTextFields.toArray(new String[0]));
/*      */ 
/*  808 */     String temp = getEngineValue(label, "InvalidateQueryCacheOnOutRangeRow");
/*  809 */     sConfig.m_invalidateQueryCacheOnOutRangeRow = StringUtils.convertToBool(temp, false);
/*      */ 
/*  813 */     this.m_engineConfig.put(label, sConfig);
/*      */ 
/*  815 */     return sConfig;
/*      */   }
/*      */ 
/*      */   protected void addAdditionalFields(String label, String key, ArrayList fields)
/*      */   {
/*  820 */     String additionalFieldsStr = getEngineValue(label, key);
/*  821 */     List additionalFields = StringUtils.makeListFromSequenceSimple(additionalFieldsStr);
/*  822 */     for (String item : additionalFields)
/*      */     {
/*  824 */       if (!fields.contains(item))
/*      */       {
/*  826 */         fields.add(item);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareOperatorTypes() throws DataException
/*      */   {
/*  833 */     DataResultSet drset = (DataResultSet)this.m_configs.get("SearchOperatorMap");
/*      */ 
/*  835 */     FieldInfo fi = new FieldInfo();
/*  836 */     drset.getFieldInfo("OperatorName", fi);
/*  837 */     int nameIndex = fi.m_index;
/*  838 */     fi = new FieldInfo();
/*  839 */     drset.getFieldInfo("OperatorDataType", fi);
/*  840 */     int typeIndex = fi.m_index;
/*  841 */     IdcStringBuilder buf = new IdcStringBuilder("|");
/*  842 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  844 */       String name = drset.getStringValue(nameIndex);
/*  845 */       int endQuote = name.indexOf(41);
/*  846 */       if (endQuote == name.length() - 1)
/*      */       {
/*  848 */         String msg = LocaleUtils.encodeMessage("csOperatorDefinitionNameError", null, name);
/*  849 */         throw new DataException(msg);
/*      */       }
/*  851 */       name = name.substring(endQuote + 1);
/*  852 */       if (this.m_operatorTypes.get(name.toLowerCase()) != null)
/*      */         continue;
/*  854 */       String type = drset.getStringValue(typeIndex);
/*  855 */       this.m_operatorTypes.setProperty(name.toLowerCase(), type);
/*  856 */       if (!type.equalsIgnoreCase("text"))
/*      */         continue;
/*  858 */       buf.append(name);
/*  859 */       buf.append("|");
/*      */     }
/*      */ 
/*  863 */     if ((buf.length() <= 1) || (SharedObjects.getEnvironmentValue("SearchEscapableOperators") != null)) {
/*      */       return;
/*      */     }
/*  866 */     SharedObjects.putEnvironmentValue("SearchEscapableOperators", buf.toString());
/*      */   }
/*      */ 
/*      */   protected void prepareClientTables(ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  875 */     if (CLIENT_TABLE_INITED)
/*      */     {
/*  877 */       return;
/*      */     }
/*      */ 
/*  880 */     Map containers = new Hashtable();
/*      */ 
/*  883 */     Properties defaultProperties = new Properties();
/*  884 */     containers.put("COMMON", defaultProperties);
/*      */ 
/*  886 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/*  887 */     FieldInfo seId = new FieldInfo();
/*  888 */     searchEngines.getFieldInfo("seId", seId);
/*  889 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/*  891 */       Properties lastProperties = defaultProperties;
/*  892 */       String id = searchEngines.getStringValue(seId.m_index);
/*      */ 
/*  894 */       int index = id.indexOf(46);
/*  895 */       boolean finished = false;
/*  896 */       while (!finished)
/*      */       {
/*      */         String name;
/*      */         String name;
/*  899 */         if (index >= 0)
/*      */         {
/*  901 */           name = id.substring(0, index);
/*      */         }
/*      */         else
/*      */         {
/*  905 */           name = id;
/*  906 */           finished = true;
/*      */         }
/*      */ 
/*  909 */         Properties p = (Properties)containers.get(name);
/*  910 */         if (p == null)
/*      */         {
/*  912 */           p = new Properties(lastProperties);
/*  913 */           containers.put(name, p);
/*      */         }
/*  915 */         lastProperties = p;
/*      */ 
/*  917 */         index = id.indexOf(46, index + 1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  924 */     containers.put("DATABASEFULLTEXT", containers.get("DATABASE.FULLTEXT"));
/*      */ 
/*  927 */     containers.put("DATABASE", containers.get("DATABASE.METADATA"));
/*      */ 
/*  929 */     prepareClientOpMap(containers);
/*  930 */     prepareClientOpStrMap(containers);
/*  931 */     prepareClientSortFields(containers);
/*  932 */     prepareClientSpecialValues(containers);
/*      */ 
/*  935 */     prepareUniversalClientTables(containers);
/*      */ 
/*  937 */     SharedObjects.putObject("globalObjects", "CommonSearchClientObjects", containers);
/*      */ 
/*  939 */     CLIENT_TABLE_INITED = true;
/*      */ 
/*  941 */     ctxt.setCachedObject("CommonSearchClientObjects", containers);
/*  942 */     ctxt.setCachedObject("CommonSearchConfig", this);
/*  943 */     PluginFilters.filter("prepareSearchEngineClientTables", null, null, ctxt);
/*      */   }
/*      */ 
/*      */   public synchronized boolean preParseQueryWithSuffix(String engineLabel, String suffix, ExecutionContext ctxt)
/*      */     throws ServiceException
/*      */   {
/*  949 */     if ((suffix == null) || (suffix.length() == 0))
/*      */     {
/*  951 */       String msg = LocaleUtils.encodeMessage("csSearchIllegalQuerySuffix", null, suffix);
/*  952 */       throw new ServiceException(msg);
/*      */     }
/*  954 */     if (engineLabel == null)
/*      */     {
/*  956 */       engineLabel = "COMMON";
/*      */     }
/*  958 */     String key = engineLabel + "." + suffix;
/*  959 */     Properties parsed = getCurrentConfig(this.m_parsedQueries, key);
/*      */     Properties suffixConfig;
/*      */     DynamicHtmlMerger dhtml;
/*      */     Enumeration en;
/*  960 */     if (parsed.size() == 0)
/*      */     {
/*  963 */       Properties engineConfig = getCurrentConfig(this.m_whereClauseDefs, engineLabel);
/*  964 */       if (engineConfig == null)
/*      */       {
/*  966 */         String msg = LocaleUtils.encodeMessage("csSearchUnablePreparseQueryWithSuffix", null, engineLabel, suffix);
/*      */ 
/*  968 */         throw new ServiceException(msg);
/*      */       }
/*  970 */       suffixConfig = getCurrentConfig(this.m_whereClauseDefs, key);
/*  971 */       suffixConfig.putAll(engineConfig);
/*      */ 
/*  973 */       dhtml = this.m_dhtml;
/*  974 */       dhtml.init(ctxt);
/*  975 */       for (en = suffixConfig.propertyNames(); en.hasMoreElements(); )
/*      */       {
/*  977 */         String name = (String)en.nextElement();
/*  978 */         String value = suffixConfig.getProperty(name);
/*      */         try
/*      */         {
/*  981 */           value = dhtml.evaluateScript(value);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  986 */           throw new ServiceException("!csSearchUnableToParseQueryDefinition", e);
/*      */         }
/*  988 */         suffixConfig.setProperty(name, value);
/*      */ 
/*  990 */         String type = this.m_operatorTypes.getProperty(name.toLowerCase());
/*  991 */         SearchOperatorParsedElements queryElements = parseQuery(name, value, type, key);
/*  992 */         parsed.put(name, queryElements);
/*      */       }
/*      */     }
/*  995 */     return true;
/*      */   }
/*      */ 
/*      */   public void setCurrentConfig(String label) throws DataException
/*      */   {
/* 1000 */     if (label == this.m_currentCfgLabel)
/*      */     {
/* 1002 */       return;
/*      */     }
/*      */ 
/* 1005 */     if (label != null)
/*      */     {
/* 1007 */       label = label.toUpperCase();
/*      */     }
/*      */     else
/*      */     {
/* 1011 */       label = "VERITY.VDK.4";
/*      */     }
/*      */ 
/* 1014 */     if (label.equals("DATABASEFULLTEXT"))
/*      */     {
/* 1016 */       label = "DATABASE.FULLTEXT";
/*      */     }
/* 1018 */     else if (label.equals("DATABASE"))
/*      */     {
/* 1020 */       label = "DATABASE.METADATA";
/*      */     }
/*      */     else
/*      */     {
/* 1024 */       String name = SharedObjects.getEnvironmentValue("SearchEngineName");
/* 1025 */       if (name == null)
/*      */       {
/* 1027 */         name = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*      */       }
/*      */ 
/* 1030 */       if ((name != null) && (name.startsWith(label + ".")))
/*      */       {
/* 1032 */         label = name;
/*      */       }
/*      */     }
/*      */ 
/* 1036 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 1037 */     FieldInfo seId = new FieldInfo();
/* 1038 */     searchEngines.getFieldInfo("seId", seId);
/* 1039 */     if (searchEngines.findRow(seId.m_index, label) == null)
/*      */     {
/* 1041 */       createDataException(null, LocaleUtils.encodeMessage("csSearchIndexerEngineNotExist", null, label));
/*      */     }
/*      */ 
/* 1045 */     this.m_currentCfgLabel = label;
/* 1046 */     CommonSearchEngineConfig sConfig = (CommonSearchEngineConfig)this.m_engineConfig.get(label);
/* 1047 */     if (sConfig == null)
/*      */     {
/* 1049 */       sConfig = prepareSearchEngineConfig(label);
/*      */     }
/* 1051 */     this.m_currentSearchEngineConfig = sConfig;
/*      */   }
/*      */ 
/*      */   protected void createAndStoreSortFields(DataResultSet drset1, String label)
/*      */     throws DataException
/*      */   {
/* 1063 */     String[][] sortFields = ResultSetUtils.createFilteredStringTable(drset1, new String[] { "IsSortable", "SearchFieldName", "SearchFieldWWString" }, "1");
/*      */ 
/* 1065 */     DataResultSet drset = new DataResultSet(new String[] { "SearchFieldName", "SearchFieldWWString" });
/* 1066 */     label = "(" + label + ")";
/* 1067 */     for (int i = 0; i < sortFields.length; ++i)
/*      */     {
/* 1070 */       String name = sortFields[i][0];
/*      */       Vector v;
/* 1071 */       if (name.startsWith("("))
/*      */       {
/* 1073 */         if (!name.startsWith(label))
/*      */           continue;
/* 1075 */         Vector v = new IdcVector();
/* 1076 */         name = name.substring(label.length());
/*      */       }
/*      */       else
/*      */       {
/* 1085 */         v = new IdcVector();
/*      */       }
/* 1087 */       v.addElement(name);
/* 1088 */       v.addElement(sortFields[i][1]);
/* 1089 */       drset.addRow(v);
/*      */     }
/* 1091 */     SharedObjects.putTable("SearchSortFields", drset);
/*      */   }
/*      */ 
/*      */   protected void prepareClientOpMap(Map<String, Properties> containers) throws DataException
/*      */   {
/* 1096 */     Hashtable labelMapHash = convertToHash(DATA_TYPE_LABEL_MAP);
/*      */ 
/* 1100 */     DataResultSet queryWhereClause = (DataResultSet)this.m_configs.get("SearchOperatorMap");
/* 1101 */     DataResultSet drset = new DataResultSet();
/* 1102 */     int typeIndex = ResultSetUtils.getIndexMustExist(queryWhereClause, "OperatorDataType");
/* 1103 */     int usableIndex = ResultSetUtils.getIndexMustExist(queryWhereClause, "UsedInQueryConstruction");
/*      */ 
/* 1105 */     for (queryWhereClause.first(); queryWhereClause.isRowPresent(); )
/*      */     {
/* 1108 */       boolean usable = StringUtils.convertToBool(queryWhereClause.getStringValue(usableIndex), false);
/* 1109 */       String name = queryWhereClause.getStringValue(0).trim();
/* 1110 */       if (usable)
/*      */       {
/* 1114 */         int closeParan = name.indexOf(41);
/* 1115 */         int openParan = name.indexOf(40);
/* 1116 */         String label = name.substring(openParan + 1, closeParan);
/* 1117 */         String type = queryWhereClause.getStringValue(typeIndex);
/*      */ 
/* 1119 */         String resultSetLabel = (String)labelMapHash.get(type.toLowerCase());
/* 1120 */         if (resultSetLabel == null)
/*      */         {
/* 1122 */           createDataException(null, LocaleUtils.encodeMessage("csSearchTypeNotFoundAsClientOperator", null, type));
/*      */         }
/*      */ 
/* 1125 */         Properties container = getCurrentConfig(containers, label);
/* 1126 */         drset = (DataResultSet)container.get(resultSetLabel);
/* 1127 */         if (drset == null)
/*      */         {
/* 1129 */           drset = new DataResultSet();
/* 1130 */           drset.copyFieldInfo(queryWhereClause);
/* 1131 */           Vector fi = ResultSetUtils.createFieldInfo(new String[] { "OpWwStrings", "OpApStrings" }, 0);
/* 1132 */           drset.mergeFieldsWithFlags(fi, 0);
/* 1133 */           container.put(resultSetLabel, drset);
/*      */         }
/* 1135 */         name = name.substring(closeParan + 1);
/*      */ 
/* 1137 */         Vector v = drset.findRow(0, name);
/* 1138 */         if (v == null)
/*      */         {
/* 1140 */           Vector old = queryWhereClause.getCurrentRowValues();
/* 1141 */           v = (Vector)old.clone();
/* 1142 */           v.setElementAt(name, 0);
/* 1143 */           name = retrieveBaseOperator(name);
/* 1144 */           String ww = getStringFromTable("SearchOperatorStrings", "OperatorName", name, "OpWwStrings");
/*      */ 
/* 1146 */           String ap = getStringFromTable("SearchOperatorStrings", "OperatorName", name, "OpApStrings");
/*      */ 
/* 1148 */           v.addElement(ww);
/* 1149 */           v.addElement(ap);
/* 1150 */           drset.addRow(v);
/*      */         }
/*      */         else
/*      */         {
/* 1154 */           Vector curValues = queryWhereClause.getCurrentRowValues();
/* 1155 */           int len = curValues.size();
/* 1156 */           for (int i = 1; i < len; ++i)
/*      */           {
/* 1158 */             v.setElementAt(curValues.elementAt(i), i);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1165 */         ResultSetUtils.sortResultSet(drset, new String[] { "OperatorOrder" });
/*      */       }
/* 1106 */       queryWhereClause.next();
/*      */     }
/*      */ 
/* 1168 */     for (int i = 0; i < DATA_TYPE_LABEL_MAP.length; ++i)
/*      */     {
/* 1170 */       String rsetName = DATA_TYPE_LABEL_MAP[i][1];
/* 1171 */       createInheritedClientDataResultSets(containers, rsetName);
/*      */     }
/*      */ 
/* 1175 */     for (String key : containers.keySet())
/*      */     {
/* 1177 */       Properties container = (Properties)containers.get(key);
/* 1178 */       drset = new DataResultSet();
/* 1179 */       drset.copyFieldInfo(queryWhereClause);
/* 1180 */       FieldInfo fi = new FieldInfo();
/* 1181 */       drset.getFieldInfo("OperatorOrder", fi);
/* 1182 */       for (int i = 0; i < DATA_TYPE_LABEL_MAP.length; ++i)
/*      */       {
/* 1184 */         DataResultSet map = (DataResultSet)container.get(DATA_TYPE_LABEL_MAP[i][1]);
/* 1185 */         if (map == null) continue; if (map.getNumRows() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1189 */         drset.merge(null, map, false);
/* 1190 */         for (map.last(); map.isRowPresent(); map.previous())
/*      */         {
/* 1192 */           String order = map.getStringValue(fi.m_index);
/* 1193 */           if (Integer.parseInt(order) < 0)
/*      */           {
/* 1195 */             map.deleteCurrentRow();
/*      */           }
/* 1197 */           if (map.getCurrentRow() == 0) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1203 */       container.put("SearchQueryOpMap", drset);
/* 1204 */       setFieldComparisonIndex(container);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createInheritedClientDataResultSets(Map<String, Properties> containers, String resultSetName)
/*      */     throws DataException
/*      */   {
/* 1211 */     Properties baseContainer = (Properties)containers.get("COMMON");
/* 1212 */     DataResultSet baseResultSet = (DataResultSet)baseContainer.get(resultSetName);
/*      */ 
/* 1214 */     HashMap alreadySet = new HashMap();
/*      */ 
/* 1216 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 1217 */     int colIndex = ResultSetUtils.getIndexMustExist(searchEngines, "seId");
/* 1218 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/* 1220 */       String id = searchEngines.getStringValue(colIndex);
/* 1221 */       DataResultSet lastResultSet = baseResultSet;
/*      */ 
/* 1223 */       boolean finished = false;
/* 1224 */       int index = id.indexOf(46);
/* 1225 */       while (!finished)
/*      */       {
/*      */         String name;
/*      */         String name;
/* 1228 */         if (index >= 0)
/*      */         {
/* 1230 */           name = id.substring(0, index);
/*      */         }
/*      */         else
/*      */         {
/* 1234 */           name = id;
/* 1235 */           finished = true;
/*      */         }
/*      */ 
/* 1238 */         Properties thisContainer = (Properties)containers.get(name);
/* 1239 */         DataResultSet thisResultSet = (DataResultSet)thisContainer.get(resultSetName);
/*      */ 
/* 1241 */         if (alreadySet.get(name) == null)
/*      */         {
/* 1243 */           if ((thisResultSet == null) && (lastResultSet != null))
/*      */           {
/* 1245 */             thisResultSet = new DataResultSet();
/* 1246 */             thisResultSet.copy(lastResultSet);
/* 1247 */             thisContainer.put(resultSetName, thisResultSet);
/*      */           }
/* 1249 */           else if ((thisResultSet != null) && (lastResultSet != null) && (thisResultSet != lastResultSet))
/*      */           {
/* 1251 */             DataResultSet newSet = new DataResultSet();
/* 1252 */             newSet.copy(lastResultSet);
/* 1253 */             newSet.merge(newSet.getFieldName(0), thisResultSet, false);
/*      */ 
/* 1255 */             thisResultSet = newSet;
/* 1256 */             thisContainer.put(resultSetName, thisResultSet);
/*      */           }
/*      */ 
/* 1259 */           alreadySet.put(name, "1");
/*      */         }
/*      */ 
/* 1262 */         lastResultSet = thisResultSet;
/* 1263 */         index = id.indexOf(46, index + 1);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setFieldComparisonIndex(Properties container)
/*      */   {
/* 1270 */     IdcStringBuilder comparisonMap = new IdcStringBuilder();
/* 1271 */     int counter = 0;
/* 1272 */     for (int i = 0; i < DATA_TYPE_LABEL_MAP.length; ++i)
/*      */     {
/* 1274 */       if (i != 0)
/*      */       {
/* 1276 */         comparisonMap.append(',');
/*      */       }
/* 1278 */       DataResultSet map = (DataResultSet)container.get(DATA_TYPE_LABEL_MAP[i][1]);
/* 1279 */       if ((map == null) || (map.getNumRows() == 0))
/*      */       {
/* 1281 */         comparisonMap.append("-1");
/*      */       }
/*      */       else {
/* 1284 */         comparisonMap.append(counter);
/* 1285 */         counter += map.getNumRows();
/*      */       }
/*      */     }
/* 1288 */     container.setProperty("FieldComparisonIndex", comparisonMap.toString());
/*      */   }
/*      */ 
/*      */   public Hashtable convertToHash(String[][] labelArray)
/*      */   {
/* 1293 */     Hashtable ht = new Hashtable();
/* 1294 */     for (int i = 0; i < labelArray.length; ++i)
/*      */     {
/* 1296 */       ht.put(labelArray[i][0], labelArray[i][1]);
/*      */     }
/* 1298 */     return ht;
/*      */   }
/*      */ 
/*      */   protected void prepareClientOpStrMap(Map<String, Properties> containers)
/*      */     throws DataException
/*      */   {
/* 1308 */     DataResultSet queryPresentation = (DataResultSet)this.m_configs.get("SearchOperatorStrings");
/* 1309 */     for (String key : containers.keySet())
/*      */     {
/* 1311 */       Properties container = (Properties)containers.get(key);
/*      */ 
/* 1313 */       DataResultSet opMap = (DataResultSet)container.get("SearchQueryOpMap");
/* 1314 */       if (opMap == null)
/*      */       {
/* 1316 */         createDataException(null, "csNullSearchQueryOpMap");
/*      */       }
/*      */ 
/* 1319 */       DataResultSet drset = new DataResultSet();
/* 1320 */       drset.copyFieldInfo(queryPresentation);
/* 1321 */       for (opMap.first(); opMap.isRowPresent(); opMap.next())
/*      */       {
/* 1323 */         String strkey = opMap.getStringValue(0);
/* 1324 */         Vector v = queryPresentation.findRow(0, strkey);
/* 1325 */         if (v == null)
/*      */           continue;
/* 1327 */         drset.addRow(v);
/*      */       }
/*      */ 
/* 1330 */       container.put("SearchQueryOpStrMap", drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareClientSortFields(Map<String, Properties> containers) throws DataException
/*      */   {
/* 1336 */     DataResultSet searchFields = (DataResultSet)this.m_configs.get("SearchFieldInfo");
/* 1337 */     DataResultSet sortFieldsTable = new DataResultSet();
/* 1338 */     sortFieldsTable.copyFieldInfo(searchFields);
/* 1339 */     FieldInfo fi = new FieldInfo();
/* 1340 */     searchFields.getFieldInfo("IsSortable", fi);
/* 1341 */     for (searchFields.first(); searchFields.next(); )
/*      */     {
/* 1343 */       String sortable = searchFields.getStringValue(fi.m_index);
/* 1344 */       if (StringUtils.convertToBool(sortable, false))
/*      */       {
/* 1346 */         Vector row = searchFields.getCurrentRowValues();
/* 1347 */         sortFieldsTable.addRow(row);
/*      */       }
/*      */     }
/*      */ 
/* 1351 */     String[] columns = { "SearchFieldName", "SearchFieldWWString" };
/* 1352 */     String[][] sortFields = ResultSetUtils.createStringTable(sortFieldsTable, columns);
/* 1353 */     for (String key : containers.keySet())
/*      */     {
/* 1355 */       Properties container = (Properties)containers.get(key);
/*      */ 
/* 1357 */       if (container == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1361 */       DataResultSet drset = new DataResultSet(columns);
/* 1362 */       String label = "(" + key + ")";
/* 1363 */       for (int i = 0; i < sortFields.length; ++i)
/*      */       {
/* 1366 */         String name = sortFields[i][0];
/*      */         Vector v;
/* 1367 */         if (name.startsWith("("))
/*      */         {
/* 1369 */           String thisLabel = getLabel(name, "(", ")");
/* 1370 */           if ((!name.startsWith(label)) && (!key.startsWith(thisLabel + ".")))
/*      */             continue;
/* 1372 */           Vector v = new IdcVector();
/* 1373 */           name = name.substring(thisLabel.length() + 2);
/*      */         }
/*      */         else
/*      */         {
/* 1382 */           v = new IdcVector();
/*      */         }
/* 1384 */         v.addElement(name);
/* 1385 */         v.addElement(sortFields[i][1]);
/*      */ 
/* 1392 */         if (drset.findRow(1, sortFields[i][1]) != null)
/*      */           continue;
/* 1394 */         drset.addRow(v);
/*      */       }
/*      */ 
/* 1397 */       container.put("SearchSortFields", drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareClientSpecialValues(Map<String, Properties> containers) throws DataException
/*      */   {
/* 1403 */     String[][] VALUEMAP = { { "and", "SearchConjunction" }, { "or", "SearchOrConjunction" }, { "not", "SearchNotOperator" }, { "fulltext", "SearchFullTextQueryDef" }, { "quicksearch", "QuickSearchQuery" } };
/*      */ 
/* 1407 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 1408 */     int colIndex = ResultSetUtils.getIndexMustExist(searchEngines, "seId");
/*      */ 
/* 1410 */     for (String key : containers.keySet())
/*      */     {
/* 1412 */       Properties container = (Properties)containers.get(key);
/* 1413 */       Properties whereClause = (Properties)this.m_whereClauseDefs.get(key);
/*      */ 
/* 1415 */       for (int i = 0; i < VALUEMAP.length; ++i)
/*      */       {
/* 1417 */         String value = whereClause.getProperty(VALUEMAP[i][0]);
/* 1418 */         if (value != null)
/*      */         {
/* 1420 */           container.setProperty(VALUEMAP[i][1], value);
/*      */         } else {
/* 1422 */           if ((!VALUEMAP[i][0].equals("and")) || (searchEngines.findRow(colIndex, key) == null))
/*      */             continue;
/* 1424 */           createDataException(null, LocaleUtils.encodeMessage("csSearchNoAndOperatorDefined", null, key));
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1429 */       String escapeChars = getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", key, "EscapeChars");
/* 1430 */       if ((escapeChars != null) && (escapeChars.length() != 0))
/*      */       {
/* 1432 */         container.setProperty("EscapeChars", escapeChars);
/* 1433 */         String searchKey = "SearchEngine" + key + "EscapeChars";
/* 1434 */         SharedObjects.putEnvironmentValue(searchKey, escapeChars);
/*      */       }
/*      */ 
/* 1437 */       for (int i = 0; i < this.OTHER_CLIENT_VALUES.length; ++i)
/*      */       {
/* 1439 */         String value = getEngineValue(key, this.OTHER_CLIENT_VALUES[i][0]);
/* 1440 */         container.setProperty(this.OTHER_CLIENT_VALUES[i][1], value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareUniversalClientTables(Map<String, Properties> containers)
/*      */   {
/* 1447 */     if (containers.get("VERITY") == null) {
/*      */       return;
/*      */     }
/* 1450 */     String[][] lists = { { "UniversalQueryOperators", "SearchQueryOpMap", "OperatorName" }, { "UniversalQueryOperators", "SearchQueryOpStrMap", "OperatorName" }, { "UniversalQueryOperators", "SearchTextField", "OperatorName" }, { "UniversalZoneQueryOperators", "SearchZoneField", "OperatorName" }, { "UniversalSortFields", "SearchSortFields", "SearchFieldName" } };
/*      */ 
/* 1457 */     String allowAllStr = getEngineValue("UniversalSearchOpMapAllowAll");
/* 1458 */     boolean opMapAllowAll = StringUtils.convertToBool(allowAllStr, true);
/* 1459 */     String[] engineList = null;
/* 1460 */     String curEngine = getCurrentEngineName();
/* 1461 */     if (curEngine.equals("DATABASE.METADATA"))
/*      */     {
/* 1463 */       engineList = new String[] { "DATABASE.METADATA" };
/*      */     }
/*      */     else
/*      */     {
/* 1467 */       engineList = new String[] { curEngine, "DATABASE.METADATA" };
/*      */     }
/* 1469 */     for (int i = 0; i < engineList.length; ++i)
/*      */     {
/* 1471 */       Properties container = (Properties)((Hashtable)containers.get("VERITY")).clone();
/* 1472 */       for (int j = 0; j < lists.length; ++j)
/*      */       {
/* 1474 */         String operators = getEngineValue(engineList[i], lists[j][0]);
/* 1475 */         if (operators == null)
/*      */         {
/* 1477 */           operators = "";
/*      */         }
/* 1479 */         DataResultSet opData = (DataResultSet)container.get(lists[j][1]);
/* 1480 */         opData = opData.shallowClone();
/* 1481 */         FieldInfo fi = new FieldInfo();
/* 1482 */         opData.getFieldInfo(lists[j][2], fi);
/* 1483 */         for (opData.last(); opData.isRowPresent(); opData.previous())
/*      */         {
/* 1485 */           String name = opData.getStringValue(fi.m_index);
/* 1486 */           if ((operators.indexOf("|" + name + "|") < 0) && (((!opMapAllowAll) || (j != 0))))
/*      */           {
/* 1488 */             opData.deleteCurrentRow();
/*      */           }
/* 1490 */           if (opData.getCurrentRow() == 0) {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/* 1495 */         container.put(lists[j][1], opData);
/*      */       }
/*      */ 
/* 1499 */       if (!StringUtils.convertToBool(getEngineValue(engineList[i], "UniversalSearchAllowFullText"), true))
/*      */       {
/* 1502 */         container.remove("SearchFullTextQueryDef");
/*      */       }
/*      */ 
/* 1505 */       setFieldComparisonIndex(container);
/*      */ 
/* 1507 */       String quickSearch = getEngineValue(engineList[i], "UniversalQuickSearchQuery");
/* 1508 */       if (quickSearch != null)
/*      */       {
/* 1510 */         container.setProperty("QuickSearchQuery", quickSearch);
/*      */       }
/*      */ 
/* 1514 */       Properties nativeContainer = (Properties)containers.get(engineList[i]);
/* 1515 */       for (int j = 0; j < this.OTHER_CLIENT_VALUES.length; ++j)
/*      */       {
/* 1517 */         container.put(this.OTHER_CLIENT_VALUES[j][1], nativeContainer.get(this.OTHER_CLIENT_VALUES[j][1]));
/*      */       }
/*      */ 
/* 1520 */       if (i == 0)
/*      */       {
/* 1522 */         containers.put("UNIVERSAL", container);
/*      */       }
/*      */       else
/*      */       {
/* 1526 */         containers.put("UNIVERSAL." + engineList[i], container);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public String prepareFullTextQuery(String parserName, String query, DataBinder binder, ExecutionContext context)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1537 */       CommonSearchConfigCompanion companion = getCompanion();
/* 1538 */       return companion.prepareFullTextQuery(query, binder, context);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1542 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareQuery(DataBinder binder, ExecutionContext ctxt, boolean isSearch)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1549 */     CommonSearchConfigCompanion companion = getCompanion();
/* 1550 */     int val = PluginFilters.filter("prepareQuery", null, binder, ctxt);
/* 1551 */     if (val != 0)
/*      */     {
/* 1553 */       return;
/*      */     }
/*      */ 
/* 1556 */     val = companion.prepareQuery(binder, ctxt);
/* 1557 */     if (val != 0)
/*      */     {
/* 1559 */       return;
/*      */     }
/*      */ 
/* 1562 */     String sortSpec = binder.getLocal("SortSpec");
/* 1563 */     if ((sortSpec == null) || (sortSpec.trim().length() == 0))
/*      */     {
/* 1565 */       binder.getLocalData().remove("SortSpec");
/* 1566 */       String sortField = binder.getLocal("SortField");
/* 1567 */       if (sortField != null)
/*      */       {
/* 1569 */         sortField = sortField.trim();
/*      */       }
/*      */ 
/* 1572 */       if ((sortField == null) || (sortField.length() == 0))
/*      */       {
/* 1574 */         sortField = this.m_currentSearchEngineConfig.m_sortFieldDefault;
/*      */       }
/*      */       else
/*      */       {
/* 1578 */         Validation.validateSortField(sortField);
/*      */       }
/* 1580 */       sortField = getFieldNameFromAlias(sortField);
/* 1581 */       binder.putLocal("SortField", sortField);
/*      */ 
/* 1583 */       String sortOrder = binder.getLocal("SortOrder");
/* 1584 */       if (sortOrder != null)
/*      */       {
/* 1586 */         sortOrder = sortOrder.trim();
/*      */       }
/* 1588 */       if ((sortOrder == null) || (sortOrder.length() == 0))
/*      */       {
/* 1590 */         sortOrder = this.m_currentSearchEngineConfig.m_sortOrderDefault;
/*      */       }
/*      */       else
/*      */       {
/* 1594 */         Validation.validateSortOrder(sortOrder);
/*      */       }
/* 1596 */       binder.putLocal("SortOrder", sortOrder);
/*      */ 
/* 1598 */       constructQueryFrag("SortSpec", binder);
/*      */     }
/* 1600 */     if (!isSearch)
/*      */     {
/* 1602 */       return;
/*      */     }
/*      */ 
/* 1605 */     String resultCount = binder.getLocal("ResultCount");
/* 1606 */     String resultStart = binder.getLocal("StartRow");
/*      */ 
/* 1610 */     if ((resultStart == null) || (resultStart.length() == 0))
/*      */     {
/* 1612 */       resultStart = "1";
/*      */     }
/* 1614 */     if ((resultCount == null) || (resultCount.length() == 0))
/*      */     {
/* 1616 */       resultCount = "20";
/* 1617 */       binder.putLocal("ResultCount", "20");
/*      */     }
/*      */     int resultStartInt;
/*      */     try {
/* 1621 */       resultCountInt = Integer.parseInt(resultCount);
/* 1622 */       resultStartInt = Integer.parseInt(resultStart);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1626 */       throw new DataException("!csSearchRangeInvalid");
/*      */     }
/*      */ 
/* 1629 */     int resultCountInt = computeRowCount(binder, resultCountInt, false);
/*      */ 
/* 1632 */     if (resultCountInt > 100000)
/*      */     {
/* 1634 */       throw new DataException("!csSearchResultCountTooBig");
/*      */     }
/*      */ 
/* 1637 */     if (resultStartInt == 0)
/*      */     {
/* 1639 */       String msg = LocaleUtils.encodeMessage("csSearchItemNotPositive", null, "StartRow");
/*      */ 
/* 1641 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1644 */     binder.putLocal("StartRow", "" + resultStartInt);
/* 1645 */     binder.putLocal("ResultCount", "" + resultCountInt);
/* 1646 */     binder.putLocal("EndRow", "" + (resultStartInt + resultCountInt - 1));
/*      */   }
/*      */ 
/*      */   public String prepareQueryText(String queryText, DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1652 */     CommonSearchConfigCompanion companion = getCompanion();
/* 1653 */     binder.putLocal("QueryText", queryText);
/*      */ 
/* 1655 */     int val = PluginFilters.filter("prepareQueryText", null, binder, ctxt);
/* 1656 */     queryText = binder.getLocal("QueryText");
/* 1657 */     if (val != 0)
/*      */     {
/* 1659 */       return queryText;
/*      */     }
/*      */ 
/* 1662 */     val = companion.prepareQueryText(binder, ctxt);
/* 1663 */     queryText = binder.getLocal("QueryText");
/* 1664 */     if (val != 0)
/*      */     {
/* 1666 */       return queryText;
/*      */     }
/*      */     try
/*      */     {
/* 1670 */       IdcStringBuilder sb = new IdcStringBuilder(0);
/* 1671 */       if ((HtmlFilterUtils.encodeForHtmlView(queryText, 1, null, sb, ctxt)) && 
/* 1673 */         (sb.compareTo(0, sb.length(), queryText, 0, queryText.length(), false) != 0))
/*      */       {
/* 1675 */         queryText = sb.toStringNoRelease();
/*      */       }
/*      */ 
/* 1678 */       DynamicHtmlMerger dhtml = getDynamicHtmlMerger(ctxt);
/* 1679 */       queryText = dhtml.evaluateScript(queryText);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1683 */       createDataException(e, "csSearchUnableToPrepareQueryText");
/*      */     }
/*      */ 
/* 1686 */     return queryText;
/*      */   }
/*      */ 
/*      */   public String fixUpAndValidateQuery(String queryText, DataBinder binder) throws DataException, ServiceException
/*      */   {
/* 1691 */     return fixUpAndValidateQuery(queryText, binder, null);
/*      */   }
/*      */ 
/*      */   public String fixUpAndValidateQuery(String queryText, DataBinder binder, ExecutionContext ctxt) throws DataException, ServiceException
/*      */   {
/* 1696 */     CommonSearchConfigCompanion companion = getCompanion();
/* 1697 */     binder.putLocal("QueryText", queryText);
/*      */ 
/* 1699 */     int val = PluginFilters.filter("fixUpAndValidateQuery", null, binder, ctxt);
/* 1700 */     queryText = binder.getLocal("QueryText");
/* 1701 */     if (val == 0)
/*      */     {
/* 1703 */       companion.fixUpAndValidateQuery(binder, ctxt);
/*      */     }
/*      */ 
/* 1706 */     PluginFilters.filter("postFixUpAndValidateQuery", null, binder, ctxt);
/* 1707 */     queryText = binder.getLocal("QueryText");
/* 1708 */     return queryText;
/*      */   }
/*      */ 
/*      */   public String getCacheKey(String secGroupCacheContext, String accounts, String request, DataBinder binder, int flags, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1715 */     CommonSearchConfigCompanion companion = getCompanion();
/* 1716 */     return companion.getCacheKey(secGroupCacheContext, accounts, request, binder, flags, ctxt);
/*      */   }
/*      */ 
/*      */   public String constructElement(String expr, DataBinder binder) throws DataException
/*      */   {
/*      */     try
/*      */     {
/* 1723 */       DynamicHtmlMerger dhtml = this.m_dhtml;
/* 1724 */       ExecutionContext ctxt = new ExecutionContextAdaptor();
/* 1725 */       ctxt.setCachedObject("DataBinder", binder);
/* 1726 */       dhtml.init(ctxt);
/* 1727 */       expr = dhtml.evaluateScript(expr);
/*      */     }
/*      */     catch (Throwable e)
/*      */     {
/* 1731 */       throw new DataException(LocaleUtils.encodeMessage("csSearchFailedToConstructElement", e.getMessage(), expr));
/*      */     }
/* 1733 */     return expr;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void appendInternalClauseElement(IdcAppendable appendable, String[] expr, String name, CharSequence value, boolean useEscapeValue)
/*      */     throws DataException
/*      */   {
/* 1751 */     for (int i = 0; i < expr.length; ++i)
/*      */     {
/* 1753 */       CharSequence nextStr = expr[i];
/* 1754 */       boolean appendedValue = false;
/* 1755 */       if (expr[i].startsWith("%F"))
/*      */       {
/* 1757 */         nextStr = name;
/*      */       }
/* 1759 */       else if (expr[i].startsWith("%V"))
/*      */       {
/* 1761 */         nextStr = value;
/* 1762 */         if (useEscapeValue)
/*      */         {
/* 1764 */           char[] charArray = null;
/* 1765 */           if (value instanceof IdcCharSequence)
/*      */           {
/* 1767 */             IdcCharSequence iValue = (IdcCharSequence)value;
/* 1768 */             charArray = new char[iValue.length()];
/* 1769 */             iValue.getChars(0, charArray.length, charArray, 0);
/*      */           }
/*      */           else
/*      */           {
/* 1773 */             String tmp = value.toString();
/* 1774 */             charArray = tmp.toCharArray();
/*      */           }
/* 1776 */           StringUtils.escapeCharArrayToAppendable(charArray, 0, charArray.length, this.m_currentSearchEngineConfig.m_escapeCharMap, appendable);
/*      */ 
/* 1778 */           appendedValue = true;
/*      */         }
/*      */       }
/* 1781 */       if (appendedValue)
/*      */         continue;
/* 1783 */       appendable.append(nextStr);
/*      */     }
/*      */   }
/*      */ 
/*      */   public QueryElementField getQueryElementField(String name, DataBinder binder, ExecutionContext cxt)
/*      */   {
/* 1798 */     QueryElementField qeF = (QueryElementField)this.m_queryFieldConfigs.get(name);
/* 1799 */     if (qeF == null)
/*      */     {
/* 1801 */       String typeStr = binder.getFieldType(name);
/* 1802 */       int type = 6;
/* 1803 */       if (typeStr != null)
/*      */       {
/* 1805 */         if (typeStr.equalsIgnoreCase("int"))
/*      */         {
/* 1807 */           type = 3;
/*      */         }
/* 1809 */         else if (typeStr.equalsIgnoreCase("date"))
/*      */         {
/* 1811 */           type = 5;
/*      */         }
/*      */       }
/* 1814 */       qeF = new QueryElementField(name, type);
/* 1815 */       this.m_queryFieldConfigs.put(name, qeF);
/*      */     }
/* 1817 */     return qeF;
/*      */   }
/*      */ 
/*      */   public boolean isFieldValueEscapable(String name)
/*      */   {
/* 1822 */     String type = this.m_operatorTypes.getProperty(name.toLowerCase());
/*      */ 
/* 1825 */     return (type != null) && (type.equalsIgnoreCase("text"));
/*      */   }
/*      */ 
/*      */   public String parseElement(String opName, String name, String value)
/*      */     throws DataException
/*      */   {
/* 1832 */     return parseElement(opName, null, name, value);
/*      */   }
/*      */ 
/*      */   public String parseElement(String opName, String configLabel, String name, String value) throws DataException
/*      */   {
/* 1837 */     IdcStringBuilder sb = new IdcStringBuilder();
/* 1838 */     appendClauseElement(sb, opName, configLabel, name, value);
/* 1839 */     return sb.toString();
/*      */   }
/*      */ 
/*      */   public void appendClauseElement(IdcAppendable appendable, String opName, String configLabel, String name, CharSequence value)
/*      */     throws DataException
/*      */   {
/* 1845 */     if (Report.m_verbose)
/*      */     {
/* 1847 */       Report.trace("searchqueryparse", "Into appendClauseElement CommonSearchConfig with operator : " + opName + " value : " + value, null);
/*      */     }
/*      */ 
/* 1851 */     SearchOperatorParsedElements expr = retrieveParsedElements(opName, configLabel, name);
/*      */ 
/* 1853 */     if (expr == null)
/*      */     {
/* 1855 */       throw new DataException(LocaleUtils.encodeMessage("csSearchQueryOperatorNotExist", null, opName));
/*      */     }
/* 1857 */     name = getFieldNameFromAlias(configLabel, name);
/* 1858 */     expr.appendElements(appendable, name, value);
/*      */   }
/*      */ 
/*      */   public SearchOperatorParsedElements retrieveParsedElements(String opName, String configLabel, String fieldName)
/*      */     throws DataException
/*      */   {
/* 1874 */     if (configLabel == null)
/*      */     {
/* 1876 */       configLabel = this.m_currentCfgLabel;
/*      */     }
/* 1878 */     Map opNameMap = this.m_opNameMap;
/* 1879 */     SearchOperatorParsedElements el = null;
/* 1880 */     String key = opName + "." + fieldName + "." + configLabel;
/* 1881 */     String decOpName = (String)opNameMap.get(key);
/* 1882 */     if (decOpName == null)
/*      */     {
/* 1884 */       opName = opName.toLowerCase();
/* 1885 */       List list = retrieveDecoratorList(opName);
/* 1886 */       String rootOpName = opName;
/* 1887 */       int beginIndex = opName.indexOf(91);
/* 1888 */       if (beginIndex > 0)
/*      */       {
/* 1890 */         rootOpName = opName.substring(0, beginIndex);
/*      */       }
/*      */ 
/* 1893 */       boolean needHexEncoding = false;
/* 1894 */       if (fieldName != null)
/*      */       {
/* 1896 */         needHexEncoding = SearchLoader.isSecurityFieldInEngine(fieldName, configLabel);
/* 1897 */         IndexerCollectionData data = null;
/* 1898 */         String tmpConfigLabel = configLabel;
/* 1899 */         while ((data = SearchLoader.getCurrentIndexDesign(tmpConfigLabel)) == null)
/*      */         {
/* 1901 */           int dotIndex = tmpConfigLabel.lastIndexOf(46);
/* 1902 */           if (dotIndex >= 0)
/*      */           {
/* 1904 */             tmpConfigLabel = tmpConfigLabel.substring(0, dotIndex);
/*      */           }
/*      */           else
/*      */           {
/* 1908 */             Report.debug("search", "unable to find current index design for '" + configLabel + "' while retrieving ", null);
/*      */ 
/* 1910 */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1915 */         if (data != null)
/*      */         {
/* 1917 */           processOperatorDecorators(data, fieldName, list);
/*      */         }
/*      */       }
/* 1920 */       List tmpList = new ArrayList();
/* 1921 */       if (list.contains("zoned"))
/*      */       {
/* 1923 */         tmpList.add("zoned");
/* 1924 */         list.remove("zoned");
/*      */       }
/* 1926 */       if (list.contains("optimized"))
/*      */       {
/* 1928 */         tmpList.add("optimized");
/* 1929 */         list.remove("optimized");
/*      */       }
/*      */ 
/* 1932 */       if (list.size() > 0)
/*      */       {
/* 1934 */         tmpList.addAll(list);
/*      */       }
/* 1936 */       list = tmpList;
/*      */ 
/* 1938 */       LinkedList ll = null;
/* 1939 */       int[] sieves = new int[0];
/* 1940 */       String tmpKey = null;
/*      */       do
/*      */       {
/* 1943 */         ll = new LinkedList(list);
/* 1944 */         for (int i = 0; i < sieves.length; ++i)
/*      */         {
/* 1946 */           ll.remove(list.get(i));
/*      */         }
/*      */ 
/* 1949 */         IdcStringBuilder builder = new IdcStringBuilder();
/* 1950 */         builder.append(rootOpName);
/* 1951 */         boolean isFirst = true;
/* 1952 */         IdcStringBuilder decor = new IdcStringBuilder();
/* 1953 */         for (String dec : ll)
/*      */         {
/* 1955 */           if (dec.equals("optimized"))
/*      */           {
/* 1957 */             needHexEncoding = false;
/*      */           }
/* 1959 */           if (!isFirst)
/*      */           {
/* 1961 */             decor.append(',');
/*      */           }
/*      */ 
/* 1964 */           decor.append(dec);
/*      */         }
/* 1966 */         String decorStr = (decor.length() > 0) ? "[" + decor.toString() + "]" : "";
/* 1967 */         String decorStrWithHexEncoding = decorStr;
/* 1968 */         if (needHexEncoding)
/*      */         {
/* 1970 */           decorStrWithHexEncoding = (decorStr.length() > 0) ? "[" + decor.toString() + ",hexEncoding]" : "[hexEncoding]";
/* 1971 */           tmpKey = builder.toString() + decorStrWithHexEncoding;
/* 1972 */           el = getParsedElements(tmpKey, configLabel);
/*      */         }
/* 1974 */         if (el == null)
/*      */         {
/* 1976 */           tmpKey = builder.toString() + decorStr;
/* 1977 */           el = getParsedElements(tmpKey, configLabel);
/* 1978 */           if ((needHexEncoding) && (el != null))
/*      */           {
/* 1980 */             tmpKey = builder.toString() + decorStrWithHexEncoding;
/* 1981 */             SearchOperatorParsedElements sope = new SearchOperatorParsedElements();
/* 1982 */             sope.copy(el);
/* 1983 */             el = sope;
/*      */ 
/* 1986 */             for (SearchOperatorParsedElement element : el.m_queryElements)
/*      */             {
/* 1988 */               if (element.m_parsedElementType == 2)
/*      */               {
/* 1991 */                 addSearchOperatorParsedElementActionIfNotExist(element, SearchOperatorParsedElements.Action.HEX_ENCODE);
/*      */               }
/* 1994 */               else if (element.m_parsedElementType == 1)
/*      */               {
/* 1997 */                 addSearchOperatorParsedElementActionIfNotExist(element, SearchOperatorParsedElements.Action.PREFIX);
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/* 2004 */             String setPrefix = getEngineValue(configLabel, "SetPrefixForHexEncodedField");
/* 2005 */             if ((sope.m_prefix == null) && (StringUtils.convertToBool(setPrefix, false)))
/*      */             {
/* 2007 */               sope.m_prefix = "z";
/*      */             }
/*      */             else
/*      */             {
/* 2011 */               String appendPrefix = getEngineValue(configLabel, "AppendPrefixForHexEncodedField");
/* 2012 */               if (StringUtils.convertToBool(appendPrefix, false))
/*      */               {
/* 2014 */                 sope.m_prefix += "z";
/*      */               }
/*      */               else
/*      */               {
/* 2018 */                 String replacePrefix = getEngineValue(configLabel, "AllowHexEncodedFieldReplaceFieldPrefix");
/* 2019 */                 if (StringUtils.convertToBool(replacePrefix, false))
/*      */                 {
/* 2021 */                   sope.m_prefix = "z";
/*      */                 }
/*      */               }
/*      */             }
/* 2025 */             putParsedElements(tmpKey, configLabel, el);
/*      */           }
/*      */         }
/* 2028 */         if (el != null)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/* 2033 */         if ((sieves.length == 0) || (sieves[(sieves.length - 1)] == sieves.length - 1))
/*      */         {
/* 2035 */           sieves = new int[sieves.length + 1];
/* 2036 */           for (int i = sieves.length; i > 0; --i)
/*      */           {
/* 2038 */             sieves[(i - 1)] = (list.size() - (sieves.length - i) - 1);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 2043 */           for (int i = sieves.length - 1; i >= 0; --i)
/*      */           {
/* 2045 */             if (i == 0)
/*      */             {
/* 2047 */               sieves[i] -= 1;
/* 2048 */               if (sieves[i] >= 0) {
/*      */                 continue;
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/* 2054 */               if (sieves[i] == sieves[(i - 1)] + 1) {
/*      */                 continue;
/*      */               }
/*      */ 
/* 2058 */               sieves[i] -= 1;
/* 2059 */               for (int j = i; j < sieves.length; ++j)
/*      */               {
/* 2061 */                 sieves[j] = (list.size() - (sieves.length - j) - 2);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 2065 */       while (sieves.length <= list.size());
/* 2066 */       if (el != null)
/*      */       {
/* 2068 */         decOpName = tmpKey;
/* 2069 */         opNameMap.put(key, tmpKey);
/*      */       }
/*      */       else
/*      */       {
/* 2073 */         decOpName = rootOpName;
/*      */       }
/*      */     }
/* 2076 */     if (el == null)
/*      */     {
/* 2078 */       el = getParsedElements(decOpName, configLabel);
/*      */     }
/*      */ 
/* 2081 */     if (el == null)
/*      */     {
/* 2083 */       String msg = LocaleUtils.encodeMessage("csUnableToFindParsedElements", null, configLabel, fieldName, opName);
/* 2084 */       throw new DataException(msg);
/*      */     }
/* 2086 */     return el;
/*      */   }
/*      */ 
/*      */   public void processOperatorDecorators(IndexerCollectionData data, String fieldName, List<String> list)
/*      */   {
/* 2097 */     if ((data == null) || (list == null))
/*      */     {
/* 2099 */       return;
/*      */     }
/*      */ 
/* 2102 */     Map map = (Map)data.m_fieldDesignMap.get(fieldName);
/* 2103 */     if (map == null)
/*      */       return;
/* 2105 */     String zoned = (String)map.get("isZoned");
/* 2106 */     boolean isZoned = StringUtils.convertToBool(zoned, false);
/* 2107 */     if ((isZoned) && (!list.contains("zoned")))
/*      */     {
/* 2109 */       list.add("zoned");
/*      */     }
/*      */ 
/* 2112 */     String optimized = (String)map.get("isOptimized");
/* 2113 */     boolean isOptimized = StringUtils.convertToBool(optimized, false);
/* 2114 */     if ((isOptimized) && (!list.contains("optimized")))
/*      */     {
/* 2116 */       list.add("optimized");
/*      */     }
/*      */ 
/* 2119 */     String caseInsensitive = (String)map.get("isCaseInsensitive");
/* 2120 */     boolean isCaseInsensitive = StringUtils.convertToBool(caseInsensitive, false);
/* 2121 */     if ((!isCaseInsensitive) || (list.contains("caseInsensitive")))
/*      */       return;
/* 2123 */     list.add("caseInsensitive");
/*      */   }
/*      */ 
/*      */   protected boolean addSearchOperatorParsedElementActionIfNotExist(SearchOperatorParsedElement element, SearchOperatorParsedElements.Action newAction)
/*      */   {
/* 2132 */     boolean hasAction = false;
/* 2133 */     if (element.m_actions != null)
/*      */     {
/* 2135 */       for (SearchOperatorParsedElements.Action action : element.m_actions)
/*      */       {
/* 2137 */         if (action.equals(newAction))
/*      */         {
/* 2139 */           hasAction = true;
/* 2140 */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 2146 */       element.m_actions = new ArrayList();
/*      */     }
/* 2148 */     if (!hasAction)
/*      */     {
/* 2150 */       element.m_actions.add(0, newAction);
/*      */     }
/* 2152 */     return !hasAction;
/*      */   }
/*      */ 
/*      */   public List<String> retrieveDecoratorList(String opName)
/*      */   {
/* 2157 */     int beginIndex = opName.indexOf(91);
/* 2158 */     int endIndex = opName.indexOf(93);
/* 2159 */     String decorators = null;
/* 2160 */     if ((beginIndex > 0) && (endIndex > beginIndex))
/*      */     {
/* 2162 */       decorators = opName.substring(beginIndex + 1, endIndex);
/*      */     }
/* 2164 */     return StringUtils.makeListFromSequence(decorators, ',', ',', 0);
/*      */   }
/*      */ 
/*      */   public String retrieveBaseOperator(String opName)
/*      */   {
/* 2169 */     int beginIndex = opName.indexOf(91);
/* 2170 */     int endIndex = opName.indexOf(93);
/* 2171 */     String baseOperator = opName;
/* 2172 */     if ((beginIndex > 0) && (endIndex > beginIndex))
/*      */     {
/* 2174 */       baseOperator = opName.substring(0, beginIndex);
/*      */     }
/* 2176 */     return baseOperator;
/*      */   }
/*      */ 
/*      */   public List<String> retrieveDecoratorList(SearchOperatorParsedElements el)
/*      */   {
/* 2181 */     String operatorWithDecorators = el.m_operator;
/* 2182 */     List decorators = null;
/*      */ 
/* 2184 */     if (operatorWithDecorators != null)
/*      */     {
/* 2186 */       decorators = retrieveDecoratorList(operatorWithDecorators);
/*      */     }
/*      */ 
/* 2189 */     return decorators;
/*      */   }
/*      */ 
/*      */   public boolean isReservedProcSupportedWithDecorators(String engineName, List<String> decorators)
/*      */   {
/* 2203 */     if ((decorators == null) || (decorators.size() == 0))
/*      */     {
/* 2205 */       return true;
/*      */     }
/*      */ 
/* 2208 */     boolean reservedProcSupport = false;
/*      */ 
/* 2210 */     List reservedProcessDecoratorsList = getReservedProcessDecorators(engineName);
/*      */ 
/* 2212 */     if ((reservedProcessDecoratorsList != null) && (reservedProcessDecoratorsList.size() > 0))
/*      */     {
/* 2214 */       boolean allDecoratorsSupportProc = false;
/* 2215 */       for (int decNo = 0; decNo < decorators.size(); ++decNo)
/*      */       {
/* 2217 */         String decorator = (String)decorators.get(decNo);
/* 2218 */         allDecoratorsSupportProc = reservedProcessDecoratorsList.contains(decorator);
/* 2219 */         if (!allDecoratorsSupportProc) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 2225 */       reservedProcSupport = allDecoratorsSupportProc;
/*      */     }
/*      */ 
/* 2228 */     return reservedProcSupport;
/*      */   }
/*      */ 
/*      */   public boolean isPrefixOperator(String engineName, String operator)
/*      */   {
/* 2233 */     return isOperatorOfCatergory(engineName, operator, "SearchPrefixOperators");
/*      */   }
/*      */ 
/*      */   public boolean isSuffixOperator(String engineName, String operator)
/*      */   {
/* 2238 */     return isOperatorOfCatergory(engineName, operator, "SearchSuffixOperators");
/*      */   }
/*      */ 
/*      */   public boolean isOperatorOfCatergory(String engineName, String operator, String category)
/*      */   {
/* 2254 */     if (this.m_operatorCategories == null)
/*      */     {
/* 2256 */       return false;
/*      */     }
/*      */ 
/* 2259 */     boolean isOpInCategory = false;
/*      */ 
/* 2261 */     if (engineName == null)
/*      */     {
/* 2263 */       engineName = getCurrentEngineName();
/*      */     }
/* 2265 */     Map operatorCategoriesForEngine = (Map)getPropertiesForEngine(this.m_operatorCategories, engineName);
/*      */ 
/* 2267 */     if (operatorCategoriesForEngine != null)
/*      */     {
/* 2269 */       List prefixOperators = (List)operatorCategoriesForEngine.get(category);
/* 2270 */       isOpInCategory = prefixOperators.contains(operator);
/*      */     }
/*      */ 
/* 2273 */     return isOpInCategory;
/*      */   }
/*      */ 
/*      */   public void resetOpNameMap()
/*      */   {
/* 2278 */     this.m_opNameMap = new ConcurrentHashMap();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String[] getQuery(String opName, String configLabel)
/*      */     throws DataException
/*      */   {
/* 2291 */     SearchOperatorParsedElements query = getParsedElements(opName, configLabel);
/*      */ 
/* 2293 */     String[] querySegments = new String[query.m_queryElements.size()];
/* 2294 */     for (int i = 0; i < querySegments.length; ++i)
/*      */     {
/* 2296 */       querySegments[i] = ((SearchOperatorParsedElement)query.m_queryElements.get(i)).m_parsedElement;
/*      */     }
/* 2298 */     return querySegments;
/*      */   }
/*      */ 
/*      */   public SearchOperatorParsedElements getParsedElements(String opName, String configLabel)
/*      */     throws DataException
/*      */   {
/* 2304 */     if (configLabel == null)
/*      */     {
/* 2306 */       configLabel = this.m_currentCfgLabel;
/*      */     }
/* 2308 */     Map queriesHash = (Map)this.m_parsedQueries.get(configLabel);
/* 2309 */     if (queriesHash == null)
/*      */     {
/* 2311 */       throw new DataException(LocaleUtils.encodeMessage("csSearchQueryConfigNotDefined", null, configLabel));
/*      */     }
/*      */ 
/* 2314 */     return (SearchOperatorParsedElements)queriesHash.get(opName.toLowerCase());
/*      */   }
/*      */ 
/*      */   public SearchOperatorParsedElements putParsedElements(String opName, String configLabel, SearchOperatorParsedElements elements)
/*      */     throws DataException
/*      */   {
/* 2321 */     if (configLabel == null)
/*      */     {
/* 2323 */       configLabel = this.m_currentCfgLabel;
/*      */     }
/* 2325 */     Map queriesHash = (Map)this.m_parsedQueries.get(configLabel);
/* 2326 */     if (queriesHash == null)
/*      */     {
/* 2328 */       throw new DataException(LocaleUtils.encodeMessage("csSearchQueryConfigNotDefined", null, configLabel));
/*      */     }
/*      */ 
/* 2331 */     return (SearchOperatorParsedElements)queriesHash.put(opName.toLowerCase(), elements);
/*      */   }
/*      */ 
/*      */   public String createQueryForPrimaryKeys(String configLabel, String[] primaryVals)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2347 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 2348 */     appendQueryForPrimaryKeys(buf, configLabel, primaryVals);
/* 2349 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   public void appendQueryForPrimaryKeys(IdcAppendable appendable, String configLabel, String[] primaryVals)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2363 */     String searchPrimaryKey = getEngineValue("SearchPrimaryKey");
/* 2364 */     if ((searchPrimaryKey == null) || (searchPrimaryKey.length() == 0))
/*      */     {
/* 2366 */       searchPrimaryKey = getFieldNameFromAlias(configLabel, "dDocName");
/*      */     }
/* 2368 */     String primaryKeyEncoding = getEngineValue("SearchPrimaryKeyEncoding");
/* 2369 */     boolean isHex = (primaryKeyEncoding != null) && (primaryKeyEncoding.equalsIgnoreCase("hex"));
/* 2370 */     SearchOperatorParsedElements equalExpr = getParsedElements("equals", configLabel);
/* 2371 */     SearchOperatorParsedElements orExpr = getParsedElements("or", configLabel);
/* 2372 */     IdcStringBuilder buf = null;
/* 2373 */     for (int i = 0; i < primaryVals.length; ++i)
/*      */     {
/* 2375 */       String primaryValOriginal = primaryVals[i];
/* 2376 */       String primaryVal = SearchLoader.prepareSearchIndexKey(primaryValOriginal, primaryKeyEncoding, this, 1);
/*      */ 
/* 2379 */       if (i > 0)
/*      */       {
/* 2381 */         appendable.append(' ');
/* 2382 */         orExpr.appendElements(appendable, null, null);
/*      */       }
/* 2384 */       CharSequence val = primaryVal;
/* 2385 */       if (isHex)
/*      */       {
/* 2387 */         if (buf == null)
/*      */         {
/* 2389 */           buf = new IdcStringBuilder(primaryVal.length() * 3 + 10);
/*      */         }
/*      */         else
/*      */         {
/* 2393 */           buf.setLength(0);
/*      */         }
/* 2395 */         buf.append('z');
/*      */         try
/*      */         {
/* 2398 */           StringUtils.appendAsHex(buf, primaryVal);
/*      */         }
/*      */         catch (UnsupportedEncodingException ignore)
/*      */         {
/*      */         }
/*      */ 
/* 2404 */         val = buf;
/*      */       }
/* 2406 */       equalExpr.appendElements(appendable, searchPrimaryKey, val);
/*      */     }
/* 2408 */     if (buf == null)
/*      */       return;
/* 2410 */     buf.releaseBuffers();
/*      */   }
/*      */ 
/*      */   public void appendQueryTextFilters(DataBinder binder, String filterStr)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2416 */     CommonSearchConfigCompanion companion = getCompanion();
/* 2417 */     String queryText = binder.getLocal("QueryText").trim();
/*      */ 
/* 2419 */     boolean doSafeParenthesesEnclosure = StringUtils.convertToBool(getEngineValue("EncloseQueryWithSafeParentheses"), true);
/*      */ 
/* 2421 */     if (doSafeParenthesesEnclosure)
/*      */     {
/* 2423 */       queryText = QueryUtils.enclosingQueryWithSafeParenthesis(queryText);
/*      */     }
/*      */ 
/* 2426 */     binder.putLocal("QueryText", queryText);
/*      */ 
/* 2429 */     binder.putLocal("ParsedQueryTextNoSecurity", queryText);
/*      */ 
/* 2431 */     int val = companion.appendQueryTextFilters(binder, filterStr);
/* 2432 */     if (val != 0)
/*      */     {
/* 2434 */       return;
/*      */     }
/*      */ 
/* 2437 */     Vector filters = new IdcVector();
/* 2438 */     if ((filterStr != null) && (filterStr.length() != 0))
/*      */     {
/* 2440 */       filters = StringUtils.parseArray(filterStr, ',', ',');
/*      */     }
/*      */ 
/* 2443 */     IdcStringBuilder buffer = new IdcStringBuilder();
/* 2444 */     int len = filters.size();
/* 2445 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2447 */       String key = (String)filters.elementAt(i);
/* 2448 */       String value = binder.getLocal(key);
/* 2449 */       appendQueryTextFilter(buffer, value);
/*      */     }
/* 2451 */     if (buffer.length() != 0)
/*      */     {
/*      */       String result;
/*      */       try
/*      */       {
/* 2456 */         DynamicHtmlMerger dm = this.m_dhtml;
/* 2457 */         ExecutionContext ctxt = new ExecutionContextAdaptor();
/* 2458 */         ctxt.setCachedObject("DataBinder", binder);
/* 2459 */         dm.init(ctxt);
/* 2460 */         result = dm.evaluateScript(buffer.toString());
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2464 */         String msg = LocaleUtils.encodeMessage("csSearchQueryErrorEvaluatingFilter", e.getMessage(), buffer.toString());
/* 2465 */         throw new DataException(msg);
/*      */       }
/* 2467 */       if ((result != null) && (result.length() != 0))
/*      */       {
/* 2469 */         if ((queryText != null) && (queryText.length() != 0))
/*      */         {
/* 2471 */           queryText = "(" + queryText + ") " + parseElement("AND", null, null) + " (" + result + ")";
/*      */         }
/*      */         else
/*      */         {
/* 2476 */           queryText = result;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2481 */     binder.putLocal("QueryText", queryText);
/*      */   }
/*      */ 
/*      */   public void appendQueryTextFilter(IdcStringBuilder buffer, String filter) throws DataException
/*      */   {
/* 2486 */     CommonSearchConfigCompanion companion = getCompanion();
/* 2487 */     int val = companion.appendQueryTextFilter(buffer, filter);
/* 2488 */     if (val != 0)
/*      */     {
/* 2490 */       return;
/*      */     }
/* 2492 */     if ((filter == null) || (filter.length() == 0))
/*      */       return;
/* 2494 */     if (buffer.length() != 0)
/*      */     {
/* 2496 */       buffer.insert(0, '(');
/* 2497 */       buffer.append(')');
/* 2498 */       buffer.append(' ');
/* 2499 */       appendClauseElement(buffer, "AND", null, null, null);
/* 2500 */       buffer.append(' ');
/*      */     }
/*      */ 
/* 2503 */     buffer.append('(');
/* 2504 */     buffer.append(filter);
/* 2505 */     buffer.append(')');
/*      */   }
/*      */ 
/*      */   public void constructFullQuery(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException
/*      */   {
/* 2511 */     String[] queryFrag = { "QuerySelection", "QueryCollection", "WhereClause", "QueryAssembly" };
/*      */ 
/* 2514 */     for (int i = 0; i < queryFrag.length; ++i)
/*      */     {
/* 2516 */       constructQueryFrag(queryFrag[i], binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void constructQueryFrag(String name, DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 2523 */     DynamicHtml dhtml = getPage(name);
/*      */ 
/* 2528 */     if (((!name.equals("WhereClause")) && (binder.getLocal(name) != null)) || (dhtml == null))
/*      */     {
/* 2530 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2535 */       DynamicHtmlMerger dm = this.m_dhtml;
/* 2536 */       ExecutionContext ctxt = new ExecutionContextAdaptor();
/* 2537 */       ctxt.setCachedObject("DataBinder", binder);
/* 2538 */       dm.init(ctxt);
/* 2539 */       String result = dm.executeDynamicHtml(dhtml);
/* 2540 */       if (result != null)
/*      */       {
/* 2542 */         binder.putLocal(name, result);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2547 */       throw new DataException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DynamicHtmlMerger getDynamicHtmlMerger(ExecutionContext ctxt) throws DataException
/*      */   {
/* 2553 */     DynamicHtmlMerger pm = (DynamicHtmlMerger)ctxt.getCachedObject("PageMerger");
/* 2554 */     if (pm == null)
/*      */     {
/* 2556 */       createDataException(null, "csSearchPageMergerNotAvailable");
/*      */     }
/* 2558 */     return pm;
/*      */   }
/*      */ 
/*      */   public DynamicHtml getPage(String name)
/*      */   {
/* 2563 */     return getPage(name, this.m_currentCfgLabel);
/*      */   }
/*      */ 
/*      */   public DynamicHtml getPage(String name, String engineName)
/*      */   {
/* 2568 */     Properties engine = (Properties)this.m_queryConfigs.get(engineName);
/* 2569 */     DynamicHtml page = (DynamicHtml)engine.get(name + "_PAGE");
/* 2570 */     return page;
/*      */   }
/*      */ 
/*      */   protected void createDataException(Exception e, String msg) throws DataException
/*      */   {
/* 2575 */     String priorMsg = null;
/* 2576 */     if (e != null)
/*      */     {
/* 2578 */       priorMsg = e.getMessage();
/*      */     }
/* 2580 */     throw new DataException(LocaleUtils.encodeMessage(msg, priorMsg));
/*      */   }
/*      */ 
/*      */   public synchronized String getStringFromTable(String tableKey, String srcCol, String key, String col)
/*      */   {
/* 2585 */     DataResultSet drset = (DataResultSet)this.m_configs.get(tableKey);
/* 2586 */     return getStringFromTable(drset, srcCol, key, col);
/*      */   }
/*      */ 
/*      */   public synchronized String getStringFromTableWithInheritance(String tableKey, String srcCol, String key, String col)
/*      */   {
/* 2591 */     if (key == null)
/*      */     {
/* 2593 */       key = this.m_currentCfgLabel;
/*      */     }
/* 2595 */     DataResultSet drset = (DataResultSet)this.m_configs.get(tableKey);
/* 2596 */     String value = getStringFromTable(drset, srcCol, key, col);
/*      */ 
/* 2598 */     if (value != null)
/*      */     {
/* 2600 */       return value;
/*      */     }
/*      */ 
/* 2603 */     int index = key.lastIndexOf(".");
/* 2604 */     while (index >= 0)
/*      */     {
/* 2606 */       key = key.substring(0, index);
/* 2607 */       value = getStringFromTable(drset, srcCol, key, col);
/* 2608 */       if (value != null)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 2614 */       index = key.lastIndexOf(".");
/*      */     }
/*      */ 
/* 2617 */     return value;
/*      */   }
/*      */ 
/*      */   public String getStringFromTable(DataResultSet drset, String srCol, String key, String col)
/*      */   {
/* 2622 */     if (drset == null)
/*      */     {
/* 2624 */       return null;
/*      */     }
/*      */     Object Ljava/lang/Object;;
/* 2626 */     monitorenter;
/*      */     try
/*      */     {
/* 2630 */       return ResultSetUtils.findValue(drset, srCol, key, col);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 2634 */       return null;
/*      */     } finally {
/* 2636 */       monitorexit;
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getFieldNameFromAlias(String alias) {
/* 2641 */     return getFieldNameFromAlias(null, alias);
/*      */   }
/*      */ 
/*      */   public String getFieldNameFromAlias(String engineName, String alias)
/*      */   {
/* 2646 */     if (alias == null)
/*      */     {
/* 2648 */       return null;
/*      */     }
/* 2650 */     if (engineName == null)
/*      */     {
/* 2652 */       engineName = this.m_currentCfgLabel;
/*      */     }
/*      */ 
/* 2655 */     Properties prop = getCurrentConfig(this.m_searchFieldAliasName, engineName, true);
/* 2656 */     String name = alias;
/* 2657 */     if (prop != null)
/*      */     {
/* 2659 */       name = prop.getProperty(alias);
/* 2660 */       if (name == null)
/*      */       {
/* 2662 */         name = alias;
/*      */       }
/*      */     }
/* 2665 */     return name;
/*      */   }
/*      */ 
/*      */   public CommonSearchConfig shallowClone()
/*      */   {
/* 2670 */     CommonSearchConfig config = new CommonSearchConfig();
/* 2671 */     config.m_configs = this.m_configs;
/* 2672 */     config.m_parsedQueries = this.m_parsedQueries;
/* 2673 */     config.m_queryConfigs = this.m_queryConfigs;
/* 2674 */     config.m_whereClauseDefs = this.m_whereClauseDefs;
/* 2675 */     config.m_engineRules = this.m_engineRules;
/* 2676 */     config.m_engineConfig = this.m_engineConfig;
/* 2677 */     config.m_queryFieldConfigs = this.m_queryFieldConfigs;
/* 2678 */     config.m_searchFieldAliasName = this.m_searchFieldAliasName;
/* 2679 */     config.m_currentCfgLabel = this.m_currentCfgLabel;
/* 2680 */     config.m_currentSearchEngineConfig = this.m_currentSearchEngineConfig;
/* 2681 */     config.m_operatorTypes = this.m_operatorTypes;
/* 2682 */     m_maxAppResults = m_maxAppResults;
/* 2683 */     config.m_opNameMap = this.m_opNameMap;
/* 2684 */     config.m_reservedRules = this.m_reservedRules;
/* 2685 */     config.m_operatorCategories = this.m_operatorCategories;
/*      */     try
/*      */     {
/* 2690 */       Class c = this.m_dhtml.getClass();
/* 2691 */       config.m_dhtml = ((DynamicHtmlMerger)c.newInstance());
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2696 */       Report.error(null, "csSearchUnableCreateNewPageMerger", e);
/*      */     }
/*      */ 
/* 2699 */     return config;
/*      */   }
/*      */ 
/*      */   public void clear()
/*      */   {
/* 2708 */     if (this.m_dhtml == null)
/*      */       return;
/* 2710 */     this.m_dhtml.releaseAllTemporary();
/*      */   }
/*      */ 
/*      */   public CommonSearchConfigCompanion getCompanion()
/*      */     throws DataException
/*      */   {
/* 2716 */     CommonSearchConfigCompanion cscc = (CommonSearchConfigCompanion)this.m_configCompanions.get(this.m_currentCfgLabel);
/*      */ 
/* 2718 */     if (cscc == null)
/*      */     {
/* 2720 */       String className = getStringFromTableWithInheritance("SearchEngineClasses", "SearchEngineName", this.m_currentCfgLabel, "SearchConfigCompanion");
/*      */       try
/*      */       {
/* 2725 */         cscc = (CommonSearchConfigCompanion)ComponentClassFactory.createClassInstance("CommonSearchConfigCompanion", className, "");
/*      */ 
/* 2727 */         cscc.init(this);
/* 2728 */         this.m_configCompanions.put(this.m_currentCfgLabel, cscc);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 2732 */         createDataException(e, "csSearchUnableCreateSearchCfgCompanion");
/*      */       }
/*      */     }
/*      */ 
/* 2736 */     return cscc;
/*      */   }
/*      */ 
/*      */   public String getCurrentEngineName()
/*      */   {
/* 2741 */     return this.m_currentCfgLabel;
/*      */   }
/*      */ 
/*      */   public String getEvaluatedEngineValue(String key, ExecutionContext ctxt) throws DataException
/*      */   {
/* 2746 */     return getEvaluatedEngineValue(null, key, ctxt);
/*      */   }
/*      */ 
/*      */   public String getEvaluatedEngineValue(String engineName, String key, ExecutionContext ctxt) throws DataException
/*      */   {
/*      */     try
/*      */     {
/* 2753 */       String value = getEngineValue(engineName, key);
/* 2754 */       if (value != null)
/*      */       {
/* 2756 */         ExecutionContextAdaptor ec = new ExecutionContextAdaptor();
/* 2757 */         ec.setParentContext(ctxt);
/* 2758 */         DynamicHtmlMerger dhtml = this.m_dhtml;
/* 2759 */         dhtml.init(ec);
/* 2760 */         value = dhtml.evaluateScript(value);
/*      */       }
/* 2762 */       return value;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2766 */       String msg = LocaleUtils.encodeMessage("csSearchUnableEvaluatedSearchEngineValue", null, key);
/* 2767 */       createDataException(e, msg);
/*      */     }
/*      */ 
/* 2770 */     return null;
/*      */   }
/*      */ 
/*      */   public static String getGlobalEngineValue(String key)
/*      */   {
/* 2775 */     CommonSearchConfig cfg = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/* 2776 */     return cfg.getEngineValue(key);
/*      */   }
/*      */ 
/*      */   public String getEngineValue(String key)
/*      */   {
/* 2781 */     return getEngineValue(null, key);
/*      */   }
/*      */ 
/*      */   public String getEngineValue(String engineName, String key)
/*      */   {
/* 2786 */     return getEngineValue(engineName, key, false, ',');
/*      */   }
/*      */ 
/*      */   public String getEngineValue(String engineName, String key, boolean isAppendEnvironmentValue, char appendChar)
/*      */   {
/* 2792 */     if ((engineName == null) || (engineName.length() == 0))
/*      */     {
/* 2794 */       engineName = this.m_currentCfgLabel;
/*      */     }
/*      */ 
/* 2797 */     IdcStringBuilder keyWithEngineName = new IdcStringBuilder();
/* 2798 */     keyWithEngineName.append("(");
/* 2799 */     keyWithEngineName.append(engineName);
/* 2800 */     keyWithEngineName.append(")");
/* 2801 */     keyWithEngineName.append(key);
/*      */ 
/* 2803 */     String envValue = SharedObjects.getEnvironmentValue(keyWithEngineName.toString());
/* 2804 */     if (envValue == null)
/*      */     {
/* 2806 */       envValue = SharedObjects.getEnvironmentValue(key);
/*      */     }
/*      */ 
/* 2809 */     String engineValue = null;
/* 2810 */     Properties prop = (Properties)this.m_engineRules.get(engineName);
/* 2811 */     if (prop != null)
/*      */     {
/* 2813 */       engineValue = prop.getProperty(key);
/*      */     }
/* 2815 */     if (engineValue == null)
/*      */     {
/* 2818 */       engineValue = getInheritedEngineValue(engineName, key);
/*      */     }
/* 2820 */     if (engineValue == null)
/*      */     {
/* 2822 */       prop = (Properties)this.m_engineRules.get("COMMON");
/* 2823 */       engineValue = (String)prop.get(key);
/*      */     }
/*      */ 
/* 2826 */     String value = null;
/* 2827 */     if (isAppendEnvironmentValue)
/*      */     {
/* 2829 */       if (engineValue == null)
/*      */       {
/* 2831 */         value = envValue;
/*      */       }
/* 2833 */       else if (envValue == null)
/*      */       {
/* 2835 */         value = engineValue;
/*      */       }
/*      */       else
/*      */       {
/* 2839 */         value = engineValue + appendChar + envValue;
/*      */       }
/*      */ 
/*      */     }
/* 2844 */     else if (envValue != null)
/*      */     {
/* 2846 */       value = envValue;
/*      */     }
/*      */     else
/*      */     {
/* 2850 */       value = engineValue;
/*      */     }
/*      */ 
/* 2854 */     return value;
/*      */   }
/*      */ 
/*      */   public String getInheritedEngineValue(String engineName, String key)
/*      */   {
/* 2859 */     String value = null;
/*      */ 
/* 2861 */     int index = engineName.lastIndexOf(".");
/* 2862 */     while (index >= 0)
/*      */     {
/* 2864 */       engineName = engineName.substring(0, index);
/* 2865 */       Properties prop = (Properties)this.m_engineRules.get(engineName);
/* 2866 */       if (prop != null)
/*      */       {
/* 2868 */         value = prop.getProperty(key);
/*      */       }
/* 2870 */       if (value != null)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 2875 */       index = engineName.lastIndexOf(".");
/*      */     }
/*      */ 
/* 2878 */     return value;
/*      */   }
/*      */ 
/*      */   public Map getEngineRules()
/*      */   {
/* 2883 */     return getEngineRules(this.m_currentCfgLabel);
/*      */   }
/*      */ 
/*      */   public Map getEngineRules(String key)
/*      */   {
/* 2888 */     if ((key == null) || (key.length() == 0))
/*      */     {
/* 2890 */       key = this.m_currentCfgLabel;
/*      */     }
/*      */ 
/* 2893 */     Map engineCfg = new HashMap();
/* 2894 */     Hashtable curCfg = (Hashtable)this.m_engineRules.get(key);
/* 2895 */     if (curCfg != null)
/*      */     {
/* 2897 */       Hashtable defaultCfg = (Hashtable)this.m_engineRules.get("COMMON");
/* 2898 */       if (defaultCfg != null)
/*      */       {
/* 2900 */         DataBinder.mergeHashTables(engineCfg, defaultCfg);
/*      */       }
/* 2902 */       DataBinder.mergeHashTables(engineCfg, curCfg);
/*      */     }
/* 2904 */     return engineCfg;
/*      */   }
/*      */ 
/*      */   public CommonSearchEngineConfig retrieveEngineConfig(String key)
/*      */   {
/* 2909 */     if ((key == null) || (key.length() == 0))
/*      */     {
/* 2911 */       key = this.m_currentCfgLabel;
/*      */     }
/* 2913 */     CommonSearchEngineConfig csec = (CommonSearchEngineConfig)this.m_engineConfig.get(key);
/* 2914 */     if (csec == null)
/*      */     {
/* 2916 */       csec = prepareSearchEngineConfig(key);
/*      */     }
/* 2918 */     return csec;
/*      */   }
/*      */ 
/*      */   public CommonSearchEngineConfig getCurrentEngineConfig()
/*      */   {
/* 2923 */     return this.m_currentSearchEngineConfig;
/*      */   }
/*      */ 
/*      */   public String retrieveEnabledFieldsForProp(IndexerCollectionData collectionDesignDef, String engineValueKey, String propertyKey, String fetchRestrictKey, String validFieldsKey)
/*      */   {
/* 2943 */     String enabledFields = "";
/* 2944 */     String curEngine = getCurrentEngineName();
/*      */ 
/* 2946 */     if ((engineValueKey != null) && (engineValueKey.length() > 0))
/*      */     {
/* 2948 */       enabledFields = getEngineValue(curEngine, engineValueKey);
/*      */     }
/*      */ 
/* 2951 */     if (collectionDesignDef == null)
/*      */     {
/* 2953 */       return enabledFields;
/*      */     }
/*      */ 
/* 2956 */     String originalEnabledFields = enabledFields;
/* 2957 */     DataBinder searchDesignBinder = collectionDesignDef.m_binder;
/*      */ 
/* 2959 */     boolean fetchRestrict = false;
/* 2960 */     if (fetchRestrictKey != null)
/*      */     {
/* 2962 */       fetchRestrict = DataBinderUtils.getBoolean(searchDesignBinder, fetchRestrictKey, false);
/*      */     }
/*      */ 
/* 2965 */     if (fetchRestrict == true)
/*      */     {
/* 2967 */       if ((validFieldsKey != null) && (validFieldsKey.length() > 0))
/*      */       {
/* 2969 */         String validFields = searchDesignBinder.getAllowMissing(validFieldsKey);
/* 2970 */         if ((validFields != null) && (validFields.length() > 0))
/*      */         {
/* 2972 */           enabledFields = validFields;
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2978 */       enabledFields = "";
/*      */ 
/* 2980 */       Enumeration fields = collectionDesignDef.m_fieldDesignMap.keys();
/* 2981 */       boolean isFirstField = true;
/* 2982 */       while (fields.hasMoreElements())
/*      */       {
/* 2984 */         String fieldName = (String)fields.nextElement();
/* 2985 */         Properties fieldProperties = (Properties)collectionDesignDef.m_fieldDesignMap.get(fieldName);
/*      */ 
/* 2987 */         String propertyValueString = fieldProperties.getProperty(propertyKey, "false");
/* 2988 */         boolean propertyValue = StringUtils.convertToBool(propertyValueString, false);
/*      */ 
/* 2990 */         if (propertyValue == true)
/*      */         {
/* 2992 */           if (isFirstField)
/*      */           {
/* 2994 */             enabledFields = enabledFields + fieldName;
/* 2995 */             isFirstField = false;
/*      */           }
/*      */           else
/*      */           {
/* 2999 */             enabledFields = enabledFields + "," + fieldName;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 3005 */     if ((enabledFields == null) || (enabledFields.length() == 0))
/*      */     {
/* 3007 */       enabledFields = originalEnabledFields;
/*      */     }
/*      */ 
/* 3010 */     return enabledFields;
/*      */   }
/*      */ 
/*      */   public void updateSearchSortFields(Map<String, Properties> containers)
/*      */     throws DataException
/*      */   {
/* 3016 */     if (containers == null)
/*      */     {
/* 3018 */       return;
/*      */     }
/*      */ 
/* 3021 */     prepareClientSortFields(containers);
/*      */   }
/*      */ 
/*      */   public void updateUniversalSortFields(Map<String, Properties> containers)
/*      */   {
/* 3032 */     if (containers == null)
/*      */     {
/* 3034 */       return;
/*      */     }
/*      */ 
/* 3037 */     Properties universalContainer = (Properties)containers.get("UNIVERSAL");
/*      */ 
/* 3039 */     Properties currentEngineContainer = (Properties)containers.get(getCurrentEngineName());
/* 3040 */     DataResultSet currentEngineSortFields = (DataResultSet)currentEngineContainer.get("SearchSortFields");
/*      */ 
/* 3042 */     DataResultSet copySortFields = new DataResultSet();
/* 3043 */     copySortFields.copy(currentEngineSortFields);
/* 3044 */     universalContainer.put("SearchSortFields", copySortFields);
/*      */   }
/*      */ 
/*      */   public boolean updateSearchFieldInfoUsingSearchDesign(boolean supportSearchDesignSortFieldsUpdate, IndexerCollectionData collectionDesignDef, String fieldsToUpdate)
/*      */   {
/* 3060 */     boolean isSuccessfulUpdate = false;
/*      */ 
/* 3062 */     if (supportSearchDesignSortFieldsUpdate == true)
/*      */     {
/* 3064 */       isSuccessfulUpdate = updateSearchFieldInfoEx(collectionDesignDef, fieldsToUpdate);
/*      */     }
/*      */ 
/* 3067 */     return isSuccessfulUpdate;
/*      */   }
/*      */ 
/*      */   public boolean updateSearchFieldInfoEx(IndexerCollectionData collectionDesignDef, String fieldsToUpdate)
/*      */   {
/* 3082 */     if (collectionDesignDef == null)
/*      */     {
/* 3084 */       return false;
/*      */     }
/*      */ 
/* 3087 */     DataResultSet searchFields = (DataResultSet)this.m_configs.get("SearchFieldInfo");
/* 3088 */     String currentEngineLabel = "(" + getCurrentEngineName() + ")";
/* 3089 */     String validSortFields = "";
/* 3090 */     DataBinder searchDesignBinder = collectionDesignDef.m_binder;
/*      */ 
/* 3092 */     List fieldsToUpdateList = null;
/* 3093 */     if ((fieldsToUpdate != null) && (fieldsToUpdate.length() > 0))
/*      */     {
/* 3095 */       fieldsToUpdateList = StringUtils.makeListFromSequenceSimple(fieldsToUpdate);
/*      */     }
/*      */ 
/* 3098 */     FieldInfo fiSortable = new FieldInfo();
/* 3099 */     searchFields.getFieldInfo("IsSortable", fiSortable);
/*      */ 
/* 3101 */     FieldInfo fiFieldName = new FieldInfo();
/* 3102 */     searchFields.getFieldInfo("SearchFieldName", fiFieldName);
/*      */ 
/* 3104 */     for (searchFields.first(); searchFields.next(); )
/*      */     {
/* 3106 */       Vector fieldRow = searchFields.getCurrentRowValues();
/* 3107 */       String fieldName = (String)fieldRow.get(fiFieldName.m_index);
/*      */ 
/* 3111 */       if ((fieldName.startsWith(currentEngineLabel)) && (fieldName.length() > currentEngineLabel.length()))
/*      */       {
/* 3113 */         fieldName = fieldName.substring(currentEngineLabel.length());
/*      */       }
/*      */ 
/* 3117 */       if (fieldName.startsWith("("))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 3124 */       if ((fieldsToUpdateList != null) && (!fieldsToUpdateList.isEmpty()) && 
/* 3126 */         (!fieldsToUpdateList.contains(fieldName)))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 3134 */       Properties fieldProperties = (Properties)collectionDesignDef.m_fieldDesignMap.get(fieldName);
/*      */ 
/* 3136 */       if (fieldProperties != null)
/*      */       {
/* 3138 */         String isSortableString = (String)fieldProperties.get("IsSortable");
/* 3139 */         boolean isSortable = StringUtils.convertToBool(isSortableString, false);
/*      */ 
/* 3141 */         if (isSortable == true)
/*      */         {
/* 3143 */           validSortFields = validSortFields + fieldName + ",";
/*      */         }
/* 3145 */         fieldRow.set(fiSortable.m_index, Boolean.toString(isSortable));
/*      */       }
/*      */     }
/*      */ 
/* 3149 */     if (searchDesignBinder != null)
/*      */     {
/* 3151 */       searchDesignBinder.putLocal("validSortFields", validSortFields);
/*      */     }
/*      */ 
/* 3154 */     SharedObjects.putTable("SearchFieldInfo", searchFields);
/* 3155 */     return true;
/*      */   }
/*      */ 
/*      */   public static void updateCommonSearchCfgUsingSearchDesign(IndexerCollectionData collectionDesignDef)
/*      */     throws DataException
/*      */   {
/* 3168 */     CommonSearchConfig commonSearchConfigObject = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/*      */ 
/* 3171 */     String curEngine = commonSearchConfigObject.getCurrentEngineName();
/* 3172 */     String supportSearchDesignOpString = commonSearchConfigObject.getEngineValue(curEngine, "SupportSearchDesignSortFieldsUpdate");
/*      */ 
/* 3174 */     boolean supportSearchDesignOp = StringUtils.convertToBool(supportSearchDesignOpString, false);
/*      */ 
/* 3176 */     boolean searchFieldUpdateSuccessful = true;
/* 3177 */     searchFieldUpdateSuccessful = commonSearchConfigObject.updateSearchFieldInfoUsingSearchDesign(supportSearchDesignOp, collectionDesignDef, null);
/*      */ 
/* 3179 */     Hashtable clientConfigs = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*      */ 
/* 3184 */     if (searchFieldUpdateSuccessful != true)
/*      */       return;
/* 3186 */     commonSearchConfigObject.updateSearchSortFields(clientConfigs);
/* 3187 */     commonSearchConfigObject.updateUniversalSortFields(clientConfigs);
/*      */   }
/*      */ 
/*      */   protected void prepareReservedRules()
/*      */   {
/* 3198 */     prepareReservedRulesEx();
/*      */ 
/* 3200 */     if (SharedObjects.getEnvValueAsBoolean("DisableDatabaseFullTextConfigurationSync", false))
/*      */       return;
/* 3202 */     this.m_reservedRules.put("DATABASEFULLTEXT", this.m_reservedRules.get("DATABASE.FULLTEXT"));
/* 3203 */     this.m_reservedRules.put("DATABASE", this.m_reservedRules.get("DATABASE.METADATA"));
/*      */   }
/*      */ 
/*      */   protected void prepareReservedRulesEx()
/*      */   {
/* 3209 */     for (int keyNo = 0; keyNo < m_reservedKeysList.length; ++keyNo)
/*      */     {
/* 3211 */       String reservedKey = m_reservedKeysList[keyNo];
/* 3212 */       prepareAllEnginesKeyMap(this.m_reservedRules, reservedKey, true, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareOperatorCategories()
/*      */   {
/* 3218 */     for (int catNo = 0; catNo < m_operatorCategoryList.length; ++catNo)
/*      */     {
/* 3220 */       String reservedKey = m_operatorCategoryList[catNo];
/* 3221 */       prepareAllEnginesKeyMap(this.m_operatorCategories, reservedKey, true, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareAllEnginesKeyMap(Map<String, Map> allEnginesStorageMap, String key, boolean allowUserUpdates, boolean doCreateList)
/*      */   {
/* 3240 */     if (allEnginesStorageMap == null)
/*      */     {
/* 3242 */       allEnginesStorageMap = new Hashtable();
/*      */     }
/*      */ 
/* 3245 */     DataResultSet searchEngines = SharedObjects.getTable("SearchEngines");
/* 3246 */     FieldInfo seId = new FieldInfo();
/* 3247 */     searchEngines.getFieldInfo("seId", seId);
/*      */ 
/* 3249 */     for (searchEngines.first(); searchEngines.isRowPresent(); searchEngines.next())
/*      */     {
/* 3251 */       String id = searchEngines.getStringValue(seId.m_index);
/* 3252 */       Map mapForEngine = (Map)allEnginesStorageMap.get(id);
/*      */ 
/* 3254 */       if (mapForEngine == null)
/*      */       {
/* 3256 */         mapForEngine = new HashMap();
/* 3257 */         allEnginesStorageMap.put(id, mapForEngine);
/*      */       }
/*      */ 
/* 3260 */       String valuesForKey = getEngineValue(id, key);
/*      */ 
/* 3262 */       if (doCreateList == true)
/*      */       {
/* 3264 */         List valuesList = StringUtils.makeListFromSequenceSimple(valuesForKey);
/*      */ 
/* 3266 */         if (allowUserUpdates == true)
/*      */         {
/* 3268 */           modifyReservedItemsList(valuesList, key);
/*      */         }
/*      */ 
/* 3271 */         mapForEngine.put(key, valuesList);
/*      */       }
/*      */       else
/*      */       {
/* 3275 */         mapForEngine.put(key, valuesForKey);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected List modifyReservedItemsList(List reservedList, String key)
/*      */   {
/* 3287 */     String replaceStrings = SharedObjects.getEnvironmentValue(key);
/* 3288 */     String addStrings = SharedObjects.getEnvironmentValue(key + "+");
/* 3289 */     String subtractStrings = SharedObjects.getEnvironmentValue(key + "-");
/*      */ 
/* 3291 */     if (reservedList == null)
/*      */     {
/* 3293 */       reservedList = new ArrayList();
/*      */     }
/*      */ 
/* 3297 */     if ((replaceStrings != null) && (replaceStrings.length() > 0))
/*      */     {
/* 3299 */       reservedList = StringUtils.makeListFromSequenceSimple(replaceStrings);
/*      */     }
/*      */ 
/* 3303 */     if ((addStrings != null) && (addStrings.length() > 0))
/*      */     {
/* 3305 */       List addList = StringUtils.makeListFromSequenceSimple(addStrings);
/*      */ 
/* 3307 */       for (int addNo = 0; addNo < addList.size(); ++addNo)
/*      */       {
/* 3309 */         reservedList.add(addList.get(addNo));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3315 */     if ((subtractStrings != null) && (subtractStrings.length() > 0))
/*      */     {
/* 3317 */       List subtractList = StringUtils.makeListFromSequenceSimple(subtractStrings);
/* 3318 */       for (int subtractNo = 0; subtractNo < subtractList.size(); ++subtractNo)
/*      */       {
/* 3320 */         reservedList.remove(subtractList.get(subtractNo));
/*      */       }
/*      */     }
/*      */ 
/* 3324 */     return reservedList;
/*      */   }
/*      */ 
/*      */   public List getReservedChars(String engineName)
/*      */   {
/* 3331 */     Map reservedRulesForEngine = getReservedRulesForEngine(engineName);
/* 3332 */     return (List)reservedRulesForEngine.get("SearchReservedChars");
/*      */   }
/*      */ 
/*      */   public List getReservedWords(String engineName)
/*      */   {
/* 3337 */     Map reservedRulesForEngine = getReservedRulesForEngine(engineName);
/* 3338 */     return (List)reservedRulesForEngine.get("SearchReservedWords");
/*      */   }
/*      */ 
/*      */   public List getReservedProcessOperators(String engineName)
/*      */   {
/* 3343 */     Map reservedRulesForEngine = getReservedRulesForEngine(engineName);
/* 3344 */     return (List)reservedRulesForEngine.get("SearchReservedProcessOperators");
/*      */   }
/*      */ 
/*      */   public List getReservedExpErrorCodes(String engineName)
/*      */   {
/* 3349 */     Map reservedRulesForEngine = getReservedRulesForEngine(engineName);
/* 3350 */     return (List)reservedRulesForEngine.get("SearchReservedExpErrorCodes");
/*      */   }
/*      */ 
/*      */   public List getReservedProcessDecorators(String engineName)
/*      */   {
/* 3355 */     Map reservedRulesForEngine = getReservedRulesForEngine(engineName);
/* 3356 */     return (List)reservedRulesForEngine.get("SearchReservedProcessDecorators");
/*      */   }
/*      */ 
/*      */   public Map getReservedRulesForEngine(String engineName)
/*      */   {
/* 3361 */     if ((engineName == null) || (engineName.length() == 0))
/*      */     {
/* 3363 */       engineName = getCurrentEngineName();
/*      */     }
/*      */ 
/* 3366 */     Map reservedRulesForEngine = (Map)getPropertiesForEngine(this.m_reservedRules, engineName);
/* 3367 */     return reservedRulesForEngine;
/*      */   }
/*      */ 
/*      */   public Object getPropertiesForEngine(Map propertiesMap, String engineName)
/*      */   {
/* 3380 */     Object propertiesForEngine = propertiesMap.get(engineName);
/*      */ 
/* 3382 */     if (propertiesForEngine == null)
/*      */     {
/* 3384 */       int indexOfDot = engineName.lastIndexOf(".");
/*      */ 
/* 3386 */       if (indexOfDot > 0)
/*      */       {
/* 3388 */         String tempEngineName = engineName.substring(0, indexOfDot);
/* 3389 */         propertiesForEngine = propertiesMap.get(tempEngineName);
/*      */ 
/* 3391 */         if (propertiesForEngine != null)
/*      */         {
/* 3393 */           propertiesMap.put(engineName, propertiesForEngine);
/*      */         }
/*      */       }
/*      */     }
/* 3397 */     return propertiesForEngine;
/*      */   }
/*      */ 
/*      */   public List getReservedChars()
/*      */   {
/* 3405 */     return getReservedChars(null);
/*      */   }
/*      */ 
/*      */   public List getReservedWords()
/*      */   {
/* 3410 */     return getReservedWords(null);
/*      */   }
/*      */ 
/*      */   public List getReservedProcessOperators()
/*      */   {
/* 3415 */     return getReservedProcessOperators(null);
/*      */   }
/*      */ 
/*      */   public List getReservedExpErrorCodes()
/*      */   {
/* 3420 */     return getReservedExpErrorCodes(null);
/*      */   }
/*      */ 
/*      */   public List getReservedProcessDecorators()
/*      */   {
/* 3425 */     return getReservedProcessDecorators(null);
/*      */   }
/*      */ 
/*      */   public boolean hasReservedExpErrorCode(String errorMsg, String exceptionMessage)
/*      */   {
/* 3438 */     return hasReservedExpErrorCode(errorMsg, exceptionMessage, null);
/*      */   }
/*      */ 
/*      */   public boolean hasReservedExpErrorCode(String errorMsg, String exceptionMessage, String engineName)
/*      */   {
/* 3443 */     if ((errorMsg == null) || (errorMsg.length() == 0))
/*      */     {
/* 3445 */       return false;
/*      */     }
/*      */ 
/* 3448 */     boolean isReservedExpError = false;
/*      */ 
/* 3450 */     List reservedExpErrorCodes = getReservedExpErrorCodes(engineName);
/*      */ 
/* 3452 */     if ((reservedExpErrorCodes != null) && (reservedExpErrorCodes.size() > 0))
/*      */     {
/* 3454 */       for (int codeNo = 0; codeNo < reservedExpErrorCodes.size(); ++codeNo)
/*      */       {
/* 3456 */         String errorCode = (String)reservedExpErrorCodes.get(codeNo);
/* 3457 */         if ((errorCode == null) || (errorCode.length() <= 0) || (
/* 3459 */           (!errorMsg.contains(errorCode)) && (((exceptionMessage == null) || (!exceptionMessage.contains(errorCode))))))
/*      */           continue;
/* 3461 */         isReservedExpError = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 3467 */     return isReservedExpError;
/*      */   }
/*      */ 
/*      */   public String getReservedProcessOperatorsAsString()
/*      */   {
/* 3472 */     HashMap reservedOperators = (HashMap)getReservedProcessOperators();
/* 3473 */     List reservedOperatorsList = (List)reservedOperators.get("ReservedList");
/* 3474 */     String operators = "";
/*      */ 
/* 3476 */     if ((reservedOperatorsList != null) && (reservedOperatorsList.size() > 0))
/*      */     {
/* 3478 */       for (int op = 0; op < reservedOperatorsList.size(); ++op)
/*      */       {
/* 3480 */         if (op != 0)
/*      */         {
/* 3482 */           operators = operators + ",";
/*      */         }
/* 3484 */         operators = operators + reservedOperatorsList.get(op);
/*      */       }
/*      */     }
/*      */ 
/* 3488 */     return operators;
/*      */   }
/*      */ 
/*      */   public String getQueryValueModifierClassName()
/*      */   {
/* 3493 */     String engineName = getCurrentEngineName();
/* 3494 */     return getQueryValueModifierClassName(engineName);
/*      */   }
/*      */ 
/*      */   public String getQueryValueModifierClassName(String engineName)
/*      */   {
/* 3499 */     if ((engineName == null) || (engineName.length() == 0))
/*      */     {
/* 3501 */       engineName = getCurrentEngineName();
/*      */     }
/* 3503 */     String className = getStringFromTableWithInheritance("SearchEngineClasses", "SearchEngineName", engineName, "SearchQueryValueModifier");
/*      */ 
/* 3506 */     return className;
/*      */   }
/*      */ 
/*      */   public static int computeRowCount(DataBinder binder, int rowCount, boolean isAllowNonPositive)
/*      */     throws DataException
/*      */   {
/* 3608 */     if ((rowCount <= 0) && (!isAllowNonPositive))
/*      */     {
/* 3610 */       String msg = LocaleUtils.encodeMessage("csSearchItemNotPositive", null, "MaxResults");
/*      */ 
/* 3612 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 3615 */     boolean isAppRequest = StringUtils.convertToBool(binder.getLocal("isAppRequest"), false);
/* 3616 */     int maxResults = NumberUtils.parseInteger(binder.getEnvironmentValue("MaxResults"), 200);
/*      */ 
/* 3618 */     if ((isAppRequest) && (maxResults < m_maxAppResults))
/*      */     {
/* 3620 */       maxResults = m_maxAppResults;
/*      */     }
/* 3622 */     if (((maxResults > 0) && (rowCount > maxResults)) || ((isAllowNonPositive) && (rowCount != 0)))
/*      */     {
/* 3624 */       rowCount = maxResults;
/*      */     }
/*      */ 
/* 3627 */     return rowCount;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg) {
/* 3631 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104415 $";
/*      */   }
/*      */ 
/*      */   class CommonSearchSubjectCallback
/*      */     implements SubjectCallback
/*      */   {
/*      */     protected CommonSearchConfig m_config;
/*      */ 
/*      */     CommonSearchSubjectCallback()
/*      */     {
/*      */     }
/*      */ 
/*      */     public void init(CommonSearchConfig cfg)
/*      */     {
/* 3514 */       this.m_config = cfg;
/*      */     }
/*      */ 
/*      */     public void refresh(String subject) throws DataException, ServiceException
/*      */     {
/* 3519 */       if (subject.equalsIgnoreCase("searchapi"))
/*      */       {
/* 3521 */         this.m_config.resetOpNameMap();
/*      */       }
/*      */ 
/* 3524 */       if (!subject.equalsIgnoreCase("metadata")) {
/*      */         return;
/*      */       }
/*      */ 
/* 3528 */       IndexerCollectionData collectionDesignDef = SearchLoader.retrieveIndexDesign(CommonSearchConfig.this.m_currentCfgLabel, false);
/* 3529 */       CommonSearchConfig.updateCommonSearchCfgUsingSearchDesign(collectionDesignDef);
/*      */ 
/* 3533 */       SharedObjects.removeEnvironmentValue("IndexDesignRebuildRequired");
/*      */ 
/* 3535 */       boolean rewriteDesignBinder = false;
/*      */ 
/* 3539 */       DataBinder searchDesignBinder = collectionDesignDef.m_binder;
/*      */ 
/* 3541 */       if (searchDesignBinder == null)
/*      */         return;
/* 3543 */       String isRebuildNeededString = searchDesignBinder.getAllowMissing("isRebuildNeeded");
/* 3544 */       String isMetaRebuildNeededString = searchDesignBinder.getAllowMissing("isMetaRebuildNeeded");
/* 3545 */       if ((isRebuildNeededString != null) || (isMetaRebuildNeededString != null))
/*      */       {
/* 3547 */         rewriteDesignBinder = true;
/*      */       }
/*      */ 
/* 3554 */       String searchDesignDrillDownFields = SharedObjects.getEnvironmentValue("SearchDesignDrillDownFields");
/*      */ 
/* 3556 */       if (searchDesignDrillDownFields != null)
/*      */       {
/* 3558 */         SharedObjects.putEnvironmentValue("DrillDownFields", searchDesignDrillDownFields);
/* 3559 */         SharedObjects.removeEnvironmentValue("SearchDesignDrillDownFields");
/*      */       }
/*      */ 
/* 3563 */       String validDrillDownFields = searchDesignBinder.getAllowMissing("validDrillDownFields");
/* 3564 */       String currentDrillDownFields = SharedObjects.getEnvironmentValue("DrillDownFields");
/*      */ 
/* 3566 */       if ((currentDrillDownFields != null) && (((validDrillDownFields == null) || (!currentDrillDownFields.equalsIgnoreCase(validDrillDownFields)))))
/*      */       {
/* 3568 */         rewriteDesignBinder = true;
/* 3569 */         searchDesignBinder.putLocal("validDrillDownFields", currentDrillDownFields);
/*      */       }
/*      */ 
/* 3575 */       if (rewriteDesignBinder != true)
/*      */         return;
/* 3577 */       searchDesignBinder.removeLocal("isRebuildNeeded");
/* 3578 */       searchDesignBinder.removeLocal("isMetaRebuildNeeded");
/* 3579 */       DataResultSet designSet = (DataResultSet)searchDesignBinder.getResultSet("SearchDesignOptions");
/*      */ 
/* 3581 */       if (designSet == null)
/*      */         return;
/* 3583 */       SearchLoader.writeDesign(CommonSearchConfig.this.m_currentCfgLabel, designSet, searchDesignBinder);
/*      */     }
/*      */ 
/*      */     public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*      */     {
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CommonSearchConfig
 * JD-Core Version:    0.5.4
 */