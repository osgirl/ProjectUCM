/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcMessageContainer;
/*      */ import intradoc.util.IdcPerfectHash;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.util.Calendar;
/*      */ import java.util.Collection;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.Set;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class LocaleResources
/*      */ {
/*      */   public static final int F_LANGUAGE = 1;
/*      */   public static final int F_APPLICATION = 2;
/*      */   public static IdcLocalizationStrings m_stringData;
/*      */   public static Map<String, IdcLocaleString> m_stringObjMap;
/*      */   public static ResourceContainer m_resourceContainer;
/*      */   public static String[] m_apps;
/*   57 */   public static String m_defaultApp = null;
/*   58 */   public static Map<String, Object[]> m_missingStrings = null;
/*   59 */   public static boolean m_disableLocalization = false;
/*      */   public static IdcPerfectHash m_languages;
/*   61 */   public static String m_baseLanguage = "en";
/*      */   public static TimeZoneFormat m_systemTimeZoneFormat;
/*      */   public static Map<String, IdcLocale> m_locales;
/*      */   public static Map<String, IdcDateFormat> m_localeDateFormats;
/*   65 */   public static Object[] m_ordinalRegexSet = null;
/*   66 */   public static ExecutionContext m_defaultContext = new ExecutionContextAdaptor();
/*      */ 
/*   68 */   public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcTimeZone m_utc;
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcTimeZone m_systemTimeZone;
/*      */   protected static TimeZone m_sysZone;
/*      */   protected static IdcLocale m_systemLocale;
/*      */   protected static IdcNumberFormat m_systemNumberFormat;
/*      */   public static IdcDateFormat m_systemDateFormat;
/*      */   public static IdcDateFormat m_iso8601Format;
/*      */   public static IdcDateFormat m_dbFormat;
/*      */   public static IdcDateFormat m_daoFormat;
/*      */   public static IdcDateFormat m_bulkloadFormat;
/*      */   public static IdcDateFormat m_searchFormat;
/*      */   public static IdcDateFormat m_rfc850Format;
/*      */   public static IdcDateFormat m_rfc1123Format;
/*      */   public static IdcDateFormat m_odbcFormat;
/*      */   public static IdcDateFormat m_utcOdbcFormat;
/*      */   public static IdcDateFormat m_legacyFormat;
/*      */   public static IdcDateFormat m_rfc2822Format;
/*      */   public static IdcDateFormat m_rfcMailFormat;
/*      */   public static Properties m_encodingAliasesMap;
/*      */   public static final int TIMESTAMP = 0;
/*      */   public static final int DATE_ONLY = 1;
/*      */   public static final int TIME_ONLY = 2;
/*  102 */   public static String m_insufficentArgumentsError = "(Err)";
/*  103 */   public static String m_numberFormatError = "(NaN)";
/*  104 */   public static String m_dateFormatError = "(NaD)";
/*  105 */   public static String m_invalidFlagError = "(Err)";
/*      */ 
/*  107 */   public static String m_nullSubstitutionString = "(null)";
/*  108 */   public static Map<Object, Map> m_staticArrays = new Hashtable();
/*      */ 
/*  110 */   public static String[][] FORMATS = { { "System", "yyyy-MM-dd HH:mm:ss" }, { "Database", "'{ts' ''yyyy-MM-dd HH:mm:ss{.SSS}'''}'" }, { "Dao", "M/d/yyyy hh:mm:ss aa" }, { "Bulkload", "yyyy-MM-dd HH:mm:ss" }, { "Search", "yyyy/MM/dd HH:mm:ss" }, { "Rfc850", "EEE{,} d-MMM-yyyy HH:mm:ss 'GMT'!tUTC!rfc" }, { "Rfc1123", "EEE{,} dd MMM yyyy HH:mm:ss 'GMT'!tUTC!rfc" }, { "Odbc", "'{ts' ''yyyy-MM-dd HH:mm:ss{.SSS}[Z]'''}'" }, { "UTCOdbc", "'{ts' ''yyyy-MM-dd HH:mm:ss{.SSS}{Z}'''}'!tUTC" }, { "Iso8601", "iso8601" }, { "Legacy", "M/d/yy {h:mm[:ss] aa}" }, { "Rfc2822", "EEE, d MMM yyyy HH:mm:ss 'GMT'!tUTC!rfc" }, { "RfcMail", "EEE, dd MMM yyyy HH:mm:ss zzzz!rfc" } };
/*      */   public static IdcComparator m_comparator;
/*      */ 
/*      */   public static void reset()
/*      */   {
/*      */     try
/*      */     {
/*  140 */       m_stringData = null;
/*  141 */       m_stringObjMap = new HashMap();
/*      */ 
/*  144 */       m_languages = new IdcPerfectHash(new Random(System.currentTimeMillis()));
/*  145 */       m_locales = new HashMap();
/*  146 */       m_localeDateFormats = new HashMap();
/*      */ 
/*  148 */       m_systemTimeZoneFormat = new TimeZoneFormat();
/*  149 */       m_utc = IdcTimeZone.wrap(UTC);
/*  150 */       m_systemTimeZoneFormat.addTZ("SystemTimeZone", UTC);
/*  151 */       initFormats(new HashMap(), new Properties());
/*  152 */       m_apps = new String[0];
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  156 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void init()
/*      */     throws ServiceException
/*      */   {
/*  164 */     SystemUtils.reportDeprecatedUsage("LocaleResourcesinit()");
/*  165 */     init(new Properties());
/*      */   }
/*      */ 
/*      */   public static void init(Properties environment) throws ServiceException
/*      */   {
/*  170 */     String baseLanguage = environment.getProperty("BaseLanguagePrefix");
/*  171 */     if (baseLanguage != null)
/*      */     {
/*  173 */       m_baseLanguage = baseLanguage;
/*      */     }
/*  175 */     if (SystemUtils.isActiveTrace("localization"))
/*      */     {
/*  177 */       m_missingStrings = new ConcurrentHashMap();
/*      */     }
/*  179 */     m_comparator = new SortUtilsComparator(3);
/*      */   }
/*      */ 
/*      */   public static void initStrings(Map strings)
/*      */   {
/*  184 */     Iterator it = strings.keySet().iterator();
/*  185 */     while (it.hasNext())
/*      */     {
/*  187 */       String resource = (String)it.next();
/*      */ 
/*  190 */       int index = resource.lastIndexOf(46);
/*      */       String id;
/*      */       String lang;
/*      */       String id;
/*  191 */       if (index < 0)
/*      */       {
/*  193 */         String lang = m_baseLanguage;
/*  194 */         id = resource;
/*      */       }
/*      */       else
/*      */       {
/*  198 */         lang = resource.substring(0, index);
/*  199 */         id = resource.substring(index + 1);
/*      */       }
/*  201 */       int langIndex = m_languages.getCode(lang);
/*  202 */       if (langIndex == -1)
/*      */       {
/*  204 */         m_languages.add(lang);
/*  205 */         langIndex = m_languages.getCode(lang);
/*      */       }
/*  207 */       IdcLocaleString lcString = (IdcLocaleString)m_stringObjMap.get(id);
/*  208 */       if (lcString == null)
/*      */       {
/*  210 */         lcString = new IdcLocaleString(id);
/*  211 */         m_stringObjMap.put(id, lcString);
/*      */       }
/*  213 */       String value = (String)strings.get(resource);
/*  214 */       lcString.setLangValue(langIndex, value);
/*      */     }
/*  216 */     fixupStrings();
/*      */   }
/*      */ 
/*      */   public static void initStrings(ResourceContainer container)
/*      */   {
/*  221 */     m_stringObjMap = container.m_stringObjMap;
/*  222 */     m_languages = container.m_languages;
/*      */ 
/*  224 */     m_resourceContainer = container;
/*  225 */     Collection c = m_resourceContainer.m_apps.values();
/*  226 */     m_apps = (String[])(String[])c.toArray(new String[c.size()]);
/*      */ 
/*  228 */     fixupStrings();
/*      */   }
/*      */ 
/*      */   public static void fixupStrings()
/*      */   {
/*      */   }
/*      */ 
/*      */   public static void initFormats(Map<String, IdcDateFormat> formats, Properties env)
/*      */     throws ServiceException
/*      */   {
/*  284 */     m_localeDateFormats = formats;
/*      */ 
/*  289 */     TimeZone systemTimeZone = m_systemTimeZone;
/*      */ 
/*  291 */     IdcNumberFormat format = new IdcNumberFormat();
/*  292 */     format.setGroupingUsed(false);
/*  293 */     format.setParseIntegerOnly(true);
/*      */ 
/*  295 */     for (int i = 0; i < FORMATS.length; ++i)
/*      */     {
/*  297 */       if (m_localeDateFormats.get(FORMATS[i][0] + "DateFormat") != null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  301 */       String fmtString = env.getProperty(FORMATS[i][0] + "DateFormat");
/*  302 */       if (fmtString == null)
/*      */       {
/*  304 */         fmtString = FORMATS[i][1];
/*      */       }
/*  310 */       else if (FORMATS[i][0].indexOf("Odbc") >= 0)
/*      */       {
/*  312 */         boolean isUtc = FORMATS[i][0].indexOf("UTC") >= 0;
/*  313 */         String zoneStr = (isUtc) ? "{Z}" : "[Z]";
/*  314 */         fmtString = validateDateFormat(FORMATS[i][0], fmtString, "{.SSS}", ":ss");
/*  315 */         fmtString = validateDateFormat(FORMATS[i][0], fmtString, zoneStr, "{.SSS}");
/*      */       }
/*      */ 
/*  318 */       TimeZone tz = null;
/*  319 */       String tmp = env.getProperty(FORMATS[i][0] + "TimeZone");
/*  320 */       if (tmp != null)
/*      */       {
/*  322 */         tz = getTimeZone(tmp);
/*      */       }
/*  324 */       if (tz == null)
/*      */       {
/*  326 */         tz = systemTimeZone;
/*  327 */         if (FORMATS[i][0].equals("Iso8601"))
/*      */         {
/*  329 */           tz = m_utc;
/*      */         }
/*      */       }
/*      */ 
/*  333 */       IdcDateFormat idcfmt = new IdcDateFormat();
/*      */       try
/*      */       {
/*  341 */         idcfmt.init(fmtString, tz, m_systemTimeZoneFormat, format);
/*  342 */         idcfmt.setPattern(fmtString);
/*      */       }
/*      */       catch (ParseException e)
/*      */       {
/*  346 */         Report.trace("localization", null, e);
/*  347 */         throw new ServiceException("!apUnableToLoadDateFormats", e);
/*      */       }
/*  349 */       String key = FORMATS[i][0] + "DateFormat";
/*  350 */       m_localeDateFormats.put(key, idcfmt);
/*      */     }
/*      */ 
/*  353 */     int i = 0;
/*  354 */     m_systemDateFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  355 */     m_dbFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  356 */     m_daoFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  357 */     m_bulkloadFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  358 */     m_searchFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  359 */     m_rfc850Format = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  360 */     m_rfc1123Format = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  361 */     m_odbcFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  362 */     m_utcOdbcFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  363 */     m_iso8601Format = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  364 */     m_legacyFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  365 */     m_rfc2822Format = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*  366 */     m_rfcMailFormat = (IdcDateFormat)m_localeDateFormats.get(FORMATS[(i++)][0] + "DateFormat");
/*      */   }
/*      */ 
/*      */   public static String validateDateFormat(String dateEnvKey, String dateFormat, String lookupKey, String insertionKey)
/*      */   {
/*  372 */     if (dateFormat.indexOf(lookupKey) < 0)
/*      */     {
/*  374 */       Object[] params = { dateEnvKey + "DateFormat", dateFormat, lookupKey, insertionKey };
/*  375 */       int insertionIndex = dateFormat.indexOf(insertionKey);
/*  376 */       String errString = "apConfigDateFormatMissingString";
/*  377 */       if (dateFormat.indexOf(insertionKey) > 0)
/*      */       {
/*  379 */         int splitIndex = insertionIndex + insertionKey.length();
/*  380 */         String startPiece = dateFormat.substring(0, splitIndex);
/*  381 */         String endPiece = dateFormat.substring(splitIndex);
/*  382 */         dateFormat = startPiece + lookupKey + endPiece;
/*  383 */         errString = "apConfigDateFormatMissingStringAndFixed";
/*      */       }
/*  385 */       String msg = LocaleUtils.encodeMessage(errString, null, params);
/*  386 */       Report.trace("system", localizeMessage(msg, null), null);
/*      */     }
/*  388 */     return dateFormat;
/*      */   }
/*      */ 
/*      */   public static void initSystemLocale(String name) throws ServiceException
/*      */   {
/*  393 */     IdcLocale locale = getLocale(name);
/*  394 */     if (locale == null)
/*      */     {
/*  396 */       throw new ServiceException(LocaleUtils.encodeMessage("apLocaleNotDefined", null, name));
/*      */     }
/*      */ 
/*  400 */     m_systemNumberFormat = locale.m_numberFormat;
/*  401 */     m_systemDateFormat = locale.m_dateFormat;
/*  402 */     m_sysZone = locale.m_dateFormat.getTimeZone();
/*  403 */     m_systemTimeZone = IdcTimeZone.wrap(m_sysZone);
/*  404 */     SystemUtils.m_traceLogFormat.setTimeZone(m_systemTimeZone);
/*      */ 
/*  406 */     m_systemLocale = locale;
/*  407 */     m_locales.put("SystemLocale", locale);
/*  408 */     m_defaultContext = new ExecutionContextAdaptor();
/*  409 */     m_defaultContext.setCachedObject("UserLocale", locale);
/*      */   }
/*      */ 
/*      */   public static IdcStringBuilder localizeMessage(IdcStringBuilder builder, IdcMessage msg, ExecutionContext cxt)
/*      */   {
/*  415 */     if (cxt == null)
/*      */     {
/*  417 */       cxt = m_defaultContext;
/*      */     }
/*  419 */     if (builder == null)
/*      */     {
/*  421 */       builder = new IdcStringBuilder();
/*      */     }
/*  423 */     localize(builder, msg, cxt, 0);
/*  424 */     return builder;
/*      */   }
/*      */ 
/*      */   public static String localizeMessage(String msg, ExecutionContext cxt)
/*      */   {
/*  429 */     if (cxt == null)
/*      */     {
/*  431 */       cxt = m_defaultContext;
/*      */     }
/*  433 */     return localizeMessageEx(msg, cxt, 0);
/*      */   }
/*      */ 
/*      */   public static void localizeArray(String[] array, ExecutionContext cxt)
/*      */   {
/*  438 */     if (cxt == null)
/*      */     {
/*  440 */       cxt = m_defaultContext;
/*      */     }
/*  442 */     for (int i = 0; i < array.length; ++i)
/*      */     {
/*  444 */       String text = getString(array[i], cxt);
/*  445 */       if (text == null)
/*      */         continue;
/*  447 */       array[i] = text;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String[][] copyDoubleStringArray(String[][] arr)
/*      */   {
/*  454 */     String[][] newArray = new String[arr.length][];
/*  455 */     for (int i = 0; i < arr.length; ++i)
/*      */     {
/*  457 */       String[] newInner = new String[arr[i].length];
/*  458 */       for (int j = 0; j < newInner.length; ++j)
/*      */       {
/*  460 */         newInner[j] = arr[i][j];
/*      */       }
/*  462 */       newArray[i] = newInner;
/*      */     }
/*      */ 
/*  465 */     return newArray;
/*      */   }
/*      */ 
/*      */   public static void localizeStaticDoubleArray(String[][] array, ExecutionContext cxt, int index)
/*      */   {
/*  470 */     Map info = (Map)m_staticArrays.get(array);
/*  471 */     if (info == null)
/*      */     {
/*  473 */       info = new Hashtable();
/*  474 */       m_staticArrays.put(array, info);
/*      */     }
/*      */ 
/*  477 */     Integer intKey = new Integer(index);
/*  478 */     String[] col = (String[])info.get(intKey);
/*  479 */     if (col == null)
/*      */     {
/*  481 */       col = new String[array.length];
/*  482 */       for (int i = 0; i < array.length; ++i)
/*      */       {
/*  484 */         String val = null;
/*  485 */         if (array[i].length > index)
/*      */         {
/*  487 */           val = array[i][index];
/*      */         }
/*  489 */         col[i] = val;
/*      */       }
/*  491 */       info.put(intKey, col);
/*      */     }
/*      */     else
/*      */     {
/*  495 */       for (int i = 0; i < array.length; ++i)
/*      */       {
/*  497 */         if (array[i].length <= index)
/*      */           continue;
/*  499 */         array[i][index] = col[i];
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  504 */     localizeDoubleArray(array, cxt, index);
/*      */   }
/*      */ 
/*      */   public static void localizeDoubleArray(String[][] array, ExecutionContext cxt, int index)
/*      */   {
/*  509 */     if (cxt == null)
/*      */     {
/*  511 */       cxt = m_defaultContext;
/*      */     }
/*  513 */     for (int i = 0; i < array.length; ++i)
/*      */     {
/*  515 */       if (array[i].length <= index) {
/*      */         continue;
/*      */       }
/*      */ 
/*  519 */       String text = array[i][index];
/*  520 */       if (text.startsWith("!"))
/*      */       {
/*  522 */         text = localizeMessage(text, cxt);
/*      */       }
/*      */       else
/*      */       {
/*  526 */         text = getString(text, cxt);
/*      */       }
/*  528 */       if (text == null)
/*      */         continue;
/*  530 */       array[i][index] = text;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static String localizeMessageEx(String msg, ExecutionContext cxt, int depth)
/*      */   {
/*  537 */     if (msg == null)
/*      */     {
/*  539 */       msg = "!syNullPointerException";
/*  540 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*      */       {
/*  542 */         throw new AssertionError(msg);
/*      */       }
/*      */     }
/*  545 */     IdcStringBuilder buf = new IdcStringBuilder(msg.length() * 3);
/*  546 */     IdcMessage idcMsg = LocaleUtils.parseMessage(msg);
/*  547 */     localize(buf, idcMsg, cxt, 0);
/*  548 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   protected static void localize(IdcStringBuilder buf, IdcMessage origMsg, ExecutionContext cxt, int flags)
/*      */   {
/*  554 */     IdcMessage idcMsg = origMsg;
/*  555 */     for (IdcMessage lastMsg = null; idcMsg != null; idcMsg = idcMsg.m_prior)
/*      */     {
/*  557 */       if (lastMsg != null)
/*      */       {
/*  559 */         boolean skip = false;
/*  560 */         if ((lastMsg.m_msgLocalized != null) && (idcMsg.m_msgLocalized != null) && (lastMsg.m_msgLocalized.equals(idcMsg.m_msgLocalized)))
/*      */         {
/*  563 */           skip = true;
/*      */         }
/*  565 */         else if ((lastMsg.m_stringKey != null) && (idcMsg.m_stringKey != null) && (lastMsg.m_stringKey.equals(idcMsg.m_stringKey)) && (m_comparator != null) && (m_comparator.compare(lastMsg.m_args, idcMsg.m_args) == 0))
/*      */         {
/*  569 */           skip = true;
/*      */         }
/*  571 */         else if ((lastMsg.m_msgSimple != null) && (idcMsg.m_msgSimple != null) && (lastMsg.m_msgSimple.equals(idcMsg.m_msgSimple)))
/*      */         {
/*  574 */           skip = true;
/*      */         }
/*  576 */         else if ((lastMsg.m_msgEncoded != null) && (idcMsg.m_msgEncoded != null) && (lastMsg.m_msgEncoded.equals(idcMsg.m_msgEncoded)))
/*      */         {
/*  579 */           skip = true;
/*      */         }
/*      */ 
/*  582 */         if (skip)
/*      */         {
/*  584 */           if (!SystemUtils.m_isDevelopmentEnvironment)
/*      */             continue;
/*  586 */           if (SystemUtils.m_verbose)
/*      */           {
/*  588 */             Report.trace(null, null, new ServiceException("Skipping duplicate message " + origMsg, null)); continue;
/*      */           }
/*      */ 
/*  593 */           Report.trace(null, "Skipping duplicate message " + origMsg, null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  600 */         int length = buf.length();
/*  601 */         if ((length > 0) && (!Character.isWhitespace(buf.charAt(length - 1))))
/*      */         {
/*  603 */           buf.append(" ");
/*      */         }
/*  605 */         if (idcMsg.m_msgLocalized != null)
/*      */         {
/*  607 */           buf.append(idcMsg.m_msgLocalized);
/*      */         }
/*  609 */         else if (idcMsg.m_stringKey != null)
/*      */         {
/*  611 */           appendStringOrSimpleText(buf, idcMsg, cxt);
/*      */         }
/*  613 */         else if (idcMsg.m_msgSimple != null)
/*      */         {
/*  615 */           buf.append(idcMsg.m_msgSimple);
/*      */         }
/*  617 */         else if (idcMsg.m_msgEncoded != null)
/*      */         {
/*  619 */           IdcMessage newMsg = LocaleUtils.parseMessage(idcMsg.m_msgEncoded);
/*  620 */           localize(buf, newMsg, cxt, flags);
/*      */         }
/*      */         else
/*      */         {
/*  624 */           String msg = "Illegal message object " + idcMsg + " found.";
/*  625 */           Report.deprecatedUsage(msg);
/*  626 */           if (SystemUtils.m_isDevelopmentEnvironment)
/*      */           {
/*  628 */             throw new AssertionError("!$" + msg);
/*      */           }
/*      */         }
/*  631 */         lastMsg = idcMsg;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String[] getLocaleStringArrayInternal(String resource, IdcLocale locale)
/*      */   {
/*  647 */     String strArr = resource;
/*  648 */     if (!m_disableLocalization)
/*      */     {
/*  650 */       strArr = getLocaleStringInternal(resource, locale);
/*      */     }
/*  652 */     if (strArr == null)
/*      */     {
/*  654 */       return null;
/*      */     }
/*      */ 
/*  657 */     Vector resultV = StringUtils.parseArrayEx(strArr, ',', '^', true);
/*  658 */     String[] result = StringUtils.convertListToArray(resultV);
/*  659 */     return result;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String getLocaleStringInternal(String resource, IdcLocale locale)
/*      */   {
/*  669 */     if ((locale != null) && (locale.m_languageId != null) && (locale.m_languageId.length() > 0))
/*      */     {
/*  671 */       resource = locale.m_languageId + "." + resource;
/*      */     }
/*  673 */     if (m_disableLocalization)
/*      */     {
/*  675 */       return resource;
/*      */     }
/*  677 */     return getStringInternal(resource);
/*      */   }
/*      */ 
/*      */   public static String getStringInternal(String resource, ExecutionContext context)
/*      */   {
/*  691 */     IdcLocalizationStrings stringData = m_stringData;
/*  692 */     if (stringData != null)
/*      */     {
/*  694 */       String value = stringData.getString(resource, context);
/*  695 */       if (value != null)
/*      */       {
/*  697 */         return value;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  704 */     CharSequence seq = addContextInfoToStringKey(resource, context);
/*      */     String value;
/*      */     String value;
/*  705 */     if (seq instanceof String)
/*      */     {
/*  707 */       value = getStringInternal((String)seq);
/*      */     }
/*      */     else
/*      */     {
/*  711 */       value = getStringInternal(seq.toString());
/*      */     }
/*  713 */     return value;
/*      */   }
/*      */ 
/*      */   public static String getStringInternal(String resource)
/*      */   {
/*  726 */     return getStringAttribute(resource, null);
/*      */   }
/*      */ 
/*      */   public static String getStringAttribute(String resource, String attribute, ExecutionContext context)
/*      */   {
/*  743 */     CharSequence seq = addContextInfoToStringKey(resource, context);
/*      */     String value;
/*      */     String value;
/*  745 */     if (seq instanceof String)
/*      */     {
/*  747 */       value = getStringAttribute((String)seq, attribute);
/*      */     }
/*      */     else
/*      */     {
/*  751 */       value = getStringAttribute(seq.toString(), attribute);
/*      */     }
/*  753 */     return value;
/*      */   }
/*      */ 
/*      */   public static String getStringAttribute(String resource, String attribute)
/*      */   {
/*  769 */     int langSepIndex = resource.lastIndexOf(".");
/*  770 */     int appIndex = resource.lastIndexOf("/");
/*  771 */     if ((appIndex < langSepIndex) && (appIndex > -1))
/*      */     {
/*  773 */       langSepIndex = -1;
/*      */     }
/*  775 */     IdcMessage isMissing = null;
/*      */ 
/*  778 */     String lang = m_baseLanguage;
/*  779 */     String app = m_defaultApp;
/*      */     String idWithApp;
/*  780 */     if ((langSepIndex >= 0) && (appIndex >= 0))
/*      */     {
/*  782 */       lang = resource.substring(0, langSepIndex);
/*  783 */       String id = resource.substring(langSepIndex + 1, appIndex);
/*  784 */       String idWithApp = resource.substring(langSepIndex + 1);
/*  785 */       app = resource.substring(appIndex + 1);
/*      */     }
/*      */     else
/*      */     {
/*      */       String idWithApp;
/*  787 */       if (langSepIndex >= 0)
/*      */       {
/*  789 */         lang = resource.substring(0, langSepIndex);
/*      */         String id;
/*  790 */         idWithApp = id = resource.substring(langSepIndex + 1);
/*      */       }
/*  792 */       else if (appIndex >= 0)
/*      */       {
/*      */         String id;
/*  794 */         String idWithApp = id = resource.substring(0, appIndex);
/*  795 */         app = resource.substring(appIndex + 1);
/*      */       }
/*      */       else
/*      */       {
/*  799 */         id = resource; idWithApp = resource;
/*      */       }
/*      */     }
/*  801 */     IdcLocalizationStrings stringData = m_stringData;
/*  802 */     if (stringData != null)
/*      */     {
/*  804 */       appIndex = -1;
/*  805 */       if (app != null)
/*      */       {
/*  807 */         appIndex = stringData.m_applicationMap.getCode(app);
/*      */       }
/*      */ 
/*  810 */       IdcPerfectHash languages = stringData.m_languageMap;
/*  811 */       String result = null;
/*  812 */       int langIndex = stringData.m_defaultLanguageIndex;
/*      */       int length;
/*  813 */       if (attribute != null)
/*      */       {
/*  816 */         langIndex = stringData.m_languageMap.size();
/*      */ 
/*  818 */         result = stringData.getString(id, langIndex, -1);
/*  819 */         List l = StringUtils.makeListFromSequenceSimple(result);
/*  820 */         result = null;
/*  821 */         length = attribute.length();
/*  822 */         for (String st : l)
/*      */         {
/*  824 */           if ((st.length() > length) && (st.startsWith(attribute)) && (st.charAt(length) == '='))
/*      */           {
/*  826 */             result = st.substring(length + 1);
/*  827 */             break;
/*      */           }
/*      */         }
/*      */       }
/*  831 */       else if (lang == null)
/*      */       {
/*  833 */         result = stringData.getString(id, langIndex, appIndex);
/*      */       }
/*      */ 
/*  836 */       while ((result == null) && (attribute == null))
/*      */       {
/*  838 */         langIndex = languages.getCode(lang);
/*  839 */         result = stringData.getString(id, langIndex, appIndex);
/*  840 */         if (result != null)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  846 */         if (isMissing == null)
/*      */         {
/*  848 */           isMissing = IdcMessageFactory.lc("syStringUndefined", new Object[] { id, lang });
/*      */         }
/*  850 */         langSepIndex = lang.lastIndexOf(45);
/*  851 */         if (langSepIndex == -1)
/*      */         {
/*  853 */           if (langIndex == stringData.m_defaultLanguageIndex)
/*      */             break;
/*  855 */           result = stringData.getString(id, stringData.m_defaultLanguageIndex, appIndex); break;
/*      */         }
/*      */ 
/*  859 */         lang = lang.substring(0, langSepIndex);
/*      */       }
/*  861 */       if ((isMissing != null) || (result == null))
/*      */       {
/*  863 */         Map missing = m_missingStrings;
/*  864 */         if (missing != null)
/*      */         {
/*  866 */           Object[] info = new Object[2];
/*  867 */           info[0] = isMissing;
/*  868 */           info[1] = new StackTrace();
/*  869 */           missing.put(resource, info);
/*      */         }
/*      */       }
/*      */ 
/*  873 */       return result;
/*      */     }
/*      */ 
/*  876 */     String initialResource = resource;
/*  877 */     String result = null;
/*      */ 
/*  879 */     Map strings = m_stringObjMap;
/*  880 */     if (strings == null)
/*      */     {
/*  882 */       return null;
/*      */     }
/*  884 */     String id = idWithApp;
/*  885 */     String originalId = id;
/*  886 */     int idLength = id.length();
/*      */ 
/*  888 */     IdcLocaleString lcString = (IdcLocaleString)strings.get(id);
/*  889 */     if (lcString == null)
/*      */     {
/*  891 */       int j = id.indexOf(47);
/*  892 */       if (j > 0)
/*      */       {
/*  899 */         id = id.substring(0, j);
/*  900 */         idLength = j;
/*  901 */         lcString = (IdcLocaleString)strings.get(id);
/*  902 */         if (lcString != null)
/*      */         {
/*  910 */           originalId = id;
/*      */         }
/*      */       }
/*  913 */       if (lcString == null)
/*      */       {
/*  916 */         IdcStringBuilder tmpBuilder = new IdcStringBuilder(id);
/*  917 */         idLength = id.length();
/*  918 */         tmpBuilder.m_disableToStringReleaseBuffers = true;
/*      */ 
/*  920 */         String[] apps = m_apps;
/*      */ 
/*  922 */         for (j = 0; j < apps.length; ++j)
/*      */         {
/*  924 */           tmpBuilder.append2('/', apps[j]);
/*  925 */           lcString = (IdcLocaleString)strings.get(tmpBuilder.toString());
/*  926 */           if (lcString != null) {
/*      */             break;
/*      */           }
/*      */ 
/*  930 */           tmpBuilder.setLength(idLength);
/*      */         }
/*  932 */         tmpBuilder.releaseBuffers();
/*      */       }
/*      */     }
/*      */ 
/*  936 */     if (lcString == null)
/*      */     {
/*  938 */       if (SystemUtils.m_verbose)
/*      */       {
/*  940 */         Report.debug("localization", "unable to find string '" + initialResource + "'", null);
/*      */       }
/*      */ 
/*  944 */       isMissing = IdcMessageFactory.lc("syStringUndefined", new Object[] { id });
/*      */     }
/*  946 */     else if (!id.equals(originalId))
/*      */     {
/*  948 */       if (SystemUtils.m_verbose)
/*      */       {
/*  950 */         Report.debug("localization", "fell back to key '" + id + "' for key '" + originalId + "'", null);
/*      */       }
/*      */ 
/*  954 */       isMissing = IdcMessageFactory.lc("syStringAppUndefined", new Object[] { originalId, id });
/*      */     }
/*      */ 
/*  957 */     if (isMissing != null)
/*      */     {
/*  959 */       Map missing = m_missingStrings;
/*  960 */       if (missing != null)
/*      */       {
/*  962 */         Object[] info = new Object[2];
/*  963 */         info[0] = isMissing;
/*  964 */         info[1] = new StackTrace();
/*  965 */         missing.put(originalId, info);
/*      */       }
/*      */     }
/*  968 */     if (lcString == null)
/*      */     {
/*  970 */       return null;
/*      */     }
/*      */ 
/*  973 */     if (attribute != null)
/*      */     {
/*  975 */       result = lcString.getAttribute(attribute);
/*  976 */       return result;
/*      */     }
/*      */     int langIndex;
/*  979 */     int origLangIndex = langIndex = m_languages.getCode(lang);
/*      */     while (true)
/*      */     {
/*  982 */       if (langIndex >= 0)
/*      */       {
/*  984 */         result = lcString.getLangValue(langIndex);
/*  985 */         if (result != null) {
/*      */           break;
/*      */         }
/*      */       }
/*      */ 
/*  990 */       if (lang.equals(m_baseLanguage))
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  995 */       int j = lang.lastIndexOf(46);
/*  996 */       if (j == -1)
/*      */       {
/*  998 */         lang = m_baseLanguage;
/*      */       }
/*      */       else
/*      */       {
/* 1002 */         lang = lang.substring(0, j);
/*      */       }
/* 1004 */       langIndex = m_languages.getCode(lang);
/*      */     }
/* 1006 */     if ((result != null) && (langIndex != origLangIndex) && (origLangIndex >= 0))
/*      */     {
/* 1009 */       lcString.setLangValue(origLangIndex, result);
/*      */     }
/* 1011 */     return result;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void resetStringCache()
/*      */   {
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void updateStringCache(String key, String value)
/*      */   {
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String prependLocale(String key, ExecutionContext cxt)
/*      */   {
/* 1032 */     CharSequence seq = addContextInfoToStringKeyEx(key, cxt, 1);
/*      */ 
/* 1034 */     if (seq instanceof String)
/*      */     {
/* 1036 */       return (String)seq;
/*      */     }
/* 1038 */     return seq.toString();
/*      */   }
/*      */ 
/*      */   public static CharSequence addContextInfoToStringKey(CharSequence keyName, ExecutionContext context)
/*      */   {
/* 1044 */     CharSequence seq = addContextInfoToStringKeyEx(keyName, context, 3);
/*      */ 
/* 1046 */     return seq;
/*      */   }
/*      */ 
/*      */   public static CharSequence addContextInfoToStringKeyEx(CharSequence key, ExecutionContext cxt, int flags)
/*      */   {
/* 1052 */     if (cxt == null)
/*      */     {
/* 1054 */       return key;
/*      */     }
/* 1056 */     IdcStringBuilder builder = null;
/* 1057 */     if ((flags & 0x1) != 0)
/*      */     {
/* 1059 */       Object obj = cxt.getLocaleResource(1);
/* 1060 */       int prefixLength = 0;
/* 1061 */       String prefix = null;
/* 1062 */       if ((obj != null) && (obj instanceof String))
/*      */       {
/* 1064 */         prefix = (String)obj;
/* 1065 */         prefixLength = prefix.length();
/*      */       }
/*      */ 
/* 1069 */       if (prefixLength <= 0)
/*      */       {
/* 1071 */         obj = cxt.getLocaleResource(0);
/* 1072 */         if (obj == null)
/*      */         {
/* 1074 */           obj = m_systemLocale;
/*      */         }
/* 1076 */         if ((obj != null) && (obj instanceof IdcLocale))
/*      */         {
/* 1078 */           IdcLocale locale = (IdcLocale)obj;
/* 1079 */           prefix = locale.m_languageId;
/* 1080 */           if (prefix != null)
/*      */           {
/* 1082 */             prefixLength = prefix.length();
/*      */           }
/*      */         }
/*      */       }
/* 1086 */       if (prefixLength > 0)
/*      */       {
/* 1088 */         builder = new IdcStringBuilder(128);
/* 1089 */         builder.append(prefix);
/* 1090 */         builder.append('.');
/* 1091 */         builder.append(key);
/*      */       }
/*      */     }
/* 1094 */     if ((flags & 0x2) != 0)
/*      */     {
/* 1096 */       String appName = null;
/* 1097 */       Object app = cxt.getLocaleResource(5);
/* 1098 */       if ((app != null) && (app instanceof String))
/*      */       {
/* 1100 */         appName = (String)app;
/*      */       }
/* 1102 */       if ((appName == null) && (m_defaultApp != null))
/*      */       {
/* 1104 */         appName = m_defaultApp;
/*      */       }
/*      */ 
/* 1107 */       if (appName != null)
/*      */       {
/* 1109 */         if (builder == null)
/*      */         {
/* 1111 */           builder = new IdcStringBuilder(128);
/* 1112 */           builder.append(key);
/*      */         }
/* 1114 */         builder.append('/');
/* 1115 */         builder.append(appName);
/*      */       }
/*      */     }
/*      */ 
/* 1119 */     if (builder != null)
/*      */     {
/* 1121 */       return builder;
/*      */     }
/* 1123 */     return key;
/*      */   }
/*      */ 
/*      */   public static CharSequence removeContextInfoFromStringKey(CharSequence key, ExecutionContext cxt, int flags)
/*      */   {
/* 1129 */     int keyLength = key.length();
/* 1130 */     int first = 0; int last = keyLength;
/* 1131 */     if ((0 == flags) || (0 != (flags & 0x1)))
/*      */     {
/* 1133 */       for (int index = keyLength - 1; index >= 0; --index)
/*      */       {
/* 1135 */         if ('.' != key.charAt(index))
/*      */           continue;
/* 1137 */         first = index + 1;
/* 1138 */         break;
/*      */       }
/*      */     }
/*      */ 
/* 1142 */     if ((0 == flags) || (0 != (flags & 0x2)))
/*      */     {
/* 1144 */       for (int index = first; index < keyLength; ++index)
/*      */       {
/* 1146 */         if ('/' != key.charAt(index))
/*      */           continue;
/* 1148 */         last = index;
/* 1149 */         break;
/*      */       }
/*      */     }
/*      */ 
/* 1153 */     if ((0 == first) && (last == keyLength))
/*      */     {
/* 1155 */       return key;
/*      */     }
/* 1157 */     return key.subSequence(first, last);
/*      */   }
/*      */ 
/*      */   public static CharSequence getContextInfoFromStringKey(CharSequence key, ExecutionContext cxt, int flags)
/*      */   {
/* 1163 */     int keyLength = key.length();
/* 1164 */     if (0 != (flags & 0x1))
/*      */     {
/* 1166 */       int first = 0;
/* 1167 */       for (int index = keyLength - 1; index >= 0; --index)
/*      */       {
/* 1169 */         if ('.' != key.charAt(index))
/*      */           continue;
/* 1171 */         first = index;
/* 1172 */         break;
/*      */       }
/*      */ 
/* 1175 */       return key.subSequence(0, first);
/*      */     }
/* 1177 */     if (0 != (flags & 0x2))
/*      */     {
/* 1179 */       int last = keyLength;
/* 1180 */       for (int index = 0; index < keyLength; ++index)
/*      */       {
/* 1182 */         if ('/' != key.charAt(index))
/*      */           continue;
/* 1184 */         last = index + 1;
/* 1185 */         break;
/*      */       }
/*      */ 
/* 1188 */       return key.subSequence(last, keyLength);
/*      */     }
/* 1190 */     return "";
/*      */   }
/*      */ 
/*      */   public static String getString(String key, ExecutionContext cxt)
/*      */   {
/* 1195 */     return getString(key, cxt, new Object[0]);
/*      */   }
/*      */ 
/*      */   public static String getString(String key, ExecutionContext cxt, Object arg1)
/*      */   {
/* 1201 */     Object[] args = { arg1 };
/* 1202 */     return getString(key, cxt, args);
/*      */   }
/*      */ 
/*      */   public static String getString(String key, ExecutionContext cxt, Object arg1, Object arg2)
/*      */   {
/* 1208 */     Object[] args = { arg1, arg2 };
/* 1209 */     return getString(key, cxt, args);
/*      */   }
/*      */ 
/*      */   public static String getString(String origkey, ExecutionContext cxt, Object[] args)
/*      */   {
/* 1215 */     if (origkey == null)
/*      */     {
/* 1217 */       origkey = "syNullPointerException";
/*      */     }
/* 1219 */     String keyValue = getStringInternal(origkey, cxt);
/*      */     int lengthEstimate;
/*      */     int lengthEstimate;
/* 1220 */     if (keyValue == null)
/*      */     {
/* 1222 */       lengthEstimate = origkey.length() + 32;
/*      */     }
/*      */     else
/*      */     {
/* 1226 */       lengthEstimate = keyValue.length() + 32;
/*      */     }
/* 1228 */     for (int i = 0; i < args.length; ++i)
/*      */     {
/* 1230 */       if (args[i] instanceof String)
/*      */       {
/* 1232 */         lengthEstimate += ((String)args[i]).length();
/*      */       }
/*      */       else
/*      */       {
/* 1236 */         lengthEstimate += 64;
/*      */       }
/*      */     }
/* 1239 */     IdcStringBuilder builder = new IdcStringBuilder(lengthEstimate);
/* 1240 */     appendString(builder, origkey, cxt, args);
/* 1241 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public static IdcAppendable appendString(IdcAppendable appendable, String origkey, ExecutionContext cxt, Object[] args)
/*      */   {
/* 1247 */     IdcMessage msg = IdcMessageFactory.lc(origkey, args);
/* 1248 */     return appendStringOrSimpleText(appendable, msg, cxt);
/*      */   }
/*      */ 
/*      */   public static IdcAppendable appendStringOrSimpleText(IdcAppendable appendable, IdcMessage msg, ExecutionContext context)
/*      */   {
/* 1254 */     if (context == null)
/*      */     {
/* 1256 */       context = m_defaultContext;
/*      */     }
/* 1258 */     String origkey = msg.m_stringKey;
/* 1259 */     Object[] args = msg.m_args;
/* 1260 */     if ((origkey == null) || (origkey.length() == 0))
/*      */     {
/* 1262 */       String retVal = (origkey == null) ? "<lcnullkey>" : "";
/* 1263 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1265 */         ServiceException e = new ServiceException(retVal);
/* 1266 */         Report.debug("system", null, e);
/*      */       }
/* 1268 */       if (appendable == null)
/*      */       {
/* 1270 */         appendable = new IdcStringBuilder(retVal);
/*      */       }
/*      */       else
/*      */       {
/* 1274 */         appendable.append(retVal);
/*      */       }
/* 1276 */       return appendable;
/*      */     }
/*      */ 
/* 1279 */     String s = null;
/*      */ 
/* 1281 */     IdcLocalizationStrings stringData = m_stringData;
/* 1282 */     if ((!m_disableLocalization) && (stringData != null))
/*      */     {
/* 1284 */       s = stringData.getString(origkey, context);
/*      */     }
/*      */ 
/* 1287 */     if (s == null)
/*      */     {
/* 1289 */       CharSequence keySequence = addContextInfoToStringKey(origkey, context);
/*      */       String key;
/*      */       String key;
/* 1291 */       if (keySequence instanceof String)
/*      */       {
/* 1293 */         key = (String)keySequence;
/*      */       }
/*      */       else
/*      */       {
/* 1297 */         key = keySequence.toString();
/*      */       }
/* 1299 */       if (!m_disableLocalization)
/*      */       {
/* 1301 */         s = getStringInternal(key);
/*      */       }
/*      */     }
/*      */ 
/* 1305 */     if (s == null)
/*      */     {
/* 1307 */       if ((!m_disableLocalization) && (msg.m_msgSimple != null))
/*      */       {
/* 1309 */         appendable.append(msg.m_msgSimple);
/* 1310 */         return appendable;
/*      */       }
/*      */ 
/* 1313 */       if ((args == null) || (args.length == 0))
/*      */       {
/* 1315 */         appendable.append(origkey);
/* 1316 */         return appendable;
/*      */       }
/*      */ 
/* 1319 */       appendable.append(origkey);
/* 1320 */       appendable.append("(");
/* 1321 */       for (int i = 0; i < args.length; ++i)
/*      */       {
/* 1323 */         if (i > 0)
/*      */         {
/* 1325 */           appendable.append(",");
/*      */         }
/* 1327 */         if ((args[i] != null) && (args[i] instanceof Calendar))
/*      */         {
/* 1329 */           Date d = ((Calendar)args[i]).getTime();
/* 1330 */           args[i] = d;
/*      */         }
/* 1332 */         if ((args[i] != null) && (args[i] instanceof Date))
/*      */         {
/* 1334 */           m_iso8601Format.format(appendable, (Date)args[i], UTC, 0);
/*      */         }
/* 1339 */         else if (args[i] instanceof CharSequence)
/*      */         {
/* 1341 */           appendable.append((CharSequence)args[i]);
/*      */         }
/* 1343 */         else if (appendable instanceof IdcStringBuilder)
/*      */         {
/* 1345 */           ((IdcStringBuilder)appendable).appendObject(args[i]);
/*      */         }
/*      */         else
/*      */         {
/* 1349 */           appendable.append(args[i].toString());
/*      */         }
/*      */       }
/*      */ 
/* 1353 */       appendable.append(")");
/* 1354 */       return appendable;
/*      */     }
/*      */ 
/* 1357 */     appendable = appendSubstituteString(appendable, s, args, context);
/*      */ 
/* 1359 */     return appendable;
/*      */   }
/*      */ 
/*      */   public static String substituteString(String s, Object[] args, ExecutionContext cxt)
/*      */   {
/* 1364 */     IdcAppendable appendable = appendSubstituteString(null, s, args, cxt);
/*      */ 
/* 1366 */     if (appendable != null)
/*      */     {
/* 1368 */       return appendable.toString();
/*      */     }
/* 1370 */     return s;
/*      */   }
/*      */ 
/*      */   public static IdcAppendable appendSubstituteString(IdcAppendable appendable, String s, Object[] args, ExecutionContext cxt)
/*      */   {
/* 1376 */     if (cxt == null)
/*      */     {
/* 1378 */       cxt = m_defaultContext;
/*      */     }
/* 1380 */     IdcAppendable l = appendable;
/* 1381 */     String r = s;
/*      */     while (true)
/*      */     {
/* 1384 */       if ((i = r.indexOf("{")) < 0)
/*      */         break label181;
/* 1386 */       if (l == null)
/*      */       {
/* 1388 */         int lengthEstimate = s.length();
/* 1389 */         for (int j = 0; j < args.length; ++j)
/*      */         {
/* 1391 */           if (args[j] instanceof String)
/*      */           {
/* 1393 */             lengthEstimate += 10 + ((String)args[j]).length();
/*      */           }
/*      */           else
/*      */           {
/* 1397 */             lengthEstimate += 64;
/*      */           }
/*      */         }
/* 1400 */         l = new IdcStringBuilder(lengthEstimate);
/*      */       }
/* 1402 */       l.append(r.substring(0, i));
/* 1403 */       r = r.substring(i + 1);
/* 1404 */       int i = r.indexOf("}");
/* 1405 */       if (i <= 0)
/*      */         break;
/* 1407 */       String arg = r.substring(0, i);
/* 1408 */       r = r.substring(i + 1);
/*      */ 
/* 1410 */       l.append(handleArgument(arg, args, cxt));
/*      */     }
/*      */ 
/* 1414 */     return null;
/*      */ 
/* 1418 */     if (l == null)
/*      */     {
/* 1420 */       label181: return null;
/*      */     }
/*      */ 
/* 1423 */     l.append(r);
/* 1424 */     return l;
/*      */   }
/*      */ 
/*      */   public static String handleArgument(String arg, Object[] args, ExecutionContext cxt)
/*      */   {
/* 1429 */     Object value = null;
/* 1430 */     int index = 0;
/* 1431 */     int length = arg.length();
/* 1432 */     char type = ' ';
/* 1433 */     while ((index < length) && (Character.isDigit(arg.charAt(index))))
/*      */     {
/* 1435 */       ++index;
/*      */     }
/*      */ 
/* 1438 */     if (arg.charAt(0) == '{')
/*      */     {
/* 1440 */       return "{";
/*      */     }
/* 1442 */     int nbr = NumberUtils.parseInteger(arg.substring(0, index), -1);
/* 1443 */     if (index < arg.length())
/*      */     {
/* 1445 */       type = arg.charAt(index);
/* 1446 */       arg = arg.substring(index + 1);
/*      */     }
/* 1448 */     --nbr;
/* 1449 */     if ((cxt != null) && (cxt.getCachedObject("ConvertToJavaStandardForm") != null))
/*      */     {
/* 1451 */       String argSubstitute = "";
/*      */ 
/* 1455 */       if (type != '?')
/*      */       {
/* 1457 */         Map map = (Map)cxt.getCachedObject("ConvertToJavaStandardTypes");
/* 1458 */         map.put("" + nbr, type + arg);
/* 1459 */         argSubstitute = "{" + nbr + "}";
/*      */       }
/* 1461 */       return argSubstitute;
/*      */     }
/* 1463 */     if ((nbr >= 0) && (nbr < args.length))
/*      */     {
/* 1465 */       value = args[nbr];
/*      */     }
/*      */     else
/*      */     {
/* 1469 */       return m_insufficentArgumentsError;
/*      */     }
/*      */ 
/* 1472 */     if (value == null)
/*      */     {
/* 1474 */       return m_nullSubstitutionString;
/*      */     }
/*      */ 
/* 1477 */     return handleArgumentRaw(value, arg, type, cxt);
/*      */   }
/*      */ 
/*      */   public static String handleArgumentRaw(Object value, String arg, char type, ExecutionContext cxt)
/*      */   {
/* 1482 */     IdcStringBuilder result = new IdcStringBuilder();
/* 1483 */     int radix = 0;
/*      */     boolean quoted;
/*      */     int i;
/*      */     Iterator i$;
/* 1484 */     switch (type)
/*      */     {
/*      */     case ' ':
/* 1487 */       if (value instanceof IdcMessage)
/*      */       {
/* 1489 */         localize(result, (IdcMessage)value, cxt, 0);
/* 1490 */         break label1785:
/*      */       }
/* 1492 */       if (value instanceof IdcMessageContainer)
/*      */       {
/* 1494 */         IdcMessageContainer container = (IdcMessageContainer)value;
/* 1495 */         IdcMessage msg = container.getIdcMessage();
/* 1496 */         if (msg != null)
/*      */         {
/* 1498 */           localize(result, msg, cxt, 0);
/* 1499 */           break label1785:
/*      */         }
/*      */       }
/* 1502 */       if (value instanceof Throwable)
/*      */       {
/* 1504 */         Throwable t = (Throwable)value;
/* 1505 */         String tmp = t.getMessage();
/* 1506 */         if (tmp.charAt(0) == '!')
/*      */         {
/* 1508 */           IdcMessage msg = LocaleUtils.parseMessage(tmp);
/* 1509 */           if (msg != null)
/*      */           {
/* 1511 */             localize(result, msg, cxt, 0);
/* 1512 */             break label1785:
/*      */           }
/*      */         }
/* 1515 */         tmp = t.getLocalizedMessage();
/* 1516 */         if (tmp == null)
/*      */         {
/* 1518 */           tmp = t.getMessage();
/*      */         }
/* 1520 */         if (tmp != null)
/*      */         {
/* 1522 */           result.append(tmp);
/* 1523 */           break label1785:
/*      */         }
/*      */       }
/*      */ 
/* 1526 */       result.append(value.toString());
/* 1527 */       break;
/*      */     case 'x':
/* 1530 */       radix = 16;
/*      */     case 'i':
/*      */       try
/*      */       {
/* 1534 */         long l = 0L;
/* 1535 */         if (value instanceof Long)
/*      */         {
/* 1537 */           l = ((Long)value).longValue();
/*      */         }
/* 1539 */         else if (value instanceof Integer)
/*      */         {
/* 1541 */           l = ((Integer)value).intValue();
/*      */         }
/* 1543 */         else if (value instanceof String)
/*      */         {
/* 1545 */           IdcStringBuilder tmp = new IdcStringBuilder((String)value);
/* 1546 */           l = Long.parseLong(tmp.getTrimmedString(0, -1));
/*      */         }
/*      */         else
/*      */         {
/* 1550 */           Report.trace("localization", "unknown object type " + value.getClass().getName(), null);
/*      */ 
/* 1552 */           l = Long.parseLong(value.toString());
/*      */         }
/* 1554 */         if (radix == 0)
/*      */         {
/* 1556 */           result.append(localizeInteger(l, cxt));
/*      */         }
/*      */         else
/*      */         {
/* 1560 */           result.append(Long.toString(l, radix));
/*      */         }
/*      */       }
/*      */       catch (NumberFormatException e)
/*      */       {
/* 1565 */         result.append(m_numberFormatError);
/*      */       }
/* 1567 */       break;
/*      */     case 'T':
/*      */       try
/*      */       {
/*      */         Date d;
/*      */         Date d;
/* 1573 */         if (value instanceof Date)
/*      */         {
/* 1575 */           d = (Date)value;
/*      */         }
/*      */         else
/*      */         {
/*      */           Date d;
/* 1577 */           if (value instanceof Long)
/*      */           {
/* 1579 */             d = new Date(((Long)value).longValue());
/*      */           }
/*      */           else
/*      */           {
/* 1583 */             d = LocaleUtils.parseODBC(value.toString());
/*      */           }
/*      */         }
/* 1585 */         result.append(localizeDate(d, cxt));
/*      */       }
/*      */       catch (ServiceException ignore)
/*      */       {
/* 1589 */         result.append(value.toString());
/*      */       }
/* 1591 */       break;
/*      */     case 't':
/* 1594 */       result.append(localizeDate(value, cxt));
/* 1595 */       break;
/*      */     case 'D':
/*      */       try
/*      */       {
/*      */         Date d;
/*      */         Date d;
/* 1601 */         if (value instanceof Date)
/*      */         {
/* 1603 */           d = (Date)value;
/*      */         }
/*      */         else
/*      */         {
/*      */           Date d;
/* 1605 */           if (value instanceof Long)
/*      */           {
/* 1607 */             d = new Date(((Long)value).longValue());
/*      */           }
/*      */           else
/*      */           {
/* 1611 */             d = LocaleUtils.parseODBC(value.toString());
/*      */           }
/*      */         }
/* 1613 */         result.append(localizeDateOnly(d, cxt));
/*      */       }
/*      */       catch (ServiceException ignore)
/*      */       {
/* 1617 */         result.append(value.toString());
/*      */       }
/* 1619 */       break;
/*      */     case 'd':
/* 1622 */       result.append(localizeDateOnly(value, cxt));
/* 1623 */       break;
/*      */     case 'f':
/*      */       try
/*      */       {
/*      */         double d;
/*      */         double d;
/* 1629 */         if (value instanceof Double)
/*      */         {
/* 1631 */           d = ((Double)value).doubleValue();
/*      */         }
/*      */         else
/*      */         {
/*      */           double d;
/* 1633 */           if (value instanceof Float)
/*      */           {
/* 1635 */             d = ((Float)value).floatValue();
/*      */           }
/*      */           else
/*      */           {
/*      */             double d;
/* 1637 */             if (value instanceof Long)
/*      */             {
/* 1639 */               d = ((Long)value).longValue();
/*      */             }
/*      */             else
/*      */             {
/*      */               double d;
/* 1641 */               if (value instanceof Integer)
/*      */               {
/* 1643 */                 d = ((Integer)value).intValue();
/*      */               }
/*      */               else
/*      */               {
/* 1647 */                 d = Double.valueOf(value.toString()).doubleValue();
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/* 1649 */         int nbr = NumberUtils.parseInteger(arg, -1);
/* 1650 */         result.append(localizeFloat(d, nbr, cxt));
/*      */       }
/*      */       catch (NumberFormatException e)
/*      */       {
/* 1654 */         result.append(m_numberFormatError);
/*      */       }
/* 1656 */       break;
/*      */     case 'k':
/* 1660 */       boolean quoted = arg.equals("q");
/* 1661 */       if (quoted)
/*      */       {
/* 1663 */         result.append(getString("syBeginQuote", cxt));
/*      */       }
/* 1665 */       if (value instanceof IdcMessage)
/*      */       {
/* 1667 */         localize(result, (IdcMessage)value, cxt, 0);
/*      */       }
/* 1669 */       else if (value instanceof IdcMessageContainer)
/*      */       {
/* 1671 */         localize(result, ((IdcMessageContainer)value).getIdcMessage(), cxt, 0);
/*      */       }
/* 1673 */       else if (value.toString().startsWith("!"))
/*      */       {
/* 1675 */         result.append(localizeMessage(value.toString(), cxt));
/*      */       }
/*      */       else
/*      */       {
/* 1679 */         result.append(getString(value.toString(), cxt));
/*      */       }
/* 1681 */       if (!quoted)
/*      */         break label1785;
/* 1683 */       result.append(getString("syEndQuote", cxt)); break;
/*      */     case 'm':
/* 1690 */       quoted = arg.equals("q");
/* 1691 */       if (quoted)
/*      */       {
/* 1693 */         result.append(getString("syBeginQuote", cxt));
/*      */       }
/* 1695 */       if (value instanceof IdcMessage)
/*      */       {
/* 1697 */         localize(result, (IdcMessage)value, cxt, 0);
/*      */       }
/* 1699 */       else if (value instanceof IdcMessageContainer)
/*      */       {
/* 1701 */         localize(result, ((IdcMessageContainer)value).getIdcMessage(), cxt, 0);
/*      */       }
/*      */       else
/*      */       {
/* 1705 */         result.append(localizeMessage(value.toString(), cxt));
/*      */       }
/* 1707 */       if (!quoted)
/*      */         break label1785;
/* 1709 */       result.append(getString("syEndQuote", cxt)); break;
/*      */     case '?':
/* 1718 */       if (value instanceof Object[])
/*      */       {
/* 1720 */         quoted = ((Object[])(Object[])value).length;
/*      */       }
/*      */       double dVal;
/*      */       double dVal;
/* 1722 */       if (value instanceof Collection)
/*      */       {
/* 1724 */         dVal = ((Collection)value).size();
/*      */       }
/*      */       else
/*      */       {
/*      */         double dVal;
/* 1726 */         if (value.toString().length() == 0)
/*      */         {
/* 1728 */           dVal = 1.0D;
/*      */         }
/*      */         else
/*      */         {
/*      */           try
/*      */           {
/* 1734 */             dVal = ScriptUtils.getDoubleVal(value, cxt);
/*      */           }
/*      */           catch (NumberFormatException ignore)
/*      */           {
/* 1738 */             dVal = 2.0D;
/*      */           }
/*      */         }
/*      */       }
/* 1741 */       result.append(LocaleUtils.handleConditional(dVal, arg));
/* 1742 */       break;
/*      */     case 'K':
/*      */     case 'M':
/*      */     case 'l':
/*      */       Collection collection;
/*      */       Collection collection;
/* 1749 */       if (value instanceof Collection)
/*      */       {
/* 1751 */         collection = (Collection)value;
/*      */       }
/*      */       else
/*      */       {
/* 1755 */         collection = StringUtils.makeListFromSequenceSimple(value.toString());
/*      */       }
/* 1757 */       i = 0;
/* 1758 */       for (i$ = collection.iterator(); i$.hasNext(); ) { Object item = i$.next();
/*      */ 
/* 1760 */         if (i++ > 0)
/*      */         {
/* 1762 */           if (type == 'M')
/*      */           {
/* 1764 */             result.append("  ");
/*      */           }
/*      */           else
/*      */           {
/* 1768 */             result.append(", ");
/*      */           }
/*      */         }
/*      */ 
/* 1772 */         if ((type == 'M') || (type == 'K'))
/*      */         {
/* 1774 */           if (item instanceof IdcMessage)
/*      */           {
/* 1776 */             IdcMessage idcmsg = (IdcMessage)item;
/* 1777 */             localizeMessage(result, idcmsg, cxt);
/*      */           }
/* 1779 */           else if (item instanceof IdcMessageContainer)
/*      */           {
/* 1781 */             IdcMessage idcmsg = ((IdcMessageContainer)item).getIdcMessage();
/* 1782 */             localizeMessage(result, idcmsg, cxt);
/*      */           }
/*      */           else
/*      */           {
/*      */             String itemStr;
/*      */             String itemStr;
/* 1786 */             if (type == 'M')
/*      */             {
/* 1788 */               itemStr = localizeMessage(item.toString(), cxt);
/*      */             }
/*      */             else
/*      */             {
/* 1792 */               itemStr = getString(item.toString(), cxt);
/*      */             }
/* 1794 */             result.append(itemStr);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1799 */           String itemStr = item.toString();
/* 1800 */           result.append(itemStr);
/*      */         } }
/*      */ 
/* 1803 */       break;
/*      */     case 'q':
/* 1808 */       if (value.toString().length() == 0)
/*      */       {
/* 1810 */         result.append(getString("syUndefined", cxt)); break label1785:
/*      */       }
/*      */ 
/* 1814 */       result.append(getString("syBeginQuote", cxt));
/* 1815 */       IdcMessage msg = null;
/* 1816 */       if (value instanceof IdcMessage)
/*      */       {
/* 1818 */         msg = (IdcMessage)value;
/*      */       }
/* 1820 */       else if (value instanceof IdcMessageContainer)
/*      */       {
/* 1822 */         msg = ((IdcMessageContainer)value).getIdcMessage();
/*      */       }
/* 1824 */       else if (value instanceof Throwable)
/*      */       {
/* 1826 */         Throwable t = (Throwable)value;
/* 1827 */         String tmp = t.getMessage();
/* 1828 */         if (tmp.charAt(0) == '!')
/*      */         {
/* 1830 */           msg = LocaleUtils.parseMessage(tmp);
/*      */         }
/* 1832 */         if (msg == null)
/*      */         {
/* 1834 */           tmp = t.getLocalizedMessage();
/* 1835 */           if (tmp == null)
/*      */           {
/* 1837 */             tmp = t.getMessage();
/*      */           }
/* 1839 */           msg = IdcMessageFactory.lc();
/* 1840 */           msg.m_msgLocalized = tmp;
/*      */         }
/*      */       }
/* 1843 */       if (msg != null)
/*      */       {
/* 1845 */         localize(result, msg, cxt, 0);
/*      */       }
/*      */       else
/*      */       {
/* 1849 */         result.append(value.toString());
/*      */       }
/* 1851 */       result.append(getString("syEndQuote", cxt));
/*      */ 
/* 1853 */       break;
/*      */     case 'o':
/* 1858 */       int intValue = 0;
/* 1859 */       if (value instanceof Double)
/*      */       {
/* 1861 */         intValue = (int)((Double)value).doubleValue();
/*      */       }
/* 1863 */       else if (value instanceof Float)
/*      */       {
/* 1865 */         intValue = (int)((Float)value).floatValue();
/*      */       }
/* 1867 */       else if (value instanceof Long)
/*      */       {
/* 1869 */         intValue = (int)((Long)value).longValue();
/*      */       }
/* 1871 */       else if (value instanceof Integer)
/*      */       {
/* 1873 */         intValue = ((Integer)value).intValue();
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/* 1879 */           intValue = Integer.parseInt(value.toString());
/*      */         }
/*      */         catch (NumberFormatException ignore)
/*      */         {
/* 1883 */           Report.trace("localization", null, ignore);
/* 1884 */           result.append(m_numberFormatError);
/* 1885 */           break label1785:
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1888 */       result.append(processOrdinalSubstitution(intValue, cxt));
/* 1889 */       break;
/*      */     case 'e':
/*      */       try
/*      */       {
/* 1896 */         int[] intervals = null;
/* 1897 */         if (value instanceof IntervalData)
/*      */         {
/* 1899 */           intervals = ((IntervalData)value).getParsedInterval();
/*      */         }
/*      */         else
/*      */         {
/* 1903 */           long interval = Long.parseLong(value.toString());
/* 1904 */           intervals = IntervalData.parseInterval(interval);
/*      */         }
/* 1906 */         result.append(processIntervalData(intervals, cxt));
/*      */       }
/*      */       catch (NumberFormatException ignore)
/*      */       {
/* 1910 */         Report.trace("localization", null, ignore);
/* 1911 */         result.append(m_numberFormatError);
/*      */       }
/* 1913 */       break;
/*      */     default:
/* 1917 */       result.append(m_invalidFlagError);
/*      */     }
/*      */ 
/* 1921 */     label1785: return result.toString();
/*      */   }
/*      */ 
/*      */   protected static String processIntervalData(int[] intervals, ExecutionContext cxt)
/*      */   {
/* 1926 */     String[] unitStrings = { "syIntervalNanoseconds", "syIntervalMicorseconds", "syIntervalMilliseconds", "syIntervalSeconds", "syIntervalMinutes", "syIntervalHours", "syIntervalDays" };
/*      */ 
/* 1929 */     int highMark = 0;
/* 1930 */     for (int i = intervals.length - 1; i >= 0; --i)
/*      */     {
/* 1932 */       if (intervals[i] == 0)
/*      */         continue;
/* 1934 */       highMark = i;
/* 1935 */       break;
/*      */     }
/*      */ 
/* 1939 */     IdcStringBuilder builder = new IdcStringBuilder();
/*      */ 
/* 1942 */     int precision = 3;
/* 1943 */     if (highMark < 4)
/*      */     {
/* 1945 */       precision = 2;
/*      */     }
/* 1947 */     int i = highMark; for (int j = 0; (i >= 0) && (j < precision); ++j)
/*      */     {
/* 1949 */       if (j > 0)
/*      */       {
/* 1951 */         builder.append(' ');
/*      */       }
/* 1953 */       String unitDisplay = getString(unitStrings[i], cxt, new Object[] { new Integer(intervals[i]) });
/* 1954 */       builder.append(unitDisplay);
/*      */ 
/* 1947 */       --i;
/*      */     }
/*      */ 
/* 1956 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   protected static String processOrdinalSubstitution(int value, ExecutionContext cxt)
/*      */   {
/* 1963 */     String msg = null;
/* 1964 */     Object[] rules = (Object[])(Object[])cxt.getCachedObject("OrdinalRules");
/* 1965 */     if (rules == null)
/*      */     {
/* 1967 */       String rulesString = getStringInternal("syOrdinalRules", cxt);
/* 1968 */       if (rulesString == null)
/*      */       {
/* 1970 */         Report.trace("localization", "syOrdinalRules is not defined.", null);
/*      */       }
/* 1972 */       Vector v = StringUtils.parseArray(rulesString, ',', '^');
/* 1973 */       rules = new Object[v.size()];
/* 1974 */       for (int i = 0; i < rules.length; ++i)
/*      */       {
/* 1976 */         Vector r = StringUtils.parseArray((String)v.elementAt(i), ':', '^');
/* 1977 */         if (r.size() != 2)
/*      */         {
/* 1979 */           Report.trace("localization", "illegal ordinal rule '" + v.elementAt(i) + "'", null);
/*      */         }
/*      */         else
/*      */         {
/* 1983 */           String[] rule = { (String)r.elementAt(0), (String)r.elementAt(1) };
/*      */ 
/* 1985 */           rules[i] = rule;
/*      */         }
/*      */       }
/* 1988 */       cxt.setCachedObject("OrdinalRules", rules);
/*      */     }
/*      */ 
/* 1991 */     String stringValue = "" + value;
/* 1992 */     for (int i = 0; i < rules.length; ++i)
/*      */     {
/* 1994 */       Object[] rule = (Object[])(Object[])rules[i];
/* 1995 */       String pattern = (String)rule[0];
/*      */ 
/* 1997 */       if ((pattern.startsWith(".*")) && (stringValue.endsWith(pattern.substring(2))))
/*      */       {
/* 2000 */         msg = (String)rule[1];
/* 2001 */         break;
/*      */       }
/* 2003 */       if ((pattern.endsWith(".*")) && (stringValue.startsWith(pattern.substring(0, pattern.length() - 2))))
/*      */       {
/* 2006 */         msg = (String)rule[1];
/* 2007 */         break;
/*      */       }
/* 2009 */       if (!pattern.equals(stringValue))
/*      */         continue;
/* 2011 */       msg = (String)rule[1];
/* 2012 */       break;
/*      */     }
/*      */ 
/* 2015 */     if (msg != null)
/*      */     {
/* 2017 */       stringValue = substituteString(msg, new Object[] { stringValue }, cxt);
/*      */     }
/*      */ 
/* 2020 */     return stringValue;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcDateFormat createDateFormat(String id)
/*      */     throws ServiceException
/*      */   {
/*      */     String parsePattern;
/*      */     String parsePattern;
/* 2032 */     if (id.startsWith("SDF:"))
/*      */     {
/* 2034 */       parsePattern = id.substring("SDF:".length());
/*      */     }
/*      */     else
/*      */     {
/* 2038 */       String tmp = id;
/* 2039 */       if (tmp.startsWith("IDF:"))
/*      */       {
/* 2041 */         tmp = id.substring("IDF:".length());
/*      */       }
/* 2043 */       Vector v = StringUtils.parseArray(tmp, ',', '^');
/* 2044 */       int size = v.size();
/* 2045 */       switch (size)
/*      */       {
/*      */       case 1:
/* 2048 */         parsePattern = (String)v.elementAt(0);
/* 2049 */         break;
/*      */       case 2:
/* 2051 */         parsePattern = (String)v.elementAt(0);
/*      */ 
/* 2053 */         break;
/*      */       default:
/* 2055 */         String msg = LocaleUtils.encodeMessage("syIllegalDateFormat", null, id);
/* 2056 */         throw new ServiceException(msg);
/*      */       }
/*      */     }
/*      */ 
/* 2060 */     IdcDateFormat fmt = new IdcDateFormat();
/*      */     try
/*      */     {
/* 2064 */       IdcNumberFormat nbrFmt = new IdcNumberFormat();
/* 2065 */       nbrFmt.setGroupingUsed(false);
/* 2066 */       nbrFmt.setParseIntegerOnly(true);
/* 2067 */       fmt.init(parsePattern, m_systemTimeZone, m_systemTimeZoneFormat, nbrFmt);
/*      */     }
/*      */     catch (ParseException e)
/*      */     {
/* 2072 */       String msg = LocaleUtils.encodeMessage("syUnableToCreateDateFormat", e.getMessage(), parsePattern);
/*      */ 
/* 2074 */       throw new ServiceException(msg);
/*      */     }
/* 2076 */     return fmt;
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat createDateFormatFromPattern(String pattern, ExecutionContext cxt) throws ServiceException
/*      */   {
/* 2081 */     IdcDateFormat userFormat = getUserDateFormat(cxt);
/* 2082 */     IdcDateFormat srcFmt = null;
/* 2083 */     IdcDateFormat fmt = null;
/*      */ 
/* 2085 */     IdcStringBuilder tempBuilder = new IdcStringBuilder(50);
/* 2086 */     tempBuilder.append("DP:");
/* 2087 */     String languageID = "noID";
/* 2088 */     if (cxt != null)
/*      */     {
/* 2090 */       Object o = cxt.getLocaleResource(1);
/* 2091 */       if ((o != null) && (o instanceof String))
/*      */       {
/* 2093 */         languageID = (String)o;
/*      */       }
/*      */     }
/* 2096 */     tempBuilder.append(languageID);
/* 2097 */     tempBuilder.append(":");
/* 2098 */     tempBuilder.append(pattern);
/*      */ 
/* 2100 */     String cacheKey = tempBuilder.toString();
/*      */ 
/* 2104 */     Object o = AppObjectRepository.getObject("dateFormats");
/* 2105 */     Map h = null;
/*      */ 
/* 2107 */     if ((o == null) || (o instanceof Map))
/*      */     {
/* 2109 */       if (o != null)
/*      */       {
/* 2111 */         h = (Map)o;
/*      */       }
/*      */       else
/*      */       {
/* 2115 */         h = new HashMap();
/* 2116 */         AppObjectRepository.putObject("dateFormats", h);
/*      */       }
/*      */     }
/* 2119 */     if (userFormat == null)
/*      */     {
/* 2121 */       userFormat = new IdcDateFormat();
/*      */     }
/*      */ 
/* 2124 */     if (h != null)
/*      */     {
/* 2126 */       srcFmt = (IdcDateFormat)h.get(cacheKey);
/* 2127 */       if (srcFmt != null)
/*      */       {
/* 2129 */         fmt = srcFmt.shallowClone();
/*      */       }
/*      */     }
/* 2132 */     if (fmt == null)
/*      */     {
/* 2134 */       fmt = userFormat.shallowClone();
/*      */       try
/*      */       {
/* 2137 */         fmt.setPattern(pattern);
/*      */       }
/*      */       catch (ParseStringException e)
/*      */       {
/* 2141 */         String msg = LocaleUtils.encodeMessage("syUnableToCreateDateFormat", null, pattern);
/*      */ 
/* 2143 */         throw new ServiceException(msg, e);
/*      */       }
/* 2145 */       if (h != null)
/*      */       {
/* 2147 */         srcFmt = fmt.shallowClone();
/* 2148 */         h.put(cacheKey, srcFmt);
/*      */       }
/*      */     }
/*      */ 
/* 2152 */     return fmt;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcTimeZone parseTimeZone(Properties props)
/*      */     throws ServiceException
/*      */   {
/* 2160 */     TimeZone tz = parseTZ(props);
/* 2161 */     return IdcTimeZone.wrap(tz);
/*      */   }
/*      */ 
/*      */   public static TimeZone parseTZ(Properties props)
/*      */     throws ServiceException
/*      */   {
/* 2167 */     String name = props.getProperty("lcTimeZone");
/* 2168 */     String configuration = props.getProperty("lcTimeZoneConfiguration");
/*      */     try
/*      */     {
/* 2171 */       TimeZone tz = (TimeZone)m_systemTimeZoneFormat.parseObject(configuration);
/* 2172 */       int index = name.indexOf(",");
/* 2173 */       if (index > 0)
/*      */       {
/* 2175 */         tz.setID(name.substring(0, index));
/*      */       }
/*      */       else
/*      */       {
/* 2179 */         tz.setID(name);
/*      */       }
/* 2181 */       return tz;
/*      */     }
/*      */     catch (ParseException e)
/*      */     {
/* 2185 */       throw new ServiceException("!csUnableToParseTimeZone");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String internationalizeDate(Object value, ExecutionContext cxt)
/*      */   {
/* 2191 */     if ((value == null) || ((value instanceof String) && (value.toString().length() == 0)))
/*      */     {
/* 2193 */       return "";
/*      */     }
/* 2195 */     if (cxt == null)
/*      */     {
/* 2197 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2200 */     Date d = getDateObjectForLocalization(value, cxt);
/* 2201 */     if (d == null)
/*      */     {
/* 2203 */       return "";
/*      */     }
/*      */ 
/* 2206 */     return m_odbcFormat.format(d);
/*      */   }
/*      */ 
/*      */   public static String localizeDate(Object value, ExecutionContext cxt)
/*      */   {
/* 2211 */     if ((value == null) || ((value instanceof String) && (value.toString().length() == 0)))
/*      */     {
/* 2213 */       return "";
/*      */     }
/* 2215 */     if (cxt == null)
/*      */     {
/* 2217 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2220 */     Date d = getDateObjectForLocalization(value, cxt);
/* 2221 */     if (d == null)
/*      */     {
/* 2223 */       return m_dateFormatError;
/*      */     }
/*      */ 
/* 2226 */     return localizeDateInternal(d, cxt, 0);
/*      */   }
/*      */ 
/*      */   public static String localizeDateOnly(Object value, ExecutionContext cxt)
/*      */   {
/* 2231 */     if (value == null)
/*      */     {
/* 2233 */       return "";
/*      */     }
/* 2235 */     if (cxt == null)
/*      */     {
/* 2237 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2240 */     Date d = getDateObjectForLocalization(value, cxt);
/* 2241 */     if (d == null)
/*      */     {
/* 2243 */       return m_dateFormatError;
/*      */     }
/*      */ 
/* 2246 */     return localizeDateInternal(d, cxt, 1);
/*      */   }
/*      */ 
/*      */   public static String localizeTimeOnly(Object value, ExecutionContext cxt)
/*      */   {
/* 2251 */     if (value == null)
/*      */     {
/* 2253 */       return "";
/*      */     }
/* 2255 */     if (cxt == null)
/*      */     {
/* 2257 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2260 */     Date d = getDateObjectForLocalization(value, cxt);
/* 2261 */     if (d == null)
/*      */     {
/* 2263 */       return m_dateFormatError;
/*      */     }
/*      */ 
/* 2266 */     return localizeDateInternal(d, cxt, 2);
/*      */   }
/*      */ 
/*      */   public static String localizeInteger(long l, ExecutionContext cxt)
/*      */   {
/* 2271 */     CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 2272 */     return clh.formatInteger(l, cxt);
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat getUserDateFormat(ExecutionContext cxt)
/*      */   {
/* 2277 */     IdcDateFormat format = null;
/* 2278 */     if (cxt == null)
/*      */     {
/* 2280 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2283 */     format = (IdcDateFormat)cxt.getLocaleResource(3);
/* 2284 */     if (format == null)
/*      */     {
/* 2286 */       format = m_odbcFormat;
/*      */     }
/* 2288 */     return format;
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat getUserDisplayDateFormat(ExecutionContext cxt)
/*      */   {
/* 2293 */     IdcDateFormat format = null;
/* 2294 */     IdcLocale locale = null;
/* 2295 */     if (cxt == null)
/*      */     {
/* 2297 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2300 */     locale = (IdcLocale)cxt.getLocaleResource(0);
/* 2301 */     if (locale != null)
/*      */     {
/* 2303 */       if (locale.m_displayDateFormat != null)
/*      */       {
/* 2305 */         format = locale.m_displayDateFormat;
/*      */       }
/*      */       else
/*      */       {
/* 2309 */         format = locale.m_dateFormat;
/*      */       }
/*      */     }
/* 2312 */     if (format == null)
/*      */     {
/* 2314 */       format = m_odbcFormat;
/*      */     }
/* 2316 */     return format;
/*      */   }
/*      */ 
/*      */   protected static Date getDateObjectForLocalization(Object value, ExecutionContext cxt)
/*      */   {
/* 2322 */     Date d = null;
/*      */ 
/* 2324 */     if (value instanceof String)
/*      */     {
/* 2326 */       String sVal = (String)value;
/*      */ 
/* 2331 */       IdcDateFormat fmt = (IdcDateFormat)cxt.getLocaleResource(3);
/* 2332 */       TimeZone tz = null;
/* 2333 */       TimeZoneFormat tzf = null;
/* 2334 */       if (fmt != null)
/*      */       {
/* 2339 */         tz = fmt.m_timeZone;
/* 2340 */         tzf = fmt.m_timeZoneFormat;
/*      */       }
/*      */ 
/* 2343 */       ParseStringLocation parsePosition = new ParseStringLocation();
/* 2344 */       if ((sVal.startsWith("{ts '")) && (sVal.endsWith("'}")))
/*      */       {
/* 2350 */         d = m_odbcFormat.parseDateFull(null, parsePosition, sVal, null, null, 0);
/*      */       } else {
/* 2352 */         if ((sVal.startsWith("{ts ")) && (sVal.endsWith("}")))
/*      */         {
/* 2355 */           long ts = Long.parseLong(sVal.substring("{ts ".length(), sVal.length() - "}".length()));
/*      */ 
/* 2358 */           return new Date(ts);
/*      */         }
/*      */ 
/* 2363 */         boolean isRetryable = true;
/* 2364 */         if (fmt != null)
/*      */         {
/* 2366 */           d = fmt.parseDateFull(null, parsePosition, sVal, null, tzf, 0);
/* 2367 */           isRetryable = isRetryableDateParseError(parsePosition);
/*      */         }
/*      */ 
/* 2370 */         if (isRetryable)
/*      */         {
/* 2372 */           parsePosition = parsePosition.createRetryCopy();
/* 2373 */           d = m_iso8601Format.parseDateFull(null, parsePosition, sVal, tz, tzf, 0);
/*      */ 
/* 2376 */           isRetryable = isRetryableDateParseError(parsePosition);
/*      */         }
/* 2378 */         if (isRetryable)
/*      */         {
/* 2380 */           parsePosition = parsePosition.createRetryCopy();
/* 2381 */           d = m_legacyFormat.parseDateFull(null, parsePosition, sVal, tz, tzf, 0);
/*      */         }
/*      */       }
/*      */     }
/* 2385 */     else if (value instanceof Date)
/*      */     {
/* 2387 */       d = (Date)value;
/*      */     }
/* 2389 */     else if (value instanceof Calendar)
/*      */     {
/* 2391 */       Calendar cal = (Calendar)value;
/* 2392 */       d = cal.getTime();
/*      */     }
/*      */ 
/* 2395 */     return d;
/*      */   }
/*      */ 
/*      */   protected static String localizeDateInternal(Date d, ExecutionContext cxt, int type)
/*      */   {
/* 2400 */     if (d == null)
/*      */     {
/* 2402 */       return m_nullSubstitutionString;
/*      */     }
/*      */ 
/* 2406 */     IdcDateFormat format = null;
/* 2407 */     if (cxt != null)
/*      */     {
/* 2409 */       format = (IdcDateFormat)cxt.getLocaleResource(3);
/*      */     }
/* 2411 */     if (format == null)
/*      */     {
/* 2413 */       format = m_systemDateFormat;
/*      */     }
/*      */ 
/* 2416 */     String r = format.format(d, type);
/* 2417 */     return r;
/*      */   }
/*      */ 
/*      */   public static String localizeFloat(double d, int n, ExecutionContext cxt)
/*      */   {
/* 2423 */     IdcNumberFormat format = new IdcNumberFormat();
/* 2424 */     if (n > -1)
/*      */     {
/* 2426 */       format.setMaximumFractionDigits(n);
/* 2427 */       format.setMinimumFractionDigits(n);
/*      */     }
/* 2429 */     String r = format.format(d);
/* 2430 */     return r;
/*      */   }
/*      */ 
/*      */   public static void initializeLocale(IdcLocale locale, Properties props)
/*      */     throws ServiceException
/*      */   {
/* 2436 */     locale.init(props);
/* 2437 */     if (!locale.m_isEnabled)
/*      */     {
/* 2439 */       return;
/*      */     }
/*      */ 
/* 2444 */     initializeInternalStrings(locale);
/*      */ 
/* 2446 */     for (String key : m_systemTimeZoneFormat.m_timeZones.keySet())
/*      */     {
/* 2448 */       TimeZone tz = getTimeZone(key);
/* 2449 */       locale.m_tzFormat.addTZ(key, tz);
/*      */     }
/*      */ 
/* 2452 */     if ((locale.m_dateTimePattern == null) || (locale.m_dateTimePattern.length() == 0))
/*      */     {
/* 2454 */       locale.m_dateTimePattern = m_systemDateFormat.toPattern();
/*      */     }
/*      */     else
/*      */     {
/* 2460 */       locale.m_dateTimePattern = LocaleUtils.addOptionalElements(locale.m_dateTimePattern);
/*      */     }
/*      */ 
/* 2463 */     m_locales.put(locale.m_name, locale);
/*      */ 
/* 2465 */     TimeZone tz = null;
/* 2466 */     if (locale.m_tzId != null)
/*      */     {
/* 2468 */       tz = getTimeZone(locale.m_tzId);
/* 2469 */       if ((tz == null) && (locale.m_tzId.trim().length() > 0))
/*      */       {
/* 2471 */         String msg = getString("apTimeZoneNotFound", null, locale.m_tzId, locale.m_name);
/* 2472 */         Report.trace("localization", msg, null);
/*      */       }
/*      */     }
/* 2475 */     if (tz == null)
/*      */     {
/* 2477 */       tz = m_systemTimeZone;
/*      */     }
/*      */     try
/*      */     {
/* 2481 */       initializeDateFormatForLocale(locale.m_dateFormat, tz, locale.m_dateTimePattern, locale);
/* 2482 */       if ((locale.m_displayDatePattern != null) && (locale.m_displayDatePattern.trim().length() >= 0))
/*      */       {
/* 2484 */         locale.m_displayDateFormat = new IdcDateFormat();
/* 2485 */         initializeDateFormatForLocale(locale.m_displayDateFormat, tz, locale.m_displayDatePattern, locale);
/*      */       }
/* 2487 */       if ((locale.m_alternateParseDatePatterns != null) && (locale.m_alternateParseDatePatterns.trim().length() > 0))
/*      */       {
/* 2489 */         Vector v = StringUtils.parseArray(locale.m_alternateParseDatePatterns, '|', '^');
/* 2490 */         String[] values = StringUtils.convertListToArray(v);
/* 2491 */         locale.m_alternateParseDateFormats = new IdcDateFormat[values.length];
/* 2492 */         for (int i = 0; i < values.length; ++i)
/*      */         {
/* 2494 */           locale.m_alternateParseDateFormats[i] = new IdcDateFormat();
/* 2495 */           initializeDateFormatForLocale(locale.m_alternateParseDateFormats[i], tz, values[i], locale);
/*      */         }
/* 2497 */         locale.m_dateFormat.m_alternateParsingFormats = locale.m_alternateParseDateFormats;
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 2502 */       String msg = LocaleUtils.encodeMessage("syUnableToLoadLocale", e.getMessage(), locale.m_name);
/*      */ 
/* 2504 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void initializeInternalStrings(IdcLocale locale)
/*      */   {
/* 2511 */     IdcLocaleInternalStrings internalStrings = new IdcLocaleInternalStrings();
/* 2512 */     internalStrings.m_shortMonthNames = getLocaleStringArrayInternal("syShortMonthNames", locale);
/* 2513 */     internalStrings.m_longMonthNames = getLocaleStringArrayInternal("syLongMonthNames", locale);
/* 2514 */     internalStrings.m_shortWeekNames = getLocaleStringArrayInternal("syShortWeekNames", locale);
/* 2515 */     internalStrings.m_longWeekNames = getLocaleStringArrayInternal("syLongWeekNames", locale);
/* 2516 */     locale.m_internalStrings = internalStrings;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void initializeDateForLocale(IdcDateFormat dateFormat, IdcTimeZone tz, String pattern, IdcLocale locale)
/*      */     throws ServiceException
/*      */   {
/* 2524 */     initializeDateFormatForLocale(dateFormat, tz, pattern, locale);
/*      */   }
/*      */ 
/*      */   public static void initializeDateFormatForLocale(IdcDateFormat dateFormat, TimeZone tz, String pattern, IdcLocale locale)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 2533 */       dateFormat.init(pattern, tz, locale.m_tzFormat, locale.getNumberFormat());
/* 2534 */       dateFormat.m_internalStrings = locale.m_internalStrings;
/*      */     }
/*      */     catch (ParseStringException e)
/*      */     {
/* 2538 */       String msg = LocaleUtils.encodeMessage("syIllegalDateFormat", e.getMessage(), pattern);
/*      */ 
/* 2540 */       throw new ServiceException(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addLocaleAlias(String alias, String localeId)
/*      */   {
/* 2546 */     IdcLocale exists = (IdcLocale)m_locales.get(alias);
/* 2547 */     IdcLocale locale = (IdcLocale)m_locales.get(localeId);
/* 2548 */     if ((exists != null) || (locale == null))
/*      */       return;
/* 2550 */     m_locales.put(alias, locale);
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat getDateFormat(String key)
/*      */   {
/* 2556 */     if (key.equals("SystemDateFormat"))
/*      */     {
/* 2558 */       return m_systemDateFormat;
/*      */     }
/* 2560 */     IdcDateFormat fmt = (IdcDateFormat)m_localeDateFormats.get(key);
/* 2561 */     return fmt;
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat getSystemDateFormat()
/*      */   {
/* 2567 */     return m_systemDateFormat;
/*      */   }
/*      */ 
/*      */   public static IdcDateFormat buildDateFormat(String fmtString) throws ServiceException
/*      */   {
/* 2572 */     IdcDateFormat idcfmt = new IdcDateFormat();
/*      */     try
/*      */     {
/* 2575 */       idcfmt.init(fmtString, m_systemTimeZone, m_systemTimeZoneFormat, new IdcNumberFormat());
/*      */     }
/*      */     catch (ParseException e)
/*      */     {
/* 2580 */       throw new ServiceException("!apUnableBuildDateFormat", e);
/*      */     }
/*      */ 
/* 2583 */     return idcfmt;
/*      */   }
/*      */ 
/*      */   public static IdcLocale getLocale(String name)
/*      */   {
/* 2588 */     IdcLocale locale = (IdcLocale)m_locales.get(name);
/* 2589 */     if (locale == null)
/*      */     {
/* 2591 */       name = LocaleUtils.normalizeId(name);
/* 2592 */       locale = (IdcLocale)m_locales.get(name);
/*      */     }
/* 2594 */     if (locale == null)
/*      */     {
/*      */       int index;
/*      */       do
/*      */       {
/* 2599 */         index = name.lastIndexOf(45);
/* 2600 */         if (index <= 0)
/*      */           continue;
/* 2602 */         name = name.substring(0, index);
/* 2603 */         locale = (IdcLocale)m_locales.get(name);
/*      */       }
/*      */ 
/* 2606 */       while ((index > 0) && (locale == null));
/*      */     }
/*      */ 
/* 2609 */     return locale;
/*      */   }
/*      */ 
/*      */   public static IdcLocale getLocaleWithoutTrim(String name)
/*      */   {
/* 2621 */     IdcLocale locale = (IdcLocale)m_locales.get(name);
/* 2622 */     if (locale == null)
/*      */     {
/* 2624 */       name = LocaleUtils.normalizeId(name);
/* 2625 */       locale = (IdcLocale)m_locales.get(name);
/*      */     }
/* 2627 */     return locale;
/*      */   }
/*      */ 
/*      */   public static IdcLocale getSystemLocale()
/*      */   {
/* 2633 */     return m_systemLocale;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcTimeZone addTimeZone(Properties props)
/*      */     throws ServiceException
/*      */   {
/* 2641 */     TimeZone tz = parseTimeZone(props);
/* 2642 */     IdcTimeZone itz = IdcTimeZone.wrap(tz);
/*      */ 
/* 2644 */     addTimeZone(tz.getID(), itz);
/* 2645 */     addTimeZone(tz.getDisplayName(false, 0), itz);
/* 2646 */     addTimeZone(tz.getDisplayName(true, 0), itz);
/* 2647 */     return itz;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void addTimeZone(String name, IdcTimeZone tz)
/*      */   {
/* 2654 */     if (name.equals("SystemTimeZone"))
/*      */     {
/* 2656 */       m_systemTimeZone = tz;
/*      */     }
/* 2658 */     m_systemTimeZoneFormat.addTimeZone(name, tz);
/*      */   }
/*      */ 
/*      */   public static Date parseDate(String origText, ExecutionContext cxt) throws ServiceException
/*      */   {
/* 2663 */     return parseDateEx(origText, cxt, null, false);
/*      */   }
/*      */ 
/*      */   public static Date parseDateDataEntry(String origText, ExecutionContext cxt, String traceContext) throws ServiceException
/*      */   {
/* 2668 */     return parseDateEx(origText, cxt, traceContext, true);
/*      */   }
/*      */ 
/*      */   public static Date parseDateEx(String origText, ExecutionContext cxt, String traceContext, boolean tryAlternateFormats)
/*      */     throws ServiceException
/*      */   {
/* 2674 */     Date d = null;
/* 2675 */     if (cxt == null)
/*      */     {
/* 2677 */       cxt = m_defaultContext;
/*      */     }
/* 2679 */     ParseStringLocation location = new ParseStringLocation();
/* 2680 */     d = parseDateImplement(origText, cxt, tryAlternateFormats, location);
/*      */ 
/* 2682 */     if (d == null)
/*      */     {
/* 2684 */       String parseErrMsg = location.getFirstErrorMessage();
/* 2685 */       String errMsg = LocaleUtils.encodeMessage("syUnableToParseDate", parseErrMsg, origText);
/* 2686 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2688 */         if (traceContext == null)
/*      */         {
/* 2690 */           traceContext = "parseDateEx";
/*      */         }
/* 2692 */         String msg = createDateTraceReport(traceContext, origText, location);
/* 2693 */         Report.debug("system", msg, null);
/*      */       }
/*      */ 
/* 2696 */       throw new ServiceException(errMsg);
/*      */     }
/* 2698 */     return d;
/*      */   }
/*      */ 
/*      */   public static Date parseDateImplement(String origText, ExecutionContext cxt, boolean tryAlternateFormats, ParseStringLocation location)
/*      */   {
/* 2711 */     ParseStringLocation workingLoc = location;
/* 2712 */     if (workingLoc == null)
/*      */     {
/* 2714 */       workingLoc = new ParseStringLocation();
/*      */     }
/* 2716 */     if (origText.startsWith("{ts '"))
/*      */     {
/* 2718 */       Date d = m_odbcFormat.parseDateFull(null, workingLoc, origText, null, null, 0);
/* 2719 */       return d;
/*      */     }
/* 2721 */     if ((origText.startsWith("{ts ")) && (origText.endsWith("}")))
/*      */     {
/* 2724 */       long ts = Long.parseLong(origText.substring("{ts ".length(), origText.length() - "}".length()));
/*      */ 
/* 2727 */       return new Date(ts);
/*      */     }
/*      */ 
/* 2730 */     IdcDateFormat fmt = (IdcDateFormat)cxt.getLocaleResource(3);
/* 2731 */     if (fmt == null)
/*      */     {
/* 2733 */       fmt = m_systemDateFormat;
/*      */     }
/* 2735 */     Date d = fmt.parseDateFull(null, workingLoc, origText, null, null, 0);
/* 2736 */     if ((d == null) && (tryAlternateFormats) && (fmt.m_alternateParsingFormats != null) && (isRetryableDateParseError(workingLoc)))
/*      */     {
/* 2741 */       TimeZoneFormat tzf = fmt.m_timeZoneFormat;
/* 2742 */       TimeZone tz = fmt.m_timeZone;
/* 2743 */       for (int i = 0; i < fmt.m_alternateParsingFormats.length; ++i)
/*      */       {
/* 2745 */         workingLoc = workingLoc.createRetryCopy();
/* 2746 */         IdcDateFormat altFmt = fmt.m_alternateParsingFormats[i];
/* 2747 */         d = altFmt.parseDateFull(null, workingLoc, origText, tz, tzf, 0);
/* 2748 */         if (d != null) break; if (!isRetryableDateParseError(workingLoc)) {
/*      */           break;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 2754 */     if (location != null)
/*      */     {
/* 2756 */       workingLoc.shallowCopy(location);
/*      */     }
/* 2758 */     return d;
/*      */   }
/*      */ 
/*      */   public static boolean isRetryableDateParseError(ParseStringLocation location)
/*      */   {
/* 2763 */     if (location == null)
/*      */     {
/* 2765 */       return false;
/*      */     }
/*      */ 
/* 2770 */     return (location.m_state != -4) && (location.m_state != 0) && (location.m_state != -8) && (location.m_state != -6);
/*      */   }
/*      */ 
/*      */   public static String createDateTraceReport(String function, String dateToParse, ParseStringLocation parsePosition)
/*      */   {
/* 2778 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 2779 */     buf.append(function);
/* 2780 */     buf.append("(");
/* 2781 */     buf.append(dateToParse);
/* 2782 */     buf.append(")");
/* 2783 */     buf.append(" -> ");
/* 2784 */     appendDateTraceReport(buf, parsePosition, true);
/* 2785 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   public static void appendDateTraceReport(IdcStringBuilder buf, ParseStringLocation parsePosition, boolean includeErrMsg)
/*      */   {
/* 2790 */     boolean isPrevFailures = false;
/* 2791 */     if (parsePosition == null)
/*      */     {
/* 2793 */       return;
/*      */     }
/* 2795 */     if (parsePosition.m_failedParsingLocations != null)
/*      */     {
/* 2797 */       Vector v = parsePosition.m_failedParsingLocations;
/* 2798 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/* 2800 */         ParseStringLocation p = (ParseStringLocation)v.elementAt(i);
/* 2801 */         if (isPrevFailures)
/*      */         {
/* 2803 */           buf.append(" retry: ");
/*      */         }
/* 2805 */         appendDateTraceReport(buf, p, includeErrMsg);
/* 2806 */         isPrevFailures = true;
/*      */       }
/*      */     }
/* 2809 */     if (isPrevFailures)
/*      */     {
/* 2811 */       buf.append(" retry: ");
/*      */     }
/* 2813 */     buf.append("Offset ");
/* 2814 */     buf.append(parsePosition.determineErrorIndex());
/* 2815 */     buf.append(" ");
/* 2816 */     if ((parsePosition.m_activeParsingObject != null) && (parsePosition.m_activeParsingObject instanceof IdcDateFormat))
/*      */     {
/* 2818 */       IdcDateFormat idf = (IdcDateFormat)parsePosition.m_activeParsingObject;
/* 2819 */       buf.append(idf.m_pattern);
/* 2820 */       buf.append(" ");
/*      */     }
/* 2822 */     String state = parsePosition.getStateTraceString();
/* 2823 */     buf.append(state);
/* 2824 */     if (parsePosition.m_elementLocation != null)
/*      */     {
/* 2826 */       buf.append(" ");
/* 2827 */       buf.append(parsePosition.m_elementLocation);
/*      */     }
/* 2829 */     if ((parsePosition.m_elementLocation == null) && (parsePosition.m_objectPartLocation != null))
/*      */     {
/* 2831 */       buf.append(" ");
/* 2832 */       buf.append(parsePosition.m_objectPartLocation);
/*      */     }
/* 2834 */     buf.append(".");
/* 2835 */     if ((!includeErrMsg) || (parsePosition.m_errMsg == null))
/*      */       return;
/* 2837 */     buf.append(" ");
/* 2838 */     String msg = localizeMessage(parsePosition.m_errMsg, null);
/* 2839 */     buf.append(msg);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static IdcTimeZone getTimeZone(String name)
/*      */   {
/* 2847 */     TimeZone tz = getTimeZone(name, null);
/* 2848 */     return IdcTimeZone.wrap(tz);
/*      */   }
/*      */ 
/*      */   public static TimeZone getTimeZone(String name, ExecutionContext cxt)
/*      */   {
/* 2853 */     if (cxt == null)
/*      */     {
/* 2855 */       cxt = m_defaultContext;
/*      */     }
/* 2857 */     if (name.equals("SystemTimeZone"))
/*      */     {
/* 2859 */       return m_systemTimeZone;
/*      */     }
/* 2861 */     TimeZone tz = m_systemTimeZoneFormat.determineTimeZone(name);
/* 2862 */     if (tz == null)
/*      */     {
/* 2864 */       return getSystemTimeZone();
/*      */     }
/* 2866 */     return tz;
/*      */   }
/*      */ 
/*      */   protected static TimeZone getTimeZone(ExecutionContext cxt)
/*      */   {
/* 2871 */     if (cxt == null)
/*      */     {
/* 2873 */       cxt = m_defaultContext;
/*      */     }
/*      */ 
/* 2877 */     TimeZone tz = (TimeZone)cxt.getCachedObject("UserTimeZone");
/* 2878 */     if (tz == null)
/*      */     {
/* 2881 */       tz = TimeZone.getDefault();
/*      */     }
/*      */ 
/* 2884 */     return tz;
/*      */   }
/*      */ 
/*      */   public static TimeZone getSystemTimeZone()
/*      */   {
/* 2889 */     return m_sysZone;
/*      */   }
/*      */ 
/*      */   public static String getEncodingFromAlias(String alias)
/*      */   {
/* 2901 */     if ((m_encodingAliasesMap == null) || (alias == null) || (alias.length() == 0))
/*      */     {
/* 2903 */       return alias;
/*      */     }
/* 2905 */     String encoding = (String)m_encodingAliasesMap.get(alias.toLowerCase());
/* 2906 */     if ((encoding == null) || (encoding.length() == 0))
/*      */     {
/* 2908 */       return alias;
/*      */     }
/* 2910 */     return encoding;
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(String text, ExecutionContext context)
/*      */     throws UnsupportedEncodingException
/*      */   {
/* 2917 */     String encoding = (String)context.getLocaleResource(2);
/* 2918 */     if (encoding == null)
/*      */     {
/* 2920 */       encoding = m_systemLocale.m_pageEncoding;
/*      */     }
/*      */     byte[] bytes;
/*      */     byte[] bytes;
/* 2922 */     if (encoding != null)
/*      */     {
/* 2924 */       String javaEncoding = getEncodingFromAlias(encoding);
/* 2925 */       if (javaEncoding != null)
/*      */       {
/* 2927 */         encoding = javaEncoding;
/*      */       }
/* 2929 */       bytes = text.getBytes(encoding);
/*      */     }
/*      */     else
/*      */     {
/* 2933 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*      */       {
/* 2935 */         throw new AssertionError("!$Unable to compute the system encoding, using JVM default.");
/*      */       }
/*      */ 
/* 2938 */       bytes = text.getBytes();
/*      */     }
/* 2940 */     return bytes;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2945 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96119 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*  133 */     reset();
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LocaleResources
 * JD-Core Version:    0.5.4
 */