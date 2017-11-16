/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.DataLoader;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.schema.SchemaViewConfig;
/*    */ import intradoc.shared.schema.SchemaViewData;
/*    */ 
/*    */ public class DocTypesSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 34 */     DataLoader.cacheTableAndOptList("DocTypes", "QdocTypes", "dDocType", "docTypes", null, this.m_workspace);
/*    */ 
/* 37 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/* 38 */     if (views == null)
/*    */       return;
/* 40 */     SchemaViewData svd = (SchemaViewData)views.getData("docTypes");
/* 41 */     if (svd == null)
/*    */       return;
/* 43 */     svd.markCacheDirty(svd, null, null);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DocTypesSubjectCallback
 * JD-Core Version:    0.5.4
 */