/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcMapWithDefaults
/*     */   implements Map, Cloneable
/*     */ {
/*     */   public Map m_map;
/*     */   public Map m_defaults;
/*     */ 
/*     */   public IdcMapWithDefaults()
/*     */   {
/*  50 */     this.m_map = new HashMap();
/*  51 */     this.m_defaults = null;
/*     */   }
/*     */ 
/*     */   public IdcMapWithDefaults(Map defaults)
/*     */   {
/*  61 */     this.m_map = new HashMap();
/*  62 */     this.m_defaults = defaults;
/*     */   }
/*     */ 
/*     */   public IdcMapWithDefaults(Map map, Map defaults)
/*     */   {
/*  72 */     this.m_map = map;
/*  73 */     this.m_defaults = defaults;
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/*  78 */     return this.m_map.size();
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  83 */     this.m_map.clear();
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/*  88 */     return this.m_map.isEmpty();
/*     */   }
/*     */ 
/*     */   public boolean containsKey(Object arg0)
/*     */   {
/*  93 */     return this.m_map.containsKey(arg0);
/*     */   }
/*     */ 
/*     */   public boolean containsValue(Object arg0)
/*     */   {
/*  98 */     return this.m_map.containsValue(arg0);
/*     */   }
/*     */ 
/*     */   public Collection values()
/*     */   {
/* 103 */     return this.m_map.values();
/*     */   }
/*     */ 
/*     */   public void putAll(Map arg0)
/*     */   {
/* 108 */     this.m_map.putAll(arg0);
/*     */   }
/*     */ 
/*     */   public Set entrySet()
/*     */   {
/* 113 */     return this.m_map.entrySet();
/*     */   }
/*     */ 
/*     */   public Set keySet()
/*     */   {
/* 118 */     return this.m_map.keySet();
/*     */   }
/*     */ 
/*     */   public Set mainAndDefaultsKeySet()
/*     */   {
/* 127 */     Set set = this.m_map.keySet();
/*     */ 
/* 129 */     Map defaults = this.m_defaults;
/* 130 */     if (defaults instanceof IdcMapWithDefaults)
/*     */     {
/* 132 */       IdcMapWithDefaults idcDefaults = (IdcMapWithDefaults)defaults;
/* 133 */       set.addAll(idcDefaults.mainAndDefaultsKeySet());
/*     */     }
/*     */     else
/*     */     {
/* 137 */       set.addAll(defaults.keySet());
/*     */     }
/*     */ 
/* 140 */     return set;
/*     */   }
/*     */ 
/*     */   public Object get(Object arg0)
/*     */   {
/* 145 */     Object o = this.m_map.get(arg0);
/*     */ 
/* 147 */     if ((o == null) && (this.m_defaults != null))
/*     */     {
/* 149 */       o = this.m_defaults.get(arg0);
/*     */     }
/*     */ 
/* 152 */     return o;
/*     */   }
/*     */ 
/*     */   public Object remove(Object arg0)
/*     */   {
/* 157 */     return this.m_map.remove(arg0);
/*     */   }
/*     */ 
/*     */   public Object put(Object arg0, Object arg1)
/*     */   {
/* 162 */     return this.m_map.put(arg0, arg1);
/*     */   }
/*     */ 
/*     */   public Object clone()
/*     */   {
/*     */     Map map;
/*     */     Map map;
/* 176 */     if (this.m_map instanceof HashMap)
/*     */     {
/* 178 */       map = (Cloneable)((HashMap)this.m_map).clone();
/*     */     }
/*     */     else
/*     */     {
/*     */       Map map;
/* 180 */       if (this.m_map instanceof Hashtable)
/*     */       {
/* 182 */         map = (Cloneable)((Hashtable)this.m_map).clone();
/*     */       }
/*     */       else
/*     */       {
/*     */         Map map;
/* 184 */         if (this.m_map instanceof IdcMapWithDefaults)
/*     */         {
/* 186 */           map = (Cloneable)((IdcMapWithDefaults)this.m_map).clone();
/*     */         }
/*     */         else
/*     */         {
/* 190 */           map = new HashMap();
/* 191 */           Object[] keys = this.m_map.keySet().toArray();
/* 192 */           for (int i = 0; i < keys.length; ++i)
/*     */           {
/* 194 */             map.put(keys[i], this.m_map.get(keys[i]));
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 198 */     IdcMapWithDefaults idcMap = new IdcMapWithDefaults(map, this.m_defaults);
/*     */ 
/* 200 */     return idcMap;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 205 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcMapWithDefaults
 * JD-Core Version:    0.5.4
 */