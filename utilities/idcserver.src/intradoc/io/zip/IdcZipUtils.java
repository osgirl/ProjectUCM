/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcByteHandlerInputStream;
/*     */ import intradoc.io.IdcRandomAccessByteArray;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.InputStream;
/*     */ 
/*     */ public class IdcZipUtils
/*     */ {
/*     */   public static long extractEntry(IdcZipEnvironment zipenv, IdcZipEntry entry, IdcByteHandler target)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/*  52 */     if (entry.m_isDirectory)
/*     */     {
/*  54 */       IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/*  55 */       msg.m_prior = new IdcMessage("syZipEntryExtractDirectory", new Object[0]);
/*  56 */       throw new IdcZipException(msg);
/*     */     }
/*  58 */     if (null == target)
/*     */     {
/*  60 */       target = entry.m_bytesUncompressed;
/*     */     }
/*  62 */     long sizeUncompressed = entry.m_sizeUncompressed;
/*  63 */     if (null == target)
/*     */     {
/*  65 */       if (sizeUncompressed > 2147483647L)
/*     */       {
/*  67 */         IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/*  68 */         msg.m_prior = new IdcMessage("syZipFormatEntryLengthBad", new Object[] { entry.m_filename });
/*  69 */         throw new IdcZipException(msg);
/*     */       }
/*  71 */       target = new IdcRandomAccessByteArray((int)sizeUncompressed);
/*  72 */       entry.m_bytesUncompressed = target;
/*     */     }
/*  74 */     short compressionMethod = entry.m_compressionMethod;
/*  75 */     IdcZipCompressor compressor = zipenv.getCompressor(compressionMethod);
/*  76 */     if (compressor == null)
/*     */     {
/*  78 */       throw new IdcZipException("syZipFormatCompressionMethodUnsupported", new Object[] { entry.m_filename, Short.valueOf(compressionMethod) });
/*     */     }
/*     */ 
/*  82 */     IdcZipCRCByteWrapper wrapper = new IdcZipCRCByteWrapper(target);
/*  83 */     IdcByteHandler compressed = entry.m_bytesCompressed;
/*  84 */     boolean isSourceSeekable = (compressed.getSupportedFeatures() & 0x200) != 0;
/*     */ 
/*  86 */     long oldSourcePosition = compressed.getPosition();
/*     */     long numBytes;
/*     */     try
/*     */     {
/*  89 */       numBytes = compressor.decompress(compressed, wrapper);
/*     */     }
/*     */     finally
/*     */     {
/*  93 */       zipenv.putCompressor(compressionMethod, compressor);
/*     */     }
/*  95 */     target.markIsDirty(false);
/*  96 */     if (numBytes != sizeUncompressed)
/*     */     {
/*  98 */       IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/*  99 */       msg.m_prior = new IdcMessage("syZipMismatchSizeUncompressed", new Object[] { Long.valueOf(sizeUncompressed), Long.valueOf(numBytes) });
/* 100 */       throw new IdcZipException(msg);
/*     */     }
/* 102 */     int crc = wrapper.getValue();
/* 103 */     if (entry.m_crc32 != crc)
/*     */     {
/* 105 */       IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/* 106 */       msg.m_prior = new IdcMessage("syZipMismatchCRC", new Object[] { Integer.valueOf(entry.m_crc32), Integer.valueOf(crc) });
/* 107 */       throw new IdcZipException(msg);
/*     */     }
/*     */ 
/* 110 */     if (isSourceSeekable)
/*     */     {
/* 112 */       compressed.setPosition(oldSourcePosition);
/*     */     }
/*     */ 
/* 115 */     return numBytes;
/*     */   }
/*     */ 
/*     */   public static InputStream extractEntryAsInputStream(IdcZipEnvironment zipenv, IdcZipEntry entry)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 130 */     IdcByteHandler handler = entry.m_bytesUncompressed;
/* 131 */     if ((handler == null) || ((handler.getSupportedFeatures() & 0x200) == 0))
/*     */     {
/* 133 */       IdcByteHandler byteArray = new IdcRandomAccessByteArray((int)entry.m_sizeUncompressed);
/*     */ 
/* 135 */       extractEntry(zipenv, entry, byteArray);
/* 136 */       if (handler == null)
/*     */       {
/* 138 */         entry.m_bytesUncompressed = handler;
/*     */       }
/* 140 */       handler = byteArray;
/*     */     }
/* 142 */     handler.setPosition(0L);
/* 143 */     return new IdcByteHandlerInputStream(handler);
/*     */   }
/*     */ 
/*     */   public static long deflateEntry(IdcZipEnvironment zipenv, IdcZipEntry entry, IdcByteHandler source)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 163 */     if (entry.m_isDirectory)
/*     */     {
/* 165 */       IdcMessage msg = new IdcMessage("syZipEntryDeflateError", new Object[] { entry.m_filename });
/* 166 */       msg.m_prior = new IdcMessage("syZipEntryDeflateDirectory", new Object[0]);
/* 167 */       throw new IdcZipException(msg);
/*     */     }
/* 169 */     if (null == source)
/*     */     {
/* 171 */       source = entry.m_bytesUncompressed;
/*     */     }
/* 173 */     if (null == source)
/*     */     {
/* 175 */       IdcMessage msg = new IdcMessage("syZipEntryDeflateError", new Object[] { entry.m_filename });
/* 176 */       msg.m_prior = new IdcMessage("syNullPointerException", new Object[0]);
/* 177 */       throw new IdcZipException(msg);
/*     */     }
/* 179 */     if (entry.m_sizeUncompressed > 2147483647L)
/*     */     {
/* 181 */       IdcMessage msg = new IdcMessage("syZipEntryExtractError", new Object[] { entry.m_filename });
/* 182 */       msg.m_prior = new IdcMessage("syZipFormatEntryLengthBad", new Object[] { entry.m_filename });
/* 183 */       throw new IdcZipException(msg);
/*     */     }
/* 185 */     IdcByteHandler compressed = new IdcRandomAccessByteArray((int)entry.m_sizeUncompressed);
/* 186 */     long initialPosition = source.getPosition();
/* 187 */     IdcZipCRCByteWrapper wrapper = new IdcZipCRCByteWrapper(source);
/*     */ 
/* 191 */     short compressionMethod = 8;
/* 192 */     IdcZipCompressor compressor = zipenv.getCompressor(compressionMethod);
/* 193 */     if (compressor == null)
/*     */     {
/* 195 */       throw new IdcZipException("syZipFormatCompressionMethodUnsupported", new Object[] { entry.m_filename, Short.valueOf(compressionMethod) });
/*     */     }
/*     */     long numBytes;
/*     */     try
/*     */     {
/* 200 */       numBytes = compressor.compress(wrapper, compressed);
/*     */     }
/*     */     finally
/*     */     {
/* 204 */       zipenv.putCompressor(8, compressor);
/*     */     }
/*     */     int i;
/* 206 */     int i = ((source.getSupportedFeatures() & 0x200) != 0) ? 1 : 0;
/*     */ 
/* 208 */     if ((i != 0) && (numBytes >= entry.m_sizeUncompressed + 5L))
/*     */     {
/* 210 */       compressed.setPosition(0L);
/* 211 */       source.setPosition(initialPosition);
/* 212 */       wrapper.reset();
/*     */ 
/* 214 */       compressionMethod = 0;
/* 215 */       compressor = zipenv.getCompressor(compressionMethod);
/* 216 */       if (compressor == null)
/*     */       {
/* 218 */         throw new IdcZipException("syZipFormatCompressionMethodUnsupported", new Object[] { entry.m_filename, Short.valueOf(compressionMethod) });
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 223 */         numBytes = compressor.compress(wrapper, compressed);
/*     */       }
/*     */       finally
/*     */       {
/* 227 */         zipenv.putCompressor(0, compressor);
/*     */       }
/*     */     }
/*     */ 
/* 231 */     compressed.setSize(numBytes);
/* 232 */     compressed.setPosition(0L);
/* 233 */     entry.m_bytesCompressed = compressed;
/* 234 */     entry.m_sizeCompressed = numBytes;
/* 235 */     entry.m_compressionMethod = compressionMethod;
/* 236 */     entry.m_crc32 = wrapper.getValue();
/* 237 */     return numBytes;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 244 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98694 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipUtils
 * JD-Core Version:    0.5.4
 */