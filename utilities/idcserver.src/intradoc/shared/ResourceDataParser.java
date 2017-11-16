/*      */ package intradoc.shared;
/*      */ 
/*      */ import intradoc.common.DynamicData;
/*      */ import intradoc.common.DynamicDataDerivedColumn;
/*      */ import intradoc.common.DynamicDataGrammar;
/*      */ import intradoc.common.DynamicDataMergeInfo;
/*      */ import intradoc.common.DynamicDataParser;
/*      */ import intradoc.common.DynamicDataUtils;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.ParseLocationInfo;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.SortOptions;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ 
/*      */ public class ResourceDataParser
/*      */   implements DynamicDataParser
/*      */ {
/*      */   public static final int BUFFER_SIZE = 2048;
/*      */   public static final int MERGE_APPEND_ONLY = 1;
/*      */   public static final int MERGE_BLANKS = 2;
/*      */   public static final int MERGE_NO_APPEND = 4;
/*      */   public static final int MERGE_WILDCARD = 8;
/*      */ 
/*      */   public int parseResource(Reader resReader, DynamicHtml dynHtml, DynamicData data, ParseOutput parseOutput)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/*   83 */     ParseLocationInfo parseInfo = parseOutput.m_parseInfo;
/*   84 */     String format = data.m_format;
/*   85 */     if (format == null)
/*      */     {
/*   87 */       format = "commatable";
/*      */     }
/*   89 */     int retVal = 1;
/*   90 */     if (format.equals("commatable"))
/*      */     {
/*   92 */       parseCommaTable(resReader, dynHtml, data, parseInfo);
/*      */     }
/*   94 */     else if (format.equals("htmltable"))
/*      */     {
/*   96 */       Table t = ResourceContainer.parseTable(resReader, null, parseInfo);
/*   97 */       data.m_table = t;
/*      */     }
/*      */     else
/*      */     {
/*  101 */       retVal = 0;
/*      */     }
/*  103 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean parseCommaTable(Reader resReader, DynamicHtml dynHtml, DynamicData data, ParseLocationInfo parseInfo)
/*      */     throws IOException
/*      */   {
/*  109 */     IdcStringBuilder strBuf = new IdcStringBuilder(4096);
/*      */ 
/*  111 */     int startIndex = 0;
/*  112 */     int endLineIndex = 0;
/*  113 */     int startNextLineIndex = 0;
/*  114 */     int endBufIndex = 0;
/*  115 */     int startSearchIndex = 0;
/*  116 */     boolean stopRead = false;
/*  117 */     boolean foundFieldDefs = false;
/*  118 */     ArrayList listStr = new ArrayList(20);
/*  119 */     String[] fieldNames = null;
/*  120 */     ArrayList rows = null;
/*      */     try
/*      */     {
/*  125 */       strBuf.m_disableToStringReleaseBuffers = true;
/*      */       while (true)
/*      */       {
/*  129 */         if (strBuf.getCapacity() - startIndex < 2048)
/*      */         {
/*  131 */           strBuf.ensureCapacity(strBuf.getCapacity() * 2);
/*      */         }
/*      */ 
/*  134 */         if (endBufIndex < IdcStringBuilder.m_totalCapacity)
/*      */         {
/*  136 */           char[] buf = strBuf.m_charArray;
/*  137 */           int curIndex = endBufIndex;
/*  138 */           while ((!stopRead) && (curIndex < buf.length))
/*      */           {
/*  140 */             int nread = resReader.read(buf, curIndex, buf.length - curIndex);
/*  141 */             if (nread <= 0)
/*      */             {
/*  143 */               stopRead = true;
/*      */             }
/*      */             else
/*      */             {
/*  147 */               curIndex += nread;
/*      */             }
/*      */           }
/*  150 */           endBufIndex = curIndex;
/*      */         }
/*  152 */         if (startIndex >= endBufIndex)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  158 */         strBuf.m_length = endBufIndex;
/*      */ 
/*  160 */         char[] buf = strBuf.m_charArray;
/*  161 */         boolean foundEndLineLastTime = false;
/*      */ 
/*  163 */         while (startSearchIndex < endBufIndex)
/*      */         {
/*  165 */           if (foundEndLineLastTime)
/*      */           {
/*  167 */             parseInfo.m_parseLine += 1;
/*  168 */             parseInfo.m_parseCharOffset = 0;
/*      */           }
/*  170 */           boolean foundLine = false;
/*  171 */           for (int i = startSearchIndex; i < endBufIndex; ++i)
/*      */           {
/*  173 */             if (buf[i] != '\n')
/*      */               continue;
/*  175 */             endLineIndex = i;
/*  176 */             startNextLineIndex = i + 1;
/*  177 */             foundLine = true;
/*  178 */             break;
/*      */           }
/*      */ 
/*  181 */           foundEndLineLastTime = foundLine;
/*  182 */           if ((!foundLine) && (stopRead))
/*      */           {
/*  184 */             foundLine = true;
/*  185 */             endLineIndex = endBufIndex;
/*  186 */             startNextLineIndex = endLineIndex;
/*      */           }
/*  188 */           if (!foundLine)
/*      */           {
/*  190 */             startSearchIndex = endBufIndex;
/*  191 */             break;
/*      */           }
/*      */ 
/*  195 */           while ((!foundFieldDefs) && 
/*  197 */             (buf[startIndex] < ' ') && (startIndex < endLineIndex))
/*      */           {
/*  199 */             ++startIndex;
/*      */           }
/*      */ 
/*  202 */           if ((endLineIndex > startIndex) && (buf[(endLineIndex - 1)] == '\r'))
/*      */           {
/*  205 */             --endLineIndex;
/*      */           }
/*      */ 
/*  209 */           listStr.clear();
/*  210 */           if (startIndex < endLineIndex)
/*      */           {
/*  212 */             boolean isComment = false;
/*  213 */             if ((endLineIndex >= startIndex + 2) && (strBuf.charAt(startIndex) == '-') && (strBuf.charAt(startIndex + 1) == '-'))
/*      */             {
/*  217 */               isComment = true;
/*      */             }
/*      */ 
/*  220 */             if (!isComment)
/*      */             {
/*  222 */               int flags = ((!foundFieldDefs) || (data.m_trimValues)) ? 32 : 0;
/*  223 */               StringUtils.appendListFromSequence(listStr, strBuf, startIndex, endLineIndex - startIndex, ',', '^', flags);
/*      */             }
/*      */           }
/*  226 */           int nvalues = listStr.size();
/*  227 */           if (nvalues > 0)
/*      */           {
/*  229 */             if (!foundFieldDefs)
/*      */             {
/*  231 */               String s = (String)listStr.get(0);
/*  232 */               if (s.length() > 0)
/*      */               {
/*  234 */                 char ch = s.charAt(0);
/*  235 */                 if (Character.isJavaIdentifierStart(ch))
/*      */                 {
/*  237 */                   fieldNames = new String[nvalues];
/*  238 */                   listStr.toArray(fieldNames);
/*  239 */                   foundFieldDefs = true;
/*      */                 }
/*      */               }
/*      */             }
/*      */             else
/*      */             {
/*  245 */               if (rows == null)
/*      */               {
/*  247 */                 rows = new ArrayList();
/*      */               }
/*  249 */               String[] row = new String[fieldNames.length];
/*  250 */               for (int i = 0; i < row.length; ++i)
/*      */               {
/*  252 */                 if (i < nvalues)
/*      */                 {
/*  254 */                   row[i] = ((String)listStr.get(i));
/*      */                 }
/*      */                 else
/*      */                 {
/*  258 */                   row[i] = "";
/*      */                 }
/*      */               }
/*  261 */               rows.add(row);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*  266 */           startSearchIndex = startNextLineIndex;
/*  267 */           startIndex = startSearchIndex;
/*      */         }
/*      */ 
/*  270 */         if (stopRead)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  276 */         System.arraycopy(buf, startIndex, buf, 0, endBufIndex - startIndex);
/*  277 */         endBufIndex -= startIndex;
/*  278 */         startSearchIndex -= startIndex;
/*  279 */         startIndex = 0;
/*  280 */         strBuf.m_length = endBufIndex;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  285 */       strBuf.releaseBuffers();
/*      */     }
/*  287 */     boolean retVal = false;
/*  288 */     if (fieldNames != null)
/*      */     {
/*  290 */       Table tableResult = new Table();
/*  291 */       tableResult.m_colNames = fieldNames;
/*  292 */       tableResult.m_rows = rows;
/*  293 */       data.m_table = tableResult;
/*  294 */       retVal = true;
/*      */     }
/*  296 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean mergeDataFromPriorScript(DynamicHtml dynHtml, DynamicData newData, DynamicDataMergeInfo mergeInfo)
/*      */   {
/*  312 */     if ((mergeInfo == null) || (mergeInfo.m_tableMergingDone))
/*      */     {
/*  314 */       return false;
/*      */     }
/*      */ 
/*  317 */     boolean doOtherData = true;
/*  318 */     if (!mergeInfo.m_mergePriorDataDone)
/*      */     {
/*  320 */       List dynHtmlToMerge = mergeInfo.m_dynamicHtmlToMerge;
/*  321 */       if (dynHtmlToMerge != null)
/*      */       {
/*  323 */         int nDynHtml = dynHtmlToMerge.size();
/*      */ 
/*  325 */         int endIndex = nDynHtml;
/*  326 */         for (int i = 0; i < nDynHtml; ++i)
/*      */         {
/*  328 */           DynamicHtml dynHtmlData = (DynamicHtml)dynHtmlToMerge.get(i);
/*  329 */           DynamicData data = dynHtmlData.m_dynamicData;
/*  330 */           if (data == null)
/*      */             continue;
/*  332 */           String mergeRule = data.m_mergeRule;
/*      */ 
/*  335 */           if (isMerging(mergeRule))
/*      */             continue;
/*  337 */           endIndex = i + 1;
/*  338 */           break;
/*      */         }
/*      */ 
/*  343 */         DynamicHtml prev = null;
/*  344 */         for (int i = endIndex - 1; i >= 0; --i)
/*      */         {
/*  346 */           DynamicHtml cur = (DynamicHtml)dynHtmlToMerge.get(i);
/*  347 */           mergePriorToCurrent(cur, prev, mergeInfo);
/*  348 */           prev = cur;
/*      */         }
/*      */ 
/*  354 */         if ((mergeInfo.m_mergeOtherData != null) && (mergeInfo.m_mergeOtherData.size() > 0))
/*      */         {
/*  356 */           doOtherData = false;
/*      */         }
/*      */         else
/*      */         {
/*  361 */           mergeInfo.m_mergeOtherDataDone = true;
/*      */         }
/*      */       }
/*      */ 
/*  365 */       mergeInfo.m_mergePriorDataDone = true;
/*      */     }
/*      */ 
/*  368 */     if ((doOtherData) && (!mergeInfo.m_mergeOtherDataDone))
/*      */     {
/*  370 */       List otherDynHtmlToMerge = mergeInfo.m_otherDynamicHtmlToMerge;
/*  371 */       if (otherDynHtmlToMerge != null)
/*      */       {
/*  373 */         for (DynamicHtml otherHtml : otherDynHtmlToMerge)
/*      */         {
/*  375 */           mergePriorToCurrent(otherHtml, null, mergeInfo);
/*      */         }
/*      */       }
/*  378 */       mergeInfo.m_mergeOtherDataDone = true;
/*      */     }
/*      */ 
/*  381 */     if ((mergeInfo.m_mergeOtherDataDone) && (!mergeInfo.m_mergeCopiedToData))
/*      */     {
/*  384 */       newData.copyOverMergedData(dynHtml, mergeInfo);
/*  385 */       mergeInfo.m_mergeCopiedToData = true;
/*      */     }
/*      */ 
/*  388 */     if (mergeInfo.m_mergeCopiedToData)
/*      */     {
/*  390 */       applyAutoAddedColumns(newData, dynHtml, mergeInfo);
/*  391 */       applySortingRules(newData, dynHtml, mergeInfo);
/*  392 */       applyAutoNumberingRule(newData, dynHtml, mergeInfo);
/*  393 */       applyMergeRuleColumns(newData, dynHtml, mergeInfo);
/*  394 */       applyIdocGrammarParsing(newData, dynHtml, mergeInfo);
/*  395 */       applyIndexingColumns(newData, dynHtml, mergeInfo);
/*  396 */       mergeInfo.m_tableMergingDone = true;
/*      */     }
/*      */ 
/*  399 */     return true;
/*      */   }
/*      */ 
/*      */   public void mergePriorToCurrent(DynamicHtml cur, DynamicHtml prev, DynamicDataMergeInfo mergeInfo)
/*      */   {
/*  404 */     DynamicData curData = cur.m_dynamicData;
/*  405 */     if (curData == null)
/*      */     {
/*  407 */       return;
/*      */     }
/*  409 */     if (!curData.m_hasMergedTable)
/*      */     {
/*  412 */       mergeInfo.m_derivedColumns = DynamicDataUtils.mergeObjectListsNoDuplicates(curData.m_derivedColumns, mergeInfo.m_derivedColumns, 0);
/*      */ 
/*  414 */       mergeInfo.m_defaultValues = DynamicDataUtils.mergeObjectListsNoDuplicates(curData.m_defaultValues, mergeInfo.m_defaultValues, 1);
/*      */ 
/*  417 */       applyAutoAddedColumns(curData, cur, mergeInfo);
/*      */ 
/*  420 */       if ((mergeInfo.m_mergeAppendFormatSet) && (!curData.m_usingDefaultMergeAppendFormat))
/*      */       {
/*  423 */         if (curData.m_usingDefaultMergeAppendFormat)
/*      */         {
/*  425 */           curData.m_usingDefaultMergeAppendFormat = mergeInfo.m_usingDefaultMergeAppendFormat;
/*  426 */           curData.m_mergeAppendSep = mergeInfo.m_mergeAppendSep;
/*  427 */           curData.m_mergeAppendEq = mergeInfo.m_mergeAppendEq;
/*      */         }
/*  429 */         else if ((curData.m_mergeAppendSep != mergeInfo.m_mergeAppendSep) || (curData.m_mergeAppendEq != mergeInfo.m_mergeAppendEq))
/*      */         {
/*  432 */           String dynDataTrace = curData.createReportString(false, null);
/*  433 */           Report.error("idocscript", null, IdcMessageFactory.lc("csDynHtmlMergeAppendConflict", new Object[] { dynDataTrace }));
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  438 */     String mergeRule = curData.m_mergeRule;
/*  439 */     boolean noAppend = (mergeRule != null) && (mergeRule.equals("mergenoappend"));
/*  440 */     Table curTable = (curData.m_hasMergedTable) ? curData.m_mergedTable : curData.m_table;
/*  441 */     if (mergeInfo.m_activeTable == null)
/*      */     {
/*  443 */       if (!noAppend)
/*      */       {
/*  445 */         mergeInfo.m_activeTable = curTable;
/*  446 */         mergeInfo.m_mergeAppendFormatSet = curData.m_usingDefaultMergeAppendFormat;
/*  447 */         mergeInfo.m_mergeAppendSep = curData.m_mergeAppendSep;
/*  448 */         mergeInfo.m_mergeAppendEq = curData.m_mergeAppendEq;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  453 */       String mergeKey = curData.m_mergeKey;
/*  454 */       String mergeNewKey = curData.m_mergeNewKey;
/*  455 */       if (mergeNewKey == null)
/*      */       {
/*  457 */         mergeNewKey = mergeKey;
/*      */       }
/*  459 */       boolean isMerge = isMerging(mergeRule);
/*  460 */       int mergeFlags = 0;
/*  461 */       if (isMerge)
/*      */       {
/*  463 */         if ((mergeRule != null) && (mergeRule.equalsIgnoreCase("append")))
/*      */         {
/*  465 */           mergeFlags |= 1;
/*      */         }
/*      */         else
/*      */         {
/*  469 */           if (noAppend)
/*      */           {
/*  471 */             mergeFlags |= 4;
/*      */           }
/*  473 */           if (curData.m_mergeBlanks)
/*      */           {
/*  475 */             mergeFlags |= 2;
/*      */           }
/*  477 */           if (curData.m_isWildcardMerge)
/*      */           {
/*  479 */             mergeFlags |= 8;
/*      */           }
/*      */         }
/*  482 */         if ((curTable != null) && (curTable.m_colNames != null) && (curTable.m_colNames.length > 0))
/*      */         {
/*  484 */           String[] mergeAppendColumns = null;
/*  485 */           if (curData.m_mergeAppendColumns != null)
/*      */           {
/*  487 */             mergeAppendColumns = StringUtils.convertListToArray(curData.m_mergeAppendColumns);
/*      */           }
/*  489 */           mergeTablesProvisionally(curTable, mergeInfo, mergeKey, mergeNewKey, mergeAppendColumns, mergeFlags);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  494 */         mergeInfo.clearDynamicData();
/*  495 */         mergeInfo.m_activeTable = curTable;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  501 */     mergeInfo.m_indexedColumns = DynamicDataUtils.mergeObjectListsNoDuplicates(curData.m_indexedColumns, mergeInfo.m_indexedColumns, 0);
/*      */ 
/*  503 */     mergeInfo.m_includeColumns = DynamicDataUtils.mergeObjectListsNoDuplicates(curData.m_includeColumns, mergeInfo.m_includeColumns, 0);
/*      */ 
/*  505 */     if (curData.m_grammarData != null)
/*      */     {
/*  507 */       DynamicDataGrammar curGrammarData = curData.m_grammarData;
/*  508 */       DynamicDataGrammar infoGrammarData = mergeInfo.m_grammarData;
/*  509 */       if (infoGrammarData == null)
/*      */       {
/*  511 */         infoGrammarData = new DynamicDataGrammar();
/*  512 */         mergeInfo.m_grammarData = infoGrammarData;
/*      */       }
/*  514 */       int flags = (curData.m_hasMergedTable) ? DynamicDataGrammar.F_ALWAYS_APPEND : 0;
/*  515 */       infoGrammarData.mergeIfNewIsNotNull(curGrammarData, flags);
/*      */     }
/*  517 */     if (curData.m_hasMergedTable) {
/*      */       return;
/*      */     }
/*  520 */     if (curData.m_sortOptions != null)
/*      */     {
/*  522 */       if (mergeInfo.m_sortOptions != null)
/*      */       {
/*  524 */         mergeInfo.m_sortOptions.mergeIfNewIsNotNull(curData.m_sortOptions);
/*      */       }
/*      */       else
/*      */       {
/*  528 */         mergeInfo.m_sortOptions = curData.m_sortOptions.cloneOptions();
/*      */       }
/*      */     }
/*  531 */     if ((curData.m_countColumn != null) && (mergeInfo.m_countColumn == null))
/*      */     {
/*  533 */       mergeInfo.m_countColumn = curData.m_countColumn;
/*      */     }
/*  535 */     mergeInfo.m_mergeOtherData = DynamicDataUtils.mergeObjectListsNoDuplicates(curData.m_mergeOtherData, mergeInfo.m_mergeOtherData, 0);
/*      */   }
/*      */ 
/*      */   public void mergeTablesProvisionally(Table newTable, DynamicDataMergeInfo info, String mergeCurKey, String mergeNewKey, String[] mergeAppendColumns, int mergeFlags)
/*      */   {
/*  543 */     Table activeTable = info.m_activeTable;
/*  544 */     String[] curFieldList = (info.m_useDynamicData) ? info.m_tempFieldList : activeTable.m_colNames;
/*  545 */     String[] newFieldList = newTable.m_colNames;
/*      */ 
/*  547 */     boolean isAppend = (mergeFlags & 0x1) != 0;
/*  548 */     boolean noAppend = (mergeFlags & 0x4) != 0;
/*  549 */     boolean mergeBlanks = (mergeFlags & 0x2) != 0;
/*  550 */     boolean mergeWildcard = (mergeFlags & 0x8) != 0;
/*      */ 
/*  554 */     int nFields = curFieldList.length;
/*  555 */     int[] fieldIndexMapNewToCur = new int[newFieldList.length];
/*  556 */     for (int i = 0; i < newFieldList.length; ++i)
/*      */     {
/*  558 */       boolean foundIt = false;
/*  559 */       for (int j = 0; j < curFieldList.length; ++j)
/*      */       {
/*  561 */         if (!newFieldList[i].equalsIgnoreCase(curFieldList[j]))
/*      */           continue;
/*  563 */         foundIt = true;
/*  564 */         fieldIndexMapNewToCur[i] = j;
/*  565 */         break;
/*      */       }
/*      */ 
/*  568 */       if (foundIt)
/*      */         continue;
/*  570 */       fieldIndexMapNewToCur[i] = (nFields++);
/*      */     }
/*      */ 
/*  576 */     String[] mergedList = null;
/*  577 */     if (nFields > curFieldList.length)
/*      */     {
/*  579 */       mergedList = new String[nFields];
/*  580 */       for (int i = 0; i < curFieldList.length; ++i)
/*      */       {
/*  582 */         mergedList[i] = curFieldList[i];
/*      */       }
/*  584 */       for (int i = 0; i < newFieldList.length; ++i)
/*      */       {
/*  588 */         int newIndex = fieldIndexMapNewToCur[i];
/*  589 */         mergedList[newIndex] = newFieldList[i];
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  594 */       mergedList = curFieldList;
/*      */     }
/*      */ 
/*  598 */     int mergeCurFieldIndex = -1;
/*  599 */     int mergeNewFieldIndex = -1;
/*  600 */     boolean hasMergeIndex = false;
/*      */ 
/*  602 */     if (!isAppend)
/*      */     {
/*  606 */       mergeCurFieldIndex = determineMergeIndex(mergeCurKey, curFieldList);
/*  607 */       if ((((mergeNewKey == null) || (mergeNewKey.length() == 0))) && (mergeCurFieldIndex >= 0))
/*      */       {
/*  609 */         mergeNewKey = curFieldList[mergeCurFieldIndex];
/*      */       }
/*  611 */       mergeNewFieldIndex = determineMergeIndex(mergeNewKey, newFieldList);
/*  612 */       hasMergeIndex = (mergeCurFieldIndex >= 0) && (mergeNewFieldIndex >= 0);
/*      */     }
/*      */ 
/*  616 */     String[][] curRows = (String[][])null;
/*      */ 
/*  618 */     if ((info.m_useDynamicData) && (info.m_tempRows != null))
/*      */     {
/*  620 */       curRows = info.m_tempRows;
/*      */     }
/*      */     else
/*      */     {
/*  624 */       List activeRows = activeTable.m_rows;
/*  625 */       if (activeRows != null)
/*      */       {
/*  627 */         curRows = new String[activeRows.size()][];
/*  628 */         activeRows.toArray(curRows);
/*      */       }
/*      */     }
/*      */ 
/*  632 */     boolean doRowMerge = true;
/*  633 */     if ((((!hasMergeIndex) || (curRows == null) || (curRows.length == 0))) && (noAppend))
/*      */     {
/*  637 */       doRowMerge = false;
/*      */     }
/*      */ 
/*  641 */     String[][] newRows = (String[][])null;
/*  642 */     List[] newRowsToCurRowsMap = null;
/*  643 */     boolean[] isMergeAppendColumn = null;
/*  644 */     if ((doRowMerge) && (newTable.m_rows != null) && (newTable.m_rows.size() > 0))
/*      */     {
/*  646 */       newRows = new String[newTable.m_rows.size()][];
/*  647 */       newTable.m_rows.toArray(newRows);
/*  648 */       newRowsToCurRowsMap = new List[newRows.length];
/*      */ 
/*  651 */       if (mergeAppendColumns != null)
/*      */       {
/*  653 */         for (int i = 0; i < mergeAppendColumns.length; ++i)
/*      */         {
/*  655 */           int index = StringUtils.findStringIndexEx(mergedList, mergeAppendColumns[i], true);
/*  656 */           if (index < 0)
/*      */             continue;
/*  658 */           if (isMergeAppendColumn == null)
/*      */           {
/*  660 */             isMergeAppendColumn = new boolean[mergedList.length];
/*      */           }
/*  662 */           isMergeAppendColumn[index] = true;
/*  663 */           if (info.m_collapseMultiValueColumns == null)
/*      */           {
/*  665 */             info.m_collapseMultiValueColumns = new HashSet();
/*      */           }
/*  667 */           info.m_collapseMultiValueColumns.add(mergeAppendColumns[i]);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  674 */     int nCurRows = 0;
/*  675 */     if (curRows != null)
/*      */     {
/*  677 */       nCurRows = curRows.length;
/*      */     }
/*      */ 
/*  680 */     if (newRows != null)
/*      */     {
/*  682 */       for (int i = 0; i < newRows.length; ++i)
/*      */       {
/*  684 */         boolean doLoop = true;
/*  685 */         boolean foundMatch = false;
/*  686 */         List toIndices = new ArrayList();
/*  687 */         if ((curRows == null) || (!hasMergeIndex))
/*      */         {
/*  689 */           doLoop = false;
/*      */         }
/*  691 */         String[] newRow = null;
/*  692 */         if (doLoop)
/*      */         {
/*  694 */           newRow = newRows[i];
/*  695 */           if ((newRow == null) || (mergeNewFieldIndex >= newRow.length))
/*      */           {
/*  697 */             doLoop = false;
/*      */           }
/*      */         }
/*  700 */         String newFieldVal = null;
/*  701 */         if (doLoop)
/*      */         {
/*  703 */           newFieldVal = newRow[mergeNewFieldIndex];
/*      */ 
/*  705 */           if (newFieldVal == null)
/*      */           {
/*  707 */             doLoop = false;
/*      */           }
/*      */         }
/*  710 */         if (doLoop)
/*      */         {
/*  712 */           for (int j = 0; j < curRows.length; ++j)
/*      */           {
/*  714 */             String[] curRow = curRows[j];
/*  715 */             boolean gotMatch = false;
/*  716 */             if ((curRow == null) || (mergeCurFieldIndex >= curRow.length))
/*      */               continue;
/*  718 */             String curFieldVal = curRow[mergeCurFieldIndex];
/*  719 */             if (curFieldVal != null)
/*      */             {
/*  721 */               if (mergeWildcard)
/*      */               {
/*  723 */                 if (StringUtils.matchEx(curFieldVal, newFieldVal, true, true))
/*      */                 {
/*  725 */                   gotMatch = true;
/*      */                 }
/*      */ 
/*      */               }
/*  730 */               else if (newFieldVal.equalsIgnoreCase(curFieldVal))
/*      */               {
/*  732 */                 gotMatch = true;
/*      */               }
/*      */             }
/*      */ 
/*  736 */             if (!gotMatch)
/*      */               continue;
/*  738 */             foundMatch = true;
/*  739 */             toIndices.add(new Integer(j));
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  745 */         if ((!foundMatch) && (!noAppend))
/*      */         {
/*  747 */           toIndices.add(new Integer(nCurRows++));
/*      */         }
/*  749 */         newRowsToCurRowsMap[i] = toIndices;
/*      */       }
/*      */     }
/*      */ 
/*  753 */     String[][] mergedRows = (String[][])null;
/*  754 */     if (newRowsToCurRowsMap != null)
/*      */     {
/*  756 */       mergedRows = new String[nCurRows][];
/*  757 */       if (curRows != null)
/*      */       {
/*  759 */         System.arraycopy(curRows, 0, mergedRows, 0, curRows.length);
/*      */       }
/*  761 */       boolean[] curMutatedRows = info.m_mutatedRows;
/*  762 */       if (curMutatedRows == null)
/*      */       {
/*  765 */         int length = 0;
/*  766 */         if (curRows != null)
/*      */         {
/*  768 */           length = curRows.length;
/*      */         }
/*  770 */         curMutatedRows = new boolean[length];
/*  771 */         info.m_mutatedRows = curMutatedRows;
/*      */       }
/*      */ 
/*  774 */       IdcStringBuilder scratchBuilder = new IdcStringBuilder();
/*  775 */       scratchBuilder.m_disableToStringReleaseBuffers = true;
/*      */ 
/*  777 */       for (int i = 0; i < newRowsToCurRowsMap.length; ++i)
/*      */       {
/*  779 */         List l = newRowsToCurRowsMap[i];
/*  780 */         if (l == null)
/*      */           continue;
/*  782 */         int nTargetRows = l.size();
/*  783 */         for (int j = 0; j < nTargetRows; ++j)
/*      */         {
/*  785 */           Integer iObj = (Integer)l.get(j);
/*  786 */           int targetRowIndex = iObj.intValue();
/*  787 */           String[] oldRow = mergedRows[targetRowIndex];
/*  788 */           String[] newMergedRow = null;
/*  789 */           if ((((targetRowIndex >= curMutatedRows.length) || (curMutatedRows[targetRowIndex] != 0))) && (oldRow != null) && (oldRow.length >= nFields))
/*      */           {
/*  792 */             newMergedRow = oldRow;
/*      */           }
/*      */           else
/*      */           {
/*  796 */             if (targetRowIndex < curMutatedRows.length)
/*      */             {
/*  801 */               curMutatedRows[targetRowIndex] = true;
/*      */             }
/*  803 */             newMergedRow = new String[nFields];
/*  804 */             if (oldRow != null)
/*      */             {
/*  806 */               for (int m = 0; m < oldRow.length; ++m)
/*      */               {
/*  808 */                 newMergedRow[m] = oldRow[m];
/*      */               }
/*      */             }
/*  811 */             mergedRows[targetRowIndex] = newMergedRow;
/*      */           }
/*  813 */           String[] newRowValues = newRows[i];
/*  814 */           for (int m = 0; m < newRowValues.length; ++m)
/*      */           {
/*  816 */             int newFieldIndex = fieldIndexMapNewToCur[m];
/*  817 */             String newVal = newRowValues[m];
/*  818 */             boolean isAppendColumn = (isMergeAppendColumn != null) && (isMergeAppendColumn[newFieldIndex] != 0);
/*  819 */             boolean doIt = (mergeBlanks) && (!isAppendColumn);
/*  820 */             if ((!doIt) && (newVal != null) && (newVal.length() > 0))
/*      */             {
/*  822 */               for (int k = 0; k < newVal.length(); ++k)
/*      */               {
/*  824 */                 if (newVal.charAt(k) <= ' ')
/*      */                 {
/*      */                   continue;
/*      */                 }
/*      */ 
/*  829 */                 doIt = true;
/*  830 */                 break;
/*      */               }
/*      */             }
/*      */ 
/*  834 */             if (!doIt)
/*      */               continue;
/*  836 */             String oldVal = null;
/*  837 */             if (isAppendColumn)
/*      */             {
/*  840 */               boolean isReal = false;
/*      */ 
/*  843 */               oldVal = newMergedRow[newFieldIndex];
/*  844 */               if ((oldVal != null) && (oldVal.length() > 0))
/*      */               {
/*  846 */                 for (int k = 0; k < oldVal.length(); ++k)
/*      */                 {
/*  848 */                   if (oldVal.charAt(k) <= ' ')
/*      */                     continue;
/*  850 */                   isReal = true;
/*  851 */                   break;
/*      */                 }
/*      */               }
/*      */ 
/*  855 */               isAppendColumn = isReal;
/*      */             }
/*  857 */             if (isAppendColumn)
/*      */             {
/*  859 */               scratchBuilder.setLength(0);
/*  860 */               scratchBuilder.append(oldVal).append(info.m_mergeAppendSep).append(newVal);
/*  861 */               newMergedRow[newFieldIndex] = scratchBuilder.toString();
/*      */             }
/*      */             else
/*      */             {
/*  865 */               newMergedRow[newFieldIndex] = newVal;
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  874 */       scratchBuilder.releaseBuffers();
/*      */     }
/*      */     else
/*      */     {
/*  878 */       mergedRows = curRows;
/*      */     }
/*      */ 
/*  882 */     info.m_tempFieldList = mergedList;
/*  883 */     info.m_tempRows = mergedRows;
/*  884 */     info.m_useDynamicData = true;
/*      */   }
/*      */ 
/*      */   public int determineMergeIndex(String key, String[] list)
/*      */   {
/*  889 */     if ((list == null) || (list.length == 0))
/*      */     {
/*  891 */       return -1;
/*      */     }
/*      */ 
/*  895 */     if ((key == null) || (key.length() == 0))
/*      */     {
/*  897 */       return 0;
/*      */     }
/*  899 */     return StringUtils.findStringIndexEx(list, key, true);
/*      */   }
/*      */ 
/*      */   public boolean isMerging(String mergeRule)
/*      */   {
/*  904 */     if (mergeRule == null)
/*      */     {
/*  906 */       return true;
/*      */     }
/*  908 */     return (StringUtils.convertToBool(mergeRule, true)) && (!mergeRule.equalsIgnoreCase("Replace"));
/*      */   }
/*      */ 
/*      */   public void applyAutoAddedColumns(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/*  913 */     if ((info == null) || (dynData == null))
/*      */     {
/*  915 */       return;
/*      */     }
/*      */ 
/*  922 */     List derivedColumns = info.m_derivedColumns;
/*  923 */     List defaultValues = (dynData.m_hasMergedTable) ? info.m_defaultValues : dynData.m_defaultValues;
/*      */ 
/*  925 */     if ((derivedColumns == null) && (defaultValues == null))
/*      */     {
/*  927 */       return;
/*      */     }
/*      */ 
/*  931 */     Table t = (dynData.m_hasMergedTable) ? dynData.m_mergedTable : dynData.m_table;
/*      */ 
/*  933 */     if ((t == null) || (t.m_colNames == null) || (t.m_rows == null) || (t.m_rows.size() == 0))
/*      */     {
/*  935 */       return;
/*      */     }
/*      */ 
/*  938 */     String[] cols = t.m_colNames;
/*  939 */     List rows = t.m_rows;
/*  940 */     int nDefaultDefs = (defaultValues != null) ? defaultValues.size() : 0;
/*  941 */     int nDerivedColumns = (derivedColumns != null) ? derivedColumns.size() : 0;
/*  942 */     List newFields = null;
/*  943 */     int nDefs = nDefaultDefs + nDerivedColumns;
/*  944 */     String[] addCols = new String[nDefs];
/*  945 */     IdcStringBuilder derivedColBuilder = null;
/*      */ 
/*  948 */     String[] defVals = null;
/*      */ 
/*  950 */     int buildIndex = 0;
/*  951 */     if (nDefaultDefs > 0)
/*      */     {
/*  953 */       defVals = new String[nDefaultDefs];
/*  954 */       for (int i = 0; i < nDefaultDefs; ++i)
/*      */       {
/*  956 */         String[] defValMap = (String[])defaultValues.get(i);
/*  957 */         addCols[buildIndex] = defValMap[0];
/*  958 */         defVals[i] = defValMap[1];
/*  959 */         ++buildIndex;
/*      */       }
/*      */     }
/*      */ 
/*  963 */     int derivedStart = buildIndex;
/*  964 */     for (int i = 0; i < nDerivedColumns; ++i)
/*      */     {
/*  966 */       DynamicDataDerivedColumn derivedColumnData = (DynamicDataDerivedColumn)derivedColumns.get(i);
/*  967 */       addCols[buildIndex] = derivedColumnData.m_name;
/*  968 */       ++buildIndex;
/*      */     }
/*      */ 
/*  973 */     int[] valuesColIndices = new int[nDefs];
/*  974 */     int nFields = cols.length;
/*      */ 
/*  976 */     for (int i = 0; i < nDefs; ++i)
/*      */     {
/*  978 */       String addCol = addCols[i];
/*  979 */       int index = -1;
/*  980 */       for (int j = 0; j < cols.length; ++j)
/*      */       {
/*  982 */         if (!addCol.equalsIgnoreCase(cols[j]))
/*      */           continue;
/*  984 */         index = j;
/*  985 */         break;
/*      */       }
/*      */ 
/*  988 */       if (index < 0)
/*      */       {
/*  990 */         if (newFields == null)
/*      */         {
/*  992 */           newFields = new ArrayList();
/*      */         }
/*  994 */         newFields.add(addCol);
/*  995 */         index = nFields++;
/*      */       }
/*  997 */       valuesColIndices[i] = index;
/*      */     }
/*      */ 
/* 1000 */     if (nFields > cols.length)
/*      */     {
/* 1002 */       cols = StringUtils.convertAndAppendListToArray(cols, newFields);
/* 1003 */       t.m_colNames = cols;
/*      */     }
/*      */ 
/* 1006 */     int nrows = rows.size();
/* 1007 */     for (int i = 0; i < nrows; ++i)
/*      */     {
/* 1009 */       String[] row = (String[])(String[])rows.get(i);
/* 1010 */       if ((row == null) || (row.length < nFields))
/*      */       {
/* 1012 */         row = StringUtils.reallocStringArray(row, nFields, "");
/* 1013 */         rows.set(i, row);
/*      */       }
/* 1015 */       for (int j = 0; j < valuesColIndices.length; ++j)
/*      */       {
/* 1017 */         int index = valuesColIndices[j];
/* 1018 */         String oldVal = row[index];
/* 1019 */         if ((oldVal != null) && (oldVal.length() > 0))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1024 */         if (j < derivedStart)
/*      */         {
/* 1026 */           row[index] = defVals[j];
/*      */         }
/*      */         else
/*      */         {
/* 1030 */           int offset = derivedStart - j;
/* 1031 */           DynamicDataDerivedColumn derivedColumn = (DynamicDataDerivedColumn)derivedColumns.get(offset);
/*      */           int[] derivedMap;
/*      */           int[] derivedMap;
/* 1033 */           if (!derivedColumn.m_computedSourceColumnIndices)
/*      */           {
/* 1036 */             int nSubDerivedCols = derivedColumn.m_sourceColumns.size();
/* 1037 */             int[] map = new int[nSubDerivedCols];
/* 1038 */             for (int k = 0; k < nSubDerivedCols; ++k)
/*      */             {
/* 1040 */               String subDerivedCol = (String)derivedColumn.m_sourceColumns.get(k);
/* 1041 */               map[k] = StringUtils.findStringIndexEx(cols, subDerivedCol, true);
/*      */             }
/* 1043 */             derivedColumn.m_sourceColumnToIndexMap = map;
/* 1044 */             derivedColumn.m_computedSourceColumnIndices = true;
/* 1045 */             derivedMap = map;
/*      */           }
/*      */           else
/*      */           {
/* 1049 */             derivedMap = derivedColumn.m_sourceColumnToIndexMap;
/*      */           }
/* 1051 */           if (derivedColBuilder == null)
/*      */           {
/* 1053 */             derivedColBuilder = new IdcStringBuilder();
/* 1054 */             derivedColBuilder.m_disableToStringReleaseBuffers = true;
/*      */           }
/*      */           else
/*      */           {
/* 1058 */             derivedColBuilder.setLength(0);
/*      */           }
/* 1060 */           for (int k = 0; k < derivedMap.length; ++k)
/*      */           {
/* 1062 */             int subDerivedColIndex = derivedMap[k];
/* 1063 */             if (k > 0)
/*      */             {
/* 1065 */               String sep = (derivedColumn.m_separator != null) ? derivedColumn.m_separator : "::";
/*      */ 
/* 1067 */               derivedColBuilder.append(sep);
/*      */             }
/* 1069 */             if (subDerivedColIndex < 0)
/*      */               continue;
/* 1071 */             derivedColBuilder.append(row[subDerivedColIndex]);
/*      */           }
/*      */ 
/* 1074 */           row[index] = derivedColBuilder.toString();
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1079 */     if (derivedColBuilder != null)
/*      */     {
/* 1081 */       derivedColBuilder.releaseBuffers();
/*      */     }
/*      */ 
/* 1085 */     dynData.m_defaultValues = info.m_defaultValues;
/*      */   }
/*      */ 
/*      */   public void applySortingRules(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/* 1090 */     if ((info == null) || (dynData == null) || (!dynData.m_hasMergedTable))
/*      */     {
/* 1092 */       return;
/*      */     }
/* 1094 */     Table t = dynData.m_mergedTable;
/* 1095 */     if ((t == null) || (t.m_colNames == null) || (t.m_rows == null) || (t.m_rows.size() == 0))
/*      */     {
/* 1097 */       return;
/*      */     }
/* 1099 */     String[] cols = t.m_colNames;
/* 1100 */     List rows = t.m_rows;
/* 1101 */     SortOptions sortOptions = info.m_sortOptions;
/* 1102 */     if (sortOptions == null)
/*      */     {
/* 1104 */       sortOptions = new SortOptions();
/*      */     }
/* 1106 */     if (sortOptions.m_sortKey == null)
/*      */     {
/* 1109 */       sortOptions.m_sortKey = "loadOrder";
/*      */     }
/* 1111 */     if ((sortOptions.m_sortType == null) || (sortOptions.m_sortType.length() == 0))
/*      */     {
/* 1113 */       sortOptions.m_sortType = "int";
/*      */     }
/* 1115 */     if ((((sortOptions.m_childSortKey == null) || (sortOptions.m_childSortKey.length() == 0))) && 
/* 1117 */       (cols != null) && (cols.length > 0))
/*      */     {
/* 1119 */       sortOptions.m_childSortKey = cols[0];
/*      */     }
/*      */ 
/* 1122 */     if ((sortOptions.m_sortNestLevelKey == null) || (sortOptions.m_sortNestLevelKey.length() == 0))
/*      */     {
/* 1124 */       sortOptions.m_sortNestLevelKey = "nestLevel";
/*      */     }
/* 1126 */     if (sortOptions.computeColumnIndices(cols))
/*      */     {
/* 1129 */       ResultSetTreeSort treeSort = new ResultSetTreeSort(rows);
/* 1130 */       treeSort.setSortOptions(sortOptions);
/* 1131 */       treeSort.sort();
/*      */     }
/* 1135 */     else if ((SystemUtils.m_verbose) && (sortOptions.m_isTreeSort))
/*      */     {
/* 1137 */       String dynamicDataTrace = dynData.createReportString(false, sortOptions);
/* 1138 */       Report.debug("idocscript", "Invalid tree sort for dynamicData = " + dynamicDataTrace, null);
/*      */     }
/*      */ 
/* 1144 */     dynData.m_sortOptions = sortOptions;
/*      */   }
/*      */ 
/*      */   public void applyAutoNumberingRule(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/* 1149 */     if ((info == null) || (dynData == null) || (!dynData.m_hasMergedTable))
/*      */     {
/* 1151 */       return;
/*      */     }
/* 1153 */     Table t = dynData.m_mergedTable;
/* 1154 */     if ((t == null) || (t.m_colNames == null) || (t.m_rows == null) || (t.m_rows.size() == 0))
/*      */     {
/* 1156 */       return;
/*      */     }
/* 1158 */     String[] cols = t.m_colNames;
/* 1159 */     List rows = t.m_rows;
/* 1160 */     String countColumn = info.m_countColumn;
/* 1161 */     if (countColumn == null)
/*      */     {
/* 1163 */       countColumn = "count";
/*      */     }
/* 1165 */     int colIndex = StringUtils.findStringIndexEx(cols, countColumn, true);
/* 1166 */     if (colIndex >= 0)
/*      */     {
/* 1168 */       for (int i = 0; i < rows.size(); ++i)
/*      */       {
/* 1170 */         String[] row = (String[])(String[])rows.get(i);
/* 1171 */         if (row.length <= colIndex)
/*      */         {
/* 1173 */           row = StringUtils.reallocStringArray(row, cols.length, "");
/*      */         }
/* 1175 */         row[colIndex] = ("" + (i + 1));
/*      */       }
/*      */ 
/* 1178 */       dynData.m_countColumn = countColumn;
/*      */     }
/*      */     else
/*      */     {
/* 1182 */       dynData.m_countColumn = null;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void applyMergeRuleColumns(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/* 1189 */     if (info.m_collapseMultiValueColumns == null)
/*      */       return;
/* 1191 */     Table t = dynData.m_mergedTable;
/* 1192 */     if ((t == null) || (t.m_colNames == null))
/*      */     {
/* 1194 */       return;
/*      */     }
/*      */ 
/* 1197 */     String[] colNames = t.m_colNames;
/* 1198 */     for (String mergeColumn : info.m_collapseMultiValueColumns)
/*      */     {
/* 1200 */       int index = StringUtils.findStringIndexEx(colNames, mergeColumn, true);
/* 1201 */       if (index < 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1206 */       List rows = t.m_rows;
/* 1207 */       if (rows != null)
/*      */       {
/* 1209 */         int nrows = rows.size();
/* 1210 */         for (int i = 0; i < nrows; ++i)
/*      */         {
/* 1212 */           String[] row = (String[])(String[])rows.get(i);
/* 1213 */           if (row == null) continue; if (index >= row.length) {
/*      */             continue;
/*      */           }
/*      */ 
/* 1217 */           String mergedColumnValue = row[index];
/* 1218 */           row[index] = DynamicDataUtils.mergeAppendColumnValue(mergedColumnValue, dynData.m_mergeAppendSep, dynData.m_mergeAppendEq);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void applyIdocGrammarParsing(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/* 1228 */     if ((info == null) || (dynData == null) || (!dynData.m_hasMergedTable) || (info.m_grammarData == null))
/*      */     {
/* 1230 */       return;
/*      */     }
/* 1232 */     DynamicDataGrammar grammarData = info.m_grammarData;
/* 1233 */     if (grammarData.m_unparsedPrepareElement == null)
/*      */       return;
/*      */   }
/*      */ 
/*      */   public void applyIndexingColumns(DynamicData dynData, DynamicHtml dynHtml, DynamicDataMergeInfo info)
/*      */   {
/* 1241 */     if ((info == null) || (dynData == null) || (!dynData.m_hasMergedTable))
/*      */     {
/* 1243 */       return;
/*      */     }
/* 1245 */     Table t = dynData.m_mergedTable;
/* 1246 */     if ((t == null) || (t.m_colNames == null))
/*      */     {
/* 1248 */       return;
/*      */     }
/* 1250 */     String[] colNames = t.m_colNames;
/* 1251 */     int numCols = colNames.length;
/*      */ 
/* 1253 */     List indexedCols = info.m_indexedColumns;
/* 1254 */     if (indexedCols == null)
/*      */     {
/* 1256 */       return;
/*      */     }
/*      */ 
/* 1259 */     Map[] lookups = new Map[numCols];
/*      */ 
/* 1261 */     for (int i = 0; i < indexedCols.size(); ++i)
/*      */     {
/* 1263 */       String indexedCol = (String)indexedCols.get(i);
/* 1264 */       int index = StringUtils.findStringIndexEx(colNames, indexedCol, true);
/* 1265 */       if (index < 0)
/*      */         continue;
/* 1267 */       lookups[index] = new HashMap();
/*      */     }
/*      */ 
/* 1273 */     List rows = t.m_rows;
/* 1274 */     if (rows != null)
/*      */     {
/* 1276 */       int nrows = rows.size();
/* 1277 */       for (int i = 0; i < nrows; ++i)
/*      */       {
/* 1279 */         String[] row = (String[])(String[])rows.get(i);
/* 1280 */         for (int j = 0; j < numCols; ++j)
/*      */         {
/* 1282 */           Map m = lookups[j];
/* 1283 */           if (m == null)
/*      */             continue;
/* 1285 */           String key = row[j].toLowerCase();
/* 1286 */           Table curTable = (Table)m.get(key);
/* 1287 */           if (curTable == null)
/*      */           {
/* 1289 */             curTable = new Table(colNames, new ArrayList());
/* 1290 */             m.put(key, curTable);
/*      */           }
/* 1292 */           curTable.m_rows.add(row);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1298 */     dynData.m_indexedLookups = lookups;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1304 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 77786 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ResourceDataParser
 * JD-Core Version:    0.5.4
 */