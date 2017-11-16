/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ 
/*     */ public class SearchUpdateCacheEventHandler
/*     */   implements SearchCacheEvent
/*     */ {
/*     */   public SearchCache m_searchCache;
/*     */   public Provider m_searchProvider;
/*     */   public SearchDocChangeData m_changeData;
/*     */   public int m_cacheFlags;
/*     */   public Workspace m_ws;
/*     */   public ExecutionContext m_cxt;
/*     */ 
/*     */   public SearchUpdateCacheEventHandler(SearchCache searchCache, Provider searchProvider, SearchDocChangeData changeData, int cacheFlags, Workspace ws)
/*     */   {
/*  67 */     this.m_searchCache = searchCache;
/*  68 */     this.m_searchProvider = searchProvider;
/*  69 */     this.m_changeData = changeData;
/*  70 */     this.m_cacheFlags = cacheFlags;
/*  71 */     this.m_ws = ws;
/*     */   }
/*     */ 
/*     */   public void doEvent(ExecutionContext cxt)
/*     */   {
/*  81 */     if (cxt != null)
/*     */     {
/*  83 */       this.m_cxt = cxt;
/*     */     }
/*     */     else
/*     */     {
/*  87 */       this.m_cxt = new ExecutionContextAdaptor();
/*     */     }
/*  89 */     this.m_cxt.setCachedObject("SearchUpdateChangeUtils", this);
/*     */     try
/*     */     {
/*  92 */       SearchUpdateChangeUtils.checkForDocUpdateChanges(this);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  96 */       Report.error(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getEventID()
/*     */   {
/* 102 */     String provID = (this.m_searchProvider != null) ? this.m_searchProvider.getName() : "##LocalSearch";
/* 103 */     return provID + ":updatedocdiffs";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 108 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchUpdateCacheEventHandler
 * JD-Core Version:    0.5.4
 */