/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.Errors;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.StatusBar;
/*     */ import intradoc.gui.iwt.DataResultSetTableModel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.gui.iwt.event.IwtItemEvent;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.AddViewValueDlg;
/*     */ import intradoc.shared.gui.FilterUtils;
/*     */ import intradoc.shared.gui.RefreshView;
/*     */ import intradoc.shared.gui.SchemaView;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.schema.SchemaEditHelper;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditViewValuesDlg
/*     */   implements RefreshView, SharedContext, SystemInterface
/*     */ {
/*  81 */   protected SystemInterface m_systemInterface = null;
/*  82 */   protected ExecutionContext m_context = null;
/*  83 */   protected DialogHelper m_helper = null;
/*     */ 
/*  85 */   protected DataBinder m_binder = null;
/*  86 */   protected DataBinder m_cachedBinder = null;
/*  87 */   protected boolean m_isFatalError = false;
/*  88 */   protected SchemaHelper m_schHelper = null;
/*     */ 
/*  91 */   protected SchemaView m_schemaView = null;
/*  92 */   protected DocumentLocalizedProfile m_docProfile = null;
/*  93 */   protected StatusBar m_statusBar = null;
/*     */ 
/*  95 */   protected boolean m_isSelectOnly = false;
/*  96 */   protected String m_initialKeyValue = null;
/*  97 */   protected String m_parentColumn = null;
/*     */ 
/*     */   public EditViewValuesDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/* 101 */     this.m_systemInterface = sys;
/* 102 */     this.m_context = sys.getExecutionContext();
/*     */ 
/* 104 */     this.m_helper = new DialogHelper();
/* 105 */     this.m_helper.m_title = title;
/* 106 */     this.m_helper.m_parent = sys.getMainWindow();
/* 107 */     this.m_helper.m_isModal = true;
/* 108 */     this.m_helper.m_helpPage = helpPage;
/* 109 */     this.m_helper.m_exitOnClose = false;
/*     */   }
/*     */ 
/*     */   public int init(Properties props)
/*     */   {
/* 114 */     this.m_helper.m_props = props;
/* 115 */     this.m_binder = new DataBinder();
/* 116 */     this.m_binder.setLocalData(props);
/* 117 */     this.m_schHelper = new SchemaHelper();
/*     */     try
/*     */     {
/* 121 */       initUI();
/*     */ 
/* 123 */       this.m_schemaView.refreshView();
/*     */ 
/* 125 */       if (this.m_initialKeyValue != null)
/*     */       {
/* 127 */         UdlPanel p = this.m_schemaView.getList();
/* 128 */         DataResultSet drset = p.m_list.m_tableDataModel.m_rset;
/* 129 */         drset.first();
/* 130 */         FieldInfo fi = new FieldInfo();
/* 131 */         if (drset.getFieldInfo(this.m_parentColumn, fi))
/*     */         {
/* 133 */           drset.findRow(fi.m_index, this.m_initialKeyValue);
/* 134 */           if (drset.isRowPresent())
/*     */           {
/* 136 */             p.m_list.select(drset.getCurrentRow());
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 143 */       MessageBox.reportError(this.m_systemInterface, e);
/* 144 */       return 0;
/*     */     }
/* 146 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   protected void initUI() throws ServiceException
/*     */   {
/* 151 */     JDialog dlg = null;
/* 152 */     if (this.m_helper.m_dialog != null)
/*     */     {
/* 154 */       dlg = new CustomDialog(this.m_helper.m_dialog, this.m_helper.m_title, true);
/*     */     }
/*     */     else
/*     */     {
/* 158 */       dlg = new CustomDialog((JFrame)this.m_helper.m_parent, this.m_helper.m_title, true);
/*     */     }
/*     */ 
/* 161 */     this.m_helper.attachToWindow(dlg, this, this.m_helper.m_props);
/* 162 */     this.m_helper.m_dialog = dlg;
/*     */ 
/* 164 */     this.m_helper.m_toolbar = new PanePanel();
/* 165 */     this.m_helper.m_toolbar.setLayout(new FlowLayout());
/*     */ 
/* 167 */     JPanel viewAreaPanel = new PanePanel();
/* 168 */     viewAreaPanel.setLayout(new BorderLayout());
/* 169 */     viewAreaPanel.add("Center", this.m_helper.m_mainPanel = new PanePanel());
/* 170 */     viewAreaPanel.add("South", this.m_helper.m_toolbar);
/*     */ 
/* 172 */     dlg.setLayout(new BorderLayout());
/* 173 */     dlg.add("Center", viewAreaPanel);
/* 174 */     dlg.add("South", this.m_statusBar = new StatusBar());
/*     */ 
/* 176 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 177 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 179 */     UserData userData = AppLauncher.getUserData();
/* 180 */     this.m_docProfile = new DocumentLocalizedProfile(userData, 8, this.m_context);
/*     */ 
/* 182 */     this.m_schemaView = new SchemaView(this.m_helper, this, this.m_docProfile);
/* 183 */     ViewData viewData = new ViewData(4);
/*     */ 
/* 186 */     loadInformationForDisplay(viewData);
/*     */ 
/* 188 */     this.m_schemaView.initUI(viewData);
/*     */ 
/* 190 */     ItemListener listListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 194 */         int state = e.getStateChange();
/* 195 */         if ((state != 1) && (state != 2))
/*     */         {
/* 197 */           return;
/*     */         }
/* 199 */         if (e instanceof IwtItemEvent)
/*     */         {
/* 201 */           IwtItemEvent itemEvent = (IwtItemEvent)e;
/* 202 */           if (!itemEvent.checkFlag(IwtItemEvent.FINAL_ITEM_EVENT))
/*     */           {
/* 204 */             return;
/*     */           }
/*     */         }
/* 207 */         EditViewValuesDlg.this.checkSelection();
/*     */       }
/*     */     };
/* 210 */     this.m_schemaView.addItemListener(listListener);
/*     */ 
/* 212 */     initButtonPanel();
/*     */   }
/*     */ 
/*     */   protected void initButtonPanel()
/*     */   {
/* 218 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 222 */         String cmdStr = e.getActionCommand();
/* 223 */         EditViewValuesDlg.this.actionCommand(cmdStr);
/*     */       }
/*     */     };
/* 227 */     JPanel btnPanel = new PanePanel();
/* 228 */     btnPanel.setLayout(new FlowLayout());
/*     */     String[][] btnInfo;
/*     */     String[][] btnInfo;
/* 232 */     if (!this.m_isSelectOnly)
/*     */     {
/* 234 */       btnInfo = new String[][] { { "add", "apSchAddValueButton", "0", "apSchAddViewValueTitle" }, { "edit", "apSchEditValueButton", "1", "apSchEditViewValueTitle" }, { "delete", "apSchDeleteValueButton", "1", "apReadableButtonDeleteValue" }, { "separator", "", "0", "" }, { "addbatch", "apSchAddBatchButton", "0", "apReadableButtonAddBatchValue" }, { "close", "apLabelClose", "0", "apLabelClose" } };
/*     */     }
/*     */     else
/*     */     {
/* 245 */       btnInfo = new String[][] { { "select", "apLabelOK", "0", "apLabelOK" }, { "cancel", "apLabelCancel", "0", "apLabelCancel" } };
/*     */     }
/*     */ 
/* 252 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 254 */       String key = btnInfo[i][1];
/* 255 */       if (key.length() > 0)
/*     */       {
/* 257 */         btnInfo[i][1] = LocaleResources.getString(key, this.m_context);
/*     */       }
/* 259 */       key = btnInfo[i][3];
/* 260 */       if (key.length() <= 0)
/*     */         continue;
/* 262 */       btnInfo[i][3] = LocaleResources.getString(key, this.m_context);
/*     */     }
/*     */ 
/* 266 */     UdlPanel list = this.m_schemaView.getList();
/* 267 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 269 */       String cmd = btnInfo[i][0];
/*     */ 
/* 271 */       if (cmd.equals("separator"))
/*     */       {
/* 274 */         btnPanel.add(new PanePanel());
/*     */       }
/*     */       else
/*     */       {
/* 278 */         String label = LocaleResources.getString(btnInfo[i][1], this.m_context);
/* 279 */         boolean isControlled = StringUtils.convertToBool(btnInfo[i][2], false);
/*     */ 
/* 281 */         JButton btn = list.addButton(label, isControlled);
/* 282 */         btn.getAccessibleContext().setAccessibleName(btnInfo[i][3]);
/* 283 */         btn.setActionCommand(cmd);
/* 284 */         btn.addActionListener(listener);
/*     */ 
/* 286 */         btnPanel.add(btn);
/*     */       }
/*     */     }
/*     */ 
/* 290 */     this.m_schemaView.addButtonToolbar(btnPanel);
/* 291 */     list.useDefaultListener();
/*     */   }
/*     */ 
/*     */   protected void loadInformationForDisplay(ViewData viewData) throws ServiceException
/*     */   {
/* 296 */     executeService("GET_SCHEMA_VIEW_EDIT_INFO", this.m_binder, false);
/*     */ 
/* 299 */     viewData.m_tableName = this.m_binder.getLocal("schTableName");
/*     */ 
/* 302 */     createFieldInfo(this.m_binder);
/*     */ 
/* 305 */     String viewColumn = this.m_binder.getLocal("schInternalColumn");
/* 306 */     this.m_schemaView.setPersistentColumns(viewColumn, viewColumn);
/*     */   }
/*     */ 
/*     */   protected void createFieldInfo(DataBinder binder)
/*     */   {
/* 311 */     ViewFields viewFields = new ViewFields(this.m_context);
/* 312 */     StringBuffer clmnStr = new StringBuffer();
/*     */ 
/* 314 */     String tableName = this.m_binder.getLocal("schTableName");
/* 315 */     Vector namedRel = this.m_schHelper.computeNamedRelations(tableName);
/*     */ 
/* 317 */     DataResultSet drset = (DataResultSet)binder.getResultSet(tableName);
/* 318 */     int num = drset.getNumFields();
/* 319 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 321 */       FieldInfo fi = new FieldInfo();
/* 322 */       drset.getIndexFieldInfo(i, fi);
/*     */ 
/* 324 */       if (clmnStr.length() > 0)
/*     */       {
/* 326 */         clmnStr.append(",");
/*     */       }
/* 328 */       clmnStr.append(fi.m_name);
/*     */ 
/* 330 */       ViewFieldDef viewField = viewFields.addViewFieldDefFromInfo(fi);
/*     */ 
/* 332 */       int numRel = namedRel.size();
/* 333 */       for (int j = 0; j < numRel; ++j)
/*     */       {
/* 335 */         SchemaRelationData relData = (SchemaRelationData)namedRel.elementAt(j);
/* 336 */         String column2 = relData.get("schTable2Column");
/* 337 */         if (!column2.equals(fi.m_name))
/*     */           continue;
/* 339 */         viewField.m_hasNamedRelation = true;
/* 340 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 345 */     this.m_schemaView.setShowColumnInfo(clmnStr.toString());
/* 346 */     this.m_schemaView.setFilterFields(viewFields);
/* 347 */     this.m_schemaView.setShowColumnFields(viewFields);
/*     */ 
/* 350 */     this.m_binder.addResultSet(tableName, drset);
/*     */   }
/*     */ 
/*     */   public DataBinder refresh(String rsetName, Vector filterData, DataResultSet defSet)
/*     */     throws ServiceException
/*     */   {
/* 359 */     DataBinder binder = new DataBinder();
/* 360 */     binder.putLocal("schViewName", this.m_binder.getLocal("schViewName"));
/*     */ 
/* 362 */     ViewData viewData = this.m_schemaView.getViewData();
/* 363 */     FilterUtils.createTopicEdits(viewData.m_viewName + ":filter", binder, defSet);
/*     */ 
/* 366 */     String whereClause = this.m_schemaView.buildSQL(filterData);
/* 367 */     binder.putLocal("whereClause", whereClause);
/*     */     try
/*     */     {
/* 370 */       executeService("GET_SCHEMA_VIEW_VALUES", binder, false);
/*     */ 
/* 372 */       if (this.m_cachedBinder != null)
/*     */       {
/* 375 */         SchemaEditHelper editHelper = new SchemaEditHelper();
/* 376 */         editHelper.validateViewData(binder, this.m_cachedBinder);
/*     */       }
/*     */       else
/*     */       {
/* 380 */         this.m_cachedBinder = binder;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 385 */       if (e instanceof ServiceException)
/*     */       {
/* 387 */         ServiceException se = (ServiceException)e;
/* 388 */         throw se;
/*     */       }
/* 390 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 394 */     UserData userData = AppLauncher.getUserData();
/* 395 */     this.m_docProfile.m_userData = userData;
/*     */ 
/* 397 */     return binder;
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataResultSet getMetaData()
/*     */   {
/* 407 */     return null;
/*     */   }
/*     */ 
/*     */   public SharedContext getSharedContext()
/*     */   {
/* 412 */     return this;
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder binder, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 421 */     if (this.m_isFatalError)
/*     */     {
/* 424 */       MessageBox.reportError(this.m_systemInterface, IdcMessageFactory.lc("apSchChangeDoRestart", new Object[0]));
/*     */     }
/*     */     else
/*     */     {
/* 428 */       AppLauncher.executeService(action, binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 434 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public void actionCommand(String cmd)
/*     */   {
/* 442 */     if (cmd.equals("add"))
/*     */     {
/* 444 */       addOrEdit(null, true);
/*     */     }
/* 446 */     else if (cmd.equals("addbatch"))
/*     */     {
/* 448 */       addBatchValues();
/*     */     }
/* 450 */     else if (cmd.equals("close"))
/*     */     {
/* 452 */       this.m_helper.close();
/*     */     }
/* 454 */     else if (cmd.equals("cancel"))
/*     */     {
/* 456 */       this.m_helper.m_result = 0;
/* 457 */       this.m_helper.close();
/*     */     }
/* 459 */     else if (cmd.equals("select"))
/*     */     {
/* 461 */       int index = this.m_schemaView.getList().getSelectedIndex();
/* 462 */       if (index >= 0)
/*     */       {
/* 464 */         this.m_helper.m_result = 1;
/* 465 */         this.m_helper.close();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 470 */       int selIndex = this.m_schemaView.getSelectedIndex();
/* 471 */       if (selIndex < 0)
/*     */       {
/* 474 */         return;
/*     */       }
/* 476 */       Properties props = this.m_schemaView.getDataAt(selIndex);
/* 477 */       if (cmd.equals("edit"))
/*     */       {
/* 479 */         addOrEdit(props, false);
/*     */       } else {
/* 481 */         if (!cmd.equals("delete"))
/*     */           return;
/* 483 */         delete(props);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEdit(Properties props, boolean isAdd)
/*     */   {
/* 490 */     String title = "apSchEditViewValueTitle";
/* 491 */     if (isAdd)
/*     */     {
/* 495 */       props = this.m_schemaView.populateFromFilter();
/* 496 */       title = "apSchAddViewValueTitle";
/*     */     }
/*     */ 
/* 499 */     AddViewValueDlg dlg = new AddViewValueDlg(this.m_systemInterface, this.m_systemInterface.getString(title), DialogHelpTable.getHelpPage("SchemaAddViewValue"));
/*     */ 
/* 502 */     String tableName = this.m_binder.getLocal("schTableName");
/* 503 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/* 504 */     String internalClmn = this.m_binder.getLocal("schInternalColumn");
/* 505 */     String primaryClmns = this.m_binder.getLocal("PrimaryColumns");
/* 506 */     int result = dlg.init(props, internalClmn, drset, this.m_schemaView.buildFilter(), isAdd, primaryClmns);
/* 507 */     if (result != 1)
/*     */       return;
/*     */     try
/*     */     {
/* 511 */       String action = "edit";
/* 512 */       if (isAdd)
/*     */       {
/* 514 */         action = "add";
/*     */       }
/* 516 */       doAction(props, action);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 520 */       String key = "apSchUnableToEditValue";
/* 521 */       if (isAdd)
/*     */       {
/* 523 */         key = "apSchUnableToAddValue";
/*     */       }
/* 525 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc(key, new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void delete(Properties props)
/*     */   {
/* 532 */     IdcMessage msg = IdcMessageFactory.lc("apSchVerifyDeleteValue", new Object[0]);
/* 533 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 2);
/* 534 */     if (result != 1)
/*     */     {
/* 536 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 540 */       doAction(props, "delete");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 544 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apSchUnableToDeleteValue", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addBatchValues()
/*     */   {
/* 550 */     Properties filterProps = this.m_schemaView.populateFromFilter();
/* 551 */     String title = this.m_systemInterface.getString("apSchBatchValueEditTitle");
/* 552 */     String helpPage = DialogHelpTable.getHelpPage("SchemaEditBatchValues");
/* 553 */     EditBatchModeDlg dlg = new EditBatchModeDlg(this.m_systemInterface, title, helpPage, this);
/*     */ 
/* 555 */     Vector filter = this.m_schemaView.buildFilter();
/* 556 */     String whereClause = this.m_schemaView.buildSQL(filter);
/* 557 */     Properties props = (Properties)this.m_binder.getLocalData().clone();
/* 558 */     props.put("whereClause", whereClause);
/* 559 */     if (dlg.init(props, filterProps, filter, (DataResultSet)this.m_schemaView.getList().getResultSet()) != 1) {
/*     */       return;
/*     */     }
/* 562 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   protected void doAction(Properties props, String action)
/*     */     throws ServiceException, DataException
/*     */   {
/* 569 */     PropParameters params = new PropParameters(props);
/*     */ 
/* 571 */     String tableName = this.m_binder.getLocal("schTableName");
/* 572 */     ResultSet drset = this.m_binder.getResultSet(tableName);
/*     */ 
/* 574 */     DataBinder binder = new DataBinder();
/* 575 */     binder.putLocal("schViewName", this.m_binder.getLocal("schViewName"));
/* 576 */     binder.putLocal("editViewValueAction", action);
/*     */ 
/* 578 */     DataResultSet valSet = new DataResultSet();
/* 579 */     valSet.copyFieldInfo(drset);
/* 580 */     Vector row = valSet.createRow(params);
/* 581 */     valSet.addRow(row);
/*     */ 
/* 583 */     binder.addResultSet(tableName, valSet);
/* 584 */     executeService("EDIT_SCHEMA_VIEW_VALUES", binder, false);
/*     */ 
/* 586 */     String selObj = null;
/* 587 */     if (!action.equals("delete"))
/*     */     {
/* 589 */       String internalClmn = this.m_binder.getLocal("schInternalColumn");
/* 590 */       selObj = props.getProperty(internalClmn);
/*     */     }
/* 592 */     refreshList(selObj);
/*     */   }
/*     */ 
/*     */   protected synchronized void refreshList(String selObj)
/*     */   {
/*     */     try
/*     */     {
/* 599 */       this.m_schemaView.refreshView(selObj);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 603 */       boolean isFatal = true;
/* 604 */       if (e instanceof ServiceException)
/*     */       {
/* 606 */         ServiceException se = (ServiceException)e;
/* 607 */         if (Errors.isNormalUserOperationalErrorCode(se.m_errorCode))
/*     */         {
/* 609 */           isFatal = false;
/* 610 */           MessageBox.reportError(this.m_systemInterface, e);
/*     */         }
/*     */       }
/* 613 */       if (isFatal)
/*     */       {
/* 616 */         IdcMessage errMsg = IdcMessageFactory.lc("apSchPleaseRestartEditSession", new Object[0]);
/* 617 */         MessageBox.reportError(this.m_systemInterface, e, errMsg);
/*     */       }
/* 619 */       if (!isFatal)
/*     */         return;
/* 621 */       this.m_isFatalError = isFatal;
/*     */     }
/*     */   }
/*     */ 
/*     */   public JFrame getMainWindow()
/*     */   {
/* 631 */     return this.m_systemInterface.getMainWindow();
/*     */   }
/*     */ 
/*     */   public void displayStatus(String str)
/*     */   {
/* 636 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public void displayStatus(IdcMessage msg)
/*     */   {
/* 641 */     String str = localizeMessage(msg);
/* 642 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public String getAppName()
/*     */   {
/* 647 */     return this.m_systemInterface.getAppName();
/*     */   }
/*     */ 
/*     */   public ExecutionContext getExecutionContext()
/*     */   {
/* 652 */     return this.m_systemInterface.getExecutionContext();
/*     */   }
/*     */ 
/*     */   public String localizeMessage(String msg)
/*     */   {
/* 657 */     return this.m_systemInterface.localizeMessage(msg);
/*     */   }
/*     */ 
/*     */   public String localizeMessage(IdcMessage msg)
/*     */   {
/* 662 */     return this.m_systemInterface.localizeMessage(msg);
/*     */   }
/*     */ 
/*     */   public String localizeCaption(String msg)
/*     */   {
/* 667 */     msg = LocaleUtils.encodeMessage("syCaptionWrapper", null, msg);
/*     */ 
/* 669 */     msg = this.m_systemInterface.localizeMessage(msg);
/* 670 */     return msg;
/*     */   }
/*     */ 
/*     */   public String getString(String str)
/*     */   {
/* 675 */     return this.m_systemInterface.getString(str);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getValidationErrorMessage(String fieldName, String value)
/*     */   {
/* 682 */     String msg = this.m_systemInterface.getValidationErrorMessage(fieldName, value);
/*     */ 
/* 684 */     return msg;
/*     */   }
/*     */ 
/*     */   public IdcMessage getValidationErrorMessageObject(String fieldName, String value, Map options)
/*     */   {
/* 689 */     IdcMessage msg = this.m_systemInterface.getValidationErrorMessageObject(fieldName, value, options);
/*     */ 
/* 691 */     return msg;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 696 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86058 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.EditViewValuesDlg
 * JD-Core Version:    0.5.4
 */