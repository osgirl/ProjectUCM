/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicDataUtils;
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SortOptions;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetMerge;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.DataTransformationUtils;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import intradoc.util.IdcAppendableBase;
/*     */ import java.io.IOException;
/*     */ import java.util.Arrays;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DynamicDataScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public DynamicDataScriptExtensions()
/*     */   {
/*  56 */     this.m_variableTable = new String[0];
/*     */ 
/*  60 */     this.m_variableDefinitionTable = new int[0][];
/*     */ 
/*  64 */     this.m_functionTable = new String[] { "ddLoadResultSet", "ddLoadIndexedColumnResultSet", "ddAppendResultSet", "ddAppendIndexedColumnResultSet", "ddSetLocalByColumnsFromFirstRow", "ddSetLocalByColumnsFromFirstRowIndexed", "ddSetLocalEmptyByColumns", "ddMergeResultSet", "ddMergeIndexedColumnResultSet", "ddMergeUsingIndexedKey", "ddGetFieldList", "ddApplyTableSortToResultSet", "ddIncludePreserveValues", "ddSetLocal", "ddSetLocalEmpty" };
/*     */ 
/*  90 */     this.m_functionDefinitionTable = new int[][] { { 0, -1, 0, 0, 1 }, { 1, -1, 0, 0, 1 }, { 2, -1, 0, 0, 1 }, { 3, -1, 0, 0, 1 }, { 4, 1, 0, -1, 1 }, { 5, 3, 0, 0, 1 }, { 6, 1, 0, -1, 1 }, { 7, -1, 0, 0, 1 }, { 8, -1, 0, 0, 1 }, { 9, -1, 0, 0, 1 }, { 10, 1, 0, -1, 0 }, { 11, 2, 0, 0, 1 }, { 12, 2, 0, 0, 0 }, { 13, -1, 0, -1, 0 }, { 14, -1, 0, -1, 0 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 114 */     int[] config = (int[])(int[])info.m_entry;
/* 115 */     String function = info.m_key;
/*     */ 
/* 117 */     int nargs = args.length - 1;
/* 118 */     int allowedParams = config[1];
/* 119 */     String insufficientArgsMsg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, new StringBuilder().append("").append(allowedParams).toString());
/*     */ 
/* 121 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 123 */       throw new IllegalArgumentException(insufficientArgsMsg);
/*     */     }
/*     */ 
/* 126 */     String msg = LocaleUtils.encodeMessage("csScriptMustBeInService", null, function, "Service");
/*     */ 
/* 128 */     DynamicHtmlMerger merger = ScriptExtensionUtils.getDynamicHtmlMerger(context, msg);
/*     */ 
/* 133 */     String sArg1 = null;
/* 134 */     String sArg2 = null;
/*     */ 
/* 136 */     long lArg2 = 0L;
/* 137 */     lArg2 += 0L;
/* 138 */     if (nargs > 0)
/*     */     {
/* 140 */       if (config[2] == 0)
/*     */       {
/* 142 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/*     */     }
/* 144 */     if ((config[2] != 1) || 
/* 150 */       (nargs > 1))
/*     */     {
/* 152 */       if (config[3] == 0)
/*     */       {
/* 154 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 156 */       else if (config[3] == 1)
/*     */       {
/* 158 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 163 */     boolean bResult = false;
/* 164 */     int iResult = 0;
/* 165 */     double dResult = 0.0D;
/*     */ 
/* 167 */     Object oResult = null;
/*     */     try
/*     */     {
/* 171 */       switch (config[0])
/*     */       {
/*     */       case 0:
/*     */       case 1:
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/*     */       case 9:
/*     */       case 13:
/*     */       case 14:
/* 192 */         int choice = config[0];
/* 193 */         boolean isIndexed = (choice == 1) || (choice == 3) || (choice == 5) || (choice == 8) || (choice == 9);
/* 194 */         boolean isAppend = (choice == 2) || (choice == 3);
/* 195 */         boolean isLocalSet = (choice == 4) || (choice == 5) || (choice == 6) || (choice == 13) || (choice == 14);
/* 196 */         boolean isNameValueRows = (choice == 13) || (choice == 14);
/* 197 */         boolean clearLocal = (choice == 6) || (choice == 14);
/* 198 */         boolean isMerge = (choice == 7) || (choice == 8) || (choice == 9);
/* 199 */         boolean isMergeOrAppend = (isAppend) || (isMerge);
/* 200 */         boolean isMergeUsingIndexedKey = choice == 9;
/* 201 */         Table t = null;
/* 202 */         String drsetKey = sArg2;
/* 203 */         int argIndex = (isLocalSet) ? 1 : 2;
/* 204 */         int minArgs = (isIndexed) ? argIndex + 2 : argIndex;
/* 205 */         if (isMergeUsingIndexedKey)
/*     */         {
/* 207 */           minArgs = 3;
/*     */         }
/*     */ 
/* 210 */         if (nargs < minArgs)
/*     */         {
/* 212 */           String insufficientArgsMsg2 = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, new StringBuilder().append("").append(minArgs).toString());
/*     */ 
/* 214 */           throw new IllegalArgumentException(insufficientArgsMsg2);
/*     */         }
/*     */ 
/* 217 */         DynamicData result = merger.getDynamicDataResource(sArg1, null);
/* 218 */         String colName = null;
/* 219 */         if (result != null)
/*     */         {
/* 221 */           if (isIndexed)
/*     */           {
/* 223 */             colName = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/* 224 */             if (!isMergeUsingIndexedKey)
/*     */             {
/* 226 */               String value = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/* 227 */               t = result.getIndexedTable(colName, value);
/*     */             }
/*     */             else
/*     */             {
/* 231 */               t = result.m_mergedTable;
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 236 */             t = result.m_mergedTable;
/*     */           }
/* 238 */           if (t != null)
/*     */           {
/* 240 */             DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/* 241 */             if (isLocalSet)
/*     */             {
/* 243 */               if (isNameValueRows)
/*     */               {
/* 245 */                 if ((t.m_rows != null) && (t.m_colNames != null))
/*     */                 {
/* 247 */                   int keyIndex = 0;
/* 248 */                   int valueIndex = (t.m_colNames.length > 1) ? 1 : -1;
/* 249 */                   if (nargs > 1)
/*     */                   {
/* 251 */                     String arg = ScriptUtils.getDisplayString(args[1], context);
/* 252 */                     if ((arg != null) && (arg.length() > 0))
/*     */                     {
/* 254 */                       keyIndex = StringUtils.findStringIndexEx(t.m_colNames, arg, true);
/*     */                     }
/* 256 */                     if (nargs > 2)
/*     */                     {
/* 258 */                       arg = ScriptUtils.getDisplayString(args[1], context);
/* 259 */                       if ((arg != null) && (arg.length() > 0))
/*     */                       {
/* 261 */                         valueIndex = StringUtils.findStringIndexEx(t.m_colNames, arg, true);
/*     */                       }
/*     */                     }
/*     */                   }
/*     */ 
/* 266 */                   int n = t.getNumRows();
/* 267 */                   for (int a = 0; a < n; ++a)
/*     */                   {
/* 269 */                     String[] currRow = t.getRow(a);
/* 270 */                     String name = currRow[keyIndex];
/* 271 */                     String value = ((clearLocal) || (valueIndex < 0)) ? "" : currRow[valueIndex];
/* 272 */                     if ((name == null) || (name.length() <= 0))
/*     */                       continue;
/* 274 */                     if (!clearLocal) { if (value == null) continue; if (value.length() < 1)
/*     */                       {
/*     */                         continue;
/*     */                       }
/*     */  }
/*     */ 
/*     */ 
/* 281 */                     if (value.contains("<$"))
/*     */                     {
/*     */                       try
/*     */                       {
/* 285 */                         value = merger.evaluateScript(value);
/*     */                       }
/*     */                       catch (Exception e)
/*     */                       {
/* 289 */                         Report.error("dynamicdata", null, e);
/*     */                       }
/*     */                     }
/* 292 */                     binder.putLocal(name, value);
/*     */                   }
/*     */                 }
/*     */ 
/*     */               }
/*     */               else
/*     */               {
/* 299 */                 boolean doLocalSet = true;
/* 300 */                 String[] row = null;
/* 301 */                 if (!clearLocal)
/*     */                 {
/* 303 */                   List rows = t.m_rows;
/* 304 */                   if ((rows != null) && (rows.size() > 0))
/*     */                   {
/* 306 */                     row = (String[])(String[])rows.get(0);
/*     */                   }
/*     */                   else
/*     */                   {
/* 310 */                     doLocalSet = false;
/*     */                   }
/*     */                 }
/* 313 */                 if (doLocalSet)
/*     */                 {
/* 315 */                   for (int i = 0; i < t.m_colNames.length; ++i)
/*     */                   {
/* 317 */                     String val = (row != null) ? row[i] : "";
/* 318 */                     binder.putLocal(t.m_colNames[i], val);
/*     */                   }
/*     */                 }
/*     */               }
/*     */ 
/*     */             }
/*     */             else
/*     */             {
/* 326 */               int extraArgsIndex = ((isMerge) && (!isMergeUsingIndexedKey)) ? argIndex + 1 : argIndex;
/* 327 */               String[] inclusionFilterNameParam = { result.m_inclusionFilterName };
/* 328 */               List[] includeColumnsParam = { result.m_includeColumns };
/* 329 */               if (extraArgsIndex < args.length - 1)
/*     */               {
/* 331 */                 Table newTable = DynamicDataUtils.applyColumnRenaming(t, args, extraArgsIndex, inclusionFilterNameParam, includeColumnsParam, merger, context);
/*     */ 
/* 333 */                 if (newTable != null)
/*     */                 {
/* 335 */                   t = newTable;
/*     */                 }
/* 337 */                 else if (SystemUtils.m_verbose)
/*     */                 {
/* 339 */                   String otherTableKey = ScriptUtils.getDisplayString(args[(extraArgsIndex++)], context);
/* 340 */                   if (otherTableKey.length() > 0)
/*     */                   {
/* 342 */                     Report.debug("idocscript", new StringBuilder().append("Column mapping using table ").append(otherTableKey).append(" to table ").append(sArg1).append(" did not succeed").toString(), null);
/*     */                   }
/*     */                 }
/*     */               }
/*     */ 
/* 347 */               DataResultSet drsetTarget = null;
/* 348 */               if (isMergeOrAppend)
/*     */               {
/* 350 */                 drsetTarget = ResultSetUtils.getMutableResultSet(binder, drsetKey, true, false);
/*     */               }
/* 352 */               if ((isMergeOrAppend) && (drsetTarget != null))
/*     */               {
/* 354 */                 List rows = t.m_rows;
/* 355 */                 if (rows != null)
/*     */                 {
/* 357 */                   String[] colNames = t.m_colNames;
/* 358 */                   List l = Arrays.asList(colNames);
/* 359 */                   drsetTarget.mergeFieldsWithFlags(l, 0);
/* 360 */                   if (isMergeUsingIndexedKey)
/*     */                   {
/* 362 */                     int indexedColIndex = drsetTarget.getFieldInfoIndex(colName);
/* 363 */                     int[] columnMappings = new int[colNames.length];
/* 364 */                     for (int i = 0; i < colNames.length; ++i)
/*     */                     {
/* 366 */                       columnMappings[i] = drsetTarget.getFieldInfoIndex(colNames[i]);
/*     */                     }
/*     */ 
/* 369 */                     drsetTarget.first();
/* 370 */                     while (drsetTarget.isRowPresent())
/*     */                     {
/* 372 */                       String value = drsetTarget.getStringValue(indexedColIndex);
/* 373 */                       Table table = result.getIndexedTable(colName, value);
/* 374 */                       if ((table != null) && (table.getNumRows() > 0))
/*     */                       {
/* 376 */                         String[] row = table.getRow(0);
/* 377 */                         for (int i = 0; i < row.length; ++i)
/*     */                         {
/* 379 */                           if (row[i].length() <= 0)
/*     */                             continue;
/* 381 */                           drsetTarget.setCurrentValue(columnMappings[i], row[i]);
/*     */                         }
/*     */ 
/*     */                       }
/*     */ 
/* 386 */                       drsetTarget.next();
/*     */                     }
/*     */                   }
/*     */                   else
/*     */                   {
/* 391 */                     DataResultSet newResultSet = new DataResultSet();
/* 392 */                     newResultSet.copyFieldInfo(drsetTarget);
/* 393 */                     FieldInfo[] fi = ResultSetUtils.createInfoList(drsetTarget, colNames, true);
/* 394 */                     int nRows = rows.size();
/* 395 */                     for (int i = 0; i < nRows; ++i)
/*     */                     {
/* 397 */                       List newRow = newResultSet.createEmptyRowAsList();
/* 398 */                       String[] row = (String[])(String[])rows.get(i);
/* 399 */                       for (int j = 0; j < row.length; ++j)
/*     */                       {
/* 401 */                         int index = fi[j].m_index;
/* 402 */                         newRow.set(index, row[j]);
/*     */                       }
/* 404 */                       newResultSet.addRowWithList(newRow);
/*     */                     }
/* 406 */                     if ((inclusionFilterNameParam[0] != null) || (includeColumnsParam[0] != null))
/*     */                     {
/* 408 */                       newResultSet = applyIncludes(newResultSet, binder, merger, inclusionFilterNameParam[0], includeColumnsParam[0], sArg1, drsetKey);
/*     */                     }
/*     */ 
/* 411 */                     if (isAppend)
/*     */                     {
/* 413 */                       drsetTarget.appendCompatibleRows(newResultSet);
/*     */                     }
/* 417 */                     else if (colNames.length > 0)
/*     */                     {
/* 419 */                       String mergeKey = result.m_mergeKey;
/* 420 */                       if ((mergeKey == null) || (mergeKey.length() == 0))
/*     */                       {
/* 422 */                         mergeKey = colNames[0];
/*     */                       }
/* 424 */                       int flags = 4;
/* 425 */                       if (argIndex < args.length)
/*     */                       {
/* 427 */                         String mergeArgsStr = ScriptUtils.getDisplayString(args[(argIndex++)], context);
/* 428 */                         if ((mergeArgsStr != null) && (mergeArgsStr.equalsIgnoreCase("replace")))
/*     */                         {
/* 430 */                           flags += 2;
/*     */                         }
/*     */                       }
/* 433 */                       ResultSetMerge rsetMerge = new ResultSetMerge(drsetTarget, newResultSet, flags);
/* 434 */                       rsetMerge.m_colKey = mergeKey;
/* 435 */                       if (result.m_collapseMultiValueColumns != null)
/*     */                       {
/* 437 */                         rsetMerge.m_mergeAppendColumns = result.m_collapseMultiValueColumns;
/*     */                       }
/* 439 */                       else if (result.m_mergeAppendColumns != null)
/*     */                       {
/* 441 */                         rsetMerge.m_mergeAppendColumns = new HashSet();
/* 442 */                         rsetMerge.m_mergeAppendColumns.addAll(result.m_mergeAppendColumns);
/*     */                       }
/*     */ 
/* 445 */                       if ((rsetMerge.m_mergeAppendColumns != null) && (!result.m_usingDefaultMergeAppendFormat))
/*     */                       {
/* 447 */                         rsetMerge.m_mergeAppendSep = result.m_mergeAppendSep;
/* 448 */                         rsetMerge.m_mergeAppendEq = result.m_mergeAppendEq;
/*     */                       }
/*     */ 
/* 451 */                       rsetMerge.merge();
/*     */                     }
/*     */                   }
/*     */                 }
/*     */ 
/*     */               }
/*     */               else
/*     */               {
/* 459 */                 DataResultSet newResultSet = new DataResultSet();
/* 460 */                 newResultSet.init(t);
/* 461 */                 if ((inclusionFilterNameParam[0] != null) || (includeColumnsParam[0] != null))
/*     */                 {
/* 463 */                   newResultSet = applyIncludes(newResultSet, binder, merger, inclusionFilterNameParam[0], includeColumnsParam[0], sArg1, drsetKey);
/*     */                 }
/*     */ 
/* 466 */                 binder.addResultSet(drsetKey, newResultSet);
/*     */               }
/*     */             }
/* 469 */             bResult = true;
/*     */           }
/* 473 */           else if (SystemUtils.m_verbose)
/*     */           {
/* 475 */             Report.debug("idocscript", "Did not retrieve table for dynamicdata resource", null);
/* 476 */             String propDes = null;
/* 477 */             if (isIndexed)
/*     */             {
/* 479 */               int prop = result.getColumnProperties(colName);
/* 480 */               propDes = result.getColumnPropertyDescriptor(prop);
/*     */             }
/* 482 */             String dynamicDataTrace = result.createReportString(false, result.m_sortOptions);
/* 483 */             IdcStringBuilder stringBuilder = new IdcStringBuilder();
/* 484 */             ScriptExtensionUtils.appendScriptFunctionReport(stringBuilder, info, args, context);
/* 485 */             stringBuilder.append(" = ");
/* 486 */             if (propDes != null)
/*     */             {
/* 488 */               stringBuilder.append("{").append(propDes).append("} ");
/*     */             }
/* 490 */             stringBuilder.append(dynamicDataTrace);
/* 491 */             Report.debug("idocscript", stringBuilder.toString(), null);
/*     */           }
/*     */ 
/*     */         }
/* 497 */         else if (SystemUtils.m_verbose)
/*     */         {
/* 499 */           Report.debug("idocscript", "Did not retrieve table for dynamicdata resource", null);
/* 500 */           String dynamicDataTrace = "(no dynamicdata)";
/* 501 */           IdcStringBuilder stringBuilder = new IdcStringBuilder();
/* 502 */           ScriptExtensionUtils.appendScriptFunctionReport(stringBuilder, info, args, context);
/* 503 */           stringBuilder.append(" = ");
/* 504 */           stringBuilder.append(dynamicDataTrace);
/* 505 */           Report.debug("idocscript", stringBuilder.toString(), null);
/* 506 */         }break;
/*     */       case 10:
/* 512 */         if (sArg1.length() > 0)
/*     */         {
/* 514 */           DynamicData result = merger.getDynamicDataResource(sArg1, null);
/* 515 */           if ((result != null) && (result.m_mergedTable != null) && (result.m_mergedTable.m_colNames != null))
/*     */           {
/* 517 */             oResult = StringUtils.createStringFromArray(result.m_mergedTable.m_colNames);
/*     */           }
/*     */         }
/* 519 */         break;
/*     */       case 11:
/* 532 */         if ((sArg1.length() > 0) && (sArg2.length() > 0))
/*     */         {
/* 534 */           DynamicData result = merger.getDynamicDataResource(sArg1, null);
/* 535 */           DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/* 536 */           DataResultSet drsetTarget = ResultSetUtils.getMutableResultSet(binder, sArg2, true, false);
/* 537 */           boolean hadSortOptions = false;
/* 538 */           SortOptions sortOptions = (result != null) ? result.m_sortOptions : null;
/* 539 */           if ((sortOptions != null) && (drsetTarget != null))
/*     */           {
/* 541 */             hadSortOptions = true;
/* 542 */             sortOptions = sortOptions.cloneOptions();
/* 543 */             String[] fieldList = ResultSetUtils.getFieldListAsStringArray(drsetTarget);
/* 544 */             if (sortOptions.computeColumnIndices(fieldList))
/*     */             {
/* 546 */               ResultSetTreeSort treeSort = new ResultSetTreeSort(drsetTarget);
/* 547 */               treeSort.setSortOptions(sortOptions);
/* 548 */               treeSort.sort();
/* 549 */               bResult = true;
/*     */             }
/*     */           }
/* 552 */           if ((!bResult) && (((SystemUtils.m_verbose) || (hadSortOptions))))
/*     */           {
/* 554 */             Report.debug("idocscript", new StringBuilder().append("Did not sort dynamicdata table (targest exists=").append(drsetTarget != null).append(")").toString(), null);
/*     */ 
/* 557 */             String dynamicDataTrace = (result != null) ? result.createReportString(false, sortOptions) : "(no dynamicdata)";
/*     */ 
/* 559 */             Report.debug("idocscript", new StringBuilder().append(info.m_key).append("(").append(sArg1).append(",").append(sArg2).append(") = ").append(dynamicDataTrace).toString(), null);
/*     */           }
/*     */         }
/* 562 */         break;
/*     */       case 12:
/* 578 */         if (sArg2.length() == 0)
/*     */         {
/* 580 */           Report.trace("idocscript", new StringBuilder().append("ddIncludePreserveValues() No dynamicdata table specified for include ").append(sArg1).toString(), null);
/*     */         }
/* 584 */         else if (sArg1.length() != 0)
/*     */         {
/* 589 */           DataBinder binder = ScriptExtensionUtils.getBinder(context, msg);
/* 590 */           DynamicData result = merger.getDynamicDataResource(sArg2, null);
/* 591 */           if ((result == null) || (result.m_mergedTable == null) || (result.m_mergedTable.m_colNames == null))
/*     */           {
/* 593 */             Report.trace("idocscript", new StringBuilder().append("ddIncludePreserveValues() Dynamicdata table not specified for include ").append(sArg1).append(" and dynamicdata section ").append(sArg2).toString(), null);
/*     */           }
/*     */           else
/*     */           {
/* 598 */             String[] list = result.m_mergedTable.m_colNames;
/* 599 */             IdcCharArrayWriter writer = merger.getTemporaryWriter();
/*     */             try
/*     */             {
/* 602 */               DataTransformationUtils.evaluteIncludeProtectValues(sArg1, list, binder, merger, writer);
/*     */             }
/*     */             catch (IOException e)
/*     */             {
/*     */             }
/*     */             finally
/*     */             {
/* 610 */               merger.releaseTemporaryWriter(writer);
/*     */             }
/*     */ 
/* 615 */             oResult = writer;
/*     */           }
/* 616 */         }break;
/*     */       default:
/* 620 */         return false;
/*     */       }
/*     */     }
/*     */     catch (ParseSyntaxException e)
/*     */     {
/* 625 */       Report.trace("system", null, e);
/* 626 */       throw new ServiceException(e);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 630 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 633 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 637 */     return true;
/*     */   }
/*     */ 
/*     */   public DataResultSet applyIncludes(DataResultSet drset, DataBinder binder, DynamicHtmlMerger merger, String filterInclude, List includeColumnsList, String tableSet, String idocSet)
/*     */     throws DataException, ServiceException
/*     */   {
/* 644 */     String[] includeColumns = null;
/* 645 */     if (includeColumnsList != null)
/*     */     {
/* 647 */       includeColumns = StringUtils.convertListToArray(includeColumnsList);
/*     */     }
/*     */     try
/*     */     {
/* 651 */       return DataTransformationUtils.applyIncludes(drset, binder, merger, filterInclude, includeColumns, tableSet, idocSet);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 655 */       Report.trace("system", null, e);
/* 656 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/* 667 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 672 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.DynamicDataScriptExtensions
 * JD-Core Version:    0.5.4
 */