/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WindowsRegistry
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected NativeOsUtils m_utils;
/*     */   protected String m_baseKey;
/*     */   protected boolean m_isDtmServer;
/*     */   protected boolean m_isProxied;
/*     */   protected boolean m_isRefinery;
/*     */   protected boolean m_isRemove;
/*     */ 
/*     */   public WindowsRegistry()
/*     */   {
/*  39 */     this.m_baseKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Content Server";
/*     */   }
/*     */ 
/*     */   public void init(SysInstaller installer)
/*     */   {
/*  48 */     this.m_installer = installer;
/*  49 */     this.m_utils = installer.m_utils;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  56 */     init(installer);
/*     */ 
/*  58 */     if (name.equals("enable-disable-ntfs-8dot3"))
/*     */     {
/*  60 */       if (installer.getInstallBool("Disable8dot3", true))
/*     */       {
/*  62 */         disable8dot3();
/*     */       }
/*     */       else
/*     */       {
/*  66 */         enable8dot3();
/*     */       }
/*  68 */       return 0;
/*     */     }
/*  70 */     this.m_isProxied = this.m_installer.getInstallBool("IsProxiedServer", false);
/*  71 */     this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*  72 */     if (!this.m_isProxied)
/*     */     {
/*  74 */       String idcAdminNTPath = this.m_installer.computeDestinationEx("admin/bin/IdcAdminNT.exe", false);
/*     */ 
/*  76 */       if ((this.m_installer.getConfigValue("SharedWeblayoutDir") != null) && (FileUtils.checkFile(idcAdminNTPath, true, false) != 0))
/*     */       {
/*  79 */         this.m_isProxied = true;
/*     */       }
/*     */     }
/*  82 */     if (name.startsWith("dtm-"))
/*     */     {
/*  84 */       this.m_isDtmServer = true;
/*  85 */       this.m_baseKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Dtm Server";
/*     */     }
/*     */ 
/*  88 */     if (this.m_isRefinery)
/*     */     {
/*  90 */       this.m_baseKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\Inbound Refinery";
/*     */     }
/*     */ 
/*  93 */     this.m_isRemove = this.m_installer.getInstallBool("IsUninstall", false);
/*     */ 
/*  96 */     if (this.m_installer.getInstallBool("RegisterServers", true))
/*     */     {
/*  98 */       registerServers();
/*     */     }
/* 100 */     if ((!this.m_installer.m_isUpdate) || (this.m_isRemove))
/*     */     {
/* 102 */       installServices();
/*     */     }
/* 104 */     if ((!this.m_isRemove) && (this.m_installer.getInstallBool("Disable8dot3NameCreation", false)))
/*     */     {
/* 107 */       disable8dot3();
/*     */     }
/*     */ 
/* 110 */     return 0;
/*     */   }
/*     */ 
/*     */   public void registerServers() throws ServiceException
/*     */   {
/* 115 */     boolean legacyEnabled = this.m_installer.getInstallBool("EnableStellentRegistryEntries", false);
/* 116 */     String idcName = this.m_installer.getConfigValue("IDC_Name");
/* 117 */     String intradocDir = this.m_installer.getConfigValue("IntradocDir");
/* 118 */     String listOfInstalledInstances = "";
/* 119 */     if (this.m_utils != null)
/*     */     {
/* 121 */       listOfInstalledInstances = this.m_utils.getRegistryValue(this.m_baseKey + "\\ListOfInstalledInstances");
/*     */     }
/*     */     String newList;
/*     */     String newList;
/* 126 */     if (legacyEnabled)
/*     */     {
/* 128 */       newList = addRemoveListElement(listOfInstalledInstances, idcName);
/*     */     }
/*     */     else
/*     */     {
/* 133 */       newList = listOfInstalledInstances;
/*     */     }
/* 135 */     if (newList != listOfInstalledInstances)
/*     */     {
/* 137 */       String key = this.m_baseKey + "\\ListOfInstalledInstances";
/* 138 */       if ((newList.length() == 0) || (newList.equals(",")))
/*     */       {
/* 140 */         removeRegistryValue(key);
/*     */       }
/*     */       else
/*     */       {
/* 144 */         if (!newList.startsWith(","))
/*     */         {
/* 146 */           newList = "," + newList;
/*     */         }
/* 148 */         setRegistryValue(key, newList, "string");
/*     */       }
/*     */     }
/*     */ 
/* 152 */     if (this.m_isRemove)
/*     */     {
/* 154 */       removeRegistryValue(this.m_baseKey + "\\Servers\\" + idcName);
/*     */     }
/* 157 */     else if (legacyEnabled)
/*     */     {
/* 159 */       String verInstalled = this.m_installer.getInstallValue("ProductVersionToBeInstalled", "unknown");
/* 160 */       setRegistryValue(this.m_baseKey + "\\Servers\\" + idcName + "\\Version", verInstalled, "string");
/*     */ 
/* 163 */       setRegistryValue(this.m_baseKey + "\\Servers\\" + idcName + "\\CoreDir", intradocDir, "string");
/*     */     }
/*     */ 
/* 168 */     if (legacyEnabled)
/*     */     {
/* 170 */       setRegistryValue(this.m_baseKey + "\\LastServerDirectory", intradocDir, "string");
/*     */     }
/*     */ 
/* 174 */     if (this.m_isProxied)
/*     */     {
/* 176 */       String masterServerDirectory = this.m_installer.getInstallValue("MasterServerDir", null);
/*     */ 
/* 178 */       if ((masterServerDirectory != null) && (legacyEnabled))
/*     */       {
/* 180 */         setRegistryValue(this.m_baseKey + "\\LastMasterServerDirectory", masterServerDirectory, "string");
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 185 */       if (!legacyEnabled)
/*     */         return;
/* 187 */       setRegistryValue(this.m_baseKey + "\\LastMasterServerDirectory", intradocDir, "string");
/*     */ 
/* 189 */       String listOfMasters = null;
/* 190 */       if (this.m_utils != null)
/*     */       {
/* 192 */         listOfMasters = this.m_utils.getRegistryValue(this.m_baseKey + "\\ListOfMasterInstances");
/*     */       }
/*     */ 
/* 195 */       newList = addRemoveListElement(listOfMasters, idcName);
/*     */ 
/* 197 */       if ((newList == listOfMasters) && (!newList.equals(",")))
/*     */         return;
/* 199 */       String key = this.m_baseKey + "\\ListOfMasterInstances";
/*     */ 
/* 201 */       if (newList.length() == 0)
/*     */       {
/* 203 */         removeRegistryValue(key);
/*     */       }
/*     */       else
/*     */       {
/* 207 */         if (!newList.startsWith(","))
/*     */         {
/* 209 */           newList = "," + newList;
/*     */         }
/* 211 */         setRegistryValue(key, newList, "string");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected long setRegistryValue(String key, String value, String type)
/*     */     throws ServiceException
/*     */   {
/* 225 */     Report.trace("install", "setting registry key \"" + key + "\" to \"" + value + "\"", null);
/*     */ 
/* 227 */     if (this.m_utils == null)
/*     */     {
/* 229 */       String msg = LocaleUtils.encodeMessage("syNativeOsUtilsNotLoaded", null);
/*     */ 
/* 231 */       msg = LocaleUtils.encodeMessage("csUnableToSetRegistryEntry", msg, key, value);
/*     */ 
/* 233 */       this.m_installer.m_installLog.error(msg);
/* 234 */       return -1L;
/*     */     }
/*     */ 
/* 237 */     long rc = this.m_utils.setRegistryValue(key, value, type);
/* 238 */     if (rc != 0L)
/*     */     {
/* 240 */       String msg = this.m_utils.getErrorMessage((int)rc);
/* 241 */       msg = LocaleUtils.encodeMessage("csUnableToSetRegistryEntry", msg, key, value);
/*     */ 
/* 243 */       this.m_installer.m_installLog.error(msg);
/*     */     }
/* 245 */     return rc;
/*     */   }
/*     */ 
/*     */   protected long removeRegistryValue(String key)
/*     */     throws ServiceException
/*     */   {
/* 251 */     Report.trace("install", "removing registry key \"" + key + "\"", null);
/*     */ 
/* 253 */     if (this.m_utils == null)
/*     */     {
/* 255 */       String msg = LocaleUtils.encodeMessage("syNativeOsUtilsNotLoaded", null);
/*     */ 
/* 257 */       msg = LocaleUtils.encodeMessage("csUnableToClearRegistryEntry", null, key);
/*     */ 
/* 259 */       this.m_installer.m_installLog.error(msg);
/* 260 */       return -1L;
/*     */     }
/* 262 */     long rc = this.m_utils.clearRegistryEntry(key);
/* 263 */     if ((rc == NativeOsUtils.ERROR_ENVVAR_NOT_FOUND) || (rc == NativeOsUtils.ERROR_FILE_NOT_FOUND) || (rc == 0L))
/*     */     {
/* 267 */       return 0L;
/*     */     }
/* 269 */     String msg = this.m_utils.getErrorMessage((int)rc);
/* 270 */     msg = LocaleUtils.encodeMessage("csUnableToClearRegistryEntry", null, key);
/*     */ 
/* 272 */     this.m_installer.m_installLog.error(msg);
/* 273 */     return rc;
/*     */   }
/*     */ 
/*     */   protected String addRemoveListElement(String list, String name)
/*     */   {
/* 278 */     if (list == null)
/*     */     {
/* 280 */       list = "";
/*     */     }
/* 282 */     Vector v = StringUtils.parseArray(list, ',', '^');
/* 283 */     int size = v.size();
/* 284 */     boolean found = false;
/* 285 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 287 */       String element = (String)v.elementAt(i);
/* 288 */       if (element.length() == 0)
/*     */       {
/* 290 */         v.removeElementAt(i--);
/* 291 */         --size;
/*     */       }
/* 293 */       if (!element.equals(name))
/*     */         continue;
/* 295 */       found = true;
/* 296 */       v.removeElementAt(i--);
/* 297 */       --size;
/*     */     }
/*     */ 
/* 301 */     if (((!this.m_isRemove) && (found)) || ((this.m_isRemove) && (!found)))
/*     */     {
/* 303 */       return list;
/*     */     }
/* 305 */     if (!this.m_isRemove)
/*     */     {
/* 307 */       v.addElement(name);
/*     */     }
/* 309 */     return StringUtils.createString(v, ',', '^');
/*     */   }
/*     */ 
/*     */   public void installServices()
/*     */     throws ServiceException
/*     */   {
/* 315 */     String cmd = "bin/IdcServerNT.exe";
/* 316 */     if (this.m_isDtmServer)
/*     */     {
/* 318 */       cmd = "bin/DtmServerNT.exe";
/*     */     }
/* 320 */     else if (this.m_isRefinery)
/*     */     {
/* 322 */       cmd = "bin/IdcRefineryNT.exe";
/*     */     }
/*     */ 
/* 325 */     installService(cmd, "InstallServerService", "false");
/* 326 */     if ((((!this.m_isRemove) || (this.m_isProxied) || (this.m_isDtmServer))) && (!this.m_installer.getInstallBool("ConfigureAdminServer", false)) && (!this.m_installer.getInstallBool("ConfigureAdminClusterNode", false))) {
/*     */       return;
/*     */     }
/*     */ 
/* 330 */     cmd = "admin/bin/IdcAdminNT.exe";
/* 331 */     installService(cmd, "InstallAdminServerService", "false");
/*     */   }
/*     */ 
/*     */   protected void installService(String cmd, String variablePrefix, String defaultValue)
/*     */     throws ServiceException
/*     */   {
/* 338 */     String installService = this.m_installer.getInstallValue(variablePrefix, defaultValue);
/*     */ 
/* 340 */     installService = installService.toLowerCase();
/* 341 */     String installFlag = null;
/* 342 */     String errMsg = null;
/* 343 */     if ((this.m_isRemove) || ((installService != null) && (installService.equals("remove"))))
/*     */     {
/* 345 */       installFlag = "-remove";
/* 346 */       errMsg = "csUnableToRemoveService";
/*     */     }
/* 348 */     else if ((installService != null) && (installService.equals("auto")))
/*     */     {
/* 350 */       installFlag = "-install_autostart";
/* 351 */       errMsg = "csUnableToInstallService";
/*     */     }
/* 353 */     else if ((installService != null) && (installService.equals("true")))
/*     */     {
/* 355 */       installFlag = "-install";
/* 356 */       errMsg = "csUnableToInstallService";
/*     */     }
/*     */     else
/*     */     {
/* 360 */       return;
/*     */     }
/*     */ 
/* 363 */     String installServiceUser = null;
/* 364 */     String installServicePassword = null;
/* 365 */     if (this.m_installer.getInstallBool(variablePrefix + "ConfigureRunAs", false))
/*     */     {
/* 368 */       installServiceUser = this.m_installer.getInstallValue(variablePrefix + "User", null);
/*     */ 
/* 370 */       if ((installServiceUser != null) && (installServiceUser.length() == 0))
/*     */       {
/* 373 */         installServiceUser = null;
/*     */       }
/* 375 */       installServicePassword = this.m_installer.getInstallValue(variablePrefix + "Password", null);
/*     */ 
/* 377 */       if ((installServicePassword != null) && (installServicePassword.length() == 0))
/*     */       {
/* 380 */         installServicePassword = null;
/*     */       }
/*     */     }
/*     */ 
/* 384 */     String installServiceDependency = null;
/* 385 */     if (this.m_installer.getInstallBool(variablePrefix + "ConfigureDependency", false))
/*     */     {
/* 388 */       installServiceDependency = this.m_installer.getInstallValue(variablePrefix + "Dependency", null);
/*     */ 
/* 390 */       if ((installServiceDependency != null) && (installServiceDependency.length() == 0))
/*     */       {
/* 393 */         installServiceDependency = null;
/*     */       }
/*     */     }
/*     */ 
/* 397 */     Vector commandLine = new IdcVector();
/*     */ 
/* 399 */     String prefix = this.m_installer.getInstallValue("ServicePathPrefix", null);
/* 400 */     if (prefix != null)
/*     */     {
/* 402 */       cmd = prefix + cmd;
/*     */     }
/* 404 */     cmd = this.m_installer.computeDestination(cmd);
/* 405 */     commandLine.addElement(cmd);
/* 406 */     commandLine.addElement(installFlag);
/* 407 */     if ((!this.m_isRemove) && (installServiceUser != null) && (installServicePassword != null))
/*     */     {
/* 410 */       commandLine.addElement("-asuser");
/* 411 */       commandLine.addElement(installServiceUser);
/* 412 */       commandLine.addElement(installServicePassword);
/*     */     }
/*     */ 
/* 415 */     runCommand(commandLine, errMsg);
/*     */ 
/* 417 */     commandLine = new IdcVector();
/* 418 */     if ((!this.m_isRemove) && (installServiceDependency != null))
/*     */     {
/* 420 */       commandLine.addElement(cmd);
/* 421 */       commandLine.addElement("-dependent");
/* 422 */       commandLine.addElement(installServiceDependency);
/*     */     }
/* 424 */     runCommand(commandLine, errMsg);
/*     */   }
/*     */ 
/*     */   protected void runCommand(Vector commandLine, String errMsg)
/*     */     throws ServiceException
/*     */   {
/* 430 */     if (commandLine.size() <= 0)
/*     */       return;
/* 432 */     String[] line = new String[commandLine.size()];
/* 433 */     commandLine.copyInto(line);
/* 434 */     Vector results = new IdcVector();
/* 435 */     if (this.m_installer.runCommand(line, results, null, true, true) == 0)
/*     */       return;
/* 437 */     this.m_installer.m_installLog.error(errMsg);
/*     */   }
/*     */ 
/*     */   protected void disable8dot3()
/*     */     throws ServiceException
/*     */   {
/* 444 */     if (this.m_isRemove)
/*     */       return;
/* 446 */     setRegistryValue("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\FileSystem\\NtfsDisable8dot3NameCreation", "1", "dword");
/*     */   }
/*     */ 
/*     */   protected void enable8dot3()
/*     */     throws ServiceException
/*     */   {
/* 454 */     if (this.m_isRemove)
/*     */       return;
/* 456 */     setRegistryValue("HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\FileSystem\\NtfsDisable8dot3NameCreation", "0", "dword");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 464 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81280 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.WindowsRegistry
 * JD-Core Version:    0.5.4
 */