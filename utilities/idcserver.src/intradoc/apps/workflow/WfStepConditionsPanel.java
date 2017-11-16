/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.workflow.JumpClausesData;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ import javax.swing.event.DocumentEvent;
/*     */ import javax.swing.event.DocumentListener;
/*     */ import javax.swing.text.Document;
/*     */ 
/*     */ public class WfStepConditionsPanel extends EditViewBase
/*     */   implements ActionListener
/*     */ {
/*     */   protected JTextField m_weightFld;
/*     */   protected String m_prevWeight;
/*     */   protected boolean m_isTemplate;
/*     */   protected JCheckBox m_exitConditionBox;
/*     */   protected JTextArea m_exitConditionText;
/*     */ 
/*     */   public WfStepConditionsPanel()
/*     */   {
/*  56 */     this.m_weightFld = null;
/*  57 */     this.m_prevWeight = "1";
/*  58 */     this.m_isTemplate = false;
/*     */ 
/*  60 */     this.m_exitConditionBox = null;
/*  61 */     this.m_exitConditionText = null;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  66 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/*  68 */     JPanel revPanel = initReviewersPanel();
/*  69 */     JPanel exitPanel = initExitConditionsPanel();
/*     */ 
/*  71 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*  72 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  73 */     this.m_helper.addLastComponentInRow(this, revPanel);
/*     */ 
/*  75 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  76 */     this.m_helper.addLastComponentInRow(this, exitPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel initReviewersPanel()
/*     */   {
/*  81 */     JPanel pnl = new CustomPanel();
/*  82 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/*  84 */     ButtonGroup group = new ButtonGroup();
/*  85 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/*  86 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  87 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apTitleRequiredApprovers", this.m_cxt), 1));
/*     */ 
/*  90 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 10, 2, 10);
/*  91 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*  92 */     JCheckBox isAllBox = new CustomCheckbox(LocaleResources.getString("apTitleAllReviewers", this.m_cxt), group, false);
/*     */ 
/*  94 */     this.m_helper.addExchangeComponent(pnl, isAllBox, "dWfStepIsAll");
/*     */ 
/*  96 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/*  97 */     JCheckBox batchBox = new CustomCheckbox(LocaleResources.getString("apTitleAtLeastThisManyReviewers", this.m_cxt), group, true);
/*     */ 
/*  99 */     this.m_helper.addExchangeComponent(pnl, batchBox, "dWfStepHasWeight");
/* 100 */     this.m_weightFld = new JTextField(2);
/* 101 */     this.m_helper.addExchangeComponent(pnl, this.m_weightFld, "dWfStepWeight");
/* 102 */     this.m_helper.addComponent(pnl, this.m_weightFld);
/* 103 */     this.m_helper.addComponent(pnl, new CustomLabel(" "));
/*     */ 
/* 105 */     ItemListener iListener = new Object(isAllBox)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 109 */         Object obj = e.getSource();
/* 110 */         boolean isAll = false;
/* 111 */         if (obj == this.val$isAllBox)
/*     */         {
/* 113 */           isAll = true;
/*     */         }
/* 115 */         WfStepConditionsPanel.this.checkOptionSelection(isAll);
/*     */       }
/*     */     };
/* 118 */     batchBox.addItemListener(iListener);
/* 119 */     isAllBox.addItemListener(iListener);
/*     */ 
/* 121 */     DocumentListener txtValidator = new DocumentListener()
/*     */     {
/*     */       public void changedUpdate(DocumentEvent e)
/*     */       {
/*     */       }
/*     */ 
/*     */       public void insertUpdate(DocumentEvent e)
/*     */       {
/* 130 */         textValueChanged(e);
/*     */       }
/*     */ 
/*     */       public void removeUpdate(DocumentEvent e)
/*     */       {
/* 135 */         textValueChanged(e);
/*     */       }
/*     */ 
/*     */       public void textValueChanged(DocumentEvent e)
/*     */       {
/* 140 */         String str = WfStepConditionsPanel.this.m_weightFld.getText().trim();
/*     */ 
/* 142 */         int val = 0;
/* 143 */         boolean isNotInteger = false;
/*     */         try
/*     */         {
/* 146 */           if ((str != null) && (str.length() > 0))
/*     */           {
/* 148 */             val = Integer.parseInt(str);
/*     */           }
/*     */           else
/*     */           {
/* 152 */             str = "1";
/*     */           }
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 157 */           isNotInteger = true;
/*     */         }
/*     */ 
/* 160 */         if ((isNotInteger) || (val < 0))
/*     */         {
/* 162 */           MessageBox.reportError(WfStepConditionsPanel.this.m_systemInterface, IdcMessageFactory.lc("apSpecifyIntegerForReviewerNumber", new Object[0]));
/*     */ 
/* 164 */           WfStepConditionsPanel.this.m_weightFld.setText(WfStepConditionsPanel.this.m_prevWeight);
/*     */         }
/*     */         else
/*     */         {
/* 168 */           WfStepConditionsPanel.this.m_prevWeight = str;
/*     */         }
/*     */       }
/*     */     };
/* 172 */     this.m_weightFld.getDocument().addDocumentListener(txtValidator);
/* 173 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void checkOptionSelection(boolean isAll)
/*     */   {
/* 178 */     this.m_weightFld.setEnabled(!isAll);
/*     */   }
/*     */ 
/*     */   protected void initialSelections()
/*     */   {
/* 183 */     boolean isAll = StringUtils.convertToBool(this.m_helper.m_props.getProperty("dWfStepIsAll"), false);
/*     */ 
/* 185 */     checkOptionSelection(isAll);
/*     */ 
/* 187 */     String str = this.m_helper.m_props.getProperty("dWfStepWeight");
/* 188 */     int value = -1;
/* 189 */     if ((str != null) && (str.length() > 0))
/*     */     {
/* 191 */       value = NumberUtils.parseInteger(str, -1);
/* 192 */       this.m_prevWeight = str;
/*     */     }
/*     */ 
/* 195 */     if (value >= 0)
/*     */       return;
/* 197 */     this.m_helper.m_props.put("dWfStepWeight", this.m_prevWeight);
/*     */   }
/*     */ 
/*     */   protected JPanel initExitConditionsPanel()
/*     */   {
/* 203 */     JPanel pnl = new CustomPanel();
/* 204 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 206 */     this.m_exitConditionBox = new CustomCheckbox(LocaleResources.getString("apUseAdditionalExitCondition", this.m_cxt), 1);
/*     */ 
/* 208 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 209 */     this.m_helper.addExchangeComponent(pnl, this.m_exitConditionBox, "HasAdditionalExitCondition");
/*     */ 
/* 211 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 212 */     addScriptUI(pnl, "wfExitConditionSummary", "ExitCondition");
/*     */ 
/* 214 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void addScriptUI(JPanel pnl, String fieldName, String cmdPrefix)
/*     */   {
/* 219 */     this.m_exitConditionText = new CustomTextArea(4, 30);
/* 220 */     this.m_helper.addExchangeComponent(pnl, this.m_exitConditionText, fieldName);
/* 221 */     this.m_exitConditionText.setEnabled(false);
/*     */ 
/* 224 */     JPanel btnPanel = new PanePanel();
/* 225 */     this.m_helper.makePanelGridBag(btnPanel, 2);
/*     */ 
/* 227 */     JButton editBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/* 228 */     editBtn.setActionCommand("edit" + cmdPrefix);
/* 229 */     editBtn.addActionListener(this);
/*     */ 
/* 231 */     JButton clearBtn = new JButton(LocaleResources.getString("apTitleClear", this.m_cxt));
/* 232 */     clearBtn.setActionCommand("clear" + cmdPrefix);
/* 233 */     clearBtn.addActionListener(this);
/*     */ 
/* 235 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 5, 5, 5);
/* 236 */     this.m_helper.addComponent(btnPanel, editBtn);
/* 237 */     this.m_helper.addLastComponentInRow(btnPanel, clearBtn);
/*     */ 
/* 239 */     this.m_helper.addLastComponentInRow(pnl, btnPanel);
/*     */   }
/*     */ 
/*     */   protected void loadCondition(boolean isFresh)
/*     */   {
/* 245 */     String key = "wfAdditionalExitCondition";
/* 246 */     if (isFresh)
/*     */     {
/* 248 */       String stepName = this.m_workflowInfo.m_wfName;
/* 249 */       if ((stepName != null) && (stepName.length() > 0))
/*     */       {
/* 251 */         key = stepName + ":" + key;
/*     */ 
/* 254 */         boolean useCondition = StringUtils.convertToBool(this.m_workflowInfo.get(stepName + ":HasAdditionalExitCondition"), false);
/*     */ 
/* 256 */         this.m_helper.m_props.put("HasAdditionalExitCondition", "" + useCondition);
/*     */       }
/*     */     }
/*     */ 
/* 260 */     String str = this.m_workflowInfo.get(key);
/* 261 */     if (str == null)
/*     */     {
/* 263 */       str = "";
/*     */     }
/* 265 */     this.m_workflowInfo.setValue("wfAdditionalExitCondition", str);
/*     */ 
/* 268 */     JumpClausesData clauseData = new JumpClausesData(true);
/* 269 */     clauseData.setClauseDisplay(null, " and\n");
/* 270 */     clauseData.parse(str);
/*     */     try
/*     */     {
/* 274 */       String val = clauseData.createQueryString();
/* 275 */       this.m_helper.m_props.put("wfExitConditionSummary", val);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 279 */       MessageBox.reportError(this.m_systemInterface, IdcMessageFactory.lc("apWfExitConditionParseError", new Object[0]));
/*     */     }
/*     */ 
/* 282 */     this.m_workflowInfo.setValue("ConditionKeys", "HasAdditionalExitCondition,wfAdditionalExitCondition");
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/* 288 */     loadCondition(true);
/* 289 */     this.m_helper.loadComponentValues();
/* 290 */     initialSelections();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 298 */     String cmd = e.getActionCommand();
/* 299 */     if (cmd.equals("editExitCondition"))
/*     */     {
/* 301 */       EditExitConditionDlg dlg = new EditExitConditionDlg(this.m_systemInterface, LocaleResources.getString("apDlgExitCondition", this.m_cxt), "EditWorkflowExitCondition");
/*     */ 
/* 304 */       int result = dlg.init(this.m_workflowInfo, this.m_context);
/* 305 */       if (result == 1)
/*     */       {
/* 308 */         loadCondition(false);
/* 309 */         String str = this.m_helper.m_props.getProperty("wfExitConditionSummary");
/* 310 */         this.m_exitConditionText.setText(str);
/*     */       }
/*     */     } else {
/* 313 */       if (!cmd.equals("clearExitCondition")) {
/*     */         return;
/*     */       }
/* 316 */       this.m_workflowInfo.setValue("wfAdditionalExitCondition", "");
/* 317 */       this.m_helper.m_props.put("wfExitConditionSummary", "");
/* 318 */       this.m_helper.m_exchange.setComponentValue("wfExitConditionSummary", "");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 324 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78651 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfStepConditionsPanel
 * JD-Core Version:    0.5.4
 */