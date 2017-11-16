/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.data.DataException;
/*     */ import java.text.DateFormat;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.Locale;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class DateUtils
/*     */ {
/*     */   public static final short DATABASE_FORMAT = 0;
/*     */   public static final short DISPLAY_FORMAT = 1;
/*     */   public static final short RFC_HTTP_FORMAT = 2;
/*     */   public static final short BULKLOAD_FORMAT = 3;
/*     */   public static final short LOG_FORMAT = 4;
/*     */   public static final long SEC_TO_MSEC = 1000L;
/*     */   public static final long MIN_TO_SEC = 60L;
/*     */   public static final long HOUR_TO_MIN = 60L;
/*     */   public static final long DAY_TO_HOUR = 24L;
/*     */   public static final long MONTH_TO_DAY = 30L;
/*     */   public static final long YEAR_TO_MONTH = 12L;
/*     */   public static final String YEAR_SPECIFIER = "year";
/*     */   public static final String MONTH_SPECIFIER = "month";
/*     */   public static final String WEEK_SPECIFIER = "week";
/*     */   public static final String DAY_SPECIFIER = "day";
/*     */   public static final String HOUR_SPECIFIER = "hour";
/*     */   public static final String MINUTE_SPECIFIER = "minute";
/*     */   public static final String SECOND_SPECIFIER = "second";
/*     */ 
/*     */   @Deprecated
/*     */   public static final String ODBC_BEGIN = "{ts '";
/*     */ 
/*     */   @Deprecated
/*     */   public static final String ODBC_END = "'}";
/*  62 */   protected static boolean m_force4DigitYear = true;
/*  63 */   protected static String m_overrideDateFormat = null;
/*  64 */   protected static String m_overrideTimeFormat = null;
/*  65 */   protected static String m_overrideDateSeparator = null;
/*  66 */   protected static String m_overrideTimeSeparator = null;
/*  67 */   protected static String m_overrideBulkloadFormat = null;
/*     */ 
/*     */   public static void setOverrideDateFormat(String dateFormat)
/*     */   {
/*  71 */     m_overrideDateFormat = dateFormat;
/*     */   }
/*     */ 
/*     */   public static void setOverrideTimeFormat(String timeFormat)
/*     */   {
/*  76 */     m_overrideTimeFormat = timeFormat;
/*     */   }
/*     */ 
/*     */   public static void setOverrideDateSeparator(String dateSep)
/*     */   {
/*  81 */     m_overrideDateSeparator = dateSep;
/*     */   }
/*     */ 
/*     */   public static void setOverrideTimeSeparator(String timeSep)
/*     */   {
/*  86 */     m_overrideTimeSeparator = timeSep;
/*     */   }
/*     */ 
/*     */   public static void setOverrideBulkloadFormat(String bulkloadFormat)
/*     */   {
/*  91 */     m_overrideBulkloadFormat = bulkloadFormat;
/*     */   }
/*     */ 
/*     */   public static void setForce4DigitYear(boolean val) {
/*  95 */     m_force4DigitYear = val;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatForDisplay(Calendar cal)
/*     */   {
/* 103 */     Date dte = cal.getTime();
/* 104 */     return formatForDisplay(dte);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatForDisplay(Calendar cal, int formatType)
/*     */   {
/* 112 */     Date dte = cal.getTime();
/* 113 */     return format(dte, formatType);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatForDisplay(Date dte)
/*     */   {
/* 121 */     return format(dte, 1);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Date parseDisplayDate(String str)
/*     */     throws ServiceException
/*     */   {
/* 129 */     return parse(str, 1);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatForBulkload(String dteStr)
/*     */     throws ServiceException
/*     */   {
/* 137 */     Date dte = parseDisplayDate(dteStr);
/* 138 */     String dteFormat = format(dte, 3);
/* 139 */     return dteFormat;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatODBC(Date dte)
/*     */   {
/* 148 */     String dbFormat = format(dte, 0);
/* 149 */     return "{ts '" + dbFormat + "'}";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String formatODBCString(String dteStr)
/*     */     throws ServiceException
/*     */   {
/* 157 */     Date dte = parse(dteStr, 0);
/* 158 */     String dteFormat = format(dte, 0);
/* 159 */     return "{ts '" + dteFormat + "'}";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String format(Date dte, int formatType)
/*     */   {
/* 167 */     DateFormat frmt = determineFormat(null, formatType);
/* 168 */     if (formatType == 1)
/*     */     {
/* 170 */       frmt = fixupDateFormat(frmt);
/*     */     }
/* 172 */     if (formatType == 2)
/*     */     {
/* 174 */       frmt.setTimeZone(TimeZone.getTimeZone("GMT"));
/*     */     }
/*     */ 
/* 177 */     String formattedDate = frmt.format(dte);
/* 178 */     if (formatType == 2)
/*     */     {
/* 180 */       formattedDate = formattedDate + " GMT";
/*     */     }
/* 182 */     return formattedDate;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static DateFormat fixupDateFormat(DateFormat frmt)
/*     */   {
/* 190 */     frmt.setTimeZone(TimeZone.getDefault());
/* 191 */     if (frmt instanceof SimpleDateFormat)
/*     */     {
/* 193 */       SimpleDateFormat sFrmt = (SimpleDateFormat)frmt;
/* 194 */       String pattern = sFrmt.toPattern();
/* 195 */       int i = pattern.indexOf("yy");
/* 196 */       if ((m_force4DigitYear) && (i >= 0))
/*     */       {
/* 198 */         boolean is4digit = pattern.startsWith("yyy", i);
/* 199 */         if (!is4digit)
/*     */         {
/* 201 */           pattern = pattern.substring(0, i) + "yy" + pattern.substring(i);
/* 202 */           sFrmt.applyPattern(pattern);
/*     */         }
/*     */       }
/*     */     }
/* 206 */     return frmt;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Date parse(String str, int formatType)
/*     */     throws ServiceException
/*     */   {
/* 214 */     str = str.trim();
/* 215 */     String failMsg = null;
/* 216 */     if (str.length() < 6)
/*     */     {
/* 218 */       failMsg = "!syDateErrorValueTooShort";
/*     */     }
/*     */ 
/* 221 */     if ((formatType != 0) && (str.indexOf("{ts '") >= 0))
/*     */     {
/* 224 */       formatType = 0;
/*     */     }
/*     */ 
/* 227 */     if (formatType == 0)
/*     */     {
/* 229 */       int dateStartsAt = str.indexOf("'");
/* 230 */       if (dateStartsAt >= 0)
/*     */       {
/* 232 */         int endLoc = str.indexOf("'", dateStartsAt + 1);
/* 233 */         if (endLoc < 0)
/*     */         {
/* 235 */           failMsg = "!syDateErrorNotTerminated";
/*     */         }
/*     */         else
/*     */         {
/* 239 */           str = str.substring(dateStartsAt + 1, endLoc).trim();
/* 240 */           if (str.length() < 10)
/*     */           {
/* 242 */             failMsg = "!syDateErrorDBFormatTooShort";
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 247 */       dateStartsAt = str.indexOf("{ts '");
/* 248 */       if (dateStartsAt >= 0)
/*     */       {
/* 250 */         dateStartsAt += "{ts '".length();
/* 251 */         int endLoc = str.indexOf("'}", dateStartsAt);
/* 252 */         if (endLoc < 0)
/*     */         {
/* 254 */           failMsg = "!syDateErrorODBCNotTerminated";
/*     */         }
/*     */         else
/*     */         {
/* 258 */           str = str.substring(dateStartsAt, endLoc).trim();
/* 259 */           if (str.length() < 10)
/*     */           {
/* 261 */             failMsg = "!syDateErrorODBCFormatTooShort";
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 266 */     if (failMsg != null)
/*     */     {
/* 268 */       throw new ServiceException(failMsg);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 273 */       String[] strParam = { str };
/* 274 */       DateFormat frmt = determineFormat(strParam, formatType);
/* 275 */       if (formatType == 2)
/*     */       {
/* 277 */         frmt.setTimeZone(TimeZone.getTimeZone("GMT"));
/*     */       }
/*     */       else
/*     */       {
/* 281 */         frmt.setTimeZone(TimeZone.getDefault());
/*     */       }
/*     */ 
/* 284 */       Date dte = internalParse(frmt, strParam[0]);
/* 285 */       return dte;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 289 */       throw new ServiceException("!syDateErrorParseError");
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static DateFormat determineFormat(String[] str, int formatType)
/*     */   {
/* 298 */     return determineFormatEx(str, formatType);
/*     */   }
/*     */ 
/*     */   public static DateFormat determineFormatEx(String[] str, int formatType)
/*     */   {
/* 303 */     int nFormats = 2;
/* 304 */     int dateFormat = 3;
/* 305 */     int timeFormat = 3;
/* 306 */     boolean isOverridableType = (formatType != 4) && (formatType != 0);
/*     */ 
/* 308 */     boolean useSeconds = false;
/* 309 */     boolean adjustTimeFormat = false;
/* 310 */     boolean dateSeparatorIsDash = false;
/*     */ 
/* 312 */     if (formatType == 2)
/*     */     {
/* 315 */       boolean useRfc1123 = true;
/* 316 */       boolean longWkday = false;
/* 317 */       if ((str != null) && (str.length > 0) && (str[0] != null))
/*     */       {
/* 321 */         if (str[0].indexOf(45) > 0)
/*     */         {
/* 323 */           useRfc1123 = false;
/*     */         }
/*     */ 
/* 327 */         int commaIndex = str[0].indexOf(",");
/* 328 */         if (commaIndex > 3)
/*     */         {
/* 330 */           longWkday = true;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 335 */       String wkDayFormat = "EEE";
/* 336 */       if (longWkday)
/*     */       {
/* 338 */         wkDayFormat = "EEEE";
/*     */       }
/*     */ 
/* 341 */       String dateFormatStr = "d MMM yyyy HH:mm:ss";
/* 342 */       if (!useRfc1123)
/*     */       {
/* 344 */         dateFormatStr = "d-MMM-yyyy HH:mm:ss";
/*     */       }
/* 346 */       dateFormatStr = wkDayFormat + ", " + dateFormatStr;
/* 347 */       SimpleDateFormat fmt = new SimpleDateFormat(dateFormatStr, Locale.US);
/*     */ 
/* 349 */       return fmt;
/*     */     }
/*     */ 
/* 352 */     if ((str != null) && (str.length > 0) && (str[0] != null))
/*     */     {
/* 357 */       int len = str[0].length();
/* 358 */       int dashIndex = str[0].indexOf(45);
/* 359 */       boolean isDatabaseFormat = false;
/*     */ 
/* 361 */       if ((len >= 8) && (dashIndex >= 0))
/*     */       {
/* 363 */         if (dashIndex < 5)
/*     */         {
/* 365 */           dateSeparatorIsDash = true;
/*     */         }
/*     */ 
/* 370 */         if ((((dashIndex == 1) || (dashIndex == 2))) && (!Validation.isNum(str[0].charAt(dashIndex + 1))))
/*     */         {
/* 373 */           dateFormat = 2;
/*     */         }
/* 375 */         else if ((!isOverridableType) && (dashIndex == 4) && (((str[0].charAt(6) == '-') || (str[0].charAt(7) == '-'))))
/*     */         {
/* 378 */           isDatabaseFormat = true;
/*     */         }
/*     */       }
/*     */ 
/* 382 */       String timeSeparator = ":";
/* 383 */       if ((isOverridableType) && (m_overrideTimeSeparator != null) && (m_overrideTimeSeparator.length() > 0))
/*     */       {
/* 386 */         timeSeparator = m_overrideTimeSeparator;
/*     */       }
/* 388 */       int spaceIndex = str[0].indexOf(32);
/* 389 */       int colonIndex = -1;
/* 390 */       if (spaceIndex > 0)
/*     */       {
/* 392 */         colonIndex = str[0].indexOf(timeSeparator, spaceIndex);
/*     */       }
/* 394 */       if (colonIndex < 0)
/*     */       {
/* 396 */         nFormats = 1;
/*     */       }
/*     */       else
/*     */       {
/* 402 */         adjustTimeFormat = true;
/*     */ 
/* 404 */         colonIndex = str[0].indexOf(timeSeparator, colonIndex + 1);
/* 405 */         if (colonIndex > 0)
/*     */         {
/* 408 */           if ((len > colonIndex + 2) && (str[0].charAt(colonIndex + 1) == '0') && (str[0].charAt(colonIndex + 2) == '0'))
/*     */           {
/* 412 */             str[0] = (str[0].substring(0, colonIndex) + str[0].substring(colonIndex + 3));
/*     */ 
/* 414 */             len -= 3;
/*     */           }
/*     */           else
/*     */           {
/* 419 */             timeFormat = 2;
/* 420 */             useSeconds = true;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 425 */       if (isDatabaseFormat)
/*     */       {
/* 427 */         String tf = "yyyy-M-d";
/* 428 */         if (nFormats > 1)
/*     */         {
/* 430 */           if (timeFormat == 3)
/*     */           {
/* 432 */             tf = tf + " H:mm";
/*     */           }
/*     */           else
/*     */           {
/* 436 */             tf = tf + " H:mm:ss";
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 442 */         return new SimpleDateFormat(tf);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 447 */       if (formatType == 0)
/*     */       {
/* 449 */         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
/*     */       }
/* 451 */       if (formatType == 4)
/*     */       {
/* 453 */         dateFormat = 3;
/* 454 */         timeFormat = 2;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 459 */     String dateFormatStr = null;
/* 460 */     String timeFormatStr = null;
/* 461 */     DateFormat defDate = null;
/* 462 */     if ((formatType == 3) && (nFormats == 2))
/*     */     {
/* 464 */       if (m_overrideBulkloadFormat != null)
/*     */       {
/* 466 */         int index = m_overrideBulkloadFormat.indexOf(32);
/* 467 */         if (index > 0)
/*     */         {
/* 469 */           dateFormatStr = m_overrideBulkloadFormat.substring(0, index);
/* 470 */           timeFormatStr = m_overrideBulkloadFormat.substring(index + 1);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 475 */         dateFormatStr = "d-MMM-yyyy";
/* 476 */         timeFormatStr = "HH:mm:ss";
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 481 */     boolean isDateChanged = false;
/* 482 */     if (dateFormatStr == null)
/*     */     {
/* 484 */       defDate = DateFormat.getDateInstance(dateFormat);
/* 485 */       if ((isOverridableType) && (dateFormat == 3))
/*     */       {
/* 487 */         if (defDate instanceof SimpleDateFormat)
/*     */         {
/* 489 */           SimpleDateFormat sdf = (SimpleDateFormat)defDate;
/* 490 */           dateFormatStr = sdf.toPattern();
/*     */         }
/* 492 */         if (m_overrideDateFormat != null)
/*     */         {
/* 494 */           isDateChanged = true;
/* 495 */           dateFormatStr = m_overrideDateFormat;
/*     */         }
/*     */ 
/* 498 */         if (dateFormatStr != null)
/*     */         {
/* 500 */           String overrideDateSep = m_overrideDateSeparator;
/* 501 */           if (dateSeparatorIsDash)
/*     */           {
/* 503 */             overrideDateSep = "-";
/*     */           }
/*     */ 
/* 506 */           if ((overrideDateSep != null) && (overrideDateSep.length() > 0))
/*     */           {
/* 508 */             char sepCh = overrideDateSep.charAt(0);
/* 509 */             dateFormatStr = replaceDateTimeSeparator(sepCh, dateFormatStr, true);
/* 510 */             isDateChanged = true;
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 516 */     if (nFormats == 1)
/*     */     {
/* 518 */       if ((isDateChanged) || (defDate == null))
/*     */       {
/* 520 */         return new SimpleDateFormat(dateFormatStr);
/*     */       }
/* 522 */       return defDate;
/*     */     }
/*     */ 
/* 525 */     if ((dateFormatStr != null) && (timeFormatStr == null))
/*     */     {
/* 527 */       DateFormat defTime = DateFormat.getTimeInstance(timeFormat);
/* 528 */       if ((isOverridableType) && (m_overrideTimeFormat != null))
/*     */       {
/* 530 */         timeFormatStr = m_overrideTimeFormat;
/*     */       }
/* 534 */       else if (defTime instanceof SimpleDateFormat)
/*     */       {
/* 536 */         timeFormatStr = ((SimpleDateFormat)defTime).toPattern();
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 543 */       isOverridableType = false;
/*     */     }
/*     */ 
/* 546 */     if ((dateFormatStr != null) && (timeFormatStr != null))
/*     */     {
/* 548 */       char sep = ':';
/* 549 */       if ((isOverridableType) && (m_overrideTimeSeparator != null) && (m_overrideTimeSeparator.length() > 0))
/*     */       {
/* 552 */         sep = m_overrideTimeSeparator.charAt(0);
/* 553 */         timeFormatStr = replaceDateTimeSeparator(sep, timeFormatStr, false);
/*     */       }
/*     */ 
/* 557 */       if (adjustTimeFormat)
/*     */       {
/* 559 */         int firstIndex = timeFormatStr.indexOf(sep);
/* 560 */         if (firstIndex > 0)
/*     */         {
/* 562 */           int secondIndex = timeFormatStr.indexOf(sep, firstIndex + 1);
/* 563 */           if (secondIndex > 0)
/*     */           {
/* 565 */             if (!useSeconds)
/*     */             {
/* 567 */               int suffixIndex = timeFormatStr.indexOf(32, secondIndex);
/* 568 */               String suffix = null;
/* 569 */               if (suffixIndex > 0)
/*     */               {
/* 571 */                 suffix = timeFormatStr.substring(suffixIndex);
/*     */               }
/* 573 */               timeFormatStr = timeFormatStr.substring(0, secondIndex);
/* 574 */               if (suffix != null)
/*     */               {
/* 576 */                 timeFormatStr = timeFormatStr + suffix;
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/*     */           }
/* 582 */           else if (useSeconds)
/*     */           {
/* 584 */             int suffixIndex = timeFormatStr.indexOf(32, firstIndex);
/* 585 */             String suffix = null;
/* 586 */             if (suffixIndex > 0)
/*     */             {
/* 588 */               suffix = timeFormatStr.substring(suffixIndex);
/* 589 */               timeFormatStr = timeFormatStr.substring(0, suffixIndex);
/*     */             }
/* 591 */             timeFormatStr = timeFormatStr + ":ss";
/* 592 */             if (suffix != null)
/*     */             {
/* 594 */               timeFormatStr = timeFormatStr + suffix;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 600 */       return new SimpleDateFormat(dateFormatStr + " " + timeFormatStr);
/*     */     }
/*     */ 
/* 603 */     return DateFormat.getDateTimeInstance(dateFormat, timeFormat);
/*     */   }
/*     */ 
/*     */   public static Date getDateXDaysAgo(int numDays)
/*     */   {
/* 608 */     long currentTime = System.currentTimeMillis();
/* 609 */     long oneDay = 86400000L;
/* 610 */     return new Date(currentTime - numDays * oneDay);
/*     */   }
/*     */ 
/*     */   protected static Date internalParse(DateFormat frmt, String str)
/*     */     throws ParseException
/*     */   {
/* 617 */     int[] dateFields = null;
/* 618 */     String strToParse = str;
/* 619 */     DateFormat frmtToUse = frmt;
/* 620 */     if (frmt instanceof SimpleDateFormat)
/*     */     {
/* 622 */       SimpleDateFormat sdf = (SimpleDateFormat)frmt;
/* 623 */       Calendar cal = sdf.getCalendar();
/* 624 */       String pattern = sdf.toPattern();
/*     */ 
/* 626 */       StringTokenizer patTok = new StringTokenizer(pattern);
/* 627 */       int ntokens = patTok.countTokens();
/*     */ 
/* 629 */       if (ntokens > 0)
/*     */       {
/* 631 */         String datePattern = patTok.nextToken();
/* 632 */         if (datePattern.length() > 4)
/*     */         {
/* 634 */           StringTokenizer strTok = new StringTokenizer(str);
/* 635 */           if (strTok.hasMoreTokens())
/*     */           {
/* 637 */             String dateStr = strTok.nextToken();
/* 638 */             dateFields = parseByDatePattern(cal, dateStr, datePattern);
/*     */ 
/* 640 */             if (dateFields != null)
/*     */             {
/* 642 */               strToParse = getRemainder(str, dateStr);
/* 643 */               String timePattern = getRemainder(pattern, datePattern);
/* 644 */               sdf.applyPattern(timePattern);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 651 */     Date dte = null;
/* 652 */     Calendar timeCal = frmtToUse.getCalendar();
/* 653 */     if ((strToParse.length() > 0) || (dateFields == null))
/*     */     {
/* 655 */       dte = frmtToUse.parse(strToParse);
/*     */     }
/*     */     else
/*     */     {
/* 659 */       timeCal.clear();
/*     */ 
/* 662 */       timeCal.set(0, 1);
/*     */     }
/* 664 */     if (dateFields != null)
/*     */     {
/* 666 */       timeCal = frmtToUse.getCalendar();
/* 667 */       timeCal.setLenient(false);
/*     */ 
/* 669 */       timeCal.set(dateFields[0], dateFields[1], dateFields[2]);
/* 670 */       dte = timeCal.getTime();
/*     */     }
/* 672 */     return dte;
/*     */   }
/*     */ 
/*     */   protected static int[] parseByDatePattern(Calendar cal, String dateStr, String pattern)
/*     */     throws ParseException
/*     */   {
/* 678 */     int patLen = pattern.length();
/* 679 */     int count = 0;
/* 680 */     char prevCh = '\000';
/* 681 */     char ch = '\000';
/* 682 */     int[] retVal = { 1970, 0, 1 };
/* 683 */     int dsIndex = 0;
/* 684 */     int endIndex = 0;
/* 685 */     int dsLen = dateStr.length();
/*     */ 
/* 687 */     for (int i = 0; i < patLen; ++i)
/*     */     {
/* 689 */       if (dsIndex >= dsLen)
/*     */       {
/* 691 */         return null;
/*     */       }
/*     */ 
/* 694 */       ch = pattern.charAt(i);
/* 695 */       if (Character.isLetter(ch))
/*     */       {
/* 697 */         if ((count > 0) && (ch != prevCh))
/*     */         {
/* 699 */           return null;
/*     */         }
/* 701 */         ++count;
/* 702 */         prevCh = ch;
/*     */       }
/*     */       else
/*     */       {
/* 706 */         endIndex = dateStr.indexOf(ch, dsIndex);
/* 707 */         if (endIndex < 0)
/*     */         {
/* 709 */           return null;
/*     */         }
/*     */ 
/* 712 */         if (!subParseDate(dateStr, dsIndex, endIndex, prevCh, count, retVal))
/*     */         {
/* 714 */           return null;
/*     */         }
/*     */ 
/* 718 */         dsIndex = endIndex + 1;
/* 719 */         prevCh = '\000';
/* 720 */         count = 0;
/*     */       }
/*     */     }
/*     */ 
/* 724 */     endIndex = dsLen;
/* 725 */     if (!subParseDate(dateStr, dsIndex, endIndex, ch, count, retVal))
/*     */     {
/* 727 */       return null;
/*     */     }
/*     */ 
/* 730 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected static boolean subParseDate(String dateStr, int dsIndex, int endIndex, char ch, int count, int[] retVal)
/*     */     throws ParseException
/*     */   {
/* 736 */     Calendar cal = null;
/* 737 */     if (count > 0)
/*     */     {
/* 739 */       int val = -1;
/* 740 */       String field = dateStr.substring(dsIndex, endIndex);
/* 741 */       if ((count <= 2) || (ch == 'y'))
/*     */       {
/* 744 */         val = Integer.parseInt(field);
/*     */       }
/*     */       else
/*     */       {
/* 749 */         char[] temp = new char[count];
/* 750 */         for (int i = 0; i < count; ++i)
/*     */         {
/* 752 */           temp[i] = ch;
/*     */         }
/* 754 */         String fieldFormat = new String(temp);
/* 755 */         SimpleDateFormat sdf = new SimpleDateFormat(fieldFormat);
/* 756 */         sdf.parse(field);
/* 757 */         cal = sdf.getCalendar();
/*     */       }
/*     */ 
/* 760 */       switch (ch)
/*     */       {
/*     */       case 'M':
/* 763 */         if (cal != null)
/*     */         {
/* 765 */           val = cal.get(2);
/*     */         }
/*     */         else
/*     */         {
/* 769 */           --val;
/*     */         }
/* 771 */         retVal[1] = val;
/* 772 */         break;
/*     */       case 'd':
/* 774 */         if (cal != null)
/*     */         {
/* 776 */           val = cal.get(5);
/*     */         }
/* 778 */         retVal[2] = val;
/* 779 */         break;
/*     */       case 'y':
/* 781 */         if (cal != null)
/*     */         {
/* 783 */           val = cal.get(1);
/*     */         }
/* 785 */         if ((val >= 0) && (val < 70))
/*     */         {
/* 787 */           val = 2000 + val;
/*     */         }
/* 789 */         else if ((val >= 70) && (val < 100))
/*     */         {
/* 791 */           val = 1900 + val;
/*     */         }
/* 793 */         retVal[0] = val;
/*     */       }
/*     */     }
/*     */ 
/* 797 */     return true;
/*     */   }
/*     */ 
/*     */   protected static String getRemainder(String str, String sub)
/*     */   {
/* 803 */     int index = str.indexOf(sub);
/* 804 */     if (index < 0)
/*     */     {
/* 806 */       return "";
/*     */     }
/* 808 */     int offset = index + sub.length() + 1;
/* 809 */     if (offset >= str.length())
/*     */     {
/* 811 */       return "";
/*     */     }
/* 813 */     return str.substring(index + sub.length() + 1);
/*     */   }
/*     */ 
/*     */   protected static String replaceDateTimeSeparator(char separator, String formatStr, boolean isDate)
/*     */   {
/* 818 */     int length = formatStr.length();
/* 819 */     char[] c = new char[length];
/* 820 */     formatStr.getChars(0, length, c, 0);
/* 821 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 823 */       char stdsep = ':';
/* 824 */       if (isDate)
/*     */       {
/* 826 */         stdsep = '/';
/*     */       }
/* 828 */       if (c[i] != stdsep)
/*     */         continue;
/* 830 */       c[i] = separator;
/*     */     }
/*     */ 
/* 834 */     return new String(c);
/*     */   }
/*     */ 
/*     */   public static long parseTime(String inputString)
/*     */     throws DataException
/*     */   {
/* 846 */     if ((inputString == null) || (inputString.length() == 0))
/*     */     {
/* 848 */       return 0L;
/*     */     }
/* 850 */     long multiplier = 1L;
/* 851 */     if (inputString.endsWith("year"))
/*     */     {
/* 853 */       multiplier = multiplier * 12L * 30L * 24L * 60L * 60L * 1000L;
/* 854 */       inputString = inputString.substring(0, inputString.length() - "year".length());
/*     */     }
/* 856 */     if (inputString.endsWith("month"))
/*     */     {
/* 858 */       multiplier = multiplier * 30L * 24L * 60L * 60L * 1000L;
/* 859 */       inputString = inputString.substring(0, inputString.length() - "month".length());
/*     */     }
/* 861 */     if (inputString.endsWith("week"))
/*     */     {
/* 863 */       multiplier = multiplier * 7L * 24L * 60L * 60L * 1000L;
/* 864 */       inputString = inputString.substring(0, inputString.length() - "week".length());
/*     */     }
/* 866 */     if (inputString.endsWith("day"))
/*     */     {
/* 868 */       multiplier = multiplier * 24L * 60L * 60L * 1000L;
/* 869 */       inputString = inputString.substring(0, inputString.length() - "day".length());
/*     */     }
/* 871 */     if (inputString.endsWith("hour"))
/*     */     {
/* 873 */       multiplier = multiplier * 60L * 60L * 1000L;
/* 874 */       inputString = inputString.substring(0, inputString.length() - "hour".length());
/*     */     }
/* 876 */     if (inputString.endsWith("minute"))
/*     */     {
/* 878 */       multiplier = multiplier * 60L * 1000L;
/* 879 */       inputString = inputString.substring(0, inputString.length() - "minute".length());
/*     */     }
/* 881 */     if (inputString.endsWith("second"))
/*     */     {
/* 883 */       multiplier *= 1000L;
/* 884 */       inputString = inputString.substring(0, inputString.length() - "second".length());
/*     */     }
/* 886 */     long time = NumberUtils.parseLong(inputString, -2L);
/* 887 */     if (time < -1L)
/*     */     {
/* 889 */       String errMsg = LocaleUtils.encodeMessage("csNotaValidLong", null);
/* 890 */       throw new DataException(errMsg);
/*     */     }
/* 892 */     time *= multiplier;
/* 893 */     return time;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 897 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103915 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DateUtils
 * JD-Core Version:    0.5.4
 */