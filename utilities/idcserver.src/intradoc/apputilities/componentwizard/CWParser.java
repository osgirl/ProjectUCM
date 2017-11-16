/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseOutput;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Parser;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ 
/*     */ public class CWParser extends Parser
/*     */ {
/*     */   public static boolean findHtmlTagEx(Reader reader, ParseOutput parseOutput, String tag, boolean noStop, boolean writeToOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/*  32 */     char[] chs = { '>', '<' };
/*  33 */     int tagLen = tag.length();
/*  34 */     char[] tagChs = tag.toCharArray();
/*     */ 
/*  36 */     if (findCharacterEx(reader, parseOutput, '<', true, writeToOutput) == true)
/*     */     {
/*     */       while (true)
/*     */       {
/*  40 */         parseOutput.copyToPending(writeToOutput, true);
/*     */ 
/*  43 */         int index = findCharactersEx(reader, parseOutput, chs, noStop, writeToOutput);
/*  44 */         if (index < 0) {
/*  45 */           return false;
/*     */         }
/*  47 */         if ((index == 0) && 
/*  50 */           (parseOutput.isBufferEqualNoCase(tagChs, tagLen))) {
/*  51 */           return true;
/*     */         }
/*     */ 
/*  54 */         if (parseOutput.m_writer != null)
/*     */         {
/*  56 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */         }
/*     */ 
/*  59 */         if (index == 0)
/*     */         {
/*  61 */           parseOutput.copyToPending(writeToOutput, false);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  66 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean findScriptTagEx(Reader reader, ParseOutput parseOutput, char ch, boolean noStop, boolean writeToOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/*  73 */     while (findCharacterEx(reader, parseOutput, '<', true, writeToOutput) == true)
/*     */     {
/*  76 */       parseOutput.copyToPending(writeToOutput, writeToOutput);
/*     */ 
/*  78 */       boolean isSuccess = true;
/*  79 */       boolean waitingData = false;
/*  80 */       boolean foundPercent = false;
/*     */ 
/*  83 */       int tch = removeNextCharacter(reader, parseOutput);
/*  84 */       if (tch != ch)
/*     */       {
/*  86 */         isSuccess = false;
/*     */       }
/*     */ 
/*  91 */       if (isSuccess == true)
/*     */       {
/*  93 */         foundPercent = findCharacterEx(reader, parseOutput, ch, noStop, writeToOutput);
/*  94 */         if (!foundPercent)
/*     */         {
/*  96 */           isSuccess = false;
/*     */         }
/*  98 */         waitingData = true;
/*     */       }
/* 100 */       int endch = -1;
/* 101 */       if (isSuccess == true)
/*     */       {
/* 103 */         endch = removeNextCharacter(reader, parseOutput);
/* 104 */         if (endch != 62)
/*     */         {
/* 106 */           isSuccess = false;
/*     */         }
/*     */       }
/* 109 */       if (isSuccess == true)
/*     */       {
/* 112 */         return true;
/*     */       }
/*     */ 
/* 115 */       if (waitingData)
/*     */       {
/* 117 */         parseOutput.createParsingException(LocaleUtils.encodeMessage("csParserUnableToFindClosingElement", null, "" + ch));
/*     */       }
/*     */ 
/* 121 */       parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/* 122 */       parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = (char)tch;
/*     */ 
/* 128 */       if (foundPercent == true)
/*     */       {
/* 130 */         parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = ch;
/*     */       }
/* 132 */       if (endch > 0)
/*     */       {
/* 134 */         parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = (char)endch;
/*     */       }
/*     */     }
/*     */ 
/* 138 */     return false;
/*     */   }
/*     */ 
/*     */   public static int findHtmlTagsEx(Reader reader, ParseOutput parseOutput, String[] tags, boolean noStop, boolean writeToOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 144 */     char[] chs = { '>', '<' };
/* 145 */     char[][] tagChsArray = new char[tags.length][];
/* 146 */     for (int k = 0; k < tags.length; ++k)
/*     */     {
/* 148 */       tagChsArray[k] = tags[k].toCharArray();
/*     */     }
/*     */ 
/* 151 */     if (findCharacterEx(reader, parseOutput, '<', true, writeToOutput) == true)
/*     */     {
/*     */       while (true)
/*     */       {
/* 155 */         parseOutput.copyToPending(writeToOutput, true);
/*     */ 
/* 158 */         int index = findCharactersEx(reader, parseOutput, chs, noStop, writeToOutput);
/* 159 */         if (index < 0) {
/* 160 */           return -1;
/*     */         }
/* 162 */         if (index == 0)
/*     */         {
/* 165 */           for (int i = 0; i < tags.length; ++i)
/*     */           {
/* 168 */             if (parseOutput.isBufferEqualNoCase(tagChsArray[i], tagChsArray[i].length)) {
/* 169 */               return i;
/*     */             }
/*     */           }
/*     */         }
/* 173 */         if (parseOutput.m_writer != null)
/*     */         {
/* 175 */           parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = '<';
/*     */         }
/*     */ 
/* 178 */         if (index == 0)
/*     */         {
/* 180 */           parseOutput.copyToPending(writeToOutput, false);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 185 */     return -1;
/*     */   }
/*     */ 
/*     */   public static int findCharactersEx(Reader reader, ParseOutput parseOutput, char[] chs, boolean noStop, boolean writeToOutput)
/*     */     throws IOException
/*     */   {
/* 191 */     parseOutput.copyToPending(false, false);
/*     */ 
/* 194 */     char[] temp = parseOutput.m_outputBuf;
/*     */     while (true)
/*     */     {
/* 198 */       int offset = parseOutput.m_readOffset;
/*     */ 
/* 200 */       int numRead = parseOutput.m_numRead;
/* 201 */       if ((numRead < temp.length / 2) && (reader.ready()))
/*     */       {
/* 203 */         numRead += reader.read(temp, numRead + offset, temp.length - numRead - offset);
/*     */ 
/* 205 */         parseOutput.m_numRead = numRead;
/*     */       }
/*     */ 
/* 208 */       if (numRead <= 0)
/*     */       {
/* 210 */         parseOutput.m_isEOF = true;
/* 211 */         break;
/*     */       }
/*     */ 
/* 215 */       for (int i = 0; i < numRead; ++i)
/*     */       {
/* 217 */         char tch = temp[(i + offset)];
/* 218 */         for (int j = 0; j < chs.length; ++j)
/*     */         {
/* 220 */           if (tch != chs[j])
/*     */             continue;
/* 222 */           parseOutput.m_numWaiting = (i + 1);
/* 223 */           return j;
/*     */         }
/*     */       }
/*     */ 
/* 227 */       parseOutput.m_numWaiting = numRead;
/*     */ 
/* 229 */       if (!noStop) {
/*     */         break;
/*     */       }
/*     */ 
/* 233 */       parseOutput.copyToPending(writeToOutput, false);
/*     */     }
/*     */ 
/* 236 */     return -1;
/*     */   }
/*     */ 
/*     */   public static boolean findCharacterEx(Reader reader, ParseOutput parseOutput, char ch, boolean noStop, boolean writeToOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 243 */     boolean insideLiteral = false;
/* 244 */     boolean skipNext = false;
/* 245 */     char literalChar = '-';
/*     */ 
/* 248 */     parseOutput.copyToPending(false, false);
/*     */ 
/* 251 */     char[] temp = parseOutput.m_outputBuf;
/*     */     while (true)
/*     */     {
/* 255 */       int offset = parseOutput.m_readOffset;
/*     */ 
/* 257 */       int numRead = parseOutput.m_numRead;
/* 258 */       if ((numRead < temp.length / 2) && (reader.ready()))
/*     */       {
/* 260 */         int nchars = reader.read(temp, numRead + offset, temp.length - numRead - offset);
/*     */ 
/* 262 */         if (nchars > 0)
/*     */         {
/* 264 */           numRead += nchars;
/* 265 */           parseOutput.m_numRead = numRead;
/*     */         }
/*     */       }
/*     */ 
/* 269 */       if (numRead <= 0)
/*     */       {
/* 271 */         parseOutput.m_isEOF = true;
/* 272 */         break;
/*     */       }
/*     */ 
/* 276 */       for (int i = 0; i < numRead; ++i)
/*     */       {
/* 278 */         char tch = temp[(i + offset)];
/* 279 */         if (insideLiteral)
/*     */         {
/* 281 */           if (!skipNext)
/*     */           {
/* 283 */             if (tch == literalChar)
/*     */             {
/* 285 */               insideLiteral = false;
/*     */             } else {
/* 287 */               if (tch != '\\')
/*     */                 continue;
/* 289 */               skipNext = true;
/*     */             }
/*     */ 
/*     */           }
/*     */           else {
/* 294 */             skipNext = false;
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 299 */           if (tch == ch)
/*     */           {
/* 301 */             parseOutput.m_numWaiting = (i + 1);
/* 302 */             return true;
/*     */           }
/* 304 */           if ((noStop) || ((tch != '\'') && (tch != '"')))
/*     */             continue;
/* 306 */           insideLiteral = true;
/* 307 */           literalChar = tch;
/*     */         }
/*     */       }
/*     */ 
/* 311 */       parseOutput.m_numWaiting = numRead;
/*     */ 
/* 313 */       if (!noStop) {
/*     */         break;
/*     */       }
/*     */ 
/* 317 */       parseOutput.copyToPending(writeToOutput, false);
/*     */     }
/*     */ 
/* 320 */     if (insideLiteral)
/*     */     {
/* 322 */       parseOutput.createParsingException(LocaleUtils.encodeMessage("csParserUnmatchedChar", null, "" + literalChar));
/*     */     }
/*     */ 
/* 325 */     return false;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 330 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.CWParser
 * JD-Core Version:    0.5.4
 */