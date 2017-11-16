/*     */ package intradoc.apps.archiver;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DisplayLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.CollectionData;
/*     */ import intradoc.shared.TableFields;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class GeneralPanel extends ArchiverPanel
/*     */ {
/*     */   protected JButton m_viewBatchBtn;
/*     */   protected JButton m_ruleEditBtn;
/*     */   protected JButton m_optionsEditBtn;
/*     */ 
/*     */   public GeneralPanel()
/*     */   {
/*  44 */     this.m_viewBatchBtn = null;
/*  45 */     this.m_ruleEditBtn = null;
/*  46 */     this.m_optionsEditBtn = null;
/*     */   }
/*     */ 
/*     */   public JPanel initUI()
/*     */   {
/*  51 */     JPanel datePanel = initDatesUI();
/*  52 */     JPanel optPanel = initExportOptionsUI();
/*  53 */     JPanel rulePanel = initImportRulesUI();
/*     */ 
/*  55 */     JPanel wrapper = new PanePanel();
/*     */ 
/*  60 */     wrapper.setLayout(new BorderLayout());
/*  61 */     wrapper.add(datePanel, "North");
/*  62 */     wrapper.add(optPanel, "Center");
/*  63 */     wrapper.add(rulePanel, "South");
/*     */ 
/*  65 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initDatesUI()
/*     */   {
/*  70 */     JPanel pnl = new PanePanel();
/*  71 */     this.m_helper.makePanelGridBag(pnl, 2);
/*     */ 
/*  73 */     CustomLabel eComp = new CustomLabel();
/*  74 */     eComp.setMinWidth(110);
/*  75 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelLastExported", this.m_cxt), eComp, "aLastExport", false);
/*     */ 
/*  78 */     CustomLabel teComp = new CustomLabel();
/*  79 */     teComp.setMinWidth(40);
/*  80 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelTotal", this.m_cxt), teComp, "aTotalLastExported", true);
/*     */ 
/*  83 */     CustomLabel iComp = new CustomLabel();
/*  84 */     iComp.setMinWidth(110);
/*  85 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelLastImported", this.m_cxt), iComp, "aLastImport", false);
/*     */ 
/*  88 */     CustomLabel tiComp = new CustomLabel();
/*  89 */     tiComp.setMinWidth(40);
/*  90 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelTotal", this.m_cxt), tiComp, "aTotalLastImported", true);
/*     */ 
/*  93 */     JPanel btnPanel = new PanePanel();
/*  94 */     this.m_viewBatchBtn = new JButton(LocaleResources.getString("apDlgButtonViewBatchFiles", this.m_cxt));
/*     */ 
/*  96 */     btnPanel.add(this.m_viewBatchBtn);
/*     */ 
/*  98 */     ActionListener viewListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 102 */         ViewBatchDlg dlg = new ViewBatchDlg(GeneralPanel.this.m_systemInterface, LocaleResources.getString("apLabelViewBatchFiles", GeneralPanel.this.m_cxt), GeneralPanel.this.m_collectionContext);
/*     */ 
/* 104 */         dlg.init();
/*     */       }
/*     */     };
/* 107 */     this.m_viewBatchBtn.addActionListener(viewListener);
/*     */ 
/* 110 */     JPanel wrapper = new CustomPanel();
/* 111 */     wrapper.setLayout(new BorderLayout());
/* 112 */     wrapper.add("Center", pnl);
/* 113 */     wrapper.add("East", btnPanel);
/*     */ 
/* 115 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initExportOptionsUI()
/*     */   {
/* 120 */     JPanel optPanel = new PanePanel();
/* 121 */     this.m_helper.makePanelGridBag(optPanel, 1);
/* 122 */     this.m_helper.addPanelTitle(optPanel, LocaleResources.getString("apLabelExportOptions", this.m_cxt));
/*     */ 
/* 124 */     DisplayLabel repComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 125 */     this.m_helper.addLabelFieldPairEx(optPanel, LocaleResources.getString("apLabelReplaceExistingExportFiles", this.m_cxt), repComp, "aDoReplace", false);
/*     */ 
/* 128 */     DisplayLabel copyComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 129 */     this.m_helper.addLabelFieldPairEx(optPanel, LocaleResources.getString("apLabelCopyWebContent", this.m_cxt), copyComp, "aCopyWebDocuments", true);
/*     */ 
/* 132 */     DisplayLabel tableComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 133 */     this.m_helper.addLabelFieldPairEx(optPanel, LocaleResources.getString("apLabelExportTableOnly", this.m_cxt), tableComp, "aExportTableOnly", false);
/*     */ 
/* 137 */     ActionListener edListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 141 */         EditExportOptionsDlg dlg = new EditExportOptionsDlg(GeneralPanel.this.m_systemInterface, LocaleResources.getString("apLabelEditExportOptions", GeneralPanel.this.m_cxt), GeneralPanel.this.m_collectionContext);
/*     */ 
/* 144 */         dlg.init(GeneralPanel.this.m_helper.m_props);
/*     */       }
/*     */     };
/* 149 */     JPanel btnPanel = new PanePanel();
/* 150 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 152 */     this.m_optionsEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 153 */     this.m_optionsEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditExportOptions", this.m_cxt));
/* 154 */     this.m_optionsEditBtn.addActionListener(edListener);
/*     */ 
/* 156 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 157 */     this.m_helper.addLastComponentInRow(btnPanel, this.m_optionsEditBtn);
/*     */ 
/* 160 */     JPanel wrapper = new CustomPanel();
/* 161 */     wrapper.setLayout(new BorderLayout());
/* 162 */     wrapper.add("Center", optPanel);
/* 163 */     wrapper.add("East", btnPanel);
/*     */ 
/* 165 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected JPanel initImportRulesUI()
/*     */   {
/* 170 */     JPanel rulePanel = new PanePanel();
/* 171 */     this.m_helper.makePanelGridBag(rulePanel, 0);
/* 172 */     this.m_helper.addPanelTitle(rulePanel, LocaleResources.getString("apLabelImportOptions", this.m_cxt));
/*     */ 
/* 175 */     DisplayLabel overComp = new DisplayLabel(TableFields.IMPORT_OPTIONLIST, 0);
/* 176 */     overComp.setMinWidth(75);
/* 177 */     this.m_helper.addLabelFieldPairEx(rulePanel, LocaleResources.getString("apLabelOverrideAction", this.m_cxt), overComp, "aOverrideRule", false);
/*     */ 
/* 180 */     DisplayLabel validComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 181 */     validComp.setMinWidth(70);
/* 182 */     this.m_helper.addLabelFieldPairEx(rulePanel, LocaleResources.getString("apLabelImportValidOnly", this.m_cxt), validComp, "aImportValidOnly", false);
/*     */ 
/* 185 */     DisplayLabel trDateComp = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 186 */     trDateComp.setMinWidth(70);
/* 187 */     this.m_helper.addLabelFieldPairEx(rulePanel, LocaleResources.getString("apLabelImportTranslateDate", this.m_cxt), trDateComp, "aTranslateDate", true);
/*     */ 
/* 190 */     DisplayLabel trUseRevClassID = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 191 */     trDateComp.setMinWidth(70);
/* 192 */     this.m_helper.addLabelFieldPairEx(rulePanel, LocaleResources.getString("apLabelImportUseRevClassID", this.m_cxt), trUseRevClassID, "aUseRevclassID", true);
/*     */ 
/* 196 */     DisplayLabel trUseDID = new DisplayLabel(TableFields.YESNO_OPTIONLIST, 1);
/* 197 */     trDateComp.setMinWidth(70);
/* 198 */     this.m_helper.addLabelFieldPairEx(rulePanel, LocaleResources.getString("apLabelImportUseDID", this.m_cxt), trUseDID, "aUseDID", true);
/*     */ 
/* 201 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 202 */     this.m_helper.m_gridHelper.addEmptyRowElement(rulePanel);
/*     */ 
/* 205 */     ActionListener edListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 209 */         EditRulesDlg dlg = new EditRulesDlg(GeneralPanel.this.m_systemInterface, LocaleResources.getString("apLabelEditImportOptions", GeneralPanel.this.m_cxt), GeneralPanel.this.m_collectionContext);
/*     */ 
/* 212 */         dlg.init(GeneralPanel.this.m_helper.m_props);
/*     */       }
/*     */     };
/* 217 */     JPanel btnPanel = new PanePanel();
/* 218 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 220 */     this.m_ruleEditBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 221 */     this.m_ruleEditBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString("apLabelEditImportOptions", this.m_cxt));
/* 222 */     this.m_ruleEditBtn.addActionListener(edListener);
/* 223 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(20, 5, 5, 5);
/* 224 */     this.m_helper.addComponent(btnPanel, this.m_ruleEditBtn);
/*     */ 
/* 227 */     JPanel wrapper = new CustomPanel();
/* 228 */     wrapper.setLayout(new BorderLayout());
/* 229 */     wrapper.add("Center", rulePanel);
/* 230 */     wrapper.add("East", btnPanel);
/*     */ 
/* 232 */     return wrapper;
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean isEnabled)
/*     */   {
/* 238 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/* 239 */     if ((curCollection != null) && (curCollection.isProxied()))
/*     */     {
/* 241 */       this.m_optionsEditBtn.setEnabled(false);
/* 242 */       this.m_ruleEditBtn.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 246 */       this.m_optionsEditBtn.setEnabled(isEnabled);
/* 247 */       this.m_ruleEditBtn.setEnabled(isEnabled);
/*     */     }
/* 249 */     this.m_viewBatchBtn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void exchangeField(String name, DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 259 */     if ((name.equals("aLastExport")) || (name.equals("aLastImport")))
/*     */     {
/* 262 */       if (!updateComponent)
/*     */         return;
/* 264 */       String value = this.m_helper.m_props.getProperty(name);
/*     */       try
/*     */       {
/* 267 */         exchange.m_compValue = LocaleResources.localizeDate(value, this.m_cxt);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 271 */         exchange.m_compValue = value;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 277 */       super.exchangeField(name, exchange, updateComponent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 283 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95262 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.GeneralPanel
 * JD-Core Version:    0.5.4
 */