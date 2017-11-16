/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
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
/*     */ import intradoc.shared.MetaFieldUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.EditOptionListDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditUserMetafieldDlg
/*     */   implements ComponentBinder
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected String m_helpPage;
/*     */   protected ComponentValidator m_cmpValidator;
/*  75 */   protected ExecutionContext m_cxt = null;
/*     */   protected String m_action;
/*     */   protected JCheckBox m_isOptionListCheckBox;
/*     */   protected DisplayChoice m_typeChoices;
/*     */ 
/*     */   public EditUserMetafieldDlg(SystemInterface sys, String title, ResultSet rset, boolean isAdd, String helpPage)
/*     */   {
/*  84 */     this.m_helper = new DialogHelper(sys, title, true);
/*  85 */     this.m_cxt = sys.getExecutionContext();
/*  86 */     this.m_systemInterface = sys;
/*  87 */     this.m_helpPage = helpPage;
/*  88 */     this.m_cmpValidator = new ComponentValidator(rset);
/*     */ 
/*  90 */     if (isAdd)
/*     */     {
/*  92 */       this.m_action = "ADD";
/*     */     }
/*     */     else
/*     */     {
/*  96 */       this.m_action = "EDIT";
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(Properties data) throws DataException
/*     */   {
/* 102 */     boolean isPredefined = false;
/*     */ 
/* 105 */     this.m_helper.m_props = data;
/*     */ 
/* 108 */     calculateOverrideBitFlag();
/*     */ 
/* 111 */     String fieldName = this.m_helper.m_props.getProperty("umdName");
/* 112 */     isPredefined = fieldName.startsWith("d");
/*     */ 
/* 115 */     DialogCallback okCallback = new DialogCallback(fieldName)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 122 */           if ((!this.val$fieldName.equals("dFullName")) && (!this.val$fieldName.equals("dEmail")) && (!this.val$fieldName.equals("dUserLocale")) && (!this.val$fieldName.equals("dUserTimeZone")))
/*     */           {
/* 125 */             String optionKey = EditUserMetafieldDlg.this.m_helper.m_props.getProperty("umdOptionListKey");
/* 126 */             boolean isOptionsList = EditUserMetafieldDlg.this.m_isOptionListCheckBox.isSelected();
/*     */ 
/* 128 */             if (isOptionsList)
/*     */             {
/* 130 */               if ((optionKey == null) || (optionKey.length() == 0))
/*     */               {
/* 132 */                 this.m_errorMessage = IdcMessageFactory.lc("apSpecifyOptionListKey", new Object[0]);
/* 133 */                 return false;
/*     */               }
/*     */ 
/* 137 */               Vector opts = SharedObjects.getOptList(optionKey);
/*     */ 
/* 139 */               boolean hasNonEmptyValues = false;
/* 140 */               if (opts != null)
/*     */               {
/* 142 */                 int size = opts.size();
/* 143 */                 for (int i = 0; i < size; ++i)
/*     */                 {
/* 145 */                   String str = (String)opts.elementAt(i);
/* 146 */                   if (str.length() <= 0)
/*     */                     continue;
/* 148 */                   hasNonEmptyValues = true;
/* 149 */                   break;
/*     */                 }
/*     */               }
/*     */ 
/* 153 */               if ((opts == null) || (!hasNonEmptyValues))
/*     */               {
/* 155 */                 this.m_errorMessage = IdcMessageFactory.lc("apSpecifyOptionListValues", new Object[0]);
/* 156 */                 return false;
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 162 */           EditUserMetafieldDlg.this.serializeData();
/*     */ 
/* 164 */           return true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 168 */           EditUserMetafieldDlg.this.reportError(exp, IdcMessageFactory.lc("apErrorAddingInfoField", new Object[0]));
/* 169 */         }return false;
/*     */       }
/*     */     };
/* 173 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, true, this.m_helpPage);
/*     */ 
/* 178 */     int minCols = 30;
/* 179 */     this.m_typeChoices = new DisplayChoice();
/* 180 */     this.m_typeChoices.init(TableFields.METAFIELD_TYPES_OPTIONSLIST);
/*     */ 
/* 182 */     JPanel curPanel = addNewSubPanel(mainPanel);
/* 183 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelFieldCaption", this.m_cxt), new CustomTextField(minCols), "umdCaption");
/*     */ 
/* 185 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelFieldType", this.m_cxt), this.m_typeChoices, "umdType");
/*     */ 
/* 187 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelOverrideFlag", this.m_cxt), new CustomLabel(), "umdOverrideBitFlag");
/*     */ 
/* 190 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelAdminEdit", this.m_cxt), createCheckBox(LocaleResources.getString("apTitleAdminOnly", this.m_cxt)), "umdIsAdminEdit");
/*     */ 
/* 193 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelViewOnly", this.m_cxt), createCheckBox(LocaleResources.getString("apTitleViewOnly", this.m_cxt)), "umdIsViewOnly");
/*     */ 
/* 196 */     this.m_isOptionListCheckBox = createCheckBox(LocaleResources.getString("apLabelOptionList", this.m_cxt));
/* 197 */     this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelEnableOptionList", this.m_cxt), this.m_isOptionListCheckBox, "umdIsOptionList");
/*     */ 
/* 202 */     if ((fieldName.equals("dFullName")) || (fieldName.equals("dEmail")) || (fieldName.equals("dUserLocale")) || (fieldName.equals("dUserTimeZone")))
/*     */     {
/* 205 */       this.m_isOptionListCheckBox.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 209 */       curPanel = addNewSubPanel(mainPanel);
/* 210 */       DisplayChoice optTypeChoices = new DisplayChoice();
/* 211 */       optTypeChoices.init(TableFields.METAFIELD_OPTIONLISTTYPE_OPTIONSLIST);
/* 212 */       this.m_helper.addLabelFieldPair(curPanel, LocaleResources.getString("apLabelOptionListType", this.m_cxt), optTypeChoices, "umdOptionListType");
/*     */ 
/* 215 */       JTextField optionListKeyTextField = new CustomTextField(minCols);
/* 216 */       this.m_helper.addLabelFieldPairEx(curPanel, LocaleResources.getString("apLabelOptionListKey", this.m_cxt), optionListKeyTextField, "umdOptionListKey", false);
/*     */ 
/* 220 */       JButton editOptionsBtn = new JButton(LocaleResources.getString("apDlgButtonEdit", this.m_cxt));
/*     */ 
/* 222 */       this.m_helper.addComponent(curPanel, editOptionsBtn);
/* 223 */       editOptionsBtn.addActionListener(new ActionListener(optionListKeyTextField)
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 229 */           EditUserMetafieldDlg.this.m_helper.m_props.put("dType", EditUserMetafieldDlg.this.m_typeChoices.getSelectedInternalValue());
/* 230 */           EditUserMetafieldDlg.this.m_helper.m_props.put("dOptionListKey", this.val$optionListKeyTextField.getText());
/*     */ 
/* 232 */           String key = this.val$optionListKeyTextField.getText();
/* 233 */           key = key.trim();
/* 234 */           if (key.length() == 0)
/*     */           {
/* 236 */             EditUserMetafieldDlg.this.reportError(null, IdcMessageFactory.lc("apSpecifyOptionListKey", new Object[0]));
/* 237 */             return;
/*     */           }
/* 239 */           String title = LocaleResources.getString("apLabelOptionList", EditUserMetafieldDlg.this.m_cxt);
/* 240 */           EditUserMetafieldDlg.this.m_helper.retrieveComponentValues();
/* 241 */           EditOptionListDlg edtOptions = new EditOptionListDlg(EditUserMetafieldDlg.this.m_systemInterface, title, DialogHelpTable.getHelpPage("OptionList"), "UPDATE_USEROPTION_LIST");
/*     */ 
/* 243 */           edtOptions.init(EditUserMetafieldDlg.this.m_helper.m_props);
/* 244 */           edtOptions.prompt();
/*     */         }
/*     */       });
/* 249 */       boolean isOptionEnabled = StringUtils.convertToBool(data.getProperty("umdIsOptionList"), false);
/*     */ 
/* 251 */       optTypeChoices.setEnabled(isOptionEnabled);
/* 252 */       optionListKeyTextField.setEnabled(isOptionEnabled);
/* 253 */       editOptionsBtn.setEnabled(isOptionEnabled);
/* 254 */       String type = data.getProperty("umdType");
/* 255 */       if ((type != null) && (!MetaFieldUtils.allowOptionList(type)))
/*     */       {
/* 257 */         this.m_isOptionListCheckBox.setEnabled(false);
/*     */       }
/*     */ 
/* 261 */       ItemListener evtListener = new Object(fieldName, optTypeChoices, optionListKeyTextField, editOptionsBtn)
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 265 */           int state = e.getStateChange();
/* 266 */           Object source = e.getSource();
/* 267 */           boolean optionListEnabled = EditUserMetafieldDlg.this.m_isOptionListCheckBox.isSelected();
/* 268 */           boolean optionListStateChanged = false;
/*     */ 
/* 270 */           if (source == EditUserMetafieldDlg.this.m_typeChoices)
/*     */           {
/* 272 */             if (state == 1)
/*     */             {
/* 274 */               String item = EditUserMetafieldDlg.this.m_typeChoices.getSelectedInternalValue();
/* 275 */               boolean allowOptionList = MetaFieldUtils.allowOptionList(item);
/* 276 */               if ((!allowOptionList) && (optionListEnabled == true))
/*     */               {
/* 278 */                 optionListEnabled = false;
/* 279 */                 EditUserMetafieldDlg.this.m_helper.m_exchange.setComponentValue("umdIsOptionList", "0");
/* 280 */                 optionListStateChanged = true;
/*     */               }
/* 282 */               EditUserMetafieldDlg.this.m_isOptionListCheckBox.setEnabled(allowOptionList);
/*     */             }
/*     */ 
/*     */           }
/*     */           else {
/* 287 */             optionListStateChanged = true;
/*     */           }
/* 289 */           if (!optionListStateChanged)
/*     */             return;
/* 291 */           String keyVal = "";
/* 292 */           if (optionListEnabled)
/*     */           {
/* 294 */             keyVal = "Users_" + this.val$fieldName.substring(1) + "List";
/*     */           }
/* 296 */           EditUserMetafieldDlg.this.m_helper.m_exchange.setComponentValue("umdOptionListKey", keyVal);
/*     */ 
/* 298 */           this.val$optTypeChoices.setEnabled(optionListEnabled);
/* 299 */           this.val$optionListKeyTextField.setEnabled(optionListEnabled);
/* 300 */           this.val$editOptionsBtn.setEnabled(optionListEnabled);
/*     */         }
/*     */       };
/* 305 */       this.m_typeChoices.addItemListener(evtListener);
/* 306 */       this.m_isOptionListCheckBox.addItemListener(evtListener);
/*     */     }
/*     */ 
/* 309 */     if (isPredefined)
/*     */     {
/* 311 */       this.m_typeChoices.setEnabled(false);
/*     */     }
/*     */     else
/*     */     {
/* 315 */       this.m_typeChoices.setEnabled(true);
/*     */     }
/*     */ 
/* 319 */     this.m_helper.m_gridHelper.m_gc.weighty = 10.0D;
/* 320 */     this.m_helper.addLastComponentInRow(mainPanel, new PanePanel());
/*     */   }
/*     */ 
/*     */   protected void serializeData() throws DataException, ServiceException
/*     */   {
/* 325 */     setOverrideBitForDisplay(false);
/*     */ 
/* 328 */     DataBinder binder = new DataBinder(true);
/* 329 */     binder.setLocalData(this.m_helper.m_props);
/* 330 */     binder.putLocal("action", this.m_action);
/*     */ 
/* 332 */     AppLauncher.executeService("UPDATE_USER_META", binder);
/*     */   }
/*     */ 
/*     */   protected JCheckBox createCheckBox(String label)
/*     */   {
/* 337 */     JCheckBox cbox = new JCheckBox(label);
/* 338 */     return cbox;
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel)
/*     */   {
/* 344 */     CustomPanel panel = new CustomPanel();
/* 345 */     panel.setInsets(10, 5, 10, 5);
/* 346 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 348 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 349 */     this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 350 */     this.m_helper.addComponent(mainPanel, panel);
/* 351 */     this.m_helper.makePanelGridBag(panel, 0);
/* 352 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/* 354 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, IdcMessage msg)
/*     */   {
/* 359 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   protected void setOverrideBitForDisplay(boolean hexDisplay)
/*     */   {
/* 364 */     String currentBit = this.m_helper.m_props.getProperty("umdOverrideBitFlag");
/*     */ 
/* 367 */     if ((hexDisplay) && (!currentBit.startsWith("0x")))
/*     */     {
/* 369 */       currentBit = "0x" + Integer.toHexString(Integer.parseInt(currentBit));
/*     */     }
/* 371 */     else if ((!hexDisplay) && (currentBit.startsWith("0x")))
/*     */     {
/* 373 */       int bit = Integer.parseInt(currentBit.substring(2), 16);
/* 374 */       currentBit = "" + bit;
/*     */     }
/*     */ 
/* 377 */     this.m_helper.m_props.put("umdOverrideBitFlag", currentBit);
/*     */   }
/*     */ 
/*     */   protected void calculateOverrideBitFlag()
/*     */     throws DataException
/*     */   {
/* 383 */     String currentBit = this.m_helper.m_props.getProperty("umdOverrideBitFlag");
/* 384 */     if (currentBit != null)
/*     */     {
/* 387 */       setOverrideBitForDisplay(true);
/* 388 */       return;
/*     */     }
/*     */ 
/* 391 */     DataResultSet data = SharedObjects.getTable("UserMetaDefinition");
/* 392 */     FieldInfo info = new FieldInfo();
/* 393 */     Vector usedBits = new IdcVector();
/* 394 */     int highBit = 0;
/* 395 */     int bit = 16;
/*     */ 
/* 397 */     if (data.getFieldInfo("umdOverrideBitFlag", info))
/*     */     {
/* 399 */       for (data.first(); data.isRowPresent(); data.next())
/*     */       {
/* 401 */         String value = data.getStringValue(info.m_index);
/*     */ 
/* 403 */         if (value == null)
/*     */           continue;
/* 405 */         usedBits.addElement(value);
/* 406 */         int v = Integer.parseInt(value);
/*     */ 
/* 408 */         if ((v <= highBit) || (v < 16))
/*     */           continue;
/* 410 */         highBit = v;
/*     */       }
/*     */ 
/* 415 */       if (highBit >= 16)
/*     */       {
/* 417 */         bit = highBit * 2;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 422 */     if (bit > 536870912)
/*     */     {
/* 424 */       bit = 16;
/*     */ 
/* 426 */       while (usedBits.contains("" + bit))
/*     */       {
/* 428 */         bit *= 2;
/*     */       }
/*     */ 
/* 432 */       if (bit > 536870912)
/*     */       {
/* 434 */         throw new DataException("!apUserMetaBitValueHigh");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 439 */     this.m_helper.m_props.put("umdOverrideBitFlag", "" + bit);
/*     */ 
/* 442 */     setOverrideBitForDisplay(true);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 447 */     this.m_helper.loadComponentValues();
/* 448 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 459 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 460 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 465 */     String name = exchange.m_compName;
/* 466 */     String val = exchange.m_compValue;
/*     */ 
/* 468 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 475 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 50);
/*     */ 
/* 477 */     IdcMessage errMsg = null;
/* 478 */     if (name.equals("umdCaption"))
/*     */     {
/* 480 */       if (val == null)
/*     */       {
/* 482 */         errMsg = IdcMessageFactory.lc("apSpecifyCaption", new Object[0]);
/*     */       }
/* 484 */       else if (val.length() > maxLength)
/*     */       {
/* 486 */         errMsg = IdcMessageFactory.lc("apCaptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 489 */     else if ((name.equals("umdOptionListKey")) && (this.m_isOptionListCheckBox.isSelected() == true))
/*     */     {
/* 492 */       if (val == null)
/*     */       {
/* 494 */         errMsg = IdcMessageFactory.lc("apSpecifyOptionListKey", new Object[0]);
/*     */       }
/* 496 */       else if (val.length() > maxLength)
/*     */       {
/* 498 */         errMsg = IdcMessageFactory.lc("apOptionListKeyExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 501 */     else if ((name.equals("umdOptionListType")) && 
/* 503 */       (this.m_helper.m_exchange.getComponentValue("umdType").equalsIgnoreCase("Int")) && (val.equalsIgnoreCase("multi")))
/*     */     {
/* 506 */       errMsg = IdcMessageFactory.lc("apIntegerFieldsCannotBeMultiselect", new Object[0]);
/*     */     }
/*     */ 
/* 510 */     if (errMsg != null)
/*     */     {
/* 512 */       exchange.m_errorMessage = errMsg;
/* 513 */       return false;
/*     */     }
/*     */ 
/* 516 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 521 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserMetafieldDlg
 * JD-Core Version:    0.5.4
 */