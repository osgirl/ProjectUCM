/*      */ package intradoc.filterdata;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.GrammarElement;
/*      */ import intradoc.common.HtmlChunk;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceData;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ 
/*      */ public class HtmlFilterUtils
/*      */ {
/*      */   public static final int ENCODE_NONE = 0;
/*      */   public static final int ENCODE_UNSAFE = 1;
/*      */   public static final int ENCODE_EXCEPT_FOR_SAFE = 2;
/*      */   public static final int ENCODE_LF_AND_EXCEPT_FOR_SAFE = 3;
/*      */   public static final int ENCODE_ALL = 4;
/*   52 */   public static String[] m_unsafeTags = { "script", "applet", "object", "html", "body", "head", "form", "input", "select", "option", "textarea", "iframe" };
/*   53 */   public static Map m_unsafeTagsLookup = new Hashtable();
/*      */ 
/*   56 */   public static String[] m_safeTags = { "font", "span", "strong", "p", "b", "i", "br", "a", "img", "hr", "center", "link", "blockquote", "bq", "fn", "note", "tab", "code", "credit", "del", "dfn", "em", "h1", "h2", "h3", "h4", "h5", "blink", "s", "small", "sub", "sup", "tt", "u", "ins", "kbd", "q", "person", "samp", "var", "ul", "li", "math", "over", "left", "right", "text", "above", "below", "bar", "dot", "ddot", "hat", "tilde", "vec", "sqrt", "root", "of", "array", "row", "item" };
/*      */ 
/*   61 */   public static Map m_safeTagsLookup = new Hashtable();
/*      */ 
/*   63 */   public static Map<String, String> m_safeIdocLookup = new Hashtable();
/*      */ 
/*   66 */   public static String[] m_lfNotSafeTags = { "br", "p", "ul", "li" };
/*   67 */   public static Map m_lfNotSafeTagsLookup = new Hashtable();
/*      */ 
/*   71 */   public static char[][] m_additionalUnsafeTags = { { 'a' }, { 'i', 'm', 'g' } };
/*      */ 
/*   75 */   public static String[] m_unsafeTagJavascriptEventPrefixes = { "blur", "key", "mouse", "focus", "error" };
/*      */ 
/*   78 */   public static FilterDataInputSpecialOptions m_defaultSpecialOptions = new FilterDataInputSpecialOptions();
/*      */ 
/*   81 */   public static int UPPER_TO_LOWER_CASE_OFFSET = 32;
/*      */ 
/*   84 */   protected static boolean m_isInit = false;
/*      */ 
/*      */   public static void checkInit()
/*      */   {
/*   88 */     if (m_isInit)
/*      */       return;
/*   90 */     m_isInit = true;
/*   91 */     addUnsafeTags(m_unsafeTags);
/*   92 */     addSafeTags(m_safeTags);
/*   93 */     addLfNotSafeTags(m_lfNotSafeTags);
/*   94 */     addSafeIdocMethod(SharedObjects.getTable("SafeIdocMethods"));
/*      */   }
/*      */ 
/*      */   public static FilterDataInputSpecialOptions getDefaultOptions()
/*      */   {
/*  103 */     return m_defaultSpecialOptions;
/*      */   }
/*      */ 
/*      */   public static FilterDataInputSpecialOptions shallowCloneDefaultOptions()
/*      */   {
/*  108 */     return m_defaultSpecialOptions.shallowClone();
/*      */   }
/*      */ 
/*      */   public static void parseSpecialOptions(String specialOptions, FilterDataInputSpecialOptions specialOptionsObj)
/*      */   {
/*  114 */     if (specialOptions == null)
/*      */       return;
/*  116 */     if (specialOptions.indexOf("wordbreak") >= 0)
/*      */     {
/*  118 */       specialOptionsObj.m_doWordBreak = (specialOptions.indexOf("nowordbreak") < 0);
/*      */     }
/*      */ 
/*  121 */     if (specialOptions.indexOf("specialencode") >= 0)
/*      */     {
/*  123 */       specialOptionsObj.m_escapePotentiallyUnsafeCharacters = (specialOptions.indexOf("nospecialencode") < 0);
/*      */     }
/*      */ 
/*  126 */     if (specialOptions.indexOf("linefeed") >= 0)
/*      */     {
/*  128 */       specialOptionsObj.m_encodeLineFeeds = (specialOptions.indexOf("nolinefeed") < 0);
/*      */     }
/*      */ 
/*  131 */     if (specialOptions.indexOf("fragment") >= 0)
/*      */     {
/*  133 */       specialOptionsObj.m_processFragments = (specialOptions.indexOf("nofragment") < 0);
/*      */     }
/*      */ 
/*  136 */     if (specialOptions.indexOf("remove") >= 0)
/*      */     {
/*  138 */       specialOptionsObj.m_removeTags = (specialOptions.indexOf("noremove") < 0);
/*      */     }
/*      */ 
/*  142 */     String key = "maxlinelength=";
/*  143 */     int index = specialOptions.indexOf(key);
/*  144 */     if (index < 0)
/*      */       return;
/*  146 */     index += key.length();
/*  147 */     int endIndex = specialOptions.indexOf(index, 44);
/*  148 */     String arg = null;
/*  149 */     if (endIndex < 0)
/*      */     {
/*  151 */       arg = specialOptions.substring(index);
/*      */     }
/*      */     else
/*      */     {
/*  155 */       arg = specialOptions.substring(index, endIndex);
/*      */     }
/*  157 */     specialOptionsObj.m_maxWordBreak = NumberUtils.parseInteger(arg, specialOptionsObj.m_maxWordBreak);
/*      */   }
/*      */ 
/*      */   public static void addUnsafeTags(String[] unsafeTags)
/*      */   {
/*  164 */     if (unsafeTags == null)
/*      */       return;
/*  166 */     for (int i = 0; i < unsafeTags.length; ++i)
/*      */     {
/*  168 */       String tag = unsafeTags[i];
/*  169 */       if (tag == null)
/*      */         continue;
/*  171 */       tag = tag.toLowerCase();
/*  172 */       m_unsafeTagsLookup.put(tag, "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addSafeTags(String[] safeTags)
/*      */   {
/*  180 */     if (safeTags == null)
/*      */       return;
/*  182 */     for (int i = 0; i < safeTags.length; ++i)
/*      */     {
/*  184 */       String tag = safeTags[i];
/*  185 */       if (tag == null)
/*      */         continue;
/*  187 */       tag = tag.toLowerCase();
/*  188 */       m_safeTagsLookup.put(tag, "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addSafeIdocMethod(DataResultSet safeSet)
/*      */   {
/*  196 */     if ((safeSet == null) || (safeSet.isEmpty()))
/*      */     {
/*  198 */       return;
/*      */     }
/*  200 */     for (safeSet.first(); safeSet.isRowPresent(); safeSet.next())
/*      */     {
/*  202 */       m_safeIdocLookup.put(safeSet.getStringValueByName("MethodName").toLowerCase(), "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void setTagSafetyState(String tag, boolean isToSafe)
/*      */   {
/*  214 */     Map from = (isToSafe) ? m_unsafeTagsLookup : m_safeTagsLookup;
/*  215 */     Map to = (isToSafe) ? m_safeTagsLookup : m_unsafeTagsLookup;
/*  216 */     from.remove(tag);
/*  217 */     to.put(tag, "1");
/*      */   }
/*      */ 
/*      */   public static void setAdditionalUnsafeTags(String[] tags)
/*      */   {
/*  229 */     if (tags == null)
/*      */       return;
/*  231 */     char[][] addTags = new char[tags.length][];
/*  232 */     for (int i = 0; i < addTags.length; ++i)
/*      */     {
/*  234 */       addTags[i] = tags[i].toCharArray();
/*      */     }
/*  236 */     m_additionalUnsafeTags = addTags;
/*      */   }
/*      */ 
/*      */   public static boolean isAdditionalUnsafeTags(char[] arr, int offset, int numChars)
/*      */   {
/*  250 */     char ch1 = arr[offset];
/*  251 */     if ((ch1 >= 'A') && (ch1 <= 'Z'))
/*      */     {
/*  253 */       ch1 = (char)(ch1 + UPPER_TO_LOWER_CASE_OFFSET);
/*      */     }
/*  255 */     boolean retVal = false;
/*  256 */     for (int i = 0; i < m_additionalUnsafeTags.length; ++i)
/*      */     {
/*  258 */       char[] testTag = m_additionalUnsafeTags[i];
/*  259 */       if ((testTag.length != numChars) || (testTag[0] != ch1))
/*      */         continue;
/*  261 */       boolean matched = true;
/*  262 */       for (int j = 1; j < testTag.length; ++j)
/*      */       {
/*  264 */         char ch = arr[(offset + j)];
/*  265 */         if ((ch >= 'A') && (ch <= 'Z'))
/*      */         {
/*  267 */           ch = (char)(ch + UPPER_TO_LOWER_CASE_OFFSET);
/*      */         }
/*  269 */         if (testTag[j] == ch)
/*      */           continue;
/*  271 */         matched = false;
/*  272 */         break;
/*      */       }
/*      */ 
/*  275 */       if (!matched)
/*      */         continue;
/*  277 */       retVal = true;
/*  278 */       break;
/*      */     }
/*      */ 
/*  282 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static void addLfNotSafeTags(String[] lfNotSafeTags)
/*      */   {
/*  287 */     if (lfNotSafeTags == null)
/*      */       return;
/*  289 */     for (int i = 0; i < lfNotSafeTags.length; ++i)
/*      */     {
/*  291 */       String tag = lfNotSafeTags[i];
/*  292 */       if (tag == null)
/*      */         continue;
/*  294 */       tag = tag.toLowerCase();
/*  295 */       m_lfNotSafeTagsLookup.put(tag, "1");
/*      */     }
/*      */   }
/*      */ 
/*      */   public static int translateEncodingRule(String rule, int defRule)
/*      */   {
/*  303 */     int retVal = defRule;
/*  304 */     if (rule != null)
/*      */     {
/*  306 */       if (rule.equalsIgnoreCase("none"))
/*      */       {
/*  308 */         retVal = 0;
/*      */       }
/*  310 */       else if (rule.equalsIgnoreCase("unsafe"))
/*      */       {
/*  312 */         retVal = 1;
/*      */       }
/*  314 */       else if (rule.equalsIgnoreCase("exceptsafe"))
/*      */       {
/*  316 */         retVal = 2;
/*      */       }
/*  318 */       else if (rule.equalsIgnoreCase("lfexceptsafe"))
/*      */       {
/*  320 */         retVal = 3;
/*      */       }
/*  322 */       else if (rule.equalsIgnoreCase("all"))
/*      */       {
/*  324 */         retVal = 4;
/*      */       }
/*  328 */       else if (Validation.checkInteger(rule) == 0)
/*      */       {
/*  330 */         retVal = NumberUtils.parseInteger(rule, retVal);
/*      */       }
/*      */     }
/*      */ 
/*  334 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean encodeForHtmlView(String in, int encodingRule, FilterDataInputSpecialOptions specialOptions, IdcStringBuilder out, ExecutionContext cxt)
/*      */   {
/*  372 */     checkInit();
/*  373 */     if (specialOptions == null)
/*      */     {
/*  375 */       specialOptions = m_defaultSpecialOptions;
/*      */     }
/*      */ 
/*  378 */     int inLen = in.length();
/*  379 */     if ((encodingRule == 0) || (inLen == 0))
/*      */     {
/*  381 */       return false;
/*      */     }
/*      */ 
/*  386 */     boolean encodeAllExceptSafe = encodingRule > 1;
/*  387 */     boolean encodeLineFeeds = (specialOptions.m_encodeLineFeeds) || (encodingRule == 3);
/*      */ 
/*  389 */     boolean doWordBreak = specialOptions.m_doWordBreak;
/*  390 */     boolean encodeSpecial = specialOptions.m_escapePotentiallyUnsafeCharacters;
/*  391 */     boolean encodeAdditionalTags = specialOptions.m_encodeAdditionalUnsafe;
/*  392 */     boolean encodeAllTags = encodingRule == 4;
/*  393 */     boolean removeTags = specialOptions.m_removeTags;
/*  394 */     boolean processFragments = specialOptions.m_processFragments;
/*  395 */     int startOffset = 0;
/*  396 */     if ((!doWordBreak) || (inLen < specialOptions.m_maxWordBreak))
/*      */     {
/*  398 */       int leftIndex = in.indexOf("<");
/*  399 */       int index = leftIndex;
/*  400 */       if (index < 0)
/*      */       {
/*  402 */         index = in.indexOf("=");
/*      */       }
/*  404 */       if ((encodeAllExceptSafe) || (processFragments))
/*      */       {
/*  406 */         if ((index < 0) || ((processFragments) && (removeTags)))
/*      */         {
/*  408 */           int rightIndex = in.indexOf(">");
/*  409 */           if ((rightIndex >= 0) && 
/*  411 */             (processFragments) && (removeTags) && 
/*  413 */             (rightIndex < leftIndex))
/*      */           {
/*  415 */             boolean trimLeftFragment = true;
/*  416 */             if (rightIndex > 0)
/*      */             {
/*  418 */               char ch = in.charAt(rightIndex - 1);
/*  419 */               if (ch <= ' ')
/*      */               {
/*  421 */                 trimLeftFragment = false;
/*      */               }
/*      */             }
/*  424 */             if (trimLeftFragment)
/*      */             {
/*  426 */               startOffset = rightIndex + 1;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  431 */           if (index < 0)
/*      */           {
/*  433 */             index = rightIndex;
/*      */           }
/*      */         }
/*  436 */         if ((index < 0) && (encodeSpecial) && (encodeAllExceptSafe))
/*      */         {
/*  439 */           char[] specialChars = specialOptions.m_potentiallyUnsafeCharacters;
/*  440 */           for (int i = 0; (i < inLen) && (index < 0); ++i)
/*      */           {
/*  442 */             char ch = in.charAt(i);
/*  443 */             for (int j = 0; j < specialChars.length; ++j)
/*      */             {
/*  445 */               if (specialChars[j] != ch)
/*      */                 continue;
/*  447 */               index = i;
/*  448 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  454 */       if ((index < 0) && (encodeLineFeeds))
/*      */       {
/*  456 */         index = in.indexOf("\n");
/*      */       }
/*  458 */       if (index < 0)
/*      */       {
/*  460 */         return false;
/*      */       }
/*      */     }
/*  463 */     int wordBreakLength = 0;
/*  464 */     if (doWordBreak)
/*      */     {
/*  466 */       wordBreakLength = specialOptions.m_maxWordBreak;
/*      */     }
/*      */ 
/*  470 */     out.ensureCapacity(out.length() + in.length() * 2);
/*  471 */     char[] chs = in.toCharArray();
/*      */ 
/*  473 */     boolean insideTag = false;
/*  474 */     boolean foundAtLeastOneTag = startOffset == 0;
/*  475 */     boolean insideUnknownTag = false;
/*  476 */     boolean insideTagDeclaration = false;
/*  477 */     boolean insideLiteral = false;
/*  478 */     boolean startingInsideAttributeValue = false;
/*  479 */     boolean insideAttributeValue = false;
/*  480 */     boolean insideHtmlComment = false;
/*  481 */     boolean removingTag = false;
/*  482 */     int htmlCommentBodyStartIndex = -1;
/*  483 */     char curLiteralChar = '\000';
/*  484 */     int tagStart = -1;
/*  485 */     boolean insideKeyWord = false;
/*  486 */     boolean afterKeyWord = false;
/*  487 */     boolean afterSpace = false;
/*      */ 
/*  489 */     boolean isIdoc = false;
/*  490 */     boolean testIdoc = false;
/*  491 */     boolean forceAllIdocUnsafe = false;
/*  492 */     int idocEmbedCounter = 0;
/*  493 */     int keyWordStart = -1;
/*      */ 
/*  496 */     int index = startOffset;
/*  497 */     int lastWordBreakCount = 0;
/*      */ 
/*  499 */     while (index < chs.length)
/*      */     {
/*  503 */       boolean appendChar = false;
/*  504 */       boolean escapeIt = false;
/*  505 */       boolean endingInsideTag = false;
/*  506 */       char ch = chs[index];
/*      */ 
/*  509 */       if ((encodeSpecial) && (encodeAllExceptSafe))
/*      */       {
/*  513 */         char[] specialChars = specialOptions.m_potentiallyUnsafeCharacters;
/*  514 */         for (int j = 0; j < specialChars.length; ++j)
/*      */         {
/*  516 */           if (specialChars[j] != ch)
/*      */             continue;
/*  518 */           if (ch == '&')
/*      */           {
/*  522 */             if ((index + 3 < chs.length) && (chs[(index + 1)] == 'a') && (chs[(index + 2)] == 'm')) {
/*      */               break;
/*      */             }
/*  525 */             out.append("&amp");
/*  526 */             ch = ';'; break;
/*      */           }
/*      */ 
/*  531 */           out.append("&#");
/*  532 */           out.append(Integer.toString(ch));
/*  533 */           ch = ';';
/*      */ 
/*  535 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  540 */       if ((!isIdoc) && (ch == '<'))
/*      */       {
/*  542 */         if (insideHtmlComment)
/*      */         {
/*  544 */           escapeIt = true;
/*      */         }
/*  546 */         else if (!insideTag)
/*      */         {
/*  548 */           insideTag = true;
/*  549 */           insideTagDeclaration = true;
/*  550 */           tagStart = index;
/*  551 */           foundAtLeastOneTag = true;
/*      */ 
/*  553 */           insideKeyWord = false;
/*  554 */           afterKeyWord = false;
/*      */         }
/*      */         else
/*      */         {
/*  558 */           appendChar = true;
/*      */         }
/*      */       }
/*  561 */       else if ((!isIdoc) && (ch == '>'))
/*      */       {
/*  563 */         if (insideHtmlComment)
/*      */         {
/*  565 */           if ((index < htmlCommentBodyStartIndex + 2) || (chs[(index - 1)] != '-') || (chs[(index - 2)] != '-'))
/*      */           {
/*  567 */             escapeIt = true;
/*      */           }
/*      */           else
/*      */           {
/*  571 */             insideHtmlComment = false;
/*  572 */             insideTag = false;
/*  573 */             insideUnknownTag = false;
/*  574 */             appendChar = true;
/*      */           }
/*      */         }
/*  577 */         else if (insideTag)
/*      */         {
/*  579 */           appendChar = true;
/*  580 */           if (!insideLiteral)
/*      */           {
/*  582 */             insideTag = false;
/*  583 */             insideUnknownTag = false;
/*  584 */             endingInsideTag = true;
/*      */           }
/*      */ 
/*      */         }
/*  589 */         else if (((encodeAllExceptSafe) && (!insideHtmlComment)) || ((processFragments) && (!foundAtLeastOneTag)))
/*      */         {
/*  592 */           escapeIt = true;
/*      */         }
/*      */         else
/*      */         {
/*  596 */           appendChar = true;
/*      */         }
/*      */ 
/*      */       }
/*  600 */       else if ((!isIdoc) && (((ch == '\'') || (ch == '"'))))
/*      */       {
/*  602 */         appendChar = true;
/*  603 */         if (insideLiteral)
/*      */         {
/*  605 */           if (curLiteralChar == ch)
/*      */           {
/*  607 */             insideLiteral = false;
/*      */           }
/*      */ 
/*      */         }
/*  612 */         else if ((!insideTagDeclaration) && (insideTag) && (!insideHtmlComment))
/*      */         {
/*  614 */           if ((insideAttributeValue) && (startingInsideAttributeValue))
/*      */           {
/*  616 */             insideLiteral = true;
/*  617 */             curLiteralChar = ch;
/*      */           }
/*      */           else
/*      */           {
/*  621 */             escapeIt = true;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*  626 */       else if ((!isIdoc) && (!removingTag) && (((ch == ')') || (ch == '(') || (ch == '&') || (ch == '\\'))))
/*      */       {
/*  628 */         if ((insideLiteral) || (insideAttributeValue))
/*      */         {
/*  630 */           if ((ch == '&') || (ch == '\\'))
/*      */           {
/*  632 */             int nextIndex = index + 1;
/*  633 */             if (nextIndex < chs.length)
/*      */             {
/*  635 */               boolean checkUnicodeEscape = ch == '\\';
/*  636 */               char nextChar = chs[nextIndex];
/*  637 */               if (((nextChar == 'u') && (checkUnicodeEscape)) || ((nextChar == '#') && (!checkUnicodeEscape) && 
/*  644 */                 (checkForUnsafeAttributeEscapeSequences(chs, nextIndex + 1, checkUnicodeEscape))))
/*      */               {
/*  646 */                 if (checkUnicodeEscape)
/*      */                 {
/*  650 */                   out.append('\\');
/*      */                 }
/*      */                 else
/*      */                 {
/*  654 */                   out.append("&#amp");
/*  655 */                   ch = ';';
/*      */                 }
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  664 */             out.append("&amp;x2");
/*  665 */             out.append((ch == '(') ? '8' : '9');
/*  666 */             ch = ';';
/*      */           }
/*      */         }
/*  669 */         appendChar = true;
/*      */       }
/*  671 */       else if ((!isIdoc) && (ch == '$') && (insideTagDeclaration) && (chs[(index - 1)] == '<'))
/*      */       {
/*  673 */         isIdoc = true;
/*      */       }
/*  675 */       else if ((isIdoc) && (ch == '$') && (insideTagDeclaration) && (chs[(index - 1)] == '<'))
/*      */       {
/*  677 */         ++idocEmbedCounter;
/*      */       }
/*  679 */       else if ((ch == '$') && (insideTagDeclaration) && (chs.length >= index + 1) && (chs[(index + 1)] == '>'))
/*      */       {
/*  681 */         if (idocEmbedCounter == 0)
/*      */         {
/*  683 */           isIdoc = false;
/*  684 */           testIdoc = true;
/*      */         }
/*      */         else
/*      */         {
/*  688 */           --idocEmbedCounter;
/*      */         }
/*      */ 
/*      */       }
/*  695 */       else if (insideTagDeclaration)
/*      */       {
/*  701 */         if ((!Character.isLetterOrDigit(ch)) && (!isIdoc))
/*      */         {
/*  703 */           appendChar = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  708 */         appendChar = true;
/*  709 */         if (insideTag)
/*      */         {
/*  711 */           if (ch == '=')
/*      */           {
/*  713 */             if ((!insideAttributeValue) && (!insideHtmlComment))
/*      */             {
/*  715 */               startingInsideAttributeValue = true;
/*  716 */               if (insideKeyWord)
/*      */               {
/*  718 */                 if (checkForUnsafeJavascriptEventNames(chs, keyWordStart, index))
/*      */                 {
/*  720 */                   insideUnknownTag = true;
/*      */                 }
/*  722 */                 insideKeyWord = false;
/*      */               }
/*      */ 
/*      */             }
/*  726 */             else if ((((insideLiteral) || (insideAttributeValue))) && (insideUnknownTag) && (!removingTag))
/*      */             {
/*  729 */               out.append("&amp;x61");
/*  730 */               ch = ';';
/*      */             }
/*      */           }
/*      */         }
/*  734 */         else if ((removeTags) && (ch == '-') && 
/*  736 */           (index + 2 < chs.length) && (chs[(index + 1)] == '-'))
/*      */         {
/*  738 */           char tch = chs[(index + 2)];
/*  739 */           if (tch == '>')
/*      */           {
/*  742 */             appendChar = false;
/*  743 */             index += 2;
/*      */           }
/*  745 */           else if ((tch == '$') && (processFragments) && ((
/*  747 */             (index == 0) || (chs[(index - 1)] <= ' '))))
/*      */           {
/*  752 */             removingTag = true;
/*  753 */             insideTag = true;
/*  754 */             appendChar = false;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  765 */       if ((appendChar) || (escapeIt))
/*      */       {
/*  773 */         if (insideAttributeValue)
/*      */         {
/*  775 */           if ((ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n'))
/*      */           {
/*  777 */             if ((!insideTag) || ((!startingInsideAttributeValue) && (!insideLiteral)))
/*      */             {
/*  779 */               afterSpace = true;
/*  780 */               insideAttributeValue = false;
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  786 */             startingInsideAttributeValue = false;
/*  787 */             if (!insideTag)
/*      */             {
/*  789 */               insideAttributeValue = false;
/*      */             }
/*      */           }
/*      */         }
/*  793 */         else if (startingInsideAttributeValue)
/*      */         {
/*  795 */           insideAttributeValue = true;
/*      */         }
/*  797 */         else if (insideTagDeclaration)
/*      */         {
/*  799 */           boolean isSafe = false;
/*      */ 
/*  803 */           int startIndex = tagStart + 1;
/*  804 */           int endIndex = index - 1;
/*  805 */           if (chs[startIndex] == '/')
/*      */           {
/*  807 */             ++startIndex;
/*      */           }
/*  809 */           if (chs[endIndex] == '/')
/*      */           {
/*  811 */             --endIndex;
/*      */           }
/*  813 */           int tagLength = endIndex - startIndex + 1;
/*  814 */           String testTag = null;
/*      */ 
/*  816 */           boolean wellFormed = false;
/*  817 */           if (tagLength > 0)
/*      */           {
/*  819 */             wellFormed = (Character.isWhitespace(ch)) || (ch == '>');
/*      */           }
/*      */ 
/*  823 */           if ((ch == '!') && (startIndex == tagStart + 1) && (endIndex == tagStart) && (index < chs.length - 3) && 
/*  825 */             (chs[(index + 1)] == '-') && (chs[(index + 2)] == '-'))
/*      */           {
/*  827 */             insideHtmlComment = true;
/*  828 */             htmlCommentBodyStartIndex = index + 3;
/*  829 */             isSafe = true;
/*      */           }
/*      */ 
/*  832 */           if ((tagLength > 0) && (!isSafe) && ((
/*  836 */             (!encodeAllExceptSafe) || (wellFormed))))
/*      */           {
/*  838 */             testTag = new String(chs, startIndex, tagLength);
/*  839 */             testTag = testTag.toLowerCase();
/*      */           }
/*      */ 
/*  843 */           if (!encodeAllTags)
/*      */           {
/*  845 */             if ((encodeAllExceptSafe) && (!isSafe))
/*      */             {
/*  848 */               if ((testTag != null) && 
/*  850 */                 (m_safeTagsLookup.get(testTag) != null) && 
/*  852 */                 (((!encodeLineFeeds) || (m_lfNotSafeTagsLookup.get(testTag) == null))) && ((
/*  855 */                 (!encodeAdditionalTags) || (!isAdditionalUnsafeTags(chs, startIndex, tagLength)))))
/*      */               {
/*  858 */                 isSafe = true;
/*      */               }
/*      */ 
/*      */             }
/*  864 */             else if (!isSafe)
/*      */             {
/*  867 */               if (testTag == null)
/*      */               {
/*  869 */                 isSafe = true;
/*      */               }
/*  874 */               else if (testIdoc)
/*      */               {
/*  876 */                 if (forceAllIdocUnsafe)
/*      */                 {
/*  879 */                   isSafe = false;
/*      */                 }
/*      */                 else
/*      */                 {
/*  883 */                   isSafe = doCheckIdocSafety("<" + testTag + ">", cxt);
/*  884 */                   if (!isSafe)
/*      */                   {
/*  886 */                     forceAllIdocUnsafe = true;
/*      */                   }
/*      */                 }
/*  889 */                 testIdoc = false;
/*      */               }
/*  891 */               else if ((m_unsafeTagsLookup.get(testTag) == null) && ((
/*  893 */                 (!encodeAdditionalTags) || (!isAdditionalUnsafeTags(chs, startIndex, tagLength)))))
/*      */               {
/*  895 */                 isSafe = true;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  903 */           int copyStart = tagStart;
/*  904 */           if (!isSafe)
/*      */           {
/*  906 */             if (removeTags)
/*      */             {
/*  908 */               removingTag = true;
/*      */             }
/*      */ 
/*  911 */             if (!removingTag)
/*      */             {
/*  913 */               if (doWordBreak)
/*      */               {
/*  915 */                 if (index > copyStart)
/*      */                 {
/*  917 */                   lastWordBreakCount += index - copyStart;
/*      */                 }
/*      */                 else
/*      */                 {
/*  923 */                   ++lastWordBreakCount;
/*      */                 }
/*      */               }
/*  926 */               out.append("&lt;");
/*  927 */               ++copyStart;
/*  928 */               if ((ch == '>') || (ch == '<'))
/*      */               {
/*  930 */                 escapeIt = true;
/*      */               }
/*      */             }
/*      */           }
/*  934 */           if ((copyStart < index) && (!removingTag) && (((!insideHtmlComment) || (!removeTags))))
/*      */           {
/*  936 */             if ((isIdoc) && (forceAllIdocUnsafe))
/*      */             {
/*  938 */               String idoc = new String(chs, copyStart, index - copyStart);
/*  939 */               idoc = idoc.replaceAll("<", "&lt;");
/*  940 */               idoc = idoc.replaceAll(">", "&gt;");
/*  941 */               out.append(idoc);
/*      */             }
/*      */             else
/*      */             {
/*  945 */               out.append(chs, copyStart, index - copyStart);
/*      */             }
/*      */           }
/*  948 */           if ((isSafe) && (ch == '<'))
/*      */           {
/*  951 */             tagStart = index;
/*  952 */             appendChar = false;
/*      */           }
/*      */           else
/*      */           {
/*  956 */             if (((!isSafe) && (!removingTag)) || (!wellFormed))
/*      */             {
/*  958 */               insideTag = false;
/*  959 */               insideUnknownTag = false;
/*      */             }
/*  961 */             insideTagDeclaration = false;
/*      */           }
/*      */ 
/*      */         }
/*  967 */         else if (!escapeIt)
/*      */         {
/*  970 */           if ((('a' <= ch) && (ch <= 'z')) || (('A' <= ch) && (ch <= 'Z')))
/*      */           {
/*  972 */             if (afterSpace)
/*      */             {
/*  974 */               keyWordStart = index;
/*  975 */               insideKeyWord = true;
/*  976 */               afterSpace = false;
/*      */             }
/*  978 */             afterKeyWord = false;
/*      */           }
/*      */           else
/*      */           {
/*  982 */             afterSpace = ch <= ' ';
/*  983 */             if (insideKeyWord)
/*      */             {
/*  985 */               if ((afterSpace) || (ch == '='))
/*      */               {
/*  987 */                 afterKeyWord = checkForUnsafeJavascriptEventNames(chs, keyWordStart, index);
/*      */               }
/*  989 */               insideKeyWord = false;
/*      */             }
/*  991 */             if ((afterKeyWord) && (ch == '='))
/*      */             {
/*  993 */               if (!afterSpace)
/*      */               {
/*  995 */                 afterKeyWord = false;
/*      */               }
/*  997 */               if (ch == '=')
/*      */               {
/* 1004 */                 insideTag = true;
/* 1005 */                 insideUnknownTag = true;
/* 1006 */                 insideAttributeValue = true;
/* 1007 */                 startingInsideAttributeValue = true;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1014 */           insideKeyWord = false;
/* 1015 */           afterKeyWord = false;
/*      */         }
/*      */ 
/* 1019 */         boolean doCharOutput = true;
/* 1020 */         if ((insideHtmlComment) && (removeTags))
/*      */         {
/* 1022 */           doCharOutput = false;
/*      */ 
/* 1025 */           index += 2;
/* 1026 */           insideHtmlComment = false;
/*      */         }
/* 1028 */         if (removingTag)
/*      */         {
/* 1030 */           doCharOutput = false;
/*      */         }
/*      */ 
/* 1033 */         if (doCharOutput)
/*      */         {
/* 1035 */           if (escapeIt)
/*      */           {
/* 1037 */             if (ch == '<')
/*      */             {
/* 1039 */               out.append("&lt;");
/*      */             }
/* 1041 */             else if (ch == '>')
/*      */             {
/* 1043 */               out.append("&gt;");
/*      */             }
/*      */             else
/*      */             {
/* 1047 */               out.append("&#");
/* 1048 */               out.append(Integer.toString(ch));
/* 1049 */               out.append(";");
/*      */             }
/*      */           }
/* 1052 */           else if (appendChar)
/*      */           {
/* 1054 */             if ((ch == '\n') && (!insideTag) && (encodeLineFeeds))
/*      */             {
/* 1057 */               if (index < chs.length - 1)
/*      */               {
/* 1059 */                 out.append("<br>");
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/* 1065 */               out.append(ch);
/*      */             }
/*      */           }
/* 1068 */           if ((doWordBreak) && (((escapeIt) || ((!insideTag) && (!endingInsideTag)))))
/*      */           {
/* 1070 */             if (Character.isWhitespace(ch))
/*      */             {
/* 1072 */               lastWordBreakCount = 0;
/*      */             }
/*      */             else
/*      */             {
/* 1076 */               if ((appendChar) || (escapeIt))
/*      */               {
/* 1078 */                 ++lastWordBreakCount;
/*      */               }
/* 1080 */               if (lastWordBreakCount >= wordBreakLength)
/*      */               {
/* 1083 */                 out.append(specialOptions.m_lineBreakEntity);
/*      */ 
/* 1086 */                 lastWordBreakCount = 0;
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 1092 */       if (endingInsideTag)
/*      */       {
/* 1094 */         removingTag = false;
/*      */       }
/* 1096 */       ++index;
/*      */     }
/* 1098 */     if (!removingTag)
/*      */     {
/* 1100 */       if (insideTagDeclaration)
/*      */       {
/* 1102 */         int copyStart = tagStart;
/* 1103 */         if (encodeAllExceptSafe)
/*      */         {
/* 1105 */           out.append("&lt;");
/* 1106 */           ++copyStart;
/*      */         }
/* 1108 */         if (copyStart < index)
/*      */         {
/* 1110 */           out.append(chs, copyStart, index - copyStart);
/*      */         }
/*      */       }
/* 1113 */       else if (insideLiteral)
/*      */       {
/* 1115 */         out.append(curLiteralChar);
/* 1116 */         out.append(">");
/*      */       }
/* 1118 */       else if (insideTag)
/*      */       {
/* 1120 */         out.append(">");
/*      */       }
/* 1122 */       if (insideHtmlComment)
/*      */       {
/* 1124 */         out.append("-->");
/*      */       }
/*      */     }
/*      */ 
/* 1128 */     return true;
/*      */   }
/*      */ 
/*      */   public static boolean checkForUnsafeJavascriptEventNames(char[] chs, int startIndex, int endIndex)
/*      */   {
/* 1133 */     if (startIndex + 5 >= endIndex)
/*      */     {
/* 1135 */       return false;
/*      */     }
/* 1137 */     int index = startIndex;
/* 1138 */     char ch1 = chs[(index++)];
/* 1139 */     if ((ch1 != 'o') && (ch1 != 'O'))
/*      */     {
/* 1141 */       return false;
/*      */     }
/* 1143 */     char ch2 = chs[(index++)];
/* 1144 */     if ((ch2 != 'n') && (ch2 != 'N'))
/*      */     {
/* 1146 */       return false;
/*      */     }
/* 1148 */     String s = new String(chs, index, endIndex - index);
/* 1149 */     s = s.toLowerCase();
/* 1150 */     boolean retVal = false;
/* 1151 */     for (int i = 0; i < m_unsafeTagJavascriptEventPrefixes.length; ++i)
/*      */     {
/* 1153 */       if (!s.startsWith(m_unsafeTagJavascriptEventPrefixes[i]))
/*      */         continue;
/* 1155 */       retVal = true;
/* 1156 */       break;
/*      */     }
/*      */ 
/* 1159 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean checkForUnsafeAttributeEscapeSequences(char[] chs, int startIndex, boolean isUnicodeEscape)
/*      */   {
/* 1166 */     int numZeroesSkipped = 0;
/* 1167 */     int index = startIndex;
/* 1168 */     boolean retVal = false;
/* 1169 */     boolean foundFirstNumber = false;
/* 1170 */     boolean finishedNumber = false;
/* 1171 */     boolean isHex = isUnicodeEscape;
/* 1172 */     while (index < chs.length)
/*      */     {
/* 1174 */       char ch = chs[index];
/* 1175 */       if (!foundFirstNumber)
/*      */       {
/* 1177 */         if (ch == '0')
/*      */         {
/* 1179 */           ++numZeroesSkipped;
/* 1180 */           if ((numZeroesSkipped <= 2) || (!isUnicodeEscape))
/*      */             break label190;
/* 1182 */           break;
/*      */         }
/*      */ 
/* 1185 */         if (((isHex) && (ch == '2') && (((numZeroesSkipped == 2) || (!isUnicodeEscape)))) || ((!isHex) && (ch == '4')))
/*      */         {
/* 1188 */           foundFirstNumber = true;
/*      */         } else {
/* 1190 */           if ((isHex) || (ch != 'x'))
/*      */             break;
/* 1192 */           isHex = true;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1199 */         if (finishedNumber)
/*      */         {
/* 1201 */           if ((ch >= '0') && (ch <= '9')) {
/*      */             break;
/*      */           }
/* 1204 */           retVal = true; break;
/*      */         }
/*      */ 
/* 1208 */         if ((((!isHex) || ((ch != '8') && (ch != '9')))) && (((isHex) || ((ch != '0') && (ch != '1')))))
/*      */           break;
/* 1210 */         finishedNumber = true;
/* 1211 */         if (isUnicodeEscape)
/*      */         {
/* 1213 */           retVal = true;
/* 1214 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1221 */       label190: ++index;
/*      */     }
/* 1223 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean doCheckIdocSafety(String testString, ExecutionContext cxt)
/*      */   {
/* 1228 */     if ((testString == null) || (testString.length() == 0))
/*      */     {
/* 1231 */       return true;
/*      */     }
/*      */ 
/* 1234 */     DynamicHtml dynHtml = null;
/*      */     try
/*      */     {
/* 1237 */       Service svc = (Service)cxt;
/*      */ 
/* 1240 */       String serviceName = svc.getServiceData().m_name;
/* 1241 */       DataResultSet safeServiceSet = SharedObjects.getTable("SafeIdocServices");
/* 1242 */       if ((safeServiceSet != null) && 
/* 1244 */         (safeServiceSet.findRow(0, serviceName) != null))
/*      */       {
/* 1246 */         return true;
/*      */       }
/*      */ 
/* 1250 */       testString = testString.toLowerCase();
/* 1251 */       if (testString.startsWith("<$if "))
/*      */       {
/* 1253 */         testString = testString.replace("<$if ", "<$");
/*      */       }
/* 1255 */       else if (testString.startsWith("<$elseif "))
/*      */       {
/* 1257 */         testString = testString.replace("<$elseif ", "<$");
/*      */       }
/* 1259 */       else if ((testString.equals("<$else$>")) || (testString.equals("<$endif$>")))
/*      */       {
/* 1261 */         return true;
/*      */       }
/*      */ 
/* 1264 */       dynHtml = svc.getPageMerger().parseScriptInternalEx(testString, false);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1269 */       return false;
/*      */     }
/*      */ 
/* 1272 */     if (dynHtml == null)
/*      */     {
/* 1275 */       return false;
/*      */     }
/*      */ 
/* 1278 */     for (int i = 0; i < dynHtml.m_htmlChunkArray.length; ++i)
/*      */     {
/* 1280 */       HtmlChunk chunk = dynHtml.m_htmlChunkArray[i];
/* 1281 */       GrammarElement element = chunk.m_grammarElement;
/* 1282 */       if (element == null)
/*      */       {
/* 1284 */         return false;
/*      */       }
/* 1286 */       if ((element.m_type != 2) && (element.m_type != 0))
/*      */       {
/* 1289 */         return false;
/*      */       }
/* 1291 */       if ((element.m_type != 0) && (!isIdocMethodElementSafe(element)))
/*      */       {
/* 1293 */         return false;
/*      */       }
/*      */     }
/*      */ 
/* 1297 */     return true;
/*      */   }
/*      */ 
/*      */   protected static boolean isIdocMethodElementSafe(GrammarElement element)
/*      */   {
/* 1302 */     if (element.m_id.equalsIgnoreCase("eval"))
/*      */     {
/* 1305 */       return false;
/*      */     }
/*      */ 
/* 1308 */     if (!m_safeIdocLookup.containsKey(element.m_id))
/*      */     {
/* 1311 */       return false;
/*      */     }
/*      */ 
/* 1314 */     if (element.m_subElementArray.length > 0)
/*      */     {
/* 1316 */       for (int s = 0; s < element.m_subElementArray.length; ++s)
/*      */       {
/* 1318 */         if (!isIdocParameterElementSafe(element.m_subElementArray[s]))
/*      */         {
/* 1321 */           return false;
/*      */         }
/*      */       }
/*      */     }
/* 1325 */     return true;
/*      */   }
/*      */ 
/*      */   protected static boolean isIdocParameterElementSafe(GrammarElement element)
/*      */   {
/* 1331 */     if ((element.m_type == 1) || (element.m_type == 0))
/*      */     {
/* 1333 */       return true;
/*      */     }
/* 1335 */     if (element.m_type == 2)
/*      */     {
/* 1338 */       return isIdocMethodElementSafe(element);
/*      */     }
/*      */ 
/* 1341 */     return false;
/*      */   }
/*      */ 
/*      */   static boolean encodeFullyForHtml(String in, IdcStringBuilder out, ExecutionContext cxt)
/*      */   {
/* 1352 */     boolean foundBadChar = false;
/* 1353 */     int l = in.length();
/* 1354 */     for (int i = 0; i < l; ++i)
/*      */     {
/* 1356 */       char ch = in.charAt(i);
/* 1357 */       switch (ch)
/*      */       {
/*      */       case '"':
/*      */       case '%':
/*      */       case '&':
/*      */       case '\'':
/*      */       case '(':
/*      */       case ')':
/*      */       case '<':
/*      */       case '>':
/*      */       case '\\':
/* 1368 */         if (!foundBadChar)
/*      */         {
/* 1370 */           foundBadChar = true;
/* 1371 */           out.append(in, 0, i);
/*      */         }
/* 1373 */         if (ch == '<')
/*      */         {
/* 1375 */           out.append("&lt;");
/*      */         }
/* 1377 */         else if (ch == '>')
/*      */         {
/* 1379 */           out.append("&gt;");
/*      */         }
/*      */         else
/*      */         {
/* 1383 */           out.append("&#");
/* 1384 */           out.append(Integer.toString(ch));
/* 1385 */           out.append(";");
/*      */         }
/* 1387 */         break;
/*      */       default:
/* 1389 */         if (!foundBadChar)
/*      */           continue;
/* 1391 */         out.append(ch);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1396 */     return foundBadChar;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1402 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 106526 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filterdata.HtmlFilterUtils
 * JD-Core Version:    0.5.4
 */