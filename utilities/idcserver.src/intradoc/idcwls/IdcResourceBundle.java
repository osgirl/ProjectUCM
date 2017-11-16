/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainerUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.utils.MessageLoggerUtils;
/*     */ import intradoc.util.IdcBundleContents;
/*     */ import java.util.HashMap;
/*     */ import java.util.ListResourceBundle;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcResourceBundle extends ListResourceBundle
/*     */   implements IdcBundleContents
/*     */ {
/*     */   protected Object[][] getContents()
/*     */   {
/*  41 */     Object[][] contents = computeContents();
/*  42 */     return contents;
/*     */   }
/*     */ 
/*     */   public Object[][] getResourceContents()
/*     */   {
/*  50 */     return getContents();
/*     */   }
/*     */ 
/*     */   protected Object[][] computeContents() {
/*  57 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/*  58 */     cxt.setCachedObject("ConvertToJavaStandardForm", "1");
/*  59 */     cxt.setCachedObject("ConvertToJavaStandardTypes", new HashMap());
/*  60 */     Object[][] contents = (Object[][])null;
/*     */     Map map;
/*     */     int count;
/*     */     try {
/*  63 */       map = new HashMap();
/*  64 */       String genericExceptionKey = MessageLoggerUtils.getHardWiredGeneralExceptionKey();
/*  65 */       map.put(genericExceptionKey, "general exception");
/*  66 */       Table t = ResourceContainerUtils.getDynamicTableResource("UCM_MessageKeys");
/*  67 */       if (t != null)
/*     */       {
/*  69 */         DataResultSet drset = new DataResultSet();
/*  70 */         drset.init(t);
/*     */ 
/*  72 */         int nameIndex = ResultSetUtils.getIndexMustExist(drset, "name");
/*  73 */         int prefixIndex = ResultSetUtils.getIndexMustExist(drset, "prefix");
/*  74 */         int numberIndex = ResultSetUtils.getIndexMustExist(drset, "number");
/*  75 */         int textIndex = ResultSetUtils.getIndexMustExist(drset, "text");
/*  76 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/*  78 */           String name = drset.getStringValue(nameIndex);
/*  79 */           String prefix = drset.getStringValue(prefixIndex);
/*  80 */           String number = drset.getStringValue(numberIndex);
/*  81 */           String key = MessageLoggerUtils.calculateLoggerMessageKey(name, prefix, number);
/*  82 */           String text = LocaleResources.getStringInternal(name, null);
/*  83 */           if ((text != null) && (text.length() > 0))
/*     */           {
/*  88 */             text = LocaleResources.getString(name, cxt);
/*     */           }
/*  90 */           else if ((((text == null) || (text.length() == 0))) && (textIndex >= 0))
/*     */           {
/*  93 */             text = drset.getStringValue(textIndex);
/*     */           }
/*  95 */           if ((text == null) || (text.length() <= 0))
/*     */             continue;
/*  97 */           map.put(key, text);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 103 */       int size = map.size();
/* 104 */       contents = new Object[size + 1][2];
/*     */ 
/* 107 */       contents[0][0] = "oracle.core.ojdl.logging.MessageIdKeyResourceBundle";
/* 108 */       contents[0][1] = "";
/*     */ 
/* 111 */       Set set = map.keySet();
/* 112 */       count = 1;
/* 113 */       for (String key : set)
/*     */       {
/* 115 */         contents[count][0] = key;
/* 116 */         contents[count][1] = map.get(key);
/* 117 */         ++count;
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 122 */       Report.error(null, e, "csMsgBundleBuildError", new Object[0]);
/*     */     }
/* 124 */     return contents;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 129 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86755 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcResourceBundle
 * JD-Core Version:    0.5.4
 */