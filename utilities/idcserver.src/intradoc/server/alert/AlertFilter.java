/*    */ package intradoc.server.alert;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataBinderUtils;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.server.Service;
/*    */ import intradoc.shared.FilterImplementor;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.UserData;
/*    */ 
/*    */ public class AlertFilter
/*    */   implements FilterImplementor
/*    */ {
/*    */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*    */     throws DataException, ServiceException
/*    */   {
/* 37 */     String param = (String)cxt.getCachedObject("filterParameter");
/* 38 */     if (param.equals("onEndServiceRequestActions"))
/*    */     {
/* 40 */       Service service = (Service)cxt;
/*    */ 
/* 42 */       if (((!service.isConditionVarTrue("IsSourcePage")) && (!service.isJava())) || (DataBinderUtils.getBoolean(binder, "AlwaysCheckAlert", false)))
/*    */       {
/* 45 */         DataResultSet drset = AlertUtils.getUserAlerts(binder, service.getWorkspace(), service, service.getUserData());
/*    */ 
/* 47 */         if (drset.getNumRows() > 0)
/*    */         {
/* 49 */           binder.addResultSet("USER_ALERTS", drset);
/*    */         }
/*    */ 
/* 56 */         UserData userData = (UserData)cxt.getCachedObject("TargetUserData");
/* 57 */         if (userData != null)
/*    */         {
/* 59 */           String userName = userData.m_name;
/* 60 */           String alertToBeDeleted = SharedObjects.getEnvironmentValue("alertIdToBeDeletedFor" + userName);
/* 61 */           if ((alertToBeDeleted != null) && (alertToBeDeleted.length() > 0))
/*    */           {
/* 64 */             String redirectParams = binder.getLocal("RedirectParams");
/* 65 */             if ((redirectParams == null) || (redirectParams.length() == 0))
/*    */             {
/* 67 */               DataBinder alertbinder = new DataBinder();
/* 68 */               alertbinder.putLocal("alertId", alertToBeDeleted);
/* 69 */               AlertUtils.deleteAlert(alertbinder);
/* 70 */               SharedObjects.removeEnvironmentValue("alertIdToBeDeletedFor" + userName);
/*    */             }
/*    */           }
/*    */         }
/*    */       }
/*    */     }
/*    */ 
/* 77 */     return 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83038 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.alert.AlertFilter
 * JD-Core Version:    0.5.4
 */