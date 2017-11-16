/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaCacheItem;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaLoader;
/*     */ import intradoc.shared.schema.SchemaLoaderUtils;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaResultSet;
/*     */ import intradoc.shared.schema.SchemaTableConfig;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.shared.schema.ViewCacheCallback;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SharedObjectsTableLoader extends ExecutionContextAdaptor
/*     */   implements SchemaLoader
/*     */ {
/*     */   protected SchemaUtils m_schemaUtils;
/*     */   protected SchemaViewConfig m_views;
/*     */   protected SchemaTableConfig m_tables;
/*     */   protected SchemaRelationConfig m_relations;
/*     */   protected Map m_capabilities;
/*     */   protected Map m_lastGlobalViewTimestamps;
/*     */   protected boolean m_doPartialLoads;
/*     */ 
/*     */   public SharedObjectsTableLoader()
/*     */   {
/*  44 */     this.m_lastGlobalViewTimestamps = new Hashtable();
/*  45 */     this.m_doPartialLoads = false;
/*     */   }
/*     */ 
/*     */   public boolean init(Map initData) throws ServiceException {
/*  49 */     this.m_schemaUtils = ((SchemaUtils)initData.get("SchemaUtils"));
/*  50 */     if (this.m_schemaUtils == null)
/*     */     {
/*  52 */       this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", "!csSchemaUnableToLoadUtils"));
/*     */ 
/*  56 */       this.m_schemaUtils.init(initData);
/*     */     }
/*     */ 
/*  59 */     this.m_views = ((SchemaViewConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaViewConfig"));
/*     */ 
/*  61 */     this.m_tables = ((SchemaTableConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaTableConfig"));
/*     */ 
/*  63 */     this.m_relations = ((SchemaRelationConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaRelationConfig"));
/*     */ 
/*  66 */     this.m_capabilities = new Hashtable();
/*  67 */     SchemaLoaderUtils.addStandardCapabilities(this.m_capabilities, 7);
/*     */ 
/*  72 */     return true;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaViewData view, Map extraData)
/*     */   {
/*  77 */     String viewType = view.get("schViewType");
/*  78 */     if ((viewType != null) && (viewType.equalsIgnoreCase("sharedobjectstable")))
/*     */     {
/*  80 */       return this.m_capabilities;
/*     */     }
/*  82 */     return new Hashtable();
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaRelationData relation, Map extraData)
/*     */   {
/*  87 */     if (relation != null)
/*     */     {
/*  89 */       String relationType = relation.get("schRelationType");
/*  90 */       if ((relationType == null) || (!relationType.equals("view")))
/*     */       {
/*  92 */         return new Hashtable();
/*     */       }
/*     */     }
/*     */ 
/*  96 */     Hashtable capabilities = new Hashtable();
/*  97 */     SchemaLoaderUtils.addStandardCapabilities(capabilities, 7);
/*     */ 
/* 101 */     return capabilities;
/*     */   }
/*     */ 
/*     */   public boolean isDirty(SchemaCacheItem item, Map args) throws DataException
/*     */   {
/* 106 */     String viewName = item.getViewName();
/* 107 */     SchemaViewData viewDefinition = (SchemaViewData)this.m_views.getData(viewName);
/*     */ 
/* 109 */     if (viewDefinition == null)
/*     */     {
/* 111 */       Report.trace("schemaloader", "isDirty() view '" + viewName + "' doesn't exist.", null);
/*     */ 
/* 115 */       return false;
/*     */     }
/*     */ 
/* 118 */     boolean isDirty = item.isMarkedDirty();
/* 119 */     if (isDirty)
/*     */     {
/* 121 */       if (SystemUtils.m_verbose)
/*     */       {
/* 123 */         Report.debug("schemaloader", "item '" + item + "' is dirty because it is marked.", null);
/*     */       }
/*     */ 
/* 126 */       return true;
/*     */     }
/*     */ 
/* 129 */     isDirty = item.objectVersionsDirty(viewDefinition, null, null);
/*     */ 
/* 131 */     if (isDirty)
/*     */     {
/* 133 */       if (SystemUtils.m_verbose)
/*     */       {
/* 135 */         Report.debug("schemaloader", "item '" + item + "' dirty due to version mismatch.", null);
/*     */       }
/*     */ 
/* 138 */       return true;
/*     */     }
/*     */ 
/* 141 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean needsMoreData(SchemaCacheItem item, Map args)
/*     */   {
/* 146 */     return !item.isComplete();
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getParentViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 152 */     String type = relationship.get("schRelationType");
/* 153 */     String viewName = relationship.get("schView1Name");
/* 154 */     if ((type == null) || (viewName == null) || (!type.equalsIgnoreCase("view")))
/*     */     {
/* 156 */       return new SchemaViewData[0];
/*     */     }
/* 158 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(viewName);
/* 159 */     if (view == null)
/*     */     {
/* 161 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_view", null, viewName);
/*     */ 
/* 163 */       throw new DataException(msg);
/*     */     }
/* 165 */     return new SchemaViewData[] { view };
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getChildViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 171 */     String type = relationship.get("schRelationType");
/* 172 */     String viewName = relationship.get("schView2Name");
/* 173 */     if ((type == null) || (viewName == null) || (!type.equalsIgnoreCase("view")))
/*     */     {
/* 175 */       return new SchemaViewData[0];
/*     */     }
/* 177 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(viewName);
/* 178 */     if (view == null)
/*     */     {
/* 180 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_view", null, viewName);
/*     */ 
/* 182 */       throw new DataException(msg);
/*     */     }
/* 184 */     if (view.get("schViewType").equalsIgnoreCase("sharedobjectstable"))
/*     */     {
/* 186 */       return new SchemaViewData[] { view };
/*     */     }
/* 188 */     return new SchemaViewData[0];
/*     */   }
/*     */ 
/*     */   public SchemaRelationData[] getParentRelations(SchemaViewData childView)
/*     */     throws DataException
/*     */   {
/* 194 */     ArrayList list = new ArrayList();
/* 195 */     SchemaRelationConfig relations = (SchemaRelationConfig)this.m_relations.shallowClone();
/* 196 */     for (relations.first(); relations.isRowPresent(); relations.next())
/*     */     {
/* 198 */       SchemaData relation = relations.getData();
/* 199 */       String type = relation.get("schRelationType");
/* 200 */       if (type == null) continue; if (!type.equalsIgnoreCase("view"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 205 */       String view2 = relation.get("schView2Name");
/* 206 */       if (!view2.equalsIgnoreCase(childView.m_name))
/*     */         continue;
/* 208 */       list.add(relation);
/*     */     }
/*     */ 
/* 211 */     return (SchemaRelationData[])(SchemaRelationData[])list.toArray(new SchemaRelationData[0]);
/*     */   }
/*     */ 
/*     */   public String[] constructParentFieldsArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, Map args)
/*     */     throws DataException
/*     */   {
/* 218 */     if (parentView != null)
/*     */     {
/* 220 */       String viewType = parentView.get("schViewType");
/* 221 */       if ((viewType == null) || (!viewType.equalsIgnoreCase("sharedobjectstable")))
/*     */       {
/* 223 */         return null;
/*     */       }
/*     */     }
/* 226 */     String field = relation.get("schView1Column");
/* 227 */     return new String[] { field };
/*     */   }
/*     */ 
/*     */   public String[] constructParentValuesArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, ResultSet resultSet, Map args)
/*     */     throws DataException
/*     */   {
/* 235 */     SchemaHelper helper = new SchemaHelper();
/* 236 */     String[] fields = constructParentFieldsArray(parentView, relation, childView, args);
/*     */ 
/* 238 */     String[] values = helper.constructParentValuesArray(fields, resultSet);
/* 239 */     return values;
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData viewDef, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 245 */     String relationName = (String)args.get("relationName");
/* 246 */     ResultSet parentValues = (ResultSet)args.get("parentValues");
/* 247 */     String[] primaryKeyValue = null;
/* 248 */     boolean isPartial = false;
/* 249 */     if (this.m_doPartialLoads)
/*     */     {
/* 251 */       primaryKeyValue = (String[])(String[])args.get("primaryKey");
/*     */     }
/* 253 */     SchemaRelationData relation = null;
/*     */ 
/* 255 */     DataBinder binder = new DataBinder();
/* 256 */     ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/* 257 */     PageMerger merger = new PageMerger(binder, context);
/* 258 */     String viewType = viewDef.get("schViewType");
/*     */ 
/* 260 */     if (!viewType.equalsIgnoreCase("SharedObjectsTable"))
/*     */     {
/* 262 */       String msg = LocaleUtils.encodeMessage("apSchemaUnknownViewType", null, viewType);
/* 263 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 266 */     String tableName = viewDef.get("schTableName");
/* 267 */     ResultSet rset = SharedObjects.getTable(tableName);
/* 268 */     boolean isAbsentResultSet = rset == null;
/* 269 */     if (isAbsentResultSet)
/*     */     {
/* 271 */       Report.trace("schemaloader", "missing ResultSet " + tableName + " for view " + viewDef.m_name, null);
/*     */ 
/* 274 */       if (SharedObjects.getEnvValueAsBoolean("SchemaFailOnMissingResultSet", false))
/*     */       {
/* 276 */         throw new DataException(null, -27, "apSchemaMissingResultSet", new Object[] { tableName, viewDef.m_name });
/*     */       }
/*     */     }
/*     */ 
/* 280 */     String fields = viewDef.get("schViewColumns");
/* 281 */     List fieldList = StringUtils.makeListFromSequence(fields, ',', '^', 0);
/*     */ 
/* 283 */     String[] fieldArray = (String[])(String[])fieldList.toArray(new String[0]);
/* 284 */     if ((rset == null) || (rset instanceof SchemaResultSet))
/*     */     {
/* 290 */       boolean allFound = true;
/* 291 */       for (int i = 0; i < fieldArray.length; ++i)
/*     */       {
/* 293 */         FieldInfo info = new FieldInfo();
/* 294 */         if ((rset != null) && (rset.getFieldInfo(fieldArray[i], info)))
/*     */           continue;
/* 296 */         allFound = false;
/* 297 */         break;
/*     */       }
/*     */ 
/* 300 */       if (!allFound)
/*     */       {
/* 304 */         DataResultSet drset = new DataResultSet(fieldArray);
/* 305 */         while ((rset != null) && (rset.isRowPresent()))
/*     */         {
/* 307 */           Vector row = new IdcVector();
/* 308 */           SchemaData data = ((SchemaResultSet)rset).getData();
/* 309 */           for (int i = 0; i < fieldArray.length; ++i)
/*     */           {
/* 311 */             String value = data.get(fieldArray[i]);
/* 312 */             if (value == null)
/*     */             {
/* 314 */               value = "";
/*     */             }
/* 316 */             row.add(value);
/*     */           }
/* 318 */           drset.addRow(row);
/* 319 */           rset.next();
/*     */         }
/* 321 */         rset = drset;
/* 322 */         rset.first();
/*     */       }
/*     */     }
/*     */ 
/* 326 */     if ((relationName != null) || (primaryKeyValue != null))
/*     */     {
/* 328 */       String[] criteriaColumns = null;
/* 329 */       boolean ignoreCase = false;
/* 330 */       String[] keyValues = null;
/* 331 */       if (relationName != null)
/*     */       {
/* 333 */         SchemaHelper helper = new SchemaHelper();
/* 334 */         relation = (SchemaRelationData)helper.requireSchemaData("SchemaRelationConfig", relationName);
/*     */ 
/* 338 */         String parentColumn = relation.get("schView1Column");
/* 339 */         String relationColumn = relation.get("schView2Column");
/* 340 */         criteriaColumns = new String[] { relationColumn };
/* 341 */         FieldInfo fi = new FieldInfo();
/* 342 */         parentValues.getFieldInfo(parentColumn, fi);
/* 343 */         keyValues = new String[] { parentValues.getStringValue(fi.m_index) };
/* 344 */         ignoreCase = true;
/*     */       }
/* 346 */       else if (primaryKeyValue != null)
/*     */       {
/* 348 */         String keyColumn = viewDef.get("schPrimaryKey");
/* 349 */         if (keyColumn == null)
/*     */         {
/* 351 */           keyColumn = viewDef.get("schInternalKey");
/*     */         }
/* 353 */         if (keyColumn == null)
/*     */         {
/* 355 */           keyColumn = viewDef.get("schInternalColumn");
/*     */         }
/* 357 */         criteriaColumns = new String[] { keyColumn };
/* 358 */         keyValues = new String[] { primaryKeyValue[0] };
/* 359 */         isPartial = true;
/*     */       }
/*     */ 
/* 362 */       FieldInfo[] keyInfos = ResultSetUtils.createInfoList(rset, criteriaColumns, true);
/*     */ 
/* 364 */       FieldInfo[] sourceInfos = ResultSetUtils.createInfoList(rset, fieldArray, true);
/*     */ 
/* 368 */       DataResultSet filteredSet = new DataResultSet();
/* 369 */       Vector tmpV = new IdcVector();
/* 370 */       for (int j = 0; j < sourceInfos.length; ++j)
/*     */       {
/* 372 */         tmpV.add(sourceInfos[j]);
/*     */       }
/* 374 */       filteredSet.mergeFieldsWithFlags(tmpV, 0);
/*     */ 
/* 376 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 378 */         boolean isOkay = true;
/* 379 */         for (int i = 0; i < keyInfos.length; ++i)
/*     */         {
/* 381 */           String value = rset.getStringValue(keyInfos[i].m_index);
/* 382 */           if ((ignoreCase) && (value.equalsIgnoreCase(keyValues[i]))) continue; if (value.equals(keyValues[i]))
/*     */           {
/*     */             continue;
/*     */           }
/*     */ 
/* 389 */           isOkay = false;
/* 390 */           break;
/*     */         }
/*     */ 
/* 393 */         if (!isOkay)
/*     */           continue;
/* 395 */         Vector row = new IdcVector();
/* 396 */         for (int j = 0; j < sourceInfos.length; ++j)
/*     */         {
/* 398 */           String value = rset.getStringValue(sourceInfos[j].m_index);
/* 399 */           row.addElement(value);
/*     */         }
/* 401 */         filteredSet.addRow(row);
/*     */       }
/*     */ 
/* 404 */       rset = filteredSet;
/*     */     }
/*     */ 
/* 410 */     if (!isAbsentResultSet)
/*     */     {
/* 412 */       rset = this.m_schemaUtils.doServerProcessing(viewDef, rset, args, SchemaUtils.F_APPLY_CRITERIA);
/*     */     }
/*     */ 
/* 416 */     long timestamp = System.currentTimeMillis();
/* 417 */     callback.updateCache(item, viewDef, timestamp, relation, parentValues, null, rset, merger, context, false, isPartial, args);
/*     */ 
/* 420 */     merger.releaseAllTemporary();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 425 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84765 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SharedObjectsTableLoader
 * JD-Core Version:    0.5.4
 */