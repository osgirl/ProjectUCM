/*    */ package intradoc.server.workflow;
/*    */ 
/*    */ public class WfStateHelper
/*    */ {
/* 25 */   public boolean m_isExit = false;
/* 26 */   public boolean m_isUpdateState = false;
/*    */ 
/* 29 */   public boolean m_isNewStep = false;
/*    */ 
/* 32 */   public String m_wfName = null;
/* 33 */   public String m_stepName = null;
/* 34 */   public String m_lookupKey = null;
/*    */ 
/*    */   public WfStateHelper(String wfName, String stepName)
/*    */   {
/* 39 */     updateWfStepInfo(wfName, stepName);
/*    */   }
/*    */ 
/*    */   public void updateWfStepInfo(String wfName, String stepName)
/*    */   {
/* 44 */     this.m_wfName = wfName;
/* 45 */     this.m_stepName = stepName;
/* 46 */     this.m_lookupKey = (stepName + "@" + wfName);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WfStateHelper
 * JD-Core Version:    0.5.4
 */