/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.schema.SchemaCacheItem;
/*     */ import intradoc.shared.schema.SchemaLoader;
/*     */ import intradoc.shared.schema.SchemaLoaderUtils;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaTableConfig;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.shared.schema.ViewCacheCallback;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SelfReferentialLoader extends ExecutionContextAdaptor
/*     */   implements SchemaLoader
/*     */ {
/*     */   protected SchemaUtils m_schemaUtils;
/*     */   protected SchemaViewConfig m_views;
/*     */   protected SchemaTableConfig m_tables;
/*     */   protected SchemaRelationConfig m_relations;
/*     */   protected Map m_capabilities;
/*     */   protected Map m_lastGlobalViewTimestamps;
/*  43 */   public static String RELATION_TYPE = "self-referential prefix";
/*     */ 
/*     */   public SelfReferentialLoader()
/*     */   {
/*  41 */     this.m_lastGlobalViewTimestamps = new Hashtable();
/*     */   }
/*     */ 
/*     */   public boolean init(Map initData)
/*     */     throws ServiceException
/*     */   {
/*  48 */     this.m_schemaUtils = ((SchemaUtils)initData.get("SchemaUtils"));
/*  49 */     if (this.m_schemaUtils == null)
/*     */     {
/*  51 */       this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", "!csSchemaUnableToLoadUtils"));
/*     */ 
/*  55 */       this.m_schemaUtils.init(initData);
/*     */     }
/*     */ 
/*  58 */     this.m_views = ((SchemaViewConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaViewConfig"));
/*     */ 
/*  60 */     this.m_tables = ((SchemaTableConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaTableConfig"));
/*     */ 
/*  62 */     this.m_relations = ((SchemaRelationConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaRelationConfig"));
/*     */ 
/*  65 */     this.m_capabilities = new Hashtable();
/*  66 */     SchemaLoaderUtils.addStandardCapabilities(this.m_capabilities, 1);
/*     */ 
/*  69 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean isSelfReferentialView(SchemaViewData view)
/*     */   {
/*  74 */     SchemaRelationConfig relations = (SchemaRelationConfig)this.m_relations.shallowClone();
/*     */ 
/*  76 */     for (relations.first(); relations.isRowPresent(); relations.next())
/*     */     {
/*  78 */       SchemaRelationData data = (SchemaRelationData)relations.getData();
/*  79 */       String type = data.get("schRelationType");
/*  80 */       String viewName = data.get("schViewName");
/*  81 */       if ((type != null) && (type.equalsIgnoreCase(RELATION_TYPE)) && (viewName != null) && (viewName.equalsIgnoreCase(view.m_name)))
/*     */       {
/*  84 */         return true;
/*     */       }
/*     */     }
/*  87 */     return false;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaViewData view, Map extraData)
/*     */   {
/*  92 */     if ((extraData != null) && (extraData.get("SelfReferentialLoader") != null))
/*     */     {
/*  94 */       return new Hashtable();
/*     */     }
/*  96 */     if (isSelfReferentialView(view))
/*     */     {
/*  98 */       return this.m_capabilities;
/*     */     }
/* 100 */     return new Hashtable();
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaRelationData relationship, Map extraData)
/*     */   {
/* 105 */     if ((extraData != null) && (extraData.get("SelfReferentialLoader") != null))
/*     */     {
/* 107 */       return new Hashtable();
/*     */     }
/* 109 */     String type = relationship.get("schRelationType");
/* 110 */     if ((type != null) && (type.equalsIgnoreCase(RELATION_TYPE)))
/*     */     {
/* 112 */       return this.m_capabilities;
/*     */     }
/* 114 */     return new Hashtable();
/*     */   }
/*     */ 
/*     */   public boolean isDirty(SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 126 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(item.getViewName());
/* 127 */     SchemaLoader baseLoader = findBaseLoader(view);
/* 128 */     SchemaCacheItem baseItem = new SchemaCacheItem();
/* 129 */     baseItem.initShallow(item);
/* 130 */     baseItem.setCacheInfo(view);
/* 131 */     return baseLoader.isDirty(baseItem, args);
/*     */   }
/*     */ 
/*     */   public boolean needsMoreData(SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 137 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(item.getViewName());
/* 138 */     SchemaLoader baseLoader = findBaseLoader(view);
/* 139 */     SchemaCacheItem baseItem = new SchemaCacheItem();
/* 140 */     baseItem.initShallow(item);
/* 141 */     baseItem.setCacheInfo(view);
/* 142 */     return baseLoader.needsMoreData(baseItem, args);
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getParentViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 148 */     String type = relationship.get("schRelationType");
/* 149 */     if ((type == null) || (!type.equals(RELATION_TYPE)))
/*     */     {
/* 151 */       return null;
/*     */     }
/* 153 */     String viewName = relationship.getRequired("schViewName");
/* 154 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(viewName);
/* 155 */     if (view == null)
/*     */     {
/* 157 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_view", null, viewName);
/*     */ 
/* 159 */       throw new DataException(msg);
/*     */     }
/* 161 */     return new SchemaViewData[] { view };
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getChildViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 167 */     return getParentViews(relationship);
/*     */   }
/*     */ 
/*     */   public SchemaRelationData[] getParentRelations(SchemaViewData childView)
/*     */     throws DataException
/*     */   {
/* 173 */     SchemaRelationConfig relations = (SchemaRelationConfig)this.m_relations.shallowClone();
/*     */ 
/* 175 */     Vector list = new IdcVector();
/* 176 */     for (relations.first(); relations.isRowPresent(); relations.next())
/*     */     {
/* 178 */       SchemaRelationData relationship = (SchemaRelationData)relations.getData();
/*     */ 
/* 180 */       String relationshipType = relationship.get("schRelationType");
/* 181 */       String viewName = relationship.get("schViewName");
/* 182 */       if ((relationshipType == null) || (!relationshipType.equals(RELATION_TYPE)) || (viewName == null) || (!viewName.equalsIgnoreCase(childView.m_name)))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 187 */       list.addElement(relationship);
/*     */     }
/*     */ 
/* 190 */     SchemaRelationData[] array = new SchemaRelationData[list.size()];
/* 191 */     list.copyInto(array);
/* 192 */     return array;
/*     */   }
/*     */ 
/*     */   public String[] constructParentFieldsArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, Map args)
/*     */     throws DataException
/*     */   {
/* 199 */     String prefixField = relation.getRequired("schPrefixField");
/* 200 */     return new String[] { prefixField };
/*     */   }
/*     */ 
/*     */   public String[] constructParentValuesArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, ResultSet resultSet, Map args)
/*     */     throws DataException
/*     */   {
/* 207 */     String[] fields = constructParentFieldsArray(parentView, relation, childView, args);
/*     */ 
/* 209 */     String[] values = new String[fields.length];
/* 210 */     if (resultSet == null)
/*     */     {
/* 212 */       for (int i = 0; i < values.length; ++i)
/*     */       {
/* 214 */         values[i] = "";
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 219 */       FieldInfo info = new FieldInfo();
/* 220 */       for (int i = 0; i < values.length; ++i)
/*     */       {
/* 222 */         resultSet.getFieldInfo(fields[i], info);
/* 223 */         values[i] = resultSet.getStringValue(info.m_index);
/*     */       }
/*     */     }
/* 226 */     return values;
/*     */   }
/*     */ 
/*     */   protected SchemaLoader findBaseLoader(SchemaViewData viewDef)
/*     */     throws DataException
/*     */   {
/* 232 */     Hashtable extraData = new Hashtable();
/* 233 */     extraData.put("SelfReferentialLoader", this);
/* 234 */     SchemaLoader baseLoader = this.m_views.findLoader(viewDef, null, extraData);
/* 235 */     if (baseLoader == null)
/*     */     {
/*     */       String msg;
/*     */       String msg;
/* 238 */       if (viewDef == null)
/*     */       {
/* 240 */         msg = LocaleUtils.encodeMessage("apSchemaNoLoader", null);
/*     */       }
/*     */       else
/*     */       {
/* 245 */         msg = LocaleUtils.encodeMessage("apSchemaNoLoader", null, viewDef.m_name);
/*     */       }
/*     */ 
/* 248 */       throw new DataException(msg);
/*     */     }
/* 250 */     return baseLoader;
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData viewDef, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 256 */     String relationName = (String)args.get("relationName");
/* 257 */     if (relationName == null)
/*     */     {
/* 259 */       Report.trace("schemaloader", "SelfReferentialLoader loading all values for view '" + viewDef.m_name + "'", null);
/*     */ 
/* 262 */       SchemaLoader baseLoader = findBaseLoader(viewDef);
/* 263 */       baseLoader.loadValues(viewDef, callback, item, args);
/*     */     }
/*     */ 
/* 266 */     ResultSet parentValues = (ResultSet)args.get("parentValues");
/* 267 */     SchemaRelationData relation = (SchemaRelationData)this.m_relations.getData(relationName);
/*     */ 
/* 269 */     ResultSet rset = viewDef.getAllViewValues();
/* 270 */     String[] fields = new String[rset.getNumFields()];
/* 271 */     for (int i = 0; i < fields.length; ++i)
/*     */     {
/* 273 */       fields[i] = rset.getFieldName(i);
/*     */     }
/* 275 */     String prefixFieldName = relation.getRequired("schPrefixField");
/* 276 */     String prefixMinimumLengthString = relation.get("schPrefixMinimumLength");
/* 277 */     int prefixMinimumLength = NumberUtils.parseInteger(prefixMinimumLengthString, 0);
/*     */ 
/* 279 */     FieldInfo prefixFieldInfo = new FieldInfo();
/* 280 */     if (!rset.getFieldInfo(prefixFieldName, prefixFieldInfo))
/*     */     {
/* 282 */       String msg = LocaleUtils.encodeMessage("csSchPrefixFieldMissing", null, prefixFieldName, viewDef.m_name);
/*     */ 
/* 285 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 288 */     DataResultSet drset = new DataResultSet(fields);
/* 289 */     parentValues.first();
/* 290 */     String parentPrefix = parentValues.getStringValue(0);
/*     */ 
/* 292 */     boolean isCaseInsensitive = relation.getBoolean("schPrefixCaseInsensitive", false);
/*     */ 
/* 294 */     if (isCaseInsensitive)
/*     */     {
/* 296 */       parentPrefix = parentPrefix.toLowerCase();
/*     */     }
/* 298 */     Vector rows = new IdcVector();
/* 299 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 301 */       String fieldValue = rset.getStringValue(prefixFieldInfo.m_index);
/* 302 */       String compFieldValue = fieldValue;
/* 303 */       if (isCaseInsensitive)
/*     */       {
/* 305 */         compFieldValue = fieldValue.toLowerCase();
/*     */       }
/* 307 */       if (compFieldValue.equals(parentPrefix)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 311 */       if (!compFieldValue.startsWith(parentPrefix))
/*     */         continue;
/* 313 */       Vector row = new IdcVector();
/* 314 */       for (int i = 0; i < fields.length; ++i)
/*     */       {
/* 316 */         row.addElement(rset.getStringValue(i));
/*     */       }
/* 318 */       rows.addElement(row);
/*     */     }
/*     */ 
/* 322 */     String prefixCandidate = null;
/* 323 */     int prefixCandidateLength = 0;
/* 324 */     Vector prefixCandidateRow = null;
/* 325 */     prefixMinimumLength += parentPrefix.length();
/* 326 */     int size = rows.size();
/* 327 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 329 */       Vector row = (Vector)rows.elementAt(i);
/* 330 */       String fieldValue = (String)row.elementAt(prefixFieldInfo.m_index);
/* 331 */       String compFieldValue = fieldValue;
/* 332 */       if (isCaseInsensitive)
/*     */       {
/* 334 */         compFieldValue = compFieldValue.toLowerCase();
/*     */       }
/* 336 */       if (prefixCandidate == null)
/*     */       {
/* 338 */         prefixCandidate = compFieldValue;
/* 339 */         prefixCandidateLength = prefixCandidate.length();
/* 340 */         prefixCandidateRow = row;
/*     */       }
/*     */       else {
/* 343 */         if ((prefixMinimumLengthString == null) || (prefixMinimumLengthString.equals("full")))
/*     */         {
/* 345 */           prefixMinimumLength = prefixCandidateLength;
/*     */         }
/* 347 */         int diffIndex = 0;
/* 348 */         int compFieldValueLength = compFieldValue.length();
/* 349 */         while ((diffIndex < compFieldValueLength) && (diffIndex < prefixCandidateLength))
/*     */         {
/* 352 */           char c1 = compFieldValue.charAt(diffIndex);
/* 353 */           char c2 = prefixCandidate.charAt(diffIndex);
/* 354 */           if (c1 != c2) {
/*     */             break;
/*     */           }
/*     */ 
/* 358 */           ++diffIndex;
/*     */         }
/* 360 */         if (diffIndex >= prefixMinimumLength)
/*     */         {
/* 362 */           if (prefixCandidateLength <= diffIndex)
/*     */             continue;
/* 364 */           prefixCandidate = prefixCandidate.substring(0, diffIndex);
/* 365 */           prefixCandidateLength = diffIndex;
/*     */         }
/*     */         else
/*     */         {
/* 372 */           prefixCandidateRow.setElementAt(prefixCandidate, prefixFieldInfo.m_index);
/*     */ 
/* 374 */           drset.addRow(prefixCandidateRow);
/* 375 */           prefixCandidate = compFieldValue;
/* 376 */           prefixCandidateLength = prefixCandidate.length();
/* 377 */           prefixCandidateRow = row;
/*     */         }
/*     */       }
/*     */     }
/* 381 */     if (prefixCandidateRow != null)
/*     */     {
/* 383 */       prefixCandidateRow.setElementAt(prefixCandidate, prefixFieldInfo.m_index);
/*     */ 
/* 385 */       drset.addRow(prefixCandidateRow);
/*     */     }
/*     */ 
/* 393 */     long timestamp = System.currentTimeMillis();
/* 394 */     ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/*     */ 
/* 396 */     DataBinder binder = new DataBinder();
/* 397 */     PageMerger merger = new PageMerger(binder, context);
/* 398 */     callback.updateCache(item, viewDef, timestamp, relation, parentValues, null, drset, merger, context, false, false, args);
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData viewDef, String keyType, String[] keyValues, ViewCacheCallback callback)
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 416 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84237 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SelfReferentialLoader
 * JD-Core Version:    0.5.4
 */