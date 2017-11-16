/*     */ package intradoc.io.zip;
/*     */ 
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.OutputStreamIdcByteHandler;
/*     */ import intradoc.util.MapUtils;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Collections;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcZipOutputStream extends IdcZipHandler
/*     */ {
/*     */   public OutputStreamIdcByteHandler m_stream;
/*     */   protected long m_offsetPtr;
/*     */ 
/*     */   public IdcZipOutputStream(OutputStream stream)
/*     */   {
/*  46 */     OutputStreamIdcByteHandler out = new OutputStreamIdcByteHandler(stream);
/*  47 */     this.m_bytes = out;
/*  48 */     this.m_stream = out;
/*     */   }
/*     */ 
/*     */   public void init(IdcZipEnvironment zipenv)
/*     */   {
/*  55 */     this.m_zipenv = zipenv;
/*  56 */     if (null != this.m_entries)
/*     */     {
/*  59 */       return;
/*     */     }
/*  61 */     if (null == this.m_encoding)
/*     */     {
/*  63 */       this.m_encoding = "UTF-8";
/*     */     }
/*  65 */     this.m_formatter = zipenv.m_formatter;
/*  66 */     if (null == this.m_formatter)
/*     */     {
/*  68 */       this.m_formatter = new IdcZipFileFormatter();
/*     */     }
/*     */ 
/*  71 */     this.m_entries = MapUtils.createConcurrentMap();
/*  72 */     this.m_directories = Collections.synchronizedSet(new HashSet());
/*     */   }
/*     */ 
/*     */   public IdcZipEntry getEntry(String pathname, int flags)
/*     */     throws IdcByteHandlerException
/*     */   {
/*  82 */     throw new IdcByteHandlerException(1);
/*     */   }
/*     */ 
/*     */   public void putEntry(IdcZipEntry entry, int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/*  93 */     if (this.m_isFinished)
/*     */     {
/*  95 */       throw new IdcZipException("syZipFileAlreadyFinished", new Object[0]);
/*     */     }
/*  97 */     if (flags == -1)
/*     */     {
/*  99 */       flags = (this.m_zipenv != null) ? this.m_zipenv.m_defaultFlags : 0;
/*     */     }
/* 101 */     if ((!entry.m_isDirectory) && (entry.m_compressionMethod == 8))
/*     */     {
/*     */       IdcZipEntry tmp62_61 = entry; tmp62_61.m_flags = (short)(tmp62_61.m_flags | 0x8);
/*     */     }
/*     */ 
/* 106 */     entry.m_headerOffset = this.m_offsetPtr;
/* 107 */     this.m_offsetPtr += saveEntry(entry, this.m_bytes, flags);
/* 108 */     String altname = trackEntryForPut(entry);
/* 109 */     this.m_entries.remove(altname);
/* 110 */     this.m_entries.put(entry.m_filename, entry);
/*     */   }
/*     */ 
/*     */   public void finish(int flags)
/*     */     throws IdcByteHandlerException, IdcZipException
/*     */   {
/* 116 */     if (this.m_isFinished)
/*     */     {
/* 118 */       throw new IdcZipException("syZipFileAlreadyFinished", new Object[0]);
/*     */     }
/* 120 */     this.m_isFinished = true;
/* 121 */     this.m_centralDirectoryOffset = this.m_offsetPtr;
/* 122 */     this.m_offsetPtr += writeCentralDirectory(flags, this.m_stream);
/*     */     try
/*     */     {
/* 125 */       this.m_stream.m_stream.flush();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 129 */       throw new IdcZipException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close()
/*     */     throws IOException
/*     */   {
/* 137 */     this.m_stream.m_stream.close();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 144 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96797 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.io.zip.IdcZipOutputStream
 * JD-Core Version:    0.5.4
 */