/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringBufferOutputStream;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.serialize.DataBinderSerializer;
/*      */ import intradoc.server.publish.WebPublishUtils;
/*      */ import intradoc.server.script.ScriptExtensionUtils;
/*      */ import intradoc.server.utils.ComponentInstallHistory;
/*      */ import intradoc.server.utils.ComponentListUtils;
/*      */ import intradoc.server.utils.ServerInstallUtils;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.GenericTracingCallback;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ 
/*      */ public class IdcInstallInfo
/*      */   implements GenericTracingCallback
/*      */ {
/*      */   public static final String AUTO_INSTALL_FILE_NAME = "autoinstall.cfg";
/*      */   public static final String INTRADOC_CFG_KEYS = "IDC_Id,IntradocDir,IdcResourcesDir,IdcNativeDir,IdcHomeDir,VaultDir,WeblayoutDir,UserProfilesDirWebBrowserPath,ComponentDir,SystemComponentDir,IdcServerServletClassPath";
/*   77 */   public static final String[] CAPTURED_APP_SERVER_PROPERTIES = { "ORACLE_COMMON_DIR", "ORACLE_COMMON_MODULES_DIR", "AppServerHome", "DomainHome", "FmwDomainConfigDir", "AppServerJavaHome", "AppServerJavaUse64Bit" };
/*      */   public static final String EXTRA_CFG_PARAMS = "AdditionalEnabledComponents,AutoInstallComplete,DisabledComponents";
/*      */   public boolean m_isAutoInstallDisabled;
/*      */   public boolean m_isStarted;
/*      */   public boolean m_isFinished;
/*      */   public boolean m_isNew;
/*      */   public boolean m_hasAutoInstall;
/*      */   public String m_autoFilePathLoaded;
/*      */   public Properties m_defaultProps;
/*      */   public Properties m_autoInstallProps;
/*      */   public SystemPropertiesEditor m_sysEditor;
/*      */   public String m_idcFile;
/*      */   public String m_backupIdcFile;
/*      */   public boolean m_usedBackupFile;
/*      */   public boolean m_isLogInitialized;
/*      */   public String m_logDir;
/*      */   public String m_tstamp;
/*      */   public String m_logFile;
/*      */   public List<String> m_messages;
/*      */   public boolean m_isInstallDebug;
/*      */   protected List m_idcKeys;
/*      */ 
/*      */   public IdcInstallInfo()
/*      */   {
/*  139 */     this.m_isFinished = true;
/*  140 */     this.m_defaultProps = new IdcProperties();
/*  141 */     this.m_messages = new IdcVector();
/*      */ 
/*  143 */     Date dte = new Date();
/*  144 */     SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd-HHmm");
/*  145 */     this.m_tstamp = frmt.format(dte);
/*  146 */     this.m_isInstallDebug = StringUtils.convertToBool(System.getProperty("idc.install.debug"), false);
/*      */   }
/*      */ 
/*      */   public void checkAndStartInstall()
/*      */     throws DataException
/*      */   {
/*  156 */     String checkConfig = System.getProperty("idc.isCheckConfig");
/*  157 */     String forceNew = System.getProperty("idc.isForceNewInstall");
/*  158 */     boolean allowNewInstall = (IdcInstallDefaults.m_checkForNewInstall) || (StringUtils.convertToBool(checkConfig, false));
/*      */ 
/*  160 */     boolean forceNewInstall = StringUtils.convertToBool(forceNew, false);
/*  161 */     boolean isCheck = (forceNewInstall) || (allowNewInstall);
/*  162 */     if (this.m_isInstallDebug)
/*      */     {
/*  164 */       StringBuilder str = new StringBuilder();
/*  165 */       if (!IdcInstallDefaults.m_checkForNewInstall)
/*      */       {
/*  167 */         str.append("not ");
/*      */       }
/*  169 */       str.append("checking for new install");
/*  170 */       if (StringUtils.convertToBool(System.getProperty("idc.isCheckConfig"), false))
/*      */       {
/*  172 */         str.append(", checking config");
/*      */       }
/*  174 */       if (forceNewInstall)
/*      */       {
/*  176 */         str.append(", forced");
/*      */       }
/*  178 */       if (!isCheck)
/*      */       {
/*  180 */         str.append(", skipping");
/*      */       }
/*  182 */       report("Info", str.toString(), null);
/*      */     }
/*  184 */     if (!isCheck)
/*      */     {
/*  186 */       dumpUninitializedLogMessages();
/*  187 */       return;
/*      */     }
/*      */ 
/*  191 */     this.m_isNew = true;
/*      */     try
/*      */     {
/*  197 */       this.m_isFinished = checkFinish();
/*  198 */       this.m_isStarted = true;
/*  199 */       String binDir = SystemUtils.getBinDir();
/*  200 */       String idcFile = SystemUtils.getCfgFilePath();
/*  201 */       int access = FileUtils.checkFile(idcFile, true, true);
/*  202 */       if (access == 0)
/*      */       {
/*  204 */         this.m_isNew = false;
/*      */       }
/*      */ 
/*  209 */       String configFile = SystemUtils.getClonedSystemProperty("idc.defaultConfigFile");
/*  210 */       if (configFile != null)
/*      */       {
/*  212 */         report("Info", new StringBuilder().append("Loading default configuration from ").append(configFile).toString(), null);
/*  213 */         FileUtils.loadProperties(this.m_defaultProps, configFile);
/*  214 */         report("Info", "Successfully loaded the default configuration.", null);
/*      */       }
/*  216 */       else if (IdcInstallDefaults.m_defaults != null)
/*      */       {
/*  218 */         this.m_defaultProps.putAll(IdcInstallDefaults.m_defaults);
/*      */       }
/*      */ 
/*  221 */       String backupIdcFile = SystemUtils.getClonedSystemProperty("BootstrapIntradocCfgPath");
/*  222 */       boolean backupExists = (backupIdcFile != null) && (FileUtils.checkFile(backupIdcFile, true, true) == 0);
/*  223 */       this.m_idcFile = idcFile;
/*  224 */       this.m_backupIdcFile = backupIdcFile;
/*  225 */       boolean usingBackupFile = (this.m_isNew) && (backupExists);
/*  226 */       String idcFileToOpen = (usingBackupFile) ? backupIdcFile : idcFile;
/*  227 */       if (usingBackupFile)
/*      */       {
/*  229 */         report("Info", new StringBuilder().append("Reading in bootstrap intradoc.cfg at ").append(idcFileToOpen).toString(), null);
/*      */       }
/*  231 */       this.m_sysEditor = new SystemPropertiesEditor(idcFileToOpen);
/*      */ 
/*  235 */       this.m_hasAutoInstall = ((!this.m_isAutoInstallDisabled) && (((loadAutoInstall(binDir)) || (usingBackupFile))));
/*      */ 
/*  237 */       if (this.m_isInstallDebug)
/*      */       {
/*  239 */         StringBuilder str = new StringBuilder();
/*  240 */         str.append((this.m_hasAutoInstall) ? "has" : "no");
/*  241 */         str.append(" autoinstall");
/*  242 */         if (this.m_isAutoInstallDisabled)
/*      */         {
/*  244 */           str.append(", disabled");
/*      */         }
/*  246 */         report("Info", str.toString(), null);
/*      */       }
/*      */ 
/*  250 */       List orderList = this.m_sysEditor.getIdcVector();
/*      */ 
/*  252 */       String intradocDir = this.m_defaultProps.getProperty("IntradocDir");
/*  253 */       if (intradocDir == null)
/*      */       {
/*  255 */         intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*      */       }
/*  257 */       intradocDir = FileUtils.directorySlashes(intradocDir);
/*  258 */       String appServerIdc_Id = this.m_defaultProps.getProperty("IDC_Id");
/*      */ 
/*  260 */       Properties curProps = null;
/*  261 */       Properties idcProps = null;
/*  262 */       boolean updateIdc_Id = false;
/*  263 */       boolean updateIntegrateValues = false;
/*  264 */       boolean writeIntradocCfg = false;
/*  265 */       boolean createNewIntradocCfg = (this.m_hasAutoInstall) || (!this.m_isFinished);
/*  266 */       if ((appServerIdc_Id != null) || (createNewIntradocCfg))
/*      */       {
/*  270 */         if ((!this.m_isNew) || (backupExists))
/*      */         {
/*  272 */           this.m_sysEditor.initIdc();
/*  273 */           curProps = this.m_sysEditor.getIdcProperties();
/*  274 */           String curIntradocDir = curProps.getProperty("IntradocDir");
/*  275 */           if (curIntradocDir != null)
/*      */           {
/*  278 */             intradocDir = curIntradocDir;
/*      */           }
/*  280 */           this.m_usedBackupFile = usingBackupFile;
/*      */         }
/*      */ 
/*  287 */         if ((curProps != null) && (appServerIdc_Id != null))
/*      */         {
/*  290 */           String curIdc_Id = curProps.getProperty("IDC_Id");
/*  291 */           if ((curIdc_Id == null) || (!curIdc_Id.equals(appServerIdc_Id)))
/*      */           {
/*  293 */             updateIdc_Id = true;
/*  294 */             writeIntradocCfg = true;
/*      */           }
/*      */ 
/*  298 */           for (String key : CAPTURED_APP_SERVER_PROPERTIES)
/*      */           {
/*  300 */             String curVal = curProps.getProperty(key);
/*  301 */             String initVal = this.m_defaultProps.getProperty(key);
/*  302 */             if ((initVal == null) || ((curVal != null) && (curVal.equals(initVal))))
/*      */               continue;
/*  304 */             updateIntegrateValues = true;
/*  305 */             writeIntradocCfg = true;
/*  306 */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  312 */       if (createNewIntradocCfg)
/*      */       {
/*  314 */         writeIntradocCfg = true;
/*  315 */         prepareLog(intradocDir);
/*      */ 
/*  318 */         String idcHomeOriginal = this.m_defaultProps.getProperty("IdcHomeDir");
/*  319 */         String idcHome = null;
/*  320 */         if ((idcHomeOriginal == null) && (curProps != null))
/*      */         {
/*  322 */           idcHomeOriginal = curProps.getProperty("IdcHomeDir");
/*      */         }
/*  324 */         if (idcHomeOriginal == null)
/*      */         {
/*  326 */           idcHomeOriginal = LegacyDirectoryLocator.getHomeDirectory();
/*      */         }
/*  328 */         if ((idcHomeOriginal != null) && (idcHomeOriginal.length() > 0))
/*      */         {
/*  330 */           idcHome = FileUtils.directorySlashes(idcHomeOriginal);
/*      */         }
/*      */ 
/*  334 */         intradoc.common.NativeOsUtilsBase.m_IdcHomeDir = idcHome;
/*      */ 
/*  336 */         idcProps = new IdcProperties();
/*  337 */         idcProps.put("IntradocDir", intradocDir);
/*  338 */         if (idcHome != null)
/*      */         {
/*  340 */           idcProps.put("IdcHomeDir", idcHome);
/*      */         }
/*      */ 
/*  343 */         checkBuildIdcKeys();
/*      */ 
/*  345 */         for (int i = 0; i < this.m_idcKeys.size(); ++i)
/*      */         {
/*  347 */           String key = (String)this.m_idcKeys.get(i);
/*  348 */           if (key.equals("IntradocDir")) {
/*      */             continue;
/*      */           }
/*      */ 
/*  352 */           String val = this.m_defaultProps.getProperty(key);
/*  353 */           if (val == null)
/*      */           {
/*  355 */             val = System.getProperty(new StringBuilder().append("idc.").append(key).toString());
/*      */           }
/*      */ 
/*  358 */           if ((val == null) && (curProps != null))
/*      */           {
/*  360 */             val = curProps.getProperty(key);
/*      */           }
/*  362 */           if (val == null)
/*      */             continue;
/*  364 */           idcProps.put(key, val);
/*  365 */           if ((!this.m_isNew) || (orderList.contains(key)))
/*      */             continue;
/*  367 */           orderList.add(key);
/*      */         }
/*      */ 
/*  372 */         report("Info", new StringBuilder().append("Extracted for intradoc.cfg: ").append(idcProps).toString(), null);
/*      */ 
/*  374 */         if (this.m_isNew)
/*      */         {
/*      */           try
/*      */           {
/*  379 */             FileUtils.checkOrCreateDirectory(intradocDir, 1);
/*      */           }
/*      */           catch (ServiceException e)
/*      */           {
/*  383 */             throw new ServiceException(e, new StringBuilder().append("The install directory ").append(intradocDir).append(" is missing or cannot be created.").toString(), new Object[0]);
/*      */           }
/*      */ 
/*  388 */           String dir = SystemUtils.getBinDir();
/*  389 */           FileUtils.checkOrCreateDirectory(dir, 1);
/*      */         }
/*      */       }
/*      */ 
/*  393 */       if ((idcProps == null) && (curProps != null))
/*      */       {
/*  396 */         idcProps = curProps;
/*  397 */         if (writeIntradocCfg)
/*      */         {
/*  401 */           prepareLog(intradocDir);
/*      */ 
/*  403 */           if (updateIdc_Id)
/*      */           {
/*  405 */             report("Info", new StringBuilder().append("Forcing IDC_Id to be ").append(appServerIdc_Id).toString(), null);
/*  406 */             idcProps.put("IDC_Id", appServerIdc_Id);
/*      */           }
/*  408 */           if (updateIntegrateValues)
/*      */           {
/*  411 */             for (String key : CAPTURED_APP_SERVER_PROPERTIES)
/*      */             {
/*  413 */               String initVal = this.m_defaultProps.getProperty(key);
/*  414 */               if (initVal == null)
/*      */                 continue;
/*  416 */               report("Info", new StringBuilder().append("Setting ").append(key).append(" to be ").append(initVal).toString(), null);
/*  417 */               idcProps.put(key, initVal);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  424 */       if (writeIntradocCfg)
/*      */       {
/*  427 */         prepareLog(intradocDir);
/*      */ 
/*  430 */         if (this.m_isNew)
/*      */         {
/*  432 */           this.m_sysEditor.writePropertiesEx(idcProps, orderList, null, idcFile, null, null, false);
/*      */ 
/*  434 */           report("Info", "Created new intradoc.cfg", null);
/*      */         }
/*      */         else
/*      */         {
/*  438 */           this.m_sysEditor.mergePropertyValuesEx(idcProps, null, true);
/*  439 */           this.m_sysEditor.saveIdc();
/*  440 */           report("Info", "Updated the existing intradoc.cfg", null);
/*      */         }
/*      */       }
/*      */ 
/*  444 */       if (idcProps != null)
/*      */       {
/*  447 */         SystemUtils.setAppProperties(idcProps);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  453 */       throw new DataException(t, "Unable to write intradoc.cfg.", new Object[0]);
/*      */     }
/*      */     finally
/*      */     {
/*  457 */       dumpUninitializedLogMessages();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkBuildIdcKeys()
/*      */   {
/*  463 */     if (this.m_idcKeys != null)
/*      */     {
/*  465 */       return;
/*      */     }
/*  467 */     String dirStr = this.m_defaultProps.getProperty("IdcPropertyKeys");
/*  468 */     if (dirStr == null)
/*      */     {
/*  470 */       dirStr = "IDC_Id,IntradocDir,IdcResourcesDir,IdcNativeDir,IdcHomeDir,VaultDir,WeblayoutDir,UserProfilesDirWebBrowserPath,ComponentDir,SystemComponentDir,IdcServerServletClassPath";
/*      */     }
/*  472 */     this.m_idcKeys = StringUtils.makeListFromSequenceSimple(dirStr);
/*  473 */     for (String key : CAPTURED_APP_SERVER_PROPERTIES)
/*      */     {
/*  475 */       this.m_idcKeys.add(key);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean loadAutoInstall(String binDir) throws IOException
/*      */   {
/*  481 */     boolean hasAutoInstall = false;
/*  482 */     String autoFile = FileUtils.getAbsolutePath(binDir, "autoinstall.cfg");
/*  483 */     int access = FileUtils.checkFile(autoFile, true, true);
/*  484 */     if (access == 0)
/*      */     {
/*  486 */       Properties props = new IdcProperties();
/*  487 */       report("Info", new StringBuilder().append("Loading auto install configuration from ").append(autoFile).toString(), null);
/*  488 */       FileUtils.loadProperties(props, autoFile);
/*  489 */       report("Info", "Successfully loaded the default configuration.", null);
/*  490 */       this.m_autoInstallProps = props;
/*      */ 
/*  494 */       Set keys = props.keySet();
/*  495 */       for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/*  497 */         String key = (String)obj;
/*  498 */         String val = props.getProperty(key);
/*  499 */         this.m_defaultProps.put(key, val); }
/*      */ 
/*  501 */       hasAutoInstall = true;
/*  502 */       this.m_autoFilePathLoaded = autoFile;
/*      */     }
/*  504 */     return hasAutoInstall;
/*      */   }
/*      */ 
/*      */   public void continueEarlyInstall() throws DataException
/*      */   {
/*  509 */     if (this.m_isInstallDebug)
/*      */     {
/*  511 */       StringBuilder str = new StringBuilder("auto-install process ");
/*  512 */       if (!this.m_isStarted)
/*      */       {
/*  514 */         str.append("not ");
/*      */       }
/*  516 */       str.append("started, ");
/*  517 */       if (!IdcInstallDefaults.m_allowFullNewInstall)
/*      */       {
/*  519 */         str.append("now ");
/*      */       }
/*  521 */       str.append("allowing full new install");
/*  522 */       report("Info", str.toString(), null);
/*      */     }
/*  524 */     if ((!this.m_isStarted) || (!IdcInstallDefaults.m_allowFullNewInstall))
/*      */     {
/*  527 */       return;
/*      */     }
/*      */ 
/*  531 */     DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*      */ 
/*  534 */     this.m_logDir = new StringBuilder().append(LegacyDirectoryLocator.getIntradocDir()).append("install/").toString();
/*      */ 
/*  539 */     if (!checkFinish())
/*      */     {
/*  541 */       this.m_isFinished = false;
/*      */     }
/*      */     try
/*      */     {
/*  545 */       if ((!this.m_isFinished) || (this.m_hasAutoInstall))
/*      */       {
/*  548 */         createConfigAndPasswordFiles();
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  553 */       report("error", "Unable to continue the install.", t);
/*  554 */       throw new DataException(t, "Install aborted.", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createConfigAndPasswordFiles()
/*      */     throws DataException, ServiceException
/*      */   {
/*  561 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  562 */     String configDir = LegacyDirectoryLocator.getConfigDirectory();
/*  563 */     report("Info", new StringBuilder().append("Creating config directory ").append(configDir).toString(), null);
/*  564 */     FileUtils.checkOrCreateDirectory(configDir, 2);
/*      */ 
/*  567 */     report("Info", "Extracting config information from defaults", null);
/*  568 */     Properties cfgProps = createConfigProperties();
/*      */ 
/*  570 */     if (!this.m_isFinished)
/*      */     {
/*  573 */       buildPasswordInfo(intradocDir, configDir, DirectoryLocator.getResourcesDirectory());
/*  574 */       if (!Report.m_needsInit)
/*      */       {
/*  576 */         report("Info", new StringBuilder().append("Install logic building password storage for configuration directory ").append(configDir).toString(), null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  581 */     String cfgFile = new StringBuilder().append(configDir).append("config.cfg").toString();
/*  582 */     report("Info", new StringBuilder().append("Creating config.cfg file ").append(cfgFile).toString(), null);
/*  583 */     Map args = new HashMap();
/*  584 */     args.put("PasswordScope", "system");
/*      */ 
/*  586 */     report("Info", new StringBuilder().append("Extracting passwords from ").append(cfgFile).toString(), null);
/*  587 */     CryptoPasswordUtils.extractAndUpdatePasswords(cfgProps, cfgFile, args);
/*      */ 
/*  589 */     this.m_sysEditor.mergePropertyValuesEx(null, cfgProps, true);
/*  590 */     if (this.m_sysEditor.getConfigWritable())
/*      */     {
/*  592 */       report("Info", "Updating the existing config.cfg", null);
/*  593 */       if (this.m_hasAutoInstall)
/*      */       {
/*  595 */         this.m_sysEditor.getCfgProperties().remove("IsProvisionalServer");
/*      */       }
/*  597 */       this.m_sysEditor.saveConfig();
/*      */     }
/*      */     else
/*      */     {
/*  601 */       report("Info", "Creating new config.cfg", null);
/*  602 */       List orderList = this.m_sysEditor.getCfgVector();
/*  603 */       if (!this.m_hasAutoInstall)
/*      */       {
/*  605 */         cfgProps.put("IsProvisionalServer", "true");
/*      */       }
/*  607 */       this.m_sysEditor.writePropertiesEx(cfgProps, orderList, null, cfgFile, null, null, false);
/*      */     }
/*  609 */     report("Info", new StringBuilder().append("Successfully wrote config.cfg file ").append(cfgFile).toString(), null);
/*      */   }
/*      */ 
/*      */   public void finishInstall()
/*      */     throws DataException, ServiceException
/*      */   {
/*  616 */     finishAutoInstall();
/*      */ 
/*  620 */     checkAndCreateIndexWeblayoutPage();
/*      */   }
/*      */ 
/*      */   public void finishAutoInstall() throws DataException, ServiceException
/*      */   {
/*  625 */     if (!this.m_hasAutoInstall) {
/*      */       return;
/*      */     }
/*  628 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/*  629 */     ServerInstallUtils installUtils = new ServerInstallUtils(cxt);
/*      */ 
/*  633 */     Map args = new HashMap();
/*  634 */     args.put("IntradocDir", LegacyDirectoryLocator.getIntradocDir());
/*  635 */     args.put("NoCfgActivity", "1");
/*  636 */     installUtils.constructInstallerWithArgs(args);
/*  637 */     installUtils.m_installerConfig.put("InstallConfiguration", "PublishStartupEnvironment");
/*  638 */     installUtils.doInstall();
/*      */ 
/*  643 */     String newPath = new StringBuilder().append(this.m_logDir).append(this.m_tstamp).append("_").append("autoinstall.cfg").toString();
/*  644 */     if (this.m_autoFilePathLoaded != null)
/*      */     {
/*  646 */       FileUtils.renameFileEx(this.m_autoFilePathLoaded, newPath, 8);
/*      */     }
/*  648 */     if ((this.m_backupIdcFile != null) && (this.m_usedBackupFile))
/*      */     {
/*  650 */       String name = FileUtils.getName(this.m_backupIdcFile);
/*  651 */       String logDestination = new StringBuilder().append(this.m_logDir).append(this.m_tstamp).append("_").append(name).toString();
/*  652 */       FileUtils.copyFile(this.m_backupIdcFile, logDestination);
/*      */     }
/*      */ 
/*  655 */     boolean isComplete = StringUtils.convertToBool(this.m_defaultProps.getProperty("AutoInstallComplete"), false);
/*      */ 
/*  657 */     if (!isComplete) {
/*      */       return;
/*      */     }
/*  660 */     String dir = new StringBuilder().append(LegacyDirectoryLocator.getIntradocDir()).append("install/").toString();
/*  661 */     DataBinder binder = new DataBinder();
/*  662 */     binder.setLocalData(this.m_defaultProps);
/*  663 */     ResourceUtils.serializeDataBinder(dir, "installconf.hda", binder, true, false);
/*      */ 
/*  665 */     SharedObjects.putEnvironmentValue("ForceSystemConfigPage", "0");
/*      */   }
/*      */ 
/*      */   public void checkAndCreateIndexWeblayoutPage()
/*      */     throws DataException, ServiceException
/*      */   {
/*  673 */     boolean isCreate = SharedObjects.getEnvValueAsBoolean("IsCheckAndCreateIndexPage", true);
/*  674 */     if (!isCreate)
/*      */     {
/*  676 */       return;
/*      */     }
/*      */ 
/*  680 */     String appDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  681 */     String filename = new StringBuilder().append(appDir).append("pages/index.hda").toString();
/*  682 */     int r = FileUtils.checkFile(filename, true, false);
/*  683 */     if (r != -16) {
/*      */       return;
/*      */     }
/*  686 */     DataBinder binder = new DataBinder();
/*  687 */     PageMerger pageMerger = ScriptExtensionUtils.getOrCreatePageMerger(binder, new ExecutionContextAdaptor());
/*      */ 
/*  689 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(new StringBuilder().append(appDir).append("pages").toString(), 2, true);
/*      */     try
/*      */     {
/*  692 */       String str = pageMerger.evaluateResourceInclude("std_create_index_page");
/*  693 */       BufferedWriter bw = FileUtils.openDataWriter(filename);
/*  694 */       bw.append(str);
/*  695 */       bw.flush();
/*  696 */       FileUtils.closeObject(bw);
/*      */ 
/*  698 */       report("Info", "Successfully built web page index.hda.", null);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  702 */       Report.warning("install", t, "csErrorBuildingIndexPage", new Object[] { filename });
/*      */     }
/*      */   }
/*      */ 
/*      */   public Properties createConfigProperties()
/*      */   {
/*  709 */     Properties cfgProps = null;
/*  710 */     Properties propsToUpdate = new IdcProperties();
/*  711 */     if (IdcInstallDefaults.m_extraConfigFileValues != null)
/*      */     {
/*  713 */       propsToUpdate.putAll(IdcInstallDefaults.m_extraConfigFileValues);
/*      */     }
/*      */ 
/*  716 */     boolean cfgExists = false;
/*  717 */     if ((!this.m_isNew) || (this.m_usedBackupFile))
/*      */     {
/*      */       try
/*      */       {
/*  727 */         this.m_sysEditor.initConfig();
/*  728 */         cfgProps = (Properties)this.m_sysEditor.getCfgProperties().clone();
/*  729 */         cfgExists = true;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  733 */         report("Warning", "The config.cfg file was not found even though the intradoc.cfgfile exists. This may be indicate a problem.", e);
/*      */       }
/*      */     }
/*      */ 
/*  737 */     if (cfgProps == null)
/*      */     {
/*  739 */       cfgProps = new IdcProperties();
/*      */     }
/*      */ 
/*  742 */     propsToUpdate.putAll(this.m_defaultProps);
/*      */ 
/*  745 */     if (propsToUpdate.get("FileEncoding") == null)
/*      */     {
/*  747 */       propsToUpdate.put("FileEncoding", "UTF8");
/*      */     }
/*      */ 
/*  751 */     List orderList = this.m_sysEditor.getCfgVector();
/*  752 */     List extraConfigKeys = StringUtils.makeListFromSequenceSimple("AdditionalEnabledComponents,AutoInstallComplete,DisabledComponents");
/*      */ 
/*  758 */     checkBuildIdcKeys();
/*      */ 
/*  760 */     Set keys = propsToUpdate.keySet();
/*  761 */     for (Iterator i$ = keys.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*      */ 
/*  763 */       String key = (String)obj;
/*      */ 
/*  766 */       if (this.m_idcKeys.contains(key)) continue; if (extraConfigKeys.contains(key))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  771 */       String val = propsToUpdate.getProperty(key);
/*      */ 
/*  774 */       if (((this.m_autoInstallProps != null) && (this.m_autoInstallProps.getProperty(key) != null)) || (cfgProps.getProperty(key) == null))
/*      */       {
/*  777 */         cfgProps.put(key, val);
/*  778 */         if ((!cfgExists) && (!orderList.contains(key)))
/*      */         {
/*  780 */           orderList.add(key);
/*      */         }
/*      */       } }
/*      */ 
/*      */ 
/*  785 */     return cfgProps;
/*      */   }
/*      */ 
/*      */   public void finishEarlyInstall() throws ServiceException, DataException
/*      */   {
/*  790 */     if (((this.m_isFinished) && (!this.m_hasAutoInstall)) || (!IdcInstallDefaults.m_allowFullNewInstall))
/*      */     {
/*  792 */       if (!IdcInstallDefaults.m_allowFullNewInstall)
/*      */       {
/*  794 */         report("Info", "finished early install, not allowing full new install", null);
/*      */       }
/*  796 */       return;
/*      */     }
/*      */ 
/*  802 */     String pubDir = new StringBuilder().append(LegacyDirectoryLocator.getAppDataDirectory()).append("publish/").toString();
/*  803 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(pubDir, 2, true);
/*  804 */     WebPublishUtils.setPublishEverythingAtStartup(null);
/*      */ 
/*  810 */     String path = new StringBuilder().append(this.m_logDir).append("finish.dat").toString();
/*  811 */     FileUtils.touchFile(path);
/*  812 */     report("Info", "The intradoc.cfg and config.cfg files have been created. Completing the early part of the install.", null);
/*      */   }
/*      */ 
/*      */   public void buildPasswordInfo(String idcDir, String cfgDir, String resourcesDir)
/*      */     throws ServiceException
/*      */   {
/*  819 */     Properties env = new IdcProperties();
/*  820 */     env.put("IntradocDir", idcDir);
/*  821 */     env.put("ConfigDir", cfgDir);
/*  822 */     CryptoPasswordUtils.setEnvironment(env);
/*      */ 
/*  824 */     report("Info", "Preparing to build password info", null);
/*  825 */     ResourceContainer rc = new ResourceContainer();
/*  826 */     String resourceFile = FileUtils.getAbsolutePath(resourcesDir, "core/tables/std_resources.htm");
/*  827 */     ResourceLoader.loadResourceFile(rc, resourceFile);
/*  828 */     Table table = (Table)rc.m_tables.get("SecurityCategories");
/*      */     DataResultSet securityCategories;
/*  831 */     if (table != null)
/*      */     {
/*  833 */       DataResultSet securityCategories = new DataResultSet();
/*  834 */       securityCategories.init(table);
/*      */     }
/*      */     else
/*      */     {
/*  839 */       securityCategories = new DataResultSet(new String[] { "scCategory", "scCategoryField", "scCategoryEncodingField", "scExtraEncoding" });
/*      */ 
/*  842 */       List row = StringUtils.makeListFromSequenceSimple("db,JdbcPassword,JdbcPasswordEncoding,");
/*      */ 
/*  844 */       securityCategories.addRowWithList(row);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  849 */       report("Info", "Loading password management.", null);
/*  850 */       CryptoPasswordUtils.loadPasswordManagement(securityCategories);
/*  851 */       report("Info", "Updating password keys.", null);
/*      */ 
/*  853 */       if (this.m_hasAutoInstall)
/*      */       {
/*  856 */         Map args = new HashMap();
/*  857 */         CryptoPasswordUtils.updateExpiredKeys(args);
/*  858 */         report("Info", "Updating passwords.", null);
/*  859 */         CryptoPasswordUtils.updateExpiredPasswords(args);
/*  860 */         report("Info", "Successfully updated password information.", null);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  866 */       report("error", "Unable to build password info.", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildComponentList() throws ServiceException, DataException
/*      */   {
/*  872 */     if ((!this.m_isStarted) || (!IdcInstallDefaults.m_allowFullNewInstall))
/*      */     {
/*  874 */       if (this.m_isInstallDebug)
/*      */       {
/*  876 */         StringBuilder str = new StringBuilder();
/*  877 */         str.append("not building component list: ");
/*  878 */         str.append((!this.m_isStarted) ? "auto-install process not started" : "not allowing full new install");
/*  879 */         report("Info", str.toString(), null);
/*      */       }
/*  881 */       return;
/*      */     }
/*      */ 
/*  884 */     report("Info", "Preparing to build component listing file.", null);
/*      */ 
/*  886 */     String homeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*  887 */     homeDir = FileUtils.directorySlashes(homeDir);
/*  888 */     String homeDataDir = new StringBuilder().append(homeDir).append("data/").toString();
/*  889 */     String newDataDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  890 */     File testOne = FileUtilsCfgBuilder.getCfgFile(homeDataDir, null, true);
/*  891 */     File testTwo = FileUtilsCfgBuilder.getCfgFile(newDataDir, null, true);
/*  892 */     if (testOne.equals(testTwo))
/*      */     {
/*  897 */       return;
/*      */     }
/*      */ 
/*  901 */     List fileList = new ArrayList();
/*  902 */     String pListFileName = ComponentListUtils.computeListingFileName();
/*  903 */     fileList.add(new StringBuilder().append(homeDataDir).append("components/").append(pListFileName).toString());
/*  904 */     fileList.add(new StringBuilder().append(homeDataDir).append("components/idc_components.hda").toString());
/*      */ 
/*  906 */     int r = -1;
/*  907 */     String filename = null;
/*  908 */     Iterator i$ = fileList.iterator();
/*      */     do { if (!i$.hasNext()) break label533; String tmp = (String)i$.next();
/*      */ 
/*  910 */       filename = tmp;
/*  911 */       r = FileUtils.checkFile(filename, true, false); }
/*  912 */     while (r != 0);
/*      */ 
/*  916 */     report("Info", new StringBuilder().append("Reading home component listing file ").append(filename).toString(), null);
/*  917 */     DataBinder cmpBinder = ComponentListUtils.readListingFile(filename, null, null);
/*      */ 
/*  920 */     String cmpDir = new StringBuilder().append(newDataDir).append("components/").toString();
/*  921 */     report("Info", new StringBuilder().append("Checking existence of component data directory ").append(cmpDir).toString(), null);
/*  922 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(cmpDir, 2, true);
/*      */ 
/*  924 */     ComponentInstallHistory cmpHistory = new ComponentInstallHistory(this.m_logDir, cmpBinder, this.m_tstamp, this);
/*      */ 
/*  926 */     String cmpFilename = new StringBuilder().append(cmpDir).append(pListFileName).toString();
/*  927 */     r = FileUtils.checkFile(cmpFilename, true, true);
/*  928 */     if (r == -16)
/*      */     {
/*  931 */       cmpHistory.registerAdditionalComponents(cmpBinder, this.m_defaultProps);
/*      */ 
/*  934 */       cmpHistory.disableComponents(cmpBinder, this.m_defaultProps);
/*      */ 
/*  938 */       cmpHistory.enableAdditionalComponents(cmpBinder, this.m_defaultProps);
/*      */ 
/*  942 */       ResourceUtils.serializeDataBinder(cmpDir, pListFileName, cmpBinder, true, false);
/*  943 */       report("Info", "Successfully created component listing file idc_components.hda with info.", null);
/*      */ 
/*  947 */       cmpHistory.m_isChanged = true;
/*      */     }
/*      */     else
/*      */     {
/*  952 */       cmpHistory.compareAndUpdateComponents(cmpDir, this.m_defaultProps);
/*      */ 
/*  954 */       if (cmpHistory.m_isChanged)
/*      */       {
/*  956 */         prepareLog(LegacyDirectoryLocator.getIntradocDir());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  961 */     cmpHistory.m_dir = this.m_logDir;
/*  962 */     cmpHistory.saveLists();
/*      */ 
/*  965 */     if (r == 0)
/*      */       label533: return;
/*  967 */     report("Info", new StringBuilder().append("The home component listing file ").append(filename).append(" does not exist.").toString(), null);
/*      */   }
/*      */ 
/*      */   public void makeBackupIntradocCfg()
/*      */     throws ServiceException
/*      */   {
/*  973 */     if ((this.m_idcFile == null) || (this.m_backupIdcFile == null))
/*      */       return;
/*  975 */     FileUtils.copyFile(this.m_idcFile, this.m_backupIdcFile);
/*  976 */     if (Report.m_needsInit)
/*      */       return;
/*  978 */     Report.trace("install", new StringBuilder().append("Copied ").append(this.m_idcFile).append(" to ").append(this.m_backupIdcFile).toString(), null);
/*      */   }
/*      */ 
/*      */   public boolean checkFinish()
/*      */   {
/*  985 */     String path = new StringBuilder().append(getLogDir()).append("finish.dat").toString();
/*  986 */     int result = FileUtils.checkFile(path, true, false);
/*  987 */     if (SystemUtils.m_verbose)
/*      */     {
/*  989 */       String status = (result == 0) ? "found: " : "not found: ";
/*  990 */       StringBuilder str = new StringBuilder(status);
/*  991 */       str.append(path);
/*  992 */       report("info", str.toString(), null);
/*      */     }
/*  994 */     return result == 0;
/*      */   }
/*      */ 
/*      */   public void prepareLog(String intradocDir) throws ServiceException
/*      */   {
/*  999 */     if (this.m_isLogInitialized) {
/*      */       return;
/*      */     }
/* 1002 */     this.m_logDir = new StringBuilder().append(intradocDir).append("install/").toString();
/* 1003 */     this.m_logFile = new StringBuilder().append(this.m_logDir).append(this.m_tstamp).append("_log.txt").toString();
/* 1004 */     FileUtils.checkOrCreateDirectory(this.m_logDir, 2);
/* 1005 */     this.m_isLogInitialized = true;
/*      */ 
/* 1007 */     List v = this.m_messages;
/* 1008 */     this.m_messages = new IdcVector();
/* 1009 */     int size = v.size();
/* 1010 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1012 */       String msg = (String)v.get(i);
/* 1013 */       report(null, msg, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getLogDir()
/*      */   {
/* 1020 */     String dir = null;
/* 1021 */     if (this.m_logDir != null)
/*      */     {
/* 1023 */       dir = this.m_logDir;
/*      */     }
/*      */     else
/*      */     {
/* 1028 */       dir = new StringBuilder().append(LegacyDirectoryLocator.getIntradocDir()).append("install/").toString();
/*      */     }
/* 1030 */     return dir;
/*      */   }
/*      */ 
/*      */   protected String createMessage(String type, String errMsg, Throwable t)
/*      */   {
/* 1035 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 1036 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/* 1037 */     buf.m_disableToStringReleaseBuffers = true;
/* 1038 */     if (type != null)
/*      */     {
/* 1040 */       buf.append(type);
/* 1041 */       buf.append(": ");
/*      */     }
/* 1043 */     buf.append(errMsg);
/* 1044 */     if (t != null)
/*      */     {
/* 1046 */       PrintStream printStream = new PrintStream(sbos);
/* 1047 */       if (t instanceof StackTrace)
/*      */       {
/* 1049 */         sbos.m_skipUntil = '\t';
/*      */       }
/* 1051 */       t.printStackTrace(printStream);
/*      */     }
/* 1053 */     String msg = buf.toString();
/* 1054 */     buf.releaseBuffers();
/* 1055 */     return msg;
/*      */   }
/*      */ 
/*      */   public void report(String type, String msg, Throwable t)
/*      */   {
/* 1060 */     msg = createMessage(type, msg, t);
/* 1061 */     if (!Report.m_needsInit)
/*      */     {
/* 1063 */       Report.trace("install", msg, t);
/*      */     }
/*      */ 
/* 1066 */     if (!this.m_isLogInitialized)
/*      */     {
/* 1068 */       this.m_messages.add(msg);
/* 1069 */       return;
/*      */     }
/* 1071 */     RandomAccessFile file = null;
/*      */     try
/*      */     {
/* 1074 */       file = new RandomAccessFile(this.m_logFile, "rw");
/* 1075 */       long length = file.length();
/* 1076 */       if (length == 0L)
/*      */       {
/* 1078 */         file.write(FileUtils.UTF8_SIGNATURE);
/*      */       }
/*      */       else
/*      */       {
/* 1082 */         file.seek(length);
/*      */       }
/* 1084 */       msg = new StringBuilder().append(msg).append("\n").toString();
/* 1085 */       byte[] bytes = msg.getBytes("UTF8");
/* 1086 */       file.write(bytes);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1090 */       System.out.println(new StringBuilder().append("Failed to log start up install message: ").append(msg).toString());
/*      */     }
/*      */     finally
/*      */     {
/* 1094 */       FileUtils.closeObject(file);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void report(int level, Object[] args)
/*      */   {
/* 1100 */     String type = GenericTracingCallback.LEVEL_NAMES[level];
/* 1101 */     String msg = (String)args[0];
/* 1102 */     Throwable t = null;
/* 1103 */     if (args.length > 1)
/*      */     {
/* 1105 */       t = (Throwable)args[1];
/*      */     }
/* 1107 */     report(type, msg, t);
/*      */   }
/*      */ 
/*      */   protected void dumpUninitializedLogMessages()
/*      */   {
/* 1112 */     List v = this.m_messages;
/* 1113 */     int size = v.size();
/* 1114 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1116 */       String msg = (String)v.get(i);
/* 1117 */       System.err.println(msg);
/*      */     }
/* 1119 */     this.m_messages = new IdcVector();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1125 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100339 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcInstallInfo
 * JD-Core Version:    0.5.4
 */