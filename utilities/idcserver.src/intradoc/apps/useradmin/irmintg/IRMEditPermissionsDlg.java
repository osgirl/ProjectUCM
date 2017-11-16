/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.EditPermissionsDlg;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.shared.PermissionsData;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ItemEvent;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMEditPermissionsDlg extends EditPermissionsDlg
/*     */ {
/*     */   public boolean m_isIRMTabEnabled;
/*     */   protected IRMGroupRoleFeaturesDlg m_irmClientRightsHelper;
/*     */   protected IRMConstraints m_irmConstraints;
/*     */   protected IRMTabbedPane m_IRMPermissionsTabPnl;
/*     */   protected JPanel irmClientRightsPnl;
/*     */   protected JPanel irmConstraintsPnl;
/*     */   protected RoleGroupData roleGroupData;
/*     */ 
/*     */   public IRMEditPermissionsDlg(SystemInterface sys, String title)
/*     */   {
/*  59 */     super(sys, title);
/*  60 */     this.m_IRMPermissionsTabPnl = new IRMTabbedPane();
/*     */   }
/*     */ 
/*     */   public void setRoleGroupData(RoleGroupData data)
/*     */   {
/*  70 */     this.roleGroupData = data;
/*     */   }
/*     */ 
/*     */   public RoleGroupData getRoleGroupData()
/*     */   {
/*  78 */     return this.roleGroupData;
/*     */   }
/*     */ 
/*     */   protected void irmTabConstructor(JPanel rightsPnl)
/*     */     throws ServiceException
/*     */   {
/*  89 */     this.m_IRMPermissionsTabPnl.setLayout(new GridLayout(0, 1, 0, 0));
/*  90 */     this.m_IRMPermissionsTabPnl.addPane("Rights", rightsPnl);
/*     */ 
/*  92 */     this.irmClientRightsPnl = new PanePanel();
/*  93 */     this.m_IRMPermissionsTabPnl.addPane(LocaleResources.getString("apTitleClientRights", this.m_ctx), this.irmClientRightsPnl);
/*     */ 
/*  96 */     this.irmConstraintsPnl = new PanePanel();
/*  97 */     this.m_IRMPermissionsTabPnl.addPane(LocaleResources.getString("apTitleClientConstraints", this.m_ctx), this.irmConstraintsPnl);
/*     */ 
/* 100 */     this.m_irmClientRightsHelper = new IRMGroupRoleFeaturesDlg(this.irmClientRightsPnl, this.roleGroupData, this.m_ctx, this.m_systemInterface);
/*     */ 
/* 102 */     this.m_irmClientRightsHelper.init();
/*     */ 
/* 104 */     this.m_irmConstraints = new IRMConstraints(this.irmConstraintsPnl, this.roleGroupData, this.m_systemInterface, this.m_ctx, this.m_cancel);
/*     */ 
/* 106 */     this.m_irmConstraints.init();
/*     */ 
/* 108 */     this.m_helper.m_mainPanel.add("South", this.m_IRMPermissionsTabPnl);
/* 109 */     if (this.m_permissionsData.m_privilege == 0)
/*     */     {
/* 111 */       setEnabledIRMTabs(false);
/*     */     }
/*     */     else
/*     */     {
/* 115 */       setEnabledIRMTabs(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addPermissionsPanel(JPanel rightsPnl)
/*     */   {
/* 140 */     if ((!this.m_isAccount) && (this.m_groupName.charAt(0) != '#'))
/*     */     {
/* 143 */       DataBinder binder = new DataBinder();
/*     */       try
/*     */       {
/* 146 */         binder.putLocal("dGroupName", this.m_groupName);
/* 147 */         binder.putLocal("isAccount", Boolean.toString(this.m_isAccount));
/*     */ 
/* 151 */         AppLauncher.executeService("IRM_GET_PROTECTION", binder);
/*     */ 
/* 153 */         this.m_isIRMTabEnabled = Boolean.parseBoolean(binder.getAllowMissing("EnableIRM"));
/*     */ 
/* 157 */         if (this.m_isIRMTabEnabled)
/*     */         {
/* 159 */           irmTabConstructor(rightsPnl);
/*     */         }
/*     */         else
/*     */         {
/* 163 */           super.addPermissionsPanel(rightsPnl);
/*     */         }
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 168 */         MessageBox.reportError(this.m_systemInterface, e);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 173 */       super.addPermissionsPanel(rightsPnl);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 185 */     super.actionPerformed(e);
/* 186 */     IRMGroupRoleFeaturesDlg clientRightsHelper = getIRMClientRightsHelper();
/* 187 */     IRMConstraints constraintsHelper = getIRMConstraintsHelper();
/*     */ 
/* 196 */     if (!this.m_isIRMTabEnabled) {
/*     */       return;
/*     */     }
/*     */ 
/* 200 */     String errFeaturesMapping = clientRightsHelper.setMappingResultSet();
/*     */ 
/* 202 */     String errConstraints = constraintsHelper.setConstraintsResultSet();
/*     */ 
/* 206 */     if (errFeaturesMapping != null)
/*     */     {
/* 208 */       this.m_helper.show();
/* 209 */       this.m_IRMPermissionsTabPnl.selectPane(LocaleResources.getString("apTitleClientRights", this.m_ctx));
/*     */     }
/*     */     else
/*     */     {
/* 213 */       if (errConstraints == null)
/*     */         return;
/* 215 */       this.m_helper.show();
/* 216 */       this.m_IRMPermissionsTabPnl.selectPane(LocaleResources.getString("apTitleClientConstraints", this.m_ctx));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 230 */     super.itemStateChanged(e);
/* 231 */     if (!this.m_isIRMTabEnabled)
/*     */       return;
/* 233 */     long privilege = 0L;
/* 234 */     privilege = super.getPrivilege();
/* 235 */     getIRMClientRightsHelper().updateFeatures(privilege);
/* 236 */     if (privilege == 0L)
/*     */     {
/* 238 */       setEnabledIRMTabs(false);
/*     */     }
/*     */     else
/*     */     {
/* 242 */       setEnabledIRMTabs(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public IRMGroupRoleFeaturesDlg getIRMClientRightsHelper()
/*     */   {
/* 252 */     return this.m_irmClientRightsHelper;
/*     */   }
/*     */ 
/*     */   public IRMConstraints getIRMConstraintsHelper()
/*     */   {
/* 262 */     return this.m_irmConstraints;
/*     */   }
/*     */ 
/*     */   protected void setEnabledIRMTabs(boolean isEnable)
/*     */   {
/* 273 */     this.m_IRMPermissionsTabPnl.setEnabledAt(1, isEnable);
/* 274 */     this.m_IRMPermissionsTabPnl.setEnabledAt(2, isEnable);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 285 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92636 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMEditPermissionsDlg
 * JD-Core Version:    0.5.4
 */