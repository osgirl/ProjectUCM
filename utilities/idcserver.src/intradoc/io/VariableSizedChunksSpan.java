/*     */ package intradoc.io;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.nio.MappedByteBuffer;
/*     */ 
/*     */ public class VariableSizedChunksSpan extends ChunkedMemoryMappedFile.Span
/*     */ {
/*     */   public static final int MIN_CHUNK_SIZE_SHIFT = 4;
/*     */   public static final int MAX_CHUNK_SIZE_SHIFT = 31;
/*     */   public final ChunkedMemoryMappedFile m_file;
/*     */   public final int m_minChunkSize;
/*     */   public final int m_maxChunkSize;
/*     */   public final byte m_minChunkSizeShift;
/*     */   public final byte m_maxChunkSizeShift;
/*     */   public final long[] m_freeChunksOffsetsBySize;
/*     */   protected boolean m_areFreeChunksAllocated;
/*     */ 
/*     */   public VariableSizedChunksSpan(ChunkedMemoryMappedFile file, long spanOffset)
/*     */   {
/* 191 */     this(file, spanOffset, 0L);
/*     */   }
/*     */ 
/*     */   public VariableSizedChunksSpan(ChunkedMemoryMappedFile file, long spanOffset, long spanLength) {
/* 195 */     this(file, 16, -2147483648, spanOffset, spanLength);
/*     */   }
/*     */ 
/*     */   public VariableSizedChunksSpan(ChunkedMemoryMappedFile file, int minSize, int maxSize, long offset, long length) {
/* 199 */     this(new Marker(file, offset), MemoryMappedFile.log2roundup(minSize), MemoryMappedFile.log2roundup(maxSize));
/*     */ 
/* 204 */     ChunkedMemoryMappedFile.SpanMarker marker = this.m_spanMarker;
/* 205 */     marker.m_spanOffset = offset;
/* 206 */     marker.m_spanLength = length;
/*     */   }
/*     */ 
/*     */   public VariableSizedChunksSpan(Marker marker, int minSizeShift, int maxSizeShift) {
/* 210 */     super(marker);
/* 211 */     this.m_file = marker.m_file;
/* 212 */     if (minSizeShift < 4)
/*     */     {
/* 214 */       minSizeShift = 4;
/*     */     }
/* 216 */     if (maxSizeShift < minSizeShift)
/*     */     {
/* 218 */       maxSizeShift = minSizeShift;
/*     */     }
/* 220 */     if (maxSizeShift > 31)
/*     */     {
/* 222 */       maxSizeShift = 31;
/*     */     }
/* 224 */     this.m_minChunkSize = (1 << minSizeShift);
/* 225 */     this.m_minChunkSizeShift = (byte)minSizeShift;
/* 226 */     this.m_maxChunkSize = (1 << maxSizeShift);
/* 227 */     this.m_maxChunkSizeShift = (byte)maxSizeShift;
/*     */ 
/* 229 */     this.m_freeChunksOffsetsBySize = new long[maxSizeShift + 1];
/*     */   }
/*     */ 
/*     */   protected void freeAChunk(long chunkOffset, int chunkSizeShift)
/*     */     throws IOException
/*     */   {
/* 242 */     ChunkedMemoryMappedFile file = this.m_file;
/* 243 */     MappedByteBuffer[] extents = file.m_extents;
/* 244 */     int extentShift = file.m_extentShift;
/* 245 */     int extentSize = 1 << extentShift;
/*     */ 
/* 248 */     int bufferIndex = (int)(chunkOffset >>> extentShift);
/* 249 */     int bufferOffset = (int)chunkOffset & extentSize - 4;
/* 250 */     MappedByteBuffer extent = extents[bufferIndex];
/* 251 */     extent.putInt(bufferOffset, 1718773093);
/*     */ 
/* 253 */     bufferOffset += 4;
/* 254 */     if (bufferOffset >= extentSize)
/*     */     {
/* 256 */       bufferOffset = 0;
/* 257 */       extent = extents[(++bufferIndex)];
/*     */     }
/* 259 */     int chunkLength = 1 << chunkSizeShift - 8;
/* 260 */     extent.putInt(bufferOffset, chunkLength);
/* 261 */     bufferOffset += 4;
/* 262 */     if (bufferOffset >= extentSize)
/*     */     {
/* 264 */       bufferOffset = 0;
/* 265 */       extent = extents[(++bufferIndex)];
/*     */     }
/*     */ 
/* 268 */     long[] freeOffsets = this.m_freeChunksOffsetsBySize;
/* 269 */     long nextFreeOffset = freeOffsets[chunkSizeShift];
/* 270 */     extent.putLong(bufferOffset, nextFreeOffset);
/*     */ 
/* 272 */     file.flush(chunkOffset, chunkLength);
/*     */ 
/* 274 */     freeOffsets[chunkSizeShift] = chunkOffset;
/* 275 */     Marker marker = (Marker)this.m_spanMarker;
/* 276 */     marker.save();
/* 277 */     file.flush(marker.m_chunkOffset, marker.m_chunkLength + 8);
/*     */   }
/*     */ 
/*     */   protected long useAFreeChunk(int chunkSizeShift)
/*     */     throws IOException
/*     */   {
/* 290 */     long[] freeOffsets = this.m_freeChunksOffsetsBySize;
/* 291 */     long freeOffset = freeOffsets[chunkSizeShift];
/* 292 */     if (freeOffset == 0L)
/*     */     {
/* 294 */       return 0L;
/*     */     }
/* 296 */     ChunkedMemoryMappedFile file = this.m_file;
/* 297 */     MappedByteBuffer[] extents = file.m_extents;
/* 298 */     int extentShift = file.m_extentShift;
/* 299 */     int extentSize = 1 << extentShift;
/*     */ 
/* 302 */     int bufferIndex = (int)(freeOffset >>> extentShift);
/* 303 */     int bufferOffset = (int)freeOffset & extentSize - 4;
/* 304 */     MappedByteBuffer extent = extents[bufferIndex];
/* 305 */     int chunkType = extent.getInt(bufferOffset);
/* 306 */     if (chunkType != 1718773093)
/*     */     {
/*     */       ChunkedMemoryMappedFile tmp88_86 = file; tmp88_86.getClass(); throw new ChunkedMemoryMappedFile.ChunkTypeMismatch(tmp88_86, chunkType, 1718773093, freeOffset);
/*     */     }
/* 310 */     bufferOffset += 4;
/* 311 */     if (bufferOffset >= extentSize)
/*     */     {
/* 313 */       bufferOffset = 0;
/* 314 */       extent = extents[(++bufferIndex)];
/*     */     }
/* 316 */     int chunkLength = extent.getInt(bufferOffset);
/* 317 */     if (chunkLength + 8 != 1 << chunkSizeShift)
/*     */     {
/* 319 */       String msg = file.createMessageForException(freeOffset, new Object[] { "bad free chunk size 0x", "ARG_HEX_INTEGER", Integer.valueOf(chunkLength + 8), " (expected 0x", "ARG_HEX_INTEGER", Integer.valueOf(1 << chunkSizeShift), ")" });
/*     */ 
/* 323 */       throw new IOException(msg);
/*     */     }
/*     */ 
/* 326 */     bufferOffset += 4;
/* 327 */     if (bufferOffset >= extentSize)
/*     */     {
/* 329 */       bufferOffset = 0;
/* 330 */       extent = extents[(++bufferIndex)];
/*     */     }
/* 332 */     long newFreeOffset = extent.getLong(bufferOffset);
/*     */ 
/* 334 */     freeOffsets[chunkSizeShift] = newFreeOffset;
/* 335 */     Marker marker = (Marker)this.m_spanMarker;
/* 336 */     marker.save();
/* 337 */     file.flush(marker.m_chunkOffset, marker.m_chunkLength + 8);
/*     */ 
/* 340 */     return freeOffset;
/*     */   }
/*     */ 
/*     */   public synchronized long allocateChunk(int chunkLength)
/*     */     throws IOException
/*     */   {
/* 353 */     if (this.m_file.m_isReadOnly)
/*     */     {
/* 355 */       throw new IOException("file is read-only");
/*     */     }
/* 357 */     int chunkSizeShift = MemoryMappedFile.log2roundup(chunkLength + 8);
/* 358 */     if (chunkSizeShift < this.m_minChunkSizeShift)
/*     */     {
/* 360 */       chunkSizeShift = this.m_minChunkSizeShift;
/*     */     }
/* 362 */     int maxChunkSizeShift = this.m_maxChunkSizeShift;
/* 363 */     if (chunkSizeShift > maxChunkSizeShift)
/*     */     {
/* 365 */       String msg = "chunkLength " + chunkLength + " is too large (maximum is " + (this.m_maxChunkSize - 8) + ")";
/* 366 */       throw new IOException(msg);
/*     */     }
/*     */ 
/* 369 */     for (int freeSizeShift = chunkSizeShift; freeSizeShift <= maxChunkSizeShift; ++freeSizeShift)
/*     */     {
/* 371 */       long freeOffset = useAFreeChunk(chunkSizeShift);
/* 372 */       if (freeOffset == 0L) {
/*     */         continue;
/*     */       }
/* 375 */       while (freeSizeShift-- > chunkSizeShift)
/*     */       {
/* 377 */         freeAChunk(freeOffset + (1 << freeSizeShift), freeSizeShift);
/*     */       }
/* 379 */       return freeOffset;
/*     */     }
/*     */ 
/* 383 */     Marker marker = (Marker)this.m_spanMarker;
/* 384 */     if (marker.m_spanLength != 0L)
/*     */     {
/* 386 */       String msg = "unable to allocate " + Integer.toString(chunkLength) + " bytes: out of space in span";
/* 387 */       throw new IOException(msg);
/*     */     }
/* 389 */     resize(marker.m_spanLength + (1 << maxChunkSizeShift));
/*     */ 
/* 391 */     return allocateChunk(chunkLength);
/*     */   }
/*     */ 
/*     */   public synchronized void freeChunk(long chunkOffset)
/*     */     throws IOException
/*     */   {
/* 402 */     ChunkedMemoryMappedFile file = this.m_file;
/* 403 */     if (file.m_isReadOnly)
/*     */     {
/* 405 */       throw new IOException("File is read-only");
/*     */     }
/* 407 */     byte minChunkSizeShift = this.m_minChunkSizeShift;
/* 408 */     if (chunkOffset % (1 << minChunkSizeShift) != 0L)
/*     */     {
/* 410 */       String msg = "chunk offset must be a multiple of the minimum chunk size";
/* 411 */       throw new IOException("chunk offset must be a multiple of the minimum chunk size");
/*     */     }
/* 413 */     Marker marker = (Marker)this.m_spanMarker;
/* 414 */     long spanOffset = marker.m_spanOffset;
/* 415 */     long spanLength = marker.m_spanLength;
/* 416 */     long fileLength = this.m_file.length();
/* 417 */     long actualSpanLength = (spanLength != 0L) ? spanLength : fileLength - spanOffset;
/*     */ 
/* 419 */     int chunkLength = file.getIntAt(chunkOffset + 4L);
/* 420 */     int chunkSizeShift = MemoryMappedFile.log2roundup(chunkLength + 8);
/* 421 */     if (chunkSizeShift < minChunkSizeShift)
/*     */     {
/* 423 */       chunkSizeShift = minChunkSizeShift;
/*     */     }
/* 425 */     if ((chunkOffset < spanOffset) || (chunkOffset + (1 << chunkSizeShift) >= actualSpanLength))
/*     */     {
/* 427 */       String msg = file.createMessageForException(chunkOffset, new Object[] { "chunk offset is outside of span" });
/* 428 */       throw new IOException(msg);
/*     */     }
/* 430 */     freeAChunk(chunkOffset, chunkSizeShift);
/*     */   }
/*     */ 
/*     */   public synchronized void resize(long newSpanLength)
/*     */     throws IOException
/*     */   {
/* 436 */     ChunkedMemoryMappedFile file = this.m_file;
/* 437 */     if (file.m_isReadOnly)
/*     */     {
/* 439 */       throw new IOException("File is read-only");
/*     */     }
/* 441 */     Marker marker = (Marker)this.m_spanMarker;
/* 442 */     if (newSpanLength == 0L)
/*     */     {
/* 445 */       marker.m_spanLength = 0L;
/* 446 */       return;
/*     */     }
/* 448 */     byte minChunkSizeShift = this.m_minChunkSizeShift; byte maxChunkSizeShift = this.m_maxChunkSizeShift;
/* 449 */     int minChunkSize = 1 << minChunkSizeShift; int maxChunkSize = 1 << maxChunkSizeShift;
/* 450 */     if (newSpanLength % minChunkSize != 0L)
/*     */     {
/* 452 */       String msg = "span length must be a multiple of the minimum chunk size";
/* 453 */       throw new IOException("span length must be a multiple of the minimum chunk size");
/*     */     }
/* 455 */     long fileLength = file.length();
/* 456 */     long spanOffset = marker.m_spanOffset;
/* 457 */     long spanLength = marker.m_spanLength;
/* 458 */     long actualSpanLength = (spanLength != 0L) ? spanLength : fileLength - spanOffset;
/* 459 */     if (newSpanLength < actualSpanLength)
/*     */     {
/* 461 */       throw new IOException("decreasing span length is unsupported");
/*     */     }
/* 463 */     long newBytes = newSpanLength - actualSpanLength;
/* 464 */     if ((spanOffset + newSpanLength > fileLength) && (newBytes > 1 << file.m_extentShift))
/*     */     {
/* 466 */       String msg = "cannot extend the file by more than one extent";
/* 467 */       throw new IOException("cannot extend the file by more than one extent");
/*     */     }
/* 469 */     long markerOffset = marker.m_chunkOffset;
/* 470 */     if ((markerOffset >= spanOffset + actualSpanLength) && (markerOffset < spanOffset + newSpanLength))
/*     */     {
/* 472 */       String msg = "cannot resize the span to include an uncontained marker";
/* 473 */       throw new IOException("cannot resize the span to include an uncontained marker");
/*     */     }
/*     */ 
/* 476 */     marker.m_spanLength = newSpanLength;
/* 477 */     marker.save();
/*     */ 
/* 479 */     long lastOffset = spanOffset + newSpanLength;
/* 480 */     long firstOffset = spanOffset + actualSpanLength;
/* 481 */     while (lastOffset - maxChunkSize >= firstOffset)
/*     */     {
/* 483 */       lastOffset -= maxChunkSize;
/* 484 */       freeAChunk(lastOffset, maxChunkSizeShift);
/*     */     }
/* 486 */     int chunkSizeShift = maxChunkSizeShift;
/* 487 */     while (--chunkSizeShift >= minChunkSizeShift)
/*     */     {
/* 489 */       int chunkSize = 1 << chunkSizeShift;
/* 490 */       if (lastOffset - chunkSize >= firstOffset)
/*     */       {
/* 492 */         lastOffset -= chunkSize;
/* 493 */         freeAChunk(lastOffset, chunkSizeShift);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public synchronized void save()
/*     */     throws IOException
/*     */   {
/* 501 */     if (this.m_file.m_isReadOnly)
/*     */     {
/* 503 */       throw new IOException("file is read-only");
/*     */     }
/* 505 */     if (this.m_areFreeChunksAllocated)
/*     */     {
/* 507 */       this.m_spanMarker.save();
/*     */     }
/*     */     else
/*     */     {
/* 511 */       allocateInitialFreeChunks();
/* 512 */       this.m_areFreeChunksAllocated = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void allocateInitialFreeChunks()
/*     */     throws IOException
/*     */   {
/* 523 */     ChunkedMemoryMappedFile file = this.m_file;
/* 524 */     byte minChunkSizeShift = this.m_minChunkSizeShift; byte maxChunkSizeShift = this.m_maxChunkSizeShift;
/* 525 */     int minChunkSize = 1 << minChunkSizeShift; int maxChunkSize = 1 << maxChunkSizeShift;
/* 526 */     Marker marker = (Marker)this.m_spanMarker;
/* 527 */     long markerOffset = marker.m_chunkOffset;
/*     */ 
/* 529 */     long spanOffset = marker.m_spanOffset;
/* 530 */     long spanLength = marker.m_spanLength;
/* 531 */     if ((spanLength > 0L) && (spanLength % minChunkSize != 0L))
/*     */     {
/* 533 */       String msg = "span length must be a multiple of the minimum chunk size";
/* 534 */       throw new IOException("span length must be a multiple of the minimum chunk size");
/*     */     }
/* 536 */     long fileLength = file.length();
/* 537 */     long actualSpanLength = (spanLength != 0L) ? spanLength : fileLength - spanOffset;
/* 538 */     boolean isMarkerContained = (markerOffset > spanOffset) && (((spanLength == 0L) || (markerOffset - spanOffset < spanLength)));
/*     */ 
/* 540 */     if (isMarkerContained)
/*     */     {
/* 542 */       long spanRelativeOffset = markerOffset - spanOffset;
/* 543 */       if (spanRelativeOffset % minChunkSize != 0L)
/*     */       {
/* 545 */         String msg = "marker must be a multiple of the minimum chunk size if contained in the span";
/* 546 */         throw new IOException("marker must be a multiple of the minimum chunk size if contained in the span");
/*     */       }
/*     */ 
/* 549 */       if ((spanLength != 0L) && (spanRelativeOffset + 256L > spanLength))
/*     */       {
/* 551 */         String msg = "marker is not fully contained in the span";
/* 552 */         throw new IOException("marker is not fully contained in the span");
/*     */       }
/* 554 */       if (spanRelativeOffset + 256L > actualSpanLength)
/*     */       {
/* 556 */         actualSpanLength = spanRelativeOffset + 256L;
/*     */       }
/*     */     }
/*     */ 
/* 560 */     long actualSpanLengthRemainder = actualSpanLength % minChunkSize;
/* 561 */     if (actualSpanLengthRemainder != 0L)
/*     */     {
/* 563 */       actualSpanLength += minChunkSize - actualSpanLengthRemainder;
/*     */     }
/* 565 */     if ((spanLength == 0L) && (actualSpanLength < maxChunkSize))
/*     */     {
/* 567 */       actualSpanLength = maxChunkSize;
/*     */     }
/*     */ 
/* 570 */     if (spanOffset + spanLength > fileLength)
/*     */     {
/* 572 */       if (spanOffset + spanLength > fileLength + (1 << file.m_extentSize))
/*     */       {
/* 574 */         String msg = "cannot extend the file by more than one extent";
/* 575 */         throw new IOException("cannot extend the file by more than one extent");
/*     */       }
/* 577 */       file.extend();
/*     */     }
/*     */ 
/* 581 */     marker.save();
/*     */ 
/* 583 */     int markerSize = 1 << MemoryMappedFile.log2roundup(marker.m_chunkLength + 8);
/*     */ 
/* 590 */     long lastOffset = spanOffset + actualSpanLength;
/* 591 */     long firstOffset = (isMarkerContained) ? markerOffset + markerSize : spanOffset;
/* 592 */     while (lastOffset - maxChunkSize >= firstOffset)
/*     */     {
/* 594 */       lastOffset -= maxChunkSize;
/* 595 */       freeAChunk(lastOffset, maxChunkSizeShift);
/*     */     }
/* 597 */     int chunkSizeShift = maxChunkSizeShift;
/* 598 */     while (--chunkSizeShift >= minChunkSizeShift)
/*     */     {
/* 600 */       int chunkSize = 1 << chunkSizeShift;
/* 601 */       if (lastOffset - chunkSize >= firstOffset)
/*     */       {
/* 603 */         lastOffset -= chunkSize;
/* 604 */         freeAChunk(lastOffset, chunkSizeShift);
/*     */       }
/*     */     }
/* 607 */     if (!isMarkerContained)
/*     */       return;
/* 609 */     lastOffset = markerOffset;
/* 610 */     while (lastOffset - maxChunkSize >= spanOffset)
/*     */     {
/* 612 */       lastOffset -= maxChunkSize;
/* 613 */       freeAChunk(lastOffset, maxChunkSizeShift);
/*     */     }
/* 615 */     chunkSizeShift = maxChunkSizeShift;
/* 616 */     while (--chunkSizeShift >= minChunkSizeShift)
/*     */     {
/* 618 */       int chunkSize = 1 << chunkSizeShift;
/* 619 */       if (lastOffset - chunkSize >= spanOffset)
/*     */       {
/* 621 */         lastOffset -= chunkSize;
/* 622 */         freeAChunk(lastOffset, chunkSizeShift);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 630 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97768 $";
/*     */   }
/*     */ 
/*     */   public static class Marker extends ChunkedMemoryMappedFile.SpanMarker
/*     */   {
/*     */     public final ChunkedMemoryMappedFile m_file;
/*     */     public VariableSizedChunksSpan m_span;
/*     */ 
/*     */     public Marker(ChunkedMemoryMappedFile file, long chunkOffset)
/*     */     {
/*     */       // Byte code:
/*     */       //   0: aload_0
/*     */       //   1: aload_1
/*     */       //   2: dup
/*     */       //   3: invokevirtual 1	java/lang/Object:getClass	()Ljava/lang/Class;
/*     */       //   6: pop
/*     */       //   7: ldc 2
/*     */       //   9: lload_2
/*     */       //   10: invokespecial 3	intradoc/io/ChunkedMemoryMappedFile$SpanMarker:<init>	(Lintradoc/io/ChunkedMemoryMappedFile;IJ)V
/*     */       //   13: aload_0
/*     */       //   14: aload_1
/*     */       //   15: putfield 4	intradoc/io/VariableSizedChunksSpan$Marker:m_file	Lintradoc/io/ChunkedMemoryMappedFile;
/*     */       //   18: return
/*     */     }
/*     */ 
/*     */     public void load()
/*     */       throws IOException
/*     */     {
/*  75 */       file = this.m_file;
/*  76 */       super.load();
/*  77 */       int length = this.m_chunkLength;
/*  78 */       if (length < 8)
/*     */       {
/*     */         ChunkedMemoryMappedFile tmp25_24 = file; tmp25_24.getClass(); throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(tmp25_24, this, 8);
/*     */       }
/*  82 */       offset = this.m_chunkOffset + 13L;
/*  83 */       int minSizeShift = file.getByteAt(++offset);
/*  84 */       if ((minSizeShift < 4) || (minSizeShift > 31))
/*     */       {
/*     */         ChunkedMemoryMappedFile tmp75_74 = file; tmp75_74.getClass(); throw new ChunkedMemoryMappedFile.NumberOutOfRange(tmp75_74, this, 14, "minimum chunk size shift", Integer.valueOf(minSizeShift), Integer.valueOf(4), Integer.valueOf(31));
/*     */       }
/*     */ 
/*  90 */       int maxSizeShift = file.getByteAt(++offset);
/*  91 */       if ((maxSizeShift < minSizeShift) || (maxSizeShift > 31))
/*     */       {
/*     */         ChunkedMemoryMappedFile tmp133_132 = file; tmp133_132.getClass(); throw new ChunkedMemoryMappedFile.NumberOutOfRange(tmp133_132, this, 15, "maximum chunk size shift", Integer.valueOf(maxSizeShift), Integer.valueOf(minSizeShift), Integer.valueOf(31));
/*     */       }
/*     */ 
/*  96 */       int numChunkOffsets = maxSizeShift - minSizeShift + 1;
/*  97 */       if (length < 16 + (numChunkOffsets << 3))
/*     */       {
/*     */         ChunkedMemoryMappedFile tmp187_186 = file; tmp187_186.getClass(); throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(tmp187_186, this, length, 16 + (numChunkOffsets << 3));
/*     */       }
/* 101 */       offset += 1L;
/* 102 */       int index = 16;
/* 103 */       long spanOffset = this.m_spanOffset;
/* 104 */       long spanLength = this.m_spanLength;
/* 105 */       if (spanLength <= 0L)
/*     */       {
/* 107 */         spanLength = 9223372036854775807L;
/*     */       }
/* 109 */       long maxOffset = spanLength - 16L;
/* 110 */       VariableSizedChunksSpan span = this.m_span = new VariableSizedChunksSpan(this, minSizeShift, maxSizeShift);
/* 111 */       offsets = span.m_freeChunksOffsetsBySize;
/* 112 */       o = minSizeShift; label368: if (o > maxSizeShift)
/*     */         break label368;
/*     */     }
/*     */ 
/*     */     public void save()
/*     */       throws IOException
/*     */     {
/* 130 */       ChunkedMemoryMappedFile file = this.m_file;
/* 131 */       VariableSizedChunksSpan span = this.m_span;
/* 132 */       byte minChunkSizeShift = span.m_minChunkSizeShift; byte maxChunkSizeShift = span.m_maxChunkSizeShift;
/* 133 */       int numChunkOffsets = maxChunkSizeShift - minChunkSizeShift + 1;
/* 134 */       int minimumChunkLength = 16 + (numChunkOffsets << 3);
/* 135 */       if (this.m_chunkLength < minimumChunkLength)
/*     */       {
/* 138 */         this.m_chunkLength = minimumChunkLength;
/*     */       }
/* 140 */       long chunkOffset = this.m_chunkOffset;
/* 141 */       file.checkAlignment(chunkOffset, 256, "variable-sized chunks span marker");
/* 142 */       super.save();
/*     */ 
/* 144 */       int extentShift = file.m_extentShift;
/* 145 */       int extentSize = 1 << extentShift;
/* 146 */       long initialOffset = chunkOffset + 30L;
/* 147 */       int bufferIndex = (int)(initialOffset >>> extentShift);
/* 148 */       int bufferOffset = (int)initialOffset & extentSize - 1;
/* 149 */       MappedByteBuffer extent = file.m_extents[bufferIndex];
/* 150 */       extent.put(bufferOffset++, minChunkSizeShift);
/* 151 */       extent.put(bufferOffset++, maxChunkSizeShift);
/* 152 */       long[] freeOffsets = span.m_freeChunksOffsetsBySize;
/* 153 */       for (int o = minChunkSizeShift; o < maxChunkSizeShift; bufferOffset += 8)
/*     */       {
/* 155 */         extent.putLong(bufferOffset, freeOffsets[o]);
/*     */ 
/* 153 */         ++o;
/*     */       }
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.VariableSizedChunksSpan
 * JD-Core Version:    0.5.4
 */