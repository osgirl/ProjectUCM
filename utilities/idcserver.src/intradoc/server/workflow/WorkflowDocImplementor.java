/*      */ package intradoc.server.workflow;
/*      */ 
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.server.DataUtils;
/*      */ import intradoc.server.DocStateTransition;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.project.ProblemReportUtils;
/*      */ import intradoc.server.project.ProjectInfo;
/*      */ import intradoc.server.project.Projects;
/*      */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*      */ import intradoc.shared.CollaborationUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.workflow.WfStepData;
/*      */ import intradoc.shared.workflow.WorkflowData;
/*      */ import intradoc.shared.workflow.WorkflowInfo;
/*      */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*      */ import java.util.Date;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class WorkflowDocImplementor
/*      */ {
/*      */   public static final int F_LOAD_INFO_ONLY = 1;
/*      */   public static final int F_LOADWF_COMPLETE = 2;
/*      */   public static final int F_LOADWF_TEST_EXISTENCE_ONLY = 4;
/*      */   public static final int F_LOADWF_LOAD_STEPINFO = 8;
/*      */   public static final int F_DO_APPROVAL = 1;
/*      */   public static final int F_TEST_AND_LOAD_TO_DO_APPROVAL = 2;
/*   79 */   protected Service m_service = null;
/*   80 */   protected Workspace m_workspace = null;
/*   81 */   protected DataBinder m_binder = null;
/*      */ 
/*   83 */   protected boolean m_useSecurity = true;
/*      */ 
/*   85 */   protected boolean m_isCriteria = false;
/*      */ 
/*   87 */   protected boolean m_hasPriorRev = false;
/*      */ 
/*   89 */   protected boolean m_priorRevIsEmpty = false;
/*      */ 
/*   92 */   protected boolean m_deleteAllDocs = true;
/*      */ 
/*   94 */   protected WfStepData m_stepData = null;
/*      */ 
/*   96 */   protected DataResultSet m_workflowRevs = null;
/*      */ 
/*   98 */   protected String m_loadedWorkflowDocName = null;
/*      */ 
/*  100 */   protected String m_loadedStepInfoId = null;
/*      */ 
/*      */   public void init(Service service)
/*      */   {
/*  109 */     this.m_service = service;
/*  110 */     this.m_workspace = this.m_service.getWorkspace();
/*  111 */     this.m_binder = this.m_service.getBinder();
/*  112 */     this.m_useSecurity = this.m_service.getUseSecurity();
/*      */   }
/*      */ 
/*      */   public void resetDocumentState()
/*      */   {
/*  117 */     this.m_workflowRevs = null;
/*  118 */     this.m_deleteAllDocs = false;
/*  119 */     this.m_priorRevIsEmpty = false;
/*  120 */     this.m_hasPriorRev = false;
/*  121 */     this.m_loadedWorkflowDocName = null;
/*      */   }
/*      */ 
/*      */   public void loadWorkflowStateInfo(String rsetName)
/*      */   {
/*  136 */     ResultSet rset = this.m_binder.getResultSet(rsetName);
/*      */ 
/*  138 */     String curWorkflowState = ResultSetUtils.getValue(rset, "dWorkflowState");
/*  139 */     this.m_binder.putLocal("dWorkflowState", curWorkflowState);
/*  140 */     String curPublishState = ResultSetUtils.getValue(rset, "dPublishState");
/*  141 */     this.m_binder.putLocal("dPublishState", curPublishState);
/*  142 */     String curReleaseState = ResultSetUtils.getValue(rset, "dReleaseState");
/*      */ 
/*  145 */     this.m_binder.putLocal("dReleaseState", curReleaseState);
/*      */ 
/*  147 */     String curRevTitle = ResultSetUtils.getValue(rset, "dDocTitle");
/*  148 */     if (curRevTitle.length() != 0)
/*      */       return;
/*  150 */     this.m_binder.putLocal("isCurRevEmpty", "1");
/*      */   }
/*      */ 
/*      */   public boolean checkWorkflow(String resultName, String action)
/*      */     throws DataException, ServiceException
/*      */   {
/*  167 */     this.m_hasPriorRev = true;
/*  168 */     this.m_priorRevIsEmpty = DataBinderUtils.getLocalBoolean(this.m_binder, "isCurRevEmpty", false);
/*      */ 
/*  170 */     DataResultSet drset = getAndLoadWorkflowInfoWithFlags(resultName, 2);
/*  171 */     if ((drset == null) || (drset.isEmpty()) || (this.m_loadedStepInfoId == null))
/*      */     {
/*  174 */       return false;
/*      */     }
/*      */ 
/*  177 */     boolean isNotActive = action.equals("isNotActive");
/*  178 */     if (isNotActive)
/*      */     {
/*  181 */       this.m_service.createServiceException(null, "!csWorkflowItemActive");
/*      */     }
/*      */ 
/*  184 */     String wfType = this.m_binder.getAllowMissing("dWfType");
/*  185 */     this.m_isCriteria = ((wfType.equalsIgnoreCase("criteria")) || (wfType.equalsIgnoreCase("subworkflow")));
/*      */ 
/*  187 */     boolean isLoad = action.startsWith("load");
/*  188 */     boolean isUpdate = action.startsWith("isUpdate");
/*      */ 
/*  190 */     if ((!isLoad) && (!isUpdate))
/*      */     {
/*  192 */       boolean isDeleteRev = false;
/*  193 */       boolean isDeleteDoc = (action.equals("isNotActiveBasic")) || (action.equals("isDeleteDoc"));
/*  194 */       boolean isDeleteAllDocs = false;
/*  195 */       if (isDeleteDoc)
/*      */       {
/*  197 */         isDeleteAllDocs = true;
/*      */       }
/*      */       else
/*      */       {
/*  201 */         isDeleteRev = action.equals("isDeleteRev");
/*  202 */         if (isDeleteRev)
/*      */         {
/*  204 */           String curID = this.m_binder.get("dID");
/*  205 */           isDeleteAllDocs = checkAndLoadRevsForDeletionTest(curID, false);
/*      */         }
/*      */       }
/*  208 */       if ((!this.m_isCriteria) && (isDeleteAllDocs))
/*      */       {
/*  210 */         String msg = (isDeleteDoc) ? "!csBasicWorkflowCannotDeleteAllRevisions" : "!csBasicWorkflowCannotDeleteLastWorkflowRevision";
/*      */ 
/*  212 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*  214 */       this.m_deleteAllDocs = isDeleteAllDocs;
/*      */ 
/*  219 */       validateUserForStep(true, ":C:", "contributor", true);
/*      */ 
/*  224 */       String stepType = this.m_binder.get("wfStepCheckinType");
/*  225 */       if (!WorkflowScriptUtils.isAutoContributorStep(stepType))
/*      */       {
/*  227 */         validateUserForStep(false, ":R:", "reviewer", true);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  232 */       String stepType = this.m_binder.get("wfStepCheckinType");
/*  233 */       if (WorkflowScriptUtils.isReviewerStep(stepType))
/*      */       {
/*  240 */         boolean isFinished = false;
/*  241 */         if (!isUpdate)
/*      */         {
/*  243 */           isFinished = StringUtils.convertToBool(this.m_binder.getLocal("isFinished"), false);
/*      */         }
/*      */ 
/*  246 */         if ((isUpdate) || (isFinished))
/*      */         {
/*  248 */           validateUserForStep(true, ":R:", "reviewer", true);
/*      */         }
/*      */       }
/*  251 */       else if (isUpdate)
/*      */       {
/*  253 */         validateUserForStep(true, ":C:", "contributor", true);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  258 */     return true;
/*      */   }
/*      */ 
/*      */   public void checkPublishCriteriaWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/*  265 */     boolean isStaging = StringUtils.convertToBool(this.m_binder.getLocal("isStaging"), false);
/*  266 */     if (!isStaging)
/*      */       return;
/*  268 */     boolean isSourceChanged = StringUtils.convertToBool(this.m_binder.getLocal("IsSourceChanged"), false);
/*  269 */     if (!isSourceChanged) {
/*      */       return;
/*      */     }
/*  272 */     String publishState = this.m_binder.getLocal("dPublishState");
/*  273 */     if (publishState.equals("W"))
/*      */     {
/*  276 */       this.m_binder.putLocal("IsPublisherRenotify", "1");
/*      */     }
/*      */     else
/*      */     {
/*  281 */       checkCriteriaWorkflowEx(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkCriteriaWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/*  289 */     checkCriteriaWorkflowEx(false);
/*      */   }
/*      */ 
/*      */   public void checkCriteriaWorkflowEx(boolean isPublishCheck) throws DataException, ServiceException
/*      */   {
/*  294 */     if (SystemUtils.m_verbose)
/*      */     {
/*  296 */       Report.debug("workflow", "Entering checkCriteriaWorkflowEx on document with dDocName=" + this.m_binder.getAllowMissing("dDocName"), null);
/*      */     }
/*      */ 
/*  300 */     int returnCode = PluginFilters.filter("preWorkflowDocImplementorCheckCriteriaWorkflow", this.m_workspace, this.m_binder, this.m_service);
/*  301 */     if (returnCode == -1)
/*      */     {
/*  303 */       return;
/*      */     }
/*      */ 
/*  308 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/*  309 */     boolean isStaging = StringUtils.convertToBool(this.m_binder.getLocal("isStaging"), false);
/*  310 */     if ((isPublish) && (!isStaging))
/*      */     {
/*  312 */       return;
/*      */     }
/*      */ 
/*  315 */     String workflowState = "";
/*  316 */     if (this.m_hasPriorRev)
/*      */     {
/*  318 */       workflowState = this.m_binder.getAllowMissing("dWorkflowState");
/*  319 */       if (workflowState == null)
/*      */       {
/*  321 */         workflowState = "";
/*      */       }
/*      */     }
/*  324 */     if (workflowState.length() > 0)
/*      */     {
/*  326 */       if (SystemUtils.m_verbose)
/*      */       {
/*  328 */         Report.debug("workflow", "Skipping criteria workflow test because document already has dWorkflowState=" + workflowState, null);
/*      */       }
/*      */ 
/*  331 */       return;
/*      */     }
/*      */ 
/*  335 */     ResultSet drset = getAndLoadWorkflowInfoWithFlags("WF_INFO", 4);
/*  336 */     if ((drset != null) && (!drset.isEmpty()))
/*      */     {
/*  339 */       if (SystemUtils.m_verbose)
/*      */       {
/*  341 */         Report.debug("workflow", "Skipping criteria workflow test, document is already in an inactive (basic) workflow" + workflowState, null);
/*      */       }
/*      */ 
/*  344 */       return;
/*      */     }
/*      */ 
/*  347 */     WorkflowData wf = (WorkflowData)SharedObjects.getTable(WorkflowData.m_tableName);
/*  348 */     if (wf == null)
/*      */     {
/*  351 */       String errMsg = LocaleUtils.encodeMessage("csWfMissingWorkflowsTable", null, WorkflowData.m_tableName);
/*  352 */       throw new DataException(errMsg);
/*      */     }
/*      */ 
/*  355 */     String relState = "N";
/*  356 */     String pubState = this.m_binder.getAllowMissing("dPublishState");
/*  357 */     String group = this.m_binder.get("dSecurityGroup");
/*  358 */     String extraCriteriaKey = null;
/*      */ 
/*  360 */     if (isStaging)
/*      */     {
/*  362 */       extraCriteriaKey = this.m_binder.get("dProjectID");
/*      */     }
/*  364 */     boolean isCollaboration = StringUtils.convertToBool(this.m_binder.getLocal("isCollaboration"), false);
/*  365 */     if (isCollaboration)
/*      */     {
/*  367 */       extraCriteriaKey = "prj";
/*      */     }
/*      */ 
/*  371 */     boolean isInWorkflow = false;
/*  372 */     if (SystemUtils.m_verbose)
/*      */     {
/*  374 */       Report.debug("workflow", "Testing for criteria workflow (dSecurityGroup=" + group + ", extraCriteriaKey=" + extraCriteriaKey + ", isStaging=" + isStaging + ", isCollaboration=" + isCollaboration + ")", null);
/*      */     }
/*      */ 
/*  378 */     Vector criteria = wf.getCriteriaForSecurityGroup(group, extraCriteriaKey);
/*  379 */     boolean didTest = false;
/*  380 */     if (criteria != null)
/*      */     {
/*  382 */       int size = criteria.size();
/*  383 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  385 */         WorkflowInfo info = (WorkflowInfo)criteria.elementAt(i);
/*  386 */         String value = this.m_binder.getLocal(info.m_name);
/*  387 */         if ((value != null) && (info.m_value != null) && (info.m_wfStatus.equals("INPROCESS")))
/*      */         {
/*  389 */           didTest = true;
/*  390 */           if (SystemUtils.m_verbose)
/*      */           {
/*  392 */             Report.debug("workflow", "Testing value " + value + " retrieved using lookup key " + info.m_name + " for workflow " + info.m_wfName + " against criteria value " + info.m_value, null);
/*      */           }
/*      */ 
/*  395 */           value = value.trim().toLowerCase();
/*  396 */           isInWorkflow = StringUtils.matchEx(value, info.m_value, true, false);
/*  397 */           if (!isInWorkflow) {
/*      */             continue;
/*      */           }
/*  400 */           this.m_binder.putLocal("dWfID", info.m_wfID);
/*  401 */           this.m_binder.putLocal("wfAction", "CHECKIN");
/*  402 */           workflowState = "E";
/*  403 */           doCriteriaWorkflow();
/*  404 */           if (isStaging)
/*      */           {
/*  406 */             pubState = "W"; break;
/*      */           }
/*      */ 
/*  410 */           relState = "E";
/*      */ 
/*  412 */           break;
/*      */         }
/*      */ 
/*  417 */         if (!SystemUtils.m_verbose)
/*      */           continue;
/*  419 */         Report.debug("workflow", "Skipped entry test on workflow " + info.m_wfName + " with status " + info.m_wfStatus + " and lookup key " + info.m_name, null);
/*      */ 
/*  421 */         if (value != null)
/*      */           continue;
/*  423 */         Report.debug("workflow", "Skipped because value for lookup field was null", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  439 */     if ((!didTest) && 
/*  441 */       (SystemUtils.m_verbose))
/*      */     {
/*  443 */       Report.debug("workflow", "No workflow criteria were tested for this document", null);
/*      */     }
/*      */ 
/*  447 */     if (pubState == null)
/*      */     {
/*  449 */       pubState = "";
/*      */     }
/*      */ 
/*  452 */     this.m_binder.putLocal("dReleaseState", relState);
/*  453 */     this.m_binder.putLocal("dPublishState", pubState);
/*  454 */     this.m_binder.putLocal("dWorkflowState", workflowState);
/*  455 */     if (!isPublishCheck) {
/*      */       return;
/*      */     }
/*  458 */     this.m_workspace.execute("UrevisionPublishState", this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void doCriteriaWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/*  468 */     this.m_binder.putLocal("IsWorkflow", "1");
/*      */ 
/*  471 */     ResultSet steps = this.m_workspace.createResultSet("QworkflowSteps", this.m_binder);
/*  472 */     if ((steps == null) || (steps.isEmpty()))
/*      */     {
/*  474 */       this.m_service.createServiceException(null, "!csWfBadCriteria");
/*      */     }
/*  476 */     this.m_binder.mergeResultSetRowIntoLocalData(steps);
/*      */ 
/*  479 */     this.m_loadedWorkflowDocName = null;
/*      */ 
/*  482 */     this.m_loadedStepInfoId = this.m_binder.getLocal("dWfStepID");
/*  483 */     this.m_binder.putLocal("dWfCurrentStepID", this.m_loadedStepInfoId);
/*      */ 
/*  486 */     Date dte = new Date();
/*  487 */     long t = dte.getTime();
/*  488 */     DataUtils.computeActionDates(this.m_binder, t);
/*  489 */     ResultSet wflow = this.m_workspace.createResultSet("QworkflowForID", this.m_binder);
/*  490 */     String wfName = ResultSetUtils.getValue(wflow, "dWfName");
/*  491 */     this.m_binder.putLocal("dWfName", wfName);
/*  492 */     this.m_binder.putLocal("dAction", "Checkin");
/*      */ 
/*  494 */     if (this.m_binder.getLocal("dUser") == null)
/*      */     {
/*  496 */       this.m_binder.putLocal("dUser", "UNKNOWN");
/*      */     }
/*  498 */     if (this.m_binder.getLocal("dDocAccount") == null)
/*      */     {
/*  500 */       this.m_binder.putLocal("dDocAccount", "");
/*      */     }
/*      */ 
/*  504 */     CollaborationUtils.setCollaborationName(this.m_binder);
/*      */     try
/*      */     {
/*  508 */       this.m_binder.putLocal("dRevClassID", this.m_binder.get("dRevClassID"));
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  512 */       if (SystemUtils.m_verbose)
/*      */       {
/*  514 */         Report.debug("workflow", null, e);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  519 */     String subDir = WfCompanionManager.createSubDirectory(this.m_binder);
/*  520 */     this.m_binder.putLocal("dWfDirectory", subDir);
/*      */ 
/*  522 */     this.m_binder.putLocal("dWfEntryTs", LocaleUtils.formatODBC(dte));
/*      */ 
/*  525 */     this.m_workspace.execute("IworkflowDocHistory", this.m_binder);
/*  526 */     this.m_workspace.execute("IworkflowDocumentActive", this.m_binder);
/*  527 */     this.m_workspace.execute("IworkflowUserAttribute", this.m_binder);
/*  528 */     this.m_workspace.execute("IworkflowState", this.m_binder);
/*      */   }
/*      */ 
/*      */   public void doWorkflowAction(String action) throws ServiceException, DataException
/*      */   {
/*  533 */     boolean isDeleteDoc = action.equals("deleteCriteriaDoc");
/*  534 */     if ((!isDeleteDoc) && (!action.equals("deleteCriteriaRev")))
/*      */       return;
/*  536 */     String pubState = this.m_binder.getAllowMissing("dPublishState");
/*  537 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/*  538 */     if (!isPublish)
/*      */     {
/*  540 */       isPublish = (pubState != null) && (pubState.length() > 0);
/*      */     }
/*      */ 
/*  543 */     String workflowState = this.m_binder.getAllowMissing("dWorkflowState");
/*  544 */     if ((workflowState.length() == 0) && (!isPublish))
/*      */     {
/*  546 */       return;
/*      */     }
/*      */ 
/*  550 */     boolean isInWf = false;
/*  551 */     if (!this.m_isCriteria)
/*      */     {
/*  554 */       DataResultSet drset = getAndLoadWorkflowInfoWithFlags("WF_INFO", 2);
/*  555 */       isInWf = (drset != null) && (drset.isRowPresent()) && (this.m_loadedStepInfoId != null);
/*      */     }
/*      */ 
/*  560 */     if ((isPublish) && (isInWf))
/*      */     {
/*  563 */       this.m_isCriteria = true;
/*      */     }
/*      */ 
/*  566 */     if ((!this.m_isCriteria) && (!isInWf)) {
/*      */       return;
/*      */     }
/*  569 */     deleteRevisionInformation(true, true, isDeleteDoc, false, null, null);
/*      */   }
/*      */ 
/*      */   public void validateUserForStep(boolean isValidateType, String type, String errorMessageSuffix, boolean isAllowAdmin)
/*      */     throws DataException, ServiceException
/*      */   {
/*  578 */     if (this.m_loadedStepInfoId == null)
/*      */     {
/*  581 */       throw new DataException("!csWorkflowStepInfoNotLoaded");
/*      */     }
/*      */ 
/*  586 */     if ((type != null) && (type.length() > 0) && (type.charAt(0) != ':'))
/*      */     {
/*  588 */       type = ":" + type + ":";
/*      */     }
/*  590 */     boolean isContributorTypeRequested = WorkflowScriptUtils.isContributorStep(type);
/*      */ 
/*  592 */     if (isContributorTypeRequested)
/*      */     {
/*  597 */       String publishState = this.m_binder.getAllowMissing("dPublishState");
/*  598 */       if ((publishState != null) && (publishState.length() > 0))
/*      */       {
/*  600 */         return;
/*      */       }
/*      */ 
/*  604 */       isAllowAdmin = true;
/*      */     }
/*      */ 
/*  608 */     boolean isReviewTypeRequested = WorkflowScriptUtils.isReviewerStep(type);
/*  609 */     if (isValidateType)
/*      */     {
/*  611 */       String stepType = this.m_binder.get("wfStepCheckinType");
/*  612 */       if (WorkflowScriptUtils.isAutoContributorStep(stepType))
/*      */       {
/*  614 */         isAllowAdmin = true;
/*      */       }
/*      */ 
/*  618 */       String[] typeList = WorkflowScriptUtils.getFlags(type);
/*  619 */       boolean isSet = false;
/*  620 */       for (int i = 0; (!isSet) && (i < typeList.length); ++i)
/*      */       {
/*  622 */         isSet = WorkflowScriptUtils.isFlagSet(typeList[i], stepType);
/*      */       }
/*  624 */       if (!isSet)
/*      */       {
/*  628 */         String typeDes = WorkflowScriptUtils.formatStepTypeDescription(stepType);
/*  629 */         String errMsg = LocaleUtils.encodeMessage("csWfDocNotInTypeStep_" + errorMessageSuffix, null, typeDes);
/*      */ 
/*  631 */         this.m_service.createServiceException(null, errMsg);
/*      */       }
/*      */     }
/*      */ 
/*  635 */     if (!this.m_useSecurity)
/*      */     {
/*  637 */       return;
/*      */     }
/*      */ 
/*  640 */     UserData userData = this.m_service.getUserData();
/*  641 */     String user = userData.m_name;
/*  642 */     if (isReviewTypeRequested)
/*      */     {
/*  644 */       if (this.m_binder.getLocal("dUser") == null)
/*      */       {
/*  646 */         Report.trace("workflow", "The parameter dUser was not provided, supplying it with acting user " + user, null);
/*  647 */         this.m_binder.putLocal("dUser", user);
/*      */       }
/*      */ 
/*  650 */       ResultSet stateSet = this.m_workspace.createResultSet("QwfStateForUser", this.m_binder);
/*  651 */       if (!stateSet.isEmpty())
/*      */       {
/*  653 */         this.m_service.createServiceException(null, "!csWfUserHasActed");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  658 */     if (WorkflowUtils.isUserInStep(this.m_workspace, this.m_binder, user, this.m_service))
/*      */     {
/*  660 */       return;
/*      */     }
/*      */ 
/*  663 */     if ((isAllowAdmin) && 
/*  665 */       (this.m_service.checkAccess(this.m_binder, 8)))
/*      */     {
/*  667 */       return;
/*      */     }
/*      */ 
/*  671 */     String msgId = (isReviewTypeRequested) ? "csWfNotReviewerForStep" : "csWfNotContributorForStep";
/*  672 */     String errMsg = LocaleUtils.encodeMessage(msgId, null, this.m_binder.getLocal("dWfStepName"));
/*  673 */     this.m_service.createServiceException(null, errMsg);
/*      */   }
/*      */ 
/*      */   public void deleteRevisionInformation(boolean isDbCleanup, boolean isCompCleanup, boolean allWorkflowDocs, boolean deleteWorkflowEntry, Hashtable topicUserMap, Hashtable topicWorkMap)
/*      */     throws DataException, ServiceException
/*      */   {
/*  680 */     boolean allWorkflowRevsDeleted = (allWorkflowDocs) || (deleteWorkflowEntry);
/*  681 */     String curID = this.m_binder.get("dID");
/*  682 */     allWorkflowRevsDeleted = checkAndLoadRevsForDeletionTest(curID, allWorkflowRevsDeleted);
/*  683 */     this.m_deleteAllDocs = allWorkflowRevsDeleted;
/*      */ 
/*  685 */     if ((isDbCleanup) && 
/*  687 */       (allWorkflowRevsDeleted))
/*      */     {
/*  689 */       this.m_binder.putLocal("dWfDocState", "INIT");
/*  690 */       if ((this.m_isCriteria) || (deleteWorkflowEntry))
/*      */       {
/*  692 */         this.m_workspace.execute("DworkflowDocument", this.m_binder);
/*  693 */         this.m_workspace.execute("DworkflowUserAttribute", this.m_binder);
/*      */       }
/*      */       else
/*      */       {
/*  697 */         this.m_workspace.execute("UworkflowDocState", this.m_binder);
/*      */       }
/*      */ 
/*  712 */       this.m_workspace.execute("DworkflowDocState", this.m_binder);
/*      */     }
/*      */ 
/*  717 */     if (!isCompCleanup)
/*      */       return;
/*  719 */     if (allWorkflowRevsDeleted)
/*      */     {
/*  721 */       boolean isUpdateTopicNow = topicUserMap == null;
/*  722 */       if (isUpdateTopicNow)
/*      */       {
/*  724 */         topicUserMap = new Hashtable();
/*  725 */         topicWorkMap = new Hashtable();
/*      */       }
/*      */ 
/*  729 */       String docName = this.m_binder.get("dDocName");
/*  730 */       String subDir = this.m_binder.get("dWfDirectory");
/*      */ 
/*  733 */       WfCompanionData wfCompanionData = WfCompanionManager.getCompanionData(docName, subDir);
/*      */ 
/*  735 */       if (wfCompanionData != null)
/*      */       {
/*  737 */         WorkflowUtils.prepareRemoveFromInQueue(docName, wfCompanionData, this.m_workspace, this.m_service, topicUserMap, topicWorkMap);
/*      */ 
/*  740 */         WfCompanionManager.deleteCompanionFile(docName, subDir, this.m_binder, true);
/*      */       }
/*      */ 
/*  743 */       if (isUpdateTopicNow)
/*      */       {
/*  746 */         WorkflowUtils.updateWorkflowTopic(topicUserMap, topicWorkMap, this.m_workspace, this.m_service);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  752 */       for (this.m_workflowRevs.first(); this.m_workflowRevs.isRowPresent(); this.m_workflowRevs.next())
/*      */       {
/*  754 */         String workflowRevID = ResultSetUtils.getValue(this.m_workflowRevs, "dID");
/*  755 */         if (!curID.equals(workflowRevID))
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  765 */       Properties oldProps = this.m_binder.getLocalData();
/*  766 */       DataBinder tempBinder = this.m_binder.createShallowCopyCloneResultSets();
/*  767 */       this.m_binder.setLocalData(new Properties(oldProps));
/*      */       try
/*      */       {
/*  771 */         String priorID = ResultSetUtils.getValue(this.m_workflowRevs, "dID");
/*  772 */         this.m_binder.putLocal("dID", priorID);
/*  773 */         this.m_binder.putLocal("wfAction", "DELETE_REV");
/*  774 */         ResultSet priorRowRset = this.m_workspace.createResultSet("QdocInfo", this.m_binder);
/*  775 */         this.m_binder.mergeResultSetRowIntoLocalData(priorRowRset);
/*  776 */         this.m_binder.putLocal("isFinished", "0");
/*  777 */         DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, false, false, false, true, this.m_service);
/*      */ 
/*  779 */         DocStateTransition.updateStateAfterDocumentAdvance(this.m_binder, this.m_workspace);
/*      */       }
/*      */       finally
/*      */       {
/*  783 */         this.m_binder.setLocalData(oldProps);
/*  784 */         this.m_binder.copyResultSetStateShallow(tempBinder);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkAndLoadRevsForDeletionTest(String curID, boolean allWorkflowRevsDeleted)
/*      */     throws DataException
/*      */   {
/*  795 */     if (this.m_workflowRevs == null)
/*      */     {
/*  797 */       ResultSet rset = this.m_workspace.createResultSet("QwfRevisionsInWorkflow", this.m_binder);
/*  798 */       this.m_workflowRevs = new DataResultSet();
/*  799 */       this.m_workflowRevs.copy(rset);
/*      */     }
/*  801 */     if (this.m_workflowRevs.getNumRows() == 0)
/*      */     {
/*  803 */       allWorkflowRevsDeleted = true;
/*      */     }
/*      */ 
/*  807 */     if ((!allWorkflowRevsDeleted) && (this.m_workflowRevs.getNumRows() == 1))
/*      */     {
/*  809 */       String workflowRevID = ResultSetUtils.getValue(this.m_workflowRevs, "dID");
/*  810 */       if (curID.equals(workflowRevID))
/*      */       {
/*  812 */         allWorkflowRevsDeleted = true;
/*      */       }
/*      */     }
/*      */ 
/*  816 */     return allWorkflowRevsDeleted;
/*      */   }
/*      */ 
/*      */   public void validateForWfStandard()
/*      */     throws DataException, ServiceException
/*      */   {
/*  822 */     String oldSecurityGroup = this.m_binder.getFromSets("dSecurityGroup");
/*  823 */     String newSecurityGroup = this.m_binder.getLocal("dSecurityGroup");
/*  824 */     if (oldSecurityGroup.equalsIgnoreCase(newSecurityGroup))
/*      */       return;
/*  826 */     this.m_service.createServiceException(null, "!csWfInvalidSecurityGroup");
/*      */   }
/*      */ 
/*      */   public boolean isReadyForRefinery()
/*      */   {
/*  832 */     String param = this.m_binder.getLocal("isFinished");
/*  833 */     return StringUtils.convertToBool(param, false);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public DataResultSet getAndLoadWorkflowInfo(String resultName, boolean testExistenceOnly)
/*      */     throws DataException, ServiceException
/*      */   {
/*  843 */     SystemUtils.reportDeprecatedUsage("getAndLoadWorkflowInfo(String, boolean) called.");
/*  844 */     int flags = (testExistenceOnly) ? 4 : 2;
/*  845 */     return getAndLoadWorkflowInfoWithFlags(resultName, flags);
/*      */   }
/*      */ 
/*      */   public DataResultSet getAndLoadWorkflowInfoWithFlags(String resultName, int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/*  851 */     String pubState = this.m_binder.getAllowMissing("dPublishState");
/*  852 */     String wfState = this.m_binder.getAllowMissing("dWorkflowState");
/*  853 */     boolean isNotDocWfState = (wfState != null) && (wfState.equals(""));
/*  854 */     if (isNotDocWfState)
/*      */     {
/*  857 */       String status = this.m_binder.getAllowMissing("dStatus");
/*  858 */       if ((status != null) && (((status.equals("EDIT")) || (status.equals("REVIEW")))))
/*      */       {
/*  860 */         isNotDocWfState = false;
/*      */       }
/*      */     }
/*      */ 
/*  864 */     if ((isNotDocWfState) && (((pubState == null) || (!pubState.equals("W")))))
/*      */     {
/*  866 */       return null;
/*      */     }
/*      */ 
/*  869 */     String docName = this.m_binder.getAllowMissing("dDocName");
/*  870 */     if (docName == null)
/*      */     {
/*  872 */       throw new DataException("!csWorkflowDocNameNotProvided");
/*      */     }
/*      */ 
/*  875 */     ResultSet rset = this.m_binder.getResultSet(resultName);
/*      */ 
/*  880 */     if ((rset != null) && (rset.isRowPresent()))
/*      */     {
/*  882 */       rset.first();
/*      */ 
/*  890 */       int docNameIndex = ResultSetUtils.getIndexMustExist(rset, "dDocName");
/*  891 */       String wfDocName = rset.getStringValue(docNameIndex);
/*  892 */       if (!wfDocName.equals(docName))
/*      */       {
/*  894 */         rset = null;
/*      */       }
/*      */ 
/*      */     }
/*  899 */     else if ((this.m_loadedWorkflowDocName == null) || (!this.m_loadedWorkflowDocName.equals(docName)))
/*      */     {
/*  901 */       rset = null;
/*      */     }
/*      */ 
/*  904 */     if (rset == null)
/*      */     {
/*  906 */       rset = this.m_workspace.createResultSet("QworkflowDocInfo", this.m_binder);
/*  907 */       if (rset == null)
/*      */       {
/*  909 */         throw new DataException("!csWfBadInfoQuery");
/*      */       }
/*      */     }
/*      */ 
/*  913 */     DataResultSet drset = null;
/*  914 */     if (!rset instanceof DataResultSet)
/*      */     {
/*  916 */       drset = new DataResultSet();
/*  917 */       drset.copy(rset);
/*      */     }
/*      */     else
/*      */     {
/*  921 */       drset = (DataResultSet)rset;
/*      */     }
/*      */ 
/*  924 */     if ((0x4 & flags) != 0)
/*      */     {
/*  926 */       return drset;
/*      */     }
/*      */ 
/*  929 */     boolean infoOnly = (flags & 0x1) != 0;
/*      */ 
/*  931 */     ResultSet docInfo = null;
/*  932 */     if ((infoOnly) && (this.m_binder.getActiveSet("DOC_INFO") == null))
/*      */     {
/*  937 */       docInfo = this.m_binder.getResultSet("DOC_INFO");
/*  938 */       if (docInfo != null)
/*      */       {
/*  940 */         this.m_binder.pushActiveResultSet("DOC_INFO", docInfo);
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  946 */       if ((!drset.isEmpty()) && (!infoOnly))
/*      */       {
/*  948 */         checkWorkflowBackwardCompatibility(drset, docName);
/*      */       }
/*      */ 
/*  954 */       this.m_binder.addResultSet(resultName, drset);
/*      */ 
/*  962 */       boolean isActive = false;
/*      */       String[] criticalFields;
/*  963 */       if (drset.isRowPresent())
/*      */       {
/*  965 */         criticalFields = new String[] { "dWfName", "dWfType", "dWfStatus", "dWfDocState", "dWfID", "dWfCurrentStepID" };
/*  966 */         for (int i = 0; i < criticalFields.length; ++i)
/*      */         {
/*  968 */           String val = ResultSetUtils.getValue(drset, criticalFields[i]);
/*  969 */           this.m_binder.putLocal(criticalFields[i], val);
/*      */         }
/*      */ 
/*  972 */         String wfDocState = this.m_binder.getLocal("dWfDocState");
/*  973 */         if (!wfDocState.equals("INIT"))
/*      */         {
/*  975 */           isActive = true;
/*      */         }
/*      */       }
/*  978 */       if (isActive)
/*      */       {
/*  980 */         loadStepTypeInfo();
/*  981 */         this.m_binder.putLocal("IsWorkflow", "1");
/*      */ 
/*  983 */         if (isNotDocWfState)
/*      */         {
/*  987 */           this.m_binder.putLocal("dWorkflowState", "E");
/*  988 */           wfState = "E";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  993 */         this.m_loadedStepInfoId = null;
/*      */       }
/*  995 */       if ((flags & 0x8) != 0)
/*      */       {
/*  997 */         criticalFields = drset;
/*      */         return criticalFields;
/*      */       }
/* 1000 */       Object[] params = new Object[0];
/* 1001 */       this.m_service.setCachedObject("getAndLoadWorkflowInfoWithFlags:parameters", params);
/* 1002 */       PluginFilters.filter("getAndLoadWorkflowInfoWithFlags", this.m_workspace, this.m_binder, this.m_service);
/*      */ 
/* 1004 */       boolean isWorkflowInfo = StringUtils.convertToBool(this.m_binder.getLocal("IsWorkflowInfo"), false);
/* 1005 */       if (isWorkflowInfo)
/*      */       {
/* 1009 */         if (!isActive)
/*      */         {
/* 1011 */           this.m_service.createServiceException(null, "!csWfNotInWorkflow");
/*      */         }
/*      */ 
/* 1014 */         ResultSet rStates = this.m_workspace.createResultSet("QwfDocStateAll", this.m_binder);
/* 1015 */         DataResultSet drStates = new DataResultSet();
/* 1016 */         drStates.copy(rStates);
/* 1017 */         this.m_binder.addResultSet("WorkflowStates", drStates);
/*      */ 
/* 1020 */         WfStepData stepData = WorkflowUtils.loadCurrentStepInfo(this.m_binder, this.m_workspace, this.m_service);
/* 1021 */         WorkflowUtils.computeDocStepInfo(stepData, this.m_binder, this.m_service, this.m_workspace);
/*      */       }
/* 1023 */       this.m_loadedWorkflowDocName = docName;
/*      */     }
/*      */     finally
/*      */     {
/* 1027 */       if (docInfo != null)
/*      */       {
/* 1029 */         this.m_binder.popActiveResultSet();
/*      */       }
/*      */     }
/* 1032 */     return drset;
/*      */   }
/*      */ 
/*      */   public void checkWorkflowBackwardCompatibility(DataResultSet drset, String docName)
/*      */   {
/* 1039 */     String workflowState = this.m_binder.getAllowMissing("dWorkflowState");
/* 1040 */     if ((workflowState != null) && (workflowState.length() != 0))
/*      */       return;
/*      */     try
/*      */     {
/* 1044 */       String status = this.m_binder.getAllowMissing("dStatus");
/* 1045 */       String id = this.m_binder.getAllowMissing("dID");
/* 1046 */       String rptId = id;
/* 1047 */       if ((rptId == null) || (rptId.length() == 0))
/*      */       {
/* 1049 */         rptId = "<unknown id>";
/*      */       }
/*      */ 
/* 1052 */       Report.trace("workflow", "Workflow state has inappropriate empty value for document " + docName + " and dID = " + rptId, null);
/*      */ 
/* 1054 */       if ((id != null) && (id.length() > 0) && (status != null) && (((status.equals("REVIEW")) || (status.equals("EDIT")))))
/*      */       {
/* 1057 */         String newWorkflowState = null;
/* 1058 */         if (status.equals("EDIT"))
/*      */         {
/* 1060 */           newWorkflowState = "E";
/*      */         }
/*      */         else
/*      */         {
/* 1064 */           newWorkflowState = "R";
/*      */ 
/* 1069 */           String wfStepID = ResultSetUtils.getValue(drset, "dWfCurrentStepID");
/* 1070 */           this.m_binder.putLocal("dWfCurrentStepID", wfStepID);
/* 1071 */           String wfID = ResultSetUtils.getValue(drset, "dWfID");
/* 1072 */           this.m_binder.putLocal("dWfID", wfID);
/* 1073 */           ResultSet rsetStep = this.m_workspace.createResultSet("QwfCurrentStep", this.m_binder);
/* 1074 */           if (rsetStep.isRowPresent())
/*      */           {
/* 1076 */             String stepType = ResultSetUtils.getValue(rsetStep, "dWfStepType");
/* 1077 */             if (stepType.indexOf("C") >= 0)
/*      */             {
/* 1079 */               newWorkflowState = "W";
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1084 */         this.m_binder.putLocal("dWorkflowState", newWorkflowState);
/* 1085 */         Report.trace("workflow", "Updating state to " + newWorkflowState, null);
/* 1086 */         this.m_workspace.execute("UrevisionWorkflowState", this.m_binder);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1091 */       Report.trace("workflow", null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadStepTypeInfo()
/*      */     throws DataException
/*      */   {
/* 1098 */     String dWfCurrentStepID = this.m_binder.getLocal("dWfCurrentStepID");
/* 1099 */     if ((this.m_loadedStepInfoId != null) && (dWfCurrentStepID.equals(dWfCurrentStepID))) {
/*      */       return;
/*      */     }
/* 1102 */     ResultSet stepRset = this.m_workspace.createResultSet("QwfCurrentStep", this.m_binder);
/* 1103 */     if ((stepRset == null) || (!stepRset.isRowPresent()))
/*      */     {
/* 1105 */       throw new DataException("!csWfMissingStepInformation");
/*      */     }
/* 1107 */     this.m_binder.mergeResultSetRowIntoLocalData(stepRset);
/* 1108 */     this.m_loadedStepInfoId = dWfCurrentStepID;
/*      */ 
/* 1111 */     String wfStepType = this.m_binder.getLocal("dWfStepType");
/* 1112 */     wfStepType = WorkflowScriptUtils.getUpgradedStepType(wfStepType);
/* 1113 */     this.m_binder.putLocal("wfStepCheckinType", wfStepType);
/*      */   }
/*      */ 
/*      */   public void computeWfDocumentsInfo(String setName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1119 */     String status = this.m_binder.get("dWfStatus");
/* 1120 */     if (status.equals("INIT"))
/*      */     {
/* 1122 */       return;
/*      */     }
/*      */ 
/* 1125 */     String wfType = this.m_binder.get("dWfType");
/* 1126 */     this.m_isCriteria = ((wfType.equalsIgnoreCase("criteria")) || (wfType.equalsIgnoreCase("subworkflow")));
/*      */ 
/* 1128 */     DataResultSet drset = null;
/* 1129 */     if (this.m_isCriteria)
/*      */     {
/* 1131 */       if (SecurityUtils.m_useCollaboration)
/*      */       {
/* 1135 */         String clbraName = this.m_binder.getAllowMissing("dClbraName");
/* 1136 */         if ((clbraName != null) && (clbraName.length() > 0))
/*      */         {
/* 1139 */           this.m_binder.putLocal("isCollaboration", "1");
/* 1140 */           this.m_binder.putLocal("clbraAccount", "prj/" + clbraName);
/*      */         }
/* 1142 */         this.m_binder.putLocal("dataSource", "WorkflowCriteriaRevs");
/* 1143 */         this.m_binder.putLocal("resultName", setName);
/* 1144 */         int maxWorkflowDocsList = SharedObjects.getEnvironmentInt("MaxWorkflowDocResults", 500);
/*      */ 
/* 1146 */         this.m_binder.putLocal("MaxQueryRows", "" + maxWorkflowDocsList);
/* 1147 */         this.m_service.createResultSetSQL();
/* 1148 */         drset = (DataResultSet)this.m_binder.getResultSet(setName);
/*      */       }
/*      */       else
/*      */       {
/* 1152 */         drset = WorkflowUtils.getWorkflowDocumentsEx(this.m_workspace, this.m_binder, "QworkflowDocuments", false);
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/* 1157 */       drset = WorkflowUtils.getWorkflowDocuments(this.m_workspace, this.m_binder);
/*      */     }
/*      */ 
/* 1160 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/* 1163 */       return;
/*      */     }
/*      */ 
/* 1166 */     this.m_service.setConditionVar("IsMaxRows", drset.isCopyAborted());
/*      */ 
/* 1169 */     drset.deDuplicate();
/*      */ 
/* 1171 */     this.m_binder.addResultSet(setName, drset);
/*      */ 
/* 1173 */     getWorkflowStepInfo(0, true, false);
/*      */   }
/*      */ 
/*      */   public WfStepData getWorkflowStepInfo(int type, boolean isStepOnly, boolean isValidateOnly)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1181 */     ResultSet rset = this.m_workspace.createResultSet("QworkflowSteps", this.m_binder);
/* 1182 */     if (rset == null)
/*      */     {
/* 1184 */       String errMsg = LocaleUtils.encodeMessage("csWfMissingStepsTable", null, this.m_binder.getAllowMissing("dWfName"));
/* 1185 */       throw new DataException(errMsg);
/*      */     }
/*      */ 
/* 1188 */     if (this.m_stepData == null)
/*      */     {
/* 1190 */       this.m_stepData = new WfStepData();
/*      */     }
/* 1192 */     this.m_binder.addResultSet("WorkflowSteps", this.m_stepData);
/*      */ 
/* 1194 */     this.m_stepData.loadStepDataType(rset, type);
/* 1195 */     WorkflowUtils.validateAndUpdateStepData(this.m_stepData, this.m_workspace);
/*      */ 
/* 1198 */     DataBinder binder = this.m_binder;
/* 1199 */     if (isStepOnly)
/*      */     {
/* 1201 */       binder = new DataBinder();
/* 1202 */       binder.addResultSet("WorkflowSteps", this.m_stepData);
/*      */     }
/*      */ 
/* 1205 */     for (; this.m_stepData.isRowPresent(); this.m_stepData.next())
/*      */     {
/* 1207 */       if (type == 0)
/*      */       {
/* 1209 */         Vector users = WorkflowUtils.computeStepUsersEx(this.m_workspace, binder, isValidateOnly, true, this.m_service, !isValidateOnly);
/*      */ 
/* 1212 */         boolean hasTokens = StringUtils.convertToBool(binder.getLocal("hasTokens"), false);
/* 1213 */         if (isValidateOnly)
/*      */         {
/* 1215 */           this.m_stepData.addUsers(users, hasTokens);
/*      */         }
/*      */         else
/*      */         {
/* 1219 */           this.m_stepData.addUserNames(users, hasTokens);
/*      */         }
/* 1221 */         binder.removeLocal("hasTokens");
/*      */       }
/*      */       else
/*      */       {
/* 1225 */         ResultSet aliasSet = this.m_workspace.createResultSet("QworkflowAliases", binder);
/* 1226 */         this.m_stepData.addAliasesString(aliasSet);
/*      */       }
/*      */     }
/* 1229 */     return this.m_stepData;
/*      */   }
/*      */ 
/*      */   public void getProjectInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1235 */     String publishState = this.m_binder.getAllowMissing("dPublishState");
/* 1236 */     if ((publishState == null) || (publishState.length() == 0))
/*      */     {
/* 1239 */       return;
/*      */     }
/*      */ 
/* 1242 */     ResultSet rset = this.m_workspace.createResultSet("QprojectDocument", this.m_binder);
/* 1243 */     if (rset.isEmpty())
/*      */     {
/* 1245 */       return;
/*      */     }
/*      */ 
/* 1248 */     DataResultSet drset = new DataResultSet();
/* 1249 */     drset.copy(rset);
/* 1250 */     this.m_binder.addResultSet("ProjectDocument", drset);
/*      */ 
/* 1253 */     String projectID = this.m_binder.get("dProjectID");
/* 1254 */     ProjectInfo pInfo = Projects.getProjectInfo(projectID);
/* 1255 */     if ((pInfo == null) || (!pInfo.m_hasWorkflow))
/*      */     {
/* 1257 */       return;
/*      */     }
/* 1259 */     if ((publishState.equals("S")) || (publishState.equals("W")))
/*      */     {
/* 1261 */       this.m_binder.putLocal("IsStagingDoc", "1");
/*      */     }
/*      */ 
/* 1264 */     ProblemReportUtils.loadStateLists(this.m_binder, this.m_workspace, this.m_service);
/*      */ 
/* 1267 */     String sourceInstance = this.m_binder.get("dSourceInstanceName");
/* 1268 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */ 
/* 1270 */     boolean isSourceLocal = sourceInstance.equals(idcName);
/* 1271 */     boolean isSourceDefined = isSourceLocal;
/* 1272 */     if ((!isSourceLocal) && (OutgoingProviderMonitor.isStarted()))
/*      */     {
/* 1274 */       this.m_binder.putLocal("IsSourceLocal", "0");
/* 1275 */       Provider provider = OutgoingProviderMonitor.getOutgoingProvider(sourceInstance);
/* 1276 */       if (provider != null)
/*      */       {
/* 1278 */         isSourceDefined = true;
/* 1279 */         provider.pollConnectionState();
/* 1280 */         Properties props = provider.getProviderState();
/* 1281 */         DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/*      */       }
/*      */     }
/*      */ 
/* 1285 */     this.m_binder.putLocal("IsSourceLocal", String.valueOf(isSourceLocal));
/* 1286 */     this.m_binder.putLocal("IsSourceDefined", String.valueOf(isSourceDefined));
/*      */   }
/*      */ 
/*      */   public void computeWorkflowCheckinType()
/*      */     throws ServiceException
/*      */   {
/* 1294 */     String stepType = this.m_binder.getLocal("wfStepCheckinType");
/* 1295 */     if (stepType == null)
/*      */     {
/* 1297 */       stepType = "NoStepType";
/*      */     }
/* 1299 */     boolean allowsNewRev = stepType.indexOf(":CN:") >= 0;
/* 1300 */     boolean allowsEditRev = stepType.indexOf(":CE:") >= 0;
/* 1301 */     if ((!allowsNewRev) && (!allowsEditRev) && (((stepType.indexOf(":C:") >= 0) || (stepType.indexOf(":CA:") >= 0))))
/*      */     {
/* 1303 */       allowsEditRev = true;
/*      */     }
/* 1305 */     if ((!allowsNewRev) && (!allowsEditRev))
/*      */     {
/* 1307 */       String stepTypeDes = WorkflowScriptUtils.formatLocalizedStepTypeDescription(stepType, this.m_service);
/* 1308 */       String msg = LocaleUtils.encodeMessage("csWorkflowStepTypeDoesNotAllowCheckin", null, stepTypeDes);
/* 1309 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1313 */     boolean wantsNewRev = DataBinderUtils.getBoolean(this.m_binder, "isNewRev", false);
/* 1314 */     boolean wantsEditRev = DataBinderUtils.getBoolean(this.m_binder, "isEditRev", false);
/* 1315 */     if ((wantsNewRev) && (!allowsNewRev))
/*      */     {
/* 1317 */       this.m_service.createServiceException(null, "!csWorkflowStepTypeDoesNotAllowNewRevisions");
/*      */     }
/* 1319 */     if ((wantsEditRev) && (!allowsEditRev))
/*      */     {
/* 1321 */       this.m_service.createServiceException(null, "!csWorkflowStepTypeDoesNotAllowEditRevisions");
/*      */     }
/*      */ 
/* 1325 */     if ((!wantsEditRev) && (allowsNewRev))
/*      */     {
/* 1327 */       allowsEditRev = false;
/*      */     }
/* 1329 */     String isUpdate = (!allowsNewRev) ? "1" : "";
/* 1330 */     this.m_binder.putLocal("IsUpdate", isUpdate);
/*      */   }
/*      */ 
/*      */   public String determineWorkflowCheckin(String docRsetName)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1341 */     DataResultSet docInfoRset = (DataResultSet)this.m_binder.getResultSet(docRsetName);
/* 1342 */     String workflowCheckinSubService = "WORKFLOW_EDIT_REV";
/* 1343 */     String revLabel = this.m_binder.getLocal("dRevLabel");
/* 1344 */     String prevRevLabel = ResultSetUtils.getValue(docInfoRset, "dRevLabel");
/* 1345 */     boolean isEmptyRev = this.m_priorRevIsEmpty;
/* 1346 */     boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false);
/* 1347 */     String validateType = (isUpdate) ? ":CE:" : ":CN:";
/*      */ 
/* 1349 */     boolean isEditRev = (isUpdate) || (isEmptyRev);
/* 1350 */     if (isEditRev)
/*      */     {
/* 1352 */       if ((revLabel == null) || (revLabel.length() == 0))
/*      */       {
/* 1354 */         this.m_binder.putLocal("dRevLabel", prevRevLabel);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1360 */       if ((revLabel == null) || (revLabel.length() == 0) || (revLabel.equals(prevRevLabel)))
/*      */       {
/* 1362 */         String nextRev = RevisionSpec.getNext(prevRevLabel);
/* 1363 */         if (nextRev == null)
/*      */         {
/* 1365 */           nextRev = RevisionSpec.getInvalidLabel();
/*      */         }
/* 1367 */         this.m_binder.putLocal("dRevLabel", nextRev);
/*      */       }
/* 1369 */       workflowCheckinSubService = "WORKFLOW_NEW_REV";
/*      */     }
/* 1371 */     validateUserForStep(true, validateType, "contributor", true);
/*      */ 
/* 1374 */     if ((isEditRev) && (!isUpdate))
/*      */     {
/* 1376 */       this.m_binder.putLocal("IsUpdate", "1");
/*      */     }
/* 1378 */     return workflowCheckinSubService;
/*      */   }
/*      */ 
/*      */   public void updateWorkflowStateAfterCheckin(int flags)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1391 */     String stQuery = null;
/* 1392 */     String workflowState = this.m_binder.get("dWorkflowState");
/* 1393 */     String isFinishedStr = this.m_binder.getLocal("isFinished");
/* 1394 */     if (isFinishedStr == null)
/*      */     {
/* 1396 */       this.m_binder.putLocal("isFinished", "0");
/*      */     }
/* 1398 */     String user = this.m_binder.getLocal("dUserName");
/* 1399 */     if (user == null)
/*      */     {
/* 1402 */       this.m_binder.putLocal("dUserName", this.m_binder.getLocal("dUser"));
/*      */     }
/* 1404 */     boolean testForApproval = (flags & 0x2) != 0;
/* 1405 */     if (testForApproval)
/*      */     {
/* 1407 */       getAndLoadWorkflowInfoWithFlags("WF_INFO", 1);
/*      */     }
/*      */ 
/* 1410 */     boolean isFinished = StringUtils.convertToBool(isFinishedStr, false);
/* 1411 */     if ((workflowState.length() > 0) && ("E".indexOf(workflowState) >= 0))
/*      */     {
/* 1419 */       String stepType = this.m_binder.getLocal("wfStepCheckinType");
/* 1420 */       if ((isFinished) || ((stepType != null) && (WorkflowScriptUtils.isAutoContributorStep(stepType))))
/*      */       {
/* 1422 */         stQuery = "IworkflowState";
/* 1423 */         ResultSet rset = this.m_workspace.createResultSet("QwfDocState", this.m_binder);
/* 1424 */         if (rset.isRowPresent())
/*      */         {
/* 1426 */           stQuery = "UworkflowState";
/*      */         }
/*      */       }
/*      */     }
/* 1430 */     else if ((workflowState.length() > 0) && ("RW".indexOf(workflowState) >= 0))
/*      */     {
/* 1434 */       boolean doTest = true;
/* 1435 */       if (testForApproval)
/*      */       {
/* 1437 */         ResultSet rset = this.m_workspace.createResultSet("QwfStateForUser", this.m_binder);
/* 1438 */         if (rset.isRowPresent())
/*      */         {
/* 1440 */           doTest = false;
/*      */         }
/*      */       }
/*      */ 
/* 1444 */       if ((doTest) && (isFinished))
/*      */       {
/* 1446 */         stQuery = "IworkflowState";
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1451 */     Date dte = new Date();
/* 1452 */     this.m_binder.putLocal("dWfEntryTs", LocaleUtils.formatODBC(dte));
/*      */ 
/* 1454 */     if (stQuery == null)
/*      */       return;
/* 1456 */     this.m_workspace.execute(stQuery, this.m_binder);
/* 1457 */     this.m_service.setConditionVar("DidWorkflowApproval", true);
/*      */   }
/*      */ 
/*      */   public DataResultSet getAlreadyComputedWorkflowRevs()
/*      */   {
/* 1463 */     return this.m_workflowRevs;
/*      */   }
/*      */ 
/*      */   public boolean isDeleteAllWorkflowDocs()
/*      */   {
/* 1468 */     return this.m_deleteAllDocs;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1474 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103203 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WorkflowDocImplementor
 * JD-Core Version:    0.5.4
 */