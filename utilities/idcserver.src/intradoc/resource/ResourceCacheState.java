/*     */ package intradoc.resource;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ResourceCacheState
/*     */ {
/*  40 */   protected static Hashtable m_cacheInfo = new Hashtable();
/*     */ 
/*  47 */   protected static Vector m_resourceList = new IdcVector();
/*     */ 
/*  53 */   protected static Hashtable m_resourceInfo = new Hashtable();
/*     */ 
/*  58 */   protected static Hashtable m_temporaryCaches = new Hashtable();
/*     */ 
/*  63 */   protected static ResourceCacheInfo m_tempStart = null;
/*     */ 
/*  68 */   protected static ResourceCacheInfo m_tempEnd = null;
/*     */ 
/*  73 */   static long m_tempResourcesTotalSize = 0L;
/*     */ 
/*  79 */   protected static int m_ageResourceFilesTimeoutInSecs = 30;
/*     */ 
/*  85 */   protected static int m_removeResourceFilesTimeoutInMins = 120;
/*     */ 
/*  94 */   protected static int m_maximumSizeMillions = 10;
/*     */ 
/*     */   public static int getAgeResourceFilesTimeoutInSecs()
/*     */   {
/*  99 */     return m_ageResourceFilesTimeoutInSecs;
/*     */   }
/*     */ 
/*     */   public static void setRemoveResourceFilesTimeoutInMins(int removeResourceFilesTimeoutInMins)
/*     */   {
/* 104 */     m_removeResourceFilesTimeoutInMins = removeResourceFilesTimeoutInMins;
/*     */   }
/*     */ 
/*     */   public static int getRemoveResourceFilesTimeoutInMins()
/*     */   {
/* 109 */     return m_removeResourceFilesTimeoutInMins;
/*     */   }
/*     */ 
/*     */   public static void setAgeResourceFilesTimeoutInSecs(int ageResourceFilesTimeoutInSecs)
/*     */   {
/* 114 */     m_ageResourceFilesTimeoutInSecs = ageResourceFilesTimeoutInSecs;
/*     */   }
/*     */ 
/*     */   public static int getMaxSizeResourceFileCacheMillions()
/*     */   {
/* 119 */     return m_maximumSizeMillions;
/*     */   }
/*     */ 
/*     */   public static void setMaxSizeResourceFileCacheMillions(int maximumSizeMillions)
/*     */   {
/* 124 */     m_maximumSizeMillions = maximumSizeMillions;
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo addCacheInfo(String key, String type, String filePath)
/*     */   {
/* 133 */     ResourceCacheInfo info = (ResourceCacheInfo)m_cacheInfo.get(key);
/* 134 */     if (info == null)
/*     */     {
/* 136 */       info = new ResourceCacheInfo();
/* 137 */       m_cacheInfo.put(key, info);
/*     */     }
/* 139 */     info.m_filePath = filePath;
/* 140 */     info.m_lookupKey = key;
/* 141 */     info.m_type = type;
/* 142 */     return info;
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo getCacheInfo(String key)
/*     */   {
/* 150 */     return (ResourceCacheInfo)m_cacheInfo.get(key);
/*     */   }
/*     */ 
/*     */   public static Vector getResourceList()
/*     */   {
/* 158 */     return m_resourceList;
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo addResourceInfo(String key, String filePath)
/*     */   {
/* 167 */     ResourceCacheInfo info = (ResourceCacheInfo)m_resourceInfo.get(key);
/* 168 */     if (info == null)
/*     */     {
/* 170 */       info = new ResourceCacheInfo();
/* 171 */       m_resourceList.addElement(info);
/* 172 */       m_resourceInfo.put(key, info);
/*     */     }
/* 174 */     info.m_lookupKey = key;
/* 175 */     info.m_filePath = filePath;
/* 176 */     return info;
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo prependResourceInfo(String key, String filePath)
/*     */   {
/* 186 */     ResourceCacheInfo info = (ResourceCacheInfo)m_resourceInfo.get(key);
/* 187 */     if (info == null)
/*     */     {
/* 189 */       info = new ResourceCacheInfo();
/* 190 */       m_resourceList.add(0, info);
/* 191 */       m_resourceInfo.put(key, info);
/*     */     }
/* 193 */     info.m_lookupKey = key;
/* 194 */     info.m_filePath = filePath;
/* 195 */     return info;
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo getTemporaryCache(String key, long curTime)
/*     */   {
/* 203 */     ResourceCacheInfo info = null;
/* 204 */     synchronized (m_temporaryCaches)
/*     */     {
/* 206 */       info = (ResourceCacheInfo)m_temporaryCaches.get(key);
/* 207 */       if ((info != null) && (info.m_removalTS < curTime))
/*     */       {
/* 209 */         m_temporaryCaches.remove(key);
/* 210 */         detachFromCache(info);
/* 211 */         info = null;
/*     */       }
/* 213 */       if (info != null)
/*     */       {
/* 215 */         promoteCache(info);
/*     */       }
/*     */     }
/*     */ 
/* 219 */     return info;
/*     */   }
/*     */ 
/*     */   public static void addTemporaryCache(String key, ResourceCacheInfo info)
/*     */   {
/* 228 */     synchronized (m_temporaryCaches)
/*     */     {
/* 230 */       ResourceCacheInfo oldInfo = (ResourceCacheInfo)m_temporaryCaches.get(key);
/* 231 */       if (oldInfo != null)
/*     */       {
/* 233 */         detachFromCache(oldInfo);
/*     */       }
/*     */ 
/* 236 */       info.m_lookupKey = key;
/* 237 */       if (info.m_size > 0L)
/*     */       {
/* 239 */         m_tempResourcesTotalSize += info.m_size;
/* 240 */         Report.trace("monitor", null, "csMonitorUpdateItemsTempLoad", new Object[] { Integer.valueOf(m_temporaryCaches.size()) });
/*     */ 
/* 242 */         Report.trace("monitor", null, "csMonitorUpdateTempItemsConsuming", new Object[] { Long.valueOf(m_tempResourcesTotalSize) });
/*     */       }
/*     */ 
/* 245 */       m_temporaryCaches.put(key, info);
/* 246 */       promoteCache(info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addTimedTemporaryCache(String key, ResourceCacheInfo info, long startTime)
/*     */   {
/* 253 */     info.m_agedTS = (startTime + m_ageResourceFilesTimeoutInSecs * 1000);
/* 254 */     info.m_removalTS = (startTime + m_removeResourceFilesTimeoutInMins * 60000);
/* 255 */     addTemporaryCache(key, info);
/* 256 */     checkTemporaryCache(startTime);
/*     */   }
/*     */ 
/*     */   public static ResourceCacheInfo removeTemporaryCache(String key)
/*     */   {
/* 262 */     ResourceCacheInfo info = null;
/* 263 */     synchronized (m_temporaryCaches)
/*     */     {
/* 265 */       info = (ResourceCacheInfo)m_temporaryCaches.remove(key);
/* 266 */       detachFromCache(info);
/*     */     }
/* 268 */     return info;
/*     */   }
/*     */ 
/*     */   public static void checkTemporaryCache(long curTime)
/*     */   {
/* 276 */     synchronized (m_temporaryCaches)
/*     */     {
/* 279 */       int count = 0;
/* 280 */       int max = m_maximumSizeMillions * 1000000;
/* 281 */       while ((m_tempEnd != null) && (count < 1000))
/*     */       {
/* 283 */         if ((m_tempResourcesTotalSize <= max) && (m_tempEnd.m_removalTS > curTime)) {
/*     */           break;
/*     */         }
/*     */ 
/* 287 */         ++count;
/* 288 */         if (m_tempEnd.m_lookupKey != null)
/*     */         {
/* 290 */           m_temporaryCaches.remove(m_tempEnd.m_lookupKey);
/*     */         }
/* 292 */         detachFromCache(m_tempEnd);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void getReport(boolean fullReport, DataBinder binder)
/*     */   {
/* 302 */     synchronized (m_temporaryCaches)
/*     */     {
/* 304 */       binder.putLocal("cacheInfoSize", "" + m_cacheInfo.size());
/* 305 */       binder.putLocal("resourceListSize", "" + m_resourceList.size());
/* 306 */       binder.putLocal("maximumCacheSizeMillions", "" + m_maximumSizeMillions);
/*     */ 
/* 308 */       DataResultSet drset = new DataResultSet(new String[] { "lookupKey", "size", "timeToRemoval", "nextRefreshCheck" });
/*     */ 
/* 310 */       long curTime = System.currentTimeMillis();
/* 311 */       int count = 0;
/*     */ 
/* 313 */       if ((m_temporaryCaches.size() > 0) || (m_tempResourcesTotalSize > 0L) || (m_tempStart != null))
/*     */       {
/* 316 */         binder.putLocal("temporaryCachesSize", "" + m_temporaryCaches.size());
/* 317 */         binder.putLocal("tempResourcesTotalSize", "" + m_tempResourcesTotalSize);
/* 318 */         if (fullReport)
/*     */         {
/* 320 */           ResourceCacheInfo info = m_tempStart;
/* 321 */           while (info != null)
/*     */           {
/* 324 */             if (count++ > 1000)
/*     */             {
/* 326 */               binder.putLocal("tooManyCacheItemsToReportAll", "1");
/* 327 */               break;
/*     */             }
/* 329 */             Vector v = new IdcVector();
/* 330 */             String key = info.m_lookupKey;
/* 331 */             if (key == null)
/*     */             {
/* 333 */               key = "----";
/*     */             }
/* 335 */             v.addElement(key);
/* 336 */             v.addElement("" + info.m_size);
/* 337 */             v.addElement("" + (info.m_removalTS - curTime) / 1000L);
/* 338 */             if (info.m_lookupKey.startsWith("idoc"))
/*     */             {
/* 340 */               long diff = info.m_agedTS - curTime;
/* 341 */               v.addElement("" + diff);
/*     */             }
/*     */             else
/*     */             {
/* 345 */               v.addElement("");
/*     */             }
/* 347 */             drset.addRow(v);
/*     */ 
/* 322 */             info = info.m_next;
/*     */           }
/*     */ 
/* 349 */           binder.addResultSet("TemporaryCaches", drset);
/*     */         }
/*     */         else
/*     */         {
/* 353 */           String simpleFilter = binder.getLocal("cacheReportSimpleFilter");
/* 354 */           long totalSize = 0L;
/* 355 */           ResourceCacheInfo info = m_tempStart;
/* 356 */           while (info != null)
/*     */           {
/* 359 */             if (count++ > 1000)
/*     */             {
/* 361 */               binder.putLocal("tooManyCacheItemsToReportAll", "1");
/* 362 */               break;
/*     */             }
/* 364 */             if ((info.m_lookupKey == null) || ((simpleFilter != null) && (info.m_lookupKey.indexOf(simpleFilter) >= 0)))
/*     */             {
/* 366 */               totalSize += info.m_size;
/*     */ 
/* 368 */               Vector v = new IdcVector();
/* 369 */               String key = info.m_lookupKey;
/* 370 */               if (key == null)
/*     */               {
/* 372 */                 key = "----";
/*     */               }
/* 374 */               v.addElement(key);
/* 375 */               v.addElement("" + info.m_size);
/* 376 */               v.addElement("" + (info.m_removalTS - curTime) / 1000L);
/* 377 */               v.addElement("");
/* 378 */               drset.addRow(v);
/*     */             }
/* 357 */             info = info.m_next;
/*     */           }
/*     */ 
/* 381 */           binder.putLocal("cacheReportTotalSize", "" + totalSize);
/*     */         }
/*     */       }
/* 384 */       binder.addResultSet("TemporaryCaches", drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void detachFromCache(ResourceCacheInfo res)
/*     */   {
/* 395 */     if (res == null)
/*     */     {
/* 397 */       return;
/*     */     }
/* 399 */     if (SystemUtils.m_verbose)
/*     */     {
/* 401 */       Report.trace("resourcecache", "Removing cache item [" + res.m_lookupKey + "] size: " + res.m_size, null);
/*     */     }
/*     */ 
/* 404 */     if (res == m_tempStart)
/*     */     {
/* 406 */       m_tempStart = res.m_next;
/*     */     }
/* 408 */     if (res == m_tempEnd)
/*     */     {
/* 410 */       m_tempEnd = res.m_prev;
/*     */     }
/* 412 */     res.detach();
/* 413 */     if (res.m_size <= 0L)
/*     */       return;
/* 415 */     m_tempResourcesTotalSize -= res.m_size;
/* 416 */     Report.trace("monitor", null, "csMonitorUpdateItemsTempLoad", new Object[] { Integer.valueOf(m_temporaryCaches.size()) });
/* 417 */     Report.trace("monitor", null, "csMonitorUpdateTempItemsConsuming", new Object[] { Long.valueOf(m_tempResourcesTotalSize) });
/*     */   }
/*     */ 
/*     */   protected static void promoteCache(ResourceCacheInfo res)
/*     */   {
/* 428 */     if (res == null)
/*     */     {
/* 430 */       return;
/*     */     }
/*     */ 
/* 433 */     if (m_tempStart == null)
/*     */     {
/* 435 */       m_tempStart = res;
/* 436 */       m_tempEnd = res;
/*     */     }
/*     */     else
/*     */     {
/* 440 */       if (m_tempEnd == null)
/*     */       {
/* 442 */         m_tempEnd = m_tempStart;
/*     */       }
/* 444 */       if (m_tempStart == res)
/*     */         return;
/* 446 */       if (res == m_tempEnd)
/*     */       {
/* 448 */         m_tempEnd = res.m_prev;
/*     */       }
/* 450 */       res.detach();
/* 451 */       m_tempStart.insertBeforeUs(res);
/* 452 */       m_tempStart = res;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 459 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98083 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceCacheState
 * JD-Core Version:    0.5.4
 */