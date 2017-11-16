/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserAttribInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.gui.SecurityEditHelper;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class EditUserAccountPanel extends EditUserBasePanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected UdlPanel m_accountsList;
/*     */   protected boolean m_useCollaboration;
/*     */ 
/*     */   protected void initUI(boolean isEdit)
/*     */   {
/*  61 */     this.m_useCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/*     */ 
/*  64 */     this.m_helper.makePanelGridBag(this, 1);
/*  65 */     GridBagConstraints gc = this.m_helper.m_gridHelper.m_gc;
/*     */ 
/*  67 */     String accountDesc = LocaleResources.getString("apEditUserAccountPanelDesc", this.m_ctx);
/*  68 */     if (this.m_useCollaboration)
/*     */     {
/*  70 */       accountDesc = accountDesc + "  " + LocaleResources.getString("apEditUserAccountPanelProjectDesc", this.m_ctx);
/*     */     }
/*     */ 
/*  73 */     this.m_helper.addLastComponentInRow(this, new CustomText(accountDesc, 80));
/*  74 */     gc.weighty = 1.0D;
/*  75 */     this.m_helper.addComponent(this, new CustomLabel(LocaleResources.getString("apLabelAccounts", this.m_ctx), 1));
/*     */ 
/*  78 */     this.m_accountsList = new UdlPanel(null, null, 225, 5, "DocAccountsList", false);
/*  79 */     this.m_accountsList.setVisibleColumns("account,privilege");
/*  80 */     this.m_accountsList.init();
/*  81 */     this.m_accountsList.useDefaultListener();
/*  82 */     this.m_accountsList.m_list.addActionListener(this);
/*     */ 
/*  84 */     this.m_helper.addComponent(this, this.m_accountsList);
/*     */ 
/*  86 */     JPanel accountsButtonsPanel = new PanePanel();
/*  87 */     accountsButtonsPanel.setLayout(new GridLayout(0, 1, 10, 5));
/*     */ 
/*  91 */     this.m_helper.addComponent(accountsButtonsPanel, new PanePanel());
/*  92 */     this.m_helper.addCommandButton(accountsButtonsPanel, LocaleResources.getString("apDlgButtonAdd", this.m_ctx), "add", this);
/*     */ 
/*  94 */     JButton editBtn = this.m_helper.addCommandButton(accountsButtonsPanel, LocaleResources.getString("apDlgButtonEdit", this.m_ctx), "edit", this);
/*     */ 
/*  96 */     JButton delBtn = this.m_helper.addCommandButton(accountsButtonsPanel, LocaleResources.getString("apLabelDelete", this.m_ctx), "delete", this);
/*     */ 
/* 100 */     this.m_accountsList.addControlComponent(editBtn);
/* 101 */     this.m_accountsList.addControlComponent(delBtn);
/*     */ 
/* 104 */     gc.fill = 0;
/* 105 */     gc.weightx = 0.0D;
/* 106 */     gc.anchor = 11;
/* 107 */     this.m_helper.addLastComponentInRow(this, accountsButtonsPanel);
/*     */ 
/* 109 */     gc.weighty = 0.0D;
/* 110 */     gc.weightx = 1.0D;
/* 111 */     gc.fill = 2;
/*     */ 
/* 114 */     if (!this.m_useCollaboration)
/*     */     {
/* 116 */       this.m_editHelper.addAccountEditField(this, LocaleResources.getString("apLabelDefaultAccount", this.m_ctx), "defaultAccount");
/*     */ 
/* 118 */       if (isEdit)
/*     */       {
/* 120 */         this.m_editHelper.setAccountEditValue("defaultAccount", this.m_editHelper.m_userData.m_defaultAccount);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 125 */     refreshAccountDisplay();
/*     */   }
/*     */ 
/*     */   protected void refreshAccountDisplay()
/*     */   {
/* 130 */     if (this.m_accountsList == null)
/*     */     {
/* 132 */       return;
/*     */     }
/*     */ 
/* 135 */     DataResultSet drset = new DataResultSet(new String[] { "account", "privilege" });
/*     */ 
/* 138 */     String selectedAccount = this.m_accountsList.getSelectedObj();
/*     */ 
/* 140 */     Vector v = this.m_editHelper.m_userData.getAttributes("account");
/* 141 */     if (v == null)
/*     */     {
/* 143 */       v = new IdcVector();
/* 144 */       SecurityUtils.addDefaultAccounts(this.m_editHelper.m_userData, v);
/* 145 */       this.m_editHelper.m_userData.putAttributes("account", v);
/*     */     }
/*     */ 
/* 148 */     int naccounts = v.size();
/* 149 */     for (int i = 0; i < naccounts; ++i)
/*     */     {
/* 151 */       Vector row = drset.createEmptyRow();
/* 152 */       UserAttribInfo uai = (UserAttribInfo)v.elementAt(i);
/* 153 */       String accountDisplayStr = this.m_editHelper.m_userData.getAccountPresentationString(uai.m_attribName, this.m_ctx);
/* 154 */       row.setElementAt(accountDisplayStr, 0);
/*     */ 
/* 156 */       String pStr = SecurityAccessListUtils.makePrivilegeStr(uai.m_attribPrivilege);
/* 157 */       row.setElementAt(pStr, 1);
/*     */ 
/* 159 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 163 */     this.m_accountsList.refreshList(drset, selectedAccount);
/*     */ 
/* 166 */     if (this.m_useCollaboration)
/*     */       return;
/* 168 */     this.m_editHelper.refreshAccountChoiceList(false, 2);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 177 */     handleAccountAction(e);
/*     */ 
/* 180 */     validate();
/*     */   }
/*     */ 
/*     */   protected void handleAccountAction(ActionEvent evt)
/*     */   {
/* 186 */     String command = evt.getActionCommand();
/* 187 */     Object src = evt.getSource();
/* 188 */     int selIndex = -1;
/* 189 */     boolean isListAction = this.m_accountsList.m_list == src;
/* 190 */     boolean isAdd = (!isListAction) && (command.equals("add"));
/* 191 */     if (!isAdd)
/*     */     {
/* 193 */       selIndex = this.m_accountsList.getSelectedIndex();
/* 194 */       if (selIndex < 0)
/*     */       {
/* 196 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 200 */     Vector v = this.m_editHelper.m_userData.getAttributes("account");
/* 201 */     if ((v == null) && (selIndex >= 0))
/*     */     {
/* 203 */       return;
/*     */     }
/*     */ 
/* 207 */     boolean selfDeniedActions = (!this.m_isAdmin) && (this.m_isEditSelf == true);
/*     */ 
/* 210 */     if (command.equals("delete"))
/*     */     {
/* 212 */       if (selfDeniedActions)
/*     */       {
/* 214 */         reportError(IdcMessageFactory.lc("apCannotAlterPrivilegeToOwnAccount", new Object[0]));
/* 215 */         return;
/*     */       }
/* 217 */       if ((v.size() == 1) && (!this.m_useCollaboration))
/*     */       {
/* 219 */         reportError(IdcMessageFactory.lc("apOneAccountMustBeDefined", new Object[0]));
/* 220 */         return;
/*     */       }
/*     */ 
/* 224 */       if (this.m_useCollaboration)
/*     */       {
/* 226 */         UserAttribInfo uai = (UserAttribInfo)v.elementAt(selIndex);
/* 227 */         if (uai.m_attribName.equals("#none"))
/*     */         {
/* 229 */           reportError(IdcMessageFactory.lc("apCannotDeleteAccount", new Object[0]));
/* 230 */           return;
/*     */         }
/*     */       }
/*     */ 
/* 234 */       v.removeElementAt(selIndex);
/*     */     }
/*     */     else
/*     */     {
/* 239 */       String account = null;
/* 240 */       UserAttribInfo uai = null;
/* 241 */       int priv = 15;
/* 242 */       if (!this.m_isAdmin)
/*     */       {
/*     */         try
/*     */         {
/* 246 */           priv = SecurityUtils.determineBestAccountPrivilege(this.m_loggedInUserData, null);
/*     */         }
/*     */         catch (ServiceException ignore)
/*     */         {
/* 250 */           if (SystemUtils.m_verbose)
/*     */           {
/* 252 */             Report.debug("system", null, ignore);
/*     */           }
/*     */         }
/*     */       }
/* 256 */       if (selIndex >= 0)
/*     */       {
/* 258 */         if (selfDeniedActions)
/*     */         {
/* 260 */           reportError(IdcMessageFactory.lc("apCannotAlterPrivilegeToOwnAccount", new Object[0]));
/* 261 */           return;
/*     */         }
/* 263 */         uai = (UserAttribInfo)v.elementAt(selIndex);
/* 264 */         priv = uai.m_attribPrivilege;
/* 265 */         account = uai.m_attribName;
/*     */ 
/* 268 */         if ((this.m_useCollaboration) && 
/* 270 */           (account.equals("#none")))
/*     */         {
/* 272 */           reportError(IdcMessageFactory.lc("apCannotAlterAccount", new Object[0]));
/* 273 */           return;
/*     */         }
/*     */ 
/*     */       }
/* 279 */       else if (selfDeniedActions)
/*     */       {
/* 281 */         reportError(IdcMessageFactory.lc("apCannotAddAccountsToOwnUserProfile", new Object[0]));
/* 282 */         return;
/*     */       }
/*     */       String title;
/*     */       String title;
/* 288 */       if (isAdd)
/*     */       {
/* 290 */         title = LocaleResources.getString("apTitleAddNewAccount", this.m_ctx);
/*     */       }
/*     */       else
/*     */       {
/* 294 */         String displayAccount = this.m_editHelper.m_userData.getAccountPresentationString(account, this.m_ctx);
/* 295 */         title = LocaleResources.getString("apTitleEditPermissionsForAccount", this.m_ctx, displayAccount);
/*     */       }
/* 297 */       EditPermissionsDlg dlg = new EditPermissionsDlg(this.m_systemInterface, title);
/* 298 */       PermissionsData permData = new PermissionsData();
/* 299 */       permData.setPrivilege(priv);
/* 300 */       if (!dlg.promptAccount(permData, this.m_helper.m_props, this.m_loggedInUserData, this.m_editHelper.m_userData, account, isAdd))
/*     */       {
/* 303 */         return;
/*     */       }
/*     */ 
/* 306 */       if (uai != null)
/*     */       {
/* 308 */         uai.m_attribPrivilege = permData.m_privilege;
/*     */       }
/*     */       else
/*     */       {
/* 312 */         account = this.m_helper.m_props.getProperty("dDocAccount");
/* 313 */         String privNumStr = Integer.toString(permData.m_privilege);
/* 314 */         this.m_editHelper.m_userData.addAttribute("account", account, privNumStr);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 319 */     refreshAccountDisplay();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 324 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserAccountPanel
 * JD-Core Version:    0.5.4
 */