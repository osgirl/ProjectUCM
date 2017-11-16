/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class AvsSearchConfigCompanion extends CommonSearchConfigCompanionAdaptor
/*     */ {
/*     */   public int fixUpAndValidateQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  31 */     String query = binder.getLocal("QueryText");
/*     */ 
/*  33 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  34 */     cxt.setCachedObject("UserDateFormat", binder.m_blDateFormat);
/*  35 */     Map types = binder.getFieldTypes();
/*     */ 
/*  37 */     Iterator it = types.keySet().iterator();
/*  38 */     while (it.hasNext())
/*     */     {
/*  40 */       String key = (String)it.next();
/*  41 */       String type = (String)types.get(key);
/*  42 */       if (type.equalsIgnoreCase("date"))
/*     */       {
/*  44 */         int i = query.indexOf(key);
/*  45 */         while (i >= 0)
/*     */         {
/*  47 */           int index = i;
/*  48 */           i = index + key.length();
/*  49 */           String pre = query.substring(0, index);
/*  50 */           String tmp = query.substring(index + key.length() + 1).trim();
/*     */ 
/*  52 */           index = tmp.indexOf("]");
/*  53 */           if (index < 0)
/*     */           {
/*  55 */             i = query.indexOf(key, i);
/*     */           }
/*     */ 
/*  58 */           String value = tmp.substring(0, index);
/*  59 */           String post = tmp.substring(index + 1);
/*  60 */           String afterDateValue = null;
/*  61 */           String beforeDateValue = null;
/*  62 */           Date afterDate = null;
/*  63 */           Date beforeDate = null;
/*     */ 
/*  66 */           if (value.startsWith("-"))
/*     */           {
/*  68 */             beforeDateValue = value.substring(1);
/*     */           }
/*  70 */           else if (value.endsWith("-"))
/*     */           {
/*  72 */             afterDateValue = value.substring(0, value.length() - 1);
/*     */           }
/*     */           else
/*     */           {
/*  76 */             index = value.indexOf("-");
/*  77 */             if (index < 0)
/*     */             {
/*  79 */               throw new DataException("!csAvsDateRangeError");
/*     */             }
/*  81 */             afterDateValue = value.substring(0, index);
/*  82 */             beforeDateValue = value.substring(index + 1, value.length());
/*     */           }
/*  84 */           if ((afterDateValue != null) && (afterDateValue.length() > 0))
/*     */           {
/*  86 */             afterDate = LocaleResources.parseDateDataEntry(afterDateValue, cxt, "fixUpAndValidateQuery");
/*     */           }
/*     */ 
/*  90 */           if ((beforeDateValue != null) && (beforeDateValue.length() > 0))
/*     */           {
/*  92 */             beforeDate = LocaleResources.parseDateDataEntry(beforeDateValue, cxt, "fixUpAndValidateQuery");
/*     */           }
/*     */ 
/*  95 */           query = pre + key + ":";
/*     */ 
/*  98 */           if (afterDate != null)
/*     */           {
/* 100 */             query = query + afterDate.getTime() / 1000L;
/*     */           }
/* 102 */           query = query + "-";
/*     */ 
/* 104 */           if (beforeDate != null)
/*     */           {
/* 106 */             query = query + beforeDate.getTime() / 1000L;
/*     */           }
/* 108 */           query = query + "]";
/*     */ 
/* 110 */           i = query.length();
/* 111 */           query = query + post;
/* 112 */           i = query.indexOf(key, i);
/*     */         }
/*     */       }
/*     */     }
/* 116 */     binder.putLocal("QueryText", query);
/* 117 */     return 1;
/*     */   }
/*     */ 
/*     */   public int prepareQuery(DataBinder binder, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 123 */     String sortField = binder.getLocal("SortField");
/* 124 */     String sortOrder = binder.getLocal("SortOrder");
/* 125 */     String sortSpec = binder.getLocal("SortSpec");
/* 126 */     if (sortSpec == null)
/*     */     {
/* 128 */       if ((sortField == null) || (sortField.length() == 0) || (sortOrder == null))
/*     */       {
/* 130 */         sortSpec = "";
/*     */       }
/*     */       else
/*     */       {
/* 135 */         DataResultSet drset = SharedObjects.getTable("IndexerFieldsMap");
/* 136 */         String type = ResultSetUtils.findValue(drset, "fieldName", sortField, "fieldType");
/* 137 */         if ((type == null) || (type.equalsIgnoreCase("literal")))
/*     */         {
/* 139 */           sortSpec = "";
/* 140 */           binder.putLocal("SortSearchResultAfterQuery", "1");
/*     */         } else {
/* 142 */           if ((sortField.equalsIgnoreCase("Score")) && (sortOrder.equalsIgnoreCase("asc")))
/*     */           {
/* 144 */             throw new ServiceException("!csAvsNoAscendingForScore");
/*     */           }
/*     */ 
/* 148 */           if (sortOrder.equalsIgnoreCase("desc"))
/*     */           {
/* 150 */             sortOrder = "+";
/*     */           }
/*     */           else
/*     */           {
/* 154 */             sortOrder = "-";
/*     */           }
/* 156 */           binder.putLocal("SortOrder", sortOrder);
/* 157 */           sortSpec = sortOrder + sortField;
/*     */         }
/*     */       }
/* 160 */       binder.putLocal("SortSpec", sortSpec);
/*     */     }
/* 162 */     return 0;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 167 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.AvsSearchConfigCompanion
 * JD-Core Version:    0.5.4
 */