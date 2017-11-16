/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PipedInputStream;
/*     */ import java.io.PipedOutputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ 
/*     */ public class EncodingCheckWriter extends FilterWriter
/*     */ {
/*     */   protected String m_encoding;
/*     */   protected Reader m_reader;
/*     */   protected Writer m_writer;
/*     */   protected char[] m_buf;
/*  30 */   protected boolean m_isInitialized = false;
/*  31 */   protected boolean m_doFail = false;
/*     */ 
/*     */   public EncodingCheckWriter(Writer writer, String encoding, boolean doFail)
/*     */   {
/*  35 */     super(writer);
/*  36 */     this.m_encoding = encoding;
/*  37 */     this.m_buf = new char[1024];
/*  38 */     this.m_doFail = doFail;
/*     */   }
/*     */ 
/*     */   public void init() throws IOException
/*     */   {
/*  43 */     if (this.m_isInitialized)
/*     */       return;
/*  45 */     this.m_isInitialized = true;
/*  46 */     if (this.m_encoding == null)
/*     */     {
/*  48 */       PipedInputStream inP = new PipedInputStream();
/*  49 */       PipedOutputStream outP = new PipedOutputStream(inP);
/*  50 */       this.m_reader = new InputStreamReader(inP);
/*  51 */       this.m_writer = new OutputStreamWriter(outP);
/*     */     }
/*     */     else
/*     */     {
/*  55 */       PipedInputStream inP = new PipedInputStream();
/*  56 */       PipedOutputStream outP = new PipedOutputStream(inP);
/*  57 */       this.m_reader = new InputStreamReader(inP, this.m_encoding);
/*  58 */       this.m_writer = new OutputStreamWriter(outP, this.m_encoding);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(char[] cbuf, int off, int len)
/*     */     throws IOException
/*     */   {
/*  67 */     init();
/*  68 */     for (int i = off; i < len; ++i)
/*     */     {
/*  70 */       this.m_writer.write(cbuf[i]);
/*  71 */       this.m_writer.flush();
/*  72 */       int newChar = this.m_reader.read();
/*  73 */       if (newChar == cbuf[i])
/*     */         continue;
/*  75 */       reportFailure();
/*     */     }
/*     */ 
/*  79 */     super.write(cbuf, off, len);
/*     */   }
/*     */ 
/*     */   public void write(int c)
/*     */     throws IOException
/*     */   {
/*  85 */     init();
/*  86 */     this.m_writer.write(c);
/*  87 */     this.m_writer.flush();
/*  88 */     int c2 = this.m_reader.read();
/*  89 */     if (c2 != c)
/*     */     {
/*  91 */       reportFailure();
/*     */     }
/*  93 */     super.write(c);
/*     */   }
/*     */ 
/*     */   public void write(String str, int off, int len)
/*     */     throws IOException
/*     */   {
/* 100 */     init();
/* 101 */     for (int i = off; i < len; ++i)
/*     */     {
/* 103 */       char oldChar = str.charAt(i);
/* 104 */       this.m_writer.write(oldChar);
/* 105 */       this.m_writer.flush();
/* 106 */       int newChar = this.m_reader.read();
/* 107 */       if (newChar == oldChar)
/*     */         continue;
/* 109 */       reportFailure();
/*     */     }
/*     */ 
/* 113 */     super.write(str, off, len);
/*     */   }
/*     */ 
/*     */   protected void reportFailure()
/*     */     throws IOException
/*     */   {
/*     */     String msg;
/* 119 */     if (this.m_encoding == null)
/*     */     {
/* 121 */       msg = "syCharEncodingErrorDefault";
/*     */     }
/*     */     else
/*     */     {
/* 125 */       msg = "syCharEncodingError";
/*     */     }
/* 127 */     String msg = LocaleUtils.encodeMessage(msg, null, this.m_encoding);
/* 128 */     IOException e = new IOException(msg);
/* 129 */     Report.trace("encoding", null, e);
/* 130 */     if (!this.m_doFail)
/*     */       return;
/* 132 */     throw e;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 138 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EncodingCheckWriter
 * JD-Core Version:    0.5.4
 */