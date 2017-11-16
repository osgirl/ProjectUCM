/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.apputilities.installer.MigrateUtils;
/*      */ import intradoc.common.Browser;
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.DateUtils;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.GuiUtils;
/*      */ import intradoc.common.Help;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Log;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerialize;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.config.ConfigFileLoader;
/*      */ import intradoc.loader.IdcClassLoader;
/*      */ import intradoc.loader.IdcLoader;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.serialize.DataBinderSerializer;
/*      */ import intradoc.server.subject.LocalesSubjectCallback;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.LocaleLoader;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessageUtils;
/*      */ import java.security.AccessController;
/*      */ import java.security.PrivilegedAction;
/*      */ import java.text.DateFormat;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcSystemConfig
/*      */ {
/*   91 */   public static final String[] CLUSTER_ADDRESS_SETTINGS = { "IdcServerBindAddress", "IdcAdminServerBindAddress", "IntradocServerHostName", "IdcAdminServerHostName", "SocketServerAddress", "SocketHostAddressSecurityFilter" };
/*      */ 
/*   99 */   public static boolean m_isInitialConfigLoaded = false;
/*      */ 
/*  101 */   public static boolean m_loadSystemStringsOnly = false;
/*      */ 
/*  103 */   public static int F_REINIT = 1024;
/*      */ 
/*  106 */   public static int F_STANDARD_SERVER = 0;
/*  107 */   public static int F_ADMIN_SERVER = 1;
/*  108 */   public static int F_UTILITY_APP = 2;
/*      */   public static final long F_CONFIG_INITED = 1L;
/*      */   public static final long F_APP_CONFIG_INITED = 16L;
/*      */   public static final long F_LOCALIZATION_INTED = 256L;
/*      */   public static final long F_LOCALIZATION_CONFIGURED = 512L;
/*      */   public static long m_initializationStatus;
/*  123 */   public static boolean m_isTestStartup = false;
/*      */ 
/*      */   @Deprecated
/*      */   public static void initConfigEarly()
/*      */     throws DataException
/*      */   {
/*  131 */     initConfigEarly(F_STANDARD_SERVER);
/*      */   }
/*      */ 
/*      */   public static void initConfigFileWorkspace(Workspace ws)
/*      */     throws ServiceException
/*      */   {
/*  139 */     FileUtilsCfgBuilder.setWorkspace(ws);
/*      */   }
/*      */ 
/*      */   public static void initConfigEarly(int flags) throws DataException
/*      */   {
/*  144 */     boolean isStandardApp = (flags & (F_ADMIN_SERVER | F_UTILITY_APP)) == 0;
/*  145 */     boolean isReinit = (flags & F_REINIT) != 0;
/*      */ 
/*  147 */     IdcInstallInfo installInfo = null;
/*      */ 
/*  149 */     if (!isReinit)
/*      */     {
/*  151 */       SharedObjects.init();
/*      */ 
/*  155 */       IdcMessageUtils.init();
/*      */ 
/*  157 */       ClassLoader loader = EnvUtils.class.getClassLoader();
/*  158 */       while (loader != null)
/*      */       {
/*  160 */         if (loader instanceof IdcLoader)
/*      */         {
/*  162 */           EnvUtils.setIdcLoader((IdcLoader)loader);
/*  163 */           break;
/*      */         }
/*  165 */         loader = loader.getParent();
/*      */       }
/*  167 */       SharedObjects.putEnvironmentValue("ClassLoader", (null == loader) ? "" : loader.getClass().getCanonicalName());
/*      */ 
/*  170 */       SharedObjects.putObject("SystemUtils", "isServerStopped", SystemUtils.m_isServerStoppedPtr);
/*      */ 
/*  172 */       installInfo = new IdcInstallInfo();
/*  173 */       if (!isStandardApp)
/*      */       {
/*  176 */         installInfo.m_isAutoInstallDisabled = true;
/*      */       }
/*  178 */       SharedObjects.putObject("InstallInfo", "IdcInstallInfo", installInfo);
/*  179 */       installInfo.checkAndStartInstall();
/*      */ 
/*  181 */       DataLoader.cacheSystemProperties();
/*  182 */       loadNativeApiConf();
/*      */     }
/*      */ 
/*  190 */     if (isReinit)
/*      */     {
/*  192 */       LegacyDirectoryLocator.resetRootDirectories();
/*      */     }
/*      */     try
/*      */     {
/*  196 */       LegacyDirectoryLocator.buildRootDirectories();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  200 */       throw new DataException(null, e);
/*      */     }
/*      */ 
/*  203 */     if (installInfo != null)
/*      */     {
/*  205 */       installInfo.continueEarlyInstall();
/*      */     }
/*      */ 
/*  209 */     if (EnvUtils.getAppServerType() == "weblogic")
/*      */     {
/*      */       try
/*      */       {
/*  213 */         Object kernelId = AccessController.doPrivileged((PrivilegedAction)ClassHelperUtils.executeStaticMethod(Class.forName("weblogic.security.service.PrivilegedActions"), "getKernelIdentityAction", null, null));
/*      */ 
/*  219 */         Class cl_ms = Class.forName("weblogic.management.provider.ManagementService");
/*  220 */         Class cl_as = Class.forName("weblogic.security.acl.internal.AuthenticatedSubject");
/*  221 */         Object[] args = { kernelId };
/*  222 */         Class[] argTypes = { cl_as };
/*  223 */         Object runtimeAccess = ClassHelperUtils.executeStaticMethod(cl_ms, "getRuntimeAccess", args, argTypes);
/*      */ 
/*  225 */         Map invokeInfo = new HashMap();
/*  226 */         invokeInfo.put("object", runtimeAccess);
/*  227 */         invokeInfo.put("suppressAccessControlCheck", Boolean.TRUE);
/*  228 */         invokeInfo.put("method", "getDomain");
/*  229 */         Object domainMbean = ClassHelperUtils.executeMethodEx(invokeInfo);
/*      */ 
/*  236 */         if (((Boolean)ClassHelperUtils.executeMethod(domainMbean, "isExalogicOptimizationsEnabled", null, null)).booleanValue())
/*      */         {
/*  238 */           SharedObjects.putEnvironmentValue("IsExalogicOptimizationsEnabled", "true");
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  259 */       loadConfigFileToSharedObjects();
/*  260 */       IdcSystemRunTimeConfig.initConfigFileParameters();
/*      */ 
/*  262 */       initFileStoreObjects();
/*      */ 
/*  264 */       if (installInfo != null)
/*      */       {
/*  266 */         installInfo.finishEarlyInstall();
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  271 */       throw new DataException(null, e);
/*      */     }
/*      */ 
/*  280 */     if ((!EnvUtils.m_useNativeOSUtils) || (System.getenv("BIN_DIR") == null))
/*      */       return;
/*      */     try
/*      */     {
/*  284 */       NativeOsUtils utils = new NativeOsUtils();
/*  285 */       utils.setEnv("BIN_DIR", "");
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  289 */       Report.trace(null, "unable to instantiate NativeOsUtils", t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void loadNativeApiConf()
/*      */   {
/*  296 */     intradoc.common.SafeFileOutputStream.m_disableNativeApi = SharedObjects.getEnvValueAsBoolean("DisableNativeApiForSafeFileOutputStream", true);
/*      */   }
/*      */ 
/*      */   public static void initFileStoreObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*  302 */     ConfigFileLoader.init(null);
/*      */   }
/*      */ 
/*      */   public static void loadConfigFileToSharedObjects()
/*      */     throws DataException
/*      */   {
/*  311 */     String configDir = LegacyDirectoryLocator.getConfigDirectory();
/*  312 */     String cfgFile = new StringBuilder().append(configDir).append("config.cfg").toString();
/*  313 */     DataLoader.cachePropertiesFromFileWithoutOverwrite(cfgFile, null);
/*      */   }
/*      */ 
/*      */   public static void loadInitialConfig() throws DataException
/*      */   {
/*  318 */     if ((m_initializationStatus & 1L) > 0L)
/*      */     {
/*  320 */       return;
/*      */     }
/*      */ 
/*  323 */     initConfigEarly(F_STANDARD_SERVER);
/*      */ 
/*  328 */     ClassLoader currentClassLoader = IdcSystemConfig.class.getClassLoader();
/*  329 */     if (currentClassLoader instanceof IdcClassLoader)
/*      */     {
/*  331 */       IdcClassLoader idcLoader = (IdcClassLoader)currentClassLoader;
/*  332 */       String classesDir = FileUtils.getAbsolutePath(LegacyDirectoryLocator.getIntradocDir(), "classes/");
/*      */       try
/*      */       {
/*  336 */         if (FileUtils.checkFile(classesDir, false, false) == 0)
/*      */         {
/*  338 */           int defaultClassDirLoadOrder = SharedObjects.getEnvironmentInt("IdcClassesDirLoadOrder", 1000);
/*  339 */           idcLoader.addClassPathElement(classesDir, defaultClassDirLoadOrder);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  346 */         throw new DataException("!$Cannot append classpath element", e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  351 */     GuiUtils.setGifDirectoryRoot(LegacyDirectoryLocator.getImagesDirectory());
/*  352 */     m_initializationStatus |= 1L;
/*      */   }
/*      */ 
/*      */   public static void loadAppConfigInfo() throws DataException, ServiceException
/*      */   {
/*  357 */     loadAppConfigInfo(null);
/*      */   }
/*      */ 
/*      */   public static void loadAppConfigInfo(Map args) throws DataException, ServiceException
/*      */   {
/*  362 */     if ((m_initializationStatus & 0x10) > 0L)
/*      */     {
/*  364 */       return;
/*      */     }
/*  366 */     List configFiles = new ArrayList();
/*  367 */     String localConfigFileList = SharedObjects.getEnvironmentValue("LocalConfigFileList");
/*  368 */     if (localConfigFileList != null)
/*      */     {
/*  370 */       StringUtils.appendListFromSequenceSimple(configFiles, localConfigFileList);
/*      */     }
/*  372 */     String configFileList = SharedObjects.getEnvironmentValue("ConfigFileList");
/*  373 */     if (configFileList != null)
/*      */     {
/*  375 */       StringUtils.appendListFromSequenceSimple(configFiles, configFileList);
/*      */     }
/*      */     else
/*      */     {
/*  379 */       configFiles.add("$ConfigDir/config.cfg");
/*  380 */       configFiles.add("$ConfigDir/state.cfg");
/*  381 */       configFiles.add("$IdcResourcesDir/core/config/defaultconfig.cfg");
/*      */     }
/*      */ 
/*  384 */     Map loadedFiles = new HashMap();
/*  385 */     for (int i = 0; i < configFiles.size(); ++i)
/*      */     {
/*  387 */       if (SharedObjects.getEnvValueAsBoolean("TraceIsVerbose", false))
/*      */       {
/*  391 */         Report.m_verbose = SystemUtils.m_verbose = 1;
/*  392 */         Report.trace(null, "TraceIsVerbose enabled", null);
/*      */       }
/*  394 */       String filePath = (String)configFiles.get(i);
/*      */ 
/*  396 */       if (SystemUtils.m_verbose)
/*      */       {
/*  398 */         Report.debug(null, new StringBuilder().append("considering config file ").append(filePath).toString(), null);
/*      */       }
/*      */ 
/*  401 */       String computedPath = PathUtils.substitutePathVariables(filePath, SharedObjects.getSecureEnvironment(), null, PathUtils.F_VARS_MUST_EXIST, null);
/*      */ 
/*  405 */       computedPath = FileUtils.fileSlashes(computedPath);
/*  406 */       loadedFiles.put(computedPath, computedPath);
/*      */ 
/*  408 */       if (SystemUtils.m_verbose)
/*      */       {
/*  410 */         Report.debug(null, new StringBuilder().append("loading config file ").append(computedPath).toString(), null);
/*      */       }
/*      */ 
/*  413 */       boolean loadAlternate = false;
/*      */       try
/*      */       {
/*  416 */         Properties newProps = DataLoader.cachePropertiesFromFileWithoutOverwrite(computedPath, args);
/*      */ 
/*  419 */         String tmp = newProps.getProperty("ConfigFileList");
/*  420 */         if ((tmp != null) && (!tmp.equals(configFileList)))
/*      */         {
/*  422 */           StringUtils.appendListFromSequenceSimple(configFiles, tmp);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  427 */         loadAlternate = true;
/*  428 */         if (SystemUtils.m_verbose)
/*      */         {
/*  430 */           Report.debug(null, "Unable to load configuration file.", e);
/*      */         }
/*      */       }
/*      */ 
/*  434 */       if (!loadAlternate)
/*      */         continue;
/*  436 */       String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/*      */ 
/*  438 */       String newPath = computedPath;
/*  439 */       if (newPath.startsWith(intradocDir))
/*      */       {
/*  442 */         newPath = new StringBuilder().append("$IdcHomeDir/").append(newPath.substring(intradocDir.length())).toString();
/*      */       }
/*      */ 
/*  445 */       String prodName = SharedObjects.getEnvironmentValue("IdcProductName");
/*  446 */       int dotIndex = newPath.indexOf(46);
/*  447 */       String ext = "";
/*  448 */       if (dotIndex > 0)
/*      */       {
/*  450 */         ext = newPath.substring(dotIndex);
/*  451 */         newPath = newPath.substring(0, dotIndex);
/*      */       }
/*      */ 
/*  455 */       if (newPath.endsWith(new StringBuilder().append("-").append(prodName).toString()))
/*      */         continue;
/*  457 */       newPath = new StringBuilder().append(newPath).append("-").append(prodName).append(ext).toString();
/*  458 */       StringUtils.appendListFromSequenceSimple(configFiles, newPath);
/*      */ 
/*  460 */       if (!SystemUtils.m_verbose)
/*      */         continue;
/*  462 */       Report.debug(null, new StringBuilder().append("Adding alternate configuration file ").append(newPath).toString(), null);
/*      */     }
/*      */ 
/*  468 */     SharedLoader.loadInitialConfig();
/*      */ 
/*  471 */     String str = SharedObjects.getEnvironmentValue("IsJdbc");
/*  472 */     if (str == null)
/*      */     {
/*  474 */       str = SharedObjects.getEnvironmentValue("isJdbc");
/*  475 */       if (str == null)
/*      */       {
/*  478 */         str = "true";
/*      */       }
/*  480 */       SharedObjects.putEnvironmentValue("IsJdbc", str);
/*      */     }
/*      */ 
/*  485 */     String specialAuthGroups = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/*      */ 
/*  487 */     if (specialAuthGroups != null)
/*      */     {
/*  489 */       String[] groups = specialAuthGroups.split(",");
/*  490 */       StringBuilder sb = new StringBuilder();
/*  491 */       for (int i = 0; i < groups.length; ++i)
/*      */       {
/*  493 */         if (i != 0)
/*      */         {
/*  495 */           sb.append(",");
/*      */         }
/*  497 */         sb.append(groups[i].trim());
/*      */       }
/*      */ 
/*  500 */       SharedObjects.putEnvironmentValue("SpecialAuthGroups", sb.toString());
/*      */     }
/*      */ 
/*  504 */     SharedLoader.initIdcName(SharedObjects.getSafeEnvironment(), 1);
/*      */ 
/*  506 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  507 */     if (!StringUtils.urlEncode(idcName).equals(idcName))
/*      */     {
/*  509 */       Report.warning(null, null, "csIdcNameIllegal", new Object[] { idcName });
/*      */     }
/*      */ 
/*  513 */     LegacyDirectoryLocator.buildWebRoots();
/*      */ 
/*  515 */     applyClusterNodeAddressOverrides();
/*      */ 
/*  518 */     String wPath = SharedObjects.getEnvironmentValue("WebBrowserPath");
/*  519 */     if (wPath == null)
/*      */     {
/*  521 */       wPath = SharedObjects.getEnvironmentValue("webBrowserPath");
/*      */     }
/*  523 */     String baseUrlPath = DocumentPathBuilder.getBaseAbsoluteRoot();
/*  524 */     Browser.setPaths(wPath, baseUrlPath);
/*  525 */     Help.setPaths(LegacyDirectoryLocator.getHelpDirectory());
/*      */ 
/*  528 */     String logDir = LegacyDirectoryLocator.getLogDirectory();
/*      */     try
/*      */     {
/*  532 */       FileUtils.checkOrCreateDirectory(logDir, 6);
/*  533 */       Log.setLogDirectory(logDir);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  537 */       Report.trace(null, null, e);
/*      */     }
/*      */ 
/*  540 */     String intradocDir = DirectoryLocator.getIntradocDir();
/*  541 */     String adminIntradocCfg = FileUtils.getAbsolutePath(intradocDir, "admin/bin/intradoc.cfg");
/*  542 */     if ((SharedObjects.getEnvironmentValue("WebProxyAdminServer") == null) && (FileUtils.checkFile(adminIntradocCfg, 1) == 0))
/*      */     {
/*  545 */       SharedObjects.putEnvironmentValue("WebProxyAdminServer", "true");
/*      */     }
/*  547 */     m_initializationStatus |= 16L;
/*      */   }
/*      */ 
/*      */   public static void checkSetLoadSystemStringsOnly(boolean loadSystemStringsOnlyDefault)
/*      */   {
/*  552 */     String alreadySetValue = SharedObjects.getEnvironmentValue("LoadSystemStringsOnly");
/*  553 */     if ((alreadySetValue == null) && (loadSystemStringsOnlyDefault))
/*      */     {
/*  555 */       SharedObjects.putEnvironmentValue("LoadSystemStringsOnly", "1");
/*      */     }
/*  557 */     ResourceLoader.m_loadSystemStringsOnly = SharedObjects.getEnvValueAsBoolean("LoadSystemStringsOnly", loadSystemStringsOnlyDefault);
/*      */   }
/*      */ 
/*      */   public static boolean checkAppServerConfig()
/*      */   {
/*  564 */     boolean hasSocketPort = true;
/*  565 */     String existingHostedInAppServerVal = SharedObjects.getEnvironmentValue("IsHostedInAppServer");
/*  566 */     String hostedInAppServerVal = "";
/*  567 */     if (EnvUtils.isHostedInAppServer())
/*      */     {
/*  569 */       hostedInAppServerVal = "1";
/*  570 */       SharedObjects.putEnvironmentValue("IsHostedInAppServer", "1");
/*  571 */       String serverPort = SharedObjects.getEnvironmentValue("IntradocServerPort");
/*  572 */       if ((serverPort == null) || (serverPort.length() == 0))
/*      */       {
/*  574 */         hasSocketPort = false;
/*      */       }
/*      */     }
/*  577 */     if (existingHostedInAppServerVal == null)
/*      */     {
/*  579 */       SharedObjects.putEnvironmentValue("IsHostedInAppServer", hostedInAppServerVal);
/*      */     }
/*      */ 
/*  584 */     if ((checkForPostMigrateConfig()) || (checkForPostInstallConfig(hasSocketPort)))
/*      */     {
/*  586 */       SharedObjects.putEnvironmentValue("ForceSystemConfigPage", "1");
/*  587 */       if (SystemUtils.m_verbose)
/*      */       {
/*  589 */         String type = SharedObjects.getEnvironmentValue("SystemConfigPageType");
/*  590 */         String msg = new StringBuilder().append("forcing system config page, type = ").append((type == null) ? "unknown" : type).toString();
/*  591 */         Report.trace(null, msg, null);
/*      */       }
/*      */     }
/*      */ 
/*  595 */     return hasSocketPort;
/*      */   }
/*      */ 
/*      */   public static boolean checkForPostInstallConfig(boolean hasSocketPort)
/*      */   {
/*  606 */     boolean forcePostInstallConf = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("ForceInitialPostInstallConfiguration"), false);
/*      */ 
/*  609 */     if ((forcePostInstallConf) && (SystemUtils.m_verbose))
/*      */     {
/*  611 */       Report.trace(null, "ForceInstallPostInstallConfiguration=1", null);
/*      */     }
/*  613 */     boolean useSocketPortConfig = SharedObjects.getEnvValueAsBoolean("AllowSocketPortConfigDisablePostInstallPage", false);
/*  614 */     if ((!forcePostInstallConf) && (((!useSocketPortConfig) || (!hasSocketPort))))
/*      */     {
/*  616 */       String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  617 */       String path = new StringBuilder().append(intradocDir).append("install/installconf.hda").toString();
/*  618 */       forcePostInstallConf = FileUtils.checkFile(path, true, false) == -16;
/*  619 */       if (SystemUtils.m_verbose)
/*      */       {
/*  621 */         String msg = new StringBuilder().append(path).append((forcePostInstallConf) ? " not found" : " found").toString();
/*  622 */         Report.trace(null, msg, null);
/*      */       }
/*      */     }
/*  625 */     if (forcePostInstallConf)
/*      */     {
/*  627 */       SharedObjects.putEnvironmentValue("SystemConfigPageType", "install");
/*      */     }
/*  629 */     return forcePostInstallConf;
/*      */   }
/*      */ 
/*      */   public static boolean checkForPostMigrateConfig()
/*      */   {
/*  639 */     DataBinder migrateState = new DataBinder();
/*      */ 
/*  642 */     String[] migrateTypes = { "upgrade", "migrate" };
/*  643 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  644 */     for (int t = 0; t < migrateTypes.length; ++t)
/*      */     {
/*  646 */       String type = migrateTypes[t];
/*      */       try
/*      */       {
/*  651 */         if (MigrateUtils.loadMigrateState(intradocDir, type, migrateState))
/*      */         {
/*  653 */           SharedObjects.putObject("DataBinder", "MigrateState", migrateState);
/*  654 */           SharedObjects.putEnvironmentValue("SystemMigrateType", type);
/*  655 */           if (SystemUtils.m_verbose)
/*      */           {
/*  657 */             Report.trace(null, new StringBuilder().append("found ").append(type).append(" in ").append(intradocDir).toString(), null);
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (ServiceException se)
/*      */       {
/*  663 */         Report.trace(null, "unable to load migration state", se);
/*      */       }
/*  665 */       String path = new StringBuilder().append(intradocDir).append(type).append("/force_system_config_page.dat").toString();
/*  666 */       if (FileUtils.checkFile(path, true, false) != 0)
/*      */         continue;
/*  668 */       SharedObjects.putEnvironmentValue("SystemConfigPageType", type);
/*  669 */       if (SystemUtils.m_verbose)
/*      */       {
/*  671 */         Report.trace(null, new StringBuilder().append("found ").append(path).toString(), null);
/*      */       }
/*  673 */       return true;
/*      */     }
/*      */ 
/*  676 */     return false;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void initIdcName(Properties props, boolean issueWarnings)
/*      */   {
/*  683 */     SharedLoader.initIdcName(props, (issueWarnings) ? 1 : 0);
/*      */   }
/*      */ 
/*      */   public static void applyClusterNodeAddressOverrides()
/*      */   {
/*  690 */     String clusterNodeAddress = SharedObjects.getEnvironmentValue("ClusterNodeAddress");
/*      */ 
/*  692 */     if ((clusterNodeAddress == null) || (clusterNodeAddress.length() == 0))
/*      */     {
/*  695 */       return;
/*      */     }
/*      */ 
/*  698 */     String[] list = CLUSTER_ADDRESS_SETTINGS;
/*  699 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*  701 */       String setting = SharedObjects.getEnvironmentValue(list[i]);
/*      */ 
/*  703 */       if (setting != null)
/*      */         continue;
/*  705 */       SharedObjects.putEnvironmentValue(list[i], clusterNodeAddress);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void initLocalization()
/*      */     throws DataException, ServiceException
/*      */   {
/*  717 */     Report.deprecatedUsage("initLocalization without parameters has been replaced with one that has parameters");
/*  718 */     initLocalization(F_STANDARD_SERVER);
/*      */   }
/*      */ 
/*      */   public static void initLocalization(int flags) throws DataException, ServiceException
/*      */   {
/*  723 */     if ((m_initializationStatus & 0x100) > 0L)
/*      */     {
/*  725 */       return;
/*      */     }
/*      */ 
/*  730 */     boolean isAdmin = (flags & F_ADMIN_SERVER) != 0;
/*  731 */     boolean isUtilityApp = (flags & F_UTILITY_APP) != 0;
/*      */ 
/*  734 */     checkSetLoadSystemStringsOnly((isAdmin) || (isUtilityApp));
/*      */ 
/*  739 */     DataSerialize serializeDataBinder = new DataBinderSerializer();
/*  740 */     if ((!isAdmin) && (!isUtilityApp))
/*      */     {
/*  742 */       Object o1 = SharedObjects.getSafeEnvironment();
/*  743 */       Object o2 = SharedObjects.getSecureEnvironment();
/*  744 */       Object[] o = { o1, o2 };
/*  745 */       serializeDataBinder.setInvalidEnvObjects(o);
/*      */     }
/*  747 */     DataSerializeUtils.setDataSerialize(serializeDataBinder);
/*      */ 
/*  749 */     String productName = (isAdmin) ? "idccs" : SharedObjects.getEnvironmentValue("IdcProductName");
/*      */ 
/*  751 */     String resDir = LegacyDirectoryLocator.getResourcesDirectory(productName);
/*  752 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  753 */     if ((isAdmin) && (FileUtils.checkFile(new StringBuilder().append(dataDir).append("locale/locale_config.hda").toString(), true, false) != 0))
/*      */     {
/*  756 */       dataDir = SharedObjects.getEnvironmentValue("ServerDataDir");
/*      */     }
/*      */ 
/*  759 */     String dir = new StringBuilder().append(dataDir).append("config/").toString();
/*  760 */     if (FileUtils.checkFile(dir, false, false) == 0)
/*      */     {
/*  762 */       DataBinder tracing = new DataBinder();
/*  763 */       ResourceUtils.serializeDataBinder(dir, "tracing.hda", tracing, false, false);
/*      */ 
/*  765 */       SharedLoader.configureTracing(tracing);
/*      */     }
/*      */ 
/*  769 */     loadEncodingMap(FileUtils.getAbsolutePath(resDir, "core/tables"));
/*      */ 
/*  771 */     loadSystemEncodingInfo();
/*      */ 
/*  775 */     ResourceLoader.loadLocalizationStrings(FileUtils.getAbsolutePath(resDir, "core"), dataDir, DirectoryLocator.getLocalDataDir("strings"));
/*      */ 
/*  777 */     m_initializationStatus |= 256L;
/*      */   }
/*      */ 
/*      */   public static void configLocalization() throws DataException, ServiceException
/*      */   {
/*  782 */     if ((m_initializationStatus & 0x200) > 0L)
/*      */     {
/*  784 */       return;
/*      */     }
/*  786 */     DataBinder binder = new DataBinder();
/*      */ 
/*  788 */     boolean hasOverrides = false;
/*  789 */     String dateFormat = SharedObjects.getEnvironmentValue("LocaleDateFormat");
/*  790 */     if ((dateFormat != null) && (dateFormat.length() > 0))
/*      */     {
/*  792 */       DateUtils.setOverrideDateFormat(dateFormat);
/*  793 */       hasOverrides = true;
/*      */     }
/*      */ 
/*  796 */     String timeFormat = SharedObjects.getEnvironmentValue("LocaleTimeFormat");
/*  797 */     if ((timeFormat != null) && (timeFormat.length() > 0))
/*      */     {
/*  799 */       DateUtils.setOverrideTimeFormat(timeFormat);
/*  800 */       hasOverrides = true;
/*      */     }
/*      */ 
/*  803 */     String dateSep = SharedObjects.getEnvironmentValue("LocaleDateSeparator");
/*  804 */     if ((dateSep != null) && (dateSep.length() > 0))
/*      */     {
/*  806 */       DateUtils.setOverrideDateSeparator(dateSep);
/*  807 */       hasOverrides = true;
/*      */     }
/*      */ 
/*  810 */     String timeSep = SharedObjects.getEnvironmentValue("LocaleTimeSeparator");
/*  811 */     if ((timeSep != null) && (timeSep.length() > 0))
/*      */     {
/*  813 */       DateUtils.setOverrideTimeSeparator(timeSep);
/*  814 */       hasOverrides = true;
/*      */     }
/*      */ 
/*  817 */     String bulkloadFormat = SharedObjects.getEnvironmentValue("LocaleBulkloadFormat");
/*  818 */     if ((bulkloadFormat != null) && (bulkloadFormat.length() > 0))
/*      */     {
/*  820 */       DateUtils.setOverrideBulkloadFormat(bulkloadFormat);
/*  821 */       if (SharedObjects.getEnvironmentValue("BulkloadDateFormat") == null)
/*      */       {
/*  823 */         SharedObjects.putEnvironmentValue("BulkloadDateFormat", bulkloadFormat);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  832 */     String tz = SharedObjects.getEnvironmentValue("SystemTimeZone");
/*      */ 
/*  834 */     if (tz == null)
/*      */     {
/*  836 */       TimeZone timezone = TimeZone.getDefault();
/*  837 */       tz = timezone.getID();
/*      */     }
/*  839 */     binder.putLocal("SystemTimeZone", tz);
/*      */ 
/*  842 */     String systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*  843 */     if (systemLocale == null)
/*      */     {
/*  845 */       throw new ServiceException("!$The SystemLocale is not defined. ");
/*      */     }
/*  847 */     binder.putLocal("SystemLocale", systemLocale);
/*      */ 
/*  849 */     String[] drsets = { "LocaleConfig", "LocaleAcceptLanguageMap", "LanguageLocaleMap" };
/*      */ 
/*  853 */     for (int i = 0; i < drsets.length; ++i)
/*      */     {
/*  855 */       String name = drsets[i];
/*  856 */       DataResultSet drset = SharedObjects.getTable(name);
/*  857 */       if (drset == null)
/*      */         continue;
/*  859 */       binder.addResultSet(name, drset);
/*      */     }
/*      */ 
/*  868 */     boolean overridingDateFormat = false;
/*  869 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  870 */     FieldInfo[] infos = ResultSetUtils.createInfoList(localeConfig, new String[] { "lcLocaleId", "lcDateTimeFormat", "lcTimeZone", "lcIsEnabled" }, true);
/*      */ 
/*  872 */     Vector v = localeConfig.findRow(infos[0].m_index, systemLocale);
/*      */ 
/*  875 */     if (v != null)
/*      */     {
/*  880 */       boolean specifiedLocale = SharedObjects.getEnvValueAsBoolean("SystemLocaleSpecified", false);
/*  881 */       String pattern = null;
/*      */ 
/*  883 */       String specifiedDateFormat = SharedObjects.getEnvironmentValue("SystemDateFormat");
/*  884 */       if (specifiedDateFormat != null)
/*      */       {
/*  886 */         pattern = specifiedDateFormat;
/*      */       }
/*  888 */       else if ((!specifiedLocale) && (!SharedObjects.getEnvValueAsBoolean("SystemLocaleNotFound", false)))
/*      */       {
/*  891 */         DateFormat fmt = DateUtils.determineFormatEx(null, 1);
/*  892 */         if (fmt instanceof SimpleDateFormat)
/*      */         {
/*  894 */           if (hasOverrides)
/*      */           {
/*  896 */             String msg = LocaleResources.getString("csOverridingDateFormatObsolete", null);
/*  897 */             Report.trace("system", msg, null);
/*      */ 
/*  899 */             overridingDateFormat = true;
/*      */           }
/*  901 */           pattern = LocaleUtils.simpleDateFormatToPattern((SimpleDateFormat)fmt);
/*      */         }
/*      */       }
/*      */ 
/*  905 */       if (pattern != null)
/*      */       {
/*  907 */         v.setElementAt(pattern, infos[1].m_index);
/*      */       }
/*  909 */       v.setElementAt(tz, infos[2].m_index);
/*  910 */       v.setElementAt("true", infos[3].m_index);
/*      */     }
/*      */ 
/*  918 */     LocaleLoader.loadLocaleConfig(null, new Hashtable(), binder);
/*      */ 
/*  920 */     if (overridingDateFormat)
/*      */     {
/*  922 */       Report.warning(null, null, "csOverridingDateFormatObsolete", new Object[0]);
/*      */     }
/*      */ 
/*  926 */     if (!SharedObjects.getEnvValueAsBoolean("IsJdbc", false))
/*      */     {
/*  930 */       String fmtString = SharedObjects.getEnvironmentValue("DaoDateFormat");
/*  931 */       if ((fmtString == null) || (fmtString.length() == 0))
/*      */       {
/*  934 */         DataResultSet localeMap = SharedObjects.getTable("LanguageLocaleMap");
/*  935 */         String orgSystemLocale = ResourceLoader.computeLocale(Locale.getDefault(), localeMap);
/*      */ 
/*  937 */         Vector sysValues = localeConfig.findRow(infos[0].m_index, orgSystemLocale);
/*  938 */         fmtString = (String)sysValues.elementAt(infos[1].m_index);
/*      */       }
/*      */ 
/*  941 */       IdcDateFormat idcfmt = LocaleResources.buildDateFormat(fmtString);
/*  942 */       LocaleResources.m_localeDateFormats.put("DaoDateFormat", idcfmt);
/*  943 */       LocaleResources.m_daoFormat = idcfmt;
/*      */     }
/*      */ 
/*  947 */     SubjectCallbackAdapter localesCallback = new LocalesSubjectCallback();
/*  948 */     SubjectManager.registerCallback("locales", localesCallback);
/*  949 */     IdcSystemLoader.getAlreadyLoadedSubjectsList().addElement("locales");
/*      */ 
/*  952 */     LegacyDirectoryLocator.computeAndSetSystemHelpRoot(null);
/*  953 */     m_initializationStatus |= 512L;
/*      */   }
/*      */ 
/*      */   public static void loadEncodingMap(String resDir) throws ServiceException, DataException
/*      */   {
/*  958 */     ResourceContainer res = SharedObjects.getResources();
/*      */     try
/*      */     {
/*  962 */       DataLoader.cacheResourceFile(res, FileUtils.getAbsolutePath(resDir, "std_encoding.htm"));
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  966 */       Report.error(null, e, "csErrorLoadingEncodingResourceFile", new Object[0]);
/*      */     }
/*      */ 
/*  971 */     Map resTables = res.m_tables;
/*  972 */     Iterator it = resTables.keySet().iterator();
/*  973 */     while (it.hasNext())
/*      */     {
/*  975 */       String name = (String)it.next();
/*  976 */       Table tble = (Table)resTables.get(name);
/*  977 */       DataResultSet rset = new DataResultSet();
/*  978 */       rset.init(tble);
/*  979 */       SharedObjects.putTable(name, rset);
/*      */     }
/*      */ 
/*  983 */     DataResultSet drset = SharedObjects.getTable("IsoJavaEncodingMap");
/*  984 */     if (drset == null)
/*      */     {
/*  986 */       throw new ServiceException("!$The IsoJavaEncodingMap table is missing. ");
/*      */     }
/*      */ 
/*  989 */     DataSerializeUtils.setEncodingMap(drset);
/*      */ 
/*  992 */     drset = SharedObjects.getTable("AliasesEncodingMap");
/*  993 */     if (drset == null)
/*      */       return;
/*  995 */     Properties aliasesMap = new IdcProperties();
/*  996 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  998 */       String alias = drset.getStringValue(0);
/*  999 */       String encoding = drset.getStringValue(1);
/* 1000 */       if ((alias == null) || (alias.length() == 0) || (encoding == null) || (encoding.length() == 0)) {
/*      */         continue;
/*      */       }
/* 1003 */       aliasesMap.put(alias.toLowerCase(), encoding);
/*      */     }
/*      */ 
/* 1006 */     LocaleResources.m_encodingAliasesMap = aliasesMap;
/*      */   }
/*      */ 
/*      */   public static void loadSystemEncodingInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1018 */     String xmlEncodingMode = SharedObjects.getEnvironmentValue("XmlEncodingMode");
/* 1019 */     if (xmlEncodingMode != null)
/*      */     {
/* 1021 */       boolean isFull = xmlEncodingMode.equalsIgnoreCase("full");
/* 1022 */       StringUtils.setIsDefaultFullXmlEncodeMode(isFull);
/*      */     }
/*      */ 
/* 1026 */     String encoding = SharedObjects.getEnvironmentValue("FileEncoding");
/* 1027 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 1029 */       encoding = System.getProperty("file.encoding");
/*      */     }
/*      */ 
/* 1032 */     encoding = LocaleResources.getEncodingFromAlias(encoding);
/* 1033 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 1035 */       encoding = "iso-8859-1";
/*      */     }
/*      */ 
/* 1038 */     DataSerializeUtils.setSystemEncoding(encoding);
/* 1039 */     FileUtils.m_javaSystemEncoding = encoding;
/*      */ 
/* 1041 */     String webEncoding = SharedObjects.getEnvironmentValue("WebEncoding");
/* 1042 */     if (webEncoding == null)
/*      */     {
/* 1044 */       webEncoding = encoding;
/*      */     }
/* 1046 */     DataSerializeUtils.setWebEncoding(webEncoding);
/*      */ 
/* 1050 */     boolean isMultiMode = SharedObjects.getEnvValueAsBoolean("IsMultiMode", false);
/* 1051 */     if (isMultiMode)
/*      */     {
/* 1053 */       DataSerializeUtils.setMultiMode(isMultiMode);
/*      */     }
/* 1055 */     boolean useClientEncoding = SharedObjects.getEnvValueAsBoolean("UseClientEncoding", true);
/* 1056 */     DataSerializeUtils.setUseClientEncoding(useClientEncoding);
/*      */ 
/* 1059 */     String pageEncoding = SharedObjects.getEnvironmentValue("PageCharset");
/* 1060 */     if (pageEncoding != null)
/*      */       return;
/* 1062 */     pageEncoding = DataSerializeUtils.getIsoEncoding(encoding);
/* 1063 */     if (pageEncoding == null)
/*      */       return;
/* 1065 */     SharedObjects.putEnvironmentValue("PageCharset", pageEncoding);
/* 1066 */     FileUtils.m_isoSystemEncoding = pageEncoding;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1073 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104596 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcSystemConfig
 * JD-Core Version:    0.5.4
 */