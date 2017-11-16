/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.provider.UserProvider;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserAttribInfo;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.Users;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class UserStorageImplementor
/*      */ {
/*      */   public final String[] EXTRACTED_META_FIELDS;
/*      */   public final String[] AUTH_TYPE_LIST;
/*      */   public UserTempCache m_userTempCache;
/*      */   public Hashtable m_cacheNameMap;
/*      */   public boolean m_isInit;
/*      */   public boolean m_allowCaseInsensitiveLogin;
/*      */   protected Workspace m_workspace;
/*      */ 
/*      */   public UserStorageImplementor()
/*      */   {
/*   33 */     this.EXTRACTED_META_FIELDS = new String[] { "umdName", "umdIsAdminEdit", "umdOverrideBitFlag", "umdType", "umdCaption" };
/*      */ 
/*   36 */     this.AUTH_TYPE_LIST = new String[] { "LOCAL", "GLOBAL", "EXTERNAL" };
/*      */ 
/*   41 */     this.m_isInit = false;
/*   42 */     this.m_allowCaseInsensitiveLogin = false;
/*   43 */     this.m_workspace = null;
/*      */   }
/*      */ 
/*      */   public void init() throws ServiceException
/*      */   {
/*   48 */     if (this.m_isInit)
/*      */       return;
/*   50 */     this.m_isInit = true;
/*      */ 
/*   52 */     this.m_userTempCache = ((UserTempCache)ComponentClassFactory.createClassInstance("intradoc.server.UserTempCache", "intradoc.server.UserTempCache", "!csUserTempCacheError"));
/*      */ 
/*   57 */     this.m_cacheNameMap = new Hashtable();
/*   58 */     this.m_allowCaseInsensitiveLogin = SharedObjects.getEnvValueAsBoolean("AllowCaseInsensitiveLogin", false);
/*      */ 
/*   62 */     this.m_workspace = WorkspaceUtils.getWorkspace("user");
/*      */   }
/*      */ 
/*      */   public UserData retrieveUserDatabaseProfileDataImplement(String name, Workspace ws, DataBinder credentialData, ExecutionContext cxt, boolean isLoadAttributes, boolean isGetUnknownFromProvider)
/*      */     throws DataException, ServiceException
/*      */   {
/*   71 */     if ((name == null) || (name.length() == 0) || (name.equalsIgnoreCase("anonymous")))
/*      */     {
/*   73 */       return null;
/*      */     }
/*      */     try
/*      */     {
/*   77 */       if (credentialData == null)
/*      */       {
/*   79 */         credentialData = new DataBinder();
/*   80 */         throw new DataException("");
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*   85 */       Report.trace(null, "Credential data parameter to retrieveUserDatabaseProfileDataImplement is null", e);
/*      */     }
/*      */ 
/*   88 */     UserStorageImplementorData vars = new UserStorageImplementorData();
/*   89 */     vars.isNewUser = false;
/*   90 */     vars.needExternalData = false;
/*   91 */     vars.cachedPasswordIsGood = false;
/*   92 */     vars.queriedDbUserInfo = false;
/*   93 */     vars.queriedDbUserAttributes = false;
/*   94 */     vars.queriedUserProvider = false;
/*   95 */     cxt.setCachedObject("UserStorageImplementorData", vars);
/*      */ 
/*   97 */     if (SystemUtils.m_verbose)
/*      */     {
/*   99 */       String binderNull = (credentialData == null) ? "is null" : "is not null";
/*  100 */       printThreadMsg("Retrieving user data (isLoadAttributes=" + isLoadAttributes + ", credentialData " + binderNull + ") for " + name);
/*      */     }
/*      */     UserData userData;
/*      */     try
/*      */     {
/*  105 */       throw new DataException("Exception manufactured to capture current stack trace.");
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  109 */       Report.trace("userstorage", "Debug dump of current call stack", e);
/*      */ 
/*  113 */       long startTime = 0L;
/*  114 */       startTime = System.currentTimeMillis();
/*  115 */       String msg = "Start user storage query for user " + name + ".";
/*  116 */       if (SystemUtils.m_verbose)
/*      */       {
/*  118 */         Report.debug("userstorage", msg, null);
/*      */       }
/*      */ 
/*  125 */       String userName = name;
/*  126 */       String cacheName = null;
/*      */ 
/*  128 */       DataBinder filterBinder = new DataBinder();
/*      */ 
/*  131 */       boolean isAuthenticating = DataBinderUtils.getLocalBoolean(credentialData, "authenticateUser", false);
/*  132 */       String originalUser = credentialData.getEnvironmentValue("HTTP_ORIGINALUSER");
/*  133 */       boolean hasClientOriginalUser = (originalUser != null) && (originalUser.length() > 0);
/*  134 */       if (!hasClientOriginalUser)
/*      */       {
/*  137 */         originalUser = userName;
/*      */ 
/*  139 */         if (isAuthenticating)
/*      */         {
/*  142 */           this.m_cacheNameMap.remove(userName);
/*      */         }
/*      */         else
/*      */         {
/*  146 */           cacheName = (String)this.m_cacheNameMap.get(userName);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  151 */         filterBinder.putLocal("hasClientOriginalUser", "1");
/*  152 */         cacheName = userName;
/*      */       }
/*  154 */       filterBinder.putLocal("originalName", originalUser);
/*      */ 
/*  158 */       userData = null;
/*  159 */       if ((cacheName != null) && (cacheName.length() > 0))
/*      */       {
/*  161 */         filterBinder.putLocal("cacheName", cacheName);
/*  162 */         userData = getUserData(cacheName);
/*  163 */         if (userData == null)
/*      */         {
/*  165 */           userData = UserUtils.createUserData(cacheName);
/*  166 */           vars.isNewUser = true;
/*  167 */           if (SystemUtils.m_verbose)
/*      */           {
/*  169 */             Report.debug("userstorage", "Created user object for user based on cache map user name of " + cacheName, null);
/*      */           }
/*      */         }
/*  172 */         cxt.setCachedObject("CachedUserData", userData);
/*      */       }
/*      */ 
/*  175 */       cxt.setCachedObject("CredentialsData", credentialData);
/*  176 */       if (PluginFilters.filter("mapToInternalUserName", ws, filterBinder, cxt) != -1)
/*      */       {
/*  179 */         boolean isInvalidMap = DataBinderUtils.getLocalBoolean(filterBinder, "isInvalidMap", false);
/*  180 */         if (isInvalidMap)
/*      */         {
/*  182 */           cacheName = null;
/*  183 */           userData = null;
/*  184 */           this.m_cacheNameMap.remove(originalUser);
/*      */         }
/*      */         else
/*      */         {
/*  188 */           String newName = filterBinder.getAllowMissing("dName");
/*  189 */           if ((newName != null) && (newName.length() > 0))
/*      */           {
/*  191 */             cacheName = newName;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  196 */       if ((cacheName == null) || (cacheName.length() == 0))
/*      */       {
/*  198 */         cacheName = userName.trim();
/*  199 */         if ((cacheName.length() != userName.length()) && 
/*  201 */           (!SharedObjects.getEnvValueAsBoolean("AllowSpacesInUsername", false)))
/*      */         {
/*  203 */           int errCode = -1;
/*  204 */           if (isAuthenticating)
/*      */           {
/*  206 */             errCode = -21;
/*      */           }
/*  208 */           throw new ServiceException(errCode, "!csUserNameContainsSpaces");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  213 */       if (userData == null)
/*      */       {
/*  217 */         userData = getUserData(cacheName);
/*  218 */         if (userData == null)
/*      */         {
/*  220 */           userData = UserUtils.createUserData(cacheName);
/*  221 */           vars.isNewUser = true;
/*  222 */           if (SystemUtils.m_verbose)
/*      */           {
/*  224 */             printThreadMsg("Created user object for user " + cacheName);
/*      */           }
/*      */         }
/*      */       }
/*  228 */       if (!userData.m_name.equals(cacheName))
/*      */       {
/*  230 */         Report.trace("userstorage", "New cacheName different from cached cacheName, cacheName=" + cacheName + ", userData.m_name=" + userData.m_name, null);
/*      */ 
/*  232 */         if (!this.m_allowCaseInsensitiveLogin)
/*      */         {
/*  235 */           userData.m_isExpired = true;
/*      */         }
/*      */       }
/*      */ 
/*  239 */       UserData userDataOriginal = userData;
/*      */ 
/*  241 */       synchronized (userDataOriginal)
/*      */       {
/*  254 */         if (!userData.m_isExpired)
/*      */         {
/*  256 */           String authType = userData.getProperty("dUserAuthType");
/*  257 */           if ((authType != null) && (authType.equalsIgnoreCase("EXTERNAL")))
/*      */           {
/*  259 */             boolean authenticateUser = StringUtils.convertToBool(credentialData.getLocal("authenticateUser"), false);
/*      */ 
/*  261 */             if (authenticateUser)
/*      */             {
/*  263 */               printThreadMsg("Authenticating cached Password");
/*  264 */               authenticateAgainstCachedPassword(cacheName, userData, credentialData, vars);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  271 */         userData = UserUtils.createUserData();
/*  272 */         copyUserData(userDataOriginal, userData, null);
/*      */ 
/*  275 */         boolean loadedFromDb = false;
/*  276 */         printThreadMsg("Finished user name determination, user=" + userData.m_name + ", expired=" + userData.m_isExpired + ", isNewUser=" + vars.isNewUser + ", hasAttributesLoaded=" + userData.m_hasAttributesLoaded + ", authtype=" + userData.getProperty("dUserAuthType"));
/*      */ 
/*  279 */         if ((vars.isNewUser) || (userData.m_isExpired))
/*      */         {
/*  281 */           loadedFromDb = loadUserFromDatabase(cacheName, userData, ws, isGetUnknownFromProvider, vars);
/*      */ 
/*  284 */           userData.setProperty("userDataFromDb", loadedFromDb + "");
/*      */ 
/*  287 */           userData.checkCreateAttributes(true);
/*      */         }
/*      */ 
/*  291 */         if ((!userData.m_hasAttributesLoaded) && ((((!vars.isNewUser) && (isLoadAttributes)) || (loadedFromDb))))
/*      */         {
/*  294 */           String authType = userData.getProperty("dUserAuthType");
/*  295 */           if (SystemUtils.m_verbose)
/*      */           {
/*  297 */             printThreadMsg("Retrieving attributes (type=" + authType + ") for " + cacheName);
/*      */           }
/*  299 */           vars.needExternalData = ((authType != null) && (authType.equalsIgnoreCase("EXTERNAL")));
/*      */         }
/*      */ 
/*  304 */         cxt.setCachedObject("TargetUserData", userData);
/*  305 */         cxt.setCachedObject("isLoadAttributes", new Boolean(isLoadAttributes));
/*  306 */         cxt.setCachedObject("isLoaded", new Boolean(userData.m_hasAttributesLoaded));
/*      */ 
/*  308 */         checkExternalProvidersForUser(cacheName, userData, credentialData, ws, cxt, isLoadAttributes, vars);
/*      */ 
/*  311 */         if (vars.cachedPasswordIsGood)
/*      */         {
/*  313 */           credentialData.putLocal("hasAuthenticatedUser", "1");
/*      */         }
/*      */ 
/*  319 */         int retVal = PluginFilters.filter("loadUserAttributes", ws, credentialData, cxt);
/*      */ 
/*  322 */         if ((retVal == 0) && (!userData.m_hasAttributesLoaded) && (isLoadAttributes == true))
/*      */         {
/*  325 */           loadAttributesForLocalUser(cacheName, userData, ws, vars);
/*  326 */           if (SystemUtils.m_verbose)
/*      */           {
/*  328 */             Report.debug("userstorage", "Load attributes from database for " + cacheName, null);
/*  329 */             String roles = SecurityUtils.getRolePackagedList(userData);
/*  330 */             String accounts = SecurityUtils.getFullExportedAccountslist(userData);
/*      */ 
/*  332 */             printThreadMsg("Database->Roles=" + roles + " Accounts=" + accounts + " for " + cacheName);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  339 */         if (SystemUtils.m_verbose)
/*      */         {
/*  341 */           Report.debug("userstorage", "Check state of attributes (isLoadAttributes=" + isLoadAttributes + ")", null);
/*      */         }
/*  343 */         if (userData.m_hasAttributesLoaded)
/*      */         {
/*  345 */           if (SystemUtils.m_verbose)
/*      */           {
/*  347 */             Report.debug("userstorage", "Check that user attributes are fully loaded.", null);
/*      */           }
/*      */ 
/*  366 */           if ((userData.getAttributesMap() != null) && (userData.getAttributes("role") != null))
/*      */           {
/*  369 */             if (Report.m_verbose)
/*      */             {
/*  371 */               Report.debug("userstorage", "User attributes are fully loaded.", null);
/*      */             }
/*  373 */             vars.retrievedUserAttributes = true;
/*      */           }
/*      */         }
/*      */ 
/*  377 */         if (isLoadAttributes)
/*      */         {
/*  379 */           if (vars.retrievedUserAttributes)
/*      */           {
/*  381 */             credentialData.putLocal("loadedUserAttributes", "1");
/*      */           }
/*      */           else
/*      */           {
/*  385 */             printThreadMsg("No attributes loaded for " + cacheName);
/*      */           }
/*  387 */           userData.m_hasAttributesLoaded = true;
/*  388 */           if (userData.getAttributesMap() == null)
/*      */           {
/*  390 */             printThreadMsg("Creating empty list of attributes for " + cacheName);
/*  391 */             userData.checkCreateAttributes(false);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  402 */         if ((!hasClientOriginalUser) && (!userData.m_name.equals(originalUser)))
/*      */         {
/*  404 */           this.m_cacheNameMap.put(originalUser, userData.m_name);
/*      */         }
/*      */ 
/*  408 */         if ((isLoadAttributes) && (((vars.isNewUser) || (vars.needExternalData) || (vars.queriedDbUserAttributes) || (vars.queriedDbUserInfo) || (vars.queriedUserProvider))))
/*      */         {
/*  411 */           printThreadMsg("Updating shared cached copy for user " + cacheName);
/*  412 */           boolean isNewlyUpdated = (vars.isNewUser) || (userData.m_isExpired);
/*  413 */           if (isNewlyUpdated)
/*      */           {
/*  416 */             userData.m_isExpired = false;
/*      */ 
/*  422 */             PluginFilters.filter("updatingUserCache", ws, filterBinder, cxt);
/*      */           }
/*      */           else
/*      */           {
/*  428 */             PluginFilters.filter("updatingLocalUserCache", ws, filterBinder, cxt);
/*      */           }
/*  430 */           copyUserData(userData, userDataOriginal, vars);
/*      */ 
/*  434 */           if (isNewlyUpdated)
/*      */           {
/*  436 */             printThreadMsg("UserTempCache updated with user data for " + cacheName);
/*      */ 
/*  441 */             if (!UserUtils.isUserDataEmpty(userData))
/*      */             {
/*  443 */               this.m_userTempCache.putCachedUserData(userData.m_name, userDataOriginal);
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  449 */         if ((SystemUtils.isActiveTrace("userstorage")) && (userData.m_hasAttributesLoaded))
/*      */         {
/*  451 */           String roles = SecurityUtils.getRolePackagedList(userData);
/*  452 */           String accounts = SecurityUtils.getFullExportedAccountslist(userData);
/*  453 */           printThreadMsg("Retrieved Roles=" + roles + " Accounts=" + accounts + " for " + cacheName);
/*      */         }
/*      */ 
/*  456 */         long endTime = System.currentTimeMillis();
/*  457 */         printPerformanceInfo(startTime, endTime, vars);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  462 */     return userData;
/*      */   }
/*      */ 
/*      */   public void copyUserData(UserData source, UserData target, UserStorageImplementorData vars)
/*      */   {
/*  467 */     target.m_isExpired = source.m_isExpired;
/*  468 */     target.copyUserProfile(source);
/*  469 */     if ((source.m_hasAttributesLoaded) && (((vars == null) || (vars.retrievedUserAttributes))))
/*      */     {
/*  471 */       target.copyAttributes(source);
/*      */     }
/*      */     else
/*      */     {
/*  476 */       target.checkCreateAttributes(true);
/*  477 */       target.m_hasAttributesLoaded = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadAttributesForLocalUser(String name, UserData userData, Workspace ws, UserStorageImplementorData vars)
/*      */     throws DataException
/*      */   {
/*  485 */     String userAuthType = userData.getProperty("dUserAuthType");
/*  486 */     if ((userAuthType == null) || (userAuthType.equalsIgnoreCase("EXTERNAL")))
/*      */     {
/*  488 */       return;
/*      */     }
/*      */ 
/*  491 */     DataBinder binder = new DataBinder();
/*  492 */     binder.putLocal("dUserName", userData.m_name);
/*  493 */     ResultSet rset = this.m_workspace.createResultSet("QuserSecurityAttributes", binder);
/*  494 */     vars.queriedDbUserAttributes = true;
/*      */ 
/*  496 */     String[][] attribInfo = ResultSetUtils.createStringTable(rset, new String[] { "dAttributeType", "dAttributeName", "dAttributePrivilege" });
/*      */ 
/*  501 */     if (userData.m_attributes.size() == 0)
/*      */     {
/*  503 */       userData.setAttributes(attribInfo);
/*      */     }
/*      */     else
/*      */     {
/*  507 */       for (int a = 0; a < attribInfo.length; ++a)
/*      */       {
/*  509 */         userData.addAttribute(attribInfo[a][0], attribInfo[a][1], attribInfo[a][2]);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  514 */     Vector roleList = userData.getAttributes("role");
/*  515 */     if (roleList == null)
/*      */     {
/*  517 */       roleList = new IdcVector();
/*  518 */       UserAttribInfo uai = new UserAttribInfo();
/*  519 */       uai.m_attribType = "role";
/*  520 */       uai.m_attribName = "guest";
/*  521 */       roleList.addElement(uai);
/*  522 */       userData.putAttributes("role", roleList);
/*      */     }
/*      */ 
/*  525 */     userData.m_hasAttributesLoaded = true;
/*      */   }
/*      */ 
/*      */   public void checkExternalProvidersForUser(String name, UserData userData, DataBinder credentialData, Workspace ws, ExecutionContext cxt, boolean isLoadAttributes, UserStorageImplementorData vars)
/*      */     throws DataException, ServiceException
/*      */   {
/*  532 */     Provider provider = null;
/*  533 */     UserProvider userProvider = null;
/*  534 */     DataBinder providerData = null;
/*      */ 
/*  537 */     if ((!vars.isNewUser) && (!vars.needExternalData))
/*      */     {
/*  539 */       provider = retrieveDefaultProvider(userData);
/*  540 */       if (provider != null)
/*      */       {
/*  542 */         userProvider = (UserProvider)provider.getProvider();
/*  543 */         vars.needExternalData = userProvider.checkSynchronization(userData.getProperties());
/*  544 */         if (SystemUtils.m_verbose)
/*      */         {
/*  546 */           Report.debug("userstorage", "Checked synchronization (needExternal=" + vars.needExternalData + ") for " + name, null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  553 */     if (!vars.needExternalData)
/*      */     {
/*  555 */       return;
/*      */     }
/*      */ 
/*  577 */     Properties oldProps = userData.getProperties();
/*  578 */     userData.setProperties((Properties)oldProps.clone());
/*      */ 
/*  580 */     boolean isUserFound = false;
/*      */ 
/*  582 */     if (userProvider == null)
/*      */     {
/*  584 */       provider = retrieveDefaultProvider(userData);
/*  585 */       if (provider != null)
/*      */       {
/*  587 */         userProvider = (UserProvider)provider.getProvider();
/*      */       }
/*      */     }
/*  590 */     if (userProvider != null)
/*      */     {
/*      */       try
/*      */       {
/*  594 */         if (SystemUtils.m_verbose)
/*      */         {
/*  596 */           Report.debug("userstorage", "Checking Default UserProvider " + provider.getName(), null);
/*      */         }
/*  598 */         userProvider.checkCredentials(userData, credentialData, isLoadAttributes);
/*      */ 
/*  602 */         userData.copyAttributesToExternal();
/*  603 */         isUserFound = true;
/*      */       }
/*      */       catch (ServiceException s)
/*      */       {
/*  613 */         if (s.m_errorCode == -21)
/*      */         {
/*  615 */           if (SystemUtils.m_verbose)
/*      */           {
/*  617 */             Report.debug("userstorage", "User failed to authenticate. Checking if dUserOrgPath exists in " + provider.getName(), null);
/*      */           }
/*      */           try
/*      */           {
/*  621 */             userData.setProperty("hasExtendedInfo", "false");
/*  622 */             credentialData.putLocal("authenticateUser", "false");
/*  623 */             userProvider.checkCredentials(userData, credentialData, isLoadAttributes);
/*      */ 
/*  625 */             isUserFound = true;
/*      */           }
/*      */           catch (ServiceException ss)
/*      */           {
/*  629 */             s = ss;
/*      */           }
/*      */           finally
/*      */           {
/*  633 */             credentialData.putLocal("authenticateUser", "true");
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  640 */         if (s.m_errorCode == -16)
/*      */         {
/*  642 */           printThreadMsg("User not found in UserProvider " + provider.getName());
/*      */ 
/*  648 */           String oldOrgPath = userData.getProperty("dUserOrgPath");
/*  649 */           credentialData.putLocal("oldUserOrgPath", oldOrgPath);
/*      */ 
/*  651 */           userData.setProperty("dUserOrgPath", "");
/*  652 */           isUserFound = false;
/*      */         }
/*      */         else
/*      */         {
/*  656 */           throw s;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  662 */     if (!isUserFound)
/*      */     {
/*  664 */       printThreadMsg("User not found in default/preferred provider");
/*  665 */       Vector userProviderList = createUserProviderList();
/*  666 */       for (int i = 0; i < userProviderList.size(); ++i)
/*      */       {
/*  668 */         provider = (Provider)userProviderList.elementAt(i);
/*  669 */         userProvider = (UserProvider)provider.getProvider();
/*      */         try
/*      */         {
/*  673 */           if (SystemUtils.m_verbose)
/*      */           {
/*  675 */             Report.debug("userstorage", "Checking UserProvider " + provider.getName(), null);
/*      */           }
/*  677 */           userProvider.checkCredentials(userData, credentialData, isLoadAttributes);
/*  678 */           isUserFound = true;
/*      */         }
/*      */         catch (ServiceException s)
/*      */         {
/*  685 */           if ((s.m_errorCode == -16) || (s.m_errorCode == -21))
/*      */           {
/*  688 */             printThreadMsg("User not found in UserProvider " + provider.getName());
/*  689 */             userData.setProperty("dUserOrgPath", "");
/*  690 */             isUserFound = false;
/*  691 */             break label596:
/*      */           }
/*  693 */           throw s;
/*      */         }
/*  695 */         label596: if (isUserFound) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  702 */     userData.setProperties(oldProps);
/*      */ 
/*  706 */     if (provider != null)
/*      */     {
/*  708 */       providerData = provider.getProviderData();
/*      */ 
/*  710 */       if ((isUserFound) && (credentialData != null))
/*      */       {
/*  712 */         printThreadMsg("Setting dUserSourceOrgPath to " + providerData.getLocal("SourcePath"));
/*  713 */         credentialData.putLocal("dUserSourceOrgPath", providerData.getLocal("SourcePath"));
/*      */ 
/*  723 */         if (isLoadAttributes)
/*      */         {
/*  725 */           vars.retrievedUserAttributes = true;
/*      */         }
/*      */       }
/*      */ 
/*  729 */       if (cxt != null)
/*      */       {
/*  731 */         cxt.setCachedObject("TargetUserProvider", userProvider);
/*      */       }
/*      */     }
/*      */ 
/*  735 */     if ((isUserFound) && (userData.getProperty("dUserAuthType") == null))
/*      */     {
/*  740 */       userData.setProperty("dUserAuthType", "EXTERNAL");
/*      */     }
/*      */ 
/*  744 */     PluginFilters.filter("alterProviderAttributes", ws, credentialData, cxt);
/*      */ 
/*  746 */     vars.queriedUserProvider = true;
/*  747 */     if (SystemUtils.m_verbose)
/*      */     {
/*  749 */       Report.debug("userstorage", "Checked credentials (isLoadAttributes=" + isLoadAttributes + ") for " + name, null);
/*      */ 
/*  751 */       if (userData.m_hasAttributesLoaded)
/*      */       {
/*  753 */         String roles = SecurityUtils.getRolePackagedList(userData);
/*  754 */         String accounts = SecurityUtils.getFullExportedAccountslist(userData);
/*  755 */         Report.debug("userstorage", "Provider->Roles=" + roles + " Accounts=" + accounts + " for " + name, null);
/*      */       }
/*      */       else
/*      */       {
/*  760 */         Report.debug("userstorage", "Provider did not provide attributes.", null);
/*      */       }
/*      */     }
/*      */ 
/*  764 */     if (credentialData != null)
/*      */     {
/*  766 */       boolean authenticateUser = StringUtils.convertToBool(credentialData.getLocal("authenticateUser"), false);
/*      */ 
/*  768 */       if (authenticateUser)
/*      */       {
/*  770 */         boolean hasAuthenticated = StringUtils.convertToBool(credentialData.getLocal("hasAuthenticatedUser"), false);
/*      */ 
/*  772 */         if (hasAuthenticated)
/*      */         {
/*  774 */           String curPassword = credentialData.getLocal("userPassword");
/*  775 */           if (curPassword != null)
/*      */           {
/*  777 */             String curPasswordHash = UserUtils.encodePassword(name, curPassword, "SHA1-CB");
/*      */ 
/*  779 */             userData.setProperty("cachedPasswordHash", curPasswordHash);
/*      */           }
/*      */ 
/*      */         }
/*  784 */         else if (vars.cachedPasswordIsGood)
/*      */         {
/*  788 */           userData.getProperties().remove("cachedPasswordHash");
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  794 */     vars.cachedPasswordIsGood = false;
/*      */   }
/*      */ 
/*      */   public boolean loadUserFromDatabase(String name, UserData userData, Workspace ws, boolean isGetUnknownFromProvider, UserStorageImplementorData vars)
/*      */     throws DataException
/*      */   {
/*  800 */     DataBinder binder = new DataBinder();
/*  801 */     boolean loadedFromDb = false;
/*      */ 
/*  803 */     binder.putLocal("dName", name);
/*  804 */     ResultSet rs = new DataResultSet();
/*  805 */     if (this.m_workspace != null)
/*      */     {
/*  807 */       rs = this.m_workspace.createResultSet("Quser", binder);
/*  808 */       vars.queriedDbUserInfo = true;
/*      */     }
/*      */ 
/*  811 */     boolean usingDbNameForCacheName = false;
/*  812 */     if (!rs.isEmpty())
/*      */     {
/*  815 */       rs.setDateFormat(LocaleUtils.m_odbcDateFormat);
/*  816 */       DataResultSet drset = new DataResultSet();
/*  817 */       drset.copy(rs);
/*  818 */       Properties props = drset.getCurrentRowProps();
/*  819 */       DataBinder.mergeHashTables(userData.getProperties(), props);
/*  820 */       String dbName = props.getProperty("dName");
/*  821 */       usingDbNameForCacheName = this.m_allowCaseInsensitiveLogin;
/*  822 */       if (!dbName.equals(userData.m_name))
/*      */       {
/*  824 */         Report.trace("userstorage", "DB name is different from cache user name,  dbName=" + dbName + ", cacheName=" + userData.m_name, null);
/*      */ 
/*  826 */         if (usingDbNameForCacheName)
/*      */         {
/*  828 */           Report.trace("userstorage", "Synchronizing cache user name to dbName", null);
/*  829 */           userData.m_name = dbName;
/*      */         }
/*      */       }
/*      */ 
/*  833 */       userData.m_hasAttributesLoaded = false;
/*  834 */       loadedFromDb = true;
/*  835 */       if (SystemUtils.m_verbose)
/*      */       {
/*  837 */         printThreadMsg("Loaded record from database for " + name);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  854 */       vars.isNewUser = true;
/*      */ 
/*  856 */       if ((isGetUnknownFromProvider) || (SharedObjects.getEnvValueAsBoolean("AllowExternalDataCheckWithoutDBEntry", true)))
/*      */       {
/*  858 */         vars.needExternalData = true;
/*      */       }
/*  860 */       if (SystemUtils.m_verbose)
/*      */       {
/*  862 */         printThreadMsg("Creating new entry for database (fromProvider=" + isGetUnknownFromProvider + ") for " + name);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  872 */     if ((!usingDbNameForCacheName) && (!name.equals(userData.m_name)))
/*      */     {
/*  874 */       Report.trace("userstorage", "Passed in name is different from cache user name, name=" + name + ", cacheName=" + userData.m_name, null);
/*      */ 
/*  876 */       Report.trace("userstorage", "Synchronizing cache user name to passed in name", null);
/*  877 */       userData.m_name = name;
/*      */     }
/*      */ 
/*  880 */     return loadedFromDb;
/*      */   }
/*      */ 
/*      */   public void printPerformanceInfo(long startTime, long endTime, UserStorageImplementorData vars)
/*      */   {
/*  885 */     long t = endTime - startTime;
/*  886 */     if ((!SystemUtils.m_verbose) && (t <= 10000L))
/*      */       return;
/*  888 */     int count = 0;
/*  889 */     String info = "";
/*  890 */     if (vars.queriedDbUserInfo)
/*      */     {
/*  892 */       info = info + "info";
/*  893 */       ++count;
/*      */     }
/*  895 */     if (vars.queriedDbUserAttributes)
/*      */     {
/*  897 */       if (count++ > 0)
/*      */       {
/*  899 */         info = info + ", ";
/*      */       }
/*  901 */       info = info + "attributes";
/*      */     }
/*  903 */     if (vars.queriedUserProvider)
/*      */     {
/*  905 */       if (count++ > 0)
/*      */       {
/*  907 */         info = info + ", ";
/*      */       }
/*  909 */       info = info + "provider";
/*      */     }
/*  911 */     if (count == 0)
/*      */     {
/*  913 */       info = "cache";
/*      */     }
/*  915 */     String msg = "Query of " + info;
/*  916 */     msg = msg + " required " + t + " milliseconds.";
/*  917 */     Report.debug("userstorage", msg, null);
/*      */   }
/*      */ 
/*      */   public void authenticateAgainstCachedPassword(String name, UserData userData, DataBinder credentialData, UserStorageImplementorData vars)
/*      */   {
/*  924 */     if (userData.m_hasAttributesLoaded)
/*      */     {
/*  926 */       String cPassword = userData.getProperty("cachedPasswordHash");
/*  927 */       if (cPassword != null)
/*      */       {
/*  929 */         String curPassword = credentialData.getLocal("userPassword");
/*  930 */         if (curPassword != null)
/*      */         {
/*  932 */           if (SystemUtils.m_verbose)
/*      */           {
/*  934 */             printThreadMsg("Validating password against cache for user " + name);
/*      */           }
/*  936 */           String curPasswordHash = UserUtils.encodePassword(name, curPassword, "SHA1-CB");
/*      */ 
/*  938 */           if (curPasswordHash.equals(cPassword))
/*      */           {
/*  940 */             vars.cachedPasswordIsGood = true;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  945 */     if (vars.cachedPasswordIsGood)
/*      */       return;
/*  947 */     if (SystemUtils.m_verbose)
/*      */     {
/*  949 */       Report.debug("userstorage", "Mismatched password against cache, cached expired for user " + name, null);
/*      */     }
/*  951 */     userData.m_isExpired = true;
/*      */   }
/*      */ 
/*      */   public void storeUserDatabaseProfileData(UserData curData, DataBinder newData, Workspace ws, ExecutionContext cxt, boolean copyAll, boolean doAdminFields, boolean alwaysSave, boolean userDataFromDb)
/*      */     throws DataException, ServiceException
/*      */   {
/*  960 */     if (SystemUtils.m_verbose)
/*      */     {
/*  962 */       Report.debug("userstorage", "storeUserDatabaseProfileData copyAll=" + copyAll + ", doAdminFields=" + doAdminFields + ", alwaysSave=" + alwaysSave + ", userDataFromDb=" + userDataFromDb, null);
/*      */     }
/*      */ 
/*  970 */     boolean[] params = { copyAll, doAdminFields, alwaysSave, userDataFromDb };
/*  971 */     if (cxt != null)
/*      */     {
/*  973 */       cxt.setCachedObject("storeUserDatabaseProfileData:params", params);
/*  974 */       cxt.setCachedObject("CurrentUserData", curData);
/*      */     }
/*  976 */     if (PluginFilters.filter("storeUserDatabaseProfileData", ws, newData, cxt) == -1)
/*      */     {
/*  978 */       return;
/*      */     }
/*  980 */     copyAll = params[0];
/*  981 */     doAdminFields = params[1];
/*  982 */     alwaysSave = params[2];
/*  983 */     userDataFromDb = params[3];
/*      */ 
/*  985 */     boolean[] hasChanged = { false };
/*  986 */     copyUserValue(curData, newData, "dUserAuthType", hasChanged, cxt);
/*      */ 
/*  988 */     String authType = curData.getProperty("dUserAuthType");
/*  989 */     if ((authType == null) || (authType.length() == 0))
/*      */     {
/*  991 */       authType = "LOCAL";
/*      */     }
/*      */ 
/*  994 */     authType = authType.toUpperCase();
/*  995 */     if (StringUtils.findStringIndex(this.AUTH_TYPE_LIST, authType) < 0)
/*      */     {
/*  997 */       String msg = LocaleUtils.encodeMessage("csUserAuthTypeNotValid", null, authType);
/*      */ 
/*  999 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1003 */     curData.setProperty("dUserAuthType", authType);
/*      */ 
/* 1005 */     String[] userFields = { "dUserOrgPath", "dUserSourceOrgPath", "dUserSourceFlags" };
/*      */ 
/* 1009 */     for (int i = 0; i < userFields.length; ++i)
/*      */     {
/* 1011 */       copyUserValue(curData, newData, userFields[i], hasChanged, cxt);
/*      */     }
/*      */ 
/* 1014 */     int userSourceFlags = NumberUtils.parseInteger(curData.getProperty("dUserSourceFlags"), 0);
/* 1015 */     boolean isSourceFlagSet = false;
/*      */ 
/* 1017 */     DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/* 1018 */     if (drset != null)
/*      */     {
/* 1020 */       String[][] extractedValues = ResultSetUtils.createStringTable(drset, this.EXTRACTED_META_FIELDS);
/* 1021 */       for (int i = 0; i < extractedValues.length; ++i)
/*      */       {
/* 1023 */         boolean skipCopy = false;
/* 1024 */         int overrideBit = NumberUtils.parseInteger(extractedValues[i][2], 0);
/*      */ 
/* 1028 */         if (copyAll)
/*      */         {
/* 1030 */           if (!doAdminFields)
/*      */           {
/* 1032 */             skipCopy = StringUtils.convertToBool(extractedValues[i][1], false);
/*      */           }
/* 1034 */           if ((!skipCopy) && (overrideBit > 0))
/*      */           {
/* 1037 */             String bitVal = newData.getLocal(extractedValues[i][0] + ":override");
/* 1038 */             boolean isBitSet = StringUtils.convertToBool(bitVal, false);
/*      */ 
/* 1040 */             if (isBitSet)
/*      */             {
/* 1042 */               userSourceFlags |= overrideBit;
/*      */             }
/*      */             else
/*      */             {
/* 1046 */               userSourceFlags &= (overrideBit ^ 0xFFFFFFFF);
/*      */             }
/* 1048 */             isSourceFlagSet = true;
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1054 */           skipCopy = (userSourceFlags & overrideBit) != 0;
/*      */         }
/* 1056 */         if (skipCopy)
/*      */           continue;
/* 1058 */         copyUserValueEx(curData, newData, extractedValues[i][0], hasChanged, extractedValues[i][3], extractedValues[i][4], cxt);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1066 */     if (isSourceFlagSet)
/*      */     {
/* 1068 */       Report.trace("userstorage", "Source flag (dUserSourceFlags) is set, will do update query", null);
/* 1069 */       curData.setProperty("dUserSourceFlags", Integer.toString(userSourceFlags));
/* 1070 */       hasChanged[0] = true;
/*      */     }
/*      */     else
/*      */     {
/* 1074 */       String tmp = curData.getProperty("dUserSourceFlags");
/* 1075 */       if ((tmp == null) || (tmp.length() == 0))
/*      */       {
/* 1079 */         curData.setProperty("dUserSourceFlags", "0");
/*      */       }
/*      */     }
/*      */ 
/* 1083 */     addDateIfMissing(curData, "dUserArriveDate", hasChanged);
/*      */ 
/* 1086 */     copyPasswordEncoding(curData, newData, hasChanged);
/*      */ 
/* 1089 */     curData.setProperty("hasChanged", (hasChanged[0] != 0) ? "1" : "0");
/*      */ 
/* 1092 */     if (cxt != null)
/*      */     {
/* 1094 */       cxt.setCachedObject("TargetUserData", curData);
/* 1095 */       cxt.setCachedObject("hasUserChanged", new Boolean(hasChanged[0]));
/*      */     }
/* 1097 */     PluginFilters.filter("auditUserAttributesStore", ws, newData, cxt);
/*      */ 
/* 1100 */     if ((hasChanged[0] == 0) && (!alwaysSave) && (userDataFromDb))
/*      */       return;
/* 1102 */     Report.trace("userstorage", "Doing update hasChanged=" + hasChanged[0] + ", copyAll=" + copyAll + ", alwaysSave=" + alwaysSave + ", userDataFromDb=" + userDataFromDb, null);
/*      */ 
/* 1105 */     String changeDate = newData.getAllowMissing("dUserChangeDate");
/* 1106 */     newData.m_blFieldTypes.put("dUserChangeDate", "date");
/* 1107 */     if ((alwaysSave == true) || (changeDate == null) || (changeDate.length() == 0))
/*      */     {
/* 1109 */       curData.setProperty("dUserChangeDate", LocaleUtils.formatODBC(new Date()));
/*      */     }
/*      */     else
/*      */     {
/* 1113 */       copyUserValue(curData, newData, "dUserChangeDate", hasChanged, cxt);
/*      */     }
/*      */ 
/* 1121 */     Properties userProps = curData.getProperties();
/* 1122 */     PropParameters paramProps = new PropParameters(userProps);
/* 1123 */     if (PluginFilters.filter("updateUserDatabaseProfileData", ws, newData, cxt) == -1)
/*      */     {
/* 1125 */       return;
/*      */     }
/* 1127 */     updateDatabase(ws, curData, paramProps);
/*      */ 
/* 1129 */     curData.setProperty("userDataFromDb", "true");
/*      */ 
/* 1137 */     UserData cachedUser = this.m_userTempCache.getCachedUserData(curData.m_name);
/* 1138 */     if ((cachedUser == null) || (cachedUser.m_isExpired))
/*      */       return;
/* 1140 */     cachedUser.setProperties(userProps);
/*      */   }
/*      */ 
/*      */   public synchronized void updateDatabase(Workspace ws, UserData curData, PropParameters paramProps)
/*      */     throws DataException
/*      */   {
/* 1149 */     ResultSet rs = this.m_workspace.createResultSet("Quser", paramProps);
/*      */     String queryToExecute;
/*      */     String queryToExecute;
/* 1150 */     if (rs.isEmpty())
/*      */     {
/* 1152 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1154 */         printThreadMsg("Inserting database user entry for " + curData.m_name);
/*      */       }
/* 1156 */       queryToExecute = "Iuser";
/*      */     }
/*      */     else
/*      */     {
/* 1161 */       queryToExecute = "Uuser";
/* 1162 */       String curName = ResultSetUtils.getValue(rs, "dName");
/* 1163 */       curData.setName(curName);
/* 1164 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1166 */         printThreadMsg("Updating database user entry for " + curName);
/*      */       }
/*      */     }
/* 1169 */     DataException origException = null;
/*      */     try
/*      */     {
/* 1172 */       this.m_workspace.execute(queryToExecute, paramProps);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1176 */       origException = e;
/*      */     }
/*      */ 
/* 1184 */     if ((origException != null) && 
/* 1186 */       (queryToExecute.equals("Iuser")))
/*      */     {
/* 1189 */       rs = this.m_workspace.createResultSet("Quser", paramProps);
/* 1190 */       if (rs.isEmpty())
/*      */       {
/* 1193 */         throw origException;
/*      */       }
/* 1195 */       String curName = ResultSetUtils.getValue(rs, "dName");
/* 1196 */       curData.setName(curName);
/*      */ 
/* 1198 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1200 */         printThreadMsg("Inserting database user entry failed for " + curName + ", retry as update.:");
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 1205 */         this.m_workspace.execute("Uuser", paramProps);
/*      */ 
/* 1208 */         origException = null;
/*      */       }
/*      */       catch (DataException d)
/*      */       {
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1216 */     if (origException == null)
/*      */       return;
/* 1218 */     throw origException;
/*      */   }
/*      */ 
/*      */   public Provider retrieveDefaultProvider(UserData userData)
/*      */     throws DataException
/*      */   {
/* 1224 */     Provider result = null;
/*      */ 
/* 1226 */     String authType = userData.getProperty("dUserAuthType");
/* 1227 */     if ((authType != null) && (authType.equalsIgnoreCase("LOCAL")))
/*      */     {
/* 1229 */       return result;
/*      */     }
/*      */ 
/* 1233 */     String sourceOrg = userData.getProperty("dUserSourceOrgPath");
/*      */ 
/* 1235 */     Vector providers = Providers.getProviderList();
/* 1236 */     for (int i = 0; i < providers.size(); ++i)
/*      */     {
/* 1238 */       Provider provider = (Provider)providers.elementAt(i);
/* 1239 */       Object providerObj = provider.getProvider();
/* 1240 */       if (!providerObj instanceof UserProvider)
/*      */         continue;
/* 1242 */       DataBinder provData = provider.getProviderData();
/* 1243 */       String sourcePath = provData.getLocal("SourcePath");
/* 1244 */       if ((sourcePath == null) || (!sourcePath.equalsIgnoreCase(sourceOrg)))
/*      */         continue;
/* 1246 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1248 */         printThreadMsg("Provider " + provider.getName() + " matches dUserSourceOrgPath");
/*      */       }
/* 1250 */       result = provider;
/* 1251 */       break;
/*      */     }
/*      */ 
/* 1255 */     return result;
/*      */   }
/*      */ 
/*      */   public Vector createUserProviderList()
/*      */     throws DataException
/*      */   {
/* 1263 */     Vector result = new IdcVector();
/* 1264 */     Vector providers = Providers.getPrioritizedProviderList();
/* 1265 */     for (int i = 0; i < providers.size(); ++i)
/*      */     {
/* 1267 */       Provider provider = (Provider)providers.elementAt(i);
/* 1268 */       Object providerObj = provider.getProvider();
/* 1269 */       if (!providerObj instanceof UserProvider)
/*      */         continue;
/* 1271 */       printThreadMsg("Adding " + provider.getName());
/* 1272 */       result.addElement(provider);
/*      */     }
/*      */ 
/* 1276 */     printThreadMsg("Returning " + result.size() + " results");
/* 1277 */     return result;
/*      */   }
/*      */ 
/*      */   public void synchronizeOptionLists(DataBinder binder, boolean hasNewData, boolean loadBinder)
/*      */     throws ServiceException
/*      */   {
/* 1290 */     DataBinder curOptLists = null;
/* 1291 */     String userCacheDir = null;
/* 1292 */     boolean isReserved = false;
/*      */     try
/*      */     {
/* 1295 */       String optionsFileName = "useroptions.hda";
/* 1296 */       Vector optLists = null;
/*      */ 
/* 1299 */       if ((loadBinder == true) && (!hasNewData))
/*      */       {
/* 1302 */         optLists = SharedObjects.getOptList("Users:optionLists");
/* 1303 */         if ((optLists == null) || (binder == null))
/*      */         {
/*      */           return;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1311 */         curOptLists = new DataBinder();
/* 1312 */         userCacheDir = DirectoryLocator.getUserCacheDir() + "config/";
/* 1313 */         FileUtils.checkOrCreateDirectoryPrepareForLocks(userCacheDir, 1, true);
/* 1314 */         FileUtils.reserveDirectory(userCacheDir);
/* 1315 */         isReserved = true;
/* 1316 */         ResourceUtils.serializeDataBinder(userCacheDir, optionsFileName, curOptLists, false, false);
/*      */ 
/* 1318 */         optLists = curOptLists.getOptionList("Users:optionLists");
/*      */       }
/* 1320 */       boolean saveFile = hasNewData;
/*      */ 
/* 1323 */       if (optLists == null)
/*      */       {
/* 1325 */         if (curOptLists == null) {
/*      */           return;
/*      */         }
/*      */ 
/* 1329 */         optLists = new IdcVector();
/* 1330 */         curOptLists.addOptionList("Users:optionLists", optLists);
/* 1331 */         saveFile = true;
/*      */       }
/*      */ 
/* 1335 */       if ((hasNewData == true) && (curOptLists != null) && (binder != null))
/*      */       {
/* 1337 */         Enumeration newLists = binder.getOptionLists();
/* 1338 */         while (newLists.hasMoreElements())
/*      */         {
/* 1340 */           String optKey = (String)newLists.nextElement();
/* 1341 */           Vector newList = binder.getOptionList(optKey);
/* 1342 */           int n = optLists.size();
/* 1343 */           boolean foundList = false;
/* 1344 */           for (int i = 0; i < n; ++i)
/*      */           {
/* 1346 */             String elt = (String)optLists.elementAt(i);
/* 1347 */             if (!elt.equals(optKey))
/*      */               continue;
/* 1349 */             foundList = true;
/*      */           }
/*      */ 
/* 1352 */           if (!foundList)
/*      */           {
/* 1354 */             optLists.addElement(optKey);
/*      */           }
/* 1356 */           curOptLists.addOptionList(optKey, newList);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1362 */       SharedObjects.putOptList("Users:optionLists", optLists);
/* 1363 */       for (int i = 0; i < optLists.size(); ++i)
/*      */       {
/* 1365 */         String optKey = (String)optLists.elementAt(i);
/* 1366 */         Vector opts = null;
/* 1367 */         if (curOptLists != null)
/*      */         {
/* 1369 */           opts = curOptLists.getOptionList(optKey);
/* 1370 */           if (opts == null)
/*      */           {
/* 1372 */             opts = new IdcVector();
/* 1373 */             curOptLists.addOptionList(optKey, opts);
/* 1374 */             saveFile = true;
/*      */           }
/* 1376 */           SharedObjects.putOptList(optKey, opts);
/*      */         }
/*      */         else
/*      */         {
/* 1380 */           opts = SharedObjects.getOptList(optKey);
/*      */         }
/* 1382 */         if ((loadBinder != true) || (opts == null) || (binder == null))
/*      */           continue;
/* 1384 */         binder.addOptionList(optKey, opts);
/*      */       }
/*      */ 
/* 1389 */       if (saveFile)
/*      */       {
/* 1391 */         ResourceUtils.serializeDataBinder(userCacheDir, optionsFileName, curOptLists, true, false);
/*      */       }
/*      */ 
/* 1395 */       synchronizeLocaleOptionLists(binder, hasNewData, loadBinder);
/*      */     }
/*      */     finally
/*      */     {
/* 1399 */       if (isReserved)
/*      */       {
/* 1401 */         FileUtils.releaseDirectory(userCacheDir);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public UserData getUserData(String name)
/*      */   {
/* 1414 */     Users users = (Users)SharedObjects.getTable("Users");
/* 1415 */     UserData userData = users.getLocalUserData(name);
/* 1416 */     if (userData == null)
/*      */     {
/* 1418 */       userData = this.m_userTempCache.getCachedUserData(name);
/*      */     }
/* 1420 */     return userData;
/*      */   }
/*      */ 
/*      */   public void printThreadMsg(String msg)
/*      */   {
/* 1425 */     Report.trace("userstorage", msg, null);
/*      */   }
/*      */ 
/*      */   public void copyUserValue(UserData curData, DataBinder newData, String key, boolean[] hasChanged, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1438 */     copyUserValueEx(curData, newData, key, hasChanged, null, null, cxt);
/*      */   }
/*      */ 
/*      */   public void copyUserValueEx(UserData curData, DataBinder newData, String key, boolean[] hasChanged, String type, String caption, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1445 */     String curVal = curData.getProperty(key);
/* 1446 */     String newVal = newData.getAllowMissing(key);
/*      */ 
/* 1448 */     if (newVal != null)
/*      */     {
/* 1450 */       if (type != null)
/*      */       {
/* 1452 */         validateUserField(newVal, type, caption, cxt);
/*      */       }
/*      */ 
/* 1455 */       newVal = newData.convertToSystem(key, newVal);
/* 1456 */       curData.setProperty(key, newVal);
/* 1457 */       if ((hasChanged == null) || (hasChanged.length <= 0) || (
/* 1459 */         (curVal != null) && (curVal.equals(newVal))))
/*      */         return;
/* 1461 */       Report.trace("userstorage", "Value changed for " + key + ", curVal=" + curVal + ", newVal=" + newVal, null);
/* 1462 */       hasChanged[0] = true;
/*      */     }
/*      */     else
/*      */     {
/* 1469 */       if (curVal != null)
/*      */         return;
/* 1471 */       Report.trace("userstorage", "Value changed for " + key + ", setting empty by default", null);
/* 1472 */       hasChanged[0] = true;
/* 1473 */       curData.setProperty(key, "");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void copyPasswordEncoding(UserData curData, DataBinder newData, boolean[] hasChanged)
/*      */   {
/* 1481 */     Users users = (Users)SharedObjects.getTable("Users");
/*      */ 
/* 1484 */     String enc = newData.getAllowMissing("dPasswordEncoding");
/* 1485 */     String psswd = newData.getAllowMissing("dPassword");
/*      */ 
/* 1488 */     if ((psswd != null) && (psswd.equals(Users.getPasswordDash()) == true))
/*      */     {
/* 1490 */       psswd = null;
/* 1491 */       enc = null;
/*      */     }
/*      */ 
/* 1494 */     if ((((enc == null) || (enc.length() == 0))) && (psswd != null) && (curData.m_name != null))
/*      */     {
/* 1498 */       enc = newData.getAllowMissing("desiredPasswordEncoding");
/* 1499 */       if (enc == null)
/*      */       {
/* 1501 */         enc = users.getDefaultPasswordEncoding();
/*      */       }
/* 1503 */       psswd = UserUtils.encodePassword(curData.m_name, psswd, enc);
/*      */     }
/*      */ 
/* 1507 */     String curPassword = curData.getProperty("dPassword");
/* 1508 */     if ((((curPassword == null) || (curPassword.length() == 0))) && (((psswd == null) || (psswd.length() == 0))))
/*      */     {
/* 1511 */       psswd = "*";
/* 1512 */       enc = "SHA1-CB";
/*      */     }
/*      */ 
/* 1515 */     if (psswd == null)
/*      */       return;
/* 1517 */     if ((hasChanged != null) && (hasChanged.length > 0))
/*      */     {
/* 1519 */       hasChanged[0] = true;
/*      */     }
/* 1521 */     curData.setProperty("dPassword", psswd);
/* 1522 */     if (enc == null)
/*      */     {
/* 1524 */       enc = "";
/*      */     }
/* 1526 */     curData.setProperty("dPasswordEncoding", enc);
/*      */   }
/*      */ 
/*      */   public void validateUserField(String value, String type, String caption, ExecutionContext cxt)
/*      */     throws ServiceException
/*      */   {
/* 1533 */     String errResourceId = null;
/* 1534 */     Exception err = null;
/*      */ 
/* 1536 */     if (!value.equals(""))
/*      */     {
/* 1538 */       if (type.equalsIgnoreCase("Date"))
/*      */       {
/*      */         try
/*      */         {
/* 1542 */           LocaleResources.parseDate(value, cxt);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1546 */           err = e;
/* 1547 */           errResourceId = "csInvalidUserDate";
/*      */         }
/*      */       }
/* 1550 */       else if (type.equals("Int"))
/*      */       {
/* 1552 */         int code = Validation.checkInteger(value);
/* 1553 */         if ((code != 0) && (code != -1))
/*      */         {
/* 1555 */           err = new Exception("!csIntegerInvalidChars");
/* 1556 */           errResourceId = "csInvalidInt";
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1561 */     if (errResourceId == null)
/*      */       return;
/* 1563 */     String msg = LocaleUtils.encodeMessage(errResourceId, null, caption);
/* 1564 */     throw new ServiceException(msg, err);
/*      */   }
/*      */ 
/*      */   public void addDateIfMissing(UserData curData, String key, boolean[] hasChanged)
/*      */   {
/* 1570 */     String curVal = curData.getProperty(key);
/* 1571 */     if ((curVal != null) && (curVal.length() != 0))
/*      */       return;
/* 1573 */     curVal = LocaleUtils.formatODBC(new Date());
/* 1574 */     curData.setProperty(key, curVal);
/* 1575 */     if ((hasChanged == null) || (hasChanged.length <= 0))
/*      */       return;
/* 1577 */     Report.trace("userstorage", "Value changed for " + key + ", supplied current date as default", null);
/* 1578 */     hasChanged[0] = true;
/*      */   }
/*      */ 
/*      */   public void synchronizeLocaleOptionLists(DataBinder binder, boolean hasNewData, boolean loadBinder)
/*      */     throws ServiceException
/*      */   {
/* 1586 */     DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/* 1587 */     if (drset == null) {
/*      */       return;
/*      */     }
/* 1590 */     String[] fields = { "umdName", "umdIsOptionList", "umdOptionListType", "umdOptionListKey" };
/*      */     try
/*      */     {
/* 1593 */       String[][] table = ResultSetUtils.createStringTable(drset, fields);
/* 1594 */       if (table == null)
/*      */       {
/* 1596 */         return;
/*      */       }
/*      */ 
/* 1599 */       for (int i = 0; i < table.length; ++i)
/*      */       {
/* 1601 */         String[] values = table[i];
/*      */ 
/* 1603 */         boolean isOptList = StringUtils.convertToBool(values[1], false);
/* 1604 */         String source = null;
/*      */ 
/* 1606 */         if (!isOptList)
/*      */           continue;
/* 1608 */         Vector optList = new IdcVector();
/* 1609 */         Vector v = StringUtils.parseArray(values[2], ',', '^');
/* 1610 */         if (v == null) continue; if (v.size() <= 1) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1614 */         source = (String)v.elementAt(1);
/* 1615 */         if ((source == null) || (!source.equalsIgnoreCase("locale")))
/*      */           continue;
/* 1617 */         String[] lcfields = { "lcLocaleId", "lcIsEnabled" };
/* 1618 */         DataResultSet localeSet = SharedObjects.getTable("LocaleConfig");
/* 1619 */         String[][] localeTable = ResultSetUtils.createStringTable(localeSet, lcfields);
/* 1620 */         if (localeTable == null)
/*      */         {
/* 1622 */           return;
/*      */         }
/*      */ 
/* 1625 */         for (int j = 0; j < localeTable.length; ++j)
/*      */         {
/* 1627 */           String[] locale = localeTable[j];
/* 1628 */           boolean isEnabled = StringUtils.convertToBool(locale[1], false);
/* 1629 */           if ((!isEnabled) || (locale[0] == null) || (locale[0].length() <= 0))
/*      */             continue;
/* 1631 */           optList.addElement(locale[0]);
/*      */         }
/*      */ 
/* 1635 */         if ((optList.size() <= 0) || (values[3] == null) || (values[3].length() <= 0))
/*      */           continue;
/* 1637 */         if ((loadBinder == true) && (binder != null))
/*      */         {
/* 1639 */           binder.addOptionList(values[3], optList);
/*      */         }
/* 1641 */         SharedObjects.putOptList(values[3], optList);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1649 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1656 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102518 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserStorageImplementor
 * JD-Core Version:    0.5.4
 */