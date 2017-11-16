/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.loader.IdcLoader;
/*     */ import java.io.PrintStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class EnvUtils
/*     */ {
/*  31 */   protected static boolean m_isEnvFlagsInitialized = false;
/*  32 */   public static boolean m_useNativeOSUtils = true;
/*  33 */   public static boolean m_allowFailedHardLinkTransactions = true;
/*     */ 
/*  35 */   protected static String m_javaVersion = "1.1";
/*  36 */   protected static boolean m_isRunAsService = false;
/*     */ 
/*  38 */   protected static boolean m_isMsApplet = false;
/*  39 */   protected static boolean m_isMicrosoftVM = true;
/*  40 */   protected static boolean m_isLinux11 = false;
/*     */ 
/*  43 */   protected static boolean m_isHostedInAppServer = false;
/*     */ 
/*  45 */   protected static boolean m_isAppServerInProductionMode = false;
/*     */ 
/*  49 */   protected static String m_servletApplicationType = "server";
/*     */   protected static String m_appServerType;
/*     */   protected static String m_productName;
/*     */   protected static IdcLoader m_classLoader;
/*     */   public static final String IS_LIBRARY_SERVER = "IsLibraryServer";
/*     */   public static final String IS_USER_SERVER = "IsUserServer";
/*     */   public static final String IS_CATALOG_SERVER = "IsCatalogServer";
/*     */   public static final String WEBLOGIC = "weblogic";
/*     */   public static final String WEBSPHERE = "websphere";
/*     */   public static final String JBOSS = "jboss";
/*  81 */   public static final String[][] OSNAMES = { { "aix", "LIBPATH" }, { "aix64", "LIBPATH" }, { "freebsd", "LD_LIBRARY_PATH" }, { "freebsd64", "LD_LIBRARY_PATH" }, { "hpux", "SHLIB_PATH" }, { "hpux64", "SHLIB_PATH" }, { "hpux-ia", "LD_LIBRARY_PATH" }, { "hpux-ia64", "LD_LIBRARY_PATH" }, { "linux", "LD_LIBRARY_PATH" }, { "linux64", "LD_LIBRARY_PATH" }, { "linux-s390", "LD_LIBRARY_PATH" }, { "linux-s390x", "LD_LIBRARY_PATH" }, { "solaris", "LD_LIBRARY_PATH" }, { "solaris64", "LD_LIBRARY_PATH" }, { "solaris-x86", "LD_LIBRARY_PATH" }, { "solaris-amd64", "LD_LIBRARY_PATH" }, { "osx", "DYLD_LIBRARY_PATH" }, { "win32", "PATH" }, { "windows-amd64", "PATH" } };
/*     */ 
/* 104 */   public static OSSettingsHelper m_osHelper = null;
/*     */ 
/* 106 */   protected static String m_osLibPathEnvName = "LD_LIBRARY_PATH";
/*     */ 
/* 110 */   public static final String[][] OSFAMILIES = { { "unix", ":", "" }, { "windows", ";", ".exe" } };
/*     */ 
/* 116 */   protected static String m_pathSep = ":";
/* 117 */   protected static String m_exeSuffix = "";
/*     */ 
/*     */   public static boolean isDebugPlatform()
/*     */   {
/* 121 */     boolean isDebugPlatform = false;
/*     */     try
/*     */     {
/* 124 */       String isDebugTraceString = System.getProperty("DebugPlatformComputation");
/* 125 */       isDebugPlatform = StringUtils.convertToBool(isDebugTraceString, isDebugPlatform);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 133 */       String isDebugTraceString = System.getenv("DEBUG_PLATFORM_COMPUTATION");
/* 134 */       isDebugPlatform = StringUtils.convertToBool(isDebugTraceString, isDebugPlatform);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */     }
/*     */ 
/* 140 */     return isDebugPlatform;
/*     */   }
/*     */ 
/*     */   public static void initializeEnvFlags()
/*     */   {
/* 145 */     if (m_isEnvFlagsInitialized)
/*     */     {
/* 147 */       return;
/*     */     }
/*     */ 
/* 150 */     synchronized ("weblogic")
/*     */     {
/* 152 */       if (m_isEnvFlagsInitialized)
/*     */       {
/* 154 */         return;
/*     */       }
/* 156 */       String useNativeOSUtilsString = SystemUtils.getAppProperties().getProperty("IdcUseNativeOSUtils");
/* 157 */       if (useNativeOSUtilsString != null)
/*     */       {
/* 159 */         m_useNativeOSUtils = StringUtils.convertToBool(useNativeOSUtilsString, m_useNativeOSUtils);
/*     */       }
/* 161 */       boolean isDebugPlatform = isDebugPlatform();
/*     */ 
/* 163 */       m_javaVersion = System.getProperty("java.version");
/*     */       try
/*     */       {
/* 170 */         m_isMicrosoftVM = System.getProperty("java.vendor").indexOf("icrosoft") >= 0;
/* 171 */         m_isMsApplet = false;
/* 172 */         if (m_isMicrosoftVM)
/*     */         {
/* 174 */           SecurityManager man = System.getSecurityManager();
/* 175 */           if (man != null)
/*     */           {
/* 177 */             String name = man.getClass().getName();
/* 178 */             if (name.indexOf("com.ms.") >= 0)
/*     */             {
/* 180 */               m_isMsApplet = true;
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 188 */         m_isMsApplet = true;
/* 189 */         m_isMicrosoftVM = true;
/*     */       }
/*     */ 
/* 192 */       boolean okay = false;
/* 193 */       String osName = getOSName();
/* 194 */       for (int i = 0; i < OSNAMES.length; ++i)
/*     */       {
/* 196 */         if (!OSNAMES[i][0].equals(osName))
/*     */           continue;
/* 198 */         okay = true;
/* 199 */         m_osLibPathEnvName = OSNAMES[i][1];
/* 200 */         break;
/*     */       }
/*     */ 
/* 203 */       if (!okay)
/*     */       {
/* 205 */         Report.trace(null, "The OS name '" + osName + "' is not known by the Content Server.", null);
/*     */       }
/*     */ 
/* 209 */       okay = false;
/* 210 */       String osFamily = getOSFamily();
/* 211 */       for (int i = 0; i < OSFAMILIES.length; ++i)
/*     */       {
/* 213 */         if (!OSFAMILIES[i][0].equals(osFamily))
/*     */           continue;
/* 215 */         okay = true;
/* 216 */         m_pathSep = OSFAMILIES[i][1];
/* 217 */         m_exeSuffix = OSFAMILIES[i][2];
/* 218 */         break;
/*     */       }
/*     */ 
/* 221 */       if (!okay)
/*     */       {
/* 223 */         Report.trace(null, "The OS family '" + osFamily + "' is not known by the Content Server.", null);
/*     */       }
/*     */ 
/* 226 */       if (isDebugPlatform)
/*     */       {
/* 228 */         String msg = "EnvUtils: executable suffix=" + m_exeSuffix + ", path separator=" + m_pathSep;
/* 229 */         System.err.println(msg);
/*     */       }
/*     */ 
/* 232 */       m_isEnvFlagsInitialized = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String determinePlatform()
/*     */   {
/* 242 */     SystemUtils.reportDeprecatedUsage("EnvUtils.determinePlatform() is deprecated in favor of getOSName()");
/*     */ 
/* 244 */     return OsUtils.getOSName();
/*     */   }
/*     */ 
/*     */   public static boolean isMsApplet()
/*     */   {
/* 249 */     initializeEnvFlags();
/* 250 */     return m_isMsApplet;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isWindows()
/*     */   {
/* 259 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isWindows() is deprecated in favor of getOSFamily()");
/*     */ 
/* 261 */     return OsUtils.getOSFamily().equals("windows");
/*     */   }
/*     */ 
/*     */   public static boolean isMicrosoftVM()
/*     */   {
/* 266 */     initializeEnvFlags();
/* 267 */     return m_isMicrosoftVM;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isUnix()
/*     */   {
/* 276 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isUnix() is deprecated in favor of getOSFamily()");
/*     */ 
/* 278 */     return OsUtils.getOSFamily().equals("unix");
/*     */   }
/*     */ 
/*     */   public static boolean isLinux11()
/*     */   {
/* 283 */     initializeEnvFlags();
/* 284 */     return m_isLinux11;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isLinux()
/*     */   {
/* 293 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isLinux() is deprecated in favor of getOSName()");
/*     */ 
/* 295 */     return OsUtils.getOSName().equals("linux");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isFreeBSD()
/*     */   {
/* 304 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isFreeBSD() is deprecated in favor of getOSName()");
/*     */ 
/* 306 */     return OsUtils.getOSName().equals("freebsd");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isSolaris()
/*     */   {
/* 315 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isSolaris() is deprecated in favor of getOSName()");
/*     */ 
/* 317 */     return OsUtils.getOSName().equals("solaris");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isAIX()
/*     */   {
/* 326 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isAIX() is deprecated in favor of getOSName()");
/*     */ 
/* 328 */     return OsUtils.getOSName().equals("aix");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isHPUX()
/*     */   {
/* 337 */     SystemUtils.reportDeprecatedUsage("EnvUtils.isHPUX() is deprecated in favor of getOSName()");
/*     */ 
/* 339 */     return OsUtils.getOSName().equals("hpux");
/*     */   }
/*     */ 
/*     */   public static String getJavaVersion()
/*     */   {
/* 344 */     initializeEnvFlags();
/* 345 */     return m_javaVersion;
/*     */   }
/*     */ 
/*     */   public static boolean supportsGuiFocus()
/*     */   {
/* 350 */     initializeEnvFlags();
/*     */ 
/* 356 */     return (m_javaVersion.startsWith("1.1")) || (m_javaVersion.startsWith("1.2")) || (m_javaVersion.equals("1.3.0"));
/*     */   }
/*     */ 
/*     */   public static String getOSName()
/*     */   {
/* 364 */     return OsUtils.getOSName();
/*     */   }
/*     */ 
/*     */   public static String getOSFamily()
/*     */   {
/* 369 */     return OsUtils.getOSFamily();
/*     */   }
/*     */ 
/*     */   public static boolean isFamily(String family)
/*     */   {
/* 374 */     return OsUtils.isFamily(family);
/*     */   }
/*     */ 
/*     */   public static boolean isOS(String os)
/*     */   {
/* 379 */     return OsUtils.isOS(os);
/*     */   }
/*     */ 
/*     */   public static String getLibraryPathEnvironmentVariableName()
/*     */   {
/* 384 */     initializeEnvFlags();
/* 385 */     return m_osLibPathEnvName;
/*     */   }
/*     */ 
/*     */   public static String getPathSeparator()
/*     */   {
/* 390 */     initializeEnvFlags();
/* 391 */     return m_pathSep;
/*     */   }
/*     */ 
/*     */   public static String getExecutableFileSuffix()
/*     */   {
/* 396 */     initializeEnvFlags();
/* 397 */     return m_exeSuffix;
/*     */   }
/*     */ 
/*     */   public static String convertPathToOSConventions(String path)
/*     */   {
/* 402 */     if (OsUtils.isFamily("unix"))
/*     */     {
/* 404 */       return path;
/*     */     }
/* 406 */     return FileUtils.windowsSlashes(path);
/*     */   }
/*     */ 
/*     */   public static String normalizeOSPath(String path, String type)
/*     */     throws ServiceException
/*     */   {
/* 425 */     Map options = new HashMap();
/* 426 */     options.put(type, type);
/* 427 */     Map result = normalizeOSPath(path, options);
/* 428 */     path = (String)result.get("path");
/* 429 */     return path;
/*     */   }
/*     */ 
/*     */   public static Map normalizeOSPath(String abstractPath, Map options)
/*     */     throws ServiceException
/*     */   {
/* 449 */     if (m_osHelper == null)
/*     */     {
/* 451 */       String msg = LocaleUtils.encodeMessage("syOSMapperNotInitialized", null);
/*     */ 
/* 453 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 456 */     Map map = m_osHelper.normalizeOSPath(abstractPath, options);
/* 457 */     return map;
/*     */   }
/*     */ 
/*     */   public static void setRunAsService(boolean isRunAsService)
/*     */   {
/* 462 */     m_isRunAsService = isRunAsService;
/*     */   }
/*     */ 
/*     */   public static boolean isRunAsService()
/*     */   {
/* 467 */     return m_isRunAsService;
/*     */   }
/*     */ 
/*     */   public static String getLibraryPath()
/*     */   {
/* 472 */     return System.getProperty("java.library.path");
/*     */   }
/*     */ 
/*     */   public static boolean isHostedInAppServer()
/*     */   {
/* 477 */     return m_isHostedInAppServer;
/*     */   }
/*     */ 
/*     */   public static void setHostedInAppServer(boolean isHostedInAppServer)
/*     */   {
/* 482 */     m_isHostedInAppServer = isHostedInAppServer;
/*     */   }
/*     */ 
/*     */   public static void setAppServerType(String appServerType)
/*     */   {
/* 488 */     if (appServerType == null)
/*     */     {
/* 490 */       appServerType = "weblogic";
/*     */     }
/* 492 */     m_appServerType = appServerType;
/*     */   }
/*     */ 
/*     */   public static String getAppServerType()
/*     */   {
/* 497 */     return m_appServerType;
/*     */   }
/*     */ 
/*     */   public static boolean isAppServerInProductionMode()
/*     */   {
/* 502 */     return m_isAppServerInProductionMode;
/*     */   }
/*     */ 
/*     */   public static boolean isAppServerType(String appServerType)
/*     */   {
/* 507 */     if (m_appServerType != null)
/*     */     {
/* 509 */       return m_appServerType.equalsIgnoreCase(appServerType);
/*     */     }
/* 511 */     return false;
/*     */   }
/*     */ 
/*     */   public static void setProductName(String productName)
/*     */   {
/* 517 */     if (productName == null)
/*     */     {
/* 519 */       productName = "idccs";
/*     */     }
/* 521 */     m_productName = productName;
/*     */   }
/*     */ 
/*     */   public static String getProductName()
/*     */   {
/* 526 */     return m_productName;
/*     */   }
/*     */ 
/*     */   public static void setIsAppServerInProductionMode(boolean isProductionMode)
/*     */   {
/* 531 */     m_isAppServerInProductionMode = isProductionMode;
/*     */   }
/*     */ 
/*     */   public static String getServletApplicationType()
/*     */   {
/* 536 */     return m_servletApplicationType;
/*     */   }
/*     */ 
/*     */   public static void setServletApplicationType(String servletApplicationType)
/*     */   {
/* 541 */     m_servletApplicationType = servletApplicationType;
/*     */   }
/*     */ 
/*     */   public static IdcLoader getIdcLoader()
/*     */   {
/* 546 */     return m_classLoader;
/*     */   }
/*     */ 
/*     */   public static void setIdcLoader(IdcLoader loader)
/*     */   {
/* 551 */     m_classLoader = loader;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 556 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97679 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EnvUtils
 * JD-Core Version:    0.5.4
 */