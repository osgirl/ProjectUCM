/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ParseStringException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Date;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ManualInstaller extends InteractiveInstaller
/*      */ {
/*   35 */   protected boolean m_isSlave = false;
/*   36 */   protected String m_corePrefix = "core";
/*      */ 
/*      */   public ManualInstaller(Properties installerProps, Properties overrideProps, PromptUser prompter)
/*      */   {
/*   41 */     super(installerProps, overrideProps, prompter);
/*      */   }
/*      */ 
/*      */   public boolean interactWithUser(Vector commandLineArguments)
/*      */     throws DataException, ServiceException
/*      */   {
/*   48 */     boolean isRefineryInstall = this.m_installer.isRefineryInstall();
/*   49 */     setIsRefinery(isRefineryInstall);
/*      */ 
/*   51 */     String proceed = "false";
/*      */ 
/*   53 */     String idcDir = null;
/*   54 */     String installTypesTbl = "InstallTypes";
/*   55 */     String installKey = "InstallType";
/*   56 */     if (isRefineryInstall == true)
/*      */     {
/*   58 */       installTypesTbl = "RefineryInstallTypes";
/*   59 */       installKey = "RefineryInstallTypes";
/*      */     }
/*      */ 
/*   62 */     String[][] types = this.m_installer.getInstallerTableAsArray(installTypesTbl);
/*   63 */     LocaleResources.localizeDoubleArray(types, null, 1);
/*   64 */     String installType = null;
/*   65 */     boolean hasWarnings = false;
/*   66 */     boolean getInstallType = true;
/*   67 */     String database = this.m_installProps.getProperty("DatabaseType");
/*      */ 
/*   69 */     while ((proceed.equals("false")) || (proceed.equals("check")))
/*      */     {
/*   71 */       if (getInstallType)
/*      */       {
/*   73 */         installType = promptUser(installKey, "!csInstallerTypePrompt", "new", types, false);
/*      */ 
/*   75 */         getInstallType = false;
/*      */       }
/*      */ 
/*   78 */       this.m_propList = new IdcVector();
/*   79 */       this.m_reviewPropList = new IdcVector();
/*   80 */       this.m_isUpdate = false;
/*      */ 
/*   82 */       if (installType.equals("update"))
/*      */       {
/*   84 */         this.m_log.notice("!csInstallerUpdateStatusMsg");
/*      */ 
/*   86 */         this.m_isUpdate = true;
/*   87 */         this.m_isSlave = false;
/*      */       }
/*   89 */       else if (installType.equals("proxy"))
/*      */       {
/*   91 */         this.m_log.notice("!csInstallerSlaveStatusMsg");
/*   92 */         this.m_corePrefix = "proxy";
/*      */ 
/*   94 */         this.m_isUpdate = false;
/*   95 */         this.m_isSlave = true;
/*      */         while (true)
/*      */         {
/*   99 */           String mIdcDir = promptUser("MasterServerDir", "!csInstallerMasterServerDirPrompt", "/oracle/ucm/server1", (String[][])null, false);
/*      */ 
/*  101 */           mIdcDir = FileUtils.directorySlashes(mIdcDir);
/*  102 */           String idcCfgFile = mIdcDir + "bin/intradoc.cfg";
/*  103 */           if (FileUtils.checkFile(idcCfgFile, true, false) == 0) {
/*      */             break;
/*      */           }
/*      */ 
/*  107 */           this.m_promptUser.outputMessage(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerConfigFileMustExist", null, FileUtils.getAbsolutePath(idcCfgFile)), null));
/*      */ 
/*  110 */           this.m_installProps.remove("Master Server Directory");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  115 */         this.m_log.notice("!csInstallerMasterStatusMsg");
/*      */       }
/*      */ 
/*  118 */       Properties dirAnswerProps = new Properties();
/*  119 */       if (this.m_isUpdate)
/*      */       {
/*  121 */         boolean autoAccept = true;
/*      */         while (true)
/*      */         {
/*  124 */           idcDir = promptUserEx("IntradocDir", "!csInstallerContentServerCoreDirPrompt", defaultMasterServerDir(), (String[][])null, autoAccept, dirAnswerProps);
/*      */ 
/*  127 */           idcDir = FileUtils.directorySlashes(idcDir);
/*  128 */           String path = idcDir;
/*  129 */           int rc = FileUtils.checkFile(idcDir, false, true);
/*  130 */           if (rc == 0)
/*      */           {
/*  132 */             path = idcDir + "config/config.cfg";
/*  133 */             rc = FileUtils.checkFile(path, true, true);
/*      */           }
/*  135 */           if (rc == 0)
/*      */             break;
/*  137 */           int checkFlags = 3;
/*  138 */           if (rc == 0)
/*      */             break;
/*  140 */           IdcMessage msg = FileUtils.getErrorMsg(path, checkFlags, rc);
/*  141 */           this.m_promptUser.outputMessage("");
/*  142 */           this.m_promptUser.outputMessage("");
/*  143 */           this.m_promptUser.outputMessage(LocaleResources.localizeMessage(null, msg, null).toString());
/*      */         }
/*      */ 
/*  148 */         autoAccept = false;
/*      */       }
/*      */       else
/*      */       {
/*  155 */         idcDir = promptForDirectoryWithProps("IntradocDir", "!csInstallerContentServerCoreDirPrompt", defaultMasterServerDir(), dirAnswerProps);
/*      */       }
/*      */ 
/*  159 */       this.m_installProps.put("TargetDir", idcDir);
/*  160 */       this.m_log.setLogDirectory(idcDir + "install/");
/*      */ 
/*  162 */       if ((installType.equals("new")) || (installType.equals("proxy")))
/*      */       {
/*  164 */         promptForJvmInfo();
/*      */       }
/*      */ 
/*  170 */       this.m_installer = new SysInstaller();
/*  171 */       this.m_installer.init(this.m_binder, this.m_installProps, this.m_overrideProps, this.m_log, this.m_progress, this.m_promptUser);
/*      */ 
/*  173 */       this.m_installer.m_isUpdate = this.m_isUpdate;
/*      */ 
/*  175 */       if (this.m_isUpdate)
/*      */       {
/*      */         try
/*      */         {
/*  179 */           this.m_installer.loadConfig();
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  183 */           throw new ServiceException("!csInstallerInstanceUpdateError", e);
/*      */         }
/*      */ 
/*  189 */         if (this.m_installer.isOlderVersion("6.1.0"))
/*      */         {
/*  191 */           promptForJvmInfo();
/*      */         }
/*      */         else
/*      */         {
/*  195 */           setJvmPath();
/*      */         }
/*      */ 
/*  198 */         String[][] list = { { "VaultDir", this.m_installer.m_idcDir + "vault/" }, { "WeblayoutDir", this.m_installer.m_idcDir + "weblayout/" } };
/*      */ 
/*  204 */         for (int i = 0; i < list.length; ++i)
/*      */         {
/*  206 */           if (this.m_installer.m_intradocConfig.get(list[i][0]) != null)
/*      */             continue;
/*  208 */           this.m_installer.m_intradocConfig.put(list[i][0], list[i][1]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  215 */       queryInstalledServers(idcDir);
/*      */ 
/*  222 */       String alreadyUsedIdcDir = isConfigValueUsed("IntradocDir", idcDir);
/*  223 */       if (this.m_isUpdate)
/*      */       {
/*  225 */         if (alreadyUsedIdcDir == null)
/*      */         {
/*  227 */           Report.trace("install", "the dir '" + idcDir + "' isn't already used but we're updating " + "into it anyway.", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  234 */         String idcCfgFile = idcDir + "bin/intradoc.cfg";
/*  235 */         if (FileUtils.checkFile(idcCfgFile, true, false) == 0)
/*      */         {
/*  237 */           if (!StringUtils.convertToBool(dirAnswerProps.getProperty("UserInteractionRequired"), false))
/*      */           {
/*  240 */             throw new ServiceException("!csInstallerInstanceExists");
/*      */           }
/*  242 */           this.m_promptUser.outputMessage(LocaleResources.localizeMessage("!csInstallerInstanceExists", null));
/*      */ 
/*  244 */           proceed = "false";
/*  245 */           removeProp("IntradocDir");
/*      */         }
/*      */       }
/*      */ 
/*  249 */       promptForDirectoryWithProps("VaultDir", "!csInstallerNativeVaultDirPrompt", idcDir + "vault/", dirAnswerProps);
/*      */ 
/*  251 */       promptForDirectoryWithProps("WeblayoutDir", "!csInstallerWeblayoutDirPrompt", idcDir + "weblayout/", dirAnswerProps);
/*      */ 
/*  254 */       if ((!this.m_isUpdate) || (this.m_installer.isOlderVersion("4")))
/*      */       {
/*  256 */         processInstallationPath("NewServerSettingsPath", true, null);
/*      */       }
/*      */ 
/*  259 */       boolean autoDefault = this.m_isUpdate;
/*      */       while (true)
/*      */       {
/*  262 */         String browserPath = promptUser("WebBrowserPath", "!csInstallerBrowserPathPrompt", defaultWebBrowserPath(), (String[][])null, autoDefault);
/*      */ 
/*  265 */         if ((browserPath != null) && (FileUtils.checkFile(browserPath, true, false) == 0))
/*      */           break;
/*  267 */         removeProp("WebBrowserPath");
/*  268 */         autoDefault = false;
/*      */       }
/*      */ 
/*  285 */       boolean promptForLocaleInfo = (!this.m_isUpdate) || (this.m_installer.isOlderVersion("5.1.1"));
/*  286 */       if (promptForLocaleInfo)
/*      */       {
/*  288 */         String sysLocaleName = promptUser("SystemLocale", "!csInstallerSelectSystemLocale", this.m_userLocale, this.m_localeChoices, false);
/*      */ 
/*  290 */         IdcLocale sysLocale = LocaleResources.getLocale(sysLocaleName);
/*  291 */         if (!checkSystemEncoding(sysLocale))
/*      */         {
/*  293 */           String message = LocaleUtils.encodeMessage("csInstallerIncompatibleEncoding", null, sysLocaleName, FileUtils.m_javaSystemEncoding);
/*      */ 
/*  296 */           this.m_installer.m_installLog.warning(message);
/*      */         }
/*      */ 
/*  299 */         String timeZone = this.m_installer.m_intradocConfig.getProperty("SystemTimeZone");
/*  300 */         if (timeZone == null)
/*      */         {
/*  302 */           timeZone = this.m_installProps.getProperty("SystemTimeZone");
/*      */         }
/*  304 */         if (timeZone == null)
/*      */         {
/*  306 */           String timeZoneRegion = promptUser("TimeZoneRegion", "!csInstallerSelectTimeZoneRegion", this.m_defaultRegion, this.m_regionChoices, false);
/*      */ 
/*  309 */           removeProp("TimeZoneRegion");
/*  310 */           if (!timeZoneRegion.equals("osDefault"))
/*      */           {
/*  312 */             String[][] zones = (String[][])(String[][])this.m_regionTimeZones.get(timeZoneRegion);
/*  313 */             timeZone = promptUser("SystemTimeZone", "!csInstallerSelectTimeZone", this.m_defaultTimeZone, zones, false);
/*      */ 
/*  315 */             SharedObjects.putEnvironmentValue("SystemTimeZone", timeZone);
/*  316 */             SharedObjects.putEnvironmentValue("WarnAboutTimeZone", "0");
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  338 */       if (this.m_installProps.getProperty("FileEncoding") == null)
/*      */       {
/*  340 */         this.m_installProps.put("FileEncoding", "UTF8");
/*      */       }
/*      */ 
/*  344 */       if (!isRefineryInstall)
/*      */       {
/*  352 */         boolean promptForVdk = (this.m_isUpdate) && (((this.m_installer.isOlderVersion("6.2")) || (StringUtils.convertToBool(this.m_installer.m_intradocConfig.getProperty("UseVdkLegacyRebuild"), false))));
/*      */ 
/*  355 */         if (promptForVdk)
/*      */         {
/*  357 */           String result = promptUser("UseVdk4RebuildPrompt", "!csInstallerUseVdkLegacyRebuildPrompt", "true", this.m_yesNoOptions, false);
/*      */ 
/*  360 */           removeProp("UseVdk4RebuildPrompt");
/*  361 */           if (!StringUtils.convertToBool(result, true))
/*      */           {
/*  363 */             setProp("UseVdkLegacyRebuild", "true");
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  368 */       String defPort = "0";
/*  369 */       String defAdminPort = "0";
/*  370 */       if (isRefineryInstall == true)
/*      */       {
/*  372 */         defPort = "0";
/*      */       }
/*  374 */       promptUser("IntradocServerPort", "!csInstallerPortPrompt", defPort, (String[][])null, this.m_isUpdate);
/*      */ 
/*  377 */       String proxiedType = this.m_installer.getInstallValue("ConfigureProxiedServer", "no");
/*  378 */       boolean isProxied = (this.m_installer.getInstallBool("IsProxiedServer", false)) || (StringUtils.convertToBool(proxiedType, true));
/*      */ 
/*  380 */       String adminServerChoice = this.m_installer.getInstallValue("ConfigureAdminServer", "true");
/*      */ 
/*  382 */       boolean configureAdminServer = StringUtils.convertToBool(adminServerChoice, false);
/*  383 */       if ((this.m_isUpdate) || (isProxied) || (!configureAdminServer))
/*      */       {
/*  387 */         setProp("InstallAdminServerService", "false");
/*      */       }
/*  389 */       if (this.m_isUpdate)
/*      */       {
/*  391 */         if (isProxied)
/*      */         {
/*  393 */           setProp("ConfigureAdminServer", "no");
/*  394 */           configureAdminServer = false;
/*      */         }
/*  396 */         setProp("InstallServerService", "false");
/*      */       }
/*      */ 
/*  399 */       if (configureAdminServer)
/*      */       {
/*  401 */         promptUser("IdcAdminServerPort", "!csInstallerAdminServerPortPrompt", defAdminPort, (String[][])null, this.m_isUpdate);
/*      */       }
/*      */ 
/*  405 */       if (!this.m_isUpdate)
/*      */       {
/*  407 */         promptUser("SocketHostAddressSecurityFilter", "!csInstallerSocketHostAddressSecurityFilterPrompt", "127.0.0.1|0:0:0:0:0:0:0:1", (String[][])null, this.m_isUpdate);
/*      */       }
/*      */ 
/*  411 */       String relRoot = promptUser("HttpRelativeWebRoot", "!csInstallerWebRelativeRootPrompt", "/idc/", (String[][])null, this.m_isUpdate);
/*      */ 
/*  413 */       String baseName = trimSlashes(relRoot);
/*      */ 
/*  415 */       if (!isRefineryInstall)
/*      */       {
/*  417 */         promptUser("MailServer", "!csInstallerMailServerPrompt", "mail", (String[][])null, this.m_isUpdate);
/*      */ 
/*  419 */         promptUser("SysAdminAddress", "!csInstallerAdminEMailPrompt", "sysadmin@mail", (String[][])null, this.m_isUpdate);
/*      */       }
/*      */ 
/*  423 */       promptUser("HttpServerAddress", "!csInstallerWebServerAddressPrompt", getHostName(), (String[][])null, this.m_isUpdate);
/*      */ 
/*  428 */       boolean getIdcNameIsUpdate = this.m_isUpdate;
/*      */       while (true)
/*      */       {
/*  431 */         String idcName = promptUser("IDC_Name", "!csInstallerInstanceNamePrompt", baseName, (String[][])null, getIdcNameIsUpdate);
/*      */ 
/*  433 */         if ((StringUtils.urlEncode(idcName).equals(idcName)) && (!idcName.contains(" ")))
/*      */           break;
/*  435 */         String msg = LocaleResources.getString("csIdcNameIllegal", null, idcName);
/*  436 */         this.m_promptUser.outputMessage(msg);
/*  437 */         getIdcNameIsUpdate = false;
/*      */       }
/*      */ 
/*  442 */       promptUser("InstanceMenuLabel", "!csInstallerInstanceLabelPrompt", baseName, (String[][])null, this.m_isUpdate);
/*      */ 
/*  444 */       promptUser("InstanceDescription", "!csInstallerDescPrompt", LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerCSDesc", null, baseName), null), (String[][])null, this.m_isUpdate);
/*      */ 
/*  448 */       if (!this.m_isUpdate)
/*      */       {
/*  450 */         processInstallationPath("ConfigureAdminUser", true, null);
/*      */       }
/*      */ 
/*  453 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("WebServerTable");
/*  454 */       String[][] webServers = createPlatformList(drset, new String[] { "WebServer", "Description" }, this.m_supportedWebServers);
/*      */ 
/*  456 */       int length = webServers.length;
/*  457 */       String[][] finalWebServers = new String[length + 1][];
/*  458 */       System.arraycopy(webServers, 0, finalWebServers, 0, length);
/*  459 */       finalWebServers[length] = { "manual", LocaleResources.getString("csInstallerManualConfigTag", null) };
/*      */ 
/*  461 */       webServers = finalWebServers;
/*  462 */       String webServer = null;
/*      */ 
/*  464 */       for (boolean autoAccept = this.m_isUpdate; ; autoAccept = false)
/*      */       {
/*  466 */         webServer = promptUser("WebServer", "!csInstallerWebServerPrompt", webServers[0][0], webServers, autoAccept);
/*      */ 
/*  469 */         if ((!isRefineryInstall) && (webServer.equals("iis")))
/*      */         {
/*  472 */           if (isProxied)
/*      */             break;
/*  474 */           String msg = LocaleUtils.encodeMessage("csInstallerDisableIISFileHandleCaching", null);
/*      */ 
/*  476 */           promptUser("DisableIISFileHandleCaching", msg, "true", this.m_yesNoOptions, autoAccept);
/*      */ 
/*  479 */           msg = LocaleUtils.encodeMessage("csInstallerSecurityIntegration", null);
/*      */ 
/*  481 */           String[][] windowsSecurityOptions = this.m_installer.getInstallerTableAsArray("WindowsSecurityOptions");
/*      */ 
/*  483 */           LocaleResources.localizeDoubleArray(windowsSecurityOptions, null, 1);
/*  484 */           promptUser("NtlmSecurityEnabled", msg, "no", windowsSecurityOptions, this.m_isUpdate);
/*      */ 
/*  486 */           break;
/*      */         }
/*      */ 
/*  489 */         if ((!webServer.equals("nes")) && (!webServer.equals("nes6")) && (!webServer.equals("sunone"))) {
/*      */           break;
/*      */         }
/*      */ 
/*  493 */         String sunonePath = promptUser("SunOnePath", "!csInstallerSunOneDirPrompt", "/usr/sunone/https-hostname", (String[][])null, autoAccept);
/*      */ 
/*  496 */         autoAccept = false;
/*  497 */         sunonePath = FileUtils.directorySlashes(sunonePath);
/*  498 */         String objConfFile = sunonePath + "config/obj.conf";
/*      */ 
/*  500 */         if (FileUtils.checkFile(objConfFile, true, false) == 0) {
/*      */           break;
/*      */         }
/*      */ 
/*  504 */         this.m_promptUser.outputMessage(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("syFileDoesNotExist", null, FileUtils.getAbsolutePath(objConfFile)), null));
/*      */ 
/*  507 */         removeProp("SunOnePath");
/*      */       }
/*      */ 
/*  515 */       if (!isRefineryInstall)
/*      */       {
/*  517 */         boolean isCreateDatabase = false;
/*  518 */         boolean isPre8 = this.m_installer.isOlderVersion("7.2");
/*  519 */         boolean redoSQL = this.m_installer.getInstallBool("ReconfigureSQLSettings", false);
/*      */ 
/*  521 */         if ((this.m_isUpdate) && (isPre8))
/*      */         {
/*  523 */           String jdbcString = this.m_installer.getConfigValue("JdbcConnectionString");
/*      */ 
/*  525 */           if (jdbcString == null)
/*      */           {
/*  527 */             jdbcString = "none";
/*      */           }
/*  529 */           jdbcString = jdbcString.toLowerCase();
/*  530 */           if ((jdbcString.indexOf("odbc") >= 0) || (jdbcString.indexOf("jtds") >= 0) || (jdbcString.indexOf("sqlserver") >= 0))
/*      */           {
/*  534 */             redoSQL = true;
/*      */           }
/*      */         }
/*      */ 
/*  538 */         boolean isSQLPrompt = true;
/*  539 */         if ((this.m_isUpdate) && (!redoSQL))
/*      */         {
/*  541 */           isSQLPrompt = false;
/*  542 */           String jdbcString = this.m_installer.getConfigValue("JdbcConnectionString");
/*  543 */           database = null;
/*  544 */           if (jdbcString != null)
/*      */           {
/*  546 */             jdbcString = jdbcString.toLowerCase();
/*      */           }
/*      */           else
/*      */           {
/*  550 */             jdbcString = "unknown";
/*      */           }
/*      */ 
/*  553 */           if ((jdbcString.indexOf("jdbc:odbc") >= 0) || (jdbcString.indexOf("jdbc:freetds:sqlserver") >= 0))
/*      */           {
/*  556 */             database = "mssql";
/*      */           }
/*  558 */           else if (jdbcString.indexOf("oracle") >= 0)
/*      */           {
/*  560 */             database = "oracle";
/*      */           }
/*      */           else
/*      */           {
/*  564 */             database = "sybase";
/*      */           }
/*      */ 
/*  567 */           Properties databaseProps = null;
/*  568 */           databaseProps = this.m_installer.getInstallerTable("DatabaseServerTable", database);
/*      */ 
/*  571 */           Properties driverProps = null;
/*  572 */           String jdbcDriver = this.m_installer.getConfigValue("JdbcDriver");
/*  573 */           if ((jdbcDriver == null) && (databaseProps != null))
/*      */           {
/*  575 */             jdbcDriver = databaseProps.getProperty("DefaultJdbcDriver");
/*      */           }
/*      */ 
/*  578 */           if (jdbcDriver != null)
/*      */           {
/*  580 */             driverProps = this.m_installer.getInstallerTable("DatabaseDriverTable", jdbcDriver);
/*      */           }
/*      */ 
/*  587 */           if ((driverProps == null) && (this.m_installer.m_intradocConfig.getProperty("JDBC_JAVA_CLASSPATH_customjdbc") == null))
/*      */           {
/*  591 */             if (jdbcDriver != null);
/*  598 */             isSQLPrompt = true;
/*      */           }
/*  600 */           if (!isSQLPrompt)
/*      */           {
/*  604 */             String customClasspath = this.m_installer.getConfigValue("JDBC_JAVA_CLASSPATH_customjdbc");
/*      */ 
/*  606 */             if ((driverProps == null) && (customClasspath == null))
/*      */             {
/*  608 */               this.m_installer.m_installLog.warning("!csInstallerUnableToFindJDBCDriver");
/*      */             }
/*  611 */             else if (customClasspath != null)
/*      */             {
/*  613 */               this.m_installProps.put("InstallerJdbcClasspath", customClasspath);
/*      */             }
/*      */             else
/*      */             {
/*      */               String cp;
/*  618 */               if (this.m_installer.getInstallBool("IsRuntimeOnly", false))
/*      */               {
/*  620 */                 cp = driverProps.getProperty("ServerClasspath");
/*      */               }
/*      */               else
/*      */               {
/*  624 */                 cp = driverProps.getProperty("InstallerClasspath");
/*      */               }
/*  626 */               String cp = this.m_installer.computeDestinationEx(cp, false);
/*  627 */               this.m_installProps.put("InstallerJdbcClasspath", cp);
/*      */             }
/*      */           }
/*      */         }
/*  631 */         if (isSQLPrompt)
/*      */         {
/*  633 */           DataResultSet databases = (DataResultSet)this.m_binder.getResultSet("DatabaseServerTable");
/*      */ 
/*  636 */           setProp("DisableDatabaseOverrides", "1");
/*  637 */           String[] dbFieldList = { "Database", "Description", "AutoConfigureFlags", "ConfigEntryList", "EnabledScript" };
/*      */ 
/*  644 */           String[][] databaseList = createPlatformList(databases, dbFieldList, this.m_supportedDatabases);
/*      */ 
/*  647 */           for (int i = 0; i < databaseList.length; ++i)
/*      */           {
/*  649 */             if ((databaseList[i][1] != null) && (databaseList[i][1].length() != 0)) {
/*      */               continue;
/*      */             }
/*  652 */             databaseList[i][1] = LocaleResources.getString("csInstallerDatabaseLabel_" + databaseList[i][0], null);
/*      */           }
/*      */ 
/*  657 */           String oldDatabase = database;
/*  658 */           if (database == null)
/*      */           {
/*  660 */             database = databaseList[0][0];
/*      */           }
/*  662 */           database = promptUser("DatabaseType", "!csInstallerDBPrompt", database, databaseList, this.m_isUpdate);
/*      */ 
/*  664 */           checkAbort(database);
/*  665 */           Properties databaseProps = this.m_installer.getInstallerTable("DatabaseServerTable", database);
/*      */ 
/*  668 */           if ((this.m_supportedDatabases.toLowerCase().indexOf(database.toLowerCase()) < 0) || (databaseProps == null))
/*      */           {
/*  671 */             String msg = LocaleUtils.encodeMessage("csInstallerDBNotSupported", null, database, this.m_platform);
/*      */ 
/*  673 */             throw new ServiceException(msg);
/*      */           }
/*      */ 
/*  677 */           if ((oldDatabase != null) && (!oldDatabase.equals(database)))
/*      */           {
/*  679 */             Properties oldDatabaseProps = this.m_installer.getInstallerTable("DatabaseServerTable", database);
/*      */ 
/*  681 */             Vector settings = this.m_installer.parseArrayTrim(oldDatabaseProps, "ConfigFileEntries");
/*      */ 
/*  684 */             settings.addElement("InstallMSDE");
/*  685 */             settings.addElement("InstallerJdbcClasspath");
/*  686 */             Vector settings2 = this.m_installer.parseArrayTrim(oldDatabaseProps, "ConfigEntryList");
/*      */ 
/*  688 */             for (int i = 0; i < settings2.size(); ++i)
/*      */             {
/*  690 */               settings.addElement(settings2.elementAt(i));
/*      */             }
/*      */ 
/*  693 */             for (int i = 0; i < settings.size(); ++i)
/*      */             {
/*  695 */               String setting = (String)settings.elementAt(i);
/*  696 */               Vector v = StringUtils.parseArray(setting, ',', '^');
/*  697 */               if (v.size() < 1)
/*      */                 continue;
/*  699 */               setting = (String)v.elementAt(0);
/*  700 */               Report.trace("install", "removing " + setting, null);
/*  701 */               removeProp(setting);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  706 */           String createDatabase = databaseProps.getProperty("AutoConfigureFlags");
/*      */ 
/*  708 */           if (database.equals("msde"))
/*      */           {
/*  713 */             MSDEInstaller msdeInstaller = new MSDEInstaller();
/*  714 */             msdeInstaller.init(this.m_installer);
/*  715 */             String msdeInstanceName = this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*      */ 
/*  717 */             int status = msdeInstaller.checkMSDEStatus(msdeInstanceName);
/*  718 */             if (status != 0)
/*      */             {
/*  720 */               setProp("InstallMSDE", "true");
/*      */             }
/*      */           }
/*      */ 
/*  724 */           doDatabaseConfiguration(databaseProps);
/*      */ 
/*  726 */           if (!this.m_isUpdate)
/*      */           {
/*  728 */             if (!createDatabase.equals("never"))
/*      */             {
/*  730 */               if (createDatabase.equals("always"))
/*      */               {
/*  732 */                 createDatabase = "true";
/*      */               }
/*      */               else
/*      */               {
/*  736 */                 createDatabase = promptUser("CreateDatabase", "!csInstallerCreateDBTablesPrompt", createDatabase, this.m_yesNoOptions, this.m_isUpdate);
/*      */               }
/*      */ 
/*  740 */               if (StringUtils.convertToBool(createDatabase, false))
/*      */               {
/*  742 */                 isCreateDatabase = true;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  748 */             boolean supportsUnicode = this.m_installer.getPropertyBool(databaseProps, "SupportsUnicodeTextFields", false);
/*      */ 
/*  750 */             if ((isCreateDatabase) && (supportsUnicode))
/*      */             {
/*  752 */               String msg = LocaleUtils.encodeMessage("csInstallerUseUnicodeTextFields", null);
/*      */ 
/*  754 */               promptUser("DatabaseUnicodeFields", msg, "true", this.m_yesNoOptions, this.m_isUpdate);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  759 */           String jdbcDriverPackageSourceFile = this.m_installProps.getProperty("JdbcDriverPackageSourceFiles");
/*      */ 
/*  761 */           if (jdbcDriverPackageSourceFile != null)
/*      */           {
/*  763 */             this.m_installProps.put("InstallerJdbcClasspath", jdbcDriverPackageSourceFile);
/*      */ 
/*  765 */             String copyDriver = this.m_installProps.getProperty("JdbcDriverPackageCopy");
/*      */ 
/*  767 */             if (StringUtils.convertToBool(copyDriver, false))
/*      */             {
/*  769 */               String pathSep = EnvUtils.getPathSeparator();
/*  770 */               List list = StringUtils.makeListFromSequence(jdbcDriverPackageSourceFile, pathSep.charAt(0), '^', 0);
/*      */ 
/*  773 */               IdcStringBuilder b = new IdcStringBuilder();
/*  774 */               for (int i = 0; i < list.size(); ++i)
/*      */               {
/*  776 */                 String jarFile = (String)list.get(i);
/*  777 */                 String tmp = FileUtils.fileSlashes(jarFile);
/*  778 */                 int index = tmp.lastIndexOf("/");
/*  779 */                 if (index <= 0)
/*      */                   continue;
/*  781 */                 tmp = "$SharedDir/classes/" + tmp.substring(index + 1);
/*  782 */                 if (b.length() > 0)
/*      */                 {
/*  784 */                   b.append("${PATH_SEPARATOR}");
/*      */                 }
/*  786 */                 b.append(tmp);
/*      */               }
/*      */ 
/*  789 */               this.m_installProps.put("JdbcClasspath", b.toString());
/*      */             }
/*      */             else
/*      */             {
/*  793 */               this.m_installProps.put("JdbcClasspath", jdbcDriverPackageSourceFile);
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  801 */             this.m_installer.setJdbcDriverAndClasspath(null, databaseProps);
/*      */ 
/*  804 */             String[] list = { "InstallerJdbcClasspath" };
/*      */ 
/*  808 */             for (int i = 0; i < list.length; ++i)
/*      */             {
/*  810 */               String tmp = this.m_installer.getInstallValue(list[i], null);
/*      */ 
/*  812 */               if (tmp == null)
/*      */                 continue;
/*  814 */               this.m_installProps.put(list[i], tmp);
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  822 */       if ((installType.equals("new")) || (installType.equals("proxy")))
/*      */       {
/*  824 */         setProp("InstallType", "new");
/*      */       }
/*      */       else
/*      */       {
/*  828 */         setProp("InstallType", "update");
/*      */       }
/*      */       String conf;
/*      */       String conf;
/*  832 */       if (isRefineryInstall == true)
/*      */       {
/*      */         String conf;
/*  834 */         if (installType.equals("proxy"))
/*      */         {
/*  836 */           conf = "RefineryProxy";
/*      */         }
/*      */         else
/*      */         {
/*  840 */           conf = "RefineryInstall";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*      */         String conf;
/*  843 */         if (installType.equals("proxy"))
/*      */         {
/*  845 */           conf = "ProxyInstall";
/*      */         }
/*      */         else
/*      */         {
/*  849 */           conf = "Install";
/*  850 */           String isHome = this.m_installProps.getProperty("IsHomeOnly");
/*  851 */           if (StringUtils.convertToBool(isHome, false))
/*      */           {
/*  853 */             conf = "CloneInstall";
/*      */           }
/*      */         }
/*      */       }
/*  856 */       setProp("InstallConfiguration", conf);
/*      */ 
/*  860 */       promptForComponents();
/*      */ 
/*  862 */       processInstallationPath("WindowsServerSpecific", true, null);
/*      */ 
/*  866 */       if (this.m_installationName == null)
/*      */       {
/*  871 */         this.m_installationName = "";
/*      */         try
/*      */         {
/*  874 */           IdcDateFormat fmt = new IdcDateFormat();
/*  875 */           fmt.init("yyyy-MM-dd");
/*  876 */           this.m_installationName += fmt.format(new Date());
/*      */         }
/*      */         catch (ParseStringException ignore)
/*      */         {
/*  880 */           Report.trace("install", null, ignore);
/*  881 */           this.m_installationName += "unknown";
/*      */         }
/*  883 */         this.m_installationName += "-";
/*  884 */         this.m_installationName += installType;
/*      */       }
/*      */ 
/*  887 */       setProp("Platform", this.m_platform);
/*  888 */       setProp("UserLocale", this.m_userLocale);
/*  889 */       setProp("RunChecks", this.m_installProps.getProperty("RunChecks", "true"));
/*  890 */       setProp("RunInstall", this.m_installProps.getProperty("RunInstall", "true"));
/*  891 */       this.m_configFile = (idcDir + "install/" + this.m_installationName + ".txt");
/*  892 */       Properties propsToWrite = saveConfig(this.m_configFile);
/*      */ 
/*  894 */       proceed = "check";
/*  895 */       while (proceed.equals("check"))
/*      */       {
/*  898 */         this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerCheckConfig", null));
/*      */ 
/*  900 */         this.m_installer.m_installerConfig = this.m_installProps;
/*  901 */         this.m_installer.m_installLog.m_quiet = true;
/*  902 */         this.m_installer.m_isUpdate = this.m_isUpdate;
/*      */ 
/*  904 */         Vector args = new IdcVector();
/*  905 */         this.m_installer.addLocaleOptions(args);
/*  906 */         args.addElement("--set-RunInstall=false");
/*  907 */         args.addElement("--set-IsRelaunch=true");
/*  908 */         args.addElement("--set-IsPreinstallationCheck=true");
/*  909 */         args.addElement("--set-ReadPropertiesFromStdin=true");
/*  910 */         args.addElement("--set-ReadPropertiesFromStdinPrompt=");
/*  911 */         int rc = this.m_installer.runInstall(this.m_installProps, this.m_configFile, args, null, null, propsToWrite);
/*      */         String proceedDefault;
/*      */         String proceedDefault;
/*  915 */         if (rc == 0)
/*      */         {
/*  917 */           this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerConfigOK", null));
/*      */ 
/*  919 */           proceedDefault = "true";
/*      */         }
/*      */         else
/*      */         {
/*  923 */           this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerConfigError", null));
/*      */ 
/*  925 */           proceedDefault = (this.m_isUpdate) ? "check" : "false";
/*  926 */           hasWarnings = true;
/*      */         }
/*      */ 
/*  929 */         this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerReviewSettings", null));
/*      */ 
/*  931 */         for (int i = 0; i < this.m_reviewPropList.size(); ++i)
/*      */         {
/*  933 */           String line = (String)this.m_reviewPropList.elementAt(i);
/*  934 */           int index = line.indexOf("=");
/*  935 */           if (index <= 0)
/*      */             continue;
/*  937 */           String key = line.substring(0, index);
/*  938 */           String value = line.substring(index + 1);
/*  939 */           String label = this.m_keyLabelMap.getProperty(key);
/*  940 */           label = localizePrompt(label);
/*  941 */           this.m_promptUser.outputMessage(label + ": " + value);
/*      */         }
/*      */         String[][] proceedOptions;
/*      */         String[][] proceedOptions;
/*  946 */         if (this.m_isUpdate)
/*      */         {
/*  948 */           if (this.m_installer.isOlderVersion("6.1"))
/*      */           {
/*  950 */             String msg = LocaleResources.getString("csInstallerExplainSocketHostAddressSecurityFilterWarning", null);
/*      */ 
/*  953 */             this.m_promptUser.outputMessage(msg);
/*      */           }
/*  955 */           proceedOptions = new String[][] { { "true", "csInstallerProceedOptProceed" }, { "check", "csInstallerProceedOptRecheck" }, { "abort", "csInstallerProceedOptAbort" } };
/*      */         }
/*      */         else
/*      */         {
/*  964 */           proceedOptions = new String[][] { { "true", "csInstallerProceedOptProceed" }, { "false", "csInstallerProceedOptChange" }, { "check", "csInstallerProceedOptRecheck" }, { "abort", "csInstallerProceedOptAbort" } };
/*      */         }
/*      */ 
/*  972 */         LocaleResources.localizeDoubleArray(proceedOptions, null, 1);
/*  973 */         proceed = promptUser("DoInstall", "!csInstallerProceedPrompt", proceedDefault, proceedOptions, false);
/*      */ 
/*  975 */         removeProp("DoInstall");
/*      */       }
/*      */ 
/*  978 */       if (proceed.equals("abort"))
/*      */       {
/*  980 */         checkAbort(null);
/*      */       }
/*      */     }
/*      */ 
/*  984 */     if (hasWarnings)
/*      */     {
/*  986 */       commandLineArguments.addElement("--set-RunChecks=false");
/*      */     }
/*      */ 
/*  989 */     return true;
/*      */   }
/*      */ 
/*      */   protected void doDatabaseConfiguration(Properties databaseProps)
/*      */     throws ServiceException
/*      */   {
/*  995 */     String configEntryList = databaseProps.getProperty("ConfigEntryList");
/*  996 */     configEntryList = configEntryList.trim();
/*  997 */     String database = databaseProps.getProperty("Database");
/*  998 */     if (database.equals("skip_database"))
/*      */     {
/* 1000 */       this.m_installProps.put("SkipJdbcSettings", "1");
/*      */     }
/* 1002 */     processInstallationPath("ManualDatabaseConfiguration", true, databaseProps);
/*      */ 
/* 1004 */     String didManual = this.m_installProps.getProperty("ConfigureJdbcSettingsManually");
/* 1005 */     String requireManual = databaseProps.getProperty("RequireConfigureJdbcSettingsManually");
/* 1006 */     if ((StringUtils.convertToBool(didManual, false)) || (StringUtils.convertToBool(requireManual, false)))
/*      */     {
/* 1009 */       Report.trace("install", "skipping config entries for " + database + " because the user configured them manually.", null);
/*      */     }
/*      */     else
/*      */     {
/* 1018 */       this.m_installProps.remove("JdbcConnectionString");
/* 1019 */       this.m_installProps.remove("JdbcDriver");
/* 1020 */       int flags = 1;
/* 1021 */       if (this.m_installer.getInstallBool("DisableDatabaseOverrides", false))
/*      */       {
/* 1023 */         flags = 0;
/*      */       }
/* 1025 */       processRequestListEx(configEntryList, flags);
/*      */     }
/*      */ 
/* 1028 */     processInstallationPath("DatabaseDriverPackageConfiguration", true, databaseProps);
/*      */   }
/*      */ 
/*      */   protected String[] filterSearchEngines(String[] engines)
/*      */     throws ServiceException
/*      */   {
/* 1035 */     Vector v = new IdcVector();
/* 1036 */     for (int i = 0; i < engines.length; ++i)
/*      */     {
/* 1038 */       String engine = engines[i];
/* 1039 */       if (engine.endsWith("*"))
/*      */       {
/* 1041 */         engine = engine.substring(0, engine.length() - 1);
/*      */       }
/* 1043 */       Report.trace("install", "checking engine '" + engine + "'", null);
/* 1044 */       String[] files = this.m_installer.getInstallerTableValueAsArray("SearchEngineConfigTable", engine, "SearchEnginePackages");
/*      */ 
/* 1047 */       boolean isOkay = true;
/* 1048 */       for (int j = 0; j < files.length; ++j)
/*      */       {
/* 1050 */         if (files[j].length() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1054 */         String path = this.m_installer.computeDestinationEx(files[j], false);
/* 1055 */         if (FileUtils.checkFile(path, true, false) == 0)
/*      */           continue;
/* 1057 */         isOkay = false;
/* 1058 */         Report.trace("install", "missing file '" + files[j] + "'", null);
/*      */ 
/* 1060 */         break;
/*      */       }
/*      */ 
/* 1063 */       if (!isOkay)
/*      */         continue;
/* 1065 */       v.addElement(engines[i]);
/*      */     }
/*      */ 
/* 1068 */     String[] newEngines = new String[v.size()];
/* 1069 */     v.copyInto(newEngines);
/* 1070 */     return newEngines;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1075 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93278 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ManualInstaller
 * JD-Core Version:    0.5.4
 */