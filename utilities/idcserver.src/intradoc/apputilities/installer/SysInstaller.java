/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.PathScriptConstructInfo;
/*      */ import intradoc.common.PathVariableLookupCallback;
/*      */ import intradoc.common.PosixStructStat;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ScriptContext;
/*      */ import intradoc.common.ScriptExtensions;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.resource.ResourceObjectLoader;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcSystemConfig;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.script.ServerScriptExtensions;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.InstallInterface;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.FilenameFilter;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Writer;
/*      */ import java.lang.reflect.Method;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.ZipEntry;
/*      */ import java.util.zip.ZipFile;
/*      */ 
/*      */ public class SysInstaller
/*      */   implements ReportProgress, InstallInterface, PathVariableLookupCallback
/*      */ {
/*      */   public static final int F_REQUIRED = 1;
/*      */   public static final int F_FILESLASHES = 2;
/*      */   public static final int F_DIRECTORYSLASHES = 4;
/*      */   public static final int F_DIRECTORYSLASHES2 = 68;
/*      */   public static final int F_RELATIVEURL = 8;
/*      */   public static final int F_MAX = 16;
/*      */   public static final int F_UNIQUE = 32;
/*      */   public static final int F_RESERVED = 64;
/*      */   public static final int F_PASSWORD = 128;
/*      */   public static final int F_NODEFAULT = 256;
/*      */   public static final int C_PRESERVE_LINKS = 1;
/*      */   public static final int C_PRESERVE_PERMISSIONS = 2;
/*      */   public static final int C_REPORT_PROGRESS = 4;
/*      */   public static final String WIN32 = "win32";
/*      */   public static final String WINDOWS_AMD64 = "windows-amd64";
/*   87 */   public static final String[] WIN_OS_NAMES = { "win32", "windows-amd64" };
/*      */ 
/*   89 */   public static final Object[][] FLAG_MAP = { { "REQUIRED", new Integer(1) }, { "FILESLASHES", new Integer(2) }, { "DIRECTORYSLASHES", new Integer(4) }, { "DIRECTORYSLASHES2", new Integer(68) }, { "RELATIVEURL", new Integer(8) }, { "max:", new Integer(16) }, { "UNIQUE", new Integer(32) }, { "PASSWORD", new Integer(128) }, { "NODEFAULT", new Integer(256) } };
/*      */   protected byte[] m_byteBuffer;
/*      */   public DataBinder m_binder;
/*      */   public InstallLog m_installLog;
/*      */   public Hashtable m_pathTransMap;
/*      */   public Hashtable m_altTransMap;
/*      */   public boolean m_retentiveReturnCode;
/*      */   public boolean m_isUpdate;
/*      */   public String m_installedVersion;
/*      */   public String[] m_myPlatforms;
/*      */   public String m_platform;
/*      */   public long m_fileOutputTime;
/*      */   public long m_fileOutputBytes;
/*      */   protected Hashtable m_installerTables;
/*      */   protected ReportProgress m_progress;
/*      */   protected String m_priorIdcDir;
/*      */   protected String m_targetDir;
/*      */   protected String m_idcDir;
/*      */   protected String m_srcDir;
/*      */   protected int[] m_workStatus;
/*      */   public Properties m_intradocConfig;
/*      */   public Properties m_installerConfig;
/*      */   public Properties m_overrideProps;
/*      */   public NativeOsUtils m_utils;
/*      */   public PromptUser m_promptUser;
/*      */   public PageMerger m_pageMerger;
/*      */   public ExecutionContext m_context;
/*      */   public static final int F_OWN_ADMIN = 1;
/*      */   public static final int F_OTHER_ADMIN = 2;
/*      */   public static final int F_REMOTE_ADMIN = 4;
/*      */ 
/*      */   public SysInstaller()
/*      */   {
/*  102 */     this.m_byteBuffer = new byte[65536];
/*      */ 
/*  108 */     this.m_retentiveReturnCode = false;
/*      */ 
/*  110 */     this.m_installedVersion = "";
/*  111 */     this.m_myPlatforms = null;
/*  112 */     this.m_platform = null;
/*  113 */     this.m_fileOutputTime = 0L;
/*  114 */     this.m_fileOutputBytes = 0L;
/*      */ 
/*  118 */     this.m_priorIdcDir = "";
/*      */ 
/*  126 */     this.m_workStatus = new int[] { 0, 1, 0 };
/*      */ 
/*  132 */     this.m_utils = null;
/*      */ 
/*  135 */     this.m_promptUser = null;
/*  136 */     this.m_pageMerger = null;
/*  137 */     this.m_context = null;
/*      */   }
/*      */ 
/*      */   public int init(DataBinder installDefinition, Properties config, Properties overrideProps, InstallLog log, ReportProgress progress, PromptUser prompt)
/*      */     throws DataException
/*      */   {
/*  144 */     this.m_installerConfig = new Properties(config);
/*  145 */     this.m_overrideProps = new Properties(overrideProps);
/*  146 */     Enumeration en = config.propertyNames();
/*  147 */     while (en.hasMoreElements())
/*      */     {
/*  149 */       String key = (String)en.nextElement();
/*  150 */       String value = config.getProperty(key);
/*  151 */       this.m_installerConfig.put(key.toLowerCase(), value);
/*      */     }
/*      */ 
/*  154 */     this.m_binder = installDefinition;
/*  155 */     this.m_binder.setLocalData(this.m_installerConfig);
/*  156 */     this.m_installLog = log;
/*  157 */     this.m_progress = progress;
/*  158 */     String idcDir = config.getProperty("IntradocDir");
/*  159 */     String srcDir = config.getProperty("SourceDirectory");
/*  160 */     String targetDir = config.getProperty("TargetDir");
/*  161 */     if (idcDir != null)
/*      */     {
/*  163 */       this.m_idcDir = FileUtils.directorySlashes(idcDir);
/*      */     }
/*  165 */     if (srcDir != null)
/*      */     {
/*  167 */       this.m_srcDir = FileUtils.directorySlashes(srcDir);
/*      */     }
/*  169 */     if (targetDir != null)
/*      */     {
/*  171 */       this.m_targetDir = FileUtils.directorySlashes(targetDir);
/*      */     }
/*      */     else
/*      */     {
/*  175 */       this.m_targetDir = this.m_idcDir;
/*      */     }
/*  177 */     if ((this.m_idcDir == null) && (this.m_targetDir != null))
/*      */     {
/*  179 */       this.m_idcDir = this.m_targetDir;
/*      */     }
/*      */ 
/*  182 */     this.m_intradocConfig = new Properties();
/*  183 */     this.m_installerTables = new Hashtable();
/*  184 */     this.m_promptUser = prompt;
/*      */ 
/*  186 */     String srcPath = FileUtils.getAbsolutePath(this.m_srcDir);
/*  187 */     config.put("SourceDirectory", srcPath);
/*      */ 
/*  189 */     this.m_pathTransMap = new Hashtable();
/*  190 */     this.m_altTransMap = new Hashtable();
/*  191 */     for (int i = 0; i < DirectoryLocator.DEFAULTS.length; ++i)
/*      */     {
/*  193 */       String key = DirectoryLocator.DEFAULTS[i][0];
/*  194 */       String prefix = DirectoryLocator.DEFAULTS[i][1];
/*  195 */       if (prefix.startsWith("$"))
/*      */       {
/*  197 */         String altExpression = prefix;
/*  198 */         int index = prefix.indexOf("/");
/*  199 */         if (index > 0)
/*      */         {
/*  201 */           prefix = prefix.substring(index + 1);
/*  202 */           String altKey = altExpression.substring(1, index);
/*  203 */           altExpression = "${" + altKey + "}" + altExpression.substring(index);
/*      */ 
/*  205 */           if (getInstallValue(altKey, null) != null)
/*      */           {
/*  207 */             this.m_altTransMap.put(prefix, altExpression);
/*      */           }
/*      */         }
/*      */       }
/*  211 */       this.m_pathTransMap.put(prefix, key);
/*      */     }
/*      */ 
/*  214 */     loadInstallerTables();
/*      */     try
/*      */     {
/*  218 */       this.m_utils = new NativeOsUtils();
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  222 */       Report.trace("install", "Unable to instantiate NativeOsUtils.", t);
/*      */     }
/*      */ 
/*  226 */     String platform = getInstallValue("Platform", null);
/*  227 */     setPlatform(platform);
/*      */ 
/*  229 */     this.m_retentiveReturnCode = getInstallBool("UseRetentiveReturnCode", false);
/*  230 */     ScriptContext scriptContext = (ScriptContext)AppObjectRepository.getObject("DefaultScriptContext");
/*      */ 
/*  232 */     ScriptExtensions extensions = new ServerScriptExtensions();
/*  233 */     extensions.load(scriptContext);
/*      */ 
/*  235 */     this.m_context = new ExecutionContextAdaptor();
/*  236 */     this.m_context.setCachedObject("SysInstaller", this);
/*  237 */     this.m_pageMerger = new PageMerger(this.m_binder, this.m_context);
/*  238 */     this.m_context.setCachedObject("PageMerger", this.m_pageMerger);
/*      */ 
/*  240 */     String term = null;
/*  241 */     if (this.m_utils != null)
/*      */     {
/*  243 */       term = this.m_utils.getEnv("TERM");
/*      */     }
/*  245 */     if (term == null)
/*      */     {
/*  247 */       term = System.getProperty("idc.term.type");
/*      */     }
/*  249 */     if (term == null)
/*      */     {
/*  251 */       term = getInstallValue("TerminalType", null);
/*      */     }
/*  253 */     if ((term != null) && (getInstallerTable("AnsiCompatibleTerminals", term) != null) && (this.m_promptUser instanceof ConsolePromptUser))
/*      */     {
/*  257 */       ConsolePromptUser promptUser = (ConsolePromptUser)this.m_promptUser;
/*      */ 
/*  259 */       promptUser.setSelectionStrings("\033[1m", "\033[0m");
/*      */     }
/*      */ 
/*  263 */     return 0;
/*      */   }
/*      */ 
/*      */   public void setPlatform(String platform)
/*      */   {
/*  268 */     Vector platformList = new IdcVector();
/*  269 */     platformList.addElement("*");
/*  270 */     if (platform == null)
/*      */     {
/*  272 */       platform = determineCurrentPlatformEx();
/*  273 */       if (platform != null)
/*      */       {
/*  275 */         Report.trace("install", "setting platform to \"" + platform + "\".", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  280 */     if (platform != null)
/*      */     {
/*  282 */       this.m_platform = platform;
/*  283 */       this.m_installerConfig.put("Platform", platform);
/*  284 */       this.m_installerConfig.put("platform", platform);
/*  285 */       Properties props = getInstallerTable("PlatformConfigTable", platform);
/*  286 */       String targetList = null;
/*  287 */       if (props != null)
/*      */       {
/*  289 */         String family = props.getProperty("Family");
/*  290 */         if ((family != null) && (family.length() > 0))
/*      */         {
/*  292 */           platformList.addElement(family);
/*      */         }
/*  294 */         targetList = getInstallValue("TargetPlatforms", props.getProperty("DefaultTargetPlatforms"));
/*      */       }
/*      */ 
/*  297 */       if (targetList == null)
/*      */       {
/*  299 */         targetList = platform;
/*      */       }
/*  301 */       Vector v = StringUtils.parseArray(targetList, ',', '^');
/*  302 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/*  304 */         platformList.addElement(v.elementAt(i));
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  309 */       Report.trace("install", "Platform not specified.", null);
/*      */     }
/*  311 */     this.m_myPlatforms = new String[platformList.size()];
/*  312 */     platformList.copyInto(this.m_myPlatforms);
/*  313 */     IdcStringBuilder msg = new IdcStringBuilder("installing for platforms ");
/*  314 */     for (int i = 0; i < this.m_myPlatforms.length; ++i)
/*      */     {
/*  316 */       if (i > 0)
/*      */       {
/*  318 */         msg.append(", ");
/*      */       }
/*  320 */       msg.append(this.m_myPlatforms[i]);
/*      */     }
/*  322 */     Report.trace("install", msg.toString(), null);
/*  323 */     msg.releaseBuffers();
/*      */   }
/*      */ 
/*      */   public String determineCurrentPlatform()
/*      */     throws ServiceException
/*      */   {
/*  329 */     Hashtable platforms = getInstallerTable("PlatformConfigTable");
/*  330 */     if (platforms == null)
/*      */     {
/*  332 */       throw new ServiceException("!csInstallerPlatformConfigTableNotFound");
/*      */     }
/*  334 */     return determineCurrentPlatformEx();
/*      */   }
/*      */ 
/*      */   public String determineCurrentPlatformEx()
/*      */   {
/*  341 */     Hashtable platforms = getInstallerTable("PlatformConfigTable");
/*  342 */     if (platforms == null)
/*      */     {
/*  344 */       Report.trace("install", "PlatformConfigTable missing.", null);
/*  345 */       return null;
/*      */     }
/*      */ 
/*  348 */     String osName = System.getProperty("os.name");
/*  349 */     String osFamily = osName;
/*  350 */     int index = osFamily.indexOf(" ");
/*  351 */     if (index > 0)
/*      */     {
/*  353 */       osFamily = osFamily.substring(0, index);
/*      */     }
/*  355 */     String arch = System.getProperty("os.arch");
/*  356 */     String dataModel = System.getProperty("sun.arch.data.model");
/*  357 */     String[] infos = { osName + "-" + arch, osFamily + "-" + arch, osName + dataModel, osFamily + dataModel, osName, osFamily };
/*      */ 
/*  370 */     if ((osName.indexOf("indows") >= 0) || (osName.indexOf("win") >= 0))
/*      */     {
/*  372 */       if ((arch != null) && (dataModel.indexOf("64") >= 0))
/*      */       {
/*  374 */         return "windows-amd64";
/*      */       }
/*  376 */       return "win32";
/*      */     }
/*      */ 
/*  380 */     for (int i = 0; i < infos.length; ++i)
/*      */     {
/*  382 */       String osInfo = infos[i];
/*  383 */       Enumeration en = platforms.keys();
/*  384 */       while (en.hasMoreElements())
/*      */       {
/*  386 */         String key = (String)en.nextElement();
/*  387 */         Properties values = getInstallerTable("PlatformConfigTable", key);
/*      */ 
/*  389 */         String tmp = values.getProperty("PlatformList");
/*  390 */         Vector list = StringUtils.parseArray(tmp, ',', '^');
/*  391 */         if (list.indexOf(osInfo) >= 0)
/*      */         {
/*  393 */           return key;
/*      */         }
/*      */       }
/*      */     }
/*  397 */     Report.trace("install", "Unable to compute platform for " + osName + "-" + arch + ".  Fix PlatformConfigTable.", null);
/*      */ 
/*  399 */     return null;
/*      */   }
/*      */ 
/*      */   public SysInstaller deriveInstaller(String idcDir)
/*      */     throws DataException
/*      */   {
/*  405 */     SysInstaller installer = new SysInstaller();
/*  406 */     Properties installerConfig = (Properties)this.m_installerConfig.clone();
/*      */ 
/*  408 */     installerConfig.put("IntradocDir", idcDir);
/*  409 */     installerConfig.put("TargetDir", idcDir);
/*  410 */     installer.init(this.m_binder.createShallowCopy(), installerConfig, this.m_overrideProps, this.m_installLog, this.m_progress, this.m_promptUser);
/*      */ 
/*  413 */     installer.m_isUpdate = this.m_isUpdate;
/*  414 */     return installer;
/*      */   }
/*      */ 
/*      */   public SysInstaller initServerConfig(String idcDir, Map<String, String> flags)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  420 */     throw new AssertionError("Legacy init method called.");
/*      */   }
/*      */ 
/*      */   public SysInstaller initServerConfig(String idcDir, boolean initServer)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  426 */     return initServerConfigEx(idcDir, false, initServer);
/*      */   }
/*      */ 
/*      */   public SysInstaller initServerConfigEx(String idcDir, boolean forceLoadStandardCSResources, boolean initServer)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  444 */     SharedObjects.init();
/*  445 */     Properties env = SharedObjects.getSafeEnvironment();
/*  446 */     String[] settings = { "SharedDir", "IdcHomeDir" };
/*      */ 
/*  449 */     for (String key : settings)
/*      */     {
/*  451 */       String value = getInstallValue(key, null);
/*  452 */       if (value == null)
/*      */         continue;
/*  454 */       env.put(key, value);
/*      */     }
/*      */ 
/*  457 */     env.put("IntradocDir", idcDir);
/*  458 */     IdcSystemConfig.initFileStoreObjects();
/*      */ 
/*  460 */     SysInstaller installer = deriveInstaller(idcDir);
/*  461 */     installer.loadConfig();
/*      */ 
/*  463 */     Enumeration en = installer.m_intradocConfig.keys();
/*  464 */     while (en.hasMoreElements())
/*      */     {
/*  466 */       Object key = en.nextElement();
/*  467 */       if (key instanceof String)
/*      */       {
/*  469 */         SharedObjects.putEnvironmentValue((String)key, installer.m_intradocConfig.getProperty((String)key));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  474 */     SharedLoader.initIdcName(env, 0);
/*  475 */     DirectoryLocator.m_keyDirMap = (String[][])null;
/*  476 */     ComponentLoader.reset();
/*      */ 
/*  478 */     if (initServer)
/*      */     {
/*  480 */       DirectoryLocator.buildRootDirectories();
/*  481 */       IdcSystemLoader.computeWebPathEnvironment();
/*  482 */       ComponentLoader.m_quiet = getInstallBool("ComponentLoaderQuiet", true);
/*  483 */       if (getInstallBool("LoadComponents", false))
/*      */       {
/*  485 */         IdcSystemLoader.initComponentData();
/*      */       }
/*      */       else
/*      */       {
/*  489 */         ComponentLoader.initDefaults();
/*      */       }
/*      */     }
/*      */ 
/*  493 */     return installer;
/*      */   }
/*      */ 
/*      */   public void setPromptInterface(PromptUser prompt)
/*      */   {
/*  498 */     this.m_promptUser = prompt;
/*      */   }
/*      */ 
/*      */   public Hashtable getInstallerTable(String tableName)
/*      */   {
/*  503 */     Hashtable table = (Hashtable)this.m_installerTables.get(tableName);
/*  504 */     return table;
/*      */   }
/*      */ 
/*      */   public Properties getInstallerTable(String tableName, String entryName)
/*      */   {
/*  509 */     Hashtable table = getInstallerTable(tableName);
/*  510 */     if (table == null)
/*      */     {
/*  512 */       Report.trace("install", "Request for " + entryName + " from unknown table " + tableName, null);
/*      */     }
/*      */     else
/*      */     {
/*  517 */       if (SystemUtils.m_verbose)
/*      */       {
/*  519 */         Report.debug("install", "getting " + entryName + " from " + tableName, null);
/*      */       }
/*  521 */       Properties props = (Properties)table.get(entryName);
/*  522 */       if (SystemUtils.m_verbose)
/*      */       {
/*  524 */         Report.debug("install", "got " + props, null);
/*      */       }
/*  526 */       return props;
/*      */     }
/*  528 */     return null;
/*      */   }
/*      */ 
/*      */   public String[][] getInstallerTableAsArray(String tableName)
/*      */   {
/*  534 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/*  535 */     if (drset == null)
/*      */     {
/*  537 */       return new String[0][0];
/*      */     }
/*      */ 
/*  540 */     int rows = drset.getNumRows();
/*  541 */     int columns = drset.getNumFields();
/*  542 */     String[][] table = new String[rows][];
/*  543 */     for (int i = 0; i < rows; ++i)
/*      */     {
/*  545 */       Vector v = drset.getRowValues(i);
/*  546 */       String[] row = new String[columns];
/*  547 */       v.copyInto(row);
/*  548 */       table[i] = row;
/*      */     }
/*      */ 
/*  551 */     return table;
/*      */   }
/*      */ 
/*      */   public String getInstallerTableValue(String tableName, String entryName, String columnName)
/*      */   {
/*  557 */     Properties props = getInstallerTable(tableName, entryName);
/*  558 */     if (props == null)
/*      */     {
/*  560 */       return null;
/*      */     }
/*  562 */     String data = props.getProperty(columnName);
/*  563 */     return data;
/*      */   }
/*      */ 
/*      */   public String[] getInstallerTableValueAsArray(String tableName, String entryName, String columnName)
/*      */   {
/*  569 */     String data = getInstallerTableValue(tableName, entryName, columnName);
/*  570 */     Vector values = StringUtils.parseArray(data, ',', '^');
/*  571 */     String[] valuesString = new String[values.size()];
/*  572 */     values.copyInto(valuesString);
/*  573 */     return valuesString;
/*      */   }
/*      */ 
/*      */   protected void loadInstallerTables()
/*      */     throws DataException
/*      */   {
/*  579 */     DataResultSet installerTables = (DataResultSet)this.m_binder.getResultSet("InstallerTables");
/*      */ 
/*  581 */     if (installerTables == null)
/*      */     {
/*  583 */       throw new DataException("!$Internal error: InstallerTables DataResultSet missing.");
/*      */     }
/*      */ 
/*  587 */     installerTables = installerTables.shallowClone();
/*  588 */     installerTables.first();
/*  589 */     while (installerTables.isRowPresent())
/*      */     {
/*  592 */       Properties tableProps = installerTables.getCurrentRowProps();
/*  593 */       String tableName = tableProps.getProperty("TableName");
/*  594 */       String keyNameList = tableProps.getProperty("KeyName");
/*  595 */       if (SystemUtils.m_verbose)
/*      */       {
/*  597 */         Report.debug("install", "registering table " + tableName + " with key definition " + keyNameList + ".", null);
/*      */       }
/*      */ 
/*  601 */       Vector v = StringUtils.parseArray(keyNameList, ',', ',');
/*  602 */       if (v.size() == 0)
/*      */       {
/*  604 */         throw new DataException("!$Internal error.");
/*      */       }
/*  606 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/*      */ 
/*  608 */       if (drset == null)
/*      */       {
/*  610 */         Report.trace("install", "declared table " + tableName + " not found.", null);
/*      */       }
/*      */       else
/*      */       {
/*  614 */         Hashtable table = new Hashtable();
/*  615 */         IdcStringBuilder key = new IdcStringBuilder();
/*  616 */         key.m_disableToStringReleaseBuffers = true;
/*  617 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  619 */           Properties props = drset.getCurrentRowProps();
/*  620 */           String tmp = props.getProperty((String)v.elementAt(0));
/*  621 */           key.append(tmp);
/*  622 */           for (int i = 1; i < v.size(); ++i)
/*      */           {
/*  624 */             tmp = props.getProperty((String)v.elementAt(i));
/*  625 */             key.append(",");
/*  626 */             key.append(tmp);
/*      */           }
/*  628 */           table.put(key.toString(), props);
/*  629 */           key.setLength(0);
/*      */         }
/*  631 */         key.releaseBuffers();
/*  632 */         Object oldTable = this.m_installerTables.get(tableName);
/*  633 */         if (oldTable != null)
/*      */         {
/*  635 */           Report.trace("install", "The table " + tableName + " was superceeded.", null);
/*      */         }
/*      */ 
/*  638 */         this.m_installerTables.put(tableName, table);
/*      */       }
/*  590 */       installerTables.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   public String getConfigValue(String key)
/*      */   {
/*  644 */     return this.m_intradocConfig.getProperty(key);
/*      */   }
/*      */ 
/*      */   public boolean getConfigBool(String key)
/*      */   {
/*  649 */     String value = getConfigValue(key);
/*  650 */     boolean rc = StringUtils.convertToBool(value, false);
/*  651 */     return rc;
/*      */   }
/*      */ 
/*      */   public long parseFlags(String flagString, Properties values)
/*      */   {
/*  656 */     if (values == null)
/*      */     {
/*  658 */       values = new Properties();
/*      */     }
/*      */ 
/*  661 */     long flags = 0L;
/*      */     try
/*      */     {
/*  666 */       if ((flagString.startsWith("0x")) || (flagString.startsWith("0X")))
/*      */       {
/*  668 */         flags = Integer.parseInt(flagString.substring(2), 16);
/*      */       }
/*  670 */       else if (!flagString.equals(""))
/*      */       {
/*  672 */         flags = Integer.parseInt(flagString);
/*      */       }
/*      */     }
/*      */     catch (NumberFormatException ignore)
/*      */     {
/*  677 */       Vector v = parseArray(flagString, '|', '^');
/*  678 */       if (v.size() > 0)
/*      */       {
/*  680 */         int l2 = v.size();
/*  681 */         for (int j = 0; j < l2; ++j)
/*      */         {
/*  683 */           boolean matches = false;
/*  684 */           for (int k = 0; k < FLAG_MAP.length; ++k)
/*      */           {
/*  686 */             String flag = (String)FLAG_MAP[k][0];
/*  687 */             String element = (String)v.elementAt(j);
/*  688 */             if (flag.endsWith(":"))
/*      */             {
/*  690 */               matches = element.startsWith(flag);
/*  691 */               if (matches)
/*      */               {
/*  693 */                 int index = element.indexOf(":");
/*  694 */                 values.put(element.substring(0, index), element.substring(index + 1));
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/*  700 */               matches = element.equals(flag);
/*      */             }
/*      */ 
/*  703 */             if (!matches)
/*      */               continue;
/*  705 */             flags |= ((Integer)FLAG_MAP[k][1]).intValue();
/*  706 */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  713 */     return flags;
/*      */   }
/*      */ 
/*      */   public boolean isOlderVersion(String version)
/*      */   {
/*  718 */     if (!this.m_isUpdate)
/*      */     {
/*  720 */       return false;
/*      */     }
/*  722 */     return SystemUtils.isOlderVersion(this.m_installedVersion, version);
/*      */   }
/*      */ 
/*      */   public boolean getInstallBool(String key, boolean defaultValue)
/*      */   {
/*  727 */     String value = getInstallValue(key, null);
/*  728 */     boolean rc = StringUtils.convertToBool(value, defaultValue);
/*  729 */     return rc;
/*      */   }
/*      */ 
/*      */   public boolean getPropertyBool(Properties props, String key, boolean defaultValue)
/*      */   {
/*  735 */     String value = props.getProperty(key);
/*  736 */     return StringUtils.convertToBool(value, defaultValue);
/*      */   }
/*      */ 
/*      */   public String getInstallValue(String key, String defaultValue)
/*      */   {
/*  741 */     Properties entryInfo = getInstallerTable("ConfigEntries", key);
/*  742 */     String captionList = key;
/*  743 */     String caption = key;
/*  744 */     long flags = 0L;
/*      */ 
/*  746 */     String value = this.m_overrideProps.getProperty(key);
/*  747 */     if (value != null)
/*      */     {
/*  749 */       return value;
/*      */     }
/*  751 */     value = this.m_intradocConfig.getProperty(key);
/*  752 */     if ((value == null) && (entryInfo != null))
/*      */     {
/*  754 */       String configEntry = entryInfo.getProperty("ConfigEntry");
/*  755 */       if (configEntry != null)
/*      */       {
/*  757 */         value = this.m_intradocConfig.getProperty(configEntry);
/*      */       }
/*      */     }
/*  760 */     if (value != null)
/*      */     {
/*  762 */       return value;
/*      */     }
/*      */ 
/*  765 */     Properties flagValues = new Properties();
/*  766 */     if (entryInfo != null)
/*      */     {
/*  768 */       captionList = key + "," + entryInfo.getProperty("CaptionList");
/*  769 */       String tmp = entryInfo.getProperty("Flags");
/*  770 */       flags = parseFlags(tmp, flagValues);
/*      */     }
/*      */     else
/*      */     {
/*  774 */       captionList = key;
/*      */     }
/*      */ 
/*  777 */     Vector v = parseArray(captionList, ',', '^');
/*  778 */     for (int j = v.size() - 1; (value == null) && (j >= 0); --j)
/*      */     {
/*  780 */       caption = (String)v.elementAt(j);
/*  781 */       value = this.m_installerConfig.getProperty(caption);
/*      */     }
/*      */ 
/*  784 */     if ((value == null) && (this.m_utils != null))
/*      */     {
/*  786 */       value = this.m_utils.getEnv("Idc" + key);
/*      */     }
/*  788 */     if ((value == null) && (entryInfo != null))
/*      */     {
/*  790 */       value = entryInfo.getProperty("DefaultValue");
/*      */     }
/*  792 */     if ((value == null) || (value.equals("null")))
/*      */     {
/*  794 */       value = defaultValue;
/*      */     }
/*  796 */     value = substituteVariables(value, this.m_installerConfig);
/*      */ 
/*  798 */     value = handleFlags(value, flags);
/*      */ 
/*  800 */     if (((flags & 0x44) == 68L) && (value != null))
/*      */     {
/*  804 */       for (int j = 0; j < DirectoryLocator.DEFAULTS.length; ++j)
/*      */       {
/*  806 */         if ((!DirectoryLocator.DEFAULTS[j][0].equals(key)) || (!value.equals(this.m_idcDir + DirectoryLocator.DEFAULTS[j][1]))) {
/*      */           continue;
/*      */         }
/*  809 */         value = null;
/*  810 */         break;
/*      */       }
/*      */ 
/*      */     }
/*  816 */     else if (entryInfo != null)
/*      */     {
/*  818 */       String tmp = entryInfo.getProperty("ValueMap");
/*  819 */       Vector mapData = parseArray(tmp, ',', '^');
/*  820 */       int size = mapData.size();
/*  821 */       if (size % 2 == 0)
/*      */       {
/*  823 */         String newValue = value;
/*  824 */         for (int j = 0; j < size / 2; ++j)
/*      */         {
/*  826 */           String old = (String)mapData.elementAt(2 * j);
/*  827 */           if (!old.equals(value))
/*      */             continue;
/*  829 */           newValue = (String)mapData.elementAt(2 * j + 1);
/*  830 */           break;
/*      */         }
/*      */ 
/*  833 */         value = newValue;
/*      */       }
/*  837 */       else if ((tmp != null) && (tmp.length() > 0))
/*      */       {
/*  839 */         String msg = LocaleUtils.encodeMessage("csInstallerIllegalValueMap", null, tmp, key);
/*      */ 
/*  841 */         Report.trace("install", LocaleResources.localizeMessage(msg, null), null);
/*      */         try
/*      */         {
/*  844 */           this.m_installLog.warning(msg);
/*      */         }
/*      */         catch (ServiceException ignore)
/*      */         {
/*  848 */           Report.trace("install", null, ignore);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  855 */     return value;
/*      */   }
/*      */ 
/*      */   public int getInstallerDefaultValue(String keyName, IdcStringBuilder defaultValue)
/*      */   {
/*  860 */     int flags = 0;
/*  861 */     String value = null;
/*      */ 
/*  864 */     value = this.m_installerConfig.getProperty(keyName);
/*  865 */     if ((value == null) && (this.m_utils != null))
/*      */     {
/*  867 */       value = this.m_utils.getEnv("Idc" + keyName);
/*      */     }
/*  869 */     if (value != null)
/*      */     {
/*  871 */       flags |= 1;
/*  872 */       defaultValue.append(value);
/*      */     }
/*  874 */     return flags;
/*      */   }
/*      */ 
/*      */   public String handleFlags(String value, long flags)
/*      */   {
/*  879 */     if (((flags & 0x4) == 4L) && (value != null))
/*      */     {
/*  881 */       value = FileUtils.directorySlashes(value);
/*      */     }
/*  883 */     else if (((flags & 0x2) == 2L) && (value != null))
/*      */     {
/*  885 */       value = FileUtils.fileSlashes(value);
/*      */     }
/*  887 */     else if (((flags & 0x8) == 8L) && (value != null))
/*      */     {
/*  889 */       value = FileUtils.directorySlashes(value);
/*  890 */       if (!value.startsWith("/"))
/*      */       {
/*  892 */         value = "/" + value;
/*      */       }
/*      */     }
/*  895 */     return value;
/*      */   }
/*      */ 
/*      */   public void setJdbcDriverAndClasspath(String jdbcDriver, Properties databaseProps)
/*      */     throws ServiceException, DataException
/*      */   {
/*  903 */     if (jdbcDriver == null)
/*      */     {
/*  905 */       jdbcDriver = this.m_installerConfig.getProperty("JdbcDriver");
/*      */     }
/*  907 */     if ((jdbcDriver == null) && (databaseProps != null))
/*      */     {
/*  909 */       jdbcDriver = databaseProps.getProperty("DefaultJdbcDriver");
/*      */     }
/*  911 */     if (jdbcDriver == null)
/*      */     {
/*  913 */       Report.trace("install", "unable to compute jdbc driver", null);
/*      */     }
/*      */     else
/*      */     {
/*  917 */       Properties jdbcDriverProps = getInstallerTable("DatabaseDriverTable", jdbcDriver);
/*      */ 
/*  919 */       if (jdbcDriverProps == null)
/*      */       {
/*  921 */         Report.trace("install", "no driver table for JDBC driver " + jdbcDriver, null);
/*      */       }
/*      */       else
/*      */       {
/*      */         String installerClasspath;
/*  926 */         if (getInstallBool("IsHomeOnly", false))
/*      */         {
/*  928 */           String installerClasspath = jdbcDriverProps.getProperty("ServerClasspath");
/*  929 */           int index = installerClasspath.indexOf("$SHAREDDIR");
/*  930 */           if (index >= 0)
/*      */           {
/*  932 */             String idcHome = getInstallValue("IdcHomeDir", null);
/*  933 */             installerClasspath = idcHome + "/shared" + installerClasspath.substring("$SHAREDDIR".length());
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  939 */           installerClasspath = jdbcDriverProps.getProperty("InstallerClasspath");
/*      */         }
/*  941 */         if ((installerClasspath != null) && (installerClasspath.length() > 0) && (getInstallValue("InstallerJdbcClasspath", null) == null))
/*      */         {
/*  945 */           installerClasspath = computeDestinationEx(installerClasspath, false);
/*      */ 
/*  947 */           this.m_installerConfig.put("InstallerJdbcClasspath", installerClasspath);
/*      */         }
/*      */ 
/*  950 */         String serverClasspath = jdbcDriverProps.getProperty("ServerClasspath");
/*      */ 
/*  952 */         if ((serverClasspath == null) || (serverClasspath.length() <= 0) || (getInstallValue("JdbcClasspath", null) != null)) {
/*      */           return;
/*      */         }
/*      */ 
/*  956 */         this.m_installerConfig.put("JdbcClasspath", serverClasspath);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public int doInstall(Vector results)
/*      */   {
/*  964 */     int rc = 0;
/*      */     try
/*      */     {
/*  967 */       String cfg = getInstallValue("InstallConfiguration", null);
/*  968 */       if (cfg == null)
/*      */       {
/*  970 */         String msg = LocaleUtils.encodeMessage("csInstallerConfigNotSpecified", null);
/*      */ 
/*  972 */         reportError(results, msg);
/*  973 */         return 1;
/*      */       }
/*      */ 
/*  976 */       Properties props = getInstallerTable("InstallConfigurations", cfg);
/*  977 */       if (props == null)
/*      */       {
/*  979 */         String msg = LocaleUtils.encodeMessage("csInstallerConfigUnknown", null, cfg);
/*      */ 
/*  981 */         reportError(results, msg);
/*  982 */         return 1;
/*      */       }
/*  984 */       Vector overrideProperties = parseArrayTrim(props, "OverrideProperties");
/*      */ 
/*  986 */       for (int i = 0; i < overrideProperties.size(); ++i)
/*      */       {
/*  988 */         String tmp = (String)overrideProperties.elementAt(i);
/*  989 */         int index = tmp.indexOf("=");
/*  990 */         if (index <= 0)
/*      */           continue;
/*  992 */         String key = tmp.substring(0, index);
/*  993 */         String value = tmp.substring(index + 1);
/*  994 */         this.m_installerConfig.put(key, value);
/*      */       }
/*      */ 
/*  998 */       boolean runChecks = getInstallBool("RunChecks", false);
/*  999 */       boolean runInstall = getInstallBool("RunInstall", true);
/* 1000 */       String installType = getInstallValue("InstallType", null);
/* 1001 */       if ((installType == null) || (installType.length() == 0))
/*      */       {
/* 1003 */         installType = "null";
/* 1004 */         this.m_installerConfig.put("InstallType", "null");
/*      */       }
/* 1006 */       if (installType.equalsIgnoreCase("update"))
/*      */       {
/* 1008 */         this.m_isUpdate = true;
/*      */       }
/*      */ 
/* 1011 */       Vector list = parseArrayTrim(props, "ValidInstallTypes");
/* 1012 */       if ((list.indexOf(installType) == -1) && (list.indexOf("*") == -1))
/*      */       {
/* 1014 */         String msg = LocaleUtils.encodeMessage("csInstallerConfigIllegal", null, cfg, installType);
/*      */ 
/* 1016 */         reportError(results, msg);
/* 1017 */         return 1;
/*      */       }
/*      */ 
/* 1020 */       for (int i = 0; i < 2; ++i)
/*      */       {
/* 1022 */         if (runChecks)
/*      */         {
/* 1024 */           Vector checkSections = parseArrayTrim(props, "CheckSectionList");
/*      */ 
/* 1026 */           if (i == 0)
/*      */           {
/* 1028 */             addSectionWork(checkSections, installType);
/*      */           }
/*      */           else
/*      */           {
/* 1032 */             int tmprc = doInstallSections(checkSections, installType, results);
/*      */ 
/* 1034 */             if (tmprc != 0)
/*      */             {
/* 1036 */               rc = tmprc;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1041 */         if (!runInstall)
/*      */           continue;
/* 1043 */         Vector installSections = parseArrayTrim(props, "InstallSectionList");
/*      */ 
/* 1045 */         if (SystemUtils.isActiveTrace("install"))
/*      */         {
/* 1047 */           IdcStringBuilder builder = new IdcStringBuilder("installing sections ");
/*      */ 
/* 1049 */           for (int j = 0; j < installSections.size(); ++j)
/*      */           {
/* 1051 */             if (j > 0)
/*      */             {
/* 1053 */               builder.append(',');
/*      */             }
/* 1055 */             builder.append((String)installSections.get(j));
/*      */           }
/* 1057 */           Report.trace("install", builder.toString(), null);
/*      */         }
/* 1059 */         if (i == 0)
/*      */         {
/* 1061 */           addSectionWork(installSections, installType);
/*      */         }
/*      */         else
/*      */         {
/* 1065 */           String lockFile = this.m_targetDir + "/install/lock.txt";
/* 1066 */           FileUtils.touchFile(lockFile);
/* 1067 */           doInstallSections(installSections, installType, results);
/*      */ 
/* 1069 */           if (results.size() != 0)
/*      */             continue;
/* 1071 */           FileUtils.deleteFile(lockFile);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1079 */       Report.trace("install", null, e);
/* 1080 */       results.addElement(e);
/* 1081 */       return -1;
/*      */     }
/*      */ 
/* 1084 */     if (this.m_retentiveReturnCode)
/*      */     {
/* 1086 */       return rc;
/*      */     }
/* 1088 */     return 0;
/*      */   }
/*      */ 
/*      */   public String checkInstallSection(String sectionName)
/*      */     throws ServiceException
/*      */   {
/* 1094 */     int i = sectionName.indexOf("?");
/* 1095 */     if (i >= 0)
/*      */     {
/* 1097 */       String criteria = sectionName.substring(0, i);
/* 1098 */       sectionName = sectionName.substring(i + 1);
/* 1099 */       String value = null;
/* 1100 */       if (criteria.startsWith("<$"))
/*      */       {
/* 1102 */         value = evaluateScript(criteria);
/*      */       }
/*      */       else
/*      */       {
/* 1106 */         value = substituteVariables(criteria, null);
/*      */       }
/* 1108 */       if (!StringUtils.convertToBool(value, true))
/*      */       {
/* 1110 */         sectionName = null;
/*      */       }
/*      */     }
/* 1113 */     return sectionName;
/*      */   }
/*      */ 
/*      */   public void addSectionWork(Vector sections, String installType)
/*      */     throws ServiceException
/*      */   {
/* 1119 */     String[] suffixes = { "always", installType };
/* 1120 */     for (int i = 0; i < sections.size(); ++i)
/*      */     {
/* 1122 */       for (int j = 0; j < suffixes.length; ++j)
/*      */       {
/* 1124 */         String section = (String)sections.elementAt(i);
/* 1125 */         section = checkInstallSection(section);
/* 1126 */         if (section == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1131 */         for (int k = 0; k < this.m_myPlatforms.length; ++k)
/*      */         {
/* 1133 */           String sectionId = section + "," + suffixes[j] + "," + this.m_myPlatforms[k];
/*      */ 
/* 1135 */           Properties props = getInstallerTable("InstallInfo", sectionId);
/* 1136 */           String weight = null;
/*      */ 
/* 1138 */           if (props != null)
/*      */           {
/* 1140 */             weight = props.getProperty("Weight");
/*      */           }
/* 1142 */           int theWeight = NumberUtils.parseInteger(weight, 0);
/*      */ 
/* 1146 */           this.m_workStatus[1] += theWeight * 3;
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public int doInstallSections(Vector sections, String installType, Vector results)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1155 */     for (int i = 0; i < sections.size(); ++i)
/*      */     {
/* 1157 */       String section = (String)sections.elementAt(i);
/* 1158 */       String finalSection = checkInstallSection(section);
/* 1159 */       if (finalSection == null)
/*      */       {
/* 1161 */         Report.trace("install", "skipping " + section, null);
/*      */       }
/*      */       else {
/* 1164 */         Report.trace("install", "installing " + section, null);
/* 1165 */         int rc = installSection(finalSection);
/* 1166 */         if (rc != 0)
/*      */         {
/* 1168 */           return rc;
/*      */         }
/*      */       }
/*      */     }
/* 1171 */     return 0;
/*      */   }
/*      */ 
/*      */   public Vector parseArrayTrim(Properties props, String key)
/*      */   {
/* 1176 */     if (props == null)
/*      */     {
/* 1178 */       Report.trace("install", "  unable to find " + key + " because props is null", null);
/*      */ 
/* 1180 */       return new IdcVector();
/*      */     }
/* 1182 */     String str = props.getProperty(key);
/* 1183 */     if ((str == null) || (str.trim().equals("null")))
/*      */     {
/* 1185 */       return new IdcVector();
/*      */     }
/* 1187 */     Vector v = StringUtils.parseArray(str, '\n', '\n');
/* 1188 */     int size = v.size();
/* 1189 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1191 */       str = (String)v.elementAt(i);
/* 1192 */       str = str.trim();
/* 1193 */       if (str.length() > 0)
/*      */       {
/* 1195 */         v.setElementAt(str, i);
/*      */       }
/*      */       else
/*      */       {
/* 1199 */         v.remove(i);
/* 1200 */         --i;
/* 1201 */         --size;
/*      */       }
/*      */     }
/* 1204 */     return v;
/*      */   }
/*      */ 
/*      */   protected void reportError(Vector results, String msg)
/*      */     throws ServiceException
/*      */   {
/* 1210 */     results.addElement(msg);
/* 1211 */     Exception e = new StackTrace("Reporting error \"" + msg + "\".");
/* 1212 */     Report.trace("install", null, e);
/* 1213 */     this.m_installLog.error(msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public int installSection(String section, String installType, Vector results)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1222 */     SystemUtils.reportDeprecatedUsage("using deprecated method installSection()");
/*      */ 
/* 1224 */     return installSection(section);
/*      */   }
/*      */ 
/*      */   public int installSection(String section)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1230 */     loadConfig();
/*      */ 
/* 1232 */     String installType = getInstallValue("InstallType", null);
/* 1233 */     String[] suffixes = { "always", installType };
/* 1234 */     int rc = 0;
/* 1235 */     for (int i = 0; i < suffixes.length; ++i)
/*      */     {
/* 1237 */       for (int j = 0; j < this.m_myPlatforms.length; ++j)
/*      */       {
/* 1239 */         String sectionId = section + "," + suffixes[i] + "," + this.m_myPlatforms[j];
/*      */ 
/* 1241 */         Report.trace("install", "looking for section matching " + sectionId, null);
/*      */ 
/* 1243 */         Properties props = getInstallerTable("InstallInfo", sectionId);
/* 1244 */         if (props != null)
/*      */         {
/* 1246 */           Report.trace("install", "installing " + sectionId, null);
/* 1247 */           String cfgEntriesTmp = props.getProperty("ConfigEntryList");
/* 1248 */           Vector cfgEntries = parseArray(cfgEntriesTmp, ',', '^');
/* 1249 */           int tmprc = installSection(section, this.m_myPlatforms[j], suffixes[i], props, cfgEntries);
/*      */ 
/* 1251 */           if (tmprc != 0)
/*      */           {
/* 1253 */             rc = tmprc;
/*      */           }
/* 1255 */           loadConfig();
/*      */         }
/*      */         else
/*      */         {
/* 1259 */           Report.trace("install", "skipping " + sectionId, null);
/*      */         }
/*      */       }
/*      */     }
/* 1263 */     return rc;
/*      */   }
/*      */ 
/*      */   public void loadConfig() throws ServiceException
/*      */   {
/* 1268 */     if (getInstallBool("NoCfgActivity", false))
/*      */     {
/* 1270 */       return;
/*      */     }
/* 1272 */     loadProductVersionBeingInstalled();
/* 1273 */     String idcCfgPath = this.m_idcDir + "bin/intradoc.cfg";
/*      */ 
/* 1275 */     loadConfigFromFile(idcCfgPath);
/* 1276 */     this.m_intradocConfig.put("IntradocDir", this.m_idcDir);
/* 1277 */     loadConfigFromFile(this.m_idcDir + "config/config.cfg");
/* 1278 */     loadConfigFromFile(idcCfgPath);
/*      */ 
/* 1282 */     String[] extras = { "ComponentDir", "IsProxiedServer", "SystemComponentDir" };
/* 1283 */     for (int i = 0; i < extras.length; ++i)
/*      */     {
/* 1285 */       String key = extras[i];
/* 1286 */       String val = this.m_intradocConfig.getProperty(key);
/* 1287 */       if ((val == null) || (val.length() <= 0))
/*      */         continue;
/* 1289 */       this.m_installerConfig.put(key, val);
/*      */     }
/*      */ 
/* 1293 */     boolean isRefinery = getInstallBool("IsRefinery", false);
/* 1294 */     if (isRefinery)
/*      */     {
/* 1297 */       SharedObjects.putEnvironmentValue("DefaultApplicationName", "ibr");
/* 1298 */       LocaleResources.m_defaultApp = "ibr";
/*      */ 
/* 1300 */       this.m_installerConfig.put("SkipDatabase", "true");
/* 1301 */       if ((this.m_installedVersion != null) && (this.m_installedVersion.length() != 0))
/*      */         return;
/* 1303 */       if ((this.m_utils != null) && (this.m_utils.isWindowsRegistrySupported()))
/*      */       {
/* 1305 */         this.m_installedVersion = this.m_utils.getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Content Server\\Refinery_Version");
/*      */       }
/*      */ 
/* 1310 */       if ((this.m_installedVersion == null) || (this.m_installedVersion.length() == 0))
/*      */       {
/* 1312 */         Properties p = new Properties();
/* 1313 */         this.m_installedVersion = null;
/* 1314 */         String versionCfg = this.m_targetDir + "install/version.cfg";
/* 1315 */         if (FileUtils.checkFile(versionCfg, true, true) == 0)
/*      */         {
/*      */           try
/*      */           {
/* 1319 */             FileUtils.loadProperties(p, versionCfg);
/* 1320 */             this.m_installedVersion = p.getProperty("version");
/*      */           }
/*      */           catch (IOException ignore)
/*      */           {
/* 1324 */             Report.trace("install", null, ignore);
/*      */           }
/*      */         }
/*      */       }
/*      */       while (true) {
/* 1328 */         if ((this.m_installedVersion == null) || (this.m_installedVersion.length() <= 0) || 
/* 1331 */           (this.m_installedVersion.length() == 7)) {
/*      */           return;
/*      */         }
/* 1334 */         this.m_installedVersion += ".0";
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1346 */     if (this.m_installedVersion.length() > 0)
/*      */     {
/* 1348 */       return;
/*      */     }
/*      */ 
/* 1351 */     boolean found = false;
/*      */     try
/*      */     {
/* 1354 */       BufferedReader r = FileUtils.openDataReader(this.m_idcDir + "install", "version.txt", "Cp1252");
/* 1355 */       this.m_installedVersion = r.readLine().trim();
/* 1356 */       found = true;
/* 1357 */       r.close();
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1361 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1363 */         Report.debug("install", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/* 1367 */     if (!found)
/*      */     {
/*      */       try
/*      */       {
/* 1372 */         String userDir = computeDestination("data/users");
/* 1373 */         BufferedReader r = FileUtils.openDataReader(userDir, "SecurityInfo.hda", "Cp1252");
/* 1374 */         String line = r.readLine().trim();
/* 1375 */         int i = line.indexOf("version=");
/* 1376 */         if (i >= 0)
/*      */         {
/* 1378 */           line = line.substring(i + "version=".length());
/* 1379 */           if (line.startsWith("\""))
/*      */           {
/* 1381 */             line = line.substring(1);
/* 1382 */             i = line.indexOf("\"");
/* 1383 */             if (i > 0)
/*      */             {
/* 1385 */               line = line.substring(0, i);
/*      */             }
/*      */           }
/* 1388 */           i = line.indexOf(" ");
/* 1389 */           if (i > 0)
/*      */           {
/* 1391 */             found = true;
/* 1392 */             this.m_installedVersion = line.substring(0, i);
/*      */           }
/*      */         }
/* 1395 */         r.close();
/*      */       }
/*      */       catch (IOException ignore)
/*      */       {
/* 1399 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1401 */           Report.debug("install", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1406 */     if (found)
/*      */       return;
/* 1408 */     String dsdDir = computeDestinationEx("data/datastoredesign", false);
/* 1409 */     if (FileUtils.checkFile(dsdDir, false, false) == 0)
/*      */     {
/* 1411 */       this.m_installedVersion = "4";
/*      */     }
/*      */     else
/*      */     {
/* 1415 */       Report.trace("install", "no data/datastoredesign dir, assuming version 3", null);
/*      */ 
/* 1417 */       this.m_installedVersion = "3";
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadProductVersionBeingInstalled()
/*      */   {
/* 1425 */     String verInstalled = this.m_installerConfig.getProperty("ProductVersionToBeInstalled");
/* 1426 */     if ((verInstalled != null) && (verInstalled.length() != 0)) {
/*      */       return;
/*      */     }
/* 1429 */     boolean isRefinery = getInstallBool("IsRefinery", false);
/* 1430 */     if (isRefinery)
/*      */     {
/*      */       try
/*      */       {
/* 1434 */         String refineryVersionClass = "docrefinery.data.RefineryVersionInfo";
/* 1435 */         Class c = Class.forName(refineryVersionClass);
/* 1436 */         Method verMethod = c.getMethod("getRefineryVersion", (Class[])null);
/* 1437 */         verInstalled = (String)verMethod.invoke(null, (Object[])null);
/*      */       }
/*      */       catch (Exception cnfe)
/*      */       {
/* 1441 */         Report.trace("install", "The version of the refinery being installed is unknown", cnfe);
/* 1442 */         verInstalled = "unknown";
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1447 */       verInstalled = VersionInfo.getProductVersionInfo();
/*      */     }
/* 1449 */     this.m_installerConfig.put("ProductVersionToBeInstalled", verInstalled);
/*      */   }
/*      */ 
/*      */   public void loadConfigFromFile(String fileName)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1458 */       File file = new File(fileName);
/* 1459 */       Report.trace("install", "loading file " + fileName, null);
/* 1460 */       if (file.exists())
/*      */       {
/* 1462 */         FileUtils.loadProperties(this.m_intradocConfig, fileName);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1467 */       String msg = LocaleUtils.encodeMessage("syUnableToReadFile", e.getMessage(), fileName);
/*      */ 
/* 1469 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String verifyDirectoryWithException(String path, int depth, String throwMsg)
/*      */     throws ServiceException
/*      */   {
/* 1476 */     String msg = verifyDirectory(path, depth);
/* 1477 */     if (msg != null)
/*      */     {
/* 1479 */       if (throwMsg != null)
/*      */       {
/* 1481 */         msg = LocaleUtils.appendMessage(msg, throwMsg);
/*      */       }
/* 1483 */       throw new ServiceException(msg);
/*      */     }
/* 1485 */     return msg;
/*      */   }
/*      */ 
/*      */   public String verifyDirectory(String path, int depth)
/*      */   {
/* 1490 */     String msg = null;
/*      */     try
/*      */     {
/* 1493 */       path = computeDestinationEx(path, false);
/* 1494 */       FileUtils.checkOrCreateDirectory(path, depth);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1498 */       msg = e.getMessage();
/* 1499 */       if (msg == null)
/*      */       {
/* 1501 */         msg = "Null";
/*      */       }
/*      */     }
/* 1504 */     return null;
/*      */   }
/*      */ 
/*      */   public void reportProgress(int type, String msg, float amtDone, float total)
/*      */   {
/* 1509 */     if (total == 0.0F)
/*      */     {
/* 1511 */       total = amtDone;
/* 1512 */       amtDone = 0.0F;
/*      */     }
/* 1514 */     if (amtDone > total)
/*      */     {
/* 1516 */       amtDone = total;
/*      */     }
/* 1518 */     amtDone = this.m_workStatus[0] + amtDone / total * this.m_workStatus[2];
/* 1519 */     total = this.m_workStatus[1];
/* 1520 */     this.m_progress.reportProgress(type, msg, amtDone, total);
/*      */   }
/*      */ 
/*      */   protected int installSection(String section, String platform, String type, Properties sectionProps, Vector cfgEntries)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1527 */     int rc = 0;
/* 1528 */     int tmprc = 0;
/* 1529 */     String weightTmp = sectionProps.getProperty("Weight");
/* 1530 */     int weight = NumberUtils.parseInteger(weightTmp, 0);
/* 1531 */     this.m_workStatus[2] = weight;
/*      */ 
/* 1533 */     Report.trace("install", "Installing section " + section + "," + type + "," + platform + " progress is " + this.m_workStatus[0] + "/" + this.m_workStatus[1] + " weight is " + weight, null);
/*      */ 
/* 1539 */     Vector srcFileList = parseArrayTrim(sectionProps, "FilePath");
/* 1540 */     boolean doBackup = StringUtils.convertToBool(sectionProps.getProperty("Backup"), false);
/* 1541 */     if (getInstallValue("InstallConfiguration", "").equals("Template"))
/*      */     {
/* 1543 */       doBackup = false;
/* 1544 */       FileUtils.m_neverLock = true;
/*      */     }
/*      */ 
/* 1547 */     if (doBackup)
/*      */     {
/* 1549 */       String msg = LocaleUtils.encodeMessage("csInstallerUnableToCreateBackupDirectory", null, this.m_idcDir + "backup");
/*      */ 
/* 1552 */       verifyDirectoryWithException(this.m_idcDir + "backup", 1, msg);
/*      */     }
/*      */ 
/* 1555 */     int size = srcFileList.size();
/* 1556 */     boolean didExpand = false;
/* 1557 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1559 */       String srcFile = (String)srcFileList.elementAt(i);
/* 1560 */       if ((srcFile == null) || (srcFile.length() == 0))
/*      */       {
/* 1562 */         Report.trace("install", "Section " + section + "," + type + "," + platform + " has extra white space in the 'FilePath' field", null);
/*      */ 
/* 1565 */         rc = 1;
/*      */       }
/*      */       else {
/* 1568 */         boolean allowMissing = false;
/* 1569 */         if (srcFile.startsWith("?"))
/*      */         {
/* 1571 */           srcFile = srcFile.substring(1);
/* 1572 */           allowMissing = true;
/*      */         }
/* 1574 */         if ((srcFile.startsWith("..")) || (!srcFile.startsWith("$")))
/*      */         {
/* 1577 */           srcFile = computeDestinationEx(this.m_srcDir + srcFile, false);
/*      */         }
/* 1579 */         if ((allowMissing) && (FileUtils.checkFile(srcFile, true, false) != 0))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1584 */         expandSectionZip(srcFile, doBackup, section, null);
/* 1585 */         didExpand = true;
/*      */       }
/*      */     }
/* 1587 */     if (didExpand)
/*      */     {
/* 1589 */       this.m_workStatus[0] += this.m_workStatus[2];
/*      */     }
/*      */     else
/*      */     {
/* 1595 */       this.m_workStatus[1] -= this.m_workStatus[2];
/*      */     }
/*      */ 
/* 1600 */     if ((cfgEntries != null) && (cfgEntries.size() > 0))
/*      */     {
/* 1602 */       updateConfigEntries(cfgEntries);
/*      */     }
/*      */ 
/* 1606 */     Vector list = parseArrayTrim(sectionProps, "CopyCommands");
/* 1607 */     size = list.size();
/* 1608 */     int totalBytes = 0;
/* 1609 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1611 */       String line = (String)list.elementAt(i);
/* 1612 */       line = line.trim();
/* 1613 */       line = ResourceObjectLoader.stripHtml(line);
/* 1614 */       if (line.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1618 */       Vector command = parseArray(line, ',', '^');
/* 1619 */       int commandSize = command.size();
/* 1620 */       if ((commandSize != 2) && (commandSize != 3))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1626 */       boolean isRecursive = false;
/*      */ 
/* 1628 */       boolean isNewOnly = false;
/* 1629 */       if (commandSize == 3)
/*      */       {
/* 1631 */         String flags = (String)command.elementAt(2);
/* 1632 */         isRecursive = flags.indexOf("r") >= 0;
/*      */ 
/* 1634 */         isNewOnly = flags.indexOf("n") >= 0;
/*      */       }
/*      */ 
/* 1637 */       String src = (String)command.elementAt(0);
/* 1638 */       src = computeDestinationEx(src, false);
/* 1639 */       File srcFile = new File(src);
/* 1640 */       if ((srcFile.isDirectory()) && (!isRecursive))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1646 */       String dst = (String)command.elementAt(1);
/* 1647 */       dst = computeDestinationEx(dst, false);
/* 1648 */       if ((isNewOnly) && (FileUtils.checkFile(dst, false, false) == 0))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1655 */       totalBytes = (int)(totalBytes + computeCopySize(src));
/*      */     }
/*      */ 
/* 1658 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1660 */       String line = (String)list.elementAt(i);
/* 1661 */       line = line.trim();
/* 1662 */       line = ResourceObjectLoader.stripHtml(line);
/* 1663 */       if (line.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1667 */       Vector command = parseArray(line, ',', '^');
/* 1668 */       int commandSize = command.size();
/* 1669 */       if ((commandSize != 2) && (commandSize != 3))
/*      */       {
/* 1671 */         String msg = LocaleUtils.encodeMessage("csInstallerBadCopyCommand", null, line);
/*      */ 
/* 1673 */         Report.trace("install", LocaleResources.localizeMessage(msg, null), null);
/* 1674 */         this.m_installLog.warning(msg);
/*      */       }
/*      */       else
/*      */       {
/* 1678 */         String src = (String)command.elementAt(0);
/* 1679 */         String dst = (String)command.elementAt(1);
/* 1680 */         boolean isRecursive = false;
/*      */ 
/* 1682 */         boolean isNewOnly = false;
/* 1683 */         if (commandSize == 3)
/*      */         {
/* 1685 */           String flags = (String)command.elementAt(2);
/* 1686 */           isRecursive = flags.indexOf("r") >= 0;
/*      */ 
/* 1688 */           isNewOnly = flags.indexOf("n") >= 0;
/*      */         }
/*      */ 
/* 1691 */         String outputDst = dst;
/* 1692 */         src = computeDestination(src);
/* 1693 */         dst = computeDestination(dst);
/* 1694 */         if (src.equals(dst))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1699 */         if (isNewOnly) { if (FileUtils.checkFile(dst, false, false) == 0) continue; if (FileUtils.checkFile(dst, true, false) == 0)
/*      */           {
/*      */             continue;
/*      */           }
/*      */  }
/*      */ 
/*      */ 
/* 1708 */         File srcFile = new File(src);
/* 1709 */         if ((srcFile.isDirectory()) && (!isRecursive))
/*      */         {
/* 1711 */           LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerBadCopyCommand", null, line), null);
/*      */         }
/*      */         else
/*      */         {
/* 1717 */           String msg = LocaleUtils.encodeMessage("csInstallerCopyFileProgress", null, section, outputDst);
/*      */           String dstDir;
/*      */           String dstDir;
/* 1720 */           if (isRecursive)
/*      */           {
/* 1722 */             dstDir = FileUtils.directorySlashes(dst);
/*      */           }
/*      */           else
/*      */           {
/* 1726 */             dstDir = FileUtils.getDirectory(dst);
/*      */           }
/* 1728 */           String msg2 = LocaleUtils.encodeMessage("syFileUtilsUnableToCreateSpecifiedDir", null, dstDir);
/*      */ 
/* 1730 */           verifyDirectoryWithException(dstDir, 9, msg2);
/* 1731 */           reportProgress(0, msg, i, size);
/*      */ 
/* 1733 */           if ((!isRecursive) && (FileUtils.checkFile(dst, true, false) == 0))
/*      */           {
/* 1736 */             FileUtils.deleteFile(dst);
/*      */           }
/*      */ 
/* 1739 */           if (FileUtils.checkFile(src, !isRecursive, false) != 0)
/*      */           {
/* 1742 */             msg = LocaleUtils.encodeMessage("csInstallerFileCopyError", null, src, dst);
/*      */ 
/* 1744 */             throw new ServiceException(msg);
/*      */           }
/*      */ 
/* 1747 */           Report.trace("install", "copying from \"" + src + "\" to \"" + dst + "\"", null);
/*      */ 
/* 1749 */           copyRecursive(src, dst, "csInstallerCopyFileProgress", section, 0L, totalBytes);
/*      */         }
/*      */       }
/*      */     }
/* 1753 */     if (size > 0)
/*      */     {
/* 1755 */       this.m_workStatus[0] += this.m_workStatus[2];
/*      */     }
/*      */     else
/*      */     {
/* 1761 */       this.m_workStatus[1] -= this.m_workStatus[2];
/*      */     }
/*      */ 
/* 1765 */     list = parseArrayTrim(sectionProps, "InstallerClassList");
/* 1766 */     tmprc = runInstallerCode(list, section, type, sectionProps);
/* 1767 */     if (tmprc != 0)
/*      */     {
/* 1769 */       rc = tmprc;
/*      */     }
/* 1771 */     this.m_workStatus[0] += this.m_workStatus[2];
/*      */ 
/* 1773 */     return rc;
/*      */   }
/*      */ 
/*      */   protected long computeCopySize(String src)
/*      */   {
/* 1778 */     long size = computeCopySizeEx(src, 0, 0);
/* 1779 */     return size;
/*      */   }
/*      */ 
/*      */   public long computeCopySizeEx(String src, int depth, int flags)
/*      */   {
/* 1784 */     boolean preserveLinks = (flags & 0x1) != 0;
/*      */ 
/* 1786 */     if ((this.m_utils == null) || (!this.m_utils.isPosixFilesystemSupported()))
/*      */     {
/* 1788 */       preserveLinks = false;
/*      */     }
/*      */ 
/* 1791 */     File srcFile = new File(src);
/* 1792 */     long count = 0L;
/*      */ 
/* 1794 */     if (srcFile.isFile())
/*      */     {
/* 1796 */       boolean skipFile = false;
/* 1797 */       if (preserveLinks)
/*      */       {
/* 1799 */         PosixStructStat sb = new PosixStructStat();
/* 1800 */         long errorCode = this.m_utils.lstat(src, sb);
/* 1801 */         if (errorCode != 0L)
/*      */         {
/* 1803 */           Report.trace("install", "lstat() failed on " + src + ": " + this.m_utils.getErrorMessage(errorCode), null);
/*      */         }
/* 1808 */         else if ((sb.st_mode | NativeOsUtils.S_IFLNK) != 0)
/*      */         {
/* 1812 */           skipFile = true;
/*      */         }
/*      */       }
/*      */ 
/* 1816 */       if (!skipFile)
/*      */       {
/* 1818 */         count = srcFile.length();
/*      */       }
/*      */     }
/* 1821 */     else if (srcFile.isDirectory())
/*      */     {
/* 1823 */       src = FileUtils.directorySlashes(src);
/* 1824 */       String[] list = srcFile.list();
/* 1825 */       if (list == null)
/*      */       {
/* 1827 */         Report.trace("install", "unable to get list of directory " + src, null);
/*      */       }
/*      */       else
/*      */       {
/* 1832 */         for (int i = 0; i < list.length; ++i)
/*      */         {
/* 1834 */           count += computeCopySize(src + list[i]);
/*      */         }
/*      */       }
/*      */     }
/* 1838 */     return count;
/*      */   }
/*      */ 
/*      */   protected long copyRecursive(String src, String dst, String msg, String section, long bytesCopied, long totalBytes)
/*      */     throws ServiceException
/*      */   {
/* 1848 */     return copyRecursiveEx(src, dst, msg, section, bytesCopied, totalBytes, 0, 0);
/*      */   }
/*      */ 
/*      */   public long copyRecursiveEx(String src, String dst, String msg, String section, long bytesCopied, long totalBytes, int depth, int flags)
/*      */     throws ServiceException
/*      */   {
/* 1856 */     File srcFile = new File(src);
/* 1857 */     PosixStructStat sb = null;
/* 1858 */     boolean preserveLinks = (flags & 0x1) != 0;
/* 1859 */     boolean preserveModes = (flags & 0x2) != 0;
/* 1860 */     boolean isPosixSupported = (this.m_utils != null) && (this.m_utils.isPosixFilesystemSupported());
/*      */ 
/* 1862 */     if ((preserveLinks) && (((this.m_utils == null) || (!isPosixSupported))))
/*      */     {
/* 1864 */       Report.trace("install", "unable to preserve links because m_utils is null or posix not supported", null);
/*      */ 
/* 1866 */       preserveLinks = false;
/* 1867 */       flags &= -2;
/*      */     }
/* 1869 */     if ((preserveModes) && (((this.m_utils == null) || (!isPosixSupported))))
/*      */     {
/* 1871 */       if (!EnvUtils.getOSFamily().equals("windows"))
/*      */       {
/* 1873 */         Report.trace("install", "unable to preserve permissions because m_utils is null or posix not supported", null);
/*      */       }
/*      */ 
/* 1876 */       preserveModes = false;
/* 1877 */       flags &= -3;
/*      */     }
/*      */ 
/* 1880 */     if ((preserveLinks) || (preserveModes))
/*      */     {
/* 1882 */       sb = new PosixStructStat();
/* 1883 */       long errorCode = this.m_utils.lstat(src, sb);
/* 1884 */       if (errorCode != 0L)
/*      */       {
/* 1886 */         Report.trace("install", "lstat() failed on " + src + ": " + this.m_utils.getErrorMessage(errorCode), null);
/*      */ 
/* 1888 */         sb = null;
/*      */       }
/*      */     }
/*      */ 
/* 1892 */     if ((preserveLinks) && (sb != null) && ((sb.st_mode & NativeOsUtils.S_IFLNK) == NativeOsUtils.S_IFLNK))
/*      */     {
/* 1898 */       String linkTarget = this.m_utils.readlink(src);
/* 1899 */       new File(dst).delete();
/*      */       int rc;
/* 1901 */       if ((rc = this.m_utils.symlink(linkTarget, dst)) != 0)
/*      */       {
/* 1903 */         IdcMessage errMsg = IdcMessageFactory.lc("csInstallerSymlinkError", new Object[] { dst, linkTarget });
/*      */ 
/* 1905 */         errMsg.m_prior = IdcMessageFactory.lc(this.m_utils.getErrorMessage(rc), new Object[0]);
/* 1906 */         throw new ServiceException(null, errMsg);
/*      */       }
/*      */     }
/* 1909 */     else if (srcFile.isDirectory())
/*      */     {
/* 1911 */       src = FileUtils.directorySlashes(src);
/* 1912 */       dst = FileUtils.directorySlashes(dst);
/* 1913 */       verifyDirectoryWithException(dst, 1, null);
/* 1914 */       String[] list = srcFile.list();
/* 1915 */       if (list == null)
/*      */       {
/* 1917 */         String errMsg = LocaleUtils.encodeMessage("syUnableToReadFile", null, src);
/*      */ 
/* 1919 */         errMsg = LocaleUtils.encodeMessage("syFileUtilsUnableToCopy", errMsg, src, dst);
/*      */ 
/* 1921 */         this.m_installLog.error(errMsg);
/*      */       }
/*      */       else
/*      */       {
/* 1925 */         for (int i = 0; i < list.length; ++i)
/*      */         {
/* 1927 */           bytesCopied = copyRecursiveEx(src + list[i], dst + list[i], msg, section, bytesCopied, totalBytes, depth + 1, flags);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/* 1933 */     else if (srcFile.isFile())
/*      */     {
/* 1935 */       if (msg != null)
/*      */       {
/* 1937 */         msg = LocaleUtils.encodeMessage(msg, null, section, dst);
/*      */       }
/* 1939 */       bytesCopied = copyFile(src, dst, msg, bytesCopied, totalBytes);
/* 1940 */       if ((preserveModes) && (sb != null))
/*      */       {
/* 1942 */         int mode = sb.st_mode & (NativeOsUtils.S_IRWXU | NativeOsUtils.S_IRWXG | NativeOsUtils.S_IRWXO);
/*      */ 
/* 1944 */         this.m_utils.chmod(dst, mode);
/* 1945 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1947 */           String modeString = Integer.toHexString(mode);
/* 1948 */           Report.debug("install", "setting mode on '" + dst + "' to " + modeString, null);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1954 */     return bytesCopied;
/*      */   }
/*      */ 
/*      */   protected long copyFile(String src, String dst, String msg, long bytesCopied, long totalBytes)
/*      */     throws ServiceException
/*      */   {
/* 1960 */     FileOutputStream fos = null;
/* 1961 */     FileInputStream fis = null;
/* 1962 */     long startTime = System.currentTimeMillis();
/*      */     try
/*      */     {
/* 1966 */       fos = openFileForOutput(dst);
/* 1967 */       fis = new FileInputStream(src);
/* 1968 */       while (fis.available() > 0)
/*      */       {
/* 1970 */         int num = fis.read(this.m_byteBuffer);
/* 1971 */         bytesCopied += num;
/* 1972 */         this.m_fileOutputBytes += num;
/* 1973 */         fos.write(this.m_byteBuffer, 0, num);
/* 1974 */         if (msg != null)
/*      */         {
/* 1976 */           reportProgress(1, msg, (float)bytesCopied, (float)totalBytes);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       long elapsed;
/* 1989 */       FileUtils.closeFiles(fos, fis);
/* 1990 */       long elapsed = System.currentTimeMillis() - startTime;
/* 1991 */       if (elapsed > 100L)
/*      */       {
/* 1993 */         this.m_fileOutputTime += elapsed;
/*      */       }
/*      */     }
/*      */ 
/* 1997 */     return bytesCopied;
/*      */   }
/*      */ 
/*      */   public void copy(String src, String dst)
/*      */     throws ServiceException
/*      */   {
/* 2003 */     long startTime = System.currentTimeMillis();
/* 2004 */     FileOutputStream fos = null;
/* 2005 */     FileInputStream fis = null;
/*      */ 
/* 2007 */     src = computeDestination(src);
/* 2008 */     dst = computeDestination(dst);
/*      */     try
/*      */     {
/* 2012 */       fos = openFileForOutput(dst);
/* 2013 */       fis = new FileInputStream(src);
/* 2014 */       while (fis.available() > 0)
/*      */       {
/* 2016 */         int num = fis.read(this.m_byteBuffer);
/* 2017 */         fos.write(this.m_byteBuffer, 0, num);
/* 2018 */         this.m_fileOutputBytes += num;
/*      */       }
/* 2020 */       fos.close();
/* 2021 */       fos = null;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       long elapsed;
/*      */       String msg;
/* 2031 */       FileUtils.closeFiles(fos, fis);
/* 2032 */       long elapsed = System.currentTimeMillis() - startTime;
/* 2033 */       if (elapsed > 100L)
/*      */       {
/* 2035 */         this.m_fileOutputTime += elapsed;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deleteRecursive(String path) throws IOException
/*      */   {
/* 2042 */     Report.trace("install", "recursively removing \"" + path + "\".", null);
/* 2043 */     deleteRecursiveEx(path);
/*      */   }
/*      */ 
/*      */   protected void deleteRecursiveEx(String path) throws IOException
/*      */   {
/* 2048 */     File f = new File(path);
/* 2049 */     if (!f.exists())
/*      */     {
/* 2051 */       Report.trace("install", "deleteRecursiveEx() called on non-existent path \"" + path + "\"", null);
/*      */ 
/* 2053 */       return;
/*      */     }
/* 2055 */     if (f.isFile())
/*      */     {
/* 2057 */       f.delete();
/* 2058 */       return;
/*      */     }
/* 2060 */     String[] names = f.list();
/* 2061 */     if (names == null)
/*      */     {
/* 2063 */       Report.trace("install", "got null listing for \"" + path + "\"", null);
/*      */     }
/*      */     else
/*      */     {
/* 2067 */       for (int i = 0; i < names.length; ++i)
/*      */       {
/* 2069 */         deleteRecursiveEx(path + "/" + names[i]);
/*      */       }
/*      */     }
/* 2072 */     f.delete();
/*      */   }
/*      */ 
/*      */   protected void expandSectionZip(String srcPath, boolean doBackup, String section, FilenameFilter filter)
/*      */     throws ServiceException
/*      */   {
/* 2080 */     int flags = 4;
/* 2081 */     expandSectionZipEx(srcPath, doBackup, section, filter, flags);
/*      */   }
/*      */ 
/*      */   protected void expandSectionZipEx(String srcPath, boolean doBackup, String section, FilenameFilter filter, int extractionFlags)
/*      */     throws ServiceException
/*      */   {
/* 2090 */     String flags = "";
/* 2091 */     int index = srcPath.lastIndexOf("(");
/* 2092 */     if (index > 0)
/*      */     {
/* 2094 */       int index2 = srcPath.lastIndexOf(")");
/* 2095 */       if (index2 > index)
/*      */       {
/* 2097 */         flags = srcPath.substring(index + 1, index2);
/* 2098 */         srcPath = srcPath.substring(0, index);
/*      */       }
/*      */     }
/*      */ 
/* 2102 */     srcPath = computeDestinationEx(srcPath, false);
/* 2103 */     ZipFile zip = null;
/* 2104 */     OutputStream out = null;
/* 2105 */     InputStream stream = null;
/*      */     try
/*      */     {
/*      */       try
/*      */       {
/* 2110 */         zip = new ZipFile(srcPath);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2114 */         String msg = LocaleUtils.encodeMessage("syUnableToOpenFile", e.getMessage(), srcPath);
/*      */ 
/* 2116 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/* 2119 */       Vector entries = new IdcVector();
/*      */ 
/* 2121 */       long totalBytes = 0L;
/* 2122 */       for (Enumeration en = zip.entries(); en.hasMoreElements(); )
/*      */       {
/* 2124 */         ZipEntry entry = (ZipEntry)en.nextElement();
/* 2125 */         if (entry.isDirectory())
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 2130 */         String outputName = entry.getName();
/* 2131 */         if ((filter != null) && (!filter.accept(null, outputName)))
/*      */         {
/* 2133 */           Report.trace("install", "skipping '" + outputName + "'", null);
/*      */         }
/*      */ 
/* 2136 */         if ((outputName.endsWith("/lockwait.dat")) && (FileUtils.m_neverLock))
/*      */         {
/* 2138 */           Report.trace("install", "skipping '" + outputName + "'", null);
/*      */         }
/*      */ 
/* 2141 */         outputName = computeDestinationEx(outputName, false);
/* 2142 */         if ((flags.indexOf("n") >= 0) && (FileUtils.checkFile(outputName, true, false) == 0))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 2147 */         long size = entry.getSize();
/* 2148 */         if (size >= 0L)
/*      */         {
/* 2150 */           totalBytes += size;
/*      */         }
/* 2152 */         entries.addElement(entry);
/*      */       }
/*      */ 
/* 2155 */       IdcComparator cmp = new IdcComparator()
/*      */       {
/*      */         public int compare(Object o1, Object o2)
/*      */         {
/* 2159 */           ZipEntry e1 = (ZipEntry)o1;
/* 2160 */           ZipEntry e2 = (ZipEntry)o2;
/* 2161 */           String n1 = e1.getName();
/* 2162 */           String n2 = e2.getName();
/* 2163 */           return n1.compareTo(n2);
/*      */         }
/*      */       };
/* 2166 */       Sort.sortVector(entries, cmp);
/*      */ 
/* 2168 */       int size = entries.size();
/* 2169 */       int progressBytes = 0;
/* 2170 */       long startTime = System.currentTimeMillis();
/* 2171 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 2173 */         ZipEntry entry = (ZipEntry)entries.elementAt(i);
/* 2174 */         String name = entry.getName();
/* 2175 */         if ((name.endsWith("/lockwait.dat")) && (FileUtils.m_neverLock)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 2179 */         String outputName = computeDestination(name);
/*      */         try
/*      */         {
/* 2182 */           long fileSize = entry.getSize();
/*      */ 
/* 2184 */           if (doBackup)
/*      */           {
/* 2186 */             File f = new File(outputName);
/* 2187 */             if (f.exists())
/*      */             {
/* 2189 */               String backupFile = this.m_idcDir + "backup/" + name;
/* 2190 */               String backupDir = FileUtils.getDirectory(backupFile);
/* 2191 */               verifyDirectoryWithException(backupDir, 99, null);
/* 2192 */               copyFile(outputName, backupFile, null, 0L, 0L);
/*      */             }
/*      */           }
/* 2195 */           out = openFileForOutput(outputName);
/*      */ 
/* 2197 */           stream = zip.getInputStream(entry);
/* 2198 */           if (stream == null)
/*      */           {
/* 2200 */             String msg = "zip.getInputStream(" + name + ") returned null.  Skipping.";
/*      */ 
/* 2202 */             Report.trace("install", msg, null);
/* 2203 */             this.m_installLog.warning(msg);
/* 2204 */             progressBytes = (int)(progressBytes + fileSize);
/*      */ 
/* 2247 */             FileUtils.closeObjects(stream, out);
/*      */           }
/*      */           else
/*      */           {
/* 2210 */             String msg = LocaleUtils.encodeMessage("csInstallingSection", null, section, name);
/*      */ 
/* 2212 */             int fileBytes = 0;
/*      */             do
/*      */             {
/*      */               int count;
/* 2213 */               if ((count = stream.read(this.m_byteBuffer)) <= 0)
/*      */                 break;
/* 2215 */               if (fileSize >= 0L)
/*      */               {
/* 2217 */                 progressBytes += count;
/* 2218 */                 if ((extractionFlags & 0x4) != 0)
/*      */                 {
/* 2220 */                   reportProgress(1, msg, progressBytes, (float)totalBytes);
/*      */                 }
/*      */               }
/*      */ 
/* 2224 */               out.write(this.m_byteBuffer, 0, count);
/* 2225 */               fileBytes += count;
/* 2226 */               this.m_fileOutputBytes += count;
/* 2227 */             }while ((fileSize <= 0L) || (fileBytes != fileSize));
/*      */ 
/* 2232 */             if ((extractionFlags & 0x4) != 0)
/*      */             {
/* 2234 */               reportProgress(1, msg, progressBytes, (float)totalBytes);
/*      */             }
/*      */           }
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*      */           String msg;
/* 2243 */           throw new ServiceException(msg);
/*      */         }
/*      */         finally
/*      */         {
/* 2247 */           FileUtils.closeObjects(stream, out);
/*      */         }
/*      */       }
/* 2250 */       long elapsed = System.currentTimeMillis() - startTime;
/* 2251 */       if (elapsed > 100L)
/*      */       {
/* 2253 */         this.m_fileOutputTime += elapsed;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 2258 */       FileUtils.closeObject(zip);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateConfigEntries(Vector cfgEntries)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2267 */     int length = cfgEntries.size();
/* 2268 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2270 */       String key = (String)cfgEntries.elementAt(i);
/* 2271 */       Properties props = getInstallerTable("ConfigEntries", key);
/* 2272 */       if (props == null)
/*      */       {
/* 2274 */         throw new DataException(LocaleUtils.encodeMessage("csInstallerConfigEntryNotDefined", null, key));
/*      */       }
/*      */ 
/* 2278 */       String entry = props.getProperty("ConfigEntry");
/* 2279 */       String condition = props.getProperty("ConditionExpression");
/* 2280 */       if ((condition != null) && (condition.trim().length() > 0))
/*      */       {
/* 2282 */         String rc = evaluateScript(condition).trim();
/* 2283 */         if (!StringUtils.convertToBool(rc, false))
/*      */         {
/* 2285 */           Report.trace("install", "not setting " + entry + " because script " + condition.trim() + " evaluated to " + rc, null);
/*      */ 
/* 2288 */           continue;
/*      */         }
/* 2290 */         Report.trace("install", "setting " + entry + " because script " + condition.trim() + " evaluated to " + rc, null);
/*      */       }
/*      */ 
/* 2294 */       Vector fileList = getList(props, "ConfigFile");
/*      */ 
/* 2296 */       Properties flagValues = new Properties();
/* 2297 */       long flags = parseFlags(props.getProperty("Flags"), flagValues);
/* 2298 */       String value = getInstallValue(key, null);
/*      */ 
/* 2300 */       if (value == null)
/*      */       {
/* 2302 */         if ((flags & 1L) == 0L)
/*      */           continue;
/* 2304 */         throw new DataException(LocaleUtils.encodeMessage("csInstallerConfigEntryInvalid", null, entry));
/*      */       }
/*      */ 
/* 2310 */       for (int j = 0; j < fileList.size(); ++j)
/*      */       {
/* 2312 */         String file = (String)fileList.elementAt(j);
/* 2313 */         editConfigFile(file, entry, value);
/*      */       }
/* 2315 */       loadConfig();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected int runInstallerCode(Vector list, String section, String type, Properties sectionProps)
/*      */     throws ServiceException
/*      */   {
/* 2322 */     int rc = 0;
/* 2323 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/* 2326 */       String className = (String)list.elementAt(i);
/* 2327 */       SectionInstaller installer = null;
/*      */       try
/*      */       {
/* 2331 */         Class c = Class.forName(className);
/* 2332 */         installer = (SectionInstaller)c.newInstance();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2336 */         String msg = LocaleUtils.encodeMessage("csInstallerSectionInstallError", e.getMessage(), section, className);
/*      */ 
/* 2339 */         Report.trace("install", null, e);
/* 2340 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/* 2343 */       Report.trace("install", "running code in class " + className, null);
/* 2344 */       int tmprc = installer.installSection(section, type, null, this, sectionProps);
/* 2345 */       if (tmprc == 0)
/*      */         continue;
/* 2347 */       rc = tmprc;
/*      */     }
/*      */ 
/* 2350 */     return rc;
/*      */   }
/*      */ 
/*      */   public String[] parseCommandLine(String commandLine)
/*      */   {
/* 2355 */     Vector v = new IdcVector();
/* 2356 */     char state = 's';
/* 2357 */     char priorState = '!';
/* 2358 */     int length = commandLine.length();
/* 2359 */     IdcStringBuilder thisArg = null;
/* 2360 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2362 */       boolean appendChar = true;
/* 2363 */       boolean appendArg = false;
/* 2364 */       char thisChar = commandLine.charAt(i);
/* 2365 */       char c = thisChar;
/* 2366 */       if (Character.isWhitespace(thisChar))
/*      */       {
/* 2368 */         c = ' ';
/*      */       }
/* 2370 */       switch (state)
/*      */       {
/*      */       case 's':
/* 2373 */         switch (c)
/*      */         {
/*      */         case ' ':
/* 2376 */           appendChar = false;
/* 2377 */           break;
/*      */         case '\\':
/* 2379 */           appendChar = false;
/* 2380 */           priorState = state;
/* 2381 */           state = 'e';
/* 2382 */           break;
/*      */         case '"':
/* 2384 */           appendChar = false;
/* 2385 */           state = 'q';
/* 2386 */           break;
/*      */         default:
/* 2388 */           state = 't';
/* 2389 */         }break;
/*      */       case 't':
/* 2393 */         switch (c)
/*      */         {
/*      */         case ' ':
/* 2396 */           appendChar = false;
/* 2397 */           appendArg = true;
/* 2398 */           state = 's';
/* 2399 */           break;
/*      */         case '\\':
/* 2401 */           appendChar = false;
/* 2402 */           priorState = state;
/* 2403 */           state = 'e';
/* 2404 */           break;
/*      */         case '"':
/* 2407 */           break label350:
/*      */         }
/* 2409 */         break;
/*      */       case 'q':
/* 2413 */         switch (c)
/*      */         {
/*      */         case ' ':
/* 2417 */           break;
/*      */         case '\\':
/* 2419 */           appendChar = false;
/* 2420 */           priorState = state;
/* 2421 */           state = 'e';
/* 2422 */           break;
/*      */         case '"':
/* 2424 */           appendChar = false;
/* 2425 */           appendArg = true;
/* 2426 */           state = 's';
/* 2427 */           break label350:
/*      */         }
/*      */ 
/* 2430 */         break;
/*      */       case 'e':
/* 2435 */         state = priorState;
/* 2436 */         break;
/*      */       default:
/* 2438 */         throw new IllegalStateException("SysInstaller.parseCommandLine() entered illegal state \"" + state + "\"");
/*      */       }
/*      */ 
/* 2442 */       if (appendChar)
/*      */       {
/* 2444 */         if (thisArg == null)
/*      */         {
/* 2446 */           label350: thisArg = new IdcStringBuilder();
/*      */         }
/* 2448 */         thisArg.append(thisChar);
/*      */       }
/* 2450 */       if ((!appendArg) || (thisArg == null))
/*      */         continue;
/* 2452 */       v.addElement(thisArg.toString());
/* 2453 */       thisArg = new IdcStringBuilder();
/*      */     }
/*      */ 
/* 2456 */     if ((thisArg != null) && (thisArg.length() > 0))
/*      */     {
/* 2458 */       v.addElement(thisArg.toString());
/*      */     }
/*      */ 
/* 2461 */     String[] array = new String[v.size()];
/* 2462 */     v.copyInto(array);
/* 2463 */     return array;
/*      */   }
/*      */ 
/*      */   public static void traceCommandExecution(String[] commandLine)
/*      */   {
/* 2468 */     IdcStringBuilder commandStringBuf = new IdcStringBuilder();
/* 2469 */     commandStringBuf.append("executing \"");
/* 2470 */     for (int i = 0; i < commandLine.length; ++i)
/*      */     {
/* 2472 */       if (i > 0)
/*      */       {
/* 2474 */         commandStringBuf.append(" ");
/*      */       }
/* 2476 */       commandStringBuf.append("'");
/* 2477 */       commandStringBuf.append(commandLine[i]);
/* 2478 */       commandStringBuf.append("'");
/*      */     }
/* 2480 */     commandStringBuf.append("\"");
/* 2481 */     Report.trace("install", commandStringBuf.toString(), null);
/*      */   }
/*      */ 
/*      */   public int runCommand(String[] commandLine, Vector results, String msg, boolean logAsError, boolean isQuiet)
/*      */     throws ServiceException
/*      */   {
/* 2490 */     int rc = -1;
/* 2491 */     IOException theException = null;
/*      */     try
/*      */     {
/* 2494 */       rc = runCriticalCommand(commandLine, results, isQuiet);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 2498 */       theException = e;
/*      */     }
/*      */ 
/* 2501 */     if (rc != 0)
/*      */     {
/* 2503 */       if (msg != null)
/*      */       {
/* 2505 */         if (msg.length() > 0)
/*      */         {
/* 2507 */           String tmpMsg = LocaleResources.localizeMessage(msg, null);
/* 2508 */           this.m_promptUser.outputMessage(tmpMsg);
/*      */         }
/*      */ 
/* 2511 */         IdcStringBuilder buf = new IdcStringBuilder();
/* 2512 */         for (int j = 0; j < commandLine.length; ++j)
/*      */         {
/* 2514 */           buf.append(commandLine[j]);
/* 2515 */           buf.append(" ");
/*      */         }
/* 2517 */         String theCommand = buf.toString();
/* 2518 */         msg = LocaleUtils.appendMessage(LocaleUtils.encodeMessage("syProcessCommand", null, theCommand), msg);
/*      */ 
/* 2520 */         for (int i = 0; i < results.size(); ++i)
/*      */         {
/* 2522 */           msg = LocaleUtils.appendMessage((String)results.get(i), msg);
/*      */         }
/* 2524 */         if (logAsError)
/*      */         {
/* 2526 */           this.m_installLog.error(msg);
/*      */         }
/*      */         else
/*      */         {
/* 2530 */           this.m_installLog.warning(msg);
/*      */         }
/*      */       }
/* 2533 */       if (theException != null)
/*      */       {
/* 2535 */         String message = theException.getMessage();
/* 2536 */         this.m_installLog.error(message);
/* 2537 */         message = LocaleResources.localizeMessage(message, null);
/* 2538 */         this.m_promptUser.outputMessage(message);
/*      */       }
/*      */     }
/* 2541 */     return rc;
/*      */   }
/*      */ 
/*      */   public static int runCommandSimple(String[] commandLine, Vector results, byte[] buffer)
/*      */     throws IOException
/*      */   {
/* 2547 */     if (results == null)
/*      */     {
/* 2549 */       results = new IdcVector();
/*      */     }
/* 2551 */     results.removeAllElements();
/*      */ 
/* 2553 */     Runtime rt = Runtime.getRuntime();
/* 2554 */     byte[] theBuffer = buffer;
/* 2555 */     Process p = null;
/* 2556 */     int exitValue = -1;
/*      */     try
/*      */     {
/* 2559 */       traceCommandExecution(commandLine);
/* 2560 */       p = rt.exec(commandLine);
/* 2561 */       InputStream[] outputStreams = { p.getInputStream(), p.getErrorStream() };
/*      */ 
/* 2563 */       Thread[] threads = new Thread[outputStreams.length];
/* 2564 */       Vector theResults = results;
/*      */ 
/* 2566 */       for (int i = 0; i < outputStreams.length; ++i)
/*      */       {
/* 2568 */         InputStream theStream = outputStreams[i];
/* 2569 */         threads[i] = new Thread(theStream, theBuffer, theResults)
/*      */         {
/*      */           public void run()
/*      */           {
/*      */             try
/*      */             {
/* 2577 */               String str = "";
/* 2578 */               while ((count = this.val$theStream.read(this.val$theBuffer)) > 0)
/*      */               {
/*      */                 int count;
/* 2580 */                 str = str + new String(this.val$theBuffer, 0, count);
/*      */ 
/* 2582 */                 while ((index = str.indexOf("\n")) >= 0)
/*      */                 {
/*      */                   int index;
/* 2584 */                   this.val$theResults.addElement(str.substring(0, index));
/* 2585 */                   str = str.substring(index + 1);
/*      */                 }
/*      */               }
/*      */             }
/*      */             catch (Exception ignore)
/*      */             {
/* 2591 */               Report.trace("install", null, ignore);
/*      */             }
/*      */           }
/*      */         };
/* 2596 */         threads[i].start();
/*      */       }
/*      */ 
/* 2599 */       p.waitFor();
/*      */     }
/*      */     catch (InterruptedException ignore)
/*      */     {
/* 2603 */       if (p != null) p.destroy();
/*      */     }
/*      */     finally
/*      */     {
/* 2607 */       if (p != null) exitValue = p.exitValue();
/*      */     }
/*      */ 
/* 2610 */     return exitValue;
/*      */   }
/*      */ 
/*      */   public int runCriticalCommand(String[] commandLine, Vector results, boolean isQuiet)
/*      */     throws IOException
/*      */   {
/* 2616 */     if (results == null)
/*      */     {
/* 2618 */       results = new IdcVector();
/*      */     }
/* 2620 */     int exitValue = runCommandSimple(commandLine, results, this.m_byteBuffer);
/*      */ 
/* 2622 */     if ((!isQuiet) || (SystemUtils.isActiveTrace("install")))
/*      */     {
/* 2624 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 2625 */       for (int j = 0; j < commandLine.length; ++j)
/*      */       {
/* 2627 */         buf.append(commandLine[j]);
/* 2628 */         buf.append(" ");
/*      */       }
/* 2630 */       String theCommand = buf.toString();
/* 2631 */       if (!isQuiet)
/*      */       {
/* 2633 */         this.m_promptUser.outputMessage(theCommand);
/*      */       }
/* 2635 */       Report.trace("install", "command \"" + theCommand + "\" results are:", null);
/*      */ 
/* 2637 */       for (int i = 0; i < results.size(); ++i)
/*      */       {
/* 2639 */         if (!isQuiet)
/*      */         {
/* 2641 */           this.m_promptUser.outputMessage((String)results.elementAt(i));
/*      */         }
/* 2643 */         Report.trace("install", (String)results.elementAt(i), null);
/*      */       }
/*      */     }
/*      */ 
/* 2647 */     return exitValue;
/*      */   }
/*      */ 
/*      */   protected Map determinePlatformInstallerSettings(Properties installProps, String cfgFile)
/*      */     throws ServiceException
/*      */   {
/* 2653 */     HashMap propMap = new HashMap();
/*      */ 
/* 2655 */     String currentPlatform = determineCurrentPlatform();
/* 2656 */     Properties platformProps = getInstallerTable("PlatformConfigTable", currentPlatform);
/* 2657 */     String installerDefault = "Installer" + getInstallerTableValue("PlatformConfigTable", currentPlatform, "ExeSuffix");
/*      */ 
/* 2659 */     String installer = platformProps.getProperty("InstallerExecutable", installerDefault);
/* 2660 */     String targetDir = installProps.getProperty("TargetDir");
/*      */ 
/* 2663 */     String installerPath = targetDir + "/install/" + installer;
/* 2664 */     String installerDstFile = "${TargetDir}/install/" + installer;
/*      */     String installerSrcFile;
/* 2666 */     if (getInstallBool("RunningFromInstalledServer", false))
/*      */     {
/* 2668 */       String installerSrcFile = "${IdcHomeDir}/native/${Platform}/bin/Launcher";
/*      */ 
/* 2670 */       String suffix = getInstallerTableValue("PlatformConfigTable", currentPlatform, "ExeSuffix");
/*      */ 
/* 2672 */       installerSrcFile = installerSrcFile + suffix;
/*      */     }
/*      */     else
/*      */     {
/* 2676 */       installerSrcFile = "${SourceDirectory}/" + installer;
/*      */     }
/* 2678 */     String nativeUtilsLibrary = platformProps.getProperty("NativeOsUtilsLibrary");
/* 2679 */     if (nativeUtilsLibrary == null)
/*      */     {
/* 2681 */       Report.trace("install", "PlatformConfigTable didn't set NativeOsUtilsLibrary.", null);
/*      */     }
/*      */     else
/*      */     {
/* 2686 */       propMap.put("NativeUtilsLibrary", nativeUtilsLibrary);
/*      */     }
/* 2688 */     if (cfgFile != null)
/*      */     {
/* 2690 */       propMap.put("InstallerDefSrcFile", FileUtils.getAbsolutePath(cfgFile));
/* 2691 */       propMap.put("InstallerDefDstFile", FileUtils.getAbsolutePath(targetDir + "/install/" + FileUtils.getName(cfgFile)));
/*      */     }
/*      */ 
/* 2695 */     propMap.put("CurrentPlatform", currentPlatform);
/* 2696 */     propMap.put("TargetDir", targetDir);
/* 2697 */     propMap.put("InstallerPath", installerPath);
/* 2698 */     propMap.put("InstallerDstFile", installerDstFile);
/* 2699 */     propMap.put("InstallerSrcFile", installerSrcFile);
/* 2700 */     propMap.put("InstallerConfigFile", "${TargetDir}/install/intradoc.cfg");
/*      */ 
/* 2702 */     return propMap;
/*      */   }
/*      */ 
/*      */   protected void prepareTargetInstallDirectory(Map installerSettings)
/*      */     throws ServiceException
/*      */   {
/* 2708 */     String installerSrcFile = (String)installerSettings.get("InstallerSrcFile");
/* 2709 */     String installerDstFile = (String)installerSettings.get("InstallerDstFile");
/* 2710 */     String nativeUtilsLibrary = (String)installerSettings.get("NativeUtilsLibrary");
/* 2711 */     String installerDefSrcFile = (String)installerSettings.get("InstallerDefSrcFile");
/* 2712 */     String installerDefDstFile = (String)installerSettings.get("InstallerDefDstFile");
/* 2713 */     String idcCfg = (String)installerSettings.get("InstallerConfigFile");
/*      */ 
/* 2715 */     if ((installerDefSrcFile != null) && (installerDefDstFile != null))
/*      */     {
/* 2717 */       File fromFile = new File(installerDefSrcFile);
/* 2718 */       File toFile = new File(installerDefDstFile);
/*      */ 
/* 2724 */       Report.trace("install", "src: " + fromFile, null);
/* 2725 */       Report.trace("install", "dst: " + toFile, null);
/*      */       try
/*      */       {
/* 2728 */         if (!fromFile.getCanonicalPath().equals(toFile.getCanonicalPath()))
/*      */         {
/* 2730 */           Report.trace("install", "copying", null);
/* 2731 */           copy(installerDefSrcFile, installerDefDstFile);
/*      */         }
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 2736 */         throw new ServiceException(e);
/*      */       }
/*      */     }
/*      */ 
/* 2740 */     copy(installerSrcFile, installerDstFile);
/* 2741 */     if ((this.m_utils != null) && (EnvUtils.isFamily("unix")))
/*      */     {
/* 2743 */       String tmpFilePath = computeDestination(installerDstFile);
/* 2744 */       int rc = this.m_utils.chmod(tmpFilePath, 509);
/* 2745 */       if (rc != 0)
/*      */       {
/* 2747 */         Report.trace("install", "unable to set permissions on " + tmpFilePath + ": " + this.m_utils.getErrorMessage(rc), null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2752 */     copy("${SourceDirectory}/intradoc.cfg", idcCfg);
/*      */ 
/* 2754 */     if (nativeUtilsLibrary != null)
/*      */     {
/* 2756 */       if (getInstallBool("RunningFromInstalledServer", false))
/*      */       {
/*      */         try
/*      */         {
/* 2760 */           String version = NativeOsUtils.getJavaNativeVersion();
/* 2761 */           copy("${IdcHomeDir}/components/NativeOsUtils/lib/${Platform}/" + version + "/" + nativeUtilsLibrary, "${TargetDir}/install/" + nativeUtilsLibrary);
/*      */         }
/*      */         catch (ServiceException ignore)
/*      */         {
/* 2767 */           Report.trace("install", null, ignore);
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/* 2772 */         copy("${SourceDirectory}/" + nativeUtilsLibrary, "${TargetDir}/install/" + nativeUtilsLibrary);
/*      */       }
/*      */ 
/* 2775 */       setExecutablePermission("${TargetDir}/install/" + nativeUtilsLibrary);
/*      */     }
/*      */ 
/* 2778 */     if ((!getInstallBool("IsPdfInstall", false)) || (!isWindows()))
/*      */       return;
/* 2780 */     copy("${SourceDirectory}/JniPrinterInstaller.dll", "${TargetDir}/install/JniPrinterInstaller.dll");
/*      */   }
/*      */ 
/*      */   protected void prepareTargetIntradocConfig(Properties installProps, Map installerSettings)
/*      */     throws ServiceException
/*      */   {
/* 2788 */     String idcCfg = (String)installerSettings.get("InstallerConfigFile");
/* 2789 */     String idcCfgDir = computeDestination(FileUtils.getParent(idcCfg));
/* 2790 */     idcCfgDir = FileUtils.directorySlashes(idcCfgDir);
/*      */ 
/* 2792 */     String srcDir = computeDestinationEx("${SourceDirectory}", false);
/* 2793 */     srcDir = FileUtils.fileSlashes(srcDir);
/* 2794 */     String idcHomeDir = FileUtils.getParent(srcDir);
/*      */     String resourcesDir;
/* 2796 */     if (getInstallBool("RunningFromInstalledServer", false))
/*      */     {
/* 2798 */       String resourcesDir = getInstallValue("IdcResourcesDir", null);
/* 2799 */       if (resourcesDir == null)
/*      */       {
/* 2801 */         String idcHome = getInstallValue("IdcHomeDir", null);
/* 2802 */         if (idcHome != null)
/*      */         {
/* 2804 */           editConfigFile(idcCfg, "IdcHomeDir", idcHome);
/* 2805 */           resourcesDir = idcHome + "/resources";
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2811 */       resourcesDir = FileUtils.getAbsolutePath(FileUtils.getParent(srcDir), "resources");
/*      */     }
/*      */ 
/* 2814 */     String mediaDir = computeDestinationEx("${MediaDirectory}", false);
/* 2815 */     if (mediaDir == null)
/*      */     {
/* 2817 */       mediaDir = FileUtils.directorySlashes(FileUtils.getParent(resourcesDir));
/*      */ 
/* 2819 */       Report.trace("install", "using legacy MEDIA_DIR", null);
/*      */     }
/* 2821 */     editConfigFile(idcCfg, "MEDIA_DIR", mediaDir);
/* 2822 */     editConfigFile(idcCfg, "IdcResourcesDir", resourcesDir);
/*      */ 
/* 2824 */     String databaseClasspath = installProps.getProperty("InstallerJdbcClasspath");
/*      */ 
/* 2826 */     if ((databaseClasspath == null) || (databaseClasspath.length() == 0))
/*      */     {
/* 2828 */       databaseClasspath = installProps.getProperty("JdbcDriverPackageSourceFiles");
/*      */     }
/*      */ 
/* 2831 */     if ((databaseClasspath != null) && (databaseClasspath.length() > 0))
/*      */     {
/* 2834 */       editConfigFile(idcCfg, "JDBC_JAVA_CLASSPATH_installerjdbc", databaseClasspath);
/*      */     }
/* 2836 */     editConfigFile(idcCfg, "IDC_LIBRARY_PATH_install", idcCfgDir);
/*      */ 
/* 2838 */     String encoding = getInstallValue("InstallerEncoding", "UTF8");
/* 2839 */     editConfigFile(idcCfg, "FileEncoding", encoding);
/* 2840 */     if (EnvUtils.getOSFamily().equals("windows"))
/*      */     {
/* 2842 */       editConfigFile(idcCfg, "UseFileEncodingForJavaVM", "true");
/*      */     }
/*      */ 
/* 2845 */     String jvm = installProps.getProperty("InstallerJvmPath");
/* 2846 */     if (jvm != null)
/*      */     {
/* 2848 */       editConfigFile(idcCfg, "JAVA_EXE", jvm);
/*      */     }
/*      */ 
/* 2853 */     editConfigFile(idcCfg, "SourceDirectory", srcDir);
/* 2854 */     editConfigFile(idcCfg, "IdcHomeDir", idcHomeDir);
/*      */ 
/* 2858 */     editConfigFile(idcCfg, "LAUNCHER_UPDATE_ENABLED", "false");
/*      */   }
/*      */ 
/*      */   protected int runInstall(Properties installProps, String cfgFile, Vector extraArgs, String jvmSelection, Vector capturedOutput, Properties stdinParams)
/*      */     throws ServiceException
/*      */   {
/* 2872 */     if (extraArgs == null)
/*      */     {
/* 2874 */       extraArgs = new IdcVector();
/*      */     }
/* 2876 */     Vector cmdLine = new IdcVector();
/*      */ 
/* 2878 */     Map installerSettings = determinePlatformInstallerSettings(installProps, cfgFile);
/*      */ 
/* 2881 */     prepareTargetInstallDirectory(installerSettings);
/*      */ 
/* 2883 */     String installerDstFile = (String)installerSettings.get("InstallerPath");
/* 2884 */     cmdLine.addElement(installerDstFile);
/*      */ 
/* 2886 */     addCommonArguments(cmdLine);
/* 2887 */     String cmdLineExtra = installProps.getProperty("InstallCmdLineExtraScript");
/* 2888 */     if (null != cmdLineExtra)
/*      */     {
/*      */       try
/*      */       {
/* 2892 */         cmdLineExtra = this.m_pageMerger.evaluateScript(cmdLineExtra);
/* 2893 */         List cmdExtra = StringUtils.makeListFromEscapedString(cmdLineExtra);
/* 2894 */         extraArgs.addAll(cmdExtra);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2898 */         Report.trace("install", null, e);
/*      */       }
/*      */     }
/*      */ 
/* 2902 */     for (int i = 0; i < extraArgs.size(); ++i)
/*      */     {
/* 2904 */       cmdLine.addElement(extraArgs.elementAt(i));
/*      */     }
/*      */ 
/* 2907 */     if (cfgFile == null)
/*      */     {
/* 2909 */       cmdLine.addElement("--");
/*      */     }
/*      */     else
/*      */     {
/* 2913 */       String installerDefDstFile = (String)installerSettings.get("InstallerDefDstFile");
/*      */ 
/* 2915 */       cmdLine.addElement(installerDefDstFile);
/*      */     }
/*      */ 
/* 2918 */     prepareTargetIntradocConfig(installProps, installerSettings);
/*      */ 
/* 2920 */     String installJvm = installProps.getProperty("InstallJvm");
/* 2921 */     String jvmSetting = null;
/* 2922 */     if ((installJvm != null) && (installJvm.equalsIgnoreCase("custom")))
/*      */     {
/* 2924 */       jvmSetting = installProps.getProperty("JvmPath");
/*      */     }
/* 2926 */     if (jvmSelection != null)
/*      */     {
/* 2928 */       jvmSetting = jvmSelection;
/*      */     }
/*      */ 
/* 2931 */     String idcCfg = (String)installerSettings.get("InstallerConfigFile");
/* 2932 */     if (jvmSetting != null)
/*      */     {
/* 2934 */       editConfigFile(idcCfg, "JAVA_EXE", jvmSetting);
/*      */     }
/*      */ 
/* 2937 */     return runInstallCommand(cmdLine, capturedOutput, stdinParams);
/*      */   }
/*      */ 
/*      */   public void addCommonArguments(Vector cmdLine)
/*      */   {
/* 2942 */     if (StringUtils.convertToBool(getInstallValue("DebugLauncher", null), false))
/*      */     {
/* 2944 */       cmdLine.add("-debug");
/*      */     }
/*      */ 
/* 2949 */     cmdLine.addElement("--set-tty.columns=" + this.m_promptUser.getLineLength());
/* 2950 */     cmdLine.addElement("--set-tty.rows=" + this.m_promptUser.getScreenHeight());
/*      */ 
/* 2952 */     if (this.m_promptUser.getQuiet())
/*      */     {
/* 2954 */       cmdLine.addElement("-q");
/*      */     }
/*      */ 
/* 2959 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2961 */       cmdLine.addElement("-v");
/*      */     }
/* 2963 */     Vector traces = SystemUtils.getActiveTraces();
/* 2964 */     IdcStringBuilder tracesBuffer = null;
/* 2965 */     for (int i = 0; (traces != null) && (i < traces.size()); ++i)
/*      */     {
/* 2967 */       String trace = (String)traces.elementAt(i);
/* 2968 */       if (tracesBuffer == null)
/*      */       {
/* 2970 */         tracesBuffer = new IdcStringBuilder();
/* 2971 */         tracesBuffer.append("--trace=");
/* 2972 */         tracesBuffer.append(trace);
/*      */       }
/*      */       else
/*      */       {
/* 2976 */         tracesBuffer.append(",");
/* 2977 */         tracesBuffer.append(trace);
/*      */       }
/*      */     }
/* 2980 */     if (tracesBuffer == null)
/*      */       return;
/* 2982 */     cmdLine.addElement(tracesBuffer.toString());
/*      */   }
/*      */ 
/*      */   protected int runInstallCommand(Vector cmdLine, Vector capturedOutput)
/*      */     throws ServiceException
/*      */   {
/* 2989 */     return runInstallCommand(cmdLine, capturedOutput, null);
/*      */   }
/*      */ 
/*      */   protected int runInstallCommand(Vector cmdLine, Vector capturedOutput, Properties writeProps)
/*      */     throws ServiceException
/*      */   {
/* 2995 */     Exception exception = null;
/*      */     try
/*      */     {
/* 2998 */       String[] cmdLineArray = new String[cmdLine.size()];
/* 2999 */       cmdLine.copyInto(cmdLineArray);
/* 3000 */       if (SystemUtils.isActiveTrace("install"))
/*      */       {
/* 3002 */         String traceMessage = "starting install with command line: ";
/* 3003 */         for (int i = 0; i < cmdLineArray.length; ++i)
/*      */         {
/* 3005 */           traceMessage = traceMessage + cmdLineArray[i] + " ";
/*      */         }
/* 3007 */         Report.trace("install", traceMessage, null);
/*      */       }
/* 3009 */       Runtime r = Runtime.getRuntime();
/* 3010 */       Process p = r.exec(cmdLineArray);
/* 3011 */       InputStream out = p.getInputStream();
/* 3012 */       InputStream err = p.getErrorStream();
/*      */       InputStreamOutputThread t1;
/*      */       InputStreamOutputThread t1;
/* 3015 */       if ((this.m_promptUser instanceof ConsolePromptUser) && (((ConsolePromptUser)this.m_promptUser).m_useNative))
/*      */       {
/* 3018 */         Writer w = new ConsoleWriter(this.m_utils);
/* 3019 */         t1 = new InputStreamOutputThread(new InputStreamReader(out, "UTF8"), w);
/*      */       }
/*      */       else
/*      */       {
/* 3023 */         t1 = new InputStreamOutputThread(out, System.out);
/*      */       }
/* 3025 */       InputStreamOutputThread t2 = new InputStreamOutputThread(err, System.err);
/* 3026 */       if (capturedOutput != null)
/*      */       {
/* 3028 */         t1.captureOutput(capturedOutput);
/* 3029 */         t2.captureOutput(capturedOutput);
/*      */       }
/* 3031 */       t1.start();
/* 3032 */       t2.start();
/* 3033 */       OutputStream childIn = p.getOutputStream();
/* 3034 */       if (writeProps == null)
/*      */       {
/* 3036 */         childIn.close();
/*      */       }
/*      */       else
/*      */       {
/* 3040 */         Vector v = new IdcVector();
/* 3041 */         Enumeration en = writeProps.keys();
/* 3042 */         while (en.hasMoreElements())
/*      */         {
/* 3044 */           String key = (String)en.nextElement();
/* 3045 */           v.addElement(key);
/*      */         }
/* 3047 */         SystemPropertiesEditor.writeFile(writeProps, v, new IdcVector(), childIn, "UTF8");
/*      */       }
/*      */ 
/* 3050 */       p.waitFor();
/* 3051 */       return p.exitValue();
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 3055 */       exception = e;
/*      */     }
/*      */     catch (InterruptedException e)
/*      */     {
/* 3059 */       exception = e;
/*      */     }
/* 3061 */     String msg = LocaleUtils.encodeMessage("csInstallerUnableToInstall", null);
/* 3062 */     throw new ServiceException(msg, exception);
/*      */   }
/*      */ 
/*      */   protected boolean setExecutablePermission(String path)
/*      */     throws ServiceException
/*      */   {
/* 3068 */     if (EnvUtils.getOSFamily().equals("windows"))
/*      */     {
/* 3070 */       return true;
/*      */     }
/* 3072 */     path = computeDestinationEx(path, false);
/* 3073 */     boolean success = false;
/* 3074 */     if (this.m_utils != null)
/*      */     {
/* 3076 */       String clearBits = getInstallerTableValue("PermissionConfig", "Executable", "UnixClearBits");
/*      */ 
/* 3078 */       String setBits = getInstallerTableValue("PermissionConfig", "Executable", "UnixSetBits");
/*      */ 
/* 3080 */       int clear = Integer.parseInt(clearBits, 8);
/* 3081 */       int set = Integer.parseInt(setBits, 8);
/* 3082 */       PosixStructStat sb = new PosixStructStat();
/* 3083 */       if (this.m_utils.stat(path, sb) == 0)
/*      */       {
/* 3085 */         int newPerm = sb.st_mode &= (clear ^ 0xFFFFFFFF);
/* 3086 */         newPerm |= set;
/* 3087 */         if (this.m_utils.chmod(path, newPerm) == 0)
/*      */         {
/* 3089 */           success = true;
/*      */         }
/*      */       }
/*      */     }
/* 3093 */     if (!success)
/*      */     {
/* 3095 */       String commandLineText = getInstallerTableValue("PermissionExecutables", determineCurrentPlatform(), "CommandLine");
/*      */ 
/* 3098 */       String flag = getInstallerTableValue("PermissionConfig", "Executable", "UnixSet");
/*      */ 
/* 3100 */       PermissionInstaller permInstaller = new PermissionInstaller();
/* 3101 */       permInstaller.m_installer = this;
/* 3102 */       String[] commandLine = permInstaller.createCommandLine(commandLineText, flag, path);
/*      */ 
/* 3104 */       Vector results = new IdcVector();
/* 3105 */       if (runCommand(commandLine, results, null, false, true) == 0)
/*      */       {
/* 3108 */         success = true;
/*      */       }
/*      */     }
/* 3111 */     if (!success)
/*      */     {
/* 3113 */       Report.trace("install", "unable to set permissions on '" + path + "'", null);
/*      */     }
/*      */ 
/* 3116 */     return success;
/*      */   }
/*      */ 
/*      */   protected int findUnmatchedChar(String str, char left, char right, int startAt)
/*      */   {
/* 3123 */     int depth = 0;
/* 3124 */     int length = str.length();
/* 3125 */     while (startAt < length)
/*      */     {
/* 3127 */       char c = str.charAt(startAt);
/* 3128 */       if (c == left)
/*      */       {
/* 3130 */         ++depth;
/*      */       }
/* 3132 */       else if (c == right)
/*      */       {
/* 3134 */         if (depth == 0)
/*      */         {
/* 3136 */           return startAt;
/*      */         }
/* 3138 */         --depth;
/*      */       }
/* 3140 */       ++startAt;
/*      */     }
/* 3142 */     return -1;
/*      */   }
/*      */ 
/*      */   public String substituteVariables(String str, Properties props)
/*      */   {
/* 3147 */     String originalString = str;
/* 3148 */     if (str == null)
/*      */     {
/* 3150 */       return str;
/*      */     }
/* 3152 */     int i = 0;
/* 3153 */     while ((i = str.indexOf("${", i)) >= 0)
/*      */     {
/* 3155 */       int j = findUnmatchedChar(str, '{', '}', i + 2);
/* 3156 */       if (j <= 0)
/*      */         break;
/* 3158 */       String arg = str.substring(i + 2, j);
/*      */       String value;
/*      */       String value;
/* 3160 */       if (arg.equals("dollar"))
/*      */       {
/* 3162 */         value = "$";
/*      */       }
/*      */       else
/*      */       {
/*      */         String value;
/* 3164 */         if (arg.equals("lbrace"))
/*      */         {
/* 3166 */           value = "{";
/*      */         }
/*      */         else
/*      */         {
/*      */           String value;
/* 3168 */           if (arg.equals("rbrace"))
/*      */           {
/* 3170 */             value = "}";
/*      */           }
/*      */           else
/*      */           {
/* 3174 */             int index = arg.indexOf(":");
/* 3175 */             String defaultValue = null;
/* 3176 */             if (index >= 0)
/*      */             {
/* 3178 */               defaultValue = arg.substring(index + 1);
/* 3179 */               char c = '\000';
/* 3180 */               if (defaultValue.length() > 0)
/*      */               {
/* 3182 */                 c = defaultValue.charAt(0);
/*      */               }
/* 3184 */               switch (c)
/*      */               {
/*      */               case '\000':
/* 3187 */                 break;
/*      */               case '-':
/*      */               default:
/* 3190 */                 defaultValue = defaultValue.substring(1);
/*      */               }
/*      */ 
/* 3193 */               arg = arg.substring(0, index);
/*      */             }
/*      */ 
/* 3196 */             if ((arg.equals("MSDEPassword")) || (arg.equals("RandomPassword")))
/*      */             {
/* 3198 */               Random r = new Random(System.currentTimeMillis());
/* 3199 */               byte[] randomBytes = new byte[8];
/* 3200 */               char[] password = new char[randomBytes.length];
/* 3201 */               r.nextBytes(randomBytes);
/* 3202 */               for (int k = 0; k < randomBytes.length; ++k)
/*      */               {
/* 3204 */                 int theByte = randomBytes[k] & 0x3F;
/* 3205 */                 char theChar = '3';
/* 3206 */                 if (theByte < 26)
/*      */                 {
/* 3208 */                   theChar = (char)(theByte - 0 + 65);
/*      */                 }
/* 3210 */                 else if (theByte < 52)
/*      */                 {
/* 3212 */                   theChar = (char)(theByte - 26 + 97);
/*      */                 }
/* 3214 */                 else if (theByte < 62)
/*      */                 {
/* 3216 */                   theChar = (char)(theByte - 52 + 48);
/*      */                 }
/* 3218 */                 else if (theByte == 62)
/*      */                 {
/* 3220 */                   theChar = '5';
/*      */                 }
/* 3222 */                 else if (theByte == 63)
/*      */                 {
/* 3224 */                   theChar = '7';
/*      */                 }
/* 3226 */                 password[k] = theChar;
/*      */               }
/* 3228 */               defaultValue = new String(password);
/*      */             }
/*      */             String value;
/* 3231 */             if (props == null)
/*      */             {
/* 3233 */               value = getInstallValue(arg, null);
/*      */             }
/*      */             else
/*      */             {
/* 3237 */               value = props.getProperty(arg);
/* 3238 */               if (value == null)
/*      */               {
/* 3240 */                 value = getInstallValue(arg, null);
/*      */               }
/*      */             }
/* 3243 */             if ((value == null) && (this.m_utils != null))
/*      */             {
/* 3245 */               value = this.m_utils.getEnv(arg);
/*      */             }
/* 3247 */             if (value == null)
/*      */             {
/* 3249 */               value = defaultValue;
/*      */             }
/* 3251 */             if (value == null)
/*      */             {
/* 3253 */               value = System.getProperty(arg);
/*      */             }
/* 3255 */             if (value == null)
/*      */             {
/* 3258 */               value = "${" + arg + "}";
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 3261 */       str = str.substring(0, i) + value + str.substring(j + 1);
/*      */ 
/* 3263 */       ++i;
/*      */     }
/*      */ 
/* 3270 */     if (SystemUtils.m_verbose)
/*      */     {
/* 3272 */       Report.trace("install", originalString + " becomes " + str, null);
/*      */     }
/*      */ 
/* 3275 */     return str;
/*      */   }
/*      */ 
/*      */   public String computeDestination(String name)
/*      */     throws ServiceException
/*      */   {
/* 3287 */     return computeDestinationEx(name, true);
/*      */   }
/*      */ 
/*      */   public String computeDestinationEx(String name, boolean check)
/*      */     throws ServiceException
/*      */   {
/* 3295 */     String leftPart = FileUtils.fileSlashes(name);
/* 3296 */     leftPart = substituteVariables(leftPart, this.m_installerConfig);
/* 3297 */     String rightPart = "";
/*      */ 
/* 3299 */     int i = 0;
/* 3300 */     while ((i = leftPart.lastIndexOf("/")) > 0)
/*      */     {
/* 3302 */       String prefix = leftPart.substring(0, i + 1);
/* 3303 */       rightPart = leftPart.substring(i) + rightPart;
/*      */ 
/* 3305 */       String key = (String)this.m_pathTransMap.get(prefix);
/* 3306 */       String trans = (key != null) ? this.m_intradocConfig.getProperty(key) : null;
/* 3307 */       if (trans == null)
/*      */       {
/* 3309 */         String alt = (String)this.m_altTransMap.get(prefix);
/* 3310 */         if (alt != null)
/*      */         {
/* 3312 */           trans = substituteVariables(alt, this.m_installerConfig);
/*      */         }
/*      */       }
/* 3315 */       if (trans != null)
/*      */       {
/* 3317 */         trans = FileUtils.directorySlashes(trans);
/* 3318 */         rightPart = trans + rightPart.substring(1);
/* 3319 */         leftPart = "";
/* 3320 */         break;
/*      */       }
/* 3322 */       leftPart = leftPart.substring(0, i);
/*      */     }
/*      */ 
/* 3325 */     rightPart = leftPart + rightPart;
/* 3326 */     i = rightPart.lastIndexOf("/");
/* 3327 */     String dir = (i >= 0) ? rightPart.substring(0, i) : rightPart;
/*      */ 
/* 3329 */     if (!FileUtils.isAbsolutePath(dir))
/*      */     {
/* 3331 */       dir = this.m_targetDir + dir;
/* 3332 */       rightPart = this.m_targetDir + rightPart;
/*      */     }
/*      */ 
/* 3335 */     if (check)
/*      */     {
/* 3337 */       FileUtils.checkOrCreateDirectory(dir, 99);
/*      */     }
/*      */ 
/* 3340 */     if (SystemUtils.m_verbose)
/*      */     {
/* 3342 */       Report.debug("install", name + "->" + rightPart, null);
/*      */     }
/* 3344 */     return rightPart;
/*      */   }
/*      */ 
/*      */   public void editConfigFile(String file, String key, String value)
/*      */     throws ServiceException
/*      */   {
/* 3350 */     String displayValue = value;
/* 3351 */     long flags = getConfigFlags(key);
/* 3352 */     boolean isPassword = (flags & 0x80) != 0L;
/* 3353 */     if (isPassword)
/*      */     {
/* 3355 */       displayValue = "<obscured>";
/*      */     }
/* 3357 */     Report.trace("install", file + ":" + key + "->" + displayValue, null);
/*      */     try
/*      */     {
/* 3361 */       String filePath = computeDestination(file);
/* 3362 */       if (FileUtils.checkFile(filePath, true, true) != 0)
/*      */       {
/* 3364 */         FileUtils.touchFile(filePath);
/*      */       }
/*      */ 
/* 3367 */       if ((file.equals("bin/intradoc.cfg")) || (file.equals("config/config.cfg")))
/*      */       {
/* 3369 */         Properties props = new Properties();
/* 3370 */         if (value != null)
/*      */         {
/* 3372 */           props.put(key, value);
/*      */         }
/* 3374 */         String idcCfg = SystemUtils.getCfgFilePath();
/* 3375 */         if (FileUtils.checkFile(idcCfg, 1) != 0)
/*      */         {
/* 3377 */           idcCfg = computeDestination("bin/intradoc.cfg");
/*      */         }
/* 3379 */         SystemPropertiesEditor editor = new SystemPropertiesEditor(idcCfg);
/* 3380 */         Map args = new HashMap();
/* 3381 */         args.put("PasswordScope", "system");
/* 3382 */         editor.initIdc();
/* 3383 */         String masterIdcCfg = computeDestinationEx("bin/intradoc.cfg", false);
/* 3384 */         if (FileUtils.checkFile(masterIdcCfg, 1) == 0)
/*      */         {
/* 3386 */           idcCfg = masterIdcCfg;
/*      */         }
/* 3388 */         editor.setFilepaths(idcCfg, computeDestinationEx("config/config.cfg", false));
/*      */ 
/* 3390 */         if (file.equals("config/config.cfg"))
/*      */         {
/* 3392 */           editor.loadProperties();
/* 3393 */           if (value != null)
/*      */           {
/* 3395 */             CryptoPasswordUtils.extractAndUpdatePasswords(props, editor.getCfgFile(), args);
/* 3396 */             editor.mergePropertyValuesEx(null, props, true);
/*      */           }
/*      */           else
/*      */           {
/* 3400 */             editor.removePropertyValues(null, new String[] { key });
/*      */           }
/* 3402 */           editor.saveConfig();
/*      */         }
/*      */         else
/*      */         {
/* 3406 */           if (value != null)
/*      */           {
/* 3408 */             CryptoPasswordUtils.extractAndUpdatePasswords(props, editor.getIdcFile(), args);
/* 3409 */             editor.mergePropertyValuesEx(props, null, true);
/*      */           }
/*      */           else
/*      */           {
/* 3413 */             editor.removePropertyValues(new String[] { key }, null);
/*      */           }
/* 3415 */           editor.saveIdc();
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 3420 */         Properties props = new Properties();
/* 3421 */         Vector v1 = new IdcVector();
/* 3422 */         Vector v2 = new IdcVector();
/* 3423 */         SystemPropertiesEditor.readFile(props, v1, v2, filePath, null);
/*      */ 
/* 3425 */         OutputStream output = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(filePath, null));
/*      */ 
/* 3427 */         editConfigEntry(props, v1, v2, key, value);
/* 3428 */         SystemPropertiesEditor.writeFile(props, v1, v2, output, null);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 3433 */       throw new ServiceException(e, "csInstallUnableToEditConfigFile", new Object[] { file });
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 3437 */       throw new ServiceException(e, "csInstallUnableToEditConfigFile", new Object[] { file });
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 3441 */       throw new ServiceException(e, "csInstallUnableToEditConfigFile", new Object[] { file });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void editConfigEntry(Properties props, Vector v1, Vector v2, String key, String value)
/*      */   {
/* 3447 */     boolean exists = checkVector(key, v1);
/* 3448 */     boolean extra = checkVector(key, v2);
/*      */ 
/* 3450 */     if ((!exists) && (!extra) && (value != null))
/*      */     {
/* 3452 */       props.put(key, value);
/* 3453 */       v1.addElement(key);
/*      */     } else {
/* 3455 */       if (!extra)
/*      */         return;
/* 3457 */       int length = v2.size();
/* 3458 */       for (int i = 0; i < length; ++i)
/*      */       {
/* 3460 */         String s = (String)v2.elementAt(i);
/* 3461 */         if (!s.startsWith(key))
/*      */           continue;
/* 3463 */         if (value != null)
/*      */         {
/* 3465 */           v2.setElementAt(key + "=" + value, i); return;
/*      */         }
/*      */ 
/* 3469 */         v2.removeElementAt(i);
/*      */ 
/* 3471 */         return;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareForLocks(String dir)
/*      */     throws ServiceException
/*      */   {
/* 3485 */     String dirPath = computeDestinationEx(dir, false);
/* 3486 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dirPath, 99, true);
/*      */   }
/*      */ 
/*      */   public void configureUserLocale()
/*      */     throws DataException, ServiceException
/*      */   {
/* 3493 */     String locale = getInstallValue("UserLocale", null);
/* 3494 */     locale = this.m_installerConfig.getProperty("UserLocale");
/*      */ 
/* 3496 */     if (locale == null)
/*      */       return;
/* 3498 */     IdcLocale userLocale = LocaleResources.getLocale(locale);
/* 3499 */     if (userLocale == null)
/*      */       return;
/* 3501 */     LocaleResources.initSystemLocale(locale);
/*      */   }
/*      */ 
/*      */   public boolean isRefineryInstall()
/*      */   {
/* 3508 */     boolean isRefineryInstall = StringUtils.convertToBool(this.m_installerConfig.getProperty("IsRefineryInstall"), false);
/*      */ 
/* 3510 */     if (!isRefineryInstall)
/*      */     {
/* 3513 */       String srcDir = getInstallValue("SourceDirectory", null);
/* 3514 */       Properties tmpProps = new Properties();
/* 3515 */       srcDir = FileUtils.directorySlashes(srcDir) + "intradoc.cfg";
/*      */       try
/*      */       {
/* 3518 */         FileUtils.loadProperties(tmpProps, srcDir);
/*      */       }
/*      */       catch (IOException ioexp)
/*      */       {
/* 3522 */         Report.trace("install", "Cannot load intradoc.cfg from " + srcDir, ioexp);
/*      */       }
/*      */ 
/* 3525 */       isRefineryInstall = StringUtils.convertToBool(tmpProps.getProperty("IsRefineryInstall"), false);
/*      */ 
/* 3527 */       this.m_installerConfig.put("IsRefinery", (isRefineryInstall) ? "1" : "0");
/*      */     }
/* 3529 */     return isRefineryInstall;
/*      */   }
/*      */ 
/*      */   public boolean checkConfigureAdminServer(int flags)
/*      */   {
/* 3549 */     String conf = getInstallValue("ConfigureAdminServer", "false");
/* 3550 */     if ((flags & 0x1) != 0)
/*      */     {
/* 3552 */       return (conf == null) || (StringUtils.convertToBool(conf, false));
/*      */     }
/* 3554 */     if ((flags & 0x2) != 0)
/*      */     {
/* 3556 */       return conf.equals("configure_existing");
/*      */     }
/* 3558 */     return false;
/*      */   }
/*      */ 
/*      */   public static Vector parseArray(String text, char c1, char c2)
/*      */   {
/* 3563 */     return StringUtils.parseArrayEx(text, c1, c2, true);
/*      */   }
/*      */ 
/*      */   protected boolean checkVector(String key, Vector v)
/*      */   {
/* 3568 */     int length = v.size();
/* 3569 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 3571 */       String s = (String)v.elementAt(i);
/* 3572 */       if (s.startsWith(key))
/*      */       {
/* 3574 */         return true;
/*      */       }
/*      */     }
/* 3577 */     return false;
/*      */   }
/*      */ 
/*      */   public static Vector getList(Properties props, String key)
/*      */   {
/* 3582 */     String listString = props.getProperty(key);
/* 3583 */     Vector list = null;
/*      */ 
/* 3585 */     if (listString != null)
/*      */     {
/* 3588 */       if (listString.equals("null"))
/*      */       {
/* 3590 */         list = new IdcVector();
/*      */       }
/*      */       else
/*      */       {
/* 3594 */         list = parseArray(listString, ',', '^');
/*      */       }
/*      */     }
/*      */ 
/* 3598 */     return list;
/*      */   }
/*      */ 
/*      */   public static String[] tokenizeVersion(String text)
/*      */   {
/* 3603 */     int index = text.indexOf(" ");
/* 3604 */     if (index > 0)
/*      */     {
/* 3606 */       text = text.substring(0, index);
/*      */     }
/* 3608 */     Vector list = new IdcVector();
/* 3609 */     while ((index = text.indexOf(".")) >= 0)
/*      */     {
/* 3611 */       String tmp = text.substring(0, index);
/* 3612 */       list.addElement(tmp);
/* 3613 */       text = text.substring(index + 1);
/*      */     }
/*      */ 
/* 3616 */     if (list.size() > 0)
/*      */     {
/* 3618 */       String[] rc = new String[list.size()];
/* 3619 */       list.copyInto(rc);
/* 3620 */       return rc;
/*      */     }
/* 3622 */     return null;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int compareVersions(String[] lop, String[] rop)
/*      */   {
/* 3629 */     int length = Math.min(lop.length, rop.length);
/* 3630 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 3632 */       String l = lop[i];
/* 3633 */       String r = rop[i];
/* 3634 */       int rc = compareVersions(l, r);
/* 3635 */       if (rc != 0)
/*      */       {
/* 3637 */         return rc;
/*      */       }
/*      */     }
/*      */ 
/* 3641 */     return lop.length - rop.length;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int compareVersions(String lop, String rop)
/*      */   {
/* 3648 */     lop = cleanVersion(lop);
/* 3649 */     rop = cleanVersion(rop);
/*      */ 
/* 3651 */     int rc = lop.length() - rop.length();
/* 3652 */     while ((rc == 0) && 
/* 3654 */       (lop.length() > 0) && ((rc = lop.charAt(0) - rop.charAt(0)) == 0))
/*      */     {
/* 3656 */       lop = lop.substring(1);
/* 3657 */       rop = rop.substring(1);
/*      */     }
/*      */ 
/* 3660 */     return rc;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String cleanVersion(String version)
/*      */   {
/* 3667 */     while ((version.length() > 0) && (((version.charAt(0) == ' ') || (version.charAt(0) == '0'))))
/*      */     {
/* 3669 */       version = version.substring(1);
/*      */     }
/*      */ 
/* 3672 */     return version;
/*      */   }
/*      */ 
/*      */   public static FileOutputStream openFileForOutput(String name)
/*      */     throws IOException
/*      */   {
/*      */     FileOutputStream out;
/*      */     try
/*      */     {
/* 3681 */       File file = new File(name);
/* 3682 */       if (file.exists())
/*      */       {
/* 3684 */         file.delete();
/*      */       }
/* 3686 */       out = new FileOutputStream(name);
/*      */     }
/*      */     catch (FileNotFoundException ignore)
/*      */     {
/* 3692 */       File oldFile = new File(name + ".old");
/* 3693 */       File newFile = new File(name);
/*      */ 
/* 3695 */       FileUtils.deleteFile(name + ".old");
/* 3696 */       newFile.renameTo(oldFile);
/* 3697 */       out = new FileOutputStream(name);
/*      */     }
/*      */ 
/* 3700 */     return out;
/*      */   }
/*      */ 
/*      */   public long getConfigFlags(String key)
/*      */   {
/* 3705 */     long flags = 0L;
/* 3706 */     Properties entryInfo = getInstallerTable("ConfigEntries", key);
/* 3707 */     if (entryInfo != null)
/*      */     {
/* 3709 */       String tmp = entryInfo.getProperty("Flags");
/* 3710 */       flags |= parseFlags(tmp, new Properties());
/*      */     }
/* 3712 */     Properties promptInfo = getInstallerTable("InstallPrompts", key);
/* 3713 */     if (promptInfo != null)
/*      */     {
/* 3715 */       String tmp = promptInfo.getProperty("Flags");
/* 3716 */       flags |= parseFlags(tmp, new Properties());
/*      */     }
/* 3718 */     return flags;
/*      */   }
/*      */ 
/*      */   public String getSharedDir()
/*      */   {
/* 3725 */     if (!isOlderVersion("7.3"))
/*      */     {
/* 3727 */       return null;
/*      */     }
/*      */ 
/* 3730 */     String sharedDir = null;
/* 3731 */     sharedDir = getConfigValue("SharedDir");
/* 3732 */     if (sharedDir == null)
/*      */     {
/* 3734 */       sharedDir = this.m_idcDir + "/shared";
/*      */     }
/* 3736 */     return FileUtils.directorySlashes(sharedDir);
/*      */   }
/*      */ 
/*      */   public void addLocaleOptions(Vector options)
/*      */   {
/* 3741 */     IdcLocale locale = (IdcLocale)LocaleResources.m_defaultContext.getLocaleResource(0);
/*      */ 
/* 3743 */     if (locale == null)
/*      */       return;
/* 3745 */     options.addElement("--set-UserLocale=" + locale.m_name);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void sleep(int millis)
/*      */   {
/* 3755 */     SystemUtils.reportDeprecatedUsage("deprecated method SysInstaller.sleep() called");
/*      */ 
/* 3757 */     SystemUtils.sleep(millis);
/*      */   }
/*      */ 
/*      */   protected static String[][] propertiesAsDoubleArray(Properties props)
/*      */   {
/* 3762 */     Enumeration en = props.keys();
/* 3763 */     Vector v = new IdcVector();
/* 3764 */     while (en.hasMoreElements())
/*      */     {
/* 3766 */       String key = (String)en.nextElement();
/* 3767 */       String value = props.getProperty(key);
/* 3768 */       v.addElement(new String[] { key, value });
/*      */     }
/* 3770 */     String[][] a = new String[v.size()][];
/* 3771 */     v.copyInto(a);
/* 3772 */     return a;
/*      */   }
/*      */ 
/*      */   public String evaluateScript(String script) throws ServiceException
/*      */   {
/* 3777 */     String result = evaluateScript(this.m_pageMerger, script);
/* 3778 */     return result;
/*      */   }
/*      */ 
/*      */   public String evaluateScript(PageMerger merger, String script)
/*      */     throws ServiceException
/*      */   {
/* 3784 */     Exception theException = null;
/* 3785 */     String result = null;
/*      */     try
/*      */     {
/* 3788 */       result = merger.evaluateScriptReportError(script);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 3792 */       theException = e;
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/* 3796 */       theException = e;
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 3800 */       theException = e;
/*      */     }
/* 3802 */     if (theException != null)
/*      */     {
/* 3804 */       throw new ServiceException(theException);
/*      */     }
/* 3806 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean isWindows()
/*      */   {
/* 3812 */     String platform = getInstallValue("Platform", "unknown");
/* 3813 */     if (platform != null)
/*      */     {
/* 3815 */       String family = getInstallerTableValue("PlatformConfigTable", platform, "Family");
/*      */ 
/* 3817 */       if (family != null)
/*      */       {
/* 3819 */         return family.equals("windows");
/*      */       }
/*      */     }
/*      */ 
/* 3823 */     return false;
/*      */   }
/*      */ 
/*      */   public void prepareScript(PathScriptConstructInfo scriptInfo, int flags)
/*      */     throws ServiceException
/*      */   {
/*      */   }
/*      */ 
/*      */   public String executeScript(PathScriptConstructInfo scriptInfo, int flags)
/*      */     throws ServiceException
/*      */   {
/* 3837 */     if (scriptInfo.m_isFunction)
/*      */     {
/* 3839 */       return null;
/*      */     }
/*      */ 
/* 3842 */     String key = scriptInfo.m_coreName;
/* 3843 */     String val = getInstallValue(key, null);
/* 3844 */     if ((val == null) && (this.m_utils != null))
/*      */     {
/* 3846 */       val = this.m_utils.getEnv(key);
/*      */     }
/* 3848 */     if (val != null)
/*      */     {
/* 3850 */       scriptInfo.m_scriptEvaluated = true;
/*      */     }
/* 3852 */     else if (!scriptInfo.m_hasDefault)
/*      */     {
/* 3854 */       String defVal = null;
/* 3855 */       if (key.equals("SharedDir"))
/*      */       {
/* 3857 */         defVal = "shared";
/*      */       }
/* 3859 */       else if ((key.equals("MSDEPassword")) || (key.equals("RandomPassword")))
/*      */       {
/* 3861 */         Random r = new Random(System.currentTimeMillis());
/* 3862 */         byte[] randomBytes = new byte[8];
/* 3863 */         char[] password = new char[randomBytes.length];
/* 3864 */         r.nextBytes(randomBytes);
/* 3865 */         for (int k = 0; k < randomBytes.length; ++k)
/*      */         {
/* 3867 */           int theByte = randomBytes[k] & 0x3F;
/* 3868 */           char theChar = '3';
/* 3869 */           if (theByte < 26)
/*      */           {
/* 3871 */             theChar = (char)(theByte - 0 + 65);
/*      */           }
/* 3873 */           else if (theByte < 52)
/*      */           {
/* 3875 */             theChar = (char)(theByte - 26 + 97);
/*      */           }
/* 3877 */           else if (theByte < 62)
/*      */           {
/* 3879 */             theChar = (char)(theByte - 52 + 48);
/*      */           }
/* 3881 */           else if (theByte == 62)
/*      */           {
/* 3883 */             theChar = '5';
/*      */           }
/* 3885 */           else if (theByte == 63)
/*      */           {
/* 3887 */             theChar = '7';
/*      */           }
/* 3889 */           password[k] = theChar;
/*      */         }
/* 3891 */         defVal = new String(password);
/*      */       }
/* 3893 */       if (defVal != null)
/*      */       {
/* 3895 */         scriptInfo.m_hasDefault = true;
/* 3896 */         scriptInfo.m_tempDefaultValStore = defVal;
/*      */       }
/*      */     }
/* 3899 */     return val;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3906 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97206 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.SysInstaller
 * JD-Core Version:    0.5.4
 */