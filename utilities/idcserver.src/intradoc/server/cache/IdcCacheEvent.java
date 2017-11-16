/*     */ package intradoc.server.cache;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.EventObject;
/*     */ 
/*     */ public class IdcCacheEvent extends EventObject
/*     */ {
/*     */   private static final long serialVersionUID = -6748616192188228800L;
/*     */   public static final int CACHE_EVENT_INSERT = 0;
/*     */   public static final int CACHE_EVENT_UPDATE = 1;
/*     */   public static final int CACHE_EVENT_DELETE = 2;
/*     */   protected int m_eventType;
/*     */   protected String m_key;
/*     */   protected Serializable m_value;
/*     */ 
/*     */   public IdcCacheEvent(IdcCacheRegion cacheRegion)
/*     */   {
/*  76 */     super(cacheRegion);
/*     */   }
/*     */ 
/*     */   public IdcCacheEvent(IdcCacheRegion cacheRegion, int eventType, String key, Serializable value)
/*     */   {
/*  88 */     super(cacheRegion);
/*  89 */     this.m_eventType = eventType;
/*  90 */     this.m_key = key;
/*  91 */     this.m_value = value;
/*     */   }
/*     */ 
/*     */   public int getEventType()
/*     */   {
/* 100 */     return this.m_eventType;
/*     */   }
/*     */ 
/*     */   public Serializable getValue()
/*     */   {
/* 109 */     return this.m_value;
/*     */   }
/*     */ 
/*     */   public String getKey()
/*     */   {
/* 118 */     return this.m_key;
/*     */   }
/*     */ 
/*     */   public void setEventType(int eventType)
/*     */   {
/* 127 */     this.m_eventType = eventType;
/*     */   }
/*     */ 
/*     */   public void setValue(Serializable value)
/*     */   {
/* 136 */     this.m_value = value;
/*     */   }
/*     */ 
/*     */   public void setKey(String key)
/*     */   {
/* 145 */     this.m_key = key;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 150 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101532 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheEvent
 * JD-Core Version:    0.5.4
 */