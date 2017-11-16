/*    */ package intradoc.shared;
/*    */ 
/*    */ public class RoleGroupData extends PermissionsData
/*    */ {
/*    */   public String m_roleName;
/*    */   public String m_groupName;
/*    */   public String m_roleDisplayName;
/*    */ 
/*    */   public RoleGroupData(String roleName, String groupName, long priv, String displayName)
/*    */   {
/* 36 */     this.m_roleName = roleName;
/* 37 */     this.m_groupName = groupName;
/* 38 */     this.m_roleDisplayName = displayName;
/*    */ 
/* 41 */     if (groupName.startsWith("$"))
/*    */     {
/* 43 */       this.m_customPrivilege = priv;
/*    */     }
/*    */     else
/*    */     {
/* 49 */       this.m_privilege = (int)priv;
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 55 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.RoleGroupData
 * JD-Core Version:    0.5.4
 */