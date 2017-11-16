/*     */ package intradoc.common;
/*     */ 
/*     */ public class ResourceContainerUtils
/*     */ {
/*  25 */   protected static ResourceContainer m_resources = null;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  32 */     m_resources = new ResourceContainer();
/*     */   }
/*     */ 
/*     */   public static ResourceContainer getResources()
/*     */   {
/*  41 */     return m_resources;
/*     */   }
/*     */ 
/*     */   public static void setResources(ResourceContainer resources)
/*     */   {
/*  51 */     m_resources = resources;
/*     */   }
/*     */ 
/*     */   public static DynamicData getDynamicDataResource(String varName)
/*     */   {
/*  61 */     return m_resources.getDynamicDataResource(varName);
/*     */   }
/*     */ 
/*     */   public static Table getDynamicTableResource(String varName)
/*     */   {
/*  71 */     if (m_resources == null)
/*     */     {
/*  73 */       return null;
/*     */     }
/*  75 */     DynamicData dynData = m_resources.getDynamicDataResource(varName);
/*  76 */     Table t = null;
/*  77 */     if (dynData != null)
/*     */     {
/*  79 */       t = dynData.m_mergedTable;
/*     */     }
/*  81 */     return t;
/*     */   }
/*     */ 
/*     */   public static String[] getDynamicFieldListResource(String varName)
/*     */   {
/*  91 */     DynamicData dynData = m_resources.getDynamicDataResource(varName);
/*  92 */     Table t = null;
/*  93 */     String[] fieldList = null;
/*  94 */     if (dynData != null)
/*     */     {
/*  96 */       t = (dynData.m_hasMergedTable) ? dynData.m_mergedTable : dynData.m_table;
/*     */     }
/*  98 */     if (t != null)
/*     */     {
/* 100 */       fieldList = t.m_colNames;
/*     */     }
/* 102 */     return fieldList;
/*     */   }
/*     */ 
/*     */   public static String[] getDynamicFieldListResource(String varName, String[] defList)
/*     */   {
/* 114 */     DynamicData dynData = m_resources.getDynamicDataResource(varName);
/* 115 */     Table t = null;
/* 116 */     String[] fieldList = defList;
/* 117 */     if (dynData != null)
/*     */     {
/* 119 */       t = (dynData.m_hasMergedTable) ? dynData.m_mergedTable : dynData.m_table;
/*     */     }
/* 121 */     if (t != null)
/*     */     {
/* 123 */       fieldList = t.m_colNames;
/*     */     }
/* 125 */     return fieldList;
/*     */   }
/*     */ 
/*     */   public static DynamicHtml getHtmlResource(String name)
/*     */   {
/* 135 */     return m_resources.getHtmlResource(name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 141 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78304 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceContainerUtils
 * JD-Core Version:    0.5.4
 */