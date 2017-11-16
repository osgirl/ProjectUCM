/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.CustomSecurityRightsData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class CustomSecurityRightsUtils
/*     */ {
/*     */   public static void setDefaultCustomPrivileges(Workspace ws)
/*     */     throws DataException
/*     */   {
/*  33 */     DataBinder binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  34 */     PageMerger pageMerger = new PageMerger(binder, null);
/*     */ 
/*  36 */     DataResultSet drset = SharedObjects.getTable("CustomSecurityRoleDefaultRights");
/*  37 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  39 */       String className = ResultSetUtils.getValue(drset, "className");
/*  40 */       String roleName = ResultSetUtils.getValue(drset, "roleName");
/*  41 */       String rights = ResultSetUtils.getValue(drset, "rights");
/*  42 */       String disableConditionStr = ResultSetUtils.getValue(drset, "disableCondition");
/*     */ 
/*  45 */       boolean isDisableCondition = false;
/*     */       try
/*     */       {
/*  48 */         String condStr = pageMerger.evaluateScript(disableConditionStr);
/*  49 */         isDisableCondition = StringUtils.convertToBool(condStr, false);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*  53 */         String msg = LocaleUtils.encodeMessage("csUnableEvaluateCondition", null);
/*  54 */         SystemUtils.err(e, msg);
/*  55 */         isDisableCondition = true;
/*     */       }
/*     */ 
/*  58 */       if (isDisableCondition)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  64 */       className = className.toLowerCase();
/*  65 */       rights = rights.toLowerCase();
/*     */ 
/*  67 */       if (!CustomSecurityRightsData.isValidClass(className))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  73 */       binder.putLocal("dRoleName", roleName);
/*  74 */       ResultSet rset = ws.createResultSet("Qrole", binder);
/*  75 */       DataResultSet roleSet = new DataResultSet();
/*  76 */       roleSet.copy(rset);
/*     */ 
/*  78 */       if (rset.isEmpty())
/*     */       {
/*  80 */         binder.putLocal("dPrivilege", "0");
/*  81 */         binder.putLocal("dRoleDisplayName", "");
/*  82 */         ws.execute("Irole", binder);
/*     */       }
/*     */ 
/*  86 */       int groupIndex = ResultSetUtils.getIndexMustExist(roleSet, "dGroupName");
/*     */ 
/*  88 */       String groupName = CustomSecurityRightsData.getClassGroup(className);
/*  89 */       if (roleSet.findRow(groupIndex, groupName) != null)
/*     */         continue;
/*  91 */       long privilege = CustomSecurityRightsData.calculatePrivilegeFromList(className, rights);
/*     */ 
/*  94 */       ResultSet rs = ws.createResultSet("QroleDisplayName", binder);
/*  95 */       String displayName = rs.getStringValueByName("dRoleDisplayName");
/*     */ 
/*  98 */       binder.putLocal("dRoleName", roleName);
/*  99 */       binder.putLocal("dGroupName", groupName);
/* 100 */       binder.putLocal("dPrivilege", "" + privilege);
/* 101 */       binder.putLocal("dRoleDisplayName", displayName);
/*     */ 
/* 103 */       ws.execute("IroleDefinition", binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98095 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.CustomSecurityRightsUtils
 * JD-Core Version:    0.5.4
 */