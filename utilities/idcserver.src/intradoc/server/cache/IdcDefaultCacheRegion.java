/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.lang.SoftHashMap;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.server.SubjectManager;
/*     */ import java.io.Serializable;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcDefaultCacheRegion
/*     */   implements IdcCacheRegion
/*     */ {
/*     */   private SoftHashMap m_cacheRegion;
/*     */   private SoftHashMap m_cacheMisses;
/*     */   private String m_regionName;
/*  50 */   private List<IdcCacheListener> m_cacheRegionListeners = new ArrayList();
/*     */   private IdcCacheStore m_cacheStore;
/*     */   private boolean m_isPersistentCache;
/*     */   private boolean m_isAutoExpiryCache;
/*  61 */   private String m_autoExpiryTime = null;
/*     */   private String m_serviceName;
/*     */ 
/*     */   public IdcDefaultCacheRegion(String cacheRegionName)
/*     */   {
/*  70 */     this(cacheRegionName, false, false, null);
/*     */   }
/*     */ 
/*     */   public IdcDefaultCacheRegion(String cacheRegionName, boolean isPersistent)
/*     */   {
/*  80 */     this(cacheRegionName, isPersistent, false, null);
/*     */   }
/*     */ 
/*     */   public IdcDefaultCacheRegion(String cacheRegionName, boolean isPersistent, boolean isAutoExpiry, String autoExpiryTime)
/*     */   {
/*  93 */     this.m_cacheRegion = new SoftHashMap();
/*  94 */     this.m_cacheMisses = new SoftHashMap();
/*  95 */     this.m_regionName = cacheRegionName;
/*  96 */     this.m_isPersistentCache = isPersistent;
/*  97 */     if (isAutoExpiry)
/*     */     {
/*  99 */       this.m_isAutoExpiryCache = true;
/*     */     }
/* 101 */     if (this.m_isAutoExpiryCache)
/*     */     {
/*     */       long parsedAutoExpiryTime;
/*     */       try
/*     */       {
/* 108 */         parsedAutoExpiryTime = IdcCacheFactory.parseTime(autoExpiryTime);
/* 109 */         this.m_autoExpiryTime = (Long.toString(parsedAutoExpiryTime) + "MS");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 113 */         Report.trace("idccache", e.getMessage() + ", time string = " + autoExpiryTime + ". Setting autoexpiry time to 1 hour.", e);
/* 114 */         parsedAutoExpiryTime = 3600000L;
/* 115 */         this.m_autoExpiryTime = (Long.toString(parsedAutoExpiryTime) + "MS");
/*     */       }
/* 117 */       this.m_cacheStore = new IdcCacheStore(cacheRegionName, parsedAutoExpiryTime);
/*     */     }
/*     */     else
/*     */     {
/* 121 */       this.m_cacheStore = new IdcCacheStore(cacheRegionName);
/*     */     }
/*     */ 
/* 124 */     this.m_serviceName = IdcCacheFactory.getServiceName(this.m_isPersistentCache, this.m_isAutoExpiryCache, this.m_autoExpiryTime);
/* 125 */     if (!IdcDefaultCacheManager.m_cacheSubjectNotification) {
/*     */       return;
/*     */     }
/*     */ 
/* 129 */     Workspace ws = this.m_cacheStore.getWorkspace();
/* 130 */     if (ws == null)
/*     */       return;
/* 132 */     SubjectCallbackAdapter cacheEventSubjectCallback = new IdcCacheEventSubjectCallback();
/* 133 */     cacheEventSubjectCallback.setWorkspace(ws);
/* 134 */     SubjectManager.registerCallback("idccacheevent-" + this.m_serviceName + "." + cacheRegionName, cacheEventSubjectCallback);
/*     */   }
/*     */ 
/*     */   public void put(String key, Serializable value)
/*     */   {
/* 146 */     int eventType = 0;
/* 147 */     Serializable oldValue = get(key);
/* 148 */     if (oldValue != null)
/*     */     {
/* 150 */       eventType = 1;
/*     */     }
/*     */ 
/* 153 */     removeCacheMiss(key);
/* 154 */     this.m_cacheRegion.put(key, value);
/* 155 */     if (IdcDefaultCacheManager.m_cacheSubjectNotification)
/*     */     {
/* 157 */       this.m_cacheStore.store(key, value);
/* 158 */       SubjectManager.notifyChanged("idccacheevent-" + this.m_regionName);
/*     */     }
/* 160 */     if (oldValue == value)
/*     */       return;
/* 162 */     notifyListeners(new IdcCacheEvent(this, eventType, key, value));
/*     */   }
/*     */ 
/*     */   public Serializable get(String key)
/*     */   {
/* 173 */     Serializable oValue = (Serializable)this.m_cacheRegion.get(key);
/* 174 */     if ((IdcDefaultCacheManager.m_cacheSubjectNotification) && 
/* 176 */       (oValue == null) && (!isCacheMiss(key)))
/*     */     {
/* 178 */       oValue = (Serializable)this.m_cacheStore.load(key);
/* 179 */       if (oValue != null)
/*     */       {
/* 181 */         this.m_cacheRegion.put(key, oValue);
/*     */       }
/*     */       else
/*     */       {
/* 185 */         putCacheMiss(key);
/*     */       }
/*     */     }
/*     */ 
/* 189 */     return oValue;
/*     */   }
/*     */ 
/*     */   public void remove(String key)
/*     */   {
/* 198 */     this.m_cacheRegion.remove(key);
/*     */ 
/* 200 */     if (IdcDefaultCacheManager.m_cacheSubjectNotification)
/*     */     {
/* 202 */       this.m_cacheStore.erase(key);
/* 203 */       SubjectManager.notifyChanged("idccacheevent-" + this.m_regionName);
/*     */     }
/* 205 */     notifyListeners(new IdcCacheEvent(this, 2, key, null));
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/* 213 */     Iterator iter = null;
/* 214 */     if (IdcDefaultCacheManager.m_cacheSubjectNotification)
/*     */     {
/* 216 */       iter = this.m_cacheStore.keys();
/*     */ 
/* 218 */       while (iter.hasNext())
/*     */       {
/* 220 */         String key = (String)iter.next();
/* 221 */         remove(key);
/*     */       }
/*     */     }
/* 224 */     this.m_cacheRegion.clear();
/*     */   }
/*     */ 
/*     */   public String getRegionName()
/*     */   {
/* 233 */     return this.m_regionName;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 244 */     return this.m_cacheRegion.size() == 0;
/*     */   }
/*     */ 
/*     */   public Set keyset()
/*     */   {
/* 255 */     return this.m_cacheRegion.keySet();
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 263 */     return this.m_cacheRegion.size();
/*     */   }
/*     */ 
/*     */   public IdcCacheRegion getCacheRegion(String regionName)
/*     */   {
/* 273 */     String regionNameWithService = this.m_serviceName + "." + regionName;
/* 274 */     if (!IdcDefaultCacheManager.containsCacheRegion(regionNameWithService))
/*     */     {
/* 276 */       IdcDefaultCacheManager.putCacheRegion(regionNameWithService, this);
/*     */     }
/* 278 */     return IdcDefaultCacheManager.getCacheRegion(regionNameWithService);
/*     */   }
/*     */ 
/*     */   public boolean isPersistenceSupported()
/*     */   {
/* 288 */     return true;
/*     */   }
/*     */ 
/*     */   public void addListener(IdcCacheListener listener)
/*     */   {
/* 298 */     if ((listener == null) || (this.m_cacheRegionListeners.contains(listener)))
/*     */       return;
/* 300 */     this.m_cacheRegionListeners.add(listener);
/*     */   }
/*     */ 
/*     */   public void removeListener(IdcCacheListener listener)
/*     */   {
/* 310 */     if (listener == null)
/*     */       return;
/* 312 */     this.m_cacheRegionListeners.remove(listener);
/*     */   }
/*     */ 
/*     */   public void loadAllFromPersistentStore()
/*     */   {
/* 321 */     for (Iterator iter = this.m_cacheStore.keys(); iter.hasNext(); )
/*     */     {
/* 323 */       Map.Entry entry = (Map.Entry)iter.next();
/* 324 */       this.m_cacheStore.load(entry.getKey());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void putAll(Map<String, Serializable> map)
/*     */   {
/* 335 */     for (Iterator iter = map.entrySet().iterator(); iter.hasNext(); )
/*     */     {
/* 337 */       Map.Entry entry = (Map.Entry)iter.next();
/* 338 */       put((String)entry.getKey(), (Serializable)entry.getValue());
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isPersistentCache()
/*     */   {
/* 350 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean isAutoExpiryCache()
/*     */   {
/* 359 */     return this.m_isAutoExpiryCache;
/*     */   }
/*     */ 
/*     */   public String getAutoExpiryTime()
/*     */   {
/* 368 */     return this.m_autoExpiryTime;
/*     */   }
/*     */ 
/*     */   private void notifyListeners(IdcCacheEvent event)
/*     */   {
/* 377 */     for (Iterator iterator = this.m_cacheRegionListeners.iterator(); iterator.hasNext(); )
/*     */     {
/* 379 */       IdcCacheListener listener = (IdcCacheListener)iterator.next();
/* 380 */       int eventType = event.getEventType();
/* 381 */       switch (eventType)
/*     */       {
/*     */       case 0:
/* 384 */         listener.insertEvent(event);
/* 385 */         break;
/*     */       case 1:
/* 388 */         listener.updateEvent(event);
/* 389 */         break;
/*     */       case 2:
/* 392 */         listener.deleteEvent(event);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void insertSubjectNotification(String key, Serializable value)
/*     */   {
/* 409 */     removeCacheMiss(key);
/* 410 */     this.m_cacheRegion.put(key, value);
/* 411 */     IdcCacheEvent event = new IdcCacheEvent(this, 0, key, value);
/* 412 */     notifyListeners(event);
/* 413 */     if (!Report.m_verbose)
/*     */       return;
/* 415 */     Report.trace("idccache", "Insert cache event received through Subject Notification on '" + this.m_regionName + "', key=" + key, null);
/*     */   }
/*     */ 
/*     */   public void updateSubjectNotification(String key, Serializable value)
/*     */   {
/* 427 */     removeCacheMiss(key);
/* 428 */     this.m_cacheRegion.put(key, value);
/* 429 */     IdcCacheEvent event = new IdcCacheEvent(this, 1, key, value);
/* 430 */     notifyListeners(event);
/* 431 */     if (!Report.m_verbose)
/*     */       return;
/* 433 */     Report.trace("idccache", "Update cache event received through Subject Notification on '" + this.m_regionName + "', key=" + key, null);
/*     */   }
/*     */ 
/*     */   public void deleteSubjectNotification(String key)
/*     */   {
/* 444 */     this.m_cacheRegion.remove(key);
/* 445 */     IdcCacheEvent event = new IdcCacheEvent(this, 2, key, null);
/* 446 */     notifyListeners(event);
/* 447 */     if (!Report.m_verbose)
/*     */       return;
/* 449 */     Report.trace("idccache", "Delete cache event received through Subject Notification on '" + this.m_regionName + "', key=" + key, null);
/*     */   }
/*     */ 
/*     */   private void putCacheMiss(String key)
/*     */   {
/* 455 */     this.m_cacheMisses.put(key, "");
/*     */   }
/*     */ 
/*     */   private void removeCacheMiss(String key) {
/* 459 */     if (!this.m_cacheMisses.containsKey(key))
/*     */       return;
/* 461 */     this.m_cacheMisses.remove(key);
/*     */   }
/*     */ 
/*     */   private boolean isCacheMiss(String key)
/*     */   {
/* 466 */     return this.m_cacheMisses.containsKey(key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 471 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101532 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcDefaultCacheRegion
 * JD-Core Version:    0.5.4
 */