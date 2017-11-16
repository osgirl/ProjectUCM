/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.indexer.DocIndexerHandler;
/*     */ import intradoc.indexer.IndexerDriver;
/*     */ import intradoc.indexer.IndexerInfo;
/*     */ import intradoc.indexer.IndexerWorkObject;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.server.SearchLoader;
/*     */ import intradoc.server.SearchService;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.ActiveIndexState;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IndexCheck
/*     */   implements IdcAnalyzeTask
/*     */ {
/*     */   protected Workspace m_workspace;
/*     */   protected Properties m_context;
/*     */   protected String m_revLogName;
/*     */   protected String m_indexLogName;
/*     */   protected SearchService m_searchService;
/*     */   protected CommonSearchConfig m_queryConfig;
/*     */   protected IdcAnalyzeApp m_analyzer;
/*     */   protected IndexerWorkObject m_data;
/*     */   protected boolean m_useAltaVista;
/*     */   protected int m_errorCount;
/*     */   protected int m_errorsFixed;
/*     */   protected int m_maxErrors;
/*     */   protected StringBuffer m_errors;
/*     */   protected int m_count;
/*     */   protected int m_iteration;
/*     */ 
/*     */   public IndexCheck()
/*     */   {
/*  49 */     this.m_useAltaVista = false;
/*  50 */     this.m_errorCount = 0;
/*  51 */     this.m_errorsFixed = 0;
/*  52 */     this.m_maxErrors = 0;
/*     */ 
/*  54 */     this.m_errors = null;
/*  55 */     this.m_count = 0;
/*  56 */     this.m_iteration = 0;
/*     */   }
/*     */ 
/*     */   public void init(IdcAnalyzeApp analyzer, Properties cxt, Workspace ws) throws DataException, ServiceException
/*     */   {
/*  61 */     this.m_analyzer = analyzer;
/*  62 */     this.m_workspace = ws;
/*  63 */     this.m_context = cxt;
/*     */ 
/*  65 */     this.m_revLogName = ((String)this.m_context.get("IdcCommandFile"));
/*     */ 
/*  67 */     SearchLoader.initEx(ws);
/*  68 */     SearchLoader.cacheSearchCollections();
/*     */ 
/*  71 */     SearchLoader.createSearchCache();
/*  72 */     SharedObjects.putEnvironmentValue("UseSearchCache", "0");
/*     */ 
/*  74 */     IdcSystemLoader.initSearchIndexer(ws, true);
/*     */ 
/*  76 */     ActiveIndexState.load();
/*     */ 
/*  78 */     this.m_useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/*     */ 
/*  80 */     this.m_data = new IndexerWorkObject(null);
/*     */     try
/*     */     {
/*  83 */       this.m_data.initEx("update", "", 0, new HashMap(), ws, null);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  89 */       e.printStackTrace();
/*  90 */       return;
/*     */     }
/*     */ 
/*  93 */     this.m_data.initForIndexEngine();
/*     */ 
/*  95 */     this.m_data.m_updateStyleFile = this.m_useAltaVista;
/*     */ 
/*  99 */     this.m_data.m_indexer.prepare();
/*     */ 
/* 101 */     this.m_searchService = ((SearchService)ComponentClassFactory.createClassInstance("SearchService", "intradoc.server.SearchService", "!csErrorError"));
/*     */ 
/* 103 */     this.m_searchService.init(this.m_workspace, null, new DataBinder(), new ServiceData());
/*     */ 
/* 109 */     this.m_searchService.initDelegatedObjects();
/*     */ 
/* 115 */     this.m_searchService.preActions();
/*     */ 
/* 120 */     IdcLocale locale = LocaleResources.getLocale("SystemLocale");
/* 121 */     this.m_searchService.setCachedObject("UserLocale", locale);
/*     */ 
/* 123 */     this.m_queryConfig = SearchIndexerUtils.retrieveSearchConfig(null);
/*     */ 
/* 126 */     this.m_searchService.searchCacheReport();
/*     */   }
/*     */ 
/*     */   public int getErrorCount()
/*     */   {
/* 131 */     return this.m_errorCount;
/*     */   }
/*     */ 
/*     */   public boolean doTask()
/*     */     throws DataException, ServiceException
/*     */   {
/* 144 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 145 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeIndexMsg", new Object[0]));
/* 146 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeLineBreak", new Object[0]));
/*     */ 
/* 149 */     String engineName = this.m_queryConfig.getCurrentEngineName();
/* 150 */     if ((engineName.startsWith("DATABASE.")) && (!engineName.equals("DATABASE.FULLTEXT")))
/*     */     {
/* 153 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeSkippingDatabaseIndexCheck", new Object[] { engineName }));
/*     */ 
/* 155 */       this.m_analyzer.setProgress(80);
/* 156 */       return true;
/*     */     }
/*     */ 
/* 159 */     this.m_errors = new StringBuffer("");
/*     */ 
/* 162 */     int numResults = -1;
/* 163 */     int startVal = 0;
/* 164 */     int maxDid = -1;
/* 165 */     int endVal = -1;
/* 166 */     boolean isBoundedCheck = false;
/*     */ 
/* 169 */     String startRange = (String)this.m_context.get("StartID");
/* 170 */     if (startRange != null)
/*     */     {
/* 172 */       startVal = NumberUtils.parseInteger(startRange, 0);
/*     */     }
/* 174 */     String endRange = (String)this.m_context.get("EndID");
/* 175 */     if (endRange != null)
/*     */     {
/* 177 */       maxDid = NumberUtils.parseInteger(endRange, -1) + 1;
/* 178 */       numResults = maxDid - startVal;
/* 179 */       isBoundedCheck = true;
/*     */     }
/*     */ 
/* 182 */     if (!isBoundedCheck)
/*     */     {
/* 185 */       numResults = IdcAnalyzeUtils.getMaxDid(this.m_workspace);
/*     */ 
/* 189 */       ResultSet rset = null;
/*     */       try
/*     */       {
/* 192 */         rset = retrieveSearchResults(0, 0, 1);
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 196 */         throw new ServiceException(ioe);
/*     */       }
/*     */ 
/* 199 */       int numIndexResults = 0;
/* 200 */       if ((rset != null) && (rset.isRowPresent()))
/*     */       {
/* 202 */         numIndexResults = NumberUtils.parseInteger(rset.getStringValue(0), 0);
/*     */       }
/*     */ 
/* 207 */       if (numIndexResults > numResults)
/*     */       {
/* 209 */         numResults = numIndexResults;
/*     */       }
/* 211 */       maxDid = numResults;
/*     */     }
/*     */ 
/* 214 */     this.m_count = 0;
/* 215 */     this.m_iteration = (numResults / 80 + 1);
/*     */ 
/* 217 */     String incStr = (String)this.m_context.get("IndexBatchSize");
/* 218 */     int inc = NumberUtils.parseInteger(incStr, 500);
/* 219 */     endVal = (inc > numResults) ? maxDid : startVal + inc;
/*     */ 
/* 221 */     Properties args = new Properties();
/*     */ 
/* 223 */     String maxErrorStr = (String)this.m_context.get("MaxErrors");
/* 224 */     this.m_maxErrors = NumberUtils.parseInteger(maxErrorStr, 5000);
/*     */ 
/* 227 */     while ((startVal < maxDid) && (this.m_errorCount <= this.m_maxErrors))
/*     */     {
/* 230 */       DataResultSet indexSet = null;
/*     */       try
/*     */       {
/* 233 */         indexSet = retrieveSearchResults(startVal, endVal, inc);
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 237 */         throw new ServiceException(ioe);
/*     */       }
/* 239 */       if (indexSet == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 243 */       Hashtable indexMap = IdcAnalyzeUtils.makeMap(indexSet, "dID", true, true);
/*     */ 
/* 245 */       args.put("startID", startVal + "");
/* 246 */       args.put("endID", endVal + "");
/* 247 */       args.put("columns", "Revisions.dID, dReleaseState, dStatus, dRevLabel, dSecurityGroup, dDocName, dDocAccount, DocMeta.*");
/*     */ 
/* 249 */       args.put("whereClause", "Revisions.dID = DocMeta.dID");
/* 250 */       args.put("table", "Revisions,DocMeta");
/*     */ 
/* 252 */       Hashtable revMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", args, this.m_workspace, "dID", true, false);
/*     */ 
/* 255 */       compareRevisionsToIndex(revMap, indexMap);
/*     */ 
/* 257 */       analyzeIndexExtras(indexMap);
/*     */ 
/* 259 */       startVal = endVal;
/* 260 */       endVal += inc;
/* 261 */       if ((isBoundedCheck) && (endVal > maxDid))
/*     */       {
/* 263 */         endVal = maxDid;
/*     */       }
/*     */     }
/*     */ 
/* 267 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 268 */     if (this.m_errors.length() > 0)
/*     */     {
/* 271 */       this.m_data.m_indexer.finishIndexing(false);
/* 272 */       IdcMessage logmsg = IdcMessageFactory.lc();
/* 273 */       logmsg.m_msgEncoded = this.m_errors.toString();
/* 274 */       this.m_analyzer.log(logmsg);
/*     */     }
/* 276 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeEntriesChecked", new Object[] { Integer.valueOf(this.m_count) }));
/* 277 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorCount", new Object[] { Integer.valueOf(this.m_errorCount) }));
/* 278 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorsFixed", new Object[] { Integer.valueOf(this.m_errorsFixed) }));
/*     */ 
/* 281 */     if (startVal < maxDid)
/*     */     {
/* 283 */       this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 284 */       if (this.m_errorCount > this.m_maxErrors)
/*     */       {
/* 287 */         this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeTooManyErrors", new Object[] { Integer.valueOf(this.m_errorCount), Integer.valueOf(this.m_maxErrors), Integer.valueOf(numResults - this.m_count), Integer.valueOf(numResults) }));
/*     */       }
/*     */       else
/*     */       {
/* 295 */         this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeTaskFinishedUnexpectedly", new Object[] { Integer.valueOf(this.m_errorCount), Integer.valueOf(numResults), Integer.valueOf(numResults - this.m_count) }));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 304 */     SubjectManager.forceRefresh("searchapi");
/*     */ 
/* 306 */     this.m_analyzer.setProgress(80);
/* 307 */     return true;
/*     */   }
/*     */ 
/*     */   protected void compareRevisionsToIndex(Hashtable revMap, Hashtable indexMap)
/*     */     throws ServiceException
/*     */   {
/* 313 */     Enumeration en = revMap.keys();
/* 314 */     for (; (this.m_errorCount <= this.m_maxErrors) && (en.hasMoreElements()); this.m_count += 1)
/*     */     {
/* 316 */       if (this.m_count % this.m_iteration == 1)
/*     */       {
/* 318 */         this.m_analyzer.incProgress();
/*     */       }
/*     */ 
/* 321 */       String did = (String)en.nextElement();
/* 322 */       Vector v = (Vector)indexMap.get(did);
/* 323 */       if (v != null)
/*     */       {
/* 327 */         if (v.size() > 1)
/*     */         {
/* 330 */           Properties p = (Properties)v.elementAt(0);
/* 331 */           logError("csIDCAnalyzeVerityIndexError", new Object[] { did, p.get("dDocName") });
/*     */ 
/* 333 */           logError("csLinefeed", null);
/*     */ 
/* 335 */           this.m_errorCount += 1;
/*     */         }
/*     */         else
/*     */         {
/* 339 */           indexMap.remove(did);
/* 340 */           revMap.remove(did);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 345 */         Properties props = (Properties)revMap.get(did);
/* 346 */         addMissingIndexEntry(did, props);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addMissingIndexEntry(String did, Properties props)
/*     */   {
/* 353 */     String dReleaseState = (String)props.get("dReleaseState");
/* 354 */     String dStatus = (String)props.get("dStatus");
/*     */ 
/* 356 */     boolean isUseAccount = (SharedObjects.getEnvValueAsBoolean("UseAccounts", false)) || (SharedObjects.getEnvValueAsBoolean("UseCollaboration", false));
/*     */ 
/* 360 */     if (((!dReleaseState.equalsIgnoreCase("Y")) && (!dReleaseState.equalsIgnoreCase("U")) && (!dReleaseState.equalsIgnoreCase("I"))) || 
/* 364 */       (!dStatus.equalsIgnoreCase("RELEASED"))) {
/*     */       return;
/*     */     }
/*     */ 
/* 368 */     this.m_analyzer.writePlain(this.m_revLogName, "IdcService=UPDATE_DOCINFO");
/* 369 */     this.m_analyzer.writePlain(this.m_revLogName, "dDocName=" + props.get("dDocName"));
/*     */ 
/* 371 */     this.m_analyzer.writePlain(this.m_revLogName, "dRevLabel=" + props.get("dRevLabel"));
/*     */ 
/* 373 */     this.m_analyzer.writePlain(this.m_revLogName, "dID=" + did);
/* 374 */     this.m_analyzer.writePlain(this.m_revLogName, "dSecurityGroup=" + props.get("dSecurityGroup"));
/*     */ 
/* 376 */     this.m_analyzer.writePlain(this.m_revLogName, "doFullIndex=1");
/* 377 */     if (isUseAccount)
/*     */     {
/* 379 */       this.m_analyzer.writePlain(this.m_revLogName, "dDocAccount=" + props.get("dDocAccount"));
/*     */     }
/*     */ 
/* 382 */     this.m_analyzer.writePlain(this.m_revLogName, "<<EOD>>");
/*     */ 
/* 384 */     this.m_errors.append(LocaleUtils.encodeMessage("csIDCAnalyzeRevNotFoundRevisions", LocaleResources.localizeMessage("!csLinefeed", null), did));
/*     */ 
/* 388 */     this.m_errorCount += 1;
/*     */   }
/*     */ 
/*     */   protected void analyzeIndexExtras(Hashtable indexMap)
/*     */     throws ServiceException
/*     */   {
/* 396 */     Enumeration en = indexMap.keys();
/* 397 */     for (; en.hasMoreElements(); this.m_count += 1)
/*     */     {
/* 399 */       if (this.m_count % this.m_iteration == 1)
/*     */       {
/* 401 */         this.m_analyzer.incProgress();
/*     */       }
/*     */ 
/* 404 */       String did = (String)en.nextElement();
/* 405 */       Vector v = (Vector)indexMap.get(did);
/*     */ 
/* 408 */       if (v.size() > 1)
/*     */       {
/* 410 */         Properties p = (Properties)v.elementAt(0);
/* 411 */         logError("csIDCAnalyzeVerityIndexError", new Object[] { did, p.get("dDocName") });
/*     */       }
/*     */       else
/*     */       {
/* 416 */         logError("csIDCAnalyzeIndexNotFoundRevisions", did);
/* 417 */         logError("csLinefeed", null);
/*     */ 
/* 419 */         Properties props = (Properties)v.elementAt(0);
/* 420 */         if ((props != null) && 
/* 422 */           (StringUtils.convertToBool((String)this.m_context.get("CleanIndex"), false)))
/*     */         {
/* 425 */           preparePropertiesForIndexDelete(props);
/* 426 */           IndexerInfo info = createIndexInfoForIndexDelete(props);
/*     */ 
/* 428 */           this.m_data.m_indexer.prepare();
/* 429 */           this.m_data.m_driver.checkConnection();
/* 430 */           this.m_data.m_indexer.indexDocument(props, info);
/* 431 */           logError("csIDCAnalyzeIndexDeletedRevision", did);
/* 432 */           logError("csLinefeed", null);
/*     */ 
/* 434 */           this.m_errorsFixed += 1;
/*     */         }
/*     */       }
/* 397 */       this.m_errorCount += 1;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void preparePropertiesForIndexDelete(Properties props)
/*     */   {
/* 443 */     String tmpDocName = props.getProperty("dDocName");
/* 444 */     props.put("VdkVgwKey", tmpDocName.toLowerCase());
/* 445 */     props.put("webFormat", "txt");
/*     */   }
/*     */ 
/*     */   protected IndexerInfo createIndexInfoForIndexDelete(Properties props)
/*     */   {
/* 450 */     IndexerInfo info = new IndexerInfo();
/* 451 */     info.m_isDelete = true;
/* 452 */     String tmpDocName = props.getProperty("dDocName");
/* 453 */     info.m_indexKey = tmpDocName.toLowerCase();
/* 454 */     return info;
/*     */   }
/*     */ 
/*     */   protected DataResultSet retrieveSearchResults(int lo, int hi, int numResults)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 465 */     String sortOrder = "Desc";
/* 466 */     String queryText = "dID >= `" + lo + "`";
/*     */ 
/* 470 */     if (hi > lo)
/*     */     {
/* 473 */       queryText = queryText + " <AND> dID < `" + hi + "`";
/* 474 */       sortOrder = "Asc";
/*     */     }
/* 476 */     return retrieveSearchResults(queryText, sortOrder, numResults);
/*     */   }
/*     */ 
/*     */   protected DataResultSet retrieveSearchResults(String queryText, String sortOrder, int numResults)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 485 */     DataBinder binder = new DataBinder();
/* 486 */     binder.putLocal("SearchQueryFormat", "Universal");
/* 487 */     binder.putLocal("SortField", "dID");
/* 488 */     binder.putLocal("SortOrder", sortOrder);
/* 489 */     this.m_searchService.setCachedObject("WorkSpace", this.m_workspace);
/*     */ 
/* 491 */     queryText = this.m_queryConfig.prepareQueryText(queryText, binder, this.m_searchService);
/*     */ 
/* 493 */     queryText = this.m_queryConfig.fixUpAndValidateQuery(queryText, binder, this.m_searchService);
/*     */ 
/* 496 */     binder.putLocal("QueryText", queryText);
/* 497 */     binder.putLocal("ResultCount", numResults + "");
/* 498 */     binder.setEnvironmentValue("MaxResults", numResults + "");
/*     */ 
/* 501 */     this.m_searchService.m_fieldNames = "dID, dDocName";
/* 502 */     this.m_searchService.m_numFieldNames = 2;
/* 503 */     binder.m_blDateFormat = LocaleResources.m_iso8601Format;
/*     */ 
/* 505 */     this.m_searchService.doLocalSearch(binder);
/*     */ 
/* 507 */     DataResultSet drset = (DataResultSet)binder.getResultSet("SearchResults");
/* 508 */     if (drset == null)
/*     */     {
/* 510 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeSearchResultsNotFound", new Object[0]));
/*     */     }
/*     */ 
/* 513 */     return drset;
/*     */   }
/*     */ 
/*     */   protected void logError(String msg, Object obj)
/*     */   {
/* 518 */     Object[] args = null;
/* 519 */     if (obj instanceof Object[])
/*     */     {
/* 521 */       args = (Object[])(Object[])obj;
/*     */     }
/*     */     else
/*     */     {
/* 525 */       args = new Object[] { obj };
/*     */     }
/* 527 */     if (SystemUtils.m_verbose)
/*     */     {
/* 529 */       Report.debug("analyzer", LocaleUtils.encodeMessage(msg, null, args), null);
/*     */     }
/*     */ 
/* 532 */     this.m_errors.append(LocaleUtils.encodeMessage(msg, null, args));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 537 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98781 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IndexCheck
 * JD-Core Version:    0.5.4
 */