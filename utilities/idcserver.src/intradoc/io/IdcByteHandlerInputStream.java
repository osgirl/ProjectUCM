/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class IdcByteHandlerInputStream extends InputStream
/*     */ {
/*     */   public IdcByteHandler m_handler;
/*     */   byte[] m_buffer;
/*     */ 
/*     */   public IdcByteHandlerInputStream(IdcByteHandler handler)
/*     */   {
/*  35 */     this.m_handler = handler;
/*  36 */     this.m_buffer = new byte[1];
/*     */   }
/*     */ 
/*     */   public int read()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  45 */       int length = this.m_handler.readNext(this.m_buffer, 0, 1);
/*  46 */       if (length < 1)
/*     */       {
/*  48 */         return -1;
/*     */       }
/*  50 */       return this.m_buffer[0] & 0xFF;
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/*  54 */       IOException io = new IOException();
/*  55 */       io.initCause(e);
/*  56 */       throw io;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int read(byte[] bytes, int off, int len)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  65 */       int numBytes = this.m_handler.readNext(bytes, off, len);
/*  66 */       return (numBytes > 0) ? numBytes : -1;
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/*  70 */       IOException io = new IOException();
/*  71 */       io.initCause(e);
/*  72 */       throw io;
/*     */     }
/*     */   }
/*     */ 
/*     */   public long skip(long n)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/*  81 */       long size = this.m_handler.getSize();
/*  82 */       long pos = this.m_handler.getPosition();
/*  83 */       if (n > size - pos)
/*     */       {
/*  85 */         n = size - pos;
/*     */       }
/*  87 */       if (pos + n < 0L)
/*     */       {
/*  89 */         n = 9223372036854775807L - pos;
/*     */       }
/*  91 */       this.m_handler.setPosition(pos + n);
/*  92 */       return n;
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/*  96 */       IOException io = new IOException();
/*  97 */       io.initCause(e);
/*  98 */       throw io;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int available()
/*     */   {
/* 105 */     long size = this.m_handler.getSize();
/* 106 */     long pos = this.m_handler.getPosition();
/* 107 */     if (size - pos > 2147483647L)
/*     */     {
/* 109 */       return 2147483647;
/*     */     }
/* 111 */     return (int)(size - pos);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 117 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71526 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcByteHandlerInputStream
 * JD-Core Version:    0.5.4
 */