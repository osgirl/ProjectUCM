/*    */ package intradoc.server.jobs;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.Service;
/*    */ 
/*    */ public class ScheduledJobService extends Service
/*    */ {
/*    */   public void createHandlersForService()
/*    */     throws ServiceException, DataException
/*    */   {
/* 29 */     super.createHandlersForService();
/* 30 */     createHandlers("ScheduledJobService");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 35 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66344 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.jobs.ScheduledJobService
 * JD-Core Version:    0.5.4
 */