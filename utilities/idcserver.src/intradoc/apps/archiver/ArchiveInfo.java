/*      */ package intradoc.apps.archiver;
/*      */ 
/*      */ import intradoc.data.DataResultSet;
/*      */ 
/*      */ class ArchiveInfo
/*      */ {
/*      */   public String m_archiveName;
/*      */   public String m_moniker;
/*      */   public long m_timeStamp;
/*      */   public boolean m_isChanged;
/*      */   public DataResultSet m_batchFiles;
/*      */ 
/*      */   public ArchiveInfo(String archiveName, String moniker)
/*      */   {
/* 1387 */     this.m_moniker = moniker;
/* 1388 */     this.m_archiveName = archiveName;
/* 1389 */     this.m_timeStamp = -1L;
/* 1390 */     this.m_isChanged = true;
/* 1391 */     this.m_batchFiles = null;
/*      */   }
/*      */ 
/*      */   public void markChanged(long counter)
/*      */   {
/* 1396 */     if ((this.m_timeStamp >= 0L) && (counter == this.m_timeStamp))
/*      */       return;
/* 1398 */     this.m_timeStamp = counter;
/* 1399 */     this.m_isChanged = true;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1405 */     return "releaseInfo=dev,releaseRevision=$Rev: 83339 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.ArchiveInfo
 * JD-Core Version:    0.5.4
 */