/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.CustomDialog;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.gui.iwt.ComboChoice;
/*     */ import intradoc.shared.CustomSecurityRightsData;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.TextEvent;
/*     */ import java.awt.event.TextListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditPermissionsDlg extends CustomDialog
/*     */   implements ItemListener, ActionListener
/*     */ {
/*     */   protected DialogHelper m_helper;
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected ExecutionContext m_ctx;
/*     */   protected PermissionBox[] m_permBoxes;
/*     */   protected JButton m_ok;
/*     */   protected JButton m_cancel;
/*     */   protected boolean m_promptResult;
/*     */   protected PermissionsData m_permissionsData;
/*  87 */   protected String m_groupName = null;
/*     */   protected RoleGroupData m_roleGroupData;
/*  90 */   protected boolean m_isAccount = false;
/*  91 */   protected boolean m_addingAccount = false;
/*  92 */   protected SecurityEditHelper m_securityEditHelper = null;
/*     */ 
/*  94 */   protected UserData m_userData = null;
/*  95 */   protected UserData m_loggedInUserData = null;
/*  96 */   protected boolean m_isAdmin = false;
/*     */   protected boolean m_useCollaboration;
/* 101 */   protected boolean m_isCustomPermissions = false;
/* 102 */   protected Properties m_customPermBoxMap = null;
/* 103 */   protected Hashtable m_customClassPermBoxMap = null;
/* 104 */   protected Vector m_customClassList = null;
/* 105 */   protected Vector m_rgDataList = null;
/*     */ 
/*     */   public EditPermissionsDlg(SystemInterface sys, String title)
/*     */   {
/* 110 */     super(sys.getMainWindow(), title, true);
/* 111 */     this.m_systemInterface = sys;
/* 112 */     this.m_ctx = sys.getExecutionContext();
/* 113 */     this.m_helper = new DialogHelper();
/* 114 */     this.m_helper.attachToDialog(this, sys, null);
/*     */ 
/* 116 */     this.m_permissionsData = null;
/* 117 */     this.m_permBoxes = null;
/* 118 */     this.m_promptResult = false;
/*     */ 
/* 120 */     this.m_securityEditHelper = new SecurityEditHelper(this.m_helper, sys);
/*     */ 
/* 122 */     this.m_useCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/*     */   }
/*     */ 
/*     */   public void init(RoleGroupData data)
/*     */   {
/* 127 */     this.m_groupName = data.m_groupName;
/* 128 */     String labelStr = "";
/* 129 */     if (this.m_groupName.charAt(0) == '#')
/*     */     {
/* 131 */       labelStr = LocaleResources.getString("apEditRightsForRole", this.m_ctx, data.m_roleName);
/*     */     }
/*     */     else
/*     */     {
/* 136 */       labelStr = LocaleResources.getString("apEditRightsForGroupAndRole", this.m_ctx, data.m_groupName, data.m_roleName);
/*     */     }
/*     */ 
/* 139 */     this.m_roleGroupData = data;
/* 140 */     initEx(data, false, false, null, labelStr);
/*     */   }
/*     */ 
/*     */   public boolean promptAccount(PermissionsData permData, Properties props, UserData loggedInUser, UserData userData, String account, boolean isAdd)
/*     */   {
/* 146 */     this.m_helper.m_props = props;
/* 147 */     if (account == null)
/*     */     {
/* 149 */       account = "";
/*     */     }
/* 151 */     this.m_userData = userData;
/* 152 */     this.m_loggedInUserData = loggedInUser;
/* 153 */     this.m_securityEditHelper.m_userData = loggedInUser;
/*     */     String labelStr;
/*     */     String labelStr;
/* 156 */     if (account.equals("#none"))
/*     */     {
/* 158 */       labelStr = LocaleResources.getString("apNoneAccountDesc", this.m_ctx);
/*     */     }
/*     */     else
/*     */     {
/*     */       String labelStr;
/* 160 */       if (account.equals("#all"))
/*     */       {
/* 162 */         labelStr = LocaleResources.getString("apAllAccountDesc", this.m_ctx);
/*     */       }
/*     */       else
/*     */       {
/* 166 */         labelStr = LocaleResources.getString("apNormalAccountDesc", this.m_ctx);
/*     */       }
/*     */     }
/* 169 */     initEx(permData, true, isAdd, account, labelStr);
/* 170 */     return this.m_promptResult;
/*     */   }
/*     */ 
/*     */   public void initEx(PermissionsData permData, boolean isAccount, boolean isAdd, String account, String labelStr)
/*     */   {
/* 176 */     this.m_isAdmin = AppLauncher.isAdmin();
/* 177 */     this.m_isAccount = isAccount;
/* 178 */     this.m_permissionsData = permData;
/* 179 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 180 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/* 183 */     if (this.m_isAccount)
/*     */     {
/* 185 */       String displayAccount = this.m_userData.getAccountPresentationString(account, this.m_ctx);
/* 186 */       this.m_helper.m_props.put("dDocAccount", displayAccount);
/*     */ 
/* 188 */       JPanel accountsPanel = new PanePanel();
/* 189 */       this.m_addingAccount = isAdd;
/*     */ 
/* 191 */       if (isAdd)
/*     */       {
/* 193 */         this.m_securityEditHelper.addAccountEditField(accountsPanel, LocaleResources.getString("apLabelAccount", this.m_ctx), "dDocAccount");
/*     */ 
/* 195 */         this.m_securityEditHelper.refreshAccountChoiceList(true, 1);
/* 196 */         TextListener accListener = new TextListener()
/*     */         {
/*     */           public void textValueChanged(TextEvent evt)
/*     */           {
/* 200 */             long priv = 0L;
/* 201 */             if (EditPermissionsDlg.this.m_isAdmin)
/*     */             {
/* 203 */               priv = 15L;
/*     */             }
/*     */             else
/*     */             {
/* 207 */               String selStr = EditPermissionsDlg.this.m_securityEditHelper.m_accountChoices.getSelectedItem();
/* 208 */               String curAccount = EditPermissionsDlg.this.m_loggedInUserData.getAccountInternalString(selStr, EditPermissionsDlg.this.m_ctx);
/* 209 */               if (curAccount == null)
/*     */               {
/* 211 */                 curAccount = selStr;
/*     */               }
/*     */               try
/*     */               {
/* 215 */                 priv = SecurityUtils.computeAccountPrivilege(EditPermissionsDlg.this.m_loggedInUserData, curAccount, 0);
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/* 219 */                 if (SystemUtils.m_verbose)
/*     */                 {
/* 221 */                   Report.debug("applet", null, e);
/*     */                 }
/*     */               }
/*     */             }
/* 225 */             EditPermissionsDlg.this.initPrivileges(priv);
/*     */           }
/*     */         };
/* 228 */         this.m_securityEditHelper.m_accountChoices.addTextListener(accListener);
/*     */       }
/*     */       else
/*     */       {
/* 232 */         Component fld = new CustomLabel(displayAccount);
/* 233 */         this.m_helper.addLabelFieldPairEx(accountsPanel, LocaleResources.getString("apLabelAccount", this.m_ctx), fld, "dDocAccount", true);
/*     */       }
/*     */ 
/* 236 */       mainPanel.add("North", accountsPanel);
/*     */     }
/*     */ 
/* 239 */     if (labelStr != null)
/*     */     {
/* 241 */       mainPanel.add("Center", new CustomText(labelStr));
/*     */     }
/*     */ 
/* 245 */     this.m_ok = this.m_helper.addCommandButton(LocaleResources.getString("apLabelOK", this.m_ctx), this);
/* 246 */     this.m_cancel = this.m_helper.addCommandButton(LocaleResources.getString("apLabelCancel", this.m_ctx), this);
/*     */ 
/* 248 */     JPanel permissionsPnl = new PanePanel();
/* 249 */     permissionsPnl.setLayout(new GridLayout(0, 1, 0, 0));
/*     */ 
/* 254 */     addPermissionsPanel(permissionsPnl);
/*     */ 
/* 256 */     long privilege = this.m_permissionsData.m_privilege;
/* 257 */     initPermissionsPanel(permissionsPnl);
/* 258 */     initPrivileges(privilege);
/* 259 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   protected void initPrivileges(long privilege)
/*     */   {
/* 264 */     int length = this.m_permBoxes.length;
/* 265 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 267 */       boolean state = false;
/* 268 */       if ((this.m_permBoxes[i].m_privilege & privilege) != 0L)
/*     */       {
/* 270 */         state = true;
/*     */       }
/* 272 */       this.m_permBoxes[i].setSelected(state);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initPermissionsPanel(JPanel pnl)
/*     */   {
/* 278 */     String[][] defs = PermissionsData.m_defs;
/* 279 */     boolean hasItemListener = true;
/* 280 */     if ((this.m_groupName != null) && 
/* 282 */       (this.m_groupName.charAt(0) == '#'))
/*     */     {
/* 284 */       defs = PermissionsData.m_appPsgDefs;
/* 285 */       hasItemListener = false;
/*     */     }
/*     */ 
/* 289 */     int length = defs.length;
/* 290 */     this.m_permBoxes = new PermissionBox[length];
/*     */ 
/* 292 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 294 */       this.m_permBoxes[i] = new PermissionBox(LocaleResources.getString(defs[i][0], this.m_ctx), defs[i][1], defs[i][2], null, null);
/*     */ 
/* 296 */       if (hasItemListener)
/*     */       {
/* 298 */         this.m_permBoxes[i].addItemListener(this);
/*     */       }
/* 300 */       pnl.add(this.m_permBoxes[i]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 306 */     boolean isSelected = false;
/* 307 */     if (e.getStateChange() == 1)
/*     */     {
/* 309 */       isSelected = true;
/*     */     }
/*     */ 
/* 312 */     PermissionBox checkBox = (PermissionBox)e.getSource();
/*     */ 
/* 315 */     if (this.m_isCustomPermissions)
/*     */     {
/* 317 */       selectCustomPermissionBoxes(checkBox, isSelected);
/* 318 */       return;
/*     */     }
/*     */ 
/* 322 */     long privilege = checkBox.m_privilege;
/* 323 */     int length = this.m_permBoxes.length;
/*     */ 
/* 325 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 327 */       if ((isSelected == true) && (this.m_permBoxes[i].m_privilege <= privilege))
/*     */       {
/* 329 */         this.m_permBoxes[i].setSelected(isSelected);
/*     */       } else {
/* 331 */         if ((isSelected) || (this.m_permBoxes[i].m_privilege <= privilege))
/*     */           continue;
/* 333 */         this.m_permBoxes[i].setSelected(isSelected);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 340 */     Object btn = e.getSource();
/* 341 */     IdcMessage errMsg = null;
/* 342 */     if (btn == this.m_ok)
/*     */     {
/* 345 */       if (this.m_isCustomPermissions)
/*     */       {
/* 347 */         saveCustomPermissions();
/*     */       }
/*     */       else
/*     */       {
/* 353 */         long privilege = 0L;
/* 354 */         privilege = getPrivilege();
/* 355 */         this.m_permissionsData.setPrivilege((int)privilege);
/*     */ 
/* 357 */         if (this.m_isAccount)
/*     */         {
/* 359 */           errMsg = handleAccountAction((int)privilege);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 364 */     if (errMsg == null)
/*     */     {
/* 366 */       this.m_promptResult = (btn == this.m_ok);
/* 367 */       setVisible(false);
/* 368 */       dispose();
/*     */     }
/*     */     else
/*     */     {
/* 372 */       reportError(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcMessage handleAccountAction(int privilege)
/*     */   {
/* 378 */     IdcMessage errMsg = null;
/*     */ 
/* 380 */     this.m_helper.retrieveComponentValues();
/* 381 */     String selStr = this.m_helper.m_props.getProperty("dDocAccount");
/* 382 */     if ((selStr == null) || (selStr.length() == 0))
/*     */     {
/* 384 */       errMsg = IdcMessageFactory.lc("apAccountUndefined", new Object[0]);
/* 385 */       return errMsg;
/*     */     }
/*     */ 
/* 388 */     String account = this.m_userData.getAccountInternalString(selStr, this.m_ctx);
/* 389 */     if (account == null)
/*     */     {
/* 392 */       account = FileUtils.fileSlashes(selStr);
/*     */     }
/*     */ 
/* 395 */     if (this.m_addingAccount)
/*     */     {
/* 398 */       String[] errMsgStub = { null };
/* 399 */       if (!UserUtils.isValidAccountName(account, errMsgStub, 2, this.m_ctx))
/*     */       {
/* 401 */         errMsg = IdcMessageFactory.lc("apErrorWithAccount", new Object[] { selStr });
/* 402 */         errMsg.m_prior = IdcMessageFactory.lc();
/* 403 */         errMsg.m_prior.m_msgEncoded = errMsgStub[0];
/*     */       }
/*     */ 
/* 406 */       if (errMsg == null)
/*     */       {
/*     */         try
/*     */         {
/* 411 */           boolean isAdmin = AppLauncher.isAdmin();
/* 412 */           if ((!isAdmin) && 
/* 414 */             (!SecurityUtils.isAccountAccessible(this.m_loggedInUserData, account, privilege)))
/*     */           {
/* 417 */             errMsg = IdcMessageFactory.lc("apInsufficientPrivilegeToAddAccount", new Object[] { account });
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (ServiceException ignore)
/*     */         {
/* 424 */           ignore.printStackTrace();
/*     */         }
/*     */       }
/* 427 */       this.m_helper.m_props.put("dDocAccount", account);
/*     */     }
/* 429 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public Insets getInsets()
/*     */   {
/* 438 */     Insets curInsets = super.getInsets();
/* 439 */     curInsets.top += 10;
/* 440 */     curInsets.left += 10;
/* 441 */     curInsets.right += 5;
/* 442 */     return curInsets;
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 447 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   public void initCustom(Vector rgDataList)
/*     */   {
/* 453 */     this.m_isCustomPermissions = true;
/* 454 */     this.m_customPermBoxMap = new Properties();
/* 455 */     this.m_customClassPermBoxMap = new Hashtable();
/* 456 */     this.m_customClassList = new IdcVector();
/* 457 */     this.m_rgDataList = rgDataList;
/*     */ 
/* 459 */     int numClasses = rgDataList.size();
/* 460 */     if (numClasses == 0)
/*     */     {
/* 462 */       return;
/*     */     }
/*     */ 
/* 466 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 467 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/* 470 */     RoleGroupData rgData = (RoleGroupData)rgDataList.elementAt(0);
/* 471 */     String roleName = rgData.m_roleName;
/*     */ 
/* 473 */     String labelStr = null;
/* 474 */     if (numClasses > 1)
/*     */     {
/* 476 */       labelStr = LocaleResources.getString("apEditExtendedRightsForRole", this.m_ctx, roleName, null);
/*     */     }
/*     */     else
/*     */     {
/* 480 */       String groupName = rgData.m_groupName;
/* 481 */       String className = CustomSecurityRightsData.getClassFromGroup(groupName);
/* 482 */       DataResultSet drset = SharedObjects.getTable("CustomSecurityClasses");
/*     */ 
/* 484 */       String classLabel = null;
/*     */       try
/*     */       {
/* 487 */         classLabel = LocaleResources.getString(ResultSetUtils.findValue(drset, "className", className, "label"), this.m_ctx);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 492 */         classLabel = CustomSecurityRightsData.capitalizeClass(className);
/*     */       }
/* 494 */       labelStr = LocaleResources.getString("apEditCustomRightsForRole", this.m_ctx, classLabel, roleName);
/*     */     }
/* 496 */     labelStr = labelStr + "                                                ";
/*     */ 
/* 498 */     mainPanel.add("Center", new CustomText(labelStr));
/*     */ 
/* 500 */     JPanel permissionPanel = new PanePanel();
/* 501 */     permissionPanel.setLayout(new GridLayout(0, 1, 0, 0));
/* 502 */     mainPanel.add("South", permissionPanel);
/*     */ 
/* 504 */     initCustomPanels(permissionPanel, rgDataList);
/*     */ 
/* 507 */     this.m_ok = this.m_helper.addCommandButton(LocaleResources.getString("apLabelOK", this.m_ctx), this);
/* 508 */     this.m_cancel = this.m_helper.addCommandButton(LocaleResources.getString("apLabelCancel", this.m_ctx), this);
/*     */ 
/* 510 */     this.m_helper.show();
/*     */   }
/*     */ 
/*     */   public void initCustomPanels(JPanel permissionPanel, Vector rgDataList)
/*     */   {
/* 515 */     if ((rgDataList == null) || (rgDataList.size() == 0))
/*     */     {
/* 517 */       return;
/*     */     }
/* 519 */     int numClasses = rgDataList.size();
/*     */ 
/* 521 */     Hashtable classSubClassMap = CustomSecurityRightsData.m_classSubClassMap;
/* 522 */     Hashtable subClassRightMap = CustomSecurityRightsData.m_subClassRightMap;
/* 523 */     Hashtable classRightMap = CustomSecurityRightsData.m_classRightMap;
/*     */ 
/* 525 */     TabPanel tabPanel = null;
/*     */ 
/* 527 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 529 */       RoleGroupData rgData = (RoleGroupData)rgDataList.elementAt(i);
/*     */ 
/* 531 */       String className = CustomSecurityRightsData.getClassFromGroup(rgData.m_groupName);
/* 532 */       this.m_customClassList.addElement(className);
/*     */ 
/* 534 */       if (numClasses == 1)
/*     */       {
/* 536 */         Vector subClassList = (Vector)classSubClassMap.get(className);
/* 537 */         if ((subClassList == null) || (subClassList.size() <= 1))
/*     */         {
/* 540 */           String title = CustomSecurityRightsData.getWindowLabel(this.m_ctx, className);
/* 541 */           JPanel customPanel = initCustomPanePanel(permissionPanel, title);
/*     */ 
/* 543 */           Vector rightList = (Vector)classRightMap.get(className);
/* 544 */           initCustomPermissionBoxes(customPanel, rightList, rgData.m_customPrivilege, true);
/*     */         }
/*     */         else
/*     */         {
/* 548 */           tabPanel = initCustomTabPanel(permissionPanel);
/* 549 */           int numSubClasses = subClassList.size();
/* 550 */           for (int j = 0; j < numSubClasses; ++j)
/*     */           {
/* 552 */             String subClassName = (String)subClassList.elementAt(j);
/* 553 */             String title = CustomSecurityRightsData.getSubClassLabel(this.m_ctx, className, subClassName);
/* 554 */             JPanel customPanel = initCustomPanePanel(tabPanel, title);
/*     */ 
/* 556 */             String fullSubClassName = className + "." + subClassName;
/* 557 */             Vector rightList = (Vector)subClassRightMap.get(fullSubClassName);
/* 558 */             initCustomPermissionBoxes(customPanel, rightList, rgData.m_customPrivilege, false);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 564 */         if (i == 0)
/*     */         {
/* 566 */           tabPanel = initCustomTabPanel(permissionPanel);
/*     */         }
/*     */ 
/* 569 */         String title = CustomSecurityRightsData.capitalizeClass(className);
/* 570 */         JPanel customPanel = initCustomPanePanel(tabPanel, title);
/*     */ 
/* 572 */         Vector rightList = (Vector)classRightMap.get(className);
/* 573 */         initCustomPermissionBoxes(customPanel, rightList, rgData.m_customPrivilege, true);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public TabPanel initCustomTabPanel(JPanel parentPanel)
/*     */   {
/* 580 */     TabPanel tabPanel = new TabPanel();
/* 581 */     tabPanel.setFullWidthTab(true);
/* 582 */     parentPanel.add(tabPanel);
/*     */ 
/* 584 */     return tabPanel;
/*     */   }
/*     */ 
/*     */   public JPanel initCustomPanePanel(JPanel parentPanel, String title)
/*     */   {
/* 589 */     JPanel customPanel = new PanePanel();
/* 590 */     customPanel.setLayout(new GridLayout(0, 2, 0, 0));
/*     */ 
/* 592 */     if (parentPanel instanceof TabPanel)
/*     */     {
/* 594 */       TabPanel tabPanel = (TabPanel)parentPanel;
/* 595 */       tabPanel.addPane(title, customPanel);
/*     */     }
/*     */     else
/*     */     {
/* 599 */       parentPanel.add(customPanel);
/*     */     }
/*     */ 
/* 602 */     return customPanel;
/*     */   }
/*     */ 
/*     */   public void initCustomPermissionBoxes(JPanel panel, Vector rightList, long privilege, boolean isClassPanel)
/*     */   {
/* 608 */     if (rightList == null)
/*     */     {
/* 610 */       return;
/*     */     }
/*     */ 
/* 613 */     Hashtable rightMap = CustomSecurityRightsData.m_rightMap;
/*     */ 
/* 615 */     int numRights = rightList.size();
/* 616 */     for (int i = 0; i < numRights; ++i)
/*     */     {
/* 618 */       String fullRight = (String)rightList.elementAt(i);
/* 619 */       Properties props = (Properties)rightMap.get(fullRight);
/* 620 */       boolean isHidden = StringUtils.convertToBool((String)props.get("IsHidden"), false);
/* 621 */       if (isHidden) {
/*     */         continue;
/*     */       }
/*     */ 
/* 625 */       String className = CustomSecurityRightsData.getLowerCaseProperty(props, "className");
/* 626 */       String rightLabel = props.getProperty("label");
/* 627 */       long rightPrivilege = NumberUtils.parseLong(props.getProperty("privilege"), 0L);
/*     */ 
/* 630 */       String btnLabel = null;
/* 631 */       if (isClassPanel)
/*     */       {
/* 633 */         btnLabel = CustomSecurityRightsData.getSubClassRightLabel(this.m_ctx, fullRight);
/*     */       }
/*     */       else
/*     */       {
/* 637 */         btnLabel = LocaleResources.getString(rightLabel, this.m_ctx);
/*     */       }
/*     */ 
/* 640 */       PermissionBox permBox = new PermissionBox(btnLabel, fullRight, "0", null, null);
/* 641 */       permBox.m_privilege = rightPrivilege;
/* 642 */       permBox.addItemListener(this);
/*     */ 
/* 644 */       if ((privilege & rightPrivilege) == rightPrivilege)
/*     */       {
/* 646 */         permBox.setSelected(true);
/*     */       }
/* 648 */       panel.add(permBox);
/*     */ 
/* 650 */       this.m_customPermBoxMap.put(fullRight, permBox);
/*     */ 
/* 652 */       Vector permBoxList = (Vector)this.m_customClassPermBoxMap.get(className);
/* 653 */       if (permBoxList == null)
/*     */       {
/* 655 */         permBoxList = new IdcVector();
/* 656 */         this.m_customClassPermBoxMap.put(className, permBoxList);
/*     */       }
/* 658 */       permBoxList.addElement(permBox);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void selectCustomPermissionBoxes(PermissionBox checkBox, boolean isSelected)
/*     */   {
/* 664 */     String fullRight = checkBox.m_idStr;
/* 665 */     Properties props = (Properties)CustomSecurityRightsData.m_rightMap.get(fullRight);
/* 666 */     if (props == null)
/*     */     {
/* 668 */       return;
/*     */     }
/* 670 */     String className = CustomSecurityRightsData.getLowerCaseProperty(props, "className");
/*     */ 
/* 672 */     String selectKeyString = (isSelected) ? "selectRightsByStrings" : "unselectRightsByStrings";
/* 673 */     List selectStrings = (List)props.get(selectKeyString);
/* 674 */     String selectKeyBits = (isSelected) ? "selectRightsByBits" : "unselectRightsByBits";
/* 675 */     String strBits = props.getProperty(selectKeyBits);
/*     */     long selectPrivilege;
/*     */     long selectPrivilege;
/* 677 */     if ((strBits == null) || (strBits.length() == 0))
/*     */     {
/* 679 */       selectPrivilege = 0L;
/*     */     }
/*     */     else
/*     */     {
/* 683 */       selectPrivilege = NumberUtils.parseHexStringAsLong(props.getProperty(selectKeyBits));
/*     */     }
/*     */ 
/* 687 */     Enumeration en = this.m_customPermBoxMap.keys();
/* 688 */     while (en.hasMoreElements())
/*     */     {
/* 690 */       String curRight = (String)en.nextElement();
/* 691 */       if ((!curRight.equals(fullRight)) && (curRight.startsWith(className)))
/*     */       {
/* 693 */         PermissionBox permBox = (PermissionBox)this.m_customPermBoxMap.get(curRight);
/*     */         boolean bEval;
/*     */         boolean bEval;
/* 695 */         if (selectPrivilege != 0L)
/*     */         {
/* 697 */           bEval = (selectPrivilege & permBox.m_privilege) == permBox.m_privilege;
/*     */         }
/*     */         else
/*     */         {
/* 703 */           bEval = false;
/*     */         }
/* 705 */         if ((bEval) || (selectStrings.contains(curRight)))
/*     */         {
/* 707 */           permBox.setSelected(isSelected);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void saveCustomPermissions()
/*     */   {
/* 715 */     int numClasses = this.m_customClassList.size();
/* 716 */     for (int i = 0; i < numClasses; ++i)
/*     */     {
/* 718 */       RoleGroupData rgData = (RoleGroupData)this.m_rgDataList.elementAt(i);
/* 719 */       rgData.m_customPrivilege = 0L;
/*     */ 
/* 721 */       String className = (String)this.m_customClassList.elementAt(i);
/* 722 */       Vector permBoxList = (Vector)this.m_customClassPermBoxMap.get(className);
/* 723 */       if (permBoxList == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 728 */       int numBoxes = permBoxList.size();
/* 729 */       for (int j = 0; j < numBoxes; ++j)
/*     */       {
/* 731 */         PermissionBox permBox = (PermissionBox)permBoxList.elementAt(j);
/* 732 */         if (!permBox.isSelected())
/*     */           continue;
/* 734 */         rgData.m_customPrivilege |= permBox.m_privilege;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addPermissionsPanel(JPanel permissionsPanel)
/*     */   {
/* 749 */     this.m_helper.m_mainPanel.add("South", permissionsPanel);
/*     */   }
/*     */ 
/*     */   protected long getPrivilege()
/*     */   {
/* 758 */     long privilege = 0L;
/* 759 */     int length = this.m_permBoxes.length;
/* 760 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 762 */       if (!this.m_permBoxes[i].isSelected())
/*     */         continue;
/* 764 */       privilege |= this.m_permBoxes[i].m_privilege;
/*     */     }
/*     */ 
/* 767 */     return privilege;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 772 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92578 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditPermissionsDlg
 * JD-Core Version:    0.5.4
 */