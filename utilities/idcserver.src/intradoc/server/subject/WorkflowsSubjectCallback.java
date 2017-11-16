/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.workflow.WorkflowData;
/*    */ 
/*    */ public class WorkflowsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 33 */     WorkflowData workflows = new WorkflowData();
/*    */     try
/*    */     {
/* 36 */       ResultSet rset = this.m_workspace.createResultSet(WorkflowData.m_queryName, null);
/*    */ 
/* 38 */       workflows.loadWorkflows(rset);
/*    */ 
/* 40 */       rset = this.m_workspace.createResultSet(WorkflowData.m_criteriaTableName, null);
/*    */ 
/* 42 */       DataResultSet drset = new DataResultSet();
/* 43 */       drset.copy(rset);
/*    */ 
/* 45 */       ResultSet sset = this.m_workspace.createResultSet("QworkflowSubs", null);
/* 46 */       workflows.loadCriteria(drset, sset);
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 50 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadWorkflowTables", e.getMessage());
/*    */ 
/* 52 */       throw new DataException(msg);
/*    */     }
/* 54 */     SharedObjects.putTable(workflows.getTableName(), workflows);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 59 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.WorkflowsSubjectCallback
 * JD-Core Version:    0.5.4
 */