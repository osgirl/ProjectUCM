/*      */ package intradoc.zip;
/*      */ 
/*      */ import intradoc.common.BufferPool;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.ReportTracingCallback;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.io.IdcByteHandler;
/*      */ import intradoc.io.IdcByteHandlerException;
/*      */ import intradoc.io.IdcRandomAccessByteArray;
/*      */ import intradoc.io.IdcRandomAccessByteFile;
/*      */ import intradoc.io.InputStreamIdcByteHandler;
/*      */ import intradoc.io.zip.IdcZipEntry;
/*      */ import intradoc.io.zip.IdcZipEnvironment;
/*      */ import intradoc.io.zip.IdcZipException;
/*      */ import intradoc.io.zip.IdcZipFile;
/*      */ import intradoc.io.zip.IdcZipHandler;
/*      */ import intradoc.io.zip.IdcZipOutputStream;
/*      */ import intradoc.io.zip.IdcZipUtils;
/*      */ import intradoc.util.GenericTracingCallback;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.util.MapUtils;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.ByteArrayOutputStream;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.RandomAccessFile;
/*      */ import java.util.Arrays;
/*      */ import java.util.Collection;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcZipFunctions
/*      */ {
/*      */   public static IdcZipEnvironment m_defaultZipEnvironment;
/*      */ 
/*      */   public static synchronized void initZipEnvironment()
/*      */   {
/*  168 */     if (m_defaultZipEnvironment != null)
/*      */       return;
/*  170 */     IdcZipEnvironment zipenv = new IdcZipEnvironment();
/*  171 */     zipenv.m_trace = new ReportTracingCallback("zip");
/*  172 */     zipenv.m_allocator = BufferPool.getBufferPool();
/*  173 */     zipenv.init();
/*  174 */     m_defaultZipEnvironment = zipenv;
/*      */   }
/*      */ 
/*      */   public static IdcZipFile newIdcZipFile(String filename)
/*      */     throws ServiceException
/*      */   {
/*  182 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  184 */       initZipEnvironment();
/*      */     }
/*  186 */     IdcZipFile zip = new IdcZipFile(filename);
/*      */     try
/*      */     {
/*  189 */       zip.init(m_defaultZipEnvironment);
/*  190 */       return zip;
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  194 */       throw new ServiceException(ibhe);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  198 */       throw new ServiceException(ize);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static IdcZipFile newIdcZipFile(File file)
/*      */     throws ServiceException
/*      */   {
/*  205 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  207 */       initZipEnvironment();
/*      */     }
/*  209 */     IdcZipFile zip = new IdcZipFile(file);
/*      */     try
/*      */     {
/*  212 */       zip.init(m_defaultZipEnvironment);
/*  213 */       return zip;
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  217 */       throw new ServiceException(ibhe);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  221 */       throw new ServiceException(ize);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static IdcZipFile newIdcZipFile(IdcByteHandler handler)
/*      */     throws ServiceException
/*      */   {
/*  228 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  230 */       initZipEnvironment();
/*      */     }
/*  232 */     IdcZipFile zip = new IdcZipFile(handler);
/*      */     try
/*      */     {
/*  235 */       zip.init(m_defaultZipEnvironment);
/*  236 */       return zip;
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  240 */       throw new ServiceException(ibhe);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  244 */       throw new ServiceException(ize);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static IdcZipOutputStream newIdcZipOutputStream(OutputStream stream)
/*      */   {
/*  250 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  252 */       initZipEnvironment();
/*      */     }
/*  254 */     IdcZipOutputStream zos = new IdcZipOutputStream(stream);
/*  255 */     zos.init(m_defaultZipEnvironment);
/*  256 */     return zos;
/*      */   }
/*      */ 
/*      */   public static IdcZipEntry newIdcZipEntry(IdcZipHandler zip, String localFilename, String zipFilename, Map options)
/*      */     throws IdcByteHandlerException
/*      */   {
/*  274 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  276 */       initZipEnvironment();
/*      */     }
/*  278 */     IdcZipEntry entry = new IdcZipEntry(zipFilename);
/*  279 */     if (zipFilename.endsWith("/"))
/*      */     {
/*  281 */       entry.m_isDirectory = true;
/*      */     }
/*  283 */     else if (localFilename != null)
/*      */     {
/*  285 */       IdcRandomAccessByteFile file = new IdcRandomAccessByteFile(localFilename, 1);
/*      */ 
/*  287 */       file.init();
/*  288 */       entry.m_bytesUncompressed = file;
/*  289 */       entry.m_sizeUncompressed = file.getSize();
/*  290 */       entry.m_lastModified = file.m_file.lastModified();
/*  291 */       if (zip instanceof IdcZipFile)
/*      */       {
/*  293 */         ((IdcZipFile)zip).addCloseable(file);
/*      */       }
/*      */     }
/*  296 */     return entry;
/*      */   }
/*      */ 
/*      */   public static DataBinder extractFileAsDataBinder(IdcZipFile file, String entryName)
/*      */     throws DataException
/*      */   {
/*  312 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  314 */       initZipEnvironment();
/*      */     }
/*  316 */     IdcZipEntry entry = file.getEntry(entryName, -1);
/*  317 */     if (entry == null)
/*      */     {
/*  319 */       throw new DataException(null, "syFileNotInZip2", new Object[] { entryName });
/*      */     }
/*  321 */     return extractEntryAsDataBinder(entry);
/*      */   }
/*      */ 
/*      */   public static DataBinder extractEntryAsDataBinder(IdcZipEntry entry)
/*      */     throws DataException
/*      */   {
/*  334 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  336 */       initZipEnvironment();
/*      */     }
/*  338 */     if (entry.m_sizeUncompressed > 2147483647L)
/*      */     {
/*  340 */       IdcMessage msg1 = IdcMessageFactory.lc("syValueTooBig", new Object[] { String.valueOf(entry.m_sizeUncompressed), Integer.valueOf(2147483647) });
/*      */ 
/*  342 */       IdcMessage msg2 = IdcMessageFactory.lc(msg1, "syUnableToExtractFile", new Object[] { entry.m_filename });
/*      */ 
/*  344 */       throw new DataException(null, msg2);
/*      */     }
/*  346 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/*  349 */       InputStream is = IdcZipUtils.extractEntryAsInputStream(m_defaultZipEnvironment, entry);
/*  350 */       BufferedInputStream bis = new BufferedInputStream(is);
/*      */ 
/*  352 */       String encoding = DataSerializeUtils.detectEncoding(binder, bis, null);
/*  353 */       BufferedReader br = FileUtils.openDataReader(bis, encoding);
/*      */ 
/*  355 */       binder.receive(br);
/*      */     }
/*      */     catch (IdcByteHandlerException e)
/*      */     {
/*  359 */       throw new DataException(e, "syZipExtractionError", new Object[] { entry.m_filename });
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  363 */       throw new DataException(e, "syZipExtractionError", new Object[] { entry.m_filename });
/*      */     }
/*      */     catch (IdcZipException e)
/*      */     {
/*  367 */       throw new DataException(e, "syZipExtractionError", new Object[] { entry.m_filename });
/*      */     }
/*  369 */     return binder;
/*      */   }
/*      */ 
/*      */   public static void extractEntry(IdcZipEntry entry, String targetName, Map options)
/*      */     throws ServiceException
/*      */   {
/*  393 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  395 */       initZipEnvironment();
/*      */     }
/*  397 */     String entryName = entry.m_filename;
/*      */ 
/*  402 */     File targetDir = new File(targetName);
/*      */ 
/*  404 */     if (!targetName.endsWith("/"))
/*      */     {
/*  406 */       String targetFilename = targetDir.getName();
/*  407 */       targetDir = targetDir.getParentFile();
/*      */     }
/*      */     else
/*      */     {
/*  416 */       int stripCount = MapUtils.getIntValueFromMap(options, "stripCount", 0);
/*  417 */       int nameIndex = 0;
/*  418 */       while (stripCount-- > 0)
/*      */       {
/*  420 */         int slashIndex = entryName.indexOf(47, nameIndex);
/*  421 */         if (slashIndex < 0)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/*  426 */         nameIndex = slashIndex + 1;
/*      */       }
/*  428 */       if (nameIndex > 0)
/*      */       {
/*  430 */         entryName = entry.m_filename.substring(nameIndex);
/*      */       }
/*      */ 
/*  435 */       int lastSlash = entryName.lastIndexOf(47);
/*      */       String targetFilename;
/*  436 */       if (lastSlash >= 0)
/*      */       {
/*  438 */         String prefix = entryName.substring(0, lastSlash);
/*  439 */         targetDir = new File(targetDir, prefix);
/*  440 */         targetFilename = entryName.substring(lastSlash + 1);
/*      */       }
/*      */       else
/*      */       {
/*  444 */         targetFilename = entryName;
/*      */       }
/*      */     }
/*  447 */     if (m_defaultZipEnvironment.m_verbosity >= 6)
/*      */     {
/*  449 */       m_defaultZipEnvironment.m_trace.report(6, new Object[] { "extracting entry \"", entry.m_filename, "\", into \"", targetDir, "/", targetFilename, "\" (", entry.m_bytesUncompressed, " bytes)" });
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  456 */       targetDir.mkdirs();
/*  457 */       if (!targetDir.isDirectory())
/*      */       {
/*  459 */         String targetDirname = targetDir.getPath();
/*  460 */         IdcMessage msg = new IdcMessage("syFileUtilsUnableToCreateSpecifiedDir", new Object[] { targetDirname });
/*  461 */         throw new IdcZipException(msg);
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/*  466 */       if (t instanceof IdcZipException)
/*      */       {
/*  468 */         throw new ServiceException(t);
/*      */       }
/*  470 */       String targetDirname = targetDir.getPath();
/*  471 */       IdcMessage msg = new IdcMessage("syFileUtilsUnableToCreateSpecifiedDir", new Object[] { targetDirname });
/*  472 */       throw new ServiceException(t, msg);
/*      */     }
/*  474 */     if (targetFilename.length() <= 0)
/*      */     {
/*  477 */       return;
/*      */     }
/*      */ 
/*  481 */     File targetFile = new File(targetDir, targetFilename);
/*  482 */     String targetFilename = targetFile.getName();
/*  483 */     if (m_defaultZipEnvironment.m_verbosity >= 7)
/*      */     {
/*  485 */       m_defaultZipEnvironment.m_trace.report(7, new Object[] { "extracting \"", entry.m_filename, "\" as \"", targetFilename, "\"" });
/*      */     }
/*      */ 
/*  488 */     File tmpFile = new File(targetDir, targetFilename + ".tmp");
/*      */ 
/*  490 */     RandomAccessFile target = null;
/*      */     Throwable t;
/*      */     try {
/*  493 */       target = new RandomAccessFile(tmpFile, "rw");
/*  494 */       target.setLength(0L);
/*  495 */       IdcRandomAccessByteFile targetHandler = new IdcRandomAccessByteFile(target);
/*  496 */       target = null;
/*      */       try
/*      */       {
/*  499 */         IdcZipUtils.extractEntry(m_defaultZipEnvironment, entry, targetHandler);
/*      */       }
/*      */       finally
/*      */       {
/*  503 */         FileUtils.closeObject(targetHandler);
/*      */       }
/*  505 */       FileUtils.renameFile(tmpFile.getPath(), targetFile.getPath());
/*  506 */       if (entry.m_lastModified != 0L)
/*      */       {
/*  508 */         targetFile.setLastModified(entry.m_lastModified);
/*      */       }
/*  510 */       if (entry.m_isExecutable)
/*      */       {
/*  512 */         targetFile.setExecutable(true, false);
/*      */       }
/*  514 */       t = null;
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  518 */       t = ibhe;
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*  522 */       t = ioe;
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  526 */       t = ize;
/*      */     }
/*      */     finally
/*      */     {
/*  530 */       if (target != null)
/*      */       {
/*  532 */         FileUtils.closeObject(target);
/*      */       }
/*      */     }
/*  535 */     if (t == null)
/*      */       return;
/*  537 */     IdcMessage msg = new IdcMessage("syZipEntryExtractIntoError", new Object[] { entry.m_filename, targetFilename });
/*  538 */     throw new ServiceException(t, msg);
/*      */   }
/*      */ 
/*      */   public static void extractEntries(IdcZipFile zip, String targetDir, Map options)
/*      */     throws ServiceException
/*      */   {
/*  570 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  572 */       initZipEnvironment();
/*      */     }
/*  574 */     if (!targetDir.endsWith("/"))
/*      */     {
/*  576 */       targetDir = targetDir + '/';
/*      */     }
/*  578 */     boolean useRecursion = !MapUtils.getBoolValueFromMap(options, "noRecursion", false);
/*  579 */     boolean allowMissing = MapUtils.getBoolValueFromMap(options, "allowMissing", false);
/*  580 */     String[] includeEntries = null;
/*  581 */     if (options != null)
/*      */     {
/*  583 */       Collection requestedEntries = (Collection)options.get("entryNames");
/*      */ 
/*  589 */       if (requestedEntries != null)
/*      */       {
/*  591 */         Set foundEntries = new HashSet();
/*  592 */         int numRequestedEntries = requestedEntries.size();
/*  593 */         String[] foundEntriesArray = new String[numRequestedEntries];
/*  594 */         foundEntriesArray = (String[])requestedEntries.toArray(foundEntriesArray);
/*  595 */         Arrays.sort(foundEntriesArray);
/*  596 */         for (int i = 0; i < numRequestedEntries; ++i)
/*      */         {
/*  598 */           String entryName = foundEntriesArray[i];
/*  599 */           boolean wasFound = true;
/*  600 */           if (entryName.endsWith("/"))
/*      */           {
/*  602 */             if (!zip.m_directories.contains(entryName))
/*      */             {
/*  604 */               wasFound = false;
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  609 */             IdcZipEntry entry = zip.getEntry(entryName, -1);
/*  610 */             if (entry == null)
/*      */             {
/*  612 */               String entryNameWithSlash = entryName + '/';
/*  613 */               if (zip.m_directories.contains(entryNameWithSlash))
/*      */               {
/*  615 */                 entryName = entryNameWithSlash;
/*      */               }
/*      */               else
/*      */               {
/*  619 */                 wasFound = false;
/*      */               }
/*      */             }
/*      */           }
/*  623 */           if (!wasFound)
/*      */           {
/*  625 */             options.put("isError", "1");
/*  626 */             if (allowMissing)
/*      */             {
/*  628 */               return;
/*      */             }
/*  630 */             IdcMessage msg = new IdcMessage("syFileNotInZip2", new Object[] { entryName });
/*  631 */             throw new ServiceException(null, msg);
/*      */           }
/*  633 */           boolean doThisEntry = true;
/*  634 */           if (useRecursion)
/*      */           {
/*  637 */             String name = entryName;
/*      */             do
/*      */             {
/*      */               int slash;
/*  639 */               if ((slash = name.lastIndexOf(47, name.length() - 2)) <= 0)
/*      */                 break label356;
/*  641 */               name = name.substring(0, slash + 1);
/*  642 */             }while (!foundEntries.contains(name));
/*      */ 
/*  644 */             doThisEntry = false;
/*      */           }
/*      */ 
/*  649 */           label356: if (!doThisEntry)
/*      */             continue;
/*  651 */           foundEntries.add(entryName);
/*      */         }
/*      */ 
/*  654 */         int numIncludeEntries = foundEntries.size();
/*  655 */         if (numIncludeEntries > 0)
/*      */         {
/*  657 */           includeEntries = new String[numIncludeEntries];
/*  658 */           foundEntries.toArray(includeEntries);
/*  659 */           Arrays.sort(includeEntries);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  664 */     Set zipEntryNames = zip.m_entries.keySet();
/*  665 */     int numZipEntries = zipEntryNames.size();
/*  666 */     String[] entries = new String[numZipEntries];
/*  667 */     entries = (String[])zipEntryNames.toArray(entries);
/*  668 */     Arrays.sort(entries);
/*      */ 
/*  677 */     int i = 0; for (int e = 0; e < numZipEntries; ++e)
/*      */     {
/*  679 */       String entryName = entries[e];
/*  680 */       boolean doExtract = true;
/*  681 */       if ((includeEntries != null) && ((
/*  683 */         (i >= includeEntries.length) || (!entryName.startsWith(includeEntries[i])))))
/*      */       {
/*  685 */         doExtract = false;
/*      */       }
/*      */ 
/*  688 */       IdcZipEntry entry = zip.getEntry(entryName, -1);
/*  689 */       if (doExtract)
/*      */       {
/*  691 */         extractEntry(entry, targetDir, options);
/*      */       }
/*      */ 
/*  698 */       if (((doExtract) && (useRecursion)) || 
/*  700 */         (includeEntries == null) || (i >= includeEntries.length))
/*      */         continue;
/*  702 */       int cmp = entryName.compareTo(includeEntries[i]);
/*  703 */       if (cmp < 0)
/*      */         continue;
/*  705 */       ++i;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addEntryFromFile(IdcZipHandler zip, String entryName, String filePath, Map options)
/*      */     throws ServiceException
/*      */   {
/*  726 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  728 */       initZipEnvironment();
/*      */     }
/*  730 */     File file = new File(filePath);
/*  731 */     String errorKey = null;
/*  732 */     boolean isFile = file.isFile();
/*  733 */     boolean isDirectory = file.isDirectory();
/*  734 */     if (!file.exists())
/*      */     {
/*  736 */       errorKey = "syZipUnableToAddFileNotFound";
/*      */     }
/*  738 */     else if ((!isFile) && (!isDirectory))
/*      */     {
/*  740 */       errorKey = "syZipUnableToAddNotFile";
/*      */     }
/*  742 */     else if (!file.canRead())
/*      */     {
/*  744 */       errorKey = "syZipUnableToAddNoAccess";
/*      */     }
/*  746 */     if (errorKey != null)
/*      */     {
/*  748 */       IdcMessage msg = new IdcMessage(errorKey, new Object[] { filePath });
/*  749 */       throw new ServiceException(null, msg);
/*      */     }
/*  751 */     boolean endsWithSlash = entryName.endsWith("/");
/*  752 */     if (isDirectory != endsWithSlash)
/*      */     {
/*  754 */       if (isDirectory)
/*      */       {
/*  756 */         entryName = entryName + '/';
/*      */       }
/*      */       else
/*      */       {
/*  760 */         entryName = entryName.substring(0, entryName.length() - 1);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/*  765 */       IdcZipEntry entry = newIdcZipEntry(zip, filePath, entryName, options);
/*  766 */       if (!isDirectory)
/*      */       {
/*  768 */         IdcZipUtils.deflateEntry(m_defaultZipEnvironment, entry, null);
/*      */       }
/*  770 */       zip.putEntry(entry, -1);
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  774 */       throw new ServiceException(ibhe);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  778 */       throw new ServiceException(ize);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addEntryFromStream(IdcZipHandler zip, String entryName, InputStream in, long length, Map options)
/*      */     throws ServiceException
/*      */   {
/*  795 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  797 */       initZipEnvironment();
/*      */     }
/*  799 */     if (entryName.endsWith("/"))
/*      */     {
/*  801 */       IdcMessage msg = new IdcMessage("syZipBadFilename", new Object[] { entryName });
/*  802 */       throw new ServiceException(null, msg);
/*      */     }
/*  804 */     InputStreamIdcByteHandler handler = new InputStreamIdcByteHandler(in, length);
/*  805 */     IdcMessage msg = new IdcMessage("syZipEntryDeflateError", new Object[] { entryName });
/*      */     try
/*      */     {
/*  808 */       IdcZipEntry entry = newIdcZipEntry(zip, null, entryName, options);
/*  809 */       entry.m_sizeUncompressed = length;
/*  810 */       entry.m_lastModified = System.currentTimeMillis();
/*  811 */       IdcZipUtils.deflateEntry(m_defaultZipEnvironment, entry, handler);
/*  812 */       zip.putEntry(entry, -1);
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  816 */       throw new ServiceException(ibhe, msg);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  820 */       throw new ServiceException(ize, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addEntryFromDataBinder(IdcZipHandler zip, String entryName, DataBinder binder, Map options)
/*      */     throws ServiceException
/*      */   {
/*  836 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  838 */       initZipEnvironment();
/*      */     }
/*  840 */     if (entryName.endsWith("/"))
/*      */     {
/*  842 */       IdcMessage msg = new IdcMessage("syZipBadFilename", new Object[] { entryName });
/*  843 */       throw new ServiceException(null, msg);
/*      */     }
/*  845 */     ByteArrayOutputStream baos = new ByteArrayOutputStream();
/*  846 */     IdcMessage msg = new IdcMessage("syZipEntryDeflateError", new Object[] { entryName });
/*      */     try
/*      */     {
/*  849 */       BufferedWriter bw = FileUtils.openDataWriter(baos, null, 0);
/*  850 */       binder.send(bw);
/*  851 */       byte[] bytes = baos.toByteArray();
/*  852 */       baos = null;
/*  853 */       IdcRandomAccessByteArray handler = new IdcRandomAccessByteArray(bytes);
/*  854 */       IdcZipEntry entry = newIdcZipEntry(zip, null, entryName, options);
/*  855 */       entry.m_sizeUncompressed = bytes.length;
/*  856 */       entry.m_bytesUncompressed = handler;
/*  857 */       entry.m_lastModified = System.currentTimeMillis();
/*  858 */       IdcZipUtils.deflateEntry(m_defaultZipEnvironment, entry, null);
/*  859 */       zip.putEntry(entry, -1);
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  863 */       throw new ServiceException(ibhe, msg);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  867 */       throw new ServiceException(ize, msg);
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*  871 */       throw new ServiceException(ioe, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addEntriesByName(IdcZipHandler zip, String entryName, String filePath, Map options)
/*      */     throws ServiceException
/*      */   {
/*  898 */     if (m_defaultZipEnvironment == null)
/*      */     {
/*  900 */       initZipEnvironment();
/*      */     }
/*  902 */     File file = new File(filePath);
/*  903 */     String errorKey = null;
/*  904 */     boolean isFile = file.isFile();
/*  905 */     boolean isDirectory = file.isDirectory();
/*  906 */     if (!file.exists())
/*      */     {
/*  908 */       errorKey = "syZipUnableToAddFileNotFound";
/*      */     }
/*  910 */     else if ((!isFile) && (!isDirectory))
/*      */     {
/*  912 */       errorKey = "syZipUnableToAddNotFile";
/*      */     }
/*  914 */     else if (!file.canRead())
/*      */     {
/*  916 */       errorKey = "syZipUnableToAddNoAccess";
/*      */     }
/*  918 */     if (errorKey != null)
/*      */     {
/*  920 */       IdcMessage msg = new IdcMessage(errorKey, new Object[] { filePath });
/*  921 */       throw new ServiceException(null, msg);
/*      */     }
/*  923 */     boolean endsWithSlash = entryName.endsWith("/");
/*  924 */     if (isDirectory != endsWithSlash)
/*      */     {
/*  926 */       if (isDirectory)
/*      */       {
/*  928 */         entryName = entryName + '/';
/*      */       }
/*      */       else
/*      */       {
/*  932 */         entryName = entryName.substring(0, entryName.length() - 1);
/*      */       }
/*      */     }
/*  935 */     if (options == null)
/*      */     {
/*  937 */       options = new HashMap();
/*      */     }
/*      */     try
/*      */     {
/*  941 */       IdcZipEntry entry = newIdcZipEntry(zip, filePath, entryName, options);
/*  942 */       if (!isDirectory)
/*      */       {
/*  944 */         IdcZipUtils.deflateEntry(m_defaultZipEnvironment, entry, null);
/*      */       }
/*  946 */       zip.putEntry(entry, -1);
/*      */     }
/*      */     catch (IdcByteHandlerException ibhe)
/*      */     {
/*  950 */       throw new ServiceException(ibhe);
/*      */     }
/*      */     catch (IdcZipException ize)
/*      */     {
/*  954 */       throw new ServiceException(ize);
/*      */     }
/*  956 */     boolean useRecursion = !MapUtils.getBoolValueFromMap(options, "noRecursion", false);
/*  957 */     if ((isFile) || (!useRecursion))
/*      */     {
/*  959 */       return;
/*      */     }
/*  961 */     ResultSetFilter filter = (ResultSetFilter)options.get("filter");
/*  962 */     filePath = FileUtils.directorySlashes(filePath);
/*  963 */     String[] files = file.list();
/*  964 */     Arrays.sort(files);
/*  965 */     Vector row = null;
/*  966 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/*  968 */       String filename = files[i];
/*  969 */       String thisEntryName = entryName + filename;
/*  970 */       String thisFilePath = filePath + filename;
/*  971 */       if (filter != null)
/*      */       {
/*      */         boolean thisIsDirectory;
/*      */         boolean thisIsDirectory;
/*  974 */         if (FileUtils.checkFile(thisFilePath, true, false) == 0)
/*      */         {
/*  976 */           thisIsDirectory = false;
/*      */         } else {
/*  978 */           if (FileUtils.checkFile(thisFilePath, false, false) != 0)
/*      */             continue;
/*  980 */           thisIsDirectory = true;
/*  981 */           thisEntryName = thisEntryName + '/';
/*  982 */           thisFilePath = thisFilePath + FileUtils.directorySlashes(thisFilePath);
/*      */         }
/*      */ 
/*  988 */         if (row == null)
/*      */         {
/*  990 */           row = new IdcVector(4);
/*  991 */           row.add(null);
/*  992 */           row.add(null);
/*  993 */           row.add(null);
/*  994 */           row.add(null);
/*      */         }
/*  996 */         row.set(0, thisEntryName);
/*  997 */         row.set(1, thisFilePath);
/*  998 */         row.set(2, filename);
/*      */ 
/* 1000 */         row.set(3, (thisIsDirectory) ? "1" : "");
/* 1001 */         if (filter.checkRow(filename, 0, row) != 1) {
/*      */           continue;
/*      */         }
/*      */       }
/*      */ 
/* 1006 */       addEntriesByName(zip, thisEntryName, thisFilePath, options);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addEntriesFromResultSet(IdcZipHandler zip, DataResultSet fileSet, Map options)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1034 */     if (m_defaultZipEnvironment == null)
/*      */     {
/* 1036 */       initZipEnvironment();
/*      */     }
/* 1038 */     String entryNameFieldName = "entryName"; String filePathFieldName = "filePath"; String entryNamePrefix = null;
/* 1039 */     if (options != null)
/*      */     {
/* 1041 */       String fieldName = (String)options.get("entryNameField");
/* 1042 */       if (fieldName != null)
/*      */       {
/* 1044 */         entryNameFieldName = fieldName;
/*      */       }
/* 1046 */       fieldName = (String)options.get("filePathField");
/* 1047 */       if (fieldName != null)
/*      */       {
/* 1049 */         filePathFieldName = fieldName;
/*      */       }
/* 1051 */       entryNamePrefix = (String)options.get("entryNamePrefix");
/*      */     }
/* 1053 */     FieldInfo filePathField = new FieldInfo();
/* 1054 */     boolean result = fileSet.getFieldInfo(filePathFieldName, filePathField);
/* 1055 */     if (!result)
/*      */     {
/* 1057 */       IdcMessage msg = new IdcMessage("syColumnDoesNotExist", new Object[] { filePathFieldName });
/* 1058 */       throw new DataException(null, msg);
/*      */     }
/* 1060 */     FieldInfo entryNameField = new FieldInfo();
/* 1061 */     result = fileSet.getFieldInfo(entryNameFieldName, entryNameField);
/* 1062 */     if (!result)
/*      */     {
/* 1064 */       entryNameField = filePathField;
/*      */     }
/*      */ 
/* 1067 */     FileStoreProvider fileStore = null;
/* 1068 */     Map descriptorMap = null;
/* 1069 */     if (options != null)
/*      */     {
/* 1071 */       fileStore = (FileStoreProvider)options.get("fileStore");
/* 1072 */       if (fileStore != null)
/*      */       {
/* 1074 */         descriptorMap = (Map)options.get("descriptorMap");
/*      */       }
/*      */     }
/*      */ 
/* 1078 */     for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*      */     {
/* 1080 */       String filePath = fileSet.getStringValue(filePathField.m_index);
/* 1081 */       String entryName = fileSet.getStringValue(entryNameField.m_index);
/* 1082 */       String fullEntryName = (entryNamePrefix != null) ? entryNamePrefix + entryName : entryName;
/* 1083 */       if (filePath.endsWith("/"))
/*      */       {
/*      */         try
/*      */         {
/* 1087 */           IdcZipEntry entry = newIdcZipEntry(zip, null, entryName, options);
/* 1088 */           zip.putEntry(entry, -1);
/*      */         }
/*      */         catch (IdcByteHandlerException ibhe)
/*      */         {
/* 1092 */           throw new ServiceException(ibhe);
/*      */         }
/*      */         catch (IdcZipException ize)
/*      */         {
/* 1096 */           throw new ServiceException(ize);
/*      */         }
/*      */       }
/*      */       else {
/* 1100 */         IdcFileDescriptor descriptor = (descriptorMap != null) ? (IdcFileDescriptor)descriptorMap.get(entryName) : null;
/* 1101 */         if (descriptor == null)
/*      */         {
/* 1103 */           addEntryFromFile(zip, fullEntryName, filePath, options);
/*      */         }
/*      */         else
/*      */         {
/* 1107 */           IdcMessage msg = new IdcMessage("syZipUnableToCreate", new Object[] { zip.m_description });
/* 1108 */           String lengthString = null;
/*      */           try
/*      */           {
/* 1111 */             Map storageData = fileStore.getStorageData(descriptor, null, null);
/* 1112 */             lengthString = (String)storageData.get("fileSize");
/*      */           }
/*      */           catch (IOException ioe)
/*      */           {
/* 1116 */             msg = new IdcMessage(msg, "syByteLengthBad", new Object[] { "<missing>" });
/* 1117 */             throw new ServiceException(ioe, msg);
/*      */           }
/*      */           long length;
/*      */           try
/*      */           {
/* 1122 */             length = Long.parseLong(lengthString);
/*      */           }
/*      */           catch (Throwable t)
/*      */           {
/* 1126 */             msg = new IdcMessage(msg, "syByteLengthBad", new Object[] { lengthString });
/* 1127 */             throw new ServiceException(t, msg);
/*      */           }
/* 1129 */           msg = new IdcMessage(msg, "syZipEntryDeflateError", new Object[] { entryName });
/*      */           try
/*      */           {
/* 1132 */             InputStream in = fileStore.getInputStream(descriptor, null);
/* 1133 */             InputStreamIdcByteHandler handler = new InputStreamIdcByteHandler(in, length);
/* 1134 */             IdcZipEntry entry = newIdcZipEntry(zip, null, entryName, options);
/* 1135 */             entry.m_sizeUncompressed = length;
/* 1136 */             IdcZipUtils.deflateEntry(m_defaultZipEnvironment, entry, handler);
/* 1137 */             zip.putEntry(entry, -1);
/*      */           }
/*      */           catch (IdcByteHandlerException ibhe)
/*      */           {
/* 1141 */             throw new ServiceException(ibhe, msg);
/*      */           }
/*      */           catch (IdcZipException ize)
/*      */           {
/* 1145 */             throw new ServiceException(ize, msg);
/*      */           }
/*      */           catch (IOException ioe)
/*      */           {
/* 1149 */             throw new ServiceException(ioe, msg);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1159 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99524 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.zip.IdcZipFunctions
 * JD-Core Version:    0.5.4
 */