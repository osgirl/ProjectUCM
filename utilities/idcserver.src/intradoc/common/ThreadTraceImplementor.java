/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class ThreadTraceImplementor extends DefaultTraceImplementor
/*     */ {
/*  28 */   public static String m_threadPrefix = "IdcServer";
/*  29 */   protected static int m_maxBuffer = 2097152;
/*     */ 
/*  31 */   public static boolean m_isInitialized = false;
/*     */ 
/*  33 */   public static ThreadLocal m_stringBuilderHolder = new ThreadLocal()
/*     */   {
/*     */     protected synchronized Object initialValue()
/*     */     {
/*  37 */       IdcStringBuilder buf = new IdcStringBuilder();
/*  38 */       buf.m_disableToStringReleaseBuffers = true;
/*  39 */       return buf;
/*     */     }
/*     */ 
/*     */     public Object get()
/*     */     {
/*  45 */       IdcStringBuilder buf = (IdcStringBuilder)super.get();
/*  46 */       if (buf.length() > ThreadTraceImplementor.m_maxBuffer)
/*     */       {
/*  48 */         char[] origBuf = buf.m_charArray;
/*  49 */         int newSize = ThreadTraceImplementor.m_maxBuffer / 2;
/*  50 */         buf.truncate(newSize);
/*  51 */         buf.append(origBuf, origBuf.length - newSize, newSize);
/*     */       }
/*  53 */       return buf;
/*     */     }
/*  33 */   };
/*     */ 
/*     */   public static void resetTracingBuilder()
/*     */   {
/*  59 */     IdcStringBuilder builder = (IdcStringBuilder)m_stringBuilderHolder.get();
/*  60 */     builder.truncate(0);
/*     */   }
/*     */ 
/*     */   public void init(Map settings)
/*     */   {
/*  66 */     this.m_traceParameters = new DefaultTraceParameters();
/*  67 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public void configureTrace(List flags, Map env)
/*     */   {
/*  73 */     String prefix = (String)env.get("TraceThreadPrefix");
/*  74 */     if ((prefix == null) || (prefix.length() <= 0))
/*     */       return;
/*  76 */     m_threadPrefix = prefix;
/*     */   }
/*     */ 
/*     */   public void traceWithOptions(TraceSection section, int level, String message, Date d, Map params, boolean isPrepend)
/*     */   {
/*  85 */     if (!shouldOutput(section, message))
/*     */       return;
/*  87 */     IdcStringBuilder buf = (IdcStringBuilder)m_stringBuilderHolder.get();
/*  88 */     StringBufferOutputStream sbos = new StringBufferOutputStream(buf);
/*  89 */     appendTraceMessage(sbos, section, level, message, d, isPrepend);
/*     */   }
/*     */ 
/*     */   public boolean shouldOutput(TraceSection section, String message)
/*     */   {
/*  97 */     if (shouldOutputThread())
/*     */     {
/*  99 */       return super.shouldOutput(section, message);
/*     */     }
/* 101 */     return false;
/*     */   }
/*     */ 
/*     */   public void outln(char[] message, int start, int length)
/*     */   {
/* 107 */     if (!shouldOutputThread())
/*     */       return;
/* 109 */     IdcStringBuilder buf = (IdcStringBuilder)m_stringBuilderHolder.get();
/* 110 */     buf.append(message, start, length);
/* 111 */     buf.append('\n');
/*     */   }
/*     */ 
/*     */   public void outln(String message)
/*     */   {
/* 118 */     if (!shouldOutputThread())
/*     */       return;
/* 120 */     IdcStringBuilder buf = (IdcStringBuilder)m_stringBuilderHolder.get();
/* 121 */     buf.append(message);
/* 122 */     buf.append('\n');
/*     */   }
/*     */ 
/*     */   public void out(char[] message, int start, int length)
/*     */   {
/* 129 */     if (!shouldOutputThread())
/*     */       return;
/* 131 */     IdcStringBuilder buf = (IdcStringBuilder)m_stringBuilderHolder.get();
/* 132 */     buf.append(message, start, length);
/*     */   }
/*     */ 
/*     */   public void out(String message)
/*     */   {
/* 139 */     if (!shouldOutputThread())
/*     */       return;
/* 141 */     IdcStringBuilder buf = (IdcStringBuilder)m_stringBuilderHolder.get();
/* 142 */     buf.append(message);
/*     */   }
/*     */ 
/*     */   public boolean shouldOutputThread()
/*     */   {
/* 148 */     return getThreadID().startsWith(m_threadPrefix);
/*     */   }
/*     */ 
/*     */   public String getThreadID()
/*     */   {
/* 153 */     return SystemUtils.getCurrentReportingThreadID(0);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 158 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.ThreadTraceImplementor
 * JD-Core Version:    0.5.4
 */