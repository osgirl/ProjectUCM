/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class OracleExpressInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public static final int INSTALLED = 0;
/*     */   public static final int NOT_INSTALLED = 1;
/*     */   protected SysInstaller m_installer;
/*     */   protected NativeOsUtils m_utils;
/*     */   protected Vector m_sections;
/*     */   protected Hashtable m_sectionMap;
/*     */ 
/*     */   public void init(SysInstaller installer)
/*     */   {
/*  42 */     this.m_installer = installer;
/*  43 */     this.m_utils = this.m_installer.m_utils;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  50 */     init(installer);
/*  51 */     int completed = 0;
/*  52 */     int totalWork = 5;
/*     */ 
/*  54 */     String databaseType = this.m_installer.getInstallValue("DatabaseType", null);
/*  55 */     if (databaseType == null)
/*     */     {
/*  57 */       String msg = LocaleUtils.encodeMessage("csJdbcDatabaseTypeUndetermined", null);
/*     */ 
/*  59 */       this.m_installer.m_installLog.error(msg);
/*  60 */       return 1;
/*     */     }
/*  62 */     if (!this.m_installer.getInstallBool("InstallOracleExpress", false))
/*     */     {
/*  65 */       return 0;
/*     */     }
/*     */ 
/*  68 */     int rc = 0;
/*  69 */     if (!name.equals("oracleexpress-check"))
/*     */     {
/*  73 */       if ((name.equals("oracleexpress-install")) || (name.equals("oracleexpress-remove")))
/*     */       {
/*  76 */         DataBinder binder = new DataBinder();
/*  77 */         Properties props = new Properties(this.m_installer.m_binder.getLocalData());
/*  78 */         binder.setLocalData(props);
/*  79 */         PageMerger merger = new PageMerger(binder, this.m_installer.m_context);
/*     */ 
/*  81 */         boolean isRemove = false;
/*  82 */         if ((name.equals("oracleexpress-remove")) || (this.m_installer.getInstallBool("RemoveOracleExpress", false)))
/*     */         {
/*  85 */           isRemove = true;
/*     */         }
/*     */ 
/*  88 */         int installStatus = checkOracleExpressStatus();
/*  89 */         boolean runOracleInstall = false;
/*     */ 
/*  91 */         switch (installStatus)
/*     */         {
/*     */         case 1:
/*  94 */           if (isRemove)
/*     */           {
/*  96 */             String msg = LocaleUtils.encodeMessage("csInstallerOracleExpressNotInstalled", null);
/*     */ 
/*  98 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToRemoveOracleExpress", msg);
/*     */ 
/* 100 */             this.m_installer.m_installLog.notice(msg);
/* 101 */             return 0;
/*     */           }
/* 103 */           runOracleInstall = true;
/* 104 */           break;
/*     */         case 0:
/* 106 */           if (isRemove)
/*     */           {
/* 108 */             runOracleInstall = true;
/*     */           }
/*     */         }
/*     */ 
/* 112 */         if (((isRemove) && (installStatus == 1)) || (
/* 116 */           (!isRemove) && (installStatus == 0)));
/* 121 */         String systemPassword = this.m_installer.getInstallValue("JdbcPassword", "idc");
/* 122 */         binder.putLocal("OracleSystemPassword", systemPassword);
/* 123 */         String oracleInstallDir = this.m_installer.getInstallValue("OracleInstallDir", null);
/* 124 */         if (oracleInstallDir == null)
/*     */         {
/* 126 */           oracleInstallDir = this.m_installer.computeDestinationEx("../oraclexp", false);
/*     */         }
/* 128 */         oracleInstallDir = FileUtils.windowsSlashes(oracleInstallDir);
/* 129 */         binder.putLocal("OracleInstallDir", oracleInstallDir);
/*     */ 
/* 131 */         if (runOracleInstall)
/*     */         {
/* 133 */           Vector results = new IdcVector();
/*     */           String textString;
/*     */           String oracleInstallConfig;
/* 136 */           if (isRemove)
/*     */           {
/* 138 */             String oracleInstallConfig = this.m_installer.computeDestinationEx("install/oraclexpremove.iss", false);
/*     */ 
/* 140 */             textString = "csOracleExpressRemoveConfig";
/*     */           }
/*     */           else
/*     */           {
/* 144 */             oracleInstallConfig = this.m_installer.computeDestinationEx("install/oraclexpremove.iss", false);
/*     */ 
/* 146 */             textString = "csOracleExpressRemoveConfig";
/*     */           }
/* 148 */           String textString = LocaleResources.getString(textString, null);
/* 149 */           if (textString == null)
/*     */           {
/* 151 */             throw new AssertionError(textString + " is undefined");
/*     */           }
/* 153 */           String resultString = this.m_installer.evaluateScript(merger, textString);
/* 154 */           String msg = LocaleUtils.encodeMessage("syUnableToWriteFile", null, oracleInstallConfig);
/*     */ 
/* 156 */           FileUtils.writeFile(resultString, new File(oracleInstallConfig), null, 1, msg);
/*     */ 
/* 160 */           String setupPath = this.m_installer.computeDestinationEx("${MediaDirectory}/packages/${platform}/OracleXEUniv.exe", false);
/*     */ 
/* 162 */           String logFile = this.m_installer.computeDestinationEx("install/oracle.log", false);
/*     */ 
/* 164 */           String[] cmd = { fixSlashes(setupPath), "/s", "/f1", fixSlashes(oracleInstallConfig), "/f2", fixSlashes(logFile) };
/*     */ 
/* 174 */           results = new IdcVector();
/* 175 */           if (isRemove)
/*     */           {
/* 177 */             msg = LocaleResources.getString("csInstallerRemovingOracle", null);
/*     */           }
/*     */           else
/*     */           {
/* 181 */             msg = LocaleResources.getString("csInstallerInstallingOracle", null);
/*     */           }
/* 183 */           this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 185 */           if (isRemove)
/*     */           {
/* 187 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToRemoveOracle", null);
/*     */ 
/* 189 */             Report.trace("install", "removing Oracle Express", null);
/*     */           }
/*     */           else
/*     */           {
/* 193 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToInstallOracle", null);
/*     */ 
/* 195 */             Report.trace("install", "installing Oracle", null);
/*     */           }
/* 197 */           rc = this.m_installer.runCommand(cmd, results, msg, true, true);
/* 198 */           SystemUtils.sleep(1000L);
/* 199 */           if (!isRemove)
/*     */           {
/* 201 */             this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 253 */     return rc;
/*     */   }
/*     */ 
/*     */   public String fixSlashes(String path)
/*     */   {
/* 258 */     return FileUtils.windowsSlashes(path);
/*     */   }
/*     */ 
/*     */   public int checkOracleExpressStatus()
/*     */     throws ServiceException
/*     */   {
/* 323 */     return 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 328 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.OracleExpressInstaller
 * JD-Core Version:    0.5.4
 */