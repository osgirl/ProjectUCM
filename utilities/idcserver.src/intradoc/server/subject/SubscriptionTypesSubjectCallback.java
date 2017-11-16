/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.common.FileUtilsCfgBuilder;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.Table;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSet;
/*    */ import intradoc.resource.ResourceUtils;
/*    */ import intradoc.server.LegacyDirectoryLocator;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.io.File;
/*    */ import java.util.List;
/*    */ 
/*    */ public class SubscriptionTypesSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 35 */     String dir = LegacyDirectoryLocator.getAppDataDirectory() + "subscription";
/* 36 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 0, true);
/* 37 */     FileUtils.reserveDirectory(dir);
/*    */     try
/*    */     {
/* 41 */       DataBinder binder = null;
/* 42 */       String filename = "subscription_types.hda";
/* 43 */       File file = FileUtilsCfgBuilder.getCfgFile(dir + "/" + filename, "Subscription", false);
/*    */ 
/* 45 */       if (file.exists())
/*    */       {
/* 47 */         binder = ResourceUtils.readDataBinder(dir, filename);
/*    */ 
/* 49 */         ResultSet subscriptionTypes = binder.getResultSet("SubscriptionTypes");
/* 50 */         SharedObjects.putTable("SubscriptionTypes", (DataResultSet)subscriptionTypes);
/*    */       }
/*    */       else
/*    */       {
/* 55 */         DataResultSet types = SharedObjects.getTable("SubscriptionTypes");
/* 56 */         Table newTypes = new Table();
/* 57 */         newTypes.m_colNames = new String[] { "scpType", "scpFields", "scpDescription", "scpEnabled" };
/*    */ 
/* 59 */         for (types.first(); types.isRowPresent(); types.next())
/*    */         {
/* 62 */           String[] row = new String[4];
/* 63 */           String type = types.getStringValue(0);
/* 64 */           String fields = types.getStringValue(1);
/* 65 */           String description = types.getStringValue(2);
/* 66 */           row[0] = type;
/* 67 */           row[1] = fields;
/* 68 */           row[2] = description;
/* 69 */           row[3] = "1";
/* 70 */           newTypes.m_rows.add(row);
/*    */         }
/* 72 */         types = new DataResultSet();
/* 73 */         if (!types.init(newTypes))
/*    */         {
/* 75 */           throw new ServiceException("!csUnableToLoadSubscriptionTypes");
/*    */         }
/* 77 */         SharedObjects.putTable("SubscriptionTypes", types);
/*    */       }
/*    */     }
/*    */     finally
/*    */     {
/* 82 */       FileUtils.releaseDirectory(dir);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void loadBinder(String subject, DataBinder binder, ExecutionContext cxt)
/*    */   {
/* 89 */     ResultSet rset = SharedObjects.getTable("SubscriptionTypes");
/* 90 */     binder.addResultSet("SubscriptionTypes", rset);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 96 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.SubscriptionTypesSubjectCallback
 * JD-Core Version:    0.5.4
 */