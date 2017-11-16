/*    */ package intradoc.common;
/*    */ 
/*    */ import intradoc.util.IdcMessageUtils;
/*    */ import java.io.PrintWriter;
/*    */ 
/*    */ public class TraceUtils
/*    */ {
/* 31 */   public static boolean m_createShortedParentStack = false;
/*    */ 
/* 36 */   public static volatile boolean m_haveSetUniversalParent = false;
/*    */ 
/* 40 */   public static StackTraceElement[] m_universalParent = null;
/*    */ 
/*    */   public static void printStackTrace(Throwable t, String prefix, PrintWriter w)
/*    */   {
/* 49 */     IdcMessageUtils.printStackTrace(m_universalParent, t, prefix, "", w);
/*    */   }
/*    */ 
/*    */   public static void setUniversalStackTraceParent(Throwable t)
/*    */   {
/* 57 */     StackTraceElement[] temp = t.getStackTrace();
/*    */ 
/* 60 */     int numElementsToKeep = 2;
/* 61 */     StackTraceElement[] shortedStack = null;
/* 62 */     if ((m_createShortedParentStack) && (numElementsToKeep < temp.length))
/*    */     {
/* 64 */       shortedStack = new StackTraceElement[numElementsToKeep];
/* 65 */       for (int i = 0; i < shortedStack.length; ++i)
/*    */       {
/* 67 */         shortedStack[i] = temp[i];
/*    */       }
/*    */     }
/*    */     else
/*    */     {
/* 72 */       shortedStack = temp;
/*    */     }
/* 74 */     m_universalParent = shortedStack;
/* 75 */     m_haveSetUniversalParent = true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 80 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82069 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.TraceUtils
 * JD-Core Version:    0.5.4
 */