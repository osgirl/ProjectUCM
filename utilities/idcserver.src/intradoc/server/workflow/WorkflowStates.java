/*      */ package intradoc.server.workflow;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.LocaleResources;
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
/*      */ import intradoc.server.DataUtils;
/*      */ import intradoc.server.InternetFunctions;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.UserStorage;
/*      */ import intradoc.server.project.Projects;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.shared.workflow.WfStepData;
/*      */ import intradoc.shared.workflow.WorkflowData;
/*      */ import intradoc.shared.workflow.WorkflowInfo;
/*      */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class WorkflowStates
/*      */ {
/*   73 */   public DataBinder m_binder = null;
/*   74 */   public Workspace m_workspace = null;
/*   75 */   public ExecutionContext m_context = null;
/*      */ 
/*   84 */   public boolean m_useLocal = false;
/*      */ 
/*   88 */   public boolean m_docConverted = false;
/*      */ 
/*   92 */   public boolean m_historyOnly = false;
/*      */ 
/*   96 */   public boolean m_isNotLatestRev = false;
/*      */ 
/*   99 */   public boolean m_skipWorkflowLoad = false;
/*  100 */   public boolean m_skipUpdateStatus = false;
/*      */ 
/*  105 */   public WorkflowInfo m_workflowInfo = null;
/*  106 */   public boolean m_isCriteria = false;
/*  107 */   public WfStepData m_stepData = null;
/*      */ 
/*  109 */   public Vector m_stepUsers = null;
/*  110 */   public DataResultSet m_basicWorkflowDocuments = null;
/*      */ 
/*  112 */   public WfDesignData m_wfDesign = null;
/*  113 */   public WfCompanionData m_wfCompanionData = null;
/*  114 */   public DataBinder m_scriptResultData = null;
/*      */ 
/*  117 */   public boolean m_isMoveToPending = false;
/*  118 */   public boolean m_isFinishedWorkflow = false;
/*  119 */   public boolean m_isInWorkflow = false;
/*  120 */   public boolean m_hasWorkflowId = false;
/*  121 */   public boolean m_isStatusChange = false;
/*  122 */   public boolean m_suppressWorkflowNotification = false;
/*      */ 
/*  125 */   public List m_mailQueue = null;
/*  126 */   public int m_historyMailStartCount = 0;
/*  127 */   public Vector m_workUsers = null;
/*  128 */   public Properties m_workMailInfo = null;
/*      */ 
/*  131 */   public Hashtable m_topicUserMap = null;
/*  132 */   public Hashtable m_topicWorkMap = null;
/*      */ 
/*  136 */   public boolean m_loadedWorkflowInfo = false;
/*  137 */   public String m_primaryFileResultSetName = null;
/*      */ 
/*  140 */   public boolean m_isOutOfContext = false;
/*      */ 
/*  142 */   public boolean m_suppressBasicWorkflowFinishTest = false;
/*      */ 
/*  145 */   public WfStateHelper m_wfHelper = null;
/*  146 */   public Hashtable m_previousStepMap = null;
/*  147 */   public Vector m_parents = null;
/*      */ 
/*      */   public void init(DataBinder binder, Workspace ws, ExecutionContext ctxt)
/*      */   {
/*  156 */     this.m_binder = binder;
/*  157 */     this.m_workspace = ws;
/*  158 */     this.m_context = ctxt;
/*      */ 
/*  161 */     this.m_isOutOfContext = StringUtils.convertToBool((String)ctxt.getCachedObject("IsOutOfContext"), false);
/*      */ 
/*  163 */     if (this.m_isOutOfContext)
/*      */     {
/*  166 */       UserData userData = null;
/*      */       try
/*      */       {
/*  169 */         String user = this.m_binder.get("dDocAuthor");
/*  170 */         userData = UserStorage.retrieveUserDatabaseProfileData(user, this.m_workspace, this.m_context);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  174 */         Report.error(null, "!csWfRetrieveUserError", e);
/*      */       }
/*      */ 
/*  179 */       if (userData == null)
/*      */       {
/*  181 */         userData = UserUtils.createUserData();
/*  182 */         userData.m_name = "@@system";
/*  183 */         userData.checkCreateAttributes(false);
/*  184 */         userData.m_hasAttributesLoaded = true;
/*  185 */         UserUtils.getOrCreateCachedProfile(userData, "ProfileSystemUser");
/*      */       }
/*      */ 
/*  190 */       Service wService = (Service)this.m_context;
/*  191 */       wService.setUserData(userData);
/*  192 */       this.m_binder.putLocal("dUser", userData.m_name);
/*      */     }
/*      */ 
/*  196 */     this.m_context.setCachedObject("WorkflowStates", this);
/*      */ 
/*  198 */     initMailQueue(false);
/*  199 */     this.m_workUsers = ((Vector)this.m_context.getCachedObject("WorkMailUsers"));
/*  200 */     this.m_workMailInfo = ((Properties)this.m_context.getCachedObject("WorkMailInfo"));
/*  201 */     if (this.m_workMailInfo != null)
/*      */     {
/*  203 */       this.m_workMailInfo.put("IsInformUsers", "1");
/*      */     }
/*      */ 
/*  206 */     this.m_previousStepMap = new Hashtable();
/*      */ 
/*  208 */     this.m_topicUserMap = new Hashtable();
/*  209 */     this.m_topicWorkMap = new Hashtable();
/*      */   }
/*      */ 
/*      */   public void initMailQueue(boolean forceNewQueue)
/*      */   {
/*  223 */     List l = null;
/*  224 */     if (!forceNewQueue)
/*      */     {
/*  226 */       l = (List)this.m_context.getCachedObject("MailQueue");
/*      */     }
/*  228 */     if (l == null)
/*      */     {
/*  231 */       l = new IdcVector();
/*  232 */       this.m_context.setCachedObject("MailQueue", l);
/*  233 */       this.m_historyMailStartCount = 0;
/*      */     }
/*      */     else
/*      */     {
/*  238 */       this.m_historyMailStartCount = l.size();
/*      */     }
/*  240 */     this.m_mailQueue = l;
/*      */   }
/*      */ 
/*      */   public void computeWorkflowInfo(boolean isSpecificDoc)
/*      */     throws DataException, ServiceException
/*      */   {
/*  248 */     String wfName = this.m_binder.getAllowMissing("dWfName");
/*  249 */     if ((wfName == null) || (wfName.length() == 0))
/*      */     {
/*  251 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowForID", this.m_binder);
/*  252 */       if ((rset == null) || (rset.isEmpty()))
/*      */       {
/*  254 */         throw new DataException("!csWfComputeWfError");
/*      */       }
/*  256 */       wfName = ResultSetUtils.getValue(rset, "dWfName");
/*      */     }
/*      */ 
/*  259 */     if (SystemUtils.m_verbose)
/*      */     {
/*  261 */       Report.debug("workflow", "computeWorkflowInfo isSpecificDoc=" + isSpecificDoc + "dWfName=" + wfName, null);
/*      */     }
/*      */ 
/*  265 */     this.m_binder.putLocal("dWfName", wfName);
/*      */ 
/*  267 */     WorkflowData wfData = (WorkflowData)SharedObjects.getTable(WorkflowData.m_tableName);
/*  268 */     this.m_workflowInfo = wfData.getWorkflowInfo(wfName);
/*  269 */     if (this.m_workflowInfo == null)
/*      */     {
/*  271 */       String wfID = this.m_binder.getAllowMissing("dWfID");
/*  272 */       String msg = LocaleUtils.encodeMessage("csWfMissing", null, wfName, wfID);
/*  273 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  276 */     String type = this.m_workflowInfo.m_properties.getProperty("dWfType");
/*  277 */     if ((type.equalsIgnoreCase("criteria")) || (type.equalsIgnoreCase("subworkflow")))
/*      */     {
/*  279 */       this.m_isCriteria = true;
/*      */     }
/*      */ 
/*  283 */     if (isSpecificDoc)
/*      */     {
/*  285 */       retrieveCompanionData();
/*      */     }
/*      */ 
/*  289 */     if (SecurityUtils.m_useCollaboration)
/*      */     {
/*  291 */       String clbraStr = this.m_workflowInfo.m_properties.getProperty("dIsCollaboration");
/*  292 */       boolean isClbra = StringUtils.convertToBool(clbraStr, false);
/*  293 */       if (isClbra)
/*      */       {
/*  295 */         WorkflowUtils.retrieveCollaborationData(this.m_context, this.m_binder);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  300 */     if (!isSpecificDoc)
/*      */       return;
/*  302 */     String str = this.m_wfCompanionData.m_data.getLocal("wfParentList");
/*  303 */     this.m_parents = StringUtils.parseArray(str, '#', '^');
/*  304 */     this.m_context.setCachedObject("WorkflowParents", this.m_parents);
/*      */ 
/*  307 */     boolean isFinished = StringUtils.convertToBool(this.m_binder.getLocal("isFinished"), true);
/*  308 */     this.m_binder.putLocal("isFinished", String.valueOf(isFinished));
/*  309 */     this.m_binder.putLocal("WfEditFinished", String.valueOf(isFinished));
/*      */   }
/*      */ 
/*      */   public void retrieveCompanionData()
/*      */     throws DataException, ServiceException
/*      */   {
/*  316 */     this.m_wfCompanionData = WorkflowUtils.retrieveCompanionData(this.m_binder, this.m_context, this.m_workspace);
/*      */   }
/*      */ 
/*      */   public void determineCurrentSteps()
/*      */     throws DataException
/*      */   {
/*  323 */     this.m_stepData = WorkflowUtils.determineSteps(this.m_workspace, this.m_binder);
/*  324 */     this.m_binder.addResultSet("WorkflowSteps", this.m_stepData);
/*      */ 
/*  327 */     int index = ResultSetUtils.getIndexMustExist(this.m_stepData, "dWfStepID");
/*  328 */     String stepID = this.m_binder.getLocal("dWfCurrentStepID");
/*  329 */     Vector row = this.m_stepData.findRow(index, stepID);
/*  330 */     if (row == null)
/*      */     {
/*  332 */       String errMsg = LocaleUtils.encodeMessage("csWfDocInMissingStep", null, this.m_binder.get("dWfName"));
/*      */ 
/*  334 */       throw new DataException(errMsg);
/*      */     }
/*      */ 
/*  338 */     String wfName = this.m_binder.getLocal("dWfName");
/*  339 */     String stepName = this.m_stepData.getStepName();
/*  340 */     this.m_binder.putLocal("dWfStepName", stepName);
/*  341 */     this.m_wfHelper = new WfStateHelper(wfName, stepName);
/*      */   }
/*      */ 
/*      */   public void exchangeCompanionData(boolean isJump)
/*      */   {
/*  346 */     String prefix = this.m_wfHelper.m_lookupKey + ":";
/*  347 */     Properties props = this.m_wfCompanionData.m_data.getLocalData();
/*  348 */     int len = prefix.length();
/*  349 */     boolean hasCounter = false;
/*  350 */     for (Enumeration iter = props.keys(); iter.hasMoreElements(); )
/*      */     {
/*  352 */       String key = (String)iter.nextElement();
/*  353 */       if (key.startsWith(prefix))
/*      */       {
/*  355 */         String name = key.substring(len);
/*  356 */         String val = props.getProperty(key);
/*  357 */         this.m_binder.putLocal(name, val);
/*      */ 
/*  359 */         if (name.equals("entryCount"))
/*      */         {
/*  361 */           hasCounter = true;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  367 */     if (!hasCounter)
/*      */     {
/*  369 */       this.m_binder.putLocal("entryCount", "0");
/*      */     }
/*      */ 
/*  373 */     String wfAction = this.m_binder.getLocal("wfAction");
/*  374 */     boolean isReject = (wfAction != null) && (wfAction.equals("REJECT"));
/*  375 */     String msg = null;
/*  376 */     if (isReject)
/*      */     {
/*  378 */       msg = this.m_binder.getLocal("wfRejectMessage");
/*      */     }
/*  380 */     if (isJump)
/*      */     {
/*  382 */       String jumpMessage = props.getProperty("wfJumpMessage");
/*  383 */       if ((jumpMessage != null) && (jumpMessage.length() > 0))
/*      */       {
/*  385 */         if ((msg != null) && (msg.length() > 0))
/*      */         {
/*  387 */           msg = msg + " ";
/*      */         }
/*      */         else
/*      */         {
/*  391 */           msg = "";
/*      */         }
/*  393 */         msg = msg + jumpMessage;
/*      */       }
/*      */     }
/*  396 */     if (msg == null)
/*      */     {
/*  398 */       msg = "";
/*      */     }
/*  400 */     this.m_binder.putLocal("wfMessage", msg);
/*  401 */     props.remove("wfJumpMessage");
/*      */ 
/*  403 */     this.m_binder.putLocal("wfCurrentStepPrefix", this.m_wfHelper.m_lookupKey);
/*      */   }
/*      */ 
/*      */   protected DataResultSet getBasicWorkflowDocumentList() throws DataException
/*      */   {
/*  408 */     if (this.m_basicWorkflowDocuments != null)
/*      */     {
/*  410 */       return this.m_basicWorkflowDocuments;
/*      */     }
/*  412 */     ResultSet rset = WorkflowUtils.getWorkflowDocuments(this.m_workspace, this.m_binder);
/*  413 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/*  415 */       throw new DataException("!csWfNoDocuments");
/*      */     }
/*  417 */     DataResultSet drset = null;
/*  418 */     if (rset instanceof DataResultSet)
/*      */     {
/*  420 */       drset = (DataResultSet)rset;
/*      */     }
/*      */     else
/*      */     {
/*  424 */       drset = new DataResultSet();
/*  425 */       drset.copy(rset);
/*      */     }
/*  427 */     this.m_basicWorkflowDocuments = drset;
/*  428 */     return drset;
/*      */   }
/*      */ 
/*      */   protected boolean checkAndLoadWorkflowDocInfo() throws DataException
/*      */   {
/*  433 */     boolean isWorkflow = true;
/*  434 */     if (!this.m_loadedWorkflowInfo)
/*      */     {
/*  436 */       ResultSet rset = this.m_workspace.createResultSet("QworkflowDocument", this.m_binder);
/*  437 */       if (rset.isEmpty())
/*      */       {
/*  439 */         isWorkflow = false;
/*  440 */         this.m_hasWorkflowId = false;
/*      */       }
/*      */       else
/*      */       {
/*  445 */         DataResultSet drset = new DataResultSet();
/*  446 */         drset.copy(rset);
/*      */ 
/*  448 */         if (drset.getNumRows() > 1)
/*      */         {
/*  450 */           boolean foundIt = false;
/*  451 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*      */           {
/*  453 */             if (!ResultSetUtils.getValue(drset, "dWfDocState").equals("INPROCESS"))
/*      */               continue;
/*  455 */             foundIt = true;
/*  456 */             break;
/*      */           }
/*      */ 
/*  459 */           if (!foundIt)
/*      */           {
/*  461 */             drset.first();
/*      */           }
/*      */         }
/*      */ 
/*  465 */         this.m_binder.mergeResultSetRowIntoLocalData(drset);
/*  466 */         isWorkflow = this.m_binder.getLocal("dWfDocState").equals("INPROCESS");
/*  467 */         this.m_hasWorkflowId = true;
/*      */       }
/*  469 */       this.m_loadedWorkflowInfo = true;
/*      */     }
/*      */ 
/*  472 */     return isWorkflow;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void advanceDocumentState(boolean isNotLatestRev, boolean docConverted, boolean historyOnly)
/*      */     throws DataException, ServiceException
/*      */   {
/*  482 */     boolean useLocal = DataBinderUtils.getLocalBoolean(this.m_binder, "isDocStateAdvanceUseLocal", true);
/*  483 */     this.m_isNotLatestRev = isNotLatestRev;
/*  484 */     this.m_docConverted = docConverted;
/*  485 */     this.m_historyOnly = historyOnly;
/*  486 */     this.m_useLocal = useLocal;
/*      */ 
/*  488 */     String curWorkflowState = determineInitialStatusValue("dWorkflowState", "");
/*  489 */     String publishState = determineInitialStatusValue("dPublishState", "");
/*  490 */     String curReleaseState = determineInitialStatusValue("dReleaseState", "N");
/*  491 */     String curStatus = determineInitialStatusValue("dStatus", "DONE");
/*      */ 
/*  493 */     String[] statusArray = { curWorkflowState, publishState, curReleaseState, curStatus };
/*  494 */     this.m_context.setCachedObject("AdvanceDocumentStateStatusArray", statusArray);
/*      */ 
/*  498 */     this.m_context.setCachedObject("WorkflowUseLocal", (this.m_useLocal) ? Boolean.TRUE : Boolean.FALSE);
/*      */ 
/*  501 */     if (PluginFilters.filter("advanceDocumentStateStart", this.m_workspace, this.m_binder, this.m_context) != 0)
/*      */     {
/*  503 */       return;
/*      */     }
/*      */ 
/*  506 */     advanceDocumentStateImplement(statusArray);
/*      */   }
/*      */ 
/*      */   public void advanceDocumentState(boolean isNotLatestRev, boolean docConverted, boolean historyOnly, boolean useLocal)
/*      */     throws DataException, ServiceException
/*      */   {
/*  517 */     this.m_isNotLatestRev = isNotLatestRev;
/*  518 */     this.m_docConverted = docConverted;
/*  519 */     this.m_historyOnly = historyOnly;
/*  520 */     this.m_useLocal = useLocal;
/*      */ 
/*  522 */     String curWorkflowState = determineInitialStatusValue("dWorkflowState", "");
/*  523 */     String publishState = determineInitialStatusValue("dPublishState", "");
/*  524 */     String curReleaseState = determineInitialStatusValue("dReleaseState", "N");
/*  525 */     String curStatus = determineInitialStatusValue("dStatus", "DONE");
/*      */ 
/*  527 */     if ((curStatus.equals("GENWWW")) && (curWorkflowState.length() > 0) && (!this.m_docConverted) && 
/*  531 */       (DataBinderUtils.getBoolean(this.m_binder, "DelayWorkflowForConversion", false)))
/*      */     {
/*  534 */       this.m_suppressWorkflowNotification = true;
/*      */     }
/*      */ 
/*  538 */     String[] statusArray = { curWorkflowState, publishState, curReleaseState, curStatus };
/*  539 */     this.m_context.setCachedObject("AdvanceDocumentStateStatusArray", statusArray);
/*      */ 
/*  543 */     this.m_context.setCachedObject("WorkflowUseLocal", (this.m_useLocal) ? Boolean.TRUE : Boolean.FALSE);
/*      */ 
/*  546 */     if (PluginFilters.filter("advanceDocumentStateStart", this.m_workspace, this.m_binder, this.m_context) != 0)
/*      */     {
/*  548 */       return;
/*      */     }
/*      */ 
/*  551 */     advanceDocumentStateImplement(statusArray);
/*      */   }
/*      */ 
/*      */   public void advanceDocumentStateImplement(String[] statusArray)
/*      */     throws DataException, ServiceException
/*      */   {
/*  603 */     if (checkForSkipUpdate(statusArray))
/*      */     {
/*  605 */       return;
/*      */     }
/*  607 */     boolean isSpecialOldRevision = checkIsSpecialOldRevision(statusArray);
/*  608 */     if (isSpecialOldRevision)
/*      */     {
/*  610 */       handleSpecialOldRevisionState(statusArray);
/*      */     }
/*  612 */     String curWorkflowState = statusArray[0];
/*  613 */     String publishState = statusArray[1];
/*  614 */     String curReleaseState = statusArray[2];
/*  615 */     String curStatus = statusArray[3];
/*      */ 
/*  617 */     boolean isFullWorkflow = curWorkflowState.length() > 0;
/*  618 */     boolean isPublishWorkflow = publishState.equals("W");
/*      */ 
/*  621 */     if ((!isFullWorkflow) && (((curStatus.equals("EDIT")) || (curStatus.equals("REVIEW")) || (curReleaseState.equals("E")))))
/*      */     {
/*  623 */       isFullWorkflow = true;
/*      */ 
/*  627 */       curWorkflowState = (curStatus.equals("EDIT")) ? "E" : "R";
/*  628 */       this.m_binder.putLocal("dWorkflowState", curWorkflowState);
/*  629 */       this.m_isStatusChange = true;
/*      */     }
/*  631 */     this.m_isInWorkflow = ((isFullWorkflow) || (isPublishWorkflow));
/*  632 */     this.m_suppressBasicWorkflowFinishTest = DataBinderUtils.getLocalBoolean(this.m_binder, "suppressBasicWorkflowFinishTest", false);
/*      */ 
/*  634 */     if (this.m_suppressBasicWorkflowFinishTest)
/*      */     {
/*  637 */       this.m_binder.putLocal("suppressBasicWorkflowFinishTest", "");
/*      */     }
/*      */ 
/*  641 */     String revLabel = this.m_binder.get("dRevLabel");
/*  642 */     String docTitle = this.m_binder.get("dDocTitle");
/*  643 */     String revClassID = this.m_binder.get("dRevClassID");
/*      */ 
/*  645 */     Report.trace("workflow", "advanceDocumentState: dDocTitle=" + docTitle + " dRevLabel=" + revLabel + " dRevClassID=" + revClassID, null);
/*      */ 
/*  649 */     if ((this.m_isInWorkflow) && (!this.m_skipWorkflowLoad))
/*      */     {
/*  651 */       this.m_isInWorkflow = checkAndLoadWorkflowDocInfo();
/*  652 */       if (!this.m_isInWorkflow)
/*      */       {
/*  657 */         this.m_isFinishedWorkflow = true;
/*  658 */         this.m_isCriteria = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  663 */     String status = curStatus;
/*  664 */     boolean doWorkflow = false;
/*  665 */     if (this.m_isInWorkflow)
/*      */     {
/*  669 */       doWorkflow = true;
/*  670 */       if ((status.equals("DELETED")) || (status.equals("EXPIRED")) || (curWorkflowState.equals("P")))
/*      */       {
/*  672 */         doWorkflow = false;
/*      */       }
/*      */ 
/*  676 */       if ((doWorkflow) && 
/*  678 */         (this.m_isNotLatestRev))
/*      */       {
/*  680 */         doWorkflow = false;
/*      */       }
/*      */ 
/*  685 */       advanceWorkflowState(doWorkflow, this.m_historyOnly);
/*      */     }
/*  687 */     if (!this.m_skipUpdateStatus)
/*      */     {
/*  689 */       updateStateForDocRevisions(this.m_binder, this.m_docConverted, doWorkflow, isPublishWorkflow, curWorkflowState, curReleaseState, curStatus);
/*      */     }
/*      */ 
/*  693 */     if (this.m_isFinishedWorkflow)
/*      */     {
/*  697 */       markWorkflowFinished();
/*      */     }
/*      */ 
/*  701 */     if ((!this.m_historyOnly) && (((doWorkflow) || (this.m_isFinishedWorkflow))) && (!this.m_skipUpdateStatus))
/*      */     {
/*  704 */       WorkflowUtils.updateWorkflowTopic(this.m_topicUserMap, this.m_topicWorkMap, this.m_workspace, this.m_context);
/*      */     }
/*      */ 
/*  707 */     if (!this.m_isStatusChange)
/*      */       return;
/*  709 */     this.m_binder.putLocal("isStatusChanged", "1");
/*      */   }
/*      */ 
/*      */   public boolean checkForSkipUpdate(String[] statusArray)
/*      */     throws DataException, ServiceException
/*      */   {
/*  715 */     String workflowState = statusArray[0];
/*  716 */     boolean doAdvanceDocState = true;
/*  717 */     if ((workflowState != null) && (workflowState.length() > 0) && (!this.m_docConverted) && 
/*  719 */       (this.m_context instanceof Service))
/*      */     {
/*  721 */       Service service = (Service)this.m_context;
/*  722 */       if (!service.isConditionVarTrue("CheckedForEnteringWorkflow"))
/*      */       {
/*  724 */         boolean allowDocLock = !DataBinderUtils.getBoolean(this.m_binder, "noDocLock", false);
/*  725 */         this.m_binder.removeLocal("noDocLock");
/*      */ 
/*  731 */         if ((doAdvanceDocState) && (allowDocLock))
/*      */         {
/*  734 */           boolean skipQueryTest = false;
/*  735 */           String title = this.m_binder.getAllowMissing("dDocTitle");
/*  736 */           if ((title == null) || (title.length() == 0))
/*      */           {
/*  738 */             skipQueryTest = true;
/*      */           }
/*  740 */           String isCheckedOut = this.m_binder.getAllowMissing("dIsCheckedOut");
/*  741 */           if ((isCheckedOut != null) && (!isCheckedOut.equals("1")))
/*      */           {
/*  744 */             skipQueryTest = true;
/*      */           }
/*  746 */           else if (service.isConditionVarTrue("HasOriginal"))
/*      */           {
/*  748 */             if (service.isConditionVarTrue("IsEmptyRev"))
/*      */             {
/*  750 */               skipQueryTest = true;
/*      */             }
/*      */             else
/*      */             {
/*  754 */               String latestID = this.m_binder.getLocal("latestID");
/*  755 */               String id = this.m_binder.getLocal("dID");
/*  756 */               if (!id.equals(latestID))
/*      */               {
/*  758 */                 skipQueryTest = true;
/*      */               }
/*      */             }
/*      */           }
/*  762 */           if (!skipQueryTest)
/*      */           {
/*  764 */             ResultSet rset = this.m_workspace.createResultSet("QdocWebFormat", this.m_binder);
/*  765 */             if (rset.isEmpty())
/*      */             {
/*  767 */               Report.trace("documentlock", "Skipping request to advance workflow state because DB web rendition entry not present - " + this.m_binder.getAllowMissing("dDocName"), null);
/*      */ 
/*  769 */               doAdvanceDocState = false;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  776 */     return !doAdvanceDocState;
/*      */   }
/*      */ 
/*      */   public boolean checkIsSpecialOldRevision(String[] statusArray)
/*      */     throws DataException, ServiceException
/*      */   {
/*  782 */     boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false);
/*      */ 
/*  784 */     boolean isSpecialOldRevision = false;
/*  785 */     String curReleaseState = statusArray[2];
/*  786 */     String curStatus = statusArray[3];
/*  787 */     if ((!this.m_isNotLatestRev) && (!curStatus.equals("EXPIRED")))
/*      */     {
/*  789 */       if (curReleaseState.equals("O"))
/*      */       {
/*  791 */         isSpecialOldRevision = true;
/*      */       }
/*      */       else
/*      */       {
/*  795 */         String prevReleaseState = this.m_binder.getLocal("prevReleaseState");
/*  796 */         if ((prevReleaseState != null) && (prevReleaseState.equals("O")) && ("NR".indexOf(curReleaseState) >= 0))
/*      */         {
/*  799 */           isSpecialOldRevision = isUpdate;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  805 */     this.m_binder.putLocal("prevReleaseState", "");
/*      */ 
/*  807 */     return isSpecialOldRevision;
/*      */   }
/*      */ 
/*      */   public void handleSpecialOldRevisionState(String[] statusArray)
/*      */     throws DataException, ServiceException
/*      */   {
/*  813 */     boolean isUpdate = DataBinderUtils.getLocalBoolean(this.m_binder, "IsUpdate", false);
/*      */ 
/*  822 */     String curReleaseState = statusArray[2];
/*  823 */     String curStatus = statusArray[3];
/*  824 */     String wfAction = this.m_binder.getLocal("wfAction");
/*  825 */     if ((wfAction == null) || (wfAction.length() == 0))
/*      */     {
/*  827 */       wfAction = "<unknown action>";
/*      */     }
/*  829 */     String user = this.m_binder.getLocal("dUser");
/*  830 */     if ((user == null) || (user.length() == 0))
/*      */     {
/*  832 */       user = "<unknown user>";
/*      */     }
/*  834 */     String msg = LocaleUtils.encodeMessage("csWorkflowConsistencyIssueWhenDoingActionByUser", null, wfAction, user);
/*  835 */     String dID = this.m_binder.get("dID");
/*  836 */     String dDocName = this.m_binder.get("dDocName");
/*      */ 
/*  839 */     ResultSet rset = this.m_workspace.createResultSet("QrevisionsByNameDeleted", this.m_binder);
/*  840 */     boolean isBadlyFormed = false;
/*  841 */     boolean isDeleteInIndexerCycle = false;
/*  842 */     if (rset.isEmpty())
/*      */     {
/*  844 */       msg = LocaleUtils.encodeMessage("csWorkflowLatestRevMarkedAsOldRev", msg, dDocName, dID);
/*  845 */       Report.error(null, null, new ServiceException(msg));
/*  846 */       isBadlyFormed = true;
/*      */     }
/*      */     else
/*      */     {
/*  851 */       String indexerState = ResultSetUtils.getValue(rset, "dIndexerState");
/*  852 */       if (indexerState.trim().length() > 0)
/*      */       {
/*  854 */         isDeleteInIndexerCycle = true;
/*      */       }
/*      */     }
/*  857 */     rset = this.m_workspace.createResultSet("QdocInfoCurrentIndexed", this.m_binder);
/*  858 */     boolean hasReleasedDoc = !rset.isEmpty();
/*  859 */     if (hasReleasedDoc)
/*      */     {
/*  861 */       msg = LocaleUtils.encodeMessage("csWorkflowLatestRevMarkedAsOldEarlierRevMarkedReleased", msg, dDocName, dID);
/*  862 */       Report.error(null, msg, null);
/*  863 */       Report.trace(null, LocaleResources.localizeMessage(msg, this.m_context), null);
/*  864 */       isBadlyFormed = true;
/*      */     }
/*  866 */     curReleaseState = "N";
/*  867 */     if ((isDeleteInIndexerCycle) && (!hasReleasedDoc) && (!curStatus.equals("EXPIRED")))
/*      */     {
/*  876 */       Report.trace("indexer", "Marking old revision (dReleaseState='0') " + dID + " (dDocName=" + dDocName + ") as indexed since there is no later revision", null);
/*      */ 
/*  885 */       curReleaseState = "U";
/*      */     }
/*  887 */     else if (curStatus.equals("RELEASED"))
/*      */     {
/*  889 */       curStatus = "DONE";
/*  890 */       this.m_binder.putLocal("dStatus", "DONE");
/*      */     }
/*  892 */     this.m_binder.putLocal("dReleaseState", curReleaseState);
/*      */ 
/*  895 */     if ((isUpdate) || (SharedObjects.getEnvValueAsBoolean("AlwaysUpdateOldTipRevisionStatus", false)) || (isBadlyFormed))
/*      */     {
/*  897 */       this.m_isStatusChange = true;
/*      */     }
/*  899 */     statusArray[2] = curReleaseState;
/*  900 */     statusArray[3] = curStatus;
/*      */   }
/*      */ 
/*      */   protected String determineInitialStatusValue(String key, String dflt)
/*      */   {
/*  906 */     String curVal = this.m_binder.getAllowMissing(key);
/*  907 */     if ((curVal == null) || (curVal.length() == 0))
/*      */     {
/*  909 */       curVal = dflt;
/*  910 */       this.m_binder.putLocal(key, curVal);
/*      */     }
/*  912 */     return curVal;
/*      */   }
/*      */ 
/*      */   protected void advanceWorkflowState(boolean doWorkflow, boolean isHistoryOnly)
/*      */     throws DataException, ServiceException
/*      */   {
/*  931 */     if (doWorkflow)
/*      */     {
/*  933 */       computeWorkflowInfo(true);
/*      */     }
/*      */ 
/*  937 */     determineCurrentSteps();
/*      */ 
/*  940 */     if (!doWorkflow)
/*      */       return;
/*  942 */     updateHistory(false);
/*      */ 
/*  944 */     if (isHistoryOnly)
/*      */     {
/*  947 */       cleanUp();
/*      */     }
/*      */     else
/*      */     {
/*  951 */       this.m_isFinishedWorkflow = false;
/*  952 */       this.m_isMoveToPending = false;
/*      */ 
/*  956 */       if (!moveToNextStep())
/*      */       {
/*  958 */         renotifyUsers();
/*      */       }
/*  960 */       else if (isWorkflowFinished(true))
/*      */       {
/*  962 */         this.m_isFinishedWorkflow = true;
/*      */       }
/*  964 */       else if (this.m_stepData.isLastRow())
/*      */       {
/*  967 */         this.m_isMoveToPending = true;
/*      */       }
/*      */       else
/*      */       {
/*  972 */         Report.trace("workflow", "Workflow exited, not on last step, but not actually exiting workflow", null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateStateForDocRevisions(DataBinder binder, boolean docConverted, boolean didWorkflow, boolean isPublishWorkflow, String prevWorkflowState, String prevReleaseState, String prevStatus)
/*      */     throws DataException
/*      */   {
/*  987 */     String status = binder.getAllowMissing("dStatus");
/*  988 */     String releaseState = binder.getAllowMissing("dReleaseState");
/*  989 */     String workflowState = binder.getAllowMissing("dWorkflowState");
/*  990 */     boolean isDocReleased = !releaseState.equals("E");
/*  991 */     boolean releaseDoc = false;
/*  992 */     boolean unreleaseDoc = false;
/*  993 */     boolean isPendingDoc = workflowState.equals("P");
/*  994 */     boolean isWorkflowDoc = ((prevWorkflowState != null) && (prevWorkflowState.length() > 0)) || (workflowState.length() > 0);
/*  995 */     boolean isPotentialMassChange = false;
/*  996 */     boolean notifyIndexer = false;
/*      */ 
/*  999 */     Object docStateObj = this.m_context.getCachedObject("WfReleaseDocumentState");
/* 1000 */     if (docStateObj instanceof Integer)
/*      */     {
/* 1002 */       Integer intObj = (Integer)docStateObj;
/* 1003 */       int val = intObj.intValue();
/* 1004 */       if (val == 1)
/*      */       {
/* 1006 */         String title = binder.getAllowMissing("dDocTitle");
/* 1007 */         if ((title == null) || (title.length() == 0))
/*      */         {
/* 1010 */           Report.trace(null, "Document " + binder.getAllowMissing("dDocName") + " cannot be released if it is empty.", null);
/*      */         }
/*      */         else
/*      */         {
/* 1014 */           releaseDoc = true;
/*      */         }
/*      */       }
/* 1017 */       else if (val == 0)
/*      */       {
/* 1019 */         unreleaseDoc = true;
/*      */       }
/*      */ 
/* 1023 */       this.m_context.setCachedObject("WfReleaseDocumentState", "");
/*      */     }
/*      */ 
/* 1027 */     if ((status.equals("RELEASED")) && (!isDocReleased))
/*      */     {
/* 1029 */       String msg = LocaleUtils.encodeMessage("csWorkflowInconsistentReleaseState", null, binder.getAllowMissing("dDocName"), status);
/*      */ 
/* 1031 */       Report.error(null, msg, null);
/*      */     }
/* 1033 */     if (this.m_isFinishedWorkflow)
/*      */     {
/* 1035 */       releaseDoc = true;
/* 1036 */       workflowState = "";
/* 1037 */       isPendingDoc = false;
/* 1038 */       isPotentialMassChange = true;
/*      */     }
/* 1042 */     else if ((!isPendingDoc) && (isWorkflowDoc))
/*      */     {
/* 1044 */       String compareWorkflowState = prevWorkflowState;
/* 1045 */       if (compareWorkflowState == null)
/*      */       {
/* 1047 */         compareWorkflowState = workflowState;
/*      */       }
/* 1049 */       workflowState = this.m_stepData.getRequiredWorkflowState();
/* 1050 */       if (!workflowState.equals(compareWorkflowState))
/*      */       {
/* 1052 */         isPotentialMassChange = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1057 */     if ((!isDocReleased) && (releaseDoc))
/*      */     {
/* 1059 */       notifyIndexer = true;
/* 1060 */       releaseState = "N";
/* 1061 */       isPotentialMassChange = true;
/*      */     }
/* 1063 */     if (isDocReleased)
/*      */     {
/* 1065 */       if (unreleaseDoc)
/*      */       {
/* 1067 */         if ("YUI".indexOf(releaseState) >= 0)
/*      */         {
/* 1070 */           releaseState = "D";
/* 1071 */           notifyIndexer = true;
/*      */         }
/*      */         else
/*      */         {
/* 1075 */           releaseState = "E";
/*      */         }
/* 1077 */         isPotentialMassChange = true;
/*      */       }
/* 1081 */       else if (("YI".indexOf(releaseState) >= 0) && (((isPotentialMassChange) || (docConverted))))
/*      */       {
/* 1083 */         releaseState = "U";
/* 1084 */         notifyIndexer = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1090 */     boolean useWorkstepStatus = false;
/* 1091 */     if (status.equals("GENWWW"))
/*      */     {
/* 1094 */       if (docConverted)
/*      */       {
/* 1096 */         if ((isDocReleased) || (releaseDoc))
/*      */         {
/* 1099 */           status = "DONE";
/*      */         }
/* 1101 */         else if (isWorkflowDoc)
/*      */         {
/* 1103 */           useWorkstepStatus = true;
/*      */         }
/*      */       }
/*      */     }
/* 1107 */     else if ((status.equals("EDIT")) || (status.equals("REVIEW")) || (status.equals("DONE")))
/*      */     {
/* 1109 */       if ((isDocReleased) || (releaseDoc))
/*      */       {
/* 1111 */         status = "DONE";
/* 1112 */         notifyIndexer = true;
/*      */       }
/*      */       else
/*      */       {
/* 1116 */         useWorkstepStatus = true;
/*      */       }
/*      */     }
/*      */ 
/* 1120 */     String workstepStatus = "";
/* 1121 */     if ((isWorkflowDoc) && (!this.m_isFinishedWorkflow))
/*      */     {
/* 1123 */       if ((this.m_isMoveToPending) || (isPendingDoc))
/*      */       {
/* 1125 */         workstepStatus = "PENDING";
/*      */       }
/*      */       else
/*      */       {
/* 1129 */         workstepStatus = this.m_stepData.getRequiredDocState();
/*      */       }
/*      */     }
/* 1132 */     if ((useWorkstepStatus) && (workstepStatus.length() > 0))
/*      */     {
/* 1134 */       status = workstepStatus;
/*      */     }
/* 1136 */     if (this.m_isMoveToPending)
/*      */     {
/* 1138 */       workflowState = "P";
/*      */     }
/* 1140 */     binder.putLocal("dStatus", status);
/* 1141 */     binder.putLocal("dReleaseState", releaseState);
/* 1142 */     binder.putLocal("dWorkflowState", workflowState);
/*      */ 
/* 1144 */     boolean isMassChange = (((isWorkflowDoc) || (isPublishWorkflow))) && (isPotentialMassChange);
/*      */ 
/* 1146 */     if ((isMassChange) && (((this.m_isCriteria) || (!this.m_isFinishedWorkflow))))
/*      */     {
/* 1148 */       updateAllWorkflowRevisions(binder, workflowState, workstepStatus, false, releaseDoc, unreleaseDoc);
/*      */     }
/* 1150 */     if (notifyIndexer)
/*      */     {
/* 1153 */       binder.putLocal("notifyIndexer", "1");
/*      */     }
/* 1155 */     if ((prevWorkflowState == null) && (prevReleaseState == null) && (prevStatus == null))
/*      */     {
/* 1157 */       this.m_isStatusChange = true;
/*      */     }
/* 1159 */     if (this.m_isStatusChange)
/*      */       return;
/* 1161 */     this.m_isStatusChange = ((!status.equals(prevStatus)) || (!releaseState.equals(prevReleaseState)) || (!workflowState.equals(prevWorkflowState)));
/*      */   }
/*      */ 
/*      */   protected void updateAllWorkflowRevisions(DataBinder binder, String newWorkflowState, String workstepStatus, boolean allRevisions, boolean releaseDoc, boolean unreleaseDoc)
/*      */     throws DataException
/*      */   {
/* 1170 */     ResultSet rset = this.m_workspace.createResultSet("QwfRevisionsInWorkflow", binder);
/* 1171 */     DataBinder updateBinder = new DataBinder();
/* 1172 */     DataResultSet drset = new DataResultSet();
/* 1173 */     drset.copy(rset);
/*      */ 
/* 1175 */     updateBinder.addResultSet("WfRevisions", drset);
/*      */ 
/* 1177 */     if (allRevisions)
/*      */     {
/* 1182 */       updateBinder.putLocal("dAction", "Exit");
/* 1183 */       String wfName = null;
/* 1184 */       String stepName = null;
/* 1185 */       if (this.m_wfHelper == null)
/*      */       {
/* 1187 */         wfName = this.m_binder.getLocal("dWfName");
/* 1188 */         stepName = this.m_binder.getLocal("dWfStepName");
/*      */       }
/*      */       else
/*      */       {
/* 1192 */         wfName = this.m_wfHelper.m_wfName;
/* 1193 */         stepName = this.m_wfHelper.m_stepName;
/*      */       }
/*      */ 
/* 1196 */       updateBinder.putLocal("dWfName", wfName);
/* 1197 */       updateBinder.putLocal("dWfStepName", stepName);
/*      */ 
/* 1202 */       updateBinder.putLocal("dUser", this.m_binder.get("dUser"));
/* 1203 */       String clbraName = this.m_binder.getAllowMissing("dClbraName");
/* 1204 */       if (clbraName == null)
/*      */       {
/* 1208 */         clbraName = "";
/*      */       }
/* 1210 */       updateBinder.putLocal("dClbraName", clbraName);
/*      */     }
/*      */ 
/* 1213 */     boolean notifyIndexer = false;
/* 1214 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1216 */       String curID = ResultSetUtils.getValue(drset, "dID");
/* 1217 */       if ((releaseDoc) && (newWorkflowState.length() == 0))
/*      */       {
/* 1219 */         String publishState = ResultSetUtils.getValue(drset, "dPublishState");
/* 1220 */         if (publishState.equals("W"))
/*      */         {
/* 1222 */           updateBinder.putLocal("dID", curID);
/* 1223 */           updateBinder.putLocal("dPublishState", "S");
/* 1224 */           this.m_workspace.execute("UrevisionPublishState", updateBinder);
/*      */         }
/*      */       }
/*      */ 
/* 1228 */       if ((!allRevisions) && (curID.equals(binder.get("dID"))))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1233 */       String curWorkflowState = ResultSetUtils.getValue(drset, "dWorkflowState");
/* 1234 */       String curStatus = ResultSetUtils.getValue(drset, "dStatus");
/* 1235 */       String curReleaseState = ResultSetUtils.getValue(drset, "dReleaseState");
/* 1236 */       String status = curStatus;
/* 1237 */       String releaseState = curReleaseState;
/*      */ 
/* 1239 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1241 */         Report.debug("workflow", "updateAllWorkflowRevisions: dID=" + curID + " dWorkflowState=" + curWorkflowState + " dStatus=" + curStatus + " dReleaseState=" + curReleaseState, null);
/*      */       }
/*      */ 
/* 1245 */       boolean isReleased = !curReleaseState.equals("E");
/* 1246 */       if (newWorkflowState.equals(""))
/*      */       {
/* 1248 */         releaseDoc = true;
/*      */       }
/* 1250 */       if (isReleased)
/*      */       {
/* 1252 */         if (unreleaseDoc)
/*      */         {
/* 1254 */           if ("YUI".indexOf(releaseState) >= 0)
/*      */           {
/* 1257 */             releaseState = "D";
/* 1258 */             notifyIndexer = true;
/*      */           }
/*      */           else
/*      */           {
/* 1262 */             releaseState = "E";
/*      */           }
/*      */ 
/*      */         }
/* 1267 */         else if ("YI".indexOf(releaseState) >= 0)
/*      */         {
/* 1269 */           releaseState = "U";
/* 1270 */           notifyIndexer = true;
/*      */         }
/*      */       }
/*      */ 
/* 1274 */       if (releaseDoc)
/*      */       {
/* 1276 */         if (!isReleased)
/*      */         {
/* 1278 */           releaseState = "N";
/*      */         }
/*      */ 
/* 1282 */         if ((curStatus.equals("REVIEW")) || (curStatus.equals("EDIT")) || (curStatus.equals("PENDING")))
/*      */         {
/* 1284 */           status = "DONE";
/* 1285 */           notifyIndexer = true;
/*      */         }
/*      */       }
/* 1288 */       if ((!isReleased) && (!releaseDoc) && 
/* 1290 */         (!curStatus.equals("GENWWW")))
/*      */       {
/* 1292 */         status = workstepStatus;
/*      */       }
/*      */ 
/* 1297 */       updateBinder.putLocal("dWorkflowState", newWorkflowState);
/* 1298 */       updateBinder.putLocal("dStatus", status);
/* 1299 */       updateBinder.putLocal("dReleaseState", releaseState);
/* 1300 */       updateBinder.putLocal("dID", curID);
/* 1301 */       this.m_workspace.execute("UrevisionWorkflowState", updateBinder);
/*      */ 
/* 1303 */       if (allRevisions)
/*      */       {
/* 1306 */         DataUtils.computeActionDates(updateBinder, 0L);
/* 1307 */         this.m_workspace.execute("IworkflowDocHistory", updateBinder);
/*      */       }
/*      */ 
/* 1310 */       if (!SystemUtils.m_verbose)
/*      */         continue;
/* 1312 */       Report.debug("workflow", "updateAllWorkflowRevisions: new dWorkflowState=" + newWorkflowState + " dStatus=" + status + " dReleaseState=" + releaseState, null);
/*      */     }
/*      */ 
/* 1317 */     if (!notifyIndexer)
/*      */       return;
/* 1319 */     this.m_binder.putLocal("notifyIndexer", "1");
/*      */   }
/*      */ 
/*      */   protected boolean moveToNextStep()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1333 */     exchangeCompanionData(false);
/*      */ 
/* 1395 */     ResultSet rset = this.m_workspace.createResultSet("QdocumentPrimary", this.m_binder);
/* 1396 */     if (rset.isRowPresent())
/*      */     {
/* 1398 */       this.m_binder.putLocal("dOriginalName", ResultSetUtils.getValue(rset, "dOriginalName"));
/* 1399 */       this.m_binder.putLocal("dExtension", ResultSetUtils.getValue(rset, "dExtension"));
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1404 */       String wfAction = this.m_binder.getLocal("wfAction");
/* 1405 */       boolean isReject = (wfAction != null) && (wfAction.equals("REJECT"));
/*      */ 
/* 1407 */       boolean isCheckinStepSignature = StringUtils.convertToBool(this.m_binder.getLocal("isCheckinStepSignature"), false);
/* 1408 */       if (isReject)
/*      */       {
/* 1411 */         this.m_wfHelper.m_isUpdateState = true;
/* 1412 */         evaluateTypedScript("entry");
/* 1413 */         this.m_binder.putLocal("isFinished", "0");
/*      */       }
/* 1415 */       else if (isCheckinStepSignature)
/*      */       {
/* 1418 */         evaluateTypedScript("exit");
/*      */       }
/*      */       else
/*      */       {
/* 1423 */         evaluateTypedScript("update");
/*      */       }
/*      */ 
/* 1426 */       if (!this.m_wfHelper.m_isExit)
/*      */       {
/* 1428 */         continueMoveToStep();
/*      */       }
/*      */ 
/* 1431 */       cleanUp();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1436 */       Vector mailQueue = new IdcVector();
/* 1437 */       this.m_context.setCachedObject("MailQueue", mailQueue);
/*      */ 
/* 1442 */       retrieveCompanionData();
/* 1443 */       String errMsg = LocaleUtils.encodeMessage("csWfStepExecutionError", null, this.m_wfHelper.m_stepName, this.m_wfHelper.m_wfName, e.getMessage());
/*      */ 
/* 1445 */       this.m_wfCompanionData.m_data.putLocal("wfLastErrorMsg", errMsg);
/*      */ 
/* 1447 */       writeCompanionFile();
/*      */ 
/* 1449 */       if (e instanceof DataException)
/*      */       {
/* 1451 */         throw ((DataException)e);
/*      */       }
/*      */ 
/* 1454 */       ServiceException sexpt = null;
/* 1455 */       if (e instanceof ServiceException)
/*      */       {
/* 1457 */         sexpt = (ServiceException)e;
/*      */       }
/*      */       else
/*      */       {
/* 1461 */         sexpt = new ServiceException(e);
/*      */       }
/*      */ 
/* 1464 */       throw sexpt;
/*      */     }
/* 1466 */     return this.m_wfHelper.m_isExit;
/*      */   }
/*      */ 
/*      */   protected boolean continueMoveToStep() throws DataException, ServiceException
/*      */   {
/* 1471 */     boolean canMove = false;
/*      */     do { if ((canMove = isCurrentStepComplete()) != true)
/*      */         break;
/* 1474 */       if (!this.m_wfHelper.m_isUpdateState)
/*      */       {
/* 1477 */         this.m_workspace.execute("DworkflowDocState", this.m_binder);
/* 1478 */         this.m_wfHelper.m_isUpdateState = true;
/*      */       }
/*      */ 
/* 1481 */       evaluateTypedScript("exit"); }
/* 1482 */     while (!this.m_wfHelper.m_isExit);
/*      */ 
/* 1488 */     if ((canMove) && (!this.m_wfHelper.m_isExit))
/*      */     {
/* 1492 */       this.m_scriptResultData = new DataBinder();
/* 1493 */       this.m_scriptResultData.putLocal("isWfExit", "1");
/* 1494 */       if (!determineExit())
/*      */       {
/* 1496 */         canMove = continueMoveToStep();
/*      */       }
/*      */     }
/*      */ 
/* 1500 */     return canMove;
/*      */   }
/*      */ 
/*      */   protected void evaluateTypedScript(String type) throws DataException, ServiceException
/*      */   {
/* 1505 */     boolean isNewStep = evaluateScript(type);
/*      */ 
/* 1508 */     boolean isCheckinStepSignature = StringUtils.convertToBool(this.m_binder.getLocal("isCheckinStepSignature"), false);
/* 1509 */     if ((type.equals("exit")) && (isCheckinStepSignature))
/*      */     {
/* 1511 */       this.m_scriptResultData = new DataBinder();
/* 1512 */       this.m_scriptResultData.putLocal("isWfExit", "1");
/* 1513 */       this.m_scriptResultData.putLocal("wfExitStep", this.m_wfHelper.m_stepName + "@" + this.m_wfHelper.m_wfName);
/*      */     }
/*      */ 
/* 1516 */     this.m_wfHelper.m_isNewStep = false;
/* 1517 */     if (determineExit())
/*      */     {
/* 1519 */       this.m_wfHelper.m_isExit = true;
/* 1520 */       return;
/*      */     }
/* 1522 */     if ((type.equals("entry")) && (!isNewStep))
/*      */     {
/* 1524 */       return;
/*      */     }
/*      */ 
/* 1527 */     if ((type.equals("exit")) && 
/* 1529 */       (!isNewStep) && (!this.m_wfHelper.m_isNewStep))
/*      */     {
/* 1531 */       if (this.m_stepData.isLastRow())
/*      */       {
/* 1533 */         this.m_scriptResultData = new DataBinder();
/* 1534 */         this.m_scriptResultData.putLocal("isWfExit", "1");
/* 1535 */         if (determineExit())
/*      */         {
/* 1537 */           this.m_wfHelper.m_isExit = true;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1543 */         this.m_stepData.next();
/* 1544 */         changeToStepAndWorkflow(null, this.m_stepData.getStepName());
/* 1545 */         updateCurrentStepInfo(true, false, false);
/* 1546 */         isNewStep = true;
/*      */       }
/*      */     }
/*      */     do
/*      */     {
/* 1551 */       if ((!isNewStep) && (!this.m_wfHelper.m_isNewStep))
/*      */         return;
/* 1553 */       if (!this.m_wfHelper.m_isUpdateState)
/*      */       {
/* 1555 */         this.m_workspace.execute("DworkflowDocState", this.m_binder);
/* 1556 */         this.m_wfHelper.m_isUpdateState = true;
/*      */       }
/*      */ 
/* 1559 */       isNewStep = evaluateScript("entry");
/* 1560 */       this.m_wfHelper.m_isNewStep = false;
/* 1561 */     }while (!determineExit());
/*      */ 
/* 1563 */     this.m_wfHelper.m_isExit = true;
/*      */   }
/*      */ 
/*      */   protected boolean determineExit()
/*      */     throws DataException
/*      */   {
/* 1571 */     boolean isExit = false;
/* 1572 */     if (this.m_scriptResultData != null)
/*      */     {
/* 1574 */       isExit = StringUtils.convertToBool(this.m_scriptResultData.getLocal("isWfExit"), false);
/*      */     }
/*      */ 
/* 1577 */     if (!isExit)
/*      */     {
/* 1579 */       return isExit;
/*      */     }
/*      */ 
/* 1582 */     Report.trace("workflow", "Current workflow of sub workflow is exiting, determining exit step", null);
/*      */ 
/* 1584 */     String exitStepStr = this.m_scriptResultData.getLocal("wfExitStep");
/* 1585 */     String[] returnStep = null;
/* 1586 */     if (exitStepStr != null)
/*      */     {
/* 1588 */       returnStep = WorkflowScriptUtils.parseTarget(exitStepStr);
/* 1589 */       this.m_scriptResultData.removeLocal("wfExitStep");
/*      */     }
/*      */     else
/*      */     {
/* 1594 */       int num = this.m_parents.size();
/* 1595 */       for (int i = 0; i < num; ++i)
/*      */       {
/* 1597 */         String str = (String)this.m_parents.elementAt(0);
/*      */ 
/* 1600 */         this.m_parents.removeElementAt(0);
/*      */ 
/* 1603 */         if (str.charAt(0) != '*') {
/*      */           continue;
/*      */         }
/* 1606 */         str = str.substring(1);
/*      */ 
/* 1610 */         String rtStr = this.m_wfCompanionData.m_data.getLocal(str + ":returnStep");
/* 1611 */         if ((rtStr == null) || (rtStr.length() <= 0))
/*      */           continue;
/* 1613 */         returnStep = WorkflowScriptUtils.parseTarget(rtStr);
/* 1614 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1624 */     if (returnStep == null)
/*      */     {
/* 1628 */       if (Report.m_verbose)
/*      */       {
/* 1630 */         Report.trace("workflow", "Going to last row as place holder for exit", null);
/*      */       }
/* 1632 */       int numRows = this.m_stepData.getNumRows();
/* 1633 */       this.m_stepData.setCurrentRow(numRows - 1);
/* 1634 */       updateCurrentStepInfo(false, false, false);
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 1646 */         changeToStepAndWorkflow(returnStep[1], returnStep[0]);
/* 1647 */         updateCurrentStepInfo(true, false, false);
/* 1648 */         this.m_wfHelper.m_isNewStep = true;
/* 1649 */         isExit = false;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1654 */         determineExit();
/*      */       }
/*      */     }
/*      */ 
/* 1658 */     if (this.m_scriptResultData != null)
/*      */     {
/* 1660 */       this.m_scriptResultData.removeLocal("isWfExit");
/*      */     }
/* 1662 */     return isExit;
/*      */   }
/*      */ 
/*      */   protected void cleanUp()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1670 */     if (this.m_wfHelper.m_isUpdateState)
/*      */     {
/* 1675 */       updateToStep();
/*      */     }
/*      */ 
/* 1678 */     updateUserWorkflowTopic();
/* 1679 */     updateHistory(true);
/* 1680 */     if (this.m_workMailInfo != null)
/*      */     {
/* 1682 */       String action = this.m_binder.getLocal("wfAction");
/* 1683 */       if ((action != null) && (action.equals("START_WORKFLOW")))
/*      */       {
/* 1687 */         this.m_context.setCachedObject("WorkMailInfo", this.m_workMailInfo);
/*      */       }
/*      */       else
/*      */       {
/* 1691 */         addMailToQueue(this.m_workUsers, this.m_workMailInfo);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1696 */     writeCompanionFile();
/*      */ 
/* 1700 */     sendMail();
/*      */   }
/*      */ 
/*      */   protected void updateUserWorkflowTopic()
/*      */   {
/* 1706 */     String docName = this.m_binder.getLocal("dDocName");
/*      */ 
/* 1708 */     String wfAction = this.m_binder.getLocal("wfAction");
/* 1709 */     boolean isReject = (wfAction != null) && (wfAction.equals("REJECT"));
/* 1710 */     boolean isWfStart = (wfAction != null) && (wfAction.equals("START_WORKFLOW"));
/*      */ 
/* 1713 */     DataBinder binder = new DataBinder();
/* 1714 */     binder.putLocal("WorkflowInQueue", docName);
/*      */ 
/* 1716 */     String currentTs = LocaleUtils.formatODBC(new Date());
/* 1717 */     binder.putLocal("wfQueueLastActionTs", currentTs);
/*      */ 
/* 1719 */     if ((!this.m_wfHelper.m_isUpdateState) && (!isReject) && (!isWfStart))
/*      */       return;
/* 1721 */     this.m_wfHelper.m_isUpdateState = true;
/*      */ 
/* 1724 */     if (!isWfStart)
/*      */     {
/* 1727 */       WorkflowUtils.prepareRemoveFromInQueue(docName, this.m_wfCompanionData, this.m_workspace, this.m_context, this.m_topicUserMap, this.m_topicWorkMap);
/*      */     }
/*      */ 
/* 1731 */     if ((this.m_workUsers != null) && (this.m_workUsers.size() > 0))
/*      */     {
/* 1734 */       String stepName = this.m_workMailInfo.getProperty("dWfStepName");
/* 1735 */       String wfName = this.m_workMailInfo.getProperty("dWfName");
/* 1736 */       WorkflowUtils.prepareUpdateInQueue(docName, this.m_binder, this.m_workUsers, this.m_topicUserMap, this.m_topicWorkMap, false, false);
/*      */ 
/* 1740 */       this.m_wfCompanionData.m_data.putLocal("wfUserQueue", this.m_workMailInfo.getProperty("wfUsers"));
/* 1741 */       this.m_wfCompanionData.m_data.putLocal("wfQueueStep", stepName + "@" + wfName);
/*      */ 
/* 1743 */       if (isWfStart)
/*      */       {
/* 1745 */         this.m_workMailInfo.put("wfQueueEnterTs", currentTs);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1750 */       this.m_wfCompanionData.m_data.removeLocal("wfUserQueue");
/* 1751 */       this.m_wfCompanionData.m_data.removeLocal("wfQueueStep");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateRenotifyUsers(Vector workUsers, Properties workMailInfo, Vector removedUsers, Vector allUsers)
/*      */     throws ServiceException
/*      */   {
/* 1760 */     String docName = this.m_binder.getLocal("dDocName");
/* 1761 */     Report.trace("workflow", "updateRenotifyUsers: users=" + workUsers, null);
/* 1762 */     String wfAction = this.m_binder.getLocal("wfAction");
/* 1763 */     boolean updateAll = true;
/*      */ 
/* 1766 */     if ((wfAction == null) || (wfAction.equals("STEP_EDIT")) || (wfAction.equals("CONVERSION")) || (wfAction.equals("TIMED_UPDATE")) || (wfAction.equals("RESUBMIT")))
/*      */     {
/* 1770 */       updateAll = false;
/*      */     }
/*      */ 
/* 1774 */     Vector newUsers = workUsers;
/* 1775 */     Vector curUpdateUsers = null;
/* 1776 */     Vector updateUsers = null;
/* 1777 */     Vector updatedAllUsers = allUsers;
/* 1778 */     if (updateAll)
/*      */     {
/* 1780 */       UserData userData = null;
/* 1781 */       if (this.m_context instanceof Service)
/*      */       {
/* 1783 */         Service service = (Service)this.m_context;
/* 1784 */         userData = service.getUserData();
/*      */       }
/*      */       else
/*      */       {
/* 1788 */         userData = (UserData)this.m_context.getCachedObject("UserData");
/*      */       }
/* 1790 */       int wSize = 0;
/* 1791 */       if (workUsers != null)
/*      */       {
/* 1793 */         wSize = workUsers.size();
/*      */       }
/*      */ 
/* 1796 */       UserData[] workUsersData = new UserData[wSize];
/* 1797 */       if (workUsers != null)
/*      */       {
/* 1799 */         workUsers.copyInto(workUsersData);
/*      */       }
/*      */ 
/* 1802 */       Vector signedOffUsers = null;
/*      */       try
/*      */       {
/* 1805 */         signedOffUsers = getSignedOffUsers();
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1809 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/* 1812 */       updatedAllUsers = new IdcVector();
/* 1813 */       int numSigned = signedOffUsers.size();
/* 1814 */       for (int i = 0; i < allUsers.size(); ++i)
/*      */       {
/* 1816 */         UserData uiAll = (UserData)allUsers.elementAt(i);
/* 1817 */         boolean foundIt = false;
/* 1818 */         for (int j = 0; j < workUsers.size(); ++j)
/*      */         {
/* 1820 */           UserData uiWork = workUsersData[j];
/* 1821 */           if (!uiWork.m_name.equals(uiAll.m_name))
/*      */             continue;
/* 1823 */           foundIt = true;
/* 1824 */           updatedAllUsers.addElement(uiAll);
/* 1825 */           break;
/*      */         }
/*      */ 
/* 1828 */         if (foundIt) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1832 */         boolean isSignedOff = false;
/* 1833 */         String curName = uiAll.m_name;
/* 1834 */         for (int k = 0; k < numSigned; ++k)
/*      */         {
/* 1836 */           String name = (String)signedOffUsers.elementAt(k);
/* 1837 */           if (!curName.equals(name))
/*      */             continue;
/* 1839 */           isSignedOff = true;
/* 1840 */           break;
/*      */         }
/*      */ 
/* 1844 */         if (isSignedOff)
/*      */         {
/* 1849 */           removedUsers.addElement(uiAll.m_name);
/*      */         }
/*      */         else
/*      */         {
/* 1853 */           updatedAllUsers.addElement(uiAll);
/* 1854 */           if (curName.equals(userData.m_name))
/*      */           {
/* 1857 */             curUpdateUsers = new IdcVector();
/* 1858 */             curUpdateUsers.addElement(uiAll);
/*      */           }
/*      */           else
/*      */           {
/* 1862 */             if (updateUsers == null)
/*      */             {
/* 1864 */               updateUsers = new IdcVector();
/*      */             }
/* 1866 */             updateUsers.addElement(uiAll);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1874 */     WorkflowUtils.prepareRemoveFromInQueueEx(docName, removedUsers, this.m_workspace, this.m_context, this.m_topicUserMap, this.m_topicWorkMap);
/*      */ 
/* 1878 */     if (newUsers != null)
/*      */     {
/* 1880 */       WorkflowUtils.prepareUpdateInQueue(docName, this.m_binder, newUsers, this.m_topicUserMap, this.m_topicWorkMap, false, false);
/*      */ 
/* 1883 */       if (workMailInfo != null)
/*      */       {
/* 1891 */         boolean globalInform = SharedObjects.getEnvValueAsBoolean("WorkflowRenotification", true);
/* 1892 */         boolean isInform = SharedObjects.getEnvValueAsBoolean(this.m_wfHelper.m_stepName + ":WorkflowRenotification", globalInform);
/*      */ 
/* 1894 */         if (isInform)
/*      */         {
/* 1897 */           workMailInfo.put("IsInformUsers", "1");
/* 1898 */           addMailToQueue(newUsers, workMailInfo);
/*      */         }
/*      */       }
/*      */     }
/* 1902 */     if (updateUsers != null)
/*      */     {
/* 1904 */       WorkflowUtils.prepareUpdateInQueue(docName, this.m_binder, updateUsers, this.m_topicUserMap, this.m_topicWorkMap, true, false);
/*      */     }
/*      */ 
/* 1907 */     if (curUpdateUsers != null)
/*      */     {
/* 1909 */       this.m_binder.putLocal("wfQueueActionState", wfAction);
/* 1910 */       WorkflowUtils.prepareUpdateInQueue(docName, this.m_binder, curUpdateUsers, this.m_topicUserMap, this.m_topicWorkMap, true, true);
/*      */     }
/*      */ 
/* 1915 */     Vector v = new IdcVector();
/* 1916 */     int num = updatedAllUsers.size();
/* 1917 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1919 */       UserData userData = (UserData)updatedAllUsers.elementAt(i);
/* 1920 */       v.addElement(userData.m_name);
/*      */     }
/*      */ 
/* 1923 */     String userStr = StringUtils.createString(v, ',', '^');
/* 1924 */     this.m_wfCompanionData.m_data.putLocal("wfUserQueue", userStr);
/*      */ 
/* 1927 */     updateHistory(true);
/*      */ 
/* 1930 */     writeCompanionFile();
/*      */ 
/* 1932 */     sendMail();
/*      */   }
/*      */ 
/*      */   protected void updateHistory(boolean isMail)
/*      */   {
/* 1940 */     DataResultSet drset = WorkflowUtils.getOrCreateWorkflowActionHistory(this.m_wfCompanionData);
/* 1941 */     DataBinder workBinder = WorkflowUtils.createWorkflowActionHistoryBinder(drset, this.m_binder, this.m_context);
/*      */ 
/* 1943 */     String action = null;
/* 1944 */     if (isMail)
/*      */     {
/* 1946 */       int num = this.m_mailQueue.size();
/* 1947 */       if (num > this.m_historyMailStartCount)
/*      */       {
/* 1949 */         action = "MAIL_NOTIFICATION";
/* 1950 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 1952 */           Properties props = (Properties)this.m_mailQueue.get(i);
/* 1953 */           CollectionUtils.mergeMaps(props, workBinder.getLocalData(), WorkflowUtils.WORKFLOWACTIONHISTORY_COLUMNS);
/* 1954 */           workBinder.putLocal("wfAction", action);
/* 1955 */           WorkflowUtils.updateWorkflowActionHistory(drset, workBinder);
/*      */         }
/* 1957 */         this.m_historyMailStartCount = num;
/*      */       }
/*      */ 
/* 1960 */       if (this.m_workMailInfo != null)
/*      */       {
/* 1962 */         action = "WORK_NOTIFICATION";
/* 1963 */         CollectionUtils.mergeMaps(this.m_workMailInfo, workBinder.getLocalData(), WorkflowUtils.WORKFLOWACTIONHISTORY_COLUMNS);
/* 1964 */         workBinder.putLocal("wfAction", action);
/* 1965 */         WorkflowUtils.updateWorkflowActionHistory(drset, workBinder);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1970 */       action = this.m_binder.getLocal("wfAction");
/* 1971 */       if ((action == null) || (action.equals("TIMED_UPDATE")) || (action.equals("CONVERSION")) || (action.equals("RESUBMIT")) || (action.equals("REJECT"))) {
/*      */         return;
/*      */       }
/* 1974 */       WorkflowUtils.updateWorkflowActionHistory(drset, workBinder);
/* 1975 */       boolean isApproved = false;
/* 1976 */       if (action.equals("CHECKIN"))
/*      */       {
/* 1978 */         isApproved = StringUtils.convertToBool(this.m_binder.getLocal("WfEditFinished"), false);
/*      */       }
/* 1982 */       else if (this.m_context instanceof Service)
/*      */       {
/* 1984 */         Service s = (Service)this.m_context;
/* 1985 */         String key = "DidWorkflowApproval";
/* 1986 */         isApproved = s.isConditionVarTrue(key);
/* 1987 */         if (isApproved)
/*      */         {
/* 1990 */           s.setConditionVar(key, false);
/*      */         }
/*      */       }
/*      */ 
/* 1994 */       if (!isApproved) {
/*      */         return;
/*      */       }
/* 1997 */       workBinder.putLocal("wfAction", "APPROVE");
/* 1998 */       WorkflowUtils.updateWorkflowActionHistory(drset, workBinder);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean isCurrentStepComplete()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2010 */     boolean isFinished = StringUtils.convertToBool(this.m_binder.getLocal("isFinished"), false);
/* 2011 */     this.m_binder.removeLocal("isFinished");
/*      */ 
/* 2013 */     String stepType = this.m_stepData.getStepType();
/* 2014 */     if (!WorkflowScriptUtils.isReviewerStep(stepType))
/*      */     {
/* 2019 */       String wfAction = this.m_binder.getLocal("wfAction");
/* 2020 */       if ((wfAction != null) && (((wfAction.equals("CONVERSION")) || (wfAction.equals("RESUBMIT")))))
/*      */       {
/* 2022 */         isFinished = false;
/*      */       }
/*      */ 
/* 2027 */       return isFinished;
/*      */     }
/*      */ 
/* 2031 */     boolean useAdditionalCondition = StringUtils.convertToBool(this.m_wfDesign.m_designData.getLocal(this.m_wfHelper.m_stepName + ":HasAdditionalExitCondition"), false);
/*      */ 
/* 2033 */     if (useAdditionalCondition)
/*      */     {
/* 2035 */       boolean isConditionMet = evaluateCondition("wfAdditionalExitCondition");
/* 2036 */       if (!isConditionMet)
/*      */       {
/* 2038 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 2042 */     boolean isAll = this.m_stepData.getIsAll();
/* 2043 */     int weight = this.m_stepData.getWeight();
/* 2044 */     Vector stepUsers = WorkflowUtils.computeStepUsers(this.m_workspace, this.m_binder, false, this.m_context, false, this.m_useLocal);
/*      */ 
/* 2047 */     Vector signedOffUsers = getSignedOffUsers();
/* 2048 */     int numSignedOff = signedOffUsers.size();
/* 2049 */     int numUsers = stepUsers.size();
/*      */ 
/* 2051 */     boolean isComplete = false;
/* 2052 */     if (((!isAll) && (numSignedOff >= weight)) || ((isAll) && (numUsers <= numSignedOff)) || (numUsers == 0))
/*      */     {
/* 2060 */       isComplete = true;
/* 2061 */       this.m_binder.putLocal("isWfCurrentStepComplete", "1");
/*      */     }
/* 2063 */     else if ((!isAll) && (numUsers < weight))
/*      */     {
/* 2065 */       Object[] args = new Object[4];
/* 2066 */       args[0] = this.m_wfHelper.m_stepName;
/* 2067 */       args[1] = this.m_wfHelper.m_wfName;
/* 2068 */       args[2] = ("" + numUsers);
/* 2069 */       args[3] = ("" + weight);
/* 2070 */       String errMsg = LocaleUtils.encodeMessage("csWfStepIncompleteError", null, args);
/*      */ 
/* 2072 */       reportLastError(null, errMsg);
/* 2073 */       this.m_binder.putLocal("isWfCurrentStepComplete", "0");
/*      */     }
/* 2075 */     return isComplete;
/*      */   }
/*      */ 
/*      */   protected boolean isWorkflowFinished(boolean isSpecificDoc) throws DataException
/*      */   {
/* 2080 */     if ((this.m_stepData != null) && (!this.m_stepData.isLastRow()))
/*      */     {
/* 2082 */       return false;
/*      */     }
/*      */ 
/* 2085 */     if (this.m_isCriteria)
/*      */     {
/* 2088 */       return true;
/*      */     }
/*      */ 
/* 2092 */     if (this.m_suppressBasicWorkflowFinishTest)
/*      */     {
/* 2094 */       return false;
/*      */     }
/*      */ 
/* 2097 */     DataResultSet dset = getBasicWorkflowDocumentList();
/* 2098 */     if (dset == null)
/*      */     {
/* 2101 */       return false;
/*      */     }
/*      */ 
/* 2104 */     boolean areAllDocsDone = true;
/*      */ 
/* 2107 */     String curDoc = null;
/* 2108 */     if (isSpecificDoc)
/*      */     {
/* 2110 */       curDoc = this.m_binder.get("dDocName");
/*      */     }
/* 2112 */     String docName = null;
/*      */ 
/* 2114 */     String[] keys = { "dWorkflowState", "dDocName" };
/* 2115 */     String[][] valuesTable = ResultSetUtils.createStringTable(dset, keys);
/*      */ 
/* 2118 */     for (int i = 0; i < valuesTable.length; ++i)
/*      */     {
/* 2120 */       String workflowState = valuesTable[i][0];
/* 2121 */       if (isSpecificDoc)
/*      */       {
/* 2123 */         docName = valuesTable[i][1];
/*      */       }
/* 2125 */       if ((workflowState.equals("P")) || ((isSpecificDoc) && (docName.equalsIgnoreCase(curDoc)))) {
/*      */         continue;
/*      */       }
/* 2128 */       areAllDocsDone = false;
/* 2129 */       break;
/*      */     }
/*      */ 
/* 2132 */     return areAllDocsDone;
/*      */   }
/*      */ 
/*      */   public void checkIsFinishedBasicWorkflow()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2140 */     this.m_isInWorkflow = true;
/* 2141 */     this.m_hasWorkflowId = true;
/* 2142 */     computeWorkflowInfo(false);
/*      */ 
/* 2144 */     if (this.m_isCriteria)
/*      */     {
/* 2146 */       String msg = LocaleUtils.encodeMessage("csBasicWorkflowOfWrongType", null, this.m_binder.getAllowMissing("dWfName"));
/* 2147 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 2150 */     if (!isWorkflowFinished(false))
/*      */       return;
/* 2152 */     markWorkflowFinished();
/*      */   }
/*      */ 
/*      */   public Vector getSignedOffUsers()
/*      */     throws DataException
/*      */   {
/* 2161 */     Vector signedOffUsers = new IdcVector();
/* 2162 */     String stepType = this.m_stepData.getStepType();
/* 2163 */     if (!WorkflowScriptUtils.isAutoContributorStep(stepType))
/*      */     {
/* 2172 */       ResultSet signedOffSet = this.m_workspace.createResultSet("QwfDocState", this.m_binder);
/* 2173 */       while ((signedOffSet != null) && (!signedOffSet.isEmpty()) && 
/* 2175 */         (signedOffSet.isRowPresent()))
/*      */       {
/* 2177 */         signedOffUsers.addElement(signedOffSet.getStringValue(0));
/*      */ 
/* 2175 */         signedOffSet.next();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2181 */     return signedOffUsers;
/*      */   }
/*      */ 
/*      */   public void updateToStep()
/*      */     throws DataException
/*      */   {
/* 2189 */     this.m_workspace.execute("DworkflowDocState", this.m_binder);
/* 2190 */     this.m_workspace.execute("UworkflowDocStep", this.m_binder);
/* 2191 */     this.m_workspace.execute("Uuncheckout", this.m_binder);
/* 2192 */     SubjectManager.notifyChanged("workflows");
/*      */   }
/*      */ 
/*      */   public void markWorkflowFinished()
/*      */     throws DataException, ServiceException
/*      */   {
/* 2198 */     if (PluginFilters.filter("advanceDocumentStateMarkWorkflowFinished", this.m_workspace, this.m_binder, this.m_context) != 0)
/*      */     {
/* 2201 */       return;
/*      */     }
/* 2203 */     DataBinder dataBinder = this.m_binder;
/*      */ 
/* 2205 */     DataResultSet basicWfDocs = null;
/* 2206 */     if (!this.m_isCriteria)
/*      */     {
/* 2208 */       if (!this.m_isInWorkflow)
/*      */       {
/* 2210 */         return;
/*      */       }
/*      */ 
/* 2214 */       basicWfDocs = getBasicWorkflowDocumentList();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2220 */       String wfUpdateQuery = null;
/* 2221 */       String wfDocQuery = null;
/* 2222 */       String wfStateQuery = null;
/* 2223 */       String wfAttributeQuery = null;
/* 2224 */       if (this.m_isCriteria)
/*      */       {
/* 2231 */         if (this.m_hasWorkflowId)
/*      */         {
/* 2233 */           wfDocQuery = "DworkflowDocument";
/*      */         }
/* 2235 */         wfStateQuery = "DworkflowDocState";
/* 2236 */         wfAttributeQuery = "DworkflowUserAttribute";
/*      */       }
/*      */       else
/*      */       {
/* 2244 */         wfUpdateQuery = "UworkflowStatusCompletion";
/* 2245 */         wfDocQuery = "UworkflowDocInitState";
/* 2246 */         wfStateQuery = "DworkflowStateAll";
/*      */       }
/*      */ 
/* 2249 */       boolean doWork = true;
/*      */ 
/* 2251 */       if (basicWfDocs != null)
/*      */       {
/* 2253 */         basicWfDocs.first();
/* 2254 */         doWork = basicWfDocs.isRowPresent();
/*      */       }
/*      */ 
/* 2258 */       while (doWork)
/*      */       {
/* 2260 */         if (!this.m_isCriteria)
/*      */         {
/* 2263 */           dataBinder = new DataBinder();
/* 2264 */           dataBinder.addResultSet("WfDocuments", basicWfDocs);
/*      */         }
/* 2266 */         if (wfDocQuery != null)
/*      */         {
/* 2268 */           this.m_workspace.execute(wfDocQuery, dataBinder);
/*      */         }
/* 2270 */         if (wfStateQuery != null)
/*      */         {
/* 2272 */           this.m_workspace.execute(wfStateQuery, dataBinder);
/*      */         }
/* 2274 */         this.m_workspace.execute("Uuncheckout", dataBinder);
/* 2275 */         if (wfAttributeQuery != null)
/*      */         {
/* 2277 */           this.m_workspace.execute(wfAttributeQuery, dataBinder);
/*      */         }
/*      */ 
/* 2280 */         String docName = dataBinder.get("dDocName");
/* 2281 */         String dir = dataBinder.get("dWfDirectory");
/* 2282 */         if (!this.m_isCriteria)
/*      */         {
/* 2285 */           updateAllWorkflowRevisions(dataBinder, "", null, true, true, false);
/*      */ 
/* 2288 */           this.m_wfCompanionData = WorkflowUtils.loadCompanionData(docName, dir, this.m_binder, this.m_context, this.m_workspace);
/*      */         }
/*      */ 
/* 2292 */         if (this.m_isInWorkflow)
/*      */         {
/* 2295 */           WfCompanionManager.removeCompanionFile(docName, dir, this.m_binder, !this.m_isOutOfContext);
/*      */ 
/* 2298 */           WorkflowUtils.prepareRemoveFromInQueue(docName, this.m_wfCompanionData, this.m_workspace, this.m_context, this.m_topicUserMap, this.m_topicWorkMap);
/*      */ 
/* 2301 */           if (this.m_isCriteria)
/*      */           {
/* 2304 */             dataBinder.putLocal("dAction", "Exit");
/* 2305 */             DataUtils.computeActionDates(dataBinder, 0L);
/* 2306 */             this.m_workspace.execute("IworkflowDocHistory", dataBinder);
/*      */           }
/*      */         }
/*      */ 
/* 2310 */         if (this.m_isCriteria)
/*      */         {
/* 2312 */           doWork = false;
/*      */         }
/*      */         else
/*      */         {
/* 2316 */           basicWfDocs.next();
/* 2317 */           doWork = basicWfDocs.isRowPresent();
/*      */         }
/*      */       }
/* 2320 */       if (basicWfDocs != null)
/*      */       {
/* 2322 */         basicWfDocs.first();
/*      */       }
/*      */ 
/* 2326 */       this.m_workspace.execute(wfStateQuery, dataBinder);
/*      */ 
/* 2328 */       if (wfUpdateQuery != null)
/*      */       {
/* 2331 */         Date dte = new Date();
/* 2332 */         String dteStr = LocaleUtils.formatODBC(dte);
/* 2333 */         this.m_binder.putLocal("dCompletionDate", dteStr);
/*      */ 
/* 2335 */         this.m_binder.putLocal("dWfStatus", "INIT");
/* 2336 */         this.m_workspace.execute(wfUpdateQuery, this.m_binder);
/* 2337 */         SubjectManager.forceRefresh("workflows");
/*      */       }
/*      */ 
/* 2340 */       this.m_binder.putLocal("notifyIndexer", "1");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2344 */       String wfName = "<unknown>";
/* 2345 */       String stepName = "<unknown>";
/* 2346 */       if (this.m_wfHelper != null)
/*      */       {
/* 2348 */         wfName = this.m_wfHelper.m_wfName;
/* 2349 */         stepName = this.m_wfHelper.m_stepName;
/*      */       }
/* 2351 */       DataException de = new DataException(e, "csWfUpdateCompanion", new Object[] { this.m_binder.getLocal("dDocName"), wfName, stepName });
/*      */ 
/* 2353 */       throw de;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void renotifyUsers()
/*      */   {
/* 2362 */     boolean isPublisherRenotify = StringUtils.convertToBool(this.m_binder.getLocal("IsPublisherRenotify"), false);
/* 2363 */     if ((this.m_wfHelper.m_isUpdateState) && (!isPublisherRenotify))
/*      */       return;
/* 2365 */     computeMailInfo(true, true);
/*      */   }
/*      */ 
/*      */   public void computeMailInfo(boolean isRenotify, boolean isInform)
/*      */   {
/* 2371 */     if ((this.m_suppressWorkflowNotification) || (SharedObjects.getEnvValueAsBoolean("IsSuppressWorkflowNotification", false)))
/*      */     {
/* 2373 */       Report.trace("workflow", "computeMailInfo (suppressed - skipped): step=" + this.m_wfHelper.m_stepName + " workflow=" + this.m_wfHelper.m_wfName + " isRenotify=" + isRenotify + " isInform=" + isInform, null);
/*      */ 
/* 2375 */       return;
/*      */     }
/*      */ 
/* 2378 */     Report.trace("workflow", "computeMailInfo: step=" + this.m_wfHelper.m_stepName + " workflow=" + this.m_wfHelper.m_wfName + " isRenotify=" + isRenotify + " isInform=" + isInform, null);
/*      */     try
/*      */     {
/* 2382 */       Vector workUsers = WorkflowUtils.computeStepUsers(this.m_workspace, this.m_binder, false, this.m_context, false, this.m_useLocal);
/*      */ 
/* 2385 */       Vector removedUsers = new IdcVector();
/* 2386 */       Vector allUsers = null;
/* 2387 */       if ((isRenotify) && (!this.m_wfHelper.m_isUpdateState))
/*      */       {
/* 2393 */         Vector signedOffUsers = getSignedOffUsers();
/* 2394 */         int size = signedOffUsers.size();
/* 2395 */         for (int i = 0; i < size; ++i)
/*      */         {
/* 2397 */           String name = (String)signedOffUsers.elementAt(i);
/* 2398 */           int wUsers = workUsers.size();
/* 2399 */           for (int j = 0; j < wUsers; ++j)
/*      */           {
/* 2401 */             UserData userData = (UserData)workUsers.elementAt(j);
/* 2402 */             if (!userData.m_name.equals(name))
/*      */               continue;
/* 2404 */             workUsers.removeElementAt(j);
/* 2405 */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 2412 */         allUsers = (Vector)workUsers.clone();
/*      */ 
/* 2419 */         String notifiedStr = this.m_wfCompanionData.m_data.getLocal("wfUserQueue");
/* 2420 */         Vector notifiedUsers = StringUtils.parseArray(notifiedStr, ',', '^');
/*      */ 
/* 2422 */         int num = notifiedUsers.size();
/* 2423 */         for (int i = 0; i < num; ++i)
/*      */         {
/* 2425 */           String name = (String)notifiedUsers.elementAt(i);
/* 2426 */           int wNum = workUsers.size();
/* 2427 */           if (wNum != 0)
/*      */           {
/* 2429 */             for (int j = 0; j < wNum; ++j)
/*      */             {
/* 2431 */               UserData userData = (UserData)workUsers.elementAt(j);
/* 2432 */               if (!userData.m_name.equals(name))
/*      */                 continue;
/* 2434 */               workUsers.removeElementAt(j);
/* 2435 */               break;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 2442 */           if (workUsers.size() != wNum) {
/*      */             continue;
/*      */           }
/* 2445 */           removedUsers.addElement(name);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 2451 */         allUsers = (Vector)workUsers.clone();
/*      */       }
/*      */ 
/* 2454 */       Properties workMailInfo = null;
/* 2455 */       if (workUsers.size() > 0)
/*      */       {
/* 2457 */         workMailInfo = WorkflowUtils.buildInformStepUsers(workUsers, this.m_binder, this.m_stepData, this.m_context, this.m_wfCompanionData, this.m_workflowInfo);
/*      */       }
/*      */ 
/* 2461 */       if (isRenotify)
/*      */       {
/* 2465 */         updateRenotifyUsers(workUsers, workMailInfo, removedUsers, allUsers);
/*      */       }
/* 2467 */       else if (workMailInfo == null)
/*      */       {
/* 2472 */         this.m_workUsers = null;
/* 2473 */         this.m_workMailInfo = null;
/*      */       }
/*      */       else
/*      */       {
/* 2477 */         workMailInfo.put("IsInformUsers", String.valueOf(isInform));
/*      */ 
/* 2480 */         boolean isNotifyOnly = StringUtils.convertToBool(workMailInfo.getProperty("IsNotifyOnly"), false);
/* 2481 */         if (isNotifyOnly)
/*      */         {
/* 2484 */           if (isInform)
/*      */           {
/* 2486 */             addMailToQueue(workUsers, workMailInfo);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 2494 */           this.m_workUsers = workUsers;
/* 2495 */           this.m_workMailInfo = workMailInfo;
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2501 */       String errMsg = LocaleUtils.encodeMessage("csWfInformUsersError", null, this.m_stepData.getStepName(), this.m_binder.getLocal("dWfName"));
/*      */ 
/* 2503 */       reportLastError(e, errMsg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addMailToQueue(Vector users, Properties mailInfo)
/*      */   {
/* 2509 */     boolean isStaging = StringUtils.convertToBool(this.m_binder.getLocal("isStaging"), false);
/* 2510 */     if (isStaging)
/*      */     {
/* 2514 */       String projectID = this.m_workflowInfo.m_properties.getProperty("dProjectID");
/* 2515 */       int numUsers = users.size();
/* 2516 */       for (int i = 0; i < numUsers; ++i)
/*      */       {
/* 2518 */         UserData userData = (UserData)users.elementAt(i);
/* 2519 */         Projects.addWork(projectID, userData.m_name, mailInfo);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2524 */       boolean isInform = StringUtils.convertToBool(mailInfo.getProperty("IsInformUsers"), false);
/* 2525 */       if (!isInform)
/*      */         return;
/* 2527 */       Report.trace("workflow", "addMailToQueue: users=" + users, null);
/* 2528 */       this.m_mailQueue.add(mailInfo);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void sendMail()
/*      */   {
/* 2536 */     if (!this.m_isOutOfContext) {
/*      */       return;
/*      */     }
/* 2539 */     InternetFunctions.sendMailInQueue(this.m_context);
/* 2540 */     initMailQueue(true);
/*      */   }
/*      */ 
/*      */   public boolean evaluateScript(String type)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2557 */     boolean isChanged = false;
/* 2558 */     this.m_scriptResultData = null;
/*      */ 
/* 2561 */     String wfName = this.m_wfHelper.m_wfName;
/* 2562 */     String stepName = this.m_wfHelper.m_stepName;
/* 2563 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2565 */       Report.debug("workflow", "evaluateScript(" + type + "): dDocName=" + this.m_binder.getLocal("dDocName") + " stepName=" + stepName + " wfName=" + wfName, null);
/*      */     }
/*      */ 
/* 2569 */     if (type.equals("entry"))
/*      */     {
/* 2571 */       String lookupKey = this.m_wfHelper.m_lookupKey;
/* 2572 */       lookupKey = lookupKey.toLowerCase();
/*      */ 
/* 2574 */       Object obj = this.m_previousStepMap.get(lookupKey);
/* 2575 */       if (obj == null)
/*      */       {
/* 2577 */         this.m_previousStepMap.put(lookupKey, lookupKey);
/*      */       }
/*      */       else
/*      */       {
/* 2584 */         computeMailInfo(false, true);
/* 2585 */         return isChanged;
/*      */       }
/*      */     }
/*      */ 
/* 2589 */     if ((this.m_wfDesign == null) || (!this.m_wfDesign.m_wfName.equals(wfName.toLowerCase())))
/*      */     {
/* 2591 */       this.m_wfDesign = WfDesignManager.getWorkflowDesign(wfName);
/*      */     }
/*      */ 
/* 2595 */     DataBinder designData = this.m_wfDesign.m_designData;
/* 2596 */     DataResultSet eventSet = (DataResultSet)designData.getResultSet("WorkflowStepEvents");
/*      */ 
/* 2598 */     String script = null;
/* 2599 */     if ((eventSet != null) && (!eventSet.isEmpty()))
/*      */     {
/* 2602 */       Vector row = eventSet.findRow(0, stepName);
/* 2603 */       if (row != null)
/*      */       {
/* 2605 */         String column = StringUtils.getInternalString(WorkflowScriptUtils.COLUMN_EVENT_MAP, type);
/* 2606 */         script = ResultSetUtils.getValue(eventSet, column);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2612 */     if (((script != null) && (script.length() != 0)) || (type.equals("entry")))
/*      */     {
/* 2614 */       isChanged = executeScript(type, stepName, wfName);
/*      */     }
/* 2616 */     return isChanged;
/*      */   }
/*      */ 
/*      */   protected boolean executeScript(String type, String stepName, String wfName)
/*      */     throws ServiceException
/*      */   {
/* 2622 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2624 */       Report.debug("workflow", "executeScript: dDocName=" + this.m_binder.getLocal("dDocName") + " type=" + type + " stepName=" + stepName + " wfName=" + wfName, null);
/*      */     }
/*      */ 
/* 2628 */     boolean isScriptError = false;
/* 2629 */     boolean isChanged = false;
/* 2630 */     DataBinder designData = this.m_wfDesign.m_designData;
/* 2631 */     PageMerger pageMerger = (PageMerger)this.m_context.getCachedObject("PageMerger");
/*      */ 
/* 2633 */     String nextStep = null;
/* 2634 */     String nextWf = null;
/* 2635 */     String returnStep = null;
/*      */     try
/*      */     {
/* 2640 */       String prefix = stepName + "_" + type + "_";
/* 2641 */       String scriptStr = WorkflowScriptUtils.computeScriptString(type, prefix, designData, false);
/*      */ 
/* 2643 */       this.m_scriptResultData = prepareAndEvaluateScript(scriptStr, pageMerger);
/*      */ 
/* 2646 */       String targetStep = this.m_scriptResultData.getLocal("wfJumpTargetStep");
/* 2647 */       returnStep = this.m_scriptResultData.getLocal("wfJumpReturnStep");
/*      */ 
/* 2649 */       if ((targetStep != null) && (targetStep.length() > 0))
/*      */       {
/* 2652 */         String[] target = WorkflowScriptUtils.parseTarget(targetStep);
/* 2653 */         nextStep = target[0];
/* 2654 */         nextWf = target[1];
/* 2655 */         isChanged = true;
/* 2656 */         Report.trace("workflow", "executeScript: dDocName=" + this.m_binder.getLocal("dDocName") + " new target step=" + nextStep + " workflow=" + nextWf, null);
/*      */       }
/*      */       else
/*      */       {
/* 2665 */         boolean isStartWorkflow = StringUtils.convertToBool(this.m_scriptResultData.getLocal("isWfStart"), false);
/*      */ 
/* 2667 */         if (isStartWorkflow)
/*      */         {
/* 2669 */           int currentIndex = this.m_stepData.getCurrentRow();
/*      */ 
/* 2671 */           this.m_stepData.setCurrentRow(0);
/* 2672 */           nextStep = this.m_stepData.getStepName();
/*      */ 
/* 2675 */           this.m_stepData.setCurrentRow(currentIndex);
/* 2676 */           isChanged = true;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 2684 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2686 */         Report.debug("workflow", " evaluateScript: error evaluating " + type + " script.", null);
/*      */       }
/*      */ 
/* 2692 */       String isScriptAbort = this.m_binder.getLocal("isReportToErrorPage");
/* 2693 */       if (StringUtils.convertToBool(isScriptAbort, false))
/*      */       {
/* 2695 */         ServiceException sExp = new ServiceException(e);
/* 2696 */         this.m_binder.putLocal("StatusMessageKey", sExp.getMessage());
/* 2697 */         this.m_binder.putLocal("StatusMessage", sExp.getMessage());
/* 2698 */         this.m_binder.removeLocal("isReportToErrorPage");
/*      */ 
/* 2700 */         throw sExp;
/*      */       }
/* 2702 */       isScriptError = true;
/* 2703 */       this.m_scriptResultData = new DataBinder();
/*      */ 
/* 2706 */       String errMsg = LocaleUtils.encodeMessage("csWfScriptEvalError", e.getMessage(), type, stepName, wfName);
/*      */ 
/* 2708 */       reportLastError(null, errMsg);
/*      */     }
/*      */ 
/* 2711 */     boolean isError = false;
/* 2712 */     boolean isDoMerge = false;
/* 2713 */     if ((isScriptError) && (type.equals("entry")))
/*      */     {
/*      */       try
/*      */       {
/* 2718 */         String defaultScript = WorkflowScriptUtils.createDefaultEntryScript();
/* 2719 */         pageMerger.evaluateScriptReportError(defaultScript);
/* 2720 */         isDoMerge = true;
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2724 */         String errMsg = LocaleUtils.encodeMessage("csWfDefaultEntryScriptError", null, stepName, wfName);
/*      */ 
/* 2726 */         reportLastError(e, errMsg);
/*      */       }
/*      */     }
/* 2729 */     else if (!isScriptError)
/*      */     {
/* 2732 */       boolean isTryReturnStep = false;
/*      */       try
/*      */       {
/* 2735 */         if (isChanged)
/*      */         {
/* 2737 */           changeToStepAndWorkflow(nextWf, nextStep);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 2742 */         if ((returnStep != null) && (returnStep.length() > 0))
/*      */         {
/* 2744 */           isTryReturnStep = true;
/*      */         }
/*      */         else
/*      */         {
/* 2748 */           isError = true;
/*      */         }
/* 2750 */         String errMsg = LocaleUtils.encodeMessage("csWfTargetStepError", null, type, stepName, wfName);
/* 2751 */         reportLastError(e, errMsg);
/*      */       }
/*      */ 
/* 2754 */       if (isTryReturnStep)
/*      */       {
/*      */         try
/*      */         {
/* 2759 */           String[] returnTarget = WorkflowScriptUtils.parseTarget(returnStep);
/* 2760 */           changeToStepAndWorkflow(returnTarget[1], returnTarget[0]);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 2764 */           isError = true;
/* 2765 */           String errMsg = LocaleUtils.encodeMessage("csWfReturnStepError", null, type, stepName, wfName);
/* 2766 */           reportLastError(e, errMsg);
/*      */         }
/*      */       }
/*      */ 
/* 2770 */       if (isError)
/*      */       {
/* 2774 */         this.m_scriptResultData = new DataBinder();
/* 2775 */         this.m_scriptResultData.putLocal("isError", "1");
/* 2776 */         this.m_scriptResultData.putLocal("isWfExit", "1");
/*      */       }
/* 2778 */       else if ((!isTryReturnStep) && 
/* 2784 */         (returnStep != null) && (returnStep.length() > 0))
/*      */       {
/* 2787 */         this.m_scriptResultData.putLocal(this.m_wfHelper.m_lookupKey + ":returnStep", returnStep);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2792 */     if ((!isError) || (isDoMerge))
/*      */     {
/* 2795 */       DataBinder wfData = this.m_wfCompanionData.m_data;
/*      */ 
/* 2798 */       String[] keys = { "wfJumpTargetStep", "wfJumpReturnStep", "isWfStart" };
/* 2799 */       for (int i = 0; i < keys.length; ++i)
/*      */       {
/* 2801 */         this.m_scriptResultData.removeLocal(keys[i]);
/*      */       }
/* 2803 */       wfData.merge(this.m_scriptResultData);
/*      */ 
/* 2807 */       boolean isNotify = (type.equals("entry")) && (!isChanged);
/* 2808 */       updateCurrentStepInfo(isChanged, true, isNotify);
/*      */     }
/* 2810 */     return (isError) || (isChanged);
/*      */   }
/*      */ 
/*      */   protected boolean changeToStepAndWorkflow(String wfName, String stepName)
/*      */     throws DataException
/*      */   {
/* 2816 */     Report.trace("workflow", "changeToStepAndWorkflow: step=" + stepName + " workflow = " + wfName, null);
/*      */ 
/* 2818 */     WorkflowInfo workflowInfo = getAndValidateWorkflowChange(wfName, stepName);
/*      */ 
/* 2820 */     String oldWfName = this.m_workflowInfo.m_wfName;
/* 2821 */     if ((((wfName == null) || (wfName.equals(oldWfName)))) && (stepName != null))
/*      */     {
/* 2824 */       wfName = oldWfName;
/* 2825 */       Vector row = this.m_stepData.findRow(0, stepName);
/* 2826 */       if (row == null)
/*      */       {
/* 2828 */         String errMsg = LocaleUtils.encodeMessage("csWfMissingStep", null, stepName, wfName);
/*      */ 
/* 2830 */         throw new DataException(errMsg);
/*      */       }
/* 2832 */       this.m_binder.putLocal("dWfStepName", stepName);
/*      */     }
/*      */     else
/*      */     {
/* 2836 */       DataBinder binder = new DataBinder();
/* 2837 */       binder.putLocal("dWfStepName", stepName);
/* 2838 */       binder.putLocal("dWfID", workflowInfo.m_properties.getProperty("dWfID"));
/*      */ 
/* 2840 */       ResultSet stepSet = this.m_workspace.createResultSet("QworkflowSteps", binder);
/* 2841 */       if (stepSet.isEmpty())
/*      */       {
/* 2843 */         String errMsg = LocaleUtils.encodeMessage("csWfIncorrectWorkflow", null, wfName);
/*      */ 
/* 2845 */         throw new DataException(errMsg);
/*      */       }
/*      */ 
/* 2848 */       WfStepData stepData = new WfStepData();
/* 2849 */       stepData.load(stepSet);
/*      */ 
/* 2852 */       Vector row = stepData.findRow(0, stepName);
/* 2853 */       if (row == null)
/*      */       {
/* 2855 */         String errMsg = LocaleUtils.encodeMessage("csWfMissingStep", null, stepName, wfName);
/*      */ 
/* 2857 */         throw new DataException(errMsg);
/*      */       }
/*      */ 
/* 2861 */       this.m_workflowInfo = workflowInfo;
/* 2862 */       this.m_stepData = stepData;
/*      */ 
/* 2865 */       this.m_binder.putLocal("dWfName", wfName);
/* 2866 */       this.m_binder.putLocal("dWfStepName", stepName);
/* 2867 */       this.m_binder.putLocal("dWfID", this.m_workflowInfo.m_wfID);
/* 2868 */       this.m_binder.putLocal("dWfType", this.m_workflowInfo.m_wfType);
/*      */     }
/* 2870 */     return true;
/*      */   }
/*      */ 
/*      */   protected WorkflowInfo getAndValidateWorkflowChange(String wfName, String stepName)
/*      */     throws DataException
/*      */   {
/* 2882 */     WorkflowInfo workflowInfo = this.m_workflowInfo;
/* 2883 */     if (wfName == null)
/*      */     {
/* 2885 */       wfName = this.m_binder.getLocal("dWfName");
/*      */     }
/*      */ 
/* 2888 */     boolean isWfChanged = (wfName != null) && (!wfName.equals(this.m_workflowInfo.m_wfName));
/* 2889 */     String wfType = this.m_workflowInfo.m_wfType;
/*      */ 
/* 2891 */     String errMsg = null;
/* 2892 */     if (wfType.equalsIgnoreCase("basic"))
/*      */     {
/* 2896 */       if (isWfChanged)
/*      */       {
/* 2898 */         errMsg = "!csWfJumpFromBasicError";
/*      */       }
/*      */     }
/* 2901 */     else if (isWfChanged)
/*      */     {
/* 2903 */       String projectID = this.m_workflowInfo.m_properties.getProperty("dProjectID");
/* 2904 */       if ((projectID != null) && (projectID.length() > 0))
/*      */       {
/* 2906 */         errMsg = "!csWfJumpToProjectError";
/*      */       }
/* 2908 */       WorkflowData wfData = null;
/* 2909 */       if (errMsg == null)
/*      */       {
/* 2911 */         wfData = (WorkflowData)SharedObjects.getTable(WorkflowData.m_tableName);
/* 2912 */         workflowInfo = wfData.getWorkflowInfo(wfName);
/* 2913 */         if (workflowInfo == null)
/*      */         {
/* 2915 */           errMsg = LocaleUtils.encodeMessage("csWfMissing", null, wfName);
/*      */         }
/* 2917 */         if (errMsg == null)
/*      */         {
/* 2919 */           boolean isCriteria = (workflowInfo.m_wfType.equalsIgnoreCase("criteria")) || (workflowInfo.m_wfType.equalsIgnoreCase("subworkflow"));
/*      */ 
/* 2921 */           if (this.m_isCriteria != isCriteria)
/*      */           {
/* 2924 */             errMsg = "!csWfJumpToTypeError";
/*      */           }
/*      */ 
/* 2927 */           if ((errMsg == null) && (workflowInfo.m_wfStatus.equalsIgnoreCase("INIT")))
/*      */           {
/* 2929 */             errMsg = "!csWfNotEnabled";
/*      */           }
/*      */ 
/* 2932 */           String oldGroup = this.m_workflowInfo.m_properties.getProperty("dSecurityGroup");
/* 2933 */           String newGroup = workflowInfo.m_properties.getProperty("dSecurityGroup");
/* 2934 */           if ((errMsg == null) && (!oldGroup.equalsIgnoreCase(newGroup)))
/*      */           {
/* 2936 */             errMsg = "!csWfDifferentSecurityGroup";
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2942 */     if (errMsg != null)
/*      */     {
/* 2944 */       errMsg = LocaleUtils.encodeMessage("csWfJumpError", errMsg, this.m_workflowInfo.m_wfName, wfName);
/* 2945 */       throw new DataException(errMsg);
/*      */     }
/* 2947 */     return workflowInfo;
/*      */   }
/*      */ 
/*      */   protected void updateCurrentStepInfo(boolean isEntry, boolean isJump, boolean isNotify)
/*      */   {
/* 2959 */     this.m_binder.mergeResultSetRowIntoLocalData(this.m_stepData);
/* 2960 */     String stepID = this.m_stepData.getStepID();
/* 2961 */     this.m_binder.putLocal("dWfCurrentStepID", stepID);
/*      */ 
/* 2963 */     String wfName = this.m_binder.getLocal("dWfName");
/* 2964 */     String stepName = this.m_stepData.getStepName();
/* 2965 */     this.m_wfHelper.updateWfStepInfo(wfName, stepName);
/*      */ 
/* 2967 */     Report.trace("workflow", "updateCurrentStepInfo: step=" + stepName + " stepID=" + stepID + " workflow=" + wfName, null);
/*      */ 
/* 2969 */     Report.trace("workflow", "updateCurrentStepInfo: isEntry=" + isEntry + " isJump=" + isJump + " isNotify=" + isNotify, null);
/*      */ 
/* 2972 */     String lookupKey = this.m_wfHelper.m_lookupKey;
/* 2973 */     if (isEntry)
/*      */     {
/* 2977 */       int num = this.m_parents.size();
/* 2978 */       boolean isUnwind = false;
/* 2979 */       for (int i = num - 1; i >= 0; --i)
/*      */       {
/* 2981 */         if (isUnwind)
/*      */         {
/* 2983 */           this.m_parents.removeElementAt(0);
/*      */         }
/*      */         else
/*      */         {
/* 2987 */           String str = (String)this.m_parents.elementAt(i);
/* 2988 */           if (str.charAt(0) == '*')
/*      */           {
/* 2990 */             str = str.substring(1);
/*      */           }
/* 2992 */           if (!str.equals(lookupKey))
/*      */             continue;
/* 2994 */           isUnwind = true;
/* 2995 */           this.m_parents.removeElementAt(i);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3001 */       if ((isJump) && (!isUnwind))
/*      */       {
/* 3003 */         String prevParent = null;
/* 3004 */         if (this.m_parents.size() > 0)
/*      */         {
/* 3006 */           prevParent = (String)this.m_parents.elementAt(0);
/*      */         }
/*      */ 
/* 3009 */         if ((prevParent != null) && (prevParent.charAt(0) != '*'))
/*      */         {
/* 3011 */           this.m_parents.setElementAt("*" + prevParent, 0);
/*      */         }
/*      */       }
/*      */ 
/* 3015 */       this.m_parents.insertElementAt(lookupKey, 0);
/*      */     }
/*      */ 
/* 3018 */     exchangeCompanionData(isJump);
/*      */ 
/* 3023 */     if (!isNotify) {
/*      */       return;
/*      */     }
/* 3026 */     boolean isInformUsers = !StringUtils.convertToBool(this.m_scriptResultData.getLocal("wfJumpEntryNotifyOff"), false);
/*      */ 
/* 3030 */     computeMailInfo(false, isInformUsers);
/*      */   }
/*      */ 
/*      */   protected void writeCompanionFile()
/*      */     throws ServiceException
/*      */   {
/* 3039 */     String str = StringUtils.createString(this.m_parents, '#', '^');
/* 3040 */     this.m_wfCompanionData.m_data.putLocal("wfParentList", str);
/*      */ 
/* 3043 */     WfCompanionManager.writeCompanionFile(this.m_wfCompanionData);
/*      */   }
/*      */ 
/*      */   protected void reportLastError(Exception e, String errMsg)
/*      */   {
/* 3048 */     Report.error(null, errMsg, e);
/* 3049 */     this.m_wfCompanionData.m_data.putLocal("wfLastErrorMsg", errMsg);
/*      */   }
/*      */ 
/*      */   protected boolean evaluateCondition(String condKey) throws ServiceException
/*      */   {
/* 3054 */     String key = this.m_wfHelper.m_stepName + ":" + condKey;
/*      */ 
/* 3056 */     boolean isMet = false;
/*      */     try
/*      */     {
/* 3059 */       String scriptStr = WorkflowScriptUtils.createConditionScript(key, this.m_wfDesign.m_designData);
/* 3060 */       if ((scriptStr != null) && (scriptStr.trim().length() > 0))
/*      */       {
/* 3063 */         PageMerger pageMerger = (PageMerger)this.m_context.getCachedObject("PageMerger");
/* 3064 */         prepareAndEvaluateScript(scriptStr, pageMerger);
/*      */ 
/* 3067 */         isMet = StringUtils.convertToBool(this.m_binder.getLocal(key + "IsMet"), false);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 3073 */       String errMsg = LocaleUtils.encodeMessage("csWfConditionError", null, this.m_wfHelper.m_wfName, this.m_wfHelper.m_stepName);
/*      */ 
/* 3075 */       Report.error(null, errMsg, e);
/*      */     }
/*      */ 
/* 3078 */     return isMet;
/*      */   }
/*      */ 
/*      */   protected DataBinder prepareAndEvaluateScript(String scriptStr, PageMerger pageMerger)
/*      */     throws IOException
/*      */   {
/* 3084 */     DataBinder scriptResultData = new DataBinder();
/* 3085 */     this.m_context.setCachedObject("WorkflowScriptResult", scriptResultData);
/* 3086 */     this.m_context.setCachedObject("WorkflowPrefix", this.m_wfHelper.m_lookupKey);
/* 3087 */     this.m_context.setCachedObject("WorkflowInfo", this.m_workflowInfo);
/*      */ 
/* 3090 */     WfStepData stepData = (WfStepData)this.m_stepData.shallowClone();
/* 3091 */     stepData.initShallow(this.m_stepData);
/*      */ 
/* 3093 */     stepData.setCurrentRow(this.m_stepData.getCurrentRow());
/* 3094 */     this.m_context.setCachedObject("WorkflowSteps", stepData);
/*      */ 
/* 3098 */     Service service = null;
/* 3099 */     if (this.m_context instanceof Service)
/*      */     {
/* 3101 */       service = (Service)this.m_context;
/*      */     }
/*      */     try
/*      */     {
/* 3105 */       if (service != null)
/*      */       {
/* 3107 */         service.setConditionVar("allowWorkflowIdocScript", true);
/*      */       }
/* 3109 */       pageMerger.evaluateScript(scriptStr);
/*      */     }
/*      */     finally
/*      */     {
/* 3113 */       if (service != null)
/*      */       {
/* 3115 */         service.setConditionVar("allowWorkflowIdocScript", false);
/*      */       }
/*      */     }
/*      */ 
/* 3119 */     return scriptResultData;
/*      */   }
/*      */ 
/*      */   public boolean getUseLocal()
/*      */   {
/* 3127 */     return this.m_useLocal;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 3132 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WorkflowStates
 * JD-Core Version:    0.5.4
 */