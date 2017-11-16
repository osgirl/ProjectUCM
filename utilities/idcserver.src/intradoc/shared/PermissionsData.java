/*    */ package intradoc.shared;
/*    */ 
/*    */ public class PermissionsData
/*    */ {
/* 26 */   public static final String[][] m_defs = { { "apRightsRead", "R", "0x01" }, { "apRightsWrite", "W", "0x02" }, { "apRightsDelete", "D", "0x04" }, { "apRightsAdmin", "A", "0x08" } };
/*    */   public static final int READ_PRIVILEGE = 1;
/*    */   public static final int WRITE_PRIVILEGE = 2;
/*    */   public static final int DELETE_PRIVILEGE = 4;
/*    */   public static final int ADMIN_PRIVILEGE = 8;
/*    */   public static final int GLOBAL_PRIVILEGE = 16;
/*    */   public static final int EXECINSCRIPT_PRIVILEGE = 32;
/*    */   public static final int ANNOTATED_STANDARD_SECURITY = 64;
/*    */   public static final int ANNOTATED_RESTRICTED_SECURITY = 128;
/*    */   public static final int ANNOTATED_HIDDEN_SECURITY = 256;
/*    */   public static final int ANNOTATED_LOCK_ADMINISTRATOR_SECURITY = 512;
/*    */   public static final String m_appPsGroupName = "#AppsGroup";
/*    */   public static final int APP_USERADMIN_PRIVILEGE = 1;
/*    */   public static final int APP_WEBLAYOUT_PRIVILEGE = 2;
/*    */   public static final int APP_DOCMAN_PRIVILEGE = 4;
/*    */   public static final int APP_WORKFLOW_PRIVILEGE = 8;
/*    */   public static final int APP_DOCCONFIG_PRIVILEGE = 16;
/*    */   public static final int APP_ARCHIVER_PRIVILEGE = 32;
/* 60 */   public static final String[][] m_appPsgDefs = { { "apRightsAppsUserAdmin", "UserAdmin", "0x01" }, { "apRightsAppsWebLayout", "WebLayout", "0x02" }, { "apRightsAppsRepMan", "RepoMan", "0x04" }, { "apRightsAppsWorkflow", "Workflow", "0x08" } };
/*    */ 
/* 69 */   public static final String[][] m_appAllPsgDefs = { { "apRightsAppsUserAdmin", "UserAdmin", "0x01" }, { "apRightsAppsWebLayout", "WebLayout", "0x02" }, { "apRightsAppsRepMan", "RepoMan", "0x04" }, { "apRightsAppsWorkflow", "Workflow", "0x08" }, { "apRightsAppsConfigMan", "ConfigMan", "0x10" }, { "apRightsAppsArchiver", "Archiver", "0x20" } };
/*    */   public int m_privilege;
/*    */   public long m_customPrivilege;
/*    */ 
/*    */   public PermissionsData()
/*    */   {
/* 88 */     this.m_privilege = 0;
/*    */   }
/*    */ 
/*    */   public void setPrivilege(int privilege)
/*    */   {
/* 93 */     this.m_privilege = privilege;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 98 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101151 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.PermissionsData
 * JD-Core Version:    0.5.4
 */