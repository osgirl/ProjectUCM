/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.ServerOSSettingsHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class StartOrStopServers
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */   public boolean m_isWindows;
/*     */   public final String ACTION_START = "start";
/*     */   public final String ACTION_STOP = "stop";
/*     */   public final String ACTION_QUERY = "query";
/*     */   public final int RUNNING = 0;
/*     */   public final int NOT_RUNNING = 1;
/*     */   public Map m_osOptions;
/*     */   public int m_actionCount;
/*     */   public int m_actionTotal;
/*     */ 
/*     */   public StartOrStopServers()
/*     */   {
/*  38 */     this.ACTION_START = "start";
/*  39 */     this.ACTION_STOP = "stop";
/*  40 */     this.ACTION_QUERY = "query";
/*     */ 
/*  42 */     this.RUNNING = 0;
/*  43 */     this.NOT_RUNNING = 1;
/*     */ 
/*  45 */     this.m_osOptions = null;
/*     */ 
/*  47 */     this.m_actionCount = 0;
/*  48 */     this.m_actionTotal = 1;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  54 */     if (EnvUtils.m_osHelper == null)
/*     */     {
/*  57 */       Report.trace("install", "using ServerOSSettingsHelper()", null);
/*  58 */       EnvUtils.m_osHelper = new ServerOSSettingsHelper();
/*     */     }
/*     */ 
/*  61 */     if (this.m_osOptions == null)
/*     */     {
/*  63 */       this.m_osOptions = new HashMap();
/*  64 */       DataResultSet platformConfigTable = (DataResultSet)installer.m_binder.getResultSet("PlatformConfigTable");
/*     */ 
/*  66 */       this.m_osOptions.put("PlatformConfigTable", platformConfigTable);
/*  67 */       this.m_osOptions.put("type_executable", "type_executable");
/*     */ 
/*  70 */       this.m_osOptions.put("base_directory", installer.computeDestinationEx("${IdcHomeDir}/native", false));
/*     */     }
/*     */ 
/*  74 */     this.m_installer = installer;
/*  75 */     this.m_isWindows = installer.isWindows();
/*     */ 
/*  80 */     if (name.equals("core-preupdate"))
/*     */     {
/*  82 */       preupdate();
/*     */     }
/*  84 */     else if (name.equals("core-postinstall"))
/*     */     {
/*  86 */       postinstall();
/*     */     }
/*  88 */     else if (name.equals("start-server"))
/*     */     {
/*  90 */       startServer(this.m_installer);
/*     */     }
/*  92 */     else if (name.equals("stop-server"))
/*     */     {
/*  94 */       stopServer(this.m_installer);
/*     */     }
/*  96 */     return 0;
/*     */   }
/*     */ 
/*     */   public void preupdate() throws ServiceException
/*     */   {
/* 101 */     stopAllServers();
/*     */   }
/*     */ 
/*     */   public void postinstall() throws ServiceException
/*     */   {
/* 106 */     String databaseType = this.m_installer.getInstallValue("DatabaseType", null);
/*     */ 
/* 108 */     String installService = this.m_installer.getInstallValue("InstallServerService", "false");
/*     */ 
/* 110 */     boolean serviceInstalled = false;
/* 111 */     if ((installService.equalsIgnoreCase("true")) || (installService.equalsIgnoreCase("auto")) || (EnvUtils.isFamily("unix")))
/*     */     {
/* 115 */       serviceInstalled = true;
/*     */     }
/* 117 */     boolean adminServiceInstalled = isAdminServiceInstalled();
/*     */ 
/* 119 */     if (this.m_installer.m_isUpdate)
/*     */     {
/* 121 */       startAllServers();
/* 122 */       return;
/*     */     }
/*     */ 
/* 125 */     ArrayList serverList = new ArrayList();
/*     */ 
/* 127 */     boolean databaseOk = (databaseType == null) || (!databaseType.equals("skip_database"));
/*     */ 
/* 130 */     if ((databaseOk) && (serviceInstalled))
/*     */     {
/* 132 */       serverList.add(new String[] { this.m_installer.m_idcDir, "csInstallerUnableToStartServer", "start" });
/*     */     }
/*     */ 
/* 136 */     String adminConfig = this.m_installer.getInstallValue("ConfigureAdminServer", "true");
/*     */ 
/* 138 */     if ((adminConfig.equalsIgnoreCase("configure_existing")) || (StringUtils.convertToBool(adminConfig, false)) || (adminServiceInstalled))
/*     */     {
/* 143 */       String adminServerDir = this.m_installer.getInstallValue("AdminServerDir", null);
/*     */ 
/* 145 */       if (adminServerDir == null)
/*     */       {
/* 147 */         String masterServerDir = this.m_installer.getInstallValue("MasterServerDir", null);
/*     */ 
/* 149 */         if (masterServerDir != null)
/*     */         {
/* 151 */           adminServerDir = masterServerDir + "/admin";
/*     */         }
/*     */       }
/* 154 */       if (adminServerDir == null)
/*     */       {
/* 156 */         adminServerDir = this.m_installer.m_idcDir + "/admin";
/*     */       }
/*     */ 
/* 159 */       boolean suppressError = false;
/* 160 */       if (FileUtils.checkFile(adminServerDir + "/config/config.cfg", true, false) != 0)
/*     */       {
/* 163 */         Report.trace("install", adminServerDir + " missing config/config.cfg, not using.", null);
/*     */ 
/* 165 */         adminServerDir = null;
/* 166 */         suppressError = true;
/*     */       }
/*     */ 
/* 169 */       if (adminServerDir == null)
/*     */       {
/* 171 */         if (!suppressError)
/*     */         {
/* 173 */           this.m_installer.m_installLog.error("!csInstallerMasterServerDirUndefined");
/* 174 */           Report.trace("install", "unable to find an admin server to start.", null);
/*     */         }
/*     */ 
/*     */       }
/* 180 */       else if (StringUtils.convertToBool(adminConfig, false))
/*     */       {
/* 182 */         serverList.add(new String[] { adminServerDir, "csInstallerUnableToRestartAdminServer", "start_admin" });
/*     */       }
/*     */       else
/*     */       {
/* 188 */         serverList.add(new String[] { adminServerDir, "csInstallerUnableToRestartAdminServer", "restart_admin" });
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 195 */     String proxiedType = this.m_installer.getInstallValue("ConfigureProxiedServer", "false");
/*     */ 
/* 197 */     if (proxiedType.equalsIgnoreCase("local_server"))
/*     */     {
/* 200 */       String masterServerDir = this.m_installer.getInstallValue("MasterServerDir", null);
/*     */ 
/* 202 */       if (masterServerDir == null)
/*     */       {
/* 204 */         Report.trace("install", "unable to find master server to restart.", null);
/*     */       }
/*     */       else
/*     */       {
/* 209 */         serverList.add(new String[] { masterServerDir, "csInstallerUnableToRestartServer", "restart" });
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 215 */     this.m_actionTotal = (2 * serverList.size());
/* 216 */     for (int i = 0; i < serverList.size(); ++i)
/*     */     {
/* 218 */       String[] serverData = (String[])(String[])serverList.get(i);
/* 219 */       String targetDir = serverData[0];
/* 220 */       String errMsg = serverData[1];
/* 221 */       String action = serverData[2];
/*     */       try
/*     */       {
/* 224 */         SysInstaller installer = this.m_installer.deriveInstaller(targetDir);
/*     */ 
/* 226 */         installer.loadConfig();
/* 227 */         int index = action.indexOf("_admin");
/* 228 */         if (index > 0)
/*     */         {
/* 230 */           action = action.substring(0, index);
/* 231 */           installer.m_installerConfig.put("IsAdminServer", "true");
/*     */         }
/* 233 */         if (action.equals("restart"))
/*     */         {
/* 235 */           stopServer(installer);
/* 236 */           this.m_actionCount += 1;
/*     */         }
/*     */         else
/*     */         {
/* 240 */           this.m_actionTotal -= 1;
/*     */         }
/* 242 */         startServer(installer);
/* 243 */         this.m_actionCount += 1;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 247 */         String msg = LocaleUtils.encodeMessage(errMsg, e.getMessage(), targetDir);
/*     */ 
/* 249 */         this.m_installer.m_installLog.warning(msg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void stopAllServers() throws ServiceException
/*     */   {
/* 256 */     if (this.m_installer.isOlderVersion("4"))
/*     */     {
/* 258 */       Report.trace("install", "not stopping pre-4.x servers", null);
/*     */ 
/* 260 */       return;
/*     */     }
/*     */ 
/* 263 */     Report.trace("install", "stopping all servers", null);
/* 264 */     Vector list = getAllServerList();
/* 265 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/* 267 */       SysInstaller installer = (SysInstaller)list.elementAt(i);
/*     */ 
/* 269 */       installer.loadConfig();
/* 270 */       Report.trace("install", "stopping server at " + installer.getConfigValue("IntradocDir"), null);
/*     */ 
/* 272 */       stopServer(installer);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void startAllServers() throws ServiceException
/*     */   {
/* 278 */     Report.trace("install", "starting all servers", null);
/* 279 */     Vector list = getAllServerList();
/* 280 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/* 282 */       SysInstaller installer = (SysInstaller)list.elementAt(i);
/*     */ 
/* 284 */       startServer(installer);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Vector getAllServerList()
/*     */     throws ServiceException
/*     */   {
/* 292 */     String proxiedType = this.m_installer.getInstallValue("ConfigureProxiedServer", "no");
/* 293 */     boolean isProxied = (this.m_installer.getInstallBool("IsProxiedServer", false)) || (StringUtils.convertToBool(proxiedType, true));
/*     */ 
/* 295 */     String adminConfig = this.m_installer.getInstallValue("ConfigureAdminServer", "true");
/* 296 */     boolean isConfigureAdmin = StringUtils.convertToBool(adminConfig, false);
/*     */ 
/* 298 */     Vector list = new IdcVector();
/*     */ 
/* 300 */     String dir = this.m_installer.computeDestinationEx("admin/data/servers/", false);
/*     */ 
/* 302 */     boolean hasAdminReference = (((!isProxied) || (isConfigureAdmin))) && (FileUtils.checkFile(dir, false, false) == 0);
/*     */ 
/* 304 */     if (hasAdminReference)
/*     */     {
/* 306 */       DataBinder serversBinder = new DataBinder();
/* 307 */       ResourceUtils.serializeDataBinder(dir, "servers.hda", serversBinder, false, false);
/*     */ 
/* 309 */       DataResultSet drset = (DataResultSet)serversBinder.getResultSet("ServerDefinition");
/*     */ 
/* 311 */       if (drset != null)
/*     */       {
/* 313 */         Report.trace("install", "retrieving all servers from admin list.", null);
/* 314 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 316 */           Properties props = drset.getCurrentRowProps();
/* 317 */           String idcName = props.getProperty("IDC_Id");
/* 318 */           if (idcName == null)
/*     */           {
/* 320 */             idcName = props.getProperty("IDC_Name");
/*     */           }
/* 322 */           String serverInfoDir = "admin/data/servers/" + idcName;
/* 323 */           serverInfoDir = this.m_installer.computeDestinationEx(serverInfoDir, false);
/*     */ 
/* 325 */           if (serverInfoDir == null)
/*     */             continue;
/* 327 */           DataBinder binder = new DataBinder();
/* 328 */           ResourceUtils.serializeDataBinder(serverInfoDir, "server.hda", binder, false, false);
/*     */ 
/* 330 */           String idcDir = binder.getLocal("IntradocDir");
/* 331 */           if (idcDir == null)
/*     */             continue;
/*     */           try
/*     */           {
/* 335 */             SysInstaller installer = this.m_installer.deriveInstaller(idcDir);
/*     */ 
/* 337 */             installer.loadConfig();
/* 338 */             if (!idcDir.equals(this.m_installer.m_idcDir))
/*     */             {
/* 340 */               installer.m_installerConfig.put("MasterServerDir", this.m_installer.m_idcDir);
/*     */             }
/*     */ 
/* 343 */             list.addElement(installer);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 347 */             Report.trace("install", "Unable to add server at \"" + idcDir + "\" to the server list.", e);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 361 */         Report.trace("install", "load proxied in start/stop list.", null);
/* 362 */         SysInstaller server = this.m_installer.deriveInstaller(this.m_installer.m_idcDir);
/* 363 */         server.loadConfig();
/* 364 */         list.addElement(server);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 368 */         Report.trace("install", "Unable to add server at \"" + this.m_installer.m_idcDir + "\" to the server list.", e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 374 */     boolean adminServiceInstalled = isAdminServiceInstalled();
/* 375 */     if ((hasAdminReference) && (((adminConfig.equalsIgnoreCase("configure_existing")) || (isConfigureAdmin) || (adminServiceInstalled))))
/*     */     {
/* 378 */       Report.trace("install", "getting admin server's start/stop information: adminConfig=" + adminConfig, null);
/* 379 */       dir = this.m_installer.computeDestinationEx("admin/", false);
/* 380 */       if (dir != null)
/*     */       {
/* 382 */         if (FileUtils.checkFile(dir, false, false) == 0)
/*     */         {
/*     */           try
/*     */           {
/* 386 */             SysInstaller adminServer = this.m_installer.deriveInstaller(dir);
/* 387 */             adminServer.loadConfig();
/* 388 */             adminServer.m_installerConfig.put("IsAdminServer", "true");
/* 389 */             list.addElement(adminServer);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 393 */             Report.trace("install", "Unable to add server at \"" + dir + "\" to the server list.", e);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 400 */           Report.trace("install", "admin directory not found, assuming no admin server.", null);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 406 */     return list;
/*     */   }
/*     */ 
/*     */   public boolean isAdminServiceInstalled()
/*     */   {
/* 411 */     boolean adminServiceInstalled = false;
/* 412 */     String installAdminService = this.m_installer.getInstallValue("InstallAdminServerService", "false");
/*     */ 
/* 414 */     if ((installAdminService.equalsIgnoreCase("true")) || (installAdminService.equalsIgnoreCase("auto")) || (EnvUtils.isFamily("unix")))
/*     */     {
/* 418 */       adminServiceInstalled = true;
/*     */     }
/* 420 */     return adminServiceInstalled;
/*     */   }
/*     */ 
/*     */   public int checkServer(SysInstaller installer)
/*     */     throws ServiceException
/*     */   {
/* 426 */     String[] cmd = buildServerCommand(installer, "query");
/* 427 */     Vector results = new IdcVector();
/* 428 */     String msg = LocaleUtils.encodeMessage("csInstallerUnableToStopServer", null, installer.getInstallValue("IntradocDir", null));
/*     */ 
/* 431 */     int rc = this.m_installer.runCommand(cmd, results, msg, false, true);
/* 432 */     if ((rc == 0) && (results.size() > 0))
/*     */     {
/* 434 */       String resultStr = (String)results.elementAt(0);
/* 435 */       if (resultStr.indexOf(" Stopped") > 0)
/*     */       {
/* 437 */         rc = 1;
/*     */       }
/*     */     }
/* 440 */     return rc;
/*     */   }
/*     */ 
/*     */   public int stopServer(SysInstaller installer)
/*     */     throws ServiceException
/*     */   {
/* 446 */     int rc = 0;
/* 447 */     if (checkServer(installer) == 0)
/*     */     {
/* 449 */       String[] cmd = buildServerCommand(installer, "stop");
/* 450 */       Vector results = new IdcVector();
/* 451 */       String msg = LocaleUtils.encodeMessage("csInstallerUnableToStopServer", null, installer.getInstallValue("IntradocDir", null));
/*     */ 
/* 454 */       rc = this.m_installer.runCommand(cmd, results, msg, false, true);
/*     */     }
/*     */     else
/*     */     {
/* 458 */       Report.trace("install", "not stopping, server is not running", null);
/*     */     }
/* 460 */     return rc;
/*     */   }
/*     */ 
/*     */   public int startServer(SysInstaller installer)
/*     */     throws ServiceException
/*     */   {
/* 468 */     String[] cmd = buildServerCommand(installer, "start");
/* 469 */     Vector results = new IdcVector();
/* 470 */     String msg = LocaleUtils.encodeMessage("csInstallerUnableToStartServer", null, installer.getInstallValue("IntradocDir", null));
/*     */ 
/* 473 */     String nameKey = "IDC_Name";
/* 474 */     if (installer.getInstallBool("IsAdminServer", false))
/*     */     {
/* 476 */       nameKey = "IDC_Admin_Name";
/*     */     }
/* 478 */     IdcMessage startMsg = IdcMessageFactory.lc("csServiceStart", new Object[] { installer.getInstallValue(nameKey, null) });
/*     */ 
/* 480 */     this.m_installer.reportProgress(0, LocaleUtils.encodeMessage(startMsg), this.m_actionCount, this.m_actionTotal);
/*     */ 
/* 482 */     int rc = this.m_installer.runCommand(cmd, results, msg, false, true);
/* 483 */     return rc;
/*     */   }
/*     */ 
/*     */   protected String[] buildServerCommand(SysInstaller installer, String action)
/*     */     throws ServiceException
/*     */   {
/* 490 */     Vector v = new IdcVector();
/* 491 */     installer.getInstallValue("IntradocDir", null);
/* 492 */     boolean isAdminServer = false;
/* 493 */     String tmp = installer.m_installerConfig.getProperty("IsAdminServer");
/* 494 */     isAdminServer = StringUtils.convertToBool(tmp, false);
/*     */ 
/* 497 */     boolean hasLocalAdmin = this.m_installer.checkConfigureAdminServer(2);
/*     */ 
/* 499 */     if (this.m_isWindows)
/*     */     {
/* 501 */       String procCtrl = null;
/*     */       String idcName;
/* 503 */       if (isAdminServer)
/*     */       {
/* 505 */         procCtrl = "bin/NtProcCtrl";
/* 506 */         String idcName = installer.getInstallValue("IDC_Admin_Name", null);
/* 507 */         if (idcName == null)
/*     */         {
/* 509 */           idcName = "idc_admin";
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 515 */         Map r = EnvUtils.normalizeOSPath("bin/NtProcCtrl", this.m_osOptions);
/* 516 */         procCtrl = (String)r.get("path");
/* 517 */         idcName = installer.getInstallValue("IDC_Name", null);
/*     */       }
/* 519 */       procCtrl = this.m_installer.getInstallValue("NtProcCtrlPath", procCtrl);
/* 520 */       procCtrl = installer.computeDestinationEx(procCtrl, false);
/*     */ 
/* 522 */       String serviceName = installer.getInstallValue("WindowsServiceName", null);
/*     */ 
/* 524 */       if (serviceName == null)
/*     */       {
/* 526 */         if (isAdminServer)
/*     */         {
/* 528 */           if ((this.m_installer.m_isUpdate) && (this.m_installer.isOlderVersion("7.0")))
/*     */           {
/* 530 */             serviceName = "IdcAdminService";
/* 531 */             this.m_installer.editConfigFile("admin/config/config.cfg", "WindowsServiceName", serviceName);
/*     */           }
/*     */           else
/*     */           {
/* 536 */             serviceName = "IdcAdminService " + idcName;
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 541 */           serviceName = buildServiceOrScriptName(installer, idcName);
/*     */         }
/*     */       }
/*     */ 
/* 545 */       v.addElement(procCtrl);
/* 546 */       v.addElement(action);
/* 547 */       v.addElement(serviceName);
/*     */     }
/*     */     else
/*     */     {
/* 551 */       String envUtil = this.m_installer.getInstallValue("EnvUtilityPath", "/usr/bin/env");
/*     */ 
/* 553 */       v.addElement(envUtil);
/* 554 */       v.addElement(EnvUtils.getLibraryPathEnvironmentVariableName() + "=");
/*     */       String procCtrl;
/*     */       String procCtrl;
/* 556 */       if (installer == this.m_installer)
/*     */       {
/* 558 */         procCtrl = null;
/*     */       }
/*     */       else
/*     */       {
/*     */         String procCtrl;
/* 562 */         if (isAdminServer)
/*     */         {
/* 564 */           procCtrl = "bin/UnixProcCtrl";
/*     */         }
/*     */         else
/*     */         {
/* 568 */           Map r = EnvUtils.normalizeOSPath("bin/UnixProcCtrl", this.m_osOptions);
/* 569 */           procCtrl = (String)r.get("path");
/*     */         }
/* 571 */         if (procCtrl != null)
/*     */         {
/* 576 */           procCtrl = installer.computeDestinationEx(procCtrl, false);
/*     */         }
/*     */       }
/*     */       String prefix;
/* 582 */       if (isAdminServer)
/*     */       {
/* 584 */         String script = "idcadmin_";
/*     */         String prefix;
/* 585 */         if (hasLocalAdmin)
/*     */         {
/* 587 */           prefix = "${AdminServerDir}";
/*     */         }
/*     */         else
/*     */         {
/* 591 */           prefix = "";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 596 */         script = buildServiceOrScriptName(installer, null);
/* 597 */         prefix = "";
/*     */       }
/*     */ 
/* 600 */       String prefix = FileUtils.directorySlashes(prefix);
/* 601 */       if (this.m_installer.getInstallValue("AdminServerDir", null) == null)
/*     */       {
/* 603 */         String masterDir = this.m_installer.getInstallValue("MasterServerDir", null);
/* 604 */         if (masterDir != null)
/*     */         {
/* 606 */           String adminDir = FileUtils.directorySlashes(masterDir) + "/admin";
/* 607 */           this.m_installer.m_installerConfig.put("AdminServerDir", adminDir);
/*     */         }
/*     */       }
/* 610 */       String script = installer.computeDestinationEx(prefix + "etc/" + script + action, false);
/*     */ 
/* 612 */       if (isAdminServer)
/*     */       {
/* 614 */         v.addElement(script);
/*     */       }
/*     */       else
/*     */       {
/* 618 */         if (procCtrl != null)
/*     */         {
/* 620 */           v.addElement(procCtrl);
/* 621 */           v.addElement("exec");
/*     */         }
/* 623 */         v.addElement(script);
/*     */       }
/*     */     }
/*     */ 
/* 627 */     String[] cmd = new String[v.size()];
/* 628 */     cmd = (String[])(String[])v.toArray(cmd);
/* 629 */     if (SystemUtils.isActiveTrace("install"))
/*     */     {
/* 631 */       IdcStringBuilder msg = new IdcStringBuilder("built command");
/*     */ 
/* 633 */       for (int i = 0; i < cmd.length; ++i)
/*     */       {
/* 635 */         msg.append(' ');
/* 636 */         msg.append(cmd[i]);
/*     */       }
/* 638 */       Report.trace("install", msg.toString(), null);
/*     */     }
/* 640 */     return cmd;
/*     */   }
/*     */ 
/*     */   protected String buildServiceOrScriptName(SysInstaller installer, String idcName)
/*     */   {
/* 645 */     String buildName = "<unknown>";
/* 646 */     String prodName = installer.getInstallValue("IdcProductName", null);
/* 647 */     if (prodName == null)
/*     */     {
/* 649 */       if (this.m_isWindows == true)
/*     */       {
/* 651 */         buildName = "IdcContentService";
/*     */       }
/*     */       else
/*     */       {
/* 655 */         buildName = "idcserver_";
/*     */       }
/*     */     }
/*     */ 
/* 659 */     DataResultSet productAttributes = (DataResultSet)installer.m_binder.getResultSet("ProductAttributes");
/*     */ 
/* 661 */     if (productAttributes != null)
/*     */     {
/* 663 */       int retCol = (this.m_isWindows) ? 1 : 2;
/* 664 */       for (int i = 0; i < productAttributes.getNumRows(); ++i)
/*     */       {
/* 666 */         Vector row = productAttributes.getRowValues(i);
/* 667 */         String name = (String)row.elementAt(0);
/* 668 */         if (!prodName.equalsIgnoreCase(name))
/*     */           continue;
/* 670 */         buildName = (String)row.elementAt(retCol);
/* 671 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 676 */     if ((this.m_isWindows) && (idcName != null))
/*     */     {
/* 678 */       buildName = buildName + " " + idcName;
/*     */     }
/* 680 */     return buildName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 685 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.StartOrStopServers
 * JD-Core Version:    0.5.4
 */