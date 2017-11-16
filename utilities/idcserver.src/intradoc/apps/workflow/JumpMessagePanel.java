/*    */ package intradoc.apps.workflow;
/*    */ 
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.gui.ContainerHelper;
/*    */ import intradoc.gui.CustomLabel;
/*    */ import intradoc.gui.CustomTextArea;
/*    */ import intradoc.gui.DynamicComponentExchange;
/*    */ import intradoc.gui.GridBagHelper;
/*    */ import intradoc.gui.PanePanel;
/*    */ import java.awt.Insets;
/*    */ import javax.swing.JPanel;
/*    */ import javax.swing.text.JTextComponent;
/*    */ 
/*    */ public class JumpMessagePanel extends JumpBasePanel
/*    */ {
/*    */   public void initUI()
/*    */   {
/* 42 */     JPanel pnl = createMessagePanel();
/*    */ 
/* 44 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 45 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/* 46 */     gh.m_gc.weightx = 1.0D;
/* 47 */     gh.m_gc.weighty = 1.0D;
/* 48 */     this.m_helper.addLastComponentInRow(this, pnl);
/*    */   }
/*    */ 
/*    */   protected JPanel createMessagePanel()
/*    */   {
/* 53 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*    */ 
/* 55 */     JPanel pnl = new PanePanel();
/* 56 */     this.m_helper.makePanelGridBag(pnl, 1);
/*    */ 
/* 58 */     gh.m_gc.anchor = 18;
/* 59 */     this.m_helper.addLastComponentInRow(pnl, new CustomLabel(LocaleResources.getString("apTypeInMessageString", this.m_cxt), 1));
/*    */ 
/* 63 */     gh.m_gc.weightx = 1.0D;
/* 64 */     gh.m_gc.weighty = 1.0D;
/* 65 */     JTextComponent msgText = new CustomTextArea(7, 50);
/* 66 */     this.m_helper.addLastComponentInRow(pnl, msgText);
/* 67 */     this.m_helper.m_exchange.addComponent("wfJumpMessage", msgText, null);
/*    */ 
/* 69 */     return pnl;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 74 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.JumpMessagePanel
 * JD-Core Version:    0.5.4
 */