/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Iterator;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SearchFieldInfo
/*     */ {
/*  32 */   public static final String[] DESIGN_COLUMNS = { "fieldName", "advOptions" };
/*     */ 
/*  34 */   protected Workspace m_workspace = null;
/*     */ 
/*     */   public void init(Workspace ws)
/*     */   {
/*  43 */     this.m_workspace = ws;
/*     */   }
/*     */ 
/*     */   public IndexerCollectionData loadFieldInfo(String engineName, DataBinder binder) {
/*  47 */     return loadFieldInfo(engineName, binder, this.m_workspace, null);
/*     */   }
/*     */ 
/*     */   public static IndexerCollectionData loadFieldInfo(String engineName, DataBinder binder, Workspace ws, IndexerCollectionData collectionDef)
/*     */   {
/*  54 */     if (collectionDef == null)
/*     */     {
/*  56 */       collectionDef = new IndexerCollectionData();
/*     */     }
/*  58 */     collectionDef.m_binder = binder;
/*     */     try
/*     */     {
/*  62 */       DataResultSet designSet = (DataResultSet)binder.getResultSet("SearchDesignOptions");
/*  63 */       Hashtable fieldDesignMap = loadCollectionDesign(engineName, designSet, binder);
/*     */ 
/*  68 */       boolean doCaseInsensitiveAcctSearch = SharedObjects.getEnvValueAsBoolean("DoCaseInsensitiveAcctSearch", true);
/*     */ 
/*  70 */       String caseInsensitive = (doCaseInsensitiveAcctSearch) ? "true" : "false";
/*  71 */       Properties props = new Properties();
/*  72 */       props.put("FieldAttributes", "isCaseInsensitive:" + caseInsensitive);
/*  73 */       addFieldProps(fieldDesignMap, props, "dDocAccount", false);
/*     */ 
/*  75 */       collectionDef.m_fieldDesignMap = fieldDesignMap;
/*     */ 
/*  78 */       String enableKey = "IndexDisabled";
/*  79 */       Hashtable fieldInfos = collectionDef.m_fieldInfos;
/*  80 */       HashMap tableColumns = new HashMap();
/*  81 */       for (Enumeration en = fieldDesignMap.keys(); en.hasMoreElements(); )
/*     */       {
/*  83 */         String fieldName = (String)en.nextElement();
/*  84 */         Properties designProps = (Properties)fieldDesignMap.get(fieldName);
/*  85 */         boolean isEnabled = !StringUtils.convertToBool(designProps.getProperty(enableKey), false);
/*  86 */         if (isEnabled)
/*     */         {
/*  89 */           addField(designProps, fieldInfos, tableColumns, ws);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  94 */       String isSupportZoneStr = binder.getLocal("isSupportZoneSearch");
/*  95 */       boolean isSupportZone = StringUtils.convertToBool(isSupportZoneStr, false);
/*  96 */       if (isSupportZone)
/*     */       {
/*  98 */         addSecurityInfos(engineName, collectionDef, binder);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 104 */       Report.trace("indexer", "SearchFieldInfo.loadFieldInfo:unable to build field list.", e);
/*     */     }
/* 106 */     return collectionDef;
/*     */   }
/*     */ 
/*     */   public IndexerCollectionData loadSearchFieldInfo(String engineName, IndexerCollectionData collectionDef, Workspace ws)
/*     */   {
/* 114 */     DataResultSet fieldSet = SharedObjects.getTable("SearchFieldInfo");
/* 115 */     String label = "(" + engineName + ")";
/* 116 */     IndexerCollectionData searchDef = new IndexerCollectionData();
/*     */ 
/* 119 */     Hashtable fieldInfos = (Hashtable)collectionDef.m_fieldInfos.clone();
/* 120 */     Hashtable fieldDesign = collectionDef.m_fieldDesignMap;
/*     */     try
/*     */     {
/* 123 */       String[] clmns = { "SearchFieldName", "SearchDisabled" };
/* 124 */       FieldInfo[] fi = ResultSetUtils.createInfoList(fieldSet, clmns, true);
/* 125 */       int nameIndex = fi[0].m_index;
/* 126 */       int enabledIndex = fi[1].m_index;
/* 127 */       HashMap tableColumns = new HashMap();
/*     */ 
/* 129 */       for (fieldSet.first(); fieldSet.isRowPresent(); fieldSet.next())
/*     */       {
/* 131 */         String name = fieldSet.getStringValue(nameIndex);
/* 132 */         if (name.startsWith("("))
/*     */         {
/* 134 */           String thisLabel = name.substring(1, name.indexOf(41));
/* 135 */           if ((!name.startsWith(label)) && (!engineName.startsWith(thisLabel + '.')))
/*     */             continue;
/* 137 */           name = name.substring(thisLabel.length() + 2);
/*     */         }
/*     */ 
/* 144 */         boolean isEnabled = !StringUtils.convertToBool(fieldSet.getStringValue(enabledIndex), false);
/* 145 */         if (isEnabled)
/*     */         {
/* 148 */           FieldInfo info = (FieldInfo)fieldInfos.get(name);
/* 149 */           if (info == null)
/*     */           {
/* 151 */             Properties props = (Properties)fieldDesign.get(name);
/* 152 */             if (props == null)
/*     */             {
/* 154 */               props = fieldSet.getCurrentRowProps();
/* 155 */               props.put("fieldName", name);
/*     */             }
/* 157 */             addField(props, fieldInfos, tableColumns, ws);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 162 */           fieldInfos.remove(name);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 168 */       Report.trace("indexer", "SearchFieldInfo.loadSearchFieldInfo:unable to build field list.", e);
/*     */     }
/* 170 */     searchDef.m_fieldInfos = fieldInfos;
/* 171 */     searchDef.m_fieldDesignMap = collectionDef.m_fieldDesignMap;
/* 172 */     return searchDef;
/*     */   }
/*     */ 
/*     */   public static Hashtable loadCollectionDesign(String engineName, DataResultSet designSet, DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 178 */     IndexerCollectionData collectionDef = new IndexerCollectionData();
/* 179 */     DataResultSet fieldSet = null;
/*     */ 
/* 182 */     boolean isFullLoad = false;
/* 183 */     if (binder != null)
/*     */     {
/* 185 */       fieldSet = (DataResultSet)binder.getResultSet("SearchFieldInfo");
/*     */     }
/* 187 */     if (fieldSet == null)
/*     */     {
/* 189 */       fieldSet = SharedObjects.getTable("SearchFieldInfo");
/* 190 */       isFullLoad = true;
/*     */     }
/*     */ 
/* 193 */     Hashtable fieldDesignMap = collectionDef.m_fieldDesignMap;
/*     */     try
/*     */     {
/* 196 */       String label = "(" + engineName.toUpperCase() + ")";
/* 197 */       String[] clmns = { "SearchFieldName" };
/* 198 */       FieldInfo[] fi = ResultSetUtils.createInfoList(fieldSet, clmns, true);
/* 199 */       int nameIndex = fi[0].m_index;
/*     */ 
/* 201 */       for (fieldSet.first(); fieldSet.isRowPresent(); fieldSet.next())
/*     */       {
/* 203 */         String name = fieldSet.getStringValue(nameIndex);
/* 204 */         if (name.startsWith("("))
/*     */         {
/* 206 */           String thisLabel = name.substring(1, name.indexOf(41));
/* 207 */           if ((!name.startsWith(label)) && (!engineName.startsWith(thisLabel + '.')))
/*     */             continue;
/* 209 */           name = name.substring(thisLabel.length() + 2);
/*     */         }
/*     */ 
/* 216 */         addFieldProps(fieldDesignMap, fieldSet.getCurrentRowProps(), name, false);
/*     */       }
/*     */ 
/* 221 */       addConfigInfo(engineName, collectionDef);
/*     */ 
/* 223 */       if (isFullLoad)
/*     */       {
/* 227 */         DataResultSet metaDefRset = SharedObjects.getTable("DocMetaDefinition");
/* 228 */         if (metaDefRset == null)
/*     */         {
/* 230 */           metaDefRset = (DataResultSet)binder.getResultSet("DocMetaDefinition");
/*     */         }
/*     */ 
/* 233 */         if (metaDefRset != null)
/*     */         {
/* 235 */           if (binder != null)
/*     */           {
/* 237 */             binder.addResultSet("DocMetaDefinition", metaDefRset);
/*     */           }
/* 239 */           PluginFilters.filter("preSearchFieldInfoLoad", null, binder, new ExecutionContextAdaptor());
/*     */ 
/* 241 */           nameIndex = ResultSetUtils.getIndexMustExist(metaDefRset, "dName");
/* 242 */           int searchIndex = ResultSetUtils.getIndexMustExist(metaDefRset, "dIsSearchable");
/* 243 */           for (metaDefRset.first(); metaDefRset.isRowPresent(); metaDefRset.next())
/*     */           {
/* 245 */             String name = metaDefRset.getStringValue(nameIndex);
/* 246 */             boolean isSearchable = StringUtils.convertToBool(metaDefRset.getStringValue(searchIndex), false);
/* 247 */             if (isSearchable)
/*     */             {
/* 249 */               Properties props = metaDefRset.getCurrentRowProps();
/* 250 */               props.put("FieldAttributes", "baseTable:DocMeta");
/* 251 */               addFieldProps(fieldDesignMap, props, name, false);
/*     */             }
/*     */             else
/*     */             {
/* 263 */               fieldDesignMap.remove(name);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 270 */       addDesignInfo(engineName, fieldDesignMap, designSet);
/*     */ 
/* 272 */       ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 273 */       Object[] objs = { engineName, binder, designSet, fieldDesignMap };
/* 274 */       cxt.setCachedObject("SearchFieldDesignMapObjs", objs);
/* 275 */       PluginFilters.filter("SearchFieldDesignMap", null, null, cxt);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 281 */       Report.trace(null, "SearchFieldInfo.loadCollectionDesign:unable to load search fields.", e);
/*     */     }
/*     */ 
/* 286 */     if ((isFullLoad) && (designSet != null))
/*     */     {
/* 288 */       SharedObjects.putTable("SearchDesignInfo", designSet);
/*     */     }
/* 290 */     return fieldDesignMap;
/*     */   }
/*     */ 
/*     */   protected static void addConfigInfo(String engineName, IndexerCollectionData collectionDef)
/*     */   {
/* 295 */     Hashtable designMap = collectionDef.m_fieldDesignMap;
/* 296 */     Properties props = null;
/*     */ 
/* 299 */     int count = 1;
/* 300 */     for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*     */     {
/* 302 */       String key = "dRendition" + count;
/* 303 */       props = new Properties();
/* 304 */       props.put("fieldName", key);
/* 305 */       props.put("dCaption", "apTitleRendition" + count);
/* 306 */       props.put("dType", "text");
/* 307 */       props.put("dIsSearchable", "1");
/* 308 */       props.put("length", "1");
/* 309 */       designMap.put(key, props);
/*     */ 
/* 300 */       ++i;
/*     */     }
/*     */ 
/* 312 */     if (!engineName.startsWith("VERITY"))
/*     */     {
/* 314 */       return;
/*     */     }
/*     */ 
/* 323 */     String[] defaultHasOwnTableFields = { "dDocTitle", "dDocAccount", "dSecurityGroup" };
/* 324 */     for (int i = 0; i < defaultHasOwnTableFields.length; ++i)
/*     */     {
/* 326 */       String fieldName = defaultHasOwnTableFields[i];
/* 327 */       props = (Properties)designMap.get(fieldName);
/* 328 */       if (props == null)
/*     */       {
/* 330 */         props = new Properties();
/* 331 */         designMap.put(fieldName, props);
/*     */       }
/* 333 */       props.put("advOptions", "hasDataTable,1");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void addDesignInfo(String engineName, Hashtable fieldMap, DataResultSet drset)
/*     */     throws ServiceException, DataException
/*     */   {
/* 345 */     if (drset == null)
/*     */       return;
/* 347 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "fieldName");
/* 348 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 350 */       String fieldName = drset.getStringValue(nameIndex);
/* 351 */       addFieldProps(fieldMap, drset.getCurrentRowProps(), fieldName, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void addSecurityInfos(String engineName, IndexerCollectionData collectionDef, DataBinder binder)
/*     */   {
/* 363 */     Hashtable fieldMap = collectionDef.m_fieldDesignMap;
/* 364 */     Hashtable securityInfos = collectionDef.m_securityInfos;
/*     */ 
/* 366 */     String zoneStr = binder.getLocal("ZonedSecurityFields");
/* 367 */     if (zoneStr == null)
/*     */     {
/* 369 */       CommonSearchConfig csc = (CommonSearchConfig)SharedObjects.getObject("globalObjects", "CommonSearchConfig");
/* 370 */       zoneStr = csc.getEngineValue(engineName, "ZonedSecurityFields");
/*     */ 
/* 379 */       String useImplicitSecurityFieldStr = ActiveIndexState.getActiveProperty("UseImplicitZonedSecurityField");
/* 380 */       String implicitFields = csc.getEngineValue(engineName, "ImplicitZonedSecurityFields");
/* 381 */       String validImplicitFields = null;
/* 382 */       boolean useImplicitFields = StringUtils.convertToBool(useImplicitSecurityFieldStr, false);
/*     */ 
/* 385 */       String cycleId = binder.getAllowMissing("indexerCycleId");
/* 386 */       if ((cycleId != null) && (cycleId.equalsIgnoreCase("rebuild")))
/*     */       {
/* 388 */         useImplicitFields = true;
/* 389 */         validImplicitFields = implicitFields;
/*     */       }
/* 391 */       if (useImplicitFields)
/*     */       {
/* 393 */         if (validImplicitFields == null)
/*     */         {
/* 395 */           String ImplicitSecurityFieldListStr = ActiveIndexState.getActiveProperty("ImplicitZonedSecurityFieldsList");
/* 396 */           if (ImplicitSecurityFieldListStr != null)
/*     */           {
/* 398 */             Vector implicitFieldsVector = StringUtils.parseArrayEx(implicitFields, ',', '^', true);
/*     */ 
/* 400 */             int num = implicitFieldsVector.size();
/* 401 */             validImplicitFields = "";
/* 402 */             for (int i = 0; i < num; ++i)
/*     */             {
/* 404 */               String fieldName = (String)implicitFieldsVector.elementAt(i);
/* 405 */               if (!ImplicitSecurityFieldListStr.contains(fieldName))
/*     */                 continue;
/* 407 */               validImplicitFields = validImplicitFields + "," + fieldName;
/*     */             }
/*     */ 
/* 410 */             int validLength = validImplicitFields.length();
/* 411 */             validImplicitFields = validImplicitFields.substring(1, validLength);
/*     */           }
/*     */           else
/*     */           {
/* 416 */             validImplicitFields = "dSecurityGroup";
/*     */           }
/*     */         }
/*     */ 
/* 420 */         if ((zoneStr == null) || (zoneStr.length() == 0))
/*     */         {
/* 422 */           zoneStr = validImplicitFields;
/*     */         }
/* 424 */         else if ((implicitFields != null) && (implicitFields.length() != 0))
/*     */         {
/* 426 */           zoneStr = zoneStr + ',' + validImplicitFields;
/*     */         }
/*     */       }
/*     */     }
/* 430 */     Vector fields = StringUtils.parseArrayEx(zoneStr, ',', '^', true);
/*     */ 
/* 432 */     int num = fields.size();
/* 433 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 435 */       String fieldName = (String)fields.elementAt(i);
/* 436 */       FieldInfo info = new FieldInfo();
/* 437 */       info.m_name = fieldName;
/* 438 */       info.m_type = 6;
/*     */ 
/* 442 */       Properties props = (Properties)fieldMap.get(fieldName);
/* 443 */       if (props == null)
/*     */       {
/* 446 */         Report.trace("indexer", "SearchFieldInfo.addSecurityInfos: the security field " + fieldName + " is not defined.", null);
/*     */       }
/*     */ 
/* 449 */       securityInfos.put(fieldName, info);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static Properties addFieldProps(Hashtable fieldDesignMap, Properties props, String name, boolean isMergeOnly)
/*     */   {
/* 455 */     Properties fieldProps = (Properties)fieldDesignMap.get(name);
/* 456 */     props.put("fieldName", name);
/* 457 */     checkAndAddDefaultProps(props, fieldProps);
/* 458 */     expandAdditionalProps(props);
/*     */ 
/* 460 */     if (fieldProps == null)
/*     */     {
/* 462 */       if (!isMergeOnly)
/*     */       {
/* 464 */         fieldDesignMap.put(name, props);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 469 */       DataBinder.mergeHashTables(fieldProps, props);
/*     */     }
/* 471 */     return props;
/*     */   }
/*     */ 
/*     */   protected static void checkAndAddDefaultProps(Properties props, Properties fieldProps)
/*     */   {
/* 476 */     String tmp = props.getProperty("SearchFieldType");
/* 477 */     if (tmp == null)
/*     */     {
/* 479 */       tmp = props.getProperty("dType");
/* 480 */       if (tmp != null)
/*     */       {
/* 482 */         tmp = normalizeFieldType(tmp);
/* 483 */         props.put("SearchFieldType", tmp);
/*     */       }
/*     */     }
/*     */ 
/* 487 */     tmp = props.getProperty("IsSortable");
/* 488 */     if ((tmp == null) && (((fieldProps == null) || (fieldProps.get("IsSortable") == null))))
/*     */     {
/* 490 */       props.put("IsSortable", "");
/*     */     }
/*     */ 
/* 493 */     tmp = props.getProperty("SearchDisabled");
/* 494 */     if ((tmp == null) && (((fieldProps == null) || (fieldProps.get("SearchDisabled") == null))))
/*     */     {
/* 496 */       props.put("SearchDisabled", "");
/*     */     }
/*     */ 
/* 499 */     tmp = props.getProperty("IndexDisabled");
/* 500 */     if ((tmp == null) && (((fieldProps == null) || (fieldProps.get("IndexDisabled") == null))))
/*     */     {
/* 502 */       props.put("IndexDisabled", "");
/*     */     }
/*     */ 
/* 505 */     tmp = props.getProperty("SearchFieldName");
/* 506 */     if (tmp != null)
/*     */       return;
/* 508 */     tmp = props.getProperty("fieldName");
/* 509 */     if (tmp == null)
/*     */       return;
/* 511 */     props.put("SearchFieldName", tmp);
/*     */   }
/*     */ 
/*     */   protected static String normalizeFieldType(String type)
/*     */   {
/* 518 */     if (type == null)
/*     */     {
/* 520 */       return type;
/*     */     }
/* 522 */     type = type.toLowerCase();
/* 523 */     if ((type.equals("string")) || (type.indexOf("text") > 0) || (type.equals("memo")))
/*     */     {
/* 525 */       type = FieldInfo.FIELD_NAMES[6];
/*     */     }
/* 527 */     return type;
/*     */   }
/*     */ 
/*     */   protected static void expandAdditionalProps(Properties props)
/*     */   {
/* 532 */     String attribs = props.getProperty("FieldAttributes");
/* 533 */     if (attribs == null)
/*     */     {
/* 535 */       return;
/*     */     }
/* 537 */     ArrayList strArray = new ArrayList();
/* 538 */     StringUtils.appendListFromSequence(strArray, attribs, 0, attribs.length(), '\n', '\n', 32);
/*     */ 
/* 540 */     String[] attribList = new String[strArray.size()];
/* 541 */     strArray.toArray(attribList);
/* 542 */     for (int i = 0; i < attribList.length; ++i)
/*     */     {
/* 544 */       ArrayList values = (ArrayList)StringUtils.appendListFromSequence(null, attribList[i], 0, attribList[i].length(), ':', '^', 32);
/*     */ 
/* 546 */       if (values == null)
/*     */         continue;
/* 548 */       if (values.size() == 2)
/*     */       {
/* 550 */         String key = (String)values.get(0);
/* 551 */         String value = (String)values.get(1);
/* 552 */         props.setProperty(key, value);
/*     */       }
/*     */       else
/*     */       {
/* 556 */         Report.trace(null, "Invalid Search field FieldAttributes setting: " + attribList[i], null);
/*     */       }
/*     */     }
/*     */ 
/* 560 */     props.remove("FieldAttributes");
/*     */   }
/*     */ 
/*     */   protected static void addField(Properties props, Hashtable fields, HashMap tableColumns, Workspace ws)
/*     */   {
/* 565 */     FieldInfo info = new FieldInfo();
/* 566 */     info.m_name = props.getProperty("fieldName");
/*     */ 
/* 568 */     String type = props.getProperty("SearchFieldType");
/* 569 */     if (type == null)
/*     */     {
/* 571 */       type = props.getProperty("dType");
/*     */     }
/*     */ 
/* 574 */     if (type != null)
/*     */     {
/* 576 */       type = type.toLowerCase();
/* 577 */       if (type.equals("date"))
/*     */       {
/* 579 */         info.m_type = 5;
/*     */       }
/* 581 */       else if (type.equals("int"))
/*     */       {
/* 583 */         info.m_type = 3;
/*     */       }
/* 585 */       else if (type.equals("decimal"))
/*     */       {
/* 587 */         info.m_type = 11;
/* 588 */         info.m_maxLen = 38;
/* 589 */         String scale = props.getProperty("dDecimalScale");
/* 590 */         info.m_scale = NumberUtils.parseInteger(scale, 1);
/*     */       }
/* 592 */       else if ((type.equals("string")) || (type.equals("text")) || (type.equals("memo")) || (type.equals("varchar")))
/*     */       {
/* 594 */         info.m_type = 6;
/*     */ 
/* 597 */         String fieldLen = props.getProperty("length");
/* 598 */         if (fieldLen != null)
/*     */         {
/* 600 */           info.m_isFixedLen = true;
/* 601 */           info.m_maxLen = NumberUtils.parseInteger(fieldLen, info.m_maxLen);
/*     */         }
/*     */         else
/*     */         {
/* 605 */           String baseTable = props.getProperty("baseTable");
/* 606 */           loadColumn(ws, tableColumns, baseTable);
/* 607 */           if ((baseTable != null) && (baseTable.trim().length() > 0))
/*     */           {
/* 609 */             String baseColumn = props.getProperty("baseColumn");
/* 610 */             if (baseColumn == null)
/*     */             {
/* 612 */               baseColumn = info.m_name;
/*     */             }
/* 614 */             String key = baseTable + "." + baseColumn;
/* 615 */             FieldInfo columnInfo = (FieldInfo)tableColumns.get(key.toLowerCase());
/* 616 */             if (columnInfo != null)
/*     */             {
/* 618 */               info.m_isFixedLen = columnInfo.m_isFixedLen;
/* 619 */               info.m_maxLen = columnInfo.m_maxLen;
/* 620 */               props.setProperty("length", "" + info.m_maxLen);
/*     */             }
/*     */           }
/*     */         }
/* 624 */         props.put("SearchFieldType", info.getTypeName());
/*     */       }
/*     */     }
/*     */ 
/* 628 */     fields.put(info.m_name, info);
/*     */ 
/* 631 */     String str = props.getProperty("advOptions");
/* 632 */     Vector options = StringUtils.parseArray(str, ',', '^');
/* 633 */     int size = options.size();
/* 634 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 636 */       String opt = (String)options.elementAt(i);
/* 637 */       ++i;
/* 638 */       String val = (String)options.elementAt(i);
/* 639 */       props.put(opt, val);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void loadColumn(Workspace ws, HashMap tableColumns, String tableName)
/*     */   {
/* 645 */     if ((tableName == null) || (tableName.length() == 0) || (tableColumns == null))
/*     */     {
/* 647 */       return;
/*     */     }
/* 649 */     String tableKey = tableName + "_isLoaded";
/* 650 */     String isLoaded = (String)tableColumns.get(tableKey.toLowerCase());
/* 651 */     if (isLoaded != null)
/*     */     {
/* 653 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 657 */       if (ws != null)
/*     */       {
/* 659 */         FieldInfo[] columnInfos = ws.getColumnList(tableName);
/* 660 */         for (int i = 0; (columnInfos != null) && (i < columnInfos.length); ++i)
/*     */         {
/* 662 */           String key = tableName + "." + columnInfos[i].m_name;
/* 663 */           tableColumns.put(key.toLowerCase(), columnInfos[i]);
/*     */         }
/* 665 */         tableColumns.put(tableKey.toLowerCase(), "1");
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 670 */       Report.trace(null, "Unable to retrieve column list from table '" + tableName + "'. ", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean compareConfigurations(IndexerCollectionData currentDesignDef, IndexerCollectionData collectionDef)
/*     */   {
/* 676 */     return compareConfigurationsEx(currentDesignDef, collectionDef, null);
/*     */   }
/*     */ 
/*     */   public static boolean compareConfigurationsEx(IndexerCollectionData currentDesignDef, IndexerCollectionData collectionDef, HashMap optionalFields)
/*     */   {
/* 685 */     boolean broken = false;
/*     */ 
/* 692 */     Hashtable designFields = (Hashtable)currentDesignDef.m_fieldInfos.clone();
/* 693 */     Hashtable designFieldPropMap = currentDesignDef.m_fieldDesignMap;
/* 694 */     Hashtable designFieldStateMap = currentDesignDef.m_fieldStates;
/* 695 */     Hashtable fields = collectionDef.m_fieldInfos;
/* 696 */     Hashtable fieldPropMap = collectionDef.m_fieldDesignMap;
/* 697 */     Hashtable fieldStateMap = collectionDef.m_fieldStates;
/*     */ 
/* 700 */     DataBinder designBinder = currentDesignDef.m_binder;
/* 701 */     DataBinder binder = collectionDef.m_binder;
/* 702 */     DataBinder.mergeHashTables(binder.getLocalData(), designBinder.getLocalData());
/*     */ 
/* 704 */     for (Enumeration e = fields.keys(); e.hasMoreElements(); )
/*     */     {
/* 706 */       String name = (String)e.nextElement();
/*     */ 
/* 708 */       FieldInfo fieldInfo = (FieldInfo)fields.get(name);
/* 709 */       FieldInfo designInfo = (FieldInfo)designFields.get(name);
/* 710 */       Properties props = (Properties)fieldPropMap.get(name);
/* 711 */       if (props == null)
/*     */       {
/* 715 */         props = new Properties();
/* 716 */         fieldPropMap.put(name, props);
/*     */       }
/*     */ 
/* 719 */       boolean inError = false;
/* 720 */       boolean inSync = true;
/* 721 */       Vector states = new IdcVector();
/* 722 */       states.addElement("isIndexed");
/* 723 */       if (designInfo == null)
/*     */       {
/* 725 */         states.addElement("isExtra");
/* 726 */         inSync = false;
/* 727 */         inError = true;
/*     */       }
/*     */       else
/*     */       {
/* 731 */         Properties designProps = (Properties)designFieldPropMap.get(name);
/*     */ 
/* 734 */         String[] pushKeys = { "isInSearchResult", "isZoneSearch" };
/* 735 */         for (int i = 0; i < pushKeys.length; ++i)
/*     */         {
/* 737 */           String key = pushKeys[i];
/* 738 */           String value = designProps.getProperty(key);
/* 739 */           if (value == null)
/*     */             continue;
/* 741 */           props.put(key, value);
/*     */         }
/*     */ 
/* 746 */         if ((designInfo.m_type != -1) && (designInfo.m_type != fieldInfo.m_type))
/*     */         {
/* 748 */           inError = true;
/* 749 */           states.addElement("isMismatch");
/*     */         }
/*     */         else
/*     */         {
/* 753 */           boolean isString = fieldInfo.m_type == 6;
/* 754 */           String[] keys = { "isZone", "hasDataTable", "isOptimized" };
/*     */ 
/* 757 */           for (int i = 0; i < keys.length; ++i)
/*     */           {
/* 759 */             String key = keys[i];
/*     */ 
/* 761 */             boolean curVal = StringUtils.convertToBool(props.getProperty(key), false);
/* 762 */             if (curVal)
/*     */             {
/* 764 */               states.addElement(key);
/*     */             }
/* 766 */             if (!isString)
/*     */               continue;
/* 768 */             boolean def = StringUtils.convertToBool(designProps.getProperty(key), false);
/* 769 */             if (def == curVal) {
/*     */               continue;
/*     */             }
/*     */ 
/* 773 */             inError = true;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 778 */       designFields.remove(name);
/*     */ 
/* 780 */       if (inError)
/*     */       {
/* 782 */         broken = true;
/* 783 */         inSync = false;
/*     */       }
/*     */ 
/* 787 */       Properties stateProps = (Properties)fieldStateMap.get(name);
/* 788 */       if (stateProps == null)
/*     */       {
/* 790 */         stateProps = new Properties();
/* 791 */         fieldStateMap.put(name, stateProps);
/*     */       }
/* 793 */       String state = StringUtils.createString(states, ',', '^');
/* 794 */       stateProps.put("isInSync", "" + inSync);
/* 795 */       stateProps.put("currentState", state);
/*     */ 
/* 797 */       if (designInfo != null)
/*     */       {
/* 799 */         Properties designStateProps = (Properties)designFieldStateMap.get(name);
/* 800 */         if (designStateProps == null)
/*     */         {
/* 802 */           designStateProps = new Properties();
/* 803 */           designFieldStateMap.put(name, stateProps);
/*     */         }
/* 805 */         designStateProps.put("isInSync", "" + inSync);
/* 806 */         designStateProps.put("currentState", state);
/*     */       }
/*     */     }
/*     */ 
/* 810 */     for (Enumeration e = designFields.elements(); e.hasMoreElements(); )
/*     */     {
/* 812 */       FieldInfo info = (FieldInfo)e.nextElement();
/* 813 */       if ((optionalFields != null) && (optionalFields.get(info.m_name.toLowerCase()) != null))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 818 */       Properties props = (Properties)designFieldStateMap.get(info.m_name);
/* 819 */       if (props == null)
/*     */       {
/* 821 */         props = new Properties();
/* 822 */         designFieldStateMap.put(info.m_name, props);
/*     */       }
/* 824 */       broken = true;
/* 825 */       String state = "isMissing";
/* 826 */       props.put("isInSync", "0");
/* 827 */       props.put("currentState", state);
/*     */     }
/* 829 */     return !broken;
/*     */   }
/*     */ 
/*     */   public static void setActiveIndexPropsOnCleanup(Hashtable securityInfos)
/*     */   {
/* 835 */     Hashtable securityFields = securityInfos;
/* 836 */     Set fieldSet = securityFields.keySet();
/* 837 */     Iterator i = fieldSet.iterator();
/* 838 */     String fieldString = "";
/* 839 */     while (i.hasNext())
/*     */     {
/* 841 */       fieldString = (String)i.next() + "," + fieldString;
/*     */     }
/* 843 */     if (fieldString.length() <= 0)
/*     */       return;
/* 845 */     int len = fieldString.length() - 1;
/* 846 */     fieldString = fieldString.substring(0, len);
/* 847 */     ActiveIndexState.setActiveProperty("ImplicitZonedSecurityFieldsList", fieldString);
/* 848 */     ActiveIndexState.setActiveProperty("UseImplicitZonedSecurityField", "true");
/* 849 */     ActiveIndexState.setActiveProperty("UseUpperCaseZonedSecurityField", "true");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 855 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98270 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.SearchFieldInfo
 * JD-Core Version:    0.5.4
 */