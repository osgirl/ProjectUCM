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
/*     */ import intradoc.shared.CollaborationUtils;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.SecurityAccessListUtils;
/*     */ import intradoc.shared.SecurityUtils;
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
/*     */ public class EditUserProjectPanel extends EditUserBasePanel
/*     */   implements ActionListener
/*     */ {
/*     */   protected UdlPanel m_projectsList;
/*     */ 
/*     */   protected void initUI(boolean isEdit)
/*     */   {
/*  58 */     this.m_helper.makePanelGridBag(this, 1);
/*  59 */     GridBagConstraints gc = this.m_helper.m_gridHelper.m_gc;
/*  60 */     this.m_helper.addLastComponentInRow(this, new CustomText(LocaleResources.getString("apEditUserProjectPanelDesc", this.m_ctx), 80));
/*     */ 
/*  62 */     gc.weighty = 1.0D;
/*  63 */     this.m_helper.addComponent(this, new CustomLabel(LocaleResources.getString("apLabelProjects", this.m_ctx), 1));
/*     */ 
/*  66 */     this.m_projectsList = new UdlPanel(null, null, 225, 5, "ProjectList", false);
/*  67 */     this.m_projectsList.setVisibleColumns("project,privilege");
/*  68 */     this.m_projectsList.init();
/*  69 */     this.m_projectsList.useDefaultListener();
/*  70 */     this.m_projectsList.m_list.addActionListener(this);
/*     */ 
/*  72 */     this.m_helper.addComponent(this, this.m_projectsList);
/*     */ 
/*  74 */     JPanel projectsButtonsPanel = new PanePanel();
/*  75 */     projectsButtonsPanel.setLayout(new GridLayout(0, 1, 10, 5));
/*     */ 
/*  79 */     this.m_helper.addComponent(projectsButtonsPanel, new PanePanel());
/*  80 */     this.m_helper.addCommandButton(projectsButtonsPanel, LocaleResources.getString("apDlgButtonAdd", this.m_ctx), "add", this);
/*     */ 
/*  82 */     JButton editBtn = this.m_helper.addCommandButton(projectsButtonsPanel, LocaleResources.getString("apDlgButtonEdit", this.m_ctx), "edit", this);
/*     */ 
/*  84 */     JButton delBtn = this.m_helper.addCommandButton(projectsButtonsPanel, LocaleResources.getString("apLabelDelete", this.m_ctx), "delete", this);
/*     */ 
/*  88 */     this.m_projectsList.addControlComponent(editBtn);
/*  89 */     this.m_projectsList.addControlComponent(delBtn);
/*     */ 
/*  92 */     gc.fill = 0;
/*  93 */     gc.weightx = 0.0D;
/*  94 */     gc.anchor = 11;
/*  95 */     this.m_helper.addLastComponentInRow(this, projectsButtonsPanel);
/*     */ 
/*  98 */     refreshProjectDisplay();
/*     */   }
/*     */ 
/*     */   protected void refreshProjectDisplay()
/*     */   {
/* 103 */     if (this.m_projectsList == null)
/*     */     {
/* 105 */       return;
/*     */     }
/*     */ 
/* 109 */     DataResultSet drset = new DataResultSet(new String[] { "project", "privilege" });
/* 110 */     String selectedProject = this.m_projectsList.getSelectedObj();
/*     */ 
/* 112 */     Vector v = this.m_editHelper.m_userData.getAttributes("account");
/* 113 */     if (v != null)
/*     */     {
/* 115 */       int naccounts = v.size();
/* 116 */       for (int i = 0; i < naccounts; ++i)
/*     */       {
/* 118 */         Vector row = drset.createEmptyRow();
/* 119 */         UserAttribInfo uai = (UserAttribInfo)v.elementAt(i);
/* 120 */         String projectDisplay = CollaborationUtils.getPresentationString(uai.m_attribName, this.m_ctx);
/*     */ 
/* 122 */         if (projectDisplay == null) continue; if (projectDisplay.equals(""))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 128 */         row.setElementAt(projectDisplay, 0);
/*     */ 
/* 130 */         String pStr = SecurityAccessListUtils.makePrivilegeStr(uai.m_attribPrivilege);
/* 131 */         row.setElementAt(pStr, 1);
/*     */ 
/* 133 */         drset.addRow(row);
/*     */       }
/*     */     }
/*     */ 
/* 137 */     this.m_projectsList.refreshList(drset, selectedProject);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 142 */     handleProjectAction(e);
/*     */ 
/* 145 */     validate();
/*     */   }
/*     */ 
/*     */   protected void handleProjectAction(ActionEvent evt)
/*     */   {
/* 151 */     String command = evt.getActionCommand();
/* 152 */     Object src = evt.getSource();
/* 153 */     boolean isListAction = this.m_projectsList.m_list == src;
/* 154 */     boolean isAdd = (!isListAction) && (command.equals("add"));
/* 155 */     String displayProject = null;
/* 156 */     String project = null;
/* 157 */     int acctIndex = -1;
/*     */ 
/* 159 */     Vector acctList = this.m_editHelper.m_userData.getAttributes("account");
/* 160 */     if (acctList == null)
/*     */     {
/* 162 */       acctList = new IdcVector();
/*     */     }
/*     */ 
/* 166 */     if (!isAdd)
/*     */     {
/* 168 */       displayProject = this.m_projectsList.getSelectedObj();
/* 169 */       if (displayProject == null)
/*     */       {
/* 171 */         return;
/*     */       }
/* 173 */       project = CollaborationUtils.getInternalString(displayProject, this.m_ctx);
/*     */ 
/* 175 */       int naccounts = acctList.size();
/* 176 */       for (int i = 0; i < naccounts; ++i)
/*     */       {
/* 178 */         UserAttribInfo uai = (UserAttribInfo)acctList.elementAt(i);
/* 179 */         if (!project.equals(uai.m_attribName))
/*     */           continue;
/* 181 */         acctIndex = i;
/* 182 */         break;
/*     */       }
/*     */ 
/* 186 */       if (acctIndex == -1)
/*     */       {
/* 188 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 193 */     boolean selfDeniedActions = (!this.m_isAdmin) && (this.m_isEditSelf);
/*     */ 
/* 195 */     if (command.equals("delete"))
/*     */     {
/* 197 */       if (selfDeniedActions)
/*     */       {
/* 199 */         reportError(IdcMessageFactory.lc("apCannotAlterPrivilegeToOwnProject", new Object[0]));
/* 200 */         return;
/*     */       }
/* 202 */       acctList.removeElementAt(acctIndex);
/*     */     }
/*     */     else
/*     */     {
/* 207 */       UserAttribInfo uai = null;
/* 208 */       int priv = 15;
/* 209 */       if (!this.m_isAdmin)
/*     */       {
/*     */         try
/*     */         {
/* 213 */           priv = SecurityUtils.determineBestAccountPrivilege(this.m_loggedInUserData, null);
/*     */         }
/*     */         catch (ServiceException ignore)
/*     */         {
/* 217 */           if (SystemUtils.m_verbose)
/*     */           {
/* 219 */             Report.debug("system", null, ignore);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 224 */       String title = null;
/* 225 */       if (isAdd)
/*     */       {
/* 227 */         if (selfDeniedActions)
/*     */         {
/* 229 */           reportError(IdcMessageFactory.lc("apCannotAddProjectsToOwnUserProfile", new Object[0]));
/* 230 */           return;
/*     */         }
/*     */ 
/* 233 */         title = LocaleResources.getString("apTitleAddNewProject", this.m_ctx);
/*     */       }
/*     */       else
/*     */       {
/* 237 */         if (selfDeniedActions)
/*     */         {
/* 239 */           reportError(IdcMessageFactory.lc("apCannotAlterPrivilegeToOwnProject", new Object[0]));
/* 240 */           return;
/*     */         }
/*     */ 
/* 243 */         uai = (UserAttribInfo)acctList.elementAt(acctIndex);
/* 244 */         priv = uai.m_attribPrivilege;
/*     */ 
/* 246 */         title = LocaleResources.getString("apTitleEditPermissionsForProject", this.m_ctx, displayProject);
/*     */       }
/*     */ 
/* 250 */       EditPermissionsDlg dlg = new EditPermissionsDlg(this.m_systemInterface, title);
/* 251 */       PermissionsData permData = new PermissionsData();
/* 252 */       permData.setPrivilege(priv);
/* 253 */       if (!dlg.promptAccount(permData, this.m_helper.m_props, this.m_loggedInUserData, this.m_editHelper.m_userData, project, isAdd))
/*     */       {
/* 256 */         return;
/*     */       }
/*     */ 
/* 259 */       if (isAdd)
/*     */       {
/* 261 */         project = this.m_helper.m_props.getProperty("dDocAccount");
/* 262 */         String privNumStr = Integer.toString(permData.m_privilege);
/* 263 */         this.m_editHelper.m_userData.addAttribute("account", project, privNumStr);
/*     */       }
/*     */       else
/*     */       {
/* 267 */         uai.m_attribPrivilege = permData.m_privilege;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 272 */     refreshProjectDisplay();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 277 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.EditUserProjectPanel
 * JD-Core Version:    0.5.4
 */