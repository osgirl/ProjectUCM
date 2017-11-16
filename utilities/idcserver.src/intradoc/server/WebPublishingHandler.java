/*    */ package intradoc.server;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.server.publish.WebPublishUtils;
/*    */ 
/*    */ public class WebPublishingHandler extends ServiceHandler
/*    */ {
/*    */   @IdcServiceAction
/*    */   public void publishDynamicFiles()
/*    */     throws DataException, ServiceException
/*    */   {
/* 37 */     WebPublishUtils.doPublish(this.m_workspace, this.m_service, 1);
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void publishWeblayoutFiles() throws DataException, ServiceException
/*    */   {
/* 43 */     WebPublishUtils.doPublish(this.m_workspace, this.m_service, 8);
/*    */   }
/*    */ 
/*    */   @IdcServiceAction
/*    */   public void publishStaticFiles() throws DataException, ServiceException
/*    */   {
/* 49 */     WebPublishUtils.doPublish(this.m_workspace, this.m_service, 16);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83596 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WebPublishingHandler
 * JD-Core Version:    0.5.4
 */