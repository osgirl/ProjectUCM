/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.UserStorage;
/*    */ 
/*    */ public class UserMetaOptListsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 31 */     UserStorage.synchronizeOptionLists(null, false, false);
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/*    */     try
/*    */     {
/* 39 */       UserStorage.synchronizeOptionLists(binder, false, true);
/*    */     }
/*    */     catch (ServiceException e)
/*    */     {
/* 44 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadSubject", e.getMessage(), "USERMETAOPTLISTS ");
/*    */ 
/* 46 */       Report.trace(null, LocaleResources.localizeMessage(msg, cxt), e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 52 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.UserMetaOptListsSubjectCallback
 * JD-Core Version:    0.5.4
 */