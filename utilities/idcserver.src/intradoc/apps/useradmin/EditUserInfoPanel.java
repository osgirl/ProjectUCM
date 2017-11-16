/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomPasswordField;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.Users;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.EditOptionListDlg;
/*     */ import intradoc.shared.localization.SharedLocalizationHandler;
/*     */ import intradoc.shared.localization.SharedLocalizationHandlerFactory;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPasswordField;
/*     */ 
/*     */ public class EditUserInfoPanel extends EditUserBasePanel
/*     */ {
/*     */   protected String[][] m_extractedValues;
/*     */   protected Hashtable m_intFields;
/*     */   protected Hashtable m_dateFields;
/*     */   protected DataResultSet m_timeZoneData;
/*     */   protected DisplayChoice m_timeZoneChoice;
/*     */   Hashtable m_fieldPropsMap;
/*  91 */   protected static final String[] EXTRACTED_META_FIELDS = { "umdName", "umdType", "umdCaption", "umdIsOptionList", "umdOptionListType", "umdOptionListKey", "umdOverrideBitFlag" };
/*     */ 
/*     */   public EditUserInfoPanel()
/*     */   {
/*  77 */     this.m_extractedValues = ((String[][])null);
/*     */ 
/*  80 */     this.m_intFields = new Hashtable();
/*  81 */     this.m_dateFields = new Hashtable();
/*     */ 
/*  87 */     this.m_fieldPropsMap = null;
/*     */   }
/*     */ 
/*     */   protected void initUI(boolean isEdit)
/*     */   {
/*  98 */     DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/*  99 */     if (drset != null)
/*     */     {
/*     */       try
/*     */       {
/* 103 */         this.m_extractedValues = ResultSetUtils.createStringTable(drset, EXTRACTED_META_FIELDS);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 107 */         e.printStackTrace();
/*     */       }
/*     */     }
/* 110 */     this.m_fieldPropsMap = new Hashtable();
/*     */     Component name;
/*     */     Component name;
/* 126 */     if (isEdit)
/*     */     {
/* 128 */       name = new CustomLabel(this.m_originalUserData.m_name);
/*     */     }
/*     */     else
/*     */     {
/* 132 */       name = new CustomTextField(20);
/*     */     }
/*     */ 
/* 136 */     String sysLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/* 137 */     this.m_helper.m_props.put("Default_dUserLocale", sysLocale);
/*     */ 
/* 139 */     this.m_helper.makePanelGridBag(this, 1);
/* 140 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 141 */     this.m_helper.m_gridHelper.m_gc.fill = 1;
/*     */ 
/* 144 */     if (!AppLauncher.getIsStandAlone())
/*     */     {
/* 146 */       this.m_timeZoneData = SharedObjects.getTable("LocalizedTimeZoneList");
/* 147 */       if (this.m_timeZoneData == null)
/*     */       {
/* 149 */         DataBinder binder = new DataBinder();
/*     */         try
/*     */         {
/* 152 */           AppLauncher.executeService("GET_USER_INFO", binder);
/* 153 */           DataResultSet tmp = (DataResultSet)binder.getResultSet("TimeZones");
/* 154 */           if (tmp != null)
/*     */           {
/* 156 */             SharedObjects.putTable("LocalizedTimeZoneList", tmp);
/* 157 */             this.m_timeZoneData = SharedObjects.getTable("LocalizedTimeZoneList");
/*     */           }
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 167 */     if (this.m_timeZoneData == null)
/*     */     {
/* 169 */       SharedLocalizationHandler slh = SharedLocalizationHandlerFactory.createInstance();
/* 170 */       this.m_timeZoneData = slh.getTimeZones(null);
/* 171 */       slh.prepareTimeZonesForDisplay(this.m_timeZoneData, null, 3);
/*     */     }
/*     */ 
/* 174 */     if (this.m_isLocal)
/*     */     {
/* 176 */       createLocalUI(name, isEdit);
/*     */     }
/*     */     else
/*     */     {
/* 180 */       createNonLocalUI(name, isEdit);
/*     */     }
/* 182 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 183 */     this.m_helper.addLastComponentInRow(this, new PanePanel());
/*     */   }
/*     */ 
/*     */   protected void createLocalUI(Component nameCmp, boolean isEdit)
/*     */   {
/* 188 */     this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelUserName", this.m_ctx), nameCmp, "dName");
/*     */ 
/* 190 */     this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelFullName", this.m_ctx), new CustomTextField(20), "dFullName");
/*     */ 
/* 193 */     addPasswordFields(isEdit);
/*     */ 
/* 195 */     addMetaUserFields(this, new String[] { "dFullName" }, false);
/*     */   }
/*     */ 
/*     */   protected void addPasswordFields(boolean isEdit)
/*     */   {
/* 201 */     JPasswordField password = new CustomPasswordField(20);
/* 202 */     password.setEchoChar('*');
/*     */ 
/* 204 */     JPasswordField confirmPassword = new CustomPasswordField(20);
/* 205 */     confirmPassword.setEchoChar('*');
/*     */ 
/* 207 */     this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelPassword", this.m_ctx), password, "dPassword");
/*     */ 
/* 209 */     this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelConfirmPassword", this.m_ctx), confirmPassword, "confirmPswrd");
/*     */ 
/* 212 */     if (!isEdit)
/*     */       return;
/* 214 */     String pswrd = this.m_originalUserData.getProperty("dPassword");
/* 215 */     this.m_helper.m_props.put("confirmPswrd", pswrd);
/*     */   }
/*     */ 
/*     */   protected void createNonLocalUI(Component nameCmp, boolean isEdit)
/*     */   {
/* 221 */     this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelUserName", this.m_ctx), nameCmp, "dName");
/*     */ 
/* 224 */     String sourceOrgPath = this.m_helper.m_props.getProperty("dUserSourceOrgPath");
/* 225 */     boolean fromOutsideSource = (sourceOrgPath != null) && (sourceOrgPath.length() > 0);
/* 226 */     if (fromOutsideSource)
/*     */     {
/* 228 */       this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelOrganizationPath", this.m_ctx), new CustomLabel(), "dUserOrgPath");
/*     */ 
/* 230 */       this.m_helper.addLabelFieldPair(this, LocaleResources.getString("apLabelSource", this.m_ctx), new CustomLabel(), "dUserSourceOrgPath");
/*     */     }
/*     */     else
/*     */     {
/* 235 */       addUserFieldComponent(this, "dUserOrgPath", "apLabelOrganizationPath", true, false, false, true, "Users_OrgPathList", false, "Text");
/*     */     }
/*     */ 
/* 239 */     if (!this.m_isExternal)
/*     */     {
/* 241 */       addPasswordFields(isEdit);
/*     */     }
/*     */ 
/* 244 */     JPanel ovrPanel = createOverridePanel();
/* 245 */     this.m_helper.addLastComponentInRow(this, ovrPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel createOverridePanel()
/*     */   {
/* 250 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 251 */     JPanel pnl = new CustomPanel();
/* 252 */     this.m_helper.makePanelGridBag(pnl, 2);
/*     */ 
/* 255 */     gh.prepareAddRowElement();
/* 256 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelUserValue", this.m_ctx), 1));
/*     */ 
/* 258 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelUserField", this.m_ctx), 1));
/*     */ 
/* 260 */     gh.prepareAddLastRowElement();
/* 261 */     gh.m_gc.fill = 0;
/* 262 */     this.m_helper.addComponent(pnl, new CustomLabel(LocaleResources.getString("apLabelOverride", this.m_ctx), 1));
/*     */ 
/* 264 */     gh.m_gc.fill = 2;
/*     */ 
/* 267 */     PanePanel cpnl = new PanePanel(false);
/* 268 */     cpnl.setStyle(16);
/* 269 */     cpnl.setColor(Color.black);
/* 270 */     cpnl.setSkip("South", true);
/* 271 */     cpnl.setSkip("East", true);
/* 272 */     cpnl.setSkip("West", true);
/* 273 */     this.m_helper.addComponent(pnl, cpnl);
/*     */ 
/* 275 */     addMetaUserFields(pnl, null, true);
/*     */ 
/* 277 */     setOverrideFlagValues();
/* 278 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void addMetaUserFields(JPanel pnl, String[] alreadyImplementedFields, boolean addOverrideOption)
/*     */   {
/* 284 */     if (this.m_extractedValues == null)
/*     */     {
/* 286 */       return;
/*     */     }
/*     */ 
/* 289 */     for (int i = 0; i < this.m_extractedValues.length; ++i)
/*     */     {
/* 291 */       String[] defValues = this.m_extractedValues[i];
/* 292 */       if ((alreadyImplementedFields != null) && (StringUtils.findStringIndex(alreadyImplementedFields, defValues[0]) >= 0))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 299 */       boolean isOptList = StringUtils.convertToBool(defValues[3], false);
/* 300 */       boolean strictChoice = false;
/* 301 */       boolean enableListButton = true;
/* 302 */       boolean multiSelect = false;
/*     */ 
/* 304 */       if (isOptList)
/*     */       {
/* 306 */         Vector v = StringUtils.parseArray(defValues[4], ',', '^');
/* 307 */         if (v != null)
/*     */         {
/* 309 */           String choice = (String)v.elementAt(0);
/* 310 */           strictChoice = choice.equalsIgnoreCase("choice");
/* 311 */           multiSelect = choice.indexOf("multi") >= 0;
/*     */         }
/*     */ 
/* 314 */         if (v.size() > 1)
/*     */         {
/* 316 */           enableListButton = false;
/*     */         }
/*     */ 
/* 319 */         if ((!enableListButton) && 
/* 321 */           (!defValues[0].equals("dUserTimeZone")))
/*     */         {
/* 323 */           v = SharedObjects.getOptList(defValues[5]);
/*     */ 
/* 325 */           if ((v == null) || (v.size() < 2))
/*     */           {
/* 327 */             isOptList = false;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 334 */       addUserFieldComponent(pnl, defValues[0], LocaleResources.getString(defValues[2], this.m_ctx), isOptList, strictChoice, multiSelect, enableListButton, defValues[5], addOverrideOption, defValues[1]);
/*     */     }
/*     */ 
/* 342 */     if (!addOverrideOption)
/*     */       return;
/* 344 */     this.m_helper.m_props.put("copyAll", "1");
/*     */   }
/*     */ 
/*     */   protected void addUserFieldComponent(JPanel pnl, String key, String caption, boolean isOptList, boolean strictChoice, boolean multi, boolean enableListButton, String optionsKey, boolean addOverrideOption, String type)
/*     */   {
/* 352 */     GridBagHelper gridHelper = this.m_helper.m_gridHelper;
/* 353 */     Component editComp = null;
/* 354 */     Component fieldComp = null;
/* 355 */     String fieldKey = null;
/*     */ 
/* 357 */     boolean isLabel = false;
/* 358 */     if (isOptList)
/*     */     {
/* 360 */       if (strictChoice)
/*     */       {
/* 362 */         editComp = new DisplayChoice();
/*     */       }
/* 366 */       else if (multi)
/*     */       {
/* 368 */         editComp = new ComboChoice(20, true);
/*     */       }
/*     */       else
/*     */       {
/* 372 */         editComp = new ComboChoice(20, false);
/*     */       }
/*     */ 
/*     */     }
/* 378 */     else if (strictChoice)
/*     */     {
/* 380 */       isLabel = true;
/* 381 */       editComp = new CustomLabel();
/*     */     }
/*     */     else
/*     */     {
/* 385 */       editComp = new CustomTextField(20);
/*     */     }
/*     */ 
/* 389 */     if (key.equals("dUserTimeZone"))
/*     */     {
/* 391 */       this.m_timeZoneChoice = ((DisplayChoice)editComp);
/* 392 */       this.m_timeZoneChoice.removeAllItems();
/* 393 */       this.m_timeZoneChoice.addItem("");
/* 394 */       FieldInfo labelField = new FieldInfo();
/* 395 */       this.m_timeZoneData.getFieldInfo("lcLabel", labelField);
/* 396 */       for (this.m_timeZoneData.first(); this.m_timeZoneData.isRowPresent(); this.m_timeZoneData.next())
/*     */       {
/* 398 */         this.m_timeZoneChoice.addItem(this.m_timeZoneData.getStringValue(labelField.m_index));
/*     */       }
/* 400 */       this.m_timeZoneChoice.setEnabled(true);
/*     */     }
/*     */     else
/*     */     {
/* 404 */       setOptionList(editComp, optionsKey);
/*     */     }
/*     */ 
/* 408 */     caption = LocaleResources.getString(caption, this.m_ctx);
/* 409 */     if ((isOptList) && (!strictChoice))
/*     */     {
/* 411 */       GridBagConstraints oldConstraints = (GridBagConstraints)gridHelper.m_gc.clone();
/* 412 */       JPanel subPanel = new PanePanel();
/*     */ 
/* 414 */       gridHelper.useGridBag(subPanel);
/* 415 */       gridHelper.m_gc.weightx = 1.0D;
/* 416 */       gridHelper.m_gc.fill = 2;
/*     */ 
/* 418 */       this.m_helper.addExchangeComponent(subPanel, editComp, key);
/*     */ 
/* 420 */       Properties fieldProps = new Properties();
/* 421 */       fieldProps.put("dType", "text");
/* 422 */       fieldProps.put("dOptionListKey", optionsKey);
/* 423 */       fieldProps.put("optionListDisplayName", caption);
/* 424 */       this.m_fieldPropsMap.put(key, fieldProps);
/*     */ 
/* 426 */       if (enableListButton)
/*     */       {
/* 428 */         ActionListener listener = new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e)
/*     */           {
/* 432 */             String name = e.getActionCommand();
/* 433 */             EditUserInfoPanel.this.editUserOptionList(name);
/*     */           }
/*     */         };
/* 437 */         gridHelper.prepareAddLastRowElement();
/* 438 */         gridHelper.m_gc.weightx = 0.0D;
/* 439 */         this.m_helper.addCommandButton(subPanel, LocaleResources.getString("apDlgButtonList", this.m_ctx), key, listener);
/*     */       }
/*     */ 
/* 442 */       gridHelper.m_gc = oldConstraints;
/*     */ 
/* 444 */       fieldComp = subPanel;
/* 445 */       fieldKey = "dummyKey";
/*     */     }
/*     */     else
/*     */     {
/* 449 */       fieldComp = editComp;
/* 450 */       fieldKey = key;
/*     */     }
/*     */ 
/* 453 */     if (strictChoice)
/*     */     {
/* 455 */       String val = this.m_helper.m_props.getProperty(key);
/* 456 */       String defVal = this.m_helper.m_props.getProperty("Default_" + key);
/* 457 */       if (isLabel)
/*     */       {
/* 459 */         if ((val != null) && (val.length() > 0) && (defVal != null) && (!val.equals(defVal)))
/*     */         {
/* 463 */           this.m_helper.m_props.put(fieldKey, defVal);
/*     */         }
/*     */ 
/* 467 */         fieldKey = "Default_" + fieldKey;
/*     */       }
/* 469 */       else if ((((val == null) || (val.length() == 0))) && 
/* 471 */         (defVal != null))
/*     */       {
/* 474 */         this.m_helper.m_props.put(fieldKey, defVal);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 479 */     if (type.equals("Int"))
/*     */     {
/* 481 */       this.m_intFields.put(caption, fieldComp);
/*     */     }
/* 483 */     if (type.equals("Date"))
/*     */     {
/* 485 */       this.m_dateFields.put(caption, fieldComp);
/*     */     }
/*     */ 
/* 488 */     this.m_helper.addLabelFieldPairEx(pnl, caption, fieldComp, fieldKey, !addOverrideOption);
/*     */ 
/* 490 */     if (!addOverrideOption)
/*     */       return;
/* 492 */     JCheckBox checkBox = new JCheckBox();
/* 493 */     String checkBoxId = key + ":override";
/* 494 */     gridHelper.prepareAddLastRowElement();
/* 495 */     int oldfill = gridHelper.m_gc.fill;
/* 496 */     gridHelper.m_gc.fill = 0;
/* 497 */     this.m_helper.addExchangeComponent(pnl, checkBox, checkBoxId);
/* 498 */     gridHelper.m_gc.fill = oldfill;
/*     */   }
/*     */ 
/*     */   protected void setOptionList(Object comp, String optionsKey)
/*     */   {
/* 504 */     Vector optList = SharedObjects.getOptList(optionsKey);
/* 505 */     if (comp instanceof DisplayChoice)
/*     */     {
/* 507 */       DisplayChoice displayChoice = (DisplayChoice)comp;
/* 508 */       displayChoice.init(optList);
/*     */     } else {
/* 510 */       if (!comp instanceof ComboChoice)
/*     */         return;
/* 512 */       ComboChoice comboChoice = (ComboChoice)comp;
/* 513 */       comboChoice.initChoiceList(optList);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void editUserOptionList(String key)
/*     */   {
/* 519 */     Properties props = (Properties)this.m_fieldPropsMap.get(key);
/* 520 */     if (props == null)
/*     */     {
/* 522 */       reportError(IdcMessageFactory.lc("apMisconfigurationOfOptionLists", new Object[0]));
/* 523 */       return;
/*     */     }
/*     */ 
/* 526 */     String title = LocaleResources.getString("apLabelOptionList", this.m_ctx);
/* 527 */     EditOptionListDlg edtOptions = new EditOptionListDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("OptionList"), "UPDATE_USEROPTION_LIST");
/*     */ 
/* 529 */     edtOptions.init(props);
/*     */ 
/* 531 */     if (edtOptions.prompt() != 1)
/*     */       return;
/* 533 */     Object[] compList = this.m_helper.m_exchange.findComponent(key, false);
/* 534 */     if (compList.length != 4)
/*     */       return;
/* 536 */     setOptionList(compList[1], props.getProperty("dOptionListKey"));
/*     */   }
/*     */ 
/*     */   protected void addItemListener(JCheckBox chbox, Component cmp)
/*     */   {
/* 543 */     Component myCmp = cmp;
/*     */ 
/* 545 */     ItemListener il = new ItemListener(myCmp)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 549 */         boolean isSelected = e.getStateChange() == 1;
/* 550 */         this.val$myCmp.setEnabled(isSelected);
/*     */       }
/*     */     };
/* 554 */     chbox.addItemListener(il);
/*     */   }
/*     */ 
/*     */   protected void setOverrideFlagValues()
/*     */   {
/* 559 */     Properties props = this.m_helper.m_props;
/* 560 */     String val = props.getProperty("dUserSourceFlags");
/* 561 */     int flags = 0;
/* 562 */     if ((val != null) && (val.length() > 0))
/*     */     {
/* 564 */       flags = Integer.parseInt(val);
/*     */     }
/*     */ 
/* 567 */     if (this.m_extractedValues == null)
/*     */       return;
/* 569 */     for (int i = 0; i < this.m_extractedValues.length; ++i)
/*     */     {
/* 571 */       String bitFlag = this.m_extractedValues[i][6];
/* 572 */       int bit = Integer.parseInt(bitFlag);
/* 573 */       boolean isOverride = (flags & bit) != 0;
/* 574 */       props.put(this.m_extractedValues[i][0] + ":override", String.valueOf(isOverride));
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 586 */     if (super.prompt() == 0)
/*     */     {
/* 588 */       return 0;
/*     */     }
/*     */ 
/* 591 */     if (this.m_isExternal)
/*     */     {
/* 594 */       this.m_helper.m_props.put("dPassword", "");
/* 595 */       this.m_helper.m_props.put("confirmPswrd", "");
/*     */     }
/* 599 */     else if ((!validatePassword()) || (!validateFormats()))
/*     */     {
/* 601 */       return 0;
/*     */     }
/*     */ 
/* 604 */     return 1;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 613 */     String name = exchange.m_compName;
/* 614 */     String val = exchange.m_compValue;
/* 615 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 617 */     if (val != null)
/*     */     {
/* 619 */       val = val.trim();
/*     */     }
/*     */ 
/* 622 */     IdcMessage errMsg = null;
/* 623 */     if (name.equals("dName"))
/*     */     {
/* 625 */       if (val.equalsIgnoreCase("anonymous"))
/*     */       {
/* 627 */         errMsg = IdcMessageFactory.lc("apCannotAddAnonymousUser", new Object[0]);
/*     */       }
/*     */       else
/*     */       {
/* 631 */         errMsg = Validation.checkFormFieldForDB(val, "apUserNameErrorStub", maxLength, null);
/*     */       }
/*     */     }
/*     */ 
/* 635 */     if (name.equals("dFullName"))
/*     */     {
/* 637 */       if ((val != null) && (val.length() > maxLength))
/*     */       {
/* 639 */         errMsg = IdcMessageFactory.lc("apFullNameExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 642 */     else if (name.equals("dEmail"))
/*     */     {
/* 644 */       if ((val != null) && (val.length() > maxLength))
/*     */       {
/* 646 */         errMsg = IdcMessageFactory.lc("apEmailExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */       }
/*     */     }
/* 649 */     else if (name.equals("dPassword"))
/*     */     {
/*     */       try
/*     */       {
/* 653 */         Validation.validatePassword(val);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 657 */         errMsg = IdcMessageFactory.lc(e);
/*     */       }
/*     */ 
/*     */     }
/* 662 */     else if ((val != null) && (val.length() > maxLength))
/*     */     {
/* 664 */       errMsg = IdcMessageFactory.lc("apMetaFieldExceedsMaxLength", new Object[] { name, Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 668 */     if (errMsg != null)
/*     */     {
/* 670 */       exchange.m_errorMessage = errMsg;
/* 671 */       return false;
/*     */     }
/* 673 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean validatePassword()
/*     */   {
/* 681 */     Properties props = this.m_helper.m_props;
/*     */ 
/* 683 */     String password = props.getProperty("dPassword");
/* 684 */     String confirm = props.getProperty("confirmPswrd");
/* 685 */     if (!password.equals(confirm))
/*     */     {
/* 687 */       reportError(IdcMessageFactory.lc("apPasswordNotConfirmed", new Object[0]));
/* 688 */       return false;
/*     */     }
/*     */ 
/* 691 */     int passwordMinLength = SharedObjects.getEnvironmentInt("MinimumPasswordLength", 0);
/* 692 */     if (password.length() < passwordMinLength)
/*     */     {
/* 694 */       IdcMessage msg = IdcMessageFactory.lc("apPasswordUnderMinLength", new Object[] { Integer.valueOf(passwordMinLength) });
/* 695 */       reportError(msg);
/* 696 */       return false;
/*     */     }
/*     */ 
/* 700 */     String user = props.getProperty("dName");
/* 701 */     Users users = (Users)SharedObjects.getTable("Users");
/* 702 */     if ((users != null) && 
/* 704 */       (!password.equals(Users.getPasswordDash())))
/*     */     {
/* 706 */       String passwordEncoding = users.getDefaultPasswordEncoding();
/* 707 */       password = UserUtils.encodePassword(user, password, passwordEncoding);
/* 708 */       props.put("dPassword", password);
/* 709 */       props.put("dPasswordEncoding", passwordEncoding);
/*     */     }
/*     */ 
/* 712 */     props.remove("confirmPswrd");
/* 713 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean validateFormats()
/*     */   {
/* 718 */     Enumeration keys = this.m_intFields.keys();
/* 719 */     CustomTextField f = null;
/* 720 */     Object o = null;
/* 721 */     String caption = null;
/* 722 */     String value = null;
/*     */ 
/* 725 */     while (keys.hasMoreElements())
/*     */     {
/* 727 */       caption = (String)keys.nextElement();
/* 728 */       o = this.m_intFields.get(caption);
/*     */ 
/* 730 */       if (!o instanceof CustomTextField)
/*     */         continue;
/* 732 */       f = (CustomTextField)o;
/* 733 */       value = f.getText();
/*     */       try
/*     */       {
/* 738 */         if (!value.trim().equals(""))
/*     */         {
/* 740 */           Integer.parseInt(value);
/*     */         }
/*     */       }
/*     */       catch (Exception ex)
/*     */       {
/* 745 */         IdcMessage msg = IdcMessageFactory.lc("apIntegerParseError", new Object[] { value, caption });
/* 746 */         MessageBox.reportError(this.m_systemInterface, msg);
/* 747 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 753 */     keys = this.m_dateFields.keys();
/* 754 */     while (keys.hasMoreElements())
/*     */     {
/* 756 */       caption = (String)keys.nextElement();
/* 757 */       o = this.m_dateFields.get(caption);
/*     */ 
/* 759 */       if (!o instanceof CustomTextField)
/*     */         continue;
/* 761 */       f = (CustomTextField)o;
/* 762 */       value = f.getText();
/*     */       try
/*     */       {
/* 766 */         if (!value.trim().equals(""))
/*     */         {
/* 768 */           LocaleResources.parseDate(value, this.m_ctx);
/*     */         }
/*     */       }
/*     */       catch (Exception ex)
/*     */       {
/* 773 */         IdcMessage msg = IdcMessageFactory.lc("apDateParseError", new Object[] { value, caption });
/* 774 */         MessageBox.reportError(this.m_systemInterface, ex, msg);
/* 775 */         return false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 780 */     return true;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 786 */     if (exchange.m_compName.equals("dUserTimeZone"))
/*     */     {
/* 788 */       if (updateComponent)
/*     */       {
/* 790 */         String value = this.m_helper.m_props.getProperty("dUserTimeZone");
/* 791 */         if ((value != null) && (value.length() > 0))
/*     */         {
/*     */           try
/*     */           {
/* 795 */             String label = ResultSetUtils.findValue(this.m_timeZoneData, "lcTimeZone", value, "lcLabel");
/*     */ 
/* 797 */             exchange.m_compValue = label;
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 806 */           exchange.m_compValue = "";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 811 */         String label = this.m_timeZoneChoice.getSelectedInternalValue();
/* 812 */         if (label.length() > 0)
/*     */         {
/*     */           try
/*     */           {
/* 816 */             String value = ResultSetUtils.findValue(this.m_timeZoneData, "lcLabel", label, "lcTimeZone");
/*     */ 
/* 818 */             this.m_helper.m_props.put("dUserTimeZone", value);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 822 */             this.m_helper.m_props.put("dUserTimeZone", "");
/*     */           }
/*     */ 
/*     */         }
/*     */         else {
/* 827 */           this.m_helper.m_props.put("dUserTimeZone", "");
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/* 833 */       super.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 839 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80531 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserInfoPanel
 * JD-Core Version:    0.5.4
 */