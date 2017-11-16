/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcMessageUtils;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.text.DateFormat;
/*     */ import java.text.DateFormatSymbols;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Locale;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class LocaleUtils
/*     */ {
/*     */   public static final String ODBC_BEGIN = "{ts '";
/*     */   public static final String ODBC_END = "'}";
/*     */   public static final int LOCALE = 0;
/*     */   public static final int LANGUAGE = 1;
/*     */   public static final int ENCODING = 2;
/*     */   public static final int DATE_FORMAT = 3;
/*     */   public static final int TIMEZONE = 4;
/*     */   public static final int APPLICATION = 5;
/*     */   public static final int F_USE_LEGACY_OBJECTS = 1;
/*     */   public static IdcDateFormat m_odbcDateFormat;
/*     */   public static IdcDateFormat m_utcOdbcDateFormat;
/*     */   public static IdcMessageFactory m_factory;
/*     */   protected static IdcDateFormat m_rfc1123Format;
/*     */   protected static IdcDateFormat m_rfcMailFormat;
/*  56 */   public static Object m_traceLogFormat = new SimpleDateFormat("MM.dd HH:mm:ss.SSS");
/*     */ 
/*     */   public static void updateDateFormats()
/*     */   {
/*  66 */     m_odbcDateFormat = LocaleResources.m_odbcFormat;
/*  67 */     m_utcOdbcDateFormat = LocaleResources.m_utcOdbcFormat;
/*     */ 
/*  69 */     m_rfc1123Format = LocaleResources.m_rfc1123Format;
/*     */ 
/*  71 */     m_rfcMailFormat = LocaleResources.m_rfcMailFormat;
/*     */   }
/*     */ 
/*     */   public static String normalizeId(String id)
/*     */   {
/*  81 */     char[] buf = new char[id.length()];
/*  82 */     id.getChars(0, buf.length, buf, 0);
/*  83 */     for (int i = 0; i < buf.length; ++i)
/*     */     {
/*  85 */       if (Character.isLetter(buf[i]))
/*     */         continue;
/*  87 */       buf[i] = '-';
/*     */     }
/*     */ 
/*  91 */     id = new String(buf);
/*  92 */     return id.toLowerCase(Locale.ENGLISH);
/*     */   }
/*     */ 
/*     */   public static String appendMessage(String msg, String priorMsg)
/*     */   {
/*  97 */     IdcStringBuilder buf = new IdcStringBuilder();
/*     */ 
/*  99 */     if ((priorMsg != null) && (priorMsg.length() > 0))
/*     */     {
/* 101 */       if (!priorMsg.startsWith("!"))
/*     */       {
/* 103 */         buf.append("!$");
/* 104 */         IdcMessageUtils.appendEscaped(buf, priorMsg, 0);
/*     */       }
/*     */       else
/*     */       {
/* 108 */         buf.append(priorMsg);
/*     */       }
/*     */     }
/* 111 */     if ((msg != null) && (msg.length() > 0))
/*     */     {
/* 113 */       if (!msg.startsWith("!"))
/*     */       {
/* 115 */         buf.append("!$");
/* 116 */         IdcMessageUtils.appendEscaped(buf, msg, 0);
/*     */       }
/*     */       else
/*     */       {
/* 120 */         buf.append(msg);
/*     */       }
/*     */     }
/*     */ 
/* 124 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(IdcMessage msg)
/*     */   {
/* 129 */     return appendMessage(null, msg).toString();
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder appendMessage(IdcStringBuilder builder, IdcMessage msg)
/*     */   {
/* 134 */     if (builder == null)
/*     */     {
/* 136 */       builder = new IdcStringBuilder();
/*     */     }
/* 138 */     IdcMessageUtils.appendMessage(builder, msg, 0);
/* 139 */     return builder;
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder appendUnencodedMessage(IdcStringBuilder builder, String msg)
/*     */   {
/* 144 */     if (builder == null)
/*     */     {
/* 146 */       builder = new IdcStringBuilder();
/*     */     }
/* 148 */     IdcMessageUtils.appendUnencodedMessage(builder, msg, 0);
/* 149 */     return builder;
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(String msg, String priorMsg)
/*     */   {
/* 154 */     return encodeMessage(msg, priorMsg, new String[0]);
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(String msg, String priorMsg, Object arg1)
/*     */   {
/* 159 */     return encodeMessage(msg, priorMsg, new Object[] { arg1 });
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(String msg, String priorMsg, Object arg1, Object arg2)
/*     */   {
/* 165 */     return encodeMessage(msg, priorMsg, new Object[] { arg1, arg2 });
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(String msg, String priorMsg, Object arg1, Object arg2, Object arg3)
/*     */   {
/* 171 */     return encodeMessage(msg, priorMsg, new Object[] { arg1, arg2, arg3 });
/*     */   }
/*     */ 
/*     */   public static String encodeMessage(String key, String priorMsg, Object[] args)
/*     */   {
/* 176 */     return appendMessage(null, key, priorMsg, args).toString();
/*     */   }
/*     */ 
/*     */   public static IdcStringBuilder appendMessage(IdcStringBuilder builder, String key, String priorMsg, Object[] args)
/*     */   {
/* 182 */     if (builder == null)
/*     */     {
/* 184 */       builder = new IdcStringBuilder();
/*     */     }
/* 186 */     IdcMessage msg = IdcMessageFactory.lc();
/* 187 */     msg.m_msgEncoded = priorMsg;
/* 188 */     IdcMessage msg2 = IdcMessageFactory.lc(key, args);
/* 189 */     msg2.m_prior = msg;
/* 190 */     IdcMessageUtils.appendMessage(builder, msg2, 0);
/* 191 */     return builder;
/*     */   }
/*     */ 
/*     */   public static IdcMessage createMessageListFromThrowable(Throwable t)
/*     */   {
/* 196 */     return IdcMessageFactory.lc(t);
/*     */   }
/*     */ 
/*     */   public static String createMessageStringFromThrowable(Throwable t)
/*     */   {
/* 201 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 202 */     appendThrowableMessagesToAppendable(t, builder);
/* 203 */     return builder.toString();
/*     */   }
/*     */ 
/*     */   public static IdcAppendable appendThrowableMessagesToAppendable(Throwable t, IdcAppendable appendable)
/*     */   {
/* 209 */     IdcMessage msg = IdcMessageFactory.lc(t);
/* 210 */     if (appendable == null)
/*     */     {
/* 212 */       appendable = new IdcStringBuilder();
/*     */     }
/* 214 */     IdcMessageUtils.appendMessage(appendable, msg, 0);
/* 215 */     return appendable;
/*     */   }
/*     */ 
/*     */   public static Vector decodeMessage(String msg)
/*     */   {
/* 220 */     return (Vector)decodeMessageInternal(msg, 1);
/*     */   }
/*     */ 
/*     */   public static IdcMessage parseMessage(CharSequence msg)
/*     */   {
/* 225 */     return (IdcMessage)decodeMessageInternal(msg, 0);
/*     */   }
/*     */ 
/*     */   public static Object decodeMessageInternal(CharSequence msg, int flags)
/*     */   {
/* 230 */     return IdcMessageUtils.decodeMessageInternal(m_factory, msg, flags);
/*     */   }
/*     */ 
/*     */   public static Date parseDateMillis(String text)
/*     */   {
/*     */     try
/*     */     {
/* 237 */       long l = Long.parseLong(text);
/* 238 */       return new Date(l);
/*     */     }
/*     */     catch (NumberFormatException ignore)
/*     */     {
/* 242 */       if (SystemUtils.m_verbose)
/*     */       {
/* 244 */         Report.debug("localization", null, ignore);
/*     */       }
/*     */     }
/* 247 */     return null;
/*     */   }
/*     */ 
/*     */   protected static String[] parseCases(String list)
/*     */   {
/* 252 */     List caseList = new ArrayList();
/*     */ 
/* 255 */     while ((index = list.indexOf(":")) >= 0)
/*     */     {
/*     */       int index;
/* 257 */       caseList.add(list.substring(0, index));
/* 258 */       list = list.substring(index + 1);
/*     */     }
/* 260 */     caseList.add(list);
/* 261 */     String[] cases = new String[caseList.size()];
/* 262 */     cases = (String[])(String[])caseList.toArray(cases);
/* 263 */     return cases;
/*     */   }
/*     */ 
/*     */   public static String handleConditional(double d, String caseList)
/*     */   {
/* 268 */     String[] cases = parseCases(caseList);
/* 269 */     String result = "";
/* 270 */     long l = ()d;
/* 271 */     if ((d != 1.0D) && (l == 1L))
/*     */     {
/* 273 */       l = 2L;
/*     */     }
/*     */ 
/* 276 */     if ((l > cases.length) || (l < 1L))
/*     */     {
/* 278 */       result = cases[0];
/*     */     }
/* 280 */     else if (cases.length == 1)
/*     */     {
/* 282 */       result = "";
/*     */     }
/*     */     else
/*     */     {
/* 286 */       result = cases[(cases.length - (int)l)];
/*     */     }
/*     */ 
/* 289 */     return result;
/*     */   }
/*     */ 
/*     */   public static String handleConditional(String value, String caseList)
/*     */   {
/* 294 */     String result = "";
/*     */     try
/*     */     {
/* 298 */       long l = Long.parseLong(value);
/* 299 */       result = handleConditional(l, caseList);
/*     */     }
/*     */     catch (NumberFormatException e)
/*     */     {
/* 304 */       String[] cases = parseCases(caseList);
/* 305 */       if (value.length() > 0)
/*     */       {
/* 307 */         result = cases[0];
/*     */       }
/* 311 */       else if (cases.length > 1)
/*     */       {
/* 313 */         result = cases[1];
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 318 */     return result;
/*     */   }
/*     */ 
/*     */   public static String debugDate(Date date)
/*     */   {
/* 329 */     Object df = m_traceLogFormat;
/* 330 */     if (df instanceof DateFormat)
/*     */     {
/* 333 */       synchronized (df)
/*     */       {
/* 335 */         return ((DateFormat)df).format(date);
/*     */       }
/*     */     }
/* 338 */     return ((IdcDateFormat)df).format(date);
/*     */   }
/*     */ 
/*     */   public static String debugDate(long date)
/*     */   {
/* 343 */     return debugDate(new Date(date));
/*     */   }
/*     */ 
/*     */   public static String formatODBC(Date date)
/*     */   {
/* 348 */     return m_odbcDateFormat.format(date);
/*     */   }
/*     */ 
/*     */   public static Date parseODBC(String str) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 355 */       return m_odbcDateFormat.parseDate(str);
/*     */     }
/*     */     catch (ParseException e)
/*     */     {
/* 359 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String formatUtcODBC(Date date)
/*     */   {
/* 365 */     return m_utcOdbcDateFormat.format(date);
/*     */   }
/*     */ 
/*     */   public static Date parseUtcODBC(String str)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 374 */       return m_utcOdbcDateFormat.parseDate(str);
/*     */     }
/*     */     catch (ParseException e)
/*     */     {
/* 378 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String formatRfcMailDate(Date dte, String format)
/*     */     throws ServiceException
/*     */   {
/* 388 */     String value = null;
/* 389 */     if ((format != null) && (format.length() > 0))
/*     */     {
/* 391 */       IdcDateFormat dateFormat = LocaleResources.createDateFormatFromPattern(format, null);
/* 392 */       value = dateFormat.format(dte);
/*     */     }
/*     */     else
/*     */     {
/* 396 */       value = m_rfcMailFormat.format(dte);
/*     */     }
/* 398 */     return value;
/*     */   }
/*     */ 
/*     */   public static String formatRFC1123Date(Date date)
/*     */   {
/* 403 */     return m_rfc1123Format.format(date);
/*     */   }
/*     */ 
/*     */   public static Date parseRFC1123Date(String str) throws ServiceException
/*     */   {
/* 408 */     str = str.trim();
/* 409 */     int i = str.indexOf(",");
/* 410 */     IdcStringBuilder buf = null;
/* 411 */     if (i > 3)
/*     */     {
/* 413 */       buf = new IdcStringBuilder();
/* 414 */       buf.append(str, 0, 3);
/* 415 */       buf.append(str, i, str.length() - i);
/*     */     }
/*     */ 
/* 418 */     if (str.indexOf("-") >= 0)
/*     */     {
/* 420 */       if (buf == null)
/*     */       {
/* 422 */         buf = new IdcStringBuilder(str);
/*     */       }
/* 424 */       i = 0;
/* 425 */       while ((i = str.indexOf("-", i)) > 0)
/*     */       {
/* 427 */         buf.setCharAt(i, ' ');
/* 428 */         ++i;
/*     */       }
/*     */     }
/* 431 */     if (buf != null)
/*     */     {
/* 433 */       str = buf.toString();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 438 */       return m_rfc1123Format.parseDate(str);
/*     */     }
/*     */     catch (ParseStringException e)
/*     */     {
/* 442 */       String msg = encodeMessage("syUnableToParseDate", null, str);
/*     */ 
/* 444 */       throw new ServiceException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String simpleDateFormatToPattern(SimpleDateFormat sdf)
/*     */   {
/* 450 */     DateFormatSymbols syms = sdf.getDateFormatSymbols();
/* 451 */     String[] ms = syms.getAmPmStrings();
/* 452 */     String ampm = null;
/* 453 */     if ((ms != null) && (ms.length == 2))
/*     */     {
/* 455 */       ampm = "!m" + ms[0] + "," + ms[1];
/*     */     }
/* 457 */     String pattern = sdf.toPattern();
/* 458 */     return pattern + ampm;
/*     */   }
/*     */ 
/*     */   public static String addOptionalElements(String pattern)
/*     */   {
/* 464 */     if ((pattern.indexOf(93) >= 0) || (pattern.indexOf(125) >= 0))
/*     */     {
/* 466 */       return pattern;
/*     */     }
/*     */ 
/* 470 */     String suffix = null;
/* 471 */     int extraRulesIndex = pattern.indexOf(33);
/* 472 */     if (extraRulesIndex > 0)
/*     */     {
/* 474 */       suffix = pattern.substring(extraRulesIndex);
/* 475 */       pattern = pattern.substring(0, extraRulesIndex);
/*     */     }
/*     */ 
/* 480 */     pattern = " " + pattern + " ";
/* 481 */     String[][] yearSegments = { { "/yy ", "{/yy} " }, { "/yyyy ", "{/yyyy} " }, { "-yy ", "{-yy} " }, { "-yyyy ", "{-yyyy} " }, { ".yy ", "{.yy} " }, { ".yyyy ", "{.yyyy} " } };
/*     */ 
/* 491 */     String[][] timeMap = { { "H:mm ", "H:mm[:ss] " }, { "H:mm:ss", "H:mm{:ss}" }, { "h:mm ", "h:mm[:ss] " }, { "h:mm:ss", "h:mm{:ss}" } };
/*     */ 
/* 499 */     String[][] meridianMap = { { " a", "{ a}" } };
/*     */ 
/* 504 */     String[][] tzMap = { { "z", "{z}" } };
/*     */ 
/* 509 */     pattern = substituteMap(pattern, yearSegments);
/*     */ 
/* 512 */     int hourIndex = pattern.indexOf(72);
/* 513 */     int hourIndex2 = pattern.indexOf(104);
/* 514 */     if (hourIndex < 0)
/*     */     {
/* 516 */       hourIndex = hourIndex2;
/*     */     }
/* 518 */     int meridianIndex = pattern.indexOf(" a");
/* 519 */     if (hourIndex >= 0)
/*     */     {
/* 522 */       boolean useMeridianTerminator = false;
/* 523 */       int startIndex = hourIndex;
/* 524 */       if (meridianIndex >= 0)
/*     */       {
/* 526 */         if (startIndex > meridianIndex)
/*     */         {
/* 528 */           startIndex = meridianIndex;
/*     */         }
/*     */         else
/*     */         {
/* 532 */           useMeridianTerminator = true;
/*     */         }
/*     */       }
/* 535 */       if ((startIndex > 1) && (pattern.charAt(startIndex - 1) == ' '))
/*     */       {
/* 537 */         --startIndex;
/*     */       }
/*     */ 
/* 541 */       int endIndex = pattern.length() - 1;
/* 542 */       if (useMeridianTerminator)
/*     */       {
/* 544 */         endIndex = meridianIndex + 2;
/*     */       }
/*     */       else
/*     */       {
/* 548 */         int end1 = pattern.indexOf(32, hourIndex);
/* 549 */         if (end1 > 0)
/*     */         {
/* 551 */           endIndex = end1;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 556 */       pattern = pattern.substring(0, startIndex) + "{" + pattern.substring(startIndex, endIndex) + "}" + pattern.substring(endIndex);
/*     */     }
/*     */ 
/* 562 */     pattern = substituteMap(pattern, timeMap);
/* 563 */     pattern = substituteMap(pattern, meridianMap);
/* 564 */     pattern = substituteMap(pattern, tzMap);
/*     */ 
/* 566 */     int length = pattern.length();
/* 567 */     if (length > 2)
/*     */     {
/* 569 */       pattern = pattern.substring(1, length - 1);
/*     */     }
/*     */ 
/* 572 */     if (suffix != null)
/*     */     {
/* 574 */       pattern = pattern + suffix;
/*     */     }
/*     */ 
/* 577 */     return pattern;
/*     */   }
/*     */ 
/*     */   public static String substituteMap(String str, String[][] map)
/*     */   {
/* 582 */     for (int i = 0; i < map.length; ++i)
/*     */     {
/* 584 */       int index = str.indexOf(map[i][0]);
/* 585 */       if (index < 0)
/*     */         continue;
/* 587 */       int index2 = index + map[i][0].length();
/* 588 */       str = str.substring(0, index) + map[i][1] + str.substring(index2);
/* 589 */       return str;
/*     */     }
/*     */ 
/* 593 */     return str;
/*     */   }
/*     */ 
/*     */   public static List getLanguageDirectoryList(Map keyMap, String systemDir)
/*     */     throws ServiceException
/*     */   {
/* 612 */     Properties environment = (Properties)AppObjectRepository.getObject("environment");
/*     */ 
/* 615 */     if (keyMap == null)
/*     */     {
/* 617 */       keyMap = new HashMap();
/* 618 */       String idcHomeDir = environment.getProperty("IdcHomeDir");
/* 619 */       String idcDir = environment.getProperty("IntradocDir");
/* 620 */       if (idcHomeDir != null)
/*     */       {
/* 622 */         keyMap.put("IdcHomeDir", idcHomeDir);
/*     */       }
/* 624 */       keyMap.put("IntradocDir", idcDir);
/*     */ 
/* 626 */       String componentDir = environment.getProperty("ComponentDir");
/* 627 */       if (componentDir != null)
/*     */       {
/* 629 */         keyMap.put("ComponentDir", componentDir);
/*     */       }
/* 631 */       String systemComponentDir = environment.getProperty("SystemComponentDir");
/* 632 */       if (systemComponentDir != null)
/*     */       {
/* 634 */         keyMap.put("SystemComponentDir", systemComponentDir);
/*     */       }
/*     */     }
/*     */ 
/* 638 */     List dirs = new ArrayList();
/* 639 */     if (systemDir != null)
/*     */     {
/* 641 */       dirs.add(systemDir);
/*     */     }
/*     */ 
/* 644 */     Enumeration en = environment.propertyNames();
/* 645 */     List keys = new ArrayList();
/* 646 */     while (en.hasMoreElements())
/*     */     {
/* 648 */       Object o = en.nextElement();
/* 649 */       if (o instanceof String)
/*     */       {
/* 651 */         String key = (String)o;
/* 652 */         if (key.startsWith("LanguageSource_"))
/*     */         {
/* 654 */           keys.add(key);
/*     */         }
/*     */       }
/*     */     }
/* 658 */     SortUtils.sortStringList(keys);
/* 659 */     for (int i = 0; i < keys.size(); ++i)
/*     */     {
/* 661 */       String key = (String)keys.get(i);
/* 662 */       String value = environment.getProperty(key);
/* 663 */       value = FileUtils.computePathFromSubstitutionMap(keyMap, value);
/* 664 */       value = FileUtils.directorySlashes(value);
/* 665 */       dirs.add(value);
/*     */     }
/* 667 */     return dirs;
/*     */   }
/*     */ 
/*     */   public static boolean testStringEncoding(String str, String encoding)
/*     */   {
/* 672 */     if (encoding == null)
/*     */     {
/* 674 */       encoding = FileUtils.m_javaSystemEncoding;
/*     */     }
/*     */     String newString;
/*     */     try
/*     */     {
/* 679 */       byte[] bytes = str.getBytes(encoding);
/* 680 */       newString = new String(bytes, encoding);
/*     */     }
/*     */     catch (UnsupportedEncodingException e)
/*     */     {
/* 684 */       return false;
/*     */     }
/* 686 */     return newString.equals(str);
/*     */   }
/*     */ 
/*     */   public static IdcLocale getLocaleFromContext(ExecutionContext context)
/*     */   {
/* 691 */     IdcLocale locale = null;
/* 692 */     if (context != null)
/*     */     {
/* 694 */       locale = (IdcLocale)context.getLocaleResource(0);
/*     */     }
/*     */ 
/* 697 */     if (locale == null)
/*     */     {
/* 699 */       locale = LocaleResources.getSystemLocale();
/*     */     }
/*     */ 
/* 702 */     return locale;
/*     */   }
/*     */ 
/*     */   public static IdcLocale getLocaleFromAcceptLanguageList(String languageList)
/*     */   {
/* 707 */     CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 708 */     IdcLocale[] locales = clh.getLocalesFromLanguageList(languageList);
/* 709 */     if ((locales != null) && (locales.length > 0))
/*     */     {
/* 711 */       return locales[0];
/*     */     }
/* 713 */     return null;
/*     */   }
/*     */ 
/*     */   public static Locale constructJavaLocaleFromContext(ExecutionContext context)
/*     */   {
/* 731 */     Locale locale = Locale.US;
/*     */ 
/* 733 */     IdcLocale idcLocale = getLocaleFromContext(context);
/*     */ 
/* 735 */     if (idcLocale == null)
/*     */     {
/* 737 */       idcLocale = (IdcLocale)context.getCachedObject("UserLocale");
/*     */     }
/*     */ 
/* 740 */     if (idcLocale == null)
/*     */     {
/* 742 */       idcLocale = LocaleResources.getSystemLocale();
/*     */     }
/*     */ 
/* 745 */     if (idcLocale != null)
/*     */     {
/* 747 */       if (idcLocale.m_locale == null)
/*     */       {
/* 749 */         locale = new Locale(idcLocale.m_languageId);
/*     */       }
/*     */       else
/*     */       {
/* 753 */         locale = idcLocale.m_locale;
/*     */       }
/*     */     }
/*     */ 
/* 757 */     return locale;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 763 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99332 $";
/*     */   }
/*     */ 
/*     */   static
/*     */   {
/*  60 */     updateDateFormats();
/*  61 */     m_factory = new IdcMessageFactory();
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.LocaleUtils
 * JD-Core Version:    0.5.4
 */