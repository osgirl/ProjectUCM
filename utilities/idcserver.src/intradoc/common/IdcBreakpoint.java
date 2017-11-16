/*    */ package intradoc.common;
/*    */ 
/*    */ public class IdcBreakpoint
/*    */ {
/* 26 */   public static int GLOBAL = 0;
/* 27 */   public static int STEP_IN = 1;
/* 28 */   public static int STEP_OVER = 2;
/* 29 */   public static int STEP_OUT = 3;
/* 30 */   public static int STEP_RETURN = 4;
/* 31 */   public static int RUN_TO = 5;
/*    */ 
/* 33 */   public String m_threadName = null;
/* 34 */   public int m_type = -1;
/* 35 */   public int m_stackDepth = 0;
/* 36 */   public int m_refCounter = 0;
/*    */ 
/*    */   public IdcBreakpoint(String threadName, int type, int stackDepth)
/*    */   {
/* 40 */     this.m_threadName = threadName;
/* 41 */     this.m_type = type;
/* 42 */     this.m_stackDepth = stackDepth;
/* 43 */     this.m_refCounter = 1;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 48 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcBreakpoint
 * JD-Core Version:    0.5.4
 */