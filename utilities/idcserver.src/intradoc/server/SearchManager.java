/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.lang.BlockingQueue;
/*     */ import intradoc.provider.ProviderConnection;
/*     */ import intradoc.provider.ProviderPoolManager;
/*     */ import intradoc.search.CommonSearchConnection;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.io.StringWriter;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchManager extends ProviderPoolManager
/*     */ {
/*  38 */   public static final String[] BASE_CACHE_QUERY_FIELDS_DEFAULTS = { "QueryText", "SortField", "SortOrder", "SortSpec", "Repository", "SearchEngineName", "LangID" };
/*     */   public static final int SEARCH = 0;
/*     */   public static final int HIGHLIGHT = 1;
/*     */   public static final int VIEW_DOC = 2;
/*     */   public static final int MAX_COLLS = 125;
/*     */   public static final int PDF_HIGHLIGHT = 0;
/*     */   public static final int HTML_HIGHLIGHT = 1;
/*     */   public static final int TEXT_HIGHLIGHT = 2;
/*     */   public static final int VIEW_HTML = 0;
/*     */   public static final int VIEW_TEXT = 1;
/*     */   protected String m_secInfo;
/*     */   protected int m_maxConnections;
/*     */   protected int m_connectionWaitTimeout;
/*     */   protected int m_connectionKeepAliveTimeout;
/*  71 */   protected String[] m_cacheQueryFields = null;
/*     */ 
/*  73 */   protected boolean m_useSearchPoolManager = false;
/*  74 */   protected SearchPoolManager m_searchPoolManager = null;
/*  75 */   protected Properties m_connEnv = null;
/*     */ 
/*  78 */   protected boolean m_debugIsWaitSearch = false;
/*  79 */   protected boolean m_reportEngineMessage = false;
/*     */ 
/*     */   public SearchManager()
/*     */   {
/*  83 */     this.m_secInfo = null;
/*  84 */     this.m_maxConnections = 5;
/*  85 */     this.m_connectionWaitTimeout = 60000;
/*  86 */     this.m_connectionKeepAliveTimeout = 60000;
/*     */   }
/*     */ 
/*     */   public void configure() throws DataException, ServiceException
/*     */   {
/*  91 */     loadSecurityFields();
/*     */ 
/* 101 */     computeCacheQueryFields();
/*     */ 
/* 104 */     prepareConnectionEnvironment();
/*     */     try
/*     */     {
/* 108 */       determineUseOfPoolManager();
/*     */     }
/*     */     catch (UnsatisfiedLinkError t)
/*     */     {
/* 112 */       ServiceException e = new ServiceException(t);
/* 113 */       e.m_errorCode = -26;
/* 114 */       throw e;
/*     */     }
/*     */     catch (NoClassDefFoundError t)
/*     */     {
/* 118 */       ServiceException e = new ServiceException(t);
/* 119 */       e.m_errorCode = -26;
/* 120 */       throw e;
/*     */     }
/*     */ 
/* 123 */     this.m_reportEngineMessage = (!SharedObjects.getEnvValueAsBoolean("DisableErrorPageStackTrace", false));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String retrieveSearchInfo(DataBinder binder, String fieldNames, int numFieldNames, int searchActionType)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 139 */     return retrieveSearchInfo(binder, fieldNames, numFieldNames, searchActionType, new ExecutionContextAdaptor());
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String retrieveSearchInfo(DataBinder binder, String fieldNames, int numFieldNames, int searchActionType, ExecutionContext ctxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 148 */     DataBinder responseBinder = retrieveSearchInfoAsBinder(binder, fieldNames, numFieldNames, searchActionType, ctxt);
/* 149 */     String responseString = getString(responseBinder);
/* 150 */     return responseString;
/*     */   }
/*     */ 
/*     */   public DataBinder retrieveSearchInfoAsBinder(DataBinder binder, String fieldNames, int numFieldNames, int searchActionType)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 157 */     return retrieveSearchInfoAsBinder(binder, fieldNames, numFieldNames, searchActionType, new ExecutionContextAdaptor());
/*     */   }
/*     */ 
/*     */   public DataBinder retrieveSearchInfoAsBinder(DataBinder binder, String fieldNames, int numFieldNames, int searchActionType, ExecutionContext ctxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 165 */     boolean isAll = true;
/*     */ 
/* 167 */     if (searchActionType != 0)
/*     */     {
/* 169 */       isAll = false;
/*     */     }
/* 171 */     DataBinder resultBinder = null;
/* 172 */     CommonSearchConfig config = SearchIndexerUtils.retrieveSearchConfig(ctxt);
/*     */ 
/* 174 */     boolean usePoolManager = usePoolManager(config);
/* 175 */     CommonSearchConnection con = getConnection(binder, isAll, ctxt, usePoolManager);
/*     */     try
/*     */     {
/* 179 */       String docName = null;
/* 180 */       String errorMsg = null;
/*     */ 
/* 182 */       int hlType = 2;
/* 183 */       boolean isSearch = true;
/*     */ 
/* 185 */       if (searchActionType == 0)
/*     */       {
/* 187 */         binder.putLocal("FieldNames", fieldNames);
/* 188 */         binder.putLocal("NumFieldNames", "" + numFieldNames);
/*     */       }
/*     */       else
/*     */       {
/* 192 */         isSearch = false;
/* 193 */         docName = binder.getLocal("dDocName");
/* 194 */         if ((docName == null) || (docName.length() == 0))
/*     */         {
/* 196 */           String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing", null, "csContentIdLabel");
/*     */ 
/* 198 */           throw new ServiceException(msg);
/*     */         }
/*     */       }
/* 201 */       config.prepareQuery(binder, ctxt, isSearch);
/*     */ 
/* 203 */       String repo = binder.getLocal("Repository");
/* 204 */       if (repo != null)
/*     */       {
/* 206 */         String filter = config.getStringFromTable("SearchRepository", "srID", repo, "srFilter");
/*     */ 
/* 209 */         binder.putLocal("srFilter", filter);
/*     */       }
/* 211 */       config.appendQueryTextFilters(binder, "srFilter,CommonSearchSecurityFilter");
/* 212 */       config.constructFullQuery(binder, ctxt);
/*     */ 
/* 214 */       if (searchActionType == 0)
/*     */       {
/* 216 */         String msg = " [" + binder.getLocal("StartRow") + "," + (NumberUtils.parseInteger(binder.getLocal("StartRow"), 0) + NumberUtils.parseInteger(binder.getLocal("ResultCount"), 20) - 1) + "] sort(" + binder.getLocal("SortSpec") + ")";
/*     */ 
/* 220 */         msg = "query(live): " + binder.getLocal("QueryText") + msg;
/* 221 */         debugTrace(msg);
/*     */ 
/* 224 */         errorMsg = con.doQuery(binder);
/*     */       }
/* 226 */       else if (searchActionType == 1)
/*     */       {
/* 228 */         String hlBegin = SharedObjects.getEnvironmentValue("HighlightBegin");
/* 229 */         String hlEnd = SharedObjects.getEnvironmentValue("HighlightEnd");
/* 230 */         String hlTypeStr = binder.getAllowMissing("HighlightType");
/*     */ 
/* 232 */         if ((hlTypeStr != null) && (hlTypeStr.length() > 0))
/*     */         {
/* 234 */           if (hlTypeStr.equals("PdfHighlight"))
/*     */           {
/* 236 */             hlType = 0;
/*     */ 
/* 242 */             binder.putLocal("forceResponseNoCompression", "1");
/*     */           }
/* 244 */           else if (hlTypeStr.equals("HtmlHighlight"))
/*     */           {
/* 246 */             hlType = 1;
/*     */           }
/*     */         }
/*     */ 
/* 250 */         if ((hlBegin == null) || (hlBegin.length() == 0))
/*     */         {
/* 252 */           hlBegin = "<strong>";
/* 253 */           hlEnd = "</strong>";
/*     */         }
/*     */ 
/* 256 */         errorMsg = con.retrieveHighlightInfo(binder, hlType, hlBegin, hlEnd);
/*     */       }
/*     */       else
/*     */       {
/* 261 */         int viewType = 1;
/* 262 */         String viewTypeStr = binder.getAllowMissing("ViewType");
/* 263 */         if ((viewTypeStr != null) && (viewTypeStr.length() > 0) && 
/* 265 */           (viewTypeStr.equals("ViewHtml")))
/*     */         {
/* 267 */           viewType = 0;
/*     */         }
/*     */ 
/* 270 */         errorMsg = con.viewDoc(binder, viewType);
/*     */       }
/*     */ 
/* 273 */       if (errorMsg != null)
/*     */       {
/* 275 */         BufferedReader bReader = new BufferedReader(new StringReader(errorMsg));
/* 276 */         DataBinder b = binder.createShallowCopy();
/* 277 */         b.receive(bReader);
/* 278 */         String statusCode = binder.getLocal("StatusCode");
/*     */ 
/* 280 */         if (statusCode != null)
/*     */         {
/* 282 */           int error = Integer.parseInt(statusCode);
/*     */ 
/* 284 */           if (error != 0)
/*     */           {
/* 286 */             String statusMessage = binder.getLocal("StatusMessage");
/* 287 */             Throwable parentException = (Throwable)ctxt.getCachedObject("SearchException");
/*     */ 
/* 290 */             if ((!this.m_reportEngineMessage) && (!statusMessage.contains("csJdbcGenericError")))
/*     */             {
/* 292 */               Report.trace("search", "Masking status message '" + statusMessage, parentException);
/*     */ 
/* 294 */               statusMessage = "!csSearchUnableToReturnResults";
/*     */             }
/*     */ 
/* 297 */             ServiceException se = new ServiceException(error, statusMessage);
/* 298 */             if (parentException != null)
/*     */             {
/* 300 */               SystemUtils.setExceptionCause(se, parentException);
/*     */             }
/* 302 */             throw se;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 307 */       resultBinder = con.getResultAsBinder();
/*     */     }
/*     */     finally
/*     */     {
/* 321 */       releaseConnection(con.getId());
/*     */     }
/* 323 */     return resultBinder;
/*     */   }
/*     */ 
/*     */   public int computeNumSafeSecurityParenthesis(String query)
/*     */   {
/* 353 */     char[] qChars = query.toCharArray();
/* 354 */     int nParensNeeded = 1;
/* 355 */     boolean insideUnsafe = false;
/* 356 */     int deductibleUnsafeParens = 0;
/* 357 */     int deductibleSafeParens = 0;
/* 358 */     boolean isPrevUnsafe = false;
/* 359 */     for (int i = 0; i < qChars.length; ++i)
/*     */     {
/* 361 */       char ch = qChars[i];
/* 362 */       boolean isSafe = true;
/* 363 */       if (ch == '(')
/*     */       {
/* 365 */         if (!isPrevUnsafe)
/*     */         {
/* 367 */           if (!insideUnsafe)
/*     */           {
/* 369 */             ++deductibleSafeParens;
/*     */           }
/*     */           else
/*     */           {
/* 373 */             ++deductibleUnsafeParens;
/*     */           }
/*     */         }
/*     */       }
/* 377 */       else if (ch == ')')
/*     */       {
/* 379 */         if (deductibleUnsafeParens > 0)
/*     */         {
/* 381 */           --deductibleUnsafeParens;
/*     */         }
/* 383 */         else if (deductibleSafeParens > 0)
/*     */         {
/* 385 */           --deductibleSafeParens;
/*     */         }
/*     */         else
/*     */         {
/* 389 */           ++nParensNeeded;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 394 */         isSafe = isSafeChar(ch);
/* 395 */         if (!isSafe)
/*     */         {
/* 397 */           insideUnsafe = true;
/* 398 */           deductibleUnsafeParens = 0;
/*     */         }
/*     */       }
/* 401 */       isPrevUnsafe = !isSafe;
/*     */     }
/*     */ 
/* 404 */     return nParensNeeded;
/*     */   }
/*     */ 
/*     */   boolean isSafeChar(char ch)
/*     */   {
/* 409 */     return (ch < '\036') || (ch > '') || (Character.isLetterOrDigit(ch)) || (ch == ' ') || (ch == '<') || (ch == '>');
/*     */   }
/*     */ 
/*     */   public DataBinder retrieveDocInfo(DataBinder binder, String fieldNames, int numFieldNames, String docKey, boolean isSecurityInfo)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 416 */     DataBinder resultBinder = null;
/* 417 */     String errorMsg = null;
/* 418 */     SearchConnection con = getAndOpenConnection(binder, false);
/*     */     try
/*     */     {
/* 422 */       if (isSecurityInfo)
/*     */       {
/* 424 */         errorMsg = con.retrieveDocInfo(docKey, this.m_secInfo, 2);
/*     */       }
/*     */       else
/*     */       {
/* 428 */         errorMsg = con.retrieveDocInfo(docKey, fieldNames, numFieldNames);
/*     */       }
/* 430 */       if (errorMsg != null)
/*     */       {
/* 432 */         BufferedReader bReader = new BufferedReader(new StringReader(errorMsg));
/* 433 */         DataBinder b = binder.createShallowCopy();
/* 434 */         b.receive(bReader);
/*     */ 
/* 436 */         String statusCode = binder.getLocal("StatusCode");
/*     */ 
/* 438 */         if (statusCode != null)
/*     */         {
/* 440 */           int error = Integer.parseInt(statusCode);
/*     */ 
/* 442 */           if (error != 0)
/*     */           {
/* 444 */             String statusMessage = binder.getLocal("StatusMessage");
/* 445 */             throw new ServiceException(error, statusMessage);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 450 */       resultBinder = con.getResultAsBinder();
/*     */     }
/*     */     finally
/*     */     {
/* 454 */       releaseConnection(con.getId());
/*     */     }
/*     */ 
/* 457 */     return resultBinder;
/*     */   }
/*     */ 
/*     */   public CommonSearchConnection getConnection(DataBinder binder, boolean isAll, ExecutionContext ctxt, boolean usePoolManger)
/*     */     throws ServiceException, DataException
/*     */   {
/* 463 */     Thread thrd = Thread.currentThread();
/* 464 */     String id = thrd.getName();
/*     */ 
/* 466 */     CommonSearchConfig cfg = SearchIndexerUtils.retrieveSearchConfig(ctxt);
/* 467 */     CommonSearchConnection con = null;
/* 468 */     if (usePoolManager(cfg))
/*     */     {
/* 470 */       con = (CommonSearchConnection)this.m_searchPoolManager.getConnection(id);
/*     */     }
/*     */     else
/*     */     {
/* 474 */       con = new CommonSearchConnection();
/* 475 */       con.init(this, new DataBinder(this.m_connEnv), SearchIndexerUtils.getSearchImplementorClassName(cfg), null, 0, null);
/*     */     }
/*     */ 
/* 478 */     con.setId(id);
/*     */ 
/* 480 */     con.prepareUse(ctxt);
/* 481 */     con.initCollection(getCollections(binder, isAll));
/* 482 */     return con;
/*     */   }
/*     */ 
/*     */   public SearchConnection getAndOpenConnection(DataBinder binder, boolean isAll)
/*     */     throws ServiceException, DataException
/*     */   {
/* 488 */     return getConnection(binder, isAll, new ExecutionContextAdaptor(), true);
/*     */   }
/*     */ 
/*     */   public void forceRefreshCurrentConnections(boolean useTimeout)
/*     */     throws ServiceException
/*     */   {
/* 494 */     if (this.m_searchPoolManager != null)
/*     */     {
/* 496 */       this.m_searchPoolManager.forceRefreshCurrentConnections(useTimeout);
/*     */     }
/* 498 */     computeCacheQueryFields();
/*     */   }
/*     */ 
/*     */   protected Vector getCollections(DataBinder binder, boolean isAll)
/*     */     throws DataException, ServiceException
/*     */   {
/* 504 */     Vector v = new IdcVector();
/*     */ 
/* 506 */     DataResultSet drset = SharedObjects.getTable("SearchCollections");
/* 507 */     if (drset == null)
/*     */     {
/* 509 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "SearchCollections");
/*     */ 
/* 511 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 514 */     if (isAll)
/*     */     {
/* 517 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 519 */         String id = ResultSetUtils.getValue(drset, "sCollectionID");
/* 520 */         String loc = ResultSetUtils.getValue(drset, "sLocation");
/* 521 */         String flag = ResultSetUtils.getValue(drset, "sFlag");
/*     */ 
/* 523 */         if ((id == null) || (id.length() <= 0) || (loc == null) || (loc.length() <= 0) || (!flag.equalsIgnoreCase("enabled"))) {
/*     */           continue;
/*     */         }
/*     */ 
/* 527 */         v.addElement(loc);
/*     */       }
/*     */ 
/* 532 */       DataResultSet optColls = SharedObjects.getTable("OptionalSearchCollections");
/*     */ 
/* 534 */       if (optColls != null)
/*     */       {
/* 536 */         for (optColls.first(); optColls.isRowPresent(); optColls.next())
/*     */         {
/* 538 */           String id = ResultSetUtils.getValue(optColls, "sCollectionID");
/*     */ 
/* 540 */           String val = binder.getAllowMissing(id);
/* 541 */           if (!StringUtils.convertToBool(val, false))
/*     */             continue;
/* 543 */           String loc = ResultSetUtils.getValue(optColls, "sLocation");
/* 544 */           v.addElement(loc);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 551 */       String sid = binder.getAllowMissing("sCollectionID");
/* 552 */       if ((sid == null) || (sid.length() == 0))
/*     */       {
/* 554 */         sid = SharedObjects.getEnvironmentValue("IDC_Name");
/*     */       }
/*     */ 
/* 557 */       FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID", "sLocation", "sFlag" }, true);
/*     */ 
/* 559 */       Vector row = drset.findRow(info[0].m_index, sid);
/* 560 */       if (row == null)
/*     */       {
/* 562 */         String msg = LocaleUtils.encodeMessage("csSearchCollectionNotDefined", null, sid);
/*     */ 
/* 564 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 567 */       String path = (String)row.elementAt(info[1].m_index);
/* 568 */       if ((path == null) || (path.length() == 0))
/*     */       {
/* 570 */         String msg = LocaleUtils.encodeMessage("csSearchCollectionPathMissing", null, sid);
/*     */ 
/* 572 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 575 */       String flag = (String)row.elementAt(info[2].m_index);
/* 576 */       if ((flag == null) || (flag.length() == 0) || (flag.equalsIgnoreCase("disabled")))
/*     */       {
/* 578 */         throw new ServiceException("!csSearchDefaultCollectionDisabled");
/*     */       }
/* 580 */       v.addElement(path);
/*     */     }
/*     */ 
/* 584 */     if (v.size() > 125)
/*     */     {
/* 586 */       Integer maxColls = new Integer(125);
/* 587 */       String maxCollsStr = maxColls.toString();
/* 588 */       String msg = LocaleUtils.encodeMessage("csTooManySearchCollections", null, maxCollsStr);
/*     */ 
/* 590 */       throw new ServiceException(msg);
/*     */     }
/* 592 */     return v;
/*     */   }
/*     */ 
/*     */   protected void loadSecurityFields()
/*     */   {
/* 597 */     String secFields = SharedObjects.getEnvironmentValue("SecurityInfoFields");
/* 598 */     if ((secFields != null) && (secFields.length() != 0))
/*     */       return;
/* 600 */     this.m_secInfo = "dSecurityGroup,dDocAccount";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected void initConnectionShare()
/*     */     throws DataException
/*     */   {
/* 611 */     this.m_maxConnections = SharedObjects.getEnvironmentInt("MaxSearchConnections", this.m_maxConnections);
/*     */ 
/* 615 */     this.m_connectionWaitTimeout = SharedObjects.getTypedEnvironmentInt("SearchConnectionWaitTimeout", this.m_connectionWaitTimeout, 18, 18);
/*     */ 
/* 620 */     this.m_connectionKeepAliveTimeout = SharedObjects.getTypedEnvironmentInt("SearchConnectionKeepAliveTimeout", this.m_connectionKeepAliveTimeout, 18, 18);
/*     */ 
/* 626 */     for (int i = 0; i < this.m_maxConnections; ++i)
/*     */     {
/* 628 */       addConnectionToPool();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeCacheQueryFields() throws ServiceException
/*     */   {
/* 634 */     List fields = new ArrayList();
/* 635 */     String[] fieldsArray = ResourceContainerUtils.getDynamicFieldListResource("SearchManagerQueryKeyFields", BASE_CACHE_QUERY_FIELDS_DEFAULTS);
/*     */ 
/* 637 */     Collections.addAll(fields, fieldsArray);
/*     */ 
/* 640 */     DataResultSet optColls = SharedObjects.getTable("OptionalSearchCollections");
/*     */ 
/* 642 */     if (optColls != null)
/*     */     {
/* 644 */       for (optColls.first(); optColls.isRowPresent(); optColls.next())
/*     */       {
/* 646 */         String id = ResultSetUtils.getValue(optColls, "sCollectionID");
/*     */ 
/* 648 */         if (StringUtils.findStringIndex(BASE_CACHE_QUERY_FIELDS_DEFAULTS, id) >= 0)
/*     */         {
/* 650 */           String msg = LocaleUtils.encodeMessage("csOptionalCollectionCannotBeField", null, id);
/* 651 */           throw new ServiceException(msg);
/*     */         }
/* 653 */         fields.add(id);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 658 */     DataResultSet extraCacheKeys = SharedObjects.getTable("ExtraSearchCacheKeys");
/* 659 */     if (extraCacheKeys != null)
/*     */     {
/* 661 */       FieldInfo keyNameFieldInfo = new FieldInfo();
/* 662 */       if (extraCacheKeys.getFieldInfo("esckKeyName", keyNameFieldInfo))
/*     */       {
/* 664 */         int keyNameIndex = keyNameFieldInfo.m_index;
/* 665 */         for (extraCacheKeys.first(); extraCacheKeys.isRowPresent(); extraCacheKeys.next())
/*     */         {
/* 667 */           fields.add(extraCacheKeys.getStringValue(keyNameIndex));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 674 */     String[] temp = new String[fields.size()];
/* 675 */     temp = (String[])fields.toArray(temp);
/*     */ 
/* 677 */     this.m_cacheQueryFields = temp;
/*     */   }
/*     */ 
/*     */   protected void prepareConnectionEnvironment() throws DataException
/*     */   {
/* 682 */     CommonSearchConfig config = SearchIndexerUtils.retrieveSearchConfig(null);
/* 683 */     Map searchConfig = config.getEngineRules();
/* 684 */     this.m_connEnv = new Properties();
/* 685 */     DataBinder.mergeHashTables(this.m_connEnv, searchConfig);
/* 686 */     Properties env = SharedObjects.getSecureEnvironment();
/* 687 */     for (Enumeration en = env.propertyNames(); en.hasMoreElements(); )
/*     */     {
/* 689 */       String key = (String)en.nextElement();
/* 690 */       String value = env.getProperty(key);
/* 691 */       this.m_connEnv.put(key, value);
/*     */     }
/*     */ 
/* 696 */     this.m_debugIsWaitSearch = SharedObjects.getEnvValueAsBoolean("SearchIsDebugWait", false);
/*     */   }
/*     */ 
/*     */   protected void determineUseOfPoolManager()
/*     */     throws DataException
/*     */   {
/* 702 */     CommonSearchConfig config = SearchIndexerUtils.retrieveSearchConfig(null);
/* 703 */     String engineName = config.getCurrentEngineName();
/* 704 */     this.m_useSearchPoolManager = StringUtils.convertToBool(config.getEngineValue("UseSearchPoolManager"), false);
/*     */ 
/* 707 */     if (!this.m_useSearchPoolManager)
/*     */       return;
/* 709 */     String defaultClassName = SearchIndexerUtils.getSearchImplementorClassName(config);
/* 710 */     this.m_searchPoolManager = new SearchPoolManager(engineName, defaultClassName, this.m_connEnv);
/* 711 */     this.m_searchPoolManager.init();
/*     */   }
/*     */ 
/*     */   protected boolean usePoolManager(CommonSearchConfig config)
/*     */   {
/* 717 */     return (this.m_useSearchPoolManager) && (!config.getCurrentEngineName().equals("DATABASE")) && (!config.getCurrentEngineName().startsWith("DATABASE.METADATA"));
/*     */   }
/*     */ 
/*     */   public String[] getCacheQueryFields()
/*     */   {
/* 723 */     return this.m_cacheQueryFields;
/*     */   }
/*     */ 
/*     */   public boolean getDebugIsWaitSearch()
/*     */   {
/* 728 */     return this.m_debugIsWaitSearch;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected ProviderConnection assignConnection(String id)
/*     */     throws DataException
/*     */   {
/* 738 */     ProviderConnection con = null;
/*     */     try
/*     */     {
/* 742 */       debugLockingMsg("assigning connection");
/*     */ 
/* 744 */       con = (ProviderConnection)this.m_connectionPool.removeWithTimeout(this.m_connectionWaitTimeout);
/* 745 */       setActiveConnection(id, con);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 749 */       throw new DataException("!csProviderNoConnections");
/*     */     }
/* 751 */     return con;
/*     */   }
/*     */ 
/*     */   public void debugTraceStart(DataBinder binder)
/*     */   {
/* 756 */     String user = binder.getLocal("dUser");
/* 757 */     String host = binder.getEnvironmentValue("HTTP_HOST");
/* 758 */     if ((user == null) || (user.length() == 0))
/*     */     {
/* 760 */       user = "anonymous";
/*     */     }
/* 762 */     if (host == null)
/*     */     {
/* 764 */       host = "<unknown>";
/*     */     }
/* 766 */     String msg = "Query by " + user + " from " + host;
/* 767 */     Report.debug("searchquery", msg, null);
/*     */   }
/*     */ 
/*     */   public void debugTrace(String msg)
/*     */   {
/* 772 */     Report.trace("searchquery", msg, null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected String encodeVdkKey(String docKey)
/*     */     throws UnsupportedEncodingException
/*     */   {
/* 782 */     String key = docKey;
/* 783 */     if (SearchLoader.m_encodeVdkKeyForSearch)
/*     */     {
/* 785 */       IdcStringBuilder buff = new IdcStringBuilder();
/* 786 */       buff.append("z");
/* 787 */       SearchLoader.encodeToHexString(docKey.toLowerCase(), buff);
/* 788 */       key = buff.toString();
/*     */     }
/*     */ 
/* 791 */     return key;
/*     */   }
/*     */ 
/*     */   public void releaseConnection(String id)
/*     */   {
/* 797 */     if (this.m_searchPoolManager == null)
/*     */       return;
/* 799 */     this.m_searchPoolManager.releaseConnection(id);
/*     */   }
/*     */ 
/*     */   public String getString(DataBinder binder)
/*     */   {
/* 805 */     String result = null;
/*     */     try
/*     */     {
/* 808 */       StringWriter sw = new StringWriter();
/* 809 */       binder.send(sw);
/* 810 */       result = sw.toString();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 815 */       SystemUtils.dumpException("search", e);
/*     */     }
/* 817 */     return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 822 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103618 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.SearchManager
 * JD-Core Version:    0.5.4
 */