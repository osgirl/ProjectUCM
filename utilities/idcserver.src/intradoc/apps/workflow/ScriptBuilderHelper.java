/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.QueryBuilderHelper;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ScriptBuilderHelper extends QueryBuilderHelper
/*     */ {
/*  42 */   protected JButton m_selTargetBtn = null;
/*  43 */   protected ComboChoice m_targetChoice = null;
/*  44 */   protected ActionListener m_targetListener = null;
/*  45 */   protected boolean m_isCriteria = false;
/*     */ 
/*  47 */   protected String[][] SYMBOLIC_STEP_LIST = { { "@wfCurrentStep(1)", "apTitleNextStep" }, { "@wfCurrentStep(-1)", "apTitlePreviousStep" }, { "@wfCurrentStep(0)", "apTitleRestartStep" }, { "@WfStart", "apTitleRestartWorkflow" }, { "@wfExit(0, 0)", "apTitleExitToParentStep" } };
/*     */ 
/*     */   public ScriptBuilderHelper()
/*     */   {
/*  59 */     this.m_useCustomQuery = true;
/*     */   }
/*     */ 
/*     */   public ScriptBuilderHelper(ActionListener targetListener, boolean isCriteria)
/*     */   {
/*  64 */     this.m_useCustomQuery = false;
/*  65 */     this.m_targetListener = targetListener;
/*  66 */     this.m_isCriteria = isCriteria;
/*     */   }
/*     */ 
/*     */   public JPanel createStandardScriptPanel(ContainerHelper guiHelper, JPanel queryDefinitionPanel, SharedContext shContext)
/*     */   {
/*  72 */     this.m_sharedContext = shContext;
/*  73 */     this.m_cxt = guiHelper.m_exchange.m_sysInterface.getExecutionContext();
/*  74 */     LocaleResources.localizeDoubleArray(this.SYMBOLIC_STEP_LIST, this.m_cxt, 1);
/*     */ 
/*  76 */     JPanel pnl = createStandardClausePanel(guiHelper, queryDefinitionPanel, LocaleResources.getString("apTitleScriptClauses", this.m_cxt));
/*     */ 
/*  80 */     GridBagHelper gh = this.m_guiHelper.m_gridHelper;
/*  81 */     gh.m_gc.anchor = 18;
/*  82 */     JPanel stepPanel = new PanePanel();
/*  83 */     this.m_guiHelper.makePanelGridBag(stepPanel, 2);
/*     */ 
/*  85 */     boolean hasStepSelect = this.m_targetListener != null;
/*  86 */     this.m_targetChoice = new ComboChoice();
/*     */ 
/*  88 */     String[][] stepList = (String[][])null;
/*  89 */     if ((this.m_isCriteria) || (this.m_targetListener == null))
/*     */     {
/*  91 */       stepList = this.SYMBOLIC_STEP_LIST;
/*     */     }
/*     */     else
/*     */     {
/*  96 */       int len = this.SYMBOLIC_STEP_LIST.length;
/*  97 */       stepList = new String[2][len - 1];
/*  98 */       for (int i = 0; i < len - 1; ++i)
/*     */       {
/* 100 */         stepList[0][i] = this.SYMBOLIC_STEP_LIST[0][i];
/* 101 */         stepList[1][i] = this.SYMBOLIC_STEP_LIST[1][i];
/*     */       }
/*     */     }
/* 104 */     this.m_targetChoice.initChoiceList(stepList);
/*     */ 
/* 106 */     this.m_guiHelper.addLabelFieldPairEx(stepPanel, LocaleResources.getString("apLabelTargetStep", this.m_cxt), this.m_targetChoice, "wfJumpTargetStep", !hasStepSelect);
/*     */ 
/* 109 */     if (hasStepSelect)
/*     */     {
/* 111 */       this.m_selTargetBtn = new JButton(LocaleResources.getString("apDlgButtonSelect", this.m_cxt));
/* 112 */       this.m_selTargetBtn.addActionListener(this.m_targetListener);
/* 113 */       gh.m_gc.weightx = 0.1D;
/* 114 */       gh.m_gc.insets = new Insets(0, 20, 0, 0);
/* 115 */       this.m_guiHelper.addLastComponentInRow(stepPanel, this.m_selTargetBtn);
/*     */     }
/*     */ 
/* 118 */     gh.m_gc.insets = new Insets(20, 5, 0, 5);
/* 119 */     this.m_guiHelper.addLastComponentInRow(pnl, stepPanel);
/*     */ 
/* 121 */     return pnl;
/*     */   }
/*     */ 
/*     */   public String handleDateValue(String str, boolean updateComponent)
/*     */   {
/* 127 */     if (str.indexOf("(") >= 0)
/*     */     {
/* 129 */       return str;
/*     */     }
/* 131 */     return super.handleDateValue(str, updateComponent);
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean loadSelection)
/*     */   {
/* 137 */     boolean isCustom = this.m_clauseData.m_isCustom;
/* 138 */     if (this.m_targetChoice != null)
/*     */     {
/* 140 */       this.m_targetChoice.setEnabled(!isCustom);
/*     */     }
/*     */ 
/* 143 */     if (this.m_selTargetBtn != null)
/*     */     {
/* 145 */       this.m_selTargetBtn.setEnabled(!isCustom);
/*     */     }
/* 147 */     super.enableDisable(loadSelection);
/*     */   }
/*     */ 
/*     */   public void enableDisableOnSave()
/*     */   {
/* 153 */     String val = this.m_guiHelper.m_exchange.getComponentValue("wfJumpTargetStep");
/* 154 */     setQueryProp("wfJumpTargetStep", val);
/*     */ 
/* 156 */     enableDisable(true);
/*     */   }
/*     */ 
/*     */   public void updateTargetStep(String step)
/*     */   {
/* 161 */     this.m_guiHelper.m_exchange.setComponentValue("wfJumpTargetStep", step);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 171 */     String val = exchange.m_compValue;
/*     */ 
/* 173 */     if (val != null)
/*     */     {
/* 175 */       val = val.trim();
/* 176 */       if (val.length() == 0)
/*     */       {
/* 178 */         val = null;
/*     */       }
/*     */     }
/* 181 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 186 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81489 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.ScriptBuilderHelper
 * JD-Core Version:    0.5.4
 */