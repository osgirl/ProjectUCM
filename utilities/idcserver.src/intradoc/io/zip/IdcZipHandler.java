/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcByteHandlerUtils;
/*     */ import intradoc.util.IdcArrayAllocator;
/*     */ import java.io.Closeable;
/*     */ import java.util.Arrays;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public abstract class IdcZipHandler
/*     */   implements Closeable
/*     */ {
/*     */   public String m_description;
/*     */   public IdcZipEnvironment m_zipenv;
/*     */   public String m_encoding;
/*     */   public String m_comment;
/*     */   public Map<String, IdcZipEntry> m_entries;
/*     */   public Set<String> m_directories;
/*     */   public IdcByteHandler m_bytes;
/*     */   public int m_bytesFeatures;
/*     */   public IdcZipFileFormatter m_formatter;
/*     */   public long m_centralDirectoryOffset;
/*     */   protected long m_centralDirectoryLength;
/*     */   protected boolean m_isFinished;
/*     */ 
/*     */   public abstract void init(IdcZipEnvironment paramIdcZipEnvironment)
/*     */     throws IdcByteHandlerException, IdcZipException;
/*     */ 
/*     */   public abstract IdcZipEntry getEntry(String paramString, int paramInt)
/*     */     throws IdcByteHandlerException, IdcZipException;
/*     */ 
/*     */   public abstract void putEntry(IdcZipEntry paramIdcZipEntry, int paramInt)
/*     */     throws IdcByteHandlerException, IdcZipException;
/*     */ 
/*     */   public abstract void finish(int paramInt)
/*     */     throws IdcByteHandlerException, IdcZipException;
/*     */ 
/*     */   protected String trackEntryForPut(IdcZipEntry entry)
/*     */   {
/* 134 */     String entryName = entry.m_filename;
/*     */ 
/* 137 */     String filename = entryName;
/*     */ 
/* 139 */     while ((slash = filename.lastIndexOf(47)) > 0)
/*     */     {
/*     */       int slash;
/* 141 */       String dirname = filename.substring(0, slash + 1);
/* 142 */       this.m_directories.add(dirname);
/* 143 */       filename = filename.substring(0, slash);
/*     */     }
/*     */ 
/* 147 */     if (entryName.endsWith("/"))
/*     */     {
/* 149 */       return entryName.substring(0, entryName.length() - 1);
/*     */     }
/* 151 */     return entryName + '/';
/*     */   }
/*     */ 
/*     */   protected long saveEntry(IdcZipEntry entry, IdcByteHandler bytes, int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 169 */     boolean endsWithSlash = entry.m_filename.endsWith("/");
/* 170 */     if (endsWithSlash != entry.m_isDirectory)
/*     */     {
/* 172 */       String key = (endsWithSlash) ? "syZipBadFilename" : "syZipBadDirectoryFilename";
/* 173 */       throw new IdcZipException(key, new Object[] { entry.m_filename });
/*     */     }
/* 175 */     long numBytes = 0L;
/* 176 */     long sizeUncompressed = 0L;
/* 177 */     boolean isDirty = false;
/* 178 */     if (!entry.m_isDirectory)
/*     */     {
/* 180 */       if (null != entry.m_bytesUncompressed)
/*     */       {
/* 182 */         sizeUncompressed = entry.m_bytesUncompressed.getSize();
/* 183 */         if ((entry.m_bytesUncompressed.getSupportedFeatures() & 0x10000000) != 0)
/*     */         {
/* 185 */           isDirty = true;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 190 */         sizeUncompressed = entry.m_sizeUncompressed;
/*     */       }
/* 192 */       if (null == entry.m_bytesCompressed)
/*     */       {
/* 194 */         isDirty = true;
/*     */       }
/*     */     }
/*     */ 
/* 198 */     if (sizeUncompressed > 2147483647L)
/*     */     {
/* 200 */       throw new IdcZipException("syZipFormatEntryLengthBad", new Object[] { entry.m_filename });
/*     */     }
/* 202 */     entry.m_dataOffset = 0L;
/*     */ 
/* 204 */     if ((entry.m_flags & 0x8) != 0)
/*     */     {
/* 206 */       entry.m_sizeCompressed = 0L;
/* 207 */       entry.m_sizeUncompressed = 0L;
/*     */     }
/* 209 */     short compressionMethod = entry.m_compressionMethod;
/* 210 */     IdcZipCompressor compressor = null;
/* 211 */     if (!entry.m_isDirectory)
/*     */     {
/* 213 */       compressor = this.m_zipenv.getCompressor(compressionMethod);
/* 214 */       if (null == compressor)
/*     */       {
/* 216 */         throw new IdcZipException("syZipFormatCompressionMethodUnsupported", new Object[] { entry.m_filename, Short.valueOf(compressionMethod) });
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 223 */       this.m_formatter.updateEntryForWrite(entry, flags);
/* 224 */       byte[] header = this.m_formatter.makeEntryHeader(entry, flags, 0);
/* 225 */       numBytes += bytes.writeNext(header, 0, header.length);
/* 226 */       if (!entry.m_isDirectory)
/*     */       {
/* 228 */         entry.m_dataOffset = (entry.m_headerOffset + numBytes);
/* 229 */         entry.m_sizeUncompressed = sizeUncompressed;
/* 230 */         if (isDirty)
/*     */         {
/* 232 */           IdcZipCRCByteWrapper wrapper = new IdcZipCRCByteWrapper(entry.m_bytesUncompressed);
/* 233 */           entry.m_sizeCompressed = compressor.compress(wrapper, bytes);
/* 234 */           entry.m_bytesCompressed = null;
/* 235 */           entry.m_crc32 = wrapper.getValue();
/* 236 */           if (null != entry.m_bytesUncompressed)
/*     */           {
/* 238 */             entry.m_bytesUncompressed.markIsDirty(false);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 243 */           byte[] buffer = (byte[])(byte[])this.m_zipenv.m_allocator.getBuffer(this.m_zipenv.m_bufferSize, 0);
/*     */           try
/*     */           {
/* 247 */             entry.m_sizeCompressed = IdcByteHandlerUtils.copy(entry.m_bytesCompressed, bytes, buffer);
/*     */           }
/*     */           finally
/*     */           {
/* 252 */             this.m_zipenv.m_allocator.releaseBuffer(buffer);
/*     */           }
/*     */         }
/* 255 */         numBytes += entry.m_sizeCompressed;
/* 256 */         if ((entry.m_flags & 0x8) != 0)
/*     */         {
/* 258 */           byte[] desc = this.m_formatter.makeEntryDataDescriptor(entry);
/* 259 */           numBytes += bytes.writeNext(desc, 0, desc.length);
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 265 */       if (compressor != null)
/*     */       {
/* 267 */         this.m_zipenv.putCompressor(compressionMethod, compressor);
/*     */       }
/*     */     }
/* 270 */     return numBytes;
/*     */   }
/*     */ 
/*     */   protected int writeCentralDirectory(int flags, IdcByteHandler output)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 285 */     if (flags == -1)
/*     */     {
/* 287 */       flags = (this.m_zipenv != null) ? this.m_zipenv.m_defaultFlags : 0;
/*     */     }
/* 289 */     int numEntries = this.m_entries.size();
/* 290 */     String[] entryNames = new String[numEntries];
/* 291 */     entryNames = (String[])this.m_entries.keySet().toArray(entryNames);
/* 292 */     numEntries = entryNames.length;
/* 293 */     Arrays.sort(entryNames);
/* 294 */     this.m_centralDirectoryLength = 0L;
/* 295 */     for (int e = 0; e < numEntries; ++e)
/*     */     {
/* 297 */       IdcZipEntry entry = (IdcZipEntry)this.m_entries.get(entryNames[e]);
/* 298 */       byte[] header = this.m_formatter.makeEntryHeader(entry, flags, 2);
/* 299 */       output.writeNext(header, 0, header.length);
/* 300 */       this.m_centralDirectoryLength += header.length;
/*     */     }
/* 302 */     byte[] end = this.m_formatter.makeEndOfCentralDirectoryRecord(this, flags);
/* 303 */     output.writeNext(end, 0, end.length);
/* 304 */     return (int)this.m_centralDirectoryLength + end.length;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 311 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89526 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipHandler
 * JD-Core Version:    0.5.4
 */