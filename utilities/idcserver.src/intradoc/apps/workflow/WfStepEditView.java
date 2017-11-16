/*     */ package intradoc.apps.workflow;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.gui.AddAliasDlg;
/*     */ import intradoc.shared.gui.ViewData;
/*     */ import intradoc.shared.gui.ViewDlg;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class WfStepEditView extends EditViewBase
/*     */ {
/*  56 */   protected WfStepData m_aliasSet = null;
/*  57 */   protected UdlPanel m_aliasList = null;
/*  58 */   protected JButton m_deleteBtn = null;
/*  59 */   protected boolean m_isTemplate = false;
/*     */ 
/*     */   public WfStepEditView(boolean isTemplate)
/*     */   {
/*  63 */     this.m_isTemplate = isTemplate;
/*     */   }
/*     */ 
/*     */   public WfStepEditView()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initUI()
/*     */   {
/*  74 */     this.m_aliasList = new UdlPanel(LocaleResources.getString("apTitleAliasUser", this.m_cxt), null, 150, 10, "WorkflowAliases", false);
/*     */ 
/*  76 */     this.m_aliasList.init();
/*  77 */     this.m_aliasList.setVisibleColumns("dAlias,dAliasType");
/*  78 */     this.m_aliasList.setIDColumn("dAlias");
/*     */ 
/*  80 */     DisplayStringCallback displayCallback = this.m_context.createStringCallback();
/*  81 */     this.m_aliasList.setDisplayCallback("dAliasType", displayCallback);
/*  82 */     this.m_aliasList.useDefaultListener();
/*     */ 
/*  85 */     JPanel aliasBtnPanel = new PanePanel();
/*  86 */     aliasBtnPanel.setLayout(new GridLayout(0, 1));
/*     */ 
/*  89 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*  93 */         String cmd = e.getActionCommand();
/*  94 */         if (cmd.equals("addAlias"))
/*     */         {
/*  96 */           WfStepEditView.this.addAliases();
/*     */         }
/*  98 */         else if (cmd.equals("addUser"))
/*     */         {
/* 100 */           WfStepEditView.this.addUsers();
/*     */         }
/* 102 */         else if (cmd.equals("addToken"))
/*     */         {
/* 104 */           WfStepEditView.this.addToken();
/*     */         } else {
/* 106 */           if (!cmd.equals("delete"))
/*     */             return;
/* 108 */           WfStepEditView.this.deleteAlias();
/*     */         }
/*     */       }
/*     */     };
/* 114 */     String[][] buttonInfo = { { LocaleResources.getString("apDlgButtonAddAlias", this.m_cxt), "addAlias", "0" }, { LocaleResources.getString("apDlgButtonAddUser", this.m_cxt), "addUser", "0" }, { LocaleResources.getString("apDlgButtonAddToken", this.m_cxt), "addToken", "0" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "1" } };
/*     */ 
/* 123 */     for (int i = 0; i < buttonInfo.length; ++i)
/*     */     {
/* 125 */       boolean isControlled = StringUtils.convertToBool(buttonInfo[i][2], false);
/* 126 */       JButton btn = this.m_aliasList.addButton(buttonInfo[i][0], isControlled);
/* 127 */       btn.setActionCommand(buttonInfo[i][1]);
/* 128 */       btn.addActionListener(listener);
/* 129 */       aliasBtnPanel.add(btn);
/*     */     }
/*     */ 
/* 132 */     JPanel dbWrapper = new PanePanel();
/* 133 */     dbWrapper.add(aliasBtnPanel);
/* 134 */     this.m_aliasList.add("East", dbWrapper);
/*     */ 
/* 137 */     setLayout(new BorderLayout());
/* 138 */     setInsets(5, 10, 5, 10);
/* 139 */     add("Center", this.m_aliasList);
/*     */   }
/*     */ 
/*     */   public void load()
/*     */   {
/*     */     try
/*     */     {
/* 147 */       DataBinder binder = this.m_workflowInfo.m_wfData;
/* 148 */       this.m_aliasSet = new WfStepData();
/* 149 */       this.m_aliasSet.loadAliasStepData(binder.getLocalData());
/* 150 */       refreshList(null);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 154 */       MessageBox.reportError(this.m_systemInterface, e);
/*     */     }
/* 156 */     this.m_helper.loadComponentValues();
/*     */   }
/*     */ 
/*     */   public boolean updateInfo()
/*     */   {
/* 162 */     return this.m_helper.retrieveComponentValues();
/*     */   }
/*     */ 
/*     */   public WorkflowStateInfo getWorkflowInfo()
/*     */   {
/* 168 */     this.m_helper.retrieveComponentValues();
/* 169 */     return super.getWorkflowInfo();
/*     */   }
/*     */ 
/*     */   protected void refreshList(String[] selObjects)
/*     */   {
/*     */     try
/*     */     {
/* 176 */       this.m_aliasList.refreshListEx(this.m_aliasSet, selObjects);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 180 */       AppLauncher.reportOperationError(this.m_systemInterface, e, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getAliases()
/*     */   {
/* 186 */     this.m_aliasSet.first();
/*     */     try
/*     */     {
/* 189 */       return this.m_aliasSet.getAliasesString(this.m_aliasSet);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 193 */       AppLauncher.reportOperationError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToRetrieveAliasList", new Object[0]));
/*     */     }
/* 195 */     return "";
/*     */   }
/*     */ 
/*     */   protected void addAliases()
/*     */   {
/* 205 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 207 */     String helpPage = "AddAliasToStep";
/* 208 */     if (this.m_isTemplate)
/*     */     {
/* 210 */       helpPage = "AddAliasToTemplateStep";
/*     */     }
/* 212 */     else if (this.m_isCriteria)
/*     */     {
/* 214 */       helpPage = "AddAliasToCriteriaStep";
/*     */     }
/*     */ 
/* 217 */     AddAliasDlg dlg = new AddAliasDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddAliasToStep", this.m_cxt), DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 220 */     if ((!dlg.init(null, this.m_aliasSet)) || 
/* 222 */       (dlg.prompt() != 1))
/*     */       return;
/* 224 */     String[] aliases = dlg.getSelected();
/* 225 */     addToList(aliases, "alias");
/*     */   }
/*     */ 
/*     */   protected void addUsers()
/*     */   {
/* 232 */     this.m_helper.retrieveComponentValues();
/*     */ 
/* 234 */     String helpPage = "AddUserToStep";
/* 235 */     if (this.m_isTemplate)
/*     */     {
/* 237 */       helpPage = "AddUserToTemplateStep";
/*     */     }
/* 239 */     else if (this.m_isCriteria)
/*     */     {
/* 241 */       helpPage = "AddUserToCriteriaStep";
/*     */     }
/*     */ 
/* 244 */     ViewDlg dlg = new ViewDlg(null, this.m_systemInterface, LocaleResources.getString("apTitleAddUserToStep", this.m_cxt), this, DialogHelpTable.getHelpPage(helpPage));
/*     */ 
/* 246 */     ViewData viewData = new ViewData(2);
/* 247 */     viewData.m_isMultipleMode = true;
/* 248 */     viewData.m_isViewOnly = false;
/* 249 */     viewData.m_viewName = "UserSelectView";
/*     */ 
/* 251 */     dlg.init(viewData, null);
/* 252 */     if (dlg.prompt() != 1)
/*     */       return;
/* 254 */     String[] users = dlg.getSelectedObjs();
/* 255 */     addToList(users, "user");
/*     */   }
/*     */ 
/*     */   protected void addToken()
/*     */   {
/* 261 */     String helpPage = null;
/* 262 */     if (this.m_isTemplate)
/*     */     {
/* 264 */       helpPage = "AddTokenToTemplateStep";
/*     */     }
/* 266 */     else if (this.m_isCriteria)
/*     */     {
/* 268 */       helpPage = "AddTokenToTemplateStep";
/*     */     }
/*     */     else
/*     */     {
/* 272 */       helpPage = "AddTokenToStep";
/*     */     }
/*     */ 
/* 275 */     SelectTokenDlg dlg = new SelectTokenDlg(this.m_systemInterface, LocaleResources.getString("apTitleAddTokenToStep", this.m_cxt), helpPage);
/*     */ 
/* 277 */     if (dlg.init(this.m_context) != 1)
/*     */       return;
/* 279 */     String[] users = dlg.getSelectedObjs();
/* 280 */     addToList(users, "token");
/*     */   }
/*     */ 
/*     */   protected void addToList(String[] items, String type)
/*     */   {
/* 287 */     if (items == null)
/*     */     {
/* 289 */       return;
/*     */     }
/*     */ 
/* 292 */     String[] keys = { "dAlias", "dAliasType" };
/* 293 */     FieldInfo[] infos = null;
/*     */     try
/*     */     {
/* 296 */       infos = ResultSetUtils.createInfoList(this.m_aliasSet, keys, true);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 300 */       IdcMessage error = IdcMessageFactory.lc("apUnableToAddAlias", new Object[0]);
/* 301 */       AppLauncher.reportOperationError(this.m_systemInterface, e, error);
/* 302 */       return;
/*     */     }
/*     */ 
/* 305 */     Vector newItems = new IdcVector();
/* 306 */     for (int i = 0; i < items.length; ++i)
/*     */     {
/* 308 */       String item = items[i];
/*     */ 
/* 310 */       boolean isFound = false;
/* 311 */       for (this.m_aliasSet.first(); this.m_aliasSet.isRowPresent(); this.m_aliasSet.next())
/*     */       {
/* 313 */         String alias = this.m_aliasSet.getStringValue(infos[0].m_index);
/* 314 */         String aliasType = this.m_aliasSet.getStringValue(infos[1].m_index);
/*     */ 
/* 316 */         if (!type.equalsIgnoreCase(aliasType)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 320 */         if (!alias.equalsIgnoreCase(item))
/*     */           continue;
/* 322 */         isFound = true;
/* 323 */         break;
/*     */       }
/*     */ 
/* 326 */       if (isFound)
/*     */         continue;
/* 328 */       newItems.addElement(item);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 334 */       int num = newItems.size();
/* 335 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 337 */         String item = (String)newItems.elementAt(i);
/*     */ 
/* 339 */         this.m_workflowInfo.setValue("dAlias", item);
/* 340 */         this.m_workflowInfo.setValue("dAliasType", type);
/* 341 */         Vector values = this.m_aliasSet.createRow(this.m_workflowInfo);
/* 342 */         this.m_aliasSet.addRow(values);
/*     */       }
/* 344 */       ResultSetUtils.sortResultSet(this.m_aliasSet, keys);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*     */     }
/*     */ 
/* 351 */     refreshList(items);
/*     */   }
/*     */ 
/*     */   protected void deleteAlias()
/*     */   {
/* 356 */     int[] index = this.m_aliasList.getSelectedIndexes();
/*     */ 
/* 358 */     if (MessageBox.doMessage(this.m_systemInterface, IdcMessageFactory.lc("apVerifySelectedUserDelete", new Object[0]), 2) == 0)
/*     */     {
/* 362 */       return;
/*     */     }
/*     */ 
/* 366 */     for (int i = index.length - 1; i >= 0; --i)
/*     */     {
/* 368 */       this.m_aliasSet.setCurrentRow(index[i]);
/* 369 */       this.m_aliasSet.deleteCurrentRow();
/*     */     }
/* 371 */     this.m_aliasList.refreshList(this.m_aliasSet, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 377 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78892 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.workflow.WfStepEditView
 * JD-Core Version:    0.5.4
 */