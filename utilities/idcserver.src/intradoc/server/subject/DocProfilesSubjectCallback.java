/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.DocProfileManager;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ 
/*    */ public class DocProfilesSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 32 */     DocProfileManager.load();
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/* 38 */     super.loadBinder(subject, binder, cxt);
/*    */ 
/* 40 */     String triggerField = DocProfileManager.getTriggerField();
/* 41 */     if (triggerField == null)
/*    */       return;
/* 43 */     binder.putLocal("dpTriggerField", triggerField);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 49 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DocProfilesSubjectCallback
 * JD-Core Version:    0.5.4
 */