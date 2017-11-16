/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.ExecutionContext;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.ResultSet;
/*    */ 
/*    */ public class BasicTopicImplementor
/*    */   implements TopicImplementor
/*    */ {
/*    */   public void init(DataBinder data)
/*    */   {
/*    */   }
/*    */ 
/*    */   public ResultSet retrieveResultSet(String rName, DataBinder binder, ExecutionContext cxt)
/*    */   {
/* 32 */     DataResultSet drset = (DataResultSet)binder.getResultSet(rName);
/* 33 */     if (drset != null)
/*    */     {
/* 35 */       drset = drset.shallowClone();
/*    */     }
/* 37 */     return drset;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 42 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.BasicTopicImplementor
 * JD-Core Version:    0.5.4
 */