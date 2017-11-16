/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.RevisionSpec;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JComboBox;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class AddDocumentDlg
/*     */   implements ComponentBinder
/*     */ {
/*  81 */   protected DialogHelper m_helper = null;
/*  82 */   protected SystemInterface m_systemInterface = null;
/*     */   protected ExecutionContext m_cxt;
/*  84 */   protected SchemaHelper m_schHelper = null;
/*     */   protected String m_action;
/*  87 */   protected DataBinder m_binder = null;
/*     */   protected String m_helpPage;
/*  90 */   protected boolean m_isUpdate = false;
/*  91 */   protected boolean m_isEditable = false;
/*  92 */   protected boolean m_isContribute = false;
/*     */ 
/*  94 */   protected String[][] m_formatDisplayMap = (String[][])null;
/*  95 */   protected RefreshView m_refresher = null;
/*  96 */   protected DocumentLocalizedProfile m_docProfile = null;
/*     */ 
/*     */   public AddDocumentDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/* 100 */     this.m_helper = new DialogHelper(sys, title, true, true);
/* 101 */     this.m_systemInterface = sys;
/* 102 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/* 103 */     this.m_action = "CHECKIN_BYNAME";
/* 104 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init(Properties data, boolean isUpdate, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*     */   {
/* 110 */     this.m_refresher = refresher;
/* 111 */     this.m_docProfile = docProfile;
/* 112 */     this.m_isEditable = true;
/*     */ 
/* 115 */     String revStr = "1";
/* 116 */     this.m_isContribute = false;
/* 117 */     if (data != null)
/*     */     {
/* 119 */       this.m_helper.m_props = ((Properties)data.clone());
/*     */ 
/* 121 */       String workflowStatus = data.getProperty("dWorkflowState");
/*     */ 
/* 123 */       String revLabel = data.getProperty("dRevLabel");
/* 124 */       if (isUpdate)
/*     */       {
/* 126 */         revStr = revLabel;
/* 127 */         this.m_action = "UPDATE_DOCINFO";
/* 128 */         this.m_isUpdate = true;
/*     */       }
/* 130 */       else if ((workflowStatus.equalsIgnoreCase("E")) || (workflowStatus.equalsIgnoreCase("W")))
/*     */       {
/* 132 */         this.m_isContribute = true;
/* 133 */         this.m_action = "WORKFLOW_CHECKIN";
/* 134 */         revStr = revLabel;
/*     */       }
/*     */       else
/*     */       {
/* 138 */         revStr = RevisionSpec.getNext(revLabel);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 143 */       revStr = RevisionSpec.getFirst();
/*     */     }
/*     */ 
/* 146 */     String author = this.m_helper.m_props.getProperty("dDocAuthor");
/* 147 */     if ((author == null) || (author.trim().length() == 0))
/*     */     {
/* 150 */       this.m_helper.m_props.put("dDocAuthor", AppLauncher.getUser());
/*     */     }
/*     */ 
/* 153 */     if (!this.m_isUpdate)
/*     */     {
/* 156 */       Date dte = new Date();
/* 157 */       String dteStr = LocaleResources.localizeDate(dte, this.m_cxt);
/*     */ 
/* 159 */       this.m_helper.m_props.put("dInDate", dteStr);
/* 160 */       this.m_helper.m_props.remove("dCreateDate");
/* 161 */       this.m_helper.m_props.remove("dOutDate");
/*     */     }
/*     */ 
/* 164 */     this.m_helper.m_props.put("dRevLabel", revStr);
/*     */ 
/* 168 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 175 */           Properties localData = (Properties)AddDocumentDlg.this.m_helper.m_props.clone();
/* 176 */           AddDocumentDlg.this.m_binder = new DataBinder(true);
/*     */ 
/* 179 */           Properties env = AddDocumentDlg.this.m_binder.getEnvironment();
/* 180 */           env.put("doFileCopy", "1");
/* 181 */           AddDocumentDlg.this.m_binder.setLocalData(localData);
/*     */ 
/* 183 */           SharedContext shContext = AddDocumentDlg.this.m_refresher.getSharedContext();
/* 184 */           AppContextUtils.executeService(shContext, AddDocumentDlg.this.m_action, AddDocumentDlg.this.m_binder);
/* 185 */           return true;
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 189 */           String errMsg = "!apCheckinContentError";
/* 190 */           if (AddDocumentDlg.this.m_isUpdate)
/*     */           {
/* 192 */             errMsg = "!apUpdateContentError";
/*     */           }
/* 194 */           AddDocumentDlg.this.reportError(exp, errMsg);
/* 195 */         }return false;
/*     */       }
/*     */     };
/* 201 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 2, this.m_helpPage != null, this.m_helpPage);
/*     */ 
/* 204 */     initUI(data, mainPanel);
/*     */   }
/*     */ 
/*     */   public void initDisplay(Properties data, RefreshView refresher)
/*     */   {
/* 209 */     this.m_refresher = refresher;
/* 210 */     this.m_helper.m_props = data;
/* 211 */     this.m_isEditable = false;
/*     */ 
/* 213 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 214 */     this.m_helper.makePanelGridBag(mainPanel, 2);
/* 215 */     this.m_helper.addOK(null);
/* 216 */     this.m_helper.m_componentBinder = this;
/*     */ 
/* 218 */     initUI(data, mainPanel);
/*     */   }
/*     */ 
/*     */   protected void initUI(Properties data, JPanel mainPanel)
/*     */   {
/* 225 */     ViewFields docFieldsObj = new ViewFields(this.m_cxt);
/* 226 */     DataResultSet drset = new DataResultSet();
/* 227 */     if (this.m_refresher != null)
/*     */     {
/* 229 */       drset = this.m_refresher.getMetaData();
/*     */     }
/*     */ 
/* 232 */     DataBinder binder = new DataBinder();
/* 233 */     binder.setLocalData(this.m_helper.m_props);
/*     */     try
/*     */     {
/* 238 */       String dID = binder.getAllowMissing("dID");
/* 239 */       if (dID == null)
/*     */       {
/* 242 */         SharedContext shContext = this.m_refresher.getSharedContext();
/* 243 */         AppContextUtils.executeService(shContext, "CHECKIN_NEW_FORM", binder);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 249 */       reportError(e, "!apUnableToRetrieveDefaultValues");
/*     */     }
/*     */     finally
/*     */     {
/* 254 */       binder.removeLocal("isDocProfileDone");
/* 255 */       binder.removeLocal("dpEvent");
/*     */     }
/*     */ 
/* 258 */     Vector docFieldsDef = null;
/*     */     try
/*     */     {
/* 261 */       docFieldsDef = docFieldsObj.createDocumentFieldsList(drset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 265 */       reportError(e, "!apUnableToCreateFieldList");
/*     */     }
/*     */ 
/* 269 */     int nfields = docFieldsDef.size();
/*     */     int i;
/* 270 */     int i = 20;
/* 271 */     JPanel curPanel = addNewSubPanel(mainPanel, false);
/* 272 */     boolean doingCustom = false;
/* 273 */     int numDoubles = 0;
/*     */ 
/* 275 */     int lastStandardField = 6;
/* 276 */     int countOffset = 1;
/* 277 */     if (docFieldsObj.m_hasDocAccount)
/*     */     {
/* 279 */       countOffset = 0;
/* 280 */       ++lastStandardField;
/*     */     }
/*     */ 
/* 284 */     int[] fieldMap = { 0, 2, lastStandardField - 1, 1, 3, 4, lastStandardField - 2 };
/*     */ 
/* 286 */     int fieldMapLen = fieldMap.length - countOffset;
/*     */ 
/* 288 */     for (int i = 0; i < nfields; ++i)
/*     */     {
/* 290 */       boolean isBigComp = false;
/*     */ 
/* 292 */       int index = i;
/*     */ 
/* 294 */       if (i < fieldMapLen)
/*     */       {
/* 296 */         index = fieldMap[i];
/*     */       }
/*     */ 
/* 299 */       ViewFieldDef fieldDef = (ViewFieldDef)docFieldsDef.elementAt(index);
/*     */ 
/* 302 */       String fName = fieldDef.m_name;
/* 303 */       if (fName.equals("dPublishType"))
/*     */       {
/* 305 */         ++countOffset;
/*     */       }
/*     */       else
/*     */       {
/* 309 */         if ((!doingCustom) && (fieldDef.m_isCustomMeta))
/*     */         {
/* 311 */           curPanel = addNewSubPanel(mainPanel, true);
/* 312 */           doingCustom = true;
/*     */         }
/*     */ 
/* 315 */         if (i == lastStandardField)
/*     */         {
/* 317 */           ++countOffset;
/* 318 */           curPanel = addNewSubPanel(mainPanel, false);
/*     */ 
/* 320 */           if (!this.m_isUpdate)
/*     */           {
/* 323 */             addFilePathComponents(curPanel);
/*     */ 
/* 325 */             curPanel = addNewSubPanel(mainPanel, false);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 331 */         if (i >= lastStandardField)
/*     */         {
/* 333 */           i = 25;
/*     */         }
/* 335 */         else if (i == 2)
/*     */         {
/* 338 */           i = 4;
/*     */         }
/* 340 */         else if (i == 3)
/*     */         {
/* 343 */           i = 80;
/*     */         }
/*     */         else
/*     */         {
/* 348 */           i = 15;
/*     */         }
/*     */ 
/* 352 */         Component comp = null;
/* 353 */         if (this.m_isEditable)
/*     */         {
/* 355 */           if ((fieldDef.m_name.equals("dDocName")) && (data != null))
/*     */           {
/* 360 */             comp = new CustomLabel();
/*     */           }
/*     */           else
/*     */           {
/* 364 */             comp = determineEditComponent(fieldDef, binder, isBigComp, i);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 369 */           i = 4 * i / 3;
/* 370 */           comp = new CustomText("", i, 0, 0);
/*     */         }
/*     */ 
/* 373 */         boolean isLast = false;
/* 374 */         if (i == 2)
/*     */         {
/* 376 */           isLast = true;
/*     */         }
/* 378 */         else if ((i >= fieldMap.length) || (docFieldsObj.m_hasDocAccount))
/*     */         {
/* 380 */           isLast = (i + countOffset + numDoubles) % 2 == 1;
/*     */         }
/* 382 */         if (fieldDef.m_name.equals("dDocTitle"))
/*     */         {
/* 385 */           isLast = true;
/*     */         }
/*     */ 
/* 388 */         boolean isStrayComp = (i == nfields - 1) && (!isLast);
/* 389 */         JPanel addToPanel = curPanel;
/* 390 */         if ((i == 1) || (i == 5))
/*     */         {
/* 394 */           addToPanel = new PanePanel();
/* 395 */           GridBagConstraints oldgc = this.m_helper.m_gridHelper.m_gc;
/* 396 */           this.m_helper.m_gridHelper.useGridBag(addToPanel);
/* 397 */           this.m_helper.addComponent(curPanel, addToPanel);
/* 398 */           this.m_helper.m_gridHelper.m_gc = oldgc;
/*     */         }
/* 400 */         if (!isBigComp)
/*     */         {
/* 402 */           String caption = LocaleResources.getString("apLabelSubstitution", this.m_cxt, fieldDef.m_caption);
/*     */ 
/* 404 */           this.m_helper.addLabelFieldPairEx(addToPanel, caption, comp, fieldDef.m_name, isLast);
/*     */         }
/*     */ 
/* 407 */         if (((isStrayComp) && (!isBigComp)) || ((isLast) && (isBigComp)))
/*     */         {
/* 409 */           this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 410 */           this.m_helper.addLastComponentInRow(curPanel, new CustomLabel(""));
/* 411 */           this.m_helper.m_gridHelper.m_gc.weightx = 0.0D;
/* 412 */           ++numDoubles;
/*     */         }
/* 414 */         if (isBigComp)
/*     */         {
/* 416 */           this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 417 */           this.m_helper.m_gridHelper.m_gc.fill = 1;
/* 418 */           String caption = LocaleResources.getString(fieldDef.m_caption, this.m_cxt);
/* 419 */           this.m_helper.addLabelFieldPairEx(addToPanel, caption, comp, fieldDef.m_name, true);
/*     */ 
/* 421 */           this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 422 */           this.m_helper.m_gridHelper.m_gc.fill = 2;
/* 423 */           ++numDoubles;
/*     */         }
/*     */ 
/* 428 */         if ((!this.m_isUpdate) || (!fieldDef.m_name.equals("dInDate")))
/*     */           continue;
/* 430 */         String st = data.getProperty("dStatus");
/* 431 */         if ((!st.equals("RELEASED")) && (!st.equals("EXPIRED")))
/*     */           continue;
/* 433 */         comp.setEnabled(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel, boolean isBigComp)
/*     */   {
/* 442 */     CustomPanel panel = new CustomPanel();
/* 443 */     panel.setInsets(10, 5, 10, 5);
/* 444 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 446 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 447 */     int constraints = 2;
/* 448 */     if (isBigComp)
/*     */     {
/* 450 */       this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 451 */       constraints = 1;
/*     */     }
/* 453 */     this.m_helper.m_gridHelper.m_gc.fill = constraints;
/* 454 */     this.m_helper.addComponent(mainPanel, panel);
/* 455 */     this.m_helper.makePanelGridBag(panel, constraints);
/* 456 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/*     */ 
/* 458 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void addFilePathComponents(JPanel mainPanel)
/*     */   {
/* 463 */     if (!this.m_isEditable)
/*     */     {
/* 465 */       CustomLabel originalName = new CustomLabel();
/* 466 */       String caption = LocaleResources.getString("apPrimaryFileCaption", this.m_cxt);
/* 467 */       this.m_helper.addLabelFieldPairEx(mainPanel, caption, originalName, "dOriginalName", false);
/*     */ 
/* 469 */       return;
/*     */     }
/* 471 */     if (this.m_isContribute)
/*     */     {
/* 473 */       this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 474 */       String caption = LocaleResources.getString("apFinishedWorkflowBoxLabel", this.m_cxt);
/* 475 */       JCheckBox isFinished = new JCheckBox(caption);
/* 476 */       this.m_helper.addExchangeComponent(mainPanel, isFinished, "isFinished");
/* 477 */       ItemListener iListener = new ItemListener()
/*     */       {
/*     */         public void itemStateChanged(ItemEvent e)
/*     */         {
/*     */         }
/*     */       };
/* 487 */       isFinished.addItemListener(iListener);
/*     */     }
/*     */ 
/* 491 */     String val = SharedObjects.getEnvironmentValue("IsOverrideFormat");
/* 492 */     if (val == null)
/*     */     {
/* 494 */       val = SharedObjects.getEnvironmentValue("isOverrideFormat");
/*     */     }
/* 496 */     boolean isOverride = StringUtils.convertToBool(val, false);
/*     */ 
/* 498 */     if (isOverride)
/*     */     {
/* 500 */       DataResultSet rset = SharedObjects.getTable("DocFormats");
/* 501 */       String[] keys = { "dFormat", "dDescription" };
/* 502 */       String[][] displayTable = (String[][])null;
/*     */       try
/*     */       {
/* 505 */         displayTable = ResultSetUtils.createStringTable(rset, keys);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 509 */         Report.trace(null, null, e);
/*     */       }
/*     */ 
/* 512 */       if (displayTable != null)
/*     */       {
/* 514 */         this.m_formatDisplayMap = new String[displayTable.length + 1][2];
/* 515 */         for (int i = 0; i < displayTable.length; ++i)
/*     */         {
/* 517 */           this.m_formatDisplayMap[(i + 1)][0] = displayTable[i][0];
/* 518 */           this.m_formatDisplayMap[(i + 1)][1] = this.m_systemInterface.getString(displayTable[i][1]);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 523 */         this.m_formatDisplayMap = new String[1][0];
/*     */       }
/* 525 */       this.m_formatDisplayMap[0][0] = "";
/* 526 */       this.m_formatDisplayMap[0][1] = this.m_systemInterface.getString("apUseDefaultText");
/*     */     }
/*     */ 
/* 529 */     addFilePathComponent(mainPanel, LocaleResources.getString("apPrimaryFileCaption", this.m_cxt), "primaryFile", isOverride, "primaryOverrideFormat");
/*     */ 
/* 531 */     addFilePathComponent(mainPanel, LocaleResources.getString("apAlternateFileCaption", this.m_cxt), "alternateFile", isOverride, "alternateOverrideFormat");
/*     */   }
/*     */ 
/*     */   protected void addFilePathComponent(JPanel panel, String label, String name, boolean isOverride, String overrideName)
/*     */   {
/* 538 */     this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 539 */     this.m_helper.addComponent(panel, new CustomLabel(label, 1));
/*     */ 
/* 541 */     this.m_helper.addFilePathComponent(panel, 50, label, name);
/*     */ 
/* 543 */     if (!isOverride)
/*     */       return;
/* 545 */     DisplayChoice formats = new DisplayChoice();
/* 546 */     formats.init(this.m_formatDisplayMap);
/*     */ 
/* 548 */     this.m_helper.m_gridHelper.prepareAddRowElement(13);
/* 549 */     this.m_helper.addComponent(panel, new CustomLabel(LocaleResources.getString("apFormatCaption", this.m_cxt), 1));
/*     */ 
/* 551 */     int oldfill = this.m_helper.m_gridHelper.m_gc.fill;
/* 552 */     this.m_helper.m_gridHelper.m_gc.fill = 0;
/* 553 */     this.m_helper.m_gridHelper.prepareAddRowElement(17, 0);
/* 554 */     this.m_helper.addExchangeComponent(panel, formats, overrideName);
/* 555 */     this.m_helper.m_gridHelper.m_gc.fill = oldfill;
/*     */   }
/*     */ 
/*     */   protected Component determineEditComponent(ViewFieldDef fieldDef, DataBinder binder, boolean isBigComp, int minCols)
/*     */   {
/* 562 */     Component comp = null;
/* 563 */     if (fieldDef.isMandatoryOptionList())
/*     */     {
/* 565 */       if ((fieldDef.m_optionListKey.equals("securityGroups")) && (this.m_isContribute))
/*     */       {
/* 567 */         comp = new CustomLabel();
/*     */       }
/*     */       else
/*     */       {
/* 571 */         JComboBox choiceList = new CustomChoice();
/* 572 */         comp = choiceList;
/* 573 */         Vector options = this.m_docProfile.getOptionList(fieldDef.m_optionListKey, true);
/* 574 */         if (options == null)
/*     */         {
/* 576 */           options = SharedObjects.getOptList(fieldDef.m_optionListKey);
/*     */         }
/* 578 */         if (options != null)
/*     */         {
/* 580 */           String name = fieldDef.m_name;
/* 581 */           if ((!name.equals("dDocType")) && (!name.equals("dSecurityGroup")))
/*     */           {
/* 584 */             choiceList.addItem("");
/*     */           }
/* 586 */           int num = options.size();
/* 587 */           for (int j = 0; j < num; ++j)
/*     */           {
/* 589 */             choiceList.addItem(options.elementAt(j));
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 594 */     else if (fieldDef.isComplexOptionList())
/*     */     {
/* 597 */       ViewChoice vChoice = new ViewChoice(this.m_systemInterface, this.m_refresher.getSharedContext());
/* 598 */       comp = vChoice;
/* 599 */       if (this.m_schHelper == null)
/*     */       {
/* 602 */         this.m_schHelper = new SchemaHelper();
/* 603 */         this.m_schHelper.computeMaps();
/*     */       }
/* 605 */       String btnLabel = this.m_systemInterface.getString("apSelectBtnLabel");
/* 606 */       vChoice.init(this.m_schHelper, fieldDef, minCols, btnLabel);
/*     */     }
/* 609 */     else if ((fieldDef.isMultiOptionList()) || (fieldDef.isComboOptionList()) || (fieldDef.isUnvalidatedList()))
/*     */     {
/* 612 */       ComboChoice choiceList = new ComboChoice(minCols, fieldDef.isMultiOptionList());
/* 613 */       comp = choiceList;
/* 614 */       Vector options = this.m_docProfile.getOptionList(fieldDef.m_optionListKey, true);
/* 615 */       if (options == null)
/*     */       {
/* 617 */         options = SharedObjects.getOptList(fieldDef.m_optionListKey);
/*     */       }
/* 619 */       if (options == null)
/*     */       {
/* 621 */         options = binder.getOptionList(fieldDef.m_optionListKey);
/*     */       }
/*     */ 
/* 624 */       if (options != null)
/*     */       {
/* 626 */         choiceList.initChoiceList(options);
/*     */       }
/*     */ 
/*     */     }
/* 631 */     else if (fieldDef.m_type.equalsIgnoreCase("memo"))
/*     */     {
/* 633 */       comp = new CustomTextArea(4, minCols);
/* 634 */       isBigComp = true;
/*     */     }
/*     */     else
/*     */     {
/* 638 */       comp = new CustomTextField(minCols);
/*     */     }
/*     */ 
/* 641 */     return comp;
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e, String msg)
/*     */   {
/* 646 */     MessageBox.reportError(this.m_systemInterface, e, msg);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 651 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getDocID()
/*     */   {
/* 656 */     String id = "";
/* 657 */     if (this.m_binder != null)
/*     */     {
/*     */       try
/*     */       {
/* 661 */         id = this.m_binder.get("dID");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 665 */         if (SystemUtils.m_verbose)
/*     */         {
/* 667 */           Report.debug(null, null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 672 */     return id;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 683 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 684 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 690 */     if (!this.m_isEditable)
/*     */     {
/* 692 */       return true;
/*     */     }
/* 694 */     String name = exchange.m_compName;
/* 695 */     String val = exchange.m_compValue;
/*     */ 
/* 697 */     return validateField(name, val, exchange);
/*     */   }
/*     */ 
/*     */   public boolean validateField(String name, String val, DynamicComponentExchange exchange)
/*     */   {
/* 704 */     if (name.equals("dDocTitle"))
/*     */     {
/* 706 */       if ((val == null) || (val.length() == 0))
/*     */       {
/* 708 */         exchange.m_errorMessage = IdcMessageFactory.lc("apSpecifyTitle", new Object[0]);
/* 709 */         return false;
/*     */       }
/*     */     }
/* 712 */     else if ((name.equals("primaryFile")) && (!this.m_isUpdate))
/*     */     {
/* 714 */       int valResult = Validation.checkUrlFileSegment(val);
/* 715 */       IdcMessage errMsg = null;
/* 716 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 719 */         break;
/*     */       case -1:
/* 721 */         errMsg = IdcMessageFactory.lc("apPrimaryFileNameMissing", new Object[0]);
/* 722 */         break;
/*     */       case -2:
/* 724 */         errMsg = IdcMessageFactory.lc("apPrimaryFileNameSpaces", new Object[0]);
/*     */       }
/*     */ 
/* 727 */       if (errMsg != null)
/*     */       {
/* 729 */         exchange.m_errorMessage = errMsg;
/* 730 */         return false;
/*     */       }
/*     */ 
/* 734 */       valResult = FileUtils.checkFile(val, true, false);
/* 735 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 738 */         break;
/*     */       case -16:
/* 740 */         errMsg = IdcMessageFactory.lc("apPrimaryFileNameMissing", new Object[0]);
/* 741 */         break;
/*     */       case -18:
/* 743 */         errMsg = IdcMessageFactory.lc("apPrimaryFileNameSpaces", new Object[0]);
/* 744 */         break;
/*     */       case -24:
/* 746 */         errMsg = IdcMessageFactory.lc("apPrimaryFileNotFile", new Object[0]);
/* 747 */         break;
/*     */       default:
/* 749 */         errMsg = IdcMessageFactory.lc("apPrimaryFileUnknownError", new Object[0]);
/*     */       }
/*     */ 
/* 752 */       if (errMsg != null)
/*     */       {
/* 754 */         exchange.m_errorMessage = errMsg;
/* 755 */         return false;
/*     */       }
/*     */ 
/* 759 */       this.m_helper.m_props.remove("dOriginalName");
/* 760 */       for (int i = 0; i < AdditionalRenditions.m_maxNum; ++i)
/*     */       {
/* 762 */         String key = "dRendition" + (i + 1);
/* 763 */         this.m_helper.m_props.remove(key);
/*     */       }
/* 765 */       this.m_helper.m_props.remove("dStatus");
/* 766 */       this.m_helper.m_props.remove("dReleaseState");
/*     */     }
/* 768 */     else if ((name.equals("alternateFile")) && (!this.m_isUpdate))
/*     */     {
/* 770 */       if (val == null)
/*     */       {
/* 772 */         return true;
/*     */       }
/* 774 */       int valResult = Validation.checkUrlFileSegment(val);
/* 775 */       IdcMessage errMsg = null;
/* 776 */       switch (valResult)
/*     */       {
/*     */       case 0:
/* 779 */         break;
/*     */       case -2:
/* 781 */         errMsg = IdcMessageFactory.lc("apAlternateFileNameSpaces", new Object[0]);
/*     */       }
/*     */ 
/* 784 */       if (errMsg != null)
/*     */       {
/* 786 */         exchange.m_errorMessage = errMsg;
/* 787 */         return false;
/*     */       }
/*     */     }
/* 790 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 795 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.AddDocumentDlg
 * JD-Core Version:    0.5.4
 */