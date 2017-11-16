/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.DocFormats;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.converter.TemplateConversions;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.schema.SchemaHelper;
/*    */ 
/*    */ public class DocFormatsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 34 */     DocFormats formats = new DocFormats();
/* 35 */     formats.load(this.m_workspace);
/* 36 */     SharedObjects.putTable(formats.getTableName(), formats);
/*    */ 
/* 38 */     boolean isDynConvEnabled = SharedObjects.getEnvValueAsBoolean("IsDynamicConverterEnabled", false);
/*    */ 
/* 40 */     if (isDynConvEnabled)
/*    */     {
/* 42 */       TemplateConversions.refresh();
/*    */     }
/*    */ 
/* 45 */     SchemaHelper schHelper = new SchemaHelper();
/* 46 */     schHelper.markViewCacheDirty("docFormats", null);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.DocFormatsSubjectCallback
 * JD-Core Version:    0.5.4
 */