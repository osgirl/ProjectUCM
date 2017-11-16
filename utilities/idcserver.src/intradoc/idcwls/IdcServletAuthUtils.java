/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class IdcServletAuthUtils
/*    */ {
/* 25 */   public static String m_ssoRequestHeader = "Osso-User-Guid";
/* 26 */   public static String m_ssoRedirectStartLines = "HTTP/1.1 470 Oracle SSO";
/* 27 */   public static String m_ssoRedirectHeaderName = "Osso-Return-Url";
/*    */ 
/*    */   public static void init()
/*    */   {
/* 31 */     String val = SharedObjects.getEnvironmentValue("SSORequestHeader");
/* 32 */     if ((val != null) && (val.length() > 0))
/*    */     {
/* 34 */       m_ssoRequestHeader = val;
/*    */     }
/*    */ 
/* 37 */     val = SharedObjects.getEnvironmentValue("SSORedirectStartLines");
/* 38 */     if ((val != null) && (val.length() > 0))
/*    */     {
/* 40 */       m_ssoRedirectStartLines = val;
/*    */     }
/*    */ 
/* 43 */     val = SharedObjects.getEnvironmentValue("SSORedirectHeaderName");
/* 44 */     if ((val == null) || (val.length() <= 0))
/*    */       return;
/* 46 */     m_ssoRedirectHeaderName = val;
/*    */   }
/*    */ 
/*    */   public static void logout(DataBinder binder, IdcServletRequestContext cxt)
/*    */   {
/* 52 */     cxt.logout();
/*    */ 
/* 54 */     if (cxt.getRequestHeader(m_ssoRequestHeader) != null)
/*    */     {
/* 57 */       binder.putLocal("RedirectHttpStartLines", m_ssoRedirectStartLines);
/*    */ 
/* 62 */       binder.putLocal("RedirectUrl", SharedObjects.getEnvironmentValue("HttpRelativeWebRoot"));
/*    */ 
/* 65 */       binder.putLocal("RedirectHeaderName", m_ssoRedirectHeaderName);
/*    */     }
/*    */     else
/*    */     {
/* 69 */       String logoutURL = SharedObjects.getEnvironmentValue("LogoutServerUrl");
/* 70 */       if (logoutURL == null)
/*    */         return;
/* 72 */       binder.putLocal("RedirectUrl", logoutURL);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 79 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82626 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletAuthUtils
 * JD-Core Version:    0.5.4
 */