/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.TimeZoneFormat;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RoleDefinitions;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserAttribInfo;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class UserService extends Service
/*      */ {
/*      */   public UserData m_targetUserData;
/*      */   protected Workspace m_userWorkspace;
/*      */ 
/*      */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData srvice)
/*      */     throws DataException
/*      */   {
/*   46 */     super.init(ws, output, binder, srvice);
/*   47 */     this.m_userWorkspace = WorkspaceUtils.getWorkspace("user");
/*   48 */     if (this.m_userWorkspace == this.m_workspace)
/*      */       return;
/*   50 */     super.addWorkspace(this.m_userWorkspace);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*   58 */     super.createHandlersForService();
/*   59 */     createHandlers("UserService");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postActions()
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*   69 */       super.postActions();
/*      */ 
/*   72 */       Vector subjects = this.m_serviceData.m_subjects;
/*   73 */       int size = subjects.size();
/*   74 */       for (int i = 0; i < size; ++i)
/*      */       {
/*   76 */         String subject = (String)subjects.elementAt(i);
/*   77 */         if (subject.equals("users")) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*   85 */       createServiceException(e, "!csUnableToBuildUserDatabase");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void remoteCredentialsCheck()
/*      */     throws DataException, ServiceException
/*      */   {
/*   96 */     this.m_useOdbcFormat = true;
/*      */ 
/*  101 */     String user = this.m_binder.getLocal("userName");
/*  102 */     boolean isAnonymousUser = false;
/*  103 */     boolean skipAuthorizationCheck = false;
/*  104 */     if ((user == null) || (user.length() == 0))
/*      */     {
/*  106 */       isAnonymousUser = true;
/*  107 */       skipAuthorizationCheck = !SharedObjects.getEnvValueAsBoolean("ApplyExtendedSecurityToAnonymousUser", false);
/*  108 */       user = "anonymous";
/*      */     }
/*  110 */     else if (user.equals("anonymous"))
/*      */     {
/*  115 */       isAnonymousUser = true;
/*      */     }
/*      */ 
/*  120 */     this.m_binder.putLocal("originalUser", user);
/*  121 */     if (skipAuthorizationCheck)
/*      */     {
/*  123 */       this.m_binder.putLocal("skipAuthorizationCheck", "1");
/*      */     }
/*  125 */     this.m_binder.putLocal("isClientCredentialsCheck", "1");
/*      */ 
/*  128 */     boolean isPathProxiedRelativeToUser = isConditionVarTrue("IsProxiedRequest");
/*  129 */     int index = user.indexOf(47);
/*  130 */     if (index > 0)
/*      */     {
/*  132 */       String relativeRoot = "/" + user.substring(0, index + 1);
/*  133 */       if (relativeRoot.equalsIgnoreCase(DocumentPathBuilder.getRelativeWebRoot()))
/*      */       {
/*  135 */         this.m_binder.putLocal("isProxyLocalLogin", "1");
/*  136 */         user = user.substring(index + 1);
/*  137 */         isPathProxiedRelativeToUser = false;
/*      */       }
/*      */     }
/*  140 */     if (isPathProxiedRelativeToUser)
/*      */     {
/*  142 */       this.m_binder.putLocal("isPathProxiedRelativeToUser", "1");
/*      */     }
/*      */ 
/*  146 */     this.m_binder.putLocal("dName", user);
/*  147 */     this.m_binder.putLocal("dUser", user);
/*      */ 
/*  150 */     boolean checkPath = StringUtils.convertToBool(this.m_binder.getLocal("checkPath"), false);
/*  151 */     boolean authenticateUser = StringUtils.convertToBool(this.m_binder.getLocal("authenticateUser"), false);
/*  152 */     boolean getUserInfo = StringUtils.convertToBool(this.m_binder.getLocal("getUserInfo"), false);
/*  153 */     boolean hasSecurityInfo = StringUtils.convertToBool(this.m_binder.getLocal("hasSecurityInfo"), false);
/*  154 */     if ((getUserInfo) && (isAnonymousUser))
/*      */     {
/*  156 */       createServiceException(null, "!csUsernameNotPassed");
/*      */     }
/*      */ 
/*  159 */     String fileUrl = this.m_binder.getLocal("fileUrl");
/*      */     try
/*      */     {
/*  163 */       retrieveUserDatabaseProfileDataEx(user, !hasSecurityInfo, false);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  167 */       if (e.m_errorCode == -21)
/*      */       {
/*  169 */         this.m_binder.putLocal("isPromptLogin", "1");
/*  170 */         return;
/*      */       }
/*  172 */       throw e;
/*      */     }
/*      */ 
/*  175 */     String authType = this.m_targetUserData.getProperty("dUserAuthType");
/*  176 */     boolean userNotFound = (authType == null) && (!isAnonymousUser);
/*  177 */     if (userNotFound)
/*      */     {
/*  179 */       this.m_targetUserData.setProperty("dUserAuthType", "EXTERNAL");
/*      */     }
/*      */ 
/*  191 */     boolean useLocallyLoadedAttributes = isAnonymousUser;
/*  192 */     if (!isAnonymousUser)
/*      */     {
/*  194 */       boolean mustUsePassedInParametersForUserAttributes = (hasSecurityInfo) || ((isPathProxiedRelativeToUser) && (checkPath));
/*  195 */       if (!mustUsePassedInParametersForUserAttributes)
/*      */       {
/*  199 */         useLocallyLoadedAttributes = StringUtils.convertToBool(this.m_binder.getLocal("loadedUserAttributes"), false);
/*      */       }
/*  201 */       if ((userNotFound) && (authenticateUser))
/*      */       {
/*  203 */         this.m_binder.putLocal("isPromptLogin", "1");
/*  204 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  208 */     if ((userNotFound) || (!useLocallyLoadedAttributes) || (SharedObjects.getEnvValueAsBoolean("AllowServerCredentialsOverride", false)))
/*      */     {
/*  212 */       this.m_targetUserData.checkCreateAttributes(false);
/*  213 */       this.m_targetUserData.m_hasAttributesLoaded = true;
/*      */ 
/*  215 */       String[][] attribVars = { { "userRoles", "role" }, { "userAccounts", "account" } };
/*      */ 
/*  217 */       for (int i = 0; i < attribVars.length; ++i)
/*      */       {
/*  219 */         String attribName = attribVars[i][1];
/*  220 */         String externalAttribs = this.m_binder.getLocal(attribVars[i][0]);
/*  221 */         if ((externalAttribs == null) || (externalAttribs.length() <= 0))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  226 */         if (DataBinderUtils.getBoolean(this.m_binder, "RemoveAttributesBeforeLoadingExternalAttributes", false))
/*      */         {
/*  228 */           this.m_targetUserData.removeAttributes(attribName);
/*      */         }
/*  230 */         loadExternalSecurityAttributes(this.m_targetUserData, attribName, externalAttribs);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  237 */     DataBinder inBinder = this.m_binder.createShallowCopy();
/*  238 */     String doStackTrace = this.m_binder.getLocal("doStackTrace");
/*  239 */     this.m_binder.clearResultSets();
/*  240 */     this.m_binder.setLocalData(new Properties());
/*      */ 
/*  243 */     this.m_binder.putLocal("dName", user);
/*  244 */     this.m_binder.putLocal("dUser", user);
/*      */ 
/*  246 */     if (doStackTrace != null)
/*      */     {
/*  248 */       this.m_binder.putLocal("doStackTrace", doStackTrace);
/*      */     }
/*      */ 
/*  252 */     this.m_binder.putLocal("IdcService", inBinder.getLocal("IdcService"));
/*  253 */     boolean noHttpHeaders = StringUtils.convertToBool(inBinder.getLocal("NoHttpHeaders"), false);
/*  254 */     if (noHttpHeaders)
/*      */     {
/*  256 */       this.m_binder.putLocal("NoHttpHeaders", "1");
/*      */     }
/*      */ 
/*  260 */     this.m_binder.putLocal("StatusCode", "0");
/*      */ 
/*  262 */     Users users = (Users)SharedObjects.getTable("Users");
/*      */ 
/*  265 */     if (authenticateUser)
/*      */     {
/*  267 */       boolean hasAuthenticated = StringUtils.convertToBool(inBinder.getLocal("hasAuthenticatedUser"), false);
/*      */ 
/*  269 */       String password = inBinder.getLocal("userPassword");
/*  270 */       if (password == null)
/*      */       {
/*  272 */         createServiceException(null, "!csPasswordNotProvided");
/*      */       }
/*      */ 
/*  275 */       String userAuthType = this.m_targetUserData.getProperty("dUserAuthType");
/*  276 */       boolean userHasValidPassword = (userAuthType != null) && (((userAuthType.equalsIgnoreCase("LOCAL")) || (userAuthType.equalsIgnoreCase("GLOBAL"))));
/*      */ 
/*  279 */       if ((hasAuthenticated) || ((userHasValidPassword) && (users.checkUserPassword(this.m_targetUserData, password))))
/*      */       {
/*  281 */         this.m_binder.putLocal("isAuthenticated", "1");
/*      */       }
/*      */       else
/*      */       {
/*  286 */         this.m_binder.putLocal("isPromptLogin", "1");
/*      */ 
/*  290 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  294 */     if ((userNotFound) || (getUserInfo))
/*      */     {
/*  300 */       String extendedInfo = inBinder.getLocal("userExtendedInfo");
/*  301 */       if ((extendedInfo != null) && (extendedInfo.length() > 0))
/*      */       {
/*  315 */         UserUtils.unpackageExtendedInfo(extendedInfo, inBinder);
/*  316 */         UserStorage.storeUserDatabaseProfileData(this.m_targetUserData, inBinder, this.m_workspace, this);
/*      */ 
/*  319 */         setCachedObject("doStoreUser", "");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  324 */     user = this.m_targetUserData.m_name;
/*  325 */     this.m_binder.putLocal("dName", user);
/*  326 */     this.m_binder.putLocal("dUser", user);
/*  327 */     inBinder.putLocal("dName", user);
/*  328 */     inBinder.putLocal("dUser", user);
/*      */ 
/*  333 */     UserData userDataCopy = UserUtils.createUserData();
/*  334 */     userDataCopy.copyUserProfile(this.m_targetUserData);
/*  335 */     userDataCopy.copyAttributes(this.m_targetUserData);
/*      */ 
/*  337 */     if (checkPath)
/*      */     {
/*  340 */       Properties props = new Properties();
/*  341 */       if (fileUrl == null)
/*      */       {
/*  343 */         throw new DataException("!csAuthenticationCheckPathMissing");
/*      */       }
/*      */ 
/*  348 */       boolean isDone = false;
/*  349 */       boolean isAuthorized = false;
/*  350 */       if (!LegacyDocumentPathUtils.parseDocInfoFromPath(fileUrl, props, this))
/*      */       {
/*  352 */         isDone = true;
/*      */       }
/*      */ 
/*  355 */       DataResultSet drset = null;
/*  356 */       if (!isDone)
/*      */       {
/*  358 */         drset = this.m_fileUtils.createFileReference(props, this.m_binder, this.m_workspace, this, true);
/*  359 */         if (drset == null)
/*      */         {
/*  364 */           boolean isStandardAuth = DataBinderUtils.getLocalBoolean(inBinder, "isStandardAuth", false);
/*  365 */           skipAuthorizationCheck = SharedObjects.getEnvValueAsBoolean("AllowAccessToUnknownFileUsingStandardModel", isStandardAuth);
/*  366 */           isDone = true;
/*      */         }
/*      */         else
/*      */         {
/*  370 */           if (skipAuthorizationCheck)
/*      */           {
/*  372 */             String securityGroup = drset.getStringValueByName("dSecurityGroup");
/*  373 */             DocumentAccessSecurity docSecurity = this.m_securityImpl.getDocumentAccessSecurity();
/*  374 */             isDone = !docSecurity.isSpecialAuthGroup(securityGroup);
/*      */           }
/*      */ 
/*  377 */           setCachedObject("isJdbcStoredFileCopied", "1");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  382 */       if (!isDone)
/*      */       {
/*  384 */         setCachedObject("isAuthorized", "");
/*      */ 
/*  388 */         isAuthorized = true;
/*      */ 
/*  392 */         UserData oldUserData = this.m_userData;
/*      */         try
/*      */         {
/*  395 */           if ((useLocallyLoadedAttributes) || (SharedObjects.getEnvValueAsBoolean("AlwaysCallAlterUserCredentialsForPathCheck", false)) || (DataBinderUtils.getLocalBoolean(this.m_binder, "allowAlterCredentials", false)))
/*      */           {
/*  404 */             setCachedObject("alterUserCredentials:isCheckPath", Boolean.TRUE);
/*  405 */             PluginFilters.filter("alterUserCredentials", getWorkspace(), this.m_binder, this);
/*      */           }
/*      */ 
/*  408 */           setCachedObject("UserData", this.m_targetUserData);
/*  409 */           this.m_userData = this.m_targetUserData;
/*  410 */           if (PluginFilters.filter("docUrlAllowAccess", getWorkspace(), this.m_binder, this) != 0)
/*      */           {
/*  413 */             isDone = true;
/*      */           }
/*  415 */           Object result = getCachedObject("isAuthorized");
/*  416 */           if ((result != null) && (result instanceof Boolean))
/*      */           {
/*  418 */             isAuthorized = ScriptUtils.getBooleanVal(result);
/*  419 */             if (!isAuthorized)
/*      */             {
/*  421 */               isDone = true;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  426 */           if (!isDone)
/*      */           {
/*  428 */             isAuthorized = checkAccess(this.m_binder, drset, 1);
/*      */           }
/*      */ 
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*  437 */           if (oldUserData != null)
/*      */           {
/*  439 */             setCachedObject("UserData", oldUserData);
/*  440 */             this.m_userData = oldUserData;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  445 */       if (!skipAuthorizationCheck)
/*      */       {
/*  447 */         if (isAuthorized)
/*      */         {
/*  449 */           this.m_binder.putLocal("isAuthorized", "1");
/*      */         }
/*      */         else
/*      */         {
/*  453 */           this.m_binder.putLocal("isAccessDenied", "1");
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  458 */     if ((!getUserInfo) || (userNotFound)) {
/*      */       return;
/*      */     }
/*  461 */     if (useLocallyLoadedAttributes)
/*      */     {
/*  467 */       loadReturnProperty("dUserOrgPath", userDataCopy, inBinder);
/*  468 */       loadReturnProperty("dUserSourceOrgPath", userDataCopy, inBinder);
/*  469 */       String accounts = SecurityUtils.getFullExportedAccountslist(userDataCopy);
/*  470 */       this.m_binder.putLocal("accounts", accounts);
/*  471 */       String roles = SecurityUtils.getRolePackagedList(userDataCopy);
/*  472 */       this.m_binder.putLocal("roles", roles);
/*  473 */       this.m_binder.putLocal("hasSecurityInfo", "1");
/*      */     }
/*      */ 
/*  477 */     String extendedInfo = UserUtils.createExtendedInfoString(userDataCopy);
/*  478 */     this.m_binder.putLocal("extendedInfo", extendedInfo);
/*      */   }
/*      */ 
/*      */   protected void loadExternalSecurityAttributes(UserData userData, String attribName, String attribList)
/*      */   {
/*  485 */     RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*      */ 
/*  487 */     Vector attribs = new IdcVector();
/*  488 */     attribs = StringUtils.parseArray(attribList, ',', '^');
/*      */ 
/*  490 */     SecurityUtils.loadExternalSecurityAttributes(userData, attribName, attribs, roleDefs, false);
/*      */   }
/*      */ 
/*      */   protected void loadReturnProperty(String key, UserData userData, DataBinder inBinder)
/*      */   {
/*  495 */     String val = null;
/*  496 */     if (inBinder != null)
/*      */     {
/*  498 */       val = inBinder.getLocal(key);
/*      */     }
/*  500 */     if (val == null)
/*      */     {
/*  502 */       val = userData.getProperty(key);
/*      */     }
/*  504 */     if (val == null)
/*      */       return;
/*  506 */     this.m_binder.putLocal(key, val);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void retrieveUserDatabaseProfileData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  513 */     String user = this.m_binder.getLocal("dName");
/*  514 */     retrieveUserDatabaseProfileDataEx(user, true, true);
/*      */   }
/*      */ 
/*      */   public void retrieveUserDatabaseProfileDataEx(String user, boolean loadSecurityInfo, boolean isOtherUser) throws DataException, ServiceException
/*      */   {
/*  519 */     DataBinder credentialData = (isOtherUser) ? null : this.m_binder;
/*  520 */     if ((user == null) || (user.length() == 0) || (user.equalsIgnoreCase("anonymous")))
/*      */     {
/*  522 */       this.m_targetUserData = UserUtils.createUserData("anonymous");
/*  523 */       this.m_targetUserData.checkCreateAttributes(false);
/*      */     }
/*      */     else
/*      */     {
/*  527 */       this.m_targetUserData = UserStorage.retrieveUserDatabaseProfileDataFull(user, this.m_workspace, credentialData, this, loadSecurityInfo, true);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadUserMustExist()
/*      */     throws DataException, ServiceException
/*      */   {
/*  535 */     if (this.m_targetUserData != null)
/*      */     {
/*  537 */       return;
/*      */     }
/*  539 */     retrieveUserDatabaseProfileData();
/*      */ 
/*  541 */     boolean isFromDb = true;
/*  542 */     if (this.m_targetUserData != null)
/*      */     {
/*  544 */       String userDataFromDb = this.m_targetUserData.getProperty("userDataFromDb");
/*  545 */       isFromDb = StringUtils.convertToBool(userDataFromDb, true);
/*      */     }
/*      */ 
/*  548 */     if ((this.m_targetUserData != null) && (isFromDb))
/*      */       return;
/*  550 */     String msg = LocaleUtils.encodeMessage("csUserCannotBeRetrieved", null, this.m_binder.getLocal("dName"));
/*      */ 
/*  552 */     throw new ServiceException(msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateUserLocale()
/*      */     throws ServiceException, DataException
/*      */   {
/*  559 */     if (this.m_targetUserData == null)
/*      */     {
/*  561 */       return;
/*      */     }
/*  563 */     IdcLocale locale = null;
/*  564 */     String userLocale = this.m_targetUserData.getProperty("dUserLocale");
/*  565 */     if ((userLocale != null) && (userLocale.length() > 0))
/*      */     {
/*  567 */       locale = LocaleResources.getLocale(userLocale);
/*  568 */       if (locale == null)
/*      */       {
/*  570 */         String msg = LocaleUtils.encodeMessage("csLocaleNotFound", null, userLocale);
/*      */ 
/*  572 */         throw new ServiceException(msg);
/*      */       }
/*      */ 
/*      */     }
/*  578 */     else if (DataBinderUtils.getBoolean(this.m_binder, "dUserLocale:override", false))
/*      */     {
/*  580 */       String msg = LocaleUtils.encodeMessage("csUnableToOverrideWithNullLocale", null, null);
/*  581 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  585 */     String userTimeZone = this.m_targetUserData.getProperty("dUserTimeZone");
/*  586 */     TimeZone timeZone = null;
/*  587 */     if ((userTimeZone != null) && (userTimeZone.length() > 0))
/*      */     {
/*  590 */       boolean isLocaleNull = false;
/*  591 */       if (locale == null)
/*      */       {
/*  593 */         isLocaleNull = true;
/*  594 */         UserData userData = null;
/*  595 */         String userName = this.m_targetUserData.getProperty("dName");
/*      */         try
/*      */         {
/*  598 */           userData = UserStorage.retrieveUserDatabaseProfileData(userName, this.m_workspace, this);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  602 */           String msg = LocaleUtils.encodeMessage("csDpUnableToRetreiveUser", null, userName);
/*  603 */           createServiceException(e, msg);
/*      */         }
/*  605 */         if (userData != null)
/*      */         {
/*  607 */           String localeName = userData.getProperty("dUserLocale");
/*  608 */           if ((localeName != null) && (!localeName.isEmpty()))
/*      */           {
/*  610 */             locale = LocaleResources.getLocale(localeName);
/*      */           }
/*      */         }
/*  613 */         if (locale == null)
/*      */         {
/*  615 */           locale = LocaleResources.getLocale("SystemLocale");
/*      */         }
/*      */       }
/*  618 */       timeZone = locale.m_tzFormat.parseTimeZone(null, userTimeZone, 0);
/*  619 */       if (isLocaleNull)
/*      */       {
/*  621 */         locale = null;
/*      */       }
/*  623 */       if (timeZone == null)
/*      */       {
/*  625 */         String msg = LocaleUtils.encodeMessage("csUnableToFindTimeZone", null, userTimeZone);
/*      */ 
/*  627 */         throw new ServiceException(msg);
/*      */       }
/*      */     }
/*  630 */     if (locale != null)
/*      */     {
/*  632 */       setCachedObject("UserLocale", locale);
/*      */     }
/*      */ 
/*  635 */     if (timeZone != null)
/*      */     {
/*  637 */       setCachedObject("UserTimeZone", timeZone);
/*      */     }
/*      */ 
/*  640 */     this.m_httpImplementor.setUpdateLocale(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void storeUserDatabaseProfileData() throws DataException, ServiceException
/*      */   {
/*  646 */     validateUserMetaData();
/*      */ 
/*  648 */     boolean isAdd = StringUtils.convertToBool(this.m_binder.getLocal("isAdd"), false);
/*  649 */     if ((isAdd) && (this.m_targetUserData != null))
/*      */     {
/*  652 */       String curAuthType = this.m_targetUserData.getProperty("dUserAuthType");
/*  653 */       boolean curIsOwnedByServer = (curAuthType != null) && (((curAuthType.equalsIgnoreCase("LOCAL")) || (curAuthType.equalsIgnoreCase("GLOBAL"))));
/*      */ 
/*  655 */       if (curIsOwnedByServer)
/*      */       {
/*  657 */         createServiceException(null, "!csFullyDefinedUserAlreadyExists");
/*      */       }
/*  659 */       String name = this.m_binder.getLocal("dName");
/*  660 */       String curName = this.m_targetUserData.m_name;
/*  661 */       if (curName.equalsIgnoreCase(name))
/*      */       {
/*  663 */         this.m_binder.putLocal("dName", curName);
/*  664 */         this.m_userWorkspace.execute("Duser", this.m_binder);
/*  665 */         this.m_binder.putLocal("dName", name);
/*      */       }
/*      */     }
/*  668 */     String editUserAuthType = this.m_binder.getLocal("dUserAuthType");
/*  669 */     boolean isLocal = false;
/*  670 */     boolean isGlobal = false;
/*  671 */     if (editUserAuthType != null)
/*      */     {
/*  673 */       if (editUserAuthType.equalsIgnoreCase("LOCAL"))
/*      */       {
/*  675 */         isLocal = true;
/*      */       }
/*  677 */       else if (editUserAuthType.equalsIgnoreCase("GLOBAL"))
/*      */       {
/*  679 */         isGlobal = true;
/*      */       }
/*      */     }
/*  682 */     if ((isLocal) || (isGlobal))
/*      */     {
/*  684 */       this.m_binder.putLocal("dUserSourceOrgPath", "");
/*      */     }
/*  686 */     if (isLocal)
/*      */     {
/*  688 */       this.m_binder.putLocal("dUserOrgPath", "");
/*      */     }
/*      */ 
/*  691 */     UserStorage.storeUserDatabaseProfileData(this.m_targetUserData, this.m_binder, this.m_workspace, this);
/*      */   }
/*      */ 
/*      */   protected void validateUserMetaData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  699 */     DataResultSet userMetaRset = SharedObjects.getTable("UserMetaDefinition");
/*      */ 
/*  702 */     int typeIndex = ResultSetUtils.getIndexMustExist(userMetaRset, "umdType");
/*  703 */     int nameIndex = ResultSetUtils.getIndexMustExist(userMetaRset, "umdName");
/*      */ 
/*  705 */     for (userMetaRset.first(); userMetaRset.isRowPresent(); userMetaRset.next())
/*      */     {
/*  707 */       Vector row = userMetaRset.getCurrentRowValues();
/*  708 */       String type = (String)row.elementAt(typeIndex);
/*  709 */       String name = (String)row.elementAt(nameIndex);
/*      */ 
/*  711 */       if (!type.equals("Date"))
/*      */         continue;
/*  713 */       this.m_binder.setFieldType(name, "date");
/*      */     }
/*      */ 
/*  717 */     setCachedObject("TargetUserData", this.m_targetUserData);
/*  718 */     PluginFilters.filter("validateUserMetaData", getWorkspace(), this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void hidePassword() throws DataException, ServiceException
/*      */   {
/*  724 */     String dashes = Users.getPasswordDash();
/*  725 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/*  727 */       String drsetName = this.m_currentAction.getParamAt(0);
/*  728 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(drsetName);
/*  729 */       if (drset != null)
/*      */       {
/*  731 */         int colIndex = ResultSetUtils.getIndexMustExist(drset, "dPassword");
/*  732 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  734 */           drset.setCurrentValue(colIndex, dashes);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  740 */       drset.first();
/*      */     }
/*      */     else
/*      */     {
/*  744 */       this.m_binder.putLocal("dPassword", dashes);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void filterUpdateData()
/*      */     throws DataException
/*      */   {
/*  752 */     boolean isProxiedUser = isConditionVarTrue("IsProxiedUser");
/*  753 */     if (isProxiedUser)
/*      */     {
/*  755 */       this.m_binder.putLocal("dPassword", Users.getPasswordDash());
/*      */     }
/*      */ 
/*  759 */     String resultCountString = this.m_binder.getLocal("ResultCount");
/*  760 */     int resultCount = NumberUtils.parseInteger(resultCountString, 20);
/*  761 */     int originalResultCount = resultCount;
/*  762 */     resultCount = validateResultCount(resultCount);
/*      */ 
/*  764 */     if (resultCount == originalResultCount)
/*      */       return;
/*  766 */     this.m_binder.putLocal("ResultCount", Integer.toString(resultCount));
/*      */ 
/*  770 */     String numTopicStrings = this.m_binder.getLocal("numTopics");
/*  771 */     String topicStringName = "";
/*  772 */     int numTopics = NumberUtils.parseInteger(numTopicStrings, 0);
/*  773 */     for (int curTopic = 1; curTopic <= numTopics; ++curTopic)
/*      */     {
/*  775 */       topicStringName = "topicString" + String.valueOf(curTopic);
/*  776 */       String topicValue = this.m_binder.getLocal(topicStringName);
/*      */ 
/*  778 */       if ((topicValue == null) || (topicValue.length() <= 0))
/*      */         continue;
/*  780 */       int indexOfLastResultCount = topicValue.indexOf("lastResultCount:");
/*  781 */       if (indexOfLastResultCount <= -1)
/*      */         continue;
/*  783 */       int endIndex = indexOfLastResultCount + "lastResultCount:".length();
/*  784 */       topicValue = topicValue.substring(0, endIndex);
/*  785 */       topicValue = topicValue + Integer.toString(resultCount);
/*  786 */       this.m_binder.putLocal(topicStringName, topicValue);
/*  787 */       return;
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkIsSelf()
/*      */     throws DataException, ServiceException
/*      */   {
/*  797 */     String userGiven = this.m_binder.get("dName");
/*  798 */     String realUser = this.m_userData.m_name;
/*  799 */     if (!userGiven.equals(realUser))
/*      */     {
/*  801 */       if (realUser.equals("anonymous"))
/*      */       {
/*  804 */         doCode("checkForceLogin");
/*      */       }
/*  806 */       throw new ServiceException("!csUserIsNotSelf");
/*      */     }
/*  808 */     this.m_targetUserData = this.m_userData;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateDelete() throws ServiceException, DataException
/*      */   {
/*  814 */     String name = this.m_binder.getLocal("dName");
/*  815 */     if ((name.equalsIgnoreCase("anonymous")) || (name.equalsIgnoreCase("sysadmin")))
/*      */     {
/*  817 */       String msg = LocaleUtils.encodeMessage("csUserCannotBeDeleted", null, name);
/*  818 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/*  821 */     PluginFilters.filter("postValidateDelete", getWorkspace(), this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateWorkflowInvolvementForUser() throws ServiceException, DataException
/*      */   {
/*  827 */     boolean doValidate = DataBinderUtils.getBoolean(this.m_binder, "validateNoWorkflows", false);
/*  828 */     if (!doValidate)
/*      */     {
/*  830 */       return;
/*      */     }
/*      */ 
/*  833 */     String name = this.m_binder.getLocal("dName");
/*  834 */     ResultSet rset = this.m_workspace.createResultSet("QworkflowsInvolvingUser", this.m_binder);
/*  835 */     if ((rset == null) || (rset.isEmpty()))
/*      */       return;
/*  837 */     DataResultSet drset = new DataResultSet();
/*  838 */     drset.copy(rset);
/*      */ 
/*  840 */     drset.first();
/*  841 */     StringBuffer workflowNames = new StringBuffer(ResultSetUtils.getValue(drset, "dWfName"));
/*  842 */     for (drset.next(); drset.isRowPresent(); drset.next())
/*      */     {
/*  844 */       String wfname = ResultSetUtils.getValue(drset, "dWfName");
/*  845 */       workflowNames.append(", " + wfname);
/*      */     }
/*      */ 
/*  848 */     this.m_binder.putLocal("StatusReason", "workflowInvolvementError:" + workflowNames.toString());
/*  849 */     String msg = LocaleUtils.encodeMessage("csUserDeleteWorkflowsInvolvement", null, name, workflowNames.toString());
/*      */ 
/*  854 */     boolean useAWA = DataBinderUtils.getBoolean(this.m_binder, "useActivityWarningAbort", false);
/*  855 */     int errorType = (useAWA) ? -67 : -1;
/*  856 */     createServiceExceptionEx(null, msg, errorType);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeCachedUser()
/*      */     throws ServiceException
/*      */   {
/*  863 */     String name = this.m_binder.getLocal("dName");
/*  864 */     UserStorage.removeCachedUserData(name);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUsers() throws ServiceException
/*      */   {
/*  870 */     Users userList = (Users)SharedObjects.getTable("Users");
/*  871 */     if (userList != null)
/*      */     {
/*  873 */       this.m_binder.addResultSet("Users", userList);
/*      */     }
/*      */     else
/*      */     {
/*  877 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "Users");
/*      */ 
/*  879 */       createServiceException(null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserInfo() throws DataException, ServiceException
/*      */   {
/*  886 */     if (isNonLocalUser())
/*      */     {
/*  888 */       executeService("GET_EXTERNAL_USER_PROFILE");
/*      */     }
/*      */     else
/*      */     {
/*  892 */       executeService("GET_USER_PROFILE");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserProfile() throws ServiceException
/*      */   {
/*  899 */     String name = this.m_binder.getLocal("dUser");
/*  900 */     this.m_binder.putLocal("dName", name);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getExternalUserProfile() throws ServiceException
/*      */   {
/*  906 */     Vector roles = this.m_userData.getAttributes("role");
/*  907 */     this.m_binder.putLocal("ExternalUserRoles", getAttributesString(roles));
/*      */ 
/*  909 */     if (!SharedObjects.getEnvValueAsBoolean("UseAccounts", false))
/*      */       return;
/*  911 */     Vector accts = this.m_userData.getAttributes("account");
/*  912 */     this.m_binder.putLocal("ExternalUserAccounts", getAttributesString(accts));
/*      */   }
/*      */ 
/*      */   public int validateResultCount(int resultCount)
/*      */   {
/*  922 */     String maxResultCountString = CommonSearchConfig.getGlobalEngineValue("MaxResultCount");
/*  923 */     int maxResultCount = NumberUtils.parseInteger(maxResultCountString, -1);
/*  924 */     if ((maxResultCount > -1) && (resultCount > maxResultCount))
/*      */     {
/*  926 */       Report.info(null, null, IdcMessageFactory.lc("csResultCountExceedsMax", new Object[0]));
/*      */ 
/*  930 */       String userName = this.m_binder.getLocal("dUser");
/*  931 */       this.m_binder.putLocal("alertId", "csResultCountExceedsMax" + userName);
/*  932 */       IdcMessage message = IdcMessageFactory.lc("csResultCountExceedsMax", new Object[0]);
/*  933 */       this.m_binder.putLocal("alertMsg", "<$lcMessage('" + LocaleUtils.encodeMessage(message) + "')$>");
/*  934 */       this.m_binder.putLocal("flags", "2");
/*  935 */       DataResultSet trigRset = new DataResultSet(AlertUtils.TRIGGER_RSET_COLS);
/*  936 */       SharedObjects.putEnvironmentValue("alertIdToBeDeletedFor" + userName, "csResultCountExceedsMax" + userName);
/*      */ 
/*  938 */       Vector trigRow = new Vector();
/*  939 */       trigRow.add(0, "user");
/*  940 */       trigRow.add(1, userName);
/*  941 */       trigRset.addRow(trigRow);
/*      */ 
/*  943 */       this.m_binder.addResultSet("AlertTriggers", trigRset);
/*      */       try
/*      */       {
/*  946 */         AlertUtils.setAlert(this.m_binder);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*      */       }
/*      */ 
/*  957 */       resultCount = maxResultCount;
/*      */     }
/*  959 */     return resultCount;
/*      */   }
/*      */ 
/*      */   protected String getAttributesString(Vector v)
/*      */   {
/*  964 */     String str = "";
/*      */ 
/*  966 */     if (v != null)
/*      */     {
/*  968 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/*  970 */         UserAttribInfo uai = (UserAttribInfo)v.elementAt(i);
/*  971 */         if (str.length() > 0)
/*      */         {
/*  973 */           str = str + ", ";
/*      */         }
/*  975 */         str = str + uai.m_attribName;
/*      */       }
/*      */     }
/*  978 */     return str;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkRefreshUsers()
/*      */     throws ServiceException, DataException
/*      */   {
/*  985 */     if (this.m_targetUserData != null)
/*      */     {
/*  987 */       String userAuthType = this.m_targetUserData.getProperty("dUserAuthType");
/*  988 */       boolean isForce = StringUtils.convertToBool(this.m_binder.getLocal("ForceUserRefresh"), false);
/*  989 */       if (((userAuthType != null) && (userAuthType.equalsIgnoreCase("LOCAL"))) || (isForce))
/*      */       {
/*  991 */         refreshUsers();
/*      */       }
/*  993 */       else if ((userAuthType != null) && (userAuthType.equalsIgnoreCase("EXTERNAL")))
/*      */       {
/*  995 */         UserStorage.refreshCachedUserData(this.m_workspace, this.m_targetUserData.m_name);
/*  996 */         if (SharedObjects.getEnvValueAsBoolean("UserCacheClusterSupport", false))
/*      */         {
/*  999 */           SubjectManager.notifyChanged("usertempcache");
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1004 */     boolean isCollaboration = SecurityUtils.m_useCollaboration;
/* 1005 */     boolean isCacheUserNames = SharedObjects.getEnvValueAsBoolean("IsCacheUserNames", true);
/* 1006 */     if ((!isCollaboration) || (!isCacheUserNames)) {
/*      */       return;
/*      */     }
/* 1009 */     UserStorage.loadUserNameCache(this.m_userWorkspace);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void refreshUsers()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1017 */     if (!SharedObjects.getEnvValueAsBoolean("IsExalogicOptimizationsEnabled", false));
/*      */     try
/*      */     {
/* 1021 */       String tableName = "Users";
/* 1022 */       Users userList = (Users)SharedObjects.getTable(tableName);
/* 1023 */       ResultSet rset = this.m_workspace.createResultSet(userList.getLocalUsersQuery(), null);
/* 1024 */       if ((rset == null) || (!rset.isRowPresent()))
/*      */       {
/* 1026 */         throw new DataException("!csUnableToFindUserList");
/*      */       }
/* 1028 */       userList.load(rset);
/* 1029 */       SharedObjects.putTable(tableName, userList);
/* 1030 */       this.m_binder.addResultSet(tableName, userList);
/*      */ 
/* 1032 */       ServiceHandler handler = (ServiceHandler)this.m_handlerMap.get("UserServiceHandler");
/* 1033 */       if (handler != null)
/*      */       {
/* 1035 */         handler.executeAction("updateCache");
/*      */       }
/* 1037 */       SubjectManager.notifyChanged("users");
/* 1038 */       this.m_binder.removeResultSet("Users");
/* 1039 */       return;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1043 */       createServiceException(e, "!csUnableToRefreshUserInformation");
/*      */ 
/* 1047 */       RefreshUsersThread.triggerRefresh(this);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void refreshRoles()
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1190 */       RoleDefinitions roleList = (RoleDefinitions)SharedObjects.getTable(RoleDefinitions.m_tableName);
/* 1191 */       ResultSet rset = this.m_userWorkspace.createResultSet(roleList.getTableName(), null);
/* 1192 */       if ((rset == null) || (!rset.isRowPresent()))
/*      */       {
/* 1194 */         throw new DataException("!csUnableToFindRoleDefinitions");
/*      */       }
/* 1196 */       roleList.load(rset);
/* 1197 */       this.m_binder.addResultSet(RoleDefinitions.m_tableName, roleList);
/* 1198 */       SharedObjects.putTable(RoleDefinitions.m_tableName, roleList);
/*      */ 
/* 1201 */       DataLoader.cacheSecurityGroupLists(this.m_userWorkspace);
/*      */ 
/* 1203 */       if (this.m_binder.m_isJava == true)
/*      */       {
/* 1205 */         DataResultSet gSet = SharedObjects.getTable("SecurityGroups");
/* 1206 */         this.m_binder.addResultSet("SecurityGroups", gSet);
/*      */       }
/*      */ 
/* 1209 */       ServiceHandler handler = (ServiceHandler)this.m_handlerMap.get("UserServiceHandler");
/* 1210 */       if (handler != null)
/*      */       {
/* 1212 */         handler.executeAction("updateCache");
/*      */       }
/*      */ 
/* 1215 */       SchemaHelper schHelper = new SchemaHelper();
/* 1216 */       schHelper.markViewCacheDirty("roles", "SecurityGroups");
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1221 */       createServiceException(e, "!csUnableToRefreshRoleInfo");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserAttributes() throws DataException, ServiceException
/*      */   {
/* 1228 */     retrieveUserAttributes();
/*      */ 
/* 1230 */     if (isConditionVarTrue("IsFilterExternalRoles"))
/*      */     {
/* 1233 */       DataResultSet sysRoles = (DataResultSet)this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/*      */ 
/* 1235 */       if (sysRoles != null)
/*      */       {
/* 1237 */         filterExternalRoles(sysRoles);
/*      */       }
/*      */     }
/* 1240 */     UserUtils.serializeAttribInfo(this.m_binder, this.m_targetUserData, true, false);
/*      */   }
/*      */ 
/*      */   public void filterExternalRoles(ResultSet systemRoles)
/*      */     throws DataException
/*      */   {
/* 1246 */     IdcVector v = new IdcVector();
/* 1247 */     IdcVector roles = (IdcVector)this.m_targetUserData.m_attributes.get("role");
/* 1248 */     if (roles == null)
/*      */       return;
/* 1250 */     for (int i = 0; i < roles.m_length; ++i)
/*      */     {
/* 1252 */       UserAttribInfo attrib = (UserAttribInfo)roles.elementAt(i);
/* 1253 */       if (ResultSetUtils.findValue(systemRoles, "dRoleName", attrib.m_attribName, "dRoleName") != null) {
/*      */         continue;
/*      */       }
/* 1256 */       v.add(attrib);
/*      */     }
/*      */ 
/* 1259 */     for (int i = 0; i < v.size(); ++i)
/*      */     {
/* 1261 */       roles.remove(v.elementAt(i));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void retrieveUserAttributes()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1268 */     loadUserMustExist();
/*      */ 
/* 1271 */     if (this.m_targetUserData.m_hasAttributesLoaded)
/*      */       return;
/* 1273 */     loadAttributeData(this.m_targetUserData);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addUserAttributes()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1280 */     String authType = this.m_binder.get("dUserAuthType");
/* 1281 */     if ((authType != null) && (authType.equalsIgnoreCase("global")))
/*      */     {
/* 1284 */       Service.checkFeatureAllowed("GlobalUsers");
/*      */     }
/*      */ 
/* 1287 */     loadUserMustExist();
/*      */ 
/* 1289 */     UserUtils.serializeAttribInfo(this.m_binder, this.m_targetUserData, false, false);
/*      */ 
/* 1292 */     if (!SecurityUtils.isUserOfRole(this.m_userData, "admin"))
/*      */     {
/* 1294 */       SecurityUtils.checkUserAttributes(this.m_userData, this.m_targetUserData);
/*      */     }
/*      */ 
/* 1298 */     Map attributes = this.m_targetUserData.getAttributesMap();
/* 1299 */     if (attributes == null)
/*      */     {
/* 1301 */       return;
/*      */     }
/*      */ 
/* 1304 */     String insertQuery = this.m_currentAction.getParamAt(0);
/*      */ 
/* 1306 */     boolean useCollaboration = SecurityUtils.m_useCollaboration;
/* 1307 */     boolean hasAddedHashNone = false;
/*      */ 
/* 1309 */     Set entrySet = attributes.entrySet();
/* 1310 */     for (Iterator entryList = entrySet.iterator(); entryList.hasNext(); )
/*      */     {
/* 1312 */       Map.Entry entry = (Map.Entry)entryList.next();
/* 1313 */       List uaiList = (List)entry.getValue();
/* 1314 */       Properties tempProps = new Properties();
/* 1315 */       PropParameters roleTestParams = new PropParameters(tempProps);
/* 1316 */       for (int i = 0; i < uaiList.size(); ++i)
/*      */       {
/* 1318 */         UserAttribInfo uai = (UserAttribInfo)uaiList.get(i);
/*      */ 
/* 1320 */         if ((uai.m_attribName.length() <= 0) || (uai.m_attribType.length() <= 0))
/*      */           continue;
/* 1322 */         if (uai.m_attribType.equals("role"))
/*      */         {
/* 1324 */           tempProps.put("dRoleName", uai.m_attribName);
/* 1325 */           ResultSet testRset = this.m_userWorkspace.createResultSet("Qrole", roleTestParams);
/* 1326 */           if (!testRset.isRowPresent())
/*      */           {
/* 1328 */             String msg = LocaleUtils.encodeMessage("csUnableToAddUserRole", null, uai.m_attribName);
/* 1329 */             createServiceException(null, msg);
/*      */           }
/*      */         }
/* 1332 */         else if ((uai.m_attribType.equals("account")) && 
/* 1336 */           (uai.m_attribName.equals("#none")) && (useCollaboration))
/*      */         {
/* 1338 */           uai.m_attribPrivilege = 15;
/* 1339 */           hasAddedHashNone = true;
/*      */         }
/*      */ 
/* 1342 */         this.m_binder.putLocal("dAttributeName", uai.m_attribName);
/* 1343 */         this.m_binder.putLocal("dAttributeType", uai.m_attribType);
/* 1344 */         this.m_binder.putLocal("dAttributePrivilege", Integer.toString(uai.m_attribPrivilege));
/*      */ 
/* 1347 */         this.m_userWorkspace.execute(insertQuery, this.m_binder);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1353 */     if ((useCollaboration) && (!hasAddedHashNone))
/*      */     {
/* 1355 */       this.m_binder.putLocal("dAttributeName", "#none");
/* 1356 */       this.m_binder.putLocal("dAttributeType", "account");
/* 1357 */       this.m_binder.putLocal("dAttributePrivilege", "15");
/*      */ 
/* 1359 */       this.m_userWorkspace.execute(insertQuery, this.m_binder);
/*      */     }
/*      */ 
/* 1362 */     PluginFilters.filter("postAddUserAttributes", getWorkspace(), this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkUserAuthType() throws DataException, ServiceException
/*      */   {
/* 1368 */     String authType = this.m_binder.get("dUserAuthType");
/* 1369 */     if (authType.equals("GLOBAL"))
/*      */     {
/* 1371 */       Service.checkFeatureAllowed("GlobalUsers");
/*      */     }
/*      */ 
/* 1374 */     String curAuthType = this.m_binder.get("curUserAuthType");
/* 1375 */     boolean isForce = (curAuthType.equalsIgnoreCase("LOCAL")) || (authType.equalsIgnoreCase("LOCAL"));
/* 1376 */     this.m_binder.putLocal("ForceUserRefresh", String.valueOf(isForce));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void insertAliasUsers() throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1384 */       String usersString = this.m_binder.getLocal("AliasUsersString");
/* 1385 */       Vector selectedUsers = StringUtils.parseArray(usersString, '\n', '\n');
/*      */ 
/* 1387 */       for (int i = 0; i < selectedUsers.size(); ++i)
/*      */       {
/* 1389 */         String name = (String)selectedUsers.elementAt(i);
/* 1390 */         name.trim();
/* 1391 */         if (name.length() <= 0)
/*      */           continue;
/* 1393 */         this.m_binder.putLocal("dUserName", name);
/* 1394 */         this.m_userWorkspace.execute("IaliasUser", this.m_binder);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1400 */       createServiceException(e, "!csUnableToInsertUsers");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void preEditAlias() throws DataException, ServiceException {
/* 1406 */     PluginFilters.filter("preEditAlias", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void preDeleteAlias() throws DataException, ServiceException {
/* 1411 */     PluginFilters.filter("preDeleteAlias", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void preDeleteUser() throws DataException, ServiceException {
/* 1416 */     PluginFilters.filter("preDeleteUser", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postAddAlias() throws DataException, ServiceException {
/* 1421 */     this.m_binder.putLocal("table", "Alias");
/* 1422 */     PluginFilters.filter("postAdd", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postDeleteAlias() throws DataException, ServiceException {
/* 1427 */     this.m_binder.putLocal("table", "Alias");
/* 1428 */     PluginFilters.filter("postDelete", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void postDeleteUser() throws DataException, ServiceException {
/* 1433 */     this.m_binder.putLocal("table", "Users");
/* 1434 */     PluginFilters.filter("postDelete", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editRole() throws DataException {
/* 1439 */     ResultSet rset = this.m_binder.getResultSet("GroupRole");
/* 1440 */     String query = "UroleDefinition";
/* 1441 */     if (rset.isEmpty())
/*      */     {
/* 1443 */       query = "IroleDefinition";
/*      */     }
/*      */ 
/* 1446 */     this.m_userWorkspace.execute(query, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void insertGroupRow() throws DataException, ServiceException
/*      */   {
/* 1452 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 1453 */     String iRoleDefinition = this.m_currentAction.getParamAt(1);
/* 1454 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(rsetName);
/* 1455 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1457 */       Properties props = drset.getCurrentRowProps();
/* 1458 */       DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/* 1459 */       ResultSet rs = this.m_userWorkspace.createResultSet("QroleDisplayName", this.m_binder);
/* 1460 */       if (rs.isRowPresent())
/*      */       {
/* 1462 */         this.m_binder.putLocal("dRoleDisplayName", rs.getStringValue(0));
/* 1463 */         if (rs.next())
/*      */         {
/* 1465 */           String warningMsg = LocaleUtils.encodeMessage("csRoleDisplayNameNotUnique", null, drset.getStringValue(0));
/*      */ 
/* 1467 */           Report.warning(null, warningMsg, null);
/*      */         }
/*      */       }
/* 1470 */       this.m_userWorkspace.execute(iRoleDefinition, this.m_binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateGroupName() throws DataException, ServiceException
/*      */   {
/* 1477 */     String groupName = this.m_binder.getLocal("dGroupName");
/* 1478 */     if ((groupName != null) && (!groupName.trim().isEmpty()))
/*      */       return;
/* 1480 */     String msg = LocaleUtils.encodeMessage("csInvalidGroupName", null);
/*      */ 
/* 1482 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateRoleName()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1489 */     String roleName = this.m_binder.getLocal("dRoleName");
/* 1490 */     if ((roleName != null) && (!roleName.trim().isEmpty()))
/*      */       return;
/* 1492 */     String msg = LocaleUtils.encodeMessage("csInvalidRoleName", null);
/*      */ 
/* 1494 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateUserNameAndType()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1501 */     String userName = this.m_binder.getLocal("dName");
/* 1502 */     if (userName == null)
/*      */     {
/* 1504 */       return;
/*      */     }
/*      */ 
/* 1507 */     String authType = this.m_binder.getLocal("dUserAuthType");
/* 1508 */     if (authType == null)
/*      */     {
/* 1510 */       return;
/*      */     }
/*      */ 
/* 1513 */     if (userName.equals("sysadmin"))
/*      */     {
/* 1515 */       String msg = LocaleUtils.encodeMessage("csUserMustBeLocal", null);
/* 1516 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1519 */     boolean isLocallyManaged = (authType.equalsIgnoreCase("LOCAL")) || (authType.equals("GLOBAL"));
/* 1520 */     if ((!isLocallyManaged) || 
/* 1522 */       (userName.indexOf(47) < 0))
/*      */       return;
/* 1524 */     String msg = LocaleUtils.encodeMessage("csUserNameNoForwardSlashes", null, userName);
/*      */ 
/* 1526 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadUserAndCheckEditAllowed()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1535 */     retrieveUserDatabaseProfileData();
/* 1536 */     if ((this.m_targetUserData == null) || (SecurityUtils.isUserOfRole(this.m_userData, "admin")))
/*      */     {
/* 1538 */       return;
/*      */     }
/*      */ 
/* 1542 */     if (!this.m_userData.m_hasAttributesLoaded)
/*      */     {
/* 1544 */       loadAttributeData(this.m_userData);
/*      */     }
/*      */ 
/* 1549 */     retrieveUserAttributes();
/*      */ 
/* 1551 */     SecurityUtils.checkUserAttributes(this.m_userData, this.m_targetUserData);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveUserOptionList() throws DataException, ServiceException
/*      */   {
/* 1557 */     String optKey = this.m_binder.get("dKey");
/* 1558 */     String textString = this.m_binder.get("OptionListString");
/* 1559 */     Vector v = DataUtils.parseOptionListEx(textString, false);
/*      */ 
/* 1562 */     this.m_binder.addOptionList(optKey, v);
/*      */ 
/* 1564 */     UserStorage.synchronizeOptionLists(this.m_binder, true, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadUserMetaData() throws ServiceException, DataException
/*      */   {
/* 1570 */     this.m_binder.addResultSet("UserMetaDefinition", SharedObjects.getTable("UserMetaDefinition"));
/* 1571 */     UserStorage.synchronizeOptionLists(this.m_binder, false, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void filterUserMetaData()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1579 */     if ((!isConditionVarTrue("IsSubAdmin")) || 
/* 1581 */       (SharedObjects.getEnvValueAsBoolean("AllowAdminRightToLeftLocale", false)))
/*      */       return;
/* 1583 */     Vector v = this.m_binder.getOptionList("Users_UserLocaleList");
/* 1584 */     if (v == null)
/*      */       return;
/* 1586 */     IdcVector filteredList = new IdcVector();
/* 1587 */     for (int i = 0; i < v.size(); ++i)
/*      */     {
/* 1589 */       String localeName = (String)v.get(i);
/* 1590 */       IdcLocale locale = LocaleResources.getLocale(localeName);
/* 1591 */       if (locale.m_direction.equals("rtl"))
/*      */         continue;
/* 1593 */       filteredList.add(localeName);
/*      */     }
/*      */ 
/* 1597 */     this.m_binder.addOptionList("Users_UserLocaleList", filteredList);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkAllowUserSelfRegistration()
/*      */     throws ServiceException
/*      */   {
/* 1607 */     boolean useSelfRegistration = SharedObjects.getEnvValueAsBoolean("UseSelfRegistration", false);
/* 1608 */     if (useSelfRegistration)
/*      */       return;
/* 1610 */     throw new ServiceException(null, IdcMessageFactory.lc("csUserSelfRegistrationNotEnabled", new Object[0]));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getUserUnique()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1617 */     String enteredUser = this.m_binder.getLocal("dName");
/* 1618 */     if (enteredUser == null)
/*      */     {
/* 1620 */       return;
/*      */     }
/* 1622 */     enteredUser = enteredUser.trim();
/*      */ 
/* 1625 */     String maxLengthStr = SharedObjects.getEnvironmentValue("dName:maxLength");
/* 1626 */     int maxLength = -1;
/*      */     try
/*      */     {
/* 1629 */       maxLength = Integer.parseInt(maxLengthStr);
/*      */     }
/*      */     catch (NumberFormatException e)
/*      */     {
/* 1633 */       maxLength = 50;
/*      */     }
/*      */ 
/* 1636 */     maxLength -= 20;
/*      */ 
/* 1638 */     if (enteredUser.length() > maxLength)
/*      */     {
/* 1640 */       this.m_binder.putLocal("isInvalidNameLength", "1");
/*      */ 
/* 1643 */       loadUserMetaData();
/*      */ 
/* 1645 */       setOverrideErrorPage("SELF_REGISTER_USER");
/* 1646 */       createServiceException(null, null);
/*      */     }
/*      */ 
/* 1650 */     if (Validation.checkUrlFileSegment(enteredUser) != 0)
/*      */     {
/* 1652 */       this.m_binder.putLocal("isInvalidNameFormat", "1");
/*      */ 
/* 1655 */       loadUserMetaData();
/*      */ 
/* 1657 */       setOverrideErrorPage("SELF_REGISTER_USER");
/* 1658 */       createServiceException(null, null);
/*      */     }
/*      */ 
/* 1661 */     UserData tmpUserData = null;
/*      */     try
/*      */     {
/* 1667 */       tmpUserData = UserStorage.retrieveUserDatabaseProfileDataFull(enteredUser, this.m_workspace, null, this, false, true);
/*      */ 
/* 1671 */       if (!UserUtils.isUserDataEmpty(tmpUserData))
/*      */       {
/* 1673 */         int userCounter = 1;
/* 1674 */         String suggestedUser = null;
/*      */ 
/* 1676 */         boolean foundEmptySlot = false;
/* 1677 */         for (int i = 0; i < 100; ++i)
/*      */         {
/* 1680 */           suggestedUser = enteredUser + String.valueOf(userCounter++);
/*      */ 
/* 1683 */           tmpUserData = UserStorage.retrieveUserDatabaseProfileDataFull(suggestedUser, this.m_workspace, null, this, false, true);
/*      */ 
/* 1685 */           if (!UserUtils.isUserDataEmpty(tmpUserData))
/*      */             continue;
/* 1687 */           foundEmptySlot = true;
/* 1688 */           break;
/*      */         }
/*      */ 
/* 1691 */         if (!foundEmptySlot)
/*      */         {
/* 1693 */           IdcMessage msg = IdcMessageFactory.lc("csFullyDefinedUserAlreadyExists", new Object[0]);
/* 1694 */           createServiceException(msg);
/*      */         }
/*      */ 
/* 1698 */         this.m_binder.putLocal("isSuggestedUser", "1");
/* 1699 */         this.m_binder.putLocal("suggestedUser", suggestedUser);
/*      */ 
/* 1702 */         loadUserMetaData();
/*      */ 
/* 1705 */         setOverrideErrorPage("SELF_REGISTER_USER");
/* 1706 */         createServiceException(null, null);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1711 */       createServiceException(e, "");
/*      */     }
/*      */ 
/* 1722 */     this.m_binder.putLocal("dName", tmpUserData.m_name);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addRegisteredUserAttributes() throws DataException, ServiceException
/*      */   {
/* 1728 */     loadUserMustExist();
/*      */ 
/* 1731 */     this.m_binder.putLocal("NewUser", this.m_binder.getLocal("dName"));
/*      */ 
/* 1734 */     String defaultRolesStr = SharedObjects.getEnvironmentValue("SelfRegisteredRoles");
/* 1735 */     if ((defaultRolesStr == null) || (defaultRolesStr.trim().equals("")))
/*      */     {
/* 1737 */       defaultRolesStr = "guest";
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 1743 */         defaultRolesStr = this.m_pageMerger.evaluateScript(defaultRolesStr);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1747 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1749 */           Report.debug("system", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1754 */     boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/* 1755 */     String defaultAccountsStr = null;
/* 1756 */     if (useAccounts)
/*      */     {
/* 1758 */       defaultAccountsStr = SharedObjects.getEnvironmentValue("SelfRegisteredAccounts");
/* 1759 */       if ((defaultAccountsStr == null) || (defaultAccountsStr.trim().equals("")))
/*      */       {
/* 1761 */         defaultAccountsStr = "#none(RWDA)";
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/* 1767 */           defaultAccountsStr = this.m_pageMerger.evaluateScript(defaultAccountsStr);
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/* 1771 */           if (SystemUtils.m_verbose)
/*      */           {
/* 1773 */             Report.debug("system", null, ignore);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1780 */     this.m_binder.removeLocal("NewUser");
/*      */ 
/* 1783 */     Vector defaultRoles = StringUtils.parseArray(defaultRolesStr, ',', '^');
/* 1784 */     Vector defaultAccounts = StringUtils.parseArray(defaultAccountsStr, ',', '^');
/*      */ 
/* 1787 */     SecurityUtils.loadExternalSecurityAttributes(this.m_targetUserData, "role", defaultRoles, null, true);
/* 1788 */     SecurityUtils.loadExternalSecurityAttributes(this.m_targetUserData, "account", defaultAccounts, null, true);
/*      */ 
/* 1791 */     UserStorage.putCachedUserData(this.m_targetUserData.m_name, this.m_targetUserData);
/*      */ 
/* 1794 */     Map attributes = this.m_targetUserData.getAttributesMap();
/* 1795 */     if (attributes == null)
/*      */     {
/* 1797 */       return;
/*      */     }
/*      */ 
/* 1800 */     String insertQuery = this.m_currentAction.getParamAt(0);
/*      */ 
/* 1802 */     Set entrySet = attributes.entrySet();
/* 1803 */     for (Iterator entryList = entrySet.iterator(); entryList.hasNext(); )
/*      */     {
/* 1805 */       Map.Entry entry = (Map.Entry)entryList.next();
/* 1806 */       List uaiList = (List)entry.getValue();
/* 1807 */       for (int i = 0; i < uaiList.size(); ++i)
/*      */       {
/* 1809 */         UserAttribInfo uai = (UserAttribInfo)uaiList.get(i);
/*      */ 
/* 1811 */         if ((uai.m_attribName.length() <= 0) || (uai.m_attribType.length() <= 0))
/*      */           continue;
/* 1813 */         this.m_binder.putLocal("dAttributeName", uai.m_attribName);
/* 1814 */         this.m_binder.putLocal("dAttributeType", uai.m_attribType);
/* 1815 */         this.m_binder.putLocal("dAttributePrivilege", Integer.toString(uai.m_attribPrivilege));
/*      */ 
/* 1817 */         this.m_userWorkspace.execute(insertQuery, this.m_binder);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateUserPassword()
/*      */     throws ServiceException
/*      */   {
/* 1826 */     String password = this.m_binder.getLocal("dPassword");
/* 1827 */     if ((password == null) || (password.equals(Users.getPasswordDash())))
/*      */     {
/* 1830 */       return;
/*      */     }
/*      */ 
/* 1833 */     int minLength = SharedObjects.getEnvironmentInt("MinimumPasswordLength", 0);
/* 1834 */     if (password.length() >= minLength)
/*      */       return;
/* 1836 */     String msg = LocaleUtils.encodeMessage("csUserPasswordUnderMinLength", null, Integer.valueOf(minLength));
/* 1837 */     createServiceException(null, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadUserLanguageIds()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1845 */     Properties localeToLanguageMap = new Properties();
/*      */ 
/* 1847 */     DataResultSet localeMapSet = SharedObjects.getTable("LanguageLocaleMap");
/* 1848 */     int localeIDIndex = ResultSetUtils.getIndexMustExist(localeMapSet, "lcLocaleId");
/* 1849 */     int languageIDIndex = ResultSetUtils.getIndexMustExist(localeMapSet, "lcLanguageId");
/*      */ 
/* 1851 */     for (localeMapSet.first(); localeMapSet.isRowPresent(); localeMapSet.next())
/*      */     {
/* 1853 */       String localeID = localeMapSet.getStringValue(localeIDIndex);
/* 1854 */       String languageID = localeMapSet.getStringValue(languageIDIndex);
/*      */ 
/* 1856 */       String oldLanguageID = localeToLanguageMap.getProperty(localeID);
/* 1857 */       if (oldLanguageID != null)
/*      */       {
/* 1859 */         if (languageID.indexOf(oldLanguageID) < 0)
/*      */           continue;
/* 1861 */         localeToLanguageMap.put(localeID, languageID);
/*      */       }
/*      */       else
/*      */       {
/* 1866 */         localeToLanguageMap.put(localeID, languageID);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1871 */     Properties localeToNativeNameMap = new Properties();
/*      */ 
/* 1873 */     DataResultSet localeNativeNameMapSet = SharedObjects.getTable("LocaleConfig");
/* 1874 */     localeIDIndex = ResultSetUtils.getIndexMustExist(localeNativeNameMapSet, "lcLocaleId");
/* 1875 */     int localeNativeNameIndex = ResultSetUtils.getIndexMustExist(localeNativeNameMapSet, "lcLocaleNativeName");
/*      */ 
/* 1877 */     for (localeNativeNameMapSet.first(); localeNativeNameMapSet.isRowPresent(); localeNativeNameMapSet.next())
/*      */     {
/* 1879 */       String localeID = localeNativeNameMapSet.getStringValue(localeIDIndex);
/* 1880 */       String localeNativeName = localeNativeNameMapSet.getStringValue(localeNativeNameIndex);
/*      */ 
/* 1882 */       String oldLocaleNativeName = localeToNativeNameMap.getProperty(localeID);
/* 1883 */       if ((oldLocaleNativeName != null) && (!oldLocaleNativeName.isEmpty()))
/*      */         continue;
/* 1885 */       localeToNativeNameMap.put(localeID, localeNativeName);
/*      */     }
/*      */ 
/* 1890 */     DataResultSet userInfoSet = (DataResultSet)this.m_binder.getResultSet("USER_INFO");
/* 1891 */     if (userInfoSet != null)
/*      */     {
/* 1893 */       FieldInfo fi = new FieldInfo();
/* 1894 */       fi.m_name = "dUserLanguageId";
/* 1895 */       fi.m_type = 6;
/* 1896 */       ArrayList fieldList = new ArrayList();
/* 1897 */       fieldList.add(fi);
/* 1898 */       userInfoSet.mergeFieldsWithFlags(fieldList, 0);
/* 1899 */       int userInfoLanguageIDIndex = ResultSetUtils.getIndexMustExist(userInfoSet, "dUserLanguageId");
/*      */ 
/* 1901 */       String localeID = ResultSetUtils.getValue(userInfoSet, "dUserLocale");
/* 1902 */       String languageID = localeToLanguageMap.getProperty(localeID);
/* 1903 */       if (languageID != null)
/*      */       {
/* 1905 */         userInfoSet.setCurrentValue(userInfoLanguageIDIndex, languageID);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1911 */     Vector localeList = this.m_binder.getOptionList("Users_UserLocaleList");
/* 1912 */     if (localeList == null)
/*      */       return;
/* 1914 */     String[] fields = { "lcLocaleID", "lcLanguageId", "lcLocaleDisplayName" };
/* 1915 */     DataResultSet localeLanguageMapSet = new DataResultSet(fields);
/*      */ 
/* 1917 */     int numLocales = localeList.size();
/* 1918 */     for (int i = 0; i < numLocales; ++i)
/*      */     {
/* 1920 */       String localeID = (String)localeList.elementAt(i);
/* 1921 */       String languageID = localeToLanguageMap.getProperty(localeID);
/* 1922 */       if (languageID == null)
/*      */       {
/* 1924 */         languageID = "";
/*      */       }
/* 1926 */       String localeNativeName = localeToNativeNameMap.getProperty(localeID);
/* 1927 */       if (localeNativeName == null)
/*      */       {
/* 1929 */         localeNativeName = "";
/*      */       }
/*      */ 
/* 1932 */       Vector row = new Vector();
/* 1933 */       row.add(localeID);
/* 1934 */       row.addElement(languageID);
/* 1935 */       row.addElement(localeNativeName);
/* 1936 */       localeLanguageMapSet.addRow(row);
/*      */     }
/*      */ 
/* 1939 */     this.m_binder.addResultSet("UserLocaleLanguageMap", localeLanguageMapSet);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1945 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104596 $";
/*      */   }
/*      */ 
/*      */   public static class RefreshUsersThread extends Thread
/*      */   {
/*      */     protected static Class s_thisClass;
/*      */     public static RefreshUsersThread s_currentThread;
/*      */     public static boolean s_triggerUpdate;
/*      */     public Service m_service;
/*      */     public DataBinder m_binder;
/*      */     public Workspace m_workspace;
/*      */     public ServiceHandler m_handler;
/*      */ 
/*      */     public static void triggerRefresh(Service callingService)
/*      */       throws DataException, ServiceException
/*      */     {
/* 1079 */       if (s_thisClass == null)
/*      */       {
/* 1081 */         s_thisClass = RefreshUsersThread.class;
/*      */       }
/* 1083 */       synchronized (s_thisClass)
/*      */       {
/* 1085 */         s_triggerUpdate = true;
/* 1086 */         if (s_currentThread == null)
/*      */         {
/* 1088 */           RefreshUsersThread thread = RefreshUsersThread.s_currentThread = new RefreshUsersThread();
/* 1089 */           thread.init(callingService);
/* 1090 */           thread.start();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*      */     protected static boolean resetTrigger()
/*      */     {
/* 1105 */       synchronized (s_thisClass)
/*      */       {
/* 1107 */         boolean isTriggered = s_triggerUpdate;
/* 1108 */         if (isTriggered)
/*      */         {
/* 1110 */           s_triggerUpdate = false;
/*      */         }
/*      */         else
/*      */         {
/* 1114 */           s_currentThread = null;
/*      */         }
/* 1116 */         return isTriggered;
/*      */       }
/*      */     }
/*      */ 
/*      */     public RefreshUsersThread()
/*      */     {
/* 1127 */       super("RefreshUsersThread");
/* 1128 */       setDaemon(true);
/*      */     }
/*      */ 
/*      */     public void init(Service callingService) throws DataException, ServiceException
/*      */     {
/* 1133 */       ServiceData serviceData = callingService.getServiceData();
/* 1134 */       String serviceName = serviceData.m_name;
/* 1135 */       Properties env = SharedObjects.getSafeEnvironment();
/* 1136 */       DataBinder binder = this.m_binder = new DataBinder(env);
/* 1137 */       Workspace ws = this.m_workspace = WorkspaceUtils.getWorkspace("user");
/* 1138 */       Service service = this.m_service = ServiceManager.getInitializedService(serviceName, binder, ws);
/* 1139 */       this.m_handler = service.getHandler("UserServiceHandler");
/*      */     }
/*      */ 
/*      */     public void run()
/*      */     {
/* 1145 */       if (!resetTrigger())
/*      */         return;
/*      */       try
/*      */       {
/* 1149 */         DataBinder binder = this.m_binder;
/* 1150 */         String tableName = "Users";
/* 1151 */         Users userList = (Users)SharedObjects.getTable("Users");
/* 1152 */         ResultSet rset = this.m_workspace.createResultSet(userList.getLocalUsersQuery(), null);
/* 1153 */         if ((rset == null) || (!rset.isRowPresent()))
/*      */         {
/* 1155 */           throw new DataException("!csUnableToFindUserList");
/*      */         }
/* 1157 */         userList.load(rset);
/* 1158 */         SharedObjects.putTable("Users", userList);
/* 1159 */         binder.addResultSet("Users", userList);
/*      */ 
/* 1161 */         ServiceHandler handler = this.m_handler;
/* 1162 */         if (handler != null)
/*      */         {
/* 1164 */           handler.executeAction("updateCache");
/*      */         }
/* 1166 */         SubjectManager.notifyChanged("users");
/* 1167 */         binder.removeResultSet("Users");
/*      */ 
/* 1176 */         this.m_workspace.releaseConnection();
/*      */       }
/*      */       catch (IdcException e)
/*      */       {
/* 1171 */         Report.warning("system", e, "csUnableToRefreshUserInformation", new Object[0]);
/*      */       }
/*      */       finally
/*      */       {
/* 1176 */         this.m_workspace.releaseConnection();
/*      */       }
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserService
 * JD-Core Version:    0.5.4
 */