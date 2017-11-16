/*     */ package intradoc.io;
/*     */ 
/*     */ import intradoc.lang.Appender;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.RandomAccessFile;
/*     */ import java.nio.MappedByteBuffer;
/*     */ 
/*     */ public class ChunkedMemoryMappedFile extends MemoryMappedFile
/*     */ {
/*     */   public static final String ARG_AT_FILE = "ARG_AT_FILE_OFFSET";
/*     */   public static final String ARG_CHUNKED_FILE = "ARG_CHUNKED_FILE";
/*     */   public static final String ARG_FOURCC = "ARG_FOURCC";
/*     */   public static final String ARG_HEX_INTEGER = "ARG_HEX_INTEGER";
/*     */   public static final int CHUNK_TYPE_FREE = 1718773093;
/*     */   public static final int CHUNK_TYPE_SPAN = 1936744814;
/*     */   public static final int SPAN_MARKER_VARIABLE = 1718117490;
/*     */   public static final int SPAN_MARKER_FIXED = 1717726544;
/*     */   public int m_headerChunkType;
/*     */   public int m_headerChunkSize;
/*     */ 
/*     */   public static void appendChunkTypeTo(StringBuilder sb, int chunkType)
/*     */   {
/*  41 */     for (int i = 3; i >= 0; --i)
/*     */     {
/*  43 */       int ch = chunkType >>> (i << 3) & 0xFF;
/*  44 */       if ((ch < 20) || (ch >= 127))
/*     */       {
/*  46 */         sb.setLength(0);
/*  47 */         sb.append("0x");
/*  48 */         String str = Integer.toString(chunkType, 16);
/*  49 */         int len = str.length();
/*  50 */         while (len++ < 7)
/*     */         {
/*  52 */           sb.append('0');
/*     */         }
/*  54 */         sb.append(str);
/*  55 */         return;
/*     */       }
/*  57 */       sb.append((char)ch);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendArgsTo(StringBuilder sb, Object[] args)
/*     */   {
/*  63 */     int numArgs = args.length;
/*  64 */     for (int a = 0; a < numArgs; ++a)
/*     */     {
/*  66 */       Object obj = args[a];
/*  67 */       if (obj instanceof String)
/*     */       {
/*  69 */         if (obj == "ARG_AT_FILE_OFFSET")
/*     */         {
/*  71 */           sb.append("\nat ");
/*  72 */           sb.append(this);
/*  73 */           Number number = (Number)args[(++a)];
/*  74 */           long value = number.longValue();
/*  75 */           sb.append("0x");
/*  76 */           sb.append(Long.toString(value));
/*     */         }
/*  78 */         else if (obj == "ARG_CHUNKED_FILE")
/*     */         {
/*  80 */           sb.append(this);
/*     */         }
/*  82 */         else if (obj == "ARG_FOURCC")
/*     */         {
/*  84 */           Integer chunkType = (Integer)args[(++a)];
/*  85 */           appendChunkTypeTo(sb, chunkType.intValue());
/*     */         }
/*  87 */         else if (obj == "ARG_HEX_INTEGER")
/*     */         {
/*  89 */           Number number = (Number)args[(++a)];
/*  90 */           long value = number.longValue();
/*  91 */           sb.append("0x");
/*  92 */           sb.append(Long.toString(value));
/*     */         }
/*     */         else
/*     */         {
/*  96 */           sb.append((String)obj);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 101 */         String str = obj.toString();
/* 102 */         sb.append(str);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String createMessageForException(long offset, Object[] args)
/*     */   {
/* 109 */     StringBuilder sb = new StringBuilder();
/* 110 */     appendArgsTo(sb, args);
/* 111 */     appendArgsTo(sb, new Object[] { "ARG_AT_FILE_OFFSET", " offset ", "ARG_HEX_INTEGER", Long.valueOf(offset) });
/* 112 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public String createMessageForException(Chunk chunk, int offsetIntoChunk, Object[] args) {
/* 116 */     StringBuilder sb = new StringBuilder();
/* 117 */     appendArgsTo(sb, args);
/* 118 */     appendArgsTo(sb, new Object[] { "ARG_AT_FILE_OFFSET", " chunk ", "ARG_FOURCC", Integer.valueOf(chunk.m_chunkType) });
/* 119 */     appendArgsTo(sb, new Object[] { " offset ", "ARG_HEX_INTEGER", Long.valueOf(chunk.m_chunkOffset), "+", "ARG_HEX_INTEGER", Integer.valueOf(offsetIntoChunk) });
/* 120 */     return sb.toString();
/*     */   }
/*     */ 
/*     */   public ChunkedMemoryMappedFile(File file, int extentSize)
/*     */   {
/* 428 */     this(file, extentSize, false);
/*     */   }
/*     */ 
/*     */   public ChunkedMemoryMappedFile(File file, int extentSize, boolean isReadOnly)
/*     */   {
/* 433 */     super(file, extentSize, isReadOnly);
/*     */   }
/*     */ 
/*     */   public void checkAlignment(long offset, int nBytes, String description)
/*     */     throws IOException
/*     */   {
/* 439 */     if (offset % (nBytes - 1) == 0L)
/*     */       return;
/* 441 */     String msg = "bad " + description + " 0x" + Long.toString(offset, 16) + " (must be " + nBytes + "-byte aligned";
/*     */ 
/* 443 */     throw new IOException(msg);
/*     */   }
/*     */ 
/*     */   public void loadHeader()
/*     */     throws IOException
/*     */   {
/* 449 */     long fileLength = this.m_randomAccessFile.length();
/* 450 */     if (fileLength < 8 + this.m_headerChunkSize)
/*     */     {
/* 452 */       throw new IOException("unexpected end of file");
/*     */     }
/* 454 */     int headerChunkType = getIntAt(0L);
/* 455 */     if (headerChunkType == this.m_headerChunkType)
/*     */       return;
/* 457 */     throw new IOException("header chunk type mismatch: 0x" + Integer.toString(headerChunkType, 16));
/*     */   }
/*     */ 
/*     */   public void saveHeader()
/*     */     throws IOException
/*     */   {
/* 463 */     long extentsLength = this.m_file.length();
/* 464 */     if (extentsLength < 8 + this.m_headerChunkSize)
/*     */     {
/* 466 */       extend();
/*     */     }
/* 468 */     setIntAt(0L, this.m_headerChunkType);
/* 469 */     setIntAt(4L, this.m_headerChunkSize);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 475 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97768 $";
/*     */   }
/*     */ 
/*     */   public class SpanMarker extends ChunkedMemoryMappedFile.Chunk
/*     */   {
/*     */     public long m_spanOffset;
/*     */     public long m_spanLength;
/*     */     public final int m_spanType;
/*     */     public byte m_spanVersion;
/*     */ 
/*     */     public SpanMarker(int spanType, long chunkOffset)
/*     */     {
/* 366 */       super(ChunkedMemoryMappedFile.this, 1936744814, chunkOffset);
/* 367 */       this.m_spanType = spanType;
/*     */     }
/*     */ 
/*     */     public void load()
/*     */       throws IOException
/*     */     {
/* 373 */       super.load();
/* 374 */       if (this.m_chunkLength < 21)
/*     */       {
/* 376 */         throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(ChunkedMemoryMappedFile.this, this, 21);
/*     */       }
/* 378 */       long offset = this.m_chunkOffset + 8L;
/* 379 */       this.m_spanOffset = ChunkedMemoryMappedFile.this.getLongAt(offset);
/* 380 */       offset += 8L;
/* 381 */       this.m_spanLength = ChunkedMemoryMappedFile.this.getLongAt(offset);
/* 382 */       offset += 8L;
/* 383 */       int spanType = ChunkedMemoryMappedFile.this.getIntAt(offset);
/* 384 */       if (spanType != this.m_spanType)
/*     */       {
/* 386 */         String msg = ChunkedMemoryMappedFile.this.createMessageForException(this, 8, new Object[] { "bad span type ", "ARG_FOURCC", Integer.valueOf(spanType), " (expected ", "ARG_FOURCC", Integer.valueOf(this.m_spanType), ")" });
/*     */ 
/* 389 */         throw new IOException(msg);
/*     */       }
/* 391 */       offset += 4L;
/* 392 */       byte version = ChunkedMemoryMappedFile.this.getByteAt(offset);
/* 393 */       if (version == 0)
/*     */         return;
/* 395 */       String msg = ChunkedMemoryMappedFile.this.createMessageForException(this, 8, new Object[] { "bad span version ", Byte.valueOf(version), " (expected 0)" });
/*     */ 
/* 397 */       throw new IOException(msg);
/*     */     }
/*     */ 
/*     */     public void save()
/*     */       throws IOException
/*     */     {
/* 404 */       long offset = this.m_chunkOffset;
/* 405 */       ChunkedMemoryMappedFile.this.checkAlignment(offset, 32, "span marker offset");
/* 406 */       super.save();
/* 407 */       int extentShift = ChunkedMemoryMappedFile.this.m_extentShift;
/* 408 */       int bufferIndex = (int)(offset >>> extentShift);
/* 409 */       int bufferOffset = (int)offset & (1 << extentShift) - 4;
/* 410 */       MappedByteBuffer extent = ChunkedMemoryMappedFile.this.m_extents[bufferIndex];
/* 411 */       extent.putLong(bufferOffset, this.m_spanOffset);
/* 412 */       bufferOffset += 8;
/* 413 */       extent.putLong(bufferOffset, this.m_spanLength);
/* 414 */       bufferOffset += 8;
/* 415 */       extent.putInt(bufferOffset, this.m_spanType);
/* 416 */       bufferOffset += 8;
/* 417 */       extent.put(bufferOffset, 0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract class Span
/*     */   {
/*     */     public final ChunkedMemoryMappedFile.SpanMarker m_spanMarker;
/*     */ 
/*     */     protected Span(ChunkedMemoryMappedFile.SpanMarker marker)
/*     */     {
/* 327 */       this.m_spanMarker = marker;
/*     */     }
/*     */ 
/*     */     public abstract void resize(long paramLong)
/*     */       throws IOException;
/*     */ 
/*     */     public void save()
/*     */       throws IOException
/*     */     {
/* 347 */       this.m_spanMarker.save();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class Chunk
/*     */     implements Appender
/*     */   {
/*     */     public final int m_chunkType;
/*     */     public int m_chunkLength;
/*     */     public long m_chunkOffset;
/*     */ 
/*     */     public Chunk(int chunkType)
/*     */     {
/* 213 */       this.m_chunkType = chunkType;
/*     */     }
/*     */ 
/*     */     public Chunk(int chunkType, long offset) {
/* 217 */       this.m_chunkType = chunkType;
/* 218 */       this.m_chunkOffset = offset;
/*     */     }
/*     */ 
/*     */     public Chunk(int chunkType, int chunkLength, long offset) {
/* 222 */       this.m_chunkType = chunkType;
/* 223 */       this.m_chunkLength = chunkLength;
/* 224 */       this.m_chunkOffset = offset;
/*     */     }
/*     */ 
/*     */     public void appendTo(StringBuilder sb)
/*     */     {
/* 229 */       ChunkedMemoryMappedFile.appendChunkTypeTo(sb, this.m_chunkType);
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/* 235 */       StringBuilder sb = new StringBuilder();
/* 236 */       appendTo(sb);
/* 237 */       return sb.toString();
/*     */     }
/*     */ 
/*     */     public void load() throws IOException
/*     */     {
/* 242 */       int length = this.m_chunkLength = ChunkedMemoryMappedFile.this.getIntAt(this.m_chunkOffset + 4L);
/* 243 */       if ((length < 0) || (length > 2147483639))
/*     */       {
/* 245 */         throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(ChunkedMemoryMappedFile.this, this, 0, 2147483639);
/*     */       }
/*     */ 
/* 248 */       if (length + 8 <= 1 << ChunkedMemoryMappedFile.this.m_extentShift)
/*     */         return;
/* 250 */       throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(ChunkedMemoryMappedFile.this, this, 0, (1 << ChunkedMemoryMappedFile.this.m_extentShift) - 8);
/*     */     }
/*     */ 
/*     */     public void save()
/*     */       throws IOException
/*     */     {
/* 256 */       if (ChunkedMemoryMappedFile.this.m_isReadOnly)
/*     */       {
/* 258 */         return;
/*     */       }
/* 260 */       long offset = this.m_chunkOffset; long length = this.m_chunkLength;
/* 261 */       int extentShift = ChunkedMemoryMappedFile.this.m_extentShift;
/* 262 */       int extentSize = 1 << extentShift;
/* 263 */       if (length + 8L > extentSize)
/*     */       {
/* 265 */         throw new ChunkedMemoryMappedFile.ChunkLengthOutOfRange(ChunkedMemoryMappedFile.this, this, 0, extentSize - 8);
/*     */       }
/* 267 */       int bufferIndex = (int)(offset >>> extentShift);
/*     */ 
/* 269 */       int bufferOffset = (int)offset & (1 << extentShift) - 4;
/* 270 */       MappedByteBuffer[] extents = ChunkedMemoryMappedFile.this.m_extents;
/* 271 */       if (bufferIndex > extents.length)
/*     */       {
/* 273 */         String msg = "Can't extend file by more than one extent to save this chunk, at offset 0x" + Long.toString(offset, 16);
/*     */ 
/* 276 */         throw new IOException(msg);
/*     */       }
/* 278 */       if (bufferIndex == extents.length)
/*     */       {
/* 281 */         ChunkedMemoryMappedFile.this.extend();
/* 282 */         extents = ChunkedMemoryMappedFile.this.m_extents;
/*     */       }
/*     */ 
/* 285 */       MappedByteBuffer extent = extents[bufferIndex];
/* 286 */       extent.putInt(bufferOffset, this.m_chunkType);
/* 287 */       bufferOffset += 4;
/* 288 */       if (bufferOffset >= extentSize)
/*     */       {
/* 291 */         if (++bufferIndex >= extents.length)
/*     */         {
/* 293 */           ChunkedMemoryMappedFile.this.extend();
/* 294 */           extents = ChunkedMemoryMappedFile.this.m_extents;
/*     */         }
/* 296 */         extent = extents[bufferIndex];
/* 297 */         bufferOffset = 0;
/*     */       }
/* 299 */       extent.putInt(bufferOffset, this.m_chunkLength);
/* 300 */       bufferOffset = (int)(bufferOffset + length);
/* 301 */       if ((bufferOffset <= extentSize) || 
/* 304 */         (++bufferIndex < extents.length))
/*     */         return;
/* 306 */       ChunkedMemoryMappedFile.this.extend();
/*     */     }
/*     */   }
/*     */ 
/*     */   public class NumberOutOfRange extends IOException
/*     */   {
/*     */     public final ChunkedMemoryMappedFile.Chunk m_chunk;
/*     */     public final int m_offsetIntoChunk;
/*     */     public final String m_valueDescription;
/*     */     public final Number m_value;
/*     */     public final Number m_minValue;
/*     */     public final Number m_maxValue;
/*     */ 
/*     */     public NumberOutOfRange(ChunkedMemoryMappedFile.Chunk chunk, int offsetIntoChunk, Number value, Number minValue, Number maxValue)
/*     */     {
/* 180 */       super(ChunkedMemoryMappedFile.this.createMessageForException(chunk, offsetIntoChunk, new Object[] { "value out of range: ", value, " (expected ", minValue, " .. ", maxValue, ")" }));
/*     */ 
/* 182 */       this.m_chunk = chunk;
/* 183 */       this.m_offsetIntoChunk = offsetIntoChunk;
/* 184 */       this.m_valueDescription = null;
/* 185 */       this.m_value = value;
/* 186 */       this.m_minValue = minValue;
/* 187 */       this.m_maxValue = maxValue;
/*     */     }
/*     */ 
/*     */     public NumberOutOfRange(ChunkedMemoryMappedFile.Chunk chunk, int offsetIntoChunk, String description, Number v, Number min, Number max)
/*     */     {
/* 192 */       super(ChunkedMemoryMappedFile.this.createMessageForException(chunk, offsetIntoChunk, new Object[] { "value for ", description, " out of range: ", v, " (expected ", min, " .. ", max, ")" }));
/*     */ 
/* 194 */       this.m_chunk = chunk;
/* 195 */       this.m_offsetIntoChunk = offsetIntoChunk;
/* 196 */       this.m_valueDescription = description;
/* 197 */       this.m_value = v;
/* 198 */       this.m_minValue = min;
/* 199 */       this.m_maxValue = max;
/*     */     }
/*     */   }
/*     */ 
/*     */   public class ChunkLengthOutOfRange extends IOException
/*     */   {
/*     */     public final ChunkedMemoryMappedFile.Chunk m_chunk;
/*     */     public final int m_chunkLengthMinimum;
/*     */     public final int m_chunkLengthMaximum;
/*     */ 
/*     */     public ChunkLengthOutOfRange(ChunkedMemoryMappedFile.Chunk chunk, int chunkLengthMinimum)
/*     */     {
/* 155 */       super(ChunkedMemoryMappedFile.this.createMessageForException(chunk, 4, new Object[] { "chunk length out of range: ", "ARG_HEX_INTEGER", Integer.valueOf(chunk.m_chunkLength), " (expected >= ", "ARG_HEX_INTEGER", Integer.valueOf(chunkLengthMinimum) }));
/*     */ 
/* 157 */       this.m_chunk = chunk;
/* 158 */       this.m_chunkLengthMinimum = chunkLengthMinimum;
/* 159 */       this.m_chunkLengthMaximum = 2147483647;
/*     */     }
/*     */ 
/*     */     public ChunkLengthOutOfRange(ChunkedMemoryMappedFile.Chunk chunk, int chunkLengthMinimum, int chunkLengthMaximum) {
/* 163 */       super(ChunkedMemoryMappedFile.this.createMessageForException(chunk, 4, new Object[] { "chunk length out of range: ", "ARG_HEX_INTEGER", Integer.valueOf(chunk.m_chunkLength), " (expected ", "ARG_HEX_INTEGER", Integer.valueOf(chunkLengthMinimum), " .. ", "ARG_HEX_INTEGER", Integer.valueOf(chunkLengthMaximum) }));
/*     */ 
/* 166 */       this.m_chunk = chunk;
/* 167 */       this.m_chunkLengthMinimum = chunkLengthMinimum;
/* 168 */       this.m_chunkLengthMaximum = chunkLengthMaximum;
/*     */     }
/*     */   }
/*     */ 
/*     */   public class ChunkTypeMismatch extends IOException
/*     */   {
/*     */     public final ChunkedMemoryMappedFile.Chunk m_chunk;
/*     */     public final int m_badChunkType;
/*     */     public final int m_expectedChunkType;
/*     */     public final long m_offset;
/*     */ 
/*     */     public ChunkTypeMismatch(ChunkedMemoryMappedFile.Chunk chunk, int badChunkType)
/*     */     {
/* 131 */       super(ChunkedMemoryMappedFile.this.createMessageForException(chunk, 0, new Object[] { "chunk type mismatch: ", "ARG_FOURCC", Integer.valueOf(badChunkType), "(expected ", "ARG_FOURCC", Integer.valueOf(chunk.m_chunkType), ")" }));
/*     */ 
/* 133 */       this.m_chunk = chunk;
/* 134 */       this.m_badChunkType = badChunkType;
/* 135 */       this.m_expectedChunkType = chunk.m_chunkType;
/* 136 */       this.m_offset = chunk.m_chunkOffset;
/*     */     }
/*     */ 
/*     */     public ChunkTypeMismatch(int chunkType, int expectedChunkType, long offset) {
/* 140 */       super(ChunkedMemoryMappedFile.this.createMessageForException(offset, new Object[] { "chunk type mismatch: ", "ARG_FOURCC", Integer.valueOf(chunkType), "(expected ", "ARG_FOURCC", Integer.valueOf(expectedChunkType), ")", "ARG_AT_FILE_OFFSET" }));
/*     */ 
/* 142 */       this.m_chunk = null;
/* 143 */       this.m_badChunkType = chunkType;
/* 144 */       this.m_expectedChunkType = expectedChunkType;
/* 145 */       this.m_offset = offset;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.ChunkedMemoryMappedFile
 * JD-Core Version:    0.5.4
 */