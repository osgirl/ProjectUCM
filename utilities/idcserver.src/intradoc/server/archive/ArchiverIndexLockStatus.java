/*      */ package intradoc.server.archive;
/*      */ 
/*      */ import java.io.File;
/*      */ 
/*      */ class ArchiverIndexLockStatus
/*      */ {
/*      */   public long m_lastTime;
/*      */   public long m_lastModified;
/*      */   public boolean m_hasChanged;
/*      */   public boolean m_processOwnsLock;
/*      */   public boolean m_isLocked;
/*      */   public File m_file;
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1149 */     return "releaseInfo=dev,releaseRevision=$Rev: 97046 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiverIndexLockStatus
 * JD-Core Version:    0.5.4
 */