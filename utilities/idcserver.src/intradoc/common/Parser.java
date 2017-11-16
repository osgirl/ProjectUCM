/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ 
/*     */ public class Parser
/*     */ {
/*     */   public static boolean findCharacter(Reader reader, ParseOutput parseOutput, char ch, char[] startCommentChars, char[] endCommentChars, boolean noStop)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/*  50 */     boolean insideCommentBlock = false;
/*  51 */     boolean insideLiteral = false;
/*  52 */     boolean insideEscapeSequence = false;
/*  53 */     boolean changeInsideCommentBlockFlag = false;
/*  54 */     int nextIterationStartIndex = 0;
/*  55 */     boolean doCommentBlock = (startCommentChars != null) && (startCommentChars.length > 0);
/*  56 */     char[] activeCharSequence = startCommentChars;
/*  57 */     char literalChar = '-';
/*     */ 
/*  61 */     assert ((!doCommentBlock) || ((endCommentChars != null) && (endCommentChars.length > 0) && (noStop)));
/*     */ 
/*  64 */     parseOutput.copyToPending(false, false);
/*     */ 
/*  67 */     char[] temp = parseOutput.m_outputBuf;
/*     */     while (true)
/*     */     {
/*  71 */       checkLoadOutputBuffer(reader, parseOutput);
/*  72 */       if (parseOutput.m_isEOF)
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/*  77 */       int offset = parseOutput.m_readOffset;
/*  78 */       int numToParse = parseOutput.m_numRead;
/*  79 */       int originalNumToParse = numToParse;
/*  80 */       if ((doCommentBlock) && (numToParse > activeCharSequence.length) && (!parseOutput.m_isReaderEOF))
/*     */       {
/*  82 */         numToParse -= activeCharSequence.length - 1;
/*     */       }
/*  84 */       int start = nextIterationStartIndex;
/*  85 */       int numWaiting = start;
/*  86 */       nextIterationStartIndex = 0;
/*     */ 
/*  89 */       for (int i = start; i < numToParse; ++i)
/*     */       {
/*  91 */         char tch = temp[(i + offset)];
/*  92 */         ++numWaiting;
/*  93 */         boolean normalChar = !insideCommentBlock;
/*  94 */         if (tch == 65533)
/*     */         {
/*  96 */           parseOutput.m_hasUnicodeReplacementCharacter = true;
/*  97 */           if ((parseOutput.m_traceOnDecodeError) && (!parseOutput.m_hasTracedUnicodeReplacementCharacter))
/*     */           {
/* 100 */             String fileName = "<unknown>";
/* 101 */             String line = "?";
/* 102 */             String charPosition = "?";
/* 103 */             if (parseOutput.m_parseInfo != null)
/*     */             {
/* 105 */               if (parseOutput.m_parseInfo.m_fileName != null)
/*     */               {
/* 107 */                 fileName = parseOutput.m_parseInfo.m_fileName;
/*     */               }
/* 109 */               line = "" + parseOutput.m_parseInfo.m_parseLine;
/* 110 */               charPosition = "" + parseOutput.m_parseInfo.m_parseCharOffset;
/*     */             }
/*     */ 
/* 113 */             Report.trace("encoding", "found a 0xFFFD character in file '" + fileName + "' at line " + line + ", character " + charPosition, null);
/*     */ 
/* 117 */             parseOutput.m_hasTracedUnicodeReplacementCharacter = true;
/*     */           }
/* 119 */           if (parseOutput.m_failOnDecodeError)
/*     */           {
/* 122 */             String msg = LocaleUtils.encodeMessage("syCharEncodingReplacementCharFound", null);
/*     */ 
/* 124 */             parseOutput.createParsingException(msg);
/*     */           }
/*     */         }
/* 127 */         else if ((doCommentBlock) && (tch == activeCharSequence[0]))
/*     */         {
/* 129 */           int j = 1;
/* 130 */           int index = i + 1;
/*     */ 
/* 133 */           while ((j < activeCharSequence.length) && (index < originalNumToParse))
/*     */           {
/* 135 */             if (activeCharSequence[j] != temp[(index + offset)]) {
/*     */               break;
/*     */             }
/*     */ 
/* 139 */             ++j;
/* 140 */             ++index;
/*     */           }
/* 142 */           if (j == activeCharSequence.length)
/*     */           {
/* 144 */             if (insideCommentBlock)
/*     */             {
/* 147 */               numWaiting = numWaiting + activeCharSequence.length - 1;
/*     */             }
/*     */             else
/*     */             {
/* 152 */               --numWaiting;
/* 153 */               nextIterationStartIndex = activeCharSequence.length;
/*     */             }
/*     */ 
/* 158 */             changeInsideCommentBlockFlag = true;
/* 159 */             break;
/*     */           }
/*     */         }
/*     */ 
/* 163 */         if (!normalChar)
/*     */           continue;
/* 165 */         if (insideLiteral)
/*     */         {
/* 167 */           if (insideEscapeSequence)
/*     */           {
/* 170 */             insideEscapeSequence = false;
/*     */           }
/* 174 */           else if (tch == literalChar)
/*     */           {
/* 176 */             insideLiteral = false;
/*     */           } else {
/* 178 */             if ((tch != '\\') || (parseOutput.m_isXmlLiteralEscape))
/*     */               continue;
/* 180 */             insideEscapeSequence = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 189 */           if (tch == ch)
/*     */           {
/* 191 */             parseOutput.m_numWaiting = numWaiting;
/* 192 */             return true;
/*     */           }
/* 194 */           if ((noStop) || ((tch != '\'') && (tch != '"')) || (parseOutput.m_noLiteralStrings))
/*     */             continue;
/* 196 */           insideLiteral = true;
/* 197 */           literalChar = tch;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 202 */       parseOutput.m_numWaiting = numWaiting;
/*     */ 
/* 204 */       if (!noStop) {
/*     */         break;
/*     */       }
/*     */ 
/* 208 */       parseOutput.copyToPending(!insideCommentBlock, false);
/*     */ 
/* 211 */       if (changeInsideCommentBlockFlag)
/*     */       {
/* 213 */         if (insideCommentBlock)
/*     */         {
/* 215 */           activeCharSequence = startCommentChars;
/* 216 */           insideCommentBlock = false;
/*     */         }
/*     */         else
/*     */         {
/* 220 */           activeCharSequence = endCommentChars;
/* 221 */           insideCommentBlock = true;
/*     */         }
/* 223 */         changeInsideCommentBlockFlag = false;
/*     */       }
/*     */     }
/*     */ 
/* 227 */     if (insideLiteral)
/*     */     {
/* 229 */       parseOutput.createParsingException(LocaleUtils.encodeMessage("csParserUnmatchedChar", null, new Character(literalChar)));
/*     */     }
/*     */ 
/* 233 */     return false;
/*     */   }
/*     */ 
/*     */   public static int findCharacters(Reader reader, ParseOutput parseOutput, char[] chs, boolean noStop)
/*     */     throws IOException
/*     */   {
/* 243 */     return findCharactersEx(reader, parseOutput, chs, null, noStop);
/*     */   }
/*     */ 
/*     */   public static int findCharactersEx(Reader reader, ParseOutput parseOutput, char[] chs, char[] startCheck, boolean noStop)
/*     */     throws IOException
/*     */   {
/* 255 */     boolean insideLiteral = false;
/* 256 */     boolean insideEscapeSequence = false;
/* 257 */     char literalChar = '-';
/*     */ 
/* 260 */     parseOutput.copyToPending(false, false);
/*     */ 
/* 263 */     char[] temp = parseOutput.m_outputBuf;
/*     */ 
/* 265 */     int startIndex = 0;
/* 266 */     boolean doStartCheck = (startCheck != null) && (startCheck.length > 0);
/*     */     while (true)
/*     */     {
/* 271 */       checkLoadOutputBuffer(reader, parseOutput);
/* 272 */       if (parseOutput.m_isEOF) {
/*     */         break;
/*     */       }
/*     */ 
/* 276 */       int offset = parseOutput.m_readOffset;
/* 277 */       int numRead = parseOutput.m_numRead;
/*     */ 
/* 280 */       for (int i = 0; i < numRead; ++i)
/*     */       {
/* 282 */         char tch = temp[(i + offset)];
/* 283 */         if (tch == 65533)
/*     */         {
/* 285 */           parseOutput.m_hasUnicodeReplacementCharacter = true;
/*     */         }
/*     */ 
/* 288 */         if (doStartCheck)
/*     */         {
/* 290 */           if (tch != startCheck[startIndex])
/*     */           {
/* 292 */             parseOutput.m_numWaiting = i;
/* 293 */             return -1;
/*     */           }
/* 295 */           ++startIndex;
/* 296 */           if (startIndex >= startCheck.length)
/*     */           {
/* 298 */             doStartCheck = false;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 303 */         if (insideLiteral)
/*     */         {
/* 305 */           if (insideEscapeSequence)
/*     */           {
/* 308 */             insideEscapeSequence = false;
/*     */           }
/* 312 */           else if (tch == literalChar)
/*     */           {
/* 314 */             insideLiteral = false;
/*     */           } else {
/* 316 */             if ((tch != '\\') || (parseOutput.m_isXmlLiteralEscape))
/*     */               continue;
/* 318 */             insideEscapeSequence = true;
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 324 */           for (int j = 0; j < chs.length; ++j)
/*     */           {
/* 326 */             if (tch != chs[j])
/*     */               continue;
/* 328 */             parseOutput.m_numWaiting = (i + 1);
/* 329 */             return j;
/*     */           }
/*     */ 
/* 332 */           if ((noStop) || ((tch != '\'') && (tch != '"')) || (parseOutput.m_noLiteralStrings))
/*     */             continue;
/* 334 */           insideLiteral = true;
/* 335 */           literalChar = tch;
/*     */         }
/*     */       }
/*     */ 
/* 339 */       parseOutput.m_numWaiting = numRead;
/*     */ 
/* 341 */       if (!noStop) {
/*     */         break;
/*     */       }
/*     */ 
/* 345 */       parseOutput.copyToPending(true, false);
/*     */     }
/*     */ 
/* 348 */     return -1;
/*     */   }
/*     */ 
/*     */   public static boolean checkLoadOutputBuffer(Reader reader, ParseOutput parseOutput)
/*     */     throws IOException
/*     */   {
/* 356 */     char[] temp = parseOutput.m_outputBuf;
/* 357 */     int offset = parseOutput.m_readOffset;
/*     */ 
/* 359 */     int numRead = parseOutput.m_numRead;
/* 360 */     if ((numRead < temp.length / 2) && (!parseOutput.m_isReaderEOF))
/*     */     {
/* 362 */       int nchars = reader.read(temp, numRead + offset, temp.length - numRead - offset);
/*     */ 
/* 364 */       if (nchars > 0)
/*     */       {
/* 366 */         numRead += nchars;
/* 367 */         parseOutput.m_numRead = numRead;
/*     */       }
/*     */       else
/*     */       {
/* 371 */         parseOutput.m_isReaderEOF = true;
/*     */       }
/*     */     }
/* 374 */     if (numRead <= 0)
/*     */     {
/* 376 */       parseOutput.m_isEOF = true;
/*     */     }
/*     */ 
/* 379 */     return !parseOutput.m_isEOF;
/*     */   }
/*     */ 
/*     */   public static boolean findHtmlTagPrefix(Reader reader, ParseOutput parseOutput, char[] tag)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 388 */     char[] chs = { '>' };
/* 389 */     while (findCharacter(reader, parseOutput, '<', null, null, true))
/*     */     {
/* 391 */       parseOutput.copyToPending(true, true);
/*     */ 
/* 395 */       int index = findCharactersEx(reader, parseOutput, chs, tag, false);
/* 396 */       if (index < 0)
/*     */       {
/* 398 */         if (parseOutput.m_isEOF)
/*     */         {
/* 400 */           return false;
/*     */         }
/* 402 */         if (parseOutput.m_writer != null)
/*     */         {
/* 404 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */         }
/* 406 */         parseOutput.copyToPending(true, false);
/*     */       }
/*     */       else
/*     */       {
/* 410 */         return true;
/*     */       }
/*     */     }
/* 413 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean findGenericTagPrefix(Reader reader, ParseOutput parseOutput, char[] beginChars, char[] endChars, char[] tag)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/*     */     while (true)
/*     */     {
/* 425 */       int findCharIndex = findCharacters(reader, parseOutput, beginChars, true);
/* 426 */       if (findCharIndex < 0) {
/*     */         break;
/*     */       }
/*     */ 
/* 430 */       parseOutput.copyToPending(true, true);
/*     */ 
/* 434 */       int index = findCharactersEx(reader, parseOutput, endChars, tag, false);
/* 435 */       if (index < 0)
/*     */       {
/* 437 */         if (parseOutput.m_isEOF)
/*     */         {
/* 439 */           return false;
/*     */         }
/* 441 */         if (parseOutput.m_writer != null)
/*     */         {
/* 443 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = beginChars[findCharIndex];
/*     */         }
/* 445 */         parseOutput.copyToPending(true, false);
/*     */       }
/*     */       else
/*     */       {
/* 449 */         return true;
/*     */       }
/*     */     }
/* 452 */     return false;
/*     */   }
/*     */ 
/*     */   public static int findHtmlPrefixTags(Reader reader, ParseOutput parseOutput, char[][] tags)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 463 */     char[] chs = { '>', '<' };
/*     */ 
/* 465 */     if (findCharacter(reader, parseOutput, '<', null, null, true))
/*     */     {
/*     */       while (true)
/*     */       {
/* 469 */         parseOutput.copyToPending(true, true);
/*     */ 
/* 472 */         int index = findCharacters(reader, parseOutput, chs, false);
/* 473 */         if (index < 0) {
/* 474 */           return -1;
/*     */         }
/* 476 */         if (index == 0)
/*     */         {
/* 479 */           for (int i = 0; i < tags.length; ++i)
/*     */           {
/* 482 */             if (parseOutput.isBufferEqualNoCase(tags[i], tags[i].length)) {
/* 483 */               return i;
/*     */             }
/*     */           }
/*     */         }
/* 487 */         if (parseOutput.m_writer != null)
/*     */         {
/* 489 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */         }
/*     */ 
/* 492 */         if (index == 0)
/*     */         {
/* 494 */           parseOutput.copyToPending(true, false);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 499 */     return -1;
/*     */   }
/*     */ 
/*     */   public static boolean findScriptTag(Reader reader, ParseOutput parseOutput, char ch, char[] startCommentChars, char[] endCommentChars)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 512 */     while (findCharacter(reader, parseOutput, '<', startCommentChars, endCommentChars, true))
/*     */     {
/* 515 */       parseOutput.copyToPending(true, true);
/*     */ 
/* 517 */       boolean isSuccess = true;
/* 518 */       boolean waitingData = false;
/* 519 */       boolean foundDollar = false;
/*     */ 
/* 522 */       int tch = removeNextCharacter(reader, parseOutput);
/* 523 */       while (tch == 60)
/*     */       {
/* 525 */         if (parseOutput.m_numPending >= parseOutput.m_maxPending)
/*     */         {
/* 527 */           parseOutput.writePending();
/*     */         }
/*     */ 
/* 530 */         if (parseOutput.m_numRemoved >= parseOutput.m_numRead)
/*     */         {
/* 532 */           parseOutput.copyToPending(true, false);
/* 533 */           checkLoadOutputBuffer(reader, parseOutput);
/*     */         }
/* 535 */         if (parseOutput.m_isEOF)
/*     */         {
/*     */           break;
/*     */         }
/*     */ 
/* 541 */         parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */ 
/* 543 */         tch = removeNextCharacter(reader, parseOutput);
/*     */       }
/* 545 */       if (tch != ch)
/*     */       {
/* 547 */         isSuccess = false;
/*     */       }
/*     */ 
/* 552 */       if (isSuccess == true)
/*     */       {
/* 554 */         foundDollar = findCharacter(reader, parseOutput, ch, null, null, false);
/* 555 */         if (!foundDollar)
/*     */         {
/* 557 */           isSuccess = false;
/*     */         }
/* 559 */         waitingData = true;
/*     */       }
/* 561 */       int endch = -1;
/* 562 */       if (isSuccess == true)
/*     */       {
/* 564 */         endch = removeNextCharacter(reader, parseOutput);
/* 565 */         if (endch != 62)
/*     */         {
/* 567 */           isSuccess = false;
/*     */         }
/*     */       }
/* 570 */       if (isSuccess == true)
/*     */       {
/* 573 */         return true;
/*     */       }
/*     */ 
/* 576 */       if (waitingData)
/*     */       {
/* 578 */         parseOutput.createParsingException(LocaleUtils.encodeMessage("csParserUnableToFindClosingElement", null, new Character(ch)));
/*     */       }
/*     */ 
/* 584 */       parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/* 585 */       parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = (char)tch;
/*     */ 
/* 591 */       if (foundDollar == true)
/*     */       {
/* 593 */         parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = ch;
/*     */       }
/* 595 */       if (endch > 0)
/*     */       {
/* 597 */         parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = (char)endch;
/*     */       }
/*     */     }
/*     */ 
/* 601 */     return false;
/*     */   }
/*     */ 
/*     */   protected static int removeNextCharacter(Reader reader, ParseOutput parseOutput)
/*     */     throws IOException
/*     */   {
/* 608 */     int ch = parseOutput.removeNextChar();
/* 609 */     if (ch >= 0)
/* 610 */       return ch;
/* 611 */     ch = reader.read();
/* 612 */     if (ch == 65533)
/*     */     {
/* 614 */       parseOutput.m_hasUnicodeReplacementCharacter = true;
/*     */     }
/* 616 */     if (ch == -1)
/*     */     {
/* 618 */       parseOutput.m_isEOF = true;
/*     */     }
/*     */     else
/*     */     {
/* 622 */       parseOutput.updateParseLocation((char)ch);
/*     */     }
/* 624 */     return ch;
/*     */   }
/*     */ 
/*     */   public static String findAnyTag(Reader reader, ParseOutput parseOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 633 */     char[] chs = { '>', '<' };
/*     */ 
/* 635 */     if (findCharacter(reader, parseOutput, '<', null, null, true) == true)
/*     */     {
/*     */       while (true)
/*     */       {
/* 639 */         parseOutput.copyToPending(true, true);
/*     */ 
/* 642 */         int index = findCharacters(reader, parseOutput, chs, false);
/* 643 */         if (index < 0)
/*     */         {
/* 645 */           return null;
/*     */         }
/*     */ 
/* 648 */         if (index == 0)
/*     */         {
/* 651 */           return parseOutput.waitingBufferAsString();
/*     */         }
/*     */ 
/* 654 */         if (parseOutput.m_writer != null)
/*     */         {
/* 657 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */         }
/*     */       }
/*     */     }
/* 661 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 666 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Parser
 * JD-Core Version:    0.5.4
 */