/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcLocalizationStrings;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.resource.ComponentData;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcPerfectHash;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class LocaleService extends Service
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void packageLocalization()
/*     */     throws DataException, ServiceException
/*     */   {
/*  42 */     IdcLocale userLocale = (IdcLocale)getCachedObject("UserLocale");
/*  43 */     IdcLocale systemLocale = LocaleResources.getSystemLocale();
/*  44 */     IdcDateFormat dateFormat = (IdcDateFormat)getCachedObject("UserDateFormat");
/*  45 */     TimeZone timezone = (TimeZone)getCachedObject("UserTimeZone");
/*  46 */     TimeZone systemTimezone = LocaleResources.getSystemTimeZone();
/*  47 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*     */ 
/*  49 */     this.m_binder.putLocal("UserLocale", userLocale.m_name);
/*  50 */     this.m_binder.putLocal("SystemLocale", systemLocale.m_name);
/*  51 */     this.m_binder.addResultSet("LocaleConfig", localeConfig);
/*     */ 
/*  53 */     String defaultApp = SharedObjects.getEnvironmentValue("DefaultApplicationName");
/*  54 */     if (defaultApp != null)
/*     */     {
/*  56 */       this.m_binder.putLocal("DefaultApplicationName", defaultApp);
/*     */     }
/*  58 */     String app = (String)getLocaleResource(5);
/*  59 */     if (app != null)
/*     */     {
/*  61 */       this.m_binder.putLocal("Application", app);
/*     */     }
/*     */ 
/*  64 */     if (dateFormat == null)
/*     */     {
/*  66 */       dateFormat = userLocale.m_dateFormat;
/*     */     }
/*  68 */     prepareDateFormat("UserDateFormat", dateFormat);
/*  69 */     prepareDateFormat("SystemDateFormat", LocaleResources.getSystemDateFormat());
/*     */ 
/*  71 */     if (timezone == null)
/*     */     {
/*  73 */       timezone = systemTimezone;
/*     */     }
/*  75 */     String tz = timezone.getID();
/*  76 */     this.m_binder.putLocal("UserTimeZone", tz);
/*  77 */     tz = systemTimezone.getID();
/*  78 */     this.m_binder.putLocal("SystemTimeZone", tz);
/*     */ 
/*  80 */     DataResultSet drset = LocaleLoader.createLocaleStringsResultSet(userLocale, new String[] { "sy", "ap", "ww" }, LocaleLoader.F_USE_EN_FOR_EMPTY_LANGUAGE);
/*     */ 
/*  82 */     this.m_binder.addResultSet("LocaleStrings", drset);
/*  83 */     this.m_binder.addResultSet("IsoJavaEncodingMap", SharedObjects.getTable("IsoJavaEncodingMap"));
/*     */   }
/*     */ 
/*     */   public void prepareDateFormat(String id, IdcDateFormat dateFormat)
/*     */   {
/*  88 */     String fmt = "IDF:" + dateFormat.toPattern();
/*  89 */     this.m_binder.putLocal(id, fmt);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void auditLocalization() throws ServiceException
/*     */   {
/*  95 */     this.m_binder.setFieldType("auditMessage", "message");
/*  96 */     this.m_binder.setFieldType("message", "message");
/*  97 */     Map table = LocaleResources.m_missingStrings;
/*  98 */     boolean skipBuild = false;
/*  99 */     if ((DataBinderUtils.getBoolean(this.m_binder, "startLocalizationAudit", false)) && (LocaleResources.m_missingStrings == null))
/*     */     {
/* 102 */       LocaleResources.m_missingStrings = new HashMap();
/* 103 */       skipBuild = true;
/*     */     }
/* 105 */     if (DataBinderUtils.getBoolean(this.m_binder, "clearLocalizationAudit", false))
/*     */     {
/* 107 */       LocaleResources.m_missingStrings = new HashMap();
/* 108 */       skipBuild = true;
/*     */     }
/* 110 */     if (DataBinderUtils.getBoolean(this.m_binder, "stopLocalizationAudit", false))
/*     */     {
/* 112 */       LocaleResources.m_missingStrings = null;
/* 113 */       skipBuild = true;
/*     */     }
/* 115 */     if (table != null)
/*     */     {
/* 117 */       this.m_binder.putLocal("auditEnabled", "1");
/*     */     }
/* 119 */     if (skipBuild)
/*     */     {
/* 121 */       this.m_binder.putLocal("RedirectParams", "IdcService=LOCALIZATION_AUDIT");
/* 122 */       return;
/*     */     }
/* 124 */     if (table == null)
/*     */     {
/* 126 */       this.m_binder.putLocal("auditMessage", "!csLocalizationAuditNotEnabled");
/*     */     }
/*     */     else
/*     */     {
/* 130 */       DataResultSet drset = new DataResultSet(new String[] { "key", "message", "stack" });
/*     */ 
/* 133 */       IdcProperties newProps = new IdcProperties();
/* 134 */       newProps.setMap(new ConcurrentHashMap());
/* 135 */       if (table instanceof IdcProperties)
/*     */       {
/* 137 */         IdcProperties oldProps = (IdcProperties)table;
/* 138 */         newProps.setDefaults(oldProps);
/*     */       }
/*     */       else
/*     */       {
/* 142 */         IdcProperties oldProps = new IdcProperties();
/* 143 */         oldProps.setMap(table);
/* 144 */         newProps.setDefaults(oldProps);
/*     */       }
/*     */ 
/* 147 */       LocaleResources.m_missingStrings = newProps;
/* 148 */       if (table instanceof IdcProperties)
/*     */       {
/* 150 */         ((IdcProperties)table).flatten();
/*     */       }
/*     */ 
/* 153 */       Set keySet = table.keySet();
/* 154 */       for (Iterator i$ = keySet.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 156 */         Object[] info = (Object[])(Object[])table.get(key);
/* 157 */         Exception e = (Exception)info[1];
/* 158 */         CharArrayWriter w = new CharArrayWriter();
/* 159 */         e.printStackTrace(new PrintWriter(w));
/* 160 */         String trace = w.toString();
/* 161 */         Vector row = new IdcVector();
/* 162 */         row.addElement(key);
/* 163 */         row.addElement(LocaleUtils.encodeMessage((IdcMessage)info[0]));
/* 164 */         row.addElement(trace);
/* 165 */         drset.addRow(row); }
/*     */ 
/*     */ 
/* 168 */       this.m_binder.addResultSet("LookupAudit", drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void doStringIndexOp(DataBinder binder, String op) throws DataException, ServiceException
/*     */   {
/* 176 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/* 177 */     String resDir = LegacyDirectoryLocator.getResourcesDirectory();
/*     */ 
/* 179 */     IdcLocalizationStrings oldStrings = LocaleResources.m_stringData;
/*     */ 
/* 181 */     FileUtils.checkOrCreateDirectory(FileUtils.getAbsolutePath(dataDir, "strings"), 1);
/*     */ 
/* 183 */     boolean isReserved = false;
/* 184 */     ResourceContainer container = new ResourceContainer();
/*     */     IdcLocalizationStrings strings;
/*     */     try
/*     */     {
/* 187 */       if (oldStrings == null)
/*     */       {
/* 189 */         container.m_languages = LocaleResources.m_languages;
/*     */       }
/*     */       else
/*     */       {
/* 193 */         container.m_languages = oldStrings.m_languageMap;
/*     */       }
/* 195 */       ReportProgress progress = null;
/* 196 */       if (oldStrings != null)
/*     */       {
/* 198 */         progress = oldStrings.getReportProgress();
/*     */       }
/* 200 */       if (progress == null)
/*     */       {
/* 202 */         ProgressState progressState = new ProgressState();
/* 203 */         progressState.init("LocalizationIndex");
/* 204 */         progress = progressState;
/*     */       }
/* 206 */       if ((op == "build") || (oldStrings == null))
/*     */       {
/* 208 */         op = "build";
/* 209 */         IdcLocalizationStrings strings = new IdcLocalizationStrings(SharedObjects.getSecureEnvironment(), FileUtils.getAbsolutePath(dataDir, "strings"), DirectoryLocator.getLocalDataDir("strings"), progress);
/*     */ 
/* 213 */         if (oldStrings == null)
/*     */         {
/* 215 */           oldStrings = strings;
/*     */         }
/* 217 */         oldStrings.reserve(op);
/* 218 */         isReserved = true;
/*     */         try
/*     */         {
/* 221 */           strings.readConfigFile();
/*     */ 
/* 225 */           strings.prepareIncrementalUpdate();
/* 226 */           strings.m_oldVersionNumber = -1;
/*     */         }
/*     */         catch (IOException e)
/*     */         {
/* 230 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 235 */         oldStrings.reserve(op);
/* 236 */         isReserved = true;
/* 237 */         strings = oldStrings.duplicateIndex();
/*     */       }
/*     */ 
/* 240 */       container.m_handler = strings;
/* 241 */       int flags = ResourceLoader.F_FORCE_LOAD | ResourceLoader.F_IS_STRICT;
/*     */ 
/* 249 */       ResourceLoader.loadLocalizationStrings(container, FileUtils.getAbsolutePath(resDir, "core"), dataDir, DirectoryLocator.getLocalDataDir("strings"), flags);
/*     */ 
/* 254 */       Vector resourceData = ComponentLoader.m_resources;
/*     */ 
/* 256 */       for (ComponentData data : resourceData)
/*     */       {
/* 258 */         String fileName = data.m_file;
/* 259 */         if (!fileName.endsWith(".hda"))
/*     */         {
/* 265 */           Map stringFileInfo = strings.getFileLoadInfo(fileName);
/* 266 */           if ((stringFileInfo != null) && ((flags & ResourceLoader.F_FORCE_LOAD) == 0))
/*     */           {
/* 268 */             long tsOld = NumberUtils.parseLong((String)stringFileInfo.get("ts"), 0L);
/* 269 */             File f = new File(fileName);
/* 270 */             long tsNew = f.lastModified();
/* 271 */             if (tsNew == tsOld)
/*     */             {
/* 275 */               Report.trace("localization", "not load unchanged file " + fileName, null);
/*     */             }
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 281 */             DataLoader.cacheResourceFile(container, fileName);
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 285 */             throw new ServiceException(e, "csErrorLoadingResourceFile", new Object[] { fileName });
/*     */           }
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 291 */         strings.finishIncrementalUpdate();
/* 292 */         isReserved = false;
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 296 */         throw new ServiceException(e);
/*     */       }
/* 298 */       LocaleResources.m_stringData = strings;
/*     */ 
/* 300 */       SubjectManager.notifyChanged("serverstartup");
/*     */     }
/*     */     finally
/*     */     {
/* 304 */       container.m_handler = null;
/* 305 */       if (isReserved)
/*     */       {
/* 307 */         oldStrings.release();
/*     */       }
/*     */     }
/*     */ 
/* 311 */     binder.putLocal("indexVersion", "" + strings.m_versionNumber);
/* 312 */     binder.putLocal("indexLanguageCount", "" + strings.m_languageMap.size());
/* 313 */     binder.putLocal("indexKeyCount", "" + strings.m_stringMap[0].size());
/* 314 */     binder.putLocal("indexBlockCount", "" + strings.m_langStringBlocks.length);
/* 315 */     binder.putLocal("indexStringCount", "" + strings.m_valLangBlockData.length / (strings.m_languageMap.size() + 1));
/*     */ 
/* 317 */     binder.putLocal("indexTagNameCount", "" + strings.m_tagLists.size());
/* 318 */     binder.putLocal("indexTreeCount", "" + strings.m_stringMap[0].treeCount());
/* 319 */     binder.putLocal("indexVertexCount", "" + strings.m_stringMap[0].graphSize());
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void buildStringIndex() throws DataException, ServiceException
/*     */   {
/* 325 */     doStringIndexOp(this.m_binder, "build");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void refreshStringIndex() throws DataException, ServiceException
/*     */   {
/* 331 */     doStringIndexOp(this.m_binder, "refresh");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 336 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 88047 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.LocaleService
 * JD-Core Version:    0.5.4
 */