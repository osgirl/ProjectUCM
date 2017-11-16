/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WindowsShortcuts
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected NativeOsUtils m_utils;
/*     */   protected boolean m_isRefinery;
/*     */   protected boolean m_isRemove;
/*     */   protected ExecutionContext m_serverContext;
/*     */ 
/*     */   public void init(SysInstaller installer)
/*     */   {
/*  46 */     this.m_installer = installer;
/*  47 */     this.m_utils = installer.m_utils;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  54 */     init(installer);
/*  55 */     this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*  56 */     this.m_isRemove = this.m_installer.getInstallBool("IsUninstall", false);
/*     */ 
/*  59 */     String locale = this.m_installer.getConfigValue("SystemLocale");
/*  60 */     if (locale != null)
/*     */     {
/*  62 */       IdcLocale idcLocale = LocaleResources.getLocale(locale);
/*  63 */       if (idcLocale == null)
/*     */       {
/*  65 */         idcLocale = LocaleResources.getSystemLocale();
/*  66 */         String msg = LocaleUtils.encodeMessage("csLocaleNotFoundUsingSpecific", null, locale, idcLocale.m_name);
/*     */ 
/*  69 */         msg = LocaleUtils.encodeMessage("csUnableToLocalizeShortcuts", msg);
/*  70 */         this.m_installer.m_installLog.warning(msg);
/*     */       }
/*     */       else
/*     */       {
/*  74 */         this.m_serverContext = new ExecutionContextAdaptor();
/*  75 */         this.m_serverContext.setCachedObject("UserLocale", idcLocale);
/*     */       }
/*     */     }
/*     */ 
/*  79 */     if (this.m_isRemove)
/*     */     {
/*  81 */       legacyCleanup();
/*  82 */       removeShortcuts();
/*     */     }
/*     */     else
/*     */     {
/*  86 */       if (installer.m_isUpdate)
/*     */       {
/*  88 */         legacyCleanup();
/*  89 */         removeShortcuts();
/*     */       }
/*  91 */       createShortcuts();
/*     */     }
/*     */ 
/*  94 */     return 0;
/*     */   }
/*     */ 
/*     */   public void createShortcuts()
/*     */     throws ServiceException
/*     */   {
/* 100 */     DataResultSet drset = (DataResultSet)this.m_installer.m_binder.getResultSet("Shortcuts");
/* 101 */     if (drset == null)
/*     */     {
/* 103 */       Report.trace("install", "Shortcuts table not found; not creating any shortcuts.", null);
/*     */     }
/*     */     try
/*     */     {
/* 107 */       String[][] shortcuts = ResultSetUtils.createStringTable(drset, null);
/* 108 */       processShortcuts(shortcuts);
/*     */     }
/*     */     catch (DataException d)
/*     */     {
/* 112 */       throw new ServiceException(d);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void processShortcuts(String[][] shortcuts)
/*     */     throws ServiceException
/*     */   {
/* 120 */     String shortcutBase = LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerShortcutBase", null), this.m_serverContext);
/*     */ 
/* 122 */     shortcutBase = shortcutBase + "/" + this.m_installer.getConfigValue("IDC_Name");
/*     */ 
/* 125 */     for (int i = 0; i < shortcuts.length; ++i)
/*     */     {
/* 127 */       String[] shortcut = shortcuts[i];
/*     */       String[] command;
/*     */       String[] command;
/* 128 */       if ((shortcut[2] == null) || (shortcut[2].length() == 0))
/*     */       {
/* 130 */         command = new String[] { this.m_installer.computeDestinationEx(shortcut[1], false) };
/*     */       }
/*     */       else
/*     */       {
/* 137 */         command = new String[] { this.m_installer.computeDestinationEx(shortcut[1], false), shortcut[2] };
/*     */       }
/*     */ 
/* 143 */       boolean hide = StringUtils.convertToBool(shortcut[4], false);
/* 144 */       String shortcutPath = shortcutBase;
/* 145 */       if ((shortcut[3] != null) && (shortcut[3].length() > 0))
/*     */       {
/* 147 */         String folder = LocaleResources.localizeMessage(LocaleUtils.encodeMessage(shortcut[3], null), this.m_serverContext);
/*     */ 
/* 149 */         shortcutPath = shortcutPath + "/" + folder;
/*     */       }
/* 151 */       shortcutPath = shortcutPath + "/" + LocaleResources.localizeMessage(LocaleUtils.encodeMessage(shortcut[5], null), this.m_serverContext);
/*     */ 
/* 153 */       String imagePath = this.m_installer.computeDestination(shortcut[0]);
/* 154 */       createShortcut(shortcutPath, command, imagePath, hide);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int createShortcut(String shortcutPath, String[] targetCommand, String iconPath, boolean hide)
/*     */     throws ServiceException
/*     */   {
/* 162 */     String target = FileUtils.windowsSlashes(targetCommand[0]);
/* 163 */     IdcStringBuilder targetArguments = new IdcStringBuilder();
/* 164 */     for (int i = 1; i < targetCommand.length; ++i)
/*     */     {
/* 166 */       if (i > 1)
/*     */       {
/* 168 */         targetArguments.append(" ");
/*     */       }
/* 170 */       targetArguments.append(targetCommand[i]);
/*     */     }
/* 172 */     String arguments = targetArguments.toString();
/* 173 */     if ((arguments == null) || (arguments.length() == 0))
/*     */     {
/* 175 */       arguments = "null";
/*     */     }
/*     */ 
/* 178 */     String[] commandLine = { "WScript.exe", "/b", FileUtils.windowsSlashes(this.m_installer.computeDestination("${IdcHomeDir}/native/win32/bin/createShortcut.vbs")), FileUtils.windowsSlashes(shortcutPath), FileUtils.windowsSlashes(target), arguments, FileUtils.windowsSlashes(iconPath), (hide) ? "true" : "false" };
/*     */ 
/* 191 */     int rc = this.m_installer.runCommand(commandLine, null, null, true, true);
/* 192 */     return rc;
/*     */   }
/*     */ 
/*     */   public void legacyCleanup()
/*     */   {
/* 200 */     String programsDir = null;
/*     */     try
/*     */     {
/* 203 */       programsDir = getAllUsersProgramsDir();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 207 */       Report.trace("install", null, e);
/*     */     }
/*     */ 
/* 210 */     if (programsDir == null)
/*     */     {
/* 212 */       Report.trace("install", "legacyCleanup() can't find the programs directory.", null);
/*     */ 
/* 214 */       return;
/*     */     }
/*     */ 
/* 217 */     String[] dirs = { "Xpedio Content Server" };
/*     */ 
/* 219 */     for (int i = 0; i < dirs.length; ++i)
/*     */     {
/* 221 */       String serverDir = programsDir + "\\" + dirs[i];
/*     */ 
/* 223 */       String targetName = null;
/*     */       try
/*     */       {
/* 226 */         targetName = serverDir + "\\" + this.m_installer.getConfigValue("IDC_Name");
/*     */ 
/* 228 */         Report.trace("install", "checking '" + targetName + "'", null);
/* 229 */         if (FileUtils.checkFile(serverDir, false, true) == 0)
/*     */         {
/* 232 */           this.m_installer.deleteRecursive(targetName);
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 237 */         Report.trace("install", "legacyCleanup() unable to remove '" + serverDir + "'", e);
/*     */       }
/*     */ 
/* 241 */       removeDir(serverDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int removeShortcuts()
/*     */     throws ServiceException
/*     */   {
/* 248 */     String programsDir = getAllUsersProgramsDir();
/* 249 */     if (programsDir == null)
/*     */     {
/* 251 */       String msg = LocaleUtils.encodeMessage("csUnableToRemoveShortcut", null, "<unknown>/" + this.m_installer.getConfigValue("IDC_Name"));
/*     */ 
/* 254 */       this.m_installer.m_installLog.warning(msg);
/* 255 */       return 1;
/*     */     }
/* 257 */     String shortcutBase = LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerShortcutBase", null), this.m_serverContext);
/*     */ 
/* 259 */     String list = LocaleUtils.encodeMessage("csInstallerShortcutLegacyBaseList", null);
/* 260 */     list = LocaleResources.localizeMessage(list, this.m_serverContext);
/* 261 */     List l = StringUtils.makeListFromSequenceSimple(list);
/* 262 */     l.add(shortcutBase);
/*     */ 
/* 264 */     for (int i = 0; i < l.size(); ++i)
/*     */     {
/* 266 */       shortcutBase = (String)l.get(i);
/* 267 */       String targetDir = programsDir + "/" + shortcutBase + "/" + this.m_installer.getConfigValue("IDC_Name");
/*     */       try
/*     */       {
/* 271 */         this.m_installer.deleteRecursive(targetDir);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 275 */         String msg = LocaleUtils.encodeMessage("csUnableToRemoveShortcut", null, targetDir);
/*     */ 
/* 277 */         this.m_installer.m_installLog.warning(msg);
/* 278 */         return 1;
/*     */       }
/*     */ 
/* 283 */       removeDir(programsDir);
/*     */     }
/*     */ 
/* 286 */     return 0;
/*     */   }
/*     */ 
/*     */   protected void removeDir(String dir)
/*     */   {
/* 291 */     File f = new File(dir);
/* 292 */     if (f.delete())
/*     */     {
/* 294 */       Report.trace("install", "removed '" + dir + "' shortcut directory", null);
/*     */     }
/*     */     else
/*     */     {
/* 299 */       Report.trace("install", "couldn't remove '" + dir + "' shortcut directory", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getAllUsersProgramsDir()
/*     */     throws ServiceException
/*     */   {
/* 306 */     Vector results = new IdcVector();
/* 307 */     String programsDir = null;
/* 308 */     String[] cmd = { "cscript.exe", this.m_installer.computeDestinationEx("${IdcHomeDir}/native/${platform}/bin/queryShortcutPath.vbs", false) };
/*     */ 
/* 314 */     int rc = -1;
/*     */     try
/*     */     {
/* 317 */       rc = this.m_installer.runCriticalCommand(cmd, results, true);
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 321 */       Report.trace("install", "unable to get AllUsersProgramDir", ignore);
/*     */     }
/*     */ 
/* 325 */     if (rc == 0)
/*     */     {
/* 327 */       for (int i = 0; i < results.size(); ++i)
/*     */       {
/* 329 */         String line = (String)results.elementAt(i);
/* 330 */         if (line.indexOf("ShortcutPath=") != 0)
/*     */           continue;
/* 332 */         programsDir = line.substring("ShortcutPath=".length()).trim();
/*     */ 
/* 334 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 338 */     if ((programsDir == null) && (this.m_utils != null))
/*     */     {
/* 340 */       programsDir = this.m_utils.getEnv("ALLUSERSPROFILE");
/*     */     }
/*     */ 
/* 343 */     return programsDir;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 348 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80698 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.WindowsShortcuts
 * JD-Core Version:    0.5.4
 */