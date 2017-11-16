/*      */ package intradoc.search;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseStringException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainerUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.server.SearchLoader;
/*      */ import intradoc.shared.CommonSearchEngineConfig;
/*      */ import intradoc.shared.QueryElementField;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Collections;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Locale;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ 
/*      */ public class SearchCacheUtils
/*      */ {
/*      */   public static final int NEW_ROW_QUERY_OK = 0;
/*      */   public static final int NEW_ROW_QUERY_UNPARSED_CANNOT_TEST = 1;
/*      */   public static final int NEW_ROW_FAILS_QUERY = 2;
/*      */   public static final int NEW_ROW_OUTSIDE_LIST_RANGE = 3;
/*      */   public static final int NEW_ROW_HAS_BAD_DATA = 4;
/*      */   public static final int NEW_ROW_HAS_MISMATCHED_DESIGN = 5;
/*      */   public static final int NEW_ROW_HAS_BEEN_DELETED = 6;
/*      */   public static final int NEW_ROW_IS_NOT_IN_CHANGE_DATA_RANGE = 7;
/*      */   public static final int NEW_ROW_CHANGE_DATA_IS_INVALID = 8;
/*      */   public static final int NEW_ROW_QUERY_HAS_UNTESTABLE_OP = 9;
/*      */   public static final int NEW_ROW_MISSING_FIELD_IN_QUERY = 10;
/*      */   public static final int NEW_ROW_QUERY_HAS_UNTESTABLE_DATA = 11;
/*      */   public static final String[] NEW_ROW_FAIL_STRINGS;
/*      */ 
/*      */   public static String getNewRowFailureString(int reason)
/*      */   {
/*   84 */     if ((reason < 0) || (reason >= NEW_ROW_FAIL_STRINGS.length))
/*      */     {
/*   86 */       return "newRowInvalidReason";
/*      */     }
/*   88 */     return NEW_ROW_FAIL_STRINGS[reason];
/*      */   }
/*      */ 
/*      */   public static Table getCapturedResultFields()
/*      */   {
/*  100 */     Table capturedResultsFields = ResourceContainerUtils.getDynamicTableResource("SearchCacheResultFields");
/*      */ 
/*  102 */     if (capturedResultsFields == null)
/*      */     {
/*  104 */       List l = Arrays.asList(new String[][] { { "0", "0", "" } });
/*  105 */       capturedResultsFields = new Table(new String[] { "TotalRows", "TotalDocsProcessed", "HasMoreRows" }, l);
/*      */     }
/*      */ 
/*  108 */     return capturedResultsFields;
/*      */   }
/*      */ 
/*      */   public static String calculateDocNameDummyQuery(CacheUpdateParameters params, String canonicalDocName)
/*      */   {
/*  120 */     IdcStringBuilder tempBuilder = params.m_tempBuilder;
/*  121 */     tempBuilder.setLength(0);
/*  122 */     tempBuilder.append(canonicalDocName).append("&&&&").append(params.m_searchConfig.m_engineName);
/*  123 */     return tempBuilder.toString();
/*      */   }
/*      */ 
/*      */   public static String calculateCanonicalDocName(CacheUpdateParameters params, String docName)
/*      */   {
/*  134 */     return docName.toUpperCase(Locale.ENGLISH);
/*      */   }
/*      */ 
/*      */   public static void fillDataBinderWithCacheData(CacheUpdateParameters params, DataBinder resultsBinder)
/*      */   {
/*  148 */     CacheRow[] rows = params.m_cacheDocsListMetadata;
/*  149 */     SearchListItem[] items = params.m_cacheDocsList;
/*  150 */     if (params.m_cacheDataDesign == null)
/*      */     {
/*  152 */       return;
/*      */     }
/*  154 */     CacheDataDesign cacheDataDesign = params.m_cacheDataDesign;
/*  155 */     boolean capturePerQueryResults = (!params.m_allowCheckRowInclude) || (params.m_parsedQueryElements == null) || (params.m_parsedQueryElements.m_hasFullTextElement) || (params.m_cacheDataDesign.m_numNavigationFields > 0);
/*      */ 
/*  160 */     DataResultSet templateResultSet = cacheDataDesign.m_templateResultSet;
/*  161 */     FieldInfo[] perQueryFields = cacheDataDesign.m_perQueryFields;
/*  162 */     IdcDateFormat templateDateFormat = templateResultSet.getDateFormat();
/*  163 */     resultsBinder.m_blDateFormat = templateDateFormat;
/*  164 */     DataResultSet drset = new DataResultSet();
/*  165 */     drset.copyFieldInfo(templateResultSet);
/*  166 */     if ((rows != null) && (rows.length > 0))
/*      */     {
/*  168 */       for (int i = 0; i < rows.length; ++i)
/*      */       {
/*  170 */         CacheRow row = rows[i];
/*  171 */         List l = new ArrayList(row.m_row.length);
/*  172 */         Collections.addAll(l, row.m_row);
/*      */ 
/*  174 */         SearchListItem item = items[i];
/*  175 */         String[] perQueryDocMetadata = (capturePerQueryResults) ? item.m_perQueryDocMetadata : row.m_capturedPerQueryValues;
/*      */ 
/*  177 */         if ((perQueryDocMetadata != null) && (perQueryFields != null))
/*      */         {
/*  179 */           if (perQueryDocMetadata.length == perQueryFields.length)
/*      */           {
/*  181 */             for (int j = 0; j < perQueryFields.length; ++j)
/*      */             {
/*  183 */               String val = perQueryDocMetadata[j];
/*  184 */               if ((val == null) || (val.length() <= 0))
/*      */                 continue;
/*  186 */               l.set(perQueryFields[j].m_index, val);
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  192 */             Report.trace("searchcache", "Cached per query fields different from field list", null);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  198 */         drset.addRowWithList(l);
/*      */       }
/*      */     }
/*  201 */     resultsBinder.addResultSet("SearchResults", drset);
/*  202 */     if (params.m_cacheBinderCapturedValues != null)
/*      */     {
/*  204 */       for (int i = 0; i < params.m_cacheBinderCaptureKeys.length; ++i)
/*      */       {
/*  206 */         String val = params.m_cacheBinderCapturedValues[i];
/*  207 */         if (val == null)
/*      */         {
/*  209 */           val = "";
/*      */         }
/*  211 */         resultsBinder.putLocal(params.m_cacheBinderCaptureKeys[i], val);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  216 */     if (params.m_extraResultSets == null)
/*      */       return;
/*  218 */     for (Map.Entry entry : params.m_extraResultSets.entrySet())
/*      */     {
/*  220 */       String key = (String)entry.getKey();
/*  221 */       DataResultSet cachedRSet = (DataResultSet)entry.getValue();
/*  222 */       DataResultSet navRSet = new DataResultSet();
/*  223 */       navRSet.copyFieldInfo(cachedRSet);
/*      */ 
/*  225 */       int size = cachedRSet.getNumRows();
/*  226 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  228 */         List l = cachedRSet.getRowAsList(i);
/*  229 */         navRSet.addRowWithList(l);
/*      */       }
/*  231 */       resultsBinder.addResultSet(key, navRSet);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void computeCacheUpdateParameters(CacheUpdateParameters params, DataBinder updateBinder, DataResultSet drset, boolean updateHasSearchList)
/*      */   {
/*  248 */     params.m_updateBinder = updateBinder;
/*  249 */     params.m_updateRowCount = 0;
/*  250 */     if ((drset != null) && (drset.getNumRows() > 0) && 
/*  252 */       (params.m_cacheDataDesign != null))
/*      */     {
/*  254 */       params.m_updateHasResults = true;
/*  255 */       params.m_updateResultSet = drset;
/*  256 */       params.m_updateRowCount = drset.getNumRows();
/*  257 */       IdcDateFormat drsetDateFormat = drset.getDateFormat();
/*  258 */       params.m_updateDateFormat = drsetDateFormat;
/*  259 */       IdcDateFormat cacheDateFormat = params.m_cacheDataDesign.m_templateResultSet.getDateFormat();
/*  260 */       if (!drsetDateFormat.equals(cacheDateFormat))
/*      */       {
/*  262 */         params.m_updateDateFormatMismatchedWithCache = true;
/*      */       }
/*      */ 
/*  266 */       params.m_fieldMap = ResultSetUtils.createColumnMap(drset, params.m_cacheDataDesign.m_templateResultSet);
/*      */     }
/*      */ 
/*  270 */     if (updateHasSearchList)
/*      */     {
/*  272 */       String[] captureKeys = params.m_updateBinderCaptureKeys;
/*  273 */       if (captureKeys != null)
/*      */       {
/*  275 */         String[] vals = new String[captureKeys.length];
/*  276 */         for (int i = 0; i < captureKeys.length; ++i)
/*      */         {
/*  278 */           String key = captureKeys[i];
/*  279 */           vals[i] = updateBinder.getLocal(key);
/*      */         }
/*  281 */         params.m_updateBinderCapturedValues = vals;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  286 */     String additionalRSetName = "SearchResultNavigation";
/*  287 */     drset = (DataResultSet)updateBinder.getResultSet(additionalRSetName);
/*  288 */     if (drset == null)
/*      */       return;
/*  290 */     if (params.m_extraResultSets == null)
/*      */     {
/*  292 */       params.m_extraResultSets = new HashMap();
/*      */     }
/*      */ 
/*  295 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  297 */       String newName = additionalRSetName + drset.getStringValue(0);
/*  298 */       DataResultSet newRSet = (DataResultSet)updateBinder.getResultSet(newName);
/*  299 */       if (newRSet == null)
/*      */         continue;
/*  301 */       params.m_extraResultSets.put(newName, newRSet);
/*      */     }
/*      */ 
/*  304 */     params.m_extraResultSets.put(additionalRSetName, drset);
/*      */   }
/*      */ 
/*      */   public static void checkUpdateTemplateResultSet(CacheUpdateParameters params, DataBinder results, DataResultSet newRows)
/*      */   {
/*  320 */     if (params.m_cacheDataDesign != null)
/*      */       return;
/*  322 */     CommonSearchEngineConfig searchEngineConfig = params.m_searchConfig;
/*  323 */     String engineName = searchEngineConfig.m_engineName;
/*  324 */     ProviderCache providerCache = params.m_providerCache;
/*  325 */     CacheDataDesign cacheDataDesign = (CacheDataDesign)providerCache.m_cacheDataDesigns.get(engineName);
/*  326 */     if (cacheDataDesign == null)
/*      */     {
/*  328 */       cacheDataDesign = new CacheDataDesign();
/*  329 */       cacheDataDesign.m_searchEngineConfig = searchEngineConfig;
/*  330 */       DataResultSet templateSet = new DataResultSet();
/*  331 */       int numFields = newRows.getNumFields();
/*  332 */       List fieldInfoList = new ArrayList(numFields);
/*  333 */       for (int i = 0; i < numFields; ++i)
/*      */       {
/*  335 */         FieldInfo fi = new FieldInfo();
/*  336 */         newRows.getFieldInfoByIndex(i, fi);
/*  337 */         String type = results.getFieldType(fi.m_name);
/*  338 */         if (type != null)
/*      */         {
/*  340 */           if (type.equals("date"))
/*      */           {
/*  342 */             fi.m_type = 5;
/*      */           }
/*  344 */           else if (type.equals("int"))
/*      */           {
/*  346 */             fi.m_type = 3;
/*      */           }
/*      */         }
/*  349 */         fieldInfoList.add(fi);
/*      */       }
/*      */ 
/*  352 */       templateSet.mergeFieldsWithFlags(fieldInfoList, 0);
/*  353 */       templateSet.setDateFormat(newRows.getDateFormat());
/*      */ 
/*  355 */       cacheDataDesign.m_primaryKey = providerCache.m_primaryKey;
/*  356 */       cacheDataDesign.m_primaryKeyIndex = templateSet.getFieldInfoIndex(providerCache.m_primaryKey);
/*      */ 
/*  358 */       cacheDataDesign.m_specificRevKey = providerCache.m_specificRevKey;
/*  359 */       cacheDataDesign.m_specificRevKeyIndex = templateSet.getFieldInfoIndex(providerCache.m_specificRevKey);
/*      */ 
/*  361 */       cacheDataDesign.m_titleRevKey = providerCache.m_titleRevKey;
/*  362 */       cacheDataDesign.m_titleRevKeyIndex = templateSet.getFieldInfoIndex(providerCache.m_titleRevKey);
/*      */ 
/*  364 */       computeFullTextFields(params, cacheDataDesign, templateSet);
/*  365 */       cacheDataDesign.m_templateResultSet = templateSet;
/*  366 */       if ((cacheDataDesign.m_primaryKeyIndex >= 0) && (cacheDataDesign.m_specificRevKeyIndex >= 0))
/*      */       {
/*  368 */         providerCache.m_cacheDataDesigns.put(engineName, cacheDataDesign);
/*      */       }
/*      */       else
/*      */       {
/*  372 */         Report.trace("system", "Update by search result set did not have a needed key, cacheDataDesign=" + cacheDataDesign, new Throwable());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  377 */     params.m_cacheDataDesign = cacheDataDesign;
/*      */   }
/*      */ 
/*      */   public static void computeFullTextFields(CacheUpdateParameters params, CacheDataDesign cacheDataDesign, DataResultSet templateSet)
/*      */   {
/*  391 */     CommonSearchEngineConfig eConfig = params.m_searchConfig;
/*  392 */     if (eConfig == null)
/*      */       return;
/*  394 */     String[] fullTextFields = eConfig.m_fullTextFields;
/*  395 */     if (fullTextFields == null)
/*      */       return;
/*      */     try
/*      */     {
/*  399 */       cacheDataDesign.m_perQueryFields = ResultSetUtils.createInfoListWithFlags(templateSet, fullTextFields, 2);
/*      */ 
/*  402 */       cacheDataDesign.m_numNavigationFields = eConfig.m_numNavigationFields;
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  406 */       Report.trace("system", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void createObjectArrayAndQueryFromRow(CacheUpdateParameters params, int rowIndex)
/*      */     throws DataException
/*      */   {
/*  423 */     DataResultSet updateResultSet = params.m_updateResultSet;
/*  424 */     List row = updateResultSet.getRowAsList(rowIndex);
/*  425 */     CacheDataDesign cacheDataDesign = params.m_cacheDataDesign;
/*  426 */     DataResultSet templateSet = cacheDataDesign.m_templateResultSet;
/*  427 */     String[] results = new String[templateSet.getNumFields()];
/*  428 */     IdcDateFormat updateDateFormat = params.m_updateDateFormat;
/*  429 */     IdcDateFormat designDateFormat = templateSet.getDateFormat();
/*  430 */     boolean dateFormatsMatch = updateDateFormat.equals(designDateFormat);
/*  431 */     int[] map = params.m_fieldMap;
/*  432 */     int rowlen = row.size();
/*      */ 
/*  434 */     Arrays.fill(results, "");
/*      */ 
/*  436 */     FieldInfo fi = new FieldInfo();
/*  437 */     for (int i = 0; i < rowlen; ++i)
/*      */     {
/*  439 */       int j = map[i];
/*  440 */       if (j < 0) continue; if (j >= results.length) {
/*      */         continue;
/*      */       }
/*      */ 
/*  444 */       String val = (String)row.get(i);
/*  445 */       templateSet.getIndexFieldInfo(j, fi);
/*  446 */       if ((fi.m_type == 5) && (!dateFormatsMatch) && (val.length() > 0))
/*      */       {
/*      */         try
/*      */         {
/*  450 */           Date d = updateDateFormat.parseDate(val);
/*  451 */           val = designDateFormat.format(d);
/*      */         }
/*      */         catch (ParseStringException e)
/*      */         {
/*  456 */           throw new DataException("", e);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  462 */       if ((val == null) || (val.length() <= 0))
/*      */         continue;
/*  464 */       results[j] = val;
/*      */     }
/*      */ 
/*  469 */     boolean capturePerQueryResults = (((!params.m_allowCheckRowInclude) || (params.m_parsedQueryElements == null) || (params.m_parsedQueryElements.m_hasFullTextElement) || (params.m_cacheDataDesign == null) || (params.m_cacheDataDesign.m_numNavigationFields > 0))) && (params.m_updateHasDocsList);
/*      */ 
/*  476 */     FieldInfo[] fiArray = cacheDataDesign.m_perQueryFields;
/*  477 */     params.m_updateCapturedPerQueryValues = null;
/*  478 */     if (fiArray != null)
/*      */     {
/*  480 */       String[] perItemList = null;
/*  481 */       for (int i = 0; i < fiArray.length; ++i)
/*      */       {
/*  483 */         int index = fiArray[i].m_index;
/*  484 */         String val = results[index];
/*  485 */         if ((val == null) || (val.length() <= 0))
/*      */           continue;
/*  487 */         if (perItemList == null)
/*      */         {
/*  489 */           perItemList = new String[fiArray.length];
/*      */         }
/*  491 */         perItemList[i] = val;
/*      */ 
/*  493 */         if (!cacheDataDesign.m_allowClearingOfOriginalPerQueryMetadata)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  498 */         results[index] = "";
/*      */       }
/*      */ 
/*  502 */       if (perItemList == null)
/*      */       {
/*  504 */         Report.trace("searchcache", "No non empty per query field values were present", null);
/*      */       }
/*  506 */       if (capturePerQueryResults)
/*      */       {
/*  508 */         if (params.m_updateDocsList != null)
/*      */         {
/*  510 */           SearchListItem item = params.m_updateDocsList[rowIndex];
/*  511 */           item.m_perQueryDocMetadata = perItemList;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  517 */         params.m_updateCapturedPerQueryValues = perItemList;
/*      */       }
/*      */ 
/*      */     }
/*  523 */     else if (params.m_cacheDataDesign.m_numNavigationFields > 0)
/*      */     {
/*  525 */       Report.trace("searchcache", "No per query fields to capture, numNavigationFields=" + params.m_cacheDataDesign.m_numNavigationFields + ", docListQuery=" + params.m_docListQuery + ", sortField=" + params.m_sortField, null);
/*      */     }
/*      */ 
/*  531 */     String key = calculateCanonicalDocName(params, results[cacheDataDesign.m_primaryKeyIndex]);
/*  532 */     params.m_query = calculateDocNameDummyQuery(params, key);
/*  533 */     params.m_updateResultSetRow = results;
/*  534 */     params.m_updateRowIndex = rowIndex;
/*  535 */     params.m_updateHasResultSetRow = true;
/*      */   }
/*      */ 
/*      */   public static void createObjectArrayFromSearchResultsUpdate(CacheUpdateParameters params)
/*      */     throws DataException
/*      */   {
/*  547 */     DataResultSet drset = params.m_updateResultSet;
/*  548 */     DataBinder binder = params.m_updateBinder;
/*  549 */     params.m_updateHasResults = false;
/*  550 */     params.m_updateHasDocsList = false;
/*  551 */     params.m_updateDocsList = null;
/*  552 */     params.m_requestedRows = 0;
/*  553 */     CacheDataDesign dataDesign = params.m_cacheDataDesign;
/*      */ 
/*  555 */     if ((binder == null) || (dataDesign == null))
/*      */       return;
/*  557 */     FieldInfo fi = new FieldInfo();
/*  558 */     params.m_requestedRows = (NumberUtils.parseInteger(binder.getLocal("StartRow"), 1) + NumberUtils.parseInteger(binder.getLocal("ResultCount"), 25) - 1);
/*      */ 
/*  561 */     if (params.m_isSorted)
/*      */     {
/*  563 */       FieldInfo cacheSortFieldInfo = new FieldInfo();
/*  564 */       if (!dataDesign.m_templateResultSet.getFieldInfo(params.m_sortField, cacheSortFieldInfo))
/*      */       {
/*  566 */         String msg = LocaleUtils.encodeMessage("csSearchSortFieldInvalid", null, params.m_sortField);
/*      */ 
/*  568 */         throw new DataException(msg);
/*      */       }
/*  570 */       params.m_cacheSortFieldInfo = cacheSortFieldInfo;
/*      */     }
/*      */ 
/*  574 */     if ((drset != null) && (!drset.isEmpty()))
/*      */     {
/*  576 */       if (!drset.getFieldInfo(dataDesign.m_primaryKey, fi))
/*      */         return;
/*  578 */       if (params.m_isSorted)
/*      */       {
/*  580 */         FieldInfo updateSortFieldInfo = new FieldInfo();
/*  581 */         drset.getFieldInfo(params.m_sortField, updateSortFieldInfo);
/*  582 */         if ((!drset.getFieldInfo(params.m_sortField, updateSortFieldInfo)) || (updateSortFieldInfo.m_type != params.m_cacheSortFieldInfo.m_type))
/*      */         {
/*  585 */           String msg = LocaleUtils.encodeMessage("csSearchSortFieldInvalid", null, params.m_sortField);
/*      */ 
/*  587 */           throw new DataException(msg);
/*      */         }
/*  589 */         params.m_updateSortFieldInfo = updateSortFieldInfo;
/*      */       }
/*  591 */       params.m_updateHasResults = true;
/*  592 */       int numRows = drset.getNumRows();
/*  593 */       int curRow = drset.getCurrentRow();
/*  594 */       SearchListItem[] data = new SearchListItem[numRows];
/*  595 */       for (int i = 0; i < numRows; ++i)
/*      */       {
/*  597 */         drset.setCurrentRow(i);
/*  598 */         SearchListItem item = createSearchListItemFromResultSet(params, drset, fi);
/*  599 */         data[i] = item;
/*      */       }
/*  601 */       drset.setCurrentRow(curRow);
/*  602 */       params.m_updateHasDocsList = true;
/*  603 */       params.m_updateDocsList = data;
/*      */     }
/*      */     else
/*      */     {
/*  610 */       params.m_updateHasDocsList = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static SearchListItem createSearchListItemFromResultSet(CacheUpdateParameters params, DataResultSet drset, FieldInfo keyField)
/*      */   {
/*  628 */     SearchListItem item = new SearchListItem();
/*      */ 
/*  630 */     item.m_key = drset.getStringValue(keyField.m_index);
/*  631 */     if (params.m_isSorted)
/*      */     {
/*  633 */       int updateSortIndex = params.m_updateSortFieldInfo.m_index;
/*      */ 
/*  635 */       int sortFieldType = params.m_cacheSortFieldInfo.m_type;
/*  636 */       if (sortFieldType == 5)
/*      */       {
/*  638 */         Date d = drset.getDateValue(updateSortIndex);
/*      */ 
/*  641 */         if (d != null)
/*      */         {
/*  643 */           item.m_sortValueParsed = d.getTime();
/*  644 */           if (params.m_updateDateFormatMismatchedWithCache)
/*      */           {
/*  646 */             IdcDateFormat cacheDate = params.m_cacheDataDesign.m_templateResultSet.getDateFormat();
/*  647 */             item.m_sortValue = cacheDate.format(d);
/*      */           }
/*      */         }
/*      */       }
/*  651 */       else if (sortFieldType == 3)
/*      */       {
/*  653 */         item.m_sortValueParsed = NumberUtils.parseLong(item.m_sortValue, 0L);
/*  654 */         item.m_sortValue = drset.getStringValue(updateSortIndex);
/*      */       }
/*      */       else
/*      */       {
/*  658 */         item.m_sortValue = drset.getStringValue(updateSortIndex);
/*  659 */         item.m_sortValuePrepared = prepareForComparison(params.m_cacheDataDesign, params.m_cacheSortFieldInfo.m_index, sortFieldType, item.m_sortValue, 0, params.m_cxt);
/*      */       }
/*      */ 
/*  663 */       if (item.m_sortValue == null)
/*      */       {
/*  665 */         item.m_sortValue = drset.getStringValue(updateSortIndex);
/*      */       }
/*      */     }
/*  668 */     return item;
/*      */   }
/*      */ 
/*      */   public static SearchListItem createSearchListItemFromCacheRow(CacheUpdateParameters params, CacheRow row)
/*      */   {
/*  686 */     SearchListItem item = new SearchListItem();
/*  687 */     item.m_key = row.m_row[row.m_dataDesign.m_primaryKeyIndex];
/*  688 */     int sortIndex = -1;
/*  689 */     boolean isSorted = params.m_isSorted;
/*  690 */     FieldInfo sortFieldInfo = params.m_cacheSortFieldInfo;
/*  691 */     if (isSorted)
/*      */     {
/*  693 */       sortIndex = row.m_dataDesign.m_templateResultSet.getFieldInfoIndex(sortFieldInfo.m_name);
/*      */     }
/*  695 */     if (sortIndex >= 0)
/*      */     {
/*  697 */       String sortValue = row.m_row[sortIndex];
/*  698 */       item.m_sortValue = sortValue;
/*  699 */       int sortFieldType = sortFieldInfo.m_type;
/*  700 */       Object o = null;
/*  701 */       Date d = null;
/*  702 */       String s = null;
/*  703 */       long l = 0L;
/*  704 */       if (row.m_resultRowCachedConversions == null)
/*      */       {
/*  706 */         row.m_resultRowCachedConversions = new Object[row.m_row.length];
/*      */       }
/*      */       else
/*      */       {
/*  710 */         o = row.m_resultRowCachedConversions[sortIndex];
/*      */       }
/*  712 */       if (o == null)
/*      */       {
/*  714 */         if (sortFieldType == 5)
/*      */         {
/*  716 */           d = row.m_dataDesign.m_templateResultSet.parseDateValue(sortValue, sortIndex);
/*  717 */           l = d.getTime();
/*  718 */           o = new Long(l);
/*      */         }
/*  720 */         else if (sortFieldType == 3)
/*      */         {
/*  722 */           l = NumberUtils.parseLong(sortValue, 0L);
/*  723 */           o = new Long(l);
/*      */         }
/*      */         else
/*      */         {
/*  727 */           s = prepareForComparison(params.m_cacheDataDesign, sortFieldInfo.m_index, sortFieldType, item.m_sortValue, 0, params.m_cxt);
/*      */ 
/*  729 */           o = s;
/*      */         }
/*  731 */         row.m_resultRowCachedConversions[sortIndex] = o;
/*      */       }
/*  735 */       else if ((sortFieldType == 5) || (sortFieldType == 3))
/*      */       {
/*  737 */         l = ((Number)o).longValue();
/*      */       }
/*      */       else
/*      */       {
/*  741 */         s = o.toString();
/*      */       }
/*      */ 
/*  744 */       if ((sortFieldType == 5) || (sortFieldType == 3))
/*      */       {
/*  746 */         item.m_sortValueParsed = l;
/*      */       }
/*      */       else
/*      */       {
/*  750 */         item.m_sortValuePrepared = s;
/*      */       }
/*      */     }
/*      */ 
/*  754 */     return item;
/*      */   }
/*      */ 
/*      */   public static void computeAllowedParsedQueryOptions(CacheUpdateParameters params)
/*      */   {
/*  765 */     if ((params.m_parsedQueryElements == null) || (params.m_parsedQueryElements.m_searchQuery == null) || (params.m_parsedQueryElements.m_isError))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/*  770 */     params.m_allowCheckRowInclude = (!params.m_parsedQueryElements.m_hasFullTextElement);
/*  771 */     params.m_allowCheckRowExclude = true;
/*      */   }
/*      */ 
/*      */   public static boolean checkAndCaptureDocListChanges(CacheUpdateParameters params)
/*      */   {
/*  792 */     SearchDocChangeData changeData = params.m_changeData;
/*  793 */     CacheRow[] currentRows = params.m_cacheDocsListMetadata;
/*  794 */     SearchListItem[] docList = params.m_cacheDocsList;
/*  795 */     boolean didChange = false;
/*  796 */     boolean doCacheChangeRepair = (params.m_cacheUpdateStateFlags & SearchCache.F_DO_CACHE_CHANGE_REPAIR) != 0;
/*      */ 
/*  798 */     boolean doDifferentialRepair = (params.m_cacheUpdateStateFlags & SearchCache.F_DO_DIFFERENTIAL_REPAIR) != 0;
/*      */ 
/*  801 */     if ((params.m_cacheSharedSearchTime < changeData.m_fastChangesStartTime) || (!changeData.m_isValidFastData))
/*      */     {
/*  807 */       Report.trace("searchcache", "checkAndCaptureDocListChanges -- incompatible changeData", null);
/*  808 */       return false;
/*      */     }
/*  810 */     if (Report.m_verbose)
/*      */     {
/*  812 */       Report.debug("searchcache", "checkAndCaptureDocListChanges -- testing parsed query: " + params.m_parsedQueryElements, null);
/*      */     }
/*      */ 
/*  816 */     HashMap[] navCountUpdates = null;
/*      */ 
/*  818 */     if ((currentRows != null) && (currentRows.length > 0))
/*      */     {
/*  820 */       for (int i = 0; (i < currentRows.length) && (params.m_isValidCache); ++i)
/*      */       {
/*  822 */         CacheRow row = currentRows[i];
/*  823 */         if (row == null)
/*      */           continue;
/*  825 */         boolean reportResult = true;
/*  826 */         String initialReportMsg = null;
/*  827 */         int reportLevel = 6000;
/*  828 */         SearchListItem docItem = docList[i];
/*  829 */         String id = row.m_row[params.m_cacheDataDesign.m_specificRevKeyIndex];
/*  830 */         SearchDocChangeItem changeItem = (SearchDocChangeItem)changeData.m_fastIdReferences.get(id);
/*  831 */         boolean reportReason = true;
/*  832 */         if ((changeItem != null) && (changeItem.m_changeType == SearchDocChangeItem.CHANGE_DELETE))
/*      */         {
/*  835 */           didChange = true;
/*  836 */           if (!doCacheChangeRepair)
/*      */           {
/*  842 */             initialReportMsg = "Cache row, deleted but repair is not allowed";
/*  843 */             params.m_isValidCache = false;
/*      */           }
/*      */           else
/*      */           {
/*  847 */             initialReportMsg = "Cache row deleted, removing from cache";
/*  848 */             docList[i] = null;
/*      */           }
/*      */ 
/*  853 */           reportReason = false;
/*      */         }
/*  855 */         else if (params.m_cacheGenerationalCounter < row.m_generationalCounter)
/*      */         {
/*  859 */           if ((!doCacheChangeRepair) || (!params.m_allowCheckRowInclude))
/*      */           {
/*  861 */             initialReportMsg = "Cache does not allow repair by new replacement row";
/*  862 */             reportReason = false;
/*  863 */             params.m_isValidCache = false;
/*  864 */             didChange = true;
/*      */           }
/*  866 */           else if (!checkSatisfiesQuery(params, docItem, row))
/*      */           {
/*  868 */             initialReportMsg = "Unable to fit in replacement row";
/*      */ 
/*  870 */             if ((params.m_newRowDidNotFitReason == 2) || ((!params.m_searchConfig.m_invalidateQueryCacheOnOutRangeRow) && (params.m_newRowDidNotFitReason == 3)))
/*      */             {
/*  874 */               docList[i] = null;
/*      */ 
/*  877 */               reportResult = Report.m_verbose;
/*  878 */               reportLevel = 7000;
/*      */             }
/*      */             else
/*      */             {
/*  882 */               if (params.m_newRowDidNotFitReason == 3)
/*      */               {
/*  885 */                 initialReportMsg = "Cache invalidated because row out of range";
/*      */               }
/*  887 */               params.m_isValidCache = false;
/*      */             }
/*  889 */             didChange = true;
/*      */           }
/*      */           else
/*      */           {
/*  893 */             if (!params.m_newItemCanReplaceOriginal)
/*      */             {
/*  895 */               initialReportMsg = "Replacement row is causing resort";
/*  896 */               didChange = true;
/*  897 */               params.m_cacheDocsListNeedsSorting = true;
/*      */             }
/*      */ 
/*  901 */             if (row.m_dataDesign.m_numNavigationFields > 0)
/*      */             {
/*  903 */               if (Report.m_verbose)
/*      */               {
/*  905 */                 String missingNavFields = "";
/*  906 */                 if (row.m_capturedPerQueryValues == null)
/*      */                 {
/*  908 */                   missingNavFields = missingNavFields + "[cached metadata]";
/*  909 */                   Report.debug("searchcache", "No per query values for navigation fields for actual cached metadata, numNavigationFields=" + row.m_dataDesign.m_numNavigationFields, null);
/*      */                 }
/*      */ 
/*  912 */                 if (docItem.m_perQueryDocMetadata == null)
/*      */                 {
/*  914 */                   missingNavFields = missingNavFields + "[per query]";
/*      */                 }
/*  916 */                 if (missingNavFields.length() > 0)
/*      */                 {
/*  918 */                   Report.debug("searchcache", "No " + missingNavFields + " values for navigation fields, numNavigationFields=" + row.m_dataDesign.m_numNavigationFields, null);
/*      */                 }
/*      */ 
/*      */               }
/*      */ 
/*  923 */               for (int j = 0; j < row.m_dataDesign.m_numNavigationFields; ++j)
/*      */               {
/*  925 */                 boolean noCapturedPerQueryValues = (row.m_capturedPerQueryValues == null) || (row.m_capturedPerQueryValues[j] == null);
/*  926 */                 boolean noDocPerQueryValues = (docItem.m_perQueryDocMetadata == null) || (docItem.m_perQueryDocMetadata[j] == null);
/*  927 */                 if ((noCapturedPerQueryValues) && (noDocPerQueryValues)) {
/*      */                   continue;
/*      */                 }
/*      */ 
/*  931 */                 if ((!noCapturedPerQueryValues) && (!noDocPerQueryValues) && (row.m_capturedPerQueryValues[j].equals(docItem.m_perQueryDocMetadata[j]))) {
/*      */                   continue;
/*      */                 }
/*      */ 
/*  935 */                 initialReportMsg = "Replacement row is causing drill down update.";
/*  936 */                 navCountUpdates = calculateNavCountChange(row.m_capturedPerQueryValues, navCountUpdates, 1);
/*      */ 
/*  938 */                 navCountUpdates = calculateNavCountChange(docItem.m_perQueryDocMetadata, navCountUpdates, -1);
/*      */ 
/*  940 */                 break;
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  948 */           initialReportMsg = "Cache row has no detectable changes";
/*  949 */           reportResult = Report.m_verbose;
/*  950 */           reportLevel = 7000;
/*  951 */           reportReason = false;
/*      */         }
/*  953 */         if (reportResult)
/*      */         {
/*  955 */           if (initialReportMsg == null)
/*      */           {
/*  957 */             initialReportMsg = "Replacement row fits in cleanly";
/*      */           }
/*  959 */           IdcStringBuilder reportMsg = new IdcStringBuilder(256);
/*  960 */           reportMsg.append(initialReportMsg).append(" for new cache row=");
/*  961 */           row.appendDebugFormat(reportMsg);
/*  962 */           if (reportReason)
/*      */           {
/*  964 */             reportMsg.append(", reasonCode=");
/*  965 */             String reasonStr = getNewRowFailureString(params.m_newRowDidNotFitReason);
/*      */ 
/*  967 */             reportMsg.append(reasonStr);
/*      */           }
/*  969 */           Report.simpleMessage("searchcache", reportLevel, reportMsg.toString(), null);
/*      */         }
/*  971 */         if (!params.m_isValidCache)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  978 */         String key = preparePrimaryKeyForIndexing(params.m_cacheDataDesign, docItem.m_key, params.m_cxt);
/*      */ 
/*  980 */         params.addAlreadyValidatedRow(key, row);
/*  981 */         params.m_cacheRowNeededQueryRetest = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  987 */     if (params.m_isValidCache)
/*      */     {
/*  989 */       if (navCountUpdates != null)
/*      */       {
/*  991 */         updateNavCountChange(params.m_extraResultSets, navCountUpdates, params.m_cacheDataDesign);
/*      */       }
/*  993 */       SearchDocChangeItem[] changeItems = changeData.m_fastChangesArray;
/*  994 */       if ((changeItems != null) && (changeItems.length > 0))
/*      */       {
/*  996 */         for (int i = 0; i < changeItems.length; ++i)
/*      */         {
/*  998 */           SearchDocChangeItem item = changeItems[i];
/*  999 */           if (item.m_capturedTime < params.m_cacheSharedSearchTime)
/*      */           {
/*      */             break;
/*      */           }
/*      */ 
/* 1004 */           String key = preparePrimaryKeyForIndexing(params.m_cacheDataDesign, item.m_classDocID, params.m_cxt);
/*      */ 
/* 1006 */           CacheRow existingRow = params.getAlreadyValidatedRow(key);
/* 1007 */           if ((existingRow != null) && (existingRow.m_generationalCounter >= item.m_cacheRow.m_generationalCounter))
/*      */           {
/* 1011 */             if (!Report.m_verbose)
/*      */               break;
/* 1013 */             Report.debug("searchcache", "Already processed cache associated with item=" + item.toString(), null); break;
/*      */           }
/*      */ 
/* 1019 */           boolean reportResult = true;
/* 1020 */           String initialReportMsg = null;
/* 1021 */           int reportLevel = 6000;
/* 1022 */           if ((existingRow != null) && (!doDifferentialRepair))
/*      */           {
/* 1025 */             params.m_isValidCache = false;
/* 1026 */             initialReportMsg = "Row is updated, invaidates cache";
/*      */           }
/* 1028 */           else if (checkSatisfiesQuery(params, null, item.m_cacheRow))
/*      */           {
/* 1030 */             if ((!doDifferentialRepair) || (!params.m_allowCheckRowInclude) || (!params.m_cacheDataDesign.equals(item.m_cacheRow.m_dataDesign)))
/*      */             {
/* 1033 */               params.m_isValidCache = false;
/* 1034 */               initialReportMsg = "New row satisfies query, invalidates cache";
/*      */             }
/*      */             else
/*      */             {
/* 1038 */               CacheRow row = item.m_cacheRow;
/* 1039 */               SearchListItem listItem = createSearchListItemFromCacheRow(params, row);
/* 1040 */               params.addNewDocsListData(row, listItem);
/*      */ 
/* 1042 */               initialReportMsg = "Fitted new row into cache";
/*      */             }
/* 1044 */             didChange = true;
/*      */           }
/* 1049 */           else if ((params.m_newRowDidNotFitReason != 2) && (params.m_newRowDidNotFitReason != 3))
/*      */           {
/* 1052 */             reportResult = true;
/* 1053 */             params.m_isValidCache = false;
/*      */           }
/*      */           else
/*      */           {
/* 1060 */             reportResult = Report.m_verbose;
/* 1061 */             reportLevel = 7000;
/*      */ 
/* 1063 */             if (existingRow != null)
/*      */             {
/* 1067 */               for (int j = 0; (j < currentRows.length) && (params.m_isValidCache); ++j)
/*      */               {
/* 1069 */                 CacheRow row = currentRows[j];
/* 1070 */                 if (row != existingRow)
/*      */                   continue;
/* 1072 */                 initialReportMsg = "Removing existing row that new row matches";
/* 1073 */                 docList[j] = null;
/* 1074 */                 didChange = true;
/* 1075 */                 break;
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1082 */           if (reportResult)
/*      */           {
/* 1084 */             IdcStringBuilder reportMsg = new IdcStringBuilder(256);
/* 1085 */             if (initialReportMsg == null)
/*      */             {
/* 1087 */               reportMsg.append("Unable to fit in new row during cache check for query=").append(params.m_query);
/*      */             }
/*      */             else
/*      */             {
/* 1092 */               reportMsg.append(initialReportMsg);
/*      */             }
/* 1094 */             reportMsg.append(", item=");
/* 1095 */             item.appendDebugFormat(reportMsg);
/* 1096 */             String reasonStr = getNewRowFailureString(params.m_newRowDidNotFitReason);
/*      */ 
/* 1098 */             reportMsg.append(", reasonCode=").append(reasonStr);
/* 1099 */             Report.simpleMessage("searchcache", reportLevel, reportMsg.toString(), null);
/*      */           }
/* 1101 */           params.addAlreadyValidatedRow(key, item.m_cacheRow);
/* 1102 */           if (!params.m_isValidCache) {
/*      */             break;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1110 */     if (didChange)
/*      */     {
/* 1112 */       params.m_cacheDocsListNeedsMerging = true;
/*      */     }
/*      */ 
/* 1115 */     return (params.m_isValidCache) && (params.m_cacheDocsListNeedsMerging);
/*      */   }
/*      */ 
/*      */   public static boolean checkIfReplacementRowFits(String query, CacheUpdateParameters params, CacheObject obj, int row)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1135 */     boolean rowFits = true;
/* 1136 */     CacheRow cacheRow = obj.m_resultSetRow;
/* 1137 */     CacheDataDesign dataDesign = cacheRow.m_dataDesign;
/* 1138 */     if (cacheRow.m_generationalCounter > params.m_cacheGenerationalCounter)
/*      */     {
/* 1146 */       SearchDocChangeData changeData = params.m_changeData;
/* 1147 */       if ((changeData != null) && (changeData.m_isValidFastData) && ((params.m_cacheUpdateStateFlags & SearchCache.F_CHECK_FOR_DELETE_ON_NEW_ROWS) != 0))
/*      */       {
/* 1151 */         long cacheSharedSearchTime = obj.m_sharedSearchTime;
/* 1152 */         int cacheAge = obj.m_ageCounter;
/* 1153 */         String id = obj.m_resultSetRow.m_row[dataDesign.m_specificRevKeyIndex];
/* 1154 */         boolean doDeleteTest = cacheAge == changeData.m_currentAge;
/* 1155 */         if ((!doDeleteTest) && (changeData.m_isValidFastData) && (cacheSharedSearchTime > changeData.m_fastChangesStartTime))
/*      */         {
/* 1158 */           doDeleteTest = true;
/*      */         }
/* 1160 */         if (doDeleteTest)
/*      */         {
/* 1162 */           SearchDocChangeItem changeItem = (SearchDocChangeItem)changeData.m_fastIdReferences.get(id);
/* 1163 */           if ((changeItem != null) && (changeItem.m_changeType == SearchDocChangeItem.CHANGE_DELETE))
/*      */           {
/* 1166 */             params.m_newRowDidNotFitReason = 6;
/* 1167 */             rowFits = false;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 1172 */           params.m_newRowDidNotFitReason = 7;
/* 1173 */           rowFits = false;
/*      */         }
/*      */       }
/* 1176 */       if (rowFits)
/*      */       {
/* 1178 */         rowFits = checkSatisfiesQuery(params, params.m_cacheDocsList[row], cacheRow);
/*      */       }
/*      */     }
/*      */ 
/* 1182 */     String initialReportMsg = null;
/* 1183 */     if (rowFits)
/*      */     {
/* 1185 */       String[] perQueryValues = null;
/* 1186 */       if (params.m_updateCapturedPerQueryValues != null)
/*      */       {
/* 1188 */         perQueryValues = params.m_updateCapturedPerQueryValues;
/*      */       }
/*      */       else
/*      */       {
/* 1192 */         perQueryValues = cacheRow.m_capturedPerQueryValues;
/*      */       }
/* 1194 */       params.m_cacheDocsListMetadata[row] = new CacheRow(params.m_updateResultSetRow, perQueryValues, params.m_cacheDataDesign, params.m_updateGenerationalCounter);
/*      */ 
/* 1197 */       if (!params.m_newItemCanReplaceOriginal)
/*      */       {
/* 1199 */         params.m_cacheDocsListNeedsSorting = true;
/* 1200 */         params.m_cacheDocsList[row] = params.m_tempNewDocItem;
/* 1201 */         initialReportMsg = "Fitted replacement row into cache (resorting required)";
/*      */       }
/*      */       else
/*      */       {
/* 1205 */         initialReportMsg = "Fitted replacement row into cache";
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1213 */       params.m_isValidCache = false;
/*      */     }
/* 1215 */     IdcStringBuilder reportMsg = new IdcStringBuilder(256);
/* 1216 */     if (initialReportMsg == null)
/*      */     {
/* 1218 */       reportMsg.append("Unable to fit in replacement row during cache check for query=").append(params.m_query);
/*      */     }
/*      */     else
/*      */     {
/* 1223 */       reportMsg.append(initialReportMsg);
/*      */     }
/* 1225 */     reportMsg.append(", row=");
/* 1226 */     if ((params.m_cacheDocsList != null) && (params.m_cacheDocsList[row] != null))
/*      */     {
/* 1228 */       params.m_cacheDocsList[row].appendDebugFormat(reportMsg);
/*      */     }
/*      */     else
/*      */     {
/* 1232 */       reportMsg.append("<missing>");
/*      */     }
/* 1234 */     String reasonStr = getNewRowFailureString(params.m_newRowDidNotFitReason);
/*      */ 
/* 1236 */     reportMsg.append(", reasonCode=").append(reasonStr);
/* 1237 */     Report.trace("searchcache", reportMsg.toString(), null);
/* 1238 */     return rowFits;
/*      */   }
/*      */ 
/*      */   public static boolean checkSatisfiesQuery(CacheUpdateParameters params, SearchListItem originalItem, CacheRow newRow)
/*      */   {
/* 1264 */     params.m_tempNewDocItem = null;
/* 1265 */     params.m_newRowDidNotFitReason = 0;
/* 1266 */     params.m_newItemCanReplaceOriginal = false;
/*      */ 
/* 1268 */     boolean isSorted = params.m_isSorted;
/* 1269 */     FieldInfo searchInfo = params.m_cacheSortFieldInfo;
/* 1270 */     if ((isSorted) && (searchInfo == null))
/*      */     {
/* 1272 */       params.m_newRowDidNotFitReason = 4;
/* 1273 */       return false;
/*      */     }
/*      */ 
/* 1276 */     boolean retVal = false;
/* 1277 */     ParsedQueryElements parsedElts = params.m_parsedQueryElements;
/* 1278 */     if (parsedElts == null)
/*      */     {
/* 1280 */       params.m_newRowDidNotFitReason = 1;
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 1286 */         retVal = checkRowAgainstParsedQuery(newRow, params, parsedElts);
/*      */       }
/*      */       catch (ParseStringException e)
/*      */       {
/* 1290 */         Report.trace("system", "Failed test against parsed query", e);
/* 1291 */         params.m_newRowDidNotFitReason = 4;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1297 */     if ((retVal) && (!newRow.m_dataDesign.equals(params.m_cacheDataDesign)))
/*      */     {
/* 1299 */       Report.trace("searchcache", "Row satisfied query, but does not have same data design as " + params.toString(), null);
/*      */ 
/* 1301 */       params.m_newRowDidNotFitReason = 5;
/* 1302 */       return false;
/*      */     }
/*      */ 
/* 1307 */     Object sortObject = null;
/* 1308 */     boolean hasLong = false;
/* 1309 */     boolean isReplacing = false;
/* 1310 */     long sortLong = 0L;
/* 1311 */     String sortStr = null;
/* 1312 */     if ((retVal) && (!isSorted))
/*      */     {
/* 1314 */       isReplacing = true;
/*      */     }
/* 1316 */     if ((retVal) && (isSorted))
/*      */     {
/*      */       try
/*      */       {
/* 1321 */         sortObject = getConvertedCacheRowValue(newRow, searchInfo.m_index, searchInfo.m_type, params.m_cxt);
/*      */ 
/* 1323 */         if (sortObject == null)
/*      */         {
/* 1325 */           Throwable t = new Throwable();
/* 1326 */           Report.trace("searchcache", "Sort value unexpectedly null", t);
/* 1327 */           params.m_newRowDidNotFitReason = 4;
/*      */ 
/* 1329 */           retVal = false;
/*      */         }
/*      */       }
/*      */       catch (ParseStringException e)
/*      */       {
/* 1334 */         Report.trace("system", "Failed sort order test for inclusion in cached query", e);
/* 1335 */         params.m_newRowDidNotFitReason = 4;
/* 1336 */         retVal = false;
/*      */       }
/* 1338 */       if (retVal)
/*      */       {
/* 1340 */         hasLong = sortObject instanceof Long;
/* 1341 */         if (hasLong)
/*      */         {
/* 1343 */           sortLong = ((Long)sortObject).longValue();
/*      */         }
/*      */         else
/*      */         {
/* 1347 */           sortStr = sortObject.toString();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1355 */     if ((retVal) && (!isReplacing) && (originalItem != null))
/*      */     {
/* 1357 */       if (hasLong)
/*      */       {
/* 1359 */         if (sortLong == originalItem.m_sortValueParsed)
/*      */         {
/* 1361 */           isReplacing = true;
/*      */         }
/*      */ 
/*      */       }
/* 1366 */       else if (comparePreparedStrings(params.m_cacheDataDesign, searchInfo, sortStr, originalItem.m_sortValuePrepared, params.m_cxt) == 0)
/*      */       {
/* 1369 */         isReplacing = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1374 */     if ((retVal) && (!params.m_hasAllPossibleRows) && (!isReplacing))
/*      */     {
/* 1376 */       SearchListItem[] items = params.m_cacheDocsList;
/* 1377 */       int nitems = items.length;
/* 1378 */       if (nitems > 0)
/*      */       {
/* 1385 */         SearchListItem lastItem = items[(nitems - 1)];
/*      */         int cmp;
/*      */         int cmp;
/* 1387 */         if (hasLong)
/*      */         {
/* 1389 */           long cmpLong = lastItem.m_sortValueParsed;
/*      */           int cmp;
/* 1390 */           if (cmpLong > sortLong)
/*      */           {
/* 1392 */             cmp = 1;
/*      */           }
/*      */           else
/*      */           {
/*      */             int cmp;
/* 1394 */             if (cmpLong < sortLong)
/*      */             {
/* 1396 */               cmp = -1;
/*      */             }
/*      */             else
/*      */             {
/* 1400 */               cmp = 0;
/*      */             }
/*      */           }
/*      */         }
/*      */         else {
/* 1405 */           cmp = comparePreparedStrings(newRow.m_dataDesign, searchInfo, lastItem.m_sortValuePrepared, sortStr, params.m_cxt);
/*      */         }
/*      */ 
/* 1410 */         if (params.m_sortIsAscending)
/*      */         {
/* 1414 */           cmp = -cmp;
/*      */         }
/*      */ 
/* 1419 */         if (cmp >= 0)
/*      */         {
/* 1421 */           params.m_newRowDidNotFitReason = 3;
/* 1422 */           retVal = false;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1427 */         params.m_newRowDidNotFitReason = 3;
/* 1428 */         retVal = false;
/*      */       }
/*      */     }
/*      */ 
/* 1432 */     if ((retVal) && (!isReplacing))
/*      */     {
/* 1435 */       SearchListItem newItem = new SearchListItem();
/* 1436 */       newItem.m_key = newRow.m_row[newRow.m_dataDesign.m_primaryKeyIndex];
/* 1437 */       newItem.m_sortValue = newRow.m_row[searchInfo.m_index];
/* 1438 */       newItem.m_sortValuePrepared = sortStr;
/* 1439 */       newItem.m_sortValueParsed = sortLong;
/*      */ 
/* 1441 */       params.m_tempNewDocItem = newItem;
/*      */ 
/* 1444 */       if ((originalItem != null) && 
/* 1446 */         (!checkKeysEqual(params.m_cacheDataDesign, searchInfo, newItem, originalItem, params.m_cxt)))
/*      */       {
/* 1449 */         Report.trace("system", "Search list item had key " + originalItem.m_key + " and metadata item it pointed to has key " + newItem.m_key + " and they " + "are not equal.", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1456 */     if ((!retVal) && (params.m_newRowDidNotFitReason == 0))
/*      */     {
/* 1458 */       params.m_newRowDidNotFitReason = 2;
/*      */     }
/* 1460 */     if ((retVal) && (isReplacing))
/*      */     {
/* 1462 */       params.m_newItemCanReplaceOriginal = true;
/*      */     }
/*      */ 
/* 1465 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean checkRowAgainstParsedQuery(CacheRow row, CacheUpdateParameters params, ParsedQueryElements parsedElts)
/*      */     throws ParseStringException
/*      */   {
/* 1486 */     CacheObject docListCache = params.m_activeCacheObject;
/* 1487 */     CacheDataDesign rowDesign = row.m_dataDesign;
/* 1488 */     ParsedQueryFieldMap[] cachedPointersArray = docListCache.m_parsedFieldPointersArray;
/* 1489 */     ParsedQueryFieldMap cachedPointers = null;
/* 1490 */     boolean foundIt = false;
/* 1491 */     if (cachedPointersArray == null)
/*      */     {
/* 1494 */       cachedPointersArray = new ParsedQueryFieldMap[2];
/* 1495 */       docListCache.m_parsedFieldPointersArray = cachedPointersArray;
/*      */     }
/*      */     else
/*      */     {
/* 1499 */       for (int i = 0; i < cachedPointersArray.length; ++i)
/*      */       {
/* 1501 */         ParsedQueryFieldMap tempPointers = cachedPointersArray[i];
/* 1502 */         if ((tempPointers == null) || (!tempPointers.m_cacheDataDesign.equals(rowDesign)))
/*      */           continue;
/* 1504 */         foundIt = true;
/* 1505 */         cachedPointers = tempPointers;
/*      */       }
/*      */     }
/*      */ 
/* 1509 */     if (!foundIt)
/*      */     {
/* 1513 */       int index = (rowDesign.equals(params.m_cacheDataDesign)) ? 1 : 0;
/* 1514 */       cachedPointers = new ParsedQueryFieldMap(parsedElts.m_nameToField.size(), rowDesign);
/* 1515 */       cachedPointersArray[index] = cachedPointers;
/*      */     }
/* 1517 */     if (row.m_resultRowCachedConversions == null)
/*      */     {
/* 1519 */       row.m_resultRowCachedConversions = new Object[row.m_row.length];
/*      */     }
/* 1521 */     boolean retVal = true;
/* 1522 */     if (parsedElts.m_securityClauseElement != null)
/*      */     {
/* 1524 */       retVal = checkRowAgainstQueryElement(row, params, parsedElts, parsedElts.m_securityClauseElement, cachedPointers);
/*      */     }
/*      */ 
/* 1527 */     if (retVal)
/*      */     {
/* 1529 */       retVal = checkRowAgainstQueryElement(row, params, parsedElts, parsedElts.m_searchQuery, cachedPointers);
/*      */     }
/*      */ 
/* 1532 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean checkRowAgainstQueryElement(CacheRow row, CacheUpdateParameters params, ParsedQueryElements parsedElts, QueryElement queryElement, ParsedQueryFieldMap parseFieldPointers)
/*      */     throws ParseStringException
/*      */   {
/* 1554 */     boolean retVal = false;
/*      */ 
/* 1556 */     if (queryElement.m_type == 101)
/*      */     {
/* 1558 */       List subElements = queryElement.m_subElements;
/* 1559 */       int nElts = subElements.size();
/* 1560 */       boolean isAnd = ((queryElement.m_operator & 0xFF) == 16) || (nElts == 0);
/*      */ 
/* 1562 */       boolean isNot = (queryElement.m_operator & 0x100) != 0;
/* 1563 */       retVal = isAnd;
/* 1564 */       boolean hasBadElement = false;
/* 1565 */       for (int i = 0; i < nElts; ++i)
/*      */       {
/* 1567 */         QueryElement elt = (QueryElement)subElements.get(i);
/* 1568 */         boolean retValElt = false;
/* 1569 */         if (elt.m_type == 101)
/*      */         {
/* 1572 */           retValElt = checkRowAgainstQueryElement(row, params, parsedElts, elt, parseFieldPointers);
/*      */         }
/*      */         else
/*      */         {
/* 1578 */           retValElt = checkRowAgainstQueryElementClause(row, params, elt, parseFieldPointers);
/*      */         }
/* 1580 */         if ((isAnd) && (!retValElt))
/*      */         {
/* 1582 */           retVal = false;
/* 1583 */           break;
/*      */         }
/* 1585 */         if ((isAnd) || (!retValElt))
/*      */           continue;
/* 1587 */         retVal = true;
/* 1588 */         break;
/*      */       }
/*      */ 
/* 1591 */       if ((isNot) && (params.m_newRowDidNotFitReason == 0))
/*      */       {
/* 1593 */         retVal = !retVal;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1598 */       retVal = checkRowAgainstQueryElementClause(row, params, queryElement, parseFieldPointers);
/*      */     }
/*      */ 
/* 1601 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static boolean checkRowAgainstQueryElementClause(CacheRow row, CacheUpdateParameters params, QueryElement elt, ParsedQueryFieldMap parsedFieldPointers)
/*      */     throws ParseStringException
/*      */   {
/* 1621 */     int op = elt.m_operator & 0xFF;
/* 1622 */     boolean retVal = true;
/* 1623 */     boolean computedComparison = false;
/* 1624 */     boolean isEqualsTest = false;
/* 1625 */     int compareOrder = 0;
/* 1626 */     boolean isStringTest = false;
/* 1627 */     boolean isSubstring = false;
/* 1628 */     boolean isStarts = false;
/* 1629 */     boolean isSubWord = false;
/* 1630 */     boolean isNot = (elt.m_operator & 0x100) != 0;
/* 1631 */     QueryElementField field = elt.m_field;
/* 1632 */     int type = 6;
/* 1633 */     String name = null;
/* 1634 */     boolean caseInsensitive = false;
/* 1635 */     if (field != null)
/*      */     {
/* 1637 */       type = field.m_type;
/* 1638 */       name = field.m_name;
/*      */ 
/* 1641 */       CommonSearchEngineConfig searchEngineConfig = params.m_searchConfig;
/* 1642 */       caseInsensitive = SearchLoader.isCaseInsensitiveField(name, searchEngineConfig.m_engineName);
/*      */     }
/* 1644 */     boolean isNumber = type != 6;
/* 1645 */     boolean cannotProcess = false;
/*      */ 
/* 1647 */     switch (op)
/*      */     {
/*      */     case 0:
/*      */     case 8:
/*      */     case 13:
/* 1652 */       isStringTest = !isNumber;
/* 1653 */       isEqualsTest = true;
/* 1654 */       break;
/*      */     case 1:
/* 1656 */       isStringTest = true;
/* 1657 */       isSubstring = true;
/* 1658 */       break;
/*      */     case 2:
/* 1660 */       isStringTest = true;
/* 1661 */       isStarts = true;
/* 1662 */       break;
/*      */     case 4:
/*      */     case 19:
/*      */     case 20:
/* 1666 */       isStringTest = true;
/* 1667 */       isSubWord = true;
/* 1668 */       if (op == 20)
/*      */       {
/* 1670 */         isStarts = true; } break;
/*      */     case 6:
/*      */     case 11:
/* 1675 */       compareOrder = 1;
/* 1676 */       break;
/*      */     case 7:
/*      */     case 12:
/* 1679 */       isEqualsTest = true;
/* 1680 */       compareOrder = 1;
/* 1681 */       break;
/*      */     case 10:
/*      */     case 15:
/* 1684 */       compareOrder = -1;
/* 1685 */       break;
/*      */     case 9:
/*      */     case 14:
/* 1688 */       isEqualsTest = true;
/* 1689 */       compareOrder = -1;
/* 1690 */       break;
/*      */     case 3:
/*      */     case 5:
/*      */     case 16:
/*      */     case 17:
/*      */     case 18:
/*      */     default:
/* 1692 */       cannotProcess = true;
/* 1693 */       params.m_newRowDidNotFitReason = 9;
/*      */     }
/*      */ 
/* 1696 */     int lookupIndex = -1;
/*      */ 
/* 1698 */     if (!cannotProcess)
/*      */     {
/* 1700 */       int fieldLookupIndex = elt.m_fieldIndexIntoQueryList;
/* 1701 */       int[] parsedFieldLookup = parsedFieldPointers.m_map;
/* 1702 */       if ((fieldLookupIndex >= 0) && (fieldLookupIndex < parsedFieldLookup.length))
/*      */       {
/* 1704 */         lookupIndex = parsedFieldLookup[fieldLookupIndex];
/* 1705 */         if (lookupIndex == -1)
/*      */         {
/* 1709 */           CacheDataDesign design = parsedFieldPointers.m_cacheDataDesign;
/* 1710 */           lookupIndex = design.m_templateResultSet.getFieldInfoIndex(name);
/* 1711 */           if (lookupIndex < 0)
/*      */           {
/* 1714 */             lookupIndex = -2;
/*      */           }
/* 1716 */           parsedFieldLookup[fieldLookupIndex] = lookupIndex;
/*      */         }
/*      */       }
/* 1719 */       if ((lookupIndex < 0) || (lookupIndex >= row.m_row.length))
/*      */       {
/* 1721 */         cannotProcess = true;
/* 1722 */         params.m_newRowDidNotFitReason = 10;
/*      */       }
/*      */     }
/* 1725 */     if (cannotProcess)
/*      */     {
/* 1727 */       retVal = false;
/*      */     }
/*      */ 
/* 1730 */     if (retVal)
/*      */     {
/* 1733 */       Object rowValueConverted = row.m_resultRowCachedConversions[lookupIndex];
/* 1734 */       if (rowValueConverted == null)
/*      */       {
/* 1736 */         rowValueConverted = getConvertedCacheRowValue(row, lookupIndex, field.m_type, params.m_cxt);
/*      */       }
/*      */ 
/* 1739 */       QueryElementValue eltValue = elt.m_value;
/* 1740 */       Object compareValue = eltValue.m_compareValue;
/* 1741 */       if (compareValue == null)
/*      */       {
/* 1743 */         if (isNumber)
/*      */         {
/* 1749 */           long l = NumberUtils.parseLong(eltValue.m_originalValue, 0L);
/* 1750 */           compareValue = new Long(l);
/*      */         }
/*      */         else
/*      */         {
/* 1754 */           compareValue = prepareForComparison(row.m_dataDesign, lookupIndex, type, eltValue.m_originalValue, op, params.m_cxt);
/*      */         }
/*      */ 
/* 1757 */         eltValue.m_compareValue = compareValue;
/*      */       }
/* 1759 */       if (isStringTest)
/*      */       {
/* 1762 */         if (!rowValueConverted instanceof String)
/*      */         {
/* 1764 */           params.m_newRowDidNotFitReason = 4;
/* 1765 */           retVal = false;
/* 1766 */           computedComparison = true;
/*      */         }
/*      */ 
/* 1769 */         if (!computedComparison)
/*      */         {
/* 1771 */           String rowValueStr = (String)rowValueConverted;
/* 1772 */           String compareValueStr = (String)compareValue;
/* 1773 */           if (caseInsensitive)
/*      */           {
/* 1775 */             rowValueStr = rowValueStr.toLowerCase();
/* 1776 */             compareValueStr = compareValueStr.toLowerCase();
/*      */           }
/* 1778 */           if ((compareOrder != 0) || (isEqualsTest))
/*      */           {
/* 1780 */             int rowCompare = compareValueStr.compareTo(rowValueStr);
/* 1781 */             if (compareOrder < 0)
/*      */             {
/* 1783 */               rowCompare = -rowCompare;
/*      */             }
/* 1785 */             if (((rowCompare > 0) && (compareOrder != 0)) || ((rowCompare == 0) && (isEqualsTest)))
/*      */             {
/* 1787 */               computedComparison = true;
/*      */             }
/*      */           }
/* 1790 */           if ((!computedComparison) && (((isSubstring) || (isSubWord) || (isStarts))))
/*      */           {
/* 1792 */             int cl = compareValueStr.length();
/* 1793 */             if (cl == 0)
/*      */             {
/* 1795 */               computedComparison = true;
/* 1796 */               retVal = false;
/*      */             }
/*      */             else
/*      */             {
/* 1800 */               char firstCompareChar = compareValueStr.charAt(0);
/* 1801 */               int rl = rowValueStr.length();
/* 1802 */               boolean isPureStarts = (!isSubWord) && (isStarts);
/* 1803 */               int endPoint = rl - cl + 1;
/* 1804 */               for (int i = 0; i < endPoint; ++i)
/*      */               {
/* 1806 */                 char rowValueChar = rowValueStr.charAt(i);
/*      */ 
/* 1810 */                 if ((isSubWord) && (!Character.isLetterOrDigit(rowValueChar)))
/*      */                 {
/*      */                   continue;
/*      */                 }
/*      */ 
/* 1815 */                 if (rowValueChar == firstCompareChar)
/*      */                 {
/* 1818 */                   int rowValueIndex = i + 1;
/* 1819 */                   boolean matchedAll = true;
/* 1820 */                   for (int j = 1; (j < cl) && (rowValueIndex < rl); ++j)
/*      */                   {
/* 1822 */                     if (rowValueStr.charAt(rowValueIndex++) == compareValueStr.charAt(j))
/*      */                       continue;
/* 1824 */                     matchedAll = false;
/* 1825 */                     break;
/*      */                   }
/*      */ 
/* 1828 */                   if (matchedAll)
/*      */                   {
/* 1830 */                     if ((isSubWord) && (!isStarts) && 
/* 1833 */                       (rowValueIndex < rl) && (Character.isLetterOrDigit(rowValueStr.charAt(rowValueIndex))))
/*      */                     {
/*      */                       continue;
/*      */                     }
/*      */ 
/* 1839 */                     computedComparison = true;
/* 1840 */                     break;
/*      */                   }
/*      */                 }
/* 1843 */                 if (isPureStarts) {
/*      */                   break;
/*      */                 }
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1852 */           if (!computedComparison)
/*      */           {
/* 1854 */             int len = compareValueStr.length();
/* 1855 */             boolean isUnusual = false;
/* 1856 */             for (int i = 0; i < len; ++i)
/*      */             {
/* 1858 */               char ch = compareValueStr.charAt(i);
/* 1859 */               if (ch >= '') {
/*      */                 continue;
/*      */               }
/*      */ 
/* 1863 */               if ((ch == '%') || (ch == '*') || (ch == '?') || (ch == '_'))
/*      */               {
/* 1865 */                 isUnusual = true;
/* 1866 */                 break;
/*      */               }
/* 1868 */               if ((!isSubWord) || 
/* 1871 */                 ((ch >= 'a') && (ch <= 'z')) || ((ch >= 'A') && (ch <= 'Z')) || (
/* 1871 */                 (ch >= '0') && (ch <= '9')))
/*      */                 continue;
/* 1873 */               isUnusual = true;
/* 1874 */               break;
/*      */             }
/*      */ 
/* 1879 */             if (isUnusual)
/*      */             {
/* 1881 */               cannotProcess = true;
/* 1882 */               params.m_newRowDidNotFitReason = 11;
/* 1883 */               computedComparison = true;
/* 1884 */               retVal = false;
/* 1885 */               Report.trace("searchcache", "Ambiguous search term invalidating result of query element, elt=" + elt, null);
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1893 */         if (!rowValueConverted instanceof Long)
/*      */         {
/* 1895 */           params.m_newRowDidNotFitReason = 4;
/* 1896 */           retVal = false;
/* 1897 */           computedComparison = true;
/*      */         }
/*      */ 
/* 1900 */         if (!computedComparison)
/*      */         {
/* 1902 */           Long rowValueLong = (Long)rowValueConverted;
/* 1903 */           Long compareValueLong = (Long)compareValue;
/* 1904 */           int rowCompare = rowValueLong.compareTo(compareValueLong);
/* 1905 */           if (compareOrder < 0)
/*      */           {
/* 1907 */             rowCompare = -rowCompare;
/*      */           }
/* 1909 */           if (((rowCompare > 0) && (compareOrder != 0)) || ((rowCompare == 0) && (isEqualsTest)))
/*      */           {
/* 1911 */             computedComparison = true;
/*      */           }
/*      */         }
/*      */       }
/* 1915 */       if ((!computedComparison) && (params.m_newRowDidNotFitReason == 0))
/*      */       {
/* 1917 */         computedComparison = true;
/* 1918 */         retVal = false;
/*      */       }
/* 1920 */       if (Report.m_verbose)
/*      */       {
/* 1922 */         Report.debug("searchcache", "Term element field=" + field.m_name + ",type=" + field.m_type + ",rowValue=" + rowValueConverted + ",op=" + op + ",isNot=" + isNot + ",compareValue=" + compareValue + ",computedComparison=" + computedComparison + ",retVal=" + retVal + ",newRowDidNotFitReasons=" + params.m_newRowDidNotFitReason, null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1929 */     if ((computedComparison) && (isNot))
/*      */     {
/* 1931 */       retVal = !retVal;
/*      */     }
/*      */ 
/* 1934 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Object getConvertedCacheRowValue(CacheRow row, int index, int type, ExecutionContext cxt)
/*      */     throws ParseStringException
/*      */   {
/* 1954 */     Object result = null;
/* 1955 */     if (row.m_resultRowCachedConversions == null)
/*      */     {
/* 1957 */       row.m_resultRowCachedConversions = new Object[row.m_row.length];
/*      */     }
/*      */     else
/*      */     {
/* 1961 */       result = row.m_resultRowCachedConversions[index];
/*      */     }
/* 1963 */     if (result == null)
/*      */     {
/* 1965 */       long l = 0L;
/* 1966 */       String str = null;
/* 1967 */       boolean hasLong = false;
/* 1968 */       String val = row.m_row[index];
/* 1969 */       if (val != null)
/*      */       {
/* 1971 */         if (type == 5)
/*      */         {
/* 1973 */           IdcDateFormat dateFormat = row.m_dataDesign.m_templateResultSet.getDateFormat();
/* 1974 */           Date d = dateFormat.parseDate(val);
/* 1975 */           l = d.getTime();
/* 1976 */           hasLong = true;
/*      */         }
/* 1978 */         else if (type == 3)
/*      */         {
/* 1980 */           l = NumberUtils.parseLong(val, 0L);
/* 1981 */           hasLong = true;
/*      */         }
/*      */         else
/*      */         {
/* 1985 */           str = prepareForComparison(row.m_dataDesign, index, type, val, 0, cxt);
/*      */         }
/*      */ 
/* 1988 */         if (hasLong)
/*      */         {
/* 1990 */           result = new Long(l);
/*      */         }
/*      */         else
/*      */         {
/* 1994 */           result = str;
/*      */         }
/* 1996 */         row.m_resultRowCachedConversions[index] = result;
/*      */       }
/*      */     }
/* 1999 */     return result;
/*      */   }
/*      */ 
/*      */   public static void mergeAndSortCachedDocList(CacheUpdateParameters params)
/*      */   {
/* 2010 */     CacheDataDesign dataDesign = params.m_cacheDataDesign;
/* 2011 */     HashMap[] navCountUpdates = null;
/* 2012 */     List newItems = params.m_newDocsList;
/* 2013 */     List newItemsMetadata = params.m_newDocsListMetadata;
/* 2014 */     int newItemsLength = (newItems != null) ? newItems.size() : 0;
/*      */ 
/* 2016 */     SearchListItem[] oldList = params.m_cacheDocsList;
/* 2017 */     CacheRow[] oldListMetadata = params.m_cacheDocsListMetadata;
/* 2018 */     int oldListLen = 0;
/* 2019 */     params.m_cacheNumDeletedRows = 0;
/* 2020 */     if ((oldList != null) && (oldList.length > 0))
/*      */     {
/* 2022 */       for (int i = 0; i < oldList.length; ++i)
/*      */       {
/* 2024 */         if (oldList[i] != null)
/*      */         {
/* 2026 */           ++oldListLen;
/*      */         }
/*      */         else
/*      */         {
/* 2030 */           if (oldListMetadata[i] == null)
/*      */             continue;
/* 2032 */           navCountUpdates = calculateNavCountChange(oldListMetadata[i].m_capturedPerQueryValues, navCountUpdates, -1);
/*      */ 
/* 2034 */           oldListMetadata[i] = null;
/*      */         }
/*      */       }
/*      */ 
/* 2038 */       params.m_cacheNumDeletedRows = (oldList.length - oldListLen);
/*      */     }
/* 2040 */     SearchListItem[] newList = null;
/* 2041 */     CacheRow[] newMetadata = null;
/* 2042 */     assert ((oldList == null) || (oldList.length == 0) || (oldList.length == oldListMetadata.length));
/*      */ 
/* 2044 */     if ((newItemsLength > 0) || ((oldList != null) && (oldListLen < oldList.length)))
/*      */     {
/* 2046 */       if ((!params.m_cacheDocsListLookupValid) && (newItemsLength > 0))
/*      */       {
/* 2048 */         prepareHashMapLookupForDocList(params, dataDesign);
/*      */       }
/* 2050 */       Map oldListMap = params.m_cacheDocsListLookup;
/*      */ 
/* 2052 */       int allocMergeLen = oldListLen + newItemsLength;
/* 2053 */       List mergedList = new ArrayList(allocMergeLen);
/* 2054 */       List mergedListMetadata = new ArrayList(allocMergeLen);
/* 2055 */       int numTotalItems = 0;
/* 2056 */       if (oldListLen > 0)
/*      */       {
/* 2058 */         for (int i = 0; i < oldList.length; ++i)
/*      */         {
/* 2060 */           SearchListItem item = oldList[i];
/* 2061 */           if (item == null) {
/*      */             continue;
/*      */           }
/*      */ 
/* 2065 */           item.m_rowIndex = (numTotalItems++);
/* 2066 */           mergedList.add(item);
/* 2067 */           mergedListMetadata.add(oldListMetadata[i]);
/*      */         }
/*      */       }
/*      */ 
/* 2071 */       for (int i = 0; i < newItemsLength; ++i)
/*      */       {
/* 2073 */         SearchListItem item = (SearchListItem)newItems.get(i);
/* 2074 */         CacheRow itemMetadata = (CacheRow)newItemsMetadata.get(i);
/* 2075 */         String key = preparePrimaryKeyForIndexing(dataDesign, item.m_key, params.m_cxt);
/*      */ 
/* 2079 */         SearchListItem prevItem = (SearchListItem)oldListMap.get(key);
/* 2080 */         if (prevItem == null)
/*      */         {
/* 2082 */           mergedList.add(item);
/* 2083 */           mergedListMetadata.add(itemMetadata);
/* 2084 */           oldListMap.put(key, item);
/* 2085 */           item.m_rowIndex = (numTotalItems++);
/*      */ 
/* 2087 */           navCountUpdates = calculateNavCountChange(itemMetadata.m_capturedPerQueryValues, navCountUpdates, 1);
/*      */         }
/*      */         else
/*      */         {
/* 2094 */           int rowIndex = prevItem.m_rowIndex;
/* 2095 */           item.m_rowIndex = rowIndex;
/* 2096 */           mergedList.set(rowIndex, item);
/* 2097 */           mergedListMetadata.set(rowIndex, itemMetadata);
/*      */         }
/*      */       }
/* 2100 */       newList = new SearchListItem[mergedList.size()];
/* 2101 */       mergedList.toArray(newList);
/* 2102 */       newMetadata = new CacheRow[mergedList.size()];
/* 2103 */       mergedListMetadata.toArray(newMetadata);
/*      */     }
/* 2105 */     else if (oldList != null)
/*      */     {
/* 2107 */       newList = oldList;
/* 2108 */       newMetadata = oldListMetadata;
/* 2109 */       for (int i = 0; i < newList.length; ++i)
/*      */       {
/* 2111 */         newList[i].m_rowIndex = i;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 2116 */     if ((newList != null) && (((params.m_cacheDocsListNeedsSorting) || (newItemsLength > 0))) && (params.m_isSorted))
/*      */     {
/* 2119 */       if (Report.m_verbose)
/*      */       {
/* 2121 */         Report.debug("searchcache", "Sorting fixed up cache for query=" + params.m_query, null);
/*      */       }
/* 2123 */       SearchListItemComparator comparator = new SearchListItemComparator(dataDesign, params.m_cacheSortFieldInfo, (params.m_sortIsAscending) ? 0 : 1, params.m_cxt);
/*      */ 
/* 2128 */       Sort.sort(newList, comparator);
/*      */ 
/* 2131 */       CacheRow[] newSortListMetadata = new CacheRow[newMetadata.length];
/* 2132 */       for (int i = 0; i < newList.length; ++i)
/*      */       {
/* 2135 */         newSortListMetadata[i] = newMetadata[newList[i].m_rowIndex];
/*      */ 
/* 2139 */         newList[i].m_rowIndex = i;
/*      */       }
/* 2141 */       newMetadata = newSortListMetadata;
/*      */     }
/*      */ 
/* 2144 */     updateNavCountChange(params.m_extraResultSets, navCountUpdates, dataDesign);
/*      */ 
/* 2146 */     params.m_cacheDocsList = newList;
/* 2147 */     params.m_cacheDocsListMetadata = newMetadata;
/* 2148 */     params.m_cacheDocsListNeedsSorting = false;
/* 2149 */     params.m_cacheDocsListNeedsMerging = false;
/*      */   }
/*      */ 
/*      */   public static void updateNavCountChange(HashMap<String, DataResultSet> resultSets, HashMap<String, Integer>[] countUpdates, CacheDataDesign dataDesign)
/*      */   {
/* 2156 */     if ((resultSets == null) || (countUpdates == null))
/*      */     {
/* 2158 */       return;
/*      */     }
/*      */ 
/* 2161 */     for (int i = 0; i < countUpdates.length; ++i)
/*      */     {
/* 2163 */       String fieldName = dataDesign.m_perQueryFields[i].m_name;
/* 2164 */       DataResultSet rset = (DataResultSet)resultSets.get("SearchResultNavigation" + fieldName);
/* 2165 */       if (rset == null) {
/*      */         continue;
/*      */       }
/*      */ 
/* 2169 */       FieldInfo countField = new FieldInfo();
/* 2170 */       rset.getFieldInfo("count", countField);
/* 2171 */       FieldInfo drillDownField = new FieldInfo();
/* 2172 */       rset.getFieldInfo("drillDownOptionValue", drillDownField);
/* 2173 */       if (countField.m_index < 0)
/*      */       {
/* 2176 */         return;
/*      */       }
/*      */ 
/* 2179 */       for (Map.Entry entry : countUpdates[i].entrySet())
/*      */       {
/* 2181 */         String key = (String)entry.getKey();
/* 2182 */         Integer diff = (Integer)entry.getValue();
/*      */ 
/* 2184 */         if (rset.findRow(drillDownField.m_index, key) != null)
/*      */         {
/* 2186 */           String oldCountStr = rset.getStringValue(countField.m_index);
/* 2187 */           int count = NumberUtils.parseInteger(oldCountStr, 0);
/* 2188 */           count += diff.intValue();
/*      */           try
/*      */           {
/* 2191 */             rset.setCurrentValue(countField.m_index, "" + count);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 2196 */             Report.trace("searchcache", "Unable to update navigation count for value:" + key + " of field " + fieldName, e);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/* 2201 */           IdcVector row = new IdcVector();
/* 2202 */           row.add(key);
/* 2203 */           row.add(key);
/* 2204 */           row.add("" + diff);
/* 2205 */           row.add(fieldName);
/* 2206 */           rset.addRow(row);
/*      */         }
/*      */       }
/*      */       try
/*      */       {
/* 2211 */         ResultSetUtils.sortResultSet(rset, new String[] { "drillDownOptionValue" });
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 2215 */         Report.trace("searchcache", "Unable to sort navigation result set for field '" + fieldName + "'", e);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static HashMap[] calculateNavCountChange(String[] item, HashMap<String, Integer>[] updateCounts, int count)
/*      */   {
/* 2223 */     if (item == null)
/*      */     {
/* 2225 */       return updateCounts;
/*      */     }
/* 2227 */     if (updateCounts == null)
/*      */     {
/* 2229 */       updateCounts = new HashMap[item.length];
/*      */     }
/*      */ 
/* 2232 */     for (int i = 0; i < updateCounts.length; ++i)
/*      */     {
/* 2234 */       if (updateCounts[i] == null)
/*      */       {
/* 2236 */         updateCounts[i] = new HashMap();
/*      */       }
/* 2238 */       String key = item[i];
/* 2239 */       if (key == null)
/*      */       {
/* 2241 */         key = "";
/*      */       }
/* 2243 */       Integer curCount = (Integer)updateCounts[i].get(key);
/* 2244 */       if (curCount == null)
/*      */       {
/* 2246 */         curCount = Integer.valueOf(count);
/*      */       }
/*      */       else
/*      */       {
/* 2250 */         curCount = Integer.valueOf(curCount.intValue() + count);
/*      */       }
/* 2252 */       updateCounts[i].put(key, curCount);
/*      */     }
/* 2254 */     return updateCounts;
/*      */   }
/*      */ 
/*      */   public static void prepareHashMapLookupForDocList(CacheUpdateParameters params, CacheDataDesign dataDesign)
/*      */   {
/* 2267 */     SearchListItem[] docsList = params.m_cacheDocsList;
/* 2268 */     Map m = params.m_cacheDocsListLookup;
/* 2269 */     int docsListLen = (docsList != null) ? docsList.length : 0;
/* 2270 */     if (m == null)
/*      */     {
/* 2273 */       m = new HashMap(docsListLen + 10);
/* 2274 */       params.m_cacheDocsListLookup = m;
/*      */     }
/*      */     else
/*      */     {
/* 2278 */       m.clear();
/*      */     }
/*      */ 
/* 2281 */     for (int i = 0; i < docsListLen; ++i)
/*      */     {
/* 2286 */       SearchListItem item = docsList[i];
/* 2287 */       if (item == null)
/*      */         continue;
/* 2289 */       item.m_rowIndex = i;
/* 2290 */       String key = preparePrimaryKeyForIndexing(dataDesign, item.m_key, params.m_cxt);
/* 2291 */       m.put(key, item);
/*      */     }
/*      */ 
/* 2294 */     params.m_cacheDocsListLookupValid = true;
/*      */   }
/*      */ 
/*      */   public static String prepareForComparison(CacheDataDesign dataDesign, int index, int type, String str, int opCode, ExecutionContext cxt)
/*      */   {
/* 2316 */     return str.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH);
/*      */   }
/*      */ 
/*      */   public static String preparePrimaryKeyForIndexing(CacheDataDesign dataDesign, String key, ExecutionContext cxt)
/*      */   {
/* 2332 */     return key;
/*      */   }
/*      */ 
/*      */   public static int comparePreparedStrings(CacheDataDesign dataDesign, FieldInfo fieldInfo, String str1, String str2, ExecutionContext cxt)
/*      */   {
/* 2350 */     return str1.compareTo(str2);
/*      */   }
/*      */ 
/*      */   public static boolean checkKeysEqual(CacheDataDesign dataDesign, FieldInfo fieldInfo, SearchListItem item1, SearchListItem item2, ExecutionContext cxt)
/*      */   {
/* 2370 */     return item1.m_key.equals(item2.m_key);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2375 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98270 $";
/*      */   }
/*      */ 
/*      */   static
/*      */   {
/*   71 */     NEW_ROW_FAIL_STRINGS = new String[] { "newRowFit", "newRowUnparsedQuery", "newRowFailsQuery", "newRowOutsideListRange", "newRowHasBadData", "newRowMismatchedDesign", "newRowHasBeenDeleted", "newRowIsNotInChangeRange", "newRowChangeDataIsInvalid", "newRowQueryHasUntestableOp", "newRowMissingFieldInQuery", "newRowQueryHasUntestableData", "newRowCacheDoesNotAllowRepair" };
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.SearchCacheUtils
 * JD-Core Version:    0.5.4
 */