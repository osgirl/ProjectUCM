/*     */ package intradoc.resource;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.DynamicHtmlUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.GrammarElement;
/*     */ import intradoc.common.HtmlChunk;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseOutput;
/*     */ import intradoc.common.Parser;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.MutableResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import java.io.CharArrayReader;
/*     */ import java.io.CharArrayWriter;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataTransformationUtils
/*     */ {
/*     */   public static void mergeInDynamicData(Workspace ws, DataBinder binder, DynamicHtml dynamicHtml)
/*     */     throws DataException, ServiceException
/*     */   {
/*  38 */     List dataChunks = dynamicHtml.m_data;
/*  39 */     int size = dataChunks.size();
/*  40 */     if (size == 0)
/*     */     {
/*  43 */       return;
/*     */     }
/*     */ 
/*  47 */     String dID = binder.getLocal("dID");
/*  48 */     String docName = binder.getLocal("dDocName");
/*  49 */     if (dID == null)
/*     */     {
/*  52 */       docName = binder.getFromSets("dDocName");
/*  53 */       String revLabel = binder.getAllowMissing("dRevLabel");
/*     */ 
/*  55 */       String query = null;
/*  56 */       if (revLabel != null)
/*     */       {
/*  58 */         query = "QdocRev";
/*     */       }
/*     */       else
/*     */       {
/*  62 */         query = "QlatestReleasedIDByName";
/*     */       }
/*  64 */       binder.putLocal("docmetaColumns", "DocMeta.dID");
/*  65 */       ResultSet revSet = ws.createResultSet(query, binder);
/*  66 */       binder.removeLocal("docmetaColumns");
/*  67 */       if (revSet.isEmpty())
/*     */       {
/*  69 */         throw new ServiceException(LocaleUtils.encodeMessage("csDataTransNoDynPage", null, docName));
/*     */       }
/*     */ 
/*  73 */       dID = ResultSetUtils.getValue(revSet, "dID");
/*     */     }
/*  75 */     else if (docName == null)
/*     */     {
/*  77 */       docName = binder.getFromSets("dDocName");
/*     */     }
/*  79 */     binder.putLocal("SourceID", dID);
/*  80 */     binder.putLocal("SourceName", docName);
/*     */ 
/*  86 */     boolean isMergeData = StringUtils.convertToBool(binder.getLocal("IsMergeData"), true);
/*  87 */     if (!isMergeData)
/*     */       return;
/*  89 */     mergeInData(binder, dynamicHtml);
/*     */   }
/*     */ 
/*     */   protected static void mergeInData(DataBinder binder, DynamicHtml dynamicHtml)
/*     */     throws DataException
/*     */   {
/*  96 */     List dataChunks = dynamicHtml.m_data;
/*  97 */     int size = dataChunks.size();
/*     */ 
/*  99 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 101 */       HtmlChunk[] chunks = (HtmlChunk[])(HtmlChunk[])dataChunks.get(i);
/* 102 */       String type = null;
/* 103 */       if (chunks[0].m_grammarElement != null)
/*     */       {
/* 105 */         type = chunks[0].m_grammarElement.m_id;
/*     */       }
/* 107 */       if ((type != null) && (!type.equals("xml")))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 114 */         CharArrayReader caReader = new CharArrayReader(chunks[1].m_chars);
/* 115 */         XmlDataMerger dataMerger = new XmlDataMerger();
/* 116 */         dataMerger.parse(caReader, dynamicHtml.m_fileName);
/*     */ 
/* 118 */         dataMerger.mergeInto(binder, null);
/*     */ 
/* 120 */         if (dynamicHtml.m_sourceEncoding != null)
/*     */         {
/* 122 */           dataMerger.setSourceEncoding(dynamicHtml.m_sourceEncoding);
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 127 */         throw new DataException(e, "csDataTransDynHTMLParseError", new Object[0]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DynamicDataMerger parseDynamicData(String fileName)
/*     */     throws ServiceException
/*     */   {
/* 139 */     ParseOutput parseOutput = new ParseOutput();
/* 140 */     CharArrayWriter outbuf = new CharArrayWriter();
/*     */ 
/* 142 */     DynamicDataMerger dataMerger = null;
/* 143 */     parseOutput.m_writer = outbuf;
/*     */ 
/* 145 */     Reader fileReader = null;
/*     */     try
/*     */     {
/* 148 */       File file = FileUtilsCfgBuilder.getCfgFile(fileName, null, false);
/* 149 */       String[] enc = new String[1];
/* 150 */       fileReader = ResourceLoader.openResourceReader(file, enc, ResourceLoader.F_IS_HTML);
/*     */ 
/* 152 */       String str = "!--$idcbegindata";
/* 153 */       char[] tag = str.toCharArray();
/*     */ 
/* 155 */       boolean isMatched = Parser.findHtmlTagPrefix(fileReader, parseOutput, tag);
/* 156 */       parseOutput.clearPending();
/* 157 */       outbuf.reset();
/* 158 */       if (isMatched)
/*     */       {
/* 161 */         String beginTag = parseOutput.waitingBufferAsString();
/*     */ 
/* 163 */         String str1 = "!--$idcenddata";
/* 164 */         tag = str1.toCharArray();
/* 165 */         isMatched = Parser.findHtmlTagPrefix(fileReader, parseOutput, tag);
/* 166 */         if (isMatched)
/*     */         {
/* 169 */           parseOutput.writePending();
/*     */ 
/* 172 */           dataMerger = createDataMerger(beginTag, parseOutput, fileName, enc[0]);
/*     */         }
/*     */         else
/*     */         {
/* 177 */           throw new ServiceException("!csDataTransDataNotClosed");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 188 */       parseOutput.releaseBuffers();
/* 189 */       closeStreams(fileReader, null);
/*     */     }
/*     */ 
/* 192 */     return dataMerger;
/*     */   }
/*     */ 
/*     */   protected static DynamicDataMerger createDataMerger(String beginTag, ParseOutput parseOutput, String fileName, String encoding)
/*     */     throws IOException, DataException
/*     */   {
/* 213 */     XmlDataMerger dataMerger = null;
/*     */ 
/* 216 */     String tag = "!--$idcbegindata";
/*     */ 
/* 218 */     String str = beginTag.trim();
/* 219 */     if (str.endsWith("--"))
/*     */     {
/* 221 */       str = str.substring(tag.length(), str.length() - 2);
/*     */     }
/*     */     else
/*     */     {
/* 225 */       str = beginTag.substring(tag.length());
/*     */     }
/*     */ 
/* 228 */     str = str.trim();
/* 229 */     if ((str.length() == 0) || (str.startsWith("xml")))
/*     */     {
/* 231 */       dataMerger = new XmlDataMerger();
/* 232 */       dataMerger.setSourceEncoding(encoding);
/* 233 */       CharArrayReader reader = new CharArrayReader(((CharArrayWriter)parseOutput.m_writer).toCharArray());
/* 234 */       dataMerger.parse(reader, fileName);
/*     */     }
/*     */ 
/* 240 */     return dataMerger;
/*     */   }
/*     */ 
/*     */   public static DataResultSet applyIncludes(DataResultSet drset, DataBinder binder, DynamicHtmlMerger merger, String filterInclude, String[] includeColumns, String tableSet, String idocSet)
/*     */     throws IOException, DataException
/*     */   {
/* 272 */     String activeKey = "ddActiveSet";
/* 273 */     String curActiveResultName = binder.getLocal(activeKey);
/* 274 */     String newActiveResultName = "ActiveResultSet:" + filterInclude;
/*     */ 
/* 276 */     binder.putLocal(activeKey, newActiveResultName);
/* 277 */     FieldInfo[] includeColumnFields = null;
/* 278 */     IdcCharArrayWriter writer = merger.getTemporaryWriter();
/* 279 */     boolean hasPushedStack = false;
/* 280 */     boolean isActiveRs = false;
/*     */     DataResultSet retSet;
/*     */     try
/*     */     {
/* 284 */       boolean hasIncludeColumn = false;
/* 285 */       if (includeColumns != null)
/*     */       {
/* 287 */         includeColumnFields = ResultSetUtils.createInfoList(drset, includeColumns, false);
/* 288 */         hasIncludeColumn = true;
/*     */       }
/* 290 */       boolean hasFilterInclude = (filterInclude != null) && (filterInclude.length() > 0);
/* 291 */       if (hasFilterInclude)
/*     */       {
/* 294 */         DataResultSet retSet = new DataResultSet();
/* 295 */         retSet.mergeFields(drset);
/*     */       }
/*     */       else
/*     */       {
/* 299 */         retSet = drset;
/*     */       }
/*     */ 
/* 302 */       if ((hasFilterInclude) || (hasIncludeColumn))
/*     */       {
/* 304 */         binder.addResultSetDirect(newActiveResultName, drset);
/* 305 */         while (binder.nextRow(newActiveResultName))
/*     */         {
/* 307 */           isActiveRs = true;
/*     */ 
/* 310 */           if (hasIncludeColumn)
/*     */           {
/* 312 */             for (int i = 0; i < includeColumnFields.length; ++i)
/*     */             {
/* 314 */               int index = includeColumnFields[i].m_index;
/* 315 */               String includeNameVal = null;
/* 316 */               if (index >= 0)
/*     */               {
/* 318 */                 includeNameVal = drset.getStringValue(index);
/*     */               }
/* 320 */               if ((includeNameVal != null) && (includeNameVal.length() > 0))
/*     */               {
/* 322 */                 if (merger.m_isReportErrorStack)
/*     */                 {
/* 324 */                   DynamicHtmlUtils.pushStackMessage("csDynHtmlEvalColumnInclude", new String[] { tableSet, idocSet, includeColumnFields[i].m_name }, merger);
/*     */ 
/* 327 */                   hasPushedStack = true;
/*     */                 }
/* 329 */                 writer.reset();
/* 330 */                 merger.evaluateResourceIncludeToWriter(includeNameVal, writer);
/* 331 */                 String newVal = writer.toString();
/* 332 */                 drset.setCurrentValue(index, newVal);
/*     */               }
/*     */ 
/* 335 */               if (!hasPushedStack)
/*     */                 continue;
/* 337 */               merger.popStack();
/* 338 */               hasPushedStack = false;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 344 */           if (!hasFilterInclude)
/*     */             continue;
/* 346 */           if (merger.m_isReportErrorStack)
/*     */           {
/* 348 */             DynamicHtmlUtils.pushStackMessage("csDynHTMLFilterTableByInclude", new String[] { tableSet, idocSet }, merger);
/*     */ 
/* 351 */             hasPushedStack = true;
/*     */           }
/*     */ 
/* 354 */           boolean skipRow = false;
/* 355 */           writer.reset();
/* 356 */           binder.putLocal("ddSkipRow", "");
/* 357 */           merger.evaluateResourceIncludeToWriter(filterInclude, writer);
/* 358 */           String val = binder.getLocal("ddSkipRow");
/* 359 */           skipRow = StringUtils.convertToBool(val, false);
/*     */ 
/* 361 */           if (!skipRow)
/*     */           {
/* 363 */             Vector row = drset.getCurrentRowValues();
/* 364 */             retSet.addRow(row);
/*     */           }
/*     */ 
/* 368 */           if (hasPushedStack)
/*     */           {
/* 370 */             merger.popStack();
/* 371 */             hasPushedStack = false;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 376 */         isActiveRs = false;
/*     */       }
/*     */ 
/* 380 */       retSet.first();
/*     */     }
/*     */     finally
/*     */     {
/* 385 */       if (hasPushedStack)
/*     */       {
/* 387 */         merger.popStack();
/* 388 */         hasPushedStack = false;
/*     */       }
/* 390 */       if (isActiveRs)
/*     */       {
/* 392 */         binder.popActiveResultSet();
/*     */       }
/*     */ 
/* 395 */       binder.removeResultSet(newActiveResultName);
/* 396 */       if (curActiveResultName == null)
/*     */       {
/* 398 */         binder.removeLocal(activeKey);
/*     */       }
/*     */       else
/*     */       {
/* 402 */         binder.putLocal(activeKey, curActiveResultName);
/*     */       }
/* 404 */       merger.releaseTemporaryWriter(writer);
/*     */     }
/*     */ 
/* 407 */     return retSet;
/*     */   }
/*     */ 
/*     */   public static boolean evaluateMakeActiveInclude(String rsName, DataBinder binder, DynamicHtmlMerger merger, Writer writer, String include, boolean singleRow)
/*     */     throws IOException
/*     */   {
/* 427 */     boolean isActiveRs = false;
/* 428 */     MutableResultSet mrSet = null;
/* 429 */     int curRow = -1;
/* 430 */     if (singleRow)
/*     */     {
/* 433 */       if (binder.isActiveSet(rsName))
/*     */       {
/* 435 */         merger.evaluateResourceIncludeToWriter(include, writer);
/* 436 */         return true;
/*     */       }
/*     */ 
/* 439 */       mrSet = (MutableResultSet)binder.getResultSet(rsName);
/* 440 */       if ((mrSet != null) && (mrSet.isRowPresent()))
/*     */       {
/* 442 */         curRow = mrSet.getCurrentRow();
/*     */       }
/* 444 */       if (curRow < 0)
/*     */       {
/* 446 */         return false;
/*     */       }
/*     */     }
/* 449 */     boolean didInclude = false;
/*     */     try {
/*     */       do {
/* 452 */         if (!binder.nextRow(rsName))
/*     */           break label123;
/* 454 */         isActiveRs = true;
/* 455 */         if (singleRow)
/*     */         {
/* 457 */           mrSet.setCurrentRow(curRow);
/*     */         }
/* 459 */         merger.evaluateResourceIncludeToWriter(include, writer);
/* 460 */         didInclude = true;
/* 461 */       }while (!singleRow);
/*     */ 
/* 463 */       binder.popActiveResultSet();
/*     */ 
/* 467 */       label123: isActiveRs = false;
/*     */     }
/*     */     finally
/*     */     {
/* 471 */       if (isActiveRs)
/*     */       {
/* 473 */         binder.popActiveResultSet();
/*     */       }
/*     */     }
/* 476 */     return didInclude;
/*     */   }
/*     */ 
/*     */   public static void evaluteIncludeProtectValues(String incName, String[] protectKeys, DataBinder binder, DynamicHtmlMerger merger, Writer writer)
/*     */     throws IOException
/*     */   {
/* 495 */     Object[] protectedObjects = null;
/* 496 */     if (protectKeys != null)
/*     */     {
/* 498 */       protectedObjects = new Object[protectKeys.length];
/* 499 */       for (int i = 0; i < protectKeys.length; ++i)
/*     */       {
/* 501 */         String key = protectKeys[i];
/* 502 */         if ((key == null) || (key.length() <= 0))
/*     */           continue;
/* 504 */         char ch = key.charAt(0);
/* 505 */         if (ch == '$')
/*     */         {
/* 507 */           String rsKey = key.substring(1);
/* 508 */           protectedObjects[i] = binder.getResultSet(rsKey);
/*     */         }
/*     */         else
/*     */         {
/* 512 */           protectedObjects[i] = binder.getLocal(key);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 520 */       merger.evaluateResourceIncludeToWriter(incName, writer);
/*     */     }
/*     */     finally
/*     */     {
/*     */       int i;
/*     */       String key;
/*     */       char ch;
/*     */       Object o;
/*     */       String rsKey;
/*     */       ResultSet rset;
/* 524 */       if (protectKeys != null)
/*     */       {
/* 526 */         for (int i = 0; i < protectKeys.length; ++i)
/*     */         {
/* 528 */           String key = protectKeys[i];
/* 529 */           if ((key == null) || (key.length() <= 0))
/*     */             continue;
/* 531 */           char ch = key.charAt(0);
/* 532 */           Object o = protectedObjects[i];
/* 533 */           if (ch == '$')
/*     */           {
/* 535 */             String rsKey = key.substring(1);
/* 536 */             if (o == null)
/*     */             {
/* 538 */               binder.removeResultSet(rsKey);
/*     */             }
/*     */             else
/*     */             {
/* 542 */               ResultSet rset = (ResultSet)o;
/* 543 */               binder.addResultSetDirect(rsKey, rset);
/*     */             }
/*     */ 
/*     */           }
/* 548 */           else if (o == null)
/*     */           {
/* 551 */             binder.putLocal(key, "");
/*     */           }
/*     */           else
/*     */           {
/* 555 */             binder.putLocal(key, (String)o);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String createMergedFile(String fileName, DynamicDataMerger dataMerger)
/*     */     throws ServiceException
/*     */   {
/* 570 */     ParseOutput parseOutput = new ParseOutput();
/*     */ 
/* 572 */     String tmpFile = DataBinder.getTemporaryDirectory() + DataBinder.getNextFileCounter() + ".hcsp";
/* 573 */     Reader fileReader = null;
/*     */     try
/*     */     {
/* 576 */       String srcEncoding = dataMerger.getSourceEncoding();
/* 577 */       if (srcEncoding == null)
/*     */       {
/* 579 */         srcEncoding = DataSerializeUtils.getSystemEncoding();
/*     */       }
/* 581 */       File f = new File(fileName);
/* 582 */       fileReader = FileUtils.openDataReader(f, srcEncoding);
/* 583 */       File tmpF = new File(tmpFile);
/* 584 */       parseOutput.m_writer = FileUtils.openDataWriter(tmpF, srcEncoding);
/*     */ 
/* 586 */       String prefix = "!--$idcbegindata";
/* 587 */       char[] tag = prefix.toCharArray();
/*     */ 
/* 589 */       boolean isMatched = Parser.findHtmlTagPrefix(fileReader, parseOutput, tag);
/* 590 */       parseOutput.writePending();
/* 591 */       if (isMatched)
/*     */       {
/* 594 */         String beginTag = parseOutput.waitingBufferAsString();
/*     */ 
/* 596 */         String str1 = "!--$idcenddata";
/* 597 */         tag = str1.toCharArray();
/*     */ 
/* 601 */         parseOutput.m_stopWriting = true;
/* 602 */         isMatched = Parser.findHtmlTagPrefix(fileReader, parseOutput, tag);
/* 603 */         parseOutput.m_stopWriting = false;
/* 604 */         if (isMatched)
/*     */         {
/* 607 */           dataMerger.write(beginTag, parseOutput, "\n<!--$idcenddata-->");
/*     */         }
/*     */         else
/*     */         {
/* 612 */           throw new ServiceException("!csDataTransDataNotClosed");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 617 */       readToEnd(fileReader, parseOutput);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 626 */       closeStreams(fileReader, parseOutput.m_writer);
/* 627 */       parseOutput.releaseBuffers();
/*     */     }
/*     */ 
/* 630 */     return tmpFile;
/*     */   }
/*     */ 
/*     */   protected static void readToEnd(Reader reader, ParseOutput parseOutput)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 638 */       parseOutput.copyToPending(false, false);
/*     */ 
/* 640 */       parseOutput.clearPending();
/*     */ 
/* 642 */       char[] temp = parseOutput.m_outputBuf;
/*     */       while (true)
/*     */       {
/* 645 */         int offset = parseOutput.m_readOffset;
/* 646 */         int numRead = parseOutput.m_numRead;
/* 647 */         if ((numRead < temp.length / 2) && (reader.ready()))
/*     */         {
/* 649 */           int nchars = reader.read(temp, numRead + offset, temp.length - numRead - offset);
/*     */ 
/* 651 */           if (nchars > 0)
/*     */           {
/* 653 */             numRead += nchars;
/* 654 */             parseOutput.m_numRead = numRead;
/*     */           }
/*     */         }
/*     */ 
/* 658 */         if (numRead <= 0)
/*     */         {
/* 660 */           parseOutput.m_isEOF = true;
/* 661 */           break;
/*     */         }
/* 663 */         parseOutput.m_numWaiting = numRead;
/* 664 */         parseOutput.copyToPending(true, false);
/* 665 */         parseOutput.writePending();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 670 */       throw new ServiceException("", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void closeStreams(Reader reader, Writer writer)
/*     */   {
/*     */     try
/*     */     {
/* 678 */       if (reader != null)
/*     */       {
/* 680 */         reader.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 685 */       if (SystemUtils.m_verbose)
/*     */       {
/* 687 */         Report.debug("system", null, ignore);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 693 */       if (writer != null)
/*     */       {
/* 695 */         writer.close();
/*     */       }
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 700 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 702 */       Report.debug("system", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 710 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.DataTransformationUtils
 * JD-Core Version:    0.5.4
 */