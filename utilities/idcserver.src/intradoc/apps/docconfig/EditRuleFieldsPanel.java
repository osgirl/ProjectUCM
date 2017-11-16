/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditRuleFieldsPanel extends DocConfigPanel
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected DataBinder m_ruleData;
/*     */   protected DataResultSet m_fieldSet;
/*     */   protected UdlPanel m_fieldList;
/*     */   protected Hashtable m_btnMap;
/*     */   protected Vector m_fieldDefs;
/*     */ 
/*     */   public EditRuleFieldsPanel()
/*     */   {
/*  69 */     this.m_ruleData = null;
/*  70 */     this.m_fieldSet = null;
/*     */ 
/*  72 */     this.m_fieldList = null;
/*  73 */     this.m_btnMap = null;
/*     */ 
/*  75 */     this.m_fieldDefs = null;
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder data) throws ServiceException
/*     */   {
/*  80 */     super.initEx(sys, data);
/*  81 */     this.m_ruleData = data;
/*  82 */     this.m_btnMap = new Hashtable();
/*     */ 
/*  84 */     this.m_fieldSet = ((DataResultSet)this.m_ruleData.getResultSet("RuleFields"));
/*     */ 
/*  86 */     JPanel panel = initUI(data);
/*  87 */     this.m_helper.makePanelGridBag(this, 1);
/*  88 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  89 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  90 */     this.m_helper.addComponent(this, panel);
/*     */ 
/*  92 */     this.m_fieldList.refreshList(this.m_ruleData, null);
/*  93 */     enableDisable();
/*     */ 
/*  96 */     computeFieldDefinitions();
/*     */   }
/*     */ 
/*     */   protected JPanel initUI(DataBinder binder)
/*     */   {
/* 101 */     JPanel pnl = new PanePanel();
/* 102 */     this.m_helper.makePanelGridBag(pnl, 1);
/*     */ 
/* 104 */     JPanel fieldPnl = initFieldsPanel();
/*     */ 
/* 106 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 107 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 108 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 109 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 110 */     this.m_helper.addComponent(pnl, fieldPnl);
/*     */ 
/* 112 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel initFieldsPanel()
/*     */   {
/* 117 */     this.m_fieldList = createList();
/*     */ 
/* 119 */     JPanel btnPanel = new PanePanel();
/* 120 */     this.m_helper.makePanelGridBag(btnPanel, 0);
/* 121 */     addButtons(btnPanel);
/*     */ 
/* 123 */     JPanel pnl = new PanePanel();
/* 124 */     this.m_helper.makePanelGridBag(pnl, 1);
/* 125 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 126 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 127 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 128 */     this.m_helper.addComponent(pnl, this.m_fieldList);
/*     */ 
/* 130 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 131 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 132 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 133 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 134 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */ 
/* 136 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected UdlPanel createList()
/*     */   {
/* 141 */     String columns = "dpRuleFieldName,dpRuleFieldType";
/* 142 */     UdlPanel list = new UdlPanel(this.m_systemInterface.getString("apDpFieldListLabel"), null, 30, 10, "RuleFields", true);
/*     */ 
/* 146 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDpFieldNameColumn"), "dpRuleFieldName", 8.0D);
/* 147 */     list.setColumnInfo(info);
/* 148 */     info = new ColumnInfo(this.m_systemInterface.getString("apDpTypeColumn"), "dpRuleFieldType", 4.0D);
/* 149 */     list.setColumnInfo(info);
/*     */ 
/* 152 */     list.setIDColumn("dpRuleFieldName");
/* 153 */     list.setVisibleColumns(columns);
/* 154 */     list.useDefaultListener();
/* 155 */     list.setHasSort(false);
/* 156 */     list.m_list.addActionListener(this);
/* 157 */     list.m_list.addItemListener(this);
/*     */ 
/* 159 */     list.init();
/*     */ 
/* 162 */     this.m_btnMap = new Hashtable();
/* 163 */     JPanel btnPanel = list.getButtonPanel();
/* 164 */     this.m_helper.makePanelGridBag(btnPanel, 0);
/*     */ 
/* 166 */     String[][] btnInfo = { { "up", "apLabelUp", "1" }, { "down", "apLabelDown", "1" } };
/*     */ 
/* 171 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 173 */       String cmd = btnInfo[i][0];
/* 174 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 175 */       JButton btn = list.addButton(this.m_systemInterface.getString(btnInfo[i][1]), isControlled);
/* 176 */       this.m_helper.addComponent(btnPanel, btn);
/* 177 */       btn.addActionListener(this);
/* 178 */       btn.setActionCommand(cmd);
/*     */ 
/* 180 */       this.m_btnMap.put(cmd, btn);
/*     */     }
/*     */ 
/* 183 */     return list;
/*     */   }
/*     */ 
/*     */   protected void addButtons(JPanel btnPanel)
/*     */   {
/* 189 */     String[][] btnInfo = { { "add", "apDpDlgButtonAddRuleField", "0", "apDpAddRuleFieldTitle1" }, { "edit", "apDpDlgButtonEditRuleField", "1", "apReadableButtonEditRuleField" }, { "delete", "apDpDlgButtonDeleteRuleField", "1", "apReadableButtonDeleteRuleField" } };
/*     */ 
/* 196 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 197 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(10, 5, 10, 5);
/*     */ 
/* 199 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 201 */       String cmd = btnInfo[i][0];
/* 202 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 203 */       JButton btn = this.m_fieldList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/*     */ 
/* 205 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 206 */       btn.setActionCommand(cmd);
/* 207 */       btn.addActionListener(this);
/* 208 */       this.m_helper.addLastComponentInRow(btnPanel, btn);
/*     */ 
/* 210 */       if (cmd.equals("add"))
/*     */         continue;
/* 212 */       btn.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void computeFieldDefinitions()
/*     */   {
/* 219 */     Vector docFieldDefs = null;
/*     */     try
/*     */     {
/* 222 */       DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/* 223 */       ViewFields docFieldsObj = new ViewFields(this.m_ctx);
/*     */ 
/* 226 */       Vector appFields = docFieldsObj.createAppViewFields("application");
/* 227 */       docFieldsObj.addSpecialProfileRuleFields();
/*     */ 
/* 230 */       docFieldDefs = docFieldsObj.createDocumentFieldsList(drset);
/* 231 */       docFieldsObj.mergeFields(appFields, "");
/*     */     }
/*     */     catch (DataException exp)
/*     */     {
/* 235 */       IdcMessage msg = IdcMessageFactory.lc("apDpUnableToBuildFieldList", new Object[0]);
/* 236 */       reportError(exp, msg);
/*     */     }
/* 238 */     this.m_fieldDefs = docFieldDefs;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 246 */     String cmd = e.getActionCommand();
/* 247 */     if (cmd.equals("add"))
/*     */     {
/* 249 */       Properties props = promptNewFieldName();
/* 250 */       if (props != null)
/*     */       {
/* 252 */         addOrEditRuleField(props, true);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 257 */       int index = this.m_fieldList.getSelectedIndex();
/* 258 */       if (index < 0)
/*     */       {
/* 260 */         reportError(null, IdcMessageFactory.lc("apDpPleaseSelectField", new Object[0]));
/* 261 */         return;
/*     */       }
/*     */ 
/* 264 */       Properties props = this.m_fieldList.getDataAt(index);
/* 265 */       if (cmd.equals("edit"))
/*     */       {
/* 267 */         addOrEditRuleField(props, false);
/*     */       }
/* 269 */       else if (cmd.equals("delete"))
/*     */       {
/* 271 */         deleteRuleField(props);
/*     */       }
/* 273 */       else if (cmd.equals("up"))
/*     */       {
/* 275 */         moveField(true);
/*     */       } else {
/* 277 */         if (!cmd.equals("down"))
/*     */           return;
/* 279 */         moveField(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties promptNewFieldName()
/*     */   {
/* 286 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 292 */         Properties promptData = this.m_dlgHelper.m_props;
/* 293 */         String name = promptData.getProperty("dpRuleFieldName");
/* 294 */         if (name.length() == 0)
/*     */         {
/* 296 */           this.m_errorMessage = IdcMessageFactory.lc("apDpPleaseSelectField", new Object[0]);
/* 297 */           return false;
/*     */         }
/*     */ 
/* 301 */         if (EditRuleFieldsPanel.this.m_fieldList.findRowPrimaryField(name) < 0)
/*     */         {
/* 303 */           return true;
/*     */         }
/* 305 */         this.m_errorMessage = IdcMessageFactory.lc("apDpRuleFieldNameConflict", new Object[0]);
/* 306 */         return false;
/*     */       }
/*     */     };
/* 311 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apDpAddRuleFieldTitle1", this.m_ctx), true);
/*     */ 
/* 313 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("DpAddRuleField"));
/*     */ 
/* 317 */     DisplayChoice fieldCmp = new DisplayChoice(this.m_systemInterface.getString("apDpNoneSpecified"));
/* 318 */     JCheckBox mFieldBox = new CustomCheckbox(this.m_systemInterface.getString("apDprMetaFieldList"));
/* 319 */     JCheckBox aFieldBox = new CustomCheckbox(this.m_systemInterface.getString("apDprAppFieldList"));
/* 320 */     ItemListener listener = new ItemListener(mFieldBox, aFieldBox, fieldCmp)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 324 */         boolean isAddMeta = this.val$mFieldBox.isSelected();
/* 325 */         boolean isAddApp = this.val$aFieldBox.isSelected();
/*     */ 
/* 327 */         EditRuleFieldsPanel.this.updateFieldChoice(this.val$fieldCmp, isAddMeta, isAddApp);
/*     */       }
/*     */     };
/* 330 */     mFieldBox.addItemListener(listener);
/* 331 */     mFieldBox.setSelected(true);
/* 332 */     aFieldBox.addItemListener(listener);
/*     */ 
/* 334 */     helper.addLastComponentInRow(mainPanel, mFieldBox);
/* 335 */     helper.addLastComponentInRow(mainPanel, aFieldBox);
/*     */ 
/* 337 */     updateFieldChoice(fieldCmp, true, false);
/*     */ 
/* 340 */     this.m_helper.addLastComponentInRow(mainPanel, new CustomText(""));
/*     */ 
/* 342 */     helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apLabelFieldName"), fieldCmp, "dpRuleFieldName");
/*     */ 
/* 345 */     DisplayChoice prChoice = new DisplayChoice();
/* 346 */     prChoice.init(TableFields.DOCPROFILERULEFIELD_PRIORITIES_OPTIONLIST);
/* 347 */     String label = this.m_systemInterface.getString("apDprFieldPositionLabel");
/* 348 */     helper.addLabelFieldPair(mainPanel, label, prChoice, "dpRuleFieldPosition");
/*     */ 
/* 351 */     if (helper.prompt() == 1)
/*     */     {
/* 353 */       Properties props = helper.m_props;
/* 354 */       return props;
/*     */     }
/*     */ 
/* 357 */     return null;
/*     */   }
/*     */ 
/*     */   protected void updateFieldChoice(DisplayChoice fieldCmp, boolean isAddMeta, boolean isAddApp)
/*     */   {
/* 362 */     Vector fields = new IdcVector();
/* 363 */     int size = this.m_fieldDefs.size();
/* 364 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 366 */       ViewFieldDef fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(i);
/* 367 */       boolean isAdd = ((fieldDef.m_isAppField) && (isAddApp)) || ((!fieldDef.m_isAppField) && (isAddMeta));
/*     */ 
/* 370 */       if (!isAdd)
/*     */         continue;
/* 372 */       String[] map = new String[2];
/* 373 */       map[0] = fieldDef.m_name;
/* 374 */       map[1] = fieldDef.m_caption;
/* 375 */       fields.addElement(map);
/*     */     }
/*     */ 
/* 379 */     int num = fields.size();
/* 380 */     String[][] display = new String[num][2];
/* 381 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 383 */       display[i] = ((String[])(String[])fields.elementAt(i));
/*     */     }
/* 385 */     fieldCmp.init(display);
/*     */   }
/*     */ 
/*     */   protected void addOrEditRuleField(Properties props, boolean isNew)
/*     */   {
/* 390 */     String helpPage = "DpEditRuleField";
/* 391 */     String titleStub = "apDpAddRuleFieldTitle2";
/* 392 */     if (!isNew)
/*     */     {
/* 394 */       titleStub = "apDpEditRuleFieldTitle";
/*     */     }
/* 396 */     String title = LocaleUtils.encodeMessage(titleStub, null, props.getProperty("dpRuleFieldName"));
/* 397 */     EditFieldRuleDlg dlg = new EditFieldRuleDlg(this.m_systemInterface, title, this, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 400 */     DataBinder binder = promoteOrDeleteFromData(props, isNew, false);
/*     */     try
/*     */     {
/* 403 */       ViewFieldDef fieldDef = getFieldDef(binder);
/* 404 */       int result = dlg.init(binder, fieldDef, isNew);
/* 405 */       if (result == 1)
/*     */       {
/* 407 */         String name = props.getProperty("dpRuleFieldName");
/* 408 */         PropParameters params = new PropParameters(props);
/* 409 */         Vector row = this.m_fieldSet.createRow(params);
/* 410 */         if (isNew)
/*     */         {
/* 413 */           String str = props.getProperty("dpRuleFieldPosition");
/* 414 */           int index = ResultSetUtils.getIndexMustExist(this.m_fieldSet, "dpRuleFieldPosition");
/* 415 */           int insertAt = 0;
/* 416 */           for (this.m_fieldSet.last(); this.m_fieldSet.isRowPresent(); this.m_fieldSet.previous())
/*     */           {
/* 418 */             String val = this.m_fieldSet.getStringValue(index);
/* 419 */             int curRow = this.m_fieldSet.getCurrentRow();
/* 420 */             int cr = comparePriority(str, val);
/* 421 */             if (cr <= 0)
/*     */             {
/* 424 */               insertAt = curRow + 1;
/* 425 */               break;
/*     */             }
/* 427 */             if (curRow == 0) {
/*     */               break;
/*     */             }
/*     */           }
/*     */ 
/* 432 */           this.m_fieldSet.insertRowAt(row, insertAt);
/*     */         }
/*     */         else
/*     */         {
/* 436 */           int nameIndex = ResultSetUtils.getIndexMustExist(this.m_fieldSet, "dpRuleFieldName");
/* 437 */           Vector oldRow = this.m_fieldSet.findRow(nameIndex, name);
/* 438 */           if (oldRow != null)
/*     */           {
/* 440 */             this.m_fieldSet.setRowValues(row, this.m_fieldSet.getCurrentRow());
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 445 */         promoteToRuleData(binder);
/*     */ 
/* 447 */         this.m_fieldList.refreshList(this.m_fieldSet, name);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 452 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ViewFieldDef getFieldDef(DataBinder binder) throws DataException
/*     */   {
/* 458 */     ViewFieldDef fd = null;
/* 459 */     String fieldName = binder.getLocal("dpRuleFieldName");
/* 460 */     if (fieldName != null)
/*     */     {
/* 462 */       int size = this.m_fieldDefs.size();
/* 463 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 465 */         ViewFieldDef fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(i);
/* 466 */         if (!fieldDef.m_name.equals(fieldName))
/*     */           continue;
/* 468 */         fd = fieldDef;
/* 469 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 473 */     if (fd == null)
/*     */     {
/* 475 */       String errMsg = LocaleUtils.encodeMessage("apDpFieldDefMissing", null, fieldName);
/* 476 */       throw new DataException(errMsg);
/*     */     }
/* 478 */     return fd;
/*     */   }
/*     */ 
/*     */   protected DataBinder promoteOrDeleteFromData(Properties props, boolean isNew, boolean isDelete)
/*     */   {
/* 486 */     DataBinder binder = null;
/* 487 */     if (!isDelete)
/*     */     {
/* 489 */       binder = new DataBinder();
/* 490 */       binder.setLocalData(props);
/*     */     }
/*     */     String prefix;
/*     */     Enumeration en;
/* 493 */     if (!isNew)
/*     */     {
/* 495 */       String name = props.getProperty("dpRuleFieldName");
/* 496 */       prefix = name + ".";
/* 497 */       Properties ruleProps = this.m_ruleData.getLocalData();
/* 498 */       for (Enumeration en = ruleProps.keys(); en.hasMoreElements(); )
/*     */       {
/* 500 */         String key = (String)en.nextElement();
/* 501 */         if (key.startsWith(prefix))
/*     */         {
/* 503 */           if (isDelete)
/*     */           {
/* 505 */             this.m_ruleData.removeLocal(key);
/*     */           }
/*     */           else
/*     */           {
/* 509 */             String val = ruleProps.getProperty(key);
/* 510 */             key = key.substring(prefix.length());
/* 511 */             binder.putLocal(key, val);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 516 */       for (en = this.m_ruleData.getResultSetList(); en.hasMoreElements(); )
/*     */       {
/* 518 */         String key = (String)en.nextElement();
/* 519 */         if (key.startsWith(prefix))
/*     */         {
/* 521 */           DataResultSet drset = (DataResultSet)this.m_ruleData.getResultSet(key);
/* 522 */           key = key.substring(prefix.length());
/* 523 */           if (isDelete)
/*     */           {
/* 525 */             this.m_ruleData.removeResultSet(key);
/*     */           }
/*     */           else
/*     */           {
/* 529 */             binder.addResultSet(key, drset);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 534 */     return binder;
/*     */   }
/*     */ 
/*     */   protected void promoteToRuleData(DataBinder binder)
/*     */   {
/* 542 */     String name = binder.getLocal("dpRuleFieldName");
/* 543 */     String prefix = name + ".";
/*     */ 
/* 548 */     int numFields = this.m_fieldSet.getNumFields();
/* 549 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 551 */       String clmn = this.m_fieldSet.getFieldName(i);
/* 552 */       binder.removeLocal(clmn);
/*     */     }
/*     */ 
/* 555 */     Properties props = binder.getLocalData();
/* 556 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*     */     {
/* 558 */       String key = (String)en.nextElement();
/* 559 */       String val = props.getProperty(key);
/* 560 */       key = prefix + key;
/* 561 */       this.m_ruleData.putLocal(key, val);
/*     */     }
/*     */ 
/* 564 */     for (Enumeration en = binder.getResultSetList(); en.hasMoreElements(); )
/*     */     {
/* 566 */       String key = (String)en.nextElement();
/* 567 */       DataResultSet drset = (DataResultSet)binder.getResultSet(key);
/* 568 */       key = prefix + key;
/* 569 */       this.m_ruleData.addResultSet(key, drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteRuleField(Properties props)
/*     */   {
/* 575 */     String name = props.getProperty("dpRuleFieldName");
/*     */ 
/* 577 */     Vector row = this.m_fieldSet.findRow(0, name);
/* 578 */     if (row != null)
/*     */     {
/* 580 */       this.m_fieldSet.deleteCurrentRow();
/*     */     }
/*     */ 
/* 584 */     promoteOrDeleteFromData(props, false, true);
/*     */ 
/* 586 */     this.m_fieldList.refreshList(this.m_ruleData, null);
/*     */   }
/*     */ 
/*     */   protected void moveField(boolean isUp)
/*     */   {
/* 594 */     int index = this.m_fieldList.getSelectedIndex();
/* 595 */     if (index < 0)
/*     */     {
/* 597 */       return;
/*     */     }
/* 599 */     String name = this.m_fieldList.getSelectedObj();
/* 600 */     DataResultSet drset = this.m_fieldSet;
/* 601 */     int num = drset.getNumRows();
/*     */ 
/* 603 */     if ((index == 0) && (isUp))
/*     */     {
/* 606 */       return;
/*     */     }
/* 608 */     if ((!isUp) && (index == num - 1))
/*     */     {
/* 611 */       return;
/*     */     }
/*     */ 
/* 614 */     drset.setCurrentRow(index);
/* 615 */     Vector row = drset.getCurrentRowValues();
/*     */     try
/*     */     {
/* 621 */       if (isUp)
/*     */       {
/* 623 */         --index;
/*     */       }
/*     */       else
/*     */       {
/* 627 */         ++index;
/*     */       }
/* 629 */       Vector nRow = drset.getRowValues(index);
/*     */ 
/* 631 */       int prIndex = ResultSetUtils.getIndexMustExist(drset, "dpRuleFieldPosition");
/* 632 */       String curPriority = (String)row.elementAt(prIndex);
/* 633 */       String prevPriority = (String)nRow.elementAt(prIndex);
/* 634 */       if (!curPriority.equals(prevPriority))
/*     */       {
/* 636 */         row.setElementAt(prevPriority, prIndex);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 641 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 645 */     drset.deleteCurrentRow();
/* 646 */     drset.insertRowAt(row, index);
/*     */ 
/* 648 */     this.m_fieldList.refreshList(drset, name);
/*     */   }
/*     */ 
/*     */   protected int comparePriority(String val1, String val2)
/*     */   {
/* 653 */     int int1 = 0;
/* 654 */     int int2 = 0;
/* 655 */     if (val1.equals("top"))
/*     */     {
/* 657 */       int1 = 2;
/*     */     }
/* 659 */     else if (val1.equals("middle"))
/*     */     {
/* 661 */       int1 = 1;
/*     */     }
/*     */ 
/* 664 */     if (val2.equals("top"))
/*     */     {
/* 666 */       int2 = 2;
/*     */     }
/* 668 */     else if (val2.equals("middle"))
/*     */     {
/* 670 */       int2 = 1;
/*     */     }
/*     */ 
/* 673 */     int result = 0;
/* 674 */     if (int1 < int2)
/*     */     {
/* 676 */       result = -1;
/*     */     }
/* 678 */     else if (int1 > int2)
/*     */     {
/* 680 */       result = 1;
/*     */     }
/* 682 */     return result;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 690 */     enableDisable();
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 695 */     boolean isUpEnabled = false;
/* 696 */     boolean isDownEnabled = false;
/* 697 */     int index = this.m_fieldList.getSelectedIndex();
/*     */ 
/* 699 */     DataResultSet drset = (DataResultSet)this.m_fieldList.getResultSet();
/* 700 */     if ((drset != null) && (index >= 0))
/*     */     {
/* 702 */       int numRows = drset.getNumRows();
/* 703 */       if (index > 0)
/*     */       {
/* 705 */         isUpEnabled = true;
/*     */       }
/* 707 */       if ((numRows > 0) && (index < numRows - 1))
/*     */       {
/* 709 */         isDownEnabled = true;
/*     */       }
/*     */     }
/*     */ 
/* 713 */     JButton btn = (JButton)this.m_btnMap.get("up");
/* 714 */     if (btn != null)
/*     */     {
/* 716 */       btn.setEnabled(isUpEnabled);
/*     */     }
/*     */ 
/* 719 */     btn = (JButton)this.m_btnMap.get("down");
/* 720 */     if (btn == null)
/*     */       return;
/* 722 */     btn.setEnabled(isDownEnabled);
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 734 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99139 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditRuleFieldsPanel
 * JD-Core Version:    0.5.4
 */