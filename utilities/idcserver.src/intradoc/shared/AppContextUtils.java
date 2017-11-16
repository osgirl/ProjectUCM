/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class AppContextUtils
/*    */ {
/*    */   public static void executeService(SharedContext shContext, String action, Properties data)
/*    */     throws ServiceException
/*    */   {
/* 34 */     DataBinder binder = new DataBinder();
/* 35 */     binder.setLocalData(data);
/* 36 */     shContext.executeService(action, binder, false);
/*    */   }
/*    */ 
/*    */   public static void executeService(SharedContext shContext, String action, DataBinder data)
/*    */     throws ServiceException
/*    */   {
/* 42 */     shContext.executeService(action, data, false);
/*    */   }
/*    */ 
/*    */   public static void executeService(SharedContext shContext, String action, Properties data, boolean isRefresh)
/*    */     throws ServiceException
/*    */   {
/* 48 */     DataBinder binder = new DataBinder();
/* 49 */     binder.setLocalData(data);
/* 50 */     shContext.executeService(action, binder, isRefresh);
/*    */   }
/*    */ 
/*    */   public static void executeService(SharedContext shContext, String action, DataBinder data, boolean isRefresh)
/*    */     throws ServiceException
/*    */   {
/* 56 */     shContext.executeService(action, data, isRefresh);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 61 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.AppContextUtils
 * JD-Core Version:    0.5.4
 */