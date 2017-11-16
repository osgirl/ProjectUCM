/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.shared.SharedObjects;
/*    */ import intradoc.shared.UserData;
/*    */ 
/*    */ public class DocumentSecurityUtils
/*    */ {
/*    */   public static boolean canUpdateOwnershipSecurity(String newOwner, String currentOwner, DataBinder binder, Service service)
/*    */     throws ServiceException, DataException
/*    */   {
/* 45 */     UserData userData = service.getUserData();
/*    */ 
/* 47 */     if ((((currentOwner == null) || (currentOwner.length() == 0) || (currentOwner.equalsIgnoreCase(userData.m_name)))) && ((
/* 50 */       (newOwner.equalsIgnoreCase(userData.m_name)) || (SharedObjects.getEnvValueAsBoolean("AllowOwnerToChangeAuthor", false)))))
/*    */     {
/* 53 */       return true;
/*    */     }
/*    */ 
/* 57 */     return service.checkAccess(binder, 8);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 62 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72978 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocumentSecurityUtils
 * JD-Core Version:    0.5.4
 */