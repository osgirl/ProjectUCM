/*    */ package intradoc.apps.archiver;
/*    */ 
/*    */ import intradoc.common.LocaleResources;
/*    */ import intradoc.common.StringUtils;
/*    */ import intradoc.common.SystemInterface;
/*    */ import intradoc.gui.CustomText;
/*    */ import intradoc.gui.DialogHelper;
/*    */ import intradoc.gui.GridBagHelper;
/*    */ import java.util.Properties;
/*    */ import javax.swing.JCheckBox;
/*    */ import javax.swing.JPanel;
/*    */ 
/*    */ public class EditTransferOptionsDlg extends EditDlg
/*    */ {
/*    */   public EditTransferOptionsDlg(SystemInterface sys, String title, CollectionContext context)
/*    */   {
/* 36 */     super(sys, title, context, "EditTransferOptions");
/* 37 */     this.m_editItems = "aIsTargetable,aIsAutomatedTransfer";
/* 38 */     this.m_action = "EDIT_TRANSFEROPTIONS";
/*    */   }
/*    */ 
/*    */   public void initUI(JPanel pnl)
/*    */   {
/* 44 */     CustomText txtField = new CustomText(LocaleResources.getString("apDoesArchiveAllowTransferByAnotherArchive", this.m_cxt), 60);
/*    */ 
/* 46 */     JCheckBox chkBox = new JCheckBox(LocaleResources.getString("apLabelIsTargetable", this.m_cxt));
/*    */ 
/* 48 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 49 */     this.m_helper.addComponent(pnl, txtField);
/* 50 */     this.m_helper.addExchangeComponent(pnl, chkBox, "aIsTargetable");
/*    */ 
/* 52 */     txtField = new CustomText(LocaleResources.getString("apIsArchiveTransferAutomated", this.m_cxt), 60);
/* 53 */     chkBox = new JCheckBox(LocaleResources.getString("apLabelIsTransferAutomated", this.m_cxt));
/*    */ 
/* 55 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 56 */     this.m_helper.addComponent(pnl, txtField);
/* 57 */     this.m_helper.addExchangeComponent(pnl, chkBox, "aIsAutomatedTransfer");
/*    */   }
/*    */ 
/*    */   public boolean prepareOkEvent()
/*    */   {
/* 63 */     boolean isAutomated = StringUtils.convertToBool(this.m_helper.m_props.getProperty("aIsAutomatedTransfer"), false);
/* 64 */     if (isAutomated)
/*    */     {
/* 66 */       String targetArchive = this.m_helper.m_props.getProperty("aTargetArchive");
/* 67 */       if ((targetArchive == null) || (targetArchive.length() == 0))
/*    */       {
/* 69 */         this.m_collectionContext.reportError(LocaleResources.getString("apMustDefineTargetBeforeAutomation", this.m_cxt));
/*    */ 
/* 71 */         return false;
/*    */       }
/*    */     }
/* 74 */     this.m_helper.m_props.put("isEditAutomated", "1");
/* 75 */     return true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 80 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditTransferOptionsDlg
 * JD-Core Version:    0.5.4
 */