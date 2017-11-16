/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.schema.SchemaManager;
/*    */ import intradoc.server.schema.ServerSchemaManager;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class SchemaSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 40 */     SchemaManager.getManager(this.m_workspace).refresh(this.m_workspace);
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/* 46 */     super.loadBinder(subject, binder, cxt);
/*    */ 
/* 53 */     DataResultSet tables = SharedObjects.getTable("SchemaTypes");
/*    */     try
/*    */     {
/* 56 */       ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/*    */ 
/* 58 */       for (tables.first(); tables.isRowPresent(); tables.next())
/*    */       {
/* 60 */         Properties typeData = tables.getCurrentRowProps();
/* 61 */         manager.getSchemaData(binder, typeData.getProperty("SourceName"));
/*    */       }
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/* 66 */       Report.trace("schema", null, e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 73 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.SchemaSubjectCallback
 * JD-Core Version:    0.5.4
 */