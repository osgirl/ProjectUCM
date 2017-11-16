/*     */ package intradoc.apps.useradmin.irmintg;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.EditPermissionsDlg;
/*     */ import intradoc.apps.useradmin.GroupPermissionsDlg;
/*     */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.FixedSizeList;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IRMGroupPermissionsDlg extends GroupPermissionsDlg
/*     */ {
/*     */   protected SystemInterface m_systemInterface;
/*     */   protected IRMEditPermissionsDlg m_dlg;
/*     */   protected JButton m_irmProtectionBtn;
/*     */ 
/*     */   public IRMGroupPermissionsDlg(SystemInterface sys, String title)
/*     */   {
/*  62 */     super(sys, title);
/*  63 */     this.m_systemInterface = sys;
/*     */   }
/*     */ 
/*     */   public void editGroups(ActionEvent evt)
/*     */   {
/*  75 */     String groupName = this.m_groupList.getSelectedItem();
/*     */ 
/*  77 */     super.editGroups(evt);
/*     */ 
/*  79 */     Object src = evt.getSource();
/*  80 */     if (src != this.m_deleteGroupBtn) {
/*     */       return;
/*     */     }
/*     */ 
/*  84 */     removeGroupProtection(groupName);
/*     */   }
/*     */ 
/*     */   protected void showHelperDialog()
/*     */   {
/*  96 */     if (this.m_permissionsPanel != null)
/*     */     {
/*  98 */       addIRMProtectionBtn(this.m_permissionsPanel);
/*     */     }
/* 100 */     super.showHelperDialog();
/*     */   }
/*     */ 
/*     */   public void addIRMProtectionBtn(JPanel permissionsPanel)
/*     */   {
/* 110 */     this.m_irmProtectionBtn = new JButton(LocaleResources.getString("apLabelIRMProtection", this.m_ctx));
/*     */ 
/* 112 */     permissionsPanel.add(this.m_irmProtectionBtn);
/* 113 */     ActionListener irmProtectionListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 117 */         IRMGroupProtectionDlg irmGroupProtectionDlg = new IRMGroupProtectionDlg(IRMGroupPermissionsDlg.this.m_system, LocaleResources.getString("apLabelIRMProtection", IRMGroupPermissionsDlg.this.m_ctx), IRMGroupPermissionsDlg.this.m_groupList.getSelectedItem());
/*     */ 
/* 121 */         irmGroupProtectionDlg.init();
/*     */       }
/*     */     };
/* 124 */     this.m_irmProtectionBtn.addActionListener(irmProtectionListener);
/*     */   }
/*     */ 
/*     */   public void removeGroupProtection(String groupName)
/*     */   {
/* 134 */     DataBinder binder = new DataBinder();
/* 135 */     Properties localData = binder.getLocalData();
/* 136 */     localData.put("dGroupName", groupName);
/*     */ 
/* 138 */     localData.put("isAccount", Boolean.FALSE.toString());
/* 139 */     localData.put("EnableIRM", Boolean.FALSE.toString());
/* 140 */     localData.put("dGroupName", groupName);
/*     */     try
/*     */     {
/* 143 */       AppLauncher.executeService("IRM_SET_PROTECTION", binder);
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 148 */       MessageBox.reportError(this.m_systemInterface, exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void updateRights(EditPermissionsDlg permsDlg)
/*     */   {
/* 159 */     IRMEditPermissionsDlg dlg = (IRMEditPermissionsDlg)permsDlg;
/*     */     try
/*     */     {
/* 162 */       IRMUtils obj = new IRMUtils();
/* 163 */       obj.updateFeatureMappingConstraints(dlg);
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 167 */       MessageBox.reportError(this.m_systemInterface, exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void editPermissions()
/*     */   {
/* 177 */     super.editPermissions();
/* 178 */     updateRights(this.m_dlg);
/*     */   }
/*     */ 
/*     */   public void createAndInitEditPermissionsDialog(RoleGroupData data, String title)
/*     */   {
/* 191 */     this.m_dlg = new IRMEditPermissionsDlg(this.m_systemInterface, title);
/* 192 */     this.m_dlg.setRoleGroupData(data);
/* 193 */     this.m_dlg.init(data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 204 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMGroupPermissionsDlg
 * JD-Core Version:    0.5.4
 */