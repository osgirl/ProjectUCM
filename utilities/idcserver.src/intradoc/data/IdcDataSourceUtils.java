/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import java.util.Arrays;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class IdcDataSourceUtils
/*     */ {
/*  28 */   protected static Map<String, IdcDataSource> m_dataSouce = new ConcurrentHashMap();
/*  29 */   protected static Map<String, IdcDataSourceQuery> m_dataSourceQueries = new ConcurrentHashMap();
/*     */ 
/*     */   @Deprecated
/*     */   public static void registerOrRefresh(Workspace ws, String type, String[] tables, String[] selectList, String[] relations, String[] filters, String[] fieldMap, Map extraProps)
/*     */     throws DataException
/*     */   {
/*  38 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(type);
/*  39 */     if (data == null)
/*     */     {
/*  41 */       data = new IdcDataSource();
/*     */     }
/*     */ 
/*  44 */     if ((extraProps != null) && (tables.length > 1))
/*     */     {
/*  46 */       data.initQueries(ws, type, new String[] { tables[0] }, selectList, relations, filters, fieldMap);
/*  47 */       int typeFlag = 14;
/*  48 */       data.addDependentQueries(null, ws, (String[])Arrays.copyOfRange(tables, 1, tables.length), selectList, relations, filters, fieldMap, typeFlag);
/*  49 */       data.addDependentQueryConditions(extraProps);
/*     */     }
/*     */     else
/*     */     {
/*  53 */       data.initQueries(ws, type, tables, selectList, relations, filters, fieldMap);
/*     */     }
/*  55 */     m_dataSouce.put(type, data);
/*     */   }
/*     */ 
/*     */   public static void registerOrRefresh(Workspace ws, String type, String[] tables, String[] selectList, String[] relations, String[] filters, String[] fieldMap)
/*     */     throws DataException
/*     */   {
/*  62 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(type);
/*  63 */     if (data == null)
/*     */     {
/*  65 */       data = new IdcDataSource();
/*     */     }
/*  67 */     data.initQueries(ws, type, tables, selectList, relations, filters, fieldMap);
/*  68 */     m_dataSouce.put(type, data);
/*     */   }
/*     */ 
/*     */   public static void addDependentQueryConditions(String dataSourceName, Map extraProps)
/*     */   {
/*  73 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(dataSourceName);
/*  74 */     if (data == null)
/*     */       return;
/*  76 */     data.addDependentQueryConditions(extraProps);
/*     */   }
/*     */ 
/*     */   public static void addDependentQueries(String dataSourceName, String name, Workspace ws, String[] tableStrs, String[] selectList, String[] relationStrs, String[] filters, String[] fieldMapStrs, int typeFlag)
/*     */     throws DataException
/*     */   {
/*  83 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(dataSourceName);
/*  84 */     if (data == null)
/*     */       return;
/*  86 */     data.addDependentQueries(name, ws, tableStrs, selectList, relationStrs, filters, fieldMapStrs, typeFlag);
/*     */   }
/*     */ 
/*     */   public static IdcDataSourceQuery registerQueryWithDDName(Workspace ws, String queryName, String databaseDataName, int queryType)
/*     */     throws DataException
/*     */   {
/*  93 */     IdcDataSourceQuery query = null;
/*  94 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(databaseDataName);
/*  95 */     if (data != null)
/*     */     {
/*  97 */       query = new IdcDataSourceQuery();
/*  98 */       query.m_data = data;
/*  99 */       query.m_queryType = queryType;
/* 100 */       m_dataSourceQueries.put(queryName, query);
/* 101 */       ws.addQueryDataSourceMap(queryName, query);
/* 102 */       ws.removeQuery(queryName);
/*     */     }
/* 104 */     return query;
/*     */   }
/*     */ 
/*     */   public static long[] modData(Workspace ws, String name, int type, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 111 */     IdcDataSource data = (IdcDataSource)m_dataSouce.get(name);
/* 112 */     if (data == null)
/*     */     {
/* 114 */       throw new DataException(null, "csNamedDataSourceNotExists", new Object[] { name });
/*     */     }
/* 116 */     return data.modData(ws, type, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static long[] addData(Workspace ws, String name, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 122 */     return modData(ws, name, 2, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static long[] updateData(Workspace ws, String name, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 128 */     return modData(ws, name, 4, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static long[] deleteData(Workspace ws, String name, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 135 */     return modData(ws, name, 8, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static long execute(Workspace ws, String queryName, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 141 */     IdcDataSourceQuery query = (IdcDataSourceQuery)m_dataSourceQueries.get(queryName);
/* 142 */     if (query == null)
/*     */     {
/* 144 */       return ws.execute(queryName, binder);
/*     */     }
/* 146 */     return query.execute(ws, binder, cxt)[0];
/*     */   }
/*     */ 
/*     */   public static ResultSet createResultSet(Workspace ws, String queryName, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 152 */     IdcDataSourceQuery query = (IdcDataSourceQuery)m_dataSourceQueries.get(queryName);
/* 153 */     if (query == null)
/*     */     {
/* 155 */       return ws.createResultSet(queryName, binder);
/*     */     }
/* 157 */     if (query.m_queryType != 1)
/*     */     {
/* 160 */       throw new DataException(null, "csUnableToCreateResultSetWrongQueryType", new Object[] { queryName, Integer.valueOf(query.m_queryType) });
/*     */     }
/* 162 */     return query.createResultSet(ws, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 166 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96659 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcDataSourceUtils
 * JD-Core Version:    0.5.4
 */