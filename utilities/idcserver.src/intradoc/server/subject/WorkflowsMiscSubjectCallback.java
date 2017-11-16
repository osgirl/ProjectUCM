/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.workflow.WfScriptManager;
/*    */ import intradoc.server.workflow.WorkflowUtils;
/*    */ 
/*    */ public class WorkflowsMiscSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 33 */     if (subject.equals("wftemplates"))
/*    */     {
/* 35 */       WorkflowUtils.cacheTemplates(true);
/*    */     } else {
/* 37 */       if (!subject.equals("wfscripts"))
/*    */         return;
/* 39 */       WfScriptManager.load(false);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.WorkflowsMiscSubjectCallback
 * JD-Core Version:    0.5.4
 */