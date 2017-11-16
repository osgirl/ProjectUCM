/*    */ package intradoc.search;
/*    */ 
/*    */ import intradoc.common.Table;
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class ProviderCache
/*    */ {
/* 26 */   public String m_providerName = null;
/* 27 */   public String m_primaryKey = null;
/* 28 */   public String m_specificRevKey = null;
/* 29 */   public String m_titleRevKey = null;
/*    */   public Map<String, CacheObject> m_queryCaches;
/*    */   public boolean m_isInvalidated;
/*    */   public int m_currentAge;
/*    */   public long[] m_invalidationTimes;
/*    */   public String[] m_additionalCapturedFields;
/*    */   public Map<String, CacheDataDesign> m_cacheDataDesigns;
/*    */   public long m_generationalSearchCounter;
/*    */ 
/*    */   public ProviderCache(String providerName)
/*    */   {
/* 62 */     this.m_providerName = providerName;
/*    */   }
/*    */ 
/*    */   public void init()
/*    */   {
/* 67 */     this.m_queryCaches = new HashMap();
/* 68 */     this.m_cacheDataDesigns = new HashMap();
/* 69 */     this.m_invalidationTimes = new long[50];
/* 70 */     for (int i = 0; i < this.m_invalidationTimes.length; ++i)
/*    */     {
/* 72 */       this.m_invalidationTimes[i] = -1L;
/*    */     }
/* 74 */     this.m_additionalCapturedFields = SearchCacheUtils.getCapturedResultFields().m_colNames;
/* 75 */     this.m_primaryKey = "dDocName";
/* 76 */     this.m_specificRevKey = "dID";
/* 77 */     this.m_titleRevKey = "dDocTitle";
/*    */   }
/*    */ 
/*    */   public ProviderCache shallowClone()
/*    */   {
/* 82 */     ProviderCache newCache = new ProviderCache(this.m_providerName);
/* 83 */     newCache.m_primaryKey = this.m_primaryKey;
/* 84 */     newCache.m_queryCaches = this.m_queryCaches;
/* 85 */     newCache.m_currentAge = this.m_currentAge;
/* 86 */     newCache.m_additionalCapturedFields = this.m_additionalCapturedFields;
/*    */ 
/* 89 */     return newCache;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 94 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.ProviderCache
 * JD-Core Version:    0.5.4
 */