/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.PluginFilterData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PluginFilterLoader
/*     */ {
/*     */   public static Vector cacheFilters(DataBinder binder, String tableName)
/*     */     throws DataException, ServiceException
/*     */   {
/*  36 */     Vector filterList = new IdcVector();
/*  37 */     DataResultSet rset = (DataResultSet)binder.getResultSet(tableName);
/*  38 */     if (rset == null)
/*     */     {
/*  40 */       return filterList;
/*     */     }
/*     */ 
/*  44 */     String[] fields = { "type", "location", "parameter", "loadOrder" };
/*  45 */     FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, false);
/*  46 */     if (info[0].m_index < 0)
/*     */     {
/*  48 */       throw new DataException("!csFiltersTableFieldTypeMissing");
/*     */     }
/*  50 */     if (info[1].m_index < 0)
/*     */     {
/*  52 */       throw new DataException("!csFilterLocationMissing");
/*     */     }
/*     */ 
/*  55 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  57 */       PluginFilterData data = new PluginFilterData();
/*  58 */       data.m_filterType = rset.getStringValue(info[0].m_index);
/*  59 */       data.m_location = rset.getStringValue(info[1].m_index);
/*  60 */       if (info[2].m_index >= 0)
/*     */       {
/*  62 */         data.m_parameter = rset.getStringValue(info[2].m_index);
/*     */       }
/*  64 */       if (info[3].m_index >= 0)
/*     */       {
/*  66 */         String order = rset.getStringValue(info[3].m_index);
/*  67 */         data.m_order = parseOrder(order);
/*     */       }
/*     */ 
/*  70 */       filterList.addElement(data);
/*     */     }
/*     */ 
/*  73 */     return filterList;
/*     */   }
/*     */ 
/*     */   public static int parseOrder(String order) throws DataException
/*     */   {
/*  78 */     int iOrder = 0;
/*     */     try
/*     */     {
/*  81 */       iOrder = Integer.parseInt(order);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*  85 */       String msg = LocaleUtils.encodeMessage("csUnableToParseInteger", null, "order");
/*     */ 
/*  87 */       throw new DataException(msg);
/*     */     }
/*  89 */     return iOrder;
/*     */   }
/*     */ 
/*     */   public static void addToFilterMap(Vector filterList, Hashtable filters)
/*     */   {
/*  95 */     int nFilters = filterList.size();
/*  96 */     if (nFilters == 0)
/*     */     {
/*  98 */       return;
/*     */     }
/*     */ 
/* 101 */     PluginFilterData[] filterData = sortFilters(filterList);
/*     */ 
/* 103 */     for (int i = 0; i < nFilters; ++i)
/*     */     {
/* 105 */       PluginFilterData fdata = filterData[i];
/* 106 */       Vector classes = (Vector)filters.get(fdata.m_filterType);
/* 107 */       if (classes == null)
/*     */       {
/* 109 */         classes = new IdcVector();
/* 110 */         filters.put(fdata.m_filterType, classes);
/*     */       }
/* 112 */       classes.addElement(fdata);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static PluginFilterData[] sortFilters(Vector filterList)
/*     */   {
/* 118 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 122 */         PluginFilterData f1 = (PluginFilterData)obj1;
/* 123 */         PluginFilterData f2 = (PluginFilterData)obj2;
/* 124 */         if (f1.m_order > f2.m_order)
/*     */         {
/* 126 */           return 1;
/*     */         }
/* 128 */         if (f1.m_order < f2.m_order)
/*     */         {
/* 130 */           return -1;
/*     */         }
/* 132 */         return 0;
/*     */       }
/*     */     };
/* 136 */     int nFilters = filterList.size();
/* 137 */     PluginFilterData[] filterData = new PluginFilterData[nFilters];
/* 138 */     for (int i = 0; i < nFilters; ++i)
/*     */     {
/* 140 */       filterData[i] = ((PluginFilterData)filterList.elementAt(i));
/*     */     }
/*     */ 
/* 143 */     Sort.sort(filterData, 0, nFilters - 1, cmp);
/* 144 */     return filterData;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 149 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.PluginFilterLoader
 * JD-Core Version:    0.5.4
 */