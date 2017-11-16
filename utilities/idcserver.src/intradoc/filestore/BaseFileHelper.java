/*     */ package intradoc.filestore;
/*     */ 
/*     */ import intradoc.common.BufferPool;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.filter.PurgerInterface;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import java.io.File;
/*     */ import java.io.FileNotFoundException;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Random;
/*     */ 
/*     */ public class BaseFileHelper
/*     */   implements CommonStoreImplementor
/*     */ {
/*     */   public Provider m_provider;
/*     */   public BaseFileStore m_fileStore;
/*     */   public BufferPool m_bufferPool;
/*     */   public Random m_random;
/*     */   public FilePurgerFactory m_purger;
/*     */ 
/*     */   public void preInit(FileStoreProvider fs, Provider provider)
/*     */   {
/*  62 */     this.m_fileStore = ((BaseFileStore)fs);
/*  63 */     this.m_provider = provider;
/*     */   }
/*     */ 
/*     */   public void init(FileStoreProvider fs, Provider provider)
/*     */   {
/*  68 */     this.m_bufferPool = BufferPool.getBufferPool("FileStore");
/*  69 */     this.m_random = new Random();
/*  70 */     this.m_purger = new FilePurgerFactory(fs);
/*  71 */     this.m_purger.init();
/*     */   }
/*     */ 
/*     */   public InputStream getInputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/*  77 */     String path = descriptor.getProperty("path");
/*  78 */     InputStream stream = null;
/*     */     try
/*     */     {
/*  81 */       stream = FileUtilsCfgBuilder.getCfgInputStream(path);
/*     */     }
/*     */     catch (FileNotFoundException nfe)
/*     */     {
/*  86 */       boolean isExceptionHandled = false;
/*  87 */       if (args != null)
/*     */       {
/*  89 */         boolean supportFallbackPath = StringUtils.convertToBool((String)args.get("FileStoreSupportFallbackPath"), false);
/*  90 */         if (supportFallbackPath == true)
/*     */         {
/*  92 */           path = descriptor.getProperty("fallbackPath");
/*  93 */           if (path != null)
/*     */           {
/*  95 */             stream = FileUtilsCfgBuilder.getCfgInputStream(path);
/*  96 */             isExceptionHandled = true;
/*  97 */             Report.trace("filestore", "fallbackPath " + path + " from descriptor used to locate file as original path " + descriptor.getProperty("path") + " was not found.", null);
/*  98 */             args.put("FileStoreHandledFallbackPath", "1");
/*     */           }
/*     */         }
/*     */       }
/* 102 */       if (!isExceptionHandled)
/*     */       {
/* 104 */         throw nfe;
/*     */       }
/*     */     }
/* 107 */     return stream;
/*     */   }
/*     */ 
/*     */   public OutputStream getOutputStream(IdcFileDescriptor descriptor, Map args)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 113 */     String path = descriptor.getProperty("path");
/* 114 */     boolean isDoTemp = this.m_fileStore.getConfigBoolean("isDoTemp", args, false, false);
/* 115 */     if (isDoTemp)
/*     */     {
/* 118 */       path = path + ".tmp";
/*     */     }
/* 120 */     OutputStream out = FileUtilsCfgBuilder.getCfgOutputStream(path, null);
/* 121 */     return out;
/*     */   }
/*     */ 
/*     */   public void storeFromLocalFile(IdcFileDescriptor descriptor, File localFile, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 127 */     if (args != null)
/*     */     {
/* 129 */       String str = (String)args.get("localInPlace");
/* 130 */       boolean isLocalInPlace = StringUtils.convertToBool(str, false);
/* 131 */       if (isLocalInPlace)
/*     */       {
/* 134 */         return;
/*     */       }
/*     */ 
/* 137 */       str = (String)args.get("doRenameWhenPossible");
/* 138 */       boolean isRenameFile = StringUtils.convertToBool(str, false);
/* 139 */       if ((isRenameFile) && 
/* 141 */         (FileStoreUtils.isStoredOnFileSystem(descriptor, this.m_fileStore)))
/*     */       {
/* 143 */         String path = descriptor.getProperty("path");
/* 144 */         if (path != null)
/*     */         {
/* 146 */           File outputFile = FileUtilsCfgBuilder.getCfgFile(path, null);
/* 147 */           if (localFile.renameTo(outputFile))
/*     */           {
/* 149 */             if (outputFile.exists())
/*     */             {
/* 151 */               args.put("isUpdateCache", "true");
/* 152 */               return;
/*     */             }
/* 154 */             if (!localFile.exists())
/*     */             {
/* 158 */               Report.trace("filestore", "Attempted to rename file '" + localFile.getPath() + "' to '" + outputFile.getPath() + "'. Unable to file both files afterward. Maybe file system delay.", null);
/*     */             }
/*     */             else
/*     */             {
/* 163 */               Report.trace("filestore", "Attempted to rename file '" + localFile.getPath() + "' to '" + outputFile.getPath() + "'. Returns successful, but target file does not exist, and " + "source file is still there.", null);
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 170 */             Report.trace("filestore", "Attempted to rename file '" + localFile.getPath() + "' to '" + outputFile.getPath() + "'. Failed rename.", null);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 178 */     InputStream in = FileUtilsCfgBuilder.getCfgInputStream(localFile);
/* 179 */     storeFromInputStream(descriptor, in, args, cxt);
/*     */   }
/*     */ 
/*     */   public void storeFromInputStream(IdcFileDescriptor descriptor, InputStream inputStream, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 185 */     prepareForFileCreation(descriptor, args, cxt);
/* 186 */     OutputStream outputStream = this.m_fileStore.getOutputStream(descriptor, args);
/* 187 */     storeFromInputStreamSimple(outputStream, inputStream, args, cxt);
/*     */   }
/*     */ 
/*     */   public void storeFromInputStreamSimple(OutputStream outputStream, InputStream inputStream, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 194 */     cxt.setCachedObject("fileInputStream", inputStream);
/* 195 */     if (PluginFilters.filter("processCheckinInputStream", null, null, cxt) == 1)
/*     */     {
/* 197 */       inputStream = (InputStream)cxt.getCachedObject("processedCheckinInputStream");
/*     */     }
/*     */ 
/* 200 */     byte[] buf = (byte[])(byte[])this.m_bufferPool.getBuffer(16384, 0);
/* 201 */     long start = System.currentTimeMillis();
/* 202 */     long totalBytes = 0L;
/*     */ 
/* 204 */     int opCount = 0;
/*     */     try
/*     */     {
/* 207 */       while ((count = inputStream.read(buf)) > 0)
/*     */       {
/*     */         int count;
/* 209 */         totalBytes += count;
/* 210 */         outputStream.write(buf, 0, count);
/* 211 */         outputStream.flush();
/* 212 */         ++opCount;
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       long duration;
/* 217 */       this.m_bufferPool.releaseBuffer(buf);
/* 218 */       FileUtils.closeObjectsEx(inputStream, outputStream);
/* 219 */       long duration = System.currentTimeMillis() - start;
/* 220 */       Report.trace("filestore", "stored " + totalBytes + " in " + duration + "ms.  " + totalBytes / (duration / 1000.0D) / 1000.0D + " KBps", null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void storeFromCacheCopy(IdcFileDescriptor descriptor, InputStream inputStream, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 228 */     prepareForFileCreation(descriptor, args, cxt);
/*     */ 
/* 231 */     OutputStream outputStream = getOutputStream(descriptor, args);
/* 232 */     storeFromInputStreamSimple(outputStream, inputStream, args, cxt);
/*     */   }
/*     */ 
/*     */   public void moveFileSimple(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 238 */     String sourcePath = this.m_fileStore.getFilesystemPathWithArgs(source, args, cxt);
/* 239 */     String targetPath = this.m_fileStore.getFilesystemPathWithArgs(target, args, cxt);
/* 240 */     if (sourcePath.equals(targetPath))
/*     */     {
/* 242 */       return;
/*     */     }
/*     */ 
/* 246 */     String dir = this.m_fileStore.getContainerPath(target, null, cxt);
/* 247 */     FileUtils.checkOrCreateDirectory(dir, 10);
/*     */ 
/* 249 */     renameFile(sourcePath, targetPath, args);
/*     */ 
/* 251 */     File targetFile = FileUtilsCfgBuilder.getCfgFile(targetPath, null);
/* 252 */     if (targetFile.exists())
/*     */       return;
/* 254 */     String msg = LocaleUtils.encodeMessage("csFsRenameTargetMissing", null, targetFile);
/* 255 */     throw new IOException(msg);
/*     */   }
/*     */ 
/*     */   public void moveFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 264 */     boolean isSrcLinked = source.isLinked();
/* 265 */     boolean isTargetLinked = target.isLinked();
/* 266 */     if (isSrcLinked == isTargetLinked)
/*     */     {
/* 268 */       if (isSrcLinked)
/*     */         return;
/* 270 */       moveFileSimple(source, target, args, cxt);
/*     */     }
/* 273 */     else if (!isSrcLinked)
/*     */     {
/* 276 */       deleteFile(source, args, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 280 */       this.m_fileStore.duplicateFile(source, target, args, cxt);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void duplicateFile(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, IOException, DataException
/*     */   {
/* 288 */     if (target.isLinked())
/*     */     {
/* 291 */       if (!source.isLinked())
/*     */       {
/* 293 */         IdcDescriptorState state = (IdcDescriptorState)cxt.getCachedObject("DescriptorStates");
/*     */ 
/* 295 */         state.updateToWebless(cxt);
/*     */       }
/* 297 */       return;
/*     */     }
/* 299 */     duplicateFileSimple(source, target, args, cxt);
/*     */   }
/*     */ 
/*     */   public void duplicateFileSimple(IdcFileDescriptor source, IdcFileDescriptor target, Map args, ExecutionContext cxt)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 305 */     prepareForFileCreation(target, args, cxt);
/*     */ 
/* 307 */     String sourcePath = this.m_fileStore.getFilesystemPathWithArgs(source, args, null);
/* 308 */     String targetPath = this.m_fileStore.getFilesystemPathWithArgs(target, args, null);
/*     */ 
/* 310 */     FileUtils.copyFile(sourcePath, targetPath);
/*     */   }
/*     */ 
/*     */   public void deleteFile(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 316 */     if (descriptor.isLinked())
/*     */     {
/* 318 */       return;
/*     */     }
/*     */ 
/* 321 */     boolean isLogged = false;
/* 322 */     boolean isTransactionActivity = this.m_fileStore.getConfigBoolean("isTransactionActivity", args, false, false);
/*     */ 
/* 324 */     if ((cxt != null) && (!isTransactionActivity))
/*     */     {
/* 326 */       isLogged = prepareForDelete(descriptor, args, cxt, false, null);
/*     */     }
/* 328 */     if (isLogged)
/*     */       return;
/* 330 */     String path = descriptor.getProperty("path");
/* 331 */     File filePathObj = FileUtilsCfgBuilder.getCfgFile(path, null);
/*     */ 
/* 335 */     if (filePathObj.isFile())
/*     */     {
/* 337 */       if (FileUtils.checkFile(path, true, false) != 0)
/*     */       {
/* 339 */         Report.trace("filestore", "delete " + path + " ignored because file doesn't exist.", null);
/*     */ 
/* 341 */         return;
/*     */       }
/*     */ 
/* 344 */       if (!FileUtils.storeInDB(path))
/*     */       {
/* 346 */         PurgerInterface purger = this.m_purger.createPurger(cxt);
/* 347 */         purger.doPreDelete(filePathObj, descriptor, args);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 352 */     int numRetries = 0;
/* 353 */     if (args != null)
/*     */     {
/* 355 */       numRetries = NumberUtils.parseInteger((String)args.get("numRetries"), 0);
/*     */     }
/* 357 */     boolean deletedIt = false;
/* 358 */     int timeToWait = 1000;
/* 359 */     for (int i = 0; i < numRetries + 1; ++i)
/*     */     {
/* 362 */       FileUtils.deleteFile(path, true);
/* 363 */       deletedIt = !filePathObj.exists();
/* 364 */       if (deletedIt) {
/*     */         break;
/*     */       }
/*     */ 
/* 368 */       if (i >= numRetries)
/*     */         continue;
/* 370 */       Report.trace("system", "Retrying to delete file " + path + " waiting " + timeToWait + " milliseconds.", null);
/*     */ 
/* 372 */       if (!SystemUtils.sleep(timeToWait)) {
/*     */         break;
/*     */       }
/*     */ 
/* 376 */       if (i >= 4)
/*     */         continue;
/* 378 */       timeToWait = 2 * timeToWait;
/*     */     }
/*     */ 
/* 382 */     if (deletedIt)
/*     */       return;
/* 384 */     String msg = LocaleUtils.encodeMessage("csFsUnableToDelete", null, path);
/* 385 */     boolean isAllowFailure = this.m_fileStore.getConfigBoolean("isAllowFailure", args, false, false);
/*     */ 
/* 387 */     if (!isAllowFailure)
/*     */     {
/* 389 */       throw new IOException(msg);
/*     */     }
/* 391 */     Report.trace("system", LocaleResources.localizeMessage(msg, cxt), null);
/*     */   }
/*     */ 
/*     */   public boolean prepareForDelete(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt, boolean isLog, String[] result)
/*     */     throws ServiceException, DataException
/*     */   {
/* 399 */     List commitLog = (List)cxt.getCachedObject("FileStore:" + this.m_fileStore.m_providerName + ":commitLog");
/*     */ 
/* 401 */     if (commitLog == null)
/*     */     {
/* 403 */       return false;
/*     */     }
/*     */ 
/* 406 */     String altPath = moveFileToAlternatePath(descriptor, args, cxt, false);
/* 407 */     if (altPath != null)
/*     */     {
/* 409 */       BasicIdcFileDescriptor altDesc = (BasicIdcFileDescriptor)descriptor.createClone();
/*     */ 
/* 411 */       altDesc.put("path", altPath);
/*     */ 
/* 414 */       FileStoreUtils.addActionToCommitLog("delete", altDesc, args, this.m_fileStore.m_providerName, cxt);
/*     */ 
/* 418 */       FileStoreUtils.addActionToRollbackLog("rename", altDesc, descriptor, args, this.m_fileStore.m_providerName, cxt);
/*     */     }
/*     */ 
/* 421 */     if (result != null)
/*     */     {
/* 423 */       result[0] = altPath;
/*     */     }
/* 425 */     return true;
/*     */   }
/*     */ 
/*     */   public String getFilesystemPathWithArgs(IdcFileDescriptor descriptor, Map args, ExecutionContext context)
/*     */     throws DataException, ServiceException
/*     */   {
/* 434 */     String path = descriptor.getProperty("path");
/* 435 */     boolean isContainerPath = this.m_fileStore.getConfigBoolean("doContainerPath", args, false, false);
/*     */ 
/* 437 */     boolean isCreate = this.m_fileStore.getConfigBoolean("doCreateContainers", args, false, true);
/*     */ 
/* 439 */     if ((isContainerPath) || (isCreate))
/*     */     {
/* 441 */       String dir = null;
/* 442 */       boolean isContainer = StringUtils.convertToBool(descriptor.getProperty("isContainer"), false);
/*     */ 
/* 444 */       if (isContainer)
/*     */       {
/* 446 */         dir = path;
/*     */       }
/*     */       else
/*     */       {
/* 450 */         dir = FileUtils.getDirectory(path);
/* 451 */         dir = FileUtils.directorySlashes(dir);
/*     */       }
/* 453 */       if (isCreate)
/*     */       {
/* 455 */         int autoCreateLimit = NumberUtils.parseInteger(this.m_fileStore.getConfigValue("AutoCreateLimit", null, true), 128);
/*     */ 
/* 457 */         String renditionKey = (String)descriptor.get("RenditionId");
/* 458 */         if ((context != null) && (renditionKey != null) && 
/* 460 */           (!FileUtils.checkPathExists(dir)))
/*     */         {
/* 462 */           context.setCachedObject(renditionKey + ":newCreatedDirectoryPath", dir);
/*     */         }
/*     */ 
/* 465 */         FileUtils.checkOrCreateDirectory(dir, autoCreateLimit);
/*     */       }
/*     */ 
/* 468 */       if (isContainerPath)
/*     */       {
/* 470 */         return dir;
/*     */       }
/*     */     }
/* 473 */     return path;
/*     */   }
/*     */ 
/*     */   public void updateCacheData(IdcFileDescriptor descriptor, Map args)
/*     */   {
/* 480 */     BasicIdcFileDescriptor d = (BasicIdcFileDescriptor)descriptor;
/* 481 */     boolean addNewOnly = false;
/* 482 */     boolean doLastModified = false;
/* 483 */     boolean isLocationOnly = false;
/* 484 */     if (args != null)
/*     */     {
/* 486 */       doLastModified = this.m_fileStore.getConfigBoolean("computeLastModified", args, false, false);
/* 487 */       String addToCacheStr = (String)args.get("addNewToCache");
/* 488 */       addNewOnly = StringUtils.convertToBool(addToCacheStr, false);
/* 489 */       isLocationOnly = this.m_fileStore.getConfigBoolean("isLocationOnly", args, false, false);
/*     */     }
/*     */ 
/* 492 */     if (isLocationOnly)
/*     */     {
/* 494 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 498 */       File file = null;
/* 499 */       String path = null;
/* 500 */       String fileExistsStr = null;
/* 501 */       boolean fileExists = false;
/* 502 */       if (addNewOnly)
/*     */       {
/* 504 */         fileExistsStr = d.getCacheProperty("fileExists");
/* 505 */         if (fileExistsStr != null)
/*     */         {
/* 507 */           fileExists = StringUtils.convertToBool(fileExistsStr, false);
/*     */         }
/*     */       }
/*     */ 
/* 511 */       if ((!addNewOnly) || (fileExistsStr == null))
/*     */       {
/* 513 */         addNewOnly = false;
/* 514 */         path = this.m_fileStore.getFilesystemPathWithArgs(descriptor, args, null);
/* 515 */         file = FileUtilsCfgBuilder.getCfgFile(path, null);
/* 516 */         fileExists = file.exists();
/* 517 */         if (fileExists)
/*     */         {
/* 519 */           d.putCacheValue("fileExists", "1");
/* 520 */           long size = file.length();
/* 521 */           d.putCacheValue("fileSize", "" + size);
/*     */         }
/*     */         else
/*     */         {
/* 525 */           d.putCacheValue("fileExists", "0");
/*     */         }
/*     */       }
/* 528 */       if ((fileExists) && (doLastModified) && (((!addNewOnly) || (d.getCacheProperty("lastModified") == null))))
/*     */       {
/* 531 */         if (path == null)
/*     */         {
/* 533 */           path = this.m_fileStore.getFilesystemPathWithArgs(descriptor, args, null);
/* 534 */           file = FileUtilsCfgBuilder.getCfgFile(path, null);
/*     */         }
/* 536 */         long lastModified = file.lastModified();
/* 537 */         d.putCacheValue("lastModified", "" + lastModified);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 542 */       Report.trace("filestore", "Unable to updateCacheData for " + descriptor.get("uniqueId"), e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareForFileCreation(IdcFileDescriptor descriptor, Map args, ExecutionContext cxt)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 553 */     boolean isCreate = false;
/* 554 */     if (args != null)
/*     */     {
/* 556 */       isCreate = StringUtils.convertToBool((String)args.get("isNew"), true);
/*     */     }
/* 558 */     Report.trace("filestore", "prepareForFileCreation: isCreate=" + isCreate + " originalPath=" + descriptor.getProperty("path"), null);
/*     */ 
/* 560 */     if (isCreate)
/*     */     {
/* 562 */       String path = null;
/* 563 */       BasicIdcFileDescriptor bDesc = (BasicIdcFileDescriptor)descriptor;
/* 564 */       boolean isMove = this.m_fileStore.getConfigBoolean("isMove", args, false, false);
/* 565 */       boolean exists = StringUtils.convertToBool(bDesc.getCacheProperty("fileExists"), false);
/* 566 */       boolean isAllowFailure = this.m_fileStore.getConfigBoolean("isAllowFailure", args, false, false);
/* 567 */       Report.trace("filestore", "prepareForFileCreation: isMove=" + isMove + " exists=" + exists, null);
/*     */ 
/* 569 */       if (exists)
/*     */       {
/* 571 */         path = moveFileToAlternatePath(descriptor, args, cxt, !isAllowFailure);
/* 572 */         bDesc.putCacheValue("fileExists", "0");
/* 573 */         if ((path != null) && (cxt != null))
/*     */         {
/* 575 */           FileStoreUtils.addActionToRollbackLog("rename", path, bDesc.getProperty("path"), args, this.m_fileStore.m_providerName, cxt);
/*     */ 
/* 578 */           if (isMove)
/*     */           {
/* 582 */             FileStoreUtils.addActionToCommitLog("delete", path, args, this.m_fileStore.m_providerName, cxt);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 589 */         if (args == null)
/*     */         {
/* 591 */           args = new HashMap();
/*     */         }
/* 593 */         args.put("doCreateContainers", "1");
/* 594 */         path = getFilesystemPathWithArgs(descriptor, args, cxt);
/*     */ 
/* 596 */         if ((cxt != null) && (!isMove))
/*     */         {
/* 598 */           FileStoreUtils.addActionToRollbackLog("delete", path, null, args, this.m_fileStore.m_providerName, cxt);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 604 */     boolean isForce = this.m_fileStore.getConfigBoolean("isForce", args, true, false);
/* 605 */     boolean isDoBackup = this.m_fileStore.getConfigBoolean("isDoBackup", args, true, false);
/* 606 */     if ((!isForce) || (isDoBackup))
/*     */       return;
/* 608 */     this.m_fileStore.deleteFile(descriptor, args, null);
/*     */   }
/*     */ 
/*     */   public void checkOrCreateDirectory(String dir, IdcFileDescriptor descriptor)
/*     */     throws ServiceException
/*     */   {
/* 615 */     String storageRule = descriptor.getProperty("StorageRule");
/* 616 */     Map storeMap = (Map)this.m_provider.getProviderObject("StorageRules");
/* 617 */     int autoCreateLimit = 0;
/* 618 */     if (storeMap != null)
/*     */     {
/* 620 */       StorageRule rule = (StorageRule)storeMap.get(storageRule);
/* 621 */       if (rule != null)
/*     */       {
/* 623 */         String storageClass = descriptor.getProperty("StorageClass");
/* 624 */         Properties pathConfig = (Properties)rule.getPathConfig(storageClass);
/* 625 */         String autoCreateLimitString = pathConfig.getProperty("AutoCreateLimit");
/* 626 */         autoCreateLimit = NumberUtils.parseInteger(autoCreateLimitString, 128);
/*     */       }
/*     */     }
/* 629 */     if (autoCreateLimit == 0)
/*     */     {
/* 631 */       autoCreateLimit = NumberUtils.parseInteger(this.m_fileStore.getConfigValue("AutoCreateLimit", null, true), 128);
/*     */     }
/*     */ 
/* 634 */     FileUtils.checkOrCreateDirectory(dir, autoCreateLimit);
/*     */   }
/*     */ 
/*     */   public String moveFileToAlternatePath(IdcFileDescriptor d, Map args, ExecutionContext cxt, boolean isLog)
/*     */     throws ServiceException
/*     */   {
/* 640 */     if (d instanceof BasicIdcFileDescriptor)
/*     */     {
/* 642 */       BasicIdcFileDescriptor bd = (BasicIdcFileDescriptor)d;
/* 643 */       boolean exists = StringUtils.convertToBool(bd.getCacheProperty("fileExists"), false);
/* 644 */       if (!exists)
/*     */       {
/* 646 */         return null;
/*     */       }
/*     */     }
/*     */ 
/* 650 */     String path = d.getProperty("path");
/* 651 */     Report.trace("filestore", "BaseFileHelper.moveFileToAlternatePath: path=" + path, null);
/* 652 */     String msg = LocaleUtils.encodeMessage("syFileExists", null, path);
/* 653 */     String altPath = null;
/*     */     try
/*     */     {
/* 657 */       cxt.setCachedObject("moveDescriptor", d);
/* 658 */       if (PluginFilters.filter("moveFileToAlternatePath", null, null, cxt) != -1)
/*     */       {
/* 660 */         altPath = (String)cxt.getCachedObject("alternatePath");
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 665 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 668 */     if (altPath == null)
/*     */     {
/* 670 */       for (int suffix = 0; suffix < 10; ++suffix)
/*     */       {
/* 672 */         int r = this.m_random.nextInt() >>> 1;
/* 673 */         altPath = path + "." + r + "." + suffix;
/* 674 */         if (FileUtils.checkFile(altPath, false, false) != 0) {
/*     */           break;
/*     */         }
/*     */ 
/* 678 */         altPath = null;
/*     */       }
/*     */     }
/*     */ 
/* 682 */     boolean isAllowFailure = this.m_fileStore.getConfigBoolean("isAllowFailure", args, false, false);
/* 683 */     if (altPath != null)
/*     */     {
/* 685 */       boolean errorOnReadOnly = this.m_fileStore.getConfigBoolean("ErrorOnReadOnly", args, true, true);
/* 686 */       File file = FileUtilsCfgBuilder.getCfgFile(path, null);
/* 687 */       String eMsg = LocaleUtils.encodeMessage("csFsMovingFileFailed", null, path, altPath);
/* 688 */       if ((errorOnReadOnly) && (file.exists()) && (!file.canWrite()))
/*     */       {
/* 690 */         Report.trace("filestore", eMsg, null);
/* 691 */         String errMsg = LocaleUtils.encodeMessage("csFsReadOnlyError", null);
/* 692 */         throw new ServiceException(errMsg);
/*     */       }
/*     */       try
/*     */       {
/* 696 */         if (isLog)
/*     */         {
/* 698 */           String msg2 = LocaleUtils.encodeMessage("csFsMovingFile", null, path, altPath);
/* 699 */           msg = LocaleUtils.appendMessage(msg2, msg);
/* 700 */           ServiceException e = new ServiceException("csFsTrackMoveFailure");
/* 701 */           Report.warning("filestore", msg, e);
/*     */         }
/* 703 */         renameFile(path, altPath, args);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 707 */         Report.trace("filestore", eMsg, e);
/* 708 */         if (!isAllowFailure)
/*     */         {
/* 710 */           throw e;
/*     */         }
/* 712 */         altPath = null;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 717 */       msg = LocaleUtils.encodeMessage("syUnableToCreateFile", msg, path);
/* 718 */       throw new ServiceException(msg);
/*     */     }
/* 720 */     return altPath;
/*     */   }
/*     */ 
/*     */   protected void renameFile(String sourcePath, String targetPath, Map args)
/*     */     throws ServiceException
/*     */   {
/* 726 */     Report.trace("filestore", "BaseFileHelper.moveFileToAlternatePath: rename file to " + targetPath, null);
/*     */ 
/* 728 */     boolean isCopyDelete = this.m_fileStore.getConfigBoolean("FsIsCopyDeleteOnRename", args, false, true);
/*     */ 
/* 730 */     int flag = 0;
/* 731 */     if (isCopyDelete)
/*     */     {
/* 733 */       flag = 8;
/*     */     }
/* 735 */     FileUtils.renameFileEx(sourcePath, targetPath, flag);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 740 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99354 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.BaseFileHelper
 * JD-Core Version:    0.5.4
 */