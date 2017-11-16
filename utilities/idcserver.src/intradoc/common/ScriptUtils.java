/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Date;
/*     */ 
/*     */ public class ScriptUtils
/*     */ {
/*  32 */   public static final ScriptObject NULL_LOCAL = new NullScriptObject(1);
/*  33 */   public static final ScriptObject NULL_RESULTSET = new NullScriptObject(2);
/*  34 */   public static final ScriptObject NULL_GENERIC_OBJECT = new NullScriptObject(3);
/*     */ 
/*     */   public static Object parseString(String sVal, int eltType, boolean forceToBool, ExecutionContext cxt)
/*     */     throws IllegalArgumentException
/*     */   {
/*  46 */     if (sVal != null)
/*     */     {
/*  50 */       switch (eltType)
/*     */       {
/*     */       case 0:
/*  53 */         if (forceToBool)
/*     */         {
/*  55 */           boolean bVal = getBooleanVal(sVal);
/*  56 */           long retVal = (bVal) ? 1L : 0L;
/*  57 */           return new Long(retVal);
/*     */         }
/*  59 */         return sVal;
/*     */       case 1:
/*  61 */         long lVal = getLongVal(sVal, cxt);
/*  62 */         if (forceToBool)
/*     */         {
/*  64 */           long retVal = (lVal != 0L) ? 1L : 0L;
/*  65 */           return new Long(retVal);
/*     */         }
/*  67 */         return new Long(lVal);
/*     */       case 2:
/*  69 */         double dVal = getDoubleVal(sVal, cxt);
/*  70 */         if (forceToBool)
/*     */         {
/*  72 */           long retVal = (dVal != 0.0D) ? 1L : 0L;
/*  73 */           return new Long(retVal);
/*     */         }
/*  75 */         return new Double(dVal);
/*     */       case 3:
/*  77 */         Date dateVal = getDateVal(sVal, cxt);
/*  78 */         if (forceToBool)
/*     */         {
/*  80 */           long retVal = (dateVal.getTime() != 0L) ? 1L : 0L;
/*  81 */           return new Long(retVal);
/*     */         }
/*  83 */         return dateVal;
/*     */       }
/*     */     }
/*     */ 
/*  87 */     return new Long(0L);
/*     */   }
/*     */ 
/*     */   public static String getDisplayString(Object scriptVal, ExecutionContext cxt)
/*     */   {
/*  95 */     if (scriptVal == null)
/*     */     {
/*  97 */       return "";
/*     */     }
/*  99 */     if (scriptVal instanceof String)
/*     */     {
/* 101 */       return (String)scriptVal;
/*     */     }
/* 103 */     if (scriptVal instanceof Date)
/*     */     {
/* 105 */       Date dateVal = (Date)scriptVal;
/* 106 */       if (dateVal.getTime() == 0L)
/*     */       {
/* 108 */         return "";
/*     */       }
/* 110 */       return LocaleResources.localizeDate(dateVal, cxt);
/*     */     }
/* 112 */     if (scriptVal instanceof Boolean)
/*     */     {
/* 114 */       Boolean bVal = (Boolean)scriptVal;
/* 115 */       return (bVal.booleanValue()) ? "1" : "0";
/*     */     }
/* 117 */     if (scriptVal instanceof ScriptObject)
/*     */     {
/* 119 */       return ((ScriptObject)scriptVal).getRepresentativeString();
/*     */     }
/* 121 */     return scriptVal.toString();
/*     */   }
/*     */ 
/*     */   public static boolean convertObjectToBool(Object scriptVal, boolean defVal)
/*     */   {
/* 130 */     if (scriptVal == null)
/*     */     {
/* 132 */       return defVal;
/*     */     }
/* 134 */     if (scriptVal instanceof String)
/*     */     {
/* 136 */       return StringUtils.convertToBool((String)scriptVal, defVal);
/*     */     }
/* 138 */     return getBooleanVal(scriptVal);
/*     */   }
/*     */ 
/*     */   public static boolean getBooleanVal(Object scriptVal)
/*     */   {
/* 147 */     if (scriptVal != null)
/*     */     {
/* 149 */       if (scriptVal instanceof Number)
/*     */       {
/* 151 */         Number numVal = (Number)scriptVal;
/*     */ 
/* 153 */         if ((!numVal instanceof Long) && ((
/* 155 */           (numVal instanceof Float) || (numVal instanceof Double))))
/*     */         {
/* 157 */           return numVal.doubleValue() != 0.0D;
/*     */         }
/*     */ 
/* 160 */         return numVal.longValue() != 0L;
/*     */       }
/* 162 */       boolean isDevBuild = SystemUtils.m_isDevelopmentEnvironment;
/* 163 */       if (scriptVal instanceof String)
/*     */       {
/* 166 */         String sVal = (String)scriptVal;
/* 167 */         boolean test = sVal.trim().length() > 0;
/* 168 */         if ((isDevBuild) && (!test) && (sVal.length() > 0))
/*     */         {
/* 170 */           Report.trace(null, "nonempty string evaluated to false", null);
/*     */         }
/* 172 */         return test;
/*     */       }
/* 174 */       if (scriptVal instanceof IdcCharArrayWriter)
/*     */       {
/* 176 */         IdcCharArrayWriter w = (IdcCharArrayWriter)scriptVal;
/* 177 */         boolean test = w.isAllSpaces();
/* 178 */         if ((isDevBuild) && (!test) && (w.m_length > 0))
/*     */         {
/* 180 */           Report.trace(null, "nonempty string evaluated to false", null);
/*     */         }
/* 182 */         return test;
/*     */       }
/* 184 */       if (scriptVal instanceof Date)
/*     */       {
/* 186 */         Date dte = (Date)scriptVal;
/* 187 */         return dte.getTime() != 0L;
/*     */       }
/* 189 */       if (scriptVal instanceof Boolean)
/*     */       {
/* 191 */         Boolean bVal = (Boolean)scriptVal;
/* 192 */         return bVal.booleanValue();
/*     */       }
/* 194 */       if (scriptVal instanceof ScriptObject)
/*     */       {
/* 196 */         return !scriptVal instanceof NullScriptObject;
/*     */       }
/* 198 */       String sVal = scriptVal.toString();
/* 199 */       boolean test = sVal.trim().length() > 0;
/* 200 */       if ((isDevBuild) && (!test) && (sVal.length() > 0))
/*     */       {
/* 202 */         Report.trace(null, "nonempty string evaluated to false", null);
/*     */       }
/* 204 */       return test;
/*     */     }
/* 206 */     return false;
/*     */   }
/*     */ 
/*     */   public static double getDoubleVal(Object scriptVal, ExecutionContext cxt)
/*     */     throws NumberFormatException
/*     */   {
/* 214 */     if (scriptVal != null)
/*     */     {
/*     */       try
/*     */       {
/* 218 */         if (scriptVal instanceof Number)
/*     */         {
/* 220 */           Number numVal = (Number)scriptVal;
/* 221 */           return numVal.doubleValue();
/*     */         }
/* 223 */         if (scriptVal instanceof String)
/*     */         {
/* 226 */           String sVal = (String)scriptVal;
/* 227 */           if (sVal.trim().length() == 0)
/*     */           {
/* 229 */             return 0.0D;
/*     */           }
/* 231 */           return new Double(sVal).doubleValue();
/*     */         }
/* 233 */         if (scriptVal instanceof Date)
/*     */         {
/* 235 */           Date dte = (Date)scriptVal;
/* 236 */           return dte.getTime();
/*     */         }
/* 238 */         if (scriptVal instanceof Boolean)
/*     */         {
/* 240 */           Boolean bVal = (Boolean)scriptVal;
/* 241 */           return (bVal.booleanValue()) ? 1.0D : 0.0D;
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 246 */         throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilErrorParsingFloat", null, scriptVal.toString()));
/*     */       }
/*     */ 
/* 250 */       throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilErrorParsingFloat", null, scriptVal.toString()));
/*     */     }
/*     */ 
/* 253 */     return 0.0D;
/*     */   }
/*     */ 
/*     */   public static long getLongVal(Object scriptVal, ExecutionContext cxt)
/*     */     throws NumberFormatException
/*     */   {
/* 261 */     if (scriptVal != null)
/*     */     {
/*     */       try
/*     */       {
/* 265 */         if (scriptVal instanceof Number)
/*     */         {
/* 267 */           Number numVal = (Number)scriptVal;
/* 268 */           return numVal.longValue();
/*     */         }
/* 270 */         if (scriptVal instanceof String)
/*     */         {
/* 273 */           String sVal = (String)scriptVal;
/* 274 */           if (sVal.trim().length() == 0)
/*     */           {
/* 276 */             return 0L;
/*     */           }
/* 278 */           return Long.parseLong(sVal);
/*     */         }
/* 280 */         if (scriptVal instanceof Date)
/*     */         {
/* 282 */           Date dte = (Date)scriptVal;
/* 283 */           return dte.getTime();
/*     */         }
/* 285 */         if (scriptVal instanceof Boolean)
/*     */         {
/* 287 */           Boolean bVal = (Boolean)scriptVal;
/* 288 */           return (bVal.booleanValue()) ? 1L : 0L;
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 293 */         throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilErrorParsingInt", null, scriptVal.toString()));
/*     */       }
/*     */ 
/* 296 */       throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilErrorParsingInt", null, scriptVal.toString()));
/*     */     }
/*     */ 
/* 299 */     return 0L;
/*     */   }
/*     */ 
/*     */   public static Date getDateVal(Object scriptVal, ExecutionContext cxt)
/*     */     throws IllegalArgumentException
/*     */   {
/* 307 */     if (scriptVal != null)
/*     */     {
/* 309 */       ParseStringLocation parseLocation = new ParseStringLocation();
/*     */       try
/*     */       {
/* 312 */         if (scriptVal instanceof Number)
/*     */         {
/* 314 */           Number numVal = (Number)scriptVal;
/* 315 */           return new Date(numVal.longValue());
/*     */         }
/* 317 */         if (scriptVal instanceof String)
/*     */         {
/* 320 */           String sVal = (String)scriptVal;
/* 321 */           sVal = sVal.trim();
/* 322 */           if (sVal.length() == 0)
/*     */           {
/* 324 */             return new Date(0L);
/*     */           }
/* 326 */           if (determineIfInteger(sVal, cxt))
/*     */           {
/* 333 */             long l = Long.parseLong(sVal);
/*     */ 
/* 335 */             return new Date(l);
/*     */           }
/*     */ 
/* 338 */           Date d = LocaleResources.parseDateImplement(sVal, cxt, false, parseLocation);
/* 339 */           if (d != null)
/*     */           {
/* 341 */             return d;
/*     */           }
/*     */         }
/* 344 */         if (scriptVal instanceof Date)
/*     */         {
/* 346 */           Date dte = (Date)scriptVal;
/* 347 */           return dte;
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 352 */         String msg = LocaleUtils.encodeMessage("csScriptUtilErrorParsingDate", null, scriptVal.toString());
/* 353 */         String priorMsg = t.getMessage();
/* 354 */         if (priorMsg.startsWith("!"))
/*     */         {
/* 356 */           msg = LocaleUtils.appendMessage(msg, priorMsg);
/*     */         }
/* 358 */         throw new IllegalArgumentException(msg);
/*     */       }
/* 360 */       if ((parseLocation.m_state != 0) && 
/* 362 */         (SystemUtils.m_verbose))
/*     */       {
/* 364 */         String msg = LocaleResources.createDateTraceReport("parseDate", scriptVal.toString(), parseLocation);
/* 365 */         Report.debug("system", msg, null);
/*     */       }
/*     */ 
/* 368 */       throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptUtilErrorParsingDate", null, scriptVal.toString()));
/*     */     }
/*     */ 
/* 371 */     return new Date(0L);
/*     */   }
/*     */ 
/*     */   public static int determineNumberType(Object scriptVal, ExecutionContext cxt)
/*     */     throws NumberFormatException
/*     */   {
/* 380 */     if (scriptVal != null)
/*     */     {
/* 382 */       if (scriptVal instanceof Number)
/*     */       {
/* 384 */         Number numVal = (Number)scriptVal;
/* 385 */         if ((numVal instanceof Double) || (numVal instanceof Float))
/*     */         {
/* 387 */           return 2;
/*     */         }
/* 389 */         return 1;
/*     */       }
/*     */ 
/* 392 */       if (scriptVal instanceof String)
/*     */       {
/* 394 */         String sVal = (String)scriptVal;
/* 395 */         if (sVal.trim().length() == 0)
/*     */         {
/* 397 */           return 1;
/*     */         }
/* 399 */         char[] buf = new char[sVal.length()];
/* 400 */         sVal.getChars(0, sVal.length(), buf, 0);
/*     */ 
/* 403 */         int start = 0;
/* 404 */         if (buf[0] == '-')
/*     */         {
/* 406 */           start = 1;
/*     */         }
/* 408 */         int type = GrammarParser.determineFormatContent(buf, start, sVal.length());
/* 409 */         if ((type == 1) || (type == 2))
/*     */         {
/* 411 */           return type;
/*     */         }
/* 413 */         throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilValueNotNumber", null, sVal));
/*     */       }
/*     */ 
/* 416 */       if (scriptVal instanceof Date)
/*     */       {
/* 418 */         return 1;
/*     */       }
/* 420 */       throw new NumberFormatException(LocaleUtils.encodeMessage("csScriptUtilCannotConvertValue", null, scriptVal.toString()));
/*     */     }
/*     */ 
/* 424 */     return 1;
/*     */   }
/*     */ 
/*     */   public static boolean determineIfInteger(String scriptVal, ExecutionContext cxt)
/*     */   {
/* 429 */     if (scriptVal == null)
/*     */     {
/* 431 */       return false;
/*     */     }
/* 433 */     int len = scriptVal.length();
/* 434 */     int start = 0;
/* 435 */     if ((len > 0) && (scriptVal.charAt(0) == '-'))
/*     */     {
/* 437 */       start = 1;
/*     */     }
/* 439 */     if (start >= len)
/*     */     {
/* 441 */       return false;
/*     */     }
/* 443 */     for (int i = start; i < len; ++i)
/*     */     {
/* 445 */       if (!Character.isDigit(scriptVal.charAt(i)))
/*     */       {
/* 447 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 451 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object performOperation(char firstChar, char secondChar, Object obj1, Object obj2, ExecutionContext cxt)
/*     */     throws NumberFormatException
/*     */   {
/* 469 */     int type = -1;
/* 470 */     int compareDir = 0;
/* 471 */     boolean allowEqual = false;
/* 472 */     boolean allowDate = false;
/* 473 */     boolean hasBooleanOperands = false;
/* 474 */     boolean isUni = false;
/*     */ 
/* 476 */     switch (firstChar)
/*     */     {
/*     */     case '-':
/* 479 */       type = 0;
/* 480 */       isUni = true;
/* 481 */       break;
/*     */     case '+':
/* 483 */       if (obj2 == null)
/*     */       {
/* 485 */         return obj1;
/*     */       }
/* 487 */       type = 1;
/* 488 */       allowDate = true;
/* 489 */       break;
/*     */     case '*':
/* 491 */       type = 2;
/* 492 */       break;
/*     */     case '/':
/* 494 */       type = 3;
/* 495 */       break;
/*     */     case '%':
/* 497 */       type = 4;
/* 498 */       break;
/*     */     case '>':
/* 500 */       type = 5;
/* 501 */       allowEqual = secondChar == '=';
/* 502 */       compareDir = 1;
/* 503 */       break;
/*     */     case '=':
/* 505 */       if (secondChar == '=')
/*     */       {
/* 507 */         type = 5;
/* 508 */         compareDir = 0;
/* 509 */         allowEqual = true; } break;
/*     */     case '!':
/* 513 */       type = 5;
/* 514 */       compareDir = 0;
/* 515 */       break;
/*     */     case '<':
/* 517 */       type = 5;
/* 518 */       compareDir = -1;
/* 519 */       allowEqual = secondChar == '=';
/* 520 */       break;
/*     */     case 'e':
/* 522 */       type = 5;
/* 523 */       compareDir = 0;
/* 524 */       allowEqual = true;
/* 525 */       break;
/*     */     case 'g':
/* 527 */       type = 5;
/* 528 */       allowEqual = secondChar == 'e';
/* 529 */       compareDir = 1;
/* 530 */       break;
/*     */     case 'l':
/* 532 */       if (secondChar == 'i')
/*     */       {
/* 535 */         String str1 = getDisplayString(obj1, cxt);
/* 536 */         String str2 = getDisplayString(obj2, cxt);
/* 537 */         boolean isMatched = StringUtils.matchEx(str1, str2, true, true);
/* 538 */         return (isMatched) ? Boolean.TRUE : Boolean.FALSE;
/*     */       }
/* 540 */       type = 5;
/* 541 */       compareDir = -1;
/* 542 */       allowEqual = secondChar == 'e';
/* 543 */       break;
/*     */     case 'n':
/* 545 */       if (secondChar == 'e')
/*     */       {
/* 547 */         type = 5;
/* 548 */         compareDir = 0;
/*     */       }
/*     */       else
/*     */       {
/* 552 */         type = 6;
/* 553 */         hasBooleanOperands = true;
/* 554 */         isUni = true;
/*     */       }
/* 556 */       break;
/*     */     case 'a':
/* 558 */       type = 7;
/* 559 */       hasBooleanOperands = true;
/* 560 */       break;
/*     */     case 'o':
/* 562 */       type = 8;
/* 563 */       hasBooleanOperands = true;
/*     */     }
/*     */ 
/* 567 */     if (type < 0)
/*     */     {
/* 569 */       return null;
/*     */     }
/*     */ 
/* 573 */     if (hasBooleanOperands)
/*     */     {
/* 575 */       boolean obj1BVal = getBooleanVal(obj1);
/* 576 */       boolean bRetVal = false;
/* 577 */       if (type == 6)
/*     */       {
/* 580 */         bRetVal = !obj1BVal;
/*     */       }
/*     */       else
/*     */       {
/* 584 */         boolean obj2BVal = getBooleanVal(obj2);
/* 585 */         if (type == 7)
/*     */         {
/* 588 */           bRetVal = (obj1BVal) && (obj2BVal);
/*     */         }
/*     */         else
/*     */         {
/* 593 */           bRetVal = (obj1BVal) || (obj2BVal);
/*     */         }
/*     */       }
/* 596 */       return (bRetVal) ? Boolean.TRUE : Boolean.FALSE;
/*     */     }
/*     */ 
/* 600 */     int obj1Type = determineNumberType(obj1, cxt);
/* 601 */     int obj2Type = -1;
/* 602 */     if (obj2 != null)
/*     */     {
/* 604 */       obj2Type = determineNumberType(obj2, cxt);
/*     */     }
/* 606 */     boolean useDouble = false;
/* 607 */     if ((obj1Type == 2) || (obj2Type == 2))
/*     */     {
/* 609 */       useDouble = true;
/*     */     }
/*     */ 
/* 612 */     double obj1DVal = 0.0D;
/* 613 */     long obj1LVal = 0L;
/* 614 */     if (useDouble)
/*     */     {
/* 616 */       obj1DVal = getDoubleVal(obj1, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 620 */       obj1LVal = getLongVal(obj1, cxt);
/*     */     }
/*     */ 
/* 623 */     if ((isUni) && 
/* 626 */       (type == 0))
/*     */     {
/* 628 */       if (useDouble)
/*     */       {
/* 630 */         return new Double(-obj1DVal);
/*     */       }
/* 632 */       return new Long(-obj1LVal);
/*     */     }
/*     */ 
/* 637 */     boolean toDate = (allowDate) && (((obj1 instanceof Date) || (obj2 instanceof Date)));
/*     */ 
/* 640 */     double obj2DVal = 0.0D;
/* 641 */     long obj2LVal = 0L;
/* 642 */     if (useDouble)
/*     */     {
/* 644 */       obj2DVal = getDoubleVal(obj2, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 648 */       obj2LVal = getLongVal(obj2, cxt);
/*     */     }
/*     */ 
/* 653 */     long retLVal = 0L;
/* 654 */     double retDVal = 0.0D;
/* 655 */     switch (type)
/*     */     {
/*     */     case 1:
/* 658 */       if (useDouble)
/*     */       {
/* 660 */         retDVal = obj1DVal + obj2DVal;
/*     */       }
/*     */       else
/*     */       {
/* 664 */         retLVal = obj1LVal + obj2LVal;
/*     */       }
/* 666 */       break;
/*     */     case 2:
/* 668 */       if (useDouble)
/*     */       {
/* 670 */         retDVal = obj1DVal * obj2DVal;
/*     */       }
/*     */       else
/*     */       {
/* 674 */         retLVal = obj1LVal * obj2LVal;
/*     */       }
/* 676 */       break;
/*     */     case 3:
/* 678 */       if (useDouble)
/*     */       {
/* 680 */         if ((obj2DVal < 1.0E-006D) && (obj2DVal > 1.0E-006D))
/*     */         {
/* 682 */           throw new NumberFormatException("!csScriptUtilDenomenatorCloseToZero");
/*     */         }
/* 684 */         retDVal = obj1DVal / obj2DVal;
/*     */       }
/*     */       else
/*     */       {
/* 688 */         if (obj2LVal == 0L)
/*     */         {
/* 690 */           throw new NumberFormatException("!csScriptUtilDenomenatorZero");
/*     */         }
/* 692 */         retLVal = obj1LVal / obj2LVal;
/*     */       }
/* 694 */       break;
/*     */     case 4:
/* 696 */       if (useDouble)
/*     */       {
/* 698 */         throw new NumberFormatException("!csScriptUtilModErrorFloat");
/*     */       }
/* 700 */       if (obj2LVal <= 0L)
/*     */       {
/* 702 */         throw new NumberFormatException("!csScriptUtilModSyntax");
/*     */       }
/* 704 */       retLVal = obj1LVal % obj2LVal;
/* 705 */       break;
/*     */     case 5:
/* 707 */       boolean retBVal = evaluateComparisonOperator(useDouble, obj1LVal, obj2LVal, obj1DVal, obj2DVal, compareDir, allowEqual);
/*     */ 
/* 709 */       useDouble = false;
/* 710 */       retLVal = (retBVal) ? 1L : 0L;
/*     */     }
/*     */ 
/* 714 */     if (toDate)
/*     */     {
/* 716 */       long timeVal = (useDouble) ? ()retDVal : retLVal;
/* 717 */       return new Date(timeVal);
/*     */     }
/* 719 */     if (useDouble)
/*     */     {
/* 721 */       return new Double(retDVal);
/*     */     }
/*     */ 
/* 724 */     return new Long(retLVal);
/*     */   }
/*     */ 
/*     */   protected static boolean evaluateComparisonOperator(boolean useDouble, long obj1LVal, long obj2LVal, double obj1DVal, double obj2DVal, int compareDir, boolean allowEqual)
/*     */   {
/* 731 */     boolean equalTestVal = false;
/* 732 */     if (useDouble)
/*     */     {
/* 734 */       equalTestVal = obj1DVal == obj2DVal;
/*     */     }
/*     */     else
/*     */     {
/* 738 */       equalTestVal = obj1LVal == obj2LVal;
/*     */     }
/*     */ 
/* 741 */     if ((compareDir == 0) || (equalTestVal == true))
/*     */     {
/* 743 */       if (!allowEqual)
/*     */       {
/* 745 */         equalTestVal = !equalTestVal;
/*     */       }
/* 747 */       return equalTestVal;
/*     */     }
/*     */ 
/* 751 */     boolean compareTestVal = false;
/*     */ 
/* 753 */     if (useDouble)
/*     */     {
/* 755 */       compareTestVal = obj1DVal > obj2DVal;
/*     */     }
/*     */     else
/*     */     {
/* 759 */       compareTestVal = obj1LVal > obj2LVal;
/*     */     }
/* 761 */     if (compareDir < 0)
/*     */     {
/* 763 */       compareTestVal = !compareTestVal;
/*     */     }
/* 765 */     return compareTestVal;
/*     */   }
/*     */ 
/*     */   public static ScriptObject getNullScriptObject(int type)
/*     */   {
/* 776 */     if (type == 1)
/*     */     {
/* 778 */       return NULL_LOCAL;
/*     */     }
/* 780 */     if (type == 2)
/*     */     {
/* 782 */       return NULL_RESULTSET;
/*     */     }
/* 784 */     return NULL_GENERIC_OBJECT;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 789 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ScriptUtils
 * JD-Core Version:    0.5.4
 */