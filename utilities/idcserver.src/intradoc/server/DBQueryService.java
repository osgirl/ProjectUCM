/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ 
/*    */ public class DBQueryService extends Service
/*    */ {
/*    */   @IdcServiceAction
/*    */   public void cancelQuery()
/*    */     throws DataException
/*    */   {
/* 29 */     String query = this.m_binder.getAllowMissing("dbQueryID");
/* 30 */     if ((query == null) || (query.length() == 0))
/*    */     {
/* 32 */       String id = this.m_binder.get("connectionID");
/* 33 */       String threadName = getThreadNameFromConnID(id);
/* 34 */       if (threadName != null)
/*    */       {
/* 36 */         query = this.m_workspace.getActiveQueryID(threadName);
/*    */       }
/*    */       else
/*    */       {
/* 40 */         query = null;
/*    */       }
/*    */     }
/* 43 */     if (query == null)
/*    */       return;
/* 45 */     this.m_workspace.cancel(query);
/*    */   }
/*    */ 
/*    */   protected String getThreadNameFromConnID(String id)
/*    */   {
/* 51 */     if ((id == null) || (id.length() == 0))
/*    */     {
/* 53 */       return null;
/*    */     }
/*    */ 
/* 56 */     String threadName = null;
/* 57 */     int index = id.lastIndexOf('.');
/* 58 */     if (index > 0)
/*    */     {
/* 60 */       threadName = id.substring(0, index);
/*    */     }
/* 62 */     return threadName;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 67 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DBQueryService
 * JD-Core Version:    0.5.4
 */