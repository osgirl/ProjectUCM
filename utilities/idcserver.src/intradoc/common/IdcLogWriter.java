/*     */ package intradoc.common;
/*     */ 
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.CharArrayReader;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.io.Writer;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Calendar;
/*     */ import java.util.Date;
/*     */ 
/*     */ public class IdcLogWriter
/*     */   implements LogWriter
/*     */ {
/*  35 */   public static char[] m_logFooterArray = { '/', 't', 'a', 'b', 'l', 'e' };
/*  36 */   public static String m_logFooter = null;
/*     */ 
/*     */   public void initFooter()
/*     */   {
/*  40 */     if (m_logFooter != null)
/*     */     {
/*  42 */       return;
/*     */     }
/*  44 */     m_logFooter = "</table>\n<a name=\"end\"><hr /></a>\n<script type=\"text/javascript\">\nvar div = document.getElementById(\"loading_div\");\nif (div)\n{\n\tdiv.style.display = \"none\";\n}\n\nvar table = document.getElementById(\"log_table\");\nif (table)\n{\n\tvar rows = table.childNodes.item(table.childNodes.length-1);\n\tvar rowCount = rows.childNodes.length;\n\tvar lastRow = null;\n\twhile (rowCount > 0 && (lastRow == null || \n\t  !(lastRow.childNodes && lastRow.childNodes.length > 1)))\n\t{\n\t\tlastRow = rows.childNodes.item(--rowCount);\n\t}\n\tif (lastRow)\n\t{\n\t\tvar timeCell = lastRow.cells.item(1);\n\t\tvar timeText = timeCell.childNodes.item(0).cloneNode(false);\n\t\tvar recentEntryDiv = document.getElementById(\"recent_entry_div\");\n\t\trecentEntryDiv.appendChild(document.createTextNode(\"" + LocaleResources.getString("csLogFileLastModified", null) + "\"));\n" + "\t\trecentEntryDiv.appendChild(timeText);\n" + "\t}\n" + "}\n" + "</script>\n" + "</body></html>";
/*     */   }
/*     */ 
/*     */   public void doMessageAppend(int messageType, String msg, LogDirInfo logDirInfo, Throwable t)
/*     */   {
/*  68 */     if ((FileUtils.m_javaSystemEncoding == null) || (LocaleResources.m_systemLocale == null))
/*     */     {
/*  73 */       Report.trace("system", "Uninitialized system, cannot write message to log file: " + msg, t);
/*  74 */       return;
/*     */     }
/*     */ 
/*  77 */     String rawDesc = Log.getRawDesc(messageType);
/*  78 */     String desc = LocaleResources.getString("csLogDesc" + rawDesc, null);
/*     */ 
/*  81 */     if ((t == null) && (messageType >= 2))
/*     */     {
/*  83 */       SystemUtils.reportDeprecatedUsage("An error has been logged without a Throwable object.");
/*     */ 
/*  85 */       t = new StackTrace();
/*     */     }
/*     */ 
/*  89 */     File file = null;
/*  90 */     Exception errData = null;
/*  91 */     boolean replaceFile = false;
/*  92 */     Calendar curTime = Calendar.getInstance();
/*  93 */     String dir = logDirInfo.m_dir;
/*     */ 
/*  97 */     File tempFile = new File(dir, "temp.log");
/*  98 */     RandomAccessFile accessFile = null;
/*  99 */     Writer tempOutput = null;
/*     */     try
/*     */     {
/* 104 */       FileUtils.reserveDirectory(dir);
/*     */ 
/* 107 */       if (!logDirInfo.m_linkPage)
/*     */       {
/* 109 */         formatLinkPage(logDirInfo);
/*     */       }
/*     */ 
/* 114 */       int curSuffix = logDirInfo.m_curSuffix;
/*     */ 
/* 117 */       IdcCharArrayWriter arrayWriter = null;
/*     */ 
/* 120 */       for (int i = 0; i < 3; ++i)
/*     */       {
/* 123 */         char m_firstChar = (char)(curSuffix / 10 + 48);
/* 124 */         char m_secChar = (char)(curSuffix % 10 + 48);
/* 125 */         LogFileInfo fileInfo = getLogFileInfo(logDirInfo, "" + m_firstChar + m_secChar);
/*     */ 
/* 127 */         boolean isUsable = false;
/* 128 */         file = new File(dir, fileInfo.m_name);
/* 129 */         boolean fileExists = file.exists();
/* 130 */         if (!fileExists)
/*     */         {
/* 134 */           if (tempFile.exists())
/*     */           {
/* 136 */             tempFile.renameTo(file);
/*     */           }
/*     */           else
/*     */           {
/* 140 */             replaceFile = true;
/*     */           }
/*     */         }
/*     */ 
/* 144 */         if (!replaceFile)
/*     */         {
/* 147 */           accessFile = findDate(file, fileInfo);
/* 148 */           if (accessFile == null)
/*     */           {
/* 150 */             replaceFile = true;
/*     */           }
/*     */         }
/* 153 */         if (replaceFile == true)
/*     */         {
/* 155 */           if (fileExists)
/*     */           {
/* 157 */             file.delete();
/*     */           }
/* 159 */           fileExists = false;
/* 160 */           isUsable = true;
/*     */         }
/*     */         else
/*     */         {
/* 168 */           Calendar cal = fileInfo.m_timeStamp;
/* 169 */           if ((cal.get(1) == curTime.get(1)) && (cal.get(2) == curTime.get(2)) && (cal.get(5) == curTime.get(5)))
/*     */           {
/* 173 */             isUsable = true;
/*     */           }
/*     */         }
/*     */ 
/* 177 */         if (isUsable)
/*     */         {
/* 179 */           tempOutput = null;
/* 180 */           if (replaceFile)
/*     */           {
/* 182 */             tempOutput = FileUtils.openDataWriter(tempFile, FileUtils.m_javaSystemEncoding);
/*     */ 
/* 184 */             String htmlStr = generateLogHeader(curTime, logDirInfo);
/* 185 */             tempOutput.write(htmlStr);
/* 186 */             if (errData != null)
/*     */             {
/* 188 */               tempOutput.write(generateLogEntry(curTime, rawDesc, null, errData));
/*     */ 
/* 190 */               errData = null;
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 195 */             if (arrayWriter != null)
/*     */             {
/* 197 */               arrayWriter.reset();
/*     */             }
/*     */             else
/*     */             {
/* 201 */               arrayWriter = new IdcCharArrayWriter();
/*     */             }
/* 203 */             tempOutput = arrayWriter;
/* 204 */             ParseOutput parseOutput = new ParseOutput();
/* 205 */             parseOutput.m_writer = arrayWriter;
/*     */ 
/* 209 */             char[] buf = new char[1000];
/* 210 */             long startOffset = accessFile.length() - buf.length;
/* 211 */             if (startOffset < 0L)
/*     */             {
/* 213 */               startOffset = 0L;
/*     */             }
/*     */ 
/* 216 */             accessFile.seek(startOffset);
/* 217 */             int amtRead = readAsCharacters(accessFile, buf);
/* 218 */             CharArrayReader reader = new CharArrayReader(buf, 0, amtRead);
/*     */ 
/* 221 */             boolean result = Parser.findHtmlTagPrefix(reader, parseOutput, m_logFooterArray);
/*     */ 
/* 223 */             parseOutput.writePending();
/*     */ 
/* 226 */             startOffset += arrayWriter.size();
/* 227 */             accessFile.seek(startOffset);
/* 228 */             if (!result)
/*     */             {
/* 230 */               errData = new Exception(LocaleUtils.encodeMessage("csLogCorrupt", null, file.getAbsolutePath()));
/*     */             }
/* 233 */             else if (errData != null)
/*     */             {
/* 235 */               tempOutput.write(generateLogEntry(curTime, rawDesc, null, errData));
/*     */ 
/* 237 */               errData = null;
/*     */             }
/* 239 */             parseOutput.releaseBuffers();
/* 240 */             arrayWriter.reset();
/*     */           }
/*     */ 
/* 243 */           if (errData == null)
/*     */           {
/* 246 */             tempOutput.write(generateLogEntry(curTime, rawDesc, msg, t));
/*     */ 
/* 249 */             initFooter();
/* 250 */             tempOutput.write(m_logFooter);
/*     */ 
/* 253 */             tempOutput.close();
/*     */ 
/* 256 */             if ((arrayWriter != null) && (arrayWriter.size() > 0))
/*     */             {
/* 258 */               byte[] outBuf = arrayWriter.toString().getBytes(FileUtils.m_javaSystemEncoding);
/*     */ 
/* 260 */               accessFile.write(outBuf);
/*     */             }
/*     */ 
/* 264 */             if (accessFile != null)
/*     */             {
/* 266 */               accessFile.close();
/* 267 */               accessFile = null;
/*     */             }
/*     */ 
/* 271 */             if ((arrayWriter != null) && (arrayWriter.size() != 0))
/*     */               break;
/* 273 */             if (fileExists)
/*     */             {
/* 275 */               file.delete();
/*     */             }
/* 277 */             tempFile.renameTo(file);
/*     */ 
/* 280 */             if ((!tempFile.exists()) || 
/* 284 */               (!replaceFile))
/*     */               break;
/* 286 */             tempFile.delete();
/* 287 */             throw new IOException("!syFileUtilsFileLocked");
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 296 */         file = null;
/* 297 */         if (accessFile != null)
/*     */         {
/* 299 */           accessFile.close();
/* 300 */           accessFile = null;
/*     */         }
/*     */ 
/* 304 */         int oldSuffix = curSuffix;
/* 305 */         if (i == 0)
/*     */         {
/* 307 */           processDirList(logDirInfo, false);
/* 308 */           curSuffix = logDirInfo.m_curSuffix;
/*     */         }
/*     */ 
/* 314 */         if (oldSuffix != curSuffix) {
/*     */           continue;
/*     */         }
/* 317 */         ++curSuffix;
/* 318 */         if (curSuffix > 30)
/*     */         {
/* 320 */           curSuffix = 1;
/*     */         }
/*     */ 
/* 325 */         replaceFile = true;
/*     */       }
/*     */ 
/* 330 */       if (arrayWriter != null)
/*     */       {
/* 332 */         arrayWriter.releaseBuffers();
/*     */       }
/*     */ 
/* 336 */       if (replaceFile)
/*     */       {
/* 338 */         logDirInfo.m_curSuffix = curSuffix;
/*     */ 
/* 341 */         formatLinkPage(logDirInfo);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 347 */       errData = e;
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 351 */       errData = e;
/*     */     }
/*     */     catch (ParseSyntaxException ignore)
/*     */     {
/* 355 */       errData = e;
/*     */     }
/*     */     finally
/*     */     {
/* 359 */       if (accessFile != null)
/*     */       {
/*     */         try
/*     */         {
/* 363 */           accessFile.close();
/* 364 */           accessFile = null;
/*     */         }
/*     */         catch (Exception ignore)
/*     */         {
/* 368 */           ignore.printStackTrace();
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 374 */     FileUtils.releaseDirectory(dir);
/*     */ 
/* 376 */     if (errData == null)
/*     */       return;
/* 378 */     String errMsg = null;
/*     */ 
/* 380 */     if (file != null)
/*     */     {
/* 382 */       errMsg = LocaleUtils.encodeMessage("csLogCouldNotLogFile", errData.getMessage(), file.getPath(), desc, msg);
/*     */     }
/*     */     else
/*     */     {
/* 387 */       errMsg = LocaleUtils.encodeMessage("csLogCouldNotLog", errData.getMessage(), desc, msg);
/*     */     }
/*     */ 
/* 390 */     Report.trace(null, LocaleResources.localizeMessage(errMsg, null), errData);
/*     */   }
/*     */ 
/*     */   protected void formatLinkPage(LogDirInfo logDirInfo)
/*     */   {
/* 401 */     logDirInfo.m_linkPage = true;
/*     */ 
/* 403 */     processDirList(logDirInfo, true);
/*     */   }
/*     */ 
/*     */   protected void processDirList(LogDirInfo logDirInfo, boolean createLinkFile)
/*     */   {
/* 415 */     String dir = logDirInfo.m_dir;
/*     */     try
/*     */     {
/* 419 */       String[] fileList = FileUtils.getMatchingFileNames(dir, logDirInfo.m_prefix + "??.htm");
/*     */ 
/* 421 */       if (fileList.length == 0)
/*     */       {
/* 423 */         if (createLinkFile)
/*     */         {
/* 425 */           FileUtils.releaseDirectory(dir);
/*     */ 
/* 429 */           Log.addMessage(0, "!csLogOrganizationString", logDirInfo, null);
/*     */ 
/* 431 */           FileUtils.reserveDirectory(dir);
/*     */ 
/* 433 */           fileList = FileUtils.getMatchingFileNames(dir, logDirInfo.m_prefix + "??.htm");
/*     */         }
/*     */         else
/*     */         {
/* 437 */           logDirInfo.m_curSuffix = 1;
/* 438 */           return;
/*     */         }
/*     */       }
/*     */ 
/* 442 */       int len = fileList.length;
/* 443 */       LogFileInfo[] fileInfo = new LogFileInfo[len];
/* 444 */       int numLogFiles = 0;
/*     */ 
/* 446 */       int firstDigitIndex = logDirInfo.m_prefix.length();
/* 447 */       int secDigitIndex = firstDigitIndex + 1;
/* 448 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 450 */         String temp = fileList[i];
/* 451 */         char firstChar = temp.charAt(firstDigitIndex);
/* 452 */         char secChar = temp.charAt(secDigitIndex);
/* 453 */         if (!Character.isDigit(firstChar)) continue; if (!Character.isDigit(secChar)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 457 */         int logNum = (firstChar - '0') * 10 + (secChar - '0');
/* 458 */         File curFile = new File(dir, temp);
/* 459 */         if (logNum > 30)
/*     */         {
/* 461 */           curFile.delete();
/*     */         }
/*     */         else
/*     */         {
/* 465 */           LogFileInfo info = new LogFileInfo();
/* 466 */           info.m_name = temp;
/* 467 */           info.m_logNum = logNum;
/*     */ 
/* 469 */           RandomAccessFile accessFile = null;
/*     */           try
/*     */           {
/* 472 */             accessFile = findDate(curFile, info);
/*     */           }
/*     */           catch (Throwable t)
/*     */           {
/* 476 */             String tmpMsg = LocaleUtils.encodeMessage("csLogUnableToCreateTimestamp", t.getMessage(), curFile.getPath());
/*     */ 
/* 478 */             Report.trace(null, LocaleResources.localizeMessage(tmpMsg, null), t);
/*     */           }
/*     */ 
/* 481 */           if (accessFile == null)
/*     */             continue;
/* 483 */           fileInfo[numLogFiles] = info;
/* 484 */           ++numLogFiles;
/* 485 */           accessFile.close();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 490 */       Sort.sort(fileInfo, 0, numLogFiles - 1, new Log());
/*     */ 
/* 497 */       if (createLinkFile)
/*     */       {
/* 499 */         String outFile = logDirInfo.m_indexPage;
/* 500 */         IdcCharArrayWriter dataOut = new IdcCharArrayWriter(2000);
/*     */ 
/* 504 */         String linkHeader = generateLinkPageHeader(logDirInfo);
/*     */ 
/* 506 */         dataOut.write(linkHeader);
/*     */ 
/* 510 */         for (int i = 0; i < numLogFiles; ++i)
/*     */         {
/* 512 */           LogFileInfo info = fileInfo[i];
/* 513 */           String linkItem = generateLinkPageEntry(info);
/* 514 */           dataOut.write(linkItem);
/*     */         }
/*     */ 
/* 518 */         initFooter();
/* 519 */         dataOut.write(m_logFooter);
/* 520 */         dataOut.flush();
/* 521 */         String outStr = dataOut.toString();
/* 522 */         dataOut.releaseBuffers();
/* 523 */         byte[] buf = outStr.getBytes(FileUtils.m_javaSystemEncoding);
/* 524 */         String path = FileUtils.getAbsolutePath(dir, outFile);
/* 525 */         OutputStream out = new FileOutputStream(path);
/*     */         try
/*     */         {
/* 528 */           out.write(buf);
/*     */         }
/*     */         finally
/*     */         {
/* 532 */           FileUtils.closeObject(out);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 537 */       if (numLogFiles > 0)
/*     */       {
/* 540 */         LogFileInfo info = fileInfo[0];
/* 541 */         logDirInfo.m_curSuffix = info.m_logNum;
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 546 */       e.printStackTrace();
/* 547 */       SystemUtils.trace(null, LocaleResources.getString("csCouldNotCreateLinkPage", null));
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 551 */       e.printStackTrace();
/* 552 */       Report.trace(null, LocaleResources.getString("csCouldNotCreateLinkPage", null), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String generateLogHeader(Calendar curTime, LogDirInfo logDirInfo)
/*     */   {
/* 563 */     String dtStr = LocaleResources.getString("csDateMessage", null, curTime);
/* 564 */     IdcStringBuilder header = new IdcStringBuilder();
/* 565 */     header.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n").append("<html lang=\"en\"><head><!--date=").append("" + curTime.getTime().getTime()).append("-->\n").append(getMetaTag()).append("\n<title>").append(dtStr).append("</title>\n").append("<script type=\"text/javascript\">").append("var isUserPage = false;").append("var globalShow = 0;\n").append("function show(id) {").append("\tif (id == 0) {").append("\t\tglobalShow = !globalShow;").append("\t\tvar elems = document.getElementsByTagName('DIV');").append("\t\tfor(var i=0; i<elems.length; i++) {").append("\t\t    if (elems[i].className == 'details') {").append("\t\t\t    doShow(elems[i], globalShow);").append("\t        }").append("\t    }").append("\t} else {").append("\t\tvar elem = document.getElementById(id);").append("\t\tdoShow(elem);").append("\t}").append("}\nfunction doShow(elem, state) {").append("\tif (state == 1 || (state==null && elem.style.display=='none'))").append("\t\telem.style.display='block';").append("\telse if (state == 0 || (state==null && elem.style.display=='block'))").append("\t\telem.style.display='none';").append("}\n</script>").append("<style type=\"text/css\">\n").append("<!--\n").append("a:link, a:visited { color:#993333; }\n").append(".heading { color:#000080; font-weight:bold; }").append("#log_table tr th { font-weight:bold; }").append("-->\n").append("</style>").append("</head>\n<body>\n").append("<noscript>\n<h1>\n").append(LocaleResources.getString("wwJsDisabledWarning", null)).append("</h1>\n</noscript>\n").append("<div id=\"loading_div\" style=\"position: fixed; top: 5; right: 50;\">\n").append("Loading...\n").append("</div>\n").append("<table id=\"log_table\" border=\"1\" width=\"100%\" summary=\"Table containing log data.\">\n").append("<caption align=top><span class=\"heading\">").append(logDirInfo.m_header).append(LocaleResources.getString("csLogFileHeaderString", null)).append(LocaleResources.getString("csLogFileCreatedHeader", null)).append(dtStr).append("\n<div id=\"recent_entry_div\"></div>\n").append("<br></span></caption><tr><th scope=\"col\">").append(LocaleResources.getString("csLogFileTypeString", null)).append("</th><th scope=\"col\">").append(LocaleResources.getString("csLogFileTimeString", null)).append("</th><th scope=\"col\">").append(LocaleResources.getString("csLogFileDescString", null)).append(" [&nbsp;<a href=\"javascript:show(0)\">").append(LocaleResources.getString("wwDetails", null)).append("</a>&nbsp;]</th></tr>\n");
/*     */ 
/* 629 */     return header.toString();
/*     */   }
/*     */ 
/*     */   public String generateLogEntry(Calendar curTime, String msgType, String origMsg, Throwable t)
/*     */   {
/* 636 */     String msg = origMsg;
/* 637 */     IdcMessage exceptionMessage = null;
/* 638 */     if (t != null)
/*     */     {
/* 640 */       exceptionMessage = IdcMessageFactory.lc(t);
/*     */     }
/* 642 */     if ((msg == null) && 
/* 644 */       (exceptionMessage == null))
/*     */     {
/* 646 */       msg = LocaleUtils.encodeMessage("syNullPointerException", null);
/*     */     }
/*     */     IdcMessage idcmsg;
/*     */     IdcMessage idcmsg;
/* 650 */     if (msg == null)
/*     */     {
/* 652 */       idcmsg = exceptionMessage;
/*     */     }
/*     */     else
/*     */     {
/* 656 */       idcmsg = IdcMessageFactory.lc();
/* 657 */       idcmsg.m_msgEncoded = msg;
/* 658 */       idcmsg.m_prior = exceptionMessage;
/*     */     }
/* 660 */     String localizedMsg = null;
/*     */     try
/*     */     {
/* 663 */       localizedMsg = LocaleResources.localizeMessage(null, idcmsg, null).toString();
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/* 667 */       Report.trace(null, "Error localizing error message: ", ignore);
/* 668 */       ignore.printStackTrace();
/* 669 */       localizedMsg = LocaleUtils.encodeMessage(idcmsg);
/*     */     }
/*     */ 
/* 672 */     msg = StringUtils.createErrorStringForBrowser(LocaleUtils.encodeMessage(idcmsg));
/* 673 */     localizedMsg = StringUtils.createErrorStringForBrowser(localizedMsg);
/*     */ 
/* 675 */     if (t != null)
/*     */     {
/* 677 */       String id = "" + Math.random();
/* 678 */       CharArrayWriter arrayWriter = new CharArrayWriter();
/* 679 */       PrintWriter printWriter = new PrintWriter(arrayWriter);
/* 680 */       t.printStackTrace(printWriter);
/* 681 */       String stackMsg = arrayWriter.toString();
/* 682 */       stackMsg = escapeClosureOfCodeConstructs(stackMsg);
/*     */ 
/* 684 */       IdcStringBuilder traceHtml = new IdcStringBuilder();
/* 685 */       traceHtml.append(" [ <a href=\"javascript:").append("if(typeof show!='undefined')show('" + id + "')\">").append(LocaleResources.getString("wwDetails", null)).append("</a> ]\n <div id=\"" + id + "\" style=\"display:none;\" class=\"details\"><pre><code>");
/*     */ 
/* 690 */       IdcStringBuilder fullMsg = new IdcStringBuilder();
/* 691 */       fullMsg.append(LocaleResources.getString("wwScsLogDetails_" + msgType, null));
/* 692 */       fullMsg.append("\n\n");
/* 693 */       fullMsg.append(msg);
/* 694 */       fullMsg.append("\n");
/* 695 */       fullMsg.append(stackMsg);
/* 696 */       String encodedMessage = StringUtils.encodeXmlEscapeSequence(fullMsg.toString());
/*     */ 
/* 698 */       traceHtml.append(encodedMessage);
/* 699 */       traceHtml.append("</code></pre></div>");
/*     */ 
/* 701 */       localizedMsg = localizedMsg + traceHtml.toString();
/*     */     }
/*     */ 
/* 704 */     String dtStr = LocaleResources.getString("csDateMessage", null, curTime);
/* 705 */     String htmlStr = "<!-- IDCLOG: " + msgType + ": (" + dtStr + ") " + msg + " -->\n";
/* 706 */     htmlStr = htmlStr + "<tr><td>" + LocaleResources.getString(new StringBuilder().append("csLogDesc").append(msgType).toString(), null) + "</td><td>" + dtStr + "</td><td>" + localizedMsg + "</td></tr>\n";
/*     */ 
/* 709 */     return htmlStr;
/*     */   }
/*     */ 
/*     */   public String escapeClosureOfCodeConstructs(String msg)
/*     */   {
/* 714 */     IdcStringBuilder msgBuf = new IdcStringBuilder(msg.length() + 32);
/*     */ 
/* 716 */     for (int i = 0; i < msg.length(); ++i)
/*     */     {
/* 718 */       char ch = msg.charAt(i);
/* 719 */       if ((ch == '<') || (ch == '>'))
/*     */       {
/* 721 */         String str = (ch == '<') ? "#60;" : "#62;";
/*     */ 
/* 723 */         msgBuf.append('&');
/* 724 */         msgBuf.append(str);
/*     */       }
/*     */       else
/*     */       {
/* 728 */         msgBuf.append(ch);
/*     */       }
/*     */     }
/* 731 */     return msgBuf.toString();
/*     */   }
/*     */ 
/*     */   public String generateLinkPageHeader(LogDirInfo logDirInfo)
/*     */   {
/* 736 */     IdcStringBuilder msgBuf = new IdcStringBuilder();
/* 737 */     msgBuf.append("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n");
/* 738 */     msgBuf.append("<html lang=\"en\">\n");
/* 739 */     msgBuf.append("<head>\n");
/* 740 */     msgBuf.append(getMetaTag()).append('\n');
/* 741 */     msgBuf.append("<title>");
/* 742 */     msgBuf.append(LocaleResources.getString("csLogHeaderDesc", null, logDirInfo.m_header));
/* 743 */     msgBuf.append("</title>\n");
/* 744 */     msgBuf.append("<script type=\"text/javascript\">\n");
/* 745 */     msgBuf.append("var isUserPage = false;\n");
/* 746 */     msgBuf.append("</script>\n");
/* 747 */     msgBuf.append("</head>\n");
/* 748 */     msgBuf.append("<body>\n");
/* 749 */     msgBuf.append("<noscript>\n<h1>\n");
/* 750 */     msgBuf.append(LocaleResources.getString("wwJsDisabledWarning", null));
/* 751 */     msgBuf.append("</h1>\n</noscript>\n");
/* 752 */     msgBuf.append("<table width=\"100%\"><tr><td><h1 align=center><strong>");
/* 753 */     msgBuf.append(LocaleResources.getString("csLogHeaderDesc", null, logDirInfo.m_header));
/* 754 */     msgBuf.append("</strong></h1></td></tr>\n");
/* 755 */     return msgBuf.toString();
/*     */   }
/*     */ 
/*     */   public String generateLinkPageEntry(LogFileInfo info)
/*     */   {
/* 760 */     String dateStr = LocaleResources.localizeDate(info.m_timeStamp.getTime(), null);
/* 761 */     String linkItem = "<tr><td><a href=\"" + info.m_name + "\">" + LocaleResources.getString("csLogDate", null, dateStr) + "</a></td></tr>\n";
/*     */ 
/* 763 */     return linkItem;
/*     */   }
/*     */ 
/*     */   public LogFileInfo getLogFileInfo(LogDirInfo logDirInfo, String logNumber)
/*     */   {
/* 768 */     LogFileInfo fileInfo = new LogFileInfo();
/*     */ 
/* 770 */     fileInfo.m_name = (logDirInfo.m_prefix + logNumber + ".htm");
/* 771 */     fileInfo.m_logNum = logDirInfo.m_curSuffix;
/*     */ 
/* 773 */     return fileInfo;
/*     */   }
/*     */ 
/*     */   public String getMetaTag()
/*     */   {
/* 778 */     if (FileUtils.m_isoSystemEncoding == null)
/*     */     {
/* 780 */       return "";
/*     */     }
/* 782 */     String metaTag = LocaleResources.getString("csHtmlMetaTag", null, FileUtils.m_isoSystemEncoding);
/*     */ 
/* 784 */     return metaTag;
/*     */   }
/*     */ 
/*     */   public static RandomAccessFile findDate(File file, LogFileInfo info)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 797 */     RandomAccessFile fileAccess = null;
/* 798 */     boolean isError = false;
/* 799 */     boolean foundIt = false;
/*     */     try
/*     */     {
/* 802 */       fileAccess = new RandomAccessFile(file, "rw");
/* 803 */       char[] buf = new char[1000];
/* 804 */       int amt = readAsCharacters(fileAccess, buf);
/* 805 */       if (amt <= 0)
/*     */       {
/* 807 */         Object localObject1 = null;
/*     */         return localObject1;
/*     */       }
/* 810 */       Date dte = null;
/* 811 */       String str = new String(buf, 0, amt);
/* 812 */       String dateMarkStr = "<!--date=";
/* 813 */       char endDateMark = '-';
/* 814 */       long ts = 0L;
/*     */ 
/* 816 */       int dteLength = dateMarkStr.length();
/* 817 */       int index = str.indexOf(dateMarkStr);
/* 818 */       if (index >= 0)
/*     */       {
/* 820 */         int endIndex = str.indexOf(endDateMark, index + dteLength);
/* 821 */         if (endIndex >= 0)
/*     */         {
/* 823 */           String tsString = str.substring(index + dteLength, endIndex);
/* 824 */           ts = Long.parseLong(tsString);
/* 825 */           dte = new Date(ts);
/*     */ 
/* 828 */           if ((amt > endIndex + 2) && 
/* 830 */             (str.charAt(endIndex + 2) == '!'))
/*     */           {
/* 833 */             fileAccess.seek(endIndex + 2);
/* 834 */             fileAccess.writeByte(45);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 840 */       if (ts == 0L)
/*     */       {
/* 843 */         dateMarkStr = "<title>";
/* 844 */         endDateMark = '<';
/*     */ 
/* 846 */         index = str.indexOf(dateMarkStr);
/* 847 */         if (index >= 0)
/*     */         {
/* 849 */           dteLength = dateMarkStr.length();
/* 850 */           int endIndex = str.indexOf(endDateMark, index + dteLength);
/* 851 */           if (endIndex < 0)
/*     */           {
/* 853 */             isError = true;
/*     */           }
/* 855 */           if (!isError)
/*     */           {
/* 857 */             String dateStr = str.substring(index + dteLength, endIndex);
/*     */             try
/*     */             {
/* 860 */               dte = LocaleResources.parseDate(dateStr, null);
/*     */             }
/*     */             catch (Throwable t)
/*     */             {
/* 864 */               SimpleDateFormat df = new SimpleDateFormat("M/d/yy h:mm a");
/* 865 */               dte = df.parse(dateStr);
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 871 */           isError = true;
/*     */         }
/*     */       }
/* 874 */       if (!isError)
/*     */       {
/* 876 */         info.m_timeStamp = Calendar.getInstance();
/* 877 */         info.m_timeStamp.setTime(dte);
/* 878 */         foundIt = true;
/*     */       }
/*     */     }
/*     */     catch (ParseException e)
/*     */     {
/* 883 */       e.printStackTrace();
/*     */     }
/*     */     finally
/*     */     {
/* 887 */       if ((fileAccess != null) && (!foundIt))
/*     */       {
/* 889 */         fileAccess.close();
/* 890 */         fileAccess = null;
/*     */       }
/*     */     }
/* 893 */     return fileAccess;
/*     */   }
/*     */ 
/*     */   public static int readAsCharacters(RandomAccessFile accessFile, char[] buf) throws IOException
/*     */   {
/* 898 */     byte[] temp = new byte[buf.length];
/* 899 */     int retVal = accessFile.read(temp);
/* 900 */     for (int i = 0; i < retVal; ++i)
/*     */     {
/* 902 */       buf[i] = (char)temp[i];
/*     */     }
/* 904 */     return retVal;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 909 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLogWriter
 * JD-Core Version:    0.5.4
 */