/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import intradoc.shared.FilterImplementor;
/*    */ 
/*    */ public class ExecuteSubServiceFilter
/*    */   implements FilterImplementor
/*    */ {
/*    */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*    */     throws DataException, ServiceException
/*    */   {
/* 33 */     Service s = null;
/* 34 */     if (cxt instanceof Service)
/*    */     {
/* 36 */       s = (Service)cxt;
/*    */     }
/*    */     else
/*    */     {
/* 40 */       s = new Service();
/* 41 */       if (binder == null) {
/* 42 */         binder = new DataBinder();
/*    */       }
/* 44 */       s.init(ws, null, binder, new ServiceData());
/* 45 */       s.initDelegatedObjects();
/*    */     }
/* 47 */     String parameter = (String)cxt.getCachedObject("filterParameter");
/* 48 */     if ((parameter != null) && (parameter.trim().length() == 0))
/*    */     {
/* 50 */       throw new DataException("!csSubServiceNeedsParameter");
/*    */     }
/* 52 */     s.executeService(parameter);
/* 53 */     return 0;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ExecuteSubServiceFilter
 * JD-Core Version:    0.5.4
 */