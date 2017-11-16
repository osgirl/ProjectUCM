/*    */ package intradoc.idcwls;
/*    */ 
/*    */ import intradoc.admin.AdminDirectServerActions;
/*    */ import intradoc.common.ExecutionContext;
/*    */ import java.io.IOException;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class ServletDirectAdminActions
/*    */   implements AdminDirectServerActions
/*    */ {
/*    */   public void doAction(String action, Map in, Map out, ExecutionContext cxt)
/*    */     throws IOException
/*    */   {
/* 34 */     IdcServletRequestContext servletRequestContext = (IdcServletRequestContext)cxt.getCachedObject("IdcServletRequestContext");
/*    */ 
/* 36 */     if (servletRequestContext == null)
/*    */     {
/* 38 */       out.put("hasNoRequestContext", "1");
/* 39 */       return;
/*    */     }
/*    */ 
/* 42 */     IdcServletConfig servletConfig = servletRequestContext.getServletConfig();
/* 43 */     servletConfig.executeAction(action, in, out);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 48 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70984 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletDirectAdminActions
 * JD-Core Version:    0.5.4
 */