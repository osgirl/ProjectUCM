/*    */ package intradoc.shared.workflow;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSet;
/*    */ 
/*    */ public class WfScriptStorage
/*    */ {
/* 31 */   public String m_scriptName = null;
/* 32 */   public String m_filename = null;
/* 33 */   public long m_lastLoadedTs = -2L;
/*    */ 
/* 35 */   public DataBinder m_scriptData = null;
/*    */ 
/*    */   public WfScriptStorage(String name)
/*    */   {
/* 40 */     this.m_scriptName = name;
/* 41 */     this.m_filename = WorkflowScriptUtils.computeScriptFileName(this.m_scriptName);
/* 42 */     this.m_scriptData = new DataBinder();
/* 43 */     DataResultSet drset = new DataResultSet(WorkflowScriptUtils.WF_JUMP_COLUMNS);
/* 44 */     this.m_scriptData.addResultSet("WorkflowScriptJumps", drset);
/*    */   }
/*    */ 
/*    */   public WfScriptStorage copy()
/*    */   {
/* 49 */     WfScriptStorage wfScript = new WfScriptStorage(this.m_scriptName);
/* 50 */     wfScript.m_lastLoadedTs = this.m_lastLoadedTs;
/*    */ 
/* 52 */     DataBinder data = new DataBinder();
/* 53 */     data.merge(this.m_scriptData);
/* 54 */     wfScript.m_scriptData = data;
/*    */ 
/* 60 */     DataResultSet jumpSet = (DataResultSet)this.m_scriptData.getResultSet("WorkflowScriptJumps");
/* 61 */     DataResultSet drset = new DataResultSet();
/* 62 */     drset.copy(jumpSet);
/*    */ 
/* 64 */     data.addResultSet("WorkflowScriptJumps", drset);
/*    */ 
/* 66 */     return wfScript;
/*    */   }
/*    */ 
/*    */   public void setScriptData(DataBinder binder)
/*    */   {
/* 71 */     this.m_scriptData = binder;
/* 72 */     ResultSet rset = this.m_scriptData.getResultSet("WorkflowScriptJumps");
/* 73 */     if (rset != null)
/*    */       return;
/* 75 */     rset = new DataResultSet(WorkflowScriptUtils.WF_JUMP_COLUMNS);
/* 76 */     this.m_scriptData.addResultSet("WorkflowScriptJumps", rset);
/*    */   }
/*    */ 
/*    */   public DataBinder getScriptData()
/*    */   {
/* 82 */     return this.m_scriptData;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 87 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WfScriptStorage
 * JD-Core Version:    0.5.4
 */