/*    */ import intradoc.common.IdcMessageFactory;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemUtils;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.IdcServerManager;
/*    */ 
/*    */ public class IdcServer
/*    */ {
/* 28 */   protected static Workspace m_workspace = null;
/* 29 */   protected static IdcServerManager m_serverManager = new IdcServerManager();
/*    */   protected static int m_port;
/*    */ 
/*    */   public static void main(String[] args)
/*    */   {
/* 34 */     IdcServer server = new IdcServer();
/*    */     try
/*    */     {
/* 37 */       server.init();
/*    */     }
/*    */     catch (Throwable t)
/*    */     {
/* 41 */       SystemUtils.handleFatalException(t, IdcMessageFactory.lc("csFailedToInitServer", new Object[0]), -1);
/*    */     }
/*    */   }
/*    */ 
/*    */   public void init()
/*    */     throws DataException, ServiceException
/*    */   {
/* 51 */     m_serverManager.init();
/* 52 */     m_serverManager.serviceStart(0, 0);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     IdcServer
 * JD-Core Version:    0.5.4
 */