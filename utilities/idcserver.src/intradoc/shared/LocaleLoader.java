/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.IdcLocaleString;
/*     */ import intradoc.common.IdcLocalizationStrings;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.GuiText;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.util.IdcPerfectHash;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class LocaleLoader
/*     */ {
/*  34 */   public static int F_USE_EN_FOR_EMPTY_LANGUAGE = 1;
/*  35 */   public static int F_INCLUDE_COMMENTS = 2;
/*     */ 
/*     */   public static void loadLocaleConfig(Map strings, Map dateFormats, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  44 */     String systemTimeZone = binder.get("SystemTimeZone");
/*  45 */     DataResultSet locales = getResultSet(binder, "LocaleConfig");
/*  46 */     String systemLocale = binder.get("SystemLocale");
/*     */ 
/*  48 */     DataResultSet langMap = (DataResultSet)binder.getResultSet("LanguageLocaleMap");
/*     */ 
/*  51 */     Properties environment = (Properties)AppObjectRepository.getObject("environment");
/*     */ 
/*  53 */     LocaleResources.init(environment);
/*  54 */     if (strings != null)
/*     */     {
/*  56 */       LocaleResources.initStrings(strings);
/*     */     }
/*     */ 
/*  59 */     if (SharedObjects.getTable("SystemTimeZones") == null)
/*     */     {
/*  62 */       DataResultSet zones = new DataResultSet(new String[] { "lcTimeZone" });
/*  63 */       String[] tzs = TimeZone.getAvailableIDs();
/*  64 */       for (String tz : tzs)
/*     */       {
/*  66 */         Vector v = zones.createEmptyRow();
/*  67 */         v.setElementAt(tz, 0);
/*  68 */         zones.addRow(v);
/*     */       }
/*  70 */       SharedObjects.putTable("SystemTimeZones", zones);
/*     */     }
/*     */ 
/*  77 */     FieldInfo[] infos = ResultSetUtils.createInfoList(locales, new String[] { "lcLocaleId" }, true);
/*     */ 
/*  79 */     Vector v = locales.findRow(infos[0].m_index, systemLocale);
/*     */ 
/*  82 */     if (v != null)
/*     */     {
/*  84 */       Properties props = locales.getCurrentRowProps();
/*  85 */       String name = props.getProperty("lcLocaleId");
/*  86 */       IdcLocale locale = new IdcLocale(name);
/*  87 */       props.put("lcIsEnabled", "1");
/*  88 */       LocaleResources.initializeLocale(locale, props);
/*     */ 
/*  93 */       LocaleResources.initSystemLocale(name);
/*  94 */       dateFormats.put("SystemDateFormat", LocaleResources.m_systemDateFormat);
/*     */     }
/*     */ 
/*  99 */     for (locales.first(); locales.isRowPresent(); locales.next())
/*     */     {
/* 101 */       Properties props = locales.getCurrentRowProps();
/* 102 */       String name = props.getProperty("lcLocaleId");
/* 103 */       if (name.equals(systemLocale))
/*     */         continue;
/* 105 */       IdcLocale locale = new IdcLocale(name);
/* 106 */       LocaleResources.initializeLocale(locale, props);
/*     */     }
/*     */ 
/* 110 */     TimeZone timezone = LocaleResources.getTimeZone(systemTimeZone, null);
/* 111 */     if (timezone == null)
/*     */     {
/* 113 */       String msg = LocaleUtils.encodeMessage("apSystemTimeZoneNotDefined", null, systemTimeZone);
/*     */ 
/* 115 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 122 */     boolean enableSetIdcTimeZone = SharedObjects.getEnvValueAsBoolean("EnableSetIdcTimeZone", false);
/* 123 */     if ((SharedLoader.allowGlobalUtilPointers()) || (enableSetIdcTimeZone == true))
/*     */     {
/* 125 */       TimeZone.setDefault(timezone);
/*     */     }
/*     */ 
/* 131 */     LocaleResources.initFormats(dateFormats, SharedObjects.getSecureEnvironment());
/* 132 */     LocaleUtils.updateDateFormats();
/*     */ 
/* 134 */     if (langMap != null)
/*     */     {
/* 136 */       for (langMap.first(); langMap.isRowPresent(); langMap.next())
/*     */       {
/* 138 */         Properties props = langMap.getCurrentRowProps();
/* 139 */         String id = props.getProperty("lcLanguageId");
/* 140 */         id = LocaleUtils.normalizeId(id);
/* 141 */         String locale = props.getProperty("lcLocaleId");
/* 142 */         IdcLocale lc = LocaleResources.getLocale(locale);
/* 143 */         if ((lc == null) && (SystemUtils.m_verbose))
/*     */         {
/* 145 */           String msg = LocaleUtils.encodeMessage("apLocaleNotFoundForLanguage", null, locale, id);
/*     */ 
/* 147 */           Report.trace("localization", LocaleResources.localizeMessage(msg, null), null);
/*     */         }
/*     */         else {
/* 150 */           LocaleResources.addLocaleAlias(id, locale);
/*     */         }
/*     */       }
/*     */     }
/* 154 */     updateLocaleSettingsFromConfig();
/*     */   }
/*     */ 
/*     */   public static void updateLocaleSettingsFromConfig()
/*     */   {
/* 165 */     LocaleResources.m_defaultApp = SharedObjects.getEnvironmentValue("DefaultApplicationName");
/* 166 */     if (LocaleResources.m_defaultApp != null)
/*     */     {
/* 168 */       LocaleResources.m_defaultContext.setCachedObject("Application", LocaleResources.m_defaultApp);
/*     */     }
/*     */ 
/* 171 */     determineSystemGuiConfig();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void loadTimeZones(DataResultSet timezones)
/*     */   {
/* 178 */     Report.deprecatedUsage("LocaleLoader.loadTimeZones() is deprecated.");
/* 179 */     for (timezones.first(); timezones.isRowPresent(); timezones.next())
/*     */     {
/* 181 */       Properties props = timezones.getCurrentRowProps();
/*     */       try
/*     */       {
/* 184 */         LocaleResources.addTimeZone(props);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 188 */         Report.trace(null, "Error loading timezone '" + props.getProperty("lcTimeZone") + "'.", e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void determineSystemGuiConfig()
/*     */   {
/* 196 */     String systemFont = SharedObjects.getEnvironmentValue("SystemFont");
/* 197 */     if (systemFont == null)
/*     */     {
/* 199 */       systemFont = SharedObjects.getEnvironmentValue("ApplicationFont");
/*     */     }
/* 201 */     if ((systemFont != null) && (systemFont.length() > 0))
/*     */     {
/* 203 */       intradoc.gui.GuiStyles.m_fontFamily = systemFont;
/*     */     }
/*     */     else
/*     */     {
/* 211 */       if (!EnvUtils.isMicrosoftVM())
/*     */         return;
/* 213 */       intradoc.gui.GuiStyles.m_fontFamily = "Arial";
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void readStringsFromBinder(Hashtable strings, DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 227 */     DataResultSet drset = (DataResultSet)binder.getResultSet("LocaleStrings");
/* 228 */     FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "lcKey", "lcValue" }, true);
/* 229 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 231 */       String key = drset.getStringValue(infos[0].m_index);
/* 232 */       String value = drset.getStringValue(infos[1].m_index);
/* 233 */       strings.put(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void configureUserLocale(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 244 */     String tzname = binder.getLocal("UserTimeZone");
/* 245 */     if (tzname == null)
/*     */     {
/* 247 */       throw new ServiceException(LocaleResources.getString("apUndefinedUserTimeZone", cxt));
/*     */     }
/* 249 */     TimeZone tz = LocaleResources.getTimeZone(tzname, null);
/* 250 */     if (tz == null)
/*     */     {
/* 252 */       throw new ServiceException(LocaleResources.getString("apUnableToLookUpTimeZone", cxt, tzname));
/*     */     }
/* 254 */     cxt.setCachedObject("UserTimeZone", tz);
/*     */ 
/* 256 */     String userLocale = binder.getLocal("UserLocale");
/* 257 */     if (userLocale == null)
/*     */     {
/* 259 */       throw new ServiceException(LocaleResources.getString("apUndefinedUserLocale", cxt));
/*     */     }
/* 261 */     IdcLocale locale = LocaleResources.getLocale(userLocale);
/* 262 */     if (locale == null)
/*     */     {
/* 264 */       throw new ServiceException(LocaleResources.getString("apLocaleNotDefined", cxt, userLocale));
/*     */     }
/* 266 */     cxt.setCachedObject("UserLocale", locale);
/* 267 */     locale.m_dateFormat.setTZ(tz);
/*     */ 
/* 269 */     String defaultApp = binder.getAllowMissing("DefaultApplicationName");
/* 270 */     if (defaultApp != null)
/*     */     {
/* 272 */       SharedObjects.putEnvironmentValue("DefaultApplicationName", defaultApp);
/* 273 */       LocaleResources.m_defaultApp = defaultApp;
/*     */     }
/*     */ 
/* 276 */     String app = binder.getAllowMissing("Application");
/* 277 */     if (app == null)
/*     */       return;
/* 279 */     cxt.setCachedObject("Application", app);
/*     */   }
/*     */ 
/*     */   public static void doStaticLocalization(ExecutionContext cxt)
/*     */   {
/* 290 */     GuiText.localize(cxt);
/*     */ 
/* 293 */     ViewFields viewFields = new ViewFields(cxt);
/* 294 */     Hashtable lMap = viewFields.m_localizationMap;
/* 295 */     for (Enumeration en = lMap.elements(); en.hasMoreElements(); )
/*     */     {
/* 297 */       String[][] list = (String[][])(String[][])en.nextElement();
/* 298 */       LocaleResources.localizeStaticDoubleArray(list, cxt, 1);
/*     */     }
/*     */ 
/* 304 */     LocaleResources.localizeStaticDoubleArray(TableFields.m_dataSources, cxt, 1);
/* 305 */     Hashtable tableDefs = viewFields.m_tableFields.m_tableDefs;
/* 306 */     for (Enumeration en = tableDefs.elements(); en.hasMoreElements(); )
/*     */     {
/* 308 */       String[][] tableDef = (String[][])(String[][])en.nextElement();
/* 309 */       LocaleResources.localizeStaticDoubleArray(tableDef, cxt, 1);
/*     */     }
/*     */ 
/* 313 */     Hashtable displayMaps = viewFields.m_tableFields.m_displayMaps;
/* 314 */     String[][] lList = TableFields.LOCALIZATION_LIST;
/* 315 */     int num = lList.length;
/* 316 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 318 */       String key = lList[i][0];
/* 319 */       String[][] displayMap = (String[][])(String[][])displayMaps.get(key);
/* 320 */       if (key == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 325 */       Vector indices = StringUtils.parseArray(lList[i][1], ',', '^');
/* 326 */       int size = indices.size();
/* 327 */       for (int j = 0; j < size; ++j)
/*     */       {
/* 329 */         int index = NumberUtils.parseInteger((String)indices.elementAt(j), 1);
/* 330 */         LocaleResources.localizeStaticDoubleArray(displayMap, cxt, index);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void localizeResultSet(String key, DataResultSet rset, ExecutionContext cxt)
/*     */   {
/* 341 */     Report.deprecatedUsage("LocaleLoader.localizeResultSet() is not needed.");
/*     */   }
/*     */ 
/*     */   public static ResultSet createStringsResultSetWithPrefixFilter(String[] prefixes, int flags)
/*     */   {
/* 353 */     return createStringsResultSetWithPrefixFilter(null, prefixes, flags);
/*     */   }
/*     */ 
/*     */   public static ResultSet createStringsResultSetWithPrefixFilter(IdcLocale locale, String[] prefixes, int flagsTmp)
/*     */   {
/* 364 */     int flags = flagsTmp;
/* 365 */     if ((flags & F_USE_EN_FOR_EMPTY_LANGUAGE) == 0)
/*     */     {
/* 367 */       throw new AssertionError("!$Localization system no longer supports empty language codes.!$Include F_USE_EN_FOR_EMPTY_LANGUAGE in flags.");
/*     */     }
/*     */ 
/* 371 */     IdcLocalizationStrings stringData = LocaleResources.m_stringData;
/* 372 */     int langIndex = -1;
/* 373 */     String baseLanguage = LocaleResources.m_baseLanguage;
/* 374 */     int baseLanguageIndex = LocaleResources.m_languages.getCode(baseLanguage);
/* 375 */     if (locale != null)
/*     */     {
/* 377 */       langIndex = LocaleResources.m_languages.getCode(locale.m_languageId);
/* 378 */       if (langIndex == -1)
/*     */       {
/* 380 */         langIndex = baseLanguageIndex;
/*     */       }
/*     */     }
/* 383 */     if (stringData != null)
/*     */     {
/* 385 */       IdcLocale theLocale = locale;
/* 386 */       int theLangIndex = langIndex;
/* 387 */       int keyCount = stringData.m_stringMap[0].size();
/* 388 */       int langCount = stringData.m_languageMap.size();
/* 389 */       int tmp = -1;
/* 390 */       if (LocaleResources.m_defaultApp != null)
/*     */       {
/* 392 */         tmp = stringData.m_applicationMap.getCode(LocaleResources.m_defaultApp);
/*     */       }
/* 394 */       int appIndex = tmp + 1;
/* 395 */       String[] prefixList = prefixes;
/*     */ 
/* 397 */       ResultSet rset = new ResultSet(flags, keyCount, stringData, appIndex, prefixList, langCount, theLocale, theLangIndex)
/*     */       {
/*     */         public int m_rowIndex;
/*     */         public int m_endRow;
/*     */ 
/*     */         public void setDateFormat(IdcDateFormat f)
/*     */         {
/*     */         }
/*     */ 
/*     */         public int getNumFields()
/*     */         {
/* 409 */           if ((this.val$flags & LocaleLoader.F_INCLUDE_COMMENTS) != 0)
/*     */           {
/* 411 */             return 4;
/*     */           }
/* 413 */           return 3;
/*     */         }
/*     */ 
/*     */         public boolean getFieldInfo(String field, FieldInfo info)
/*     */         {
/* 418 */           if (field.equals("lcKey"))
/*     */           {
/* 420 */             info.m_index = 0;
/* 421 */             info.m_name = field;
/* 422 */             info.m_type = 6;
/*     */           }
/* 424 */           else if (field.equals("lcValue"))
/*     */           {
/* 426 */             info.m_index = 1;
/* 427 */             info.m_name = field;
/* 428 */             info.m_type = 6;
/*     */           }
/* 430 */           else if (field.equals("lcLanguageCode"))
/*     */           {
/* 432 */             info.m_index = 2;
/* 433 */             info.m_name = field;
/* 434 */             info.m_type = 6;
/*     */           }
/* 436 */           else if (field.equals("lcComment"))
/*     */           {
/* 438 */             info.m_index = 3;
/* 439 */             info.m_name = field;
/* 440 */             info.m_type = 6;
/*     */           }
/*     */           else
/*     */           {
/* 444 */             return false;
/*     */           }
/* 446 */           return true;
/*     */         }
/*     */ 
/*     */         public boolean next()
/*     */         {
/* 451 */           this.m_rowIndex += 1;
/* 452 */           getStringValue(1);
/* 453 */           return this.m_rowIndex < this.m_endRow;
/*     */         }
/*     */ 
/*     */         public String getStringValue(int index)
/*     */         {
/* 458 */           int lang = this.m_rowIndex / this.val$keyCount;
/* 459 */           int key = this.m_rowIndex % this.val$keyCount;
/* 460 */           String id = (String)this.val$stringData.m_stringMap[0].get(key);
/* 461 */           int finalKey = this.val$stringData.m_stringMap[this.val$appIndex].getActiveCode(id);
/* 462 */           while ((!LocaleLoader.checkStringKeyForPrefixes(id, this.val$prefixList)) || (this.val$stringData.getString(finalKey, lang, this.val$appIndex) == null))
/*     */           {
/* 465 */             this.m_rowIndex += 1;
/* 466 */             if (this.m_rowIndex >= this.m_endRow)
/*     */             {
/* 468 */               return null;
/*     */             }
/* 470 */             ++key;
/* 471 */             if (key == this.val$keyCount)
/*     */             {
/* 473 */               key = 0;
/* 474 */               ++lang;
/*     */             }
/* 476 */             id = (String)this.val$stringData.m_stringMap[0].get(key);
/* 477 */             finalKey = this.val$stringData.m_stringMap[this.val$appIndex].getActiveCode(id);
/*     */           }
/* 479 */           switch (index)
/*     */           {
/*     */           case 0:
/* 482 */             return id;
/*     */           case 1:
/* 484 */             key = this.val$stringData.m_stringMap[this.val$appIndex].getActiveCode(id);
/* 485 */             String val = this.val$stringData.getString(finalKey, lang, this.val$appIndex);
/* 486 */             return val;
/*     */           case 2:
/* 488 */             String langCode = (String)this.val$stringData.m_languageMap.get(lang);
/* 489 */             return langCode;
/*     */           case 3:
/* 492 */             String attributes = this.val$stringData.getString(finalKey, this.val$langCount, this.val$appIndex);
/* 493 */             List l = StringUtils.makeListFromSequenceSimple(attributes);
/* 494 */             String comment = "";
/* 495 */             for (String attribute : l)
/*     */             {
/* 497 */               if (attribute.startsWith("comment="))
/*     */               {
/* 499 */                 comment = attribute.substring("comment=".length());
/*     */               }
/*     */             }
/*     */ 
/* 503 */             return comment;
/*     */           }
/* 505 */           return null;
/*     */         }
/*     */ 
/*     */         public String getFieldName(int index)
/*     */         {
/* 511 */           switch (index)
/*     */           {
/*     */           case 0:
/* 514 */             return "lcKey";
/*     */           case 1:
/* 516 */             return "lcValue";
/*     */           case 2:
/* 518 */             return "lcLanguageCode";
/*     */           case 3:
/* 520 */             return "lcComment";
/*     */           }
/* 522 */           return null;
/*     */         }
/*     */ 
/*     */         public Date getDateValue(int fieldIndex)
/*     */         {
/* 528 */           return null;
/*     */         }
/*     */ 
/*     */         public boolean isRowPresent()
/*     */         {
/* 533 */           getStringValue(1);
/* 534 */           return this.m_rowIndex < this.m_endRow;
/*     */         }
/*     */ 
/*     */         public boolean first()
/*     */         {
/* 539 */           if (this.val$theLocale == null)
/*     */           {
/* 541 */             this.m_rowIndex = 0;
/* 542 */             this.m_endRow = (this.val$keyCount * this.val$langCount);
/*     */           }
/*     */           else
/*     */           {
/* 546 */             this.m_rowIndex = (this.val$keyCount * this.val$theLangIndex);
/* 547 */             this.m_endRow = (this.m_rowIndex + this.val$keyCount);
/*     */           }
/* 549 */           return this.m_endRow > 0;
/*     */         }
/*     */ 
/*     */         public String getStringValueByName(String fieldName)
/*     */         {
/* 554 */           if (fieldName.equals("lcKey"))
/*     */           {
/* 556 */             return getStringValue(0);
/*     */           }
/* 558 */           if (fieldName.equals("lcValue"))
/*     */           {
/* 560 */             return getStringValue(1);
/*     */           }
/* 562 */           if (fieldName.equals("lcLanguageCode"))
/*     */           {
/* 564 */             return getStringValue(2);
/*     */           }
/* 566 */           if (fieldName.equals("lcComment"))
/*     */           {
/* 568 */             return getStringValue(3);
/*     */           }
/*     */ 
/* 572 */           return null;
/*     */         }
/*     */ 
/*     */         public boolean isMutable()
/*     */         {
/* 578 */           return false;
/*     */         }
/*     */ 
/*     */         public boolean renameField(String f1, String f2)
/*     */         {
/* 583 */           return false;
/*     */         }
/*     */ 
/*     */         public boolean isEmpty()
/*     */         {
/* 588 */           return false;
/*     */         }
/*     */ 
/*     */         public int getFieldInfoIndex(String fieldName)
/*     */         {
/* 593 */           if (fieldName.equals("lcKey"))
/*     */           {
/* 595 */             return 0;
/*     */           }
/* 597 */           if (fieldName.equals("lcValue"))
/*     */           {
/* 599 */             return 1;
/*     */           }
/* 601 */           if (fieldName.equals("lcLanguageCode"))
/*     */           {
/* 603 */             return 2;
/*     */           }
/* 605 */           if (fieldName.equals("lcComment"))
/*     */           {
/* 607 */             return 3;
/*     */           }
/*     */ 
/* 611 */           return -1;
/*     */         }
/*     */ 
/*     */         public void getIndexFieldInfo(int index, FieldInfo info)
/*     */         {
/* 617 */           switch (index)
/*     */           {
/*     */           case 0:
/* 620 */             info.m_index = 0;
/* 621 */             info.m_name = "lcKey";
/* 622 */             info.m_type = 6;
/* 623 */             break;
/*     */           case 1:
/* 625 */             info.m_index = 1;
/* 626 */             info.m_name = "lcValue";
/* 627 */             info.m_type = 6;
/* 628 */             break;
/*     */           case 2:
/* 630 */             info.m_index = 2;
/* 631 */             info.m_name = "lcLanguageCode";
/* 632 */             info.m_type = 6;
/*     */           }
/*     */         }
/*     */ 
/*     */         public void closeInternals()
/*     */         {
/*     */         }
/*     */ 
/*     */         public Date getDateValueByName(String key)
/*     */         {
/* 646 */           return null;
/*     */         }
/*     */ 
/*     */         public boolean canRenameFields()
/*     */         {
/* 651 */           return false;
/*     */         }
/*     */ 
/*     */         public int skip(int rows)
/*     */         {
/*     */           int newIndex;
/*     */           int newIndex;
/* 657 */           if (this.val$theLocale == null)
/*     */           {
/* 659 */             newIndex = this.m_rowIndex + rows;
/*     */           }
/*     */           else
/*     */           {
/* 663 */             newIndex = this.m_rowIndex + rows * this.val$langCount;
/*     */           }
/* 665 */           if (newIndex > this.m_endRow)
/*     */           {
/* 667 */             rows = this.m_endRow - this.m_rowIndex;
/* 668 */             newIndex = this.m_endRow;
/*     */           }
/* 670 */           this.m_rowIndex = newIndex;
/* 671 */           return rows;
/*     */         }
/*     */ 
/*     */         public boolean hasRawObjects()
/*     */         {
/* 676 */           return true;
/*     */         }
/*     */ 
/*     */         public IdcDateFormat getDateFormat()
/*     */         {
/* 681 */           return null;
/*     */         }
/*     */       };
/* 684 */       rset.first();
/* 685 */       return rset;
/*     */     }
/*     */     DataResultSet drset;
/*     */     DataResultSet drset;
/* 689 */     if ((flags & F_INCLUDE_COMMENTS) != 0)
/*     */     {
/* 691 */       drset = new DataResultSet(new String[] { "lcKey", "lcValue", "lcLanguageCode", "lcComment" });
/*     */     }
/*     */     else
/*     */     {
/* 695 */       drset = new DataResultSet(new String[] { "lcKey", "lcValue", "lcLanguageCode" });
/*     */     }
/* 697 */     int langCount = LocaleResources.m_languages.size();
/* 698 */     Iterator it = LocaleResources.m_stringObjMap.keySet().iterator();
/* 699 */     while (it.hasNext())
/*     */     {
/* 701 */       String id = (String)it.next();
/* 702 */       if (!checkStringKeyForPrefixes(id, prefixes)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 706 */       IdcLocaleString lcString = (IdcLocaleString)LocaleResources.m_stringObjMap.get(id);
/* 707 */       if (locale == null)
/*     */       {
/* 709 */         for (int i = 0; i < langCount; ++i)
/*     */         {
/* 711 */           String lang = (String)LocaleResources.m_languages.get(i);
/* 712 */           String value = lcString.getLangValue(i);
/* 713 */           if (value == null)
/*     */           {
/* 723 */             IdcLocale l = LocaleResources.getLocaleWithoutTrim(lang);
/* 724 */             if (l == null)
/*     */             {
/* 730 */               int index = lang.lastIndexOf(45);
/* 731 */               if (index <= 0)
/*     */                 continue;
/* 733 */               String langTrimmed = lang.substring(0, index);
/* 734 */               IdcLocale baseLocale = LocaleResources.getLocale(langTrimmed);
/* 735 */               if (baseLocale == null)
/*     */               {
/*     */                 continue;
/*     */               }
/*     */ 
/* 744 */               boolean hasBaseLocaleInLangList = false;
/* 745 */               for (int j = 0; j < langCount; ++j)
/*     */               {
/* 747 */                 String langItem = (String)LocaleResources.m_languages.get(j);
/* 748 */                 if (!langItem.equalsIgnoreCase(langTrimmed))
/*     */                   continue;
/* 750 */                 hasBaseLocaleInLangList = true;
/* 751 */                 break;
/*     */               }
/*     */ 
/* 754 */               if (hasBaseLocaleInLangList) {
/*     */                 continue;
/*     */               }
/*     */ 
/* 758 */               l = baseLocale;
/*     */             }
/*     */ 
/* 767 */             ExecutionContextAdaptor ctx = new ExecutionContextAdaptor();
/* 768 */             ctx.setCachedObject("UserLocale", l);
/* 769 */             value = LocaleResources.getStringInternal(lcString.m_key, ctx);
/*     */           }
/*     */ 
/* 772 */           if (value == null)
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 777 */           Vector v = new IdcVector();
/* 778 */           v.addElement(id);
/* 779 */           v.addElement(value);
/* 780 */           v.addElement(lang);
/* 781 */           if ((flags & F_INCLUDE_COMMENTS) != 0)
/*     */           {
/* 783 */             String comment = lcString.getAttribute("comment");
/* 784 */             if (comment == null)
/*     */             {
/* 786 */               comment = "";
/*     */             }
/* 788 */             v.addElement(comment);
/*     */           }
/* 790 */           drset.addRow(v);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 795 */         String value = lcString.getLangValue(langIndex);
/* 796 */         String lang = locale.m_languageId;
/* 797 */         if (value == null)
/*     */         {
/* 799 */           lang = baseLanguage;
/* 800 */           value = lcString.getLangValue(baseLanguageIndex);
/*     */         }
/* 802 */         if (value == null)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 807 */         Vector v = new IdcVector();
/* 808 */         v.addElement(id);
/* 809 */         v.addElement(value);
/* 810 */         v.addElement(lang);
/* 811 */         if ((flags & F_INCLUDE_COMMENTS) != 0)
/*     */         {
/* 813 */           String comment = lcString.getAttribute("comment");
/* 814 */           if (comment == null)
/*     */           {
/* 816 */             comment = "";
/*     */           }
/* 818 */           v.addElement(comment);
/*     */         }
/* 820 */         drset.addRow(v);
/*     */       }
/*     */     }
/* 823 */     drset.first();
/* 824 */     return drset;
/*     */   }
/*     */ 
/*     */   public static DataResultSet createLocaleStringsResultSet(IdcLocale locale, String[] prefixes, int flags)
/*     */   {
/* 836 */     ResultSet rset = createStringsResultSetWithPrefixFilter(locale, prefixes, flags);
/* 837 */     if (rset instanceof DataResultSet)
/*     */     {
/* 839 */       return (DataResultSet)rset;
/*     */     }
/* 841 */     DataResultSet drset = new DataResultSet();
/* 842 */     drset.copy(rset);
/* 843 */     return drset;
/*     */   }
/*     */ 
/*     */   protected static boolean checkStringKeyForPrefixes(String key, String[] prefixes)
/*     */   {
/* 849 */     if (prefixes == null)
/*     */     {
/* 851 */       return true;
/*     */     }
/* 853 */     for (int i = 0; i < prefixes.length; ++i)
/*     */     {
/* 855 */       if (key.startsWith(prefixes[i]))
/*     */       {
/* 859 */         return (!prefixes[i].equals("")) || ((!key.startsWith("ap")) && (!key.startsWith("ww")) && (!key.startsWith("sy")));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 864 */     return false;
/*     */   }
/*     */ 
/*     */   public static DataResultSet getResultSet(DataBinder binder, String name)
/*     */     throws ServiceException
/*     */   {
/* 872 */     DataResultSet drset = (DataResultSet)binder.getResultSet(name);
/* 873 */     if (drset == null)
/*     */     {
/* 875 */       String msg = LocaleUtils.encodeMessage("apTableMissing", null, name);
/*     */ 
/* 877 */       throw new ServiceException(msg);
/*     */     }
/* 879 */     return drset;
/*     */   }
/*     */ 
/*     */   public static String determineDefaultLocale()
/*     */   {
/* 884 */     Locale lc = Locale.getDefault();
/* 885 */     DataResultSet localeMap = SharedObjects.getTable("LanguageLocaleMap");
/* 886 */     String locale = ResourceLoader.computeLocale(lc, localeMap);
/* 887 */     if (locale != null)
/*     */     {
/*     */       try
/*     */       {
/* 891 */         String languageId = ResultSetUtils.findValue(localeMap, "lcLocaleId", locale, "lcLanguageId");
/* 892 */         if ((languageId != null) && (languageId.length() > 0))
/*     */         {
/* 894 */           DataResultSet directionMap = SharedObjects.getTable("LanguageDirectionMap");
/* 895 */           String direction = ResultSetUtils.findValue(directionMap, "lcLanguageId", languageId, "lcDirection");
/*     */ 
/* 897 */           if ((direction != null) && (direction.equalsIgnoreCase("rtl")))
/*     */           {
/* 899 */             locale = "English-US";
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 909 */     return locale;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 914 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96119 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.LocaleLoader
 * JD-Core Version:    0.5.4
 */