/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.AliasData;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.DefaultListSelectionModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class EditAliasDlg
/*     */   implements ActionListener, ListSelectionListener, ComponentBinder, SharedContext
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   protected String m_action;
/*     */   protected DataBinder m_binder;
/*     */   protected String m_helpPage;
/*     */   protected FixedSizeList m_aliasUsersList;
/*     */   protected Hashtable m_usersMap;
/*  91 */   protected JButton m_addBtn = null;
/*  92 */   protected JButton m_deleteBtn = null;
/*     */ 
/*     */   public EditAliasDlg(SystemInterface sys, String title, ResultSet rset, String helpPage)
/*     */   {
/*  96 */     this.m_helper = new DialogHelper(sys, title, true);
/*  97 */     this.m_systemInterface = sys;
/*  98 */     this.m_ctx = sys.getExecutionContext();
/*  99 */     this.m_helpPage = helpPage;
/* 100 */     this.m_cmpValidator = new ComponentValidator(rset);
/* 101 */     this.m_usersMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(Properties data)
/*     */   {
/*     */     Component name;
/* 107 */     if (data != null)
/*     */     {
/* 109 */       Component name = new CustomLabel(data.getProperty("dAlias"));
/* 110 */       this.m_action = "EDIT_ALIAS";
/* 111 */       this.m_helper.m_props = data;
/*     */     }
/*     */     else
/*     */     {
/* 115 */       name = new CustomTextField(20);
/* 116 */       this.m_action = "ADD_ALIAS";
/*     */     }
/*     */ 
/* 119 */     DialogCallback okCallback = new DialogCallback()
/*     */     {
/*     */       public boolean handleDialogEvent(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 126 */           Properties localData = this.m_dlgHelper.m_props;
/* 127 */           EditAliasDlg.this.m_binder = new DataBinder(true);
/*     */ 
/* 129 */           if (EditAliasDlg.this.m_aliasUsersList.m_dataModel.getSize() == 0)
/*     */           {
/* 131 */             this.m_errorMessage = IdcMessageFactory.lc("apAliasMustHaveOneUser", new Object[0]);
/* 132 */             return false;
/*     */           }
/*     */ 
/* 135 */           int items = EditAliasDlg.this.m_aliasUsersList.getItemCount();
/* 136 */           String selectedUsers = "";
/*     */ 
/* 138 */           for (int i = 0; i < items; ++i)
/*     */           {
/* 140 */             selectedUsers = selectedUsers + EditAliasDlg.this.m_aliasUsersList.m_dataModel.getElementAt(i);
/* 141 */             selectedUsers = selectedUsers + '\n';
/*     */           }
/*     */ 
/* 144 */           localData.put("AliasUsersString", selectedUsers);
/* 145 */           EditAliasDlg.this.m_binder.setLocalData(localData);
/* 146 */           EditAliasDlg.this.executeService(EditAliasDlg.this.m_action, EditAliasDlg.this.m_binder, false);
/*     */         }
/*     */         catch (ServiceException exp)
/*     */         {
/* 150 */           this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 151 */           return false;
/*     */         }
/* 153 */         return true;
/*     */       }
/*     */     };
/* 156 */     okCallback.m_dlgHelper = this.m_helper;
/*     */ 
/* 158 */     JPanel mainPanel = this.m_helper.initStandard(this, okCallback, 1, true, this.m_helpPage);
/*     */ 
/* 160 */     initUI(mainPanel, name);
/*     */ 
/* 162 */     initUserList(data);
/*     */   }
/*     */ 
/*     */   protected void initUI(JPanel mainPanel, Component name)
/*     */   {
/* 167 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/* 168 */     GridBagConstraints gc = gh.m_gc;
/*     */ 
/* 171 */     JPanel editPanel = new CustomPanel();
/* 172 */     this.m_helper.makePanelGridBag(editPanel, 2);
/* 173 */     gc.weightx = 1.0D;
/* 174 */     gc.insets = new Insets(5, 15, 5, 15);
/* 175 */     this.m_helper.addLabelFieldPair(editPanel, LocaleResources.getString("apLabelAliasName", this.m_ctx), name, "dAlias");
/*     */ 
/* 177 */     gc.insets = new Insets(0, 15, 5, 15);
/* 178 */     this.m_helper.addLabelFieldPair(editPanel, LocaleResources.getString("apLabelAliasDisplayName", this.m_ctx), new CustomTextField(30), "dAliasDisplayName");
/*     */ 
/* 180 */     gc.insets = new Insets(0, 15, 5, 15);
/* 181 */     this.m_helper.addLabelFieldPair(editPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), new CustomTextField(30), "dAliasDescription");
/*     */ 
/* 185 */     JPanel usersPanel = new PanePanel();
/* 186 */     this.m_helper.makePanelGridBag(usersPanel, 1);
/* 187 */     gc.weightx = 1.0D;
/* 188 */     gh.prepareAddLastRowElement();
/* 189 */     this.m_helper.addComponent(usersPanel, new CustomLabel(LocaleResources.getString("apLabelUsers", this.m_ctx), 1));
/*     */ 
/* 192 */     this.m_aliasUsersList = new FixedSizeList(15, 150);
/* 193 */     this.m_aliasUsersList.setMultipleMode(true);
/*     */ 
/* 195 */     gc.weighty = 1.0D;
/* 196 */     this.m_helper.addComponent(usersPanel, this.m_aliasUsersList);
/* 197 */     this.m_aliasUsersList.m_selectionModel.addListSelectionListener(this);
/*     */ 
/* 200 */     JPanel btnPanel = new PanePanel();
/* 201 */     btnPanel.setLayout(new GridLayout(0, 1));
/*     */ 
/* 203 */     this.m_helper.addComponent(btnPanel, this.m_addBtn = new JButton(LocaleResources.getString("apDlgButtonAdd", this.m_ctx)));
/*     */ 
/* 205 */     this.m_addBtn.addActionListener(this);
/*     */ 
/* 207 */     this.m_helper.addComponent(btnPanel, this.m_deleteBtn = new JButton(LocaleResources.getString("apLabelDelete", this.m_ctx)));
/*     */ 
/* 209 */     this.m_deleteBtn.setEnabled(false);
/* 210 */     this.m_deleteBtn.addActionListener(this);
/*     */ 
/* 212 */     JPanel btnWrapper = new PanePanel();
/* 213 */     this.m_helper.makePanelGridBag(btnWrapper, 0);
/* 214 */     gh.addEmptyRow(btnWrapper);
/* 215 */     gh.prepareAddLastRowElement();
/* 216 */     this.m_helper.addComponent(btnWrapper, btnPanel);
/*     */ 
/* 219 */     JPanel wrapper = new PanePanel();
/* 220 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 221 */     gh.m_gc.weighty = 1.0D;
/* 222 */     gh.m_gc.weightx = 1.0D;
/* 223 */     gh.prepareAddRowElement();
/* 224 */     this.m_helper.addComponent(wrapper, usersPanel);
/* 225 */     gh.prepareAddRowElement();
/* 226 */     this.m_helper.addComponent(wrapper, btnWrapper);
/*     */ 
/* 229 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/* 230 */     gc.weightx = 1.0D;
/* 231 */     gh.prepareAddLastRowElement();
/* 232 */     this.m_helper.addComponent(mainPanel, editPanel);
/* 233 */     gc.weighty = 1.0D;
/* 234 */     gh.prepareAddLastRowElement();
/* 235 */     this.m_helper.addComponent(mainPanel, wrapper);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 240 */     return this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public String getAlias()
/*     */   {
/* 245 */     return this.m_helper.m_props.getProperty("dAlias");
/*     */   }
/*     */ 
/*     */   public DataBinder getBinder()
/*     */   {
/* 250 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   protected void initUserList(Properties data)
/*     */   {
/*     */     try
/*     */     {
/* 257 */       if (data != null)
/*     */       {
/* 259 */         AliasData aliases = (AliasData)SharedObjects.getTable("Alias");
/* 260 */         String[][] users = aliases.getUsers(data.getProperty("dAlias"));
/* 261 */         for (int i = 0; i < users.length; ++i)
/*     */         {
/* 263 */           String user = users[i][0];
/* 264 */           this.m_aliasUsersList.add(user);
/* 265 */           this.m_usersMap.put(user, user);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 271 */       Report.trace(null, null, e);
/* 272 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void enabledDisable(FixedSizeList list, boolean flag)
/*     */   {
/* 278 */     int[] index = list.getSelectedIndexes();
/*     */ 
/* 280 */     if (index.length <= 0)
/*     */       return;
/* 282 */     this.m_deleteBtn.setEnabled(!flag);
/*     */   }
/*     */ 
/*     */   public void valueChanged(ListSelectionEvent e)
/*     */   {
/* 291 */     enabledDisable(this.m_aliasUsersList, false);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 299 */     Object src = e.getSource();
/* 300 */     if (src == this.m_addBtn)
/*     */     {
/* 302 */       addUsers();
/*     */     } else {
/* 304 */       if (src != this.m_deleteBtn)
/*     */         return;
/* 306 */       deleteUsers();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addUsers()
/*     */   {
/* 312 */     ViewDlg dlg = new ViewDlg(this.m_helper.m_dialog, this.m_systemInterface, LocaleResources.getString("apLabelSelectUsers", this.m_ctx), this, DialogHelpTable.getHelpPage("AddUserToAlias"));
/*     */ 
/* 314 */     ViewData viewData = new ViewData(2, "Users", "UserList");
/* 315 */     viewData.m_isMultipleMode = true;
/* 316 */     viewData.m_isViewOnly = false;
/* 317 */     viewData.m_viewName = "UserSelectView";
/*     */ 
/* 319 */     dlg.init(viewData, this.m_usersMap);
/*     */ 
/* 321 */     if (dlg.prompt() == 0)
/*     */     {
/* 324 */       return;
/*     */     }
/*     */ 
/* 327 */     String[] selUsers = dlg.getSelectedObjs();
/* 328 */     if (selUsers == null)
/*     */     {
/* 330 */       return;
/*     */     }
/*     */ 
/* 333 */     for (int i = 0; i < selUsers.length; ++i)
/*     */     {
/* 336 */       String user = selUsers[i];
/* 337 */       if (this.m_usersMap.get(user) != null)
/*     */         continue;
/* 339 */       this.m_usersMap.put(user, user);
/*     */     }
/*     */ 
/* 344 */     int num = this.m_usersMap.size();
/* 345 */     String[] users = new String[num];
/* 346 */     int count = 0;
/* 347 */     for (Enumeration en = this.m_usersMap.keys(); en.hasMoreElements(); ++count)
/*     */     {
/* 349 */       users[count] = ((String)en.nextElement());
/*     */     }
/*     */ 
/* 352 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 356 */         String s1 = (String)obj1;
/* 357 */         String s2 = (String)obj2;
/*     */ 
/* 359 */         return s1.compareTo(s2);
/*     */       }
/*     */     };
/* 363 */     Sort.sort(users, 0, num - 1, cmp);
/*     */ 
/* 365 */     this.m_aliasUsersList.removeAllItems();
/* 366 */     int numSel = selUsers.length;
/* 367 */     int[] selIndexes = new int[numSel];
/*     */ 
/* 369 */     for (int i = 0; i < users.length; ++i)
/*     */     {
/* 371 */       String user = users[i];
/* 372 */       this.m_aliasUsersList.add(user);
/* 373 */       for (int j = 0; j < numSel; ++j)
/*     */       {
/* 375 */         if (!user.equals(selUsers[j]))
/*     */           continue;
/* 377 */         selIndexes[j] = i;
/* 378 */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 384 */     for (int i = 0; i < numSel; ++i)
/*     */     {
/* 386 */       this.m_aliasUsersList.select(selIndexes[i]);
/*     */     }
/*     */ 
/* 389 */     this.m_deleteBtn.setEnabled(numSel > 0);
/*     */   }
/*     */ 
/*     */   protected void deleteUsers()
/*     */   {
/* 394 */     String[] users = this.m_aliasUsersList.getSelectedItems();
/* 395 */     for (int i = 0; i < users.length; ++i)
/*     */     {
/* 397 */       String user = users[i];
/* 398 */       if (user.trim().length() <= 0)
/*     */         continue;
/* 400 */       this.m_aliasUsersList.remove(user);
/* 401 */       this.m_usersMap.remove(user);
/*     */     }
/*     */ 
/* 404 */     this.m_deleteBtn.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 413 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 414 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 419 */     String name = exchange.m_compName;
/* 420 */     String val = exchange.m_compValue;
/*     */ 
/* 422 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 424 */     IdcMessage errMsg = null;
/* 425 */     if (name.equals("dAlias"))
/*     */     {
/* 427 */       int resultVal = Validation.checkUrlFileSegment(val);
/* 428 */       if (resultVal != 0)
/*     */       {
/* 430 */         switch (resultVal)
/*     */         {
/*     */         case -1:
/* 433 */           errMsg = IdcMessageFactory.lc("apAliasCannotBeEmpty", new Object[] { val });
/* 434 */           break;
/*     */         case -2:
/* 436 */           errMsg = IdcMessageFactory.lc("apAliasCannotContainSpaces", new Object[] { val });
/* 437 */           break;
/*     */         case -3:
/* 439 */           errMsg = IdcMessageFactory.lc("apInvalidCharsInAlias", new Object[] { val });
/*     */         }
/*     */ 
/* 443 */         if ((errMsg == null) && (val != null) && (val.length() > maxLength))
/*     */         {
/* 445 */           errMsg = IdcMessageFactory.lc("apAliasExceedsMaxLength", new Object[] { val, Integer.valueOf(maxLength) });
/*     */         }
/*     */       }
/*     */     }
/* 449 */     else if ((name.equals("dAliasDisplayName")) && (val != null) && (val.length() > maxLength))
/*     */     {
/* 451 */       errMsg = IdcMessageFactory.lc("apAliasDisplayNameExceedsMaxLength", new Object[] { val, Integer.valueOf(maxLength) });
/*     */     }
/* 453 */     else if ((name.equals("dAliasDescription")) && (val != null) && (val.length() > maxLength))
/*     */     {
/* 455 */       errMsg = IdcMessageFactory.lc("apAliasDescriptionExceedsMaxLength", new Object[] { val, Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 458 */     if (errMsg != null)
/*     */     {
/* 460 */       exchange.m_errorMessage = errMsg;
/* 461 */       return false;
/*     */     }
/* 463 */     return true;
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder data, boolean isRefreshList)
/*     */     throws ServiceException
/*     */   {
/* 472 */     AppLauncher.executeService(action, data);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 477 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 482 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78892 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditAliasDlg
 * JD-Core Version:    0.5.4
 */