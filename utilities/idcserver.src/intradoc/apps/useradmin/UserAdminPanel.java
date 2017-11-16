/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.gui.FilterUtils;
/*     */ import intradoc.shared.gui.RefreshView;
/*     */ import intradoc.shared.gui.UserView;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Observable;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class UserAdminPanel extends BasePanel
/*     */   implements Observer, ActionListener, RefreshView, SharedContext
/*     */ {
/*     */   protected UserView m_userView;
/*     */   protected UserData m_loggedInUserData;
/*     */   protected DocumentLocalizedProfile m_profile;
/*     */ 
/*     */   public UserAdminPanel()
/*     */   {
/*  68 */     this.m_loggedInUserData = null;
/*  69 */     this.m_profile = null;
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys) throws ServiceException
/*     */   {
/*  74 */     super.init(sys);
/*     */ 
/*  77 */     refreshList();
/*     */ 
/*  80 */     this.m_loggedInUserData = AppLauncher.getUserData();
/*     */ 
/*  83 */     AppLauncher.addSubjectObserver("userlist", this);
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */     throws ServiceException
/*     */   {
/*  89 */     UserData userData = AppLauncher.getUserData();
/*  90 */     this.m_profile = new DocumentLocalizedProfile(userData, 8, this.m_cxt);
/*     */ 
/*  93 */     initList();
/*     */ 
/*  95 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAdd", this.m_cxt), "add", "0" }, { LocaleResources.getString("apDlgButtonAddSimilar", this.m_cxt), "addSimilar", "1" }, { LocaleResources.getString("apDlgButtonEdit", this.m_cxt), "edit", "1" }, { LocaleResources.getString("apDlgButtonChange", this.m_cxt), "change", "1" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "1" } };
/*     */ 
/* 104 */     JPanel btnActionsPanel = new PanePanel();
/* 105 */     UdlPanel userList = this.m_userView.getList();
/* 106 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 108 */       JButton btn = new JButton(buttonInfo[i][0]);
/* 109 */       btn.setActionCommand(buttonInfo[i][1]);
/* 110 */       btn.addActionListener(this);
/* 111 */       btnActionsPanel.add(btn);
/*     */ 
/* 113 */       boolean isListControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 114 */       if (!isListControlled)
/*     */         continue;
/* 116 */       userList.addControlComponent(btn);
/*     */     }
/*     */ 
/* 120 */     userList.useDefaultListener();
/* 121 */     this.m_userView.addButtonToolbar(btnActionsPanel);
/*     */   }
/*     */ 
/*     */   protected UserData getOrCreateUserData(String name, Properties props)
/*     */   {
/* 126 */     UserData userData = UserUtils.createUserData(props);
/* 127 */     String authType = userData.getProperty("dUserAuthType");
/* 128 */     if ((authType != null) && (authType.equalsIgnoreCase("EXTERNAL")))
/*     */     {
/* 130 */       userData.checkCreateAttributes(false);
/* 131 */       userData.m_hasAttributesLoaded = true;
/*     */     }
/*     */ 
/* 134 */     return userData;
/*     */   }
/*     */ 
/*     */   protected boolean loadAllUserData(UserData userData)
/*     */   {
/* 139 */     if (userData.m_hasAttributesLoaded)
/*     */     {
/* 141 */       return true;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 146 */       DataBinder binder = new DataBinder(true);
/* 147 */       binder.putLocal("dName", userData.m_name);
/* 148 */       AppLauncher.executeService("QUERY_USER_ATTRIBUTES", binder);
/* 149 */       UserUtils.serializeAttribInfo(binder, userData, false, false);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 154 */       reportError(e);
/* 155 */       return false;
/*     */     }
/*     */ 
/* 158 */     return true;
/*     */   }
/*     */ 
/*     */   protected JPanel initList()
/*     */   {
/* 164 */     this.m_helper = new ContainerHelper();
/* 165 */     this.m_helper.attachToContainer(this, this.m_systemInterface, null);
/* 166 */     this.m_helper.m_mainPanel = this;
/*     */ 
/* 168 */     this.m_userView = new UserView(this.m_helper, this, this.m_profile);
/* 169 */     ViewData viewData = new ViewData(2);
/* 170 */     this.m_userView.initUI(viewData);
/*     */ 
/* 172 */     UdlPanel userList = this.m_userView.getList();
/* 173 */     ItemListener listener = new ItemListener(userList)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 177 */         int state = e.getStateChange();
/* 178 */         switch (state)
/*     */         {
/*     */         case 1:
/* 181 */           this.val$userList.enableDisable(true);
/*     */         case 2:
/*     */         }
/*     */       }
/*     */     };
/* 188 */     this.m_userView.addItemListener(listener);
/* 189 */     this.m_userView.addActionListener(this);
/*     */ 
/* 191 */     return null;
/*     */   }
/*     */ 
/*     */   public void refreshList(String selectedUser)
/*     */   {
/*     */     try
/*     */     {
/* 198 */       this.m_userView.refreshView(selectedUser);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 202 */       Report.trace(null, LocaleResources.getString("apUnableToLoadUsersTable", this.m_cxt), e);
/* 203 */       e.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void refreshList()
/*     */   {
/* 209 */     refreshList(null);
/*     */   }
/*     */ 
/*     */   public DataBinder refresh(String rsetName, Vector filterData, DataResultSet defSet)
/*     */     throws ServiceException
/*     */   {
/* 217 */     DataBinder binder = new DataBinder();
/*     */ 
/* 219 */     ViewData viewData = this.m_userView.getViewData();
/* 220 */     FilterUtils.createTopicEdits(viewData.m_viewName + ":filter", binder, defSet);
/*     */ 
/* 223 */     String whereClause = this.m_userView.buildSQL(filterData);
/*     */ 
/* 226 */     binder.putLocal("MaxQueryRows", "" + SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500));
/* 227 */     binder.putLocal("whereClause", whereClause);
/* 228 */     binder.putLocal("orderClause", "ORDER by dName");
/* 229 */     binder.putLocal("resultName", rsetName);
/* 230 */     binder.putLocal("dataSource", "Users");
/*     */ 
/* 232 */     String action = "GET_DATARESULTSET";
/*     */     try
/*     */     {
/* 236 */       AppLauncher.executeService(action, binder, this.m_systemInterface);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 240 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 244 */     UserData userData = AppLauncher.getUserData();
/* 245 */     this.m_profile.m_userData = userData;
/*     */ 
/* 247 */     return binder;
/*     */   }
/*     */ 
/*     */   public void checkSelection()
/*     */   {
/* 252 */     int index = this.m_userView.getSelectedIndex();
/* 253 */     boolean isSelected = index >= 0;
/*     */ 
/* 255 */     UdlPanel userList = this.m_userView.getList();
/* 256 */     userList.enableDisable(isSelected);
/*     */   }
/*     */ 
/*     */   public SharedContext getSharedContext()
/*     */   {
/* 261 */     return this;
/*     */   }
/*     */ 
/*     */   public DataResultSet getMetaData()
/*     */   {
/* 266 */     return null;
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder data, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 274 */     AppLauncher.executeService(action, data);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 279 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 287 */     String cmd = e.getActionCommand();
/* 288 */     Object src = e.getSource();
/* 289 */     UdlPanel userList = this.m_userView.getList();
/*     */ 
/* 291 */     if (cmd.equals("add"))
/*     */     {
/* 293 */       addOrEditUser(false, false, false);
/*     */     }
/* 295 */     else if ((src == userList.m_list) || (cmd.equals("edit")))
/*     */     {
/* 297 */       addOrEditUser(true, false, false);
/*     */     }
/* 299 */     else if (cmd.equals("change"))
/*     */     {
/* 301 */       addOrEditUser(true, true, false);
/*     */     }
/* 303 */     else if (cmd.equals("delete"))
/*     */     {
/* 305 */       deleteUser();
/*     */     } else {
/* 307 */       if (!cmd.equals("addSimilar"))
/*     */         return;
/* 309 */       addOrEditUser(false, false, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditUser(boolean isEdit, boolean isChangeAuth, boolean isAddSimilar)
/*     */   {
/* 315 */     UserData userData = null;
/* 316 */     String name = null;
/* 317 */     String dlgTitle = LocaleResources.getString("apTitleAddUser", this.m_cxt);
/* 318 */     String helpPage = "AddUser";
/*     */ 
/* 320 */     if ((isEdit) || (isAddSimilar))
/*     */     {
/* 322 */       int index = this.m_userView.getSelectedIndex();
/* 323 */       if (index < 0)
/*     */       {
/* 325 */         return;
/*     */       }
/* 327 */       Properties data = this.m_userView.getDataAt(index);
/* 328 */       name = data.getProperty("dName");
/* 329 */       userData = getOrCreateUserData(name, data);
/*     */ 
/* 331 */       if (isEdit)
/*     */       {
/* 333 */         dlgTitle = LocaleResources.getString("apTitleEditUser", this.m_cxt, name);
/* 334 */         helpPage = "EditUser";
/*     */       }
/*     */ 
/* 337 */       if (!loadAllUserData(userData))
/*     */       {
/* 339 */         return;
/*     */       }
/*     */ 
/* 342 */       if (isAddSimilar)
/*     */       {
/* 344 */         UserData newUser = UserUtils.createUserData();
/* 345 */         newUser.copyAttributes(userData);
/* 346 */         newUser.setProperties((Properties)userData.getProperties().clone());
/* 347 */         newUser.setName("");
/*     */ 
/* 349 */         String[] propsToRemove = { "dFullName", "dEmail", "dPassword", "dPasswordEncoding", "dUserArriveDate", "dUserChangeDate" };
/*     */ 
/* 351 */         for (int i = 0; i < propsToRemove.length; ++i)
/*     */         {
/* 353 */           newUser.setProperty(propsToRemove[i], "");
/*     */         }
/*     */ 
/* 356 */         userData = newUser;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 361 */       userData = UserUtils.createUserData();
/* 362 */       userData.setAttributes((String[][])null);
/*     */ 
/* 364 */       boolean hasGlobalUsers = SharedObjects.getEnvValueAsBoolean("HasGlobalUsers", true);
/* 365 */       if (hasGlobalUsers)
/*     */       {
/* 368 */         EditUserAuthType dlg = new EditUserAuthType(this.m_systemInterface, LocaleResources.getString("apTitleChooseAuthType", this.m_cxt), null);
/*     */ 
/* 370 */         dlg.init(userData, isEdit);
/* 371 */         if (dlg.prompt() == 0)
/*     */         {
/* 374 */           return;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 379 */         userData.setProperty("dUserAuthType", "LOCAL");
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 385 */       if (isChangeAuth)
/*     */       {
/* 387 */         EditUserAuthType dlg = new EditUserAuthType(this.m_systemInterface, LocaleResources.getString("apTitleChangeAuthType", this.m_cxt), null);
/*     */ 
/* 389 */         dlg.init(userData, isEdit);
/* 390 */         if (dlg.prompt() == 0)
/*     */         {
/* 392 */           return;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 397 */         EditUserDlg userDlg = new EditUserDlg(this.m_systemInterface, dlgTitle, SharedObjects.getTable("Users"), DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 399 */         userDlg.init(userData, isEdit);
/* 400 */         if (userDlg.prompt() == 0)
/*     */         {
/* 402 */           return;
/*     */         }
/* 404 */         if (!isEdit)
/*     */         {
/* 406 */           Properties props = userDlg.getProps();
/* 407 */           name = props.getProperty("dName");
/*     */         }
/*     */       }
/* 410 */       refreshList(name);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 414 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteUser()
/*     */   {
/* 420 */     int index = this.m_userView.getSelectedIndex();
/* 421 */     if (index < 0)
/*     */     {
/* 423 */       reportError(IdcMessageFactory.lc("apTitleSelectUser", new Object[0]));
/* 424 */       return;
/*     */     }
/*     */ 
/* 427 */     Properties data = null;
/* 428 */     String name = "";
/*     */     try
/*     */     {
/* 431 */       data = this.m_userView.getDataAt(index);
/* 432 */       name = data.getProperty("dName");
/* 433 */       if (name.equalsIgnoreCase("sysadmin"))
/*     */       {
/* 435 */         reportError(IdcMessageFactory.lc("apMayNotDeleteUser", new Object[] { name }));
/* 436 */         return;
/*     */       }
/* 438 */       String currentUser = this.m_loggedInUserData.m_name;
/* 439 */       if (currentUser.equalsIgnoreCase(name))
/*     */       {
/* 441 */         reportError(IdcMessageFactory.lc("apMayNotDeleteSelf", new Object[0]));
/* 442 */         return;
/*     */       }
/*     */ 
/* 445 */       UserData userData = getOrCreateUserData(name, data);
/* 446 */       if (!loadAllUserData(userData))
/*     */       {
/* 448 */         return;
/*     */       }
/*     */ 
/* 451 */       if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyUserDelete", new Object[] { name }), 4) == 2)
/*     */       {
/*     */         try
/*     */         {
/* 457 */           data.put("validateNoWorkflows", "1");
/* 458 */           data.put("useActivityWarningAbort", "1");
/* 459 */           AppLauncher.executeService("DELETE_USER", data);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 463 */           String statusReason = data.getProperty("StatusReason");
/* 464 */           if ((statusReason != null) && (statusReason.startsWith("workflowInvolvementError")))
/*     */           {
/* 466 */             String workflows = statusReason.substring("workflowInvolvementError:".length());
/* 467 */             if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifyUserDeleteWorkflow", new Object[] { name, workflows }), 4) == 2)
/*     */             {
/* 471 */               data.remove("validateNoWorkflows");
/* 472 */               AppLauncher.executeService("DELETE_USER", data);
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 477 */             throw e;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 482 */         data.remove("validNoWorkFlows");
/* 483 */         data.remove("useActivityWarningAbort");
/*     */ 
/* 485 */         refreshList();
/*     */       }
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 490 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 499 */     String name = this.m_userView.getSelectedObj();
/* 500 */     refreshList(name);
/*     */   }
/*     */ 
/*     */   public void removeNotify()
/*     */   {
/* 506 */     AppLauncher.removeSubjectObserver("userlist", this);
/* 507 */     super.removeNotify();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 512 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87442 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.UserAdminPanel
 * JD-Core Version:    0.5.4
 */