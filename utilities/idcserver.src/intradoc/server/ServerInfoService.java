/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ 
/*    */ public class ServerInfoService extends Service
/*    */ {
/*    */   public void createHandlersForService()
/*    */     throws ServiceException, DataException
/*    */   {
/* 30 */     super.createHandlersForService();
/* 31 */     createHandlers("ServerInfoService");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 36 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServerInfoService
 * JD-Core Version:    0.5.4
 */