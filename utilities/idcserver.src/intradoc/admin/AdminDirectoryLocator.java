/*     */ package intradoc.admin;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.filestore.config.ConfigFileUtilities;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class AdminDirectoryLocator
/*     */ {
/*  35 */   public static final String[][] m_adminServerDirMap = { { "AdminDir", "admin/" }, { "ConfigDir", "config/" }, { "AdminDataDir", "admin/data/" }, { "BinDir", "admin/bin/" }, { "ProviderDir", "data/providers/" }, { "SubjectsDir", "admin/data/subjects" }, { "WeblayoutDir", "weblayout/$DefaultSubWebLayoutDir" }, { "WebLogDir", "$WeblayoutDir/groups/secure/logs/" }, { "IdcResourcesDir", "$IdcHomeDir/resources/" }, { "IdcNativeDir", "$IdcHomeDir/native/" }, { "ServerDataDir", "data/" } };
/*     */ 
/*  53 */   protected static String[][] DEFAULTS = { { "BinDir", "bin/" }, { "ConfigDir", "config/" }, { "DataDir", "data/" }, { "ScriptDir", "script/" } };
/*     */ 
/*  61 */   protected static Properties m_directories = null;
/*     */ 
/*  64 */   static final String[][] ADMIN_KEYPATH_PAIRS = { { "AdminDir", "$IntradocDir/admin/" }, { "ConfigDir", "$AdminDir/config/" }, { "AdminDataDir", "$AdminDir/data/" }, { "AdminServersDir", "$AdminDataDir/servers/" } };
/*     */ 
/*  73 */   static final String[][] ADMIN_SIMPLE_KEYPATH_PAIRS = { { "ConfigDir", "$IntradocDir/config/" } };
/*     */ 
/*     */   public static void init()
/*     */   {
/*  86 */     intradoc.server.LegacyDirectoryLocator.m_keyDirMap = m_adminServerDirMap;
/*     */ 
/*  90 */     m_directories = new Properties();
/*  91 */     for (int i = 0; i < DEFAULTS.length; ++i)
/*     */     {
/*  93 */       m_directories.put(DEFAULTS[i][0], DEFAULTS[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getConfigDir(DataBinder serverData)
/*     */     throws DataException
/*     */   {
/* 103 */     String server = serverData.getLocal("IDC_Id");
/* 104 */     String cfsStr = "";
/*     */     try
/*     */     {
/* 107 */       cfsStr = ConfigFileUtilities.getComputedPathFor(server, "$ConfigDir");
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 111 */       Report.trace(null, "$ConfigDir missing", e);
/*     */     }
/* 113 */     String idcDir = serverData.get("IntradocDir");
/* 114 */     String retVal = serverData.getLocal("ConfigDir");
/* 115 */     if (retVal == null)
/* 116 */       retVal = idcDir + m_directories.get("ConfigDir");
/* 117 */     if (!cfsStr.equals(retVal))
/*     */     {
/* 119 */       Report.trace(null, "ConfigDir mismatch: " + retVal + " ==> " + cfsStr, null);
/*     */     }
/* 121 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static String getBinDir(DataBinder serverData)
/*     */     throws DataException
/*     */   {
/* 130 */     String server = serverData.getLocal("IDC_Id");
/* 131 */     String cfsStr = "";
/*     */     try
/*     */     {
/* 134 */       cfsStr = ConfigFileUtilities.getComputedPathFor(server, "$BinDir");
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 138 */       Report.trace(null, "$BinDir missing", e);
/*     */     }
/* 140 */     String idcDir = serverData.get("IntradocDir");
/* 141 */     String retVal = serverData.getLocal("BinDir");
/* 142 */     if (retVal == null)
/* 143 */       retVal = idcDir + m_directories.get("BinDir");
/* 144 */     if (!cfsStr.equals(retVal))
/*     */     {
/* 146 */       Report.trace(null, "BinDir mismatch: " + retVal + " ==> " + cfsStr, null);
/*     */     }
/* 148 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static String getAppDataDirectory(DataBinder serverData) throws DataException
/*     */   {
/* 153 */     String server = serverData.getLocal("IDC_Id");
/* 154 */     String cfsStr = "";
/*     */     try
/*     */     {
/* 157 */       cfsStr = ConfigFileUtilities.getComputedPathFor(server, "$DataDir");
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 161 */       Report.trace(null, "$DataDir missing", e);
/*     */     }
/* 163 */     String idcDir = serverData.get("IntradocDir");
/* 164 */     String retVal = serverData.getLocal("DataDir");
/* 165 */     if (retVal == null)
/* 166 */       retVal = idcDir + m_directories.get("DataDir");
/* 167 */     if (!cfsStr.equals(retVal))
/*     */     {
/* 169 */       Report.trace(null, "DataDir mismatch: " + retVal + " ==> " + cfsStr, null);
/*     */     }
/* 171 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 176 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93152 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminDirectoryLocator
 * JD-Core Version:    0.5.4
 */