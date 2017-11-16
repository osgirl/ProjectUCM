/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import java.io.FilterOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class ProcessOutputStream extends FilterOutputStream
/*     */ {
/*  30 */   public Process m_process = null;
/*  31 */   public boolean m_isClosed = false;
/*     */ 
/*     */   public ProcessOutputStream(Process p)
/*     */   {
/*  35 */     super(p.getOutputStream());
/*  36 */     this.m_process = p;
/*  37 */     debug("Creating ProcessOutputStream Object");
/*     */   }
/*     */ 
/*     */   public ProcessOutputStream(OutputStream os, Process p)
/*     */   {
/*  42 */     super(os);
/*  43 */     this.m_process = p;
/*  44 */     debug("Creating ProcessOutputStream Object");
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  52 */       debug("Closing ProcessOutputStream");
/*  53 */       if (!this.m_isClosed)
/*     */       {
/*  55 */         super.close();
/*  56 */         this.m_isClosed = true;
/*     */       }
/*     */       else
/*     */       {
/*  60 */         debug("Redundantly closing ProcessOutputStream");
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*  65 */       closeProcess();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void closeProcess()
/*     */   {
/*  71 */     if (this.m_process == null)
/*     */       return;
/*  73 */     InputStream in = this.m_process.getErrorStream();
/*     */ 
/*  77 */     byte[] buf = new byte[256];
/*  78 */     int nread = 0;
/*     */     try
/*     */     {
/*  81 */       nread = in.read(buf);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  85 */       if (SystemUtils.m_verbose)
/*     */       {
/*  87 */         Report.debug("system", null, e);
/*     */       }
/*     */     }
/*  90 */     if (nread > 0)
/*     */     {
/*  92 */       String s = new String(buf, 0, nread);
/*  93 */       debug("Destroying ProcessOutputStream's process--" + s);
/*  94 */       this.m_process.destroy();
/*     */     }
/*  96 */     this.m_process = null;
/*     */   }
/*     */ 
/*     */   public void debug(String msg)
/*     */   {
/* 102 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 104 */     Report.debug("system", msg, null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 110 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ProcessOutputStream
 * JD-Core Version:    0.5.4
 */