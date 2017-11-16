/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class FileLockData
/*    */ {
/*    */   public static final int UNLOCKED = 0;
/*    */   public static final int LOCKED_BY_THIS_PROCESS = 1;
/*    */   public static final int LOCKED_EXTERNAL_PROVISIONAL = 2;
/*    */   public static final int LOCKED_EXTERNAL = 3;
/*    */   public static final int LOCKED_EXTERNAL_NOT_MAINTAINED = 4;
/* 36 */   String m_fullPath = null;
/*    */ 
/* 40 */   public int m_type = 0;
/*    */ 
/* 45 */   public int m_state = 0;
/*    */ 
/* 48 */   public boolean m_isTempFile = false;
/*    */ 
/* 51 */   public boolean m_isLocked = false;
/*    */ 
/* 55 */   public long m_lastTimestamp = 0L;
/*    */ 
/* 58 */   public long m_lastModified = 0L;
/*    */ 
/* 61 */   public String m_dir = null;
/*    */ 
/* 65 */   public String m_id = null;
/*    */ 
/* 70 */   public String m_agent = null;
/*    */ 
/* 74 */   public boolean m_isProcessed = false;
/*    */ 
/* 77 */   public Vector m_waitingAgents = new IdcVector();
/*    */ 
/* 80 */   public boolean[] m_lockObj = null;
/*    */ 
/*    */   public FileLockData()
/*    */   {
/*    */   }
/*    */ 
/*    */   public FileLockData(String dir, String fullPath, String id, String agent)
/*    */   {
/* 89 */     this.m_fullPath = fullPath;
/* 90 */     this.m_dir = dir;
/* 91 */     this.m_id = id;
/* 92 */     this.m_agent = agent;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 97 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.FileLockData
 * JD-Core Version:    0.5.4
 */