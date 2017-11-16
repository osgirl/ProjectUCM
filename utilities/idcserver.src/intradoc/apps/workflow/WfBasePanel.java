/*    */ package intradoc.apps.workflow;
/*    */ 
/*    */ import intradoc.apps.shared.BasePanel;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemInterface;
/*    */ 
/*    */ public abstract class WfBasePanel extends BasePanel
/*    */ {
/*    */   protected WorkflowContext m_context;
/*    */ 
/*    */   public WfBasePanel()
/*    */   {
/* 30 */     this.m_context = null;
/*    */   }
/*    */ 
/*    */   public void init(SystemInterface sys, WorkflowContext ctxt) throws ServiceException {
/* 34 */     this.m_context = ctxt;
/* 35 */     super.init(sys);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 40 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfBasePanel
 * JD-Core Version:    0.5.4
 */