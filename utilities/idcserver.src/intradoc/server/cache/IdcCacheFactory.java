/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcCacheFactory
/*     */ {
/*     */   private static Map<String, String> m_cacheInterfaceNames;
/*     */   private static IdcCacheRegion m_idcCacheRegion;
/*  47 */   public static String[] m_cacheFields = { "dCacheRegionName", "dCacheEntriesSize", "isPersistent", "isAutoExpiry", "autoExpiryTime" };
/*     */ 
/*     */   public static void init()
/*     */     throws ServiceException, DataException
/*     */   {
/*  56 */     String classname = getCacheManagerName();
/*  57 */     Class cl = ClassHelperUtils.createClass(classname);
/*     */     try
/*     */     {
/*  60 */       ClassHelperUtils.executeStaticMethod(cl, "init", null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  64 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getCacheRegion(String cacheRegionName)
/*     */     throws ServiceException, DataException
/*     */   {
/*  78 */     return getCacheRegion(cacheRegionName, false, false, null);
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getCacheRegion(String cacheRegionName, boolean isPersistent)
/*     */     throws ServiceException, DataException
/*     */   {
/*  92 */     return getCacheRegion(cacheRegionName, isPersistent, false, null);
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getCacheRegion(String cacheRegionName, boolean isPersistent, boolean isAutoExpiryEnabled, String autoExpiryTime)
/*     */     throws ServiceException, DataException
/*     */   {
/* 110 */     if ((isAutoExpiryEnabled) && 
/* 112 */       (autoExpiryTime != null) && (!autoExpiryTime.matches("(\\d)+(MS|ms|S|s|M|m|H|h|D|d)?")))
/*     */     {
/* 114 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheInvalidAutoExpiryTime", null);
/* 115 */       throw new DataException(errMsg);
/*     */     }
/*     */ 
/* 118 */     return getCacheRegion(cacheRegionName, getCacheInterfaceName(), isPersistent, isAutoExpiryEnabled, autoExpiryTime);
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getAutoExpiryCacheRegion(String cacheRegionName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 131 */     String time = SharedObjects.getEnvironmentValue("IdcCacheDefaultAutoExpiredTime");
/* 132 */     if (time == null)
/*     */     {
/* 134 */       time = "1H";
/*     */     }
/* 136 */     return getAutoExpiryCacheRegion(cacheRegionName, time);
/*     */   }
/*     */ 
/*     */   public static IdcCacheRegion getAutoExpiryCacheRegion(String cacheRegionName, String autoExpiryTime)
/*     */     throws ServiceException, DataException
/*     */   {
/* 151 */     if ((autoExpiryTime != null) && (!autoExpiryTime.matches("(\\d)+(MS|ms|S|s|M|m|H|h|D|d)?")))
/*     */     {
/* 153 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheInvalidAutoExpiryTime", null);
/* 154 */       throw new DataException(errMsg);
/*     */     }
/* 156 */     return getCacheRegion(cacheRegionName, getCacheInterfaceName(), false, true, autoExpiryTime);
/*     */   }
/*     */ 
/*     */   protected static String getCacheInterfaceName()
/*     */     throws ServiceException, DataException
/*     */   {
/* 169 */     return createInterfaceForCaches("IdcCacheRegion");
/*     */   }
/*     */ 
/*     */   protected static String getCacheManagerName() throws ServiceException, DataException
/*     */   {
/* 174 */     return createInterfaceForCaches("IdcCacheManager");
/*     */   }
/*     */ 
/*     */   public static String getCacheType()
/*     */     throws ServiceException, DataException
/*     */   {
/* 185 */     return createInterfaceForCaches("IdcCacheType");
/*     */   }
/*     */ 
/*     */   protected static String createInterfaceForCaches(String interfaceName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 197 */     if (m_cacheInterfaceNames == null)
/*     */     {
/* 199 */       loadInterfaceNames();
/*     */     }
/*     */ 
/* 202 */     String classname = (String)m_cacheInterfaceNames.get(interfaceName);
/* 203 */     if (classname == null)
/*     */     {
/* 205 */       String msg = LocaleUtils.encodeMessage("csIdcCacheMissingInterface", null, interfaceName);
/* 206 */       throw new ServiceException(msg);
/*     */     }
/* 208 */     return classname;
/*     */   }
/*     */ 
/*     */   protected static void loadInterfaceNames()
/*     */     throws DataException
/*     */   {
/* 218 */     Map interfaceNames = new HashMap();
/* 219 */     DataResultSet cacheImplementors = SharedObjects.getTable("IdcCacheImplementors");
/* 220 */     if ((cacheImplementors == null) || (cacheImplementors.isEmpty()))
/*     */     {
/* 222 */       Report.trace("idccache", "Cannot find IdcCacheImplementors resource table", null);
/* 223 */       m_cacheInterfaceNames = null;
/* 224 */       return;
/*     */     }
/*     */ 
/* 227 */     String[] colNames = { "Interface", "Implementation" };
/* 228 */     FieldInfo[] fields = ResultSetUtils.createInfoList(cacheImplementors, colNames, true);
/* 229 */     int idcCacheIndex = fields[0].m_index;
/* 230 */     int classnameIndex = fields[1].m_index;
/*     */ 
/* 232 */     if (!cacheImplementors.isRowPresent())
/*     */     {
/* 234 */       Report.trace("idccache", "IdcCacheImplementors resource table is empty, or error loading the table", null);
/*     */     }
/* 236 */     for (cacheImplementors.first(); cacheImplementors.isRowPresent(); cacheImplementors.next())
/*     */     {
/* 238 */       String appInterface = cacheImplementors.getStringValue(idcCacheIndex);
/* 239 */       String classname = cacheImplementors.getStringValue(classnameIndex);
/* 240 */       interfaceNames.put(appInterface, classname);
/* 241 */       if (!Report.m_verbose)
/*     */         continue;
/* 243 */       Report.trace("idccache", classname + " for the interface " + appInterface, null);
/*     */     }
/*     */ 
/* 246 */     m_cacheInterfaceNames = interfaceNames;
/*     */   }
/*     */ 
/*     */   protected static IdcCacheRegion getCacheRegion(String cacheRegionName, String classname, boolean isPersistent, boolean isAutoExpiryEnabled, String setAutoExpiryTime)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 260 */       ClassHelper cHelper = new ClassHelper();
/* 261 */       Object[] paramObjs = { cacheRegionName, Boolean.valueOf(isPersistent), Boolean.valueOf(isAutoExpiryEnabled), setAutoExpiryTime };
/* 262 */       cHelper.init(classname, paramObjs);
/* 263 */       m_idcCacheRegion = (IdcCacheRegion)cHelper.getClassInstance();
/* 264 */       return m_idcCacheRegion.getCacheRegion(cacheRegionName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 268 */       String msg = LocaleUtils.encodeMessage("csIdcCacheGetRegionFailed", null, null);
/* 269 */       throw new ServiceException(e, msg, new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataResultSet getCachesList()
/*     */     throws ServiceException, DataException
/*     */   {
/* 281 */     String classname = getCacheManagerName();
/* 282 */     Class cl = ClassHelperUtils.createClass(classname);
/* 283 */     DataResultSet drset = null;
/*     */     try
/*     */     {
/* 286 */       drset = (DataResultSet)ClassHelperUtils.executeStaticMethod(cl, "getCachesList", null, null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 290 */       throw new ServiceException(e);
/*     */     }
/* 292 */     return drset;
/*     */   }
/*     */ 
/*     */   public static long parseTime(String time)
/*     */     throws DataException
/*     */   {
/* 303 */     long multiplier = 1L;
/* 304 */     int length = time.trim().length();
/* 305 */     long value = 0L;
/* 306 */     int substrLen = length - 1;
/* 307 */     char c1 = time.charAt(length - 1); char c2 = time.charAt(length - 2);
/* 308 */     if ((((c2 == 'M') || (c2 == 'm'))) && (((c1 == 'S') || (c1 == 's'))))
/*     */     {
/* 310 */       substrLen = length - 2;
/*     */     }
/* 313 */     else if ((c1 == 'S') || (c1 == 's'))
/*     */     {
/* 315 */       multiplier = 1000L;
/*     */     }
/* 318 */     else if ((c1 == 'M') || (c1 == 'm'))
/*     */     {
/* 320 */       multiplier = 60000L;
/*     */     }
/* 323 */     else if ((c1 == 'H') || (c1 == 'h'))
/*     */     {
/* 325 */       multiplier = 3600000L;
/*     */     }
/* 328 */     else if ((c1 == 'D') || (c1 == 'd'))
/*     */     {
/* 330 */       multiplier = 86400000L;
/*     */     }
/*     */     else
/*     */     {
/* 335 */       multiplier = 1000L;
/* 336 */       substrLen = length;
/*     */     }
/*     */     try
/*     */     {
/* 340 */       value = Integer.valueOf(time.substring(0, substrLen)).intValue() * multiplier;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 345 */       String errMsg = LocaleUtils.encodeMessage("csIdcCacheInvalidAutoExpiryTime", null);
/* 346 */       throw new DataException(errMsg);
/*     */     }
/*     */ 
/* 349 */     return value;
/*     */   }
/*     */ 
/*     */   public static boolean isPersistentService(String serviceName)
/*     */   {
/* 362 */     return serviceName.startsWith("ucm-persistent");
/*     */   }
/*     */ 
/*     */   public static boolean isAutoExpiryService(String serviceName)
/*     */   {
/* 374 */     String serviceNamePrefix = "ucm-nonpersistent";
/* 375 */     if (isPersistentService(serviceName))
/*     */     {
/* 377 */       serviceNamePrefix = "ucm-persistent";
/*     */     }
/*     */ 
/* 381 */     return serviceName.startsWith(serviceNamePrefix + "-" + "autoexpiry");
/*     */   }
/*     */ 
/*     */   public static String getAutoExpiryTimeFromService(String serviceName)
/*     */   {
/* 393 */     String autoExpiryTime = null;
/* 394 */     boolean isAutoExpiry = isAutoExpiryService(serviceName);
/* 395 */     if (!isAutoExpiry)
/*     */     {
/* 397 */       return autoExpiryTime;
/*     */     }
/* 399 */     boolean isPersistent = isPersistentService(serviceName);
/* 400 */     String servicePrefix = null;
/* 401 */     int prefixLength = 0;
/* 402 */     if (isPersistent)
/*     */     {
/* 404 */       servicePrefix = "ucm-persistent-autoexpiry";
/*     */     }
/*     */     else
/*     */     {
/* 408 */       servicePrefix = "ucm-nonpersistent-autoexpiry";
/*     */     }
/* 410 */     prefixLength = servicePrefix.length();
/* 411 */     autoExpiryTime = serviceName.substring(prefixLength, serviceName.length());
/* 412 */     return autoExpiryTime;
/*     */   }
/*     */ 
/*     */   public static String getServiceName(boolean isPersistent, boolean isAutoExpiry, String autoExpiryTime)
/*     */   {
/* 424 */     String serviceName = "ucm-persistent";
/* 425 */     if (!isPersistent)
/*     */     {
/* 427 */       serviceName = "ucm-nonpersistent";
/*     */     }
/* 429 */     if (isAutoExpiry)
/*     */     {
/* 431 */       serviceName = serviceName + "-autoexpiry";
/* 432 */       if ((!autoExpiryTime.endsWith("ms")) && (!autoExpiryTime.endsWith("MS")))
/*     */       {
/* 434 */         long parsedAutoExpiryTime = 0L;
/*     */         try
/*     */         {
/* 439 */           parsedAutoExpiryTime = parseTime(autoExpiryTime);
/* 440 */           autoExpiryTime = Long.toString(parsedAutoExpiryTime) + "MS";
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 444 */           Report.trace("idccache", e.getMessage() + ", time string = " + autoExpiryTime + ". Setting autoexpiry time to 1 hour.", null);
/* 445 */           parsedAutoExpiryTime = 3600000L;
/* 446 */           autoExpiryTime = Long.toString(parsedAutoExpiryTime) + "MS";
/*     */         }
/*     */       }
/* 449 */       serviceName = serviceName + autoExpiryTime;
/*     */     }
/* 451 */     return serviceName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 457 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheFactory
 * JD-Core Version:    0.5.4
 */