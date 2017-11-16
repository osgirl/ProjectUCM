/*     */ package intradoc.autosuggest.datastore;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.cache.IdcCacheFactory;
/*     */ import intradoc.server.cache.IdcCacheRegion;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.io.Serializable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class CacheStorage
/*     */ {
/*     */   public String m_cacheNamespace;
/*     */   public IdcCacheRegion m_cache;
/*     */ 
/*     */   public CacheStorage(String cacheRegionName, String cacheNamespace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  40 */     this.m_cache = IdcCacheFactory.getCacheRegion(cacheRegionName, true);
/*  41 */     this.m_cacheNamespace = cacheNamespace;
/*     */   }
/*     */ 
/*     */   public synchronized void put(String key, Serializable object)
/*     */     throws DataException
/*     */   {
/*  55 */     String originalKey = key;
/*  56 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/*  57 */     strBuilder.append(this.m_cacheNamespace).append('.').append(key);
/*  58 */     key = strBuilder.toString();
/*  59 */     this.m_cache.put(key, object);
/*  60 */     if (originalKey.equalsIgnoreCase("#IdcCount"))
/*     */       return;
/*  62 */     putCount(getCount() + 1L);
/*     */   }
/*     */ 
/*     */   public synchronized void update(String key, Serializable object)
/*     */     throws DataException
/*     */   {
/*  76 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/*  77 */     strBuilder.append(this.m_cacheNamespace).append('.').append(key);
/*  78 */     key = strBuilder.toString();
/*  79 */     this.m_cache.put(key, object);
/*     */   }
/*     */ 
/*     */   public Object get(String key)
/*     */     throws DataException
/*     */   {
/*  93 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/*  94 */     strBuilder.append(this.m_cacheNamespace).append('.').append(key);
/*  95 */     key = strBuilder.toString();
/*  96 */     Object fetched = this.m_cache.get(key);
/*  97 */     return fetched;
/*     */   }
/*     */ 
/*     */   public void remove(String key)
/*     */     throws DataException
/*     */   {
/* 110 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 111 */     strBuilder.append(this.m_cacheNamespace).append('.').append(key);
/* 112 */     key = strBuilder.toString();
/* 113 */     this.m_cache.remove(key);
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */     throws DataException
/*     */   {
/* 122 */     Report.trace("autosuggest", "Clearing entries in cache for : " + this.m_cacheNamespace, null);
/* 123 */     Iterator iterator = this.m_cache.keyset().iterator();
/* 124 */     while (iterator.hasNext())
/*     */     {
/* 126 */       String key = (String)iterator.next();
/* 127 */       if (key.startsWith(this.m_cacheNamespace + "."))
/*     */       {
/* 129 */         iterator.remove();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized long getCount()
/*     */     throws DataException
/*     */   {
/* 141 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 142 */     strBuilder.append(this.m_cacheNamespace).append('.').append("#IdcCount");
/* 143 */     String countKey = strBuilder.toString();
/* 144 */     Object fetchedCount = this.m_cache.get(countKey);
/* 145 */     if (fetchedCount == null)
/*     */     {
/* 147 */       return 0L;
/*     */     }
/* 149 */     Long countLong = (Long)fetchedCount;
/* 150 */     long count = countLong.longValue();
/* 151 */     if (Report.m_verbose)
/*     */     {
/* 153 */       Report.trace("autosuggest", countKey + " - Retrieved count " + count, null);
/*     */     }
/* 155 */     return count;
/*     */   }
/*     */ 
/*     */   public synchronized void putCount(long count)
/*     */     throws DataException
/*     */   {
/* 166 */     IdcStringBuilder strBuilder = new IdcStringBuilder();
/* 167 */     strBuilder.append(this.m_cacheNamespace).append('.').append("#IdcCount");
/* 168 */     String countKey = strBuilder.toString();
/* 169 */     Long countLong = new Long(count);
/* 170 */     this.m_cache.put(countKey, countLong);
/* 171 */     if (!Report.m_verbose)
/*     */       return;
/* 173 */     Report.trace("autosuggest", countKey + " - Updated count " + count, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 179 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105661 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.CacheStorage
 * JD-Core Version:    0.5.4
 */