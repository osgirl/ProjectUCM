/*     */ package intradoc.apps.useradmin;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.BasePanel;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.apps.useradmin.irmintg.IRMConfigDlg;
/*     */ import intradoc.apps.useradmin.irmintg.IRMEditKnownAccountsDlg;
/*     */ import intradoc.apps.useradmin.irmintg.IRMGroupPermissionsDlg;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class UserAdminFrame extends MainFrame
/*     */ {
/*     */   protected ExecutionContext m_ctx;
/*     */   protected String[][] PANEL_INFOS;
/*     */   protected TabPanel m_tabPanel;
/*     */   protected BasePanel[] m_userPanels;
/*     */ 
/*     */   public UserAdminFrame()
/*     */   {
/*  56 */     this.PANEL_INFOS = new String[][] { { "UserAdminPanel", "intradoc.apps.useradmin.UserAdminPanel", "apLabelUsers" }, { "AliasPanel", "intradoc.apps.useradmin.AliasPanel", "apLabelAliases" }, { "UserInfoPanel", "intradoc.apps.useradmin.UserInfoPanel", "apLabelUserInfo" } };
/*     */ 
/*  62 */     this.m_tabPanel = null;
/*  63 */     this.m_userPanels = null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean closeOnExit) throws ServiceException
/*     */   {
/*  69 */     IdcMessage msg = null;
/*  70 */     if (title != null)
/*     */     {
/*  72 */       msg = IdcMessageFactory.lc();
/*  73 */       msg.m_msgEncoded = title;
/*     */     }
/*  75 */     init(msg, closeOnExit);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean closeOnExit)
/*     */     throws ServiceException
/*     */   {
/*  81 */     super.init(title, closeOnExit);
/*     */ 
/*  84 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*  85 */     this.m_ctx = this.m_appHelper.getExecutionContext();
/*     */ 
/*  88 */     DataBinder binder = new DataBinder(true);
/*  89 */     AppLauncher.executeService("GET_USERS", binder);
/*     */ 
/*  92 */     boolean isAdmin = AppLauncher.isAdmin();
/*  93 */     buildMenu(isAdmin);
/*  94 */     JPanel pnl = initPanels(isAdmin);
/*     */ 
/*  96 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/*  97 */     mainPanel.setLayout(new BorderLayout());
/*  98 */     mainPanel.add("Center", pnl);
/*     */ 
/* 100 */     PromptHandler allowCloseCallback = new PromptHandler()
/*     */     {
/*     */       public int prompt()
/*     */       {
/* 104 */         if (UserAdminFrame.this.m_userPanels != null)
/*     */         {
/* 106 */           for (int i = 0; i < UserAdminFrame.this.PANEL_INFOS.length; ++i)
/*     */           {
/* 108 */             if (UserAdminFrame.this.m_userPanels[i].canExit())
/*     */               continue;
/* 110 */             UserAdminFrame.this.m_tabPanel.selectPane(LocaleResources.getString(UserAdminFrame.this.PANEL_INFOS[i][2], UserAdminFrame.this.m_ctx));
/* 111 */             return 0;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 116 */         return 1;
/*     */       }
/*     */     };
/* 119 */     this.m_appHelper.m_isCloseAllowedCallback = allowCloseCallback;
/*     */ 
/* 121 */     pack();
/* 122 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected JPanel initPanels(boolean isAdmin) throws ServiceException
/*     */   {
/* 127 */     if (isAdmin)
/*     */     {
/* 129 */       this.m_tabPanel = new TabPanel();
/* 130 */       int numPanels = this.PANEL_INFOS.length;
/* 131 */       this.m_userPanels = new BasePanel[numPanels];
/*     */ 
/* 133 */       for (int i = 0; i < numPanels; ++i)
/*     */       {
/* 135 */         this.m_userPanels[i] = ((BasePanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_ctx, this.PANEL_INFOS[i][0])));
/*     */ 
/* 139 */         this.m_userPanels[i].init(this.m_appHelper);
/* 140 */         this.m_tabPanel.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_ctx), this.m_userPanels[i]);
/*     */       }
/*     */ 
/* 143 */       return this.m_tabPanel;
/*     */     }
/*     */ 
/* 146 */     UserAdminPanel pnl = new UserAdminPanel();
/* 147 */     pnl.init(this.m_appHelper);
/* 148 */     return pnl;
/*     */   }
/*     */ 
/*     */   protected void buildMenu(boolean isAdmin)
/*     */   {
/* 153 */     JMenuBar mb = new JMenuBar();
/* 154 */     setJMenuBar(mb);
/*     */ 
/* 156 */     JMenu userMenu = new JMenu(LocaleResources.getString("apOptionsMenuLabel", this.m_ctx));
/*     */ 
/* 159 */     if (IRMUtils.isIRMIntgEnabled())
/*     */     {
/* 161 */       JMenuItem miIRMConfig = new JMenuItem(LocaleResources.getString("apLabelIRMConfig", this.m_cxt));
/*     */ 
/* 163 */       ActionListener irmConfigListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 168 */           IRMConfigDlg irmConfigDlg = new IRMConfigDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelIRMConfig", UserAdminFrame.this.m_cxt));
/*     */ 
/* 171 */           irmConfigDlg.init();
/*     */         }
/*     */       };
/* 174 */       miIRMConfig.addActionListener(irmConfigListener);
/* 175 */       userMenu.add(miIRMConfig, 0);
/*     */     }
/* 177 */     mb.add(userMenu);
/* 178 */     addStandardOptions(userMenu);
/*     */ 
/* 181 */     if (isAdmin)
/*     */     {
/* 183 */       JMenu toolsMenu = new JMenu(LocaleResources.getString("apLabelSecurity", this.m_ctx));
/* 184 */       mb.add(toolsMenu);
/*     */ 
/* 186 */       JMenuItem miGroupRights = new JMenuItem(LocaleResources.getString("apDlgButtonPermissionsByGroup", this.m_ctx));
/*     */ 
/* 188 */       ActionListener groupListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/*     */           GroupPermissionsDlg groupDlg;
/*     */           GroupPermissionsDlg groupDlg;
/* 195 */           if (IRMUtils.isIRMIntgEnabled())
/*     */           {
/* 197 */             groupDlg = new IRMGroupPermissionsDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelPermissionsByGroup", UserAdminFrame.this.m_ctx));
/*     */           }
/*     */           else
/*     */           {
/* 203 */             groupDlg = new GroupPermissionsDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelPermissionsByGroup", UserAdminFrame.this.m_ctx));
/*     */           }
/*     */ 
/* 207 */           groupDlg.init();
/*     */         }
/*     */       };
/* 210 */       miGroupRights.addActionListener(groupListener);
/* 211 */       toolsMenu.add(miGroupRights);
/*     */ 
/* 213 */       JMenuItem miRoleRights = new JMenuItem(LocaleResources.getString("apDlgButtonPermissionsByRole", this.m_ctx));
/*     */ 
/* 215 */       ActionListener roleListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 219 */           RolePermissionsDlg roleDlg = new RolePermissionsDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelPermissionsByRole", UserAdminFrame.this.m_ctx));
/*     */ 
/* 221 */           roleDlg.init();
/*     */         }
/*     */       };
/* 224 */       miRoleRights.addActionListener(roleListener);
/* 225 */       toolsMenu.add(miRoleRights);
/*     */ 
/* 227 */       if (SharedObjects.getEnvValueAsBoolean("UseAccounts", false))
/*     */       {
/* 229 */         JMenuItem miKnownAccounts = new JMenuItem(LocaleResources.getString("apDlgButtonPredefinedAccounts", this.m_ctx));
/*     */ 
/* 231 */         ActionListener accountsListener = new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e)
/*     */           {
/*     */             EditKnownAccountsDlg accountsDlg;
/*     */             EditKnownAccountsDlg accountsDlg;
/* 238 */             if (IRMUtils.isIRMIntgEnabled())
/*     */             {
/* 240 */               accountsDlg = new IRMEditKnownAccountsDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelPredefinedAccounts", UserAdminFrame.this.m_ctx), "PredefinedAccounts");
/*     */             }
/*     */             else
/*     */             {
/* 248 */               accountsDlg = new EditKnownAccountsDlg(UserAdminFrame.this.m_appHelper, LocaleResources.getString("apLabelPredefinedAccounts", UserAdminFrame.this.m_ctx), "PredefinedAccounts");
/*     */             }
/*     */ 
/* 254 */             accountsDlg.init();
/*     */           }
/*     */         };
/* 257 */         miKnownAccounts.addActionListener(accountsListener);
/* 258 */         toolsMenu.add(miKnownAccounts);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 263 */     addAppMenu(mb);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 268 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92578 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.UserAdminFrame
 * JD-Core Version:    0.5.4
 */