/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MSDEInstaller
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
/*  40 */     this.m_installer = installer;
/*  41 */     this.m_utils = this.m_installer.m_utils;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  48 */     init(installer);
/*  49 */     int completed = 0;
/*  50 */     int totalWork = 5;
/*     */ 
/*  52 */     String databaseType = this.m_installer.getInstallValue("DatabaseType", null);
/*  53 */     if (databaseType == null)
/*     */     {
/*  55 */       String msg = LocaleUtils.encodeMessage("csJdbcDatabaseTypeUndetermined", null);
/*     */ 
/*  57 */       this.m_installer.m_installLog.error(msg);
/*  58 */       return 1;
/*     */     }
/*  60 */     if (!databaseType.equals("msde"))
/*     */     {
/*  63 */       return 0;
/*     */     }
/*     */ 
/*  66 */     int rc = 0;
/*  67 */     if (!name.equals("msde-copy"))
/*     */     {
/*  71 */       if (name.equals("msde-check"))
/*     */       {
/*  73 */         this.m_installer.m_installerConfig.put("InstallMSDE", "true");
/*  74 */         this.m_installer.m_installerConfig.put("CreateDatabase", "true");
/*  75 */         this.m_installer.m_installerConfig.put("JdbcUser", "sa");
/*  76 */         String instanceName = this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*  77 */         int installStatus = checkMSDEStatus(instanceName);
/*  78 */         if (installStatus != 0)
/*     */         {
/*  80 */           this.m_installer.m_installerConfig.put("CopyMSDE", "true");
/*     */         }
/*     */       }
/*  83 */       else if ((name.equals("msde-install")) || (name.equals("msde-remove")))
/*     */       {
/*  86 */         boolean isRemove = false;
/*  87 */         if ((name.equals("msde-remove")) || (this.m_installer.getInstallBool("RemoveMSDE", false)))
/*     */         {
/*  90 */           isRemove = true;
/*     */         }
/*     */ 
/*  93 */         String instanceName = this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*  94 */         int installStatus = checkMSDEStatus(instanceName);
/*  95 */         boolean runMSDEInstall = false;
/*     */ 
/*  97 */         switch (installStatus)
/*     */         {
/*     */         case 1:
/* 100 */           if (isRemove)
/*     */           {
/* 102 */             String msg = LocaleUtils.encodeMessage("csInstallerMSDENotInstalled", null);
/*     */ 
/* 104 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToRemoveMSDE", msg);
/*     */ 
/* 106 */             this.m_installer.m_installLog.notice(msg);
/* 107 */             return 0;
/*     */           }
/* 109 */           runMSDEInstall = true;
/* 110 */           break;
/*     */         case 0:
/* 112 */           if (isRemove)
/*     */           {
/* 114 */             runMSDEInstall = true;
/*     */           }
/*     */         }
/*     */ 
/* 118 */         if (((isRemove) && (installStatus == 1)) || (
/* 122 */           (!isRemove) && (installStatus == 0)));
/* 127 */         String saPassword = this.m_installer.getInstallValue("JdbcPassword", "idc");
/* 128 */         String msdeInstallDir = this.m_installer.getInstallValue("MSDEInstallDir", null);
/* 129 */         String msdeDataDir = this.m_installer.getInstallValue("MSDEDataDir", null);
/* 130 */         if (msdeInstallDir == null)
/*     */         {
/* 132 */           msdeInstallDir = this.m_installer.computeDestinationEx("../msde", false);
/*     */         }
/* 134 */         if (msdeDataDir == null)
/*     */         {
/* 136 */           msdeDataDir = this.m_installer.computeDestinationEx("../msde", false);
/*     */         }
/* 138 */         msdeInstallDir = FileUtils.windowsSlashes(msdeInstallDir);
/* 139 */         msdeDataDir = FileUtils.windowsSlashes(msdeDataDir);
/*     */ 
/* 141 */         if (runMSDEInstall)
/*     */         {
/* 143 */           Vector results = new IdcVector();
/* 144 */           String msdeInstallConfig = FileUtils.windowsSlashes(this.m_installer.computeDestinationEx("install/msde/stellentmsde.msi", false));
/*     */ 
/* 147 */           String installFlag = (isRemove) ? "/x" : "/i";
/*     */ 
/* 151 */           if (isRemove)
/*     */           {
/* 153 */             String[] cmd = { "net", "stop", "MSSQL$" + instanceName };
/*     */ 
/* 159 */             String msg = LocaleResources.getString("csInstallerStoppingMSDE", null);
/* 160 */             this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 162 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToStopMSDE", null);
/*     */ 
/* 164 */             this.m_installer.runCommand(cmd, results, msg, true, true);
/* 165 */             SystemUtils.sleep(1000L);
/*     */           }
/*     */ 
/* 168 */           String setupPath = this.m_installer.computeDestinationEx("install/msde/setup.exe", false);
/*     */ 
/* 170 */           setupPath = setupPath.replace('/', '\\');
/* 171 */           String[] cmd = { setupPath, "/qb", installFlag, msdeInstallConfig, "INSTANCENAME=" + instanceName, "SECURITYMODE=SQL", "SAPWD=" + saPassword, "TARGETDIR=" + msdeInstallDir + "\\", "DATADIR=" + msdeDataDir + "\\" };
/*     */ 
/* 184 */           results = new IdcVector();
/*     */           String msg;
/*     */           String msg;
/* 185 */           if (isRemove)
/*     */           {
/* 187 */             msg = LocaleResources.getString("csInstallerRemovingMSDE", null);
/*     */           }
/*     */           else
/*     */           {
/* 191 */             msg = LocaleResources.getString("csInstallerInstallingMSDE", null);
/*     */           }
/* 193 */           this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 195 */           if (isRemove)
/*     */           {
/* 197 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToRemoveMSDE", null);
/*     */ 
/* 199 */             Report.trace("install", "removing MSDE", null);
/*     */           }
/*     */           else
/*     */           {
/* 203 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToInstallMSDE", null);
/*     */ 
/* 205 */             Report.trace("install", "installing MSDE", null);
/*     */           }
/* 207 */           rc = this.m_installer.runCommand(cmd, results, msg, true, true);
/* 208 */           SystemUtils.sleep(1000L);
/* 209 */           if (!isRemove)
/*     */           {
/* 211 */             cmd = new String[] { "net", "start", "MSSQL$" + instanceName };
/*     */ 
/* 217 */             msg = LocaleResources.getString("csInstallerStartingMSDE", null);
/* 218 */             this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 220 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToStartMSDE", null);
/*     */ 
/* 222 */             results = new IdcVector();
/* 223 */             this.m_installer.runCommand(cmd, results, msg, true, true);
/* 224 */             SystemUtils.sleep(1000L);
/*     */ 
/* 226 */             String osqlPath = findOSQLPath();
/* 227 */             String databaseServer = "(local)\\" + this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*     */ 
/* 231 */             cmd = new String[] { osqlPath, "-S", databaseServer, "-Q", "sp_password NULL, '" + saPassword + "', 'sa'", "-U", "sa", "-P", "" };
/*     */ 
/* 243 */             results = new IdcVector();
/* 244 */             msg = LocaleResources.getString("csInstallerSettingPassword", null, "sa");
/*     */ 
/* 246 */             this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 248 */             msg = LocaleUtils.encodeMessage("csInstallerUnableToSetPassword", null, "sa");
/*     */ 
/* 250 */             rc = this.m_installer.runCommand(cmd, results, msg, true, true);
/* 251 */             SystemUtils.sleep(1000L);
/*     */           }
/*     */         }
/*     */ 
/* 255 */         if (!isRemove)
/*     */         {
/* 258 */           String databaseName = this.m_installer.getInstallValue("DBServerDatabase", "idc");
/* 259 */           String osqlPath = findOSQLPath();
/* 260 */           String databaseServer = "(local)\\" + this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*     */ 
/* 262 */           this.m_installer.m_installerConfig.put("DBServerHost", databaseServer);
/* 263 */           String[] cmd = { osqlPath, "-S", databaseServer, "-Q", "create database " + databaseName + " on (NAME = " + databaseName + "db_dat, " + "  FILENAME = '" + msdeDataDir + "\\" + databaseName + "_dat.mdf')" + " log on (NAME = " + databaseName + "db_log, " + "  FILENAME = '" + msdeDataDir + "\\" + databaseName + "_log.ldf')", "-U", "sa", "-P", saPassword };
/*     */ 
/* 279 */           String msg = LocaleUtils.encodeMessage("csInstallerCreatingDatabase", null, databaseName);
/*     */ 
/* 281 */           msg = LocaleResources.localizeMessage(msg, null);
/* 282 */           this.m_installer.reportProgress(1, msg, completed++, totalWork);
/*     */ 
/* 284 */           msg = LocaleUtils.encodeMessage("csInstallerUnableToCreateDatabase", null, databaseName);
/*     */ 
/* 286 */           Vector results = new IdcVector();
/* 287 */           rc = this.m_installer.runCommand(cmd, results, msg, true, true);
/* 288 */           for (int i = 0; i < results.size(); ++i)
/*     */           {
/* 290 */             String line = (String)results.elementAt(i);
/* 291 */             line = line.toLowerCase();
/* 292 */             if (line.indexOf("create database failed") < 0)
/*     */               continue;
/* 294 */             this.m_installer.m_installLog.error(msg);
/*     */           }
/*     */ 
/* 297 */           SystemUtils.sleep(1000L);
/*     */         }
/*     */       }
/*     */     }
/* 300 */     return rc;
/*     */   }
/*     */ 
/*     */   public int checkMSDEStatus(String instance)
/*     */     throws ServiceException
/*     */   {
/* 413 */     if (this.m_utils == null)
/*     */     {
/* 415 */       String msg = LocaleUtils.encodeMessage("syNativeOsUtilsNotLoaded", null);
/*     */ 
/* 417 */       msg = LocaleUtils.encodeMessage("csInstallerUnableToCheckMSDE", msg);
/*     */ 
/* 419 */       throw new ServiceException(msg);
/*     */     }
/* 421 */     String mssqlKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SQL Server";
/*     */ 
/* 423 */     String myServerKey = mssqlKey + "\\" + instance;
/* 424 */     String versionKey = myServerKey + "\\MSSQLServer\\CurrentVersion\\CurrentVersion";
/* 425 */     String version = this.m_utils.getRegistryValue(versionKey);
/* 426 */     if (version == null)
/*     */     {
/* 428 */       Report.trace("install", "MSDE instance " + instance + " not installed.", null);
/* 429 */       return 1;
/*     */     }
/* 431 */     Report.trace("install", "MSDE instance " + instance + " installed at version " + version, null);
/*     */ 
/* 433 */     return 0;
/*     */   }
/*     */ 
/*     */   public String findOSQLPath()
/*     */   {
/* 438 */     String osqlPath = this.m_installer.m_utils.getRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Microsoft SQL Server\\80\\Tools\\ClientSetup\\SQLPath");
/*     */ 
/* 440 */     if (osqlPath == null)
/*     */     {
/* 442 */       Report.trace("install", "couldn't find SQL tools, using default location.", null);
/*     */ 
/* 444 */       osqlPath = "C:\\Program Files\\Microsoft SQL Server\\80\\Tools\\Binn\\osql.exe";
/*     */     }
/*     */     else
/*     */     {
/* 448 */       osqlPath = osqlPath + "\\Binn\\osql.exe";
/*     */     }
/* 450 */     return osqlPath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 455 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.MSDEInstaller
 * JD-Core Version:    0.5.4
 */