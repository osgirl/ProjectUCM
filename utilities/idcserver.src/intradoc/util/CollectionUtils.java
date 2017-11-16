/*     */ package intradoc.util;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class CollectionUtils
/*     */ {
/*     */   public static List appendToList(List list, Object[] items)
/*     */   {
/*  41 */     if (list == null)
/*     */     {
/*  43 */       list = new ArrayList(items.length);
/*     */     }
/*  45 */     for (Object item : items)
/*     */     {
/*  47 */       list.add(item);
/*     */     }
/*  49 */     return list;
/*     */   }
/*     */ 
/*     */   public static ArrayList copyToList(Object[] array)
/*     */   {
/*  65 */     ArrayList list = new ArrayList();
/*  66 */     list.addAll(Arrays.asList(array));
/*  67 */     return list;
/*     */   }
/*     */ 
/*     */   public static Collection createIntersection(Collection a, Collection b)
/*     */   {
/*     */     Collection larger;
/*     */     Collection smaller;
/*     */     Collection larger;
/*  80 */     if (a.size() < b.size())
/*     */     {
/*  82 */       Collection smaller = a;
/*  83 */       larger = b;
/*     */     }
/*     */     else
/*     */     {
/*  87 */       smaller = b;
/*  88 */       larger = a;
/*     */     }
/*     */ 
/*  91 */     Collection c = new HashSet();
/*  92 */     for (Iterator i$ = smaller.iterator(); i$.hasNext(); ) { Object o = i$.next();
/*     */ 
/*  94 */       if (larger.contains(o))
/*     */       {
/*  96 */         c.add(o);
/*     */       } }
/*     */ 
/*     */ 
/* 100 */     return c;
/*     */   }
/*     */ 
/*     */   public static Collection createUnion(Collection a, Collection b)
/*     */   {
/* 111 */     Collection c = new HashSet();
/* 112 */     c.addAll(a);
/* 113 */     c.addAll(b);
/* 114 */     return c;
/*     */   }
/*     */ 
/*     */   public static List createUniqueListFromCollection(Collection col)
/*     */   {
/* 124 */     Map foundItems = new HashMap();
/* 125 */     List newCol = null;
/*     */     int pass;
/*     */     Iterator i$;
/* 126 */     for (pass : new int[] { 1, 2 })
/*     */     {
/* 128 */       for (i$ = col.iterator(); i$.hasNext(); ) { Object obj = i$.next();
/*     */ 
/* 130 */         if (foundItems.get(obj) != null)
/*     */         {
/* 132 */           if (pass != 1)
/*     */             continue;
/* 134 */           foundItems = new HashMap();
/* 135 */           newCol = new ArrayList();
/* 136 */           break;
/*     */         }
/*     */ 
/* 140 */         foundItems.put(obj, obj);
/* 141 */         if (pass == 2)
/*     */         {
/* 143 */           newCol.add(obj);
/*     */         } }
/*     */ 
/*     */     }
/* 147 */     return newCol;
/*     */   }
/*     */ 
/*     */   public static void removeDuplicatesFromList(List list)
/*     */   {
/* 156 */     Map foundItems = new HashMap();
/* 157 */     int size = list.size();
/* 158 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 160 */       Object obj = list.get(i);
/* 161 */       if (foundItems.get(obj) != null)
/*     */       {
/* 163 */         list.remove(i);
/* 164 */         --i;
/* 165 */         --size;
/*     */       }
/* 167 */       foundItems.put(obj, obj);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int mergeMaps(Map src, Map dst, Object[] keys)
/*     */   {
/* 180 */     int count = 0;
/* 181 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 183 */       Object key = keys[i];
/* 184 */       Object value = src.get(key);
/* 185 */       if (value == null)
/*     */         continue;
/* 187 */       ++count;
/* 188 */       dst.put(key, value);
/*     */     }
/*     */ 
/* 191 */     return count;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 196 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94469 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.CollectionUtils
 * JD-Core Version:    0.5.4
 */