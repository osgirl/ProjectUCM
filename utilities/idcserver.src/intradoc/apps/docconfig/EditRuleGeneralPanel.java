/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditRuleGeneralPanel extends DocConfigPanel
/*     */   implements ActionListener, ItemListener, ComponentBinder
/*     */ {
/*     */   protected DataBinder m_ruleData;
/*     */   protected JCheckBox m_isGlobalBox;
/*     */   protected JTextField m_globalPriorityTxt;
/*     */   protected JCheckBox m_isGroupBox;
/*     */   protected JCheckBox m_hasHeaderBox;
/*     */   protected JButton m_editHeaderBtn;
/*     */   protected JCheckBox m_activationBox;
/*     */   protected JButton m_activationBtn;
/*     */ 
/*     */   public EditRuleGeneralPanel()
/*     */   {
/*  67 */     this.m_ruleData = null;
/*     */ 
/*  70 */     this.m_isGlobalBox = null;
/*  71 */     this.m_globalPriorityTxt = null;
/*  72 */     this.m_isGroupBox = null;
/*  73 */     this.m_hasHeaderBox = null;
/*  74 */     this.m_editHeaderBtn = null;
/*  75 */     this.m_activationBox = null;
/*  76 */     this.m_activationBtn = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder data) throws ServiceException
/*     */   {
/*  81 */     super.initEx(sys, data);
/*  82 */     this.m_helper.m_componentBinder = this;
/*  83 */     this.m_ruleData = data;
/*  84 */     JPanel panel = initUI(data);
/*     */ 
/*  86 */     this.m_helper.makePanelGridBag(this, 1);
/*  87 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  88 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  89 */     this.m_helper.addComponent(this, panel);
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(DataBinder binder)
/*     */   {
/*  94 */     JPanel pnl = new PanePanel();
/*  95 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/*  97 */     boolean isNew = binder.getLocal("dpRuleName") == null;
/*  98 */     if (isNew)
/*     */     {
/* 100 */       this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apDpRuleNameLabel", this.m_ctx), 30, "dpRuleName");
/*     */     }
/*     */     else
/*     */     {
/* 106 */       this.m_helper.addLabelDisplayPair(pnl, LocaleResources.getString("apDpRuleNameLabel", this.m_ctx), 30, "dpRuleName");
/*     */     }
/*     */ 
/* 110 */     this.m_helper.addLabelEditPair(pnl, LocaleResources.getString("apDpRuleDescriptionLabel", this.m_ctx), 30, "dpRuleDescription");
/*     */ 
/* 115 */     JPanel activationPanel = addNewSubPanel(pnl);
/* 116 */     this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 117 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 118 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 119 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 120 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/* 122 */     this.m_isGlobalBox = new CustomCheckbox(this.m_systemInterface.getString("apDpRuleIsGlobalWithPriorityLabel"));
/*     */ 
/* 124 */     this.m_helper.addExchangeComponent(activationPanel, this.m_isGlobalBox, "dpRuleIsGlobal");
/* 125 */     this.m_isGlobalBox.addItemListener(this);
/*     */ 
/* 127 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 128 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 129 */     this.m_globalPriorityTxt = new CustomTextField(2);
/* 130 */     this.m_helper.addExchangeComponent(activationPanel, this.m_globalPriorityTxt, "dpRuleGlobalPriority");
/*     */ 
/* 133 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 134 */     this.m_isGroupBox = new CustomCheckbox(this.m_systemInterface.getString("apDpRuleIsGroup"));
/* 135 */     this.m_helper.addExchangeComponent(activationPanel, this.m_isGroupBox, "dpRuleIsGroup");
/* 136 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 137 */     this.m_isGroupBox.addItemListener(this);
/*     */ 
/* 139 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 25, 2, 5);
/* 140 */     this.m_hasHeaderBox = new CustomCheckbox(this.m_systemInterface.getString("apDpRuleHasHeader"));
/* 141 */     this.m_hasHeaderBox.addItemListener(this);
/* 142 */     this.m_helper.addExchangeComponent(activationPanel, this.m_hasHeaderBox, "dpRuleHasHeader");
/* 143 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 144 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 145 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 146 */     this.m_editHeaderBtn = new JButton(this.m_systemInterface.getString("apDpEditHeader"));
/* 147 */     this.m_editHeaderBtn.setActionCommand("editGroupHeader");
/* 148 */     this.m_editHeaderBtn.addActionListener(this);
/* 149 */     this.m_helper.addComponent(activationPanel, this.m_editHeaderBtn);
/*     */ 
/* 152 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 153 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 154 */     this.m_activationBox = new CustomCheckbox(this.m_systemInterface.getString("apDpRuleUseActivationCondition"));
/* 155 */     this.m_helper.addExchangeComponent(activationPanel, this.m_activationBox, "dpRuleHasActivationCondition");
/* 156 */     this.m_activationBox.addItemListener(this);
/*     */ 
/* 158 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 159 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 160 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 161 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 162 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 25, 2, 5);
/* 163 */     CustomTextArea ctext = new CustomTextArea(5, 30);
/* 164 */     this.m_helper.addExchangeComponent(activationPanel, ctext, "dprActivationConditionSummary");
/* 165 */     ctext.setEditable(false);
/*     */ 
/* 167 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 168 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 169 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 170 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 171 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 172 */     this.m_activationBtn = new JButton(this.m_systemInterface.getString("apDlgButtonEdit"));
/* 173 */     this.m_helper.addComponent(activationPanel, this.m_activationBtn);
/* 174 */     this.m_activationBtn.setActionCommand("editActivationCondition");
/* 175 */     this.m_activationBtn.addActionListener(this);
/*     */ 
/* 177 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel)
/*     */   {
/* 183 */     CustomPanel panel = new CustomPanel();
/* 184 */     panel.setInsets(10, 5, 10, 5);
/* 185 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 187 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 188 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 189 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 190 */     this.m_helper.addComponent(mainPanel, panel);
/* 191 */     this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 193 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void loadActivationCondition(DataBinder workingBinder, boolean isRefresh)
/*     */   {
/* 198 */     if (!isRefresh)
/*     */     {
/* 201 */       String[] keys = { "dprActivationIsCustom", "dprActivationCustomScript", "dprSideEffects" };
/* 202 */       for (int i = 0; i < keys.length; ++i)
/*     */       {
/* 204 */         String key = keys[i];
/* 205 */         String val = workingBinder.getLocal(key);
/* 206 */         this.m_ruleData.putLocal(key, val);
/*     */       }
/*     */ 
/* 209 */       ResultSet rset = workingBinder.getResultSet("ActivationRuleClauses");
/* 210 */       this.m_ruleData.addResultSet("ActivationRuleClauses", rset);
/*     */     }
/*     */ 
/* 213 */     String summary = DocProfileScriptUtils.computeScriptString("", this.m_ruleData, "activation", false);
/* 214 */     this.m_helper.m_exchange.setComponentValue("dprActivationConditionSummary", summary);
/*     */   }
/*     */ 
/*     */   public void loadComponents()
/*     */   {
/* 220 */     loadActivationCondition(null, true);
/* 221 */     super.loadComponents();
/*     */ 
/* 223 */     enableDisable();
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 228 */     boolean isChecked = this.m_activationBox.isSelected();
/* 229 */     this.m_activationBtn.setEnabled(isChecked);
/*     */ 
/* 231 */     isChecked = this.m_isGroupBox.isSelected();
/* 232 */     if (isChecked)
/*     */     {
/* 234 */       this.m_hasHeaderBox.setEnabled(isChecked);
/* 235 */       isChecked = this.m_hasHeaderBox.isSelected();
/* 236 */       this.m_editHeaderBtn.setEnabled(isChecked);
/*     */     }
/*     */     else
/*     */     {
/* 240 */       this.m_hasHeaderBox.setEnabled(isChecked);
/* 241 */       this.m_editHeaderBtn.setEnabled(isChecked);
/*     */     }
/*     */ 
/* 244 */     isChecked = this.m_isGlobalBox.isSelected();
/* 245 */     this.m_globalPriorityTxt.setEnabled(isChecked);
/* 246 */     if (!isChecked)
/*     */       return;
/* 248 */     String str = this.m_globalPriorityTxt.getText();
/* 249 */     if ((str != null) && (str.length() != 0))
/*     */       return;
/* 251 */     this.m_globalPriorityTxt.setText("10");
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 261 */     String cmd = e.getActionCommand();
/* 262 */     if (cmd.equals("editActivationCondition"))
/*     */     {
/* 264 */       DataBinder workingBinder = prepareActivationConditionEdit();
/*     */ 
/* 266 */       Properties configProps = DocProfileScriptUtils.loadConfiguration("activation");
/*     */ 
/* 268 */       String title = this.m_systemInterface.getString("apDpEditActivationConditionTitle");
/* 269 */       String helpPage = DialogHelpTable.getHelpPage("DpActivationCondition");
/* 270 */       EditProfileScriptDlg dlg = new EditProfileScriptDlg(this.m_systemInterface, this, title, helpPage);
/*     */ 
/* 272 */       int result = dlg.init(workingBinder, configProps, null);
/* 273 */       if (result == 1)
/*     */       {
/* 275 */         loadActivationCondition(workingBinder, false);
/*     */       }
/*     */     } else {
/* 278 */       if (!cmd.equals("editGroupHeader"))
/*     */         return;
/* 280 */       promptForHeader();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected DataBinder prepareActivationConditionEdit()
/*     */   {
/* 286 */     Properties props = this.m_ruleData.getLocalData();
/* 287 */     props = (Properties)props.clone();
/* 288 */     ResultSet rset = this.m_ruleData.getResultSet("ActivationRuleClauses");
/*     */ 
/* 290 */     if (rset == null)
/*     */     {
/* 292 */       rset = new DataResultSet(DocProfileScriptUtils.DP_RULE_ACTIVATION_COLUMNS);
/*     */     }
/*     */     else
/*     */     {
/* 296 */       DataResultSet copySet = new DataResultSet();
/* 297 */       copySet.copy(rset);
/* 298 */       rset = copySet;
/*     */     }
/*     */ 
/* 302 */     DataBinder workingBinder = new DataBinder();
/* 303 */     workingBinder.setLocalData(props);
/* 304 */     workingBinder.addResultSet("ActivationRuleClauses", rset);
/*     */ 
/* 306 */     return workingBinder;
/*     */   }
/*     */ 
/*     */   protected void promptForHeader()
/*     */   {
/* 311 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 316 */         return true;
/*     */       }
/*     */     };
/* 321 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, this.m_systemInterface.getString("apDpRuleEditGroupHeaderTitle"), true);
/*     */ 
/* 323 */     JPanel mainPanel = helper.initStandard(null, okCallback, 1, true, DialogHelpTable.getHelpPage("DpEditGroupHeader"));
/*     */ 
/* 327 */     String[] keys = { "dpRuleGroupHeader", "dpRuleStartInclude", "dpRuleEndInclude", "dpIsGroupDefaultHide" };
/* 328 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 330 */       String key = keys[i];
/* 331 */       String str = this.m_ruleData.getLocal(key);
/* 332 */       if (str == null)
/*     */         continue;
/* 334 */       helper.m_props.put(key, str);
/*     */     }
/*     */ 
/* 338 */     JPanel pnl = new PanePanel();
/* 339 */     helper.makePanelGridBag(pnl, 2);
/*     */ 
/* 341 */     String[][] startOpts = DocProfileScriptUtils.createDisplayIncludeOptions("*start*", this.m_ctx);
/* 342 */     String[][] endOpts = DocProfileScriptUtils.createDisplayIncludeOptions("*end*", this.m_ctx);
/*     */ 
/* 344 */     helper.m_gridHelper.prepareAddLastRowElement(17);
/* 345 */     CustomText txt = new CustomText(this.m_systemInterface.getString("apDpRuleEditHeaderDesc"));
/* 346 */     helper.addComponent(pnl, txt);
/*     */ 
/* 348 */     helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpIsGroupDefaultHide"), new CustomCheckbox("Enable"), "dpIsGroupDefaultHide");
/*     */ 
/* 351 */     if (startOpts != null)
/*     */     {
/* 353 */       ComboChoice choice = new ComboChoice(this.m_systemInterface.getString("apDpNoneSpecified"));
/* 354 */       choice.initChoiceList(startOpts);
/* 355 */       helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpRuleStartInclude"), choice, "dpRuleStartInclude");
/*     */     }
/*     */     else
/*     */     {
/* 361 */       helper.addLabelEditPair(pnl, this.m_systemInterface.localizeCaption("apDpRuleStartInclude"), 30, "dpRuleStartInclude");
/*     */     }
/*     */ 
/* 365 */     if (endOpts != null)
/*     */     {
/* 367 */       ComboChoice choice = new ComboChoice(this.m_systemInterface.getString("apDpNoneSpecified"));
/* 368 */       choice.initChoiceList(endOpts);
/* 369 */       helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpRuleEndInclude"), choice, "dpRuleEndInclude");
/*     */     }
/*     */     else
/*     */     {
/* 376 */       helper.addLabelEditPair(pnl, this.m_systemInterface.localizeCaption("apDpRuleEndInclude"), 30, "dpRuleEndInclude");
/*     */     }
/*     */ 
/* 380 */     this.m_helper.addLastComponentInRow(mainPanel, pnl);
/*     */ 
/* 382 */     helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 383 */     helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 384 */     CustomLabel lbl = new CustomLabel(this.m_systemInterface.getString("apDpRuleEditHeaderMsg"));
/* 385 */     helper.addLastComponentInRow(mainPanel, lbl);
/* 386 */     helper.addExchangeComponent(mainPanel, new CustomTextArea(), "dpRuleGroupHeader");
/*     */ 
/* 389 */     if (helper.prompt() != 1)
/*     */       return;
/* 391 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 393 */       String key = keys[i];
/* 394 */       String str = helper.m_props.getProperty(key);
/* 395 */       this.m_ruleData.putLocal(key, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 405 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 414 */     ContainerHelper helper = (ContainerHelper)exchange.m_currentObject;
/* 415 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 420 */     String name = exchange.m_compName;
/* 421 */     String val = exchange.m_compValue;
/*     */ 
/* 423 */     IdcMessage errMsg = null;
/* 424 */     if (name.equals("dpRuleName"))
/*     */     {
/* 426 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apDpRuleName", 0, null);
/*     */     }
/* 428 */     else if (name.equals("dpRuleGlobalPriority"))
/*     */     {
/* 430 */       boolean isGlobal = this.m_isGlobalBox.isSelected();
/* 431 */       if (isGlobal)
/*     */       {
/* 433 */         int status = Validation.checkInteger(val);
/* 434 */         if (status != 0)
/*     */         {
/* 436 */           errMsg = IdcMessageFactory.lc("apDpPriorityNotIntegerError", new Object[] { val });
/*     */         }
/*     */         else
/*     */         {
/* 441 */           int pr = NumberUtils.parseInteger(val, 0);
/* 442 */           if (pr <= 0)
/*     */           {
/* 444 */             errMsg = IdcMessageFactory.lc("apDpPriorityNotPositiveError", new Object[] { val });
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 450 */     if (errMsg != null)
/*     */     {
/* 452 */       exchange.m_errorMessage = errMsg;
/* 453 */       return false;
/*     */     }
/* 455 */     return true;
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 466 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80607 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditRuleGeneralPanel
 * JD-Core Version:    0.5.4
 */