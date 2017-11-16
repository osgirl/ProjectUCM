/*      */ package intradoc.server.publish;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcTimer;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LoggingUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.PageHandlerService;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.ServiceData;
/*      */ import intradoc.shared.LocaleLoader;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.ProgressState;
/*      */ import intradoc.shared.ProgressStateUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class WebPublisher
/*      */   implements Runnable
/*      */ {
/*      */   public WebPublishState m_state;
/*      */   public Thread m_thread;
/*      */ 
/*      */   public void initializeState(Workspace ws, ExecutionContext cxt, int flags)
/*      */     throws ServiceException
/*      */   {
/*  175 */     boolean isStartup = (flags & 0x400) != 0;
/*      */ 
/*  177 */     this.m_state = new WebPublishState();
/*  178 */     this.m_state.m_workspace = ws;
/*      */ 
/*  180 */     ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/*  181 */     context.setParentContext(cxt);
/*  182 */     context.setCachedObject("WebPublishState", this.m_state);
/*      */ 
/*  184 */     Properties env = new IdcProperties(SharedObjects.getSecureEnvironment());
/*  185 */     this.m_state.m_publishBinder = new DataBinder(env);
/*  186 */     context.setCachedObject("DataBinder", this.m_state.m_publishBinder);
/*  187 */     this.m_state.m_publishMerger = new PageMerger(this.m_state.m_publishBinder, context);
/*  188 */     context.setCachedObject("PageMerger", this.m_state.m_publishMerger);
/*  189 */     ProgressState progress = new ProgressState();
/*  190 */     progress.init("WebPublish");
/*  191 */     this.m_state.m_progress = progress;
/*  192 */     this.m_state.m_context = context;
/*  193 */     this.m_state.m_CFU = ConfigFileUtilities.getOrCreateConfigFileUtilitiesForExecutionContext(cxt);
/*      */ 
/*  195 */     if (SystemUtils.isActiveTrace("publish"))
/*      */     {
/*  197 */       this.m_state.m_doTrace = true;
/*  198 */       this.m_state.m_timer = new IdcTimer("publish");
/*  199 */       this.m_state.m_timer.m_levelDelimiter = " -> ";
/*  200 */       this.m_state.m_timer.start((isStartup) ? ": startup publish" : ": publish");
/*  201 */       cxt.setCachedObject("IdcTimer:publish", this.m_state.m_timer);
/*  202 */       this.m_state.m_timerFlags = 3072;
/*      */     }
/*  204 */     this.m_state.m_weblayoutDirectory = SharedObjects.getEnvironmentValue("WeblayoutDir");
/*  205 */     this.m_state.m_publishDirectory = (LegacyDirectoryLocator.getAppDataDirectory() + "publish");
/*  206 */     FileUtils.checkOrCreateDirectory(this.m_state.m_publishDirectory, 0, 1);
/*      */ 
/*  208 */     boolean doStaticPublish = false;
/*      */     try
/*      */     {
/*  212 */       this.m_state.m_startupBinder = WebPublishUtils.loadPublishAtStartupBinder(this.m_state.m_CFU);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  216 */       Report.trace("publish", "unable to load publish-at-startup binder", e);
/*  217 */       this.m_state.m_startupBinder = new DataBinder();
/*  218 */       doStaticPublish = true;
/*      */     }
/*  220 */     if (isStartup)
/*      */     {
/*  222 */       if (SystemUtils.m_verbose)
/*      */       {
/*  224 */         Report.trace("publish", "determining whether to perform startup publishing", null);
/*      */       }
/*  226 */       flags |= 1;
/*  227 */       ResultSet publishClasses = this.m_state.m_startupBinder.getResultSet("PublishClasses");
/*  228 */       if (publishClasses != null)
/*      */       {
/*  230 */         if (!publishClasses.isEmpty())
/*      */         {
/*      */           try
/*      */           {
/*  235 */             this.m_state.m_filteredStaticClasses = createSortedClassesArrayFromResultSet(publishClasses);
/*  236 */             if (SystemUtils.m_verbose)
/*      */             {
/*  238 */               Report.trace("publish", "triggering static publish, PublishClasses was defined", null);
/*      */             }
/*  240 */             doStaticPublish = true;
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  244 */             Report.trace("publish", "unable to determine filtered static classes", e);
/*      */           }
/*      */         }
/*      */ 
/*  248 */         this.m_state.m_startupBinder.removeResultSet("PublishClasses");
/*      */       }
/*  250 */       if (!doStaticPublish)
/*      */       {
/*  253 */         String publishEverything = this.m_state.m_startupBinder.getLocal("PublishEverything");
/*  254 */         doStaticPublish = StringUtils.convertToBool(publishEverything, false);
/*  255 */         if ((doStaticPublish) && (SystemUtils.m_verbose))
/*      */         {
/*  257 */           Report.trace("publish", "triggering static publish, PublishEverything is true", null);
/*      */         }
/*  259 */         if (!doStaticPublish)
/*      */         {
/*  262 */           doStaticPublish = SharedObjects.getEnvValueAsBoolean("PublishWebFilesOnStartup", false);
/*  263 */           if ((doStaticPublish) && (SystemUtils.m_verbose))
/*      */           {
/*  265 */             Report.trace("publish", "triggering static publish, PublishWebFilesOnStartup is true", null);
/*      */           }
/*  267 */           if (!doStaticPublish)
/*      */           {
/*  269 */             doStaticPublish = checkIfComponentsRequireStaticPublish();
/*  270 */             if ((doStaticPublish) && (SystemUtils.m_verbose))
/*      */             {
/*  272 */               Report.trace("publish", "triggering static publish, required by components", null);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*  277 */       if ((StringUtils.convertToBool(this.m_state.m_startupBinder.getLocal("PublishStrings"), false)) || (checkIfEnabledComponentsChanged()) || (SharedObjects.getEnvValueAsBoolean("RebuildHomePage", false)))
/*      */       {
/*  280 */         flags |= 8;
/*  281 */         if (SystemUtils.m_verbose)
/*      */         {
/*  283 */           Report.trace("publish", "triggering generated publish", null);
/*      */         }
/*      */       }
/*      */ 
/*  287 */       this.m_state.m_startupBinder.putLocal("PublishEverything", "false");
/*  288 */       this.m_state.m_startupBinder.putLocal("PublishStrings", "false");
/*      */     }
/*  290 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*      */     {
/*  292 */       flags |= 8;
/*      */     }
/*  294 */     if (doStaticPublish)
/*      */     {
/*  296 */       flags |= 16;
/*      */     }
/*  298 */     if ((flags & 0x8) != 0)
/*      */     {
/*  300 */       flags |= 1;
/*      */     }
/*  302 */     if ((flags & 0x10) != 0)
/*      */     {
/*  304 */       flags |= 9;
/*      */     }
/*  306 */     this.m_state.m_flags = flags;
/*      */ 
/*  308 */     this.m_state.m_doBundling = SharedObjects.getEnvValueAsBoolean("BundlePublishedFiles", true);
/*      */   }
/*      */ 
/*      */   public void prepareForPublishing()
/*      */     throws DataException, ServiceException
/*      */   {
/*  322 */     IdcTimer timer = this.m_state.m_timer;
/*  323 */     if (timer != null)
/*      */     {
/*  325 */       timer.start("prepare for publish");
/*      */     }
/*  327 */     lock();
/*      */     try
/*      */     {
/*  330 */       doFilter("prepareForPublish");
/*  331 */       this.m_state.m_publishedResources = new HashMap();
/*  332 */       this.m_state.m_publishedClasses = new HashMap();
/*  333 */       doRefreshLayoutLists();
/*      */ 
/*  335 */       if ((this.m_state.m_flags & 0x10) == 0)
/*      */       {
/*  337 */         loadFilteredStaticPublishedFiles();
/*      */       }
/*  339 */       if (this.m_state.m_staticFiles == null)
/*      */       {
/*  341 */         this.m_state.m_flags |= 16;
/*      */       }
/*  343 */       if ((this.m_state.m_flags & 0x10) != 0)
/*      */       {
/*  345 */         doFilter("prepareStaticPublish");
/*      */       }
/*  347 */       if ((this.m_state.m_flags & 0x10) != 0)
/*      */       {
/*  349 */         prepareStaticPublish();
/*      */       }
/*      */ 
/*  352 */       this.m_state.m_dynamicPublisher = new DynamicPublisher(this.m_state);
/*  353 */       this.m_state.m_dynamicPublisher.computePublishedBundles();
/*      */ 
/*  355 */       computeStaticPublishedResources();
/*  356 */       addLanguageFilesToDynamicPublish();
/*      */ 
/*  358 */       this.m_state.m_dynamicPublisher.loadPreviouslyPublishedDynamicFiles();
/*  359 */       if ((this.m_state.m_flags & 0x1) != 0)
/*      */       {
/*  361 */         doFilter("prepareDynamicPublish");
/*      */       }
/*  363 */       if ((this.m_state.m_flags & 0x1) != 0)
/*      */       {
/*  365 */         this.m_state.m_dynamicPublisher.prepare();
/*      */       }
/*      */ 
/*  368 */       this.m_state.m_webFeaturesPublisher = new WebFeaturesPublisher(this.m_state);
/*  369 */       doFilter("computeWebFeatures");
/*  370 */       this.m_state.m_webFeaturesPublisher.computeWebFeatures();
/*  371 */       computeSortedPublishedContainers();
/*  372 */       PublishedResourceUtils.m_features = this.m_state.m_features;
/*  373 */       PublishedResourceUtils.m_sortedContainers = this.m_state.m_sortedContainers;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  377 */       finish();
/*  378 */       throw e;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  382 */       finish();
/*  383 */       throw e;
/*      */     }
/*  385 */     if (timer == null)
/*      */       return;
/*  387 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void lock()
/*      */     throws ServiceException
/*      */   {
/*  399 */     IdcStringBuilder agentBuilder = new IdcStringBuilder("WebPublisher");
/*  400 */     String prefix = LoggingUtils.getLogFileMsgPrefix();
/*  401 */     if (prefix != null)
/*      */     {
/*  403 */       agentBuilder.append(" for ");
/*  404 */       agentBuilder.append(prefix);
/*      */     }
/*  406 */     String agent = agentBuilder.toString();
/*  407 */     boolean isDevelopmentEnvironment = SystemUtils.m_isDevelopmentEnvironment;
/*  408 */     int timeout = (isDevelopmentEnvironment) ? 5000 : 300000;
/*  409 */     timeout = SharedObjects.getEnvironmentInt("StaticPublishLockTimeout", timeout);
/*  410 */     timeout = SharedObjects.getEnvironmentInt("PublishLockTimeout", timeout);
/*  411 */     if (!FileUtils.reserveLongTermLock(this.m_state.m_publishDirectory, "publish", agent, timeout, false))
/*      */     {
/*  413 */       IdcMessage msg = new IdcMessage("syErrorCreatingLongTermLock", new Object[] { this.m_state.m_publishDirectory, "publish" });
/*  414 */       if (!isDevelopmentEnvironment)
/*      */       {
/*  416 */         throw new ServiceException(null, msg);
/*      */       }
/*  418 */       Report.trace("publish", null, msg);
/*      */ 
/*  420 */       FileUtils.releaseLongTermLock(this.m_state.m_publishDirectory, "publish", agent);
/*      */     }
/*      */     else
/*      */     {
/*  424 */       this.m_state.m_lockAgent = agent;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void doFilter(String filterName)
/*      */     throws DataException, ServiceException
/*      */   {
/*  465 */     IdcTimer timer = this.m_state.m_timer;
/*  466 */     if (timer != null)
/*      */     {
/*  468 */       timer.start(filterName + " filter");
/*      */     }
/*  470 */     PluginFilters.filter(filterName, this.m_state.m_workspace, this.m_state.m_publishBinder, this.m_state.m_context);
/*  471 */     if (timer != null)
/*      */     {
/*  473 */       timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */     }
/*  475 */     if (!this.m_state.m_isAbort)
/*      */       return;
/*  477 */     throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */   }
/*      */ 
/*      */   public void startPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/*  491 */     if ((this.m_state.m_flags & 0x100) != 0)
/*      */     {
/*  493 */       this.m_thread = new Thread(this, "WebPublisher");
/*  494 */       this.m_thread.setDaemon(true);
/*  495 */       this.m_thread.start();
/*      */     }
/*      */     else
/*      */     {
/*  499 */       doPublish();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void doPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/*  510 */     IdcTimer timer = this.m_state.m_timer;
/*  511 */     if (timer != null)
/*      */     {
/*  513 */       timer.start("publish");
/*      */     }
/*      */ 
/*  516 */     ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "publish", 4, "starting publishing", null);
/*      */     try
/*      */     {
/*  520 */       if ((this.m_state.m_flags & 0x10) != 0)
/*      */       {
/*  522 */         doFilter("doStaticPublish");
/*      */       }
/*  524 */       if ((this.m_state.m_flags & 0x10) != 0)
/*      */       {
/*  526 */         doStaticPublish();
/*      */       }
/*      */ 
/*  529 */       if ((this.m_state.m_flags & 0x8) != 0)
/*      */       {
/*  531 */         doFilter("doGeneratedPublish");
/*      */       }
/*  533 */       if ((this.m_state.m_flags & 0x8) != 0)
/*      */       {
/*  535 */         setupLanguageStringsForDynamicPublish();
/*  536 */         publishStaticPortal();
/*      */       }
/*  538 */       WebPublishUtils.savePublishAtStartupBinder(this.m_state.m_CFU, this.m_state.m_startupBinder);
/*  539 */       if (this.m_state.m_isAbort)
/*      */       {
/*  541 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*  543 */       if ((this.m_state.m_flags & 0x1) != 0)
/*      */       {
/*  545 */         doFilter("doDynamicPublish");
/*      */       }
/*  547 */       if ((this.m_state.m_flags & 0x1) != 0)
/*      */       {
/*  549 */         this.m_state.m_dynamicPublisher.doPublish();
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  554 */       finish();
/*  555 */       if (timer != null)
/*      */       {
/*  557 */         timer.resetToLevelByName("publish");
/*      */       }
/*  559 */       doFilter("finishPublish");
/*  560 */       if (this.m_state.m_isAbort)
/*      */       {
/*  562 */         ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "publish", 2, "publish failed", null);
/*      */       }
/*      */       else
/*      */       {
/*  567 */         ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "publish", 2, "publish finished", null);
/*      */       }
/*      */     }
/*      */ 
/*  571 */     if ((timer == null) || (this.m_state.m_isAbort))
/*      */       return;
/*  573 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*  574 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void finish()
/*      */   {
/*  583 */     if (this.m_state.m_staticPublisher != null)
/*      */     {
/*  585 */       this.m_state.m_staticPublisher.cleanup();
/*  586 */       this.m_state.m_staticPublisher = null;
/*      */     }
/*  588 */     if (this.m_state.m_lockAgent != null)
/*      */     {
/*  590 */       FileUtils.releaseLongTermLock(this.m_state.m_publishDirectory, "publish", this.m_state.m_lockAgent);
/*  591 */       this.m_state.m_lockAgent = null;
/*      */     }
/*  593 */     if (this.m_state.m_publishMerger == null)
/*      */       return;
/*  595 */     this.m_state.m_publishMerger.releaseAllTemporary();
/*  596 */     this.m_state.m_publishMerger = null;
/*      */   }
/*      */ 
/*      */   public boolean abort(long timeoutInMillis)
/*      */   {
/*  609 */     this.m_state.m_isAbort = true;
/*      */ 
/*  611 */     while ((thread = this.m_thread) != null)
/*      */     {
/*      */       Thread thread;
/*      */       try {
/*  615 */         thread.join(timeoutInMillis);
/*  616 */         return true;
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  620 */         if (SystemUtils.m_verbose)
/*      */         {
/*  622 */           Report.trace("publish", "waiting for publishing thread", ignore);
/*      */         }
/*      */       }
/*      */     }
/*  626 */     return false;
/*      */   }
/*      */ 
/*      */   public void run()
/*      */   {
/*      */     try
/*      */     {
/*  635 */       doPublish();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  639 */       Report.error("publish", "!csUnableToPublishAtStartup", e);
/*      */     }
/*  641 */     this.m_thread = null;
/*      */   }
/*      */ 
/*      */   protected void prepareStaticPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/*  654 */     IdcTimer timer = this.m_state.m_timer;
/*  655 */     if (timer != null)
/*      */     {
/*  657 */       timer.start("static");
/*      */     }
/*  659 */     this.m_state.m_staticPublisher = new StaticPublisher();
/*  660 */     this.m_state.m_staticPublisher.init(this.m_state.m_context);
/*      */     try
/*      */     {
/*  664 */       DataResultSet allFiles = this.m_state.m_staticPublisher.prepare();
/*  665 */       allFiles.renameField("file", "path");
/*  666 */       allFiles.renameField("source", "src");
/*      */ 
/*  668 */       int index = ResultSetUtils.getIndexMustExist(allFiles, "path");
/*  669 */       ResultSetFilter filter = new PublishedWebResourceResultSetFilter(index);
/*  670 */       DataResultSet filteredFiles = new DataResultSet();
/*  671 */       filteredFiles.copyFiltered(allFiles, "path", filter);
/*  672 */       this.m_state.m_staticFiles = filteredFiles;
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  676 */       this.m_state.m_staticPublisher.cleanup();
/*  677 */       throw e;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  681 */       this.m_state.m_staticPublisher.cleanup();
/*  682 */       throw e;
/*      */     }
/*  684 */     if (timer == null)
/*      */       return;
/*  686 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void doStaticPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/*  698 */     IdcTimer timer = this.m_state.m_timer;
/*  699 */     if (timer != null)
/*      */     {
/*  701 */       timer.start("static");
/*      */     }
/*      */     try
/*      */     {
/*  705 */       this.m_state.m_staticPublisher.publish();
/*      */     }
/*      */     finally
/*      */     {
/*  709 */       this.m_state.m_staticPublisher.cleanup();
/*  710 */       this.m_state.m_staticPublisher = null;
/*      */     }
/*      */     try
/*      */     {
/*  714 */       DataBinder binder = new DataBinder();
/*  715 */       this.m_state.m_CFU.readDataBinderFromName(PublishedResourceUtils.STATIC_FILENAME, binder, null);
/*  716 */       if (this.m_state.m_isAbort)
/*      */       {
/*  718 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*  720 */       binder.addResultSet("FilteredStaticWebResources", this.m_state.m_staticFiles);
/*  721 */       this.m_state.m_CFU.writeDataBinderToName(PublishedResourceUtils.STATIC_FILENAME, binder, null);
/*  722 */       if (this.m_state.m_isAbort)
/*      */       {
/*  724 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  729 */       this.m_state.m_staticPublisher.cleanup();
/*  730 */       throw new ServiceException(e);
/*      */     }
/*  732 */     doExplicitCodedPublishing();
/*  733 */     if (timer == null)
/*      */       return;
/*  735 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void doExplicitCodedPublishing()
/*      */     throws ServiceException
/*      */   {
/*  752 */     String instanceWeblayoutDir = this.m_state.m_weblayoutDirectory;
/*  753 */     instanceWeblayoutDir = FileUtils.directorySlashes(instanceWeblayoutDir);
/*      */ 
/*  757 */     String homeWeblayoutCommonDir = SharedObjects.getEnvironmentValue("HomeWeblayoutCommonDir");
/*      */ 
/*  759 */     if (homeWeblayoutCommonDir == null)
/*      */     {
/*  761 */       String idcHomeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*  762 */       if ((idcHomeDir == null) || (idcHomeDir.length() == 0))
/*      */       {
/*  764 */         return;
/*      */       }
/*  766 */       homeWeblayoutCommonDir = FileUtils.getAbsolutePath(idcHomeDir, "weblayout/common");
/*      */     }
/*      */ 
/*  769 */     File homeCommonDir = new File(homeWeblayoutCommonDir);
/*  770 */     File instanceCommonDir = new File(instanceWeblayoutDir, "common");
/*  771 */     if (!homeCommonDir.equals(instanceCommonDir))
/*      */     {
/*      */       try
/*      */       {
/*  776 */         FileUtils.copyDirectoryWithFlags(homeCommonDir, instanceCommonDir, 2, null, 18);
/*      */ 
/*  778 */         if (this.m_state.m_isAbort)
/*      */         {
/*  780 */           throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  785 */         Report.trace("error", e, "csPublishCouldNotCopyClientDotZip", new Object[0]);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  792 */     String homeWeblayoutClientZipPath = SharedObjects.getEnvironmentValue("HomeWeblayoutClientZipPath");
/*  793 */     if (homeWeblayoutClientZipPath == null) {
/*      */       return;
/*      */     }
/*      */ 
/*  797 */     String instanceWeblayoutClientZipPath = instanceWeblayoutDir + "common/idcapplet.jar";
/*  798 */     File homeClientZip = new File(homeWeblayoutClientZipPath);
/*  799 */     File instanceClientZip = new File(instanceWeblayoutClientZipPath);
/*  800 */     if (homeClientZip.equals(instanceClientZip))
/*      */       return;
/*  802 */     long homeLen = homeClientZip.length();
/*  803 */     long instanceLen = instanceClientZip.length();
/*  804 */     if ((homeLen <= 0L) || (homeLen == instanceLen)) {
/*      */       return;
/*      */     }
/*      */     try
/*      */     {
/*  809 */       FileUtils.copyFile(homeWeblayoutClientZipPath, instanceWeblayoutClientZipPath);
/*  810 */       if (this.m_state.m_isAbort)
/*      */       {
/*  812 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  817 */       Report.trace("error", e, "csPublishCouldNotCopyClientDotZip", new Object[0]);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkIfComponentsRequireStaticPublish()
/*      */   {
/*  829 */     DataBinder binder = this.m_state.m_startupBinder;
/*  830 */     String[] staticPublishingComponentsFieldNames = { "componentName", "componentVersion" };
/*  831 */     DataResultSet staticPublishingComponents = new DataResultSet(staticPublishingComponentsFieldNames);
/*  832 */     FieldInfo fi = new FieldInfo();
/*  833 */     Map componentsRequiringStaticPublishing = new HashMap();
/*  834 */     Map componentBinders = ComponentLoader.m_components;
/*  835 */     int numComponents = componentBinders.size();
/*  836 */     String[] componentNames = new String[numComponents];
/*  837 */     componentNames = (String[])componentBinders.keySet().toArray(componentNames);
/*  838 */     Arrays.sort(componentNames);
/*  839 */     for (int i = 0; i < numComponents; ++i)
/*      */     {
/*  841 */       String componentName = componentNames[i];
/*  842 */       DataBinder componentBinder = (DataBinder)componentBinders.get(componentName);
/*  843 */       String componentVersion = componentBinder.getAllowMissing("version");
/*  844 */       if (componentVersion == null)
/*      */       {
/*  846 */         componentVersion = "";
/*      */       }
/*      */ 
/*  849 */       if (!StringUtils.convertToBool(componentBinder.getAllowMissing("hasPublishedStaticFiles"), false))
/*      */       {
/*  851 */         DataResultSet mergeRules = (DataResultSet)componentBinder.getResultSet("MergeRules");
/*  852 */         if (null == mergeRules) continue; if (!mergeRules.getFieldInfo("toTable", fi)) {
/*      */           continue;
/*      */         }
/*      */ 
/*  856 */         List row = mergeRules.findRow(fi.m_index, "PublishedStaticFiles");
/*  857 */         if (null == row) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  863 */       componentsRequiringStaticPublishing.put(componentName, componentVersion);
/*  864 */       List row = new ArrayList(1);
/*  865 */       row.add(componentName);
/*  866 */       row.add(componentVersion);
/*  867 */       staticPublishingComponents.addRowWithList(row);
/*      */     }
/*      */ 
/*  870 */     Map previousComponentsRequiringStaticPublishing = new HashMap();
/*  871 */     DataResultSet previousComponents = (DataResultSet)binder.getResultSet("StaticPublishingComponents");
/*  872 */     FieldInfo[] fields = null;
/*  873 */     if (null != previousComponents)
/*      */     {
/*      */       try
/*      */       {
/*  877 */         fields = ResultSetUtils.createInfoList(previousComponents, staticPublishingComponentsFieldNames, true);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*  881 */         Report.trace("publish", null, e);
/*      */       }
/*      */     }
/*  884 */     if (null != fields)
/*      */     {
/*  886 */       for (previousComponents.first(); previousComponents.isRowPresent(); previousComponents.next())
/*      */       {
/*  888 */         String componentName = previousComponents.getStringValue(fields[0].m_index);
/*  889 */         String componentVersion = previousComponents.getStringValue(fields[1].m_index);
/*  890 */         previousComponentsRequiringStaticPublishing.put(componentName, componentVersion);
/*      */       }
/*      */     }
/*  893 */     boolean isChanged = !componentsRequiringStaticPublishing.equals(previousComponentsRequiringStaticPublishing);
/*  894 */     if (isChanged)
/*      */     {
/*  896 */       binder.addResultSet("StaticPublishingComponents", staticPublishingComponents);
/*  897 */       if ((SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("publish")))
/*      */       {
/*  899 */         IdcStringBuilder str = new IdcStringBuilder("static publishing components list changed:");
/*  900 */         Set allComponentsSet = new HashSet();
/*  901 */         allComponentsSet.addAll(componentsRequiringStaticPublishing.keySet());
/*  902 */         allComponentsSet.addAll(previousComponentsRequiringStaticPublishing.keySet());
/*  903 */         String[] allComponents = new String[allComponentsSet.size()];
/*  904 */         allComponentsSet.toArray(allComponents);
/*  905 */         Arrays.sort(allComponents);
/*      */ 
/*  907 */         for (int c = 0; c < allComponents.length; ++c)
/*      */         {
/*  909 */           String componentName = allComponents[c];
/*  910 */           String oldVersion = (String)previousComponentsRequiringStaticPublishing.get(componentName);
/*  911 */           String newVersion = (String)componentsRequiringStaticPublishing.get(componentName);
/*  912 */           if (oldVersion == null)
/*      */           {
/*  914 */             str.append("\n\tadded ");
/*  915 */             str.append(componentName);
/*      */           }
/*  917 */           else if (newVersion == null)
/*      */           {
/*  919 */             str.append("\n\tremoved ");
/*  920 */             str.append(componentName);
/*      */           }
/*      */           else
/*      */           {
/*  924 */             str.append("\n\tchanged ");
/*  925 */             str.append(componentName);
/*  926 */             str.append(" from ");
/*  927 */             str.append(oldVersion);
/*  928 */             str.append(" to ");
/*  929 */             str.append(newVersion);
/*      */           }
/*      */         }
/*  932 */         Report.trace("publish", str.toString(), null);
/*      */       }
/*      */     }
/*  935 */     return isChanged;
/*      */   }
/*      */ 
/*      */   protected String[] createSortedClassesArrayFromResultSet(ResultSet rset)
/*      */     throws DataException
/*      */   {
/*  951 */     String[] fieldNames = { "class" };
/*  952 */     FieldInfo[] fields = ResultSetUtils.createInfoList(rset, fieldNames, true);
/*  953 */     int classIndex = fields[0].m_index;
/*      */ 
/*  956 */     Set classesSet = new HashSet();
/*  957 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  959 */       String className = rset.getStringValue(classIndex);
/*  960 */       className = PublishedResourceUtils.toClassname(className);
/*  961 */       classesSet.add(className);
/*      */     }
/*      */ 
/*  965 */     String[] classesArray = StringUtils.convertToArray(classesSet);
/*  966 */     int numClasses = classesArray.length;
/*  967 */     boolean didPrune = false;
/*  968 */     for (int i = 0; i < numClasses - 1; ++i)
/*      */     {
/*  970 */       for (int j = i + 1; j < numClasses; ++j)
/*      */       {
/*  972 */         String superclass = classesArray[i];
/*  973 */         String subclass = classesArray[j];
/*  974 */         if (superclass.length() > subclass.length())
/*      */         {
/*  976 */           superclass = subclass;
/*  977 */           subclass = classesArray[i];
/*      */         }
/*  979 */         if (!PublishedResourceUtils.classnameMatches(superclass, subclass))
/*      */           continue;
/*  981 */         classesSet.remove(subclass);
/*  982 */         didPrune = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  988 */     if (didPrune)
/*      */     {
/*  990 */       classesArray = StringUtils.convertToArray(classesSet);
/*      */     }
/*  992 */     Arrays.sort(classesArray);
/*      */ 
/*  994 */     return classesArray;
/*      */   }
/*      */ 
/*      */   protected boolean checkIfEnabledComponentsChanged()
/*      */   {
/* 1004 */     DataBinder binder = this.m_state.m_startupBinder;
/* 1005 */     String[] componentsFieldNames = { "componentName", "componentVersion" };
/* 1006 */     DataResultSet currentComponents = new DataResultSet(componentsFieldNames);
/* 1007 */     Map currentComponentVersions = new HashMap();
/* 1008 */     Map componentBinders = ComponentLoader.m_components;
/* 1009 */     int numComponents = componentBinders.size();
/* 1010 */     String[] componentNames = new String[numComponents];
/* 1011 */     componentNames = (String[])componentBinders.keySet().toArray(componentNames);
/* 1012 */     Arrays.sort(componentNames);
/* 1013 */     for (int i = 0; i < numComponents; ++i)
/*      */     {
/* 1015 */       String componentName = componentNames[i];
/* 1016 */       DataBinder componentBinder = (DataBinder)componentBinders.get(componentName);
/* 1017 */       String componentVersion = componentBinder.getAllowMissing("version");
/* 1018 */       if (componentVersion == null)
/*      */       {
/* 1020 */         componentVersion = "";
/*      */       }
/* 1022 */       currentComponentVersions.put(componentName, componentVersion);
/* 1023 */       Vector row = new IdcVector(2);
/* 1024 */       row.add(componentName);
/* 1025 */       row.add(componentVersion);
/* 1026 */       currentComponents.addRow(row);
/*      */     }
/*      */ 
/* 1029 */     Map previousComponentVersions = new HashMap();
/* 1030 */     DataResultSet previousComponents = (DataResultSet)binder.getResultSet("EnabledComponents");
/* 1031 */     if (previousComponents != null)
/*      */     {
/*      */       try
/*      */       {
/* 1035 */         FieldInfo[] fields = ResultSetUtils.createInfoList(previousComponents, componentsFieldNames, true);
/* 1036 */         for (previousComponents.first(); previousComponents.isRowPresent(); previousComponents.next())
/*      */         {
/* 1038 */           String componentName = previousComponents.getStringValue(fields[0].m_index);
/* 1039 */           String componentVersion = previousComponents.getStringValue(fields[1].m_index);
/* 1040 */           previousComponentVersions.put(componentName, componentVersion);
/*      */         }
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1045 */         Report.trace("publish", null, e);
/*      */       }
/*      */     }
/* 1048 */     boolean isChanged = !currentComponentVersions.equals(previousComponentVersions);
/* 1049 */     if (isChanged)
/*      */     {
/* 1051 */       binder.addResultSet("EnabledComponents", currentComponents);
/*      */     }
/* 1053 */     return isChanged;
/*      */   }
/*      */ 
/*      */   protected void addLanguageFilesToDynamicPublish()
/*      */   {
/* 1063 */     Map langsMap = new HashMap();
/* 1064 */     for (Map.Entry entry : LocaleResources.m_locales.entrySet())
/*      */     {
/* 1066 */       String name = (String)entry.getKey();
/* 1067 */       IdcLocale locale = (IdcLocale)entry.getValue();
/* 1068 */       String langId = locale.m_languageId;
/* 1069 */       if ((langId == null) || (langId.length() == 0))
/*      */       {
/* 1071 */         Report.error("publish", null, new DataException(null, "csLocaleHasNoLangIDForPublish", new Object[] { name, langId }));
/*      */       }
/*      */ 
/* 1076 */       langsMap.put(langId, locale);
/*      */     }
/* 1078 */     Set langsSet = langsMap.keySet();
/* 1079 */     int numLangs = langsSet.size();
/* 1080 */     String[] langs = new String[numLangs];
/* 1081 */     langsSet.toArray(langs);
/* 1082 */     Arrays.sort(langs);
/* 1083 */     this.m_state.m_languages = langs;
/*      */ 
/* 1085 */     this.m_state.m_languageBinders = new HashMap();
/* 1086 */     DataBinder binder = this.m_state.m_publishBinder;
/* 1087 */     binder.clearResultSets();
/* 1088 */     String langClassname = "javascript:lang";
/* 1089 */     PublishedResourceContainer.Class resourceClass = new PublishedResourceContainer.Class("javascript:lang");
/* 1090 */     this.m_state.m_publishedClasses.put("javascript:lang", resourceClass);
/* 1091 */     resourceClass.m_bundle = this.m_state.m_dynamicPublisher.findBundleForClass("javascript:lang");
/*      */ 
/* 1093 */     boolean doPublish = (this.m_state.m_flags & 0x8) != 0;
/*      */ 
/* 1095 */     for (int i = 0; i < numLangs; ++i)
/*      */     {
/* 1097 */       String langId = langs[i];
/* 1098 */       String path = "resources/lang/" + langId + "/ww_strings.js";
/* 1099 */       String classname = "javascript:lang:" + langId;
/* 1100 */       String template = "LM_WW_JS_STRINGS";
/*      */ 
/* 1102 */       binder.setLocalData(new IdcProperties());
/* 1103 */       binder.putLocal("path", path);
/* 1104 */       binder.putLocal("langId", langId);
/* 1105 */       binder.putLocal("fileClass", classname);
/* 1106 */       binder.putLocal("fileLocation", path);
/* 1107 */       binder.putLocal("fileSource", "");
/* 1108 */       binder.putLocal("fileTemplate", template);
/*      */ 
/* 1110 */       PublishedResource resource = new PublishedResource(resourceClass, path, 10, 8);
/*      */ 
/* 1112 */       resource.m_source = template;
/* 1113 */       resource.m_doPublish = doPublish;
/* 1114 */       resource.m_binder = binder.createShallowCopy();
/* 1115 */       this.m_state.m_languageBinders.put(langId, resource.m_binder);
/* 1116 */       PublishedResourceUtils.trackResource(resource, this.m_state);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void setupLanguageStringsForDynamicPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1128 */     IdcTimer timer = this.m_state.m_timer;
/* 1129 */     if (timer != null)
/*      */     {
/* 1131 */       timer.start("generate language files");
/* 1132 */       timer.start("compute language strings");
/*      */     }
/*      */ 
/* 1136 */     String[] filter = { "ww" };
/* 1137 */     int flags = LocaleLoader.F_USE_EN_FOR_EMPTY_LANGUAGE;
/* 1138 */     ResultSet rsetStrings = LocaleLoader.createStringsResultSetWithPrefixFilter(filter, flags);
/* 1139 */     String[] fieldNames = { "lcLanguageCode", "lcKey", "lcValue" };
/* 1140 */     FieldInfo[] fields = ResultSetUtils.createInfoList(rsetStrings, fieldNames, true);
/*      */ 
/* 1142 */     Map strings = new HashMap();
/* 1143 */     for (rsetStrings.first(); rsetStrings.isRowPresent(); rsetStrings.next())
/*      */     {
/* 1145 */       String lang = rsetStrings.getStringValue(fields[0].m_index);
/* 1146 */       Map langStrings = (Map)strings.get(lang);
/* 1147 */       if (langStrings == null)
/*      */       {
/* 1149 */         langStrings = new HashMap();
/* 1150 */         strings.put(lang, langStrings);
/*      */       }
/* 1152 */       String key = rsetStrings.getStringValue(fields[1].m_index);
/* 1153 */       String value = rsetStrings.getStringValue(fields[2].m_index);
/* 1154 */       assert (value != null);
/* 1155 */       langStrings.put(key, value);
/*      */     }
/* 1157 */     if (this.m_state.m_isAbort)
/*      */     {
/* 1159 */       throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */     }
/* 1161 */     rsetStrings = null;
/* 1162 */     if (timer != null)
/*      */     {
/* 1164 */       timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */     }
/*      */ 
/* 1167 */     IdcStringBuilder str = new IdcStringBuilder();
/*      */ 
/* 1169 */     String[] langs = this.m_state.m_languages;
/* 1170 */     int numLangs = langs.length;
/*      */     try
/*      */     {
/* 1174 */       for (int i = 0; i < numLangs; ++i)
/*      */       {
/* 1176 */         String langId = langs[i];
/* 1177 */         if ((timer != null) && (SystemUtils.m_verbose))
/*      */         {
/* 1179 */           timer.start(langId);
/*      */         }
/* 1181 */         str.setLength(0);
/* 1182 */         Map langStrings = (Map)strings.get(langId);
/* 1183 */         if (langStrings != null)
/*      */         {
/* 1185 */           Set keySet = langStrings.keySet();
/* 1186 */           int numKeys = keySet.size();
/* 1187 */           String[] keys = new String[numKeys];
/* 1188 */           keySet.toArray(keys);
/* 1189 */           Arrays.sort(keys);
/* 1190 */           for (int k = 0; k < numKeys; ++k)
/*      */           {
/* 1192 */             String key = keys[k];
/* 1193 */             String value = (String)langStrings.get(key);
/* 1194 */             if (k > 0)
/*      */             {
/* 1196 */               str.append(",\n");
/*      */             }
/* 1198 */             str.append('"');
/* 1199 */             str.append(key);
/* 1200 */             str.append("\": \"");
/* 1201 */             StringUtils.appendEscapedString(str, value, 1344284292L);
/* 1202 */             str.append('"');
/*      */           }
/*      */         }
/*      */ 
/* 1206 */         DataBinder binder = (DataBinder)this.m_state.m_languageBinders.get(langId);
/* 1207 */         binder.putLocal("LocaleStrings", str.toStringNoRelease());
/* 1208 */         if ((timer == null) || (!SystemUtils.m_verbose))
/*      */           continue;
/* 1210 */         timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */       }
/*      */ 
/*      */     }
/*      */     finally
/*      */     {
/* 1216 */       str.releaseBuffers();
/*      */     }
/*      */ 
/* 1219 */     if (timer == null)
/*      */       return;
/* 1221 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void publishStaticPortal()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1238 */     IdcTimer timer = this.m_state.m_timer;
/* 1239 */     if (timer != null)
/*      */     {
/* 1241 */       timer.start("publish static portal");
/*      */     }
/* 1243 */     DataBinder portalBinder = this.m_state.m_publishBinder;
/* 1244 */     portalBinder.putLocal("PageFunction", "GetPageList");
/* 1245 */     portalBinder.putLocal("IsRebuild", "1");
/*      */ 
/* 1247 */     PageHandlerService pageHandlerService = new PageHandlerService();
/* 1248 */     pageHandlerService.init(this.m_state.m_workspace, System.err, portalBinder, new ServiceData());
/* 1249 */     pageHandlerService.initDelegatedObjects();
/*      */ 
/* 1251 */     IdcLocale theLocale = LocaleResources.getLocale("SystemLocale");
/* 1252 */     if (theLocale == null)
/*      */     {
/* 1254 */       theLocale = LocaleResources.getLocale("English-US");
/*      */ 
/* 1270 */       theLocale.m_pageEncoding = "utf-8";
/* 1271 */       IdcMessage msg = new IdcMessage("csLocaleNotFoundUsingSpecific", new Object[] { "SystemLocale", "English-US" });
/* 1272 */       Report.warning("publish", null, msg);
/*      */     }
/* 1274 */     this.m_state.m_context.setCachedObject("PageLocale", theLocale);
/* 1275 */     pageHandlerService.executePageService();
/* 1276 */     pageHandlerService.clear();
/* 1277 */     if (this.m_state.m_isAbort)
/*      */     {
/* 1279 */       throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */     }
/* 1281 */     if (timer == null)
/*      */       return;
/* 1283 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void doRefreshLayoutLists()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1299 */     IdcTimer timer = this.m_state.m_timer;
/* 1300 */     PageMerger merger = this.m_state.m_publishMerger;
/* 1301 */     merger.setActiveBinder(this.m_state.m_publishBinder);
/*      */ 
/* 1303 */     DataResultSet layouts = SharedObjects.getTable("LmLayouts");
/* 1304 */     FieldInfo[] layoutInfos = ResultSetUtils.createInfoList(layouts, new String[] { "id", "label", "enabled" }, true);
/*      */ 
/* 1306 */     DataResultSet layoutSkinPairs = SharedObjects.getTable("LmLayoutSkinPairs");
/* 1307 */     String[] skinInfoNames = { "layout", "skin", "skinDir", "enabled" };
/* 1308 */     FieldInfo[] skinInfos = ResultSetUtils.createInfoList(layoutSkinPairs, skinInfoNames, true);
/*      */ 
/* 1310 */     boolean scanLayouts = SharedObjects.getEnvValueAsBoolean("LegacyScanLayoutsDirForAdditionalLayouts", false);
/* 1311 */     if (scanLayouts)
/*      */     {
/* 1313 */       if (timer != null)
/*      */       {
/* 1315 */         timer.start("LmLayouts");
/*      */       }
/* 1317 */       String allLayoutsStr = this.m_state.m_weblayoutDirectory + "resources/layouts/";
/* 1318 */       File allLayoutsDir = new File(allLayoutsStr);
/* 1319 */       if ((allLayoutsDir.exists()) && (allLayoutsDir.isDirectory()))
/*      */       {
/* 1321 */         String[] dirList = allLayoutsDir.list();
/* 1322 */         for (int i = 0; i < dirList.length; ++i)
/*      */         {
/* 1324 */           String layout = dirList[i];
/* 1325 */           if (layout.startsWith(".svn"))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1330 */           File layoutDir = new File(allLayoutsStr + layout);
/* 1331 */           if (!layoutDir.isDirectory())
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/* 1336 */           if (layouts.findRow(layoutInfos[0].m_index, layout) != null)
/*      */             continue;
/* 1338 */           Vector v = layouts.createEmptyRow();
/* 1339 */           v.setElementAt(layout, layoutInfos[0].m_index);
/* 1340 */           v.setElementAt(layout, layoutInfos[1].m_index);
/* 1341 */           v.setElementAt("1", layoutInfos[2].m_index);
/* 1342 */           layouts.addRow(v);
/*      */         }
/*      */ 
/* 1345 */         SharedObjects.putTable("LmLayouts", layouts);
/*      */       }
/* 1347 */       if (timer != null)
/*      */       {
/* 1349 */         timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */       }
/*      */     }
/*      */ 
/* 1353 */     if (timer != null)
/*      */     {
/* 1355 */       timer.start("LmLayoutSkinPairs");
/*      */     }
/*      */ 
/* 1358 */     for (layoutSkinPairs.first(); layoutSkinPairs.isRowPresent(); layoutSkinPairs.next())
/*      */     {
/* 1360 */       String skinDir = layoutSkinPairs.getStringValue(skinInfos[2].m_index);
/* 1361 */       if (skinDir.length() < 1)
/*      */       {
/* 1363 */         String layoutName = layoutSkinPairs.getStringValue(skinInfos[0].m_index);
/* 1364 */         String skinName = layoutSkinPairs.getStringValue(skinInfos[1].m_index);
/* 1365 */         skinDir = "resources/layouts/" + layoutName + '/' + skinName;
/* 1366 */         layoutSkinPairs.setCurrentValue(skinInfos[2].m_index, skinDir);
/*      */       }
/* 1368 */       String script = layoutSkinPairs.getStringValue(skinInfos[3].m_index);
/*      */       try
/*      */       {
/* 1371 */         String value = merger.evaluateScript(script);
/* 1372 */         layoutSkinPairs.setCurrentValue(skinInfos[3].m_index, value);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1376 */         Report.debug("publish", null, ignore);
/*      */       }
/*      */       finally
/*      */       {
/* 1380 */         merger.releaseAllTemporary();
/*      */       }
/*      */     }
/* 1383 */     SharedObjects.putTable("LmLayoutSkinPairs", layoutSkinPairs);
/* 1384 */     if (timer == null)
/*      */       return;
/* 1386 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void loadFilteredStaticPublishedFiles()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1399 */     IdcTimer timer = this.m_state.m_timer;
/* 1400 */     if (timer != null)
/*      */     {
/* 1402 */       timer.start("load static file list");
/*      */     }
/* 1404 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/* 1407 */       this.m_state.m_CFU.readDataBinderFromName(PublishedResourceUtils.STATIC_FILENAME, binder, null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1411 */       if (timer != null)
/*      */       {
/* 1413 */         timer.resetToLevelByName("load filtered table");
/* 1414 */         timer.stop(0, new Object[0]);
/*      */       }
/* 1416 */       throw new ServiceException(e);
/*      */     }
/* 1418 */     this.m_state.m_staticFiles = ((DataResultSet)binder.getResultSet("FilteredStaticWebResources"));
/* 1419 */     if (timer == null)
/*      */       return;
/* 1421 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void computeStaticPublishedResources()
/*      */     throws DataException
/*      */   {
/* 1432 */     DataResultSet staticFiles = this.m_state.m_staticFiles;
/* 1433 */     String[] fieldNames = { "class", "loadOrder", "path", "component" };
/* 1434 */     FieldInfo[] fields = ResultSetUtils.createInfoList(staticFiles, fieldNames, true);
/* 1435 */     for (staticFiles.first(); staticFiles.isRowPresent(); staticFiles.next())
/*      */     {
/* 1437 */       String classname = staticFiles.getStringValue(fields[0].m_index);
/* 1438 */       String loadOrderString = staticFiles.getStringValue(fields[1].m_index);
/* 1439 */       String path = staticFiles.getStringValue(fields[2].m_index);
/* 1440 */       String component = staticFiles.getStringValue(fields[3].m_index);
/* 1441 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(classname);
/* 1442 */       if (resourceClass == null)
/*      */       {
/* 1444 */         resourceClass = new PublishedResourceContainer.Class(classname);
/* 1445 */         this.m_state.m_publishedClasses.put(classname, resourceClass);
/* 1446 */         resourceClass.m_bundle = this.m_state.m_dynamicPublisher.findBundleForClass(classname);
/*      */       }
/* 1448 */       int loadOrder = NumberUtils.parseInteger(loadOrderString, 0);
/* 1449 */       PublishedResource resource = new PublishedResource(resourceClass, path, loadOrder, 16);
/*      */ 
/* 1451 */       resource.m_component = component;
/* 1452 */       PublishedResourceUtils.trackResource(resource, this.m_state);
/*      */     }
/* 1454 */     SharedObjects.putTable("FilteredStaticWebResources", this.m_state.m_staticFiles);
/*      */   }
/*      */ 
/*      */   public void computeSortedPublishedContainers()
/*      */     throws DataException
/*      */   {
/* 1467 */     Set containers = new HashSet();
/* 1468 */     Map bundledClasses = new HashMap();
/*      */ 
/* 1471 */     for (String classname : this.m_state.m_publishedClasses.keySet())
/*      */     {
/* 1473 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(classname);
/* 1474 */       if (resourceClass.m_bundle == null)
/*      */       {
/* 1476 */         int numResources = resourceClass.m_resources.size();
/* 1477 */         String[] sortedPaths = resourceClass.m_sortedResourcePaths = new String[numResources];
/* 1478 */         for (int r = 0; r < numResources; ++r)
/*      */         {
/* 1480 */           sortedPaths[r] = ((PublishedResource)resourceClass.m_resources.get(r)).m_path;
/*      */         }
/* 1482 */         containers.add(resourceClass);
/*      */       }
/*      */       else
/*      */       {
/* 1486 */         Set classes = (Set)bundledClasses.get(resourceClass.m_bundle);
/* 1487 */         if (classes == null)
/*      */         {
/* 1489 */           classes = new HashSet();
/* 1490 */           bundledClasses.put(resourceClass.m_bundle, classes);
/*      */         }
/* 1492 */         classes.add(resourceClass);
/*      */       }
/*      */     }
/* 1495 */     WebFeaturesPublisher wfp = this.m_state.m_webFeaturesPublisher;
/*      */ 
/* 1498 */     if (this.m_state.m_doBundling)
/*      */     {
/* 1500 */       for (String bundlePath : this.m_state.m_bundles.keySet())
/*      */       {
/* 1502 */         PublishedResourceContainer.Bundle bundle = (PublishedResourceContainer.Bundle)this.m_state.m_bundles.get(bundlePath);
/* 1503 */         Set classes = (Set)bundledClasses.get(bundle);
/* 1504 */         PublishedResourceContainer[] sortedContainers = null;
/* 1505 */         if (classes != null)
/*      */         {
/* 1507 */           sortedContainers = wfp.sortContainers(classes, false);
/*      */         }
/* 1509 */         this.m_state.m_dynamicPublisher.bundleSortedContainers(bundle, sortedContainers);
/* 1510 */         containers.add(bundle);
/*      */       }
/*      */     }
/*      */ 
/* 1514 */     this.m_state.m_sortedContainers = wfp.sortContainers(containers, true);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1521 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94233 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.WebPublisher
 * JD-Core Version:    0.5.4
 */