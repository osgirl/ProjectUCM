/*     */ package intradoc.server.filter;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WebFilterConfigUtils
/*     */ {
/*  32 */   static final String[] SERVER_CONFIG_VALS = { "DefaultAuth", "CGI_DEBUG", "DisableGzipCompression" };
/*     */ 
/*  35 */   static final String[][] DEFAULT_CONFIG_VALS = { { "DefaultAuth", "Basic" }, { "DefaultNetworkAccounts", "#none" }, { "AccountMapPrefix", "@" }, { "NetworkAdminGroup", "Domain Admins" } };
/*     */ 
/*     */   public static DataBinder readFilterConfigFromFile()
/*     */     throws ServiceException
/*     */   {
/*  43 */     String fileName = "filter.hda";
/*  44 */     String configDir = LegacyDirectoryLocator.getUserCacheDir() + "config/";
/*     */ 
/*  46 */     DataBinder binder = null;
/*  47 */     FileUtils.checkOrCreateDirectory(configDir, 10);
/*  48 */     FileUtils.reserveDirectory(configDir);
/*     */     try
/*     */     {
/*  51 */       binder = new DataBinder(true);
/*  52 */       binder.putLocal("IsStripPasswords", "1");
/*  53 */       binder.putLocal("PasswordScope", "system");
/*  54 */       ResourceUtils.serializeDataBinder(configDir, fileName, binder, false, false);
/*     */     }
/*     */     finally
/*     */     {
/*  58 */       FileUtils.releaseDirectory(configDir);
/*     */     }
/*  60 */     return binder;
/*     */   }
/*     */ 
/*     */   public static boolean checkForHomePageUpdate(DataBinder binder)
/*     */   {
/*  65 */     boolean hasChanged = false;
/*  66 */     boolean curNtlmEnabled = StringUtils.convertToBool(binder.getLocal("NtlmSecurityEnabled"), false);
/*     */ 
/*  68 */     if (curNtlmEnabled != SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false))
/*     */     {
/*  70 */       SharedObjects.putEnvironmentValue("RebuildHomePage", "true");
/*  71 */       hasChanged = true;
/*     */     }
/*  73 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public static void updateWebFilterConfig(DataBinder binder)
/*     */   {
/*  78 */     DataResultSet wfcSet = SharedObjects.getTable("WebFilterConfiguration");
/*  79 */     for (Enumeration e = binder.m_localData.keys(); e.hasMoreElements(); )
/*     */     {
/*  81 */       String key = (String)e.nextElement();
/*  82 */       String value = binder.getLocal(key);
/*     */ 
/*  84 */       if (value == null) {
/*     */         continue;
/*     */       }
/*     */ 
/*  88 */       Vector v = wfcSet.findRow(0, key);
/*  89 */       if (v == null)
/*     */       {
/*  91 */         v = wfcSet.createEmptyRow();
/*  92 */         wfcSet.addRow(v);
/*     */       }
/*  94 */       v.setElementAt(key, 0);
/*  95 */       v.setElementAt(value, 1);
/*  96 */       v.setElementAt("string", 2);
/*  97 */       v.setElementAt("Default", 3);
/*     */     }
/*  99 */     SharedObjects.putTable("WebFilterConfiguration", wfcSet);
/*     */ 
/* 102 */     if ((!SharedObjects.getEnvValueAsBoolean("UseAdsi", false)) || (!SharedObjects.getEnvValueAsBoolean("NtlmSecurityEnabled", false))) {
/*     */       return;
/*     */     }
/* 105 */     String fileName = SharedObjects.getEnvironmentValue("AdsLoginPluginFileName");
/* 106 */     if ((fileName == null) || (fileName.length() == 0))
/*     */     {
/* 108 */       fileName = "AdsLoginPlugin";
/*     */     }
/* 110 */     addWebServerFilter("AdsLoginPlugin", fileName);
/*     */   }
/*     */ 
/*     */   public static boolean addWebServerFilter(String symbol)
/*     */   {
/* 116 */     return addWebServerFilter(symbol, symbol);
/*     */   }
/*     */ 
/*     */   public static boolean addWebServerFilter(String symbol, String fileName)
/*     */   {
/* 122 */     DataResultSet iapTable = SharedObjects.getTable("IdcAuthPlugins");
/* 123 */     if (iapTable == null)
/*     */     {
/* 125 */       String[] fields = { "iapExportedSymbolName", "iapFileNameRoot" };
/* 126 */       iapTable = new DataResultSet(fields);
/*     */     }
/*     */ 
/* 129 */     Vector v = iapTable.findRow(0, symbol);
/* 130 */     if (v == null)
/*     */     {
/* 132 */       v = iapTable.createEmptyRow();
/* 133 */       iapTable.addRow(v);
/*     */     }
/* 135 */     v.setElementAt(symbol, 0);
/* 136 */     v.setElementAt(fileName, 1);
/*     */ 
/* 138 */     SharedObjects.putTable("IdcAuthPlugins", iapTable);
/* 139 */     return true;
/*     */   }
/*     */ 
/*     */   public static void writeFilterConfigToFile(DataBinder binder) throws ServiceException
/*     */   {
/* 144 */     String configDir = LegacyDirectoryLocator.getUserCacheDir() + "config/";
/*     */     try
/*     */     {
/* 147 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(configDir, 10, true);
/* 148 */       FileUtils.reserveDirectory(configDir);
/* 149 */       updateWebFilterConfig(binder);
/* 150 */       ResourceUtils.serializeDataBinder(configDir, "filter.hda", binder, true, false);
/* 151 */       SubjectManager.notifyChanged("users");
/*     */     }
/*     */     finally
/*     */     {
/* 155 */       FileUtils.releaseDirectory(configDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateServerEnvironment(DataBinder binder)
/*     */   {
/* 161 */     for (int i = 0; i < SERVER_CONFIG_VALS.length; ++i)
/*     */     {
/* 168 */       String newVal = binder.getLocal(SERVER_CONFIG_VALS[i]);
/* 169 */       if (newVal == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 174 */       boolean isOverride = SharedObjects.getEnvValueAsBoolean(SERVER_CONFIG_VALS[i] + ":overrideFilterAdmin", false);
/*     */ 
/* 176 */       if (isOverride)
/*     */         continue;
/* 178 */       SharedObjects.putEnvironmentValue(SERVER_CONFIG_VALS[i], newVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 185 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91020 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.filter.WebFilterConfigUtils
 * JD-Core Version:    0.5.4
 */