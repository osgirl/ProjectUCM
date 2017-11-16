/*     */ package intradoc.io;
/*     */ 
/*     */ public class IdcNullByteHandler
/*     */   implements IdcByteHandler
/*     */ {
/*     */   public IdcNullByteHandler m_parent;
/*     */   public long m_offset;
/*     */   public long m_length;
/*     */   public long m_position;
/*     */ 
/*     */   public IdcNullByteHandler()
/*     */   {
/*     */   }
/*     */ 
/*     */   public IdcNullByteHandler(long initialSize)
/*     */   {
/*  40 */     this.m_length = initialSize;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  46 */     return 66307;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone()
/*     */   {
/*  52 */     IdcNullByteHandler handler = new IdcNullByteHandler(this.m_length);
/*  53 */     handler.m_offset = this.m_offset;
/*  54 */     handler.m_position = this.m_position;
/*  55 */     handler.m_parent = this;
/*  56 */     return handler;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length) throws IdcByteHandlerException
/*     */   {
/*  61 */     if ((offset < 0L) || (offset + this.m_offset > this.m_length))
/*     */     {
/*  63 */       throw new IdcByteHandlerException("syByteOffsetBad", new Object[] { Long.valueOf(offset) });
/*     */     }
/*  65 */     if (length > this.m_length)
/*     */     {
/*  67 */       this.m_length = length;
/*     */     }
/*  69 */     offset += this.m_offset;
/*  70 */     if (length < 0L)
/*     */     {
/*  72 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Long.valueOf(length) });
/*     */     }
/*  74 */     IdcNullByteHandler handler = new IdcNullByteHandler(this.m_length);
/*  75 */     handler.m_offset = offset;
/*  76 */     handler.m_position = this.m_position;
/*  77 */     handler.m_parent = this;
/*  78 */     return handler;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/*  83 */     return this.m_length;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/*  88 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size) throws IdcByteHandlerException
/*     */   {
/*  93 */     if (size < 0L)
/*     */     {
/*  95 */       throw new IdcByteHandlerException("syByteSizeBad", new Object[] { Long.valueOf(size) });
/*     */     }
/*  97 */     this.m_length = size;
/*     */   }
/*     */ 
/*     */   public void setPosition(long position) throws IdcByteHandlerException
/*     */   {
/* 102 */     if (position < 0L)
/*     */     {
/* 104 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 106 */     this.m_position = position;
/* 107 */     if (position <= this.m_length)
/*     */       return;
/* 109 */     this.m_length = position;
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length)
/*     */   {
/* 115 */     return 0;
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 120 */     if (position < 0L)
/*     */     {
/* 122 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 124 */     if (length < 0)
/*     */     {
/* 126 */       length = 0;
/*     */     }
/* 128 */     if (position + length > this.m_length)
/*     */     {
/* 130 */       this.m_length = position;
/*     */     }
/* 132 */     return 0;
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length)
/*     */   {
/* 137 */     if (length < 0)
/*     */     {
/* 139 */       length = 0;
/*     */     }
/* 141 */     this.m_position += length;
/* 142 */     if (this.m_position > this.m_length)
/*     */     {
/* 144 */       this.m_length = this.m_position;
/*     */     }
/* 146 */     return length;
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 151 */     if (position < 0L)
/*     */     {
/* 153 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 155 */     if (length < 0)
/*     */     {
/* 157 */       length = 0;
/*     */     }
/* 159 */     this.m_position += length;
/* 160 */     if (this.m_position > this.m_length)
/*     */     {
/* 162 */       this.m_length = this.m_position;
/*     */     }
/* 164 */     return length;
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 177 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71995 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcNullByteHandler
 * JD-Core Version:    0.5.4
 */