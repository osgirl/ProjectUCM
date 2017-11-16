/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.apputilities.installer.MigrateUtils;
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DatabaseTypes;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.IdocFileBuilder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.Action;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.server.proxy.ProviderFileUtils;
/*     */ import intradoc.server.proxy.ProviderUtils;
/*     */ import intradoc.server.publish.WebPublishUtils;
/*     */ import intradoc.server.utils.ServerInstallUtils;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.FileReader;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class WlsServiceHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void generateIdocFileFromResourcesList()
/*     */     throws DataException, ServiceException
/*     */   {
/*  88 */     String resourceListTableName = this.m_currentAction.getParamAt(0);
/*  89 */     String filename = this.m_binder.getLocal("fileName");
/*  90 */     if (filename == null)
/*     */     {
/*  92 */       throw new DataException(null, "syParameterNotFound", new Object[] { "fileName" });
/*     */     }
/*  94 */     Properties env = SharedObjects.getSecureEnvironment();
/*  95 */     int flags = PathUtils.F_VARS_MUST_EXIST;
/*  96 */     String pathname = PathUtils.substitutePathVariables(filename, env, null, flags, null);
/*  97 */     String dirname = FileUtils.getDirectory(pathname);
/*  98 */     File dir = new File(dirname);
/*  99 */     dir.mkdirs();
/* 100 */     FileUtils.checkOrCreateDirectory(dirname, 1);
/* 101 */     PageMerger merger = this.m_service.getPageMerger();
/* 102 */     IdocFileBuilder builder = new IdocFileBuilder();
/* 103 */     builder.m_alignDynamicDataColumns = true;
/*     */     try
/*     */     {
/* 106 */       builder.init(this.m_binder, merger, pathname);
/* 107 */       builder.appendResourcesList(resourceListTableName);
/* 108 */       builder.close();
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 112 */       throw new ServiceException(ioe);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareSystemConfigPage() throws DataException, ServiceException
/*     */   {
/* 119 */     prepareSystemConfigPageCommon();
/*     */ 
/* 121 */     String pageType = this.m_binder.getAllowMissing("SystemConfigPageType");
/* 122 */     if (pageType != null)
/*     */     {
/* 124 */       if (SystemUtils.m_verbose)
/*     */       {
/* 126 */         Report.trace("install", "preparing system config page for type " + pageType, null);
/*     */       }
/* 128 */       if (pageType.equals("install"))
/*     */       {
/* 130 */         preparePostInstallConfigPage();
/*     */       } else {
/* 132 */         if (!pageType.equals("upgrade"))
/*     */           break label83;
/* 134 */         loadMigrateState();
/* 135 */         loadMigrateLogs();
/*     */       }
/*     */ 
/* 141 */       return;
/*     */     }
/* 143 */     label83: throw new DataException(null, "syParameterNotFound", new Object[] { "SystemConfigPageType" });
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareSystemConfigPageCommon()
/*     */   {
/* 149 */     this.m_binder.putLocal("IntradocDir", SharedObjects.getEnvironmentValue("IntradocDir"));
/* 150 */     this.m_binder.putLocal("VaultDir", SharedObjects.getEnvironmentValue("VaultDir"));
/* 151 */     this.m_binder.putLocal("UserProfilesDir", LegacyDirectoryLocator.getUserProfilesDir());
/* 152 */     String weblayoutDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 153 */     String isCSonWebsphere = "false";
/*     */ 
/* 157 */     if ((EnvUtils.isAppServerType("websphere")) && ("idccs".equals(EnvUtils.getProductName())))
/*     */     {
/* 159 */       isCSonWebsphere = "true";
/* 160 */       String webroot = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*     */ 
/* 162 */       if (weblayoutDir.endsWith(webroot))
/*     */       {
/* 164 */         weblayoutDir = weblayoutDir.substring(0, weblayoutDir.lastIndexOf(webroot));
/*     */       }
/*     */     }
/* 167 */     this.m_binder.putLocal("IsCSonWebsphere", isCSonWebsphere);
/* 168 */     this.m_binder.putLocal("WeblayoutDir", weblayoutDir);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void preparePostInstallConfigPage() throws ServiceException, DataException
/*     */   {
/* 174 */     if (SharedObjects.getEnvValueAsBoolean("PostInstallComplete", false))
/*     */     {
/* 176 */       this.m_binder.putLocal("isSimplePage", "1");
/* 177 */       IdcMessage msg = IdcMessageFactory.lc("csPostInstallConfigAlreadyDoneRequiresRestart", new Object[0]);
/* 178 */       this.m_service.createServiceException(msg);
/*     */     }
/* 180 */     if (Features.checkLevel("ContentManagement", null))
/*     */     {
/* 183 */       ResultSet rset = this.m_workspace.createResultSetSQL("SELECT * FROM Revisions");
/* 184 */       if ((rset != null) && (rset.first()))
/*     */       {
/* 187 */         String existingVal = this.m_binder.getLocal("forceAttachToExistingServer");
/* 188 */         if (existingVal == null)
/*     */         {
/* 191 */           this.m_binder.putLocal("forceAttachToExistingServer", "1");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 201 */     String serverPort = SharedObjects.getEnvironmentValue("IntradocServerPort");
/* 202 */     if (serverPort == null)
/*     */     {
/* 204 */       serverPort = "";
/*     */     }
/* 206 */     this.m_binder.putLocal("IntradocServerPort", serverPort);
/*     */ 
/* 208 */     String socketFilter = SharedObjects.getEnvironmentValue("SocketHostAddressSecurityFilter");
/* 209 */     if (socketFilter == null)
/*     */     {
/* 211 */       socketFilter = "127.0.0.1|0:0:0:0:0:0:0:1";
/*     */     }
/* 213 */     this.m_binder.putLocal("SocketHostAddressSecurityFilter", socketFilter);
/*     */ 
/* 215 */     String mailServer = SharedObjects.getEnvironmentValue("MailServer");
/* 216 */     if (mailServer == null)
/*     */     {
/* 218 */       mailServer = "mail";
/*     */     }
/* 220 */     this.m_binder.putLocal("MailServer", mailServer);
/*     */ 
/* 222 */     String sysadminAddress = SharedObjects.getEnvironmentValue("SysAdminAddress");
/* 223 */     if (sysadminAddress == null)
/*     */     {
/* 225 */       sysadminAddress = "sysadmin@example.com";
/*     */     }
/* 227 */     this.m_binder.putLocal("SysAdminAddress", sysadminAddress);
/*     */ 
/* 229 */     String prefix = SharedObjects.getEnvironmentValue("AutoNumberPrefix");
/* 230 */     if ((prefix == null) || (prefix.length() == 0))
/*     */     {
/* 232 */       String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 233 */       if (idcName.length() > 14)
/*     */       {
/* 235 */         idcName = idcName.substring(0, 14);
/*     */       }
/* 237 */       prefix = idcName;
/* 238 */       this.m_binder.putLocal("AutoNumberPrefix", prefix);
/*     */     }
/*     */ 
/* 241 */     if ((this.m_workspace != null) && ((
/* 243 */       (WorkspaceUtils.isDatabaseType(this.m_workspace, DatabaseTypes.ORACLE)) || (WorkspaceUtils.isDatabaseType(this.m_workspace, DatabaseTypes.MSSQL)))))
/*     */     {
/* 245 */       this.m_binder.putLocal("isSupportedSearchConfig", "1");
/*     */     }
/*     */ 
/* 250 */     boolean forcePostInstallConf = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("ForceInitialPostInstallConfiguration"), false);
/* 251 */     String installConfPath = FileUtils.directorySlashes(DirectoryLocator.getIntradocDir()) + "install/installconf.hda";
/* 252 */     int fileCheck = FileUtils.checkFile(installConfPath, false, false);
/* 253 */     if (SystemUtils.m_verbose)
/*     */     {
/* 255 */       if (forcePostInstallConf)
/*     */       {
/* 257 */         Report.trace("install", "ForceInitialPostInstallConfiguration = 1", null);
/*     */       }
/* 259 */       else if (fileCheck != -16)
/*     */       {
/* 261 */         Report.trace("install", "found " + installConfPath, null);
/*     */       }
/*     */     }
/* 264 */     if ((fileCheck == -16) || (forcePostInstallConf))
/*     */       return;
/* 266 */     this.m_service.createServiceExceptionEx(null, "!csPostInstallConfigAlreadyDone", fileCheck);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void doPostInstallConfig()
/*     */     throws ServiceException, DataException
/*     */   {
/* 274 */     String intradocDir = DirectoryLocator.getIntradocDir();
/* 275 */     String newIntradocDir = this.m_binder.getLocal("IntradocDir");
/* 276 */     if ((newIntradocDir == null) || (newIntradocDir.length() == 0))
/*     */     {
/* 278 */       this.m_service.createServiceException(IdcMessageFactory.lc("csRequiredFieldMissing", new Object[] { "IntradocDir" }));
/*     */     }
/* 280 */     newIntradocDir = FileUtils.fixDirectorySlashes(newIntradocDir, 78).toString();
/* 281 */     this.m_binder.putLocal("IntradocDir", newIntradocDir);
/* 282 */     this.m_binder.putLocal("OldIntradocDir", intradocDir);
/*     */ 
/* 286 */     String installConfDir = newIntradocDir + "install/";
/* 287 */     String installConfFile = "installconf.hda";
/*     */ 
/* 289 */     boolean isIdcDirChanged = false;
/* 290 */     if (EnvUtils.isFamily("windows"))
/*     */     {
/* 292 */       isIdcDirChanged = !intradocDir.equalsIgnoreCase(newIntradocDir);
/*     */     }
/*     */     else
/*     */     {
/* 296 */       isIdcDirChanged = !intradocDir.equals(newIntradocDir);
/*     */     }
/* 298 */     if ((isIdcDirChanged) && (SystemUtils.m_verbose))
/*     */     {
/* 300 */       Report.trace("install", "IntradocDir changed from " + intradocDir + " to " + newIntradocDir, null);
/*     */     }
/*     */ 
/* 304 */     SystemPropertiesEditor sysEditor = new SystemPropertiesEditor();
/*     */     try
/*     */     {
/* 307 */       sysEditor.initIdc();
/* 308 */       String idcFile = sysEditor.getIdcFile();
/* 309 */       boolean loadedCfgFile = false;
/* 310 */       if (isIdcDirChanged)
/*     */       {
/* 312 */         String cfgFile = FileUtils.getAbsolutePath(newIntradocDir, "config/config.cfg");
/*     */ 
/* 315 */         FileUtils.checkOrCreateDirectory(newIntradocDir + "/config", 6);
/* 316 */         if (FileUtils.checkFile(cfgFile, 1) != 0)
/*     */         {
/* 319 */           sysEditor.initConfig();
/* 320 */           loadedCfgFile = true;
/*     */         }
/* 322 */         sysEditor.setFilepaths(idcFile, cfgFile);
/*     */ 
/* 325 */         FileUtils.checkOrCreateDirectory(newIntradocDir + "/install", 2);
/*     */       }
/*     */       else
/*     */       {
/* 330 */         boolean forcePostInstallConf = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("ForceInitialPostInstallConfiguration"), false);
/*     */ 
/* 332 */         int fileCheck = FileUtils.checkFile(installConfDir + installConfFile, false, false);
/* 333 */         if ((fileCheck != -16) && (!forcePostInstallConf))
/*     */         {
/* 335 */           this.m_service.createServiceException(IdcMessageFactory.lc("csPostInstallConfigAlreadyDone", new Object[0]));
/*     */         }
/*     */       }
/*     */ 
/* 339 */       if (!loadedCfgFile)
/*     */       {
/* 348 */         sysEditor.getCfgVector().add("SearchIndexerEngineName");
/*     */ 
/* 350 */         sysEditor.initConfig();
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 355 */       this.m_service.createServiceException(e, "!csUnableToReadCurrentConfigFiles");
/*     */     }
/*     */ 
/* 359 */     if ((EnvUtils.isAppServerType("websphere")) && ("idccs".equals(EnvUtils.getProductName())))
/*     */     {
/* 361 */       String weblayoutDir = FileUtils.directorySlashes(this.m_binder.getLocal("WeblayoutDir"));
/* 362 */       String webroot = FileUtils.directorySlashes(this.m_binder.getLocal("HttpRelativeWebRoot"));
/*     */ 
/* 364 */       if (webroot.length() > 0)
/*     */       {
/* 366 */         weblayoutDir = weblayoutDir + webroot.substring(1);
/*     */       }
/* 368 */       this.m_binder.putLocal("WeblayoutDir", weblayoutDir);
/*     */     }
/*     */ 
/* 377 */     if (DataBinderUtils.getLocalBoolean(this.m_binder, "isMasterServer", false))
/*     */     {
/* 379 */       String isProvisionalString = sysEditor.searchForValue("IsProvisionalServer");
/* 380 */       boolean isProvisional = StringUtils.convertToBool(isProvisionalString, false);
/* 381 */       if (isProvisional)
/*     */       {
/* 383 */         String idcName = this.m_binder.getLocal("IDC_Name");
/* 384 */         if (!StringUtils.urlEncode(idcName).equals(idcName))
/*     */         {
/* 386 */           this.m_service.createServiceException(IdcMessageFactory.lc("csIdcNameIllegal", new Object[] { idcName }));
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 391 */         String oldIdcName = sysEditor.searchForValue("IDC_Name");
/* 392 */         String newIdcName = this.m_binder.getLocal("IDC_Name");
/* 393 */         if (!oldIdcName.equals(newIdcName))
/*     */         {
/* 395 */           IdcMessage msg = IdcMessageFactory.lc("csPostInstallIDCNameMismatch", new Object[] { oldIdcName, newIdcName });
/* 396 */           this.m_service.createServiceException(msg);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 402 */     DataResultSet configOptions = (DataResultSet)this.m_binder.getResultSet("PostInstallConfigOptions");
/* 403 */     validateAndMergeProps(sysEditor, configOptions);
/* 404 */     sysEditor.saveIdc();
/*     */ 
/* 407 */     sysEditor.getCfgProperties().remove("IsProvisionalServer");
/* 408 */     sysEditor.saveConfig();
/*     */ 
/* 411 */     ResourceUtils.serializeDataBinder(installConfDir, installConfFile, this.m_binder, true, false);
/*     */   }
/*     */ 
/*     */   public void validateAndMergeProps(SystemPropertiesEditor sysEditor, DataResultSet configOptions)
/*     */     throws DataException, ServiceException
/*     */   {
/* 426 */     Properties newIdcProps = new IdcProperties();
/* 427 */     Properties newCfgProps = new IdcProperties();
/*     */ 
/* 429 */     boolean isMasterServer = DataBinderUtils.getLocalBoolean(this.m_binder, "isMasterServer", false);
/*     */ 
/* 431 */     Properties targetProps = null;
/* 432 */     for (configOptions.first(); configOptions.isRowPresent(); configOptions.next())
/*     */     {
/* 434 */       Properties rowProps = configOptions.getCurrentRowProps();
/* 435 */       String isDisabledString = rowProps.getProperty("isDisabled");
/* 436 */       boolean isDisabled = StringUtils.convertToBool(isDisabledString, false);
/* 437 */       if (isDisabled) {
/*     */         continue;
/*     */       }
/*     */ 
/* 441 */       String fieldName = rowProps.getProperty("fieldName");
/* 442 */       String descKey = rowProps.getProperty("labelDescriptionKey");
/* 443 */       String flags = rowProps.getProperty("flags");
/*     */ 
/* 445 */       String fieldVal = this.m_binder.getLocal(fieldName);
/* 446 */       if (fieldVal == null)
/*     */       {
/* 448 */         fieldVal = "";
/*     */       }
/*     */ 
/* 452 */       if (fieldName.equals("SearchIndexerEngineName"))
/*     */       {
/* 454 */         fieldVal = configureFullTextSearch(fieldVal, newIdcProps, newCfgProps);
/*     */       }
/* 456 */       if (fieldName.equals("ExternalDataSource"))
/*     */       {
/* 458 */         fieldVal = null;
/*     */       }
/*     */ 
/* 461 */       if ((flags.contains("isMaster")) && (!isMasterServer)) continue; if ((flags.contains("isCluster")) && (isMasterServer))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 467 */       if (flags.contains("booleanEnv"))
/*     */       {
/* 469 */         boolean defVal = flags.contains("defaultTrue");
/* 470 */         fieldVal = (StringUtils.convertToBool(fieldVal, defVal)) ? "Yes" : "No";
/*     */       }
/*     */ 
/* 473 */       if ((flags.contains("isRequired")) && (((fieldVal == null) || (fieldVal.length() == 0))))
/*     */       {
/* 475 */         this.m_service.createServiceException(IdcMessageFactory.lc("csRequiredFieldMissing", new Object[] { descKey + "Label" }));
/*     */       }
/*     */ 
/* 479 */       if (fieldVal == null) continue; if (fieldVal.length() == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 484 */       targetProps = (flags.contains("isIdcField")) ? newIdcProps : newCfgProps;
/* 485 */       targetProps.put(fieldName, fieldVal);
/*     */     }
/*     */ 
/* 489 */     sysEditor.mergePropertyValuesEx(newIdcProps, newCfgProps, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void finishPostInstall()
/*     */     throws DataException, ServiceException
/*     */   {
/* 497 */     ServerInstallUtils installUtils = new ServerInstallUtils(this.m_service);
/* 498 */     Map args = new HashMap();
/* 499 */     String binDir = SystemUtils.getBinDir();
/* 500 */     args.put("IntradocDir", FileUtils.directorySlashes(FileUtils.getParent(binDir)));
/* 501 */     args.put("NoCfgActivity", "1");
/* 502 */     String IDCName = this.m_binder.getLocal("IDC_Name");
/* 503 */     if (IDCName != null)
/*     */     {
/* 505 */       args.put("IDC_Name", IDCName);
/*     */     }
/* 507 */     installUtils.constructInstallerWithArgs(args);
/* 508 */     installUtils.m_installerConfig.put("InstallConfiguration", "PublishStartupEnvironment");
/* 509 */     installUtils.doInstall();
/*     */ 
/* 511 */     SharedObjects.putEnvironmentValue("PostInstallComplete", "1");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setIntradocDirForInstall() throws DataException, ServiceException
/*     */   {
/* 517 */     String newIntradocDirVariableName = this.m_currentAction.getParamAt(0);
/* 518 */     String newIntradocDir = this.m_binder.getLocal(newIntradocDirVariableName);
/* 519 */     if (newIntradocDir == null)
/*     */     {
/* 521 */       IdcMessage msg = IdcMessageFactory.lc("syParameterNotFound", new Object[] { newIntradocDirVariableName });
/* 522 */       this.m_service.createServiceException(msg);
/*     */     }
/* 524 */     DirectoryLocator.m_intradocDir = newIntradocDir;
/* 525 */     SharedObjects.putEnvironmentValue("IntradocDir", newIntradocDir);
/* 526 */     IdcSystemConfig.initConfigEarly(IdcSystemConfig.F_REINIT | IdcSystemConfig.F_STANDARD_SERVER);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setPublishEverythingAtStartup()
/*     */     throws DataException, ServiceException
/*     */   {
/* 533 */     WebPublishUtils.setPublishEverythingAtStartup(this.m_service);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadPostInstallData()
/*     */     throws ServiceException
/*     */   {
/* 540 */     this.m_service.evaluateResourceIncludeForServiceAction("load_post_install_config_options", "WlsServiceHandler.loadPostInstallData");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadMigrateState()
/*     */     throws ServiceException
/*     */   {
/* 547 */     String type = SharedObjects.getEnvironmentValue("SystemConfigPageType");
/* 548 */     String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 549 */     this.m_binder.putLocal("IntradocDir", intradocDir);
/* 550 */     if (MigrateUtils.loadMigrateState(intradocDir, type, this.m_binder))
/*     */       return;
/* 552 */     String path = this.m_binder.getLocal("path");
/* 553 */     IdcMessage msg = IdcMessageFactory.lc("csResourceUtilsNoFile", new Object[] { path });
/* 554 */     throw new ServiceException(null, -16, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadMigrateLogs()
/*     */     throws DataException, ServiceException
/*     */   {
/* 561 */     String type = SharedObjects.getEnvironmentValue("SystemConfigPageType");
/* 562 */     String intradocDir = this.m_binder.getLocal("IntradocDir");
/* 563 */     String componentsDir = intradocDir + type + "/components/";
/* 564 */     IdcStringBuilder str = new IdcStringBuilder();
/* 565 */     DataResultSet components = (DataResultSet)this.m_binder.getResultSet("Components");
/* 566 */     String[] fieldNames = { "componentName", "reason" };
/* 567 */     FieldInfo[] fields = ResultSetUtils.createInfoList(components, fieldNames, true);
/* 568 */     for (components.first(); components.isRowPresent(); components.next())
/*     */     {
/* 570 */       String result = components.getStringValue(fields[1].m_index);
/* 571 */       if (!result.startsWith("file:")) {
/*     */         continue;
/*     */       }
/*     */ 
/* 575 */       String logFilename = result.substring(5);
/* 576 */       String componentName = components.getStringValue(fields[0].m_index);
/* 577 */       String variableName = componentName + "_log";
/* 578 */       FileReader reader = null;
/*     */       try
/*     */       {
/* 581 */         reader = new FileReader(componentsDir + logFilename);
/* 582 */         char[] chars = new char[256];
/* 583 */         int numChars = 0;
/* 584 */         while ((numChars = reader.read(chars)) > 0)
/*     */         {
/* 586 */           str.append(chars, 0, numChars);
/*     */         }
/* 588 */         reader.close();
/* 589 */         this.m_binder.putLocal(variableName, str.toStringNoRelease());
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 593 */         Report.trace("system", "unable to read " + logFilename, ioe);
/*     */       }
/*     */       finally
/*     */       {
/* 597 */         FileUtils.closeObject(reader);
/*     */       }
/*     */     }
/* 600 */     str.releaseBuffers();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void doPostUpgradeConfig() throws DataException, ServiceException
/*     */   {
/* 606 */     String type = SharedObjects.getEnvironmentValue("SystemConfigPageType");
/* 607 */     String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 608 */     String path = intradocDir + type + "/force_system_config_page.dat";
/* 609 */     int check = FileUtils.checkFile(path, 1);
/* 610 */     if (check != 0)
/*     */     {
/* 612 */       String typeKey = "csMigrateType_" + type;
/* 613 */       String msg = LocaleUtils.encodeMessage("csPostMigrateConfigAlreadyDoneRequiresRestart", null, typeKey);
/*     */ 
/* 615 */       this.m_service.createServiceExceptionEx(null, msg, check);
/*     */     }
/* 617 */     if (SystemUtils.m_verbose)
/*     */     {
/* 619 */       Report.trace("install", "found " + path, null);
/*     */     }
/*     */ 
/* 622 */     SystemPropertiesEditor sysEditor = new SystemPropertiesEditor();
/*     */     try
/*     */     {
/* 625 */       sysEditor.initIdc();
/* 626 */       sysEditor.initConfig();
/*     */     }
/*     */     catch (ServiceException se)
/*     */     {
/* 630 */       this.m_service.createServiceException(se, "!csUnableToReadCurrentConfigFiles");
/*     */     }
/*     */     try
/*     */     {
/* 634 */       DynamicData ddConfigOptions = this.m_service.getPageMerger().getDynamicDataResource("PostMigrateConfigOptions", null);
/*     */ 
/* 636 */       Table table = ddConfigOptions.m_mergedTable;
/* 637 */       DataResultSet configOptions = new DataResultSet();
/* 638 */       configOptions.init(table);
/* 639 */       validateAndMergeProps(sysEditor, configOptions);
/*     */     }
/*     */     catch (ParseSyntaxException pse)
/*     */     {
/* 643 */       throw new ServiceException(pse);
/*     */     }
/* 645 */     sysEditor.saveIdc();
/* 646 */     sysEditor.saveConfig();
/* 647 */     FileUtils.deleteFile(path);
/*     */ 
/* 650 */     ResourceUtils.serializeDataBinder(intradocDir + "install/", "installconf.hda", this.m_binder, true, false);
/*     */   }
/*     */ 
/*     */   public String configureFullTextSearch(String fieldVal, Properties idcProps, Properties cfgProps)
/*     */     throws DataException, ServiceException
/*     */   {
/* 656 */     if (fieldVal.equals("Internal"))
/*     */     {
/* 658 */       String version = this.m_workspace.getProperty("DatabaseVersion");
/* 659 */       if (WorkspaceUtils.isDatabaseType(this.m_workspace, DatabaseTypes.ORACLE))
/*     */       {
/* 661 */         if (version.compareTo("11.1.0.6") > 0)
/*     */         {
/* 663 */           fieldVal = "OracleTextSearch";
/*     */         }
/*     */         else
/*     */         {
/* 667 */           fieldVal = "DATABASE.FULLTEXT";
/*     */         }
/*     */       }
/* 669 */       else if (WorkspaceUtils.isDatabaseType(this.m_workspace, DatabaseTypes.MSSQL))
/*     */       {
/* 671 */         fieldVal = "DATABASE.FULLTEXT";
/*     */       }
/*     */       else
/*     */       {
/* 675 */         fieldVal = null;
/*     */       }
/*     */     }
/* 678 */     else if (fieldVal.equals("External"))
/*     */     {
/* 680 */       cfgProps.put("IndexerDatabaseProviderName", "ExternalSearchProvider");
/* 681 */       this.m_binder.putLocal("createExternalProviderForFullTextSearch", "1");
/*     */ 
/* 683 */       fieldVal = "OracleTextSearch";
/*     */     }
/* 685 */     else if (fieldVal.equals("None"))
/*     */     {
/* 687 */       fieldVal = null;
/*     */     }
/*     */ 
/* 690 */     return fieldVal;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void createExternalProviderForFullTextSearch() throws DataException, ServiceException
/*     */   {
/* 696 */     if (!StringUtils.convertToBool(this.m_binder.getLocal("createExternalProviderForFullTextSearch"), false))
/*     */     {
/* 698 */       return;
/*     */     }
/*     */ 
/* 701 */     String externaldsVal = this.m_binder.getLocal("ExternalDataSource");
/* 702 */     String pName = "ExternalSearchProvider";
/* 703 */     String pDesc = "External Database Provider";
/* 704 */     DataBinder defData = Providers.getProviderData(pName);
/* 705 */     if (defData != null)
/*     */       return;
/* 707 */     DataBinder provData = new DataBinder();
/* 708 */     provData.putLocal("pName", pName);
/* 709 */     provData.putLocal("pDescription", pDesc);
/* 710 */     provData.putLocal("pType", "database");
/* 711 */     provData.putLocal("ProviderType", "database");
/* 712 */     provData.putLocal("ProviderClass", "intradoc.jdbc.JdbcWorkspace");
/* 713 */     provData.putLocal("ProviderConnection", "intradoc.jdbc.JdbcConnection");
/* 714 */     provData.putLocal("DatabaseType", "ORACLE");
/* 715 */     provData.putLocal("UseDataSource", "1");
/* 716 */     provData.putLocal("DataSource", externaldsVal);
/* 717 */     provData.putLocal("TestQuery", "SELECT 1 FROM DUAL");
/* 718 */     provData.putLocal("NumConnections", "5");
/* 719 */     provData.putLocal("ExtraStorageKeys", "system");
/*     */ 
/* 721 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 722 */     String providerDir = DirectoryLocator.getProviderDirectory();
/* 723 */     FileUtils.checkOrCreateDirectory(providerDir, 2);
/* 724 */     ProviderFileUtils.init(providerDir);
/* 725 */     ProviderUtils.addOrEditProvider(false, null, provData, this.m_workspace, cxt);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 731 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95531 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.WlsServiceHandler
 * JD-Core Version:    0.5.4
 */