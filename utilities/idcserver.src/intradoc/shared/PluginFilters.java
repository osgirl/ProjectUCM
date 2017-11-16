/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class PluginFilters
/*     */ {
/*  32 */   protected static Map<String, ArrayList> m_filters = new ConcurrentHashMap();
/*  33 */   protected static Map<String, ArrayList> m_filterTypeActions = new ConcurrentHashMap();
/*     */ 
/*     */   public static void registerFilters(Vector filterList)
/*     */   {
/*  38 */     int nFilters = filterList.size();
/*  39 */     for (int i = 0; i < nFilters; ++i)
/*     */     {
/*  41 */       PluginFilterData data = (PluginFilterData)filterList.elementAt(i);
/*     */ 
/*  43 */       ArrayList implList = (ArrayList)m_filters.get(data.m_filterType);
/*  44 */       if (implList == null)
/*     */       {
/*  46 */         implList = new ArrayList();
/*  47 */         m_filters.put(data.m_filterType, implList);
/*  48 */         m_filterTypeActions.put(data.m_filterType, new ArrayList());
/*     */       }
/*     */       else
/*     */       {
/*  53 */         ArrayList actions = (ArrayList)m_filterTypeActions.get(data.m_filterType);
/*  54 */         for (String action : actions)
/*     */         {
/*  56 */           m_filters.remove(data.m_filterType + "." + action);
/*     */         }
/*  58 */         actions.clear();
/*     */       }
/*  60 */       implList.add(data);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int filter(String type, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  68 */     return filterWithAction(type, null, ws, binder, cxt);
/*     */   }
/*     */ 
/*     */   public static int filterWithAction(String type, String action, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  75 */     ArrayList filters = getFilters(type, action);
/*     */ 
/*  77 */     int size = 0;
/*  78 */     if (filters != null)
/*     */     {
/*  80 */       size = filters.size();
/*     */     }
/*     */ 
/*  83 */     boolean reportedCalledSomething = false;
/*  84 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  86 */       PluginFilterData fData = (PluginFilterData)filters.get(i);
/*  87 */       String name = fData.m_location;
/*  88 */       String msg = LocaleUtils.encodeMessage("apFilterInstantiationError", null, name, type);
/*     */ 
/*  90 */       FilterImplementor filter = (FilterImplementor)ComponentClassFactory.createClassInstance(name, name, msg);
/*     */ 
/*  92 */       if (cxt != null)
/*     */       {
/*  94 */         String parameter = fData.m_parameter;
/*  95 */         if ((parameter == null) || (parameter.equalsIgnoreCase("null")))
/*     */         {
/*  97 */           parameter = "";
/*     */         }
/*  99 */         cxt.setCachedObject("filterParameter", parameter);
/*     */       }
/*     */ 
/* 103 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("services")) && (isReportableFilterEvent(type)))
/*     */       {
/* 105 */         String param = fData.m_parameter;
/* 106 */         if ((param == null) || (param.length() == 0))
/*     */         {
/* 108 */           param = "<none>";
/*     */         }
/* 110 */         Report.trace("services", "Calling filter event " + type + " on class " + name + " with parameter " + param, null);
/*     */ 
/* 112 */         reportedCalledSomething = true;
/*     */       }
/* 114 */       int result = filter.doFilter(ws, binder, cxt);
/* 115 */       if (result == 0)
/*     */         continue;
/* 117 */       if ((SystemUtils.m_verbose) && (isReportableFilterEvent(type)))
/*     */       {
/* 119 */         Report.trace("services", "Returning early from filter event " + type + " with result " + result, null);
/*     */       }
/*     */ 
/* 122 */       return result;
/*     */     }
/*     */ 
/* 126 */     if ((!reportedCalledSomething) && (SystemUtils.m_verbose) && (isReportableFilterEvent(type)))
/*     */     {
/* 128 */       Report.trace("services", "Called filter event " + type + " with no filter plugins registered", null);
/*     */     }
/*     */ 
/* 131 */     return 0;
/*     */   }
/*     */ 
/*     */   public static boolean isReportableFilterEvent(String type)
/*     */   {
/* 136 */     return (!type.equals("computeFunction")) && (!type.equals("getnextrow")) && (!type.equals("notifynextrow"));
/*     */   }
/*     */ 
/*     */   public static ArrayList getFilters(String type, String action)
/*     */     throws ServiceException, DataException
/*     */   {
/* 142 */     ArrayList filters = (ArrayList)m_filters.get(type);
/* 143 */     if ((filters != null) && (action != null) && (action.length() != 0))
/*     */     {
/* 145 */       action = action.toLowerCase();
/* 146 */       ArrayList generalFilters = filters;
/* 147 */       String typeAction = type + "." + action;
/* 148 */       filters = (ArrayList)m_filters.get(typeAction);
/* 149 */       if (filters == null)
/*     */       {
/* 151 */         filters = retrieveConditionalFilters(type, action, generalFilters);
/* 152 */         m_filters.put(typeAction, filters);
/* 153 */         ArrayList actions = (ArrayList)m_filterTypeActions.get(type);
/* 154 */         if (actions != null)
/*     */         {
/* 156 */           actions.add(action);
/*     */         }
/*     */       }
/*     */     }
/* 160 */     return filters;
/*     */   }
/*     */ 
/*     */   public static ArrayList retrieveConditionalFilters(String type, String action, ArrayList<PluginFilterData> generalFilters)
/*     */     throws ServiceException, DataException
/*     */   {
/* 166 */     ArrayList conditionalFilters = new ArrayList();
/* 167 */     for (PluginFilterData data : generalFilters)
/*     */     {
/* 169 */       String msg = LocaleUtils.encodeMessage("apFilterInstantiationError", null, data.m_location, type);
/*     */ 
/* 171 */       FilterImplementor filter = (FilterImplementor)ComponentClassFactory.createClassInstance(data.m_location, data.m_location, msg);
/*     */ 
/* 174 */       boolean allowAdd = true;
/* 175 */       if (filter instanceof ConditionalFilterImplementor)
/*     */       {
/* 177 */         allowAdd = ((ConditionalFilterImplementor)filter).isActionMonitored(action);
/*     */       }
/*     */ 
/* 180 */       if (allowAdd)
/*     */       {
/* 182 */         conditionalFilters.add(data);
/*     */       }
/*     */     }
/*     */ 
/* 186 */     return conditionalFilters;
/*     */   }
/*     */ 
/*     */   public static void sortFilters()
/*     */   {
/* 191 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 195 */         PluginFilterData f1 = (PluginFilterData)obj1;
/* 196 */         PluginFilterData f2 = (PluginFilterData)obj2;
/* 197 */         if (f1.m_order > f2.m_order)
/*     */         {
/* 199 */           return 1;
/*     */         }
/* 201 */         if (f1.m_order < f2.m_order)
/*     */         {
/* 203 */           return -1;
/*     */         }
/* 205 */         return 0;
/*     */       }
/*     */     };
/* 209 */     Iterator it = m_filters.keySet().iterator();
/* 210 */     while (it.hasNext())
/*     */     {
/* 212 */       String name = (String)it.next();
/* 213 */       ArrayList filterList = (ArrayList)m_filters.get(name);
/* 214 */       Sort.sortList(filterList, cmp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean hasFilter(String type)
/*     */   {
/* 220 */     ArrayList filters = (ArrayList)m_filters.get(type);
/* 221 */     return filters != null;
/*     */   }
/*     */ 
/*     */   public static void removeAllFilters()
/*     */   {
/* 226 */     m_filters.clear();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 231 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 72137 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.PluginFilters
 * JD-Core Version:    0.5.4
 */