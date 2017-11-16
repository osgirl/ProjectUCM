/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.EditOptionListDlg;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.ButtonGroup;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditMetafieldOptionsDlg extends DialogCallback
/*     */   implements ActionListener, ItemListener, ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected SharedContext m_context;
/*     */   protected SchemaHelper m_schemaHelper;
/*     */   protected String m_helpPage;
/*     */   protected DataResultSet m_fields;
/*     */   protected ComponentValidator m_cmpValidator;
/*  88 */   protected boolean m_isAdd = false;
/*     */   protected String m_nameField;
/*     */   protected String m_typeField;
/*     */   protected String m_captionField;
/*     */   protected String m_fieldName;
/*     */   protected String m_actualName;
/*     */   protected DisplayChoice m_relationshipChoices;
/*  98 */   protected Hashtable m_controlMap = null;
/*  99 */   protected Hashtable m_nameToControlMap = null;
/* 100 */   protected Hashtable m_dependentControlMap = null;
/*     */ 
/*     */   public EditMetafieldOptionsDlg(SystemInterface sys, String title, SharedContext sharedContext, SchemaHelper schemaHelper, String helpPage)
/*     */   {
/* 105 */     this.m_helper = new DialogHelper(sys, title, true);
/* 106 */     this.m_systemInterface = sys;
/*     */ 
/* 108 */     this.m_context = sharedContext;
/* 109 */     this.m_schemaHelper = schemaHelper;
/* 110 */     this.m_helpPage = helpPage;
/*     */ 
/* 112 */     this.m_controlMap = new Hashtable();
/* 113 */     this.m_nameToControlMap = new Hashtable();
/* 114 */     this.m_dependentControlMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public int init(Properties props, DataResultSet fields, String nameField, String typeField, String captionField, ComponentValidator cmpValidator, boolean isAdd)
/*     */   {
/* 122 */     this.m_helper.m_props = props;
/* 123 */     this.m_isAdd = isAdd;
/*     */ 
/* 126 */     this.m_nameField = nameField;
/* 127 */     this.m_typeField = typeField;
/* 128 */     this.m_captionField = captionField;
/* 129 */     this.m_fieldName = MetaFieldGui.createDisplayName(props.getProperty(this.m_nameField));
/*     */ 
/* 131 */     this.m_actualName = props.getProperty(this.m_nameField);
/*     */ 
/* 133 */     this.m_fields = fields;
/* 134 */     this.m_cmpValidator = cmpValidator;
/*     */ 
/* 136 */     initUI();
/* 137 */     prepareInfo();
/* 138 */     this.m_helper.loadComponentValues();
/* 139 */     enableDisable();
/* 140 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 145 */     JPanel panel = new PanePanel();
/* 146 */     this.m_helper.makePanelGridBag(panel, 2);
/*     */ 
/* 149 */     DisplayChoice optTypeChoices = new DisplayChoice();
/* 150 */     this.m_nameToControlMap.put("OptionTypes", optTypeChoices);
/* 151 */     optTypeChoices.init(TableFields.METAFIELD_OPTIONLISTTYPE_OPTIONSLIST);
/* 152 */     this.m_helper.addLabelFieldPairEx(panel, this.m_systemInterface.localizeCaption("apLabelOptionListType"), optTypeChoices, "dOptionListType", false);
/*     */ 
/* 155 */     JButton advButton = new JButton(this.m_systemInterface.getString("apLabelAdvancedStorageButton"));
/* 156 */     advButton.setActionCommand("advancedStorage");
/* 157 */     advButton.addActionListener(this);
/* 158 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 159 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 160 */     this.m_helper.addComponent(panel, advButton);
/*     */ 
/* 162 */     JPanel viewPanel = createViewPanel();
/* 163 */     JPanel depPanel = createDependentPanel();
/* 164 */     JPanel mainPanel = this.m_helper.initStandard(this, this, 1, true, this.m_helpPage);
/*     */ 
/* 166 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 167 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 168 */     this.m_helper.addComponent(mainPanel, panel);
/* 169 */     this.m_helper.addComponent(mainPanel, viewPanel);
/* 170 */     this.m_helper.addComponent(mainPanel, depPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel createViewPanel()
/*     */   {
/* 175 */     int minCols = 30;
/* 176 */     PanePanel panel = new PanePanel();
/* 177 */     this.m_helper.makePanelGridBag(panel, 2);
/*     */ 
/* 180 */     String[][] viewOptions = { { "OptionListKey", "apLabelUseOptionListKey", "UseOptionList", "apDlgButtonEdit", "editList", "apReadableButtonEditOptionList" }, { "OptionViewKey", "apLabelUseViewOption", "UseViewList", "apSchDlgButtonEditViewValues", "editView", "apReadableButtonEditViewValues" }, { "TreeDefinition", "apLabelUseTreeOption", "UseTreeControl", "apSchDlgButtonEditTreeDefinition", "editTree", "apReadableButtonEditTreeDef" } };
/*     */ 
/* 187 */     ButtonGroup boxGroup = new ButtonGroup();
/* 188 */     for (int i = 0; i < viewOptions.length; ++i)
/*     */     {
/* 190 */       String boxName = viewOptions[i][2];
/* 191 */       JCheckBox box = new CustomCheckbox(this.m_systemInterface.getString(viewOptions[i][1]), boxGroup);
/* 192 */       box.addItemListener(this);
/*     */ 
/* 194 */       this.m_helper.m_gridHelper.prepareAddRowElement(17);
/* 195 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 196 */       this.m_helper.addExchangeComponent(panel, box, boxName);
/* 197 */       this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*     */ 
/* 199 */       Vector dControls = new IdcVector();
/* 200 */       this.m_dependentControlMap.put(boxName, dControls);
/* 201 */       this.m_controlMap.put(box, boxName);
/* 202 */       this.m_nameToControlMap.put(boxName, box);
/*     */ 
/* 204 */       String name = viewOptions[i][0];
/* 205 */       if (name.equals("OptionListKey"))
/*     */       {
/* 207 */         JTextField tf = new CustomTextField(minCols);
/* 208 */         dControls.addElement(tf);
/* 209 */         this.m_helper.addExchangeComponent(panel, tf, name);
/*     */       }
/* 211 */       else if (name.equals("OptionViewKey"))
/*     */       {
/* 213 */         DisplayChoice viewChoices = createViewChoice();
/* 214 */         dControls.addElement(viewChoices);
/* 215 */         this.m_helper.addExchangeComponent(panel, viewChoices, name);
/*     */       }
/* 217 */       else if (name.equals("TreeDefinition"))
/*     */       {
/* 219 */         this.m_helper.addExchangeComponent(panel, new CustomLabel(), name);
/*     */       }
/*     */ 
/* 222 */       JButton editBtn = new JButton(this.m_systemInterface.getString(viewOptions[i][3]));
/* 223 */       editBtn.getAccessibleContext().setAccessibleName(this.m_systemInterface.getString(viewOptions[i][5]));
/* 224 */       dControls.addElement(editBtn);
/* 225 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 226 */       this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 227 */       this.m_helper.addComponent(panel, editBtn);
/* 228 */       editBtn.addActionListener(this);
/* 229 */       editBtn.setActionCommand(viewOptions[i][4]);
/*     */     }
/* 231 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel createDependentPanel()
/*     */   {
/* 236 */     int minCols = 30;
/* 237 */     JPanel panel = new CustomPanel();
/* 238 */     this.m_helper.makePanelGridBag(panel, 2);
/* 239 */     Vector controls = new IdcVector();
/*     */ 
/* 241 */     JCheckBox depBox = createCheckbox("apTitleEnabled");
/* 242 */     this.m_helper.addLabelFieldPair(panel, this.m_systemInterface.localizeCaption("apLabelDependentField"), depBox, "IsDependentList");
/*     */ 
/* 246 */     this.m_dependentControlMap.put("IsDependentList", controls);
/* 247 */     this.m_controlMap.put(depBox, "IsDependentList");
/* 248 */     this.m_nameToControlMap.put("IsDependentList", depBox);
/*     */ 
/* 250 */     JTextField dependentFieldName = new CustomTextField(minCols);
/* 251 */     controls.addElement(dependentFieldName);
/* 252 */     this.m_helper.addLabelFieldPairEx(panel, this.m_systemInterface.getString("apDependsOnField"), dependentFieldName, "DependentOnField", false);
/*     */ 
/* 255 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 256 */     DisplayChoice fieldChoices = new DisplayChoice();
/* 257 */     controls.addElement(fieldChoices);
/* 258 */     updateFieldChoice(fieldChoices);
/* 259 */     this.m_helper.addExchangeComponent(panel, fieldChoices, "_DependentOnField");
/* 260 */     fieldChoices.addItemListener(this);
/*     */ 
/* 262 */     this.m_relationshipChoices = new DisplayChoice();
/* 263 */     controls.addElement(this.m_relationshipChoices);
/* 264 */     this.m_helper.addLabelFieldPair(panel, this.m_systemInterface.getString("apDependentRelationship"), this.m_relationshipChoices, "DependentRelationship");
/*     */ 
/* 267 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void prepareInfo()
/*     */   {
/* 272 */     Properties props = this.m_helper.m_props;
/*     */ 
/* 274 */     boolean isView = false;
/* 275 */     String isTreeListTmp = props.getProperty("IsTreeList");
/* 276 */     boolean isTree = StringUtils.convertToBool(isTreeListTmp, false);
/*     */ 
/* 278 */     String relationshipParent = null;
/*     */ 
/* 281 */     String key = props.getProperty("dOptionListKey");
/* 282 */     if ((key == null) || (key.length() == 0))
/*     */     {
/* 284 */       key = this.m_fieldName + "List";
/*     */     }
/*     */     else
/*     */     {
/* 288 */       isView = key.startsWith(SchemaHelper.VIEW_PREFIX);
/* 289 */       isTree = key.startsWith(SchemaHelper.TREE_PREFIX);
/*     */     }
/*     */ 
/* 292 */     if (isView)
/*     */     {
/* 294 */       key = key.substring(SchemaHelper.VIEW_PREFIX.length());
/* 295 */       props.put("UseOptionList", "0");
/* 296 */       props.put("UseViewList", "1");
/* 297 */       props.put("UseTreeControl", "0");
/* 298 */       props.put("OptionViewKey", key);
/*     */ 
/* 300 */       relationshipParent = SchemaHelper.VIEW_PREFIX + key;
/*     */     }
/* 302 */     else if (isTree)
/*     */     {
/* 304 */       key = key.substring(SchemaHelper.TREE_PREFIX.length());
/* 305 */       props.put("UseOptionList", "0");
/* 306 */       props.put("UseViewList", "0");
/* 307 */       props.put("UseTreeControl", "1");
/*     */ 
/* 309 */       relationshipParent = SchemaHelper.TREE_PREFIX + props.getProperty("TreeDefinition");
/*     */     }
/*     */     else
/*     */     {
/* 313 */       props.put("UseOptionList", "1");
/* 314 */       props.put("UseViewList", "0");
/* 315 */       props.put("UseTreeControl", "0");
/* 316 */       props.put("OptionListKey", key);
/*     */     }
/* 318 */     updateRelationshipChoice(relationshipParent);
/*     */ 
/* 321 */     String depField = props.getProperty("DependentOnField");
/* 322 */     if ((depField == null) || (depField.length() <= 0))
/*     */       return;
/* 324 */     props.put("_DependentOnField", depField);
/*     */   }
/*     */ 
/*     */   protected void updateFieldChoice(DisplayChoice list)
/*     */   {
/* 330 */     list.m_defaultDisplayStr = this.m_systemInterface.getString("apSchNoFields");
/*     */     try
/*     */     {
/* 333 */       if (this.m_fields == null)
/*     */       {
/* 336 */         throw new DataException("!$Missing metadata fields ResultSet.");
/*     */       }
/*     */ 
/* 339 */       Vector fields = new IdcVector();
/* 340 */       for (this.m_fields.first(); this.m_fields.isRowPresent(); this.m_fields.next())
/*     */       {
/* 342 */         Map row = this.m_fields.getCurrentRowMap();
/* 343 */         String key = (String)row.get(this.m_nameField);
/* 344 */         if (this.m_actualName == null) continue; if (key.equals(this.m_actualName)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 348 */         String[] map = new String[2];
/* 349 */         map[0] = key;
/* 350 */         map[1] = this.m_systemInterface.getString((String)row.get(this.m_captionField));
/* 351 */         fields.addElement(map);
/*     */       }
/*     */ 
/* 354 */       int num = fields.size();
/* 355 */       String[][] display = new String[num][2];
/* 356 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 358 */         display[i] = ((String[])(String[])fields.elementAt(i));
/*     */       }
/* 360 */       list.init(display);
/* 361 */       this.m_controlMap.put(list, "FieldChoices");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 365 */       Report.trace("schema", null, e);
/* 366 */       reportError(e, IdcMessageFactory.lc("apSchErrorCreatingFieldList", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateRelationshipChoice(String key)
/*     */   {
/* 372 */     this.m_relationshipChoices.m_defaultDisplayStr = this.m_systemInterface.getString("apSchNoRelationshipsDefined");
/* 373 */     Vector opts = new IdcVector();
/* 374 */     if ((key != null) && (((key.startsWith(SchemaHelper.VIEW_PREFIX)) || (key.startsWith(SchemaHelper.TREE_PREFIX)))))
/*     */     {
/*     */       try
/*     */       {
/* 380 */         Vector relList = this.m_schemaHelper.computeViewRelations(key);
/*     */ 
/* 382 */         int size = relList.size();
/* 383 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 385 */           SchemaRelationData data = (SchemaRelationData)relList.elementAt(i);
/* 386 */           String relationName = data.get("schRelationName");
/* 387 */           opts.addElement(relationName);
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 392 */         if (SystemUtils.m_verbose)
/*     */         {
/* 394 */           Report.debug("schema", null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 399 */     boolean isEnabled = this.m_relationshipChoices.isEnabled();
/* 400 */     this.m_relationshipChoices.init(opts);
/* 401 */     this.m_relationshipChoices.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 406 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 411 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected JCheckBox createCheckbox(String label)
/*     */   {
/* 416 */     return createCheckboxEx(label, null, false, false);
/*     */   }
/*     */ 
/*     */   protected JCheckBox createCheckboxEx(String label, ButtonGroup group, boolean checked, boolean useCustom)
/*     */   {
/* 422 */     label = this.m_systemInterface.getString(label);
/*     */     JCheckBox cbox;
/* 424 */     if (group != null)
/*     */     {
/*     */       JCheckBox cbox;
/* 426 */       if (useCustom)
/*     */       {
/* 428 */         cbox = new CustomCheckbox(label, group, checked);
/*     */       }
/*     */       else
/*     */       {
/* 432 */         JCheckBox cbox = new JCheckBox(label, checked);
/* 433 */         group.add(cbox);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*     */       JCheckBox cbox;
/* 438 */       if (useCustom)
/*     */       {
/* 440 */         cbox = new CustomCheckbox(label, checked);
/*     */       }
/*     */       else
/*     */       {
/* 444 */         cbox = new JCheckBox(label, checked);
/*     */       }
/*     */     }
/* 447 */     cbox.addItemListener(this);
/* 448 */     return cbox;
/*     */   }
/*     */ 
/*     */   protected DisplayChoice createViewChoice()
/*     */   {
/* 453 */     String defaultStr = this.m_systemInterface.getString("apSchNoViewsAvailable");
/* 454 */     DisplayChoice viewChoice = new DisplayChoice(defaultStr);
/*     */     try
/*     */     {
/* 458 */       Vector opts = new IdcVector();
/* 459 */       DataResultSet viewConfig = SharedObjects.getTable("SchemaViewConfig");
/* 460 */       if (viewConfig == null)
/*     */       {
/* 463 */         throw new DataException("!$Missing the SchemaViewConfig object.");
/*     */       }
/*     */ 
/* 466 */       int index = ResultSetUtils.getIndexMustExist(viewConfig, "schViewName");
/* 467 */       for (viewConfig.first(); viewConfig.isRowPresent(); viewConfig.next())
/*     */       {
/* 469 */         String key = viewConfig.getStringValue(index);
/* 470 */         opts.addElement(key);
/*     */       }
/* 472 */       viewChoice.init(opts);
/* 473 */       viewChoice.addItemListener(this);
/*     */ 
/* 475 */       this.m_controlMap.put(viewChoice, "ViewChoices");
/* 476 */       this.m_nameToControlMap.put("ViewChoices", viewChoice);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 480 */       Report.trace("schema", null, e);
/* 481 */       reportError(e, IdcMessageFactory.lc("apSchErrorCreatingViewList", new Object[0]));
/*     */     }
/*     */ 
/* 484 */     return viewChoice;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 492 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 493 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 498 */     String name = exchange.m_compName;
/* 499 */     String val = exchange.m_compValue;
/*     */ 
/* 501 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 506 */     IdcMessage errMsg = null;
/*     */ 
/* 508 */     if (name.equals("dOptionListType"))
/*     */     {
/* 510 */       if ((this.m_helper.m_props.getProperty(this.m_typeField).equalsIgnoreCase("Int")) && (val.toLowerCase().startsWith("multi")))
/*     */       {
/* 513 */         errMsg = IdcMessageFactory.lc("apIntegerFieldsCannotBeMultiselect", new Object[0]);
/*     */       }
/*     */     }
/* 516 */     else if ((this.m_helper.m_props.getProperty(this.m_typeField).equalsIgnoreCase("Decimal")) && (val.toLowerCase().startsWith("multi")))
/*     */     {
/* 519 */       errMsg = IdcMessageFactory.lc("apDecimalFieldsCannotBeMultiselect", new Object[0]);
/*     */     }
/*     */ 
/* 522 */     if (errMsg != null)
/*     */     {
/* 524 */       exchange.m_errorMessage = errMsg;
/* 525 */       return false;
/*     */     }
/*     */ 
/* 528 */     return true;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 536 */     this.m_helper.retrieveComponentValues();
/* 537 */     this.m_helper.handleActionPerformed(e, this, this.m_systemInterface);
/*     */   }
/*     */ 
/*     */   public void editList()
/*     */   {
/* 543 */     String key = this.m_helper.m_props.getProperty("OptionListKey");
/* 544 */     key = key.trim();
/* 545 */     if (key.length() == 0)
/*     */     {
/* 547 */       reportError(null, IdcMessageFactory.lc("apSpecifyOptionListKey", new Object[0]));
/* 548 */       return;
/*     */     }
/* 550 */     launchOptionListDialog(key);
/*     */   }
/*     */ 
/*     */   public void editView()
/*     */   {
/* 556 */     String key = this.m_helper.m_props.getProperty("OptionViewKey");
/* 557 */     if (key == null)
/*     */     {
/* 559 */       key = "";
/*     */     }
/* 561 */     key = key.trim();
/* 562 */     if (key.length() == 0)
/*     */     {
/* 564 */       reportError(null, IdcMessageFactory.lc("apSpecifyOptionViewKey", new Object[0]));
/* 565 */       return;
/*     */     }
/*     */ 
/* 570 */     SchemaViewData viewData = this.m_schemaHelper.getView(key);
/* 571 */     String table = viewData.get("schTableName");
/* 572 */     if ((table != null) && (table.equals("OptionsList")))
/*     */     {
/* 574 */       launchOptionListDialog(key);
/*     */     }
/*     */     else
/*     */     {
/* 578 */       Properties props = new Properties();
/* 579 */       props.put("schViewName", key);
/* 580 */       props.put("FieldName", this.m_actualName);
/*     */ 
/* 582 */       String title = LocaleUtils.encodeMessage("apSchEditViewValuesTitle", null, key);
/* 583 */       title = this.m_systemInterface.localizeMessage(title);
/* 584 */       EditViewValuesDlg dlg = new EditViewValuesDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("EditViewValues"));
/*     */ 
/* 586 */       dlg.init(props);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void editTree()
/*     */   {
/* 592 */     Properties props = (Properties)this.m_helper.m_props.clone();
/*     */ 
/* 594 */     String definition = props.getProperty("TreeDefinition");
/* 595 */     if (definition == null)
/*     */     {
/* 597 */       definition = "";
/*     */     }
/* 599 */     definition = definition.trim();
/*     */ 
/* 601 */     props.put("TreeDefinition", definition);
/*     */ 
/* 603 */     String title = LocaleUtils.encodeMessage("apSchEditTreeDefinitionTitle", null, props.getProperty("FieldName"));
/* 604 */     title = this.m_systemInterface.localizeMessage(title);
/* 605 */     EditTreeDefinitionDialog dlg = new EditTreeDefinitionDialog(this.m_systemInterface, this.m_schemaHelper, title, DialogHelpTable.getHelpPage("EditTreeDefinition"));
/*     */ 
/* 608 */     dlg.init(props, this.m_context, this.m_isAdd);
/* 609 */     if (dlg.prompt() != 1)
/*     */       return;
/* 611 */     DataBinder.mergeHashTables(this.m_helper.m_props, props);
/*     */ 
/* 613 */     definition = props.getProperty("TreeDefinition");
/* 614 */     if (definition.length() == 0)
/*     */     {
/* 616 */       definition = this.m_systemInterface.getString("apDatabaseColumnNotSelected");
/*     */     }
/* 618 */     this.m_helper.m_exchange.setComponentValue("TreeDefinition", definition);
/* 619 */     updateRelationshipChoice(SchemaHelper.TREE_PREFIX + definition);
/*     */   }
/*     */ 
/*     */   public void advancedStorage()
/*     */   {
/* 625 */     String title = LocaleUtils.encodeMessage("apTitleAdvancedMetaStorage", null, this.m_fieldName);
/*     */ 
/* 627 */     String helpPage = DialogHelpTable.getHelpPage("AdvancedMetaStorage");
/* 628 */     EditOptionStorageDlg dlg = new EditOptionStorageDlg(this.m_systemInterface, title, this.m_context, helpPage);
/*     */ 
/* 631 */     DisplayChoice optionTypes = (DisplayChoice)this.m_nameToControlMap.get("OptionTypes");
/*     */ 
/* 633 */     String optType = optionTypes.getSelectedInternalValue();
/* 634 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 635 */     int rc = dlg.init(props, optType, this.m_isAdd);
/* 636 */     if (rc != 1)
/*     */       return;
/* 638 */     DataBinder.mergeHashTables(this.m_helper.m_props, props);
/*     */   }
/*     */ 
/*     */   protected void launchOptionListDialog(String key)
/*     */   {
/* 644 */     String title = this.m_systemInterface.getString("apLabelOptionList");
/*     */ 
/* 646 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 647 */     props.put("dOptionListKey", key);
/* 648 */     EditOptionListDlg edtOptions = new EditOptionListDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("OptionList"), "UPDATE_OPTION_LIST");
/*     */ 
/* 650 */     edtOptions.init(props);
/* 651 */     edtOptions.prompt();
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 659 */     int state = e.getStateChange();
/* 660 */     Object source = e.getSource();
/* 661 */     String name = (String)this.m_controlMap.get(source);
/*     */ 
/* 663 */     boolean stateChanged = false;
/* 664 */     String optionListKey = "";
/* 665 */     String useOptionList = "0";
/* 666 */     String useViewList = "0";
/* 667 */     String useTreeControl = "0";
/* 668 */     String viewName = null;
/* 669 */     if (name.equals("UseOptionList"))
/*     */     {
/* 671 */       if (state == 1)
/*     */       {
/* 673 */         stateChanged = true;
/* 674 */         optionListKey = this.m_fieldName + "List";
/* 675 */         useOptionList = "1";
/* 676 */         viewName = optionListKey;
/*     */       }
/*     */     }
/* 679 */     else if (name.equals("UseViewList"))
/*     */     {
/* 681 */       if (state == 1)
/*     */       {
/* 683 */         stateChanged = true;
/* 684 */         useViewList = "1";
/*     */ 
/* 686 */         DisplayChoice viewChoices = (DisplayChoice)this.m_nameToControlMap.get("ViewChoices");
/* 687 */         viewName = SchemaHelper.VIEW_PREFIX + viewChoices.getSelectedInternalValue();
/*     */       }
/*     */     }
/* 690 */     else if (name.equals("UseTreeControl"))
/*     */     {
/* 692 */       if (state == 1)
/*     */       {
/* 694 */         stateChanged = true;
/* 695 */         useTreeControl = "1";
/*     */ 
/* 697 */         String treeDef = this.m_helper.m_exchange.getComponentValue("TreeDefinition");
/* 698 */         if ((treeDef != null) && (treeDef.length() > 0))
/*     */         {
/* 700 */           viewName = SchemaHelper.TREE_PREFIX + treeDef;
/*     */         }
/*     */         else
/*     */         {
/* 704 */           viewName = "";
/*     */         }
/*     */       }
/*     */     }
/* 708 */     else if (name.equals("IsDependentList"))
/*     */     {
/* 710 */       JCheckBox box = (JCheckBox)source;
/* 711 */       boolean isEnabled = box.isSelected();
/*     */ 
/* 713 */       Vector controls = (Vector)this.m_dependentControlMap.get(name);
/* 714 */       enableDisable(controls, isEnabled);
/*     */     }
/* 716 */     else if (name.equals("FieldChoices"))
/*     */     {
/* 718 */       DisplayChoice fieldChoices = (DisplayChoice)source;
/* 719 */       this.m_helper.m_exchange.setComponentValue("DependentOnField", fieldChoices.getSelectedInternalValue());
/*     */     }
/* 721 */     else if ((name.equals("ViewChoices")) && 
/* 724 */       (state == 1))
/*     */     {
/* 726 */       DisplayChoice viewChoices = (DisplayChoice)source;
/* 727 */       viewName = SchemaHelper.VIEW_PREFIX + viewChoices.getSelectedInternalValue();
/*     */     }
/*     */ 
/* 731 */     if (stateChanged)
/*     */     {
/* 733 */       this.m_helper.m_exchange.setComponentValue("OptionListKey", optionListKey);
/* 734 */       this.m_helper.m_exchange.setComponentValue("UseOptionList", useOptionList);
/* 735 */       this.m_helper.m_exchange.setComponentValue("UseViewList", useViewList);
/* 736 */       this.m_helper.m_exchange.setComponentValue("UseTreeControl", useTreeControl);
/*     */ 
/* 738 */       if (StringUtils.convertToBool(useOptionList, false))
/*     */       {
/* 740 */         this.m_helper.m_exchange.setComponentValue("IsDependentList", "0");
/*     */       }
/* 742 */       enableDisable();
/*     */     }
/* 744 */     if (viewName == null)
/*     */       return;
/* 746 */     updateRelationshipChoice(viewName);
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 752 */     boolean isOptionList = false;
/* 753 */     for (Enumeration en = this.m_dependentControlMap.keys(); en.hasMoreElements(); )
/*     */     {
/* 755 */       String key = (String)en.nextElement();
/* 756 */       JCheckBox cmp = (JCheckBox)this.m_nameToControlMap.get(key);
/* 757 */       boolean isEnabled = cmp.isSelected();
/*     */ 
/* 759 */       Vector v = (Vector)this.m_dependentControlMap.get(key);
/* 760 */       enableDisable(v, isEnabled);
/*     */ 
/* 762 */       if (key.equals("UseOptionList"))
/*     */       {
/* 764 */         isOptionList = isEnabled;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 770 */     JCheckBox cmp = (JCheckBox)this.m_nameToControlMap.get("IsDependentList");
/* 771 */     if (isOptionList)
/*     */     {
/* 773 */       cmp.setSelected(!isOptionList);
/*     */     }
/* 775 */     cmp.setEnabled(!isOptionList);
/*     */ 
/* 777 */     boolean isDependent = cmp.isSelected();
/* 778 */     Vector v = (Vector)this.m_dependentControlMap.get("IsDependentList");
/* 779 */     enableDisable(v, isDependent);
/*     */   }
/*     */ 
/*     */   protected void enableDisable(Vector controls, boolean isEnabled)
/*     */   {
/* 784 */     int size = controls.size();
/* 785 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 787 */       Component cmp = (Component)controls.elementAt(i);
/* 788 */       cmp.setEnabled(isEnabled);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent e)
/*     */   {
/* 798 */     this.m_errorMessage = null;
/*     */     try
/*     */     {
/* 801 */       String optionKey = null;
/* 802 */       Properties props = this.m_helper.m_props;
/*     */ 
/* 804 */       boolean isView = StringUtils.convertToBool(props.getProperty("UseViewList"), false);
/*     */ 
/* 806 */       boolean isTree = StringUtils.convertToBool(props.getProperty("UseTreeControl"), false);
/*     */ 
/* 808 */       if (isView)
/*     */       {
/* 810 */         optionKey = props.getProperty("OptionViewKey");
/*     */       }
/* 812 */       else if (isTree)
/*     */       {
/* 814 */         optionKey = props.getProperty("TreeDefinition");
/*     */       }
/*     */       else
/*     */       {
/* 818 */         optionKey = props.getProperty("OptionListKey");
/*     */       }
/*     */ 
/* 821 */       if ((optionKey == null) || (optionKey.length() == 0))
/*     */       {
/* 823 */         this.m_errorMessage = IdcMessageFactory.lc("apSpecifyOptionListKey", new Object[0]);
/* 824 */         return false;
/*     */       }
/*     */ 
/* 828 */       int maxLength = this.m_cmpValidator.getMaxLength("dOptionListKey", 50);
/* 829 */       if ((!isView) && (!isTree) && (optionKey.length() > maxLength))
/*     */       {
/* 831 */         this.m_errorMessage = IdcMessageFactory.lc("apOptionListKeyExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/* 832 */         return false;
/*     */       }
/*     */ 
/* 835 */       if ((isView) && (!optionKey.startsWith(SchemaHelper.VIEW_PREFIX)))
/*     */       {
/* 837 */         optionKey = SchemaHelper.VIEW_PREFIX + optionKey;
/*     */       }
/* 839 */       if ((isTree) && (!optionKey.startsWith(SchemaHelper.TREE_PREFIX)))
/*     */       {
/* 841 */         optionKey = SchemaHelper.TREE_PREFIX + optionKey;
/*     */       }
/* 843 */       props.put("dOptionListKey", optionKey);
/*     */ 
/* 848 */       boolean isDependent = StringUtils.convertToBool(props.getProperty("IsDependentList"), false);
/*     */ 
/* 850 */       if (isDependent)
/*     */       {
/* 852 */         String parentField = props.getProperty("DependentOnField");
/* 853 */         String depRel = props.getProperty("DependentRelationship");
/*     */ 
/* 855 */         if ((parentField == null) || (parentField.length() == 0))
/*     */         {
/* 857 */           this.m_errorMessage = IdcMessageFactory.lc("apDependentFieldMissingParent", new Object[0]);
/*     */         }
/* 859 */         else if ((depRel == null) || (depRel.length() == 0))
/*     */         {
/* 861 */           this.m_errorMessage = IdcMessageFactory.lc("apDependentFieldMissingRelationship", new Object[0]);
/*     */         }
/* 863 */         if (this.m_errorMessage != null)
/*     */         {
/* 865 */           return false;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 870 */       Vector opts = null;
/* 871 */       if ((!isView) && (!isTree))
/*     */       {
/* 873 */         opts = SharedObjects.getOptList(optionKey);
/*     */       }
/*     */ 
/* 876 */       boolean hasNonEmptyValues = false;
/* 877 */       if (opts != null)
/*     */       {
/* 879 */         int size = opts.size();
/* 880 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 882 */           String str = (String)opts.elementAt(i);
/* 883 */           if (str.length() <= 0)
/*     */             continue;
/* 885 */           hasNonEmptyValues = true;
/* 886 */           break;
/*     */         }
/*     */       }
/*     */ 
/* 890 */       if ((!isView) && (!isTree) && (((opts == null) || (!hasNonEmptyValues))))
/*     */       {
/* 892 */         this.m_errorMessage = IdcMessageFactory.lc("apSpecifyOptionListValues", new Object[0]);
/* 893 */         return false;
/*     */       }
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 898 */       reportError(exp, IdcMessageFactory.lc("apErrorEditingMetaFieldOptions", new Object[0]));
/* 899 */       return false;
/*     */     }
/* 901 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 906 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditMetafieldOptionsDlg
 * JD-Core Version:    0.5.4
 */