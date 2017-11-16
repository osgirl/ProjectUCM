/*      */ package intradoc.common;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.List;
/*      */ 
/*      */ public class DynamicHtml
/*      */ {
/*      */   public static final int F_LOAD_INCLUDE = 0;
/*      */   public static final int F_LOAD_DATA = 1;
/*      */   public List m_htmlChunks;
/*      */   public HtmlChunk[] m_htmlChunkArray;
/*      */   public String m_sourceEncoding;
/*      */   public long m_timeStamp;
/*      */   protected List m_interiorNestingTracker;
/*      */   protected int m_exteriorNestingLevel;
/*      */   protected List m_exteriorNestingTracker;
/*      */   protected int m_activeDirectives;
/*      */   protected String[] m_preservedParameters;
/*      */   public DynamicHtml m_priorScript;
/*      */   public String m_tempKey;
/*      */   public DynamicHtml m_capturedVersion;
/*      */   public int m_parseLine;
/*      */   public int m_parseCharOffset;
/*      */   public String m_fileName;
/*      */   public String m_resourceString;
/*  145 */   public ArrayList m_data = null;
/*      */ 
/*  150 */   protected boolean m_isInData = false;
/*      */   public static final int m_maximumExecutionRecursion = 100;
/*  163 */   public boolean m_isNotParseGrammar = false;
/*      */ 
/*  168 */   public int m_parsingFlags = 0;
/*      */ 
/*  175 */   public boolean m_isMergedDynamicData = false;
/*      */   public DynamicData m_dynamicData;
/*      */   public Object m_extraData;
/*      */ 
/*      */   public DynamicHtml()
/*      */   {
/*  192 */     this.m_htmlChunks = new ArrayList();
/*  193 */     this.m_interiorNestingTracker = new ArrayList();
/*  194 */     this.m_interiorNestingTracker.add(new ArrayList());
/*  195 */     this.m_exteriorNestingTracker = new ArrayList();
/*  196 */     this.m_exteriorNestingLevel = 0;
/*  197 */     this.m_priorScript = null;
/*  198 */     this.m_tempKey = null;
/*  199 */     this.m_capturedVersion = null;
/*  200 */     this.m_fileName = null;
/*  201 */     this.m_resourceString = null;
/*  202 */     this.m_sourceEncoding = null;
/*  203 */     this.m_timeStamp = 0L;
/*  204 */     this.m_activeDirectives = DynamicHtmlStatic.m_defaultDirectives;
/*  205 */     this.m_data = new ArrayList();
/*      */   }
/*      */ 
/*      */   public void loadHtml(Reader reader, String fileName, boolean isXmlSyntax)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  214 */     loadHtmlEx(reader, fileName, isXmlSyntax, null);
/*      */   }
/*      */ 
/*      */   public void loadHtmlEx(Reader reader, String fileName, boolean isXmlSyntax, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  220 */     ParseOutput parseOutput = new ParseOutput();
/*      */     try
/*      */     {
/*  223 */       parseOutput.m_parseInfo.m_fileName = fileName;
/*  224 */       setIsXmlSyntax(isXmlSyntax);
/*  225 */       loadHtmlInContextEx(reader, null, parseOutput, 0, bp);
/*      */     }
/*      */     finally
/*      */     {
/*  229 */       parseOutput.releaseBuffers();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void loadHtmlInContext(Reader reader, ParseOutput parseOutput)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  240 */     loadHtmlInContextEx(reader, null, parseOutput, 0, null);
/*      */   }
/*      */ 
/*      */   public void loadHtmlInContextEx(Reader reader, ResourceOptions resOptions, ParseOutput parseOutput, int loadType, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  247 */     DynamicHtmlStatic.checkInit(parseOutput);
/*  248 */     int[] mergeFlags = null;
/*  249 */     if (resOptions != null)
/*      */     {
/*  251 */       mergeFlags = resOptions.m_mergeFlags;
/*  252 */       if (resOptions.m_preservedParameters != null)
/*      */       {
/*  254 */         this.m_preservedParameters = StringUtils.convertListToArray(resOptions.m_preservedParameters);
/*      */       }
/*      */     }
/*  257 */     updateActiveDirectives(mergeFlags, parseOutput);
/*  258 */     this.m_parsingFlags = loadType;
/*      */ 
/*  261 */     this.m_fileName = parseOutput.m_parseInfo.m_fileName;
/*      */ 
/*  263 */     this.m_parseCharOffset = parseOutput.m_parseInfo.m_parseCharOffset;
/*  264 */     this.m_parseLine = parseOutput.m_parseInfo.m_parseLine;
/*      */ 
/*  266 */     if ((this.m_parsingFlags & 0x1) != 0)
/*      */     {
/*  272 */       DynamicData dynData = new DynamicData();
/*  273 */       dynData.parse(reader, this, parseOutput);
/*  274 */       this.m_dynamicData = dynData;
/*      */     }
/*      */     else
/*      */     {
/*  279 */       IdcCharArrayWriter outbuf = new IdcCharArrayWriter();
/*      */       try
/*      */       {
/*  282 */         Writer oldWriter = parseOutput.m_writer;
/*  283 */         parseOutput.m_writer = outbuf;
/*      */ 
/*  286 */         while (findScriptTag(reader, parseOutput) == true)
/*      */         {
/*  289 */           addChunks(reader, outbuf, parseOutput, bp);
/*      */         }
/*      */ 
/*  293 */         parseOutput.copyToPending(true, false);
/*  294 */         addChunks(reader, outbuf, parseOutput, bp);
/*      */ 
/*  297 */         if (this.m_exteriorNestingLevel > 0)
/*      */         {
/*  299 */           createNestingException(this.m_exteriorNestingTracker, "!csDynHTMLUnterminatedLoop");
/*      */         }
/*      */ 
/*  302 */         List curIfList = (List)this.m_interiorNestingTracker.get(0);
/*  303 */         if (curIfList.size() > 0)
/*      */         {
/*  305 */           createNestingException(curIfList, "!csDynHTMLUnterminatedIf");
/*      */         }
/*  307 */         parseOutput.m_writer = oldWriter;
/*      */ 
/*  309 */         int numChunks = this.m_htmlChunks.size();
/*  310 */         this.m_htmlChunkArray = new HtmlChunk[numChunks];
/*  311 */         for (int i = 0; i < numChunks; ++i)
/*      */         {
/*  313 */           HtmlChunk htmlChunk = (HtmlChunk)this.m_htmlChunks.get(i);
/*  314 */           this.m_htmlChunkArray[i] = htmlChunk;
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/*  319 */         outbuf.releaseBuffers();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean findScriptTag(Reader reader, ParseOutput parseOutput)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  327 */     if (parseOutput.m_isXmlLiteralEscape)
/*      */     {
/*  331 */       char[] startChars = (this.m_isInData) ? DynamicHtmlStatic.XML_SIMPLE_SCRIPT_TAG_START_CHARS : DynamicHtmlStatic.XML_SCRIPT_TAG_START_CHARS;
/*      */ 
/*  333 */       return Parser.findGenericTagPrefix(reader, parseOutput, startChars, DynamicHtmlStatic.XML_SCRIPT_TAG_END_CHARS, DynamicHtmlStatic.XML_SCRIPT_TAG);
/*      */     }
/*      */ 
/*  336 */     return Parser.findScriptTag(reader, parseOutput, '$', DynamicHtmlStatic.NORMAL_SCRIPT_START_COMMENT_CHARS, DynamicHtmlStatic.NORMAL_SCRIPT_END_COMMENT_CHARS);
/*      */   }
/*      */ 
/*      */   protected void addChunks(Reader reader, IdcCharArrayWriter outbuf, ParseOutput parseOutput, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  348 */     parseOutput.writePending();
/*      */ 
/*  351 */     if (outbuf.size() > 0)
/*      */     {
/*  353 */       HtmlChunk chunk = createHtmlTextChunk(parseOutput, outbuf, bp);
/*  354 */       this.m_htmlChunks.add(chunk);
/*      */ 
/*  356 */       outbuf.reset();
/*      */     }
/*      */ 
/*  359 */     addScriptChunk(parseOutput, bp);
/*      */ 
/*  362 */     parseOutput.copyToPending(false, false);
/*      */   }
/*      */ 
/*      */   protected void addScriptChunk(ParseOutput parseOutput, IdcBreakpoints bp)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  368 */     if (this.m_isNotParseGrammar)
/*      */     {
/*  371 */       int scriptTagLen = DynamicHtmlStatic.XML_SCRIPT_TAG.length;
/*  372 */       int numCopy = parseOutput.m_numWaiting;
/*  373 */       if (numCopy > 0)
/*      */       {
/*  375 */         numCopy -= scriptTagLen + 2 + 1;
/*      */       }
/*  377 */       int startCopy = parseOutput.m_readOffset + scriptTagLen;
/*  378 */       IdcStringBuilder idcBuilder = new IdcStringBuilder();
/*      */ 
/*  381 */       idcBuilder.append(parseOutput.m_outputBuf, startCopy, numCopy);
/*      */ 
/*  384 */       HtmlChunk chunk = new HtmlChunk(bp);
/*  385 */       chunk.m_chunkType = 12;
/*  386 */       chunk.m_chars = idcBuilder.toCharArray();
/*      */ 
/*  388 */       this.m_htmlChunks.add(chunk);
/*  389 */       return;
/*      */     }
/*      */ 
/*  395 */     String tag = parseOutput.waitingBufferAsString();
/*      */ 
/*  398 */     char[] array = parseOutput.m_outputBuf;
/*  399 */     int offset = parseOutput.m_readOffset;
/*  400 */     int start = offset;
/*  401 */     int end = offset + tag.length();
/*  402 */     if (parseOutput.m_isXmlLiteralEscape)
/*      */     {
/*  404 */       start += DynamicHtmlStatic.XML_SCRIPT_TAG.length;
/*  405 */       end -= 2;
/*      */     }
/*  407 */     if (start >= end)
/*      */     {
/*  409 */       return;
/*      */     }
/*      */ 
/*  412 */     GrammarParseState parseState = new GrammarParseState(array, offset, end, tag, offset);
/*      */ 
/*  414 */     parseState.m_isXmlLiteralEscape = parseOutput.m_isXmlLiteralEscape;
/*      */ 
/*  417 */     String token = null;
/*  418 */     boolean inTag = false;
/*      */ 
/*  420 */     for (int i = start; i < end; ++i)
/*      */     {
/*  422 */       char ch = array[i];
/*  423 */       if (ch <= ' ')
/*      */       {
/*  425 */         if (!inTag)
/*      */           continue;
/*  427 */         token = tag.substring(start - offset, i - offset);
/*  428 */         inTag = false;
/*  429 */         break;
/*      */       }
/*      */ 
/*  434 */       if (inTag)
/*      */         continue;
/*  436 */       inTag = true;
/*  437 */       start = i;
/*      */     }
/*      */ 
/*  441 */     if (inTag)
/*      */     {
/*  443 */       token = tag.substring(start - offset, end - offset);
/*      */     }
/*  445 */     if (token == null)
/*      */     {
/*  447 */       if (GrammarParser.m_disallowEmptyExpressions)
/*      */       {
/*  449 */         parseOutput.createParsingException("!csDynHTMLEmptyExpression");
/*      */       }
/*  451 */       return;
/*      */     }
/*      */ 
/*  454 */     HtmlChunk chunk = new HtmlChunk(bp);
/*  455 */     boolean foundMatch = false;
/*      */ 
/*  457 */     String[] options = HtmlChunk.CHUNK_TYPES;
/*      */ 
/*  460 */     for (int i = 0; i < options.length; ++i)
/*      */     {
/*  462 */       if (!options[i].equals(token))
/*      */         continue;
/*  464 */       chunk.m_chunkType = i;
/*  465 */       foundMatch = true;
/*  466 */       break;
/*      */     }
/*      */ 
/*  472 */     if (chunk.m_chunkType == 16)
/*      */     {
/*  474 */       return;
/*      */     }
/*      */ 
/*  478 */     chunk.m_parseCharOffset = parseOutput.m_parseInfo.m_parseCharOffset;
/*  479 */     chunk.m_parseLine = parseOutput.m_parseInfo.m_parseLine;
/*  480 */     chunk.m_fileName = parseOutput.m_parseInfo.m_fileName;
/*      */ 
/*  483 */     if (foundMatch)
/*      */     {
/*  485 */       parseState.m_startOffset = (start + token.length());
/*      */     }
/*      */     else
/*      */     {
/*  491 */       parseState.m_startOffset = start;
/*  492 */       chunk.m_chunkType = 1;
/*      */     }
/*      */ 
/*  495 */     chunk.m_grammarElement = parseGrammarStatement(chunk, parseState);
/*  496 */     this.m_htmlChunks.add(chunk);
/*      */ 
/*  499 */     Object[] customParseState = (Object[])(Object[])parseOutput.m_customParseState;
/*  500 */     if (customParseState == null)
/*      */     {
/*  502 */       customParseState = DynamicHtmlStatic.createNewParsingStateArray();
/*  503 */       parseOutput.m_customParseState = customParseState;
/*      */     }
/*      */ 
/*  507 */     if ((chunk.m_chunkType == 1) && (chunk.m_grammarElement != null) && 
/*  509 */       (checkIsDisplayGrammarElement(chunk.m_grammarElement)))
/*      */     {
/*  512 */       customParseState[0] = chunk;
/*      */ 
/*  515 */       customParseState[1] = null;
/*      */     }
/*      */ 
/*  519 */     boolean requiresArgument = false;
/*      */ 
/*  523 */     if ((this.m_isInData) && (chunk.m_chunkType != 14))
/*      */     {
/*  525 */       parseOutput.createParsingException("!csDynHTMLInvalidStructure");
/*      */     }
/*      */ 
/*  535 */     switch (chunk.m_chunkType)
/*      */     {
/*      */     case 4:
/*      */     case 5:
/*      */     case 6:
/*      */     case 7:
/*      */     case 11:
/*  542 */       List curIfList = (List)this.m_interiorNestingTracker.get(this.m_exteriorNestingLevel);
/*  543 */       if (chunk.m_chunkType == 4)
/*      */       {
/*  545 */         addNestingInfo(curIfList, parseOutput);
/*  546 */         requiresArgument = true;
/*      */       }
/*      */       else
/*      */       {
/*  550 */         int curSize = curIfList.size();
/*  551 */         if ((chunk.m_chunkType == 5) || (chunk.m_chunkType == 6))
/*      */         {
/*  554 */           boolean isAllowed = false;
/*  555 */           boolean isElse = chunk.m_chunkType == 6;
/*  556 */           if (curSize > 0)
/*      */           {
/*  558 */             ParseLocationInfo parseInfo = (ParseLocationInfo)curIfList.get(curSize - 1);
/*      */ 
/*  560 */             if (parseInfo.m_parseState != 6)
/*      */             {
/*  562 */               isAllowed = true;
/*  563 */               if (isElse)
/*      */               {
/*  565 */                 parseInfo.copy(parseOutput.m_parseInfo);
/*  566 */                 parseInfo.m_parseState = chunk.m_chunkType;
/*      */               }
/*      */             }
/*      */           }
/*  570 */           if (!isAllowed)
/*      */           {
/*  572 */             String name = (isElse) ? "else" : "elseif";
/*  573 */             if (curSize == 0)
/*      */             {
/*  575 */               parseOutput.createParsingException(LocaleUtils.encodeMessage("csDynHTMLFoundOrphanConditional", null, name));
/*      */             }
/*      */             else
/*      */             {
/*  581 */               createNestingException(curIfList, LocaleUtils.encodeMessage("csDynHTMLFoundInvalidKeyAfterElse", null, name));
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  586 */           requiresArgument = !isElse;
/*      */         }
/*  588 */         else if (chunk.m_chunkType == 7)
/*      */         {
/*  591 */           if (curIfList.size() == 0)
/*      */           {
/*  593 */             parseOutput.createParsingException("!csDynHTMLFoundEndifWithoutIf");
/*      */           }
/*  595 */           curIfList.remove(curIfList.size() - 1);
/*      */         }
/*      */         else
/*      */         {
/*  600 */           if (curIfList.size() > 0)
/*      */           {
/*  602 */             createNestingException(curIfList, "!csDynHTMLFoundEndloopWithUnclosedIf");
/*      */           }
/*      */ 
/*  606 */           if (this.m_exteriorNestingLevel == 0)
/*      */           {
/*  608 */             parseOutput.createParsingException("!csDynHTMLFoundEndloopWithoutLoop");
/*      */           }
/*  610 */           this.m_interiorNestingTracker.remove(this.m_exteriorNestingLevel);
/*  611 */           this.m_exteriorNestingTracker.remove(this.m_exteriorNestingLevel - 1);
/*  612 */           this.m_exteriorNestingLevel -= 1;
/*      */         }
/*      */       }
/*  615 */       break;
/*      */     case 8:
/*      */     case 9:
/*  618 */       addNestingInfo(this.m_exteriorNestingTracker, parseOutput);
/*  619 */       this.m_interiorNestingTracker.add(new ArrayList());
/*  620 */       this.m_exteriorNestingLevel += 1;
/*  621 */       requiresArgument = true;
/*  622 */       break;
/*      */     case 13:
/*  625 */       this.m_isInData = true;
/*  626 */       break;
/*      */     case 14:
/*  628 */       if (!this.m_isInData)
/*      */       {
/*  630 */         parseOutput.createParsingException("!csDynHTMLInvalidIDCEndData");
/*      */       }
/*      */ 
/*  633 */       HtmlChunk[] chunks = new HtmlChunk[3];
/*      */ 
/*  635 */       int size = this.m_htmlChunks.size();
/*  636 */       HtmlChunk chunk1 = (HtmlChunk)this.m_htmlChunks.get(size - 3);
/*  637 */       if (chunk1.m_chunkType != 13)
/*      */       {
/*  639 */         parseOutput.createParsingException("!csDynHTMLInvalidBeginEndDataFormat");
/*      */       }
/*  641 */       chunks[0] = chunk1;
/*  642 */       chunks[1] = ((HtmlChunk)this.m_htmlChunks.get(size - 2));
/*  643 */       chunks[2] = ((HtmlChunk)this.m_htmlChunks.get(size - 1));
/*      */ 
/*  646 */       for (int count = 1; count < 4; ++count)
/*      */       {
/*  648 */         this.m_htmlChunks.remove(size - count);
/*      */       }
/*      */ 
/*  651 */       this.m_data.add(chunks);
/*  652 */       this.m_isInData = false;
/*  653 */       break;
/*      */     case 15:
/*  655 */       int[] directives = { 0, 0 };
/*  656 */       GrammarParser.translateSimpleGrammarExpressionToBitFlagChanges(chunk.m_grammarElement, directives, DynamicHtmlStatic.m_directiveFlags, false);
/*      */ 
/*  658 */       updateActiveDirectives(directives, parseOutput);
/*      */     case 10:
/*      */     case 12:
/*      */     }
/*      */ 
/*  663 */     if (!requiresArgument)
/*      */       return;
/*  665 */     boolean hasArgument = false;
/*  666 */     GrammarElement elt = chunk.m_grammarElement;
/*  667 */     if (elt != null)
/*      */     {
/*  669 */       hasArgument = (elt.m_id != null) && (elt.m_id.length() > 0);
/*      */     }
/*  671 */     if (hasArgument)
/*      */       return;
/*  673 */     parseOutput.createParsingException(LocaleUtils.encodeMessage("csDynHTMLTokenRequiresArg", null, token));
/*      */   }
/*      */ 
/*      */   public void updateActiveDirectives(int[] mergeFlags, ParseOutput parseOutput)
/*      */   {
/*  684 */     if (mergeFlags != null)
/*      */     {
/*  686 */       this.m_activeDirectives |= mergeFlags[0];
/*  687 */       this.m_activeDirectives &= (mergeFlags[1] ^ 0xFFFFFFFF);
/*      */     }
/*  689 */     parseOutput.m_isXmlLiteralEscape = ((this.m_activeDirectives & 0x4) != 0);
/*      */   }
/*      */ 
/*      */   public void setIsXmlSyntax(boolean isXmlSyntax)
/*      */   {
/*  697 */     if (isXmlSyntax)
/*      */     {
/*  699 */       this.m_activeDirectives |= 4;
/*      */     }
/*      */     else
/*      */     {
/*  703 */       this.m_activeDirectives &= -5;
/*      */     }
/*      */   }
/*      */ 
/*      */   public HtmlChunk createHtmlTextChunk(ParseOutput parseOutput, IdcCharArrayWriter outbuf, IdcBreakpoints bp)
/*      */   {
/*  713 */     HtmlChunk chunk = new HtmlChunk(bp);
/*  714 */     chunk.m_chunkType = 0;
/*  715 */     chunk.m_chars = outbuf.toCharArray();
/*      */ 
/*  718 */     int offset = 0;
/*  719 */     int endIndex = chunk.m_chars.length - 1;
/*  720 */     boolean doTrimLeftWhitespace = false;
/*  721 */     boolean alwaysTrimAllLeftWhitespace = false;
/*  722 */     boolean doTrimRightWhitespace = false;
/*  723 */     Object[] customParseState = (Object[])(Object[])parseOutput.m_customParseState;
/*      */ 
/*  726 */     HtmlChunk waitingChunk = null;
/*  727 */     if (customParseState != null)
/*      */     {
/*  729 */       waitingChunk = (HtmlChunk)customParseState[1];
/*      */     }
/*      */ 
/*  732 */     if ((this.m_activeDirectives & 0x2) != 0)
/*      */     {
/*  734 */       doTrimLeftWhitespace = true;
/*  735 */       alwaysTrimAllLeftWhitespace = true;
/*  736 */       doTrimRightWhitespace = true;
/*      */     }
/*  738 */     if ((this.m_activeDirectives & 0x1) != 0)
/*      */     {
/*  742 */       HtmlChunk prevDisplayChunk = null;
/*  743 */       if (customParseState != null)
/*      */       {
/*  746 */         prevDisplayChunk = (HtmlChunk)customParseState[0];
/*      */       }
/*      */ 
/*  752 */       if (!doTrimLeftWhitespace)
/*      */       {
/*  754 */         doTrimLeftWhitespace = prevDisplayChunk == null;
/*      */       }
/*      */     }
/*      */ 
/*  758 */     char[] chars = chunk.m_chars;
/*  759 */     boolean lastBeginLineHasAllWhitespace = false;
/*  760 */     int lastLineFeedIndex = -1;
/*  761 */     int firstNonSpaceIndex = 0;
/*  762 */     if ((doTrimLeftWhitespace) && 
/*  764 */       (chars.length > 0))
/*      */     {
/*  766 */       int i = 0;
/*  767 */       boolean doTrim = false;
/*  768 */       if (alwaysTrimAllLeftWhitespace)
/*      */       {
/*  770 */         doTrim = true;
/*      */       }
/*  772 */       else if ((chars[0] == '\r') || (chars[0] == '\n'))
/*      */       {
/*  775 */         lastBeginLineHasAllWhitespace = true;
/*  776 */         doTrim = true;
/*  777 */         lastLineFeedIndex = 0;
/*  778 */         i = 1;
/*      */       }
/*  780 */       if (doTrim)
/*      */       {
/*  782 */         while (i < chars.length)
/*      */         {
/*  784 */           char ch = chars[i];
/*  785 */           if ((ch == '\r') || (ch == '\n'))
/*      */           {
/*  788 */             lastBeginLineHasAllWhitespace = true;
/*  789 */             lastLineFeedIndex = i;
/*      */           }
/*  791 */           else if ((ch != '\t') && (ch != ' '))
/*      */           {
/*  793 */             lastBeginLineHasAllWhitespace = false;
/*  794 */             break;
/*      */           }
/*  796 */           ++i;
/*      */         }
/*  798 */         firstNonSpaceIndex = i;
/*  799 */         if (alwaysTrimAllLeftWhitespace)
/*      */         {
/*  801 */           offset = firstNonSpaceIndex;
/*      */         }
/*      */         else
/*      */         {
/*  807 */           offset = lastLineFeedIndex + 1;
/*      */         }
/*      */       }
/*  810 */       if ((doTrim) && (waitingChunk != null))
/*      */       {
/*  814 */         i = waitingChunk.m_charsOffset + waitingChunk.m_charsLength - 1;
/*  815 */         while (i >= waitingChunk.m_charsOffset)
/*      */         {
/*  817 */           char ch = waitingChunk.m_chars[i];
/*  818 */           if ((ch != ' ') && (ch != '\t'))
/*      */             break;
/*  820 */           --i;
/*  821 */           waitingChunk.m_charsLength -= 1;
/*      */         }
/*      */ 
/*  828 */         if (waitingChunk.m_charsLength == 0)
/*      */         {
/*  831 */           waitingChunk.m_charsOffset = waitingChunk.m_chars.length;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  841 */     int i = endIndex;
/*  842 */     boolean lastEndLineHasAllWhitespace = lastBeginLineHasAllWhitespace;
/*  843 */     while (i >= firstNonSpaceIndex)
/*      */     {
/*  845 */       char ch = chars[i];
/*  846 */       if ((ch == '\r') || (ch == '\n'))
/*      */       {
/*  848 */         lastEndLineHasAllWhitespace = true;
/*  849 */         if (doTrimRightWhitespace)
/*      */           break label462;
/*  851 */         break;
/*      */       }
/*      */ 
/*  854 */       if ((ch != '\t') && (ch != ' '))
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  859 */       label462: --i;
/*      */     }
/*  861 */     if (doTrimRightWhitespace)
/*      */     {
/*  863 */       endIndex = i;
/*      */     }
/*      */ 
/*  866 */     HtmlChunk nextWaitingChunk = null;
/*  867 */     if ((offset <= endIndex) && (lastEndLineHasAllWhitespace) && (!alwaysTrimAllLeftWhitespace))
/*      */     {
/*  870 */       nextWaitingChunk = chunk;
/*      */     }
/*  872 */     HtmlChunk indicatorChunk = null;
/*  873 */     if (!lastEndLineHasAllWhitespace)
/*      */     {
/*  876 */       indicatorChunk = chunk;
/*      */     }
/*      */ 
/*  879 */     if ((((indicatorChunk != null) || (nextWaitingChunk != null))) && 
/*  881 */       (customParseState == null))
/*      */     {
/*  883 */       customParseState = DynamicHtmlStatic.createNewParsingStateArray();
/*  884 */       parseOutput.m_customParseState = customParseState;
/*      */     }
/*      */ 
/*  887 */     if (customParseState != null)
/*      */     {
/*  889 */       customParseState[0] = indicatorChunk;
/*  890 */       customParseState[1] = nextWaitingChunk;
/*      */     }
/*  892 */     chunk.m_parseLine = parseOutput.m_parseInfo.m_parseLine;
/*  893 */     chunk.m_charsOffset = offset;
/*  894 */     chunk.m_charsLength = (endIndex - offset + 1);
/*  895 */     return chunk;
/*      */   }
/*      */ 
/*      */   public boolean checkIsDisplayGrammarElement(GrammarElement elt)
/*      */   {
/*  904 */     if (elt == null)
/*      */     {
/*  906 */       return false;
/*      */     }
/*  908 */     boolean retVal = false;
/*  909 */     if (elt.m_type == 3)
/*      */     {
/*  911 */       if (elt.m_xxOpFirstChar != '=')
/*      */       {
/*  913 */         retVal = true;
/*      */       }
/*      */     }
/*  916 */     else if (elt.m_type == 4)
/*      */     {
/*  918 */       if ((elt.m_subElementArray != null) && (elt.m_subElementArray.length > 0))
/*      */       {
/*  920 */         GrammarElement lastElement = elt.m_subElementArray[(elt.m_subElementArray.length - 1)];
/*  921 */         if (lastElement.m_type != 3)
/*      */         {
/*  923 */           retVal = true;
/*      */         }
/*      */       }
/*      */     }
/*  927 */     else if (elt.m_type == 2)
/*      */     {
/*  931 */       if ((elt.m_id == null) || (!elt.m_id.equals("inc")))
/*      */       {
/*  933 */         retVal = true;
/*      */       }
/*      */ 
/*      */     }
/*      */     else {
/*  938 */       retVal = true;
/*      */     }
/*  940 */     return retVal;
/*      */   }
/*      */ 
/*      */   public void createNestingException(List parseInfos, String msg)
/*      */     throws ParseSyntaxException
/*      */   {
/*  948 */     ParseLocationInfo parseInfo = (ParseLocationInfo)parseInfos.get(parseInfos.size() - 1);
/*  949 */     throw new ParseSyntaxException(parseInfo, msg);
/*      */   }
/*      */ 
/*      */   public void addNestingInfo(List parseInfos, ParseOutput parseOutput)
/*      */   {
/*  957 */     ParseLocationInfo parseInfo = new ParseLocationInfo();
/*  958 */     parseInfo.copy(parseOutput.m_parseInfo);
/*  959 */     parseInfos.add(parseInfo);
/*      */   }
/*      */ 
/*      */   public GrammarElement parseGrammarStatement(HtmlChunk chunk, GrammarParseState parseState)
/*      */     throws ParseSyntaxException
/*      */   {
/*  969 */     return GrammarParser.parseElement(parseState, chunk.m_fileName, chunk.m_parseLine, chunk.m_parseCharOffset);
/*      */   }
/*      */ 
/*      */   public void outputHtml(Writer writer, DynamicHtmlOutput dynCallback)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*  982 */     dynCallback.registerMerger();
/*      */     try
/*      */     {
/*  987 */       boolean[] isBreak = new boolean[1];
/*  988 */       isBreak[0] = false;
/*  989 */       int[] retType = new int[1];
/*  990 */       retType[0] = 0;
/*  991 */       outputHtmlFromStart(writer, dynCallback, isBreak, retType, 0);
/*      */     }
/*      */     finally
/*      */     {
/*  998 */       dynCallback.unregisterMerger();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected int outputHtmlFromStart(Writer writer, DynamicHtmlOutput dynCallback, boolean[] isBreak, int[] retType, int nestLevel)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1007 */     int retVal = 0;
/* 1008 */     Object[] preservedValues = null;
/* 1009 */     if (this.m_preservedParameters != null)
/*      */     {
/* 1012 */       preservedValues = new Object[this.m_preservedParameters.length * 2];
/* 1013 */       int i = 0; for (int j = 0; i < this.m_preservedParameters.length; j += 2)
/*      */       {
/* 1015 */         String key = this.m_preservedParameters[i];
/* 1016 */         preservedValues[j] = dynCallback.getScriptObject(key, Integer.valueOf(1));
/* 1017 */         preservedValues[(j + 1)] = dynCallback.getScriptObject(key, Integer.valueOf(2));
/*      */ 
/* 1013 */         ++i;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1020 */     retVal = outputHtmlEx(0, writer, dynCallback, 0, true, isBreak, retType, nestLevel);
/*      */ 
/* 1022 */     if (this.m_preservedParameters != null)
/*      */     {
/* 1025 */       int i = 0; for (int j = 0; i < this.m_preservedParameters.length; j += 2)
/*      */       {
/* 1027 */         String key = this.m_preservedParameters[i];
/*      */ 
/* 1032 */         dynCallback.setScriptObject(key, preservedValues[j]);
/* 1033 */         dynCallback.setScriptObject(key, preservedValues[(j + 1)]);
/*      */ 
/* 1025 */         ++i;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1037 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected int outputHtmlEx(int curIndex, Writer writer, DynamicHtmlOutput dynCallback, int exitType, boolean doOutput, boolean[] isBreak, int[] retType, int nestLevel)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1049 */     int nChunks = this.m_htmlChunks.size();
/* 1050 */     boolean foundExit = false;
/* 1051 */     boolean result = false;
/* 1052 */     boolean insideIf = false;
/*      */ 
/* 1055 */     if (nestLevel > 100)
/*      */     {
/* 1058 */       String msg = LocaleUtils.encodeMessage("csDynHTMLResourceIncludesNestedTooDeep", null, "100");
/*      */ 
/* 1060 */       throw new IOException(msg);
/*      */     }
/*      */ 
/* 1079 */     while ((curIndex < nChunks) && (!foundExit))
/*      */     {
/* 1081 */       HtmlChunk chunk = this.m_htmlChunkArray[curIndex];
/*      */ 
/* 1083 */       int chunkType = chunk.m_chunkType;
/* 1084 */       GrammarElement elt = chunk.m_grammarElement;
/* 1085 */       String varName = "!csDynHTMLDefaultChunkName";
/* 1086 */       if ((elt != null) && (elt.m_id != null))
/*      */       {
/* 1088 */         varName = elt.m_id;
/*      */       }
/* 1090 */       boolean output = (doOutput) && (isBreak[0] == 0);
/* 1091 */       if (output)
/*      */       {
/* 1093 */         checkBreakpoint(chunk, elt, dynCallback);
/*      */       }
/* 1095 */       switch (chunkType)
/*      */       {
/*      */       case 0:
/* 1098 */         if ((output) && (chunk.m_charsLength > 0))
/*      */         {
/* 1100 */           writer.write(chunk.m_chars, chunk.m_charsOffset, chunk.m_charsLength);
/*      */         }
/* 1102 */         ++curIndex;
/* 1103 */         break;
/*      */       case 1:
/*      */       case 2:
/*      */       case 3:
/* 1107 */         if (output)
/*      */         {
/* 1109 */           Writer w = (chunkType != 2) ? writer : null;
/* 1110 */           substituteVariable(chunk, w, dynCallback, elt);
/*      */         }
/* 1112 */         ++curIndex;
/* 1113 */         break;
/*      */       case 4:
/*      */       case 5:
/* 1116 */         boolean isIf = chunkType == 4;
/* 1117 */         if ((!isIf) && (!insideIf) && (exitType == 6))
/*      */         {
/* 1119 */           retType[0] = chunkType;
/* 1120 */           foundExit = true;
/*      */         }
/*      */         else
/*      */         {
/* 1125 */           boolean doTest = (isIf == true) || (!result);
/* 1126 */           if ((doTest) && (output))
/*      */           {
/* 1128 */             result = checkCondition(chunk, dynCallback, elt);
/*      */           }
/* 1130 */           curIndex = outputHtmlEx(curIndex + 1, writer, dynCallback, 6, (result) && (output) && (doTest), isBreak, retType, nestLevel);
/*      */ 
/* 1135 */           if (retType[0] == 5)
/*      */           {
/* 1138 */             insideIf = true;
/*      */           }
/*      */           else
/*      */           {
/* 1143 */             curIndex = outputHtmlEx(curIndex, writer, dynCallback, 7, (!result) && (output), isBreak, retType, nestLevel);
/*      */ 
/* 1146 */             insideIf = false;
/*      */           }
/*      */         }
/* 1148 */         break;
/*      */       case 8:
/*      */       case 9:
/* 1151 */         if (output)
/*      */         {
/* 1153 */           int newIndex = curIndex;
/* 1154 */           boolean hasExecuted = false;
/* 1155 */           boolean doNextRow = true;
/* 1156 */           String loopKey = null;
/* 1157 */           if (chunkType == 8)
/*      */           {
/* 1159 */             loopKey = varName;
/* 1160 */             if ((elt != null) && (elt.m_type == 2) && (elt.m_id.equals("rs")) && (elt.m_subElementArray.length == 1))
/*      */             {
/* 1163 */               loopKey = dynCallback.evaluateVariable(chunk, elt.m_subElementArray[0]);
/* 1164 */               if ((loopKey == null) || (loopKey.length() == 0))
/*      */               {
/* 1166 */                 doNextRow = false;
/* 1167 */                 newIndex = outputHtmlEx(curIndex + 1, writer, dynCallback, 11, doNextRow, isBreak, retType, nestLevel);
/*      */               }
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1173 */           while (doNextRow == true)
/*      */           {
/* 1179 */             if (chunkType == 8)
/*      */             {
/* 1181 */               doNextRow = dynCallback.loadNextRow(chunk, loopKey);
/*      */             }
/*      */             else
/*      */             {
/* 1185 */               doNextRow = checkCondition(chunk, dynCallback, elt);
/*      */             }
/* 1187 */             if ((!doNextRow) && (hasExecuted == true)) {
/*      */               break;
/*      */             }
/*      */ 
/* 1191 */             newIndex = outputHtmlEx(curIndex + 1, writer, dynCallback, 11, doNextRow, isBreak, retType, nestLevel);
/*      */ 
/* 1193 */             if (isBreak[0] != 0)
/*      */             {
/* 1195 */               if ((doNextRow) && (chunkType == 8))
/*      */               {
/* 1197 */                 dynCallback.endActiveResultSet();
/*      */               }
/* 1199 */               doNextRow = false;
/* 1200 */               isBreak[0] = false;
/*      */             }
/* 1202 */             hasExecuted = true;
/*      */           }
/* 1204 */           curIndex = newIndex;
/*      */         }
/*      */         else
/*      */         {
/* 1208 */           curIndex = outputHtmlEx(curIndex + 1, writer, dynCallback, 11, false, isBreak, retType, nestLevel);
/*      */         }
/*      */ 
/* 1211 */         break;
/*      */       case 10:
/* 1213 */         if (output)
/*      */         {
/* 1215 */           isBreak[0] = true;
/*      */         }
/* 1217 */         ++curIndex;
/* 1218 */         break;
/*      */       case 12:
/* 1220 */         if (output == true)
/*      */         {
/* 1222 */           DynamicHtml resource = dynCallback.getAndRedirectHtmlResource(varName, chunk);
/* 1223 */           if (resource != null)
/*      */           {
/*      */             try
/*      */             {
/* 1227 */               resource.outputHtmlFromStart(writer, dynCallback, isBreak, retType, nestLevel + 1);
/*      */             }
/*      */             finally
/*      */             {
/* 1232 */               dynCallback.setBackRedirectHtmlResource(varName, resource, chunk);
/*      */             }
/*      */           }
/*      */         }
/* 1236 */         ++curIndex;
/* 1237 */         break;
/*      */       case 15:
/* 1240 */         ++curIndex;
/* 1241 */         break;
/*      */       case 6:
/*      */       case 7:
/*      */       case 11:
/*      */       case 13:
/*      */       case 14:
/*      */       default:
/* 1245 */         retType[0] = chunkType;
/* 1246 */         if ((exitType == 6) && 
/* 1248 */           (chunkType == 7))
/*      */         {
/* 1250 */           foundExit = true;
/*      */         }
/*      */         else
/*      */         {
/* 1256 */           if (exitType == chunkType)
/*      */           {
/* 1258 */             foundExit = true;
/*      */           }
/* 1260 */           ++curIndex;
/*      */         }
/*      */       }
/*      */     }
/* 1264 */     return curIndex;
/*      */   }
/*      */ 
/*      */   public void checkBreakpoint(HtmlChunk chunk, GrammarElement elt, DynamicHtmlOutput dynCallback)
/*      */   {
/* 1269 */     if (chunk.m_chunkType == 0)
/*      */     {
/* 1271 */       return;
/*      */     }
/*      */ 
/* 1274 */     IdcBreakpoints bps = chunk.m_markers;
/* 1275 */     if ((((bps == null) || (bps.m_lines == null) || (bps.m_lines[chunk.m_parseLine] == 0))) && (!IdcBreakpointManager.m_isStepping)) {
/*      */       return;
/*      */     }
/* 1278 */     Report.trace("idcdebug", "DynamicHtml.checkBreakpoint:The breakpoint " + chunk.m_fileName + " has been set at line " + chunk.m_parseLine, null);
/*      */ 
/* 1280 */     dynCallback.evaluateBreakpoint(chunk);
/*      */   }
/*      */ 
/*      */   public DynamicHtml getPriorScript()
/*      */   {
/* 1289 */     return this.m_priorScript;
/*      */   }
/*      */ 
/*      */   public void setPriorScript(DynamicHtml priorScript)
/*      */   {
/* 1297 */     this.m_priorScript = priorScript;
/*      */   }
/*      */ 
/*      */   public DynamicHtml shallowClone()
/*      */   {
/* 1307 */     DynamicHtml dynHtml = null;
/*      */     try
/*      */     {
/* 1310 */       dynHtml = (DynamicHtml)super.getClass().newInstance();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1314 */       Report.trace(null, LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csDynHTMLUnableToInstantiateClass", e.getMessage()), null), e);
/*      */ 
/* 1317 */       dynHtml = new DynamicHtml();
/*      */     }
/* 1319 */     dynHtml.m_htmlChunks = this.m_htmlChunks;
/* 1320 */     dynHtml.m_htmlChunkArray = this.m_htmlChunkArray;
/* 1321 */     dynHtml.m_fileName = this.m_fileName;
/* 1322 */     dynHtml.m_parseCharOffset = this.m_parseCharOffset;
/* 1323 */     dynHtml.m_parseLine = this.m_parseLine;
/* 1324 */     dynHtml.m_resourceString = this.m_resourceString;
/* 1325 */     dynHtml.m_parsingFlags = this.m_parsingFlags;
/* 1326 */     dynHtml.m_extraData = this.m_extraData;
/* 1327 */     dynHtml.m_dynamicData = this.m_dynamicData;
/* 1328 */     dynHtml.m_preservedParameters = this.m_preservedParameters;
/* 1329 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public DynamicHtml shallowCloneWithPriorScript(DynamicHtml priorScript)
/*      */   {
/* 1338 */     DynamicHtml dynHtml = shallowClone();
/* 1339 */     dynHtml.m_priorScript = priorScript;
/* 1340 */     return dynHtml;
/*      */   }
/*      */ 
/*      */   public DynamicHtml findEarliestValidPriorScript(DynamicHtml priorResource)
/*      */   {
/* 1361 */     if (priorResource == null)
/*      */     {
/* 1363 */       return null;
/*      */     }
/* 1365 */     DynamicHtml pRes = priorResource;
/* 1366 */     DynamicHtml oRes = priorResource;
/* 1367 */     while (pRes != null)
/*      */     {
/* 1369 */       if (pRes.m_fileName.equals(this.m_fileName))
/*      */       {
/* 1377 */         oRes = pRes.getPriorScript();
/* 1378 */         break;
/*      */       }
/* 1380 */       pRes = pRes.getPriorScript();
/*      */     }
/* 1382 */     return oRes;
/*      */   }
/*      */ 
/*      */   protected void substituteVariable(HtmlChunk chunk, Writer writer, DynamicHtmlOutput dynCallback, GrammarElement elt)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*      */     try
/*      */     {
/* 1394 */       dynCallback.substituteVariable(chunk, elt, writer);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1398 */       createAbsoluteSyntaxException(chunk, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkCondition(HtmlChunk chunk, DynamicHtmlOutput dynCallback, GrammarElement elt)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*      */     try
/*      */     {
/* 1410 */       return dynCallback.checkCondition(chunk, elt);
/*      */     }
/*      */     catch (ParseSyntaxException e)
/*      */     {
/* 1414 */       createAbsoluteSyntaxException(chunk, e);
/*      */     }
/* 1416 */     return false;
/*      */   }
/*      */ 
/*      */   protected void createAbsoluteSyntaxException(HtmlChunk chunk, ParseSyntaxException e)
/*      */     throws ParseSyntaxException
/*      */   {
/* 1428 */     if (e.m_parseInfo.m_parseLine > 0)
/*      */     {
/* 1430 */       e.m_parseInfo.m_parseLine += chunk.m_parseLine;
/*      */     }
/*      */     else
/*      */     {
/* 1434 */       e.m_parseInfo.m_parseLine = chunk.m_parseLine;
/* 1435 */       e.m_parseInfo.m_parseCharOffset += chunk.m_parseCharOffset;
/*      */     }
/* 1437 */     e.m_parseInfo.m_fileName = chunk.m_fileName;
/* 1438 */     throw e;
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1444 */     return this.m_fileName;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1449 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82583 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicHtml
 * JD-Core Version:    0.5.4
 */