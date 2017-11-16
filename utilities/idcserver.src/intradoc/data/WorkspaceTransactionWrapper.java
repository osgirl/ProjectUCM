/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.IdcTransactionListener;
/*    */ import intradoc.common.ServiceException;
/*    */ 
/*    */ public class WorkspaceTransactionWrapper
/*    */   implements IdcTransactionListener
/*    */ {
/*    */   protected Workspace m_workspace;
/*    */ 
/*    */   public WorkspaceTransactionWrapper(Workspace ws)
/*    */   {
/* 33 */     this.m_workspace = ws;
/*    */   }
/*    */ 
/*    */   public void beginTransaction(int flags, ExecutionContext context)
/*    */     throws ServiceException
/*    */   {
/*    */     try
/*    */     {
/* 41 */       if ((flags & 0x1) == 1)
/*    */       {
/* 43 */         this.m_workspace.beginTranEx(1);
/*    */       }
/*    */       else
/*    */       {
/* 47 */         int tranType = 4;
/*    */ 
/* 49 */         if (context != null)
/*    */         {
/* 51 */           Boolean useSoftTran = (Boolean)context.getCachedObject("UseSoftTran");
/* 52 */           if ((useSoftTran != null) && (!useSoftTran.booleanValue()))
/*    */           {
/* 54 */             tranType = 2;
/*    */           }
/*    */         }
/* 57 */         this.m_workspace.beginTranEx(tranType);
/*    */       }
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 62 */       throw new ServiceException(e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void commitTransaction(int flags, ExecutionContext context)
/*    */     throws ServiceException
/*    */   {
/*    */     try
/*    */     {
/* 71 */       this.m_workspace.commitTran();
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 75 */       throw new ServiceException(e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void rollbackTransaction(int flags, ExecutionContext context)
/*    */     throws ServiceException
/*    */   {
/* 82 */     this.m_workspace.rollbackTran();
/*    */   }
/*    */ 
/*    */   public void closeTransactionListener(int flags, ExecutionContext context)
/*    */   {
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 95 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77316 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.WorkspaceTransactionWrapper
 * JD-Core Version:    0.5.4
 */