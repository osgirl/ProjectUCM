/*    */ package intradoc.data;
/*    */ 
/*    */ import java.util.Map;
/*    */ 
/*    */ public class WorkspaceCallbackAdaptor
/*    */   implements WorkspaceCallback
/*    */ {
/*    */   public WorkspaceCallbackStatus callback(WorkspaceEventID eventID, Map data)
/*    */   {
/* 27 */     return WorkspaceCallbackStatus.CONTINUE;
/*    */   }
/*    */ 
/*    */   public boolean canHandle(WorkspaceEventID eventID, Map data)
/*    */   {
/* 32 */     return false;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 37 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.WorkspaceCallbackAdaptor
 * JD-Core Version:    0.5.4
 */