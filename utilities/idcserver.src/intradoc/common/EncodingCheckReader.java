/*     */ package intradoc.common;
/*     */ 
/*     */ import java.io.FilterReader;
/*     */ import java.io.IOException;
/*     */ import java.io.Reader;
/*     */ 
/*     */ public class EncodingCheckReader extends FilterReader
/*     */ {
/*     */   protected int m_flags;
/*  27 */   protected boolean m_isEOF = false;
/*     */   protected String m_encoding;
/*     */ 
/*     */   public EncodingCheckReader(Reader reader, String encoding, int flags)
/*     */   {
/*  33 */     super(reader);
/*  34 */     this.m_encoding = encoding;
/*  35 */     this.m_flags = flags;
/*     */   }
/*     */ 
/*     */   public static boolean areFlagsInteresting(int flags)
/*     */   {
/*  42 */     return flags != 0;
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*  50 */     if (this.m_isEOF)
/*     */     {
/*  52 */       return -1;
/*     */     }
/*  54 */     int rc = super.read();
/*  55 */     if (rc == 65533)
/*     */     {
/*  57 */       reportFFFD();
/*  58 */       if ((this.m_flags & 0x8) != 0)
/*     */       {
/*  60 */         this.m_isEOF = true;
/*  61 */         return -1;
/*     */       }
/*     */     }
/*  64 */     return rc;
/*     */   }
/*     */ 
/*     */   public int read(char[] buffer, int offset, int length)
/*     */     throws IOException
/*     */   {
/*  71 */     if (this.m_isEOF)
/*     */     {
/*  73 */       return -1;
/*     */     }
/*  75 */     int rc = super.read(buffer, offset, length);
/*  76 */     if (this.m_flags != 0)
/*     */     {
/*  78 */       int stopAt = offset + rc;
/*  79 */       for (int i = offset; i < stopAt; ++i)
/*     */       {
/*  81 */         if (buffer[i] != 65533)
/*     */           continue;
/*  83 */         reportFFFD();
/*  84 */         if ((this.m_flags & 0x8) == 0)
/*     */           continue;
/*  86 */         this.m_isEOF = true;
/*  87 */         rc = i - offset;
/*  88 */         return rc;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  93 */     return rc;
/*     */   }
/*     */ 
/*     */   protected void reportFFFD()
/*     */     throws IOException
/*     */   {
/*     */     String msg;
/*     */     String msg;
/*  99 */     if (this.m_encoding != null)
/*     */     {
/* 101 */       msg = LocaleUtils.encodeMessage("syCharEncodingError", null, this.m_encoding);
/*     */     }
/*     */     else
/*     */     {
/* 106 */       msg = LocaleUtils.encodeMessage("syCharEncodingErrorDefault", null);
/*     */     }
/*     */ 
/* 109 */     IOException e = new IOException(msg);
/* 110 */     if ((this.m_flags & 0x1) != 0)
/*     */     {
/* 112 */       Report.trace("encoding", null, e);
/*     */     }
/* 114 */     if ((this.m_flags & 0x2) == 0)
/*     */       return;
/* 116 */     throw e;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 122 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.EncodingCheckReader
 * JD-Core Version:    0.5.4
 */