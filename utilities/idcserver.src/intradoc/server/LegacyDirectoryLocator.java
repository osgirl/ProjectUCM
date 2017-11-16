/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.Help;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.LegacyDocumentPathBuilder;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import java.net.URL;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ 
/*      */ public class LegacyDirectoryLocator
/*      */ {
/*      */   public static final int F_USE_SYSTEM_WEB_PATHS = 1;
/*      */   public static final String ROOT_HOME_DIR_KEY = "UCM_ORACLE_HOME";
/*      */   public static final String RELATIVE_PATH_TO_IDC_HOME = "ucm/idc/";
/*   76 */   public static final String[][] DEFAULTS = { { "WeblayoutDir", "weblayout/$DefaultSubWebLayoutDir" }, { "WebLogDir", "$WeblayoutDir/groups/secure/logs/" }, { "BinDir", "bin/" }, { "DatabaseDir", "$IdcHomeDir/database/" }, { "VaultDir", "vault/" }, { "VaultTempDir", "$VaultDir/~temp/" }, { "SystemDir", "$VaultDir/~system/" }, { "SystemDataDir", "$VaultDir/~system/data/" }, { "SystemLogDir", "$VaultDir/~system/log/" }, { "SearchDir", "search/" }, { "ConfigDir", "config/" }, { "QueueDir", "queue/" }, { "DataDir", "data/" }, { "SubjectsDir", "$DataDir/subjects/" }, { "CollectionLocation", "archives/" }, { "CollectionsLocation", "$DataDir/collections/" }, { "WorkflowDir", "$DataDir/workflow/" }, { "ProviderDir", "$DataDir/providers/" }, { "ProjectDir", "$DataDir/projects/" }, { "IdcResourcesDir", "$IdcHomeDir/resources/" }, { "IdcNativeDir", "$IdcHomeDir/native/" }, { "MediaDirectory", "$IdcHomeDir/" } };
/*      */ 
/*  102 */   public static String[] COMPONENT_DIRECTORY_KEYS = { "ComponentDir", "SystemComponentDir" };
/*      */   public static String[][] m_keyDirMap;
/*      */   public static boolean m_missingIdcProductNameReported;
/*      */   public static String m_intradocDir;
/*      */ 
/*      */   public static String getIntradocDir()
/*      */   {
/*  110 */     if (m_intradocDir != null)
/*      */     {
/*  112 */       return m_intradocDir;
/*      */     }
/*  114 */     String idcDir = FileUtils.getParent(FileUtils.fileSlashes(SystemUtils.getBinDir()));
/*      */     try
/*      */     {
/*  118 */       idcDir = processSubDirectory("IntradocDir", "", idcDir);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  123 */       Report.trace("system", "Could not evaluate expression in IntradocDir " + idcDir, e);
/*      */     }
/*  125 */     return idcDir;
/*      */   }
/*      */ 
/*      */   public static void resetRootDirectories()
/*      */   {
/*  131 */     SharedObjects.removeEnvironmentValue("IdcHomeDir");
/*  132 */     SharedObjects.removeEnvironmentValue("UCM_ORACLE_HOME");
/*      */ 
/*  134 */     String[] dirKeys = COMPONENT_DIRECTORY_KEYS;
/*  135 */     for (int d = dirKeys.length - 1; d >= 0; --d)
/*      */     {
/*  137 */       SharedObjects.removeEnvironmentValue(dirKeys[d]);
/*      */     }
/*      */ 
/*  140 */     String[][] keyDirMap = m_keyDirMap;
/*  141 */     for (int d = keyDirMap.length - 1; d >= 0; --d)
/*      */     {
/*  143 */       SharedObjects.removeEnvironmentValue(keyDirMap[d][0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void buildRootDirectories()
/*      */     throws ServiceException
/*      */   {
/*  150 */     String root = getIntradocDir();
/*  151 */     computeHomeDirectory(root);
/*  152 */     updateComponentDirectories();
/*  153 */     setSubWeblayoutDirByAppServer();
/*      */ 
/*  156 */     if (m_keyDirMap == null)
/*      */     {
/*  158 */       m_keyDirMap = DEFAULTS;
/*      */     }
/*      */ 
/*  161 */     for (int i = 0; i < m_keyDirMap.length; ++i)
/*      */     {
/*  163 */       processSubDirectory(m_keyDirMap[i][0], root, m_keyDirMap[i][1]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void computeHomeDirectory(String root) throws ServiceException
/*      */   {
/*  169 */     String homeDir = getOrCheckSystemEnvironmentValue("IdcHomeDir");
/*  170 */     if (homeDir == null)
/*      */     {
/*  172 */       String oracleHome = getOrCheckSystemEnvironmentValue("UCM_ORACLE_HOME");
/*  173 */       if (oracleHome != null)
/*      */       {
/*  176 */         homeDir = FileUtils.getAbsolutePath(oracleHome, "ucm/idc/");
/*      */       }
/*      */     }
/*  179 */     if (homeDir != null)
/*      */     {
/*  183 */       IdcMessage badConfigDirMsg = IdcMessageFactory.lc("csConfigDirectoryIsNotValid", new Object[] { "IdcHomeDir" });
/*  184 */       FileUtils.validatePath(homeDir, badConfigDirMsg, 0);
/*      */     }
/*  186 */     if (homeDir == null)
/*      */     {
/*  189 */       homeDir = root;
/*      */     }
/*  191 */     SharedObjects.putEnvironmentValue("IdcHomeDir", homeDir);
/*      */ 
/*  194 */     intradoc.common.NativeOsUtilsBase.m_IdcHomeDir = homeDir;
/*      */   }
/*      */ 
/*      */   public static String getHomeDirectory()
/*      */   {
/*  201 */     String homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*  202 */     if (homeDir == null)
/*      */     {
/*  205 */       homeDir = SystemUtils.getClonedSystemProperty("IdcHomeDir");
/*      */     }
/*  207 */     if (homeDir == null)
/*      */     {
/*  210 */       String oracleHome = SystemUtils.getClonedSystemProperty("UCM_ORACLE_HOME");
/*  211 */       if (oracleHome != null)
/*      */       {
/*  214 */         homeDir = FileUtils.getAbsolutePath(oracleHome, "ucm/idc/");
/*      */       }
/*      */     }
/*  217 */     if (homeDir == null)
/*      */     {
/*  219 */       homeDir = getIntradocDir();
/*      */     }
/*  221 */     return homeDir;
/*      */   }
/*      */ 
/*      */   public static void updateComponentDirectories()
/*      */   {
/*  226 */     String[] keys = COMPONENT_DIRECTORY_KEYS;
/*  227 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  229 */       String key = keys[i];
/*  230 */       String val = getOrCheckSystemEnvironmentValue(key);
/*  231 */       if ((val == null) && ((
/*  243 */         (val == null) || (val.length() <= 0))))
/*      */         continue;
/*  245 */       val = FileUtils.directorySlashes(val);
/*  246 */       SharedObjects.putEnvironmentValue(key, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static boolean getOrCheckSystemEnvAsBool(String key, boolean defVal)
/*      */   {
/*  253 */     String val = getOrCheckSystemEnvironmentValue(key);
/*  254 */     return StringUtils.convertToBool(val, defVal);
/*      */   }
/*      */ 
/*      */   public static String getOrCheckSystemEnvironmentValue(String key)
/*      */   {
/*  259 */     String val = SharedObjects.getEnvironmentValue(key);
/*  260 */     if ((val == null) || (val.length() == 0))
/*      */     {
/*  262 */       val = SystemUtils.getClonedSystemProperty(key);
/*  263 */       if (val != null)
/*      */       {
/*  265 */         if (val.length() == 0)
/*      */         {
/*  267 */           val = null;
/*      */         }
/*      */         else
/*      */         {
/*  271 */           SharedObjects.putEnvironmentValue(key, val);
/*      */         }
/*      */       }
/*      */     }
/*  275 */     return val;
/*      */   }
/*      */ 
/*      */   public static String processSubDirectory(String key, String root, String subdir)
/*      */     throws ServiceException
/*      */   {
/*  281 */     String temp = SharedObjects.getEnvironmentValue(key);
/*  282 */     if (temp == null)
/*      */     {
/*  284 */       subdir = DocumentPathBuilder.evaluatePathScript(subdir, null, PathUtils.F_VARS_MUST_EXIST, null);
/*  285 */       temp = FileUtils.getAbsolutePath(root, subdir);
/*      */     }
/*      */     else
/*      */     {
/*  289 */       temp = DocumentPathBuilder.evaluatePathScript(temp, null, PathUtils.F_VARS_MUST_EXIST, null);
/*      */     }
/*      */ 
/*  292 */     temp = FileUtils.directorySlashes(temp);
/*  293 */     SharedObjects.putEnvironmentValue(key, temp);
/*  294 */     return temp;
/*      */   }
/*      */ 
/*      */   public static String getLogDirectory()
/*      */   {
/*  299 */     String logDir = SharedObjects.getEnvironmentValue("WebLogDir");
/*  300 */     if (logDir == null)
/*      */     {
/*      */       try
/*      */       {
/*  304 */         buildRootDirectories();
/*  305 */         logDir = SharedObjects.getEnvironmentValue("WebLogDir");
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  309 */         Report.error(null, "Unable to build directories.", e);
/*      */       }
/*      */     }
/*  312 */     return logDir;
/*      */   }
/*      */ 
/*      */   public static String getLogDirectory(String appLog)
/*      */   {
/*  317 */     return getLogDirectory() + appLog;
/*      */   }
/*      */ 
/*      */   public static String getMonitorDirectory()
/*      */   {
/*  329 */     return SharedObjects.getEnvironmentValue("WeblayoutDir") + "groups/secure/monitor/";
/*      */   }
/*      */ 
/*      */   public static String getReportsDirectory() throws ServiceException
/*      */   {
/*  334 */     String reportsDir = getProductParameterizedEnvironmentValue("SystemReportsDir", null);
/*  335 */     if (reportsDir == null)
/*      */     {
/*  337 */       String resources = getResourcesDirectory();
/*  338 */       reportsDir = resources + "core/reports/";
/*      */     }
/*      */     else
/*      */     {
/*  342 */       reportsDir = FileUtils.directorySlashes(replaceEnvironmentToken(reportsDir));
/*      */     }
/*  344 */     return reportsDir;
/*      */   }
/*      */ 
/*      */   public static String getTemplatesDirectory()
/*      */   {
/*  349 */     String templateDir = null;
/*      */     try
/*      */     {
/*  352 */       templateDir = getProductParameterizedEnvironmentValue("SystemTemplatesDir", null);
/*  353 */       if (templateDir != null)
/*      */       {
/*  355 */         templateDir = FileUtils.directorySlashes(replaceEnvironmentToken(templateDir));
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  360 */       if (templateDir == null)
/*      */       {
/*  362 */         templateDir = "<unretrieved>";
/*      */       }
/*  364 */       Report.trace("system", "Could not evaluate SystemTemplatesDir " + templateDir, e);
/*  365 */       templateDir = null;
/*      */     }
/*  367 */     if (templateDir == null)
/*      */     {
/*  369 */       String resourcesDir = getResourcesDirectory();
/*  370 */       templateDir = resourcesDir + "core/templates/";
/*      */     }
/*  372 */     return templateDir;
/*      */   }
/*      */ 
/*      */   public static String getResourcesDirectory()
/*      */   {
/*  377 */     String dir = SharedObjects.getEnvironmentValue("IdcResourcesDir");
/*  378 */     if (dir != null)
/*      */     {
/*  380 */       return FileUtils.directorySlashes(dir);
/*      */     }
/*  382 */     dir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*  383 */     if (dir != null)
/*      */     {
/*  385 */       dir = FileUtils.directorySlashes(FileUtils.getAbsolutePath(dir, "resources"));
/*      */ 
/*  387 */       return dir;
/*      */     }
/*  389 */     dir = SharedObjects.getEnvironmentValue("IntradocDir");
/*  390 */     if (dir != null)
/*      */     {
/*  392 */       dir = FileUtils.directorySlashes(FileUtils.getAbsolutePath(dir, "resources"));
/*      */ 
/*  394 */       return dir;
/*      */     }
/*  396 */     throw new AssertionError("IdcResourcesDir not set");
/*      */   }
/*      */ 
/*      */   public static String getResourcesDirectory(String prodName)
/*      */   {
/*  401 */     String resDir = null;
/*      */     try
/*      */     {
/*  404 */       resDir = getProductParameterizedEnvironmentValue("SystemResourcesDir", prodName);
/*  405 */       if (resDir != null)
/*      */       {
/*  407 */         resDir = FileUtils.directorySlashes(replaceEnvironmentToken(resDir));
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  412 */       if (resDir == null)
/*      */       {
/*  414 */         resDir = "<unretrieved>";
/*      */       }
/*  416 */       Report.trace("system", "Could not evaluate SystemResourcesDir " + resDir, e);
/*  417 */       resDir = null;
/*      */     }
/*  419 */     if (resDir == null)
/*      */     {
/*  421 */       resDir = getResourcesDirectory();
/*      */     }
/*  423 */     return resDir;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getPublishDirectory()
/*      */     throws ServiceException
/*      */   {
/*  431 */     String resDir = getProductParameterizedEnvironmentValue("SystemPublishDir", null);
/*  432 */     if (resDir == null)
/*      */     {
/*  434 */       String sharedDir = getSharedDirectory();
/*  435 */       resDir = sharedDir + "publish/";
/*      */     }
/*      */     else
/*      */     {
/*  439 */       resDir = FileUtils.directorySlashes(replaceEnvironmentToken(resDir));
/*      */     }
/*  441 */     return resDir;
/*      */   }
/*      */ 
/*      */   public static String getProductParameterizedEnvironmentValue(String key, String prodName)
/*      */     throws ServiceException
/*      */   {
/*  448 */     if (prodName == null)
/*      */     {
/*  450 */       prodName = SharedObjects.getEnvironmentValue("IdcProductName");
/*      */     }
/*      */ 
/*  453 */     if (prodName == null)
/*      */     {
/*  455 */       if ((!m_missingIdcProductNameReported) && (SystemUtils.m_isDevelopmentEnvironment))
/*      */       {
/*  457 */         m_missingIdcProductNameReported = true;
/*  458 */         String msg = "IdcProductName is null. Common values: idccs, idcibr, idcadmin";
/*  459 */         Report.error(null, msg, new StackTrace());
/*      */       }
/*  461 */       prodName = "idccs";
/*  462 */       SharedObjects.putEnvironmentValue("IdcProductName", prodName);
/*      */     }
/*      */ 
/*  465 */     return SharedObjects.getParameterizedEnvironmentValue(key, prodName);
/*      */   }
/*      */ 
/*      */   public static String replaceEnvironmentToken(String data) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  472 */       data = DocumentPathBuilder.evaluatePathScript(data, null, PathUtils.F_VARS_MUST_EXIST, null);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  476 */       Report.trace("system", "Could not replace tokens in " + data, e);
/*      */     }
/*  478 */     return data;
/*      */   }
/*      */ 
/*      */   public static String getConfigDirectory()
/*      */   {
/*  483 */     return SharedObjects.getEnvironmentValue("ConfigDir");
/*      */   }
/*      */ 
/*      */   public static String getSharedDirectory()
/*      */   {
/*  488 */     return SharedObjects.getEnvironmentValue("SharedDir");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getSharedOSDirectory()
/*      */   {
/*  495 */     String osDir = SharedObjects.getEnvironmentValue("SharedOSDir");
/*  496 */     if (osDir == null)
/*      */     {
/*  498 */       osDir = DirectoryLocator.getNativeDirectory();
/*      */     }
/*  500 */     return osDir;
/*      */   }
/*      */ 
/*      */   public static String getAppDataDirectory()
/*      */   {
/*  505 */     return SharedObjects.getEnvironmentValue("DataDir");
/*      */   }
/*      */ 
/*      */   public static String getSubjectsDirectory()
/*      */   {
/*  510 */     return SharedObjects.getEnvironmentValue("SubjectsDir");
/*      */   }
/*      */ 
/*      */   public static String getImagesDirectory()
/*      */   {
/*  519 */     String dir = SharedObjects.getEnvironmentValue("ImagesDir");
/*  520 */     if (dir == null)
/*      */     {
/*  522 */       dir = SharedObjects.getEnvironmentValue("SharedWeblayoutDir");
/*  523 */       if (dir == null)
/*      */       {
/*  525 */         dir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */       }
/*  527 */       dir = FileUtils.directorySlashes(dir);
/*  528 */       dir = dir + "images/";
/*      */     }
/*  530 */     return dir;
/*      */   }
/*      */ 
/*      */   public static String getDocGifsDirectory()
/*      */   {
/*  535 */     return SharedObjects.getEnvironmentValue("WeblayoutDir") + "images/docgifs/";
/*      */   }
/*      */ 
/*      */   public static String getHelpDirectory()
/*      */   {
/*  540 */     String dir = SharedObjects.getEnvironmentValue("HelpDir");
/*  541 */     if (dir == null)
/*      */     {
/*  543 */       dir = SharedObjects.getEnvironmentValue("SharedWeblayoutDir");
/*  544 */       if (dir == null)
/*      */       {
/*  546 */         dir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */       }
/*  548 */       dir = FileUtils.directorySlashes(dir);
/*  549 */       dir = dir + "help/";
/*      */     }
/*  551 */     return dir;
/*      */   }
/*      */ 
/*      */   public static String getWebGroupRootDirectory(String securityGroup)
/*      */   {
/*  556 */     return SharedObjects.getEnvironmentValue("WeblayoutDir") + "groups/" + securityGroup.toLowerCase() + "/";
/*      */   }
/*      */ 
/*      */   public static String getDocConversionDirectory()
/*      */   {
/*  562 */     return SharedObjects.getEnvironmentValue("QueueDir") + "conversion/";
/*      */   }
/*      */ 
/*      */   public static String getWeblayoutDirectory()
/*      */   {
/*  567 */     return SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */   }
/*      */ 
/*      */   public static String getVaultDirectory()
/*      */   {
/*  572 */     return SharedObjects.getEnvironmentValue("VaultDir");
/*      */   }
/*      */ 
/*      */   public static String getVaultTempDirectory()
/*      */   {
/*  577 */     return SharedObjects.getEnvironmentValue("VaultTempDir");
/*      */   }
/*      */ 
/*      */   public static String getSystemDirectory()
/*      */   {
/*  582 */     return SharedObjects.getEnvironmentValue("SystemDir");
/*      */   }
/*      */ 
/*      */   public static String getSystemDataDirectory()
/*      */   {
/*  587 */     return SharedObjects.getEnvironmentValue("SystemDataDir");
/*      */   }
/*      */ 
/*      */   public static String getSystemLogDirectory()
/*      */   {
/*  592 */     return SharedObjects.getEnvironmentValue("SystemLogDir");
/*      */   }
/*      */ 
/*      */   public static void buildBaseDirectories(String storeLocation)
/*      */   {
/*  597 */     if (storeLocation.equalsIgnoreCase("Filesystem"))
/*      */     {
/*  599 */       SharedObjects.putEnvironmentValue("BaseBinaryDir", DirectoryLocator.getIntradocDir());
/*  600 */       SharedObjects.putEnvironmentValue("BaseLogDir", DirectoryLocator.getAppDataDirectory());
/*  601 */       SharedObjects.putEnvironmentValue("BaseDataDir", DirectoryLocator.getAppDataDirectory());
/*      */     } else {
/*  603 */       if (!storeLocation.equalsIgnoreCase("Database"))
/*      */         return;
/*  605 */       SharedObjects.putEnvironmentValue("BaseBinaryDir", DirectoryLocator.getSystemDirectory());
/*      */ 
/*  607 */       SharedObjects.putEnvironmentValue("BaseLogDir", DirectoryLocator.getSystemLogDirectory());
/*      */ 
/*  609 */       SharedObjects.putEnvironmentValue("BaseDataDir", DirectoryLocator.getSystemDataDirectory());
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getSystemBaseDirectory(String type)
/*      */   {
/*  616 */     if (type.equalsIgnoreCase("data"))
/*      */     {
/*  618 */       return SharedObjects.getEnvironmentValue("BaseDataDir");
/*      */     }
/*  620 */     if (type.equalsIgnoreCase("log"))
/*      */     {
/*  622 */       return SharedObjects.getEnvironmentValue("BaseLogDir");
/*      */     }
/*  624 */     if (type.equalsIgnoreCase("binary"))
/*      */     {
/*  626 */       return SharedObjects.getEnvironmentValue("BaseBinaryDir");
/*      */     }
/*      */ 
/*  630 */     return null;
/*      */   }
/*      */ 
/*      */   public static String getSearchDirectory()
/*      */   {
/*  636 */     return SharedObjects.getEnvironmentValue("SearchDir");
/*      */   }
/*      */ 
/*      */   public static String getUserCacheDir()
/*      */   {
/*  641 */     String userCache = SharedObjects.getEnvironmentValue("UserCache");
/*  642 */     if (userCache == null)
/*      */     {
/*  644 */       userCache = SharedObjects.getEnvironmentValue("BaseDataDir") + "users/";
/*      */     }
/*  646 */     if (userCache != null)
/*      */     {
/*  648 */       userCache = FileUtils.directorySlashes(userCache);
/*      */     }
/*  650 */     return userCache;
/*      */   }
/*      */ 
/*      */   public static String getUserPublishCacheDir()
/*      */   {
/*  655 */     String publishDir = SharedObjects.getEnvironmentValue("UserPublishCacheDir");
/*  656 */     if ((publishDir == null) || (publishDir.length() == 0))
/*      */     {
/*  658 */       publishDir = getUserCacheDir();
/*      */     }
/*      */     else
/*      */     {
/*  662 */       publishDir = FileUtils.directorySlashes(publishDir);
/*      */     }
/*  664 */     return publishDir;
/*      */   }
/*      */ 
/*      */   public static boolean hasSeparateUserPublishDir()
/*      */   {
/*  669 */     String publishDir = SharedObjects.getEnvironmentValue("UserPublishCacheDir");
/*  670 */     return (publishDir != null) && (publishDir.length() > 0);
/*      */   }
/*      */ 
/*      */   public static String getUserProfilesDir()
/*      */   {
/*  675 */     String dir = SharedObjects.getEnvironmentValue("UserProfilesDir");
/*  676 */     if (dir == null)
/*      */     {
/*  678 */       dir = getUserCacheDir() + "profiles/";
/*      */     }
/*  680 */     return dir;
/*      */   }
/*      */ 
/*      */   public static void buildWebRoots()
/*      */     throws ServiceException
/*      */   {
/*  698 */     String absRoot = DocumentPathBuilder.getAbsoluteWebRoot();
/*  699 */     String relRoot = DocumentPathBuilder.getRelativeWebRoot();
/*  700 */     String baseRoot = DocumentPathBuilder.getBaseAbsoluteRoot();
/*      */ 
/*  702 */     String sharedRoot = createAbsoluteAndRelativePair("SharedRoot", relRoot, baseRoot, absRoot);
/*  703 */     createAbsoluteAndRelativePair("CommonRoot", sharedRoot + "common/", baseRoot, absRoot);
/*  704 */     createAbsoluteAndRelativePair("ImagesRoot", sharedRoot + "images/", baseRoot, absRoot);
/*      */ 
/*  706 */     String defaultHelpPathExpression = "${HttpSharedRoot}help";
/*  707 */     if (getOrCheckSystemEnvAsBool("UseOhsHelpSystem", true))
/*      */     {
/*  710 */       getOrCheckSystemEnvironmentValue("OhsHelpContextRoot");
/*      */ 
/*  718 */       defaultHelpPathExpression = "${OhsHelpContextRoot:$ifcmp(${WebServer:-},*app*|*wls*,$HttpAppHelpSharedRoot:/,$HttpSharedRoot)_ocsh/help}";
/*      */     }
/*  720 */     String helpPathExpression = getOrCheckSystemEnvironmentValue("HelpRootPrefixExpression");
/*  721 */     if ((helpPathExpression == null) || (helpPathExpression.length() == 0))
/*      */     {
/*  723 */       helpPathExpression = defaultHelpPathExpression;
/*      */     }
/*  725 */     DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  726 */     String defaultRelativeHelpPath = DocumentPathBuilder.evaluatePathScript(helpPathExpression, binder, 0, null);
/*      */ 
/*  729 */     createAbsoluteAndRelativePair("HelpRootPrefix", defaultRelativeHelpPath, baseRoot, absRoot);
/*      */   }
/*      */ 
/*      */   protected static String createAbsoluteAndRelativePair(String suffix, String defaultRel, String baseRoot, String absRoot)
/*      */   {
/*  735 */     String relValue = getOrCheckSystemEnvironmentValue("Http" + suffix);
/*  736 */     if (relValue == null)
/*      */     {
/*  738 */       relValue = defaultRel;
/*  739 */       SharedObjects.putEnvironmentValue("Http" + suffix, relValue);
/*      */     }
/*      */ 
/*  742 */     String absValue = getOrCheckSystemEnvironmentValue("HttpAbsolute" + suffix);
/*  743 */     if (absValue == null)
/*      */     {
/*  745 */       absValue = relValue;
/*  746 */       if (absValue.startsWith("/"))
/*      */       {
/*  748 */         absValue = baseRoot + absValue;
/*      */       }
/*  750 */       else if (!absValue.toLowerCase().startsWith("http://"))
/*      */       {
/*  752 */         absValue = absRoot + absValue;
/*      */       }
/*  754 */       SharedObjects.putEnvironmentValue("HttpAbsolute" + suffix, absValue);
/*      */     }
/*      */ 
/*  757 */     return relValue;
/*      */   }
/*      */ 
/*      */   public static String getWebRoot(boolean isAbsolute)
/*      */   {
/*  762 */     if (isAbsolute)
/*      */     {
/*  764 */       return DocumentPathBuilder.getAbsoluteWebRoot();
/*      */     }
/*  766 */     return DocumentPathBuilder.getRelativeWebRoot();
/*      */   }
/*      */ 
/*      */   public static String computeHelpRoot(DataBinder binder, ExecutionContext cxt, int flags)
/*      */     throws ServiceException
/*      */   {
/*  775 */     buildWebRoots();
/*  776 */     boolean useSystemLocale = (flags & 0x1) != 0;
/*  777 */     boolean isAbsolute = StringUtils.convertToBool(binder.getAllowMissing("isAbsoluteWeb"), false);
/*  778 */     String helpPathPrefix = SharedObjects.getEnvironmentValue((isAbsolute) ? "HttpAbsoluteHelpRootPrefix" : "HttpHelpRootPrefix");
/*  779 */     binder.putLocal("helpRootPrefix", helpPathPrefix);
/*  780 */     String langId = null;
/*  781 */     if ((!useSystemLocale) || (Help.getIsSystemHelpLocalized()))
/*      */     {
/*  783 */       langId = Help.computeLangHelpId(cxt);
/*      */     }
/*  785 */     if (langId == null)
/*      */     {
/*  787 */       langId = LocaleResources.getSystemLocale().m_languageId;
/*  788 */       if (!Help.isValidHelpLangId(langId))
/*      */       {
/*  792 */         langId = "";
/*      */       }
/*      */     }
/*      */ 
/*  796 */     binder.putLocal("helpLangId", langId);
/*  797 */     String helpPathExpression = SharedObjects.getEnvironmentValue("HttpHelpRootExpression");
/*      */ 
/*  799 */     if ((helpPathExpression == null) || (helpPathExpression.length() == 0))
/*      */     {
/*  801 */       helpPathExpression = "$helpRootPrefix?${ifset(helpLangId,locale=${helpLangId}&)}topic=";
/*      */     }
/*  803 */     return DocumentPathBuilder.evaluatePathScript(helpPathExpression, binder, PathUtils.F_VARS_MUST_EXIST, cxt);
/*      */   }
/*      */ 
/*      */   public static void computeAndSetSystemHelpRoot(ExecutionContext cxt) throws ServiceException
/*      */   {
/*  808 */     if (cxt == null)
/*      */     {
/*  810 */       cxt = new ExecutionContextAdaptor();
/*      */     }
/*  812 */     DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  813 */     binder.putLocal("isAbsoluteWeb", "1");
/*  814 */     String systemHelpRoot = computeHelpRoot(binder, cxt, 0);
/*      */     try
/*      */     {
/*  817 */       Help.setHelpUrlBase(new URL(systemHelpRoot));
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  821 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getCgiWebUrl(boolean isAbsolute)
/*      */   {
/*      */     String cgiRoot;
/*      */     String cgiRoot;
/*  828 */     if (isAbsolute)
/*      */     {
/*  830 */       cgiRoot = getAbsoluteCgiRoot();
/*      */     }
/*      */     else
/*      */     {
/*  834 */       cgiRoot = getRelativeCgiRoot();
/*      */     }
/*  836 */     return cgiRoot + getCgiFileName();
/*      */   }
/*      */ 
/*      */   public static String getAbsoluteCgiRoot()
/*      */   {
/*  841 */     String str = SharedObjects.getEnvironmentValue("HttpAbsoluteCgiRoot");
/*  842 */     if (str == null)
/*      */     {
/*  844 */       IdcStringBuilder path = new IdcStringBuilder(100);
/*  845 */       appendAbsoluteEnterpriseWebRoot(path);
/*  846 */       String relRoot = getRelativeCgiRoot();
/*  847 */       path.append(relRoot);
/*      */ 
/*  849 */       str = path.toString();
/*      */     }
/*  851 */     return str;
/*      */   }
/*      */ 
/*      */   public static void appendAbsoluteEnterpriseWebRoot(IdcStringBuilder path)
/*      */   {
/*  856 */     String domain = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  857 */     if (domain == null)
/*      */     {
/*  859 */       return;
/*      */     }
/*  861 */     boolean isSSL = SharedObjects.getEnvValueAsBoolean("UseSSL", false);
/*  862 */     String httpPrefix = (isSSL) ? "https://" : "http://";
/*  863 */     path.append(httpPrefix);
/*  864 */     path.append(domain);
/*      */   }
/*      */ 
/*      */   public static String getRelativeCgiRoot()
/*      */   {
/*  869 */     String str = SharedObjects.getEnvironmentValue("HttpRelativeCgiRoot");
/*  870 */     if (str == null)
/*      */     {
/*  872 */       return DocumentPathBuilder.getRelativeWebRoot();
/*      */     }
/*  874 */     return str;
/*      */   }
/*      */ 
/*      */   public static String getCgiFileName()
/*      */   {
/*  879 */     String str = SharedObjects.getEnvironmentValue("CgiFileName");
/*  880 */     if (str == null)
/*      */     {
/*  882 */       return "idcplg";
/*      */     }
/*  884 */     return str;
/*      */   }
/*      */ 
/*      */   public static String createRootCgiFileName()
/*      */   {
/*  893 */     String cgiFileName = DirectoryLocator.getCgiFileName();
/*  894 */     int index = cgiFileName.indexOf(47);
/*  895 */     if (index > 0)
/*      */     {
/*  897 */       cgiFileName = cgiFileName.substring(0, index);
/*      */     }
/*  899 */     return cgiFileName;
/*      */   }
/*      */ 
/*      */   public static String getEnterpriseCgiWebUrl(boolean isAbsolute)
/*      */   {
/*      */     String cgiRoot;
/*      */     String cgiRoot;
/*  905 */     if (isAbsolute)
/*      */     {
/*  907 */       cgiRoot = getAbsoluteCgiRoot();
/*      */     }
/*      */     else
/*      */     {
/*  911 */       cgiRoot = getRelativeCgiRoot();
/*      */     }
/*      */ 
/*  914 */     String rootFileName = createRootCgiFileName();
/*  915 */     return cgiRoot + rootFileName;
/*      */   }
/*      */ 
/*      */   public static String createProxiedCgiFileName(String cgiFileName, String relativeWebRoot)
/*      */   {
/*  923 */     if (cgiFileName.endsWith("pxs"))
/*      */     {
/*  925 */       return cgiFileName;
/*      */     }
/*  927 */     return cgiFileName + relativeWebRoot + "pxs";
/*      */   }
/*      */ 
/*      */   public static String getExternalProxiedCgiWebUrl(boolean isAbsolute, String relativeWebRoot)
/*      */   {
/*      */     String cgiRoot;
/*      */     String cgiRoot;
/*  934 */     if (isAbsolute)
/*      */     {
/*  936 */       cgiRoot = getAbsoluteCgiRoot();
/*      */     }
/*      */     else
/*      */     {
/*  940 */       cgiRoot = getRelativeCgiRoot();
/*      */     }
/*      */ 
/*  944 */     String rootFileName = createRootCgiFileName();
/*  945 */     return cgiRoot + rootFileName + relativeWebRoot + "pxs";
/*      */   }
/*      */ 
/*      */   public static String getIntradocCgiRoot(boolean isAbsolute)
/*      */   {
/*  950 */     if (isAbsolute)
/*      */     {
/*  952 */       return getAbsoluteCgiRoot();
/*      */     }
/*  954 */     return getRelativeCgiRoot();
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int getIntradocServerPort()
/*      */   {
/*  963 */     String str = SharedObjects.getEnvironmentValue("IntradocServerPort");
/*  964 */     return NumberUtils.parseInteger(str, 0);
/*      */   }
/*      */ 
/*      */   public static String getWebSecurityGroupRoot(String group, boolean isAbsolute)
/*      */   {
/*  969 */     String envVarRoot = "Http" + group + "Root";
/*  970 */     String str = SharedObjects.getEnvironmentValue(envVarRoot);
/*  971 */     if (str == null)
/*      */     {
/*  973 */       str = getWebRoot(isAbsolute) + "groups/" + group.toLowerCase() + "/";
/*      */     }
/*  975 */     return str;
/*      */   }
/*      */ 
/*      */   public static String getRelativeAdminRoot()
/*      */   {
/*  980 */     String str = SharedObjects.getEnvironmentValue("HttpRelativeAdminRoot");
/*  981 */     if (str == null)
/*      */     {
/*  983 */       return "/cs-admin/";
/*      */     }
/*  985 */     return str;
/*      */   }
/*      */ 
/*      */   public static String computeVaultPath(String fileName, Parameters docInfo)
/*      */     throws DataException
/*      */   {
/*  992 */     String vaultPath = getVaultDirectory() + LegacyDocumentPathBuilder.computeRelativeVaultDir(docInfo) + fileName;
/*      */ 
/*  995 */     return vaultPath;
/*      */   }
/*      */ 
/*      */   public static String computeVaultFileName(Parameters docInfo) throws DataException
/*      */   {
/* 1000 */     String docName = null;
/* 1001 */     String id = docInfo.get("dID");
/* 1002 */     String extension = docInfo.get("dExtension");
/* 1003 */     docName = id;
/* 1004 */     if ((extension != null) && (extension.length() > 0))
/*      */     {
/* 1006 */       docName = docName + "." + extension.toLowerCase();
/*      */     }
/* 1008 */     return docName;
/*      */   }
/*      */ 
/*      */   public static String computeWebPathDir(Parameters docInfo) throws DataException
/*      */   {
/* 1013 */     String securityGroup = docInfo.get("dSecurityGroup");
/* 1014 */     String groupDir = getWebGroupRootDirectory(securityGroup);
/*      */ 
/* 1016 */     String wPathDir = groupDir + LegacyDocumentPathBuilder.computeWebDirSuffix(docInfo);
/*      */ 
/* 1018 */     return wPathDir;
/*      */   }
/*      */ 
/*      */   public static String getOitFilePath(String filePath, String fileType)
/*      */     throws ServiceException
/*      */   {
/* 1031 */     Map pathArgs = new HashMap();
/* 1032 */     pathArgs = getOitMap(pathArgs, filePath, fileType);
/* 1033 */     String path = (String)pathArgs.get("path");
/* 1034 */     if (path == null)
/*      */     {
/* 1036 */       List attemptedPaths = (List)pathArgs.get("attemptedPaths");
/* 1037 */       String msg = LocaleUtils.encodeMessage("csContentAccessExecutableMissing", null, StringUtils.createStringSimple(attemptedPaths));
/*      */ 
/* 1039 */       throw new ServiceException(msg);
/*      */     }
/* 1041 */     return path;
/*      */   }
/*      */ 
/*      */   public static Map getOitMap(Map pathArgs, String filePath, String fileType)
/*      */     throws ServiceException
/*      */   {
/* 1056 */     if (fileType != null)
/*      */     {
/* 1058 */       pathArgs.put(fileType, fileType);
/*      */     }
/*      */ 
/* 1061 */     boolean useLegacyContentAccess = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseLegacyContentAccessComponent"), false);
/* 1062 */     if (!useLegacyContentAccess)
/*      */     {
/* 1064 */       String oracleHome = getOrCheckSystemEnvironmentValue("UCM_ORACLE_HOME");
/*      */ 
/* 1066 */       if (oracleHome != null)
/*      */       {
/* 1068 */         pathArgs.put("base_directory", oracleHome + "/oit");
/*      */       }
/*      */       else
/*      */       {
/* 1072 */         String idcHomeDir = getOrCheckSystemEnvironmentValue("IdcHomeDir");
/* 1073 */         if (idcHomeDir != null)
/*      */         {
/* 1075 */           String parentDir = FileUtils.getParent(idcHomeDir);
/* 1076 */           if (parentDir != null)
/*      */           {
/* 1078 */             String ucmHomeDir = FileUtils.getParent(parentDir);
/* 1079 */             if ((ucmHomeDir != null) && (FileUtils.checkPathExists(ucmHomeDir + "/oit")))
/*      */             {
/* 1081 */               pathArgs.put("base_directory", ucmHomeDir + "/oit");
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1087 */     pathArgs = EnvUtils.normalizeOSPath(filePath, pathArgs);
/* 1088 */     return pathArgs;
/*      */   }
/*      */ 
/*      */   public static void setSubWeblayoutDirByAppServer()
/*      */   {
/* 1104 */     String defaultSubWebLayoutDir = "";
/* 1105 */     if (EnvUtils.isAppServerType("websphere"))
/*      */     {
/* 1107 */       String productName = EnvUtils.getProductName();
/* 1108 */       if ((productName == null) || (productName.equalsIgnoreCase("idccs")))
/*      */       {
/* 1110 */         defaultSubWebLayoutDir = "cs/";
/*      */       }
/*      */     }
/*      */ 
/* 1114 */     if (!SharedObjects.isInit())
/*      */     {
/* 1116 */       SharedObjects.init();
/*      */     }
/* 1118 */     SharedObjects.addSecureEnvironmentKey("DefaultSubWebLayoutDir");
/* 1119 */     SharedObjects.putEnvironmentValueWithoutOverwrite("DefaultSubWebLayoutDir", defaultSubWebLayoutDir, "setSubWeblayoutDirByAppServer");
/*      */   }
/*      */ 
/*      */   public static String computeCurrentReleaseWebPath(String name, Parameters params) throws DataException
/*      */   {
/* 1124 */     String path = computeWebPathDir(params);
/*      */ 
/* 1127 */     String rState = params.get("dReleaseState");
/* 1128 */     if ("YUI".indexOf(rState) < 0)
/*      */     {
/* 1131 */       String revLabel = params.get("dRevLabel");
/* 1132 */       name = name + "~" + revLabel.toLowerCase();
/*      */     }
/*      */ 
/* 1137 */     name = name.toLowerCase();
/* 1138 */     path = path + name;
/* 1139 */     String webExtension = params.get("dWebExtension");
/* 1140 */     if ((webExtension != null) && (webExtension.length() > 0))
/*      */     {
/* 1142 */       path = path + "." + webExtension;
/*      */     }
/* 1144 */     return path;
/*      */   }
/*      */ 
/*      */   public static String computeWebSecurityPathDir(Parameters docInfo) throws DataException
/*      */   {
/* 1149 */     String securityGroup = docInfo.get("dSecurityGroup");
/* 1150 */     String groupDir = getWebGroupRootDirectory(securityGroup);
/*      */ 
/* 1152 */     String wPathDir = groupDir + LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(docInfo);
/*      */ 
/* 1154 */     return wPathDir;
/*      */   }
/*      */ 
/*      */   public static String computeWebPath(String fileName, Parameters docInfo) throws DataException
/*      */   {
/* 1159 */     String wPath = computeWebPathDir(docInfo) + fileName.toLowerCase();
/* 1160 */     return wPath;
/*      */   }
/*      */ 
/*      */   public static String getDefaultCollection()
/*      */   {
/* 1166 */     return SharedObjects.getEnvironmentValue("CollectionLocation");
/*      */   }
/*      */ 
/*      */   public static String getCollectionsDirectory()
/*      */   {
/* 1172 */     return SharedObjects.getEnvironmentValue("CollectionsLocation");
/*      */   }
/*      */ 
/*      */   public static String getCollectionExport()
/*      */   {
/* 1178 */     return SharedObjects.getEnvironmentValue("CollectionExportLocation");
/*      */   }
/*      */ 
/*      */   public static void buildCollectionExport(String storeLocation)
/*      */   {
/* 1183 */     if (storeLocation.equalsIgnoreCase("Filesystem"))
/*      */     {
/* 1185 */       SharedObjects.putEnvironmentValue("CollectionExportLocation", getDefaultCollection());
/*      */     } else {
/* 1187 */       if (!storeLocation.equalsIgnoreCase("Database"))
/*      */         return;
/* 1189 */       String collExport = getSystemBaseDirectory("binary") + "archives/";
/* 1190 */       SharedObjects.putEnvironmentValue("CollectionExportLocation", collExport);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String getWorkflowDirectory()
/*      */   {
/* 1196 */     return SharedObjects.getEnvironmentValue("WorkflowDir");
/*      */   }
/*      */ 
/*      */   public static String getProviderDirectory()
/*      */   {
/* 1201 */     return SharedObjects.getEnvironmentValue("ProviderDir");
/*      */   }
/*      */ 
/*      */   public static String getProjectDirectory()
/*      */   {
/* 1206 */     return SharedObjects.getEnvironmentValue("ProjectDir");
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1211 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102928 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.LegacyDirectoryLocator
 * JD-Core Version:    0.5.4
 */