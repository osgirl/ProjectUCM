/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.admin.AdminDirectoryLocator;
/*     */ import intradoc.admin.AdminServerData;
/*     */ import intradoc.admin.AdminServerUtils;
/*     */ import intradoc.admin.AdminService;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.config.ConfigFileLoader;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ClusterTool
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */   public SysInstaller m_targetInstaller;
/*     */   public SysInstaller m_adminInstaller;
/*     */   public String m_clusterConfigurationRule;
/*     */   public String m_targetDir;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  46 */     this.m_installer = installer;
/*     */     try
/*     */     {
/*  49 */       this.m_adminInstaller = createAdminServerInstaller(this.m_installer, false);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  53 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  56 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/*  58 */     String rule = null;
/*  59 */     if (this.m_adminInstaller != null)
/*     */     {
/*  61 */       rule = this.m_adminInstaller.getConfigValue("ClusterBinDirRule");
/*     */     }
/*  63 */     if ((rule == null) && 
/*  65 */       (this.m_adminInstaller != null))
/*     */     {
/*  67 */       rule = this.m_adminInstaller.getInstallValue("ClusterBinDirRule", null);
/*  68 */       if (rule != null)
/*     */       {
/*  70 */         this.m_adminInstaller.editConfigFile("config/config.cfg", "ClusterBinDirRule", rule);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  75 */     if (rule == null)
/*     */     {
/*  77 */       rule = this.m_installer.getConfigValue("ClusterBinDirRule");
/*     */     }
/*  79 */     if (rule == null)
/*     */     {
/*  81 */       rule = this.m_installer.getInstallValue("ClusterBinDirRule", null);
/*     */     }
/*  83 */     validateClusterBinDirRule(rule);
/*  84 */     if (rule == null)
/*     */     {
/*  86 */       rule = this.m_clusterConfigurationRule = "local";
/*     */     }
/*     */     else
/*     */     {
/*  90 */       this.m_clusterConfigurationRule = rule;
/*     */     }
/*     */ 
/*  93 */     String clusterNodeName = this.m_installer.getInstallValue("ClusterNodeName", null);
/*     */ 
/*  95 */     if (clusterNodeName == null)
/*     */     {
/*  97 */       String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", null, "ClusterNodeName");
/*     */ 
/* 100 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 103 */     String targetDir = null;
/* 104 */     if (rule.equals("shared"))
/*     */     {
/* 106 */       targetDir = this.m_installer.m_idcDir;
/*     */     }
/*     */ 
/* 109 */     targetDir = this.m_installer.getInstallValue("ClusterNodeIntradocDir", targetDir);
/* 110 */     if (targetDir == null)
/*     */     {
/* 112 */       String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", null, "ClusterNodeIntradocDir");
/*     */ 
/* 115 */       throw new ServiceException(msg);
/*     */     }
/* 117 */     this.m_targetDir = targetDir;
/*     */     try
/*     */     {
/* 120 */       this.m_targetInstaller = this.m_installer.deriveInstaller(this.m_targetDir);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 124 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 127 */     String ntProcCtrlPath = this.m_installer.getInstallValue("NtProcCtrlPath", null);
/*     */ 
/* 129 */     if (ntProcCtrlPath == null)
/*     */     {
/* 131 */       ntProcCtrlPath = this.m_installer.computeDestinationEx("bin/NtProcCtrl.exe", false);
/*     */ 
/* 133 */       if ((FileUtils.checkFile(ntProcCtrlPath, true, false) != 0) && (this.m_adminInstaller != null))
/*     */       {
/* 136 */         ntProcCtrlPath = this.m_adminInstaller.computeDestinationEx("bin/NtProcCtrl.exe", false);
/*     */       }
/*     */     }
/*     */ 
/* 140 */     this.m_installer.m_installerConfig.put("NtProcCtrlPath", ntProcCtrlPath);
/*     */ 
/* 142 */     if (this.m_adminInstaller != null)
/*     */     {
/* 144 */       this.m_adminInstaller.m_installerConfig.put("NtProcCtrlPath", ntProcCtrlPath);
/*     */     }
/*     */ 
/* 147 */     this.m_targetInstaller.m_installerConfig.put("NtProcCtrlPath", ntProcCtrlPath);
/*     */ 
/* 150 */     if (isAdminServer)
/*     */     {
/* 152 */       controlServer("stop-server", this.m_adminInstaller);
/*     */     }
/*     */     else
/*     */     {
/* 156 */       controlServer("stop-server", this.m_installer);
/*     */     }
/* 158 */     syncBinEtcDirectory();
/* 159 */     syncStaticFiles();
/* 160 */     syncDataUsersDirectory();
/* 161 */     registerClusterNodeWithAdminServer();
/* 162 */     configureIIS();
/* 163 */     configureWindowsServices();
/* 164 */     configureWindowsShortcuts();
/* 165 */     if (this.m_targetInstaller.getInstallValue("WindowsServiceName", null) == null)
/*     */     {
/* 168 */       if (isAdminServer)
/*     */       {
/* 170 */         String idcName = this.m_adminInstaller.getInstallValue("IDC_Admin_Name", null);
/*     */ 
/* 172 */         this.m_adminInstaller.m_installerConfig.put("WindowsServiceName", "IdcAdminService " + idcName + "-" + clusterNodeName);
/*     */       }
/*     */       else
/*     */       {
/* 177 */         String idcName = this.m_targetInstaller.getInstallValue("IDC_Name", null);
/*     */ 
/* 179 */         this.m_targetInstaller.m_installerConfig.put("WindowsServiceName", "IdcContentService " + idcName + "-" + clusterNodeName);
/*     */       }
/*     */     }
/*     */ 
/* 183 */     if (isAdminServer)
/*     */     {
/*     */       try
/*     */       {
/* 187 */         SysInstaller localAdminInstaller = this.m_targetInstaller.deriveInstaller(this.m_targetInstaller.m_idcDir + "/admin");
/*     */ 
/* 190 */         localAdminInstaller.m_installerConfig.put("IsAdminServer", "true");
/*     */ 
/* 192 */         localAdminInstaller.m_installerConfig.put("AdminServerDir", localAdminInstaller.m_idcDir);
/* 193 */         controlServer("start-server", localAdminInstaller);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 197 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 202 */       controlServer("start-server", this.m_targetInstaller);
/*     */     }
/*     */ 
/* 205 */     return 0;
/*     */   }
/*     */ 
/*     */   public void validateClusterBinDirRule(String rule)
/*     */     throws ServiceException
/*     */   {
/* 211 */     if ((rule == null) || (rule.equals("local")) || (rule.equals("shared")))
/*     */       return;
/* 213 */     String msg = LocaleUtils.encodeMessage("csClusterBinDirRuleIllegal", null, rule);
/*     */ 
/* 215 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public static SysInstaller createAdminServerInstaller(SysInstaller installer, boolean useLocalPaths)
/*     */     throws DataException
/*     */   {
/* 222 */     String adminServerPath = installer.getInstallValue("AdminServerDir", null);
/*     */ 
/* 224 */     if (adminServerPath == null)
/*     */     {
/* 226 */       SysInstaller tmpInstaller = installer;
/* 227 */       if (!useLocalPaths)
/*     */       {
/* 229 */         String masterServerPath = installer.getConfigValue("MasterServerDir");
/* 230 */         if (masterServerPath == null)
/*     */         {
/* 233 */           masterServerPath = installer.getConfigValue("IntradocDir");
/*     */         }
/* 235 */         if (masterServerPath != null)
/*     */         {
/* 237 */           tmpInstaller = installer.deriveInstaller(masterServerPath);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 242 */       adminServerPath = tmpInstaller.getConfigValue("AdminServerDir");
/* 243 */       if (adminServerPath == null)
/*     */       {
/* 245 */         adminServerPath = FileUtils.directorySlashes(tmpInstaller.m_idcDir + "/admin");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 250 */     Report.trace("install", "ClusterTool using '" + adminServerPath + "' for adminServerPath", null);
/*     */ 
/* 252 */     if (FileUtils.checkFile(adminServerPath, false, false) != 0)
/*     */     {
/* 254 */       Report.trace("install", "no admin server found, so not registering node", null);
/* 255 */       return null;
/*     */     }
/* 257 */     SysInstaller adminServerInstaller = installer.deriveInstaller(adminServerPath);
/*     */ 
/* 259 */     adminServerInstaller.m_installerConfig.put("AdminServerDir", adminServerPath);
/* 260 */     adminServerInstaller.m_installerConfig.put("IsAdminServer", "true");
/* 261 */     return adminServerInstaller;
/*     */   }
/*     */ 
/*     */   public void controlServer(String action, SysInstaller installer)
/*     */     throws ServiceException
/*     */   {
/* 267 */     boolean controlServer = installer.getInstallBool("ControlServer-" + action, true);
/*     */ 
/* 269 */     if (!controlServer)
/*     */     {
/* 271 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 275 */       installer.installSection(action);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 279 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void syncBinEtcDirectory() throws ServiceException
/*     */   {
/* 285 */     String clusterNodeName = this.m_installer.getInstallValue("ClusterNodeName", null);
/*     */ 
/* 288 */     boolean isSharedConfig = this.m_clusterConfigurationRule.equals("shared");
/* 289 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */     String etcDirSuffix;
/*     */     String binDirSuffix;
/*     */     String etcDirSuffix;
/* 291 */     if (isSharedConfig)
/*     */     {
/* 293 */       String binDirSuffix = "bin-" + clusterNodeName;
/* 294 */       etcDirSuffix = null;
/*     */     }
/*     */     else
/*     */     {
/* 298 */       binDirSuffix = "bin";
/* 299 */       etcDirSuffix = "etc";
/*     */     }
/*     */ 
/* 302 */     String binDir = computeTargetDirectory("TargetBinDir", binDirSuffix, true);
/*     */ 
/* 304 */     String etcDir = computeTargetDirectory("TargetEtcDir", etcDirSuffix, true);
/*     */ 
/* 306 */     computeTargetDirectory("SharedDir", "shared", false);
/*     */ 
/* 308 */     FileUtils.checkOrCreateDirectory(binDir, 99);
/* 309 */     if (!isSharedConfig)
/*     */     {
/* 311 */       FileUtils.checkOrCreateDirectory(etcDir, 99);
/*     */     }
/* 313 */     Properties pathMap = new Properties();
/* 314 */     if (isAdminServer)
/*     */     {
/* 316 */       pathMap.put("admin/bin", binDir);
/* 317 */       if (!isSharedConfig)
/*     */       {
/* 319 */         pathMap.put("admin/etc", etcDir);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 324 */       pathMap.put("bin", binDir);
/* 325 */       if (!isSharedConfig)
/*     */       {
/* 327 */         pathMap.put("etc", etcDir);
/*     */       }
/*     */     }
/*     */ 
/* 331 */     String family = null;
/* 332 */     if (this.m_installer.isWindows())
/*     */     {
/* 334 */       family = this.m_installer.getInstallValue("Platform", null);
/*     */     }
/*     */     else
/*     */     {
/* 338 */       family = "unix";
/*     */     }
/* 340 */     String type = "master";
/* 341 */     if (isAdminServer)
/*     */     {
/* 343 */       type = "admin";
/*     */     }
/* 345 */     Properties launcherConfig = this.m_installer.getInstallerTable("Launchers", type + "-" + family);
/*     */ 
/* 347 */     CoreLaunchers.installLaunchers(this.m_installer, launcherConfig, pathMap, this.m_installer.m_platform, true);
/*     */ 
/* 350 */     if (!isAdminServer)
/*     */     {
/* 352 */       copyIcons(binDir);
/*     */     }
/*     */ 
/* 355 */     if (!isSharedConfig)
/*     */     {
/* 357 */       String oldUseCopiesSetting = this.m_installer.getInstallValue("CopyLaunchers", null);
/*     */ 
/* 359 */       this.m_installer.m_installerConfig.put("CopyLaunchers", "true");
/* 360 */       launcherConfig = this.m_installer.getInstallerTable("Launchers", "local" + type + "node-" + family);
/*     */ 
/* 362 */       CoreLaunchers.installLaunchers(this.m_installer, launcherConfig, pathMap, this.m_installer.m_platform, true);
/*     */ 
/* 364 */       if (oldUseCopiesSetting != null)
/*     */       {
/* 366 */         this.m_installer.m_installerConfig.put("CopyLaunchers", oldUseCopiesSetting);
/*     */       }
/*     */       else
/*     */       {
/* 371 */         this.m_installer.m_installerConfig.remove("CopyLaunchers");
/*     */       }
/*     */     }
/*     */ 
/* 375 */     String intradocCfgSource = this.m_installer.computeDestinationEx("bin/intradoc.cfg", false);
/*     */ 
/* 377 */     intradocCfgSource = this.m_installer.getInstallValue("IntradocCfgSource", intradocCfgSource);
/*     */ 
/* 380 */     String intradocCfgTarget = binDir + "intradoc.cfg";
/* 381 */     intradocCfgTarget = this.m_installer.getInstallValue("IntradocCfgTarget", intradocCfgTarget);
/*     */ 
/* 383 */     Properties oldProps = new Properties();
/* 384 */     Properties localProps = new Properties();
/*     */     try
/*     */     {
/* 387 */       String localConfigSource = binDir + "local.cfg";
/* 388 */       localConfigSource = this.m_installer.getInstallValue("LocalCfgSource", localConfigSource);
/*     */ 
/* 390 */       if (FileUtils.checkFile(localConfigSource, true, false) == 0)
/*     */       {
/* 393 */         FileUtils.loadProperties(localProps, localConfigSource);
/*     */       }
/* 395 */       if (FileUtils.checkFile(intradocCfgTarget, true, false) == 0)
/*     */       {
/* 398 */         FileUtils.loadProperties(oldProps, intradocCfgTarget);
/*     */       }
/*     */ 
/* 401 */       FileUtils.copyFile(intradocCfgSource, intradocCfgTarget);
/* 402 */       this.m_installer.editConfigFile(intradocCfgTarget, "ClusterNodeName", clusterNodeName);
/*     */ 
/* 404 */       if (!isSharedConfig)
/*     */       {
/* 406 */         this.m_installer.editConfigFile(intradocCfgTarget, "ClusterNodeIntradocDir", this.m_targetDir);
/*     */       }
/*     */ 
/* 412 */       int i = 0;
/* 413 */       for (; i < IdcSystemConfig.CLUSTER_ADDRESS_SETTINGS.length; ++i)
/*     */       {
/* 415 */         this.m_installer.editConfigFile(intradocCfgTarget, IdcSystemConfig.CLUSTER_ADDRESS_SETTINGS[i], null);
/*     */       }
/*     */ 
/* 419 */       Properties currentProps = new Properties();
/* 420 */       FileUtils.loadProperties(currentProps, intradocCfgTarget);
/* 421 */       String[][] array = SysInstaller.propertiesAsDoubleArray(oldProps);
/* 422 */       for (int i = 0; i < array.length; ++i)
/*     */       {
/* 424 */         String key = array[i][0];
/* 425 */         String value = array[i][1];
/* 426 */         if (currentProps.getProperty(key) != null)
/*     */           continue;
/* 428 */         this.m_installer.editConfigFile(intradocCfgTarget, key, value);
/*     */       }
/*     */ 
/* 433 */       String[] preserveList = { "ClusterNodeAddress", "ClusterNodeHostName", "IdcServerBindAddress", "IdcAdminServerBindAddress", "IntradocServerHostName", "SocketHostAddressSecurityFilter", "IdcAdminServerHostName" };
/*     */ 
/* 441 */       for (int i = 0; i < preserveList.length; ++i)
/*     */       {
/* 443 */         String key = preserveList[i];
/* 444 */         String value = this.m_installer.m_overrideProps.getProperty(key);
/* 445 */         if (value == null)
/*     */         {
/* 447 */           value = oldProps.getProperty(key);
/*     */         }
/* 449 */         if (value == null)
/*     */           continue;
/* 451 */         this.m_installer.editConfigFile(intradocCfgTarget, key, value);
/*     */       }
/*     */ 
/* 456 */       array = SysInstaller.propertiesAsDoubleArray(localProps);
/* 457 */       for (int i = 0; i < array.length; ++i)
/*     */       {
/* 459 */         String key = array[i][0];
/* 460 */         if (this.m_installer.m_overrideProps.get(key) != null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 464 */         this.m_installer.editConfigFile(intradocCfgTarget, key, array[i][1]);
/*     */       }
/*     */ 
/* 467 */       if (isAdminServer)
/*     */       {
/* 469 */         String adminDataDir = this.m_installer.m_installerConfig.getProperty("AdminDataDir");
/* 470 */         if (null == adminDataDir)
/*     */         {
/* 472 */           adminDataDir = this.m_adminInstaller.computeDestinationEx("data", false);
/*     */         }
/* 474 */         this.m_installer.editConfigFile(intradocCfgTarget, "AdminDataDir", adminDataDir);
/*     */       }
/*     */ 
/* 479 */       FileUtils.loadProperties(currentProps, intradocCfgTarget);
/*     */ 
/* 482 */       if (this.m_installer.getInstallBool("AllowSocketHostAddressSecurityFilterUpdates", true))
/*     */       {
/* 485 */         String address = currentProps.getProperty("ClusterNodeAddress");
/* 486 */         if (address == null)
/*     */         {
/* 488 */           address = currentProps.getProperty("IdcServerBindAddress");
/*     */         }
/* 490 */         if (address == null)
/*     */         {
/* 492 */           address = currentProps.getProperty("IntradocServerHostName");
/*     */         }
/*     */ 
/* 495 */         String filter = this.m_installer.getConfigValue("SocketHostAddressSecurityFilter");
/*     */         Vector list;
/*     */         Vector list;
/* 498 */         if (filter == null)
/*     */         {
/* 500 */           list = new IdcVector();
/*     */         }
/*     */         else
/*     */         {
/* 504 */           list = StringUtils.parseArray(filter, '|', '^');
/*     */         }
/* 506 */         if ((address != null) && 
/* 508 */           (list.indexOf(address) < 0))
/*     */         {
/* 510 */           list.addElement(address);
/* 511 */           filter = StringUtils.createString(list, '|', '^');
/*     */ 
/* 513 */           this.m_installer.editConfigFile("config/config.cfg", "SocketHostAddressSecurityFilter", filter);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 519 */       if ((!isSharedConfig) && (!isWindows()))
/*     */       {
/* 523 */         FileOutputStream outStream = new FileOutputStream(etcDir + "/config");
/*     */ 
/* 525 */         Writer w = FileUtils.openDataWriterEx(outStream, null, 1);
/*     */         try
/*     */         {
/* 529 */           String binDir2 = fixPathName(binDir);
/* 530 */           String etcDir2 = fixPathName(etcDir);
/* 531 */           w.write("NODE_BINDIR='" + binDir2 + "'\n");
/* 532 */           w.write("INTRADOCMS_ETC='" + etcDir2 + "'\n");
/* 533 */           w.write("INTRADOCMS_PID='" + etcDir2 + "pid'\n");
/* 534 */           w.write("INTRADOCMS_LOG='" + etcDir2 + "log'\n");
/* 535 */           w.write("INTRADOCMS_HISTORY='" + etcDir2 + "history'\n");
/* 536 */           w.write("INTRADOCMS_STARTFILE='" + etcDir2 + ".starting'\n");
/* 537 */           w.close();
/*     */         }
/*     */         finally
/*     */         {
/* 541 */           FileUtils.closeObject(w);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 547 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void copyIcons(String destDir) throws ServiceException
/*     */   {
/* 553 */     String iconsSource = this.m_installer.computeDestinationEx("bin/icons", false);
/*     */ 
/* 555 */     if (FileUtils.checkFile(iconsSource, false, false) != 0)
/*     */       return;
/* 557 */     Report.trace("install", "copying icons from '" + iconsSource + "'", null);
/*     */ 
/* 559 */     this.m_installer.copyRecursiveEx(iconsSource, destDir + "/icons", null, null, 0L, 1L, 0, 0);
/*     */   }
/*     */ 
/*     */   public void syncStaticFiles()
/*     */     throws ServiceException
/*     */   {
/* 568 */     boolean isSharedConfig = this.m_clusterConfigurationRule.equals("shared");
/* 569 */     if ((isSharedConfig) || (!this.m_installer.getInstallBool("UseLocalStaticFiles", true)))
/*     */     {
/* 572 */       return;
/*     */     }
/*     */ 
/* 575 */     String[][] staticFileDirs = { { "resources", "IdcResourcesDir" }, { "native", "IdcNativeDir" } };
/*     */ 
/* 581 */     for (String[] dirInfo : staticFileDirs)
/*     */     {
/* 583 */       String targetDir = this.m_targetDir + "/" + dirInfo[0];
/* 584 */       targetDir = this.m_installer.getInstallValue("TargetDir_" + dirInfo[0], targetDir);
/*     */ 
/* 586 */       targetDir = FileUtils.directorySlashes(targetDir);
/*     */ 
/* 588 */       String binDir = computeTargetDirectory("TargetBinDir", "bin", true);
/* 589 */       this.m_installer.editConfigFile(binDir + "intradoc.cfg", "IdcHomeDir", this.m_installer.computeDestinationEx("${IdcHomeDir}", false));
/*     */ 
/* 592 */       if (!this.m_installer.getInstallBool("SyncStaticFiles", true))
/*     */         continue;
/* 594 */       String sourceDir = this.m_installer.computeDestination("${IdcHomeDir}/" + dirInfo[0]);
/*     */ 
/* 596 */       this.m_installer.copyRecursiveEx(sourceDir, targetDir, null, null, 0L, 1L, 0, 2);
/*     */ 
/* 598 */       this.m_installer.editConfigFile(binDir + "intradoc.cfg", dirInfo[1], targetDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void syncDataUsersDirectory()
/*     */     throws ServiceException
/*     */   {
/* 606 */     boolean isSharedConfig = this.m_clusterConfigurationRule.equals("shared");
/* 607 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 609 */     if ((isSharedConfig) || (isAdminServer) || (!this.m_installer.getInstallBool("UseLocalDataUsersDirectory", true)))
/*     */     {
/* 612 */       return;
/*     */     }
/*     */ 
/* 615 */     String cacheDir = this.m_targetDir + "/data/users";
/* 616 */     cacheDir = this.m_installer.getInstallValue("TargetDataUsersDir", cacheDir);
/* 617 */     cacheDir = FileUtils.directorySlashes(cacheDir);
/* 618 */     String binDir = computeTargetDirectory("TargetBinDir", "bin", true);
/* 619 */     this.m_installer.editConfigFile(binDir + "intradoc.cfg", "UserPublishCacheDir", cacheDir);
/*     */   }
/*     */ 
/*     */   public void registerClusterNodeWithAdminServer()
/*     */     throws ServiceException
/*     */   {
/* 625 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 627 */     boolean skipRegister = this.m_installer.getInstallBool("DisableClusterAdminServerRegistration", false);
/*     */ 
/* 629 */     if ((isAdminServer) || (skipRegister))
/*     */     {
/* 631 */       return;
/*     */     }
/* 633 */     if (this.m_adminInstaller == null)
/*     */     {
/* 635 */       Report.trace("install", "not registering with admin server because m_adminInstaller is null", null);
/*     */ 
/* 638 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 643 */       this.m_adminInstaller.loadConfig();
/*     */ 
/* 645 */       this.m_adminInstaller.initServerConfig(this.m_adminInstaller.m_idcDir, false);
/*     */ 
/* 647 */       SharedObjects.putEnvironmentValue("IdcProductName", "idcadmin");
/* 648 */       AdminDirectoryLocator.init();
/* 649 */       DirectoryLocator.buildRootDirectories();
/* 650 */       ComponentLoader.reset();
/* 651 */       ComponentLoader.initDefaults();
/* 652 */       AdminServerUtils.setupFileStoreForAdminServer();
/*     */ 
/* 654 */       AdminServerUtils.loadComponentData();
/*     */ 
/* 656 */       String myId = getMyId();
/* 657 */       String idcName = this.m_installer.getInstallValue("IDC_Name", null);
/* 658 */       String nodeName = this.m_installer.getInstallValue("ClusterNodeName", null);
/*     */ 
/* 660 */       AdminServerData servers = new AdminServerData();
/* 661 */       servers.load(ConfigFileLoader.m_defaultCFU);
/* 662 */       SharedObjects.putTable("ServerDefinition", servers);
/* 663 */       DataResultSet adminServers = servers.getAdminServers();
/*     */ 
/* 666 */       boolean skip = false;
/* 667 */       FieldInfo idField = new FieldInfo();
/* 668 */       FieldInfo nameField = new FieldInfo();
/* 669 */       if (!servers.getFieldInfo("IDC_Id", idField))
/*     */       {
/* 671 */         Report.trace("install", "unable to enumerate servers because IDC_Id column isn't defined.", null);
/*     */ 
/* 673 */         skip = true;
/*     */       }
/* 675 */       if (!servers.getFieldInfo("IDC_Name", nameField))
/*     */       {
/* 677 */         Report.trace("install", "unable to enumerate servers because IDC_Name column isn't defined.", null);
/*     */ 
/* 679 */         skip = true;
/*     */       }
/* 681 */       for (servers.first(); (!skip) && (servers.isRowPresent()); servers.next())
/*     */       {
/* 683 */         String serverId = servers.getStringValue(idField.m_index);
/* 684 */         String serverIdcName = servers.getStringValue(nameField.m_index);
/* 685 */         if (!serverId.equals(serverIdcName))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 690 */         Properties data = servers.getLocalData(serverId);
/* 691 */         String intradocDir = data.getProperty("IntradocDir");
/* 692 */         if (!intradocDir.equals(this.m_installer.m_idcDir))
/*     */           continue;
/* 694 */         Report.trace("install", "removing server '" + serverId + "' because it appears to be a dup of me.", null);
/*     */ 
/* 696 */         servers.removeServerReference(serverId, ConfigFileLoader.m_defaultCFU);
/* 697 */         break;
/*     */       }
/*     */ 
/* 700 */       String[] adminFieldNames = { "IDC_Admin_Id", "IdcAdminServerPort" };
/* 701 */       FieldInfo[] adminFields = null;
/*     */       try
/*     */       {
/* 704 */         adminFields = ResultSetUtils.createInfoList(adminServers, adminFieldNames, true);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 708 */         Report.trace("install", "Unable to enumerate admin servers", e);
/* 709 */         skip = true;
/*     */       }
/* 711 */       String adminServerPort = "";
/* 712 */       String adminServerIdcName = this.m_adminInstaller.getConfigValue("IDC_Admin_Name");
/* 713 */       while ((!skip) && (null != adminServers.findRow(adminFields[0].m_index, adminServerIdcName)))
/*     */       {
/* 715 */         adminServerPort = adminServers.getStringValue(adminFields[1].m_index);
/* 716 */         Report.trace("install", "removing admin server '" + adminServerIdcName + "' because it appears to be a dup of me.", null);
/*     */ 
/* 718 */         adminServers.deleteCurrentRow();
/*     */       }
/*     */ 
/* 721 */       Properties props = servers.getLocalData(myId);
/* 722 */       if (props == null)
/*     */       {
/* 724 */         DataBinder binder = new DataBinder();
/* 725 */         binder.putLocal("IntradocDir", this.m_installer.m_idcDir);
/* 726 */         binder.putLocal("IDC_Admin_Name", adminServerIdcName);
/* 727 */         binder.putLocal("IDC_Id", myId);
/* 728 */         binder.putLocal("IDC_Name", idcName);
/* 729 */         binder.putLocal("IDC_Admin_Id", adminServerIdcName + "-" + nodeName);
/* 730 */         binder.putLocal("ClusterNodeIntradocDir", this.m_targetDir);
/* 731 */         binder.putLocal("ClusterNodeName", nodeName);
/* 732 */         binder.putLocal("ClusterGroup", adminServerIdcName);
/* 733 */         if (EnvUtils.getOSFamily().equals("unix"))
/*     */         {
/* 735 */           binder.putLocal("processController", this.m_targetDir + "/admin/bin/UnixProcCtrl");
/*     */         }
/*     */         else
/*     */         {
/* 740 */           binder.putLocal("processController", this.m_targetDir + "/admin/bin/NtProcCtrl.exe");
/*     */         }
/*     */ 
/* 743 */         String address = this.m_installer.getInstallValue("ClusterNodeAddress", null);
/*     */ 
/* 746 */         if (address == null)
/*     */         {
/* 748 */           address = this.m_installer.getInstallValue("ClusterNodeHostName", null);
/*     */         }
/*     */ 
/* 752 */         String[] list = IdcSystemConfig.CLUSTER_ADDRESS_SETTINGS;
/* 753 */         for (int i = 0; (address == null) && (i < list.length); ++i)
/*     */         {
/* 755 */           address = this.m_installer.getConfigValue(list[i]);
/*     */         }
/* 757 */         if (address == null)
/*     */         {
/* 759 */           Report.trace("install", "unable to compute a reasonable address for node " + nodeName + ", using 127.0.0.1", null);
/*     */ 
/* 762 */           address = "127.0.0.1";
/*     */         }
/* 764 */         binder.putLocal("SocketServerAddress", address);
/*     */ 
/* 766 */         list = new String[] { "HttpServerAddress", "IntradocServerPort", "HttpRelativeWebRoot", "HttpRelativeCgiRoot", "CgiFileName", "InstanceMenuLabel" };
/*     */ 
/* 770 */         for (int i = 0; i < list.length; ++i)
/*     */         {
/* 772 */           String key = list[i];
/* 773 */           String value = this.m_installer.getConfigValue(key);
/* 774 */           if (value == null)
/*     */             continue;
/* 776 */           binder.putLocal(key, value);
/*     */         }
/*     */ 
/* 779 */         binder.putLocal("serverActions", "stop,start,restart,query,read,write");
/*     */ 
/* 781 */         binder.putLocal("FileEncoding", DataSerializeUtils.getSystemEncoding());
/*     */ 
/* 784 */         binder.putLocal("IdcAdminServerPort", adminServerPort);
/* 785 */         Parameters adminServerProps = new PropParameters(binder.getLocalData());
/* 786 */         List adminServerRow = adminServers.createRow(adminServerProps);
/* 787 */         adminServers.addRowWithList(adminServerRow);
/*     */ 
/* 789 */         AdminService service = new AdminService();
/* 790 */         service.init(null, System.err, binder, new ServiceData());
/* 791 */         service.setAdminServerData(servers);
/* 792 */         service.initFileStoreObjects();
/* 793 */         service.saveServerData();
/*     */       }
/*     */       else
/*     */       {
/* 797 */         Report.trace("install", "a reference to a server " + idcName + "-" + nodeName + " already existed", null);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 803 */       throw new ServiceException(e);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 808 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void configureIIS() throws ServiceException
/*     */   {
/* 814 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 816 */     if ((!isWindows()) || (isAdminServer) || (!this.m_installer.getInstallBool("ClusterNodeConfigureIIS", true)))
/*     */     {
/* 819 */       return;
/*     */     }
/* 821 */     String webServer = null;
/* 822 */     if (this.m_installer.getInstallBool("ClusterNodeConfigureIIS", false))
/*     */     {
/* 826 */       webServer = "iis";
/* 827 */       this.m_installer.m_installerConfig.put("WebServer", webServer);
/*     */     }
/*     */     else
/*     */     {
/* 831 */       webServer = this.m_installer.getInstallValue("WebServer", null);
/*     */     }
/*     */ 
/* 834 */     if ((webServer == null) || (!webServer.equals("iis")))
/*     */     {
/* 836 */       return;
/*     */     }
/*     */ 
/* 839 */     boolean isUpdateSetting = this.m_installer.m_isUpdate;
/*     */     try
/*     */     {
/* 842 */       this.m_installer.m_isUpdate = true;
/* 843 */       this.m_installer.installSection("cluster-node-iis-remove-filter");
/*     */ 
/* 845 */       String myId = getMyId();
/* 846 */       this.m_installer.m_installerConfig.put("IISPluginName", "Idc Plugin " + myId);
/*     */ 
/* 848 */       this.m_installer.m_installerConfig.put("IISFilterFileName", "idc_cgi_isapi-" + myId + ".dll");
/*     */ 
/* 850 */       this.m_installer.m_installerConfig.put("CgiDir", this.m_targetDir + "/idcplg/");
/* 851 */       this.m_installer.installSection("cluster-node-iis-configure");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 859 */       this.m_installer.m_isUpdate = isUpdateSetting;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void configureWindowsServices() throws ServiceException
/*     */   {
/* 865 */     if ((!isWindows()) || (!this.m_installer.getInstallBool("ClusterNodeConfigureServices", true)))
/*     */     {
/* 868 */       return;
/*     */     }
/* 870 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 872 */     Properties props = this.m_installer.m_installerConfig;
/*     */     try
/*     */     {
/* 877 */       props.put("RegisterServers", "false");
/*     */ 
/* 880 */       props.put("IsUninstall", "true");
/* 881 */       this.m_installer.m_isUpdate = false;
/* 882 */       String installServerService = "auto";
/* 883 */       String installAdminServerService = "false";
/* 884 */       if (isAdminServer)
/*     */       {
/* 886 */         installServerService = "false";
/* 887 */         installAdminServerService = "auto";
/*     */       }
/* 889 */       if (props.get("InstallServerService") == null)
/*     */       {
/* 891 */         props.put("InstallServerService", installServerService);
/*     */       }
/* 893 */       if (props.get("InstallAdminServerService") == null)
/*     */       {
/* 895 */         props.put("InstallAdminServerService", installAdminServerService);
/*     */       }
/* 897 */       this.m_installer.installSection("core-registry");
/*     */ 
/* 900 */       props.remove("IsUninstall");
/* 901 */       props.put("ServicePathPrefix", this.m_targetDir + "/");
/* 902 */       this.m_installer.installSection("core-registry");
/* 903 */       props.remove("ServicePathPrefix");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 907 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void configureWindowsShortcuts()
/*     */     throws ServiceException
/*     */   {
/* 914 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 916 */     if ((isAdminServer) || (!isWindows()) || (!this.m_installer.getInstallBool("ClusterNodeConfigureShortcuts", true)))
/*     */     {
/* 919 */       return;
/*     */     }
/*     */ 
/* 922 */     String targetBinDir = this.m_targetDir + "/bin/";
/* 923 */     targetBinDir = FileUtils.directorySlashes(targetBinDir);
/* 924 */     this.m_installer.m_intradocConfig.put("BinDir", targetBinDir);
/*     */     try
/*     */     {
/* 927 */       this.m_installer.installSection("core-shortcuts");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 935 */       this.m_installer.m_intradocConfig.remove("BinDir");
/*     */     }
/*     */   }
/*     */ 
/*     */   public String computeTargetDirectory(String key, String defaultTarget, boolean useAdminPrefix)
/*     */   {
/* 942 */     boolean isAdminServer = this.m_installer.getInstallBool("ConfigureAdminClusterNode", false);
/*     */ 
/* 944 */     String adminPrefix = "";
/* 945 */     if (isAdminServer)
/*     */     {
/* 947 */       adminPrefix = "admin/";
/*     */     }
/* 949 */     String targetDir = this.m_targetDir + "/" + adminPrefix + defaultTarget;
/* 950 */     targetDir = this.m_installer.getInstallValue(key, targetDir);
/* 951 */     targetDir = FileUtils.directorySlashes(targetDir);
/* 952 */     return targetDir;
/*     */   }
/*     */ 
/*     */   public String getMyId() throws ServiceException
/*     */   {
/* 957 */     String id = this.m_installer.getInstallValue("IDC_Name", null);
/* 958 */     String node = this.m_installer.getInstallValue("ClusterNodeName", null);
/* 959 */     if (id == null)
/*     */     {
/* 962 */       throw new ServiceException("!$The IDC_Name is not defined.");
/*     */     }
/* 964 */     if (node == null)
/*     */     {
/* 967 */       throw new ServiceException("!$The ClusterNodeName is not defined.");
/*     */     }
/* 969 */     String myId = id + "-" + node;
/* 970 */     return myId;
/*     */   }
/*     */ 
/*     */   public String fixPathName(String str)
/*     */   {
/* 975 */     while ((index = str.indexOf("'")) >= 0)
/*     */     {
/*     */       int index;
/* 977 */       str = str.substring(0, index) + "\\'" + str.substring(index + 1);
/*     */     }
/*     */ 
/* 980 */     return str;
/*     */   }
/*     */ 
/*     */   public boolean isWindows()
/*     */   {
/* 985 */     return this.m_installer.isWindows();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 990 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ClusterTool
 * JD-Core Version:    0.5.4
 */