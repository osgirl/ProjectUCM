/*     */ package intradoc.lang;
/*     */ 
/*     */ import java.lang.ref.ReferenceQueue;
/*     */ import java.lang.ref.SoftReference;
/*     */ import java.util.AbstractMap;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class SoftHashMap<K, V> extends AbstractMap<K, V>
/*     */ {
/*     */   public Map<K, SoftHashMap<K, V>.SoftValue> m_hashMap;
/*     */   public ReferenceQueue<SoftHashMap<K, V>.SoftValue> m_referenceQueue;
/*     */ 
/*     */   public SoftHashMap()
/*     */   {
/*  42 */     this.m_hashMap = new ConcurrentHashMap();
/*  43 */     this.m_referenceQueue = new ReferenceQueue();
/*     */   }
/*     */ 
/*     */   public V get(Object key)
/*     */   {
/*  53 */     Object value = null;
/*  54 */     SoftValue softValue = (SoftValue)this.m_hashMap.get(key);
/*  55 */     if (softValue != null)
/*     */     {
/*  57 */       value = softValue.get();
/*  58 */       if (value == null)
/*     */       {
/*  60 */         this.m_hashMap.remove(key);
/*     */       }
/*     */     }
/*  63 */     return value;
/*     */   }
/*     */ 
/*     */   public V put(K key, V value)
/*     */   {
/*  74 */     cleanup();
/*  75 */     SoftValue softValue = new SoftValue(value, key, this.m_referenceQueue);
/*  76 */     SoftValue previousSoftValue = (SoftValue)this.m_hashMap.put(key, softValue);
/*  77 */     return (previousSoftValue == null) ? null : previousSoftValue.get();
/*     */   }
/*     */ 
/*     */   public V remove(Object key)
/*     */   {
/*  83 */     cleanup();
/*  84 */     SoftValue previousSoftValue = (SoftValue)this.m_hashMap.remove(key);
/*  85 */     return (previousSoftValue == null) ? null : previousSoftValue.get();
/*     */   }
/*     */ 
/*     */   public void clear()
/*     */   {
/*  91 */     cleanup();
/*  92 */     this.m_hashMap.clear();
/*     */   }
/*     */ 
/*     */   public int size()
/*     */   {
/*  98 */     cleanup();
/*  99 */     return this.m_hashMap.size();
/*     */   }
/*     */ 
/*     */   public Set entrySet()
/*     */   {
/* 105 */     cleanup();
/* 106 */     return this.m_hashMap.entrySet();
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/* 117 */     SoftValue softValue = (SoftValue)this.m_referenceQueue.poll();
/* 118 */     while (softValue != null)
/*     */     {
/* 120 */       this.m_hashMap.remove(softValue.m_key);
/* 121 */       softValue = (SoftValue)this.m_referenceQueue.poll();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 142 */     return "releaseInfo=dev,releaseRevision=$Rev: 99560 $";
/*     */   }
/*     */ 
/*     */   public class SoftValue extends SoftReference
/*     */   {
/*     */     public K m_key;
/*     */ 
/*     */     public SoftValue(K value, ReferenceQueue<SoftHashMap<K, V>.SoftValue> key)
/*     */     {
/* 136 */       super(value, queue);
/* 137 */       this.m_key = key;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.lang.SoftHashMap
 * JD-Core Version:    0.5.4
 */