/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcLocalizationStrings;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ 
/*     */ public class ServerStartupSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  43 */     updateTimestamp();
/*  44 */     checkAndUpdateStringIndex();
/*     */   }
/*     */ 
/*     */   public void updateTimestamp()
/*     */   {
/*  49 */     File f = FileUtilsCfgBuilder.getCfgFile(DirectoryLocator.getAppDataDirectory() + "subjects/serverstartup.mrk", "Subject", false);
/*  50 */     if (!f.exists())
/*     */       return;
/*  52 */     intradoc.common.SystemUtils.m_sharedServerStartupTime = f.lastModified();
/*     */   }
/*     */ 
/*     */   public void checkAndUpdateStringIndex()
/*     */   {
/*  62 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  63 */     String stringDir = FileUtils.getAbsolutePath(dataDir, "strings");
/*  64 */     String localStringDir = DirectoryLocator.getLocalDataDir("strings");
/*     */ 
/*  66 */     HashMap args = new HashMap();
/*  67 */     args.put("psName", "LocalizationIndex");
/*  68 */     args.put("psPrefix", "LocalizationIndex");
/*  69 */     args.put("ProgressDirectory", FileUtils.getAbsolutePath(stringDir, "log"));
/*     */ 
/*  71 */     ProgressState progressState = new ProgressState();
/*     */ 
/*  75 */     IdcLocalizationStrings strings = new IdcLocalizationStrings(SharedObjects.getSecureEnvironment(), stringDir, localStringDir, progressState);
/*     */     try
/*     */     {
/*  81 */       strings.readConfigFile();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  85 */       Report.error("localization", null, e);
/*     */     }
/*  87 */     if (strings.m_versionNumber == -1)
/*     */     {
/*  91 */       return;
/*     */     }
/*     */ 
/*  94 */     IdcLocalizationStrings curStrings = LocaleResources.m_stringData;
/*  95 */     if ((curStrings != null) && (strings.m_versionNumber == curStrings.m_versionNumber))
/*     */     {
/* 100 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 105 */       progressState.init(args);
/* 106 */       strings.loadStringIndex();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 110 */       Report.error("localization", null, e);
/* 111 */       return;
/*     */     }
/*     */ 
/* 114 */     LocaleResources.m_stringData = strings;
/* 115 */     Report.trace("localization", "now using string index version " + strings.m_versionNumber, null);
/*     */ 
/* 117 */     LocaleResources.m_stringObjMap = new HashMap();
/* 118 */     ResourceContainer res = SharedObjects.getResources();
/* 119 */     res.resetStrings();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 124 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.ServerStartupSubjectCallback
 * JD-Core Version:    0.5.4
 */