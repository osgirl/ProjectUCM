/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.CharConversionException;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStreamReader;
/*      */ import java.io.StringReader;
/*      */ import java.io.UnsupportedEncodingException;
/*      */ import java.text.ParseException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Collection;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Properties;
/*      */ import java.util.Random;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class StringUtils
/*      */ {
/*   42 */   public static Properties m_xmlStringMap = null;
/*   43 */   public static final String[][] XML_ESCAPES = { { "amp", "&" }, { "gt", ">" }, { "lt", "<" }, { "quot", "\"" }, { "apos", "'" } };
/*      */ 
/*   53 */   public static boolean m_isDefaultFullXmlEncodeMode = false;
/*      */   public static final int F_USE_DEFAULTS = 0;
/*      */   public static final int F_FOUND_FFFD = 1;
/*      */   public static final int F_EXCEPTION_THROWN = 2;
/*      */   public static final int F_TRACE = 1;
/*      */   public static final int F_ERROR = 2;
/*      */   public static final int F_WATCH_FOR_FFFD = 4;
/*      */   public static final int F_STOP_ON_FFFD = 8;
/*      */   public static final int F_KEEP_GOING = 16;
/*      */   public static final int F_TRIM = 32;
/*      */   public static final int F_SKIP_EMPTY = 64;
/*      */   public static final int F_ALWAYS_COPY = 128;
/*      */   public static final long F_ESCAPE_NL = 1024L;
/*      */   public static final long F_ESCAPE_CR = 8192L;
/*      */   public static final long F_ESCAPE_TAB = 512L;
/*      */   public static final long F_ESCAPE_WHITESPACE = 9728L;
/*      */   public static final long F_ESCAPE_SINGLE_QUOTE = 128L;
/*      */   public static final long F_ESCAPE_DOUBLE_QUOTE = 4L;
/*      */   public static final long F_ESCAPE_QUOTES = 132L;
/*      */   public static final long F_ESCAPE_LITERALS = 9860L;
/*      */   public static final long F_ESCAPE_UNPRINTABLES = 2097152L;
/*      */   public static final long F_ESCAPE_LESS_THAN = 268435456L;
/*      */   public static final long F_ESCAPE_GREATER_THAN = 1073741824L;
/*      */   public static final long F_ESCAPE_ANGLE_BRACKETS = 1342177280L;
/*      */   public static final long F_ESCAPE_FOR_JAVASCRIPT = 1344284292L;
/*      */   public static final int F_ADD_QUOTES = 256;
/*      */   public static final int F_IGNORE_CASE = 1;
/*      */   public static final int F_STARTS_WITH = 2;
/*      */   public static final int F_MAKE_LOWER_CASE = 4;
/*      */   public static final int F_PREFIX_MANDATORY = 4;
/*      */   public static final int F_SUFFIX_MANDATORY = 8;
/*      */   public static final int F_REPLACE_ILLEGAL_CHARS = 1;
/*      */   public static final int F_ENCODE_ILLEGAL_CHARS = 2;
/*      */   public static final int F_OMIT_ILLEGAL_CHARS = 4;
/*      */   public static final String QUERY_UNRESERVED_CHARS = "_.-";
/*      */   public static final int F_APPEND_NEWLINE = 1;
/*      */   public static final int F_LAZY_LIST_CREATION = 1;
/*      */   public static final int F_KEEP_SEPARATORS = 2;
/* 2138 */   public static final int[] m_cp850TranslationRange = { 128, 255 };
/* 2139 */   public static final char[] m_toCP850TranslationMap = { 'º', 'Í', 'É', '»', 'È', '¼', 'Ì', '¹', 'Ë', 'Ê', 'Î', 'ß', 'Ü', 'Û', 'þ', 'ò', '³', 'Ä', 'Ú', '¿', 'À', 'Ù', 'Ã', '´', 'Â', 'Á', 'Å', '°', '±', '²', 'Õ', '', 'ÿ', '­', '½', '', 'Ï', '¾', 'Ý', 'õ', 'ù', '¸', '¦', '®', 'ª', 'ð', '©', 'î', 'ø', 'ñ', 'ý', 'ü', 'ï', 'æ', 'ô', 'ú', '÷', 'û', '§', '¯', '¬', '«', 'ó', '¨', '·', 'µ', '¶', 'Ç', '', '', '', '', 'Ô', '', 'Ò', 'Ó', 'Þ', 'Ö', '×', 'Ø', 'Ñ', '¥', 'ã', 'à', 'â', 'å', '', '', '', 'ë', 'é', 'ê', '', 'í', 'è', 'á', '', ' ', '', 'Æ', '', '', '', '', '', '', '', '', '', '¡', '', '', 'Ð', '¤', '', '¢', '', 'ä', '', 'ö', '', '', '£', '', '', 'ì', 'ç', '' };
/*      */ 
/* 2151 */   public static final char[] m_fromCP850TranslationMap = { 'Ç', 'ü', 'é', 'â', 'ä', 'à', 'å', 'ç', 'ê', 'ë', 'è', 'ï', 'î', 'ì', 'Ä', 'Å', 'É', 'æ', 'Æ', 'ô', 'ö', 'ò', 'û', 'ù', 'ÿ', 'Ö', 'Ü', 'ø', '£', 'Ø', '×', '', 'á', 'í', 'ó', 'ú', 'ñ', 'Ñ', 'ª', 'º', '¿', '®', '¬', '½', '¼', '¡', '«', '»', '', '', '', '', '', 'Á', 'Â', 'À', '©', '', '', '', '', '¢', '¥', '', '', '', '', '', '', '', 'ã', 'Ã', '', '', '', '', '', '', '', '¤', 'ð', 'Ð', 'Ê', 'Ë', 'È', '', 'Í', 'Î', 'Ï', '', '', '', '', '¦', 'Ì', '', 'Ó', 'ß', 'Ô', 'Ò', 'õ', 'Õ', 'µ', 'þ', 'Þ', 'Ú', 'Û', 'Ù', 'ý', 'Ý', '¯', '´', '­', '±', '', '¾', '¶', '§', '÷', '¸', '°', '¨', '·', '¹', '³', '²', '', ' ' };
/*      */   static Random s_random;
/*      */ 
/*      */   public static List appendListFromSequence(List strArray, CharSequence sequence, int start, int length, char sep, char esc, int flags)
/*      */   {
/*  155 */     if ((sequence == null) || (sequence.length() == 0) || (length <= 0))
/*      */     {
/*  157 */       return strArray;
/*      */     }
/*  159 */     if (strArray == null)
/*      */     {
/*  161 */       strArray = new ArrayList();
/*      */     }
/*  163 */     IdcCharSequence curCharStr = null;
/*  164 */     String curStrStr = null;
/*  165 */     if (sequence instanceof IdcCharSequence)
/*      */     {
/*  167 */       curCharStr = (IdcCharSequence)sequence;
/*      */     }
/*  169 */     else if ((sequence instanceof String) && (sequence.length() - length < 500))
/*      */     {
/*  171 */       curStrStr = (String)sequence;
/*      */     }
/*      */     else
/*      */     {
/*  175 */       IdcStringBuilder subCharStr = new IdcStringBuilder();
/*  176 */       subCharStr.append(sequence, start, length);
/*  177 */       curCharStr = subCharStr;
/*  178 */       length -= start;
/*  179 */       start = 0;
/*      */     }
/*      */ 
/*  183 */     boolean isDoTrim = (flags & 0x20) > 0;
/*  184 */     int index = start;
/*  185 */     String sepStr = null;
/*      */ 
/*  187 */     IdcStringBuilder[] refBuilder = null;
/*  188 */     int endIndex = start + length;
/*      */     while (true)
/*      */     {
/*  191 */       String addStr = null;
/*  192 */       if (start < endIndex)
/*      */       {
/*  194 */         if (curCharStr != null)
/*      */         {
/*  196 */           if (sepStr == null)
/*      */           {
/*  198 */             sepStr = Character.toString(sep);
/*      */           }
/*  200 */           index = curCharStr.indexOf(start, endIndex, sepStr, 0, 1, false);
/*      */         }
/*      */         else
/*      */         {
/*  204 */           index = curStrStr.indexOf(sep, start);
/*      */         }
/*  206 */         if ((index >= endIndex) || (index < 0))
/*      */         {
/*  208 */           index = endIndex;
/*      */         }
/*  210 */         int curSubStrLen = index - start;
/*      */ 
/*  212 */         if (curSubStrLen > 0)
/*      */         {
/*  221 */           if (refBuilder == null)
/*      */           {
/*  223 */             refBuilder = new IdcStringBuilder[] { null };
/*      */           }
/*  225 */           else if (refBuilder[0] != null)
/*      */           {
/*  227 */             refBuilder[0].setLength(0);
/*      */           }
/*  229 */           if (sep != esc)
/*      */           {
/*  231 */             removeEscapeCharsEx(refBuilder, sequence, start, curSubStrLen, sep, esc, 128);
/*      */           }
/*      */           else
/*      */           {
/*  236 */             if (refBuilder[0] == null)
/*      */             {
/*  238 */               refBuilder[0] = new IdcStringBuilder(curSubStrLen + 10);
/*      */             }
/*  240 */             refBuilder[0].append(sequence, start, curSubStrLen);
/*      */           }
/*      */ 
/*  243 */           if (isDoTrim)
/*      */           {
/*  245 */             addStr = refBuilder[0].getTrimmedString(0, refBuilder[0].length());
/*      */           }
/*      */           else
/*      */           {
/*  249 */             addStr = refBuilder[0].toStringNoRelease();
/*      */           }
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  255 */         index = start;
/*      */       }
/*  257 */       if (addStr == null)
/*      */       {
/*  259 */         addStr = "";
/*      */       }
/*  261 */       if ((addStr.length() > 0) || ((flags & 0x40) == 0))
/*      */       {
/*  263 */         strArray.add(addStr);
/*      */       }
/*      */ 
/*  266 */       if (index < 0) break; if (index >= endIndex) {
/*      */         break;
/*      */       }
/*      */ 
/*  270 */       start = index + 1;
/*      */     }
/*  272 */     if ((refBuilder != null) && (refBuilder[0] != null))
/*      */     {
/*  274 */       refBuilder[0].releaseBuffers();
/*      */     }
/*  276 */     return strArray;
/*      */   }
/*      */ 
/*      */   public static List appendListFromSequenceSimple(List list, CharSequence sequence)
/*      */   {
/*  288 */     if (list == null)
/*      */     {
/*  290 */       list = new ArrayList();
/*      */     }
/*  292 */     if (sequence != null)
/*      */     {
/*  294 */       appendListFromSequence(list, sequence, 0, sequence.length(), ',', '^', 32);
/*      */     }
/*  296 */     return list;
/*      */   }
/*      */ 
/*      */   public static List makeListFromSequenceSimple(CharSequence str)
/*      */   {
/*  307 */     List list = appendListFromSequenceSimple(null, str);
/*  308 */     return list;
/*      */   }
/*      */ 
/*      */   public static List makeListFromSequence(CharSequence str, char sep, char esc, int flags)
/*      */   {
/*  323 */     ArrayList list = new ArrayList();
/*  324 */     if (str != null)
/*      */     {
/*  326 */       appendListFromSequence(list, str, 0, str.length(), sep, esc, flags);
/*      */     }
/*  328 */     return list;
/*      */   }
/*      */ 
/*      */   public static String[] makeStringArrayFromSequence(CharSequence str)
/*      */   {
/*  339 */     return makeStringArrayFromSequenceEx(str, ',', '^', 32);
/*      */   }
/*      */ 
/*      */   public static String[] makeStringArrayFromSequenceEx(CharSequence str, char sep, char esc, int flags)
/*      */   {
/*  354 */     ArrayList list = new ArrayList();
/*  355 */     if (str != null)
/*      */     {
/*  357 */       appendListFromSequence(list, str, 0, str.length(), sep, esc, flags);
/*      */     }
/*  359 */     String[] ret = new String[list.size()];
/*  360 */     list.toArray(ret);
/*  361 */     return ret;
/*      */   }
/*      */ 
/*      */   public static Vector parseArrayEx(String str, char sep, char esc, boolean isDoTrim)
/*      */   {
/*  373 */     Vector v = new IdcVector();
/*  374 */     if (str != null)
/*      */     {
/*  376 */       appendListFromSequence(v, str, 0, str.length(), sep, esc, (isDoTrim) ? 32 : 0);
/*      */     }
/*      */ 
/*  379 */     return v;
/*      */   }
/*      */ 
/*      */   public static Vector parseArray(String str, char sep, char esc)
/*      */   {
/*  391 */     return parseArrayEx(str, sep, esc, false);
/*      */   }
/*      */ 
/*      */   public static boolean removeEscapeCharsEx(IdcStringBuilder[] builderRef, CharSequence str, int start, int length, char sep, char esc, int flags)
/*      */   {
/*  417 */     IdcStringBuilder builder = builderRef[0];
/*  418 */     boolean hasEscapes = false;
/*  419 */     int stop = start + length;
/*  420 */     char universalEscapeChar = (sep == '#') ? '\\' : '#';
/*  421 */     for (int i = start; i < stop; ++i)
/*      */     {
/*  423 */       char ch = str.charAt(i);
/*  424 */       char replaceCh = ch;
/*  425 */       boolean isReplace = false;
/*  426 */       boolean isSkip = false;
/*      */ 
/*  428 */       if (ch == universalEscapeChar)
/*      */       {
/*  430 */         if (i < stop - 1)
/*      */         {
/*  432 */           char nextChar = str.charAt(i + 1);
/*  433 */           if ((nextChar == universalEscapeChar) || (nextChar == esc))
/*      */           {
/*  435 */             isReplace = true;
/*  436 */             isSkip = true;
/*  437 */             replaceCh = nextChar;
/*      */           }
/*      */         }
/*      */       }
/*  441 */       else if (ch == esc)
/*      */       {
/*  443 */         isReplace = true;
/*  444 */         replaceCh = sep;
/*      */       }
/*      */ 
/*  447 */       if ((!isReplace) && (!hasEscapes) && ((flags & 0x80) <= 0))
/*      */         continue;
/*  449 */       if (!hasEscapes)
/*      */       {
/*  451 */         hasEscapes = true;
/*  452 */         if (builder == null)
/*      */         {
/*      */            tmp176_173 = new IdcStringBuilder(i - start + 10); builder = tmp176_173; builderRef[0] = tmp176_173;
/*      */         }
/*  456 */         if (str instanceof String)
/*      */         {
/*  458 */           builder.append((String)str, start, i - start);
/*      */         }
/*  460 */         else if (str instanceof IdcStringBuilder)
/*      */         {
/*  462 */           builder.append((IdcStringBuilder)str, start, i - start);
/*      */         }
/*      */         else
/*      */         {
/*  466 */           if (SystemUtils.m_verbose)
/*      */           {
/*  468 */             Report.debug("system", "StringUtils.removeEscapeCharsEx() found an un-optimizable object of type " + str.getClass().getName(), null);
/*      */           }
/*      */ 
/*  473 */           char[] tmpArray = new char[i - start];
/*  474 */           int k = 0; for (int j = start; j < i; ++k)
/*      */           {
/*  476 */             tmpArray[k] = str.charAt(j);
/*      */ 
/*  474 */             ++j;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  480 */       builder.append(replaceCh);
/*  481 */       if (!isSkip)
/*      */         continue;
/*  483 */       ++i;
/*      */     }
/*      */ 
/*  487 */     return hasEscapes;
/*      */   }
/*      */ 
/*      */   public static void removeEscapeChars(IdcStringBuilder builder, CharSequence str, int start, int length, char sep, char esc)
/*      */   {
/*  503 */     IdcStringBuilder[] builderRef = { builder };
/*  504 */     removeEscapeCharsEx(builderRef, str, start, length, sep, esc, 128);
/*      */   }
/*      */ 
/*      */   public static CharSequence removeEscapeChars(CharSequence str, int start, int length, char sep, char esc)
/*      */   {
/*  520 */     IdcStringBuilder[] builder = new IdcStringBuilder[1];
/*  521 */     if (removeEscapeCharsEx(builder, str, start, length, sep, esc, 128))
/*      */     {
/*  524 */       return builder[0].toString();
/*      */     }
/*  526 */     return str;
/*      */   }
/*      */ 
/*      */   public static String removeEscapeChars(String str, char sep, char esc)
/*      */   {
/*  539 */     IdcStringBuilder[] builder = new IdcStringBuilder[1];
/*  540 */     if (removeEscapeCharsEx(builder, str, 0, str.length(), sep, esc, 128))
/*      */     {
/*  543 */       return builder[0].toString();
/*      */     }
/*  545 */     return str;
/*      */   }
/*      */ 
/*      */   public static String createString(List strArray, char sep, char esc)
/*      */   {
/*  556 */     return createStringEx(strArray, sep, esc, false);
/*      */   }
/*      */ 
/*      */   public static String createString(Vector strArray, char sep, char esc)
/*      */   {
/*  564 */     return createStringEx(strArray, sep, esc, false);
/*      */   }
/*      */ 
/*      */   public static String createStringSimple(List strArray)
/*      */   {
/*  579 */     return createStringEx(strArray, ',', '^', false);
/*      */   }
/*      */ 
/*      */   public static String createStringFromArray(String[] strArray)
/*      */   {
/*  593 */     List l = Arrays.asList(strArray);
/*  594 */     return createStringEx(l, ',', '^', false);
/*      */   }
/*      */ 
/*      */   public static String createStringRemoveEmpty(List strArray, char sep, char esc)
/*      */   {
/*  603 */     return createStringEx(strArray, sep, esc, true);
/*      */   }
/*      */ 
/*      */   public static String createStringRemoveEmpty(Vector strArray, char sep, char esc)
/*      */   {
/*  608 */     return createStringEx(strArray, sep, esc, true);
/*      */   }
/*      */ 
/*      */   public static String createStringEx(List strArray, char sep, char esc, boolean skipEmpty)
/*      */   {
/*  622 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  623 */     appendStringFromList(builder, strArray, sep, esc, (skipEmpty) ? 64 : 0);
/*      */ 
/*  625 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public static String createStringEx(Vector strArray, char sep, char esc, boolean skipEmpty)
/*      */   {
/*  630 */     IdcStringBuilder builder = new IdcStringBuilder();
/*  631 */     appendStringFromList(builder, strArray, sep, esc, (skipEmpty) ? 64 : 0);
/*      */ 
/*  633 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public static IdcStringBuilder appendStringFromList(IdcStringBuilder strBuf, List strArray, char sep, char esc, int flags)
/*      */   {
/*  728 */     boolean isFirstElt = true;
/*  729 */     int size = strArray.size();
/*  730 */     boolean skipEmpty = (flags & 0x40) > 0;
/*  731 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  733 */       String str = (String)strArray.get(i);
/*      */ 
/*  735 */       if ((skipEmpty) && (str.length() == 0))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  742 */       if (sep != esc)
/*      */       {
/*  746 */         str = addEscapeChars(str, sep, esc);
/*      */       }
/*  748 */       if (!isFirstElt)
/*      */       {
/*  750 */         strBuf.append(sep);
/*      */       }
/*      */       else
/*      */       {
/*  754 */         isFirstElt = false;
/*      */       }
/*  756 */       strBuf.append(str);
/*      */     }
/*      */ 
/*  759 */     return strBuf;
/*      */   }
/*      */ 
/*      */   public static String addEscapeChars(String str, char sep, char esc)
/*      */   {
/*  773 */     int len = str.length();
/*  774 */     IdcStringBuilder convStr = null;
/*  775 */     int charsAdded = 0;
/*      */ 
/*  779 */     char universalEscapeChar = (sep == '#') ? '\\' : '#';
/*      */ 
/*  781 */     for (int j = 0; j < len; ++j)
/*      */     {
/*  783 */       char ch = str.charAt(j);
/*  784 */       if ((ch != sep) && (ch != esc) && (ch != universalEscapeChar))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  789 */       if ((ch == universalEscapeChar) && (j < len - 1))
/*      */       {
/*  791 */         char nextCh = str.charAt(j + 1);
/*  792 */         if ((nextCh != universalEscapeChar) && (nextCh != esc) && (nextCh != sep)) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  798 */       if (ch == sep)
/*      */       {
/*  800 */         if (convStr == null)
/*      */         {
/*  802 */           convStr = new IdcStringBuilder(str);
/*      */         }
/*  804 */         convStr.setCharAt(j + charsAdded, esc);
/*      */       }
/*      */       else
/*      */       {
/*  808 */         if (convStr == null)
/*      */         {
/*  810 */           convStr = new IdcStringBuilder(len + 1);
/*  811 */           convStr.append(str);
/*      */         }
/*  813 */         convStr.insert(j + charsAdded, universalEscapeChar);
/*  814 */         ++charsAdded;
/*      */       }
/*      */     }
/*  817 */     if (convStr != null)
/*      */     {
/*  819 */       str = convStr.toString();
/*      */     }
/*  821 */     return str;
/*      */   }
/*      */ 
/*      */   public static String replaceCRLF(String value)
/*      */   {
/*  827 */     IdcStringBuilder buff = new IdcStringBuilder(value);
/*  828 */     int length = buff.length();
/*  829 */     for (int i = 0; i < length; ++i)
/*      */     {
/*  831 */       char ch = buff.charAt(i);
/*  832 */       if ((ch != '\r') && (ch != '\n'))
/*      */         continue;
/*  834 */       buff.setCharAt(i, ' ');
/*      */     }
/*      */ 
/*  838 */     return buff.toString();
/*      */   }
/*      */ 
/*      */   public static void setIsDefaultFullXmlEncodeMode(boolean isDefaultFullXmlEncodeMode)
/*      */   {
/*  849 */     m_isDefaultFullXmlEncodeMode = isDefaultFullXmlEncodeMode;
/*      */   }
/*      */ 
/*      */   public static boolean getIsDefaultFullXmlEncodeMode()
/*      */   {
/*  860 */     return m_isDefaultFullXmlEncodeMode;
/*      */   }
/*      */ 
/*      */   public static String urlEncode(String str)
/*      */   {
/*  876 */     return urlEncodeEx(str, true);
/*      */   }
/*      */ 
/*      */   public static String urlEncodeEx(String str, boolean usePluses)
/*      */   {
/*  893 */     return encodeUrlStyle(str, '%', usePluses);
/*      */   }
/*      */ 
/*      */   public static String urlEncodeEx(String str, boolean usePluses, String encodingMode, String encoding)
/*      */   {
/*  909 */     return encodeUrlStyle(str, '%', usePluses, encodingMode, encoding);
/*      */   }
/*      */ 
/*      */   public static String encodeUrlStyle(String str, char escapeChar, boolean usePluses)
/*      */   {
/*  924 */     return encodeUrlStyle(str, escapeChar, usePluses, null, null);
/*      */   }
/*      */ 
/*      */   public static String encodeUrlStyle(String str, char escapeChar, boolean usePluses, String encodingMode, String encoding)
/*      */   {
/*  950 */     boolean fullEncoding = false;
/*      */ 
/*  952 */     if (encoding != null)
/*      */     {
/*  954 */       if (encodingMode == null)
/*      */       {
/*  956 */         fullEncoding = m_isDefaultFullXmlEncodeMode;
/*      */       }
/*      */       else
/*      */       {
/*  960 */         fullEncoding = encodingMode.toLowerCase().endsWith("full");
/*      */       }
/*      */     }
/*  963 */     else if ((encodingMode != null) && (Report.m_verbose))
/*      */     {
/*  965 */       Report.trace("system", "Encoding mode specified without encoding parameter", new Throwable());
/*      */     }
/*  967 */     int len = str.length();
/*  968 */     IdcStringBuilder strBuf = new IdcStringBuilder();
/*  969 */     strBuf.ensureCapacity(len + 10);
/*      */ 
/*  971 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  973 */       char ch = str.charAt(i);
/*  974 */       if ((ch == ' ') && (usePluses))
/*      */       {
/*  976 */         strBuf.append('+');
/*      */       }
/*  979 */       else if ((Validation.isAlphaNum(ch)) || ("_.-".indexOf(ch) >= 0) || ((ch >= '') && (!fullEncoding) && (ch != '’')))
/*      */       {
/*  981 */         strBuf.append(ch);
/*      */       }
/*  986 */       else if ((ch > '') && (encoding != null))
/*      */       {
/*      */         try
/*      */         {
/*      */           byte[] b;
/*  991 */           if ((i + 1 < len) && (Character.isSurrogatePair(ch, str.charAt(i + 1))))
/*      */           {
/*  993 */             byte[] b = str.substring(i, i + 2).getBytes(encoding);
/*  994 */             ++i;
/*      */           }
/*      */           else
/*      */           {
/*  998 */             b = new Character(ch).toString().getBytes(encoding);
/*      */           }
/*      */ 
/* 1001 */           for (int j = 0; j < b.length; ++j)
/*      */           {
/* 1003 */             strBuf.append(escapeChar);
/* 1004 */             if ((b[j] & 0xFF) < 16)
/*      */             {
/* 1006 */               strBuf.append('0');
/*      */             }
/* 1008 */             strBuf.append(Integer.toHexString(b[j] & 0xFF));
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1013 */           e.printStackTrace();
/*      */ 
/* 1015 */           strBuf.append(ch);
/*      */         }
/*      */       }
/* 1018 */       else if (ch < '')
/*      */       {
/* 1020 */         strBuf.append(escapeChar);
/* 1021 */         String hexStr = Integer.toHexString(ch);
/* 1022 */         for (int j = 0; j < 2 - hexStr.length(); ++j)
/*      */         {
/* 1024 */           strBuf.append('0');
/*      */         }
/* 1026 */         strBuf.append(hexStr);
/*      */       }
/*      */       else
/*      */       {
/* 1031 */         strBuf.append(ch);
/*      */       }
/*      */     }
/*      */ 
/* 1035 */     return strBuf.toString();
/*      */   }
/*      */ 
/*      */   public static String urlEscape7Bit(String input, char escapeChar, String encoding)
/*      */   {
/* 1051 */     return urlEscape7BitEx(input, escapeChar, encoding, true);
/*      */   }
/*      */ 
/*      */   public static String urlEscape7BitEx(String input, char escapeChar, String encoding, boolean escapeQuotes)
/*      */   {
/* 1056 */     int len = input.length();
/* 1057 */     IdcStringBuilder output = new IdcStringBuilder(len);
/* 1058 */     if ((encoding == null) || (encoding.length() == 0)) {
/* 1059 */       encoding = "UTF8";
/*      */     }
/* 1061 */     byte[] byteBuf = null;
/*      */     try
/*      */     {
/* 1064 */       byteBuf = input.getBytes(encoding);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1068 */       Report.trace("encoding", null, e);
/* 1069 */       byteBuf = input.getBytes();
/*      */     }
/*      */ 
/* 1072 */     int bufLen = byteBuf.length;
/* 1073 */     boolean isMultiByte = false;
/*      */ 
/* 1076 */     if (bufLen > len)
/*      */     {
/* 1078 */       char ch = encoding.charAt(0);
/*      */ 
/* 1083 */       if ((ch != 'u') && (ch != 'U'))
/*      */       {
/* 1085 */         isMultiByte = true;
/*      */       }
/*      */     }
/* 1088 */     boolean encodeNextByte = false;
/* 1089 */     for (int i = 0; i < bufLen; ++i)
/*      */     {
/* 1093 */       byte b = byteBuf[i];
/* 1094 */       boolean highBitSet = (b & 0x80) != 0;
/* 1095 */       if ((highBitSet) || (encodeNextByte) || ((escapeQuotes) && (((b == 39) || (b == 34)))))
/*      */       {
/* 1097 */         output.append(escapeChar);
/* 1098 */         NumberUtils.appendHexByte(output, b);
/*      */       }
/*      */       else
/*      */       {
/* 1102 */         output.append((char)b);
/*      */       }
/*      */ 
/* 1107 */       encodeNextByte = (isMultiByte) && (highBitSet) && (!encodeNextByte);
/*      */     }
/* 1109 */     return output.toString();
/*      */   }
/*      */ 
/*      */   public static String encodeHttpHeaderStyle(String str, boolean usePluses)
/*      */   {
/* 1124 */     int len = str.length();
/* 1125 */     char escapeChar = '%';
/* 1126 */     IdcStringBuilder strBuf = new IdcStringBuilder(len + 10);
/*      */ 
/* 1128 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1130 */       char ch = str.charAt(i);
/* 1131 */       if ((ch == ' ') && (usePluses))
/*      */       {
/* 1133 */         if (!usePluses)
/*      */           continue;
/* 1135 */         strBuf.append('+');
/*      */       }
/* 1138 */       else if ((ch < '') && (ch >= ' ') && (ch != escapeChar) && (ch != '+'))
/*      */       {
/* 1140 */         strBuf.append(ch);
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/* 1146 */           String tmpString = "" + ch;
/* 1147 */           byte[] bytes = tmpString.getBytes("UTF8");
/* 1148 */           for (int j = 0; j < bytes.length; ++j)
/*      */           {
/* 1150 */             strBuf.append(escapeChar);
/* 1151 */             NumberUtils.appendHexByte(strBuf, bytes[j]);
/*      */           }
/*      */         }
/*      */         catch (UnsupportedEncodingException ignore)
/*      */         {
/* 1156 */           str = "UTF-8 not supported by this JVM.";
/* 1157 */           Report.trace("system", str, ignore);
/* 1158 */           return str;
/*      */         }
/*      */       }
/*      */     }
/* 1162 */     return strBuf.toString();
/*      */   }
/*      */ 
/*      */   public static String decodeHttpHeaderStyle(String byteString)
/*      */     throws CharConversionException
/*      */   {
/* 1182 */     return decodeUrlEncodedBytes(byteString, "UTF8");
/*      */   }
/*      */ 
/*      */   public static String decodeUrlEncodedString(String str, String encoding)
/*      */     throws CharConversionException
/*      */   {
/* 1205 */     boolean hasNon7BitChars = false;
/* 1206 */     boolean hasEscapeChars = false;
/* 1207 */     for (int i = 0; i < str.length(); ++i)
/*      */     {
/* 1209 */       char ch = str.charAt(i);
/* 1210 */       if (ch >= '')
/*      */       {
/* 1212 */         hasNon7BitChars = true;
/*      */       } else {
/* 1214 */         if ((ch != '+') && (ch != '%'))
/*      */           continue;
/* 1216 */         hasEscapeChars = true;
/*      */       }
/*      */     }
/* 1219 */     if ((!hasNon7BitChars) && (!hasEscapeChars))
/*      */     {
/* 1221 */       return str;
/*      */     }
/* 1223 */     if (encoding == null)
/*      */     {
/* 1225 */       encoding = "UTF8";
/*      */     }
/* 1227 */     String result = null;
/* 1228 */     if (!hasNon7BitChars)
/*      */     {
/* 1231 */       result = decodeUrlEncodedBytes(str, encoding);
/*      */     }
/*      */     else
/*      */     {
/* 1235 */       result = decodeUrlEncodedNonBytes(str, encoding);
/*      */     }
/* 1237 */     return result;
/*      */   }
/*      */ 
/*      */   public static String decodeUrlEncodedBytes(String byteString, String encoding)
/*      */     throws CharConversionException
/*      */   {
/* 1251 */     byte[] bytes = new byte[byteString.length()];
/* 1252 */     boolean is8Bit = false;
/* 1253 */     boolean isEscaped = false;
/* 1254 */     for (int i = 0; i < bytes.length; ++i)
/*      */     {
/* 1256 */       bytes[i] = (byte)(byteString.charAt(i) % 'ÿ');
/* 1257 */       switch (bytes[i])
/*      */       {
/* 1266 */       case 43:
/* 1260 */         bytes[i] = 32;
/* 1261 */         break;
/*      */       case 37:
/* 1263 */         isEscaped = true;
/* 1264 */         break;
/*      */       default:
/* 1266 */         if (bytes[i] <= 127) continue; is8Bit = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1271 */     if ((!is8Bit) && (!isEscaped))
/*      */     {
/* 1273 */       return byteString;
/*      */     }
/*      */     int j;
/* 1277 */     for (int i = j = 0; i < bytes.length; ++j)
/*      */     {
/* 1279 */       byte b = bytes[i];
/* 1280 */       if (b == 37)
/*      */       {
/* 1282 */         if (i + 2 >= bytes.length)
/*      */         {
/* 1284 */           throw new CharConversionException("Bad URL");
/*      */         }
/* 1286 */         byte b1 = bytes[(++i)];
/* 1287 */         byte b2 = bytes[(++i)];
/* 1288 */         byte v = NumberUtils.getHexValue(b1);
/* 1289 */         if (v < 0)
/*      */         {
/* 1291 */           CharConversionException e = new CharConversionException("Bad URL");
/* 1292 */           Report.message(null, "system", 6000, null, bytes, 0, bytes.length, e, null);
/* 1293 */           throw e;
/*      */         }
/* 1295 */         bytes[j] = (byte)(v * 16);
/* 1296 */         v = NumberUtils.getHexValue(b2);
/* 1297 */         if (v < 0)
/*      */         {
/* 1299 */           CharConversionException e = new CharConversionException("Bad URL");
/* 1300 */           Report.message(null, "system", 6000, null, bytes, 0, bytes.length, e, null);
/* 1301 */           throw e;
/*      */         }
/*      */         int tmp273_271 = j;
/*      */         byte[] tmp273_270 = bytes; tmp273_270[tmp273_271] = (byte)(tmp273_270[tmp273_271] + v);
/*      */       }
/*      */       else
/*      */       {
/* 1307 */         bytes[j] = b;
/*      */       }
/* 1277 */       ++i;
/*      */     }
/*      */ 
/*      */     String str;
/*      */     try
/*      */     {
/* 1314 */       if ((!is8Bit) && 
/* 1316 */         (encoding == null))
/*      */       {
/* 1318 */         encoding = "UTF8";
/*      */       }
/*      */ 
/* 1321 */       str = getString(bytes, 0, j, encoding);
/*      */     }
/*      */     catch (UnsupportedEncodingException ignore)
/*      */     {
/* 1325 */       if (encoding == null)
/*      */       {
/* 1327 */         encoding = "(null encoding)";
/*      */       }
/* 1329 */       str = encoding + " is not supported by this JVM.";
/* 1330 */       Report.trace("system", str, null);
/* 1331 */       Report.message(null, "system", 6000, null, bytes, 0, j, ignore, null);
/*      */     }
/*      */ 
/* 1334 */     return str;
/*      */   }
/*      */ 
/*      */   public static String decodeUrlEncodedNonBytes(String str, String encoding)
/*      */     throws CharConversionException
/*      */   {
/* 1348 */     char[] newBuf = new char[str.length()];
/* 1349 */     str.getChars(0, newBuf.length, newBuf, 0);
/* 1350 */     int j = 0;
/* 1351 */     for (int i = 0; i < newBuf.length; ++j)
/*      */     {
/* 1353 */       char ch = newBuf[i];
/* 1354 */       if (ch == '%')
/*      */       {
/* 1356 */         if (i + 2 >= newBuf.length)
/*      */         {
/* 1358 */           Report.trace("system", str, null);
/* 1359 */           throw new CharConversionException("Bad URL");
/*      */         }
/* 1361 */         char b1 = newBuf[(++i)];
/* 1362 */         char b2 = newBuf[(++i)];
/* 1363 */         if ((b1 > '') || (b2 >= ''))
/*      */         {
/* 1365 */           Report.trace("system", str, null);
/* 1366 */           throw new CharConversionException("Bad URL");
/*      */         }
/* 1368 */         byte v = NumberUtils.getHexValue((byte)b1);
/* 1369 */         if (v < 0)
/*      */         {
/* 1371 */           Report.trace("system", str, null);
/* 1372 */           throw new CharConversionException("Bad URL");
/*      */         }
/* 1374 */         newBuf[j] = (char)(v * 16);
/* 1375 */         v = NumberUtils.getHexValue((byte)b2);
/* 1376 */         if (v < 0)
/*      */         {
/* 1378 */           Report.trace("system", str, null);
/* 1379 */           throw new CharConversionException("Bad URL");
/*      */         }
/*      */         int tmp189_188 = j;
/*      */         char[] tmp189_187 = newBuf; tmp189_187[tmp189_188] = (char)(tmp189_187[tmp189_188] + v);
/*      */       }
/* 1383 */       else if (ch == '+')
/*      */       {
/* 1385 */         newBuf[j] = ' ';
/*      */       }
/*      */       else
/*      */       {
/* 1389 */         newBuf[j] = ch;
/*      */       }
/* 1351 */       ++i;
/*      */     }
/*      */ 
/* 1392 */     return new String(newBuf, 0, j);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static byte getHexValue(byte c)
/*      */   {
/* 1401 */     SystemUtils.reportDeprecatedUsage("getHexValue(byte) called.");
/* 1402 */     return NumberUtils.getHexValue(c);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static StringBuffer appendHexString(byte b, StringBuffer buf)
/*      */   {
/* 1412 */     SystemUtils.reportDeprecatedUsage("appendHexString(byte, StringBuffer) called.");
/*      */ 
/* 1414 */     int val = b & 0xFF;
/* 1415 */     int h = (byte)(val / 16);
/* 1416 */     int l = (byte)(val % 16);
/* 1417 */     if (h >= 10)
/*      */     {
/* 1419 */       buf.append((char)(h - 10 + 65));
/*      */     }
/*      */     else
/*      */     {
/* 1423 */       buf.append((char)(h + 48));
/*      */     }
/* 1425 */     if (l >= 10)
/*      */     {
/* 1427 */       buf.append((char)(l - 10 + 65));
/*      */     }
/*      */     else
/*      */     {
/* 1431 */       buf.append((char)(l + 48));
/*      */     }
/* 1433 */     return buf;
/*      */   }
/*      */ 
/*      */   public static String encodeLiteralStringEscapeSequence(String str)
/*      */   {
/* 1450 */     int len = str.length();
/* 1451 */     IdcStringBuilder convStr = new IdcStringBuilder();
/* 1452 */     convStr.ensureCapacity(len + 8);
/* 1453 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1455 */       char ch = str.charAt(i);
/* 1456 */       switch (ch)
/*      */       {
/*      */       case '\\':
/* 1459 */         convStr.append("\\\\");
/* 1460 */         break;
/*      */       case '"':
/* 1462 */         convStr.append("\\\"");
/* 1463 */         break;
/*      */       case '\'':
/* 1465 */         convStr.append("\\'");
/* 1466 */         break;
/*      */       case '\t':
/* 1468 */         convStr.append("\\t");
/* 1469 */         break;
/*      */       case '\n':
/* 1471 */         convStr.append("\\n");
/* 1472 */         break;
/*      */       case '\r':
/* 1474 */         convStr.append("\\r");
/* 1475 */         break;
/*      */       default:
/* 1477 */         convStr.append(ch);
/*      */       }
/*      */     }
/*      */ 
/* 1481 */     return convStr.toString();
/*      */   }
/*      */ 
/*      */   public static void appendEscapedString(IdcStringBuilder app, String str, long flags)
/*      */   {
/* 1486 */     int length = str.length();
/* 1487 */     char[] chars = new char[length];
/* 1488 */     str.getChars(0, length, chars, 0);
/* 1489 */     appendEscapedChars(app, chars, 0, length, flags);
/*      */   }
/*      */ 
/*      */   public static void appendEscapedChars(IdcStringBuilder app, char[] chars, int offset, int length, long flags)
/*      */   {
/* 1503 */     char[] HEX_DIGITS = NumberUtils.HEX_DIGITS;
/* 1504 */     char[] appChars = app.m_charArray;
/* 1505 */     int index = app.m_length; int capacity = app.m_capacity;
/*      */ 
/* 1507 */     boolean fEscapeNL = (flags & 0x400) != 0L;
/* 1508 */     boolean fEscapeCR = (flags & 0x2000) != 0L;
/* 1509 */     boolean fEscapeTab = (flags & 0x200) != 0L;
/* 1510 */     boolean fEscapeSingleQuote = (flags & 0x80) != 0L;
/* 1511 */     boolean fEscapeDoubleQuote = (flags & 0x4) != 0L;
/* 1512 */     boolean fEscapeUnprintables = (flags & 0x200000) != 0L;
/*      */ 
/* 1514 */     boolean fEscapeBackslash = (fEscapeNL) || (fEscapeCR) || (fEscapeTab) || (fEscapeSingleQuote) || (fEscapeDoubleQuote) || (fEscapeUnprintables);
/*      */ 
/* 1517 */     while (length-- > 0)
/*      */     {
/* 1519 */       if (index + length + 8 > capacity)
/*      */       {
/* 1521 */         app.m_length = index;
/* 1522 */         app.ensureCapacity(index + length + 8);
/* 1523 */         appChars = app.m_charArray;
/* 1524 */         capacity = app.m_capacity;
/*      */       }
/* 1526 */       char ch = chars[(offset++)];
/* 1527 */       if (fEscapeBackslash)
/*      */       {
/* 1529 */         if (ch < ' ')
/*      */         {
/* 1531 */           appChars[(index++)] = '\\';
/* 1532 */           if ((ch == '\n') && (fEscapeNL))
/*      */           {
/* 1534 */             appChars[(index++)] = 'n';
/*      */           }
/* 1536 */           if ((ch == '\r') && (fEscapeCR))
/*      */           {
/* 1538 */             appChars[(index++)] = 'r';
/*      */           }
/* 1540 */           if ((ch == '\t') && (fEscapeTab))
/*      */           {
/* 1542 */             appChars[(index++)] = 't';
/*      */           }
/* 1544 */           if (fEscapeUnprintables)
/*      */           {
/* 1546 */             appChars[(index++)] = 'u';
/* 1547 */             appChars[(index++)] = '0';
/* 1548 */             appChars[(index++)] = '0';
/* 1549 */             appChars[(index++)] = (char)(48 + (ch >> '\004'));
/* 1550 */             appChars[(index++)] = HEX_DIGITS[(ch & 0xF)];
/*      */           }
/*      */ 
/* 1554 */           appChars[(index - 1)] = ch;
/*      */         }
/*      */ 
/* 1558 */         if ((ch == '\\') || ((ch == '\'') && (fEscapeSingleQuote)) || ((ch == '"') && (fEscapeDoubleQuote)))
/*      */         {
/* 1560 */           appChars[(index++)] = '\\';
/*      */         }
/* 1562 */         else if ((ch >= '') && (fEscapeUnprintables))
/*      */         {
/* 1564 */           appChars[(index++)] = '\\';
/* 1565 */           appChars[(index++)] = 'u';
/* 1566 */           appChars[(index++)] = HEX_DIGITS[(ch >> '\f' & 0xF)];
/* 1567 */           appChars[(index++)] = HEX_DIGITS[(ch >> '\b' & 0xF)];
/* 1568 */           appChars[(index++)] = HEX_DIGITS[(ch >> '\004' & 0xF)];
/* 1569 */           appChars[(index++)] = HEX_DIGITS[(ch & 0xF)];
/*      */         }
/*      */       }
/*      */ 
/* 1573 */       appChars[(index++)] = ch;
/*      */     }
/* 1575 */     app.m_length = index;
/*      */   }
/*      */ 
/*      */   public static String decodeLiteralStringEscapeSequence(String str)
/*      */   {
/* 1586 */     int len = str.length();
/* 1587 */     IdcStringBuilder decodeStr = null;
/* 1588 */     boolean translateChar = false;
/*      */ 
/* 1590 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1592 */       char ch = str.charAt(i);
/* 1593 */       if (translateChar)
/*      */       {
/* 1595 */         switch (ch)
/*      */         {
/*      */         case 't':
/* 1598 */           decodeStr.append("\t");
/* 1599 */           break;
/*      */         case 'n':
/* 1601 */           decodeStr.append("\n");
/* 1602 */           break;
/*      */         case 'r':
/* 1604 */           decodeStr.append("\r");
/* 1605 */           break;
/*      */         default:
/* 1607 */           decodeStr.append(ch);
/*      */         }
/*      */ 
/* 1610 */         translateChar = false;
/*      */       }
/* 1612 */       else if (ch == '\\')
/*      */       {
/* 1614 */         if (decodeStr == null)
/*      */         {
/* 1616 */           decodeStr = new IdcStringBuilder(str.substring(0, i));
/*      */         }
/* 1618 */         translateChar = true;
/*      */       }
/*      */       else
/*      */       {
/* 1622 */         if (decodeStr == null)
/*      */           continue;
/* 1624 */         decodeStr.append(ch);
/*      */       }
/*      */     }
/*      */ 
/* 1628 */     if (decodeStr != null)
/*      */     {
/* 1630 */       return decodeStr.toString();
/*      */     }
/* 1632 */     return str;
/*      */   }
/*      */ 
/*      */   public static List makeListFromEscapedString(String str)
/*      */     throws ParseException
/*      */   {
/* 1646 */     int len = str.length();
/* 1647 */     char[] source = new char[len];
/* 1648 */     char[] target = new char[len];
/* 1649 */     List array = new ArrayList();
/* 1650 */     char quote = '\000';
/* 1651 */     boolean notEmpty = false;
/* 1652 */     int ti = 0;
/*      */ 
/* 1654 */     str.getChars(0, len, source, 0);
/* 1655 */     for (int si = 0; si < len; ++si)
/*      */     {
/* 1657 */       char c = source[si];
/* 1658 */       if ('\\' == c)
/*      */       {
/* 1660 */         notEmpty = true;
/* 1661 */         if (++si >= len)
/*      */         {
/* 1663 */           String illegalCharMsg = LocaleUtils.encodeMessage("syIllegalEndChar", null, "\\");
/* 1664 */           String unableToParseMsg = LocaleUtils.encodeMessage("syUnableToParse", illegalCharMsg, str);
/* 1665 */           throw new ParseException(unableToParseMsg, len);
/*      */         }
/* 1667 */         c = source[si];
/* 1668 */         switch (c)
/*      */         {
/*      */         case 'n':
/* 1671 */           c = '\n';
/* 1672 */           break;
/*      */         case 'r':
/* 1674 */           c = '\r';
/* 1675 */           break;
/*      */         case 't':
/* 1677 */           c = '\t';
/*      */         }
/*      */ 
/* 1680 */         target[(ti++)] = c;
/*      */       }
/* 1684 */       else if ('\000' != quote)
/*      */       {
/* 1686 */         if (c != quote)
/*      */         {
/* 1688 */           target[(ti++)] = c;
/*      */         }
/*      */         else
/*      */         {
/* 1692 */           quote = '\000';
/*      */         }
/*      */ 
/*      */       }
/* 1696 */       else if (('\'' == c) || ('"' == c))
/*      */       {
/* 1698 */         notEmpty = true;
/* 1699 */         quote = c;
/*      */       }
/* 1702 */       else if (!Character.isWhitespace(c))
/*      */       {
/* 1704 */         notEmpty = true;
/* 1705 */         target[(ti++)] = source[si];
/*      */       }
/*      */       else
/*      */       {
/* 1709 */         if (notEmpty)
/*      */         {
/* 1711 */           String element = new String(target, 0, ti);
/* 1712 */           array.add(element);
/* 1713 */           notEmpty = false;
/*      */         }
/* 1715 */         ti = 0;
/*      */       }
/*      */     }
/* 1717 */     if ('\000' != quote)
/*      */     {
/* 1719 */       String msg = LocaleUtils.encodeMessage("syQuoteNotTerminated", null, String.valueOf(quote), String.valueOf(len), str);
/*      */ 
/* 1721 */       throw new ParseException(msg, len);
/*      */     }
/* 1723 */     if (notEmpty)
/*      */     {
/* 1725 */       String element = new String(target, 0, ti);
/* 1726 */       array.add(element);
/*      */     }
/* 1728 */     return array;
/*      */   }
/*      */ 
/*      */   public static void checkXmlDecodeInit()
/*      */   {
/* 1733 */     if (m_xmlStringMap != null)
/*      */       return;
/* 1735 */     m_xmlStringMap = new Properties();
/* 1736 */     int num = XML_ESCAPES.length;
/* 1737 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1739 */       m_xmlStringMap.put(XML_ESCAPES[i][0], XML_ESCAPES[i][1]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String encodeXmlEscapeSequence(String input)
/*      */   {
/* 1747 */     return encodeXmlEscapeSequence(input, null);
/*      */   }
/*      */ 
/*      */   public static String encodeXmlEscapeSequence(String input, String encodingMode)
/*      */   {
/* 1752 */     return encodeXmlEscapeSequenceWithFlags(input, encodingMode, 2);
/*      */   }
/*      */ 
/*      */   public static String encodeXmlEscapeSequenceWithFlags(String input, String encodingMode, int flags)
/*      */   {
/* 1759 */     if ((input == null) || (input.length() == 0))
/*      */     {
/* 1761 */       return input;
/*      */     }
/* 1763 */     boolean useFullEncoding = false;
/* 1764 */     if ((encodingMode != null) && (encodingMode.toLowerCase().endsWith("full")))
/*      */     {
/* 1766 */       useFullEncoding = true;
/*      */     }
/* 1768 */     int len = input.length();
/* 1769 */     char[] buf = new char[len];
/* 1770 */     input.getChars(0, len, buf, 0);
/* 1771 */     IdcStringBuilder output = null;
/* 1772 */     for (int i = 0; i < buf.length; ++i)
/*      */     {
/* 1774 */       char ch = buf[i];
/* 1775 */       String appendStr = null;
/* 1776 */       switch (ch)
/*      */       {
/*      */       case '&':
/* 1779 */         appendStr = "&amp;";
/* 1780 */         break;
/*      */       case '"':
/* 1782 */         appendStr = "&quot;";
/* 1783 */         break;
/*      */       case '<':
/* 1785 */         appendStr = "&lt;";
/* 1786 */         break;
/*      */       case '>':
/* 1788 */         appendStr = "&gt;";
/* 1789 */         break;
/*      */       case '\'':
/* 1791 */         appendStr = "&#39;";
/*      */       }
/*      */ 
/* 1794 */       if (appendStr != null)
/*      */       {
/* 1796 */         if (output == null)
/*      */         {
/* 1798 */           output = new IdcStringBuilder(buf.length + 5);
/* 1799 */           output.append(buf, 0, i);
/*      */         }
/* 1801 */         label386: output.append(appendStr);
/*      */       } else {
/* 1803 */         if ((!useFullEncoding) || (ch <= '')) { if (ch >= ' ') break label386; if (((((ch != '\n') ? 1 : 0) & ((ch != '\r') ? 1 : 0)) == 0) || (ch == '\t')) {
/*      */             break label386;
/*      */           } }
/*      */ 
/* 1807 */         if (output == null)
/*      */         {
/* 1809 */           output = new IdcStringBuilder();
/* 1810 */           output.append(buf, 0, i);
/*      */         }
/* 1812 */         boolean encodeChar = useFullEncoding;
/* 1813 */         boolean appendChar = true;
/* 1814 */         if (ch < ' ')
/*      */         {
/* 1817 */           if ((flags & 0x4) != 0)
/*      */           {
/* 1820 */             appendChar = false;
/*      */           }
/* 1822 */           else if ((flags & 0x1) != 0)
/*      */           {
/* 1824 */             encodeChar = false;
/* 1825 */             ch = 65533;
/*      */           }
/* 1827 */           else if ((flags & 0x2) != 0)
/*      */           {
/* 1829 */             encodeChar = true;
/*      */           }
/*      */         }
/* 1832 */         if (appendChar)
/*      */         {
/* 1834 */           if (encodeChar)
/*      */           {
/* 1836 */             output.append("&#");
/* 1837 */             NumberUtils.appendLong(output, ch);
/* 1838 */             output.append(";");
/*      */           }
/*      */           else
/*      */           {
/* 1842 */             output.append(ch);
/*      */           }
/*      */         }
/* 1845 */         continue;
/* 1846 */         if (output == null)
/*      */           continue;
/* 1848 */         output.append(ch);
/*      */       }
/*      */     }
/* 1851 */     if (output != null)
/*      */     {
/* 1853 */       return output.toString();
/*      */     }
/* 1855 */     return input;
/*      */   }
/*      */ 
/*      */   public static String decodeXmlEscapeSequence(char[] array, int offset, int len)
/*      */   {
/* 1864 */     IdcStringBuilder result = null;
/* 1865 */     boolean insideEscape = false;
/* 1866 */     int startEscape = -1;
/* 1867 */     int end = offset + len;
/*      */ 
/* 1869 */     for (int i = offset; i < end; ++i)
/*      */     {
/* 1871 */       char tch = array[i];
/* 1872 */       if (insideEscape)
/*      */       {
/* 1875 */         if (tch != ';')
/*      */           continue;
/* 1877 */         int escapeLen = i - startEscape;
/* 1878 */         char ch = decodeXmlEscapeCharacter(array, startEscape, escapeLen);
/* 1879 */         result.append(ch);
/* 1880 */         insideEscape = false;
/*      */       }
/* 1885 */       else if (tch == '&')
/*      */       {
/* 1887 */         if (result == null)
/*      */         {
/* 1889 */           result = new IdcStringBuilder();
/* 1890 */           result.append(array, offset, i - offset);
/*      */         }
/* 1892 */         startEscape = i + 1;
/* 1893 */         insideEscape = true;
/*      */       }
/*      */       else
/*      */       {
/* 1897 */         if (result == null)
/*      */           continue;
/* 1899 */         result.append(tch);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1905 */     if (result != null)
/*      */     {
/* 1907 */       return result.toString();
/*      */     }
/*      */ 
/* 1910 */     return new String(array, offset, len);
/*      */   }
/*      */ 
/*      */   public static char decodeXmlEscapeCharacter(char[] array, int offset, int len)
/*      */   {
/* 1916 */     if (len <= 0)
/*      */     {
/* 1918 */       return '?';
/*      */     }
/*      */ 
/* 1922 */     boolean isNumber = false;
/* 1923 */     boolean isHexNumber = false;
/* 1924 */     char firstChar = array[offset];
/* 1925 */     if (firstChar == '#')
/*      */     {
/* 1927 */       isNumber = true;
/* 1928 */       ++offset;
/* 1929 */       --len;
/*      */ 
/* 1931 */       if (len > 0)
/*      */       {
/* 1933 */         char secondChar = array[offset];
/* 1934 */         if (secondChar == 'x')
/*      */         {
/* 1936 */           isHexNumber = true;
/* 1937 */           ++offset;
/* 1938 */           --len;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1950 */     char ch = '?';
/* 1951 */     if (isNumber)
/*      */     {
/* 1953 */       long[] val = { 0L };
/* 1954 */       int end = offset + len;
/* 1955 */       if (isHexNumber)
/*      */       {
/* 1957 */         NumberUtils.parseHexValue(array, offset, end, val);
/*      */       }
/*      */       else
/*      */       {
/* 1961 */         NumberUtils.parseDecimalValue(array, offset, end, val);
/*      */       }
/* 1963 */       if ((val[0] <= 65535L) && (val[0] > 0L))
/*      */       {
/* 1965 */         ch = (char)(int)val[0];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1970 */       String temp = new String(array, offset, len);
/*      */ 
/* 1973 */       checkXmlDecodeInit();
/*      */ 
/* 1976 */       String val = m_xmlStringMap.getProperty(temp);
/* 1977 */       if (val != null)
/*      */       {
/* 1979 */         ch = val.charAt(0);
/*      */       }
/*      */     }
/* 1982 */     return ch;
/*      */   }
/*      */ 
/*      */   public static String createErrorStringForBrowser(String msg)
/*      */   {
/* 1994 */     IdcStringBuilder msgBuf = new IdcStringBuilder(msg.length() + 32);
/*      */ 
/* 1996 */     for (int i = 0; i < msg.length(); ++i)
/*      */     {
/* 1998 */       char ch = msg.charAt(i);
/* 1999 */       if ((ch == '<') || (ch == '>'))
/*      */       {
/* 2001 */         String str = (ch == '<') ? "#60;" : "#62;";
/*      */ 
/* 2003 */         msgBuf.append('&');
/* 2004 */         msgBuf.append(str);
/*      */       }
/* 2006 */       else if (ch == '\n')
/*      */       {
/* 2008 */         msgBuf.append("<br>");
/*      */       }
/*      */       else
/*      */       {
/* 2012 */         msgBuf.append(ch);
/*      */       }
/*      */     }
/* 2015 */     return msgBuf.toString();
/*      */   }
/*      */ 
/*      */   public static String createQuotableString(String str)
/*      */   {
/* 2026 */     IdcAppendable result = appendQuotableString(null, str, 0);
/* 2027 */     if (result == null)
/*      */     {
/* 2029 */       return str;
/*      */     }
/* 2031 */     return result.toString();
/*      */   }
/*      */ 
/*      */   public static IdcAppendable appendQuotableString(IdcAppendable outBuf, String str, int flags)
/*      */   {
/* 2037 */     if ((flags & 0x100) != 0)
/*      */     {
/* 2039 */       if (str == null)
/*      */       {
/* 2041 */         if (outBuf != null)
/*      */         {
/* 2043 */           outBuf.append("null");
/*      */         }
/* 2045 */         return outBuf;
/*      */       }
/* 2047 */       if (outBuf == null)
/*      */       {
/* 2049 */         outBuf = new IdcStringBuilder(str.length() + 4);
/*      */       }
/* 2051 */       outBuf.append('\'');
/*      */     }
/* 2053 */     int len = str.length();
/* 2054 */     int startCopyIndex = 0;
/*      */ 
/* 2056 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2058 */       if (str.charAt(i) != '\'')
/*      */         continue;
/* 2060 */       if (outBuf == null)
/*      */       {
/* 2062 */         outBuf = new IdcStringBuilder(str.substring(0, i));
/*      */       }
/* 2066 */       else if (i > startCopyIndex)
/*      */       {
/* 2068 */         outBuf.append(str.substring(startCopyIndex, i));
/*      */       }
/*      */ 
/* 2071 */       outBuf.append("''");
/* 2072 */       startCopyIndex = i + 1;
/*      */     }
/*      */ 
/* 2076 */     if (outBuf != null)
/*      */     {
/* 2078 */       if (startCopyIndex < len)
/*      */       {
/* 2080 */         outBuf.append(str.substring(startCopyIndex));
/*      */       }
/* 2082 */       if ((flags & 0x100) != 0)
/*      */       {
/* 2084 */         outBuf.append('\'');
/*      */       }
/*      */     }
/* 2087 */     return outBuf;
/*      */   }
/*      */ 
/*      */   public static String removeWhitespace(String str)
/*      */   {
/* 2095 */     IdcStringBuilder outBuf = null;
/* 2096 */     int len = str.length();
/* 2097 */     int startCopyIndex = 0;
/*      */ 
/* 2099 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 2101 */       if (!Character.isWhitespace(str.charAt(i)))
/*      */         continue;
/* 2103 */       if (outBuf == null)
/*      */       {
/* 2105 */         outBuf = new IdcStringBuilder(str.substring(0, i));
/*      */       }
/* 2109 */       else if (i > startCopyIndex)
/*      */       {
/* 2111 */         outBuf.append(str.substring(startCopyIndex, i));
/*      */       }
/*      */ 
/* 2114 */       startCopyIndex = i + 1;
/*      */     }
/*      */ 
/* 2118 */     if (outBuf != null)
/*      */     {
/* 2120 */       if (startCopyIndex < len)
/*      */       {
/* 2122 */         outBuf.append(str.substring(startCopyIndex));
/*      */       }
/* 2124 */       return outBuf.toString();
/*      */     }
/* 2126 */     return str;
/*      */   }
/*      */ 
/*      */   public static void convertToCP850(char[] tmp, int start, int len)
/*      */   {
/* 2169 */     if (tmp == null)
/*      */     {
/* 2171 */       return;
/*      */     }
/*      */ 
/* 2174 */     int startOffset = m_cp850TranslationRange[0];
/* 2175 */     int endOffset = m_cp850TranslationRange[1];
/*      */ 
/* 2177 */     int end = start + len;
/* 2178 */     for (int i = start; i < end; ++i)
/*      */     {
/* 2180 */       char ch = tmp[i];
/* 2181 */       if ((ch < startOffset) || (ch > endOffset))
/*      */         continue;
/* 2183 */       int index = ch - startOffset;
/* 2184 */       tmp[i] = m_toCP850TranslationMap[index];
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void convertFromCP850(char[] tmp, int start, int len)
/*      */   {
/* 2192 */     if (tmp == null)
/*      */     {
/* 2194 */       return;
/*      */     }
/*      */ 
/* 2197 */     int startOffset = m_cp850TranslationRange[0];
/* 2198 */     int endOffset = m_cp850TranslationRange[1];
/*      */ 
/* 2200 */     int end = start + len;
/* 2201 */     for (int i = start; i < end; ++i)
/*      */     {
/* 2203 */       char ch = tmp[i];
/* 2204 */       if ((ch < startOffset) || (ch > endOffset))
/*      */         continue;
/* 2206 */       int index = ch - startOffset;
/* 2207 */       tmp[i] = m_fromCP850TranslationMap[index];
/*      */     }
/*      */   }
/*      */ 
/*      */   public static byte[] convertCharsToBytesSafe(char[] chars, int start, int length, byte[] outbuf, int[] outlen, int flags)
/*      */   {
/* 2229 */     if (outbuf == null)
/*      */     {
/* 2231 */       outbuf = new byte[(int)(length * 1.25D)];
/*      */     }
/*      */ 
/* 2234 */     int j = 0;
/* 2235 */     for (int i = start; i < length; ++i)
/*      */     {
/* 2237 */       char c = chars[i];
/* 2238 */       if ((((c < ' ') || (c > '~') || (c == '#'))) && (c != '\t') && (c != '\n') && (c != '\r'))
/*      */       {
/* 2241 */         if (j + 5 >= outbuf.length)
/*      */         {
/* 2243 */           byte[] tmp = outbuf;
/* 2244 */           outbuf = new byte[outbuf.length * 2];
/* 2245 */           System.arraycopy(tmp, 0, outbuf, 0, tmp.length);
/*      */         }
/* 2247 */         outbuf[(j++)] = 35;
/* 2248 */         outbuf[(j++)] = (byte)NumberUtils.HEX_DIGITS[(c >>> '\f')];
/* 2249 */         outbuf[(j++)] = (byte)NumberUtils.HEX_DIGITS[(c >>> '\b' & 0xF)];
/* 2250 */         outbuf[(j++)] = (byte)NumberUtils.HEX_DIGITS[(c >>> '\004' & 0xF)];
/* 2251 */         outbuf[(j++)] = (byte)NumberUtils.HEX_DIGITS[(c & 0xF)];
/*      */       }
/*      */       else
/*      */       {
/* 2255 */         if (j + 1 >= outbuf.length)
/*      */         {
/* 2257 */           byte[] tmp = outbuf;
/* 2258 */           outbuf = new byte[outbuf.length * 2];
/* 2259 */           System.arraycopy(tmp, 0, outbuf, 0, tmp.length);
/*      */         }
/* 2261 */         outbuf[(j++)] = (byte)c;
/*      */       }
/*      */     }
/* 2264 */     if ((flags & 0x1) != 0)
/*      */     {
/* 2266 */       if (j + 1 >= outbuf.length)
/*      */       {
/* 2268 */         byte[] tmp = outbuf;
/* 2269 */         outbuf = new byte[outbuf.length * 2];
/* 2270 */         System.arraycopy(tmp, 0, outbuf, 0, tmp.length);
/*      */       }
/* 2272 */       outbuf[(j++)] = 10;
/*      */     }
/* 2274 */     if (outlen != null)
/*      */     {
/* 2276 */       outlen[0] = j;
/*      */     }
/* 2278 */     return outbuf;
/*      */   }
/*      */ 
/*      */   public static String findString(String[][] map, String key, int lookupIndex, int returnIndex)
/*      */   {
/* 2289 */     if (key == null)
/*      */     {
/* 2291 */       return null;
/*      */     }
/* 2293 */     for (int i = 0; i < map.length; ++i)
/*      */     {
/* 2295 */       if (map[i][lookupIndex].equals(key))
/*      */       {
/* 2297 */         return map[i][returnIndex];
/*      */       }
/*      */     }
/* 2300 */     return null;
/*      */   }
/*      */ 
/*      */   public static String getPresentationString(String[][] map, String internalStr)
/*      */   {
/* 2305 */     return findString(map, internalStr, 0, 1);
/*      */   }
/*      */ 
/*      */   public static String getInternalString(String[][] map, String presentationStr)
/*      */   {
/* 2310 */     return findString(map, presentationStr, 1, 0);
/*      */   }
/*      */ 
/*      */   public static boolean convertToBool(String str, boolean defVal)
/*      */   {
/* 2326 */     if (str == null)
/*      */     {
/* 2328 */       return defVal;
/*      */     }
/* 2330 */     str = str.trim();
/* 2331 */     if (str.length() == 0)
/*      */     {
/* 2333 */       return defVal;
/*      */     }
/* 2335 */     char ch = Character.toUpperCase(str.charAt(0));
/* 2336 */     if (defVal)
/*      */     {
/* 2338 */       return (ch != '0') && (ch != 'F') && (ch != 'N');
/*      */     }
/*      */ 
/* 2343 */     return (ch == '1') || (ch == 'T') || (ch == 'Y') || (str.equals("-1"));
/*      */   }
/*      */ 
/*      */   public static Properties convertStringArrayToProperties(String[][] str)
/*      */   {
/* 2352 */     Properties props = new Properties();
/* 2353 */     for (int i = 0; i < str.length; ++i)
/*      */     {
/* 2355 */       props.put(str[i][0], str[i][1]);
/*      */     }
/* 2357 */     return props;
/*      */   }
/*      */ 
/*      */   public static void parseProperties(Properties props, String input)
/*      */   {
/* 2363 */     parsePropertiesEx(props, input, '\n', '\\', '=');
/*      */   }
/*      */ 
/*      */   public static void parsePropertiesEx(Properties props, String input, char propSeparator, char escapeChar, char nameValueSeparator)
/*      */   {
/* 2370 */     Vector list = parseArray(input, propSeparator, escapeChar);
/* 2371 */     int nstrings = list.size();
/*      */ 
/* 2373 */     for (int i = 0; i < nstrings; ++i)
/*      */     {
/* 2375 */       String nameValue = (String)list.elementAt(i);
/* 2376 */       int len = nameValue.length();
/* 2377 */       int equalsLoc = nameValue.indexOf(nameValueSeparator);
/* 2378 */       if (equalsLoc < 0) {
/*      */         continue;
/*      */       }
/* 2381 */       String key = nameValue.substring(0, equalsLoc);
/* 2382 */       String val = "";
/* 2383 */       if (equalsLoc < len - 1)
/*      */       {
/* 2385 */         val = nameValue.substring(equalsLoc + 1);
/*      */       }
/* 2387 */       key = key.trim();
/* 2388 */       props.put(key, val.trim());
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String convertToString(Properties props)
/*      */   {
/* 2396 */     Vector list = new IdcVector();
/*      */ 
/* 2398 */     for (Enumeration e = props.propertyNames(); e.hasMoreElements(); )
/*      */     {
/* 2400 */       IdcStringBuilder buf = new IdcStringBuilder(256);
/* 2401 */       String key = (String)e.nextElement();
/* 2402 */       String val = props.getProperty(key);
/* 2403 */       buf.append(key);
/* 2404 */       buf.append('=');
/* 2405 */       buf.append(val);
/* 2406 */       list.addElement(buf.toString());
/*      */     }
/*      */ 
/* 2409 */     return createString(list, '\n', '\\');
/*      */   }
/*      */ 
/*      */   public static String[] convertListToArray(List strs)
/*      */   {
/* 2420 */     if (strs == null)
/*      */     {
/* 2422 */       return new String[0];
/*      */     }
/*      */ 
/* 2425 */     int size = strs.size();
/* 2426 */     String[] retVal = new String[size];
/* 2427 */     strs.toArray(retVal);
/* 2428 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static String[] convertAndAppendListToArray(String[] curArray, List strs)
/*      */   {
/* 2441 */     if (strs == null)
/*      */     {
/* 2443 */       return curArray;
/*      */     }
/*      */ 
/* 2446 */     int listSize = strs.size();
/* 2447 */     int oldSize = (curArray != null) ? curArray.length : 0;
/*      */ 
/* 2449 */     String[] retVal = new String[oldSize + listSize];
/*      */ 
/* 2452 */     for (int i = 0; i < oldSize; ++i)
/*      */     {
/* 2454 */       retVal[i] = curArray[i];
/*      */     }
/*      */ 
/* 2457 */     int newSize = oldSize;
/* 2458 */     if (listSize > 0)
/*      */     {
/* 2460 */       for (int i = 0; i < listSize; ++i)
/*      */       {
/* 2462 */         retVal[(newSize++)] = ((String)strs.get(i));
/*      */       }
/*      */     }
/* 2465 */     return retVal;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static String[] convertToArray(Vector strs)
/*      */   {
/* 2475 */     if (strs == null)
/*      */     {
/* 2477 */       return new String[0];
/*      */     }
/*      */ 
/* 2480 */     int size = strs.size();
/* 2481 */     String[] retVal = new String[size];
/* 2482 */     strs.toArray(retVal);
/* 2483 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Vector convertToVector(String[] strs)
/*      */   {
/* 2490 */     Vector retVal = new IdcVector();
/* 2491 */     if (strs == null)
/*      */     {
/* 2493 */       return retVal;
/*      */     }
/* 2495 */     List l = Arrays.asList(strs);
/* 2496 */     retVal.addAll(l);
/* 2497 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static ArrayList convertToList(String[] strs)
/*      */   {
/* 2509 */     ArrayList list = new ArrayList();
/* 2510 */     List l = Arrays.asList(strs);
/*      */ 
/* 2514 */     list.addAll(l);
/* 2515 */     return list;
/*      */   }
/*      */ 
/*      */   public static String[] reallocStringArray(String[] curArray, int newSize, String defVal)
/*      */   {
/* 2533 */     String[] newArray = new String[newSize];
/* 2534 */     int oldSize = (curArray != null) ? curArray.length : 0;
/* 2535 */     for (int i = 0; i < newSize; ++i)
/*      */     {
/* 2537 */       if (i < oldSize)
/*      */       {
/* 2539 */         newArray[i] = curArray[i];
/*      */       }
/*      */       else
/*      */       {
/* 2543 */         newArray[i] = defVal;
/*      */       }
/*      */     }
/* 2546 */     return newArray;
/*      */   }
/*      */ 
/*      */   public static String createReportProgressString(int type, String msg, float amtDone, float max)
/*      */   {
/* 2553 */     if (msg == null)
/*      */     {
/* 2555 */       msg = "null";
/*      */     }
/*      */ 
/* 2558 */     Vector reportStrs = new IdcVector();
/* 2559 */     reportStrs.addElement(Integer.toString(type));
/* 2560 */     reportStrs.addElement(Float.toString(amtDone));
/* 2561 */     reportStrs.addElement(Float.toString(max));
/* 2562 */     reportStrs.addElement(msg);
/* 2563 */     return createString(reportStrs, ',', '\\');
/*      */   }
/*      */ 
/*      */   public static int findStringIndex(String[] list, String key)
/*      */   {
/* 2579 */     return findStringIndexEx(list, key, false);
/*      */   }
/*      */ 
/*      */   public static int findStringIndexEx(String[] list, String key, boolean ignoreCase)
/*      */   {
/* 2592 */     if ((list == null) || (key == null))
/*      */     {
/* 2594 */       return -1;
/*      */     }
/*      */ 
/* 2597 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/* 2599 */       if (ignoreCase)
/*      */       {
/* 2601 */         if (list[i].equalsIgnoreCase(key))
/*      */         {
/* 2603 */           return i;
/*      */         }
/*      */ 
/*      */       }
/* 2608 */       else if (list[i].equals(key))
/*      */       {
/* 2610 */         return i;
/*      */       }
/*      */     }
/*      */ 
/* 2614 */     return -1;
/*      */   }
/*      */ 
/*      */   public static int findStringListIndex(List list, String key)
/*      */   {
/* 2625 */     return findStringListIndexEx(list, key, 0);
/*      */   }
/*      */ 
/*      */   public static int findStringListIndexEx(List list, String key, int flags)
/*      */   {
/* 2638 */     if ((list == null) || (key == null))
/*      */     {
/* 2640 */       return -1;
/*      */     }
/*      */ 
/* 2643 */     boolean ignoreCase = (flags & 0x1) != 0;
/* 2644 */     boolean startsWith = (flags & 0x2) != 0;
/*      */ 
/* 2646 */     int s = list.size();
/* 2647 */     IdcStringBuilder temp = null;
/* 2648 */     int keyLen = 0;
/* 2649 */     for (int i = 0; i < s; ++i)
/*      */     {
/* 2651 */       String l = list.get(i).toString();
/* 2652 */       if (ignoreCase)
/*      */       {
/* 2654 */         if (startsWith)
/*      */         {
/* 2656 */           if (temp == null)
/*      */           {
/* 2658 */             temp = new IdcStringBuilder(key);
/* 2659 */             keyLen = temp.length();
/*      */           }
/* 2661 */           if (temp.compareTo(0, keyLen, l, 0, keyLen, true) == 0)
/*      */           {
/* 2663 */             return i;
/*      */           }
/*      */         }
/* 2666 */         else if (l.equalsIgnoreCase(key))
/*      */         {
/* 2668 */           return i;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 2673 */         if ((startsWith) && 
/* 2675 */           (l.startsWith(key)))
/*      */         {
/* 2677 */           return i;
/*      */         }
/*      */ 
/* 2680 */         if (l.equals(key))
/*      */         {
/* 2682 */           return i;
/*      */         }
/*      */       }
/*      */     }
/* 2686 */     return -1;
/*      */   }
/*      */ 
/*      */   public static boolean match(String string, String pattern, boolean allChoices)
/*      */   {
/* 2694 */     return matchEx(string, pattern, allChoices, false);
/*      */   }
/*      */ 
/*      */   public static boolean matchEx(String string, String pattern, boolean allChoices, boolean ignoreCase)
/*      */   {
/* 2707 */     return matchChars(string, pattern, allChoices, ignoreCase, '*', '?');
/*      */   }
/*      */ 
/*      */   public static boolean matchChars(String string, String pattern, boolean allChoices, boolean ignoreCase, char manyChar, char oneChar)
/*      */   {
/* 2722 */     int strLen = string.length();
/* 2723 */     int patLen = pattern.length();
/* 2724 */     int i = 0;
/* 2725 */     int j = 0;
/* 2726 */     boolean inStar = false;
/*      */     while (true)
/*      */     {
/* 2730 */       i = 0;
/*      */ 
/* 2732 */       while (j < patLen)
/*      */       {
/* 2734 */         char pch = pattern.charAt(j);
/*      */ 
/* 2736 */         if (pch == '|')
/*      */         {
/* 2738 */           if ((!inStar) && (i != strLen)) break;
/* 2739 */           return true;
/*      */         }
/*      */ 
/* 2745 */         if (pch == manyChar)
/*      */         {
/* 2747 */           inStar = true;
/* 2748 */           ++j;
/*      */         }
/*      */ 
/* 2752 */         if (i == strLen) {
/*      */           break;
/*      */         }
/* 2755 */         char sch = string.charAt(i);
/* 2756 */         if (ignoreCase)
/*      */         {
/* 2758 */           pch = Character.toLowerCase(pch);
/* 2759 */           sch = Character.toLowerCase(sch);
/*      */         }
/* 2761 */         ++i;
/* 2762 */         if ((pch == oneChar) || (pch == sch))
/*      */         {
/* 2764 */           if (inStar)
/*      */           {
/* 2766 */             if (matchEx(string.substring(i), pattern.substring(j + 1), false, ignoreCase) == true);
/* 2767 */             return true;
/*      */           }
/*      */ 
/* 2770 */           ++j;
/*      */         }
/*      */         else
/*      */         {
/* 2775 */           if (!inStar) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/* 2780 */       if (j == patLen)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 2785 */       if (!allChoices) {
/* 2786 */         return false;
/*      */       }
/* 2788 */       j = pattern.indexOf(124, j);
/* 2789 */       if (j < 0) {
/* 2790 */         return false;
/*      */       }
/* 2792 */       i = 0;
/* 2793 */       ++j;
/* 2794 */       inStar = false;
/*      */     }
/*      */ 
/* 2797 */     return (inStar) || (i == strLen);
/*      */   }
/*      */ 
/*      */   public static boolean containsWildcards(String string)
/*      */   {
/* 2802 */     return containsWildChars(string, '*', '?');
/*      */   }
/*      */ 
/*      */   public static boolean containsWildChars(String string, char manyChar, char oneChar)
/*      */   {
/* 2810 */     return (string.indexOf(manyChar) >= 0) || (string.indexOf(oneChar) >= 0) || (string.indexOf(124) >= 0);
/*      */   }
/*      */ 
/*      */   public static byte[] getAsClientBytes(String str)
/*      */   {
/* 2822 */     int length = str.length();
/* 2823 */     byte[] buf = new byte[length];
/* 2824 */     char[] c = new char[length];
/* 2825 */     str.getChars(0, length, c, 0);
/* 2826 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2828 */       buf[i] = (byte)c[i];
/*      */     }
/*      */ 
/* 2831 */     return buf;
/*      */   }
/*      */ 
/*      */   public static String toStringRaw(byte[] byteArray)
/*      */   {
/* 2841 */     int length = byteArray.length;
/* 2842 */     char[] c = new char[length];
/* 2843 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2845 */       c[i] = (char)byteArray[i];
/*      */     }
/*      */ 
/* 2848 */     return new String(c);
/*      */   }
/*      */ 
/*      */   public static String toStringRaw(byte[] byteArray, int offset, int length)
/*      */   {
/* 2860 */     char[] c = new char[length - offset];
/* 2861 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 2863 */       c[i] = (char)byteArray[(i - offset)];
/*      */     }
/*      */ 
/* 2866 */     return new String(c);
/*      */   }
/*      */ 
/*      */   public static byte[] getBytes(String str, String encoding)
/*      */     throws UnsupportedEncodingException
/*      */   {
/* 2878 */     byte[] retByte = null;
/* 2879 */     if ((encoding != null) && (encoding.length() > 0))
/*      */     {
/* 2881 */       retByte = str.getBytes(encoding);
/*      */     }
/*      */     else
/*      */     {
/* 2885 */       retByte = str.getBytes();
/*      */     }
/*      */ 
/* 2888 */     return retByte;
/*      */   }
/*      */ 
/*      */   public static String getString(byte[] bytes, String encoding)
/*      */     throws UnsupportedEncodingException, CharConversionException
/*      */   {
/* 2900 */     String retString = getString(bytes, 0, bytes.length, encoding);
/* 2901 */     return retString;
/*      */   }
/*      */ 
/*      */   public static String getString(byte[] bytes, int offset, int length, String encoding)
/*      */     throws UnsupportedEncodingException, CharConversionException
/*      */   {
/* 2916 */     IdcStringBuilder buf = new IdcStringBuilder(bytes.length * 2);
/* 2917 */     copyByteArray(buf, bytes, offset, length, encoding, 0);
/*      */ 
/* 2919 */     String resultString = buf.toString();
/* 2920 */     return resultString;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static int convertByteArray(StringBuffer buf, byte[] bytes, int offset, int length, String encoding, int flags)
/*      */     throws UnsupportedEncodingException, CharConversionException
/*      */   {
/* 2931 */     SystemUtils.reportDeprecatedUsage("use copyByteArray instead of convertByteArray()");
/*      */ 
/* 2933 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 2934 */     int rc = copyByteArray(builder, bytes, offset, length, encoding, flags);
/* 2935 */     buf.append(builder.toString());
/* 2936 */     return rc;
/*      */   }
/*      */ 
/*      */   public static int copyByteArray(IdcStringBuilder buf, byte[] bytes, int offset, int length, String encoding, int flags)
/*      */     throws UnsupportedEncodingException, CharConversionException
/*      */   {
/* 2973 */     ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes, offset, length);
/*      */     InputStreamReader r;
/*      */     InputStreamReader r;
/* 2975 */     if ((encoding != null) && (encoding.length() > 0))
/*      */     {
/* 2977 */       r = new InputStreamReader(byteStream, encoding);
/*      */     }
/*      */     else
/*      */     {
/* 2981 */       r = new InputStreamReader(byteStream);
/*      */     }
/* 2983 */     int results = 0;
/*      */ 
/* 2985 */     boolean isWatchFFFD = (flags & 0x4) == 4;
/*      */ 
/* 2987 */     boolean isTracingFFFD = ((flags & 0x1) == 1) || ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("encoding")));
/*      */ 
/* 2989 */     boolean isFailingFFFD = ((flags & 0x2) == 2) || (SystemUtils.getFailOnReplacementCharacterDefault());
/*      */ 
/* 2991 */     boolean isStopFFFD = (flags & 0x8) == 8;
/* 2992 */     boolean isKeepGoing = (flags & 0x10) == 16;
/*      */ 
/* 2994 */     if (isKeepGoing)
/*      */     {
/* 2996 */       isFailingFFFD = false;
/*      */     }
/* 2998 */     if (isFailingFFFD == true)
/*      */     {
/* 3000 */       isTracingFFFD = true;
/*      */     }
/* 3002 */     if ((isTracingFFFD) || (isStopFFFD))
/*      */     {
/* 3004 */       isWatchFFFD = true;
/*      */     }
/*      */ 
/* 3007 */     int count = 0;
/* 3008 */     int size = 1024;
/* 3009 */     char[] cbuf = new char[1024];
/*      */     try
/*      */     {
/* 3012 */       while ((count = r.read(cbuf, 0, 1024)) > 0)
/*      */       {
/* 3014 */         if (isWatchFFFD)
/*      */         {
/* 3016 */           for (int i = 0; i < count; ++i)
/*      */           {
/* 3018 */             if (cbuf[i] != 65533)
/*      */               continue;
/* 3020 */             results |= 1;
/* 3021 */             if (isStopFFFD) {
/*      */               break;
/*      */             }
/*      */ 
/* 3025 */             Report.trace("encoding", "StringUtils.getString() found a replacement character", null);
/*      */ 
/* 3028 */             isWatchFFFD = false;
/* 3029 */             if (!isFailingFFFD)
/*      */               continue;
/* 3031 */             String msg = LocaleUtils.encodeMessage("syCharEncodingReplacementCharFound", null);
/*      */ 
/* 3033 */             throw new CharConversionException(msg);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 3038 */         buf.append(cbuf, 0, count);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String msg;
/*      */       String msg;
/* 3044 */       if (encoding == null)
/*      */       {
/* 3046 */         msg = LocaleUtils.encodeMessage("syCharEncodingErrorDefault", null);
/*      */       }
/*      */       else
/*      */       {
/* 3050 */         msg = LocaleUtils.encodeMessage("syCharEncodingError", null, encoding);
/*      */       }
/* 3052 */       results |= 2;
/* 3053 */       if (SystemUtils.m_verbose)
/*      */       {
/* 3055 */         Report.trace("encoding", null, e);
/* 3056 */         TracingOutputStream stream = new TracingOutputStream("copyByteArray:", "encoding");
/* 3057 */         stream.write(bytes, offset, length);
/* 3058 */         stream.close();
/*      */       }
/* 3060 */       if (!isKeepGoing)
/*      */       {
/* 3062 */         CharConversionException cce = new CharConversionException(msg);
/* 3063 */         cce.initCause(e);
/* 3064 */         throw cce;
/*      */       }
/*      */     }
/*      */ 
/* 3068 */     return results;
/*      */   }
/*      */ 
/*      */   public static IdcStringBuilder escapeCharArray(char[] src, String[][] escapeCharMap)
/*      */   {
/* 3077 */     IdcStringBuilder buffer = new IdcStringBuilder(src.length + 8);
/* 3078 */     escapeCharArrayToAppendable(src, 0, src.length, escapeCharMap, buffer);
/* 3079 */     return buffer;
/*      */   }
/*      */ 
/*      */   public static void escapeCharArrayToAppendable(char[] src, int start, int len, String[][] escapeCharMap, IdcAppendable appendable)
/*      */   {
/* 3092 */     int charMapLen = 0;
/* 3093 */     if (escapeCharMap != null)
/*      */     {
/* 3095 */       charMapLen = escapeCharMap.length;
/*      */     }
/* 3097 */     char[] escapeChars = new char[charMapLen];
/* 3098 */     for (int i = 0; i < escapeChars.length; ++i)
/*      */     {
/* 3100 */       escapeChars[i] = escapeCharMap[i][0].charAt(0);
/*      */     }
/* 3102 */     int end = start + len;
/* 3103 */     for (int i = start; i < end; ++i)
/*      */     {
/* 3105 */       int index = -1;
/* 3106 */       for (int j = 0; j < escapeChars.length; ++j)
/*      */       {
/* 3108 */         if (src[i] != escapeChars[j])
/*      */           continue;
/* 3110 */         index = j;
/* 3111 */         break;
/*      */       }
/*      */ 
/* 3114 */       if (index == -1)
/*      */       {
/* 3116 */         appendable.append(src[i]);
/*      */       }
/*      */       else
/*      */       {
/* 3120 */         appendable.append(escapeCharMap[index][1]);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static StringBuffer escapeChars(char[] src, String[][] escapeCharMap)
/*      */   {
/* 3131 */     StringBuffer buffer = new StringBuffer();
/* 3132 */     Report.trace("deprecated", "escapeChars", null);
/*      */ 
/* 3134 */     int len = 0;
/* 3135 */     if (escapeCharMap != null)
/*      */     {
/* 3137 */       len = escapeCharMap.length;
/*      */     }
/* 3139 */     char[] escapeChars = new char[len];
/* 3140 */     for (int i = 0; i < escapeChars.length; ++i)
/*      */     {
/* 3142 */       escapeChars[i] = escapeCharMap[i][0].charAt(0);
/*      */     }
/* 3144 */     for (int i = 0; i < src.length; ++i)
/*      */     {
/* 3146 */       int index = -1;
/* 3147 */       for (int j = 0; j < escapeChars.length; ++j)
/*      */       {
/* 3149 */         if (src[i] != escapeChars[j])
/*      */           continue;
/* 3151 */         index = j;
/* 3152 */         break;
/*      */       }
/*      */ 
/* 3155 */       if (index == -1)
/*      */       {
/* 3157 */         buffer.append(src[i]);
/*      */       }
/*      */       else
/*      */       {
/* 3161 */         buffer.append(escapeCharMap[index][1]);
/*      */       }
/*      */     }
/* 3164 */     return buffer;
/*      */   }
/*      */ 
/*      */   public static String encodeJavascriptString(String arg)
/*      */   {
/* 3175 */     arg = encodeLiteralStringEscapeSequence(arg);
/* 3176 */     IdcStringBuilder rBuf = new IdcStringBuilder(arg.length() * 4);
/*      */ 
/* 3178 */     for (int i = 0; i < arg.length(); ++i)
/*      */     {
/* 3180 */       int ch = arg.charAt(i);
/* 3181 */       if (ch > 4095)
/*      */       {
/* 3183 */         rBuf.append("\\u").append(Integer.toHexString(ch));
/*      */       }
/* 3185 */       else if (ch > 255)
/*      */       {
/* 3187 */         rBuf.append("\\u0").append(Integer.toHexString(ch));
/*      */       }
/* 3189 */       else if ((ch > 127) || (ch == 60) || (ch == 62))
/*      */       {
/* 3191 */         rBuf.append("\\u00").append(Integer.toHexString(ch));
/*      */       }
/*      */       else
/*      */       {
/* 3195 */         rBuf.append((char)ch);
/*      */       }
/*      */     }
/*      */ 
/* 3199 */     return rBuf.toString();
/*      */   }
/*      */ 
/*      */   protected static String encodeJavascriptFilenameEx(String arg)
/*      */   {
/* 3211 */     String result = arg;
/* 3212 */     IdcStringBuilder buf = null;
/* 3213 */     int length = arg.length();
/* 3214 */     boolean started = false;
/* 3215 */     int startIndex = 0;
/* 3216 */     int numTrailingSpaces = 0;
/*      */ 
/* 3218 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 3220 */       char ch = arg.charAt(i);
/* 3221 */       if (ch == ' ')
/*      */       {
/* 3223 */         if (!started) {
/*      */           continue;
/*      */         }
/*      */ 
/* 3227 */         ++numTrailingSpaces;
/*      */       }
/*      */       else
/*      */       {
/* 3231 */         numTrailingSpaces = 0;
/*      */       }
/* 3233 */       if (!started)
/*      */       {
/* 3235 */         startIndex = i;
/* 3236 */         started = true;
/*      */       }
/* 3238 */       if ((ch >= ' ') && (ch < ''))
/*      */       {
/* 3240 */         switch (ch)
/*      */         {
/*      */         case ' ':
/*      */         case '"':
/*      */         case '#':
/*      */         case '\'':
/*      */         case '*':
/*      */         case '/':
/*      */         case ':':
/*      */         case '<':
/*      */         case '>':
/*      */         case '?':
/*      */         case '@':
/*      */         case '\\':
/*      */         case '|':
/* 3255 */           break;
/*      */         default:
/* 3257 */           if (buf == null)
/*      */             continue;
/* 3259 */           buf.append(ch); break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 3264 */       if (buf == null)
/*      */       {
/* 3266 */         buf = new IdcStringBuilder(length + 128);
/* 3267 */         buf.append(arg.substring(startIndex, i));
/*      */       }
/* 3269 */       if (ch >= 'က')
/* 3270 */         buf.append("@").append(Integer.toHexString(ch));
/* 3271 */       else if (ch >= 'Ā')
/* 3272 */         buf.append("@0").append(Integer.toHexString(ch));
/* 3273 */       else if (ch >= '\020')
/* 3274 */         buf.append("@00").append(Integer.toHexString(ch));
/*      */       else {
/* 3276 */         buf.append("@000").append(Integer.toHexString(ch));
/*      */       }
/*      */     }
/* 3279 */     if (buf != null)
/*      */     {
/* 3281 */       if (numTrailingSpaces > 0)
/*      */       {
/* 3283 */         buf.setLength(buf.length() - numTrailingSpaces * 5);
/*      */       }
/* 3285 */       result = buf.toString();
/*      */     }
/* 3287 */     return result;
/*      */   }
/*      */ 
/*      */   public static String encodeJavascriptFilename(String arg)
/*      */   {
/* 3292 */     return encodeJavascriptFilenameEx(arg).toLowerCase();
/*      */   }
/*      */ 
/*      */   public static String encodeJavascriptFilenamePreserveCase(String arg)
/*      */   {
/* 3297 */     return encodeJavascriptFilenameEx(arg);
/*      */   }
/*      */ 
/*      */   public static String decodeJavascriptFilename(String arg)
/*      */     throws CharConversionException
/*      */   {
/* 3303 */     if (arg == null)
/*      */     {
/* 3305 */       return null;
/*      */     }
/*      */ 
/* 3308 */     String result = arg;
/* 3309 */     IdcStringBuilder buf = null;
/* 3310 */     int length = arg.length();
/* 3311 */     for (int i = 0; i < length; ++i)
/*      */     {
/* 3313 */       char ch = arg.charAt(i);
/* 3314 */       if (ch != '@')
/*      */       {
/* 3316 */         if (buf == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 3323 */         buf.append(ch);
/*      */       }
/*      */       else
/*      */       {
/* 3327 */         if (i + 5 > length)
/*      */         {
/* 3329 */           throw new CharConversionException("StringUtils.decodeJavascriptFilename() called with defective string.");
/*      */         }
/*      */ 
/* 3332 */         String code = arg.substring(i + 1, i + 5);
/*      */         try
/*      */         {
/* 3335 */           ch = (char)Integer.parseInt(code, 16);
/*      */         }
/*      */         catch (NumberFormatException e)
/*      */         {
/* 3339 */           String msg = LocaleUtils.encodeMessage("csScriptUtilErrorParsingInt", null, code);
/*      */ 
/* 3341 */           throw new CharConversionException(msg);
/*      */         }
/* 3343 */         if (buf == null)
/*      */         {
/* 3345 */           buf = new IdcStringBuilder(length);
/* 3346 */           buf.append(arg.substring(0, i));
/*      */         }
/* 3348 */         buf.append(ch);
/* 3349 */         i += 4;
/*      */       }
/*      */     }
/* 3352 */     if (buf == null)
/*      */     {
/* 3354 */       return result;
/*      */     }
/* 3356 */     return buf.toString();
/*      */   }
/*      */ 
/*      */   public static boolean isConfigAllWhiteSpace(String str)
/*      */   {
/* 3368 */     if (str == null)
/*      */     {
/* 3370 */       return true;
/*      */     }
/* 3372 */     boolean retVal = true;
/* 3373 */     int len = str.length();
/* 3374 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 3376 */       if (str.charAt(i) <= ' ')
/*      */         continue;
/* 3378 */       retVal = false;
/* 3379 */       break;
/*      */     }
/*      */ 
/* 3382 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean isConfigValueCaseInsensitiveStartsWith(String prefix, String value)
/*      */   {
/* 3399 */     if (prefix == null)
/*      */     {
/* 3401 */       return true;
/*      */     }
/* 3403 */     int prefixLen = prefix.length();
/*      */ 
/* 3405 */     if (value == null)
/*      */     {
/* 3407 */       return false;
/*      */     }
/* 3409 */     int valueLen = value.length();
/* 3410 */     if (prefixLen > valueLen)
/*      */     {
/* 3412 */       return false;
/*      */     }
/* 3414 */     boolean isPrefix = true;
/* 3415 */     for (int i = 0; i < prefixLen; ++i)
/*      */     {
/* 3421 */       char prefixCh = prefix.charAt(i);
/* 3422 */       char valueCh = value.charAt(i);
/* 3423 */       if ((prefixCh >= 'A') && (prefixCh <= 'Z'))
/*      */       {
/* 3425 */         prefixCh = (char)(prefixCh - 'A' + 97);
/*      */       }
/* 3427 */       if ((valueCh >= 'A') && (valueCh <= 'Z'))
/*      */       {
/* 3429 */         valueCh = (char)(valueCh - 'A' + 97);
/*      */       }
/* 3431 */       if (prefixCh == valueCh)
/*      */         continue;
/* 3433 */       isPrefix = false;
/* 3434 */       break;
/*      */     }
/*      */ 
/* 3437 */     return isPrefix;
/*      */   }
/*      */ 
/*      */   public static boolean isConfigValueCaseInsensitiveEquals(String str1, String str2)
/*      */   {
/* 3451 */     return (str1.length() == str2.length()) && (isConfigValueCaseInsensitiveStartsWith(str1, str2));
/*      */   }
/*      */ 
/*      */   public static int findConfigValueCaseInsensitiveInList(String str, String[] list)
/*      */   {
/* 3463 */     int index = -1;
/* 3464 */     if ((list == null) || (str == null))
/*      */     {
/* 3466 */       return index;
/*      */     }
/* 3468 */     int l = str.length();
/* 3469 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/* 3471 */       String lStr = list[i];
/* 3472 */       if ((lStr == null) || (lStr.length() != l) || (!isConfigValueCaseInsensitiveStartsWith(str, lStr))) {
/*      */         continue;
/*      */       }
/* 3475 */       index = i;
/* 3476 */       break;
/*      */     }
/*      */ 
/* 3479 */     return index;
/*      */   }
/*      */ 
/*      */   public static boolean isSafeHtml(String str)
/*      */     throws IOException, ParseSyntaxException, ServiceException
/*      */   {
/* 3492 */     StringReader reader = new StringReader(str);
/* 3493 */     ParseOutput parseOutput = new ParseOutput();
/* 3494 */     String tag = null;
/* 3495 */     boolean isSafe = true;
/* 3496 */     while ((tag = Parser.findAnyTag(reader, parseOutput)) != null)
/*      */     {
/* 3498 */       int end = tag.indexOf(" ");
/* 3499 */       if (end > 0) {
/* 3500 */         tag = tag.substring(0, end);
/*      */       }
/* 3502 */       char ch = tag.charAt(0);
/* 3503 */       if ((ch != '/') && (!Validation.isAllowableHtmlTag(tag)))
/*      */       {
/* 3505 */         isSafe = false;
/* 3506 */         break;
/*      */       }
/*      */     }
/* 3509 */     parseOutput.releaseBuffers();
/* 3510 */     return isSafe;
/*      */   }
/*      */ 
/*      */   public static String removeSubstringKey(String str, String key, String optKey)
/*      */   {
/* 3523 */     if ((key == null) || (key.length() == 0))
/*      */     {
/* 3525 */       return str;
/*      */     }
/* 3527 */     int index = str.indexOf(key);
/* 3528 */     if (index >= 0)
/*      */     {
/* 3530 */       int start = index;
/* 3531 */       int end = index + key.length();
/* 3532 */       int prevCharIndex = start - 1;
/* 3533 */       int afterCharIndex = end;
/* 3534 */       boolean prevCharIsSlash = false;
/* 3535 */       boolean afterCharIsSlash = false;
/* 3536 */       if (prevCharIndex >= 0)
/*      */       {
/* 3538 */         char ch = str.charAt(prevCharIndex);
/* 3539 */         prevCharIsSlash = ch == '/';
/* 3540 */         if ((!Character.isLetterOrDigit(ch)) && (!prevCharIsSlash))
/*      */         {
/* 3542 */           --start;
/*      */         }
/*      */       }
/* 3545 */       if (afterCharIndex < str.length())
/*      */       {
/* 3547 */         char ch = str.charAt(afterCharIndex);
/* 3548 */         afterCharIsSlash = ch == '/';
/* 3549 */         if ((!Character.isLetterOrDigit(ch)) && (!afterCharIsSlash))
/*      */         {
/* 3551 */           ++end;
/*      */         }
/*      */       }
/* 3554 */       String replaceStr = "";
/* 3555 */       if (((optKey != null) && (afterCharIsSlash) && (start == 0)) || ((prevCharIsSlash) && (end == str.length())))
/*      */       {
/* 3557 */         replaceStr = optKey;
/*      */       }
/*      */ 
/* 3560 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 3561 */       buf.append(str.substring(0, start));
/* 3562 */       buf.append(replaceStr);
/* 3563 */       buf.append(str.substring(end));
/* 3564 */       str = buf.toString();
/*      */     }
/* 3566 */     return str;
/*      */   }
/*      */ 
/*      */   public static String replaceString(String str, String key, String newVal, int flags)
/*      */   {
/* 3580 */     boolean ignoreCase = (flags & 0x1) != 0;
/*      */ 
/* 3582 */     String tmpStr = str;
/* 3583 */     String tmpKey = key;
/* 3584 */     if (ignoreCase)
/*      */     {
/* 3586 */       tmpStr = str.toLowerCase();
/* 3587 */       tmpKey = key.toLowerCase();
/*      */     }
/* 3589 */     int index = tmpStr.indexOf(tmpKey);
/* 3590 */     int prevIndex = 0;
/* 3591 */     IdcStringBuilder out = null;
/* 3592 */     while (index >= 0)
/*      */     {
/* 3594 */       if (out == null)
/*      */       {
/* 3596 */         out = new IdcStringBuilder(str.length() + newVal.length());
/*      */       }
/* 3598 */       out.append(str, prevIndex, index - prevIndex);
/* 3599 */       out.append(newVal);
/* 3600 */       prevIndex = index + key.length();
/* 3601 */       index = tmpStr.indexOf(tmpKey, prevIndex);
/*      */     }
/* 3603 */     if ((out != null) && (prevIndex < str.length()))
/*      */     {
/* 3605 */       out.append(str, prevIndex, str.length() - prevIndex);
/*      */     }
/* 3607 */     String result = str;
/* 3608 */     if (out != null)
/*      */     {
/* 3610 */       result = out.toString();
/*      */     }
/* 3612 */     return result;
/*      */   }
/*      */ 
/*      */   public static void appendForDebug(IdcAppendable appendable, Object o, int nestCount)
/*      */   {
/* 3623 */     if (nestCount++ > 10)
/*      */     {
/* 3625 */       Report.trace("system", "appendForDebug() Nesting too deeply", null);
/* 3626 */       return;
/*      */     }
/* 3628 */     if (o == null)
/*      */     {
/* 3630 */       appendable.append("(null)");
/*      */     }
/* 3632 */     else if (o instanceof Object[])
/*      */     {
/* 3634 */       Object[] array = (Object[])(Object[])o;
/* 3635 */       appendable.append("[");
/* 3636 */       for (int i = 0; i < array.length; ++i)
/*      */       {
/* 3638 */         if (i > 0)
/*      */         {
/* 3640 */           appendable.append(",");
/*      */         }
/* 3642 */         if (i > 1000)
/*      */         {
/* 3644 */           appendable.append("...");
/* 3645 */           break;
/*      */         }
/* 3647 */         appendForDebug(appendable, array[i], nestCount);
/*      */       }
/* 3649 */       appendable.append("]");
/*      */     }
/* 3651 */     else if (o instanceof List)
/*      */     {
/* 3653 */       List l = (List)o;
/* 3654 */       int size = l.size();
/* 3655 */       appendable.append("[");
/* 3656 */       for (int i = 0; i < size; ++i)
/*      */       {
/* 3658 */         if (i > 0)
/*      */         {
/* 3660 */           appendable.append(",");
/*      */         }
/* 3662 */         if (i > 1000)
/*      */         {
/* 3664 */           appendable.append("...");
/* 3665 */           break;
/*      */         }
/* 3667 */         appendForDebug(appendable, l.get(i), nestCount);
/*      */       }
/* 3669 */       appendable.append("]");
/*      */     }
/* 3671 */     else if (o instanceof Set)
/*      */     {
/* 3673 */       Set s = (Set)o;
/* 3674 */       int count = 0;
/* 3675 */       appendable.append("[");
/* 3676 */       for (Iterator i$ = s.iterator(); i$.hasNext(); ) { Object item = i$.next();
/*      */ 
/* 3678 */         if (count > 0)
/*      */         {
/* 3680 */           appendable.append(",");
/*      */         }
/* 3682 */         if (count > 1000)
/*      */         {
/* 3684 */           appendable.append("...");
/* 3685 */           break;
/*      */         }
/* 3687 */         appendForDebug(appendable, item, nestCount);
/* 3688 */         ++count; }
/*      */ 
/* 3690 */       appendable.append("]");
/*      */     }
/* 3692 */     else if (o instanceof IdcDebugOutput)
/*      */     {
/* 3694 */       IdcDebugOutput debugOutput = (IdcDebugOutput)o;
/* 3695 */       debugOutput.appendDebugFormat(appendable);
/*      */     }
/* 3697 */     else if (o instanceof IdcAppender)
/*      */     {
/* 3699 */       IdcAppender appender = (IdcAppender)o;
/* 3700 */       appender.appendTo(appendable);
/*      */     }
/* 3702 */     else if (o instanceof Date)
/*      */     {
/* 3704 */       appendable.append(LocaleUtils.debugDate((Date)o));
/*      */     }
/*      */     else
/*      */     {
/* 3708 */       appendable.append(o.toString());
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void appendDebugProperty(IdcAppendable appendable, String key, Object val, boolean isAfterFirst)
/*      */   {
/* 3722 */     if (isAfterFirst)
/*      */     {
/* 3724 */       appendable.append(", ");
/*      */     }
/* 3726 */     appendable.append(key);
/* 3727 */     appendable.append("=");
/* 3728 */     if ((val != null) && (val instanceof String))
/*      */     {
/* 3730 */       appendable.append((String)val);
/*      */     }
/*      */     else
/*      */     {
/* 3734 */       appendForDebug(appendable, val, 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String appenderToString(IdcAppender appender)
/*      */   {
/* 3740 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 3741 */     appender.appendTo(builder);
/* 3742 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public static String createGUID(int size)
/*      */   {
/* 3747 */     return createGUIDEx(size, 2, "-");
/*      */   }
/*      */ 
/*      */   public static String createGUIDEx(int size, int numCharsInGroup, String sep)
/*      */   {
/* 3752 */     return createGUIDEx(size, numCharsInGroup, sep, false);
/*      */   }
/*      */ 
/*      */   public static String createGUIDEx(int size, int numCharsInGroup, String sep, boolean useSecureRandom)
/*      */   {
/* 3766 */     int nChars = (size + 1) / 2;
/* 3767 */     int fudge = (nChars - 1) / 2;
/* 3768 */     byte[] b = new byte[nChars];
/*      */ 
/* 3776 */     if (useSecureRandom)
/*      */     {
/* 3778 */       CryptoCommonUtils.getRandomBytes(b);
/*      */     }
/*      */     else
/*      */     {
/* 3782 */       getSemiRandomBytes(b);
/*      */     }
/*      */ 
/* 3785 */     IdcStringBuilder builder = new IdcStringBuilder();
/*      */ 
/* 3787 */     builder.ensureCapacity(2 * nChars + fudge);
/*      */ 
/* 3789 */     int count = 0;
/* 3790 */     for (int i = 0; i < nChars; ++i)
/*      */     {
/* 3792 */       if ((i != 0) && (numCharsInGroup > 0) && (i % numCharsInGroup == 0))
/*      */       {
/* 3794 */         builder.append(sep);
/* 3795 */         ++count;
/*      */       }
/* 3797 */       NumberUtils.appendHexByte(builder, b[i]);
/*      */     }
/* 3799 */     builder.setLength(size + count);
/*      */ 
/* 3801 */     return builder.toString();
/*      */   }
/*      */ 
/*      */   public static synchronized void getSemiRandomBytes(byte[] b)
/*      */   {
/* 3815 */     if (s_random == null)
/*      */     {
/* 3817 */       s_random = new Random(new Date().getTime());
/*      */     }
/*      */ 
/* 3820 */     s_random.nextBytes(b);
/*      */   }
/*      */ 
/*      */   public static IdcAppendable appendAsHex(IdcAppendable buf, CharSequence source)
/*      */     throws UnsupportedEncodingException
/*      */   {
/* 3826 */     if (buf == null)
/*      */     {
/* 3828 */       buf = new IdcStringBuilder();
/*      */     }
/*      */     byte[] b;
/*      */     byte[] b;
/* 3832 */     if (source instanceof String)
/*      */     {
/* 3834 */       b = ((String)source).getBytes("UTF8");
/*      */     }
/*      */     else
/*      */     {
/* 3838 */       b = source.toString().getBytes("UTF8");
/*      */     }
/* 3840 */     int len = b.length;
/* 3841 */     for (int j = 0; j < len; ++j)
/*      */     {
/* 3843 */       NumberUtils.appendHexByte(buf, b[j]);
/*      */     }
/* 3845 */     return buf;
/*      */   }
/*      */ 
/*      */   public static String createSubstr(String str, String prefix, String suffix, int flags)
/*      */   {
/* 3850 */     if (str == null)
/*      */     {
/* 3852 */       return null;
/*      */     }
/* 3854 */     String tmp = str;
/* 3855 */     if ((flags & 0x1) != 0)
/*      */     {
/* 3857 */       tmp = tmp.toLowerCase();
/* 3858 */       if (prefix != null)
/*      */       {
/* 3860 */         prefix = prefix.toLowerCase();
/*      */       }
/*      */ 
/* 3863 */       if (suffix != null)
/*      */       {
/* 3865 */         suffix = suffix.toLowerCase();
/*      */       }
/*      */     }
/*      */ 
/* 3869 */     int startIndex = 0;
/* 3870 */     if (prefix != null)
/*      */     {
/* 3872 */       startIndex = str.indexOf(prefix);
/*      */     }
/* 3874 */     int endIndex = str.length();
/* 3875 */     if ((suffix != null) && (startIndex >= 0))
/*      */     {
/* 3877 */       endIndex = str.indexOf(suffix, startIndex);
/* 3878 */       if (((flags & 0x8) == 0) && (endIndex < 0))
/*      */       {
/* 3880 */         endIndex = str.length();
/*      */       }
/*      */     }
/* 3883 */     if (((startIndex < 0) && ((flags & 0x4) != 0)) || (endIndex < 0))
/*      */     {
/* 3885 */       return null;
/*      */     }
/*      */ 
/* 3888 */     return str.substring(startIndex + 1, endIndex);
/*      */   }
/*      */ 
/*      */   public static String strip(String str)
/*      */   {
/* 3893 */     return stripEx(str, "\r\n\t");
/*      */   }
/*      */ 
/*      */   public static String stripEx(String str, String characters)
/*      */   {
/* 3898 */     int beginIndex = 0;
/* 3899 */     int endIndex = str.length();
/*      */ 
/* 3901 */     while ((beginIndex < endIndex) && (characters.indexOf(str.charAt(beginIndex)) >= 0))
/*      */     {
/* 3903 */       ++beginIndex;
/*      */     }
/*      */ 
/* 3906 */     while ((endIndex > beginIndex) && (characters.indexOf(str.charAt(endIndex - 1)) >= 0))
/*      */     {
/* 3908 */       --endIndex;
/*      */     }
/*      */ 
/* 3911 */     return str.substring(beginIndex, endIndex);
/*      */   }
/*      */ 
/*      */   public static String getLabel(String str, String beginStr, String endStr)
/*      */   {
/* 3924 */     String label = null;
/* 3925 */     int beginIndex = str.indexOf(beginStr);
/* 3926 */     if (beginIndex != -1)
/*      */     {
/* 3928 */       int endIndex = str.indexOf(endStr, beginIndex);
/* 3929 */       if (endIndex != -1)
/*      */       {
/* 3931 */         label = str.substring(beginIndex + 1, endIndex);
/*      */       }
/*      */     }
/* 3934 */     return label;
/*      */   }
/*      */ 
/*      */   public static int findStoppingIndex(String str, int startIndex, char ch1, char ch2, char ch3)
/*      */   {
/* 3950 */     int sLen = str.length();
/* 3951 */     for (int i = startIndex; i < sLen; ++i)
/*      */     {
/* 3953 */       char ch = str.charAt(i);
/* 3954 */       if (ch <= ' ')
/*      */       {
/* 3956 */         return i;
/*      */       }
/* 3958 */       if (ch == ch1)
/*      */       {
/* 3960 */         return i;
/*      */       }
/* 3962 */       if (ch == ch2)
/*      */       {
/* 3964 */         return i;
/*      */       }
/* 3966 */       if (ch == ch3)
/*      */       {
/* 3968 */         return i;
/*      */       }
/*      */     }
/* 3971 */     return -1;
/*      */   }
/*      */ 
/*      */   public static boolean isEqualStringArrays(String[] arr1, String[] arr2)
/*      */   {
/* 3983 */     if (arr1 == arr2)
/*      */     {
/* 3986 */       return true;
/*      */     }
/* 3988 */     if ((arr1 == null) || (arr2 == null))
/*      */     {
/* 3991 */       return false;
/*      */     }
/* 3993 */     if (arr1.length != arr2.length)
/*      */     {
/* 3995 */       return false;
/*      */     }
/*      */ 
/* 3998 */     boolean retVal = true;
/* 3999 */     for (int i = 0; i < arr1.length; ++i)
/*      */     {
/* 4001 */       String s1 = arr1[i];
/* 4002 */       String s2 = arr2[i];
/* 4003 */       if (s1 == s2) {
/*      */         continue;
/*      */       }
/* 4006 */       if ((s1 == null) || (s2 == null))
/*      */       {
/* 4009 */         retVal = false;
/* 4010 */         break;
/*      */       }
/* 4012 */       if (s1.equals(s2))
/*      */         continue;
/* 4014 */       retVal = false;
/* 4015 */       break;
/*      */     }
/*      */ 
/* 4019 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static String[] convertToArray(Collection c)
/*      */   {
/* 4029 */     String[] array = new String[c.size()];
/* 4030 */     return (String[])(String[])c.toArray(array);
/*      */   }
/*      */ 
/*      */   public static List[] splitString(String str, char[] separators, int flags)
/*      */   {
/* 4035 */     int numSep = separators.length;
/* 4036 */     List[] resultArray = new Vector[numSep + 1];
/*      */ 
/* 4038 */     if ((flags & 0x1) == 0)
/*      */     {
/* 4040 */       for (int i = 0; i <= numSep; ++i)
/*      */       {
/* 4042 */         resultArray[i] = new IdcVector();
/*      */       }
/*      */     }
/*      */ 
/* 4046 */     char[] strChars = str.toCharArray();
/*      */ 
/* 4048 */     int curSepIndex = numSep;
/* 4049 */     IdcStringBuilder curString = new IdcStringBuilder();
/*      */ 
/* 4051 */     for (int i = 0; i < strChars.length; ++i)
/*      */     {
/* 4053 */       char c = strChars[i];
/* 4054 */       boolean foundNewSep = false;
/* 4055 */       for (int j = 0; j < numSep; ++j)
/*      */       {
/* 4057 */         if (c != separators[j]) {
/*      */           continue;
/*      */         }
/* 4060 */         if (curString.m_length > 0)
/*      */         {
/* 4062 */           if (((flags & 0x1) > 0) && (resultArray[curSepIndex] == null))
/*      */           {
/* 4064 */             resultArray[curSepIndex] = new IdcVector();
/*      */           }
/* 4066 */           resultArray[curSepIndex].add(curString.toStringNoRelease());
/* 4067 */           curString.setLength(0);
/*      */         }
/* 4069 */         curSepIndex = j;
/* 4070 */         foundNewSep = true;
/* 4071 */         break;
/*      */       }
/*      */ 
/* 4074 */       if ((foundNewSep) && ((((flags & 0x2) <= 0) || (!foundNewSep))))
/*      */         continue;
/* 4076 */       curString.append(c);
/*      */     }
/*      */ 
/* 4080 */     if (curString.m_length > 0)
/*      */     {
/* 4082 */       if (((flags & 0x1) > 0) && (resultArray[curSepIndex] == null))
/*      */       {
/* 4084 */         resultArray[curSepIndex] = new IdcVector();
/*      */       }
/* 4086 */       resultArray[curSepIndex].add(curString.toString());
/*      */     }
/*      */ 
/* 4089 */     return resultArray;
/*      */   }
/*      */ 
/*      */   public static String[] appendStringArrayNoDuplicates(String[] in1, String[] in2, int flags)
/*      */   {
/* 4107 */     List result = new ArrayList();
/* 4108 */     boolean makeLowerCase = (flags & 0x4) != 0;
/* 4109 */     boolean isCaseInsensitive = ((flags & 0x1) != 0) && (!makeLowerCase);
/* 4110 */     boolean isSimpleCase = (in1 == null) || (in2 == null) || (in1.length + in2.length < 10);
/* 4111 */     Set seenStrings = null;
/*      */ 
/* 4113 */     if ((in1 == null) && (in2 == null))
/*      */     {
/* 4115 */       return null;
/*      */     }
/* 4117 */     if (!isSimpleCase)
/*      */     {
/* 4119 */       seenStrings = new HashSet();
/*      */     }
/*      */ 
/* 4122 */     if (in1 != null)
/*      */     {
/* 4124 */       for (String s : in1)
/*      */       {
/* 4126 */         if (s == null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 4130 */         if (makeLowerCase)
/*      */         {
/* 4132 */           s = s.toLowerCase();
/*      */         }
/* 4134 */         if (!isSimpleCase)
/*      */         {
/* 4136 */           String storeKey = (isCaseInsensitive) ? s.toLowerCase() : s;
/* 4137 */           seenStrings.add(storeKey);
/*      */         }
/* 4139 */         result.add(s);
/*      */       }
/*      */     }
/* 4142 */     if (in2 != null)
/*      */     {
/* 4144 */       label311: for (String s : in2)
/*      */       {
/* 4146 */         if (s == null) {
/*      */           continue;
/*      */         }
/*      */ 
/* 4150 */         if (makeLowerCase)
/*      */         {
/* 4152 */           s = s.toLowerCase();
/*      */         }
/* 4154 */         boolean isFound = false;
/* 4155 */         if (isSimpleCase)
/*      */         {
/* 4158 */           for (String testString : result)
/*      */           {
/* 4160 */             if (isCaseInsensitive)
/*      */             {
/* 4162 */               if (!testString.equalsIgnoreCase(s))
/*      */                 break label311;
/* 4164 */               isFound = true;
/* 4165 */               break;
/*      */             }
/*      */ 
/* 4170 */             if (testString.equals(s))
/*      */             {
/* 4172 */               isFound = true;
/* 4173 */               break;
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 4180 */           String storeKey = (isCaseInsensitive) ? s.toLowerCase() : s;
/* 4181 */           if (seenStrings.contains(storeKey))
/*      */           {
/* 4183 */             isFound = true;
/*      */           }
/*      */           else
/*      */           {
/* 4187 */             seenStrings.add(storeKey);
/*      */           }
/*      */         }
/* 4190 */         if (isFound)
/*      */           continue;
/* 4192 */         result.add(s);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 4197 */     return convertListToArray(result);
/*      */   }
/*      */ 
/*      */   public static String[] mergeStringArrays(String[] array1, String[] array2)
/*      */   {
/* 4212 */     if ((array1 == null) || (array1.length == 0))
/* 4213 */       return array2;
/* 4214 */     if ((array2 == null) || (array2.length == 0))
/* 4215 */       return array1;
/* 4216 */     List array1List = Arrays.asList(array1);
/* 4217 */     List array2List = Arrays.asList(array2);
/* 4218 */     List result = new ArrayList(array1List);
/* 4219 */     List tmp = new ArrayList(array1List);
/* 4220 */     tmp.retainAll(array2List);
/* 4221 */     result.removeAll(tmp);
/* 4222 */     result.addAll(array2List);
/* 4223 */     return (String[])result.toArray(new String[result.size()]);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 4228 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98726 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.StringUtils
 * JD-Core Version:    0.5.4
 */