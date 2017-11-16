/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.FileUtils;
/*    */ import intradoc.common.FileUtilsCfgBuilder;
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.resource.ResourceUtils;
/*    */ import intradoc.server.LegacyDirectoryLocator;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.archive.TransferMonitor;
/*    */ import intradoc.shared.ArchiveCollections;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import java.io.File;
/*    */ 
/*    */ public class ArchiveCollectionsSubjectCallback extends SubjectCallbackAdapter
/*    */ {
/*    */   public void refresh(String subject)
/*    */     throws DataException, ServiceException
/*    */   {
/* 36 */     cacheCollections();
/* 37 */     if (!SharedObjects.getEnvValueAsBoolean("IsAutoArchiver", false))
/*    */       return;
/* 39 */     TransferMonitor.checkInit();
/*    */   }
/*    */ 
/*    */   public void cacheCollections()
/*    */     throws ServiceException, DataException
/*    */   {
/* 49 */     String collDir = LegacyDirectoryLocator.getCollectionsDirectory();
/*    */ 
/* 51 */     FileUtils.reserveDirectory(collDir);
/*    */     try
/*    */     {
/* 56 */       DataBinder binder = null;
/* 57 */       String filename = "collections.hda";
/* 58 */       File file = FileUtilsCfgBuilder.getCfgFile(collDir + filename, "Collection", false);
/* 59 */       boolean isNew = true;
/*    */       try
/*    */       {
/* 63 */         if (file.exists())
/*    */         {
/* 65 */           binder = ResourceUtils.readDataBinder(collDir, filename);
/* 66 */           isNew = false;
/*    */         }
/*    */       }
/*    */       catch (Exception e)
/*    */       {
/* 71 */         Report.warning(null, e, "csCollectionsFileWillBeRebuilt", new Object[0]);
/*    */       }
/*    */ 
/* 74 */       if (isNew)
/*    */       {
/* 76 */         binder = new DataBinder();
/*    */       }
/*    */ 
/* 79 */       ArchiveCollections colls = new ArchiveCollections();
/* 80 */       boolean isChanged = colls.load(binder);
/*    */ 
/* 82 */       SharedObjects.putTable(colls.getTableName(), colls);
/*    */ 
/* 84 */       if ((isNew) || (isChanged))
/*    */       {
/* 86 */         ResourceUtils.serializeDataBinder(collDir, filename, binder, true, false);
/*    */       }
/*    */     }
/*    */     finally
/*    */     {
/* 91 */       FileUtils.releaseDirectory(collDir);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 98 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.ArchiveCollectionsSubjectCallback
 * JD-Core Version:    0.5.4
 */