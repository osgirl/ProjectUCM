/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
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
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.FocusEvent;
/*     */ import java.awt.event.FocusListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SubscriptionPanel extends BasePanel
/*     */   implements ActionListener, FocusListener, Observer
/*     */ {
/*  74 */   protected DataResultSet m_subscriptionTypeList = null;
/*  75 */   protected UdlPanel m_list = null;
/*     */ 
/*  77 */   protected JButton[] m_btns = null;
/*  78 */   protected JButton m_deleteBtn = null;
/*  79 */   protected static final String[][] BUTTON_INFO = { { "apAddSubscription", "add", "0", "apNewSubscriptionTypeTitle" }, { "apEditSubscription", "edit", "1", "apReadableButtonEditSubscriptionType" }, { "apDeleteSubscription", "delete", "1", "apReadableButtonDeleteSubscriptionType" }, { "apSubscriberList", "subscribers", "1", "apReadableButtonShowSubscribers" } };
/*     */ 
/*     */   public void init(SystemInterface sys, JMenu fMenu)
/*     */     throws ServiceException
/*     */   {
/*  95 */     super.init(sys, fMenu);
/*     */ 
/*  98 */     refreshList();
/*     */ 
/* 101 */     AppLauncher.addSubjectObserver("subscriptiontypes", this);
/*     */ 
/* 106 */     SubscriptionUsersDlg.clearDialogs();
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/* 112 */     return new Insets(5, 10, 5, 10);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/* 118 */     this.m_list = createSubscriptionsListPanel();
/*     */ 
/* 120 */     int len = BUTTON_INFO.length;
/* 121 */     this.m_btns = new JButton[len];
/* 122 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 124 */       String cmd = BUTTON_INFO[i][1];
/* 125 */       JButton btn = new JButton(LocaleResources.getString(BUTTON_INFO[i][0], this.m_cxt));
/* 126 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(BUTTON_INFO[i][3], this.m_cxt));
/* 127 */       btn.setActionCommand(cmd);
/* 128 */       boolean isControlled = StringUtils.convertToBool(BUTTON_INFO[i][2], false);
/* 129 */       if (isControlled)
/*     */       {
/* 131 */         this.m_list.addControlComponent(btn);
/*     */       }
/* 133 */       if (cmd.equals("delete"))
/*     */       {
/* 135 */         this.m_deleteBtn = btn;
/*     */       }
/* 137 */       btn.addActionListener(this);
/* 138 */       this.m_btns[i] = btn;
/*     */     }
/*     */ 
/* 141 */     JPanel listPanel = addButtons(this.m_list, this.m_btns, 15);
/*     */ 
/* 143 */     this.m_helper.makePanelGridBag(this, 1);
/* 144 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 145 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 146 */     this.m_helper.addComponent(this, listPanel);
/*     */   }
/*     */ 
/*     */   protected void updateAccessibilityInfo(ExecutionContext cxt)
/*     */   {
/*     */   }
/*     */ 
/*     */   public UdlPanel createSubscriptionsListPanel()
/*     */   {
/* 163 */     String columns = "scpType,scpFields,scpDescription,scpEnabled";
/*     */ 
/* 165 */     UdlPanel list = new UdlPanel(LocaleResources.getString("apSubscriptionTypesLabel", this.m_cxt), null, 300, 20, "SubscriptionTypes", true);
/*     */ 
/* 170 */     ColumnInfo info = new ColumnInfo(getString("apSubscriptionTypeColumnLabel"), "scpType", 8.0D);
/* 171 */     list.setColumnInfo(info);
/* 172 */     info = new ColumnInfo(getString("apSubscriptionFieldListColumnLabel"), "scpFields", 14.0D);
/*     */ 
/* 174 */     list.setColumnInfo(info);
/* 175 */     info = new ColumnInfo(getString("apSubscriptionDescriptionColumnLabel"), "scpDescription", 14.0D);
/*     */ 
/* 177 */     list.setColumnInfo(info);
/* 178 */     info = new ColumnInfo(getString("apSubscriptionStatusColumnLabel"), "scpEnabled", 6.0D);
/* 179 */     list.setColumnInfo(info);
/*     */ 
/* 181 */     list.setVisibleColumns(columns);
/* 182 */     list.setIDColumn("scpType");
/* 183 */     list.useDefaultListener();
/* 184 */     list.m_list.addActionListener(this);
/*     */ 
/* 187 */     DisplayStringCallback displayCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 193 */         String desc = null;
/* 194 */         if ((name.equals("scpDescription")) && (value.length() > 0))
/*     */         {
/* 196 */           desc = LocaleResources.getString(value, SubscriptionPanel.this.m_cxt);
/*     */         }
/*     */ 
/* 199 */         if (desc == null)
/*     */         {
/* 201 */           desc = value;
/*     */         }
/* 203 */         return desc;
/*     */       }
/*     */     };
/* 206 */     list.setDisplayCallback("scpDescription", displayCallback);
/*     */ 
/* 209 */     ItemListener listener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 213 */         int index = SubscriptionPanel.this.m_list.getSelectedIndex();
/* 214 */         if (index < 0)
/*     */         {
/* 216 */           return;
/*     */         }
/* 218 */         Properties props = SubscriptionPanel.this.m_list.getDataAt(index);
/* 219 */         String type = props.getProperty("scpType");
/* 220 */         if ((type.equalsIgnoreCase("basic")) || (type.equalsIgnoreCase("folder")))
/*     */         {
/* 223 */           SubscriptionPanel.this.m_deleteBtn.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 227 */           SubscriptionPanel.this.m_deleteBtn.setEnabled(true);
/*     */         }
/*     */       }
/*     */     };
/* 231 */     list.addItemListener(listener);
/*     */ 
/* 233 */     DisplayStringCallback enabledCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 239 */         boolean v = StringUtils.convertToBool(value, false);
/* 240 */         return (v) ? SubscriptionPanel.this.getString("apTitleEnabled") : SubscriptionPanel.this.getString("apTitleDisabled");
/*     */       }
/*     */     };
/* 244 */     ViewFields fields = new ViewFields(this.m_cxt);
/* 245 */     fields.addStandardDocFields();
/* 246 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/*     */     try
/*     */     {
/* 249 */       fields.addMetaFields(drset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 253 */       reportError(e, IdcMessageFactory.lc("apUnableToRetrieveFieldInfo", new Object[0]));
/*     */     }
/* 255 */     fields.addField("fParentGUID", getString("apSubscriptionFolderID"));
/* 256 */     Hashtable nameMap = new Hashtable();
/* 257 */     int length = fields.m_viewFields.size();
/* 258 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 260 */       FieldDef def = (FieldDef)fields.m_viewFields.elementAt(i);
/* 261 */       nameMap.put(def.m_name, def);
/*     */     }
/*     */ 
/* 264 */     DisplayStringCallback fieldsCallback = new DisplayStringCallbackAdaptor(nameMap)
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 270 */         Vector v = StringUtils.parseArray(value, ',', ',');
/* 271 */         int len = v.size();
/* 272 */         String displayString = "";
/* 273 */         for (int i = 0; i < len; ++i)
/*     */         {
/* 275 */           String fname = (String)v.elementAt(i);
/* 276 */           FieldDef def = (FieldDef)this.val$nameMap.get(fname);
/* 277 */           if (def != null)
/*     */           {
/* 279 */             displayString = displayString + def.m_caption;
/*     */           }
/*     */           else
/*     */           {
/* 283 */             displayString = displayString + fname;
/*     */           }
/*     */ 
/* 286 */           if (i + 1 >= len)
/*     */             continue;
/* 288 */           displayString = displayString + ", ";
/*     */         }
/*     */ 
/* 292 */         return displayString;
/*     */       }
/*     */     };
/* 296 */     list.setDisplayCallback("scpEnabled", enabledCallback);
/* 297 */     list.setDisplayCallback("scpFields", fieldsCallback);
/*     */ 
/* 299 */     list.init();
/*     */ 
/* 301 */     return list;
/*     */   }
/*     */ 
/*     */   public JPanel addButtons(Component base, JButton[] btns, int edge)
/*     */   {
/* 312 */     JPanel btnPanel = new PanePanel();
/* 313 */     GridLayout btnLayout = null;
/* 314 */     JPanel outerPanel = new PanePanel();
/* 315 */     this.m_helper.makePanelGridBag(outerPanel, 1);
/*     */ 
/* 317 */     int btnCount = btns.length;
/*     */ 
/* 319 */     GridBagConstraints gbc = this.m_helper.m_gridHelper.m_gc;
/* 320 */     float wx = 0.0F;
/* 321 */     float wy = 0.0F;
/* 322 */     int fill = 0;
/* 323 */     switch (edge) {
/*     */     case 13:
/*     */     case 17:
/* 327 */       gbc.gridx = -1;
/* 328 */       btnLayout = new GridLayout(btnCount, 1);
/* 329 */       break;
/*     */     case 11:
/*     */     case 15:
/* 332 */       gbc.gridx = 0;
/* 333 */       gbc.gridy = -1;
/* 334 */       btnLayout = new GridLayout(1, btnCount);
/*     */     case 12:
/*     */     case 14:
/*     */     case 16:
/* 337 */     }btnLayout.setHgap(2);
/* 338 */     btnLayout.setVgap(2);
/* 339 */     btnPanel.setLayout(btnLayout);
/*     */ 
/* 341 */     for (int i = 0; i < btnCount; ++i)
/*     */     {
/* 343 */       JButton btn = btns[i];
/* 344 */       btnPanel.add(btn, gbc);
/*     */     }
/*     */ 
/* 347 */     Component c1 = null;
/* 348 */     Component c2 = null;
/* 349 */     switch (edge) {
/*     */     case 11:
/*     */     case 17:
/* 353 */       gbc.weightx = wx;
/* 354 */       gbc.weighty = wy;
/* 355 */       wx = wy = 1.0F;
/* 356 */       gbc.fill = 0;
/* 357 */       fill = 1;
/* 358 */       c1 = btnPanel;
/* 359 */       c2 = base;
/* 360 */       break;
/*     */     case 13:
/*     */     case 15:
/* 363 */       gbc.weightx = (gbc.weighty = 1.0D);
/* 364 */       c1 = base;
/* 365 */       c2 = btnPanel;
/*     */     case 12:
/*     */     case 14:
/*     */     case 16:
/*     */     }
/* 369 */     this.m_helper.addComponent(outerPanel, c1);
/* 370 */     gbc.weightx = wx;
/* 371 */     gbc.weighty = wy;
/* 372 */     gbc.fill = fill;
/* 373 */     this.m_helper.addComponent(outerPanel, c2);
/*     */ 
/* 375 */     return outerPanel;
/*     */   }
/*     */ 
/*     */   protected void refreshList()
/*     */   {
/* 381 */     int index = this.m_list.getSelectedIndex();
/* 382 */     String key = null;
/* 383 */     if (index >= 0)
/*     */     {
/* 385 */       Properties props = this.m_list.getDataAt(index);
/* 386 */       key = (String)props.get("scpType");
/*     */     }
/*     */ 
/* 389 */     refreshList(key);
/*     */ 
/* 391 */     updateAccessibilityInfo(this.m_systemInterface.getExecutionContext());
/*     */   }
/*     */ 
/*     */   public void refreshList(String selectedObj)
/*     */   {
/* 396 */     this.m_subscriptionTypeList = SharedObjects.getTable("SubscriptionTypes");
/* 397 */     this.m_list.refreshList(this.m_subscriptionTypeList, selectedObj);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 403 */     String cmd = event.getActionCommand();
/* 404 */     int index = this.m_list.getSelectedIndex();
/* 405 */     if ((index < 0) && (!cmd.equals("add")))
/*     */     {
/* 408 */       return;
/*     */     }
/*     */ 
/* 411 */     Object component = event.getSource();
/* 412 */     if (!component instanceof JButton)
/*     */     {
/* 415 */       cmd = "subscribers";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 420 */       if (cmd.equals("add"))
/*     */       {
/* 422 */         Properties props = new Properties();
/* 423 */         props.put("IsNew", "1");
/* 424 */         AddEditSubscriptionType dlg = new AddEditSubscriptionType(this.m_systemInterface, getString("apNewSubscriptionTypeTitle"), DialogHelpTable.getHelpPage("AddSubscriptionType"));
/*     */ 
/* 427 */         DialogCallback callback = new DialogCallback(props)
/*     */         {
/*     */           public boolean handleDialogEvent(ActionEvent ignore)
/*     */           {
/* 432 */             DataBinder binder = new DataBinder();
/* 433 */             binder.setLocalData(this.val$props);
/*     */             try
/*     */             {
/* 436 */               AppLauncher.executeService("CREATE_SUBSCRIPTION_TYPE", binder);
/*     */             }
/*     */             catch (ServiceException e)
/*     */             {
/* 440 */               SubscriptionPanel.this.reportError(e);
/*     */             }
/*     */ 
/* 443 */             SubscriptionPanel.this.refreshList((String)this.val$props.get("scpType"));
/*     */ 
/* 445 */             return true;
/*     */           }
/*     */         };
/* 449 */         dlg.init(callback, props);
/* 450 */         dlg.prompt();
/*     */       }
/* 452 */       else if (index >= 0)
/*     */       {
/* 454 */         Properties props = this.m_list.getDataAt(index);
/*     */ 
/* 456 */         if (cmd.equals("edit"))
/*     */         {
/* 458 */           String type = props.getProperty("scpType");
/* 459 */           AddEditSubscriptionType dlg = new AddEditSubscriptionType(this.m_systemInterface, LocaleResources.getString("apEditSubscriptionTypeTitle", this.m_cxt, type), DialogHelpTable.getHelpPage("EditSubscriptionType"));
/*     */ 
/* 463 */           String scpFields = (String)props.get("scpFields");
/*     */ 
/* 465 */           DialogCallback callback = new DialogCallback(props, scpFields)
/*     */           {
/*     */             public boolean handleDialogEvent(ActionEvent ignore)
/*     */             {
/* 470 */               String type2 = (String)this.val$props.get("scpType");
/* 471 */               String description = (String)this.val$props.get("scpDescription");
/* 472 */               String enabled = (String)this.val$props.get("scpEnabled");
/* 473 */               String fields = (String)this.val$props.get("scpFields");
/* 474 */               IdcMessage msg = IdcMessageFactory.lc("apSubscriptionFieldListChanges", new Object[0]);
/*     */ 
/* 476 */               if ((!fields.equals(this.val$scpFields)) && (MessageBox.doMessage(SubscriptionPanel.this.m_systemInterface, msg, 4) != 2))
/*     */               {
/* 480 */                 return false;
/*     */               }
/*     */ 
/* 483 */               DataBinder binder = new DataBinder();
/* 484 */               binder.putLocal("scpType", type2);
/* 485 */               binder.putLocal("scpDescription", description);
/* 486 */               binder.putLocal("scpEnabled", enabled);
/* 487 */               if (!fields.equals(this.val$scpFields))
/*     */               {
/* 489 */                 binder.putLocal("scpFields", fields);
/*     */               }
/*     */               try
/*     */               {
/* 493 */                 AppLauncher.executeService("UPDATE_SUBSCRIPTION_TYPE", binder);
/*     */               }
/*     */               catch (ServiceException e)
/*     */               {
/* 497 */                 SubscriptionPanel.this.reportError(e);
/*     */               }
/*     */ 
/* 500 */               SubscriptionPanel.this.refreshList();
/*     */ 
/* 502 */               return true;
/*     */             }
/*     */           };
/* 506 */           dlg.init(callback, props);
/* 507 */           dlg.prompt();
/*     */         }
/* 509 */         else if (cmd.equals("delete"))
/*     */         {
/* 511 */           deleteSubscriptionType(props);
/*     */         }
/* 513 */         else if (cmd.equals("subscribers"))
/*     */         {
/* 515 */           SubscriptionUsersDlg dlg = new SubscriptionUsersDlg();
/*     */           try
/*     */           {
/* 519 */             dlg.init(this.m_systemInterface, props, "SubscribedUsersHelp");
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 523 */             reportError(e);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 530 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteSubscriptionType(Properties props)
/*     */   {
/* 536 */     String name = props.getProperty("scpType");
/* 537 */     IdcMessage msg = IdcMessageFactory.lc("apSubscriptionTypeDelete", new Object[] { name });
/*     */ 
/* 539 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 544 */       DataBinder binder = new DataBinder();
/* 545 */       binder.setLocalData(props);
/*     */       try
/*     */       {
/* 549 */         AppLauncher.executeService("DELETE_SUBSCRIPTION_TYPE", binder);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 553 */         reportError(e);
/*     */       }
/*     */ 
/* 556 */       refreshList();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 560 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 569 */     refreshList();
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 575 */     AppLauncher.removeSubjectObserver("subscriptiontypes", this);
/* 576 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public void focusGained(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void focusLost(FocusEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 592 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98934 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.SubscriptionPanel
 * JD-Core Version:    0.5.4
 */