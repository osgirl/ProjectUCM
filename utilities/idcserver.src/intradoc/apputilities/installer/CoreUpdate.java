/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Stack;
/*     */ 
/*     */ public class CoreUpdate
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */   public String[] m_renameExcludeList;
/*     */ 
/*     */   public CoreUpdate()
/*     */   {
/*  42 */     this.m_renameExcludeList = new String[] { "bin/intradoc.cfg", "config/config.cfg", "config/license.cfg", "database/intradoc.mdb", "weblayout/groups" };
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  52 */     this.m_installer = installer;
/*     */ 
/*  54 */     if (name.equals("core-remove-patches"))
/*     */     {
/*  56 */       String[] list = { "classes/intradoc/", "shared/classes/intradoc/", "classes/IdcServer.class", "classes/BatchLoader.class", "classes/IdcServerThread.class", "classes/SystemProperties.class", "etc/intradoc-ms", "etc/intradocms_start", "etc/intradocms_stop" };
/*     */ 
/*  63 */       Stack failStack = new Stack();
/*  64 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/*  66 */         String dir = list[i];
/*  67 */         failStack.push(dir);
/*     */       }
/*     */ 
/*  70 */       String oldDir = installer.m_idcDir + "old/";
/*  71 */       moveDirToNumberedBackup(oldDir);
/*  72 */       createDirectory(oldDir);
/*     */ 
/*  74 */       while (!failStack.empty())
/*     */       {
/*  76 */         String dir = (String)failStack.pop();
/*  77 */         moveDirectoryToOld(dir, failStack);
/*     */       }
/*  79 */       removePatchComponents();
/*     */     }
/*  81 */     else if (name.equals("core-preupdate"))
/*     */     {
/*     */       try
/*     */       {
/*  86 */         remove(installer.computeDestination("config/activestate.hda"), false);
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/*  90 */         if (SystemUtils.m_verbose)
/*     */         {
/*  92 */           Report.debug("install", null, ignore);
/*     */         }
/*     */       }
/*  95 */       String configureAdminServer = installer.getInstallValue("ConfigureAdminServer", null);
/*     */ 
/*  97 */       if (configureAdminServer == null)
/*     */       {
/*  99 */         String adminDir = installer.computeDestinationEx("admin/", false);
/*     */ 
/* 101 */         if ((adminDir != null) && (FileUtils.checkFile(adminDir, false, true) == 0))
/*     */         {
/* 104 */           configureAdminServer = "true";
/*     */         }
/*     */         else
/*     */         {
/* 108 */           configureAdminServer = "false";
/*     */         }
/* 110 */         installer.m_installerConfig.put("ConfigureAdminServer", configureAdminServer);
/*     */       }
/*     */     }
/* 113 */     else if (name.equals("core-postupdate"))
/*     */     {
/* 116 */       String cgiFileName = installer.getConfigValue("CgiFileName");
/* 117 */       if ((cgiFileName != null) && (cgiFileName.equals("iis_idc_cgi.dll")))
/*     */       {
/*     */         try
/*     */         {
/* 121 */           installer.editConfigFile("config/config.cfg", "CgiFileName", "idc_cgi_isapi.dll");
/* 122 */           installer.editConfigFile("admin/config/config.cfg", "CgiFileName", "idc_cgi_isapi.dll");
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 126 */           String msg = LocaleUtils.encodeMessage("csInstallerCgiFilenameUpdateError", e.getMessage());
/*     */ 
/* 128 */           installer.m_installLog.warning(msg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 133 */       String proxyAdmin = installer.getConfigValue("WebProxyAdminServer");
/* 134 */       String isProxied = installer.getConfigValue("IsProxiedServer");
/* 135 */       String type = installer.getInstallValue("ConfigureProxiedServer", "no");
/* 136 */       if ((proxyAdmin == null) && (isProxied == null) && (!StringUtils.convertToBool(type, true)))
/*     */       {
/*     */         try
/*     */         {
/* 141 */           installer.editConfigFile("config/config.cfg", "WebProxyAdminServer", "true");
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 145 */           String msg = LocaleUtils.encodeMessage("csInstallerWebProxyAdminServerUpdateError", e.getMessage());
/*     */ 
/* 148 */           installer.m_installLog.warning(msg);
/*     */         }
/*     */       }
/*     */     }
/* 152 */     return 0;
/*     */   }
/*     */ 
/*     */   public void removePatchComponents()
/*     */   {
/*     */     try
/*     */     {
/* 159 */       Report.trace("install", "removing patch components from '" + this.m_installer.m_idcDir, null);
/*     */ 
/* 161 */       String configDir = this.m_installer.computeDestinationEx("config/", false);
/* 162 */       String editDir = this.m_installer.computeDestinationEx("data/components", false);
/* 163 */       String mediaDir = this.m_installer.getConfigValue("MediaDirectory");
/* 164 */       ComponentListManager.init(this.m_installer.m_idcDir, configDir, editDir, mediaDir);
/* 165 */       String currentVersion = this.m_installer.getInstallValue("ProductVersionToBeInstalled", "unknown");
/*     */ 
/* 168 */       DataBinder binder = new DataBinder();
/* 169 */       ResourceUtils.serializeDataBinder(configDir, "components.hda", binder, false, false);
/*     */ 
/* 171 */       DataResultSet drset = (DataResultSet)binder.getResultSet("Components");
/* 172 */       if (drset != null)
/*     */       {
/* 174 */         FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "name", "location" }, true);
/*     */ 
/* 177 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 179 */           String compName = "(unknown)";
/*     */           try
/*     */           {
/* 182 */             DataBinder compBinder = new DataBinder();
/* 183 */             compName = drset.getStringValue(infos[0].m_index);
/* 184 */             String location = drset.getStringValue(infos[1].m_index);
/* 185 */             if (location.length() != 0)
/*     */             {
/* 192 */               String path = FileUtils.getAbsolutePath(this.m_installer.m_idcDir, location);
/* 193 */               ResourceUtils.serializeDataBinder(FileUtils.getDirectory(path), FileUtils.getName(path), compBinder, false, true);
/*     */ 
/* 195 */               String version = compBinder.getLocal("RemoveAfterVersion");
/* 196 */               if (version == null)
/*     */               {
/* 199 */                 version = compBinder.getLocal("RemoverAfterVersion");
/* 200 */                 if (version != null)
/*     */                 {
/* 202 */                   SystemUtils.reportDeprecatedUsage("component '" + compName + "' had a misnamed RemoverAfterVersion flag");
/*     */                 }
/*     */ 
/*     */               }
/*     */ 
/* 208 */               boolean isDeleted = false;
/* 209 */               if ((version != null) && (version.length() > 0))
/*     */               {
/* 211 */                 int cmp = SystemUtils.compareVersions(version, currentVersion);
/* 212 */                 SystemUtils.trace("install", "comparing component version " + version + " to my version " + currentVersion + " = " + cmp);
/*     */ 
/* 215 */                 if (cmp <= 0)
/*     */                 {
/* 217 */                   ComponentListManager.getEditor().enableOrDisableComponent(compName, false);
/*     */ 
/* 219 */                   isDeleted = true;
/*     */                 }
/*     */               }
/*     */ 
/* 223 */               if (isDeleted)
/*     */               {
/* 225 */                 Report.trace("install", "removed component '" + compName + "' with RemoveAfterVersion '" + version + "'", null);
/*     */               }
/*     */               else
/*     */               {
/* 230 */                 Report.trace("install", "leaving component '" + compName + "' with RemoveAfterVersion '" + version + "'", null);
/*     */               }
/*     */             }
/*     */           }
/*     */           catch (Throwable ignore)
/*     */           {
/* 236 */             Report.trace("install", "unable to unregister component '" + compName + "' from server at '" + this.m_installer.m_idcDir + "'", ignore);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/* 245 */       Report.trace("install", "unable to unregister components from server at '" + this.m_installer.m_idcDir + "'", ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void moveDirectoryToOld(String dir, Stack failures)
/*     */     throws ServiceException
/*     */   {
/* 254 */     String fullDir = this.m_installer.computeDestinationEx(dir, false);
/* 255 */     if (!exists(fullDir))
/*     */       return;
/* 257 */     String[] list = list(fullDir);
/* 258 */     if (list == null)
/*     */     {
/* 260 */       this.m_installer.copyFile(fullDir, fullDir + ".old", null, 0L, 0L);
/*     */       try
/*     */       {
/* 264 */         remove(fullDir, false);
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/*     */         try
/*     */         {
/* 270 */           remove(fullDir + ".old", false);
/*     */         }
/*     */         catch (ServiceException ignore2)
/*     */         {
/* 274 */           if (SystemUtils.m_verbose)
/*     */           {
/* 276 */             Report.debug("install", null, ignore2);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 283 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/* 285 */         String name = list[i];
/* 286 */         boolean skip = false;
/* 287 */         String oldName = fullDir + name;
/* 288 */         String newSuffix = dir + name;
/* 289 */         String newDir = this.m_installer.m_idcDir + "old/";
/* 290 */         String newName = newDir + newSuffix;
/* 291 */         newDir = newDir + dir;
/*     */ 
/* 293 */         for (int j = 0; j < this.m_renameExcludeList.length; ++j)
/*     */         {
/* 295 */           if (!this.m_renameExcludeList[j].equalsIgnoreCase(newSuffix))
/*     */             continue;
/* 297 */           skip = true;
/* 298 */           break;
/*     */         }
/*     */ 
/* 302 */         if (skip)
/*     */           continue;
/* 304 */         if (!exists(newDir))
/*     */         {
/* 306 */           createDirectory(newDir);
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 311 */           rename(oldName, newName);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 315 */           failures.push(newSuffix);
/*     */         }
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 321 */         remove(fullDir, false);
/*     */       }
/*     */       catch (ServiceException ignore)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void moveDirToNumberedBackup(String dir)
/*     */     throws ServiceException
/*     */   {
/* 333 */     dir = FileUtils.directorySlashesEx(dir, false);
/* 334 */     if (dir.endsWith("/"))
/*     */     {
/* 336 */       dir = dir.substring(0, dir.length() - 1);
/*     */     }
/*     */ 
/* 339 */     if (isEmpty(dir))
/*     */     {
/* 341 */       FileUtils.deleteDirectory(new File(dir), true);
/*     */     }
/*     */ 
/* 344 */     if (!exists(dir))
/*     */       return;
/* 346 */     boolean finished = false;
/*     */ 
/* 348 */     for (int i = 1; i < 10; ++i)
/*     */     {
/* 350 */       String newName = dir + i;
/* 351 */       if (exists(newName))
/*     */         continue;
/* 353 */       rename(dir, newName);
/* 354 */       finished = true;
/* 355 */       break;
/*     */     }
/*     */ 
/* 359 */     if (finished)
/*     */       return;
/* 361 */     remove(dir + 1, true);
/* 362 */     for (int i = 1; i < 9; ++i)
/*     */     {
/* 364 */       rename(dir + (i + 1), dir + i);
/*     */     }
/* 366 */     rename(dir, dir + 9);
/*     */   }
/*     */ 
/*     */   public boolean isEmpty(String dir)
/*     */   {
/* 376 */     File f = new File(dir);
/* 377 */     return (f.isDirectory()) && (f.list().length == 0);
/*     */   }
/*     */ 
/*     */   public void rename(String f1, String f2) throws ServiceException
/*     */   {
/* 382 */     File fo1 = new File(f1);
/* 383 */     File fo2 = new File(f2);
/* 384 */     if (fo1.renameTo(fo2))
/*     */       return;
/* 386 */     throw new ServiceException(LocaleUtils.encodeMessage("syUnableToRename", null, fo1.getAbsolutePath(), fo2.getAbsolutePath()));
/*     */   }
/*     */ 
/*     */   public boolean exists(String name)
/*     */   {
/* 393 */     File f = new File(name);
/* 394 */     return f.exists();
/*     */   }
/*     */ 
/*     */   public void remove(String name, boolean recursive) throws ServiceException
/*     */   {
/* 399 */     File f = new File(name);
/* 400 */     if ((f.isDirectory()) && (recursive))
/*     */     {
/* 402 */       String[] list = f.list();
/* 403 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/* 405 */         remove(list[i], recursive);
/*     */       }
/*     */     }
/*     */ 
/* 409 */     if (f.delete())
/*     */       return;
/* 411 */     throw new ServiceException(LocaleUtils.encodeMessage("syUnableToDeleteFile", null, f.getAbsolutePath()));
/*     */   }
/*     */ 
/*     */   public void createDirectory(String name)
/*     */     throws ServiceException
/*     */   {
/* 418 */     File f = new File(name);
/* 419 */     if (f.mkdirs())
/*     */       return;
/* 421 */     String msg = LocaleUtils.encodeMessage("syUnableToCreateFile", null, f.getAbsolutePath());
/*     */ 
/* 423 */     throw new ServiceException(msg);
/*     */   }
/*     */ 
/*     */   public String[] list(String dir)
/*     */   {
/* 429 */     File f = new File(dir);
/* 430 */     return f.list();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 435 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 67809 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CoreUpdate
 * JD-Core Version:    0.5.4
 */