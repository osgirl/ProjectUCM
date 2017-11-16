/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class EventInputStream extends FilterInputStream
/*     */ {
/*     */   protected InputStream m_in;
/*     */   protected EventStreamHelper m_helper;
/*     */ 
/*     */   public EventInputStream(InputStream inStream)
/*     */   {
/*  31 */     super(inStream);
/*  32 */     this.m_in = inStream;
/*  33 */     this.m_helper = new EventStreamHelper(this);
/*     */   }
/*     */ 
/*     */   public void setTraceSection(String section)
/*     */   {
/*  38 */     this.m_helper.m_section = section;
/*     */   }
/*     */ 
/*     */   public int setFlags(int flags)
/*     */   {
/*  47 */     return this.m_helper.setFlags(flags);
/*     */   }
/*     */ 
/*     */   public int clearFlags(int flags)
/*     */   {
/*  56 */     return this.m_helper.clearFlags(flags);
/*     */   }
/*     */ 
/*     */   public void notifyFinished(IOException e)
/*     */   {
/*  61 */     this.m_helper.notifyFinished(e);
/*     */   }
/*     */ 
/*     */   public IOException getExternalException()
/*     */   {
/*  66 */     return this.m_helper.getExternalException();
/*     */   }
/*     */ 
/*     */   public void checkExternalException(int flags) throws IOException
/*     */   {
/*  71 */     this.m_helper.checkExternalException(null, flags);
/*     */   }
/*     */ 
/*     */   public InputStream getUnderlyingStream()
/*     */   {
/*  76 */     return this.m_in;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  82 */     throw new AssertionError("Unwise use of InputStream.read()");
/*     */   }
/*     */ 
/*     */   public int read(byte[] b)
/*     */     throws IOException
/*     */   {
/*  88 */     checkExternalException(0);
/*  89 */     int count = -1;
/*     */     try
/*     */     {
/*  92 */       count = this.m_in.read(b);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  96 */       this.m_helper.checkExternalException(e, 1);
/*  97 */       throw e;
/*     */     }
/*  99 */     if (count == -1)
/*     */     {
/* 101 */       checkExternalException(1);
/*     */     }
/* 103 */     return count;
/*     */   }
/*     */ 
/*     */   public int read(byte[] b, int off, int len)
/*     */     throws IOException
/*     */   {
/* 109 */     checkExternalException(0);
/* 110 */     int count = -1;
/*     */     try
/*     */     {
/* 113 */       count = this.m_in.read(b, off, len);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 117 */       this.m_helper.checkExternalException(e, 1);
/* 118 */       throw e;
/*     */     }
/* 120 */     if (count == -1)
/*     */     {
/* 122 */       checkExternalException(1);
/*     */     }
/* 124 */     return count;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 130 */     checkExternalException(1);
/*     */     try
/*     */     {
/* 133 */       super.close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 137 */       this.m_helper.checkExternalException(e, 1);
/*     */     }
/*     */     finally
/*     */     {
/* 141 */       this.m_helper.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addStreamEventHandler(StreamEventHandler h, Object data)
/*     */   {
/* 147 */     this.m_helper.addStreamEventHandler(h, data);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90870 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EventInputStream
 * JD-Core Version:    0.5.4
 */