/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.SharedPageMergerData;
/*     */ import java.io.File;
/*     */ 
/*     */ public class TemplatesSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  34 */     cacheCustomTemplateTables();
/*  35 */     DataLoader.cacheGlobalIncludes();
/*     */   }
/*     */ 
/*     */   public void cacheCustomTemplateTables() throws ServiceException, DataException
/*     */   {
/*  40 */     String templateDir = LegacyDirectoryLocator.getTemplatesDirectory();
/*     */ 
/*  43 */     cacheCustomResultsTemplates(templateDir);
/*     */   }
/*     */ 
/*     */   public void cacheCustomResultsTemplates(String templateDir)
/*     */     throws ServiceException, DataException
/*     */   {
/*  49 */     String resultTemplates = "results/custom_results.hda";
/*     */ 
/*  51 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  52 */     String custResultsPath = dataDir + resultTemplates;
/*  53 */     int fileState = FileUtils.checkFile(custResultsPath, true, false);
/*     */ 
/*  56 */     if (fileState == 0)
/*     */     {
/*  58 */       DataLoader.cacheTemplateDataFromFile(dataDir, resultTemplates, null);
/*     */     }
/*  60 */     else if (fileState != -16)
/*     */     {
/*  62 */       throw new ServiceException(fileState, FileUtils.getErrorMsg(custResultsPath, true, fileState));
/*     */     }
/*     */ 
/*  68 */     String sourceTable = "SearchResultTemplates";
/*     */ 
/*  70 */     DataResultSet drsOriginal = SharedObjects.getTable(sourceTable);
/*  71 */     if (drsOriginal == null)
/*     */     {
/*  73 */       String msg = LocaleUtils.encodeMessage("csSearchResultsTemplatesMissing", null, sourceTable);
/*     */ 
/*  75 */       throw new DataException(msg);
/*     */     }
/*     */ 
/*  80 */     DataLoader.mergeTables(sourceTable, "MergedVerityTemplates", "name");
/*     */ 
/*  83 */     if (SharedObjects.getTable("CurrentVerityTemplates") != null)
/*     */     {
/*  85 */       DataLoader.mergeTables("CurrentVerityTemplates", "MergedVerityTemplates", "name");
/*     */     }
/*     */ 
/*  92 */     DataResultSet drsCopy = new DataResultSet();
/*  93 */     drsCopy.copy(drsOriginal);
/*  94 */     drsCopy.removeFields(new String[] { "flexdata" });
/*  95 */     SharedObjects.putTable("SourceSearchTemplatesCopy", drsCopy);
/*  96 */     DataLoader.mergeTables("SourceSearchTemplatesCopy", "MergedVerityTemplates", "name");
/*     */ 
/*  99 */     DataResultSet drsDest = SharedObjects.getTable("MergedVerityTemplates");
/* 100 */     if (drsDest == null)
/*     */     {
/* 102 */       throw new DataException("!csSearchResultTemplateListError");
/*     */     }
/*     */ 
/* 106 */     for (drsDest.first(); drsDest.isRowPresent(); drsDest.next())
/*     */     {
/* 108 */       String name = drsDest.getStringValue(0);
/* 109 */       String formtype = drsDest.getStringValue(1);
/* 110 */       String filename = drsDest.getStringValue(2);
/*     */ 
/* 112 */       if ((filename != null) && (filename.length() > 0))
/*     */       {
/* 114 */         String resultPagePath = FileUtils.getAbsolutePath(templateDir, filename);
/* 115 */         File file = new File(resultPagePath);
/*     */ 
/* 117 */         if (file.exists())
/*     */         {
/* 119 */           DataLoader.cachePage(name, formtype, resultPagePath);
/*     */         }
/* 121 */         SharedPageMergerData.addTemplateInfo(name, resultPagePath, "Results", formtype);
/*     */       }
/*     */ 
/* 124 */       SharedPageMergerData.addResultTemplateInfo(name, drsDest.getCurrentRowProps());
/*     */     }
/*     */ 
/* 127 */     SharedObjects.putTable("CurrentVerityTemplates", drsDest);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 132 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73658 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.TemplatesSubjectCallback
 * JD-Core Version:    0.5.4
 */