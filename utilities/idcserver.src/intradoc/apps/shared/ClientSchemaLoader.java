/*     */ package intradoc.apps.shared;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
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
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaCacheItem;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaLoader;
/*     */ import intradoc.shared.schema.SchemaLoaderUtils;
/*     */ import intradoc.shared.schema.SchemaRelationConfig;
/*     */ import intradoc.shared.schema.SchemaRelationData;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.shared.schema.ViewCacheCallback;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ClientSchemaLoader
/*     */   implements SchemaLoader
/*     */ {
/*     */   protected Map m_initData;
/*     */   protected Map m_capabilities;
/*     */   protected DynamicHtmlMerger m_merger;
/*     */   protected ExecutionContext m_context;
/*     */   protected SchemaHelper m_helper;
/*     */ 
/*     */   public ClientSchemaLoader()
/*     */   {
/*  31 */     this.m_initData = null;
/*  32 */     this.m_capabilities = null;
/*     */ 
/*  34 */     this.m_merger = null;
/*  35 */     this.m_context = new ExecutionContextAdaptor();
/*     */   }
/*     */ 
/*     */   public boolean init(Map initData)
/*     */     throws ServiceException
/*     */   {
/*  42 */     this.m_initData = initData;
/*     */ 
/*  44 */     this.m_capabilities = new Hashtable();
/*  45 */     SchemaLoaderUtils.addStandardCapabilities(this.m_capabilities, 2);
/*     */ 
/*  47 */     this.m_helper = new SchemaHelper();
/*  48 */     return true;
/*     */   }
/*     */ 
/*     */   public Map getInitData()
/*     */   {
/*  53 */     return this.m_initData;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaViewData view, Map extraData)
/*     */   {
/*  58 */     return this.m_capabilities;
/*     */   }
/*     */ 
/*     */   public Map getLoaderCapabilities(SchemaRelationData relation, Map extraData)
/*     */   {
/*  63 */     return this.m_capabilities;
/*     */   }
/*     */ 
/*     */   public boolean isDirty(SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/*  69 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean needsMoreData(SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/*  75 */     return !item.isComplete();
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getParentViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/*  81 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/*  83 */     DataBinder binder = new DataBinder();
/*  84 */     binder.putLocal("schRelationName", relationship.m_name);
/*  85 */     binder.putLocal("IdcService", "GET_SCHEMA_VIEW_FRAGMENT");
/*  86 */     binder.putLocal("GetParentViews", "1");
/*  87 */     executeService(binder);
/*  88 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ParentViews");
/*     */ 
/*  90 */     SchemaViewData[] data = new SchemaViewData[drset.getNumRows()];
/*  91 */     drset.first();
/*  92 */     for (int i = 0; drset.isRowPresent(); drset.next())
/*     */     {
/*  94 */       String name = drset.getStringValue(0);
/*  95 */       if ((data[(i++)] =  = (SchemaViewData)views.getData(name)) != null)
/*     */         continue;
/*  97 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, name);
/*     */ 
/*  99 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 102 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaRelationData[] getParentRelations(SchemaViewData childView)
/*     */     throws DataException
/*     */   {
/* 108 */     SchemaRelationConfig relations = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*     */ 
/* 110 */     DataBinder binder = new DataBinder();
/* 111 */     binder.putLocal("schViewName", childView.m_name);
/* 112 */     binder.putLocal("IdcService", "GET_SCHEMA_VIEW_FRAGMENT");
/* 113 */     binder.putLocal("GetParentRelations", "1");
/* 114 */     executeService(binder);
/* 115 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ParentRelations");
/*     */ 
/* 117 */     SchemaRelationData[] data = new SchemaRelationData[drset.getNumRows()];
/* 118 */     drset.first();
/* 119 */     for (int i = 0; drset.isRowPresent(); drset.next())
/*     */     {
/* 121 */       String name = drset.getStringValue(0);
/* 122 */       if ((data[(i++)] =  = (SchemaRelationData)relations.getData(name)) != null)
/*     */         continue;
/* 124 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_relation", null, name);
/*     */ 
/* 126 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 129 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaViewData[] getChildViews(SchemaRelationData relationship)
/*     */     throws DataException
/*     */   {
/* 135 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/* 137 */     DataBinder binder = new DataBinder();
/* 138 */     binder.putLocal("schRelationName", relationship.m_name);
/* 139 */     binder.putLocal("IdcService", "GET_SCHEMA_VIEW_FRAGMENT");
/* 140 */     binder.putLocal("GetChildViews", "1");
/* 141 */     executeService(binder);
/* 142 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ChildViews");
/*     */ 
/* 144 */     SchemaViewData[] data = new SchemaViewData[drset.getNumRows()];
/* 145 */     drset.first();
/* 146 */     for (int i = 0; drset.isRowPresent(); drset.next())
/*     */     {
/* 148 */       String name = drset.getStringValue(0);
/* 149 */       if ((data[(i++)] =  = (SchemaViewData)views.getData(name)) != null)
/*     */         continue;
/* 151 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectDoesntExist_view", null, name);
/*     */ 
/* 153 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 156 */     return data;
/*     */   }
/*     */ 
/*     */   public String[] constructParentFieldsArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, Map args)
/*     */     throws DataException
/*     */   {
/* 173 */     String parentFieldsString = null;
/* 174 */     if (parentView != null)
/*     */     {
/* 176 */       parentFieldsString = relation.get("ParentFieldsList_" + parentView.m_name);
/*     */     }
/*     */ 
/* 179 */     if (parentFieldsString == null)
/*     */     {
/* 181 */       parentFieldsString = relation.get("ParentFieldsList");
/*     */     }
/* 183 */     if (parentFieldsString == null)
/*     */     {
/* 185 */       parentFieldsString = relation.get("schTable1Column");
/*     */     }
/* 187 */     if (parentFieldsString == null)
/*     */     {
/* 190 */       Report.trace("schemaloader", "unable to compute parentFieldsArray for the relationship " + relation.m_name, null);
/*     */ 
/* 193 */       return null;
/*     */     }
/* 195 */     List list = StringUtils.makeListFromSequence(parentFieldsString, ',', '^', 0);
/*     */ 
/* 197 */     String[] array = null;
/* 198 */     array = (String[])(String[])list.toArray(new String[0]);
/* 199 */     return array;
/*     */   }
/*     */ 
/*     */   public String[] constructParentValuesArray(SchemaViewData parentView, SchemaRelationData relation, SchemaViewData childView, ResultSet resultSet, Map args)
/*     */     throws DataException
/*     */   {
/* 211 */     String[] fields = constructParentFieldsArray(parentView, relation, childView, args);
/*     */ 
/* 213 */     String[] values = this.m_helper.constructParentValuesArray(fields, resultSet);
/* 214 */     return values;
/*     */   }
/*     */ 
/*     */   public void loadValues(SchemaViewData view, ViewCacheCallback callback, SchemaCacheItem item, Map args)
/*     */     throws DataException
/*     */   {
/* 220 */     String relationName = (String)args.get("relationName");
/* 221 */     ResultSet parentValues = (ResultSet)args.get("parentValues");
/* 222 */     String[] primaryKeyValues = (String[])(String[])args.get("primaryKey");
/* 223 */     SchemaRelationData relation = null;
/* 224 */     if (relationName != null)
/*     */     {
/* 226 */       relation = (SchemaRelationData)this.m_helper.requireSchemaData("SchemaRelationConfig", relationName);
/*     */     }
/*     */ 
/* 230 */     DataBinder binder = new DataBinder();
/* 231 */     binder.putLocal("IdcService", "GET_SCHEMA_VIEW_FRAGMENT");
/* 232 */     binder.putLocal("schViewName", view.m_name);
/* 233 */     if (primaryKeyValues != null)
/*     */     {
/* 235 */       for (int i = 0; i < primaryKeyValues.length; ++i)
/*     */       {
/* 237 */         binder.putLocal("schPrimaryKeyValue" + i, primaryKeyValues[i]);
/*     */       }
/*     */     }
/* 240 */     if (relationName != null)
/*     */     {
/* 242 */       binder.putLocal("schRelationName", relationName);
/* 243 */       int i = 0;
/* 244 */       for (parentValues.first(); parentValues.isRowPresent(); parentValues.next())
/*     */       {
/* 246 */         binder.putLocal("schParentValue" + i, parentValues.getStringValue(0));
/* 247 */         ++i;
/*     */       }
/* 249 */       parentValues.first();
/*     */     }
/* 251 */     executeService(binder);
/*     */ 
/* 253 */     String rsetName = binder.getLocal("PublishedTableName");
/* 254 */     DataResultSet rset = (DataResultSet)binder.getResultSet(rsetName);
/* 255 */     long timestamp = System.currentTimeMillis();
/*     */ 
/* 258 */     ExecutionContext context = this.m_helper.constructExecutionContext(args);
/* 259 */     boolean isIncremental = false;
/* 260 */     boolean isPartial = StringUtils.convertToBool(binder.getLocal("IsPartialSet"), false);
/* 261 */     context.setCachedObject("DataBinder", binder);
/* 262 */     callback.updateCache(item, view, timestamp, relation, parentValues, null, rset, null, context, isIncremental, isPartial, args);
/*     */   }
/*     */ 
/*     */   protected void executeService(DataBinder binder)
/*     */     throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 272 */       AppLauncher.executeService(binder.get("IdcService"), binder);
/* 273 */       String code = binder.getLocal("StatusCode");
/* 274 */       if ((code != null) && (!code.equals("0")))
/*     */       {
/* 276 */         throw new DataException(binder.getLocal("StatusMessage"));
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 282 */       DataException de = new DataException(e.getMessage());
/* 283 */       SystemUtils.setExceptionCause(de, e);
/* 284 */       throw de;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 290 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84765 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.shared.ClientSchemaLoader
 * JD-Core Version:    0.5.4
 */