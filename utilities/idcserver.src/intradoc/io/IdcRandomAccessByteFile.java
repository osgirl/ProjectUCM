/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.nio.ByteBuffer;
/*     */ import java.nio.channels.FileChannel;
/*     */ 
/*     */ public class IdcRandomAccessByteFile
/*     */   implements Closeable, IdcByteHandler
/*     */ {
/*     */   public static final int F_READ_ONLY = 1;
/*     */   public static final int F_SYNC = 2;
/*     */   public static final int F_PRELOAD = 1048576;
/*  47 */   public static int MIN_BLOCK_SIZE = 65536;
/*     */   public IdcRandomAccessByteFile m_parent;
/*     */   public int m_features;
/*     */   public int m_flags;
/*     */   public File m_file;
/*     */   public RandomAccessFile m_access;
/*     */   public FileChannel m_channel;
/*     */   public long m_fileLength;
/*     */   public ByteBuffer m_buffer;
/*     */   public long m_bufferOffset;
/*     */   public int m_bufferSize;
/*     */   public long m_offset;
/*     */   public long m_length;
/*     */   public long m_position;
/*     */ 
/*     */   public IdcRandomAccessByteFile(String pathname)
/*     */   {
/*  91 */     this.m_file = new File(pathname);
/*  92 */     initFeatures();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteFile(String pathname, int flags)
/*     */   {
/*  97 */     this.m_file = new File(pathname);
/*  98 */     this.m_flags = flags;
/*  99 */     initFeatures();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteFile(File file)
/*     */   {
/* 104 */     this.m_file = file;
/* 105 */     initFeatures();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteFile(File file, int flags)
/*     */   {
/* 110 */     this.m_file = file;
/* 111 */     this.m_flags = flags;
/* 112 */     initFeatures();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteFile(RandomAccessFile access)
/*     */   {
/* 122 */     this.m_access = access;
/* 123 */     initFeatures();
/*     */   }
/*     */ 
/*     */   public IdcRandomAccessByteFile(RandomAccessFile access, int flags)
/*     */   {
/* 134 */     this.m_access = access;
/* 135 */     this.m_flags = flags;
/* 136 */     initFeatures();
/*     */   }
/*     */ 
/*     */   protected void initFeatures()
/*     */   {
/* 143 */     this.m_features = 66048;
/* 144 */     boolean exists = (null != this.m_file) && (this.m_file.exists());
/* 145 */     if (((null != this.m_file) && (((!exists) || (this.m_file.canRead())))) || (null != this.m_access))
/*     */     {
/* 147 */       this.m_features |= 1;
/*     */     }
/* 149 */     if (((this.m_flags & 0x1) == 0) && ((
/* 152 */       ((null != this.m_file) && (((!exists) || (this.m_file.canWrite())))) || (null != this.m_access))))
/*     */     {
/* 154 */       this.m_features |= 2;
/*     */     }
/*     */ 
/* 159 */     this.m_features |= 256;
/*     */   }
/*     */ 
/*     */   protected void ensureOpenChannel() throws IdcByteHandlerException
/*     */   {
/*     */     try
/*     */     {
/* 166 */       if ((null == this.m_access) && (null != this.m_file))
/*     */       {
/* 169 */         String mode = ((this.m_flags & 0x2) != 0) ? "rws" : ((this.m_flags & 0x1) != 0) ? "r" : "rw";
/*     */ 
/* 171 */         this.m_access = new RandomAccessFile(this.m_file, mode);
/* 172 */         this.m_channel = null;
/*     */       }
/* 174 */       if (null == this.m_channel)
/*     */       {
/* 176 */         this.m_channel = this.m_access.getChannel();
/*     */ 
/* 178 */         this.m_buffer = null;
/* 179 */         this.m_bufferOffset = 0L;
/* 180 */         this.m_bufferSize = 0;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 185 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws IdcByteHandlerException
/*     */   {
/* 192 */     ensureOpenChannel();
/*     */     try
/*     */     {
/* 195 */       this.m_fileLength = this.m_channel.size();
/* 196 */       if ((this.m_flags & 0x100000) != 0)
/*     */       {
/* 198 */         if (this.m_fileLength > 2147483647L)
/*     */         {
/* 200 */           this.m_flags &= -1048577;
/* 201 */           throw new IdcByteHandlerException("syZipPreloadError", new Object[0]);
/*     */         }
/* 203 */         this.m_buffer = ByteBuffer.allocateDirect((int)this.m_fileLength);
/* 204 */         this.m_bufferSize = this.m_channel.read(this.m_buffer);
/* 205 */         this.m_buffer.flip();
/* 206 */         this.m_bufferOffset = 0L;
/* 207 */         if (this.m_bufferSize < this.m_fileLength)
/*     */         {
/* 209 */           throw new IdcByteHandlerException('r', this.m_bufferSize, this.m_fileLength);
/*     */         }
/* 211 */         close();
/* 212 */         return;
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 217 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 225 */     if (null == this.m_access)
/*     */     {
/* 227 */       return;
/*     */     }
/*     */ 
/* 230 */     this.m_access.close();
/* 231 */     this.m_channel = null;
/* 232 */     this.m_access = null;
/*     */   }
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/* 239 */     return this.m_features;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowClone() throws IdcByteHandlerException
/*     */   {
/* 244 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/* 246 */       throw new IdcByteHandlerException(65536);
/*     */     }
/*     */     IdcRandomAccessByteFile handler;
/* 249 */     if (null != this.m_file)
/*     */     {
/* 251 */       IdcRandomAccessByteFile handler = new IdcRandomAccessByteFile(this.m_file);
/* 252 */       handler.m_access = this.m_access;
/*     */     }
/*     */     else
/*     */     {
/* 256 */       handler = new IdcRandomAccessByteFile(this.m_access);
/*     */     }
/* 258 */     handler.m_parent = this;
/* 259 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/* 260 */     handler.m_flags = this.m_flags;
/* 261 */     handler.m_channel = this.m_channel;
/* 262 */     handler.m_fileLength = this.m_fileLength;
/*     */ 
/* 264 */     if (null != this.m_buffer)
/*     */     {
/* 266 */       handler.m_buffer = this.m_buffer.duplicate();
/* 267 */       handler.m_bufferOffset = this.m_bufferOffset;
/* 268 */       handler.m_bufferSize = this.m_bufferSize;
/*     */     }
/* 270 */     handler.m_offset = this.m_offset;
/* 271 */     handler.m_length = this.m_length;
/* 272 */     return handler;
/*     */   }
/*     */ 
/*     */   public IdcByteHandler shallowCloneSubrange(long offset, long length) throws IdcByteHandlerException
/*     */   {
/* 277 */     if ((this.m_features & 0x10000) == 0)
/*     */     {
/* 279 */       throw new IdcByteHandlerException(65536);
/*     */     }
/* 281 */     long myLength = (this.m_length == 0L) ? this.m_fileLength : this.m_length;
/* 282 */     if ((offset + this.m_offset < 0L) || (offset + this.m_offset > myLength))
/*     */     {
/* 284 */       throw new IdcByteHandlerException("syByteOffsetBad", new Object[] { Long.valueOf(offset) });
/*     */     }
/* 286 */     offset += this.m_offset;
/* 287 */     if (((length > 0L) && (length > myLength - offset)) || ((length <= 0L) && (-length > myLength - offset)))
/*     */     {
/* 289 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Long.valueOf(length) });
/*     */     }
/* 291 */     if (length <= 0L)
/*     */     {
/* 293 */       length += this.m_fileLength - offset;
/*     */     }
/*     */     IdcRandomAccessByteFile handler;
/* 296 */     if (null != this.m_file)
/*     */     {
/* 298 */       IdcRandomAccessByteFile handler = new IdcRandomAccessByteFile(this.m_file);
/* 299 */       handler.m_access = this.m_access;
/*     */     }
/*     */     else
/*     */     {
/* 303 */       handler = new IdcRandomAccessByteFile(this.m_access);
/*     */     }
/* 305 */     handler.m_parent = this;
/* 306 */     handler.m_features = (this.m_features & 0xFFFFFEFF);
/* 307 */     handler.m_flags = this.m_flags;
/* 308 */     handler.m_channel = this.m_channel;
/* 309 */     handler.m_fileLength = this.m_fileLength;
/*     */ 
/* 312 */     if ((this.m_buffer != null) && ((this.m_flags & 0x100000) != 0))
/*     */     {
/* 314 */       handler.m_buffer = this.m_buffer.duplicate();
/* 315 */       handler.m_bufferOffset = this.m_bufferOffset;
/* 316 */       handler.m_bufferSize = this.m_bufferSize;
/*     */     }
/* 318 */     handler.m_offset = offset;
/* 319 */     handler.m_length = length;
/* 320 */     return handler;
/*     */   }
/*     */ 
/*     */   public long getSize()
/*     */   {
/* 326 */     return (this.m_length == 0L) ? this.m_fileLength : this.m_length;
/*     */   }
/*     */ 
/*     */   public long getPosition()
/*     */   {
/* 331 */     return this.m_position;
/*     */   }
/*     */ 
/*     */   public void setSize(long size)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 337 */     if (((this.m_features & 0x100) == 0) || (this.m_length != 0L))
/*     */     {
/* 339 */       throw new IdcByteHandlerException(256);
/*     */     }
/* 341 */     if (size < 0L)
/*     */     {
/* 343 */       throw new IdcByteHandlerException("syByteSizeBad", new Object[] { Long.valueOf(size) });
/*     */     }
/* 345 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 347 */       markIsDirty(true);
/*     */     }
/*     */     try
/*     */     {
/* 351 */       this.m_access.setLength(size);
/* 352 */       this.m_fileLength = this.m_access.length();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 356 */       throw new IdcByteHandlerException(e);
/*     */     }
/* 358 */     if (this.m_position <= this.m_fileLength)
/*     */       return;
/* 360 */     this.m_position = this.m_fileLength;
/*     */   }
/*     */ 
/*     */   public void setPosition(long position)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 366 */     if ((this.m_features & 0x200) == 0)
/*     */     {
/* 368 */       throw new IdcByteHandlerException(512);
/*     */     }
/* 370 */     if (position >= 0L) if (position <= ((this.m_length == 0L) ? this.m_fileLength : this.m_length))
/*     */         break label74;
/* 372 */     throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */ 
/* 374 */     label74: this.m_position = position;
/*     */   }
/*     */ 
/*     */   public int readNext(byte[] dst, int dstoffset, int length)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 380 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 382 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 384 */     if (length < 0)
/*     */     {
/* 386 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Integer.valueOf(length) });
/*     */     }
/* 388 */     long myLength = (this.m_length == 0L) ? this.m_fileLength : this.m_length;
/* 389 */     if (this.m_position + length > myLength)
/*     */     {
/* 391 */       length = (int)(myLength - this.m_position);
/*     */     }
/* 393 */     if (length <= 0)
/*     */     {
/* 395 */       return length;
/*     */     }
/*     */ 
/* 398 */     int numBytes = doRead(this.m_position, dst, dstoffset, length);
/* 399 */     if (numBytes > 0)
/*     */     {
/* 401 */       this.m_position += numBytes;
/*     */     }
/* 403 */     return numBytes;
/*     */   }
/*     */ 
/*     */   public int readFrom(long position, byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 408 */     if ((this.m_features & 0x1) == 0)
/*     */     {
/* 410 */       throw new IdcByteHandlerException(1);
/*     */     }
/* 412 */     if ((this.m_features & 0x200) == 0)
/*     */     {
/* 414 */       throw new IdcByteHandlerException(512);
/*     */     }
/* 416 */     if (length < 0)
/*     */     {
/* 418 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Integer.valueOf(length) });
/*     */     }
/* 420 */     long myLength = (this.m_length == 0L) ? this.m_fileLength : this.m_length;
/* 421 */     if ((position < 0L) || (position > myLength))
/*     */     {
/* 423 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 425 */     if (position + length > myLength)
/*     */     {
/* 427 */       length = (int)(myLength - position);
/*     */     }
/* 429 */     if (length <= 0)
/*     */     {
/* 431 */       return length;
/*     */     }
/*     */ 
/* 434 */     return doRead(position, dst, dstoffset, length);
/*     */   }
/*     */ 
/*     */   protected int doRead(long position, byte[] dst, int dstoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 439 */     int numBytes = 0;
/*     */ 
/* 444 */     if ((position + this.m_offset >= this.m_bufferOffset) && (position + this.m_offset < this.m_bufferOffset + this.m_bufferSize))
/*     */     {
/* 447 */       numBytes = (position + this.m_offset + length < this.m_bufferOffset + this.m_bufferSize) ? length : (int)(this.m_bufferOffset + this.m_bufferSize - (position + this.m_offset));
/*     */ 
/* 449 */       this.m_buffer.position((int)(position + this.m_offset - this.m_bufferOffset));
/* 450 */       this.m_buffer.get(dst, dstoffset, numBytes);
/* 451 */       position += numBytes;
/* 452 */       if (numBytes >= length)
/*     */       {
/* 455 */         return numBytes;
/*     */       }
/* 457 */       dstoffset += numBytes;
/*     */     }
/*     */ 
/* 465 */     ensureOpenChannel();
/*     */ 
/* 467 */     this.m_bufferOffset = (position + this.m_offset & (MIN_BLOCK_SIZE - 1 ^ 0xFFFFFFFF));
/* 468 */     int extraLen = (int)(position + this.m_offset - this.m_bufferOffset);
/*     */ 
/* 470 */     int needLen = length - numBytes + extraLen + MIN_BLOCK_SIZE - 1 & (MIN_BLOCK_SIZE - 1 ^ 0xFFFFFFFF);
/* 471 */     if (needLen > this.m_bufferSize)
/*     */     {
/* 474 */       this.m_bufferSize = 0;
/*     */ 
/* 476 */       this.m_buffer = ByteBuffer.allocateDirect(needLen);
/*     */     }
/*     */     try
/*     */     {
/* 480 */       this.m_buffer.rewind();
/* 481 */       int readLen = this.m_channel.read(this.m_buffer, this.m_bufferOffset);
/* 482 */       if (readLen < length - numBytes)
/*     */       {
/* 484 */         throw new IdcByteHandlerException('r', readLen, length - numBytes);
/*     */       }
/* 486 */       this.m_bufferSize = readLen;
/* 487 */       this.m_buffer.flip();
/* 488 */       this.m_buffer.position((int)(position + this.m_offset - this.m_bufferOffset));
/* 489 */       this.m_buffer.get(dst, dstoffset, length - numBytes);
/* 490 */       return length;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 494 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public int writeNext(byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 500 */     int numBytes = writeTo(this.m_position, src, srcoffset, length);
/* 501 */     if (numBytes > 0)
/*     */     {
/* 503 */       this.m_position += numBytes;
/*     */     }
/* 505 */     return numBytes;
/*     */   }
/*     */ 
/*     */   public int writeTo(long position, byte[] src, int srcoffset, int length) throws IdcByteHandlerException
/*     */   {
/* 510 */     if ((this.m_features & 0x2) == 0)
/*     */     {
/* 512 */       throw new IdcByteHandlerException(2);
/*     */     }
/* 514 */     if (length < 0)
/*     */     {
/* 516 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Integer.valueOf(length) });
/*     */     }
/* 518 */     if (position < 0L)
/*     */     {
/* 520 */       throw new IdcByteHandlerException("syBytePositionBad", new Object[] { Long.valueOf(position) });
/*     */     }
/* 522 */     if (length <= 0)
/*     */     {
/* 524 */       return length;
/*     */     }
/* 526 */     long myLength = (this.m_length == 0L) ? this.m_fileLength : this.m_length;
/* 527 */     if (((this.m_features & 0x100) == 0) && 
/* 529 */       (position + length > myLength))
/*     */     {
/* 531 */       throw new IdcByteHandlerException("syByteLengthBad", new Object[] { Integer.valueOf(length) });
/*     */     }
/*     */ 
/* 535 */     if ((position < this.m_bufferOffset + this.m_bufferSize) && (this.m_bufferOffset < position + length))
/*     */     {
/* 537 */       this.m_bufferSize = 0;
/* 538 */       this.m_buffer = null;
/*     */     }
/* 540 */     ensureOpenChannel();
/* 541 */     if ((this.m_features & 0x10000000) == 0)
/*     */     {
/* 543 */       markIsDirty(true);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 548 */       ByteBuffer buffer = ByteBuffer.wrap(src, srcoffset, length);
/* 549 */       int numBytes = this.m_channel.write(buffer, position);
/* 550 */       if ((numBytes > 0) && (numBytes + position > myLength))
/*     */       {
/* 552 */         setSize(numBytes + position);
/*     */       }
/* 554 */       return numBytes;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 558 */       throw new IdcByteHandlerException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void markIsDirty(boolean isDirty)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 565 */     if (!isDirty)
/*     */     {
/* 567 */       this.m_features &= -268435457;
/* 568 */       return;
/*     */     }
/* 570 */     for (IdcRandomAccessByteFile kin = this; null != kin; kin = kin.m_parent)
/*     */     {
/* 572 */       if ((kin.m_features & 0x10000000) != 0) {
/*     */         return;
/*     */       }
/*     */ 
/* 576 */       kin.m_features |= 268435456;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 584 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98717 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.IdcRandomAccessByteFile
 * JD-Core Version:    0.5.4
 */