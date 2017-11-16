/*    */ package intradoc.apps.useradmin.irmintg;
/*    */ 
/*    */ import intradoc.gui.CustomCheckbox;
/*    */ 
/*    */ public class IRMFeatureBox extends CustomCheckbox
/*    */ {
/*    */   public String m_id;
/*    */   public String m_label;
/*    */   public String m_toolTip;
/*    */ 
/*    */   public IRMFeatureBox(String id, String label, String toolTip)
/*    */   {
/* 51 */     this.m_id = id;
/* 52 */     this.m_label = label;
/* 53 */     this.m_toolTip = toolTip;
/* 54 */     setText(this.m_label);
/* 55 */     setToolTipText(this.m_toolTip);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 66 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMFeatureBox
 * JD-Core Version:    0.5.4
 */