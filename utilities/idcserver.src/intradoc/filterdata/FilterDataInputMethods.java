/*    */ package intradoc.filterdata;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.common.IdcStringBuilder;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.Service;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class FilterDataInputMethods
/*    */ {
/* 34 */   public static FilterDataInputEventImplement m_eventImplement = new FilterDataInputEventImplement();
/*    */ 
/*    */   public static void filterServiceDataInput(DataBinder binder, Service service, int flags)
/*    */     throws DataException, ServiceException
/*    */   {
/* 42 */     m_eventImplement.checkInit(service);
/* 43 */     IdcStringBuilder sb = new IdcStringBuilder();
/* 44 */     m_eventImplement.filterEnvironmentData(binder, service, flags, sb);
/* 45 */     Map localData = binder.getLocalData();
/* 46 */     m_eventImplement.filterDataInput(localData, localData, service, flags, sb);
/* 47 */     sb.releaseBuffers();
/*    */   }
/*    */ 
/*    */   public static void filterDataInput(Map inData, Map outdata, ExecutionContext cxt, int flags)
/*    */     throws DataException, ServiceException
/*    */   {
/* 58 */     m_eventImplement.checkInit(cxt);
/* 59 */     IdcStringBuilder sb = new IdcStringBuilder();
/* 60 */     m_eventImplement.filterDataInput(inData, outdata, cxt, flags, sb);
/* 61 */     sb.releaseBuffers();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 68 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filterdata.FilterDataInputMethods
 * JD-Core Version:    0.5.4
 */