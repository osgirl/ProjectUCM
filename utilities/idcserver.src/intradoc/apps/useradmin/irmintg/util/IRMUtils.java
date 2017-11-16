/*     */ package intradoc.apps.useradmin.irmintg.util;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.useradmin.irmintg.IRMConstraints;
/*     */ import intradoc.apps.useradmin.irmintg.IRMEditPermissionsDlg;
/*     */ import intradoc.apps.useradmin.irmintg.IRMGroupRoleFeaturesDlg;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.RoleGroupData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class IRMUtils
/*     */ {
/*     */   protected static Boolean m_isIRMIntgEnabled;
/*     */ 
/*     */   public static String[][] getContentOptions(ExecutionContext ctx)
/*     */   {
/*  67 */     String[][] contentOptions = { { "ALLOW", LocaleResources.getString("apOptionAllow", ctx) }, { "REFUSE", LocaleResources.getString("apOptionRefuse", ctx) }, { "WARN", LocaleResources.getString("apOptionWarn", ctx) } };
/*     */ 
/*  71 */     return contentOptions;
/*     */   }
/*     */ 
/*     */   public static Vector getRefreshPeriods(ExecutionContext ctx)
/*     */   {
/*  82 */     DataBinder binder = new DataBinder();
/*     */     Vector refreshPeriods;
/*     */     try
/*     */     {
/*  86 */       AppLauncher.executeService("IRM_GET_REFRESH_PERIODS", binder);
/*     */ 
/*  88 */       refreshPeriods = binder.getOptionList("refreshPeriodsList");
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  94 */       refreshPeriods = new IdcVector();
/*  95 */       refreshPeriods.add(LocaleResources.getString("ap3hrs", ctx));
/*  96 */       refreshPeriods.add(LocaleResources.getString("ap1day", ctx));
/*  97 */       refreshPeriods.add(LocaleResources.getString("ap3days", ctx));
/*  98 */       refreshPeriods.add(LocaleResources.getString("ap10days", ctx));
/*     */     }
/*     */ 
/* 101 */     return refreshPeriods;
/*     */   }
/*     */ 
/*     */   public static boolean isInputValid(String inputText)
/*     */   {
/* 112 */     return Pattern.matches("[\\p{L}\\p{N}]+[[^[:punct:]--[.,\"';:#$%/@^_-]][\\p{L}\\p{N}]+]*", inputText);
/*     */   }
/*     */ 
/*     */   public void updateFeatureMappingConstraints(IRMEditPermissionsDlg dlg)
/*     */     throws ServiceException
/*     */   {
/* 125 */     if (!dlg.m_isIRMTabEnabled)
/*     */       return;
/* 127 */     DataBinder binder = new DataBinder();
/* 128 */     DataResultSet rsMap = new DataResultSet();
/*     */ 
/* 131 */     dlg.getIRMClientRightsHelper().getMappingResultSet(rsMap);
/*     */ 
/* 133 */     binder.addResultSet("rsFeatureMap", rsMap);
/* 134 */     binder.putLocal("dGroupName", dlg.getRoleGroupData().m_groupName);
/*     */ 
/* 136 */     binder.putLocal("dRoleName", dlg.getRoleGroupData().m_roleName);
/*     */ 
/* 138 */     binder.putLocal("hasOverriddenFeatures", Boolean.toString(dlg.getIRMClientRightsHelper().isDefaultFeatureList()));
/*     */ 
/* 142 */     AppLauncher.executeService("EDIT_IRM_MAPPING", binder);
/*     */ 
/* 146 */     dlg.getIRMConstraintsHelper().getConstraints(binder);
/* 147 */     AppLauncher.executeService("EDIT_IRM_CONSTRAINTS", binder);
/*     */   }
/*     */ 
/*     */   public static boolean isIRMIntgEnabled()
/*     */   {
/* 160 */     if (m_isIRMIntgEnabled == null)
/*     */     {
/* 162 */       m_isIRMIntgEnabled = Boolean.FALSE;
/*     */ 
/* 165 */       DataBinder configInfoBinder = new DataBinder();
/*     */       try
/*     */       {
/* 168 */         AppLauncher.executeService("CONFIG_INFO", configInfoBinder);
/*     */ 
/* 171 */         ResultSet rsEnabledComponents = configInfoBinder.getResultSet("EnabledComponents");
/*     */ 
/* 173 */         String irmUcmComponent = ResultSetUtils.findValue(rsEnabledComponents, "name", "IRMUCMIntegration", "name");
/*     */ 
/* 178 */         if (irmUcmComponent != null)
/*     */         {
/* 180 */           m_isIRMIntgEnabled = Boolean.TRUE;
/*     */         }
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 185 */         m_isIRMIntgEnabled = Boolean.FALSE;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 189 */         m_isIRMIntgEnabled = Boolean.FALSE;
/*     */       }
/*     */     }
/* 192 */     return m_isIRMIntgEnabled.booleanValue();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 203 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94435 $";
/*     */   }
/*     */ 
/*     */   public static enum m_nonUCMSealedContent
/*     */   {
/*  53 */     ALLOW, REFUSE, WARN;
/*     */   }
/*     */ 
/*     */   public static enum m_unsealableTypeOptions
/*     */   {
/*  45 */     ALLOW, REFUSE, WARN;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.useradmin.irmintg.util.IRMUtils
 * JD-Core Version:    0.5.4
 */