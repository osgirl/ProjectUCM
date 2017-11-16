/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class EventOutputStream extends FilterOutputStream
/*     */ {
/*     */   protected OutputStream m_out;
/*     */   protected EventStreamHelper m_helper;
/*     */ 
/*     */   public EventOutputStream(OutputStream outStream)
/*     */   {
/*  31 */     super(outStream);
/*  32 */     this.m_out = outStream;
/*  33 */     this.m_helper = new EventStreamHelper(this);
/*     */   }
/*     */ 
/*     */   public void setTraceSection(String section)
/*     */   {
/*  38 */     this.m_helper.m_section = section;
/*     */   }
/*     */ 
/*     */   public void notifyFinished(IOException e)
/*     */   {
/*  43 */     this.m_helper.notifyFinished(e);
/*     */   }
/*     */ 
/*     */   public IOException getExternalException()
/*     */   {
/*  48 */     return this.m_helper.getExternalException();
/*     */   }
/*     */ 
/*     */   public void checkExternalException(int flags) throws IOException
/*     */   {
/*  53 */     this.m_helper.checkExternalException(null, flags);
/*     */   }
/*     */ 
/*     */   public int setFlags(int flags)
/*     */   {
/*  62 */     return this.m_helper.setFlags(flags);
/*     */   }
/*     */ 
/*     */   public int clearFlags(int flags)
/*     */   {
/*  71 */     return this.m_helper.clearFlags(flags);
/*     */   }
/*     */ 
/*     */   public OutputStream getUnderlyingStream()
/*     */   {
/*  76 */     return this.m_out;
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf)
/*     */     throws IOException
/*     */   {
/*  82 */     checkExternalException(0);
/*     */     try
/*     */     {
/*  85 */       this.m_out.write(buf);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  89 */       this.m_helper.checkExternalException(e, 0);
/*  90 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(byte[] buf, int start, int length)
/*     */     throws IOException
/*     */   {
/*  97 */     checkExternalException(0);
/*     */     try
/*     */     {
/* 100 */       this.m_out.write(buf, start, length);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 104 */       this.m_helper.checkExternalException(e, 0);
/* 105 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(int b)
/*     */     throws IOException
/*     */   {
/* 112 */     throw new AssertionError("Unwise use of OutputStream.write(int)");
/*     */   }
/*     */ 
/*     */   public void flush()
/*     */     throws IOException
/*     */   {
/* 118 */     checkExternalException(0);
/*     */     try
/*     */     {
/* 121 */       this.m_out.flush();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 125 */       this.m_helper.checkExternalException(e, 1);
/* 126 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 133 */     checkExternalException(0);
/*     */     try
/*     */     {
/* 136 */       super.close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 141 */       throw e;
/*     */     }
/*     */     finally
/*     */     {
/* 145 */       this.m_helper.close();
/*     */     }
/* 147 */     checkExternalException(1);
/*     */   }
/*     */ 
/*     */   public void addStreamEventHandler(StreamEventHandler h, Object data)
/*     */   {
/* 152 */     this.m_helper.addStreamEventHandler(h, data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 157 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90870 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EventOutputStream
 * JD-Core Version:    0.5.4
 */