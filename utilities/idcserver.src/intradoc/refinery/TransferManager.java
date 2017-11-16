/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.filestore.BasicIdcFileDescriptor;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.io.zip.IdcZipFile;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ServerRequestUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.zip.IdcZipFunctions;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.File;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class TransferManager
/*     */ {
/*     */   protected ExecutionContext m_refineryQueueContext;
/*     */ 
/*     */   public TransferManager(ExecutionContext refineryQueueContext)
/*     */   {
/*  67 */     this.m_refineryQueueContext = refineryQueueContext;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*     */   }
/*     */ 
/*     */   public Provider findProviderForWork(Properties conversionProps)
/*     */     throws DataException
/*     */   {
/*  79 */     long timeBetweenRefineryTransfers = SharedObjects.getTypedEnvironmentInt("RefinerySecondsBetweenTransferAttempts", 10000, 18, 24);
/*     */ 
/*  86 */     int numTran = 0;
/*  87 */     DataResultSet loadRs = new DataResultSet();
/*  88 */     loadRs.copy(RefineryProviderManager.m_refineryQueueLoadRS);
/*  89 */     while (numTran++ < 3)
/*     */     {
/*  91 */       String dConversion = conversionProps.getProperty("dConversion");
/*  92 */       Provider prov = RefineryProviderManager.getNextProviderByType(dConversion, loadRs);
/*  93 */       if (prov == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  98 */       String curName = prov.getName();
/*     */ 
/* 100 */       boolean providerRejectsJob = true;
/* 101 */       boolean providerMightAcceptJob = false;
/* 102 */       String acceptStatus = "-1";
/* 103 */       String acceptStatusMsg = null;
/*     */ 
/* 105 */       DataBinder response = new DataBinder();
/* 106 */       boolean transferErr = false;
/*     */       try
/*     */       {
/* 109 */         Report.trace("ibrsupport", "Transfer attempt to provider: " + curName, null);
/* 110 */         response = doTransfer(false, prov, "ACCEPT_CONVERSION", conversionProps);
/*     */       }
/*     */       catch (Exception acceptExp)
/*     */       {
/* 114 */         transferErr = true;
/* 115 */         Report.trace("ibrsupport", "Unable to connect to provider '" + curName + "'", acceptExp);
/*     */       }
/*     */ 
/* 118 */       if (!transferErr)
/*     */       {
/* 120 */         acceptStatus = response.getLocal("JobAcceptStatus");
/* 121 */         acceptStatusMsg = response.getLocal("JobAcceptStatusMsg");
/* 122 */         String accept = response.getLocal("JobCanAccept");
/* 123 */         boolean canAcceptedJob = StringUtils.convertToBool(accept, false);
/* 124 */         Report.trace("ibrsupport", "Transfer to provider: " + curName + " job can be accepted: " + canAcceptedJob + "; JobAcceptStatus: " + acceptStatus + "; JobAcceptStatusMsg: " + acceptStatusMsg, null);
/*     */ 
/* 128 */         if (canAcceptedJob == true)
/*     */         {
/* 131 */           return prov;
/*     */         }
/*     */ 
/* 134 */         providerRejectsJob = acceptStatus.equalsIgnoreCase("-1");
/* 135 */         providerMightAcceptJob = (acceptStatus.equalsIgnoreCase("1")) || (acceptStatus.equalsIgnoreCase("3"));
/*     */ 
/* 138 */         if (acceptStatus.equalsIgnoreCase("0"))
/*     */         {
/* 141 */           RefineryProviderManager.changeConversionType(prov, dConversion, false);
/*     */         }
/*     */       }
/* 144 */       RefineryStatus rstat = (RefineryStatus)RefineryProviderManager.m_refineryDataMap.get(curName);
/* 145 */       if (providerRejectsJob == true)
/*     */       {
/* 148 */         rstat.m_isAvailable = false;
/*     */       }
/* 150 */       else if (providerMightAcceptJob == true)
/*     */       {
/* 153 */         long refineryDirectedBusyTime = NumberUtils.parseInteger(response.getLocal("RefineryBusyTimeSeconds"), 30);
/*     */ 
/* 155 */         rstat.m_nextAttempt = (System.currentTimeMillis() + refineryDirectedBusyTime * 1000L);
/*     */       }
/* 157 */       SystemUtils.sleep(timeBetweenRefineryTransfers);
/*     */     }
/* 159 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean submitConversionToRefinery(Provider prov, Properties jobData)
/*     */     throws ServiceException, DataException
/*     */   {
/* 165 */     boolean jobAccepted = false;
/* 166 */     DataBinder response = doTransfer(true, prov, "SUBMIT_CONVERSION_TO_REF_IN_QUEUE", jobData);
/* 167 */     String jobAcceptedStr = response.getLocal("JobAccepted");
/* 168 */     jobAccepted = StringUtils.convertToBool(jobAcceptedStr, false);
/* 169 */     if (jobAccepted)
/*     */     {
/* 171 */       jobData.put("dConvProvider", prov.getName());
/*     */     }
/* 173 */     String statusMessage = response.getLocal("StatusMessage");
/* 174 */     if (statusMessage != null)
/*     */     {
/* 176 */       jobData.setProperty("dConvMessage", statusMessage);
/*     */     }
/* 178 */     return jobAccepted;
/*     */   }
/*     */ 
/*     */   public DataBinder pullConversionOver(Provider prov, String dConvJobID)
/*     */     throws ServiceException, DataException
/*     */   {
/* 185 */     Properties props = new Properties();
/* 186 */     props.put("dConvJobID", dConvJobID);
/* 187 */     DataBinder response = doTransfer(false, prov, "PULL_JOB_OFF_CONVERTED_QUEUE", props);
/*     */ 
/* 189 */     String tempDir = null;
/* 190 */     String pathToDownloadedFile = response.getLocal("downloadFile:path");
/* 191 */     if (pathToDownloadedFile != null)
/*     */     {
/* 194 */       tempDir = DataBinder.getTemporaryDirectory();
/* 195 */       String subDir = "dr" + dConvJobID;
/* 196 */       FileUtils.checkOrCreateSubDirectory(tempDir, subDir);
/* 197 */       tempDir = tempDir + subDir + "/";
/*     */ 
/* 199 */       ZipFunctions.extractZipFiles(pathToDownloadedFile, tempDir);
/* 200 */       DataBinder conversionBinder = ResourceUtils.readDataBinder(tempDir, "conversion.hda");
/* 201 */       response.merge(conversionBinder);
/* 202 */       response.putLocal("TempRecieveDir", tempDir);
/* 203 */       if (RefineryUtils.m_refineryTransferCleanUp == true)
/*     */       {
/* 205 */         FileUtils.deleteFile(pathToDownloadedFile);
/*     */       }
/*     */     }
/*     */ 
/* 209 */     if (SystemUtils.m_verbose)
/*     */     {
/* 213 */       IdcStringBuilder traceMsg = new IdcStringBuilder("Unpackage IBR response attachments from: " + prov.getName());
/* 214 */       if (pathToDownloadedFile != null)
/*     */       {
/* 216 */         traceMsg.append("; IBR jobDir: " + tempDir);
/* 217 */         String[] files = FileUtils.getMatchingFileNames(tempDir, "*");
/* 218 */         for (int i = 0; i < files.length; ++i)
/*     */         {
/* 220 */           String file = files[i];
/* 221 */           traceMsg.append("\nfile " + i + " : " + file);
/* 222 */           String ext = FileUtils.getExtension(file);
/* 223 */           if (!ext.equalsIgnoreCase("hda"))
/*     */             continue;
/* 225 */           DataBinder tmpBinder = ResourceUtils.readDataBinder(tempDir, file);
/* 226 */           traceMsg.append(tmpBinder.toString());
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 232 */         traceMsg.append("; no file attachments");
/*     */       }
/* 234 */       Report.trace("ibrsupport", traceMsg.toString(), null);
/*     */     }
/* 236 */     return response;
/*     */   }
/*     */ 
/*     */   public boolean signalRefineryJobComplete(Provider prov, Properties completedJobProps)
/*     */     throws ServiceException, DataException
/*     */   {
/* 242 */     DataBinder response = doTransfer(false, prov, "SIGN_OFF_COMPLETED_JOB", completedJobProps);
/* 243 */     boolean jobComplete = DataBinderUtils.getBoolean(response, "SuccessSignOff", false);
/* 244 */     if (jobComplete)
/*     */     {
/* 246 */       completedJobProps.put("dConvProvider", prov.getName());
/*     */     }
/* 248 */     return jobComplete;
/*     */   }
/*     */ 
/*     */   public DataBinder retrieveConversionTypeList(Provider prov) throws ServiceException, DataException
/*     */   {
/* 253 */     DataBinder response = doTransfer(prov, "BUILD_CURRENT_JOB_STATUS");
/* 254 */     return response;
/*     */   }
/*     */ 
/*     */   public DataBinder notifyRefineryAbortedJob(Provider prov, Properties props) throws ServiceException, DataException
/*     */   {
/* 259 */     DataBinder ret = doTransfer(false, prov, "ABORT_JOB", props);
/* 260 */     return ret;
/*     */   }
/*     */ 
/*     */   protected boolean checkResponseIsValidIbr(Provider prov, DataBinder binder)
/*     */   {
/* 265 */     boolean isValid = false;
/* 266 */     String pname = prov.getName();
/* 267 */     String statusCodeStr = binder.getLocal("StatusCode");
/* 268 */     String statusMsg = null;
/* 269 */     if (statusCodeStr != null)
/*     */     {
/* 272 */       int statusCode = Integer.parseInt(statusCodeStr);
/* 273 */       if (statusCode < 0)
/*     */       {
/* 276 */         statusMsg = binder.getLocal("StatusMessage");
/* 277 */         Report.trace(null, "Refinery provider reported error: " + statusMsg, null);
/* 278 */         prov.markErrorState(-1, new ServiceException(new Throwable(), statusMsg, new Object[0]));
/* 279 */         RefineryProviderManager.updateProviderStatus(pname, statusCodeStr, statusMsg);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 285 */       isValid = DataBinderUtils.getBoolean(binder, "IsValidRefinery", false);
/* 286 */       if (!isValid)
/*     */       {
/* 288 */         String contentType = binder.getEnvironmentValue("CONTENT_TYPE");
/* 289 */         isValid = (contentType != null) && (contentType.equalsIgnoreCase("application/zip"));
/*     */       }
/*     */ 
/* 292 */       if (!isValid)
/*     */       {
/* 295 */         IdcMessage invalidMsg = IdcMessageFactory.lc("csRefineryNotVaild", new Object[] { pname });
/* 296 */         Report.trace(null, "IBR provider " + pname + " is not a valid IBR", null);
/* 297 */         prov.markErrorState(-1, new ServiceException(new Throwable(), invalidMsg));
/* 298 */         RefineryProviderManager.updateProviderStatus(pname, String.valueOf(-1), invalidMsg);
/*     */       }
/*     */     }
/*     */ 
/* 302 */     if (isValid)
/*     */     {
/* 305 */       RefineryProviderManager.updateProviderStatus(pname, String.valueOf(0), (String)null);
/*     */     }
/* 307 */     return isValid;
/*     */   }
/*     */ 
/*     */   protected DataBinder doTransfer(Provider provider, String svcName) throws ServiceException, DataException
/*     */   {
/* 312 */     return doTransfer(false, provider, svcName, null);
/*     */   }
/*     */ 
/*     */   protected DataBinder doTransfer(boolean attachFile, Provider provider, String svcName, Properties props)
/*     */     throws ServiceException, DataException
/*     */   {
/* 319 */     DataBinder transferBinder = new DataBinder();
/* 320 */     DataBinder response = new DataBinder();
/* 321 */     if (props != null)
/*     */     {
/* 323 */       transferBinder.setLocalData(props);
/*     */     }
/* 325 */     transferBinder.putLocal("IdcService", svcName);
/* 326 */     transferBinder.putLocal("Agent_Name", RefineryProviderManager.m_agentName);
/* 327 */     transferBinder.putLocal("ClusterNodeName", RefineryProviderManager.m_clusterNodeName);
/* 328 */     transferBinder.m_hasAttachedFiles = attachFile;
/* 329 */     String jobDir = null;
/* 330 */     String provName = provider.getName();
/* 331 */     if (attachFile)
/*     */     {
/* 333 */       transferBinder.setEnvironmentValue("REQUEST_METHOD", "POST");
/* 334 */       jobDir = RefineryUtils.getJobDataDir(transferBinder);
/* 335 */       if (!buildConversionPackage(jobDir, transferBinder, provName))
/*     */       {
/* 337 */         return response;
/*     */       }
/*     */ 
/*     */     }
/* 342 */     else if (SystemUtils.m_verbose)
/*     */     {
/* 345 */       Report.trace("ibrsupport", "Transfering binder to IBR: " + provName + "; on service: " + svcName + "; without attachments:" + transferBinder.toString(), null);
/*     */     }
/*     */ 
/* 351 */     provider.markState("active");
/* 352 */     String dConvJobID = transferBinder.getLocal("dConvJobID");
/* 353 */     if ((dConvJobID == null) || (dConvJobID.length() == 0))
/*     */     {
/* 355 */       dConvJobID = "<no job>";
/*     */     }
/* 357 */     Report.trace("ibrperformance", "job progress for: " + dConvJobID + " -- calling " + svcName + " on " + provName, null);
/* 358 */     ServerRequestUtils.doAdminProxyRequest(provider, transferBinder, response, new ExecutionContextAdaptor());
/*     */ 
/* 360 */     checkResponseIsValidIbr(provider, response);
/* 361 */     provider.pollConnectionState();
/* 362 */     if ((RefineryUtils.m_refineryTransferCleanUp) && (attachFile) && (jobDir != null))
/*     */     {
/* 364 */       FileUtils.deleteDirectory(new File(jobDir), true);
/*     */     }
/* 366 */     traceTransferResponse(provider, response);
/* 367 */     return response;
/*     */   }
/*     */ 
/*     */   protected boolean buildConversionPackage(String jobDir, DataBinder transferBinder, String provName)
/*     */     throws ServiceException, DataException
/*     */   {
/* 373 */     DataResultSet fileSet = new DataResultSet(ZipFunctions.ZIP_FILE_COLUMNS);
/*     */ 
/* 375 */     Map descMap = new HashMap();
/* 376 */     ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/* 377 */     context.setCachedObject("Workspace", RefineryProviderManager.m_workspace);
/* 378 */     FileStoreProvider fs = FileStoreProviderLoader.initFileStore(context);
/*     */ 
/* 380 */     boolean exists = DataBinderUtils.getBoolean(transferBinder, "isWebRowPresent", false);
/* 381 */     if (exists)
/*     */     {
/* 383 */       String renditionType = "primaryFile";
/* 384 */       boolean isPrimary = DataBinderUtils.getBoolean(transferBinder, "dIsPrimary", false);
/* 385 */       if (!isPrimary)
/*     */       {
/* 387 */         renditionType = "alternateFile";
/*     */       }
/* 389 */       transferBinder.putLocal("RenditionId", renditionType);
/* 390 */       IdcFileDescriptor inFileD = fs.createDescriptor(transferBinder, null, context);
/* 391 */       String fileSizeStr = ((BasicIdcFileDescriptor)inFileD).getCacheProperty("fileSize");
/* 392 */       long fsize = NumberUtils.parseLong(fileSizeStr, 0L);
/*     */ 
/* 394 */       long maxFileSizeToCompress = SharedObjects.getTypedEnvironmentInt("RefineryNativeFileSizeMaxCompress", 1073741824, 5, 1);
/*     */ 
/* 400 */       if (PluginFilters.filter("conversionNativeFilePath", RefineryProviderManager.m_workspace, transferBinder, context) == -1)
/*     */       {
/* 402 */         return false;
/*     */       }
/* 404 */       if (fsize <= maxFileSizeToCompress)
/*     */       {
/* 406 */         createFileSetEntry(transferBinder, fileSet, descMap, fs, context);
/*     */       }
/*     */       else
/*     */       {
/* 411 */         String nativePath = transferBinder.getLocal("NativeFile:path");
/* 412 */         if (nativePath == null)
/*     */         {
/* 414 */           nativePath = fs.getFilesystemPath(inFileD, context);
/* 415 */           transferBinder.putLocal("NativeFile:path", nativePath);
/*     */ 
/* 417 */           IdcFileDescriptor outFileD = fs.createDescriptor(transferBinder, null, context);
/* 418 */           String inFilePath = inFileD.getProperty("path");
/* 419 */           String inFile = FileUtils.getName(inFilePath);
/* 420 */           String outFilePath = outFileD.getProperty("path");
/* 421 */           String outFile = FileUtils.getName(outFilePath);
/* 422 */           transferBinder.putLocal("InFile", inFile);
/* 423 */           transferBinder.putLocal("OutFile", outFile);
/* 424 */           transferBinder.putLocal("WebFilePath", outFilePath);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 430 */       return false;
/*     */     }
/*     */ 
/* 435 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 438 */     transferBinder.addResultSet("AdditionalRenditions", renSet);
/* 439 */     IdcVector row = new IdcVector();
/* 440 */     row.add("conversiondata.hda");
/* 441 */     row.add(jobDir + "conversiondata.hda");
/* 442 */     fileSet.addRow(row);
/*     */     try
/*     */     {
/* 446 */       Object[] o = { fileSet, descMap };
/* 447 */       this.m_refineryQueueContext.setCachedObject("alterRefineryPreConversionPackage:parameters", o);
/* 448 */       int ret = PluginFilters.filter("alterRefineryPreConversionPackage", RefineryProviderManager.m_workspace, transferBinder, this.m_refineryQueueContext);
/* 449 */       if (ret == -1)
/*     */       {
/* 451 */         String errMsg = LocaleResources.localizeMessage("!csFilterError,alterRefineryPreConversionPackage", null);
/*     */ 
/* 453 */         throw new DataException(errMsg);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 458 */       String errMsg = LocaleUtils.encodeMessage("csFilterError", e.getMessage(), "alterRefineryPreConversionPackage");
/*     */ 
/* 460 */       throw new DataException(errMsg);
/*     */     }
/*     */ 
/* 463 */     ResourceUtils.serializeDataBinder(jobDir, "conversiondata.hda", transferBinder, true, false);
/*     */ 
/* 465 */     String dConvJobID = transferBinder.getLocal("dConvJobID");
/* 466 */     String zipFile = "convert-" + dConvJobID;
/* 467 */     String zipPath = jobDir + zipFile + ".zip";
/* 468 */     if (!RefineryUtils.m_useIdcZip)
/*     */     {
/* 470 */       ZipFunctions.createZipFileEx(zipPath, fileSet, descMap, fs);
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 476 */         IdcZipFile zip = IdcZipFunctions.newIdcZipFile(zipPath);
/* 477 */         Map zipOptions = new HashMap();
/* 478 */         zipOptions.put("fileStore", fs);
/* 479 */         zipOptions.put("descriptorMap", descMap);
/* 480 */         zipOptions.put("entryNameField", ZipFunctions.ZIP_FILE_COLUMNS[0]);
/* 481 */         zipOptions.put("filePathField", ZipFunctions.ZIP_FILE_COLUMNS[1]);
/* 482 */         IdcZipFunctions.addEntriesFromResultSet(zip, fileSet, zipOptions);
/* 483 */         zip.finish(-1);
/*     */       }
/*     */       catch (Exception zipExp)
/*     */       {
/* 487 */         Report.trace(null, "Error creating package for IBR", zipExp);
/* 488 */         throw new ServiceException(zipExp);
/*     */       }
/*     */     }
/* 491 */     transferBinder.putLocal("ZipFile:path", zipPath);
/* 492 */     if (SystemUtils.m_verbose)
/*     */     {
/* 494 */       String svcName = transferBinder.getLocal("IdcService");
/* 495 */       IdcStringBuilder debug = new IdcStringBuilder("Transfering to IBR with attachments: " + provName + "; on service: " + svcName);
/*     */ 
/* 497 */       for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*     */       {
/* 499 */         Map zipRow = fileSet.getCurrentRowMap();
/* 500 */         String file = (String)zipRow.get(ZipFunctions.ZIP_FILE_COLUMNS[1]);
/* 501 */         debug.append("\nfile in zip: " + file);
/*     */       }
/* 503 */       Report.trace("ibrsupport", debug.toString() + "\nTransfer binder:" + transferBinder.toString(), null);
/*     */     }
/* 505 */     return true;
/*     */   }
/*     */ 
/*     */   protected void createFileSetEntry(DataBinder transferBinder, DataResultSet fileSet, Map descMap, FileStoreProvider fs, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 512 */     ExecutionContextAdaptor workContext = new ExecutionContextAdaptor();
/* 513 */     workContext.setParentContext(cxt);
/* 514 */     workContext.setCachedObject("Workspace", RefineryProviderManager.m_workspace);
/* 515 */     String renditionType = "primaryFile";
/* 516 */     boolean isPrimary = DataBinderUtils.getBoolean(transferBinder, "dIsPrimary", false);
/* 517 */     if (!isPrimary)
/*     */     {
/* 519 */       renditionType = "alternateFile";
/*     */     }
/* 521 */     transferBinder.putLocal("RenditionId", renditionType);
/* 522 */     IdcFileDescriptor inFileD = fs.createDescriptor(transferBinder, null, workContext);
/*     */ 
/* 524 */     transferBinder.putLocal("RenditionId", "webViewableFile");
/* 525 */     IdcFileDescriptor outFileD = fs.createDescriptor(transferBinder, null, workContext);
/*     */ 
/* 527 */     String inFilePath = inFileD.getProperty("path");
/* 528 */     String inFile = FileUtils.getName(inFilePath);
/* 529 */     String outFilePath = outFileD.getProperty("path");
/* 530 */     String outFile = FileUtils.getName(outFilePath);
/*     */ 
/* 532 */     transferBinder.putLocal("InFile", inFile);
/* 533 */     transferBinder.putLocal("OutFile", outFile);
/* 534 */     transferBinder.putLocal("WebFilePath", outFilePath);
/*     */ 
/* 537 */     String tmp = transferBinder.getLocal("NativeFile:path");
/* 538 */     if (tmp != null)
/*     */     {
/* 540 */       inFilePath = tmp;
/*     */     }
/*     */     else
/*     */     {
/* 544 */       descMap.put(inFile, inFileD);
/*     */     }
/* 546 */     IdcVector row = new IdcVector();
/* 547 */     row.addElement(inFile);
/* 548 */     row.addElement(inFilePath);
/* 549 */     fileSet.addRow(row);
/*     */   }
/*     */ 
/*     */   protected void traceTransferResponse(Provider provider, DataBinder response)
/*     */   {
/* 554 */     if (!SystemUtils.m_verbose)
/*     */       return;
/* 556 */     String svcName = response.getAllowMissing("IdcService");
/* 557 */     IdcStringBuilder traceMsg = new IdcStringBuilder("The provider '" + provider.getName() + "' ");
/* 558 */     String statusCode = response.getAllowMissing("StatusCode");
/* 559 */     if (svcName != null)
/*     */     {
/* 561 */       traceMsg.append("executed service '" + svcName + "' ");
/*     */     }
/*     */     else
/*     */     {
/* 565 */       traceMsg.append("was used ");
/*     */     }
/* 567 */     if ((statusCode != null) && (!statusCode.equals("0")))
/*     */     {
/* 569 */       traceMsg.append("and returned an unexpected data. Status: " + response.getAllowMissing("StatusMessage"));
/*     */     }
/*     */     else
/*     */     {
/* 574 */       traceMsg.append("successfully");
/*     */     }
/* 576 */     traceMsg.append("\nResponse binder: " + response.toString());
/* 577 */     Report.trace("ibrsupport", traceMsg.toString(), null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 583 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102737 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.TransferManager
 * JD-Core Version:    0.5.4
 */