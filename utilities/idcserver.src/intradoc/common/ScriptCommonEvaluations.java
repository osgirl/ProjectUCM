/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ScriptCommonEvaluations extends ScriptExtensionsAdaptor
/*     */ {
/*  32 */   protected static String m_overflowChars = "...";
/*  33 */   protected static int m_overflowCharsLength = 3;
/*     */ 
/*  39 */   public static final String[] COMMON_FUNCTIONS = { "strLength", "strEquals", "strEqualsIgnoreCase", "strSubstring", "strIndexOf", "strTrimWs", "strRemoveWs", "strReplace", "strReplaceIgnoreCase", "strLeftFill", "strRightFill", "js", "parseDate", "dateCurrent", "isTrue", "isFalse", "formatDate", "formatDateOnly", "formatDateWithPattern", "formatDateDatabase", "formatTimeOnly", "toSqlStr", "toSqlNum", "toInteger", "strLower", "strUpper", "strCenterPad", "strLeftPad", "strRightPad", "strConfine", "strCommaAppendNoDuplicates", "formatDateDisplay", "formatDateOnlyFull", "formatDateOnlyDisplay", "formatTimeOnlyDisplay", "parseDateWithPattern", "parseDataEntryDate", "jsFilename", "isVerboseTrace", "isActiveTrace", "strGenerateRandom", "regexMatches", "regexReplaceFirst", "regexReplaceAll", "strCmp", "strStartsWith", "dateReplaceTime", "strEndsWith", "removeUrlParameter", "jsFilenamePreserveCase", "strLastIndexOf" };
/*     */ 
/* 101 */   public static final int[][] COMMON_FUNCTIONS_CONFIG = { { 0, 1, 0, -1, 2 }, { 1, 2, 0, 0, 1 }, { 2, 2, 0, 0, 1 }, { 3, -1, 0, 1, 0 }, { 4, 2, 0, 0, 2 }, { 5, 1, 0, -1, 0 }, { 6, 1, 0, -1, 0 }, { 7, 3, 0, 0, 0 }, { 8, 3, 0, 0, 0 }, { 9, 3, 0, 0, 0 }, { 10, 3, 0, 0, 0 }, { 11, 1, 0, -1, 0 }, { 12, 1, -1, -1, 0 }, { 13, -1, 0, -1, 0 }, { 14, 1, -1, -1, 1 }, { 15, 1, -1, -1, 1 }, { 16, 1, -1, -1, 0 }, { 17, 1, -1, -1, 0 }, { 18, 2, -1, 0, 0 }, { 19, 1, -1, -1, 0 }, { 20, 1, -1, -1, 0 }, { 21, 1, 0, -1, 0 }, { 22, 1, 0, -1, 0 }, { 23, 1, -1, -1, 2 }, { 24, 1, 0, -1, 0 }, { 25, 1, 0, -1, 0 }, { 26, 2, 0, 1, 0 }, { 27, 2, 0, 1, 0 }, { 28, 2, 0, 1, 0 }, { 29, 2, 0, 1, 0 }, { 30, 2, 0, 0, 0 }, { 31, 1, -1, -1, 0 }, { 32, 1, -1, -1, 0 }, { 33, 1, -1, -1, 0 }, { 34, 1, -1, -1, 0 }, { 35, 2, -1, 0, 0 }, { 36, 1, -1, -1, 0 }, { 37, 1, 0, -1, 0 }, { 38, 0, -1, -1, 1 }, { 39, 1, 0, -1, 1 }, { 40, -1, 1, -1, 0 }, { 41, 2, 0, 0, 1 }, { 42, 3, 0, 0, 0 }, { 43, 3, 0, 0, 0 }, { 44, 2, 0, 0, 2 }, { 45, 2, 0, 0, 1 }, { 46, -1, -1, -1, 0 }, { 47, 2, 0, 0, 1 }, { 48, 2, 0, 0, 0 }, { 49, 1, 0, -1, 0 }, { 50, 2, 0, 0, 2 } };
/*     */ 
/*     */   public void load(ScriptRegistrator reg)
/*     */   {
/* 162 */     this.m_functionTable = COMMON_FUNCTIONS;
/* 163 */     this.m_functionDefinitionTable = COMMON_FUNCTIONS_CONFIG;
/* 164 */     super.load(reg);
/*     */ 
/* 166 */     Properties env = (Properties)AppObjectRepository.getObject("environment");
/* 167 */     String chars = env.getProperty("StrConfineOverflowChars");
/* 168 */     if (chars == null)
/*     */       return;
/* 170 */     m_overflowChars = chars;
/* 171 */     m_overflowCharsLength = chars.length();
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 183 */     String function = info.m_key;
/* 184 */     int[] config = (int[])(int[])info.m_entry;
/* 185 */     if (config == null)
/*     */     {
/* 187 */       return false;
/*     */     }
/*     */ 
/* 190 */     int nargs = args.length - 1;
/* 191 */     int allowedParams = config[1];
/* 192 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 194 */       throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, new Integer(allowedParams)));
/*     */     }
/*     */ 
/* 201 */     String sArg1 = null;
/* 202 */     String sArg2 = null;
/* 203 */     String sArg3 = null;
/* 204 */     long lArg1 = 0L;
/* 205 */     long lArg2 = 0L;
/* 206 */     long lArg3 = 0L;
/* 207 */     if (nargs > 0)
/*     */     {
/* 209 */       if (config[2] == 0)
/*     */       {
/* 211 */         sArg1 = ScriptUtils.getDisplayString(args[0], cxt);
/*     */       }
/* 213 */       else if (config[2] == 1)
/*     */       {
/* 215 */         lArg1 = ScriptUtils.getLongVal(args[0], cxt);
/*     */       }
/*     */     }
/* 218 */     if (nargs > 1)
/*     */     {
/* 220 */       if (config[3] == 0)
/*     */       {
/* 222 */         sArg2 = ScriptUtils.getDisplayString(args[1], cxt);
/*     */       }
/* 224 */       else if (config[3] == 1)
/*     */       {
/* 226 */         lArg2 = ScriptUtils.getLongVal(args[1], cxt);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 231 */     boolean bResult = false;
/* 232 */     long iResult = 0L;
/* 233 */     double dResult = 0.0D;
/* 234 */     Object oResult = null;
/*     */     String s3;
/*     */     long i3;
/*     */     Date dt;
/* 239 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 242 */       iResult = sArg1.length();
/* 243 */       break;
/*     */     case 1:
/* 246 */       bResult = sArg1.equals(sArg2);
/* 247 */       break;
/*     */     case 2:
/* 250 */       bResult = sArg1.equalsIgnoreCase(sArg2);
/* 251 */       break;
/*     */     case 3:
/* 254 */       if ((nargs < 2) || (nargs > 3))
/*     */       {
/* 256 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*     */       }
/*     */ 
/* 260 */       oResult = "";
/* 261 */       int sArg1Len = sArg1.length();
/* 262 */       if ((lArg2 >= 0L) && (lArg2 <= sArg1Len))
/*     */       {
/* 264 */         if (nargs > 2)
/*     */         {
/* 266 */           lArg3 = ScriptUtils.getLongVal(args[2], cxt);
/* 267 */           if (lArg3 > sArg1Len)
/*     */           {
/* 269 */             lArg3 = sArg1Len;
/*     */           }
/* 271 */           if (lArg3 > lArg2)
/*     */           {
/* 273 */             oResult = sArg1.substring((int)lArg2, (int)lArg3);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 278 */           oResult = sArg1.substring((int)lArg2); } 
/* 278 */       }break;
/*     */     case 4:
/* 284 */       iResult = sArg1.indexOf(sArg2);
/* 285 */       break;
/*     */     case 5:
/* 288 */       oResult = sArg1.trim();
/* 289 */       break;
/*     */     case 6:
/* 292 */       oResult = StringUtils.removeWhitespace(sArg1);
/* 293 */       break;
/*     */     case 7:
/* 296 */       s3 = ScriptUtils.getDisplayString(args[2], cxt);
/* 297 */       oResult = stringReplace(sArg1, sArg2, s3);
/* 298 */       break;
/*     */     case 8:
/* 301 */       s3 = ScriptUtils.getDisplayString(args[2], cxt);
/* 302 */       oResult = stringReplaceIgnoreCase(sArg1, sArg2, s3);
/* 303 */       break;
/*     */     case 9:
/* 306 */       i3 = ScriptUtils.getLongVal(args[2], cxt);
/* 307 */       oResult = stringFill(sArg1, sArg2, i3, false);
/* 308 */       break;
/*     */     case 10:
/* 311 */       i3 = ScriptUtils.getLongVal(args[2], cxt);
/* 312 */       oResult = stringFill(sArg1, sArg2, i3, true);
/* 313 */       break;
/*     */     case 11:
/* 316 */       oResult = StringUtils.encodeJavascriptString(sArg1);
/* 317 */       break;
/*     */     case 12:
/* 319 */       if (args[0] instanceof String)
/*     */       {
/* 321 */         oResult = LocaleResources.parseDate((String)args[0], cxt);
/*     */       }
/*     */       else
/*     */       {
/* 326 */         oResult = ScriptUtils.getDateVal(args[0], cxt);
/*     */       }
/* 328 */       break;
/*     */     case 13:
/* 331 */       Date curDate = new Date();
/* 332 */       if (nargs > 1)
/*     */       {
/* 334 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionCannotHaveMoreThanOneArg", null, function));
/*     */       }
/*     */ 
/* 337 */       if (nargs > 0)
/*     */       {
/* 339 */         String[] month_suffixes = { "M", "mo", "mos", "mon", "mons", "month", "months" };
/* 340 */         int monthIndex = 0;
/* 341 */         for (int m = month_suffixes.length - 1; m >= 0; --m)
/*     */         {
/* 343 */           if (!sArg1.endsWith(month_suffixes[m]))
/*     */             continue;
/* 345 */           monthIndex = -month_suffixes[m].length();
/* 346 */           break;
/*     */         }
/*     */ 
/* 349 */         if (monthIndex < 0)
/*     */         {
/* 351 */           sArg1 = sArg1.substring(0, sArg1.length() + monthIndex);
/* 352 */           int months = (int)ScriptUtils.getLongVal(sArg1, cxt);
/* 353 */           Calendar cal = Calendar.getInstance();
/* 354 */           cal.add(2, months);
/* 355 */           curDate = cal.getTime();
/*     */         }
/*     */         else
/*     */         {
/* 359 */           long curDateMS = curDate.getTime();
/* 360 */           long relativeMS = NumberUtils.parseTypedLong(sArg1, 0L, 18, 21);
/* 361 */           curDateMS = curDateMS += relativeMS;
/* 362 */           curDate = new Date(curDateMS);
/*     */         }
/*     */       }
/* 365 */       oResult = curDate;
/* 366 */       break;
/*     */     case 14:
/* 369 */       bResult = ScriptUtils.convertObjectToBool(args[0], false);
/* 370 */       break;
/*     */     case 15:
/* 373 */       bResult = !ScriptUtils.convertObjectToBool(args[0], true);
/* 374 */       break;
/*     */     case 16:
/* 377 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 378 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 380 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 384 */         oResult = LocaleResources.localizeDate(dt, cxt);
/*     */       }
/* 386 */       break;
/*     */     case 17:
/* 389 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 390 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 392 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 396 */         oResult = LocaleResources.localizeDateOnly(dt, cxt);
/*     */       }
/* 398 */       break;
/*     */     case 18:
/* 400 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 401 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 403 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 407 */         IdcDateFormat fmt = LocaleResources.createDateFormatFromPattern(sArg2, cxt);
/* 408 */         oResult = fmt.format(dt);
/*     */       }
/* 410 */       break;
/*     */     case 19:
/* 413 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 414 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 416 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 420 */         oResult = LocaleUtils.formatODBC(dt);
/*     */       }
/* 422 */       break;
/*     */     case 20:
/* 425 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 426 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 428 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 432 */         oResult = LocaleResources.localizeTimeOnly(dt, cxt);
/*     */       }
/* 434 */       break;
/*     */     case 21:
/* 437 */       if (sArg1.length() > 0)
/*     */       {
/* 439 */         oResult = "'" + StringUtils.createQuotableString(sArg1) + "'";
/*     */       }
/*     */       else
/*     */       {
/* 443 */         oResult = "null";
/*     */       }
/* 445 */       break;
/*     */     case 22:
/* 448 */       if (sArg1.length() > 0)
/*     */       {
/* 450 */         oResult = sArg1;
/*     */       }
/*     */       else
/*     */       {
/* 454 */         oResult = "null";
/*     */       }
/* 456 */       break;
/*     */     case 23:
/* 459 */       iResult = ScriptUtils.getLongVal(args[0], cxt);
/* 460 */       break;
/*     */     case 24:
/* 463 */       oResult = sArg1.toLowerCase();
/* 464 */       break;
/*     */     case 25:
/* 467 */       oResult = sArg1.toUpperCase();
/* 468 */       break;
/*     */     case 26:
/* 471 */       if (sArg1 == null)
/* 472 */         sArg1 = "";
/* 473 */       int spaceLength = (int)lArg2 - sArg1.length();
/* 474 */       int sideSpace = (spaceLength + spaceLength % 2) / 2;
/* 475 */       oResult = doPad(sArg1, sideSpace, sideSpace);
/* 476 */       break;
/*     */     case 27:
/* 479 */       if (sArg1 == null)
/* 480 */         sArg1 = "";
/* 481 */       int leftSpace = (int)lArg2 - sArg1.length();
/* 482 */       oResult = doPad(sArg1, leftSpace, 0);
/* 483 */       break;
/*     */     case 28:
/* 486 */       if (sArg1 == null)
/* 487 */         sArg1 = "";
/* 488 */       int rightSpace = (int)lArg2 - sArg1.length();
/* 489 */       oResult = doPad(sArg1, 0, rightSpace);
/* 490 */       break;
/*     */     case 29:
/* 493 */       if (sArg1 == null)
/* 494 */         sArg1 = "";
/* 495 */       int argLength = sArg1.length();
/* 496 */       int overflow = argLength - (int)lArg2;
/* 497 */       if (overflow > 0)
/*     */       {
/* 499 */         overflow += m_overflowCharsLength;
/* 500 */         oResult = sArg1.substring(0, argLength - overflow) + m_overflowChars;
/*     */       }
/*     */       else
/*     */       {
/* 504 */         oResult = sArg1;
/*     */       }
/* 506 */       break;
/*     */     case 30:
/* 509 */       if ((sArg1 == null) || (sArg1.length() == 0))
/*     */       {
/* 511 */         oResult = sArg2;
/*     */       }
/* 513 */       else if (sArg1.indexOf(sArg2) < 0)
/*     */       {
/* 515 */         oResult = sArg1 + "," + sArg2;
/*     */       }
/*     */       else
/*     */       {
/* 519 */         oResult = sArg1;
/*     */       }
/* 521 */       break;
/*     */     case 31:
/* 524 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 525 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 527 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 531 */         IdcDateFormat fmt = LocaleResources.getUserDisplayDateFormat(cxt);
/* 532 */         oResult = fmt.format(dt);
/*     */       }
/* 534 */       break;
/*     */     case 32:
/*     */     case 33:
/* 540 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 541 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 543 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 547 */         IdcDateFormat fmt = LocaleResources.getUserDisplayDateFormat(cxt);
/* 548 */         oResult = fmt.format(dt, 1);
/*     */       }
/* 550 */       break;
/*     */     case 34:
/* 554 */       dt = ScriptUtils.getDateVal(args[0], cxt);
/* 555 */       if ((dt == null) || (dt.getTime() == 0L))
/*     */       {
/* 557 */         oResult = "";
/*     */       }
/*     */       else
/*     */       {
/* 561 */         IdcDateFormat fmt = LocaleResources.getUserDisplayDateFormat(cxt);
/* 562 */         oResult = fmt.format(dt, 2);
/*     */       }
/* 564 */       break;
/*     */     case 35:
/* 567 */       if (!args[0] instanceof String)
/*     */       {
/* 569 */         oResult = ScriptUtils.getDateVal(args[0], cxt);
/*     */       }
/*     */       else
/*     */       {
/* 573 */         String strDate = (String)args[0];
/* 574 */         if (strDate.length() > 0)
/*     */         {
/* 579 */           ParseStringLocation parseLocation = new ParseStringLocation();
/* 580 */           IdcDateFormat fmt = LocaleResources.createDateFormatFromPattern(sArg2, cxt);
/* 581 */           oResult = fmt.parseDateFull(null, parseLocation, strDate, null, null, 0);
/* 582 */           if ((parseLocation.m_state != 0) && 
/* 584 */             (SystemUtils.m_verbose))
/*     */           {
/* 586 */             String msg = LocaleResources.createDateTraceReport("parseDateWithPattern", strDate, parseLocation);
/* 587 */             Report.debug("system", msg, null);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 592 */       break;
/*     */     case 36:
/* 594 */       if (!args[0] instanceof String)
/*     */       {
/* 596 */         oResult = ScriptUtils.getDateVal(args[0], cxt);
/*     */       }
/*     */       else
/*     */       {
/* 600 */         String strDate = (String)args[0];
/* 601 */         if (strDate.length() > 0)
/*     */         {
/* 606 */           ParseStringLocation parseLocation = new ParseStringLocation();
/* 607 */           oResult = LocaleResources.parseDateImplement(strDate, cxt, true, parseLocation);
/* 608 */           if ((((parseLocation.m_state != 0) || ((parseLocation.m_failedParsingLocations != null) && (parseLocation.m_failedParsingLocations.size() > 0)))) && 
/* 611 */             (SystemUtils.m_verbose))
/*     */           {
/* 613 */             String msg = LocaleResources.createDateTraceReport("parseDataEntryDate", strDate, parseLocation);
/* 614 */             Report.debug("system", msg, null);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 619 */       break;
/*     */     case 37:
/* 621 */       oResult = StringUtils.encodeJavascriptFilename(sArg1);
/* 622 */       break;
/*     */     case 38:
/* 625 */       bResult = SystemUtils.m_verbose;
/* 626 */       break;
/*     */     case 39:
/* 629 */       bResult = SystemUtils.isActiveTrace(sArg1.toLowerCase());
/* 630 */       break;
/*     */     case 40:
/* 633 */       if (nargs > 1)
/*     */       {
/* 635 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionCannotHaveMoreThanOneArg", null, function));
/*     */       }
/*     */ 
/* 638 */       int numChars = -1;
/* 639 */       if (nargs > 0)
/*     */       {
/* 641 */         numChars = (int)lArg1;
/*     */       }
/*     */ 
/* 644 */       oResult = (numChars >= 0) ? CryptoCommonUtils.generateRandomString(numChars) : CryptoCommonUtils.generateRandomStringOfSuggestedSize();
/*     */ 
/* 646 */       break;
/*     */     case 41:
/* 648 */       Boolean b = (Boolean)doRegex(config[0], sArg1, sArg2, null);
/* 649 */       bResult = b.booleanValue();
/* 650 */       break;
/*     */     case 42:
/*     */     case 43:
/* 654 */       if (nargs < 2)
/*     */       {
/* 656 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*     */       }
/*     */ 
/* 659 */       sArg3 = ScriptUtils.getDisplayString(args[2], cxt);
/* 660 */       oResult = doRegex(config[0], sArg1, sArg2, sArg3);
/* 661 */       break;
/*     */     case 44:
/* 663 */       iResult = sArg1.compareTo(sArg2);
/* 664 */       break;
/*     */     case 45:
/* 666 */       bResult = sArg1.startsWith(sArg2);
/* 667 */       break;
/*     */     case 46:
/* 669 */       Date d = null;
/* 670 */       if (nargs > 0)
/*     */       {
/* 672 */         d = ScriptUtils.getDateVal(args[0], cxt);
/*     */       }
/*     */       else
/*     */       {
/* 676 */         d = new Date();
/*     */       }
/* 678 */       int hour = 0;
/* 679 */       int minute = 0;
/* 680 */       int seconds = 0;
/* 681 */       int argsOffset = 1;
/* 682 */       Object[] processingArgs = args;
/* 683 */       int nProcessArgs = nargs;
/* 684 */       if (nProcessArgs > argsOffset)
/*     */       {
/* 686 */         Object oArg1 = args[1];
/* 687 */         if ((oArg1 instanceof String) && (((String)oArg1).indexOf(58) > 0))
/*     */         {
/* 690 */           List parsedList = StringUtils.makeListFromSequence((String)oArg1, ':', '%', 32);
/*     */ 
/* 692 */           Object[] newArgs = parsedList.toArray();
/* 693 */           nProcessArgs = newArgs.length;
/* 694 */           processingArgs = newArgs;
/* 695 */           argsOffset = 0;
/*     */         }
/* 697 */         hour = (int)ScriptUtils.getLongVal(processingArgs[(argsOffset++)], cxt);
/*     */       }
/* 699 */       if (nProcessArgs > argsOffset)
/*     */       {
/* 701 */         minute = (int)ScriptUtils.getLongVal(processingArgs[(argsOffset++)], cxt);
/*     */       }
/* 703 */       if (nProcessArgs > argsOffset)
/*     */       {
/* 705 */         seconds = (int)ScriptUtils.getLongVal(processingArgs[argsOffset], cxt);
/*     */       }
/* 707 */       IdcDateFormat fmt = LocaleResources.getUserDisplayDateFormat(cxt);
/* 708 */       oResult = fmt.replaceTime(d, null, hour, minute, seconds);
/* 709 */       break;
/*     */     case 47:
/* 711 */       bResult = sArg1.endsWith(sArg2);
/* 712 */       break;
/*     */     case 48:
/* 714 */       String result = sArg1.replaceAll("&" + sArg2 + "=[^&]+", "");
/* 715 */       result = result.replaceAll("\\?" + sArg2 + "=[^&]+&", "?");
/* 716 */       result = result.replaceAll("\\?" + sArg2 + "=[^&]+", "");
/* 717 */       oResult = result;
/* 718 */       break;
/*     */     case 49:
/* 721 */       oResult = StringUtils.encodeJavascriptFilenamePreserveCase(sArg1);
/* 722 */       break;
/*     */     case 50:
/* 725 */       iResult = sArg1.lastIndexOf(sArg2);
/* 726 */       break;
/*     */     default:
/* 729 */       return false;
/*     */     }
/*     */ 
/* 732 */     switch (config[4])
/*     */     {
/*     */     case 1:
/* 735 */       oResult = new Long((bResult) ? 1L : 0L);
/* 736 */       break;
/*     */     case 2:
/* 738 */       oResult = new Long(iResult);
/* 739 */       break;
/*     */     case 3:
/* 741 */       oResult = new Double(dResult);
/*     */     }
/*     */ 
/* 745 */     args[nargs] = oResult;
/*     */ 
/* 748 */     return true;
/*     */   }
/*     */ 
/*     */   protected static String stringReplace(String str, String fromStr, String toStr)
/*     */   {
/* 759 */     if ((fromStr == null) || (fromStr.length() == 0))
/*     */     {
/* 761 */       return str;
/*     */     }
/* 763 */     int index = str.indexOf(fromStr);
/* 764 */     if (index < 0)
/*     */     {
/* 766 */       return str;
/*     */     }
/*     */ 
/* 769 */     String newStr = "";
/* 770 */     int len = fromStr.length();
/*     */     do
/*     */     {
/* 773 */       newStr = newStr + str.substring(0, index) + toStr;
/* 774 */       str = str.substring(index + len);
/* 775 */       index = str.indexOf(fromStr);
/* 776 */     }while (index >= 0);
/*     */ 
/* 778 */     newStr = newStr + str;
/*     */ 
/* 782 */     return newStr;
/*     */   }
/*     */ 
/*     */   protected static String stringReplaceIgnoreCase(String str, String fromStr, String toStr)
/*     */   {
/* 787 */     if ((fromStr == null) || (fromStr.length() == 0))
/*     */     {
/* 789 */       return str;
/*     */     }
/* 791 */     String lowerStr = str.toLowerCase();
/* 792 */     String lowerFromStr = fromStr.toLowerCase();
/*     */ 
/* 794 */     int index = lowerStr.indexOf(lowerFromStr);
/* 795 */     if (index < 0)
/*     */     {
/* 797 */       return str;
/*     */     }
/*     */ 
/* 800 */     String newStr = "";
/* 801 */     int len = fromStr.length();
/*     */     do
/*     */     {
/* 804 */       newStr = newStr + str.substring(0, index) + toStr;
/*     */ 
/* 806 */       str = str.substring(index + len);
/* 807 */       lowerStr = lowerStr.substring(index + len);
/*     */ 
/* 809 */       index = lowerStr.indexOf(lowerFromStr);
/* 810 */     }while (index >= 0);
/*     */ 
/* 812 */     newStr = newStr + str;
/*     */ 
/* 817 */     return newStr;
/*     */   }
/*     */ 
/*     */   protected static String stringFill(String str, String fill, long len, boolean isRight)
/*     */   {
/* 826 */     if (fill.length() == 0)
/*     */     {
/* 828 */       return str;
/*     */     }
/*     */ 
/* 831 */     int sLength = str.length();
/* 832 */     int fLength = (int)len - sLength;
/* 833 */     if (fLength <= 0)
/*     */     {
/* 835 */       return str;
/*     */     }
/*     */ 
/* 838 */     char cfill = fill.charAt(0);
/* 839 */     String filler = "";
/* 840 */     for (int i = 0; i < fLength; ++i)
/*     */     {
/* 842 */       filler = filler + cfill;
/*     */     }
/*     */ 
/* 845 */     if (isRight)
/*     */     {
/* 847 */       str = str + filler;
/*     */     }
/*     */     else
/*     */     {
/* 851 */       str = filler + str;
/*     */     }
/*     */ 
/* 854 */     return str;
/*     */   }
/*     */ 
/*     */   protected String doPad(String str, int left, int right)
/*     */   {
/* 863 */     if ((left < 0) || (right < 0)) {
/* 864 */       return str;
/*     */     }
/* 866 */     IdcStringBuilder buf = new IdcStringBuilder(10);
/*     */ 
/* 868 */     for (int i = 0; i < left; ++i) {
/* 869 */       buf.append(' ');
/*     */     }
/* 871 */     buf.append(str);
/*     */ 
/* 873 */     for (int i = 0; i < right; ++i) {
/* 874 */       buf.append(' ');
/*     */     }
/* 876 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   protected static boolean doRegexInit()
/*     */   {
/* 885 */     return ClassHelperUtils.checkMethodExistence(ClassHelper.m_stringClass, "matches", new Class[] { ClassHelper.m_stringClass });
/*     */   }
/*     */ 
/*     */   public static Object doRegex(int function, String arg1, String arg2, String arg3)
/*     */     throws ServiceException
/*     */   {
/* 900 */     if (!doRegexInit())
/*     */     {
/* 902 */       throw new ServiceException("");
/*     */     }
/*     */ 
/* 905 */     String action = null;
/*     */     try
/*     */     {
/* 908 */       switch (function)
/*     */       {
/*     */       case 41:
/* 911 */         action = "matches";
/* 912 */         return ClassHelperUtils.executeMethod(arg1, action, new Object[] { arg2 }, new Class[] { ClassHelper.m_stringClass });
/*     */       case 42:
/*     */       case 43:
/* 916 */         if (function == 42)
/*     */         {
/* 918 */           action = "replaceFirst";
/*     */         }
/*     */         else
/*     */         {
/* 922 */           action = "replaceAll";
/*     */         }
/* 924 */         return ClassHelperUtils.executeMethod(arg1, action, new Object[] { arg2, arg3 }, new Class[] { ClassHelper.m_stringClass, ClassHelper.m_stringClass });
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 930 */       if (SystemUtils.m_verbose)
/*     */       {
/* 932 */         Report.debug("system", "Unable to perform action " + action + " with arg1=" + arg1 + "arg2=" + arg2 + " arg3=" + arg3, e);
/*     */       }
/*     */     }
/*     */ 
/* 936 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 943 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94774 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptCommonEvaluations
 * JD-Core Version:    0.5.4
 */