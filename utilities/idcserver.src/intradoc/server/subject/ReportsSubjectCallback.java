/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.LocaleUtils;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.data.ResultSetUtils;
/*    */ import intradoc.server.DataLoader;
/*    */ import intradoc.server.Service;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.UserData;
/*    */ import intradoc.shared.UserDocumentAccessFilter;
/*    */ import intradoc.util.IdcMessage;
/*    */ 
/*    */ public class ReportsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 35 */     DataLoader.cacheReportFiles();
/*    */   }
/*    */ 
/*    */   public void loadFilteredData(String subject, DataBinder binder, Service srv, UserData userData, UserDocumentAccessFilter readFilter)
/*    */   {
/* 43 */     ResultSet dataSources = SharedObjects.getTable("StdReportDataSources");
/* 44 */     if (dataSources != null)
/*    */     {
/* 46 */       binder.addResultSet("StdReportDataSources", dataSources);
/*    */       try
/*    */       {
/* 52 */         int resNameIndex = ResultSetUtils.getIndexMustExist(dataSources, "resourceName");
/* 53 */         for (dataSources.first(); dataSources.isRowPresent(); dataSources.next())
/*    */         {
/* 55 */           String resName = dataSources.getStringValue(resNameIndex);
/* 56 */           ResultSet table = SharedObjects.getTable(resName);
/* 57 */           binder.addResultSet(resName, table);
/*    */         }
/*    */       }
/*    */       catch (Exception e)
/*    */       {
/* 62 */         IdcMessage msg = IdcMessageFactory.lc(e, "csUnableToLoadDataSourcesForSubject", new Object[] { "REPORTS" });
/* 63 */         Report.trace(null, LocaleResources.localizeMessage(null, msg, null).toString(), e);
/*    */       }
/*    */ 
/*    */     }
/*    */ 
/* 68 */     ResultSet displayMaps = SharedObjects.getTable("StdReportDisplayMaps");
/* 69 */     if (displayMaps == null)
/*    */       return;
/* 71 */     binder.addResultSet("StdReportDisplayMaps", displayMaps);
/*    */     try
/*    */     {
/* 77 */       int resNameIndex = ResultSetUtils.getIndexMustExist(displayMaps, "resourceName");
/* 78 */       for (displayMaps.first(); displayMaps.isRowPresent(); displayMaps.next())
/*    */       {
/* 80 */         String resName = displayMaps.getStringValue(resNameIndex);
/* 81 */         ResultSet table = SharedObjects.getTable(resName);
/* 82 */         binder.addResultSet(resName, table);
/*    */       }
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 87 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadOptionListsForSubject", e.getMessage(), "REPORTS");
/*    */ 
/* 89 */       Report.trace(null, LocaleResources.localizeMessage(msg, null), e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.ReportsSubjectCallback
 * JD-Core Version:    0.5.4
 */