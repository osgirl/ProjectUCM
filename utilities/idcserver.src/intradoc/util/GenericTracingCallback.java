/*    */ package intradoc.util;
/*    */ 
/*    */ public abstract interface GenericTracingCallback
/*    */ {
/*    */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   public static final int FATAL = 0;
/*    */   public static final int ALERT = 1;
/*    */   public static final int ERROR = 3;
/*    */   public static final int WARNING = 4;
/*    */   public static final int NOTICE = 5;
/*    */   public static final int INFO = 6;
/*    */   public static final int DEBUG = 7;
/* 35 */   public static final String[] LEVEL_NAMES = { "Fatal", "Alert", "", "Error", "Warning", "Notice", "Info", "Debug" };
/*    */ 
/*    */   public abstract void report(int paramInt, Object[] paramArrayOfObject);
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.GenericTracingCallback
 * JD-Core Version:    0.5.4
 */