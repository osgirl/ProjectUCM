/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ 
/*     */ public class FileDirLockData
/*     */ {
/*  30 */   public String m_dir = null;
/*     */ 
/*  35 */   public String m_agent = null;
/*     */ 
/*  41 */   public int m_numGlobalCount = 0;
/*     */ 
/*  46 */   public long m_numDirLocks = 0L;
/*     */ 
/*  52 */   public boolean m_isActive = false;
/*     */ 
/*  57 */   public boolean m_isPromoted = false;
/*     */ 
/*  62 */   public long m_startWaitForLockTime = 0L;
/*     */ 
/*  67 */   public long m_startTime = 0L;
/*     */   public Thread m_lockingThread;
/*     */ 
/*     */   public FileDirLockData()
/*     */   {
/*     */   }
/*     */ 
/*     */   public FileDirLockData(String dir, String agent, int numGlobalCount, long numDirLocks)
/*     */   {
/*  87 */     this.m_dir = dir;
/*  88 */     this.m_agent = agent;
/*  89 */     this.m_numGlobalCount = numGlobalCount;
/*  90 */     this.m_numDirLocks = numDirLocks;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 100 */     IdcStringBuilder strBuf = new IdcStringBuilder(255);
/* 101 */     appendDebugFormat(strBuf);
/* 102 */     return strBuf.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 107 */     appendable.append("m_dir=").append(this.m_dir).append(",");
/* 108 */     if (this.m_lockingThread != null)
/*     */     {
/* 110 */       appendable.append("m_lockingThread=").append(this.m_lockingThread.toString()).append(",");
/*     */     }
/* 112 */     appendable.append("m_isActive=" + this.m_isActive).append(",");
/* 113 */     appendable.append("m_numDirLocks=" + this.m_numDirLocks);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 120 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70481 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileDirLockData
 * JD-Core Version:    0.5.4
 */