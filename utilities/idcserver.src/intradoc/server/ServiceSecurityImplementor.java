/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.CryptoCommonUtils;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SessionUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.IncomingConnection;
/*      */ import intradoc.provider.ProxyConnectionUtils;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserAttribInfo;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import java.util.Date;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ServiceSecurityImplementor
/*      */   implements SecurityImplementor
/*      */ {
/*      */   public static final int F_IS_LOGGED_IN = 1;
/*   35 */   protected DocumentAccessSecurity m_docAccessSecurity = null;
/*      */ 
/*      */   public void init()
/*      */     throws ServiceException
/*      */   {
/*   44 */     String DASclassName = SharedObjects.getEnvironmentValue("DocumentAccessSecurityClass");
/*   45 */     if (DASclassName == null)
/*      */     {
/*   47 */       DASclassName = "intradoc.server.DocumentAccessSecurity";
/*      */     }
/*   49 */     Object obj = ComponentClassFactory.createClassInstance("DocumentAccessSecurity", DASclassName, "!csDocumentAccessSecurityError");
/*      */ 
/*   51 */     this.m_docAccessSecurity = ((DocumentAccessSecurity)obj);
/*      */ 
/*   53 */     this.m_docAccessSecurity.m_securityImpl = this;
/*      */   }
/*      */ 
/*      */   public void globalSecurityCheck(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*   61 */     boolean userAlreadyAssigned = computeInitialUserState(service, binder);
/*      */ 
/*   65 */     if (!service.getUseSecurity())
/*      */     {
/*   67 */       return;
/*      */     }
/*      */ 
/*   70 */     String browserAuthType = service.getBrowserAuthType();
/*   71 */     String loginState = service.getLoginState();
/*   72 */     ServiceData serviceData = service.getServiceData();
/*   73 */     String newAuthType = binder.getLocal("Auth");
/*   74 */     boolean isAutoLogin = false;
/*      */ 
/*   86 */     if (serviceData.m_accessLevel != 0)
/*      */     {
/*   88 */       isAutoLogin = loginState.equals("1");
/*   89 */       if ((!isAutoLogin) && (browserAuthType != null))
/*      */       {
/*   91 */         isAutoLogin = (browserAuthType.equalsIgnoreCase("Intranet")) || (browserAuthType.equalsIgnoreCase("NTLM"));
/*      */       }
/*      */     }
/*      */ 
/*   95 */     if ((!isAutoLogin) && (binder.getLocal("monitoredSubjects") != null))
/*      */     {
/*   97 */       isAutoLogin = true;
/*      */     }
/*      */ 
/*  102 */     ProxyConnectionUtils.copyOverProxyHeaders(binder, service);
/*      */ 
/*  105 */     computeInitialProxyingFlags(service, binder);
/*      */ 
/*  111 */     UserData userData = null;
/*  112 */     String user = null;
/*  113 */     if (!userAlreadyAssigned)
/*      */     {
/*  115 */       if (!loginState.equals("0"))
/*      */       {
/*  117 */         user = determineUser(service, binder);
/*  118 */         if (user != null)
/*      */         {
/*  120 */           if (DataBinderUtils.getBoolean(binder, "IsExternalLogout", false))
/*      */           {
/*  123 */             service.setLoginState("1");
/*      */           }
/*  125 */           binder.setEnvironmentValue("ORIGINAL_USER", user);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  132 */       userData = service.getUserData();
/*  133 */       user = userData.m_name;
/*      */     }
/*      */ 
/*  137 */     user = determineUserProxyFlags(service, binder, user);
/*      */ 
/*  148 */     boolean actAsAnon = DataBinderUtils.getBoolean(binder, "ActAsAnonymous", false);
/*  149 */     if (!actAsAnon)
/*      */     {
/*  151 */       String sType = binder.getEnvironmentValue("SECURITY_TYPE");
/*  152 */       if ((sType != null) && (sType.indexOf("anonymous") >= 0))
/*      */       {
/*  154 */         actAsAnon = true;
/*      */       }
/*      */     }
/*  157 */     service.setConditionVar("ActAsAnonymous", actAsAnon);
/*  158 */     boolean isLoggedIn = (!actAsAnon) && (user != null) && (!user.isEmpty()) && (!user.equalsIgnoreCase("anonymous"));
/*      */ 
/*  160 */     if (isLoggedIn)
/*      */     {
/*  164 */       service.getUserData().m_name = user;
/*      */     }
/*      */ 
/*  172 */     if ((!SharedObjects.getEnvValueAsBoolean("DisableAuthorizationTokenCheck", false)) && 
/*  174 */       (binder.m_isStandardHttpRequest))
/*      */     {
/*  176 */       if (isLoggedIn)
/*      */       {
/*  178 */         createAuthorizationToken(service, binder);
/*      */       }
/*  180 */       if ((serviceData.m_accessLevel & 0x20) == 0)
/*      */       {
/*  182 */         String token = binder.getLocal("idcToken");
/*  183 */         if (((!isLoggedIn) && (token == null)) || (!service.isConditionVarTrue("SkipIdcTokenValidation")))
/*      */         {
/*  186 */           validateAuthorizationToken(token, service, binder, (isLoggedIn) ? 1 : 0);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  192 */     if ((user == null) || (user.length() == 0) || (user.equals("anonymous")))
/*      */     {
/*  194 */       if (!actAsAnon)
/*      */       {
/*  196 */         boolean isHeavyClient = service.isClientControlled();
/*  197 */         boolean forceLogin = DataBinderUtils.getLocalBoolean(binder, "forceLogin", false);
/*      */ 
/*  203 */         if ((isHeavyClient) || (isAutoLogin) || (forceLogin) || ((serviceData.m_accessLevel & 0x8) != 0))
/*      */         {
/*  206 */           service.setPromptForLogin(true);
/*  207 */           service.createServiceExceptionEx(null, "!csSystemNeedsLogin", -20);
/*      */         }
/*      */       }
/*      */ 
/*  211 */       user = "anonymous";
/*      */     }
/*  213 */     binder.putLocal("dUser", user);
/*      */ 
/*  216 */     if (newAuthType != null)
/*      */     {
/*  218 */       service.checkForceLogin();
/*      */     }
/*      */ 
/*  223 */     if (userData == null)
/*      */     {
/*  226 */       loadUserData(user, service, binder);
/*  227 */       userData = service.getUserData();
/*      */     }
/*      */ 
/*  234 */     if (SharedObjects.getEnvValueAsBoolean("EnableImpersonation", true))
/*      */     {
/*  236 */       String impersonateUser = binder.getAllowMissing("RunAs");
/*  237 */       boolean validatedImpersonator = false;
/*  238 */       int filterReturnVal = 0;
/*      */ 
/*  240 */       if ((impersonateUser != null) && (impersonateUser.length() > 0))
/*      */       {
/*  242 */         UserData impersonatedUserData = null;
/*      */         try
/*      */         {
/*  245 */           impersonatedUserData = UserStorage.retrieveUserDatabaseProfileData(impersonateUser, service.m_workspace, service);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  249 */           String msg = LocaleUtils.encodeMessage("csUnableToRetrieveUserInfo", null, impersonateUser);
/*  250 */           throw new ServiceException(-16, msg);
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  258 */           service.setCachedObject("impersonatedUserData", impersonatedUserData);
/*  259 */           filterReturnVal = PluginFilters.filter("validateImpersonationPermission", service.m_workspace, binder, service);
/*      */ 
/*  261 */           if (filterReturnVal == -1)
/*      */           {
/*  263 */             String msg = LocaleUtils.encodeMessage("csUnableToImpersonateUser", null, impersonateUser);
/*  264 */             throw new ServiceException(-20, msg);
/*      */           }
/*      */ 
/*  267 */           if (filterReturnVal == 1)
/*      */           {
/*  269 */             validatedImpersonator = true;
/*      */           }
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*  274 */           String msg = LocaleUtils.encodeMessage("csFilterError", e.getMessage(), "validateImpersonationPermission");
/*      */ 
/*  276 */           service.createServiceException(e, msg);
/*      */         }
/*      */       }
/*      */ 
/*  280 */       if (filterReturnVal == 0)
/*      */       {
/*  282 */         Vector roleList = userData.getAttributes("role");
/*  283 */         String impersonatorRole = SharedObjects.getEnvironmentValue("ImpersonatorRole");
/*  284 */         if ((impersonatorRole == null) || (impersonatorRole.trim().length() < 1))
/*      */         {
/*  286 */           impersonatorRole = "admin";
/*      */         }
/*      */ 
/*  289 */         for (int i = 0; (roleList != null) && (i < roleList.size()); ++i)
/*      */         {
/*  291 */           UserAttribInfo tempRole = (UserAttribInfo)roleList.elementAt(i);
/*  292 */           if (!tempRole.m_attribName.equals(impersonatorRole))
/*      */             continue;
/*  294 */           validatedImpersonator = true;
/*  295 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  300 */       if (validatedImpersonator)
/*      */       {
/*  302 */         String stickyImpersonationString = binder.getAllowMissing("StickyImpersonation");
/*      */ 
/*  304 */         Object IdcServletRequestContext = service.getCachedObject("IdcServletRequestContext");
/*  305 */         if (IdcServletRequestContext != null)
/*      */         {
/*  307 */           if (stickyImpersonationString != null)
/*      */           {
/*  310 */             SessionUtils.setSessionAttribute(IdcServletRequestContext, "StickyImpersonation", stickyImpersonationString);
/*      */           }
/*      */           else
/*      */           {
/*  314 */             stickyImpersonationString = SessionUtils.getSessionAttributeAsString(IdcServletRequestContext, "StickyImpersonation");
/*      */           }
/*      */ 
/*  317 */           if (StringUtils.convertToBool(stickyImpersonationString, false))
/*      */           {
/*      */             try
/*      */             {
/*  321 */               if (impersonateUser == null)
/*      */               {
/*  323 */                 impersonateUser = SessionUtils.getSessionAttributeAsString(IdcServletRequestContext, "RunAs");
/*      */               }
/*      */               else
/*      */               {
/*  327 */                 SessionUtils.setSessionAttribute(IdcServletRequestContext, "RunAs", impersonateUser);
/*      */               }
/*      */             }
/*      */             catch (Exception e)
/*      */             {
/*  332 */               throw new ServiceException("csImpersonationError", e);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*  337 */         if (impersonateUser != null)
/*      */         {
/*  339 */           String originalUser = binder.getLocal("dUser");
/*  340 */           binder.setEnvironmentValue("OriginalUser", originalUser);
/*  341 */           service.setCachedObject("OriginalUser", originalUser);
/*  342 */           service.setCachedObject("OriginalUserData", userData);
/*  343 */           binder.putLocal("dUser", impersonateUser);
/*  344 */           user = impersonateUser;
/*  345 */           service.m_userData = null;
/*  346 */           loadUserData(impersonateUser, service, binder);
/*  347 */           userData = service.getUserData();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  359 */     validateRemoteClientConnectionPrivilege(service, binder);
/*      */     try
/*      */     {
/*  375 */       alterUserCredentials(userData, service, binder);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  379 */       throw new ServiceException("!csFailedToModifyUserCredentials", e);
/*      */     }
/*      */ 
/*  383 */     boolean isAdmin = false;
/*      */     try
/*      */     {
/*  386 */       int bestPrivilege = determinePrivilege(service, binder, userData, true);
/*      */ 
/*  388 */       service.setPrivilege(bestPrivilege);
/*      */ 
/*  392 */       isAdmin = SecurityUtils.isUserOfRole(userData, "admin");
/*      */ 
/*  398 */       boolean adminAtLeastOneGroup = (bestPrivilege & 0x8) != 0;
/*  399 */       service.setConditionVar("AdminAtLeastOneGroup", adminAtLeastOneGroup);
/*  400 */       service.setConditionVar("IsContributor", (bestPrivilege & 0x2) != 0);
/*      */ 
/*  403 */       boolean isSubAdmin = false;
/*  404 */       if (isAdmin)
/*      */       {
/*  406 */         isSubAdmin = true;
/*      */       }
/*  408 */       else if (SecurityUtils.determineGroupPrivilege(userData, "#AppsGroup") != 0)
/*      */       {
/*  410 */         isSubAdmin = true;
/*      */       }
/*  412 */       service.setConditionVar("IsSubAdmin", isSubAdmin);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  417 */       service.createServiceException(e, null);
/*      */     }
/*      */ 
/*  420 */     service.setConditionVar("IsAdmin", isAdmin);
/*      */ 
/*  423 */     if (isAdmin)
/*      */     {
/*  425 */       int priv = service.getPrivilege();
/*  426 */       priv |= 8;
/*  427 */       service.setPrivilege(priv);
/*      */     }
/*      */ 
/*  431 */     boolean isProxiedRequest = service.isConditionVarTrue("IsProxiedRequest");
/*  432 */     boolean isProxiedUser = service.isConditionVarTrue("IsProxiedUser");
/*      */ 
/*  434 */     if ((isProxiedUser) || (!isProxiedRequest))
/*      */     {
/*  439 */       boolean canUseAdminServer = ((user.equals("sysadmin")) && (isAdmin)) || (SecurityUtils.isUserOfRole(userData, "sysmanager"));
/*  440 */       service.setConditionVar("IsSysManager", canUseAdminServer);
/*      */     }
/*      */ 
/*  457 */     boolean hasDifferentiatedRoles = SharedObjects.getTable("RoleDefinition") != null;
/*      */ 
/*  459 */     if ((hasDifferentiatedRoles) && (binder.m_isGet) && (SharedObjects.getEnvValueAsBoolean("EnableSecuredGets", false)) && (serviceData.m_accessLevel != 0) && ((serviceData.m_accessLevel & 0x20) == 0))
/*      */     {
/*  464 */       service.createServiceException(null, "!csRequestRequiresPost");
/*      */     }
/*      */ 
/*  469 */     if ((serviceData.m_accessLevel & 0x10) == 0)
/*      */     {
/*  473 */       int toCheck = 14;
/*      */ 
/*  475 */       if (((serviceData.m_accessLevel & toCheck) != 0) && (SharedObjects.getEnvValueAsBoolean("CheckServiceAccessLevels", false)))
/*      */       {
/*  477 */         this.m_docAccessSecurity.m_ensureSecurityCheck = true;
/*      */       }
/*  479 */       return;
/*      */     }
/*      */ 
/*  482 */     if (((serviceData.m_accessLevel & 0x8) != 0) && 
/*  484 */       (!isAdmin))
/*      */     {
/*  486 */       service.setPromptForLogin(false);
/*  487 */       String errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess", null, user);
/*      */ 
/*  489 */       service.createServiceException(null, errMsg);
/*      */     }
/*      */ 
/*  493 */     validateSecurityPrivilegeLevel(userData, service, binder, true);
/*      */   }
/*      */ 
/*      */   public void checkSecurity(Service service, DataBinder binder, ResultSet rset)
/*      */     throws ServiceException, DataException
/*      */   {
/*  500 */     this.m_docAccessSecurity.checkSecurity(service, binder, rset);
/*      */   }
/*      */ 
/*      */   public boolean checkAccess(Service service, DataBinder binder, ResultSet rset, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/*  507 */     return this.m_docAccessSecurity.checkAccess(service, binder, rset, desiredPriv);
/*      */   }
/*      */ 
/*      */   public void checkMetaChangeSecurity(Service service, DataBinder binder, ResultSet oldSet, boolean isNewDoc)
/*      */     throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  518 */       Object priorDocInfo = oldSet;
/*  519 */       if (priorDocInfo == null)
/*      */       {
/*  521 */         priorDocInfo = "";
/*      */       }
/*      */ 
/*  524 */       service.setCachedObject("checkMeta:priorDocInfo", priorDocInfo);
/*  525 */       Boolean isNewDocBool = (isNewDoc) ? Boolean.TRUE : Boolean.FALSE;
/*  526 */       service.setCachedObject("checkMeta:isNewDoc", isNewDocBool);
/*  527 */       if (PluginFilters.filter("checkMetaChangeSecurity", service.getWorkspace(), binder, service) == 0)
/*      */       {
/*  530 */         this.m_docAccessSecurity.checkMetaChangeSecurity(service, binder, oldSet, isNewDoc);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  537 */       if ((e.m_errorCode != -20) && (e.m_errorCode != -21))
/*      */       {
/*  539 */         e.m_errorCode = -18;
/*      */       }
/*  541 */       throw e;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void computeInitialProxyingFlags(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  548 */     if (binder.getEnvironmentValue("HTTP_RELATIVEURL") == null)
/*      */       return;
/*  550 */     service.setConditionVar("IsProxiedRequest", true);
/*  551 */     binder.setEnvironmentValue("IsProxiedServer", "1");
/*      */   }
/*      */ 
/*      */   public String determineUser(Service service, DataBinder binder)
/*      */   {
/*  566 */     if (checkForcedAnonymous(service, binder))
/*      */     {
/*  568 */       return null;
/*      */     }
/*      */ 
/*  571 */     String user = binder.getEnvironmentValue("HTTP_INTERNETUSER");
/*  572 */     if (user == null)
/*      */     {
/*  574 */       user = binder.getEnvironmentValue("REMOTE_USER");
/*      */     }
/*      */ 
/*  577 */     if (user == null)
/*      */     {
/*  579 */       return null;
/*      */     }
/*      */ 
/*  582 */     return user;
/*      */   }
/*      */ 
/*      */   public boolean checkForcedAnonymous(Service service, DataBinder binder)
/*      */   {
/*  587 */     String noUser = binder.getLocal("ActAsAnonymous");
/*      */ 
/*  590 */     return (noUser != null) && (noUser.equals("1"));
/*      */   }
/*      */ 
/*      */   public String determineUserProxyFlags(Service service, DataBinder binder, String user)
/*      */   {
/*  597 */     if (user != null)
/*      */     {
/*  599 */       binder.setEnvironmentValue("ORIGINAL_USER", user);
/*      */ 
/*  602 */       boolean isProxiedUser = service.isConditionVarTrue("IsProxiedRequest");
/*  603 */       int index = user.indexOf(47);
/*  604 */       if (index > 0)
/*      */       {
/*  606 */         String relativeRoot = "/" + user.substring(0, index + 1);
/*  607 */         if (relativeRoot.equalsIgnoreCase(DocumentPathBuilder.getRelativeWebRoot()))
/*      */         {
/*  609 */           service.setConditionVar("IsProxyLocalLogin", true);
/*  610 */           user = user.substring(index + 1);
/*  611 */           isProxiedUser = false;
/*      */         }
/*      */       }
/*  614 */       if (isProxiedUser)
/*      */       {
/*  616 */         service.setConditionVar("IsProxiedUser", true);
/*      */       }
/*      */     }
/*  619 */     return user;
/*      */   }
/*      */ 
/*      */   public void loadUserData(String user, Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  636 */     Properties curBinderParams = binder.getLocalData();
/*  637 */     Properties newBinderParams = new Properties(curBinderParams);
/*  638 */     binder.setLocalData(newBinderParams);
/*      */ 
/*  641 */     int flags = 0;
/*  642 */     if (!service.isConditionVarTrue("IgnoreExternalInfo"))
/*      */     {
/*  644 */       flags = 16;
/*      */     }
/*  646 */     if (service.isConditionVarTrue("IsProxiedRequest"))
/*      */     {
/*  648 */       flags |= 4;
/*  649 */       if (service.isConditionVarTrue("IsProxiedUser"))
/*      */       {
/*  651 */         flags |= 8;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  656 */     if ((user == null) || (user.length() == 0) || (user.equalsIgnoreCase("anonymous")))
/*      */     {
/*  658 */       flags |= 1;
/*      */     }
/*      */ 
/*  662 */     Workspace ws = service.getWorkspace();
/*      */ 
/*  664 */     UserData userData = null;
/*      */     try
/*      */     {
/*  667 */       userData = UserStorageUtils.loadUserData(user, binder, curBinderParams, ws, service, flags);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  671 */       throw new ServiceException("!csUnableToLoadUserInfo", e);
/*      */     }
/*      */ 
/*  674 */     if (ws == null)
/*      */     {
/*  681 */       service.executeFilter("afterLoadNoWorkspaceUserAttributes");
/*      */     }
/*      */ 
/*  685 */     binder.setLocalData(curBinderParams);
/*      */ 
/*  687 */     service.setUserData(userData);
/*      */ 
/*  694 */     service.setCachedObject("ConnectionUserName", userData.m_name);
/*      */   }
/*      */ 
/*      */   public void alterUserCredentials(UserData userData, Service service, DataBinder binder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  701 */     service.setCachedObject("TargetUserData", userData);
/*  702 */     PluginFilters.filter("alterUserCredentials", service.getWorkspace(), binder, service);
/*      */   }
/*      */ 
/*      */   public void storeUserDatabaseProfileData(UserData userData, DataBinder newData, Service service)
/*      */     throws ServiceException
/*      */   {
/*  709 */     if (!checkUserPresent(userData))
/*      */     {
/*  711 */       return;
/*      */     }
/*      */ 
/*  715 */     Workspace ws = service.getWorkspace();
/*  716 */     if (ws == null)
/*      */     {
/*  718 */       throw new ServiceException("!csCannotSaveProfile");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  725 */       UserStorage.storeUserDatabaseProfileData(userData, newData, ws, service);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  730 */       ws.rollbackTran();
/*  731 */       throw new ServiceException("!csCannotStoreProfile", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String determineDocumentWhereClause(UserData userData, Service service, DataBinder binder, int privilege, boolean isVerity)
/*      */     throws DataException, ServiceException
/*      */   {
/*  739 */     return this.m_docAccessSecurity.determineDocumentWhereClause(userData, service, binder, privilege, isVerity);
/*      */   }
/*      */ 
/*      */   public int determinePrivilege(Service service, DataBinder binder, UserData userData, boolean isGlobal)
/*      */     throws DataException, ServiceException
/*      */   {
/*  749 */     if (!service.getUseSecurity())
/*      */     {
/*  752 */       return 15;
/*      */     }
/*      */ 
/*  755 */     String group = null;
/*  756 */     String account = null;
/*  757 */     boolean useAccounts = (SecurityUtils.m_useAccounts) || (SecurityUtils.m_useCollaboration);
/*  758 */     String errMsg = null;
/*  759 */     if (!isGlobal)
/*      */     {
/*  761 */       group = binder.getLocal("dSecurityGroup");
/*      */ 
/*  763 */       if (group == null)
/*      */       {
/*  765 */         errMsg = "!csSecurityGroupNotDefined";
/*      */       }
/*      */ 
/*  768 */       if ((errMsg == null) && (useAccounts))
/*      */       {
/*  770 */         boolean ignoreAccounts = service.isConditionVarTrue("IgnoreAccounts");
/*  771 */         account = binder.getLocal("dDocAccount");
/*  772 */         if ((account == null) && (!ignoreAccounts))
/*      */         {
/*  774 */           errMsg = "!csAccountNotDefined";
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  779 */     if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("checkordetermineaccess")))
/*      */     {
/*  781 */       Report.trace("checkordetermineaccess", "determinePrivilege: group=" + group + "  account=" + account + " useAccounts=" + useAccounts + "  errMsg=" + errMsg, null);
/*      */     }
/*  783 */     if (errMsg != null)
/*      */     {
/*  785 */       service.createServiceException(null, errMsg);
/*      */     }
/*      */ 
/*  788 */     return SecurityUtils.determineGroupPrivilege(userData, group);
/*      */   }
/*      */ 
/*      */   public DocumentAccessSecurity getDocumentAccessSecurity()
/*      */   {
/*  793 */     return this.m_docAccessSecurity;
/*      */   }
/*      */ 
/*      */   protected boolean computeInitialUserState(Service service, DataBinder binder)
/*      */   {
/*  802 */     UserData userData = service.getUserData();
/*  803 */     if ((userData != null) && (userData.m_hasAttributesLoaded))
/*      */     {
/*  805 */       return true;
/*      */     }
/*  807 */     binder.putLocal("dUser", "anonymous");
/*  808 */     userData = UserUtils.createUserData("anonymous");
/*  809 */     userData.checkCreateAttributes(false);
/*  810 */     userData.m_hasAttributesLoaded = true;
/*  811 */     service.setUserData(userData);
/*  812 */     return false;
/*      */   }
/*      */ 
/*      */   protected UserData retrieveUserData(String user, Service service, DataBinder binder) throws ServiceException
/*      */   {
/*  817 */     UserData userData = service.getUserData();
/*  818 */     if ((userData == null) || (!userData.m_name.equalsIgnoreCase(user)))
/*      */     {
/*  820 */       service.fillUserData(user);
/*  821 */       userData = service.getUserData();
/*      */     }
/*  823 */     if (userData == null)
/*      */     {
/*  825 */       String msg = LocaleUtils.encodeMessage("csUserDoesNotHaveProfile", null, user);
/*      */ 
/*  827 */       service.createServiceException(null, msg);
/*      */     }
/*  829 */     return userData;
/*      */   }
/*      */ 
/*      */   protected void validateRemoteClientConnectionPrivilege(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  835 */     Object iObj = service.getCachedObject("IncomingConnection");
/*  836 */     if ((iObj == null) || (!iObj instanceof IncomingConnection))
/*      */       return;
/*  838 */     IncomingConnection incoming = (IncomingConnection)iObj;
/*      */     try
/*      */     {
/*  841 */       incoming.checkRequestAllowed(binder, service);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  845 */       service.createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void validateSecurityPrivilegeLevel(UserData userData, Service service, DataBinder binder, boolean isGlobal)
/*      */     throws ServiceException
/*      */   {
/*  853 */     int priv = service.getPrivilege();
/*  854 */     int accessLevel = service.getServiceData().m_accessLevel;
/*  855 */     String errMsg = null;
/*  856 */     String user = null;
/*      */ 
/*  859 */     accessLevel &= 15;
/*  860 */     if (accessLevel == 0)
/*      */     {
/*  862 */       return;
/*      */     }
/*      */ 
/*  867 */     if ((userData == null) || (userData.m_name == null) || (userData.m_name.length() == 0))
/*      */     {
/*  869 */       errMsg = "!csNoUserDataLoaded";
/*      */     }
/*      */     else
/*      */     {
/*  873 */       user = userData.m_name;
/*      */     }
/*      */ 
/*  876 */     if ((errMsg == null) && ((priv & accessLevel) == 0))
/*      */     {
/*  878 */       if (service.setPromptForLoginIfAnonymous())
/*      */       {
/*  880 */         errMsg = "!csSystemNeedsLogin";
/*      */       }
/*      */       else
/*      */       {
/*  884 */         service.setPromptForLogin(false);
/*  885 */         errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess", null, user);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  890 */     boolean useAccounts = (SecurityUtils.m_useAccounts) || (SecurityUtils.m_useCollaboration);
/*  891 */     boolean ignoreAccounts = service.isConditionVarTrue("IgnoreAccounts");
/*  892 */     if ((errMsg == null) && (useAccounts == true) && (!ignoreAccounts))
/*      */     {
/*  894 */       String account = null;
/*  895 */       if (!isGlobal)
/*      */       {
/*  897 */         account = binder.getLocal("dDocAccount");
/*      */       }
/*  899 */       if (account != null)
/*      */       {
/*  901 */         boolean isAccountAllowed = SecurityUtils.isAccountAccessible(userData, account, accessLevel);
/*      */ 
/*  903 */         if (!isAccountAllowed)
/*      */         {
/*  905 */           if (service.setPromptForLoginIfAnonymous())
/*      */           {
/*  907 */             if (isGlobal)
/*      */             {
/*  909 */               errMsg = "!csSystemNeedsLogin2";
/*      */             }
/*      */             else
/*      */             {
/*  913 */               errMsg = "!csSystemNeedsLogin3";
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  918 */             service.setPromptForLogin(false);
/*  919 */             if (isGlobal)
/*      */             {
/*  921 */               errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess2", null, user);
/*      */             }
/*  926 */             else if (account.trim().length() == 0)
/*      */             {
/*  928 */               errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess3", null, user);
/*      */             }
/*      */             else
/*      */             {
/*  933 */               errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess4", null, user, account);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  942 */     if (errMsg == null)
/*      */       return;
/*  944 */     int errCode = (service.getPromptForLogin()) ? -20 : -18;
/*      */ 
/*  946 */     service.createServiceExceptionEx(null, errMsg, errCode);
/*      */   }
/*      */ 
/*      */   protected boolean checkUserPresent(UserData userData)
/*      */   {
/*  955 */     return (userData != null) && (userData.m_name != null) && (userData.m_name.length() != 0) && (!userData.m_name.equalsIgnoreCase("anonymous"));
/*      */   }
/*      */ 
/*      */   public void createAuthorizationToken(Service service, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  963 */     Date now = new Date();
/*  964 */     long expiration = now.getTime() + 172800000L;
/*  965 */     String pw = SharedObjects.getEnvironmentValue("LongTermHashPassword");
/*  966 */     String key = expiration + ":" + service.getUserData().m_name + ":" + pw;
/*  967 */     String hash = CryptoCommonUtils.hexEncodeStringWithDigest(key, "SHA-256", 32);
/*  968 */     service.setCachedObject("idcToken", expiration + ":" + hash);
/*      */   }
/*      */ 
/*      */   public void validateAuthorizationToken(String token, Service service, DataBinder binder, int flags)
/*      */     throws ServiceException
/*      */   {
/*  974 */     boolean isValid = false;
/*  975 */     boolean isLoggedIn = (flags & 0x1) != 0;
/*  976 */     if ((((token == null) || (token.length() == 0))) && (!isLoggedIn))
/*      */     {
/*  979 */       return;
/*      */     }
/*  981 */     if (token != null)
/*      */     {
/*  985 */       int index = token.indexOf(58);
/*  986 */       if ((index > 0) && (index < token.length() - 1))
/*      */       {
/*      */         try
/*      */         {
/*  990 */           String expirationStr = token.substring(0, index);
/*  991 */           long expiration = Long.parseLong(expirationStr);
/*  992 */           Date now = new Date();
/*  993 */           long time = now.getTime();
/*  994 */           if (expiration > time)
/*      */           {
/*  996 */             String hash = token.substring(index + 1);
/*  997 */             String[] hashPasswordKeys = { "LongTermHashPassword", "NewLongTermHashPassword", "PriorLongTermHashPassword" };
/*      */ 
/* 1003 */             int i = 0;
/* 1004 */             while ((!isValid) && (i < hashPasswordKeys.length))
/*      */             {
/* 1006 */               String pw = SharedObjects.getEnvironmentValue(hashPasswordKeys[i]);
/* 1007 */               if ((pw != null) && (pw.length() > 0))
/*      */               {
/* 1009 */                 String validHash = CryptoCommonUtils.hexEncodeStringWithDigest(expirationStr + ":" + service.getUserData().m_name + ":" + pw, "SHA-256", 32);
/*      */ 
/* 1012 */                 if (hash.equals(validHash))
/*      */                 {
/* 1014 */                   isValid = true;
/*      */                 }
/*      */               }
/*      */ 
/* 1018 */               ++i;
/*      */             }
/*      */           }
/*      */         }
/*      */         catch (NumberFormatException e)
/*      */         {
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1029 */     if (isValid)
/*      */       return;
/* 1031 */     String msg = LocaleUtils.encodeMessage((isLoggedIn) ? "csInvalidAuthorizationToken" : "csInvalidAuthorizationTokenNotLoggedIn", null);
/*      */ 
/* 1033 */     throw new ServiceException(-18, msg);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1039 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99789 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServiceSecurityImplementor
 * JD-Core Version:    0.5.4
 */