/*    */ package intradoc.server.subject;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.SubjectCallbackAdapter;
/*    */ import intradoc.server.UserStorage;
/*    */ 
/*    */ public class UserTempCacheSubjectCallback extends SubjectCallbackAdapter
/*    */   implements Runnable
/*    */ {
/*    */   protected Thread m_threadHandle;
/*    */ 
/*    */   public UserTempCacheSubjectCallback()
/*    */   {
/* 28 */     this.m_threadHandle = null;
/*    */   }
/*    */ 
/*    */   public void refresh(String subject) throws DataException, ServiceException
/*    */   {
/* 33 */     if (UserStorage.m_userStorageImpl == null)
/*    */     {
/* 35 */       return;
/*    */     }
/*    */ 
/* 39 */     checkIfWorking();
/* 40 */     if ((this.m_workspace == null) || (this.m_threadHandle != null))
/*    */       return;
/* 42 */     Thread bgThread = new Thread(this, "UserTempCacheRefreshThread");
/* 43 */     bgThread.setPriority(1);
/* 44 */     bgThread.setDaemon(true);
/* 45 */     this.m_threadHandle = bgThread;
/* 46 */     bgThread.start();
/*    */   }
/*    */ 
/*    */   private void checkIfWorking()
/*    */   {
/* 52 */     if (this.m_threadHandle == null)
/*    */       return;
/* 54 */     Thread threadCopy = this.m_threadHandle;
/* 55 */     if (threadCopy.isAlive()) {
/*    */       return;
/*    */     }
/* 58 */     this.m_threadHandle = null;
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/* 66 */     UserStorage.refreshCachedUserData();
/* 67 */     Report.trace("userstorage", "External load stamp marked to current time for refresh on next load", null);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 73 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104244 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.UserTempCacheSubjectCallback
 * JD-Core Version:    0.5.4
 */