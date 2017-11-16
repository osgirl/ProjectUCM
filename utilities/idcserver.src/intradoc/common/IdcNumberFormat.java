/*      */ package intradoc.common;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.InvalidObjectException;
/*      */ import java.io.ObjectInputStream;
/*      */ import java.math.BigInteger;
/*      */ 
/*      */ public class IdcNumberFormat
/*      */ {
/*      */   public static final int MAX_COUNT = 19;
/*      */   public static final int INTEGER_FIELD = 0;
/*      */   public static final int FRACTION_FIELD = 1;
/*      */   private static final int DOUBLE_INTEGER_DIGITS = 309;
/*      */   private static final int DOUBLE_FRACTION_DIGITS = 340;
/*      */   private static final int STATUS_INFINITE = 0;
/*      */   private static final int STATUS_POSITIVE = 1;
/*      */   private static final int STATUS_LENGTH = 2;
/*   43 */   private String m_positivePrefix = "";
/*   44 */   private String m_positiveSuffix = "";
/*   45 */   private String m_negativePrefix = "-";
/*   46 */   private String m_negativeSuffix = "";
/*      */ 
/*   48 */   private int m_maximumIntegerDigits = 40;
/*   49 */   private int m_minimumIntegerDigits = 1;
/*   50 */   private int m_maximumFractionDigits = 3;
/*   51 */   private int m_minimumFractionDigits = 0;
/*   52 */   private int m_multiplier = 1;
/*   53 */   private byte m_groupingSize = 3;
/*      */ 
/*   55 */   private boolean m_groupingUsed = true;
/*   56 */   private boolean m_parseIntegerOnly = false;
/*   57 */   private boolean m_decimalSeparatorAlwaysShown = false;
/*      */ 
/*   60 */   private static String m_NaN = "�";
/*   61 */   private static String m_infinity = "∞";
/*   62 */   private static char m_zeroDigit = '0';
/*   63 */   private static char m_decimalSeparator = '.';
/*   64 */   private static char m_groupingSeparator = ',';
/*      */   private static byte[] LONG_MIN_REP;
/*      */ 
/*      */   public IdcNumberFormat()
/*      */   {
/*      */   }
/*      */ 
/*      */   public IdcNumberFormat(char decimalSeparator, char groupingSeparator, char zero)
/*      */   {
/*   97 */     m_decimalSeparator = decimalSeparator;
/*   98 */     m_groupingSeparator = groupingSeparator;
/*   99 */     m_zeroDigit = zero;
/*      */   }
/*      */ 
/*      */   public final IdcStringBuilder format(Object number, IdcStringBuilder toAppendTo, IdcPosition pos)
/*      */   {
/*  104 */     if ((number instanceof Long) || ((number instanceof BigInteger) && (((BigInteger)number).bitLength() < 64)))
/*      */     {
/*  107 */       return format(((Number)number).longValue(), toAppendTo, pos);
/*      */     }
/*  109 */     if (number instanceof Number)
/*      */     {
/*  111 */       return format(((Number)number).doubleValue(), toAppendTo, pos);
/*      */     }
/*      */ 
/*  115 */     throw new IllegalArgumentException("!csNumberFormatObjectInvalid");
/*      */   }
/*      */ 
/*      */   public final String format(double number)
/*      */   {
/*  121 */     return format(number, new IdcStringBuilder(), new IdcPosition(0, true)).toString();
/*      */   }
/*      */ 
/*      */   public final String format(long number)
/*      */   {
/*  126 */     return format(number, new IdcStringBuilder(), new IdcPosition(0, true)).toString();
/*      */   }
/*      */ 
/*      */   public IdcStringBuilder format(double number, IdcStringBuilder result, IdcPosition fieldPosition)
/*      */   {
/*  139 */     fieldPosition.setBeginIndex(0);
/*  140 */     fieldPosition.setEndIndex(0);
/*      */ 
/*  143 */     if (Double.isNaN(number))
/*      */     {
/*  145 */       if (fieldPosition.getField() == 0)
/*      */       {
/*  147 */         fieldPosition.setBeginIndex(result.length());
/*      */       }
/*      */ 
/*  150 */       result.append(m_NaN);
/*      */ 
/*  152 */       if (fieldPosition.getField() == 0)
/*      */       {
/*  154 */         fieldPosition.setEndIndex(result.length());
/*      */       }
/*      */ 
/*  157 */       return result;
/*      */     }
/*      */ 
/*  170 */     boolean isNegative = (number < 0.0D) || ((number == 0.0D) && (1.0D / number < 0.0D));
/*  171 */     if (isNegative) number = -number;
/*      */ 
/*  174 */     if (this.m_multiplier != 1) number *= this.m_multiplier;
/*      */ 
/*  176 */     if (Double.isInfinite(number))
/*      */     {
/*  178 */       result.append((isNegative) ? this.m_negativePrefix : this.m_positivePrefix);
/*      */ 
/*  180 */       if (fieldPosition.getField() == 0)
/*      */       {
/*  182 */         fieldPosition.setBeginIndex(result.length());
/*      */       }
/*      */ 
/*  185 */       result.append(m_infinity);
/*      */ 
/*  187 */       if (fieldPosition.getField() == 0)
/*      */       {
/*  189 */         fieldPosition.setEndIndex(result.length());
/*      */       }
/*      */ 
/*  192 */       result.append((isNegative) ? this.m_negativeSuffix : this.m_positiveSuffix);
/*  193 */       return result;
/*      */     }
/*      */ 
/*  198 */     byte[] digits = setDigits(number, getMaximumFractionDigits());
/*      */ 
/*  200 */     return subformat(digits, result, fieldPosition, isNegative, false);
/*      */   }
/*      */ 
/*      */   public IdcStringBuilder format(long number, IdcStringBuilder result, IdcPosition fieldPosition)
/*      */   {
/*  214 */     fieldPosition.setBeginIndex(0);
/*  215 */     fieldPosition.setEndIndex(0);
/*      */ 
/*  217 */     boolean isNegative = number < 0L;
/*  218 */     if (isNegative) number = -number;
/*      */ 
/*  226 */     if ((this.m_multiplier != 1) && (this.m_multiplier != 0))
/*      */     {
/*  228 */       boolean useDouble = false;
/*      */ 
/*  230 */       if (number < 0L)
/*      */       {
/*  232 */         long cutoff = -9223372036854775808L / this.m_multiplier;
/*  233 */         useDouble = number < cutoff;
/*      */       }
/*      */       else
/*      */       {
/*  237 */         long cutoff = 9223372036854775807L / this.m_multiplier;
/*  238 */         useDouble = number > cutoff;
/*      */       }
/*      */ 
/*  241 */       if (useDouble)
/*      */       {
/*  243 */         double dnumber = number;
/*  244 */         return format(dnumber, result, fieldPosition);
/*      */       }
/*      */     }
/*      */ 
/*  248 */     number *= this.m_multiplier;
/*      */ 
/*  250 */     byte[] digits = setDigits(number, 0);
/*      */ 
/*  252 */     return subformat(digits, result, fieldPosition, isNegative, true);
/*      */   }
/*      */ 
/*      */   private IdcStringBuilder subformat(byte[] digits, IdcStringBuilder result, IdcPosition fieldPosition, boolean isNegative, boolean isInteger)
/*      */   {
/*  262 */     char zero = m_zeroDigit;
/*  263 */     int zeroDelta = zero - '0';
/*  264 */     char grouping = m_groupingSeparator;
/*  265 */     char decimal = m_decimalSeparator;
/*      */ 
/*  278 */     result.append((isNegative) ? this.m_negativePrefix : this.m_positivePrefix);
/*      */ 
/*  281 */     if (fieldPosition.getField() == 0)
/*      */     {
/*  283 */       fieldPosition.setBeginIndex(result.length());
/*      */     }
/*      */ 
/*  290 */     int count = getMinimumIntegerDigits();
/*  291 */     int decimalAt = getDecimalAt(digits);
/*  292 */     int digitIndex = 0;
/*  293 */     if ((decimalAt > 0) && (count < decimalAt))
/*      */     {
/*  295 */       count = decimalAt;
/*      */     }
/*      */ 
/*  302 */     if (count > getMaximumIntegerDigits())
/*      */     {
/*  304 */       count = getMaximumIntegerDigits();
/*  305 */       digitIndex = decimalAt - count;
/*      */     }
/*      */ 
/*  308 */     int sizeBeforeIntegerPart = result.length();
/*  309 */     for (int i = count - 1; i >= 0; --i)
/*      */     {
/*  311 */       if ((i < decimalAt) && (digitIndex < digits.length))
/*      */       {
/*  314 */         result.append((char)(digits[(digitIndex++)] + zeroDelta));
/*      */       }
/*      */       else
/*      */       {
/*  319 */         result.append(zero);
/*      */       }
/*      */ 
/*  325 */       if ((!isGroupingUsed()) || (i <= 0) || (this.m_groupingSize == 0) || (i % this.m_groupingSize != 0))
/*      */         continue;
/*  327 */       result.append(grouping);
/*      */     }
/*      */ 
/*  332 */     if (fieldPosition.getField() == 0)
/*      */     {
/*  334 */       fieldPosition.setEndIndex(result.length());
/*      */     }
/*      */ 
/*  339 */     boolean fractionPresent = (getMinimumFractionDigits() > 0) || ((!isInteger) && (digitIndex < digits.length));
/*      */ 
/*  345 */     if ((!fractionPresent) && (result.length() == sizeBeforeIntegerPart))
/*      */     {
/*  347 */       result.append(zero);
/*      */     }
/*      */ 
/*  351 */     if ((this.m_decimalSeparatorAlwaysShown) || (fractionPresent))
/*      */     {
/*  353 */       result.append(decimal);
/*  354 */       ++digitIndex;
/*      */     }
/*      */ 
/*  358 */     if (fieldPosition.getField() == 1) {
/*  359 */       fieldPosition.setBeginIndex(result.length());
/*      */     }
/*  361 */     for (int i = 0; i < getMaximumFractionDigits(); ++i)
/*      */     {
/*  368 */       if (i >= getMinimumFractionDigits()) { if (isInteger) break; if (digitIndex >= digits.length)
/*      */         {
/*      */           break;
/*      */         }
/*      */  }
/*      */ 
/*      */ 
/*  377 */       if (-1 - i > decimalAt - 1)
/*      */       {
/*  379 */         result.append(zero);
/*      */       }
/*  385 */       else if ((!isInteger) && (digitIndex < digits.length))
/*      */       {
/*  387 */         result.append((char)(digits[(digitIndex++)] + zeroDelta));
/*      */       }
/*      */       else
/*      */       {
/*  391 */         result.append(zero);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  396 */     if (fieldPosition.getField() == 1) {
/*  397 */       fieldPosition.setEndIndex(result.length());
/*      */     }
/*  399 */     result.append((isNegative) ? this.m_negativeSuffix : this.m_positivePrefix);
/*      */ 
/*  401 */     return result;
/*      */   }
/*      */ 
/*      */   public Number parse(String text) throws ParseStringException
/*      */   {
/*  406 */     ParseStringLocation parsePosition = new ParseStringLocation();
/*  407 */     Number result = parse(text, parsePosition);
/*  408 */     if ((parsePosition.m_index == 0) || (parsePosition.m_state != 0))
/*      */     {
/*  410 */       if (parsePosition.m_state == 0)
/*      */       {
/*  412 */         parsePosition.m_state = -7;
/*      */       }
/*  414 */       throw new ParseStringException(parsePosition, LocaleUtils.encodeMessage("csNumberFormatUnparseableNumber", null, text));
/*      */     }
/*      */ 
/*  417 */     return result;
/*      */   }
/*      */ 
/*      */   public Number parse(String text, ParseStringLocation parsePosition)
/*      */   {
/*  433 */     if (text.regionMatches(parsePosition.m_index, m_NaN, 0, m_NaN.length()))
/*      */     {
/*  435 */       parsePosition.m_index += m_NaN.length();
/*  436 */       return new Double((0.0D / 0.0D));
/*      */     }
/*      */ 
/*  439 */     boolean[] status = new boolean[2];
/*  440 */     byte[] digitList = null;
/*      */ 
/*  443 */     if (text.length() > 19) digitList = new byte[19]; else {
/*  444 */       digitList = new byte[text.length()];
/*      */     }
/*  446 */     if (!subparse(text, parsePosition, digitList, status))
/*      */     {
/*  448 */       return null;
/*      */     }
/*      */ 
/*  451 */     double doubleResult = 0.0D;
/*  452 */     long longResult = 0L;
/*  453 */     boolean gotDouble = true;
/*      */ 
/*  456 */     if (status[0] != 0)
/*      */     {
/*  458 */       doubleResult = (1.0D / 0.0D);
/*      */     }
/*  460 */     else if (fitsIntoLong(digitList, status[1], isParseIntegerOnly()))
/*      */     {
/*  462 */       gotDouble = false;
/*  463 */       longResult = getLongValue(digitList);
/*      */     }
/*      */     else
/*      */     {
/*  467 */       doubleResult = getDoubleValue(digitList);
/*      */     }
/*      */ 
/*  472 */     if (this.m_multiplier != 1)
/*      */     {
/*  474 */       if (gotDouble)
/*      */       {
/*  476 */         doubleResult /= this.m_multiplier;
/*      */       }
/*  481 */       else if (longResult % this.m_multiplier == 0L)
/*      */       {
/*  483 */         longResult /= this.m_multiplier;
/*      */       }
/*      */       else
/*      */       {
/*  487 */         doubleResult = longResult / this.m_multiplier;
/*  488 */         if (doubleResult < 0.0D) doubleResult = -doubleResult;
/*  489 */         gotDouble = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  494 */     if (status[1] == 0)
/*      */     {
/*  496 */       doubleResult = -doubleResult;
/*      */ 
/*  499 */       if (longResult > 0L)
/*      */       {
/*  501 */         longResult = -longResult;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  512 */     if ((this.m_multiplier != 1) && (gotDouble))
/*      */     {
/*  514 */       longResult = ()doubleResult;
/*  515 */       gotDouble = (doubleResult != longResult) || ((doubleResult == 0.0D) && (status[1] == 0) && (!isParseIntegerOnly()));
/*      */     }
/*      */ 
/*  519 */     return (gotDouble) ? new Double(doubleResult) : new Long(longResult);
/*      */   }
/*      */ 
/*      */   private final boolean subparse(String text, ParseStringLocation parsePosition, byte[] digits, boolean[] status)
/*      */   {
/*  536 */     int position = parsePosition.m_index;
/*  537 */     int oldStart = parsePosition.m_index;
/*      */ 
/*  541 */     boolean gotPositive = text.regionMatches(position, this.m_positivePrefix, 0, this.m_positivePrefix.length());
/*      */ 
/*  543 */     boolean gotNegative = text.regionMatches(position, this.m_negativePrefix, 0, this.m_negativePrefix.length());
/*      */ 
/*  545 */     if ((gotPositive) && (gotNegative))
/*      */     {
/*  547 */       if (this.m_positivePrefix.length() > this.m_negativePrefix.length())
/*  548 */         gotNegative = false;
/*  549 */       else if (this.m_positivePrefix.length() < this.m_negativePrefix.length())
/*  550 */         gotPositive = false;
/*      */     }
/*  552 */     if (gotPositive)
/*      */     {
/*  554 */       position += this.m_positivePrefix.length();
/*      */     }
/*  556 */     else if (gotNegative)
/*      */     {
/*  558 */       position += this.m_negativePrefix.length();
/*      */     }
/*      */     else
/*      */     {
/*  562 */       parsePosition.setErrorState(position, -3);
/*  563 */       return false;
/*      */     }
/*      */ 
/*  567 */     status[0] = false;
/*  568 */     if (text.regionMatches(position, m_infinity, 0, m_infinity.length()))
/*      */     {
/*  570 */       position += m_infinity.length();
/*  571 */       status[0] = true;
/*      */     }
/*      */     else
/*      */     {
/*  582 */       int decimalAt = 0;
/*  583 */       int count = 0;
/*  584 */       char zero = m_zeroDigit;
/*  585 */       char decimal = m_decimalSeparator;
/*  586 */       char grouping = m_groupingSeparator;
/*  587 */       boolean sawDecimal = false;
/*  588 */       boolean sawDigit = false;
/*      */ 
/*  592 */       int digitCount = 0;
/*      */ 
/*  594 */       int backup = -1;
/*  595 */       for (; position < text.length(); ++position)
/*      */       {
/*  597 */         char ch = text.charAt(position);
/*      */ 
/*  610 */         int digit = ch - zero;
/*  611 */         if ((digit < 0) || (digit > 9)) digit = Character.digit(ch, 10);
/*      */ 
/*  613 */         if (digit == 0)
/*      */         {
/*  616 */           backup = -1;
/*  617 */           sawDigit = true;
/*      */ 
/*  620 */           if (count == 0)
/*      */           {
/*  623 */             if (!sawDecimal)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/*  628 */             --decimalAt;
/*      */           }
/*      */           else
/*      */           {
/*  632 */             ++digitCount;
/*  633 */             digits[(count++)] = (byte)(digit + 48);
/*      */           }
/*      */         }
/*  636 */         else if ((digit > 0) && (digit <= 9))
/*      */         {
/*  638 */           sawDigit = true;
/*  639 */           ++digitCount;
/*  640 */           digits[(count++)] = (byte)(digit + 48);
/*      */ 
/*  643 */           backup = -1;
/*      */         }
/*  645 */         else if (ch == decimal)
/*      */         {
/*  649 */           if (isParseIntegerOnly()) break; if (sawDecimal) break;
/*  650 */           decimalAt = digitCount;
/*  651 */           digits[(count++)] = 46;
/*  652 */           sawDecimal = true;
/*      */         } else {
/*  654 */           if ((ch != grouping) || (!isGroupingUsed()))
/*      */             break;
/*  656 */           if (sawDecimal)
/*      */           {
/*      */             break;
/*      */           }
/*      */ 
/*  662 */           backup = position;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  667 */       if (backup != -1) position = backup;
/*      */ 
/*  670 */       if (!sawDecimal) decimalAt = digitCount;
/*      */ 
/*  676 */       if ((!sawDigit) && (digitCount == 0))
/*      */       {
/*  678 */         parsePosition.setErrorState(oldStart, -3);
/*  679 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  684 */     if (gotPositive)
/*      */     {
/*  686 */       gotPositive = text.regionMatches(position, this.m_positivePrefix, 0, this.m_positivePrefix.length());
/*      */     }
/*      */ 
/*  690 */     if (gotNegative)
/*      */     {
/*  692 */       gotNegative = text.regionMatches(position, this.m_negativeSuffix, 0, this.m_negativeSuffix.length());
/*      */     }
/*      */ 
/*  697 */     if ((gotPositive) && (gotNegative)) {
/*  698 */       if (this.m_positivePrefix.length() > this.m_negativeSuffix.length())
/*  699 */         gotNegative = false;
/*  700 */       else if (this.m_positivePrefix.length() < this.m_negativeSuffix.length()) {
/*  701 */         gotPositive = false;
/*      */       }
/*      */     }
/*      */ 
/*  705 */     if (gotPositive == gotNegative) {
/*  706 */       parsePosition.setErrorState(position, -3);
/*  707 */       return false;
/*      */     }
/*      */ 
/*  710 */     parsePosition.m_index = (position + ((gotPositive) ? this.m_positivePrefix.length() : this.m_negativeSuffix.length()));
/*      */ 
/*  713 */     status[1] = gotPositive;
/*  714 */     if (parsePosition.m_index == oldStart)
/*      */     {
/*  716 */       parsePosition.setErrorState(position, -3);
/*  717 */       return false;
/*      */     }
/*  719 */     return true;
/*      */   }
/*      */ 
/*      */   public String getPositivePrefix()
/*      */   {
/*  728 */     return this.m_positivePrefix;
/*      */   }
/*      */ 
/*      */   public void setPositivePrefix(String newValue)
/*      */   {
/*  737 */     this.m_positivePrefix = newValue;
/*      */   }
/*      */ 
/*      */   public String getNegativePrefix()
/*      */   {
/*  746 */     return this.m_negativePrefix;
/*      */   }
/*      */ 
/*      */   public void setNegativePrefix(String newValue)
/*      */   {
/*  755 */     this.m_negativePrefix = newValue;
/*      */   }
/*      */ 
/*      */   public String getPositiveSuffix()
/*      */   {
/*  764 */     return this.m_positiveSuffix;
/*      */   }
/*      */ 
/*      */   public void setPositiveSuffix(String newValue)
/*      */   {
/*  773 */     this.m_positiveSuffix = newValue;
/*      */   }
/*      */ 
/*      */   public String getNegativeSuffix()
/*      */   {
/*  782 */     return this.m_negativeSuffix;
/*      */   }
/*      */ 
/*      */   public void setNegativeSuffix(String newValue)
/*      */   {
/*  791 */     this.m_negativeSuffix = newValue;
/*      */   }
/*      */ 
/*      */   public int getMultiplier()
/*      */   {
/*  803 */     return this.m_multiplier;
/*      */   }
/*      */ 
/*      */   public void setMultiplier(int newValue)
/*      */   {
/*  815 */     this.m_multiplier = newValue;
/*      */   }
/*      */ 
/*      */   public int getMaximumIntegerDigits()
/*      */   {
/*  825 */     return this.m_maximumIntegerDigits;
/*      */   }
/*      */ 
/*      */   public int getMinimumIntegerDigits()
/*      */   {
/*  835 */     return this.m_minimumIntegerDigits;
/*      */   }
/*      */ 
/*      */   public int getMaximumFractionDigits()
/*      */   {
/*  845 */     return this.m_maximumFractionDigits;
/*      */   }
/*      */ 
/*      */   public int getMinimumFractionDigits()
/*      */   {
/*  855 */     return this.m_minimumFractionDigits;
/*      */   }
/*      */ 
/*      */   public void setMinimumFractionDigits(int newValue)
/*      */   {
/*  872 */     this.m_minimumFractionDigits = Math.max(0, newValue);
/*  873 */     if (this.m_maximumFractionDigits >= this.m_minimumFractionDigits)
/*      */       return;
/*  875 */     this.m_maximumFractionDigits = this.m_minimumFractionDigits;
/*      */   }
/*      */ 
/*      */   public int getGroupingSize()
/*      */   {
/*  888 */     return this.m_groupingSize;
/*      */   }
/*      */ 
/*      */   public void setGroupingSize(int newValue)
/*      */   {
/*  900 */     this.m_groupingSize = (byte)newValue;
/*      */   }
/*      */ 
/*      */   public boolean isDecimalSeparatorAlwaysShown()
/*      */   {
/*  910 */     return this.m_decimalSeparatorAlwaysShown;
/*      */   }
/*      */ 
/*      */   public void setDecimalSeparatorAlwaysShown(boolean newValue)
/*      */   {
/*  920 */     this.m_decimalSeparatorAlwaysShown = newValue;
/*      */   }
/*      */ 
/*      */   public boolean equals(Object obj)
/*      */   {
/*  929 */     if (obj == null) return false;
/*  930 */     IdcNumberFormat other = (IdcNumberFormat)obj;
/*      */ 
/*  932 */     return (this.m_maximumIntegerDigits == other.m_maximumIntegerDigits) && (this.m_minimumIntegerDigits == other.m_minimumIntegerDigits) && (this.m_maximumFractionDigits == other.m_maximumFractionDigits) && (this.m_minimumFractionDigits == other.m_minimumFractionDigits) && (this.m_groupingUsed == other.m_groupingUsed) && (this.m_parseIntegerOnly == other.m_parseIntegerOnly) && (this.m_positivePrefix.equals(other.m_positivePrefix)) && (this.m_positiveSuffix.equals(other.m_positiveSuffix)) && (this.m_negativePrefix.equals(other.m_negativePrefix)) && (this.m_negativeSuffix.equals(other.m_negativeSuffix)) && (this.m_multiplier == other.m_multiplier) && (this.m_groupingSize == other.m_groupingSize) && (this.m_decimalSeparatorAlwaysShown == other.m_decimalSeparatorAlwaysShown);
/*      */   }
/*      */ 
/*      */   public boolean isGroupingUsed()
/*      */   {
/*  955 */     return this.m_groupingUsed;
/*      */   }
/*      */ 
/*      */   public void setGroupingUsed(boolean newValue)
/*      */   {
/*  964 */     this.m_groupingUsed = newValue;
/*      */   }
/*      */ 
/*      */   public boolean isParseIntegerOnly()
/*      */   {
/*  975 */     return this.m_parseIntegerOnly;
/*      */   }
/*      */ 
/*      */   public void setParseIntegerOnly(boolean value)
/*      */   {
/*  984 */     this.m_parseIntegerOnly = value;
/*      */   }
/*      */ 
/*      */   public int hashCode()
/*      */   {
/*  993 */     return super.hashCode() * 37 + this.m_positivePrefix.hashCode();
/*      */   }
/*      */ 
/*      */   public void setMaximumIntegerDigits(int newValue)
/*      */   {
/* 1004 */     int value = Math.min(newValue, 309);
/*      */ 
/* 1006 */     this.m_maximumIntegerDigits = Math.max(0, value);
/* 1007 */     if (this.m_minimumIntegerDigits <= this.m_maximumIntegerDigits)
/*      */       return;
/* 1009 */     this.m_minimumIntegerDigits = this.m_maximumIntegerDigits;
/*      */   }
/*      */ 
/*      */   public void setMinimumIntegerDigits(int newValue)
/*      */   {
/* 1019 */     int value = Math.min(newValue, 309);
/*      */ 
/* 1021 */     this.m_minimumIntegerDigits = Math.max(0, value);
/* 1022 */     if (this.m_minimumIntegerDigits <= this.m_maximumIntegerDigits)
/*      */       return;
/* 1024 */     this.m_maximumIntegerDigits = this.m_minimumIntegerDigits;
/*      */   }
/*      */ 
/*      */   public void setMaximumFractionDigits(int newValue)
/*      */   {
/* 1035 */     int value = Math.min(newValue, 340);
/*      */ 
/* 1037 */     this.m_maximumFractionDigits = Math.max(0, value);
/* 1038 */     if (this.m_maximumFractionDigits >= this.m_minimumFractionDigits)
/*      */       return;
/* 1040 */     this.m_minimumFractionDigits = this.m_maximumFractionDigits;
/*      */   }
/*      */ 
/*      */   public String getNaN()
/*      */   {
/* 1046 */     return m_NaN;
/*      */   }
/*      */ 
/*      */   public void setNaN(String s)
/*      */   {
/* 1051 */     m_NaN = s;
/*      */   }
/*      */ 
/*      */   public String getInfinity()
/*      */   {
/* 1056 */     return m_infinity;
/*      */   }
/*      */ 
/*      */   public void setInfinity(String s)
/*      */   {
/* 1061 */     m_infinity = s;
/*      */   }
/*      */ 
/*      */   public char getZeroDigit()
/*      */   {
/* 1066 */     return m_zeroDigit;
/*      */   }
/*      */ 
/*      */   public void setZeroDigit(char c)
/*      */   {
/* 1071 */     m_zeroDigit = c;
/*      */   }
/*      */ 
/*      */   public char getDecimalSeparator()
/*      */   {
/* 1076 */     return m_decimalSeparator;
/*      */   }
/*      */ 
/*      */   public void setDecimalSeparator(char c)
/*      */   {
/* 1081 */     m_decimalSeparator = c;
/*      */   }
/*      */ 
/*      */   public char getGroupingSeparator()
/*      */   {
/* 1086 */     return m_groupingSeparator;
/*      */   }
/*      */ 
/*      */   public void setGroupingSeparator(char c)
/*      */   {
/* 1091 */     m_groupingSeparator = c;
/*      */   }
/*      */ 
/*      */   private void readObject(ObjectInputStream stream)
/*      */     throws IOException, ClassNotFoundException
/*      */   {
/* 1112 */     stream.defaultReadObject();
/*      */ 
/* 1116 */     if ((getMaximumIntegerDigits() <= 309) && (getMaximumFractionDigits() <= 340)) {
/*      */       return;
/*      */     }
/* 1119 */     throw new InvalidObjectException("!csNumberFormatTooManyDigits");
/*      */   }
/*      */ 
/*      */   private byte[] setDigits(double source, int maximumDigits)
/*      */   {
/* 1125 */     byte[] digits = null;
/*      */ 
/* 1127 */     if (source == 0.0D) source = 0.0D;
/*      */ 
/* 1131 */     String rep = Double.toString(source);
/*      */ 
/* 1133 */     int count = 0;
/* 1134 */     int numZeros = 0;
/*      */ 
/* 1136 */     boolean nonZeroDigitSeen = false;
/* 1137 */     for (int i = 0; i < rep.length(); ++i)
/*      */     {
/* 1139 */       char c = rep.charAt(i);
/* 1140 */       if (count >= 19)
/*      */         continue;
/* 1142 */       if (!nonZeroDigitSeen)
/*      */       {
/* 1144 */         nonZeroDigitSeen = c != '0';
/* 1145 */         if (nonZeroDigitSeen)
/*      */         {
/* 1147 */           digits = new byte[rep.length() - numZeros];
/*      */         }
/*      */         else
/*      */         {
/* 1151 */           ++numZeros;
/*      */         }
/*      */       }
/* 1154 */       if (!nonZeroDigitSeen) continue; digits[(count++)] = (byte)c;
/*      */     }
/*      */ 
/* 1160 */     digits = round(digits, maximumDigits);
/*      */ 
/* 1162 */     return digits;
/*      */   }
/*      */ 
/*      */   private byte[] setDigits(long source, int maximumDigits)
/*      */   {
/* 1182 */     byte[] digits = null;
/* 1183 */     int count = 0;
/*      */ 
/* 1185 */     if (source <= 0L)
/*      */     {
/* 1187 */       if (source == -9223372036854775808L)
/*      */       {
/* 1189 */         count = 19;
/* 1190 */         digits = new byte[19];
/*      */ 
/* 1192 */         System.arraycopy(LONG_MIN_REP, 0, digits, 0, count);
/*      */       }
/*      */       else
/*      */       {
/* 1196 */         count = 0;
/* 1197 */         digits = new byte[0];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1202 */       byte[] temp = new byte[19];
/*      */ 
/* 1206 */       int left = 19;
/* 1207 */       while (source > 0L)
/*      */       {
/* 1209 */         temp[(--left)] = (byte)(int)(48L + source % 10L);
/* 1210 */         source /= 10L;
/*      */       }
/* 1212 */       count = 19 - left;
/* 1213 */       digits = new byte[count];
/* 1214 */       System.arraycopy(temp, left, digits, 0, count);
/*      */     }
/*      */ 
/* 1217 */     if (maximumDigits > 0)
/*      */     {
/* 1219 */       digits = round(digits, maximumDigits);
/*      */     }
/*      */ 
/* 1222 */     return digits;
/*      */   }
/*      */ 
/*      */   private byte[] round(byte[] digits, int maximumDigits)
/*      */   {
/* 1235 */     if ((maximumDigits >= 0) && (maximumDigits < digits.length) && 
/* 1237 */       (shouldRoundUp(digits, maximumDigits)))
/*      */     {
/*      */       do
/*      */       {
/* 1243 */         --maximumDigits;
/* 1244 */         if (maximumDigits < 0)
/*      */         {
/* 1247 */           digits[0] = 49;
/*      */ 
/* 1249 */           maximumDigits = 0;
/* 1250 */           break;
/*      */         }
/*      */ 
/* 1253 */         ++maximumDigits;
/* 1254 */       }while (digits[maximumDigits] > 57);
/*      */     }
/*      */ 
/* 1259 */     return digits;
/*      */   }
/*      */ 
/*      */   private boolean shouldRoundUp(byte[] digits, int maximumDigits)
/*      */   {
/* 1277 */     if (maximumDigits < digits.length)
/*      */     {
/* 1279 */       if (digits[maximumDigits] > 53)
/*      */       {
/* 1281 */         return true;
/*      */       }
/* 1283 */       if (digits[maximumDigits] == 53)
/*      */       {
/* 1285 */         for (int i = maximumDigits + 1; i < digits.length; ++i) {
/* 1286 */           if (digits[i] != 48) {
/* 1287 */             return true;
/*      */           }
/*      */         }
/* 1290 */         return (maximumDigits > 0) && (digits[(maximumDigits - 1)] % 2 != 0);
/*      */       }
/*      */     }
/* 1293 */     return false;
/*      */   }
/*      */ 
/*      */   private int getDecimalAt(byte[] digits)
/*      */   {
/* 1298 */     for (int i = 0; i < digits.length; ++i)
/*      */     {
/* 1300 */       if (digits[i] == 46) return i;
/*      */     }
/* 1302 */     return digits.length;
/*      */   }
/*      */ 
/*      */   private double getDoubleValue(byte[] digits)
/*      */   {
/* 1307 */     int count = digits.length;
/*      */ 
/* 1309 */     if (count == 0) return 0.0D;
/* 1310 */     IdcStringBuilder temp = new IdcStringBuilder(count);
/* 1311 */     for (int i = 0; i < count; ++i) temp.append((char)digits[i]);
/*      */ 
/* 1313 */     return Double.valueOf(temp.toString()).doubleValue();
/*      */   }
/*      */ 
/*      */   private long getLongValue(byte[] digits)
/*      */   {
/* 1321 */     int count = digits.length;
/* 1322 */     int decimalAt = getDecimalAt(digits);
/*      */ 
/* 1324 */     if (count == 0) return 0L;
/*      */ 
/* 1329 */     if (isLongMIN_VALUE(digits)) return -9223372036854775808L;
/*      */ 
/* 1331 */     IdcStringBuilder temp = new IdcStringBuilder(count);
/* 1332 */     for (int i = 0; i < decimalAt; ++i)
/*      */     {
/* 1334 */       temp.append((char)digits[i]);
/*      */     }
/* 1336 */     return Long.parseLong(temp.toString());
/*      */   }
/*      */ 
/*      */   private boolean isLongMIN_VALUE(byte[] digits)
/*      */   {
/* 1341 */     int decimalAt = getDecimalAt(digits);
/* 1342 */     int count = digits.length;
/*      */ 
/* 1344 */     if ((decimalAt != count) || (count != 19)) return false;
/*      */ 
/* 1346 */     for (int i = 0; i < count; ++i)
/*      */     {
/* 1348 */       if (digits[i] != LONG_MIN_REP[i]) return false;
/*      */     }
/*      */ 
/* 1351 */     return true;
/*      */   }
/*      */ 
/*      */   private boolean fitsIntoLong(byte[] digits, boolean isPositive, boolean ignoreNegativeZero)
/*      */   {
/* 1361 */     int count = digits.length;
/* 1362 */     int decimalAt = getDecimalAt(digits);
/*      */ 
/* 1365 */     for (; (count > 0) && (digits[(count - 1)] == 48); --count);
/* 1367 */     if (count == 0)
/*      */     {
/* 1370 */       return (isPositive) || (ignoreNegativeZero);
/*      */     }
/*      */ 
/* 1373 */     if ((decimalAt < count) || (decimalAt > 19)) return false;
/*      */ 
/* 1375 */     if (decimalAt < 19) return true;
/*      */ 
/* 1380 */     for (int i = 0; i < count; ++i)
/*      */     {
/* 1382 */       byte dig = digits[i]; byte max = LONG_MIN_REP[i];
/* 1383 */       if (dig > max) return false;
/* 1384 */       if (dig < max) return true;
/*      */ 
/*      */     }
/*      */ 
/* 1389 */     if (count < decimalAt) return true;
/*      */ 
/* 1394 */     return !isPositive;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1399 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*   70 */     String s = Long.toString(-9223372036854775808L);
/*   71 */     LONG_MIN_REP = new byte[19];
/*   72 */     for (int i = 0; i < 19; ++i)
/*      */     {
/*   74 */       LONG_MIN_REP[i] = (byte)s.charAt(i + 1);
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcNumberFormat
 * JD-Core Version:    0.5.4
 */