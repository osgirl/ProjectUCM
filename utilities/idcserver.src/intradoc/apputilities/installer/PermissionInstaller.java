/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.PosixStructStat;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PermissionInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public DataResultSet m_executables;
/*     */   public DataResultSet m_permissions;
/*     */   public DataResultSet m_rules;
/*     */   public boolean m_isUnix;
/*     */   public String m_platform;
/*     */   public String m_pathSep;
/*     */   public SysInstaller m_installer;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  47 */     this.m_installer = installer;
/*     */ 
/*  53 */     this.m_executables = ((DataResultSet)installer.m_binder.getResultSet("PermissionExecutables"));
/*  54 */     this.m_permissions = ((DataResultSet)installer.m_binder.getResultSet("PermissionConfig"));
/*  55 */     this.m_rules = ((DataResultSet)installer.m_binder.getResultSet("PermissionInfo"));
/*     */ 
/*  57 */     if (this.m_executables == null)
/*     */     {
/*  59 */       throw new ServiceException("!csInstallerPermExecTableMissing");
/*     */     }
/*  61 */     if (this.m_permissions == null)
/*     */     {
/*  63 */       throw new ServiceException("!csInstallerPermConfigTableMissing");
/*     */     }
/*  65 */     if (this.m_rules == null)
/*     */     {
/*  67 */       throw new ServiceException("!csInstallerPermInfoTableMissing");
/*     */     }
/*     */ 
/*  71 */     this.m_isUnix = true;
/*  72 */     this.m_pathSep = ":";
/*  73 */     this.m_platform = installer.getInstallValue("Platform", null);
/*  74 */     if (this.m_platform == null)
/*     */     {
/*  77 */       throw new ServiceException("Platform not set.");
/*     */     }
/*     */ 
/*  81 */     installer.m_installerConfig.put(":", this.m_pathSep);
/*     */     boolean isPreinstall;
/*     */     boolean isPreinstall;
/*  84 */     if (name.indexOf("preinstall") > 0)
/*     */     {
/*  86 */       isPreinstall = true;
/*     */     }
/*     */     else
/*     */     {
/*  90 */       isPreinstall = false;
/*     */     }
/*     */ 
/*  93 */     if ((!isPreinstall) || (disposition.equals("update")))
/*     */     {
/*     */       try
/*     */       {
/*  97 */         for (int i = 1; i < this.m_installer.m_myPlatforms.length; ++i)
/*     */         {
/*  99 */           this.m_installer.m_installerConfig.put("target.platform", this.m_installer.m_myPlatforms[i]);
/*     */ 
/* 101 */           applyPermissions(installer, this.m_installer.m_myPlatforms[i], (isPreinstall) ? "Clear" : "Set");
/*     */         }
/*     */ 
/* 105 */         this.m_installer.m_installerConfig.put("target.platform", this.m_installer.m_platform);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 110 */         Report.trace("install", null, e);
/* 111 */         throw new ServiceException("!csInstallerPermConfigError", e);
/*     */       }
/*     */     }
/* 114 */     return 0;
/*     */   }
/*     */ 
/*     */   public void applyPermissions(SysInstaller installer, String platform, String action)
/*     */     throws DataException, ServiceException
/*     */   {
/* 120 */     NativeOsUtils utils = this.m_installer.m_utils;
/*     */ 
/* 123 */     FieldInfo[] infos = ResultSetUtils.createInfoList(this.m_executables, new String[] { "Platform", "CommandLine", "DirectorySuffix", "IsUnix" }, true);
/*     */ 
/* 125 */     Vector executableInfo = this.m_executables.findRow(infos[0].m_index, this.m_installer.m_platform);
/* 126 */     if ((executableInfo == null) && (utils == null))
/*     */     {
/* 128 */       throw new ServiceException(LocaleUtils.encodeMessage("csInstallerPermExecNotFound", null, this.m_installer.m_platform));
/*     */     }
/*     */ 
/* 132 */     String commandLine = (String)executableInfo.elementAt(infos[1].m_index);
/* 133 */     String directorySuffix = (String)executableInfo.elementAt(infos[2].m_index);
/* 134 */     String tmp = (String)executableInfo.elementAt(infos[3].m_index);
/* 135 */     boolean isUnix = StringUtils.convertToBool(tmp, false);
/*     */ 
/* 137 */     String permissionSet = new StringBuilder().append((isUnix) ? "Unix" : "Windows").append(action).toString();
/* 138 */     infos = ResultSetUtils.createInfoList(this.m_permissions, new String[] { "Permission", permissionSet }, true);
/*     */ 
/* 141 */     for (this.m_rules.first(); this.m_rules.isRowPresent(); this.m_rules.next())
/*     */     {
/* 143 */       Properties props = this.m_rules.getCurrentRowProps();
/* 144 */       String enabled = props.getProperty("IsEnabled");
/* 145 */       String platformList = props.getProperty("PlatformList");
/* 146 */       if (platformList != null)
/*     */       {
/* 148 */         Vector list = StringUtils.parseArray(platformList, ',', '^');
/* 149 */         if ((!platformList.equals("*")) && (list.indexOf(platform) < 0)) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 155 */       if ((action.equals("Set")) && (!StringUtils.convertToBool(enabled, false))) {
/*     */         continue;
/*     */       }
/*     */ 
/* 159 */       String path = props.getProperty("Path");
/* 160 */       String permission = props.getProperty("Permission");
/* 161 */       String dest = installer.computeDestinationEx(path, false);
/*     */ 
/* 163 */       if ((utils != null) && (EnvUtils.getOSFamily().equals("unix")))
/*     */       {
/* 167 */         String setBits = this.m_installer.getInstallerTableValue("PermissionConfig", permission, "UnixSetBits");
/*     */ 
/* 169 */         String clearBits = this.m_installer.getInstallerTableValue("PermissionConfig", permission, "UnixClearBits");
/*     */ 
/* 171 */         if (action.equalsIgnoreCase("clear"))
/*     */         {
/* 173 */           String tmpBits = setBits;
/* 174 */           setBits = clearBits;
/* 175 */           clearBits = tmpBits;
/*     */         }
/*     */ 
/* 178 */         int setMask = Integer.parseInt(setBits, 8);
/* 179 */         int clearMask = Integer.parseInt(clearBits, 8);
/* 180 */         applyPermissions(utils, dest, setMask, clearMask);
/*     */       }
/*     */       else
/*     */       {
/* 184 */         Vector v = this.m_permissions.findRow(infos[0].m_index, permission);
/* 185 */         if (v == null)
/*     */           continue;
/* 187 */         String setting = (String)v.elementAt(infos[1].m_index);
/* 188 */         if ((setting == null) || (setting.length() == 0)) continue; if (setting.equals("null")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 192 */         File destFile = new File(dest);
/* 193 */         if ((FileUtils.checkFile(dest, false, false) != 0) && (FileUtils.checkFile(dest, true, false) != 0))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 198 */         if ((destFile.isDirectory()) && (!dest.endsWith(directorySuffix)))
/*     */         {
/* 200 */           dest = new StringBuilder().append(dest).append(directorySuffix).toString();
/*     */         }
/* 202 */         String[] finalCommandLine = createCommandLine(commandLine, setting, dest);
/* 203 */         String message = LocaleUtils.encodeMessage("csInstallerPermExecError", null, finalCommandLine);
/*     */ 
/* 205 */         Vector results = new IdcVector();
/* 206 */         if (installer.runCommand(finalCommandLine, results, message, false, true) == 0) {
/*     */           continue;
/*     */         }
/* 209 */         message = LocaleUtils.encodeMessage("csInstallerPermError", null, dest);
/*     */ 
/* 211 */         installer.m_installLog.warning(message);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String applyPermissions(NativeOsUtils utils, String dest, int setMask, int clearMask)
/*     */   {
/* 221 */     PosixStructStat sb = new PosixStructStat();
/* 222 */     boolean success = false;
/* 223 */     String osErrorText = null;
/*     */     int rc;
/* 225 */     if ((rc = utils.stat(dest, sb)) == 0)
/*     */     {
/* 227 */       File file = new File(dest);
/* 228 */       if (file.isDirectory())
/*     */       {
/* 230 */         String[] listing = file.list();
/* 231 */         for (int i = 0; i < listing.length; ++i)
/*     */         {
/* 233 */           String tmpDest = new StringBuilder().append(dest).append("/").append(listing[i]).toString();
/* 234 */           applyPermissions(utils, tmpDest, setMask, clearMask);
/*     */         }
/* 236 */         success = true;
/*     */       }
/*     */       else
/*     */       {
/* 240 */         int newMode = sb.st_mode |= setMask;
/* 241 */         newMode &= (clearMask ^ 0xFFFFFFFF);
/* 242 */         if ((rc = utils.chmod(dest, newMode)) == 0)
/*     */         {
/* 244 */           success = true;
/*     */         }
/*     */         else
/*     */         {
/* 248 */           osErrorText = utils.getErrorMessage(rc);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 254 */       osErrorText = utils.getErrorMessage(rc);
/*     */     }
/*     */ 
/* 257 */     if (!success)
/*     */     {
/* 259 */       Report.trace("install", new StringBuilder().append("unable to set permissions on '").append(dest).append("': ").append(osErrorText).toString(), null);
/*     */     }
/*     */ 
/* 263 */     return osErrorText;
/*     */   }
/*     */ 
/*     */   public String[] createCommandLine(String line, String flag, String path)
/*     */   {
/* 269 */     Properties props = new Properties(this.m_installer.m_installerConfig);
/* 270 */     props.put("flag", flag);
/* 271 */     props.put("path", path);
/*     */ 
/* 273 */     line = this.m_installer.substituteVariables(line, props);
/* 274 */     Vector v = StringUtils.parseArray(line, ' ', ' ');
/*     */ 
/* 276 */     int size = v.size();
/* 277 */     String[] commandLine = new String[size];
/* 278 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 280 */       commandLine[i] = ((String)v.elementAt(i));
/*     */     }
/*     */ 
/* 283 */     return commandLine;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.PermissionInstaller
 * JD-Core Version:    0.5.4
 */