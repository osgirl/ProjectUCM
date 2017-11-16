/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class InputStreamIdcByteHandler
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public int m_features;
/*     */   public InputStream m_stream;
/*     */   public long m_length;
/*     */   public long m_position;
/*     */ 
/*     */   public InputStreamIdcByteHandler(InputStream stream, long length)
/*     */   {
/*  38 */     this.m_features = 1;
/*  39 */     this.m_stream = stream;
/*  40 */     this.m_length = length;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  46 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */     throws IdcByteHandlerException
/*     */   {
/*  55 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  63 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/*  69 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/*  74 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  82 */     throw new IdcByteHandlerException(256);
/*     */   }
/*     */ 
/*     */   public void setPosition(long position)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  90 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  96 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/*  98 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 100 */     if (length <= 0)
/*     */     {
/* 102 */       return length;
/*     */     }
/*     */     try
/*     */     {
/* 106 */       int numBytes = this.m_stream.read(dst, dstoffset, length);
/* 107 */       this.m_position += numBytes;
/* 108 */       return numBytes;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 112 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int len)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 121 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 129 */     throw new IdcByteHandlerException(2);
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 137 */     throw new IdcByteHandlerException(514);
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 149 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.InputStreamIdcByteHandler
 * JD-Core Version:    0.5.4
 */