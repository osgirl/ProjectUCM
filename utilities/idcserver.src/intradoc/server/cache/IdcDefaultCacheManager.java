/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.jdbc.JdbcWorkspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Set;
/*     */ import java.util.Timer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcDefaultCacheManager
/*     */ {
/*     */   private static JdbcWorkspace m_workspace;
/*  42 */   private static HashMap m_cacheRegionList = null;
/*     */ 
/*  44 */   public static boolean m_cacheSubjectNotification = false;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  51 */     m_cacheRegionList = new HashMap();
/*     */ 
/*  53 */     Provider wsProvider = Providers.getProvider("SystemDatabase");
/*  54 */     if (wsProvider != null)
/*     */     {
/*  56 */       m_workspace = (JdbcWorkspace)wsProvider.getProvider();
/*  57 */       scheduleCacheStoreCleanup();
/*  58 */       scheduleUpdateAutoExpiredEntries();
/*     */     }
/*  60 */     m_cacheSubjectNotification = SharedObjects.getEnvValueAsBoolean("DefaultCacheSubjectNotifications", false);
/*  61 */     Report.trace("idccache", "SoftMap based cache initialization successful", null);
/*     */   }
/*     */ 
/*     */   private static void scheduleUpdateAutoExpiredEntries()
/*     */   {
/*  70 */     Timer timer = new Timer("CacheAutoExpiredScheduler", true);
/*  71 */     String temp = SharedObjects.getEnvironmentValue("DefaultCacheUpdateExpiredInterval");
/*  72 */     long scheduleTime = 0L;
/*  73 */     if (temp == null)
/*     */     {
/*  75 */       scheduleTime = 7200000L;
/*     */     }
/*     */     else
/*     */     {
/*  79 */       scheduleTime = Long.parseLong(temp);
/*     */     }
/*  81 */     IdcCacheAutoExpiredScheduler autoExpiryThread = new IdcCacheAutoExpiredScheduler();
/*  82 */     autoExpiryThread.setWorkspace(m_workspace);
/*  83 */     timer.schedule(autoExpiryThread, 0L, scheduleTime);
/*  84 */     Report.trace("idccache", "A timer task is scheduled to update auto expired entries once in every " + scheduleTime + " msec.", null);
/*     */   }
/*     */ 
/*     */   private static void scheduleCacheStoreCleanup()
/*     */   {
/*  93 */     Timer timer = new Timer("CacheStoreCleanupScheduler", true);
/*  94 */     String temp = SharedObjects.getEnvironmentValue("DefaultCacheStoreCleanupInterval");
/*  95 */     long scheduleTime = 0L;
/*  96 */     if (temp == null)
/*     */     {
/*  98 */       scheduleTime = 86400000L;
/*     */     }
/*     */     else
/*     */     {
/* 102 */       scheduleTime = Long.parseLong(temp);
/*     */     }
/* 104 */     IdcCacheStoreCleanupScheduler cleanupThread = new IdcCacheStoreCleanupScheduler();
/* 105 */     cleanupThread.setWorkspace(m_workspace);
/* 106 */     timer.schedule(cleanupThread, 0L, scheduleTime);
/* 107 */     Report.trace("idccache", "A timer task is scheduled to clean expired entries in cache store once in every " + scheduleTime + " msec.", null);
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getCacheRegion(String regionName)
/*     */   {
/* 117 */     return (IdcCacheRegion)m_cacheRegionList.get(regionName);
/*     */   }
/*     */ 
/*     */   public static void putCacheRegion(String regionName, IdcCacheRegion cacheRegion)
/*     */   {
/* 127 */     m_cacheRegionList.put(regionName, cacheRegion);
/*     */   }
/*     */ 
/*     */   public static boolean containsCacheRegion(String regionName)
/*     */   {
/* 138 */     return m_cacheRegionList.containsKey(regionName);
/*     */   }
/*     */ 
/*     */   public static DataResultSet getCachesList()
/*     */   {
/* 146 */     Iterator it = m_cacheRegionList.keySet().iterator();
/* 147 */     DataResultSet drset = new DataResultSet(IdcCacheFactory.m_cacheFields);
/* 148 */     while ((it != null) && 
/* 150 */       (it.hasNext()))
/*     */     {
/* 152 */       Vector v = new IdcVector();
/* 153 */       String cacheNameWithPrefix = (String)it.next();
/* 154 */       String serviceName = cacheNameWithPrefix.substring(0, cacheNameWithPrefix.indexOf(46));
/* 155 */       String cacheName = cacheNameWithPrefix.substring(cacheNameWithPrefix.indexOf(46) + 1);
/* 156 */       boolean isPersistent = IdcCacheFactory.isPersistentService(serviceName);
/* 157 */       boolean isAutoExpiry = IdcCacheFactory.isAutoExpiryService(serviceName);
/* 158 */       String autoExpiryTime = IdcCacheFactory.getAutoExpiryTimeFromService(serviceName);
/*     */       try
/*     */       {
/* 162 */         IdcCacheRegion cacheRegion = IdcCacheFactory.getCacheRegion(cacheName, isPersistent, isAutoExpiry, autoExpiryTime);
/* 163 */         v.addElement(cacheName);
/* 164 */         v.addElement(Integer.valueOf(cacheRegion.size()));
/* 165 */         v.addElement(Boolean.valueOf(isPersistent));
/* 166 */         v.addElement(Boolean.valueOf(isAutoExpiry));
/* 167 */         if (autoExpiryTime == null)
/*     */         {
/* 169 */           autoExpiryTime = "";
/*     */         }
/* 171 */         v.addElement(autoExpiryTime);
/* 172 */         drset.addRow(v);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 176 */         String errMsg = LocaleUtils.encodeMessage("csIdcCacheGetRegionsFailed", null);
/* 177 */         Report.error("idccache", errMsg, e);
/*     */       }
/*     */     }
/*     */ 
/* 181 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 186 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101532 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcDefaultCacheManager
 * JD-Core Version:    0.5.4
 */