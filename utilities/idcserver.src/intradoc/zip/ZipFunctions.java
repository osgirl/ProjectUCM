/*     */ package intradoc.zip;
/*     */ 
/*     */ import intradoc.common.BufferPool;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.CharArrayReader;
/*     */ import java.io.CharConversionException;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipFile;
/*     */ import java.util.zip.ZipOutputStream;
/*     */ 
/*     */ public class ZipFunctions
/*     */ {
/*  35 */   public static final String[] ZIP_FILE_COLUMNS = { "fileName", "filePath" };
/*     */ 
/*     */   public static ZipFile readZipFile(String zipFilename, Hashtable entries)
/*     */     throws IOException, ServiceException
/*     */   {
/*  40 */     ZipFile zipFile = null;
/*  41 */     File file = new File(zipFilename);
/*  42 */     if (!file.exists())
/*     */     {
/*  44 */       throw new ServiceException(null, "syZipFileNotFound", new Object[0]);
/*     */     }
/*     */ 
/*  47 */     zipFile = new ZipFile(file);
/*  48 */     for (Enumeration en = zipFile.entries(); en.hasMoreElements(); )
/*     */     {
/*  50 */       ZipEntry entry = (ZipEntry)en.nextElement();
/*  51 */       String name = entry.getName().toLowerCase();
/*  52 */       entries.put(name, entry);
/*     */     }
/*     */ 
/*  55 */     return zipFile;
/*     */   }
/*     */ 
/*     */   public static Hashtable readZipFile(String zipFilename) throws IOException, ServiceException
/*     */   {
/*  64 */     Hashtable entries = new Hashtable();
/*  65 */     ZipFile zipFile = null;
/*     */     Enumeration en;
/*     */     try
/*     */     {
/*  68 */       File file = new File(zipFilename);
/*  69 */       if (!file.exists())
/*     */       {
/*  71 */         throw new ServiceException(null, "syZipFileNotFound", new Object[0]);
/*     */       }
/*     */ 
/*  74 */       zipFile = new ZipFile(file);
/*  75 */       for (en = zipFile.entries(); en.hasMoreElements(); )
/*     */       {
/*  77 */         ZipEntry entry = (ZipEntry)en.nextElement();
/*  78 */         String name = entry.getName().toLowerCase();
/*  79 */         entries.put(name, entry);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*  84 */       FileUtils.closeObject(zipFile);
/*     */     }
/*  86 */     return entries;
/*     */   }
/*     */ 
/*     */   public static String[] findEntriesMatchingWildcardFilter(String zipFileName, String pattern) throws ServiceException
/*     */   {
/*  98 */     ZipFile zipFile = null;
/*  99 */     List list = new ArrayList();
/*     */     Enumeration en;
/*     */     try
/*     */     {
/* 102 */       File file = new File(zipFileName);
/* 103 */       if (!file.exists())
/*     */       {
/* 105 */         throw new ServiceException(null, "syZipFileNotFound", new Object[0]);
/*     */       }
/*     */ 
/* 108 */       zipFile = new ZipFile(file);
/* 109 */       for (en = zipFile.entries(); en.hasMoreElements(); )
/*     */       {
/* 111 */         ZipEntry entry = (ZipEntry)en.nextElement();
/* 112 */         String name = entry.getName().toLowerCase();
/* 113 */         if (StringUtils.match(name, pattern, true))
/*     */         {
/* 115 */           boolean isDir = entry.isDirectory();
/* 116 */           name = FileUtils.fixDirectorySlashes(name, (isDir) ? 78 : 13).toString();
/* 117 */           list.add(name);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 124 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 128 */       FileUtils.closeObject(zipFile);
/*     */     }
/* 130 */     String[] retList = new String[list.size()];
/* 131 */     return (String[])list.toArray(retList);
/*     */   }
/*     */ 
/*     */   public static DataBinder extractFileAsDataBinder(String zipFileName, String fileName)
/*     */     throws DataException
/*     */   {
/* 141 */     DataBinder binder = null;
/* 142 */     ZipFile zipFile = null;
/*     */     try
/*     */     {
/* 145 */       zipFile = new ZipFile(zipFileName);
/* 146 */       ZipEntry entry = zipFile.getEntry(fileName);
/* 147 */       binder = extractFileAsDataBinder(zipFile, entry, fileName);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 153 */       throw new DataException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 157 */       FileUtils.closeObject(zipFile);
/*     */     }
/*     */ 
/* 160 */     return binder;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static DataBinder extractFileAsDataBinder(String zipFileName, ZipEntry entry)
/*     */     throws DataException
/*     */   {
/* 174 */     ZipFile zipFile = null;
/* 175 */     DataBinder binder = null;
/*     */     try
/*     */     {
/* 178 */       zipFile = new ZipFile(zipFileName);
/* 179 */       binder = extractFileAsDataBinder(zipFile, entry);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 185 */       throw new DataException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 189 */       FileUtils.closeObject(zipFile);
/*     */     }
/* 191 */     return binder;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static DataBinder extractFileAsDataBinder(ZipFile zipFile, ZipEntry entry)
/*     */     throws DataException
/*     */   {
/* 201 */     return extractFileAsDataBinder(zipFile, entry, null);
/*     */   }
/*     */ 
/*     */   public static DataBinder extractFileAsDataBinder(ZipFile zipFile, ZipEntry entry, String path)
/*     */     throws DataException
/*     */   {
/* 213 */     if (zipFile == null)
/*     */     {
/* 215 */       throw new DataException("!syZipFileNull");
/*     */     }
/* 217 */     if (entry == null)
/*     */     {
/*     */       String msg;
/*     */       String msg;
/* 220 */       if (path != null)
/*     */       {
/* 222 */         msg = LocaleUtils.encodeMessage("syFileNotInZip2", null, path);
/*     */       }
/*     */       else
/*     */       {
/* 227 */         msg = LocaleUtils.encodeMessage("syFileNotInZip", null);
/*     */       }
/*     */ 
/* 230 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 233 */     DataBinder binder = new DataBinder();
/* 234 */     InputStream input = null;
/*     */     try
/*     */     {
/* 237 */       input = zipFile.getInputStream(entry);
/* 238 */       BufferedInputStream bstream = new BufferedInputStream(input);
/*     */ 
/* 241 */       String encoding = DataSerializeUtils.detectEncoding(binder, bstream, null);
/*     */ 
/* 243 */       BufferedReader inReader = FileUtils.openDataReader(bstream, encoding);
/*     */ 
/* 245 */       IdcCharArrayWriter writer = new IdcCharArrayWriter();
/* 246 */       int length = -1;
/* 247 */       char[] buf = new char[1028];
/* 248 */       while ((length = inReader.read(buf)) > 0)
/*     */       {
/* 250 */         writer.write(buf, 0, length);
/*     */       }
/*     */ 
/* 253 */       Reader reader = new CharArrayReader(writer.m_charArray, 0, writer.m_length);
/* 254 */       BufferedReader bf = new BufferedReader(reader);
/* 255 */       binder.receive(bf);
/* 256 */       writer.release();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 260 */       throw new DataException(e, "syZipExtractionError", new Object[1]);
/*     */     }
/*     */     finally
/*     */     {
/* 264 */       FileUtils.closeObject(input);
/*     */     }
/* 266 */     return binder;
/*     */   }
/*     */ 
/*     */   public static void extractZipFiles(String zipFile, String dirPath) throws ServiceException
/*     */   {
/* 275 */     Vector zipFileEntries = new IdcVector();
/* 276 */     ZipFile zfile = null;
/*     */     Enumeration e;
/*     */     try
/*     */     {
/* 279 */       zfile = new ZipFile(zipFile);
/* 280 */       for (e = zfile.entries(); e.hasMoreElements(); )
/*     */       {
/* 282 */         ZipEntry entry = (ZipEntry)e.nextElement();
/* 283 */         zipFileEntries.addElement(entry.getName());
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*     */       String msg;
/* 292 */       throw new ServiceException(msg, t);
/*     */     }
/*     */     finally
/*     */     {
/* 296 */       FileUtils.closeObject(zfile);
/*     */     }
/* 298 */     extractZipFilesEx(zipFile, dirPath, zipFileEntries, true);
/*     */   }
/*     */ 
/*     */   public static void extractZipFilesEx(String zipFile, String dirPath, Vector zipFileEntries, boolean expandDirectories)
/*     */     throws ServiceException
/*     */   {
/* 312 */     dirPath = FileUtils.directorySlashes(dirPath);
/* 313 */     ZipFile zfile = null;
/* 314 */     String filename = null;
/*     */     try
/*     */     {
/* 317 */       zfile = new ZipFile(zipFile);
/* 318 */       int size = zipFileEntries.size();
/* 319 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 321 */         filename = (String)zipFileEntries.elementAt(i);
/* 322 */         if (!expandDirectories)
/*     */         {
/* 324 */           int j = filename.lastIndexOf("/");
/* 325 */           if (j >= 0)
/*     */           {
/* 327 */             filename = filename.substring(j + 1);
/*     */           }
/*     */         }
/* 330 */         extractZipFileOrDirectory(zfile, filename, filename, dirPath);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */       String msg;
/* 336 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       String msg;
/* 341 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 345 */       FileUtils.closeObject(zfile);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void extractFileFromZip(String zipFile, String filename, String filePath)
/*     */     throws ServiceException
/*     */   {
/* 357 */     extractFileFromZipWithArgs(zipFile, filename, filePath, null);
/*     */   }
/*     */ 
/*     */   public static void extractFileFromZipWithArgs(String zipFile, String filename, String filePath, Map<String, String> args)
/*     */     throws ServiceException
/*     */   {
/* 364 */     ZipFile zfile = null;
/*     */     try
/*     */     {
/* 367 */       zfile = new ZipFile(zipFile);
/* 368 */       ZipEntry entry = zfile.getEntry(filename);
/* 369 */       if (entry == null)
/*     */       {
/* 371 */         boolean catchError = false;
/* 372 */         if (args != null)
/*     */         {
/* 374 */           catchError = StringUtils.convertToBool((String)args.get("allowMissing"), false);
/* 375 */           args.put("isError", "1");
/*     */         }
/* 377 */         if (!catchError)
/*     */         {
/* 379 */           String msg = LocaleUtils.encodeMessage("syUnableToFindEntryInZip", null, filename);
/*     */ 
/* 381 */           throw new DataException(msg);
/*     */         }
/*     */         return;
/*     */       }
/* 385 */       extractFileFromZip(zfile, entry, filename, filePath);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */       String msg;
/* 391 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 397 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 401 */       FileUtils.closeObject(zfile);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void extractFileFromZip(ZipFile zipFile, ZipEntry entry, String filename, String filePath)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 410 */       InputStream input = zipFile.getInputStream(entry);
/* 411 */       extractFileToDisk(filePath, input, entry);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 415 */       String msg = LocaleUtils.encodeMessage("syUnableToExtractFile", null, filePath);
/*     */ 
/* 417 */       throw new ServiceException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void extractFileToDisk(String fullFileName, InputStream input, ZipEntry entry)
/*     */     throws IOException, ServiceException
/*     */   {
/* 428 */     OutputStream out = null;
/* 429 */     if (SystemUtils.m_verbose)
/*     */     {
/* 431 */       Report.trace("zip", "ZipFunctions.extractFileToDisk() - fileName:[" + fullFileName + "] entryName:[" + entry.getName() + "] entrySize:[" + entry.getSize() + "]", null);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*     */       try
/*     */       {
/* 439 */         out = FileUtils.openOutputStream(fullFileName, 16);
/*     */       }
/*     */       catch (FileNotFoundException e)
/*     */       {
/* 445 */         FileUtils.deleteFile(fullFileName + ".tmp");
/* 446 */         FileUtils.renameFile(fullFileName, fullFileName + ".tmp");
/* 447 */         out = new FileOutputStream(fullFileName);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/* 453 */       String oldFileName = fullFileName + ".old";
/* 454 */       if (SystemUtils.m_verbose)
/*     */       {
/* 456 */         Report.trace("zip", "ZipFunctions.extractFileToDisk() - Unable to overwite original name, renaming to: " + oldFileName, null);
/*     */       }
/*     */ 
/* 459 */       FileUtils.renameFile(fullFileName, oldFileName);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 464 */       if (out == null)
/*     */       {
/* 466 */         out = new FileOutputStream(fullFileName);
/*     */       }
/* 468 */       byte[] buf = new byte[BufferPool.m_mediumBufferSize];
/* 469 */       int length = 0;
/* 470 */       long size = entry.getSize();
/* 471 */       long numRead = 0L;
/*     */       do { if ((length = input.read(buf)) <= 0)
/*     */           break;
/* 474 */         out.write(buf, 0, length);
/* 475 */         numRead += length; }
/* 476 */       while ((size <= 0L) || (size - numRead > 0L));
/*     */     }
/*     */     finally
/*     */     {
/* 486 */       FileUtils.closeObjects(out, input);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean extractZipFileOrDirectoryNotEncodedEntry(ZipFile zFile, String entryName, String path, String destinationDir)
/*     */     throws ServiceException
/*     */   {
/* 499 */     boolean isError = true;
/*     */     try
/*     */     {
/* 502 */       entryName = StringUtils.encodeHttpHeaderStyle(entryName, false);
/* 503 */       isError = extractZipFileOrDirectoryInternal(zFile, entryName, path, destinationDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 507 */       throw e;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 511 */       String msg = LocaleUtils.encodeMessage("syUnableToExtractFile", null, path);
/*     */ 
/* 513 */       throw new ServiceException(msg, e);
/*     */     }
/* 515 */     return isError;
/*     */   }
/*     */ 
/*     */   public static boolean extractZipFileOrDirectory(ZipFile zFile, String entryName, String path, String destinationDir)
/*     */     throws ServiceException
/*     */   {
/* 526 */     boolean isError = true;
/*     */     try
/*     */     {
/* 529 */       path = StringUtils.decodeHttpHeaderStyle(path);
/* 530 */       isError = extractZipFileOrDirectoryInternal(zFile, entryName, path, destinationDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 534 */       throw e;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 538 */       String msg = LocaleUtils.encodeMessage("syUnableToExtractFile", null, path);
/*     */ 
/* 540 */       throw new ServiceException(msg, e);
/*     */     }
/* 542 */     return isError;
/*     */   }
/*     */ 
/*     */   protected static boolean extractZipFileOrDirectoryInternal(ZipFile zFile, String entryName, String path, String destinationDir)
/*     */     throws CharConversionException, ServiceException
/*     */   {
/* 549 */     ZipEntry entry = zFile.getEntry(entryName);
/* 550 */     String zipName = zFile.getName();
/* 551 */     destinationDir = FileUtils.directorySlashesEx(destinationDir, false);
/*     */ 
/* 555 */     FileUtils.checkOrCreateDirectory(destinationDir, 1);
/*     */ 
/* 557 */     boolean isExtracted = false;
/*     */     String sEntryName;
/*     */     String sPath;
/*     */     Enumeration e;
/* 562 */     if ((entry == null) || (entry.getSize() == 0L))
/*     */     {
/* 566 */       sEntryName = entryName;
/*     */ 
/* 569 */       boolean makeDir = false;
/* 570 */       if (entryName.endsWith("/"))
/*     */       {
/* 572 */         makeDir = true;
/*     */       }
/*     */       else
/*     */       {
/* 576 */         sEntryName = sEntryName + "/";
/*     */       }
/*     */ 
/* 579 */       sPath = path;
/* 580 */       if (!path.endsWith("/"))
/*     */       {
/* 582 */         sPath = sPath + "/";
/*     */       }
/*     */ 
/* 585 */       if (makeDir)
/*     */       {
/* 587 */         FileUtils.checkOrCreateSubDirectory(destinationDir, sPath);
/* 588 */         isExtracted = true;
/*     */       }
/*     */ 
/* 591 */       for (e = zFile.entries(); e.hasMoreElements(); )
/*     */       {
/* 593 */         entry = (ZipEntry)e.nextElement();
/* 594 */         String name = entry.getName();
/* 595 */         if ((name.startsWith(sEntryName)) && (!name.equals(sEntryName)))
/*     */         {
/* 597 */           isExtracted = true;
/*     */ 
/* 610 */           String subDest = name.substring(sEntryName.length());
/* 611 */           subDest = StringUtils.decodeHttpHeaderStyle(subDest);
/* 612 */           String subDir = "";
/* 613 */           int index = subDest.lastIndexOf(47);
/*     */ 
/* 615 */           if (subDest.endsWith("/"))
/*     */           {
/* 617 */             subDir = subDest;
/*     */           }
/* 619 */           else if (index > 0)
/*     */           {
/* 622 */             subDir = subDest.substring(0, index + 1);
/*     */           }
/*     */ 
/* 625 */           FileUtils.checkOrCreateSubDirectory(destinationDir, sPath + subDir);
/* 626 */           if (!entry.isDirectory())
/*     */           {
/* 631 */             extractFileFromZip(zipName, name, destinationDir + sPath + subDest);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 637 */     if (!isExtracted)
/*     */     {
/* 640 */       String dir = FileUtils.getDirectory(destinationDir + path);
/* 641 */       if (dir.length() > destinationDir.length())
/*     */       {
/* 643 */         String subDir = dir.substring(destinationDir.length()) + "/";
/* 644 */         FileUtils.checkOrCreateSubDirectory(destinationDir, subDir);
/*     */       }
/*     */ 
/* 648 */       Map args = new HashMap();
/* 649 */       args.put("allowMissing", "1");
/* 650 */       extractFileFromZipWithArgs(zipName, entryName, destinationDir + path, args);
/* 651 */       boolean isError = StringUtils.convertToBool((String)args.get("isError"), false);
/* 652 */       isExtracted = !isError;
/*     */     }
/*     */ 
/* 655 */     return !isExtracted;
/*     */   }
/*     */ 
/*     */   public static void createZipFile(String zipFilename, DataResultSet fileSet)
/*     */     throws ServiceException, DataException
/*     */   {
/* 666 */     File zipFile = new File(zipFilename);
/* 667 */     String zipPath = FileUtils.getAbsolutePath(null, zipFilename);
/*     */ 
/* 669 */     ZipOutputStream zos = null;
/*     */     try
/*     */     {
/* 672 */       zos = new ZipOutputStream(new FileOutputStream(zipFile));
/* 673 */       String name = null;
/* 674 */       String filePath = null;
/*     */ 
/* 676 */       FieldInfo[] infos = ResultSetUtils.createInfoList(fileSet, ZIP_FILE_COLUMNS, true);
/* 677 */       int nameIndex = infos[0].m_index;
/* 678 */       int pathIndex = infos[1].m_index;
/*     */ 
/* 680 */       for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*     */       {
/* 682 */         name = fileSet.getStringValue(nameIndex);
/* 683 */         filePath = fileSet.getStringValue(pathIndex);
/*     */ 
/* 685 */         if (filePath.equals(zipPath))
/*     */           continue;
/* 687 */         addFile(name, filePath, zos);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 695 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 699 */       FileUtils.closeObject(zos);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void createZipFileEx(String zipFilename, DataResultSet fileSet, Map descMap, FileStoreProvider fileStore)
/*     */     throws ServiceException, DataException
/*     */   {
/* 706 */     File zipFile = new File(zipFilename);
/* 707 */     String zipPath = FileUtils.getAbsolutePath(null, zipFilename);
/*     */ 
/* 709 */     ZipOutputStream zos = null;
/*     */     try
/*     */     {
/* 712 */       zos = new ZipOutputStream(new FileOutputStream(zipFile));
/*     */ 
/* 714 */       FieldInfo[] infos = ResultSetUtils.createInfoList(fileSet, ZIP_FILE_COLUMNS, true);
/* 715 */       int nameIndex = infos[0].m_index;
/* 716 */       int pathIndex = infos[1].m_index;
/* 717 */       for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*     */       {
/* 719 */         String name = fileSet.getStringValue(nameIndex);
/* 720 */         String filePath = fileSet.getStringValue(pathIndex);
/*     */ 
/* 722 */         IdcFileDescriptor descriptor = (IdcFileDescriptor)descMap.get(name);
/* 723 */         if (descriptor != null)
/*     */         {
/* 725 */           InputStream in = fileStore.getInputStream(descriptor, null);
/* 726 */           addStream(name, in, zos);
/*     */         } else {
/* 728 */           if (filePath.equals(zipPath))
/*     */             continue;
/* 730 */           addFile(name, filePath, zos);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */       String msg;
/* 738 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 742 */       FileUtils.closeObject(zos);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addFile(String name, String filePath, ZipOutputStream zos)
/*     */     throws IOException, ServiceException
/*     */   {
/* 755 */     String errMsg = null;
/* 756 */     File file = FileUtilsCfgBuilder.getCfgFile(filePath, null);
/* 757 */     if (!file.exists())
/*     */     {
/* 759 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddFileNotFound", null, filePath);
/*     */     }
/* 761 */     else if (!file.isFile())
/*     */     {
/* 763 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddNotFile", null, filePath);
/*     */     }
/* 765 */     else if (!file.canRead())
/*     */     {
/* 767 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddNoAccess", null, filePath);
/*     */     }
/*     */ 
/* 770 */     if (errMsg != null)
/*     */     {
/* 772 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/* 775 */     InputStream in = FileUtilsCfgBuilder.getCfgInputStream(filePath);
/* 776 */     addStream(name, in, zos);
/*     */   }
/*     */ 
/*     */   public static void addStream(String name, InputStream in, ZipOutputStream zos)
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 786 */       name = StringUtils.encodeHttpHeaderStyle(name, false);
/*     */ 
/* 788 */       ZipEntry entry = new ZipEntry(name);
/* 789 */       zos.putNextEntry(entry);
/*     */ 
/* 791 */       byte[] buf = new byte[BufferPool.m_mediumBufferSize];
/* 792 */       int num = 0;
/* 793 */       while ((num = in.read(buf)) > 0)
/*     */       {
/* 795 */         zos.write(buf, 0, num);
/*     */       }
/*     */     }
/*     */     catch (FileNotFoundException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       String msg;
/* 806 */       FileUtils.closeObject(in);
/* 807 */       zos.closeEntry();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addDirectory(String name, String path, boolean isRecursive, ZipOutputStream zos)
/*     */     throws IOException, ServiceException
/*     */   {
/* 819 */     addDirectoryEx(name, path, isRecursive, null, zos);
/*     */   }
/*     */ 
/*     */   public static void addDirectoryFiltered(String name, String path, boolean isRecursive, ResultSetFilter filter, ZipOutputStream zos)
/*     */     throws IOException, ServiceException
/*     */   {
/* 826 */     addDirectoryEx(name, path, isRecursive, filter, zos);
/*     */   }
/*     */ 
/*     */   public static void addDirectoryEx(String name, String path, boolean isRecursive, ResultSetFilter filter, ZipOutputStream zos)
/*     */     throws IOException, ServiceException
/*     */   {
/* 833 */     String errMsg = null;
/* 834 */     File file = new File(path);
/* 835 */     if (!file.exists())
/*     */     {
/* 837 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddFileNotFound", null, path);
/*     */     }
/* 840 */     else if (!file.isDirectory())
/*     */     {
/* 842 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddNotFile", null, path);
/*     */     }
/* 845 */     else if (!file.canRead())
/*     */     {
/* 847 */       errMsg = LocaleUtils.encodeMessage("syZipUnableToAddNoAccess", null, path);
/*     */     }
/*     */ 
/* 851 */     if (errMsg != null)
/*     */     {
/* 853 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/* 856 */     String[] files = file.list();
/* 857 */     for (int i = 0; i < files.length; ++i)
/*     */     {
/* 859 */       String newName = FileUtils.directorySlashes(name) + files[i];
/* 860 */       String newPath = FileUtils.directorySlashes(path) + files[i];
/* 861 */       if (FileUtils.checkFile(newPath, true, false) == 0)
/*     */       {
/* 864 */         if (!checkAllowPath(newName, newPath, files[i], false, filter)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 868 */         addFile(newName, newPath, zos);
/*     */       } else {
/* 870 */         if (FileUtils.checkFile(newPath, false, false) != 0) {
/*     */           continue;
/*     */         }
/* 873 */         if (!checkAllowPath(newName, newPath, files[i], true, filter)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 877 */         addDirectoryEx(newName, newPath, isRecursive, filter, zos);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean checkAllowPath(String newName, String newPath, String fileName, boolean isDir, ResultSetFilter filter)
/*     */   {
/* 885 */     if (filter == null)
/*     */     {
/* 887 */       return true;
/*     */     }
/* 889 */     if (isDir)
/*     */     {
/* 891 */       newPath = FileUtils.directorySlashes(newPath);
/* 892 */       fileName = FileUtils.directorySlashes(fileName);
/*     */     }
/* 894 */     Vector v = new IdcVector();
/* 895 */     v.addElement(newName);
/* 896 */     v.addElement(newPath);
/* 897 */     v.addElement(fileName);
/* 898 */     String isDirStr = (isDir) ? "1" : "";
/* 899 */     v.addElement(isDirStr);
/*     */ 
/* 907 */     return filter.checkRow(fileName, 0, v) == 1;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void closeStreams(OutputStream out, InputStream in, ZipFile zFile)
/*     */   {
/* 920 */     SystemUtils.reportDeprecatedUsage("ZipFunctions.closeStreams()");
/*     */     try
/*     */     {
/* 923 */       if (out != null)
/*     */       {
/* 925 */         out.close();
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 930 */       if (SystemUtils.m_verbose)
/*     */       {
/* 932 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 938 */       if (in != null)
/*     */       {
/* 940 */         in.close();
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 945 */       if (SystemUtils.m_verbose)
/*     */       {
/* 947 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 953 */       if (zFile != null)
/*     */       {
/* 955 */         zFile.close();
/*     */       }
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 960 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 962 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 969 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97049 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.zip.ZipFunctions
 * JD-Core Version:    0.5.4
 */