/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Stack;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FileSystemCheck
/*     */   implements IdcAnalyzeTask
/*     */ {
/*     */   protected ExecutionContext m_executionContext;
/*     */   protected Workspace m_workspace;
/*     */   protected Properties m_context;
/*     */   protected FileStoreProvider m_fileStore;
/*     */   protected FileStoreProviderHelper m_fileUtils;
/*     */   protected int m_fileCount;
/*     */   protected String m_logName;
/*     */   protected IdcAnalyzeApp m_analyzer;
/*     */   protected String m_weblayoutDir;
/*     */   protected String m_vaultDir;
/*     */   protected int m_totalErrors;
/*     */ 
/*     */   public FileSystemCheck()
/*     */   {
/*  55 */     this.m_totalErrors = 0;
/*     */   }
/*     */ 
/*     */   public int getErrorCount() {
/*  59 */     return this.m_totalErrors;
/*     */   }
/*     */ 
/*     */   public void init(IdcAnalyzeApp analyzer, Properties environment, Workspace ws)
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     this.m_executionContext = new ExecutionContextAdaptor();
/*  66 */     this.m_analyzer = analyzer;
/*  67 */     this.m_workspace = ws;
/*  68 */     this.m_context = environment;
/*  69 */     this.m_fileCount = 0;
/*     */ 
/*  71 */     this.m_logName = ((String)this.m_context.get("IdcCommandFile"));
/*     */ 
/*  73 */     loadRenditionInfo();
/*     */ 
/*  76 */     this.m_weblayoutDir = IdcAnalyzeUtils.computeWeblayoutDir(this.m_context);
/*  77 */     this.m_analyzer.debug(new StringBuilder().append("FileSystemCheck: Using WeblayoutDir = ").append(this.m_weblayoutDir).toString());
/*     */ 
/*  79 */     this.m_vaultDir = IdcAnalyzeUtils.computeVaultDir();
/*  80 */     this.m_analyzer.debug(new StringBuilder().append("FileSystemCheck: Using VaultDir = ").append(this.m_vaultDir).toString());
/*     */ 
/*  82 */     if (StringUtils.convertToBool((String)this.m_context.get("CheckExtra"), false))
/*     */     {
/*  84 */       if (!SharedObjects.getEnvValueAsBoolean("IdcAnalyzeEnableExtraFileCheck", false))
/*     */       {
/*  86 */         throw new ServiceException(LocaleUtils.encodeMessage("csIDCAnalyzeExtraFileCheckDeprecated", null));
/*     */       }
/*     */ 
/*  89 */       createTempDirectory();
/*  90 */       if (StringUtils.convertToBool((String)this.m_context.get("SafeDeleteExtra"), false))
/*     */       {
/*  92 */         createSafeDeleteDirectory();
/*     */       }
/*     */     }
/*     */ 
/*  96 */     this.m_fileStore = FileStoreProviderLoader.initFileStore(this.m_executionContext);
/*  97 */     this.m_fileUtils = FileStoreProviderHelper.getFileStoreProviderUtils(this.m_fileStore, this.m_executionContext);
/*     */   }
/*     */ 
/*     */   public boolean doTask()
/*     */     throws DataException, ServiceException
/*     */   {
/* 107 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 108 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeCheckFilesystemMsg", new Object[0]));
/* 109 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeLineBreak", new Object[0]));
/*     */ 
/* 111 */     Vector missingFileList = new IdcVector();
/*     */ 
/* 113 */     String batchNumStr = (String)this.m_context.get("FileBatchSize");
/* 114 */     int batchNum = NumberUtils.parseInteger(batchNumStr, 1000);
/*     */ 
/* 116 */     int startVal = 0;
/* 117 */     int maxDid = -1;
/* 118 */     int didCount = -1;
/* 119 */     int endVal = -1;
/* 120 */     boolean isBoundedCheck = false;
/* 121 */     boolean isRangeCheck = false;
/*     */ 
/* 123 */     String startRange = (String)this.m_context.get("StartID");
/* 124 */     if (startRange != null)
/*     */     {
/* 126 */       isRangeCheck = true;
/* 127 */       startVal = NumberUtils.parseInteger(startRange, 0);
/*     */     }
/* 129 */     String endRange = (String)this.m_context.get("EndID");
/* 130 */     if (endRange != null)
/*     */     {
/* 132 */       maxDid = NumberUtils.parseInteger(endRange, -1);
/*     */ 
/* 135 */       if (maxDid > 0)
/*     */       {
/* 137 */         ++maxDid;
/*     */       }
/* 139 */       didCount = maxDid - startVal;
/* 140 */       isBoundedCheck = true;
/*     */     }
/*     */ 
/* 143 */     if (!isBoundedCheck)
/*     */     {
/* 145 */       maxDid = IdcAnalyzeUtils.getMaxDid(this.m_workspace);
/* 146 */       didCount = IdcAnalyzeUtils.getRevisionCount(this.m_workspace);
/*     */     }
/*     */ 
/* 149 */     endVal = (batchNum > didCount) ? maxDid : startVal + batchNum;
/* 150 */     int count = 0;
/* 151 */     int iteration = didCount / 50 + 1;
/*     */ 
/* 153 */     FileSystemCheckData fsData = null;
/* 154 */     if (StringUtils.convertToBool((String)this.m_context.get("CheckExtra"), false))
/*     */     {
/* 156 */       if (isRangeCheck)
/*     */       {
/* 158 */         this.m_analyzer.error(IdcMessageFactory.lc("csIDCAnalyzeCannotCheckExtraForRange", new Object[0]));
/*     */       }
/*     */       else
/*     */       {
/* 162 */         fsData = new FileSystemCheckData(this.m_context);
/*     */ 
/* 165 */         iteration = didCount / 50 + 1;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 171 */     while (startVal < maxDid)
/*     */     {
/* 173 */       Properties props = new Properties();
/*     */ 
/* 175 */       props.put("startID", new StringBuilder().append(startVal).append("").toString());
/* 176 */       props.put("endID", new StringBuilder().append(endVal).append("").toString());
/*     */ 
/* 179 */       props.put("columns", "dID, dExtension, dIsWebFormat, dIsPrimary");
/* 180 */       props.put("table", "Documents");
/* 181 */       Hashtable docMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", props, this.m_workspace, "dID", true, true);
/*     */ 
/* 184 */       String revColumns = "Revisions.*, DocMeta.*";
/* 185 */       props.put("columns", revColumns);
/* 186 */       props.put("table", "Revisions,DocMeta");
/* 187 */       props.put("whereClause", " Revisions.dID = DocMeta.dID");
/* 188 */       props.put("orderby", "dSecurityGroup, dDocAccount, dDocType");
/* 189 */       Hashtable revMap = IdcAnalyzeUtils.makeMap("IDCAnalyzeGenericSource", props, this.m_workspace, "dID", true, false);
/*     */ 
/* 191 */       startVal = endVal;
/* 192 */       endVal += batchNum;
/* 193 */       if ((isBoundedCheck) && (endVal > maxDid))
/*     */       {
/* 195 */         endVal = maxDid;
/*     */       }
/*     */ 
/* 198 */       if ((docMap == null) || (revMap == null))
/*     */       {
/* 200 */         return false;
/*     */       }
/*     */       Properties revProps;
/*     */       String dID;
/*     */       Hashtable fileHash;
/*     */       Enumeration en;
/* 202 */       for (Enumeration e = revMap.keys(); e.hasMoreElements(); ++count)
/*     */       {
/* 204 */         if (count % iteration == 1)
/*     */         {
/* 206 */           this.m_analyzer.incProgress();
/*     */         }
/*     */ 
/* 209 */         String curDid = (String)e.nextElement();
/* 210 */         revProps = (Properties)revMap.get(curDid);
/*     */ 
/* 212 */         dID = (String)revProps.get("dID");
/* 213 */         Vector docPropsVector = (Vector)docMap.get(dID);
/*     */ 
/* 217 */         fileHash = null;
/*     */ 
/* 219 */         fileHash = computeFileObjects(revProps, docPropsVector);
/*     */ 
/* 221 */         for (en = fileHash.keys(); en.hasMoreElements(); )
/*     */         {
/* 223 */           String fileType = (String)en.nextElement();
/* 224 */           Object fileObject = fileHash.get(fileType);
/* 225 */           boolean isVault = StringUtils.match(fileType, "*Vault", true);
/* 226 */           String missingFileString = null;
/*     */           try
/*     */           {
/* 233 */             if (fileObject instanceof File)
/*     */             {
/* 235 */               File f = (File)fileObject;
/* 236 */               missingFileString = f.getAbsolutePath();
/* 237 */               FileUtils.validatePath(missingFileString, IdcMessageFactory.lc("syFileUtilsFileNotFound", new Object[0]), 1);
/*     */             }
/* 240 */             else if (fileObject instanceof IdcFileDescriptor)
/*     */             {
/* 242 */               IdcFileDescriptor d = (IdcFileDescriptor)fileObject;
/* 243 */               boolean exists = this.m_fileUtils.fileExists(d, this.m_executionContext);
/* 244 */               if (!exists)
/*     */               {
/* 246 */                 throw new ServiceException(null, "syFileUtilsFileNotFound", new Object[0]);
/*     */               }
/*     */             }
/*     */             else
/*     */             {
/* 251 */               throw new AssertionError(new StringBuilder().append("!$Unrecognized class ").append(fileObject.getClass().getName()).append(" checking filesystem.").toString());
/*     */             }
/*     */ 
/* 255 */             if (fsData != null)
/*     */             {
/* 257 */               this.m_fileCount += 1;
/* 258 */               fsData.addFile(revProps, fileObject, isVault);
/*     */             }
/*     */           }
/*     */           catch (ServiceException s)
/*     */           {
/* 263 */             missingFileList.addElement(fileObject);
/* 264 */             String idcCmd = (isVault) ? "csIDCAnalyzeDeleteVaultCommand" : "csIDCAnalyzeUpdateWebCommand";
/*     */ 
/* 266 */             IdcMessage cmd = IdcMessageFactory.lc(idcCmd, new Object[] { dID });
/* 267 */             this.m_analyzer.log(this.m_logName, cmd);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 273 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*     */ 
/* 275 */     int errorCount = 0;
/* 276 */     for (int i = 0; i < missingFileList.size(); ++errorCount)
/*     */     {
/* 280 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeMissingFile", new Object[] { missingFileList.elementAt(i) }));
/*     */ 
/* 276 */       ++i;
/*     */     }
/*     */ 
/* 283 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeEntriesChecked", new Object[] { Integer.valueOf(count) }));
/* 284 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorCount", new Object[] { Integer.valueOf(errorCount) }));
/* 285 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorsFixed", new Object[] { Integer.valueOf(0) }));
/* 286 */     this.m_totalErrors += errorCount;
/*     */ 
/* 288 */     if (fsData != null)
/*     */     {
/* 290 */       findExtra(fsData);
/*     */     }
/* 292 */     this.m_analyzer.setProgress(80);
/*     */ 
/* 294 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean findExtra(FileSystemCheckData fsData)
/*     */     throws DataException
/*     */   {
/* 300 */     Vector extraFileList = new IdcVector();
/* 301 */     Stack dirStack = new Stack();
/* 302 */     Vector ignoreList = new IdcVector();
/* 303 */     Vector dirPrefixList = new IdcVector();
/*     */ 
/* 305 */     int errorCount = 0;
/* 306 */     int count = 0;
/* 307 */     int errorsFixed = 0;
/*     */ 
/* 309 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 310 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeFindExtraFiles", new Object[0]));
/* 311 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeLineBreak", new Object[0]));
/*     */ 
/* 313 */     dirPrefixList.addElement(this.m_vaultDir);
/* 314 */     dirPrefixList.addElement(this.m_weblayoutDir);
/*     */ 
/* 316 */     int iteration = this.m_fileCount / 30 + 1;
/* 317 */     for (Enumeration en = dirPrefixList.elements(); en.hasMoreElements(); )
/*     */     {
/* 319 */       String dirPrefix = FileUtils.directorySlashes((String)en.nextElement());
/* 320 */       boolean isVault = false;
/* 321 */       boolean isFirstDirLevel = true;
/* 322 */       if (dirPrefix.equals(this.m_weblayoutDir))
/*     */       {
/* 325 */         dirStack.push(new StringBuilder().append(dirPrefix).append("groups/").toString());
/* 326 */         isVault = false;
/*     */       }
/*     */       else
/*     */       {
/* 330 */         dirStack.push(dirPrefix);
/* 331 */         isVault = true;
/*     */       }
/*     */ 
/* 334 */       while (!dirStack.empty())
/*     */       {
/* 336 */         String curDir = FileUtils.directorySlashes((String)dirStack.pop());
/* 337 */         this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 338 */         this.m_analyzer.logStraight(IdcMessageFactory.lc("csIDCAnalyzeScanMsg", new Object[] { curDir }));
/* 339 */         String[] dirContents = FileUtils.getMatchingFileNames(curDir, "*");
/*     */ 
/* 341 */         this.m_analyzer.debug(new StringBuilder().append("FSCheck->ExtraCheck: Current dir = ").append(curDir).toString());
/* 342 */         this.m_analyzer.debug(new StringBuilder().append("FSCheck->ExtraCheck: Items = ").append((dirContents == null) ? "None" : new StringBuilder().append(dirContents.length).append("").toString()).toString());
/*     */ 
/* 346 */         Hashtable files = new Hashtable();
/* 347 */         Vector extras = new IdcVector();
/*     */ 
/* 350 */         sortListNumeric(dirContents);
/* 351 */         fsData.sortDirContents(curDir, dirContents, files, extras, isVault, this.m_fileUtils);
/*     */ 
/* 355 */         for (Enumeration hashEnum = files.elements(); hashEnum.hasMoreElements(); )
/*     */         {
/* 360 */           Vector fileList = (Vector)hashEnum.nextElement();
/* 361 */           for (int j = 0; j < fileList.size(); ++j)
/*     */           {
/* 363 */             boolean isFile = true;
/* 364 */             String dirItem = (String)fileList.elementAt(j);
/* 365 */             String dirItemPath = new StringBuilder().append(curDir).append(dirItem).toString();
/* 366 */             File dirItemFile = new File(dirItemPath);
/* 367 */             if (dirItemFile.isDirectory())
/*     */             {
/* 369 */               extras.addElement(dirItem);
/*     */             }
/*     */             else
/*     */             {
/* 374 */               isFile = fsData.lookupFile(dirItemPath, isVault, this.m_fileUtils);
/* 375 */               if (isFile)
/*     */               {
/* 377 */                 if (++count % iteration != 1)
/*     */                   continue;
/* 379 */                 this.m_analyzer.incProgress();
/*     */               }
/*     */               else
/*     */               {
/* 386 */                 extras.addElement(dirItem);
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 394 */         for (int i = 0; i < extras.size(); ++i)
/*     */         {
/* 396 */           String dirItem = (String)extras.elementAt(i);
/* 397 */           String dirItemPath = new StringBuilder().append(curDir).append(dirItem).toString();
/*     */           try
/*     */           {
/* 401 */             FileUtils.validatePath(dirItemPath, IdcMessageFactory.lc("apDirNotDir", new Object[0]), 0);
/*     */           }
/*     */           catch (ServiceException e)
/*     */           {
/* 407 */             boolean isRemoved = deleteExtraFile(dirItemPath, isVault);
/* 408 */             if (!isRemoved)
/*     */             {
/* 410 */               this.m_analyzer.debug(new StringBuilder().append("FSCheck->ExtraCheck: Adding dir = ").append(dirItemPath).toString());
/* 411 */               extraFileList.addElement(dirItemPath);
/*     */             }
/*     */             else
/*     */             {
/* 415 */               ++errorsFixed;
/* 416 */               boolean isDelete = StringUtils.convertToBool((String)this.m_context.get("DeleteExtra"), false);
/*     */ 
/* 418 */               String msg = (isDelete) ? "!csIDCAnalyzeFileDeleted" : "!csIDCAnalyzeFileSafeDeleted";
/*     */ 
/* 420 */               extraFileList.addElement(new StringBuilder().append(dirItemPath).append(" ").append(LocaleResources.localizeMessage(msg, null)).toString());
/*     */             }
/*     */ 
/* 424 */             break label817:
/*     */           }
/*     */ 
/* 433 */           if ((dirItem.startsWith("documents")) || (curDir.endsWith("documents/")) || (dirItem.startsWith("@")) || ((isFirstDirLevel) && (!dirItem.startsWith("~"))))
/*     */           {
/* 439 */             dirStack.push(dirItemPath);
/*     */           }
/*     */           else
/*     */           {
/* 443 */             ignoreList.addElement(dirItemPath);
/*     */           }
/*     */         }
/* 446 */         isFirstDirLevel = false;
/*     */       }
/*     */     }
/*     */ 
/* 450 */     label817: this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/*     */ 
/* 453 */     for (int i = 0; i < ignoreList.size(); ++i)
/*     */     {
/* 455 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeIgnoredFile", new Object[] { ignoreList.elementAt(i) }));
/*     */     }
/*     */ 
/* 458 */     for (int i = 0; i < extraFileList.size(); ++errorCount)
/*     */     {
/* 460 */       String filePath = (String)extraFileList.elementAt(i);
/* 461 */       this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeExtraFile", new Object[] { filePath }));
/*     */ 
/* 458 */       ++i;
/*     */     }
/*     */ 
/* 463 */     this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 464 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorCount", new Object[] { Integer.valueOf(errorCount) }));
/* 465 */     this.m_analyzer.log(IdcMessageFactory.lc("csIDCAnalyzeErrorsFixed", new Object[] { Integer.valueOf(errorsFixed) }));
/* 466 */     this.m_totalErrors += errorCount;
/* 467 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean deleteExtraFile(String filePath, boolean isVault)
/*     */   {
/* 472 */     if (filePath == null)
/*     */     {
/* 474 */       return false;
/*     */     }
/*     */ 
/* 477 */     boolean isDelete = StringUtils.convertToBool((String)this.m_context.get("DeleteExtra"), false);
/*     */ 
/* 479 */     boolean isSafeDelete = StringUtils.convertToBool((String)this.m_context.get("SafeDeleteExtra"), false);
/*     */ 
/* 481 */     boolean isRemoved = false;
/*     */ 
/* 483 */     if ((isDelete) || (isSafeDelete))
/*     */     {
/* 486 */       String internalDir = FileUtils.directorySlashes(FileUtils.getDirectory(filePath));
/*     */       try
/*     */       {
/* 492 */         if (isDelete)
/*     */         {
/* 494 */           FileUtils.deleteFile(filePath);
/*     */ 
/* 496 */           this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 497 */           this.m_analyzer.logStraight(IdcMessageFactory.lc("csIDCAnalyzeFileRemoved", new Object[] { filePath }));
/*     */         }
/* 499 */         else if (isSafeDelete)
/*     */         {
/* 501 */           String safeDir = (String)this.m_context.get("SafeDir");
/*     */ 
/* 504 */           int index = filePath.indexOf("/");
/* 505 */           String fixedFilePath = filePath;
/* 506 */           if (index > 0)
/*     */           {
/* 508 */             fixedFilePath = filePath.substring(index);
/*     */           }
/* 510 */           index = internalDir.indexOf("/");
/* 511 */           String fixedInternalDir = internalDir;
/* 512 */           if (index > 0)
/*     */           {
/* 514 */             fixedInternalDir = internalDir.substring(index);
/*     */           }
/*     */ 
/* 517 */           FileUtils.checkOrCreateSubDirectory(safeDir, fixedInternalDir);
/*     */ 
/* 520 */           FileUtils.copyFile(filePath, new StringBuilder().append(safeDir).append(fixedFilePath).toString());
/* 521 */           FileUtils.deleteFile(filePath);
/* 522 */           this.m_analyzer.log(IdcMessageFactory.lc("csLinefeed", new Object[0]));
/* 523 */           this.m_analyzer.logStraight(IdcMessageFactory.lc("csIDCAnalyzeFileMoved", new Object[] { filePath, new StringBuilder().append(FileUtils.getWorkingDir()).append(safeDir).append(filePath).toString() }));
/*     */         }
/*     */ 
/* 526 */         isRemoved = true;
/*     */       }
/*     */       catch (ServiceException f)
/*     */       {
/* 530 */         this.m_analyzer.error(f, IdcMessageFactory.lc("syGeneralError", new Object[0]));
/*     */       }
/*     */     }
/* 533 */     return isRemoved;
/*     */   }
/*     */ 
/*     */   protected Hashtable computeFileObjects(Properties revProps, Vector docRow)
/*     */     throws DataException, ServiceException
/*     */   {
/* 541 */     Hashtable fileHash = new Hashtable(6);
/* 542 */     Object fileObject = null;
/*     */ 
/* 544 */     if ((revProps == null) || (docRow == null))
/*     */     {
/* 546 */       return fileHash;
/*     */     }
/*     */ 
/* 550 */     for (int i = 0; i < docRow.size(); ++i)
/*     */     {
/* 552 */       Properties row = (Properties)docRow.elementAt(i);
/*     */ 
/* 554 */       String primaryStr = (String)row.get("dIsPrimary");
/* 555 */       boolean isPrimary = StringUtils.convertToBool(primaryStr, false);
/*     */ 
/* 557 */       String webStr = (String)row.get("dIsWebFormat");
/* 558 */       boolean isWebFormat = StringUtils.convertToBool(webStr, false);
/*     */ 
/* 560 */       Properties tmpProps = (Properties)revProps.clone();
/* 561 */       PropParameters revParams = new PropParameters(tmpProps);
/* 562 */       PropParameters docParams = new PropParameters((Properties)docRow.elementAt(i));
/* 563 */       if (isWebFormat)
/*     */       {
/* 565 */         tmpProps.put("RenditionId", "webViewableFile");
/* 566 */         fileObject = computeWebFormatPath(revParams);
/* 567 */         if (fileObject == null)
/*     */           continue;
/* 569 */         fileHash.put("Web", fileObject);
/*     */       }
/* 572 */       else if (isPrimary)
/*     */       {
/* 574 */         PropParameters allParameters = new PropParameters(tmpProps, docParams);
/* 575 */         tmpProps.put("RenditionId", "primaryFile");
/* 576 */         fileObject = computeVaultPath(revProps, allParameters, docParams);
/*     */ 
/* 578 */         if (fileObject != null)
/*     */         {
/* 580 */           fileHash.put("Primary Vault", fileObject);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 585 */         PropParameters allParameters = new PropParameters(tmpProps, docParams);
/* 586 */         tmpProps.put("RenditionId", "alternateFile");
/* 587 */         fileObject = computeVaultPath(revProps, allParameters, docParams);
/*     */ 
/* 589 */         if (fileObject == null)
/*     */           continue;
/* 591 */         fileHash.put("Alternate Vault", fileObject);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 596 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 598 */     int numRenditions = AdditionalRenditions.m_maxNum;
/* 599 */     for (int i = 0; i < numRenditions; ++i)
/*     */     {
/* 601 */       String renKey = new StringBuilder().append("dRendition").append(i + 1).toString();
/* 602 */       String renLabel = new StringBuilder().append("Rendition ").append(i + 1).toString();
/* 603 */       String testVal = revProps.getProperty(renKey);
/*     */ 
/* 608 */       if ((testVal == null) || (testVal.length() <= 0))
/*     */         continue;
/* 610 */       fileObject = computeRenditionPath(revProps, i + 1, renSet);
/* 611 */       if (fileObject == null)
/*     */         continue;
/* 613 */       fileHash.put(renLabel, fileObject);
/*     */     }
/*     */ 
/* 618 */     return fileHash;
/*     */   }
/*     */ 
/*     */   public Object computeWebFormatPath(Parameters revParams)
/*     */     throws DataException, ServiceException
/*     */   {
/* 624 */     String status = revParams.get("dStatus");
/* 625 */     if ((status.equalsIgnoreCase("EDIT")) || (status.equalsIgnoreCase("GENWWW")) || (status.equalsIgnoreCase("DELETED")))
/*     */     {
/* 629 */       return null;
/*     */     }
/*     */ 
/* 632 */     IdcFileDescriptor d = this.m_fileUtils.createDescriptorForRendition(revParams, "webViewableFile");
/*     */ 
/* 634 */     return d;
/*     */   }
/*     */ 
/*     */   public Object computeVaultPath(Properties revProps, Parameters revParams, Parameters docParams)
/*     */     throws DataException, ServiceException
/*     */   {
/* 641 */     String releaseState = revParams.get("dReleaseState");
/* 642 */     String status = revParams.get("dStatus");
/* 643 */     String docType = revParams.get("dDocType");
/*     */ 
/* 645 */     if ((status.equalsIgnoreCase("EDIT")) && (releaseState.equalsIgnoreCase("E")))
/*     */     {
/* 647 */       if ((docType == null) || (docType.length() == 0))
/*     */       {
/* 649 */         return null;
/*     */       }
/*     */     }
/* 652 */     else if (status.equalsIgnoreCase("DELETED"))
/*     */     {
/* 654 */       return null;
/*     */     }
/*     */ 
/* 657 */     IdcFileDescriptor file = this.m_fileStore.createDescriptor(revParams, null, this.m_executionContext);
/*     */ 
/* 659 */     return file;
/*     */   }
/*     */ 
/*     */   public Object computeRenditionPath(Properties revProps, int renditionNum, AdditionalRenditions renSet)
/*     */     throws DataException, ServiceException
/*     */   {
/* 666 */     String dRendition = (String)revProps.get(new StringBuilder().append("dRendition").append(renditionNum).toString());
/* 667 */     if (dRendition.length() > 0)
/*     */     {
/* 669 */       PropParameters revParams = new PropParameters(revProps);
/* 670 */       revProps.put("RenditionId", new StringBuilder().append("rendition:").append(dRendition).toString());
/*     */ 
/* 672 */       IdcFileDescriptor d = this.m_fileStore.createDescriptor(revParams, null, this.m_executionContext);
/* 673 */       return d;
/*     */     }
/* 675 */     return null;
/*     */   }
/*     */ 
/*     */   public void createSafeDeleteDirectory()
/*     */     throws DataException
/*     */   {
/* 681 */     String logDir = (String)this.m_context.get("IdcAnalyzeLogDir");
/* 682 */     this.m_context.put("SafeDir", new StringBuilder().append(logDir).append("savedfiles/").toString());
/*     */     try
/*     */     {
/* 685 */       FileUtils.checkOrCreateSubDirectory(logDir, "savedfiles/");
/*     */     }
/*     */     catch (ServiceException s)
/*     */     {
/* 689 */       throw new DataException("csIDCAnalyzeCreateSafeDeleteDirError", s);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createTempDirectory()
/*     */     throws DataException
/*     */   {
/* 696 */     String tempDir = (String)this.m_context.get("TempDir");
/*     */ 
/* 700 */     String binDir = SystemUtils.getBinDir();
/* 701 */     tempDir = FileUtils.getAbsolutePath(binDir, tempDir);
/* 702 */     this.m_context.put("TempDir", tempDir);
/*     */     try
/*     */     {
/* 706 */       File dir = new File(new StringBuilder().append(tempDir).append("weblayout/").toString());
/* 707 */       FileUtils.deleteDirectory(dir, false);
/* 708 */       FileUtils.checkOrCreateDirectory(tempDir, 10);
/* 709 */       FileUtils.checkOrCreateSubDirectory(tempDir, "weblayout/");
/* 710 */       dir = new File(new StringBuilder().append(tempDir).append("vault/").toString());
/* 711 */       FileUtils.deleteDirectory(dir, false);
/* 712 */       FileUtils.checkOrCreateSubDirectory(tempDir, "vault/");
/*     */     }
/*     */     catch (ServiceException s)
/*     */     {
/* 716 */       throw new DataException(LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csIDCAnalyzeTempDirCleanupError", null, tempDir), null));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadRenditionInfo()
/*     */     throws DataException
/*     */   {
/* 726 */     DataResultSet drset = SharedObjects.getTable("AdditionalRenditionsSource");
/* 727 */     if (drset == null)
/*     */     {
/* 729 */       throw new DataException("!csIDCAnalyzeAdditionalRenditionsTableLoadError");
/*     */     }
/* 731 */     AdditionalRenditions renSet = new AdditionalRenditions();
/* 732 */     renSet.load(drset);
/* 733 */     SharedObjects.putTable("AdditionalRenditions", renSet);
/*     */   }
/*     */ 
/*     */   public static void sortListNumeric(String[] fileList)
/*     */   {
/* 738 */     if (fileList == null)
/*     */     {
/* 740 */       return;
/*     */     }
/*     */ 
/* 743 */     int num = fileList.length;
/*     */ 
/* 745 */     IdcComparator cmp = new IdcComparator()
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 749 */         String s1 = (String)obj1;
/* 750 */         int index = s1.indexOf(".");
/* 751 */         if (index > -1)
/*     */         {
/* 753 */           s1 = s1.substring(0, index);
/*     */         }
/* 755 */         String s2 = (String)obj2;
/* 756 */         index = s2.indexOf(".");
/* 757 */         if (index > -1)
/*     */         {
/* 759 */           s2 = s2.substring(0, index);
/*     */         }
/*     */ 
/* 762 */         int result = 0;
/*     */ 
/* 764 */         if (s1.length() > s2.length())
/*     */         {
/* 766 */           return 1;
/*     */         }
/* 768 */         if (s1.length() < s2.length())
/*     */         {
/* 770 */           return -1;
/*     */         }
/* 772 */         result = s1.compareTo(s2);
/* 773 */         return result;
/*     */       }
/*     */     };
/* 777 */     Sort.sort(fileList, 0, num - 1, cmp);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 784 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 71159 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.FileSystemCheck
 * JD-Core Version:    0.5.4
 */