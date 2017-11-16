/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.text.JTextComponent;
/*     */ 
/*     */ public class JumpSideEffectsPanel extends JumpBasePanel
/*     */ {
/*     */   protected JCheckBox m_rpBox;
/*     */   protected ComboChoice m_rpChoice;
/*     */   protected String[][] SYMBOLIC_STEP_RETURN_LIST;
/*     */ 
/*     */   public JumpSideEffectsPanel()
/*     */   {
/*  44 */     this.m_rpBox = null;
/*  45 */     this.m_rpChoice = null;
/*     */ 
/*  47 */     this.SYMBOLIC_STEP_RETURN_LIST = new String[][] { { "@wfCurrentStep(0)", "apTitleCurrentStep" }, { "@wfCurrentStep(1)", "apTitleNextStep" }, { "@wfCurrentStep(-1)", "apTitlePreviousStep" } };
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  57 */     LocaleResources.localizeDoubleArray(this.SYMBOLIC_STEP_RETURN_LIST, this.m_cxt, 1);
/*  58 */     JPanel pnl = createSideEffectsPanel();
/*     */ 
/*  60 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  61 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/*  62 */     gh.m_gc.weightx = 1.0D;
/*  63 */     gh.m_gc.weighty = 1.0D;
/*  64 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */ 
/*  66 */     load();
/*     */   }
/*     */ 
/*     */   protected JPanel createSideEffectsPanel()
/*     */   {
/*  71 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/*  73 */     JPanel pnl = new PanePanel();
/*  74 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/*  76 */     gh.m_gc.anchor = 18;
/*     */ 
/*  78 */     initReturnUI();
/*     */ 
/*  80 */     gh.prepareAddRowElement();
/*  81 */     this.m_helper.addExchangeComponent(pnl, this.m_rpBox, "HasReturnStep");
/*  82 */     gh.prepareAddLastRowElement();
/*  83 */     this.m_helper.addExchangeComponent(pnl, this.m_rpChoice, "wfJumpReturnStep");
/*     */ 
/*  85 */     JCheckBox rnBox = new CustomCheckbox(LocaleResources.getString("apDontNotifyUsersOnEntry", this.m_cxt));
/*     */ 
/*  87 */     this.m_helper.addExchangeComponent(pnl, rnBox, "wfJumpEntryNotifyOff");
/*  88 */     JCheckBox drBox = new CustomCheckbox(LocaleResources.getString("apWfReleaseDocument", this.m_cxt));
/*     */ 
/*  90 */     this.m_helper.addExchangeComponent(pnl, drBox, "wfReleaseDocument");
/*     */ 
/*  92 */     JPanel cPanel = createCustomPanel();
/*  93 */     this.m_helper.addLastComponentInRow(pnl, cPanel);
/*  94 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initReturnUI()
/*     */   {
/*  99 */     this.m_rpBox = new CustomCheckbox(LocaleResources.getString("apHasReturnPoint", this.m_cxt));
/* 100 */     this.m_rpChoice = new ComboChoice();
/*     */ 
/* 102 */     this.m_rpChoice.initChoiceList(this.SYMBOLIC_STEP_RETURN_LIST);
/*     */ 
/* 104 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 108 */         JumpSideEffectsPanel.this.checkSelection();
/*     */       }
/*     */     };
/* 111 */     this.m_rpBox.addItemListener(iListener);
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 116 */     boolean enable = this.m_rpBox.isSelected();
/* 117 */     this.m_rpChoice.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   protected JPanel createCustomPanel()
/*     */   {
/* 122 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 124 */     JPanel pnl = new PanePanel();
/* 125 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 127 */     gh.m_gc.anchor = 18;
/* 128 */     this.m_helper.addLastComponentInRow(pnl, new CustomLabel(LocaleResources.getString("apTitleCustomEffects", this.m_cxt), 1));
/*     */ 
/* 132 */     gh.m_gc.weightx = 1.0D;
/* 133 */     gh.m_gc.weighty = 1.0D;
/* 134 */     JTextComponent msgText = new CustomTextArea(7, 50);
/* 135 */     this.m_helper.addLastComponentInRow(pnl, msgText);
/* 136 */     this.m_helper.m_exchange.addComponent("wfJumpCustomEffects", msgText, null);
/*     */ 
/* 138 */     return pnl;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/* 143 */     String rp = this.m_helper.m_props.getProperty("wfJumpReturnStep");
/* 144 */     boolean hasReturnStep = (rp != null) && (rp.length() > 0);
/*     */ 
/* 146 */     this.m_helper.m_props.put("HasReturnStep", "" + hasReturnStep);
/* 147 */     this.m_rpBox.setSelected(hasReturnStep);
/* 148 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 153 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.JumpSideEffectsPanel
 * JD-Core Version:    0.5.4
 */