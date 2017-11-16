/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.search.ParsedQueryElements;
/*      */ import intradoc.search.QueryElement;
/*      */ import intradoc.search.SearchQueryUtils;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.utils.FileRevisionSelectionUtils;
/*      */ import intradoc.shared.AliasData;
/*      */ import intradoc.shared.Collaborations;
/*      */ import intradoc.shared.CommonSearchConfig;
/*      */ import intradoc.shared.SecurityAccessListUtils;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DocumentAccessSecurity
/*      */ {
/*      */   public ServiceSecurityImplementor m_securityImpl;
/*      */   public CommonSearchConfig m_queryConfig;
/*      */   public ParsedQueryElements m_queryElts;
/*      */   public String m_currentEngine;
/*      */   public boolean m_supportCombinedNotOp;
/*      */   public boolean m_appendQueryTableName;
/*      */   public boolean m_doingDocReferenceSecurity;
/*      */   public Service m_service;
/*      */   public DataBinder m_binder;
/*      */   public ResultSet m_rset;
/*      */   public int m_desiredPriv;
/*      */   public int m_userPriv;
/*      */   public Map m_fieldNameMap;
/*      */   public Hashtable m_columnTableMap;
/*      */   public String m_account;
/*      */   public String m_group;
/*      */   public boolean m_accountAvailable;
/*      */   public UserData m_userData;
/*      */   public Object[] m_filterObjects;
/*      */   public int m_flags;
/*      */   public String m_securityGroupClauseField;
/*      */   public String m_docAccountClauseField;
/*      */   public String m_clbraUserListClauseField;
/*      */   public String m_clbraAliasListClauseField;
/*  104 */   public static boolean m_isMaxSecurityAttributesInitialized = false;
/*  105 */   public static int m_maxSecurityAttributesByType = 50;
/*      */   public static int m_maxSecurityGroupsInSecurityClause;
/*      */   public static int m_maxAccountsInSecurityClause;
/*      */   public static int m_maxAliasesInSecurityClause;
/*      */   public List m_entityList;
/*      */   public boolean m_ensureSecurityCheck;
/*      */   public boolean m_hasCheckedSecurity;
/*      */   public static final int F_USE_DEFAULTS = 0;
/*      */   public static final int F_DETERMINE_ACCESS_PRIVS = 1;
/*      */ 
/*      */   public DocumentAccessSecurity()
/*      */   {
/*   69 */     this.m_securityImpl = null;
/*      */ 
/*   74 */     this.m_appendQueryTableName = false;
/*      */ 
/*   81 */     this.m_doingDocReferenceSecurity = true;
/*      */ 
/*   90 */     this.m_fieldNameMap = null;
/*   91 */     this.m_columnTableMap = null;
/*      */ 
/*  112 */     this.m_ensureSecurityCheck = false;
/*  113 */     this.m_hasCheckedSecurity = false;
/*      */   }
/*      */ 
/*      */   public void initMaxSecurityAttributes()
/*      */   {
/*  120 */     m_maxSecurityAttributesByType = SharedObjects.getEnvironmentInt("MaxSecurityAttributesByType", m_maxSecurityAttributesByType);
/*  121 */     m_maxSecurityGroupsInSecurityClause = SharedObjects.getEnvironmentInt("MaxSecurityGroupsInSecurityClause", m_maxSecurityAttributesByType);
/*  122 */     m_maxAccountsInSecurityClause = SharedObjects.getEnvironmentInt("MaxAccountsInSecurityClause", m_maxSecurityAttributesByType);
/*  123 */     m_maxAliasesInSecurityClause = SharedObjects.getEnvironmentInt("MaxAliasesInSecurityClause", m_maxSecurityAttributesByType);
/*  124 */     m_isMaxSecurityAttributesInitialized = true;
/*      */   }
/*      */ 
/*      */   public void checkSecurity(Service service, DataBinder binder, ResultSet rset)
/*      */     throws ServiceException, DataException
/*      */   {
/*  134 */     this.m_service = service;
/*  135 */     this.m_binder = binder;
/*  136 */     this.m_rset = rset;
/*      */ 
/*  138 */     checkSecurity();
/*      */   }
/*      */ 
/*      */   public void checkSecurity() throws ServiceException, DataException
/*      */   {
/*  143 */     this.m_hasCheckedSecurity = true;
/*      */ 
/*  148 */     ServiceData serviceData = this.m_service.getServiceData();
/*  149 */     if ((!this.m_service.getUseSecurity()) || (serviceData.m_accessLevel == 0))
/*      */     {
/*  151 */       return;
/*      */     }
/*      */ 
/*  154 */     this.m_accountAvailable = true;
/*      */ 
/*  158 */     if (this.m_rset != null)
/*      */     {
/*  160 */       this.m_group = ResultSetUtils.getValue(this.m_rset, "dSecurityGroup");
/*  161 */       if (this.m_group == null)
/*      */       {
/*  163 */         this.m_service.createServiceException(null, "!csSecurityGroupColumnMissing");
/*      */       }
/*      */       else
/*      */       {
/*  167 */         this.m_binder.putLocal("dSecurityGroup", this.m_group);
/*      */       }
/*  169 */       this.m_account = ResultSetUtils.getValue(this.m_rset, "dDocAccount");
/*  170 */       if (this.m_account != null)
/*      */       {
/*  172 */         this.m_binder.putLocal("dDocAccount", this.m_account);
/*      */       }
/*      */       else
/*      */       {
/*  176 */         this.m_accountAvailable = false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  182 */     UserData userData = this.m_service.getUserData();
/*  183 */     this.m_userPriv = this.m_securityImpl.determinePrivilege(this.m_service, this.m_binder, userData, false);
/*      */ 
/*  185 */     this.m_service.setPrivilege(this.m_userPriv);
/*  186 */     boolean isTracingAccessSecurity = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("checkordetermineaccess"));
/*  187 */     if (isTracingAccessSecurity)
/*      */     {
/*  189 */       Report.trace("checkordetermineaccess", new StringBuilder().append("m_userPriv=").append(this.m_userPriv).toString(), null);
/*      */     }
/*      */ 
/*  192 */     this.m_securityImpl.validateSecurityPrivilegeLevel(userData, this.m_service, this.m_binder, false);
/*      */ 
/*  195 */     this.m_desiredPriv = serviceData.m_accessLevel;
/*  196 */     if (checkMetaDataSecurity())
/*      */       return;
/*  198 */     boolean isAnonymous = this.m_service.setPromptForLoginIfAnonymous();
/*  199 */     int errorCode = (isAnonymous) ? -20 : -18;
/*      */ 
/*  202 */     String errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess", null, userData.m_name);
/*      */ 
/*  204 */     this.m_service.createServiceExceptionEx(null, errMsg, errorCode);
/*      */   }
/*      */ 
/*      */   public boolean checkAccess(Service service, DataBinder binder, ResultSet rset, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/*  215 */     this.m_service = service;
/*  216 */     this.m_binder = binder;
/*  217 */     this.m_rset = rset;
/*  218 */     this.m_desiredPriv = desiredPriv;
/*      */ 
/*  220 */     return checkAccess();
/*      */   }
/*      */ 
/*      */   public boolean checkAccess() throws DataException, ServiceException
/*      */   {
/*  225 */     return checkOrDetermineAccess(0) > 0;
/*      */   }
/*      */ 
/*      */   public int determineBestPrivilege(Service service, DataBinder binder, ResultSet rset)
/*      */     throws DataException, ServiceException
/*      */   {
/*  232 */     this.m_service = service;
/*  233 */     this.m_binder = binder;
/*  234 */     this.m_rset = rset;
/*      */ 
/*  236 */     return determineBestPrivilege();
/*      */   }
/*      */ 
/*      */   public int determineBestPrivilege() throws DataException, ServiceException
/*      */   {
/*  241 */     return checkOrDetermineAccess(1);
/*      */   }
/*      */ 
/*      */   public int checkOrDetermineAccess(int flags) throws DataException, ServiceException
/*      */   {
/*  246 */     this.m_hasCheckedSecurity = true;
/*      */ 
/*  248 */     boolean isCheck = true;
/*  249 */     this.m_flags = flags;
/*  250 */     if ((flags & 0x1) != 0)
/*      */     {
/*  252 */       isCheck = false;
/*      */     }
/*      */ 
/*  255 */     UserData userData = this.m_service.getUserData();
/*  256 */     this.m_accountAvailable = true;
/*  257 */     if (this.m_rset != null)
/*      */     {
/*  259 */       this.m_group = ResultSetUtils.getValue(this.m_rset, "dSecurityGroup");
/*  260 */       this.m_account = ResultSetUtils.getValue(this.m_rset, "dDocAccount");
/*      */ 
/*  266 */       this.m_accountAvailable = (this.m_account != null);
/*      */     }
/*      */     else
/*      */     {
/*  270 */       this.m_group = this.m_binder.get("dSecurityGroup");
/*  271 */       this.m_account = this.m_binder.getAllowMissing("dDocAccount");
/*      */     }
/*      */ 
/*  276 */     this.m_service.setCachedObject("DocumentAccessSecurity", this);
/*  277 */     this.m_service.setReturnValue(null);
/*  278 */     this.m_service.executeFilter("skipAccessSecurityCheck");
/*  279 */     Object returnVal = this.m_service.getReturnValue();
/*  280 */     boolean isTracingAccessSecurity = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("checkordetermineaccess"));
/*  281 */     if (isTracingAccessSecurity)
/*      */     {
/*  283 */       Report.trace("checkordetermineaccess", new StringBuilder().append("\nuser=").append(userData.m_name).append("  isCheck=").append(isCheck).append("   m_desiredPriv=").append(this.m_desiredPriv).append("\n").append(userData).append("\n").append("m_rset=").append(this.m_rset).append("\n").append("m_group=").append(this.m_group).append("  m_account=").append(this.m_account).toString(), null);
/*      */     }
/*      */ 
/*  291 */     if ((returnVal != null) && (returnVal instanceof Boolean) && 
/*  293 */       (((Boolean)returnVal).booleanValue()))
/*      */     {
/*  295 */       if (isTracingAccessSecurity)
/*      */       {
/*  297 */         Report.trace("checkordetermineaccess", new StringBuilder().append("skipAccessSecurityCheck m_userPriv=").append(this.m_userPriv).append("  m_desiredPriv=").append(this.m_desiredPriv).append("  (m_userPriv & m_desiredPriv)=").append(this.m_userPriv & this.m_desiredPriv).toString(), null);
/*      */       }
/*  299 */       return (isCheck) ? this.m_userPriv & this.m_desiredPriv : this.m_userPriv;
/*      */     }
/*      */ 
/*  303 */     this.m_userPriv = SecurityUtils.determineGroupPrivilege(userData, this.m_group);
/*      */ 
/*  305 */     boolean hasAccess = true;
/*  306 */     if (isCheck)
/*      */     {
/*  308 */       hasAccess = (this.m_userPriv & this.m_desiredPriv) != 0;
/*      */     }
/*      */     else
/*      */     {
/*  312 */       hasAccess = this.m_userPriv != 0;
/*      */     }
/*  314 */     if (isTracingAccessSecurity)
/*      */     {
/*  316 */       Report.trace("checkordetermineaccess", new StringBuilder().append("determineGroupPrivilege m_userPriv=").append(this.m_userPriv).append("  hasAccess=").append(hasAccess).toString(), null);
/*      */     }
/*      */ 
/*  320 */     if (hasAccess)
/*      */     {
/*  325 */       this.m_service.setReturnValue(null);
/*  326 */       this.m_service.executeFilter("skipAccountAndMetaDataSecurityCheck");
/*  327 */       returnVal = this.m_service.getReturnValue();
/*  328 */       boolean skipAccountAndMetaDataSecurityCheck = false;
/*      */ 
/*  331 */       if (isCheck)
/*      */       {
/*  333 */         hasAccess = (this.m_userPriv & this.m_desiredPriv) != 0;
/*      */       }
/*      */       else
/*      */       {
/*  337 */         hasAccess = this.m_userPriv != 0;
/*      */       }
/*      */ 
/*  340 */       if ((returnVal != null) && (returnVal instanceof Boolean))
/*      */       {
/*  342 */         skipAccountAndMetaDataSecurityCheck = ((Boolean)returnVal).booleanValue();
/*      */       }
/*      */       else
/*      */       {
/*  346 */         boolean useAccounts = (this.m_accountAvailable) && (SecurityUtils.m_useAccounts);
/*  347 */         if ((!useAccounts) && (!SecurityUtils.m_useEntitySecurity))
/*      */         {
/*  349 */           skipAccountAndMetaDataSecurityCheck = true;
/*      */         }
/*      */       }
/*      */ 
/*  353 */       if (isTracingAccessSecurity)
/*      */       {
/*  355 */         Report.trace("checkordetermineaccess", new StringBuilder().append("skipAccountAndMetaDataSecurityCheck=").append(skipAccountAndMetaDataSecurityCheck).append("  hasAccess=").append(hasAccess).toString(), null);
/*      */       }
/*      */ 
/*  358 */       if ((!skipAccountAndMetaDataSecurityCheck) && (hasAccess))
/*      */       {
/*  360 */         if (this.m_accountAvailable)
/*      */         {
/*  362 */           if (this.m_account == null)
/*      */           {
/*  364 */             this.m_account = "";
/*      */           }
/*      */ 
/*  367 */           if (isCheck)
/*      */           {
/*  369 */             hasAccess = SecurityUtils.isAccountAccessible(userData, this.m_account, this.m_desiredPriv);
/*      */           }
/*      */           else
/*      */           {
/*  373 */             this.m_userPriv &= SecurityUtils.determineBestAccountPrivilege(userData, this.m_account);
/*  374 */             if (this.m_userPriv == 0)
/*      */             {
/*  376 */               hasAccess = false;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*  381 */         if (hasAccess)
/*      */         {
/*  383 */           if (isCheck)
/*      */           {
/*  385 */             hasAccess = checkMetaDataSecurity();
/*      */           }
/*      */           else
/*      */           {
/*  389 */             this.m_userPriv &= determineBestMetaDataSecurity();
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  395 */     if (isCheck)
/*      */     {
/*  397 */       if (isTracingAccessSecurity)
/*      */       {
/*  399 */         Report.trace("checkordetermineaccess", new StringBuilder().append("isCheck=true  hasAccess=").append(hasAccess).append("\n !!!!!!!!!!!!!!!!!!!!!!!!").toString(), null);
/*      */       }
/*  401 */       if (hasAccess)
/*      */       {
/*  403 */         return 1;
/*      */       }
/*      */ 
/*  406 */       return 0;
/*      */     }
/*      */ 
/*  409 */     if (isTracingAccessSecurity)
/*      */     {
/*  411 */       Report.trace("checkordetermineaccess", new StringBuilder().append("isCheck=false  m_userPriv=").append(this.m_userPriv).append("\n !!!!!!!!!!!!!!!!!!!!!!!!").toString(), null);
/*      */     }
/*  413 */     return this.m_userPriv;
/*      */   }
/*      */ 
/*      */   public boolean checkMetaDataSecurity(Service service, DataBinder binder, ResultSet rset, int desiredPriv, int userPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/*  421 */     this.m_service = service;
/*  422 */     this.m_binder = binder;
/*  423 */     this.m_rset = rset;
/*  424 */     this.m_desiredPriv = desiredPriv;
/*  425 */     this.m_userPriv = userPriv;
/*      */ 
/*  427 */     return checkMetaDataSecurity();
/*      */   }
/*      */ 
/*      */   public boolean checkMetaDataSecurity()
/*      */     throws DataException, ServiceException
/*      */   {
/*  433 */     return checkOrDetermineMetaDataSecurity(0) > 0;
/*      */   }
/*      */ 
/*      */   public int determineBestMetaDataSecurity() throws DataException, ServiceException
/*      */   {
/*  438 */     return checkOrDetermineMetaDataSecurity(1);
/*      */   }
/*      */ 
/*      */   public int checkOrDetermineMetaDataSecurity(int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/*  444 */     boolean isCheck = true;
/*  445 */     if ((flags & 0x1) != 0)
/*      */     {
/*  447 */       isCheck = false;
/*      */     }
/*      */ 
/*  450 */     boolean hasAccess = false;
/*  451 */     int priv = 0;
/*  452 */     int allAccess = 15;
/*      */ 
/*  454 */     if (!isUseEntitySecurityForRequest())
/*      */     {
/*  456 */       hasAccess = true;
/*  457 */       priv = allAccess;
/*      */     }
/*      */ 
/*  460 */     boolean isTracingAccessSecurity = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("checkordetermineaccess"));
/*      */ 
/*  462 */     if (isTracingAccessSecurity)
/*      */     {
/*  464 */       Report.trace("checkordetermineaccess", new StringBuilder().append("determineBestMetaDataSecurity isUseEntitySecurityForRequest=").append(!hasAccess).toString(), null);
/*      */     }
/*      */ 
/*  467 */     if ((!hasAccess) && 
/*  471 */       (this.m_rset == null) && (!this.m_service.isConditionVarTrue("useLocalDataDuringCheckMetaDataSecurity")))
/*      */     {
/*  474 */       hasAccess = true;
/*  475 */       priv = allAccess;
/*      */     }
/*      */ 
/*  479 */     List entityValues = createEntityList();
/*  480 */     if (!hasAccess)
/*      */     {
/*  483 */       boolean secFieldExists = false;
/*  484 */       if (this.m_rset != null)
/*      */       {
/*  486 */         for (EntityValue entityValue : entityValues)
/*      */         {
/*  488 */           if (this.m_rset.getFieldInfo(entityValue.m_field, entityValue.m_fieldInfo))
/*      */           {
/*  490 */             secFieldExists = true;
/*  491 */             break;
/*      */           }
/*      */         }
/*      */ 
/*  495 */         if (!secFieldExists)
/*      */         {
/*  497 */           if (!this.m_doingDocReferenceSecurity)
/*      */           {
/*  499 */             hasAccess = true;
/*  500 */             priv = allAccess;
/*      */           }
/*      */           else
/*      */           {
/*  504 */             this.m_rset = mergeDocInfo(this.m_binder, this.m_service.getWorkspace());
/*  505 */             if (this.m_rset == null)
/*      */             {
/*  507 */               hasAccess = true;
/*  508 */               priv = allAccess;
/*      */             }
/*      */           }
/*      */         }
/*  512 */         if (isTracingAccessSecurity)
/*      */         {
/*  514 */           Report.trace("checkordetermineaccess", new StringBuilder().append("determineBestMetaDataSecurity secFieldExists=").append(secFieldExists).append("  hasAccess=").append(hasAccess).toString(), null);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  519 */     if (!hasAccess)
/*      */     {
/*  521 */       populateEntityList(entityValues, this.m_binder, this.m_rset, true, false);
/*      */ 
/*  524 */       UserData userData = this.m_service.getUserData();
/*  525 */       String userName = userData.m_name;
/*      */ 
/*  527 */       if (isTracingAccessSecurity)
/*      */       {
/*  529 */         for (EntityValue ev : entityValues)
/*      */         {
/*  531 */           Report.trace("checkordetermineaccess", new StringBuilder().append("determineBestMetaDataSecurity entityValue: ").append(ev.m_field).append("=").append(ev.m_entityListStr).toString(), null);
/*      */         }
/*      */       }
/*      */ 
/*  535 */       if (isCheck)
/*      */       {
/*  537 */         hasAccess = hasEntityPrivilege(userName, entityValues, this.m_desiredPriv);
/*      */       }
/*      */       else
/*      */       {
/*  541 */         priv = SecurityAccessListUtils.determineBestEntityPrivilege(userData, entityValues, this.m_service);
/*      */ 
/*  543 */         if (priv > 0)
/*      */         {
/*  545 */           hasAccess = true;
/*      */         }
/*      */       }
/*  548 */       if (isTracingAccessSecurity)
/*      */       {
/*  550 */         Report.trace("checkordetermineaccess", new StringBuilder().append("determineBestMetaDataSecurity hasAccess=").append(hasAccess).toString(), null);
/*      */       }
/*      */ 
/*  553 */       if ((!hasAccess) && (isCheck) && 
/*  555 */         (this.m_service.setPromptForLoginIfAnonymous()))
/*      */       {
/*  557 */         this.m_service.createServiceException(null, "!csSystemNeedsLogin3");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  562 */     if (isCheck)
/*      */     {
/*  564 */       if (hasAccess)
/*      */       {
/*  566 */         return 1;
/*      */       }
/*      */ 
/*  569 */       return 0;
/*      */     }
/*      */ 
/*  572 */     return priv;
/*      */   }
/*      */ 
/*      */   public void checkMetaChangeSecurity(Service service, DataBinder binder, ResultSet oldSet, boolean isNewDoc)
/*      */     throws DataException, ServiceException
/*      */   {
/*  583 */     List entityValues = createEntityList();
/*  584 */     populateEntityList(entityValues, binder, null, false, true);
/*      */ 
/*  587 */     UserData userData = service.getUserData();
/*      */ 
/*  597 */     if ((SharedObjects.getEnvValueAsBoolean("PreventNonSpecialAuthGroupAdminFromACLUpdate", true)) && 
/*  603 */       (this.m_group == null) && (oldSet != null))
/*      */     {
/*  605 */       this.m_group = ResultSetUtils.getValue(oldSet, "dSecurityGroup");
/*      */     }
/*      */ 
/*  613 */     if ((isNewDoc) && (this.m_group == null))
/*      */     {
/*  615 */       this.m_group = binder.getLocal("dSecurityGroup");
/*      */     }
/*      */ 
/*  625 */     int userPriv = SecurityUtils.determineGroupPrivilege(userData, this.m_group);
/*  626 */     if (!isUseEntitySecurityForRequest(binder, userPriv, 2, service))
/*      */     {
/*  628 */       return;
/*      */     }
/*      */ 
/*  632 */     if (isNewDoc)
/*      */     {
/*  636 */       boolean useCollaboration = (SecurityUtils.m_useCollaboration) && (!service.isConditionVarTrue("IgnoreCollaboration"));
/*      */ 
/*  638 */       if (useCollaboration)
/*      */       {
/*  641 */         boolean hasProject = StringUtils.convertToBool(binder.getLocal("isCollaboration"), false);
/*      */ 
/*  643 */         if (hasProject)
/*      */         {
/*  645 */           userData = service.getUserData();
/*  646 */           String userName = userData.m_name;
/*      */ 
/*  648 */           String clbraName = binder.getLocal("dClbraName");
/*  649 */           if ((clbraName != null) && (clbraName.length() > 0))
/*      */           {
/*  654 */             boolean requireCollaboration = service.isConditionVarTrue("RequireCollaborationRead");
/*  655 */             int collaborationPrivlege = (requireCollaboration) ? 1 : 2;
/*      */ 
/*  657 */             String errMsgResource = (requireCollaboration) ? "csNeedReadToClbrProj" : "csNeedWriteToClbrProj";
/*      */ 
/*  660 */             if (!Collaborations.isUserInCollaboration(userName, clbraName, service, collaborationPrivlege))
/*      */             {
/*  663 */               String errMsg = LocaleUtils.encodeMessage(errMsgResource, null, userName, clbraName);
/*      */ 
/*  665 */               throw new ServiceException(errMsg);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*  670 */         if (!SecurityUtils.m_accessListPrivilegesGrantedWhenEmpty)
/*      */         {
/*  674 */           for (EntityValue entityValue : entityValues)
/*      */           {
/*  676 */             String entityList = entityValue.m_entityListStr;
/*  677 */             if ((entityValue.m_type.equals("user")) && (entityList != null) && (entityList.length() == 0))
/*      */             {
/*  680 */               entityValue.m_entityListStr = new StringBuilder().append(binder.getLocal("dDocAuthor")).append("(RWDA)").toString();
/*  681 */               entityList = addEntitySymbols(entityValue, true);
/*  682 */               binder.putLocal(entityValue.m_field, entityList);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  691 */     List oldEntityValues = null;
/*  692 */     boolean entitiesChanged = false;
/*      */ 
/*  694 */     if ((oldSet != null) && (!isNewDoc))
/*      */     {
/*  696 */       oldEntityValues = createEntityList(true);
/*  697 */       populateEntityList(oldEntityValues, null, oldSet, false, true);
/*      */ 
/*  699 */       if ((this.m_rset != null) && (!this.m_rset.isRowPresent()))
/*      */       {
/*  701 */         this.m_rset.first();
/*      */       }
/*  703 */       if (determineBestPrivilege() < service.getServiceData().m_accessLevel)
/*      */       {
/*  705 */         String errMsg = LocaleUtils.encodeMessage("csUserInsufficientAccess", null, userData.m_name);
/*      */ 
/*  707 */         service.createServiceException(null, errMsg);
/*      */       }
/*  709 */       populateEntityList(entityValues, binder, null, false, true);
/*      */ 
/*  711 */       int size = entityValues.size();
/*  712 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  715 */         EntityValue entityValue = (EntityValue)entityValues.get(i);
/*  716 */         EntityValue oldEntityValue = (EntityValue)oldEntityValues.get(i);
/*  717 */         entitiesChanged = hasEntityFieldsChanged(oldEntityValue.m_entityListStr, entityValue.m_entityListStr);
/*      */ 
/*  719 */         if (entitiesChanged) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  727 */     if ((!isNewDoc) && (!entitiesChanged)) {
/*      */       return;
/*      */     }
/*  730 */     if ((!isNewDoc) && (!hasEntityPrivilege(userData.m_name, oldEntityValues, 8)))
/*      */     {
/*  733 */       String errMsg = LocaleUtils.encodeMessage("csCannotChangeEntityFields", null, userData.m_name);
/*      */ 
/*  735 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/*  740 */     if (hasEntityPrivilege(userData.m_name, entityValues, 8))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/*  745 */     boolean isFolderShortcut = service.isConditionVarTrue("isFolderShortcut");
/*  746 */     if (isFolderShortcut)
/*      */     {
/*  748 */       if ((isNewDoc) && (!hasEntityPrivilege(userData.m_name, entityValues, 1)))
/*      */       {
/*  751 */         String errMsg = LocaleUtils.encodeMessage("csNeedReadPrivEntityFields", null, userData.m_name);
/*      */ 
/*  753 */         throw new ServiceException(errMsg);
/*      */       }
/*  755 */       if ((isNewDoc) || (hasEntityPrivilege(userData.m_name, entityValues, 2))) {
/*      */         return;
/*      */       }
/*  758 */       String errMsg = LocaleUtils.encodeMessage("csNeedWritePrivEntityFields", null, userData.m_name);
/*      */ 
/*  760 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/*  766 */     if (SharedObjects.getEnvValueAsBoolean("CollectionAllowCheckinWithWriteACL", false))
/*      */     {
/*  768 */       if (hasEntityPrivilege(userData.m_name, entityValues, 2)) {
/*      */         return;
/*      */       }
/*  771 */       String errMsg = LocaleUtils.encodeMessage("csNeedWritePrivEntityFields", null, userData.m_name);
/*      */ 
/*  773 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/*  778 */     String errMsg = LocaleUtils.encodeMessage("csNeedAdminPrivEntityFields", null, userData.m_name);
/*      */ 
/*  780 */     throw new ServiceException(errMsg);
/*      */   }
/*      */ 
/*      */   public List<EntityValue> createEntityList()
/*      */     throws ServiceException, DataException
/*      */   {
/*  789 */     return createEntityList(false);
/*      */   }
/*      */ 
/*      */   public List<EntityValue> createEntityList(boolean isNew) throws ServiceException, DataException
/*      */   {
/*  794 */     List entityList = new ArrayList();
/*      */ 
/*  796 */     if ((this.m_entityList == null) || (isNew == true))
/*      */     {
/*  798 */       Table secFields = ResourceContainerUtils.getDynamicTableResource("EntitySecurityFields");
/*  799 */       DataResultSet drset = new DataResultSet();
/*  800 */       drset.init(secFields);
/*      */ 
/*  802 */       FieldInfo[] fis = ResultSetUtils.createInfoList(drset, new String[] { "field", "type", "symbol" }, true);
/*      */ 
/*  804 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  806 */         entityList.add(new EntityValue(drset.getStringValue(fis[0].m_index), drset.getStringValue(fis[1].m_index), drset.getStringValue(fis[2].m_index)));
/*      */       }
/*      */ 
/*  810 */       if (this.m_entityList == null)
/*      */       {
/*  812 */         this.m_entityList = entityList;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  817 */       entityList = this.m_entityList;
/*      */     }
/*      */ 
/*  820 */     return entityList;
/*      */   }
/*      */ 
/*      */   public void populateEntityList(List<EntityValue> entityList, DataBinder binder, ResultSet rset, boolean isRsetChecked, boolean isUpdateBinder)
/*      */     throws ServiceException, DataException
/*      */   {
/*  827 */     for (EntityValue entityValue : entityList)
/*      */     {
/*  829 */       if ((rset != null) && (rset.isRowPresent()))
/*      */       {
/*  832 */         if (rset.getFieldInfo(entityValue.m_field, entityValue.m_fieldInfo))
/*      */         {
/*  834 */           entityValue.m_entityListStr = rset.getStringValue(entityValue.m_fieldInfo.m_index);
/*      */         }
/*      */ 
/*      */       }
/*  838 */       else if (binder != null)
/*      */       {
/*  840 */         entityValue.m_entityListStr = binder.getLocal(entityValue.m_field);
/*      */       }
/*      */ 
/*  843 */       entityValue.m_entityListStr = addEntitySymbols(entityValue, true);
/*      */ 
/*  845 */       if ((isUpdateBinder) && (binder != null))
/*      */       {
/*  847 */         binder.putLocal(entityValue.m_field, entityValue.m_entityListStr);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public boolean isUseEntitySecurity(DataBinder binder, int userPriv, int desiredPriv)
/*      */     throws DataException, ServiceException
/*      */   {
/*  859 */     Report.trace("deprecation", "isUseEntitySecurity is deprecated.  Use isUseEntitySecurityForRequest instead.", null);
/*  860 */     return isUseEntitySecurityForRequest(binder, userPriv, desiredPriv, null);
/*      */   }
/*      */ 
/*      */   public boolean isUseEntitySecurityForRequest(DataBinder binder, int userPriv, int desiredPriv, Service service)
/*      */     throws DataException, ServiceException
/*      */   {
/*  871 */     this.m_service = service;
/*  872 */     this.m_binder = binder;
/*  873 */     this.m_desiredPriv = desiredPriv;
/*  874 */     this.m_userPriv = userPriv;
/*      */ 
/*  878 */     this.m_group = binder.getAllowMissing("dSecurityGroup");
/*  879 */     this.m_account = binder.getAllowMissing("dDocAccount");
/*      */ 
/*  882 */     this.m_accountAvailable = true;
/*      */ 
/*  884 */     return isUseEntitySecurityForRequest();
/*      */   }
/*      */ 
/*      */   public boolean isUseEntitySecurityForRequest()
/*      */     throws DataException, ServiceException
/*      */   {
/*  894 */     boolean determinedUseEntitySecurity = false;
/*  895 */     boolean useEntitySecurity = false;
/*      */ 
/*  897 */     this.m_service.setCachedObject("DocumentAccessSecurity", this);
/*  898 */     this.m_service.setReturnValue(null);
/*  899 */     this.m_service.executeFilter("isUseEntitySecurity");
/*  900 */     Object returnVal = this.m_service.getReturnValue();
/*  901 */     if (returnVal != null)
/*      */     {
/*  903 */       determinedUseEntitySecurity = true;
/*  904 */       useEntitySecurity = ((Boolean)returnVal).booleanValue();
/*      */     }
/*      */ 
/*  908 */     if ((!determinedUseEntitySecurity) && 
/*  910 */       ((this.m_userPriv & 0x8) != 0))
/*      */     {
/*  912 */       determinedUseEntitySecurity = true;
/*  913 */       useEntitySecurity = false;
/*      */     }
/*      */ 
/*  918 */     if (!determinedUseEntitySecurity)
/*      */     {
/*  920 */       useEntitySecurity = SecurityUtils.m_useEntitySecurity;
/*  921 */       if (!useEntitySecurity)
/*      */       {
/*  923 */         determinedUseEntitySecurity = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  928 */     if ((!determinedUseEntitySecurity) && 
/*  930 */       (this.m_group != null) && (!isSpecialAuthGroup(this.m_group)))
/*      */     {
/*  932 */       determinedUseEntitySecurity = true;
/*  933 */       useEntitySecurity = false;
/*      */     }
/*      */ 
/*  937 */     if ((!determinedUseEntitySecurity) && 
/*  939 */       (this.m_service != null) && 
/*  941 */       (this.m_service.isConditionVarTrue("NoEntitySecurity")))
/*      */     {
/*  943 */       determinedUseEntitySecurity = true;
/*  944 */       useEntitySecurity = false;
/*      */     }
/*      */ 
/*  949 */     if (!determinedUseEntitySecurity)
/*      */     {
/*  951 */       useEntitySecurity = true;
/*      */     }
/*      */ 
/*  954 */     this.m_service.executeFilter("postIsUseEntitySecurity");
/*      */ 
/*  956 */     return useEntitySecurity;
/*      */   }
/*      */ 
/*      */   public String determineDocumentWhereClause(UserData userData, Service service, DataBinder binder, int privilege, boolean isVerity)
/*      */     throws DataException, ServiceException
/*      */   {
/*  963 */     this.m_hasCheckedSecurity = true;
/*      */ 
/*  965 */     if (!m_isMaxSecurityAttributesInitialized) {
/*  966 */       initMaxSecurityAttributes();
/*      */     }
/*  968 */     IdcStringBuilder clause = new IdcStringBuilder();
/*      */ 
/*  970 */     if (!service.getUseSecurity())
/*      */     {
/*  972 */       return clause.toString();
/*      */     }
/*      */ 
/*  975 */     this.m_service = service;
/*  976 */     this.m_binder = binder;
/*  977 */     this.m_desiredPriv = privilege;
/*  978 */     this.m_userData = userData;
/*  979 */     this.m_queryElts = SearchQueryUtils.lookupSearchParsingObject(service);
/*      */ 
/*  982 */     this.m_accountAvailable = true;
/*      */ 
/*  984 */     CommonSearchConfig csc = SearchIndexerUtils.retrieveSearchConfig(service);
/*  985 */     if ((this.m_queryConfig != null) && (csc != this.m_queryConfig))
/*      */     {
/*  987 */       this.m_queryConfig.clear();
/*      */     }
/*  989 */     this.m_queryConfig = csc;
/*      */ 
/*  991 */     this.m_currentEngine = this.m_queryConfig.getCurrentEngineName();
/*  992 */     this.m_supportCombinedNotOp = StringUtils.convertToBool(this.m_queryConfig.getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", this.m_currentEngine, "SupportCombinedNotOp"), false);
/*      */ 
/*  995 */     DataResultSet drset = SharedObjects.getTable("ColumnTableMap");
/*  996 */     this.m_columnTableMap = new Hashtable();
/*  997 */     if (drset != null)
/*      */     {
/*  999 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, new String[] { "columnName", "tableName" }, true);
/* 1000 */       for (; drset.isRowPresent(); drset.next())
/*      */       {
/* 1002 */         Vector v = drset.getCurrentRowValues();
/* 1003 */         String columnName = (String)v.elementAt(fi[0].m_index);
/* 1004 */         String tableName = (String)v.elementAt(fi[0].m_index);
/* 1005 */         this.m_columnTableMap.put(columnName, tableName);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1010 */     Object o = service.getCachedObject("ColumnTableMapOverrides");
/* 1011 */     if (o instanceof Map)
/*      */     {
/* 1013 */       Map overrides = (Map)o;
/* 1014 */       Iterator it = overrides.keySet().iterator();
/* 1015 */       while (it.hasNext())
/*      */       {
/* 1017 */         String key = (String)it.next();
/* 1018 */         String val = (String)overrides.get(key);
/* 1019 */         if (val == null)
/*      */         {
/* 1021 */           this.m_columnTableMap.remove(key);
/*      */         }
/*      */         else
/*      */         {
/* 1025 */           this.m_columnTableMap.put(key, val);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1030 */     String clauseType = null;
/* 1031 */     if (isVerity)
/*      */     {
/* 1033 */       clauseType = "search";
/*      */     }
/*      */     else
/*      */     {
/* 1038 */       this.m_queryConfig.clear();
/* 1039 */       this.m_queryConfig = this.m_queryConfig.shallowClone();
/* 1040 */       this.m_queryConfig.setCurrentConfig("DATABASE");
/* 1041 */       clauseType = "database";
/*      */     }
/*      */ 
/* 1046 */     boolean useEntitySecurity = SecurityUtils.m_useEntitySecurity;
/* 1047 */     if (useEntitySecurity)
/*      */     {
/* 1049 */       if ((privilege & 0x8) != 0)
/*      */       {
/* 1051 */         useEntitySecurity = false;
/*      */       }
/*      */ 
/* 1055 */       if (service.isConditionVarTrue("IgnoreAccounts"))
/*      */       {
/* 1057 */         useEntitySecurity = false;
/*      */       }
/*      */ 
/* 1061 */       String specialAuthGroups = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 1062 */       if ((specialAuthGroups == null) || (specialAuthGroups.length() == 0))
/*      */       {
/* 1064 */         useEntitySecurity = false;
/*      */       }
/*      */     }
/*      */ 
/* 1068 */     Vector allGroups = new IdcVector();
/* 1069 */     int[] naccessible = new int[2];
/* 1070 */     if (useEntitySecurity)
/*      */     {
/* 1072 */       boolean[][] results = determineGroupsAccessibleWithEntityFields(userData, binder, privilege, allGroups, naccessible);
/*      */ 
/* 1074 */       if (naccessible[0] + naccessible[1] == 0)
/*      */       {
/* 1076 */         boolean isAnonymous = service.setPromptForLoginIfAnonymous();
/* 1077 */         int errorCode = (isAnonymous) ? -20 : -18;
/*      */ 
/* 1079 */         service.createServiceExceptionEx(null, "!csAppGroupsAccessDenied", errorCode);
/*      */       }
/*      */ 
/* 1083 */       List nonAuthGroupParsedElts = null;
/* 1084 */       List authGroupParsedElts = null;
/* 1085 */       List originalParsedElts = null;
/*      */ 
/* 1087 */       IdcStringBuilder nonAuthGroupClause = new IdcStringBuilder();
/* 1088 */       IdcStringBuilder authGroupClause = new IdcStringBuilder();
/*      */ 
/* 1090 */       if (this.m_queryElts != null)
/*      */       {
/* 1092 */         originalParsedElts = this.m_queryElts.m_rawParsedElements;
/* 1093 */         nonAuthGroupParsedElts = new ArrayList();
/* 1094 */         this.m_queryElts.m_rawParsedElements = nonAuthGroupParsedElts;
/*      */       }
/* 1096 */       buildGroupClause(binder, allGroups, results[0], naccessible[0], clauseType, nonAuthGroupClause);
/*      */ 
/* 1098 */       if (this.m_queryElts != null)
/*      */       {
/* 1100 */         authGroupParsedElts = new ArrayList();
/* 1101 */         this.m_queryElts.m_rawParsedElements = authGroupParsedElts;
/*      */       }
/*      */ 
/* 1105 */       this.m_filterObjects = new Object[] { allGroups, results[1], new Integer(naccessible[1]), clauseType, authGroupClause };
/* 1106 */       this.m_service.setCachedObject("DocumentAccessSecurity", this);
/* 1107 */       this.m_service.setReturnValue(null);
/* 1108 */       this.m_service.executeFilter("specialAuthGroupClause");
/* 1109 */       Object returnVal = this.m_service.getReturnValue();
/* 1110 */       boolean retVal = false;
/*      */ 
/* 1112 */       if (returnVal != null)
/*      */       {
/* 1114 */         retVal = ((Boolean)returnVal).booleanValue();
/*      */       }
/*      */ 
/* 1117 */       if (!retVal)
/*      */       {
/* 1119 */         buildGroupClause(binder, allGroups, results[1], naccessible[1], clauseType, authGroupClause);
/* 1120 */         if (authGroupClause.length() > 0)
/*      */         {
/* 1122 */           buildUserClause(userData, binder, privilege, clauseType, authGroupClause);
/*      */         }
/*      */       }
/*      */ 
/* 1126 */       if (this.m_queryElts != null)
/*      */       {
/* 1128 */         this.m_queryElts.m_rawParsedElements = originalParsedElts;
/*      */       }
/* 1130 */       if (nonAuthGroupClause.length() > 0)
/*      */       {
/* 1132 */         if (authGroupClause.length() > 0)
/*      */         {
/* 1134 */           if (originalParsedElts != null)
/*      */           {
/* 1136 */             originalParsedElts.add("(");
/* 1137 */             originalParsedElts.addAll(nonAuthGroupParsedElts);
/* 1138 */             originalParsedElts.add(")");
/*      */           }
/* 1140 */           clause.append(new StringBuilder().append("(").append(nonAuthGroupClause.toString()).append(")").toString());
/* 1141 */           buildClauseConjunction(clauseType, "OR", clause);
/* 1142 */           if (originalParsedElts != null)
/*      */           {
/* 1144 */             originalParsedElts.add("(");
/* 1145 */             originalParsedElts.addAll(authGroupParsedElts);
/* 1146 */             originalParsedElts.add(")");
/*      */           }
/* 1148 */           clause.append(new StringBuilder().append("(").append(authGroupClause).append(")").toString());
/*      */         }
/*      */         else
/*      */         {
/* 1152 */           clause.append(nonAuthGroupClause);
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/* 1157 */         clause.append(authGroupClause);
/*      */       }
/* 1159 */       nonAuthGroupClause.releaseBuffers();
/* 1160 */       authGroupClause.releaseBuffers();
/*      */     }
/*      */     else
/*      */     {
/* 1164 */       boolean[] results = null;
/*      */ 
/* 1166 */       results = determineGroupsAccessible(userData, binder, privilege, allGroups, naccessible, null);
/*      */ 
/* 1168 */       if (naccessible[0] == 0)
/*      */       {
/* 1170 */         boolean isAnonymous = service.setPromptForLoginIfAnonymous();
/* 1171 */         int errorCode = (isAnonymous) ? -20 : -18;
/*      */ 
/* 1173 */         service.createServiceExceptionEx(null, "!csAppGroupsAccessDenied", errorCode);
/*      */       }
/*      */ 
/* 1177 */       buildGroupClause(binder, allGroups, results, naccessible[0], clauseType, clause);
/*      */     }
/*      */ 
/* 1180 */     if ((this.m_accountAvailable) && (!service.isConditionVarTrue("IgnoreAccounts")))
/*      */     {
/* 1182 */       buildAccountClause(userData, service, binder, privilege, clauseType, clause);
/*      */     }
/*      */ 
/* 1185 */     return clause.toString();
/*      */   }
/*      */ 
/*      */   public DataResultSet mergeDocInfo(DataBinder binder, Workspace ws) throws DataException, ServiceException
/*      */   {
/* 1190 */     String tempKey = "__SECURITY_TEMP_NAME";
/* 1191 */     DataResultSet retRs = FileRevisionSelectionUtils.loadAdditionalDocInfo(tempKey, binder, this.m_service, ws);
/*      */ 
/* 1196 */     binder.removeResultSet(tempKey);
/* 1197 */     return retRs;
/*      */   }
/*      */ 
/*      */   public boolean[] determineGroupsAccessible(UserData userData, DataBinder binder, int privilege, Vector allGroups, int[] naccessible, Hashtable groupsAccessible)
/*      */     throws ServiceException
/*      */   {
/* 1204 */     boolean[] results = null;
/*      */ 
/* 1207 */     if (privilege == 0)
/*      */     {
/* 1209 */       privilege = 15;
/*      */     }
/*      */ 
/* 1215 */     this.m_group = binder.getLocal("dSecurityGroup");
/* 1216 */     if ((this.m_group != null) && (this.m_group.length() > 0))
/*      */     {
/*      */       try
/*      */       {
/* 1220 */         int groupPriv = SecurityUtils.determineGroupPrivilege(userData, this.m_group);
/* 1221 */         if ((groupPriv & privilege) != 0)
/*      */         {
/* 1223 */           allGroups.addElement(this.m_group);
/* 1224 */           results = new boolean[] { true };
/*      */ 
/* 1230 */           if (groupsAccessible != null)
/*      */           {
/* 1232 */             groupsAccessible.put(this.m_group, "1");
/*      */           }
/* 1234 */           naccessible[0] = 1;
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1239 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1241 */           Report.debug("system", null, e);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1247 */       if (groupsAccessible == null)
/*      */       {
/* 1249 */         groupsAccessible = new Hashtable();
/*      */       }
/* 1251 */       SecurityUtils.determineGroupsAccessible(userData, privilege, allGroups, groupsAccessible);
/*      */ 
/* 1254 */       results = new boolean[allGroups.size()];
/* 1255 */       Iterator it = groupsAccessible.keySet().iterator();
/* 1256 */       while (it.hasNext())
/*      */       {
/* 1258 */         int index = allGroups.indexOf(it.next());
/* 1259 */         if (index >= 0)
/*      */         {
/* 1261 */           results[index] = true;
/*      */         }
/*      */       }
/*      */ 
/* 1265 */       Object[] params = { new Integer(privilege), allGroups, groupsAccessible, results };
/* 1266 */       this.m_service.setCachedObject("determineGroupsAccessible:params", params);
/* 1267 */       this.m_service.executeFilter("determineGroupsAccessible");
/*      */ 
/* 1269 */       naccessible[0] = groupsAccessible.size();
/*      */     }
/*      */ 
/* 1272 */     return results;
/*      */   }
/*      */ 
/*      */   public boolean[][] determineGroupsAccessibleWithEntityFields(UserData userData, DataBinder binder, int privilege, Vector allGroups, int[] naccessible)
/*      */     throws ServiceException
/*      */   {
/* 1280 */     int[] nGroupsAccessible = new int[1];
/* 1281 */     boolean[] groupResults = determineGroupsAccessible(userData, binder, privilege, allGroups, nGroupsAccessible, null);
/*      */ 
/* 1287 */     Vector allGroupsAdmin = new IdcVector();
/* 1288 */     Hashtable adminGroupsAccessible = new Hashtable();
/* 1289 */     int[] nAdminGroupsAccessible = new int[1];
/* 1290 */     determineGroupsAccessible(userData, binder, 8, allGroupsAdmin, nAdminGroupsAccessible, adminGroupsAccessible);
/*      */ 
/* 1294 */     String specialAuthGroupsStr = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 1295 */     Vector specialAuthGroups = StringUtils.parseArray(specialAuthGroupsStr, ',', '^');
/* 1296 */     int numAuthGroups = specialAuthGroups.size();
/*      */ 
/* 1298 */     int ngroups = allGroups.size();
/* 1299 */     boolean[][] results = new boolean[2][ngroups];
/* 1300 */     naccessible[0] = 0;
/* 1301 */     naccessible[1] = 0;
/* 1302 */     for (int i = 0; i < ngroups; ++i)
/*      */     {
/* 1305 */       if (groupResults[i] == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1310 */       String group = (String)allGroups.elementAt(i);
/*      */ 
/* 1313 */       if (adminGroupsAccessible.get(group) != null)
/*      */       {
/* 1315 */         results[0][i] = 1;
/* 1316 */         naccessible[0] += 1;
/*      */       }
/*      */       else
/*      */       {
/* 1321 */         boolean isAuthGroup = false;
/*      */ 
/* 1324 */         this.m_filterObjects = new Object[] { group, new Boolean(isAuthGroup) };
/* 1325 */         this.m_service.setCachedObject("DocumentAccessSecurity", this);
/* 1326 */         this.m_service.setReturnValue(null);
/* 1327 */         this.m_service.executeFilter("checkSpecialAuthGroup");
/* 1328 */         Object returnVal = this.m_service.getReturnValue();
/*      */ 
/* 1330 */         boolean retVal = false;
/* 1331 */         if (returnVal != null)
/*      */         {
/* 1333 */           retVal = ((Boolean)returnVal).booleanValue();
/*      */         }
/*      */ 
/* 1336 */         if (retVal)
/*      */         {
/* 1338 */           isAuthGroup = ((Boolean)this.m_filterObjects[1]).booleanValue();
/*      */         }
/*      */         else
/*      */         {
/* 1342 */           for (int j = 0; j < numAuthGroups; ++j)
/*      */           {
/* 1344 */             String authGroup = (String)specialAuthGroups.elementAt(j);
/* 1345 */             if (!authGroup.equalsIgnoreCase(group))
/*      */               continue;
/* 1347 */             isAuthGroup = true;
/* 1348 */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1353 */         if (isAuthGroup)
/*      */         {
/* 1355 */           results[1][i] = 1;
/* 1356 */           naccessible[1] += 1;
/*      */         }
/*      */         else
/*      */         {
/* 1360 */           results[0][i] = 1;
/* 1361 */           naccessible[0] += 1;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1366 */     return results;
/*      */   }
/*      */ 
/*      */   public void buildGroupClause(DataBinder binder, Vector allGroups, boolean[] results, int naccessible, String clauseType, IdcAppendable clause)
/*      */     throws ServiceException
/*      */   {
/* 1372 */     boolean doAllowClause = (naccessible == 1) || (naccessible <= results.length / 2);
/* 1373 */     String disableNegativeStr = this.m_queryConfig.getEngineValue(this.m_currentEngine, "DisableNegativeGroupClause");
/* 1374 */     doAllowClause |= StringUtils.convertToBool(disableNegativeStr, false);
/*      */ 
/* 1376 */     int ngroups = results.length;
/* 1377 */     boolean firstTime = true;
/* 1378 */     int nGroupsToInclude = ngroups;
/*      */ 
/* 1381 */     if ((doAllowClause) && (naccessible > m_maxSecurityGroupsInSecurityClause))
/*      */     {
/* 1383 */       int lastReportInMin = SharedObjects.getEnvironmentInt("maxSecurityAttributesLimitReached", 0);
/*      */ 
/* 1385 */       Calendar rightNow = Calendar.getInstance();
/*      */ 
/* 1387 */       if (rightNow.getTimeInMillis() / 60000L - lastReportInMin > 1440L)
/*      */       {
/* 1389 */         IdcMessage msg = new IdcMessage("csLogTooManySecurityAttributes", new Object[] { this.m_userData.m_name, "Accessible Security Groups" });
/* 1390 */         Report.info(null, null, msg);
/* 1391 */         SharedObjects.putEnvironmentValue("maxSecurityAttributesLimitReached", Long.toString(rightNow.getTimeInMillis() / 60000L));
/*      */       }
/* 1393 */       IdcMessage msg = new IdcMessage("csAlertTooManySecurityAttributes", new Object[] { this.m_userData.m_name, Integer.valueOf(naccessible), "Accessible Security Groups", Integer.valueOf(m_maxSecurityGroupsInSecurityClause) });
/* 1394 */       Report.trace("alerts", null, msg);
/*      */ 
/* 1396 */       AlertUtils.createOneTimeAlert(msg, this.m_service.m_parentCxt, this.m_userData.m_name);
/* 1397 */       binder.putLocal("IsMaxSecurityAttributesLimitReached", "true");
/*      */ 
/* 1400 */       String truncated = binder.getLocal("TruncatedGroups");
/* 1401 */       if (truncated == null)
/* 1402 */         truncated = "";
/* 1403 */       nGroupsToInclude = naccessible;
/* 1404 */       for (int i = ngroups - 1; nGroupsToInclude > m_maxSecurityGroupsInSecurityClause; --i)
/*      */       {
/* 1406 */         if (results[i] != 1)
/*      */           continue;
/* 1408 */         truncated = new StringBuilder().append(truncated).append((truncated.length() == 0) ? (String)allGroups.elementAt(i) : new StringBuilder().append(",").append((String)allGroups.elementAt(i)).toString()).toString();
/* 1409 */         --nGroupsToInclude;
/*      */       }
/*      */ 
/* 1412 */       binder.putLocal("TruncatedGroups", truncated);
/*      */     }
/*      */ 
/* 1416 */     for (int i = 0; (i < ngroups) && (nGroupsToInclude > 0); ++i)
/*      */     {
/* 1418 */       boolean addClause = doAllowClause == results[i];
/* 1419 */       if (!addClause)
/*      */         continue;
/* 1421 */       if (!firstTime)
/*      */       {
/* 1423 */         if (doAllowClause)
/*      */         {
/* 1425 */           buildClauseConjunction(clauseType, "OR", clause);
/*      */         }
/*      */         else
/*      */         {
/* 1429 */           buildClauseConjunction(clauseType, "AND", clause);
/*      */         }
/*      */       }
/* 1432 */       firstTime = false;
/* 1433 */       String group = (String)allGroups.elementAt(i);
/*      */ 
/* 1435 */       buildClauseElement(clauseType, getSecurityGroupClauseField(), "equals", group, !doAllowClause, clause);
/*      */ 
/* 1437 */       --nGroupsToInclude;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildAccountClause(UserData userData, Service service, DataBinder binder, int privilege, String clauseType, IdcStringBuilder clause)
/*      */     throws ServiceException
/*      */   {
/* 1445 */     boolean ignoreAccounts = service.isConditionVarTrue("IgnoreAccounts");
/* 1446 */     if ((((!SecurityUtils.m_useAccounts) || (ignoreAccounts))) && (!SecurityUtils.m_useCollaboration))
/*      */       return;
/* 1448 */     String[] accounts = null;
/* 1449 */     String account = binder.getLocal("dDocAccount");
/* 1450 */     if (null == account)
/*      */     {
/* 1452 */       String project = binder.getLocal("dClbraName");
/* 1453 */       if ((project != null) && (project.length() > 0))
/*      */       {
/* 1455 */         account = new StringBuilder().append("prj/").append(project).toString();
/*      */       }
/*      */     }
/* 1458 */     if ((account != null) && (account.length() > 0))
/*      */     {
/* 1460 */       if (SecurityUtils.isAccountAccessible(userData, account, privilege))
/*      */       {
/* 1462 */         accounts = new String[] { account };
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1467 */       accounts = SecurityUtils.getPrivilegedAccounts(userData, privilege, true);
/*      */     }
/* 1469 */     if ((accounts == null) || (accounts.length == 0))
/*      */     {
/* 1471 */       service.setPromptForLoginIfAnonymous();
/* 1472 */       throw new ServiceException("!csAppAccountsAccessDenied");
/*      */     }
/*      */ 
/* 1476 */     if (StringUtils.findStringIndex(accounts, "#all") >= 0) {
/*      */       return;
/*      */     }
/* 1479 */     boolean hasGroupClause = clause.length() > 0;
/* 1480 */     if (hasGroupClause)
/*      */     {
/* 1482 */       if (this.m_queryElts != null)
/*      */       {
/* 1484 */         this.m_queryElts.m_rawParsedElements.add(0, "(");
/* 1485 */         this.m_queryElts.m_rawParsedElements.add(")");
/*      */       }
/* 1487 */       clause.insert(0, '(');
/* 1488 */       clause.append(')');
/* 1489 */       buildClauseConjunction(clauseType, "AND", clause);
/* 1490 */       clause.append('(');
/* 1491 */       if (this.m_queryElts != null)
/*      */       {
/* 1493 */         this.m_queryElts.m_rawParsedElements.add("(");
/*      */       }
/*      */     }
/*      */ 
/* 1497 */     int naccounts = accounts.length;
/* 1498 */     boolean firstTime = true;
/*      */ 
/* 1501 */     if (naccounts > m_maxAccountsInSecurityClause)
/*      */     {
/* 1503 */       int lastReportInMin = SharedObjects.getEnvironmentInt("maxSecurityAttributesLimitReached", 0);
/*      */ 
/* 1505 */       Calendar rightNow = Calendar.getInstance();
/*      */ 
/* 1507 */       if (rightNow.getTimeInMillis() / 60000L - lastReportInMin > 1440L)
/*      */       {
/* 1509 */         IdcMessage msg = new IdcMessage("csLogTooManySecurityAttributes", new Object[] { this.m_userData.m_name, "Accounts" });
/*      */ 
/* 1511 */         Report.info(null, null, msg);
/* 1512 */         SharedObjects.putEnvironmentValue("maxSecurityAttributesLimitReached", Long.toString(rightNow.getTimeInMillis() / 60000L));
/*      */       }
/* 1514 */       IdcMessage msg = new IdcMessage("csAlertTooManySecurityAttributes", new Object[] { this.m_userData.m_name, Integer.valueOf(naccounts), "Accounts", Integer.valueOf(m_maxAccountsInSecurityClause) });
/* 1515 */       Report.trace("alerts", null, msg);
/*      */ 
/* 1517 */       AlertUtils.createOneTimeAlert(msg, this.m_service.m_parentCxt, userData.m_name);
/* 1518 */       binder.putLocal("IsMaxSecurityAttributesLimitReached", "true");
/*      */ 
/* 1521 */       String truncated = "";
/* 1522 */       for (int i = m_maxAccountsInSecurityClause; i < naccounts; ++i)
/*      */       {
/* 1524 */         truncated = new StringBuilder().append(truncated).append((i == m_maxAccountsInSecurityClause) ? accounts[i] : new StringBuilder().append(",").append(accounts[i]).toString()).toString();
/*      */       }
/* 1526 */       binder.putLocal("TruncatedAccounts", truncated);
/*      */ 
/* 1528 */       naccounts = m_maxAccountsInSecurityClause;
/*      */     }
/*      */ 
/* 1532 */     for (int i = 0; i < naccounts; ++i)
/*      */     {
/* 1534 */       if (!firstTime)
/*      */       {
/* 1536 */         buildClauseConjunction(clauseType, "OR", clause);
/*      */       }
/* 1538 */       firstTime = false;
/*      */ 
/* 1541 */       String acct = accounts[i];
/* 1542 */       if ((acct.trim().length() == 0) || (acct.equals("#none")))
/*      */       {
/* 1544 */         if (this.m_queryElts != null)
/*      */         {
/* 1546 */           buildParsedClauseElement(getDocAccountClauseField(), "equals", "", false);
/*      */         }
/*      */ 
/* 1550 */         ParsedQueryElements curQueryElts = this.m_queryElts;
/* 1551 */         this.m_queryElts = null;
/*      */         try
/*      */         {
/* 1554 */           buildClauseElement(clauseType, getDocAccountClauseField(), "equals", this.m_queryConfig.getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", this.m_currentEngine, "EmptyValue"), false, clause);
/*      */ 
/* 1558 */           if ((clauseType.equalsIgnoreCase("database")) || (this.m_currentEngine.startsWith("DATABASE")))
/*      */           {
/* 1561 */             buildClauseConjunction(clauseType, "OR", clause);
/* 1562 */             buildClauseElement(clauseType, getDocAccountClauseField(), "isnull", "", false, clause);
/*      */           }
/*      */         }
/*      */         finally
/*      */         {
/* 1567 */           this.m_queryElts = curQueryElts;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1572 */         String docAccountClause = getDocAccountClauseField();
/*      */ 
/* 1574 */         buildClauseElement(clauseType, docAccountClause, "beginsWith", acct, false, clause);
/*      */       }
/*      */     }
/*      */ 
/* 1578 */     if (!hasGroupClause)
/*      */       return;
/* 1580 */     if (this.m_queryElts != null)
/*      */     {
/* 1582 */       this.m_queryElts.m_rawParsedElements.add(")");
/*      */     }
/* 1584 */     clause.append(')');
/*      */   }
/*      */ 
/*      */   protected void buildUserClause(UserData userData, DataBinder binder, int privilege, String clauseType, IdcStringBuilder clause)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1595 */     String permStr = null;
/* 1596 */     if ((privilege & 0x8) != 0)
/*      */     {
/* 1598 */       permStr = "RWDA";
/*      */     }
/* 1600 */     else if ((privilege & 0x4) != 0)
/*      */     {
/* 1602 */       permStr = "RWD";
/*      */     }
/* 1604 */     else if ((privilege & 0x2) != 0)
/*      */     {
/* 1606 */       permStr = "RW";
/*      */     }
/* 1608 */     else if ((privilege & 0x1) != 0)
/*      */     {
/* 1610 */       permStr = "R";
/*      */     }
/*      */ 
/* 1613 */     boolean hasGroupClause = clause.length() > 0;
/* 1614 */     if (hasGroupClause)
/*      */     {
/* 1616 */       if (this.m_queryElts != null)
/*      */       {
/* 1618 */         this.m_queryElts.m_rawParsedElements.add(0, "(");
/* 1619 */         this.m_queryElts.m_rawParsedElements.add(")");
/*      */       }
/* 1621 */       clause.insert(0, '(');
/* 1622 */       clause.append(')');
/*      */     }
/* 1624 */     buildClauseConjunction(clauseType, "AND", clause);
/*      */ 
/* 1627 */     if (this.m_queryElts != null)
/*      */     {
/* 1629 */       this.m_queryElts.m_rawParsedElements.add("(");
/*      */     }
/* 1631 */     clause.append("(");
/*      */ 
/* 1633 */     List entityValues = createEntityList();
/*      */ 
/* 1636 */     AliasData aliasData = (AliasData)SharedObjects.getTable("Alias");
/* 1637 */     String[][] aliasMap = aliasData.getAliasesForUser(userData.m_name);
/* 1638 */     int nalias = aliasMap.length;
/*      */ 
/* 1640 */     boolean hasEntityValues = false;
/* 1641 */     this.m_service.setCachedObject("DocumentAccessSecurity", this);
/* 1642 */     this.m_service.setCachedObject("EntityList", entityValues);
/* 1643 */     this.m_service.setCachedObject("EntityUserData", userData);
/* 1644 */     this.m_service.setReturnValue(null);
/* 1645 */     this.m_service.executeFilter("hasEntityValues");
/* 1646 */     Object returnVal = this.m_service.getReturnValue();
/* 1647 */     if ((returnVal != null) && (returnVal instanceof Boolean))
/*      */     {
/* 1649 */       hasEntityValues = ((Boolean)returnVal).booleanValue();
/*      */     }
/* 1651 */     if ((nalias > 0) || (hasEntityValues))
/*      */     {
/* 1653 */       if (this.m_queryElts != null)
/*      */       {
/* 1655 */         this.m_queryElts.m_rawParsedElements.add("(");
/*      */       }
/* 1657 */       clause.append('(');
/*      */     }
/*      */ 
/* 1660 */     ExecutionContext ctxt = null;
/* 1661 */     if ((clauseType.equalsIgnoreCase("search")) && 
/* 1663 */       (this.m_queryConfig != null))
/*      */     {
/* 1665 */       ctxt = new ExecutionContextAdaptor();
/* 1666 */       ctxt.setCachedObject("CommonSearchConfig", this.m_queryConfig);
/*      */     }
/*      */ 
/* 1670 */     for (EntityValue entityValue : entityValues)
/*      */     {
/* 1672 */       String field = entityValue.m_field;
/* 1673 */       String clauseField = getEntityListClauseField(entityValue.m_field);
/* 1674 */       entityValue.m_clauseField = clauseField;
/* 1675 */       boolean isSecurityField = false;
/* 1676 */       if (clauseType.equalsIgnoreCase("search"))
/*      */       {
/* 1678 */         isSecurityField = SearchLoader.isSecurityField(field, ctxt);
/* 1679 */         entityValue.m_isSecurityField = isSecurityField;
/*      */       }
/*      */ 
/* 1682 */       String op = null;
/* 1683 */       String val = null;
/* 1684 */       if (isSecurityField)
/*      */       {
/* 1686 */         op = "hasAsWord";
/*      */       }
/*      */       else
/*      */       {
/* 1690 */         op = "hasAsSubstring";
/*      */       }
/*      */ 
/* 1693 */       if (entityValue.m_type.equals("user"))
/*      */       {
/* 1695 */         if (isSecurityField)
/*      */         {
/* 1697 */           val = new StringBuilder().append(entityValue.m_symbol).append(userData.m_name).toString();
/*      */         }
/*      */         else
/*      */         {
/* 1701 */           val = new StringBuilder().append(entityValue.m_symbol).append(userData.m_name).append('(').append(permStr).toString();
/*      */         }
/* 1703 */         buildClauseElement(clauseType, clauseField, op, val, false, clause);
/*      */       }
/* 1705 */       else if (entityValue.m_type.equals("alias"))
/*      */       {
/* 1708 */         if (nalias > m_maxAliasesInSecurityClause)
/*      */         {
/* 1710 */           int lastReportInMin = SharedObjects.getEnvironmentInt("maxSecurityAttributesLimitReached", 0);
/*      */ 
/* 1712 */           Calendar rightNow = Calendar.getInstance();
/*      */ 
/* 1714 */           if (rightNow.getTimeInMillis() / 60000L - lastReportInMin > 1440L)
/*      */           {
/* 1716 */             IdcMessage msg = new IdcMessage("csLogTooManySecurityAttributes", new Object[] { this.m_userData.m_name, "Aliases" });
/*      */ 
/* 1718 */             Report.info(null, null, msg);
/* 1719 */             SharedObjects.putEnvironmentValue("maxSecurityAttributesLimitReached", Long.toString(rightNow.getTimeInMillis() / 60000L));
/*      */           }
/* 1721 */           IdcMessage msg = new IdcMessage("csAlertTooManySecurityAttributes", new Object[] { this.m_userData.m_name, Integer.valueOf(nalias), "Aliases", Integer.valueOf(m_maxAliasesInSecurityClause) });
/* 1722 */           Report.trace("alerts", null, msg);
/*      */ 
/* 1724 */           AlertUtils.createOneTimeAlert(msg, this.m_service.m_parentCxt, this.m_userData.m_name);
/* 1725 */           binder.putLocal("IsMaxSecurityAttributesLimitReached", "true");
/*      */ 
/* 1728 */           String truncated = "";
/* 1729 */           for (int i = m_maxAliasesInSecurityClause; i < nalias; ++i)
/*      */           {
/* 1731 */             truncated = new StringBuilder().append(truncated).append((i == m_maxAliasesInSecurityClause) ? aliasMap[i][0] : new StringBuilder().append(",").append(aliasMap[i][0]).toString()).toString();
/*      */           }
/* 1733 */           binder.putLocal("TruncatedAliases", truncated);
/*      */ 
/* 1735 */           nalias = m_maxAliasesInSecurityClause;
/*      */         }
/*      */ 
/* 1738 */         for (int i = 0; i < nalias; ++i)
/*      */         {
/* 1740 */           buildClauseConjunction(clauseType, "OR", clause);
/* 1741 */           if (isSecurityField)
/*      */           {
/* 1743 */             val = new StringBuilder().append(entityValue.m_symbol).append(aliasMap[i][0]).toString();
/*      */           }
/*      */           else
/*      */           {
/* 1747 */             val = new StringBuilder().append(entityValue.m_symbol).append(aliasMap[i][0]).append('(').append(permStr).toString();
/*      */           }
/* 1749 */           buildClauseElement(clauseType, clauseField, op, val, false, clause);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1754 */         this.m_service.setCachedObject("Clause", clause);
/* 1755 */         this.m_service.setCachedObject("EntityUserData", userData);
/* 1756 */         this.m_service.setCachedObject("ClauseType", clauseType);
/* 1757 */         this.m_service.setCachedObject("EntityValue", entityValue);
/* 1758 */         this.m_service.setCachedObject("Operator", op);
/* 1759 */         this.m_service.setCachedObject("PermissionString", permStr);
/* 1760 */         this.m_service.executeFilter("buildUserClause");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1765 */     if (SecurityUtils.m_accessListPrivilegesGrantedWhenEmpty)
/*      */     {
/* 1767 */       String emptyValue = this.m_queryConfig.getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", this.m_currentEngine, "EmptyValue");
/*      */ 
/* 1769 */       buildClauseConjunction(clauseType, "OR", clause);
/* 1770 */       if (this.m_queryElts != null)
/*      */       {
/* 1772 */         this.m_queryElts.m_rawParsedElements.add("(");
/*      */       }
/* 1774 */       clause.append("(");
/* 1775 */       boolean isFirst = true;
/* 1776 */       for (EntityValue entityValue : entityValues)
/*      */       {
/* 1778 */         if (!isFirst)
/*      */         {
/* 1780 */           buildClauseConjunction(clauseType, "AND", clause);
/*      */         }
/* 1782 */         isFirst = false;
/*      */ 
/* 1784 */         if (this.m_queryElts != null)
/*      */         {
/* 1786 */           this.m_queryElts.m_rawParsedElements.add("(");
/*      */         }
/* 1788 */         clause.append("(");
/*      */ 
/* 1790 */         buildClauseElement(clauseType, entityValue.m_clauseField, "equals", emptyValue, false, clause);
/*      */ 
/* 1792 */         buildClauseConjunction(clauseType, "OR", clause);
/* 1793 */         buildClauseElement(clauseType, entityValue.m_clauseField, "isnull", "", false, clause);
/*      */ 
/* 1795 */         if (this.m_queryElts != null)
/*      */         {
/* 1797 */           this.m_queryElts.m_rawParsedElements.add(")");
/*      */         }
/* 1799 */         clause.append(')');
/*      */       }
/* 1801 */       if (this.m_queryElts != null)
/*      */       {
/* 1803 */         this.m_queryElts.m_rawParsedElements.add(")");
/*      */       }
/* 1805 */       clause.append(")");
/*      */     }
/*      */ 
/* 1808 */     if ((nalias > 0) || (hasEntityValues))
/*      */     {
/* 1810 */       if (this.m_queryElts != null)
/*      */       {
/* 1812 */         this.m_queryElts.m_rawParsedElements.add(")");
/*      */       }
/* 1814 */       clause.append(')');
/*      */     }
/*      */ 
/* 1818 */     if (this.m_queryElts != null)
/*      */     {
/* 1820 */       this.m_queryElts.m_rawParsedElements.add(")");
/*      */     }
/* 1822 */     clause.append(")");
/*      */   }
/*      */ 
/*      */   public void buildParsedClauseElement(String paramName, String operator, String paramValue, boolean isNot)
/*      */     throws ServiceException
/*      */   {
/* 1836 */     if (this.m_queryElts == null)
/*      */     {
/* 1838 */       return;
/*      */     }
/* 1840 */     int opCode = SearchQueryUtils.convertToOperatorConstant(operator);
/* 1841 */     if (isNot)
/*      */     {
/* 1844 */       opCode |= 256;
/*      */     }
/* 1846 */     QueryElement qElt = SearchQueryUtils.createQueryElement(paramName, opCode, paramValue, null, this.m_binder, this.m_queryConfig, this.m_service);
/* 1847 */     this.m_queryElts.m_rawParsedElements.add(qElt);
/*      */   }
/*      */ 
/*      */   public void buildClauseElement(String clauseType, String paramName, String operator, String paramValue, boolean isNot, IdcAppendable buffer)
/*      */     throws ServiceException
/*      */   {
/* 1853 */     boolean isZone = false;
/* 1854 */     if (clauseType.equalsIgnoreCase("search"))
/*      */     {
/* 1856 */       isZone = SearchLoader.isSecurityField(paramName, this.m_service);
/*      */     }
/*      */ 
/* 1859 */     if (this.m_queryElts != null)
/*      */     {
/* 1861 */       buildParsedClauseElement(paramName, operator, paramValue, isNot);
/*      */     }
/*      */ 
/* 1865 */     ParsedQueryElements curElts = this.m_queryElts;
/* 1866 */     this.m_queryElts = null;
/*      */     try
/*      */     {
/* 1869 */       buildClauseElementEx(clauseType, paramName, operator, paramValue, isNot, buffer, isZone);
/*      */     }
/*      */     finally
/*      */     {
/* 1873 */       this.m_queryElts = curElts;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildClauseElementEx(String clauseType, String paramName, String operator, String paramValue, boolean isNot, IdcAppendable buffer, boolean isZone)
/*      */     throws ServiceException
/*      */   {
/* 1880 */     buffer.append('(');
/* 1881 */     if (isNot)
/*      */     {
/* 1883 */       if (this.m_supportCombinedNotOp)
/*      */       {
/* 1885 */         operator = new StringBuilder().append("not").append(operator).toString();
/*      */       }
/*      */       else
/*      */       {
/* 1889 */         buildClauseConjunction(clauseType, "NOT", buffer);
/*      */       }
/*      */     }
/* 1892 */     String queryConfig = null;
/* 1893 */     if (clauseType.equalsIgnoreCase("search"))
/*      */     {
/* 1895 */       if (isZone)
/*      */       {
/* 1897 */         queryConfig = this.m_queryConfig.getStringFromTableWithInheritance("SearchQueryDefinition", "SearchEngineName", this.m_currentEngine, "ZoneSearchQueryLabel");
/*      */ 
/* 1899 */         if ((queryConfig != null) && (queryConfig.length() == 0))
/*      */         {
/* 1901 */           queryConfig = null;
/*      */         }
/*      */ 
/* 1907 */         if ((this.m_supportCombinedNotOp) && (isNot))
/*      */         {
/* 1909 */           operator = new StringBuilder().append("notZone").append(operator.substring(3)).toString();
/*      */         }
/*      */         else
/*      */         {
/* 1913 */           operator = new StringBuilder().append("zone").append(operator).toString();
/*      */         }
/*      */       }
/*      */ 
/* 1917 */       if ((this.m_currentEngine != null) && (this.m_currentEngine.equals("DATABASE")))
/*      */       {
/* 1919 */         paramName = prependTableName(paramName);
/*      */       }
/* 1921 */       appendQueryElement(buffer, operator, queryConfig, paramName, paramValue);
/*      */     }
/* 1923 */     else if (clauseType.equalsIgnoreCase("database"))
/*      */     {
/* 1925 */       if (this.m_appendQueryTableName)
/*      */       {
/* 1927 */         paramName = prependTableName(paramName);
/*      */       }
/*      */ 
/* 1930 */       appendQueryElement(buffer, operator, "DATABASE", paramName, paramValue);
/*      */     }
/*      */     else
/*      */     {
/* 1934 */       String errMsg = LocaleUtils.encodeMessage("csInvalidWhereClauseType", null, clauseType);
/* 1935 */       throw new ServiceException(errMsg);
/*      */     }
/*      */ 
/* 1938 */     buffer.append(')');
/*      */   }
/*      */ 
/*      */   public String prependTableName(String fieldName)
/*      */   {
/* 1948 */     if (this.m_columnTableMap != null)
/*      */     {
/* 1950 */       String tableName = (String)this.m_columnTableMap.get(fieldName);
/* 1951 */       if ((tableName != null) && (tableName.length() > 0))
/*      */       {
/* 1953 */         fieldName = new StringBuilder().append(tableName).append('.').append(fieldName).toString();
/*      */       }
/*      */     }
/*      */ 
/* 1957 */     return fieldName;
/*      */   }
/*      */ 
/*      */   protected String computeHexFieldValue(String paramValue)
/*      */   {
/* 1964 */     IdcStringBuilder buff = new IdcStringBuilder();
/*      */     try
/*      */     {
/* 1967 */       byte[] b = paramValue.getBytes("UTF8");
/* 1968 */       int len = b.length;
/* 1969 */       if ((len > 0) && (!paramValue.equalsIgnoreCase("idcnull")))
/*      */       {
/* 1971 */         buff.append("z");
/* 1972 */         for (int j = 0; j < len; ++j)
/*      */         {
/* 1974 */           NumberUtils.appendHexByte(buff, b[j]);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1979 */         buff.append("idcnull");
/*      */       }
/*      */     }
/*      */     catch (UnsupportedEncodingException e)
/*      */     {
/*      */     }
/*      */ 
/* 1986 */     return buff.toString();
/*      */   }
/*      */ 
/*      */   protected void appendQueryElement(IdcAppendable buffer, String op, String queryConfig, String name, String value)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1994 */       this.m_queryConfig.appendClauseElement(buffer, op, queryConfig, name, value);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1998 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void buildClauseConjunction(String clauseType, String conjunction, IdcAppendable buffer)
/*      */     throws ServiceException
/*      */   {
/* 2005 */     if (this.m_queryElts != null)
/*      */     {
/* 2007 */       Integer conjCode = null;
/* 2008 */       if (conjunction.equalsIgnoreCase("and"))
/*      */       {
/* 2010 */         conjCode = new Integer(16);
/*      */       }
/* 2012 */       else if (conjunction.equalsIgnoreCase("or"))
/*      */       {
/* 2014 */         conjCode = new Integer(17);
/*      */       }
/* 2016 */       else if (conjunction.equalsIgnoreCase("not"))
/*      */       {
/* 2018 */         conjCode = new Integer(18);
/*      */       }
/*      */       else
/*      */       {
/* 2022 */         ServiceException e = new ServiceException(new StringBuilder().append("Illegal conjuction ").append(conjunction).append(" used in security clause construction").toString());
/*      */ 
/* 2024 */         Report.trace("system", null, e);
/*      */       }
/* 2026 */       if (conjCode != null)
/*      */       {
/* 2028 */         this.m_queryElts.m_rawParsedElements.add(conjCode);
/*      */       }
/*      */     }
/* 2031 */     if (clauseType.equalsIgnoreCase("Search"))
/*      */     {
/* 2033 */       appendQueryElement(buffer, conjunction, null, null, null);
/*      */     }
/* 2035 */     else if (clauseType.equalsIgnoreCase("Database"))
/*      */     {
/* 2037 */       appendQueryElement(buffer, conjunction, "DATABASE", null, null);
/*      */     }
/*      */     else
/*      */     {
/* 2041 */       String errMsg = LocaleUtils.encodeMessage("csInvalidWhereClauseType", null, clauseType);
/* 2042 */       throw new ServiceException(errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean hasEntityPrivilege(String userName, List<EntityValue> entityValues, int privilege)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2049 */     String privStr = getPrivilegeString(privilege);
/* 2050 */     boolean hasEntityList = false;
/* 2051 */     for (EntityValue entityValue : entityValues)
/*      */     {
/* 2053 */       String entityList = entityValue.m_entityListStr;
/* 2054 */       if (entityList == null) continue; if (entityList.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2058 */       hasEntityList = true;
/* 2059 */       if (entityValue.m_type.equals("user"))
/*      */       {
/* 2062 */         entityList = entityList.toLowerCase();
/* 2063 */         String userClause = new StringBuilder().append("&").append(userName.toLowerCase()).append("(").append(privStr).toString();
/*      */ 
/* 2065 */         if (entityList.indexOf(userClause) >= 0)
/*      */         {
/* 2067 */           return true;
/*      */         }
/*      */       }
/* 2070 */       else if (entityValue.m_type.equals("alias"))
/*      */       {
/* 2072 */         entityList = entityList.toLowerCase();
/*      */ 
/* 2074 */         AliasData aliasData = (AliasData)SharedObjects.getTable("Alias");
/* 2075 */         if (aliasData == null)
/*      */         {
/* 2077 */           aliasData = new AliasData();
/*      */         }
/* 2079 */         String[][] aliasList = aliasData.getAliasesForUser(userName);
/* 2080 */         int numAlias = aliasList.length;
/*      */ 
/* 2082 */         for (int j = 0; j < numAlias; ++j)
/*      */         {
/* 2084 */           String alias = aliasList[j][0];
/* 2085 */           String aliasClause = new StringBuilder().append("@").append(alias.toLowerCase()).append("(").append(privStr).toString();
/*      */ 
/* 2087 */           if (entityList.indexOf(aliasClause) >= 0)
/*      */           {
/* 2089 */             return true;
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2095 */         this.m_service.setCachedObject("DocumentAccessSecurity", this);
/* 2096 */         this.m_service.setCachedObject("EntityValue", entityValue);
/* 2097 */         this.m_service.setCachedObject("PrivilegeString", privStr);
/* 2098 */         this.m_service.setCachedObject("EntityUserData", this.m_service.m_userData);
/* 2099 */         this.m_service.setReturnValue(null);
/* 2100 */         this.m_service.executeFilter("hasEntityPrivilege");
/* 2101 */         Object returnVal = this.m_service.getReturnValue();
/* 2102 */         if ((returnVal != null) && (returnVal instanceof Boolean) && 
/* 2104 */           (((Boolean)returnVal).booleanValue()))
/*      */         {
/* 2106 */           return true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2114 */     return (SecurityUtils.m_accessListPrivilegesGrantedWhenEmpty) && (!hasEntityList);
/*      */   }
/*      */ 
/*      */   public String addEntitySymbols(EntityValue entityValue, boolean isValidate)
/*      */     throws ServiceException
/*      */   {
/* 2122 */     IdcStringBuilder buffer = new IdcStringBuilder();
/* 2123 */     String entityListStr = entityValue.m_entityListStr;
/* 2124 */     char symbol = entityValue.m_symbol;
/*      */ 
/* 2126 */     String entityType = entityValue.m_type;
/* 2127 */     Vector entityList = StringUtils.parseArray(entityListStr, ',', '^');
/* 2128 */     boolean firstTime = true;
/* 2129 */     for (String entity : entityList)
/*      */     {
/* 2131 */       entity = entity.trim();
/* 2132 */       String[] entityValues = SecurityAccessListUtils.parseSecurityFlags(entity, "");
/* 2133 */       if ((entityValues[0] == null) || (entityValues[0].equals("")) || (entityValues[1].equals("")))
/*      */       {
/* 2136 */         if (!isValidate)
/*      */           continue;
/* 2138 */         String errMsg = LocaleUtils.encodeMessage("csInvalidEntityFormat", null, entityType, entity);
/*      */ 
/* 2140 */         throw new ServiceException(errMsg);
/*      */       }
/*      */ 
/* 2146 */       if (!firstTime)
/*      */       {
/* 2148 */         buffer.append(',');
/*      */       }
/* 2150 */       firstTime = false;
/*      */ 
/* 2152 */       if (entity.charAt(0) != symbol)
/*      */       {
/* 2154 */         buffer.append(symbol);
/*      */       }
/*      */ 
/* 2158 */       String user = entityValues[0].trim();
/* 2159 */       int priv = NumberUtils.parseInteger(entityValues[1], 0);
/* 2160 */       String privStr = SecurityAccessListUtils.makePrivilegeStr(priv);
/* 2161 */       buffer.append(new StringBuilder().append(user).append("(").append(privStr).append(")").toString());
/*      */     }
/*      */ 
/* 2164 */     return buffer.toString();
/*      */   }
/*      */ 
/*      */   public boolean hasEntityFieldsChanged(String oldEntityListStr, String newEntityListStr)
/*      */   {
/* 2169 */     Vector oldEntityList = StringUtils.parseArray(oldEntityListStr, ',', '^');
/* 2170 */     String[] oldEntity = StringUtils.convertListToArray(oldEntityList);
/*      */ 
/* 2172 */     Vector newEntityList = StringUtils.parseArray(newEntityListStr, ',', '^');
/* 2173 */     String[] newEntity = StringUtils.convertListToArray(newEntityList);
/*      */ 
/* 2175 */     int length = oldEntity.length;
/* 2176 */     if (newEntity.length != length)
/*      */     {
/* 2178 */       return true;
/*      */     }
/*      */ 
/* 2181 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2183 */       boolean foundMatch = false;
/* 2184 */       for (int j = 0; j < length; ++j)
/*      */       {
/* 2186 */         if (!oldEntity[i].equalsIgnoreCase(newEntity[j]))
/*      */           continue;
/* 2188 */         foundMatch = true;
/*      */       }
/*      */ 
/* 2192 */       if (!foundMatch)
/*      */       {
/* 2194 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 2198 */     return false;
/*      */   }
/*      */ 
/*      */   public String getPrivilegeString(int privilege)
/*      */   {
/* 2203 */     String privStr = "";
/*      */ 
/* 2205 */     if ((privilege & 0x1) != 0)
/*      */     {
/* 2207 */       privStr = "r";
/*      */     }
/* 2209 */     else if ((privilege & 0x2) != 0)
/*      */     {
/* 2211 */       privStr = "rw";
/*      */     }
/* 2213 */     else if ((privilege & 0x4) != 0)
/*      */     {
/* 2215 */       privStr = "rwd";
/*      */     }
/* 2217 */     else if ((privilege & 0x8) != 0)
/*      */     {
/* 2219 */       privStr = "rwda";
/*      */     }
/*      */ 
/* 2222 */     return privStr;
/*      */   }
/*      */ 
/*      */   public boolean isSpecialAuthGroup(String securityGroup)
/*      */   {
/* 2227 */     String specialAuthGroupsStr = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 2228 */     Vector authGroupList = StringUtils.parseArray(specialAuthGroupsStr, ',', '^');
/* 2229 */     int size = authGroupList.size();
/* 2230 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 2232 */       String authGroup = (String)authGroupList.elementAt(i);
/* 2233 */       if (authGroup.equalsIgnoreCase(securityGroup))
/*      */       {
/* 2235 */         return true;
/*      */       }
/*      */     }
/*      */ 
/* 2239 */     return false;
/*      */   }
/*      */ 
/*      */   public String getSecurityGroupClauseField()
/*      */   {
/* 2244 */     if (this.m_securityGroupClauseField == null)
/*      */     {
/* 2246 */       if (this.m_fieldNameMap != null)
/*      */       {
/* 2248 */         this.m_securityGroupClauseField = ((String)this.m_fieldNameMap.get("dSecurityGroup"));
/*      */       }
/*      */ 
/* 2251 */       if (this.m_securityGroupClauseField == null)
/*      */       {
/* 2253 */         this.m_securityGroupClauseField = "dSecurityGroup";
/*      */       }
/*      */     }
/*      */ 
/* 2257 */     return this.m_securityGroupClauseField;
/*      */   }
/*      */ 
/*      */   public String getDocAccountClauseField()
/*      */   {
/* 2262 */     if (this.m_docAccountClauseField == null)
/*      */     {
/* 2264 */       if (this.m_fieldNameMap != null)
/*      */       {
/* 2266 */         this.m_docAccountClauseField = ((String)this.m_fieldNameMap.get("dDocAccount"));
/*      */       }
/*      */ 
/* 2269 */       if (this.m_docAccountClauseField == null)
/*      */       {
/* 2271 */         this.m_docAccountClauseField = "dDocAccount";
/*      */       }
/*      */     }
/*      */ 
/* 2275 */     return this.m_docAccountClauseField;
/*      */   }
/*      */ 
/*      */   public String getEntityListClauseField(String field)
/*      */   {
/* 2280 */     String clauseField = null;
/* 2281 */     if (this.m_fieldNameMap != null)
/*      */     {
/* 2283 */       clauseField = (String)this.m_fieldNameMap.get(field);
/*      */     }
/* 2285 */     if (clauseField == null)
/*      */     {
/* 2287 */       clauseField = field;
/*      */     }
/* 2289 */     return clauseField;
/*      */   }
/*      */ 
/*      */   public String getClbraUserListClauseField()
/*      */   {
/* 2294 */     if (this.m_clbraUserListClauseField == null)
/*      */     {
/* 2296 */       if (this.m_fieldNameMap != null)
/*      */       {
/* 2298 */         this.m_clbraUserListClauseField = ((String)this.m_fieldNameMap.get("xClbraUserList"));
/*      */       }
/*      */ 
/* 2301 */       if (this.m_clbraUserListClauseField == null)
/*      */       {
/* 2303 */         this.m_clbraUserListClauseField = "xClbraUserList";
/*      */       }
/*      */     }
/*      */ 
/* 2307 */     return this.m_clbraUserListClauseField;
/*      */   }
/*      */ 
/*      */   public String getClbraAliasListClauseField()
/*      */   {
/* 2312 */     if (this.m_clbraAliasListClauseField == null)
/*      */     {
/* 2314 */       if (this.m_fieldNameMap != null)
/*      */       {
/* 2316 */         this.m_clbraAliasListClauseField = ((String)this.m_fieldNameMap.get("xClbraAliasList"));
/*      */       }
/*      */ 
/* 2319 */       if (this.m_clbraAliasListClauseField == null)
/*      */       {
/* 2321 */         this.m_clbraAliasListClauseField = "xClbraAliasList";
/*      */       }
/*      */     }
/*      */ 
/* 2325 */     return this.m_clbraAliasListClauseField;
/*      */   }
/*      */ 
/*      */   public boolean didProperSecurityCheck()
/*      */   {
/* 2330 */     return (!this.m_ensureSecurityCheck) || (this.m_hasCheckedSecurity);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2335 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 106101 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocumentAccessSecurity
 * JD-Core Version:    0.5.4
 */