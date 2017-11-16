/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class DocClassUtils
/*     */ {
/*     */   public static final String DEFAULT_DOCUMENT_CLASS = "Base";
/*     */   public static final String DEFAULT_META_SET = "DocMeta";
/*     */   public static final String DEFAULT_DOCCLASS_PROFILE_PREFIX = "DCP_";
/*     */   public static final String DMS_TABLE_PREFIX = "DMS";
/*  37 */   protected static List<String> m_docClassNamesList = new ArrayList();
/*  38 */   protected static Set<String> m_docClassNames = new HashSet();
/*  39 */   protected static Map<String, List<String>> m_docClassSetMap = new HashMap();
/*  40 */   protected static Map<String, HashSet> m_docSetFieldMap = new HashMap();
/*  41 */   protected static Map<String, HashSet<String>> m_docClassFieldMap = new HashMap();
/*  42 */   protected static Map<String, String> m_docClassDefaultProfileMap = new HashMap();
/*  43 */   protected static boolean m_areDocClassesUsed = false;
/*     */ 
/*     */   public static void cacheDocClasses(Map<String, List<String>> docClassMap, List<String> docClassNames)
/*     */   {
/*  51 */     m_docClassNamesList = docClassNames;
/*  52 */     m_docClassSetMap = docClassMap;
/*  53 */     m_docClassNames = new HashSet();
/*     */ 
/*  56 */     Iterator it = m_docClassNamesList.iterator();
/*  57 */     while (it.hasNext())
/*     */     {
/*  59 */       String className = (String)it.next();
/*  60 */       m_docClassNames.add(className);
/*  61 */       m_docClassNames.add(className.toLowerCase());
/*     */     }
/*  63 */     if (!m_docClassNamesList.contains("Base"))
/*     */     {
/*  65 */       m_docClassNamesList.add("Base");
/*  66 */       m_docClassNames.add("Base");
/*  67 */       m_docClassNames.add("Base".toLowerCase());
/*     */     }
/*     */ 
/*  70 */     it = m_docClassSetMap.keySet().iterator();
/*  71 */     while (it.hasNext())
/*     */     {
/*  73 */       String className = (String)it.next();
/*  74 */       String classNameLower = className.toLowerCase();
/*  75 */       if (!className.equals(classNameLower))
/*     */       {
/*  77 */         m_docClassSetMap.put(classNameLower, m_docClassSetMap.get(className));
/*     */       }
/*     */     }
/*     */ 
/*  81 */     if (!m_docClassSetMap.isEmpty())
/*     */     {
/*  83 */       m_areDocClassesUsed = true;
/*     */     }
/*  85 */     refreshClassFieldMap();
/*     */   }
/*     */ 
/*     */   public static void cacheDocClassesInfo(Map<String, String> profileMap)
/*     */   {
/*  90 */     m_docClassDefaultProfileMap = profileMap;
/*     */   }
/*     */ 
/*     */   public static void cacheDocMetaSets(MetaFieldData metaFields)
/*     */   {
/*  99 */     m_docSetFieldMap = new HashMap();
/* 100 */     for (metaFields.first(); metaFields.isRowPresent(); metaFields.next())
/*     */     {
/* 102 */       String metaset = metaFields.getDocMetaSet();
/* 103 */       if ((metaset == null) || (metaset.length() <= 0) || (metaset.equals("DocMeta")))
/*     */         continue;
/* 105 */       String name = metaFields.getName();
/* 106 */       HashSet set = (HashSet)m_docSetFieldMap.get(metaset);
/* 107 */       if (set == null)
/*     */       {
/* 109 */         set = new HashSet();
/* 110 */         m_docSetFieldMap.put(metaset, set);
/*     */       }
/* 112 */       set.add(name);
/*     */     }
/*     */ 
/* 115 */     refreshClassFieldMap();
/*     */   }
/*     */ 
/*     */   private static void refreshClassFieldMap()
/*     */   {
/* 120 */     m_docClassFieldMap = new HashMap();
/* 121 */     if ((m_docSetFieldMap == null) || (m_docClassSetMap == null) || (m_docSetFieldMap.isEmpty()) || (m_docClassSetMap.isEmpty()))
/*     */     {
/* 123 */       return;
/*     */     }
/*     */ 
/* 127 */     Iterator class_iter = m_docClassSetMap.keySet().iterator();
/* 128 */     while (class_iter.hasNext())
/*     */     {
/* 130 */       String className = (String)class_iter.next();
/* 131 */       List sets = getClassDMSTables(className);
/* 132 */       HashSet fields = new HashSet();
/*     */ 
/* 134 */       for (int i = 0; i < sets.size(); ++i)
/*     */       {
/* 136 */         fields.addAll((Collection)m_docSetFieldMap.get(sets.get(i)));
/*     */       }
/*     */ 
/* 139 */       m_docClassFieldMap.put(className, fields);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean doesDocClassExist(String className, boolean isCaseSensitive)
/*     */   {
/* 146 */     if (isCaseSensitive)
/*     */     {
/* 148 */       return m_docClassNamesList.contains(className);
/*     */     }
/*     */ 
/* 151 */     return m_docClassNames.contains(className);
/*     */   }
/*     */ 
/*     */   public static boolean areDocClassesUsed()
/*     */   {
/* 156 */     return m_areDocClassesUsed;
/*     */   }
/*     */ 
/*     */   public static Set getNonemptyClasses()
/*     */   {
/* 162 */     return m_docClassSetMap.keySet();
/*     */   }
/*     */ 
/*     */   public static List<String> getClassList()
/*     */   {
/* 168 */     return m_docClassNamesList;
/*     */   }
/*     */ 
/*     */   public static Iterator<String> getNonemptyClassesIterator()
/*     */   {
/* 175 */     return m_docClassSetMap.keySet().iterator();
/*     */   }
/*     */ 
/*     */   public static List<String> getClassDMSTables(String className)
/*     */   {
/* 181 */     List sets = (List)m_docClassSetMap.get(className);
/* 182 */     if (sets != null)
/*     */     {
/* 184 */       return sets;
/*     */     }
/*     */ 
/* 187 */     String classNameLower = className.toLowerCase();
/* 188 */     return (List)m_docClassSetMap.get(classNameLower);
/*     */   }
/*     */ 
/*     */   public static Set<String> getClassFields(String className)
/*     */   {
/* 199 */     return (Set)m_docClassFieldMap.get(className);
/*     */   }
/*     */ 
/*     */   public static Iterator<String> getCommonTableSets(List<String> docClassesList)
/*     */   {
/* 206 */     Set docMetaSets = new HashSet();
/* 207 */     boolean isFirstAdd = true;
/* 208 */     for (String dclass : docClassesList)
/*     */     {
/* 210 */       if (dclass == null) continue; if (dclass.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 214 */       List sets = getClassDMSTables(dclass);
/* 215 */       if (sets == null)
/*     */       {
/* 219 */         docMetaSets.clear();
/* 220 */         break;
/*     */       }
/*     */ 
/* 223 */       if (isFirstAdd)
/*     */       {
/* 225 */         docMetaSets.addAll(sets);
/* 226 */         isFirstAdd = false;
/*     */       }
/*     */       else
/*     */       {
/* 230 */         docMetaSets.retainAll(sets);
/* 231 */         if (docMetaSets.isEmpty()) {
/*     */           break;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 238 */     return docMetaSets.iterator();
/*     */   }
/*     */ 
/*     */   public static boolean isSetInClass(String className, String setName)
/*     */   {
/* 244 */     if ("DocMeta".equalsIgnoreCase(setName))
/*     */     {
/* 246 */       return true;
/*     */     }
/*     */ 
/* 249 */     List sets = getClassDMSTables(className);
/* 250 */     if ((sets == null) || (sets.isEmpty()))
/*     */     {
/* 252 */       return false;
/*     */     }
/*     */ 
/* 257 */     for (String dms : sets)
/*     */     {
/* 259 */       if (dms.equalsIgnoreCase(setName))
/*     */       {
/* 261 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 265 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isFieldInClass(String className, String fieldName)
/*     */   {
/* 272 */     Set fields = getClassFields(className);
/* 273 */     if ((fields == null) || (fields.isEmpty()))
/*     */     {
/* 275 */       return false;
/*     */     }
/*     */ 
/* 278 */     return fields.contains(fieldName);
/*     */   }
/*     */ 
/*     */   public static boolean isSetUsed(String setName)
/*     */   {
/* 288 */     Iterator class_iter = m_docClassSetMap.keySet().iterator();
/* 289 */     while (class_iter.hasNext())
/*     */     {
/* 291 */       String className = (String)class_iter.next();
/* 292 */       List sets = getClassDMSTables(className);
/* 293 */       for (String dms : sets)
/*     */       {
/* 295 */         if (dms.equalsIgnoreCase(setName))
/*     */         {
/* 297 */           return true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 302 */     return false;
/*     */   }
/*     */ 
/*     */   public static String getDefaultClassProfile(String className, Workspace ws) throws DataException
/*     */   {
/* 307 */     String profile = (String)m_docClassDefaultProfileMap.get(className);
/* 308 */     return "DCP_" + className;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 313 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97937 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DocClassUtils
 * JD-Core Version:    0.5.4
 */