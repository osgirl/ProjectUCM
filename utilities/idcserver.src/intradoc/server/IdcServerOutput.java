/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ForkedOutputStream;
/*     */ import intradoc.common.TruncatedOutputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.PrintStream;
/*     */ 
/*     */ public class IdcServerOutput
/*     */ {
/*  31 */   public static boolean m_replaceSystemStreams = false;
/*     */ 
/*  34 */   public static TruncatedOutputStream m_bytes = new TruncatedOutputStream(10000);
/*     */ 
/*  36 */   protected static ForkedOutputStream m_fork = null;
/*  37 */   protected static PrintStream m_output = null;
/*  38 */   protected static PrintStream m_originalOutput = null;
/*     */ 
/*  40 */   protected static ForkedOutputStream m_errFork = null;
/*  41 */   protected static PrintStream m_err = null;
/*  42 */   protected static PrintStream m_originalErr = null;
/*     */ 
/*     */   public static void init()
/*     */   {
/*  52 */     m_fork = new ForkedOutputStream(new OutputStream[] { m_bytes, System.out });
/*  53 */     m_output = new PrintStream(m_fork, true);
/*  54 */     m_originalOutput = System.out;
/*  55 */     intradoc.common.SystemUtils.m_out = m_output;
/*  56 */     intradoc.common.SystemUtils.m_captureOutStream = m_bytes;
/*     */ 
/*  60 */     if ((!EnvUtils.isHostedInAppServer()) && (m_replaceSystemStreams))
/*     */     {
/*  62 */       System.setOut(m_output);
/*     */     }
/*     */ 
/*  66 */     m_errFork = new ForkedOutputStream(new OutputStream[] { m_bytes, System.err });
/*  67 */     m_err = new PrintStream(m_errFork, true);
/*  68 */     m_originalErr = System.err;
/*  69 */     intradoc.common.SystemUtils.m_err = m_err;
/*  70 */     if ((EnvUtils.isHostedInAppServer()) || (!m_replaceSystemStreams))
/*     */       return;
/*  72 */     System.setErr(m_err);
/*     */   }
/*     */ 
/*     */   static PrintStream outReportStream()
/*     */   {
/*  83 */     return m_output;
/*     */   }
/*     */ 
/*     */   static PrintStream errReportStream()
/*     */   {
/*  93 */     return m_err;
/*     */   }
/*     */ 
/*     */   static PrintStream getOriginalOutputStream()
/*     */   {
/*  98 */     return m_originalOutput;
/*     */   }
/*     */ 
/*     */   static PrintStream getOriginalErrStream()
/*     */   {
/* 103 */     return m_originalErr;
/*     */   }
/*     */ 
/*     */   public static void clearOutput()
/*     */   {
/* 111 */     m_bytes.reset();
/*     */   }
/*     */ 
/*     */   public static String viewOutput()
/*     */   {
/* 119 */     String output = m_bytes.toString();
/* 120 */     return output;
/*     */   }
/*     */ 
/*     */   public static void resetBufferSize(int numLines)
/*     */   {
/* 128 */     m_bytes.setMaxLines(numLines);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73807 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcServerOutput
 * JD-Core Version:    0.5.4
 */