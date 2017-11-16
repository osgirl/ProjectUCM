/*     */ package intradoc.io;
/*     */ 
/*     */ public class IdcRandomAccessByteArray
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public static final int ARRAY_COPY_THRESHOLD = 6;
/*     */   protected static final int ARRAY_FEATURES = 66051;
/*     */   public IdcRandomAccessByteArray m_parent;
/*     */   public int m_features;
/*     */   public byte[] m_bytes;
/*     */   public int m_offset;
/*     */   public int m_length;
/*     */   public int m_position;
/*     */ 
/*     */   public IdcRandomAccessByteArray(int initialSize)
/*     */   {
/*  43 */     this.m_features = 66307;
/*  44 */     this.m_bytes = new byte[initialSize];
/*  45 */     this.m_length = this.m_bytes.length;
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteArray(byte[] bytes)
/*     */   {
/*  50 */     this.m_features = 66307;
/*  51 */     this.m_bytes = bytes;
/*  52 */     this.m_length = bytes.length;
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteArray(byte[] bytes, int offset, int length)
/*     */   {
/*  57 */     if (offset + length > bytes.length)
/*     */     {
/*  59 */       throw new ArrayIndexOutOfBoundsException(offset + length);
/*     */     }
/*  61 */     this.m_features = 66051;
/*  62 */     this.m_bytes = bytes;
/*  63 */     this.m_offset = offset;
/*  64 */     this.m_length = length;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  70 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */     throws IdcByteHandlerException
/*     */   {
/*  76 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/*  78 */       throw new IdcByteHandlerException(65536);
/*     */     }
/*  80 */     IdcRandomAccessByteArray handler = new IdcRandomAccessByteArray(this.m_bytes, this.m_offset, this.m_length);
/*  81 */     handler.m_parent = this;
/*  82 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/*  83 */     return handler;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length) throws IdcByteHandlerException
/*     */   {
/*  88 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/*  90 */       throw new IdcByteHandlerException(65536);
/*     */     }
/*  92 */     if ((offset < -2147483648L) || (offset > 2147483647L) || (offset + this.m_offset < 0L) || (offset + this.m_offset > this.m_bytes.length))
/*     */     {
/*  95 */       throw new IdcByteHandlerException("syByteOffsetBad", new Object[] { Long.valueOf(offset) });
/*     */     }
/*  97 */     offset += this.m_offset;
/*  98 */     if (((length > 0L) && (length > this.m_bytes.length - offset)) || ((length <= 0L) && (-length > this.m_bytes.length - offset)))
/*     */     {
/* 100 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Long.valueOf(length) });
/*     */     }
/* 102 */     if (length <= 0L)
/*     */     {
/* 104 */       length += this.m_bytes.length - offset;
/*     */     }
/* 106 */     IdcRandomAccessByteArray handler = new IdcRandomAccessByteArray(this.m_bytes, (int)offset, (int)length);
/* 107 */     handler.m_parent = this;
/* 108 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/* 109 */     return handler;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 115 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/* 120 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size) throws IdcByteHandlerException
/*     */   {
/* 125 */     if ((this.m_features & 0x100) == 0)
/*     */     {
/* 127 */       throw new IdcByteHandlerException(256);
/*     */     }
/*     */ 
/* 134 */     if ((size < 0L) || (size > 2147483647L) || (size + this.m_offset > 2147483647L))
/*     */     {
/* 136 */       throw new IdcByteHandlerException("syByteSizeBad", new Object[] { Long.valueOf(size) });
/*     */     }
/* 138 */     int sz = (int)size;
/* 139 */     if ((sz != this.m_length) && 
/* 141 */       ((this.m_features & 0x10000000) == 0))
/*     */     {
/* 143 */       markIsDirty(true);
/*     */     }
/*     */ 
/* 146 */     if (size < this.m_length)
/*     */     {
/* 148 */       this.m_length = sz;
/* 149 */       if (this.m_position > this.m_length)
/*     */       {
/* 151 */         this.m_position = this.m_length;
/*     */       }
/* 153 */       return;
/*     */     }
/* 155 */     if (this.m_offset + sz > this.m_bytes.length)
/*     */     {
/* 158 */       byte[] newBytes = new byte[sz];
/* 159 */       System.arraycopy(this.m_bytes, this.m_offset, newBytes, 0, this.m_length);
/* 160 */       this.m_bytes = newBytes;
/* 161 */       this.m_offset = 0;
/* 162 */       this.m_length = sz;
/* 163 */       return;
/*     */     }
/*     */ 
/* 166 */     while (this.m_length < sz)
/*     */     {
/* 168 */       this.m_bytes[(this.m_offset + this.m_length++)] = 0;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setPosition(long position) throws IdcByteHandlerException
/*     */   {
/* 174 */     if ((this.m_features & 0x200) == 0)
/*     */     {
/* 176 */       throw new IdcByteHandlerException(512);
/*     */     }
/* 178 */     if ((position < 0L) || (position > this.m_length))
/*     */     {
/* 180 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 182 */     this.m_position = (int)position;
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 188 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 190 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 192 */     if (this.m_position + length > this.m_length)
/*     */     {
/* 194 */       length = this.m_length - this.m_position;
/*     */     }
/* 196 */     if (length < 6)
/*     */     {
/* 198 */       int len = length;
/* 199 */       while (len-- > 0)
/*     */       {
/* 201 */         dst[(dstoffset++)] = this.m_bytes[(this.m_offset + this.m_position++)];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 206 */       System.arraycopy(this.m_bytes, this.m_offset + this.m_position, dst, dstoffset, length);
/* 207 */       this.m_position += length;
/*     */     }
/* 209 */     return length;
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 214 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 216 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 218 */     if ((position < 0L) || (position > this.m_length))
/*     */     {
/* 220 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 222 */     int pos = (int)position;
/* 223 */     if (position + length > this.m_length)
/*     */     {
/* 225 */       length = this.m_length - pos;
/*     */     }
/* 227 */     if (length < 6)
/*     */     {
/* 229 */       int len = length;
/* 230 */       while (len-- > 0)
/*     */       {
/* 232 */         dst[(dstoffset++)] = this.m_bytes[(this.m_offset + pos++)];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 237 */       System.arraycopy(this.m_bytes, this.m_offset + pos, dst, dstoffset, length);
/*     */     }
/* 239 */     return length;
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 244 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 246 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 248 */     if (this.m_position + length > this.m_length)
/*     */     {
/* 250 */       setSize(this.m_position + length);
/*     */     }
/* 252 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 254 */       markIsDirty(true);
/*     */     }
/* 256 */     if (length < 6)
/*     */     {
/* 258 */       int len = length;
/* 259 */       while (len-- > 0)
/*     */       {
/* 261 */         this.m_bytes[(this.m_offset + this.m_position++)] = src[(srcoffset++)];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 266 */       System.arraycopy(src, srcoffset, this.m_bytes, this.m_offset + this.m_position, length);
/* 267 */       this.m_position += length;
/*     */     }
/* 269 */     return length;
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 274 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 276 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 278 */     if ((position < 0L) || (position > 2147483647L) || (position + this.m_length > 2147483647L))
/*     */     {
/* 280 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 282 */     if (position + length > this.m_length)
/*     */     {
/* 284 */       setSize(position + length);
/*     */     }
/* 286 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 288 */       markIsDirty(true);
/*     */     }
/* 290 */     int pos = (int)position;
/* 291 */     if (length < 6)
/*     */     {
/* 293 */       int len = length;
/* 294 */       while (len-- > 0)
/*     */       {
/* 296 */         this.m_bytes[(this.m_offset + pos++)] = src[(srcoffset++)];
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 301 */       System.arraycopy(src, srcoffset, this.m_bytes, this.m_offset + pos, length);
/*     */     }
/* 303 */     return length;
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 309 */     if (!isDirty)
/*     */     {
/* 311 */       this.m_features &= -268435457;
/* 312 */       return;
/*     */     }
/* 314 */     for (IdcRandomAccessByteArray kin = this; null != kin; kin = kin.m_parent)
/*     */     {
/* 316 */       if ((kin.m_features & 0x10000000) != 0) {
/*     */         return;
/*     */       }
/*     */ 
/* 320 */       kin.m_features |= 268435456;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 328 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89509 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcRandomAccessByteArray
 * JD-Core Version:    0.5.4
 */