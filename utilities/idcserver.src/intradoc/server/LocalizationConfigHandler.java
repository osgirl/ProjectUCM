/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.schema.SchemaManager;
/*     */ import intradoc.server.schema.ServerSchemaManager;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Writer;
/*     */ import java.text.DateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Calendar;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class LocalizationConfigHandler extends ServiceHandler
/*     */ {
/*  58 */   protected static int[] m_syncObject = { 0 };
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadLocaleInfo() throws ServiceException, DataException
/*     */   {
/*  63 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*  64 */     this.m_binder.addResultSet("LocaleConfig", localeConfig);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadTimezoneInfo()
/*     */     throws ServiceException, DataException
/*     */   {
/*  71 */     DataResultSet timezonesOrig = SharedObjects.getTable("SystemTimeZones");
/*  72 */     DataResultSet timezones = new DataResultSet();
/*  73 */     timezones.copy(timezonesOrig);
/*  74 */     cleanTimeZones(timezones);
/*  75 */     sortTimeZones(timezones);
/*  76 */     addTimeZoneOffsets(timezones);
/*  77 */     this.m_binder.addResultSet("SystemTimeZones", timezones);
/*     */ 
/*  80 */     String lang = (String)this.m_service.getLocaleResource(1);
/*  81 */     if (lang == null)
/*     */     {
/*  83 */       lang = LocaleResources.getSystemLocale().m_languageId;
/*     */     }
/*  85 */     Locale lc = new Locale(lang);
/*  86 */     DateFormat fmt = DateFormat.getDateTimeInstance(3, 3, lc);
/*  87 */     TimeZone theDefaultTz = fmt.getCalendar().getTimeZone();
/*  88 */     if (theDefaultTz == null)
/*     */     {
/*  90 */       theDefaultTz = TimeZone.getDefault();
/*     */     }
/*  92 */     this.m_binder.putLocal("defaultTimeZoneId", theDefaultTz.getID());
/*     */ 
/*  95 */     this.m_binder.putLocal("SystemTimeZone", LocaleResources.getSystemTimeZone().getID());
/*     */   }
/*     */ 
/*     */   public void cleanTimeZones(DataResultSet timezones)
/*     */   {
/* 100 */     int idIndex = timezones.getFieldInfoIndex("lcTimeZone");
/* 101 */     timezones.first();
/* 102 */     while (timezones.isRowPresent())
/*     */     {
/* 104 */       String id = timezones.getStringValue(idIndex);
/* 105 */       if ((!id.equals("UTC")) && (id.indexOf(47) == -1))
/*     */       {
/* 107 */         timezones.deleteCurrentRow();
/*     */       }
/*     */       else
/*     */       {
/* 111 */         timezones.next();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void sortTimeZones(DataResultSet timeZones)
/*     */   {
/* 118 */     int rowCount = timeZones.getNumRows();
/* 119 */     Object[][] zones = new Object[rowCount][];
/*     */ 
/* 121 */     FieldInfo nameField = new FieldInfo();
/* 122 */     timeZones.getFieldInfo("lcTimeZone", nameField);
/* 123 */     for (int i = 0; i < rowCount; ++i)
/*     */     {
/* 125 */       Vector row = timeZones.getRowValues(i);
/* 126 */       String timeZoneName = (String)row.elementAt(nameField.m_index);
/* 127 */       TimeZone tz = LocaleResources.getTimeZone(timeZoneName, this.m_service);
/* 128 */       zones[i] = { tz, row };
/*     */     }
/*     */ 
/* 131 */     Sort.sort(zones, 0, rowCount - 1, new TimeZoneComparator());
/* 132 */     for (int i = 0; i < rowCount; ++i)
/*     */     {
/* 134 */       Vector row = (Vector)zones[i][1];
/* 135 */       timeZones.setRowValues(row, i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addTimeZoneOffsets(DataResultSet timezones)
/*     */     throws DataException
/*     */   {
/* 166 */     FieldInfo offsetField = new FieldInfo();
/* 167 */     offsetField.m_name = "offsetLabel";
/* 168 */     List finfo = new ArrayList();
/* 169 */     finfo.add(offsetField);
/* 170 */     timezones.mergeFieldsWithFlags(finfo, 0);
/*     */ 
/* 172 */     FieldInfo[] fis = ResultSetUtils.createInfoList(timezones, new String[] { "lcTimeZone", "offsetLabel" }, true);
/*     */ 
/* 174 */     for (timezones.first(); timezones.isRowPresent(); timezones.next())
/*     */     {
/* 176 */       String id = timezones.getStringValue(fis[0].m_index);
/* 177 */       if (id.indexOf("/") <= 0)
/*     */         continue;
/* 179 */       TimeZone tz = LocaleResources.getTimeZone(id, this.m_service);
/* 180 */       int offset = tz.getRawOffset() / 1000 / 60;
/*     */       String sign;
/*     */       String sign;
/* 182 */       if (offset >= 0)
/*     */       {
/* 184 */         sign = "+";
/*     */       }
/*     */       else
/*     */       {
/* 188 */         sign = "-";
/* 189 */         offset *= -1;
/*     */       }
/* 191 */       int hourOffset = offset / 60;
/* 192 */       int minuteOffset = offset % 60;
/*     */       String hourOffsetString;
/*     */       String hourOffsetString;
/* 194 */       if (hourOffset < 10)
/*     */       {
/* 196 */         hourOffsetString = "0" + hourOffset;
/*     */       }
/*     */       else
/*     */       {
/* 200 */         hourOffsetString = "" + hourOffset;
/*     */       }
/*     */       String minuteOffsetString;
/*     */       String minuteOffsetString;
/* 203 */       if (minuteOffset < 10)
/*     */       {
/* 205 */         minuteOffsetString = "0" + minuteOffset;
/*     */       }
/*     */       else
/*     */       {
/* 209 */         minuteOffsetString = "" + minuteOffset;
/*     */       }
/* 211 */       String offsetLabel = sign + hourOffsetString + ":" + minuteOffsetString;
/* 212 */       timezones.setCurrentValue(fis[1].m_index, offsetLabel);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateEnabledLocales()
/*     */     throws ServiceException, DataException
/*     */   {
/* 220 */     UserData userData = this.m_service.getUserData();
/* 221 */     if ((userData == null) || (!SecurityUtils.isUserOfRole(userData, "admin")))
/*     */     {
/* 223 */       String msg = LocaleUtils.encodeMessage("csSystemAccessDenied", null);
/* 224 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 227 */     synchronized (m_syncObject)
/*     */     {
/* 230 */       Collection enabledLocales = new HashSet();
/* 231 */       DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/* 232 */       int localeIdIndex = localeConfig.getFieldInfoIndex("lcLocaleId");
/* 233 */       for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*     */       {
/* 235 */         String localeId = localeConfig.getStringValue(localeIdIndex);
/* 236 */         boolean isEnabled = DataBinderUtils.getLocalBoolean(this.m_binder, localeId, false);
/* 237 */         if (!isEnabled)
/*     */           continue;
/* 239 */         enabledLocales.add(localeId);
/*     */       }
/*     */ 
/* 244 */       String systemLocale = LocaleResources.getSystemLocale().m_name;
/* 245 */       enabledLocales.add(systemLocale);
/*     */ 
/* 248 */       DataResultSet enabledSet = new DataResultSet(new String[] { "lcLocaleId", "lcIsEnabled" });
/* 249 */       for (String localeId : enabledLocales)
/*     */       {
/* 251 */         Vector v = new IdcVector();
/* 252 */         v.add(localeId);
/* 253 */         v.add("true");
/* 254 */         enabledSet.addRow(v);
/*     */       }
/*     */ 
/* 258 */       String localeDir = LegacyDirectoryLocator.getAppDataDirectory() + "locale";
/* 259 */       FileUtils.checkOrCreateDirectory(localeDir, 1);
/* 260 */       FileUtils.reserveDirectory(localeDir);
/*     */       try
/*     */       {
/* 263 */         DataBinder binder = new DataBinder();
/* 264 */         ResourceUtils.serializeDataBinder(localeDir, "locale_config.hda", binder, false, false);
/* 265 */         binder.addResultSet("LocaleConfig_lcIsEnabled", enabledSet);
/* 266 */         ResourceUtils.serializeDataBinder(localeDir, "locale_config.hda", binder, true, false);
/*     */       }
/*     */       finally
/*     */       {
/* 270 */         FileUtils.releaseDirectory(localeDir);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void forceRefreshSchema() throws DataException, ServiceException
/*     */   {
/* 278 */     if (this.m_workspace == null)
/*     */       return;
/* 280 */     ServerSchemaManager manager = SchemaManager.getManager(this.m_workspace);
/* 281 */     manager.refresh(this.m_workspace, 1);
/*     */ 
/* 283 */     manager.resetPublishingTimers();
/* 284 */     DataBinder binder = new DataBinder();
/* 285 */     binder.putLocal("publishOperation", "full");
/* 286 */     manager.publish(0L, true, binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void generateTranslationFiles()
/*     */     throws ServiceException, DataException
/*     */   {
/* 293 */     if (this.m_binder.getAllowMissing("format") == null)
/*     */     {
/* 295 */       this.m_binder.putLocal("format", "xlf");
/*     */     }
/* 297 */     if (this.m_binder.getAllowMissing("LangID") == null)
/*     */     {
/* 299 */       this.m_binder.putLocal("LangID", "en");
/*     */     }
/* 301 */     if (this.m_binder.getAllowMissing("targetLang") == null)
/*     */     {
/* 303 */       this.m_binder.putLocal("targetLang", "en");
/*     */     }
/* 305 */     if (this.m_binder.getAllowMissing("XmlEncodingMode") == null)
/*     */     {
/* 307 */       this.m_binder.putLocal("XmlEncodingMode", "full");
/*     */     }
/*     */ 
/* 310 */     String encoding = this.m_binder.getAllowMissing("TranslationEncoding");
/* 311 */     encoding = "UTF8";
/* 312 */     String dir = this.m_binder.getAllowMissing("TranslationOutputDir");
/* 313 */     if (dir == null)
/*     */     {
/* 315 */       dir = FileUtils.getAbsolutePath(DirectoryLocator.getAppDataDirectory(), "translation");
/*     */     }
/* 317 */     FileUtils.checkOrCreateDirectory(dir, 1);
/* 318 */     for (String type : new String[] { "sy", "ap", "ww", "cs" })
/*     */     {
/* 320 */       String[] prefix = { type };
/* 321 */       if (type == "cs")
/*     */       {
/* 323 */         prefix = new String[] { type, "" };
/*     */       }
/* 325 */       ResultSet rset = LocaleLoader.createStringsResultSetWithPrefixFilter((IdcLocale)this.m_service.getLocaleResource(0), prefix, LocaleLoader.F_USE_EN_FOR_EMPTY_LANGUAGE | LocaleLoader.F_INCLUDE_COMMENTS);
/*     */ 
/* 328 */       this.m_binder.addResultSet("LocalizationStrings", rset);
/* 329 */       OutputStream out = null;
/*     */       try
/*     */       {
/* 332 */         String file = FileUtils.getAbsolutePath(dir, type + "_strings.xlf");
/* 333 */         out = FileUtils.openOutputStream(file, 16);
/* 334 */         Writer w = FileUtils.openDataWriter(out, encoding, 0);
/* 335 */         PageMerger merger = this.m_service.m_pageMerger;
/* 336 */         boolean isStrict = merger.m_isStrict;
/*     */         try
/*     */         {
/* 339 */           merger.m_isStrict = true;
/* 340 */           merger.writeResourceInclude("localization_strings", w, true);
/*     */         }
/*     */         finally
/*     */         {
/* 344 */           merger.m_isStrict = isStrict;
/*     */         }
/* 346 */         w.close();
/* 347 */         out = null;
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 351 */         this.m_service.createServiceException(e, null);
/*     */       }
/*     */       finally
/*     */       {
/* 355 */         FileUtils.abortAndClose(out);
/*     */       }
/*     */     }
/* 358 */     this.m_binder.removeResultSet("LocalizationStrings");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 363 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ 
/*     */   public class TimeZoneComparator
/*     */     implements IdcComparator
/*     */   {
/*     */     public TimeZoneComparator()
/*     */     {
/*     */     }
/*     */ 
/*     */     public int compare(Object o1, Object o2)
/*     */     {
/* 143 */       Object[] array1 = (Object[])(Object[])o1;
/* 144 */       Object[] array2 = (Object[])(Object[])o2;
/* 145 */       if ((array1[0] instanceof TimeZone) && (array2[0] instanceof TimeZone))
/*     */       {
/* 147 */         TimeZone tz1 = (TimeZone)array1[0];
/* 148 */         TimeZone tz2 = (TimeZone)array2[0];
/* 149 */         int t1 = tz1.getRawOffset();
/* 150 */         int t2 = tz2.getRawOffset();
/* 151 */         if (t1 == t2)
/*     */         {
/* 153 */           String id1 = tz1.getID();
/* 154 */           String id2 = tz2.getID();
/* 155 */           return id1.compareTo(id2);
/*     */         }
/* 157 */         return t1 - t2;
/*     */       }
/*     */ 
/* 160 */       return 0;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.LocalizationConfigHandler
 * JD-Core Version:    0.5.4
 */