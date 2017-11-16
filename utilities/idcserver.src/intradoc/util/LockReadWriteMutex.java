/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class LockReadWriteMutex
/*     */ {
/*  27 */   public final int NO_LOCK = 0;
/*  28 */   public final int READ_LOCK = 1;
/*  29 */   public final int WRITE_LOCK = 2;
/*     */ 
/*  33 */   public final int F_NONEXCLUSIVE_READ = 1;
/*  34 */   public final int F_EXCLUSIVE_WRITE = 2;
/*     */ 
/*  42 */   public static boolean m_auditLocking = false;
/*     */   public String m_name;
/*     */   public int m_currentLockLevel;
/*     */   public int m_numReadLocks;
/*     */   public boolean m_waitingExclusiveLock;
/*     */ 
/*     */   public LockReadWriteMutex(String name)
/*     */   {
/*  67 */     this.m_name = name;
/*     */   }
/*     */ 
/*     */   public synchronized void reserve(int flags)
/*     */     throws InterruptedException
/*     */   {
/*  80 */     boolean isExclusive = (flags & 0x2) != 0;
/*  81 */     boolean haveWaited = false;
/*  82 */     if (m_auditLocking)
/*     */     {
/*  84 */       System.out.println("Reserve, threadID=" + Thread.currentThread().getName() + ", m_name=" + this.m_name + ", isExclusive=" + isExclusive + ", m_currentLockLevel=" + this.m_currentLockLevel + ", m_numReadLocks=" + this.m_numReadLocks);
/*     */     }
/*     */ 
/*  89 */     while (isLockedOut(isExclusive))
/*     */     {
/*  91 */       if ((haveWaited) && (m_auditLocking))
/*     */       {
/*  93 */         System.err.println("ThreadID=" + Thread.currentThread().getName() + ", timed out waiting for lock " + this.m_name + " locking state being held too long");
/*     */       }
/*     */ 
/*  96 */       super.wait(5000L);
/*  97 */       haveWaited = true;
/*     */     }
/*  99 */     this.m_currentLockLevel = ((isExclusive) ? 2 : 1);
/* 100 */     if (isExclusive)
/*     */       return;
/* 102 */     this.m_numReadLocks += 1;
/*     */   }
/*     */ 
/*     */   public synchronized void release(int flags)
/*     */   {
/* 113 */     boolean isExclusive = (flags & 0x2) != 0;
/*     */ 
/* 116 */     if ((isExclusive) && (this.m_currentLockLevel != 2))
/*     */     {
/* 118 */       if (m_auditLocking)
/*     */       {
/* 120 */         System.err.println("ThreadID=" + Thread.currentThread().getName() + ", exclusive release of non exclusive lock " + this.m_name + ".");
/*     */       }
/*     */ 
/*     */     }
/* 124 */     else if ((!isExclusive) && (this.m_numReadLocks <= 0) && 
/* 126 */       (m_auditLocking))
/*     */     {
/* 128 */       System.err.println("ThreadID=" + Thread.currentThread().getName() + ", decrementing past zero for lock " + this.m_name + ".");
/*     */     }
/*     */ 
/* 133 */     if (!isExclusive)
/*     */     {
/* 135 */       if (this.m_numReadLocks > 0)
/*     */       {
/* 137 */         this.m_numReadLocks -= 1;
/*     */       }
/* 139 */       if (this.m_numReadLocks <= 0)
/*     */       {
/* 141 */         this.m_currentLockLevel = 0;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 146 */       this.m_currentLockLevel = 0;
/*     */     }
/* 148 */     if (this.m_currentLockLevel == 0)
/*     */     {
/* 150 */       super.notify();
/*     */     }
/*     */ 
/* 153 */     if (!m_auditLocking)
/*     */       return;
/* 155 */     System.out.println("Release, threadID=" + Thread.currentThread().getName() + ", m_name=" + this.m_name + ", isExclusive=" + isExclusive + ", m_currentLockLevel=" + this.m_currentLockLevel + ", m_numReadLocks=" + this.m_numReadLocks);
/*     */   }
/*     */ 
/*     */   protected boolean isLockedOut(boolean needExclusiveLock)
/*     */   {
/* 164 */     boolean retVal = false;
/* 165 */     if (this.m_currentLockLevel == 2)
/*     */     {
/* 167 */       retVal = true;
/*     */     }
/* 169 */     else if ((needExclusiveLock) && 
/* 171 */       (this.m_numReadLocks > 0))
/*     */     {
/* 173 */       retVal = true;
/*     */     }
/*     */ 
/* 176 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 181 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.LockReadWriteMutex
 * JD-Core Version:    0.5.4
 */