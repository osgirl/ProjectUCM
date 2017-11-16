/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class InstallJvm
/*     */   implements SectionInstaller
/*     */ {
/*     */   protected SysInstaller m_installer;
/*     */   protected boolean m_isRefinery;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  37 */     this.m_installer = installer;
/*  38 */     this.m_isRefinery = this.m_installer.getInstallBool("IsRefinery", false);
/*     */ 
/*  46 */     String installJvm = installer.getInstallValue("InstallJvm", "default");
/*     */ 
/*  48 */     String useJDK = installer.getInstallValue("UseJDK", installJvm);
/*     */ 
/*  50 */     installJvm(installer, useJDK, name);
/*     */ 
/*  52 */     String isJdbc = installer.getInstallValue("IsJdbc", "true");
/*     */ 
/*  54 */     if (!StringUtils.convertToBool(isJdbc, true))
/*     */     {
/*  57 */       installer.editConfigFile(getCfgPath(), "UseMicrosoftVM", "true");
/*  58 */       return 0;
/*     */     }
/*  60 */     String driver = installer.getInstallValue("JdbcDriver", null);
/*  61 */     if ((!this.m_isRefinery) && (driver != null) && (driver.equals("com.ms.jdbc.odbc.JdbcOdbcDriver")))
/*     */     {
/*  64 */       installer.editConfigFile("config/config.cfg", "JdbcDriver", "sun.jdbc.odbc.JdbcOdbcDriver");
/*     */     }
/*     */ 
/*  68 */     if (installJvm.equals("custom"))
/*     */     {
/*  70 */       String path = installer.getInstallValue("JvmPath", null);
/*  71 */       installer.editConfigFile(getCfgPath(), "JAVA_EXE", path);
/*  72 */       return 0;
/*     */     }
/*     */ 
/*  75 */     if ((installJvm.equals("default")) || (!installJvm.equals("current")))
/*     */     {
/*  77 */       installer.editConfigFile(getCfgPath(), "JAVA_EXE", null);
/*     */     }
/*  79 */     return 0;
/*     */   }
/*     */ 
/*     */   public void installJvm(SysInstaller installer, String choicesList, String sectionName)
/*     */     throws ServiceException
/*     */   {
/*  85 */     if (!StringUtils.convertToBool(choicesList, true))
/*     */     {
/*  87 */       Report.trace("install", "not installing a JDK because UseJDK is false", null);
/*  88 */       return;
/*     */     }
/*  90 */     DataResultSet drset = (DataResultSet)installer.m_binder.getResultSet("JvmTable");
/*  91 */     if (drset == null)
/*     */     {
/*  93 */       String msg = LocaleUtils.encodeMessage("csResultSetMissing", null, "JvmTable");
/*     */ 
/*  95 */       msg = LocaleUtils.encodeMessage("csUnableToInstallJvm", msg);
/*  96 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/*  99 */     HashMap jvms = new HashMap();
/* 100 */     ArrayList installProps = new ArrayList();
/* 101 */     List choices = StringUtils.makeListFromSequence(choicesList, ',', '^', 0);
/*     */ 
/* 103 */     if (choices.size() == 1)
/*     */     {
/* 105 */       String jdkDir = FileUtils.directorySlashes((String)choices.get(0));
/*     */ 
/* 108 */       if ((jdkDir.indexOf("/") >= 0) && (FileUtils.checkFile(jdkDir, false, false) == 0))
/*     */       {
/* 111 */         if (this.m_installer.getInstallBool("CopyJDK", false))
/*     */         {
/* 113 */           String sourceDir = jdkDir;
/* 114 */           int index = jdkDir.substring(0, jdkDir.length() - 1).lastIndexOf("/");
/* 115 */           jdkDir = this.m_installer.computeDestination("native/" + this.m_installer.m_platform + jdkDir.substring(index));
/*     */ 
/* 117 */           int flags = 0x1 | 0x2 | 0x4;
/*     */ 
/* 120 */           int depth = 0;
/* 121 */           long progress = 0L;
/* 122 */           String msg = null;
/* 123 */           long size = this.m_installer.computeCopySizeEx(sourceDir, depth, flags);
/* 124 */           progress = this.m_installer.copyRecursiveEx(sourceDir, jdkDir, msg, "JDK", progress, size, depth, flags);
/*     */         }
/*     */ 
/* 127 */         setupJDK(jdkDir);
/* 128 */         return;
/*     */       }
/*     */     }
/* 131 */     for (int choiceIndex = 0; choiceIndex < choices.size(); ++choiceIndex)
/*     */     {
/* 133 */       String choice = (String)choices.get(choiceIndex);
/* 134 */       for (int i = 0; i < this.m_installer.m_myPlatforms.length; ++i)
/*     */       {
/* 136 */         String myPlatform = this.m_installer.m_myPlatforms[i];
/* 137 */         Properties bestProps = null;
/* 138 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 140 */           Properties props = drset.getCurrentRowProps();
/* 141 */           String platform = props.getProperty("Platform");
/* 142 */           String version = platform + "-" + props.getProperty("Vendor") + "-" + props.getProperty("Version");
/*     */ 
/* 145 */           if (version.equalsIgnoreCase(choice))
/*     */           {
/* 147 */             bestProps = props;
/* 148 */             break;
/*     */           }
/* 150 */           if (!platform.equals(myPlatform))
/*     */             continue;
/* 152 */           bestProps = props;
/*     */         }
/*     */ 
/* 157 */         if (bestProps == null)
/*     */           continue;
/* 159 */         String platform = bestProps.getProperty("Platform");
/* 160 */         String version = platform + "-" + bestProps.getProperty("Vendor") + "-" + bestProps.getProperty("Version");
/*     */ 
/* 163 */         if (jvms.get(version) != null)
/*     */           continue;
/* 165 */         Report.trace("install", "installing " + version, null);
/* 166 */         jvms.put(version, bestProps);
/* 167 */         installProps.add(bestProps);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 172 */     if (installProps.size() == 0)
/*     */     {
/* 174 */       String msg = LocaleUtils.encodeMessage("csUnableToFindAppropriateJvm", null);
/* 175 */       msg = LocaleUtils.encodeMessage("csUnableToInstallJvm", msg);
/* 176 */       installer.m_installLog.warning(msg);
/* 177 */       return;
/*     */     }
/*     */ 
/* 180 */     for (int i = 0; i < installProps.size(); ++i)
/*     */     {
/* 182 */       Properties props = (Properties)installProps.get(i);
/* 183 */       String src = props.getProperty("SourceDir");
/* 184 */       String dst = props.getProperty("DestinationDir");
/* 185 */       src = installer.computeDestination(src);
/* 186 */       dst = installer.computeDestination(dst);
/* 187 */       if (FileUtils.checkFile(dst, false, false) == 0)
/*     */         continue;
/* 189 */       long totalBytes = installer.computeCopySize(src);
/* 190 */       installer.copyRecursive(src, dst, "csInstallerCopyFileProgress", sectionName, 0L, totalBytes);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setupJDK(String dir)
/*     */     throws ServiceException
/*     */   {
/* 198 */     this.m_installer.editConfigFile(getCfgPath(), "JDK_custom", dir);
/*     */   }
/*     */ 
/*     */   public String getCfgPath()
/*     */   {
/* 203 */     return "bin/intradoc.cfg";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 208 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80564 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.InstallJvm
 * JD-Core Version:    0.5.4
 */