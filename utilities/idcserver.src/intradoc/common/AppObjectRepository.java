/*    */ package intradoc.common;
/*    */ 
/*    */ import java.util.Hashtable;
/*    */ import java.util.concurrent.ConcurrentHashMap;
/*    */ 
/*    */ public class AppObjectRepository
/*    */ {
/* 29 */   protected static ConcurrentHashMap m_repository = new ConcurrentHashMap();
/*    */ 
/*    */   public static Object putObject(String name, Object obj)
/*    */   {
/* 33 */     return m_repository.put(name, obj);
/*    */   }
/*    */ 
/*    */   public static Object getObject(String name)
/*    */   {
/* 38 */     return m_repository.get(name);
/*    */   }
/*    */ 
/*    */   public static Object removeObject(String name)
/*    */   {
/* 43 */     return m_repository.remove(name);
/*    */   }
/*    */ 
/*    */   public static Hashtable getRepository()
/*    */   {
/* 48 */     Hashtable repCopy = new Hashtable();
/* 49 */     repCopy.putAll(m_repository);
/* 50 */     return repCopy;
/*    */   }
/*    */ 
/*    */   public static String dump()
/*    */   {
/* 55 */     return "";
/*    */   }
/*    */ 
/*    */   public static void clear()
/*    */   {
/* 63 */     m_repository.clear();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84723 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.AppObjectRepository
 * JD-Core Version:    0.5.4
 */