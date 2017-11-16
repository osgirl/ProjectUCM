/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CoreLaunchers
/*     */   implements SectionInstaller
/*     */ {
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  34 */     boolean isRefinery = installer.getInstallBool("IsRefinery", false);
/*  35 */     for (int i = 0; i < installer.m_myPlatforms.length; ++i)
/*     */     {
/*  37 */       String platform = installer.m_myPlatforms[i];
/*  38 */       String productString = installer.getInstallValue("LauncherProductString", null);
/*  39 */       if (productString == null)
/*     */       {
/*  41 */         productString = (isRefinery) ? "refinery" : "master";
/*     */       }
/*  43 */       Properties launcherConfig = installer.getInstallerTable("Launchers", productString + "-" + platform);
/*     */ 
/*  45 */       if (launcherConfig == null)
/*     */       {
/*  47 */         Report.trace("install", "no launcher definition for " + productString + "-" + platform, null);
/*     */       }
/*     */ 
/*  50 */       installLaunchers(installer, launcherConfig, new Properties(), platform, false);
/*     */ 
/*  52 */       boolean configureAdminServerDefault = installer.getInstallValue("ConfigureAdminServer", null) == null;
/*     */ 
/*  54 */       boolean configureAdminServer = installer.getInstallBool("ConfigureAdminServer", configureAdminServerDefault);
/*     */ 
/*  56 */       if (!configureAdminServer) {
/*     */         continue;
/*     */       }
/*  59 */       launcherConfig = installer.getInstallerTable("Launchers", productString + "-admin-" + platform);
/*     */ 
/*  61 */       installLaunchers(installer, launcherConfig, new Properties(), platform, false);
/*     */     }
/*     */ 
/*  65 */     return 0;
/*     */   }
/*     */ 
/*     */   public static void installLaunchers(SysInstaller installer, Properties launcherConfig, Properties pathMap, String platform, boolean skipIfMapMissing)
/*     */     throws ServiceException
/*     */   {
/*  74 */     boolean useCopies = false;
/*  75 */     if (EnvUtils.getOSFamily().equals("windows"))
/*     */     {
/*  77 */       useCopies = true;
/*     */     }
/*     */     else
/*     */     {
/*  81 */       String useCopiesString = installer.getInstallValue("CopyLaunchers", "false");
/*     */ 
/*  83 */       useCopies = StringUtils.convertToBool(useCopiesString, false);
/*     */     }
/*     */ 
/*  86 */     Vector launcherList = installer.parseArrayTrim(launcherConfig, "Launchers");
/*     */ 
/*  89 */     NativeOsUtils utils = installer.m_utils;
/*  90 */     String symLinkCommandTemplate = null;
/*  91 */     if (!useCopies)
/*     */     {
/*  93 */       Properties linkProperties = installer.getInstallerTable("LinkExecutables", installer.m_platform);
/*     */ 
/*  95 */       if (linkProperties == null)
/*     */       {
/*  97 */         Report.trace("install", "LinkExecutables not defined for " + platform, null);
/*     */ 
/*  99 */         symLinkCommandTemplate = "ln \"${source}\" \"${target}\"";
/*     */       }
/*     */       else
/*     */       {
/* 103 */         symLinkCommandTemplate = linkProperties.getProperty("SymLink");
/*     */       }
/*     */     }
/* 106 */     String[][] pathMapArray = SysInstaller.propertiesAsDoubleArray(pathMap);
/* 107 */     IdcStringBuilder configEntryValue = new IdcStringBuilder();
/* 108 */     boolean isAdmin = false;
/* 109 */     for (int i = 0; i < launcherList.size(); ++i)
/*     */     {
/* 111 */       String launcherPathsString = (String)launcherList.elementAt(i);
/* 112 */       Vector launcherPaths = StringUtils.parseArray(launcherPathsString, ',', '^');
/*     */ 
/* 114 */       String targetPath = (String)launcherPaths.elementAt(0);
/* 115 */       String launcherPath = (String)launcherPaths.elementAt(1);
/* 116 */       String launcherName = targetPath;
/* 117 */       int index1 = launcherName.lastIndexOf(47);
/* 118 */       if ((launcherName.indexOf("bin") >= 0) && (index1 > 0))
/*     */       {
/* 121 */         if (launcherName.startsWith("admin"))
/*     */         {
/* 123 */           isAdmin = true;
/*     */         }
/* 125 */         int index2 = launcherName.indexOf(46, index1 + 1);
/* 126 */         if (index2 > 0)
/*     */         {
/* 128 */           launcherName = launcherName.substring(index1 + 1, index2);
/*     */         }
/*     */         else
/*     */         {
/* 132 */           launcherName = launcherName.substring(index1 + 1);
/*     */         }
/* 134 */         if ((!launcherName.equals("Launcher")) && (launcherPath.indexOf("Launcher") >= 0))
/*     */         {
/* 136 */           if (configEntryValue.length() > 0)
/*     */           {
/* 138 */             configEntryValue.append2(',', launcherName);
/*     */           }
/*     */           else
/*     */           {
/* 142 */             configEntryValue.append(launcherName);
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 148 */         Report.trace("install", "unable to compute launcher name for " + launcherName, null);
/*     */       }
/* 150 */       boolean found = false;
/* 151 */       for (int j = 0; j < pathMapArray.length; ++j)
/*     */       {
/* 153 */         if (!targetPath.startsWith(pathMapArray[j][0]))
/*     */           continue;
/* 155 */         found = true;
/* 156 */         String newPrefix = pathMapArray[j][1];
/* 157 */         if (newPrefix.length() <= 0)
/*     */           break;
/* 159 */         targetPath = newPrefix + targetPath.substring(pathMapArray[j][0].length()); break;
/*     */       }
/*     */ 
/* 165 */       if ((!found) && (skipIfMapMissing)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 169 */       if (useCopies)
/*     */       {
/* 171 */         targetPath = installer.computeDestinationEx(targetPath, true);
/*     */ 
/* 173 */         if (launcherPath.indexOf("/") == -1)
/*     */         {
/* 175 */           int index = targetPath.lastIndexOf("/");
/* 176 */           launcherPath = targetPath.substring(0, index) + "/" + launcherPath;
/*     */         }
/*     */ 
/* 179 */         launcherPath = installer.computeDestinationEx(launcherPath, false);
/* 180 */         Report.trace("install", "installing a launcher from " + launcherPath + " to " + targetPath, null);
/*     */ 
/* 182 */         File targetFile = new File(targetPath);
/* 183 */         targetFile.delete();
/* 184 */         installer.copyRecursiveEx(launcherPath, targetPath, null, null, 0L, 1L, 1, 2);
/*     */       }
/*     */       else
/*     */       {
/* 190 */         if (launcherPath.indexOf("/") > 0)
/*     */         {
/* 192 */           launcherPath = installer.computeDestinationEx(launcherPath, false);
/* 193 */           Report.trace("install", "using computed launcher source " + launcherPath, null);
/*     */ 
/* 196 */           if (launcherPath.startsWith(installer.m_targetDir))
/*     */           {
/* 198 */             int index = installer.m_targetDir.length();
/* 199 */             if (launcherPath.charAt(index) == '/')
/*     */             {
/* 201 */               ++index;
/*     */             }
/* 203 */             launcherPath = launcherPath.substring(index);
/* 204 */             int count = 0;
/* 205 */             for (int j = 0; j < targetPath.length(); ++j)
/*     */             {
/* 207 */               if (targetPath.charAt(j) != '/')
/*     */                 continue;
/* 209 */               ++count;
/*     */             }
/*     */ 
/* 212 */             for (int j = 0; j < count; ++j)
/*     */             {
/* 214 */               launcherPath = "../" + launcherPath;
/*     */             }
/*     */           }
/*     */         }
/* 218 */         targetPath = installer.computeDestination(targetPath);
/* 219 */         File targetFile = new File(targetPath);
/*     */ 
/* 222 */         targetFile.delete();
/*     */ 
/* 224 */         String msg = LocaleUtils.encodeMessage("csInstallerSymlinkError", null, launcherPath, targetPath);
/*     */ 
/* 226 */         Report.trace("install", "linking " + launcherPath + "\" to \"" + targetPath + "\".", null);
/*     */ 
/* 229 */         Vector results = new IdcVector();
/*     */         int rc;
/*     */         int rc;
/* 230 */         if (utils == null)
/*     */         {
/* 232 */           Properties props = new Properties();
/* 233 */           props.put("source", launcherPath);
/* 234 */           props.put("target", targetPath);
/* 235 */           String commandLine = installer.substituteVariables(symLinkCommandTemplate, props);
/*     */ 
/* 237 */           String[] cmdLine = installer.parseCommandLine(commandLine);
/* 238 */           rc = installer.runCommand(cmdLine, results, msg, false, true);
/*     */         }
/*     */         else
/*     */         {
/* 242 */           Report.trace("install", "calling symlink(\"" + launcherPath + "\", \"" + targetPath + "\".", null);
/*     */ 
/* 244 */           rc = utils.symlink(launcherPath, targetPath);
/* 245 */           results.add(utils.getErrorMessage(rc));
/*     */         }
/*     */ 
/* 248 */         if (rc == 0)
/*     */           continue;
/* 250 */         for (String result : results)
/*     */         {
/* 252 */           msg = LocaleUtils.appendMessage(result, msg);
/*     */         }
/* 254 */         installer.m_installLog.warning(msg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 259 */     boolean isUpdate = !installer.getInstallBool("NoCfgActivity", false);
/* 260 */     if ((configEntryValue.length() <= 0) || (!isUpdate))
/*     */       return;
/* 262 */     if (isAdmin)
/*     */     {
/*     */       try
/*     */       {
/* 266 */         installer.editConfigFile("admin/config/config.cfg", "LAUNCHERS_system", configEntryValue.toString());
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 272 */         Report.trace("install", "unable to set LAUNCHERS_system in admin/config/config.cfg", e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 278 */       installer.editConfigFile("config/config.cfg", "LAUNCHERS_system", configEntryValue.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 287 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CoreLaunchers
 * JD-Core Version:    0.5.4
 */