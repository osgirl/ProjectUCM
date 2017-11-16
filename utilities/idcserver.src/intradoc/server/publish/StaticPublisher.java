/*     */ package intradoc.server.publish;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcTimer;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.LoggingUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetJoin;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.SimpleParameters;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.ProgressState;
/*     */ import intradoc.shared.ProgressStateUtils;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.regex.Pattern;
/*     */ 
/*     */ public class StaticPublisher
/*     */ {
/*     */   protected DataBinder m_workingBinder;
/*     */   protected Properties m_savedLocals;
/*     */   protected PageMerger m_pageMerger;
/*     */   protected String m_publishDataDir;
/*     */   protected List m_targetDirs;
/*     */   protected Map m_targets;
/*     */   protected Map m_ownedTargets;
/*     */   protected DataResultSet m_originalDirs;
/*     */   protected List<DataBinder> m_bindersToWrite;
/*     */   protected IdcTimer m_timer;
/*     */   protected WebPublishState m_publishState;
/*     */   protected boolean m_isSuperVerboseTrace;
/*     */   protected int m_maxParentsOnHistory;
/*     */   protected final int OWNER_DIR = 1;
/*     */   protected final int PARENT_DIR = 2;
/*     */   protected final String[] BINDER_FIELDS;
/*     */   protected final String[] FILES_FIELDS;
/*     */   protected final int INDEX_RELATIVE_PATH = 0;
/*     */   protected final int INDEX_DO_COPY = 5;
/*     */   protected final int INDEX_SOURCE_PATH = 6;
/*     */   protected final int INDEX_TARGET_PATH = 7;
/*     */ 
/*     */   public StaticPublisher()
/*     */   {
/*  82 */     this.m_maxParentsOnHistory = 2;
/*     */ 
/*  84 */     this.OWNER_DIR = 1;
/*  85 */     this.PARENT_DIR = 2;
/*     */ 
/*  87 */     this.BINDER_FIELDS = new String[] { "path", "srcPath", "idcComponentName", "class", "loadOrder", "doCopy" };
/*     */ 
/*  89 */     this.FILES_FIELDS = new String[] { "file", "source", "component", "class", "loadOrder", "didCopy" };
/*     */ 
/*  91 */     this.INDEX_RELATIVE_PATH = 0;
/*  92 */     this.INDEX_DO_COPY = 5;
/*     */ 
/*  94 */     this.INDEX_SOURCE_PATH = 6;
/*  95 */     this.INDEX_TARGET_PATH = 7;
/*     */   }
/*     */ 
/*     */   protected void addTarget(String targetDir, Map list, String source, String target, DataBinder binder)
/*     */   {
/* 111 */     String[] info = new String[8];
/*     */ 
/* 113 */     for (int i = 0; i < this.BINDER_FIELDS.length; ++i)
/*     */     {
/* 115 */       info[i] = binder.getLocal(this.BINDER_FIELDS[i]);
/*     */     }
/*     */ 
/* 118 */     info[6] = source;
/* 119 */     info[7] = target;
/*     */ 
/* 122 */     target = target.toLowerCase();
/* 123 */     list.put(target, info);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public DataResultSet doStaticPublishing(ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 133 */     init(cxt);
/* 134 */     FileUtils.checkOrCreateDirectory(this.m_publishDataDir, 0, 1);
/*     */ 
/* 136 */     String agent = "static-publish";
/* 137 */     String prefix = LoggingUtils.getLogFileMsgPrefix();
/* 138 */     if (prefix != null)
/*     */     {
/* 140 */       agent = agent + prefix;
/*     */     }
/*     */     try
/*     */     {
/* 144 */       int timeout = (SystemUtils.m_isDevelopmentEnvironment) ? 15000 : 150000;
/* 145 */       timeout = SharedObjects.getEnvironmentInt("StaticPublishLockTimeout", timeout);
/* 146 */       if (!FileUtils.reserveLongTermLock(this.m_publishDataDir, "publish", agent, timeout, true))
/*     */       {
/* 148 */         Report.trace("publish", "Static file publish blocked by lock placed by another process.", null);
/* 149 */         Object localObject1 = null;
/*     */         return localObject1;
/*     */       }
/* 151 */       DataResultSet allFiles = prepare();
/* 152 */       publish();
/* 153 */       DataResultSet localDataResultSet1 = allFiles;
/*     */ 
/* 158 */       return localDataResultSet1;
/*     */     }
/*     */     finally
/*     */     {
/* 157 */       cleanup();
/* 158 */       FileUtils.releaseLongTermLock(this.m_publishDataDir, "publish", agent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 167 */     this.m_workingBinder = ((DataBinder)cxt.getCachedObject("DataBinder"));
/* 168 */     if (this.m_workingBinder == null)
/*     */     {
/* 170 */       this.m_workingBinder = new DataBinder();
/* 171 */       Properties env = new Properties(SharedObjects.getSecureEnvironment());
/* 172 */       this.m_workingBinder.setEnvironment(env);
/* 173 */       cxt.setCachedObject("DataBinder", this.m_workingBinder);
/*     */     }
/*     */     else
/*     */     {
/* 177 */       this.m_savedLocals = this.m_workingBinder.getLocalData();
/*     */     }
/* 179 */     this.m_pageMerger = ((PageMerger)cxt.getCachedObject("PageMerger"));
/* 180 */     if (this.m_pageMerger == null)
/*     */     {
/* 182 */       this.m_pageMerger = new PageMerger(this.m_workingBinder, cxt);
/* 183 */       cxt.setCachedObject("PageMerger", this.m_pageMerger);
/*     */     }
/*     */ 
/* 187 */     this.m_publishDataDir = (LegacyDirectoryLocator.getAppDataDirectory() + "publish");
/* 188 */     this.m_maxParentsOnHistory = SharedObjects.getEnvironmentInt("MaxStaticPublishHistoryParentDirectories", this.m_maxParentsOnHistory);
/*     */ 
/* 190 */     this.m_targetDirs = new ArrayList();
/* 191 */     this.m_targets = new HashMap();
/* 192 */     this.m_ownedTargets = new HashMap();
/* 193 */     this.m_bindersToWrite = new ArrayList();
/* 194 */     this.m_timer = ((IdcTimer)cxt.getCachedObject("IdcTimer:publish"));
/* 195 */     this.m_publishState = ((WebPublishState)cxt.getCachedObject("WebPublishState"));
/*     */ 
/* 197 */     this.m_isSuperVerboseTrace = SharedObjects.getEnvValueAsBoolean("IsSuperStaticPublishVerbose", false);
/*     */   }
/*     */ 
/*     */   public DataResultSet prepare()
/*     */     throws DataException, ServiceException
/*     */   {
/* 203 */     if (this.m_timer != null)
/*     */     {
/* 205 */       this.m_timer.start("prepare");
/* 206 */       this.m_timer.start("sort table");
/*     */     }
/*     */ 
/* 211 */     DataResultSet fileSet = SharedObjects.getTable("PublishedStaticFiles");
/*     */ 
/* 213 */     if (this.m_isSuperVerboseTrace)
/*     */     {
/* 215 */       int num = fileSet.getNumRows();
/* 216 */       fileSet.setDataFormatOptions("text,rows=" + num);
/* 217 */       Report.trace(null, "StaticPublisher.prepare: before sort fileSet=" + fileSet.toString(), null);
/*     */     }
/*     */ 
/* 222 */     ResultSetJoin rsJoin = new ResultSetJoin(fileSet, this.m_workingBinder);
/* 223 */     rsJoin.m_canSearchSharedObjects = true;
/* 224 */     rsJoin.m_joinedTableFilterPattern = Pattern.compile("[Ll]ayout");
/* 225 */     rsJoin.m_joinedFieldFilterPattern = Pattern.compile("skinDir");
/* 226 */     rsJoin.m_joinedValueFilterPattern = Pattern.compile("^resources/layouts/");
/* 227 */     rsJoin.m_isJoinedFilterInclusive = true;
/* 228 */     rsJoin.join();
/* 229 */     fileSet = (DataResultSet)rsJoin.m_target;
/* 230 */     int index = ResultSetUtils.getIndexMustExist(fileSet, "loadOrder");
/* 231 */     WebPublishUtils.sortFileSet(fileSet, index);
/* 232 */     if (this.m_timer != null)
/*     */     {
/* 234 */       this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/*     */     }
/*     */ 
/* 237 */     if (this.m_isSuperVerboseTrace)
/*     */     {
/* 239 */       Report.trace(null, "StaticPublisher.prepare: after sort fileSet=" + fileSet, null);
/*     */     }
/*     */ 
/* 243 */     DataResultSet allFiles = null;
/* 244 */     if (this.m_timer != null)
/*     */     {
/* 246 */       this.m_timer.start("load previous directories");
/*     */     }
/* 248 */     this.m_originalDirs = retrievePublishedDirectories();
/* 249 */     if (this.m_timer != null)
/*     */     {
/* 251 */       this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/* 252 */       this.m_timer.start("compute file list");
/*     */     }
/* 254 */     doDryRun(fileSet);
/* 255 */     if (this.m_timer != null)
/*     */     {
/* 257 */       this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/* 258 */       this.m_timer.start("capture binders");
/*     */     }
/* 260 */     allFiles = prepareForCopyFiles();
/* 261 */     if (this.m_timer != null)
/*     */     {
/* 263 */       this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/* 264 */       this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/*     */     }
/* 266 */     return allFiles;
/*     */   }
/*     */ 
/*     */   public void publish() throws DataException, ServiceException
/*     */   {
/* 271 */     if (this.m_timer != null)
/*     */     {
/* 273 */       this.m_timer.start("publish");
/* 274 */       this.m_timer.start("copy files");
/*     */     }
/* 276 */     ProgressStateUtils.reportProgress(this.m_publishState.m_progress, null, "publish", 3, "starting static publish", null);
/*     */ 
/* 278 */     boolean success = false;
/*     */     try
/*     */     {
/* 281 */       copyFiles();
/* 282 */       if (this.m_timer != null)
/*     */       {
/* 284 */         this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/*     */       }
/* 286 */       if (this.m_originalDirs != null)
/*     */       {
/* 288 */         if (this.m_timer != null)
/*     */         {
/* 290 */           this.m_timer.start("delete old files");
/*     */         }
/* 292 */         deleteUnusedFiles(this.m_originalDirs);
/* 293 */         if (this.m_timer != null)
/*     */         {
/* 295 */           this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/*     */         }
/* 297 */         this.m_originalDirs = null;
/*     */       }
/* 299 */       if (this.m_timer != null)
/*     */       {
/* 301 */         this.m_timer.start("save listings");
/*     */       }
/* 303 */       writeCapturedListings();
/* 304 */       if (this.m_timer != null)
/*     */       {
/* 306 */         this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/* 307 */         this.m_timer.stop(this.m_publishState.m_timerFlags, new Object[0]);
/*     */       }
/* 309 */       success = true;
/*     */     }
/*     */     finally
/*     */     {
/* 313 */       if (success)
/*     */       {
/* 315 */         this.m_publishState.m_progress.setStateValue("latestState", "Success");
/* 316 */         ProgressStateUtils.reportProgress(this.m_publishState.m_progress, null, "publish", 3, "finished static publish", null);
/*     */       }
/*     */       else
/*     */       {
/* 321 */         this.m_publishState.m_progress.setStateValue("latestState", "Failed");
/* 322 */         ProgressStateUtils.reportProgress(this.m_publishState.m_progress, null, "publish", 3, "static publish failed", null);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/* 330 */     if (this.m_savedLocals == null)
/*     */       return;
/* 332 */     this.m_workingBinder.setLocalData(this.m_savedLocals);
/* 333 */     this.m_savedLocals = null;
/*     */   }
/*     */ 
/*     */   public boolean checkForPublish()
/*     */     throws ServiceException
/*     */   {
/* 346 */     boolean doCopy = true;
/* 347 */     if ((this.m_publishState != null) && (this.m_publishState.m_filteredStaticClasses != null))
/*     */     {
/* 349 */       String className = this.m_workingBinder.getLocal("class");
/* 350 */       doCopy = false;
/* 351 */       String[] filtered = this.m_publishState.m_filteredStaticClasses;
/* 352 */       for (int i = filtered.length - 1; i >= 0; --i)
/*     */       {
/* 354 */         if (!PublishedResourceUtils.classnameMatches(filtered[i], className))
/*     */           continue;
/* 356 */         doCopy = true;
/* 357 */         break;
/*     */       }
/*     */     }
/*     */ 
/* 361 */     this.m_workingBinder.putLocal("doCopy", (doCopy) ? "1" : "0");
/* 362 */     String publishScript = this.m_workingBinder.getLocal("doPublishScript");
/* 363 */     return WebPublishUtils.checkForPublish(publishScript, this.m_workingBinder, this.m_pageMerger);
/*     */   }
/*     */ 
/*     */   public void doDryRun(DataResultSet fileSet)
/*     */     throws ServiceException
/*     */   {
/* 369 */     DataBinder workingBinder = this.m_workingBinder;
/* 370 */     if (this.m_isSuperVerboseTrace)
/*     */     {
/* 372 */       Report.trace(null, "StaticPublisher.doDryRun: working binder=" + workingBinder.toString(), null);
/*     */     }
/*     */ 
/* 378 */     String weblayoutDir = this.m_publishState.m_weblayoutDirectory;
/* 379 */     workingBinder.putLocal("canDeleteDir", "0");
/* 380 */     manageTargetListing(weblayoutDir, workingBinder);
/*     */ 
/* 382 */     String publishDir = LegacyDirectoryLocator.getResourcesDirectory();
/* 383 */     for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*     */     {
/* 385 */       Properties props = fileSet.getCurrentRowProps();
/*     */ 
/* 387 */       if (this.m_isSuperVerboseTrace)
/*     */       {
/* 389 */         Report.trace(null, "StaticPublisher.doDryRun: BEFORE fileSet row=" + props, null);
/*     */       }
/*     */ 
/* 392 */       workingBinder.setLocalData(props);
/* 393 */       boolean isPublish = checkForPublish();
/* 394 */       if (!isPublish)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 399 */       String srcPath = workingBinder.getLocal("srcPath");
/* 400 */       String path = workingBinder.getLocal("path");
/* 401 */       String cmpDir = null;
/* 402 */       String cmpPathPrefix = null;
/*     */       try
/*     */       {
/* 405 */         srcPath = this.m_pageMerger.evaluateScript(srcPath);
/* 406 */         path = this.m_pageMerger.evaluateScript(path);
/* 407 */         cmpDir = this.m_pageMerger.evaluateScript("<$getComponentInfo(idcComponentName,\"ComponentDir\")$>");
/*     */ 
/* 409 */         cmpPathPrefix = this.m_pageMerger.evaluateScript("<$getComponentInfo(idcComponentName,\"StaticPublishPathPrefix\")$>");
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 414 */         String errMsg = LocaleUtils.encodeMessage("csStaticPublishPathScriptError", null);
/* 415 */         Report.error(null, errMsg, e);
/*     */       }
/*     */ 
/* 418 */       workingBinder.putLocal("srcPath", srcPath);
/* 419 */       workingBinder.putLocal("path", path);
/*     */ 
/* 421 */       if (this.m_isSuperVerboseTrace)
/*     */       {
/* 423 */         Report.trace(null, "StaticPublisher.doDryRun: AFTER srcPath=" + srcPath + "  path=" + path, null);
/*     */       }
/*     */ 
/* 428 */       String defaultSrcDir = publishDir;
/* 429 */       if ((cmpDir != null) && (cmpDir.length() > 0))
/*     */       {
/* 431 */         defaultSrcDir = cmpDir;
/*     */ 
/* 433 */         if ((cmpPathPrefix != null) && (cmpPathPrefix.length() > 0))
/*     */         {
/* 435 */           defaultSrcDir = defaultSrcDir + "/" + cmpPathPrefix;
/*     */         }
/*     */       }
/* 438 */       srcPath = FileUtils.getAbsolutePath(defaultSrcDir, srcPath);
/*     */ 
/* 440 */       int fixFlags = 12;
/* 441 */       path = FileUtils.fixDirectorySlashes(path, 12).toString();
/* 442 */       if (FileUtils.doesPathContainRelativeSegments(path))
/*     */       {
/* 444 */         IdcMessage msg = new IdcMessage("csStaticPublishTargetPathContainsRelativeSegments", new Object[] { path });
/* 445 */         Report.error("publish", null, msg);
/*     */       }
/*     */       else {
/* 448 */         if ((path == null) || (path.length() == 0))
/*     */         {
/* 450 */           path = weblayoutDir;
/*     */         }
/*     */         else
/*     */         {
/* 454 */           path = FileUtils.getAbsolutePath(weblayoutDir, path);
/*     */         }
/*     */ 
/* 458 */         captureFiles(srcPath, path, workingBinder);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void captureFiles(String srcPath, String path, DataBinder workingBinder) throws ServiceException {
/* 464 */     File src = new File(srcPath);
/* 465 */     if (src.exists())
/*     */     {
/* 467 */       if (src.isDirectory())
/*     */       {
/* 469 */         File target = new File(path);
/* 470 */         captureDirectory(src, target, workingBinder);
/*     */       }
/*     */       else
/*     */       {
/* 474 */         String targetDir = FileUtils.getDirectory(path);
/* 475 */         targetDir = FileUtils.directorySlashes(targetDir);
/* 476 */         if (path.endsWith("/"))
/*     */         {
/* 483 */           String filename = FileUtils.getName(srcPath);
/* 484 */           path = path + filename;
/*     */ 
/* 486 */           String relativePath = workingBinder.getLocal("path");
/* 487 */           relativePath = relativePath + filename;
/* 488 */           workingBinder.putLocal("path", relativePath);
/*     */         }
/* 490 */         Map list = manageTargetListing(targetDir, workingBinder);
/* 491 */         addTarget(targetDir, list, srcPath, path, workingBinder);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 496 */       String errMsg = LocaleUtils.encodeMessage("csMissingPublishSource", null, srcPath);
/* 497 */       Report.error("publish", errMsg, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void captureDirectory(File srcDir, File targetDir, DataBinder workingBinder) throws ServiceException
/*     */   {
/* 503 */     String[] files = srcDir.list();
/* 504 */     if (files == null)
/*     */     {
/* 506 */       return;
/*     */     }
/*     */ 
/* 509 */     String oldDir = FileUtils.directorySlashes(srcDir.getAbsolutePath());
/* 510 */     String newDir = FileUtils.directorySlashes(targetDir.getAbsolutePath());
/* 511 */     String savePath = FileUtils.directorySlashes(workingBinder.getLocal("path"));
/* 512 */     String saveSource = FileUtils.directorySlashes(workingBinder.getLocal("srcPath"));
/*     */ 
/* 514 */     Map list = manageTargetListing(newDir, workingBinder);
/* 515 */     for (int i = 0; i < files.length; ++i)
/*     */     {
/* 517 */       String filename = files[i];
/* 518 */       if (filename.equals(".svn"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 523 */       String oldPath = oldDir + filename;
/* 524 */       File oldFile = new File(oldPath);
/*     */ 
/* 526 */       String newPath = newDir + filename;
/* 527 */       File newFile = new File(newPath);
/*     */ 
/* 529 */       String copyPath = savePath + filename;
/* 530 */       workingBinder.putLocal("path", copyPath);
/* 531 */       String copySource = saveSource + filename;
/* 532 */       workingBinder.putLocal("srcPath", copySource);
/*     */ 
/* 534 */       if (oldFile.isDirectory())
/*     */       {
/* 536 */         captureDirectory(oldFile, newFile, workingBinder);
/*     */       }
/*     */       else
/*     */       {
/* 540 */         addTarget(newDir, list, oldPath, newPath, workingBinder);
/*     */       }
/*     */     }
/*     */ 
/* 544 */     workingBinder.putLocal("path", savePath);
/* 545 */     workingBinder.putLocal("srcPath", saveSource);
/*     */   }
/*     */ 
/*     */   public DataResultSet prepareForCopyFiles() throws ServiceException
/*     */   {
/* 550 */     DataResultSet allFiles = new DataResultSet(this.FILES_FIELDS);
/* 551 */     DataResultSet dirSet = new DataResultSet(new String[] { "dirName", "canDelete" });
/* 552 */     int size = this.m_targetDirs.size();
/* 553 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 555 */       String dir = (String)this.m_targetDirs.get(i);
/* 556 */       List row = dirSet.createEmptyRowAsList();
/* 557 */       row.set(0, dir);
/* 558 */       row.set(1, this.m_ownedTargets.get(dir.toLowerCase()));
/* 559 */       dirSet.addRowWithList(row);
/*     */ 
/* 561 */       String lookupDir = dir.toLowerCase();
/* 562 */       DataResultSet drset = new DataResultSet(this.FILES_FIELDS);
/* 563 */       Map files = (Map)this.m_targets.get(lookupDir);
/* 564 */       Set set = files.keySet();
/* 565 */       for (Iterator iter = set.iterator(); iter.hasNext(); )
/*     */       {
/* 567 */         String key = (String)iter.next();
/* 568 */         String[] info = (String[])(String[])files.get(key);
/*     */ 
/* 570 */         row = drset.createEmptyRowAsList();
/* 571 */         for (int j = 0; j < this.FILES_FIELDS.length; ++j)
/*     */         {
/* 573 */           row.set(j, info[j]);
/*     */         }
/* 575 */         drset.addRowWithList(row);
/* 576 */         allFiles.addRowWithList(row);
/*     */       }
/*     */ 
/* 579 */       DataBinder binder = new DataBinder();
/* 580 */       binder.putLocal("TargetDir", dir);
/* 581 */       binder.addResultSet("FileList", drset);
/*     */ 
/* 583 */       String dirHash = buildDirectoryHash(lookupDir);
/* 584 */       String pubDir = this.m_publishDataDir + "/" + dirHash;
/* 585 */       captureBinderToWrite(pubDir, "files.hda", binder);
/*     */     }
/*     */ 
/* 588 */     DataBinder data = new DataBinder();
/* 589 */     data.addResultSet("DirectoryList", dirSet);
/* 590 */     captureBinderToWrite(this.m_publishDataDir, "directories.hda", data);
/*     */ 
/* 592 */     return allFiles;
/*     */   }
/*     */ 
/*     */   public void copyFiles() throws ServiceException
/*     */   {
/* 597 */     int size = this.m_targetDirs.size();
/* 598 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 600 */       String dir = (String)this.m_targetDirs.get(i);
/*     */ 
/* 602 */       if (SystemUtils.m_verbose)
/*     */       {
/* 604 */         Report.debug("fileaccess", "copyFiles - checkOrCreateDirectory - " + dir, null);
/*     */       }
/* 606 */       FileUtils.checkOrCreateDirectory(dir, 10);
/* 607 */       if ((this.m_publishState != null) && (this.m_publishState.m_isAbort))
/*     */       {
/* 609 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */       }
/*     */ 
/* 612 */       String lookupDir = dir.toLowerCase();
/* 613 */       Map files = (Map)this.m_targets.get(lookupDir);
/* 614 */       Set set = files.keySet();
/* 615 */       for (Iterator iter = set.iterator(); iter.hasNext(); )
/*     */       {
/* 617 */         String key = (String)iter.next();
/* 618 */         String[] info = (String[])(String[])files.get(key);
/* 619 */         boolean doCopy = StringUtils.convertToBool(info[5], true);
/* 620 */         if (doCopy)
/*     */         {
/* 622 */           FileUtils.copyFile(info[6], info[7]);
/* 623 */           if ((this.m_publishState != null) && (this.m_publishState.m_isAbort))
/*     */           {
/* 625 */             throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */           }
/*     */         }
/*     */       }
/* 629 */       String dirHash = buildDirectoryHash(lookupDir);
/* 630 */       String pubDir = this.m_publishDataDir + "/" + dirHash;
/* 631 */       FileUtils.checkOrCreateDirectory(pubDir, 1);
/* 632 */       if ((this.m_publishState == null) || (!this.m_publishState.m_isAbort))
/*     */         continue;
/* 634 */       throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void captureBinderToWrite(String dir, String fileName, DataBinder binder)
/*     */   {
/* 647 */     binder.putLocal("StoredDir", dir);
/* 648 */     binder.putLocal("StoredFileName", fileName);
/* 649 */     this.m_bindersToWrite.add(binder);
/*     */   }
/*     */ 
/*     */   public void writeCapturedListings()
/*     */     throws ServiceException
/*     */   {
/* 658 */     for (DataBinder binder : this.m_bindersToWrite)
/*     */     {
/* 660 */       String dir = binder.getLocal("StoredDir");
/* 661 */       String fileName = binder.getLocal("StoredFileName");
/* 662 */       ResourceUtils.serializeDataBinder(dir, fileName, binder, true, false);
/* 663 */       if ((this.m_publishState != null) && (this.m_publishState.m_isAbort))
/*     */       {
/* 665 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteUnusedFiles(DataResultSet dirSet)
/*     */     throws ServiceException
/*     */   {
/* 679 */     List checkDeleteDir = new ArrayList();
/*     */ 
/* 682 */     List deleteList = new ArrayList();
/* 683 */     Map oldDirs = new HashMap();
/* 684 */     for (SimpleParameters params : dirSet.getSimpleParametersIterable())
/*     */     {
/* 686 */       String dirName = params.get("dirName");
/* 687 */       int deleteFlag = NumberUtils.parseInteger(params.get("canDelete"), 0);
/*     */ 
/* 689 */       String lookupKey = dirName.toLowerCase();
/* 690 */       Map nFiles = (Map)this.m_targets.get(lookupKey);
/* 691 */       boolean didWePublishFilesThisTime = nFiles != null;
/* 692 */       if ((!didWePublishFilesThisTime) && (deleteFlag == 1))
/*     */       {
/* 694 */         deleteList.add(dirName);
/*     */       }
/*     */ 
/* 699 */       DataBinder data = (DataBinder)oldDirs.get(dirName);
/* 700 */       if (data == null)
/*     */       {
/* 703 */         data = new DataBinder();
/* 704 */         String dirHash = buildDirectoryHash(lookupKey);
/* 705 */         ResourceUtils.serializeDataBinder(this.m_publishDataDir + "/" + dirHash, "files.hda", data, false, false);
/*     */ 
/* 707 */         if ((this.m_publishState != null) && (this.m_publishState.m_isAbort))
/*     */         {
/* 709 */           throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */         }
/* 711 */         oldDirs.put(lookupKey, data);
/*     */       }
/*     */ 
/* 714 */       DataResultSet fileSet = (DataResultSet)data.getResultSet("FileList");
/* 715 */       if (fileSet != null)
/*     */       {
/* 719 */         for (fileSet.first(); fileSet.isRowPresent(); fileSet.next())
/*     */         {
/* 721 */           String[] nFile = null;
/* 722 */           String pathName = fileSet.getStringValue(0);
/* 723 */           String fileName = FileUtils.getName(pathName);
/* 724 */           String path = FileUtils.getAbsolutePath(dirName, fileName);
/* 725 */           lookupKey = path.toLowerCase();
/* 726 */           if (null != nFiles)
/*     */           {
/* 728 */             nFile = (String[])(String[])nFiles.get(lookupKey);
/*     */           }
/* 730 */           if (nFile != null) {
/*     */             continue;
/*     */           }
/* 733 */           FileUtils.deleteFile(path);
/* 734 */           if ((this.m_publishState == null) || (!this.m_publishState.m_isAbort))
/*     */             continue;
/* 736 */           throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */         }
/*     */ 
/* 742 */         if ((!didWePublishFilesThisTime) && (deleteFlag == 2))
/*     */         {
/* 744 */           checkDeleteDir.add(dirName);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 749 */     cleanUpListings(deleteList, !SharedObjects.getEnvValueAsBoolean("DeletePublishedOwnedDirectoriesAlways", false));
/* 750 */     cleanUpListings(checkDeleteDir, true);
/*     */   }
/*     */ 
/*     */   public DataResultSet retrievePublishedDirectories() throws ServiceException
/*     */   {
/* 755 */     DataBinder data = new DataBinder();
/* 756 */     ResourceUtils.serializeDataBinder(this.m_publishDataDir, "directories.hda", data, false, false);
/* 757 */     DataResultSet dirSet = (DataResultSet)data.getResultSet("DirectoryList");
/* 758 */     if (dirSet != null)
/*     */     {
/* 762 */       ResultSetTreeSort resultSetSort = new ResultSetTreeSort(dirSet, 0, false);
/* 763 */       resultSetSort.determineFieldType("string");
/* 764 */       resultSetSort.determineIsAscending("desc");
/* 765 */       resultSetSort.sort();
/*     */     }
/* 767 */     return dirSet;
/*     */   }
/*     */ 
/*     */   public void cleanUpListings(List delList, boolean isEmptyCheck)
/*     */   {
/*     */     try
/*     */     {
/* 776 */       int size = delList.size();
/* 777 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 779 */         String key = (String)delList.get(i);
/*     */ 
/* 781 */         File dir = new File(key);
/* 782 */         String[] files = dir.list();
/* 783 */         boolean isEmpty = (files == null) || (files.length == 0);
/*     */ 
/* 785 */         String lookup = key.toLowerCase();
/* 786 */         String hash = buildDirectoryHash(lookup);
/* 787 */         File hashDir = new File(this.m_publishDataDir + "/" + hash);
/* 788 */         FileUtils.deleteDirectory(hashDir, true);
/* 789 */         if ((this.m_publishState != null) && (this.m_publishState.m_isAbort))
/*     */         {
/* 791 */           throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */         }
/*     */ 
/* 795 */         boolean doDelete = isEmpty;
/* 796 */         if (!isEmpty)
/*     */         {
/* 799 */           String msg = "csPublishUnexpectedFilesInDirectoryNotDeleting";
/* 800 */           if (!isEmptyCheck)
/*     */           {
/* 802 */             msg = "csPublishUnexpectedFilesInDirectoryDeletingAnyway";
/* 803 */             doDelete = true;
/*     */           }
/* 805 */           Report.error("publish", null, msg, new Object[] { key, files });
/*     */         }
/* 807 */         if (doDelete)
/*     */         {
/* 809 */           FileUtils.deleteDirectory(dir, true);
/*     */         }
/* 811 */         if ((this.m_publishState == null) || (!this.m_publishState.m_isAbort))
/*     */           continue;
/* 813 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 820 */       Report.trace("publish", "Unable to cleanup static publish directory.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Map manageTargetListing(String targetDir, DataBinder workingBinder)
/*     */     throws ServiceException
/*     */   {
/* 827 */     String lookupKey = targetDir.toLowerCase();
/* 828 */     String str = workingBinder.getLocal("canDeleteDir");
/* 829 */     boolean canDelete = StringUtils.convertToBool(str, false);
/* 830 */     updateOwnership(lookupKey, canDelete, true);
/*     */ 
/* 832 */     Map list = (Map)this.m_targets.get(lookupKey);
/* 833 */     if (list == null)
/*     */     {
/* 835 */       this.m_targetDirs.add(targetDir);
/* 836 */       list = new HashMap();
/* 837 */       this.m_targets.put(lookupKey, list);
/*     */ 
/* 840 */       int nmaxdirs = this.m_maxParentsOnHistory;
/* 841 */       int i = 0;
/* 842 */       String parent = FileUtils.getParent(targetDir);
/* 843 */       for (; (parent != null) && (i < nmaxdirs); ++i)
/*     */       {
/* 845 */         parent = FileUtils.directorySlashes(parent);
/* 846 */         if (!checkRootDirectory(parent)) {
/*     */           break;
/*     */         }
/*     */ 
/* 850 */         String key = parent.toLowerCase();
/* 851 */         Map l = (Map)this.m_targets.get(key);
/* 852 */         if (l == null)
/*     */         {
/* 854 */           this.m_targetDirs.add(parent);
/* 855 */           l = new HashMap();
/* 856 */           this.m_targets.put(key, l);
/*     */         }
/*     */ 
/* 859 */         updateOwnership(key, canDelete, false);
/*     */ 
/* 843 */         parent = FileUtils.getParent(parent);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 862 */     return list;
/*     */   }
/*     */ 
/*     */   public boolean checkRootDirectory(String dir)
/*     */   {
/* 867 */     int startNonRootChar = 0;
/* 868 */     boolean foundDirChar = false;
/* 869 */     for (int i = 0; i < dir.length(); ++i)
/*     */     {
/* 871 */       char ch = dir.charAt(i);
/* 872 */       boolean isDirChar = (ch == '/') || (ch == '\\') || (ch == ':');
/* 873 */       if (foundDirChar)
/*     */       {
/* 875 */         if (isDirChar)
/*     */           continue;
/* 877 */         startNonRootChar = i;
/* 878 */         break;
/*     */       }
/*     */ 
/* 883 */       if (!isDirChar)
/*     */         continue;
/* 885 */       foundDirChar = true;
/*     */     }
/*     */ 
/* 891 */     if (startNonRootChar <= 0)
/*     */     {
/* 893 */       return false;
/*     */     }
/* 895 */     boolean foundSubDirChar = false;
/* 896 */     boolean hasSubDir = false;
/* 897 */     for (int i = startNonRootChar; i < dir.length(); ++i)
/*     */     {
/* 899 */       char ch = dir.charAt(i);
/* 900 */       boolean isDirChar = (ch == '/') || (ch == '\\');
/* 901 */       if (foundSubDirChar)
/*     */       {
/* 903 */         if (isDirChar)
/*     */           continue;
/* 905 */         hasSubDir = true;
/* 906 */         break;
/*     */       }
/*     */ 
/* 911 */       if (!isDirChar)
/*     */         continue;
/* 913 */       foundSubDirChar = true;
/*     */     }
/*     */ 
/* 918 */     return hasSubDir;
/*     */   }
/*     */ 
/*     */   public void updateOwnership(String lookupKey, boolean canDelete, boolean isOwner)
/*     */   {
/* 923 */     Object obj = this.m_ownedTargets.get(lookupKey);
/* 924 */     if (obj != null)
/*     */     {
/* 926 */       if (canDelete)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 931 */       this.m_ownedTargets.put(lookupKey, "0");
/*     */     }
/*     */     else
/*     */     {
/*     */       String str;
/*     */       String str;
/* 937 */       if (canDelete)
/*     */       {
/*     */         String str;
/* 939 */         if (isOwner)
/*     */         {
/* 941 */           str = "1";
/*     */         }
/*     */         else
/*     */         {
/* 945 */           str = "2";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 950 */         str = "0";
/*     */       }
/* 952 */       this.m_ownedTargets.put(lookupKey, str);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String buildDirectoryHash(String dir)
/*     */   {
/* 958 */     String prefix = "";
/* 959 */     int index = dir.lastIndexOf(47);
/* 960 */     if ((index == dir.length() - 1) && 
/* 962 */       (index > 0))
/*     */     {
/* 964 */       dir = dir.substring(0, index);
/* 965 */       index = dir.lastIndexOf(47);
/*     */     }
/*     */ 
/* 968 */     if (index >= 0)
/*     */     {
/* 970 */       prefix = dir.substring(index + 1);
/*     */     }
/* 972 */     else if (dir.indexOf(":") < 0)
/*     */     {
/* 974 */       prefix = dir;
/*     */     }
/* 976 */     return prefix + dir.hashCode();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 982 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.StaticPublisher
 * JD-Core Version:    0.5.4
 */