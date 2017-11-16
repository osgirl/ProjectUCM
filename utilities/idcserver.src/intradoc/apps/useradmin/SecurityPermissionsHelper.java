/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.CustomSecurityRightsData;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.RoleDefinitions;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.DefaultListSelectionModel;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JDialog;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class SecurityPermissionsHelper
/*     */   implements ActionListener
/*     */ {
/*     */   public SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   public ContainerHelper m_helper;
/*     */   public FixedSizeList m_roleList;
/*     */   public FixedSizeList m_groupList;
/*  77 */   protected Hashtable m_buttonsMap = null;
/*     */   public boolean m_isForDialog;
/*  80 */   public Vector m_currentGroups = null;
/*  81 */   public RoleDefinitions m_roleDefs = null;
/*     */   public Vector m_visibleRoles;
/*  85 */   public boolean m_isCustomInstalled = false;
/*  86 */   public Hashtable m_roleCustomGroupDataMap = null;
/*     */   public ActionListener m_editListener;
/*     */ 
/*     */   public SecurityPermissionsHelper(SystemInterface sys, ContainerHelper helper, boolean isForDialog)
/*     */   {
/*  91 */     this.m_systemInterface = sys;
/*  92 */     this.m_ctx = sys.getExecutionContext();
/*  93 */     this.m_roleDefs = ((RoleDefinitions)SharedObjects.getTable("RoleDefinition"));
/*     */ 
/*  95 */     this.m_helper = helper;
/*     */ 
/*  97 */     this.m_isForDialog = isForDialog;
/*     */ 
/*  99 */     this.m_buttonsMap = new Hashtable();
/*     */ 
/* 101 */     CustomSecurityRightsData.init(true, sys);
/* 102 */     this.m_isCustomInstalled = CustomSecurityRightsData.m_isInitialized;
/*     */   }
/*     */ 
/*     */   public void init(JPanel parentPanel, ActionListener editListener)
/*     */   {
/* 107 */     this.m_editListener = editListener;
/*     */ 
/* 110 */     int numRowsList = 8;
/* 111 */     JPanel buttonsPanel = null;
/* 112 */     String addBtnLabel = LocaleResources.getString("apDlgButtonAddRole", this.m_ctx);
/* 113 */     String delBtnLabel = LocaleResources.getString("apLabelRemoveRole", this.m_ctx);
/* 114 */     String editBtnLabel = LocaleResources.getString("apDlgButtonEditRole", this.m_ctx);
/* 115 */     if (this.m_isForDialog)
/*     */     {
/* 117 */       addBtnLabel = LocaleResources.getString("apDlgButtonAddNewRole", this.m_ctx);
/* 118 */       delBtnLabel = LocaleResources.getString("apLabelDeleteRole", this.m_ctx);
/* 119 */       numRowsList = 15;
/*     */     }
/*     */     else
/*     */     {
/* 123 */       buttonsPanel = new PanePanel();
/* 124 */       buttonsPanel.setLayout(new GridLayout(0, 1, 10, 10));
/* 125 */       buttonsPanel.add(new CustomLabel(""));
/*     */     }
/*     */ 
/* 128 */     JPanel rolePanel = new PanePanel();
/* 129 */     rolePanel.setLayout(new BorderLayout());
/* 130 */     rolePanel.add("North", new CustomLabel(LocaleResources.getString("apLabelRoles", this.m_ctx), 1));
/*     */ 
/* 132 */     rolePanel.add("Center", this.m_roleList = new FixedSizeList(numRowsList, 125));
/* 133 */     ListSelectionListener listener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 137 */         String roleName = null;
/* 138 */         int index = SecurityPermissionsHelper.this.m_roleList.getSelectedIndex();
/* 139 */         if (index >= 0)
/*     */         {
/* 141 */           roleName = SecurityPermissionsHelper.this.m_roleList.getSelectedItem();
/*     */ 
/* 144 */           int begin = roleName.indexOf("(");
/* 145 */           if (begin >= 0)
/*     */           {
/* 147 */             int end = roleName.indexOf(")");
/* 148 */             roleName = roleName.substring(begin + 1, end);
/*     */           }
/*     */ 
/* 151 */           SecurityPermissionsHelper.this.enableCommandButton("editRole", true);
/* 152 */           SecurityPermissionsHelper.this.enableCommandButton("deleteRole", true);
/* 153 */           SecurityPermissionsHelper.this.enableCommandButton("editRights", true);
/* 154 */           SecurityPermissionsHelper.this.enableCustomCommandButtons(true);
/*     */         }
/*     */         else
/*     */         {
/* 158 */           SecurityPermissionsHelper.this.enableCommandButton("editRole", false);
/* 159 */           SecurityPermissionsHelper.this.enableCommandButton("deleteRole", false);
/* 160 */           SecurityPermissionsHelper.this.enableCommandButton("editRights", false);
/* 161 */           SecurityPermissionsHelper.this.enableCustomCommandButtons(false);
/*     */         }
/*     */ 
/* 164 */         SecurityPermissionsHelper.this.setSelectedGroups(roleName, null);
/*     */       }
/*     */     };
/* 167 */     this.m_roleList.m_selectionModel.addListSelectionListener(listener);
/* 168 */     this.m_roleList.addActionListener(this);
/*     */ 
/* 170 */     addCommandButton(buttonsPanel, addBtnLabel, editListener, "addRole", this.m_isForDialog, true);
/* 171 */     if (this.m_isForDialog)
/*     */     {
/* 173 */       addCommandButton(buttonsPanel, editBtnLabel, editListener, "editRole", this.m_isForDialog, false);
/*     */     }
/* 175 */     addCommandButton(buttonsPanel, delBtnLabel, editListener, "deleteRole", this.m_isForDialog, false);
/*     */ 
/* 177 */     JPanel groupPanel = new PanePanel();
/* 178 */     groupPanel.setLayout(new BorderLayout());
/* 179 */     groupPanel.add("North", new CustomLabel(LocaleResources.getString("apLabelGroupsRights", this.m_ctx), 1));
/*     */ 
/* 181 */     groupPanel.add("Center", this.m_groupList = new FixedSizeList(numRowsList, 150));
/*     */ 
/* 183 */     ListSelectionListener iListener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 187 */         int groupIndex = SecurityPermissionsHelper.this.m_groupList.getSelectedIndex();
/* 188 */         if (groupIndex < 0)
/*     */         {
/* 190 */           SecurityPermissionsHelper.this.enableCommandButton("editPermissions", false);
/*     */         }
/*     */         else
/*     */         {
/* 194 */           RoleGroupData data = (RoleGroupData)SecurityPermissionsHelper.this.m_currentGroups.elementAt(groupIndex);
/* 195 */           boolean isPsGroup = (data.m_groupName.charAt(0) == '#') || (data.m_groupName.charAt(0) == '$');
/* 196 */           SecurityPermissionsHelper.this.enableCommandButton("editPermissions", !isPsGroup);
/*     */         }
/*     */       }
/*     */     };
/* 200 */     this.m_groupList.m_selectionModel.addListSelectionListener(iListener);
/*     */ 
/* 202 */     JPanel possibleActionsPanel = buttonsPanel;
/* 203 */     if (this.m_isForDialog)
/*     */     {
/* 205 */       JPanel permissionsPanel = new PanePanel();
/* 206 */       permissionsPanel.setLayout(new GridLayout(0, 1, 5, 5));
/* 207 */       possibleActionsPanel = permissionsPanel;
/*     */ 
/* 210 */       String[][] buttonInfo = { { "apLabelClose", "close" }, { "apDlgButtonEditPermissions", "editPermissions" }, { "apDlgButtonEditRights", "editRights" } };
/*     */ 
/* 217 */       int len = buttonInfo.length;
/* 218 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 220 */         String cmd = buttonInfo[i][1];
/* 221 */         boolean isEnabled = true;
/* 222 */         if ((cmd.equals("editPermissions")) || (cmd.equals("editRights")) || (cmd.equals("editRmaRights")))
/*     */         {
/* 224 */           isEnabled = false;
/*     */         }
/* 226 */         addCommandButton(permissionsPanel, LocaleResources.getString(buttonInfo[i][0], this.m_ctx), this, cmd, false, isEnabled);
/*     */       }
/*     */ 
/* 230 */       addCustomCommandButtons(permissionsPanel);
/* 231 */       this.m_groupList.addActionListener(this);
/*     */     }
/*     */ 
/* 234 */     JPanel componentsPanel = parentPanel;
/* 235 */     if (!this.m_isForDialog)
/*     */     {
/* 237 */       componentsPanel = new PanePanel();
/*     */     }
/*     */ 
/* 240 */     this.m_helper.makePanelGridBag(componentsPanel, 1);
/* 241 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 242 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 243 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 5, 5, 5);
/* 244 */     this.m_helper.m_gridHelper.m_gc.anchor = 17;
/*     */ 
/* 246 */     this.m_helper.addComponent(componentsPanel, rolePanel);
/* 247 */     this.m_helper.addComponent(componentsPanel, groupPanel);
/*     */ 
/* 253 */     JPanel wrapper = new PanePanel();
/* 254 */     wrapper.setLayout(new BorderLayout());
/* 255 */     wrapper.add("North", possibleActionsPanel);
/* 256 */     this.m_helper.addComponent(componentsPanel, wrapper);
/*     */ 
/* 258 */     if (this.m_isForDialog)
/*     */       return;
/* 260 */     this.m_helper.m_gridHelper.prepareAddLastRowElement(17);
/* 261 */     this.m_helper.addComponent(parentPanel, componentsPanel);
/*     */   }
/*     */ 
/*     */   public void addCommandButton(JPanel btnsPanel, String label, ActionListener onClick, String cmd, boolean isInToolBar, boolean isEnabled)
/*     */   {
/* 268 */     JButton btn = null;
/* 269 */     if ((isInToolBar) && (this.m_helper instanceof DialogHelper))
/*     */     {
/* 271 */       DialogHelper dhelper = (DialogHelper)this.m_helper;
/* 272 */       btn = dhelper.addCommandButton(label, onClick);
/*     */     }
/*     */     else
/*     */     {
/* 276 */       btn = new JButton(label);
/* 277 */       btnsPanel.add(btn);
/* 278 */       btn.addActionListener(onClick);
/*     */     }
/*     */ 
/* 281 */     btn.setActionCommand(cmd);
/* 282 */     btn.setEnabled(isEnabled);
/* 283 */     this.m_buttonsMap.put(cmd, btn);
/*     */   }
/*     */ 
/*     */   protected void enableCommandButton(String name, boolean isEnabled)
/*     */   {
/* 288 */     JButton btn = (JButton)this.m_buttonsMap.get(name);
/* 289 */     if (btn == null)
/*     */       return;
/* 291 */     btn.setEnabled(isEnabled);
/*     */   }
/*     */ 
/*     */   public void refreshLists()
/*     */   {
/* 297 */     refreshLists(null);
/*     */   }
/*     */ 
/*     */   public void refreshLists(String roleName)
/*     */   {
/* 302 */     refreshLists(roleName, null);
/*     */   }
/*     */ 
/*     */   protected void refreshLists(String roleName, String groupName)
/*     */   {
/* 307 */     this.m_roleDefs = ((RoleDefinitions)SharedObjects.getTable("RoleDefinition"));
/* 308 */     boolean useDisplayNames = SharedObjects.getEnvValueAsBoolean("UseRoleAndAliasDisplayNames", false);
/*     */ 
/* 310 */     int selectedIndex = -1;
/* 311 */     this.m_groupList.removeAllItems();
/* 312 */     if (this.m_roleList != null)
/*     */     {
/* 314 */       this.m_roleList.removeAllItems();
/*     */ 
/* 316 */       Vector roleNames = this.m_visibleRoles;
/* 317 */       if (roleNames == null)
/*     */       {
/* 320 */         reportError(IdcMessageFactory.lc("apNoRolesInSystem", new Object[0]));
/* 321 */         return;
/*     */       }
/* 323 */       int size = roleNames.size();
/* 324 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 326 */         String name = (String)roleNames.elementAt(i);
/* 327 */         if (useDisplayNames)
/*     */         {
/* 330 */           String displayName = null;
/*     */           try
/*     */           {
/* 333 */             displayName = this.m_roleDefs.getRoleDisplayName(name);
/*     */           }
/*     */           catch (DataException de)
/*     */           {
/*     */           }
/*     */ 
/* 339 */           if ((displayName != null) && (displayName.length() > 0))
/*     */           {
/* 341 */             name = displayName + " (" + name + ")";
/*     */           }
/*     */         }
/* 344 */         this.m_roleList.add(name);
/* 345 */         if ((roleName == null) || (!roleName.equalsIgnoreCase(name)))
/*     */           continue;
/* 347 */         selectedIndex = i;
/*     */       }
/*     */ 
/* 351 */       boolean enable = false;
/* 352 */       if (selectedIndex >= 0)
/*     */       {
/* 355 */         int begin = roleName.indexOf("(");
/* 356 */         if (begin >= 0)
/*     */         {
/* 358 */           int end = roleName.indexOf(")");
/* 359 */           roleName = roleName.substring(begin + 1, end);
/*     */         }
/*     */ 
/* 362 */         this.m_roleList.select(selectedIndex);
/* 363 */         setSelectedGroups(roleName, groupName);
/* 364 */         enable = true;
/*     */       }
/* 366 */       enableCommandButton("editRole", enable);
/* 367 */       enableCommandButton("deleteRole", enable);
/* 368 */       enableCommandButton("editRights", enable);
/*     */     }
/*     */     else
/*     */     {
/* 372 */       int size = this.m_currentGroups.size();
/* 373 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 375 */         RoleGroupData data = (RoleGroupData)this.m_currentGroups.elementAt(i);
/* 376 */         String str = data.m_groupName + "  (" + SecurityAccessListUtils.makePrivilegeStr(data.m_privilege) + ")";
/* 377 */         this.m_groupList.add(str);
/* 378 */         if ((groupName == null) || (!groupName.equalsIgnoreCase(data.m_groupName)))
/*     */           continue;
/* 380 */         selectedIndex = i;
/*     */       }
/*     */ 
/* 384 */       boolean enable = false;
/* 385 */       if (selectedIndex >= 0)
/*     */       {
/* 387 */         this.m_groupList.select(selectedIndex);
/* 388 */         enable = true;
/*     */       }
/* 390 */       enableCommandButton("editPermissions", enable);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setSelectedGroups(String roleName, String groupName)
/*     */   {
/* 396 */     this.m_groupList.removeAllItems();
/* 397 */     if (roleName == null)
/*     */     {
/* 399 */       return;
/*     */     }
/*     */ 
/* 402 */     this.m_currentGroups = this.m_roleDefs.getRoleGroups(roleName);
/* 403 */     if (this.m_currentGroups == null)
/*     */     {
/* 405 */       reportError(IdcMessageFactory.lc("apNoGroupsDefinedForRole", new Object[] { roleName }));
/* 406 */       return;
/*     */     }
/*     */ 
/* 409 */     this.m_currentGroups = ((Vector)this.m_currentGroups.clone());
/* 410 */     this.m_roleCustomGroupDataMap = new Hashtable();
/*     */ 
/* 413 */     int index = this.m_roleDefs.getFieldInfoIndex("dRoleName");
/* 414 */     this.m_roleDefs.findRow(index, roleName);
/* 415 */     String displayName = this.m_roleDefs.getStringValueByName("dRoleDisplayName");
/*     */ 
/* 418 */     Vector psGroups = this.m_roleDefs.getPsRoleGroups(roleName);
/* 419 */     int count = 0;
/* 420 */     if (psGroups != null)
/*     */     {
/* 422 */       int num = psGroups.size();
/* 423 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 425 */         RoleGroupData data = (RoleGroupData)psGroups.elementAt(i);
/* 426 */         String gName = data.m_groupName;
/* 427 */         if (gName.equals("#AppsGroup"))
/*     */         {
/* 429 */           long priv = data.m_privilege;
/* 430 */           String[][] defs = PermissionsData.m_appAllPsgDefs;
/* 431 */           for (int j = 0; j < defs.length; ++j)
/*     */           {
/* 433 */             long p = NumberUtils.parseHexStringAsLong(defs[j][2]);
/* 434 */             if ((p & priv) == 0L)
/*     */               continue;
/* 436 */             RoleGroupData rgData = new RoleGroupData(roleName, gName, p, displayName);
/* 437 */             this.m_currentGroups.insertElementAt(rgData, count);
/* 438 */             this.m_groupList.add(LocaleResources.getString(defs[j][0], this.m_ctx));
/* 439 */             ++count;
/*     */           }
/*     */         }
/*     */         else {
/* 443 */           if (!gName.startsWith("$"))
/*     */             continue;
/* 445 */           count = setSelectedCustomGroup(data, count);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 450 */     int selectedIndex = -1;
/* 451 */     int size = this.m_currentGroups.size();
/* 452 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 454 */       RoleGroupData data = (RoleGroupData)this.m_currentGroups.elementAt(i);
/* 455 */       String gName = data.m_groupName;
/* 456 */       if ((gName.charAt(0) != '#') && (gName.charAt(0) != '$'))
/*     */       {
/* 458 */         this.m_groupList.add(data.m_groupName + "  (" + SecurityAccessListUtils.makePrivilegeStr(data.m_privilege) + ")");
/*     */       }
/* 460 */       if ((groupName == null) || (!groupName.equalsIgnoreCase(data.m_groupName)))
/*     */         continue;
/* 462 */       selectedIndex = i;
/*     */     }
/*     */ 
/* 466 */     boolean enable = false;
/* 467 */     if (selectedIndex >= 0)
/*     */     {
/* 469 */       this.m_groupList.select(selectedIndex);
/* 470 */       enable = true;
/*     */     }
/* 472 */     boolean isPsGroup = (groupName != null) && (((groupName.charAt(0) == '#') || (groupName.charAt(0) == '$')));
/* 473 */     enableCommandButton("editPermissions", (!isPsGroup) && (enable));
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 483 */     Object src = e.getSource();
/* 484 */     if (src == this.m_groupList)
/*     */     {
/* 487 */       int index = this.m_groupList.getSelectedIndex();
/* 488 */       if (index < 0)
/*     */       {
/* 490 */         return;
/*     */       }
/* 492 */       RoleGroupData data = (RoleGroupData)this.m_currentGroups.elementAt(index);
/* 493 */       boolean isPermissions = (data.m_groupName.charAt(0) != '#') && (data.m_groupName.charAt(0) != '$');
/* 494 */       boolean isAppletRights = data.m_groupName.charAt(0) == '#';
/* 495 */       editRolePrivileges(isPermissions, isAppletRights);
/* 496 */       return;
/*     */     }
/* 498 */     if (src == this.m_roleList)
/*     */     {
/* 501 */       int index = this.m_roleList.getSelectedIndex();
/* 502 */       if (index < 0)
/*     */       {
/* 504 */         return;
/*     */       }
/*     */ 
/* 507 */       ActionEvent evt = new ActionEvent(this.m_roleList, 1001, "editRole");
/* 508 */       this.m_editListener.actionPerformed(evt);
/*     */ 
/* 510 */       return;
/*     */     }
/*     */ 
/* 513 */     String cmd = e.getActionCommand();
/* 514 */     if (cmd.equals("close"))
/*     */     {
/* 516 */       closeAttachedDialog();
/*     */     }
/* 518 */     else if (cmd.equals("editPermissions"))
/*     */     {
/* 520 */       editRolePrivileges(true, false);
/*     */ 
/* 525 */       ActionEvent evt = new ActionEvent(this.m_roleList, 1001, "editPermissions");
/* 526 */       this.m_editListener.actionPerformed(evt);
/*     */     }
/* 528 */     else if (cmd.equals("editRights"))
/*     */     {
/* 530 */       editRolePrivileges(false, true);
/*     */     }
/* 532 */     else if (cmd.equals("editRmaRights"))
/*     */     {
/* 534 */       editRolePrivileges(false, false);
/*     */     } else {
/* 536 */       if (!cmd.startsWith("editCustomRights"))
/*     */         return;
/* 538 */       editRoleCustomPrivileges(cmd);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void closeAttachedDialog()
/*     */   {
/* 544 */     if (!this.m_helper instanceof DialogHelper)
/*     */       return;
/* 546 */     DialogHelper dhelper = (DialogHelper)this.m_helper;
/* 547 */     dhelper.m_dialog.setVisible(false);
/* 548 */     dhelper.m_dialog.dispose();
/*     */   }
/*     */ 
/*     */   protected void editRolePrivileges(boolean isPermissions, boolean isAppletRights)
/*     */   {
/* 554 */     String roleName = this.m_roleList.getSelectedItem();
/* 555 */     if ((roleName == null) || (roleName.length() == 0))
/*     */     {
/* 557 */       return;
/*     */     }
/*     */ 
/* 561 */     int begin = roleName.indexOf("(");
/* 562 */     if (begin >= 0)
/*     */     {
/* 564 */       int end = roleName.indexOf(")");
/* 565 */       roleName = roleName.substring(begin + 1, end);
/*     */     }
/*     */ 
/* 571 */     if ((roleName.equalsIgnoreCase("admin")) && (!isPermissions))
/*     */     {
/* 573 */       reportError(IdcMessageFactory.lc("apRoleCannotChangeApplicationRights", new Object[] { roleName }));
/* 574 */       return;
/*     */     }
/*     */ 
/* 577 */     RoleGroupData data = createRoleGroupData(roleName, isPermissions, isAppletRights);
/* 578 */     if (data == null)
/*     */     {
/* 580 */       reportError(IdcMessageFactory.lc("apUnableToEditPermissions", new Object[0]));
/* 581 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 586 */       String title = LocaleResources.getString("apTitleEditPermissions", this.m_ctx);
/* 587 */       if ((!isPermissions) && 
/* 589 */         (isAppletRights))
/*     */       {
/* 591 */         title = LocaleResources.getString("apTitleEditRights", this.m_ctx);
/*     */       }
/*     */ 
/* 595 */       createAndInitEditPermissionsDialog(data, title);
/* 596 */       long priv = data.m_privilege;
/* 597 */       DataBinder binder = new DataBinder();
/* 598 */       Properties localData = binder.getLocalData();
/* 599 */       localData.put("dRoleName", data.m_roleName);
/* 600 */       localData.put("dGroupName", data.m_groupName);
/* 601 */       localData.put("dPrivilege", String.valueOf(priv));
/* 602 */       localData.put("dRoleDisplayName", data.m_roleDisplayName);
/* 603 */       AppLauncher.executeService("EDIT_ROLE", binder);
/*     */ 
/* 605 */       refreshLists(data.m_roleName, data.m_groupName);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 609 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected RoleGroupData createRoleGroupData(String roleName, boolean isPermissions, boolean isAppletRights)
/*     */   {
/* 616 */     RoleGroupData data = null;
/* 617 */     String errMsg = null;
/* 618 */     if (isPermissions)
/*     */     {
/* 620 */       int groupIndex = this.m_groupList.getSelectedIndex();
/* 621 */       if (groupIndex < 0)
/*     */       {
/* 623 */         errMsg = LocaleResources.getString("apSelectRole", this.m_ctx);
/*     */       }
/*     */       else
/*     */       {
/* 627 */         data = (RoleGroupData)this.m_currentGroups.elementAt(groupIndex);
/*     */       }
/*     */ 
/* 630 */       if ((data != null) && (data.m_groupName.charAt(0) == '#'))
/*     */       {
/* 632 */         data = null;
/*     */       }
/*     */     }
/* 635 */     else if (isAppletRights)
/*     */     {
/* 637 */       int priv = 0;
/* 638 */       String displayName = "";
/*     */       try
/*     */       {
/* 641 */         priv = this.m_roleDefs.getRolePrivilege(roleName, "#AppsGroup");
/* 642 */         displayName = this.m_roleDefs.getRoleDisplayName(roleName);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 646 */         if (SystemUtils.m_verbose)
/*     */         {
/* 648 */           Report.debug("applet", null, ignore);
/*     */         }
/*     */       }
/* 651 */       data = new RoleGroupData(roleName, "#AppsGroup", priv, displayName);
/*     */     }
/*     */ 
/* 654 */     if (errMsg != null)
/*     */     {
/* 656 */       Report.trace("applet", errMsg, null);
/*     */     }
/* 658 */     return data;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 663 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/* 668 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   protected void addCustomCommandButtons(JPanel panel)
/*     */   {
/* 673 */     if (!this.m_isCustomInstalled)
/*     */     {
/* 675 */       return;
/*     */     }
/*     */ 
/* 679 */     boolean isCustomButton = false;
/*     */ 
/* 681 */     Vector classList = CustomSecurityRightsData.m_classList;
/* 682 */     int numClasses = classList.size();
/* 683 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 685 */       String className = (String)classList.elementAt(i);
/* 686 */       boolean isUseButton = CustomSecurityRightsData.isUseButton(className);
/* 687 */       if (isUseButton)
/*     */       {
/* 689 */         String cmd = "editCustomRights:" + className;
/* 690 */         String btnLabel = CustomSecurityRightsData.getButtonLabel(this.m_ctx, className);
/* 691 */         addCommandButton(panel, btnLabel, this, cmd, false, false);
/*     */       }
/*     */       else
/*     */       {
/* 695 */         isCustomButton = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 700 */     if (!isCustomButton)
/*     */       return;
/* 702 */     String label = LocaleResources.getString("apDlgButtonEditExtendedRights", this.m_ctx);
/* 703 */     addCommandButton(panel, label, this, "editCustomRights", false, false);
/*     */   }
/*     */ 
/*     */   public void enableCustomCommandButtons(boolean isEnable)
/*     */   {
/* 709 */     if (!this.m_isCustomInstalled)
/*     */     {
/* 711 */       return;
/*     */     }
/*     */ 
/* 714 */     Enumeration en = this.m_buttonsMap.keys();
/* 715 */     while (en.hasMoreElements())
/*     */     {
/* 717 */       String cmd = (String)en.nextElement();
/* 718 */       if (cmd.startsWith("editCustomRights"))
/*     */       {
/* 720 */         enableCommandButton(cmd, isEnable);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int setSelectedCustomGroup(RoleGroupData data, int count)
/*     */   {
/* 727 */     if (!this.m_isCustomInstalled)
/*     */     {
/* 729 */       return count;
/*     */     }
/*     */ 
/* 732 */     String groupName = data.m_groupName;
/* 733 */     long rolePrivilege = data.m_customPrivilege;
/*     */ 
/* 735 */     String className = CustomSecurityRightsData.getClassFromGroup(groupName);
/* 736 */     Vector rightList = (Vector)CustomSecurityRightsData.m_classRightMap.get(className);
/* 737 */     if (rightList == null)
/*     */     {
/* 739 */       return count;
/*     */     }
/*     */ 
/* 742 */     int numRights = rightList.size();
/* 743 */     for (int i = 0; i < numRights; ++i)
/*     */     {
/* 745 */       String right = (String)rightList.elementAt(i);
/* 746 */       long rightPrivilege = CustomSecurityRightsData.getRightPrivilege(right);
/* 747 */       if ((rolePrivilege & rightPrivilege) != rightPrivilege) {
/*     */         continue;
/*     */       }
/* 750 */       String label = CustomSecurityRightsData.getSubClassRightLabel(this.m_ctx, right);
/* 751 */       String classLabel = CustomSecurityRightsData.capitalizeClass(className);
/* 752 */       label = label + " (" + classLabel + ")";
/*     */ 
/* 754 */       RoleGroupData rgData = new RoleGroupData(data.m_roleName, groupName, rightPrivilege, data.m_roleDisplayName);
/* 755 */       this.m_currentGroups.insertElementAt(rgData, count);
/* 756 */       this.m_groupList.add(label);
/* 757 */       ++count;
/*     */     }
/*     */ 
/* 761 */     RoleGroupData rgData = new RoleGroupData(data.m_roleName, groupName, rolePrivilege, data.m_roleDisplayName);
/* 762 */     this.m_roleCustomGroupDataMap.put(className, rgData);
/*     */ 
/* 764 */     return count;
/*     */   }
/*     */ 
/*     */   public void editRoleCustomPrivileges(String cmd)
/*     */   {
/* 769 */     if (!this.m_isCustomInstalled)
/*     */     {
/* 771 */       return;
/*     */     }
/*     */ 
/* 775 */     String roleName = this.m_roleList.getSelectedItem();
/* 776 */     if ((roleName == null) || (roleName.length() == 0))
/*     */     {
/* 778 */       return;
/*     */     }
/*     */ 
/* 782 */     String displayName = null;
/* 783 */     int begin = roleName.indexOf("(");
/* 784 */     if (begin >= 0)
/*     */     {
/* 786 */       int end = roleName.indexOf(")");
/* 787 */       displayName = roleName.substring(0, begin - 1);
/* 788 */       roleName = roleName.substring(begin + 1, end);
/*     */     }
/*     */     else
/*     */     {
/* 792 */       int colIndex = this.m_roleDefs.getFieldInfoIndex("dRoleName");
/* 793 */       this.m_roleDefs.findRow(colIndex, roleName);
/* 794 */       displayName = this.m_roleDefs.getStringValueByName("dRoleDisplayName");
/*     */     }
/*     */ 
/* 797 */     String title = null;
/* 798 */     Vector rgDataList = new IdcVector();
/*     */ 
/* 800 */     if (cmd.equals("editCustomRights"))
/*     */     {
/* 802 */       title = LocaleResources.getString("apTitleEditExtendedRights", this.m_ctx);
/*     */ 
/* 804 */       Vector classList = CustomSecurityRightsData.m_classList;
/* 805 */       int numClasses = classList.size();
/* 806 */       for (int i = 0; i < numClasses; ++i)
/*     */       {
/* 808 */         String className = (String)classList.elementAt(i);
/* 809 */         if (CustomSecurityRightsData.isUseButton(className))
/*     */           continue;
/* 811 */         RoleGroupData rgData = (RoleGroupData)this.m_roleCustomGroupDataMap.get(className);
/* 812 */         if (rgData == null)
/*     */         {
/* 814 */           String groupName = CustomSecurityRightsData.getClassGroup(className);
/* 815 */           rgData = new RoleGroupData(roleName, groupName, 0L, displayName);
/* 816 */           rgData.m_customPrivilege = 0L;
/*     */         }
/* 818 */         rgDataList.addElement(rgData);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 824 */       int index = cmd.indexOf(":");
/* 825 */       String className = cmd.substring(index + 1);
/* 826 */       String groupName = CustomSecurityRightsData.getClassGroup(className);
/* 827 */       title = CustomSecurityRightsData.getWindowLabel(this.m_ctx, className);
/*     */ 
/* 829 */       RoleGroupData rgData = (RoleGroupData)this.m_roleCustomGroupDataMap.get(className);
/* 830 */       if (rgData == null)
/*     */       {
/* 832 */         rgData = new RoleGroupData(roleName, groupName, 0L, displayName);
/* 833 */         rgData.m_customPrivilege = 0L;
/*     */       }
/* 835 */       rgDataList.addElement(rgData);
/*     */     }
/*     */ 
/* 838 */     EditPermissionsDlg dlg = new EditPermissionsDlg(this.m_systemInterface, title);
/* 839 */     dlg.initCustom(rgDataList);
/*     */     try
/*     */     {
/* 844 */       int numClasses = rgDataList.size();
/* 845 */       for (int i = 0; i < numClasses; ++i)
/*     */       {
/* 847 */         RoleGroupData rgData = (RoleGroupData)rgDataList.elementAt(i);
/*     */ 
/* 849 */         DataBinder binder = new DataBinder();
/* 850 */         Properties localData = binder.getLocalData();
/* 851 */         localData.put("dRoleName", roleName);
/* 852 */         localData.put("dGroupName", rgData.m_groupName);
/* 853 */         localData.put("dPrivilege", "" + rgData.m_customPrivilege);
/* 854 */         localData.put("dRoleDisplayName", rgData.m_roleDisplayName);
/* 855 */         AppLauncher.executeService("EDIT_ROLE", binder);
/*     */ 
/* 857 */         refreshLists(roleName, rgData.m_groupName);
/*     */       }
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 862 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createAndInitEditPermissionsDialog(RoleGroupData data, String title)
/*     */   {
/* 875 */     EditPermissionsDlg dlg = new EditPermissionsDlg(this.m_systemInterface, title);
/* 876 */     dlg.init(data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 881 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92578 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.SecurityPermissionsHelper
 * JD-Core Version:    0.5.4
 */