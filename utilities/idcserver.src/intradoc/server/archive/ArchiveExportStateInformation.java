/*    */ package intradoc.server.archive;
/*    */ 
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSetUtils;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class ArchiveExportStateInformation
/*    */ {
/*    */   public DataResultSet m_masterSet;
/*    */   public int m_classIndex;
/*    */   public boolean m_isDeleteExport;
/*    */   public DataResultSet m_revInfoSet;
/*    */   public DataResultSet m_docSet;
/*    */   public String m_fileDirectory;
/*    */   public Properties m_props;
/*    */   public int m_numBatched;
/*    */   public int m_classExportCount;
/*    */ 
/*    */   public ArchiveExportStateInformation()
/*    */   {
/* 37 */     this.m_masterSet = null;
/* 38 */     this.m_classIndex = -1;
/* 39 */     this.m_isDeleteExport = false;
/*    */ 
/* 47 */     this.m_revInfoSet = null;
/* 48 */     this.m_docSet = null;
/* 49 */     this.m_fileDirectory = null;
/* 50 */     this.m_props = new Properties();
/*    */ 
/* 55 */     this.m_numBatched = 0;
/*    */ 
/* 60 */     this.m_classExportCount = 0;
/*    */   }
/*    */ 
/*    */   public void resetBatchInfo()
/*    */   {
/* 68 */     this.m_numBatched = 0;
/* 69 */     this.m_docSet = null;
/* 70 */     this.m_props = new Properties();
/* 71 */     if ((this.m_revInfoSet == null) || (this.m_revInfoSet.isRowPresent()))
/*    */       return;
/* 73 */     this.m_revInfoSet = null;
/*    */   }
/*    */ 
/*    */   public String getClassId()
/*    */     throws DataException
/*    */   {
/* 79 */     if (this.m_classIndex < 0)
/*    */     {
/* 81 */       this.m_classIndex = ResultSetUtils.getIndexMustExist(this.m_masterSet, "dRevClassID");
/*    */     }
/* 83 */     return this.m_masterSet.getStringValue(this.m_classIndex);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 88 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveExportStateInformation
 * JD-Core Version:    0.5.4
 */