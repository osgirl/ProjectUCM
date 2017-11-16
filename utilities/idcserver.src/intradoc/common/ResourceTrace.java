/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ResourceTrace
/*     */ {
/*  30 */   public static boolean m_traceResourceConflict = false;
/*  31 */   public static boolean m_traceResourceOverride = false;
/*  32 */   public static boolean m_traceResourceLoad = false;
/*     */ 
/*     */   public static void reset()
/*     */   {
/*  40 */     m_traceResourceConflict = false;
/*  41 */     m_traceResourceOverride = false;
/*  42 */     m_traceResourceLoad = false;
/*     */   }
/*     */ 
/*     */   public static void doMsg(String string)
/*     */   {
/*  50 */     Report.trace("componentloader", LocaleResources.localizeMessage(string, null), null);
/*     */   }
/*     */ 
/*     */   public static void msg(String string)
/*     */   {
/*  58 */     if (!m_traceResourceConflict)
/*     */       return;
/*  60 */     doMsg(string);
/*     */   }
/*     */ 
/*     */   public static void loadMsg(String key)
/*     */   {
/*  69 */     if (!m_traceResourceLoad)
/*     */       return;
/*  71 */     doMsg(LocaleUtils.encodeMessage("csComponentLoad", null, key));
/*     */   }
/*     */ 
/*     */   public static void overrideMsg(String string)
/*     */   {
/*  80 */     if ((!m_traceResourceOverride) && (!m_traceResourceLoad))
/*     */       return;
/*  82 */     doMsg(LocaleUtils.encodeMessage("csComponentLoadOverride", null, string));
/*     */   }
/*     */ 
/*     */   public static void conflictMsg(String key)
/*     */   {
/*  91 */     if ((!m_traceResourceConflict) && (!m_traceResourceOverride) && (!m_traceResourceLoad))
/*     */       return;
/*  93 */     doMsg(LocaleUtils.encodeMessage("csComponentLoadConflict", null, key));
/*     */   }
/*     */ 
/*     */   public static void doHashtableLoadAndLog(Map resources, Map overloadedResources, Object key, Object value, String keyDesc, boolean noOverload)
/*     */   {
/* 107 */     if (resources.put(key, value) != null)
/*     */     {
/* 109 */       if (overloadedResources.put(key, "") != null)
/*     */       {
/* 111 */         conflictMsg(keyDesc);
/*     */       }
/* 115 */       else if (noOverload)
/*     */       {
/* 117 */         conflictMsg(keyDesc);
/*     */       }
/*     */       else
/*     */       {
/* 121 */         overrideMsg(keyDesc);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 127 */       loadMsg(keyDesc);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ResourceTrace
 * JD-Core Version:    0.5.4
 */