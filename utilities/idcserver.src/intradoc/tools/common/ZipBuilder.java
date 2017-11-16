/*     */ package intradoc.tools.common;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.io.IdcByteHandler;
/*     */ import intradoc.io.IdcByteHandlerException;
/*     */ import intradoc.io.IdcRandomAccessByteFile;
/*     */ import intradoc.io.zip.IdcZipEntry;
/*     */ import intradoc.io.zip.IdcZipException;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.io.zip.IdcZipUtils;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.zip.IdcZipFunctions;
/*     */ import java.io.Closeable;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class ZipBuilder
/*     */ {
/*     */   public static final int F_INCLUDE_DIRECTORIES = 1;
/*     */   public static final int F_REMOVE_UNINCLUDED_ENTRIES = 2;
/*     */   public final File m_file;
/*     */   public final IdcZipFile m_zip;
/*     */ 
/*     */   public ZipBuilder(File dir, String zipfilename)
/*     */   {
/*  58 */     File file = this.m_file = new File(dir, zipfilename);
/*  59 */     IdcRandomAccessByteFile handler = new IdcRandomAccessByteFile(file, 1048576);
/*  60 */     this.m_zip = new IdcZipFile(handler);
/*  61 */     this.m_zip.m_description = zipfilename;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws IdcException
/*     */   {
/*     */     try
/*     */     {
/*  74 */       this.m_zip.init(IdcZipFunctions.m_defaultZipEnvironment);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  78 */       IdcException ie = new IdcException();
/*  79 */       ie.init(t, 0, null, null);
/*  80 */       throw ie;
/*     */     }
/*     */   }
/*     */ 
/*     */   public int update(boolean isExclusive, EntriesSource[] sources)
/*     */     throws IdcException
/*     */   {
/*  96 */     IdcZipFile zip = this.m_zip;
/*  97 */     Set remainingEntries = new HashSet(this.m_zip.m_entries.keySet());
/*  98 */     int numFilesUpdated = 0;
/*  99 */     long latestTimestamp = 0L;
/*     */     try
/*     */     {
/* 103 */       for (EntriesSource source : sources)
/*     */       {
/* 105 */         if (source == null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 109 */         int numEntries = source.count();
/* 110 */         for (int e = 0; e < numEntries; ++e)
/*     */         {
/* 112 */           String entryName = source.getEntryPath(e);
/* 113 */           IdcZipEntry entry = zip.getEntry(entryName, -1);
/* 114 */           boolean isEntryChanged = (entry == null) || (source.doesEntryNeedUpdate(e, entry));
/* 115 */           if (isEntryChanged)
/*     */           {
/* 117 */             entry = source.constructEntry(e);
/* 118 */             zip.putEntry(entry, -1);
/* 119 */             ++numFilesUpdated;
/*     */           }
/* 121 */           if (entry.m_lastModified > latestTimestamp)
/*     */           {
/* 123 */             latestTimestamp = entry.m_lastModified;
/*     */           }
/* 125 */           remainingEntries.remove(entryName);
/*     */         }
/*     */       }
/* 128 */       if (isExclusive)
/*     */       {
/* 130 */         for (String entryName : remainingEntries)
/*     */         {
/* 132 */           zip.m_entries.remove(entryName);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 137 */         for (String entryName : remainingEntries)
/*     */         {
/* 139 */           IdcZipEntry entry = zip.getEntry(entryName, -1);
/* 140 */           if (entry.m_lastModified > latestTimestamp)
/*     */           {
/* 142 */             latestTimestamp = entry.m_lastModified;
/*     */           }
/*     */         }
/*     */       }
/* 146 */       zip.finish(-1);
/* 147 */       this.m_file.setLastModified(latestTimestamp);
/*     */     }
/*     */     catch (IdcException ie)
/*     */     {
/* 151 */       throw ie;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 155 */       IdcException ie = new IdcException();
/* 156 */       ie.init(t, 0, null, null);
/* 157 */       throw ie;
/*     */     }
/* 159 */     return numFilesUpdated;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 288 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99577 $";
/*     */   }
/*     */ 
/*     */   public class FileEntries
/*     */     implements ZipBuilder.EntriesSource
/*     */   {
/*     */     public File m_topDir;
/*     */     public List<String> m_filelist;
/*     */     public String m_zipEntryPrefix;
/*     */ 
/*     */     public FileEntries(List<String> topDir)
/*     */     {
/* 206 */       this(ZipBuilder.this, topDir, filelist, null);
/*     */     }
/*     */ 
/*     */     public FileEntries(List<String> topDir, String filelist)
/*     */     {
/* 211 */       this.m_topDir = topDir;
/* 212 */       this.m_filelist = filelist;
/* 213 */       this.m_zipEntryPrefix = zipEntryPrefix;
/*     */     }
/*     */ 
/*     */     public int count()
/*     */     {
/* 219 */       return this.m_filelist.size();
/*     */     }
/*     */ 
/*     */     public String getEntryPath(int index)
/*     */     {
/* 224 */       String filename = (String)this.m_filelist.get(index);
/* 225 */       return (this.m_zipEntryPrefix != null) ? this.m_zipEntryPrefix + filename : filename;
/*     */     }
/*     */ 
/*     */     public boolean doesEntryNeedUpdate(int index, IdcZipEntry entry)
/*     */     {
/* 230 */       String filename = (String)this.m_filelist.get(index);
/* 231 */       File file = new File(this.m_topDir, filename);
/* 232 */       long lastModified = file.lastModified();
/* 233 */       if (lastModified == 0L)
/*     */       {
/* 235 */         return true;
/*     */       }
/*     */ 
/* 238 */       return lastModified > entry.m_lastModified + 2000L;
/*     */     }
/*     */ 
/*     */     public IdcZipEntry constructEntry(int index) throws IdcException
/*     */     {
/* 243 */       String filename = (String)this.m_filelist.get(index);
/* 244 */       File file = new File(this.m_topDir, filename);
/* 245 */       boolean isDirectory = file.isDirectory();
/* 246 */       if (isDirectory != filename.endsWith("/"))
/*     */       {
/* 248 */         String message = "directory entry doesn't end in slash: " + filename;
/* 249 */         throw new DataException(message);
/*     */       }
/* 251 */       String entryname = (this.m_zipEntryPrefix != null) ? this.m_zipEntryPrefix + filename : filename;
/* 252 */       String filepath = file.getPath();
/*     */       try
/*     */       {
/* 255 */         IdcZipEntry entry = IdcZipFunctions.newIdcZipEntry(ZipBuilder.this.m_zip, filepath, entryname, null);
/* 256 */         if (!isDirectory)
/*     */         {
/* 258 */           IdcZipUtils.deflateEntry(IdcZipFunctions.m_defaultZipEnvironment, entry, null);
/* 259 */           IdcByteHandler handler = entry.m_bytesUncompressed;
/* 260 */           if (handler instanceof Closeable)
/*     */           {
/* 262 */             Closeable closer = (Closeable)handler;
/*     */             try
/*     */             {
/* 265 */               closer.close();
/*     */             }
/*     */             catch (IOException ioe)
/*     */             {
/* 269 */               throw new ServiceException(ioe);
/*     */             }
/*     */           }
/*     */         }
/* 273 */         return entry;
/*     */       }
/*     */       catch (IdcByteHandlerException ibhe)
/*     */       {
/* 277 */         throw new ServiceException(ibhe);
/*     */       }
/*     */       catch (IdcZipException ize)
/*     */       {
/* 281 */         throw new ServiceException(ize);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static abstract interface EntriesSource
/*     */   {
/*     */     public abstract int count();
/*     */ 
/*     */     public abstract String getEntryPath(int paramInt);
/*     */ 
/*     */     public abstract boolean doesEntryNeedUpdate(int paramInt, IdcZipEntry paramIdcZipEntry);
/*     */ 
/*     */     public abstract IdcZipEntry constructEntry(int paramInt)
/*     */       throws IdcException;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.common.ZipBuilder
 * JD-Core Version:    0.5.4
 */