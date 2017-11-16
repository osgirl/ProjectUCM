/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class OutputStreamIdcByteHandler
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public int m_features;
/*     */   public OutputStream m_stream;
/*     */   public long m_position;
/*     */ 
/*     */   public OutputStreamIdcByteHandler(OutputStream stream)
/*     */   {
/*  37 */     this.m_features = 2;
/*  38 */     this.m_stream = stream;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  44 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */     throws IdcByteHandlerException
/*     */   {
/*  53 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  61 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/*  67 */     return 0L;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/*  72 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  80 */     throw new IdcByteHandlerException(256);
/*     */   }
/*     */ 
/*     */   public void setPosition(long position)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  88 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int offset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  97 */     throw new IdcByteHandlerException(1);
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int len)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 105 */     throw new IdcByteHandlerException(513);
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 110 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 112 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 114 */     if (length <= 0)
/*     */     {
/* 116 */       return length;
/*     */     }
/*     */     try
/*     */     {
/* 120 */       this.m_stream.write(src, srcoffset, length);
/* 121 */       this.m_position += length;
/* 122 */       return length;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 126 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 135 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 148 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.OutputStreamIdcByteHandler
 * JD-Core Version:    0.5.4
 */