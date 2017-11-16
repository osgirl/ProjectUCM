/*     */ package intradoc.util;
/*     */ 
/*     */ import java.lang.ref.ReferenceQueue;
/*     */ import java.lang.ref.SoftReference;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class IdcSoftHashMap<K, V> extends HashMap<K, V>
/*     */ {
/*     */   protected ReferenceQueue m_queue;
/*     */   protected Map<K, IdcSoftHashMap<K, V>.IdcSoftReference> m_map;
/*     */ 
/*     */   public IdcSoftHashMap(boolean isSynchronized)
/*     */   {
/*  46 */     this.m_map = ((isSynchronized) ? new ConcurrentHashMap() : new HashMap());
/*  47 */     this.m_queue = new ReferenceQueue();
/*     */   }
/*     */ 
/*     */   public IdcSoftHashMap(boolean isSynchronized, Map<? extends K, ? extends V> m)
/*     */   {
/*  52 */     this(isSynchronized);
/*  53 */     for (Map.Entry entry : m.entrySet())
/*     */     {
/*  55 */       put(entry.getKey(), entry.getValue());
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  62 */     this.m_map.clear();
/*     */   }
/*     */ 
/*     */   public boolean containsKey(Object key)
/*     */   {
/*  68 */     return this.m_map.containsKey(key);
/*     */   }
/*     */ 
/*     */   public boolean containsValue(Object value)
/*     */   {
/*  74 */     for (Map.Entry entry : this.m_map.entrySet())
/*     */     {
/*  76 */       Object v = get(entry.getKey());
/*  77 */       if (v.equals(value))
/*     */       {
/*  79 */         return true;
/*     */       }
/*     */     }
/*  82 */     return false;
/*     */   }
/*     */ 
/*     */   public Set<Map.Entry<K, V>> entrySet()
/*     */   {
/*  88 */     throw new AssertionError("!$IdcSoftHashMap.entrySet() not supported.");
/*     */   }
/*     */ 
/*     */   public V get(Object key)
/*     */   {
/*  94 */     IdcSoftReference r = (IdcSoftReference)this.m_map.get(key);
/*  95 */     if (r == null)
/*     */     {
/*  97 */       return null;
/*     */     }
/*  99 */     Object value = r.get();
/* 100 */     return value;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 106 */     return this.m_map.isEmpty();
/*     */   }
/*     */ 
/*     */   public Set<K> keySet()
/*     */   {
/* 112 */     return this.m_map.keySet();
/*     */   }
/*     */ 
/*     */   public V put(K key, V value)
/*     */   {
/* 118 */     Object oldV = null;
/* 119 */     IdcSoftReference r = (IdcSoftReference)this.m_map.get(key);
/* 120 */     if (r != null)
/*     */     {
/* 122 */       oldV = r.get();
/*     */     }
/* 124 */     r = new IdcSoftReference(key, value, this.m_queue);
/* 125 */     this.m_map.put(key, r);
/*     */ 
/* 127 */     while ((r = (IdcSoftReference)this.m_queue.poll()) != null)
/*     */     {
/* 129 */       IdcSoftReference curRef = (IdcSoftReference)this.m_map.get(r.m_key);
/* 130 */       if (curRef == r)
/*     */       {
/* 135 */         this.m_map.remove(r.m_key);
/*     */       }
/*     */     }
/* 138 */     return oldV;
/*     */   }
/*     */ 
/*     */   public void putAll(Map<? extends K, ? extends V> map)
/*     */   {
/* 144 */     for (Map.Entry entry : map.entrySet())
/*     */     {
/* 146 */       put(entry.getKey(), entry.getValue());
/*     */     }
/*     */   }
/*     */ 
/*     */   public V remove(Object key)
/*     */   {
/* 153 */     IdcSoftReference r = (IdcSoftReference)this.m_map.remove(key);
/* 154 */     if (r != null)
/*     */     {
/* 156 */       Object val = r.get();
/* 157 */       return val;
/*     */     }
/* 159 */     return null;
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/* 165 */     return this.m_map.size();
/*     */   }
/*     */ 
/*     */   public Collection<V> values()
/*     */   {
/* 171 */     throw new AssertionError("!$IdcSoftHashMap.values() not supported.");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 176 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94344 $";
/*     */   }
/*     */ 
/*     */   protected class IdcSoftReference extends SoftReference<V>
/*     */   {
/*     */     K m_key;
/*     */ 
/*     */     IdcSoftReference(V key, ReferenceQueue val)
/*     */     {
/*  36 */       super(val, queue);
/*  37 */       this.m_key = key;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcSoftHashMap
 * JD-Core Version:    0.5.4
 */