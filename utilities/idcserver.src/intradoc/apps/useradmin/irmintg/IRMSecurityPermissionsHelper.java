/*    */ package intradoc.apps.useradmin.irmintg;
/*    */ 
/*    */ import intradoc.apps.useradmin.SecurityPermissionsHelper;
/*    */ import intradoc.apps.useradmin.irmintg.util.IRMUtils;
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.common.SystemInterface;
/*    */ import intradoc.gui.ContainerHelper;
/*    */ import intradoc.shared.RoleGroupData;
/*    */ 
/*    */ public class IRMSecurityPermissionsHelper extends SecurityPermissionsHelper
/*    */ {
/*    */   IRMEditPermissionsDlg m_dlg;
/*    */ 
/*    */   public IRMSecurityPermissionsHelper(SystemInterface sys, ContainerHelper helper, boolean isForDialog)
/*    */   {
/* 45 */     super(sys, helper, isForDialog);
/*    */   }
/*    */ 
/*    */   public void createAndInitEditPermissionsDialog(RoleGroupData data, String title)
/*    */   {
/* 58 */     this.m_dlg = new IRMEditPermissionsDlg(this.m_systemInterface, title);
/* 59 */     this.m_dlg.setRoleGroupData(data);
/* 60 */     this.m_dlg.init(data);
/*    */   }
/*    */ 
/*    */   protected void editRolePrivileges(boolean isPermissions, boolean isAppletRights)
/*    */   {
/* 73 */     super.editRolePrivileges(isPermissions, isAppletRights);
/*    */     try
/*    */     {
/* 76 */       IRMUtils obj = new IRMUtils();
/* 77 */       obj.updateFeatureMappingConstraints(this.m_dlg);
/*    */     }
/*    */     catch (ServiceException exp)
/*    */     {
/* 81 */       reportError(exp);
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92579 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.IRMSecurityPermissionsHelper
 * JD-Core Version:    0.5.4
 */