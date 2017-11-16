/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcNullByteHandler;
/*     */ import intradoc.io.IdcRandomAccessByteArray;
/*     */ import intradoc.io.IdcRandomAccessByteFile;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.Collections;
/*     */ import java.util.HashSet;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class IdcZipFile extends IdcZipHandler
/*     */ {
/*     */   protected long m_zipfileLength;
/*     */   public Map m_unloadedEntries;
/*     */   protected String m_filename;
/*     */   protected IdcByteHandler m_bytesTemp;
/*     */   protected Map m_entriesSaved;
/*     */   protected List<Closeable> m_closeWhenFinished;
/*     */ 
/*     */   protected IdcZipFile()
/*     */   {
/*  69 */     this.m_closeWhenFinished = new ArrayList();
/*     */   }
/*     */ 
/*     */   public IdcZipFile(String filename)
/*     */   {
/*  75 */     this.m_bytes = new IdcRandomAccessByteFile(filename);
/*  76 */     this.m_filename = filename;
/*  77 */     this.m_description = filename;
/*     */   }
/*     */ 
/*     */   public IdcZipFile(File file)
/*     */   {
/*  83 */     this.m_bytes = new IdcRandomAccessByteFile(file);
/*     */     try
/*     */     {
/*  86 */       this.m_filename = file.getCanonicalPath();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*  90 */       IdcZipException z = new IdcZipException(e, "syZipNoCanonicalPath", new Object[0]);
/*  91 */       this.m_zipenv.m_trace.report(3, new Object[] { z });
/*     */     }
/*  93 */     this.m_description = this.m_filename;
/*     */   }
/*     */ 
/*     */   public IdcZipFile(IdcByteHandler handler)
/*     */   {
/*  99 */     this.m_bytes = handler;
/* 100 */     if (!handler instanceof IdcRandomAccessByteFile)
/*     */       return;
/* 102 */     IdcRandomAccessByteFile f = (IdcRandomAccessByteFile)handler;
/* 103 */     if (null == f.m_file)
/*     */       return;
/*     */     try
/*     */     {
/* 107 */       this.m_filename = f.m_file.getCanonicalPath();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 111 */       IdcZipException z = new IdcZipException(e, "syZipNoCanonicalPath", new Object[0]);
/* 112 */       this.m_zipenv.m_trace.report(3, new Object[] { z });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(IdcZipEnvironment zipenv)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 130 */     this.m_zipenv = zipenv;
/* 131 */     int flags = (this.m_zipenv != null) ? this.m_zipenv.m_defaultFlags : 0;
/* 132 */     if (null != this.m_entries)
/*     */     {
/* 135 */       return;
/*     */     }
/* 137 */     if (null == this.m_encoding)
/*     */     {
/* 139 */       this.m_encoding = "UTF-8";
/*     */     }
/* 141 */     this.m_formatter = this.m_zipenv.m_formatter;
/* 142 */     if (null == this.m_formatter)
/*     */     {
/* 144 */       this.m_formatter = new IdcZipFileFormatter();
/*     */     }
/* 146 */     this.m_bytesFeatures = this.m_bytes.getSupportedFeatures();
/* 147 */     int requiredFeatures = 513;
/* 148 */     if (this.m_bytesFeatures != (this.m_bytesFeatures | requiredFeatures))
/*     */     {
/* 150 */       throw new IdcByteHandlerException(requiredFeatures);
/*     */     }
/* 152 */     if (this.m_bytes instanceof IdcRandomAccessByteFile)
/*     */     {
/* 154 */       ((IdcRandomAccessByteFile)this.m_bytes).init();
/*     */     }
/* 156 */     this.m_zipfileLength = this.m_bytes.getSize();
/*     */ 
/* 159 */     this.m_entries = MapUtils.createSynchronizedMap(0);
/* 160 */     this.m_unloadedEntries = MapUtils.createSynchronizedMap(0);
/* 161 */     this.m_directories = Collections.synchronizedSet(new HashSet());
/* 162 */     if (this.m_zipfileLength <= 0L)
/*     */       return;
/* 164 */     int numEntries = this.m_formatter.locateCentralDirectory(this, flags);
/* 165 */     long position = this.m_centralDirectoryOffset;
/* 166 */     for (int entryNum = 0; entryNum < numEntries; ++entryNum)
/*     */     {
/* 168 */       if (position >= this.m_centralDirectoryOffset + this.m_centralDirectoryLength)
/*     */       {
/* 170 */         IdcZipException z = new IdcZipException("syZipFormatCentralDirectoryOverlap", new Object[0]);
/* 171 */         this.m_zipenv.m_trace.report(3, new Object[] { z });
/* 172 */         return;
/*     */       }
/* 174 */       this.m_bytes.setPosition(position);
/*     */       IdcZipEntry entry;
/*     */       try
/*     */       {
/* 178 */         entry = this.m_formatter.loadEntryFromCentralDirectory(this, flags, 0);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 182 */         IdcZipException z = new IdcZipException(e, "syZipIgnoreRemainingEntries", new Object[0]);
/* 183 */         this.m_zipenv.m_trace.report(3, new Object[] { z });
/* 184 */         return;
/*     */       }
/* 186 */       position = this.m_bytes.getPosition();
/* 187 */       if (entry.m_headerOffset + 30L >= this.m_centralDirectoryOffset)
/*     */       {
/* 189 */         IdcMessage msg = new IdcMessage("syZipFormatEntryHeaderOverlap", new Object[0]);
/* 190 */         label762: this.m_zipenv.m_trace.report(4, new Object[] { msg });
/*     */       }
/*     */       else
/*     */       {
/*     */         try {
/* 195 */           this.m_formatter.validateCentralDirectoryEntry(this, entry);
/* 196 */           this.m_bytes.setPosition(entry.m_headerOffset);
/* 197 */           IdcZipEntry local = this.m_formatter.loadEntryFromLocalFileHeader(this, flags);
/* 198 */           if ((local.m_flags & 0x8) != 0)
/*     */           {
/* 200 */             this.m_bytes.setPosition(local.m_dataOffset + entry.m_sizeCompressed);
/* 201 */             this.m_formatter.loadDataDescriptor(this, local);
/*     */           }
/* 203 */           this.m_formatter.validateAndMergeLocalHeaderIntoCentralDirectoryEntry(local, entry);
/* 204 */           int extraBytes = ((entry.m_flags & 0x8) != 0) ? 16 : 0;
/* 205 */           if (entry.m_dataOffset + entry.m_sizeCompressed + extraBytes > this.m_centralDirectoryOffset)
/*     */           {
/* 207 */             IdcMessage msg = new IdcMessage("syZipFormatEntryDataOverlap", new Object[] { entry.m_filename });
/* 208 */             this.m_zipenv.m_trace.report(4, new Object[] { msg });
/* 209 */             break label762:
/*     */           }
/* 211 */           if (!entry.m_isDirectory)
/*     */           {
/* 214 */             setCompressedHandlerForEntry(entry, this.m_bytes);
/*     */           }
/*     */         }
/*     */         catch (IdcZipException e)
/*     */         {
/* 219 */           IdcMessage msg = new IdcMessage("syZipEntrySkipped", new Object[0]);
/* 220 */           this.m_zipenv.m_trace.report(4, new Object[] { e, msg });
/* 221 */           break label762:
/*     */         }
/* 223 */         entry.m_string = null;
/* 224 */         String altname = trackEntryForPut(entry);
/* 225 */         this.m_entries.remove(altname);
/* 226 */         this.m_entries.put(entry.m_filename, entry);
/* 227 */         if (entry.m_isDirectory)
/*     */           continue;
/* 229 */         this.m_unloadedEntries.put(entry.m_filename, entry);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public IdcZipEntry getEntry(String pathname, int flags)
/*     */   {
/* 239 */     if (null == this.m_entries)
/*     */     {
/* 241 */       return null;
/*     */     }
/* 243 */     IdcZipEntry entry = (IdcZipEntry)this.m_entries.get(pathname);
/* 244 */     return entry;
/*     */   }
/*     */ 
/*     */   public void putEntry(IdcZipEntry entry, int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 252 */     if (this.m_isFinished)
/*     */     {
/* 254 */       throw new IdcZipException("syZipFileAlreadyFinished", new Object[0]);
/*     */     }
/* 256 */     if (flags == -1)
/*     */     {
/* 258 */       flags = (this.m_zipenv != null) ? this.m_zipenv.m_defaultFlags : 0;
/*     */     }
/*     */     IdcZipEntry tmp46_45 = entry; tmp46_45.m_flags = (short)(tmp46_45.m_flags & 0xFFFFFFF7);
/* 262 */     if (null == this.m_bytesTemp)
/*     */     {
/* 264 */       if (null == this.m_filename)
/*     */       {
/* 266 */         if (this.m_bytes instanceof IdcRandomAccessByteArray)
/*     */         {
/* 268 */           IdcRandomAccessByteArray array = (IdcRandomAccessByteArray)this.m_bytes;
/* 269 */           byte[] bytes = new byte[array.m_length];
/* 270 */           this.m_bytesTemp = new IdcRandomAccessByteArray(bytes);
/* 271 */           this.m_entriesSaved = MapUtils.createSynchronizedMap(0);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 276 */         IdcRandomAccessByteFile file = new IdcRandomAccessByteFile(this.m_filename + ".tmp");
/* 277 */         file.init();
/* 278 */         this.m_bytesTemp = file;
/* 279 */         this.m_entriesSaved = MapUtils.createSynchronizedMap(0);
/*     */       }
/*     */     }
/* 282 */     if (null != this.m_bytesTemp)
/*     */     {
/*     */       try
/*     */       {
/* 286 */         entry.m_headerOffset = this.m_bytesTemp.getPosition();
/* 287 */         saveEntry(entry, this.m_bytesTemp, flags);
/* 288 */         if (!entry.m_isDirectory)
/*     */         {
/* 290 */           setCompressedHandlerForEntry(entry, this.m_bytesTemp);
/* 291 */           byte[] header = this.m_formatter.makeEntryHeader(entry, flags, 0);
/* 292 */           this.m_bytesTemp.writeTo(entry.m_headerOffset, header, 0, header.length);
/*     */         }
/*     */       }
/*     */       catch (IdcByteHandlerException e)
/*     */       {
/* 297 */         if (this.m_bytesTemp instanceof IdcRandomAccessByteFile)
/*     */         {
/* 299 */           IdcRandomAccessByteFile file = (IdcRandomAccessByteFile)this.m_bytesTemp;
/*     */           try
/*     */           {
/* 302 */             file.close();
/*     */           }
/*     */           catch (IOException ioe)
/*     */           {
/* 306 */             throw new IdcByteHandlerException(ioe);
/*     */           }
/*     */         }
/* 309 */         this.m_bytesTemp = null;
/* 310 */         throw e;
/*     */       }
/* 312 */       String altname = trackEntryForPut(entry);
/* 313 */       this.m_entries.remove(altname);
/* 314 */       this.m_entriesSaved.remove(altname);
/* 315 */       this.m_entries.remove(entry.m_filename);
/* 316 */       this.m_entriesSaved.put(entry.m_filename, entry);
/* 317 */       return;
/*     */     }
/* 319 */     throw new IdcZipException("syZipFileInplaceModifyUnsupported", new Object[0]);
/*     */   }
/*     */ 
/*     */   public void copyRemainingEntries(int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 333 */     if (this.m_isFinished)
/*     */     {
/* 335 */       throw new IdcZipException("syZipFileAlreadyFinished", new Object[0]);
/*     */     }
/* 337 */     if (flags == -1)
/*     */     {
/* 339 */       flags = (this.m_zipenv != null) ? this.m_zipenv.m_defaultFlags : 0;
/*     */     }
/* 341 */     if (null == this.m_bytesTemp)
/*     */       return;
/*     */     try
/*     */     {
/* 345 */       Iterator iter = this.m_entries.values().iterator();
/* 346 */       while (iter.hasNext())
/*     */       {
/* 348 */         IdcZipEntry entry = (IdcZipEntry)iter.next();
/* 349 */         entry.m_headerOffset = this.m_bytesTemp.getPosition();
/*     */         IdcZipEntry tmp101_100 = entry; tmp101_100.m_flags = (short)(tmp101_100.m_flags & 0xFFFFFFF7);
/* 351 */         saveEntry(entry, this.m_bytesTemp, flags);
/* 352 */         if (!entry.m_isDirectory)
/*     */         {
/* 354 */           setCompressedHandlerForEntry(entry, this.m_bytesTemp);
/*     */         }
/* 356 */         this.m_entriesSaved.put(entry.m_filename, entry);
/*     */       }
/*     */     }
/*     */     catch (IdcByteHandlerException e)
/*     */     {
/* 361 */       if (this.m_bytesTemp instanceof IdcRandomAccessByteFile)
/*     */       {
/*     */         try
/*     */         {
/* 365 */           ((IdcRandomAccessByteFile)this.m_bytesTemp).close();
/*     */         }
/*     */         catch (IOException ioe)
/*     */         {
/* 369 */           throw new IdcByteHandlerException(ioe);
/*     */         }
/*     */       }
/* 372 */       this.m_bytesTemp = null;
/* 373 */       throw e;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcByteHandler setCompressedHandlerForEntry(IdcZipEntry entry, IdcByteHandler bytes)
/*     */     throws IdcByteHandlerException
/*     */   {
/* 389 */     long sizeCompressed = entry.m_sizeCompressed;
/*     */     IdcByteHandler handler;
/*     */     IdcByteHandler handler;
/* 391 */     if (sizeCompressed == 0L)
/*     */     {
/* 393 */       handler = new IdcNullByteHandler();
/*     */     }
/*     */     else
/*     */     {
/* 397 */       handler = bytes.shallowCloneSubrange(entry.m_dataOffset, sizeCompressed);
/*     */     }
/* 399 */     entry.m_bytesCompressed = handler;
/* 400 */     return handler;
/*     */   }
/*     */ 
/*     */   public void finish(int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 414 */     if (this.m_isFinished)
/*     */     {
/* 416 */       throw new IdcZipException("syZipFileAlreadyFinished", new Object[0]);
/*     */     }
/* 418 */     copyRemainingEntries(flags);
/* 419 */     this.m_isFinished = true;
/* 420 */     IdcByteHandler tempHandler = this.m_bytesTemp;
/*     */     try
/*     */     {
/* 423 */       if (tempHandler != null)
/*     */       {
/*     */         try
/*     */         {
/* 427 */           this.m_centralDirectoryOffset = this.m_bytesTemp.getPosition();
/* 428 */           this.m_entries = this.m_entriesSaved;
/* 429 */           this.m_entriesSaved = null;
/* 430 */           writeCentralDirectory(flags, this.m_bytesTemp);
/*     */         }
/*     */         catch (IdcByteHandlerException e)
/*     */         {
/* 434 */           close();
/* 435 */           throw e;
/*     */         }
/*     */ 
/* 438 */         if (tempHandler instanceof IdcRandomAccessByteFile)
/*     */         {
/* 441 */           IdcRandomAccessByteFile old = (IdcRandomAccessByteFile)this.m_bytes;
/* 442 */           IdcRandomAccessByteFile tmp = (IdcRandomAccessByteFile)tempHandler;
/*     */ 
/* 445 */           close();
/*     */ 
/* 448 */           old.m_file.delete();
/*     */ 
/* 450 */           if (!tmp.m_file.renameTo(old.m_file))
/*     */           {
/* 452 */             throw new IdcZipException("syZipFileRenameFailed", new Object[0]);
/*     */           }
/* 454 */           tmp.m_file = old.m_file;
/*     */         }
/* 456 */         this.m_bytes = tempHandler;
/* 457 */         return;
/*     */       }
/* 459 */       close();
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 463 */       throw new IdcByteHandlerException(ioe);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 471 */     synchronized (this.m_closeWhenFinished)
/*     */     {
/* 473 */       for (Closeable closeable : this.m_closeWhenFinished)
/*     */       {
/*     */         try
/*     */         {
/* 477 */           closeable.close();
/*     */         }
/*     */         catch (IOException ioe)
/*     */         {
/*     */         }
/*     */       }
/*     */ 
/* 484 */       this.m_closeWhenFinished.clear();
/*     */     }
/* 486 */     if (this.m_bytesTemp != null)
/*     */     {
/* 488 */       if (this.m_bytesTemp instanceof IdcRandomAccessByteFile)
/*     */       {
/* 490 */         ((IdcRandomAccessByteFile)this.m_bytesTemp).close();
/*     */       }
/* 492 */       this.m_bytesTemp = null;
/*     */     }
/* 494 */     if (!this.m_bytes instanceof IdcRandomAccessByteFile)
/*     */       return;
/* 496 */     ((IdcRandomAccessByteFile)this.m_bytes).close();
/*     */   }
/*     */ 
/*     */   public void addCloseable(Closeable closeable)
/*     */   {
/* 509 */     synchronized (this.m_closeWhenFinished)
/*     */     {
/* 511 */       this.m_closeWhenFinished.add(closeable);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean canBePurged()
/*     */   {
/* 520 */     if (null == this.m_unloadedEntries)
/*     */     {
/* 522 */       return true;
/*     */     }
/* 524 */     for (Iterator iter = this.m_unloadedEntries.keySet().iterator(); iter.hasNext(); )
/*     */     {
/* 526 */       IdcZipEntry entry = (IdcZipEntry)iter.next();
/* 527 */       if (entry.m_wasExtracted)
/*     */       {
/* 529 */         iter.remove();
/*     */       }
/*     */     }
/* 532 */     return this.m_unloadedEntries.isEmpty();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 538 */     if (this.m_filename != null)
/*     */     {
/* 540 */       return this.m_filename;
/*     */     }
/* 542 */     return super.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 548 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98694 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipFile
 * JD-Core Version:    0.5.4
 */