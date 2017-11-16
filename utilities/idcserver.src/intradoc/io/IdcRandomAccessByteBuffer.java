/*     */ package intradoc.io;
/*     */ 
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.ReadOnlyBufferException;
/*     */ 
/*     */ public class IdcRandomAccessByteBuffer
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public IdcRandomAccessByteBuffer m_parent;
/*     */   public int m_features;
/*     */   public ByteBuffer m_buffer;
/*     */   public int m_offset;
/*     */   public int m_length;
/*     */   public int m_position;
/*     */ 
/*     */   public IdcRandomAccessByteBuffer(int length)
/*     */   {
/*  46 */     this.m_features = 66051;
/*  47 */     this.m_buffer = ByteBuffer.allocate(length);
/*  48 */     this.m_offset = 0;
/*  49 */     this.m_length = length;
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteBuffer(ByteBuffer buffer)
/*     */   {
/*  55 */     this.m_features = 66049;
/*     */ 
/*  57 */     this.m_buffer = buffer.duplicate();
/*     */ 
/*  59 */     buffer.clear();
/*  60 */     this.m_offset = 0;
/*  61 */     this.m_length = buffer.capacity();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteBuffer(ByteBuffer buffer, int offset, int length)
/*     */   {
/*  66 */     if (offset + length > buffer.capacity())
/*     */     {
/*  68 */       throw new IndexOutOfBoundsException(String.valueOf(offset + length));
/*     */     }
/*     */ 
/*  71 */     this.m_features = 66049;
/*     */ 
/*  73 */     this.m_buffer = buffer.duplicate();
/*     */ 
/*  75 */     buffer.clear();
/*  76 */     this.m_offset = offset;
/*  77 */     this.m_length = length;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  83 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */     throws IdcByteHandlerException
/*     */   {
/*  89 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/*  91 */       throw new IdcByteHandlerException(65536);
/*     */     }
/*  93 */     IdcRandomAccessByteBuffer handler = new IdcRandomAccessByteBuffer(this.m_buffer, this.m_offset, this.m_length);
/*  94 */     handler.m_parent = this;
/*  95 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/*  96 */     return handler;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 102 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/* 104 */       throw new IdcByteHandlerException(65536);
/*     */     }
/* 106 */     int capacity = this.m_buffer.capacity();
/* 107 */     if ((offset < -2147483648L) || (offset > 2147483647L) || (offset + this.m_offset < 0L) || (offset + this.m_offset > capacity))
/*     */     {
/* 110 */       throw new IdcByteHandlerException("syByteOffsetBad", new Object[] { Long.valueOf(offset) });
/*     */     }
/* 112 */     offset += this.m_offset;
/* 113 */     if ((length > 0L) || (length > capacity - offset) || ((length <= 0L) && (-length > capacity - offset)))
/*     */     {
/* 115 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Long.valueOf(length) });
/*     */     }
/* 117 */     if (length <= 0L)
/*     */     {
/* 119 */       length += capacity - offset;
/*     */     }
/* 121 */     IdcRandomAccessByteBuffer handler = new IdcRandomAccessByteBuffer(this.m_buffer, (int)offset, (int)length);
/* 122 */     handler.m_parent = this;
/* 123 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/* 124 */     return handler;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 130 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/* 135 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 143 */     throw new IdcByteHandlerException(256);
/*     */   }
/*     */ 
/*     */   public void setPosition(long position) throws IdcByteHandlerException
/*     */   {
/* 148 */     if ((this.m_features & 0x200) == 0)
/*     */     {
/* 150 */       throw new IdcByteHandlerException(512);
/*     */     }
/* 152 */     if ((position < 0L) || (position > this.m_length))
/*     */     {
/* 154 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 156 */     this.m_position = (int)position;
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 162 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 164 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 166 */     if (this.m_position + length > this.m_length)
/*     */     {
/* 168 */       length = this.m_length - this.m_position;
/*     */     }
/* 170 */     if (length <= 0)
/*     */     {
/* 172 */       return length;
/*     */     }
/* 174 */     synchronized (this.m_buffer)
/*     */     {
/* 176 */       this.m_buffer.position(this.m_offset + this.m_position);
/* 177 */       this.m_buffer.get(dst, dstoffset, length);
/*     */     }
/* 179 */     this.m_position += length;
/* 180 */     return length;
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 185 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 187 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 189 */     if ((position < 0L) || (position > this.m_length))
/*     */     {
/* 191 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 193 */     int pos = (int)position;
/* 194 */     if (position + length > this.m_length)
/*     */     {
/* 196 */       length = this.m_length - pos;
/*     */     }
/* 198 */     if (length <= 0)
/*     */     {
/* 200 */       return length;
/*     */     }
/* 202 */     synchronized (this.m_buffer)
/*     */     {
/* 204 */       this.m_buffer.position(this.m_offset + pos);
/* 205 */       this.m_buffer.get(dst, dstoffset, length);
/*     */     }
/* 207 */     return length;
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 212 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 214 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 216 */     if (this.m_position + length > this.m_length)
/*     */     {
/* 218 */       throw new IdcByteHandlerException(256);
/*     */     }
/* 220 */     if (length <= 0)
/*     */     {
/* 222 */       return length;
/*     */     }
/* 224 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 226 */       markIsDirty(true);
/*     */     }
/*     */     try
/*     */     {
/* 230 */       synchronized (this.m_buffer)
/*     */       {
/* 232 */         this.m_buffer.position(this.m_offset + this.m_position);
/* 233 */         this.m_buffer.put(src, srcoffset, length);
/*     */       }
/*     */     }
/*     */     catch (ReadOnlyBufferException e)
/*     */     {
/* 238 */       this.m_features &= -3;
/* 239 */       throw new IdcByteHandlerException(e, 2);
/*     */     }
/* 241 */     this.m_position += length;
/* 242 */     return length;
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 247 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 249 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 251 */     if ((position < 0L) || (position > 2147483647L) || (position + this.m_length > 2147483647L))
/*     */     {
/* 253 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 255 */     if (position + length > this.m_length)
/*     */     {
/* 257 */       throw new IdcByteHandlerException(256);
/*     */     }
/* 259 */     if (length <= 0)
/*     */     {
/* 261 */       return length;
/*     */     }
/* 263 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 265 */       markIsDirty(true);
/*     */     }
/*     */     try
/*     */     {
/* 269 */       synchronized (this.m_buffer)
/*     */       {
/* 271 */         this.m_buffer.position(this.m_offset + (int)position);
/* 272 */         this.m_buffer.put(src, srcoffset, length);
/*     */       }
/*     */     }
/*     */     catch (ReadOnlyBufferException e)
/*     */     {
/* 277 */       this.m_features &= -3;
/* 278 */       throw new IdcByteHandlerException(e, 2);
/*     */     }
/* 280 */     return length;
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 286 */     if (!isDirty)
/*     */     {
/* 288 */       this.m_features &= -268435457;
/* 289 */       return;
/*     */     }
/* 291 */     for (IdcRandomAccessByteBuffer kin = this; null != kin; kin = kin.m_parent)
/*     */     {
/* 293 */       if ((kin.m_features & 0x10000000) != 0) {
/*     */         return;
/*     */       }
/*     */ 
/* 297 */       kin.m_features |= 268435456;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 305 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89509 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcRandomAccessByteBuffer
 * JD-Core Version:    0.5.4
 */