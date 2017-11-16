/*    */ package intradoc.apps.useradmin.irmintg;
/*    */ 
/*    */ import intradoc.gui.TabPanel;
/*    */ import intradoc.gui.iwt.IdcTabbedPane;
/*    */ 
/*    */ public class IRMTabbedPane extends TabPanel
/*    */ {
/*    */   public void setEnabledAt(int index, boolean isEnabled)
/*    */   {
/* 35 */     this.m_tabPane.setEnabledAt(index, isEnabled);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 45 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMTabbedPane
 * JD-Core Version:    0.5.4
 */