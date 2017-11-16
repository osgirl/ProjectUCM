/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import java.text.DecimalFormat;
/*      */ import java.text.DecimalFormatSymbols;
/*      */ import java.text.NumberFormat;
/*      */ import java.text.ParseException;
/*      */ import java.util.Locale;
/*      */ 
/*      */ public class NumberUtils
/*      */ {
/*      */   public static final int F_RETURN_MESSAGE = 1;
/*      */   public static final int TYPE_SIZE = 0;
/*      */   public static final int TYPE_TIME = 16;
/*      */   public static final int UNIT_KB = 0;
/*      */   public static final int UNIT_MB = 1;
/*      */   public static final int UNIT_GB = 2;
/*      */   public static final int UNIT_TB = 3;
/*      */   public static final int UNIT_PB = 4;
/*      */   public static final int UNIT_BYTE = 5;
/*      */   public static final int UNIT_NS = 16;
/*      */   public static final int UNIT_US = 17;
/*      */   public static final int UNIT_MS = 18;
/*      */   public static final int UNIT_MIN = 19;
/*      */   public static final int UNIT_HOUR = 20;
/*      */   public static final int UNIT_DAY = 21;
/*      */   public static final int UNIT_WEEK = 22;
/*      */   public static final int UNIT_YEAR = 23;
/*      */   public static final int UNIT_SEC = 24;
/*   56 */   public static final int[] M_UNIT_TYPES = { 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 16, 16, 16, 16, 16, 16, 16, 16, 16 };
/*      */ 
/*   85 */   public static final long[] M_UNIT_MULTIPLES = { 1024L, 1048576L, 1073741824L, 1099511627776L, 1125899906842624L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 1L, 1000L, 1000000L, 60000000000L, 3600000000000L, 86400000000000L, 604800000000000L, 31536000000000000L, 1000000000L };
/*      */ 
/*  116 */   public static final String[][] M_UNIT_SUFFIXES = { { "k", "kb" }, { "m", "mb" }, { "g", "gb" }, { "t", "tb" }, { "p", "pb" }, { "b" }, null, null, null, null, null, null, null, null, null, null, { "ns" }, { "u", "µ", "us" }, { "ms" }, { "m", "min", "mins", "minute", "minutes" }, { "h", "hr", "hrs", "hour", "hours" }, { "d", "day", "days" }, { "w", "wk", "wks", "week", "weeks" }, { "y", "yr", "yrs", "year", "years" }, { "s", "sec", "secs", "second", "seconds" } };
/*      */ 
/*  145 */   public static char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
/*      */   public static final int HN_NOSPACE = 2;
/*      */   public static final int HN_DIVISOR_1000 = 8;
/*      */   public static final int HN_USE_IEC = 256;
/* 1049 */   private static final char[] SI_SUFFIXES = "kMGTPE".toCharArray();
/* 1050 */   private static final char[] IEC_SUFFIXES = "KMGTPE".toCharArray();
/*      */   public static final int KiB = 1024;
/*      */   public static final int MiB = 1048576;
/*      */   public static final int GiB = 1073741824;
/*      */   public static final long TiB = 1099511627776L;
/*      */   public static final long PiB = 1125899906842624L;
/*      */   public static final long EiB = 1152921504606846976L;
/*      */ 
/*      */   public static void appendLong(IdcAppendable appendable, long value)
/*      */   {
/*  154 */     append(appendable, value);
/*      */   }
/*      */ 
/*      */   public static void append(IdcAppendableBase appendable, long value)
/*      */   {
/*  160 */     int index = 20;
/*  161 */     char[] digits = new char[index];
/*  162 */     boolean isNegative = value < 0L;
/*      */     do
/*      */     {
/*  166 */       int digit = (int)(value % 10L);
/*  167 */       digit = (digit < 0) ? 48 - digit : 48 + digit;
/*  168 */       digits[(--index)] = (char)digit;
/*  169 */       value /= 10L;
/*  170 */     }while (0L != value);
/*  171 */     if (isNegative)
/*      */     {
/*  173 */       digits[(--index)] = '-';
/*      */     }
/*  175 */     appendable.append(digits, index, 20 - index);
/*      */   }
/*      */ 
/*      */   public static void appendLongWithPadding(IdcAppendable appendable, long value, int padding, char padChar)
/*      */   {
/*  180 */     appendWithPadding(appendable, value, padding, padChar);
/*      */   }
/*      */ 
/*      */   public static void appendWithPadding(IdcAppendableBase appendable, long value, int padding, char padChar)
/*      */   {
/*  185 */     int index = 20;
/*  186 */     char[] digits = new char[index];
/*  187 */     boolean isNegative = value < 0L;
/*      */     do
/*      */     {
/*  191 */       int digit = (int)(value % 10L);
/*  192 */       digit = (digit < 0) ? 48 - digit : 48 + digit;
/*  193 */       digits[(--index)] = (char)digit;
/*  194 */       value /= 10L;
/*  195 */     }while (0L != value);
/*  196 */     if ((padding > 0) && (padding < 20))
/*      */     {
/*  198 */       if (isNegative)
/*      */       {
/*  200 */         --padding;
/*      */       }
/*  202 */       while (padding > 20 - index)
/*      */       {
/*  204 */         digits[(--index)] = padChar;
/*      */       }
/*      */     }
/*  207 */     if (isNegative)
/*      */     {
/*  209 */       digits[(--index)] = '-';
/*      */     }
/*  211 */     appendable.append(digits, index, 20 - index);
/*      */   }
/*      */ 
/*      */   public static void appendHexByte(IdcAppendableBase appendable, byte b)
/*      */   {
/*  216 */     int nibble = b >>> 4;
/*  217 */     appendable.append(HEX_DIGITS[(nibble & 0xF)]);
/*  218 */     nibble = b & 0xF;
/*  219 */     appendable.append(HEX_DIGITS[nibble]);
/*      */   }
/*      */ 
/*      */   public static boolean isInteger(CharSequence str)
/*      */   {
/*  229 */     boolean isIntegerVal = false;
/*  230 */     if (str != null)
/*      */     {
/*  232 */       int l = str.length();
/*  233 */       for (int i = 0; i < l; ++i)
/*      */       {
/*  235 */         char ch = str.charAt(i);
/*  236 */         if ((ch >= '0') && (ch <= '9'))
/*      */         {
/*  238 */           isIntegerVal = true;
/*      */         } else {
/*  240 */           if ((i <= 0) && (ch == '-'))
/*      */             continue;
/*  242 */           isIntegerVal = false;
/*  243 */           break;
/*      */         }
/*      */       }
/*      */     }
/*  247 */     return isIntegerVal;
/*      */   }
/*      */ 
/*      */   public static boolean isDouble(CharSequence str)
/*      */   {
/*  257 */     boolean isDoubleVal = false;
/*  258 */     boolean hasDecimal = false;
/*  259 */     boolean hasE = false;
/*  260 */     if (str != null)
/*      */     {
/*  262 */       int l = str.length();
/*  263 */       char last = '\000';
/*  264 */       for (int i = 0; i < l; ++i)
/*      */       {
/*  266 */         char ch = str.charAt(i);
/*  267 */         if ((ch >= '0') && (ch <= '9'))
/*      */         {
/*  269 */           isDoubleVal = true;
/*      */         }
/*  271 */         else if ((!hasDecimal) && (ch == '.'))
/*      */         {
/*  273 */           isDoubleVal = hasDecimal = 1;
/*      */         }
/*  275 */         else if ((i > 0) && (!hasE) && (((ch == 'e') || (ch == 'E'))))
/*      */         {
/*  277 */           hasE = true;
/*  278 */           isDoubleVal = false;
/*  279 */           hasDecimal = true;
/*      */         }
/*  281 */         else if ((hasE) && (((last == 'e') || (last == 'E'))) && (((ch == '-') || (ch == '+'))))
/*      */         {
/*  284 */           isDoubleVal = false;
/*      */         }
/*  286 */         else if ((i > 0) || (ch != '-'))
/*      */         {
/*  288 */           isDoubleVal = false;
/*  289 */           break;
/*      */         }
/*  291 */         last = ch;
/*      */       }
/*      */     }
/*  294 */     return isDoubleVal;
/*      */   }
/*      */ 
/*      */   public static int parseInteger(String str, int defValue)
/*      */   {
/*  302 */     int value = defValue;
/*      */     try
/*      */     {
/*  305 */       if ((str != null) && (str.length() > 0))
/*      */       {
/*  307 */         value = Integer.parseInt(str);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  312 */       if (SystemUtils.m_verbose)
/*      */       {
/*  314 */         Report.debug("systemparse", null, e);
/*      */       }
/*      */     }
/*  317 */     return value;
/*      */   }
/*      */ 
/*      */   public static boolean parseDecimalValue(char[] buf, int start, int stop, long[] valueArray)
/*      */   {
/*  326 */     long value = 0L;
/*  327 */     boolean doMinus = false;
/*  328 */     boolean retVal = false;
/*  329 */     for (int i = start; i < stop; ++i)
/*      */     {
/*  331 */       value *= 10L;
/*  332 */       char c = buf[i];
/*  333 */       if ((c >= '0') && (c <= '9'))
/*      */       {
/*  335 */         value += c - '0';
/*  336 */         retVal = true;
/*      */       }
/*  338 */       else if ((i == start) && (c == '-'))
/*      */       {
/*  340 */         doMinus = true;
/*      */       }
/*      */       else
/*      */       {
/*  344 */         retVal = false;
/*  345 */         break;
/*      */       }
/*      */     }
/*  348 */     if (retVal)
/*      */     {
/*  350 */       valueArray[0] = ((doMinus) ? -value : value);
/*      */     }
/*  352 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean parseHexValue(char[] buf, int start, int stop, long[] valueArray)
/*      */   {
/*  363 */     long value = 0L;
/*  364 */     for (int i = start; i < stop; ++i)
/*      */     {
/*  366 */       char c = buf[i];
/*  367 */       value *= 16L;
/*  368 */       if ((c >= '0') && (c <= '9'))
/*      */       {
/*  370 */         value += c - '0';
/*      */       }
/*  372 */       else if ((c >= 'A') && (c <= 'F'))
/*      */       {
/*  374 */         value += c - 'A' + 10;
/*      */       }
/*  376 */       else if ((c >= 'a') && (c <= 'f'))
/*      */       {
/*  378 */         value += c - 'a' + 10;
/*      */       }
/*      */       else
/*      */       {
/*  382 */         return false;
/*      */       }
/*      */     }
/*  385 */     valueArray[0] = value;
/*  386 */     return true;
/*      */   }
/*      */ 
/*      */   public static byte getHexValue(byte c)
/*      */   {
/*  394 */     if ((c >= 48) && (c <= 57))
/*      */     {
/*  396 */       return (byte)(c - 48);
/*      */     }
/*  398 */     if ((c >= 65) && (c <= 70))
/*      */     {
/*  400 */       return (byte)(c + 10 - 65);
/*      */     }
/*  402 */     if ((c >= 97) && (c <= 102))
/*      */     {
/*  404 */       return (byte)(c + 10 - 97);
/*      */     }
/*  406 */     return -1;
/*      */   }
/*      */ 
/*      */   public static long parseLong(String str, long defValue)
/*      */   {
/*  417 */     long longVal = 0L;
/*      */ 
/*  419 */     int sign = 1;
/*  420 */     if (str == null)
/*      */     {
/*  422 */       if (SystemUtils.m_verbose)
/*      */       {
/*  424 */         Report.debug("systemparse", "null long value, using default", null);
/*      */       }
/*  426 */       return defValue;
/*      */     }
/*  428 */     int length = str.length();
/*  429 */     int exp = 0;
/*  430 */     int scaleExp = 0;
/*  431 */     if (length == 0)
/*      */     {
/*  433 */       return defValue;
/*      */     }
/*  435 */     char ch = str.charAt(0);
/*  436 */     int i = 1;
/*  437 */     while (ch == ' ')
/*      */     {
/*  439 */       ch = str.charAt(i++);
/*      */     }
/*  441 */     if (length > 1)
/*      */     {
/*  443 */       if (ch == '-')
/*      */       {
/*  445 */         sign = -1;
/*  446 */         ch = str.charAt(i++);
/*      */       }
/*  448 */       else if (ch == '+')
/*      */       {
/*  450 */         ch = str.charAt(i++);
/*      */       }
/*      */     }
/*  453 */     boolean hasDigit = false;
/*  454 */     while (i <= length)
/*      */     {
/*  456 */       if ((ch >= '0') && (ch <= '9'))
/*      */       {
/*  458 */         longVal *= 10L;
/*  459 */         longVal += ch - '0';
/*  460 */         hasDigit = true;
/*      */       } else {
/*  462 */         if ((hasDigit) && (((ch == 'e') || (ch == 'E'))))
/*      */         {
/*  464 */           if (i == length)
/*      */           {
/*  466 */             if (SystemUtils.m_verbose)
/*      */             {
/*  468 */               Report.debug("systemparse", "lacking value for exponent in " + str, null);
/*      */             }
/*      */ 
/*  471 */             return defValue;
/*      */           }
/*  473 */           ch = str.charAt(i++);
/*  474 */           boolean negExp = false;
/*  475 */           if ((ch == '-') || (ch == '+'))
/*      */           {
/*  477 */             if (ch == '-')
/*      */             {
/*  479 */               negExp = true;
/*      */             }
/*  481 */             if (i == length)
/*      */             {
/*  483 */               if (SystemUtils.m_verbose)
/*      */               {
/*  485 */                 Report.debug("systemparse", "lacking value for exponent in " + str, null);
/*      */               }
/*      */ 
/*  488 */               return defValue;
/*      */             }
/*  490 */             ch = str.charAt(i++);
/*      */           }
/*  492 */           boolean inSpaces = false;
/*      */           while (true)
/*      */           {
/*  495 */             if (inSpaces)
/*      */             {
/*  497 */               if (ch != ' ')
/*      */               {
/*  499 */                 if (SystemUtils.m_verbose)
/*      */                 {
/*  501 */                   Report.debug("systemparse", "trailing non-space '" + ch + "': using default for " + str, null);
/*      */                 }
/*      */ 
/*  505 */                 return defValue;
/*      */               }
/*      */             }
/*  508 */             else if ((ch >= '0') && (ch <= '9'))
/*      */             {
/*  510 */               exp *= 10;
/*  511 */               if (negExp)
/*      */               {
/*  513 */                 exp -= ch - '0';
/*      */               }
/*      */               else
/*      */               {
/*  517 */                 exp += ch - '0';
/*      */               }
/*      */             }
/*  520 */             else if (ch == ' ')
/*      */             {
/*  522 */               inSpaces = true;
/*      */             }
/*      */             else
/*      */             {
/*  526 */               if (SystemUtils.m_verbose)
/*      */               {
/*  528 */                 Report.debug("systemparse", "unknown char '" + ch + "': using default value for " + str, null);
/*      */               }
/*      */ 
/*  532 */               return defValue;
/*      */             }
/*  534 */             if (i == length) {
/*      */               break;
/*      */             }
/*      */ 
/*  538 */             ch = str.charAt(i++);
/*      */           }
/*  540 */           if (exp > 0)
/*      */             break;
/*  542 */           if (SystemUtils.m_verbose)
/*      */           {
/*  544 */             Report.debug("systemparse", "illegal zero or negative exponent: using default value for " + str, null);
/*      */           }
/*      */ 
/*  547 */           return defValue;
/*      */         }
/*      */ 
/*  551 */         if (ch == ' ') {
/*      */           do {
/*  553 */             if (i >= length)
/*      */               break label686;
/*  555 */             ch = str.charAt(i++);
/*  556 */           }while (ch == ' ');
/*      */ 
/*  558 */           if (SystemUtils.m_verbose)
/*      */           {
/*  560 */             Report.debug("systemparse", "trailing non-space '" + ch + "': using default value for " + str, null);
/*      */           }
/*      */ 
/*  564 */           return defValue;
/*      */         }
/*      */ 
/*  571 */         if (SystemUtils.m_verbose)
/*      */         {
/*  573 */           Report.debug("systemparse", "unknown char '" + ch + "': using default value for " + str, null);
/*      */         }
/*      */ 
/*  577 */         return defValue;
/*      */       }
/*  579 */       if (i == length) {
/*      */         break;
/*      */       }
/*      */ 
/*  583 */       ch = str.charAt(i++);
/*      */     }
/*  585 */     label686: longVal *= sign;
/*  586 */     exp += scaleExp;
/*  587 */     while (exp > 0)
/*      */     {
/*  589 */       longVal *= 10L;
/*  590 */       --exp;
/*      */     }
/*  592 */     return longVal;
/*      */   }
/*      */ 
/*      */   public static double parseDouble(String str, double defValue)
/*      */   {
/*  600 */     double doubleVal = 0.0D;
/*      */ 
/*  602 */     int sign = 1;
/*  603 */     if (str == null)
/*      */     {
/*  605 */       if (SystemUtils.m_verbose)
/*      */       {
/*  607 */         Report.debug("systemparse", "null value, using default", null);
/*      */       }
/*  609 */       return defValue;
/*      */     }
/*  611 */     int length = str.length();
/*  612 */     int exp = 0;
/*  613 */     int scaleExp = 0;
/*  614 */     if (length == 0)
/*      */     {
/*  616 */       return defValue;
/*      */     }
/*  618 */     char ch = str.charAt(0);
/*  619 */     int i = 1;
/*  620 */     while (ch == ' ')
/*      */     {
/*  622 */       ch = str.charAt(i++);
/*      */     }
/*  624 */     if (length > 1)
/*      */     {
/*  626 */       if (ch == '-')
/*      */       {
/*  628 */         sign = -1;
/*  629 */         ch = str.charAt(i++);
/*      */       }
/*  631 */       else if (ch == '+')
/*      */       {
/*  633 */         ch = str.charAt(i++);
/*      */       }
/*      */     }
/*  636 */     boolean hasDigit = false;
/*  637 */     boolean hasDecimal = false;
/*  638 */     while (i <= length)
/*      */     {
/*  640 */       if ((ch >= '0') && (ch <= '9'))
/*      */       {
/*  642 */         doubleVal *= 10.0D;
/*  643 */         doubleVal += ch - '0';
/*  644 */         hasDigit = true;
/*  645 */         if (hasDecimal)
/*      */         {
/*  647 */           --scaleExp;
/*      */         }
/*      */       }
/*  650 */       else if ((!hasDecimal) && (ch == '.'))
/*      */       {
/*  652 */         hasDecimal = true;
/*      */       } else {
/*  654 */         if ((hasDigit) && (((ch == 'e') || (ch == 'E'))))
/*      */         {
/*  656 */           if (i == length)
/*      */           {
/*  658 */             if (SystemUtils.m_verbose)
/*      */             {
/*  660 */               Report.debug("systemparse", "lacking value for exponent in " + str, null);
/*      */             }
/*      */ 
/*  663 */             return defValue;
/*      */           }
/*  665 */           ch = str.charAt(i++);
/*  666 */           boolean negExp = false;
/*  667 */           if ((ch == '-') || (ch == '+'))
/*      */           {
/*  669 */             if (ch == '-')
/*      */             {
/*  671 */               negExp = true;
/*      */             }
/*  673 */             if (i == length)
/*      */             {
/*  675 */               if (SystemUtils.m_verbose)
/*      */               {
/*  677 */                 Report.debug("systemparse", "lacking value for exponent in " + str, null);
/*      */               }
/*      */ 
/*  680 */               return defValue;
/*      */             }
/*  682 */             ch = str.charAt(i++);
/*      */           }
/*  684 */           boolean inSpaces = false;
/*      */           while (true)
/*      */           {
/*  687 */             if (inSpaces)
/*      */             {
/*  689 */               if (ch != ' ')
/*      */               {
/*  691 */                 if (SystemUtils.m_verbose)
/*      */                 {
/*  693 */                   Report.debug("systemparse", "trailing non-space '" + ch + "': using default for " + str, null);
/*      */                 }
/*      */ 
/*  697 */                 return defValue;
/*      */               }
/*      */             }
/*  700 */             else if ((ch >= '0') && (ch <= '9'))
/*      */             {
/*  702 */               exp *= 10;
/*  703 */               if (negExp)
/*      */               {
/*  705 */                 exp -= ch - '0';
/*      */               }
/*      */               else
/*      */               {
/*  709 */                 exp += ch - '0';
/*      */               }
/*      */             }
/*  712 */             else if (ch == ' ')
/*      */             {
/*  714 */               inSpaces = true;
/*      */             }
/*      */             else
/*      */             {
/*  718 */               if (SystemUtils.m_verbose)
/*      */               {
/*  720 */                 Report.debug("systemparse", "unknown char '" + ch + "': using default value for " + str, null);
/*      */               }
/*      */ 
/*  724 */               return defValue;
/*      */             }
/*  726 */             if (i == length) {
/*      */               break;
/*      */             }
/*      */ 
/*  730 */             ch = str.charAt(i++);
/*      */           }
/*  732 */           if (exp != 0)
/*      */             break;
/*  734 */           if (SystemUtils.m_verbose)
/*      */           {
/*  736 */             Report.debug("systemparse", "illegal zero exponent: using default value for " + str, null);
/*      */           }
/*      */ 
/*  739 */           return defValue;
/*      */         }
/*      */ 
/*  743 */         if (ch == ' ') {
/*      */           do {
/*  745 */             if (i >= length)
/*      */               break label766;
/*  747 */             ch = str.charAt(i++);
/*  748 */           }while (ch == ' ');
/*      */ 
/*  750 */           if (SystemUtils.m_verbose)
/*      */           {
/*  752 */             Report.debug("systemparse", "trailing non-space '" + ch + "': using default value for " + str, null);
/*      */           }
/*      */ 
/*  756 */           return defValue;
/*      */         }
/*      */ 
/*  763 */         String tmpStr = str.trim().toLowerCase();
/*  764 */         if (tmpStr.equals("nan"))
/*      */         {
/*  766 */           return (0.0D / 0.0D);
/*      */         }
/*  768 */         if (tmpStr.equals("infinity"))
/*      */         {
/*  770 */           return (1.0D / 0.0D);
/*      */         }
/*  772 */         if (tmpStr.equals("-infinity"))
/*      */         {
/*  774 */           return (-1.0D / 0.0D);
/*      */         }
/*  776 */         if (SystemUtils.m_verbose)
/*      */         {
/*  778 */           Report.debug("systemparse", "unknown char '" + ch + "': using default value for " + str, null);
/*      */         }
/*      */ 
/*  782 */         return defValue;
/*      */       }
/*  784 */       if (i == length) {
/*      */         break;
/*      */       }
/*      */ 
/*  788 */       ch = str.charAt(i++);
/*      */     }
/*  790 */     label766: doubleVal *= sign;
/*  791 */     exp += scaleExp;
/*  792 */     while (exp < 0)
/*      */     {
/*  794 */       doubleVal /= 10.0D;
/*  795 */       ++exp;
/*      */     }
/*  797 */     while (exp > 0)
/*      */     {
/*  799 */       doubleVal *= 10.0D;
/*  800 */       --exp;
/*      */     }
/*  802 */     return doubleVal;
/*      */   }
/*      */ 
/*      */   public static int min(int[] numbers, boolean mustBePositive)
/*      */   {
/*  814 */     if ((numbers == null) || (numbers.length == 0)) {
/*  815 */       return 0;
/*      */     }
/*  817 */     int size = numbers.length;
/*  818 */     int min = 0;
/*  819 */     boolean minSet = false;
/*  820 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  822 */       if ((mustBePositive) && (numbers[i] < 0)) {
/*      */         continue;
/*      */       }
/*  825 */       if ((!minSet) || (numbers[i] < min))
/*  826 */         min = numbers[i];
/*      */     }
/*  828 */     return min;
/*      */   }
/*      */ 
/*      */   public static int parseTypedInteger(String str, int defaultValue, int resultUnitType, int defaultUnitType)
/*      */   {
/*  844 */     if ((str == null) || (str.length() == 0))
/*      */     {
/*  846 */       return defaultValue;
/*      */     }
/*  848 */     Object value = parseTypedIntegerInternal(str, resultUnitType, defaultUnitType, 0);
/*  849 */     if (value instanceof String)
/*      */     {
/*  851 */       if (SystemUtils.m_verbose)
/*      */       {
/*  853 */         Report.trace("systemparse", (String)value, null);
/*      */       }
/*  855 */       return defaultValue;
/*      */     }
/*  857 */     return ((Integer)value).intValue();
/*      */   }
/*      */ 
/*      */   public static long parseTypedLong(String str, long defaultValue, int resultUnitType, int defaultUnitType)
/*      */   {
/*  872 */     if ((str == null) || (str.length() == 0))
/*      */     {
/*  874 */       return defaultValue;
/*      */     }
/*  876 */     Object value = parseTypedLongInternal(str, resultUnitType, defaultUnitType, 0);
/*  877 */     if (value instanceof String)
/*      */     {
/*  879 */       if (SystemUtils.m_verbose)
/*      */       {
/*  881 */         Report.trace("systemparse", (String)value, null);
/*      */       }
/*  883 */       return defaultValue;
/*      */     }
/*  885 */     return ((Long)value).longValue();
/*      */   }
/*      */ 
/*      */   public static Object parseTypedIntegerInternal(String str, int resultUnitType, int defaultUnitType, int flags)
/*      */   {
/*  900 */     Object internalValue = parseTypedLongInternal(str, resultUnitType, defaultUnitType, flags);
/*  901 */     if (internalValue instanceof String)
/*      */     {
/*  903 */       return internalValue;
/*      */     }
/*  905 */     long value = ((Long)internalValue).longValue();
/*  906 */     if (value > 2147483647L)
/*      */     {
/*  908 */       String msg = LocaleUtils.encodeMessage("syValueTooBig", null, "" + value, "2147483647");
/*      */ 
/*  910 */       return msg;
/*      */     }
/*  912 */     if (value < -2147483648L)
/*      */     {
/*  914 */       String msg = LocaleUtils.encodeMessage("syValueTooSmall", null, "" + value, "-2147483648");
/*      */ 
/*  916 */       return msg;
/*      */     }
/*  918 */     return new Integer((int)value);
/*      */   }
/*      */ 
/*      */   public static Object parseTypedLongInternal(String str, int resultUnitType, int defaultUnitType, int flags)
/*      */   {
/*  932 */     str = str.trim().toLowerCase();
/*  933 */     int type = M_UNIT_TYPES[resultUnitType];
/*  934 */     if (M_UNIT_TYPES[defaultUnitType] != type)
/*      */     {
/*  936 */       throw new AssertionError("coding error: type mismatch: " + M_UNIT_SUFFIXES[resultUnitType] + " vs. " + M_UNIT_SUFFIXES[defaultUnitType]);
/*      */     }
/*      */ 
/*  941 */     int unit = defaultUnitType;
/*  942 */     boolean found = false;
/*  943 */     for (int i = type; (!found) && (i < type + 16) && (i < M_UNIT_SUFFIXES.length); ++i)
/*      */     {
/*  945 */       String[] suffixes = M_UNIT_SUFFIXES[i];
/*  946 */       if (suffixes == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  950 */       for (int j = 0; j < suffixes.length; ++j)
/*      */       {
/*  952 */         if (!str.endsWith(suffixes[j]))
/*      */           continue;
/*  954 */         if (str.length() > suffixes[j].length())
/*      */         {
/*  956 */           int index = str.length() - suffixes[j].length() - 1;
/*  957 */           char ch = str.charAt(index);
/*  958 */           if ((ch >= 'a') && (ch <= 'z')) {
/*      */             continue;
/*      */           }
/*      */         }
/*      */ 
/*  963 */         unit = i;
/*  964 */         str = str.substring(0, str.length() - suffixes[j].length());
/*  965 */         str = str.trim();
/*  966 */         found = true;
/*  967 */         break;
/*      */       }
/*      */     }
/*      */ 
/*  971 */     double value = parseDouble(str, (0.0D / 0.0D));
/*  972 */     if (Double.isNaN(value))
/*      */     {
/*  974 */       String msg = LocaleUtils.encodeMessage("syUnableToParse", null, str);
/*      */ 
/*  976 */       return msg;
/*      */     }
/*  978 */     value = value * M_UNIT_MULTIPLES[unit] / M_UNIT_MULTIPLES[resultUnitType];
/*  979 */     if (value > 9.223372036854776E+018D)
/*      */     {
/*  981 */       String msg = LocaleUtils.encodeMessage("syValueTooBig", null, Double.toString(value), Long.toString(9223372036854775807L));
/*      */ 
/*  983 */       return msg;
/*      */     }
/*  985 */     if (value < -9.223372036854776E+018D)
/*      */     {
/*  987 */       String msg = LocaleUtils.encodeMessage("syValueTooSmall", null, Double.toString(value), Long.toString(-9223372036854775808L));
/*      */ 
/*  989 */       return msg;
/*      */     }
/*  991 */     return new Long(()value);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static long getRandomLong(boolean returnPositive)
/*      */   {
/*  998 */     Report.deprecatedUsage("Use CryptoUtils.getRandomLong() instead of NumberUtils.getRandomLong().");
/*      */ 
/* 1001 */     long randomNumber = CryptoCommonUtils.getRandomLong();
/*      */ 
/* 1003 */     if ((randomNumber < 0L) && (returnPositive))
/*      */     {
/* 1005 */       randomNumber *= -1L;
/*      */     }
/* 1007 */     return randomNumber;
/*      */   }
/*      */ 
/*      */   public static long parseHexStringAsLong(String str)
/*      */   {
/* 1012 */     if ((str == null) || (str.length() == 0))
/*      */     {
/* 1014 */       return 0L;
/*      */     }
/* 1016 */     if ((str.startsWith("0x")) || (str.startsWith("0X")))
/*      */     {
/* 1018 */       str = str.substring(2);
/*      */     }
/* 1020 */     return Long.parseLong(str, 16);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String formatSizeToBytes(long value)
/*      */   {
/* 1030 */     return formatHumanizedBytes(value, 0, null);
/*      */   }
/*      */ 
/*      */   public static String formatHumanizedBytes(long value, int flags, ExecutionContext cxt)
/*      */   {
/* 1060 */     CommonLocalizationHandler clh = CommonLocalizationHandlerFactory.createInstance();
/* 1061 */     boolean useDivisor1000 = (flags & 0x8) != 0;
/* 1062 */     char[] suffixes = IEC_SUFFIXES;
/* 1063 */     double ddivisor = 1024.0D;
/* 1064 */     if (useDivisor1000)
/*      */     {
/* 1066 */       ddivisor = 1000.0D;
/* 1067 */       suffixes = SI_SUFFIXES;
/*      */     }
/*      */ 
/* 1070 */     int scale = -1;
/*      */     double number;
/* 1071 */     if (value < 0L)
/*      */     {
/* 1073 */       double number = 1.844674407370955E+019D + value;
/* 1074 */       value = 1024L;
/*      */     }
/*      */     else
/*      */     {
/* 1078 */       number = value;
/*      */     }
/* 1080 */     int base = (useDivisor1000) ? 1000 : 1024;
/* 1081 */     if (value >= ddivisor)
/*      */     {
/*      */       double oldDivisor;
/*      */       do
/*      */       {
/* 1086 */         ++scale;
/* 1087 */         oldDivisor = ddivisor;
/* 1088 */         ddivisor *= base;
/*      */       }
/* 1090 */       while (number >= ddivisor);
/* 1091 */       number /= oldDivisor;
/*      */     }
/*      */ 
/* 1094 */     if (number + 0.5D >= base)
/*      */     {
/* 1096 */       number = 1.0D;
/* 1097 */       ++scale;
/*      */     }
/* 1099 */     IdcStringBuilder formatted = new IdcStringBuilder();
/* 1100 */     formatted.append(clh.formatDecimal(number, (scale >= 0) ? 1 : 0, cxt));
/* 1101 */     if ((flags & 0x2) == 0)
/*      */     {
/* 1103 */       formatted.append(' ');
/*      */     }
/* 1105 */     if (scale >= 0)
/*      */     {
/* 1107 */       formatted.append(suffixes[scale]);
/* 1108 */       if ((flags & 0x100) != 0)
/*      */       {
/* 1110 */         formatted.append('i');
/*      */       }
/*      */     }
/* 1113 */     formatted.append('B');
/* 1114 */     return formatted.toString();
/*      */   }
/*      */ 
/*      */   public static String formatNumber(String value, Locale fromLocale, Locale toLocale)
/*      */     throws ParseException
/*      */   {
/* 1120 */     value = formatNumber(value, fromLocale, toLocale, false);
/* 1121 */     return value;
/*      */   }
/*      */ 
/*      */   public static String formatNumber(String value, Locale fromLocale, Locale toLocale, boolean doUseGrouping)
/*      */     throws ParseException
/*      */   {
/* 1141 */     value = value.replace(" ", "");
/*      */ 
/* 1144 */     NumberFormat fromNumFormat = NumberFormat.getInstance(fromLocale);
/* 1145 */     DecimalFormat fromDecimalFormat = (DecimalFormat)fromNumFormat;
/*      */ 
/* 1147 */     NumberFormat toNumFormat = NumberFormat.getInstance(toLocale);
/* 1148 */     toNumFormat.setGroupingUsed(doUseGrouping);
/* 1149 */     DecimalFormat toDecimalFormat = (DecimalFormat)toNumFormat;
/*      */ 
/* 1152 */     DecimalFormatSymbols fromLocaleDecimalSymbols = fromDecimalFormat.getDecimalFormatSymbols();
/* 1153 */     char fromLocaleGroupSep = fromLocaleDecimalSymbols.getGroupingSeparator();
/* 1154 */     char fromLocaleDecSep = fromLocaleDecimalSymbols.getDecimalSeparator();
/*      */ 
/* 1156 */     DecimalFormatSymbols toLocaleDecimalSymbols = toDecimalFormat.getDecimalFormatSymbols();
/* 1157 */     char toLocaleGroupSep = toLocaleDecimalSymbols.getGroupingSeparator();
/* 1158 */     char toLocaleDecSep = toLocaleDecimalSymbols.getDecimalSeparator();
/*      */ 
/* 1161 */     if (doUseGrouping == true)
/*      */     {
/* 1163 */       value = value.replace(fromLocaleGroupSep, toLocaleGroupSep);
/*      */     }
/*      */     else
/*      */     {
/* 1167 */       String fromLocaleGroupSepString = Character.toString(fromLocaleGroupSep);
/* 1168 */       value = value.replace(fromLocaleGroupSepString, "");
/*      */     }
/*      */ 
/* 1172 */     value = value.replace(fromLocaleDecSep, toLocaleDecSep);
/*      */ 
/* 1174 */     return value;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1180 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89455 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.NumberUtils
 * JD-Core Version:    0.5.4
 */