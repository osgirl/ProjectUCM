/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.SharedLoader;
/*     */ 
/*     */ public class ConfigSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public ConfigSubjectCallback()
/*     */   {
/*  33 */     setListsWithEnv(new String[] { "ClassAliases", "TracingConfiguration", "DpDisplayIncludes", "AppletHelpPages" }, null, new String[] { "IDC_Name", "InstanceMenuLabel", "InstanceDescription", "UseAccounts", "DefaultAccounts", "UseCollaboration", "IsAutoNumber", "HasExternalUsers", "HasGlobalUsers", "DefaultPasswordEncoding", "DatabaseWildcards", "NumAdditionalRenditions", "HttpServerAddress", "HttpRelativeWebRoot", "SystemLocale", "IsBeta", "ValidHelpLangs", "StringsDebugPrefix", "UseAltaVista", "UseTamino", "UseTaminoXML", "SearchIndexerEngineName", "MinMemoFieldSize", "UseVdkLegacyRebuild", "UseVdkLegacySearch", "UseDatabaseSearch", "ApplicationFont", "MaxStandardDatabaseResults", "ShowAllRevisionsForArchiverPreview", "IdcProductVersion", "IdcProductVersionInfo", "IdcProductBuildInfo", "SchemaBatchEditCheck", "UseCustomSecurityRights", "UseRoleAndAliasDisplayNames", "SupportFastRebuild", "SupportAdvanceConfigOptions", "UseOhsHelpSystem", "NumDecimalScale", "IsElectronicSignaturesInstalled", "SpecialProfileRuleFields" });
/*     */   }
/*     */ 
/*     */   public void refresh(String subject)
/*     */   {
/*     */     try
/*     */     {
/*  87 */       refreshTracing();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  91 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void refreshTracing() throws ServiceException
/*     */   {
/*  97 */     String dir = LegacyDirectoryLocator.getAppDataDirectory() + "/config";
/*  98 */     if (FileUtils.checkFile(dir, false, true) != 0)
/*     */     {
/* 100 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 104 */       FileUtils.reserveDirectory(dir);
/* 105 */       DataBinder binder = new DataBinder();
/* 106 */       ResourceUtils.serializeDataBinder(dir, "tracing.hda", binder, false, false);
/*     */ 
/* 108 */       SharedLoader.configureTracing(binder);
/*     */     }
/*     */     finally
/*     */     {
/* 112 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 118 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99139 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.ConfigSubjectCallback
 * JD-Core Version:    0.5.4
 */