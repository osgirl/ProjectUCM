/*     */ package intradoc.tools.utils;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.util.Calendar;
/*     */ import java.util.Properties;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class TextUtils
/*     */ {
/*     */   public static void appendNumericDateTimeTo(StringBuilder sb, Calendar cal, String dateSeparator, String dateTimeSeparator, String timeSeparator)
/*     */   {
/*  42 */     if (cal == null)
/*     */     {
/*  44 */       cal = Calendar.getInstance();
/*     */     }
/*  46 */     appendNumericDateTo(sb, cal, dateSeparator);
/*  47 */     if (dateTimeSeparator != null)
/*     */     {
/*  49 */       sb.append(dateTimeSeparator);
/*     */     }
/*  51 */     appendNumericTimeTo(sb, cal, timeSeparator);
/*     */   }
/*     */ 
/*     */   public static void appendNumericDateTo(StringBuilder sb, Calendar cal, String separator)
/*     */   {
/*  63 */     if (cal == null)
/*     */     {
/*  65 */       cal = Calendar.getInstance();
/*     */     }
/*  67 */     if (separator == null)
/*     */     {
/*  69 */       separator = "";
/*     */     }
/*  71 */     appendNumericCalendarFieldTo(sb, cal, 1);
/*  72 */     sb.append(separator);
/*  73 */     appendNumericCalendarFieldTo(sb, cal, 2);
/*  74 */     sb.append(separator);
/*  75 */     appendNumericCalendarFieldTo(sb, cal, 5);
/*     */   }
/*     */ 
/*     */   public static void appendNumericTimeTo(StringBuilder sb, Calendar cal, String separator)
/*     */   {
/*  87 */     if (cal == null)
/*     */     {
/*  89 */       cal = Calendar.getInstance();
/*     */     }
/*  91 */     if (separator == null)
/*     */     {
/*  93 */       separator = "";
/*     */     }
/*  95 */     appendNumericCalendarFieldTo(sb, cal, 11);
/*  96 */     sb.append(separator);
/*  97 */     appendNumericCalendarFieldTo(sb, cal, 12);
/*  98 */     sb.append(separator);
/*  99 */     appendNumericCalendarFieldTo(sb, cal, 13);
/*     */   }
/*     */ 
/*     */   public static void appendNumericCalendarFieldTo(StringBuilder sb, Calendar cal, int fieldNumber)
/*     */   {
/* 111 */     int intValue = cal.get(fieldNumber);
/*     */     int minDigits;
/* 114 */     switch (fieldNumber)
/*     */     {
/*     */     case 4:
/* 117 */       minDigits = 1;
/* 118 */       break;
/*     */     case 2:
/* 120 */       ++intValue;
/* 121 */       minDigits = 2;
/* 122 */       break;
/*     */     case 10:
/* 124 */       if (intValue == 0)
/*     */       {
/* 126 */         intValue = 12;
/*     */       }
/* 128 */       minDigits = 2;
/* 129 */       break;
/*     */     case 3:
/*     */     case 5:
/*     */     case 11:
/*     */     case 12:
/*     */     case 13:
/* 135 */       minDigits = 2;
/* 136 */       break;
/*     */     case 6:
/*     */     case 14:
/* 139 */       minDigits = 3;
/* 140 */       break;
/*     */     case 1:
/* 142 */       minDigits = 4;
/* 143 */       break;
/*     */     case 15:
/*     */     case 16:
/* 146 */       sb.append((intValue >= 0) ? '+' : '-');
/* 147 */       if (intValue >= 0)
/*     */       {
/* 149 */         sb.append('+');
/*     */       }
/*     */       else
/*     */       {
/* 153 */         sb.append('-');
/* 154 */         intValue = -intValue;
/*     */       }
/* 156 */       minDigits = 4;
/* 157 */       break;
/*     */     case 7:
/*     */     case 8:
/*     */     case 9:
/*     */     default:
/* 159 */       return;
/*     */     }
/*     */ 
/* 162 */     String stringValue = Integer.toString(intValue);
/* 163 */     int stringLength = stringValue.length();
/* 164 */     for (int s = stringLength; s < minDigits; ++s)
/*     */     {
/* 166 */       sb.append('0');
/*     */     }
/* 168 */     sb.append(stringValue);
/*     */   }
/*     */ 
/*     */   public static String substituteVariables(String string, Properties props, boolean shouldKeepUnknown)
/*     */   {
/* 184 */     int length = string.length();
/* 185 */     StringBuilder sb = new StringBuilder(length);
/* 186 */     int nextIndex = 0;
/* 187 */     while ((firstIndex = string.indexOf('$', nextIndex)) >= 0)
/*     */     {
/*     */       int firstIndex;
/* 189 */       sb.append(string.substring(nextIndex, firstIndex));
/* 190 */       nextIndex = firstIndex + 1;
/* 191 */       if (nextIndex >= length) {
/*     */         break;
/*     */       }
/*     */ 
/* 195 */       char ch = string.charAt(nextIndex);
/*     */       int pastVarIndex;
/*     */       int firstVarIndex;
/*     */       int pastVarIndex;
/* 197 */       if (ch != '{')
/*     */       {
/* 199 */         int firstVarIndex = nextIndex;
/* 200 */         while (((nextIndex < length) && (ch == '_')) || (Character.isLetterOrDigit(ch)))
/*     */         {
/* 202 */           ch = string.charAt(++nextIndex);
/*     */         }
/* 204 */         pastVarIndex = nextIndex;
/*     */       }
/*     */       else
/*     */       {
/* 208 */         firstVarIndex = ++nextIndex;
/* 209 */         if (nextIndex >= length) {
/*     */           break;
/*     */         }
/*     */ 
/* 213 */         pastVarIndex = string.indexOf('}', firstVarIndex);
/* 214 */         if (pastVarIndex < 0)
/*     */         {
/* 216 */           pastVarIndex = length;
/* 217 */           nextIndex = length;
/*     */         }
/*     */         else
/*     */         {
/* 221 */           nextIndex = pastVarIndex + 1;
/*     */         }
/*     */       }
/* 224 */       String defaultValue = null;
/* 225 */       int colonIndex = string.indexOf(':', firstVarIndex);
/* 226 */       if ((colonIndex >= 0) && (colonIndex < pastVarIndex))
/*     */       {
/* 228 */         defaultValue = string.substring(colonIndex + 1, pastVarIndex);
/* 229 */         pastVarIndex = colonIndex;
/*     */       }
/* 231 */       String varName = string.substring(firstVarIndex, pastVarIndex);
/* 232 */       String varValue = props.getProperty(varName);
/* 233 */       if (varValue != null)
/*     */       {
/* 235 */         sb.append(varValue);
/*     */       }
/* 237 */       else if (defaultValue != null)
/*     */       {
/* 239 */         sb.append(defaultValue);
/*     */       }
/* 241 */       else if (shouldKeepUnknown)
/*     */       {
/* 243 */         sb.append("${");
/* 244 */         sb.append(varName);
/* 245 */         sb.append('}');
/*     */       }
/*     */     }
/* 248 */     sb.append(string.substring(nextIndex));
/* 249 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public static PatternFilter addWildcardArrayToPatternFilter(PatternFilter filter, String[] wildcardStrings)
/*     */   {
/* 265 */     if (filter == null)
/*     */     {
/* 267 */       filter = new PatternFilter();
/*     */     }
/* 269 */     for (int f = 0; f < wildcardStrings.length; ++f)
/*     */     {
/* 271 */       boolean isExclusive = false;
/* 272 */       String filterString = wildcardStrings[f];
/* 273 */       if (filterString.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 277 */       if (filterString.startsWith("+"))
/*     */       {
/* 279 */         filterString = filterString.substring(1);
/*     */       }
/* 281 */       else if ((filterString.startsWith("-")) || (filterString.startsWith("!")))
/*     */       {
/* 283 */         filterString = filterString.substring(1);
/* 284 */         isExclusive = true;
/*     */       }
/*     */ 
/* 287 */       String regexString = wildcardPathToRegex(filterString);
/* 288 */       Pattern pattern = Pattern.compile(regexString);
/* 289 */       filter.add(isExclusive, pattern);
/*     */     }
/* 291 */     return filter;
/*     */   }
/*     */ 
/*     */   public static PatternFilter addWildcardsToPatternFilter(PatternFilter filter, String wildcardStrings)
/*     */   {
/* 303 */     String[] strings = wildcardStrings.split(",");
/* 304 */     return addWildcardArrayToPatternFilter(filter, strings);
/*     */   }
/*     */ 
/*     */   public static PatternFilter addRegexArrayToPatternFilter(PatternFilter filter, String[] regexStrings)
/*     */   {
/* 318 */     if (filter == null)
/*     */     {
/* 320 */       filter = new PatternFilter();
/*     */     }
/* 322 */     for (int f = 0; f < regexStrings.length; ++f)
/*     */     {
/* 324 */       boolean isExclusive = false;
/* 325 */       String filterString = regexStrings[f];
/* 326 */       if (filterString.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 330 */       if (filterString.startsWith("+"))
/*     */       {
/* 332 */         filterString = filterString.substring(1);
/*     */       }
/* 334 */       else if ((filterString.startsWith("-")) || (filterString.startsWith("!")))
/*     */       {
/* 336 */         filterString = filterString.substring(1);
/* 337 */         isExclusive = true;
/*     */       }
/* 339 */       Pattern pattern = Pattern.compile(filterString);
/* 340 */       filter.add(isExclusive, pattern);
/*     */     }
/* 342 */     return filter;
/*     */   }
/*     */ 
/*     */   public static PatternFilter createPatternFilterFromWildcards(String wildcardFilters)
/*     */   {
/* 354 */     String[] filters = wildcardFilters.split(",");
/* 355 */     return addWildcardArrayToPatternFilter(null, filters);
/*     */   }
/*     */ 
/*     */   public static String wildcardPathToRegex(String wildcardString)
/*     */   {
/* 371 */     int length = wildcardString.length();
/* 372 */     IdcStringBuilder str = new IdcStringBuilder(length);
/* 373 */     int c = 0;
/* 374 */     while (c < length)
/*     */     {
/* 376 */       char ch = wildcardString.charAt(c++);
/* 377 */       switch (ch)
/*     */       {
/*     */       case '*':
/* 380 */         if ((c < length) && (wildcardString.charAt(c) == '*'))
/*     */         {
/* 382 */           ++c;
/* 383 */           str.append(".*");
/*     */         }
/*     */         else
/*     */         {
/* 387 */           str.append("[^/]*");
/*     */         }
/* 389 */         break;
/*     */       case '.':
/* 391 */         str.append("\\.");
/* 392 */         break;
/*     */       case '?':
/* 394 */         str.append('.');
/* 395 */         break;
/*     */       case '[':
/* 397 */         str.append('[');
/* 398 */         int end = wildcardString.indexOf(']', c + 1);
/* 399 */         if (end >= 0)
/*     */         {
/* 401 */           char chNot = wildcardString.charAt(c);
/* 402 */           if (chNot == '!')
/*     */           {
/* 404 */             str.append('^');
/* 405 */             ++c;
/*     */           }
/* 407 */           str.append(wildcardString.substring(c, end + 1));
/* 408 */           c = end + 1;
/* 409 */         }break;
/*     */       case '$':
/*     */       case '(':
/*     */       case ')':
/*     */       case '+':
/*     */       case '^':
/*     */       case '{':
/*     */       case '}':
/* 418 */         str.append('\\');
/*     */       default:
/* 421 */         str.append(ch);
/*     */       }
/*     */     }
/*     */ 
/* 425 */     return str.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 432 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98496 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.utils.TextUtils
 * JD-Core Version:    0.5.4
 */