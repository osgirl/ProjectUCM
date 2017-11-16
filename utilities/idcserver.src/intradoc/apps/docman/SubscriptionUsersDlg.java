/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.DataRetrievalHelper;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.gui.iwt.UserDrawListItem;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.FilterUtils;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Dictionary;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SubscriptionUsersDlg
/*     */   implements ActionListener, SharedContext
/*     */ {
/*     */   public boolean m_useModalDialogs;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected String m_helpPage;
/*     */   protected String m_scpType;
/*     */   protected String[] m_scpFields;
/*     */   protected Properties m_scpInfo;
/*     */   protected String m_title;
/*     */   protected JDialog m_dialog;
/*     */   protected DialogHelper m_dialogHelper;
/*     */   protected DataBinder m_binder;
/*     */   protected UdlPanel m_list;
/*     */   protected Hashtable m_filterData;
/*     */   protected Properties m_filterProps;
/*     */   protected Hashtable m_fieldDefs;
/*     */   protected Hashtable m_fieldInfos;
/*     */   protected int m_subscriptionIdIndex;
/*     */   protected JCheckBox m_useFilterBox;
/*     */   protected JButton m_filterBtn;
/* 103 */   protected static Hashtable m_dialogs = new Hashtable();
/*     */ 
/*     */   public SubscriptionUsersDlg()
/*     */   {
/*  76 */     this.m_useModalDialogs = true;
/*     */ 
/*  91 */     this.m_filterData = new Hashtable();
/*  92 */     this.m_filterProps = new Properties();
/*     */ 
/*  95 */     this.m_fieldInfos = null;
/*     */   }
/*     */ 
/*     */   protected void copyShallow(SubscriptionUsersDlg that)
/*     */   {
/* 107 */     this.m_systemInterface = that.m_systemInterface;
/* 108 */     this.m_helpPage = that.m_helpPage;
/* 109 */     this.m_scpType = that.m_scpType;
/* 110 */     this.m_scpInfo = that.m_scpInfo;
/* 111 */     this.m_scpFields = that.m_scpFields;
/*     */ 
/* 113 */     this.m_title = that.m_title;
/* 114 */     this.m_dialog = that.m_dialog;
/* 115 */     this.m_dialogHelper = that.m_dialogHelper;
/* 116 */     this.m_binder = that.m_binder;
/* 117 */     this.m_list = that.m_list;
/* 118 */     this.m_filterData = that.m_filterData;
/*     */ 
/* 120 */     this.m_useFilterBox = that.m_useFilterBox;
/* 121 */     this.m_filterBtn = that.m_filterBtn;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sysint, Properties scpInfo, String helpPage)
/*     */     throws DataException, ServiceException
/*     */   {
/* 127 */     this.m_cxt = sysint.getExecutionContext();
/* 128 */     this.m_scpInfo = scpInfo;
/* 129 */     this.m_scpType = scpInfo.getProperty("scpType");
/* 130 */     SubscriptionUsersDlg existing = (SubscriptionUsersDlg)m_dialogs.get(this.m_scpType);
/*     */ 
/* 132 */     if (existing != null)
/*     */     {
/* 134 */       copyShallow(existing);
/* 135 */       this.m_dialog.setVisible(true);
/* 136 */       return;
/*     */     }
/*     */ 
/* 139 */     m_dialogs.put(this.m_scpType, this);
/* 140 */     this.m_systemInterface = sysint;
/* 141 */     this.m_helpPage = helpPage;
/*     */ 
/* 143 */     this.m_title = LocaleResources.getString("apSubscriptionUsersTitle", this.m_cxt, this.m_scpType);
/* 144 */     this.m_dialogHelper = new DialogHelper();
/*     */ 
/* 146 */     Vector v = StringUtils.parseArray(this.m_scpInfo.getProperty("scpFields"), ',', ',');
/* 147 */     int length = v.size();
/* 148 */     this.m_scpFields = new String[length];
/* 149 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 151 */       this.m_scpFields[i] = ((String)v.elementAt(i));
/*     */     }
/*     */ 
/* 154 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI() throws DataException, ServiceException
/*     */   {
/* 159 */     JPanel filterControls = new PanePanel();
/* 160 */     this.m_useFilterBox = new JCheckBox(LocaleResources.getString("apUseFilterBoxLabel", this.m_cxt));
/*     */ 
/* 162 */     this.m_filterBtn = new JButton(LocaleResources.getString("apDefineFilterButtonLabel", this.m_cxt));
/*     */ 
/* 164 */     filterControls.add(this.m_useFilterBox);
/* 165 */     filterControls.add(this.m_filterBtn);
/*     */ 
/* 167 */     ItemListener itemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent event)
/*     */       {
/* 171 */         if (event.getSource() != SubscriptionUsersDlg.this.m_useFilterBox)
/*     */           return;
/*     */         try
/*     */         {
/* 175 */           SubscriptionUsersDlg.this.refreshList();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 179 */           MessageBox.reportError(SubscriptionUsersDlg.this.m_systemInterface, e);
/*     */         }
/*     */       }
/*     */     };
/* 185 */     ActionListener actionListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent event)
/*     */       {
/* 189 */         SubscriptionUsersFilterDlg dlg = new SubscriptionUsersFilterDlg(SubscriptionUsersDlg.this.m_systemInterface, LocaleResources.getString("apDefineFilterButtonLabel", SubscriptionUsersDlg.this.m_cxt), DialogHelpTable.getHelpPage("SubscriptionFilter"), SubscriptionUsersDlg.this);
/*     */ 
/* 193 */         dlg.addIgnoreField("dSubscriptionType");
/* 194 */         dlg.init(SubscriptionUsersDlg.this.m_filterData, SubscriptionUsersDlg.this.m_filterProps);
/* 195 */         if ((dlg.prompt() != 1) || (!SubscriptionUsersDlg.this.m_useFilterBox.isSelected()))
/*     */           return;
/*     */         try
/*     */         {
/* 199 */           SubscriptionUsersDlg.this.refreshList();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 203 */           MessageBox.reportError(SubscriptionUsersDlg.this.m_systemInterface, e);
/*     */         }
/*     */       }
/*     */     };
/* 209 */     DataRetrievalHelper dataHelper = new Object()
/*     */     {
/*     */       public Object get(Object source, Object key)
/*     */       {
/* 213 */         Object rc = null;
/* 214 */         Dictionary data = (Dictionary)((UserDrawListItem)source).getData();
/* 215 */         if (key instanceof ColumnInfo)
/*     */         {
/* 217 */           ColumnInfo cinfo = (ColumnInfo)key;
/* 218 */           String[] scpFields = SubscriptionUsersDlg.this.m_scpFields;
/* 219 */           for (int i = 0; i < scpFields.length; ++i)
/*     */           {
/* 221 */             if (!cinfo.m_fieldId.equals(scpFields[i]))
/*     */               continue;
/* 223 */             String tmp = (String)data.get("dSubscriptionID");
/* 224 */             Vector v = StringUtils.parseArray(tmp, ',', '^');
/* 225 */             rc = v.elementAt(i);
/* 226 */             break;
/*     */           }
/*     */ 
/* 230 */           if (rc == null)
/*     */           {
/* 232 */             if (cinfo.m_fieldId.equals("dSubscriptionAlias"))
/*     */             {
/* 234 */               String name = (String)data.get("dSubscriptionAlias");
/* 235 */               String type = (String)data.get("dSubscriptionAliasType");
/* 236 */               rc = name + " (" + type + ")";
/*     */             }
/*     */             else
/*     */             {
/* 240 */               rc = data.get(cinfo.m_fieldId);
/*     */             }
/*     */           }
/*     */         }
/* 244 */         else if (!key instanceof Object[]);
/* 263 */         return rc;
/*     */       }
/*     */     };
/* 267 */     this.m_useFilterBox.addItemListener(itemListener);
/* 268 */     this.m_filterBtn.addActionListener(actionListener);
/*     */ 
/* 270 */     this.m_list = new UdlPanel(LocaleResources.getString("apSubscribersTitle", this.m_cxt), dataHelper, 400, 18, "USER_LIST", true);
/*     */ 
/* 272 */     String fieldList = "dSubscriptionID,dSubscriptionAliasType,dSubscriptionAlias";
/* 273 */     this.m_subscriptionIdIndex = 0;
/* 274 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apSubscriptionCriteriaColumnLabel", this.m_cxt), "dSubscriptionID", 10.0D);
/*     */ 
/* 276 */     info.m_isVisible = false;
/* 277 */     this.m_list.setColumnInfo(info);
/* 278 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionUserAliasColumnLabel", this.m_cxt), "dSubscriptionAlias", 8.0D));
/*     */ 
/* 280 */     info = new ColumnInfo(LocaleResources.getString("apSubscriptionAliasTypeColumnLabel", this.m_cxt), "dSubscriptionAliasType", 5.0D);
/*     */ 
/* 282 */     info.m_isVisible = false;
/* 283 */     this.m_list.setColumnInfo(info);
/* 284 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionCreateDateColumnLabel", this.m_cxt), "dSubscriptionCreateDate", 6.0D));
/*     */ 
/* 287 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionNotifyDateColumnLabel", this.m_cxt), "dSubscriptionNotifyDate", 6.0D));
/*     */ 
/* 290 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionAccessDateColumnLabel", this.m_cxt), "dSubscriptionUsedDate", 6.0D));
/*     */ 
/* 292 */     this.m_list.addItemListener(itemListener);
/* 293 */     this.m_list.useDefaultListener();
/*     */ 
/* 296 */     ViewFields columnFields = new ViewFields(this.m_cxt);
/* 297 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */     try
/*     */     {
/* 301 */       Vector v = columnFields.createAllDocumentFieldsList(drset, true, false, true, true);
/* 302 */       int length = v.size();
/* 303 */       this.m_fieldDefs = new Hashtable();
/* 304 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 306 */         ViewFieldDef def = (ViewFieldDef)v.elementAt(i);
/* 307 */         this.m_fieldDefs.put(def.m_name, def);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 312 */       if (SystemUtils.m_verbose)
/*     */       {
/* 314 */         Report.debug("applet", null, e);
/*     */       }
/*     */     }
/*     */ 
/* 318 */     for (int i = 0; i < this.m_scpFields.length; ++i)
/*     */     {
/* 320 */       String fieldName = this.m_scpFields[i];
/* 321 */       FieldDef def = (FieldDef)this.m_fieldDefs.get(fieldName);
/* 322 */       String fieldDescript = fieldName + ":";
/* 323 */       if (def != null)
/*     */       {
/* 325 */         fieldDescript = LocaleResources.getString(def.m_caption, this.m_cxt);
/*     */       }
/* 327 */       this.m_list.setColumnInfo(new ColumnInfo(fieldDescript, fieldName, 5.0D));
/* 328 */       fieldList = fieldList + "," + fieldName;
/*     */     }
/*     */ 
/* 331 */     fieldList = fieldList + ",dSubscriptionCreateDate,dSubscriptionNotifyDate,dSubscriptionUsedDate";
/* 332 */     this.m_list.setVisibleColumns(fieldList);
/* 333 */     this.m_list.m_list.addActionListener(this);
/* 334 */     this.m_list.init();
/*     */ 
/* 336 */     if (this.m_useModalDialogs)
/*     */     {
/* 338 */       this.m_dialog = new CustomDialog(this.m_systemInterface.getMainWindow(), this.m_title, true);
/*     */     }
/*     */     else
/*     */     {
/* 347 */       this.m_dialog = new CustomDialog(this.m_systemInterface.getMainWindow(), this.m_title, false)
/*     */       {
/*     */         public void dispose()
/*     */         {
/* 352 */           SubscriptionUsersDlg.m_dialogs.remove(SubscriptionUsersDlg.this.m_scpType);
/* 353 */           super.dispose();
/*     */         }
/*     */       };
/* 357 */       m_dialogs.put(this.m_scpType, this.m_dialog);
/*     */     }
/*     */ 
/* 360 */     this.m_dialogHelper.attachToDialog(this.m_dialog, this.m_systemInterface, null);
/* 361 */     this.m_dialog.add("North", filterControls);
/* 362 */     this.m_dialog.add("Center", this.m_list);
/*     */ 
/* 365 */     String[][] buttonInfo = { { "add", "apAddSubscription", "0" }, { "delete", "apDeleteSubscription", "1" }, { "viewDocs", "apSubscriptionViewItems", "1" }, { "refresh", "apLabelRefresh", "0" }, { "close", "apLabelClose", "0" } };
/*     */ 
/* 373 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 375 */       String cmd = buttonInfo[i][0];
/*     */ 
/* 377 */       JButton btn = null;
/* 378 */       String label = LocaleResources.getString(buttonInfo[i][1], this.m_cxt);
/* 379 */       if (cmd.equals("close"))
/*     */       {
/* 381 */         btn = this.m_dialogHelper.addCommandButton(label, this.m_dialogHelper);
/*     */       }
/*     */       else
/*     */       {
/* 385 */         btn = this.m_dialogHelper.addCommandButton(label, this);
/*     */       }
/* 387 */       btn.setActionCommand(cmd);
/* 388 */       boolean isListControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 389 */       boolean isViewDocsFolderSubscription = (this.m_scpType != null) && (this.m_scpType.equalsIgnoreCase("Folder")) && (cmd.equals("viewDocs"));
/* 390 */       if (!isListControlled) {
/*     */         continue;
/*     */       }
/* 393 */       if (!isViewDocsFolderSubscription)
/*     */       {
/* 395 */         this.m_list.addControlComponent(btn);
/*     */       }
/* 397 */       btn.setEnabled(false);
/*     */     }
/*     */ 
/* 401 */     refreshList();
/* 402 */     this.m_dialogHelper.prompt();
/*     */   }
/*     */ 
/*     */   public static void clearDialogs()
/*     */   {
/* 407 */     m_dialogs.clear();
/*     */   }
/*     */ 
/*     */   protected void setupRefreshQuery(DataBinder binder)
/*     */   {
/* 412 */     this.m_binder.putLocal("dataSource", "Subscriptions");
/* 413 */     this.m_binder.putLocal("MaxQueryRows", "" + SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500));
/*     */ 
/* 415 */     this.m_binder.putLocal("resultName", "USER_LIST");
/*     */   }
/*     */ 
/*     */   public void refreshList() throws ServiceException
/*     */   {
/* 420 */     String whereClause = "dSubscriptionType = '" + StringUtils.createQuotableString(this.m_scpType) + "'";
/*     */ 
/* 422 */     if (this.m_useFilterBox.isSelected())
/*     */     {
/* 424 */       String filterClause = FilterUtils.buildWhereClause(this.m_filterData);
/* 425 */       if ((filterClause == null) || (filterClause.length() > 0))
/*     */       {
/* 427 */         whereClause = whereClause + " AND " + filterClause;
/*     */       }
/*     */     }
/* 430 */     this.m_binder = new DataBinder();
/*     */ 
/* 434 */     this.m_binder.putLocal("whereClause", whereClause);
/* 435 */     setupRefreshQuery(this.m_binder);
/*     */ 
/* 438 */     this.m_binder.putLocal("dSubscriptionType", this.m_scpType);
/*     */ 
/* 440 */     AppLauncher.executeService("GET_DATARESULTSET", this.m_binder, this.m_systemInterface);
/*     */ 
/* 443 */     doRefresh(this.m_binder, null);
/*     */   }
/*     */ 
/*     */   protected void doRefresh(DataBinder binder, String selected)
/*     */   {
/* 448 */     if (this.m_fieldInfos == null)
/*     */     {
/* 450 */       this.m_fieldInfos = new Hashtable();
/* 451 */       ResultSet rset = this.m_binder.getResultSet("USER_LIST");
/* 452 */       int length = rset.getNumFields();
/* 453 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 455 */         FieldInfo info = new FieldInfo();
/* 456 */         rset.getIndexFieldInfo(i, info);
/* 457 */         this.m_fieldInfos.put(info.m_name, info);
/*     */       }
/*     */     }
/*     */ 
/* 461 */     if (selected == null)
/*     */     {
/* 463 */       selected = this.m_list.getSelectedObj();
/*     */     }
/* 465 */     this.m_list.refreshList(binder, selected);
/*     */   }
/*     */ 
/*     */   public boolean addSubscription(Properties newProps)
/*     */   {
/* 470 */     String scpType = newProps.getProperty("scpType");
/* 471 */     String scpFields = newProps.getProperty("scpFields");
/*     */ 
/* 474 */     newProps.put("dSubscriptionType", scpType);
/*     */ 
/* 477 */     Vector fields = StringUtils.parseArray(scpFields, ',', ',');
/* 478 */     int length = fields.size();
/* 479 */     String scpId = "";
/* 480 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 482 */       String fieldName = (String)fields.elementAt(i);
/* 483 */       String fieldValue = newProps.getProperty(fieldName);
/* 484 */       scpId = scpId + fieldValue;
/* 485 */       if (i + 1 >= length)
/*     */         continue;
/* 487 */       scpId = scpId + ",";
/*     */     }
/*     */ 
/* 490 */     newProps.put("dSubscriptionID", scpId);
/*     */ 
/* 493 */     if (newProps.get("dSubscriptionEmail") == null)
/*     */     {
/* 495 */       newProps.put("dSubscriptionEmail", "");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 500 */       String whereClause = "dSubscriptionType = '" + StringUtils.createQuotableString(this.m_scpType) + "'";
/*     */ 
/* 502 */       String filterClause = FilterUtils.buildWhereClause(this.m_filterData);
/* 503 */       if ((filterClause != null) && (filterClause.length() > 0))
/*     */       {
/* 505 */         whereClause = whereClause + " AND " + filterClause;
/*     */       }
/* 507 */       this.m_binder.setLocalData(newProps);
/* 508 */       this.m_binder.putLocal("whereClause", whereClause);
/* 509 */       setupRefreshQuery(this.m_binder);
/* 510 */       AppLauncher.executeService("SUBSCRIBE_EX", this.m_binder);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 514 */       MessageBox.reportError(this.m_systemInterface, e);
/* 515 */       return false;
/*     */     }
/*     */ 
/* 518 */     String alias = this.m_binder.getLocal("dSubscriptionAlias");
/* 519 */     doRefresh(this.m_binder, alias);
/* 520 */     return true;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 526 */     String cmd = event.getActionCommand();
/*     */     try
/*     */     {
/* 530 */       int index = this.m_list.getSelectedIndex();
/*     */ 
/* 532 */       if (cmd.equals("add"))
/*     */       {
/* 534 */         String title = LocaleResources.getString("apAddSubscriptionTitle", this.m_cxt);
/* 535 */         AddUserSubscription dlg = new AddUserSubscription(this.m_systemInterface, this, title, DialogHelpTable.getHelpPage("AddUserSubscription"));
/*     */ 
/* 537 */         DataResultSet drset = SharedObjects.getTable("SubscriptionTypes");
/* 538 */         drset.findRow(0, this.m_scpType);
/* 539 */         Properties props = drset.getCurrentRowProps();
/* 540 */         DialogCallback callback = new DialogCallback(props)
/*     */         {
/*     */           public boolean handleDialogEvent(ActionEvent e)
/*     */           {
/* 545 */             return SubscriptionUsersDlg.this.addSubscription(this.val$props);
/*     */           }
/*     */         };
/* 548 */         dlg.init(callback, props);
/* 549 */         dlg.prompt();
/*     */       }
/* 551 */       else if (cmd.equals("refresh"))
/*     */       {
/* 553 */         refreshList();
/*     */       }
/* 555 */       else if (index >= 0)
/*     */       {
/* 557 */         if (cmd.equals("delete"))
/*     */         {
/* 560 */           Properties props = this.m_list.getDataAt(index);
/*     */ 
/* 562 */           if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apSubscriptionDelete", new Object[0]), 4) == 2)
/*     */           {
/* 566 */             String whereClause = "dSubscriptionType = '" + StringUtils.createQuotableString(this.m_scpType) + "'";
/*     */ 
/* 568 */             String filterClause = FilterUtils.buildWhereClause(this.m_filterData);
/* 569 */             if ((filterClause == null) || (filterClause.length() > 0))
/*     */             {
/* 571 */               whereClause = whereClause + " AND " + filterClause;
/*     */             }
/* 573 */             this.m_binder.setLocalData(props);
/* 574 */             this.m_binder.putLocal("whereClause", whereClause);
/* 575 */             setupRefreshQuery(this.m_binder);
/* 576 */             AppLauncher.executeService("UNSUBSCRIBE_FROM_LIST_EX", this.m_binder);
/* 577 */             doRefresh(this.m_binder, null);
/*     */           }
/*     */         }
/* 580 */         else if (cmd.equals("viewDocs"))
/*     */         {
/* 582 */           ViewData viewData = new ViewData(1, "DocSubscriptionList", "DOCUMENT_LIST");
/* 583 */           viewData.m_action = "DOC_SUBS_LIST";
/* 584 */           viewData.m_inDateState = false;
/*     */ 
/* 586 */           String title = LocaleResources.getString("apSubscriptionDocumentsTitle", this.m_cxt);
/* 587 */           ViewDlg dlg = new ViewDlg(this.m_dialog, this.m_systemInterface, title, this, DialogHelpTable.getHelpPage("SubscriptionDocList"));
/*     */ 
/* 589 */           dlg.init(viewData, null, this.m_list.getDataAt(index));
/* 590 */           dlg.prompt();
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 596 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder data, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 606 */     AppLauncher.executeService(action, data);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 611 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 617 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98371 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.SubscriptionUsersDlg
 * JD-Core Version:    0.5.4
 */