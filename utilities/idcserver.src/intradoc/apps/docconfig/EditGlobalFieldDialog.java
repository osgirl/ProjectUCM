/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
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
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaTargetConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditGlobalFieldDialog extends DialogCallback
/*     */   implements ComponentBinder, ItemListener, ActionListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected SharedContext m_context;
/*     */   protected boolean m_isAdd;
/*     */   protected String m_action;
/*     */   protected String m_helpPage;
/*     */   protected ComponentValidator m_cmpValidator;
/*  75 */   protected boolean m_skipValidation = false;
/*     */   protected DataResultSet m_fields;
/*     */   protected DataBinder m_binder;
/*  79 */   protected HashMap m_controls = new HashMap();
/*  80 */   protected ArrayList m_controlConfig = new ArrayList();
/*     */   protected JButton m_configureOptionListButton;
/*  85 */   protected SchemaHelper m_schemaHelper = null;
/*     */ 
/*     */   public EditGlobalFieldDialog(SystemInterface sys, String title, DataResultSet currentFieldList, String helpPage)
/*     */   {
/*  90 */     this.m_helper = new DialogHelper(sys, title, true);
/*  91 */     this.m_systemInterface = sys;
/*  92 */     this.m_helpPage = helpPage;
/*  93 */     this.m_fields = currentFieldList;
/*  94 */     this.m_cmpValidator = new ComponentValidator(currentFieldList);
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder, SharedContext sharedContext)
/*     */     throws DataException
/*     */   {
/* 100 */     this.m_context = sharedContext;
/* 101 */     SystemInterface si = this.m_systemInterface;
/*     */ 
/* 104 */     this.m_binder = binder;
/* 105 */     this.m_helper.m_props = binder.getLocalData();
/* 106 */     this.m_isAdd = (binder.getLocal("schFieldName") == null);
/*     */ 
/* 108 */     if (this.m_isAdd)
/*     */     {
/* 110 */       this.m_action = "ADD_SCHEMA_FIELD";
/*     */     }
/*     */     else
/*     */     {
/* 114 */       this.m_action = "EDIT_SCHEMA_FIELD";
/*     */     }
/*     */ 
/* 118 */     this.m_schemaHelper = new SchemaHelper();
/* 119 */     this.m_schemaHelper.computeMaps();
/*     */ 
/* 122 */     JPanel mainPanel = this.m_helper.initStandard(this, this, 2, true, this.m_helpPage);
/*     */ 
/* 124 */     JPanel curPanel = mainPanel;
/*     */ 
/* 126 */     SchemaTargetConfig targets = this.m_schemaHelper.m_targets;
/*     */ 
/* 129 */     int minCols = 30;
/* 130 */     String targetName = this.m_binder.getLocal("schFieldTarget");
/* 131 */     if (targetName == null)
/*     */     {
/* 133 */       targetName = "DocMeta";
/*     */     }
/* 135 */     SchemaData target = targets.getData(targetName);
/* 136 */     if (target == null)
/*     */     {
/* 138 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_target", null, targetName);
/*     */ 
/* 140 */       throw new DataException(msg);
/*     */     }
/* 142 */     DataResultSet fields = target.getResultSet("TargetFieldInfo");
/* 143 */     for (fields.first(); fields.isRowPresent(); fields.next())
/*     */     {
/* 145 */       Properties fieldProps = fields.getCurrentRowProps();
/* 146 */       this.m_controlConfig.add(fieldProps);
/* 147 */       String fieldName = fieldProps.getProperty("schFieldName");
/* 148 */       String schemaEquiv = fieldProps.getProperty("schSchemaField");
/* 149 */       String type = fieldProps.getProperty("schFieldType");
/* 150 */       String caption = fieldProps.getProperty("schFieldCaption");
/* 151 */       Component component = null;
/* 152 */       if ((caption != null) && (caption.length() > 0))
/*     */       {
/* 154 */         caption = si.localizeCaption(caption);
/*     */       }
/*     */       else
/*     */       {
/* 158 */         caption = fieldName;
/*     */       }
/*     */ 
/* 161 */       if (type.equals("boolean"))
/*     */       {
/* 163 */         boolean endRow = true;
/* 164 */         Component extraControl = null;
/* 165 */         if (schemaEquiv.equals("dIsOptionList"))
/*     */         {
/* 167 */           endRow = false;
/* 168 */           extraControl = this.m_configureOptionListButton = new JButton(si.getString("apOptionConfigureButton"));
/*     */ 
/* 170 */           this.m_configureOptionListButton.addActionListener(this);
/* 171 */           this.m_configureOptionListButton.setActionCommand("configureOptionList");
/*     */ 
/* 173 */           Properties extraProps = new Properties();
/* 174 */           this.m_controlConfig.add(extraProps);
/* 175 */           this.m_controls.put("OptionListConfigureButton", extraControl);
/* 176 */           extraProps.put("schFieldName", "OptionListConfigureButton");
/* 177 */           extraProps.put("schSchemaField", "OptionListConfigureButton");
/* 178 */           extraProps.put("schEnableCriteria", "dIsOptionList");
/*     */         }
/*     */ 
/* 181 */         this.m_helper.addLabelFieldPairEx(curPanel, caption, component = createCheckbox("apLabelEnabled"), fieldName, endRow);
/*     */ 
/* 184 */         if (!endRow)
/*     */         {
/* 186 */           this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 187 */           this.m_helper.m_gridHelper.prepareAddRowElement(17, 0);
/*     */ 
/* 189 */           this.m_helper.addComponent(curPanel, extraControl);
/*     */         }
/*     */       }
/* 192 */       else if (type.startsWith(SchemaHelper.VIEW_PREFIX))
/*     */       {
/* 194 */         SchemaViewData view = this.m_schemaHelper.getView(type);
/* 195 */         if (view == null)
/*     */         {
/* 197 */           String msg = LocaleUtils.encodeMessage("wwSchemaObjectDoesntExist_view", null, type);
/*     */ 
/* 199 */           throw new DataException(msg);
/*     */         }
/* 201 */         DataResultSet rset = (DataResultSet)view.getAllViewValues();
/* 202 */         DisplayChoice choiceControl = new DisplayChoice();
/* 203 */         String[][] choices = this.m_schemaHelper.initChoicesFromView(view, rset);
/* 204 */         LocaleResources.localizeDoubleArray(choices, si.getExecutionContext(), 1);
/* 205 */         choiceControl.init(choices);
/*     */ 
/* 207 */         this.m_helper.addLabelFieldPair(curPanel, caption, component = choiceControl, fieldName);
/*     */       }
/*     */       else
/*     */       {
/* 213 */         this.m_helper.addLabelFieldPair(curPanel, caption, component = new CustomTextField(minCols), fieldName);
/*     */       }
/*     */ 
/* 216 */       this.m_controls.put(fieldName, component);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getControlValue(String controlName)
/*     */   {
/* 222 */     Component targetControl = (Component)this.m_controls.get(controlName);
/* 223 */     Object[] exchangeObjects = { null, controlName, null, null };
/* 224 */     this.m_helper.m_exchange.m_component = targetControl;
/* 225 */     this.m_helper.m_exchange.exchangeComponent(exchangeObjects, false);
/*     */ 
/* 227 */     String value = this.m_helper.m_exchange.m_compValue;
/* 228 */     return value;
/*     */   }
/*     */ 
/*     */   protected void enableDisable()
/*     */   {
/* 233 */     for (int i = 0; i < this.m_controlConfig.size(); ++i)
/*     */     {
/* 235 */       Properties fieldProps = (Properties)this.m_controlConfig.get(i);
/* 236 */       String fieldName = fieldProps.getProperty("schFieldName");
/* 237 */       Component control = (Component)this.m_controls.get(fieldName);
/* 238 */       String criteria = fieldProps.getProperty("schEnableCriteria");
/* 239 */       boolean isEnabled = true;
/* 240 */       if ((criteria != null) && (criteria.length() > 0))
/*     */       {
/* 244 */         String value = getControlValue(criteria);
/* 245 */         isEnabled = StringUtils.convertToBool(value, false);
/*     */       }
/* 247 */       if (control == null)
/*     */         continue;
/* 249 */       if ((fieldName.equals("schFieldName")) && (!this.m_isAdd))
/*     */       {
/* 252 */         control.setEnabled(false);
/*     */       }
/*     */       else
/*     */       {
/* 256 */         control.setEnabled(isEnabled);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JCheckBox createCheckbox(String label)
/*     */   {
/* 264 */     label = this.m_systemInterface.getString(label);
/* 265 */     JCheckBox cbox = new JCheckBox(label);
/* 266 */     cbox.addItemListener(this);
/* 267 */     return cbox;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 272 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 277 */     this.m_helper.loadComponentValues();
/* 278 */     enableDisable();
/* 279 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 285 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 286 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 291 */     String name = exchange.m_compName;
/* 292 */     String val = exchange.m_compValue;
/*     */ 
/* 294 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 300 */     if (this.m_skipValidation)
/*     */     {
/* 302 */       return true;
/*     */     }
/* 304 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 50);
/* 305 */     IdcMessage errMsg = null;
/* 306 */     if (name.equals("schFieldCaption"))
/*     */     {
/* 308 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 310 */         errMsg = IdcMessageFactory.lc("apSpecifyCaption", new Object[0]);
/*     */       }
/* 312 */       else if (val.length() > maxLength)
/*     */       {
/* 314 */         errMsg = IdcMessageFactory.lc("apCaptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 317 */     else if (name.equals("schOrder"))
/*     */     {
/* 319 */       if (Validation.checkInteger(val) != 0)
/*     */       {
/* 321 */         errMsg = IdcMessageFactory.lc("apInvalidDisplayOrder", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 325 */         int order = Integer.parseInt(val);
/* 326 */         if (order < 1)
/*     */         {
/* 328 */           errMsg = IdcMessageFactory.lc("apNonpositiveDisplayOrder", new Object[0]);
/*     */         }
/*     */       }
/*     */     }
/* 332 */     else if (name.equals("schFieldName"))
/*     */     {
/* 334 */       errMsg = validateFieldName(val);
/*     */     }
/*     */ 
/* 337 */     if (errMsg != null)
/*     */     {
/* 339 */       exchange.m_errorMessage = errMsg;
/* 340 */       return false;
/*     */     }
/* 342 */     return true;
/*     */   }
/*     */ 
/*     */   protected IdcMessage validateFieldName(String name)
/*     */   {
/* 348 */     IdcMessage errMsg = null;
/* 349 */     int val = Validation.checkDatabaseFieldName(name);
/* 350 */     switch (val)
/*     */     {
/*     */     case 0:
/* 353 */       break;
/*     */     case -1:
/* 355 */       errMsg = IdcMessageFactory.lc("apSpecifyApplicationFieldName", new Object[0]);
/* 356 */       break;
/*     */     case -2:
/* 358 */       errMsg = IdcMessageFactory.lc("apNameCannotContainSpaces", new Object[0]);
/* 359 */       break;
/*     */     case -3:
/* 361 */       errMsg = IdcMessageFactory.lc("apInvalidCharInFieldName", new Object[0]);
/* 362 */       break;
/*     */     default:
/* 364 */       errMsg = IdcMessageFactory.lc("apInvalidNameForInfoField", new Object[0]);
/*     */     }
/*     */ 
/* 367 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public boolean handleDialogEvent(ActionEvent e)
/*     */   {
/* 376 */     this.m_errorMessage = null;
/*     */     try
/*     */     {
/* 379 */       Properties props = this.m_helper.m_props;
/* 380 */       props = (Properties)props.clone();
/*     */ 
/* 383 */       boolean isOptionList = StringUtils.convertToBool(props.getProperty("dIsOptionList"), false);
/*     */ 
/* 385 */       if (isOptionList)
/*     */       {
/* 387 */         String optType = props.getProperty("dOptionListType");
/* 388 */         if ((optType == null) || (optType.length() == 0))
/*     */         {
/* 390 */           reportError(null, IdcMessageFactory.lc("apConfigureOptionListMsg", new Object[0]));
/* 391 */           return false;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 397 */         props.put("dOptionListType", "");
/*     */       }
/*     */ 
/* 400 */       AppLauncher.executeService(this.m_action, props);
/* 401 */       return true;
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 405 */       reportError(exp, IdcMessageFactory.lc("apErrorAddingInfoField", new Object[0]));
/* 406 */     }return false;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 415 */     enableDisable();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 423 */     this.m_helper.handleActionPerformed(event, this, this.m_systemInterface);
/*     */   }
/*     */ 
/*     */   public void configureOptionList()
/*     */   {
/* 428 */     String fieldName = getControlValue("schFieldName");
/* 429 */     if (fieldName.length() == 0)
/*     */     {
/* 431 */       reportError(null, IdcMessageFactory.lc("apSchSpecifyFieldName", new Object[0]));
/* 432 */       return;
/*     */     }
/* 434 */     String title = LocaleUtils.encodeMessage("apTitleMetafieldOptions", null, fieldName);
/*     */ 
/* 436 */     title = this.m_systemInterface.localizeMessage(title);
/* 437 */     String helpPage = DialogHelpTable.getHelpPage("MetafieldOptions");
/*     */ 
/* 439 */     EditMetafieldOptionsDlg dlg = new EditMetafieldOptionsDlg(this.m_systemInterface, title, this.m_context, this.m_schemaHelper, helpPage);
/*     */     try
/*     */     {
/* 444 */       this.m_skipValidation = true;
/* 445 */       this.m_helper.retrieveComponentValues();
/*     */     }
/*     */     finally
/*     */     {
/* 449 */       this.m_skipValidation = false;
/*     */     }
/* 451 */     Properties props = (Properties)this.m_helper.m_props.clone();
/* 452 */     int rc = dlg.init(props, this.m_fields, "schFieldName", "schFieldType", "schFieldCaption", this.m_cmpValidator, this.m_isAdd);
/*     */ 
/* 454 */     if (rc != 1) {
/*     */       return;
/*     */     }
/* 457 */     DataBinder.mergeHashTables(this.m_helper.m_props, props);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 463 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81425 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditGlobalFieldDialog
 * JD-Core Version:    0.5.4
 */