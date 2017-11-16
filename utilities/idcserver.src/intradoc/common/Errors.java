/*     */ package intradoc.common;
/*     */ 
/*     */ public class Errors
/*     */ {
/*     */   public static final int SUCCESS = 0;
/*     */   public static final int FAILED = -1;
/*     */   public static final int COMM_ADDRESS_NOT_FOUND = -2;
/*     */   public static final int COMM_FAILED_CONNECT = -3;
/*     */   public static final int COMM_FAILED_SEND = -4;
/*     */   public static final int COMM_FAILED_RECEIVE = -5;
/*     */   public static final int COMM_TIMEOUT_RECEIVE = -6;
/*     */   public static final int RESOURCE_NOT_FOUND = -16;
/*     */   public static final int RESOURCE_EXISTS = -17;
/*     */   public static final int RESOURCE_CANNOT_ACCESS = -18;
/*     */   public static final int RESOURCE_READ_ONLY = -19;
/*     */   public static final int RESOURCE_INSUFFICIENT_PRIVILEGE = -20;
/*     */   public static final int RESOURCE_FAILED_LOGIN = -21;
/*     */   public static final int RESOURCE_LOCKED = -22;
/*     */   public static final int RESOURCE_WRONG_VERSION = -23;
/*     */   public static final int RESOURCE_WRONG_TYPE = -24;
/*     */   public static final int RESOURCE_UNAVAILABLE = -25;
/*     */   public static final int RESOURCE_MISCONFIGURED = -26;
/*     */   public static final int RESOURCE_NOT_DEFINED = -27;
/*     */   public static final int PROCESS_ERROR = -32;
/*     */   public static final int PROCESS_UNNECESSARY = -33;
/*     */   public static final int PROCESS_LOGIC_ERROR = -34;
/*     */   public static final int UNHANDLED_EXCEPTION = -48;
/*     */   public static final int INSUFFICIENT_MEMORY = -49;
/*     */   public static final int MISMATCHED_PARAMETERS = -50;
/*     */   public static final int ACTIVITY_ABORTED = -64;
/*     */   public static final int ACTIVITY_CANCELLED = -65;
/*     */   public static final int ACTIVITY_SUSPENDED = -66;
/*     */   public static final int ACTIVITY_WARNING_ABORT = -67;
/*     */   public static final int LICENSE_INVALID = -80;
/*     */   public static final int LICENSE_EXPIRED = -81;
/*     */   public static final int LICENSE_ACTION_NOT_ALLOWED = -82;
/*     */ 
/*     */   public static boolean isNormalUserOperationalErrorCode(int errorCode)
/*     */   {
/* 104 */     return (errorCode <= -64) && (errorCode >= -82);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 109 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 76308 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Errors
 * JD-Core Version:    0.5.4
 */