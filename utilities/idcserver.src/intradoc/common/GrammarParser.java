/*      */ package intradoc.common;
/*      */ 
/*      */ import intradoc.util.IdcVector;
/*      */ import java.text.DecimalFormatSymbols;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class GrammarParser
/*      */ {
/*   38 */   public static final String[] m_operators = { "=", "?", ":", "&", "join", "or", "and", "eq", "==", "ne", "!=", "ge", ">=", "le", "<=", "lt", "<", "gt", ">", "like", "+", "%", "/", "*", "not", "-" };
/*      */   public static final int m_plusMinusPriorityDiff = 5;
/*      */   public static final String m_operatorChars = "*/%+-><=&?";
/*      */   public static final String m_identifierChars = ".:#~";
/*   62 */   public static char m_decimalPoint = '.';
/*      */ 
/*   69 */   public static boolean m_disallowEmptyExpressions = false;
/*      */ 
/*   76 */   public static boolean m_isInit = false;
/*      */ 
/*      */   public static void init()
/*      */   {
/*   83 */     DecimalFormatSymbols symbols = new DecimalFormatSymbols();
/*   84 */     m_decimalPoint = symbols.getDecimalSeparator();
/*   85 */     m_isInit = true;
/*      */   }
/*      */ 
/*      */   public static GrammarElement parseElement(String str)
/*      */     throws ParseSyntaxException
/*      */   {
/*   93 */     char[] array = new char[str.length()];
/*   94 */     str.getChars(0, str.length(), array, 0);
/*   95 */     GrammarParseState parseState = new GrammarParseState(array, 0, array.length, str, 0);
/*      */ 
/*   97 */     return parseElement(parseState, null, 0, 0);
/*      */   }
/*      */ 
/*      */   public static GrammarElement parseElement(GrammarParseState parseState, String fileName, int startLine, int startOffset)
/*      */     throws ParseSyntaxException
/*      */   {
/*      */     try
/*      */     {
/*  117 */       List multiElement = null;
/*  118 */       GrammarElement retElt = null;
/*      */       while (true)
/*      */       {
/*  122 */         GrammarElement elt = parseSubElement(parseState, startLine, startOffset);
/*  123 */         if (parseState.m_terminateChar == ')')
/*      */         {
/*  125 */           createParseException(parseState.m_parseLines, parseState.m_startOffset - parseState.m_curLineOffset, "!csGrammarParserNoBeginBrace");
/*      */         }
/*      */ 
/*  130 */         if (elt != null)
/*      */         {
/*  132 */           if (multiElement != null)
/*      */           {
/*  134 */             multiElement.add(elt);
/*      */           }
/*  138 */           else if (retElt == null)
/*      */           {
/*  140 */             retElt = elt;
/*      */           }
/*      */           else
/*      */           {
/*  144 */             multiElement = new ArrayList();
/*  145 */             multiElement.add(retElt);
/*  146 */             multiElement.add(elt);
/*      */           }
/*      */ 
/*      */         }
/*  152 */         else if ((m_disallowEmptyExpressions) && (retElt != null))
/*      */         {
/*  154 */           createParseException(parseState.m_parseLines, parseState.m_startOffset - parseState.m_curLineOffset, "!csGrammarParserEmptyExpression");
/*      */         }
/*      */ 
/*  159 */         if (parseState.m_terminateChar != ',')
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  165 */         parseState.m_startOffset += 1;
/*      */       }
/*  167 */       if (multiElement != null)
/*      */       {
/*  169 */         retElt = new GrammarElement();
/*  170 */         retElt.m_subElements = multiElement;
/*  171 */         retElt.m_type = 4;
/*      */       }
/*      */ 
/*  174 */       convertSubElementsToArray(retElt);
/*      */ 
/*  176 */       return retElt;
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/*  180 */       e.m_parseInfo.m_fileName = fileName;
/*  181 */       if (!e.m_outerOffsetsUsed)
/*      */       {
/*  183 */         if (e.m_parseInfo.m_parseLine > 0)
/*      */         {
/*  185 */           e.m_parseInfo.m_parseLine += startLine;
/*      */         }
/*      */         else
/*      */         {
/*  189 */           e.m_parseInfo.m_parseLine = startLine;
/*  190 */           e.m_parseInfo.m_parseCharOffset += startOffset;
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/*  195 */         e.m_outerOffsetsUsed = false;
/*      */       }
/*  197 */       throw e;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void convertSubElementsToArray(GrammarElement elt)
/*      */   {
/*  203 */     if ((elt == null) || (elt.m_subElements == null))
/*      */     {
/*  205 */       return;
/*      */     }
/*  207 */     List subElements = elt.m_subElements;
/*      */ 
/*  212 */     int numSubElements = subElements.size();
/*  213 */     int numCapturedSubElements = numSubElements;
/*  214 */     int numNamedArguments = 0;
/*  215 */     int capturedIndex = 0;
/*  216 */     int argsIndex = 0;
/*  217 */     boolean isFunction = elt.m_type == 2;
/*  218 */     int startIndex = (isFunction) ? 0 : 1;
/*  219 */     boolean isFirstTime = isFunction;
/*  220 */     for (int j = startIndex; j < 2; ++j)
/*      */     {
/*  222 */       if (!isFirstTime)
/*      */       {
/*  224 */         if (numNamedArguments > 0)
/*      */         {
/*  226 */           elt.m_namedFunctionParameters = new GrammarElement[numNamedArguments];
/*  227 */           numCapturedSubElements -= numNamedArguments;
/*      */         }
/*  229 */         elt.m_subElementArray = new GrammarElement[numCapturedSubElements];
/*      */       }
/*      */ 
/*  233 */       for (int i = 0; i < numSubElements; ++i)
/*      */       {
/*  235 */         GrammarElement subElt = (GrammarElement)elt.m_subElements.get(i);
/*  236 */         if (!isFirstTime)
/*      */         {
/*  239 */           convertSubElementsToArray(subElt);
/*      */         }
/*  241 */         if ((isFunction) && (subElt != null) && (subElt.m_type == 3) && (subElt.m_xxOpFirstChar == '=') && (subElt.m_xxOpSecondChar == 0))
/*      */         {
/*  246 */           if (isFirstTime)
/*      */           {
/*  248 */             ++numNamedArguments;
/*      */           }
/*      */           else
/*      */           {
/*  252 */             elt.m_namedFunctionParameters[(argsIndex++)] = subElt;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  257 */           if (isFirstTime)
/*      */             continue;
/*  259 */           elt.m_subElementArray[(capturedIndex++)] = subElt;
/*      */         }
/*      */       }
/*      */ 
/*  263 */       isFirstTime = false;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static GrammarElement parseSubElement(GrammarParseState parseState, int startLine, int startOffset)
/*      */     throws ParseSyntaxException
/*      */   {
/*  283 */     int end = parseState.m_endOffset;
/*      */ 
/*  287 */     int startElt = parseState.m_startOffset;
/*  288 */     int parseLines = parseState.m_parseLines;
/*  289 */     int curLineOffset = parseState.m_curLineOffset;
/*      */ 
/*  291 */     Vector v = null;
/*  292 */     boolean insideId = false;
/*  293 */     boolean insideOp = false;
/*  294 */     boolean insideLiteral = false;
/*  295 */     boolean insideEscapeSequence = false;
/*  296 */     boolean earlyExit = false;
/*  297 */     char literalChar = '-';
/*      */ 
/*  299 */     for (int i = parseState.m_startOffset; i < end; ++i)
/*      */     {
/*  301 */       char ch = parseState.m_array[i];
/*      */ 
/*  304 */       if ((ch == '\n') || (ch == '\r'))
/*      */       {
/*  306 */         parseState.m_curLineOffset = (i + 1);
/*  307 */         if ((ch != '\n') || (!parseState.m_ignoreLf))
/*      */         {
/*  309 */           parseState.m_parseLines += 1;
/*      */         }
/*      */       }
/*  312 */       parseState.m_ignoreLf = (ch == '\r');
/*      */ 
/*  315 */       boolean isSpace = Character.isWhitespace(ch);
/*  316 */       if (v == null)
/*      */       {
/*  318 */         if (isSpace)
/*      */           continue;
/*  320 */         v = new IdcVector();
/*      */       }
/*      */ 
/*  329 */       if (insideLiteral)
/*      */       {
/*  331 */         if (insideEscapeSequence)
/*      */         {
/*  333 */           insideEscapeSequence = false;
/*      */         }
/*  335 */         else if ((ch == '\\') && (!parseState.m_isXmlLiteralEscape))
/*      */         {
/*  337 */           insideEscapeSequence = true;
/*      */         } else {
/*  339 */           if (ch != literalChar) {
/*      */             continue;
/*      */           }
/*  342 */           insideLiteral = false;
/*  343 */           String litStr = null;
/*  344 */           if (parseState.m_isXmlLiteralEscape)
/*      */           {
/*  353 */             int offset = startElt + 1;
/*  354 */             int len = i - offset;
/*  355 */             litStr = StringUtils.decodeXmlEscapeSequence(parseState.m_array, offset, len);
/*      */           }
/*      */           else
/*      */           {
/*  360 */             litStr = getSubstring(parseState, startElt + 1, i);
/*  361 */             litStr = StringUtils.decodeLiteralStringEscapeSequence(litStr);
/*      */           }
/*  363 */           GrammarElement elt = new GrammarElement(litStr, 1, parseLines, startElt - curLineOffset, i - startElt + 1);
/*      */ 
/*  367 */           adjustElementLocation(startLine, startOffset, elt);
/*  368 */           v.addElement(elt);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  374 */         boolean isTerminatingElt = false;
/*  375 */         boolean isIdPart = false;
/*  376 */         boolean isOpChar = false;
/*  377 */         boolean isQuoteChar = false;
/*  378 */         if (!isSpace)
/*      */         {
/*  382 */           if (ch == '!')
/*      */           {
/*  384 */             if (i < end - 1)
/*      */             {
/*  386 */               if (parseState.m_array[(i + 1)] == '=')
/*      */               {
/*  388 */                 isOpChar = true;
/*      */               }
/*  390 */               else if (insideId)
/*      */               {
/*  392 */                 isIdPart = true;
/*      */               }
/*      */             }
/*      */           }
/*  396 */           else if ((ch == ':') && (!insideId))
/*      */           {
/*  401 */             isOpChar = true;
/*      */           }
/*      */           else
/*      */           {
/*  405 */             isIdPart = isIdentifierChar(ch);
/*  406 */             if (!isIdPart)
/*      */             {
/*  408 */               isOpChar = isOperatorChar(ch);
/*      */             }
/*  410 */             if (!isOpChar)
/*      */             {
/*  412 */               isQuoteChar = (ch == '\'') || (ch == '"');
/*      */             }
/*      */           }
/*      */         }
/*  416 */         boolean isStandardChar = (isIdPart) || (isSpace) || (isOpChar);
/*      */ 
/*  419 */         if (insideOp)
/*      */         {
/*  422 */           if (ch != '=')
/*      */           {
/*  424 */             isTerminatingElt = true;
/*      */           }
/*      */         }
/*  427 */         else if ((insideId) && 
/*  429 */           (!isIdPart) && (ch != '.'))
/*      */         {
/*  431 */           isTerminatingElt = true;
/*      */         }
/*      */ 
/*  436 */         GrammarElement elt = null;
/*  437 */         if (isTerminatingElt)
/*      */         {
/*  439 */           elt = createElement(parseState, insideOp, insideId, startElt, parseLines, startElt - curLineOffset, i);
/*      */ 
/*  443 */           adjustElementLocation(startLine, startOffset, elt);
/*  444 */           v.addElement(elt);
/*      */         }
/*      */ 
/*  448 */         boolean isHandled = false;
/*  449 */         if (!isStandardChar)
/*      */         {
/*  451 */           if (ch == '(')
/*      */           {
/*  454 */             parseState.m_startOffset = (i + 1);
/*  455 */             if (insideId)
/*      */             {
/*  457 */               if (elt != null)
/*      */               {
/*  459 */                 elt.m_type = 2;
/*  460 */                 elt.m_subElements = new ArrayList();
/*      */               }
/*      */ 
/*  463 */               boolean foundComma = false;
/*  464 */               while (parseState.m_startOffset < end)
/*      */               {
/*  466 */                 GrammarElement subElt = parseSubElement(parseState, startLine, startOffset);
/*      */ 
/*  468 */                 if (elt != null)
/*      */                 {
/*  470 */                   if (subElt != null)
/*      */                   {
/*  472 */                     elt.m_subElements.add(subElt);
/*      */                   }
/*  474 */                   else if (foundComma)
/*      */                   {
/*  476 */                     createParseException(parseState.m_parseLines, parseState.m_startOffset - parseState.m_curLineOffset, "!csGrammarParserEmptyArgument");
/*      */                   }
/*      */ 
/*      */                 }
/*      */ 
/*  483 */                 if (parseState.m_terminateChar != ',')
/*      */                 {
/*  485 */                   if (parseState.m_terminateChar == ')')
/*      */                     break;
/*  487 */                   createParseException(parseState.m_parseLines, parseState.m_startOffset - parseState.m_curLineOffset, "!csGrammarParserNoEndBrace"); break;
/*      */                 }
/*      */ 
/*  493 */                 foundComma = true;
/*  494 */                 parseState.m_startOffset += 1;
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  499 */               int startingLines = parseState.m_parseLines;
/*  500 */               int startingLineOffset = parseState.m_curLineOffset;
/*  501 */               GrammarElement nestedElt = parseSubElement(parseState, startLine, startOffset);
/*      */ 
/*  503 */               if (parseState.m_terminateChar != ')')
/*      */               {
/*  505 */                 createParseException(startingLines, i - startingLineOffset, "!csGrammarParserNoEndBrace");
/*      */               }
/*      */ 
/*  508 */               if (nestedElt != null)
/*      */               {
/*  510 */                 v.addElement(nestedElt);
/*      */               }
/*      */             }
/*  513 */             i = parseState.m_startOffset;
/*  514 */             isHandled = true;
/*      */           }
/*  516 */           else if ((ch == ',') || (ch == ')'))
/*      */           {
/*  519 */             parseState.m_startOffset = i;
/*  520 */             parseState.m_terminateChar = ch;
/*  521 */             earlyExit = true;
/*  522 */             isHandled = true;
/*      */           }
/*      */         }
/*      */ 
/*  526 */         if (!isHandled)
/*      */         {
/*  529 */           if (isTerminatingElt)
/*      */           {
/*  531 */             if ((!isStandardChar) && (!isQuoteChar))
/*      */             {
/*  533 */               createParseException(parseState.m_parseLines, i - parseState.m_curLineOffset, LocaleUtils.encodeMessage("csGrammarParserIllegalTerminatingToken", null, new Character(ch)));
/*      */             }
/*      */ 
/*  538 */             isTerminatingElt = false;
/*  539 */             insideId = false;
/*  540 */             insideOp = false;
/*      */           }
/*      */ 
/*  543 */           boolean isStartingElt = false;
/*  544 */           if (isStandardChar)
/*      */           {
/*  547 */             if ((isIdPart == true) && (!insideId))
/*      */             {
/*  549 */               insideId = true;
/*  550 */               isStartingElt = true;
/*      */             }
/*  552 */             else if ((isOpChar == true) && (!insideOp))
/*      */             {
/*  554 */               insideOp = true;
/*  555 */               isStartingElt = true;
/*      */             }
/*      */ 
/*      */           }
/*  560 */           else if (isQuoteChar)
/*      */           {
/*  562 */             insideLiteral = true;
/*  563 */             literalChar = ch;
/*  564 */             isStartingElt = true;
/*      */           }
/*      */           else
/*      */           {
/*  568 */             createParseException(parseState.m_parseLines, i - parseState.m_curLineOffset, LocaleUtils.encodeMessage("csGrammarParserIllegalChar", null, new Character(ch)));
/*      */           }
/*      */ 
/*  575 */           if (isStartingElt)
/*      */           {
/*  577 */             startElt = i;
/*  578 */             parseLines = parseState.m_parseLines;
/*  579 */             curLineOffset = parseState.m_curLineOffset;
/*      */           }
/*      */         }
/*      */ 
/*  583 */         if (isTerminatingElt)
/*      */         {
/*  585 */           insideId = false;
/*  586 */           insideOp = false;
/*  587 */           isTerminatingElt = false;
/*      */         }
/*  589 */         if (earlyExit) {
/*      */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  597 */     if (insideLiteral)
/*      */     {
/*  599 */       createParseException(parseLines, startElt - curLineOffset, LocaleUtils.encodeMessage("csGrammarParserLiteralMissingClosing", null, new Character(literalChar)));
/*      */     }
/*      */ 
/*  603 */     if ((insideId) || (insideOp))
/*      */     {
/*  606 */       GrammarElement elt = createElement(parseState, insideOp, insideId, startElt, parseLines, startElt - curLineOffset, end);
/*      */ 
/*  610 */       adjustElementLocation(startLine, startOffset, elt);
/*  611 */       v.addElement(elt);
/*      */     }
/*      */ 
/*  614 */     if (!earlyExit)
/*      */     {
/*  616 */       parseState.m_startOffset = end;
/*  617 */       parseState.m_terminateChar = '\000';
/*      */     }
/*      */ 
/*  621 */     if ((v == null) || (v.size() == 0))
/*      */     {
/*  623 */       return null;
/*      */     }
/*      */ 
/*  628 */     GrammarElement retElt = collapseElements(parseState, v);
/*      */ 
/*  630 */     return retElt;
/*      */   }
/*      */ 
/*      */   public static GrammarElement collapseElements(GrammarParseState parseState, Vector v)
/*      */     throws ParseSyntaxException
/*      */   {
/*  639 */     GrammarElement retElt = null;
/*      */     while (true)
/*      */     {
/*  644 */       int priority = -1;
/*  645 */       int selectedIndex = -1;
/*      */ 
/*  649 */       int curSize = v.size();
/*  650 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/*  652 */         GrammarElement elt = (GrammarElement)v.elementAt(i);
/*  653 */         if ((elt.m_priority <= priority) || (elt.m_type != 3) || (elt.m_subElements != null)) {
/*      */           continue;
/*      */         }
/*  656 */         selectedIndex = i;
/*  657 */         priority = elt.m_priority;
/*      */       }
/*      */ 
/*  661 */       if (selectedIndex < 0)
/*      */       {
/*  663 */         retElt = (GrammarElement)v.elementAt(0);
/*  664 */         if (curSize == 1)
/*      */           break;
/*  666 */         GrammarElement errElt = getRepresentativeElement(retElt);
/*  667 */         createParseExceptionElt(errElt, "!csGrammarParserNoOperatorToCombine");
/*  668 */         break;
/*      */       }
/*      */ 
/*  673 */       GrammarElement elt = (GrammarElement)v.elementAt(selectedIndex);
/*  674 */       if (selectedIndex == curSize - 1)
/*      */       {
/*  676 */         createParseExceptionElt(elt, "!csGrammarParserOperatorNoFollowingOperand");
/*      */       }
/*  678 */       boolean isAdditiveNumeric = isAdditiveNumeric(elt);
/*  679 */       boolean isAdditiveBoolean = isAdditiveBoolean(elt);
/*  680 */       boolean canBeUni = (isAdditiveNumeric) || (isAdditiveBoolean);
/*  681 */       if ((!canBeUni) && (selectedIndex == 0))
/*      */       {
/*  683 */         createParseExceptionElt(elt, "!csGrammarParserOperatorNotPrecededByOperand");
/*      */       }
/*      */ 
/*  687 */       GrammarElement afterElt = (GrammarElement)v.elementAt(selectedIndex + 1);
/*  688 */       boolean combined = false;
/*  689 */       if ((afterElt.m_type == 3) && (afterElt.m_subElements == null))
/*      */       {
/*  692 */         boolean isAfterAddNumeric = isAdditiveNumeric(afterElt);
/*  693 */         if (afterElt.m_id.equals("+"))
/*      */         {
/*  696 */           selectedIndex += 1;
/*  697 */           combined = true;
/*      */         }
/*  699 */         else if (isAdditiveNumeric)
/*      */         {
/*  701 */           if (isAfterAddNumeric)
/*      */           {
/*  703 */             afterElt.m_id = ((afterElt.m_id.equals(elt.m_id)) ? "+" : "-");
/*  704 */             combined = true;
/*      */           }
/*      */         }
/*  707 */         else if ((isAdditiveBoolean) && 
/*  709 */           (isAdditiveBoolean(afterElt)))
/*      */         {
/*  711 */           v.removeElementAt(selectedIndex + 1);
/*  712 */           combined = true;
/*      */         }
/*      */ 
/*  715 */         if (!combined)
/*      */         {
/*  717 */           createParseExceptionElt(elt, "!csGrammarParserOperatorFollowedByOperator");
/*      */         }
/*      */       }
/*  720 */       else if (selectedIndex == 0)
/*      */       {
/*  722 */         afterElt.m_uniOperator = elt.m_id;
/*  723 */         combined = true;
/*      */       }
/*  725 */       if (combined)
/*      */       {
/*  727 */         v.removeElementAt(selectedIndex);
/*      */       }
/*      */ 
/*  731 */       GrammarElement beforeElt = (GrammarElement)v.elementAt(selectedIndex - 1);
/*  732 */       if ((beforeElt.m_type == 3) && (beforeElt.m_subElements == null))
/*      */       {
/*  735 */         if (isAdditiveNumeric)
/*      */         {
/*  739 */           boolean isBeforeAddNumeric = isAdditiveNumeric(beforeElt);
/*  740 */           if (isBeforeAddNumeric)
/*      */           {
/*  742 */             beforeElt.m_id = ((beforeElt.m_id.equals(elt.m_id)) ? "+" : "-");
/*  743 */             beforeElt.m_xxOpFirstChar = Character.toLowerCase(beforeElt.m_id.charAt(0));
/*      */           }
/*  747 */           else if (elt.m_id.equals("-"))
/*      */           {
/*  749 */             afterElt.m_uniOperator = "-";
/*      */           }
/*      */ 
/*  752 */           combined = true;
/*      */         }
/*  754 */         else if (isAdditiveBoolean)
/*      */         {
/*  759 */           if (isAdditiveBoolean(beforeElt))
/*      */           {
/*  762 */             v.removeElementAt(selectedIndex - 1);
/*      */           }
/*      */           else
/*      */           {
/*  766 */             afterElt.m_uniOperator = elt.m_id;
/*      */           }
/*  768 */           combined = true;
/*      */         }
/*  770 */         if (!combined)
/*      */         {
/*  772 */           createParseExceptionElt(elt, "!csGrammarParserOperatorFollowedByOperator");
/*      */         }
/*      */       }
/*  775 */       else if (isAdditiveBoolean)
/*      */       {
/*  777 */         afterElt.m_uniOperator = elt.m_id;
/*  778 */         combined = true;
/*      */       }
/*  780 */       if (combined)
/*      */       {
/*  782 */         v.removeElementAt(selectedIndex);
/*      */       }
/*      */ 
/*  787 */       if (elt.m_id.equals("-"))
/*      */       {
/*  789 */         elt.m_id = "+";
/*  790 */         elt.m_xxOpFirstChar = '+';
/*  791 */         elt.m_priority -= 5;
/*  792 */         afterElt.m_uniOperator = "-";
/*      */       }
/*      */ 
/*  802 */       if (elt.m_id.equals("?"))
/*      */       {
/*  805 */         if (!afterElt.m_id.equals(":"))
/*      */         {
/*  807 */           createParseExceptionElt(elt, "!csGrammarParserChoiceOperatorNotFollowedByChoices");
/*      */         }
/*      */       }
/*  810 */       else if (elt.m_id.equals(":"))
/*      */       {
/*  815 */         if (selectedIndex < 2)
/*      */         {
/*  817 */           createParseExceptionElt(elt, "!csGrammarParserChoiceOperatorNotPresentForChoices");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  822 */         validateOperand(parseState, elt.m_id, beforeElt, true);
/*  823 */         validateOperand(parseState, elt.m_id, afterElt, false);
/*      */       }
/*      */ 
/*  827 */       elt.m_subElements = new ArrayList(2);
/*  828 */       elt.m_subElements.add(beforeElt);
/*  829 */       elt.m_subElements.add(afterElt);
/*  830 */       v.removeElementAt(selectedIndex + 1);
/*  831 */       v.removeElementAt(selectedIndex - 1);
/*      */     }
/*      */ 
/*  834 */     return retElt;
/*      */   }
/*      */ 
/*      */   public static GrammarElement getRepresentativeElement(GrammarElement elt)
/*      */   {
/*  839 */     List v = elt.m_subElements;
/*  840 */     if (v == null)
/*      */     {
/*  842 */       return elt;
/*      */     }
/*  844 */     int n = v.size();
/*  845 */     if (n == 0)
/*      */     {
/*  847 */       return elt;
/*      */     }
/*  849 */     GrammarElement subElement = (GrammarElement)v.get(n - 1);
/*  850 */     return getRepresentativeElement(subElement);
/*      */   }
/*      */ 
/*      */   public static boolean isAdditiveNumeric(GrammarElement elt)
/*      */   {
/*  855 */     return (elt.m_id.equals("-")) || (elt.m_id.equals("+"));
/*      */   }
/*      */ 
/*      */   public static boolean isAdditiveBoolean(GrammarElement elt)
/*      */   {
/*  860 */     return elt.m_id.equalsIgnoreCase("not");
/*      */   }
/*      */ 
/*      */   public static void validateOperand(GrammarParseState parseState, String op, GrammarElement operand, boolean isLeftOperand)
/*      */     throws ParseSyntaxException
/*      */   {
/*  866 */     if (op.equals("="))
/*      */     {
/*  868 */       if ((!isLeftOperand) || 
/*  871 */         (operand.m_type == 0))
/*      */         return;
/*  873 */       String msg = null;
/*  874 */       if (operand.m_type == 2)
/*      */       {
/*  876 */         msg = "!csGrammarParserInvalidFunctionCall";
/*      */       }
/*  878 */       else if (operand.m_type == 3)
/*      */       {
/*  880 */         String operandOp = operand.m_id;
/*  881 */         if ((operand.m_subElements != null) && (operand.m_subElements.size() > 0))
/*      */         {
/*  883 */           GrammarElement rightHand = (GrammarElement)operand.m_subElements.get(operand.m_subElements.size() - 1);
/*      */ 
/*  885 */           if ((rightHand != null) && (rightHand.m_uniOperator != null))
/*      */           {
/*  887 */             operandOp = rightHand.m_uniOperator;
/*      */           }
/*      */         }
/*  890 */         msg = LocaleUtils.encodeMessage("csGrammarParserInvalidVariable", null, operandOp);
/*      */       }
/*      */       else
/*      */       {
/*  895 */         msg = "!csGrammarParserProperVariableRequired";
/*      */       }
/*  897 */       createParseExceptionElt(operand, msg);
/*      */     }
/*      */     else
/*      */     {
/*  903 */       if ((operand.m_type != 1) || (operand.m_idContentType != 0) || (op.equals("&")) || (op.equals("join")) || (op.equals("like"))) {
/*      */         return;
/*      */       }
/*      */ 
/*  907 */       String msg = null;
/*      */ 
/*  909 */       if (isLeftOperand)
/*      */       {
/*  911 */         msg = LocaleUtils.encodeMessage("csGrammarParserOperandPrecedingInvalid", null, op);
/*      */       }
/*      */       else
/*      */       {
/*  915 */         msg = LocaleUtils.encodeMessage("csGrammarParserOperandFollowingInvalid", null, op);
/*      */       }
/*      */ 
/*  918 */       createParseExceptionElt(operand, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static GrammarElement createElement(GrammarParseState parseState, boolean insideOp, boolean insideId, int startElt, int lineCount, int lineOffset, int end)
/*      */     throws ParseSyntaxException
/*      */   {
/*  926 */     String id = getSubstring(parseState, startElt, end);
/*      */ 
/*  929 */     int priority = -1;
/*  930 */     int type = 0;
/*      */ 
/*  932 */     for (int i = 0; i < m_operators.length; ++i)
/*      */     {
/*  934 */       if (!id.equalsIgnoreCase(m_operators[i]))
/*      */         continue;
/*  936 */       priority = i;
/*  937 */       type = 3;
/*  938 */       id = m_operators[i];
/*  939 */       break;
/*      */     }
/*      */ 
/*  943 */     if ((type == 0) && (insideOp == true))
/*      */     {
/*  945 */       createParseException(lineCount, lineOffset, "!csGrammarParserUnknownOperator");
/*      */     }
/*      */ 
/*  948 */     GrammarElement elt = new GrammarElement(id, type, lineCount, lineOffset, id.length());
/*  949 */     elt.m_priority = priority;
/*      */ 
/*  952 */     int idType = determineFormatContent(parseState.m_array, startElt, end);
/*  953 */     if (idType != 0)
/*      */     {
/*  955 */       elt.m_idContentType = idType;
/*  956 */       elt.m_type = 1;
/*      */     }
/*      */ 
/*  961 */     return elt;
/*      */   }
/*      */ 
/*      */   public static void adjustElementLocation(int startLine, int startOffset, GrammarElement elt)
/*      */   {
/*  967 */     if (elt.m_parseLines > 0)
/*      */     {
/*  969 */       elt.m_parseLines += startLine;
/*      */     }
/*      */     else
/*      */     {
/*  973 */       elt.m_parseLines = startLine;
/*  974 */       elt.m_lineCharOffset += startOffset;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void createParseException(int line, int offset, String msg)
/*      */     throws ParseSyntaxException
/*      */   {
/*  982 */     createParseExceptionEx(line, offset, msg, false);
/*      */   }
/*      */ 
/*      */   public static void createParseExceptionElt(GrammarElement elt, String msg)
/*      */     throws ParseSyntaxException
/*      */   {
/*  990 */     createParseExceptionEx(elt.m_parseLines, elt.m_lineCharOffset, msg, true);
/*      */   }
/*      */ 
/*      */   public static void createParseExceptionEx(int line, int offset, String msg, boolean outerOffsetsUsed)
/*      */     throws ParseSyntaxException
/*      */   {
/*  998 */     ParseLocationInfo parseInfo = new ParseLocationInfo();
/*  999 */     parseInfo.m_parseCharOffset = offset;
/* 1000 */     parseInfo.m_parseLine = line;
/* 1001 */     throw new ParseSyntaxException(parseInfo, msg, outerOffsetsUsed);
/*      */   }
/*      */ 
/*      */   public static String getSubstring(GrammarParseState parseState, int startOffset, int endOffset)
/*      */   {
/* 1014 */     return parseState.m_parseStr.substring(startOffset - parseState.m_parseStrOffset, endOffset - parseState.m_parseStrOffset);
/*      */   }
/*      */ 
/*      */   public static boolean isOperatorChar(char ch)
/*      */   {
/* 1023 */     return "*/%+-><=&?".indexOf(ch) >= 0;
/*      */   }
/*      */ 
/*      */   public static boolean isIdentifierChar(char ch)
/*      */   {
/* 1031 */     return (Character.isJavaIdentifierPart(ch)) || (".:#~".indexOf(ch) >= 0);
/*      */   }
/*      */ 
/*      */   public static int determineFormatContent(char[] buf, int offset, int end)
/*      */   {
/* 1044 */     if (offset >= end)
/*      */     {
/* 1047 */       return 0;
/*      */     }
/*      */ 
/* 1051 */     if (!m_isInit)
/*      */     {
/* 1053 */       init();
/*      */     }
/*      */ 
/* 1057 */     int retVal = 1;
/* 1058 */     boolean foundDecimalPoint = false;
/* 1059 */     for (int i = offset; i < end; ++i)
/*      */     {
/* 1062 */       if (buf[i] == m_decimalPoint)
/*      */       {
/* 1064 */         if (foundDecimalPoint)
/*      */         {
/* 1067 */           return 0;
/*      */         }
/* 1069 */         foundDecimalPoint = true;
/*      */       }
/* 1073 */       else if (!Character.isDigit(buf[i]))
/*      */       {
/* 1075 */         return 0;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1080 */     if (foundDecimalPoint)
/*      */     {
/* 1082 */       retVal = 2;
/*      */     }
/*      */ 
/* 1085 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static void translateSimpleGrammarExpressionToBitFlagChanges(GrammarElement elt, int[] flags, String[] keys, boolean isRemoving)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1093 */     if (elt.m_type == 4)
/*      */     {
/* 1095 */       if (elt.m_subElementArray == null)
/*      */         return;
/* 1097 */       for (int i = 0; i < elt.m_subElementArray.length; ++i)
/*      */       {
/* 1099 */         GrammarElement subElement = elt.m_subElementArray[i];
/* 1100 */         boolean isSubRemoving = isRemoving;
/* 1101 */         if (subElement.m_type == 4)
/*      */         {
/* 1104 */           if ((subElement.m_uniOperator != null) && ((
/* 1106 */             (subElement.m_uniOperator.equals("not")) || (subElement.m_uniOperator.equals("-")))))
/*      */           {
/* 1108 */             isSubRemoving = !isSubRemoving;
/*      */           }
/*      */ 
/* 1111 */           translateSimpleGrammarExpressionToBitFlagChanges(subElement, flags, keys, isSubRemoving);
/* 1112 */           isSubRemoving = isRemoving;
/*      */         }
/*      */         else
/*      */         {
/* 1116 */           translateEvalGrammarElementToBitFlagChanges(subElement, flags, keys, isSubRemoving);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1123 */       translateEvalGrammarElementToBitFlagChanges(elt, flags, keys, isRemoving);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void translateEvalGrammarElementToBitFlagChanges(GrammarElement elt, int[] flags, String[] keys, boolean isRemoving)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1134 */     if (elt.m_type != 0)
/*      */     {
/* 1136 */       createParseException(elt.m_parseLines, elt.m_lineCharOffset, "!csGrammarParserIllegalChar");
/*      */     }
/* 1138 */     if ((elt.m_uniOperator != null) && ((
/* 1140 */       (elt.m_uniOperator.equals("not")) || (elt.m_uniOperator.equals("-")))))
/*      */     {
/* 1142 */       isRemoving = !isRemoving;
/*      */     }
/*      */ 
/* 1146 */     int index = StringUtils.findStringIndex(keys, elt.m_id);
/* 1147 */     if (index < 0)
/*      */       return;
/* 1149 */     int bitFlag = 1 << index;
/* 1150 */     int flagIndex = (isRemoving) ? 1 : 0;
/* 1151 */     flags[flagIndex] |= bitFlag;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1159 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73956 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.GrammarParser
 * JD-Core Version:    0.5.4
 */