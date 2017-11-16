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
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.schema.SchemaCacheItem;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaLoader;
/*     */ import intradoc.shared.schema.SchemaLoaderUtils;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaTableConfig;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.shared.schema.ViewCacheCallback;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.lang.reflect.Field;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JavaArrayLoader extends ExecutionContextAdaptor
/*     */   implements SchemaLoader
/*     */ {
/*     */   protected SchemaUtils m_schemaUtils;
/*     */   protected SchemaViewConfig m_views;
/*     */   protected SchemaTableConfig m_tables;
/*     */   protected SchemaRelationConfig m_relations;
/*     */   protected Map m_capabilities;
/*     */   protected Map m_lastGlobalViewTimestamps;
/*     */ 
/*     */   public JavaArrayLoader()
/*     */   {
/*  42 */     this.m_lastGlobalViewTimestamps = new Hashtable();
/*     */   }
/*     */ 
/*     */   public boolean init(Map initData) throws ServiceException
/*     */   {
/*  47 */     this.m_schemaUtils = ((SchemaUtils)initData.get("SchemaUtils"));
/*  48 */     if (this.m_schemaUtils == null)
/*     */     {
/*  50 */       this.m_schemaUtils = ((SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", "!csSchemaUnableToLoadUtils"));
/*     */ 
/*  54 */       this.m_schemaUtils.init(initData);
/*     */     }
/*     */ 
/*  57 */     this.m_views = ((SchemaViewConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaViewConfig"));
/*     */ 
/*  59 */     this.m_tables = ((SchemaTableConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaTableConfig"));
/*     */ 
/*  61 */     this.m_relations = ((SchemaRelationConfig)this.m_schemaUtils.getInitResultSet(initData, "SchemaRelationConfig"));
/*     */ 
/*  64 */     this.m_capabilities = new Hashtable();
/*  65 */     SchemaLoaderUtils.addStandardCapabilities(this.m_capabilities, 7);
/*     */ 
/*  70 */     return true;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaViewData view, Map extraData)
/*     */   {
/*  75 */     String viewType = view.get("schViewType");
/*  76 */     if ((viewType != null) && (viewType.equalsIgnoreCase("javaarray")))
/*     */     {
/*  78 */       return this.m_capabilities;
/*     */     }
/*  80 */     return new Hashtable();
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaRelationData relation, Map extraData)
/*     */   {
/*  85 */     Map capabilities = new HashMap();
/*  86 */     if (relation != null)
/*     */     {
/*  88 */       String relationType = relation.get("schRelationType");
/*  89 */       if ((relationType != null) && (relationType.equals("view")))
/*     */       {
/*  91 */         SchemaLoaderUtils.addStandardCapabilities(capabilities, 7);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  97 */     return capabilities;
/*     */   }
/*     */ 
/*     */   public boolean isDirty(SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 103 */     String viewName = item.getViewName();
/* 104 */     SchemaViewData viewDefinition = (SchemaViewData)this.m_views.getData(viewName);
/*     */ 
/* 106 */     if (viewDefinition == null)
/*     */     {
/* 108 */       Report.trace("schemaloader", "isDirty() view '" + viewName + "' doesn't exist.", null);
/*     */ 
/* 112 */       return false;
/*     */     }
/*     */ 
/* 115 */     boolean isDirty = item.isMarkedDirty();
/* 116 */     if (isDirty)
/*     */     {
/* 118 */       if (SystemUtils.m_verbose)
/*     */       {
/* 120 */         Report.debug("schemaloader", "item '" + item + "' is dirty because it is marked.", null);
/*     */       }
/*     */ 
/* 123 */       return true;
/*     */     }
/*     */ 
/* 126 */     isDirty = item.objectVersionsDirty(viewDefinition, null, null);
/*     */ 
/* 128 */     if (isDirty)
/*     */     {
/* 130 */       if (SystemUtils.m_verbose)
/*     */       {
/* 132 */         Report.debug("schemaloader", "item '" + item + "' dirty due to version mismatch.", null);
/*     */       }
/*     */ 
/* 135 */       return true;
/*     */     }
/*     */ 
/* 138 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean needsMoreData(SchemaCacheItem item, Map args)
/*     */   {
/* 143 */     return !item.isComplete();
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getParentViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 150 */     String type = relationship.getRequired("schRelationType");
/* 151 */     String viewName = relationship.getRequired("schView1Name");
/* 152 */     if (!type.equalsIgnoreCase("view"))
/*     */     {
/* 154 */       return new SchemaViewData[0];
/*     */     }
/* 156 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(viewName);
/* 157 */     if (view == null)
/*     */     {
/* 159 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_view", null, viewName);
/*     */ 
/* 161 */       throw new DataException(msg);
/*     */     }
/* 163 */     if (view.getRequired("schViewType").equalsIgnoreCase("javaarray"))
/*     */     {
/* 165 */       return new SchemaViewData[] { view };
/*     */     }
/* 167 */     return new SchemaViewData[0];
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getChildViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 173 */     String type = relationship.get("schRelationType");
/* 174 */     if ((type == null) || (!type.equals("view")))
/*     */     {
/* 176 */       return new SchemaViewData[0];
/*     */     }
/* 178 */     String viewName = relationship.getRequired("schView2Name");
/* 179 */     SchemaViewData view = (SchemaViewData)this.m_views.getData(viewName);
/* 180 */     if (view == null)
/*     */     {
/* 182 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_view", null, viewName);
/*     */ 
/* 184 */       throw new DataException(msg);
/*     */     }
/* 186 */     if (view.getRequired("schViewType").equalsIgnoreCase("javaarray"))
/*     */     {
/* 188 */       return new SchemaViewData[] { view };
/*     */     }
/* 190 */     return new SchemaViewData[0];
/*     */   }
/*     */ 
/*     */   public SchemaRelationData[] getParentRelations(SchemaViewData childView)
/*     */     throws DataException
/*     */   {
/* 197 */     return new SchemaRelationData[0];
/*     */   }
/*     */ 
/*     */   public String[] constructParentFieldsArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, Map args)
/*     */     throws DataException
/*     */   {
/* 204 */     if (parentView != null)
/*     */     {
/* 206 */       String viewType = parentView.getRequired("schViewType");
/* 207 */       if (!viewType.equalsIgnoreCase("javaarray"))
/*     */       {
/* 209 */         return null;
/*     */       }
/*     */     }
/* 212 */     String field = relation.get("schView1Column");
/* 213 */     return new String[] { field };
/*     */   }
/*     */ 
/*     */   public String[] constructParentValuesArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, ResultSet resultSet, Map args)
/*     */     throws DataException
/*     */   {
/* 221 */     SchemaHelper helper = new SchemaHelper();
/* 222 */     String[] fields = constructParentFieldsArray(parentView, relation, childView, args);
/*     */ 
/* 224 */     String[] values = helper.constructParentValuesArray(fields, resultSet);
/* 225 */     return values;
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData viewDef, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 231 */     String relationName = (String)args.get("relationName");
/* 232 */     ResultSet parentValues = (ResultSet)args.get("parentValues");
/* 233 */     DataBinder binder = new DataBinder();
/* 234 */     ExecutionContext context = this.m_schemaUtils.constructExecutionContext(args);
/* 235 */     PageMerger merger = new PageMerger(binder, context);
/* 236 */     String viewType = viewDef.get("schViewType");
/*     */ 
/* 238 */     if (viewType.equalsIgnoreCase("javaarray"))
/*     */     {
/* 240 */       String[][] theArray = (String[][])null;
/* 241 */       List theList = null;
/*     */       int length;
/*     */       try
/*     */       {
/* 245 */         String className = viewDef.getRequired("schClassName");
/* 246 */         String fieldName = viewDef.getRequired("schFieldName");
/* 247 */         Class cl = Class.forName(className);
/* 248 */         Field fl = cl.getField(fieldName);
/* 249 */         Object obj = fl.get(null);
/*     */         int length;
/* 250 */         if (obj instanceof List)
/*     */         {
/* 252 */           theList = (List)obj;
/* 253 */           length = theList.size();
/*     */         }
/*     */         else
/*     */         {
/* 257 */           theArray = (String[][])(String[][])obj;
/* 258 */           length = theArray.length;
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 265 */         String msg = LocaleUtils.encodeMessage("apSchemaUnableToLoadView", viewDef.m_name);
/*     */ 
/* 267 */         throw new DataException(msg, e);
/*     */       }
/*     */ 
/* 270 */       SchemaHelper helper = new SchemaHelper();
/*     */ 
/* 272 */       String columnListString = viewDef.getRequired("schViewColumns");
/* 273 */       Vector columnList = StringUtils.parseArray(columnListString, ',', '^');
/* 274 */       String[] columnListArray = new String[columnList.size()];
/* 275 */       columnList.copyInto(columnListArray);
/*     */ 
/* 277 */       String[] keyValues = null;
/* 278 */       int[] keyIndexes = null;
/* 279 */       SchemaRelationData relation = null;
/* 280 */       if (relationName != null)
/*     */       {
/* 282 */         relation = (SchemaRelationData)helper.requireSchemaData("SchemaRelationConfig", relationName);
/*     */ 
/* 285 */         String parentColumn = relation.get("schView1Column");
/* 286 */         String relationColumn = relation.get("schView2Column");
/* 287 */         String[] criteriaColumns = { relationColumn };
/* 288 */         FieldInfo fi = new FieldInfo();
/* 289 */         parentValues.getFieldInfo(parentColumn, fi);
/* 290 */         keyValues = new String[] { parentValues.getStringValue(fi.m_index) };
/* 291 */         keyIndexes = new int[criteriaColumns.length];
/*     */ 
/* 293 */         for (int i = 0; i < criteriaColumns.length; ++i)
/*     */         {
/* 295 */           String col = criteriaColumns[i];
/* 296 */           keyIndexes[i] = -1;
/* 297 */           for (int j = 0; j < columnListArray.length; ++j)
/*     */           {
/* 299 */             if (!col.equalsIgnoreCase(columnListArray[j]))
/*     */               continue;
/* 301 */             keyIndexes[i] = j;
/* 302 */             break;
/*     */           }
/*     */ 
/* 305 */           if (keyIndexes[i] != -1)
/*     */             continue;
/* 307 */           throw new DataException("!$AJK unable to find column matching criteria column " + col);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 313 */       DataResultSet drset = new DataResultSet(columnListArray);
/* 314 */       for (int i = 0; i < length; ++i)
/*     */       {
/* 316 */         String[] rowArray = (theList != null) ? (String[])theList.get(i) : theArray[i];
/* 317 */         if (rowArray.length != columnListArray.length)
/*     */         {
/* 319 */           String msg = LocaleUtils.encodeMessage("syMismatchedNumberOfColumns", null);
/*     */ 
/* 321 */           msg = LocaleUtils.encodeMessage("apSchemaUnableToLoadView", msg, viewDef.m_name);
/*     */ 
/* 323 */           throw new DataException(msg);
/*     */         }
/* 325 */         if (keyIndexes != null)
/*     */         {
/* 327 */           boolean okay = true;
/* 328 */           for (int k = 0; k < keyIndexes.length; ++k)
/*     */           {
/* 330 */             if (keyValues[k].equalsIgnoreCase(rowArray[keyIndexes[k]]))
/*     */               continue;
/* 332 */             okay = false;
/*     */           }
/*     */ 
/* 335 */           if (!okay) {
/*     */             continue;
/*     */           }
/*     */         }
/*     */ 
/* 340 */         Vector row = new IdcVector();
/* 341 */         for (int j = 0; j < rowArray.length; ++j)
/*     */         {
/* 343 */           row.addElement(rowArray[j]);
/*     */         }
/* 345 */         drset.addRow(row);
/*     */       }
/*     */ 
/* 348 */       ResultSet rset = drset;
/* 349 */       long timestamp = System.currentTimeMillis();
/* 350 */       rset = this.m_schemaUtils.doServerProcessing(viewDef, rset, args);
/*     */ 
/* 352 */       callback.updateCache(item, viewDef, timestamp, relation, parentValues, null, rset, merger, context, false, false, args);
/*     */     }
/*     */     else
/*     */     {
/* 357 */       String msg = LocaleUtils.encodeMessage("apSchemaUnknownViewType", null, viewType);
/*     */ 
/* 359 */       msg = LocaleUtils.encodeMessage("apSchemaUnableToLoadView", msg, viewDef.m_name);
/*     */ 
/* 361 */       throw new DataException(msg);
/*     */     }
/* 363 */     merger.releaseAllTemporary();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 370 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85489 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.JavaArrayLoader
 * JD-Core Version:    0.5.4
 */