/*      */ package intradoc.shared.schema;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcAppender;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.shared.LRUManager;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcAppendableBase;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaCacheItem
/*      */   implements IdcAppender
/*      */ {
/*   34 */   protected static int m_objectCount = 0;
/*   35 */   protected static LRUManager m_manager = (LRUManager)SharedObjects.getObject("globalObjects", "SchemaViewLRUManager");
/*      */ 
/*   40 */   protected boolean m_isValid = false;
/*      */   protected boolean m_isAllValues;
/*   42 */   protected boolean m_isDirty = false;
/*      */   protected int m_flags;
/*      */   protected String m_viewName;
/*      */   protected String m_viewVersion;
/*      */   protected String m_tableVersion;
/*      */   protected String m_relationName;
/*      */   protected String m_relationVersion;
/*      */   protected String[] m_parentValues;
/*   51 */   protected boolean m_isComplete = false;
/*      */   protected Map m_rows;
/*      */   protected DataResultSet m_resultSet;
/*      */   protected long m_timestamp;
/*   55 */   protected int m_size = 0;
/*   56 */   protected Map m_cachedWork = new HashMap();
/*      */ 
/*   58 */   protected SchemaViewConfig m_viewConfig = null;
/*   59 */   protected SchemaTableConfig m_tableConfig = null;
/*   60 */   protected SchemaRelationConfig m_relationConfig = null;
/*      */ 
/*   62 */   public Hashtable m_externalData = new Hashtable();
/*      */ 
/*   64 */   public static String PRIMARY_KEY_STRING = "primaryKey";
/*   65 */   public static String RELATION_KEY_STRING = "relation";
/*   66 */   public static String FIELD_KEY_STRING = "field";
/*   67 */   public static String SORT_KEY_STRING = "sort";
/*      */ 
/*   69 */   public static String SCHEMA_ARGS = "schema:arguments";
/*      */ 
/*   72 */   protected static char HASH_SEPARATOR = 65535;
/*      */ 
/*      */   public SchemaCacheItem()
/*      */   {
/*   79 */     m_objectCount += 1;
/*   80 */     this.m_isValid = false;
/*      */   }
/*      */ 
/*      */   public SchemaCacheItem(SchemaViewData myView)
/*      */   {
/*   89 */     m_objectCount += 1;
/*   90 */     setCacheInfo(myView);
/*      */   }
/*      */ 
/*      */   public void setCacheInfo(SchemaViewData myView)
/*      */   {
/*   95 */     this.m_isAllValues = true;
/*   96 */     this.m_viewName = myView.m_name;
/*   97 */     this.m_relationName = null;
/*   98 */     this.m_parentValues = null;
/*      */ 
/*  100 */     this.m_viewVersion = myView.get("schVersion");
/*      */   }
/*      */ 
/*      */   public SchemaCacheItem(SchemaViewData myView, SchemaRelationData relationship, String[] parentValues, int flags)
/*      */   {
/*  106 */     m_objectCount += 1;
/*  107 */     setCacheInfo(myView, relationship, parentValues, flags);
/*      */   }
/*      */ 
/*      */   public void setCacheInfo(SchemaViewData myView, SchemaRelationData relationship, String[] parentValues, int flags)
/*      */   {
/*  113 */     this.m_viewName = myView.m_name;
/*  114 */     this.m_relationName = relationship.m_name;
/*  115 */     this.m_parentValues = parentValues;
/*  116 */     this.m_flags = flags;
/*      */ 
/*  118 */     this.m_viewVersion = myView.get("schVersion");
/*  119 */     this.m_relationVersion = relationship.get("schVersion");
/*      */   }
/*      */ 
/*      */   public void appendTo(IdcAppendable appendable)
/*      */   {
/*  124 */     appendTo(appendable);
/*      */   }
/*      */ 
/*      */   public void appendTo(IdcAppendableBase appendable)
/*      */   {
/*  129 */     appendable.append(super.toString());
/*  130 */     appendable.append(" on ");
/*  131 */     appendable.append(this.m_viewName);
/*  132 */     appendable.append("(");
/*  133 */     appendable.append(this.m_viewVersion);
/*  134 */     appendable.append(".");
/*  135 */     appendable.append(this.m_tableVersion);
/*  136 */     appendable.append(")");
/*  137 */     if (this.m_relationName != null)
/*      */     {
/*  139 */       appendable.append("/");
/*  140 */       appendable.append(this.m_relationName);
/*  141 */       appendable.append("(");
/*  142 */       appendable.append(this.m_relationVersion);
/*  143 */       appendable.append(")");
/*  144 */       for (int i = 0; i < this.m_parentValues.length; ++i)
/*      */       {
/*  146 */         appendable.append(":");
/*  147 */         appendable.append(this.m_parentValues[i]);
/*      */       }
/*  149 */       appendable.append(":");
/*      */     }
/*  151 */     appendable.append(" timestamp: ");
/*  152 */     appendable.append("" + this.m_timestamp);
/*  153 */     if (this.m_flags != 0)
/*      */     {
/*  155 */       appendable.append(" flags: ");
/*  156 */       appendable.append("" + this.m_flags);
/*      */     }
/*  158 */     appendable.append(", rows: ");
/*  159 */     if (this.m_resultSet != null)
/*      */     {
/*  161 */       appendable.append("" + this.m_resultSet.getNumRows());
/*      */     }
/*      */     else
/*      */     {
/*  165 */       appendable.append("null");
/*      */     }
/*  167 */     appendable.append(", size: " + this.m_size);
/*      */   }
/*      */ 
/*      */   public void writeTo(Writer w) throws IOException
/*      */   {
/*  172 */     FileUtils.appendToWriter(this, w);
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/*  178 */     return StringUtils.appenderToString(this);
/*      */   }
/*      */ 
/*      */   public boolean isValid()
/*      */   {
/*  184 */     return this.m_isValid;
/*      */   }
/*      */ 
/*      */   public int getSize()
/*      */   {
/*  189 */     return this.m_size;
/*      */   }
/*      */ 
/*      */   public int getFlags()
/*      */   {
/*  194 */     return this.m_flags;
/*      */   }
/*      */ 
/*      */   public int hashCode()
/*      */   {
/*  200 */     int value = 0;
/*  201 */     if (this.m_isAllValues)
/*      */     {
/*  203 */       value = this.m_viewName.hashCode();
/*      */     }
/*      */     else
/*      */     {
/*  207 */       value = this.m_viewName.hashCode() * this.m_relationName.hashCode();
/*  208 */       for (int i = 0; i < this.m_parentValues.length; ++i)
/*      */       {
/*  210 */         value *= this.m_parentValues[i].hashCode();
/*      */       }
/*      */     }
/*  213 */     value += this.m_flags;
/*  214 */     return value;
/*      */   }
/*      */ 
/*      */   public boolean equals(Object obj)
/*      */   {
/*  220 */     boolean rc = true;
/*  221 */     if (obj instanceof SchemaCacheItem)
/*      */     {
/*  223 */       SchemaCacheItem key = (SchemaCacheItem)obj;
/*  224 */       if (!key.m_viewName.equals(this.m_viewName))
/*      */       {
/*  226 */         return false;
/*      */       }
/*  228 */       if (this.m_isAllValues)
/*      */       {
/*  230 */         rc = key.m_isAllValues;
/*      */       }
/*  232 */       else if (this.m_parentValues.length != key.m_parentValues.length)
/*      */       {
/*  234 */         rc = false;
/*      */       }
/*  236 */       else if (!this.m_relationName.equals(key.m_relationName))
/*      */       {
/*  238 */         rc = false;
/*      */       }
/*  240 */       else if (this.m_flags != key.m_flags)
/*      */       {
/*  242 */         rc = false;
/*      */       }
/*      */       else
/*      */       {
/*  246 */         for (int i = 0; i < this.m_parentValues.length; ++i)
/*      */         {
/*  248 */           if (this.m_parentValues[i].equals(key.m_parentValues[i]))
/*      */             continue;
/*  250 */           rc = false;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  255 */       return rc;
/*      */     }
/*  257 */     return false;
/*      */   }
/*      */ 
/*      */   public void initShallow(SchemaCacheItem source)
/*      */   {
/*  262 */     this.m_isValid = source.m_isValid;
/*  263 */     this.m_isAllValues = source.m_isAllValues;
/*  264 */     this.m_isDirty = source.m_isDirty;
/*  265 */     this.m_viewName = source.m_viewName;
/*  266 */     this.m_viewVersion = source.m_viewVersion;
/*  267 */     this.m_tableVersion = source.m_tableVersion;
/*  268 */     this.m_relationName = source.m_relationName;
/*  269 */     this.m_relationVersion = source.m_relationVersion;
/*  270 */     this.m_parentValues = source.m_parentValues;
/*      */ 
/*  272 */     this.m_isComplete = source.m_isComplete;
/*  273 */     this.m_rows = source.m_rows;
/*  274 */     this.m_resultSet = source.m_resultSet;
/*  275 */     this.m_timestamp = source.m_timestamp;
/*  276 */     this.m_size = source.m_size;
/*  277 */     this.m_cachedWork = source.m_cachedWork;
/*      */ 
/*  279 */     this.m_viewConfig = source.m_viewConfig;
/*  280 */     this.m_tableConfig = source.m_tableConfig;
/*  281 */     this.m_relationConfig = source.m_relationConfig;
/*      */ 
/*  283 */     this.m_externalData = source.m_externalData;
/*      */ 
/*  287 */     if (m_manager == null) {
/*      */       return;
/*      */     }
/*  290 */     int currentUsage = m_manager.getCurrentUsage();
/*  291 */     int maxUsage = m_manager.getMaximumUsage();
/*  292 */     double schemaCachePercentageUsed = currentUsage * 100 / maxUsage;
/*  293 */     String schemaCachePercentageUsedString = Double.toString(schemaCachePercentageUsed);
/*  294 */     int endIndex = schemaCachePercentageUsedString.indexOf(46) + 3;
/*  295 */     if (endIndex > schemaCachePercentageUsedString.length())
/*      */     {
/*  297 */       endIndex = schemaCachePercentageUsedString.length();
/*      */     }
/*  299 */     schemaCachePercentageUsedString = schemaCachePercentageUsedString.substring(0, endIndex);
/*  300 */     String amountUsed = currentUsage + " bytes(" + schemaCachePercentageUsedString + "%)";
/*  301 */     Report.trace("schemacache", null, "csMonitorSchemaCacheObjectCount", new Object[] { Integer.valueOf(m_objectCount) });
/*  302 */     Report.trace("schemacache", null, "csMonitorSchemaCacheAmountUsed", new Object[] { amountUsed });
/*      */   }
/*      */ 
/*      */   public synchronized ResultSet getResultSet()
/*      */   {
/*  308 */     if (this.m_resultSet == null)
/*      */     {
/*  310 */       return null;
/*      */     }
/*  312 */     ResultSet rset = this.m_resultSet.shallowClone();
/*  313 */     return rset;
/*      */   }
/*      */ 
/*      */   public ResultSet getViewValues(Map args)
/*      */     throws DataException
/*      */   {
/*  323 */     HashableStringArray hashableKey = computeCacheKey(args);
/*  324 */     if ((hashableKey == null) || (hashableKey.m_array == null) || (hashableKey.m_array.length == 0))
/*      */     {
/*  327 */       return getResultSet();
/*      */     }
/*      */ 
/*  330 */     SchemaCacheEntry cacheEntry = getCacheEntryFromKey(hashableKey);
/*      */ 
/*  332 */     String[] primaryKeyValues = (String[])(String[])args.get("primaryKey");
/*  333 */     if ((cacheEntry == null) && (primaryKeyValues == null))
/*      */     {
/*  337 */       cacheEntry = constructCacheEntry(args);
/*      */     }
/*      */ 
/*  340 */     DataResultSet drset = null;
/*  341 */     if ((cacheEntry != null) && (cacheEntry.m_rows != null))
/*      */     {
/*  343 */       drset = new DataResultSet();
/*  344 */       drset.copyFieldInfo(this.m_resultSet);
/*  345 */       drset.setRows(cacheEntry.m_rows);
/*      */     }
/*      */ 
/*  348 */     return drset;
/*      */   }
/*      */ 
/*      */   public SchemaCacheEntry constructCacheEntry(Map args) throws DataException
/*      */   {
/*  353 */     String[] filterFieldNames = (String[])(String[])args.get("fieldNames");
/*  354 */     String[] filterFieldValues = (String[])(String[])args.get("fieldValues");
/*  355 */     String[] sortFields = (String[])(String[])args.get("sortFields");
/*  356 */     String[] sortOrders = (String[])(String[])args.get("sortOrders");
/*      */ 
/*  358 */     SchemaCacheEntry cacheEntry = null;
/*      */ 
/*  360 */     if ((filterFieldNames != null) && (filterFieldNames.length > 0) && 
/*  362 */       (filterFieldNames.length == 1) && 
/*  364 */       (!this.m_externalData.containsKey("loaderHandled:" + FIELD_KEY_STRING + ":" + filterFieldNames[0])))
/*      */     {
/*  369 */       if (this.m_viewConfig == null)
/*      */       {
/*  371 */         this.m_viewConfig = ((SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig"));
/*      */       }
/*      */ 
/*  374 */       SchemaViewData viewDef = (SchemaViewData)this.m_viewConfig.getData(this.m_viewName);
/*  375 */       String primaryKey = viewDef.get("schInternalColumn");
/*  376 */       if (primaryKey.equals(filterFieldNames[0]))
/*      */       {
/*  380 */         Map newArgs = new HashMap();
/*  381 */         newArgs.put("primaryKey", new String[] { filterFieldValues[0] });
/*  382 */         cacheEntry = getCacheEntry(newArgs);
/*      */       }
/*      */       else
/*      */       {
/*  386 */         if (!isFieldIndexed(filterFieldNames[0]))
/*      */         {
/*  388 */           addIndexToField(filterFieldNames[0]);
/*      */         }
/*      */ 
/*  391 */         Map newArgs = new HashMap();
/*  392 */         newArgs.put("fieldNames", filterFieldNames);
/*  393 */         newArgs.put("fieldValues", filterFieldValues);
/*  394 */         cacheEntry = getCacheEntry(newArgs);
/*      */       }
/*      */ 
/*  397 */       if (cacheEntry == null)
/*      */       {
/*  402 */         HashableStringArray hashableKey = computeCacheKey(args);
/*  403 */         synchronized (this)
/*      */         {
/*  405 */           cacheEntry = new SchemaCacheEntry();
/*  406 */           cacheEntry.m_rows = new ArrayList();
/*  407 */           this.m_rows.put(hashableKey, cacheEntry);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  419 */     if ((sortFields != null) && (sortFields.length > 0) && 
/*  422 */       (!this.m_externalData.containsKey("loaderHandled:" + SORT_KEY_STRING + ":" + sortFields[0])))
/*      */     {
/*  424 */       synchronized (this)
/*      */       {
/*  426 */         List rows = null;
/*  427 */         if (cacheEntry != null)
/*      */         {
/*  429 */           rows = cacheEntry.m_rows;
/*      */         }
/*      */         else
/*      */         {
/*  433 */           rows = new ArrayList();
/*  434 */           for (this.m_resultSet.first(); this.m_resultSet.isRowPresent(); this.m_resultSet.next())
/*      */           {
/*  436 */             rows.add(this.m_resultSet.getCurrentRowValues());
/*      */           }
/*      */         }
/*      */ 
/*  440 */         List sortedRows = new ArrayList(rows);
/*  441 */         DataResultSet sortedSet = new DataResultSet();
/*  442 */         sortedSet.copyFieldInfo(this.m_resultSet);
/*  443 */         sortedSet.setRows(new ArrayList(cacheEntry.m_rows));
/*      */ 
/*  445 */         FieldInfo fi = new FieldInfo();
/*  446 */         sortedSet.getFieldInfo(sortFields[0], fi);
/*      */ 
/*  448 */         ResultSetTreeSort treeSort = new ResultSetTreeSort(sortedSet, fi.m_index, false);
/*  449 */         treeSort.determineFieldType(null);
/*  450 */         if (sortOrders[0].equalsIgnoreCase("asc"))
/*      */         {
/*  452 */           treeSort.m_isAscending = true;
/*      */         }
/*      */         else
/*      */         {
/*  456 */           treeSort.m_isAscending = false;
/*      */         }
/*  458 */         treeSort.sort();
/*  459 */         cacheEntry = new SchemaCacheEntry(sortedRows);
/*      */ 
/*  461 */         HashableStringArray hashableKey = computeCacheKey(args);
/*  462 */         this.m_rows.put(hashableKey, cacheEntry);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  467 */     return cacheEntry;
/*      */   }
/*      */ 
/*      */   public boolean isFieldIndexed(String fieldName)
/*      */   {
/*  472 */     return this.m_cachedWork.containsKey("indexedField:" + fieldName);
/*      */   }
/*      */ 
/*      */   public synchronized void addIndexToField(String fieldName)
/*      */     throws DataException
/*      */   {
/*  484 */     FieldInfo fi = new FieldInfo();
/*  485 */     if (!this.m_resultSet.getFieldInfo(fieldName, fi))
/*      */     {
/*  487 */       String msg = LocaleUtils.encodeMessage("apSchemaCacheUnableToFindField", null, this.m_viewName, fieldName);
/*      */ 
/*  489 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  492 */     Map cacheEntries = new HashMap();
/*  493 */     for (this.m_resultSet.first(); this.m_resultSet.isRowPresent(); this.m_resultSet.next())
/*      */     {
/*  495 */       String value = this.m_resultSet.getStringValue(fi.m_index);
/*  496 */       SchemaCacheEntry tmpEntry = (SchemaCacheEntry)cacheEntries.get(value);
/*  497 */       if (tmpEntry == null)
/*      */       {
/*  499 */         tmpEntry = new SchemaCacheEntry();
/*  500 */         cacheEntries.put(value, tmpEntry);
/*      */       }
/*  502 */       tmpEntry.addRow(this.m_resultSet.getCurrentRowValues());
/*      */     }
/*      */ 
/*  505 */     Iterator it = cacheEntries.keySet().iterator();
/*  506 */     while (it.hasNext())
/*      */     {
/*  508 */       String fieldValue = (String)it.next();
/*  509 */       SchemaCacheEntry tmpEntry = (SchemaCacheEntry)cacheEntries.get(fieldValue);
/*      */ 
/*  511 */       Map args = new HashMap();
/*  512 */       args.put("fieldNames", new String[] { fieldName });
/*  513 */       args.put("fieldValues", new String[] { fieldValue });
/*  514 */       HashableStringArray hashableKey = computeCacheKey(args);
/*      */ 
/*  516 */       this.m_rows.put(hashableKey, tmpEntry);
/*      */     }
/*      */ 
/*  519 */     this.m_cachedWork.put("indexedField:" + fieldName, "1");
/*      */   }
/*      */ 
/*      */   public HashableStringArray computeCacheKey(Map args)
/*      */   {
/*  524 */     String[] primaryKeyValues = (String[])(String[])args.get("primaryKey");
/*  525 */     String[] filterFieldNames = (String[])(String[])args.get("fieldNames");
/*  526 */     String[] filterFieldValues = (String[])(String[])args.get("fieldValues");
/*  527 */     String[] sortFields = (String[])(String[])args.get("sortFields");
/*  528 */     String[] sortOrders = (String[])(String[])args.get("sortOrders");
/*      */ 
/*  530 */     HashableStringArray hashableKey = null;
/*  531 */     if (primaryKeyValues != null)
/*      */     {
/*  533 */       String[] key = new String[primaryKeyValues.length + 1];
/*  534 */       key[0] = PRIMARY_KEY_STRING;
/*  535 */       System.arraycopy(primaryKeyValues, 0, key, 1, primaryKeyValues.length);
/*  536 */       hashableKey = new HashableStringArray(key);
/*      */     }
/*      */     else
/*      */     {
/*  540 */       List keyList = new ArrayList();
/*      */ 
/*  542 */       if (filterFieldNames != null)
/*      */       {
/*  544 */         IdcStringBuilder buffer = new IdcStringBuilder();
/*  545 */         for (int i = 0; i < filterFieldNames.length; ++i)
/*      */         {
/*  547 */           if (this.m_externalData.containsKey("loaderHandled:" + FIELD_KEY_STRING + ":" + filterFieldNames[i])) {
/*      */             continue;
/*      */           }
/*  550 */           if (buffer.length() > 0)
/*      */           {
/*  552 */             buffer.append(HASH_SEPARATOR);
/*      */           }
/*  554 */           buffer.append(filterFieldNames[i]);
/*  555 */           buffer.append(HASH_SEPARATOR);
/*  556 */           buffer.append(filterFieldValues[i]);
/*      */         }
/*      */ 
/*  560 */         if (buffer.length() > 0)
/*      */         {
/*  562 */           keyList.add(FIELD_KEY_STRING);
/*  563 */           keyList.add(buffer.toString());
/*      */         }
/*      */       }
/*      */ 
/*  567 */       if (sortFields != null)
/*      */       {
/*  569 */         IdcStringBuilder buffer = new IdcStringBuilder();
/*  570 */         for (int i = 0; i < sortFields.length; ++i)
/*      */         {
/*  572 */           if (this.m_externalData.containsKey("loaderHandled:" + SORT_KEY_STRING + ":" + sortFields[i])) {
/*      */             continue;
/*      */           }
/*  575 */           if (buffer.length() > 0)
/*      */           {
/*  577 */             buffer.append(HASH_SEPARATOR);
/*      */           }
/*  579 */           buffer.append(sortFields[i]);
/*  580 */           buffer.append(HASH_SEPARATOR);
/*  581 */           buffer.append(sortOrders[i]);
/*      */         }
/*      */ 
/*  585 */         if (buffer.length() > 0)
/*      */         {
/*  587 */           keyList.add(SORT_KEY_STRING);
/*  588 */           keyList.add(buffer.toString());
/*      */         }
/*      */       }
/*      */ 
/*  592 */       String[] key = new String[keyList.size()];
/*  593 */       keyList.toArray(key);
/*  594 */       hashableKey = new HashableStringArray(key);
/*      */     }
/*      */ 
/*  597 */     return hashableKey;
/*      */   }
/*      */ 
/*      */   public SchemaCacheEntry getCacheEntry(Map args)
/*      */   {
/*  602 */     HashableStringArray hashableKey = computeCacheKey(args);
/*  603 */     return getCacheEntryFromKey(hashableKey);
/*      */   }
/*      */ 
/*      */   public SchemaCacheEntry getCacheEntryFromKey(HashableStringArray hashableKey)
/*      */   {
/*  608 */     SchemaCacheEntry cacheEntry = (SchemaCacheEntry)this.m_rows.get(hashableKey);
/*  609 */     if (SystemUtils.m_verbose)
/*      */     {
/*  611 */       Report.debug("schemacache", "getCacheEntry() key: " + hashableKey + " m_isValid: " + this.m_isValid + " m_isAllValues: " + this.m_isAllValues + " m_isDirty: " + this.m_isDirty + " returning " + cacheEntry, null);
/*      */     }
/*      */ 
/*  615 */     return cacheEntry;
/*      */   }
/*      */ 
/*      */   public String getViewName()
/*      */   {
/*  620 */     return this.m_viewName;
/*      */   }
/*      */ 
/*      */   public String getRelationshipName()
/*      */   {
/*  625 */     return this.m_relationName;
/*      */   }
/*      */ 
/*      */   public String[] getParentValues()
/*      */   {
/*  630 */     return this.m_parentValues;
/*      */   }
/*      */ 
/*      */   public long getTimestamp()
/*      */   {
/*  635 */     return this.m_timestamp;
/*      */   }
/*      */ 
/*      */   public synchronized int updateCache(ResultSet deletedData, ResultSet newData, DynamicHtmlMerger merger, ExecutionContext context, SchemaViewData viewDef, SchemaData tableDef, String viewVersion, String relationVersion, long timestamp, boolean isIncrementalUpdate, boolean isPartial)
/*      */     throws DataException
/*      */   {
/*  646 */     this.m_cachedWork = new HashMap();
/*      */ 
/*  648 */     if (isIncrementalUpdate)
/*      */     {
/*  650 */       return 2;
/*      */     }
/*      */ 
/*  653 */     if (tableDef == null)
/*      */     {
/*  655 */       tableDef = viewDef;
/*      */     }
/*      */ 
/*  658 */     String timestampColumn = tableDef.get("schTableRowModifyTimestamp");
/*  659 */     FieldInfo timestampFieldInfo = null;
/*  660 */     if ((timestampColumn != null) && (((timestampColumn.length() == 0) || (timestampColumn.equals("<none>")))))
/*      */     {
/*  663 */       timestampColumn = null;
/*      */     }
/*      */ 
/*  666 */     if (timestampColumn != null)
/*      */     {
/*  668 */       timestampFieldInfo = new FieldInfo();
/*  669 */       if (!newData.getFieldInfo(timestampColumn, timestampFieldInfo))
/*      */       {
/*  671 */         timestampFieldInfo = null;
/*      */       }
/*      */     }
/*      */ 
/*  675 */     String internalColumn = viewDef.get("schInternalColumn");
/*  676 */     FieldInfo internalColumnInfo = null;
/*  677 */     if (internalColumn != null)
/*      */     {
/*  679 */       internalColumnInfo = new FieldInfo();
/*  680 */       if (!newData.getFieldInfo(internalColumn, internalColumnInfo))
/*      */       {
/*  682 */         internalColumnInfo = null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  688 */     Hashtable fieldMap = new Hashtable();
/*  689 */     for (int i = 0; i < newData.getNumFields(); ++i)
/*      */     {
/*  691 */       FieldInfo info = new FieldInfo();
/*  692 */       newData.getIndexFieldInfo(i, info);
/*  693 */       fieldMap.put(info.m_name.toLowerCase(), info);
/*      */     }
/*      */ 
/*  696 */     ViewFieldInfo viewFieldInfo = viewDef.m_viewFieldInfo;
/*  697 */     if (viewFieldInfo.m_needsLocalization)
/*      */     {
/*  699 */       synchronized (viewFieldInfo)
/*      */       {
/*  701 */         if ((viewFieldInfo.m_needsLocalization) && (merger != null))
/*      */         {
/*  703 */           viewDef.addLocalizationInformation(merger, viewFieldInfo, context, newData);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  708 */     ViewLocaleInfo[] fields = (ViewLocaleInfo[])(ViewLocaleInfo[])viewFieldInfo.m_fieldList.toArray(new ViewLocaleInfo[viewFieldInfo.m_fieldList.size()]);
/*      */ 
/*  710 */     DataBinder binder = (DataBinder)context.getCachedObject("DataBinder");
/*  711 */     binder.pushActiveResultSet("NewData", newData);
/*      */ 
/*  718 */     int[] indexes = new int[fields.length];
/*  719 */     FieldInfo[] fis = new FieldInfo[fields.length];
/*  720 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/*  722 */       ViewLocaleInfo info = fields[i];
/*  723 */       if (SystemUtils.m_verbose)
/*      */       {
/*  725 */         Report.debug("schemacache", "Configuring field " + info.m_name + ".  isScript: " + info.m_isScript, null);
/*      */       }
/*      */ 
/*  728 */       if (info.m_isScript)
/*      */       {
/*  730 */         fis[i] = new FieldInfo();
/*  731 */         fis[i].m_name = info.m_name;
/*  732 */         fis[i].m_index = i;
/*  733 */         indexes[i] = -1;
/*      */       }
/*      */       else {
/*  736 */         String key = "<unknown>";
/*  737 */         if (info.m_fieldName != null)
/*      */         {
/*  739 */           key = info.m_fieldName.toLowerCase();
/*      */         }
/*  741 */         FieldInfo fInfo = (FieldInfo)fieldMap.get(key);
/*  742 */         if (fInfo == null)
/*      */         {
/*  744 */           String msg = LocaleUtils.encodeMessage("apSchemaCacheUnableToFindField", null, this.m_viewName, info.m_name);
/*      */ 
/*  747 */           throw new DataException(msg);
/*      */         }
/*  749 */         indexes[i] = fInfo.m_index;
/*      */ 
/*  751 */         fis[i] = new FieldInfo();
/*  752 */         fis[i].copy(fInfo);
/*  753 */         fis[i].m_name = info.m_name;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  758 */     int size = 0;
/*      */     Map rows;
/*      */     DataResultSet drset;
/*  759 */     if ((((isIncrementalUpdate) || (isPartial))) && (this.m_resultSet != null) && (this.m_rows != null))
/*      */     {
/*  762 */       DataResultSet drset = this.m_resultSet;
/*  763 */       Map rows = this.m_rows;
/*  764 */       size = this.m_size;
/*      */     }
/*      */     else
/*      */     {
/*  768 */       rows = new HashMap();
/*  769 */       drset = new DataResultSet();
/*      */ 
/*  772 */       Vector fieldInfos = new IdcVector(Arrays.asList(fis));
/*  773 */       fieldInfos.setSize(fis.length);
/*  774 */       drset.mergeFieldsWithFlags(fieldInfos, 0);
/*  775 */       drset.setDateFormat(newData.getDateFormat());
/*      */     }
/*      */ 
/*  778 */     long newestItem = 0L;
/*  779 */     long[] cachedTimestampInfo = (long[])(long[])this.m_externalData.get("CachedAllRowTimestampInfo");
/*      */ 
/*  781 */     if (cachedTimestampInfo != null)
/*      */     {
/*  786 */       newestItem = cachedTimestampInfo[0];
/*      */     }
/*  788 */     int scriptEvaluations = 0;
/*  789 */     String[] row = new String[fields.length];
/*  790 */     if (SystemUtils.m_verbose)
/*      */     {
/*  792 */       Report.debug("schemacache", "loading from " + newData + " for " + this, null);
/*      */     }
/*      */ 
/*  795 */     int rowCount = 0;
/*      */ 
/*  801 */     String rowLimitString = viewDef.get("ServerSchemaLoaderViewLoadMaxRows", null);
/*      */ 
/*  803 */     int rowLimit = NumberUtils.parseInteger(rowLimitString, 0);
/*      */ 
/*  805 */     while (newData.isRowPresent())
/*      */     {
/*  807 */       ++rowCount;
/*  808 */       if ((rowLimit > 0) && (rowCount > rowLimit))
/*      */       {
/*  810 */         String failOnAbort = viewDef.get("SchemaSchemaLoaderRowOverflowError", null);
/*      */ 
/*  812 */         if (StringUtils.convertToBool(failOnAbort, false))
/*      */         {
/*  814 */           String msg = LocaleUtils.encodeMessage("apSchemaRowLimitExceeded", null, "" + rowLimit, viewDef.m_name);
/*      */ 
/*  817 */           throw new DataException(msg);
/*      */         }
/*  819 */         Report.trace("schemacache", "stopped loading rows on view '" + viewDef.m_name + "'", null);
/*      */ 
/*  821 */         break;
/*      */       }
/*      */ 
/*  824 */       for (int i = 0; i < indexes.length; ++i)
/*      */       {
/*  826 */         int index = indexes[i];
/*  827 */         String value = "";
/*  828 */         if (index >= 0)
/*      */         {
/*  830 */           value = newData.getStringValue(indexes[i]);
/*      */         }
/*  832 */         else if (merger != null)
/*      */         {
/*  834 */           ViewLocaleInfo info = fields[i];
/*  835 */           DynamicHtml script = info.m_script;
/*  836 */           if (script == null)
/*      */           {
/*  839 */             throw new AssertionError("!$script not defined for column " + info.m_name);
/*      */           }
/*      */ 
/*      */           try
/*      */           {
/*  844 */             merger.setExecutionContext(info.m_context);
/*  845 */             if (merger.m_isReportErrorStack)
/*      */             {
/*  847 */               merger.pushStackMessage(info.m_localizationMessage);
/*      */             }
/*  849 */             value = merger.executeDynamicHtml(script);
/*  850 */             ++scriptEvaluations;
/*  851 */             merger.setExecutionContext(context);
/*      */           }
/*      */           catch (Throwable e)
/*      */           {
/*  855 */             value = e.getMessage();
/*  856 */             value = LocaleResources.localizeMessage(value, context);
/*      */           }
/*      */           finally
/*      */           {
/*  860 */             if (merger.m_isReportErrorStack)
/*      */             {
/*  862 */               merger.popStack();
/*      */             }
/*      */           }
/*      */         }
/*  866 */         row[i] = value;
/*      */       }
/*  868 */       Vector newRowVector = new IdcVector(Arrays.asList(row));
/*      */ 
/*  870 */       if (timestampFieldInfo != null)
/*      */       {
/*  872 */         Date timestampDate = newData.getDateValue(timestampFieldInfo.m_index);
/*  873 */         if (timestampDate != null)
/*      */         {
/*  875 */           long ts = timestampDate.getTime();
/*  876 */           if (ts > newestItem)
/*      */           {
/*  878 */             newestItem = ts;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  883 */       HashableStringArray hashableKey = createKey(internalColumnInfo, row);
/*  884 */       SchemaCacheEntry cacheEntry = null;
/*  885 */       if ((((isIncrementalUpdate) || (isPartial))) && ((cacheEntry = (SchemaCacheEntry)rows.get(hashableKey)) != null))
/*      */       {
/*  890 */         size -= cacheEntry.m_size;
/*  891 */         Vector oldRow = (Vector)cacheEntry.m_rows.get(0);
/*      */ 
/*  893 */         int oSize = oldRow.size();
/*  894 */         if (oSize != row.length)
/*      */         {
/*  896 */           String errMsg = LocaleUtils.encodeMessage("apSchemaCacheUpdateError", null, this.m_viewName);
/*  897 */           throw new DataException(errMsg);
/*      */         }
/*      */ 
/*  902 */         for (int i = 0; i < row.length; ++i)
/*      */         {
/*  904 */           oldRow.setElementAt(row[i], i);
/*      */         }
/*      */ 
/*  907 */         ArrayList list = new ArrayList();
/*  908 */         list.add(newRowVector);
/*  909 */         cacheEntry.setRows(list);
/*      */       }
/*      */       else
/*      */       {
/*  914 */         drset.addRow(newRowVector);
/*  915 */         List l = new ArrayList();
/*  916 */         l.add(newRowVector);
/*  917 */         cacheEntry = new SchemaCacheEntry(l);
/*      */       }
/*  919 */       size += cacheEntry.m_size;
/*      */ 
/*  921 */       rows.put(hashableKey, cacheEntry);
/*  922 */       for (int i = 0; i < row.length; ++i)
/*      */       {
/*  924 */         ViewLocaleInfo info = fields[i];
/*  925 */         if (!info.m_isKeyField)
/*      */           continue;
/*  927 */         String[] key = { info.m_name, row[i] };
/*  928 */         hashableKey = new HashableStringArray(key);
/*  929 */         rows.put(hashableKey, cacheEntry);
/*      */       }
/*      */ 
/*  933 */       newData.next();
/*      */     }
/*      */ 
/*  936 */     if (SystemUtils.m_verbose)
/*      */     {
/*  938 */       Report.debug("schemacache", "loading view '" + viewDef.m_name + "' under relation '" + this.m_relationName + "' evaluated " + scriptEvaluations + " scripts over " + rowCount + " rows", null);
/*      */     }
/*      */ 
/*  943 */     boolean isServerSorted = viewDef.getBoolean("schIsServerSorted", false);
/*  944 */     boolean isDatabaseSorted = viewDef.getBoolean("schIsDatabaseSorted", false);
/*  945 */     if (rowCount == 0)
/*      */     {
/*  947 */       String keyType = (String)context.getCachedObject("KeyType");
/*  948 */       String[] keyValues = (String[])(String[])context.getCachedObject("KeyValues");
/*  949 */       if ((keyType != null) && (keyType.equals(PRIMARY_KEY_STRING)))
/*      */       {
/*  953 */         isPartial = true;
/*      */ 
/*  956 */         SchemaCacheEntry cacheEntry = new SchemaCacheEntry();
/*  957 */         String[] key = { keyType, keyValues[0] };
/*  958 */         HashableStringArray hashableKey = new HashableStringArray(key);
/*  959 */         rows.put(hashableKey, cacheEntry);
/*  960 */         if (SystemUtils.m_verbose)
/*      */         {
/*  962 */           Report.debug("schemacache", "cached the miss for primary key '" + keyValues[0] + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*  968 */     else if ((rowCount > 1) && (((isPartial) || (isIncrementalUpdate))) && (((isServerSorted) || (isDatabaseSorted))))
/*      */     {
/*  971 */       if ((isDatabaseSorted) && (!isServerSorted))
/*      */       {
/*  973 */         Report.trace("schemacache", "sorting data for view " + viewDef.m_name + " in server due to partial loading", null);
/*      */       }
/*      */ 
/*  977 */       SchemaHelper helper = new SchemaHelper();
/*  978 */       drset = (DataResultSet)helper.sortViewData(viewDef, drset);
/*      */     }
/*      */ 
/*  981 */     this.m_rows = rows;
/*  982 */     this.m_resultSet = drset;
/*  983 */     if ((timestampFieldInfo != null) && (newestItem > 0L))
/*      */     {
/*  985 */       this.m_timestamp = newestItem;
/*      */     }
/*      */     else
/*      */     {
/*  989 */       this.m_timestamp = timestamp;
/*      */     }
/*  991 */     if (SystemUtils.m_verbose)
/*      */     {
/*  994 */       Report.debug("schemacache", "resetting timestamp on " + this, null);
/*      */     }
/*      */ 
/*  998 */     this.m_viewVersion = viewVersion;
/*  999 */     this.m_tableVersion = tableDef.get("schVersion", "");
/* 1000 */     this.m_relationVersion = relationVersion;
/*      */ 
/* 1002 */     this.m_isValid = true;
/* 1003 */     if (!isPartial)
/*      */     {
/* 1005 */       this.m_isComplete = true;
/*      */     }
/* 1007 */     this.m_isDirty = false;
/*      */ 
/* 1009 */     this.m_size = size;
/* 1010 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1012 */       Report.debug("schemacache", "updateCache() newData: " + newData + ", deletedData: " + deletedData + " isPartial: " + isPartial + " isIncremental: " + isIncrementalUpdate + " results in: m_isDirty: " + this.m_isDirty + " m_isComplete: " + this.m_isComplete, null);
/*      */     }
/*      */ 
/* 1020 */     return 0;
/*      */   }
/*      */ 
/*      */   protected String simplifyTrivialScript(String script, Vector columns)
/*      */   {
/* 1025 */     int columnCount = columns.size();
/* 1026 */     for (int i = 0; i < columnCount; ++i)
/*      */     {
/* 1028 */       String column = (String)columns.elementAt(i);
/* 1029 */       if ((!script.equals(column)) && (!script.equals("<$" + column + "$>"))) {
/*      */         continue;
/*      */       }
/* 1032 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1034 */         Report.debug("schemacache", "converting script '" + script + "' into column '" + column + "'", null);
/*      */       }
/*      */ 
/* 1037 */       return column;
/*      */     }
/*      */ 
/* 1040 */     return null;
/*      */   }
/*      */ 
/*      */   protected HashableStringArray createKey(FieldInfo columnInfo, String[] row)
/*      */   {
/*      */     HashableStringArray hashableKey;
/*      */     HashableStringArray hashableKey;
/* 1046 */     if (columnInfo != null)
/*      */     {
/* 1048 */       String[] key = { PRIMARY_KEY_STRING, row[columnInfo.m_index] };
/*      */ 
/* 1051 */       hashableKey = new HashableStringArray(key);
/*      */     }
/*      */     else
/*      */     {
/* 1057 */       String[] key = new String[row.length + 1];
/* 1058 */       key[0] = PRIMARY_KEY_STRING;
/* 1059 */       System.arraycopy(row, 0, key, 1, row.length);
/* 1060 */       hashableKey = new HashableStringArray(key);
/*      */     }
/* 1062 */     return hashableKey;
/*      */   }
/*      */ 
/*      */   public synchronized void setDirty()
/*      */   {
/* 1067 */     this.m_rows = new HashMap();
/* 1068 */     this.m_isDirty = true;
/*      */   }
/*      */ 
/*      */   public boolean isMarkedDirty()
/*      */   {
/* 1074 */     return this.m_isDirty;
/*      */   }
/*      */ 
/*      */   public synchronized boolean objectVersionsDirty(SchemaViewData view, SchemaTableData table, SchemaRelationData relationship)
/*      */   {
/* 1081 */     if (this.m_viewVersion == null)
/*      */     {
/* 1083 */       this.m_viewVersion = "";
/*      */     }
/* 1085 */     if (this.m_tableVersion == null)
/*      */     {
/* 1087 */       this.m_tableVersion = "";
/*      */     }
/* 1089 */     if (this.m_relationVersion == null)
/*      */     {
/* 1091 */       this.m_relationVersion = "";
/*      */     }
/*      */ 
/* 1094 */     boolean isDirty = false;
/*      */ 
/* 1096 */     if (view != null)
/*      */     {
/* 1098 */       String tmp = view.get("schVersion", "0");
/* 1099 */       if (!tmp.equals(this.m_viewVersion))
/*      */       {
/* 1101 */         isDirty = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1115 */     if (relationship != null)
/*      */     {
/* 1117 */       String tmp = relationship.get("schVersion", "0");
/* 1118 */       if (!tmp.equals(this.m_relationVersion))
/*      */       {
/* 1120 */         isDirty = true;
/*      */       }
/*      */     }
/* 1123 */     return isDirty;
/*      */   }
/*      */ 
/*      */   public synchronized boolean isDirty(long correctTimestamp)
/*      */   {
/* 1128 */     if (this.m_isDirty)
/*      */     {
/* 1130 */       return true;
/*      */     }
/*      */ 
/* 1133 */     boolean isDirty = correctTimestamp > this.m_timestamp;
/* 1134 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1136 */       Report.debug("schemacache", this + " isDirty: " + isDirty + "  " + correctTimestamp + " vs. " + this.m_timestamp + " (" + (this.m_timestamp - correctTimestamp) + "ms)", null);
/*      */     }
/*      */ 
/* 1145 */     if (this.m_viewConfig == null)
/*      */     {
/* 1147 */       this.m_viewConfig = ((SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig"));
/*      */     }
/*      */ 
/* 1150 */     if (this.m_tableConfig == null)
/*      */     {
/* 1152 */       this.m_tableConfig = ((SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig"));
/*      */     }
/*      */ 
/* 1155 */     if (this.m_relationConfig == null)
/*      */     {
/* 1157 */       this.m_relationConfig = ((SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig"));
/*      */     }
/*      */ 
/* 1161 */     SchemaViewData viewDef = (SchemaViewData)this.m_viewConfig.getData(this.m_viewName);
/*      */ 
/* 1163 */     if (viewDef == null)
/*      */     {
/* 1166 */       Report.trace("schemacache", "the view '" + this.m_viewName + "' is no longer defined.  isDirty() returns false.", null);
/*      */     }
/*      */ 
/* 1170 */     String tableName = viewDef.get("schTableName");
/* 1171 */     SchemaTableData tableDef = (SchemaTableData)this.m_tableConfig.getData(tableName);
/*      */ 
/* 1173 */     if (tableDef == null)
/*      */     {
/* 1175 */       Report.trace("schemacache", "the table '" + tableName + "' is no longer defined.  isDirty() returns false.", null);
/*      */     }
/*      */ 
/* 1179 */     SchemaRelationData relationDef = null;
/* 1180 */     if (this.m_relationName != null)
/*      */     {
/* 1182 */       relationDef = (SchemaRelationData)this.m_relationConfig.getData(this.m_relationName);
/*      */ 
/* 1184 */       if (relationDef == null)
/*      */       {
/* 1186 */         Report.trace("schemacache", "the relation '" + this.m_relationName + "' is not defined.  isDirty returns false.", null);
/*      */ 
/* 1189 */         return false;
/*      */       }
/*      */     }
/* 1192 */     if (objectVersionsDirty(viewDef, tableDef, relationDef))
/*      */     {
/* 1194 */       return true;
/*      */     }
/*      */ 
/* 1197 */     return isDirty;
/*      */   }
/*      */ 
/*      */   public synchronized boolean isComplete()
/*      */   {
/* 1202 */     return this.m_isComplete;
/*      */   }
/*      */ 
/*      */   protected void finalize()
/*      */     throws Throwable
/*      */   {
/* 1208 */     m_objectCount -= 1;
/* 1209 */     if (SystemUtils.m_verbose)
/*      */     {
/* 1211 */       Report.debug("objectCounter", "finalize() on " + this + ", objectCount is " + m_objectCount, null);
/*      */     }
/*      */ 
/* 1217 */     if (m_manager != null)
/*      */     {
/* 1220 */       int currentUsage = m_manager.getCurrentUsage();
/* 1221 */       int maxUsage = m_manager.getMaximumUsage();
/* 1222 */       double schemaCachePercentageUsed = currentUsage * 100 / maxUsage;
/* 1223 */       String schemaCachePercentageUsedString = Double.toString(schemaCachePercentageUsed);
/* 1224 */       int endIndex = schemaCachePercentageUsedString.indexOf(46) + 3;
/* 1225 */       if (endIndex > schemaCachePercentageUsedString.length())
/*      */       {
/* 1227 */         endIndex = schemaCachePercentageUsedString.length();
/*      */       }
/* 1229 */       schemaCachePercentageUsedString = schemaCachePercentageUsedString.substring(0, endIndex);
/* 1230 */       String amountUsed = currentUsage + " bytes(" + schemaCachePercentageUsedString + "%)";
/* 1231 */       Report.trace("schemacache", null, "csMonitorSchemaCacheObjectCount", new Object[] { Integer.valueOf(m_objectCount) });
/* 1232 */       Report.trace("schemacache", null, "csMonitorSchemaCacheAmountUsed", new Object[] { amountUsed });
/*      */     }
/* 1234 */     super.finalize();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1239 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98621 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaCacheItem
 * JD-Core Version:    0.5.4
 */