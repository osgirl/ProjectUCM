/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.shared.CommonSearchEngineConfig;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class CacheUpdateParameters
/*     */ {
/*     */   public String m_query;
/*     */   public String m_docListQuery;
/*     */   public DataBinder m_requestBinder;
/*     */   public ParsedQueryElements m_parsedQueryElements;
/*     */   public boolean m_allowCheckRowInclude;
/*     */   public boolean m_allowCheckRowExclude;
/*     */   public String m_providerName;
/*     */   public Provider m_provider;
/*     */   public CommonSearchEngineConfig m_searchConfig;
/*     */   public boolean m_isLocalSearch;
/*     */   public boolean m_isSecondarySearchEngine;
/*     */   public boolean m_captureCacheRows;
/*     */   public long m_sharedSearchTime;
/*     */   public ExecutionContext m_cxt;
/*     */   public int m_updateType;
/*     */   public ProviderCache m_providerCache;
/*     */   public SearchDocChangeData m_changeData;
/*     */   public DataBinder m_updateBinder;
/*     */   public DataResultSet m_updateResultSet;
/*     */   public int m_updateRowCount;
/*     */   public int m_requestedRows;
/*     */   public boolean m_updateHasResults;
/*     */   public boolean m_updateHasResultSetRow;
/*     */   public int m_updateRowIndex;
/*     */   public String[] m_updateResultSetRow;
/*     */   public String[] m_updateCapturedPerQueryValues;
/*     */   public boolean m_updateHasDocsList;
/*     */   public SearchListItem[] m_updateDocsList;
/*     */   public String[] m_updateBinderCaptureKeys;
/*     */   public String[] m_updateBinderCapturedValues;
/*     */   public IdcDateFormat m_updateDateFormat;
/*     */   public boolean m_updateDateFormatMismatchedWithCache;
/*     */   public boolean m_allowOldCache;
/*     */   public boolean m_preferOldCache;
/*     */   public int[] m_fieldMap;
/*     */   public CacheRow[] m_capturedCacheRows;
/*     */   public int m_timeoutWaitingForCache;
/*     */   public long m_startTime;
/*     */   public boolean m_hasSearchResults;
/*     */   public FieldInfo m_updateSortFieldInfo;
/*     */   public DataBinder m_searchResults;
/*     */   public long m_cacheGenerationalCounter;
/*     */   public boolean m_updateGenerationalCounterComputed;
/*     */   public long m_updateGenerationalCounter;
/*     */   public boolean m_isSorted;
/*     */   public String m_sortField;
/*     */   public boolean m_sortIsAscending;
/*     */   public boolean m_isValidCache;
/*     */   public long m_cacheSharedSearchTime;
/*     */   public CacheDataDesign m_cacheDataDesign;
/*     */   public ProviderCache m_clonedLastAccessedCache;
/*     */   public boolean m_isOldCache;
/*     */   public boolean m_cacheRowNeededQueryRetest;
/*     */   public boolean m_cacheIsComplete;
/*     */   public SearchListItem[] m_missingNames;
/*     */   public Map<String, Integer> m_unresolvedMissingNames;
/*     */   public Map<String, Integer> m_keyToIndex;
/*     */   public boolean m_hasAllPossibleRows;
/*     */   public SearchListItem[] m_cacheDocsList;
/*     */   public FieldInfo m_cacheSortFieldInfo;
/*     */   public CacheRow[] m_cacheDocsListMetadata;
/*     */   public boolean m_cacheDocsListLookupValid;
/*     */   public Map<String, SearchListItem> m_cacheDocsListLookup;
/*     */   public Map<String, CacheRow> m_cacheDocsAlreadyValidatedQuery;
/*     */   public String[] m_cacheBinderCaptureKeys;
/*     */   public String[] m_cacheBinderCapturedValues;
/*     */   public CacheDataDesign m_repairCacheDataDesign;
/*     */   public boolean m_cacheDocsListNeedsMerging;
/*     */   public boolean m_cacheDocsListNeedsSorting;
/*     */   public boolean m_cacheDocListNeedsUpdating;
/*     */   public List<SearchListItem> m_newDocsList;
/*     */   public List<CacheRow> m_newDocsListMetadata;
/*     */   public int m_cacheNumDeletedRows;
/*     */   public SearchListItem m_tempNewDocItem;
/*     */   public int m_newRowDidNotFitReason;
/*     */   public boolean m_newItemCanReplaceOriginal;
/*     */   public CacheRow m_cacheDocInfo;
/*     */   public IdcStringBuilder m_tempBuilder;
/*     */   public boolean m_queryDocListPending;
/*     */   public boolean m_queryDocInfoPending;
/*     */   public int m_updateListSyncCount;
/*     */   public CacheObject m_activeCacheObject;
/*     */   int m_cacheUpdateStateFlags;
/*     */   int m_capturedCurrentAge;
/*     */   public boolean m_isError;
/*     */   public int m_statusCode;
/*     */   public String m_errMsg;
/*     */   public Exception m_exception;
/*     */   public HashMap<String, DataResultSet> m_extraResultSets;
/*     */   public HashMap<String, String> m_extraRSetNameMapping;
/*     */ 
/*     */   public CacheUpdateParameters(String query, DataBinder requestBinder, String providerName, Provider provider, long sharedSearchTime, CommonSearchEngineConfig searchConfig, ExecutionContext cxt)
/*     */   {
/* 723 */     this.m_query = query;
/* 724 */     this.m_docListQuery = query;
/* 725 */     this.m_providerName = providerName;
/* 726 */     this.m_provider = provider;
/* 727 */     this.m_requestBinder = requestBinder;
/* 728 */     this.m_sharedSearchTime = sharedSearchTime;
/* 729 */     this.m_searchConfig = searchConfig;
/* 730 */     this.m_cxt = cxt;
/* 731 */     this.m_startTime = System.currentTimeMillis();
/* 732 */     this.m_tempBuilder = new IdcStringBuilder();
/* 733 */     this.m_tempBuilder.m_disableToStringReleaseBuffers = true;
/*     */   }
/*     */ 
/*     */   public void addNewDocsListData(CacheRow row, SearchListItem listItem)
/*     */   {
/* 744 */     if (this.m_newDocsListMetadata == null)
/*     */     {
/* 746 */       this.m_newDocsListMetadata = new ArrayList();
/*     */     }
/* 748 */     this.m_newDocsListMetadata.add(row);
/*     */ 
/* 750 */     if (this.m_newDocsList == null)
/*     */     {
/* 752 */       this.m_newDocsList = new ArrayList();
/*     */     }
/* 754 */     this.m_newDocsList.add(listItem);
/*     */   }
/*     */ 
/*     */   public void addAlreadyValidatedRow(String key, CacheRow row)
/*     */   {
/* 764 */     if (this.m_cacheDocsAlreadyValidatedQuery == null)
/*     */     {
/* 766 */       this.m_cacheDocsAlreadyValidatedQuery = new HashMap();
/*     */     }
/* 768 */     this.m_cacheDocsAlreadyValidatedQuery.put(key, row);
/*     */   }
/*     */ 
/*     */   public CacheRow getAlreadyValidatedRow(String key)
/*     */   {
/* 779 */     if (this.m_cacheDocsAlreadyValidatedQuery == null)
/*     */     {
/* 781 */       return null;
/*     */     }
/* 783 */     return (CacheRow)this.m_cacheDocsAlreadyValidatedQuery.get(key);
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 792 */     if (this.m_query == null)
/*     */     {
/* 794 */       appendable.append("<Not valid>");
/*     */     }
/*     */     else
/*     */     {
/* 798 */       appendable.append(this.m_query);
/* 799 */       appendable.append(" (updateType=");
/* 800 */       appendable.append(CacheObject.getTypeString(this.m_updateType));
/* 801 */       appendable.append("\nsearchConfig=(");
/* 802 */       this.m_searchConfig.appendDebugFormat(appendable);
/* 803 */       appendable.append(")\nisValidCache=").append("" + this.m_isValidCache);
/* 804 */       appendable.append(",isError=").append("" + this.m_isError);
/* 805 */       appendable.append(",hasSearchResults=").append("" + this.m_hasSearchResults);
/* 806 */       appendable.append(")");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 817 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 818 */     appendDebugFormat(builder);
/* 819 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 827 */     this.m_tempBuilder.releaseBuffers();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 833 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85485 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CacheUpdateParameters
 * JD-Core Version:    0.5.4
 */