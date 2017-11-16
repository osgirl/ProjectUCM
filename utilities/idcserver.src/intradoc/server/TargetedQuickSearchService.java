/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.Workspace;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ public class TargetedQuickSearchService extends Service
/*    */ {
/*    */   public void init(Workspace ws, OutputStream output, DataBinder binder, ServiceData serviceData)
/*    */     throws DataException
/*    */   {
/* 38 */     super.init(ws, output, binder, serviceData);
/*    */   }
/*    */ 
/*    */   public void createHandlersForService()
/*    */     throws ServiceException, DataException
/*    */   {
/* 44 */     super.createHandlersForService();
/* 45 */     createHandlers("TargetedQuickSearchService");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.TargetedQuickSearchService
 * JD-Core Version:    0.5.4
 */