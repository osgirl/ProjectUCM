/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.EditOptionListDlg;
/*     */ import intradoc.shared.schema.SchemaFieldConfig;
/*     */ import intradoc.shared.schema.SchemaFieldData;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocSchemaPanel extends DocConfigPanel
/*     */   implements ActionListener, ItemListener
/*     */ {
/*  65 */   protected UdlPanel m_viewList = null;
/*     */ 
/*  68 */   protected SchemaViewConfig m_schemaViewConfig = null;
/*     */ 
/*  70 */   protected Vector m_controlComponents = null;
/*  71 */   protected Vector m_controlFlags = null;
/*     */ 
/*  73 */   public String m_resultSetName = "SchemaViewConfig";
/*     */ 
/*     */   public DocSchemaPanel()
/*     */   {
/*  78 */     this.m_subject = "schema";
/*  79 */     this.m_controlComponents = new IdcVector();
/*  80 */     this.m_controlFlags = new IdcVector();
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys)
/*     */     throws ServiceException
/*     */   {
/*  86 */     super.init(sys);
/*     */ 
/*  88 */     initUI();
/*     */ 
/*  91 */     refreshData(null);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*  96 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*  97 */     JPanel pnl = new PanePanel();
/*  98 */     gh.useGridBag(pnl);
/*     */ 
/* 100 */     this.m_viewList = createViewList();
/*     */ 
/* 103 */     gh.m_gc.fill = 0;
/* 104 */     gh.m_gc.weighty = 0.0D;
/* 105 */     addButtons(pnl);
/*     */ 
/* 108 */     gh.useGridBag(this);
/* 109 */     gh.m_gc.weightx = 1.0D;
/* 110 */     gh.m_gc.weighty = 1.0D;
/* 111 */     gh.m_gc.fill = 1;
/* 112 */     this.m_helper.addLastComponentInRow(this, this.m_viewList);
/* 113 */     gh.m_gc.weightx = 0.0D;
/* 114 */     this.m_helper.addLastComponentInRow(this, pnl);
/*     */   }
/*     */ 
/*     */   protected UdlPanel createViewList()
/*     */   {
/* 119 */     String columns = "schViewName,schViewDescription,schTableName,schInternalColumn";
/* 120 */     UdlPanel list = new UdlPanel(this.m_systemInterface.getString("apSchLabelView"), null, 500, 20, "SchemaViewConfig", true);
/*     */ 
/* 124 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apSchViewNameColumn"), "schViewName", 8.0D);
/* 125 */     list.setColumnInfo(info);
/* 126 */     info = new ColumnInfo(this.m_systemInterface.getString("apSchViewDescriptionColumn"), "schViewDescription", 14.0D);
/*     */ 
/* 128 */     list.setColumnInfo(info);
/* 129 */     info = new ColumnInfo(this.m_systemInterface.getString("apSchTableNameColumn"), "schTableName", 8.0D);
/*     */ 
/* 131 */     list.setColumnInfo(info);
/* 132 */     info = new ColumnInfo(this.m_systemInterface.getString("apSchInternalColumn"), "schInternalColumn", 10.0D);
/* 133 */     list.setColumnInfo(info);
/*     */ 
/* 135 */     list.setVisibleColumns(columns);
/* 136 */     list.setIDColumn("schViewName");
/* 137 */     list.m_list.addActionListener(this);
/* 138 */     list.m_list.addItemListener(this);
/*     */ 
/* 140 */     list.init();
/* 141 */     return list;
/*     */   }
/*     */ 
/*     */   protected void addButtons(JPanel pnl)
/*     */   {
/* 147 */     String[][] btnInfo = { { "add", "apSchDlgButtonAddView", "", "apSchAddViewTitle" }, { "edit", "apSchDlgButtonEditView", ":edit:", "apReadableButtonEditView" }, { "delete", "apSchDlgButtonDeleteView", ":delete:", "apReadableButtonDeleteView" }, { "space", "", "", "" }, { "editValues", "apSchDlgButtonEditViewValues", ":editValues:", "apReadableButtonEditViewValues" } };
/*     */ 
/* 156 */     JPanel btnPanel = new PanePanel();
/* 157 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 159 */       String cmd = btnInfo[i][0];
/* 160 */       if (cmd.equals("space"))
/*     */       {
/* 163 */         btnPanel.add(new PanePanel());
/*     */       }
/*     */       else
/*     */       {
/* 167 */         JButton btn = addButton(LocaleResources.getString(btnInfo[i][1], this.m_ctx), btnInfo[i][2]);
/*     */ 
/* 169 */         btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 170 */         btn.setActionCommand(cmd);
/* 171 */         btn.addActionListener(this);
/* 172 */         btnPanel.add(btn);
/*     */       }
/*     */     }
/* 175 */     this.m_helper.addComponent(pnl, btnPanel);
/*     */   }
/*     */ 
/*     */   public JButton addButton(String label, String flags)
/*     */   {
/* 180 */     JButton btn = new JButton(label);
/* 181 */     this.m_controlComponents.addElement(btn);
/* 182 */     this.m_controlFlags.addElement(flags);
/* 183 */     return btn;
/*     */   }
/*     */ 
/*     */   public void addControlComponent(JMenuItem cmpt)
/*     */   {
/* 188 */     this.m_controlComponents.addElement(cmpt);
/*     */   }
/*     */ 
/*     */   public void addControlComponent(Component cmpt)
/*     */   {
/* 193 */     this.m_controlComponents.addElement(cmpt);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 201 */     String cmd = e.getActionCommand();
/* 202 */     if (cmd.equals("add"))
/*     */     {
/* 204 */       addOrEdit(true, false);
/*     */     }
/* 206 */     else if (cmd.equals("edit"))
/*     */     {
/* 208 */       addOrEdit(false, false);
/*     */     }
/* 210 */     else if (cmd.equals("delete"))
/*     */     {
/* 212 */       delete();
/*     */     }
/* 214 */     else if (cmd.equals("editValues"))
/*     */     {
/* 216 */       editValues();
/*     */     } else {
/* 218 */       if (!cmd.equals(""))
/*     */         return;
/* 220 */       addOrEdit(false, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 227 */     checkSelection();
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(boolean isAdd, boolean isChild)
/*     */   {
/* 232 */     String dlgTitle = null;
/* 233 */     String viewName = null;
/* 234 */     Properties props = null;
/* 235 */     if (isAdd)
/*     */     {
/* 237 */       dlgTitle = "apSchAddViewTitle";
/* 238 */       SelectTableWizard dlg = new SelectTableWizard(this.m_systemInterface, LocaleResources.getString(dlgTitle, this.m_ctx), DialogHelpTable.getHelpPage("SelectSchemaTable"));
/*     */ 
/* 241 */       props = new Properties();
/* 242 */       props.put("IsNew", "" + isAdd);
/*     */       try
/*     */       {
/* 245 */         int result = dlg.init(props);
/* 246 */         if (result != 1)
/*     */         {
/* 249 */           return;
/*     */         }
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 254 */         Report.trace("schema", null, e);
/* 255 */         reportError(e);
/* 256 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 260 */     if (!isAdd)
/*     */     {
/* 262 */       viewName = this.m_viewList.getSelectedObj();
/* 263 */       if (viewName == null)
/*     */       {
/* 266 */         return;
/*     */       }
/* 268 */       SchemaViewData data = getViewData(viewName);
/* 269 */       Properties viewProps = data.getData().getLocalData();
/* 270 */       viewProps = (Properties)viewProps.clone();
/*     */ 
/* 272 */       viewName = viewProps.getProperty("schViewName");
/* 273 */       if (isChild)
/*     */       {
/* 275 */         dlgTitle = "apSchAddChildViewTitle";
/*     */ 
/* 278 */         props.put("schViewParent", viewName);
/* 279 */         props.put("isChildView", "true");
/*     */       }
/*     */       else
/*     */       {
/* 283 */         dlgTitle = "apSchEditViewTitle";
/* 284 */         props = viewProps;
/*     */       }
/*     */     }
/*     */ 
/* 288 */     String title = LocaleUtils.encodeMessage(dlgTitle, null, viewName);
/* 289 */     dlgTitle = LocaleResources.localizeMessage(title, this.m_ctx);
/* 290 */     AddViewDlg dlg = new AddViewDlg(this.m_systemInterface, dlgTitle, DialogHelpTable.getHelpPage("AddOrEditView"));
/*     */ 
/* 292 */     int result = dlg.init(props, this.m_schemaViewConfig);
/* 293 */     if (result != 1) {
/*     */       return;
/*     */     }
/* 296 */     String selName = props.getProperty("schViewName");
/* 297 */     refreshData(selName);
/*     */   }
/*     */ 
/*     */   protected void delete()
/*     */   {
/* 303 */     String selName = this.m_viewList.getSelectedObj();
/* 304 */     if (selName == null)
/*     */     {
/* 306 */       return;
/*     */     }
/*     */ 
/* 309 */     String action = "DELETE_SCHEMA_VIEW";
/* 310 */     IdcMessage msg = IdcMessageFactory.lc("apSchVerifyViewDelete", new Object[] { selName });
/*     */ 
/* 312 */     SchemaViewData viewData = getViewData(selName);
/* 313 */     Vector fieldList = null;
/* 314 */     boolean isOptionList = false;
/* 315 */     SchemaFieldConfig fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/*     */ 
/* 318 */     if (viewData == null)
/*     */     {
/* 320 */       Report.trace("schema", "view data missing for view '" + selName + "'", null);
/*     */     }
/*     */     else
/*     */     {
/* 324 */       String viewType = viewData.get("schViewType");
/* 325 */       String tableName = viewData.get("schTableName");
/* 326 */       if ((viewType != null) && (viewType.equals("table")) && (tableName != null) && (tableName.equals("OptionsList")))
/*     */       {
/* 331 */         isOptionList = true;
/* 332 */         String listKey = viewData.get("schCriteriaValue0");
/* 333 */         if (listKey != null)
/*     */         {
/* 335 */           msg = IdcMessageFactory.lc("apSchVerifyViewDeleteWithOptionList", new Object[] { selName, listKey });
/* 336 */           fieldList = fields.fieldsUsingView(listKey);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 341 */     if (fieldList == null)
/*     */     {
/* 343 */       fieldList = fields.fieldsUsingView(selName);
/*     */     }
/*     */ 
/* 346 */     if (fieldList.size() > 0)
/*     */     {
/* 348 */       SchemaFieldData data = (SchemaFieldData)fieldList.elementAt(0);
/*     */ 
/* 350 */       String key = (isOptionList) ? "apSchOptionsListUsedByField" : "apSchViewUsedByField";
/*     */ 
/* 352 */       String target = data.get("schFieldTarget");
/* 353 */       if ((target != null) && (target.length() > 0))
/*     */       {
/* 355 */         key = key + "_" + target.toLowerCase();
/*     */       }
/* 357 */       msg = IdcMessageFactory.lc(key, new Object[] { selName, data.m_name });
/* 358 */       msg = IdcMessageFactory.lc(msg, "apSchErrorDeletingView", new Object[] { selName });
/* 359 */       MessageBox.doMessage(this.m_systemInterface, msg, 1);
/* 360 */       return;
/*     */     }
/*     */ 
/* 363 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 368 */       DataBinder binder = new DataBinder();
/* 369 */       binder.putLocal("schViewName", selName);
/* 370 */       AppLauncher.executeService(action, binder);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 374 */       reportError(exp);
/*     */     }
/*     */     finally
/*     */     {
/* 378 */       refreshData(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void editValues()
/*     */   {
/* 385 */     String selName = this.m_viewList.getSelectedObj();
/* 386 */     if (selName == null)
/*     */     {
/* 388 */       return;
/*     */     }
/*     */ 
/* 391 */     if (selName.equalsIgnoreCase("docTypes"))
/*     */     {
/*     */       try
/*     */       {
/* 395 */         String title = this.m_systemInterface.getString("apDocTypesDialogTitle");
/* 396 */         String helpPage = DialogHelpTable.getHelpPage("EditDocTypes");
/* 397 */         DocTypesDialog typeDlg = new DocTypesDialog(this.m_systemInterface, title, helpPage);
/* 398 */         typeDlg.init();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 402 */         reportError(e);
/*     */       }
/* 404 */       return;
/*     */     }
/* 406 */     if (selName.equalsIgnoreCase("docFormats"))
/*     */     {
/*     */       try
/*     */       {
/* 410 */         String title = this.m_systemInterface.getString("apDocFormatsDialogTitle");
/* 411 */         String helpPage = DialogHelpTable.getHelpPage("EditDocFormats");
/* 412 */         DocFormatsDlg formatDlg = new DocFormatsDlg(this.m_systemInterface, title, helpPage);
/* 413 */         formatDlg.init();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 417 */         reportError(e);
/*     */       }
/* 419 */       return;
/*     */     }
/* 421 */     if ((selName.equals("roles")) || (selName.equals("docAuthors")))
/*     */     {
/* 425 */       IdcMessage msg = IdcMessageFactory.lc("apSchUseUserApplet", new Object[] { selName });
/* 426 */       reportError(null, msg);
/* 427 */       return;
/*     */     }
/*     */ 
/* 432 */     SchemaViewData vData = getViewData(selName);
/* 433 */     Properties props = (Properties)vData.getData().getLocalData().clone();
/*     */ 
/* 436 */     String tableName = props.getProperty("schTableName");
/* 437 */     if (tableName == null)
/*     */     {
/* 440 */       reportError(null, IdcMessageFactory.lc("apSchSelectedViewNotTable", new Object[0]));
/* 441 */       return;
/*     */     }
/* 443 */     int result = 1;
/* 444 */     if (tableName.equalsIgnoreCase("OptionsList"))
/*     */     {
/* 446 */       String criteriaValue = props.getProperty("schCriteriaValue0");
/* 447 */       if (criteriaValue == null)
/*     */       {
/* 449 */         reportError(null, IdcMessageFactory.lc("apSchSelectedViewIsOptionsListWithoutCriteria", new Object[0]));
/* 450 */         return;
/*     */       }
/*     */ 
/* 453 */       String title = LocaleResources.getString("apLabelOptionList", this.m_ctx);
/*     */ 
/* 459 */       props.put("dType", "text");
/* 460 */       props.put("dOptionListKey", criteriaValue);
/*     */ 
/* 462 */       EditOptionListDlg edtOptions = new EditOptionListDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("OptionList"), "UPDATE_OPTION_LIST");
/*     */ 
/* 464 */       edtOptions.init(props);
/* 465 */       edtOptions.prompt();
/*     */     }
/*     */     else
/*     */     {
/* 469 */       String title = LocaleUtils.encodeMessage("apSchEditViewValuesTitle", null, selName);
/*     */ 
/* 471 */       title = LocaleResources.localizeMessage(title, this.m_ctx);
/* 472 */       EditViewValuesDlg dlg = new EditViewValuesDlg(this.m_systemInterface, title, DialogHelpTable.getHelpPage("EditViewValues"));
/*     */ 
/* 475 */       result = dlg.init(props);
/*     */     }
/*     */ 
/* 478 */     if (result != 1) {
/*     */       return;
/*     */     }
/* 481 */     refreshData(selName);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 489 */     String selectedName = this.m_viewList.getSelectedObj();
/* 490 */     refreshData(selectedName);
/*     */   }
/*     */ 
/*     */   public void refreshData(String selName)
/*     */   {
/* 495 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 498 */       this.m_schemaViewConfig = ((SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig"));
/* 499 */       if ((!AppLauncher.getIsStandAlone()) || (AppLauncher.getIsHeavyClient()))
/*     */       {
/* 501 */         if (this.m_schemaViewConfig != null)
/*     */         {
/* 505 */           binder.addResultSet("SchemaViewConfig", this.m_schemaViewConfig);
/*     */         }
/* 507 */         AppLauncher.executeService("GET_SCHEMA_VIEWS", binder);
/* 508 */         this.m_schemaViewConfig = ((SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig"));
/*     */       }
/* 510 */       refreshList(selName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 514 */       Report.trace("schema", null, e);
/* 515 */       reportError(e);
/*     */     }
/*     */     finally
/*     */     {
/* 519 */       checkSelection();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected synchronized void refreshList(String name)
/*     */   {
/* 525 */     this.m_viewList.refreshList(this.m_schemaViewConfig, name);
/*     */   }
/*     */ 
/*     */   protected SchemaViewData getViewData(String viewName)
/*     */   {
/* 530 */     if (this.m_schemaViewConfig == null)
/*     */     {
/* 532 */       return null;
/*     */     }
/* 534 */     SchemaViewData data = (SchemaViewData)this.m_schemaViewConfig.getData(viewName);
/* 535 */     return data;
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/* 543 */     String viewName = this.m_viewList.getSelectedObj();
/* 544 */     boolean isItemSelected = viewName != null;
/* 545 */     boolean isOptionList = false;
/* 546 */     boolean isSystem = false;
/* 547 */     if (isItemSelected)
/*     */     {
/* 549 */       if ((viewName.equals("docFormats")) || (viewName.equals("docTypes")))
/*     */       {
/* 552 */         isSystem = true;
/*     */       }
/*     */       else
/*     */       {
/* 556 */         SchemaViewData data = getViewData(viewName);
/* 557 */         if (data != null)
/*     */         {
/* 559 */           String type = data.get("schViewType");
/* 560 */           if ((type != null) && (type.equals("optionList")))
/*     */           {
/* 562 */             isOptionList = true;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 568 */     int size = this.m_controlComponents.size();
/* 569 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 571 */       Object obj = this.m_controlComponents.elementAt(i);
/* 572 */       String flags = (String)this.m_controlFlags.elementAt(i);
/*     */ 
/* 574 */       boolean enable = true;
/* 575 */       if (flags.length() > 0)
/*     */       {
/* 577 */         if ((flags.indexOf(":edit:") >= 0) && (!isItemSelected))
/*     */         {
/* 579 */           enable = false;
/*     */         }
/* 581 */         else if ((flags.indexOf(":editValues:") >= 0) && (((!isItemSelected) || (isOptionList))))
/*     */         {
/* 584 */           enable = false;
/*     */         }
/* 586 */         else if ((flags.indexOf(":delete:") >= 0) && (((!isItemSelected) || (isOptionList) || (isSystem))))
/*     */         {
/* 589 */           enable = false;
/*     */         }
/*     */       }
/*     */ 
/* 593 */       if (obj instanceof Component)
/*     */       {
/* 595 */         Component cmpt = (Component)obj;
/* 596 */         cmpt.setEnabled(enable);
/*     */       } else {
/* 598 */         if (!obj instanceof JMenuItem) {
/*     */           continue;
/*     */         }
/* 601 */         JMenuItem menuItem = (JMenuItem)obj;
/* 602 */         menuItem.setEnabled(enable);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 615 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocSchemaPanel
 * JD-Core Version:    0.5.4
 */