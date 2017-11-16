/*     */ package intradoc.util;
/*     */ 
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class SimpleTracingCallback
/*     */   implements GenericTracingCallback
/*     */ {
/*     */   public String m_prefix;
/*     */   public IdcAppendableFactory m_factory;
/*     */   public PrintStream m_output;
/*     */ 
/*     */   public SimpleTracingCallback()
/*     */   {
/*  31 */     this.m_output = System.err;
/*     */   }
/*     */ 
/*     */   public SimpleTracingCallback(String prefix)
/*     */   {
/*  37 */     this.m_prefix = prefix;
/*     */   }
/*     */ 
/*     */   public void report(int level, Object[] args)
/*     */   {
/*  43 */     if (this.m_output == null)
/*     */     {
/*  45 */       return;
/*     */     }
/*     */ 
/*  48 */     IdcAppendableBase a = null;
/*  49 */     if (this.m_factory != null)
/*     */     {
/*  51 */       a = this.m_factory.getIdcAppendable(this, 0);
/*     */     }
/*  53 */     if (a == null)
/*     */     {
/*  56 */       IdcMessageUtils.init();
/*     */ 
/*  58 */       a = IdcMessageUtils.m_defaultFactory.getIdcAppendable(this, 0);
/*     */     }
/*     */ 
/*  61 */     a.append(LEVEL_NAMES[level].toUpperCase());
/*  62 */     a.append(": ");
/*  63 */     for (Object arg : args)
/*     */     {
/*  65 */       if (arg == null)
/*     */       {
/*  67 */         new AssertionError("Null argument passed to report().").printStackTrace();
/*     */       }
/*  69 */       if (arg instanceof Throwable)
/*     */       {
/*  71 */         appendThrowable(a, (Throwable)arg);
/*     */       }
/*  73 */       else if (arg instanceof String)
/*     */       {
/*  75 */         a.append((String)arg);
/*     */       }
/*     */       else
/*     */       {
/*  79 */         String tmp = arg.toString();
/*  80 */         if (tmp == null)
/*     */         {
/*  82 */           new AssertionError("Object of type " + arg.getClass().getName() + " returned null from toString().").printStackTrace();
/*     */         }
/*     */ 
/*  85 */         a.append(arg.toString());
/*     */       }
/*     */     }
/*  88 */     this.m_output.println(a.toString());
/*     */   }
/*     */ 
/*     */   public static void appendThrowable(IdcAppendableBase appendable, Throwable t)
/*     */   {
/* 101 */     appendable.append(t.toString());
/* 102 */     StackTraceElement[] trace = t.getStackTrace();
/* 103 */     for (int i = 0; i < trace.length; ++i)
/*     */     {
/* 105 */       appendable.append("\n\tat ");
/* 106 */       appendable.append(trace[i].toString());
/*     */     }
/* 108 */     Throwable cause = t.getCause();
/* 109 */     if (null != cause)
/*     */     {
/* 111 */       appendThrowableAsCause(appendable, cause, trace);
/*     */     }
/* 113 */     appendable.append('\n');
/*     */   }
/*     */ 
/*     */   protected static void appendThrowableAsCause(IdcAppendableBase appendable, Throwable t, StackTraceElement[] causedTrace)
/*     */   {
/* 126 */     StackTraceElement[] trace = t.getStackTrace();
/* 127 */     int m = trace.length - 1; int n = causedTrace.length - 1;
/* 128 */     while ((m >= 0) && (n >= 0) && (trace[m].equals(causedTrace[n])))
/*     */     {
/* 130 */       --m; --n;
/*     */     }
/* 132 */     int framesInCommon = trace.length - 1 - m;
/*     */ 
/* 134 */     appendable.append("\nCaused by: ");
/* 135 */     appendable.append(t.toString());
/* 136 */     for (int i = 0; i <= m; ++i)
/*     */     {
/* 138 */       appendable.append("\n\tat ");
/* 139 */       appendable.append(trace[i].toString());
/*     */     }
/* 141 */     if (0 != framesInCommon)
/*     */     {
/* 143 */       appendable.append("\n\t... ");
/* 144 */       appendable.append("" + framesInCommon);
/* 145 */       appendable.append(" more");
/*     */     }
/*     */ 
/* 148 */     Throwable cause = t.getCause();
/* 149 */     if (null == cause)
/*     */       return;
/* 151 */     appendThrowableAsCause(appendable, cause, trace);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 159 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84795 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.util.SimpleTracingCallback
 * JD-Core Version:    0.5.4
 */