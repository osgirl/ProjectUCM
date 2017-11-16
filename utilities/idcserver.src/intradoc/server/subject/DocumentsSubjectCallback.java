/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ 
/*    */ public class DocumentsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 46 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DocumentsSubjectCallback
 * JD-Core Version:    0.5.4
 */