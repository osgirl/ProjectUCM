/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.alert.AlertUtils;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerConfigurationAnalyzer
/*     */ {
/*     */   protected static DataBinder m_binder;
/*     */ 
/*     */   public static void analyzeCurrentConfiguration(Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  55 */     analyzeComponents();
/*  56 */     analyzeFeatures();
/*  57 */     analyzeClassesDirectory();
/*  58 */     analyzeDatabaseConfiguration(workspace);
/*  59 */     analyzeSearchConfiguration(workspace);
/*     */ 
/*  61 */     String specialAuthGroups = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/*  62 */     if (specialAuthGroups != null)
/*     */     {
/*  64 */       Vector authGroups = StringUtils.parseArrayEx(specialAuthGroups, ',', '^', true);
/*  65 */       if (authGroups.contains("Public"))
/*     */       {
/*  67 */         IdcMessage message = IdcMessageFactory.lc("csSpecialAuthGroupsHasPublic", new Object[0]);
/*  68 */         addAlert("SpecialAuthGroupsHasPublic", message, null);
/*     */       }
/*     */     }
/*     */ 
/*  72 */     if (SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigFilter", false))
/*     */       return;
/*  74 */     PluginFilters.filter("analyzeServerConfig", workspace, new DataBinder(), new ExecutionContextAdaptor());
/*     */   }
/*     */ 
/*     */   public static void analyzeComponents()
/*     */     throws DataException, ServiceException
/*     */   {
/*  80 */     if (SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigComponents", false))
/*     */     {
/*  82 */       return;
/*     */     }
/*     */ 
/*  85 */     ComponentListEditor cle = ComponentListManager.getEditor();
/*  86 */     DataResultSet components = SharedObjects.getTable("DeprecatedComponents");
/*  87 */     FieldInfo[] fields = ResultSetUtils.createInfoList(components, new String[] { "componentName", "messageKey" }, true);
/*     */ 
/*  89 */     for (components.first(); components.isRowPresent(); components.next())
/*     */     {
/*  91 */       String componentName = components.getStringValue(fields[0].m_index);
/*  92 */       if (!cle.isComponentEnabled(componentName))
/*     */         continue;
/*  94 */       IdcMessage reason = null;
/*  95 */       String messageKey = components.getStringValue(fields[1].m_index);
/*  96 */       if (messageKey.length() == 0)
/*     */       {
/*  98 */         reason = IdcMessageFactory.lc(messageKey, new Object[] { componentName });
/*     */       }
/* 100 */       IdcMessage message = IdcMessageFactory.lc(reason, "csDeprecatedComponentInstalled", new Object[] { componentName });
/* 101 */       addAlert("DeprecatedComponent:" + componentName, message, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void analyzeFeatures()
/*     */     throws DataException, ServiceException
/*     */   {
/* 108 */     if (SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigFeatures", false))
/*     */     {
/* 110 */       return;
/*     */     }
/*     */ 
/* 113 */     DataResultSet features = SharedObjects.getTable("DeprecatedFeatures");
/* 114 */     FieldInfo[] fields = ResultSetUtils.createInfoList(features, new String[] { "featureName", "featureLevel", "messageKey" }, true);
/*     */ 
/* 116 */     for (features.first(); features.isRowPresent(); features.next())
/*     */     {
/* 118 */       String featureName = features.getStringValue(fields[0].m_index);
/* 119 */       String currentLevel = (String)Features.getLevel(featureName);
/* 120 */       if (currentLevel == null)
/*     */         continue;
/* 122 */       String badLevel = features.getStringValue(fields[1].m_index);
/* 123 */       int cmp = SystemUtils.compareVersions(badLevel, currentLevel);
/* 124 */       if (cmp < 0)
/*     */         continue;
/* 126 */       String messageKey = features.getStringValue(fields[2].m_index);
/* 127 */       if (messageKey.length() == 0)
/*     */       {
/* 129 */         messageKey = "csDeprecatedFeatureEnabled";
/*     */       }
/* 131 */       List list = Features.getFeatureComponents(featureName);
/* 132 */       String compList = StringUtils.createString(list, ',', '^');
/* 133 */       IdcMessage message = IdcMessageFactory.lc(messageKey, new Object[] { featureName, badLevel, currentLevel, compList });
/* 134 */       addAlert("DeprecatedFeature:" + featureName, message, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void analyzeClassesDirectory()
/*     */     throws DataException, ServiceException
/*     */   {
/* 142 */     if (SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigClassesDir", false))
/*     */     {
/* 144 */       return;
/*     */     }
/*     */ 
/* 147 */     String classesDir = DirectoryLocator.getIntradocDir() + "classes/";
/* 148 */     DataResultSet packages = SharedObjects.getTable("DeprecatedClassesDirPackages");
/* 149 */     FieldInfo[] fields = ResultSetUtils.createInfoList(packages, new String[] { "packageName", "messageKey" }, true);
/*     */ 
/* 151 */     for (packages.first(); packages.isRowPresent(); packages.next())
/*     */     {
/* 153 */       String packageName = packages.getStringValue(fields[0].m_index);
/* 154 */       String packagePath = packageName.replace('.', '/');
/* 155 */       String fullPackagePath = classesDir + packagePath;
/* 156 */       File f = new File(fullPackagePath);
/* 157 */       if (!f.isDirectory())
/*     */         continue;
/* 159 */       String messageKey = packages.getStringValue(fields[1].m_index);
/* 160 */       if (messageKey.length() == 0)
/*     */       {
/* 162 */         messageKey = "csDeprecatedClassesDirPackageFound";
/*     */       }
/* 164 */       IdcMessage message = IdcMessageFactory.lc(messageKey, new Object[] { packageName, packagePath });
/* 165 */       addAlert("ClassesDir:" + packageName, message, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void analyzeDatabaseConfiguration(Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/* 173 */     if (!SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigDatabase", false))
/*     */       return;
/* 175 */     return;
/*     */   }
/*     */ 
/*     */   public static void analyzeSearchConfiguration(Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/* 182 */     if ((SharedObjects.getEnvValueAsBoolean("SkipAnalyzeServerConfigSearch", false)) || (!Features.checkLevel("Search", null)))
/*     */     {
/* 185 */       return;
/*     */     }
/*     */ 
/* 188 */     CommonSearchConfig csc = SearchIndexerUtils.getCommonSearchConfig(IdcSystemLoader.getInitializationContext());
/*     */ 
/* 190 */     if ((workspace == null) || (csc == null) || (!csc.getCurrentEngineName().equalsIgnoreCase("DATABASE.FULLTEXT")) || 
/* 192 */       (!workspace.getProperty("DatabaseType").equalsIgnoreCase("ORACLE")) || 
/* 194 */       (!StringUtils.convertToBool(workspace.getProperty("UseUnicode"), false)))
/*     */       return;
/* 196 */     IdcMessage message = IdcMessageFactory.lc("csUsingUnicodeWithOracleFullTextDatabase", new Object[0]);
/* 197 */     addAlert("OracleFullTextUnicode", message, null);
/*     */   }
/*     */ 
/*     */   public static void addAlert(String alertId, IdcMessage m, String url)
/*     */     throws DataException, ServiceException
/*     */   {
/* 206 */     Report.alert(null, new StackTrace(), m);
/*     */ 
/* 208 */     DataBinder binder = getAlertBinder();
/* 209 */     binder.putLocal("alertId", alertId);
/* 210 */     binder.putLocal("alertMsg", "<$lcMessage('" + LocaleUtils.encodeMessage(m) + "')$>");
/* 211 */     if (url != null)
/*     */     {
/* 213 */       binder.putLocal("alertUrl", url);
/*     */     }
/*     */     else
/*     */     {
/* 217 */       binder.removeLocal("alertUrl");
/*     */     }
/* 219 */     AlertUtils.setAlert(binder);
/*     */   }
/*     */ 
/*     */   public static DataBinder getAlertBinder()
/*     */   {
/* 224 */     if (m_binder == null)
/*     */     {
/* 226 */       m_binder = new DataBinder();
/* 227 */       DataResultSet drset = new DataResultSet(AlertUtils.TRIGGER_RSET_COLS);
/* 228 */       String[] trigger1 = { "role", "admin" };
/* 229 */       drset.addRowWithList(StringUtils.convertToList(trigger1));
/* 230 */       m_binder.addResultSet("AlertTriggers", drset);
/* 231 */       m_binder.putLocal("flags", "2");
/*     */     }
/*     */ 
/* 234 */     return m_binder;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 239 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93090 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ServerConfigurationAnalyzer
 * JD-Core Version:    0.5.4
 */