/*     */ package intradoc.server.converter;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConverterUtils
/*     */ {
/*     */   public static final String F_PAGE_IMAGES_INPROGRESS_MARKER = "inprocess.dat";
/*     */   public static final String F_PAGE_IMAGES_ISQUEUED_MARKER = "isqueued.dat";
/*  59 */   protected static ExecutionContext m_converterUtilsCxt = new ExecutionContextAdaptor();
/*     */ 
/*     */   public static boolean isConversionFormat(String format)
/*     */   {
/*  63 */     return isConversionFormat(format, null);
/*     */   }
/*     */ 
/*     */   public static boolean isConversionFormat(String format, String conversionFormat)
/*     */   {
/*  68 */     if (format == null)
/*     */     {
/*  70 */       return false;
/*     */     }
/*     */ 
/*  73 */     if (conversionFormat == null)
/*     */     {
/*  75 */       conversionFormat = SharedObjects.getEnvironmentValue("conversionFormat");
/*     */     }
/*  77 */     if (conversionFormat == null)
/*     */     {
/*  79 */       return false;
/*     */     }
/*     */ 
/*  82 */     Vector conversionList = StringUtils.parseArray(conversionFormat, ',', '^');
/*  83 */     int convSize = conversionList.size();
/*  84 */     for (int i = 0; i < convSize; ++i)
/*     */     {
/*  86 */       String curConv = (String)conversionList.elementAt(i);
/*  87 */       curConv = curConv.trim();
/*  88 */       if (curConv.equalsIgnoreCase(format))
/*     */       {
/*  90 */         return true;
/*     */       }
/*     */     }
/*  93 */     return false;
/*     */   }
/*     */ 
/*     */   public static String getWebFormat(DataBinder binder) throws ServiceException
/*     */   {
/*  98 */     String webExt = binder.getAllowMissing("dWebExtension");
/*  99 */     if ((webExt == null) || (webExt.equals("")))
/*     */     {
/* 101 */       return null;
/*     */     }
/*     */ 
/* 104 */     DataResultSet drset = SharedObjects.getTable("ExtensionFormatMap");
/* 105 */     if (drset == null)
/*     */     {
/* 107 */       Report.trace("dynamicconverter", "ExtensionFormatMap table could not be loaded.", null);
/*     */     }
/*     */ 
/* 110 */     int extIndex = -1;
/* 111 */     int formatIndex = 1;
/*     */     try
/*     */     {
/* 114 */       extIndex = ResultSetUtils.getIndexMustExist(drset, "dExtension");
/* 115 */       formatIndex = ResultSetUtils.getIndexMustExist(drset, "dFormat");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 119 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 122 */     String webFormat = null;
/* 123 */     Vector row = drset.findRow(extIndex, webExt);
/* 124 */     if (row == null)
/*     */     {
/* 126 */       webFormat = "application/" + webExt;
/*     */     }
/*     */     else
/*     */     {
/* 130 */       webFormat = (String)row.elementAt(formatIndex);
/*     */     }
/* 132 */     return webFormat;
/*     */   }
/*     */ 
/*     */   public static String[] getExternalCollectionInfo(String collectionID)
/*     */     throws DataException, ServiceException
/*     */   {
/* 138 */     DataResultSet drset = SharedObjects.getTable("SearchCollections");
/* 139 */     if (drset == null)
/*     */     {
/* 141 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadTable", null, "SearchCollections");
/*     */ 
/* 143 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 146 */     String physicalPath = null;
/* 147 */     String relativePath = null;
/*     */ 
/* 149 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "sCollectionID", "sPhysicalFileRoot", "sRelativeWebRoot" }, true);
/*     */ 
/* 152 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 154 */       String currentCollection = drset.getStringValue(info[0].m_index);
/* 155 */       if (!currentCollection.equals(collectionID))
/*     */         continue;
/* 157 */       physicalPath = drset.getStringValue(info[1].m_index);
/* 158 */       relativePath = drset.getStringValue(info[2].m_index);
/*     */ 
/* 160 */       physicalPath = FileUtils.directorySlashes(physicalPath);
/* 161 */       relativePath = FileUtils.directorySlashes(relativePath);
/*     */ 
/* 163 */       break;
/*     */     }
/*     */ 
/* 167 */     String[] externalValues = new String[2];
/* 168 */     externalValues[0] = physicalPath;
/* 169 */     externalValues[1] = relativePath;
/*     */ 
/* 171 */     return externalValues;
/*     */   }
/*     */ 
/*     */   public static long getOutputDirSize(String outputDir)
/*     */   {
/* 177 */     long dirSize = 0L;
/*     */ 
/* 179 */     File outputFile = new File(outputDir);
/* 180 */     if (!outputFile.exists())
/*     */     {
/* 182 */       return 0L;
/*     */     }
/* 184 */     String[] fileNames = null;
/* 185 */     if ((outputDir.contains("@d")) && (outputDir.contains(".dir")))
/*     */     {
/* 190 */       String filter = "pages*.*";
/* 191 */       fileNames = FileUtils.getMatchingFileNames(outputDir, "pages*.*");
/*     */     }
/*     */     else
/*     */     {
/* 195 */       fileNames = outputFile.list();
/*     */     }
/* 197 */     int numFiles = fileNames.length;
/* 198 */     for (int i = 0; i < numFiles; ++i)
/*     */     {
/* 200 */       String currentFileName = fileNames[i];
/* 201 */       File currentFile = new File(outputDir + "/" + currentFileName);
/* 202 */       if (!currentFile.isFile())
/*     */         continue;
/* 204 */       dirSize += currentFile.length();
/*     */     }
/*     */ 
/* 207 */     Report.trace("dynamicconverter", "cache dir: " + outputDir + "; has cache size (bytes): " + dirSize, null);
/*     */ 
/* 209 */     return dirSize;
/*     */   }
/*     */ 
/*     */   public static Properties getConversionSumValues(Workspace ws) throws DataException
/*     */   {
/* 214 */     String totalCachedStr = "0";
/* 215 */     String totalFilesStr = "0";
/* 216 */     String lastAccessed = "";
/* 217 */     String lastKey = "";
/*     */ 
/* 219 */     DataBinder binder = new DataBinder();
/* 220 */     ResultSet rset = ws.createResultSet("QhtmlConversionSums", binder);
/* 221 */     if ((rset == null) || (!rset.isRowPresent()))
/*     */     {
/* 224 */       binder.putLocal("dTotalCached", "0");
/* 225 */       binder.putLocal("dTotalFiles", "0");
/* 226 */       binder.putLocal("dLastAccessed", "");
/* 227 */       binder.putLocal("dLastKey", "");
/*     */     }
/*     */     else
/*     */     {
/* 232 */       FieldInfo[] info = ResultSetUtils.createInfoList(rset, new String[] { "dTotalCached", "dTotalFiles", "dLastAccessed", "dLastKey" }, true);
/*     */ 
/* 235 */       totalCachedStr = rset.getStringValue(info[0].m_index);
/* 236 */       totalFilesStr = rset.getStringValue(info[1].m_index);
/* 237 */       lastAccessed = rset.getStringValue(info[2].m_index);
/* 238 */       lastKey = rset.getStringValue(info[3].m_index);
/*     */     }
/*     */ 
/* 241 */     Properties props = new Properties();
/* 242 */     props.put("dTotalCached", totalCachedStr);
/* 243 */     props.put("dTotalFiles", totalFilesStr);
/* 244 */     props.put("dLastAccessed", lastAccessed);
/* 245 */     props.put("dLastKey", lastKey);
/*     */ 
/* 247 */     return props;
/*     */   }
/*     */ 
/*     */   public static void setConversionSumValues(Workspace ws, Properties props)
/*     */     throws DataException
/*     */   {
/* 253 */     boolean recordExists = false;
/* 254 */     DataBinder binder = new DataBinder();
/* 255 */     ResultSet rset = ws.createResultSet("QhtmlConversionSums", binder);
/* 256 */     if ((rset != null) && (rset.isRowPresent()))
/*     */     {
/* 258 */       recordExists = true;
/*     */     }
/*     */ 
/* 261 */     binder.setLocalData(props);
/*     */ 
/* 263 */     if (recordExists)
/*     */     {
/* 265 */       ws.execute("UhtmlConversionSums", binder);
/*     */     }
/*     */     else
/*     */     {
/* 269 */       ws.execute("IhtmlConversionSums", binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void deleteExpiredConversions(Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/* 276 */     String convDir = DirectoryLocator.getAppDataDirectory() + "conversion/";
/* 277 */     int result = FileUtils.checkFile(convDir, false, true);
/* 278 */     if (result != 0)
/*     */     {
/* 281 */       ServiceException e = new ServiceException(null, result, "Diagnostic trace only.", new Object[0]);
/* 282 */       Report.trace("system", "Unable to access the directory " + convDir, e);
/* 283 */       return;
/*     */     }
/*     */ 
/* 287 */     Properties sumProps = new Properties();
/* 288 */     sumProps = getConversionSumValues(ws);
/* 289 */     long totalCached = Long.parseLong(sumProps.getProperty("dTotalCached"));
/* 290 */     long totalFiles = Long.parseLong(sumProps.getProperty("dTotalFiles"));
/*     */ 
/* 292 */     long totalCachedRemoved = 0L;
/* 293 */     long totalFilesRemoved = 0L;
/*     */ 
/* 296 */     DataBinder cleanupReport = new DataBinder();
/* 297 */     FileUtils.reserveDirectory(convDir);
/*     */     try
/*     */     {
/* 300 */       ResourceUtils.serializeDataBinder(convDir, "cachecleanupreport.hda", cleanupReport, false, false);
/*     */     }
/*     */     finally
/*     */     {
/* 305 */       FileUtils.releaseDirectory(convDir);
/*     */     }
/*     */ 
/* 310 */     long maxCacheSize = SharedObjects.getEnvironmentInt("MaxConversionCacheSizeInMegs", 10000) * 1048576L;
/*     */ 
/* 312 */     long expirePeriod = SharedObjects.getEnvironmentInt("ConversionCacheExpirationPeriodInDays", 7) * 86400000L;
/*     */ 
/* 317 */     long startTime = System.currentTimeMillis();
/* 318 */     long expiredTime = startTime - expirePeriod;
/* 319 */     Date expiredDate = new Date(expiredTime);
/* 320 */     long timeOutInterval = 1800000L;
/*     */ 
/* 323 */     DataBinder conversionBinder = new DataBinder();
/* 324 */     conversionBinder.putLocal("dConversionDate", LocaleUtils.formatODBC(expiredDate));
/*     */ 
/* 326 */     boolean rowProcessed = false;
/* 327 */     boolean isAbort = false;
/* 328 */     Date lastProcessedDate = null;
/* 329 */     String lastProcessedFromReport = cleanupReport.getLocal("lastProcessedDate");
/* 330 */     if (lastProcessedFromReport != null)
/*     */     {
/* 332 */       long lDate = NumberUtils.parseLong(lastProcessedFromReport, 0L);
/* 333 */       if (lDate != 0L)
/*     */       {
/* 335 */         lastProcessedDate = new Date(lDate);
/*     */       }
/*     */     }
/* 338 */     boolean mustDelete = maxCacheSize < totalCached;
/* 339 */     Report.trace("dynamicconverter", "Conversion Cache Cleanup: expiredDate=" + LocaleUtils.formatODBC(expiredDate) + "totalCached=" + totalCached + " totalFiles=" + totalFiles + " mustDelete=" + mustDelete, null);
/*     */ 
/* 343 */     int numToCopy = 2000;
/* 344 */     while (!isAbort)
/*     */     {
/* 347 */       long currentTime = System.currentTimeMillis();
/* 348 */       if (currentTime - startTime > 1800000L)
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/* 354 */       String conversionListQuery = "QhtmlConversionsByDate";
/* 355 */       if (lastProcessedDate != null)
/*     */       {
/* 357 */         conversionBinder.putLocal("lastProcessedDate", LocaleUtils.formatODBC(lastProcessedDate));
/*     */ 
/* 359 */         conversionListQuery = "QhtmlConversionsByDateRange";
/*     */       }
/* 361 */       IdcStringBuilder msg = new IdcStringBuilder("Executing query " + conversionListQuery);
/* 362 */       if ((lastProcessedDate != null) && (!mustDelete))
/*     */       {
/* 364 */         msg.append(" looking for expired conversions after " + LocaleUtils.formatODBC(lastProcessedDate));
/*     */       }
/*     */       else
/*     */       {
/* 369 */         msg.append(" looking for any conversion that is expired");
/*     */       }
/* 371 */       Report.trace("dynamicconverter", msg.toString(), null);
/*     */ 
/* 373 */       ResultSet rset = ws.createResultSet(conversionListQuery, conversionBinder);
/* 374 */       if (rset == null) break; if (!rset.isRowPresent())
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/* 379 */       rowProcessed = true;
/*     */ 
/* 382 */       DataResultSet drset = new DataResultSet();
/* 383 */       drset.copy(rset, 2000);
/* 384 */       int rowsRemoved = 0;
/*     */ 
/* 386 */       FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "dConversionKey", "dConversionDate" }, true);
/*     */ 
/* 388 */       for (drset.first(); (drset.isRowPresent()) && (!isAbort); drset.next())
/*     */       {
/* 390 */         String conversionKey = drset.getStringValue(info[0].m_index);
/* 391 */         Date conversionDate = drset.getDateValue(info[1].m_index);
/*     */ 
/* 394 */         conversionBinder.putLocal("dConversionKey", conversionKey);
/* 395 */         ResultSet currentSet = ws.createResultSet("QhtmlConversions", conversionBinder);
/* 396 */         if ((currentSet == null) || (!currentSet.isRowPresent()))
/*     */         {
/* 398 */           Report.trace("dynamicconverter", "Record for " + conversionKey + " has been deleted before it could be processed, aborting", null);
/*     */ 
/* 400 */           isAbort = true;
/*     */         }
/*     */         else {
/* 403 */           FieldInfo[] currentInfo = ResultSetUtils.createInfoList(currentSet, new String[] { "dConversionKey", "dConversionDate", "dOutputFile" }, true);
/*     */ 
/* 405 */           Date currentDate = currentSet.getDateValue(currentInfo[1].m_index);
/* 406 */           if (currentDate.after(expiredDate))
/*     */           {
/* 408 */             Report.trace("dynamicconverter", "Record for " + conversionKey + " has been accessed before it could be processed", null);
/*     */           }
/*     */           else
/*     */           {
/* 413 */             String relativeOutputFilePath = currentSet.getStringValue(currentInfo[2].m_index);
/*     */ 
/* 416 */             lastProcessedDate = conversionDate;
/*     */ 
/* 429 */             long[] outputDirSize = { 0L };
/* 430 */             String[] dirToDelete = { null };
/* 431 */             String[] docNameToTest = { null };
/* 432 */             mustDelete = maxCacheSize < totalCached;
/* 433 */             boolean isDirLocked = false;
/* 434 */             if (relativeOutputFilePath.equalsIgnoreCase("---"))
/*     */             {
/* 436 */               dirToDelete[0] = relativeOutputFilePath;
/*     */             }
/*     */             else
/*     */             {
/* 440 */               isDirLocked = checkForLockedDirectory(conversionKey, relativeOutputFilePath, outputDirSize, dirToDelete, docNameToTest, mustDelete);
/*     */             }
/*     */ 
/* 443 */             if ((isDirLocked) && (docNameToTest[0] != null) && (docNameToTest[0].length() > 0))
/*     */             {
/* 446 */               conversionBinder.putLocal("dDocName", docNameToTest[0]);
/* 447 */               ResultSet testRset = ws.createResultSet("QtestForActiveDocName", conversionBinder);
/*     */ 
/* 453 */               if ((testRset != null) && (testRset.isRowPresent()))
/*     */               {
/* 456 */                 StringTokenizer st = new StringTokenizer(conversionKey, "~");
/* 457 */                 st.nextToken();
/* 458 */                 String dIDInConversionKey = st.nextToken();
/*     */ 
/* 461 */                 boolean dIDFound = false;
/* 462 */                 String dIDActiveDoc = null;
/* 463 */                 for (testRset.first(); testRset.isRowPresent(); testRset.next())
/*     */                 {
/* 465 */                   dIDActiveDoc = ResultSetUtils.getValue(testRset, "dID");
/* 466 */                   if (!dIDActiveDoc.equals(dIDInConversionKey))
/*     */                     continue;
/* 468 */                   dIDFound = true;
/* 469 */                   break;
/*     */                 }
/*     */ 
/* 472 */                 isDirLocked = dIDFound;
/*     */               }
/*     */               else
/*     */               {
/* 476 */                 isDirLocked = false;
/*     */               }
/*     */             }
/*     */ 
/* 480 */             Report.trace("dynamicconverter", "Processing cache entry: " + conversionKey + "; in dir: " + relativeOutputFilePath + "; is locked: " + isDirLocked + "; must delete: " + mustDelete, null);
/*     */ 
/* 483 */             if (isDirLocked)
/*     */               continue;
/*     */             try
/*     */             {
/* 487 */               ws.beginTran();
/*     */ 
/* 500 */               currentSet = ws.createResultSet("QhtmlConversions", conversionBinder);
/* 501 */               boolean doDelete = true;
/* 502 */               if ((currentSet == null) || (!currentSet.isRowPresent()))
/*     */               {
/* 504 */                 Report.trace("dynamicconverter", "Record has been deleted before it could be processed, aborting", null);
/*     */ 
/* 506 */                 doDelete = false;
/* 507 */                 isAbort = true;
/*     */               }
/*     */               else
/*     */               {
/* 511 */                 currentDate = currentSet.getDateValue(currentInfo[1].m_index);
/* 512 */                 if (currentDate.after(expiredDate))
/*     */                 {
/* 514 */                   Report.trace("dynamicconverter", "Record has been accessed before it could be processed", null);
/*     */ 
/* 516 */                   doDelete = false;
/*     */                 }
/*     */               }
/* 519 */               if (doDelete)
/*     */               {
/* 521 */                 deleteConversion(ws, conversionKey, dirToDelete[0]);
/*     */ 
/* 523 */                 Object skipHtmlConversionSumsObj = m_converterUtilsCxt.getCachedObject("SkipHtmlConversionSums");
/* 524 */                 boolean skipHtmlConversionSums = false;
/* 525 */                 if ((skipHtmlConversionSumsObj != null) && (skipHtmlConversionSumsObj instanceof Boolean))
/*     */                 {
/* 527 */                   skipHtmlConversionSums = ((Boolean)skipHtmlConversionSumsObj).booleanValue();
/*     */                 }
/*     */ 
/* 530 */                 if (!skipHtmlConversionSums)
/*     */                 {
/* 535 */                   sumProps = getConversionSumValues(ws);
/* 536 */                   totalCached = Long.parseLong(sumProps.getProperty("dTotalCached"));
/* 537 */                   totalFiles = Long.parseLong(sumProps.getProperty("dTotalFiles"));
/* 538 */                   totalCached -= outputDirSize[0];
/* 539 */                   totalFiles -= 1L;
/* 540 */                   String lastKey = sumProps.getProperty("dLastKey");
/* 541 */                   sumProps.clear();
/* 542 */                   sumProps.put("dTotalCached", String.valueOf(totalCached));
/* 543 */                   sumProps.put("dTotalFiles", String.valueOf(totalFiles));
/* 544 */                   sumProps.put("dLastKey", lastKey);
/*     */ 
/* 546 */                   Date dte = new Date();
/* 547 */                   sumProps.put("dLastAccessed", LocaleUtils.formatODBC(dte));
/*     */ 
/* 549 */                   setConversionSumValues(ws, sumProps);
/*     */                 }
/* 551 */                 ws.commitTran();
/*     */ 
/* 553 */                 totalCachedRemoved += outputDirSize[0];
/* 554 */                 totalFilesRemoved += 1L;
/* 555 */                 ++rowsRemoved;
/*     */ 
/* 560 */                 ws.releaseConnection();
/*     */               }
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 565 */               Report.trace("dynamicconverter", null, e);
/* 566 */               ws.rollbackTran();
/* 567 */               throw new ServiceException(e);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 577 */       if (drset.getNumRows() < 2000) break; if (rowsRemoved < 200)
/*     */       {
/*     */         break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 584 */     if ((isAbort) || (lastProcessedDate == null) || (!rowProcessed))
/*     */       return;
/* 586 */     Report.trace(null, "Creating report: lastProcessedDate=" + LocaleUtils.formatODBC(lastProcessedDate) + " totalCachedRemoved=" + totalCachedRemoved + " totalFilesRemoved=" + totalFilesRemoved + " maxCacheSize=" + maxCacheSize, null);
/*     */ 
/* 594 */     long dateLong = lastProcessedDate.getTime();
/* 595 */     cleanupReport.putLocal("lastProcessedDate", "" + dateLong);
/*     */ 
/* 609 */     String dateOdbc = LocaleUtils.formatODBC(lastProcessedDate);
/* 610 */     cleanupReport.putLocal("lastProcessedDateOdbc", dateOdbc);
/* 611 */     cleanupReport.putLocal("totalCachedRemoved", "" + totalCachedRemoved);
/* 612 */     cleanupReport.putLocal("totalFilesRemoved", "" + totalFilesRemoved);
/* 613 */     cleanupReport.putLocal("maxCacheSize", "" + maxCacheSize);
/* 614 */     cleanupReport.putLocal("startTime", LocaleUtils.formatODBC(new Date(startTime)));
/* 615 */     cleanupReport.putLocal("expiredDate", LocaleUtils.formatODBC(expiredDate));
/* 616 */     cleanupReport.putLocal("totalCache", "" + totalCached);
/* 617 */     cleanupReport.putLocal("totalFiles", "" + totalFiles);
/*     */ 
/* 620 */     FileUtils.reserveDirectory(convDir);
/*     */     try
/*     */     {
/* 623 */       ResourceUtils.serializeDataBinder(convDir, "cachecleanupreport.hda", cleanupReport, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 628 */       FileUtils.releaseDirectory(convDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean checkForLockedDirectory(String conversionKey, String relativeOutputFilePath, long[] outputDirSize, String[] dirToDelete, String[] docNameToTest, boolean mustDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 649 */     outputDirSize[0] = 0L;
/* 650 */     dirToDelete[0] = null;
/*     */ 
/* 653 */     boolean isExternal = false;
/* 654 */     int index = conversionKey.indexOf("~");
/* 655 */     if (index < 0)
/*     */     {
/* 657 */       return false;
/*     */     }
/*     */ 
/* 660 */     String collectionID = conversionKey.substring(0, index);
/* 661 */     if (!collectionID.equalsIgnoreCase("local"))
/*     */     {
/* 663 */       isExternal = true;
/*     */     }
/*     */ 
/* 667 */     String outputDir = null;
/* 668 */     String outputFilePath = null;
/* 669 */     if (isExternal)
/*     */     {
/* 672 */       String[] externalInfo = getExternalCollectionInfo(collectionID);
/* 673 */       String physicalPath = externalInfo[0];
/* 674 */       String relativePath = externalInfo[1];
/*     */ 
/* 676 */       if ((physicalPath == null) || (physicalPath.equals("")) || (relativePath == null) || (relativePath.equals("")))
/*     */       {
/* 679 */         String msg = LocaleUtils.encodeMessage("csExternalCollectionInvalid", null, collectionID);
/*     */ 
/* 681 */         Report.error(null, msg, null);
/* 682 */         return false;
/*     */       }
/* 684 */       physicalPath = FileUtils.directorySlashes(physicalPath);
/* 685 */       relativePath = FileUtils.directorySlashes(relativePath);
/*     */ 
/* 689 */       if (!relativeOutputFilePath.toUpperCase().startsWith(relativePath.toUpperCase()))
/*     */       {
/* 691 */         String msg = LocaleUtils.encodeMessage("csExternalCollectionPathsNotMatch", null);
/* 692 */         Report.error(null, msg, null);
/* 693 */         return false;
/*     */       }
/*     */ 
/* 697 */       index = relativePath.length();
/* 698 */       outputFilePath = physicalPath + relativeOutputFilePath.substring(index);
/*     */     }
/*     */     else
/*     */     {
/* 702 */       outputFilePath = FileUtils.getAbsolutePath(SharedObjects.getEnvironmentValue("WeblayoutDir"), relativeOutputFilePath);
/*     */     }
/*     */ 
/* 706 */     outputDir = FileUtils.getDirectory(outputFilePath);
/* 707 */     outputDir = FileUtils.directorySlashes(outputDir);
/*     */ 
/* 709 */     boolean inProgress = isPageImageExportInProgress(outputDir);
/* 710 */     if (inProgress)
/*     */     {
/* 713 */       return true;
/*     */     }
/*     */ 
/* 718 */     File file = new File(outputFilePath);
/* 719 */     if (!file.exists())
/*     */     {
/* 721 */       Report.trace("dynamicconverter", "cache dir not locked; path not present: " + file.getAbsolutePath(), null);
/*     */ 
/* 723 */       return false;
/*     */     }
/*     */ 
/* 726 */     outputDirSize[0] = getOutputDirSize(outputDir);
/*     */ 
/* 729 */     File deleteFile = new File(outputDir);
/* 730 */     if (!deleteFile.exists())
/*     */     {
/* 732 */       Report.trace("dynamicconverter", "cache dir not locked; path not present: " + deleteFile.getAbsolutePath(), null);
/*     */ 
/* 734 */       return false;
/*     */     }
/* 736 */     dirToDelete[0] = outputDir;
/* 737 */     if ((isExternal) || (mustDelete))
/*     */     {
/* 739 */       Report.trace("dynamicconverter", "cache dir not locked; isExternal: " + isExternal + "mustDelete: " + mustDelete, null);
/*     */ 
/* 741 */       return false;
/*     */     }
/*     */ 
/* 745 */     DataBinder data = new DataBinder();
/* 746 */     if (!ResourceUtils.serializeDataBinder(outputDir, "forcedconversion.hda", data, false, false))
/*     */     {
/* 749 */       Report.trace("dynamicconverter", "cache dir not locked; no forced conversion data", null);
/*     */ 
/* 751 */       return false;
/*     */     }
/*     */ 
/* 754 */     Report.trace("dynamicconverter", "forcedconversion data: " + data.toString(), null);
/*     */ 
/* 757 */     String docName = data.getLocal("dDocName");
/* 758 */     if ((docName == null) || (docName.length() == 0))
/*     */     {
/* 760 */       Report.trace("dynamicconverter", "cache dir not locked; missing dDocName", null);
/* 761 */       return false;
/*     */     }
/*     */ 
/* 764 */     docNameToTest[0] = docName;
/*     */ 
/* 766 */     Report.trace("dynamicconverter", "cache dir locked; dDocName: " + docName, null);
/* 767 */     return true;
/*     */   }
/*     */ 
/*     */   public static void deleteConversion(Workspace ws, String conversionKey, String dirToDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 774 */     DataBinder deleteBinder = new DataBinder();
/* 775 */     deleteBinder.putLocal("dConversionKey", conversionKey);
/* 776 */     ws.execute("DhtmlConversion", deleteBinder);
/*     */ 
/* 778 */     if (dirToDelete == null)
/*     */       return;
/* 780 */     deleteBinder.putLocal("DirToDelete", dirToDelete);
/*     */ 
/* 782 */     int ret = PluginFilters.filter("deleteCachedConversion", ws, deleteBinder, m_converterUtilsCxt);
/* 783 */     if (ret == -1)
/*     */     {
/* 785 */       String errMsg = LocaleResources.localizeMessage("!csFilterError,deleteCachedConversion", null);
/* 786 */       throw new DataException(errMsg);
/*     */     }
/* 788 */     if (ret == 1)
/*     */     {
/* 790 */       return;
/*     */     }
/*     */ 
/* 793 */     Report.trace("dynamicconverter", "deleteConversion: path = " + dirToDelete, null);
/*     */ 
/* 795 */     FileUtils.deleteDirectory(new File(dirToDelete), true);
/*     */   }
/*     */ 
/*     */   public static boolean isPageImageExportInProgress(String outputDirectory)
/*     */   {
/* 801 */     String imageCacheInprocMarker = outputDirectory + "/" + "inprocess.dat";
/* 802 */     File marker = new File(imageCacheInprocMarker);
/* 803 */     boolean isInProgress = marker.exists();
/* 804 */     if (isInProgress)
/*     */     {
/* 806 */       Report.trace("dynamicconverter", "conversion in process", null);
/*     */ 
/* 813 */       long lastmod = marker.lastModified();
/* 814 */       long curTime = System.currentTimeMillis();
/* 815 */       long markerTimeout = SharedObjects.getTypedEnvironmentInt("DCPageImageMarkerTimeOut", 300000, 18, 24);
/*     */ 
/* 818 */       long timeout = lastmod + markerTimeout;
/* 819 */       if (Report.m_verbose)
/*     */       {
/* 821 */         Report.trace("dynamicconverter", "marker timeout at: " + new Date(timeout), null);
/*     */       }
/* 823 */       if (timeout < curTime)
/*     */       {
/* 825 */         marker.delete();
/* 826 */         Report.trace("dynamicconverter", "blow through marker file: " + marker.getPath() + "; been around too long", null);
/*     */ 
/* 828 */         isInProgress = false;
/*     */       }
/*     */     }
/* 831 */     return isInProgress;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 836 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101860 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.converter.ConverterUtils
 * JD-Core Version:    0.5.4
 */