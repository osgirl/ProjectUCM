/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ConcurrentModificationException;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcCacheServiceHandler extends ServiceHandler
/*     */ {
/*     */   public IdcCacheRegion prepareCacheRegion(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  49 */     String cacheName = this.m_binder.getLocal("dCacheRegionName");
/*  50 */     if (cacheName == null)
/*     */     {
/*  52 */       String msg = LocaleUtils.encodeMessage("csIdcCacheServiceMissingParam", null, "dCacheRegionName");
/*  53 */       throw new DataException(msg);
/*     */     }
/*     */ 
/*  56 */     boolean isPersistent = DataBinderUtils.getBoolean(this.m_binder, "isPersistent", false);
/*  57 */     boolean isAutoExpiry = DataBinderUtils.getBoolean(this.m_binder, "isAutoExpiry", false);
/*  58 */     String autoExpiryTime = null;
/*  59 */     if (isAutoExpiry)
/*     */     {
/*  61 */       autoExpiryTime = this.m_binder.getLocal("autoExpiryTime");
/*  62 */       if (autoExpiryTime == null)
/*     */       {
/*  64 */         String errMsg = LocaleUtils.encodeMessage("csIdcCacheInvalidAutoExpiryTime", null);
/*  65 */         throw new DataException(errMsg);
/*     */       }
/*     */     }
/*  68 */     IdcCacheRegion cache = IdcCacheFactory.getCacheRegion(cacheName, isPersistent, isAutoExpiry, autoExpiryTime);
/*  69 */     return cache;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getCacheRegionsList()
/*     */     throws DataException, ServiceException
/*     */   {
/*  80 */     DataResultSet caches = IdcCacheFactory.getCachesList();
/*  81 */     this.m_binder.addResultSet("CacheRegionsList", caches);
/*  82 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void putToCache()
/*     */     throws DataException, ServiceException
/*     */   {
/*  93 */     String key = this.m_binder.getLocal("dCacheKey");
/*  94 */     if (key == null)
/*     */     {
/*  96 */       String msg = LocaleUtils.encodeMessage("csIdcCacheServiceMissingParam", null, "dCacheKey");
/*  97 */       throw new DataException(msg);
/*     */     }
/*  99 */     String value = this.m_binder.getLocal("dCacheValue");
/* 100 */     if (value == null)
/*     */     {
/* 102 */       String msg = LocaleUtils.encodeMessage("csIdcCacheServiceMissingParam", null, "dCacheValue");
/* 103 */       throw new DataException(msg);
/*     */     }
/* 105 */     IdcCacheRegion cache = prepareCacheRegion(this.m_binder);
/* 106 */     cache.put(key, value);
/* 107 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getFromCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 118 */     String key = this.m_binder.getLocal("dCacheKey");
/* 119 */     if (key == null)
/*     */     {
/* 121 */       String msg = LocaleUtils.encodeMessage("csIdcCacheServiceMissingParam", null, "dCacheKey");
/* 122 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 125 */     IdcCacheRegion cache = prepareCacheRegion(this.m_binder);
/* 126 */     Object value = cache.get(key);
/* 127 */     this.m_binder.putLocal("dCacheValue", String.valueOf(value));
/* 128 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void removeFromCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 139 */     String key = this.m_binder.getLocal("dCacheKey");
/* 140 */     if (key == null)
/*     */     {
/* 142 */       String msg = LocaleUtils.encodeMessage("csIdcCacheServiceMissingParam", null, "dCacheKey");
/* 143 */       throw new DataException(msg);
/*     */     }
/* 145 */     IdcCacheRegion cache = prepareCacheRegion(this.m_binder);
/* 146 */     cache.remove(key);
/* 147 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void clearCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 158 */     IdcCacheRegion cache = prepareCacheRegion(this.m_binder);
/* 159 */     cache.clear();
/* 160 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getAllKeyValues()
/*     */     throws DataException, ServiceException
/*     */   {
/* 171 */     IdcCacheRegion cache = prepareCacheRegion(this.m_binder);
/*     */ 
/* 173 */     String[] getCacheFields = { "dCacheKey", "dCacheValue" };
/* 174 */     DataResultSet caches = new DataResultSet(getCacheFields);
/* 175 */     String autoExpiryTime = cache.getAutoExpiryTime();
/*     */     try
/*     */     {
/* 178 */       if (autoExpiryTime == null)
/*     */       {
/* 180 */         autoExpiryTime = "";
/*     */       }
/* 182 */       this.m_binder.putLocal("autoExpiryTime", autoExpiryTime);
/* 183 */       this.m_binder.putLocal("size", Integer.toString(cache.size()));
/* 184 */       this.m_binder.putLocal("isPersistent", (cache.isPersistentCache()) ? "1" : "0");
/* 185 */       this.m_binder.putLocal("isAutoExpiry", (cache.isAutoExpiryCache()) ? "1" : "0");
/*     */ 
/* 187 */       Iterator it = cache.keyset().iterator();
/* 188 */       while ((it != null) && 
/* 190 */         (it.hasNext()))
/*     */       {
/* 192 */         String key = (String)it.next();
/* 193 */         Object value = cache.get(key);
/* 194 */         Vector v = new IdcVector();
/* 195 */         v.addElement(key);
/* 196 */         v.addElement(String.valueOf(value));
/* 197 */         caches.addRow(v);
/*     */       }
/*     */ 
/* 200 */       this.m_binder.addResultSet("CacheKeyValues", caches);
/*     */     }
/*     */     catch (ConcurrentModificationException e)
/*     */     {
/* 204 */       Report.trace("idccache", "Cache region " + cache.getRegionName() + " is being updated", e);
/*     */     }
/* 206 */     this.m_binder.putLocal("StatusCode", Integer.toString(0));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 211 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100229 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheServiceHandler
 * JD-Core Version:    0.5.4
 */