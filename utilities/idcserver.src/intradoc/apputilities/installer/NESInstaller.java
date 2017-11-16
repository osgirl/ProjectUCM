/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileReader;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class NESInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public int m_loadModulesLine;
/*     */   public int m_defaultObjectStart;
/*     */   public int m_idcInitLine;
/*     */   public int m_idcCgiLine;
/*     */   public int m_idcAliasLine;
/*     */   public int m_idcObjectStart;
/*     */   public int m_idcObjectFinish;
/*     */   public int m_idcCgiObjectStart;
/*     */   public int m_idcCgiObjectFinish;
/*     */   protected SysInstaller m_installer;
/*     */   protected String m_weblayoutDir;
/*     */   protected boolean m_netscape6;
/*     */ 
/*     */   public NESInstaller()
/*     */   {
/*  32 */     this.m_loadModulesLine = -1;
/*  33 */     this.m_defaultObjectStart = -1;
/*  34 */     this.m_idcInitLine = -1;
/*  35 */     this.m_idcCgiLine = -1;
/*  36 */     this.m_idcAliasLine = -1;
/*  37 */     this.m_idcObjectStart = -1;
/*  38 */     this.m_idcObjectFinish = -1;
/*  39 */     this.m_idcCgiObjectStart = -1;
/*  40 */     this.m_idcCgiObjectFinish = -1;
/*     */ 
/*  44 */     this.m_netscape6 = false;
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  50 */     this.m_installer = installer;
/*  51 */     String webServer = this.m_installer.getInstallValue("WebServer", "");
/*  52 */     if ((!webServer.equals("nes")) && (!webServer.equals("nes6")) && (!webServer.equals("sunone")))
/*     */     {
/*  57 */       return 0;
/*     */     }
/*     */ 
/*  60 */     if (this.m_installer.isOlderVersion("4"))
/*     */     {
/*  62 */       this.m_installer.editConfigFile("config/config.cfg", "CgiFileName", "nph-idc_cgi.exe");
/*     */ 
/*  64 */       if (this.m_installer.getInstallBool("ConfigureAdminServer", true))
/*     */       {
/*  66 */         this.m_installer.editConfigFile("admin/config/config.cfg", "CgiFileName", "nph-idc_cgi.exe");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  71 */     this.m_netscape6 = ((webServer.equals("nes6")) || (webServer.equals("sunone")));
/*  72 */     this.m_installer = installer;
/*  73 */     this.m_weblayoutDir = this.m_installer.computeDestination("weblayout/");
/*  74 */     this.m_weblayoutDir = this.m_weblayoutDir.substring(0, this.m_weblayoutDir.length() - 1);
/*     */ 
/*  76 */     if (disposition.equals("always"))
/*     */     {
/*  78 */       installer.m_installLog.notice("!csInstallerSunOneConfigMsg");
/*     */ 
/*  80 */       String path = installer.getInstallValue("SunOnePath", installer.getInstallValue("NetscapePath", null));
/*     */ 
/*  83 */       if ((path == null) || (path.length() == 0))
/*     */       {
/*  85 */         installer.m_installLog.error("!csInstallerSunOnePathError");
/*  86 */         return 0;
/*     */       }
/*  88 */       path = FileUtils.directorySlashes(path);
/*     */ 
/*  90 */       String platform = installer.m_installerConfig.getProperty("Platform");
/*  91 */       if ((platform == null) || (platform.length() == 0))
/*     */       {
/*  93 */         installer.m_installLog.error("!csInstallerSunOnePlatformError");
/*  94 */         return 0;
/*     */       }
/*     */ 
/*  97 */       String configFile = path + "config/obj.conf";
/*  98 */       String magnusFile = path + "config/magnus.conf";
/*  99 */       if (FileUtils.checkFile(configFile, true, false) != 0)
/*     */       {
/* 101 */         installer.m_installLog.error(LocaleUtils.encodeMessage("csInstallerSunOneObjConfFileNotFound", null, FileUtils.getAbsolutePath(configFile)));
/*     */ 
/* 104 */         return 0;
/*     */       }
/*     */ 
/* 107 */       String workDir = installer.computeDestination("install");
/* 108 */       FileUtils.copyFile(configFile, workDir + "/obj.conf.orig");
/* 109 */       if (this.m_netscape6)
/*     */       {
/* 111 */         FileUtils.copyFile(magnusFile, workDir + "/magnus.conf.orig");
/*     */       }
/*     */ 
/* 114 */       Vector lines = readFile(configFile);
/* 115 */       if (lines.size() == 0)
/*     */       {
/* 117 */         installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerSunOneFileParseError", null, configFile));
/*     */ 
/* 120 */         return 0;
/*     */       }
/*     */ 
/* 123 */       parseConfigFile(lines);
/* 124 */       String soExtension = this.m_installer.getInstallerTableValue("PlatformConfigTable", platform, "SharedObjectSuffix");
/*     */ 
/* 127 */       String plugin = "${IdcHomeDir}/native/" + platform + "/lib/IdcNSAuth." + soExtension;
/*     */ 
/* 133 */       if (this.m_idcObjectStart < 0)
/*     */       {
/* 136 */         setLine(lines, -1, "<Object ppath=\"" + this.m_weblayoutDir + "/*\">");
/* 137 */         setLine(lines, -1, "NameTrans fn=\"idcNameTrans\"");
/* 138 */         setLine(lines, -1, "PathCheck fn=\"idcPathCheck\"");
/* 139 */         setLine(lines, -1, "Service fn=\"idcService\"");
/* 140 */         setLine(lines, -1, "</Object>");
/*     */       }
/*     */ 
/* 143 */       if (this.m_idcAliasLine < 0)
/*     */       {
/* 145 */         int line = (this.m_idcCgiLine > 0) ? this.m_idcCgiLine + 1 : this.m_defaultObjectStart + 1;
/*     */ 
/* 147 */         String webRoot = installer.getConfigValue("HttpRelativeWebRoot");
/* 148 */         webRoot = webRoot.substring(0, webRoot.length() - 1);
/* 149 */         lines.insertElementAt("NameTrans fn=\"pfx2dir\" from=\"" + webRoot + "\" dir=\"" + this.m_weblayoutDir + "\"", line);
/*     */       }
/*     */ 
/* 153 */       String idcInitLine = "Init fn=\"idcInit\" idocdb=\"" + installer.computeDestination("data/users/userdb.txt") + "\"";
/*     */ 
/* 155 */       String loadModLine = "Init fn=\"load-modules\" funcs=\"idcInit,idcNameTrans,idcPathCheck,idcService\" shlib=\"" + installer.computeDestination(plugin) + "\"";
/*     */ 
/* 157 */       Vector magnusLines = null;
/* 158 */       if (this.m_netscape6)
/*     */       {
/* 160 */         magnusLines = readFile(magnusFile);
/* 161 */         boolean initLineDone = false;
/* 162 */         boolean loadModLineDone = false;
/* 163 */         for (int i = 0; i < magnusLines.size(); ++i)
/*     */         {
/* 165 */           String line = (String)magnusLines.elementAt(i);
/* 166 */           if (line.startsWith("Init fn=\"idcInit\""))
/*     */           {
/* 168 */             initLineDone = true;
/*     */           }
/*     */           else {
/* 171 */             if ((!line.startsWith("Init fn=\"load-modules\"")) || (line.indexOf("idcNameTrans") <= 0))
/*     */               continue;
/* 173 */             loadModLineDone = true;
/*     */           }
/*     */         }
/*     */ 
/* 177 */         if (!loadModLineDone)
/*     */         {
/* 179 */           magnusLines.addElement(loadModLine);
/*     */         }
/* 181 */         if (!initLineDone)
/*     */         {
/* 183 */           magnusLines.addElement(idcInitLine);
/*     */         }
/*     */ 
/*     */       }
/* 188 */       else if ((this.m_idcInitLine < 0) && (this.m_loadModulesLine < 0))
/*     */       {
/* 190 */         lines.insertElementAt(idcInitLine, this.m_defaultObjectStart);
/* 191 */         lines.insertElementAt(loadModLine, this.m_defaultObjectStart);
/*     */       }
/* 193 */       else if (this.m_loadModulesLine >= 0)
/*     */       {
/* 196 */         setLine(lines, this.m_loadModulesLine, loadModLine);
/*     */       }
/*     */ 
/* 200 */       writeFile(workDir + "/obj.conf", lines);
/* 201 */       if (this.m_netscape6)
/*     */       {
/* 203 */         writeFile(workDir + "/magnus.conf", magnusLines);
/*     */       }
/* 205 */       if (this.m_installer.isWindows())
/*     */       {
/* 207 */         String targetFile = configFile;
/* 208 */         String msg = "csInstallerSunOneObjConfCopyError";
/*     */         try
/*     */         {
/* 211 */           FileUtils.copyFile(workDir + "/obj.conf", targetFile);
/* 212 */           if (this.m_netscape6)
/*     */           {
/* 214 */             targetFile = magnusFile;
/* 215 */             msg = "csInstallerSunOneMagnusConfCopyError";
/* 216 */             FileUtils.copyFile(workDir + "/magnus.conf", targetFile);
/*     */           }
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 221 */           installer.m_installLog.error(LocaleUtils.encodeMessage(msg, null, targetFile));
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 227 */         installer.m_installLog.notice(LocaleUtils.encodeMessage("csInstallerSunOneInstallCopyError", null, configFile));
/*     */ 
/* 230 */         installer.m_installLog.notice(LocaleUtils.encodeMessage("csInstallerSunOneInstallMagnusCopyError", null, magnusFile));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 235 */     return 0;
/*     */   }
/*     */ 
/*     */   protected Vector readFile(String path) throws ServiceException
/*     */   {
/* 240 */     BufferedReader r = null;
/*     */     try
/*     */     {
/* 243 */       r = new BufferedReader(new FileReader(path));
/*     */ 
/* 245 */       Vector v = new IdcVector();
/*     */ 
/* 247 */       while ((line = r.readLine()) != null)
/*     */       {
/*     */         String line;
/* 249 */         v.addElement(line);
/*     */       }
/* 251 */       Vector localVector1 = v;
/*     */ 
/* 263 */       return localVector1; } catch (FileNotFoundException e) { } catch (IOException e) { } finally { FileUtils.closeObject(r); }
/*     */ 
/*     */   }
/*     */ 
/*     */   protected void writeFile(String path, Vector lines)
/*     */     throws ServiceException
/*     */   {
/* 270 */     BufferedWriter w = null;
/*     */     try
/*     */     {
/* 273 */       w = new BufferedWriter(new FileWriter(path));
/* 274 */       int size = lines.size();
/* 275 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 277 */         String line = (String)lines.elementAt(i);
/* 278 */         w.write(line);
/* 279 */         w.newLine();
/*     */       }
/*     */ 
/* 282 */       w.flush();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 290 */       FileUtils.closeObject(w);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void parseConfigFile(Vector lines) throws ServiceException
/*     */   {
/* 296 */     int count = lines.size();
/* 297 */     String masterCgiDir1 = this.m_installer.getInstallValue("MasterServerDir", this.m_installer.m_idcDir) + "/intradoc-cgi";
/*     */ 
/* 300 */     String masterCgiDir2 = this.m_installer.getInstallValue("MasterServerDir", this.m_installer.m_idcDir) + "/idcplg";
/*     */ 
/* 303 */     masterCgiDir1 = FileUtils.directorySlashesEx(masterCgiDir1, false);
/* 304 */     masterCgiDir2 = FileUtils.directorySlashesEx(masterCgiDir2, false);
/* 305 */     masterCgiDir1 = masterCgiDir1.toLowerCase();
/* 306 */     masterCgiDir2 = masterCgiDir2.toLowerCase();
/* 307 */     String lowercaseWeblayoutDir = this.m_weblayoutDir.toLowerCase();
/*     */ 
/* 309 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 311 */       String line = (String)lines.elementAt(i);
/* 312 */       String lowercaseLine = line.toLowerCase();
/*     */ 
/* 314 */       if (line.indexOf("IdcNSAuth") > 0)
/*     */       {
/* 316 */         this.m_loadModulesLine = i;
/* 317 */         label472: label356: this.m_idcInitLine = (i + 1);
/*     */       }
/* 321 */       else if ((lowercaseLine.indexOf("dir=\"" + masterCgiDir1 + "\"") > 0) || (lowercaseLine.indexOf("dir=\"" + masterCgiDir2 + "\"") > 0))
/*     */       {
/* 324 */         this.m_idcCgiLine = i;
/*     */       }
/* 328 */       else if (line.indexOf("dir=\"" + lowercaseWeblayoutDir + "\"") > 0)
/*     */       {
/* 330 */         this.m_idcAliasLine = i;
/*     */       }
/* 334 */       else if (lowercaseLine.indexOf("ppath=\"" + lowercaseWeblayoutDir + "/*\"") > 0)
/*     */       {
/* 337 */         this.m_idcObjectStart = i;
/*     */         do { if (i >= count)
/*     */             break label356;
/* 340 */           line = (String)lines.elementAt(i++); }
/* 341 */         while (!line.startsWith("</Object>"));
/*     */ 
/* 343 */         --i;
/*     */ 
/* 347 */         this.m_idcObjectFinish = i;
/*     */       }
/* 351 */       else if ((lowercaseLine.indexOf("ppath=\"" + masterCgiDir1 + "/*\"") > 0) || (lowercaseLine.indexOf("ppath=\"" + masterCgiDir2 + "/*\"") > 0))
/*     */       {
/* 354 */         this.m_idcCgiObjectStart = i;
/*     */         do { if (i >= count)
/*     */             break label472;
/* 357 */           line = (String)lines.elementAt(i++); }
/* 358 */         while (!line.startsWith("</Object>"));
/*     */ 
/* 360 */         --i;
/*     */ 
/* 364 */         this.m_idcCgiObjectFinish = i;
/*     */       }
/*     */       else
/*     */       {
/* 368 */         if ((!line.startsWith("<Object name=\"default\">")) && (!line.startsWith("<Object name=default>")))
/*     */           continue;
/* 370 */         this.m_defaultObjectStart = i;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setLine(Vector lines, int index, String line)
/*     */   {
/* 378 */     if (index >= 0)
/*     */     {
/* 380 */       lines.setElementAt(line, index);
/*     */     }
/*     */     else
/*     */     {
/* 384 */       lines.addElement(line);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 390 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.NESInstaller
 * JD-Core Version:    0.5.4
 */