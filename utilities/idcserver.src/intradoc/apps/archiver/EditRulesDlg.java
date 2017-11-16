/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.shared.TableFields;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditRulesDlg extends EditDlg
/*     */   implements ItemListener
/*     */ {
/*  39 */   protected DisplayChoice m_rulesChoice = null;
/*  40 */   protected CustomText m_descriptionField = null;
/*     */ 
/*  42 */   protected String[][] RULES_DESCRIPTION = { { "update", "apUpdateRulesDesc" }, { "insertRev", "apInsertRevRulesDesc" }, { "insertCreate", "apInsertCreateRulesDesc" }, { "deleteRev", "apDeleteRevRulesDesc" }, { "deleteDoc", "apDeleteDocRulesDesc" } };
/*     */ 
/*     */   public EditRulesDlg(SystemInterface sys, String title, CollectionContext context)
/*     */   {
/*  54 */     super(sys, title, context, "EditImportOptions");
/*  55 */     LocaleResources.localizeDoubleArray(this.RULES_DESCRIPTION, this.m_cxt, 1);
/*  56 */     this.m_editItems = "aOverrideRule,aImportValidOnly,aTranslateDate,aUseRevclassID,aUseDID";
/*  57 */     this.m_errMsg = LocaleResources.getString("apErrorEditingImportRules", this.m_cxt);
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel pnl)
/*     */   {
/*  63 */     this.m_rulesChoice = new DisplayChoice();
/*  64 */     this.m_rulesChoice.init(TableFields.IMPORT_OPTIONLIST);
/*  65 */     this.m_rulesChoice.addItemListener(this);
/*     */ 
/*  67 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  68 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelOverrideImportRules", this.m_cxt), this.m_rulesChoice, "aOverrideRule");
/*     */ 
/*  71 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  72 */     this.m_descriptionField = new CustomText()
/*     */     {
/*     */       public Dimension getPreferredSize()
/*     */       {
/*  77 */         Dimension d = super.getPreferredSize();
/*     */ 
/*  79 */         d.width = (this.m_maxWidth + this.m_marginWidth);
/*  80 */         d.height = (6 * this.m_lineHeight + this.m_marginHeight);
/*     */ 
/*  82 */         return d;
/*     */       }
/*     */     };
/*  85 */     this.m_descriptionField.setMaxColumns(75);
/*  86 */     this.m_helper.addComponent(pnl, this.m_descriptionField);
/*     */ 
/*  88 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  89 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apImportOnlyRevsWithValidOptionListValues", this.m_cxt)), "aImportValidOnly");
/*     */ 
/*  93 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apImportTranslateDate", this.m_cxt)), "aTranslateDate");
/*     */ 
/*  97 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apImportUseRevclassID", this.m_cxt)), "aUseRevclassID");
/*     */ 
/* 101 */     this.m_helper.addExchangeComponent(pnl, new JCheckBox(LocaleResources.getString("apImportUseDID", this.m_cxt)), "aUseDID");
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 109 */     this.m_helper.loadComponentValues();
/* 110 */     checkSelection();
/* 111 */     this.m_helper.show();
/* 112 */     return this.m_helper.m_result;
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 117 */     String selRule = this.m_rulesChoice.getSelectedInternalValue();
/* 118 */     if (selRule == null)
/*     */     {
/* 120 */       return;
/*     */     }
/*     */ 
/* 123 */     String text = null;
/* 124 */     for (int i = 0; i < this.RULES_DESCRIPTION.length; ++i)
/*     */     {
/* 126 */       String rule = this.RULES_DESCRIPTION[i][0];
/* 127 */       if (!rule.equals(selRule))
/*     */         continue;
/* 129 */       text = this.RULES_DESCRIPTION[i][1];
/*     */     }
/*     */ 
/* 133 */     if (text == null)
/*     */       return;
/* 135 */     this.m_descriptionField.setText(text);
/* 136 */     this.m_helper.m_dialog.invalidate();
/* 137 */     this.m_helper.m_dialog.pack();
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 143 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 148 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95262 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditRulesDlg
 * JD-Core Version:    0.5.4
 */