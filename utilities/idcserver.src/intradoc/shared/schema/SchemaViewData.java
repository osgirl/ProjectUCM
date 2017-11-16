/*      */ package intradoc.shared.schema;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.DynamicHtmlMerger;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetCopier;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.shared.LRUManager;
/*      */ import intradoc.shared.LRUManagerContainer;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SchemaViewData extends SchemaData
/*      */   implements ViewCacheCallback, LRUManagerContainer
/*      */ {
/*   32 */   public int m_descriptionIndex = -1;
/*   33 */   public int m_tableNameIndex = -1;
/*   34 */   public int m_internalClmnIndex = -1;
/*      */ 
/*   36 */   protected SchemaTableConfig m_tables = null;
/*      */ 
/*   38 */   public static String[] VIEW_LOCALE_COLUMNS = { "schLocale", "schDisplayRule" };
/*      */   public static final int LOCALE_NAME_INDEX = 0;
/*      */   public static final int DISPLAY_RULE_INDEX = 1;
/*      */   public static final int F_ALLOW_LOCALIZE_DISPLAY_VALUE = 1;
/*   52 */   protected ViewFieldInfo m_viewFieldInfo = null;
/*      */ 
/*   54 */   protected Map m_viewValues = new HashMap();
/*   55 */   protected boolean m_lruManagerInit = false;
/*      */   protected LRUManager m_lruManager;
/*   64 */   protected boolean m_loadsUnconvertedValues = false;
/*      */ 
/*      */   public SchemaViewData()
/*      */   {
/*   69 */     this.m_localDataCruftKeys.addAll(Arrays.asList(new String[] { "schColumnList", "schTableDescription" }));
/*      */ 
/*   74 */     this.m_resultSetCruftKeys.addAll(Arrays.asList(new String[] { "TableDefinition" }));
/*      */ 
/*   79 */     this.m_baseResultSets.add("ViewLocales");
/*      */   }
/*      */ 
/*      */   public void initLRUManager()
/*      */   {
/*   84 */     if (this.m_lruManagerInit)
/*      */       return;
/*   86 */     this.m_lruManagerInit = true;
/*   87 */     String useLRUManager = get("SchemaUseLRUManager", null);
/*   88 */     if (!StringUtils.convertToBool(useLRUManager, true))
/*      */       return;
/*   90 */     this.m_lruManager = ((LRUManager)SharedObjects.getObject("globalObjects", "SchemaViewLRUManager"));
/*      */   }
/*      */ 
/*      */   public LRUManager getLRUManager()
/*      */   {
/*   98 */     return this.m_lruManager;
/*      */   }
/*      */ 
/*      */   public void setLRUManager(LRUManager manager)
/*      */   {
/*  103 */     this.m_lruManager = manager;
/*      */   }
/*      */ 
/*      */   public String getNameField()
/*      */   {
/*  108 */     return "schViewName";
/*      */   }
/*      */ 
/*      */   public String getTimestampField()
/*      */   {
/*  113 */     return "schViewLastLoaded";
/*      */   }
/*      */ 
/*      */   public String getIsUpToDateField()
/*      */   {
/*  118 */     return "schViewIsUpToDate";
/*      */   }
/*      */ 
/*      */   public SchemaTableData getTableDefinition()
/*      */   {
/*  123 */     String tableName = get("schTableName");
/*  124 */     if (this.m_tables == null)
/*      */     {
/*  126 */       this.m_tables = ((SchemaTableConfig)SharedObjects.getTable("SchemaTableConfig"));
/*      */     }
/*      */ 
/*  130 */     SchemaTableData def = (SchemaTableData)this.m_tables.getData(tableName);
/*      */ 
/*  132 */     return def;
/*      */   }
/*      */ 
/*      */   protected void initIndexes()
/*      */   {
/*  137 */     super.initIndexes();
/*  138 */     FieldInfo info = new FieldInfo();
/*      */ 
/*  140 */     this.m_resultSet.getFieldInfo("schViewDescription", info);
/*  141 */     this.m_descriptionIndex = info.m_index;
/*      */ 
/*  143 */     this.m_resultSet.getFieldInfo("schTableName", info);
/*  144 */     this.m_tableNameIndex = info.m_index;
/*      */ 
/*  146 */     this.m_resultSet.getFieldInfo("schInternalColumn", info);
/*  147 */     this.m_internalClmnIndex = info.m_index;
/*      */   }
/*      */ 
/*      */   public void update(DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  155 */     super.update(binder);
/*      */   }
/*      */ 
/*      */   public void updateEx(DataBinder binder)
/*      */   {
/*  160 */     super.updateEx(binder);
/*      */ 
/*  162 */     Map fieldMap = new HashMap();
/*  163 */     List fieldList = new ArrayList();
/*  164 */     boolean localizeOnDisplay = getBoolean("schLocalizeWhenDisplayed", false);
/*      */ 
/*  166 */     String tmp = get("schViewColumns");
/*  167 */     List tmpFields = StringUtils.makeListFromSequenceSimple(tmp);
/*  168 */     String internalColumn = get("schInternalColumn");
/*  169 */     if ((internalColumn != null) && (tmpFields.indexOf(internalColumn) == -1))
/*      */     {
/*  171 */       tmpFields.add(internalColumn);
/*      */     }
/*      */ 
/*  174 */     for (int i = 0; i < tmpFields.size(); ++i)
/*      */     {
/*  176 */       String name = (String)tmpFields.get(i);
/*  177 */       ViewLocaleInfo info = new ViewLocaleInfo();
/*  178 */       info.m_name = name;
/*  179 */       info.m_fieldName = name;
/*  180 */       fieldMap.put(name.toLowerCase(), info);
/*  181 */       fieldList.add(info);
/*      */     }
/*      */ 
/*  184 */     Map localeMap = null;
/*      */ 
/*  186 */     DataResultSet localeSet = (DataResultSet)this.m_data.getResultSet("ViewLocales");
/*      */ 
/*  188 */     if ((!localizeOnDisplay) && (localeSet != null))
/*      */     {
/*  190 */       localeMap = new HashMap();
/*  191 */       for (localeSet.first(); localeSet.isRowPresent(); localeSet.next())
/*      */       {
/*  193 */         Properties props = localeSet.getCurrentRowProps();
/*  194 */         localeMap.put(props.getProperty("schLocale"), props);
/*      */       }
/*      */     }
/*      */ 
/*  198 */     DataResultSet localeConfig = SharedObjects.getTable("LocaleConfig");
/*      */ 
/*  201 */     String l10nString = get("schLocalizationList");
/*      */     List l10nList;
/*  202 */     if (l10nString == null)
/*      */     {
/*  204 */       List l10nList = new ArrayList();
/*  205 */       l10nList.add("Display");
/*      */     }
/*      */     else
/*      */     {
/*  209 */       l10nList = StringUtils.makeListFromSequenceSimple(l10nString);
/*      */     }
/*      */ 
/*  212 */     String lcStart = "<$lc(";
/*  213 */     String lcEnd = ")$>";
/*      */ 
/*  215 */     for (int i = 0; i < l10nList.size(); ++i)
/*      */     {
/*  217 */       String key = (String)l10nList.get(i);
/*  218 */       String defaultScript = get("schDefault" + key + "Expression");
/*  219 */       ViewLocaleInfo defaultScriptInfo = null;
/*  220 */       if (defaultScript != null)
/*      */       {
/*  222 */         defaultScriptInfo = new ViewLocaleInfo();
/*  223 */         defaultScriptInfo.m_name = (key + ".default");
/*  224 */         if ((getBoolean("schLocalizeWhenDisplayed", false)) && (defaultScript.startsWith(lcStart)) && (defaultScript.endsWith(lcEnd)))
/*      */         {
/*  231 */           IdcStringBuilder builder = new IdcStringBuilder(defaultScript.length());
/*  232 */           builder.append("<$");
/*  233 */           builder.append(defaultScript.substring(lcStart.length(), defaultScript.length() - lcEnd.length()));
/*      */ 
/*  235 */           builder.append("$>");
/*  236 */           defaultScript = builder.toString();
/*      */ 
/*  238 */           defaultScriptInfo.m_fieldName = defaultScript.substring(lcStart.length(), defaultScript.length() - lcEnd.length());
/*      */         }
/*      */         else
/*      */         {
/*  243 */           defaultScriptInfo.m_isScript = true;
/*  244 */           defaultScriptInfo.m_displayScript = defaultScript;
/*      */         }
/*      */       }
/*      */ 
/*  248 */       if ((key.equals("Display")) && (defaultScriptInfo == null))
/*      */       {
/*  251 */         defaultScript = get("schLabelColumn");
/*  252 */         if ((defaultScript == null) || (defaultScript.length() == 0))
/*      */         {
/*  255 */           defaultScript = get("schInternalColumn");
/*      */         }
/*  257 */         defaultScriptInfo = new ViewLocaleInfo();
/*  258 */         defaultScriptInfo.m_name = (key + ".default");
/*  259 */         defaultScriptInfo.m_fieldName = defaultScript;
/*      */       }
/*      */ 
/*  262 */       if (defaultScriptInfo != null)
/*      */       {
/*  264 */         fieldList.add(defaultScriptInfo);
/*  265 */         fieldMap.put(defaultScriptInfo.m_name.toLowerCase(), defaultScriptInfo);
/*      */       }
/*  267 */       if (localizeOnDisplay)
/*      */         continue;
/*  269 */       for (localeConfig.first(); localeConfig.isRowPresent(); localeConfig.next())
/*      */       {
/*  271 */         Properties localeProps = localeConfig.getCurrentRowProps();
/*  272 */         String localeName = localeProps.getProperty("lcLocaleId");
/*  273 */         String script = null;
/*  274 */         ViewLocaleInfo info = null;
/*  275 */         if (localeMap != null)
/*      */         {
/*  277 */           Properties row = (Properties)localeMap.get(localeName);
/*  278 */           if (row != null)
/*      */           {
/*  280 */             script = row.getProperty("sch" + key + "Expression");
/*  281 */             if (script == null)
/*      */             {
/*  283 */               script = row.getProperty("sch" + key + "Rule");
/*      */             }
/*      */           }
/*  286 */           if ((script != null) && (script.length() > 0))
/*      */           {
/*  288 */             info = new ViewLocaleInfo();
/*  289 */             info.m_name = (key + "." + localeName);
/*  290 */             info.m_isScript = true;
/*  291 */             info.m_displayScript = script;
/*      */           }
/*      */         }
/*  294 */         else if (defaultScriptInfo != null)
/*      */         {
/*  296 */           info = new ViewLocaleInfo();
/*  297 */           info.m_name = (key + "." + localeName);
/*  298 */           info.m_isScript = defaultScriptInfo.m_isScript;
/*  299 */           info.m_displayScript = defaultScriptInfo.m_displayScript;
/*  300 */           info.m_fieldName = defaultScriptInfo.m_fieldName;
/*      */         }
/*      */ 
/*  303 */         if (info == null)
/*      */           continue;
/*  305 */         fieldMap.put(info.m_name.toLowerCase(), info);
/*  306 */         fieldList.add(info);
/*  307 */         if (!SystemUtils.m_verbose)
/*      */           continue;
/*  309 */         Report.debug("schemacache", "adding field " + info.m_name + " to view " + this.m_name, null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  317 */     synchronized (this)
/*      */     {
/*  319 */       this.m_viewFieldInfo = new ViewFieldInfo(fieldList, fieldMap);
/*      */ 
/*  321 */       initLRUManager();
/*  322 */       markEverythingDirty();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void populateBinder(DataBinder binder)
/*      */   {
/*  328 */     super.populateBinder(binder);
/*      */   }
/*      */ 
/*      */   public int getSize(Object data)
/*      */   {
/*  338 */     SchemaCacheItem item = (SchemaCacheItem)data;
/*  339 */     return item.m_size;
/*      */   }
/*      */ 
/*      */   public synchronized void discard(Object data)
/*      */   {
/*  344 */     SchemaCacheItem item = (SchemaCacheItem)data;
/*      */ 
/*  353 */     this.m_viewValues.remove(item);
/*      */   }
/*      */ 
/*      */   public Hashtable getViewValuesTable()
/*      */   {
/*  358 */     return new Hashtable(this.m_viewValues);
/*      */   }
/*      */ 
/*      */   public ResultSet getViewValues(Map args)
/*      */     throws DataException
/*      */   {
/*  379 */     SchemaCacheItem item = null;
/*  380 */     SchemaLoader loader = null;
/*      */ 
/*  382 */     String relationName = (String)args.get("relationName");
/*  383 */     if (relationName != null)
/*      */     {
/*  385 */       ResultSet parentValues = (ResultSet)args.get("parentValues");
/*      */ 
/*  387 */       SchemaRelationConfig relations = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*      */ 
/*  389 */       SchemaRelationData relationship = (SchemaRelationData)relations.getData(relationName);
/*      */ 
/*  391 */       loader = getViewLoader(relationship, args);
/*  392 */       item = createCacheItemByRelationship(relationName, parentValues, loader, args);
/*      */     }
/*      */     else
/*      */     {
/*  396 */       item = new SchemaCacheItem(this);
/*  397 */       loader = getViewLoader(null, args);
/*      */     }
/*      */ 
/*  400 */     if (SystemUtils.m_verbose)
/*      */     {
/*  402 */       Report.debug("schemacache", "getViewValues() using '" + item + "'", null);
/*      */     }
/*      */ 
/*  411 */     syncViewValues(loader, item, args);
/*  412 */     ResultSet data = item.getViewValues(args);
/*      */ 
/*  414 */     Object filter = args.get("filter");
/*  415 */     if ((data != null) && (filter != null) && (filter instanceof SchemaSecurityFilter))
/*      */     {
/*  417 */       String privStr = (String)args.get("privilege");
/*  418 */       int priv = NumberUtils.parseInteger(privStr, 1);
/*  419 */       data = copyFilterResultSetValues(null, data, (SchemaSecurityFilter)filter, priv);
/*      */     }
/*      */ 
/*  422 */     return data;
/*      */   }
/*      */ 
/*      */   public ResultSet getAllViewValuesEx(Map args) throws DataException
/*      */   {
/*  427 */     if (args == null)
/*      */     {
/*  429 */       args = new HashMap();
/*      */     }
/*  431 */     args.put("privilege", "0");
/*  432 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   public ResultSet getAllViewValues() throws DataException
/*      */   {
/*  437 */     Map args = new HashMap();
/*  438 */     args.put("privilege", "0");
/*  439 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   public ResultSet getAllViewValuesWithFilter(SchemaSecurityFilter filter)
/*      */     throws DataException
/*      */   {
/*  453 */     Map args = new HashMap();
/*  454 */     if (filter != null)
/*      */     {
/*  456 */       args.put("filter", filter);
/*      */     }
/*  458 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   public ResultSet getViewValuesWithFilter(String relationName, ResultSet parentValues, SchemaSecurityFilter filter)
/*      */     throws DataException
/*      */   {
/*  465 */     Map args = new HashMap();
/*  466 */     args.put("relationName", relationName);
/*  467 */     args.put("parentValues", parentValues);
/*  468 */     args.put("filter", filter);
/*  469 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   public ResultSet getViewValues(String relationName, ResultSet parentValues, Map args)
/*      */     throws DataException
/*      */   {
/*  475 */     if (args == null)
/*      */     {
/*  477 */       args = new HashMap();
/*      */     }
/*  479 */     args.put("relationName", relationName);
/*  480 */     args.put("parentValues", parentValues);
/*  481 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public ResultSet getViewValuesWithFilter(String keyType, String[] keyValues, SchemaSecurityFilter filter)
/*      */     throws DataException
/*      */   {
/*  490 */     SystemUtils.reportDeprecatedUsage("SchemaViewData.getViewValuesWithFilter(keyType, keyValues, filter) is deprecated in favor of SchemaViewData.getViewValues(map)");
/*      */ 
/*  494 */     Map args = new HashMap();
/*  495 */     args.put("filter", filter);
/*  496 */     if (keyType.equals("primaryKey"))
/*      */     {
/*  498 */       args.put("primaryKey", keyValues);
/*      */     }
/*  500 */     else if (keyType.startsWith("relation:"))
/*      */     {
/*  502 */       String relationName = keyType.substring("relation:".length());
/*  503 */       args.put("relationName", relationName);
/*      */ 
/*  505 */       SchemaHelper helper = new SchemaHelper();
/*  506 */       SchemaRelationData relation = (SchemaRelationData)helper.requireSchemaData("SchemaRelationConfig", relationName);
/*      */ 
/*  508 */       String relationColumn = relation.get("schView2Column");
/*  509 */       DataResultSet parentValues = new DataResultSet(new String[] { relationColumn });
/*  510 */       Vector v = new IdcVector();
/*  511 */       v.add(keyValues[0]);
/*  512 */       parentValues.addRow(v);
/*      */ 
/*  514 */       args.put("parentValues", parentValues);
/*      */     }
/*  516 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public ResultSet getViewValues(String keyType, String[] keyValues)
/*      */     throws DataException
/*      */   {
/*  525 */     SystemUtils.reportDeprecatedUsage("SchemaViewData.getViewValuesWith(keyType, keyValues) is deprecated in favor of SchemaViewData.getViewValues(map)");
/*      */ 
/*  529 */     Map args = new HashMap();
/*  530 */     if (keyType.equals("primaryKey"))
/*      */     {
/*  532 */       args.put("primaryKey", keyValues);
/*      */     }
/*  534 */     else if (keyType.startsWith("relation:"))
/*      */     {
/*  536 */       String relationName = keyType.substring("relation:".length());
/*  537 */       args.put("relationName", relationName);
/*      */ 
/*  539 */       SchemaHelper helper = new SchemaHelper();
/*  540 */       SchemaRelationData relation = (SchemaRelationData)helper.requireSchemaData("SchemaRelationConfig", relationName);
/*      */ 
/*  542 */       String relationColumn = relation.get("schView2Column");
/*  543 */       DataResultSet parentValues = new DataResultSet(new String[] { relationColumn });
/*  544 */       Vector v = new IdcVector();
/*  545 */       v.add(keyValues[0]);
/*  546 */       parentValues.addRow(v);
/*      */ 
/*  548 */       args.put("parentValues", parentValues);
/*      */     }
/*  550 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public ResultSet getViewValuesWithFilter(String keyType, String[] keyValues, String relationName, ResultSet parentValues, SchemaSecurityFilter filter)
/*      */     throws DataException
/*      */   {
/*  560 */     SystemUtils.reportDeprecatedUsage("SchemaViewData.getViewValuesWith(keyType, keyValues, relationName, parentValues, filter) is deprecated in favor of SchemaViewData.getViewValues(map)");
/*      */ 
/*  564 */     Map args = new HashMap();
/*  565 */     if (keyType.equals("primaryKey"))
/*      */     {
/*  567 */       args.put("primaryKey", keyValues);
/*      */     }
/*  569 */     args.put("relationName", relationName);
/*  570 */     args.put("parentValues", parentValues);
/*  571 */     args.put("filter", filter);
/*  572 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public ResultSet getViewValues(String keyType, String[] keyValues, String relationName, ResultSet parentValues)
/*      */     throws DataException
/*      */   {
/*  582 */     SystemUtils.reportDeprecatedUsage("SchemaViewData.getViewValuesWith(keyType, keyValues, relationName, parentValues) is deprecated in favor of SchemaViewData.getViewValues(map)");
/*      */ 
/*  586 */     Map args = new HashMap();
/*  587 */     if (keyType.equals("primaryKey"))
/*      */     {
/*  589 */       args.put("primaryKey", keyValues);
/*      */     }
/*  591 */     args.put("relationName", relationName);
/*  592 */     args.put("parentValues", parentValues);
/*  593 */     return getViewValues(args);
/*      */   }
/*      */ 
/*      */   public ResultSet getParentViewValuesWithFilter(ResultSet parentKeyValues, String childViewName, String childRelationName, String parentViewName, String parentRelationName, SchemaSecurityFilter filter)
/*      */     throws DataException
/*      */   {
/*  602 */     ResultSet data = getParentViewValues(parentKeyValues, childViewName, childRelationName, parentViewName, parentRelationName);
/*      */ 
/*  605 */     if ((filter != null) && (data != null))
/*      */     {
/*  607 */       return copyFilterResultSetValues(null, data, filter, 1);
/*      */     }
/*      */ 
/*  610 */     return data;
/*      */   }
/*      */ 
/*      */   public ResultSet getParentViewValues(ResultSet parentKeyValues, String childViewName, String childRelationName, String parentViewName, String parentRelationName)
/*      */     throws DataException
/*      */   {
/*  619 */     SchemaHelper helper = new SchemaHelper();
/*      */ 
/*  621 */     SchemaViewData childView = helper.getView(childViewName);
/*  622 */     SchemaRelationData childRelationship = helper.getRelation(childRelationName);
/*      */ 
/*  625 */     SchemaRelationData parentRelationship = null;
/*  626 */     if (parentViewName != null)
/*      */     {
/*  629 */       helper.getView(parentViewName);
/*      */     }
/*  631 */     if (parentRelationName != null)
/*      */     {
/*  633 */       parentRelationship = helper.getRelation(parentRelationName);
/*      */     }
/*      */ 
/*  637 */     SchemaLoader childLoader = getViewLoader(parentRelationship, null);
/*      */ 
/*  639 */     String[] parentColumns = childLoader.constructParentFieldsArray(this, childRelationship, childView, null);
/*      */     ResultSet myResultSet;
/*      */     ResultSet myResultSet;
/*  643 */     if ((parentRelationName == null) || (parentKeyValues == null))
/*      */     {
/*  645 */       String initialParent = childRelationship.get("schInitialParent");
/*      */       ResultSet myResultSet;
/*  646 */       if ((childViewName.equals(this.m_name)) && (initialParent != null))
/*      */       {
/*  648 */         Vector row = new IdcVector();
/*  649 */         if (initialParent.equals("@empty"))
/*      */         {
/*  651 */           initialParent = "";
/*      */         }
/*  653 */         row.addElement(initialParent);
/*  654 */         DataResultSet drset = new DataResultSet(parentColumns);
/*  655 */         drset.addRow(row);
/*  656 */         myResultSet = getViewValues(childRelationName, drset, null);
/*      */       }
/*      */       else
/*      */       {
/*  660 */         myResultSet = getAllViewValues();
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  665 */       myResultSet = getViewValues(parentRelationName, parentKeyValues, null);
/*      */     }
/*      */ 
/*  668 */     DataResultSet smallParentSet = new DataResultSet(parentColumns);
/*  669 */     Hashtable priorValues = new Hashtable();
/*  670 */     FieldInfo[] infos = ResultSetUtils.createInfoList(myResultSet, parentColumns, true);
/*      */ 
/*  672 */     String[] fieldNames = new String[myResultSet.getNumFields()];
/*  673 */     for (int i = 0; i < fieldNames.length; ++i)
/*      */     {
/*  675 */       fieldNames[i] = myResultSet.getFieldName(i);
/*      */     }
/*  677 */     DataResultSet fullParentSet = new DataResultSet(fieldNames);
/*  678 */     String[] myValues = new String[parentColumns.length];
/*  679 */     boolean hasMultiple = false;
/*  680 */     for (myResultSet.first(); myResultSet.isRowPresent(); myResultSet.next())
/*      */     {
/*  682 */       for (int i = 0; i < myValues.length; ++i)
/*      */       {
/*  684 */         myValues[i] = myResultSet.getStringValue(infos[i].m_index);
/*      */       }
/*  686 */       HashableStringArray a = new HashableStringArray(myValues);
/*  687 */       if (priorValues.get(a) == null)
/*      */       {
/*  689 */         priorValues.put(a, a);
/*  690 */         Vector v = new IdcVector();
/*  691 */         for (int i = 0; i < myValues.length; ++i)
/*      */         {
/*  693 */           v.addElement(myValues[i]);
/*      */         }
/*  695 */         smallParentSet.addRow(v);
/*  696 */         v = new IdcVector();
/*  697 */         for (int i = 0; i < fieldNames.length; ++i)
/*      */         {
/*  699 */           v.addElement(myResultSet.getStringValue(i));
/*      */         }
/*  701 */         fullParentSet.addRow(v);
/*      */       }
/*      */       else
/*      */       {
/*  705 */         hasMultiple = true;
/*      */       }
/*      */     }
/*      */ 
/*  709 */     if ((hasMultiple) && (!getBoolean("schAlwaysReturnCompleteParentSet", false)))
/*      */     {
/*  712 */       return smallParentSet;
/*      */     }
/*  714 */     return fullParentSet;
/*      */   }
/*      */ 
/*      */   protected ResultSet copyFilterResultSetValues(String internalColumn, ResultSet data, SchemaSecurityFilter filter, int requestedAuthorization)
/*      */     throws DataException
/*      */   {
/*  721 */     ResultSet filteredData = null;
/*      */     try
/*      */     {
/*  724 */       filter.prepareFilter(data, this, requestedAuthorization);
/*      */ 
/*  726 */       if (filter instanceof ResultSetCopier)
/*      */       {
/*  728 */         filteredData = ((ResultSetCopier)filter).copy(data);
/*      */       }
/*      */       else
/*      */       {
/*  732 */         DataResultSet drset = new DataResultSet();
/*  733 */         drset.copyFiltered(data, internalColumn, filter);
/*  734 */         filteredData = drset;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  739 */       filter.releaseFilterResultSet();
/*      */     }
/*  741 */     return filteredData;
/*      */   }
/*      */ 
/*      */   protected SchemaCacheItem createCacheItemByRelationship(String relationshipName, ResultSet parentValues, SchemaLoader loader, Map args)
/*      */     throws DataException
/*      */   {
/*  758 */     SchemaRelationConfig relations = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*      */ 
/*  760 */     SchemaRelationData relationData = (SchemaRelationData)relations.getData(relationshipName);
/*      */ 
/*  762 */     if (relationData == null)
/*      */     {
/*  764 */       String msg = LocaleUtils.encodeMessage("apSchemaObjDoesntExist_relation", null, relationshipName);
/*      */ 
/*  766 */       throw new DataException(msg);
/*      */     }
/*  768 */     String[] parentValuesArray = loader.constructParentValuesArray(null, relationData, this, parentValues, args);
/*      */ 
/*  770 */     int flags = 0;
/*  771 */     flags = SchemaLoaderUtils.computeSchemaCacheItemFlags(args, flags);
/*      */ 
/*  773 */     SchemaCacheItem item = new SchemaCacheItem(this, relationData, parentValuesArray, flags);
/*      */ 
/*  775 */     return item;
/*      */   }
/*      */ 
/*      */   protected SchemaCacheItem getCacheItem(SchemaCacheItem valueSelector)
/*      */     throws DataException
/*      */   {
/*  786 */     SchemaCacheItem data = (SchemaCacheItem)this.m_viewValues.get(valueSelector);
/*  787 */     if (data == null)
/*      */     {
/*  789 */       data = syncGetValueSelector(valueSelector);
/*      */     }
/*  791 */     if (this.m_lruManager != null)
/*      */     {
/*  793 */       this.m_lruManager.touchObject(this, data, false);
/*      */     }
/*  795 */     return data;
/*      */   }
/*      */ 
/*      */   protected synchronized SchemaCacheItem syncGetValueSelector(SchemaCacheItem valueSelector)
/*      */     throws DataException
/*      */   {
/*  801 */     SchemaCacheItem data = (SchemaCacheItem)this.m_viewValues.get(valueSelector);
/*  802 */     if (data == null)
/*      */     {
/*  804 */       if (SystemUtils.m_verbose)
/*      */       {
/*  806 */         Report.debug("schemacache", "creating new SchemaCacheItem for '" + this.m_name + "' relation '" + valueSelector.m_relationName + "'.", null);
/*      */       }
/*      */ 
/*  809 */       data = valueSelector;
/*  810 */       this.m_viewValues.put(valueSelector, data);
/*      */     }
/*  812 */     return data;
/*      */   }
/*      */ 
/*      */   protected SchemaLoader getViewLoader(SchemaRelationData relationship, Map extraData)
/*      */     throws DataException
/*      */   {
/*  818 */     SchemaViewConfig viewConfig = (SchemaViewConfig)this.m_resultSet;
/*      */     SchemaLoader loader;
/*      */     SchemaLoader loader;
/*  820 */     if (extraData == null)
/*      */     {
/*  822 */       loader = viewConfig.findLoader(this, relationship, null);
/*      */     }
/*      */     else
/*      */     {
/*  826 */       loader = viewConfig.findLoader(this, relationship, extraData);
/*      */     }
/*      */ 
/*  829 */     if (loader == null)
/*      */     {
/*  831 */       String msg = LocaleUtils.encodeMessage("apSchemaNoLoader", null, this.m_name);
/*      */ 
/*  833 */       DataException e = new DataException(msg);
/*  834 */       Report.trace("schemacache", null, e);
/*  835 */       throw e;
/*      */     }
/*  837 */     return loader;
/*      */   }
/*      */ 
/*      */   protected boolean isKeyTypeSupported(String keyType, SchemaLoader loader)
/*      */   {
/*  842 */     if (keyType != null)
/*      */     {
/*  844 */       Map capabilities = loader.getLoaderCapabilities(this, null);
/*  845 */       return SchemaLoaderUtils.hasKeyType(keyType, capabilities);
/*      */     }
/*  847 */     return true;
/*      */   }
/*      */ 
/*      */   protected ResultSet syncViewValues(SchemaLoader loader, SchemaCacheItem valueSelector, Map args)
/*      */     throws DataException
/*      */   {
/*  861 */     SchemaCacheItem cacheItem = getCacheItem(valueSelector);
/*  862 */     synchronized (cacheItem)
/*      */     {
/*  864 */       Boolean forceDirtyCacheItem = (Boolean)args.get("forceDirtyCacheItem");
/*  865 */       if ((forceDirtyCacheItem != null) && (forceDirtyCacheItem.booleanValue()))
/*      */       {
/*  867 */         cacheItem.setDirty();
/*      */       }
/*      */       ResultSet rset;
/*  871 */       if ((loader.isDirty(cacheItem, args)) || ((!cacheItem.m_isComplete) && (loader.needsMoreData(cacheItem, args))))
/*      */       {
/*  874 */         loader.loadValues(this, this, cacheItem, args);
/*  875 */         ResultSet rset = cacheItem.getResultSet();
/*      */ 
/*  877 */         if (rset == null)
/*      */         {
/*  879 */           String msg = LocaleUtils.encodeMessage("apSchemaLoaderFailed", null, this.m_name);
/*      */ 
/*  881 */           Report.trace("schemaloader", "failed to load '" + this.m_name + "' using loader '" + loader.getClass().getName() + "' for " + cacheItem, new StackTrace());
/*      */ 
/*  886 */           throw new DataException(msg);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  891 */         rset = cacheItem.getResultSet();
/*      */       }
/*      */ 
/*  894 */       valueSelector.initShallow(cacheItem);
/*  895 */       if (this.m_lruManager != null)
/*      */       {
/*  897 */         this.m_lruManager.touchObject(this, cacheItem, true);
/*      */       }
/*      */ 
/*  900 */       return rset;
/*      */     }
/*      */   }
/*      */ 
/*      */   public int updateCache(SchemaCacheItem item, SchemaViewData viewDef, long timestamp, SchemaRelationData relation, ResultSet parentValues, ResultSet deletedData, ResultSet newData, DynamicHtmlMerger merger, ExecutionContext context, boolean isIncrementalUpdate, boolean isPartial, Map args)
/*      */     throws DataException
/*      */   {
/*  917 */     if ((viewDef == null) || (viewDef != this))
/*      */     {
/*  920 */       String msg = LocaleUtils.encodeMessage("apSchemaCacheManagementError", null);
/*      */ 
/*  922 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  925 */     String valuesString = null;
/*  926 */     String relationName = null;
/*  927 */     if (SystemUtils.isActiveTrace("schemacache"))
/*      */     {
/*  929 */       if (relation != null)
/*      */       {
/*  931 */         relationName = relation.m_name;
/*      */       }
/*      */ 
/*  934 */       IdcStringBuilder buf = new IdcStringBuilder();
/*  935 */       if (parentValues != null)
/*      */       {
/*  937 */         for (int i = 0; i < parentValues.getNumFields(); ++i)
/*      */         {
/*  939 */           buf.append(":");
/*  940 */           buf.append(parentValues.getStringValue(i));
/*      */         }
/*  942 */         buf.append(":");
/*      */       }
/*  944 */       valuesString = buf.toString();
/*  945 */       if (SystemUtils.m_verbose)
/*      */       {
/*  947 */         Report.debug("schemacache", "updateCache() called on view '" + this.m_name + "' via relation '" + relationName + "' with values " + valuesString + ".", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  954 */     String viewVersion = viewDef.get("schVersion");
/*  955 */     if (viewVersion == null)
/*      */     {
/*  957 */       viewVersion = "0";
/*      */     }
/*  959 */     String relationVersion = null;
/*  960 */     if (relation != null)
/*      */     {
/*  962 */       relationVersion = relation.get("schVersion");
/*      */     }
/*  964 */     if (relationVersion == null)
/*      */     {
/*  966 */       relationVersion = "0";
/*      */     }
/*      */ 
/*  969 */     SchemaTableData tableDef = viewDef.getTableDefinition();
/*  970 */     int rc = item.updateCache(deletedData, newData, merger, context, viewDef, tableDef, viewVersion, relationVersion, timestamp, isIncrementalUpdate, isPartial);
/*      */ 
/*  974 */     if (this.m_lruManager != null)
/*      */     {
/*  976 */       this.m_lruManager.touchObject(this, item, true);
/*      */     }
/*  978 */     return rc;
/*      */   }
/*      */ 
/*      */   public void markCacheDirty(SchemaViewData viewDef, SchemaRelationData relation, ResultSet parentValues)
/*      */     throws DataException
/*      */   {
/*  985 */     markCacheDirtyEx(viewDef, relation, parentValues, null);
/*      */   }
/*      */ 
/*      */   public void markCacheDirtyEx(SchemaViewData viewDef, SchemaRelationData relation, ResultSet parentValues, Map args)
/*      */     throws DataException
/*      */   {
/*  998 */     if ((viewDef == null) || (viewDef != this))
/*      */     {
/* 1001 */       String msg = LocaleUtils.encodeMessage("apSchemaCacheManagementError", null);
/*      */ 
/* 1003 */       throw new DataException(msg);
/*      */     }
/*      */ 
/* 1006 */     if (relation == null)
/*      */     {
/* 1008 */       markEverythingDirty();
/* 1009 */       return;
/*      */     }
/*      */ 
/* 1012 */     SchemaLoader loader = getViewLoader(relation, args);
/* 1013 */     String[] parentValuesArray = loader.constructParentValuesArray(null, relation, this, parentValues, args);
/*      */ 
/* 1015 */     int flags = 0;
/* 1016 */     flags = SchemaLoaderUtils.computeSchemaCacheItemFlags(args, flags);
/* 1017 */     SchemaCacheItem item = new SchemaCacheItem(this, relation, parentValuesArray, flags);
/*      */ 
/* 1019 */     item = (SchemaCacheItem)this.m_viewValues.get(item);
/* 1020 */     item.setDirty();
/*      */   }
/*      */ 
/*      */   public void addLocalizationInformation(DynamicHtmlMerger merger, ViewFieldInfo viewInfo, ExecutionContext context, ResultSet data)
/*      */     throws DataException
/*      */   {
/* 1052 */     if (viewInfo.m_localizationException != null)
/*      */     {
/* 1054 */       throw viewInfo.m_localizationException;
/*      */     }
/*      */ 
/* 1057 */     String sStart = "<$";
/* 1058 */     String sEnd = "$>";
/*      */ 
/* 1060 */     Map localeContexts = new HashMap();
/*      */ 
/* 1062 */     int size = viewInfo.m_fieldList.size();
/* 1063 */     DataBinder binder = null;
/* 1064 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1066 */       ViewLocaleInfo info = (ViewLocaleInfo)viewInfo.m_fieldList.get(i);
/* 1067 */       if (!info.m_isScript) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1071 */       if ((info.m_displayScript.startsWith(sStart)) && (info.m_displayScript.endsWith(sEnd)))
/*      */       {
/* 1074 */         String name = info.m_displayScript.substring(2, info.m_displayScript.length() - 2);
/* 1075 */         FieldInfo fInfo = new FieldInfo();
/* 1076 */         if (data.getFieldInfo(name, fInfo))
/*      */         {
/* 1078 */           info.m_isScript = false;
/* 1079 */           info.m_fieldName = name;
/*      */         }
/*      */       }
/*      */       else {
/* 1083 */         if (binder == null)
/*      */         {
/* 1085 */           binder = (DataBinder)context.getCachedObject("DataBinder");
/*      */ 
/* 1089 */           if (data.isEmpty())
/*      */           {
/* 1091 */             DataResultSet drset = new DataResultSet();
/* 1092 */             drset.copy(data);
/* 1093 */             int numFields = drset.getNumFields();
/* 1094 */             String[] dummyRow = new String[numFields];
/* 1095 */             Arrays.fill(dummyRow, "");
/* 1096 */             List dummyRowList = Arrays.asList(dummyRow);
/* 1097 */             drset.addRowWithList(dummyRowList);
/* 1098 */             binder.pushActiveResultSet("NewData", drset);
/*      */           }
/*      */           else
/*      */           {
/* 1102 */             binder.pushActiveResultSet("NewData", data);
/*      */           }
/*      */         }
/* 1105 */         String localeName = null;
/* 1106 */         int index = info.m_name.indexOf(".");
/* 1107 */         if (index > 0)
/*      */         {
/* 1109 */           localeName = info.m_name.substring(index + 1);
/*      */         }
/* 1111 */         DynamicHtml html = null;
/*      */         try
/*      */         {
/* 1114 */           html = merger.parseScriptInternal(info.m_displayScript);
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/* 1118 */           String msg = LocaleUtils.encodeMessage("apSchemaScriptError", null, info.m_displayScript, localeName, this.m_name);
/*      */ 
/* 1120 */           viewInfo.m_localizationException = new DataException(msg, e);
/* 1121 */           throw viewInfo.m_localizationException;
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/* 1127 */           String msg = LocaleUtils.encodeMessage("csSchUpdateCacheIdocStackMessage", null, info.m_name, this.m_name);
/*      */ 
/* 1129 */           info.m_localizationMessage = msg;
/*      */ 
/* 1139 */           binder.putLocal("isReportToErrorPage", "1");
/* 1140 */           binder.putLocal("isSchemaViewDataLocalizationCheck", "1");
/* 1141 */           merger.executeDynamicHtml(html);
/* 1142 */           String errMsg = binder.getLocal("scriptErrorReportMsg");
/* 1143 */           if ((errMsg != null) && (errMsg.length() > 0))
/*      */           {
/* 1145 */             msg = LocaleUtils.appendMessage(errMsg, msg);
/* 1146 */             Report.warning("schemacache", msg, null);
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*      */           String msg;
/* 1163 */           binder.removeLocal("isReportToErrorPage");
/* 1164 */           binder.removeLocal("scriptErrorReportMsg");
/* 1165 */           binder.removeLocal("isSchemaViewDataLocalizationCheck");
/*      */         }
/*      */ 
/* 1168 */         info.m_script = html;
/* 1169 */         ExecutionContext localeContext = (ExecutionContext)localeContexts.get(localeName);
/*      */         IdcLocale locale;
/* 1172 */         if (localeContext == null)
/*      */         {
/* 1174 */           IdcLocale locale = LocaleResources.getLocale(localeName);
/* 1175 */           if (locale == null)
/*      */           {
/* 1177 */             locale = LocaleResources.getSystemLocale();
/* 1178 */             if (!localeName.equals("default"))
/*      */             {
/* 1180 */               Report.trace("schemacache", "the locale '" + localeName + "' is not defined, using the system locale.", null);
/*      */             }
/*      */           }
/*      */ 
/* 1184 */           ExecutionContextAdaptor adaptor = new ExecutionContextAdaptor();
/* 1185 */           localeContext = adaptor;
/* 1186 */           adaptor.setParentContext(context);
/* 1187 */           adaptor.setCachedObject("UserLocale", locale);
/*      */         }
/*      */         else
/*      */         {
/* 1191 */           locale = (IdcLocale)localeContext.getCachedObject("UserLocale");
/*      */         }
/*      */ 
/* 1194 */         info.m_context = localeContext;
/* 1195 */         info.m_idcLocale = locale;
/*      */       }
/*      */     }
/* 1197 */     viewInfo.m_needsLocalization = false;
/*      */   }
/*      */ 
/*      */   public String computeDisplayValue(ResultSet rset, ExecutionContext context, int flags)
/*      */   {
/* 1211 */     FieldInfo theField = new FieldInfo();
/*      */ 
/* 1214 */     IdcLocale locale = (IdcLocale)context.getCachedObject("UserLocale");
/* 1215 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */     {
/* 1217 */       return null;
/*      */     }
/* 1219 */     if (locale != null)
/*      */     {
/* 1221 */       String fieldName = "Display." + locale.m_name;
/* 1222 */       if (rset.getFieldInfo(fieldName, theField))
/*      */       {
/* 1224 */         String theValue = (String)ResultSetUtils.getResultSetObject(rset, theField);
/* 1225 */         return theValue;
/*      */       }
/*      */     }
/* 1228 */     if (rset.getFieldInfo("Display.default", theField))
/*      */     {
/* 1230 */       String theValue = (String)ResultSetUtils.getResultSetObject(rset, theField);
/* 1231 */       if (((flags & 0x1) != 0) && (theValue.length() > 0) && 
/* 1233 */         (this.m_loadsUnconvertedValues) && (getBoolean("schLocalizeWhenDisplayed", false)))
/*      */       {
/* 1235 */         theValue = LocaleResources.getString(theValue, context);
/*      */       }
/*      */ 
/* 1238 */       return theValue;
/*      */     }
/* 1240 */     String fieldName = get("schLabelColumn");
/* 1241 */     if ((fieldName != null) && (rset.getFieldInfo(fieldName, theField)))
/*      */     {
/* 1243 */       String theValue = (String)ResultSetUtils.getResultSetObject(rset, theField);
/* 1244 */       return theValue;
/*      */     }
/* 1246 */     fieldName = get("schInternalColumn");
/* 1247 */     if ((fieldName != null) && (rset.getFieldInfo(fieldName, theField)))
/*      */     {
/* 1249 */       String theValue = (String)ResultSetUtils.getResultSetObject(rset, theField);
/* 1250 */       return theValue;
/*      */     }
/* 1252 */     return null;
/*      */   }
/*      */ 
/*      */   public ResultSet prepareForConsumption(ResultSet rset, ExecutionContext context, int flags)
/*      */     throws DataException
/*      */   {
/* 1268 */     if (((flags & 0x1) == 0) || (!getBoolean("schLocalizeWhenDisplayed", false)) || (!this.m_loadsUnconvertedValues))
/*      */     {
/* 1271 */       return rset;
/*      */     }
/* 1273 */     FieldInfo theField = new FieldInfo();
/* 1274 */     if (!rset.getFieldInfo("Display.default", theField))
/*      */     {
/* 1276 */       return rset;
/*      */     }
/* 1278 */     DataResultSet drset = new DataResultSet();
/* 1279 */     drset.copy(rset);
/* 1280 */     int rowIndex = 0;
/* 1281 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1283 */       String theValue = (String)ResultSetUtils.getResultSetObject(drset, theField);
/* 1284 */       String newValue = LocaleResources.getString(theValue, context);
/* 1285 */       if ((newValue != null) && (theValue != null) && (!theValue.equals(newValue)))
/*      */       {
/* 1287 */         drset.setCurrentValue(theField.m_index, newValue);
/*      */       }
/*      */       else
/*      */       {
/* 1291 */         Report.trace("schemacache", "No effect when localizing display value " + theValue + " for view " + this.m_name, null);
/*      */       }
/* 1293 */       ++rowIndex;
/*      */     }
/* 1295 */     return drset;
/*      */   }
/*      */ 
/*      */   public void markEverythingDirty()
/*      */   {
/* 1300 */     List items = new ArrayList();
/* 1301 */     synchronized (this)
/*      */     {
/* 1303 */       Iterator i = this.m_viewValues.keySet().iterator();
/* 1304 */       while (i.hasNext())
/*      */       {
/* 1306 */         items.add((SchemaCacheItem)i.next());
/*      */       }
/*      */     }
/*      */ 
/* 1310 */     for (SchemaCacheItem item : items)
/*      */     {
/* 1312 */       item.setDirty();
/*      */     }
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1318 */     return this.m_data.toString();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1323 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84240 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaViewData
 * JD-Core Version:    0.5.4
 */