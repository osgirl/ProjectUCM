/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayChoice;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Graphics;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocProfilePanel extends DocConfigPanel
/*     */   implements ItemListener, ActionListener, DisplayStringCallback
/*     */ {
/*  70 */   protected UdlPanel m_profileList = null;
/*  71 */   protected String m_triggerField = null;
/*  72 */   protected String m_relUrl = null;
/*     */   protected DisplayChoice m_docClass;
/*     */   protected JButton m_deleteBtn;
/*     */   protected String m_defProfile;
/*  76 */   protected boolean m_isClassListChanged = false;
/*     */ 
/*     */   public DocProfilePanel()
/*     */   {
/*  80 */     this.m_subject = "docprofiles";
/*     */   }
/*     */ 
/*     */   public void initEx(SystemInterface sys, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  86 */     if (binder == null)
/*     */     {
/*  88 */       binder = new DataBinder();
/*     */     }
/*  90 */     super.initEx(sys, binder);
/*     */ 
/*  92 */     initUI();
/*     */ 
/*  95 */     refreshView();
/*  96 */     this.m_ctx.setCachedObject("profilesPanel", this);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 101 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 103 */     JPanel triggerPnl = createTriggerUI();
/* 104 */     JPanel docClassPnl = createDocClassChoice();
/* 105 */     this.m_profileList = createList();
/*     */ 
/* 108 */     JPanel pnl = new PanePanel();
/* 109 */     gh.useGridBag(pnl);
/* 110 */     gh.m_gc.fill = 0;
/* 111 */     gh.m_gc.weighty = 0.0D;
/* 112 */     addButtons(pnl);
/*     */ 
/* 115 */     gh.useGridBag(this);
/* 116 */     gh.m_gc.weightx = 1.0D;
/* 117 */     gh.m_gc.weighty = 0.0D;
/* 118 */     gh.m_gc.fill = 2;
/* 119 */     this.m_helper.addLastComponentInRow(this, triggerPnl);
/* 120 */     this.m_helper.addLastComponentInRow(this, docClassPnl);
/* 121 */     gh.m_gc.weightx = 1.0D;
/* 122 */     gh.m_gc.weighty = 1.0D;
/* 123 */     gh.m_gc.fill = 1;
/* 124 */     this.m_helper.addLastComponentInRow(this, this.m_profileList);
/* 125 */     gh.m_gc.weightx = 0.0D;
/* 126 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */   }
/*     */ 
/*     */   protected JPanel createTriggerUI()
/*     */   {
/* 131 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 132 */     PanePanel pnl = new PanePanel();
/* 133 */     gh.useGridBag(pnl);
/* 134 */     gh.m_gc.fill = 2;
/* 135 */     gh.m_gc.anchor = 17;
/*     */ 
/* 137 */     String label = this.m_systemInterface.localizeCaption("apDpTriggerFieldLabel");
/* 138 */     this.m_helper.addLabelDisplayPairEx(pnl, label, 30, "dpTriggerField", false);
/*     */ 
/* 140 */     PanePanel btnPanel = new PanePanel();
/* 141 */     label = this.m_systemInterface.getString("apLabelSelectButton");
/* 142 */     JButton btn = new JButton(label);
/* 143 */     btn.setActionCommand("selectTrigger");
/* 144 */     btn.addActionListener(this);
/*     */ 
/* 146 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 147 */     gh.m_gc.weightx = 1.0D;
/* 148 */     gh.m_gc.fill = 2;
/* 149 */     gh.m_gc.anchor = 17;
/* 150 */     this.m_helper.addComponent(btnPanel, new PanePanel());
/*     */ 
/* 152 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 153 */     gh.m_gc.weightx = 0.0D;
/* 154 */     gh.m_gc.fill = 0;
/* 155 */     this.m_helper.addComponent(btnPanel, btn);
/*     */ 
/* 157 */     gh.m_gc.fill = 2;
/* 158 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */ 
/* 160 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected JPanel createDocClassChoice()
/*     */   {
/* 165 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 166 */     PanePanel pnl = new PanePanel();
/* 167 */     gh.useGridBag(pnl);
/* 168 */     gh.m_gc.fill = 2;
/* 169 */     gh.m_gc.anchor = 17;
/*     */ 
/* 171 */     this.m_docClass = new DisplayChoice();
/* 172 */     initDocClassList();
/* 173 */     this.m_docClass.select("Base");
/* 174 */     this.m_docClass.addItemListener(this);
/* 175 */     this.m_helper.addLabelFieldPair(pnl, this.m_systemInterface.localizeCaption("apDpDocClassLabel"), this.m_docClass, "dDocClass");
/*     */ 
/* 178 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void initDocClassList()
/*     */   {
/* 183 */     if (this.m_docClass == null)
/*     */     {
/* 185 */       return;
/*     */     }
/*     */ 
/* 190 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 193 */       AppLauncher.executeService("GET_DOCCLASSES", binder);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */     }
/*     */ 
/* 199 */     DataResultSet rset = (DataResultSet)binder.getResultSet("DocClassesInfo");
/*     */     String[][] docClassList;
/*     */     String[][] docClassList;
/* 200 */     if ((rset == null) || (rset.isEmpty()))
/*     */     {
/* 202 */       docClassList = new String[][] { { "Base", "Base" } };
/*     */     }
/*     */     else
/*     */     {
/* 206 */       int colIndex = rset.getFieldInfoIndex("dDocClass");
/*     */       String[][] docClassList;
/* 207 */       if (null != rset.findRow(colIndex, "Base", 0, 2))
/*     */       {
/* 209 */         docClassList = new String[rset.getNumRows()][2];
/*     */       }
/*     */       else
/*     */       {
/* 214 */         docClassList = new String[rset.getNumRows() + 1][2];
/*     */       }
/*     */ 
/* 217 */       docClassList[0] = { "Base", "Base" };
/* 218 */       int i = 1;
/* 219 */       for (rset.first(); rset.isRowPresent(); ++i)
/*     */       {
/* 221 */         String choice = rset.getStringValue(colIndex);
/* 222 */         if (choice.equalsIgnoreCase("Base"))
/*     */         {
/* 224 */           --i;
/*     */         }
/*     */         else
/* 227 */           docClassList[i] = { choice, choice };
/* 219 */         rset.next();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 231 */     this.m_docClass.init(docClassList);
/*     */   }
/*     */ 
/*     */   protected UdlPanel createList()
/*     */   {
/* 236 */     String columns = "dpName,dpDisplayLabel,dpDescription,dpTriggerValue";
/* 237 */     UdlPanel list = new UdlPanel(this.m_systemInterface.getString("apDpProfileListLabel"), null, 500, 20, "DocumentProfiles", true);
/*     */ 
/* 241 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDpNameColumn"), "dpName", 8.0D);
/* 242 */     list.setColumnInfo(info);
/* 243 */     info = new ColumnInfo(this.m_systemInterface.getString("apDpLabelColumn"), "dpDisplayLabel", 8.0D);
/* 244 */     list.setColumnInfo(info);
/* 245 */     info = new ColumnInfo(this.m_systemInterface.getString("apDpDescriptionColumn"), "dpDescription", 14.0D);
/*     */ 
/* 247 */     list.setColumnInfo(info);
/* 248 */     info = new ColumnInfo(this.m_systemInterface.getString("apDpTiggerColumn"), "dpTriggerValue", 10.0D);
/*     */ 
/* 250 */     list.setColumnInfo(info);
/*     */ 
/* 252 */     list.setVisibleColumns(columns);
/* 253 */     list.setIDColumn("dpName");
/* 254 */     list.setStateColumn("isValid");
/* 255 */     list.useDefaultListener();
/* 256 */     list.m_list.addActionListener(this);
/* 257 */     list.m_list.addItemListener(this);
/*     */ 
/* 261 */     DisplayStringCallback dsc = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 267 */         return DocProfilePanel.this.m_systemInterface.getString(value);
/*     */       }
/*     */     };
/* 271 */     list.setDisplayCallback("dpDisplayLabel", dsc);
/*     */ 
/* 273 */     list.init();
/* 274 */     list.setDisplayCallback("dpName", this);
/* 275 */     return list;
/*     */   }
/*     */ 
/*     */   protected void addButtons(JPanel pnl)
/*     */   {
/* 281 */     String[][] btnInfo = { { "add", "apDpDlgButtonAddProfile", "0", "apDpAddProfileTitle1" }, { "edit", "apDpDlgButtonEditProfile", "1", "apReadableButtonEditProfile" }, { "delete", "apDpDlgButtonDeleteProfile", "1", "apReadableButtonDeleteProfile" }, { "space", "", "0", "" }, { "preview", "apDpDlgPreviewProfile", "1", "apDpPreviewProfile" } };
/*     */ 
/* 290 */     JPanel btnPanel = new PanePanel();
/* 291 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 293 */       String cmd = btnInfo[i][0];
/* 294 */       if (cmd.equals("space"))
/*     */       {
/* 297 */         btnPanel.add(new PanePanel());
/*     */       }
/*     */       else {
/* 300 */         boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/* 301 */         JButton btn = this.m_profileList.addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), isControlled);
/*     */ 
/* 303 */         if (btnInfo[i][0].equals("delete"))
/*     */         {
/* 305 */           this.m_deleteBtn = btn;
/*     */         }
/* 307 */         btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 308 */         btn.setActionCommand(cmd);
/* 309 */         btn.addActionListener(this);
/* 310 */         btnPanel.add(btn);
/*     */       }
/*     */     }
/* 313 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 319 */     String selObj = this.m_profileList.getSelectedObj();
/* 320 */     refreshData(selObj);
/*     */   }
/*     */ 
/*     */   public void refreshData(String selName)
/*     */   {
/* 325 */     DataBinder binder = new DataBinder();
/* 326 */     binder.setLocalData(this.m_helper.m_props);
/* 327 */     String docClass = this.m_docClass.getSelectedInternalValue();
/* 328 */     if (docClass == null)
/*     */     {
/* 330 */       return;
/*     */     }
/*     */ 
/* 333 */     binder.putLocal("dDocClass", docClass);
/*     */     try
/*     */     {
/* 337 */       executeService("GET_DOCCLASS_DOCPROFILES", binder, false);
/* 338 */       this.m_profileList.refreshList(binder, selName);
/*     */ 
/* 340 */       this.m_triggerField = binder.getLocal("dpTriggerField");
/* 341 */       this.m_relUrl = binder.getLocal("RelativeCgiWebRoot");
/*     */ 
/* 344 */       executeService("GET_DOCCLASS_INFO", binder, false);
/* 345 */       DataResultSet rset = (DataResultSet)binder.getResultSet("DocClassInfo");
/* 346 */       if ((rset != null) && (!rset.isEmpty()))
/*     */       {
/* 348 */         this.m_defProfile = rset.getStringValueByName("dDefaultProfile");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 353 */       Report.trace("profiles", null, e);
/* 354 */       reportError(e);
/*     */     }
/*     */     finally
/*     */     {
/* 358 */       checkSelection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkSelection()
/*     */   {
/* 364 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 372 */     String cmd = e.getActionCommand();
/* 373 */     if (cmd.equals("add"))
/*     */     {
/* 375 */       Properties props = promptForNewName();
/* 376 */       if (props != null)
/*     */       {
/* 378 */         addOrEditProfile(props, true);
/*     */       }
/*     */     }
/* 381 */     else if (cmd.equals("edit"))
/*     */     {
/* 383 */       int index = this.m_profileList.getSelectedIndex();
/* 384 */       Properties props = this.m_profileList.getDataAt(index);
/* 385 */       addOrEditProfile(props, false);
/*     */     }
/* 387 */     else if (cmd.equals("delete"))
/*     */     {
/* 389 */       deleteProfile();
/*     */     }
/* 391 */     else if (cmd.equals("selectTrigger"))
/*     */     {
/* 393 */       promptForTriggerField();
/*     */     } else {
/* 395 */       if (!cmd.equals("preview"))
/*     */         return;
/* 397 */       previewProfile();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void promptForTriggerField()
/*     */   {
/* 404 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apDpTitleEditTriggerField", this.m_ctx), true);
/*     */ 
/* 407 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 413 */         Properties promptData = this.m_dlgHelper.m_props;
/*     */ 
/* 415 */         IdcMessage msg = null;
/* 416 */         String name = promptData.getProperty("dpTriggerField");
/* 417 */         if ((DocProfilePanel.this.m_triggerField != null) && (DocProfilePanel.this.m_triggerField.length() > 0))
/*     */         {
/* 419 */           if (name.length() == 0)
/*     */           {
/* 421 */             msg = IdcMessageFactory.lc("apDpDisablingTriggerWarning", new Object[0]);
/*     */           }
/* 423 */           else if (!DocProfilePanel.this.m_triggerField.equalsIgnoreCase(name))
/*     */           {
/* 425 */             msg = IdcMessageFactory.lc("apDpChangingTriggerWarning", new Object[0]);
/*     */           }
/*     */         }
/*     */ 
/* 429 */         if (msg != null)
/*     */         {
/* 431 */           int result = MessageBox.doMessage(DocProfilePanel.this.m_systemInterface, IdcMessageFactory.lc(msg, "apDpContinueTriggerChange", new Object[0]), 2);
/*     */ 
/* 434 */           if (result == 0)
/*     */           {
/* 436 */             return false;
/*     */           }
/*     */         }
/* 439 */         return true;
/*     */       }
/*     */     };
/* 443 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("DpEditTriggerField"));
/*     */ 
/* 447 */     Properties props = helper.m_props;
/* 448 */     if (this.m_triggerField != null)
/*     */     {
/* 450 */       props.put("dpTriggerField", this.m_triggerField);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 455 */       ViewFields docFieldsObj = new ViewFields(this.m_ctx);
/*     */ 
/* 458 */       docFieldsObj.addField("", this.m_systemInterface.getString("apDpNoneSpecified"));
/*     */ 
/* 461 */       docFieldsObj.addDocOptionFieldsEx(false);
/*     */ 
/* 464 */       DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/* 465 */       docFieldsObj.addMetaFieldsEx(drset, false, true);
/*     */ 
/* 467 */       Vector docFieldsDef = docFieldsObj.m_viewFields;
/* 468 */       Vector fields = new IdcVector();
/* 469 */       int size = docFieldsDef.size();
/* 470 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 472 */         FieldDef fieldDef = (FieldDef)docFieldsDef.elementAt(i);
/* 473 */         String[] map = new String[2];
/* 474 */         map[0] = fieldDef.m_name;
/* 475 */         map[1] = fieldDef.m_caption;
/* 476 */         fields.addElement(map);
/*     */       }
/*     */ 
/* 479 */       int num = fields.size();
/* 480 */       String[][] display = new String[num][2];
/* 481 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 483 */         display[i] = ((String[])(String[])fields.elementAt(i));
/*     */       }
/* 485 */       DisplayChoice fieldCmp = new DisplayChoice();
/* 486 */       fieldCmp.init(display);
/*     */ 
/* 488 */       helper.addLabelFieldPair(mainPanel, this.m_systemInterface.localizeCaption("apLabelFieldName"), fieldCmp, "dpTriggerField");
/*     */ 
/* 492 */       if (helper.prompt() == 1)
/*     */       {
/* 494 */         AppContextUtils.executeService(this, "EDIT_DOCPROFILE_TRIGGER", props);
/* 495 */         String val = props.getProperty("dpTriggerField");
/* 496 */         this.m_helper.m_exchange.setComponentValue("dpTriggerField", val);
/*     */ 
/* 498 */         this.m_helper.m_props.put("dpTriggerField", val);
/* 499 */         this.m_triggerField = val;
/*     */ 
/* 501 */         refreshView();
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 506 */       IdcMessage msg = IdcMessageFactory.lc("apDpUnableToBuildFieldList", new Object[0]);
/* 507 */       reportError(e, msg);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 511 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Properties promptForNewName()
/*     */   {
/* 517 */     if ((this.m_triggerField == null) || (this.m_triggerField.length() == 0))
/*     */     {
/* 519 */       MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apDpMustDefineTrigger", new Object[0]), 1);
/*     */ 
/* 521 */       return null;
/*     */     }
/*     */ 
/* 524 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/* 530 */         Properties promptData = this.m_dlgHelper.m_props;
/* 531 */         String name = promptData.getProperty("dpName");
/*     */ 
/* 533 */         this.m_errorMessage = Validation.checkUrlFileSegmentForDB(name, "apDpName", 0, null);
/* 534 */         if (this.m_errorMessage == null)
/*     */         {
/* 537 */           if (DocProfilePanel.this.m_profileList.findRowPrimaryField(name) < 0)
/*     */           {
/* 539 */             return true;
/*     */           }
/* 541 */           this.m_errorMessage = IdcMessageFactory.lc("apDpNameConflict", new Object[0]);
/*     */         }
/* 543 */         return false;
/*     */       }
/*     */     };
/* 548 */     DialogHelper helper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apDpAddProfileTitle1", this.m_ctx, this.m_docClass.getSelectedInternalValue()), true);
/*     */ 
/* 550 */     JPanel mainPanel = helper.initStandard(null, okCallback, 2, true, DialogHelpTable.getHelpPage("DpAddProfileName"));
/*     */ 
/* 552 */     helper.addLabelEditPair(mainPanel, this.m_systemInterface.getString("apDpLabelName"), 30, "dpName");
/*     */ 
/* 556 */     if (helper.prompt() == 1)
/*     */     {
/* 558 */       helper.m_props.put("dDocClass", this.m_docClass.getSelectedInternalValue());
/* 559 */       return helper.m_props;
/*     */     }
/* 561 */     return null;
/*     */   }
/*     */ 
/*     */   protected void addOrEditProfile(Properties props, boolean isNew)
/*     */   {
/* 566 */     String name = props.getProperty("dpName");
/* 567 */     DataBinder binder = new DataBinder();
/* 568 */     binder.setLocalData(props);
/*     */ 
/* 570 */     String title = null;
/* 571 */     String helpPage = null;
/* 572 */     if (isNew)
/*     */     {
/* 574 */       title = "apDpAddProfileTitle2";
/* 575 */       helpPage = "DpAddProfile";
/*     */     }
/*     */     else
/*     */     {
/* 579 */       title = "apDpEditProfileTitle";
/* 580 */       helpPage = "DpEditProfile";
/*     */       try
/*     */       {
/* 585 */         executeServiceWithCursor("GET_DOCPROFILE", binder);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 590 */         reportError(e);
/* 591 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 595 */     title = LocaleUtils.encodeMessage(title, null, name);
/* 596 */     title = LocaleResources.localizeMessage(title, this.m_ctx);
/* 597 */     helpPage = DialogHelpTable.getHelpPage(helpPage);
/* 598 */     EditProfileDlg dlg = new EditProfileDlg(this.m_systemInterface, title, this, helpPage);
/*     */ 
/* 600 */     int result = dlg.prompt(binder, isNew, this.m_helper.m_props);
/* 601 */     if (result != 1) {
/*     */       return;
/*     */     }
/* 604 */     refreshData(name);
/*     */   }
/*     */ 
/*     */   protected void deleteProfile()
/*     */   {
/* 610 */     int index = this.m_profileList.getSelectedIndex();
/* 611 */     if (index < 0)
/*     */     {
/* 613 */       return;
/*     */     }
/*     */ 
/* 616 */     Properties props = this.m_profileList.getDataAt(index);
/*     */ 
/* 618 */     IdcMessage msg = IdcMessageFactory.lc("apDpDeleteProfilePrompt", new Object[] { props.getProperty("dpName") });
/*     */ 
/* 620 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 621 */     if (result != 2)
/*     */       return;
/*     */     try
/*     */     {
/* 625 */       AppContextUtils.executeService(this, "DELETE_DOCPROFILE", props);
/* 626 */       refreshData(null);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 630 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void previewProfile()
/*     */   {
/* 637 */     int index = this.m_profileList.getSelectedIndex();
/* 638 */     if (index < 0)
/*     */     {
/* 640 */       return;
/*     */     }
/*     */ 
/* 643 */     Properties props = this.m_profileList.getDataAt(index);
/* 644 */     props.put("RelativeCgiWebUrl", this.m_relUrl);
/*     */ 
/* 646 */     String helpPage = DialogHelpTable.getHelpPage("DpPreview");
/* 647 */     PreviewProfileDlg dlg = new PreviewProfileDlg(this.m_systemInterface, this.m_systemInterface.getString("apDpPreviewProfile"), this, helpPage);
/*     */ 
/* 649 */     dlg.init(props);
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 660 */     if (e.getSource().equals(this.m_profileList.m_list))
/*     */     {
/* 662 */       int index = this.m_profileList.getSelectedIndex();
/* 663 */       Properties props = this.m_profileList.getDataAt(index);
/* 664 */       if (props != null)
/*     */       {
/* 666 */         String selectedItem = props.getProperty("dpName");
/* 667 */         if ((selectedItem != null) && (selectedItem.equals(this.m_defProfile)))
/*     */         {
/* 669 */           this.m_deleteBtn.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 673 */           this.m_deleteBtn.setEnabled(true);
/*     */         }
/*     */       }
/*     */     } else {
/* 677 */       if (!e.getSource().equals(this.m_docClass))
/*     */         return;
/*     */       try
/*     */       {
/* 681 */         refreshView();
/*     */       }
/*     */       catch (ServiceException e1)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 697 */     return (value.equals(this.m_defProfile)) ? value + " (" + LocaleResources.getString("apDpDefaultLabel", this.m_ctx) + ")" : value;
/*     */   }
/*     */ 
/*     */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 703 */     return null;
/*     */   }
/*     */ 
/*     */   public void setClassListChanged()
/*     */   {
/* 711 */     this.m_isClassListChanged = true;
/*     */   }
/*     */ 
/*     */   public void paintComponent(Graphics g)
/*     */   {
/* 717 */     if (this.m_isClassListChanged)
/*     */     {
/* 719 */       initDocClassList();
/* 720 */       refreshData(null);
/* 721 */       this.m_isClassListChanged = false;
/*     */     }
/* 723 */     super.paintComponent(g);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 729 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98092 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocProfilePanel
 * JD-Core Version:    0.5.4
 */