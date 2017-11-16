/*      */ package intradoc.server.script;
/*      */ 
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptExtensionsAdaptor;
/*      */ import intradoc.common.ScriptInfo;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.MutableResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleResultSetFilter;
/*      */ import intradoc.resource.DataTransformationUtils;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.StringTokenizer;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ResultSetScriptExtensions extends ScriptExtensionsAdaptor
/*      */ {
/*      */   public ResultSetScriptExtensions()
/*      */   {
/*   37 */     this.m_variableTable = new String[0];
/*      */ 
/*   41 */     this.m_variableDefinitionTable = new int[0][];
/*      */ 
/*   45 */     this.m_functionTable = new String[] { "rsFirst", "rsNext", "rsSetRow", "rsRename", "rsFindRowPrimary", "rsNumFields", "rsFieldByIndex", "rsNumRows", "rsIsRowPresent", "rsMakeFromList", "rsMakeFromString", "rsExists", "rsSort", "rsSortTree", "rsFieldExists", "rsCreateResultSet", "rsInsertNewRow", "rsAppendNewRow", "rsDeleteRow", "rsAddRowCountColumn", "rsCopyFiltered", "rsAppendRowValues", "rsMerge", "rsMergeReplaceOnly", "rsMergeDelete", "rsAppend", "rsRenameField", "rsAddFields", "rsRemove", "rsGetFromSharedObjects", "rsFindNextRow", "rsFillField", "rsMergeFields", "rsCreateReference", "rsLast", "rsAppendRowValuesWithKeys", "rsCloneRowAndAppend", "rsLoopInclude", "rsLoopSingleRowInclude", "rsAddFieldsWithDefaults", "rsValueByIndex", "rsGetResultSetDefinition", "rsCurrentRow", "rsGetFieldIndex" };
/*      */ 
/*  100 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 1 }, { 1, 1, 0, -1, 1 }, { 2, 2, 0, 1, 1 }, { 3, 2, 0, 0, 1 }, { 4, 2, 0, 0, 1 }, { 5, 1, 0, -1, 2 }, { 6, 2, 0, 1, 0 }, { 7, 1, 0, -1, 2 }, { 8, 1, 0, -1, 1 }, { 9, -1, 0, 0, 1 }, { 10, -1, 0, 0, -1 }, { 11, 1, 0, -1, 1 }, { 12, -1, -1, -1, 0 }, { 13, -1, -1, -1, 0 }, { 14, 2, 0, 0, 1 }, { 15, 2, 0, 0, 0 }, { 16, 1, 0, -1, 0 }, { 17, 1, 0, -1, 0 }, { 18, 1, 0, -1, 0 }, { 19, 2, 0, 0, 0 }, { 20, 4, 0, 0, 0 }, { 21, 2, 0, 0, 0 }, { 22, 2, 0, 0, 0 }, { 23, 3, 0, 0, 0 }, { 24, 3, 0, 0, 0 }, { 25, 2, 0, 0, 0 }, { 26, 3, 0, 0, 0 }, { 27, 2, 0, 0, 0 }, { 28, 1, 0, -1, 1 }, { 29, -1, 0, 0, 1 }, { 30, 3, 0, 0, 1 }, { 31, 2, 0, 0, 2 }, { 32, 2, 0, 0, 0 }, { 33, 2, 0, 0, 1 }, { 34, 1, 0, -1, 1 }, { 35, 3, 0, 0, 0 }, { 36, 1, 0, 0, 0 }, { 37, 2, 0, 0, 0 }, { 38, 2, 0, 0, 0 }, { 39, 2, 0, 0, 0 }, { 40, 2, 0, 1, 0 }, { 41, -1, 0, 0, 0 }, { 42, 1, 0, -1, 2 }, { 43, 2, 0, 0, 2 } };
/*      */   }
/*      */ 
/*      */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*      */   {
/*  153 */     int[] config = (int[])(int[])info.m_entry;
/*  154 */     String function = info.m_key;
/*  155 */     DataBinder binder = ScriptExtensionUtils.getBinder(context);
/*  156 */     PageMerger pageMerger = ScriptExtensionUtils.getPageMerger(context);
/*      */ 
/*  158 */     if ((config == null) || (binder == null) || (pageMerger == null))
/*      */     {
/*  160 */       return false;
/*      */     }
/*      */ 
/*  163 */     int nargs = args.length - 1;
/*  164 */     int minParams = config[1];
/*  165 */     if ((minParams >= 0) && (nargs < minParams))
/*      */     {
/*  167 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + minParams);
/*      */ 
/*  169 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/*  175 */     String sArg1 = null;
/*  176 */     String sArg2 = null;
/*      */ 
/*  178 */     long lArg2 = 0L;
/*  179 */     if ((nargs > 0) && 
/*  181 */       (config[2] == 0))
/*      */     {
/*  183 */       sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*      */     }
/*      */ 
/*  191 */     if (nargs > 1)
/*      */     {
/*  193 */       if (config[3] == 0)
/*      */       {
/*  195 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*      */       }
/*  197 */       else if (config[3] == 1)
/*      */       {
/*  199 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  204 */     boolean bResult = false;
/*  205 */     int iResult = 0;
/*  206 */     double dResult = 0.0D;
/*  207 */     Object oResult = null;
/*      */ 
/*  209 */     switch (config[0])
/*      */     {
/*      */     case 0:
/*      */     case 1:
/*      */     case 2:
/*      */     case 3:
/*      */     case 4:
/*      */     case 5:
/*      */     case 6:
/*      */     case 7:
/*      */     case 8:
/*  221 */       checkNonEmpty(sArg1);
/*  222 */       ResultSet rset = binder.getResultSet(sArg1);
/*  223 */       if (rset == null)
/*      */       {
/*  225 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/*  227 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  230 */       switch (config[0])
/*      */       {
/*      */       case 0:
/*  233 */         bResult = rset.first();
/*  234 */         break;
/*      */       case 1:
/*  237 */         bResult = rset.next();
/*  238 */         break;
/*      */       case 2:
/*  241 */         if (rset instanceof DataResultSet)
/*      */         {
/*  243 */           DataResultSet drset = (DataResultSet)rset;
/*  244 */           if ((lArg2 < 0L) || (lArg2 >= drset.getNumRows()))
/*      */           {
/*  246 */             bResult = false;
/*      */           }
/*      */           else
/*      */           {
/*  250 */             bResult = true;
/*  251 */             drset.setCurrentRow((int)lArg2);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  256 */           throw new IllegalArgumentException("!csUnableToSetRowInResultSet");
/*      */         }
/*      */       case 3:
/*  261 */         bResult = false;
/*  262 */         checkNonEmpty(sArg1);
/*  263 */         checkNonEmpty(sArg2);
/*      */ 
/*  265 */         ResultSet newRset = binder.getResultSet(sArg2);
/*  266 */         if (newRset == null)
/*      */         {
/*  271 */           newRset = binder.removeResultSet(sArg1);
/*  272 */           if (newRset != null)
/*      */           {
/*  277 */             binder.addResultSet(sArg2, newRset);
/*      */ 
/*  279 */             bResult = true;
/*      */           }
/*      */         }
/*  281 */         break;
/*      */       case 4:
/*  287 */         checkNonEmpty(sArg1);
/*  288 */         DataResultSet drset = (DataResultSet)rset;
/*      */ 
/*  291 */         Vector row = drset.findRow(0, sArg2);
/*      */ 
/*  294 */         if (row != null)
/*      */         {
/*  296 */           bResult = true; } break;
/*      */       case 5:
/*  301 */         iResult = rset.getNumFields();
/*  302 */         break;
/*      */       case 6:
/*  305 */         FieldInfo fi = new FieldInfo();
/*  306 */         rset.getIndexFieldInfo((int)lArg2, fi);
/*  307 */         oResult = fi.m_name;
/*  308 */         break;
/*      */       case 7:
/*      */         MutableResultSet drset;
/*  313 */         if (rset instanceof MutableResultSet)
/*      */         {
/*  315 */           drset = (MutableResultSet)rset;
/*      */         }
/*      */         else
/*      */         {
/*  319 */           throw new IllegalArgumentException("!csUnableToSetRowInResultSet");
/*      */         }
/*      */         MutableResultSet drset;
/*  321 */         iResult = drset.getNumRows();
/*  322 */         break;
/*      */       case 8:
/*  325 */         bResult = rset.isRowPresent();
/*      */       }
/*      */ 
/*  328 */       break;
/*      */     case 9:
/*  333 */       if ((nargs < 2) || (nargs > 3))
/*      */       {
/*  335 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*      */       }
/*      */ 
/*  340 */       String rowName = "row";
/*  341 */       if (nargs == 3)
/*      */       {
/*  343 */         rowName = (String)args[2];
/*      */       }
/*      */ 
/*  347 */       Vector list = null;
/*      */       try
/*      */       {
/*  350 */         list = pageMerger.getScriptOptionList(sArg2, null, null);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  354 */         Report.trace("system", null, e);
/*  355 */         throw new IllegalArgumentException(e.getMessage());
/*      */       }
/*      */ 
/*  359 */       if (list != null)
/*      */       {
/*  361 */         DataResultSet newRset = ResultSetUtils.createResultSetFromList(sArg1, list, rowName);
/*  362 */         newRset.setDateFormat(binder.getLocaleDateFormat());
/*  363 */         binder.addResultSet(sArg1, newRset);
/*  364 */         bResult = true;
/*  365 */       }break;
/*      */     case 10:
/*  371 */       if ((nargs < 2) || (nargs > 3))
/*      */       {
/*  373 */         throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*      */       }
/*      */ 
/*  378 */       String rowName = "row";
/*  379 */       if (nargs == 3)
/*      */       {
/*  381 */         rowName = (String)args[2];
/*      */       }
/*  383 */       DataResultSet newRset = new DataResultSet(new String[] { rowName });
/*  384 */       newRset.setDateFormat(binder.getLocaleDateFormat());
/*      */ 
/*  387 */       StringTokenizer st = new StringTokenizer(sArg2, ",");
/*  388 */       while (st.hasMoreTokens())
/*      */       {
/*  390 */         Vector row = new IdcVector();
/*  391 */         row.addElement(st.nextToken());
/*  392 */         newRset.addRow(row);
/*      */       }
/*      */ 
/*  395 */       binder.addResultSet(sArg1, newRset);
/*  396 */       break;
/*      */     case 11:
/*  400 */       ResultSet rset = binder.getResultSet(sArg1);
/*  401 */       bResult = (rset != null) && (!rset.isEmpty());
/*  402 */       break;
/*      */     case 12:
/*  411 */       doResultSetSort(function, binder, args, false);
/*  412 */       break;
/*      */     case 13:
/*  424 */       doResultSetSort(function, binder, args, true);
/*  425 */       break;
/*      */     case 14:
/*  429 */       bResult = false;
/*  430 */       ResultSet rset = binder.getResultSet(sArg1);
/*  431 */       if (rset != null)
/*      */       {
/*  433 */         FieldInfo fi = new FieldInfo();
/*  434 */         if (rset.getFieldInfo(sArg2, fi))
/*      */         {
/*  436 */           bResult = true;
/*      */         }
/*      */       }
/*  438 */       break;
/*      */     case 15:
/*  443 */       checkNonEmpty(sArg1);
/*  444 */       checkNonEmpty(sArg2);
/*  445 */       DataResultSet drset = null;
/*  446 */       if (sArg2.equalsIgnoreCase("#localdata"))
/*      */       {
/*  448 */         drset = new DataResultSet(new String[] { "ldKey", "ldValue" });
/*  449 */         Properties localData = binder.getLocalData();
/*  450 */         Enumeration en = localData.keys();
/*  451 */         while (en.hasMoreElements())
/*      */         {
/*  453 */           Object key = en.nextElement();
/*  454 */           if (key instanceof String)
/*      */           {
/*  456 */             String value = localData.getProperty((String)key);
/*  457 */             Vector v = new IdcVector();
/*  458 */             v.addElement(key);
/*  459 */             v.addElement(value);
/*  460 */             drset.addRow(v);
/*      */           }
/*      */         }
/*      */       }
/*  464 */       else if (sArg2.equalsIgnoreCase("#resultsets"))
/*      */       {
/*  466 */         drset = new DataResultSet(new String[] { "rsName" });
/*  467 */         Enumeration en = binder.getResultSetList();
/*  468 */         while (en.hasMoreElements())
/*      */         {
/*  470 */           Vector v = new IdcVector();
/*  471 */           v.addElement(en.nextElement());
/*  472 */           drset.addRow(v);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  477 */         Vector temp = StringUtils.parseArray(sArg2, ',', '^');
/*  478 */         String[] fields = StringUtils.convertListToArray(temp);
/*  479 */         drset = new DataResultSet(fields);
/*      */       }
/*  481 */       drset.setDateFormat(binder.getLocaleDateFormat());
/*  482 */       drset.first();
/*  483 */       binder.addResultSet(sArg1, drset);
/*  484 */       break;
/*      */     case 16:
/*      */     case 17:
/*  489 */       boolean mustBeOnRow = config[0] == 16;
/*  490 */       checkNonEmpty(sArg1);
/*  491 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, mustBeOnRow);
/*  492 */       if (drset != null)
/*      */       {
/*  494 */         Vector tempRow = drset.createEmptyRow();
/*  495 */         if (mustBeOnRow)
/*      */         {
/*  497 */           int curRow = drset.getCurrentRow();
/*  498 */           drset.insertRowAt(tempRow, curRow);
/*      */         }
/*      */         else
/*      */         {
/*  502 */           drset.addRow(tempRow);
/*  503 */           drset.last();
/*      */         }
/*      */       }
/*  505 */       break;
/*      */     case 18:
/*  510 */       checkNonEmpty(sArg1);
/*  511 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, true);
/*  512 */       drset.deleteCurrentRow();
/*  513 */       break;
/*      */     case 19:
/*  517 */       checkNonEmpty(sArg1);
/*  518 */       checkNonEmpty(sArg2);
/*  519 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  520 */       FieldInfo fi = new FieldInfo();
/*      */ 
/*  525 */       Vector vfi = new IdcVector();
/*  526 */       fi.m_name = sArg2;
/*  527 */       vfi.addElement(fi);
/*  528 */       drset.mergeFieldsWithFlags(vfi, 2);
/*      */       try
/*      */       {
/*  531 */         int rowCount = 0;
/*  532 */         int curRow = drset.getCurrentRow();
/*  533 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  535 */           drset.setCurrentValue(fi.m_index, "" + rowCount);
/*  536 */           ++rowCount;
/*      */         }
/*  538 */         drset.setCurrentRow(curRow);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  542 */         Report.trace(null, null, ignore);
/*      */       }
/*  544 */       break;
/*      */     case 20:
/*  548 */       checkNonEmpty(sArg1);
/*  549 */       checkNonEmpty(sArg2);
/*  550 */       String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*  551 */       String sArg4 = ScriptUtils.getDisplayString(args[3], context);
/*  552 */       checkNonEmpty(sArg3);
/*  553 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  554 */       DataResultSet drsetCopy = new DataResultSet();
/*  555 */       drsetCopy.setDateFormat(binder.getLocaleDateFormat());
/*  556 */       SimpleResultSetFilter filter = new SimpleResultSetFilter(sArg4);
/*  557 */       filter.m_isWildcard = true;
/*  558 */       drsetCopy.copyFiltered(drset, sArg3, filter);
/*  559 */       binder.addResultSet(sArg2, drsetCopy);
/*  560 */       break;
/*      */     case 21:
/*  564 */       checkNonEmpty(sArg1);
/*  565 */       checkNonEmpty(sArg2);
/*  566 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*      */ 
/*  569 */       Vector values = StringUtils.parseArray(sArg2, ',', '^');
/*  570 */       int nfields = drset.getNumFields();
/*  571 */       int i = 0;
/*  572 */       int nvalues = values.size();
/*      */       while (true) { if (i >= nvalues)
/*      */           break label4402;
/*  575 */         Vector newRow = new IdcVector(nfields);
/*  576 */         for (int j = 0; j < nfields; ++j)
/*      */         {
/*  578 */           Object elt = null;
/*  579 */           if (i < nvalues)
/*      */           {
/*  581 */             elt = values.elementAt(i);
/*      */           }
/*      */           else
/*      */           {
/*  585 */             elt = "";
/*      */           }
/*  587 */           newRow.addElement(elt);
/*  588 */           ++i;
/*      */         }
/*  590 */         drset.addRow(newRow); }
/*      */ 
/*      */     case 22:
/*      */     case 23:
/*      */     case 24:
/*      */     case 25:
/*  599 */       checkNonEmpty(sArg1);
/*  600 */       checkNonEmpty(sArg2);
/*  601 */       String sArg3 = null;
/*  602 */       boolean specifiesField = config[0] != 25;
/*  603 */       boolean mustHaveField = (config[0] == 23) || (config[0] == 24);
/*  604 */       boolean isAppending = (config[0] == 22) || (config[0] == 25);
/*  605 */       boolean isDeleting = config[0] == 24;
/*  606 */       if (specifiesField)
/*      */       {
/*  608 */         sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*  609 */         if (mustHaveField)
/*      */         {
/*  611 */           checkNonEmpty(sArg3);
/*      */         }
/*      */       }
/*  614 */       DataResultSet drsetTarget = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  615 */       DataResultSet drsetSource = ResultSetUtils.getMutableResultSet(binder, sArg2, false, false);
/*  616 */       if (drsetTarget == null)
/*      */       {
/*  618 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/*  620 */         throw new IllegalArgumentException(msg);
/*      */       }
/*  622 */       if (drsetSource == null)
/*      */       {
/*  624 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg2);
/*      */ 
/*  626 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/*  631 */         if (isDeleting)
/*      */         {
/*  633 */           drsetTarget.mergeDelete(sArg3, drsetSource, false);
/*      */         }
/*      */         else
/*      */         {
/*  637 */           if (isAppending)
/*      */           {
/*  639 */             drsetTarget.mergeFields(drsetSource);
/*      */           }
/*  641 */           drsetTarget.merge(sArg3, drsetSource, !isAppending);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  646 */         throw new IllegalArgumentException(e.getMessage());
/*      */       }
/*      */     case 26:
/*  652 */       checkNonEmpty(sArg1);
/*  653 */       checkNonEmpty(sArg2);
/*  654 */       String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*  655 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*      */ 
/*  659 */       binder.removeResultSet(sArg1);
/*  660 */       drset.renameField(sArg2, sArg3);
/*  661 */       binder.addResultSet(sArg1, drset);
/*  662 */       break;
/*      */     case 27:
/*  666 */       checkNonEmpty(sArg1);
/*      */ 
/*  668 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  669 */       if (drset != null)
/*      */       {
/*  675 */         Vector fields = StringUtils.parseArray(sArg2, ',', '^');
/*  676 */         drset.mergeFieldsWithFlags(fields, 0);
/*  677 */         bResult = true;
/*  678 */       }break;
/*      */     case 28:
/*  682 */       ResultSet removedSet = binder.removeResultSet(sArg1);
/*  683 */       if (removedSet != null)
/*      */       {
/*  685 */         bResult = true;
/*      */       }
/*      */       else
/*      */       {
/*  689 */         bResult = false;
/*      */       }
/*  691 */       break;
/*      */     case 29:
/*  702 */       DataResultSet safeSet = SharedObjects.getTable("SafeSharedObjectsTables");
/*  703 */       boolean okay = false;
/*  704 */       for (safeSet.first(); safeSet.isRowPresent(); safeSet.next())
/*      */       {
/*  706 */         String name = safeSet.getStringValue(0);
/*  707 */         if (!name.equals(sArg1))
/*      */           continue;
/*  709 */         okay = true;
/*  710 */         break;
/*      */       }
/*      */ 
/*  713 */       bResult = false;
/*  714 */       if (!okay)
/*      */       {
/*  716 */         Report.trace("idocscript", "request for unsafe table " + sArg1, null);
/*      */       }
/*      */       else
/*      */       {
/*  720 */         DataResultSet set = SharedObjects.getTable(sArg1);
/*  721 */         if (set != null)
/*      */         {
/*  723 */           if (sArg2 == null)
/*      */           {
/*  725 */             sArg2 = sArg1;
/*      */           }
/*  727 */           binder.addResultSet(sArg2, set);
/*  728 */           bResult = true;
/*      */         }
/*      */       }
/*  731 */       break;
/*      */     case 30:
/*  754 */       checkNonEmpty(sArg1);
/*  755 */       ResultSet rset = binder.getResultSet(sArg1);
/*  756 */       if (null == rset)
/*      */       {
/*  758 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/*  760 */         throw new IllegalArgumentException(msg);
/*      */       }
/*  762 */       DataResultSet drset = (DataResultSet)rset;
/*  763 */       FieldInfo fieldInfo = new FieldInfo();
/*  764 */       if (!drset.getFieldInfo(sArg2, fieldInfo))
/*      */       {
/*  766 */         String msg = LocaleUtils.encodeMessage("csFieldNotFound", null, sArg2);
/*      */ 
/*  768 */         throw new IllegalArgumentException(msg);
/*      */       }
/*  770 */       int fieldIndex = fieldInfo.m_index;
/*  771 */       String value = ScriptUtils.getDisplayString(args[2], context);
/*  772 */       checkNonEmpty(value);
/*      */ 
/*  774 */       int offset = 0;
/*  775 */       if (nargs > 3)
/*      */       {
/*  777 */         offset = (int)ScriptUtils.getLongVal(args[3], context);
/*      */       }
/*  779 */       boolean direction = true;
/*  780 */       if (nargs > 4)
/*      */       {
/*  783 */         String directionStr = ScriptUtils.getDisplayString(args[4], context);
/*  784 */         direction = !directionStr.equalsIgnoreCase("desc");
/*      */       }
/*      */ 
/*  787 */       int flags = 2;
/*  788 */       if (!direction)
/*      */       {
/*  790 */         flags |= 1;
/*      */       }
/*      */ 
/*  793 */       int startRow = drset.getCurrentRow() + offset;
/*  794 */       List row = drset.findRow(fieldIndex, value, startRow, flags);
/*  795 */       bResult = row != null;
/*  796 */       break;
/*      */     case 31:
/*  809 */       checkNonEmpty(sArg1);
/*  810 */       checkNonEmpty(sArg2);
/*      */ 
/*  812 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  813 */       if (null == drset)
/*      */       {
/*  815 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/*  817 */         throw new IllegalArgumentException(msg);
/*      */       }
/*  819 */       FieldInfo fieldInfo = new FieldInfo();
/*      */ 
/*  824 */       fieldInfo.m_name = sArg2;
/*  825 */       List fieldInfos = new IdcVector();
/*  826 */       fieldInfos.add(fieldInfo);
/*  827 */       drset.mergeFieldsWithFlags(fieldInfos, 2);
/*  828 */       int fieldIndex = fieldInfo.m_index;
/*      */       String value;
/*      */       String value;
/*  830 */       if (nargs > 2)
/*      */       {
/*  832 */         value = ScriptUtils.getDisplayString(args[2], context);
/*      */       }
/*      */       else
/*      */       {
/*  836 */         value = "";
/*      */       }
/*  838 */       drset.fillField(fieldIndex, value);
/*  839 */       iResult = fieldIndex;
/*  840 */       break;
/*      */     case 32:
/*  851 */       checkNonEmpty(sArg1);
/*  852 */       checkNonEmpty(sArg2);
/*      */ 
/*  854 */       DataResultSet target = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*      */ 
/*  856 */       ResultSet source = binder.getResultSet(sArg2);
/*  857 */       target.mergeFields(source);
/*  858 */       break;
/*      */     case 33:
/*  870 */       bResult = false;
/*  871 */       checkNonEmpty(sArg1);
/*  872 */       checkNonEmpty(sArg2);
/*      */ 
/*  874 */       ResultSet source = binder.getResultSet(sArg1);
/*  875 */       if (source == null)
/*      */       {
/*  877 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*  878 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  881 */       ResultSet dest = binder.getResultSet(sArg2);
/*  882 */       if (dest == null)
/*      */       {
/*  887 */         binder.addResultSet(sArg2, source);
/*  888 */         bResult = true;
/*      */       }
/*  890 */       break;
/*      */     case 34:
/*  900 */       checkNonEmpty(sArg1);
/*  901 */       ResultSet rset = binder.getResultSet(sArg1);
/*  902 */       if (rset == null)
/*      */       {
/*  904 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*  905 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  908 */       if (rset instanceof DataResultSet)
/*      */       {
/*  910 */         bResult = ((DataResultSet)rset).last();
/*      */       }
/*      */       else
/*      */       {
/*  914 */         throw new IllegalArgumentException("!csUnableToSetRowInResultSet");
/*      */       }
/*      */     case 35:
/*  921 */       String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/*      */ 
/*  923 */       checkNonEmpty(sArg1);
/*  924 */       checkNonEmpty(sArg2);
/*  925 */       checkNonEmpty(sArg3);
/*      */ 
/*  927 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, false);
/*  928 */       if (drset == null)
/*      */       {
/*  930 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*  931 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  934 */       List fieldsList = StringUtils.makeListFromSequence(sArg2, ',', '^', 0);
/*      */ 
/*  936 */       List valuesList = StringUtils.makeListFromSequence(sArg3, ',', '^', 0);
/*      */ 
/*  939 */       if (fieldsList.size() != valuesList.size())
/*      */       {
/*  941 */         String msg = LocaleUtils.encodeMessage("csArgumentListsMustBeTheSameSize", null, sArg2, sArg3);
/*  942 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/*  945 */       Vector v = drset.createEmptyRow();
/*  946 */       drset.addRow(v);
/*  947 */       drset.last();
/*      */       Iterator valuesIt;
/*      */       Iterator fieldsIt;
/*      */       try {
/*  951 */         valuesIt = valuesList.iterator();
/*  952 */         for (fieldsIt = fieldsList.iterator(); fieldsIt.hasNext(); )
/*      */         {
/*  959 */           String fieldName = (String)fieldsIt.next();
/*  960 */           String value = (String)valuesIt.next();
/*  961 */           Vector fieldInfos = ResultSetUtils.createFieldInfo(new String[] { fieldName }, 0);
/*  962 */           drset.mergeFieldsWithFlags(fieldInfos, 2);
/*  963 */           FieldInfo fieldInfo = (FieldInfo)fieldInfos.get(0);
/*  964 */           drset.setCurrentValue(fieldInfo.m_index, value);
/*      */         }
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  969 */         Report.trace(null, null, ignore);
/*      */       }
/*      */ 
/*  972 */       break;
/*      */     case 36:
/*  989 */       checkNonEmpty(sArg1);
/*      */ 
/*  991 */       DataResultSet srcDrset = ResultSetUtils.getMutableResultSet(binder, sArg1, true, true);
/*      */ 
/*  993 */       if (srcDrset == null)
/*      */       {
/*  995 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*  996 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */ 
/* 1001 */       boolean complexAdd = false;
/*      */       DataResultSet destDrset;
/* 1002 */       if (nargs > 1)
/*      */       {
/* 1004 */         checkNonEmpty(sArg2);
/* 1005 */         DataResultSet destDrset = ResultSetUtils.getMutableResultSet(binder, sArg2, true, false);
/*      */ 
/* 1007 */         if (destDrset == null)
/*      */         {
/* 1009 */           String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg2);
/* 1010 */           throw new IllegalArgumentException(msg);
/*      */         }
/*      */ 
/* 1013 */         int numFields = destDrset.getNumFields();
/* 1014 */         destDrset.mergeFields(srcDrset);
/* 1015 */         complexAdd = numFields != destDrset.getNumFields();
/*      */       }
/*      */       else
/*      */       {
/* 1019 */         destDrset = srcDrset;
/*      */       }
/*      */ 
/* 1024 */       List currentRowValues = null;
/* 1025 */       if (complexAdd)
/*      */       {
/* 1027 */         Map m = srcDrset.getCurrentRowMap();
/* 1028 */         MapParameters mp = new MapParameters(m);
/*      */         try
/*      */         {
/* 1031 */           currentRowValues = destDrset.createRowAsList(mp);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/* 1035 */           throw new IllegalArgumentException(e.getMessage());
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1040 */         currentRowValues = srcDrset.getCurrentRowAsList();
/*      */       }
/* 1042 */       destDrset.createAndAddRowInitializedWithList(currentRowValues);
/* 1043 */       destDrset.last();
/*      */ 
/* 1045 */       break;
/*      */     case 37:
/*      */     case 38:
/* 1058 */       if (sArg1.length() == 0)
/*      */       {
/* 1060 */         Report.trace("idocscript", info.m_key + "() was not supplied a non empty result set name", null);
/*      */       }
/*      */       else {
/* 1063 */         boolean singleRow = config[0] == 38;
/* 1064 */         IdcCharArrayWriter writer = pageMerger.getTemporaryWriter();
/*      */         try
/*      */         {
/* 1067 */           evaluateMakeActiveInclude(sArg1, binder, pageMerger, writer, sArg2, singleRow);
/*      */         }
/*      */         finally
/*      */         {
/* 1071 */           pageMerger.releaseTemporaryWriter(writer);
/*      */         }
/*      */ 
/* 1075 */         oResult = writer;
/* 1076 */       }break;
/*      */     case 39:
/* 1091 */       checkNonEmpty(sArg1);
/* 1092 */       checkNonEmpty(sArg2);
/* 1093 */       String[] fieldList = StringUtils.makeStringArrayFromSequence(sArg2);
/*      */ 
/* 1096 */       DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, sArg1, false, false);
/* 1097 */       if (drset == null)
/*      */       {
/* 1099 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/* 1101 */         throw new IllegalArgumentException(msg);
/*      */       }
/* 1103 */       int oldNumFields = drset.getNumFields();
/*      */ 
/* 1106 */       DataResultSet mergeDrset = new DataResultSet(fieldList);
/* 1107 */       drset.mergeFields(mergeDrset);
/*      */ 
/* 1110 */       if (nargs > 2)
/*      */       {
/* 1119 */         int numNewFields = drset.getNumFields() - oldNumFields;
/* 1120 */         int[] fieldIndexes = new int[numNewFields];
/* 1121 */         String[] defVals = new String[numNewFields];
/* 1122 */         Arrays.fill(defVals, "");
/*      */ 
/* 1125 */         String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/* 1126 */         String[] tmpDefVals = StringUtils.makeStringArrayFromSequence(sArg3);
/* 1127 */         String[] tmpFullDefVals = new String[fieldList.length];
/*      */ 
/* 1131 */         int copyLength = tmpFullDefVals.length;
/* 1132 */         if (tmpDefVals.length != copyLength)
/*      */         {
/* 1134 */           Report.trace("idocscript", "default values list not the same length as field list", null);
/*      */ 
/* 1137 */           if (tmpDefVals.length < copyLength)
/*      */           {
/* 1139 */             copyLength = tmpDefVals.length;
/*      */           }
/*      */         }
/* 1142 */         System.arraycopy(tmpDefVals, 0, tmpFullDefVals, 0, copyLength);
/*      */         int i;
/* 1145 */         int i = 0;
/* 1146 */         for (int a = 0; a < fieldList.length; ++a)
/*      */         {
/* 1148 */           FieldInfo fi = new FieldInfo();
/* 1149 */           drset.getFieldInfo(fieldList[a], fi);
/*      */ 
/* 1152 */           if (fi.m_index < oldNumFields)
/*      */             continue;
/* 1154 */           fieldIndexes[i] = fi.m_index;
/* 1155 */           defVals[i] = tmpFullDefVals[a];
/* 1156 */           ++i;
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/* 1163 */           drset.first();
/* 1164 */           int numRows = drset.getNumRows();
/* 1165 */           for (int a = 0; a < numRows; ++a)
/*      */           {
/* 1167 */             for (int b = 0; b < fieldIndexes.length; ++b)
/*      */             {
/* 1169 */               drset.setCurrentValue(fieldIndexes[b], defVals[b]);
/*      */             }
/* 1171 */             drset.next();
/*      */           }
/*      */         }
/*      */         catch (DataException de)
/*      */         {
/* 1176 */           IllegalArgumentException e = new IllegalArgumentException(de.getMessage());
/* 1177 */           e.initCause(de);
/* 1178 */           throw e;
/*      */         }
/*      */       }
/* 1180 */       break;
/*      */     case 40:
/* 1186 */       checkNonEmpty(sArg1);
/* 1187 */       ResultSet rset = binder.getResultSet(sArg1);
/* 1188 */       if (rset == null)
/*      */       {
/* 1190 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/* 1192 */         throw new IllegalArgumentException(msg);
/*      */       }
/* 1194 */       oResult = rset.getStringValue((int)lArg2);
/* 1195 */       break;
/*      */     case 41:
/* 1208 */       ResultSet sourceSet = binder.getResultSet(sArg1);
/* 1209 */       DataResultSet defSet = new DataResultSet();
/* 1210 */       List fieldList = new ArrayList();
/* 1211 */       fieldList.add("FieldName");
/* 1212 */       fieldList.add("FieldType");
/* 1213 */       fieldList.add("MaxLength");
/* 1214 */       fieldList.add("Flags");
/* 1215 */       defSet.mergeFieldsWithFlags(fieldList, 0);
/* 1216 */       if (sArg2 == null)
/*      */       {
/* 1218 */         sArg2 = sArg1 + "_definition";
/*      */       }
/* 1220 */       int length = sourceSet.getNumFields();
/* 1221 */       FieldInfo finfo = new FieldInfo();
/* 1222 */       for (int i = 0; i < length; ++i)
/*      */       {
/* 1224 */         sourceSet.getIndexFieldInfo(i, finfo);
/* 1225 */         Vector row = new Vector();
/* 1226 */         row.addElement(finfo.m_name);
/* 1227 */         row.addElement(FieldInfo.FIELD_NAMES[finfo.m_type]);
/* 1228 */         row.addElement("" + finfo.m_maxLen);
/* 1229 */         String flags = "";
/* 1230 */         if (finfo.m_isFixedLen)
/*      */         {
/* 1232 */           flags = "fixed";
/*      */         }
/* 1234 */         row.addElement(flags);
/* 1235 */         defSet.addRow(row);
/*      */       }
/* 1237 */       binder.addResultSet(sArg2, defSet);
/* 1238 */       break;
/*      */     case 42:
/* 1244 */       checkNonEmpty(sArg1);
/* 1245 */       ResultSet rset = binder.getResultSet(sArg1);
/* 1246 */       if (rset == null)
/*      */       {
/* 1248 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/* 1250 */         throw new IllegalArgumentException(msg);
/*      */       }
/*      */       MutableResultSet drset;
/* 1254 */       if (rset instanceof MutableResultSet)
/*      */       {
/* 1256 */         drset = (MutableResultSet)rset;
/*      */       }
/*      */       else
/*      */       {
/* 1260 */         throw new IllegalArgumentException("!csUnableToSetRowInResultSet");
/*      */       }
/*      */       MutableResultSet drset;
/* 1262 */       iResult = drset.getCurrentRow();
/* 1263 */       break;
/*      */     case 43:
/* 1268 */       checkNonEmpty(sArg1);
/* 1269 */       ResultSet rset = binder.getResultSet(sArg1);
/* 1270 */       if (rset == null)
/*      */       {
/* 1272 */         String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, sArg1);
/*      */ 
/* 1274 */         throw new IllegalArgumentException(msg);
/*      */       }
/* 1276 */       FieldInfo fi = new FieldInfo();
/* 1277 */       rset.getFieldInfo(sArg2, fi);
/* 1278 */       iResult = fi.m_index;
/* 1279 */       break;
/*      */     default:
/* 1282 */       return false;
/*      */     }
/*      */ 
/* 1286 */     label4402: args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*      */ 
/* 1289 */     return true;
/*      */   }
/*      */ 
/*      */   public void doResultSetSort(String function, DataBinder binder, Object[] args, boolean isTreeSort)
/*      */   {
/* 1295 */     int nargs = args.length - 1;
/* 1296 */     int minArgs = (isTreeSort) ? 4 : 2;
/* 1297 */     if (nargs < minArgs)
/*      */     {
/* 1299 */       throw new IllegalArgumentException(LocaleUtils.encodeMessage("csScriptEvalFunctionRequiresNumberArgs", null, function));
/*      */     }
/*      */ 
/* 1302 */     String rsName = (String)args[0];
/* 1303 */     int curIndex = 1;
/* 1304 */     String itemIdCol = null;
/* 1305 */     String parentIdCol = null;
/* 1306 */     String nestLevelCol = null;
/* 1307 */     String sortCol = null;
/* 1308 */     String sortType = "";
/* 1309 */     String sortOrder = "asc";
/*      */ 
/* 1311 */     checkNonEmpty(rsName);
/*      */ 
/* 1313 */     if (isTreeSort)
/*      */     {
/* 1315 */       itemIdCol = (String)args[1];
/* 1316 */       parentIdCol = (String)args[2];
/* 1317 */       nestLevelCol = (String)args[3];
/*      */ 
/* 1319 */       if (nargs > 4)
/*      */       {
/* 1321 */         sortCol = (String)args[4];
/*      */       }
/*      */       else
/*      */       {
/* 1325 */         sortCol = itemIdCol;
/* 1326 */         sortType = "int";
/*      */       }
/* 1328 */       curIndex = 5;
/*      */     }
/*      */     else
/*      */     {
/* 1332 */       sortCol = (String)args[1];
/* 1333 */       curIndex = 2;
/*      */     }
/* 1335 */     if (nargs > curIndex)
/*      */     {
/* 1337 */       sortType = (String)args[(curIndex++)];
/*      */     }
/* 1339 */     if (nargs > curIndex)
/*      */     {
/* 1341 */       sortOrder = (String)args[(curIndex++)];
/*      */     }
/*      */ 
/* 1344 */     DataResultSet drset = ResultSetUtils.getMutableResultSet(binder, rsName, false, false);
/* 1345 */     if (drset == null)
/*      */     {
/* 1347 */       String msg = LocaleUtils.encodeMessage("csResultSetNotFound", null, rsName);
/*      */ 
/* 1349 */       throw new IllegalArgumentException(msg);
/*      */     }
/*      */ 
/* 1352 */     if (drset.isEmpty())
/*      */       return;
/*      */     try
/*      */     {
/* 1356 */       int itemIdColIndex = -1;
/* 1357 */       int parentIdColIndex = -1;
/* 1358 */       if (isTreeSort)
/*      */       {
/* 1360 */         itemIdColIndex = ResultSetUtils.getIndexMustExist(drset, itemIdCol);
/* 1361 */         parentIdColIndex = ResultSetUtils.getIndexMustExist(drset, parentIdCol);
/*      */       }
/* 1363 */       int sortColIndex = ResultSetUtils.getIndexMustExist(drset, sortCol);
/*      */ 
/* 1366 */       ResultSetTreeSort treeSort = new ResultSetTreeSort(drset, sortColIndex, isTreeSort);
/* 1367 */       treeSort.determineFieldType(sortType);
/* 1368 */       treeSort.determineIsAscending(sortOrder);
/* 1369 */       if (isTreeSort)
/*      */       {
/* 1371 */         treeSort.findOrAppendNestLevelField(nestLevelCol);
/* 1372 */         treeSort.m_itemIdColIndex = itemIdColIndex;
/* 1373 */         treeSort.m_parentIdColIndex = parentIdColIndex;
/*      */       }
/* 1375 */       treeSort.sort();
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/* 1379 */       IllegalArgumentException e = new IllegalArgumentException(de.getMessage());
/* 1380 */       e.initCause(de);
/* 1381 */       throw e;
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean evaluateMakeActiveInclude(String rsName, DataBinder binder, DynamicHtmlMerger merger, Writer writer, String include, boolean singleRow)
/*      */   {
/* 1389 */     boolean retVal = false;
/*      */     try
/*      */     {
/* 1392 */       DataTransformationUtils.evaluateMakeActiveInclude(rsName, binder, merger, writer, include, singleRow);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1397 */       Report.trace("system", null, e);
/*      */     }
/* 1399 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1404 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80643 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.ResultSetScriptExtensions
 * JD-Core Version:    0.5.4
 */