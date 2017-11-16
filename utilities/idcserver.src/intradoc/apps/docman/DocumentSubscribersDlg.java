/*     */ package intradoc.apps.docman;
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
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.DataRetrievalHelper;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.gui.iwt.UserDrawListItem;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.shared.gui.FilterUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
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
/*     */ import javax.swing.JList;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocumentSubscribersDlg
/*     */   implements ActionListener, ItemListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected SharedContext m_shContext;
/*  81 */   protected String m_title = "apSubscribersTitle";
/*     */   protected String m_helpPage;
/*  84 */   protected Hashtable m_filterData = new Hashtable();
/*  85 */   protected boolean m_filterEnabled = false;
/*     */   protected UdlPanel m_list;
/*     */   protected JCheckBox m_useFilterBox;
/*  90 */   protected static Properties m_docCaptionMap = null;
/*     */   protected Properties m_docProps;
/*     */   protected DataBinder m_binder;
/*  95 */   protected static Hashtable m_dlgTable = new Hashtable();
/*     */ 
/*     */   public DocumentSubscribersDlg(SystemInterface sys, String helpPage, SharedContext shContext)
/*     */   {
/*  99 */     this.m_systemInterface = sys;
/* 100 */     this.m_helpPage = helpPage;
/* 101 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/* 102 */     this.m_shContext = shContext;
/*     */   }
/*     */ 
/*     */   public void copyShallow(DocumentSubscribersDlg that)
/*     */   {
/* 107 */     this.m_systemInterface = that.m_systemInterface;
/* 108 */     this.m_title = that.m_title;
/* 109 */     this.m_helpPage = that.m_helpPage;
/* 110 */     this.m_docProps = that.m_docProps;
/* 111 */     this.m_binder = that.m_binder;
/*     */ 
/* 113 */     this.m_helper = that.m_helper;
/* 114 */     this.m_list = that.m_list;
/*     */ 
/* 116 */     this.m_filterEnabled = that.m_filterEnabled;
/*     */   }
/*     */ 
/*     */   public void init(Properties props)
/*     */   {
/* 121 */     this.m_title = LocaleResources.getString(this.m_title, this.m_cxt);
/*     */ 
/* 123 */     String dDocName = props.getProperty("dDocName");
/* 124 */     if (dDocName != null)
/*     */     {
/* 126 */       DocumentSubscribersDlg dlg = (DocumentSubscribersDlg)m_dlgTable.get(dDocName);
/*     */ 
/* 128 */       if (dlg != null)
/*     */       {
/* 130 */         dlg.m_docProps = props;
/* 131 */         copyShallow(dlg);
/* 132 */         this.m_helper.m_dialog.setVisible(true);
/* 133 */         return;
/*     */       }
/* 135 */       m_dlgTable.put(dDocName, this);
/*     */     }
/*     */ 
/* 138 */     this.m_docProps = props;
/* 139 */     this.m_helper = new DialogHelper(this.m_systemInterface, this.m_title, false);
/* 140 */     JDialog dialog = new CustomDialog(this.m_systemInterface.getMainWindow(), true)
/*     */     {
/*     */       public void dispose()
/*     */       {
/* 145 */         DocumentSubscribersDlg.m_dlgTable.remove(DocumentSubscribersDlg.this.m_docProps.get("dDocName"));
/* 146 */         super.dispose();
/*     */       }
/*     */     };
/* 149 */     this.m_helper.attachToDialog(dialog, this.m_systemInterface, this.m_docProps);
/*     */ 
/* 151 */     DataRetrievalHelper dataHelper = new Object()
/*     */     {
/*     */       public Object get(Object source, Object key)
/*     */       {
/* 155 */         Object rc = null;
/* 156 */         Dictionary data = (Dictionary)((UserDrawListItem)source).getData();
/* 157 */         if (key instanceof ColumnInfo)
/*     */         {
/* 159 */           ColumnInfo cinfo = (ColumnInfo)key;
/* 160 */           if (cinfo.m_fieldId.equals("dSubscriptionAlias"))
/*     */           {
/* 162 */             rc = data.get("dSubscriptionAlias") + " (" + data.get("dSubscriptionAliasType") + ")";
/*     */           }
/*     */           else
/*     */           {
/* 167 */             rc = data.get(cinfo.m_fieldId);
/*     */           }
/*     */         }
/* 170 */         return rc;
/*     */       }
/*     */     };
/* 174 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 175 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 177 */     this.m_list = new UdlPanel(LocaleResources.getString("apSubscriptionsTitle", this.m_cxt), dataHelper, 350, 15, "SUBSCRIPTIONS", true);
/*     */ 
/* 179 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apSubscriptionAliasTypeColumnLabel", this.m_cxt), "dSubscriptionAliasType", 3.0D);
/*     */ 
/* 181 */     info.m_isVisible = false;
/* 182 */     this.m_list.setColumnInfo(info);
/* 183 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionUserAliasColumnLabel", this.m_cxt), "dSubscriptionAlias", 10.0D));
/*     */ 
/* 185 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionTypeColumnLabel", this.m_cxt), "dSubscriptionType", 6.0D));
/*     */ 
/* 187 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionCreateDateColumnLabel", this.m_cxt), "dSubscriptionCreateDate", 8.0D));
/*     */ 
/* 189 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionNotifyDateColumnLabel", this.m_cxt), "dSubscriptionNotifyDate", 8.0D));
/*     */ 
/* 191 */     this.m_list.setColumnInfo(new ColumnInfo(LocaleResources.getString("apSubscriptionAccessDateColumnLabel", this.m_cxt), "dSubscriptionUsedDate", 8.0D));
/*     */ 
/* 193 */     String columnList = "dSubscriptionAliasType,dSubscriptionAlias,dSubscriptionType,dSubscriptionCreateDate,dSubscriptionNotifyDate,dSubscriptionUsedDate";
/*     */ 
/* 195 */     this.m_list.init();
/* 196 */     this.m_list.setVisibleColumns(columnList);
/*     */ 
/* 198 */     this.m_list.useDefaultListener();
/* 199 */     this.m_list.m_list.addActionListener(this);
/*     */ 
/* 201 */     String[][] buttonInfo = { { "unsubscribe", "apUnsubscribe", "1" }, { "viewDetails", "apViewSubscriptionDetails", "1" } };
/*     */ 
/* 207 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 209 */       boolean isControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 210 */       String label = LocaleResources.getString(buttonInfo[i][1], this.m_cxt);
/* 211 */       JButton btn = this.m_list.addButton(label, isControlled);
/* 212 */       btn.setActionCommand(buttonInfo[i][0]);
/* 213 */       this.m_helper.addCommandButtonEx(btn, this);
/*     */     }
/*     */ 
/* 217 */     this.m_helper.m_ok = this.m_helper.addCommandButton(LocaleResources.getString("apLabelClose", this.m_cxt), this.m_helper);
/*     */ 
/* 220 */     GridBagConstraints gbc = this.m_helper.m_gridHelper.m_gc;
/* 221 */     gbc.insets.left = (gbc.insets.right = 10);
/* 222 */     gbc.weightx = 1.0D;
/* 223 */     JPanel filterPanel = new PanePanel();
/* 224 */     filterPanel.setLayout(new GridBagLayout());
/* 225 */     this.m_helper.addLastComponentInRow(mainPanel, filterPanel);
/* 226 */     gbc.weighty = 1.0D;
/* 227 */     this.m_helper.addComponent(mainPanel, this.m_list);
/* 228 */     gbc.insets.left = (gbc.insets.right = 0);
/*     */ 
/* 231 */     gbc.weightx = (gbc.weighty = 0.0D);
/* 232 */     this.m_useFilterBox = new JCheckBox(LocaleResources.getString("apUseFilterBoxLabel", this.m_cxt));
/*     */ 
/* 234 */     JButton defineFilter = new JButton(LocaleResources.getString("apDefineFilterButtonLabel", this.m_cxt));
/*     */ 
/* 236 */     defineFilter.setActionCommand("defineFilter");
/*     */ 
/* 238 */     filterPanel.add(this.m_useFilterBox, gbc);
/* 239 */     filterPanel.add(defineFilter, gbc);
/*     */ 
/* 241 */     this.m_useFilterBox.addItemListener(this);
/* 242 */     defineFilter.addActionListener(this);
/*     */     try
/*     */     {
/* 246 */       refresh();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 250 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */ 
/* 253 */     prompt();
/*     */   }
/*     */ 
/*     */   public void refresh() throws ServiceException
/*     */   {
/* 258 */     this.m_binder = new DataBinder();
/* 259 */     this.m_binder.setLocalData(this.m_docProps);
/* 260 */     if (this.m_filterEnabled)
/*     */     {
/* 262 */       String whereClause = FilterUtils.buildWhereClause(this.m_filterData);
/* 263 */       this.m_binder.putLocal("whereClause", whereClause);
/*     */     }
/*     */     else
/*     */     {
/* 267 */       this.m_binder.removeLocal("whereClause");
/*     */     }
/* 269 */     AppLauncher.executeService("GET_DOC_SUBSCRIBERS", this.m_binder);
/*     */ 
/* 271 */     doRefresh();
/*     */   }
/*     */ 
/*     */   protected void doRefresh()
/*     */   {
/* 276 */     String selected = this.m_list.getSelectedObj();
/* 277 */     this.m_list.refreshList(this.m_binder, selected);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 282 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 290 */     Object source = event.getSource();
/*     */ 
/* 292 */     if (source instanceof JButton)
/*     */     {
/* 294 */       JButton btn = (JButton)source;
/* 295 */       String cmd = btn.getActionCommand();
/* 296 */       if (cmd.equals("defineFilter"))
/*     */       {
/* 298 */         SubscriptionUsersFilterDlg dlg = new SubscriptionUsersFilterDlg(this.m_systemInterface, "Subscriptions Filter", null, this.m_shContext);
/*     */ 
/* 300 */         Vector scpTypes = new IdcVector();
/* 301 */         DataResultSet drset = SharedObjects.getTable("SubscriptionTypes");
/* 302 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 304 */           String type = ResultSetUtils.getValue(drset, "scpType");
/* 305 */           scpTypes.addElement(type);
/*     */         }
/*     */ 
/* 308 */         dlg.addOptionList("subscriptionTypes", scpTypes);
/* 309 */         dlg.init(this.m_filterData, this.m_helper.m_props);
/* 310 */         if ((dlg.prompt() == 1) && (this.m_useFilterBox.isSelected()))
/*     */         {
/*     */           try
/*     */           {
/* 314 */             refresh();
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 318 */             MessageBox.reportError(this.m_systemInterface, e);
/*     */           }
/*     */         }
/*     */       }
/* 322 */       else if (cmd.equals("viewDetails"))
/*     */       {
/* 324 */         subscriptionDetails();
/*     */       }
/* 326 */       else if (cmd.equals("unsubscribe"))
/*     */       {
/* 328 */         int index = this.m_list.getSelectedIndex();
/* 329 */         if (index >= 0)
/*     */         {
/* 332 */           Properties props = this.m_list.getDataAt(index);
/* 333 */           String scpType = props.getProperty("dSubscriptionType");
/*     */ 
/* 335 */           if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifySubscriptionDelete", new Object[0]), 4) == 2)
/*     */           {
/*     */             try
/*     */             {
/* 341 */               String whereClause = "dSubscriptionType = '" + scpType + "'";
/* 342 */               String filterClause = FilterUtils.buildWhereClause(this.m_filterData);
/* 343 */               if ((filterClause != null) && (filterClause.length() > 0))
/*     */               {
/* 345 */                 whereClause = whereClause + " AND " + filterClause;
/*     */               }
/* 347 */               this.m_binder.setLocalData(props);
/* 348 */               this.m_binder.putLocal("whereClause", whereClause);
/*     */ 
/* 350 */               AppLauncher.executeService("UNSUBSCRIBE_FROM_LIST", this.m_binder);
/* 351 */               refresh();
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 355 */               MessageBox.reportError(this.m_systemInterface, e);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 361 */     else if (source instanceof JList)
/*     */     {
/* 363 */       subscriptionDetails();
/*     */     } else {
/* 365 */       if (!source instanceof FixedSizeList)
/*     */         return;
/* 367 */       subscriptionDetails();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void subscriptionDetails()
/*     */   {
/* 373 */     int index = this.m_list.getSelectedIndex();
/* 374 */     if (index < 0)
/*     */       return;
/* 376 */     if (m_docCaptionMap == null)
/*     */     {
/* 379 */       m_docCaptionMap = new Properties();
/* 380 */       ViewFields fields = new ViewFields(this.m_cxt);
/* 381 */       fields.addStandardDocFields();
/* 382 */       DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */       try
/*     */       {
/* 385 */         fields.addMetaFields(drset);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 389 */         e.printStackTrace();
/*     */       }
/* 391 */       int length = fields.m_viewFields.size();
/* 392 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 394 */         ViewFieldDef def = (ViewFieldDef)fields.m_viewFields.elementAt(i);
/* 395 */         m_docCaptionMap.put(def.m_name, def);
/*     */       }
/*     */     }
/* 398 */     Properties scpProps = this.m_list.getDataAt(index);
/* 399 */     showSubscriptionDetails(scpProps, this.m_docProps, m_docCaptionMap);
/*     */   }
/*     */ 
/*     */   protected void showSubscriptionDetails(Properties scpProps, Properties docProps, Properties docCaptionMap)
/*     */   {
/* 407 */     String dDocName = docProps.getProperty("dDocName");
/* 408 */     String userAliasName = scpProps.getProperty("dSubscriptionAlias");
/* 409 */     String userAliasType = scpProps.getProperty("dSubscriptionAliasType");
/* 410 */     String scpType = scpProps.getProperty("dSubscriptionType");
/*     */ 
/* 412 */     DetailKey key = new DetailKey(dDocName, userAliasName, userAliasType, scpType);
/*     */ 
/* 414 */     JDialog dlg = (JDialog)m_dlgTable.get(key);
/* 415 */     if (dlg != null)
/*     */     {
/* 417 */       dlg.setVisible(true);
/* 418 */       return;
/*     */     }
/*     */ 
/* 421 */     String userAliasLabel = (userAliasType.equals("user")) ? "apLabelUser" : "apLabelAlias";
/* 422 */     userAliasLabel = LocaleResources.getString(userAliasLabel, this.m_systemInterface.getExecutionContext());
/*     */ 
/* 424 */     DialogHelper dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("apSubscriptionDetailTitle", this.m_cxt, dDocName), true);
/*     */ 
/* 426 */     dlg = new CustomDialog((JFrame)this.m_helper.m_dialog.getParent(), true, key)
/*     */     {
/*     */       public void dispose()
/*     */       {
/* 431 */         DocumentSubscribersDlg.m_dlgTable.remove(this.val$key);
/* 432 */         super.dispose();
/*     */       }
/*     */     };
/* 435 */     m_dlgTable.put(key, dlg);
/*     */ 
/* 438 */     dlgHelper.attachToDialog(dlg, this.m_systemInterface, scpProps);
/* 439 */     GridBagConstraints gbc = dlgHelper.m_gridHelper.m_gc;
/* 440 */     gbc.insets = new Insets(5, 10, 5, 10);
/* 441 */     JPanel dlgPanel = new PanePanel();
/* 442 */     dlgHelper.m_mainPanel.setLayout(new GridBagLayout());
/* 443 */     gbc.weightx = 1.0D;
/* 444 */     gbc.fill = 1;
/* 445 */     gbc.anchor = 17;
/* 446 */     dlgHelper.addComponent(dlgHelper.m_mainPanel, dlgPanel);
/* 447 */     gbc.insets = new Insets(0, 2, 0, 2);
/*     */ 
/* 450 */     dlgPanel.setLayout(new GridBagLayout());
/* 451 */     JPanel userPanel = new PanePanel(8);
/* 452 */     JPanel criteriaPanel = new PanePanel(8);
/* 453 */     JPanel datePanel = new PanePanel(8);
/* 454 */     dlgHelper.addLastComponentInRow(dlgPanel, userPanel);
/* 455 */     dlgHelper.addLastComponentInRow(dlgPanel, criteriaPanel);
/* 456 */     dlgHelper.addLastComponentInRow(dlgPanel, datePanel);
/*     */ 
/* 462 */     userPanel.setLayout(new GridBagLayout());
/* 463 */     gbc.weightx = 0.0D;
/* 464 */     CustomLabel label = new CustomLabel(userAliasLabel + ":", 1);
/* 465 */     dlgHelper.addComponent(userPanel, label);
/* 466 */     gbc.weightx = 1.0D;
/* 467 */     label = new CustomLabel(scpProps.getProperty("dSubscriptionAlias"));
/* 468 */     dlgHelper.addLastComponentInRow(userPanel, label);
/*     */ 
/* 471 */     criteriaPanel.setLayout(new GridBagLayout());
/* 472 */     gbc.weightx = 0.0D;
/* 473 */     label = new CustomLabel(LocaleResources.getString("apSubscriptionTypeCaption", this.m_cxt), 1);
/*     */ 
/* 475 */     dlgHelper.addComponent(criteriaPanel, label);
/* 476 */     gbc.weightx = 1.0D;
/* 477 */     label = new CustomLabel(scpType);
/* 478 */     dlgHelper.addLastComponentInRow(criteriaPanel, label);
/* 479 */     int left = gbc.insets.left;
/* 480 */     Vector fieldList = getSubscriptionFields(scpType);
/* 481 */     int length = fieldList.size();
/* 482 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 484 */       String name = (String)fieldList.elementAt(i);
/* 485 */       FieldDef def = (FieldDef)docCaptionMap.get(name);
/* 486 */       String caption = name + ":";
/* 487 */       if (def != null)
/*     */       {
/* 489 */         caption = LocaleResources.getString(def.m_caption, this.m_cxt);
/*     */       }
/* 491 */       String value = docProps.getProperty(name);
/* 492 */       gbc.weightx = 0.0D;
/* 493 */       gbc.insets.left = 20;
/* 494 */       label = new CustomLabel(caption);
/* 495 */       dlgHelper.addComponent(criteriaPanel, label);
/*     */ 
/* 497 */       gbc.weightx = 1.0D;
/* 498 */       gbc.insets.left = left;
/* 499 */       label = new CustomLabel(value);
/* 500 */       dlgHelper.addLastComponentInRow(criteriaPanel, label);
/*     */     }
/*     */ 
/* 504 */     datePanel.setLayout(new GridBagLayout());
/* 505 */     label = new CustomLabel(LocaleResources.getString("apSubscriptionDatesLabel", this.m_cxt), 1);
/*     */ 
/* 507 */     dlgHelper.addLastComponentInRow(datePanel, label);
/* 508 */     String[][] list = { { "dSubscriptionCreateDate", "apSubscriptionCreateDateLabel" }, { "dSubscriptionNotifyDate", "apSubscriptionNotifyDateLabel" }, { "dSubscriptionUsedDate", "apSubscriptionUsedDateLabel" } };
/*     */ 
/* 514 */     length = list.length;
/* 515 */     if (userAliasType.equals("alias"))
/*     */     {
/* 517 */       --length;
/*     */     }
/*     */ 
/* 520 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 522 */       gbc.weightx = 0.0D;
/* 523 */       label = new CustomLabel(LocaleResources.getString(list[i][1], this.m_cxt));
/* 524 */       gbc.insets.left = 20;
/* 525 */       dlgHelper.addComponent(datePanel, label);
/* 526 */       gbc.weightx = 1.0D;
/* 527 */       gbc.insets.left = left;
/* 528 */       label = new CustomLabel(scpProps.getProperty(list[i][0]));
/* 529 */       dlgHelper.addLastComponentInRow(datePanel, label);
/*     */     }
/*     */ 
/* 532 */     dlgHelper.addCommandButton(LocaleResources.getString("apLabelClose", this.m_cxt), dlgHelper);
/* 533 */     dlgHelper.prompt();
/*     */   }
/*     */ 
/*     */   public Vector getSubscriptionFields(String type)
/*     */   {
/* 538 */     DataResultSet scpTable = SharedObjects.getTable("SubscriptionTypes");
/* 539 */     Vector v = scpTable.findRow(0, type);
/* 540 */     String fields = (String)v.elementAt(1);
/* 541 */     Vector fieldList = StringUtils.parseArray(fields, ',', '^');
/* 542 */     return fieldList;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 549 */     Object source = event.getSource();
/*     */     try
/*     */     {
/* 552 */       JCheckBox box = (JCheckBox)source;
/* 553 */       this.m_filterEnabled = box.isSelected();
/* 554 */       refresh();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 558 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 564 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.DocumentSubscribersDlg
 * JD-Core Version:    0.5.4
 */