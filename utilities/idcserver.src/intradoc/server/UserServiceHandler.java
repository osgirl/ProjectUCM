/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.schema.SchemaUtils;
/*      */ import intradoc.server.script.ScriptExtensionUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RoleDefinitions;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.SharedUtils;
/*      */ import intradoc.shared.TopicInfo;
/*      */ import intradoc.shared.UserAttribInfo;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.shared.localization.SharedLocalizationHandler;
/*      */ import intradoc.shared.localization.SharedLocalizationHandlerFactory;
/*      */ import intradoc.shared.schema.SchemaSecurityFilter;
/*      */ import intradoc.shared.schema.SchemaViewConfig;
/*      */ import intradoc.shared.schema.SchemaViewData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.net.ServerSocket;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class UserServiceHandler extends ServiceHandler
/*      */ {
/*      */   protected String m_userCacheDir;
/*      */   protected String m_cacheName;
/*      */   protected String m_newCache;
/*      */   protected String m_backupCache;
/*      */   protected String m_securityInfoName;
/*   55 */   static final String[] m_exportedParameters = { "IDC_Name", "IntradocServerPort", "IntradocServerHostName", "AuthFilePathPrefix", "UseAccounts", "DefaultAuth", "WebServerAuthOnly", "IdcAuthExtraRequestParams", "DefaultMasterDomain", "AccountMapPrefix", "NetworkAdminGroup", "DefaultNetworkAccounts", "UseLocalGroups", "LocalGroupServer", "IdcRealm", "CGI_DEBUG", "CGI_SEND_DUMP", "SpecialAuthGroups", "CGI_RECEIVE_DUMP", "FILTER_DEBUG", "FILTER_DUMP", "DomainControllerName", "IsJspServerEnabled", "JspEnabledGroups", "IsIntranetAuthOnly", "ProfileCacheTimeoutInMins", "DisableGzipCompression", "CheckWebFileExists", "ForceSystemConfigPage", "IsExternalLogout" };
/*      */ 
/*      */   public UserServiceHandler()
/*      */   {
/*   66 */     initInfo(null);
/*      */   }
/*      */ 
/*      */   public UserServiceHandler(Workspace ws)
/*      */   {
/*   71 */     initInfo(ws);
/*      */   }
/*      */ 
/*      */   public void initInfo(Workspace ws)
/*      */   {
/*   76 */     this.m_userCacheDir = LegacyDirectoryLocator.getUserPublishCacheDir();
/*   77 */     this.m_cacheName = (this.m_userCacheDir + "userdb.txt");
/*   78 */     this.m_newCache = (this.m_cacheName + ".new");
/*   79 */     this.m_backupCache = (this.m_cacheName + ".bak");
/*      */ 
/*   81 */     this.m_securityInfoName = "SecurityInfo.hda";
/*   82 */     this.m_workspace = ws;
/*      */   }
/*      */ 
/*      */   public void init(Service service)
/*      */     throws ServiceException, DataException
/*      */   {
/*   88 */     super.init(service);
/*      */ 
/*   90 */     Workspace ws = null;
/*   91 */     if (service != null)
/*      */     {
/*   93 */       ws = service.getWorkspace();
/*      */     }
/*   95 */     initInfo(ws);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateCache()
/*      */     throws ServiceException, DataException
/*      */   {
/*  103 */     Users userList = (Users)SharedObjects.getTable("Users");
/*  104 */     RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*      */ 
/*  106 */     Workspace ws = WorkspaceUtils.getWorkspace("user");
/*  107 */     if (!userList.isAllLocalAttributesLoaded())
/*      */     {
/*  109 */       if (ws == null)
/*      */       {
/*  111 */         throw new ServiceException("!csUserWorkspaceNotAvailable");
/*      */       }
/*      */       try
/*      */       {
/*  115 */         ResultSet rset = ws.createResultSet("QallLocalUserSecurityAttributes", null);
/*      */ 
/*  117 */         userList.loadAllAttributes(rset);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  121 */         throw new ServiceException("!csUserUnableToLoad", e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  127 */     if ((EnvUtils.isHostedInAppServer()) && (SharedObjects.getEnvValueAsBoolean("ForceSystemConfigPage", false)))
/*      */     {
/*  130 */       return;
/*      */     }
/*      */ 
/*  136 */     Vector users = userList.getLocalUsers();
/*      */     try
/*      */     {
/*  145 */       FileUtils.reserveDirectory(this.m_userCacheDir, true);
/*      */ 
/*  147 */       if (!this.m_service.executeFilter("publishDataForWebServer"))
/*      */       {
/*  149 */         Report.trace("system", "Not doing default publishing because a filter returned abort.", null);
/*      */         return;
/*      */       }
/*      */ 
/*  158 */       writeSecurityInfo(userList, roleDefs, users);
/*      */ 
/*  161 */       writeWebServerConfig();
/*      */ 
/*  166 */       writeUserList(userList, roleDefs, users);
/*      */     }
/*      */     finally
/*      */     {
/*  170 */       FileUtils.releaseDirectory(this.m_userCacheDir, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeUserList(Users userList, RoleDefinitions roleDefs, Vector users)
/*      */     throws ServiceException
/*      */   {
/*  179 */     Writer fw = null;
/*  180 */     String name = this.m_cacheName + ".new";
/*  181 */     boolean result = false;
/*      */     try
/*      */     {
/*  185 */       fw = FileUtilsCfgBuilder.getCfgWriter(name, "User");
/*      */ 
/*  189 */       UserData anonUserData = UserUtils.createUserData("anonymous");
/*  190 */       anonUserData.setAttributes((String[][])null);
/*  191 */       Vector anonGroups = SecurityUtils.getUserGroupsWithPrivilege(anonUserData, 1);
/*      */ 
/*  193 */       writeUserGroups("anonymous", "", anonGroups, fw);
/*  194 */       result = true;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  202 */       closeFile(fw);
/*      */     }
/*      */ 
/*  205 */     if (!result)
/*      */       return;
/*  207 */     moveCache(this.m_newCache, this.m_cacheName, true);
/*      */   }
/*      */ 
/*      */   protected void closeFile(Writer fw)
/*      */   {
/*      */     try
/*      */     {
/*  215 */       if (fw != null)
/*      */       {
/*  217 */         fw.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/*  222 */       ignore.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeWebServerConfig() throws ServiceException, DataException
/*      */   {
/*  228 */     String[] versions = { "", "2", "22" };
/*  229 */     for (String version : versions)
/*      */     {
/*      */       try
/*      */       {
/*  233 */         String targetFile = SharedObjects.getEnvironmentValue("Apache" + version + "ConfigFile");
/*      */ 
/*  235 */         if (targetFile == null)
/*      */         {
/*  237 */           targetFile = FileUtils.directorySlashes(this.m_userCacheDir) + "apache" + version + "/apache.conf";
/*      */         }
/*      */ 
/*  240 */         writeApacheConfig(targetFile, version);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  244 */         SharedUtils.logCommonException(e, null, 0);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  248 */         SharedUtils.logCommonException(e, null, 0);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void writeApacheConfig(String targetFile, String apacheVersion)
/*      */     throws ServiceException, DataException
/*      */   {
/*  256 */     IdcMessage msg = IdcMessageFactory.lc("csUnableToPublishWebConfigFile", new Object[] { FileUtils.fileSlashes(targetFile) });
/*      */ 
/*  258 */     DataBinder tmpBinder = this.m_binder;
/*  259 */     DataBinder origInfoData = (DataBinder)AppObjectRepository.getObject("SecurityInfo");
/*  260 */     DataBinder infoData = origInfoData.createShallowCopy();
/*      */     try
/*      */     {
/*  263 */       String dir = FileUtils.getDirectory(targetFile);
/*  264 */       FileUtils.checkOrCreateDirectory(dir, 1);
/*      */ 
/*  266 */       DataBinder binder = new DataBinder(SharedObjects.getSafeEnvironment());
/*  267 */       PluginFilters.filter("populateApacheConfigBinder", this.m_workspace, binder, this.m_service);
/*  268 */       DataResultSet servers = new DataResultSet();
/*  269 */       servers.copy(infoData.getResultSet("ProxiedServers"));
/*      */ 
/*  274 */       Vector newFields = new Vector();
/*  275 */       for (String fieldName : new String[] { "psIDC_Name", "psWeblayoutDir", "psUseMasterSecurityProfile", "psHttpRelativeWebRoot" })
/*      */       {
/*  278 */         FieldInfo info = new FieldInfo();
/*  279 */         info.m_name = fieldName;
/*  280 */         newFields.addElement(info);
/*      */       }
/*  282 */       servers.mergeFieldsWithFlags(newFields, 0);
/*      */ 
/*  284 */       Properties rowProps = new Properties();
/*  285 */       rowProps.put("psIDC_Name", SharedObjects.getEnvironmentValue("IDC_Name"));
/*  286 */       String httpRelativeWebRoot = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*  287 */       if (httpRelativeWebRoot == null)
/*      */       {
/*  289 */         throw new ServiceException(null, "csUnableToFindValue", new Object[] { "HttpRelativeWebRoot" });
/*      */       }
/*  291 */       boolean trimWebRoot = SharedObjects.getEnvValueAsBoolean("Apache" + apacheVersion + "TrimWebRoot", true);
/*      */ 
/*  293 */       if (trimWebRoot)
/*      */       {
/*  295 */         httpRelativeWebRoot = FileUtils.fileSlashes(FileUtils.windowsSlashes(httpRelativeWebRoot));
/*      */       }
/*      */ 
/*  298 */       binder.addResultSet("Servers", servers);
/*  299 */       boolean doWindowsEscaping = false;
/*  300 */       String modPath = SharedObjects.getEnvironmentValue("Apache" + apacheVersion + "ModulePath");
/*      */ 
/*  302 */       if (modPath != null)
/*      */       {
/*  304 */         doWindowsEscaping = EnvUtils.isFamily("windows");
/*      */       }
/*      */       else
/*      */       {
/*  308 */         String apacheModFragment = "lib/IdcApache" + apacheVersion + "Auth";
/*  309 */         Map options = new HashMap();
/*  310 */         Object type = "type_webserverobject";
/*  311 */         options.put(type, type);
/*  312 */         Map rc = EnvUtils.normalizeOSPath(apacheModFragment, options);
/*  313 */         modPath = (String)rc.get("path");
/*  314 */         if (modPath == null)
/*      */         {
/*  316 */           if (SharedObjects.getEnvValueAsBoolean("ApacheConfLoadModuleEnabled", true))
/*      */           {
/*  318 */             throw new ServiceException(null, "syUnableToFindFile", new Object[] { apacheModFragment });
/*      */           }
/*  320 */           modPath = "<unknown>";
/*      */         }
/*      */         else
/*      */         {
/*  324 */           String family = (String)rc.get("osFamily");
/*  325 */           doWindowsEscaping = family.equals("windows");
/*      */         }
/*      */       }
/*  328 */       int fixDirFlags = 13;
/*  329 */       if (doWindowsEscaping)
/*      */       {
/*  331 */         fixDirFlags |= 36;
/*      */       }
/*      */ 
/*  334 */       modPath = FileUtils.fixDirectorySlashes(modPath, fixDirFlags).toString();
/*  335 */       binder.putLocal("ApacheModFilePath", modPath);
/*  336 */       String cacheName = FileUtils.fixDirectorySlashes(this.m_cacheName, fixDirFlags).toString();
/*  337 */       binder.putLocal("UserDBPath", cacheName);
/*  338 */       String webRoot = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*  339 */       if (trimWebRoot)
/*      */       {
/*  341 */         webRoot = FileUtils.fileSlashes(FileUtils.windowsSlashes(webRoot));
/*      */       }
/*  343 */       rowProps.put("psHttpRelativeWebRoot", webRoot);
/*  344 */       String webDir = SharedObjects.getEnvironmentValue("Apache" + apacheVersion + "WeblayoutDir");
/*      */ 
/*  346 */       if (webDir == null)
/*      */       {
/*  348 */         webDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*      */       }
/*  350 */       webDir = FileUtils.fixDirectorySlashes(webDir, fixDirFlags | 0x2).toString();
/*      */ 
/*  352 */       rowProps.put("psWeblayoutDir", webDir);
/*  353 */       rowProps.put("psUseMasterSecurityProfile", "1");
/*      */ 
/*  355 */       Vector row = servers.createRow(new PropParameters(rowProps));
/*  356 */       servers.insertRowAt(row, 0);
/*      */ 
/*  358 */       if (!SharedObjects.getEnvValueAsBoolean("EnableApacheConfigPublishing", true))
/*      */       {
/*  360 */         Report.trace("system", "Not doing web server config publishing because EnableApacheConfigPublishing=false.", null);
/*      */         return;
/*      */       }
/*      */ 
/*  365 */       if (!this.m_service.executeFilter("publishWebServerConfig"))
/*      */       {
/*  367 */         Report.trace("system", "Not doing web server config publishing because a filter returned abort.", null);
/*      */         return;
/*      */       }
/*      */ 
/*  372 */       this.m_service.m_pageMerger.setActiveBinder(binder);
/*  373 */       String data = this.m_service.m_pageMerger.evaluateResourceInclude("apache_conf_file");
/*  374 */       FileUtils.writeFileRaw(data, new File(targetFile + ".tmp"), null, 1);
/*      */ 
/*  376 */       FileUtils.renameFile(targetFile + ".tmp", targetFile);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  388 */       this.m_service.m_pageMerger.setActiveBinder(tmpBinder);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeSecurityInfo(Users userList, RoleDefinitions roleDefs, Vector users)
/*      */     throws ServiceException
/*      */   {
/*  398 */     DataBinder infoData = new DataBinder();
/*      */ 
/*  402 */     DataResultSet rsRoles = new DataResultSet();
/*  403 */     ResultSetFilter rFilter = new ResultSetFilter()
/*      */     {
/*      */       public int checkRow(String val, int curNumRows, Vector row)
/*      */       {
/*      */         try
/*      */         {
/*  409 */           int iVal = Integer.parseInt(val);
/*  410 */           if ((iVal & 0x1) == 0)
/*      */           {
/*  412 */             return 0;
/*      */           }
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*  417 */           return 0;
/*      */         }
/*  419 */         return 1;
/*      */       }
/*      */     };
/*  423 */     rsRoles.copyFiltered(roleDefs, "dPrivilege", rFilter);
/*      */ 
/*  425 */     infoData.addResultSet("RoleDefinition", rsRoles);
/*      */ 
/*  428 */     DataResultSet rsAttributes = new DataResultSet(new String[] { "user", "password", "passwordencoding", "roles", "accounts", "extendedUserInfo" });
/*      */ 
/*  430 */     int size = users.size();
/*  431 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  433 */       UserData data = (UserData)users.elementAt(i);
/*  434 */       addUserInfo(userList, data, rsAttributes);
/*      */     }
/*      */ 
/*  439 */     UserData anonUserData = UserUtils.createUserData("anonymous");
/*  440 */     anonUserData.setAttributes((String[][])null);
/*  441 */     addUserInfo(userList, anonUserData, rsAttributes);
/*      */ 
/*  443 */     infoData.addResultSet("UserSecurityInfo", rsAttributes);
/*      */ 
/*  446 */     addExportedConfigurationParameters(infoData);
/*      */ 
/*  448 */     addServerProviderConfig(infoData);
/*      */ 
/*  451 */     DataResultSet proxiedServers = computeWebServerProxiedServersResultSet();
/*  452 */     if (proxiedServers != null)
/*      */     {
/*  454 */       infoData.addResultSet("ProxiedServers", proxiedServers);
/*      */     }
/*      */ 
/*  458 */     String securedUrls = SharedObjects.getEnvironmentValue("AuthFilterSecuredRelativeUrls");
/*  459 */     if (securedUrls == null)
/*      */     {
/*  461 */       securedUrls = computeAllowableSecurityUrlPrefixes(proxiedServers);
/*      */     }
/*  463 */     if ((securedUrls != null) && (securedUrls.length() > 0) && (!securedUrls.startsWith("#n")))
/*      */     {
/*  465 */       infoData.putLocal("AuthFilterSecuredRelativeUrls", securedUrls);
/*      */     }
/*      */ 
/*  469 */     DataResultSet drset = SharedObjects.getTable("IdcAuthPlugins");
/*  470 */     if (drset != null)
/*      */     {
/*  472 */       infoData.addResultSet("IdcAuthPlugins", drset);
/*      */     }
/*      */ 
/*  476 */     String webPluginDir = SharedObjects.getEnvironmentValue("WebPluginRootDir");
/*  477 */     if ((webPluginDir == null) || (webPluginDir.length() == 0))
/*      */     {
/*  479 */       webPluginDir = DirectoryLocator.getNativeDirectory();
/*      */     }
/*      */ 
/*  483 */     infoData.putLocal("SharedDir", webPluginDir);
/*  484 */     infoData.putLocal("WebPluginRootDir", webPluginDir);
/*      */     try
/*      */     {
/*  491 */       PluginFilters.filter("addExportedWebFilterConfiguration", this.m_workspace, infoData, this.m_service);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  496 */       Report.error(null, null, e);
/*      */     }
/*      */ 
/*  500 */     String encoding = SharedObjects.getEnvironmentValue("WebEncoding");
/*      */ 
/*  502 */     ResourceUtils.serializeDataBinderWithEncoding(this.m_userCacheDir, this.m_securityInfoName, infoData, 1, encoding);
/*      */ 
/*  505 */     AppObjectRepository.putObject("SecurityInfo", infoData);
/*      */   }
/*      */ 
/*      */   protected DataResultSet computeWebServerProxiedServersResultSet()
/*      */   {
/*  513 */     DataResultSet webProxiedServers = SharedObjects.getTable("WebServerProxiedServers");
/*  514 */     if (webProxiedServers != null)
/*      */     {
/*  516 */       return webProxiedServers;
/*      */     }
/*      */ 
/*  520 */     DataResultSet proxiedServers = SharedObjects.getTable("ProxiedServers");
/*      */ 
/*  522 */     if (SharedObjects.getEnvValueAsBoolean("WebProxyAdminServer", false))
/*      */     {
/*      */       try
/*      */       {
/*  526 */         DataResultSet adminProxiedServers = SharedObjects.getTable("AdminProxiedServers");
/*  527 */         if (adminProxiedServers != null)
/*      */         {
/*  530 */           executeFieldEntriesAsScript(adminProxiedServers);
/*      */ 
/*  533 */           if (proxiedServers != null)
/*      */           {
/*  535 */             proxiedServers.mergeFields(adminProxiedServers);
/*  536 */             proxiedServers.merge("psHttpRelativeWebRoot", adminProxiedServers, false);
/*      */           }
/*      */           else
/*      */           {
/*  540 */             proxiedServers = adminProxiedServers;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  547 */         this.m_service.logError(e, "!csUnableToComputeProxyEntryForAdmin");
/*      */       }
/*      */     }
/*      */ 
/*  551 */     if (proxiedServers != null)
/*      */     {
/*  553 */       SharedObjects.putTable("WebServerProxiedServers", proxiedServers);
/*      */     }
/*  555 */     return proxiedServers;
/*      */   }
/*      */ 
/*      */   protected String computeAllowableSecurityUrlPrefixes(DataResultSet proxiedServers)
/*      */   {
/*  560 */     boolean multipleMasters = SharedObjects.getEnvValueAsBoolean("AllowsMultipleMastersSingleHost", false);
/*  561 */     String retVal = null;
/*  562 */     if (multipleMasters)
/*      */     {
/*  564 */       Vector v = new IdcVector();
/*  565 */       String myRoot = DocumentPathBuilder.getRelativeWebRoot();
/*  566 */       v.addElement(myRoot);
/*  567 */       if (proxiedServers != null)
/*      */       {
/*  570 */         FieldInfo fi = new FieldInfo();
/*  571 */         fi.m_name = "psUseMasterSecurityProfile";
/*  572 */         fi.m_type = 6;
/*  573 */         Vector infos = new IdcVector();
/*  574 */         infos.addElement(fi);
/*  575 */         proxiedServers.mergeFieldsWithFlags(infos, 0);
/*      */         try
/*      */         {
/*  579 */           String[][] rows = ResultSetUtils.createStringTable(proxiedServers, new String[] { "psHttpRelativeWebRoot", "psUseMasterSecurityProfile" });
/*      */ 
/*  581 */           for (int i = 0; i < rows.length; ++i)
/*      */           {
/*  583 */             if (StringUtils.convertToBool(rows[i][1], false))
/*      */               continue;
/*  585 */             v.addElement(rows[i][0]);
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  591 */           Report.trace(null, null, e);
/*      */         }
/*      */       }
/*  594 */       retVal = StringUtils.createString(v, ',', ',');
/*      */     }
/*  596 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected void executeFieldEntriesAsScript(DataResultSet drset) throws IOException
/*      */   {
/*  601 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  603 */       Vector v = drset.getCurrentRowValues();
/*  604 */       executeEntriesAsScript(v);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void executeEntriesAsScript(Vector v) throws IOException
/*      */   {
/*  610 */     int n = v.size();
/*  611 */     for (int i = 0; i < n; ++i)
/*      */     {
/*  615 */       DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  616 */       PageMerger pageMerger = new PageMerger(binder, null);
/*  617 */       String val = pageMerger.evaluateScript((String)v.elementAt(i));
/*  618 */       v.setElementAt(val, i);
/*  619 */       pageMerger.releaseAllTemporary();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addExportedConfigurationParameters(DataBinder binder)
/*      */   {
/*  626 */     DataResultSet wfcSet = SharedObjects.getTable("WebFilterConfiguration");
/*  627 */     if (wfcSet != null)
/*      */     {
/*  629 */       binder.addResultSet("WebFilterConfiguration", wfcSet);
/*      */     }
/*      */ 
/*  633 */     for (int i = 0; i < m_exportedParameters.length; ++i)
/*      */     {
/*  635 */       String val = SharedObjects.getEnvironmentValue(m_exportedParameters[i]);
/*  636 */       if (val == null)
/*      */         continue;
/*  638 */       binder.putLocal(m_exportedParameters[i], val);
/*      */     }
/*      */ 
/*  648 */     if (SharedObjects.getEnvValueAsBoolean("AllowServerCredentialsOverride", false))
/*      */     {
/*  650 */       binder.putLocal("FORCE_WEBSERVER_CREDENTIALS", "1");
/*      */     }
/*      */ 
/*  655 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/*  657 */       binder.putLocal("UseAccounts", "true");
/*      */     }
/*      */ 
/*  661 */     String extraCustomVars = SharedObjects.getEnvironmentValue("IdcAuthExtraConfigParams");
/*  662 */     if (extraCustomVars != null)
/*      */     {
/*  664 */       Vector vars = StringUtils.parseArrayEx(extraCustomVars, ',', '^', true);
/*  665 */       for (int i = 0; i < vars.size(); ++i)
/*      */       {
/*  667 */         String key = (String)vars.elementAt(i);
/*  668 */         String val = SharedObjects.getEnvironmentValue(key);
/*  669 */         if (val == null)
/*      */           continue;
/*  671 */         binder.putLocal(key, val);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  682 */     String httpCgiRelativeUrl = LegacyDirectoryLocator.getCgiWebUrl(false);
/*  683 */     binder.putLocal("HttpCgiPath", httpCgiRelativeUrl);
/*      */   }
/*      */ 
/*      */   protected void addServerProviderConfig(DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  689 */     String port = SharedObjects.getEnvironmentValue("IntradocServerPort");
/*  690 */     if (port == null)
/*      */     {
/*  692 */       Provider p = Providers.getProvider("SystemServerSocket");
/*  693 */       if (p == null)
/*      */       {
/*  695 */         if (SystemUtils.m_verbose)
/*      */         {
/*  697 */           Report.debug("system", "no SystemServerSocket provider available when writing SecurityInfo.hda", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  703 */         ServerSocket socket = (ServerSocket)p.getProviderObject("ServerSocket");
/*  704 */         if (socket == null)
/*      */         {
/*  706 */           if (SystemUtils.m_verbose)
/*      */           {
/*  708 */             Report.debug("system", "ServerSocket not available when writing SecurityInfo.hda", null);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*  713 */           port = "" + socket.getLocalPort();
/*      */       }
/*      */     }
/*  716 */     if (port == null)
/*      */     {
/*  719 */       DataBinder tmpBinder = new DataBinder();
/*  720 */       ResourceUtils.serializeDataBinderWithEncoding(this.m_userCacheDir, this.m_securityInfoName, tmpBinder, 0, null);
/*      */ 
/*  722 */       port = tmpBinder.getLocal("IntradocServerPort");
/*      */     }
/*  724 */     if (port == null)
/*      */       return;
/*  726 */     binder.putLocal("IntradocServerPort", port);
/*      */   }
/*      */ 
/*      */   protected void writeUserGroups(String user, String pswrd, Vector groups, Writer writer)
/*      */     throws IOException
/*      */   {
/*  733 */     if (groups == null)
/*      */     {
/*  735 */       return;
/*      */     }
/*      */ 
/*  739 */     int numGroups = groups.size();
/*  740 */     for (int j = 0; j < numGroups; ++j)
/*      */     {
/*  742 */       String group = (String)groups.elementAt(j);
/*  743 */       String row = user + "\t";
/*  744 */       if (pswrd != null)
/*      */       {
/*  746 */         row = row + pswrd;
/*      */       }
/*  748 */       row = row + "\t\t\t" + group + "\n";
/*  749 */       writer.write(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addUserInfo(Users userList, UserData userData, DataResultSet drset)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  758 */       this.m_service.setCachedObject("UserData", userData);
/*  759 */       PluginFilters.filter("modifyExportedWebFilterUserInfo", this.m_workspace, this.m_binder, this.m_service);
/*  760 */       userData = (UserData)this.m_service.getCachedObject("UserData");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  764 */       Report.warning("system", null, e);
/*      */     }
/*      */ 
/*  767 */     Vector v = drset.createEmptyRow();
/*  768 */     String user = userData.m_name;
/*  769 */     String pswrd = userData.getProperty("dPassword");
/*  770 */     String pswrdencoding = userData.getProperty("dPasswordEncoding");
/*  771 */     if (pswrd == null)
/*      */     {
/*  773 */       pswrd = "";
/*      */     }
/*  775 */     String rolesStr = SecurityUtils.getRolePackagedList(userData);
/*  776 */     String accountsStr = SecurityUtils.getFullExportedAccountslist(userData);
/*      */ 
/*  780 */     String defEncoding = userList.getDefaultPasswordEncoding();
/*  781 */     if ((defEncoding.equals("SHA1-CB") == true) && (!pswrdencoding.equals("SHA1-CB")))
/*      */     {
/*  784 */       pswrd = UserUtils.encodePassword(user, pswrd, defEncoding);
/*  785 */       pswrdencoding = defEncoding;
/*      */     }
/*      */ 
/*  788 */     v.setElementAt(user, 0);
/*  789 */     v.setElementAt(pswrd, 1);
/*  790 */     v.setElementAt(pswrdencoding, 2);
/*  791 */     v.setElementAt(rolesStr, 3);
/*  792 */     v.setElementAt(accountsStr, 4);
/*      */ 
/*  795 */     String extendedInfo = UserUtils.createExtendedInfoString(userData);
/*  796 */     v.setElementAt(extendedInfo, 5);
/*  797 */     drset.addRow(v);
/*      */   }
/*      */ 
/*      */   protected void moveCache(String fromName, String toName, boolean force) throws ServiceException
/*      */   {
/*  802 */     File fromFile = FileUtilsCfgBuilder.getCfgFile(fromName, null, false);
/*  803 */     File toFile = FileUtilsCfgBuilder.getCfgFile(toName, null, false);
/*  804 */     boolean toExists = toFile.exists();
/*  805 */     boolean fromExists = fromFile.exists();
/*      */ 
/*  807 */     String errMsg = null;
/*  808 */     if (!fromExists)
/*      */     {
/*  810 */       if (!force)
/*      */       {
/*  812 */         return;
/*      */       }
/*  814 */       errMsg = LocaleUtils.encodeMessage("csUnableToRenameUserCache", null, fromName, toName);
/*      */     }
/*      */ 
/*  817 */     if ((errMsg == null) && (toExists == true))
/*      */     {
/*  819 */       toFile.delete();
/*      */     }
/*      */ 
/*  822 */     if ((errMsg == null) && (!fromFile.renameTo(toFile)))
/*      */     {
/*  824 */       errMsg = LocaleUtils.encodeMessage("csUnableToRenameUserCache", null, fromName, toName);
/*      */     }
/*      */ 
/*  827 */     if (errMsg == null)
/*      */       return;
/*  829 */     throw new ServiceException(errMsg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setPublicGroupPermissions()
/*      */     throws ServiceException, DataException
/*      */   {
/*  836 */     String priv = this.m_binder.get("dPrivilege");
/*  837 */     if (!priv.equals("0"))
/*      */       return;
/*  839 */     this.m_binder.putLocal("dGroupName", "Public");
/*  840 */     this.m_binder.putLocal("dPrivilege", "1");
/*  841 */     WorkspaceUtils.getWorkspace("user").execute("UroleDefinition", this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserPermissions()
/*      */     throws ServiceException, DataException
/*      */   {
/*  848 */     UserData userData = this.m_service.getUserData();
/*  849 */     SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
/*      */ 
/*  851 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*      */ 
/*  854 */     DataResultSet securityGroups = new DataResultSet(new String[] { "dGroupName", "privilege" });
/*  855 */     SchemaViewData svd = (SchemaViewData)views.getData("SecurityGroups");
/*  856 */     String internalColumn = svd.get("schInternalColumn");
/*  857 */     SchemaSecurityFilter securityFilter = schemaUtils.getSecurityImplementor(svd, this.m_service);
/*  858 */     ResultSet rset = svd.getAllViewValuesWithFilter(securityFilter);
/*  859 */     int internalIndex = ResultSetUtils.getIndexMustExist(rset, internalColumn);
/*  860 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  862 */       String groupName = rset.getStringValue(internalIndex);
/*  863 */       int priv = SecurityUtils.determineGroupPrivilege(userData, groupName);
/*  864 */       Vector v = new IdcVector();
/*  865 */       v.add(groupName);
/*  866 */       v.add("" + priv);
/*  867 */       securityGroups.addRow(v);
/*      */     }
/*  869 */     this.m_binder.addResultSet("SecurityGroups", securityGroups);
/*      */ 
/*  872 */     if (SharedObjects.getEnvValueAsBoolean("UseAccounts", false))
/*      */     {
/*  874 */       DataResultSet accounts = new DataResultSet(new String[] { "dDocAccount", "privilege" });
/*  875 */       Vector allowedAccountsData = userData.getAttributes("account");
/*  876 */       if (allowedAccountsData == null)
/*      */       {
/*  878 */         allowedAccountsData = new Vector();
/*  879 */         SecurityUtils.addDefaultAccounts(userData, allowedAccountsData);
/*      */       }
/*      */ 
/*  882 */       for (UserAttribInfo info : allowedAccountsData)
/*      */       {
/*  884 */         Vector v = new IdcVector();
/*  885 */         v.add(info.m_attribName);
/*  886 */         v.add("" + info.m_attribPrivilege);
/*  887 */         accounts.addRow(v);
/*      */       }
/*      */ 
/*  890 */       this.m_binder.addResultSet("DocumentAccounts", accounts);
/*      */     }
/*      */ 
/*  894 */     DataResultSet flags = new DataResultSet(new String[] { "flag", "value" });
/*  895 */     String[] conditionVarList = { "IsAdmin", "AdminAtLeastOneGroup", "IsSubAdmin", "IsSysManager", "IsContributor", "ActAsAnonymous" };
/*  896 */     for (String conditionVar : conditionVarList)
/*      */     {
/*  898 */       Vector v = new IdcVector();
/*  899 */       v.add(conditionVar);
/*  900 */       v.add(this.m_service.m_conditionVars.get(conditionVar).toString());
/*  901 */       flags.addRow(v);
/*      */     }
/*  903 */     this.m_binder.addResultSet("UserSecurityFlags", flags);
/*      */     try
/*      */     {
/*  910 */       PluginFilters.filter("postGetUserPermissions", this.m_service.getWorkspace(), this.m_binder, this.m_service);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  914 */       this.m_service.createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUsersDocProfiles()
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/*  929 */       getUsersDocProfiles(this.m_binder, this.m_service, ScriptExtensionUtils.getPageMerger(this.m_service), true);
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/*  933 */       throw new ServiceException("!csDpUnableToGetProfiles", de);
/*      */     }
/*      */   }
/*      */ 
/*      */   static void getUsersDocProfiles(DataBinder binder, ExecutionContext cxt, PageMerger pageMerger, boolean includeStandardLinks)
/*      */     throws DataException, ServiceException
/*      */   {
/*  947 */     TopicInfo topicInfo = UserProfileUtils.getTopicInfo(cxt, "pne_portal");
/*  948 */     DataResultSet pneDocumentProfiles = ((topicInfo == null) || (topicInfo.m_data == null)) ? null : (DataResultSet)topicInfo.m_data.getResultSet("PneDocumentProfiles");
/*      */ 
/*  962 */     String[] navColumns = { "dpOrder" };
/*  963 */     int navNum = navColumns.length;
/*  964 */     int num = DocProfileStorage.DOCPROFILE_COLUMNS.length;
/*      */ 
/*  966 */     String[] columns = new String[num + navNum];
/*  967 */     for (int i = 0; i < num + navNum; ++i)
/*      */     {
/*  969 */       if (i < num)
/*      */       {
/*  971 */         columns[i] = DocProfileStorage.DOCPROFILE_COLUMNS[i];
/*      */       }
/*      */       else
/*      */       {
/*  975 */         columns[i] = navColumns[(i - num)];
/*      */       }
/*      */     }
/*  978 */     DataResultSet pneSetCheckin = new DataResultSet(columns);
/*  979 */     DataResultSet pneSetSearch = new DataResultSet(columns);
/*      */     try
/*      */     {
/*  984 */       int checkinCount = 0;
/*  985 */       int searchCount = 0;
/*      */ 
/*  987 */       DataResultSet dpSet = SharedObjects.getTable("DocumentProfiles");
/*  988 */       if ((dpSet != null) && (!dpSet.isEmpty()))
/*      */       {
/*  990 */         int index = ResultSetUtils.getIndexMustExist(dpSet, "dpName");
/*      */ 
/*  992 */         int count = 0;
/*  993 */         DataBinder params = new DataBinder();
/*      */ 
/*  996 */         params.setLocalData(new Properties());
/*  997 */         params.addResultSet("DocumentProfiles", dpSet);
/*      */ 
/*  999 */         for (dpSet.first(); dpSet.isRowPresent(); ++count)
/*      */         {
/* 1001 */           String pName = dpSet.getStringValue(index);
/*      */ 
/* 1003 */           String label = dpSet.getStringValueByName("dpDisplayLabel");
/* 1004 */           if (label != null) {
/* 1005 */             label = LocaleResources.getString(label, cxt);
/* 1006 */             params.putLocal("dpDisplayLabel", label);
/*      */           }
/*      */ 
/* 1009 */           boolean isCheckin = true;
/* 1010 */           boolean isSearch = true;
/* 1011 */           if (pneDocumentProfiles != null)
/*      */           {
/* 1013 */             String checkinProfileLinkEnabled = ResultSetUtils.findValue(pneDocumentProfiles, "dpName", pName, "dpIsCheckin");
/*      */ 
/* 1015 */             if ((checkinProfileLinkEnabled != null) && (checkinProfileLinkEnabled.equals("false"))) isCheckin = false;
/*      */ 
/* 1017 */             String searchProfileLinkEnabled = ResultSetUtils.findValue(pneDocumentProfiles, "dpName", pName, "dpIsSearch");
/*      */ 
/* 1019 */             if ((searchProfileLinkEnabled != null) && (searchProfileLinkEnabled.equals("false"))) isSearch = false;
/*      */           }
/*      */ 
/* 1022 */           params.putLocal("dpOrder", "" + count);
/* 1023 */           params.putLocal("dpIsCheckin", "1");
/* 1024 */           params.putLocal("dpCheckinEnabled", "1");
/* 1025 */           params.putLocal("dpIsSearch", "1");
/* 1026 */           params.putLocal("dpSearchEnabled", "1");
/*      */ 
/* 1028 */           if ((isCheckin) || (isSearch))
/*      */           {
/* 1030 */             DocProfileManager.checkProfileLinks(pName, params, cxt, pageMerger, true);
/*      */           }
/*      */ 
/* 1033 */           boolean isCheckinEnabled = StringUtils.convertToBool(params.get("dpCheckinEnabled"), false);
/* 1034 */           if ((isCheckin) && (isCheckinEnabled))
/*      */           {
/* 1036 */             Vector row = pneSetCheckin.createRow(params);
/* 1037 */             pneSetCheckin.addRow(row);
/* 1038 */             ++checkinCount;
/*      */           }
/* 1040 */           boolean isSearchEnabled = StringUtils.convertToBool(params.get("dpSearchEnabled"), false);
/* 1041 */           if ((isSearch) && (isSearchEnabled))
/*      */           {
/* 1043 */             Vector row = pneSetSearch.createRow(params);
/* 1044 */             pneSetSearch.addRow(row);
/* 1045 */             ++searchCount;
/*      */           }
/*  999 */           dpSet.next();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1050 */       if (includeStandardLinks)
/*      */       {
/* 1052 */         Vector v = new Vector();
/* 1053 */         v.add("StandardCheckIn");
/* 1054 */         v.add(LocaleResources.getString("wwStandardCheckIn", cxt));
/* 1055 */         v.add("");
/* 1056 */         v.add(LocaleResources.getString("wwStandardCheckIn", cxt));
/* 1057 */         v.add("Base");
/* 1058 */         v.add(Integer.toString(pneSetCheckin.getNumRows()));
/* 1059 */         pneSetCheckin.addRow(v);
/*      */ 
/* 1061 */         v = new Vector();
/* 1062 */         v.add("StandardSearch");
/* 1063 */         v.add(LocaleResources.getString("wwStandardSearch", cxt));
/* 1064 */         v.add("");
/* 1065 */         v.add(LocaleResources.getString("wwStandardSearch", cxt));
/* 1066 */         v.add("Base");
/* 1067 */         v.add(Integer.toString(pneSetSearch.getNumRows()));
/* 1068 */         pneSetSearch.addRow(v);
/*      */       }
/*      */ 
/* 1071 */       binder.putLocal("enabledCheckinCount", "" + checkinCount);
/* 1072 */       binder.putLocal("enabledSearchCount", "" + searchCount);
/* 1073 */       binder.addResultSet("DocumentProfilesForUserCheckin", pneSetCheckin);
/* 1074 */       binder.addResultSet("DocumentProfilesForUserSearch", pneSetSearch);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1078 */       Report.trace(null, "The DocumentProfiles table 'DocumentProfilesForUser' is badly defined. " + e.getMessage(), null);
/*      */ 
/* 1080 */       throw e;
/*      */     }
/*      */ 
/* 1083 */     PluginFilters.filter("postUsersDocProfiles", null, binder, cxt);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadTimeZoneList() throws ServiceException, DataException
/*      */   {
/* 1089 */     SharedLocalizationHandler slh = SharedLocalizationHandlerFactory.createInstance();
/* 1090 */     DataResultSet tz = slh.getTimeZones(this.m_service);
/* 1091 */     slh.prepareTimeZonesForDisplay(tz, this.m_service, 3);
/* 1092 */     this.m_binder.addResultSet("TimeZones", tz);
/*      */ 
/* 1095 */     TimeZone currentTimeZone = (TimeZone)this.m_service.getCachedObject("UserTimeZone");
/* 1096 */     if (currentTimeZone == null)
/*      */     {
/* 1098 */       currentTimeZone = LocaleResources.getSystemTimeZone();
/*      */     }
/* 1100 */     this.m_binder.putLocal("UserTimeZone", currentTimeZone.getID());
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserCookie() throws ServiceException
/*      */   {
/* 1106 */     int redirectCount = NumberUtils.parseInteger(this.m_binder.getLocal("RedirectCount"), 0);
/*      */ 
/* 1108 */     String cookie = this.m_binder.getEnvironmentValue("HTTP_COOKIE");
/*      */ 
/* 1111 */     String sessionKey = this.m_binder.getEnvironmentValue("IDCSESSIONKEY");
/*      */ 
/* 1114 */     String sessionValue = this.m_binder.getEnvironmentValue("IDCSESSIONVALUE");
/*      */ 
/* 1116 */     if ((sessionValue == null) && (sessionKey != null) && (sessionKey.length() > 0) && (cookie != null))
/*      */     {
/* 1118 */       sessionValue = DataSerializeUtils.parseCookie(cookie, sessionKey);
/*      */     }
/*      */ 
/* 1121 */     if (sessionValue != null)
/*      */     {
/* 1123 */       this.m_binder.putLocal("SESSIONCOOKIENAME", sessionKey);
/* 1124 */       this.m_binder.putLocal(sessionKey, sessionValue);
/*      */     }
/* 1128 */     else if (redirectCount < 5)
/*      */     {
/* 1131 */       redirectCount += 1;
/* 1132 */       this.m_binder.putLocal("RedirectCount", Integer.toString(redirectCount));
/* 1133 */       this.m_binder.putLocal("RedirectParams", "IdcService=GET_USER_COOKIE&RedirectCount=" + redirectCount);
/*      */     }
/*      */ 
/* 1145 */     String contentType = "application/json;charset=UTF-8";
/* 1146 */     this.m_binder.setContentType(contentType);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1151 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101194 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserServiceHandler
 * JD-Core Version:    0.5.4
 */