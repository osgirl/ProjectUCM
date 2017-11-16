/*    */ package intradoc.server.cache;
/*    */ 
/*    */ public class IdcCacheConstants
/*    */ {
/*    */   public static final String AUTOEXPIRY_TIME_PATTERN = "(\\d)+(MS|ms|S|s|M|m|H|h|D|d)?";
/*    */   public static final long SEC_TO_MSEC = 1000L;
/*    */   public static final long MIN_TO_MSEC = 60000L;
/*    */   public static final long HOUR_TO_MSEC = 3600000L;
/*    */   public static final long DAY_TO_MSEC = 86400000L;
/*    */   public static final String PERSISTENT_SERVICE_PREFIX = "ucm-persistent";
/*    */   public static final String NONPERSISTENT_SERVICE_PREFIX = "ucm-nonpersistent";
/*    */   public static final String AUTOEXPIRY_SERVICE_TAG = "autoexpiry";
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 50 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99324 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.cache.IdcCacheConstants
 * JD-Core Version:    0.5.4
 */