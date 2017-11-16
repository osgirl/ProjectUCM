/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.resource.ResourceCacheInfo;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcLinkedHashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class UserTempCache
/*     */ {
/*     */   protected Map<String, ResourceCacheInfo> m_userCacheMap;
/*     */   protected int m_userCacheTimeout;
/*     */   protected int m_maxSize;
/*     */   protected boolean m_enableClusterCache;
/*     */   public long m_lastExternalRefreshTime;
/*     */ 
/*     */   public UserTempCache()
/*     */   {
/*  39 */     this.m_maxSize = SharedObjects.getEnvironmentInt("UserCacheMaxSize", 5000);
/*     */ 
/*  41 */     this.m_userCacheMap = new IdcLinkedHashMap(this.m_maxSize);
/*  42 */     this.m_userCacheTimeout = SharedObjects.getTypedEnvironmentInt("UserCacheTimeout", 60000, 18, 18);
/*     */ 
/*  45 */     this.m_enableClusterCache = SharedObjects.getEnvValueAsBoolean("UserCacheClusterSupport", false);
/*  46 */     this.m_lastExternalRefreshTime = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public boolean isExternalLoadExpired(String name)
/*     */   {
/*  51 */     Object obj = this.m_userCacheMap.get(name.toLowerCase());
/*  52 */     if (obj == null)
/*     */     {
/*  54 */       return false;
/*     */     }
/*  56 */     ResourceCacheInfo info = (ResourceCacheInfo)obj;
/*  57 */     return this.m_lastExternalRefreshTime > info.m_lastExternalLoadTime;
/*     */   }
/*     */ 
/*     */   public UserData getCachedUserData(String name)
/*     */   {
/*  63 */     Object obj = this.m_userCacheMap.get(name.toLowerCase());
/*  64 */     if (obj == null)
/*     */     {
/*  66 */       return null;
/*     */     }
/*     */ 
/*  69 */     long curTime = System.currentTimeMillis();
/*  70 */     ResourceCacheInfo info = (ResourceCacheInfo)obj;
/*  71 */     UserData userData = (UserData)info.m_resourceObj;
/*  72 */     if ((curTime > info.m_removalTS) && (userData != null))
/*     */     {
/*  74 */       userData.m_isExpired = true;
/*     */     }
/*  76 */     return userData;
/*     */   }
/*     */ 
/*     */   public void putCachedUserData(String name, UserData userData)
/*     */   {
/*  81 */     putCachedUserDataEx(name, userData, 0L);
/*     */   }
/*     */ 
/*     */   private void putCachedUserDataEx(String name, UserData userData, long removalTS)
/*     */   {
/*  86 */     ResourceCacheInfo info = new ResourceCacheInfo();
/*  87 */     long curTime = System.currentTimeMillis();
/*  88 */     info.m_lastLoaded = curTime;
/*  89 */     info.m_lastExternalLoadTime = curTime;
/*  90 */     if (removalTS > 0L)
/*     */     {
/*  92 */       info.m_removalTS = removalTS;
/*     */     }
/*     */     else
/*     */     {
/*  96 */       info.m_removalTS = (curTime + this.m_userCacheTimeout);
/*     */     }
/*  98 */     info.m_resourceObj = userData;
/*  99 */     synchronized (this.m_userCacheMap)
/*     */     {
/* 101 */       this.m_userCacheMap.put(name.toLowerCase(), info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeCachedUserData(String name)
/*     */   {
/* 107 */     synchronized (this.m_userCacheMap)
/*     */     {
/* 109 */       this.m_userCacheMap.remove(name.toLowerCase());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void replaceCachedUserData(String name, UserData userData)
/*     */   {
/* 116 */     Object obj = this.m_userCacheMap.get(name.toLowerCase());
/* 117 */     long removalTS = 0L;
/* 118 */     if (obj != null)
/*     */     {
/* 120 */       ResourceCacheInfo info = (ResourceCacheInfo)obj;
/* 121 */       removalTS = info.m_removalTS;
/*     */     }
/* 123 */     putCachedUserDataEx(name, userData, removalTS);
/*     */   }
/*     */ 
/*     */   public Set<String> getCachedUserNames()
/*     */   {
/* 128 */     return this.m_userCacheMap.keySet();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104244 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserTempCache
 * JD-Core Version:    0.5.4
 */