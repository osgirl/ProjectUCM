/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Hashtable;
/*     */ 
/*     */ public class IdcBreakpointManager
/*     */ {
/*  26 */   public static Hashtable m_threadMap = new Hashtable();
/*  27 */   public static Hashtable m_stepMap = new Hashtable();
/*  28 */   public static boolean m_isStepping = false;
/*  29 */   public static boolean m_enableDebug = false;
/*     */ 
/*  31 */   protected static String m_syncObject = "BreakpointManager";
/*     */ 
/*     */   public static void setEnableDebugging(boolean enableDebug)
/*     */   {
/*  35 */     m_enableDebug = enableDebug;
/*     */   }
/*     */ 
/*     */   public static IdcBreakpoint register(String threadName, DynamicHtmlMerger htmlMerger)
/*     */   {
/*  40 */     if (!m_enableDebug)
/*     */     {
/*  42 */       return null;
/*     */     }
/*     */ 
/*  45 */     Object obj = m_threadMap.put(threadName, htmlMerger);
/*     */     IdcBreakpoint bp;
/*     */     IdcBreakpoint bp;
/*  47 */     if (obj != null)
/*     */     {
/*  50 */       DynamicHtmlMerger dynMerger = (DynamicHtmlMerger)obj;
/*  51 */       dynMerger.m_idcBreakpoint.m_refCounter += 1;
/*  52 */       bp = dynMerger.m_idcBreakpoint;
/*     */     }
/*     */     else
/*     */     {
/*  56 */       bp = new IdcBreakpoint(threadName, -1, htmlMerger.m_evalNestingLevel);
/*  57 */       m_threadMap.put(threadName, htmlMerger);
/*     */     }
/*  59 */     return bp;
/*     */   }
/*     */ 
/*     */   public static void unregister(String threadName)
/*     */   {
/*  64 */     if (!m_enableDebug)
/*     */     {
/*  66 */       return;
/*     */     }
/*  68 */     synchronized (m_syncObject)
/*     */     {
/*  70 */       DynamicHtmlMerger dynMerger = (DynamicHtmlMerger)m_threadMap.get(threadName);
/*  71 */       if (dynMerger != null)
/*     */       {
/*  73 */         dynMerger.m_idcBreakpoint.m_refCounter -= 1;
/*  74 */         if (dynMerger.m_idcBreakpoint.m_refCounter == 0)
/*     */         {
/*  76 */           m_threadMap.remove(threadName);
/*  77 */           m_stepMap.remove(threadName);
/*  78 */           m_isStepping = !m_stepMap.isEmpty();
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void enableStepping(String threadName, DynamicHtmlMerger merger)
/*     */   {
/*  86 */     synchronized (m_syncObject)
/*     */     {
/*  88 */       m_stepMap.put(threadName, merger);
/*  89 */       m_isStepping = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void removeThreadBreakpoint(IdcBreakpoint bp)
/*     */   {
/*  95 */     synchronized (m_syncObject)
/*     */     {
/*  97 */       if ((bp != null) && (bp.m_threadName != null))
/*     */       {
/*  99 */         m_stepMap.remove(bp.m_threadName);
/* 100 */         m_isStepping = !m_stepMap.isEmpty();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void clearThreadBreakpoints()
/*     */   {
/* 107 */     synchronized (m_syncObject)
/*     */     {
/* 109 */       m_stepMap.clear();
/* 110 */       m_isStepping = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 116 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcBreakpointManager
 * JD-Core Version:    0.5.4
 */