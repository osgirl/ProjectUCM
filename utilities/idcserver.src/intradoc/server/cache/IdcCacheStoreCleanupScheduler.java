/*    */ package intradoc.server.cache;
/*    */ 
/*    */ import intradoc.common.Report;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import java.util.TimerTask;
/*    */ 
/*    */ public class IdcCacheStoreCleanupScheduler extends TimerTask
/*    */ {
/*    */   private Workspace m_workspace;
/*    */ 
/*    */   public void setWorkspace(Workspace ws)
/*    */   {
/* 41 */     this.m_workspace = ws;
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/*    */     try
/*    */     {
/* 49 */       if (this.m_workspace != null)
/*    */       {
/* 51 */         cleanupCacheStore();
/*    */       }
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 56 */       Report.trace("idccache", "Unable to cleanup deleted entries from persistent cache store", e);
/*    */     }
/*    */   }
/*    */ 
/*    */   private void cleanupCacheStore()
/*    */     throws DataException
/*    */   {
/*    */     try
/*    */     {
/* 68 */       synchronized (this.m_workspace)
/*    */       {
/* 70 */         this.m_workspace.execute("DcacheStoreDeleteStatus", null);
/*    */       }
/*    */     }
/*    */     catch (Exception e)
/*    */     {
/*    */       String errMsg;
/* 76 */       throw new DataException(errMsg, e);
/*    */     }
/*    */     finally
/*    */     {
/* 80 */       this.m_workspace.releaseConnection();
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheStoreCleanupScheduler
 * JD-Core Version:    0.5.4
 */