/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class AutomatedArchiveData
/*     */ {
/*  31 */   public String m_location = null;
/*  32 */   public String m_collectionName = null;
/*  33 */   public String m_archiveName = null;
/*     */ 
/*  36 */   public Properties m_props = null;
/*     */ 
/*  38 */   public boolean m_isChanged = false;
/*     */ 
/*  40 */   public long m_archiveTargetTS = -2L;
/*  41 */   public long m_exportTargetTS = -2L;
/*     */ 
/*  43 */   public int m_counter = 0;
/*  44 */   public int m_counterThreshold = 0;
/*     */ 
/*     */   public AutomatedArchiveData(String location)
/*     */   {
/*  49 */     this.m_location = location;
/*  50 */     String[] data = ArchiveUtils.parseLocation(location);
/*     */ 
/*  52 */     this.m_collectionName = data[0];
/*  53 */     this.m_archiveName = data[1];
/*     */   }
/*     */ 
/*     */   public String getProperty(String key)
/*     */   {
/*  58 */     return this.m_props.getProperty(key);
/*     */   }
/*     */ 
/*     */   public boolean checkArchiveTS()
/*     */   {
/*     */     try
/*     */     {
/*  65 */       long ts = ArchiveUtils.checkArchiveFile(this.m_collectionName, this.m_archiveName);
/*  66 */       if ((ts >= 0L) && (((ts != this.m_archiveTargetTS) || (this.m_isChanged))))
/*     */       {
/*  68 */         this.m_archiveTargetTS = ts;
/*  69 */         return true;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  74 */       String msg = LocaleUtils.encodeMessage("csArchiverDefinitionError", null, this.m_archiveName, this.m_collectionName);
/*     */ 
/*  76 */       Report.appError("archiver", null, msg, e);
/*     */     }
/*  78 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean checkExportsTS()
/*     */   {
/*     */     try
/*     */     {
/*  85 */       long ts = ArchiveUtils.checkExportFile(this.m_collectionName, this.m_archiveName);
/*  86 */       if (ts != this.m_exportTargetTS)
/*     */       {
/*  88 */         this.m_isChanged = true;
/*  89 */         this.m_exportTargetTS = ts;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  94 */       String msg = LocaleUtils.encodeMessage("csArchiverExportDefError", null, this.m_archiveName, this.m_collectionName);
/*     */ 
/*  96 */       Report.appError("archiver", null, msg, e);
/*     */     }
/*  98 */     return this.m_isChanged;
/*     */   }
/*     */ 
/*     */   public void resetForTarget(long archiveTargetTS, long exportTargetTS)
/*     */   {
/* 103 */     this.m_isChanged = false;
/* 104 */     this.m_archiveTargetTS = archiveTargetTS;
/* 105 */     this.m_exportTargetTS = exportTargetTS;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.AutomatedArchiveData
 * JD-Core Version:    0.5.4
 */