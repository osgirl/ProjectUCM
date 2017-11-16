/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.workflow.WfCompanionManager;
/*     */ import intradoc.server.workflow.WorkflowDocImplementor;
/*     */ import intradoc.server.workflow.WorkflowUtils;
/*     */ import intradoc.shared.CollaborationData;
/*     */ import intradoc.shared.CollaborationUtils;
/*     */ import intradoc.shared.Collaborations;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CollaborationService extends Service
/*     */ {
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  66 */     super.createHandlersForService();
/*  67 */     createHandlers("CollaborationService");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getCollaborations()
/*     */     throws DataException, ServiceException
/*     */   {
/*  75 */     DataResultSet drset = Collaborations.computeUserCollaborationSet(this.m_userData, this, 1);
/*     */ 
/*  77 */     this.m_binder.addResultSet("Collaborations", drset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getCollaborationData()
/*     */     throws DataException, ServiceException
/*     */   {
/*  84 */     String name = this.m_binder.get("dClbraName");
/*     */ 
/*  86 */     CollaborationData clbraData = Collaborations.getOrCreateCollaborationData(name, false);
/*  87 */     this.m_binder.merge(clbraData.m_data);
/*     */ 
/*  89 */     boolean isCombinedACL = StringUtils.convertToBool(this.m_binder.getLocal("isCombinedACL"), false);
/*     */ 
/*  92 */     DataBinder binder = clbraData.m_data;
/*  93 */     String userStr = binder.getLocal("UserAccessList");
/*  94 */     String aliasStr = binder.getLocal("AliasAccessList");
/*     */ 
/*  96 */     Vector users = StringUtils.parseArray(userStr, ',', '^');
/*  97 */     Vector aliases = StringUtils.parseArray(aliasStr, ',', '^');
/*     */ 
/* 100 */     Vector pList = new IdcVector();
/* 101 */     CollaborationUtils.createAccessPresentationStr(pList, users, isCombinedACL, false, null);
/*     */ 
/* 104 */     CollaborationUtils.createAccessPresentationStr(pList, aliases, isCombinedACL, true, "@");
/*     */ 
/* 106 */     if (isCombinedACL)
/*     */     {
/* 108 */       String clbraAccessStr = StringUtils.createString(pList, '\n', '*');
/* 109 */       this.m_binder.putLocal("clbraAccessList", clbraAccessStr);
/*     */     }
/*     */     else
/*     */     {
/* 114 */       this.m_binder.addOptionList("AccessList", pList);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void validateCollaboration()
/*     */     throws DataException, ServiceException
/*     */   {
/* 124 */     String str = this.m_binder.getLocal("clbraAccessList");
/* 125 */     boolean isEditAccessList = str != null;
/* 126 */     if (isEditAccessList)
/*     */     {
/* 128 */       Vector aList = StringUtils.parseArray(str, '\n', '*');
/*     */ 
/* 130 */       Vector aliasList = new IdcVector();
/* 131 */       Hashtable aliasMap = new Hashtable();
/* 132 */       Vector userList = new IdcVector();
/* 133 */       Hashtable userMap = new Hashtable();
/*     */ 
/* 138 */       int num = aList.size();
/* 139 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 141 */         String val = (String)aList.elementAt(i);
/* 142 */         String[] info = SecurityAccessListUtils.parseSecurityFlags(val, "0");
/* 143 */         if (info[0] == null)
/*     */         {
/* 145 */           String errMsg = LocaleUtils.encodeMessage("csClbraAccessListFormatError", null, val);
/* 146 */           createServiceException(null, errMsg);
/*     */         }
/*     */ 
/* 149 */         String name = info[0].trim();
/* 150 */         if (name.length() == 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 154 */         if (name.startsWith("@"))
/*     */         {
/* 156 */           name = name.substring(1);
/* 157 */           Object obj = aliasMap.get(name);
/* 158 */           if (obj == null)
/*     */           {
/* 160 */             aliasList.addElement(name);
/* 161 */             aliasList.addElement(info[1]);
/* 162 */             aliasMap.put(name, name);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 167 */           Object obj = userMap.get(name);
/* 168 */           if (obj != null)
/*     */             continue;
/* 170 */           userList.addElement(name);
/* 171 */           userList.addElement(info[1]);
/* 172 */           userMap.put(name, name);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 177 */       String aliasStr = StringUtils.createString(aliasList, ',', '^');
/* 178 */       String userStr = StringUtils.createString(userList, ',', '^');
/*     */ 
/* 181 */       this.m_binder.putLocal("NewAliasAccessList", aliasStr);
/* 182 */       this.m_binder.putLocal("NewUserAccessList", userStr);
/*     */ 
/* 185 */       boolean isAdmin = SecurityUtils.isUserOfRole(this.m_userData, "admin");
/* 186 */       if (!isAdmin)
/*     */       {
/* 188 */         String userName = this.m_userData.m_name;
/* 189 */         boolean isFound = false;
/* 190 */         num = userList.size();
/* 191 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 193 */           String name = (String)userList.elementAt(i);
/* 194 */           ++i;
/*     */ 
/* 203 */           if (!name.equalsIgnoreCase(userName))
/*     */             continue;
/* 205 */           int priv = NumberUtils.parseInteger((String)userList.elementAt(i), 0);
/* 206 */           if (((priv & 0x8) == 0) && ((priv & 0x1) == 0))
/*     */           {
/* 209 */             createServiceException(null, "!csClbraMustHaveSufficientPrivileges");
/*     */           }
/* 211 */           isFound = true;
/* 212 */           break;
/*     */         }
/*     */ 
/* 215 */         if (!isFound)
/*     */         {
/* 217 */           createServiceException(null, "!csClbraMustHaveSufficientPrivileges");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 223 */     validateFields();
/*     */   }
/*     */ 
/*     */   protected void validateFields()
/*     */     throws DataException, ServiceException
/*     */   {
/* 229 */     String fieldsStr = this.m_binder.getLocal("ExtraClbraFields");
/* 230 */     Vector extraFields = StringUtils.parseArray(fieldsStr, ',', '^');
/*     */ 
/* 233 */     String[][] fields = { { "dClbraName", "1", "1" }, { "dClbraType", "1", "1" }, { "dClbraDescription", "0", "0" } };
/*     */ 
/* 241 */     IdcMessage errMsg = null;
/* 242 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/* 244 */       String key = fields[i][0];
/* 245 */       String value = this.m_binder.get(key);
/* 246 */       String errSegment = "csClbraFieldError_" + key;
/* 247 */       boolean isFileSegment = StringUtils.convertToBool(fields[i][1], false);
/* 248 */       boolean isRequired = StringUtils.convertToBool(fields[i][2], false);
/* 249 */       int defaultMaxLength = 50;
/* 250 */       if (key.equals("dClbraMailExcludeList"))
/*     */       {
/* 253 */         defaultMaxLength = 1000;
/*     */       }
/* 255 */       int maxLength = SharedObjects.getEnvironmentInt(key + ":maxLength", defaultMaxLength);
/*     */ 
/* 257 */       if ((!isRequired) && (value.trim().length() == 0))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 263 */       if (isFileSegment)
/*     */       {
/* 265 */         errMsg = Validation.checkUrlFileSegmentForDB(value, errSegment, maxLength, null);
/*     */       }
/*     */       else
/*     */       {
/* 269 */         errMsg = Validation.checkFormFieldForDB(value, errSegment, maxLength, null);
/*     */       }
/*     */ 
/* 272 */       if (errMsg == null)
/*     */         continue;
/* 274 */       createServiceException(null, LocaleUtils.encodeMessage(errMsg));
/*     */     }
/*     */ 
/* 279 */     String[] prtFields = { "clbraAccessList", "AliasAccessList", "UserAccessList" };
/* 280 */     for (int i = 0; i < prtFields.length; ++i)
/*     */     {
/* 282 */       String key = prtFields[i];
/* 283 */       int num = extraFields.size();
/* 284 */       for (int j = 0; j < num; ++j)
/*     */       {
/* 286 */         String field = (String)extraFields.elementAt(i);
/* 287 */         if (!field.equals(key))
/*     */           continue;
/* 289 */         extraFields.removeElementAt(j);
/*     */ 
/* 292 */         --num;
/* 293 */         --j;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addCollaboration()
/*     */     throws DataException, ServiceException
/*     */   {
/* 303 */     checkFeatureAllowed("Collaboration");
/*     */ 
/* 306 */     Date dte = new Date();
/* 307 */     String createDate = LocaleUtils.formatODBC(dte);
/* 308 */     this.m_binder.m_localizedFields.put("dClbraCreateDate", "");
/* 309 */     this.m_binder.putLocal("dClbraCreateDate", createDate);
/*     */ 
/* 311 */     this.m_binder.putLocal("dClbraCreatedBy", this.m_userData.m_name);
/*     */ 
/* 313 */     addOrEditCollaboration(false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editCollaboration()
/*     */     throws DataException, ServiceException
/*     */   {
/* 320 */     Date dte = new Date();
/* 321 */     String createDate = LocaleUtils.formatODBC(dte);
/* 322 */     this.m_binder.m_localizedFields.put("dClbraChangeDate", "");
/* 323 */     this.m_binder.putLocal("dClbraChangeDate", createDate);
/*     */ 
/* 325 */     this.m_binder.putLocal("dClbraChangedBy", this.m_userData.m_name);
/*     */ 
/* 328 */     addOrEditCollaboration(true);
/*     */   }
/*     */ 
/*     */   protected void addOrEditCollaboration(boolean isNew) throws DataException, ServiceException
/*     */   {
/* 333 */     String str = this.m_binder.getLocal("clbraAccessList");
/* 334 */     boolean isEditAccessList = str != null;
/*     */ 
/* 337 */     String name = this.m_binder.get("dClbraName");
/* 338 */     String lockDir = Collaborations.getCollaborationDirectory();
/* 339 */     FileUtils.reserveDirectory(lockDir);
/*     */     try
/*     */     {
/* 342 */       CollaborationData clbraData = Collaborations.getOrCreateCollaborationData(name, isNew);
/*     */ 
/* 345 */       DataBinder binder = clbraData.m_data;
/* 346 */       if (isEditAccessList)
/*     */       {
/* 349 */         String oldAliases = binder.getLocal("AliasAccessList");
/* 350 */         String oldUsers = binder.getLocal("UserAccessList");
/* 351 */         if (oldAliases != null)
/*     */         {
/* 353 */           this.m_binder.putLocal("OldAliasAccessList", oldAliases);
/*     */         }
/* 355 */         if (oldUsers != null)
/*     */         {
/* 357 */           this.m_binder.putLocal("OldUserAccessList", oldUsers);
/*     */         }
/*     */ 
/* 362 */         String aliasStr = this.m_binder.getLocal("NewAliasAccessList");
/* 363 */         String userStr = this.m_binder.getLocal("NewUserAccessList");
/* 364 */         this.m_binder.putLocal("AliasAccessList", aliasStr);
/* 365 */         this.m_binder.putLocal("UserAccessList", userStr);
/*     */ 
/* 368 */         binder.putLocal("AliasAccessList", aliasStr);
/* 369 */         binder.putLocal("UserAccessList", userStr);
/*     */       }
/*     */ 
/* 373 */       String fieldsStr = this.m_binder.getLocal("ExtraClbraFields");
/* 374 */       Vector fields = StringUtils.parseArray(fieldsStr, ',', '^');
/* 375 */       int num = fields.size();
/* 376 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 378 */         String field = (String)fields.elementAt(i);
/* 379 */         String value = this.m_binder.getLocal(field);
/* 380 */         if (value == null)
/*     */         {
/* 382 */           value = "";
/*     */         }
/*     */ 
/* 385 */         binder.putLocal(field, value);
/*     */       }
/*     */ 
/* 388 */       Collaborations.writeCollaborationFile(clbraData);
/*     */     }
/*     */     finally
/*     */     {
/* 392 */       FileUtils.releaseDirectory(lockDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteCollaboration()
/*     */     throws DataException, ServiceException
/*     */   {
/* 402 */     String name = this.m_binder.get("dClbraName");
/*     */ 
/* 405 */     this.m_binder.putLocal("dDocAccount", "prj/" + name);
/* 406 */     ResultSet rset = this.m_workspace.createResultSet("QacctRevisions", this.m_binder);
/* 407 */     if (rset.isRowPresent())
/*     */     {
/* 409 */       createServiceException(null, "!csUnableToDeleteActiveClbra");
/*     */     }
/*     */ 
/* 412 */     String lockDir = Collaborations.getCollaborationDirectory();
/* 413 */     FileUtils.reserveDirectory(lockDir);
/*     */     try
/*     */     {
/* 416 */       Collaborations.deleteCollaboration(name, this.m_binder, true);
/*     */     }
/*     */     finally
/*     */     {
/* 420 */       FileUtils.releaseDirectory(lockDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyCollaborationUsers()
/*     */     throws DataException, ServiceException
/*     */   {
/* 431 */     String oldUserStr = this.m_binder.getLocal("OldUserAccessList");
/* 432 */     String oldAliasStr = this.m_binder.getLocal("OldAliasAccessList");
/* 433 */     String userStr = this.m_binder.getLocal("UserAccessList");
/* 434 */     String aliasStr = this.m_binder.getLocal("AliasAccessList");
/*     */ 
/* 436 */     Vector oldUsers = StringUtils.parseArray(oldUserStr, ',', '^');
/* 437 */     Vector oldAliases = StringUtils.parseArray(oldAliasStr, ',', '^');
/* 438 */     Vector users = StringUtils.parseArray(userStr, ',', '^');
/* 439 */     Vector aliases = StringUtils.parseArray(aliasStr, ',', '^');
/*     */ 
/* 441 */     Vector userList = new IdcVector();
/*     */ 
/* 443 */     WorkflowUtils.buildUsersList(oldUsers, oldAliases, userList, this.m_workspace, this, 0);
/*     */ 
/* 448 */     int numNotified = userList.size();
/*     */ 
/* 451 */     WorkflowUtils.buildUsersList(users, aliases, userList, this.m_workspace, this, 0);
/*     */ 
/* 455 */     for (int i = 0; i < numNotified; ++i)
/*     */     {
/* 457 */       userList.removeElementAt(0);
/*     */     }
/*     */ 
/* 460 */     if (userList.size() <= 0) {
/*     */       return;
/*     */     }
/* 463 */     String mailPage = this.m_currentAction.getParamAt(0);
/* 464 */     String subjectTmp = this.m_currentAction.getParamAt(1);
/*     */ 
/* 466 */     Properties props = DataBinderUtils.createMergedProperties(this.m_binder);
/* 467 */     props.put("mailTemplate", mailPage);
/* 468 */     props.put("mailSubject", subjectTmp);
/*     */ 
/* 470 */     props.put("mailUserInfos", InternetFunctions.createMailUserInfoString(userList));
/*     */ 
/* 472 */     Vector mailQueue = (Vector)getCachedObject("MailQueue");
/* 473 */     if (mailQueue == null)
/*     */     {
/* 475 */       mailQueue = new IdcVector();
/* 476 */       setCachedObject("MailQueue", mailQueue);
/*     */     }
/* 478 */     mailQueue.addElement(props);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateCollaborationCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 487 */     ResultSet rset = this.m_workspace.createResultSet("Qcollaborations", null);
/* 488 */     Collaborations.load(rset, false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateAffectedWorkflows()
/*     */     throws DataException, ServiceException
/*     */   {
/* 500 */     String clbraName = this.m_binder.get("dClbraName");
/* 501 */     ResultSetFilter filter = new ResultSetFilter(clbraName)
/*     */     {
/*     */       public int checkRow(String val, int curNumRows, Vector row)
/*     */       {
/* 505 */         if (curNumRows > 0)
/*     */         {
/* 507 */           return -1;
/*     */         }
/*     */ 
/* 510 */         if (CollaborationUtils.isInCollaboration(val, this.val$clbraName))
/*     */         {
/* 512 */           return 1;
/*     */         }
/* 514 */         return 0;
/*     */       }
/*     */     };
/* 518 */     WfCompanionManager.updateWorkflowItemsEx(this.m_workspace, "QclbraWfActiveDocs", "STEP_UPDATE", "dDocAccount", filter);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getUserCollaborationList()
/*     */     throws DataException, ServiceException
/*     */   {
/* 528 */     UserData userData = getUserData();
/*     */ 
/* 530 */     Vector clbraList = new IdcVector();
/*     */ 
/* 533 */     if (SecurityUtils.isAccountAccessible(userData, "prj", 1))
/*     */     {
/* 535 */       clbraList.addElement("[" + LocaleResources.getString("apAllProjects", this) + "]");
/*     */     }
/*     */ 
/* 538 */     DataResultSet clbraSet = SharedObjects.getTable("Collaborations");
/*     */ 
/* 540 */     int nameIndex = ResultSetUtils.getIndexMustExist(clbraSet, "dClbraName");
/* 541 */     for (clbraSet.first(); clbraSet.isRowPresent(); clbraSet.next())
/*     */     {
/* 543 */       String clbraName = clbraSet.getStringValue(nameIndex);
/* 544 */       String account = null;
/* 545 */       if (clbraName.equals(""))
/*     */       {
/* 547 */         account = "prj";
/*     */       }
/*     */       else
/*     */       {
/* 551 */         account = "prj/" + clbraName;
/*     */       }
/*     */ 
/* 555 */       if (!SecurityUtils.isAccountAccessible(userData, account, 1))
/*     */         continue;
/* 557 */       String displayAccount = CollaborationUtils.getPresentationString(account, this);
/* 558 */       clbraList.addElement(displayAccount);
/*     */     }
/*     */ 
/* 562 */     this.m_binder.addOptionList("ClbraList", clbraList);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getCollaborationDocs()
/*     */     throws DataException, ServiceException
/*     */   {
/* 577 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 578 */     String dspChoice = this.m_binder.getLocal("DisplayChoice");
/* 579 */     String clbraName = this.m_binder.get("dClbraName");
/*     */ 
/* 581 */     String dataSource = null;
/* 582 */     String whereClause = null;
/* 583 */     if ((dspChoice == null) || (dspChoice.length() == 0) || (dspChoice.equals("docsActiveNotWf")))
/*     */     {
/* 585 */       this.m_binder.putLocal("DisplayChoice", "docsActiveNotWf");
/* 586 */       dataSource = "WorkingDocs";
/* 587 */       whereClause = "Revisions.dStatus<>'DELETED' AND Revisions.dStatus<>'EXPIRED' AND Revisions.dStatus<>'RELEASED' AND Revisions.dDocAccount = 'prj/" + clbraName + "'";
/*     */     }
/* 590 */     else if (dspChoice.equals("docsInWf"))
/*     */     {
/* 593 */       String wfName = this.m_binder.get("DisplayParam");
/* 594 */       if (wfName.length() > 0)
/*     */       {
/* 596 */         this.m_binder.putLocal("dWfName", wfName);
/* 597 */         ResultSet rset = this.m_workspace.createResultSet("Qworkflow", this.m_binder);
/* 598 */         DataResultSet drset = new DataResultSet();
/* 599 */         drset.copy(rset);
/* 600 */         this.m_binder.addResultSet("WorkflowInfo", drset);
/* 601 */         if (drset.isRowPresent())
/*     */         {
/* 604 */           String className = "intradoc.server.workflow.WorkflowDocImplementor";
/* 605 */           WorkflowDocImplementor wfImplementor = (WorkflowDocImplementor)ComponentClassFactory.createClassInstance("WorkflowDocImplementor", className, "!csWorkflowImplementorMissing");
/*     */ 
/* 610 */           wfImplementor.init(this);
/* 611 */           wfImplementor.computeWfDocumentsInfo(rsetName);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 616 */     if (dataSource == null)
/*     */       return;
/* 618 */     this.m_binder.putLocal("resultName", rsetName);
/* 619 */     this.m_binder.putLocal("dataSource", dataSource);
/* 620 */     if (whereClause != null)
/*     */     {
/* 622 */       this.m_binder.putLocal("whereClause", whereClause);
/*     */     }
/*     */ 
/* 625 */     createResultSetSQL();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 633 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77191 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.CollaborationService
 * JD-Core Version:    0.5.4
 */