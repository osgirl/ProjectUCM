/*    */ package intradoc.shared.workflow;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class WorkflowTemplates extends DataResultSet
/*    */ {
/*    */   public static final String m_tableName = "WfTemplates";
/* 30 */   public static final String[] COLUMNS = { "dWfTemplateName", "dWfTemplateDescription" };
/*    */ 
/*    */   public WorkflowTemplates()
/*    */   {
/* 38 */     super(COLUMNS);
/*    */   }
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 44 */     DataResultSet rset = new WorkflowTemplates();
/* 45 */     initShallow(rset);
/*    */ 
/* 47 */     return rset;
/*    */   }
/*    */ 
/*    */   public void load(DataBinder binder)
/*    */   {
/* 52 */     ResultSet rset = binder.getResultSet("WfTemplates");
/* 53 */     load(rset);
/*    */   }
/*    */ 
/*    */   public void load(ResultSet rset)
/*    */   {
/* 58 */     if (rset != null)
/*    */     {
/* 60 */       reset();
/* 61 */       copy(rset);
/*    */     }
/*    */ 
/* 64 */     SharedObjects.putTable("WfTemplates", this);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 69 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.workflow.WorkflowTemplates
 * JD-Core Version:    0.5.4
 */