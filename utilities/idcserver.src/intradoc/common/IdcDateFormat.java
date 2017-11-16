/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.BasicFormatter;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.text.DateFormat;
/*      */ import java.text.FieldPosition;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.EmptyStackException;
/*      */ import java.util.GregorianCalendar;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Stack;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcDateFormat
/*      */   implements BasicFormatter
/*      */ {
/*      */   public static final int F_TIME_ONLY = 2;
/*      */   public static final int F_DATE_ONLY = 1;
/*      */   public static final int F_DIRECT = 256;
/*   38 */   public static boolean m_defaultUseSTZ = false;
/*      */ 
/*   43 */   public static IdcLocaleInternalStrings m_rfcStrings = new IdcLocaleInternalStrings(new String[] { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" }, new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }, new String[] { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" }, new String[] { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" });
/*      */   public boolean m_useSTZ;
/*      */   protected IdcNumberFormat m_numberFormat;
/*      */   protected TimeZoneFormat m_timeZoneFormat;
/*      */   protected TimeZone m_timeZone;
/*      */   protected Calendar m_calendar;
/*      */   protected String[] m_meridianSymbols;
/*      */   protected String m_pattern;
/*      */   protected IdcDateToken[] m_parseTokens;
/*      */   protected IdcDateToken[] m_parseDateTokens;
/*      */   protected IdcDateToken[] m_parseTimeTokens;
/*      */   protected IdcDateToken[] m_iso8601ShortTokens;
/*      */   protected IdcDateToken[] m_formatTokens;
/*      */   protected IdcDateToken[] m_formatDateTokens;
/*      */   protected IdcDateToken[] m_formatTimeTokens;
/*      */   protected boolean m_isIso8601;
/*      */   protected boolean m_attemptEpochParse;
/*      */   public IdcLocaleInternalStrings m_internalStrings;
/*      */   public IdcDateFormat[] m_alternateParsingFormats;
/*      */   protected char[] m_formatSpecifiers;
/*      */   protected IdcDateToken[] m_formatSpecifierTokens;
/*      */ 
/*      */   public IdcDateFormat()
/*      */   {
/*   56 */     this.m_useSTZ = m_defaultUseSTZ;
/*      */ 
/*   60 */     this.m_timeZone = null;
/*      */ 
/*   71 */     this.m_isIso8601 = false;
/*   72 */     this.m_attemptEpochParse = true;
/*      */ 
/*   78 */     this.m_internalStrings = null;
/*      */ 
/*   89 */     this.m_alternateParsingFormats = null;
/*      */ 
/*   91 */     this.m_formatSpecifiers = new char[] { 'G', 'y', 'M', 'E', 'd', 'D', 'h', 'H', 'm', 's', 'S', 'a', 'k', 'K', 'z', 'Z' };
/*      */   }
/*      */ 
/*      */   public void initDefault()
/*      */   {
/*      */     try
/*      */     {
/*  106 */       IdcNumberFormat fmt = new IdcNumberFormat();
/*  107 */       fmt.setGroupingUsed(false);
/*  108 */       fmt.setParseIntegerOnly(true);
/*      */ 
/*  110 */       IdcLocale locale = LocaleResources.getSystemLocale();
/*  111 */       if (locale != null)
/*      */       {
/*  113 */         IdcDateFormat dflt = LocaleResources.getSystemDateFormat();
/*  114 */         shallowCopy(dflt);
/*      */       }
/*      */       else
/*      */       {
/*  118 */         DateFormat df = DateFormat.getDateTimeInstance(3, 1);
/*  119 */         if (df instanceof SimpleDateFormat)
/*      */         {
/*  121 */           SimpleDateFormat sdf = (SimpleDateFormat)df;
/*  122 */           String pattern = LocaleUtils.simpleDateFormatToPattern(sdf);
/*  123 */           init(pattern, TimeZone.getDefault(), new TimeZoneFormat(), fmt);
/*      */         }
/*      */         else
/*      */         {
/*  128 */           init("M/d/yy h:mm[:ss] {aa}!mAM,PM", TimeZone.getDefault(), new TimeZoneFormat(), fmt);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (ParseStringException ignore)
/*      */     {
/*  136 */       ignore.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void init(String pattern)
/*      */     throws ParseStringException
/*      */   {
/*  151 */     initWithDefaultTimezone(pattern, null);
/*      */   }
/*      */ 
/*      */   public void initWithDefaultTimezone(String pattern, TimeZone defaultTimeZone)
/*      */     throws ParseStringException
/*      */   {
/*  167 */     IdcLocale locale = LocaleResources.getSystemLocale();
/*  168 */     IdcNumberFormat nbrFmt = null;
/*  169 */     if (locale != null)
/*      */     {
/*  171 */       nbrFmt = locale.getNumberFormat();
/*      */     }
/*  173 */     if (nbrFmt == null)
/*      */     {
/*  175 */       nbrFmt = new IdcNumberFormat();
/*  176 */       nbrFmt.setGroupingUsed(false);
/*  177 */       nbrFmt.setParseIntegerOnly(true);
/*      */     }
/*  179 */     if (locale != null)
/*      */     {
/*  181 */       this.m_internalStrings = locale.m_internalStrings;
/*      */     }
/*  183 */     if (defaultTimeZone == null)
/*      */     {
/*  185 */       defaultTimeZone = LocaleResources.getSystemTimeZone();
/*      */     }
/*  187 */     initEx(pattern, null, defaultTimeZone, LocaleResources.m_systemTimeZoneFormat, nbrFmt, false, false);
/*      */   }
/*      */ 
/*      */   public void init(String pattern, TimeZone tz, TimeZoneFormat tzf, IdcNumberFormat nbrFormat)
/*      */     throws ParseStringException
/*      */   {
/*  206 */     initEx(pattern, null, tz, tzf, nbrFormat, false, true);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void init(String pattern, String[] symbols, TimeZone tz, TimeZoneFormat tzf, IdcNumberFormat nbrFormat)
/*      */     throws ParseStringException
/*      */   {
/*  216 */     initEx(pattern, symbols, tz, tzf, nbrFormat, true, true);
/*      */   }
/*      */ 
/*      */   protected void initEx(String pattern, String[] symbols, TimeZone tz, TimeZoneFormat tzf, IdcNumberFormat nbrFormat, boolean forceSymbols, boolean forceTimeZone)
/*      */     throws ParseStringException
/*      */   {
/*  223 */     if ((pattern.equalsIgnoreCase("iso8601")) || (pattern.equalsIgnoreCase("iso8601short")))
/*      */     {
/*  225 */       this.m_isIso8601 = true;
/*      */ 
/*  228 */       if (pattern.equalsIgnoreCase("iso8601short"))
/*      */       {
/*  230 */         this.m_pattern = "yyyy[-]MM[-]dd{'T'}HH[:]mm[:]ssZ";
/*      */       }
/*      */       else
/*      */       {
/*  234 */         this.m_pattern = "yyyy-MM-dd{ }['T']HH:mm:ssZ";
/*      */       }
/*  236 */       if (tzf == null)
/*      */       {
/*  239 */         tzf = new TimeZoneFormat();
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  244 */       if (pattern.indexOf("&") >= 0)
/*      */       {
/*  246 */         pattern = StringUtils.decodeXmlEscapeSequence(pattern.toCharArray(), 0, pattern.length());
/*      */       }
/*  248 */       this.m_pattern = pattern;
/*      */     }
/*      */ 
/*  251 */     this.m_calendar = new GregorianCalendar();
/*  252 */     this.m_calendar.clear();
/*  253 */     this.m_calendar.setLenient(false);
/*  254 */     if (tz != null)
/*      */     {
/*  256 */       this.m_timeZone = tz;
/*      */     }
/*      */     else
/*      */     {
/*  260 */       this.m_timeZone = TimeZone.getDefault();
/*      */     }
/*  262 */     this.m_numberFormat = nbrFormat;
/*  263 */     this.m_timeZoneFormat = tzf;
/*      */ 
/*  265 */     this.m_formatSpecifierTokens = new IdcDateToken[] { new IdcDateToken(this.m_calendar, 0, 'G', "Era", 1), new IdcDateToken(this.m_calendar, 1, 'y', "Year", -1), new IdcDateToken(this.m_calendar, 2, 'M', "Month", -1), new IdcDateToken(this.m_calendar, 7, 'E', "DayOfWeek", -1), new IdcDateToken(this.m_calendar, 5, 'd', "Day", -1), new IdcDateToken(this.m_calendar, 6, 'D', "DayOfYear", -1), new IdcDateToken(this.m_calendar, 11, 'h', "Hour", -2), new IdcDateToken(this.m_calendar, 11, 'H', "Hour", -2), new IdcDateToken(this.m_calendar, 12, 'm', "Minute", -2), new IdcDateToken(this.m_calendar, 13, 's', "Second", 0), new IdcDateToken(this.m_calendar, 14, 'S', "Millisecond", 0), new IdcDateToken('M', 'a', "Meridian"), new IdcDateToken(this.m_calendar, 11, 'k', "HourOfDay", -2), new IdcDateToken(this.m_calendar, 11, 'K', "HourOfDay", -2), new IdcDateToken('z', 'z', "Timezone"), new IdcDateToken('Z', 'Z', "Zulu") };
/*      */ 
/*  285 */     this.m_meridianSymbols = symbols;
/*  286 */     if (this.m_meridianSymbols == null)
/*      */     {
/*  288 */       this.m_meridianSymbols = new String[0];
/*      */     }
/*      */ 
/*  291 */     optimizeFormat((forceSymbols) && (this.m_meridianSymbols.length == 2), (forceTimeZone) && (tz != null));
/*      */ 
/*  293 */     if (!this.m_isIso8601)
/*      */       return;
/*  295 */     prepareShortIso8601();
/*      */   }
/*      */ 
/*      */   public void prepareShortIso8601()
/*      */   {
/*  301 */     List list = new ArrayList();
/*  302 */     for (int i = 0; i < this.m_parseTokens.length; ++i)
/*      */     {
/*  304 */       IdcDateToken token = this.m_parseTokens[i];
/*  305 */       if ((((token.m_type == 'T') || (token.m_type == 'S'))) && (((token.m_type != 'T') || (!token.m_text.equals("T"))))) {
/*      */         continue;
/*      */       }
/*  308 */       list.add(token);
/*      */     }
/*      */ 
/*  311 */     this.m_iso8601ShortTokens = new IdcDateToken[list.size()];
/*  312 */     list.toArray(this.m_iso8601ShortTokens);
/*      */   }
/*      */ 
/*      */   public String[] getMeridianSymbols()
/*      */   {
/*  319 */     return this.m_meridianSymbols;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getAllParseTokens()
/*      */   {
/*  324 */     return this.m_parseTokens;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getDateParseTokens()
/*      */   {
/*  329 */     return this.m_parseDateTokens;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getTimeParseTokens()
/*      */   {
/*  334 */     return this.m_parseTimeTokens;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getAllFormatTokens()
/*      */   {
/*  339 */     return this.m_formatTokens;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getDateFormatTokens()
/*      */   {
/*  344 */     return this.m_formatDateTokens;
/*      */   }
/*      */ 
/*      */   public IdcDateToken[] getTimeFormatTokens()
/*      */   {
/*  349 */     return this.m_formatTimeTokens;
/*      */   }
/*      */ 
/*      */   public String getPattern()
/*      */   {
/*  354 */     return this.m_pattern;
/*      */   }
/*      */ 
/*      */   public void setPattern(String pattern) throws ParseStringException
/*      */   {
/*  359 */     if (pattern.equalsIgnoreCase("iso8601"))
/*      */     {
/*  361 */       this.m_isIso8601 = true;
/*      */ 
/*  365 */       this.m_pattern = "yyyy-MM-dd HH:mm:ssZ";
/*      */     }
/*      */     else
/*      */     {
/*  369 */       this.m_pattern = pattern;
/*      */     }
/*  371 */     optimizeFormat(false, false);
/*      */ 
/*  373 */     if ((!this.m_isIso8601) || (this.m_iso8601ShortTokens != null))
/*      */       return;
/*  375 */     prepareShortIso8601();
/*      */   }
/*      */ 
/*      */   public boolean getEpochParse()
/*      */   {
/*  381 */     return this.m_attemptEpochParse;
/*      */   }
/*      */ 
/*      */   public void setEpochParse(boolean newValue)
/*      */   {
/*  386 */     this.m_attemptEpochParse = newValue;
/*      */   }
/*      */ 
/*      */   protected void optimizeFormat(boolean hasMeridian, boolean hasTimeZone)
/*      */     throws ParseStringException
/*      */   {
/*  399 */     Vector parseTokens = new IdcVector();
/*  400 */     Vector formatTokens = new IdcVector();
/*  401 */     Stack optionalTokenStack = new Stack();
/*  402 */     Stack nestingStack = new Stack();
/*      */ 
/*  404 */     int length = this.m_pattern.length();
/*  405 */     char lastChar = ' ';
/*  406 */     char state = 'N';
/*  407 */     IdcStringBuilder buf = null;
/*  408 */     IdcDateToken token = null;
/*  409 */     boolean nextIsFirstOptional = false;
/*  410 */     boolean isDisplayed = true;
/*  411 */     int isOptionalCount = 0;
/*  412 */     String extendedInfo = null;
/*      */ 
/*  414 */     int index = 0;
/*  415 */     while (index < length)
/*      */     {
/*  417 */       char thisChar = this.m_pattern.charAt(index);
/*  418 */       boolean incrementIndex = true;
/*  419 */       switch (state)
/*      */       {
/*      */       case 'E':
/*  422 */         switch (thisChar)
/*      */         {
/*  425 */         case '\'':
/*  425 */           if (lastChar == thisChar) buf.append('\'');
/*  426 */           token = new IdcDateToken(buf.toString());
/*  427 */           if (nextIsFirstOptional)
/*      */           {
/*  429 */             nextIsFirstOptional = false;
/*  430 */             optionalTokenStack.push(token);
/*      */           }
/*  432 */           parseTokens.addElement(token);
/*  433 */           if (isDisplayed)
/*      */           {
/*  435 */             formatTokens.addElement(token);
/*      */           }
/*  437 */           state = 'N';
/*  438 */           break;
/*      */         default:
/*  440 */           buf.append(thisChar);
/*  441 */         }break;
/*      */       case 'N':
/*  446 */         if (thisChar == '\'')
/*      */         {
/*  448 */           state = 'E';
/*  449 */           if (buf == null)
/*      */           {
/*  451 */             buf = new IdcStringBuilder();
/*  452 */             buf.m_disableToStringReleaseBuffers = true;
/*      */           }
/*      */           else
/*      */           {
/*  456 */             buf.setLength(0);
/*      */           }
/*      */         }
/*  459 */         else if (Character.isSpaceChar(thisChar))
/*      */         {
/*  461 */           if (thisChar != lastChar)
/*      */           {
/*  463 */             token = new IdcDateToken('S', ' ', "Space");
/*  464 */             token.m_text = " ";
/*  465 */             parseTokens.addElement(token);
/*  466 */             if (isDisplayed)
/*      */             {
/*  468 */               formatTokens.addElement(token);
/*      */             }
/*  470 */             if (nextIsFirstOptional)
/*      */             {
/*  472 */               nextIsFirstOptional = false;
/*  473 */               optionalTokenStack.push(token);
/*      */             }
/*      */           }
/*      */         }
/*  477 */         else if (((thisChar >= 'a') && (thisChar <= 'z')) || ((thisChar >= 'A') && (thisChar <= 'Z')))
/*      */         {
/*  480 */           if (thisChar != lastChar)
/*      */           {
/*  484 */             token = null;
/*  485 */             for (int j = 0; j < this.m_formatSpecifiers.length; ++j)
/*      */             {
/*  487 */               if (this.m_formatSpecifiers[j] != thisChar)
/*      */                 continue;
/*  489 */               token = (IdcDateToken)this.m_formatSpecifierTokens[j].clone();
/*  490 */               if (nextIsFirstOptional)
/*      */               {
/*  492 */                 nextIsFirstOptional = false;
/*  493 */                 optionalTokenStack.push(token);
/*      */               }
/*      */ 
/*  496 */               parseTokens.addElement(token);
/*  497 */               if (!isDisplayed)
/*      */                 break;
/*  499 */               formatTokens.addElement(token); break;
/*      */             }
/*      */ 
/*  504 */             if (token == null)
/*      */             {
/*  506 */               String msg = LocaleUtils.encodeMessage("syIdcDateTokenNotSupported", null, "" + thisChar);
/*      */ 
/*  508 */               throw new ParseStringException(msg, index);
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  514 */             token.m_length += 1;
/*      */           }
/*      */         }
/*  517 */         else if ((thisChar == '{') || (thisChar == '['))
/*      */         {
/*  519 */           nestingStack.push("" + thisChar);
/*  520 */           ++isOptionalCount;
/*  521 */           nextIsFirstOptional = true;
/*  522 */           if (thisChar == '[')
/*      */           {
/*  524 */             if (!isDisplayed)
/*      */             {
/*  526 */               throw new ParseStringException("!syIdcDateTokenExtraLeftBracket", index);
/*      */             }
/*  528 */             isDisplayed = false;
/*      */           }
/*      */         }
/*  531 */         else if ((thisChar == '}') || (thisChar == ']'))
/*      */         {
/*  533 */           boolean okay = false;
/*      */           try
/*      */           {
/*  536 */             String str = "{";
/*  537 */             if (thisChar == ']')
/*      */             {
/*  539 */               isDisplayed = true;
/*  540 */               str = "[";
/*      */             }
/*  542 */             if (((String)nestingStack.pop()).equals(str))
/*      */             {
/*  544 */               okay = true;
/*      */             }
/*      */           }
/*      */           catch (EmptyStackException ignore)
/*      */           {
/*  549 */             if (SystemUtils.m_verbose)
/*      */             {
/*  551 */               Report.debug("systemparse", null, ignore);
/*      */             }
/*      */           }
/*  554 */           if (!okay)
/*      */           {
/*  556 */             String msg = LocaleUtils.encodeMessage("syMismatchedCharacter", null, "" + thisChar);
/*      */ 
/*  558 */             throw new ParseStringException(msg, index);
/*      */           }
/*      */ 
/*  561 */           --isOptionalCount;
/*  562 */           if (isOptionalCount < 0)
/*      */           {
/*  564 */             String msg = LocaleUtils.encodeMessage("syMismatchedCharacter", null, "" + thisChar);
/*      */ 
/*  566 */             throw new ParseStringException(msg, index);
/*      */           }
/*      */ 
/*  569 */           if (isOptionalCount < optionalTokenStack.size())
/*      */           {
/*  571 */             token = (IdcDateToken)optionalTokenStack.elementAt(isOptionalCount);
/*  572 */             optionalTokenStack.setSize(isOptionalCount);
/*  573 */             token.m_parseSkipToIndex = parseTokens.size();
/*      */           }
/*      */         }
/*  576 */         else if (thisChar == '!')
/*      */         {
/*  578 */           extendedInfo = this.m_pattern.substring(index);
/*  579 */           index = length;
/*      */         }
/*      */         else
/*      */         {
/*  583 */           state = 'L';
/*  584 */           if (buf == null)
/*      */           {
/*  586 */             buf = new IdcStringBuilder();
/*  587 */             buf.m_disableToStringReleaseBuffers = true;
/*      */           }
/*      */           else
/*      */           {
/*  591 */             buf.setLength(0);
/*      */           }
/*  593 */           buf.append(thisChar);
/*      */         }
/*  595 */         break;
/*      */       case 'L':
/*  598 */         if (thisChar == '\'')
/*      */         {
/*  600 */           state = 'E';
/*  601 */           token = new IdcDateToken(buf.toString());
/*  602 */           parseTokens.addElement(token);
/*  603 */           if (isDisplayed)
/*      */           {
/*  605 */             formatTokens.addElement(token);
/*      */           }
/*  607 */           if (nextIsFirstOptional)
/*      */           {
/*  609 */             nextIsFirstOptional = false;
/*  610 */             optionalTokenStack.push(token);
/*      */           }
/*  612 */           buf.setLength(0);
/*      */         }
/*  614 */         else if (((thisChar >= 'a') && (thisChar <= 'z')) || ((thisChar >= 'A') && (thisChar <= 'Z')) || (thisChar == '{') || (thisChar == '[') || (thisChar == '}') || (thisChar == ']') || (thisChar == '!'))
/*      */         {
/*  620 */           token = new IdcDateToken(buf.toString());
/*  621 */           parseTokens.addElement(token);
/*  622 */           if (isDisplayed)
/*      */           {
/*  624 */             formatTokens.addElement(token);
/*      */           }
/*  626 */           if (nextIsFirstOptional)
/*      */           {
/*  628 */             nextIsFirstOptional = false;
/*  629 */             optionalTokenStack.push(token);
/*      */           }
/*      */ 
/*  633 */           state = 'N';
/*      */ 
/*  636 */           incrementIndex = false;
/*      */         }
/*      */         else
/*      */         {
/*  640 */           buf.append(thisChar);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  645 */       if (incrementIndex)
/*      */       {
/*  647 */         ++index;
/*  648 */         lastChar = thisChar;
/*      */       }
/*      */     }
/*      */ 
/*  652 */     if ((buf != null) && (buf.length() > 0) && (state == 'L'))
/*      */     {
/*  654 */       token = new IdcDateToken(buf.toString());
/*  655 */       parseTokens.addElement(token);
/*  656 */       if (isDisplayed)
/*      */       {
/*  658 */         formatTokens.addElement(token);
/*      */       }
/*      */     }
/*  661 */     if (buf != null)
/*      */     {
/*  663 */       buf.releaseBuffers();
/*      */     }
/*      */ 
/*  666 */     if ((extendedInfo != null) && (((!hasMeridian) || (!hasTimeZone))))
/*      */     {
/*  668 */       int endIndex = extendedInfo.length();
/*  669 */       index = extendedInfo.lastIndexOf("!");
/*  670 */       while (index >= 0)
/*      */       {
/*  672 */         int tagLen = endIndex - index - 2;
/*  673 */         if (tagLen <= 0)
/*      */         {
/*  675 */           Report.trace("systemparse", "Extended date format info at end of pattern" + this.m_pattern + " has tag that is less than two characters ", null);
/*      */ 
/*  677 */           endIndex = index;
/*  678 */           index = extendedInfo.lastIndexOf("!", endIndex - 1);
/*      */         }
/*      */ 
/*  681 */         char c = extendedInfo.charAt(index + 1);
/*  682 */         switch (c)
/*      */         {
/*      */         case 't':
/*  686 */           if (!hasTimeZone)
/*      */           {
/*  688 */             String text = extendedInfo.substring(index + 2, endIndex);
/*  689 */             TimeZone tz = this.m_timeZoneFormat.parseTimeZone(null, text, 0);
/*  690 */             if (tz == null)
/*      */             {
/*  692 */               throw new ParseStringException("!syUnableToParseTimeZone");
/*      */             }
/*  694 */             this.m_timeZone = tz;
/*  695 */           }break;
/*      */         case 'm':
/*  701 */           if (!hasMeridian)
/*      */           {
/*  703 */             List list = new ArrayList();
/*  704 */             StringUtils.appendListFromSequence(list, extendedInfo, index + 2, tagLen, ',', '^', 32);
/*      */ 
/*  706 */             int size = list.size();
/*  707 */             if (size == 2)
/*      */             {
/*  709 */               this.m_meridianSymbols = new String[2];
/*  710 */               list.toArray(this.m_meridianSymbols);
/*      */             }
/*      */           }
/*  712 */           break;
/*      */         case 'r':
/*  716 */           if (extendedInfo.regionMatches(false, index + 2, "fc", 0, 2))
/*      */           {
/*  718 */             this.m_internalStrings = m_rfcStrings; } break;
/*      */         default:
/*  723 */           Report.trace("systemparse", "Extended date format info tag '" + c + "' unknown in pattern '" + this.m_pattern + "'.", null);
/*      */         }
/*      */ 
/*  728 */         endIndex = index;
/*  729 */         if (endIndex <= 0)
/*      */         {
/*  731 */           index = -1;
/*      */         }
/*      */         else
/*      */         {
/*  735 */           index = extendedInfo.lastIndexOf("!", endIndex - 1);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  740 */     this.m_parseTokens = new IdcDateToken[parseTokens.size()];
/*  741 */     parseTokens.copyInto(this.m_parseTokens);
/*      */ 
/*  744 */     IdcDateToken[][] separatedTokens = determineDateAndTimeTokens(this.m_parseTokens);
/*  745 */     this.m_parseDateTokens = separatedTokens[0];
/*  746 */     this.m_parseTimeTokens = separatedTokens[1];
/*      */ 
/*  749 */     int formatSize = formatTokens.size();
/*  750 */     if (formatSize > 0)
/*      */     {
/*  752 */       int n = formatSize - 1;
/*  753 */       IdcDateToken tok = (IdcDateToken)formatTokens.elementAt(n);
/*  754 */       if (tok.m_type == 'S')
/*      */       {
/*  756 */         formatTokens.removeElementAt(n);
/*  757 */         formatSize = n;
/*      */       }
/*      */     }
/*  760 */     this.m_formatTokens = new IdcDateToken[formatSize];
/*  761 */     formatTokens.copyInto(this.m_formatTokens);
/*      */ 
/*  763 */     separatedTokens = determineDateAndTimeTokens(this.m_formatTokens);
/*  764 */     this.m_formatDateTokens = separatedTokens[0];
/*  765 */     this.m_formatTimeTokens = separatedTokens[1];
/*      */   }
/*      */ 
/*      */   protected IdcDateToken[][] determineDateAndTimeTokens(IdcDateToken[] tokens)
/*      */   {
/*  771 */     IdcDateToken[][] separatedTokens = new IdcDateToken[2][];
/*      */ 
/*  774 */     boolean inDate = false;
/*  775 */     boolean inTime = false;
/*  776 */     int firstDate = -1; int lastDate = -1;
/*  777 */     int firstTime = -1; int lastTime = -1;
/*  778 */     for (int i = 0; i < tokens.length; ++i)
/*      */     {
/*  780 */       IdcDateToken token = tokens[i];
/*  781 */       if ("gyMdEDFwW".indexOf(token.m_sym) >= 0)
/*      */       {
/*  784 */         inTime = false;
/*  785 */         if (firstDate < 0)
/*      */         {
/*  787 */           firstDate = lastDate = i;
/*  788 */           inDate = true;
/*      */         }
/*  790 */         if (!inDate)
/*      */           continue;
/*  792 */         lastDate = i;
/*      */       }
/*      */       else {
/*  795 */         if ("hHmsSakKzZ".indexOf(token.m_sym) < 0) {
/*      */           continue;
/*      */         }
/*  798 */         inDate = false;
/*  799 */         if (firstTime < 0)
/*      */         {
/*  801 */           firstTime = lastTime = i;
/*  802 */           inTime = true;
/*      */         }
/*  804 */         if (!inTime)
/*      */           continue;
/*  806 */         lastTime = i;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  811 */     if (firstDate >= 0)
/*      */     {
/*  813 */       separatedTokens[0] = cloneTokenRange(tokens, firstDate, lastDate);
/*      */     }
/*      */     else
/*      */     {
/*  817 */       separatedTokens[0] = null;
/*      */     }
/*      */ 
/*  820 */     if (firstTime >= 0)
/*      */     {
/*  822 */       separatedTokens[1] = cloneTokenRange(tokens, firstTime, lastTime);
/*      */     }
/*      */     else
/*      */     {
/*  826 */       separatedTokens[1] = null;
/*      */     }
/*      */ 
/*  829 */     return separatedTokens;
/*      */   }
/*      */ 
/*      */   protected IdcDateToken[] cloneTokenRange(IdcDateToken[] original, int firstIndex, int lastIndex)
/*      */   {
/*  834 */     int length = lastIndex - firstIndex + 1;
/*  835 */     IdcDateToken[] tokens = new IdcDateToken[length];
/*  836 */     for (int i = 0; i < length; ++i)
/*      */     {
/*  838 */       tokens[i] = ((IdcDateToken)original[(firstIndex + i)].clone());
/*  839 */       tokens[i].m_parseSkipToIndex -= firstIndex;
/*  840 */       if (tokens[i].m_parseSkipToIndex <= length)
/*      */         continue;
/*  842 */       tokens[i].m_parseSkipToIndex = length;
/*      */     }
/*      */ 
/*  845 */     return tokens;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Date parseDate(String text, IdcTimeZone tz, TimeZoneFormat tzf)
/*      */     throws ParseStringException
/*      */   {
/*  853 */     return parseDateWithTimeZone(text, tz, tzf, 0);
/*      */   }
/*      */ 
/*      */   public Date parseDate(String text) throws ParseStringException
/*      */   {
/*  858 */     return parseDateWithTimeZone(text, null, null, 0);
/*      */   }
/*      */ 
/*      */   public Date parseDateWithTimeZone(String text, TimeZone tz, TimeZoneFormat tzf, int flags)
/*      */     throws ParseStringException
/*      */   {
/*  864 */     ParseStringLocation parseLocation = new ParseStringLocation();
/*  865 */     if (((flags & 0x100) == 0) && 
/*  867 */       (text.startsWith("{ts '")))
/*      */     {
/*  869 */       return LocaleResources.m_odbcFormat.parseDateWithTimeZone(text, tz, tzf, 256);
/*      */     }
/*      */ 
/*  873 */     Date d = parseDateFull(null, parseLocation, text, tz, tzf, 0);
/*      */ 
/*  875 */     if ((parseLocation.m_state != 0) && 
/*  877 */       (this.m_attemptEpochParse) && ((flags & 0x100) == 0))
/*      */     {
/*      */       try
/*      */       {
/*  883 */         boolean isNumeric = false;
/*  884 */         if (text.length() > 4)
/*      */         {
/*  886 */           isNumeric = true;
/*  887 */           for (int i = 0; i < text.length(); ++i)
/*      */           {
/*  889 */             char ch = text.charAt(i);
/*  890 */             if (Validation.isNum(ch))
/*      */               continue;
/*  892 */             isNumeric = false;
/*  893 */             break;
/*      */           }
/*      */         }
/*      */ 
/*  897 */         if (isNumeric)
/*      */         {
/*  899 */           long l = Long.parseLong(text);
/*      */ 
/*  907 */           return new Date(l);
/*      */         }
/*      */       }
/*      */       catch (NumberFormatException ignore)
/*      */       {
/*  912 */         if (SystemUtils.m_verbose)
/*      */         {
/*  914 */           Report.debug("systemparse", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  919 */     if (parseLocation.m_state != 0)
/*      */     {
/*  921 */       String errMsg = parseLocation.m_errMsg;
/*  922 */       if (errMsg == null)
/*      */       {
/*  924 */         errMsg = LocaleUtils.encodeMessage("syUnableToParseDate", null, text);
/*      */       }
/*  926 */       throw new ParseStringException(parseLocation, errMsg);
/*      */     }
/*      */ 
/*  929 */     return d;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Date parseDateDirect(String text, IdcTimeZone tz, TimeZoneFormat tzf)
/*      */     throws ParseStringException
/*      */   {
/*  936 */     return parseDateWithTimeZone(text, tz, tzf, 256);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Date parseDateWithPattern(String text, ParseStringLocation parsePosition, IdcTimeZone tz, TimeZoneFormat tzf)
/*      */   {
/*  944 */     Date d = parseDateFull(null, parsePosition, text, tz, tzf, 0);
/*  945 */     return d;
/*      */   }
/*      */ 
/*      */   public Date parseDateFull(Calendar cal, ParseStringLocation parsePosition, String text, TimeZone tz, TimeZoneFormat tzf, int flags)
/*      */   {
/*  951 */     Map parseData = new HashMap();
/*  952 */     cal = parseDateIntoCalendar(cal, parseData, parsePosition, text.length(), text, tz, tzf, null);
/*  953 */     if (null == cal)
/*      */     {
/*  956 */       return null;
/*      */     }
/*  958 */     Date d = null;
/*      */     try
/*      */     {
/*  961 */       if (parseData.get("isLeapSecond") != null)
/*      */       {
/*  963 */         int sec = cal.get(13);
/*  964 */         cal.set(13, sec - 1);
/*  965 */         d = new Date(cal.getTime().getTime() + 1000L);
/*  966 */         cal.set(13, sec);
/*      */       }
/*      */       else
/*      */       {
/*  970 */         d = cal.getTime();
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/*  981 */       IdcMessage msg = IdcMessageFactory.lc("syDateErrorParseError", new Object[0]);
/*  982 */       msg.m_prior = IdcMessageFactory.lc(e);
/*  983 */       String msgText = LocaleUtils.encodeMessage(msg);
/*  984 */       parsePosition.setErrorMessage(parsePosition.m_index, -1, msgText);
/*      */     }
/*      */ 
/*  987 */     return d;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Calendar parseDateWithPatternEx(String text, ParseStringLocation parsePosition, int endIndex, IdcTimeZone tz, TimeZoneFormat tzf, IdcDateToken[] tokens, Map<String, Object> parseData)
/*      */   {
/* 1014 */     Calendar cal = (Calendar)parseData.get("Calendar");
/* 1015 */     return parseDateIntoCalendar(cal, parseData, parsePosition, endIndex, text, tz, tzf, tokens);
/*      */   }
/*      */ 
/*      */   public Calendar parseDateIntoCalendar(Calendar cal, Map<String, Object> parseData, ParseStringLocation parsePosition, int endIndex, String text, TimeZone tz, TimeZoneFormat tzf, IdcDateToken[] tokens)
/*      */   {
/* 1037 */     if (parsePosition == null)
/*      */     {
/* 1039 */       parsePosition = new ParseStringLocation();
/*      */     }
/* 1041 */     parsePosition.m_activeParsingObject = this;
/* 1042 */     if (cal == null)
/*      */     {
/* 1044 */       cal = (Calendar)this.m_calendar.clone();
/*      */     }
/* 1046 */     setCalTZ(cal, tz);
/* 1047 */     if (tzf == null)
/*      */     {
/* 1049 */       tzf = this.m_timeZoneFormat;
/*      */     }
/* 1051 */     Calendar defaults = null;
/*      */ 
/* 1053 */     int index = parsePosition.m_index;
/* 1054 */     while ((index < endIndex) && (Character.isSpaceChar(text.charAt(index))))
/*      */     {
/* 1056 */       ++index;
/*      */     }
/*      */ 
/* 1059 */     boolean isPM = false;
/* 1060 */     boolean isPMSet = false;
/* 1061 */     int hourOfDay = -1;
/*      */ 
/* 1063 */     if (tokens == null)
/*      */     {
/* 1065 */       tokens = this.m_parseTokens;
/*      */ 
/* 1068 */       if ((this.m_isIso8601) && (endIndex - index > 4) && (text.indexOf(45) < 0))
/*      */       {
/* 1070 */         tokens = this.m_iso8601ShortTokens;
/* 1071 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1073 */           Report.debug("systemparse", "parseDateWithPatternEx: text = " + text, null);
/*      */         }
/*      */       }
/* 1076 */       if ((tokens.length > 0) && (tokens[0].m_type == 'T') && (text != null) && (text.length() < 12) && (text.length() > 8) && 
/* 1082 */         (!text.startsWith(tokens[0].m_text)) && (tokens[0].m_parseSkipToIndex < 0) && 
/* 1084 */         (this.m_formatDateTokens != null))
/*      */       {
/* 1086 */         tokens = this.m_formatDateTokens;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1094 */     ParseStringLocation provisionalError = null;
/*      */ 
/* 1105 */     IdcDateToken prevToken = null;
/* 1106 */     IdcDateToken curOptionalToken = null;
/* 1107 */     IdcDateToken prevTokenAtOptionalCapture = null;
/* 1108 */     int curOptionalIndex = -1;
/* 1109 */     int curOptionalI = -1;
/* 1110 */     boolean isLeapSecond = false;
/*      */ 
/* 1112 */     for (int i = 0; (index < endIndex) && (i < tokens.length); ++i)
/*      */     {
/* 1114 */       IdcDateToken token = tokens[i];
/* 1115 */       IdcDateToken nextToken = null;
/* 1116 */       if (i + 1 < tokens.length)
/*      */       {
/* 1118 */         nextToken = tokens[(i + 1)];
/*      */       }
/* 1120 */       if (token.m_parseSkipToIndex >= 0)
/*      */       {
/* 1122 */         curOptionalToken = token;
/* 1123 */         prevTokenAtOptionalCapture = prevToken;
/* 1124 */         curOptionalIndex = index;
/* 1125 */         curOptionalI = i;
/*      */       }
/* 1127 */       else if ((curOptionalToken != null) && (curOptionalToken.m_parseSkipToIndex <= i))
/*      */       {
/* 1129 */         curOptionalToken = null;
/*      */       }
/* 1131 */       boolean skipForward = false;
/*      */ 
/* 1133 */       parsePosition.m_elementLocation = token.m_fieldName;
/* 1134 */       switch (token.m_type)
/*      */       {
/*      */       case 'F':
/* 1141 */         if (parsePosition.m_objectPartLocation == null)
/*      */         {
/* 1143 */           parsePosition.m_objectPartLocation = "Date";
/*      */         }
/* 1145 */         if (token.m_isTime)
/*      */         {
/* 1147 */           parsePosition.m_objectPartLocation = "Time";
/*      */         }
/* 1149 */         int end = endIndex;
/* 1150 */         if ((nextToken != null) && (nextToken.m_type == 'F'))
/*      */         {
/* 1153 */           end = index + token.m_length;
/* 1154 */           if (end > endIndex)
/*      */           {
/* 1156 */             end = endIndex;
/*      */           }
/*      */         }
/*      */ 
/* 1160 */         long value = 0L;
/* 1161 */         int beforeIndex = parsePosition.m_index;
/* 1162 */         boolean isNumber = true;
/* 1163 */         if ((((token.m_calendarType == 2) || (token.m_calendarType == 7))) && (token.m_length >= 3))
/*      */         {
/* 1166 */           value = parseCalendarFieldString(text, end, token, parsePosition);
/* 1167 */           if (value == -1L)
/*      */           {
/* 1169 */             String msg = LocaleUtils.encodeMessage("syBadMonth", null, text.substring(beforeIndex, parsePosition.m_index));
/*      */ 
/* 1172 */             parsePosition.m_errMsg = msg;
/*      */           }
/* 1174 */           isNumber = false;
/*      */         }
/*      */         else
/*      */         {
/* 1178 */           value = parseLong(text, end, parsePosition);
/*      */         }
/*      */ 
/* 1182 */         int diff = parsePosition.m_index - beforeIndex;
/* 1183 */         boolean useDefault = false;
/* 1184 */         if (diff == 0)
/*      */         {
/* 1186 */           useDefault = true;
/*      */ 
/* 1188 */           if (curOptionalToken != null)
/*      */           {
/* 1190 */             skipForward = true;
/*      */           }
/*      */           else
/*      */           {
/* 1194 */             if (parsePosition.m_state == 0)
/*      */             {
/*      */               int errType;
/*      */               int errType;
/* 1197 */               if (isNumber)
/*      */               {
/* 1199 */                 errType = -3;
/*      */               }
/*      */               else
/*      */               {
/* 1203 */                 errType = -1;
/*      */               }
/* 1205 */               parsePosition.setErrorState(beforeIndex, errType);
/*      */             }
/*      */             else
/*      */             {
/* 1209 */               int errType = -1;
/* 1210 */               parsePosition.setErrorState(beforeIndex, errType);
/*      */             }
/* 1212 */             Report.trace("systemparse", "returning " + parsePosition, null);
/* 1213 */             return null;
/*      */           }
/*      */         }
/* 1216 */         index = parsePosition.m_index;
/*      */ 
/* 1218 */         if ((!useDefault) && (!skipForward))
/*      */         {
/* 1220 */           if (isNumber)
/*      */           {
/* 1222 */             switch (token.m_calendarType)
/*      */             {
/*      */             case 11:
/* 1225 */               hourOfDay = (int)value;
/* 1226 */               break;
/*      */             case 1:
/* 1229 */               if (diff == 2)
/*      */               {
/* 1231 */                 if (value >= 69L)
/*      */                 {
/* 1233 */                   value += 1900L;
/*      */                 }
/*      */                 else
/*      */                 {
/* 1237 */                   value += 2000L; } 
/* 1237 */               }break;
/*      */             case 2:
/* 1243 */               value -= 1L;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1248 */           if ((value == 60L) && (token.m_calendarType == 13))
/*      */           {
/* 1250 */             isLeapSecond = true;
/*      */           }
/* 1252 */           else if ((value < 0L) || (value > token.m_calendarMax))
/*      */           {
/* 1256 */             parsePosition.setErrorState(beforeIndex, -4);
/* 1257 */             provisionalError = parsePosition.shallowClone();
/*      */           }
/*      */         }
/* 1260 */         if (!skipForward)
/*      */         {
/* 1262 */           if (provisionalError == null)
/*      */           {
/* 1264 */             cal.set(token.m_calendarType, (int)value);
/*      */           }
/* 1266 */           if ((token.m_isTime) && 
/* 1268 */             (parseData != null))
/*      */           {
/* 1270 */             parseData.put("someTimeSet", "1");
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1275 */         break;
/*      */       case 'T':
/* 1279 */         if (token.m_text.regionMatches(true, 0, text, index, token.m_textLength))
/*      */         {
/* 1281 */           index += token.m_textLength;
/*      */         }
/* 1283 */         else if ((curOptionalToken != null) && (((curOptionalIndex == index) || (prevToken == null) || (prevToken.m_type == 'S'))))
/*      */         {
/* 1291 */           skipForward = true;
/*      */         }
/*      */         else
/*      */         {
/* 1295 */           String msg = LocaleUtils.encodeMessage("syUnableToFindText", null, token.m_text);
/*      */ 
/* 1297 */           parsePosition.setErrorMessage(index, -2, msg);
/* 1298 */           return null;
/*      */         }
/*      */       case 'S':
/* 1313 */         if (Character.isSpaceChar(text.charAt(index)))
/*      */         {
/* 1315 */           ++index;
/* 1316 */           --i;
/*      */         }
/* 1318 */         else if ((prevToken != null) && (prevToken.m_type != 'S') && (prevToken.m_type != 'T'))
/*      */         {
/* 1320 */           if (curOptionalToken != null)
/*      */           {
/* 1322 */             skipForward = true;
/*      */           }
/*      */           else
/*      */           {
/* 1326 */             String msg = LocaleUtils.encodeMessage("syUnableToFindSpace", null);
/* 1327 */             parsePosition.setErrorMessage(index, -2, msg);
/* 1328 */             return null;
/*      */           }
/*      */         }
/*      */       case 'W':
/* 1337 */         if (!Character.isSpaceChar(text.charAt(index)))
/*      */         {
/* 1339 */           ++index;
/* 1340 */           --i; } break;
/*      */       case 'z':
/* 1347 */         parsePosition.m_objectPartLocation = "Timezone";
/*      */ 
/* 1349 */         tz = tzf.parseTimeZone(parsePosition, text, 0);
/* 1350 */         if (tz != null)
/*      */         {
/* 1352 */           index = parsePosition.m_index;
/* 1353 */           curOptionalToken = null;
/*      */         }
/* 1355 */         else if (curOptionalToken != null)
/*      */         {
/* 1357 */           skipForward = true;
/*      */         }
/*      */         else
/*      */         {
/* 1361 */           if (parsePosition.m_state == 0)
/*      */           {
/* 1363 */             parsePosition.setErrorMessage(index, -1, "!syUnableToParseTimeZone");
/*      */           }
/* 1365 */           return null;
/*      */         }
/*      */       case 'Z':
/* 1372 */         parsePosition.m_objectPartLocation = "Timezone";
/* 1373 */         if (text.charAt(index) == 'Z')
/*      */         {
/* 1375 */           tz = LocaleResources.UTC;
/* 1376 */           curOptionalToken = null;
/* 1377 */           ++index; } break;
/*      */       case 'M':
/* 1386 */         for (int j = 0; j < this.m_meridianSymbols.length; ++j)
/*      */         {
/* 1388 */           int end = index + this.m_meridianSymbols[j].length();
/* 1389 */           if (end > endIndex)
/*      */             continue;
/* 1391 */           String subs = text.substring(index, index + this.m_meridianSymbols[j].length());
/* 1392 */           boolean validMeridianMatch = false;
/* 1393 */           if (subs.equalsIgnoreCase(this.m_meridianSymbols[j]))
/*      */           {
/* 1395 */             validMeridianMatch = true;
/*      */ 
/* 1398 */             if ((end + 1 < endIndex) && (curOptionalToken != null))
/*      */             {
/* 1400 */               char nextChar = text.charAt(end);
/* 1401 */               if (Character.isLetter(nextChar))
/*      */               {
/* 1403 */                 validMeridianMatch = false;
/*      */               }
/*      */             }
/*      */           }
/* 1407 */           if (validMeridianMatch) {
/*      */             break;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1413 */         if (j < this.m_meridianSymbols.length)
/*      */         {
/* 1415 */           isPM = j > 0;
/* 1416 */           index += this.m_meridianSymbols[j].length();
/* 1417 */           if (parseData != null)
/*      */           {
/* 1419 */             parseData.put("someTimeSet", "1");
/*      */           }
/* 1421 */           isPMSet = true;
/* 1422 */           curOptionalToken = null;
/*      */         }
/* 1424 */         else if (curOptionalToken != null)
/*      */         {
/* 1426 */           skipForward = true;
/*      */         }
/*      */         else
/*      */         {
/* 1430 */           parsePosition.setErrorMessage(index, -2, "!syUnableToParseMeridian");
/* 1431 */           return null;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1437 */       if (skipForward)
/*      */       {
/* 1439 */         prevToken = prevTokenAtOptionalCapture;
/* 1440 */         index = curOptionalIndex;
/* 1441 */         i = curOptionalI;
/* 1442 */         parsePosition.clearError();
/* 1443 */         i = skipForward(tokens, cal, i, curOptionalToken.m_parseSkipToIndex, defaults);
/*      */       }
/*      */       else
/*      */       {
/* 1447 */         prevToken = token;
/*      */       }
/* 1449 */       parsePosition.m_index = index;
/*      */     }
/*      */ 
/* 1452 */     while ((index < endIndex) && (Character.isWhitespace(text.charAt(index))))
/*      */     {
/* 1454 */       ++index;
/*      */     }
/*      */ 
/* 1457 */     if (index < endIndex)
/*      */     {
/* 1459 */       String msg = LocaleUtils.encodeMessage("syUnableToParseTooLong", null, text.substring(index, endIndex));
/*      */ 
/* 1461 */       parsePosition.setErrorMessage(index, -9, msg);
/* 1462 */       return null;
/*      */     }
/*      */ 
/* 1465 */     while (i < tokens.length)
/*      */     {
/* 1467 */       IdcDateToken token = tokens[i];
/* 1468 */       if (token.m_parseSkipToIndex >= 0)
/*      */       {
/* 1470 */         i = skipForward(tokens, cal, i, token.m_parseSkipToIndex, defaults);
/*      */       }
/* 1472 */       else if ((token.m_type != 'S') && (token.m_type != 'W')) if (token.m_type != 'Z')
/*      */         {
/* 1480 */           parsePosition.setErrorState(index, -7);
/* 1481 */           return null;
/*      */         }
/*      */ 
/* 1484 */       ++i;
/*      */     }
/*      */ 
/* 1487 */     if (provisionalError != null)
/*      */     {
/* 1489 */       provisionalError.shallowCopy(parsePosition);
/* 1490 */       return null;
/*      */     }
/*      */ 
/* 1493 */     parsePosition.m_elementLocation = "Conversion";
/* 1494 */     parsePosition.m_indexIsErrorOffset = false;
/* 1495 */     if (isPMSet)
/*      */     {
/* 1499 */       if ((hourOfDay == -1) || ((hourOfDay > 12) && (!isPM)))
/*      */       {
/* 1501 */         parsePosition.setErrorMessage(index, -4, "!syIllegalMeridianSpecifier");
/* 1502 */         return null;
/*      */       }
/*      */ 
/* 1505 */       if ((hourOfDay < 12) && (isPM))
/*      */       {
/* 1507 */         hourOfDay += 12;
/*      */       }
/* 1509 */       else if ((hourOfDay == 12) && (!isPM))
/*      */       {
/* 1511 */         hourOfDay = 0;
/*      */       }
/* 1513 */       cal.set(11, hourOfDay);
/*      */     }
/*      */ 
/* 1516 */     if (tz != null)
/*      */     {
/* 1518 */       cal.setTimeZone(tz);
/*      */     }
/*      */ 
/* 1521 */     parsePosition.m_index = index;
/* 1522 */     if (isLeapSecond)
/*      */     {
/* 1524 */       if ((cal.get(2) == 1) && (cal.get(5) == 31) && (cal.get(10) == 23) && (cal.get(12) == 59))
/*      */       {
/* 1527 */         parseData.put("isLeapSecond", "1");
/*      */       }
/* 1529 */       cal.set(13, 60);
/*      */     }
/*      */ 
/* 1532 */     return cal;
/*      */   }
/*      */ 
/*      */   protected int parseCalendarFieldString(String text, int end, IdcDateToken token, ParseStringLocation parseLocation)
/*      */   {
/* 1537 */     int index = parseLocation.m_index;
/* 1538 */     String[] toUse = null;
/* 1539 */     int val = -1;
/* 1540 */     if (this.m_internalStrings != null)
/*      */     {
/* 1542 */       if (token.m_calendarType == 2)
/*      */       {
/* 1544 */         toUse = (token.m_length == 3) ? this.m_internalStrings.m_shortMonthNames : this.m_internalStrings.m_longMonthNames;
/*      */       }
/*      */       else
/*      */       {
/* 1548 */         toUse = (token.m_length == 3) ? this.m_internalStrings.m_shortWeekNames : this.m_internalStrings.m_longWeekNames;
/*      */       }
/*      */     }
/*      */ 
/* 1552 */     if (toUse != null)
/*      */     {
/* 1554 */       int fieldEnd = index;
/* 1555 */       while (fieldEnd < end)
/*      */       {
/* 1557 */         char ch = text.charAt(fieldEnd);
/*      */ 
/* 1561 */         boolean isValidFieldPart = Character.isLetterOrDigit(ch);
/* 1562 */         if (!isValidFieldPart) {
/*      */           break;
/*      */         }
/*      */ 
/* 1566 */         ++fieldEnd;
/*      */       }
/* 1568 */       String lookupVal = text.substring(index, fieldEnd);
/* 1569 */       if (lookupVal.length() == 0)
/*      */       {
/* 1571 */         parseLocation.setErrorState(index, -2);
/*      */       }
/*      */       else
/*      */       {
/* 1575 */         boolean foundIt = false;
/* 1576 */         for (int i = 0; i < toUse.length; ++i)
/*      */         {
/* 1578 */           if (!lookupVal.equalsIgnoreCase(toUse[i]))
/*      */             continue;
/* 1580 */           foundIt = true;
/* 1581 */           val = i;
/* 1582 */           if (token.m_calendarType != 7)
/*      */             break;
/* 1584 */           ++val; break;
/*      */         }
/*      */ 
/* 1589 */         if (foundIt)
/*      */         {
/* 1591 */           parseLocation.m_index = fieldEnd;
/*      */         }
/*      */         else
/*      */         {
/* 1595 */           parseLocation.setErrorState(index, -5);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1601 */       parseLocation.setErrorMessage(index, -6, "!syDateFormatDoesNotSupportTextDateValues");
/*      */     }
/*      */ 
/* 1604 */     return val;
/*      */   }
/*      */ 
/*      */   protected int skipForward(IdcDateToken[] tokens, Calendar cal, int i, int skipTo, Calendar defaults)
/*      */   {
/* 1610 */     while (++i < skipTo) {
/*      */       IdcDateToken token;
/*      */       while (true) { token = tokens[i];
/* 1613 */         if (token.m_type == 'S')
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1618 */         if (token.m_type != 'F')
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1626 */         switch (token.m_defaultValue)
/*      */         {
/*      */         case -2:
/*      */         case -1:
/*      */         } }
/* 1631 */       if (defaults == null)
/*      */       {
/* 1633 */         defaults = (Calendar)this.m_calendar.clone();
/* 1634 */         defaults.setTime(new Date());
/*      */       }
/* 1636 */       int value = defaults.get(token.m_calendarType);
/* 1637 */       break label126:
/*      */ 
/* 1640 */       value = token.m_defaultValue;
/*      */ 
/* 1644 */       label126: cal.set(token.m_calendarType, value);
/*      */     }
/* 1646 */     return i - 1;
/*      */   }
/*      */ 
/*      */   public boolean doesFormatUseLocalizedStrings()
/*      */   {
/* 1651 */     boolean retVal = false;
/* 1652 */     if (this.m_formatTokens != null)
/*      */     {
/* 1654 */       for (int i = 0; i < this.m_formatTokens.length; ++i)
/*      */       {
/* 1656 */         IdcDateToken token = this.m_formatTokens[i];
/* 1657 */         if ((token.m_type != 'F') || 
/* 1659 */           ((token.m_calendarType != 2) && (token.m_calendarType != 7)) || (token.m_length < 3)) {
/*      */           continue;
/*      */         }
/* 1662 */         retVal = true;
/* 1663 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1668 */     return retVal;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void setTimeZone(IdcTimeZone tz)
/*      */   {
/* 1676 */     this.m_timeZone = tz;
/*      */   }
/*      */ 
/*      */   public void setTZ(TimeZone tz)
/*      */   {
/* 1681 */     if (tz == null)
/*      */     {
/* 1683 */       throw new AssertionError("timezone set to null");
/*      */     }
/* 1685 */     this.m_timeZone = tz;
/*      */   }
/*      */ 
/*      */   public TimeZone getTimeZone()
/*      */   {
/* 1690 */     return this.m_timeZone;
/*      */   }
/*      */ 
/*      */   public Calendar getCalendar()
/*      */   {
/* 1695 */     return (Calendar)this.m_calendar.clone();
/*      */   }
/*      */ 
/*      */   public String format(Object o)
/*      */   {
/* 1700 */     Date d = null;
/* 1701 */     if (o instanceof Date)
/*      */     {
/* 1703 */       d = (Date)o;
/*      */     }
/* 1705 */     else if (o instanceof Long)
/*      */     {
/* 1707 */       Long l = (Long)o;
/* 1708 */       d = new Date(l.longValue());
/*      */     }
/* 1710 */     if (d == null)
/*      */     {
/* 1712 */       return null;
/*      */     }
/* 1714 */     return formatInternal(d, null, this.m_formatTokens);
/*      */   }
/*      */ 
/*      */   public IdcAppendableBase format(IdcAppendableBase a, Object o, Object context, int flags)
/*      */   {
/* 1719 */     String text = format(o);
/* 1720 */     if (text == null)
/*      */     {
/* 1723 */       text = format((Date)o, null);
/*      */     }
/* 1725 */     if (a == null)
/*      */     {
/* 1727 */       a = new IdcStringBuilder();
/*      */     }
/* 1729 */     a.append(text);
/* 1730 */     return a;
/*      */   }
/*      */ 
/*      */   protected String toString(IdcAppendableBase a)
/*      */   {
/* 1735 */     if (a == null)
/*      */     {
/* 1737 */       return null;
/*      */     }
/* 1739 */     return a.toString();
/*      */   }
/*      */ 
/*      */   public String format(Date d)
/*      */   {
/* 1744 */     IdcAppendableBase rc = format(null, d, null, 0);
/* 1745 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   public String format(Date d, int flags)
/*      */   {
/* 1750 */     IdcAppendableBase rc = format(null, d, null, flags);
/* 1751 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   public String format(Date d, TimeZone zone, int flags)
/*      */   {
/* 1756 */     IdcAppendableBase rc = format(null, d, zone, flags);
/* 1757 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String format(Date d, IdcTimeZone tz)
/*      */   {
/* 1764 */     IdcAppendableBase rc = format(null, d, tz, 0);
/* 1765 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String formatDateOnly(Date d, IdcTimeZone tz)
/*      */   {
/* 1772 */     IdcAppendableBase rc = format(null, d, tz, 1);
/* 1773 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String formatTimeOnly(Date d, IdcTimeZone tz)
/*      */   {
/* 1780 */     IdcAppendableBase rc = format(null, d, tz, 2);
/* 1781 */     return toString(rc);
/*      */   }
/*      */ 
/*      */   public IdcAppendableBase format(IdcAppendableBase appendable, Date d, TimeZone tz, int flags)
/*      */   {
/* 1787 */     if ((flags & 0x1) != 0)
/*      */     {
/* 1789 */       appendable = format(appendable, d, null, tz, this.m_formatDateTokens);
/*      */     }
/* 1791 */     else if ((flags & 0x2) != 0)
/*      */     {
/* 1793 */       appendable = format(appendable, d, null, tz, this.m_formatTimeTokens);
/*      */     }
/*      */     else
/*      */     {
/* 1797 */       appendable = format(appendable, d, null, tz, this.m_formatTokens);
/*      */     }
/* 1799 */     if (appendable == null)
/*      */     {
/* 1801 */       return null;
/*      */     }
/* 1803 */     return appendable;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected String formatInternal(Date d, TimeZone tz, IdcDateToken[] tokens)
/*      */   {
/* 1810 */     IdcAppendableBase appendable = format(null, d, null, tz, tokens);
/* 1811 */     if (appendable == null)
/*      */     {
/* 1813 */       return "";
/*      */     }
/* 1815 */     return appendable.toString();
/*      */   }
/*      */ 
/*      */   public IdcAppendableBase format(IdcAppendableBase buf, Date d, Calendar cal, TimeZone tz, IdcDateToken[] tokens)
/*      */   {
/* 1821 */     if ((d == null) || (tokens == null))
/*      */     {
/* 1823 */       return buf;
/*      */     }
/* 1825 */     if (cal == null)
/*      */     {
/* 1827 */       cal = (Calendar)this.m_calendar.clone();
/* 1828 */       setCalTZ(cal, tz);
/*      */     }
/* 1830 */     cal.setTime(new Date(d.getTime()));
/* 1831 */     if (buf == null)
/*      */     {
/* 1833 */       buf = new IdcStringBuilder();
/*      */     }
/*      */ 
/* 1836 */     for (int i = 0; i < tokens.length; ++i)
/*      */     {
/* 1838 */       IdcDateToken token = tokens[i];
/* 1839 */       switch (token.m_type)
/*      */       {
/*      */       case 'F':
/*      */       case 'W':
/* 1844 */         int val = cal.get(token.m_calendarType);
/* 1845 */         switch (token.m_calendarType)
/*      */         {
/*      */         case 11:
/* 1848 */           switch (token.m_sym)
/*      */           {
/*      */           case 'H':
/* 1851 */             break;
/*      */           case 'h':
/* 1853 */             if (val > 12)
/*      */             {
/* 1855 */               val -= 12;
/*      */             }
/* 1857 */             else if (val == 0)
/*      */             {
/* 1859 */               val = 12; } break;
/*      */           case 'k':
/* 1863 */             if (val == 0)
/*      */             {
/* 1865 */               val = 24; } break;
/*      */           case 'K':
/* 1869 */             if (val > 11)
/*      */             {
/* 1871 */               val -= 12;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1876 */           break;
/*      */         case 2:
/* 1879 */           ++val;
/*      */         }
/*      */ 
/* 1883 */         if (token.m_type == 'W')
/*      */         {
/* 1885 */           buf.append("" + val);
/*      */         }
/*      */         else
/*      */         {
/* 1889 */           String tmp = null;
/* 1890 */           if ((((token.m_calendarType == 2) || (token.m_calendarType == 7))) && (token.m_length > 2))
/*      */           {
/* 1893 */             String[] toUse = null;
/* 1894 */             int offset = val;
/* 1895 */             if (this.m_internalStrings != null)
/*      */             {
/* 1897 */               if (token.m_calendarType == 2)
/*      */               {
/* 1899 */                 toUse = (token.m_length == 3) ? this.m_internalStrings.m_shortMonthNames : this.m_internalStrings.m_longMonthNames;
/* 1900 */                 --offset;
/*      */               }
/*      */               else
/*      */               {
/* 1904 */                 toUse = (token.m_length == 3) ? this.m_internalStrings.m_shortWeekNames : this.m_internalStrings.m_longWeekNames;
/*      */ 
/* 1907 */                 offset -= 1;
/*      */               }
/* 1909 */               if ((toUse != null) && (offset >= 0) && (offset < toUse.length))
/*      */               {
/* 1911 */                 tmp = toUse[offset];
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/* 1916 */           if ((token.m_sym == 'y') && (token.m_length == 2) && (val > 100))
/*      */           {
/* 1918 */             val %= 100;
/*      */           }
/* 1920 */           if (tmp == null)
/*      */           {
/* 1922 */             NumberUtils.appendWithPadding(buf, val, token.m_length, '0');
/*      */           }
/*      */           else
/*      */           {
/* 1926 */             buf.append(tmp);
/*      */           }
/*      */         }
/* 1929 */         break;
/*      */       case 'S':
/*      */       case 'T':
/* 1935 */         buf.append(token.m_text);
/* 1936 */         break;
/*      */       case 'z':
/* 1941 */         TimeZone tmp = tz;
/* 1942 */         if (tmp == null)
/*      */         {
/* 1944 */           tmp = this.m_timeZone;
/*      */         }
/* 1946 */         int flags = 0;
/* 1947 */         if (token.m_length == 4)
/*      */         {
/* 1949 */           flags = 4;
/*      */         }
/* 1951 */         else if (token.m_length == 5)
/*      */         {
/* 1953 */           flags = 2;
/*      */         }
/*      */         else
/*      */         {
/* 1957 */           flags = 1;
/*      */         }
/* 1959 */         this.m_timeZoneFormat.appendFormat(tmp, buf, d, flags);
/* 1960 */         break;
/*      */       case 'Z':
/* 1965 */         if ((((tz == null) || (tz.getRawOffset() != 0) || (tz.useDaylightTime()))) && (((this.m_timeZone.getRawOffset() != 0) || (this.m_timeZone.useDaylightTime()))))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1970 */         buf.append('Z'); break;
/*      */       case 'M':
/* 1977 */         if (this.m_meridianSymbols.length != 2) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1981 */         int val = cal.get(11);
/* 1982 */         if (val < 12)
/*      */         {
/* 1984 */           buf.append(this.m_meridianSymbols[0]);
/*      */         }
/*      */         else
/*      */         {
/* 1988 */           buf.append(this.m_meridianSymbols[1]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1998 */     return buf;
/*      */   }
/*      */ 
/*      */   public IdcStringBuilder format(Object obj, IdcStringBuilder buf, FieldPosition p)
/*      */     throws IllegalArgumentException
/*      */   {
/* 2004 */     if (obj instanceof Calendar)
/*      */     {
/* 2006 */       obj = ((Calendar)obj).getTime();
/*      */     }
/* 2008 */     else if (!obj instanceof Date)
/*      */     {
/* 2010 */       String msg = LocaleUtils.encodeMessage("syUnableToFormat", null, obj);
/* 2011 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/* 2014 */     format(buf, obj, (TimeZone)null, 0);
/* 2015 */     return buf;
/*      */   }
/*      */ 
/*      */   public Date replaceTime(Date d, IdcTimeZone tz, int hour, int minute, int seconds)
/*      */     throws ServiceException
/*      */   {
/* 2033 */     Calendar cal = (Calendar)this.m_calendar.clone();
/* 2034 */     setCalTZ(cal, tz);
/* 2035 */     cal.setTime(new Date(d.getTime()));
/* 2036 */     cal.set(11, hour);
/* 2037 */     cal.set(12, minute);
/* 2038 */     cal.set(13, seconds);
/* 2039 */     Date returnDate = null;
/*      */     try
/*      */     {
/* 2042 */       returnDate = cal.getTime();
/*      */     }
/*      */     catch (IllegalArgumentException e)
/*      */     {
/* 2046 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 2049 */     return returnDate;
/*      */   }
/*      */ 
/*      */   public Object parseObject(String obj, ParseStringLocation parsePosition)
/*      */     throws ParseStringException
/*      */   {
/* 2055 */     ParseStringLocation parseLocation = new ParseStringLocation();
/* 2056 */     Date d = parseDateWithPattern(obj, parsePosition, null, this.m_timeZoneFormat);
/* 2057 */     if (parseLocation.m_state != 0)
/*      */     {
/* 2059 */       String errMsg = parseLocation.m_errMsg;
/* 2060 */       if (errMsg == null)
/*      */       {
/* 2062 */         errMsg = LocaleUtils.encodeMessage("syUnableToParseDate", null, obj);
/*      */       }
/* 2064 */       throw new ParseStringException(parseLocation, errMsg);
/*      */     }
/* 2066 */     return d;
/*      */   }
/*      */ 
/*      */   public String toPattern()
/*      */   {
/* 2071 */     String pattern = toPattern(this.m_useSTZ);
/* 2072 */     return pattern;
/*      */   }
/*      */ 
/*      */   public String toPattern(boolean useSTZ)
/*      */   {
/* 2077 */     String pattern = this.m_pattern;
/* 2078 */     if (pattern.indexOf("!t") < 0)
/*      */     {
/* 2080 */       String encoding = this.m_timeZone.getID();
/* 2081 */       pattern = pattern + "!t" + encoding;
/*      */     }
/* 2083 */     if ((pattern.indexOf("!m") < 0) && (this.m_meridianSymbols != null) && (this.m_meridianSymbols.length > 0))
/*      */     {
/* 2086 */       pattern = pattern + "!m" + this.m_meridianSymbols[0];
/* 2087 */       for (int i = 1; i < this.m_meridianSymbols.length; ++i)
/*      */       {
/* 2089 */         pattern = pattern + "," + this.m_meridianSymbols[i];
/*      */       }
/*      */     }
/* 2092 */     return pattern;
/*      */   }
/*      */ 
/*      */   public String toSimplePattern()
/*      */   {
/* 2097 */     IdcStringBuilder pattern = new IdcStringBuilder();
/* 2098 */     for (int i = 0; i < this.m_formatTokens.length; ++i)
/*      */     {
/* 2100 */       IdcDateToken token = this.m_formatTokens[i];
/* 2101 */       switch (token.m_type)
/*      */       {
/*      */       case 'T':
/* 2104 */         pattern.append(token.m_text);
/* 2105 */         break;
/*      */       case 'F':
/*      */       case 'M':
/*      */       case 'S':
/*      */       case 'W':
/*      */       case 'z':
/* 2113 */         int count = token.m_length;
/* 2114 */         while (count-- > 0)
/*      */         {
/* 2116 */           pattern.append(token.m_sym);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2122 */     return pattern.toString().trim();
/*      */   }
/*      */ 
/*      */   public boolean equals(Object obj)
/*      */   {
/* 2127 */     if (obj instanceof IdcDateFormat)
/*      */     {
/* 2129 */       IdcDateFormat fmt = (IdcDateFormat)obj;
/* 2130 */       if (!fmt.m_pattern.equals(this.m_pattern))
/*      */       {
/* 2132 */         return false;
/*      */       }
/*      */ 
/* 2136 */       return getTimeZone().equals(fmt.getTimeZone());
/*      */     }
/*      */ 
/* 2140 */     return false;
/*      */   }
/*      */ 
/*      */   public void shallowCopy(IdcDateFormat fmt)
/*      */   {
/* 2149 */     this.m_numberFormat = fmt.m_numberFormat;
/* 2150 */     this.m_timeZoneFormat = fmt.m_timeZoneFormat;
/* 2151 */     this.m_timeZone = fmt.m_timeZone;
/* 2152 */     this.m_calendar = fmt.m_calendar;
/* 2153 */     this.m_timeZone = fmt.m_timeZone;
/* 2154 */     this.m_meridianSymbols = fmt.m_meridianSymbols;
/* 2155 */     this.m_pattern = fmt.m_pattern;
/* 2156 */     this.m_parseTokens = fmt.m_parseTokens;
/* 2157 */     this.m_iso8601ShortTokens = fmt.m_iso8601ShortTokens;
/* 2158 */     this.m_formatTokens = fmt.m_formatTokens;
/* 2159 */     this.m_formatDateTokens = fmt.m_formatDateTokens;
/* 2160 */     this.m_formatTimeTokens = fmt.m_formatTimeTokens;
/* 2161 */     this.m_isIso8601 = fmt.m_isIso8601;
/* 2162 */     this.m_formatSpecifierTokens = fmt.m_formatSpecifierTokens;
/* 2163 */     this.m_internalStrings = fmt.m_internalStrings;
/* 2164 */     this.m_alternateParsingFormats = fmt.m_alternateParsingFormats;
/*      */   }
/*      */ 
/*      */   public IdcDateFormat shallowClone()
/*      */   {
/* 2172 */     IdcDateFormat fmt = new IdcDateFormat();
/* 2173 */     fmt.shallowCopy(this);
/* 2174 */     return fmt;
/*      */   }
/*      */ 
/*      */   public void copy(IdcDateFormat fmt)
/*      */   {
/* 2183 */     shallowCopy(fmt);
/* 2184 */     this.m_calendar = ((Calendar)fmt.m_calendar.clone());
/* 2185 */     this.m_timeZoneFormat = ((TimeZoneFormat)fmt.m_timeZoneFormat.clone());
/* 2186 */     this.m_timeZone = ((TimeZone)fmt.m_timeZone.clone());
/* 2187 */     this.m_meridianSymbols = ((String[])(String[])fmt.cloneArray(this.m_meridianSymbols));
/* 2188 */     this.m_parseTokens = ((IdcDateToken[])(IdcDateToken[])fmt.cloneArray(this.m_parseTokens));
/* 2189 */     this.m_iso8601ShortTokens = ((IdcDateToken[])(IdcDateToken[])fmt.cloneArray(this.m_iso8601ShortTokens));
/* 2190 */     this.m_formatTokens = ((IdcDateToken[])(IdcDateToken[])fmt.cloneArray(this.m_formatTokens));
/* 2191 */     this.m_formatDateTokens = ((IdcDateToken[])(IdcDateToken[])fmt.cloneArray(this.m_formatDateTokens));
/* 2192 */     this.m_formatTimeTokens = ((IdcDateToken[])(IdcDateToken[])fmt.cloneArray(this.m_formatTimeTokens));
/*      */   }
/*      */ 
/*      */   public Object clone()
/*      */   {
/* 2200 */     IdcDateFormat fmt = shallowClone();
/* 2201 */     fmt.copy(this);
/* 2202 */     return fmt;
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 2207 */     return toPattern();
/*      */   }
/*      */ 
/*      */   protected Object[] cloneArray(Object[] array)
/*      */   {
/* 2212 */     if (array == null)
/*      */     {
/* 2214 */       return array;
/*      */     }
/*      */     Object[] newArray;
/* 2217 */     if (array instanceof IdcDateToken[])
/*      */     {
/* 2219 */       Object[] newArray = new IdcDateToken[array.length];
/* 2220 */       for (int i = 0; i < array.length; ++i)
/*      */       {
/* 2222 */         newArray[i] = ((IdcDateToken)array[i]).clone();
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 2227 */       newArray = new String[array.length];
/* 2228 */       System.arraycopy(array, 0, newArray, 0, newArray.length);
/*      */     }
/* 2230 */     return newArray;
/*      */   }
/*      */ 
/*      */   protected void setCalTZ(Calendar cal, TimeZone tz)
/*      */   {
/* 2235 */     if (tz == null)
/*      */     {
/* 2237 */       tz = this.m_timeZone;
/*      */     }
/*      */ 
/* 2240 */     while (tz instanceof IdcTimeZone)
/*      */     {
/* 2242 */       TimeZone impl = ((IdcTimeZone)tz).m_impl;
/* 2243 */       if (impl == null) {
/*      */         break;
/*      */       }
/*      */ 
/* 2247 */       tz = impl;
/*      */     }
/* 2249 */     cal.setTimeZone(tz);
/*      */   }
/*      */ 
/*      */   protected long parseLong(String str, int end, ParseStringLocation p)
/*      */   {
/* 2256 */     long v = 0L;
/* 2257 */     int i = p.m_index;
/* 2258 */     if (end <= i)
/*      */     {
/* 2260 */       return 0L;
/*      */     }
/*      */ 
/* 2263 */     boolean isNegative = false;
/* 2264 */     char c = str.charAt(i);
/* 2265 */     switch (c)
/*      */     {
/*      */     case '-':
/* 2268 */       isNegative = true;
/*      */     case '+':
/* 2271 */       ++i;
/*      */     }
/*      */ 
/* 2275 */     boolean done = false;
/* 2276 */     while ((i < end) && (!done))
/*      */     {
/* 2278 */       c = str.charAt(i);
/* 2279 */       if ((c >= '0') && (c <= '9'))
/*      */       {
/* 2281 */         v = v * 10L + c - 48L;
/* 2282 */         ++i;
/*      */       }
/*      */ 
/* 2286 */       done = true;
/*      */     }
/*      */ 
/* 2289 */     if (isNegative)
/*      */     {
/* 2291 */       v *= -1L;
/*      */     }
/* 2293 */     p.m_index = i;
/* 2294 */     return v;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2299 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84492 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcDateFormat
 * JD-Core Version:    0.5.4
 */