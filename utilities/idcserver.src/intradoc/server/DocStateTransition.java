/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.IdcDescriptorState;
/*     */ import intradoc.server.workflow.WorkflowStates;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ 
/*     */ public class DocStateTransition
/*     */ {
/*     */   public static WorkflowStates createWorkflowStatesObject(DataBinder binder, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*  43 */     WorkflowStates statesObj = (WorkflowStates)ComponentClassFactory.createClassInstance("WorkflowStates", "intradoc.server.workflow.WorkflowStates", "!csWorkflowTransitionClassError");
/*     */ 
/*  46 */     statesObj.init(binder, ws, cxt);
/*  47 */     return statesObj;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void advanceDocumentState(DataBinder binder, Workspace ws, boolean isNotLatestRev, boolean docConverted, boolean isHistoryOnly, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  61 */     WorkflowStates work = createWorkflowStatesObject(binder, ws, cxt);
/*  62 */     work.advanceDocumentState(isNotLatestRev, docConverted, isHistoryOnly, true);
/*     */   }
/*     */ 
/*     */   public static void advanceDocumentState(DataBinder binder, Workspace ws, boolean isNotLatestRev, boolean docConverted, boolean isHistoryOnly, boolean useLocal, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  74 */     WorkflowStates work = createWorkflowStatesObject(binder, ws, cxt);
/*  75 */     work.advanceDocumentState(isNotLatestRev, docConverted, isHistoryOnly, useLocal);
/*     */ 
/*  77 */     IdcDescriptorState states = (IdcDescriptorState)cxt.getCachedObject("DescriptorStates");
/*  78 */     if (states == null)
/*     */       return;
/*  80 */     states.finishUpdate(binder, ws);
/*     */   }
/*     */ 
/*     */   public static void updateStateAfterDocumentAdvance(DataBinder binder, Workspace ws)
/*     */     throws DataException
/*     */   {
/*  90 */     if (DataBinderUtils.getLocalBoolean(binder, "isStatusChanged", false))
/*     */     {
/*  92 */       ws.execute("UrevisionWorkflowState", binder);
/*  93 */       binder.removeLocal("isStatusChanged");
/*     */     }
/*  95 */     if (!DataBinderUtils.getLocalBoolean(binder, "notifyIndexer", false))
/*     */       return;
/*  97 */     SubjectManager.notifyChanged("indexerwork");
/*  98 */     binder.removeLocal("notifyIndexer");
/*     */   }
/*     */ 
/*     */   public static void checkIsFinishedBasicWorkflow(DataBinder binder, Workspace ws, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 109 */     WorkflowStates work = createWorkflowStatesObject(binder, ws, ctxt);
/* 110 */     work.checkIsFinishedBasicWorkflow();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70600 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocStateTransition
 * JD-Core Version:    0.5.4
 */