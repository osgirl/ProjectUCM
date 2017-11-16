/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemUtils;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class PeriodicTasks extends Thread
/*    */ {
/*    */   public CryptoHashManager m_cryptoHashManager;
/*    */ 
/*    */   public void init()
/*    */     throws ServiceException
/*    */   {
/* 37 */     this.m_cryptoHashManager = new CryptoHashManager();
/* 38 */     this.m_cryptoHashManager.init();
/*    */ 
/* 41 */     Thread t = new Thread(this, "PeriodicTasks");
/* 42 */     t.setDaemon(true);
/* 43 */     t.start();
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/* 49 */     SystemUtils.registerSynchronizationObjectToNotifyOnStop(this);
/* 50 */     while (!SystemUtils.m_isServerStopped)
/*    */     {
/*    */       try
/*    */       {
/* 54 */         processWork();
/* 55 */         SystemUtils.sleep(1800000L);
/*    */       }
/*    */       catch (Exception e)
/*    */       {
/* 59 */         Report.trace("system", null, e);
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   public void processWork() throws ServiceException, DataException
/*    */   {
/* 66 */     this.m_cryptoHashManager.checkUpdateHashes();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 71 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82634 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PeriodicTasks
 * JD-Core Version:    0.5.4
 */