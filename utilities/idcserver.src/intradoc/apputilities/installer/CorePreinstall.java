/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class CorePreinstall
/*     */   implements SectionInstaller
/*     */ {
/*     */   public DataResultSet m_executables;
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  36 */     if (disposition.equals("new"))
/*     */     {
/*  38 */       String[] removeFiles = { "bin/intradoc.cfg", "bin/config.cfg" };
/*     */ 
/*  43 */       for (int i = 0; i < removeFiles.length; ++i)
/*     */       {
/*  45 */         String path = installer.computeDestination(removeFiles[i]);
/*  46 */         FileUtils.deleteFile(path);
/*     */       }
/*     */     }
/*     */ 
/*  50 */     String configureAdminServer = installer.getInstallValue("ConfigureAdminServer", "true");
/*     */ 
/*  52 */     String adminServerDir = installer.getInstallValue("AdminServerDir", null);
/*     */ 
/*  54 */     if ((configureAdminServer.equalsIgnoreCase("configure_existing")) && (adminServerDir != null))
/*     */     {
/*     */       try
/*     */       {
/*  59 */         SysInstaller adminInstaller = installer.deriveInstaller(adminServerDir);
/*     */ 
/*  61 */         adminInstaller.loadConfig();
/*     */ 
/*  63 */         String adminServerPort = adminInstaller.getInstallValue("IdcAdminServerPort", null);
/*     */ 
/*  65 */         if (adminServerPort != null)
/*     */         {
/*  67 */           installer.m_installerConfig.put("IdcAdminServerPort", adminServerPort);
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  73 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/*     */ 
/*  77 */     String idcAdminName = installer.getInstallValue("IDC_Admin_Name", null);
/*  78 */     if (idcAdminName == null)
/*     */     {
/*  80 */       idcAdminName = installer.getInstallValue("IDC_Name", null);
/*  81 */       if (idcAdminName != null)
/*     */       {
/*  83 */         idcAdminName = idcAdminName + "_admin";
/*  84 */         installer.m_installerConfig.put("IDC_Admin_Name", idcAdminName);
/*     */       }
/*     */     }
/*  87 */     Report.trace("install", "IDC_Admin_Name is " + idcAdminName, null);
/*     */ 
/*  89 */     if (disposition.equals("always"))
/*     */     {
/*  91 */       String platform = installer.getInstallValue("Platform", null);
/*  92 */       Properties props = installer.getInstallerTable("PlatformConfigTable", platform);
/*     */ 
/*  94 */       String searchImpl = installer.getInstallValue("SearchImplementorName", null);
/*  95 */       if ((searchImpl == null) && 
/*  97 */         (props != null))
/*     */       {
/*  99 */         searchImpl = props.getProperty("SearchImplementorName");
/* 100 */         if ((searchImpl != null) && (!searchImpl.equals("null")))
/*     */         {
/* 102 */           installer.m_installerConfig.put("SearchImplementorName", searchImpl);
/*     */ 
/* 104 */           Report.trace("install", "setting SearchImplementorName=" + searchImpl, null);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 110 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 115 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.CorePreinstall
 * JD-Core Version:    0.5.4
 */