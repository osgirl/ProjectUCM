/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.SelectValueDlg;
/*     */ import intradoc.shared.schema.SchemaFieldConfig;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.math.BigDecimal;
/*     */ import java.math.MathContext;
/*     */ import java.math.RoundingMode;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditMetafieldDlg extends DialogCallback
/*     */   implements ComponentBinder, ItemListener, ActionListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected SharedContext m_context;
/*     */   protected boolean m_isAdd;
/*     */   protected String m_action;
/*     */   protected String m_helpPage;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   protected DataResultSet m_fields;
/*     */   protected String m_fieldName;
/*     */   protected DisplayChoice m_typeChoices;
/*     */   protected DisplayChoice m_decimalScale;
/*     */   protected DisplayChoice m_dmsTable;
/*     */   protected JTextField m_defaultField;
/*     */   protected JButton m_selectDefaultButton;
/*     */   protected JCheckBox m_isOptionListCheckBox;
/*     */   protected JCheckBox m_placeholderCheckbox;
/*     */   protected JCheckBox m_searchableCheckbox;
/*     */   protected JButton m_optionsButton;
/*     */   protected String[][] m_dmsTableList;
/*  99 */   protected SchemaHelper m_schemaHelper = null;
/*     */ 
/*     */   public EditMetafieldDlg(SystemInterface sys, String title, DataResultSet rset, boolean isAdd, String helpPage)
/*     */   {
/* 104 */     this.m_helper = new DialogHelper(sys, title, true);
/* 105 */     this.m_systemInterface = sys;
/* 106 */     this.m_helpPage = helpPage;
/* 107 */     this.m_fields = rset;
/* 108 */     this.m_isAdd = isAdd;
/* 109 */     if (isAdd)
/*     */     {
/* 111 */       this.m_action = "ADD_METADEF";
/*     */     }
/*     */     else
/*     */     {
/* 115 */       this.m_action = "EDIT_METADEF";
/*     */     }
/* 117 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */   }
/*     */ 
/*     */   public void init(Properties data, SharedContext sharedContext)
/*     */   {
/* 122 */     this.m_context = sharedContext;
/* 123 */     SystemInterface si = this.m_systemInterface;
/*     */ 
/* 126 */     this.m_helper.m_props = data;
/*     */ 
/* 129 */     this.m_fieldName = MetaFieldGui.getDispFieldName(data);
/*     */ 
/* 132 */     this.m_schemaHelper = new SchemaHelper();
/* 133 */     this.m_schemaHelper.computeMaps();
/*     */ 
/* 136 */     JPanel mainPanel = this.m_helper.initStandard(this, this, 2, true, this.m_helpPage);
/*     */ 
/* 138 */     JPanel curPanel = mainPanel;
/*     */ 
/* 141 */     int minCols = 30;
/* 142 */     String[][] list = getTypeChoiceList(data);
/* 143 */     this.m_typeChoices = new DisplayChoice();
/* 144 */     this.m_typeChoices.init(list);
/* 145 */     this.m_typeChoices.addItemListener(this);
/*     */ 
/* 147 */     String[][] scaleList = getDecimalScaleList(data);
/* 148 */     this.m_decimalScale = new DisplayChoice();
/* 149 */     this.m_decimalScale.init(scaleList);
/*     */ 
/* 151 */     this.m_dmsTableList = getDMSTableList();
/* 152 */     this.m_dmsTable = new DisplayChoice();
/* 153 */     this.m_dmsTable.init(this.m_dmsTableList);
/*     */ 
/* 155 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelFieldCaption"), new CustomTextField(minCols), "dCaption");
/*     */ 
/* 157 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelFieldType"), this.m_typeChoices, "dType");
/*     */ 
/* 159 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelDecimalScale"), this.m_decimalScale, "dDecimalScale");
/*     */ 
/* 161 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelMetaSet"), this.m_dmsTable, "dDocMetaSet");
/*     */ 
/* 163 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelFieldOrder"), new CustomTextField(3), "dOrder");
/*     */ 
/* 167 */     this.m_helper.addLabelFieldPairEx(curPanel, this.m_systemInterface.localizeCaption("apLabelDefaultValue"), this.m_defaultField = new CustomTextField(minCols), "dDefaultValue", false);
/*     */ 
/* 170 */     this.m_selectDefaultButton = new JButton(this.m_systemInterface.getString("apLabelSelectButton"));
/* 171 */     this.m_selectDefaultButton.setActionCommand("selectDefault");
/* 172 */     this.m_selectDefaultButton.addActionListener(this);
/* 173 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 174 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 175 */     this.m_helper.addComponent(curPanel, this.m_selectDefaultButton);
/* 176 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*     */ 
/* 178 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelRequireValue"), createCheckbox("apTitleRequired"), "dIsRequired");
/*     */ 
/* 180 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelPlaceholderField"), this.m_placeholderCheckbox = createCheckbox("apTitleEnabled"), "dIsPlaceholderField");
/*     */ 
/* 182 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelEnableOnUserInterface"), createCheckbox("apTitleEnabled"), "dIsEnabled");
/*     */ 
/* 184 */     this.m_helper.addLabelFieldPair(curPanel, si.localizeCaption("apLabelEnableForSearchIndex"), this.m_searchableCheckbox = createCheckbox("apTitleIndexed"), "dIsSearchable");
/*     */ 
/* 187 */     this.m_isOptionListCheckBox = createCheckbox("apLabelOptionList");
/* 188 */     this.m_isOptionListCheckBox.addItemListener(this);
/* 189 */     this.m_helper.addLabelFieldPairEx(curPanel, si.localizeCaption("apLabelEnableOptionList"), this.m_isOptionListCheckBox, "dIsOptionList", false);
/*     */ 
/* 193 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 194 */     this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 195 */     this.m_optionsButton = new JButton(si.getString("apOptionConfigureButton"));
/* 196 */     this.m_helper.addComponent(curPanel, this.m_optionsButton);
/* 197 */     this.m_optionsButton.setActionCommand("configureOptionList");
/* 198 */     this.m_optionsButton.addActionListener(this);
/*     */ 
/* 200 */     if (this.m_isAdd)
/*     */     {
/* 202 */       data.setProperty("dDocMetaSet", "DocMeta");
/*     */     }
/*     */     else
/*     */     {
/* 206 */       String dDocMetaSet = data.getProperty("dDocMetaSet");
/* 207 */       if ((dDocMetaSet == null) || (dDocMetaSet.length() == 0))
/*     */       {
/* 209 */         data.setProperty("dDocMetaSet", "DocMeta");
/*     */       }
/* 211 */       this.m_dmsTable.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String[][] getTypeChoiceList(Properties data) {
/* 216 */     String listStr = "";
/* 217 */     String type = data.getProperty("dOldType");
/* 218 */     if (type == null)
/*     */     {
/* 220 */       type = data.getProperty("dType");
/*     */     }
/* 222 */     listStr = "Text,BigText,Date,Memo,Int,Decimal";
/*     */ 
/* 224 */     if (!isNewField(data))
/*     */     {
/* 227 */       if ((type.equalsIgnoreCase("Date")) || (type.equalsIgnoreCase("Integer")) || (type.equalsIgnoreCase("Memo")) || (type.equalsIgnoreCase("Int")) || (type.equalsIgnoreCase("Decimal")))
/*     */       {
/* 232 */         listStr = type;
/*     */       }
/* 234 */       else if (type.equalsIgnoreCase("Text"))
/*     */       {
/* 236 */         listStr = "Text,BigText,Memo";
/*     */       }
/* 238 */       else if (type.equalsIgnoreCase("BigText"))
/*     */       {
/* 240 */         listStr = "BigText,Memo";
/*     */       }
/*     */     }
/*     */ 
/* 244 */     Vector list = StringUtils.parseArray(listStr, ',', ',');
/* 245 */     int size = list.size();
/* 246 */     String[][] choiceList = new String[size][2];
/* 247 */     for (int i = 0; i < TableFields.METAFIELD_TYPES_OPTIONSLIST.length; ++i)
/*     */     {
/* 249 */       for (int j = 0; j < size; ++j)
/*     */       {
/* 251 */         String tmp = (String)list.elementAt(j);
/* 252 */         if (tmp.equalsIgnoreCase(TableFields.METAFIELD_TYPES_OPTIONSLIST[i][0]))
/*     */         {
/* 254 */           choiceList[j] = TableFields.METAFIELD_TYPES_OPTIONSLIST[i];
/*     */         }
/*     */         else
/*     */         {
/* 259 */           if (!tmp.equals("Decimal"))
/*     */             continue;
/* 261 */           choiceList[j][0] = tmp;
/* 262 */           String displayText = this.m_systemInterface.getString("apLabelDecimalFieldType");
/* 263 */           choiceList[j][1] = displayText;
/*     */         }
/*     */       }
/*     */     }
/* 267 */     return choiceList;
/*     */   }
/*     */ 
/*     */   private boolean isNewField(Properties data) {
/* 271 */     String isNewFieldStr = data.getProperty("isNewField");
/* 272 */     boolean isNewField = StringUtils.convertToBool(isNewFieldStr, false);
/* 273 */     return isNewField;
/*     */   }
/*     */ 
/*     */   private String[][] createDefaultNumberList(String defaultValue) {
/* 277 */     String[][] choiceList = new String[1][2];
/* 278 */     choiceList[0][0] = defaultValue;
/* 279 */     choiceList[0][1] = defaultValue;
/* 280 */     return choiceList;
/*     */   }
/*     */ 
/*     */   protected String[][] getDecimalScaleList(Properties data)
/*     */   {
/*     */     String[][] choiceList;
/*     */     String[][] choiceList;
/* 285 */     if (isNewField(data)) {
/* 286 */       choiceList = getDecimalScaleList();
/*     */     }
/*     */     else {
/* 289 */       String decimalScale = data.getProperty("dDecimalScale");
/* 290 */       choiceList = createDefaultNumberList(decimalScale);
/*     */     }
/*     */ 
/* 293 */     return choiceList;
/*     */   }
/*     */ 
/*     */   private String[][] getDecimalScaleList()
/*     */   {
/* 301 */     int listLength = SharedObjects.getEnvironmentInt("NumDecimalScale", 31);
/* 302 */     String[][] choiceList = new String[listLength][2];
/* 303 */     for (int i = 1; i <= listLength; ++i)
/*     */     {
/* 305 */       choiceList[(i - 1)] = { "" + i, "" + i };
/*     */     }
/* 307 */     return choiceList;
/*     */   }
/*     */ 
/*     */   private String[][] getDMSTableList() {
/* 311 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 314 */       AppLauncher.executeService("GET_DOCMETASETS", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */     }
/*     */ 
/* 320 */     DataResultSet rset = (DataResultSet)binder.getResultSet("DocMetaSets");
/* 321 */     if ((rset == null) || (rset.isEmpty()))
/*     */     {
/* 323 */       String[][] choiceList = new String[1][2];
/* 324 */       choiceList[0] = { "DocMeta", "DocMeta" };
/* 325 */       return choiceList;
/*     */     }
/*     */ 
/* 328 */     String[][] choiceList = new String[rset.getNumRows()][2];
/*     */ 
/* 330 */     choiceList[0] = { "DocMeta", "DocMeta" };
/* 331 */     int i = 1;
/* 332 */     for (rset.first(); rset.isRowPresent(); ++i)
/*     */     {
/* 334 */       String choice = rset.getStringValueByName("dDocMetaSet");
/* 335 */       if (choice.equalsIgnoreCase("DocMeta"))
/*     */       {
/* 337 */         --i;
/*     */       }
/*     */       else
/* 340 */         choiceList[i] = { choice, choice };
/* 332 */       rset.next();
/*     */     }
/*     */ 
/* 342 */     return choiceList;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 347 */     String item = this.m_typeChoices.getSelectedInternalValue();
/* 348 */     boolean optionListEnabled = this.m_isOptionListCheckBox.isSelected();
/* 349 */     boolean allowOptionList = MetaFieldUtils.allowOptionList(item);
/* 350 */     if ((!allowOptionList) && (optionListEnabled == true))
/*     */     {
/* 352 */       this.m_helper.m_props.put("dIsOptionList", "0");
/* 353 */       this.m_helper.m_exchange.setComponentValue("dIsOptionList", "0");
/* 354 */       this.m_isOptionListCheckBox.setSelected(false);
/*     */     }
/* 356 */     this.m_isOptionListCheckBox.setEnabled(allowOptionList);
/* 357 */     boolean isEnabled = this.m_isOptionListCheckBox.isSelected();
/* 358 */     this.m_optionsButton.setEnabled(isEnabled);
/* 359 */     this.m_selectDefaultButton.setEnabled(isEnabled);
/* 360 */     if (!this.m_isAdd)
/*     */     {
/* 362 */       this.m_placeholderCheckbox.setEnabled(false);
/*     */     }
/*     */ 
/* 365 */     boolean allowDecimalField = MetaFieldUtils.allowDecimalField(item);
/* 366 */     this.m_decimalScale.setEnabled(allowDecimalField);
/* 367 */     if (allowDecimalField)
/* 368 */       this.m_helper.m_props.put("dDecimalScale", "1");
/*     */   }
/*     */ 
/*     */   protected JCheckBox createCheckbox(String label)
/*     */   {
/* 374 */     label = this.m_systemInterface.getString(label);
/* 375 */     JCheckBox cbox = new JCheckBox(label);
/* 376 */     cbox.addItemListener(this);
/* 377 */     return cbox;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 382 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 387 */     this.m_helper.loadComponentValues();
/* 388 */     enableDisable();
/* 389 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 396 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 397 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 403 */     String name = exchange.m_compName;
/* 404 */     String val = exchange.m_compValue;
/*     */ 
/* 406 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 412 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 50);
/* 413 */     IdcMessage errMsg = null;
/* 414 */     if (name.equals("dCaption"))
/*     */     {
/* 416 */       if (val == null)
/*     */       {
/* 418 */         errMsg = IdcMessageFactory.lc("apSpecifyCaption", new Object[0]);
/*     */       }
/* 420 */       else if (val.length() > maxLength)
/*     */       {
/* 422 */         errMsg = IdcMessageFactory.lc("apCaptionExceedsMaxLength", new Object[] { "" + maxLength });
/*     */       }
/*     */     }
/* 425 */     else if (name.equals("dOrder"))
/*     */     {
/* 427 */       if (Validation.checkInteger(val) != 0)
/*     */       {
/* 429 */         errMsg = IdcMessageFactory.lc("apInvalidDisplayOrder", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 433 */         int order = Integer.parseInt(val);
/* 434 */         if (order < 1)
/*     */         {
/* 436 */           errMsg = IdcMessageFactory.lc("apNonpositiveDisplayOrder", new Object[0]);
/*     */         }
/*     */       }
/*     */     }
/* 440 */     else if ((name.equals("dDefaultValue")) && 
/* 442 */       (val != null) && (val.length() > maxLength))
/*     */     {
/* 444 */       errMsg = IdcMessageFactory.lc("apDefaultValueExceedsMaxLength", new Object[] { "" + maxLength });
/*     */     }
/*     */ 
/* 448 */     if (errMsg != null)
/*     */     {
/* 450 */       exchange.m_errorMessage = errMsg;
/* 451 */       return false;
/*     */     }
/* 453 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent e)
/*     */   {
/* 462 */     this.m_errorMessage = null;
/*     */     try
/*     */     {
/* 465 */       Properties props = this.m_helper.m_props;
/* 466 */       props = (Properties)props.clone();
/*     */ 
/* 468 */       boolean isOptionList = StringUtils.convertToBool(props.getProperty("dIsOptionList"), false);
/*     */ 
/* 470 */       if (isOptionList)
/*     */       {
/* 472 */         String optType = props.getProperty("dOptionListType");
/* 473 */         if ((optType == null) || (optType.length() == 0))
/*     */         {
/* 475 */           reportError(null, IdcMessageFactory.lc("apConfigureOptionListMsg", new Object[0]));
/* 476 */           return false;
/*     */         }
/*     */ 
/* 479 */         if (!validateDefaultValue(props))
/*     */         {
/* 481 */           return false;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 487 */         props.put("dOptionListType", "");
/*     */       }
/*     */ 
/* 490 */       if (!validateDecimalDefaultValue(props))
/*     */       {
/* 492 */         return false;
/*     */       }
/*     */ 
/* 496 */       if (this.m_isAdd)
/*     */       {
/* 498 */         String internalName = MetaFieldGui.getDbFieldName(props);
/* 499 */         props.put("dName", internalName);
/*     */       }
/*     */ 
/* 502 */       AppLauncher.executeService(this.m_action, props);
/* 503 */       return true;
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 507 */       reportError(exp, IdcMessageFactory.lc("apErrorAddingInfoField", new Object[0]));
/* 508 */     }return false;
/*     */   }
/*     */ 
/*     */   protected boolean validateDecimalDefaultValue(Properties props)
/*     */   {
/* 513 */     String fieldType = props.getProperty("dType");
/* 514 */     if ("Decimal".equals(fieldType)) {
/* 515 */       String defaultValue = props.getProperty("dDefaultValue");
/* 516 */       if ((defaultValue == null) || (defaultValue.length() == 0))
/*     */       {
/* 518 */         return true;
/*     */       }
/* 520 */       String scaleStr = props.getProperty("dDecimalScale", "0");
/* 521 */       int decimalScale = Integer.parseInt(scaleStr);
/*     */       try
/*     */       {
/* 524 */         new BigDecimal(defaultValue, new MathContext(decimalScale, RoundingMode.UP));
/*     */       }
/*     */       catch (NumberFormatException e)
/*     */       {
/* 528 */         this.m_errorMessage = IdcMessageFactory.lc("apDefaultValueNotADecimal", new Object[0]);
/* 529 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 533 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean validateDefaultValue(Properties props)
/*     */     throws ServiceException
/*     */   {
/* 539 */     boolean result = true;
/* 540 */     String defaultValue = props.getProperty("dDefaultValue");
/* 541 */     if ((defaultValue == null) || (defaultValue.length() == 0))
/*     */     {
/* 543 */       return result;
/*     */     }
/*     */ 
/* 546 */     String optionKey = null;
/* 547 */     boolean isView = StringUtils.convertToBool(props.getProperty("UseViewList"), false);
/*     */ 
/* 549 */     boolean isTree = StringUtils.convertToBool(props.getProperty("UseTreeControl"), false);
/*     */ 
/* 551 */     if (isView)
/*     */     {
/* 553 */       optionKey = props.getProperty("OptionViewKey");
/*     */     }
/* 555 */     else if (isTree)
/*     */     {
/* 557 */       optionKey = props.getProperty("TreeDefinition");
/*     */     }
/*     */     else
/*     */     {
/* 561 */       optionKey = props.getProperty("OptionListKey");
/*     */ 
/* 564 */       if (optionKey == null)
/*     */       {
/* 566 */         optionKey = props.getProperty("dOptionListKey");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 571 */     if ((isView) && (!optionKey.startsWith(SchemaHelper.VIEW_PREFIX)))
/*     */     {
/* 573 */       optionKey = "view://" + optionKey;
/*     */     }
/* 575 */     if ((isTree) && (!optionKey.startsWith(SchemaHelper.TREE_PREFIX)))
/*     */     {
/* 577 */       optionKey = "tree://" + optionKey;
/*     */     }
/*     */ 
/* 581 */     MetaFieldData fieldData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*     */ 
/* 583 */     boolean found = fieldData.isValueInOptionList(optionKey, defaultValue);
/* 584 */     if (!found)
/*     */     {
/* 586 */       this.m_errorMessage = IdcMessageFactory.lc("apDefaultValueNotInOptionList", new Object[0]);
/* 587 */       result = false;
/*     */     }
/* 589 */     return result;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 597 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 605 */     this.m_helper.handleActionPerformed(e, this, this.m_systemInterface);
/*     */   }
/*     */ 
/*     */   public void configureOptionList()
/*     */   {
/* 610 */     String title = LocaleUtils.encodeMessage("apTitleMetafieldOptions", null, this.m_fieldName);
/* 611 */     title = this.m_systemInterface.localizeMessage(title);
/* 612 */     String helpPage = DialogHelpTable.getHelpPage("MetafieldOptions");
/*     */ 
/* 614 */     EditMetafieldOptionsDlg dlg = new EditMetafieldOptionsDlg(this.m_systemInterface, title, this.m_context, this.m_schemaHelper, helpPage);
/*     */ 
/* 617 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 618 */     int rc = dlg.init(props, this.m_fields, "dName", "dType", "dCaption", this.m_cmpValidator, this.m_isAdd);
/*     */ 
/* 620 */     if (rc != 1) {
/*     */       return;
/*     */     }
/* 623 */     DataBinder.mergeHashTables(this.m_helper.m_props, props);
/*     */   }
/*     */ 
/*     */   public void selectDefault()
/*     */     throws DataException, ServiceException
/*     */   {
/* 629 */     String field = this.m_helper.m_props.getProperty("dName");
/* 630 */     if (field == null)
/*     */     {
/* 632 */       field = this.m_helper.m_props.getProperty("schFieldName");
/*     */     }
/* 634 */     String title = LocaleUtils.encodeMessage("apSchSelectValue", null, field);
/*     */ 
/* 636 */     title = this.m_systemInterface.localizeMessage(title);
/*     */ 
/* 638 */     String helpPage = DialogHelpTable.getHelpPage("SchemaSelectValue");
/* 639 */     SelectValueDlg dialog = new SelectValueDlg(this.m_systemInterface, title, helpPage, this.m_context);
/*     */ 
/* 642 */     SchemaHelper schemaHelper = new SchemaHelper();
/*     */ 
/* 647 */     DataBinder binder = new DataBinder();
/* 648 */     binder.setLocalData(this.m_helper.m_props);
/* 649 */     SchemaFieldConfig fields = schemaHelper.m_fields;
/* 650 */     fields.updateEx(binder, 0L, false);
/*     */ 
/* 652 */     schemaHelper.computeMaps();
/* 653 */     dialog.init(schemaHelper, field, null, true, false, true);
/* 654 */     if (dialog.prompt() != 1)
/*     */       return;
/* 656 */     String value = dialog.getSelectedValue();
/* 657 */     this.m_defaultField.setText(value);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 663 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97873 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditMetafieldDlg
 * JD-Core Version:    0.5.4
 */