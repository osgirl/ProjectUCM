/*      */ package intradoc.search;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.shared.CommonSearchEngineConfig;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SearchCache
/*      */   implements Runnable
/*      */ {
/*  145 */   public static int F_NEW_CACHE = 0;
/*  146 */   public static int F_UPDATE_CACHE = 1;
/*      */ 
/*  151 */   public static int F_LOCAL_CACHE_CHANGED = 1;
/*  152 */   public static int F_FLUSH_CACHE = 2;
/*  153 */   public static int F_FULL_FLUSH = 4;
/*  154 */   public static int F_USER_SECURITY_CHANGED = 8;
/*  155 */   public static int F_USE_PASSED_IN_AGE_COUNTER = 16;
/*      */ 
/*  160 */   public static int F_MERGE_RESULTS_INTO_PARAMETERS = 1;
/*  161 */   public static int F_CACHE_TOO_OLD = 2;
/*  162 */   public static int F_WAIT_FOR_CACHE_TO_BUILD = 4;
/*  163 */   public static int F_ALLOW_DIFFERENCE_UPDATE = 8;
/*  164 */   public static int F_DO_CHANGE_DIFFERENCE_UPDATE = 16;
/*  165 */   public static int F_DO_DELETE_CHANGE_TESTS_ONLY = 32;
/*  166 */   public static int F_DO_CACHE_CHANGE_REPAIR = 64;
/*  167 */   public static int F_DO_DIFFERENTIAL_REPAIR = 128;
/*  168 */   public static int F_DID_DIFFERENCE_UPDATE = 256;
/*  169 */   public static int F_LOAD_RESULTS_AGAIN = 512;
/*  170 */   public static int F_CHECK_FOR_DELETE_ON_NEW_ROWS = 1024;
/*      */ 
/*  174 */   public static int ADD_CACHE_OBJECT = 1;
/*  175 */   public static int UPDATE_CACHE_OBJECT = 2;
/*  176 */   public static int DELETE_CACHE_OBJECT = 3;
/*      */   protected List<SearchCacheEvent> m_searchCacheEvents;
/*      */   protected Map<String, ProviderCache> m_cacheByProvider;
/*      */   protected Vector m_invalidatedCaches;
/*      */   protected CacheObject m_lru;
/*      */   protected CacheObject m_mru;
/*      */   public int m_targetCachedItems;
/*      */   public int m_docInfoItemMultiplier;
/*      */   protected int m_currentCachedObjectCount;
/*      */   protected int m_currentCachedResultRowCount;
/*      */   protected int m_currentCachedDocInfoCount;
/*      */   protected int m_searchCacheCleanerInterval;
/*      */   protected int m_searchCacheMaxItemAge;
/*  209 */   protected boolean[] m_statusSync = { false };
/*      */   protected int m_priorCleanerCachedItemCount;
/*      */   protected int m_hitCount;
/*      */   protected int m_docInfoHitCount;
/*      */   protected int m_missCount;
/*      */   protected int m_docInfoMissCount;
/*      */   protected int m_hitRowCount;
/*      */   protected int m_missRowCount;
/*      */   protected int m_oldCacheHitCount;
/*      */   protected int m_docInfoOldCacheHitCount;
/*      */   protected int m_searchConnections;
/*  222 */   protected boolean m_useOldCacheItems = false;
/*  223 */   protected int m_waitForCacheToBuildInSeconds = 30;
/*      */ 
/*  225 */   protected int m_useOldCacheItemsMultiplier = 50;
/*      */ 
/*  228 */   protected int m_numWaitingForBuild = 0;
/*      */   protected int m_debugOldCacheIntervalSeconds;
/*  233 */   protected boolean m_invalidatePendingCaches = false;
/*      */ 
/*      */   public SearchCache()
/*      */   {
/*  238 */     this.m_docInfoItemMultiplier = SharedObjects.getEnvironmentInt("CachedDocInfoCountMultiplier", 50);
/*      */ 
/*  240 */     this.m_targetCachedItems = (SharedObjects.getEnvironmentInt("CachedResultRowCount", 40000) * this.m_docInfoItemMultiplier);
/*      */ 
/*  242 */     this.m_targetCachedItems = SharedObjects.getEnvironmentInt("CachedResultItemsMaximum", this.m_targetCachedItems);
/*      */ 
/*  244 */     this.m_searchCacheCleanerInterval = SharedObjects.getTypedEnvironmentInt("SearchCacheCleanerInterval", 120, 24, 24);
/*      */ 
/*  246 */     this.m_searchCacheMaxItemAge = SharedObjects.getTypedEnvironmentInt("SearchCacheMaxItemAgeInMinutes", 240, 19, 19);
/*      */ 
/*  248 */     this.m_useOldCacheItems = SharedObjects.getEnvValueAsBoolean("UseOldSearchCaches", false);
/*  249 */     this.m_waitForCacheToBuildInSeconds = SharedObjects.getTypedEnvironmentInt("WaitForSearchCacheToBuildInSeconds", 30, 24, 24);
/*      */ 
/*  251 */     this.m_useOldCacheItemsMultiplier = SharedObjects.getEnvironmentInt("UseOldSearchCacheItemsMultiplier", 10);
/*      */ 
/*  253 */     this.m_debugOldCacheIntervalSeconds = SharedObjects.getTypedEnvironmentInt("SearchOldCacheDebugIntervalSeconds", 0, 18, 24);
/*      */ 
/*  255 */     this.m_invalidatePendingCaches = SharedObjects.getEnvValueAsBoolean("UsePendingOldSearchCaches", false);
/*      */ 
/*  257 */     init();
/*      */ 
/*  259 */     Thread t = new Thread(this, "SearchCache");
/*  260 */     t.setDaemon(true);
/*  261 */     t.start();
/*      */   }
/*      */ 
/*      */   public int getNumQueriesActive()
/*      */   {
/*  266 */     return this.m_numWaitingForBuild;
/*      */   }
/*      */ 
/*      */   protected synchronized void init()
/*      */   {
/*  271 */     this.m_priorCleanerCachedItemCount = -1;
/*  272 */     this.m_currentCachedObjectCount = (this.m_currentCachedResultRowCount = this.m_currentCachedDocInfoCount = 0);
/*  273 */     this.m_hitCount = (this.m_docInfoHitCount = this.m_missCount = this.m_docInfoMissCount = 0);
/*  274 */     this.m_hitRowCount = (this.m_missRowCount = 0);
/*  275 */     this.m_oldCacheHitCount = (this.m_docInfoOldCacheHitCount = 0);
/*  276 */     this.m_searchConnections = 0;
/*  277 */     this.m_searchCacheEvents = new ArrayList();
/*  278 */     this.m_cacheByProvider = new HashMap();
/*  279 */     this.m_invalidatedCaches = new IdcVector();
/*  280 */     this.m_lru = (this.m_mru = null);
/*      */ 
/*  282 */     Report.trace("monitor", null, "csMonitorSearchHitCount", new Object[] { Integer.valueOf(this.m_hitCount) });
/*  283 */     Report.trace("monitor", null, "csMonitorSearchMissCount", new Object[] { Integer.valueOf(this.m_missCount) });
/*  284 */     Report.trace("monitor", null, "csMonitorSearchCachedQueries", new Object[] { Integer.valueOf(this.m_docInfoItemMultiplier * this.m_currentCachedDocInfoCount + this.m_currentCachedResultRowCount + 2 * this.m_currentCachedObjectCount) });
/*      */   }
/*      */ 
/*      */   public void run()
/*      */   {
/*  291 */     message("Initializing cache background maintenance thread");
/*  292 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/*  293 */     while (!SystemUtils.m_isServerStopped)
/*      */     {
/*  296 */       Object Ljava/lang/Object; = this; monitorenter;
/*      */       Vector localInvalidated;
/*      */       ExecutionContext ctxt;
/*      */       try
/*      */       {
/*  300 */         if ((this.m_invalidatedCaches.size() == 0) && (this.m_searchCacheEvents.size() == 0))
/*      */         {
/*  302 */           SystemUtils.wait(this, this.m_searchCacheCleanerInterval * 1000);
/*      */         }
/*  304 */         localInvalidated = this.m_invalidatedCaches;
/*  305 */         this.m_invalidatedCaches = new IdcVector();
/*      */       }
/*      */       catch (InterruptedException ignore)
/*      */       {
/*  309 */         monitorexit; return;
/*      */ 
/*  311 */         monitorexit; } finally { monitorexit; }
/*      */ 
/*      */ 
/*  316 */       int size = localInvalidated.size();
/*      */       int i;
/*  317 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  319 */         ProviderCache cache = (ProviderCache)localInvalidated.elementAt(i);
/*  320 */         Set entries = cache.m_queryCaches.entrySet();
/*  321 */         for (Map.Entry entry : entries)
/*      */         {
/*  323 */           CacheObject obj = (CacheObject)entry.getValue();
/*  324 */           removeCacheEntry(obj);
/*      */         }
/*      */       }
/*  327 */       int curNumItems = this.m_docInfoItemMultiplier * this.m_currentCachedDocInfoCount + this.m_currentCachedResultRowCount + 2 * this.m_currentCachedObjectCount;
/*      */ 
/*  330 */       if (this.m_priorCleanerCachedItemCount != curNumItems)
/*      */       {
/*  332 */         if (SystemUtils.isActiveTrace("searchcache"))
/*      */         {
/*  334 */           String msg = LocaleUtils.encodeMessage("csCacheCapacity", null, "" + curNumItems * 100 / this.m_targetCachedItems);
/*      */ 
/*  336 */           msg = LocaleResources.localizeMessage(msg, ctxt);
/*  337 */           message(msg);
/*      */         }
/*  339 */         this.m_priorCleanerCachedItemCount = curNumItems;
/*      */       }
/*      */ 
/*  342 */       long curTime = System.currentTimeMillis();
/*  343 */       long ageLimit = curTime - this.m_searchCacheMaxItemAge * 60 * 1000;
/*  344 */       boolean isExpired = true;
/*  345 */       while (isExpired)
/*      */       {
/*  348 */         isExpired = false;
/*  349 */         boolean isTooMany = curNumItems > this.m_targetCachedItems;
/*  350 */         if (isTooMany)
/*      */         {
/*  352 */           isExpired = true;
/*      */         }
/*  354 */         if (this.m_lru != null)
/*      */         {
/*  356 */           if (!isExpired)
/*      */           {
/*  358 */             isExpired = this.m_lru.m_lastAccessTime < ageLimit;
/*      */           }
/*  360 */           if (isExpired)
/*      */           {
/*  362 */             if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("searchcache")))
/*      */             {
/*  364 */               String msg = "Removing cache item because too old (" + (ageLimit - this.m_lru.m_lastAccessTime) / 1000L + " seconds) " + this.m_lru;
/*      */ 
/*  369 */               debug(msg);
/*      */             }
/*  371 */             removeCacheEntry(this.m_lru);
/*      */           }
/*      */ 
/*      */         }
/*  376 */         else if (isExpired)
/*      */         {
/*  378 */           Report.error("system", null, "csCacheInconsistent", new Object[] { "" + curNumItems });
/*  379 */           init();
/*  380 */           break;
/*      */         }
/*      */ 
/*  383 */         curNumItems = this.m_docInfoItemMultiplier * this.m_currentCachedDocInfoCount + this.m_currentCachedResultRowCount + 2 * this.m_currentCachedObjectCount;
/*      */       }
/*      */ 
/*  387 */       Report.trace("monitor", null, "csMonitorSearchCachedQueries", new Object[] { Integer.valueOf(curNumItems) });
/*      */ 
/*  390 */       processCacheEvents(ctxt);
/*      */ 
/*  393 */       Providers.releaseConnections();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void processCacheEvents(ExecutionContext cxt)
/*      */   {
/*  402 */     boolean doEvents = true;
/*  403 */     while (doEvents)
/*      */     {
/*  405 */       SearchCacheEvent nextEvent = null;
/*  406 */       synchronized (this)
/*      */       {
/*  408 */         if (this.m_searchCacheEvents.size() > 0)
/*      */         {
/*  410 */           nextEvent = (SearchCacheEvent)this.m_searchCacheEvents.remove(0);
/*      */         }
/*      */       }
/*  413 */       if (nextEvent != null)
/*      */       {
/*  415 */         message("Starting processing cache event");
/*  416 */         nextEvent.doEvent(cxt);
/*  417 */         message("Finished processing cache event");
/*      */       }
/*      */       else
/*      */       {
/*  421 */         doEvents = false;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkCache(CacheUpdateParameters updateParameters, int first, int count)
/*      */   {
/*  439 */     synchronized (this.m_statusSync)
/*      */     {
/*  441 */       this.m_searchConnections += 1;
/*      */     }
/*      */     try
/*      */     {
/*  445 */       updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_LIST;
/*  446 */       updateParameters.m_timeoutWaitingForCache = (this.m_waitForCacheToBuildInSeconds * 1000);
/*  447 */       if ((updateParameters.m_isLocalSearch) && (updateParameters.m_changeData == null))
/*      */       {
/*  449 */         SearchDocChangeData globalData = SearchDocChangeUtils.getOrCreateGlobalSearchChangeData(updateParameters.m_requestBinder, updateParameters.m_cxt);
/*      */ 
/*  451 */         if (globalData.m_differentialUpdatesAllowed)
/*      */         {
/*  453 */           SearchDocChangeData dataCopy = new SearchDocChangeData();
/*  454 */           SearchDocChangeUtils.getChangeFastData(globalData, dataCopy);
/*  455 */           updateParameters.m_changeData = dataCopy;
/*      */         }
/*      */       }
/*      */ 
/*  459 */       SearchCacheUtils.computeAllowedParsedQueryOptions(updateParameters);
/*      */ 
/*  461 */       checkCacheImplement(updateParameters, first, count);
/*  462 */       if (updateParameters.m_isValidCache)
/*      */       {
/*  464 */         CacheObject cacheEntry = updateParameters.m_activeCacheObject;
/*  465 */         int[] docsListSync = cacheEntry.m_docsListSync;
/*      */ 
/*  476 */         synchronized (docsListSync)
/*      */         {
/*  478 */           int docListFlags = updateParameters.m_cacheUpdateStateFlags;
/*  479 */           int currentAge = updateParameters.m_capturedCurrentAge;
/*  480 */           boolean doDifferenceUpdate = (docListFlags & F_DO_CHANGE_DIFFERENCE_UPDATE) != 0;
/*  481 */           if (doDifferenceUpdate)
/*      */           {
/*  483 */             synchronized (this)
/*      */             {
/*  486 */               doDifferenceUpdate = (updateParameters.m_cacheGenerationalCounter == cacheEntry.m_generationalCounter) && (updateParameters.m_updateListSyncCount == docsListSync[0]);
/*      */ 
/*  491 */               if (!doDifferenceUpdate)
/*      */               {
/*  493 */                 synchronized (cacheEntry)
/*      */                 {
/*  499 */                   docListFlags |= F_LOAD_RESULTS_AGAIN;
/*  500 */                   updateParameters.m_cacheUpdateStateFlags = docListFlags;
/*  501 */                   if (cacheEntry.m_isValid)
/*      */                   {
/*  503 */                     mergeResultsToParameters(first, count, cacheEntry, currentAge, updateParameters);
/*      */ 
/*  505 */                     docListFlags = updateParameters.m_cacheUpdateStateFlags;
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*  511 */           SearchListItem[] docList = updateParameters.m_cacheDocsList;
/*  512 */           if ((docList != null) && (docList.length > 0))
/*      */           {
/*  514 */             if (docList.length > 0)
/*      */             {
/*  517 */               fillSearchListRowsWithMetadata(updateParameters, docList);
/*  518 */               docListFlags = updateParameters.m_cacheUpdateStateFlags;
/*      */             }
/*      */             else
/*      */             {
/*  523 */               updateParameters.m_cacheIsComplete = true;
/*      */             }
/*      */           }
/*      */ 
/*  527 */           boolean madeChange = false;
/*  528 */           if ((doDifferenceUpdate) && (updateParameters.m_isValidCache) && 
/*  533 */             (SearchCacheUtils.checkAndCaptureDocListChanges(updateParameters)))
/*      */           {
/*  535 */             SearchCacheUtils.mergeAndSortCachedDocList(updateParameters);
/*  536 */             madeChange = true;
/*      */           }
/*      */ 
/*  539 */           if ((!updateParameters.m_isValidCache) || (updateParameters.m_cacheRowNeededQueryRetest))
/*      */           {
/*  541 */             madeChange = true;
/*      */           }
/*  543 */           if (madeChange)
/*      */           {
/*  545 */             docListFlags |= F_DID_DIFFERENCE_UPDATE;
/*      */           }
/*      */ 
/*  550 */           updateParameters.m_cacheUpdateStateFlags = docListFlags;
/*      */ 
/*  553 */           finishUpFirstPassUpdatingCache(first, count, cacheEntry, currentAge, updateParameters);
/*      */         }
/*      */       }
/*  556 */       if (!updateParameters.m_isValidCache)
/*      */       {
/*  559 */         updateParameters.m_cacheIsComplete = false;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  564 */       synchronized (this.m_statusSync)
/*      */       {
/*  566 */         this.m_searchConnections -= 1;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void fillSearchListRowsWithMetadata(CacheUpdateParameters updateParameters, SearchListItem[] docList)
/*      */   {
/*  574 */     List missingDocsList = null;
/*  575 */     Map pendingMissingDocs = null;
/*  576 */     boolean isComplete = true;
/*  577 */     int docListFlags = updateParameters.m_cacheUpdateStateFlags;
/*  578 */     boolean doDifferenceUpdate = (docListFlags & F_DO_CHANGE_DIFFERENCE_UPDATE) != 0;
/*  579 */     boolean allowDifferenceUpdate = (docListFlags & F_ALLOW_DIFFERENCE_UPDATE) != 0;
/*  580 */     String currentQuery = updateParameters.m_query;
/*  581 */     boolean isCurrentOldCache = updateParameters.m_isOldCache;
/*  582 */     boolean isValidDocListCache = true;
/*      */     try
/*      */     {
/*  585 */       CacheRow[] docInfo = new CacheRow[docList.length];
/*  586 */       for (int i = 0; i < docList.length; ++i)
/*      */       {
/*  588 */         if (docList[i] == null)
/*      */         {
/*  590 */           Report.trace("system", "Missing entry in search cache list, cache integrity failed, updateParameters=" + updateParameters, null);
/*      */         }
/*      */         else
/*      */         {
/*  594 */           String canonicalDocName = SearchCacheUtils.calculateCanonicalDocName(updateParameters, docList[i].m_key);
/*      */ 
/*  596 */           String docKey = SearchCacheUtils.calculateDocNameDummyQuery(updateParameters, canonicalDocName);
/*      */ 
/*  599 */           updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_INFO;
/*  600 */           updateParameters.m_query = docKey;
/*      */ 
/*  603 */           checkCacheImplement(updateParameters, 1, 1);
/*  604 */           if (updateParameters.m_isValidCache)
/*      */           {
/*  606 */             CacheRow newRow = updateParameters.m_cacheDocInfo;
/*  607 */             if ((updateParameters.m_cacheGenerationalCounter < newRow.m_generationalCounter) && 
/*  609 */               (!doDifferenceUpdate))
/*      */             {
/*  611 */               isValidDocListCache = false;
/*  612 */               break;
/*      */             }
/*      */ 
/*  615 */             docInfo[i] = newRow;
/*      */           }
/*      */           else
/*      */           {
/*  627 */             if ((doDifferenceUpdate) || (!allowDifferenceUpdate) || (!updateParameters.m_allowCheckRowInclude))
/*      */             {
/*  632 */               isValidDocListCache = false;
/*  633 */               break;
/*      */             }
/*  635 */             if (missingDocsList == null)
/*      */             {
/*  637 */               missingDocsList = new ArrayList();
/*      */             }
/*  639 */             if (pendingMissingDocs == null)
/*      */             {
/*  641 */               pendingMissingDocs = new HashMap();
/*      */             }
/*  643 */             missingDocsList.add(docList[i]);
/*  644 */             if (pendingMissingDocs.get(docKey) != null)
/*      */             {
/*  646 */               message("The key " + docList[i] + " is listed more than once for the results of the query " + currentQuery);
/*      */ 
/*  649 */               isValidDocListCache = false;
/*  650 */               break;
/*      */             }
/*  652 */             pendingMissingDocs.put(docKey, new Integer(i));
/*  653 */             isComplete = false;
/*      */           }
/*      */         }
/*      */       }
/*  656 */       updateParameters.m_cacheDocsListMetadata = docInfo;
/*      */     }
/*      */     finally
/*      */     {
/*  662 */       updateParameters.m_cacheUpdateStateFlags = docListFlags;
/*  663 */       updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_LIST;
/*  664 */       updateParameters.m_query = currentQuery;
/*  665 */       updateParameters.m_isOldCache = isCurrentOldCache;
/*  666 */       updateParameters.m_isValidCache = isValidDocListCache;
/*  667 */       if (!isValidDocListCache)
/*      */       {
/*  669 */         isComplete = false;
/*      */       }
/*      */     }
/*  672 */     if (missingDocsList != null)
/*      */     {
/*  674 */       updateParameters.m_missingNames = ((SearchListItem[])missingDocsList.toArray(new SearchListItem[missingDocsList.size()]));
/*      */     }
/*      */     else
/*      */     {
/*  679 */       updateParameters.m_missingNames = null;
/*      */     }
/*  681 */     updateParameters.m_unresolvedMissingNames = pendingMissingDocs;
/*  682 */     updateParameters.m_cacheIsComplete = isComplete;
/*      */   }
/*      */ 
/*      */   public void checkCacheImplement(CacheUpdateParameters updateParameters, int first, int count)
/*      */   {
/*  701 */     CacheObject cacheEntry = null;
/*  702 */     ProviderCache providerCache = updateParameters.m_providerCache;
/*  703 */     String query = updateParameters.m_query;
/*  704 */     int updateType = updateParameters.m_updateType;
/*  705 */     boolean isDocInfo = updateType == CacheObject.CACHE_IS_DOC_INFO;
/*  706 */     int currentAge = 0;
/*      */ 
/*  708 */     synchronized (this)
/*      */     {
/*  710 */       if (SystemUtils.m_verbose)
/*      */       {
/*  712 */         debug("Check cache for query " + query + ", m_searchConnections=" + this.m_searchConnections + " m_numWaitingForBuild=" + this.m_numWaitingForBuild + " allowOldCache=" + updateParameters.m_allowOldCache + " preferOldCache=" + updateParameters.m_preferOldCache);
/*      */       }
/*      */ 
/*  717 */       if ((providerCache == null) || (providerCache.m_isInvalidated) || (updateParameters.m_providerName == null))
/*      */       {
/*  720 */         loadProviderCacheReference(updateParameters);
/*  721 */         providerCache = updateParameters.m_providerCache;
/*      */       }
/*  723 */       if (!updateParameters.m_updateGenerationalCounterComputed)
/*      */       {
/*  725 */         updateParameters.m_updateGenerationalCounter = (providerCache.m_generationalSearchCounter++);
/*  726 */         updateParameters.m_updateGenerationalCounterComputed = true;
/*      */       }
/*  728 */       Map cache = providerCache.m_queryCaches;
/*  729 */       currentAge = providerCache.m_currentAge;
/*      */ 
/*  731 */       cacheEntry = (CacheObject)cache.get(query);
/*      */ 
/*  733 */       if (cacheEntry != null)
/*      */       {
/*  735 */         boolean isEmptyListQuery = (!isDocInfo) && (((cacheEntry.m_docsList == null) || (cacheEntry.m_docsList.length == 0)));
/*      */ 
/*  737 */         if ((!isEmptyListQuery) && (((updateParameters.m_cacheDataDesign == null) || (cacheEntry.m_dataDesign == null) || (!updateParameters.m_cacheDataDesign.equals(cacheEntry.m_dataDesign)))))
/*      */         {
/*  741 */           cacheEntry = null;
/*      */         }
/*      */       }
/*  744 */       if (cacheEntry == null)
/*      */       {
/*  746 */         createOrUpdateCacheEntry(updateParameters, null, F_NEW_CACHE);
/*      */ 
/*  749 */         mergeResultsToParameters(first, count, cacheEntry, currentAge, updateParameters);
/*      */       }
/*      */       else
/*      */       {
/*  753 */         checkCurrentStateOfCache(first, count, cacheEntry, updateParameters, currentAge);
/*      */       }
/*      */     }
/*      */ 
/*  757 */     boolean waitingForCacheToBuild = (updateParameters.m_cacheUpdateStateFlags & F_WAIT_FOR_CACHE_TO_BUILD) != 0;
/*      */ 
/*  759 */     if (waitingForCacheToBuild)
/*      */     {
/*  761 */       waitForCacheToBuild(cacheEntry, updateParameters);
/*  762 */       synchronized (this)
/*      */       {
/*  764 */         synchronized (cacheEntry)
/*      */         {
/*  766 */           mergeResultsToParameters(first, count, cacheEntry, currentAge, updateParameters);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  772 */     if ((!isDocInfo) && (updateParameters.m_isValidCache))
/*      */       return;
/*  774 */     finishUpFirstPassUpdatingCache(first, count, cacheEntry, currentAge, updateParameters);
/*      */   }
/*      */ 
/*      */   protected void checkCurrentStateOfCache(int first, int count, CacheObject cacheEntry, CacheUpdateParameters updateParameters, int currentAge)
/*      */   {
/*  781 */     boolean isCacheTooOld = false;
/*  782 */     long cacheTime = -1L;
/*  783 */     long expirationTime = -1L;
/*  784 */     boolean doOldCacheTest = false;
/*  785 */     boolean allowDifferenceUpdate = false;
/*  786 */     boolean doDifferenceUpdate = false;
/*  787 */     boolean doCacheChangeRepair = false;
/*  788 */     boolean doDifferentialRepair = false;
/*  789 */     boolean waitingForCacheToBuild = false;
/*  790 */     int flags = 0;
/*  791 */     ProviderCache providerCache = updateParameters.m_providerCache;
/*  792 */     int updateType = updateParameters.m_updateType;
/*  793 */     boolean isDocInfo = updateType == CacheObject.CACHE_IS_DOC_INFO;
/*  794 */     boolean preferOldCache = updateParameters.m_preferOldCache;
/*  795 */     boolean allowOldCache = updateParameters.m_allowOldCache;
/*      */ 
/*  797 */     ParsedQueryElements queryElements = updateParameters.m_parsedQueryElements;
/*  798 */     CommonSearchEngineConfig eConfig = updateParameters.m_searchConfig;
/*  799 */     if ((eConfig != null) && (queryElements != null) && 
/*  801 */       (eConfig.m_allowCacheDiffUpdates) && (eConfig.m_cacheDiffUpdateLevel != 0))
/*      */     {
/*  804 */       allowDifferenceUpdate = true;
/*  805 */       doCacheChangeRepair = (eConfig.m_cacheDiffUpdateLevel == 2) && (!queryElements.m_hasFullTextElement);
/*      */ 
/*  808 */       doDifferentialRepair = doCacheChangeRepair;
/*      */     }
/*      */ 
/*  812 */     synchronized (cacheEntry)
/*      */     {
/*  814 */       if ((cacheEntry.m_ageCounter < currentAge) && (cacheEntry.m_isValid) && (allowDifferenceUpdate) && (!isDocInfo))
/*      */       {
/*  818 */         if ((updateParameters.m_allowCheckRowExclude) && (updateParameters.m_changeData != null) && (updateParameters.m_changeData.m_isValidFastData) && (!isDocInfo) && 
/*  821 */           (cacheEntry.m_sharedSearchTime > updateParameters.m_changeData.m_fastChangesStartTime))
/*      */         {
/*  826 */           updateParameters.m_cacheSharedSearchTime = cacheEntry.m_sharedSearchTime;
/*  827 */           doDifferenceUpdate = true;
/*      */         }
/*      */ 
/*  831 */         if (!doDifferenceUpdate)
/*      */         {
/*  833 */           doOldCacheTest = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  838 */       if (doOldCacheTest)
/*      */       {
/*  840 */         if (allowOldCache)
/*      */         {
/*  842 */           int timeExpirationDiff = -1;
/*  843 */           long currentTime = System.currentTimeMillis();
/*      */ 
/*  845 */           if (providerCache.m_invalidationTimes != null)
/*      */           {
/*  847 */             int lookupIndex = currentAge - cacheEntry.m_ageCounter - 1;
/*      */ 
/*  849 */             if ((lookupIndex >= 0) && (lookupIndex < providerCache.m_invalidationTimes.length))
/*      */             {
/*  852 */               cacheTime = providerCache.m_invalidationTimes[lookupIndex];
/*      */             }
/*      */           }
/*  855 */           if (cacheTime < 0L)
/*      */           {
/*  858 */             cacheTime = cacheEntry.m_lastAccessTime;
/*      */           }
/*  860 */           int mult = this.m_numWaitingForBuild;
/*      */ 
/*  867 */           if (this.m_numWaitingForBuild >= 10)
/*      */           {
/*  869 */             if (preferOldCache)
/*      */             {
/*  871 */               mult *= mult / 4;
/*      */             }
/*      */             else
/*      */             {
/*  875 */               mult *= 4;
/*      */             }
/*      */           }
/*      */ 
/*  879 */           timeExpirationDiff = this.m_useOldCacheItemsMultiplier * mult * 1000;
/*  880 */           if (cacheEntry.m_isPending)
/*      */           {
/*  886 */             timeExpirationDiff /= 4;
/*      */           }
/*  888 */           timeExpirationDiff += this.m_debugOldCacheIntervalSeconds;
/*  889 */           expirationTime = currentTime - timeExpirationDiff;
/*      */         }
/*  891 */         if ((!allowOldCache) || (cacheTime < expirationTime) || ((cacheTime < 0L) && (expirationTime < 0L)))
/*      */         {
/*  894 */           isCacheTooOld = true;
/*  895 */           if ((SystemUtils.isActiveTrace("searchcache")) && ((
/*  897 */             (cacheTime < expirationTime) || (cacheTime < 0L) || (expirationTime < 0L))))
/*      */           {
/*  899 */             String msg = null;
/*  900 */             if (cacheTime < 0L)
/*      */             {
/*  902 */               msg = "Timestamp of old cache could not be computed";
/*      */             }
/*  904 */             else if (expirationTime < 0L)
/*      */             {
/*  906 */               msg = "Time for expiration of old cache could not be computed";
/*      */             }
/*      */             else
/*      */             {
/*  910 */               String cacheDateStr = LocaleUtils.debugDate(cacheTime);
/*  911 */               String expirationDateStr = LocaleUtils.debugDate(expirationTime);
/*  912 */               msg = "Not using old cache, timestamp on cache " + cacheDateStr + " is before earliest date allowable " + expirationDateStr;
/*      */             }
/*      */ 
/*  915 */             message(msg);
/*      */           }
/*      */ 
/*  921 */           if (this.m_invalidatePendingCaches)
/*      */           {
/*  923 */             cacheEntry.m_ageCounter = currentAge;
/*  924 */             cacheEntry.m_isValid = false;
/*      */           }
/*      */ 
/*      */         }
/*  929 */         else if (SystemUtils.isActiveTrace("searchcache"))
/*      */         {
/*  931 */           String cacheStr = LocaleResources.m_iso8601Format.format(new Date(cacheTime), 2);
/*      */ 
/*  933 */           String expStr = LocaleResources.m_iso8601Format.format(new Date(expirationTime), 2);
/*      */ 
/*  935 */           String msg = "Old cache is useable, timestamp on cache " + cacheStr + " is after earliest date allowable " + expStr;
/*      */ 
/*  937 */           message(msg);
/*      */         }
/*      */ 
/*  941 */         if (isCacheTooOld)
/*      */         {
/*  943 */           if (cacheEntry.m_isPending)
/*      */           {
/*  945 */             waitingForCacheToBuild = true;
/*      */           }
/*      */           else
/*      */           {
/*  949 */             markPendingCache(cacheEntry, updateParameters);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  954 */         if ((cacheEntry.m_isPending) && (!cacheEntry.m_isValid) && (!isCacheTooOld))
/*      */         {
/*  956 */           waitingForCacheToBuild = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  964 */       if (allowDifferenceUpdate)
/*      */       {
/*  966 */         flags |= F_ALLOW_DIFFERENCE_UPDATE;
/*      */       }
/*  968 */       if (doDifferenceUpdate)
/*      */       {
/*  970 */         flags |= F_DO_CHANGE_DIFFERENCE_UPDATE;
/*      */       }
/*  972 */       if (doCacheChangeRepair)
/*      */       {
/*  974 */         flags |= F_DO_CACHE_CHANGE_REPAIR;
/*      */       }
/*  976 */       if (doDifferentialRepair)
/*      */       {
/*  978 */         flags |= F_DO_DIFFERENTIAL_REPAIR;
/*      */       }
/*  980 */       if (isCacheTooOld)
/*      */       {
/*  982 */         flags |= F_CACHE_TOO_OLD;
/*      */       }
/*  984 */       if (waitingForCacheToBuild)
/*      */       {
/*  986 */         flags |= F_WAIT_FOR_CACHE_TO_BUILD;
/*      */       }
/*  988 */       updateParameters.m_cacheUpdateStateFlags = flags;
/*      */ 
/*  990 */       if (!waitingForCacheToBuild)
/*      */       {
/*  995 */         mergeResultsToParameters(first, count, cacheEntry, currentAge, updateParameters);
/*      */       }
/*      */ 
/*  999 */       if ((((cacheEntry.m_isValid) || (waitingForCacheToBuild))) && (!isDocInfo))
/*      */       {
/* 1010 */         updateParameters.m_activeCacheObject = cacheEntry;
/* 1011 */         updateParameters.m_capturedCurrentAge = currentAge;
/* 1012 */         updateParameters.m_cacheGenerationalCounter = cacheEntry.m_generationalCounter;
/*      */ 
/* 1014 */         if (cacheEntry.m_docsListSync == null)
/*      */         {
/* 1016 */           cacheEntry.m_docsListSync = new int[] { 0 };
/*      */         }
/* 1018 */         updateParameters.m_updateListSyncCount = cacheEntry.m_docsListSync[0];
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void waitForCacheToBuild(CacheObject cacheEntry, CacheUpdateParameters updateParameters)
/*      */   {
/* 1025 */     String providerName = updateParameters.m_providerName;
/* 1026 */     String query = updateParameters.m_query;
/* 1027 */     int flags = updateParameters.m_cacheUpdateStateFlags;
/* 1028 */     boolean isCacheTooOld = (flags & F_CACHE_TOO_OLD) != 0;
/* 1029 */     synchronized (cacheEntry)
/*      */     {
/* 1031 */       if ((((!cacheEntry.m_isValid) || (isCacheTooOld))) && (cacheEntry.m_isPending))
/*      */       {
/*      */         try
/*      */         {
/* 1035 */           String msg = "Waiting for pending cache to be filled for " + query + " targeting " + providerName;
/*      */ 
/* 1037 */           if (isCacheTooOld)
/*      */           {
/* 1039 */             msg = msg + ", cache was too old";
/*      */           }
/* 1041 */           message(msg);
/* 1042 */           long currentTimeMillis = System.currentTimeMillis();
/* 1043 */           long timeLeft = updateParameters.m_startTime + updateParameters.m_timeoutWaitingForCache - currentTimeMillis;
/*      */ 
/* 1046 */           if ((timeLeft > 0L) && (timeLeft <= updateParameters.m_timeoutWaitingForCache))
/*      */           {
/* 1048 */             cacheEntry.wait(timeLeft);
/*      */           }
/*      */ 
/* 1057 */           updateParameters.m_cacheUpdateStateFlags &= (F_CACHE_TOO_OLD ^ 0xFFFFFFFF);
/*      */ 
/* 1059 */           if (cacheEntry.m_isPending)
/*      */           {
/* 1061 */             if (cacheEntry.m_isValid)
/*      */             {
/* 1063 */               message("Waited for pending cache, but query was not resolved in time, using potentially very stale cache");
/*      */             }
/*      */             else
/*      */             {
/* 1068 */               message("Waited for pending cache, but query was not resolved in time, doing query");
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1074 */           cacheEntry.notify();
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/* 1078 */           Report.trace(null, null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void mergeResultsToParameters(int first, int count, CacheObject cacheEntry, int currentAge, CacheUpdateParameters updateParameters)
/*      */   {
/* 1101 */     String providerName = updateParameters.m_providerName;
/* 1102 */     String query = updateParameters.m_query;
/* 1103 */     int updateType = updateParameters.m_updateType;
/* 1104 */     boolean isDocInfo = updateType == CacheObject.CACHE_IS_DOC_INFO;
/* 1105 */     int flags = updateParameters.m_cacheUpdateStateFlags;
/* 1106 */     boolean isCacheTooOld = (flags & F_CACHE_TOO_OLD) != 0;
/* 1107 */     boolean doDifferenceUpdate = (flags & F_DO_CHANGE_DIFFERENCE_UPDATE) != 0;
/* 1108 */     updateParameters.m_isValidCache = false;
/* 1109 */     if ((cacheEntry == null) || (!cacheEntry.m_isValid) || (isCacheTooOld))
/*      */     {
/* 1111 */       message("Missed cache for " + query + " targeting " + providerName);
/*      */     }
/* 1115 */     else if ((cacheEntry.m_hasAllPossibleRows) || (cacheEntry.m_rowCount >= first + count - 1))
/*      */     {
/* 1117 */       if ((!isDocInfo) || (SystemUtils.m_verbose))
/*      */       {
/* 1119 */         String msg = "Cache hit for " + query + " targeting " + providerName;
/* 1120 */         if (!isDocInfo)
/*      */         {
/* 1122 */           message(msg);
/*      */         }
/*      */         else
/*      */         {
/* 1126 */           debug(msg);
/*      */         }
/*      */       }
/* 1129 */       if (isDocInfo)
/*      */       {
/* 1131 */         updateParameters.m_cacheDocInfo = cacheEntry.m_resultSetRow;
/*      */       }
/*      */       else
/*      */       {
/* 1135 */         copySearchListToParameters(cacheEntry, updateParameters);
/*      */       }
/* 1137 */       boolean isOldCache = false;
/* 1138 */       if (!doDifferenceUpdate)
/*      */       {
/* 1140 */         isOldCache = cacheEntry.m_ageCounter < currentAge;
/*      */       }
/* 1142 */       usedCacheEntry(cacheEntry, updateParameters.m_startTime, true);
/* 1143 */       updateParameters.m_isValidCache = true;
/* 1144 */       updateParameters.m_isOldCache = isOldCache;
/*      */     }
/*      */     else
/*      */     {
/* 1148 */       if (SystemUtils.isActiveTrace("searchcache"))
/*      */       {
/* 1150 */         Object[] args = { "" + (first + count - 1), "" + cacheEntry.m_rowCount, providerName, query };
/*      */ 
/* 1153 */         String msg = LocaleUtils.encodeMessage("csCacheMissInsufficientRows", null, args);
/*      */ 
/* 1155 */         msg = LocaleResources.localizeMessage(msg, null);
/* 1156 */         message(msg);
/*      */       }
/* 1158 */       this.m_missCount += 1;
/*      */ 
/* 1160 */       Report.trace("searchcache", null, "csMonitorSearchMissCount", new Object[] { Integer.valueOf(this.m_missCount) });
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void finishUpFirstPassUpdatingCache(int first, int count, CacheObject cacheEntry, int currentAge, CacheUpdateParameters updateParameters)
/*      */   {
/* 1175 */     int flags = updateParameters.m_cacheUpdateStateFlags;
/* 1176 */     boolean didDifferenceUpdate = (flags & F_DID_DIFFERENCE_UPDATE) != 0;
/*      */ 
/* 1178 */     synchronized (this)
/*      */     {
/* 1180 */       ProviderCache curProviderCache = updateParameters.m_providerCache;
/* 1181 */       if (!updateParameters.m_updateGenerationalCounterComputed)
/*      */       {
/* 1183 */         updateParameters.m_updateGenerationalCounter = (curProviderCache.m_generationalSearchCounter++);
/* 1184 */         updateParameters.m_updateGenerationalCounterComputed = true;
/*      */       }
/*      */ 
/* 1188 */       if (didDifferenceUpdate)
/*      */       {
/* 1190 */         synchronized (cacheEntry)
/*      */         {
/* 1192 */           int[] docsListSync = cacheEntry.m_docsListSync;
/* 1193 */           if ((cacheEntry.m_isValid) && (updateParameters.m_updateListSyncCount == docsListSync[0]) && (updateParameters.m_cacheGenerationalCounter == cacheEntry.m_generationalCounter))
/*      */           {
/* 1199 */             cacheEntry.updateChangedCacheDocList(updateParameters);
/*      */ 
/* 1202 */             docsListSync[0] += 1;
/*      */           }
/*      */           else
/*      */           {
/* 1206 */             message("Could not update with repaired cache because it changed");
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/* 1211 */       updateStatisticsAndPrepareQueryResults(updateParameters);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateStatisticsAndPrepareQueryResults(CacheUpdateParameters updateParameters)
/*      */   {
/* 1217 */     ProviderCache providerCache = updateParameters.m_providerCache;
/* 1218 */     int updateType = updateParameters.m_updateType;
/* 1219 */     boolean isDocInfo = updateType == CacheObject.CACHE_IS_DOC_INFO;
/* 1220 */     if (!updateParameters.m_isValidCache)
/*      */     {
/* 1222 */       if (isDocInfo)
/*      */       {
/* 1224 */         this.m_docInfoMissCount += 1;
/*      */       }
/*      */       else
/*      */       {
/* 1228 */         this.m_missCount += 1;
/*      */ 
/* 1230 */         Report.trace("searchcache", null, "csMonitorSearchMissCount", new Object[] { Integer.valueOf(this.m_missCount) });
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1236 */       if (isDocInfo)
/*      */       {
/* 1238 */         this.m_docInfoHitCount += 1;
/*      */       }
/*      */       else
/*      */       {
/* 1242 */         this.m_hitCount += 1;
/*      */ 
/* 1244 */         Report.trace("searchcache", null, "csMonitorSearchHitCount", new Object[] { Integer.valueOf(this.m_hitCount) });
/*      */       }
/* 1246 */       if (updateParameters.m_isOldCache)
/*      */       {
/* 1248 */         if (isDocInfo)
/*      */         {
/* 1250 */           this.m_docInfoOldCacheHitCount += 1;
/*      */         }
/*      */         else
/*      */         {
/* 1254 */           this.m_oldCacheHitCount += 1;
/*      */         }
/*      */       }
/*      */ 
/* 1258 */       if (updateParameters.m_cacheDocsList != null)
/*      */       {
/* 1260 */         this.m_hitRowCount += updateParameters.m_cacheDocsList.length;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1268 */     boolean needToComeBack = (!updateParameters.m_isValidCache) || ((!updateParameters.m_cacheIsComplete) && (!isDocInfo));
/*      */ 
/* 1270 */     if ((!needToComeBack) || (updateParameters.m_clonedLastAccessedCache != null))
/*      */       return;
/* 1272 */     updateParameters.m_clonedLastAccessedCache = providerCache.shallowClone();
/*      */   }
/*      */ 
/*      */   protected void copySearchListToParameters(CacheObject cacheEntry, CacheUpdateParameters updateParameters)
/*      */   {
/* 1279 */     updateParameters.m_hasAllPossibleRows = cacheEntry.m_hasAllPossibleRows;
/* 1280 */     updateParameters.m_cacheDocsList = cacheEntry.m_docsList;
/* 1281 */     updateParameters.m_cacheBinderCaptureKeys = cacheEntry.m_supplementaryDataKeys;
/* 1282 */     updateParameters.m_cacheBinderCapturedValues = cacheEntry.m_supplementaryDataValues;
/* 1283 */     updateParameters.m_isSorted = cacheEntry.m_isSorted;
/* 1284 */     updateParameters.m_cacheSortFieldInfo = cacheEntry.m_sortFieldInfo;
/* 1285 */     updateParameters.m_extraResultSets = cacheEntry.m_additionalResultSets;
/*      */   }
/*      */ 
/*      */   protected synchronized void markPendingCache(CacheObject cacheEntry, CacheUpdateParameters updateParameters)
/*      */   {
/* 1299 */     cacheEntry.m_isPending = true;
/* 1300 */     if (updateParameters.m_updateType == CacheObject.CACHE_IS_DOC_LIST)
/*      */     {
/* 1302 */       updateParameters.m_queryDocListPending = true;
/*      */     }
/*      */     else
/*      */     {
/* 1306 */       updateParameters.m_queryDocInfoPending = true;
/*      */     }
/* 1308 */     this.m_numWaitingForBuild += 1;
/*      */   }
/*      */ 
/*      */   protected void loadProviderCacheReference(CacheUpdateParameters updateParameters)
/*      */   {
/* 1324 */     String providerName = computeProviderName(updateParameters);
/* 1325 */     updateParameters.m_providerName = providerName;
/*      */ 
/* 1327 */     ProviderCache cache = (ProviderCache)this.m_cacheByProvider.get(providerName);
/* 1328 */     if (cache == null)
/*      */     {
/* 1330 */       cache = new ProviderCache(providerName);
/* 1331 */       cache.init();
/* 1332 */       this.m_cacheByProvider.put(providerName, cache);
/*      */     }
/* 1334 */     updateParameters.m_providerCache = cache;
/* 1335 */     if (updateParameters.m_searchConfig != null)
/*      */     {
/* 1337 */       String engineName = updateParameters.m_searchConfig.m_engineName;
/* 1338 */       if (engineName != null)
/*      */       {
/* 1340 */         updateParameters.m_cacheDataDesign = ((CacheDataDesign)cache.m_cacheDataDesigns.get(engineName));
/*      */       }
/*      */     }
/*      */ 
/* 1344 */     updateParameters.m_updateBinderCaptureKeys = cache.m_additionalCapturedFields;
/*      */   }
/*      */ 
/*      */   protected void incrementCounters(CacheObject obj, int count, int actionType)
/*      */   {
/* 1355 */     if (actionType == ADD_CACHE_OBJECT)
/*      */     {
/* 1357 */       this.m_currentCachedObjectCount += 1;
/*      */     }
/* 1359 */     else if (actionType == DELETE_CACHE_OBJECT)
/*      */     {
/* 1361 */       count = -count;
/* 1362 */       this.m_currentCachedObjectCount -= 1;
/*      */     }
/* 1364 */     if (obj.m_type == CacheObject.CACHE_IS_DOC_INFO)
/*      */     {
/* 1366 */       this.m_currentCachedDocInfoCount += count;
/*      */     }
/*      */     else
/*      */     {
/* 1370 */       this.m_currentCachedResultRowCount += count;
/*      */     }
/*      */ 
/* 1373 */     Report.trace("monitor", null, "csMonitorSearchCachedQueries", new Object[] { Integer.valueOf(this.m_docInfoItemMultiplier * this.m_currentCachedDocInfoCount + this.m_currentCachedResultRowCount + 2 * this.m_currentCachedObjectCount) });
/*      */   }
/*      */ 
/*      */   protected CacheObject createOrUpdateCacheEntry(CacheUpdateParameters updateParameters, CacheObject obj, int flags)
/*      */   {
/* 1400 */     boolean isUpdate = (flags & F_UPDATE_CACHE) != 0;
/* 1401 */     String query = updateParameters.m_query;
/* 1402 */     ProviderCache providerCache = updateParameters.m_providerCache;
/* 1403 */     if (obj == null)
/*      */     {
/* 1405 */       obj = new CacheObject(updateParameters);
/* 1406 */       CacheObject oldObj = (CacheObject)providerCache.m_queryCaches.get(query);
/* 1407 */       if (oldObj != null)
/*      */       {
/* 1409 */         removeCacheEntry(oldObj);
/*      */       }
/* 1411 */       providerCache.m_queryCaches.put(query, obj);
/* 1412 */       incrementCounters(obj, obj.m_rowCount, ADD_CACHE_OBJECT);
/* 1413 */       if ((!obj.m_isValid) && (!obj.m_isPending) && (!isUpdate))
/*      */       {
/* 1416 */         markPendingCache(obj, updateParameters);
/*      */       }
/*      */ 
/*      */     }
/* 1421 */     else if (isUpdate)
/*      */     {
/* 1429 */       int addedRows = obj.update(updateParameters);
/* 1430 */       incrementCounters(obj, addedRows, UPDATE_CACHE_OBJECT);
/*      */ 
/* 1432 */       releasePending(obj);
/*      */     }
/*      */ 
/* 1436 */     obj.m_isLinked = true;
/* 1437 */     if (this.m_lru == null)
/*      */     {
/* 1439 */       this.m_lru = (this.m_mru = obj);
/*      */     }
/*      */     else
/*      */     {
/* 1447 */       usedCacheEntry(obj, updateParameters.m_startTime, false);
/*      */     }
/*      */ 
/* 1451 */     if (updateParameters.m_extraResultSets != null)
/*      */     {
/* 1453 */       obj.m_additionalResultSets = updateParameters.m_extraResultSets;
/*      */     }
/* 1455 */     return obj;
/*      */   }
/*      */ 
/*      */   protected void releasePending(CacheObject obj)
/*      */   {
/* 1465 */     synchronized (obj)
/*      */     {
/* 1467 */       if (obj.m_isPending)
/*      */       {
/* 1470 */         this.m_numWaitingForBuild -= 1;
/* 1471 */         obj.m_isPending = false;
/* 1472 */         obj.notify();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void usedCacheEntry(CacheObject entry, long curTime, boolean updateTimestamp)
/*      */   {
/* 1481 */     if ((entry.m_ageCounter < entry.m_providerCache.m_currentAge) || (!entry.m_isLinked))
/*      */       return;
/* 1483 */     if (updateTimestamp)
/*      */     {
/* 1485 */       entry.m_lastAccessTime = curTime;
/*      */     }
/* 1487 */     if (entry == this.m_mru)
/*      */       return;
/* 1489 */     if (entry == this.m_lru)
/*      */     {
/* 1491 */       this.m_lru = entry.m_next;
/*      */     }
/* 1493 */     entry.remove();
/* 1494 */     entry.insertAfter(this.m_mru);
/* 1495 */     this.m_mru = entry;
/*      */   }
/*      */ 
/*      */   protected synchronized void removeCacheEntry(CacheObject entry)
/*      */   {
/* 1502 */     boolean wasLinkedIn = entry.m_isLinked;
/* 1503 */     if (this.m_mru == entry)
/*      */     {
/* 1505 */       this.m_mru = entry.m_prev;
/*      */     }
/* 1507 */     if (this.m_lru == entry)
/*      */     {
/* 1509 */       this.m_lru = entry.m_next;
/*      */     }
/* 1511 */     entry.remove();
/* 1512 */     entry.m_isLinked = false;
/* 1513 */     if (!entry.m_providerCache.m_isInvalidated)
/*      */     {
/* 1515 */       entry.m_providerCache.m_queryCaches.remove(entry.m_query);
/*      */     }
/* 1517 */     releasePending(entry);
/* 1518 */     if (!wasLinkedIn)
/*      */       return;
/* 1520 */     incrementCounters(entry, entry.m_rowCount, DELETE_CACHE_OBJECT);
/*      */   }
/*      */ 
/*      */   protected synchronized boolean updateCacheEntries(CacheUpdateParameters updateParameters)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1536 */     loadProviderCacheReference(updateParameters);
/* 1537 */     ProviderCache shallowClonedCache = updateParameters.m_clonedLastAccessedCache;
/* 1538 */     ProviderCache curProviderCache = updateParameters.m_providerCache;
/* 1539 */     boolean didUpdate = false;
/* 1540 */     String query = updateParameters.m_query;
/* 1541 */     int updateType = updateParameters.m_updateType;
/* 1542 */     if (!updateParameters.m_updateGenerationalCounterComputed)
/*      */     {
/* 1544 */       updateParameters.m_updateGenerationalCounter = (curProviderCache.m_generationalSearchCounter++);
/* 1545 */       updateParameters.m_updateGenerationalCounterComputed = true;
/*      */     }
/*      */ 
/* 1551 */     if ((shallowClonedCache != null) && (curProviderCache.m_queryCaches == shallowClonedCache.m_queryCaches))
/*      */     {
/* 1554 */       Map cache = shallowClonedCache.m_queryCaches;
/* 1555 */       Map unresolvedNames = updateParameters.m_unresolvedMissingNames;
/*      */       try
/*      */       {
/* 1558 */         if (updateParameters.m_updateHasResults)
/*      */         {
/* 1560 */           updateCacheItemsAndList(query, updateParameters, cache, unresolvedNames);
/*      */         }
/*      */ 
/*      */       }
/*      */       finally
/*      */       {
/* 1567 */         updateParameters.m_query = query;
/* 1568 */         updateParameters.m_updateType = updateType;
/* 1569 */         if (updateType == CacheObject.CACHE_IS_DOC_LIST)
/*      */         {
/* 1571 */           if (updateParameters.m_queryDocListPending)
/*      */           {
/* 1574 */             createOrUpdateCacheEntry(updateParameters, null, F_UPDATE_CACHE);
/* 1575 */             updateParameters.m_queryDocListPending = false;
/*      */           }
/*      */ 
/* 1578 */           releasePendingDocInfoQueries(updateParameters, cache, unresolvedNames);
/* 1579 */           updateParameters.m_queryDocInfoPending = false;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1584 */     return didUpdate;
/*      */   }
/*      */ 
/*      */   protected void updateCacheItemsAndList(String query, CacheUpdateParameters updateParameters, Map<String, CacheObject> cache, Map<String, Integer> unresolvedNames)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1609 */     boolean updateRows = (!updateParameters.m_updateHasDocsList) && (updateParameters.m_isValidCache) && (updateParameters.m_cacheDocsListMetadata != null);
/*      */ 
/* 1611 */     updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_INFO;
/* 1612 */     int numRows = updateParameters.m_updateRowCount;
/* 1613 */     if (updateParameters.m_captureCacheRows)
/*      */     {
/* 1615 */       updateParameters.m_capturedCacheRows = new CacheRow[numRows];
/*      */     }
/* 1617 */     for (int i = 0; i < numRows; ++i)
/*      */     {
/* 1619 */       SearchCacheUtils.createObjectArrayAndQueryFromRow(updateParameters, i);
/* 1620 */       CacheObject obj = (CacheObject)cache.get(updateParameters.m_query);
/*      */ 
/* 1623 */       obj = createOrUpdateCacheEntry(updateParameters, obj, F_UPDATE_CACHE);
/* 1624 */       if (updateParameters.m_captureCacheRows)
/*      */       {
/* 1626 */         updateParameters.m_capturedCacheRows[i] = obj.m_resultSetRow;
/*      */       }
/*      */ 
/* 1629 */       if (unresolvedNames == null)
/*      */         continue;
/* 1631 */       Integer rowIndex = (Integer)unresolvedNames.get(updateParameters.m_query);
/* 1632 */       if (rowIndex == null)
/*      */         continue;
/* 1634 */       if ((updateRows) && (updateParameters.m_isValidCache))
/*      */       {
/* 1636 */         int row = rowIndex.intValue();
/*      */ 
/* 1640 */         SearchCacheUtils.checkIfReplacementRowFits(query, updateParameters, obj, row);
/*      */       }
/*      */ 
/* 1644 */       unresolvedNames.remove(updateParameters.m_query);
/*      */     }
/*      */ 
/* 1650 */     if (updateParameters.m_updateHasDocsList)
/*      */     {
/* 1652 */       updateParameters.m_query = query;
/* 1653 */       CacheObject obj = (CacheObject)cache.get(updateParameters.m_query);
/* 1654 */       updateParameters.m_updateType = CacheObject.CACHE_IS_DOC_LIST;
/* 1655 */       createOrUpdateCacheEntry(updateParameters, obj, F_UPDATE_CACHE);
/* 1656 */       updateParameters.m_queryDocListPending = false;
/*      */     } else {
/* 1658 */       if ((!updateParameters.m_isValidCache) || (!updateRows))
/*      */         return;
/* 1660 */       updateParameters.m_cacheIsComplete = (unresolvedNames.size() == 0);
/* 1661 */       if (!updateParameters.m_cacheIsComplete)
/*      */         return;
/* 1663 */       handleUpdatedCache(updateParameters, cache);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void handleUpdatedCache(CacheUpdateParameters updateParameters, Map<String, CacheObject> cache)
/*      */     throws DataException
/*      */   {
/* 1681 */     if (updateParameters.m_cacheDocsListNeedsMerging)
/*      */     {
/* 1683 */       SearchCacheUtils.mergeAndSortCachedDocList(updateParameters);
/*      */     }
/* 1685 */     if (!updateParameters.m_cacheDocListNeedsUpdating)
/*      */       return;
/* 1687 */     CacheObject obj = (CacheObject)cache.get(updateParameters.m_query);
/* 1688 */     if (obj != null)
/*      */     {
/* 1690 */       int addedRows = obj.updateChangedCacheDocList(updateParameters);
/* 1691 */       incrementCounters(obj, addedRows, UPDATE_CACHE_OBJECT);
/*      */     }
/* 1693 */     updateParameters.m_cacheDocListNeedsUpdating = false;
/*      */   }
/*      */ 
/*      */   public void updateCache(CacheUpdateParameters updateParameters, DataBinder results)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1706 */     long time = System.currentTimeMillis();
/* 1707 */     results.putLocal("LastUsedTime", "" + time);
/* 1708 */     updateParameters.m_hasSearchResults = true;
/* 1709 */     boolean hasSearchList = updateParameters.m_updateType == CacheObject.CACHE_IS_DOC_LIST;
/* 1710 */     DataResultSet searchRset = (DataResultSet)results.getResultSet("SearchResults");
/* 1711 */     synchronized (this)
/*      */     {
/* 1713 */       if (updateParameters.m_providerCache == null)
/*      */       {
/* 1715 */         loadProviderCacheReference(updateParameters);
/*      */ 
/* 1719 */         updateParameters.m_clonedLastAccessedCache = updateParameters.m_providerCache.shallowClone();
/*      */       }
/* 1721 */       if ((searchRset != null) && (searchRset.getNumFields() > 0))
/*      */       {
/* 1723 */         SearchCacheUtils.checkUpdateTemplateResultSet(updateParameters, results, searchRset);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1729 */     SearchCacheUtils.computeCacheUpdateParameters(updateParameters, results, searchRset, hasSearchList);
/* 1730 */     if (hasSearchList)
/*      */     {
/* 1732 */       SearchCacheUtils.createObjectArrayFromSearchResultsUpdate(updateParameters);
/*      */     }
/*      */ 
/* 1737 */     updateCacheEntries(updateParameters);
/*      */   }
/*      */ 
/*      */   public synchronized void releaseCache(CacheUpdateParameters updateParameters)
/*      */   {
/* 1748 */     loadProviderCacheReference(updateParameters);
/* 1749 */     ProviderCache shallowClonedCache = updateParameters.m_clonedLastAccessedCache;
/* 1750 */     if ((shallowClonedCache == null) || (updateParameters.m_providerCache.m_queryCaches != shallowClonedCache.m_queryCaches))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 1755 */     createOrUpdateCacheEntry(updateParameters, null, F_UPDATE_CACHE);
/*      */ 
/* 1757 */     updateParameters.m_queryDocListPending = false;
/* 1758 */     releasePendingDocInfoQueries(updateParameters, shallowClonedCache.m_queryCaches, updateParameters.m_unresolvedMissingNames);
/*      */ 
/* 1760 */     updateParameters.m_queryDocInfoPending = false;
/*      */   }
/*      */ 
/*      */   public void releasePendingDocInfoQueries(CacheUpdateParameters updateParameters, Map<String, CacheObject> cache, Map<String, Integer> unresolvedNames)
/*      */   {
/* 1776 */     if (unresolvedNames == null)
/*      */     {
/* 1778 */       return;
/*      */     }
/* 1780 */     Set keys = unresolvedNames.keySet();
/* 1781 */     for (String key : keys)
/*      */     {
/* 1783 */       CacheObject obj = (CacheObject)cache.get(key);
/* 1784 */       if (obj != null)
/*      */       {
/* 1786 */         message("Released unresolved pending doc info cache " + obj);
/* 1787 */         releasePending(obj);
/*      */       }
/*      */     }
/* 1790 */     unresolvedNames.clear();
/*      */   }
/*      */ 
/*      */   public synchronized void invalidateCache(Provider searchProvider, int curAge, SearchCacheEvent cacheEvent, ExecutionContext cxt, int flags)
/*      */   {
/* 1814 */     String providerName = computeProviderNameFromProvider(searchProvider);
/* 1815 */     ProviderCache cache = (ProviderCache)this.m_cacheByProvider.get(providerName);
/* 1816 */     boolean fullFlush = (flags & F_FULL_FLUSH) != 0;
/* 1817 */     boolean flushCache = (fullFlush) || ((flags & F_FLUSH_CACHE) != 0);
/* 1818 */     boolean usePassedInCounter = (flags & F_USE_PASSED_IN_AGE_COUNTER) != 0;
/* 1819 */     boolean notifyCache = false;
/* 1820 */     boolean useOldItems = (this.m_useOldCacheItems) && (!fullFlush) && (!usePassedInCounter);
/* 1821 */     if (cache != null)
/*      */     {
/* 1823 */       if (((useOldItems) && (flushCache)) || ((usePassedInCounter) && (!flushCache)))
/*      */       {
/* 1827 */         if ((usePassedInCounter) && (curAge <= cache.m_currentAge))
/*      */         {
/* 1829 */           Report.trace("system", "New search cache age " + curAge + " is not after current age " + cache.m_currentAge + ", flushing cache", null);
/*      */ 
/* 1832 */           flushCache = true;
/*      */         }
/*      */         else
/*      */         {
/* 1840 */           flushCache = false;
/* 1841 */           for (int i = cache.m_invalidationTimes.length - 1; i > 0; --i)
/*      */           {
/* 1843 */             cache.m_invalidationTimes[i] = cache.m_invalidationTimes[(i - 1)];
/*      */           }
/* 1845 */           cache.m_invalidationTimes[0] = System.currentTimeMillis();
/* 1846 */           if (usePassedInCounter)
/*      */           {
/* 1848 */             cache.m_currentAge = curAge;
/*      */           }
/*      */           else
/*      */           {
/* 1852 */             cache.m_currentAge += 1;
/*      */           }
/*      */         }
/* 1855 */         message("Cache notified of change in released documents state, m_currentAge=" + cache.m_currentAge);
/*      */       }
/*      */ 
/* 1858 */       if (flushCache)
/*      */       {
/* 1860 */         message("Cache invalidated by change in released documents state,  creating new search cache");
/*      */ 
/* 1862 */         this.m_cacheByProvider.remove(providerName);
/*      */ 
/* 1864 */         this.m_invalidatedCaches.addElement(cache);
/* 1865 */         cache.m_isInvalidated = true;
/* 1866 */         notifyCache = true;
/*      */       }
/*      */     }
/* 1869 */     if (cacheEvent != null)
/*      */     {
/* 1871 */       boolean foundIt = false;
/*      */ 
/* 1874 */       for (int i = this.m_searchCacheEvents.size() - 1; i >= 0; --i)
/*      */       {
/* 1876 */         SearchCacheEvent event = (SearchCacheEvent)this.m_searchCacheEvents.get(i);
/* 1877 */         if (!event.getEventID().equals(cacheEvent.getEventID()))
/*      */           continue;
/* 1879 */         foundIt = true;
/* 1880 */         this.m_searchCacheEvents.set(i, cacheEvent);
/*      */       }
/*      */ 
/* 1883 */       if (!foundIt)
/*      */       {
/* 1885 */         this.m_searchCacheEvents.add(cacheEvent);
/*      */       }
/* 1887 */       notifyCache = true;
/*      */     }
/* 1889 */     if (!notifyCache)
/*      */       return;
/* 1891 */     super.notify();
/*      */   }
/*      */ 
/*      */   protected void message(String text)
/*      */   {
/* 1897 */     Report.trace("searchcache", text, null);
/*      */   }
/*      */ 
/*      */   protected void debug(String text)
/*      */   {
/* 1902 */     Report.debug("searchcache", text, null);
/*      */   }
/*      */ 
/*      */   protected String computeProviderName(CacheUpdateParameters updateParameters)
/*      */   {
/* 1914 */     return computeProviderNameFromProvider(updateParameters.m_provider);
/*      */   }
/*      */ 
/*      */   protected String computeProviderNameFromProvider(Provider p)
/*      */   {
/* 1924 */     if (p == null)
/*      */     {
/* 1926 */       return "##LocalSearch";
/*      */     }
/* 1928 */     return p.getName();
/*      */   }
/*      */ 
/*      */   public synchronized void outputCacheReport(DataBinder binder)
/*      */   {
/* 1934 */     binder.putLocal("scDocInfoItemMultiplier", "" + this.m_docInfoItemMultiplier);
/* 1935 */     binder.putLocal("scCurrentCachedDocInfoCount", "" + this.m_currentCachedDocInfoCount);
/* 1936 */     binder.putLocal("scCurrentCachedResultRowCount", "" + this.m_currentCachedResultRowCount);
/* 1937 */     binder.putLocal("scCurrentCachedObjectCount", "" + this.m_currentCachedObjectCount);
/*      */ 
/* 1939 */     int curNumItems = this.m_docInfoItemMultiplier * this.m_currentCachedDocInfoCount + this.m_currentCachedResultRowCount + 2 * this.m_currentCachedObjectCount;
/*      */ 
/* 1941 */     binder.putLocal("scCurrentRowCount", "" + curNumItems);
/* 1942 */     binder.putLocal("scTargetRowCount", "" + this.m_targetCachedItems);
/* 1943 */     binder.putLocal("scHitCount", "" + this.m_hitCount);
/* 1944 */     binder.putLocal("scDocInfoHitCount", "" + this.m_docInfoHitCount);
/* 1945 */     binder.putLocal("scMissCount", "" + this.m_missCount);
/* 1946 */     binder.putLocal("scDocInfoMissCount", "" + this.m_docInfoMissCount);
/* 1947 */     binder.putLocal("scHitRowCount", "" + this.m_hitRowCount);
/* 1948 */     binder.putLocal("scMissRowCount", "" + this.m_missRowCount);
/*      */ 
/* 1951 */     binder.putLocal("scCacheByProviderCount", "" + this.m_cacheByProvider.size());
/* 1952 */     binder.putLocal("scActiveConnections", "" + this.m_searchConnections);
/* 1953 */     binder.putLocal("scMaxItemAge", "" + this.m_searchCacheMaxItemAge);
/* 1954 */     binder.putLocal("scCleanupInterval", "" + this.m_searchCacheCleanerInterval);
/*      */ 
/* 1957 */     binder.putLocal("scNumWaitingForBuild", "" + this.m_numWaitingForBuild);
/* 1958 */     binder.putLocal("scOldCacheHitCount", "" + this.m_oldCacheHitCount);
/*      */ 
/* 1962 */     int count1 = 0;
/* 1963 */     int count2 = 0;
/*      */ 
/* 1966 */     for (CacheObject item = this.m_lru; (item != null) && (item.m_next != null); item = item.m_next)
/*      */     {
/* 1968 */       ++count1;
/*      */     }
/* 1970 */     if ((item != null) && (item.m_next == null))
/*      */     {
/* 1972 */       ++count1;
/*      */     }
/*      */ 
/* 1975 */     if (item != this.m_mru)
/*      */     {
/* 1977 */       binder.putLocal("scMruListBroken", "1");
/*      */     }
/*      */ 
/* 1981 */     for (item = this.m_mru; (item != null) && (item.m_prev != null); item = item.m_prev)
/*      */     {
/* 1983 */       ++count2;
/*      */     }
/* 1985 */     if ((item != null) && (item.m_prev == null))
/*      */     {
/* 1987 */       ++count2;
/*      */     }
/*      */ 
/* 1990 */     if (item != this.m_lru)
/*      */     {
/* 1992 */       binder.putLocal("scLruListBroken", "1");
/*      */     }
/* 1994 */     if ((count1 != count2) || (count1 != this.m_currentCachedObjectCount))
/*      */     {
/* 1996 */       binder.putLocal("scListIsInconsistentSize", "1");
/* 1997 */       binder.putLocal("scForwardListSize", "" + count1);
/* 1998 */       binder.putLocal("scBackwardListSize", "" + count2);
/* 1999 */       binder.putLocal("scMaintainedListSize", "" + this.m_currentCachedObjectCount);
/*      */     }
/*      */     else
/*      */     {
/* 2003 */       binder.putLocal("scCacheSize", "" + count1);
/*      */     }
/*      */ 
/* 2007 */     if (this.m_mru != null)
/*      */     {
/* 2009 */       binder.putLocalDate("scMruDate", new Date(this.m_mru.m_lastAccessTime));
/*      */     }
/*      */ 
/* 2012 */     if (this.m_lru != null)
/*      */     {
/* 2014 */       binder.putLocalDate("scLruDate", new Date(this.m_lru.m_lastAccessTime));
/*      */     }
/*      */ 
/* 2018 */     DataResultSet drset = new DataResultSet(new String[] { "queryText", "cacheTime" });
/* 2019 */     binder.addResultSet("SearchCacheUnreferencedEntries", drset);
/* 2020 */     binder.setFieldType("cacheTime", "date");
/*      */ 
/* 2022 */     for (item = this.m_lru; item != null; item = item.m_next)
/*      */     {
/* 2025 */       if (item.m_providerCache.m_queryCaches.get(item.m_query) != null)
/*      */         continue;
/* 2027 */       Vector v = new IdcVector();
/* 2028 */       v.addElement(item.m_query);
/* 2029 */       String d = LocaleUtils.formatODBC(new Date(item.m_lastAccessTime));
/* 2030 */       v.addElement(d);
/* 2031 */       drset.addRow(v);
/*      */     }
/*      */   }
/*      */ 
/*      */   public synchronized void updateSearchData(DataResultSet drset, int askedForCount)
/*      */   {
/* 2038 */     if (drset != null)
/*      */     {
/* 2041 */       this.m_missRowCount += drset.getNumRows();
/*      */     }
/*      */     else
/*      */     {
/* 2046 */       this.m_missRowCount += askedForCount;
/*      */     }
/*      */   }
/*      */ 
/*      */   public synchronized void validateCache()
/*      */   {
/* 2052 */     Hashtable items = new Hashtable();
/*      */ 
/* 2054 */     for (CacheObject item = this.m_lru; (item != null) && (item.m_next != null); item = item.m_next)
/*      */     {
/* 2056 */       if (items.get(item) != null)
/*      */       {
/* 2058 */         Exception e = new Exception("Cache loop.");
/* 2059 */         e.printStackTrace();
/*      */       }
/* 2061 */       items.put(item, item);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2067 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85488 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchCache
 * JD-Core Version:    0.5.4
 */