/*    */ package intradoc.apps.archiver;
/*    */ 
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.SystemInterface;
/*    */ import intradoc.gui.DialogHelper;
/*    */ import intradoc.gui.GridBagHelper;
/*    */ import javax.swing.JCheckBox;
/*    */ import javax.swing.JPanel;
/*    */ 
/*    */ public class EditAdditionalDlg extends EditDlg
/*    */ {
/*    */   public EditAdditionalDlg(SystemInterface sys, String title, CollectionContext context)
/*    */   {
/* 34 */     super(sys, title, context, "EditAdditionalExportData");
/*    */ 
/* 36 */     this.m_editItems = "aExportDocConfig,aExportUserConfig";
/* 37 */     this.m_errMsg = LocaleResources.getString("apErrorEditingArchive", this.m_cxt);
/*    */   }
/*    */ 
/*    */   public void initUI(JPanel pnl)
/*    */   {
/* 44 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 45 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apExportContentConfigInfo", this.m_cxt)), "aExportDocConfig");
/*    */ 
/* 47 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 48 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apExportUserConfigInfo", this.m_cxt)), "aExportUserConfig");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditAdditionalDlg
 * JD-Core Version:    0.5.4
 */