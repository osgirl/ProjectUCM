/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DialogCallback;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.WindowHelper;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.RoleDefinitions;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.DefaultListSelectionModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class GroupPermissionsDlg extends JDialog
/*     */   implements ComponentBinder
/*     */ {
/*     */   public SystemInterface m_system;
/*     */   public ExecutionContext m_ctx;
/*     */   protected DialogHelper m_helper;
/*     */   protected ComponentValidator m_cmpValidator;
/*     */   public FixedSizeList m_groupList;
/*     */   protected FixedSizeList m_roleList;
/*     */   protected JButton m_close;
/*     */   protected JButton m_editPermissionsBtn;
/*     */   protected JButton m_addGroupBtn;
/*     */   protected JButton m_deleteGroupBtn;
/*  86 */   protected RoleDefinitions m_roleDefs = null;
/*  87 */   protected Vector m_currentRoles = null;
/*     */   protected JPanel m_permissionsPanel;
/*     */ 
/*     */   public GroupPermissionsDlg(SystemInterface sys, String title)
/*     */   {
/*  93 */     super(sys.getMainWindow(), title, true);
/*     */ 
/*  95 */     this.m_system = sys;
/*  96 */     this.m_ctx = sys.getExecutionContext();
/*  97 */     this.m_roleDefs = ((RoleDefinitions)SharedObjects.getTable("RoleDefinition"));
/*     */ 
/*  99 */     DataResultSet drset = SharedObjects.getTable("SecurityGroups");
/* 100 */     this.m_cmpValidator = new ComponentValidator(drset);
/*     */ 
/* 102 */     this.m_helper = new DialogHelper();
/* 103 */     this.m_helper.attachToDialog(this, sys, null);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 108 */     JPanel rolePanel = new PanePanel();
/* 109 */     rolePanel.setLayout(new BorderLayout());
/* 110 */     rolePanel.add("North", new CustomLabel(LocaleResources.getString("apLabelRoles", this.m_ctx), 1));
/*     */ 
/* 112 */     rolePanel.add("Center", this.m_roleList = new FixedSizeList(15, 125));
/* 113 */     ListSelectionListener listener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 117 */         if (GroupPermissionsDlg.this.m_roleList.getSelectedIndex() >= 0)
/*     */         {
/* 119 */           GroupPermissionsDlg.this.m_editPermissionsBtn.setEnabled(true);
/*     */         }
/*     */         else
/*     */         {
/* 123 */           GroupPermissionsDlg.this.m_editPermissionsBtn.setEnabled(false);
/*     */         }
/*     */       }
/*     */     };
/* 127 */     this.m_roleList.m_selectionModel.addListSelectionListener(listener);
/*     */ 
/* 129 */     JPanel groupPanel = new PanePanel();
/* 130 */     groupPanel.setLayout(new BorderLayout());
/* 131 */     groupPanel.add("North", new CustomLabel(LocaleResources.getString("apLabelGroups", this.m_ctx), 1));
/*     */ 
/* 133 */     groupPanel.add("Center", this.m_groupList = new FixedSizeList(15, 125));
/*     */ 
/* 135 */     listener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 139 */         String groupName = GroupPermissionsDlg.this.m_groupList.getSelectedItem();
/* 140 */         if (groupName != null)
/*     */         {
/* 142 */           groupName = GroupPermissionsDlg.this.m_groupList.getSelectedItem();
/* 143 */           GroupPermissionsDlg.this.m_deleteGroupBtn.setEnabled(true);
/*     */         }
/*     */         else
/*     */         {
/* 147 */           GroupPermissionsDlg.this.m_deleteGroupBtn.setEnabled(false);
/*     */         }
/*     */ 
/* 150 */         GroupPermissionsDlg.this.set_selectedRoles(groupName, null);
/*     */       }
/*     */     };
/* 153 */     this.m_groupList.m_selectionModel.addListSelectionListener(listener);
/* 154 */     this.m_permissionsPanel = new PanePanel();
/* 155 */     this.m_permissionsPanel.setLayout(new GridLayout(0, 1, 10, 10));
/* 156 */     this.m_permissionsPanel.add(this.m_close = new JButton(LocaleResources.getString("apLabelClose", this.m_ctx)));
/*     */ 
/* 158 */     ActionListener cl = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 162 */         GroupPermissionsDlg.this.setVisible(false);
/* 163 */         GroupPermissionsDlg.this.dispose();
/*     */       }
/*     */     };
/* 166 */     this.m_close.addActionListener(cl);
/* 167 */     this.m_permissionsPanel.add(this.m_editPermissionsBtn = new JButton(LocaleResources.getString("apDlgButtonEditPermissions", this.m_ctx)));
/*     */ 
/* 169 */     ActionListener permListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 173 */         GroupPermissionsDlg.this.editPermissions();
/*     */       }
/*     */     };
/* 176 */     this.m_roleList.addActionListener(permListener);
/* 177 */     this.m_editPermissionsBtn.addActionListener(permListener);
/* 178 */     this.m_editPermissionsBtn.setEnabled(false);
/*     */ 
/* 180 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 184 */         GroupPermissionsDlg.this.editGroups(e);
/*     */       }
/*     */     };
/* 187 */     this.m_addGroupBtn = this.m_helper.addCommandButton(LocaleResources.getString("apDlgButtonAddGroup", this.m_ctx), editListener);
/*     */ 
/* 189 */     this.m_deleteGroupBtn = this.m_helper.addCommandButton(LocaleResources.getString("apLabelDeleteGroup", this.m_ctx), editListener);
/*     */ 
/* 191 */     this.m_groupList.addActionListener(editListener);
/*     */ 
/* 193 */     JPanel wrapper = new PanePanel();
/* 194 */     wrapper.setLayout(new BorderLayout());
/* 195 */     wrapper.add("North", this.m_permissionsPanel);
/*     */ 
/* 197 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 198 */     mainPanel.setLayout(new GridLayout(1, 0, 10, 20));
/* 199 */     mainPanel.add(groupPanel);
/* 200 */     mainPanel.add(rolePanel);
/* 201 */     mainPanel.add(wrapper);
/*     */ 
/* 204 */     refreshLists();
/*     */ 
/* 207 */     showHelperDialog();
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/* 216 */     Insets curInsets = super.getInsets();
/* 217 */     curInsets.top += 10;
/* 218 */     curInsets.left += 10;
/* 219 */     curInsets.right += 5;
/* 220 */     return curInsets;
/*     */   }
/*     */ 
/*     */   public void refreshLists()
/*     */   {
/* 225 */     refreshLists(null, null);
/*     */   }
/*     */ 
/*     */   public void refreshLists(String groupName)
/*     */   {
/* 230 */     refreshLists(groupName, null);
/*     */   }
/*     */ 
/*     */   protected void refreshLists(String groupName, String roleName)
/*     */   {
/* 235 */     this.m_roleDefs = ((RoleDefinitions)SharedObjects.getTable("RoleDefinition"));
/*     */ 
/* 237 */     this.m_roleList.removeAllItems();
/* 238 */     this.m_groupList.removeAllItems();
/*     */ 
/* 240 */     int selectedIndex = -1;
/*     */ 
/* 242 */     Vector groupNames = SharedObjects.getOptList("securityGroups");
/* 243 */     if (groupNames == null)
/*     */     {
/* 246 */       reportError(IdcMessageFactory.lc("apNoGroupsInSystem", new Object[0]));
/* 247 */       return;
/*     */     }
/* 249 */     int size = groupNames.size();
/* 250 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 252 */       String name = (String)groupNames.elementAt(i);
/* 253 */       this.m_groupList.add(name);
/* 254 */       if ((groupName == null) || (!groupName.equalsIgnoreCase(name)))
/*     */         continue;
/* 256 */       selectedIndex = i;
/*     */     }
/*     */ 
/* 260 */     boolean enable = false;
/* 261 */     if (selectedIndex >= 0)
/*     */     {
/* 263 */       this.m_groupList.select(selectedIndex);
/* 264 */       set_selectedRoles(groupName, roleName);
/* 265 */       enable = true;
/*     */     }
/* 267 */     this.m_deleteGroupBtn.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   public void set_selectedRoles(String groupName, String roleName)
/*     */   {
/* 272 */     this.m_roleList.removeAllItems();
/* 273 */     if (groupName == null)
/*     */       return;
/* 275 */     this.m_currentRoles = this.m_roleDefs.getGroupRoles(groupName);
/* 276 */     if (this.m_currentRoles == null)
/*     */     {
/* 278 */       reportError(new IdcMessage("apNoRolesForGroup", new Object[] { null, groupName }));
/* 279 */       return;
/*     */     }
/*     */ 
/* 282 */     int selectedIndex = -1;
/* 283 */     int size = this.m_currentRoles.size();
/* 284 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 286 */       RoleGroupData data = (RoleGroupData)this.m_currentRoles.elementAt(i);
/* 287 */       String str = data.m_roleName + "  (" + SecurityAccessListUtils.makePrivilegeStr(data.m_privilege) + ")";
/* 288 */       this.m_roleList.add(str);
/* 289 */       if ((roleName == null) || (!roleName.equalsIgnoreCase(data.m_roleName)))
/*     */         continue;
/* 291 */       selectedIndex = i;
/*     */     }
/*     */ 
/* 295 */     boolean enable = false;
/* 296 */     if (selectedIndex >= 0)
/*     */     {
/* 298 */       this.m_roleList.select(selectedIndex);
/* 299 */       enable = true;
/*     */     }
/* 301 */     this.m_editPermissionsBtn.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   public void editPermissions()
/*     */   {
/* 307 */     String groupName = this.m_groupList.getSelectedItem();
/* 308 */     if (groupName == null)
/*     */     {
/* 310 */       reportError(IdcMessageFactory.lc("apSelectGroup", new Object[0]));
/* 311 */       return;
/*     */     }
/*     */ 
/* 314 */     int roleIndex = this.m_roleList.getSelectedIndex();
/* 315 */     if (roleIndex < 0)
/*     */     {
/* 317 */       reportError(IdcMessageFactory.lc("apSelectRole", new Object[0]));
/* 318 */       return;
/*     */     }
/*     */ 
/* 321 */     RoleGroupData data = (RoleGroupData)this.m_currentRoles.elementAt(roleIndex);
/* 322 */     if (data == null)
/*     */     {
/* 325 */       reportError(IdcMessageFactory.lc("apGroupUndefined", new Object[0]));
/* 326 */       return;
/*     */     }
/*     */ 
/* 329 */     createAndInitEditPermissionsDialog(data, LocaleResources.getString("apLabelEditPermission", this.m_ctx));
/*     */     try
/*     */     {
/* 333 */       DataBinder binder = new DataBinder();
/* 334 */       Properties localData = binder.getLocalData();
/* 335 */       localData.put("dRoleName", data.m_roleName);
/* 336 */       localData.put("dGroupName", data.m_groupName);
/* 337 */       localData.put("dPrivilege", String.valueOf(data.m_privilege));
/* 338 */       AppLauncher.executeService("EDIT_ROLE", binder);
/*     */ 
/* 340 */       refreshLists(data.m_groupName, data.m_roleName);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 344 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void editGroups(ActionEvent evt)
/*     */   {
/* 350 */     Object src = evt.getSource();
/*     */ 
/* 352 */     if (src == this.m_addGroupBtn)
/*     */     {
/* 354 */       DialogHelper addGroupDlg = new DialogHelper(this.m_system, LocaleResources.getString("apTitleAddNewGroup", this.m_ctx), true);
/*     */ 
/* 356 */       DialogCallback okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 363 */             Properties localData = this.m_dlgHelper.m_props;
/* 364 */             localData.put("dPrivilege", "0");
/* 365 */             DataBinder binder = new DataBinder(true);
/* 366 */             binder.setLocalData(localData);
/* 367 */             AppLauncher.executeService("ADD_GROUP", binder);
/* 368 */             GroupPermissionsDlg.this.refreshLists(localData.getProperty("dGroupName"));
/* 369 */             return true;
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 373 */             this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 374 */           }return false;
/*     */         }
/*     */       };
/* 379 */       JPanel mainPanel = addGroupDlg.initStandard(this, okCallback, 2, true, DialogHelpTable.getHelpPage("AddNewGroup"));
/*     */ 
/* 381 */       addGroupDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelGroupName", this.m_ctx), new CustomTextField(30), "dGroupName");
/*     */ 
/* 383 */       addGroupDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), new CustomTextField(30), "dDescription");
/*     */ 
/* 386 */       addGroupDlg.prompt();
/*     */     }
/* 388 */     else if (src == this.m_deleteGroupBtn)
/*     */     {
/* 390 */       String groupName = this.m_groupList.getSelectedItem();
/* 391 */       if ((groupName == null) || (groupName.length() == 0))
/*     */       {
/* 393 */         reportError(IdcMessageFactory.lc("apSelectGroup", new Object[0]));
/* 394 */         return;
/*     */       }
/*     */ 
/* 397 */       if ((groupName.equalsIgnoreCase("public")) || (groupName.equalsIgnoreCase("secure")))
/*     */       {
/* 399 */         IdcMessage msg = IdcMessageFactory.lc("apNotAllowedToDeleteGroup", new Object[] { groupName });
/* 400 */         reportError(msg);
/* 401 */         return;
/*     */       }
/*     */ 
/* 404 */       if (MessageBox.doMessage(this.m_system, IdcMessageFactory.lc("apVerifyGroupDelete", new Object[] { groupName }), 4) == 2)
/*     */       {
/* 408 */         DataBinder binder = new DataBinder();
/* 409 */         Properties localData = binder.getLocalData();
/* 410 */         localData.put("dGroupName", groupName);
/*     */         try
/*     */         {
/* 413 */           AppLauncher.executeService("DELETE_GROUP", binder);
/* 414 */           refreshLists();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 418 */           reportError(exp);
/*     */         }
/*     */       }
/*     */     } else {
/* 422 */       if (src != this.m_groupList) {
/*     */         return;
/*     */       }
/*     */ 
/* 426 */       DialogHelper editGroupDlg = new DialogHelper(this.m_system, LocaleResources.getString("apTitleEditGroup", this.m_ctx), true);
/*     */ 
/* 428 */       DialogCallback okCallback = new DialogCallback()
/*     */       {
/*     */         public boolean handleDialogEvent(ActionEvent e)
/*     */         {
/*     */           try
/*     */           {
/* 435 */             Properties localData = this.m_dlgHelper.m_props;
/* 436 */             DataBinder binder = new DataBinder(true);
/* 437 */             binder.setLocalData(localData);
/* 438 */             AppLauncher.executeService("EDIT_GROUP", binder);
/* 439 */             GroupPermissionsDlg.this.refreshLists(localData.getProperty("dGroupName"));
/* 440 */             return true;
/*     */           }
/*     */           catch (Exception exp)
/*     */           {
/* 444 */             this.m_errorMessage = IdcMessageFactory.lc(exp);
/* 445 */           }return false;
/*     */         }
/*     */       };
/* 450 */       JPanel mainPanel = editGroupDlg.initStandard(this, okCallback, 2, false, null);
/*     */       try
/*     */       {
/* 454 */         DataBinder binder = new DataBinder(true);
/* 455 */         String groupName = this.m_groupList.getSelectedItem();
/* 456 */         binder.putLocal("dGroupName", groupName);
/* 457 */         AppLauncher.executeService("QUERY_GROUP", binder);
/* 458 */         binder.putLocal("dDescription", binder.get("dDescription"));
/* 459 */         editGroupDlg.m_props = binder.getLocalData();
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/* 463 */         if (SystemUtils.m_verbose)
/*     */         {
/* 465 */           Report.debug("system", null, ignore);
/*     */         }
/*     */       }
/*     */ 
/* 469 */       editGroupDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelGroupName", this.m_ctx), new CustomLabel(), "dGroupName");
/*     */ 
/* 471 */       editGroupDlg.addLabelFieldPair(mainPanel, LocaleResources.getString("apLabelDescription", this.m_ctx), new CustomTextField(30), "dDescription");
/*     */ 
/* 473 */       editGroupDlg.prompt();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 483 */     WindowHelper helper = (WindowHelper)exchange.m_currentObject;
/* 484 */     helper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 489 */     String name = exchange.m_compName;
/* 490 */     String val = exchange.m_compValue;
/*     */ 
/* 492 */     int maxLength = this.m_cmpValidator.getMaxLength(name, 30);
/*     */ 
/* 494 */     IdcMessage errMsg = null;
/* 495 */     if (name.equals("dGroupName"))
/*     */     {
/* 497 */       errMsg = Validation.checkUrlFileSegmentForDB(val, "apLabelGroupName", maxLength, null);
/*     */     }
/* 500 */     else if ((name.equals("dDescription")) && 
/* 502 */       (val != null) && (val.length() > maxLength))
/*     */     {
/* 504 */       errMsg = IdcMessageFactory.lc("apDescriptionExceedsMaxLength", new Object[] { Integer.valueOf(maxLength) });
/*     */     }
/*     */ 
/* 507 */     if (errMsg != null)
/*     */     {
/* 509 */       exchange.m_errorMessage = errMsg;
/* 510 */       return false;
/*     */     }
/*     */ 
/* 513 */     return true;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 518 */     MessageBox.reportError(this.m_system, msg);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/* 523 */     MessageBox.reportError(this.m_system, e);
/*     */   }
/*     */ 
/*     */   public void createAndInitEditPermissionsDialog(RoleGroupData data, String title)
/*     */   {
/* 536 */     EditPermissionsDlg dlg = new EditPermissionsDlg(this.m_system, title);
/* 537 */     dlg.init(data);
/*     */   }
/*     */ 
/*     */   protected void showHelperDialog()
/*     */   {
/* 547 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 552 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97324 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.GroupPermissionsDlg
 * JD-Core Version:    0.5.4
 */