/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import java.util.zip.CRC32;
/*     */ 
/*     */ public class IdcZipCRCByteWrapper
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public int m_features;
/*     */   public IdcByteHandler m_handler;
/*     */   public CRC32 m_crc;
/*     */ 
/*     */   public IdcZipCRCByteWrapper(IdcByteHandler handler)
/*     */   {
/*  37 */     this.m_handler = handler;
/*  38 */     this.m_features = (handler.getSupportedFeatures() & 0xFFFEFDFF);
/*  39 */     this.m_crc = new CRC32();
/*     */   }
/*     */ 
/*     */   public int getValue()
/*     */   {
/*  47 */     return (int)this.m_crc.getValue();
/*     */   }
/*     */ 
/*     */   public void reset()
/*     */   {
/*  55 */     this.m_crc.reset();
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  61 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */     throws IdcByteHandlerException
/*     */   {
/*  69 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  77 */     throw new IdcByteHandlerException(65536);
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/*  82 */     return this.m_handler.getSize();
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/*  87 */     return this.m_handler.getPosition();
/*     */   }
/*     */ 
/*     */   public void setSize(long size) throws IdcByteHandlerException
/*     */   {
/*  92 */     this.m_handler.setSize(size);
/*     */   }
/*     */ 
/*     */   public void setPosition(long position)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 100 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 105 */     int ret = this.m_handler.readNext(dst, dstoffset, length);
/* 106 */     this.m_crc.update(dst, dstoffset, length);
/* 107 */     return ret;
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 115 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 120 */     int ret = this.m_handler.writeNext(src, srcoffset, length);
/* 121 */     this.m_crc.update(src, srcoffset, length);
/* 122 */     return ret;
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 130 */     throw new IdcByteHandlerException(512);
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 136 */     this.m_handler.markIsDirty(isDirty);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 143 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78418 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipCRCByteWrapper
 * JD-Core Version:    0.5.4
 */