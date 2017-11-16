/*     */ package intradoc.server.subject;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.Help;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectCallbackAdapter;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcLoggerUtils;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class LocalesSubjectCallback extends SubjectCallbackAdapter
/*     */ {
/*     */   public void refresh(String subject)
/*     */     throws DataException, ServiceException
/*     */   {
/*  50 */     String traceSubject = "localization";
/*  51 */     Report.trace(traceSubject, "Refreshing LocalesSubjectCallback.", null);
/*     */ 
/*  53 */     synchronized (DataLoader.m_syncObject)
/*     */     {
/*  55 */       boolean systemStringsOnly = ResourceLoader.m_loadSystemStringsOnly;
/*     */ 
/*  58 */       String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/*  59 */       ResourceContainer container = SharedObjects.getResources();
/*  60 */       ResourceLoader.loadAndMergeStdLocaleOverrides(dataDir, container);
/*     */ 
/*  63 */       DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  64 */       ResourceLoader.addExtraLocaleConfigColumns(localeConfig);
/*     */ 
/*  67 */       String resDir = LegacyDirectoryLocator.getResourcesDirectory() + "lang/";
/*  68 */       List languageDirs = LocaleUtils.getLanguageDirectoryList(null, resDir);
/*  69 */       Vector validHelpLangs = (Vector)Help.getValidHelpLangs();
/*  70 */       String helpDir = Help.getHelpDir();
/*     */ 
/*  73 */       int localeIdIndex = localeConfig.getFieldInfoIndex("lcLocaleId");
/*  74 */       int isEnabledIndex = localeConfig.getFieldInfoIndex("lcIsEnabled");
/*  75 */       DataResultSet languageMap = SharedObjects.getTable("LanguageLocationMap");
/*  76 */       for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*     */       {
/*  78 */         String localeId = localeConfig.getStringValue(localeIdIndex);
/*  79 */         String isEnabledStr = localeConfig.getStringValue(isEnabledIndex);
/*  80 */         if (StringUtils.convertToBool(isEnabledStr, false))
/*     */         {
/*  82 */           IdcLocale locale = LocaleResources.getLocale(localeId);
/*  83 */           if (locale == null)
/*     */           {
/*  85 */             Report.trace(traceSubject, "Creating IdcLocale for " + localeId, null);
/*  86 */             Properties props = localeConfig.getCurrentRowProps();
/*  87 */             locale = new IdcLocale(localeId);
/*  88 */             LocaleResources.initializeLocale(locale, props);
/*     */ 
/*  91 */             DataResultSet languageLocaleMap = SharedObjects.getTable("LanguageLocaleMap");
/*  92 */             FieldInfo[] fis = ResultSetUtils.createInfoList(languageLocaleMap, new String[] { "lcLanguageId", "lcLocaleId" }, true);
/*     */ 
/*  94 */             for (languageLocaleMap.first(); languageLocaleMap.isRowPresent(); languageLocaleMap.next())
/*     */             {
/*  96 */               String tmpLocaleId = languageLocaleMap.getStringValue(fis[1].m_index);
/*  97 */               if (!localeId.equals(tmpLocaleId))
/*     */                 continue;
/*  99 */               String languageId = languageLocaleMap.getStringValue(fis[0].m_index);
/* 100 */               languageId = LocaleUtils.normalizeId(languageId);
/* 101 */               LocaleResources.addLocaleAlias(languageId, tmpLocaleId);
/*     */             }
/*     */ 
/* 105 */             if (!systemStringsOnly)
/*     */             {
/* 107 */               Report.trace(traceSubject, "Loading strings for " + localeId, null);
/* 108 */               for (int i = 0; i < languageDirs.size(); ++i)
/*     */               {
/* 110 */                 String dir = (String)languageDirs.get(i);
/* 111 */                 ResourceLoader.loadLocalizationStringsForEnabledLocale(dir, helpDir, validHelpLangs, localeConfig, localeId, props, container, languageMap, ResourceLoader.F_PREPEND_RESOURCE);
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 119 */             locale.m_isEnabled = true;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 124 */           IdcLocale locale = LocaleResources.getLocale(localeId);
/* 125 */           if (locale == null)
/*     */             continue;
/* 127 */           locale.m_isEnabled = false;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 133 */       Help.setValidHelpLangs(validHelpLangs);
/* 134 */       String validHelpLangsStr = StringUtils.createString(validHelpLangs, ',', ',');
/* 135 */       SharedObjects.putEnvironmentValue("ValidHelpLangs", validHelpLangsStr);
/*     */ 
/* 137 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/* 138 */       cxt.setCachedObject("forceRefresh", "1");
/* 139 */       DataLoader.checkSharedCachedResources(cxt);
/*     */ 
/* 142 */       IdcLoggerUtils.clearStringCache();
/*     */     }
/*     */ 
/* 145 */     Report.trace(traceSubject, "Finished refreshing LocalesSubjectCallback.", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81433 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.subject.LocalesSubjectCallback
 * JD-Core Version:    0.5.4
 */