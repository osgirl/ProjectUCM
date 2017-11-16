/*     */ package intradoc.server.converter;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.EnvUtils;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.resource.DataTransformationUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.taskmanager.TaskInfo;
/*     */ import intradoc.taskmanager.TaskInfo.STATUS;
/*     */ import intradoc.taskmanager.TaskMonitor;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ConverterHandler extends ServiceHandler
/*     */ {
/*     */   protected long m_maxFileSize;
/*     */ 
/*     */   public ConverterHandler()
/*     */   {
/*  39 */     this.m_maxFileSize = 20000000L;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getTemplateConversions() throws DataException, ServiceException {
/*  44 */     disableSendFile();
/*  45 */     TemplateConversions.refresh();
/*  46 */     TemplateConversions.load(this.m_service, this.m_binder, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void saveTemplateConversions() throws DataException, ServiceException
/*     */   {
/*  52 */     disableSendFile();
/*  53 */     TemplateConversions.save(this.m_binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void disableSendFile()
/*     */   {
/*  61 */     this.m_service.setDisableSendFile(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setInputConversionInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/*  68 */     Service.checkFeatureAllowed("DynamicConverter");
/*     */ 
/*  71 */     boolean isExternal = StringUtils.convertToBool(this.m_binder.getLocal("isExternal"), false);
/*  72 */     if (isExternal)
/*     */     {
/*  74 */       ResultSet rset = this.m_binder.getResultSet("SearchCollectionDocInfo");
/*  75 */       if (rset != null)
/*     */       {
/*  77 */         this.m_binder.removeResultSet("SearchCollectionDocInfo");
/*  78 */         this.m_binder.addResultSet("DOC_INFO", rset);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  83 */     String format = this.m_binder.getFromSets("dFormat");
/*  84 */     this.m_binder.putLocal("FileFormat", format);
/*     */ 
/*  86 */     FileStoreProvider fs = this.m_service.m_fileStore;
/*  87 */     String inputFilePath = null;
/*  88 */     if (isExternal)
/*     */     {
/*  90 */       inputFilePath = this.m_binder.get("primaryFile");
/*     */     }
/*     */     else
/*     */     {
/*  94 */       String docID = this.m_binder.get("dID");
/*     */ 
/*  97 */       boolean isNativeFormat = true;
/*  98 */       String viewFormat = this.m_binder.getLocal("DCViewFormat");
/*  99 */       if (viewFormat == null)
/*     */       {
/* 101 */         viewFormat = SharedObjects.getEnvironmentValue("DCViewFormat");
/*     */       }
/* 103 */       if ((viewFormat != null) && (viewFormat.equalsIgnoreCase("WebViewable")))
/*     */       {
/* 105 */         isNativeFormat = false;
/*     */       }
/*     */ 
/* 108 */       if (isNativeFormat)
/*     */       {
/* 111 */         DataBinder binder = new DataBinder();
/* 112 */         binder.putLocal("dID", docID);
/*     */ 
/* 114 */         ResultSet rset = this.m_workspace.createResultSet("Qdocuments", binder);
/* 115 */         FieldInfo[] fi = ResultSetUtils.createInfoList(rset, new String[] { "dIsPrimary", "dIsWebFormat", "dExtension", "dFormat" }, true);
/*     */ 
/* 118 */         boolean isPrimary = false;
/* 119 */         boolean isWebFile = false;
/* 120 */         boolean alternateFound = false;
/* 121 */         for (rset.first(); rset.isRowPresent(); rset.next())
/*     */         {
/* 123 */           isPrimary = StringUtils.convertToBool(rset.getStringValue(fi[0].m_index), false);
/* 124 */           isWebFile = StringUtils.convertToBool(rset.getStringValue(fi[1].m_index), false);
/* 125 */           if ((isPrimary) || (isWebFile))
/*     */             continue;
/* 127 */           alternateFound = true;
/* 128 */           break;
/*     */         }
/*     */ 
/* 132 */         String extension = this.m_binder.get("dExtension");
/* 133 */         if (alternateFound)
/*     */         {
/* 136 */           format = rset.getStringValue(fi[3].m_index);
/* 137 */           if (ConverterUtils.isConversionFormat(format))
/*     */           {
/* 139 */             extension = rset.getStringValue(fi[2].m_index);
/* 140 */             this.m_binder.putLocal("FileFormat", format);
/*     */           }
/*     */         }
/*     */ 
/* 144 */         String rendition = (alternateFound) ? "alternateFile" : "primaryFile";
/*     */ 
/* 146 */         this.m_binder.putLocal("RenditionId", rendition);
/* 147 */         IdcFileDescriptor d = fs.createDescriptor(this.m_binder, null, this.m_service);
/* 148 */         inputFilePath = fs.getFilesystemPath(d, this.m_service);
/*     */ 
/* 151 */         this.m_binder.putLocal("TemplateType", extension);
/*     */       }
/*     */       else
/*     */       {
/* 155 */         this.m_binder.putLocal("RenditionId", "webViewableFile");
/* 156 */         IdcFileDescriptor d = fs.createDescriptor(this.m_binder, null, this.m_service);
/* 157 */         inputFilePath = fs.getFilesystemPath(d, this.m_service);
/*     */       }
/*     */     }
/*     */ 
/* 161 */     File inputFile = new File(inputFilePath);
/* 162 */     if (!inputFile.exists())
/*     */     {
/* 164 */       String msg = LocaleUtils.encodeMessage("csDynConvInputFileNotFound", null);
/* 165 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 169 */     long maxFileSize = -1L;
/* 170 */     String maxSizeStr = SharedObjects.getEnvironmentValue("DCMaxFileSize");
/* 171 */     if (maxSizeStr == null)
/*     */     {
/* 173 */       maxFileSize = this.m_maxFileSize;
/*     */     }
/*     */     else
/*     */     {
/* 177 */       maxFileSize = Long.parseLong(maxSizeStr);
/*     */     }
/* 179 */     long inputFileSize = inputFile.length();
/* 180 */     if (inputFileSize > maxFileSize)
/*     */     {
/* 182 */       String msg = LocaleUtils.encodeMessage("csDynConvFileTooLarge", null);
/* 183 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 186 */     this.m_binder.putLocal("inputFilePath", inputFilePath);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setTemplateConversionInfo() throws DataException, ServiceException
/*     */   {
/* 192 */     Properties oldLocalData = (Properties)this.m_binder.getLocalData().clone();
/*     */ 
/* 195 */     TemplateConversions.load(this.m_service, this.m_binder, false);
/*     */ 
/* 198 */     String conversionResource = "conversion_template_computation";
/* 199 */     PageMerger pageMerger = this.m_service.getPageMerger();
/*     */     try
/*     */     {
/* 202 */       pageMerger.evaluateResourceInclude(conversionResource);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 206 */       String msg = LocaleUtils.encodeMessage("csDynHTMLSystemExecutionError", null);
/* 207 */       throw new ServiceException(msg);
/*     */     }
/* 209 */     String templateName = this.m_binder.getLocal("dcTemplate");
/*     */ 
/* 212 */     if ((templateName == null) || (templateName.equals("")))
/*     */     {
/* 214 */       templateName = this.m_binder.getEnvironmentValue("DefaultHtmlConversion");
/* 215 */       if (templateName == null)
/*     */       {
/* 217 */         String msg = LocaleUtils.encodeMessage("csDynConvNoDefaultTemplate", null);
/* 218 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 225 */       pageMerger = new PageMerger(this.m_binder, null);
/* 226 */       templateName = pageMerger.evaluateScriptReportError(templateName);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 230 */       String msg = LocaleUtils.encodeMessage("csUnableToEvalScriptForField", null, "conversion template");
/*     */ 
/* 232 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 236 */     this.m_binder.setLocalData(oldLocalData);
/*     */ 
/* 239 */     DataBinder binder = new DataBinder();
/* 240 */     binder.putLocal("dDocName", templateName);
/* 241 */     ResultSet rset = this.m_workspace.createResultSet("QlatestReleasedIDByName", binder);
/* 242 */     if ((rset == null) || (!rset.isRowPresent()))
/*     */     {
/* 247 */       String msg = LocaleUtils.encodeMessage("csDynConvTemplateNotExists", null, templateName);
/*     */ 
/* 249 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 252 */     String docID = ResultSetUtils.getValue(rset, "dID");
/* 253 */     if (docID.length() == 0)
/*     */     {
/* 256 */       String msg = LocaleUtils.encodeMessage("csDynConvTemplateNotExists", null, templateName);
/*     */ 
/* 258 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 262 */     binder.putLocal("dID", docID);
/* 263 */     rset = this.m_workspace.createResultSet("QdocInfo", binder);
/* 264 */     DataResultSet drset = new DataResultSet();
/* 265 */     drset.copy(rset);
/* 266 */     binder.addResultSet("DOC_INFO", drset);
/* 267 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, new String[] { "dSecurityGroup", "dStatus", "dExtension" }, true);
/*     */ 
/* 271 */     String status = drset.getStringValue(fi[1].m_index);
/* 272 */     if (status.equalsIgnoreCase("DELETED"))
/*     */     {
/* 274 */       String msg = LocaleUtils.encodeMessage("csDynConvTemplateDeleted", null, templateName);
/*     */ 
/* 276 */       throw new ServiceException(msg);
/*     */     }
/* 278 */     if (status.equalsIgnoreCase("EXPIRED"))
/*     */     {
/* 280 */       String msg = LocaleUtils.encodeMessage("csDynConvTemplateExpired", null, templateName);
/*     */ 
/* 282 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 288 */     String docName = binder.get("dDocName");
/* 289 */     docName = docName.toLowerCase();
/* 290 */     String extension = drset.getStringValue(fi[2].m_index);
/* 291 */     extension = extension.toLowerCase();
/* 292 */     String templateFilePath = null;
/*     */ 
/* 294 */     FileStoreProvider fs = this.m_service.m_fileStore;
/* 295 */     binder.putLocal("RenditionId", "webViewableFile");
/* 296 */     IdcFileDescriptor d = fs.createDescriptor(binder, null, this.m_service);
/* 297 */     templateFilePath = fs.getFilesystemPath(d, this.m_service);
/*     */ 
/* 299 */     File templateFile = new File(templateFilePath);
/* 300 */     if (!templateFile.exists())
/*     */     {
/* 302 */       String msg = LocaleUtils.encodeMessage("csDynConvTemplateNotExists", null, templateName);
/*     */ 
/* 304 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 307 */     this.m_binder.putLocal("templateFilePath", templateFilePath);
/* 308 */     this.m_binder.putLocal("templateName", templateName);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void setOutputConversionInfo() throws DataException, ServiceException
/*     */   {
/* 314 */     String inputFilePath = this.m_binder.get("inputFilePath");
/* 315 */     String templateName = this.m_binder.get("templateName");
/* 316 */     String outputFilePath = null;
/* 317 */     String outputDirectory = null;
/*     */ 
/* 319 */     boolean isExternal = StringUtils.convertToBool(this.m_binder.getLocal("isExternal"), false);
/* 320 */     if (isExternal)
/*     */     {
/* 322 */       String fileName = FileUtils.getName(inputFilePath);
/* 323 */       int index = fileName.indexOf(".");
/* 324 */       if (index > 0)
/*     */       {
/* 326 */         fileName = fileName.substring(0, index);
/*     */       }
/*     */ 
/* 329 */       String externalPath = FileUtils.getDirectory(inputFilePath);
/* 330 */       externalPath = FileUtils.directorySlashes(externalPath);
/* 331 */       String exportPath = "~export/" + fileName + "~" + templateName + "/";
/*     */ 
/* 334 */       outputDirectory = externalPath + exportPath;
/* 335 */       this.m_binder.putLocal("outputDirectory", outputDirectory);
/*     */ 
/* 337 */       outputFilePath = outputDirectory + fileName + ".hcst";
/* 338 */       this.m_binder.putLocal("outputFilePath", outputFilePath);
/*     */ 
/* 341 */       String collectionID = this.m_binder.get("sCollectionID");
/* 342 */       String[] externalInfo = ConverterUtils.getExternalCollectionInfo(collectionID);
/*     */ 
/* 344 */       String collectionPhysicalPath = externalInfo[0];
/* 345 */       String collectionRelativePath = externalInfo[1];
/*     */ 
/* 347 */       if ((collectionPhysicalPath == null) || (collectionPhysicalPath.equals("")) || (collectionRelativePath == null) || (collectionRelativePath.equals("")))
/*     */       {
/* 350 */         String msg = LocaleUtils.encodeMessage("csExternalCollectionInvalid", null, collectionID);
/* 351 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 354 */       if (!outputDirectory.toUpperCase().startsWith(collectionPhysicalPath.toUpperCase()))
/*     */       {
/* 356 */         String msg = LocaleUtils.encodeMessage("csExternalCollectionPathsNotMatch", null);
/* 357 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/* 360 */       int length = collectionPhysicalPath.length();
/* 361 */       String fileEnd = outputDirectory.substring(length);
/*     */ 
/* 363 */       String outputRelativeWebDir = collectionRelativePath + fileEnd;
/* 364 */       this.m_binder.putLocal("outputRelativeWebDir", outputRelativeWebDir);
/* 365 */       this.m_binder.putLocal("outputRelativePath", outputRelativeWebDir + fileName + ".hcst");
/* 366 */       this.m_binder.putLocal("outputFileName", fileName);
/*     */     }
/*     */     else
/*     */     {
/* 370 */       String docName = this.m_binder.getFromSets("dDocName");
/* 371 */       String dID = this.m_binder.getFromSets("dID");
/* 372 */       String fileName = dID + ".hcst";
/* 373 */       String exportPath = "~export/" + docName + "~" + templateName + "/";
/*     */ 
/* 375 */       FileStoreProvider fs = this.m_service.m_fileStore;
/*     */ 
/* 377 */       this.m_binder.putLocal("RenditionId", "webViewableFile");
/* 378 */       IdcFileDescriptor d = fs.createDescriptor(this.m_binder, null, this.m_service);
/* 379 */       String path = d.getProperty("path");
/* 380 */       String dirPath = FileUtils.getDirectory(path);
/* 381 */       outputDirectory = dirPath + exportPath;
/*     */ 
/* 385 */       HashMap args = new HashMap();
/* 386 */       args.put("useAbsolute", "0");
/* 387 */       args.put("isNew", "1");
/* 388 */       String url = fs.getClientURL(d, null, args, this.m_service);
/* 389 */       String dirURL = FileUtils.getDirectory(url);
/* 390 */       this.m_binder.putLocal("outputRelativeWebDir", dirURL + exportPath);
/*     */ 
/* 394 */       String webLayoutDir = SharedObjects.getEnvironmentValue("WeblayoutDir");
/* 395 */       int index = path.indexOf(webLayoutDir);
/* 396 */       if (index >= 0)
/*     */       {
/* 398 */         path = path.substring(webLayoutDir.length());
/*     */       }
/* 400 */       this.m_binder.putLocal("outputRelativePath", path + '/' + exportPath + fileName);
/*     */ 
/* 402 */       this.m_binder.putLocal("outputDirectory", outputDirectory);
/* 403 */       this.m_binder.putLocal("outputFilePath", outputDirectory + fileName);
/* 404 */       this.m_binder.putLocal("outputFileName", dID);
/*     */     }
/*     */ 
/* 408 */     long oldOutputDirSize = ConverterUtils.getOutputDirSize(outputDirectory);
/* 409 */     this.m_binder.putLocal("oldOutputDirSize", String.valueOf(oldOutputDirSize));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void checkConversionCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 416 */     this.m_binder.putLocal("doConversion", "1");
/* 417 */     boolean ignoreCache = SharedObjects.getEnvValueAsBoolean("IgnoreDCCache", false);
/* 418 */     if (ignoreCache)
/*     */     {
/* 420 */       return;
/*     */     }
/*     */ 
/* 423 */     DataBinder binder = new DataBinder();
/* 424 */     String conversionKey = getConversionKey();
/* 425 */     binder.putLocal("dConversionKey", conversionKey);
/* 426 */     ResultSet convResultSet = this.m_workspace.createResultSet("QhtmlConversions", binder);
/*     */ 
/* 429 */     if ((convResultSet == null) || (!convResultSet.isRowPresent()))
/*     */     {
/* 431 */       return;
/*     */     }
/*     */ 
/* 435 */     FieldInfo[] info = ResultSetUtils.createInfoList(convResultSet, new String[] { "dOutputFile", "dConversionDate", "dVaultTimeStamp", "dDependencyKey" }, true);
/*     */ 
/* 441 */     boolean isExternal = StringUtils.convertToBool(this.m_binder.getLocal("isExternal"), false);
/* 442 */     if (!isExternal)
/*     */     {
/* 447 */       String dbOutputFilePath = FileUtils.getAbsolutePath(SharedObjects.getEnvironmentValue("WeblayoutDir"), convResultSet.getStringValue(info[0].m_index));
/*     */ 
/* 450 */       if (dbOutputFilePath == null)
/*     */       {
/* 452 */         return;
/*     */       }
/*     */ 
/* 456 */       String actualDir = this.m_binder.getLocal("outputDirectory");
/* 457 */       String oldOutputDir = FileUtils.getDirectory(dbOutputFilePath);
/* 458 */       oldOutputDir = FileUtils.directorySlashes(oldOutputDir);
/* 459 */       if (!actualDir.equalsIgnoreCase(oldOutputDir))
/*     */       {
/* 461 */         File oldOutputFile = new File(oldOutputDir);
/* 462 */         if (oldOutputFile.exists())
/*     */         {
/* 464 */           FileUtils.deleteDirectory(oldOutputFile, true);
/*     */         }
/* 466 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 471 */     String outputDirectory = this.m_binder.getLocal("outputDirectory");
/* 472 */     File outputDirFile = new File(outputDirectory);
/* 473 */     if (!outputDirFile.exists())
/*     */     {
/* 475 */       return;
/*     */     }
/* 477 */     String outputFilePath = this.m_binder.getLocal("outputFilePath");
/* 478 */     File outputFile = new File(outputFilePath);
/* 479 */     if (!outputFile.exists())
/*     */     {
/* 481 */       return;
/*     */     }
/*     */ 
/* 485 */     String inputFilePath = this.m_binder.getLocal("inputFilePath");
/* 486 */     File inputFile = new File(inputFilePath);
/* 487 */     long inputTS = inputFile.lastModified();
/* 488 */     String dbInputFileTS = convResultSet.getStringValue(info[2].m_index);
/* 489 */     if (!dbInputFileTS.equals(String.valueOf(inputTS)))
/*     */     {
/* 491 */       return;
/*     */     }
/*     */ 
/* 494 */     String templateFilePath = this.m_binder.getLocal("templateFilePath");
/* 495 */     File templateFile = new File(templateFilePath);
/* 496 */     long templateTS = templateFile.lastModified();
/* 497 */     String dbTemplateFileTS = convResultSet.getStringValue(info[3].m_index);
/* 498 */     if (!dbTemplateFileTS.equals(String.valueOf(templateTS)))
/*     */     {
/* 500 */       return;
/*     */     }
/*     */ 
/* 504 */     long currentTime = System.currentTimeMillis();
/* 505 */     long expiredInterval = 42900000L;
/* 506 */     Date expiredDate = new Date(currentTime - expiredInterval);
/* 507 */     Date conversionDate = convResultSet.getDateValue(info[1].m_index);
/*     */ 
/* 509 */     if (conversionDate.before(expiredDate))
/*     */     {
/* 511 */       return;
/*     */     }
/*     */ 
/* 515 */     this.m_binder.putLocal("doConversion", "0");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void runHtmlConversion() throws DataException, ServiceException
/*     */   {
/* 521 */     boolean isHtmlForm = false;
/* 522 */     String extension = this.m_binder.get("dExtension");
/* 523 */     if ((extension.equalsIgnoreCase("hcsp")) || (extension.equalsIgnoreCase("hcsf")))
/*     */     {
/* 525 */       isHtmlForm = true;
/*     */     }
/*     */ 
/* 528 */     boolean doConversion = StringUtils.convertToBool(this.m_binder.getLocal("doConversion"), true);
/*     */ 
/* 530 */     if (doConversion)
/*     */     {
/* 532 */       String inputFilePath = this.m_binder.getLocal("inputFilePath");
/* 533 */       String templateFilePath = this.m_binder.getLocal("templateFilePath");
/* 534 */       String outputFilePath = this.m_binder.getLocal("outputFilePath");
/*     */ 
/* 537 */       String outputDirectory = this.m_binder.getLocal("outputDirectory");
/* 538 */       File outputFile = new File(outputDirectory);
/* 539 */       FileUtils.deleteDirectory(outputFile, false);
/*     */ 
/* 542 */       FileUtils.checkOrCreateDirectory(outputDirectory, 2);
/*     */ 
/* 545 */       if (extension.equalsIgnoreCase("xml"))
/*     */       {
/* 547 */         ConverterXml.convertToHtml(inputFilePath, templateFilePath, outputFilePath);
/*     */       }
/* 550 */       else if (isHtmlForm)
/*     */       {
/* 552 */         outputFile = new File(outputFilePath);
/* 553 */         FileUtils.copyFile(templateFilePath, outputFilePath);
/*     */       }
/*     */       else
/*     */       {
/* 558 */         String libPath = SharedObjects.getEnvironmentValue("ExportLibraryBaseDirectory");
/* 559 */         if (libPath == null)
/*     */         {
/* 561 */           libPath = "lib/contentaccess/";
/*     */         }
/*     */         else
/*     */         {
/* 565 */           libPath = FileUtils.directorySlashes(libPath);
/*     */         }
/*     */ 
/* 568 */         String exeName = "htmlexport";
/*     */ 
/* 571 */         Map pathSettings = LegacyDirectoryLocator.getOitMap(new HashMap(), libPath + "/" + exeName, "type_executable");
/* 572 */         String htmlExportProgramPath = (String)pathSettings.get("path");
/* 573 */         if (htmlExportProgramPath == null)
/*     */         {
/* 575 */           String msg = LocaleUtils.encodeMessage("csHtmlExportExecutableMissing", null, htmlExportProgramPath);
/*     */ 
/* 577 */           throw new ServiceException(msg);
/*     */         }
/* 579 */         String htmlExportProgramDir = FileUtils.getDirectory(htmlExportProgramPath);
/*     */ 
/* 581 */         String[] environment = null;
/* 582 */         String libraryPathEnvName = EnvUtils.getLibraryPathEnvironmentVariableName();
/* 583 */         String pathSep = EnvUtils.getPathSeparator();
/* 584 */         String libraryPath = null;
/* 585 */         String path = null;
/*     */ 
/* 588 */         String os = EnvUtils.getOSName();
/* 589 */         DataResultSet config = SharedObjects.getTable("ConverterOSSettings");
/* 590 */         FieldInfo[] infos = ResultSetUtils.createInfoList(config, new String[] { "dcOSName", "dcUseX", "dcExtraLibDirs", "dcExtraBinDirs" }, true);
/*     */ 
/* 592 */         Vector v = config.findRow(infos[0].m_index, os);
/* 593 */         if (v != null)
/*     */         {
/* 595 */           boolean useX = StringUtils.convertToBool((String)v.elementAt(infos[1].m_index), false);
/* 596 */           if (useX)
/*     */           {
/* 598 */             libraryPath = libraryPathEnvName + "=" + EnvUtils.getLibraryPath() + pathSep + htmlExportProgramDir;
/*     */ 
/* 600 */             String extraLibPaths = (String)v.elementAt(infos[2].m_index);
/* 601 */             String extraBinPaths = (String)v.elementAt(infos[3].m_index);
/* 602 */             if (extraLibPaths.length() > 0)
/*     */             {
/* 604 */               extraLibPaths = extraLibPaths.replace(',', pathSep.charAt(0));
/* 605 */               libraryPath = libraryPath + pathSep + extraLibPaths;
/*     */             }
/* 607 */             path = "PATH=" + htmlExportProgramDir;
/* 608 */             if (extraBinPaths.length() > 0)
/*     */             {
/* 610 */               extraBinPaths = extraBinPaths.replace(',', pathSep.charAt(0));
/* 611 */               path = path + pathSep + extraBinPaths;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 616 */           String envDisplay = null;
/* 617 */           if (useX)
/*     */           {
/* 619 */             envDisplay = getEnvDisplay(htmlExportProgramDir);
/* 620 */             if (envDisplay == null)
/*     */             {
/* 622 */               envDisplay = "";
/*     */             }
/*     */ 
/* 625 */             environment = new String[] { libraryPath, path, envDisplay };
/*     */           }
/*     */         }
/* 628 */         if (environment == null)
/*     */         {
/* 632 */           String settings = (String)pathSettings.get("environment_settings");
/* 633 */           environment = new String[] { settings };
/* 634 */           Report.trace("htmlconversion", "runHtmlConversion - environment still null, so doing new way (set verbose to see environment)", null);
/*     */ 
/* 636 */           if (SystemUtils.m_verbose)
/*     */           {
/* 638 */             if (settings == null)
/*     */             {
/* 640 */               Report.trace("htmlconversion", "runHtmlConversion: null environment", null);
/*     */             }
/*     */             else
/*     */             {
/* 645 */               for (int i = 0; i < environment.length; ++i)
/*     */               {
/* 647 */                 Report.trace("htmlconversion", "runHtmlConversion - environment: " + environment[i], null);
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 655 */         File exeFile = new File(htmlExportProgramPath);
/* 656 */         if (!exeFile.exists())
/*     */         {
/* 658 */           String msg = LocaleUtils.encodeMessage("csHtmlExportFiltersNotLoaded", null);
/* 659 */           throw new ServiceException(msg);
/*     */         }
/*     */ 
/* 663 */         boolean useStyleSheets = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseCascadingStyleSheets"), true);
/*     */ 
/* 665 */         String useStyleSheetsStr = null;
/* 666 */         if (useStyleSheets)
/*     */         {
/* 668 */           useStyleSheetsStr = "1";
/*     */         }
/*     */         else
/*     */         {
/* 672 */           useStyleSheetsStr = "0";
/*     */         }
/*     */ 
/* 676 */         String manifestDir = DirectoryLocator.getTempDirectory() + "htmlexport/";
/* 677 */         String manifestFileName = DataBinder.getNextFileCounter() + ".hda";
/* 678 */         FileUtils.checkOrCreateDirectory(manifestDir, 1);
/*     */ 
/* 680 */         DataBinder binder = new DataBinder();
/* 681 */         binder.putLocal("inputFilePath", inputFilePath);
/* 682 */         binder.putLocal("templateFilePath", templateFilePath);
/* 683 */         binder.putLocal("outputPhysicalDir", outputDirectory);
/* 684 */         binder.putLocal("outputRelativeWebDir", this.m_binder.getLocal("outputRelativeWebDir"));
/* 685 */         binder.putLocal("dID", this.m_binder.getLocal("outputFileName"));
/* 686 */         binder.putLocal("useCascadingStyleSheets", useStyleSheetsStr);
/*     */ 
/* 689 */         String fileEncoding = SharedObjects.getEnvironmentValue("FileEncoding");
/* 690 */         if (fileEncoding == null)
/*     */         {
/* 692 */           fileEncoding = DataSerializeUtils.getSystemEncoding();
/*     */         }
/* 694 */         if (fileEncoding == null)
/*     */         {
/* 696 */           fileEncoding = "Cp1252";
/*     */         }
/* 698 */         String fallbackFormat = SharedObjects.getEnvironmentValue("DefaultFilterInputFormat");
/* 699 */         if (fallbackFormat == null)
/*     */         {
/* 701 */           fallbackFormat = fileEncoding;
/*     */         }
/* 703 */         String outputCharSet = SharedObjects.getEnvironmentValue("DefaultFilterOutputFormat");
/* 704 */         if (outputCharSet == null)
/*     */         {
/* 706 */           outputCharSet = fileEncoding;
/*     */         }
/*     */ 
/* 709 */         binder.putLocal("fallbackFormat", fallbackFormat);
/* 710 */         binder.putLocal("outputCharacterSet", outputCharSet);
/*     */ 
/* 713 */         String htmlFlavor = SharedObjects.getEnvironmentValue("DynamicConverterHtmlFlavor");
/* 714 */         if (htmlFlavor != null)
/*     */         {
/* 716 */           binder.putLocal("htmlFlavor", htmlFlavor);
/*     */         }
/*     */ 
/* 719 */         ResourceUtils.serializeDataBinder(manifestDir, manifestFileName, binder, true, false);
/*     */ 
/* 722 */         if (!SharedObjects.getEnvValueAsBoolean("DisableHtmlConversionCleanup", false))
/*     */         {
/* 725 */           this.m_binder.addTempFile(manifestDir + manifestFileName);
/*     */         }
/*     */ 
/* 729 */         Vector cmdLine = new IdcVector();
/* 730 */         cmdLine.addElement(htmlExportProgramPath);
/* 731 */         cmdLine.addElement("-c");
/* 732 */         cmdLine.addElement(manifestDir + manifestFileName);
/* 733 */         cmdLine.addElement("-f");
/* 734 */         cmdLine.addElement(outputFilePath);
/*     */ 
/* 737 */         TaskInfo info = new TaskInfo("HtmlExport", cmdLine, outputFilePath);
/* 738 */         info.m_timeout = SharedObjects.getTypedEnvironmentInt("DCTimeOut", 180000, 18, 19);
/*     */ 
/* 740 */         info.m_environment = environment;
/*     */ 
/* 742 */         TaskMonitor.addToQueue(info);
/*     */ 
/* 744 */         synchronized (info)
/*     */         {
/* 746 */           if (!info.m_isFinished)
/*     */           {
/*     */             try
/*     */             {
/* 750 */               info.wait();
/*     */             }
/*     */             catch (Throwable ignore)
/*     */             {
/* 754 */               Report.trace(null, null, ignore);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 759 */         if ((info.m_status == TaskInfo.STATUS.FAILURE) || (info.m_status == TaskInfo.STATUS.TIMEOUT))
/*     */         {
/* 761 */           throw new ServiceException(info.m_errMsg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 766 */       outputFile = new File(outputFilePath);
/* 767 */       if (!outputFile.exists())
/*     */       {
/* 769 */         String msg = LocaleUtils.encodeMessage("csHtmlExportNotSuccessful", null);
/* 770 */         throw new ServiceException(msg);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 776 */     if (!isHtmlForm)
/*     */       return;
/* 778 */     mergeHtmlFormsData();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void mergeHtmlFormsData()
/*     */     throws DataException, ServiceException
/*     */   {
/* 785 */     DynamicHtml dynHtml = null;
/* 786 */     long curTime = System.currentTimeMillis();
/* 787 */     String inputFilePath = null;
/*     */     try
/*     */     {
/* 790 */       inputFilePath = this.m_binder.getLocal("inputFilePath");
/* 791 */       dynHtml = DataLoader.loadDynamicPage(inputFilePath, inputFilePath, curTime, true, this.m_service);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 796 */       this.m_service.createServiceException(e, null);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 802 */       DataTransformationUtils.mergeInDynamicData(this.m_workspace, this.m_binder, dynHtml);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 806 */       String msg = LocaleUtils.encodeMessage("csFileServiceDynamicAppFileError", null, inputFilePath);
/*     */ 
/* 808 */       this.m_binder.putLocal("StatusCode", "-1");
/* 809 */       this.m_binder.putLocal("StatusMessageKey", msg);
/* 810 */       this.m_binder.putLocal("StatusMessage", msg);
/* 811 */       Report.trace(null, LocaleResources.localizeMessage(msg, new ExecutionContextAdaptor()), null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getEnvDisplay(String exePath)
/*     */   {
/* 818 */     String displayValue = null;
/*     */     try
/*     */     {
/* 821 */       NativeOsUtils utils = new NativeOsUtils();
/* 822 */       displayValue = utils.getEnv("DISPLAY");
/* 823 */       if (displayValue == null)
/*     */       {
/* 825 */         return null;
/*     */       }
/* 827 */       return "DISPLAY=" + displayValue.trim();
/*     */     }
/*     */     catch (Throwable e)
/*     */     {
/* 831 */       Report.trace(null, null, t);
/*     */       try
/*     */       {
/* 836 */         String[] cmd = { "/bin/sh", "-c", "echo $DISPLAY" };
/* 837 */         Process proc = Runtime.getRuntime().exec(cmd);
/* 838 */         BufferedReader inp = new BufferedReader(new InputStreamReader(proc.getInputStream()));
/*     */ 
/* 840 */         String envDisplay = "DISPLAY=" + inp.readLine();
/* 841 */         return envDisplay.trim();
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 845 */         Report.trace(null, null, e);
/*     */       }
/* 846 */     }return null;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void updateConversionCache()
/*     */     throws DataException, ServiceException
/*     */   {
/* 854 */     Date dte = new Date();
/* 855 */     String currentDateStr = LocaleUtils.formatODBC(dte);
/*     */ 
/* 858 */     Properties sumProps = ConverterUtils.getConversionSumValues(this.m_workspace);
/* 859 */     long totalCached = Long.parseLong(sumProps.getProperty("dTotalCached"));
/* 860 */     long totalFiles = Long.parseLong(sumProps.getProperty("dTotalFiles"));
/*     */ 
/* 863 */     boolean conversionExists = false;
/* 864 */     DataBinder binder = new DataBinder();
/* 865 */     String conversionKey = getConversionKey();
/* 866 */     binder.putLocal("dConversionKey", conversionKey);
/* 867 */     ResultSet rset = this.m_workspace.createResultSet("QhtmlConversions", binder);
/* 868 */     if ((rset != null) && (rset.isRowPresent()))
/*     */     {
/* 870 */       conversionExists = true;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 875 */       this.m_workspace.beginTran();
/*     */ 
/* 877 */       boolean isNewExport = StringUtils.convertToBool(this.m_binder.getLocal("doConversion"), true);
/*     */ 
/* 879 */       if (isNewExport)
/*     */       {
/* 882 */         String inputFilePath = this.m_binder.getLocal("inputFilePath");
/* 883 */         File inputFile = new File(inputFilePath);
/* 884 */         long inputTS = inputFile.lastModified();
/*     */ 
/* 886 */         String templateFilePath = this.m_binder.getLocal("templateFilePath");
/* 887 */         File templateFile = new File(templateFilePath);
/* 888 */         long templateTS = templateFile.lastModified();
/*     */ 
/* 890 */         DataBinder conversionBinder = new DataBinder();
/* 891 */         conversionBinder.putLocal("dConversionKey", conversionKey);
/* 892 */         conversionBinder.putLocal("dOutputFile", this.m_binder.getLocal("outputRelativePath"));
/* 893 */         conversionBinder.putLocal("dConversionDate", currentDateStr);
/* 894 */         conversionBinder.putLocal("dVaultTimeStamp", String.valueOf(inputTS));
/* 895 */         conversionBinder.putLocal("dDependencyKey", String.valueOf(templateTS));
/*     */ 
/* 897 */         conversionBinder.putLocal("dDependencyKeyLayout", "");
/* 898 */         conversionBinder.putLocal("dDependencyKeySCD", "");
/*     */ 
/* 900 */         if (conversionExists)
/*     */         {
/* 902 */           this.m_workspace.execute("UhtmlConversion", conversionBinder);
/*     */         }
/*     */         else
/*     */         {
/* 906 */           this.m_workspace.execute("IhtmlConversion", conversionBinder);
/*     */         }
/*     */ 
/* 910 */         String outputDirectory = this.m_binder.getLocal("outputDirectory");
/* 911 */         String oldOutputDirSize = this.m_binder.getLocal("oldOutputDirSize");
/* 912 */         long currentOutputDirSize = ConverterUtils.getOutputDirSize(outputDirectory);
/* 913 */         if (conversionExists)
/*     */         {
/* 915 */           totalCached += currentOutputDirSize - Long.parseLong(oldOutputDirSize);
/*     */         }
/*     */         else
/*     */         {
/* 919 */           totalCached += currentOutputDirSize;
/* 920 */           totalFiles += 1L;
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 927 */         if (!conversionExists)
/*     */         {
/* 929 */           this.m_workspace.rollbackTran();
/* 930 */           return;
/*     */         }
/*     */ 
/* 934 */         DataBinder updateBinder = new DataBinder();
/* 935 */         updateBinder.putLocal("dConversionKey", conversionKey);
/* 936 */         updateBinder.putLocal("dConversionDate", currentDateStr);
/*     */ 
/* 938 */         this.m_workspace.execute("UhtmlConversionDate", updateBinder);
/*     */       }
/*     */ 
/* 942 */       sumProps.clear();
/* 943 */       sumProps.put("dTotalCached", String.valueOf(totalCached));
/* 944 */       sumProps.put("dTotalFiles", String.valueOf(totalFiles));
/* 945 */       sumProps.put("dLastAccessed", currentDateStr);
/* 946 */       sumProps.put("dLastKey", conversionKey);
/*     */ 
/* 948 */       ConverterUtils.setConversionSumValues(this.m_workspace, sumProps);
/*     */ 
/* 950 */       this.m_workspace.commitTran();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 954 */       this.m_workspace.rollbackTran();
/*     */ 
/* 957 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getConversionKey() throws DataException
/*     */   {
/* 963 */     String conversionKey = null;
/* 964 */     String templateName = this.m_binder.getLocal("templateName");
/* 965 */     boolean isExternal = StringUtils.convertToBool(this.m_binder.getLocal("isExternal"), false);
/* 966 */     if (isExternal)
/*     */     {
/* 968 */       String collectionID = this.m_binder.get("sCollectionID");
/* 969 */       String externalKey = this.m_binder.get("VdkVgwKey");
/*     */ 
/* 971 */       conversionKey = collectionID + "~" + externalKey + "~" + templateName;
/*     */     }
/*     */     else
/*     */     {
/* 975 */       String docID = this.m_binder.getFromSets("dID");
/* 976 */       conversionKey = "local~" + docID + "~" + templateName;
/*     */     }
/* 978 */     return conversionKey;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 983 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101624 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.converter.ConverterHandler
 * JD-Core Version:    0.5.4
 */