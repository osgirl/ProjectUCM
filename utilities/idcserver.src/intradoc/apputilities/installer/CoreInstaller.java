/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.Socket;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CoreInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public SysInstaller m_installer;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  39 */     this.m_installer = installer;
/*  40 */     boolean isUpdate = disposition.equals("update");
/*     */ 
/*  42 */     if ((name.equals("core-install")) || (name.equals("proxy-install")) || (name.equals("refinery-install")))
/*     */     {
/*  46 */       boolean createAdminServerDirs = installer.getInstallBool("ConfigureAdminServer", true);
/*     */ 
/*  49 */       String idcDir = installer.getConfigValue("IntradocDir");
/*     */ 
/*  51 */       if (idcDir == null)
/*     */       {
/*  53 */         throw new ServiceException("!csInstallerIntradocDirUndefined");
/*     */       }
/*     */ 
/*  56 */       idcDir = FileUtils.directorySlashes(idcDir);
/*  57 */       String[] list = { "queue/conversion", "data/subjects", "data/users", "data/users/config", "weblayout/groups/public/pages", "weblayout/groups/secure/pages", "weblayout/groups/secure/logs", (createAdminServerDirs) ? "admin/data" : null, (createAdminServerDirs) ? "admin/data/servers" : null };
/*     */ 
/*  67 */       for (int i = 0; i < list.length; ++i)
/*     */       {
/*  69 */         if (list[i] == null)
/*     */           continue;
/*  71 */         installer.verifyDirectoryWithException(list[i], 99, null);
/*     */       }
/*     */ 
/*     */     }
/*  75 */     else if (name.equals("core-check"))
/*     */     {
/*  77 */       if (disposition.startsWith("new"))
/*     */       {
/*  79 */         String cfgFile = installer.computeDestination("config/config.cfg");
/*  80 */         File file = new File(cfgFile);
/*  81 */         if (file.exists())
/*     */         {
/*  83 */           boolean throwError = true;
/*  84 */           boolean isRetry = installer.getInstallBool("IsRetry", false);
/*  85 */           if (isRetry)
/*     */           {
/*  87 */             String lockFile = installer.m_targetDir + "/install/lock.txt";
/*  88 */             if (FileUtils.checkFile(lockFile, true, true) == 0)
/*     */             {
/*  90 */               Report.trace("install", "overwriting partial content server install.", null);
/*     */ 
/*  92 */               throwError = false;
/*     */             }
/*     */           }
/*  95 */           if (throwError)
/*     */           {
/*  97 */             throw new ServiceException(null, "csInstallerInstanceExists", new Object[0]);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 102 */       if (isUpdate)
/*     */       {
/* 106 */         String filterName = installer.getConfigValue("CgiFileName");
/* 107 */         if (filterName == null)
/*     */         {
/* 109 */           filterName = "idc_cgi_isapi.dll";
/*     */         }
/*     */         else
/*     */         {
/* 113 */           int index = filterName.indexOf("/");
/* 114 */           if (index >= 0)
/*     */           {
/* 116 */             filterName = filterName.substring(0, index);
/*     */           }
/*     */         }
/*     */ 
/* 120 */         String filterPath = installer.getConfigValue("HttpRelativeCgiRoot");
/* 121 */         if (filterPath == null)
/*     */         {
/* 123 */           if (installer.isOlderVersion("7.0"))
/*     */           {
/* 125 */             filterPath = installer.computeDestinationEx("idcplg/" + filterName, false);
/*     */           }
/*     */           else
/*     */           {
/* 130 */             filterPath = installer.computeDestinationEx("intradoc-cgi/" + filterName, false);
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 136 */           filterPath = installer.getConfigValue("IntradocDir") + "/" + filterPath + "/" + filterName;
/*     */         }
/*     */ 
/* 139 */         filterPath = FileUtils.fileSlashes(filterPath);
/* 140 */         String testPath = filterPath + ".tmp";
/*     */         try
/*     */         {
/* 143 */           File file = new File(testPath);
/* 144 */           file.delete();
/* 145 */           file = new File(filterPath);
/* 146 */           if (file.exists())
/*     */           {
/* 148 */             FileUtils.copyFile(filterPath, testPath);
/* 149 */             file.delete();
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/*     */         }
/*     */         finally
/*     */         {
/*     */           File file;
/*     */           String msg;
/*     */           try
/*     */           {
/* 162 */             FileUtils.renameFile(testPath, filterPath);
/*     */           }
/*     */           catch (ServiceException ignore)
/*     */           {
/* 166 */             if (SystemUtils.m_verbose)
/*     */             {
/* 168 */               Report.debug("system", null, ignore);
/*     */             }
/*     */           }
/* 171 */           File file = new File(testPath);
/* 172 */           file.delete();
/*     */         }
/*     */       }
/*     */     }
/* 176 */     else if (name.equals("core-check-running"))
/*     */     {
/* 179 */       if (isUpdate)
/*     */       {
/* 182 */         String queryScript = installer.m_idcDir + "/etc/idcserver_query";
/* 183 */         if (FileUtils.checkFile(queryScript, true, false) == 0)
/*     */         {
/* 185 */           checkRunning(queryScript, "!csInstallerContentServerRunning");
/*     */         }
/*     */ 
/* 188 */         queryScript = installer.m_idcDir + "/admin/etc/idcadmin_query";
/* 189 */         if (FileUtils.checkFile(queryScript, true, false) == 0)
/*     */         {
/* 191 */           checkRunning(queryScript, "!csInstallerAdminServerRunning");
/*     */         }
/*     */       }
/*     */ 
/* 195 */       String port = installer.getInstallValue("IntradocServerPort", null);
/* 196 */       if (port == null)
/*     */       {
/* 198 */         installer.m_installLog.warning("!csInstallerIntradocPortUndefined");
/*     */       }
/*     */       else
/*     */       {
/* 202 */         int portNbr = NumberUtils.parseInteger(port, 0);
/* 203 */         if (portNbr < 0)
/*     */         {
/* 205 */           installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerIntradocPortInvalid", null, port));
/*     */         }
/*     */         else
/*     */         {
/* 210 */           boolean portOkay = true;
/* 211 */           String msg = LocaleUtils.encodeMessage("csInstallerIntradocPortInUse", null, "" + portNbr);
/*     */           try
/*     */           {
/* 214 */             new Socket("localhost", portNbr);
/* 215 */             portOkay = !isUpdate;
/*     */           }
/*     */           catch (UnknownHostException e)
/*     */           {
/* 219 */             portOkay = false;
/* 220 */             msg = LocaleUtils.encodeMessage("csInstallerIntradocPortError", null, "" + portNbr, e.getMessage());
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 225 */             portOkay = true;
/*     */           }
/* 227 */           if ((!portOkay) && (msg != null))
/*     */           {
/* 229 */             throw new ServiceException(msg);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 234 */     return 0;
/*     */   }
/*     */ 
/*     */   public void checkRunning(String script, String msg) throws ServiceException
/*     */   {
/* 239 */     String[][] commandLines = { { script }, { "sh", script } };
/*     */ 
/* 244 */     boolean success = false;
/* 245 */     Vector results = null;
/* 246 */     for (int i = 0; i < commandLines.length; ++i)
/*     */     {
/* 248 */       results = new IdcVector();
/*     */       try
/*     */       {
/* 251 */         if (this.m_installer.runCriticalCommand(commandLines[i], results, true) == 0)
/*     */         {
/* 253 */           success = true;
/* 254 */           break label103:
/*     */         }
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 259 */         throw new ServiceException("!csInstallerUnableToCheckServer", e);
/*     */       }
/*     */     }
/*     */ 
/* 263 */     if (success)
/*     */     {
/* 265 */       for (int i = 0; i < results.size(); ++i)
/*     */       {
/* 267 */         label103: String line = (String)results.elementAt(i);
/* 268 */         line = line.toLowerCase();
/* 269 */         String statusPrefix = LocaleResources.getString("csStatus", null);
/* 270 */         int index = line.indexOf(statusPrefix);
/* 271 */         if (index < 0)
/*     */           continue;
/* 273 */         String stat = line.substring(index + statusPrefix.length());
/* 274 */         if (stat.indexOf("stopped") != -1)
/*     */           continue;
/* 276 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 283 */       throw new ServiceException(LocaleUtils.encodeMessage("csInstallerUnableToExecScript", null, script));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 290 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76561 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CoreInstaller
 * JD-Core Version:    0.5.4
 */