/*     */ package intradoc.filestore;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class BasicIdcFileDescriptor
/*     */   implements IdcFileDescriptor
/*     */ {
/*     */   protected String m_className;
/*  27 */   protected Map m_data = null;
/*  28 */   protected Map m_cache = null;
/*     */ 
/*  30 */   protected boolean m_isLinked = false;
/*     */ 
/*     */   public BasicIdcFileDescriptor()
/*     */   {
/*  34 */     this.m_data = new HashMap();
/*  35 */     this.m_cache = new HashMap();
/*     */   }
/*     */ 
/*     */   public IdcFileDescriptor createClone()
/*     */   {
/*  40 */     BasicIdcFileDescriptor cDesc = new BasicIdcFileDescriptor();
/*  41 */     cDesc.m_data = cloneMap(this.m_data);
/*  42 */     return cDesc;
/*     */   }
/*     */ 
/*     */   public String getProperty(String key)
/*     */   {
/*  47 */     Object data = this.m_data.get(key);
/*  48 */     if (data instanceof String)
/*     */     {
/*  50 */       return (String)data;
/*     */     }
/*  52 */     return null;
/*     */   }
/*     */ 
/*     */   public Object get(String key)
/*     */   {
/*  57 */     return this.m_data.get(key);
/*     */   }
/*     */ 
/*     */   public void put(String key, Object value)
/*     */   {
/*  62 */     this.m_data.put(key, value);
/*     */   }
/*     */ 
/*     */   public Object remove(String key)
/*     */   {
/*  67 */     return this.m_data.remove(key);
/*     */   }
/*     */ 
/*     */   public void putCacheValue(String key, Object value)
/*     */   {
/*  72 */     this.m_cache.put(key, value);
/*     */   }
/*     */ 
/*     */   public String getCacheProperty(String key)
/*     */   {
/*  77 */     Object val = this.m_cache.get(key);
/*  78 */     if (val instanceof String)
/*     */     {
/*  80 */       return (String)val;
/*     */     }
/*  82 */     return null;
/*     */   }
/*     */ 
/*     */   public Object getCacheObject(String key)
/*     */   {
/*  87 */     return this.m_cache.get(key);
/*     */   }
/*     */ 
/*     */   public Map cloneMap(Map map)
/*     */   {
/*  92 */     Map mm = null;
/*  93 */     if (map instanceof HashMap)
/*     */     {
/*  95 */       HashMap m = (HashMap)map;
/*  96 */       mm = (Map)m.clone();
/*     */     }
/*  98 */     else if (map instanceof Hashtable)
/*     */     {
/* 100 */       Hashtable m = (Hashtable)map;
/* 101 */       mm = (Hashtable)m.clone();
/*     */     }
/* 103 */     return mm;
/*     */   }
/*     */ 
/*     */   public boolean isLinked()
/*     */   {
/* 108 */     return this.m_isLinked;
/*     */   }
/*     */ 
/*     */   public void setIsLinked(boolean isLinked)
/*     */   {
/* 113 */     this.m_isLinked = isLinked;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 119 */     if (this.m_className == null)
/*     */     {
/* 121 */       this.m_className = super.getClass().getName();
/*     */     }
/* 123 */     return this.m_className + this.m_data.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 128 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.BasicIdcFileDescriptor
 * JD-Core Version:    0.5.4
 */