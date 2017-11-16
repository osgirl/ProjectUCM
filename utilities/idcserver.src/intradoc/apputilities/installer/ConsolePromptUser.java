/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConsolePromptUser
/*     */   implements PromptUser
/*     */ {
/*     */   public int m_maxDisplayCount;
/*     */   protected boolean m_onNewLine;
/*     */   protected int m_lastMessageLength;
/*     */   protected boolean m_isQuiet;
/*     */   public int m_lineLength;
/*     */   public int m_screenHeight;
/*     */   public String m_preSelectionString;
/*     */   public String m_postSelectionString;
/*     */   public int m_tabStop;
/*     */   public Map m_unusualUnicodeBlocks;
/*     */   public boolean m_useNative;
/*     */   public NativeOsUtils m_utils;
/*     */ 
/*     */   public ConsolePromptUser()
/*     */   {
/*  46 */     this.m_maxDisplayCount = 16;
/*  47 */     this.m_onNewLine = true;
/*  48 */     this.m_lastMessageLength = 0;
/*  49 */     this.m_isQuiet = false;
/*     */ 
/*  51 */     this.m_lineLength = 80;
/*  52 */     this.m_screenHeight = 25;
/*  53 */     this.m_preSelectionString = "";
/*  54 */     this.m_postSelectionString = "";
/*  55 */     this.m_tabStop = 8;
/*     */ 
/*  57 */     this.m_unusualUnicodeBlocks = new HashMap();
/*     */ 
/*  59 */     this.m_useNative = true;
/*  60 */     this.m_utils = null;
/*     */   }
/*     */ 
/*     */   public void setLineLength(int length) {
/*  64 */     if ((length <= 30) || (length >= 1000))
/*     */       return;
/*  66 */     this.m_lineLength = length;
/*     */   }
/*     */ 
/*     */   public int getLineLength()
/*     */   {
/*  72 */     return this.m_lineLength;
/*     */   }
/*     */ 
/*     */   public void setScreenHeight(int height)
/*     */   {
/*  77 */     if ((height <= 10) || (height >= 1000))
/*     */       return;
/*  79 */     this.m_screenHeight = height;
/*     */   }
/*     */ 
/*     */   public void setSelectionStrings(String pre, String post)
/*     */   {
/*  85 */     this.m_preSelectionString = pre;
/*  86 */     this.m_postSelectionString = post;
/*     */   }
/*     */ 
/*     */   public int getScreenHeight()
/*     */   {
/*  91 */     return this.m_screenHeight;
/*     */   }
/*     */ 
/*     */   public String prompt(int type, String label, String defValue, Object data, String explaination)
/*     */   {
/*  98 */     IdcMessage labelMessage = LocaleUtils.parseMessage(label);
/*     */ 
/* 100 */     IdcMessage explainationMessage = null;
/* 101 */     if (explaination != null)
/*     */     {
/* 104 */       explainationMessage = LocaleUtils.parseMessage(explaination);
/*     */     }
/* 106 */     return prompt(type, labelMessage, defValue, data, explainationMessage);
/*     */   }
/*     */ 
/*     */   public String prompt(int type, IdcMessage label, String defValue, Object data, IdcMessage explaination)
/*     */   {
/* 112 */     out(LocaleResources.getString("csLinefeed", null));
/*     */ 
/* 115 */     String labelText = LocaleResources.localizeMessage(null, label, null).toString();
/* 116 */     int maxLines = this.m_screenHeight - 3;
/* 117 */     if (maxLines < 10)
/*     */     {
/* 119 */       maxLines = 10;
/*     */     }
/* 121 */     if (explaination != null)
/*     */     {
/* 123 */       String explainationText = LocaleResources.localizeMessage(null, explaination, null).toString();
/*     */ 
/* 125 */       outputParagraph(explainationText);
/*     */     }
/*     */ 
/* 128 */     switch (type)
/*     */     {
/*     */     case 0:
/*     */     case 3:
/* 132 */       String key = (defValue == null) ? "csInstallerConsoleTextMsg2" : "csInstallerConsoleTextMsg1";
/* 133 */       out(LocaleResources.getString(key, null, label, defValue));
/* 134 */       return readResponse(defValue, type);
/*     */     case 1:
/*     */     case 2:
/* 139 */       String moreString = LocaleResources.getString("csInstallerMoreChoicesKey", null);
/*     */ 
/* 141 */       String finishedString = LocaleResources.getString("csInstallerFinishedChoicesKey", null);
/*     */ 
/* 143 */       String[][] list = (String[][])(String[][])data;
/* 144 */       ArrayList multiselectValues = new ArrayList();
/* 145 */       if (type == 2)
/*     */       {
/* 147 */         StringUtils.appendListFromSequence(multiselectValues, defValue, 0, defValue.length(), ',', '^', 64);
/*     */       }
/*     */       else
/*     */       {
/* 153 */         multiselectValues.add(defValue);
/*     */       }
/* 155 */       int choiceLength = ("" + list.length).length();
/* 156 */       outln(labelText);
/* 157 */       int first = 0;
/*     */       while (true)
/*     */       {
/* 160 */         int last = list.length;
/* 161 */         if (last - first > maxLines)
/*     */         {
/* 163 */           last = first + maxLines;
/*     */         }
/* 165 */         for (int i = first; i < last; ++i)
/*     */         {
/* 167 */           boolean isSelected = multiselectValues.indexOf(list[i][0]) >= 0;
/* 168 */           out(createChoice("" + (i + 1), list[i][1], choiceLength, isSelected));
/*     */ 
/* 170 */           if (isSelected)
/*     */           {
/* 172 */             defValue = "" + (1 + i);
/*     */           }
/* 174 */           out(LocaleResources.getString("csLinefeed", null));
/*     */         }
/* 176 */         if (list.length > maxLines)
/*     */         {
/* 178 */           String tmp = LocaleResources.getString("csInstallerMoreChoices", null);
/* 179 */           out(createChoice(moreString, tmp, choiceLength, false));
/* 180 */           out(LocaleResources.getString("csLinefeed", null));
/*     */         }
/*     */ 
/* 183 */         String tmp = null;
/*     */         while (true)
/*     */         {
/*     */           String msg;
/*     */           String msg;
/* 187 */           if (type == 2)
/*     */           {
/* 189 */             msg = LocaleResources.getString("csInstallerMultiselectChoicePrompt", null, "csInstallerFinishedChoicesKey");
/*     */           }
/*     */           else
/*     */           {
/* 195 */             msg = LocaleResources.getString("csInstallerChoicePrompt", null);
/*     */           }
/*     */ 
/* 198 */           out(msg);
/*     */ 
/* 200 */           if (type == 2)
/*     */           {
/* 202 */             tmp = readResponse(finishedString, 0);
/*     */           }
/*     */           else
/*     */           {
/* 206 */             tmp = readResponse(defValue, 0);
/*     */           }
/* 208 */           if (type == 2)
/*     */           {
/* 210 */             List l = StringUtils.makeListFromSequence(tmp, ',', '^', 64);
/*     */ 
/* 212 */             boolean isFinished = false;
/* 213 */             for (int i = 0; i < l.size(); ++i)
/*     */             {
/* 215 */               tmp = (String)l.get(i);
/* 216 */               if ((i + 1 == l.size()) && (tmp.equalsIgnoreCase(finishedString)))
/*     */               {
/* 218 */                 isFinished = true;
/* 219 */                 break;
/*     */               }
/* 221 */               int index = NumberUtils.parseInteger(tmp, -1) - 1;
/*     */ 
/* 223 */               if (-1 == index)
/*     */               {
/* 225 */                 multiselectValues.clear();
/*     */               }
/* 228 */               else if ((index < 0) || (index >= list.length))
/*     */               {
/* 231 */                 tmp = null;
/*     */               }
/*     */               else {
/* 234 */                 String tmpValue = list[index][0];
/* 235 */                 if (multiselectValues.indexOf(tmpValue) >= 0)
/*     */                 {
/* 237 */                   multiselectValues.remove(tmpValue);
/*     */                 }
/*     */                 else
/*     */                 {
/* 241 */                   multiselectValues.add(tmpValue);
/*     */                 }
/*     */               }
/*     */             }
/* 244 */             if (isFinished)
/*     */             {
/* 246 */               String value = StringUtils.createString(multiselectValues, ',', '^');
/*     */ 
/* 248 */               return value;
/*     */             }
/* 250 */             outputMessage("");
/* 251 */             break;
/*     */           }
/*     */           try
/*     */           {
/* 255 */             if (tmp.equalsIgnoreCase(moreString))
/*     */             {
/* 257 */               first += maxLines;
/* 258 */               if (first >= list.length)
/*     */               {
/* 260 */                 first = 0;
/*     */               }
/* 262 */               break label727:
/*     */             }
/* 264 */             int i = Integer.parseInt(tmp);
/* 265 */             tmp = list[(i - 1)][0];
/* 266 */             label727: return tmp;
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/* 270 */             tmp = null;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 277 */     throw new IllegalArgumentException(LocaleUtils.encodeMessage("csInstallerUnknownType", null, "" + type));
/*     */   }
/*     */ 
/*     */   protected String createChoice(String choice, String label, int length, boolean isSelected)
/*     */   {
/* 288 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 289 */     buf.append("\t");
/* 290 */     int i = choice.length();
/* 291 */     if (!isSelected)
/*     */     {
/* 293 */       ++length;
/*     */     }
/* 295 */     while (i++ < length)
/*     */     {
/* 297 */       buf.append(" ");
/*     */     }
/* 299 */     if (isSelected)
/*     */     {
/* 301 */       buf.append(this.m_preSelectionString + "*");
/*     */     }
/* 303 */     buf.append(choice);
/* 304 */     buf.append(". ");
/* 305 */     buf.append(label);
/* 306 */     buf.append(this.m_postSelectionString);
/* 307 */     String str = buf.toString();
/* 308 */     buf.releaseBuffers();
/* 309 */     return str;
/*     */   }
/*     */ 
/*     */   protected String readResponse(String defValue, int type)
/*     */   {
/* 314 */     String value = null;
/* 315 */     if (this.m_useNative)
/*     */     {
/*     */       try
/*     */       {
/* 319 */         if (this.m_utils == null)
/*     */         {
/* 321 */           this.m_utils = new NativeOsUtils();
/*     */         }
/* 323 */         int flags = 0;
/* 324 */         if (type == 3)
/*     */         {
/* 326 */           flags |= 1;
/*     */         }
/* 328 */         value = this.m_utils.readConsole(flags);
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 332 */         ignore.printStackTrace();
/*     */       }
/* 334 */       if (value == null)
/*     */       {
/* 336 */         this.m_useNative = false;
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 342 */       if (value == null)
/*     */       {
/* 344 */         byte[] buf = new byte[8192];
/* 345 */         int count = System.in.read(buf);
/* 346 */         if (count == -1)
/*     */         {
/* 348 */           return defValue;
/*     */         }
/* 350 */         value = new String(buf, 0, count);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 355 */       return defValue;
/*     */     }
/*     */ 
/* 358 */     value = value.trim();
/*     */ 
/* 360 */     if (value.length() == 0)
/*     */     {
/* 362 */       return defValue;
/*     */     }
/* 364 */     return value;
/*     */   }
/*     */ 
/*     */   public int outputParagraph(String text)
/*     */   {
/* 369 */     int totalLines = 0;
/*     */ 
/* 374 */     text = formatParagraph(text, this.m_lineLength - 2);
/* 375 */     BufferedReader r = new BufferedReader(new StringReader(text));
/* 376 */     int lineCount = 0;
/*     */     try
/*     */     {
/* 379 */       while ((line = r.readLine()) != null)
/*     */       {
/*     */         String line;
/* 381 */         outputMessage(line);
/* 382 */         ++totalLines;
/* 383 */         ++lineCount;
/* 384 */         if (lineCount + 3 != this.m_screenHeight)
/*     */           continue;
/* 386 */         lineCount = 0;
/* 387 */         String msg = LocaleResources.getString("csPressEnterToContinue", null);
/*     */ 
/* 389 */         updateMessage(msg);
/* 390 */         readResponse(null, 0);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 396 */       Report.trace("install", null, ignore);
/*     */     }
/* 398 */     return totalLines;
/*     */   }
/*     */ 
/*     */   public String formatParagraph(String text, int max)
/*     */   {
/* 406 */     int length = text.length();
/* 407 */     char[] chars = new char[length];
/* 408 */     text.getChars(0, length, chars, 0);
/* 409 */     IdcStringBuilder result = new IdcStringBuilder();
/*     */ 
/* 411 */     char s = 'S';
/* 412 */     int start = 0;
/* 413 */     int wordLength = 0;
/* 414 */     int lineLength = 0;
/* 415 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 417 */       char c = chars[i];
/* 418 */       switch (s)
/*     */       {
/*     */       case 'H':
/*     */       case 'T':
/* 423 */         boolean end = c == '>';
/* 424 */         if (end)
/*     */         {
/* 426 */           if (c == '>')
/*     */           {
/* 428 */             ++wordLength;
/*     */           }
/* 430 */           if (c == '<')
/*     */           {
/* 432 */             --i;
/*     */           }
/*     */ 
/* 435 */           s = 'S';
/* 436 */           lineLength = appendWord(result, max, wordLength, lineLength, start, chars);
/* 437 */           wordLength = 0;
/*     */         }
/*     */         else
/*     */         {
/* 441 */           ++wordLength;
/*     */         }
/* 443 */         break;
/*     */       case 'S':
/* 446 */         if (Character.isWhitespace(c))
/*     */           continue;
/* 448 */         s = (c == '<') ? 'H' : 'T';
/* 449 */         wordLength = 1;
/* 450 */         start = i;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 455 */     if (wordLength > 0)
/*     */     {
/* 457 */       appendWord(result, max, wordLength, lineLength, start, chars);
/*     */     }
/*     */ 
/* 460 */     String str = result.toString();
/* 461 */     result.releaseBuffers();
/* 462 */     return str;
/*     */   }
/*     */ 
/*     */   protected int appendWord(IdcStringBuilder buf, int m, int w, int l, int s, char[] c)
/*     */   {
/* 467 */     String word = new String(c, s, w);
/* 468 */     int columns = computeLength(word);
/* 469 */     if (word.equals("<br>"))
/*     */     {
/* 471 */       buf.append("\n");
/* 472 */       return 0;
/*     */     }
/* 474 */     if (word.equals("<p>"))
/*     */     {
/* 476 */       buf.append("\n\n");
/* 477 */       return 0;
/*     */     }
/*     */ 
/* 480 */     if ((l > 0) && (l + columns >= m))
/*     */     {
/* 482 */       buf.append('\n');
/* 483 */       l = columns + 1;
/*     */     }
/*     */     else
/*     */     {
/* 487 */       l += columns + 1;
/*     */     }
/* 489 */     buf.append(c, s, w);
/* 490 */     buf.append(' ');
/*     */ 
/* 492 */     return l;
/*     */   }
/*     */ 
/*     */   public int computeLength(String message)
/*     */   {
/* 497 */     Character.UnicodeBlock[] wideBlocks = { Character.UnicodeBlock.CJK_COMPATIBILITY, Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS, Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS, Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT, Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION, Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS, Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS, Character.UnicodeBlock.KATAKANA, Character.UnicodeBlock.HIRAGANA, Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS };
/*     */ 
/* 514 */     int size = message.length();
/* 515 */     int length = size;
/* 516 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 518 */       boolean found = false;
/* 519 */       char c = message.charAt(i);
/* 520 */       Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
/* 521 */       for (int b = 0; b < wideBlocks.length; ++b)
/*     */       {
/* 523 */         if (block != wideBlocks[b])
/*     */           continue;
/* 525 */         ++length;
/* 526 */         found = true;
/* 527 */         break;
/*     */       }
/*     */ 
/* 530 */       if ((SystemUtils.m_isDevelopmentEnvironment) && (!found) && (c > 'Ȁ') && 
/* 532 */         (this.m_unusualUnicodeBlocks.get(block) == null))
/*     */       {
/* 534 */         this.m_unusualUnicodeBlocks.put(block, block);
/* 535 */         SystemUtils.trace("install", "found unusual block " + block + " for character code " + c);
/*     */       }
/*     */ 
/* 540 */       if (c != '\t')
/*     */         continue;
/* 542 */       length += this.m_tabStop - i % this.m_tabStop;
/*     */     }
/*     */ 
/* 545 */     return length;
/*     */   }
/*     */ 
/*     */   public String trimStringMid(String str)
/*     */   {
/* 550 */     int length = computeLength(str);
/* 551 */     int targetLength = this.m_lineLength - 4;
/* 552 */     int over = length - targetLength;
/* 553 */     if (over > 0)
/*     */     {
/* 555 */       int index = str.lastIndexOf(":");
/* 556 */       if (index < 0)
/*     */       {
/* 560 */         index = 0;
/*     */       }
/* 562 */       int l = str.length() - index;
/* 563 */       if (l <= over + 3)
/*     */       {
/* 567 */         l = str.length();
/* 568 */         index = 0;
/*     */       }
/* 570 */       if (l > over + 3)
/*     */       {
/* 576 */         int left = index + l / 2 - over / 2 - 2;
/* 577 */         int right = index + l / 2 + (over + 1) / 2 + 1;
/* 578 */         str = str.substring(0, left) + "..." + str.substring(right);
/*     */       }
/*     */     }
/*     */ 
/* 582 */     return str;
/*     */   }
/*     */ 
/*     */   public String[] fixMessageLength(String message)
/*     */   {
/* 587 */     int length = computeLength(message);
/* 588 */     int targetLength = this.m_lineLength - 2;
/* 589 */     if (length <= targetLength)
/*     */     {
/* 591 */       return new String[] { message };
/*     */     }
/* 593 */     length = message.length();
/*     */ 
/* 595 */     Vector list = new IdcVector();
/* 596 */     int tabIndex = message.indexOf("\t");
/*     */ 
/* 600 */     boolean hasTab = (tabIndex >= 0) && (tabIndex < this.m_lineLength - 2);
/*     */ 
/* 602 */     int lineStartOffset = 0;
/*     */ 
/* 606 */     int[] lastBreakOffsets = new int[2];
/* 607 */     int lineLength = 0;
/* 608 */     int lineCount = 0;
/* 609 */     for (int stringOffset = 0; stringOffset <= length; ++stringOffset)
/*     */     {
/* 611 */       boolean breakLine = false;
/*     */       char c;
/*     */       char c;
/* 613 */       if (stringOffset < length)
/*     */       {
/* 615 */         c = message.charAt(stringOffset);
/*     */       }
/*     */       else
/*     */       {
/* 619 */         c = '\n';
/*     */       }
/* 621 */       ++lineLength;
/* 622 */       switch (c)
/*     */       {
/*     */       case '\t':
/* 625 */         lastBreakOffsets[0] = stringOffset;
/* 626 */         lineLength += this.m_tabStop - lineLength % this.m_tabStop - 1;
/* 627 */         break;
/*     */       case ' ':
/* 629 */         lastBreakOffsets[0] = stringOffset;
/* 630 */         break;
/*     */       case '\n':
/* 632 */         lastBreakOffsets[0] = stringOffset;
/* 633 */         breakLine = true;
/* 634 */         break;
/*     */       case '-':
/*     */       case '.':
/*     */       case '/':
/*     */       case ';':
/*     */       case '\\':
/*     */       case '_':
/*     */       case '|':
/* 642 */         lastBreakOffsets[1] = stringOffset;
/*     */       }
/*     */ 
/* 646 */       if (lineLength >= targetLength)
/*     */       {
/* 648 */         breakLine = true;
/*     */       }
/*     */ 
/* 651 */       if (!breakLine)
/*     */         continue;
/*     */       String line;
/* 655 */       if (lastBreakOffsets[0] > 0)
/*     */       {
/* 657 */         String line = message.substring(lineStartOffset, lastBreakOffsets[0]);
/*     */ 
/* 659 */         lineStartOffset = lastBreakOffsets[0] + 1;
/* 660 */         lineLength = 0;
/*     */       }
/* 662 */       else if (lastBreakOffsets[1] > 0)
/*     */       {
/* 664 */         String line = message.substring(lineStartOffset, lastBreakOffsets[1] + 1);
/*     */ 
/* 666 */         lineStartOffset = lastBreakOffsets[0] + 1;
/* 667 */         lineLength = 0;
/*     */       }
/*     */       else
/*     */       {
/* 672 */         line = message.substring(lineStartOffset, stringOffset);
/*     */ 
/* 674 */         lineLength = 0;
/* 675 */         lineStartOffset = stringOffset + 1;
/*     */       }
/*     */ 
/* 678 */       if (lineCount > 0)
/*     */       {
/* 680 */         if (hasTab)
/*     */         {
/* 682 */           line = "\t" + line;
/*     */         }
/* 684 */         lineLength += this.m_tabStop;
/*     */       }
/* 686 */       list.addElement(line);
/* 687 */       lastBreakOffsets = new int[2];
/* 688 */       ++lineCount;
/*     */     }
/*     */ 
/* 692 */     String[] msgArray = new String[list.size()];
/* 693 */     list.copyInto(msgArray);
/* 694 */     return msgArray;
/*     */   }
/*     */ 
/*     */   public void outputMessage(String msg)
/*     */   {
/* 699 */     String[] msgList = fixMessageLength(msg);
/* 700 */     updateMessageEx(msgList[0]);
/* 701 */     if (this.m_lastMessageLength > 0)
/*     */     {
/* 703 */       outln("");
/*     */     }
/* 705 */     for (int i = 1; i < msgList.length; ++i)
/*     */     {
/* 707 */       outln(msgList[i]);
/*     */     }
/* 709 */     this.m_lastMessageLength = 0;
/* 710 */     this.m_onNewLine = true;
/*     */   }
/*     */ 
/*     */   public void updateMessage(String msg)
/*     */   {
/* 715 */     if (this.m_isQuiet)
/*     */     {
/* 717 */       return;
/*     */     }
/* 719 */     updateMessageEx(msg);
/*     */   }
/*     */ 
/*     */   public void updateMessageEx(String msg)
/*     */   {
/* 724 */     if (SystemUtils.m_verbose)
/*     */     {
/* 726 */       outln(msg);
/* 727 */       this.m_onNewLine = true;
/* 728 */       return;
/*     */     }
/*     */ 
/* 731 */     int length = computeLength(msg);
/*     */ 
/* 740 */     IdcStringBuilder buf = new IdcStringBuilder("\r");
/*     */ 
/* 742 */     while (this.m_lastMessageLength-- > 0)
/*     */     {
/* 744 */       buf.append(" ");
/*     */     }
/* 746 */     String str = buf.toString();
/* 747 */     buf.releaseBuffers();
/* 748 */     out(str);
/* 749 */     out("\r");
/* 750 */     out(msg);
/* 751 */     this.m_lastMessageLength = length;
/* 752 */     this.m_onNewLine = false;
/*     */   }
/*     */ 
/*     */   public boolean getQuiet()
/*     */   {
/* 757 */     return this.m_isQuiet;
/*     */   }
/*     */ 
/*     */   public boolean setQuiet(boolean quiet)
/*     */   {
/* 762 */     boolean tmp = this.m_isQuiet;
/* 763 */     this.m_isQuiet = quiet;
/* 764 */     return tmp;
/*     */   }
/*     */ 
/*     */   public void finalizeOutput()
/*     */   {
/* 769 */     if (this.m_onNewLine)
/*     */       return;
/* 771 */     outln("");
/*     */   }
/*     */ 
/*     */   protected void out(String msg)
/*     */   {
/* 777 */     if (this.m_useNative)
/*     */     {
/*     */       try
/*     */       {
/* 781 */         if (this.m_utils == null)
/*     */         {
/* 783 */           this.m_utils = new NativeOsUtils();
/*     */         }
/* 785 */         int rc = this.m_utils.writeConsole(msg, 0);
/* 786 */         if (rc == 0)
/*     */         {
/* 788 */           return;
/*     */         }
/* 790 */         Report.trace("install", "unable to write to console: " + this.m_utils.getErrorMessage(rc), null);
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/* 795 */         Report.trace("install", null, ignore);
/*     */       }
/* 797 */       this.m_useNative = false;
/*     */     }
/* 799 */     SystemUtils.out(msg);
/*     */   }
/*     */ 
/*     */   protected void outln(String msg)
/*     */   {
/* 804 */     out(msg);
/* 805 */     out("\n");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 810 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.ConsolePromptUser
 * JD-Core Version:    0.5.4
 */