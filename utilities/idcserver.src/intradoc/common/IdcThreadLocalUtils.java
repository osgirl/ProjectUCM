/*    */ package intradoc.common;
/*    */ 
/*    */ import java.util.concurrent.ConcurrentHashMap;
/*    */ 
/*    */ public class IdcThreadLocalUtils
/*    */ {
/* 24 */   private static ThreadLocal<ConcurrentHashMap> m_threadLocal = new ThreadLocal();
/*    */ 
/*    */   public static void set(String name, String value)
/*    */   {
/* 28 */     ConcurrentHashMap map = null;
/* 29 */     if (m_threadLocal == null)
/*    */       return;
/* 31 */     map = (ConcurrentHashMap)m_threadLocal.get();
/* 32 */     if (map == null)
/*    */     {
/* 34 */       map = new ConcurrentHashMap();
/*    */     }
/* 36 */     map.put(name, value);
/* 37 */     m_threadLocal.set(map);
/*    */   }
/*    */ 
/*    */   public static String get(String name)
/*    */   {
/* 43 */     ConcurrentHashMap map = null;
/* 44 */     if (m_threadLocal != null)
/*    */     {
/* 46 */       map = (ConcurrentHashMap)m_threadLocal.get();
/*    */     }
/* 48 */     String value = "";
/* 49 */     if ((map != null) && (map.get(name) != null))
/*    */     {
/* 51 */       value = (String)map.get(name);
/*    */     }
/* 53 */     return value;
/*    */   }
/*    */ 
/*    */   public static void remove(String name)
/*    */   {
/* 58 */     ConcurrentHashMap map = (ConcurrentHashMap)m_threadLocal.get();
/* 59 */     if (map != null)
/*    */     {
/* 61 */       map.remove(name);
/*    */     }
/* 63 */     if (m_threadLocal == null)
/*    */       return;
/* 65 */     m_threadLocal.set(map);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 71 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96496 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcThreadLocalUtils
 * JD-Core Version:    0.5.4
 */