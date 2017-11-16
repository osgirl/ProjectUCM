/*    */ package intradoc.server.alert;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataBinderUtils;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.server.Action;
/*    */ import intradoc.server.IdcServiceAction;
/*    */ import intradoc.server.Service;
/*    */ import intradoc.server.ServiceHandler;
/*    */ import intradoc.shared.UserData;
/*    */ 
/*    */ public class AlertHandler extends ServiceHandler
/*    */ {
/*    */   @IdcServiceAction
/*    */   public void setUserAlert()
/*    */     throws ServiceException, DataException
/*    */   {
/* 33 */     if (DataBinderUtils.getBoolean(this.m_binder, "isTempAlert", false))
/*    */     {
/* 35 */       this.m_binder.putLocal("flags", "2");
/*    */     }
/* 37 */     AlertUtils.setAlert(this.m_binder);
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void deleteUserAlert() throws ServiceException, DataException
/*    */   {
/* 43 */     AlertUtils.deleteAlert(this.m_binder);
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void getUserAlerts() throws ServiceException, DataException
/*    */   {
/* 49 */     UserData user = this.m_service.getUserData();
/* 50 */     DataResultSet drset = AlertUtils.getUserAlerts(this.m_binder, this.m_workspace, this.m_service, user);
/*    */ 
/* 52 */     String rsName = this.m_currentAction.getParamAt(0);
/* 53 */     this.m_binder.addResultSet(rsName, drset);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.alert.AlertHandler
 * JD-Core Version:    0.5.4
 */