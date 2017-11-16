/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.server.utils.ComponentPreferenceData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class InstallPanel extends CWizardPanel
/*     */ {
/*     */   protected String[][] m_prefDataColMap;
/*     */   protected UdlPanel m_prefList;
/*     */   protected DialogHelper m_dlgHelper;
/*     */   protected String[][] m_msgChoiceList;
/*     */   protected String[][] m_promptChoiceList;
/*     */   protected ComponentPreferenceData m_prefData;
/*     */   protected DataResultSet m_prefResultSet;
/*     */   protected JButton m_filterBtn;
/*     */   protected JButton m_strBtn;
/*     */   protected String m_intradocDir;
/*     */   protected boolean m_isUILoad;
/*     */ 
/*     */   public InstallPanel()
/*     */   {
/*  70 */     this.m_prefDataColMap = new String[][] { { "pName", "!csCompWizLabelName", "30" }, { "pMsgType", "!csCompWizLabelType", "10" }, { "pMessage", "!csCompWizLabelPrompt", "30" } };
/*     */ 
/*  74 */     this.m_prefList = null;
/*  75 */     this.m_dlgHelper = null;
/*  76 */     this.m_msgChoiceList = new String[][] { { "info", "csCompWizInformationType" }, { "installonly", "csCompWizInstallType" }, { "postinstallonly", "csCompWizPostInstallType" }, { "configurable", "csCompWizConfigurableType" } };
/*     */ 
/*  80 */     this.m_promptChoiceList = new String[][] { { "string", "csCompWizLabelString" }, { "boolean", "csCompWizLabelBoolean" }, { "integer", "csCompWizLabelInteger" }, { "options", "csCompWizLabelOptionList" } };
/*     */ 
/*  85 */     this.m_prefData = null;
/*  86 */     this.m_prefResultSet = null;
/*  87 */     this.m_filterBtn = null;
/*  88 */     this.m_strBtn = null;
/*     */ 
/*  90 */     this.m_intradocDir = null;
/*     */ 
/*  92 */     this.m_isUILoad = false;
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  97 */     JPanel nPanel = new PanePanel();
/*  98 */     this.m_helper.makePanelGridBag(this, 2);
/*  99 */     this.m_filterBtn = new JButton(LocaleResources.getString("csCompWizLanchEditor", null));
/* 100 */     addCheckboxAndButton(this, "csCompWizInstallFilter", "hasInstallFilter", this.m_filterBtn, "installFilter");
/*     */ 
/* 102 */     this.m_strBtn = new JButton(LocaleResources.getString("csCompWizLanchEditor", null));
/* 103 */     addCheckboxAndButton(this, "csCompWizInstallStrings", "hasInstallStrings", this.m_strBtn, "installStrings");
/*     */ 
/* 105 */     initListPanel(nPanel);
/* 106 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 107 */     this.m_helper.addComponent(this, nPanel);
/*     */ 
/* 109 */     this.m_intradocDir = FileUtils.directorySlashes(SharedObjects.getEnvironmentValue("IntradocDir"));
/*     */   }
/*     */ 
/*     */   protected void addCheckboxAndButton(JPanel panel, String checkBoxLabel, String checkBoxName, JButton btn, String type)
/*     */   {
/* 115 */     String sourceType = type;
/* 116 */     JButton button = btn;
/* 117 */     ItemListener iListener = new ItemListener(sourceType, button)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 121 */         if (InstallPanel.this.m_isUILoad)
/*     */         {
/* 123 */           return;
/*     */         }
/* 125 */         int state = e.getStateChange();
/* 126 */         switch (state)
/*     */         {
/*     */         case 1:
/* 131 */           if (this.val$sourceType.equals("installFilter"))
/*     */           {
/*     */             try
/*     */             {
/* 135 */               InstallPanel.this.addOrRemoveFilters(true);
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 139 */               CWizardGuiUtils.reportError(InstallPanel.this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizUnableToIncludeInstallFilters", new Object[0]));
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/*     */             try
/*     */             {
/* 147 */               InstallPanel.this.m_component.m_binder.putLocal("hasInstallStrings", "true");
/* 148 */               String filePath = InstallPanel.this.m_component.m_absCompDir + "install_strings.htm";
/* 149 */               if (InstallPanel.this.m_component.createInstallStringFile(filePath))
/*     */               {
/* 151 */                 CWizardGuiUtils.doMessage(InstallPanel.this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizCreatedStringFile", new Object[] { filePath }), 1);
/*     */               }
/*     */ 
/* 155 */               InstallPanel.this.m_component.updateResDefFile();
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 159 */               CWizardGuiUtils.reportError(InstallPanel.this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizUnableToIncludeInstallStrings", new Object[0]));
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 164 */           this.val$button.setEnabled(true);
/* 165 */           break;
/*     */         case 2:
/* 167 */           if (this.val$sourceType.equals("installFilter"))
/*     */           {
/*     */             try
/*     */             {
/* 171 */               InstallPanel.this.addOrRemoveFilters(false);
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 175 */               if (SystemUtils.m_verbose)
/*     */               {
/* 177 */                 Report.debug(null, null, exp);
/*     */               }
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 183 */             InstallPanel.this.m_component.m_binder.putLocal("hasInstallStrings", "false");
/*     */             try
/*     */             {
/* 187 */               InstallPanel.this.m_component.updateResDefFile();
/*     */             }
/*     */             catch (Exception exp)
/*     */             {
/* 191 */               if (SystemUtils.m_verbose)
/*     */               {
/* 193 */                 Report.debug(null, null, exp);
/*     */               }
/*     */             }
/*     */           }
/* 197 */           this.val$button.setEnabled(false);
/*     */         }
/*     */       }
/*     */     };
/* 202 */     CWizardGuiUtils.addCheckbox(this.m_helper, panel, checkBoxLabel, checkBoxName, true, iListener, false);
/*     */ 
/* 204 */     ActionListener launchListener = new ActionListener(sourceType)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 208 */         String location = null;
/* 209 */         if (this.val$sourceType.equals("installFilter"))
/*     */         {
/* 211 */           String className = InstallPanel.this.m_component.m_name + "InstallFilter";
/* 212 */           location = InstallPanel.this.m_intradocDir + "classes/" + InstallPanel.this.m_component.m_name + "/" + className + ".java";
/*     */         }
/*     */         else
/*     */         {
/* 216 */           location = InstallPanel.this.m_component.m_absCompDir + "install_strings.htm";
/*     */         }
/* 218 */         CWizardGuiUtils.launchEditor(InstallPanel.this.m_systemInterface, location);
/*     */       }
/*     */     };
/* 222 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 223 */     this.m_helper.addComponent(panel, CWizardGuiUtils.addButton(button, launchListener, false));
/*     */   }
/*     */ 
/*     */   protected void addOrRemoveFilters(boolean isAdd) throws ServiceException
/*     */   {
/* 228 */     addOrRemoveFilter("extraAfterConfigInit", isAdd);
/* 229 */     addOrRemoveFilter("extraBeforeCacheLoadInit", isAdd);
/* 230 */     addOrRemoveFilter("extraAfterServicesLoadInit", isAdd);
/* 231 */     addOrRemoveFilter("initSubjects", isAdd);
/*     */ 
/* 233 */     if (isAdd) {
/*     */       return;
/*     */     }
/* 236 */     addOrRemoveFilter(this.m_component.m_name + "ComponentUninstallFilter", isAdd);
/*     */   }
/*     */ 
/*     */   protected void addOrRemoveFilter(String name, boolean isAdd)
/*     */     throws ServiceException
/*     */   {
/* 242 */     Properties props = new Properties();
/* 243 */     String className = this.m_component.m_name + "InstallFilter.java";
/* 244 */     props.put("type", name);
/* 245 */     props.put("parameter", name);
/* 246 */     String filePath = this.m_intradocDir + "classes/" + this.m_component.m_name + "/" + className;
/* 247 */     props.put("location", this.m_component.m_name + "." + this.m_component.m_name + "InstallFilter");
/* 248 */     props.put("loadOrder", "50");
/* 249 */     props.put("filename", filePath);
/*     */ 
/* 251 */     if (isAdd)
/*     */     {
/* 253 */       if (!this.m_component.addJavaCode(props, true, true, true))
/*     */         return;
/* 255 */       IdcMessage msg = IdcMessageFactory.lc("csCompWizCreatedJava", new Object[] { filePath });
/* 256 */       CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*     */     }
/*     */     else
/*     */     {
/* 261 */       this.m_component.deleteJavaCode(props, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initListPanel(JPanel panel)
/*     */   {
/* 267 */     this.m_helper.makePanelGridBag(panel, 1);
/* 268 */     this.m_prefList = createUdlPanel("!csCompWizCustomUserPreferenceTitle", 250, 14, "PrefereceData", true, this.m_prefDataColMap, this.m_prefDataColMap[0][0], false);
/*     */ 
/* 271 */     ItemListener cItemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 275 */         int state = e.getStateChange();
/* 276 */         switch (state)
/*     */         {
/*     */         case 1:
/*     */         case 2:
/*     */         }
/*     */       }
/*     */     };
/* 285 */     this.m_prefList.m_list.addItemListener(cItemListener);
/*     */ 
/* 287 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 291 */         if (InstallPanel.this.m_component == null)
/*     */         {
/* 293 */           IdcMessage errMsg = IdcMessageFactory.lc("csCompWizCompInfoNotFound", new Object[0]);
/* 294 */           CWizardGuiUtils.reportError(InstallPanel.this.m_systemInterface, null, errMsg);
/* 295 */           return;
/*     */         }
/* 297 */         String cmdStr = e.getActionCommand();
/* 298 */         if ((cmdStr.equals("add")) || (cmdStr.equals("edit")))
/*     */         {
/* 300 */           int index = InstallPanel.this.m_prefList.getSelectedIndex();
/* 301 */           Properties props = null;
/* 302 */           if (cmdStr.equals("edit"))
/*     */           {
/* 304 */             props = InstallPanel.this.m_prefList.getDataAt(index);
/*     */           }
/* 306 */           InstallPanel.this.addEditPreference(props);
/*     */         } else {
/* 308 */           if (!cmdStr.equals("delete"))
/*     */             return;
/* 310 */           InstallPanel.this.delete();
/*     */         }
/*     */       }
/*     */     };
/* 315 */     JPanel wrapper = CWizardGuiUtils.addWrapperPanel(this.m_helper, true);
/* 316 */     this.m_prefList.add("East", CWizardGuiUtils.addUdlPanelCommandButtons(this.m_helper, this.m_prefList, listener, false));
/* 317 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 318 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 319 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 320 */     this.m_helper.addComponent(wrapper, this.m_prefList);
/* 321 */     this.m_helper.addComponent(panel, wrapper);
/* 322 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   protected void delete()
/*     */   {
/* 327 */     int index = this.m_prefList.getSelectedIndex();
/* 328 */     if (index < 0)
/*     */     {
/* 330 */       IdcMessage errMsg = IdcMessageFactory.lc("csCompWizRemovePrefWarning", new Object[0]);
/* 331 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, errMsg);
/* 332 */       return;
/*     */     }
/*     */ 
/* 335 */     Properties props = this.m_prefList.getDataAt(index);
/* 336 */     String name = props.getProperty("pName");
/*     */ 
/* 338 */     if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizPromptRemove", new Object[] { name }), 4) != 2) {
/*     */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 344 */       this.m_prefData.deletePrefTableRow(name);
/* 345 */       refreshList(null);
/* 346 */       this.m_helper.loadComponentValues();
/* 347 */       this.m_prefList.enableDisable(false);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 351 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizRemovedFailed", new Object[] { name }));
/*     */ 
/* 353 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcMessage assignComponentInfo(IntradocComponent comp, boolean reloadAll, Map options)
/*     */   {
/* 361 */     this.m_component = comp;
/*     */ 
/* 363 */     if (comp != null)
/*     */     {
/* 365 */       this.m_prefData = comp.m_prefData;
/*     */ 
/* 367 */       this.m_helper.m_props = new Properties();
/* 368 */       DataResultSet drset = this.m_component.getFiltersTable();
/*     */ 
/* 370 */       Vector v = drset.findRow(1, this.m_component.m_name + "InstallFilter");
/* 371 */       boolean hasInstFilter = v != null;
/* 372 */       this.m_helper.m_props.put("hasInstallFilter", "" + hasInstFilter);
/* 373 */       this.m_filterBtn.setEnabled(hasInstFilter);
/*     */ 
/* 375 */       boolean hasInstStr = StringUtils.convertToBool(this.m_component.m_binder.getLocal("hasInstallStrings"), false);
/* 376 */       this.m_helper.m_props.put("hasInstallStrings", "" + hasInstStr);
/* 377 */       this.m_strBtn.setEnabled(hasInstStr);
/*     */     }
/*     */     else
/*     */     {
/* 381 */       if (this.m_helper.m_props == null)
/*     */       {
/* 383 */         this.m_helper.m_props = new Properties();
/*     */       }
/* 385 */       this.m_filterBtn.setEnabled(false);
/* 386 */       this.m_strBtn.setEnabled(false);
/* 387 */       this.m_prefData = new ComponentPreferenceData();
/* 388 */       this.m_helper.m_props.put("hasInstallFilter", "0");
/* 389 */       this.m_helper.m_props.put("hasInstallStrings", "0");
/*     */     }
/* 391 */     this.m_isUILoad = true;
/* 392 */     this.m_helper.loadComponentValues();
/* 393 */     this.m_isUILoad = false;
/* 394 */     refreshList(null);
/* 395 */     return null;
/*     */   }
/*     */ 
/*     */   protected void refreshList(String selObj)
/*     */   {
/* 400 */     ResultSet rset = null;
/* 401 */     if (this.m_prefData == null)
/*     */       return;
/* 403 */     rset = this.m_prefData.getPreferenceTable();
/* 404 */     this.m_prefList.refreshList(rset, selObj);
/* 405 */     if (selObj == null)
/*     */     {
/* 407 */       this.m_prefList.enableDisable(false);
/*     */     }
/*     */     else
/*     */     {
/* 411 */       this.m_prefList.enableDisable(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addEditPreference(Properties props)
/*     */   {
/* 418 */     String title = "csCompWizAddPref";
/* 419 */     String help = "CW_AddPreference";
/* 420 */     if (props != null)
/*     */     {
/* 422 */       title = "csCompWizEditPref";
/* 423 */       help = "CW_EditPreference";
/*     */     }
/* 425 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString(title, null), true);
/*     */ 
/* 427 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage(help);
/*     */ 
/* 429 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 430 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 432 */     if (props != null)
/*     */     {
/* 434 */       this.m_dlgHelper.addLabelDisplayPair(mainPanel, LocaleResources.getString("csCompWizardPrefName", null), 30, "pName");
/*     */     }
/*     */     else
/*     */     {
/* 439 */       this.m_dlgHelper.addLabelEditPair(mainPanel, LocaleResources.getString("csCompWizardPrefName", null), 30, "pName");
/*     */     }
/*     */ 
/* 443 */     boolean isAdd = props == null;
/* 444 */     DisplayChoice msgChoice = new DisplayChoice();
/* 445 */     LocaleResources.localizeDoubleArray(this.m_msgChoiceList, null, 1);
/* 446 */     msgChoice.init(this.m_msgChoiceList);
/* 447 */     DisplayChoice promptChoice = new DisplayChoice();
/* 448 */     LocaleResources.localizeDoubleArray(this.m_promptChoiceList, null, 1);
/* 449 */     promptChoice.init(this.m_promptChoiceList);
/* 450 */     JTextField labelField = new CustomTextField(40);
/* 451 */     JTextField optNameTextField = new CustomTextField(40);
/* 452 */     JTextField valueField = new CustomTextField(40);
/* 453 */     JCheckBox alwaysUseDefaultsField = new CustomCheckbox("   ", 1);
/* 454 */     JTextField optDispCol = new CustomTextField(40);
/* 455 */     JTextField isDisabledField = new CustomTextField(40);
/* 456 */     JCheckBox isRequiredField = new CustomCheckbox("   ", 1);
/*     */ 
/* 458 */     this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefLabel", null), labelField, "pLabel");
/*     */ 
/* 461 */     this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefMsgType", null), msgChoice, "pNewMsgType");
/*     */ 
/* 464 */     CustomLabel promptChoiceLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefPromptType", null), promptChoice, "pPromptType");
/*     */ 
/* 467 */     CustomLabel optNameLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefOptListName", null), optNameTextField, "pOptionListName");
/*     */ 
/* 470 */     CustomLabel optDispColLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefOptListDispCol", null), optDispCol, "pOptionListDispCol");
/*     */ 
/* 473 */     JTextArea msgBox = new CustomTextArea(4, 40);
/* 474 */     this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefMessage", null), msgBox, "pMessage");
/*     */ 
/* 477 */     CustomLabel valueLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefValue", null), valueField, "pValue");
/*     */ 
/* 480 */     CustomLabel alwaysUseDefaultsLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefAlwaysUseDefaults", null), alwaysUseDefaultsField, "pAlwaysUseDefaultsOnInstall");
/*     */ 
/* 483 */     CustomLabel isDisabledLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefIsDisabled", null), isDisabledField, "pIsDisabled");
/*     */ 
/* 486 */     CustomLabel isRequiredLabel = this.m_dlgHelper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizPrefRequireValue", null), isRequiredField, "pIsRequired");
/*     */ 
/* 490 */     ItemListener mclistener = new ItemListener(msgChoice, promptChoice, optNameTextField, optDispCol, valueField, alwaysUseDefaultsField, isDisabledField, promptChoiceLabel, optNameLabel, optDispColLabel, valueLabel, alwaysUseDefaultsLabel, isDisabledLabel, isRequiredLabel, isRequiredField)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 494 */         int state = e.getStateChange();
/* 495 */         if (state != 1)
/*     */         {
/* 497 */           return;
/*     */         }
/*     */ 
/* 500 */         String msgType = this.val$msgChoice.getSelectedInternalValue();
/* 501 */         if (msgType.equals("info"))
/*     */         {
/* 503 */           this.val$promptChoice.setEnabled(false);
/* 504 */           this.val$optNameTextField.setEnabled(false);
/* 505 */           this.val$optDispCol.setEnabled(false);
/* 506 */           this.val$valueField.setEnabled(false);
/* 507 */           this.val$alwaysUseDefaultsField.setEnabled(false);
/* 508 */           this.val$isDisabledField.setEnabled(false);
/* 509 */           this.val$promptChoiceLabel.setEnabled(false);
/* 510 */           this.val$optNameLabel.setEnabled(false);
/* 511 */           this.val$optDispColLabel.setEnabled(false);
/* 512 */           this.val$valueLabel.setEnabled(false);
/* 513 */           this.val$alwaysUseDefaultsLabel.setEnabled(false);
/* 514 */           this.val$isDisabledLabel.setEnabled(false);
/* 515 */           this.val$isRequiredLabel.setEnabled(false);
/* 516 */           this.val$isRequiredField.setEnabled(false);
/* 517 */           this.val$isRequiredField.setSelected(false);
/*     */         }
/*     */         else
/*     */         {
/* 521 */           this.val$promptChoice.setEnabled(true);
/* 522 */           this.val$valueField.setEnabled(true);
/* 523 */           this.val$alwaysUseDefaultsField.setEnabled(true);
/* 524 */           this.val$isDisabledField.setEnabled(true);
/* 525 */           this.val$promptChoiceLabel.setEnabled(true);
/* 526 */           this.val$valueLabel.setEnabled(true);
/* 527 */           this.val$alwaysUseDefaultsLabel.setEnabled(true);
/* 528 */           this.val$isDisabledLabel.setEnabled(true);
/*     */ 
/* 530 */           boolean enable = !msgType.equals("postinstallonly");
/* 531 */           this.val$isRequiredLabel.setEnabled(enable);
/* 532 */           this.val$isRequiredField.setEnabled(enable);
/* 533 */           if (msgType.equals("installonly"))
/*     */           {
/* 535 */             this.val$isRequiredField.setSelected(true);
/*     */           }
/*     */ 
/* 538 */           String promptType = this.val$promptChoice.getSelectedInternalValue();
/* 539 */           if (!promptType.equals("options"))
/*     */             return;
/* 541 */           this.val$optNameTextField.setEnabled(true);
/* 542 */           this.val$optDispCol.setEnabled(true);
/* 543 */           this.val$optNameLabel.setEnabled(true);
/* 544 */           this.val$optDispColLabel.setEnabled(true);
/*     */         }
/*     */       }
/*     */     };
/* 549 */     msgChoice.addItemListener(mclistener);
/*     */ 
/* 551 */     ItemListener pclistener = new ItemListener(promptChoice, optNameTextField, optDispCol, optNameLabel, optDispColLabel)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 555 */         int state = e.getStateChange();
/* 556 */         if (state != 1)
/*     */         {
/* 558 */           return;
/*     */         }
/*     */ 
/* 561 */         String promptType = this.val$promptChoice.getSelectedInternalValue();
/* 562 */         if (promptType.equals("options"))
/*     */         {
/* 564 */           this.val$optNameTextField.setEnabled(true);
/* 565 */           this.val$optDispCol.setEnabled(true);
/* 566 */           this.val$optNameLabel.setEnabled(true);
/* 567 */           this.val$optDispColLabel.setEnabled(true);
/*     */         }
/*     */         else
/*     */         {
/* 571 */           this.val$optNameTextField.setEnabled(false);
/* 572 */           this.val$optDispCol.setEnabled(false);
/* 573 */           this.val$optNameLabel.setEnabled(false);
/* 574 */           this.val$optDispColLabel.setEnabled(false);
/*     */         }
/*     */       }
/*     */     };
/* 578 */     promptChoice.addItemListener(pclistener);
/*     */ 
/* 580 */     if ((isAdd) || (props.getProperty("pNewMsgType").equals("info")))
/*     */     {
/* 582 */       msgChoice.setSelectedIndex(0);
/* 583 */       promptChoice.setEnabled(false);
/* 584 */       optNameTextField.setEnabled(false);
/* 585 */       optDispCol.setEnabled(false);
/* 586 */       valueField.setEnabled(false);
/* 587 */       alwaysUseDefaultsField.setEnabled(false);
/* 588 */       isDisabledField.setEnabled(false);
/* 589 */       promptChoiceLabel.setEnabled(false);
/* 590 */       optNameLabel.setEnabled(false);
/* 591 */       optDispColLabel.setEnabled(false);
/* 592 */       valueLabel.setEnabled(false);
/* 593 */       alwaysUseDefaultsLabel.setEnabled(false);
/* 594 */       isDisabledLabel.setEnabled(false);
/* 595 */       isRequiredLabel.setEnabled(false);
/* 596 */       isRequiredField.setEnabled(false);
/*     */     }
/* 598 */     else if (!props.getProperty("pPromptType").equals("options"))
/*     */     {
/* 600 */       optNameTextField.setEnabled(false);
/* 601 */       optDispCol.setEnabled(false);
/* 602 */       optNameLabel.setEnabled(false);
/* 603 */       optDispColLabel.setEnabled(false);
/*     */     }
/*     */ 
/* 606 */     DialogCallback okCallback = new DialogCallback(isAdd)
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 613 */           InstallPanel.this.onOk(this.val$isAdd);
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 617 */           CWizardGuiUtils.reportError(InstallPanel.this.m_systemInterface, exp, (IdcMessage)null);
/* 618 */           return false;
/*     */         }
/* 620 */         return true;
/*     */       }
/*     */     };
/* 624 */     this.m_dlgHelper.addOK(okCallback);
/* 625 */     this.m_dlgHelper.addCancel(null);
/* 626 */     this.m_dlgHelper.addHelp(null);
/*     */ 
/* 628 */     if (props != null)
/*     */     {
/* 630 */       this.m_dlgHelper.m_props = props;
/*     */     }
/*     */ 
/* 633 */     if (this.m_dlgHelper.prompt() != 1)
/*     */       return;
/* 635 */     String name = this.m_dlgHelper.m_props.getProperty("pName");
/* 636 */     refreshList(name);
/*     */   }
/*     */ 
/*     */   public boolean onOk(boolean isAdd)
/*     */     throws ServiceException, DataException
/*     */   {
/* 642 */     Properties props = this.m_dlgHelper.m_props;
/* 643 */     String name = props.getProperty("pName");
/* 644 */     String msgType = props.getProperty("pNewMsgType");
/*     */ 
/* 646 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 648 */       throw new ServiceException("!csCompWizardPrefNameNotDefined");
/*     */     }
/*     */ 
/* 651 */     if (!msgType.equalsIgnoreCase("info"))
/*     */     {
/* 653 */       String promptType = props.getProperty("pPromptType");
/* 654 */       String optName = props.getProperty("pOptionListName");
/* 655 */       String optDispCol = props.getProperty("pOptionListDispCol");
/* 656 */       String msg = props.getProperty("pMessage");
/* 657 */       String dVal = props.getProperty("pValue");
/* 658 */       if ((promptType.equals("options")) && (((optName == null) || (optName.length() == 0) || (optDispCol == null) || (optDispCol.length() == 0))))
/*     */       {
/* 661 */         throw new ServiceException("!csCompWizOptionListNotDefined");
/*     */       }
/*     */ 
/* 664 */       if ((msg == null) || (msg.length() == 0))
/*     */       {
/* 666 */         throw new ServiceException("!csCompWizMessageNotDefined");
/*     */       }
/*     */ 
/* 669 */       if (dVal == null)
/*     */       {
/* 671 */         throw new ServiceException("!csCompWizDefaultValueNotDefined");
/*     */       }
/*     */ 
/* 674 */       String trimmedVal = dVal.trim();
/*     */ 
/* 676 */       if ((promptType.equalsIgnoreCase("boolean")) && (trimmedVal.length() > 0) && (!trimmedVal.contains("<$")) && (!trimmedVal.matches("[10tTfFyYnN].*")))
/*     */       {
/* 679 */         throw new ServiceException("!csCompWizDefaultBooleanValueInvalid");
/*     */       }
/*     */ 
/* 682 */       if ((promptType.equalsIgnoreCase("integer")) && (trimmedVal.length() > 0) && (!trimmedVal.contains("<$")) && (!trimmedVal.matches("\\d+")))
/*     */       {
/* 685 */         throw new ServiceException("!csCompWizDefaultIntegerValueInvalid");
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 691 */       this.m_prefData.addEditPrefTableRow(props, isAdd);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 695 */       throw new ServiceException("!csCompWizardPrefNameNotDefined");
/*     */     }
/* 697 */     return true;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String validateField(String name, String val)
/*     */   {
/* 704 */     IdcMessage msg = validateField(name, val, null);
/* 705 */     return LocaleUtils.encodeMessage(msg);
/*     */   }
/*     */ 
/*     */   public IdcMessage validateField(String name, String val, Map options)
/*     */   {
/* 710 */     IdcMessage errMsg = null;
/*     */ 
/* 712 */     if (name.equals("name"))
/*     */     {
/* 714 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "csCompWizardPrefName", 0, null);
/*     */     }
/* 716 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 721 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.InstallPanel
 * JD-Core Version:    0.5.4
 */