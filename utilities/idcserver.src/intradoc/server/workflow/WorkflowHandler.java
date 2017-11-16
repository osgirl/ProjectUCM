/*      */ package intradoc.server.workflow;
/*      */ 
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcCounterUtils;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.DataUtils;
/*      */ import intradoc.server.DocServiceHandler;
/*      */ import intradoc.server.DocStateTransition;
/*      */ import intradoc.server.IdcServiceAction;
/*      */ import intradoc.server.InternetFunctions;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceHandler;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.project.ProjectInfo;
/*      */ import intradoc.server.project.Projects;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.workflow.WfStepData;
/*      */ import intradoc.shared.workflow.WorkflowData;
/*      */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.Date;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class WorkflowHandler extends ServiceHandler
/*      */ {
/*      */   protected DocServiceHandler m_docHandler;
/*      */   protected WorkflowDocImplementor m_wfImplementor;
/*      */   protected WorkflowData m_workflowData;
/*      */   protected WfStepData m_stepData;
/*      */   protected String m_wfName;
/*      */   protected String m_securityGroup;
/*      */   protected int m_revIDIndex;
/*      */   protected int m_revLabelIndex;
/*      */   protected int m_statusIndex;
/*      */   protected int m_idIndex;
/*      */   protected int m_sgIndex;
/*      */   protected int m_rsIndex;
/*      */   protected int m_docIndex;
/*      */   protected int m_checkedOutIndex;
/*      */   static final int WF_ID = 0;
/*      */   static final int WFSTEP_ID = 1;
/*   94 */   public static final String[][] ID_INCREMENTS = { { "QnextWfID", "UnextWfID", "WfID" }, { "QnextWfStepID", "UnextWfStepID", "WfStepID" } };
/*      */ 
/*      */   public WorkflowHandler()
/*      */   {
/*   71 */     this.m_docHandler = null;
/*   72 */     this.m_wfImplementor = null;
/*      */ 
/*   74 */     this.m_workflowData = null;
/*   75 */     this.m_stepData = null;
/*      */ 
/*   77 */     this.m_wfName = null;
/*   78 */     this.m_securityGroup = null;
/*      */ 
/*   81 */     this.m_revIDIndex = -1;
/*   82 */     this.m_revLabelIndex = -1;
/*   83 */     this.m_statusIndex = -1;
/*   84 */     this.m_idIndex = -1;
/*      */ 
/*   86 */     this.m_sgIndex = -1;
/*   87 */     this.m_rsIndex = -1;
/*   88 */     this.m_docIndex = -1;
/*   89 */     this.m_checkedOutIndex = -1;
/*      */   }
/*      */ 
/*      */   public void init(Service service)
/*      */     throws ServiceException, DataException
/*      */   {
/*  103 */     super.init(service);
/*      */ 
/*  105 */     this.m_workflowData = ((WorkflowData)SharedObjects.getTable(WorkflowData.m_tableName));
/*      */ 
/*  107 */     this.m_docHandler = ((DocServiceHandler)ComponentClassFactory.createClassInstance("DocServiceHandler", "intradoc.server.DocServiceHandler", "!csWfCreateHandlerFailed"));
/*      */ 
/*  109 */     this.m_docHandler.init(this.m_service);
/*  110 */     this.m_docHandler.setIsWorkflow(true);
/*  111 */     this.m_docHandler.initImplementor();
/*  112 */     this.m_wfImplementor = this.m_docHandler.getWorkflowImplementor();
/*      */   }
/*      */ 
/*      */   public boolean executeAction(String actFunction)
/*      */     throws ServiceException, DataException
/*      */   {
/*  118 */     this.m_docHandler.setCurrentAction(this.m_service.getCurrentAction());
/*  119 */     return super.executeAction(actFunction);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createWorkflowID()
/*      */     throws ServiceException, DataException
/*      */   {
/*  130 */     incrementCounter(0, "dWfID");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void cancelWorkflow() throws ServiceException, DataException
/*      */   {
/*  136 */     cancelWorkflow(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void cancelCriteriaWorkflow() throws ServiceException, DataException
/*      */   {
/*  142 */     cancelWorkflow(true);
/*      */   }
/*      */ 
/*      */   public void cancelWorkflow(boolean isCriteria)
/*      */     throws ServiceException, DataException
/*      */   {
/*  148 */     String wfState = this.m_binder.get("dWfStatus");
/*  149 */     if (wfState.equalsIgnoreCase("INIT"))
/*      */     {
/*  151 */       return;
/*      */     }
/*      */ 
/*  154 */     boolean isStaging = false;
/*  155 */     if (isCriteria)
/*      */     {
/*  157 */       String projectID = this.m_binder.get("dProjectID");
/*  158 */       isStaging = projectID.length() > 0;
/*      */     }
/*      */ 
/*  162 */     this.m_binder.putLocal("dWfStatus", "INIT");
/*  163 */     this.m_workspace.execute("UworkflowStatus", this.m_binder);
/*      */ 
/*  165 */     DataResultSet dset = (DataResultSet)this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/*  166 */     if (dset.isEmpty())
/*      */     {
/*  168 */       return;
/*      */     }
/*      */ 
/*  171 */     if (!isCriteria)
/*      */     {
/*  173 */       this.m_statusIndex = ResultSetUtils.getIndexMustExist(dset, "dStatus");
/*      */     }
/*      */ 
/*  178 */     this.m_binder.removeLocal("dID");
/*  179 */     this.m_binder.removeLocal("dRevClassID");
/*  180 */     this.m_binder.removeLocal("dDocName");
/*  181 */     this.m_binder.removeLocal("dOriginalName");
/*  182 */     this.m_binder.removeLocal("dFormat");
/*  183 */     this.m_binder.removeLocal("dExtension");
/*  184 */     this.m_binder.removeLocal("dProcessingState");
/*  185 */     this.m_binder.removeLocal("dReleaseState");
/*  186 */     this.m_binder.removeLocal("dStatus");
/*  187 */     this.m_binder.removeLocal("dWorkflowStatus");
/*  188 */     this.m_binder.removeLocal("dPublishState");
/*  189 */     this.m_binder.removeLocal("dWfStepName");
/*      */ 
/*  191 */     Properties oldProps = this.m_binder.getLocalData();
/*  192 */     DataBinder tempBinder = this.m_binder.createShallowCopyCloneResultSets();
/*      */ 
/*  194 */     Hashtable topicUserMap = new Hashtable();
/*  195 */     Hashtable topicWorkMap = new Hashtable();
/*      */     try
/*      */     {
/*  198 */       for (dset.first(); dset.isRowPresent(); dset.next())
/*      */       {
/*  201 */         this.m_wfImplementor.resetDocumentState();
/*      */ 
/*  204 */         this.m_binder.addResultSet(this.m_currentAction.getParamAt(0), dset);
/*      */ 
/*  208 */         Properties docProps = dset.getCurrentRowProps();
/*  209 */         Properties localData = new Properties(oldProps);
/*  210 */         DataBinder.mergeHashTables(localData, docProps);
/*  211 */         this.m_binder.setLocalData(localData);
/*      */ 
/*  213 */         if ((isStaging) || (isCriteria))
/*      */         {
/*  216 */           DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, false, false, false, false, this.m_service);
/*      */ 
/*  218 */           DocStateTransition.updateStateAfterDocumentAdvance(this.m_binder, this.m_workspace);
/*      */         }
/*      */ 
/*  222 */         this.m_wfImplementor.deleteRevisionInformation(false, true, true, false, topicUserMap, topicWorkMap);
/*      */ 
/*  224 */         if ((isStaging) || (isCriteria)) {
/*      */           continue;
/*      */         }
/*  227 */         DataResultSet drset = this.m_wfImplementor.getAlreadyComputedWorkflowRevs();
/*  228 */         if (drset == null)
/*      */           continue;
/*  230 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  232 */           this.m_binder.setLocalData(new Properties(oldProps));
/*  233 */           String id = ResultSetUtils.getValue(drset, "dID");
/*  234 */           this.m_binder.putLocal("dID", id);
/*  235 */           ResultSet rset = this.m_workspace.createResultSet("QdocInfo", this.m_binder);
/*  236 */           if ((rset == null) || (rset.isEmpty()))
/*      */           {
/*  238 */             String msg = LocaleUtils.encodeMessage("csContentItemRevisionMissing", null, id);
/*  239 */             throw new DataException(msg);
/*      */           }
/*  241 */           this.m_binder.attemptRawSynchronizeLocale(rset);
/*  242 */           DataResultSet infoSet = new DataResultSet();
/*  243 */           infoSet.copy(rset);
/*  244 */           this.m_binder.clearResultSets();
/*  245 */           this.m_binder.addResultSet("DOC_INFO", infoSet);
/*      */ 
/*  249 */           this.m_service.executeService("DELETE_BYREV_REVISION");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  257 */       this.m_binder.setLocalData(oldProps);
/*  258 */       this.m_binder.copyResultSetStateShallow(tempBinder);
/*      */     }
/*      */ 
/*  262 */     WorkflowUtils.updateWorkflowTopic(topicUserMap, topicWorkMap, this.m_workspace, this.m_service);
/*      */ 
/*  265 */     this.m_docHandler.deleteFilesInDeleteList();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void reportDeletedDocuments()
/*      */     throws DataException, ServiceException
/*      */   {
/*  279 */     String wfDocumentsName = this.m_currentAction.getParamAt(0);
/*  280 */     if (null == wfDocumentsName)
/*      */     {
/*  282 */       return;
/*      */     }
/*  284 */     DataResultSet wfDocuments = (DataResultSet)this.m_binder.getResultSet(wfDocumentsName);
/*  285 */     if ((null == wfDocuments) || (wfDocuments.getNumRows() < 1))
/*      */     {
/*  287 */       return;
/*      */     }
/*      */ 
/*  290 */     Properties oldLocalData = (Properties)this.m_binder.getLocalData().clone();
/*      */     try
/*      */     {
/*  294 */       for (wfDocuments.first(); wfDocuments.isRowPresent(); wfDocuments.next())
/*      */       {
/*  296 */         this.m_wfImplementor.getAndLoadWorkflowInfoWithFlags("wfDocs", 8);
/*  297 */         this.m_binder.mergeResultSetRowIntoLocalData(wfDocuments);
/*      */ 
/*  299 */         this.m_binder.putLocal("dAction", "Exit");
/*  300 */         DataUtils.computeActionDates(this.m_binder, 0L);
/*  301 */         this.m_workspace.execute("IworkflowDocHistory", this.m_binder);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  306 */       Report.trace("workflow", null, e);
/*      */     }
/*      */     finally
/*      */     {
/*  310 */       this.m_binder.setLocalData(oldLocalData);
/*  311 */       this.m_binder.removeResultSet("wfDocs");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addWfDocuments() throws DataException, ServiceException
/*      */   {
/*  318 */     String docNames = this.m_binder.getLocal("docNames");
/*  319 */     Vector docs = StringUtils.parseArray(docNames, '\t', '^');
/*      */ 
/*  321 */     int num = docs.size();
/*  322 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  324 */       String name = (String)docs.elementAt(i);
/*  325 */       this.m_binder.putLocal("dDocName", name);
/*      */ 
/*  327 */       this.m_service.executeService("ADD_WORKFLOWDOCUMENT_SUB");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteWfDocuments()
/*      */     throws DataException, ServiceException
/*      */   {
/*  335 */     String str = this.m_binder.getLocal("docNames");
/*  336 */     Vector docNames = StringUtils.parseArray(str, '\t', '^');
/*  337 */     int num = docNames.size();
/*      */ 
/*  339 */     String wfState = this.m_binder.get("dWfStatus");
/*  340 */     boolean isInprocess = wfState.equalsIgnoreCase("INPROCESS");
/*  341 */     if (isInprocess)
/*      */     {
/*  344 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowDocuments", this.m_binder);
/*  345 */       DataResultSet drset = new DataResultSet();
/*  346 */       drset.copy(rset);
/*      */ 
/*  348 */       if (drset.getNumRows() <= num)
/*      */       {
/*  350 */         this.m_service.createServiceException(null, "!csWfDeleteDenied");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  356 */     this.m_binder.removeLocal("dID");
/*  357 */     this.m_binder.removeLocal("dRevClassID");
/*  358 */     this.m_binder.removeLocal("dDocName");
/*  359 */     this.m_binder.removeLocal("dOriginalName");
/*  360 */     this.m_binder.removeLocal("dFormat");
/*  361 */     this.m_binder.removeLocal("dExtension");
/*  362 */     this.m_binder.removeLocal("dProcessingState");
/*  363 */     this.m_binder.removeLocal("dReleaseState");
/*  364 */     this.m_binder.removeLocal("dStatus");
/*  365 */     this.m_binder.removeLocal("dWorkflowStatus");
/*  366 */     this.m_binder.removeLocal("dPublishState");
/*  367 */     this.m_binder.removeLocal("dWfStepName");
/*      */ 
/*  369 */     Properties oldProps = this.m_binder.getLocalData();
/*  370 */     DataBinder tempBinder = this.m_binder.createShallowCopyCloneResultSets();
/*      */ 
/*  372 */     Hashtable topicUserMap = new Hashtable();
/*  373 */     Hashtable topicWorkMap = new Hashtable();
/*      */     try
/*      */     {
/*  377 */       for (int i = 0; i < num; ++i)
/*      */       {
/*  380 */         this.m_wfImplementor.resetDocumentState();
/*      */ 
/*  384 */         this.m_binder.setLocalData(new Properties(oldProps));
/*      */ 
/*  387 */         String name = (String)docNames.elementAt(i);
/*  388 */         this.m_binder.putLocal("dDocName", name);
/*  389 */         ResultSet rsetInfo = this.m_workspace.createResultSet("QdocName", this.m_binder);
/*  390 */         String revClassID = ResultSetUtils.getValue(rsetInfo, "dRevClassID");
/*  391 */         this.m_binder.putLocal("dRevClassID", revClassID);
/*  392 */         String latestID = ResultSetUtils.getValue(rsetInfo, "dID");
/*  393 */         this.m_binder.putLocal("dID", latestID);
/*      */ 
/*  396 */         this.m_wfImplementor.deleteRevisionInformation(true, true, true, true, topicUserMap, topicWorkMap);
/*      */ 
/*  399 */         DataResultSet drset = this.m_wfImplementor.getAlreadyComputedWorkflowRevs();
/*  400 */         if (drset == null) {
/*      */           continue;
/*      */         }
/*  403 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  405 */           this.m_binder.setLocalData(new Properties(oldProps));
/*  406 */           String id = ResultSetUtils.getValue(drset, "dID");
/*  407 */           this.m_binder.putLocal("dID", id);
/*  408 */           ResultSet rset = this.m_workspace.createResultSet("QdocInfo", this.m_binder);
/*  409 */           if ((rset == null) || (rset.isEmpty()))
/*      */           {
/*  411 */             String msg = LocaleUtils.encodeMessage("csContentItemRevisionMissing", null, id);
/*  412 */             throw new DataException(msg);
/*      */           }
/*  414 */           this.m_binder.attemptRawSynchronizeLocale(rset);
/*  415 */           DataResultSet infoSet = new DataResultSet();
/*  416 */           infoSet.copy(rset);
/*  417 */           this.m_binder.clearResultSets();
/*  418 */           this.m_binder.addResultSet("DOC_INFO", infoSet);
/*      */ 
/*  422 */           this.m_service.executeService("DELETE_BYREV_REVISION");
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/*  429 */       this.m_binder.setLocalData(oldProps);
/*  430 */       this.m_binder.copyResultSetStateShallow(tempBinder);
/*      */     }
/*      */ 
/*  434 */     WorkflowUtils.updateWorkflowTopic(topicUserMap, topicWorkMap, this.m_workspace, this.m_service);
/*      */ 
/*  437 */     this.m_docHandler.deleteFilesInDeleteList();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void isDocCheckedOut() throws DataException, ServiceException
/*      */   {
/*  443 */     this.m_docHandler.isDocCheckedOut();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void approveDoc()
/*      */     throws DataException, ServiceException
/*      */   {
/*  450 */     boolean allowEditApprove = false;
/*  451 */     String workflowState = this.m_binder.getLocal("dWorkflowState");
/*  452 */     if ((workflowState != null) && (workflowState.equals("E")) && ((
/*  454 */       (this.m_service.isConditionVarTrue("AutoContributorAllowsReview")) || (SharedObjects.getEnvValueAsBoolean("AutoContributorAllowsReview", false)))))
/*      */     {
/*  457 */       allowEditApprove = true;
/*      */     }
/*      */ 
/*  460 */     if (allowEditApprove)
/*      */     {
/*  464 */       this.m_workspace.execute("DworkflowStateAll", this.m_binder);
/*      */     }
/*  466 */     prepareForApprove(allowEditApprove);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void approveEditDoc()
/*      */     throws DataException, ServiceException
/*      */   {
/*  479 */     prepareForApprove(true);
/*      */   }
/*      */ 
/*      */   public void prepareForApprove(boolean isAllowContributorStep)
/*      */     throws DataException, ServiceException
/*      */   {
/*  495 */     checkIsWorkflowActionRev();
/*      */ 
/*  498 */     String wfName = this.m_binder.get("dWfName");
/*  499 */     String stepName = this.m_binder.getLocal("curStepName");
/*  500 */     if (stepName == null)
/*      */     {
/*  502 */       stepName = this.m_binder.getLocal("dWfStepName");
/*  503 */       this.m_binder.putLocal("curStepName", stepName);
/*      */     }
/*  505 */     if (stepName != null)
/*      */     {
/*  508 */       int stepID = -1;
/*  509 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowStep2", this.m_binder);
/*  510 */       if ((rset != null & rset.isRowPresent()))
/*      */       {
/*  512 */         String str = ResultSetUtils.getValue(rset, "dWfStepID");
/*  513 */         stepID = NumberUtils.parseInteger(str, -1);
/*      */ 
/*  517 */         if (!this.m_service.isConditionVarTrue("IsSignAndApprove"))
/*      */         {
/*  519 */           String signStr = ResultSetUtils.getValue(rset, "dWfStepIsSignature");
/*  520 */           boolean isSignature = StringUtils.convertToBool(signStr, false);
/*  521 */           if (isSignature)
/*      */           {
/*  523 */             String msg = LocaleUtils.encodeMessage("csWfApprovalRequiresSignature", null, wfName, stepName);
/*      */ 
/*  525 */             this.m_service.createServiceException(null, msg);
/*      */           }
/*      */         }
/*      */       }
/*  529 */       if (stepID < 0)
/*      */       {
/*  531 */         String msg = LocaleUtils.encodeMessage("csWfApproveOnMissingStep", null, "" + stepID, wfName);
/*      */ 
/*  533 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */       else
/*      */       {
/*  537 */         int curStepID = DataBinderUtils.getInteger(this.m_binder, "dWfCurrentStepID", -1);
/*  538 */         if (curStepID != stepID)
/*      */         {
/*  540 */           String msg = LocaleUtils.encodeMessage("csWfApproveOnWrongStep", null, stepName, this.m_binder.getLocal("dWfStepName"), wfName);
/*      */ 
/*  542 */           this.m_service.createServiceException(null, msg);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  550 */     Date dte = new Date();
/*  551 */     this.m_binder.putLocal("dWfEntryTs", LocaleUtils.formatODBC(dte));
/*      */ 
/*  553 */     String stepType = this.m_binder.get("wfStepCheckinType");
/*  554 */     boolean isContributorStep = WorkflowScriptUtils.isContributorStep(stepType);
/*  555 */     if ((isAllowContributorStep) && (isContributorStep))
/*      */     {
/*  557 */       this.m_wfImplementor.validateUserForStep(true, ":C:", "contributor", true);
/*      */     }
/*      */     else
/*      */     {
/*  562 */       this.m_wfImplementor.validateUserForStep(true, ":R:", "reviewer", true);
/*      */ 
/*  564 */       checkIsReview(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkIsFinishedBasicWorkflow() throws DataException, ServiceException
/*      */   {
/*  571 */     String wfStatus = this.m_binder.get("dWfStatus");
/*  572 */     if (wfStatus.equals("INIT"))
/*      */     {
/*  575 */       return;
/*      */     }
/*      */ 
/*  578 */     DocStateTransition.checkIsFinishedBasicWorkflow(this.m_binder, this.m_workspace, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateWorkflowAndDocState()
/*      */     throws DataException, ServiceException
/*      */   {
/*  586 */     boolean isNotLatestRev = DataBinderUtils.getLocalBoolean(this.m_binder, "IsNotLatestRev", false);
/*  587 */     DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, isNotLatestRev, false, false, false, this.m_service);
/*      */ 
/*  589 */     DocStateTransition.updateStateAfterDocumentAdvance(this.m_binder, this.m_workspace);
/*      */ 
/*  592 */     PluginFilters.filter("updateWorkflowAndDocState", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void rejectDoc() throws DataException, ServiceException
/*      */   {
/*  598 */     checkIsWorkflowActionRev();
/*  599 */     this.m_wfImplementor.validateUserForStep(true, ":R:", "reviewer", false);
/*      */ 
/*  601 */     checkIsReview(false);
/*      */ 
/*  604 */     String stepName = this.m_binder.get("dWfStepName");
/*  605 */     this.m_binder.putLocal("dWfStepName", stepName);
/*  606 */     WfCompanionData wfCompanionData = WorkflowUtils.retrieveCompanionData(this.m_binder, this.m_service, this.m_workspace);
/*  607 */     WorkflowUtils.updateWorkflowHistory(wfCompanionData, this.m_binder, this.m_service);
/*      */ 
/*  609 */     SubjectManager.notifyChanged("workflows");
/*      */   }
/*      */ 
/*      */   protected void checkIsWorkflowActionRev()
/*      */     throws DataException, ServiceException
/*      */   {
/*  615 */     ResultSet rset = this.m_workspace.createResultSet("QlatestID", this.m_binder);
/*  616 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/*  618 */       this.m_service.createServiceException(null, "!csNoRevisions");
/*      */     }
/*  620 */     String latestId = ResultSetUtils.getValue(rset, "dID");
/*  621 */     String curID = this.m_binder.getLocal("dID");
/*  622 */     if (latestId.equals(curID))
/*      */       return;
/*  624 */     this.m_service.createServiceException(null, "!csRevIsNotLatest");
/*      */   }
/*      */ 
/*      */   public void checkIsReview(boolean isAllowStaging)
/*      */     throws DataException, ServiceException
/*      */   {
/*  630 */     String errMsg = null;
/*  631 */     String publishState = this.m_binder.get("dPublishState");
/*  632 */     String dWorkflowState = this.m_binder.get("dWorkflowState");
/*  633 */     if (publishState.length() > 0)
/*      */     {
/*  635 */       if (!publishState.equals("W"))
/*      */       {
/*  637 */         errMsg = "!csWfNotInWorkflow";
/*      */       }
/*  639 */       else if (!isAllowStaging)
/*      */       {
/*  641 */         errMsg = "!csCheckinItemPublished";
/*      */       }
/*      */     }
/*  644 */     else if ("RW".indexOf(dWorkflowState) < 0)
/*      */     {
/*  646 */       errMsg = "!csWfNotInReview";
/*      */     }
/*      */ 
/*  649 */     if (errMsg == null)
/*      */       return;
/*  651 */     this.m_service.createServiceException(null, errMsg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeRejectTargetStep()
/*      */     throws DataException, ServiceException
/*      */   {
/*  665 */     WfCompanionData wfCompanionData = WorkflowUtils.retrieveCompanionData(this.m_binder, this.m_service, this.m_workspace);
/*      */ 
/*  667 */     String curWfName = this.m_binder.get("dWfName");
/*  668 */     String str = wfCompanionData.m_data.getLocal("wfParentList");
/*  669 */     Vector parents = StringUtils.parseArray(str, '#', '^');
/*  670 */     int num = parents.size();
/*      */ 
/*  673 */     int wfStatusIndex = -1;
/*  674 */     int wfIdIndex = -1;
/*  675 */     int stepTypeIndex = -1;
/*      */ 
/*  677 */     DataBinder workingBinder = new DataBinder();
/*  678 */     workingBinder.putLocal("dWfID", this.m_binder.get("dWfID"));
/*      */ 
/*  680 */     boolean isSkipWorkflow = false;
/*  681 */     int index = 1;
/*  682 */     for (; index < num; ++index)
/*      */     {
/*  684 */       str = (String)parents.elementAt(index);
/*  685 */       if (str.charAt(0) == '*')
/*      */       {
/*  688 */         str = str.substring(1);
/*      */       }
/*      */ 
/*  691 */       String[] parent = WorkflowScriptUtils.parseTarget(str);
/*  692 */       String newWf = parent[1];
/*      */ 
/*  694 */       boolean isSameWf = curWfName.equalsIgnoreCase(newWf);
/*  695 */       if ((isSkipWorkflow) && (isSameWf))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  700 */       workingBinder.putLocal("dWfStepName", parent[0]);
/*  701 */       workingBinder.putLocal("dWfName", newWf);
/*  702 */       if (!isSameWf)
/*      */       {
/*  704 */         curWfName = newWf;
/*      */ 
/*  708 */         ResultSet wfSet = this.m_workspace.createResultSet("Qworkflow", workingBinder);
/*  709 */         if (wfStatusIndex < 0)
/*      */         {
/*  711 */           wfStatusIndex = ResultSetUtils.getIndexMustExist(wfSet, "dWfStatus");
/*  712 */           wfIdIndex = ResultSetUtils.getIndexMustExist(wfSet, "dWfID");
/*      */         }
/*  714 */         String wfStatus = wfSet.getStringValue(wfStatusIndex);
/*  715 */         if (!wfStatus.equalsIgnoreCase("INPROCESS"))
/*      */         {
/*  717 */           isSkipWorkflow = true;
/*  718 */           continue;
/*      */         }
/*  720 */         isSkipWorkflow = false;
/*      */ 
/*  722 */         workingBinder.putLocal("dWfID", wfSet.getStringValue(wfIdIndex));
/*      */       }
/*      */ 
/*  728 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowStep", workingBinder);
/*  729 */       if (rset.isEmpty())
/*      */       {
/*  731 */         String msg = LocaleUtils.encodeMessage("csWfNotInStep", null, parent[0], curWfName);
/*      */ 
/*  733 */         Report.trace("workflow", LocaleResources.localizeMessage(msg, new ExecutionContextAdaptor()), null);
/*      */       }
/*      */       else
/*      */       {
/*  738 */         if (stepTypeIndex < 0)
/*      */         {
/*  740 */           stepTypeIndex = ResultSetUtils.getIndexMustExist(rset, "dWfStepType");
/*      */         }
/*  742 */         String stepType = rset.getStringValue(stepTypeIndex);
/*  743 */         stepType = WorkflowScriptUtils.getUpgradedStepType(stepType);
/*  744 */         if (!WorkflowScriptUtils.isContributorStep(stepType))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  757 */         String stepId = ResultSetUtils.getValue(rset, "dWfStepID");
/*  758 */         this.m_binder.putLocal("dWfCurrentStepID", stepId);
/*  759 */         this.m_binder.putLocal("dWfStepID", stepId);
/*  760 */         this.m_binder.putLocal("dWfStepType", stepType);
/*  761 */         this.m_binder.putLocal("dWfStepName", parent[0]);
/*  762 */         this.m_binder.putLocal("dWfName", parent[1]);
/*  763 */         this.m_binder.putLocal("dWfID", workingBinder.getLocal("dWfID"));
/*  764 */         break;
/*      */       }
/*      */     }
/*      */ 
/*  768 */     if (index < num)
/*      */     {
/*  771 */       for (int i = 0; i < index; ++i)
/*      */       {
/*  773 */         parents.removeElementAt(0);
/*      */       }
/*  775 */       str = StringUtils.createString(parents, '#', '^');
/*  776 */       wfCompanionData.m_data.putLocal("wfParentList", str);
/*      */ 
/*  779 */       this.m_service.setCachedObject("WorkflowCompanionData", wfCompanionData);
/*      */     }
/*      */     else
/*      */     {
/*  785 */       String docName = this.m_binder.getLocal("dDocName");
/*  786 */       String errMsg = LocaleUtils.encodeMessage("csWfNoContributionStepForReject", null, docName);
/*      */ 
/*  788 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareMailForStepUsers() throws ServiceException, DataException
/*      */   {
/*  795 */     Vector stepUsers = WorkflowUtils.computeStepUsers(this.m_workspace, this.m_binder, false, this.m_service, false, true);
/*      */ 
/*  798 */     int numUsers = stepUsers.size();
/*  799 */     if (numUsers == 0)
/*      */     {
/*  801 */       throw new DataException("!csWfNoUsersToInform");
/*      */     }
/*      */ 
/*  805 */     Vector users = new IdcVector();
/*  806 */     StringBuffer emailBuff = new StringBuffer();
/*  807 */     for (int i = 0; i < numUsers; ++i)
/*      */     {
/*  809 */       UserData userData = (UserData)stepUsers.elementAt(i);
/*  810 */       String email = userData.getProperty("dEmail");
/*  811 */       if (emailBuff.length() > 0)
/*      */       {
/*  813 */         emailBuff.append(",");
/*      */       }
/*  815 */       emailBuff.append(email);
/*  816 */       users.addElement(userData.m_name);
/*      */     }
/*      */ 
/*  819 */     String mailPage = this.m_currentAction.getParamAt(0);
/*  820 */     String subjectTmp = this.m_currentAction.getParamAt(1);
/*      */ 
/*  822 */     Properties props = DataBinderUtils.createMergedProperties(this.m_binder);
/*  823 */     props.put("mailAddress", emailBuff.toString());
/*  824 */     props.put("mailTemplate", mailPage);
/*  825 */     props.put("mailSubject", subjectTmp);
/*      */ 
/*  827 */     String str = StringUtils.createString(users, ',', '^');
/*  828 */     props.put("wfUsers", str);
/*  829 */     this.m_binder.putLocal("wfUsers", str);
/*      */ 
/*  833 */     this.m_binder.putLocal("wfMailTemplate", mailPage);
/*  834 */     this.m_binder.putLocal("wfMailSubject", subjectTmp);
/*      */ 
/*  837 */     props.put("mailUsers", str);
/*  838 */     props.put("mailUserInfos", InternetFunctions.createMailUserInfoString(stepUsers));
/*      */ 
/*  841 */     this.m_service.setCachedObject("WorkMailUsers", stepUsers);
/*  842 */     this.m_service.setCachedObject("WorkMailInfo", props);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editCriteria()
/*      */     throws DataException, ServiceException
/*      */   {
/*  851 */     String state = this.m_binder.get("dWfStatus");
/*  852 */     String newType = this.m_binder.getLocal("dWfType");
/*  853 */     if (state.equals("INPROCESS"))
/*      */     {
/*  856 */       String projectID = this.m_binder.getFromSets("dProjectID");
/*  857 */       this.m_binder.putLocal("dProjectID", projectID);
/*      */ 
/*  859 */       if (SecurityUtils.m_useCollaboration)
/*      */       {
/*  864 */         boolean wasCollaboration = StringUtils.convertToBool(this.m_binder.getFromSets("dIsCollaboration"), false);
/*  865 */         boolean isNowClbra = StringUtils.convertToBool(this.m_binder.getLocal("dIsCollaboration"), false);
/*      */ 
/*  867 */         String oldType = this.m_binder.getFromSets("dWfType");
/*  868 */         boolean isChangedType = !oldType.equalsIgnoreCase(newType);
/*  869 */         if ((oldType.equalsIgnoreCase("subworkflow")) && (isChangedType) && (isNowClbra))
/*      */         {
/*  872 */           this.m_service.createServiceException(null, "!csWfClbraTypeChangeError");
/*      */         }
/*  874 */         if ((newType.equalsIgnoreCase("subworkflow")) && (isChangedType) && (wasCollaboration))
/*      */         {
/*  877 */           this.m_service.createServiceException(null, "!csWfClbraTypeChangeError");
/*      */         }
/*      */ 
/*  880 */         if ((newType.equalsIgnoreCase("criteria")) && 
/*  883 */           (wasCollaboration != isNowClbra))
/*      */         {
/*  885 */           this.m_service.createServiceException(null, "!csWfClbraEnableError");
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  892 */       checkUseProject();
/*      */     }
/*      */ 
/*  897 */     if (!newType.equalsIgnoreCase("subworkflow"))
/*      */       return;
/*  899 */     this.m_binder.putLocal("dIsCollaboration", "0");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkUseProject()
/*      */     throws ServiceException
/*      */   {
/*  906 */     boolean useProject = StringUtils.convertToBool(this.m_binder.getLocal("UseProject"), false);
/*  907 */     if (!useProject)
/*      */     {
/*  909 */       this.m_binder.putLocal("dProjectID", "");
/*      */     }
/*      */     else
/*      */     {
/*  913 */       String projectID = this.m_binder.getLocal("dProjectID");
/*  914 */       if (projectID == null)
/*      */       {
/*  916 */         this.m_service.createServiceException(null, "!csWfMissingProject");
/*      */       }
/*  918 */       ProjectInfo info = Projects.getProjectInfo(projectID);
/*  919 */       String errMsg = null;
/*  920 */       if (info != null)
/*      */       {
/*  922 */         String str = info.m_properties.getProperty("IsWfProjectRemovalPending");
/*  923 */         boolean isWfRemovalPending = StringUtils.convertToBool(str, false);
/*  924 */         if (isWfRemovalPending)
/*      */         {
/*  926 */           errMsg = "!csWfProjectBeingRemoved";
/*      */         }
/*  928 */         else if (!info.m_hasWorkflow)
/*      */         {
/*  930 */           errMsg = "!csWfProjectNotRegistered";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  935 */         errMsg = "!csWfProjectNotDefined";
/*      */       }
/*  937 */       if (errMsg == null)
/*      */         return;
/*  939 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/*  949 */     String wfName = this.m_binder.get("dWfName");
/*  950 */     String type = this.m_binder.getLocal("dWfType");
/*  951 */     boolean isCriteriaType = type.equalsIgnoreCase("criteria");
/*  952 */     boolean isSubWorkflow = type.equalsIgnoreCase("subworkflow");
/*      */ 
/*  955 */     createInitialStep((isCriteriaType) || (isSubWorkflow));
/*      */ 
/*  957 */     checkUseProject();
/*      */ 
/*  959 */     boolean hasTemplate = StringUtils.convertToBool(this.m_binder.getLocal("HasTemplate"), false);
/*  960 */     if (hasTemplate)
/*      */     {
/*  963 */       String templateName = this.m_binder.getLocal("dWfTemplateName");
/*  964 */       if (templateName == null)
/*      */       {
/*  966 */         throw new DataException("!csWfMissingTemplateParam");
/*      */       }
/*      */ 
/*  969 */       String filename = templateName.toLowerCase() + ".hda";
/*      */ 
/*  972 */       DataBinder templateData = WorkflowUtils.readTemplate(filename, true);
/*      */ 
/*  974 */       DataResultSet steps = (DataResultSet)templateData.getResultSet("WorkflowSteps");
/*  975 */       if (steps == null)
/*      */       {
/*  977 */         String msg = LocaleUtils.encodeMessage("csWfTemplateNotExist", null, templateName);
/*  978 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*  980 */       this.m_binder.addResultSet("WorkflowSteps", steps);
/*      */ 
/*  988 */       String[] keys = { "dAliases" };
/*  989 */       FieldInfo[] infos = ResultSetUtils.createInfoList(steps, keys, true);
/*      */ 
/*  991 */       for (steps.first(); steps.isRowPresent(); steps.next())
/*      */       {
/*  993 */         String aliasStr = steps.getStringValue(infos[0].m_index);
/*  994 */         Vector aliases = StringUtils.parseArray(aliasStr, '\t', '\t');
/*      */ 
/*  996 */         incrementCounter(1, "dWfStepID");
/*  997 */         this.m_workspace.execute("IworkflowStep", this.m_binder);
/*      */ 
/*  999 */         int num = aliases.size();
/* 1000 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 1002 */           this.m_binder.putLocal("dAlias", (String)aliases.elementAt(i));
/* 1003 */           this.m_binder.putLocal("dAliasType", (String)aliases.elementAt(++i));
/* 1004 */           this.m_workspace.execute("IworkflowStepAlias", this.m_binder);
/*      */         }
/*      */       }
/*      */ 
/* 1008 */       DataBinder designData = new DataBinder();
/* 1009 */       DataResultSet eventSet = (DataResultSet)templateData.getResultSet("WorkflowStepEvents");
/* 1010 */       if (eventSet != null)
/*      */       {
/* 1012 */         designData.addResultSet("WorkflowStepEvents", eventSet);
/*      */ 
/* 1015 */         infos = ResultSetUtils.createInfoList(eventSet, WorkflowScriptUtils.WF_EVENT_COLUMNS, true);
/* 1016 */         for (eventSet.first(); eventSet.isRowPresent(); eventSet.next())
/*      */         {
/* 1018 */           String stepName = eventSet.getStringValue(infos[0].m_index);
/* 1019 */           for (int i = 1; i < WorkflowScriptUtils.WF_EVENT_COLUMNS.length; ++i)
/*      */           {
/* 1021 */             String event = eventSet.getStringValue(infos[i].m_index);
/* 1022 */             if ((event == null) || (event.length() <= 0)) {
/*      */               continue;
/*      */             }
/* 1025 */             String rName = stepName + "_" + event + "_WorkflowScriptJumps";
/* 1026 */             ResultSet rset = templateData.getResultSet(rName);
/* 1027 */             if (rset == null)
/*      */             {
/* 1029 */               String errMsg = LocaleUtils.encodeMessage("csWfDesignEventError", null, event, wfName, stepName);
/*      */ 
/* 1031 */               this.m_service.createServiceException(null, errMsg);
/*      */             }
/* 1033 */             designData.addResultSet(rName, rset);
/*      */ 
/* 1036 */             String[] cInfos = WorkflowScriptUtils.CUSTOM_SCRIPT_VALUES;
/* 1037 */             for (int j = 0; j < cInfos.length; ++j)
/*      */             {
/* 1039 */               String key = stepName + "_" + event + "_" + cInfos[j];
/* 1040 */               String val = templateData.getLocal(key);
/* 1041 */               if (val == null)
/*      */                 continue;
/* 1043 */               designData.putLocal(key, val);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1051 */         Properties props = templateData.getLocalData();
/* 1052 */         WorkflowScriptUtils.updateStepExitConditionInfo(props, designData);
/*      */ 
/* 1055 */         FileUtils.reserveDirectory(WfDesignManager.m_designDir);
/*      */         try
/*      */         {
/* 1058 */           WfDesignManager.writeWorkflowDesign(wfName, designData);
/*      */         }
/*      */         finally
/*      */         {
/* 1062 */           FileUtils.releaseDirectory(WfDesignManager.m_designDir);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1067 */     String cll = this.m_binder.getLocal("dIsCollaboration");
/* 1068 */     if (cll == null)
/*      */     {
/* 1070 */       this.m_binder.putLocal("dIsCollaboration", "0");
/*      */     }
/* 1072 */     if (isSubWorkflow)
/*      */     {
/* 1075 */       this.m_binder.putLocal("dIsCollaboration", "0");
/*      */     }
/*      */ 
/* 1078 */     if ((!isCriteriaType) && (!isSubWorkflow))
/*      */       return;
/* 1080 */     this.m_workspace.execute("IworkflowCriteria", this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void createInitialStep(boolean isCriteriaType)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1087 */     incrementCounter(1, "dWfStepID");
/* 1088 */     String stepType = determineInitialStepType(isCriteriaType);
/*      */ 
/* 1090 */     this.m_binder.putLocal("dWfStepType", stepType);
/* 1091 */     this.m_workspace.execute("IworkflowInitStep", this.m_binder);
/* 1092 */     this.m_binder.removeLocal("dWfStepType");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateContributorStep() throws DataException
/*      */   {
/* 1098 */     String type = this.m_binder.getLocal("dWfType");
/* 1099 */     boolean isCriteriaType = (type.equalsIgnoreCase("criteria")) || (type.equalsIgnoreCase("subworkflow"));
/* 1100 */     String updateQuery = this.m_currentAction.getParamAt(0);
/* 1101 */     String stepType = determineInitialStepType(isCriteriaType);
/* 1102 */     if ((stepType == null) || (stepType.length() <= 0))
/*      */       return;
/* 1104 */     this.m_binder.putLocal("dWfStepType", stepType);
/* 1105 */     this.m_workspace.execute(updateQuery, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected String determineInitialStepType(boolean isCriteriaType)
/*      */   {
/* 1111 */     String stepType = this.m_binder.getLocal("dWfAutoContributeStepType");
/* 1112 */     if ((stepType == null) || (stepType.length() == 0))
/*      */     {
/* 1114 */       if (isCriteriaType)
/*      */       {
/* 1116 */         stepType = ":C:CA:CE:";
/*      */       }
/*      */       else
/*      */       {
/* 1120 */         stepType = ":C:CE:";
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1125 */       int index = stepType.indexOf(":CA:");
/* 1126 */       if (isCriteriaType)
/*      */       {
/* 1128 */         if (index < 0)
/*      */         {
/* 1130 */           int index2 = stepType.indexOf(":C:");
/* 1131 */           if (index2 >= 0)
/*      */           {
/* 1133 */             stepType = stepType.substring(0, index2 + 3) + "CA:" + stepType.substring(index2 + 3);
/*      */           }
/*      */           else
/*      */           {
/* 1137 */             stepType = stepType + "CA:";
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/* 1143 */       else if (index >= 0)
/*      */       {
/* 1145 */         stepType = stepType.substring(0, index + 1) + stepType.substring(index + 4);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1150 */     return stepType;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addAliases() throws DataException, ServiceException
/*      */   {
/* 1156 */     String str = this.m_binder.getLocal("aliases");
/* 1157 */     Vector names = StringUtils.parseArray(str, '\t', '^');
/*      */ 
/* 1159 */     int num = names.size();
/* 1160 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1162 */       String name = (String)names.elementAt(i);
/* 1163 */       this.m_binder.putLocal("dAlias", name);
/* 1164 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowStepAlias", this.m_binder);
/* 1165 */       if (rset.isRowPresent())
/*      */         continue;
/* 1167 */       this.m_workspace.execute("IworkflowAlias", this.m_binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteAliases()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1175 */     checkAliasDelete();
/* 1176 */     changeWorkflowObjects("aliases", "dAlias", "DworkflowAlias");
/*      */   }
/*      */ 
/*      */   protected void changeWorkflowObjects(String objsKey, String key, String query)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1182 */     String str = this.m_binder.getLocal(objsKey);
/* 1183 */     Vector names = StringUtils.parseArray(str, '\t', '^');
/*      */ 
/* 1185 */     int num = names.size();
/* 1186 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1188 */       String name = (String)names.elementAt(i);
/* 1189 */       this.m_binder.putLocal(key, name);
/* 1190 */       this.m_workspace.execute(query, this.m_binder);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void checkAliasDelete() throws DataException, ServiceException
/*      */   {
/* 1196 */     String wfState = this.m_binder.get("dWfStatus");
/* 1197 */     if (wfState.equalsIgnoreCase("INIT"))
/*      */     {
/* 1199 */       return;
/*      */     }
/*      */ 
/* 1202 */     String rsetName = this.m_currentAction.getParamAt(0);
/* 1203 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet(rsetName);
/* 1204 */     int numRows = rset.getNumRows();
/*      */ 
/* 1206 */     String str = this.m_binder.getLocal("aliases");
/* 1207 */     Vector aliases = StringUtils.parseArray(str, '\t', '^');
/*      */ 
/* 1209 */     if (numRows >= aliases.size() + 1)
/*      */       return;
/* 1211 */     this.m_service.createServiceException(null, "!csWfLastStepAlias");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addWorkflowStep()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1218 */     addOrEditStep(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editWorkflowStep() throws DataException, ServiceException
/*      */   {
/* 1224 */     addOrEditStep(false);
/*      */   }
/*      */ 
/*      */   protected void addOrEditStep(boolean isNew)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1233 */     String wfStatus = this.m_binder.get("dWfStatus");
/* 1234 */     boolean isInProcess = wfStatus.equals("INPROCESS");
/* 1235 */     if ((isInProcess) && (isNew))
/*      */     {
/* 1237 */       this.m_service.createServiceException(null, "!csWfNewStepToActive");
/*      */     }
/*      */ 
/* 1240 */     String stepSQL = this.m_currentAction.getParamAt(0);
/* 1241 */     if (isNew)
/*      */     {
/* 1243 */       incrementCounter(1, "dWfStepID");
/*      */     }
/*      */     else
/*      */     {
/* 1248 */       this.m_workspace.execute("DworkflowStepAliases", this.m_binder);
/*      */     }
/*      */ 
/* 1251 */     this.m_workspace.execute(stepSQL, this.m_binder);
/*      */ 
/* 1253 */     String aliasStr = this.m_binder.getLocal("dAliases");
/* 1254 */     Vector aliases = StringUtils.parseArray(aliasStr, '\t', '^');
/* 1255 */     int numAliases = aliases.size();
/* 1256 */     for (int i = 0; i < numAliases; ++i)
/*      */     {
/* 1258 */       this.m_binder.putLocal("dAlias", (String)aliases.elementAt(i));
/* 1259 */       this.m_binder.putLocal("dAliasType", (String)aliases.elementAt(++i));
/* 1260 */       this.m_workspace.execute("IworkflowAlias", this.m_binder);
/*      */     }
/*      */ 
/* 1263 */     if (!isInProcess)
/*      */       return;
/* 1265 */     checkEditedStep();
/*      */   }
/*      */ 
/*      */   protected void checkEditedStep()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1273 */     this.m_stepData = new WfStepData();
/* 1274 */     this.m_stepData.loadStepDataFromBinder(this.m_binder, 0);
/*      */ 
/* 1276 */     DataBinder binder = new DataBinder();
/* 1277 */     binder.addResultSet("WorkflowSteps", this.m_stepData);
/*      */ 
/* 1279 */     Vector users = WorkflowUtils.computeStepUsers(this.m_workspace, binder, true, this.m_service, false, false);
/*      */ 
/* 1281 */     boolean hasTokens = StringUtils.convertToBool(this.m_binder.getLocal("hasTokens"), false);
/* 1282 */     this.m_stepData.addUsers(users, hasTokens);
/*      */ 
/* 1284 */     String errMsg = this.m_stepData.validateStep();
/* 1285 */     if (errMsg != null)
/*      */     {
/* 1287 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */ 
/* 1291 */     String oldWeightStr = this.m_binder.getFromSets("dWfStepWeight");
/*      */ 
/* 1293 */     boolean isAll = this.m_stepData.getIsAll();
/* 1294 */     boolean oldIsAll = StringUtils.convertToBool(this.m_binder.getFromSets("dWfStepIsAll"), false);
/*      */ 
/* 1296 */     int weight = this.m_stepData.getWeight();
/* 1297 */     int oldWeight = 1;
/*      */     try
/*      */     {
/* 1300 */       oldWeight = Integer.parseInt(oldWeightStr);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/*      */     }
/*      */ 
/* 1307 */     if (isAll)
/*      */     {
/* 1309 */       if (isAll == oldIsAll)
/*      */       {
/* 1311 */         return;
/*      */       }
/*      */ 
/*      */     }
/* 1316 */     else if ((weight >= oldWeight) && (isAll == oldIsAll))
/*      */     {
/* 1318 */       return;
/*      */     }
/*      */ 
/* 1322 */     moveWorkflowDocuments();
/*      */   }
/*      */ 
/*      */   protected void moveWorkflowDocuments()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1332 */     ResultSet rset = this.m_workspace.createResultSet("QworkflowDocuments", this.m_binder);
/* 1333 */     DataResultSet drset = new DataResultSet();
/* 1334 */     drset.copy(rset);
/*      */ 
/* 1336 */     this.m_binder.putLocal("wfAction", "STEP_EDIT");
/*      */ 
/* 1338 */     if (drset.isEmpty())
/*      */     {
/* 1340 */       return;
/*      */     }
/*      */ 
/* 1344 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "dDocName");
/* 1345 */     Properties oldLocalData = this.m_binder.getLocalData();
/* 1346 */     boolean isBasicFinished = true;
/*      */     try
/*      */     {
/* 1350 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1352 */         Properties newLocalData = new Properties(oldLocalData);
/* 1353 */         this.m_binder.setLocalData(newLocalData);
/* 1354 */         this.m_binder.putLocal("wfAction", "STEP_EDIT");
/* 1355 */         this.m_binder.putLocal("dDocName", drset.getStringValue(nameIndex));
/* 1356 */         ResultSet rsetInfo = this.m_workspace.createResultSet("QdocNameMeta", this.m_binder);
/* 1357 */         if (!rsetInfo.isEmpty())
/*      */         {
/* 1360 */           this.m_binder.mergeResultSetRowIntoLocalData(rsetInfo);
/* 1361 */           rsetInfo.closeInternals();
/*      */ 
/* 1365 */           this.m_binder.putLocal("isFinished", "0");
/*      */ 
/* 1368 */           this.m_service.setCachedObject("WorkflowCompanionData", "dummy");
/*      */ 
/* 1371 */           this.m_binder.putLocal("suppressBasicWorkflowFinishTest", "1");
/* 1372 */           updateWorkflowAndDocState();
/* 1373 */           String workflowState = this.m_binder.get("dWorkflowState");
/*      */ 
/* 1377 */           if (!workflowState.equals("P"))
/*      */           {
/* 1379 */             isBasicFinished = false;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1384 */           isBasicFinished = false;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1394 */       this.m_binder.setLocalData(oldLocalData);
/*      */     }
/*      */ 
/* 1398 */     if (!isBasicFinished) {
/*      */       return;
/*      */     }
/*      */ 
/* 1402 */     DocStateTransition.checkIsFinishedBasicWorkflow(this.m_binder, this.m_workspace, this.m_service);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateSteps()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1411 */     String wfType = this.m_binder.get("dWfType");
/* 1412 */     boolean isCriteria = (wfType.equalsIgnoreCase("criteria")) || (wfType.equalsIgnoreCase("subworkflow"));
/*      */ 
/* 1415 */     String projectID = this.m_binder.get("dProjectID");
/* 1416 */     boolean isStaging = projectID.length() > 0;
/*      */ 
/* 1418 */     this.m_stepData = this.m_wfImplementor.getWorkflowStepInfo(0, true, true);
/*      */ 
/* 1420 */     int numSteps = this.m_stepData.getNumRows();
/*      */ 
/* 1422 */     if ((numSteps < 1) || ((isCriteria) && (numSteps < 2)))
/*      */     {
/* 1424 */       this.m_service.createServiceException(null, "!csWfNeedsStep");
/*      */     }
/*      */ 
/* 1427 */     for (this.m_stepData.first(); this.m_stepData.isRowPresent(); this.m_stepData.next())
/*      */     {
/* 1429 */       if (isCriteria)
/*      */       {
/* 1432 */         String stepType = this.m_stepData.getStepType();
/* 1433 */         if (WorkflowScriptUtils.isAutoContributorStep(stepType)) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1437 */         if ((isStaging) && (WorkflowScriptUtils.isManualContributorStep(stepType)))
/*      */         {
/* 1439 */           String stepTypeDes = WorkflowScriptUtils.formatLocalizedStepTypeDescription(stepType, this.m_service);
/* 1440 */           String errMsg = LocaleUtils.encodeMessage("csWfStepTypeNotAllowedInStaging", null, stepTypeDes, this.m_stepData.getStepName());
/*      */ 
/* 1442 */           this.m_service.createServiceException(null, errMsg);
/*      */         }
/*      */       }
/*      */ 
/* 1446 */       String errMsg = this.m_stepData.validateStep();
/* 1447 */       if (errMsg == null)
/*      */         continue;
/* 1449 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void canDeleteStep()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1458 */     String wfStatus = this.m_binder.get("dWfStatus");
/* 1459 */     boolean isInProcess = wfStatus.equals("INPROCESS");
/* 1460 */     if (!isInProcess)
/*      */       return;
/* 1462 */     this.m_service.createServiceException(null, "!csWfDeleteStepFromActive");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getWorkflowDocumentInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1470 */     String wfDocsName = this.m_currentAction.getParamAt(0);
/*      */ 
/* 1472 */     DataResultSet docSet = (DataResultSet)this.m_binder.getResultSet(wfDocsName);
/* 1473 */     if ((docSet == null) || (docSet.isEmpty()))
/*      */     {
/* 1475 */       return;
/*      */     }
/*      */ 
/* 1478 */     DataResultSet stepSet = (DataResultSet)this.m_binder.getResultSet("WorkflowSteps");
/* 1479 */     if ((stepSet == null) || (stepSet.isEmpty()))
/*      */     {
/* 1481 */       return;
/*      */     }
/*      */ 
/* 1484 */     String[] keys = { "dWfDocState", "dWfCurrentStepID", "dWfComputed" };
/* 1485 */     FieldInfo[] docInfos = ResultSetUtils.createInfoList(docSet, keys, true);
/*      */ 
/* 1487 */     String[] stepKeys = { "dWfStepID", "dWfStepName" };
/* 1488 */     FieldInfo[] stepInfos = ResultSetUtils.createInfoList(stepSet, stepKeys, true);
/* 1489 */     for (docSet.first(); docSet.isRowPresent(); docSet.next())
/*      */     {
/* 1491 */       String docState = docSet.getStringValue(docInfos[0].m_index);
/* 1492 */       String stepStr = "inactive";
/* 1493 */       if (!docState.equalsIgnoreCase("init"))
/*      */       {
/* 1495 */         String stepID = docSet.getStringValue(docInfos[1].m_index);
/* 1496 */         Vector values = stepSet.findRow(stepInfos[0].m_index, stepID);
/* 1497 */         if (values != null)
/*      */         {
/* 1499 */           stepStr = (String)values.elementAt(stepInfos[1].m_index);
/*      */         }
/*      */       }
/* 1502 */       docSet.setCurrentValue(docInfos[2].m_index, stepStr);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void incrementCounter(int type, String counterKey)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1510 */     long nextValue = IdcCounterUtils.nextValue(this.m_workspace, ID_INCREMENTS[type][2]);
/*      */ 
/* 1513 */     this.m_binder.putLocal(counterKey, "" + nextValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeWorkflowInfo() throws DataException, ServiceException
/*      */   {
/* 1519 */     String wfInfoName = this.m_currentAction.getParamAt(0);
/* 1520 */     String rsName = null;
/* 1521 */     this.m_binder.putLocal("isCurRevEmpty", "");
/* 1522 */     if (this.m_currentAction.getNumParams() >= 2)
/*      */     {
/* 1524 */       rsName = this.m_currentAction.getParamAt(1);
/* 1525 */       if ((rsName != null) && (rsName.length() == 0))
/*      */       {
/* 1527 */         rsName = null;
/*      */       }
/* 1529 */       if (rsName != null)
/*      */       {
/* 1537 */         this.m_binder.putLocal("SecurityProfileResultSet", rsName);
/* 1538 */         this.m_wfImplementor.loadWorkflowStateInfo(rsName);
/*      */       }
/*      */     }
/* 1541 */     boolean isInWorkflow = this.m_wfImplementor.checkWorkflow(wfInfoName, "loadInfoCheckIsWorkflow");
/* 1542 */     if (rsName != null)
/*      */     {
/* 1544 */       this.m_binder.removeLocal("SecurityProfileResultSet");
/*      */     }
/* 1546 */     if (!isInWorkflow)
/*      */     {
/* 1548 */       this.m_service.createServiceException(null, "!csWfNotInWorkflow");
/*      */     }
/*      */ 
/* 1553 */     this.m_service.setCachedObject("WorkflowDocImplementor", this.m_wfImplementor);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkWorkflowStart()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1565 */     this.m_wfName = this.m_binder.getLocal("dWfName");
/* 1566 */     this.m_securityGroup = this.m_binder.getLocal("dSecurityGroup");
/*      */ 
/* 1569 */     String wfState = this.m_workflowData.getWorkflowState(this.m_wfName);
/* 1570 */     if (wfState == null)
/*      */     {
/* 1572 */       this.m_service.createServiceException(null, "!csWfMissing");
/*      */     }
/* 1574 */     if (!wfState.equalsIgnoreCase("INIT"))
/*      */     {
/* 1576 */       this.m_service.createServiceException(null, "!csWfNotInInit");
/*      */     }
/*      */ 
/* 1579 */     DataResultSet dset = (DataResultSet)this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/* 1580 */     if (dset == null)
/*      */     {
/* 1582 */       this.m_service.createServiceException(null, "!csWfInfoMissing");
/*      */     }
/* 1584 */     if (dset.isEmpty())
/*      */     {
/* 1586 */       this.m_service.createServiceException(null, "!csWfNoDocuments");
/*      */     }
/*      */ 
/* 1589 */     setCheckDocIndexes(dset);
/*      */ 
/* 1591 */     for (dset.first(); dset.isRowPresent(); dset.next())
/*      */     {
/* 1594 */       checkWfDocState(dset);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setCheckDocIndexes(ResultSet rset) throws DataException
/*      */   {
/* 1600 */     this.m_sgIndex = ResultSetUtils.getIndexMustExist(rset, "dSecurityGroup");
/* 1601 */     this.m_rsIndex = ResultSetUtils.getIndexMustExist(rset, "dReleaseState");
/* 1602 */     this.m_docIndex = ResultSetUtils.getIndexMustExist(rset, "dDocName");
/* 1603 */     this.m_checkedOutIndex = ResultSetUtils.getIndexMustExist(rset, "dCheckoutUser");
/*      */   }
/*      */ 
/*      */   protected void checkWfDocState(ResultSet rset)
/*      */     throws ServiceException
/*      */   {
/* 1611 */     String docName = rset.getStringValue(this.m_docIndex);
/*      */ 
/* 1613 */     String checkedOutUser = rset.getStringValue(this.m_checkedOutIndex);
/* 1614 */     if ((checkedOutUser != null) && (checkedOutUser.length() > 0))
/*      */     {
/* 1616 */       String errMsg = LocaleUtils.encodeMessage("csWfDocCheckedOut", null, docName, checkedOutUser);
/*      */ 
/* 1618 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */ 
/* 1621 */     String releaseState = rset.getStringValue(this.m_rsIndex);
/* 1622 */     if ((releaseState != null) && (releaseState.length() > 0) && (releaseState.equals("E") == true))
/*      */     {
/* 1625 */       String errMsg = LocaleUtils.encodeMessage("csWfDocLocked", null, docName);
/* 1626 */       this.m_service.createServiceException(null, errMsg);
/*      */     }
/*      */ 
/* 1629 */     String securityGroup = rset.getStringValue(this.m_sgIndex);
/* 1630 */     if ((securityGroup == null) || (securityGroup.length() <= 0) || (securityGroup.equalsIgnoreCase(this.m_securityGroup))) {
/*      */       return;
/*      */     }
/* 1633 */     String errMsg = LocaleUtils.encodeMessage("csWfDocInWrongGroup", null, docName, this.m_securityGroup);
/*      */ 
/* 1635 */     this.m_service.createServiceException(null, errMsg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkDocState()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1642 */     String wfState = this.m_binder.get("dWfStatus");
/* 1643 */     if (wfState.equalsIgnoreCase("INIT"))
/*      */     {
/* 1645 */       return;
/*      */     }
/*      */ 
/* 1653 */     ResultSet rset = this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/* 1654 */     if (rset == null)
/*      */     {
/* 1656 */       this.m_service.createServiceException(null, "!csWfErrorRetrievingInfo");
/*      */     }
/* 1658 */     if (rset.isEmpty())
/*      */     {
/* 1660 */       return;
/*      */     }
/*      */ 
/* 1663 */     this.m_securityGroup = this.m_binder.get("dSecurityGroup");
/* 1664 */     setCheckDocIndexes(rset);
/* 1665 */     checkWfDocState(rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void startWorkflow() throws ServiceException, DataException
/*      */   {
/* 1671 */     startWorkflow(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void startCriteriaWorkflow() throws ServiceException, DataException
/*      */   {
/* 1677 */     startWorkflow(true);
/*      */   }
/*      */ 
/*      */   public void startWorkflow(boolean isCriteria) throws ServiceException, DataException
/*      */   {
/* 1682 */     if (!isCriteria)
/*      */     {
/* 1685 */       DataResultSet dset = (DataResultSet)this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/*      */ 
/* 1688 */       Properties workMailInfo = (Properties)this.m_service.getCachedObject("WorkMailInfo");
/* 1689 */       String users = "";
/* 1690 */       if (workMailInfo != null)
/*      */       {
/* 1692 */         users = workMailInfo.getProperty("wfUsers");
/*      */       }
/*      */ 
/* 1695 */       Properties localData = this.m_binder.getLocalData();
/*      */       try
/*      */       {
/* 1698 */         createRevInfo(dset);
/* 1699 */         for (dset.first(); dset.isRowPresent(); dset.next())
/*      */         {
/* 1702 */           this.m_binder.setLocalData(new Properties());
/* 1703 */           this.m_binder.putLocal("dSecurityGroup", localData.getProperty("dSecurityGroup"));
/* 1704 */           this.m_binder.putLocal("dCreateDate", localData.getProperty("dCreateDate"));
/* 1705 */           this.m_binder.putLocal("dInDate", localData.getProperty("dCreateDate"));
/*      */ 
/* 1707 */           createNewRev(dset);
/*      */ 
/* 1710 */           Properties props = dset.getCurrentRowProps();
/* 1711 */           DataBinder.mergeHashTables(this.m_binder.getLocalData(), props);
/* 1712 */           this.m_binder.putLocal("wfAction", "START_WORKFLOW");
/* 1713 */           this.m_binder.putLocal("isFinished", "0");
/* 1714 */           this.m_binder.putLocal("wfUsers", users);
/*      */ 
/* 1717 */           String docName = this.m_binder.getLocal("dDocName");
/* 1718 */           String subDir = this.m_binder.getLocal("dWfDirectory");
/* 1719 */           if ((subDir != null) && (subDir.length() > 0))
/*      */           {
/* 1721 */             WfCompanionManager.deleteCompanionFile(docName, subDir, this.m_binder, false);
/*      */           }
/*      */ 
/* 1728 */           this.m_binder.putLocal("dReleaseState", "E");
/* 1729 */           this.m_binder.putLocal("dWorkflowState", "E");
/* 1730 */           this.m_binder.putLocal("dStatus", "EDIT");
/* 1731 */           updateWorkflowAndDocState();
/*      */ 
/* 1734 */           this.m_service.setCachedObject("WorkflowCompanionData", "dummy");
/*      */         }
/*      */ 
/*      */       }
/*      */       finally
/*      */       {
/* 1740 */         this.m_binder.setLocalData(localData);
/*      */       }
/*      */ 
/* 1744 */       if (workMailInfo != null)
/*      */       {
/* 1746 */         Vector mailQueue = (Vector)this.m_service.getCachedObject("MailQueue");
/* 1747 */         mailQueue.addElement(workMailInfo);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1752 */     this.m_binder.putLocal("dWfStatus", "INPROCESS");
/* 1753 */     this.m_workspace.execute("UworkflowStatus", this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void createRevInfo(DataResultSet dset) throws DataException
/*      */   {
/* 1758 */     Date dte = new Date();
/* 1759 */     String createDate = LocaleUtils.formatODBC(dte);
/* 1760 */     this.m_binder.putLocal("dCreateDate", createDate);
/* 1761 */     this.m_binder.putLocal("dInDate", createDate);
/*      */ 
/* 1764 */     if (dset == null)
/*      */       return;
/* 1766 */     if (this.m_revIDIndex < 0)
/*      */     {
/* 1768 */       this.m_revIDIndex = ResultSetUtils.getIndexMustExist(dset, "dRevisionID");
/*      */     }
/* 1770 */     if (this.m_revLabelIndex >= 0)
/*      */       return;
/* 1772 */     this.m_revLabelIndex = ResultSetUtils.getIndexMustExist(dset, "dRevLabel");
/*      */   }
/*      */ 
/*      */   public void createNewRev(DataResultSet dset)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1779 */     boolean isNew = false;
/*      */ 
/* 1781 */     if (dset != null)
/*      */     {
/* 1783 */       if (this.m_idIndex < 0)
/*      */       {
/* 1785 */         this.m_idIndex = ResultSetUtils.getIndexMustExist(dset, "dID");
/*      */       }
/*      */ 
/* 1788 */       String id = dset.getStringValue(this.m_idIndex);
/* 1789 */       if ((id == null) || (id.length() == 0))
/*      */       {
/* 1792 */         isNew = true;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1797 */       isNew = true;
/*      */     }
/*      */ 
/* 1800 */     if (isNew)
/*      */     {
/* 1803 */       makeNewWfRevClass(dset);
/*      */     }
/*      */     else
/*      */     {
/* 1807 */       updateRevisionID(dset);
/*      */     }
/*      */ 
/* 1810 */     this.m_docHandler.addIncrementID(dset, 1, "dID");
/* 1811 */     this.m_docHandler.addIncrementID(dset, 2, "dDocID");
/*      */ 
/* 1813 */     this.m_service.setConditionVar("IsEmptyRev", true);
/* 1814 */     this.m_docHandler.updateExtendedAttributes("newRevEmpty");
/*      */ 
/* 1816 */     long affected = this.m_workspace.execute("IrevisionID", this.m_binder);
/* 1817 */     affected = this.m_workspace.execute("IdocMetaID", this.m_binder);
/* 1818 */     affected = this.m_workspace.execute("IdocumentID", this.m_binder);
/* 1819 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1821 */       Report.debug("workflow", "Result of inserting doc row " + affected, null);
/*      */     }
/*      */ 
/* 1824 */     this.m_binder.putLocal("dWfDocState", "INPROCESS");
/* 1825 */     affected = this.m_workspace.execute("UworkflowDocStart", this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createNewRev() throws ServiceException, DataException
/*      */   {
/* 1831 */     String wfState = this.m_binder.get("dWfStatus");
/* 1832 */     if (wfState.equalsIgnoreCase("INIT"))
/*      */     {
/* 1834 */       return;
/*      */     }
/*      */ 
/* 1838 */     DataResultSet dset = (DataResultSet)this.m_binder.getResultSet(this.m_currentAction.getParamAt(0));
/* 1839 */     if (dset.isEmpty())
/*      */     {
/* 1841 */       dset = null;
/*      */     }
/*      */ 
/* 1846 */     ResultSet stepSet = this.m_workspace.createResultSet("QworkflowSteps", this.m_binder);
/* 1847 */     if ((stepSet == null) || (stepSet.isEmpty()))
/*      */     {
/* 1849 */       throw new DataException("!csWfNoSteps");
/*      */     }
/* 1851 */     String currentStep = ResultSetUtils.getValue(stepSet, "dWfStepID");
/* 1852 */     this.m_binder.putLocal("dWfStepID", currentStep);
/*      */ 
/* 1854 */     createRevInfo(dset);
/* 1855 */     createNewRev(dset);
/*      */   }
/*      */ 
/*      */   public void makeNewWfRevClass(DataResultSet dSet) throws ServiceException, DataException
/*      */   {
/* 1860 */     if (dSet == null)
/*      */     {
/* 1862 */       this.m_docHandler.makeNewRevClass();
/* 1863 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1868 */       this.m_docHandler.addIncrementID(dSet, 0, "dRevClassID");
/*      */ 
/* 1870 */       dSet.setCurrentValue(this.m_revIDIndex, "1");
/* 1871 */       dSet.setCurrentValue(this.m_revLabelIndex, RevisionSpec.getFirst());
/*      */ 
/* 1873 */       this.m_docHandler.addRevClassesEntry();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1878 */       this.m_service.createServiceException(e, "!csWfUnableToCreateRev");
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateRevisionID(DataResultSet dSet) throws ServiceException, DataException
/*      */   {
/* 1884 */     if (dSet == null)
/*      */     {
/* 1886 */       this.m_docHandler.updateRevisionID(true);
/* 1887 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1892 */       int revID = Integer.parseInt(this.m_binder.get("dRevisionID"));
/* 1893 */       dSet.setCurrentValue(this.m_revIDIndex, String.valueOf(revID + 1));
/* 1894 */       String revLabel = this.m_binder.get("dRevLabel");
/* 1895 */       String nextRev = RevisionSpec.getNext(revLabel);
/* 1896 */       if (nextRev == null)
/*      */       {
/* 1898 */         nextRev = RevisionSpec.getInvalidLabel();
/*      */       }
/* 1900 */       dSet.setCurrentValue(this.m_revLabelIndex, RevisionSpec.getNext(revLabel));
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1904 */       this.m_service.createServiceException(e, "!csInvalidRevision");
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeCompanionDirectory() throws DataException, ServiceException
/*      */   {
/* 1911 */     String dir = WfCompanionManager.createSubDirectory(this.m_binder);
/* 1912 */     this.m_binder.putLocal("dWfDirectory", dir);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getWorkflowStepAliasesInfo() throws DataException, ServiceException
/*      */   {
/* 1918 */     getWorkflowStepInfo(1);
/*      */   }
/*      */ 
/*      */   public void getWorkflowStepInfo(int type) throws DataException, ServiceException
/*      */   {
/* 1923 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 1925 */     this.m_wfImplementor.getWorkflowStepInfo(type, true, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getWfDocuments()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1932 */     String tableName = this.m_currentAction.getParamAt(0);
/* 1933 */     DataResultSet rset = WorkflowUtils.getWorkflowDocuments(this.m_workspace, this.m_binder);
/* 1934 */     this.m_binder.addResultSet(tableName, rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeWfDocumentsInfo() throws DataException, ServiceException
/*      */   {
/* 1940 */     if (this.m_wfImplementor == null)
/*      */       return;
/* 1942 */     this.m_wfImplementor.computeWfDocumentsInfo(this.m_currentAction.getParamAt(0));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeRemainingUsers()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1951 */     String stateSetName = this.m_currentAction.getParamAt(0);
/* 1952 */     String remainingUsersKey = this.m_currentAction.getParamAt(1);
/*      */ 
/* 1955 */     WfStepData stepData = WorkflowUtils.loadCurrentStepInfo(this.m_binder, this.m_workspace, this.m_service);
/*      */ 
/* 1958 */     DataResultSet stateSet = (DataResultSet)this.m_binder.getResultSet(stateSetName);
/* 1959 */     Vector users = stepData.getUsers();
/*      */ 
/* 1961 */     if (stateSet.isRowPresent())
/*      */     {
/* 1963 */       int userIndex = ResultSetUtils.getIndexMustExist(stateSet, "dUserName");
/* 1964 */       for (stateSet.first(); stateSet.isRowPresent(); stateSet.next())
/*      */       {
/* 1966 */         String userName = stateSet.getStringValue(userIndex);
/* 1967 */         int num = users.size();
/* 1968 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 1970 */           String user = (String)users.elementAt(i);
/* 1971 */           if (!user.equalsIgnoreCase(userName))
/*      */             continue;
/* 1973 */           users.removeElementAt(i);
/* 1974 */           break;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1980 */     String str = StringUtils.createString(users, ',', '^');
/* 1981 */     this.m_binder.putLocal(remainingUsersKey, str);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateWfColumns() throws DataException, ServiceException
/*      */   {
/* 1987 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("WorkflowInQueue");
/* 1988 */     if (drset == null)
/*      */     {
/* 1990 */       return;
/*      */     }
/*      */ 
/* 1993 */     List finfo = ResultSetUtils.createFieldInfo(new String[] { "wfAllowedStepActions" }, 120);
/* 1994 */     drset.mergeFieldsWithFlags(finfo, 2);
/*      */ 
/* 1996 */     DataBinder workBinder = new DataBinder();
/* 1997 */     int actionIndex = ((FieldInfo)finfo.get(0)).m_index;
/* 1998 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2000 */       Properties wfInfo = drset.getCurrentRowProps();
/* 2001 */       workBinder.setLocalData(wfInfo);
/* 2002 */       String actions = computeAllowedStepActions(workBinder);
/* 2003 */       drset.setCurrentValue(actionIndex, actions);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String computeAllowedStepActions(DataBinder wfBinder)
/*      */   {
/* 2009 */     WorkflowUtils.computeAllowedStepActions(wfBinder, this.m_service.getUserData(), this.m_service);
/* 2010 */     String actions = wfBinder.getLocal("computedWfStepActions");
/* 2011 */     return actions;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void signWorkflowContentItem() throws DataException, ServiceException
/*      */   {
/* 2017 */     if (!this.m_service.isConditionVarTrue("IsSignAndApprove"))
/*      */       return;
/* 2019 */     PluginFilters.filter("workflowSignAndApprove", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2025 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99974 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WorkflowHandler
 * JD-Core Version:    0.5.4
 */