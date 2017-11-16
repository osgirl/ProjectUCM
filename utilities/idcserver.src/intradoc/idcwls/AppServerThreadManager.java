/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.ThreadManagerInterface;
/*    */ import intradoc.data.DataException;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class AppServerThreadManager
/*    */   implements ThreadManagerInterface
/*    */ {
/*    */   public Object m_workManager;
/*    */ 
/*    */   AppServerThreadManager(Object workManager)
/*    */   {
/* 40 */     this.m_workManager = workManager;
/*    */   }
/*    */ 
/*    */   public Object schedule(Thread t, Map params) throws ServiceException
/*    */   {
/* 45 */     Object retVal = null;
/*    */     try
/*    */     {
/* 48 */       retVal = WorkManagerUtils.scheduleClientInitiatedManagedThread(this.m_workManager, t, params);
/*    */     }
/*    */     catch (DataException e)
/*    */     {
/* 52 */       throw new ServiceException(e, "csServletFailedToScheduleThread", new Object[0]);
/*    */     }
/* 54 */     return retVal;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 59 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69228 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.AppServerThreadManager
 * JD-Core Version:    0.5.4
 */