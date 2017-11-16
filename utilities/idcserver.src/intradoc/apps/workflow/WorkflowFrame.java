/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Observable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WorkflowFrame extends MainFrame
/*     */   implements WorkflowContext, SharedContext
/*     */ {
/*  63 */   protected boolean m_isDirty = true;
/*  64 */   protected Vector m_wfPanels = new IdcVector();
/*     */ 
/*  67 */   protected final String[][] PANEL_INFOS = { { "WorkflowPanel", "intradoc.apps.workflow.WorkflowPanel", "apTitleWorkflows", "1" }, { "CriteriaPanel", "intradoc.apps.workflow.CriteriaPanel", "apTitleCriteria", "1" }, { "TemplatesPanel", "intradoc.apps.workflow.TemplatesPanel", "apTitleTemplates", "0" } };
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean closeOnExit)
/*     */     throws ServiceException
/*     */   {
/*  85 */     IdcMessage msg = null;
/*  86 */     if (title != null)
/*     */     {
/*  88 */       msg = IdcMessageFactory.lc();
/*  89 */       msg.m_msgEncoded = title;
/*     */     }
/*  91 */     init(msg, closeOnExit);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean closeOnExit)
/*     */     throws ServiceException
/*     */   {
/*  97 */     super.init(title, closeOnExit);
/*  98 */     this.m_cxt = this.m_appHelper.getExecutionContext();
/*     */ 
/* 101 */     AppLauncher.addSubjectObserver("workflows", this);
/*     */ 
/* 103 */     this.m_appHelper.attachToAppFrame(this, null, null, title);
/*     */ 
/* 107 */     refreshWorkflows();
/*     */ 
/* 109 */     boolean isAdmin = AppLauncher.isAdmin();
/*     */ 
/* 111 */     buildMenu(isAdmin);
/* 112 */     JPanel pnl = initTabPanels(isAdmin);
/*     */ 
/* 114 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 115 */     mainPanel.setLayout(new BorderLayout());
/* 116 */     mainPanel.add("Center", pnl);
/*     */ 
/* 118 */     pack();
/* 119 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected JPanel initTabPanels(boolean isAdmin) throws ServiceException
/*     */   {
/* 124 */     TabPanel tab = new TabPanel();
/*     */ 
/* 126 */     int numPanels = this.PANEL_INFOS.length;
/* 127 */     WfBasePanel[] tabPanels = new WfBasePanel[numPanels];
/* 128 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 130 */       boolean isWfPanel = StringUtils.convertToBool(this.PANEL_INFOS[i][3], false);
/* 131 */       if ((!isWfPanel) && (!isAdmin))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 136 */       tabPanels[i] = ((WfBasePanel)ComponentClassFactory.createClassInstance(this.PANEL_INFOS[i][0], this.PANEL_INFOS[i][1], LocaleResources.getString("apUnableToLoadTabPanel", this.m_cxt, this.PANEL_INFOS[i][0])));
/*     */ 
/* 139 */       tabPanels[i].init(this.m_appHelper, this);
/* 140 */       tab.addPane(LocaleResources.getString(this.PANEL_INFOS[i][2], this.m_cxt), tabPanels[i]);
/*     */ 
/* 142 */       if (!isWfPanel)
/*     */         continue;
/* 144 */       this.m_wfPanels.addElement(tabPanels[i]);
/*     */     }
/*     */ 
/* 147 */     return tab;
/*     */   }
/*     */ 
/*     */   protected void buildMenu(boolean isAdmin)
/*     */   {
/* 152 */     JMenuBar mb = new JMenuBar();
/* 153 */     setJMenuBar(mb);
/*     */ 
/* 155 */     JMenu wfMenu = new JMenu(LocaleResources.getString("apTitleOptions", this.m_cxt));
/* 156 */     mb.add(wfMenu);
/*     */ 
/* 158 */     if (isAdmin)
/*     */     {
/* 160 */       JMenuItem miScript = new JMenuItem(LocaleResources.getString("apDlgButtonScriptTemplates", this.m_cxt));
/* 161 */       ActionListener sListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 165 */           ScriptDlg dlg = new ScriptDlg(WorkflowFrame.this.m_appHelper, LocaleResources.getString("apTitleWorkflowScripts", WorkflowFrame.this.m_cxt));
/*     */ 
/* 167 */           dlg.prompt(null, WorkflowFrame.this);
/*     */         }
/*     */       };
/* 170 */       miScript.addActionListener(sListener);
/* 171 */       wfMenu.add(miScript);
/*     */ 
/* 173 */       JMenuItem miToken = new JMenuItem(LocaleResources.getString("apDlgButtonTokens", this.m_cxt));
/* 174 */       ActionListener tListener = new ActionListener()
/*     */       {
/*     */         public void actionPerformed(ActionEvent e)
/*     */         {
/* 178 */           TokenDlg dlg = new TokenDlg(WorkflowFrame.this.m_appHelper, LocaleResources.getString("apTitleWorkflowTokens", WorkflowFrame.this.m_cxt));
/*     */ 
/* 180 */           dlg.prompt(null, WorkflowFrame.this);
/*     */         }
/*     */       };
/* 183 */       miToken.addActionListener(tListener);
/* 184 */       wfMenu.add(miToken);
/*     */     }
/*     */ 
/* 187 */     wfMenu.addSeparator();
/*     */ 
/* 189 */     addStandardOptions(wfMenu);
/*     */ 
/* 191 */     addAppMenu(mb);
/*     */   }
/*     */ 
/*     */   public void setIsDirty(boolean isDirty)
/*     */   {
/* 199 */     this.m_isDirty = isDirty;
/*     */   }
/*     */ 
/*     */   public void refreshWorkflows() throws ServiceException
/*     */   {
/* 204 */     if (!this.m_isDirty)
/*     */       return;
/* 206 */     DataBinder binder = new DataBinder();
/* 207 */     executeService("GET_WORKFLOWS", binder, false);
/* 208 */     this.m_isDirty = false;
/*     */   }
/*     */ 
/*     */   public SharedContext getSharedContext()
/*     */   {
/* 214 */     return this;
/*     */   }
/*     */ 
/*     */   public Vector getUsersGroups()
/*     */   {
/* 219 */     Vector securityGroups = null;
/* 220 */     UserData user = AppLauncher.getUserData();
/* 221 */     if (SecurityUtils.isUserOfRole(user, "admin"))
/*     */     {
/* 223 */       securityGroups = SharedObjects.getOptList("securityGroups");
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 229 */         securityGroups = SecurityUtils.getUserGroupsWithPrivilege(user, 8);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 233 */         IdcMessage msg = IdcMessageFactory.lc("apCannotCalculateAdminPrivilegesForUser", new Object[] { user.m_name });
/* 234 */         reportError(e, msg);
/* 235 */         return securityGroups;
/*     */       }
/*     */     }
/*     */ 
/* 239 */     if (securityGroups == null)
/*     */     {
/* 241 */       IdcMessage msg = IdcMessageFactory.lc("apCannotCalculateAdminPrivilegesForUser", new Object[] { user.m_name });
/* 242 */       reportError(null, msg);
/*     */     }
/* 244 */     return securityGroups;
/*     */   }
/*     */ 
/*     */   public String[][] buildProjectMap()
/*     */   {
/* 249 */     DataResultSet drset = SharedObjects.getTable("RegisteredProjects");
/* 250 */     if ((drset == null) || (!drset.isRowPresent()))
/*     */     {
/* 252 */       return (String[][])null;
/*     */     }
/*     */ 
/* 255 */     DataResultSet wfProjects = new DataResultSet();
/* 256 */     ResultSetFilter filter = new ResultSetFilter()
/*     */     {
/*     */       public int checkRow(String val, int curNumRows, Vector row)
/*     */       {
/* 260 */         Vector functions = StringUtils.parseArray(val, ',', '^');
/* 261 */         int num = functions.size();
/* 262 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 264 */           String function = (String)functions.elementAt(i);
/* 265 */           if (function.equals("stagingworkflow"))
/*     */           {
/* 267 */             return 1;
/*     */           }
/*     */         }
/* 270 */         return 0;
/*     */       }
/*     */     };
/* 274 */     wfProjects.copyFiltered(drset, "dPrjFunctions", filter);
/*     */ 
/* 276 */     String[][] map = (String[][])null;
/*     */     try
/*     */     {
/* 279 */       map = ResultSetUtils.createStringTable(wfProjects, new String[] { "dProjectID", "dPrjDescription" });
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 283 */       String error = LocaleUtils.encodeMessage("apUnableToBuildProjectMap", e.getMessage());
/* 284 */       Report.trace(null, LocaleResources.getString(error, this.m_cxt), e);
/*     */     }
/*     */ 
/* 287 */     if (map.length == 0)
/*     */     {
/* 290 */       return (String[][])null;
/*     */     }
/* 292 */     return map;
/*     */   }
/*     */ 
/*     */   public DisplayStringCallbackAdaptor createStringCallback()
/*     */   {
/* 297 */     String[][] aliasTypes = { { "user", LocaleResources.getString("apWfUser", this.m_cxt) }, { "alias", LocaleResources.getString("apWfAlias", this.m_cxt) }, { "token", LocaleResources.getString("apWfToken", this.m_cxt) } };
/*     */ 
/* 305 */     DisplayStringCallbackAdaptor dscAdaptor = new DisplayStringCallbackAdaptor(aliasTypes)
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 310 */         if (name.equals("dWfComputed"))
/*     */         {
/* 312 */           if (value.equalsIgnoreCase("inactive"))
/*     */           {
/* 314 */             value = LocaleResources.getString("apWfInactive", WorkflowFrame.this.m_cxt);
/*     */           }
/* 316 */           else if (value.equalsIgnoreCase("contribution"))
/*     */           {
/* 318 */             value = LocaleResources.getString("apWf" + value, WorkflowFrame.this.m_cxt);
/*     */           }
/*     */         } else {
/* 321 */           if (name.equals("dAliasType"))
/*     */           {
/* 323 */             return StringUtils.getPresentationString(this.val$aliasTypes, value);
/*     */           }
/* 325 */           if (name.equals("dWfStepType"))
/*     */           {
/* 327 */             value = WorkflowScriptUtils.getUpgradedStepType(value);
/* 328 */             return WorkflowScriptUtils.formatLocalizedStepTypeDescription(value, WorkflowFrame.this.m_cxt);
/*     */           }
/* 330 */           if ((name.equals("dWfStepName")) && 
/* 332 */             (value.equalsIgnoreCase("contribution")))
/*     */           {
/* 334 */             value = LocaleResources.getString("apWf" + value, WorkflowFrame.this.m_cxt);
/*     */           }
/*     */         }
/* 337 */         return value;
/*     */       }
/*     */     };
/* 340 */     return dscAdaptor;
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 348 */     executeService(action, binder, true);
/*     */   }
/*     */ 
/*     */   public void executeService(String action, DataBinder binder, boolean isRefresh)
/*     */     throws ServiceException
/*     */   {
/* 354 */     AppLauncher.executeService(action, binder, this.m_appHelper);
/* 355 */     if (!isRefresh)
/*     */       return;
/* 357 */     this.m_isDirty = isRefresh;
/* 358 */     update(null, null);
/*     */   }
/*     */ 
/*     */   public UserData getUserData()
/*     */   {
/* 364 */     return AppLauncher.getUserData();
/*     */   }
/*     */ 
/*     */   public void update(Observable obs, Object arg)
/*     */   {
/* 373 */     setIsDirty(true);
/*     */ 
/* 375 */     int size = this.m_wfPanels.size();
/* 376 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 378 */       WorkflowPanel pnl = (WorkflowPanel)this.m_wfPanels.elementAt(i);
/* 379 */       pnl.update();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void dispose()
/*     */   {
/* 386 */     AppLauncher.removeSubjectObserver("workflows", this);
/* 387 */     super.dispose();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 392 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92636 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WorkflowFrame
 * JD-Core Version:    0.5.4
 */