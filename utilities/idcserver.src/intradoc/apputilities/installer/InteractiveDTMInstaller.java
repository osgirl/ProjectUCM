/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseStringException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class InteractiveDTMInstaller extends InteractiveInstaller
/*     */ {
/*  33 */   protected String m_corePrefix = "dtm";
/*     */ 
/*     */   public InteractiveDTMInstaller(Properties installerProps, Properties overrideProps, PromptUser prompter)
/*     */   {
/*  38 */     super(installerProps, overrideProps, prompter);
/*  39 */     this.m_registryBase = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Dtm Server";
/*     */ 
/*  41 */     this.m_defaultWindowsName = "c:/oracle/ucm/dtmserver";
/*  42 */     this.m_defaultUnixName = "/oracle/ucm/dtmserver";
/*     */   }
/*     */ 
/*     */   public boolean interactWithUser(Vector commandLineArguments)
/*     */     throws DataException, ServiceException
/*     */   {
/*  49 */     String proceed = "false";
/*     */ 
/*  51 */     String idcDir = null;
/*     */ 
/*  53 */     String[][] types = this.m_installer.getInstallerTableAsArray("DtmInstallTypes");
/*  54 */     LocaleResources.localizeDoubleArray(types, null, 1);
/*  55 */     String installType = null;
/*  56 */     boolean hasWarnings = false;
/*  57 */     boolean getInstallType = true;
/*  58 */     LocaleResources.m_defaultApp = "dtm";
/*  59 */     SharedObjects.putEnvironmentValue("DefaultApplicationName", "dtm");
/*     */ 
/*  61 */     while ((proceed.equals("false")) || (proceed.equals("check")))
/*     */     {
/*  63 */       if (getInstallType)
/*     */       {
/*  65 */         installType = promptUser("InstallType", "!csInstallerTypePrompt", "new", types, false);
/*     */ 
/*  67 */         getInstallType = false;
/*     */       }
/*     */ 
/*  70 */       this.m_propList = new IdcVector();
/*  71 */       this.m_reviewPropList = new IdcVector();
/*  72 */       this.m_isUpdate = false;
/*     */ 
/*  74 */       if (installType.equals("update"))
/*     */       {
/*  76 */         this.m_log.notice("!csInstallerUpdateStatusMsg");
/*     */ 
/*  78 */         this.m_isUpdate = true;
/*     */       }
/*     */       else
/*     */       {
/*  82 */         this.m_log.notice("!csInstallerMasterStatusMsg");
/*     */       }
/*     */ 
/*  85 */       if (this.m_isUpdate)
/*     */       {
/*  87 */         boolean autoAccept = true;
/*     */         while (true)
/*     */         {
/*  90 */           idcDir = promptUser("IntradocDir", "!csInstallerContentServerCoreDirPrompt", defaultMasterServerDir(), (String[][])null, autoAccept);
/*     */ 
/*  93 */           idcDir = FileUtils.directorySlashes(idcDir);
/*  94 */           int rc = FileUtils.checkFile(idcDir, false, true);
/*  95 */           if (rc == 0)
/*     */             break;
/*  97 */           this.m_promptUser.outputMessage(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerDirWriteError", null, idcDir), null));
/*     */ 
/* 104 */           autoAccept = false;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 110 */         idcDir = promptForDirectory("IntradocDir", "!csInstallerContentServerCoreDirPrompt", defaultMasterServerDir());
/*     */       }
/*     */ 
/* 114 */       this.m_installProps.put("TargetDir", idcDir);
/* 115 */       this.m_log.setLogDirectory(idcDir + "install/");
/*     */ 
/* 117 */       if (installType.equals("new"))
/*     */       {
/* 119 */         promptForJvmInfo();
/*     */       }
/*     */ 
/* 125 */       this.m_installer = new SysInstaller();
/* 126 */       this.m_installer.init(this.m_binder, this.m_installProps, this.m_overrideProps, this.m_log, this.m_progress, this.m_promptUser);
/*     */ 
/* 128 */       this.m_installer.m_isUpdate = this.m_isUpdate;
/*     */ 
/* 130 */       if (this.m_isUpdate)
/*     */       {
/*     */         try
/*     */         {
/* 134 */           this.m_installer.loadConfig();
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 138 */           throw new ServiceException("!csInstallerInstanceUpdateError", e);
/*     */         }
/*     */ 
/* 148 */         if (this.m_installer.isOlderVersion("6.1.0"))
/*     */         {
/* 150 */           promptForJvmInfo();
/*     */         }
/*     */         else
/*     */         {
/* 154 */           setJvmPath();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 160 */       queryInstalledServers(idcDir);
/*     */ 
/* 167 */       String alreadyUsedIdcDir = isConfigValueUsed("IntradocDir", idcDir);
/* 168 */       if (this.m_isUpdate)
/*     */       {
/* 170 */         if (alreadyUsedIdcDir == null)
/*     */         {
/* 172 */           Report.trace("install", "the dir '" + idcDir + "' isn't already used but we're updating " + "into it anyway.", null);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 179 */         String idcCfgFile = idcDir + "bin/intradoc.cfg";
/* 180 */         if (FileUtils.checkFile(idcCfgFile, true, false) == 0)
/*     */         {
/* 182 */           this.m_promptUser.outputMessage(LocaleResources.localizeMessage("!csInstallerInstanceExists", null));
/*     */ 
/* 184 */           proceed = "false";
/* 185 */           removeProp("IntradocDir");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 199 */       boolean promptForLocaleInfo = (!this.m_isUpdate) || (this.m_installer.isOlderVersion("5.1.1"));
/* 200 */       if (promptForLocaleInfo)
/*     */       {
/* 202 */         String sysLocaleName = promptUser("SystemLocale", "!csInstallerSelectSystemLocale", this.m_userLocale, this.m_localeChoices, false);
/*     */ 
/* 204 */         IdcLocale sysLocale = LocaleResources.getLocale(sysLocaleName);
/* 205 */         if (!checkSystemEncoding(sysLocale))
/*     */         {
/* 207 */           String message = LocaleUtils.encodeMessage("csInstallerIncompatibleEncoding", null, sysLocaleName, FileUtils.m_javaSystemEncoding);
/*     */ 
/* 210 */           this.m_installer.m_installLog.warning(message);
/*     */         }
/*     */ 
/* 213 */         String timeZone = this.m_installer.m_intradocConfig.getProperty("SystemTimeZone");
/* 214 */         if (timeZone == null)
/*     */         {
/* 216 */           String timeZoneRegion = promptUser("TimeZoneRegion", "!csInstallerSelectTimeZoneRegion", this.m_defaultRegion, this.m_regionChoices, false);
/*     */ 
/* 219 */           removeProp("TimeZoneRegion");
/* 220 */           if (!timeZoneRegion.equals("osDefault"))
/*     */           {
/* 222 */             String[][] zones = (String[][])(String[][])this.m_regionTimeZones.get(timeZoneRegion);
/* 223 */             timeZone = promptUser("SystemTimeZone", "!csInstallerSelectTimeZone", this.m_defaultTimeZone, zones, false);
/*     */ 
/* 225 */             SharedObjects.putEnvironmentValue("SystemTimeZone", timeZone);
/* 226 */             SharedObjects.putEnvironmentValue("WarnAboutTimeZone", "0");
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 244 */         if (this.m_installProps.getProperty("FileEncoding") == null)
/*     */         {
/* 246 */           this.m_installProps.put("FileEncoding", "UTF8");
/*     */         }
/*     */       }
/*     */ 
/* 250 */       promptUser("ServerPort", "!csInstallerPortPrompt", "4400", (String[][])null, this.m_isUpdate);
/*     */ 
/* 252 */       if (!this.m_isUpdate)
/*     */       {
/* 254 */         promptUser("SocketHostAddressSecurityFilter", "!csInstallerSocketHostAddressSecurityFilterPrompt", "127.0.0.1|0:0:0:0:0:0:0:1", (String[][])null, this.m_isUpdate);
/*     */       }
/*     */ 
/* 258 */       promptUser("MailServer", "!csInstallerMailServerPrompt", "mail", (String[][])null, this.m_isUpdate);
/*     */ 
/* 260 */       promptUser("SysAdminAddress", "!csInstallerAdminEMailPrompt", "sysadmin@mail", (String[][])null, this.m_isUpdate);
/*     */ 
/* 263 */       boolean getIdcNameIsUpdate = this.m_isUpdate;
/*     */       while (true)
/*     */       {
/* 266 */         String idcName = promptUser("IDC_Name", "!csInstallerInstanceNamePrompt", "DtmServer", (String[][])null, getIdcNameIsUpdate);
/*     */ 
/* 268 */         if ((StringUtils.urlEncode(idcName).equals(idcName)) && (!idcName.contains(" ")))
/*     */           break;
/* 270 */         String msg = LocaleResources.getString("csIdcNameIllegal", null, idcName);
/* 271 */         this.m_promptUser.outputMessage(msg);
/* 272 */         getIdcNameIsUpdate = false;
/*     */       }
/*     */ 
/* 279 */       if ((this.m_installer.isWindows()) && (((this.m_installer.isOlderVersion("4.0")) || (!installType.equals("update")))))
/*     */       {
/* 283 */         String[][] serviceDefaults = { { "auto", "!csStartServiceAutomatically" }, { "true", "!csInstallService" }, { "false", "!csDontInstallService" } };
/*     */ 
/* 289 */         LocaleResources.localizeDoubleArray(serviceDefaults, null, 1);
/*     */ 
/* 293 */         String msg = LocaleUtils.encodeMessage("csInstallerInstallContentServerService", null);
/*     */ 
/* 295 */         promptUser("InstallServerService", msg, "auto", serviceDefaults, true);
/*     */ 
/* 298 */         msg = LocaleUtils.encodeMessage("csInstallerDisable8dot3NameCreation", null);
/*     */ 
/* 300 */         promptUser("Disable8dot3NameCreation", msg, "true", this.m_yesNoOptions, true);
/*     */       }
/*     */ 
/* 306 */       if (this.m_installationName == null)
/*     */       {
/* 311 */         this.m_installationName = "";
/*     */         try
/*     */         {
/* 314 */           IdcDateFormat fmt = new IdcDateFormat();
/* 315 */           fmt.init("yyyy-MM-dd");
/* 316 */           this.m_installationName += fmt.format(new Date());
/*     */         }
/*     */         catch (ParseStringException ignore)
/*     */         {
/* 320 */           Report.trace("install", null, ignore);
/* 321 */           this.m_installationName += "unknown";
/*     */         }
/* 323 */         this.m_installationName += "-";
/* 324 */         this.m_installationName += installType;
/*     */       }
/*     */ 
/* 327 */       if ((installType.equals("new")) || (installType.equals("proxy")))
/*     */       {
/* 329 */         setProp("InstallType", "new");
/*     */       }
/*     */       else
/*     */       {
/* 333 */         setProp("InstallType", "update");
/*     */       }
/* 335 */       setProp("InstallConfiguration", "DtmInstall");
/*     */ 
/* 337 */       setProp("Platform", this.m_platform);
/* 338 */       setProp("UserLocale", this.m_userLocale);
/* 339 */       setProp("RunChecks", this.m_installProps.getProperty("RunChecks", "true"));
/* 340 */       setProp("RunInstall", this.m_installProps.getProperty("RunInstall", "true"));
/* 341 */       this.m_configFile = (idcDir + "install/" + this.m_installationName + ".txt");
/* 342 */       Properties propsToWrite = saveConfig(this.m_configFile);
/*     */ 
/* 344 */       proceed = "check";
/* 345 */       while (proceed.equals("check"))
/*     */       {
/* 348 */         this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerCheckConfig", null));
/*     */ 
/* 350 */         this.m_installer.m_installerConfig = this.m_installProps;
/* 351 */         this.m_installer.m_installLog.m_quiet = true;
/* 352 */         this.m_installer.m_isUpdate = this.m_isUpdate;
/*     */ 
/* 354 */         Vector args = new IdcVector();
/* 355 */         args.addElement("--set-RunInstall=false");
/* 356 */         args.addElement("--set-IsPreinstallationCheck=true");
/* 357 */         int rc = this.m_installer.runInstall(this.m_installProps, this.m_configFile, args, null, null, propsToWrite);
/*     */         String proceedDefault;
/*     */         String proceedDefault;
/* 361 */         if (rc == 0)
/*     */         {
/* 363 */           this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerConfigOK", null));
/*     */ 
/* 365 */           proceedDefault = "true";
/*     */         }
/*     */         else
/*     */         {
/* 369 */           this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerConfigError", null));
/*     */ 
/* 371 */           proceedDefault = (this.m_isUpdate) ? "check" : "false";
/* 372 */           hasWarnings = true;
/*     */         }
/*     */ 
/* 375 */         this.m_promptUser.outputMessage(LocaleResources.getString("csInstallerReviewSettings", null));
/*     */ 
/* 377 */         for (int i = 0; i < this.m_reviewPropList.size(); ++i)
/*     */         {
/* 379 */           String line = (String)this.m_reviewPropList.elementAt(i);
/* 380 */           int index = line.indexOf("=");
/* 381 */           if (index <= 0)
/*     */             continue;
/* 383 */           String key = line.substring(0, index);
/* 384 */           String value = line.substring(index + 1);
/* 385 */           String label = this.m_keyLabelMap.getProperty(key);
/* 386 */           label = LocaleResources.localizeMessage(label, null);
/* 387 */           this.m_promptUser.outputMessage(label + ": " + value);
/*     */         }
/*     */         String[][] proceedOptions;
/*     */         String[][] proceedOptions;
/* 392 */         if (this.m_isUpdate)
/*     */         {
/* 394 */           if (this.m_installer.isOlderVersion("6.0"))
/*     */           {
/* 396 */             String msg = LocaleResources.getString("csInstallerExplainSocketHostAddressSecurityFilterWarning", null);
/*     */ 
/* 399 */             this.m_promptUser.outputMessage(msg);
/*     */           }
/* 401 */           proceedOptions = new String[][] { { "true", "csInstallerProceedOptProceed" }, { "check", "csInstallerProceedOptRecheck" }, { "abort", "csInstallerProceedOptAbort" } };
/*     */         }
/*     */         else
/*     */         {
/* 410 */           proceedOptions = new String[][] { { "true", "csInstallerProceedOptProceed" }, { "false", "csInstallerProceedOptChange" }, { "check", "csInstallerProceedOptRecheck" }, { "abort", "csInstallerProceedOptAbort" } };
/*     */         }
/*     */ 
/* 418 */         LocaleResources.localizeDoubleArray(proceedOptions, null, 1);
/* 419 */         proceed = promptUser("DoInstall", "!csInstallerProceedPrompt", proceedDefault, proceedOptions, false);
/*     */ 
/* 421 */         removeProp("DoInstall");
/*     */       }
/*     */ 
/* 424 */       if (proceed.equals("abort"))
/*     */       {
/* 426 */         checkAbort(null);
/*     */       }
/*     */     }
/*     */ 
/* 430 */     if (hasWarnings)
/*     */     {
/* 432 */       commandLineArguments.addElement("--set-RunChecks=false");
/*     */     }
/*     */ 
/* 435 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 440 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93238 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InteractiveDTMInstaller
 * JD-Core Version:    0.5.4
 */