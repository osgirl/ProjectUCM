/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class SearchUpdateChangeUtils
/*     */ {
/*     */   public static final int F_ADDED_ITEMS = 0;
/*     */   public static final int F_DELETED_ITEMS = 1;
/*     */ 
/*     */   public static void processReleasedDocumentsChange(Provider searchProvider, SearchCache searchCache, Workspace ws, ExecutionContext cxt, int cacheFlags)
/*     */     throws ServiceException
/*     */   {
/*  74 */     SearchUpdateCacheEventHandler eventHandler = null;
/*  75 */     boolean isLocalChange = (cacheFlags & SearchCache.F_LOCAL_CACHE_CHANGED) != 0;
/*  76 */     int newCurAge = 0;
/*     */ 
/*  78 */     if (isLocalChange)
/*     */     {
/*  80 */       DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  81 */       if (cxt == null)
/*     */       {
/*  83 */         cxt = new ExecutionContextAdaptor();
/*     */       }
/*  85 */       SearchDocChangeData changeData = SearchDocChangeUtils.getOrCreateGlobalSearchChangeData(binder, cxt);
/*     */ 
/*  87 */       boolean isFlush = ((cacheFlags & SearchCache.F_FLUSH_CACHE) != 0) || (!changeData.m_differentialUpdatesAllowed);
/*     */ 
/*  89 */       if (!isFlush)
/*     */       {
/*  93 */         synchronized (changeData)
/*     */         {
/*  95 */           if (!changeData.m_isValid)
/*     */           {
/*  97 */             isFlush = true;
/*  98 */             changeData.resetToValidState();
/*     */ 
/* 103 */             if (!changeData.m_isInit)
/*     */             {
/* 105 */               changeData.m_lastEndTime = DocumentInfoCacheUtils.getSharedDocTimestamp();
/* 106 */               changeData.m_fastChangesStartTime = changeData.m_lastEndTime;
/* 107 */               changeData.m_hasValidEndTime = true;
/* 108 */               changeData.m_isInit = true;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 115 */         SearchDocChangeUtils.setToErrorState(changeData, 2, "!csSearchUpdateChangeCacheFlushed");
/*     */       }
/*     */ 
/* 118 */       if (!isFlush)
/*     */       {
/* 120 */         eventHandler = new SearchUpdateCacheEventHandler(searchCache, searchProvider, changeData, cacheFlags, ws);
/*     */       }
/*     */       else
/*     */       {
/* 125 */         cacheFlags |= SearchCache.F_FLUSH_CACHE;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 132 */     searchCache.invalidateCache(searchProvider, newCurAge, eventHandler, cxt, cacheFlags);
/*     */   }
/*     */ 
/*     */   public static void checkForDocUpdateChanges(SearchUpdateCacheEventHandler cacheUpdateEvent)
/*     */     throws DataException, ServiceException
/*     */   {
/* 155 */     long endTime = DocumentInfoCacheUtils.synchronizeSharedTimestamp(DocumentInfoCacheUtils.F_READ_SYNC_AND_UPDATE | DocumentInfoCacheUtils.F_RETURN_WRITE_TIMESTAMP);
/*     */ 
/* 160 */     ExecutionContext cxt = cacheUpdateEvent.m_cxt;
/* 161 */     SearchDocChangeData changeData = cacheUpdateEvent.m_changeData;
/* 162 */     SearchDocChangeData capturedCopy = new SearchDocChangeData();
/* 163 */     SearchDocChangeUtils.checkForResetSearchChangeData(changeData, capturedCopy, cxt);
/* 164 */     int newCurAge = capturedCopy.m_currentAge + 1;
/* 165 */     boolean didLoading = false;
/*     */ 
/* 167 */     if ((capturedCopy.m_isValid) && (capturedCopy.m_hasValidEndTime))
/*     */     {
/* 169 */       long startTime = DocumentInfoCacheUtils.getSharedTimestampForSafeStartTime(capturedCopy.m_lastEndTime);
/*     */ 
/* 171 */       didLoading = true;
/* 172 */       for (int i = 0; i < 2; ++i)
/*     */       {
/* 174 */         int updateDiffFlags = (i == 0) ? 1 : 0;
/* 175 */         DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*     */         try
/*     */         {
/* 178 */           loadDocUpdateChanges(binder, cacheUpdateEvent, newCurAge, startTime, endTime, updateDiffFlags);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 183 */           Report.error(null, e, null);
/* 184 */           SearchDocChangeUtils.setToErrorState(changeData, e.m_errorCode, e.getMessage());
/*     */         }
/* 186 */         if (SearchDocChangeUtils.checkIsUsable(changeData))
/*     */           continue;
/* 188 */         didLoading = false;
/* 189 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 193 */     int cacheFlags = cacheUpdateEvent.m_cacheFlags;
/* 194 */     if ((!didLoading) || (!SearchDocChangeUtils.checkIsUsable(changeData)))
/*     */     {
/* 196 */       cacheFlags |= SearchCache.F_FLUSH_CACHE;
/*     */     }
/* 198 */     if (capturedCopy.m_isValid)
/*     */     {
/* 202 */       SearchDocChangeUtils.setNewAgeAndEndTimeAndGroomData(changeData, newCurAge, endTime);
/* 203 */       cacheFlags |= SearchCache.F_USE_PASSED_IN_AGE_COUNTER;
/*     */     }
/*     */ 
/* 206 */     cacheUpdateEvent.m_searchCache.invalidateCache(cacheUpdateEvent.m_searchProvider, newCurAge, null, cxt, cacheFlags);
/*     */   }
/*     */ 
/*     */   public static void loadDocUpdateChanges(DataBinder binder, SearchUpdateCacheEventHandler cacheUpdateEvent, int newCurAge, long startTime, long endTime, int updateDiffFlags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 238 */     boolean isDelete = (updateDiffFlags & 0x1) != 0;
/* 239 */     String isDeleteVal = (isDelete) ? "1" : "";
/* 240 */     binder.putLocal("isDeletedItems", isDeleteVal);
/* 241 */     String resultSetKey = (isDelete) ? "DocsDeleted" : "DocsAdded";
/* 242 */     binder.putLocal("searchResultsName", resultSetKey);
/* 243 */     long curStartTime = startTime;
/* 244 */     long currentSharedTime = endTime;
/* 245 */     int changeFlag = (isDelete) ? SearchDocChangeItem.CHANGE_DELETE : SearchDocChangeItem.CHANGE_ADD;
/* 246 */     SearchDocChangeData changeData = cacheUpdateEvent.m_changeData;
/* 247 */     String changeDateColumnName = "dActionDate";
/*     */ 
/* 249 */     while ((curStartTime < endTime) && (SearchDocChangeUtils.checkIsUsable(changeData)))
/*     */     {
/* 251 */       DataBinder params = new DataBinder(SharedObjects.getSecureEnvironment());
/* 252 */       params.copyLocalDataStateClone(binder);
/* 253 */       DataBinderUtils.setOdbcDate(params, "endTime", endTime);
/* 254 */       DataBinderUtils.setOdbcDate(params, "startTime", curStartTime);
/* 255 */       Service s = ServiceManager.getInitializedService("GET_CHANGED_SEARCH_ITEMS", params, cacheUpdateEvent.m_ws);
/*     */ 
/* 257 */       s.setCachedObject("SearchUpdateCacheEventHandler", cacheUpdateEvent);
/* 258 */       s.doRequestInternal();
/* 259 */       List cacheRows = null;
/* 260 */       DataResultSet rsSet = (DataResultSet)params.getResultSet(resultSetKey);
/* 261 */       boolean isMaxRows = DataBinderUtils.getLocalBoolean(params, resultSetKey + ":hasMaxRows", false);
/* 262 */       String[][] extractedRows = (String[][])null;
/* 263 */       Map map = null;
/* 264 */       if (isDelete)
/*     */       {
/* 266 */         if (rsSet == null) return; if (rsSet.isEmpty()) {
/*     */           return;
/*     */         }
/*     */ 
/* 270 */         extractedRows = ResultSetUtils.createStringTable(rsSet, new String[] { "dDocName", "dID", changeDateColumnName });
/*     */       }
/*     */       else
/*     */       {
/* 275 */         cacheRows = (List)s.getCachedObject("SearchCacheRows");
/* 276 */         if (cacheRows == null) return; if (cacheRows.size() == 0) {
/*     */           return;
/*     */         }
/*     */ 
/* 280 */         map = new HashMap();
/* 281 */         for (int i = 0; i < cacheRows.size(); ++i)
/*     */         {
/* 283 */           CacheRow cacheRow = (CacheRow)cacheRows.get(i);
/* 284 */           CacheDataDesign dataDesign = cacheRow.m_dataDesign;
/*     */ 
/* 288 */           String classID = cacheRow.m_row[dataDesign.m_primaryKeyIndex];
/* 289 */           map.put(classID.toLowerCase(), cacheRow);
/*     */         }
/* 291 */         extractedRows = (String[][])(String[][])s.getCachedObject("SearchDocValues");
/*     */       }
/*     */ 
/* 294 */       Date firstDate = null;
/* 295 */       Date lastDate = null;
/* 296 */       int numRows = rsSet.getNumRows();
/* 297 */       for (int i = 0; (i < numRows) && (changeData.m_isValid); ++i)
/*     */       {
/* 300 */         CacheDataDesign dataDesign = null;
/* 301 */         String[] changeRow = extractedRows[i];
/* 302 */         String classID = changeRow[0];
/* 303 */         CacheRow cacheRow = null;
/*     */         String specificRevID;
/*     */         String specificRevID;
/* 304 */         if (isDelete)
/*     */         {
/* 306 */           specificRevID = changeRow[1];
/*     */         }
/*     */         else
/*     */         {
/* 310 */           cacheRow = (CacheRow)map.get(classID.toLowerCase());
/* 311 */           if (cacheRow == null)
/*     */           {
/* 313 */             Report.trace("searchcache", "Search for updated document " + classID + " found no result " + " at index " + i + " out of " + numRows + " rows", null);
/*     */ 
/* 315 */             continue;
/*     */           }
/* 317 */           dataDesign = cacheRow.m_dataDesign;
/*     */ 
/* 321 */           classID = cacheRow.m_row[dataDesign.m_primaryKeyIndex];
/* 322 */           specificRevID = cacheRow.m_row[dataDesign.m_specificRevKeyIndex];
/*     */         }
/*     */ 
/* 325 */         Date d = null;
/*     */         try
/*     */         {
/* 328 */           d = binder.parseDate(changeDateColumnName, changeRow[2]);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 332 */           throw new ServiceException(e);
/*     */         }
/* 334 */         if (firstDate == null)
/*     */         {
/* 336 */           firstDate = d;
/*     */         }
/* 338 */         lastDate = d;
/*     */ 
/* 340 */         long changeTime = d.getTime();
/* 341 */         SearchDocChangeItem changeItem = new SearchDocChangeItem(classID, specificRevID, cacheRow, changeTime, changeFlag, currentSharedTime, newCurAge);
/*     */ 
/* 347 */         SearchDocChangeUtils.updateSearchChangeData(changeData, changeItem, s, 1);
/*     */       }
/*     */ 
/* 351 */       s.clear();
/*     */ 
/* 353 */       if (lastDate == null) {
/*     */         return;
/*     */       }
/*     */ 
/* 357 */       long firstTime = firstDate.getTime();
/* 358 */       long lastTime = lastDate.getTime();
/* 359 */       boolean isAdvancing = true;
/* 360 */       if ((lastTime < firstTime) || (firstTime < curStartTime))
/*     */       {
/* 363 */         Report.trace("searchcache", "Going backwards, lastTime=" + lastTime + ", firstTime=" + firstTime + ", curStartTime=" + curStartTime, null);
/*     */ 
/* 365 */         if (!$assertionsDisabled) throw new AssertionError();
/* 366 */         Report.trace("system", "Timestamp from change differencing queries going backwards", null);
/* 367 */         isAdvancing = false;
/*     */       }
/* 369 */       if (!isMaxRows)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 376 */       if (isAdvancing)
/*     */       {
/* 378 */         isAdvancing = (lastTime > firstTime) && (lastTime > curStartTime);
/*     */       }
/* 380 */       curStartTime = lastTime;
/* 381 */       if (!isAdvancing)
/*     */       {
/* 387 */         String msg = LocaleUtils.encodeMessage("csSearchUpdateChangeDatesNotGoingForward", null, (isDelete) ? "add" : "delete", "" + numRows);
/*     */ 
/* 389 */         SearchDocChangeUtils.setToErrorState(changeData, 1, msg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 397 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86222 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchUpdateChangeUtils
 * JD-Core Version:    0.5.4
 */