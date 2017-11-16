/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.RuleClausesData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.CardLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class ScriptClausePanel extends DocConfigPanel
/*     */   implements ItemListener, ActionListener
/*     */ {
/*     */   public int m_currentIndex;
/*     */   public RuleClausesData m_currentClause;
/*     */   protected DataBinder m_scriptData;
/*     */   protected DataResultSet m_clauseSet;
/*     */   protected ViewFieldDef m_fieldDef;
/*     */   protected String m_tableName;
/*     */   protected String m_valueKey;
/*     */   protected String m_isCustomKey;
/*     */   protected String m_scriptType;
/*     */   protected UdlPanel m_clauseList;
/*     */   protected RuleBuilderHelper m_ruleHelper;
/*     */   protected JPanel m_curView;
/*     */   protected JPanel m_flipPanel;
/*     */   protected Hashtable m_flipComponents;
/*     */   protected JButton[] m_controlBtns;
/*     */ 
/*     */   public ScriptClausePanel()
/*     */   {
/*  69 */     this.m_currentIndex = -1;
/*  70 */     this.m_currentClause = null;
/*  71 */     this.m_scriptData = null;
/*  72 */     this.m_clauseSet = null;
/*  73 */     this.m_fieldDef = null;
/*     */ 
/*  76 */     this.m_tableName = null;
/*  77 */     this.m_valueKey = null;
/*  78 */     this.m_isCustomKey = null;
/*  79 */     this.m_scriptType = null;
/*     */ 
/*  82 */     this.m_clauseList = null;
/*  83 */     this.m_ruleHelper = null;
/*  84 */     this.m_curView = null;
/*  85 */     this.m_flipPanel = null;
/*  86 */     this.m_flipComponents = null;
/*  87 */     this.m_controlBtns = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder) throws ServiceException
/*     */   {
/*  92 */     this.m_scriptData = binder;
/*  93 */     this.m_clauseSet = ((DataResultSet)binder.getResultSet(this.m_tableName));
/*     */ 
/*  95 */     super.initEx(sys, binder);
/*  96 */     initUI();
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/* 101 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 102 */     gh.useGridBag(this);
/*     */ 
/* 104 */     JPanel pnl = createClausePanel();
/* 105 */     initScriptPanels();
/*     */ 
/* 108 */     gh.m_gc.insets = new Insets(5, 5, 5, 5);
/* 109 */     gh.m_gc.weightx = 1.0D;
/* 110 */     gh.m_gc.weighty = 1.0D;
/* 111 */     gh.m_gc.fill = 1;
/* 112 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */ 
/* 114 */     gh.m_gc.weightx = 1.0D;
/* 115 */     gh.m_gc.weighty = 0.0D;
/* 116 */     this.m_helper.addLastComponentInRow(this, this.m_flipPanel);
/*     */ 
/* 118 */     refreshClauseList(null);
/*     */   }
/*     */ 
/*     */   protected JPanel createClausePanel()
/*     */   {
/* 123 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 125 */     JPanel pnl = new PanePanel();
/* 126 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 127 */     gh.prepareAddRowElement();
/* 128 */     this.m_helper.addComponent(pnl, new CustomLabel(this.m_systemInterface.localizeCaption("apDpRuleConditionsLabel"), 1));
/*     */ 
/* 131 */     this.m_clauseList = new UdlPanel(null, null, 200, 5, this.m_tableName, false);
/* 132 */     this.m_clauseList.setVisibleColumns("dpRuleConditionName");
/* 133 */     this.m_clauseList.init();
/* 134 */     this.m_clauseList.useDefaultListener();
/* 135 */     this.m_clauseList.addItemListener(this);
/*     */ 
/* 137 */     gh.m_gc.weightx = 1.0D;
/* 138 */     gh.m_gc.weighty = 1.0D;
/* 139 */     this.m_helper.addComponent(pnl, this.m_clauseList);
/*     */ 
/* 142 */     String[][] buttonInfo = { { this.m_systemInterface.getString("apDlgButtonAdd"), "add", "0", this.m_systemInterface.getString("apDpAddRuleConditionTitle") }, { this.m_systemInterface.getString("apLabelDelete"), "delete", "1", this.m_systemInterface.getString("apReadableButtonDeleteCondition") } };
/*     */ 
/* 147 */     JPanel btnPanel = new PanePanel();
/* 148 */     gh.useGridBag(btnPanel);
/* 149 */     gh.m_gc.fill = 2;
/* 150 */     gh.prepareAddLastRowElement();
/*     */ 
/* 152 */     this.m_controlBtns = new JButton[buttonInfo.length];
/* 153 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 155 */       boolean isControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 156 */       JButton btn = this.m_clauseList.addButton(buttonInfo[i][0], isControlled);
/* 157 */       btn.getAccessibleContext().setAccessibleName(buttonInfo[i][3]);
/* 158 */       btn.setActionCommand(buttonInfo[i][1]);
/* 159 */       btn.addActionListener(this);
/* 160 */       this.m_helper.addComponent(btnPanel, btn);
/* 161 */       btn.setEnabled(!isControlled);
/* 162 */       this.m_controlBtns[i] = btn;
/*     */     }
/*     */ 
/* 165 */     gh.prepareAddLastRowElement();
/* 166 */     gh.m_gc.weightx = 0.0D;
/* 167 */     gh.m_gc.weighty = 0.0D;
/* 168 */     Insets oldInsets = gh.m_gc.insets;
/* 169 */     gh.m_gc.insets = new Insets(25, 5, 20, 5);
/* 170 */     this.m_helper.addComponent(pnl, btnPanel);
/* 171 */     gh.m_gc.insets = oldInsets;
/*     */ 
/* 173 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initScriptPanels()
/*     */   {
/* 178 */     this.m_flipPanel = new PanePanel();
/* 179 */     CardLayout cardLayout = new CardLayout();
/* 180 */     this.m_flipPanel.setLayout(cardLayout);
/* 181 */     this.m_flipComponents = new Hashtable();
/*     */ 
/* 183 */     JPanel emptyView = new CustomPanel();
/* 184 */     this.m_curView = emptyView;
/* 185 */     this.m_flipPanel.add("Empty", emptyView);
/* 186 */     this.m_flipComponents.put("Empty", emptyView);
/*     */ 
/* 188 */     JPanel scriptDefPanel = initRuleHelper();
/* 189 */     this.m_flipPanel.add("Script", scriptDefPanel);
/* 190 */     this.m_flipComponents.put("Script", scriptDefPanel);
/*     */ 
/* 192 */     cardLayout.show(this.m_flipPanel, "Empty");
/*     */   }
/*     */ 
/*     */   protected JPanel initRuleHelper()
/*     */   {
/* 197 */     JPanel pnl = new PanePanel();
/* 198 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 201 */     boolean hasValue = this.m_valueKey != null;
/* 202 */     this.m_ruleHelper = new RuleBuilderHelper("apDpRuleValueLabel", "dpRuleValue", "apDpRuleClausesLabel", hasValue, this);
/*     */ 
/* 204 */     this.m_ruleHelper.init(this.m_systemInterface, this.m_fieldDef, this);
/* 205 */     this.m_ruleHelper.setDocumentProfile(this.m_docProfile);
/*     */     try
/*     */     {
/* 209 */       createFieldList();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 213 */       reportError(e, IdcMessageFactory.lc("apDpErrorCreatingDisplayForRule", new Object[0]));
/*     */     }
/*     */ 
/* 216 */     String conjunction = "\n";
/* 217 */     if ((this.m_scriptType != null) && (this.m_scriptType.equals("activation")))
/*     */     {
/* 219 */       conjunction = " and\n";
/*     */     }
/*     */ 
/* 222 */     this.m_ruleHelper.setQueryLabels("apDpConditionExpression", "apDpUseCustom");
/* 223 */     this.m_ruleHelper.createStandardRuleClausePanel(this.m_helper, pnl, this, this.m_scriptType);
/*     */ 
/* 225 */     RuleClausesData data = new RuleClausesData(false, this.m_isCustomKey);
/* 226 */     data.setClauseDisplay(null, conjunction);
/*     */ 
/* 228 */     this.m_ruleHelper.setData(data, null);
/*     */     try
/*     */     {
/* 232 */       this.m_ruleHelper.loadData();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 236 */       reportError(e, IdcMessageFactory.lc("apDpUnableToLoadRuleData", new Object[0]));
/*     */     }
/*     */ 
/* 239 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void createFieldList() throws DataException, ServiceException
/*     */   {
/* 244 */     ViewFields fieldsObj = new ViewFields(this.m_ctx);
/* 245 */     fieldsObj.m_enabledOnly = false;
/*     */ 
/* 248 */     Vector userFields = fieldsObj.createUserViewFields(SharedObjects.getTable("UserMetaDefinition"));
/*     */ 
/* 251 */     int size = userFields.size();
/* 252 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 254 */       ViewFieldDef fieldDef = (ViewFieldDef)userFields.elementAt(i);
/* 255 */       fieldDef.m_name = ("getUserValue(\"" + fieldDef.m_name + "\")");
/*     */     }
/*     */ 
/* 259 */     ViewFieldDef fd = fieldsObj.addField("role", this.m_systemInterface.getString("apRoleName"));
/* 260 */     fd.m_isOptionList = true;
/* 261 */     fd.m_optionListKey = "roles";
/* 262 */     fd.m_type = "yes/no";
/*     */ 
/* 264 */     if ((this.m_scriptType.indexOf("default") >= 0) || (this.m_scriptType.indexOf("derived") >= 0) || (this.m_scriptType.indexOf("activation") >= 0))
/*     */     {
/* 268 */       Vector appFields = fieldsObj.createAppViewFields("application");
/*     */ 
/* 271 */       ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 272 */       fieldsObj.createAllDocumentFieldsList(metaFields, true, false, false, false);
/*     */ 
/* 275 */       Vector fields = fieldsObj.m_viewFields;
/* 276 */       size = fields.size();
/* 277 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 279 */         ViewFieldDef fieldDef = (ViewFieldDef)fields.elementAt(i);
/* 280 */         fieldDef.m_name = ("#active." + fieldDef.m_name);
/*     */       }
/*     */ 
/* 283 */       fieldsObj.mergeFields(appFields, "");
/*     */     }
/*     */ 
/* 286 */     fieldsObj.mergeFields(userFields, "");
/*     */ 
/* 289 */     this.m_ruleHelper.setFieldList(fieldsObj.m_viewFields, null);
/* 290 */     this.m_ruleHelper.setDisplayMaps(fieldsObj.m_tableFields.m_displayMaps);
/*     */   }
/*     */ 
/*     */   protected void loadConfiguration(Properties props)
/*     */   {
/* 297 */     this.m_tableName = props.getProperty("TableName");
/* 298 */     this.m_valueKey = props.getProperty("ValueKey");
/* 299 */     this.m_isCustomKey = props.getProperty("IsCustomKey");
/*     */ 
/* 302 */     this.m_scriptType = props.getProperty("ScriptType");
/*     */   }
/*     */ 
/*     */   public void setFieldDef(ViewFieldDef fieldDef)
/*     */   {
/* 307 */     this.m_fieldDef = fieldDef;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 315 */     checkSelection();
/* 316 */     enableDisable();
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 321 */     CardLayout panelHandler = (CardLayout)this.m_flipPanel.getLayout();
/* 322 */     int index = this.m_clauseList.getSelectedIndex();
/* 323 */     saveSelection(true);
/* 324 */     if (index < 0)
/*     */     {
/* 326 */       this.m_currentIndex = index;
/* 327 */       this.m_currentClause = null;
/* 328 */       panelHandler.show(this.m_flipPanel, "Empty");
/* 329 */       return;
/*     */     }
/*     */ 
/* 332 */     if (this.m_currentClause == null)
/*     */     {
/* 334 */       this.m_currentClause = new RuleClausesData(false, this.m_isCustomKey);
/* 335 */       this.m_currentClause.m_isCustom = StringUtils.convertToBool(this.m_scriptData.getLocal(this.m_isCustomKey), false);
/*     */     }
/*     */ 
/* 339 */     this.m_clauseSet.setCurrentRow(index);
/* 340 */     Properties props = this.m_clauseSet.getCurrentRowProps();
/* 341 */     this.m_currentClause.parseRuleScript(props, this.m_scriptData.getLocalData());
/* 342 */     this.m_currentIndex = index;
/* 343 */     this.m_helper.m_props = props;
/*     */ 
/* 345 */     this.m_ruleHelper.setData(this.m_currentClause, null);
/*     */     try
/*     */     {
/* 348 */       this.m_ruleHelper.loadData();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 354 */       reportError(e);
/*     */     }
/* 356 */     panelHandler.show(this.m_flipPanel, "Script");
/*     */   }
/*     */ 
/*     */   protected boolean saveSelection(boolean isFullSave)
/*     */   {
/* 361 */     if ((this.m_currentIndex >= 0) && (this.m_currentClause != null) && (this.m_currentIndex < this.m_clauseSet.getNumRows()))
/*     */     {
/* 363 */       if (isFullSave)
/*     */       {
/* 365 */         this.m_ruleHelper.saveCurrentSelection();
/* 366 */         if (!this.m_ruleHelper.exchangeQueryInfo(false))
/*     */         {
/* 368 */           return false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 373 */         this.m_ruleHelper.enableDisable(true);
/*     */       }
/*     */ 
/* 376 */       Properties props = this.m_currentClause.formatRuleScript();
/* 377 */       Parameters params = new PropParameters(props);
/*     */       try
/*     */       {
/* 380 */         Vector row = this.m_clauseSet.createRow(params);
/* 381 */         this.m_clauseSet.setRowValues(row, this.m_currentIndex);
/*     */       }
/*     */       catch (Exception exp)
/*     */       {
/* 385 */         if (isFullSave)
/*     */         {
/* 387 */           reportError(exp);
/*     */         }
/*     */         else
/*     */         {
/* 391 */           Report.trace(null, null, exp);
/*     */         }
/* 393 */         return false;
/*     */       }
/*     */     }
/* 396 */     return true;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 401 */     String str = this.m_scriptData.getLocal(this.m_isCustomKey);
/* 402 */     boolean isCustom = StringUtils.convertToBool(str, false);
/* 403 */     if (this.m_currentClause != null)
/*     */     {
/* 405 */       this.m_currentClause.m_isCustom = isCustom;
/*     */     }
/*     */ 
/* 408 */     boolean isEnabled = !isCustom;
/* 409 */     int start = 0;
/* 410 */     if ((this.m_currentClause == null) && (!isCustom))
/*     */     {
/* 413 */       start = 1;
/* 414 */       isEnabled = false;
/* 415 */       this.m_controlBtns[0].setEnabled(true);
/*     */     }
/*     */     else
/*     */     {
/* 419 */       this.m_ruleHelper.enableDisable(false);
/*     */     }
/* 421 */     int len = this.m_controlBtns.length;
/* 422 */     for (int i = start; i < len; ++i)
/*     */     {
/* 424 */       this.m_controlBtns[i].setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 434 */     if (saveSelection(true))
/*     */     {
/* 436 */       return 1;
/*     */     }
/* 438 */     return 0;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 445 */     String cmd = e.getActionCommand();
/* 446 */     if (cmd.equals("add"))
/*     */     {
/* 448 */       promptForName();
/*     */     }
/* 450 */     else if (cmd.equals("delete"))
/*     */     {
/* 452 */       int index = this.m_clauseList.getSelectedIndex();
/* 453 */       if (index >= 0)
/*     */       {
/* 455 */         this.m_currentClause = null;
/* 456 */         this.m_currentIndex = -1;
/* 457 */         this.m_clauseSet.deleteRow(index);
/* 458 */         refreshClauseList(null);
/*     */       }
/*     */     } else {
/* 461 */       if (!cmd.equals("selectValue")) {
/*     */         return;
/*     */       }
/* 464 */       selectValue();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties promptForName()
/*     */   {
/* 470 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 476 */         Properties promptData = this.m_dlgHelper.m_props;
/* 477 */         String name = promptData.getProperty("dpRuleConditionName");
/*     */ 
/* 480 */         if (ScriptClausePanel.this.m_clauseList.findRowPrimaryField(name) < 0)
/*     */         {
/* 482 */           promptData.put("dpRuleConditionName", name);
/* 483 */           return true;
/*     */         }
/* 485 */         this.m_errorMessage = IdcMessageFactory.lc("apDpRuleConditionNameConflict", new Object[0]);
/* 486 */         return false;
/*     */       }
/*     */     };
/* 491 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, this.m_systemInterface.getString("apDpAddRuleConditionTitle"), true);
/*     */ 
/* 493 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("DpAddRuleCondition"));
/*     */ 
/* 496 */     helper.addLabelEditPair(mainPanel, this.m_systemInterface.localizeCaption("apDpConditionLabel"), 30, "dpRuleConditionName");
/*     */ 
/* 500 */     Properties resultProps = null;
/* 501 */     if (helper.prompt() == 1)
/*     */     {
/* 503 */       Properties props = helper.m_props;
/* 504 */       Vector row = this.m_clauseSet.createEmptyRow();
/*     */ 
/* 506 */       String name = props.getProperty("dpRuleConditionName");
/* 507 */       IdcMessage errMsg = Validation.checkFormFieldForDB(name, "apDpRuleName", 0, null);
/* 508 */       if (errMsg != null)
/*     */       {
/* 510 */         reportError(null, errMsg);
/*     */       }
/*     */       else
/*     */       {
/* 514 */         row.setElementAt(name, 0);
/* 515 */         this.m_clauseSet.addRow(row);
/* 516 */         this.m_clauseList.refreshList(this.m_clauseSet, name);
/* 517 */         resultProps = props;
/*     */       }
/*     */     }
/* 520 */     return resultProps;
/*     */   }
/*     */ 
/*     */   protected void selectValue()
/*     */   {
/* 525 */     String title = this.m_systemInterface.getString("apDpFieldSelectTitle");
/* 526 */     String helpPage = DialogHelpTable.getHelpPage("DpFieldSelect");
/* 527 */     SelectFieldDlg dlg = new SelectFieldDlg(this.m_systemInterface, title, helpPage);
/*     */ 
/* 529 */     Properties props = new Properties();
/* 530 */     int result = dlg.init(props);
/* 531 */     if (result != 1)
/*     */       return;
/* 533 */     String val = null;
/* 534 */     String field = "#active." + props.getProperty("ValueField");
/* 535 */     String clmn = props.getProperty("ValueFieldColumn");
/* 536 */     if ((clmn != null) && (clmn.length() > 0))
/*     */     {
/* 538 */       val = "getFieldViewValue(\"" + props.getProperty("ValueField") + "\"," + field + ",\"" + clmn + "\")";
/*     */     }
/*     */     else
/*     */     {
/* 543 */       val = field;
/*     */     }
/* 545 */     this.m_ruleHelper.updateValueField("@" + val);
/*     */   }
/*     */ 
/*     */   protected void refreshClauseList(String name)
/*     */   {
/* 551 */     this.m_clauseList.refreshList(this.m_clauseSet, name);
/* 552 */     checkSelection();
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/* 561 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/* 567 */     saveSelection(true);
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 578 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.ScriptClausePanel
 * JD-Core Version:    0.5.4
 */