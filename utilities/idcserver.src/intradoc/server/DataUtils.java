/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.MutableResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.utils.DocumentInfoCacheUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.CollectionUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataUtils
/*     */ {
/*     */   public static final int RAW_DATASOURCE = 0;
/*     */   public static final int USE_MAX_ROWS = 1;
/*     */   public static final int PARSED_DATASOURCE = 2;
/*     */   public static final int DATASOURCE_WORKSPACE = 3;
/*     */ 
/*     */   public static String[][] lookupSQL(String dataSource)
/*     */     throws DataException, ServiceException
/*     */   {
/*  58 */     String errMsg = null;
/*  59 */     String[][] sql = (String[][])null;
/*  60 */     if (dataSource == null)
/*     */     {
/*  62 */       errMsg = "!csUndefinedDataSource";
/*     */     }
/*     */     else
/*     */     {
/*  66 */       DataResultSet rset = SharedObjects.getTable("DataSources");
/*  67 */       if (rset == null)
/*     */       {
/*  69 */         errMsg = LocaleUtils.encodeMessage("csResourceTableUndefined", null, "DataSources");
/*     */       }
/*     */       else
/*     */       {
/*  74 */         String[] keys = { "name", "dataSource", "useMaxRows", "workspace" };
/*  75 */         sql = ResultSetUtils.createFilteredStringTable(rset, keys, dataSource);
/*  76 */         if ((sql == null) || (sql.length == 0))
/*     */         {
/*  78 */           errMsg = LocaleUtils.encodeMessage("csDataSourceNotDefined", null, dataSource);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/*  83 */     if (errMsg != null)
/*     */     {
/*  85 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/*  88 */     return sql;
/*     */   }
/*     */ 
/*     */   public static Object[] lookupPreParsedSQL(String dataSource) throws DataException, ServiceException
/*     */   {
/*  93 */     HashMap preParsedDataSources = (HashMap)SharedObjects.getObject("PreparsedDataSources", "DataSources");
/*  94 */     if (preParsedDataSources == null)
/*     */     {
/*  96 */       preParsedDataSources = new HashMap();
/*  97 */       SharedObjects.putObject("PreparsedDataSources", "DataSources", preParsedDataSources);
/*     */     }
/*  99 */     Object[] result = (Object[])(Object[])preParsedDataSources.get(dataSource);
/* 100 */     if (result == null)
/*     */     {
/* 102 */       String[][] sql = lookupSQL(dataSource);
/* 103 */       PageMerger pm = new PageMerger();
/*     */       try
/*     */       {
/* 106 */         DynamicHtml dhtml = pm.parseScriptInternal(sql[0][0]);
/* 107 */         result = new Object[4];
/* 108 */         result[0] = sql[0][0];
/* 109 */         result[1] = sql[0][1];
/* 110 */         result[2] = dhtml;
/* 111 */         result[3] = ((sql[0][2].length() > 0) ? sql[0][2] : "system");
/* 112 */         preParsedDataSources.put(dataSource, result);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*     */         String msg;
/* 118 */         throw new ServiceException(msg, e);
/*     */       }
/*     */       finally
/*     */       {
/* 122 */         pm.releaseAllTemporary();
/*     */       }
/*     */     }
/* 125 */     return result;
/*     */   }
/*     */ 
/*     */   public static String prepareQuery(String sql, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 132 */     PageMerger pageMerger = null;
/*     */     try
/*     */     {
/* 135 */       if (binder == null)
/*     */       {
/* 137 */         binder = new DataBinder(SharedObjects.getSafeEnvironment());
/*     */       }
/* 139 */       ExecutionContextAdaptor myContext = new ExecutionContextAdaptor();
/* 140 */       myContext.setParentContext(cxt);
/* 141 */       pageMerger = new PageMerger(binder, myContext);
/* 142 */       sql = pageMerger.evaluateScript(sql);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       String msg;
/* 152 */       if (pageMerger != null)
/*     */       {
/* 154 */         pageMerger.releaseAllTemporary();
/*     */       }
/*     */     }
/* 157 */     return sql;
/*     */   }
/*     */ 
/*     */   public static Vector parseOptionList(String textString)
/*     */   {
/* 162 */     return parseOptionListEx(textString, true);
/*     */   }
/*     */ 
/*     */   public static Vector parseOptionListEx(String textString, boolean makeNonEmpty)
/*     */   {
/* 168 */     Vector v = StringUtils.parseArray(textString, '\n', '\n');
/* 169 */     int lastNoneSpace = 0;
/* 170 */     int size = v.size();
/* 171 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 173 */       String str = (String)v.elementAt(i);
/* 174 */       str = str.trim();
/* 175 */       if (str.length() > 0)
/*     */       {
/* 177 */         lastNoneSpace = i + 1;
/*     */       }
/*     */       else
/*     */       {
/* 181 */         str = " ";
/*     */       }
/* 183 */       v.setElementAt(str, i);
/*     */     }
/* 185 */     if (lastNoneSpace <= size - 1)
/*     */     {
/* 187 */       v.setSize(lastNoneSpace);
/*     */     }
/* 189 */     size = v.size();
/* 190 */     if ((size == 0) && (makeNonEmpty))
/*     */     {
/* 192 */       v = new IdcVector();
/* 193 */       v.addElement(" ");
/*     */     }
/* 195 */     return v;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int getInteger(DataBinder binder, String name, int def)
/*     */   {
/* 202 */     return DataBinderUtils.getInteger(binder, name, def);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static int getLocalInteger(DataBinder binder, String name, int def)
/*     */   {
/* 209 */     return DataBinderUtils.getLocalInteger(binder, name, def);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean getBoolean(DataBinder binder, String name, boolean def)
/*     */   {
/* 223 */     return DataBinderUtils.getBoolean(binder, name, def);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean getLocalBoolean(DataBinder binder, String name, boolean def)
/*     */   {
/* 238 */     return DataBinderUtils.getLocalBoolean(binder, name, def);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Vector getList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 257 */     return DataBinderUtils.getList(binder, name, sep1, sep2);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Vector getLocalList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 276 */     return DataBinderUtils.getLocalList(binder, name, sep1, sep2);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static List getArrayList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 294 */     return DataBinderUtils.getArrayList(binder, name, sep1, sep2);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static List getLocalArrayList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 312 */     return DataBinderUtils.getLocalArrayList(binder, name, sep1, sep2);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void setOdbcDate(DataBinder binder, String name, long dateVal)
/*     */   {
/* 330 */     DataBinderUtils.setOdbcDate(binder, name, dateVal);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void mergeHashtables(Hashtable src, Hashtable dst, String[] list)
/*     */   {
/* 337 */     CollectionUtils.mergeMaps(src, dst, list);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Properties createMergedProperties(DataBinder binder)
/*     */   {
/* 355 */     return DataBinderUtils.createMergedProperties(binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Map createMergedMap(DataBinder binder)
/*     */   {
/* 372 */     return DataBinderUtils.createMergedMap(binder);
/*     */   }
/*     */ 
/*     */   public static void computeActionDates(DataBinder binder, long t)
/*     */   {
/* 393 */     if (t == 0L)
/*     */     {
/*     */       try
/*     */       {
/* 397 */         t = DocumentInfoCacheUtils.getSharedDocTimestamp();
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 401 */         Report.trace("system", null, e);
/* 402 */         t = System.currentTimeMillis();
/*     */       }
/*     */     }
/*     */ 
/* 406 */     String idStr = binder.getLocal("dID");
/* 407 */     long idPrefix = NumberUtils.parseLong(idStr, 0L);
/* 408 */     long millis = t % 60000L + idPrefix % 10000L * 60000L;
/* 409 */     long truncatedDate = t / 60000L * 60000L;
/* 410 */     binder.putLocal("dActionMillis", Long.toString(millis));
/* 411 */     binder.putLocalDate("dActionDate", new Date(truncatedDate));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static Map getInternalMap(Parameters params)
/*     */   {
/* 418 */     return DataBinderUtils.getInternalMap(params);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static DataBinder createBinderFromParameters(Parameters params, ExecutionContext cxt)
/*     */   {
/* 425 */     return DataBinderUtils.createBinderFromParameters(params, cxt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void addToBinder(Map map, DataBinder binder)
/*     */   {
/* 432 */     DataBinderUtils.addToBinder(map, binder);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void localizeResultSetFieldsIntoColumn(MutableResultSet rs, String keyName, String[] argNames, String targetName, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 464 */     DataBinderUtils.localizeResultSetFieldsIntoColumn(rs, keyName, argNames, targetName, cxt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void localizeResultSetFieldsIntoColumn(MutableResultSet rs, int keyField, int[] argFields, int targetField, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 490 */     DataBinderUtils.localizeResultSetFieldsIntoColumn(rs, keyField, argFields, targetField, cxt);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 496 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98186 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DataUtils
 * JD-Core Version:    0.5.4
 */