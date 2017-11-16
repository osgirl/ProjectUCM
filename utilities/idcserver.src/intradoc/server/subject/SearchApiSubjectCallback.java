/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SearchLoader;
/*    */ import intradoc.server.SearchManager;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class SearchApiSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/*    */     try
/*    */     {
/* 34 */       SearchLoader.cacheSearchCollections();
/*    */ 
/* 36 */       Object obj = SharedObjects.getObject("globalObjects", "SearchManager");
/* 37 */       if (obj != null)
/*    */       {
/* 39 */         SearchManager sManager = (SearchManager)obj;
/* 40 */         sManager.forceRefreshCurrentConnections(false);
/*    */       }
/*    */ 
/* 49 */       SearchLoader.processApiChangeReleasedDocumentsChange(null, null);
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 53 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadSubject", null, subject);
/*    */ 
/* 55 */       Report.error(null, msg, e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 61 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.SearchApiSubjectCallback
 * JD-Core Version:    0.5.4
 */