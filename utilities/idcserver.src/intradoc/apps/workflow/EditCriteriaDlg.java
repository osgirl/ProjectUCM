/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DisplayLabel;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.ViewChoice;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditCriteriaDlg extends EditWorkflowDlg
/*     */   implements ActionListener
/*     */ {
/*  77 */   protected Vector m_fieldDefs = null;
/*  78 */   protected Hashtable m_displayMaps = null;
/*  79 */   protected SchemaHelper m_schHelper = null;
/*     */ 
/*  81 */   protected JCheckBox m_criteriaBox = null;
/*  82 */   protected DisplayChoice m_fieldChoice = null;
/*  83 */   protected CustomTextField m_valueField = null;
/*  84 */   protected JButton m_selButton = null;
/*  85 */   protected Component m_valueComp = null;
/*  86 */   protected JCheckBox m_cllboraBox = null;
/*     */ 
/*  88 */   protected PanePanel m_valuePanel = null;
/*  89 */   protected String m_currentOptKey = null;
/*     */ 
/*     */   public EditCriteriaDlg(SystemInterface sys, String title, WorkflowContext ctxt, ResultSet rset, String helpPage)
/*     */   {
/*  94 */     super(sys, title, ctxt, rset, helpPage);
/*  95 */     this.TYPES = new String[2];
/*  96 */     this.TYPES[0] = "Criteria";
/*  97 */     this.TYPES[1] = "SubWorkflow";
/*     */   }
/*     */ 
/*     */   public boolean init(Properties data)
/*     */   {
/* 103 */     String name = null;
/* 104 */     if (data != null)
/*     */     {
/* 106 */       this.m_action = "EDIT_WORKFLOWCRITERIA";
/* 107 */       this.m_helper.m_props = data;
/* 108 */       this.m_isNew = false;
/* 109 */       name = data.getProperty("dWfName");
/*     */     }
/*     */     else
/*     */     {
/* 113 */       this.m_action = "ADD_WORKFLOW";
/*     */     }
/*     */ 
/* 116 */     if (!createGroupChoice(this.m_isNew))
/*     */     {
/* 118 */       return false;
/*     */     }
/*     */ 
/* 121 */     setDisplayMaps();
/*     */ 
/* 123 */     JPanel mainPanel = initUI(name, this.m_isNew);
/* 124 */     initProjectUI(mainPanel, this.m_isNew);
/*     */ 
/* 126 */     if (this.m_isNew)
/*     */     {
/* 128 */       initTemplateChoice(mainPanel);
/*     */ 
/* 131 */       this.m_helper.m_props.put("dWfType", this.TYPES[0]);
/*     */     }
/*     */ 
/* 134 */     initCriteriaUI(mainPanel);
/* 135 */     load();
/* 136 */     return true;
/*     */   }
/*     */ 
/*     */   protected void load()
/*     */   {
/* 141 */     String type = this.m_helper.m_props.getProperty("dWfType");
/* 142 */     boolean hasCriteria = false;
/* 143 */     if ((type == null) || (type.equals("Criteria")))
/*     */     {
/* 145 */       hasCriteria = true;
/* 146 */       this.m_helper.m_props.put("HasCriteria", "1");
/*     */     }
/* 148 */     enableDisableCriteria(hasCriteria);
/*     */   }
/*     */ 
/*     */   protected void setDisplayMaps()
/*     */   {
/* 154 */     ResultSet metaFields = SharedObjects.getTable("DocMetaDefinition");
/* 155 */     ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/*     */     try
/*     */     {
/* 159 */       ViewFieldDef tempDef = null;
/*     */ 
/* 161 */       tempDef = docFieldsObj.addViewFieldDef("dDocName", LocaleResources.getString("apTitleDocName", this.m_cxt));
/*     */ 
/* 163 */       tempDef.m_hasView = true;
/* 164 */       tempDef.m_optionListKey = "documents";
/*     */ 
/* 166 */       tempDef = docFieldsObj.addViewFieldDef("dDocAuthor", LocaleResources.getString("apTitleDocAuthor", this.m_cxt));
/*     */ 
/* 168 */       tempDef.m_hasView = true;
/* 169 */       tempDef.m_optionListKey = "users";
/*     */ 
/* 171 */       tempDef = docFieldsObj.addViewFieldDef("dDocType", LocaleResources.getString("apTitleDocType", this.m_cxt));
/*     */ 
/* 173 */       tempDef.m_isOptionList = true;
/* 174 */       tempDef.m_optionListKey = "docTypes";
/*     */ 
/* 176 */       tempDef = docFieldsObj.addViewFieldDef("dDocClass", LocaleResources.getString("apTitleDocClass", this.m_cxt));
/*     */ 
/* 179 */       docFieldsObj.addSpecialExtensionFields();
/*     */ 
/* 181 */       docFieldsObj.addMetaFieldsEx(metaFields, false, false);
/*     */ 
/* 183 */       this.m_fieldDefs = docFieldsObj.m_viewFields;
/* 184 */       this.m_displayMaps = docFieldsObj.getDisplayMaps();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 188 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apErrorCreatingDisplayFieldList", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initProjectUI(JPanel mainPanel, boolean isNew)
/*     */   {
/* 195 */     String status = this.m_helper.m_props.getProperty("dWfStatus");
/* 196 */     String projectID = this.m_helper.m_props.getProperty("dProjectID");
/* 197 */     if ((isNew) || ((status != null) && (status.equals("INIT"))))
/*     */     {
/* 199 */       String[][] display = this.m_context.buildProjectMap();
/* 200 */       if (display == null)
/*     */       {
/* 203 */         return;
/*     */       }
/*     */ 
/* 206 */       JCheckBox prjBox = new CustomCheckbox(LocaleResources.getString("apTitleUseProject", this.m_cxt));
/*     */ 
/* 208 */       DisplayChoice prjChoice = new DisplayChoice();
/*     */ 
/* 210 */       prjChoice.init(display);
/* 211 */       ItemListener iListener = new ItemListener(prjChoice, prjBox)
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/* 215 */           this.val$prjChoice.setEnabled(this.val$prjBox.isSelected());
/*     */         }
/*     */       };
/* 218 */       prjBox.addItemListener(iListener);
/*     */ 
/* 221 */       if ((projectID != null) && (projectID.length() > 0))
/*     */       {
/* 223 */         this.m_helper.m_props.put("UseProject", "1");
/*     */       }
/*     */       else
/*     */       {
/* 227 */         prjChoice.setEnabled(false);
/*     */       }
/*     */ 
/* 230 */       this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 231 */       this.m_helper.addExchangeComponent(mainPanel, prjBox, "UseProject");
/* 232 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 233 */       this.m_helper.addExchangeComponent(mainPanel, prjChoice, "dProjectID");
/*     */     }
/*     */     else
/*     */     {
/* 237 */       if ((projectID == null) || (projectID.length() == 0))
/*     */       {
/* 240 */         return;
/*     */       }
/*     */ 
/* 243 */       DataResultSet drset = SharedObjects.getTable("RegisteredProjects");
/* 244 */       String desc = null;
/*     */       try
/*     */       {
/* 247 */         if (drset != null)
/*     */         {
/* 249 */           desc = ResultSetUtils.findValue(drset, "dProjectID", projectID, "dPrjDescription");
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 254 */         String error = LocaleUtils.encodeMessage("apUnableToSearchForProject", e.getMessage());
/* 255 */         Report.trace(null, LocaleResources.getString(error, this.m_cxt), e);
/*     */       }
/*     */ 
/* 258 */       if (desc == null)
/*     */       {
/* 260 */         desc = projectID;
/*     */       }
/* 262 */       this.m_helper.m_props.put("dPrjDescription", desc);
/*     */ 
/* 264 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 265 */       this.m_helper.addLabelDisplayPair(mainPanel, LocaleResources.getString("apLabelUseProject", this.m_cxt), 30, "dPrjDescription");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initCriteriaUI(JPanel mainPanel)
/*     */   {
/* 272 */     JPanel pnl = new CustomPanel();
/* 273 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 274 */     this.m_helper.addComponent(mainPanel, pnl);
/*     */ 
/* 276 */     this.m_helper.makePanelGridBag(pnl, 2);
/* 277 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(0, 0, 0, 0);
/*     */ 
/* 279 */     this.m_criteriaBox = new CustomCheckbox(LocaleResources.getString("apTitleHasCriteriaDefinition", this.m_cxt), 1);
/*     */ 
/* 281 */     ItemListener iListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 285 */         EditCriteriaDlg.this.enableDisableCriteria();
/*     */       }
/*     */     };
/* 288 */     this.m_criteriaBox.addItemListener(iListener);
/* 289 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 290 */     this.m_helper.addExchangeComponent(pnl, this.m_criteriaBox, "HasCriteria");
/*     */ 
/* 292 */     this.m_fieldChoice = createFieldList();
/* 293 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelField", this.m_cxt), this.m_fieldChoice, "dWfCriteriaName");
/*     */ 
/* 296 */     DisplayLabel lbl = new DisplayLabel(CriteriaPanel.OPERATORS);
/* 297 */     lbl.setAlignment(0);
/* 298 */     this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelOperator", this.m_cxt), lbl, "dWfCriteriaOperator");
/*     */ 
/* 301 */     this.m_valuePanel = new PanePanel();
/* 302 */     this.m_valuePanel.setLayout(new GridBagLayout());
/*     */ 
/* 304 */     this.m_helper.addLabelFieldPairEx(pnl, LocaleResources.getString("apLabelValue", this.m_cxt), this.m_valuePanel, "valuePanel", false);
/*     */ 
/* 307 */     checkSelectedField(this.m_helper.m_props.getProperty("dWfCriteriaName"));
/*     */ 
/* 310 */     boolean isCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/* 311 */     if (isCollaboration)
/*     */     {
/* 313 */       this.m_cllboraBox = new CustomCheckbox(LocaleResources.getString("apTitleEnable", this.m_cxt));
/* 314 */       this.m_helper.m_gridHelper.addEmptyRow(pnl);
/* 315 */       this.m_helper.addLabelFieldPair(pnl, LocaleResources.getString("apLabelIsCollaboration", this.m_cxt), this.m_cllboraBox, "dIsCollaboration");
/*     */     }
/*     */ 
/* 319 */     ItemListener fieldListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 323 */         int state = e.getStateChange();
/* 324 */         if (state != 1)
/*     */           return;
/* 326 */         EditCriteriaDlg.this.checkSelectedField();
/*     */       }
/*     */     };
/* 330 */     this.m_fieldChoice.addItemListener(fieldListener);
/*     */   }
/*     */ 
/*     */   public DisplayChoice createFieldList()
/*     */   {
/* 335 */     String[][] nameCaptions = new String[this.m_fieldDefs.size()][2];
/* 336 */     for (int i = 0; i < nameCaptions.length; ++i)
/*     */     {
/* 338 */       FieldDef fieldDef = (FieldDef)this.m_fieldDefs.elementAt(i);
/* 339 */       nameCaptions[i][0] = fieldDef.m_name;
/* 340 */       nameCaptions[i][1] = fieldDef.m_caption;
/*     */     }
/*     */ 
/* 343 */     DisplayChoice choice = new DisplayChoice();
/* 344 */     choice.init(nameCaptions);
/* 345 */     return choice;
/*     */   }
/*     */ 
/*     */   protected void checkSelectedField()
/*     */   {
/* 350 */     String selItem = this.m_fieldChoice.getSelectedInternalValue();
/* 351 */     if (selItem == null)
/*     */     {
/* 353 */       return;
/*     */     }
/*     */ 
/* 356 */     checkSelectedField(selItem);
/*     */   }
/*     */ 
/*     */   protected void checkSelectedField(String selItem)
/*     */   {
/* 361 */     ViewFieldDef fieldDef = null;
/* 362 */     for (int i = 0; i < this.m_fieldDefs.size(); ++i)
/*     */     {
/* 364 */       fieldDef = (ViewFieldDef)this.m_fieldDefs.elementAt(i);
/*     */ 
/* 366 */       if ((selItem != null) && (selItem.length() != 0) && (!fieldDef.m_name.equals(selItem)))
/*     */         continue;
/* 368 */       changeValueView(fieldDef);
/* 369 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void changeValueView(ViewFieldDef fieldDef)
/*     */   {
/* 376 */     this.m_currentOptKey = fieldDef.m_optionListKey;
/* 377 */     boolean hasView = fieldDef.m_hasView;
/*     */ 
/* 379 */     this.m_valuePanel.removeAll();
/* 380 */     this.m_helper.m_exchange.removeComponent("dWfCriteriaValue");
/*     */ 
/* 382 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 383 */     gh.m_gc.fill = 2;
/* 384 */     gh.m_gc.weightx = 1.0D;
/*     */ 
/* 386 */     if (hasView)
/*     */     {
/* 389 */       gh.m_gc.gridwidth = -1;
/* 390 */       this.m_helper.addExchangeComponent(this.m_valuePanel, this.m_valueField = new CustomTextField(10), "dWfCriteriaValue");
/*     */ 
/* 392 */       this.m_selButton = new JButton(LocaleResources.getString("apSelectBtnLabel", this.m_cxt));
/* 393 */       gh.m_gc.weightx = 0.0D;
/* 394 */       this.m_helper.addLastComponentInRow(this.m_valuePanel, this.m_selButton);
/* 395 */       this.m_selButton.addActionListener(this);
/*     */     }
/* 397 */     else if (fieldDef.isComplexOptionList())
/*     */     {
/* 399 */       if (this.m_schHelper == null)
/*     */       {
/* 401 */         this.m_schHelper = new SchemaHelper();
/* 402 */         this.m_schHelper.computeMaps();
/*     */       }
/*     */ 
/* 405 */       ViewChoice vChoice = new ViewChoice(this.m_systemInterface, this.m_context.getSharedContext());
/* 406 */       vChoice.setEnableMultiSelect(false);
/* 407 */       vChoice.init(this.m_schHelper, fieldDef, 10, this.m_systemInterface.getString("apSelectBtnLabel"));
/* 408 */       this.m_helper.addExchangeComponent(this.m_valuePanel, vChoice, "dWfCriteriaValue");
/*     */ 
/* 410 */       this.m_valueField = ((CustomTextField)vChoice.getComponent());
/* 411 */       this.m_selButton = vChoice.getBrowseButton();
/*     */     }
/*     */     else
/*     */     {
/* 416 */       ComboChoice valueChoice = getOptChoiceList(this.m_currentOptKey);
/*     */ 
/* 418 */       this.m_valueComp = valueChoice;
/* 419 */       if (valueChoice == null)
/*     */       {
/* 421 */         JTextField cl = new JTextField();
/* 422 */         this.m_valueComp = cl;
/*     */       }
/* 424 */       this.m_helper.addExchangeComponent(this.m_valuePanel, this.m_valueComp, "dWfCriteriaValue");
/*     */     }
/*     */ 
/* 427 */     this.m_valuePanel.updateUI();
/*     */   }
/*     */ 
/*     */   protected ComboChoice getOptChoiceList(String optKey)
/*     */   {
/* 432 */     if ((optKey == null) || (optKey.length() == 0))
/*     */     {
/* 434 */       return null;
/*     */     }
/*     */ 
/* 437 */     Vector optList = SharedObjects.getOptList(optKey);
/* 438 */     if (optList == null)
/*     */     {
/* 440 */       return null;
/*     */     }
/*     */ 
/* 443 */     ComboChoice choice = new ComboChoice();
/* 444 */     choice.initChoiceList(optList);
/* 445 */     return choice;
/*     */   }
/*     */ 
/*     */   protected void enableDisableCriteria()
/*     */   {
/* 450 */     boolean enabled = this.m_criteriaBox.isSelected();
/*     */ 
/* 452 */     int index = (enabled) ? 0 : 1;
/* 453 */     this.m_helper.m_props.put("dWfType", this.TYPES[index]);
/*     */ 
/* 455 */     enableDisableCriteria(enabled);
/*     */   }
/*     */ 
/*     */   protected void enableDisableCriteria(boolean enable)
/*     */   {
/* 460 */     this.m_fieldChoice.setEnabled(enable);
/* 461 */     if (this.m_valueField != null)
/*     */     {
/* 463 */       this.m_valueField.setEnabled(enable);
/*     */     }
/* 465 */     if (this.m_selButton != null)
/*     */     {
/* 467 */       this.m_selButton.setEnabled(enable);
/*     */     }
/* 469 */     if (this.m_valueComp != null)
/*     */     {
/* 471 */       this.m_valueComp.setEnabled(enable);
/*     */     }
/* 473 */     if (this.m_cllboraBox == null)
/*     */       return;
/* 475 */     this.m_cllboraBox.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 486 */     String name = exchange.m_compName;
/* 487 */     String val = exchange.m_compValue;
/*     */ 
/* 489 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 491 */     IdcMessage errMsg = null;
/* 492 */     if (name.equals("dWfCriteriaValue"))
/*     */     {
/* 494 */       String type = this.m_helper.m_props.getProperty("dWfType");
/* 495 */       if (type.equals("Criteria"))
/*     */       {
/* 497 */         if ((val == null) || (val.trim().length() == 0))
/*     */         {
/* 499 */           errMsg = IdcMessageFactory.lc("apSpecifyCriteriaValue", new Object[0]);
/*     */         }
/* 501 */         else if (val.length() > maxLength)
/*     */         {
/* 503 */           errMsg = IdcMessageFactory.lc("apCriteriaValueExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 509 */       return super.validateComponentValue(exchange);
/*     */     }
/*     */ 
/* 512 */     if (errMsg != null)
/*     */     {
/* 514 */       exchange.m_errorMessage = errMsg;
/* 515 */       return false;
/*     */     }
/* 517 */     return true;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 526 */     String[][] viewDefinition = { { "documents", "docView", "" }, { "accounts", "docView", "dDocName,dDocAccount,dRevisionLabel" }, { "users", "userView", "" } };
/*     */ 
/* 533 */     for (int i = 0; i < viewDefinition.length; ++i)
/*     */     {
/* 535 */       if (!this.m_currentOptKey.equals(viewDefinition[i][0])) {
/*     */         continue;
/*     */       }
/* 538 */       String viewType = viewDefinition[i][1];
/*     */ 
/* 540 */       ViewData viewData = null;
/* 541 */       String key = null;
/* 542 */       String title = null;
/* 543 */       String helpPage = null;
/* 544 */       if (viewType.equals("docView"))
/*     */       {
/* 546 */         viewData = new ViewData(1);
/* 547 */         viewData.m_viewName = "DocSelectView";
/* 548 */         key = "dDocName";
/* 549 */         title = LocaleResources.getString("apSelectContentItem", this.m_cxt);
/* 550 */         helpPage = "SelectDocument";
/*     */       }
/* 552 */       else if (viewType.equals("userView"))
/*     */       {
/* 554 */         viewData = new ViewData(2);
/* 555 */         viewData.m_viewName = "UserSelectView";
/* 556 */         key = "dName";
/* 557 */         title = LocaleResources.getString("apTitleSelectUser", this.m_cxt);
/* 558 */         helpPage = "SelectUser";
/*     */       }
/*     */ 
/* 561 */       viewData.m_isViewOnly = false;
/* 562 */       ViewDlg viewDlg = new ViewDlg(this.m_helper.m_dialog, this.m_systemInterface, title, this.m_context.getSharedContext(), DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 565 */       viewDlg.init(viewData, null);
/* 566 */       if (viewDlg.prompt() != 1)
/*     */         continue;
/* 568 */       Vector v = viewDlg.computeSelectedValues(key, false);
/* 569 */       if (v.size() <= 0)
/*     */         continue;
/* 571 */       String value = (String)v.elementAt(0);
/* 572 */       this.m_valueField.setText(value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 581 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97211 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.EditCriteriaDlg
 * JD-Core Version:    0.5.4
 */