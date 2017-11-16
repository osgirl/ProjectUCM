/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IISInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected String m_winBinDir;
/*     */   protected boolean m_isProxied;
/*     */   protected boolean m_useLegacyIISConfiguration;
/*     */   protected boolean m_isClusterConfiguration;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  40 */     this.m_installer = installer;
/*     */ 
/*  42 */     String webServer = this.m_installer.getInstallValue("WebServer", "");
/*  43 */     if ((!webServer.equals("iis")) || (!disposition.equals("always")))
/*     */     {
/*  45 */       Report.trace("install", "skipping iis because the webserver is \"" + webServer + "\".", null);
/*  46 */       return 0;
/*     */     }
/*  48 */     this.m_isClusterConfiguration = ((name.equals("cluster-node-iis-configure")) || (name.equals("cluster-node-iis-remove-filter")));
/*     */ 
/*  50 */     String platform = this.m_installer.getInstallValue("Platform", "unknown");
/*  51 */     if (this.m_installer.isWindows())
/*     */     {
/*  53 */       this.m_winBinDir = this.m_installer.computeDestination("shared/os/" + platform + "/bin");
/*     */     }
/*     */ 
/*  56 */     String proxiedType = this.m_installer.getInstallValue("ConfigureProxiedServer", "no");
/*  57 */     this.m_isProxied = ((this.m_installer.getInstallBool("IsProxiedServer", false)) || (StringUtils.convertToBool(proxiedType, true)));
/*     */ 
/*  60 */     if (name.equals("cluster-node-iis-remove-filter"))
/*     */     {
/*  62 */       removeFilter();
/*  63 */       return 0;
/*     */     }
/*  65 */     String cgiFileName = this.m_installer.getConfigValue("CgiFileName");
/*  66 */     if ((this.m_installer.isOlderVersion("7.0.0.0")) || ((cgiFileName != null) && (cgiFileName.equals("idc_cgi_isapi.dll"))))
/*     */     {
/*  69 */       Report.trace("install", "using legacy IIS configuration", null);
/*  70 */       this.m_useLegacyIISConfiguration = true;
/*     */     }
/*     */ 
/*  92 */     String relativeWebRoot = this.m_installer.getInstallValue("HttpRelativeWebRoot", "/idc/");
/*     */ 
/*  94 */     if (relativeWebRoot == null)
/*     */     {
/*  96 */       String msg = LocaleUtils.encodeMessage("csInstallerRelativeWebRootMissing", null);
/*     */ 
/*  98 */       msg = LocaleUtils.encodeMessage("csInstallerIISConfigurationSkipped", msg);
/*  99 */       this.m_installer.m_installLog.error(msg);
/* 100 */       return 0;
/*     */     }
/* 102 */     String relativeCgiRoot = this.m_installer.getInstallValue("HttpRelativeCgiRoot", null);
/*     */ 
/* 104 */     if ((this.m_useLegacyIISConfiguration) && (relativeCgiRoot == null))
/*     */     {
/* 106 */       relativeCgiRoot = "/intradoc-cgi/";
/*     */     }
/* 108 */     if (relativeCgiRoot.equals(relativeWebRoot))
/*     */     {
/* 110 */       relativeCgiRoot = null;
/*     */     }
/* 112 */     if ((relativeCgiRoot != null) && (relativeCgiRoot.equals("/")))
/*     */     {
/* 114 */       if (this.m_useLegacyIISConfiguration)
/*     */       {
/* 116 */         relativeCgiRoot = "/intradoc-cgi/";
/*     */       }
/*     */       else
/*     */       {
/* 120 */         relativeCgiRoot = null;
/*     */       }
/*     */     }
/* 123 */     this.m_installer.editConfigFile("config/config.cfg", "HttpRelativeCgiRoot", relativeCgiRoot);
/*     */ 
/* 125 */     if (relativeCgiRoot == null)
/*     */     {
/* 127 */       relativeCgiRoot = relativeWebRoot + "idcplg";
/*     */     }
/* 129 */     relativeWebRoot = cleanWebPathForIIS(relativeWebRoot);
/* 130 */     relativeCgiRoot = cleanWebPathForIIS(relativeCgiRoot);
/*     */ 
/* 132 */     boolean isRemove = this.m_installer.getInstallBool("IsUninstall", false);
/*     */ 
/* 134 */     boolean disableFileHandleCaching = this.m_installer.getInstallBool("DisableIISFileHandleCaching", false);
/*     */ 
/* 136 */     String useNtlmSecurity = this.m_installer.getInstallValue("NtlmSecurityEnabled", "false");
/*     */ 
/* 138 */     useNtlmSecurity = useNtlmSecurity.toLowerCase();
/* 139 */     boolean useNtlm = false;
/* 140 */     boolean useAdsi = false;
/* 141 */     if ((useNtlmSecurity.equals("ntlm")) || (StringUtils.convertToBool(useNtlmSecurity, false)))
/*     */     {
/* 144 */       useNtlm = true;
/*     */     }
/* 146 */     else if (useNtlmSecurity.equals("adsi"))
/*     */     {
/* 148 */       useNtlm = true;
/* 149 */       useAdsi = true;
/*     */     }
/*     */ 
/* 152 */     if (!this.m_installer.m_isUpdate)
/*     */     {
/* 154 */       this.m_installer.editConfigFile("config/config.cfg", "NtlmSecurityEnabled", (useNtlm) ? "true" : "false");
/*     */ 
/* 156 */       this.m_installer.editConfigFile("config/config.cfg", "UseAdsi", (useAdsi) ? "true" : "false");
/*     */     }
/*     */ 
/* 160 */     String weblayoutDir = this.m_installer.getConfigValue("WeblayoutDir");
/* 161 */     if (weblayoutDir == null)
/*     */     {
/* 163 */       weblayoutDir = this.m_installer.computeDestination("weblayout/");
/*     */     }
/* 165 */     weblayoutDir = FileUtils.windowsSlashes(weblayoutDir);
/*     */ 
/* 167 */     String cgiDir = this.m_installer.computeDestinationEx("idcplg/", false);
/* 168 */     if (this.m_isClusterConfiguration)
/*     */     {
/* 170 */       cgiDir = this.m_installer.getInstallValue("CgiDir", cgiDir);
/*     */     }
/*     */ 
/* 173 */     if ((!this.m_isProxied) && (this.m_installer.isOlderVersion("4.0")))
/*     */     {
/* 177 */       String[] cmd = { "CScript.exe", this.m_winBinDir + "/UpdateLegacyDefaultFile.vbs", relativeWebRoot };
/*     */ 
/* 183 */       String msg = LocaleUtils.encodeMessage("csInstallerIISUnableToUpdateDefaultPage", null);
/*     */ 
/* 185 */       Vector results = new IdcVector();
/* 186 */       this.m_installer.runCommand(cmd, results, msg, false, true);
/*     */     }
/*     */ 
/* 189 */     if (!this.m_isProxied)
/*     */     {
/* 191 */       if (isRemove)
/*     */       {
/* 193 */         removeFilter();
/*     */       }
/*     */       else
/*     */       {
/* 197 */         installFilter(cgiDir, platform);
/*     */       }
/*     */     }
/*     */ 
/* 201 */     if ((!isRemove) && (disableFileHandleCaching))
/*     */     {
/* 203 */       WindowsRegistry winReg = new WindowsRegistry();
/* 204 */       winReg.init(this.m_installer);
/* 205 */       winReg.setRegistryValue("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Services\\InetInfo\\Parameters\\DisableMemoryCache", "1", "dword");
/*     */     }
/*     */ 
/* 212 */     String[] cmd = { "CScript.exe", this.m_winBinDir + "/RemoveIISDirs.vbs", relativeCgiRoot };
/*     */ 
/* 219 */     if ((!this.m_isProxied) && (!relativeCgiRoot.equals(relativeWebRoot)))
/*     */     {
/* 221 */       Vector results = new IdcVector();
/* 222 */       String msg = LocaleUtils.encodeMessage("csInstallerIISUnableToRemoveDirectory", null, cmd[2]);
/*     */ 
/* 224 */       this.m_installer.runCommand(cmd, results, msg, false, true);
/*     */     }
/*     */ 
/* 227 */     Vector results = new IdcVector();
/* 228 */     cmd[2] = relativeWebRoot;
/* 229 */     String msg = LocaleUtils.encodeMessage("csInstallerIISUnableToRemoveDirectory", null, relativeWebRoot);
/*     */ 
/* 231 */     this.m_installer.runCommand(cmd, results, msg, false, true);
/*     */ 
/* 235 */     if (!isRemove)
/*     */     {
/* 237 */       if (this.m_isProxied)
/*     */       {
/* 239 */         cmd = new String[] { "CScript.exe", this.m_winBinDir + "/CreateSlaveIISDirs.vbs", FileUtils.windowsSlashes(weblayoutDir), relativeWebRoot, (useNtlm) ? "true" : "false" };
/*     */       }
/*     */       else
/*     */       {
/* 250 */         cmd = new String[] { "CScript.exe", this.m_winBinDir + "/CreateMasterIISDirs.vbs", FileUtils.windowsSlashes(weblayoutDir), FileUtils.windowsSlashes(cgiDir), relativeWebRoot, relativeCgiRoot, (useNtlm) ? "true" : "false" };
/*     */       }
/*     */ 
/* 261 */       results = new IdcVector();
/* 262 */       msg = LocaleUtils.encodeMessage("csInstallerIISUnableToCreateDirectories", null);
/*     */ 
/* 264 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */     }
/*     */ 
/* 267 */     stopIIS();
/* 268 */     startIIS();
/*     */ 
/* 270 */     return 0;
/*     */   }
/*     */ 
/*     */   public void stopIIS() throws ServiceException
/*     */   {
/* 275 */     String[] cmd = { "net.exe", "stop", "iisadmin", "/y" };
/*     */ 
/* 280 */     Vector results = new IdcVector();
/* 281 */     String msg = LocaleUtils.encodeMessage("csInstallerIISUnableToStopIIS", null);
/*     */ 
/* 283 */     this.m_installer.runCommand(cmd, results, msg, false, true);
/* 284 */     SystemUtils.sleep(1000L);
/*     */   }
/*     */ 
/*     */   public void startIIS() throws ServiceException
/*     */   {
/* 289 */     String[] cmd = { "net.exe", "start", "w3svc" };
/*     */ 
/* 294 */     Vector results = new IdcVector();
/* 295 */     String msg = LocaleUtils.encodeMessage("csInstallerIISUnableToStartIIS", null);
/*     */ 
/* 297 */     this.m_installer.runCommand(cmd, results, msg, false, true);
/* 298 */     SystemUtils.sleep(1000L);
/*     */   }
/*     */ 
/*     */   public void installFilter(String cgiDir)
/*     */     throws ServiceException
/*     */   {
/* 304 */     installFilter(cgiDir, "win32");
/*     */   }
/*     */ 
/*     */   public void installFilter(String cgiDir, String platform) throws ServiceException
/*     */   {
/* 309 */     String msg = LocaleUtils.encodeMessage("csInstallerIISUnknownError", null);
/*     */ 
/* 311 */     String idcName = this.m_installer.getInstallValue("IDC_Name", "unknown");
/*     */     String filterFileName;
/* 313 */     if (this.m_useLegacyIISConfiguration)
/*     */     {
/* 315 */       filterFileName = "idc_cgi_isapi.dll";
/*     */     }
/*     */     else
/*     */     {
/* 319 */       filterFileName = "idc_cgi_isapi-" + idcName + ".dll";
/*     */     }
/* 321 */     String filterFileName = this.m_installer.getInstallValue("IISFilterFileName", filterFileName);
/*     */ 
/* 323 */     String filterPath = cgiDir + "/" + filterFileName;
/* 324 */     filterPath = this.m_installer.computeDestination(filterPath);
/*     */ 
/* 326 */     if (this.m_installer.isOlderVersion("7.0"))
/*     */     {
/* 328 */       String[] cmd = { "CScript.exe", this.m_winBinDir + "/SetFilterClass.vbs" };
/*     */ 
/* 333 */       Vector results = new IdcVector();
/* 334 */       msg = LocaleUtils.encodeMessage("csInstallerIISUnableToInstallFilter", null);
/*     */ 
/* 336 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */     }
/*     */ 
/* 339 */     String pluginSource = this.m_installer.computeDestinationEx("${IdcHomeDir}/${Platform}/lib/idc_cgi_isapi.dll", false);
/*     */     try
/*     */     {
/* 343 */       stopIIS();
/* 344 */       this.m_installer.copy("shared/os/" + platform + "/lib/idc_cgi_isapi.dll", filterPath);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 350 */       String dst = this.m_installer.computeDestination("shared/os/" + platform + "/lib/idc_cgi_isapi.dll");
/*     */ 
/* 352 */       msg = LocaleUtils.encodeMessage("syFileUtilsUnableToCopy", e.getMessage(), pluginSource, dst);
/*     */ 
/* 354 */       this.m_installer.m_installLog.error(msg);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 359 */       startIIS();
/* 360 */       removeFilter();
/* 361 */       stopIIS();
/* 362 */       startIIS();
/* 363 */       String filterName = "Idc Plugin " + idcName;
/* 364 */       filterName = this.m_installer.getInstallValue("IISPluginName", filterName);
/*     */ 
/* 366 */       String[] cmd = { "CScript.exe", this.m_winBinDir + "/AddIdcIISFilter.vbs", FileUtils.windowsSlashes(filterPath), filterName };
/*     */ 
/* 373 */       Vector results = new IdcVector();
/* 374 */       msg = LocaleUtils.encodeMessage("csInstallerIISUnableToInstallFilter", null);
/*     */ 
/* 376 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 380 */       this.m_installer.m_installLog.error(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void removeFilter()
/*     */     throws ServiceException
/*     */   {
/* 387 */     String msg = LocaleUtils.encodeMessage("csInstallerIISUnknownError", null);
/*     */ 
/* 389 */     String idcName = this.m_installer.getInstallValue("IDC_Name", "unknown");
/*     */     try
/*     */     {
/* 393 */       String filterName = "Idc Plugin " + idcName;
/* 394 */       filterName = this.m_installer.getInstallValue("IISPluginName", filterName);
/*     */ 
/* 396 */       String[] cmd = { "CScript.exe", this.m_winBinDir + "/RemoveIdcIISFilter.vbs", filterName };
/*     */ 
/* 402 */       Vector results = new IdcVector();
/* 403 */       msg = LocaleUtils.encodeMessage("csInstallerIISUnableToRemoveFilter", null);
/*     */ 
/* 405 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */ 
/* 407 */       cmd[2] = "Stellent Content Server Plugin";
/* 408 */       results = new IdcVector();
/* 409 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */ 
/* 411 */       cmd[2] = "IntraNet Content Server Plugin";
/* 412 */       results = new IdcVector();
/* 413 */       this.m_installer.runCommand(cmd, results, msg, true, true);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 417 */       this.m_installer.m_installLog.error(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String cleanWebPathForIIS(String path)
/*     */   {
/* 423 */     if (path.equals("/"))
/*     */     {
/* 425 */       return path;
/*     */     }
/* 427 */     if (path.indexOf("/") == 0)
/*     */     {
/* 429 */       path = path.substring(1);
/*     */     }
/* 431 */     int index = path.lastIndexOf("/");
/* 432 */     if ((index > 0) && (index == path.length() - 1))
/*     */     {
/* 434 */       path = path.substring(0, index);
/*     */     }
/* 436 */     return path;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 441 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92555 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.IISInstaller
 * JD-Core Version:    0.5.4
 */