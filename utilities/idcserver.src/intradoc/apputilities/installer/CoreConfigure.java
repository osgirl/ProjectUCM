/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.admin.AdminDirectoryLocator;
/*      */ import intradoc.admin.AdminServerData;
/*      */ import intradoc.admin.AdminServerUtils;
/*      */ import intradoc.admin.AdminService;
/*      */ import intradoc.admin.IdcAdminSystemLoader;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportSubProgress;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.config.ConfigFileLoader;
/*      */ import intradoc.filestore.config.ConfigFileStore;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.filestore.config.ServerData;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.ActiveState;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcExtendedLoader;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.ProviderManagerService;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceData;
/*      */ import intradoc.server.UserService;
/*      */ import intradoc.server.UserServiceHandler;
/*      */ import intradoc.server.proxy.ProviderFileUtils;
/*      */ import intradoc.server.publish.WebPublishUtils;
/*      */ import intradoc.server.subject.UsersSubjectCallback;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.FilenameFilter;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Locale;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class CoreConfigure
/*      */   implements SectionInstaller, FilenameFilter
/*      */ {
/*      */   protected SysInstaller m_installer;
/*      */   protected SysInstaller m_masterInstaller;
/*      */   protected PromptUser m_prompter;
/*      */   protected int m_unattemptedWork;
/*      */   protected Vector m_warnings;
/*      */   protected ReportSubProgress m_progress;
/*      */   protected boolean m_isProxied;
/*      */   protected boolean m_isRefinery;
/*      */   protected String m_section;
/*      */ 
/*      */   public CoreConfigure()
/*      */   {
/*   97 */     this.m_unattemptedWork = 0;
/*      */   }
/*      */ 
/*      */   public int installSection(String sectionName, String disposition, String arg, SysInstaller installer, Properties config)
/*      */     throws ServiceException
/*      */   {
/*  108 */     this.m_section = sectionName;
/*  109 */     this.m_installer = installer;
/*  110 */     this.m_prompter = this.m_installer.m_promptUser;
/*      */ 
/*  113 */     String proxiedType = this.m_installer.getInstallValue("ConfigureProxiedServer", "no");
/*      */ 
/*  115 */     this.m_isProxied = ((((this.m_installer.getInstallBool("IsProxiedServer", false)) || (StringUtils.convertToBool(proxiedType, true)))) && (!this.m_installer.m_isUpdate));
/*      */ 
/*  117 */     this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*      */ 
/*  119 */     if (!this.m_installer.m_isUpdate)
/*      */     {
/*  121 */       this.m_installer.prepareForLocks("bin");
/*  122 */       this.m_installer.prepareForLocks("config");
/*  123 */       this.m_installer.prepareForLocks("data/users");
/*      */ 
/*  125 */       this.m_installer.prepareForLocks("weblayout/groups/secure/logs");
/*  126 */       this.m_installer.prepareForLocks("data/schema/publishlock");
/*  127 */       this.m_installer.prepareForLocks("data/subjects");
/*  128 */       if (this.m_installer.checkConfigureAdminServer(1))
/*      */       {
/*  130 */         this.m_installer.prepareForLocks("admin/bin");
/*  131 */         this.m_installer.prepareForLocks("admin/config");
/*  132 */         this.m_installer.prepareForLocks("admin/data/subjects");
/*      */       }
/*  134 */       if (!this.m_installer.isRefineryInstall())
/*      */       {
/*  136 */         this.m_installer.prepareForLocks("search");
/*  137 */         this.m_installer.prepareForLocks("search/lock");
/*  138 */         this.m_installer.prepareForLocks("data/search");
/*  139 */         this.m_installer.prepareForLocks("data/collections");
/*  140 */         this.m_installer.prepareForLocks("data/workflow");
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  146 */       if (this.m_isProxied)
/*      */       {
/*  148 */         this.m_masterInstaller = this.m_installer.deriveInstaller(this.m_installer.getInstallValue("MasterServerDir", null));
/*      */ 
/*  150 */         this.m_masterInstaller.loadConfig();
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  155 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  158 */     if (disposition.equals("always"))
/*      */     {
/*  160 */       if ((this.m_installer.isOlderVersion("4")) && (this.m_installer.getInstallBool("ConfigureAdminServer", true)))
/*      */       {
/*  164 */         this.m_installer.editConfigFile("admin/bin/intradoc.cfg", "IntradocDir", this.m_installer.m_idcDir);
/*      */ 
/*  167 */         this.m_installer.editConfigFile("admin/config/config.cfg", "IDC_Admin_Name", this.m_installer.getConfigValue("IDC_Name") + "_admin");
/*      */ 
/*  170 */         this.m_installer.editConfigFile("admin/config/config.cfg", "HttpRelativeWebRoot", this.m_installer.getConfigValue("HttpRelativeWebRoot"));
/*      */ 
/*  173 */         this.m_installer.editConfigFile("admin/config/config.cfg", "HttpServerAddress", this.m_installer.getConfigValue("HttpServerAddress"));
/*      */       }
/*      */ 
/*  178 */       if (!this.m_installer.m_isUpdate)
/*      */       {
/*  180 */         String relativeWebRoot = this.m_installer.getInstallValue("HttpRelativeWebRoot", "/idc/");
/*      */ 
/*  182 */         String relativeCgiRoot = this.m_installer.getInstallValue("HttpRelativeCgiRoot", null);
/*      */ 
/*  184 */         if ((relativeCgiRoot == null) || (relativeCgiRoot.equals("/")))
/*      */         {
/*  186 */           if (this.m_isProxied)
/*      */           {
/*  188 */             relativeCgiRoot = this.m_masterInstaller.getInstallValue("HttpRelativeCgiRoot", null);
/*      */ 
/*  190 */             if ((relativeCgiRoot == null) || (relativeCgiRoot.equals("/")))
/*      */             {
/*  192 */               relativeCgiRoot = this.m_masterInstaller.getInstallValue("HttpRelativeWebRoot", "/");
/*      */             }
/*      */ 
/*  195 */             String cgiFileName = this.m_masterInstaller.getInstallValue("CgiFileName", "idcplg");
/*      */ 
/*  197 */             this.m_installer.editConfigFile("config/config.cfg", "CgiFileName", cgiFileName + relativeWebRoot + "pxs");
/*      */           }
/*      */           else
/*      */           {
/*  202 */             relativeCgiRoot = null;
/*      */           }
/*      */         }
/*  205 */         if (relativeCgiRoot != null)
/*      */         {
/*  207 */           this.m_installer.editConfigFile("config/config.cfg", "HttpRelativeCgiRoot", relativeCgiRoot);
/*      */         }
/*      */ 
/*  211 */         if (this.m_isProxied)
/*      */         {
/*  213 */           this.m_installer.editConfigFile("config/config.cfg", "IsProxiedServer", "true");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  218 */       boolean skipDatabase = StringUtils.convertToBool(this.m_installer.m_installerConfig.getProperty("SkipDatabase"), false);
/*      */ 
/*  224 */       int totalMessages = 17;
/*  225 */       if (!this.m_isProxied)
/*      */       {
/*  227 */         totalMessages -= 5;
/*      */       }
/*  229 */       if (skipDatabase)
/*      */       {
/*  231 */         totalMessages -= 3;
/*      */       }
/*  233 */       this.m_progress = new ReportSubProgress(this.m_installer, 0, totalMessages);
/*      */ 
/*  237 */       this.m_warnings = new IdcVector();
/*  238 */       if ((!skipDatabase) && 
/*  240 */         (!this.m_isRefinery))
/*      */       {
/*  242 */         this.m_warnings.addElement("!csInstallerUserDBPublishError");
/*      */       }
/*      */ 
/*  245 */       if (this.m_isProxied)
/*      */       {
/*  247 */         this.m_warnings.addElement("!csInstallerProviderToMasterError");
/*  248 */         this.m_warnings.addElement("!csInstallerProviderFromMasterError");
/*      */       }
/*  250 */       String configureAdminServer = this.m_installer.getInstallValue("ConfigureAdminServer", "true");
/*      */ 
/*  252 */       if ((StringUtils.convertToBool(configureAdminServer, false)) || (configureAdminServer.equalsIgnoreCase("configure_existing")))
/*      */       {
/*  255 */         this.m_warnings.addElement("!csInstallerAdminServerRegError");
/*      */       }
/*      */ 
/*  258 */       boolean needsMenuLabelAndDescription = this.m_installer.isOlderVersion("4");
/*  259 */       if (needsMenuLabelAndDescription)
/*      */       {
/*  261 */         installer.editConfigFile("config/config.cfg", "InstanceMenuLabel", installer.getInstallValue("InstanceMenuLabel", null));
/*      */ 
/*  263 */         installer.editConfigFile("config/config.cfg", "InstanceDescription", installer.getInstallValue("InstanceDescription", null));
/*      */       }
/*      */ 
/*  267 */       if (!this.m_isProxied)
/*      */       {
/*  271 */         configureAllServers();
/*      */       }
/*      */       else
/*      */       {
/*  275 */         Exception exception = null;
/*      */         try
/*      */         {
/*  278 */           processServer(this.m_installer.m_idcDir);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  282 */           exception = e;
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  286 */           exception = e;
/*      */         }
/*  288 */         if (exception != null)
/*      */         {
/*  290 */           this.m_installer.m_installLog.error(exception.getMessage());
/*      */         }
/*      */       }
/*      */ 
/*  294 */       Workspace ws = null;
/*      */       try
/*      */       {
/*  300 */         if ((!this.m_isRefinery) && (this.m_isProxied))
/*      */         {
/*      */           try
/*      */           {
/*  305 */             reportProgress("!csInstallerProgressConfigSharedWebRoot");
/*      */ 
/*  309 */             String[][] names = { { "HttpSharedRoot", "config/config.cfg" }, { "HttpImagesRoot", "config/config.cfg" }, { "HttpHelpRoot", "config/config.cfg" }, { "HttpCommonRoot", "config/config.cfg" }, { "JAVA_EXE", "bin/intradoc.cfg" }, { "UseMicrosoftVM", "bin/intradoc.cfg" } };
/*      */ 
/*  318 */             String[] defaults = new String[names.length];
/*  319 */             String[] values = new String[names.length];
/*      */ 
/*  321 */             for (int i = 0; i < names.length; ++i)
/*      */             {
/*  323 */               values[i] = this.m_masterInstaller.getConfigValue(names[i][0]);
/*      */             }
/*      */ 
/*  328 */             defaults[0] = this.m_masterInstaller.getConfigValue("HttpRelativeWebRoot");
/*  329 */             String actualSharedRoot = (values[0] == null) ? defaults[0] : values[0];
/*  330 */             defaults[1] = (actualSharedRoot + "images/");
/*  331 */             defaults[2] = (actualSharedRoot + "help/");
/*  332 */             defaults[3] = (actualSharedRoot + "common/");
/*  333 */             defaults[4] = null;
/*  334 */             defaults[5] = null;
/*      */ 
/*  336 */             installer.editConfigFile(names[0][1], names[0][0], actualSharedRoot);
/*      */ 
/*  339 */             for (int i = 1; i < values.length; ++i)
/*      */             {
/*  341 */               if ((values[i] == null) || ((defaults[i] != null) && (values[i].equals(defaults[i])))) {
/*      */                 continue;
/*      */               }
/*      */ 
/*  345 */               installer.editConfigFile(names[i][1], names[i][0], values[i]);
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  352 */             failure(t);
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  357 */             this.m_unattemptedWork = 1;
/*      */ 
/*  359 */             if (this.m_installer.getInstallBool("InstallWithSharedWeblayout", false))
/*      */             {
/*  361 */               reportProgress("!csInstallerProgressConfigSharedWeblayoutDir");
/*  362 */               String sharedWebDir = this.m_masterInstaller.getConfigValue("SharedWeblayoutDir");
/*  363 */               if (sharedWebDir == null)
/*      */               {
/*  365 */                 sharedWebDir = this.m_masterInstaller.getConfigValue("WeblayoutDir");
/*      */               }
/*  367 */               if (sharedWebDir == null)
/*      */               {
/*  369 */                 sharedWebDir = this.m_installer.getInstallValue("MasterServerDir", null);
/*  370 */                 sharedWebDir = FileUtils.directorySlashes(sharedWebDir);
/*  371 */                 sharedWebDir = sharedWebDir + "weblayout/";
/*      */               }
/*      */ 
/*  374 */               installer.editConfigFile("bin/intradoc.cfg", "SharedWeblayoutDir", sharedWebDir);
/*      */             }
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  379 */             failure(t);
/*      */           }
/*      */         }
/*      */ 
/*  383 */         reportProgress("!csInstallerProgressConfigServer");
/*  384 */         IdcSystemLoader.m_progress = this.m_progress;
/*      */ 
/*  386 */         installer.initServerConfig(installer.m_idcDir, true);
/*  387 */         ComponentListManager.init();
/*  388 */         IdcSystemLoader.loadComponentData();
/*  389 */         if (!skipDatabase)
/*      */         {
/*  391 */           IdcSystemLoader.initProviders(0);
/*      */         }
/*      */ 
/*  394 */         String serverLocale = installer.m_intradocConfig.getProperty("SystemLocale");
/*  395 */         if (serverLocale == null)
/*      */         {
/*  397 */           Locale locale = Locale.getDefault();
/*  398 */           DataResultSet drset = SharedObjects.getTable("LanguageLocaleMap");
/*  399 */           if ((drset != null) && (locale != null))
/*      */           {
/*  401 */             serverLocale = ResourceLoader.computeLocale(locale, drset);
/*      */           }
/*      */         }
/*  404 */         if (serverLocale == null)
/*      */         {
/*  406 */           IdcLocale locale = LocaleResources.getLocale("SystemLocale");
/*  407 */           serverLocale = locale.m_name;
/*      */         }
/*  409 */         if (serverLocale == null)
/*      */         {
/*  411 */           serverLocale = "English-US";
/*      */         }
/*  413 */         SharedObjects.putEnvironmentValue("SystemLocale", serverLocale);
/*  414 */         SharedObjects.putEnvironmentValue("isUseComponentDBInstallToCreateTable", "false");
/*      */ 
/*  416 */         IdcExtendedLoader extendedLoader = null;
/*  417 */         if (!this.m_isRefinery)
/*      */         {
/*  420 */           extendedLoader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader", "intradoc.server.IdcExtendedLoader", "!csInstallerCustomInitLoaderError");
/*      */ 
/*  423 */           IdcSystemLoader.setExtendedLoader(extendedLoader);
/*      */         }
/*  425 */         ActiveState.load();
/*      */ 
/*  427 */         if (!skipDatabase)
/*      */         {
/*      */           try
/*      */           {
/*  431 */             this.m_unattemptedWork = 2;
/*  432 */             reportProgress("!csInstallerProgressConfigServer");
/*  433 */             ws = IdcSystemLoader.loadDatabase(1);
/*  434 */             IdcSystemLoader.loadSystemUserDatabase(1);
/*      */ 
/*  436 */             reportProgress("!csInstallerProgressConfigUser");
/*  437 */             extendedLoader.extraBeforeCacheLoadInit(ws);
/*      */ 
/*  442 */             UsersSubjectCallback usersCallback = new UsersSubjectCallback();
/*  443 */             usersCallback.setWorkspace(ws);
/*  444 */             usersCallback.cacheUsers();
/*  445 */             this.m_installer.m_installerConfig.put("PublishedUsers", "true");
/*      */ 
/*  447 */             if ((!this.m_installer.m_isUpdate) && (this.m_installer.getInstallBool("ConfigureAdminUser", false)))
/*      */             {
/*  451 */               DataBinder binder = new DataBinder();
/*  452 */               String userName = this.m_installer.getInstallValue("AdminUserName", "sysadmin");
/*  453 */               String password = this.m_installer.getInstallValue("AdminUserPassword", null);
/*  454 */               String enc = this.m_installer.getInstallValue("AdminUserPasswordEncoding", "SHA1-CB");
/*  455 */               binder.putLocal("dName", userName);
/*  456 */               if ((password != null) && (password.length() > 0) && (!this.m_installer.getInstallBool("AdminUserPasswordIsEncoded", false)))
/*      */               {
/*  459 */                 password = UserUtils.encodePassword(userName, password, enc);
/*  460 */                 binder.putLocal("dPassword", password);
/*      */               }
/*  462 */               binder.putLocal("dPasswordEncoding", enc);
/*  463 */               binder.putLocal("dUserAuthType", "LOCAL");
/*  464 */               binder.putLocal("isAdd", "1");
/*      */ 
/*  466 */               DataResultSet attributes = new DataResultSet(new String[] { "dUserName", "AttributeInfo" });
/*      */ 
/*  469 */               Vector row = new IdcVector();
/*  470 */               row.addElement(userName);
/*  471 */               row.addElement("role,sysmanager,15,role,admin,15");
/*  472 */               attributes.addRow(row);
/*  473 */               binder.addResultSet("UserAttribInfo", attributes);
/*      */ 
/*  475 */               UserService service = new UserService();
/*  476 */               service.init(ws, null, binder, new ServiceData());
/*  477 */               service.initDelegatedObjects();
/*  478 */               service.storeUserDatabaseProfileData();
/*      */ 
/*  480 */               ArrayList wsparams = new ArrayList();
/*  481 */               wsparams.add("workspace");
/*  482 */               wsparams.add("user");
/*      */ 
/*  484 */               Action action = new Action();
/*  485 */               service.setCurrentAction(action);
/*  486 */               action.m_function = "DuserSecurityAttributes";
/*  487 */               action.m_type = 2;
/*  488 */               action.m_params = new IdcVector(wsparams);
/*  489 */               service.doAction();
/*      */ 
/*  491 */               action = new Action();
/*  492 */               service.setCurrentAction(action);
/*  493 */               action.m_params = new IdcVector();
/*  494 */               action.m_params.addElement("IuserSecurityAttribute");
/*  495 */               UserData userData = UserUtils.createUserData("@install");
/*  496 */               userData.setAttributes(new String[][] { { "role", "admin", "15" } });
/*      */ 
/*  498 */               service.setUserData(userData);
/*  499 */               service.addUserAttributes();
/*      */ 
/*  502 */               this.m_installer.m_installerConfig.put("PublishedUsers", "false");
/*  503 */               usersCallback.cacheUsers();
/*  504 */               this.m_installer.m_installerConfig.put("PublishedUsers", "true");
/*      */             }
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  509 */             failure(t);
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  514 */             this.m_unattemptedWork = 1;
/*  515 */             if (!this.m_isRefinery)
/*      */             {
/*  517 */               if (ws == null)
/*      */               {
/*  519 */                 throw new ServiceException("!csInstallerWorkspaceNotInit");
/*      */               }
/*  521 */               Service service = new Service();
/*  522 */               service.init(ws, null, new DataBinder(), new ServiceData());
/*  523 */               UserServiceHandler handler = new UserServiceHandler(ws);
/*  524 */               handler.init(service);
/*  525 */               handler.updateCache();
/*  526 */               reportProgress("!csInstallerProgressConfigUser");
/*      */             }
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  531 */             failure(t);
/*      */           }
/*  533 */           this.m_warnings.removeElementAt(0);
/*      */         }
/*      */ 
/*  537 */         if (this.m_isProxied)
/*      */         {
/*      */           try
/*      */           {
/*  541 */             this.m_unattemptedWork = 2;
/*      */ 
/*  543 */             reportProgress("!csInstallerProgressProviderConfig");
/*  544 */             String providerDir = DirectoryLocator.getProviderDirectory();
/*  545 */             FileUtils.checkOrCreateDirectory(providerDir, 1);
/*  546 */             ProviderFileUtils.init(providerDir);
/*      */ 
/*  548 */             String masterIdcName = this.m_masterInstaller.getConfigValue("IDC_Name");
/*  549 */             String masterIdcPort = this.m_masterInstaller.getConfigValue("IntradocServerPort");
/*  550 */             if (masterIdcPort == null)
/*      */             {
/*  552 */               masterIdcPort = "4444";
/*      */             }
/*      */ 
/*  555 */             DataBinder binder = new DataBinder();
/*      */ 
/*  557 */             binder.putLocal("pName", masterIdcName);
/*  558 */             binder.putLocal("pType", "outgoing");
/*  559 */             binder.putLocal("IDC_Name", masterIdcName);
/*  560 */             binder.putLocal("pDescription", LocaleResources.getString("csInstallerMasterProviderDesc", null));
/*      */ 
/*  562 */             binder.putLocal("ProviderClass", "intradoc.provider.SocketOutgoingProvider");
/*  563 */             binder.putLocal("IntradocServerPort", masterIdcPort);
/*  564 */             binder.putLocal("IntradocServerHostName", "localhost");
/*  565 */             binder.putLocal("HttpRelativeWebRoot", this.m_masterInstaller.getConfigValue("HttpRelativeWebRoot"));
/*      */ 
/*  567 */             if (!this.m_isRefinery)
/*      */             {
/*  569 */               binder.putLocal("IsNotifyTarget", "true");
/*  570 */               binder.putLocal("NotifySubjects", "users");
/*      */             }
/*      */ 
/*  573 */             ProviderManagerService service = new ProviderManagerService();
/*  574 */             service.init(null, System.err, binder, new ServiceData());
/*  575 */             service.initDelegatedObjects();
/*  576 */             boolean exists = false;
/*      */             try
/*      */             {
/*  579 */               service.addOrEditProvider();
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/*  583 */               if (e.m_errorCode == -17)
/*      */               {
/*  585 */                 exists = true;
/*      */               }
/*      */               else
/*      */               {
/*  589 */                 throw e;
/*      */               }
/*      */             }
/*  592 */             reportProgress("!csInstallerProgressProviderConfig");
/*  593 */             if (exists)
/*      */             {
/*  595 */               String msg = LocaleUtils.encodeMessage("csInstallerProviderCreationError", null, masterIdcName);
/*      */ 
/*  598 */               this.m_installer.m_installLog.warning(msg);
/*  599 */               reportProgress(msg);
/*      */             }
/*      */             else
/*      */             {
/*  603 */               reportProgress(LocaleUtils.encodeMessage("csInstallerProviderConfigToMaster", null, masterIdcName));
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  610 */             failure(t);
/*      */           }
/*  612 */           this.m_warnings.removeElementAt(0);
/*      */           try
/*      */           {
/*  616 */             this.m_unattemptedWork = 2;
/*      */ 
/*  618 */             this.m_installer.initServerConfigEx(this.m_installer.getInstallValue("MasterServerDir", null), true, true);
/*      */ 
/*  620 */             reportProgress("!csInstallerProgressProviderConfig");
/*  621 */             String idcName = this.m_installer.getConfigValue("IDC_Name");
/*  622 */             ProviderFileUtils.init(DirectoryLocator.getProviderDirectory());
/*      */ 
/*  624 */             DataBinder binder = new DataBinder();
/*  625 */             binder.putLocal("pName", idcName);
/*  626 */             binder.putLocal("IDC_Name", idcName);
/*  627 */             String pType = "outgoing";
/*  628 */             String pDescription = LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerProxyProviderDesc", null, idcName), null);
/*      */ 
/*  630 */             if (this.m_isRefinery)
/*      */             {
/*  632 */               binder.putLocal("IsRefinery", "1");
/*  633 */               binder.putLocal("RefineryMaxJobs", "1000");
/*  634 */               pDescription = LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerRefineryProxyProviderDesc", null, idcName), null);
/*      */             }
/*      */ 
/*  637 */             binder.putLocal("pType", pType);
/*  638 */             binder.putLocal("pDescription", pDescription);
/*  639 */             binder.putLocal("ProviderClass", "intradoc.provider.SocketOutgoingProvider");
/*  640 */             binder.putLocal("IntradocServerPort", this.m_installer.getInstallValue("IntradocServerPort", "4444"));
/*  641 */             binder.putLocal("IntradocServerHostName", "localhost");
/*  642 */             binder.putLocal("HttpRelativeWebRoot", this.m_installer.getInstallValue("HttpRelativeWebRoot", "/xcs/"));
/*  643 */             binder.putLocal("IsProxiedServer", "true");
/*      */ 
/*  645 */             ProviderManagerService service = new ProviderManagerService();
/*  646 */             service.init(null, System.err, binder, new ServiceData());
/*  647 */             service.initDelegatedObjects();
/*  648 */             boolean exists = false;
/*      */             try
/*      */             {
/*  651 */               service.addOrEditProvider();
/*      */             }
/*      */             catch (ServiceException e)
/*      */             {
/*  655 */               if (e.m_errorCode == -17)
/*      */               {
/*  657 */                 exists = true;
/*      */               }
/*      */               else
/*      */               {
/*  661 */                 throw e;
/*      */               }
/*      */             }
/*  664 */             reportProgress("!csInstallerProgressProviderConfig");
/*  665 */             if (exists)
/*      */             {
/*  667 */               this.m_installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerProviderInServerError", null, idcName));
/*      */             }
/*      */             else
/*      */             {
/*  671 */               reportProgress(LocaleUtils.encodeMessage("csInstallerProviderConfigInMaster", null, SharedObjects.getEnvironmentValue("IDC_Name")));
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/*  679 */             failure(t);
/*      */           }
/*  681 */           this.m_warnings.removeElementAt(0);
/*      */         }
/*      */ 
/*  684 */         if ((StringUtils.convertToBool(configureAdminServer, false)) || (configureAdminServer.equalsIgnoreCase("configure_existing")))
/*      */         {
/*  688 */           String adminDir = null;
/*  689 */           if (StringUtils.convertToBool(configureAdminServer, false))
/*      */           {
/*  691 */             adminDir = this.m_installer.m_idcDir;
/*      */           }
/*  693 */           adminDir = this.m_installer.getInstallValue("AdminServerDir", this.m_installer.getInstallValue("MasterServerDir", adminDir) + "/admin");
/*      */ 
/*  697 */           registerWithAdminServer(adminDir);
/*      */         }
/*  699 */         reportProgress("!csInstallerProgressConfigDone");
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  706 */         IdcMessage msg = IdcMessageFactory.lc(t, "csUnexpectedException", new Object[] { t.getClass().getName() });
/*  707 */         installer.m_installLog.warning(LocaleUtils.encodeMessage(msg));
/*  708 */         Report.trace("install", null, t);
/*      */       }
/*      */ 
/*  711 */       for (int i = 0; i < this.m_warnings.size(); ++i)
/*      */       {
/*  713 */         installer.m_installLog.warning((String)this.m_warnings.elementAt(i));
/*      */       }
/*      */ 
/*  716 */       reportProgress("!csInstallerProgressConfigDone");
/*      */     }
/*  718 */     return 0;
/*      */   }
/*      */ 
/*      */   public void writeVersionInfo(SysInstaller installer)
/*      */     throws ServiceException
/*      */   {
/*  725 */     OutputStream out = null;
/*      */     try
/*      */     {
/*  728 */       String versionPath = installer.computeDestination("install/version.txt");
/*  729 */       out = new FileOutputStream(versionPath);
/*  730 */       String verInstalled = this.m_installer.getInstallValue("ProductVersionToBeInstalled", "unknown");
/*  731 */       out.write((verInstalled + "\n").getBytes());
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  739 */       FileUtils.closeObject(out);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void reportProgress(String msg)
/*      */   {
/*  745 */     this.m_progress.m_curProgress += 1;
/*  746 */     this.m_installer.reportProgress(1, msg, this.m_progress.m_curProgress, this.m_progress.m_maxProgress);
/*      */ 
/*  748 */     if (SystemUtils.m_verbose)
/*      */     {
/*  750 */       Exception e = new StackTrace("reporting progress of " + this.m_progress.m_curProgress + " out of " + this.m_progress.m_maxProgress);
/*      */ 
/*  753 */       Report.debug("install", null, e);
/*      */     }
/*  755 */     if (this.m_unattemptedWork <= 0)
/*      */       return;
/*  757 */     this.m_unattemptedWork -= 1;
/*      */   }
/*      */ 
/*      */   public void failure(Throwable t)
/*      */     throws ServiceException
/*      */   {
/*  763 */     String message = t.getMessage();
/*  764 */     String className = t.getClass().getName();
/*      */ 
/*  766 */     Report.trace("install", null, t);
/*  767 */     if (this.m_warnings.size() > 0)
/*      */     {
/*  769 */       String logMessage = LocaleUtils.appendMessage(className, (String)this.m_warnings.elementAt(0));
/*      */ 
/*  771 */       logMessage = LocaleUtils.appendMessage(message, logMessage);
/*  772 */       this.m_installer.m_installLog.warning(logMessage);
/*      */     }
/*      */     else
/*      */     {
/*  776 */       this.m_installer.m_installLog.warning(t.toString());
/*      */     }
/*  778 */     this.m_progress.m_curProgress += this.m_unattemptedWork;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String[] tokenizeVersion(String text)
/*      */   {
/*  785 */     return SysInstaller.tokenizeVersion(text);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int compareVersions(String[] lop, String[] rop)
/*      */   {
/*  792 */     return SysInstaller.compareVersions(lop, rop);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int compareVersions(String lop, String rop)
/*      */   {
/*  799 */     return SysInstaller.compareVersions(lop, rop);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String cleanVersion(String version)
/*      */   {
/*  806 */     return SysInstaller.cleanVersion(version);
/*      */   }
/*      */ 
/*      */   public void configureAllServers() throws ServiceException
/*      */   {
/*  811 */     String idcDir = this.m_installer.m_idcDir;
/*  812 */     DataBinder binder = new DataBinder();
/*      */ 
/*  819 */     String serverFileDir = idcDir + "/admin/data/servers";
/*  820 */     String serverFileName = "servers.hda";
/*  821 */     String rsetName = "ServerDefinition";
/*  822 */     String connectionNameField = "IDC_Id";
/*  823 */     String connectionNameBackupField = "IDC_Name";
/*  824 */     String connectionPathField = "IDC_Id";
/*  825 */     String connectionPathBackupField = "IDC_Name";
/*  826 */     String connectionPathPrefix = idcDir + "/admin/data/servers/";
/*  827 */     String connectionPathFile = "server.hda";
/*  828 */     String configFile = "config/config.cfg";
/*      */ 
/*  830 */     Report.trace("install", "getting ready to configure all servers", null);
/*  831 */     ResourceUtils.serializeDataBinder(serverFileDir, serverFileName, binder, false, false);
/*      */ 
/*  833 */     DataResultSet drset = (DataResultSet)binder.getResultSet(rsetName);
/*  834 */     if (drset != null)
/*      */     {
/*  836 */       Report.trace("install", "finding servers from the admin server.", null);
/*  837 */       FieldInfo serverName = new FieldInfo();
/*  838 */       FieldInfo serverPathComponent = new FieldInfo();
/*  839 */       if ((!drset.getFieldInfo(connectionNameField, serverName)) && (!drset.getFieldInfo(connectionNameBackupField, serverName)))
/*      */       {
/*  842 */         throw new ServiceException(LocaleUtils.encodeMessage("csInstallerQryInvalidField", null, connectionNameField, serverFileName));
/*      */       }
/*      */ 
/*  845 */       if ((!drset.getFieldInfo(connectionPathField, serverPathComponent)) && (!drset.getFieldInfo(connectionPathBackupField, serverPathComponent)))
/*      */       {
/*  848 */         throw new ServiceException(LocaleUtils.encodeMessage("csInstallerQryMissingField", null, connectionPathField, serverFileName));
/*      */       }
/*      */ 
/*  852 */       Hashtable processedDirectories = new Hashtable();
/*  853 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  855 */         String connectionName = drset.getStringValue(serverName.m_index);
/*  856 */         String connectionPath = drset.getStringValue(serverPathComponent.m_index);
/*      */ 
/*  859 */         DataBinder serverData = new DataBinder();
/*  860 */         Report.trace("install", "loading configuration data from '" + connectionPathPrefix + connectionPath + "'", null);
/*      */ 
/*  862 */         ResourceUtils.serializeDataBinder(connectionPathPrefix + connectionPath, connectionPathFile, serverData, false, true);
/*      */ 
/*  865 */         String idc = serverData.getAllowMissing("IntradocDir");
/*      */ 
/*  867 */         if (idc == null)
/*      */         {
/*  869 */           this.m_prompter.outputMessage(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerQryIntradocDirCfgMissing", null, connectionName, connectionPathFile), null));
/*      */         }
/*  874 */         else if (processedDirectories.get(idc) != null)
/*      */         {
/*  876 */           Report.trace("install", "skipping directory '" + idc + "'", null);
/*      */         }
/*      */         else
/*      */         {
/*  880 */           processedDirectories.put(idc, idc);
/*  881 */           Exception exception = null;
/*      */           try
/*      */           {
/*  884 */             Report.trace("install", "processing a server at " + idc, null);
/*  885 */             processServer(idc);
/*      */           }
/*      */           catch (ServiceException e)
/*      */           {
/*  889 */             exception = e;
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  893 */             exception = e;
/*      */           }
/*  895 */           if (exception == null)
/*      */             continue;
/*  897 */           this.m_installer.m_installLog.error(exception.getMessage());
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  903 */       Report.trace("install", "only configuring this server", null);
/*  904 */       String cfgPath = this.m_installer.computeDestination(configFile);
/*  905 */       if (FileUtils.checkFile(cfgPath, true, false) != 0)
/*      */         return;
/*  907 */       String idc = this.m_installer.m_idcDir;
/*      */ 
/*  909 */       Exception exception = null;
/*      */       try
/*      */       {
/*  912 */         processServer(idc);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  916 */         exception = e;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  920 */         exception = e;
/*      */       }
/*  922 */       if (exception == null)
/*      */         return;
/*  924 */       this.m_installer.m_installLog.error(exception.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void processServer(String idcDir)
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     SysInstaller installer;
/*      */     SysInstaller installer;
/*  934 */     if (idcDir.equals(this.m_installer.m_idcDir))
/*      */     {
/*  936 */       installer = this.m_installer;
/*      */     }
/*      */     else
/*      */     {
/*  940 */       installer = this.m_installer.deriveInstaller(idcDir);
/*  941 */       installer.m_installerConfig.put("IsProxiedServer", "1");
/*  942 */       installer.m_installerConfig.put("MasterServerDir", this.m_installer.m_idcDir);
/*      */     }
/*      */ 
/*  945 */     installer.loadConfig();
/*      */ 
/*  951 */     String masterSharedDir = this.m_installer.getSharedDir();
/*  952 */     String targetSharedDir = installer.getSharedDir();
/*  953 */     boolean isSharedProxy = false;
/*  954 */     if (masterSharedDir == null)
/*      */     {
/*  959 */       isSharedProxy = targetSharedDir != null;
/*      */     }
/*  963 */     else if (EnvUtils.getOSFamily().equals("windows"))
/*      */     {
/*  965 */       isSharedProxy = masterSharedDir.equalsIgnoreCase(targetSharedDir);
/*      */     }
/*      */     else
/*      */     {
/*  970 */       isSharedProxy = masterSharedDir.equals(targetSharedDir);
/*      */     }
/*      */ 
/*  973 */     if (!isSharedProxy)
/*      */     {
/*  975 */       return;
/*      */     }
/*      */ 
/*  978 */     boolean needsJvmUpdating = this.m_installer.isOlderVersion("6.1.0");
/*  979 */     if (needsJvmUpdating)
/*      */     {
/*  981 */       String adminCfgPath = installer.computeDestinationEx("admin/bin/intradoc.cfg", false);
/*  982 */       boolean adminCfg = FileUtils.checkFile(adminCfgPath, true, true) == 0;
/*  983 */       String jvmChoice = this.m_installer.getInstallValue("InstallJvm", "current");
/*  984 */       String setting = null;
/*  985 */       String value = null;
/*  986 */       boolean isMicrosoftVM = false;
/*      */ 
/*  988 */       if (jvmChoice.equals("current"))
/*      */       {
/*  991 */         return;
/*      */       }
/*  993 */       if (jvmChoice.equals("custom"))
/*      */       {
/*  995 */         String isJdbcStr = installer.getConfigValue("IsJdbc");
/*  996 */         if ((!EnvUtils.isFamily("windows")) || (StringUtils.convertToBool(isJdbcStr, true)))
/*      */         {
/*  999 */           setting = "JAVA_EXE";
/* 1000 */           value = this.m_installer.getInstallValue("JvmPath", "java");
/*      */         }
/*      */         else
/*      */         {
/* 1004 */           setting = "UseMicrosoftVM";
/* 1005 */           value = "true";
/* 1006 */           isMicrosoftVM = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1011 */         String isJdbcStr = installer.getConfigValue("IsJdbc");
/* 1012 */         if ((!EnvUtils.isFamily("windows")) || (StringUtils.convertToBool(isJdbcStr, true)))
/*      */         {
/* 1015 */           setting = "JAVA_EXE";
/*      */         }
/*      */         else
/*      */         {
/* 1019 */           setting = "UseMicrosoftVM";
/* 1020 */           value = "true";
/* 1021 */           isMicrosoftVM = true;
/*      */         }
/*      */       }
/* 1024 */       cleanJvmCommandLine(installer);
/*      */ 
/* 1026 */       String jdbcDriver = installer.getConfigValue("JdbcDriver");
/* 1027 */       if (jdbcDriver != null)
/*      */       {
/* 1029 */         boolean isMicrosoftJdbc = jdbcDriver.equals("com.ms.jdbc.odbc.JdbcOdbcDriver");
/*      */ 
/* 1031 */         if ((isMicrosoftJdbc) && (!isMicrosoftVM))
/*      */         {
/* 1033 */           installer.editConfigFile("config/config.cfg", "JdbcDriver", "sun.jdbc.odbc.JdbcOdbcDriver");
/*      */         }
/* 1036 */         else if ((!isMicrosoftJdbc) && (isMicrosoftVM))
/*      */         {
/* 1038 */           installer.editConfigFile("config/config.cfg", "JdbcDriver", "com.ms.jdbc.odbc.JdbcOdbcDriver");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1043 */       if (adminCfg)
/*      */       {
/* 1045 */         SysInstaller adminInstaller = this.m_installer.deriveInstaller(this.m_installer.computeDestination("admin/"));
/*      */ 
/* 1047 */         adminInstaller.loadConfig();
/* 1048 */         cleanJvmCommandLine(adminInstaller);
/*      */       }
/* 1050 */       installer.editConfigFile("bin/intradoc.cfg", setting, value);
/* 1051 */       if (adminCfg)
/*      */       {
/* 1053 */         installer.editConfigFile("bin/intradoc.cfg", setting, value);
/*      */       }
/*      */ 
/* 1061 */       String publishDir = this.m_installer.computeDestinationEx("data/schema/publishlock", false);
/*      */ 
/* 1063 */       if (FileUtils.checkFile(publishDir, false, false) == 0)
/*      */       {
/* 1065 */         WebPublishUtils.setPublishEverythingAtStartup(null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1070 */     if (installer.isOlderVersion("7.0"))
/*      */     {
/* 1072 */       String relativeCgiRoot = installer.getConfigValue("HttpRelativeCgiRoot");
/*      */ 
/* 1074 */       if (relativeCgiRoot == null)
/*      */       {
/* 1076 */         installer.editConfigFile("config/config.cfg", "HttpRelativeCgiRoot", "/intradoc-cgi/");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1082 */     if (installer.isOlderVersion("7.2.1"))
/*      */     {
/* 1084 */       String[] keys = { "SearchEngineName", "SearchIndexerEngineName" };
/* 1085 */       for (int i = 0; i < keys.length; ++i)
/*      */       {
/* 1087 */         String key = installer.getConfigValue(keys[i]);
/* 1088 */         if (key == null)
/*      */           continue;
/* 1090 */         if (key.equals("VERITY"))
/*      */         {
/* 1092 */           installer.editConfigFile("config/config.cfg", key, "VERITY.VDK.4");
/*      */         }
/* 1094 */         else if (key.equals("DATABASE"))
/*      */         {
/* 1096 */           installer.editConfigFile("config/config.cfg", key, "DATABASE.METADATA");
/*      */         } else {
/* 1098 */           if (!key.equals("DATABASEFULLTEXT"))
/*      */             continue;
/* 1100 */           installer.editConfigFile("config/config.cfg", key, "DATABASE.FULLTEXT");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1133 */     String stateCfgFile = installer.computeDestination("config/state.cfg");
/* 1134 */     if (FileUtils.checkFile(stateCfgFile, true, false) != 0)
/*      */     {
/*      */       try
/*      */       {
/* 1138 */         BufferedWriter w = FileUtils.openDataWriter(installer.computeDestination("config/"), "state.cfg");
/*      */ 
/* 1141 */         w.close();
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/* 1145 */         String msg = LocaleUtils.encodeMessage("csResourceUtilsFileWriteError", null, stateCfgFile);
/*      */ 
/* 1148 */         Report.trace("install", null, e);
/* 1149 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1154 */     String webServer = installer.getConfigValue("WebServer");
/* 1155 */     String webServerSetting = this.m_installer.getInstallValue("WebServer", null);
/* 1156 */     if ((webServer == null) && (webServerSetting != null))
/*      */     {
/* 1158 */       installer.editConfigFile("config/config.cfg", "WebServer", webServerSetting);
/*      */     }
/*      */ 
/* 1162 */     String databaseType = installer.getConfigValue("DatabaseType");
/* 1163 */     String databaseTypeSetting = this.m_installer.getInstallValue("DatabaseType", null);
/* 1164 */     if ((databaseType == null) && (databaseTypeSetting != null) && (!StringUtils.isConfigValueCaseInsensitiveStartsWith("skip", databaseTypeSetting)))
/*      */     {
/* 1167 */       installer.editConfigFile("config/config.cfg", "DatabaseType", databaseTypeSetting);
/*      */     }
/*      */ 
/* 1171 */     String publishFile = installer.computeDestination("data/schema/publishlock/publish.dat");
/* 1172 */     FileUtils.touchFile(publishFile);
/*      */ 
/* 1176 */     ExecutionContext context = new ExecutionContextAdaptor();
/* 1177 */     ConfigFileStore cfs = ConfigFileLoader.createNewConfigFileStore(installer.m_idcDir, null);
/*      */ 
/* 1179 */     ConfigFileUtilities.createConfigFileUtilities(cfs, context);
/*      */ 
/* 1183 */     WebPublishUtils.setPublishEverythingAtStartup(context);
/*      */ 
/* 1185 */     if ((installer.isWindows()) && 
/* 1187 */       (installer.getInstallBool("IsProxiedServer", false)))
/*      */     {
/* 1190 */       WindowsShortcuts shortcuts = new WindowsShortcuts();
/* 1191 */       shortcuts.init(installer);
/* 1192 */       shortcuts.installSection("core-shortcuts", "always", null, installer, null);
/*      */     }
/*      */ 
/* 1197 */     if (installer != this.m_installer)
/*      */     {
/* 1200 */       installer.installSection("proxy-etc");
/* 1201 */       installer.installSection("core-launchers");
/*      */ 
/* 1205 */       copyWeblayoutResources(installer);
/*      */     }
/*      */ 
/* 1209 */     installer.installSection("core-remove-patches");
/*      */ 
/* 1228 */     writeVersionInfo(installer);
/*      */   }
/*      */ 
/*      */   public void cleanJvmCommandLine(SysInstaller installer)
/*      */     throws ServiceException
/*      */   {
/*      */   }
/*      */ 
/*      */   public void copyWeblayoutResources(SysInstaller installer)
/*      */     throws ServiceException
/*      */   {
/* 1242 */     String source = installer.computeDestinationEx("weblayout/resources/layouts", false);
/*      */ 
/* 1244 */     String target = installer.computeDestination("weblayout/resources/layouts");
/*      */ 
/* 1246 */     Report.trace("install", "copying layouts from " + source + " to " + target, null);
/*      */ 
/* 1248 */     FileUtils.copyDirectoryWithFlags(new File(source), new File(target), 1, null, 1);
/*      */   }
/*      */ 
/*      */   public boolean accept(File dir, String name)
/*      */   {
/* 1254 */     return name.startsWith("weblayout/resources");
/*      */   }
/*      */ 
/*      */   public void registerWithAdminServer(String adminServerDir)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1270 */       this.m_unattemptedWork = 2;
/*      */ 
/* 1272 */       if (adminServerDir == null)
/*      */       {
/* 1274 */         throw new ServiceException("!csInstallerMasterServerDirUndefined");
/*      */       }
/* 1276 */       SysInstaller adminInstaller = this.m_installer.deriveInstaller(adminServerDir);
/* 1277 */       adminInstaller.loadConfig();
/* 1278 */       String adminName = adminInstaller.getConfigValue("IDC_Admin_Name");
/* 1279 */       if (adminName == null)
/*      */       {
/* 1281 */         adminName = this.m_installer.getConfigValue("IDC_Admin_Name");
/* 1282 */         if (adminName == null)
/*      */         {
/* 1284 */           adminName = this.m_installer.getConfigValue("IDC_Name");
/* 1285 */           adminName = adminName + "_admin";
/*      */         }
/*      */       }
/* 1288 */       SharedObjects.init();
/* 1289 */       SharedObjects.putEnvironmentValue("IDC_Admin_Name", adminName);
/* 1290 */       String intradocDir = adminServerDir + "/../";
/* 1291 */       SharedObjects.putEnvironmentValue("IntradocDir", intradocDir);
/* 1292 */       String homeDir = this.m_installer.computeDestinationEx("${IdcHomeDir}", false);
/* 1293 */       if (FileUtils.checkFile(homeDir, false, false) != 0)
/*      */       {
/* 1295 */         homeDir = intradocDir;
/*      */       }
/* 1297 */       SharedObjects.putEnvironmentValue("IdcHomeDir", homeDir);
/* 1298 */       AdminServerUtils.setupFileStoreForAdminServer();
/*      */ 
/* 1300 */       ServerData serverData = ConfigFileLoader.m_defaultCFU.getServerData();
/* 1301 */       DataBinder cfsBinder = serverData.m_binder;
/* 1302 */       cfsBinder.putLocal("AdminServerDir", adminServerDir);
/* 1303 */       cfsBinder.putLocal("IntradocDir", adminServerDir + "../");
/*      */ 
/* 1305 */       String[][] settings = { { "IdcHomeDir", "IdcHomeDir", "/../" }, { "DataDir", "AdminDataDir", "/data" } };
/*      */ 
/* 1310 */       for (String[] setting : settings)
/*      */       {
/* 1312 */         String value = adminInstaller.getConfigValue(setting[0]);
/* 1313 */         if ((value == null) && (setting[2] != null))
/*      */         {
/* 1315 */           value = adminServerDir + setting[2];
/*      */         }
/* 1317 */         if (value == null)
/*      */           continue;
/* 1319 */         cfsBinder.putLocal(setting[1], value);
/*      */       }
/*      */ 
/* 1323 */       serverData.init(cfsBinder);
/*      */ 
/* 1325 */       SharedObjects.putEnvironmentValue("IdcProductName", "idcadmin");
/* 1326 */       AdminDirectoryLocator.init();
/*      */ 
/* 1328 */       SharedObjects.putEnvironmentValue("IdcResourcesDir", FileUtils.getAbsolutePath(homeDir, "resources"));
/*      */ 
/* 1330 */       SharedObjects.putEnvironmentValue("PrimaryResourceFile", "$IdcResourcesDir/admin/tables/resource_files.htm");
/*      */ 
/* 1333 */       DirectoryLocator.buildRootDirectories();
/* 1334 */       AdminServerUtils.setupFileStoreForAdminServer();
/* 1335 */       IdcAdminSystemLoader.validateStandardDirectories();
/* 1336 */       ComponentLoader.reset();
/* 1337 */       ComponentLoader.initDefaults();
/* 1338 */       reportProgress("!csInstallerProgressAdminServerConfig");
/* 1339 */       AdminServerUtils.loadComponentData();
/*      */ 
/* 1341 */       AdminServerData servers = new AdminServerData();
/* 1342 */       servers.load(ConfigFileLoader.m_defaultCFU);
/* 1343 */       SharedObjects.putTable("ServerDefinition", servers);
/*      */ 
/* 1345 */       DataBinder binder = new DataBinder();
/* 1346 */       binder.putLocal("IntradocDir", this.m_installer.m_idcDir);
/* 1347 */       binder.putLocal("FileEncoding", DataSerializeUtils.getSystemEncoding());
/* 1348 */       binder.putLocal("IDC_Name", this.m_installer.getConfigValue("IDC_Name"));
/* 1349 */       if (this.m_isRefinery)
/*      */       {
/* 1351 */         String idcName = this.m_installer.getConfigValue("IDC_Name");
/* 1352 */         binder.putLocal("contentServerType", "partial");
/* 1353 */         binder.putLocal("relativeServerLogLink", "groups/secure/logs/IdcRefLog.htm");
/* 1354 */         binder.putLocal("HttpRelativeWebRoot", this.m_installer.getConfigValue("HttpRelativeWebRoot"));
/* 1355 */         binder.putLocal("procControlScriptPrefix", "idcrefinery_");
/* 1356 */         binder.putLocal("serviceName", "IdcRefineryService " + idcName);
/*      */       }
/*      */ 
/* 1359 */       binder.putLocal("description", this.m_installer.getConfigValue("InstanceMenuLabel"));
/* 1360 */       AdminService service = new AdminService();
/* 1361 */       service.init(null, System.err, binder, new ServiceData());
/* 1362 */       service.initDelegatedObjects();
/* 1363 */       service.addServerReference();
/* 1364 */       boolean serverExists = false;
/*      */       try
/*      */       {
/* 1367 */         service.saveServerData();
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1371 */         if (e.m_errorCode == -17)
/*      */         {
/* 1373 */           serverExists = true;
/*      */         }
/*      */         else
/*      */         {
/* 1377 */           throw e;
/*      */         }
/*      */       }
/*      */ 
/* 1381 */       if (!serverExists)
/*      */       {
/* 1383 */         reportProgress("!csInstallerAdminServerConfigured");
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1388 */       failure(t);
/*      */     }
/* 1390 */     this.m_warnings.removeElementAt(0);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1395 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99914 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CoreConfigure
 * JD-Core Version:    0.5.4
 */