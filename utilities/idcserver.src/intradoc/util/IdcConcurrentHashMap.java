/*     */ package intradoc.util;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class IdcConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V>
/*     */ {
/*  28 */   protected boolean m_isCaseSensitive = false;
/*  29 */   protected Map<String, String> m_keyMap = new HashMap();
/*  30 */   protected Map<String, List> m_reverseKeyMap = new HashMap();
/*     */ 
/*     */   public IdcConcurrentHashMap()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcConcurrentHashMap(boolean isCaseSensitive)
/*     */   {
/*  39 */     this.m_isCaseSensitive = isCaseSensitive;
/*     */   }
/*     */ 
/*     */   public void initCaseInsensitiveKeyMap(boolean allowKeyMapMissCache)
/*     */   {
/*     */   }
/*     */ 
/*     */   public V get(Object key)
/*     */   {
/*  58 */     if ((this.m_isCaseSensitive) || (!key instanceof String))
/*     */     {
/*  60 */       return super.get(key);
/*     */     }
/*  62 */     Object result = null;
/*  63 */     String tmpKey = (String)this.m_keyMap.get(key);
/*  64 */     if (tmpKey == null)
/*     */     {
/*  67 */       tmpKey = ((String)key).toLowerCase();
/*  68 */       result = super.get(tmpKey);
/*  69 */       if (result != null)
/*     */       {
/*  71 */         addKeyMap((String)key, tmpKey);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*  76 */       result = super.get(tmpKey);
/*     */     }
/*  78 */     return result;
/*     */   }
/*     */ 
/*     */   public V put(K key, V value)
/*     */   {
/*  84 */     if ((this.m_isCaseSensitive) || (!key instanceof String))
/*     */     {
/*  86 */       return super.put(key, value);
/*     */     }
/*  88 */     String tmpKey = ((String)key).toLowerCase();
/*  89 */     Object result = super.put(tmpKey, value);
/*  90 */     if (result == null)
/*     */     {
/*  93 */       addKeyMap((String)key, tmpKey);
/*     */     }
/*     */ 
/*  96 */     return result;
/*     */   }
/*     */ 
/*     */   public V remove(Object key)
/*     */   {
/* 102 */     if ((this.m_isCaseSensitive) || (!key instanceof String))
/*     */     {
/* 104 */       return super.remove(key);
/*     */     }
/* 106 */     Object result = null;
/* 107 */     String tmpKey = (String)this.m_keyMap.get(key);
/* 108 */     if (tmpKey == null)
/*     */     {
/* 111 */       tmpKey = ((String)key).toLowerCase();
/*     */     }
/* 113 */     result = super.remove(tmpKey);
/* 114 */     if ((result != null) && (this.m_reverseKeyMap != null))
/*     */     {
/* 116 */       removeKeyMap(tmpKey);
/*     */     }
/* 118 */     return result;
/*     */   }
/*     */ 
/*     */   protected void addKeyMap(String key, String lowerCaseKey)
/*     */   {
/* 123 */     String prevKey = (String)this.m_keyMap.put(key, lowerCaseKey);
/*     */ 
/* 125 */     if ((prevKey != null) || (this.m_reverseKeyMap == null))
/*     */       return;
/* 127 */     List reverseKeyList = (List)this.m_reverseKeyMap.get(lowerCaseKey);
/* 128 */     if (reverseKeyList == null)
/*     */     {
/* 130 */       reverseKeyList = new ArrayList();
/* 131 */       this.m_reverseKeyMap.put(lowerCaseKey, reverseKeyList);
/*     */     }
/* 133 */     reverseKeyList.add(key);
/*     */   }
/*     */ 
/*     */   protected void removeKeyMap(String lowerCaseKey)
/*     */   {
/* 139 */     List reverseKeyList = (List)this.m_reverseKeyMap.remove(lowerCaseKey);
/* 140 */     if (reverseKeyList == null)
/*     */     {
/* 142 */       return;
/*     */     }
/* 144 */     for (String key : reverseKeyList)
/*     */     {
/* 146 */       this.m_keyMap.remove(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66831 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.IdcConcurrentHashMap
 * JD-Core Version:    0.5.4
 */