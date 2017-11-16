/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DynamicData
/*     */   implements DynamicDataHandleAttribute
/*     */ {
/*     */   public static final int NO_TABLE = 0;
/*     */   public static final int HAS_TABLE = 1;
/*     */   public static final int IS_COLUMN = 2;
/*     */   public static final int IS_INDEXED_COLUMN = 3;
/*  38 */   public static String[] COLUMN_PROPERTY_DESCRIPTORS = { "noTable", "hasTable", "isColumn", "isIndexedColumn" };
/*     */ 
/*  49 */   protected static DynamicDataParser[] m_parsers = null;
/*     */   public String m_format;
/*     */   public String m_dataText;
/*     */   public boolean m_trimValues;
/*     */   public String m_mergeKey;
/*     */   public String m_mergeNewKey;
/*     */   public String m_mergeRule;
/*     */   public SortOptions m_sortOptions;
/*     */   public boolean m_isWildcardMerge;
/*     */   public boolean m_mergeBlanks;
/*     */   public List<String> m_mergeAppendColumns;
/*     */   public boolean m_usingDefaultMergeAppendFormat;
/*     */   public char m_mergeAppendSep;
/*     */   public char m_mergeAppendEq;
/*     */   public Set<String> m_collapseMultiValueColumns;
/*     */   public List<String> m_mergeOtherData;
/*     */   public Table m_table;
/*     */   public boolean m_hasMergedTable;
/*     */   public boolean m_isLoadingAssociatedData;
/*     */   public Table m_mergedTable;
/*     */   public List m_indexedColumns;
/*     */   public Map[] m_indexedLookups;
/*     */   public String m_countColumn;
/*     */   public String m_inclusionFilterName;
/*     */   public List<String> m_includeColumns;
/*     */   public List<String[]> m_defaultValues;
/*     */   public List<DynamicDataDerivedColumn> m_derivedColumns;
/*     */   public DynamicDataGrammar m_grammarData;
/*     */   public boolean m_hasExtraInfo;
/*     */   public Object m_extraInfo;
/*     */   public boolean m_isError;
/*     */   public String m_errorKey;
/*     */ 
/*     */   public DynamicData()
/*     */   {
/*  64 */     this.m_trimValues = true;
/*     */ 
/* 107 */     this.m_usingDefaultMergeAppendFormat = true;
/* 108 */     this.m_mergeAppendSep = ':';
/* 109 */     this.m_mergeAppendEq = '=';
/*     */   }
/*     */ 
/*     */   public static void addParser(DynamicDataParser parser)
/*     */   {
/* 216 */     int nCurParsers = (m_parsers == null) ? 0 : m_parsers.length;
/* 217 */     DynamicDataParser[] newParsers = new DynamicDataParser[nCurParsers + 1];
/* 218 */     for (int i = 0; i < nCurParsers; ++i)
/*     */     {
/* 220 */       newParsers[i] = m_parsers[i];
/*     */     }
/* 222 */     newParsers[nCurParsers] = parser;
/* 223 */     m_parsers = newParsers;
/*     */   }
/*     */ 
/*     */   public boolean parse(Reader resReader, DynamicHtml dynHtml, ParseOutput parseOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 239 */     BufferedReader bufReader = new BufferedReader(resReader);
/* 240 */     peakFormat(bufReader, parseOutput);
/*     */ 
/* 242 */     return parseData(bufReader, dynHtml, parseOutput);
/*     */   }
/*     */ 
/*     */   public void peakFormat(BufferedReader resReader, ParseOutput parseOutput)
/*     */     throws ParseSyntaxException, IOException
/*     */   {
/* 256 */     resReader.mark(500);
/* 257 */     char[] buf = new char[500];
/* 258 */     int nchars = resReader.read(buf);
/* 259 */     int[] resetIndex = { 0 };
/* 260 */     ParseLocationInfo parseInfo = parseOutput.m_parseInfo;
/*     */ 
/* 263 */     DynamicDataUtils.parseSpecialXmlTag(DynamicDataUtils.STANDARD_BEGIN_FORMAT_TAG, DynamicDataUtils.STANDARD_END_FORMAT_TAG, buf, 0, nchars, this, resetIndex, 0);
/*     */ 
/* 266 */     resReader.reset();
/* 267 */     if (resetIndex[0] > 0)
/*     */     {
/* 269 */       int charsRead = resetIndex[0];
/* 270 */       resReader.skip(charsRead);
/* 271 */       advanceParseLocation(parseInfo, buf, 0, charsRead);
/*     */     }
/* 273 */     if (!this.m_isError)
/*     */       return;
/* 275 */     String msg = LocaleUtils.encodeMessage("csDynHTMLDynamicDataBadFormatSpecification", null, this.m_errorKey);
/* 276 */     throw new ParseSyntaxException(parseInfo, msg);
/*     */   }
/*     */ 
/*     */   public void advanceParseLocation(ParseLocationInfo parseInfo, char[] buf, int start, int len)
/*     */   {
/* 289 */     int nlinesRead = 0;
/* 290 */     int charOffset = parseInfo.m_parseCharOffset;
/* 291 */     int end = start + len;
/* 292 */     for (int i = start; i < end; ++i)
/*     */     {
/* 294 */       char ch = buf[i];
/* 295 */       if (ch == '\n')
/*     */       {
/* 297 */         ++nlinesRead;
/* 298 */         charOffset = 0;
/*     */       }
/*     */       else
/*     */       {
/* 302 */         ++charOffset;
/*     */       }
/*     */     }
/* 305 */     parseInfo.m_parseLine += nlinesRead;
/* 306 */     parseInfo.m_parseCharOffset = charOffset;
/*     */   }
/*     */ 
/*     */   public boolean parseData(Reader resReader, DynamicHtml dynHtml, ParseOutput parseOutput)
/*     */     throws IOException, ParseSyntaxException
/*     */   {
/* 322 */     boolean didIt = false;
/* 323 */     if (m_parsers == null)
/*     */     {
/* 325 */       Report.trace("idocscript", "No parser registered for parsing dynamic data", null);
/* 326 */       return didIt;
/*     */     }
/* 328 */     for (int i = 0; i < m_parsers.length; ++i)
/*     */     {
/* 330 */       if (m_parsers[i].parseResource(resReader, dynHtml, this, parseOutput) != 1)
/*     */         continue;
/* 332 */       didIt = true;
/* 333 */       break;
/*     */     }
/*     */ 
/* 336 */     return didIt;
/*     */   }
/*     */ 
/*     */   public void loadRecursiveData(DynamicDataMergeInfo mergeInfo, DynamicHtml dynHtml)
/*     */   {
/* 347 */     mergeInfo.m_dynamicHtmlToMerge = new ArrayList();
/* 348 */     DynamicHtml curDyn = dynHtml;
/* 349 */     while (curDyn != null)
/*     */     {
/* 351 */       mergeInfo.m_dynamicHtmlToMerge.add(curDyn);
/* 352 */       curDyn = curDyn.m_priorScript;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mergeData(DynamicHtml dynHtml, DynamicDataMergeInfo mergeInfo)
/*     */   {
/* 369 */     for (int i = 0; i < m_parsers.length; ++i)
/*     */     {
/* 373 */       m_parsers[i].mergeDataFromPriorScript(dynHtml, this, mergeInfo);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearMergeInfo()
/*     */   {
/* 383 */     this.m_hasExtraInfo = false;
/* 384 */     this.m_extraInfo = null;
/* 385 */     this.m_hasMergedTable = false;
/* 386 */     this.m_mergedTable = null;
/* 387 */     this.m_indexedLookups = null;
/*     */   }
/*     */ 
/*     */   public void copyOverMergedData(DynamicHtml dynHtml, DynamicDataMergeInfo mergeInfo)
/*     */   {
/* 397 */     if (mergeInfo.m_useDynamicData)
/*     */     {
/* 399 */       String[] fields = mergeInfo.m_tempFieldList;
/* 400 */       String[][] rows = mergeInfo.m_tempRows;
/* 401 */       int numRows = (rows != null) ? rows.length : 0;
/* 402 */       ArrayList tableRowList = new ArrayList(numRows);
/* 403 */       for (int i = 0; i < numRows; ++i)
/*     */       {
/* 405 */         String[] row = rows[i];
/* 406 */         String[] changingRow = null;
/* 407 */         boolean copyingRow = false;
/* 408 */         if ((row == null) || (row.length < fields.length))
/*     */         {
/* 410 */           copyingRow = (row != null) && (row.length > 0);
/* 411 */           changingRow = new String[fields.length];
/*     */         }
/*     */         else
/*     */         {
/* 415 */           changingRow = row;
/*     */         }
/* 417 */         for (int j = 0; j < fields.length; ++j)
/*     */         {
/* 419 */           if ((copyingRow) && (j < row.length))
/*     */           {
/* 421 */             changingRow[j] = row[j];
/*     */           }
/* 423 */           if (changingRow[j] != null)
/*     */             continue;
/* 425 */           changingRow[j] = "";
/*     */         }
/*     */ 
/* 428 */         tableRowList.add(changingRow);
/*     */       }
/*     */ 
/* 431 */       this.m_collapseMultiValueColumns = mergeInfo.m_collapseMultiValueColumns;
/* 432 */       this.m_mergedTable = new Table(fields, tableRowList);
/* 433 */       this.m_hasMergedTable = true;
/*     */     }
/* 437 */     else if (mergeInfo.m_activeTable != null)
/*     */     {
/* 439 */       this.m_mergedTable = mergeInfo.m_activeTable;
/* 440 */       this.m_hasMergedTable = true;
/*     */     }
/*     */ 
/* 445 */     this.m_includeColumns = mergeInfo.m_includeColumns;
/*     */   }
/*     */ 
/*     */   public Table getIndexedTable(String colName, String value)
/*     */   {
/* 462 */     if ((this.m_indexedLookups == null) || (this.m_mergedTable == null) || (this.m_mergedTable.m_colNames == null))
/*     */     {
/* 465 */       return null;
/*     */     }
/* 467 */     String[] colNames = this.m_mergedTable.m_colNames;
/* 468 */     int index = StringUtils.findStringIndexEx(colNames, colName, true);
/* 469 */     if (index < 0)
/*     */     {
/* 471 */       return null;
/*     */     }
/* 473 */     Map m = this.m_indexedLookups[index];
/* 474 */     if (m == null)
/*     */     {
/* 476 */       return null;
/*     */     }
/*     */ 
/* 479 */     if (value == null)
/*     */     {
/* 481 */       value = "";
/*     */     }
/* 483 */     String lookupValue = value.toLowerCase();
/* 484 */     return (Table)m.get(lookupValue);
/*     */   }
/*     */ 
/*     */   public int getColumnProperties(String colName)
/*     */   {
/* 496 */     if ((this.m_mergedTable == null) || (this.m_mergedTable.m_colNames == null))
/*     */     {
/* 499 */       return 0;
/*     */     }
/* 501 */     String[] colNames = this.m_mergedTable.m_colNames;
/* 502 */     int index = StringUtils.findStringIndexEx(colNames, colName, true);
/* 503 */     if (index < 0)
/*     */     {
/* 505 */       return 1;
/*     */     }
/* 507 */     if (this.m_indexedLookups == null)
/*     */     {
/* 509 */       return 2;
/*     */     }
/* 511 */     Map m = this.m_indexedLookups[index];
/* 512 */     if (m == null)
/*     */     {
/* 514 */       return 2;
/*     */     }
/* 516 */     return 3;
/*     */   }
/*     */ 
/*     */   public String getColumnPropertyDescriptor(int prop)
/*     */   {
/* 527 */     return COLUMN_PROPERTY_DESCRIPTORS[prop];
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 537 */     return createReportString(true, this.m_sortOptions);
/*     */   }
/*     */ 
/*     */   public String createReportString(boolean verbose, SortOptions sortOptions)
/*     */   {
/* 548 */     IdcStringBuilder strBuf = new IdcStringBuilder(255);
/* 549 */     appendDebugFormat(verbose, sortOptions, strBuf);
/* 550 */     return strBuf.toString();
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 555 */     appendDebugFormat(true, this.m_sortOptions, appendable);
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(boolean verbose, SortOptions sortOptions, IdcAppendable appendable)
/*     */   {
/* 560 */     appendable.append("format=");
/* 561 */     if (this.m_format == null)
/*     */     {
/* 563 */       appendable.append("<not defined>");
/*     */     }
/*     */     else
/*     */     {
/* 567 */       appendable.append(this.m_format);
/*     */     }
/* 569 */     if (verbose)
/*     */     {
/* 571 */       appendable.append(",hasExtraInfo=");
/* 572 */       appendable.append("" + this.m_hasExtraInfo);
/*     */     }
/* 574 */     appendable.append(",hasMergedTable=");
/* 575 */     appendable.append("" + this.m_hasMergedTable);
/* 576 */     if (this.m_mergeKey != null)
/*     */     {
/* 578 */       appendable.append(",mergeKey=");
/* 579 */       appendable.append(this.m_mergeKey);
/*     */     }
/* 581 */     if (this.m_mergeNewKey != null)
/*     */     {
/* 583 */       appendable.append(",mergeNewKey=");
/* 584 */       appendable.append(this.m_mergeNewKey);
/*     */     }
/* 586 */     if (this.m_mergeRule != null)
/*     */     {
/* 588 */       appendable.append(",mergeRule=");
/* 589 */       appendable.append(this.m_mergeRule);
/*     */     }
/* 591 */     if (verbose)
/*     */     {
/* 593 */       appendable.append("\ntrimValues=");
/* 594 */       appendable.append("" + this.m_trimValues);
/*     */     }
/* 596 */     appendable.append(",isWildcardMerge=");
/* 597 */     appendable.append("" + this.m_isWildcardMerge);
/* 598 */     if (verbose)
/*     */     {
/* 600 */       appendable.append(",mergeBlanks=");
/* 601 */       appendable.append("" + this.m_mergeBlanks);
/*     */     }
/* 603 */     appendable.append(",mergeAppendColumns=");
/* 604 */     appendForDebug(appendable, this.m_mergeAppendColumns);
/* 605 */     appendable.append(",mergeAppendSep=");
/* 606 */     appendForDebug(appendable, Character.valueOf(this.m_mergeAppendSep));
/* 607 */     appendable.append(",mergeAppendEq=");
/* 608 */     appendForDebug(appendable, Character.valueOf(this.m_mergeAppendEq));
/* 609 */     appendable.append(",mergeOtherData=");
/* 610 */     appendForDebug(appendable, this.m_mergeOtherData);
/* 611 */     appendable.append(",indexedColumns=");
/* 612 */     appendForDebug(appendable, this.m_indexedColumns);
/* 613 */     appendable.append(",includeColumns=");
/* 614 */     appendForDebug(appendable, this.m_includeColumns);
/* 615 */     appendable.append(",defaultValues=");
/* 616 */     appendForDebug(appendable, this.m_defaultValues);
/* 617 */     if (this.m_derivedColumns != null)
/*     */     {
/* 619 */       appendable.append(",derivedColumns=");
/* 620 */       appendForDebug(appendable, this.m_derivedColumns);
/*     */     }
/* 622 */     if (this.m_grammarData != null)
/*     */     {
/* 624 */       appendable.append(",grammarData=");
/* 625 */       appendForDebug(appendable, this.m_grammarData);
/*     */     }
/* 627 */     if (this.m_inclusionFilterName != null)
/*     */     {
/* 629 */       appendable.append(",inclusionFilterName=");
/* 630 */       appendable.append(this.m_inclusionFilterName);
/*     */     }
/* 632 */     if (this.m_isError)
/*     */     {
/* 634 */       appendable.append(",hasError[errorKey=");
/* 635 */       if (this.m_errorKey == null)
/*     */       {
/* 637 */         appendable.append("<not defined>");
/*     */       }
/*     */       else
/*     */       {
/* 641 */         appendable.append(this.m_errorKey);
/*     */       }
/* 643 */       appendable.append("]");
/*     */     }
/* 645 */     if (sortOptions != null)
/*     */     {
/* 647 */       appendable.append("\n");
/* 648 */       appendForDebug(appendable, sortOptions);
/*     */     }
/* 650 */     Table table = (this.m_hasMergedTable) ? this.m_mergedTable : this.m_table;
/* 651 */     if (table != null)
/*     */     {
/* 653 */       if (!verbose)
/*     */         return;
/* 655 */       appendable.append("\nTable Loaded\n");
/* 656 */       table.appendDebugFormat(appendable);
/*     */     }
/*     */     else
/*     */     {
/* 661 */       appendable.append("\nNo Table loaded\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendForDebug(IdcAppendable appendable, Object o)
/*     */   {
/* 671 */     StringUtils.appendForDebug(appendable, o, 0);
/*     */   }
/*     */ 
/*     */   public void handleTag(String tag)
/*     */   {
/* 681 */     this.m_format = tag;
/*     */   }
/*     */ 
/*     */   public void handleValue(String attribute, String value)
/*     */   {
/* 691 */     String testAttrib = attribute.toLowerCase();
/* 692 */     if (testAttrib.startsWith("merge"))
/*     */     {
/* 694 */       if (testAttrib.equals("mergerule"))
/*     */       {
/* 696 */         this.m_mergeRule = value;
/*     */       }
/* 698 */       else if (testAttrib.equals("mergekey"))
/*     */       {
/* 700 */         this.m_mergeKey = value;
/*     */       }
/* 702 */       else if (testAttrib.equals("mergenewkey"))
/*     */       {
/* 704 */         this.m_mergeNewKey = value;
/*     */       }
/* 706 */       else if (testAttrib.equals("mergeblanks"))
/*     */       {
/* 708 */         this.m_mergeBlanks = StringUtils.convertToBool(value, true);
/*     */       }
/* 710 */       else if (testAttrib.equals("mergeappendcolumns"))
/*     */       {
/* 712 */         this.m_mergeAppendColumns = StringUtils.makeListFromSequenceSimple(value);
/*     */       } else {
/* 714 */         if (!testAttrib.equals("mergeotherdata"))
/*     */           return;
/* 716 */         this.m_mergeOtherData = StringUtils.makeListFromSequenceSimple(value);
/*     */       }
/*     */     }
/* 719 */     else if (testAttrib.equals("notrim"))
/*     */     {
/* 721 */       this.m_trimValues = (!StringUtils.convertToBool(value, true));
/*     */     }
/* 723 */     else if (testAttrib.equals("wildcard"))
/*     */     {
/* 725 */       this.m_isWildcardMerge = StringUtils.convertToBool(value, true);
/*     */     }
/* 727 */     else if (testAttrib.equals("indexedcolumns"))
/*     */     {
/* 729 */       this.m_indexedColumns = StringUtils.makeListFromSequenceSimple(value);
/*     */     }
/* 731 */     else if (testAttrib.equals("countcolumn"))
/*     */     {
/* 733 */       this.m_countColumn = value;
/*     */     }
/* 735 */     else if ((testAttrib.equals("inclusionfiltername")) || (testAttrib.equals("filterinclude")))
/*     */     {
/* 737 */       this.m_inclusionFilterName = value;
/*     */     }
/* 739 */     else if (testAttrib.equals("includecolumns"))
/*     */     {
/* 741 */       this.m_includeColumns = StringUtils.makeListFromSequenceSimple(value);
/*     */     }
/* 743 */     else if (testAttrib.equals("defaultvalues"))
/*     */     {
/* 745 */       List defPairs = StringUtils.makeListFromSequenceSimple(value);
/* 746 */       List defValues = null;
/* 747 */       if (defPairs != null)
/*     */       {
/* 751 */         int numPairs = defPairs.size();
/* 752 */         defValues = new ArrayList(numPairs);
/* 753 */         for (int i = 0; i < numPairs; ++i)
/*     */         {
/* 755 */           String pair = (String)defPairs.get(i);
/* 756 */           List pairList = StringUtils.makeListFromSequence(pair, ':', '*', 32);
/*     */ 
/* 759 */           if ((pairList == null) || (pairList.size() <= 0))
/*     */             continue;
/* 761 */           String key = (String)pairList.get(0);
/* 762 */           if ((key == null) || (key.length() <= 0))
/*     */             continue;
/* 764 */           String[] pairArray = { key, "" };
/* 765 */           if (pairList.size() > 1)
/*     */           {
/* 767 */             pairArray[1] = ((String)pairList.get(1));
/*     */           }
/* 769 */           defValues.add(pairArray);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 774 */       this.m_defaultValues = defValues;
/*     */     }
/* 776 */     else if (testAttrib.startsWith("idoc"))
/*     */     {
/* 778 */       if (this.m_grammarData == null)
/*     */       {
/* 780 */         this.m_grammarData = new DynamicDataGrammar();
/*     */       }
/* 782 */       this.m_grammarData.setOption(testAttrib, value);
/*     */     }
/* 784 */     else if (testAttrib.startsWith("derived"))
/*     */     {
/* 786 */       if (this.m_derivedColumns == null)
/*     */       {
/* 788 */         this.m_derivedColumns = new ArrayList();
/*     */       }
/* 790 */       if (!testAttrib.equals("derivedcolumns"))
/*     */         return;
/* 792 */       List derivedColumnData = StringUtils.makeListFromSequenceSimple(value);
/* 793 */       for (String derivedColumnStr : derivedColumnData)
/*     */       {
/* 795 */         DynamicDataDerivedColumn colDef = new DynamicDataDerivedColumn();
/* 796 */         colDef.setOption("derivedColumns", derivedColumnStr);
/* 797 */         this.m_derivedColumns.add(colDef);
/*     */       }
/*     */ 
/*     */     }
/* 801 */     else if (testAttrib.startsWith("sort"))
/*     */     {
/* 803 */       SortOptions m = this.m_sortOptions;
/* 804 */       if (m == null)
/*     */       {
/* 806 */         m = new SortOptions();
/* 807 */         this.m_sortOptions = m;
/*     */       }
/* 809 */       m.setOption(testAttrib, value);
/*     */     }
/* 811 */     else if (testAttrib.startsWith("css"))
/*     */     {
/* 813 */       this.m_usingDefaultMergeAppendFormat = false;
/* 814 */       this.m_mergeAppendSep = ';';
/* 815 */       this.m_mergeAppendEq = ':';
/*     */     }
/*     */     else
/*     */     {
/* 819 */       Report.trace("idocscript", "The dynamicdata property " + attribute + " is unrecognized", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void handleError(String errKey, String errMessage, int attributeStartIndex, String attribute, int dataStart, int dataEnd, char[] data)
/*     */   {
/* 837 */     IdcStringBuilder buf = new IdcStringBuilder(dataEnd - dataStart + errMessage.length() + 50);
/* 838 */     buf.append("Improperly formed data definition tag (");
/* 839 */     buf.append(errMessage);
/* 840 */     buf.append(") for buffer\n");
/* 841 */     buf.append(data, dataStart, dataEnd - dataStart);
/* 842 */     Report.trace("idocscript", buf.toString(), null);
/* 843 */     this.m_isError = true;
/* 844 */     this.m_errorKey = errKey;
/*     */   }
/*     */ 
/*     */   public DynamicData shallowCloneForMerging()
/*     */   {
/* 856 */     DynamicData dynData = new DynamicData();
/* 857 */     dynData.m_format = this.m_format;
/* 858 */     dynData.m_dataText = this.m_dataText;
/* 859 */     dynData.m_trimValues = this.m_trimValues;
/* 860 */     dynData.m_mergeKey = this.m_mergeKey;
/* 861 */     dynData.m_mergeNewKey = this.m_mergeNewKey;
/* 862 */     dynData.m_mergeRule = this.m_mergeRule;
/* 863 */     dynData.m_isWildcardMerge = this.m_isWildcardMerge;
/* 864 */     dynData.m_mergeBlanks = this.m_mergeBlanks;
/* 865 */     dynData.m_mergeAppendColumns = this.m_mergeAppendColumns;
/* 866 */     dynData.m_usingDefaultMergeAppendFormat = this.m_usingDefaultMergeAppendFormat;
/* 867 */     dynData.m_mergeAppendSep = this.m_mergeAppendSep;
/* 868 */     dynData.m_mergeAppendEq = this.m_mergeAppendEq;
/* 869 */     dynData.m_collapseMultiValueColumns = this.m_collapseMultiValueColumns;
/* 870 */     dynData.m_mergeOtherData = this.m_mergeOtherData;
/* 871 */     dynData.m_inclusionFilterName = this.m_inclusionFilterName;
/* 872 */     dynData.m_table = this.m_table;
/* 873 */     SortOptions m = this.m_sortOptions;
/* 874 */     if (m != null)
/*     */     {
/* 876 */       dynData.m_sortOptions = m.cloneOptions();
/*     */     }
/* 878 */     dynData.m_indexedColumns = this.m_indexedColumns;
/* 879 */     dynData.m_includeColumns = this.m_includeColumns;
/* 880 */     dynData.m_defaultValues = this.m_defaultValues;
/* 881 */     dynData.m_grammarData = this.m_grammarData;
/* 882 */     dynData.m_derivedColumns = this.m_derivedColumns;
/* 883 */     return dynData;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 889 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 67644 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.DynamicData
 * JD-Core Version:    0.5.4
 */