/*     */ package intradoc.resource;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceTrace;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStream;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.Reader;
/*     */ import java.io.Writer;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ResourceUtils
/*     */ {
/*     */   public static final int F_IS_READ = 0;
/*     */   public static final int F_IS_WRITE = 1;
/*     */   public static final int F_IS_HEADER_ONLY = 2;
/*     */   public static final int F_MUST_EXIST = 4;
/*     */   public static final int F_MUST_CREATE = 8;
/*     */   public static final int F_IGNORE_RENAME_ERRORS = 16;
/*     */   public static final int F_NULL_DIRECTORY = 32;
/*     */ 
/*     */   public static boolean serializeDataBinder(String dir, String file, DataBinder data, boolean isWrite, boolean mustExist)
/*     */     throws ServiceException
/*     */   {
/*  55 */     int flags = 0;
/*  56 */     if (isWrite)
/*     */     {
/*  58 */       flags |= 1;
/*     */     }
/*  60 */     if (mustExist)
/*     */     {
/*  62 */       flags |= 4;
/*     */     }
/*  64 */     return serializeDataBinderWithEncoding(dir, file, data, flags, null);
/*     */   }
/*     */ 
/*     */   public static boolean serializeDataBinder(File file, DataBinder data, boolean isWrite, boolean mustExist)
/*     */     throws ServiceException
/*     */   {
/*  70 */     int flags = 0;
/*  71 */     if (isWrite)
/*     */     {
/*  73 */       flags |= 1;
/*     */     }
/*  75 */     if (mustExist)
/*     */     {
/*  77 */       flags |= 4;
/*     */     }
/*  79 */     return serializeDataBinderWithEncoding(file, data, flags, null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean serializeDataBinderEx(String dir, String file, DataBinder data, boolean isWrite, boolean mustExist, boolean isHeaderOnly)
/*     */     throws ServiceException
/*     */   {
/*  89 */     String encoding = null;
/*  90 */     if (isWrite)
/*     */     {
/*  93 */       encoding = DataSerializeUtils.getSystemEncoding();
/*     */     }
/*  95 */     int flags = 0;
/*  96 */     if (isWrite)
/*     */     {
/*  98 */       flags |= 1;
/*     */     }
/* 100 */     if (mustExist)
/*     */     {
/* 102 */       flags |= 4;
/*     */     }
/* 104 */     if (isHeaderOnly)
/*     */     {
/* 106 */       flags |= 2;
/*     */     }
/* 108 */     return serializeDataBinderWithEncoding(dir, file, data, flags, encoding);
/*     */   }
/*     */ 
/*     */   public static boolean serializeDataBinderEx(String dir, String file, DataBinder data, int flags)
/*     */     throws ServiceException
/*     */   {
/* 119 */     return serializeDataBinderWithEncoding(dir, file, data, flags, null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean serializeDataBinderWithEncoding(String dir, String file, DataBinder data, boolean isWrite, boolean mustExist, boolean isHeaderOnly, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 130 */     int flags = 0;
/* 131 */     if (isWrite)
/*     */     {
/* 133 */       flags |= 1;
/*     */     }
/* 135 */     if (mustExist)
/*     */     {
/* 137 */       flags |= 4;
/*     */     }
/* 139 */     if (isHeaderOnly)
/*     */     {
/* 141 */       flags |= 2;
/*     */     }
/* 143 */     return serializeDataBinderWithEncoding(dir, file, data, flags, encoding);
/*     */   }
/*     */ 
/*     */   public static boolean serializeDataBinderWithEncoding(String dir, String file, DataBinder data, int flags, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 159 */     String path = null;
/* 160 */     if (dir == null)
/*     */     {
/* 162 */       if ((flags & 0x20) == 0)
/*     */       {
/* 164 */         throw new NullPointerException("!csResourceUtilsDirNotSpecified");
/*     */       }
/* 166 */       path = file;
/* 167 */       file = FileUtils.getAbsolutePath(file);
/* 168 */       dir = FileUtils.getParent(file);
/* 169 */       file = FileUtils.getName(file);
/*     */     }
/* 171 */     dir = FileUtils.directorySlashes(dir);
/* 172 */     if (path == null)
/*     */     {
/* 174 */       path = dir + file;
/*     */     }
/*     */ 
/* 177 */     String fullDir = dir;
/* 178 */     String name = file;
/*     */ 
/* 183 */     if (file.indexOf("/") >= 0)
/*     */     {
/* 185 */       fullDir = FileUtils.getDirectory(path);
/* 186 */       name = FileUtils.getName(path);
/*     */     }
/*     */ 
/* 189 */     File tempFile = FileUtilsCfgBuilder.getCfgFile(fullDir + "/__temp" + name + ".dat", null, false);
/* 190 */     File dataFile = FileUtilsCfgBuilder.getCfgFile(path, null, false);
/* 191 */     return serializeDataBinderWithEncoding(dataFile, tempFile, data, flags, encoding);
/*     */   }
/*     */ 
/*     */   public static boolean serializeDataBinderWithEncoding(File dataFile, DataBinder data, int flags, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 198 */     String fullDir = dataFile.getParent();
/* 199 */     fullDir = FileUtils.directorySlashes(fullDir);
/* 200 */     String name = dataFile.getName();
/* 201 */     File tempFile = FileUtilsCfgBuilder.getCfgFile(fullDir + "/__temp" + name + ".dat", null, false);
/* 202 */     return serializeDataBinderWithEncoding(dataFile, tempFile, data, flags, encoding);
/*     */   }
/*     */ 
/*     */   public static boolean serializeDataBinderWithEncoding(File dataFile, File tempFile, DataBinder data, int flags, String encoding)
/*     */     throws ServiceException
/*     */   {
/* 209 */     String path = dataFile.getAbsolutePath();
/* 210 */     boolean isWrite = (flags & 0x1) != 0;
/* 211 */     if (SystemUtils.m_verbose)
/*     */     {
/* 213 */       Report.debug("fileaccess", "serializeDataBinder dir=" + FileUtils.getDirectory(path) + ", file=" + FileUtils.getName(path) + ", isWrite=" + isWrite + ", encoding=" + encoding, null);
/*     */     }
/*     */ 
/* 216 */     if ((!isWrite) && (!dataFile.exists()))
/*     */     {
/* 218 */       if (tempFile.exists() == true)
/*     */       {
/* 220 */         Report.error("system", "'" + dataFile.getAbsolutePath() + "' does not exist but the associated temp file '" + tempFile.getAbsolutePath() + "' does. Reading the temp file instead.", null);
/*     */ 
/* 224 */         FileUtils.renameFile(tempFile.getAbsolutePath(), dataFile.getAbsolutePath());
/*     */       }
/*     */       else
/*     */       {
/* 228 */         if ((flags & 0x4) == 0)
/*     */         {
/* 230 */           return false;
/*     */         }
/* 232 */         Report.trace("system", "Could not read data file " + dataFile.getAbsolutePath(), null);
/*     */ 
/* 234 */         throw new ServiceException(-16, LocaleUtils.encodeMessage("csResourceUtilsNoFile", null, dataFile.getName()));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 239 */     BufferedReader reader = null;
/* 240 */     Writer writer = null;
/*     */     try
/*     */     {
/* 244 */       BufferedInputStream bstream = null;
/* 245 */       if (isWrite)
/*     */       {
/* 247 */         boolean mustCreate = (flags & 0x8) != 0;
/* 248 */         File fileToOpen = tempFile;
/* 249 */         if (FileUtils.storeInDB(path))
/*     */         {
/* 251 */           fileToOpen = dataFile;
/*     */         }
/* 253 */         else if (mustCreate)
/*     */         {
/* 258 */           fileToOpen = dataFile;
/* 259 */           if ((FileUtils.usesAtomicCreateFileMethod()) && 
/* 261 */             (!FileUtils.atomicCreateFile(fileToOpen)))
/*     */           {
/* 263 */             Report.trace("system", "Could not atomic create file " + fileToOpen.getAbsolutePath(), null);
/* 264 */             throw new ServiceException(-17, LocaleUtils.encodeMessage("csResourceUtilsCouldNotAtomicCreateFile", null, dataFile.getName()));
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 272 */         stripPasswordData(data, path, true);
/*     */ 
/* 274 */         writer = FileUtils.openDataWriter(fileToOpen, encoding);
/* 275 */         data.sendWithEncoding(writer, encoding);
/* 276 */         writer.close();
/* 277 */         writer = null;
/*     */ 
/* 279 */         if ((dataFile.exists()) && (!FileUtils.storeInDB(path)))
/*     */         {
/* 281 */           FileUtils.deleteFile(path);
/*     */ 
/* 283 */           if (dataFile.exists())
/*     */           {
/* 285 */             throw new ServiceException(-18, LocaleUtils.encodeMessage("csResourceUtilsFileWriteError", null, dataFile.getAbsolutePath()));
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 291 */         if (fileToOpen != dataFile)
/*     */         {
/* 293 */           fileToOpen.renameTo(dataFile);
/*     */ 
/* 295 */           if (!dataFile.exists())
/*     */           {
/* 297 */             if ((flags & 0x10) != 0)
/*     */             {
/* 299 */               Report.error("system", "Unable to rename '" + fileToOpen.getAbsolutePath() + "' to '" + dataFile.getAbsolutePath() + "'.", null);
/*     */             }
/*     */             else
/*     */             {
/* 304 */               Report.trace("system", "Could write to file " + dataFile.getAbsolutePath(), null);
/* 305 */               throw new ServiceException(-18, LocaleUtils.encodeMessage("csResourceUtilsFileWriteError", null, dataFile.getName()));
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 315 */         bstream = new BufferedInputStream(FileUtilsCfgBuilder.getCfgInputStream(dataFile));
/*     */ 
/* 317 */         if (encoding == null)
/*     */         {
/* 319 */           encoding = DataSerializeUtils.detectEncoding(data, bstream, null);
/* 320 */           if (SystemUtils.m_verbose)
/*     */           {
/* 322 */             Report.debug("encoding", "encoding of file '" + dataFile.getAbsolutePath() + "' is '" + encoding + "'", null);
/*     */           }
/*     */         }
/*     */ 
/* 326 */         reader = FileUtils.openDataReader(bstream, encoding);
/* 327 */         data.receiveEx(reader, (flags & 0x2) != 0);
/* 328 */         reader.close();
/* 329 */         reader = null;
/*     */ 
/* 331 */         stripPasswordData(data, path, false);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       String msgAction;
/*     */       String msgAction;
/* 337 */       if (isWrite)
/*     */       {
/* 339 */         msgAction = LocaleUtils.encodeMessage("csResourceUtilsFileWriteError", null, dataFile.getName());
/*     */       }
/*     */       else
/*     */       {
/* 344 */         msgAction = LocaleUtils.encodeMessage("csResourceUtilsFileReadError", null, dataFile.getName());
/*     */       }
/*     */ 
/* 347 */       String msg = LocaleUtils.encodeMessage("csResourceUtilsFileIOError", msgAction);
/*     */       ServiceException se;
/* 350 */       throw se;
/*     */     }
/*     */     finally
/*     */     {
/* 354 */       FileUtils.closeObjects(reader, writer);
/*     */     }
/* 356 */     return true;
/*     */   }
/*     */ 
/*     */   protected static void stripPasswordData(DataBinder binder, String source, boolean isWrite)
/*     */     throws DataException, ServiceException
/*     */   {
/* 362 */     String isStripPasswords = binder.getLocal("IsStripPasswords");
/* 363 */     boolean isStrip = StringUtils.convertToBool(isStripPasswords, false);
/*     */ 
/* 366 */     binder.removeLocal("IsStripPasswords");
/* 367 */     if (!isStrip)
/*     */       return;
/* 369 */     Properties props = binder.getLocalData();
/* 370 */     Map args = new HashMap();
/* 371 */     CryptoPasswordUtils.updatePasswordsInPlace(props, source, args);
/*     */   }
/*     */ 
/*     */   public static DataBinder readDataBinder(String dir, String file)
/*     */     throws ServiceException
/*     */   {
/* 379 */     DataBinder data = new DataBinder(true);
/* 380 */     serializeDataBinder(dir, file, data, false, true);
/* 381 */     return data;
/*     */   }
/*     */ 
/*     */   public static DataBinder readDataBinderFromPath(String path) throws ServiceException
/*     */   {
/* 386 */     String dir = FileUtils.getDirectory(path);
/* 387 */     String file = FileUtils.getName(path);
/*     */ 
/* 389 */     return readDataBinder(dir, file);
/*     */   }
/*     */ 
/*     */   public static DataBinder readDataBinderHeader(String dir, String file)
/*     */     throws ServiceException
/*     */   {
/* 396 */     DataBinder data = new DataBinder(true);
/* 397 */     serializeDataBinderEx(dir, file, data, false, true, true);
/* 398 */     return data;
/*     */   }
/*     */ 
/*     */   public static void readDataBinderFromStream(InputStream stream, DataBinder binder, int flags, String source)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 415 */     Reader reader = null;
/* 416 */     if (FileUtils.storeInDB(source))
/*     */     {
/* 418 */       reader = FileUtilsCfgBuilder.getCfgReader(source);
/*     */     }
/*     */     else
/*     */     {
/*     */       BufferedInputStream bstream;
/*     */       BufferedInputStream bstream;
/* 423 */       if (stream instanceof BufferedInputStream)
/*     */       {
/* 425 */         bstream = (BufferedInputStream)stream;
/*     */       }
/*     */       else
/*     */       {
/* 429 */         bstream = new BufferedInputStream(stream);
/*     */       }
/*     */ 
/* 432 */       String encoding = DataSerializeUtils.detectEncoding(binder, bstream, null);
/* 433 */       if (SystemUtils.m_verbose)
/*     */       {
/* 435 */         Report.debug("encoding", "encoding detected: " + encoding, null);
/*     */       }
/*     */ 
/* 438 */       if (null == encoding)
/*     */       {
/* 440 */         reader = new InputStreamReader(bstream);
/*     */       }
/*     */       else
/*     */       {
/* 444 */         reader = new InputStreamReader(bstream, encoding);
/*     */       }
/*     */     }
/* 447 */     BufferedReader breader = new BufferedReader(reader);
/* 448 */     binder.receiveEx(breader, 0 != (flags & 0x2));
/*     */ 
/* 450 */     stripPasswordData(binder, source, false);
/*     */   }
/*     */ 
/*     */   public static void writeDataBinderToStream(OutputStream stream, DataBinder binder, int flags, String source)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 466 */     String encoding = binder.m_javaEncoding;
/* 467 */     if (null == encoding)
/*     */     {
/* 469 */       encoding = DataSerializeUtils.getSystemEncoding();
/*     */     }
/*     */     Writer writer;
/*     */     Writer writer;
/* 472 */     if (null == encoding)
/*     */     {
/* 474 */       writer = new OutputStreamWriter(stream);
/*     */     }
/*     */     else
/*     */     {
/* 478 */       writer = new OutputStreamWriter(stream, encoding);
/*     */     }
/*     */ 
/* 481 */     stripPasswordData(binder, source, true);
/* 482 */     binder.sendWithEncoding(writer, encoding);
/* 483 */     writer.flush();
/* 484 */     writer.close();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void writeDataBinderToStream(OutputStream stream, DataBinder binder, int flags)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 500 */     writeDataBinderToStream(stream, binder, flags, "N/A");
/*     */   }
/*     */ 
/*     */   public static void doResultSetLog(DataResultSet source, String sourceName, String destName, String column)
/*     */   {
/* 510 */     if ((source == null) || (source.isEmpty()))
/*     */     {
/* 512 */       return;
/*     */     }
/*     */ 
/* 515 */     if (!ResourceTrace.m_traceResourceLoad)
/*     */       return;
/* 517 */     ResourceTrace.doMsg(LocaleUtils.encodeMessage("csComponentLoadMergeTables", null, sourceName, destName));
/*     */ 
/* 521 */     int colIndex = -1;
/* 522 */     if ((column != null) && (column.length() > 0))
/*     */     {
/*     */       try
/*     */       {
/* 526 */         colIndex = ResultSetUtils.getIndexMustExist(source, column);
/*     */       }
/*     */       catch (Exception ignore)
/*     */       {
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 534 */     for (source.first(); source.isRowPresent(); source.next())
/*     */     {
/*     */       String value;
/*     */       String value;
/* 536 */       if (colIndex >= 0)
/*     */       {
/* 538 */         value = source.getStringValue(colIndex);
/*     */       }
/*     */       else
/*     */       {
/* 542 */         value = source.getCurrentRowProps().toString();
/*     */       }
/*     */ 
/* 545 */       ResourceTrace.doMsg(LocaleUtils.encodeMessage("csComponentLoadMergeTableRow", null, value));
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 553 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97380 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.resource.ResourceUtils
 * JD-Core Version:    0.5.4
 */