/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.ViewChoice;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditProfileDlg
/*     */   implements ComponentBinder, ActionListener, ItemListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*     */   protected Properties m_configProps;
/*     */   protected String m_helpPage;
/*  87 */   protected ExecutionContext m_context = null;
/*  88 */   protected SharedContext m_shContext = null;
/*  89 */   protected SchemaHelper m_schHelper = null;
/*     */ 
/*  92 */   protected UdlPanel m_ruleList = null;
/*  93 */   protected Hashtable m_btnMap = null;
/*     */ 
/*     */   public EditProfileDlg(SystemInterface sys, String title, SharedContext shContext, String helpPage)
/*     */   {
/*  98 */     this.m_helper = new DialogHelper(sys, title, true);
/*  99 */     this.m_context = sys.getExecutionContext();
/* 100 */     this.m_shContext = shContext;
/* 101 */     this.m_systemInterface = sys;
/*     */ 
/* 103 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public boolean init(DataBinder binder, boolean isNew, Properties configProps)
/*     */   {
/* 108 */     this.m_binder = binder;
/* 109 */     this.m_configProps = configProps;
/* 110 */     this.m_helper.m_props = binder.getLocalData();
/* 111 */     if (isNew)
/*     */     {
/* 113 */       this.m_action = "ADD_DOCPROFILE";
/*     */     }
/*     */     else
/*     */     {
/* 117 */       this.m_action = "EDIT_DOCPROFILE";
/*     */     }
/*     */ 
/* 120 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 128 */         boolean isRetry = true;
/* 129 */         EditProfileDlg.this.m_binder.putLocal("isValidateTrigger", "1");
/* 130 */         while (isRetry)
/*     */         {
/*     */           try
/*     */           {
/* 134 */             AppContextUtils.executeService(EditProfileDlg.this.m_shContext, EditProfileDlg.this.m_action, EditProfileDlg.this.m_binder);
/* 135 */             return true;
/*     */           }
/*     */           catch (ServiceException exp)
/*     */           {
/* 139 */             boolean isValidateTrigger = StringUtils.convertToBool(EditProfileDlg.this.m_binder.getLocal("isValidateTrigger"), false);
/*     */ 
/* 141 */             if (isValidateTrigger)
/*     */             {
/* 143 */               EditProfileDlg.this.m_binder.removeLocal("isValidateTrigger");
/* 144 */               IdcMessage msg = new IdcMessage(exp);
/*     */ 
/* 147 */               if (-33 == exp.m_errorCode)
/*     */               {
/* 149 */                 MessageBox.doMessage(EditProfileDlg.this.m_systemInterface, msg, 1);
/* 150 */                 isRetry = false;
/*     */               }
/*     */               else
/*     */               {
/* 154 */                 msg.setPrior(IdcMessageFactory.lc("apDpContinueEdit", new Object[0]));
/* 155 */                 int result = MessageBox.doMessage(EditProfileDlg.this.m_systemInterface, msg, 2);
/*     */ 
/* 157 */                 if (result == 0)
/*     */                 {
/* 159 */                   isRetry = false;
/*     */                 }
/*     */               }
/*     */             }
/*     */             else
/*     */             {
/* 165 */               MessageBox.reportError(EditProfileDlg.this.m_systemInterface, exp);
/* 166 */               isRetry = false;
/*     */             }
/*     */           }
/*     */         }
/* 170 */         return false;
/*     */       }
/*     */     };
/* 173 */     okCallback.m_dlgHelper = this.m_helper;
/* 174 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */     try
/*     */     {
/* 179 */       initUI(mainPanel);
/* 180 */       loadComponents();
/* 181 */       enableDisable();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 185 */       MessageBox.reportError(this.m_systemInterface, e);
/* 186 */       return false;
/*     */     }
/* 188 */     return true;
/*     */   }
/*     */ 
/*     */   public void initUI(JPanel mainPanel) throws ServiceException
/*     */   {
/* 193 */     JPanel pnl = createProfileUI();
/* 194 */     this.m_ruleList = createList();
/*     */ 
/* 196 */     JPanel btnPanel = addButtons();
/*     */ 
/* 198 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 199 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 200 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 201 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 202 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 204 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 205 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 206 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 207 */     this.m_helper.addComponent(mainPanel, this.m_ruleList);
/*     */ 
/* 209 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 210 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 15, 5, 15);
/* 211 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 212 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 213 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 214 */     this.m_helper.addComponent(mainPanel, btnPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel createProfileUI() throws ServiceException
/*     */   {
/* 219 */     PanePanel pnl = new PanePanel();
/* 220 */     this.m_helper.m_gridHelper.useGridBag(pnl);
/* 221 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/*     */ 
/* 223 */     this.m_helper.addLabelDisplayPair(pnl, this.m_systemInterface.localizeCaption("apDpNameLabel"), 30, "dpName");
/* 224 */     this.m_helper.addLabelEditPair(pnl, this.m_systemInterface.localizeCaption("apDpDisplayLabel"), 50, "dpDisplayLabel");
/*     */ 
/* 226 */     this.m_helper.addLabelEditPair(pnl, this.m_systemInterface.localizeCaption("apDpDescriptonLabel"), 50, "dpDescription");
/*     */     try
/*     */     {
/* 234 */       String triggerField = this.m_configProps.getProperty("dpTriggerField");
/*     */ 
/* 236 */       boolean isFound = false;
/* 237 */       DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/* 238 */       ViewFields viewFields = new ViewFields(this.m_context);
/* 239 */       ViewFieldDef fieldDef = null;
/* 240 */       Vector fields = viewFields.createDocumentFieldsList(drset);
/* 241 */       int num = fields.size();
/* 242 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 244 */         fieldDef = (ViewFieldDef)fields.elementAt(i);
/* 245 */         if (!fieldDef.m_name.equals(triggerField))
/*     */           continue;
/* 247 */         isFound = true;
/* 248 */         break;
/*     */       }
/*     */ 
/* 251 */       if (!isFound)
/*     */       {
/* 253 */         throw new ServiceException(null, "apDpTriggerFieldMissing", new Object[0]);
/*     */       }
/* 255 */       Component cmp = determineComponent(fieldDef, 30);
/* 256 */       this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpTriggerValueLabel"), cmp, "dpTriggerValue");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 261 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 265 */     String label = this.m_systemInterface.getString("apLabelEnabled");
/* 266 */     JCheckBox box = new JCheckBox(label);
/* 267 */     this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpExcludeNonRuleFieldsLabel"), box, "dpExludeNonRuleFields");
/*     */ 
/* 270 */     box = new JCheckBox(label);
/* 271 */     label = this.m_systemInterface.getString("apDlgButtonEdit");
/* 272 */     JButton btn = new JButton(label);
/* 273 */     btn.setActionCommand("editLinks");
/* 274 */     btn.addActionListener(this);
/*     */ 
/* 276 */     JCheckBox enabledBox = box;
/* 277 */     JButton editButton = btn;
/* 278 */     ItemListener listener = new ItemListener(enabledBox, editButton)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 282 */         boolean isEnabled = this.val$enabledBox.isSelected();
/* 283 */         this.val$editButton.setEnabled(isEnabled);
/*     */       }
/*     */     };
/* 286 */     enabledBox.addItemListener(listener);
/* 287 */     this.m_helper.addLabelFieldPairEx(pnl, this.m_systemInterface.localizeCaption("apDpRestrictLinks"), box, "dpHasLinkScripts", false);
/*     */ 
/* 291 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(13);
/* 292 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 293 */     this.m_helper.addComponent(pnl, btn);
/*     */ 
/* 296 */     boolean isRestricted = StringUtils.convertToBool(this.m_helper.m_props.getProperty("dpHasLinkScripts"), false);
/*     */ 
/* 299 */     btn.setEnabled(isRestricted);
/*     */ 
/* 301 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected Component determineComponent(ViewFieldDef fieldDef, int minCols)
/*     */     throws ServiceException
/*     */   {
/* 307 */     Component comp = null;
/* 308 */     if (fieldDef.isComplexOptionList())
/*     */     {
/* 311 */       ViewChoice vChoice = new ViewChoice(this.m_systemInterface, this.m_shContext);
/* 312 */       comp = vChoice;
/* 313 */       if (this.m_schHelper == null)
/*     */       {
/* 316 */         this.m_schHelper = new SchemaHelper();
/* 317 */         this.m_schHelper.computeMaps();
/*     */       }
/* 319 */       String btnLabel = this.m_systemInterface.getString("apSelectBtnLabel");
/* 320 */       vChoice.init(this.m_schHelper, fieldDef, minCols, btnLabel);
/*     */     }
/* 322 */     else if (fieldDef.m_isOptionList)
/*     */     {
/* 324 */       ComboChoice choiceList = new ComboChoice(minCols, fieldDef.isMultiOptionList());
/* 325 */       comp = choiceList;
/* 326 */       Vector options = SharedObjects.getOptList(fieldDef.m_optionListKey);
/* 327 */       if (options != null)
/*     */       {
/* 329 */         choiceList.initChoiceList(options);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 335 */       throw new ServiceException(null, "apDpTriggerFieldNotOptionList", new Object[0]);
/*     */     }
/* 337 */     return comp;
/*     */   }
/*     */ 
/*     */   protected UdlPanel createList()
/*     */   {
/* 342 */     String columns = "dpRuleName";
/* 343 */     UdlPanel list = new UdlPanel(this.m_systemInterface.getString("apDpRuleListLabel"), null, 300, 10, "ProfileRules", false);
/*     */ 
/* 347 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDpRuleNameColumn"), "dpRuleName", 8.0D);
/* 348 */     list.setColumnInfo(info);
/*     */ 
/* 350 */     list.setVisibleColumns(columns);
/* 351 */     list.setIDColumn("dpRuleName");
/* 352 */     list.useDefaultListener();
/* 353 */     list.m_list.addActionListener(this);
/* 354 */     list.m_list.addItemListener(this);
/*     */ 
/* 356 */     list.init();
/*     */ 
/* 359 */     this.m_btnMap = new Hashtable();
/* 360 */     JPanel btnPanel = list.getButtonPanel();
/* 361 */     this.m_helper.makePanelGridBag(btnPanel, 0);
/*     */ 
/* 363 */     String[][] btnInfo = { { "up", "apLabelUp", "1" }, { "down", "apLabelDown", "1" } };
/*     */ 
/* 368 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 370 */       String cmd = btnInfo[i][0];
/* 371 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 372 */       JButton btn = list.addButton(this.m_systemInterface.getString(btnInfo[i][1]), isControlled);
/* 373 */       this.m_helper.addComponent(btnPanel, btn);
/* 374 */       btn.addActionListener(this);
/* 375 */       btn.setActionCommand(cmd);
/*     */ 
/* 377 */       this.m_btnMap.put(cmd, btn);
/*     */     }
/*     */ 
/* 380 */     return list;
/*     */   }
/*     */ 
/*     */   protected JPanel addButtons()
/*     */   {
/* 386 */     String[][] btnInfo = { { "add", "apDpDlgButtonAddRule", "0", "apDpAddRuleTitle" }, { "delete", "apDpDlgButtonDeleteRule", "1", "apReadableButtonDeleteRule" } };
/*     */ 
/* 392 */     JPanel btnPanel = new PanePanel();
/* 393 */     this.m_helper.m_gridHelper.useGridBag(btnPanel);
/* 394 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 395 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 397 */       String cmd = btnInfo[i][0];
/* 398 */       if (cmd.equals("space"))
/*     */       {
/* 401 */         btnPanel.add(new PanePanel());
/*     */       }
/*     */       else
/*     */       {
/* 405 */         boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 406 */         JButton btn = this.m_ruleList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_context), isControlled);
/*     */ 
/* 408 */         btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_context));
/* 409 */         btn.setActionCommand(cmd);
/* 410 */         btn.addActionListener(this);
/* 411 */         this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */ 
/* 413 */         if (cmd.equals("add"))
/*     */           continue;
/* 415 */         btn.setEnabled(false);
/*     */       }
/*     */     }
/* 418 */     return btnPanel;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 423 */     boolean isUpEnabled = false;
/* 424 */     boolean isDownEnabled = false;
/* 425 */     int index = this.m_ruleList.getSelectedIndex();
/*     */ 
/* 427 */     DataResultSet drset = (DataResultSet)this.m_ruleList.getResultSet();
/* 428 */     if ((drset != null) && (index >= 0))
/*     */     {
/* 430 */       int numRows = drset.getNumRows();
/* 431 */       if (index > 0)
/*     */       {
/* 433 */         isUpEnabled = true;
/*     */       }
/* 435 */       if ((numRows > 0) && (index < numRows - 1))
/*     */       {
/* 437 */         isDownEnabled = true;
/*     */       }
/*     */     }
/*     */ 
/* 441 */     JButton btn = (JButton)this.m_btnMap.get("up");
/* 442 */     if (btn != null)
/*     */     {
/* 444 */       btn.setEnabled(isUpEnabled);
/*     */     }
/*     */ 
/* 447 */     btn = (JButton)this.m_btnMap.get("down");
/* 448 */     if (btn == null)
/*     */       return;
/* 450 */     btn.setEnabled(isDownEnabled);
/*     */   }
/*     */ 
/*     */   protected void loadComponents()
/*     */   {
/* 456 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("ProfileRules");
/* 457 */     if (drset == null)
/*     */     {
/* 459 */       drset = new DataResultSet(DocProfileScriptUtils.DP_DOCRULE_COLUMNS);
/* 460 */       this.m_binder.addResultSet("ProfileRules", drset);
/*     */     }
/* 462 */     this.m_helper.loadComponentValues();
/* 463 */     this.m_ruleList.refreshList(drset, null);
/*     */   }
/*     */ 
/*     */   public int prompt(DataBinder binder, boolean isNew, Properties configProps)
/*     */   {
/* 468 */     if (!init(binder, isNew, configProps))
/*     */     {
/* 470 */       return 0;
/*     */     }
/*     */ 
/* 473 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 482 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 483 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 488 */     String name = exchange.m_compName;
/* 489 */     String val = exchange.m_compValue;
/*     */ 
/* 491 */     IdcMessage errMsg = null;
/* 492 */     if (name.equals("dpName"))
/*     */     {
/* 494 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apDpName", 0, null);
/*     */     }
/* 496 */     else if (name.equals("dpTriggerValue"))
/*     */     {
/* 498 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 500 */         errMsg = IdcMessageFactory.lc("apDpTriggerValueIsEmpty", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 504 */         boolean hasWildCard = StringUtils.containsWildcards(val);
/* 505 */         if (hasWildCard)
/*     */         {
/* 507 */           errMsg = IdcMessageFactory.lc("apDpTriggerValueIsInvalid", new Object[0]);
/*     */         }
/*     */       }
/*     */     }
/* 511 */     else if ((name.equals("dpDisplayLabel")) && 
/* 513 */       (val.length() == 0))
/*     */     {
/* 515 */       errMsg = IdcMessageFactory.lc("apDpDisplayLabelIsEmpty", new Object[0]);
/*     */     }
/*     */ 
/* 519 */     if (errMsg != null)
/*     */     {
/* 521 */       exchange.m_errorMessage = errMsg;
/* 522 */       return false;
/*     */     }
/* 524 */     return true;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 532 */     String cmd = e.getActionCommand();
/* 533 */     if (cmd.equals("add"))
/*     */     {
/* 535 */       addRule();
/*     */     }
/* 537 */     else if (cmd.equals("editLinks"))
/*     */     {
/* 539 */       String title = LocaleUtils.encodeMessage("apDpEditProfileLinksTitle", null, this.m_binder.getLocal("dpName"));
/*     */ 
/* 541 */       EditProfileLinksDlg dlg = new EditProfileLinksDlg(this.m_systemInterface, LocaleResources.localizeMessage(title, this.m_context), this.m_shContext, DialogHelpTable.getHelpPage("DpEditLinkScript"));
/*     */ 
/* 545 */       dlg.init(this.m_binder);
/*     */     }
/*     */     else
/*     */     {
/* 549 */       int index = this.m_ruleList.getSelectedIndex();
/* 550 */       if (index < 0)
/*     */       {
/* 552 */         MessageBox.reportError(this.m_systemInterface, IdcMessageFactory.lc("apDpPleaseSelectRule", new Object[0]));
/* 553 */         return;
/*     */       }
/*     */ 
/* 556 */       Properties props = this.m_ruleList.getDataAt(index);
/* 557 */       if (cmd.equals("delete"))
/*     */       {
/* 559 */         deleteRule(props);
/*     */       }
/* 561 */       else if (cmd.equals("up"))
/*     */       {
/* 563 */         moveRule(true);
/*     */       } else {
/* 565 */         if (!cmd.equals("down"))
/*     */           return;
/* 567 */         moveRule(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addRule()
/*     */   {
/* 574 */     Properties props = promptNewRule();
/* 575 */     if (props == null)
/*     */       return;
/*     */     try
/*     */     {
/* 579 */       String name = props.getProperty("dpRuleName");
/* 580 */       DataResultSet ruleSet = (DataResultSet)this.m_ruleList.getResultSet();
/*     */ 
/* 582 */       PropParameters params = new PropParameters(props);
/* 583 */       Vector row = ruleSet.createRow(params);
/*     */ 
/* 586 */       String str = props.getProperty("dpRulePriority");
/* 587 */       int index = ResultSetUtils.getIndexMustExist(ruleSet, "dpRulePriority");
/* 588 */       int insertAt = 0;
/* 589 */       for (ruleSet.last(); ruleSet.isRowPresent(); ruleSet.previous())
/*     */       {
/* 591 */         String val = ruleSet.getStringValue(index);
/* 592 */         int curRow = ruleSet.getCurrentRow();
/* 593 */         int cr = comparePriority(str, val);
/* 594 */         if (cr <= 0)
/*     */         {
/* 597 */           insertAt = curRow + 1;
/* 598 */           break;
/*     */         }
/* 600 */         if (curRow == 0) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/* 605 */       ruleSet.insertRowAt(row, insertAt);
/*     */ 
/* 607 */       this.m_ruleList.refreshList(ruleSet, name);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 611 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties promptNewRule()
/*     */   {
/* 618 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 624 */         Properties promptData = this.m_dlgHelper.m_props;
/* 625 */         String name = promptData.getProperty("dpRuleName");
/*     */ 
/* 628 */         if ((name != null) && (EditProfileDlg.this.m_ruleList.findRowPrimaryField(name) < 0))
/*     */         {
/* 630 */           return true;
/*     */         }
/*     */ 
/* 633 */         this.m_errorMessage = IdcMessageFactory.lc("apDpRuleNameConflict", new Object[0]);
/* 634 */         return false;
/*     */       }
/*     */     };
/* 639 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apDpAddRuleTitle", this.m_context), true);
/*     */ 
/* 641 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("DpAddRuleToProfile"));
/*     */ 
/* 644 */     CustomChoice choice = new CustomChoice();
/*     */     try
/*     */     {
/* 647 */       DataResultSet drset = SharedObjects.getTable("DocumentRules");
/* 648 */       if (drset.isEmpty())
/*     */       {
/* 650 */         MessageBox.reportError(this.m_systemInterface, IdcMessageFactory.lc("apDpNoRules", new Object[0]));
/* 651 */         return null;
/*     */       }
/*     */ 
/* 654 */       int index = ResultSetUtils.getIndexMustExist(drset, "dpRuleName");
/* 655 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 657 */         String val = drset.getStringValue(index);
/* 658 */         choice.addItem(val);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 663 */       MessageBox.reportError(this.m_systemInterface, e);
/* 664 */       return null;
/*     */     }
/*     */ 
/* 667 */     helper.addLabelFieldPair(mainPanel, this.m_systemInterface.getString("apDpRuleNameLabel"), choice, "dpRuleName");
/*     */ 
/* 670 */     DisplayChoice prChoice = new DisplayChoice();
/* 671 */     prChoice.init(TableFields.DOCPROFILERULEFIELD_PRIORITIES_OPTIONLIST);
/* 672 */     String label = this.m_systemInterface.getString("apDpRulePriorityLabel");
/* 673 */     helper.addLabelFieldPair(mainPanel, label, prChoice, "dpRulePriority");
/*     */ 
/* 676 */     if (helper.prompt() == 1)
/*     */     {
/* 678 */       Properties props = helper.m_props;
/* 679 */       return props;
/*     */     }
/* 681 */     return null;
/*     */   }
/*     */ 
/*     */   protected void deleteRule(Properties props)
/*     */   {
/* 686 */     String selObj = this.m_ruleList.getSelectedObj();
/* 687 */     if (selObj == null)
/*     */     {
/* 689 */       return;
/*     */     }
/*     */ 
/* 692 */     DataResultSet drset = (DataResultSet)this.m_ruleList.getResultSet();
/* 693 */     Vector row = drset.findRow(0, selObj);
/* 694 */     if (row != null)
/*     */     {
/* 696 */       drset.deleteCurrentRow();
/*     */     }
/* 698 */     this.m_ruleList.refreshList(drset, null);
/*     */   }
/*     */ 
/*     */   protected void moveRule(boolean isUp)
/*     */   {
/* 706 */     int index = this.m_ruleList.getSelectedIndex();
/* 707 */     if (index < 0)
/*     */     {
/* 709 */       return;
/*     */     }
/* 711 */     String name = this.m_ruleList.getSelectedObj();
/* 712 */     DataResultSet drset = (DataResultSet)this.m_ruleList.getResultSet();
/* 713 */     int num = drset.getNumRows();
/*     */ 
/* 715 */     if ((index == 0) && (isUp))
/*     */     {
/* 718 */       return;
/*     */     }
/* 720 */     if ((!isUp) && (index == num - 1))
/*     */     {
/* 723 */       return;
/*     */     }
/*     */ 
/* 726 */     drset.setCurrentRow(index);
/* 727 */     Vector row = drset.getCurrentRowValues();
/*     */     try
/*     */     {
/* 733 */       if (isUp)
/*     */       {
/* 735 */         --index;
/*     */       }
/*     */       else
/*     */       {
/* 739 */         ++index;
/*     */       }
/* 741 */       Vector nRow = drset.getRowValues(index);
/*     */ 
/* 743 */       int prIndex = ResultSetUtils.getIndexMustExist(drset, "dpRulePriority");
/* 744 */       String curPriority = (String)row.elementAt(prIndex);
/* 745 */       String prevPriority = (String)nRow.elementAt(prIndex);
/* 746 */       if (!curPriority.equals(prevPriority))
/*     */       {
/* 748 */         row.setElementAt(prevPriority, prIndex);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 753 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 757 */     drset.deleteCurrentRow();
/* 758 */     drset.insertRowAt(row, index);
/*     */ 
/* 760 */     this.m_ruleList.refreshList(drset, name);
/*     */   }
/*     */ 
/*     */   protected int comparePriority(String val1, String val2)
/*     */   {
/* 765 */     int int1 = 0;
/* 766 */     int int2 = 0;
/* 767 */     if (val1.equals("top"))
/*     */     {
/* 769 */       int1 = 2;
/*     */     }
/* 771 */     else if (val1.equals("middle"))
/*     */     {
/* 773 */       int1 = 1;
/*     */     }
/*     */ 
/* 776 */     if (val2.equals("top"))
/*     */     {
/* 778 */       int2 = 2;
/*     */     }
/* 780 */     else if (val2.equals("middle"))
/*     */     {
/* 782 */       int2 = 1;
/*     */     }
/*     */ 
/* 785 */     int result = 0;
/* 786 */     if (int1 < int2)
/*     */     {
/* 788 */       result = -1;
/*     */     }
/* 790 */     else if (int1 > int2)
/*     */     {
/* 792 */       result = 1;
/*     */     }
/* 794 */     return result;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 802 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 807 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103866 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditProfileDlg
 * JD-Core Version:    0.5.4
 */