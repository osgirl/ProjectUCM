/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.File;
/*     */ 
/*     */ public class FileAttemptingLockParameters
/*     */ {
/*  32 */   public File m_beforeRenameFile = null;
/*     */ 
/*  37 */   public File m_afterRenameFile = null;
/*     */ 
/*  42 */   public File m_reserveLockFile = null;
/*     */ 
/*  47 */   public boolean m_afterLongLock = false;
/*     */ 
/*  52 */   public boolean m_lockIsExternalReserve = false;
/*     */ 
/*  57 */   public boolean m_isLockOurReserve = false;
/*     */ 
/*  62 */   public FileDirLockData m_lockData = null;
/*     */ 
/*  67 */   public boolean m_lockStarted = false;
/*     */ 
/*  72 */   public int m_waitInterval = 5000;
/*     */ 
/*  77 */   public int m_totalTimeAllowed = 30000;
/*     */ 
/*  84 */   public int m_totalTimeWaited = 0;
/*     */ 
/*  89 */   public boolean m_specifyTimeout = false;
/*     */ 
/*  94 */   public long m_timeout = 0L;
/*     */ 
/*  99 */   public long m_timeRemaining = 0L;
/*     */ 
/* 104 */   public long m_expireTime = 0L;
/*     */ 
/* 110 */   public boolean m_timedOut = false;
/*     */ 
/* 115 */   public boolean m_lockIsExternalPromoted = false;
/*     */ 
/* 121 */   public boolean m_promoteToLongLock = false;
/*     */ 
/* 126 */   public ExecutionContext m_cxt = null;
/*     */ 
/*     */   FileAttemptingLockParameters(File beforeRename, File afterRename, FileDirLockData lockData)
/*     */   {
/* 133 */     this.m_beforeRenameFile = beforeRename;
/* 134 */     this.m_afterRenameFile = afterRename;
/* 135 */     this.m_lockData = lockData;
/*     */   }
/*     */ 
/*     */   FileAttemptingLockParameters(FileDirLockData lockData)
/*     */   {
/* 143 */     this.m_lockData = lockData;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 149 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileAttemptingLockParameters
 * JD-Core Version:    0.5.4
 */