/*      */ package intradoc.server.publish;
/*      */ 
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.IdcTimer;
/*      */ import intradoc.common.LoggingUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.filestore.config.ConfigFileStore;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.ProgressState;
/*      */ import intradoc.shared.ProgressStateUtils;
/*      */ import intradoc.shared.ResultSetTreeSort;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Reader;
/*      */ import java.io.Writer;
/*      */ import java.util.ArrayList;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DynamicPublisher
/*      */ {
/*   79 */   protected static String[] WEBLAYOUT_FILES_FIELDNAMES = { "path", "template", "class", "loadOrder", "doPublishScript", "srcPath" };
/*      */ 
/*   83 */   protected static final String[] DYNAMIC_FILES_FIELDNAMES = { "path", "class", "loadOrder" };
/*      */   public WebPublishState m_state;
/*      */   public DataResultSet m_publishedWeblayoutFiles;
/*      */   public FieldInfo[] m_publishedWeblayoutFilesFields;
/*      */   public DataResultSet m_previousDynamicFiles;
/*      */   public boolean m_useGzip;
/*      */   public String m_dynamicFilenameExtension;
/*      */ 
/*      */   public DynamicPublisher(WebPublishState state)
/*      */   {
/*  107 */     this.m_state = state;
/*  108 */     this.m_useGzip = SharedObjects.getEnvValueAsBoolean("GzipPublishedFiles", false);
/*  109 */     if ((!this.m_useGzip) || 
/*  111 */       (!SharedObjects.getEnvValueAsBoolean("UseGzipExtension", true)))
/*      */       return;
/*  113 */     this.m_dynamicFilenameExtension = ".gz";
/*      */   }
/*      */ 
/*      */   public void loadPreviouslyPublishedDynamicFiles()
/*      */     throws DataException, ServiceException
/*      */   {
/*  127 */     if (this.m_state.m_timer != null)
/*      */     {
/*  129 */       this.m_state.m_timer.start("load dynamic file list");
/*      */     }
/*      */ 
/*  136 */     DataBinder binder = new DataBinder();
/*  137 */     ConfigFileUtilities cfu = this.m_state.m_CFU;
/*      */     try
/*      */     {
/*  141 */       cfu.readDataBinderFromName(PublishedResourceUtils.LEGACY_DYNAMIC_FILENAME, binder, null);
/*  142 */       IdcFileDescriptor desc = cfu.createDescriptorByName(PublishedResourceUtils.LEGACY_DYNAMIC_FILENAME, null);
/*      */ 
/*  144 */       cfu.m_CFS.deleteFile(desc, null, cfu.m_context);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  153 */       cfu.readDataBinderFromName(PublishedResourceUtils.DYNAMIC_FILENAME, binder, null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  157 */       if (SystemUtils.m_verbose)
/*      */       {
/*  159 */         Report.trace("publish", "unable to determine previously-published dynamic files", e);
/*      */       }
/*      */     }
/*  162 */     this.m_previousDynamicFiles = ((DataResultSet)binder.getResultSet("LastWebfilesPublished"));
/*      */ 
/*  164 */     if (this.m_state.m_timer == null)
/*      */       return;
/*  166 */     this.m_state.m_timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void prepare()
/*      */     throws DataException, ServiceException
/*      */   {
/*  178 */     IdcTimer timer = this.m_state.m_timer;
/*  179 */     if (timer != null)
/*      */     {
/*  181 */       timer.start("dynamic");
/*      */     }
/*      */ 
/*  184 */     DataResultSet publishedWeblayoutFiles = SharedObjects.getTable("PublishedWeblayoutFiles");
/*  185 */     this.m_publishedWeblayoutFiles = new DataResultSet();
/*  186 */     this.m_publishedWeblayoutFiles.copy(publishedWeblayoutFiles);
/*  187 */     publishedWeblayoutFiles = this.m_publishedWeblayoutFiles;
/*  188 */     this.m_publishedWeblayoutFilesFields = ResultSetUtils.createInfoList(publishedWeblayoutFiles, WEBLAYOUT_FILES_FIELDNAMES, true);
/*      */ 
/*  190 */     WebPublishUtils.sortFileSet(publishedWeblayoutFiles, this.m_publishedWeblayoutFilesFields[3].m_index);
/*      */ 
/*  192 */     computeDynamicResources();
/*  193 */     addBundlesToDynamicPublish();
/*  194 */     computePublishedDynamicFiles();
/*      */ 
/*  196 */     if (timer == null)
/*      */       return;
/*  198 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void computeDynamicResources()
/*      */     throws DataException, ServiceException
/*      */   {
/*  210 */     IdcTimer timer = this.m_state.m_timer;
/*  211 */     if (timer != null)
/*      */     {
/*  213 */       timer.start("compute weblayout files");
/*      */     }
/*  215 */     String resourceDir = LegacyDirectoryLocator.getResourcesDirectory();
/*  216 */     DataResultSet publishedWeblayoutFiles = SharedObjects.getTable("PublishedWeblayoutFiles");
/*  217 */     this.m_publishedWeblayoutFiles = new DataResultSet();
/*  218 */     int idcComponentNameIndex = publishedWeblayoutFiles.getFieldInfoIndex("idcComponentName");
/*  219 */     FieldInfo[] fileInfos = this.m_publishedWeblayoutFilesFields;
/*      */ 
/*  221 */     DataBinder binder = this.m_state.m_publishBinder;
/*  222 */     this.m_state.m_publishMerger.setActiveBinder(binder);
/*  223 */     for (publishedWeblayoutFiles.first(); publishedWeblayoutFiles.isRowPresent(); publishedWeblayoutFiles.next())
/*      */     {
/*  226 */       binder.setLocalData(new IdcProperties());
/*  227 */       binder.clearResultSets();
/*      */ 
/*  229 */       String fileLocation = publishedWeblayoutFiles.getStringValue(fileInfos[0].m_index);
/*  230 */       String template = publishedWeblayoutFiles.getStringValue(fileInfos[1].m_index);
/*  231 */       String className = publishedWeblayoutFiles.getStringValue(fileInfos[2].m_index);
/*  232 */       String loadOrder = publishedWeblayoutFiles.getStringValue(fileInfos[3].m_index);
/*  233 */       String doPublishScript = publishedWeblayoutFiles.getStringValue(fileInfos[4].m_index);
/*  234 */       String srcPath = publishedWeblayoutFiles.getStringValue(fileInfos[5].m_index);
/*  235 */       String componentName = (idcComponentNameIndex < 0) ? null : publishedWeblayoutFiles.getStringValue(idcComponentNameIndex);
/*      */ 
/*  240 */       if ((className.startsWith("css:layout")) && (!fileLocation.contains("/rtl/")) && (fileLocation.startsWith("resources/layouts/")))
/*      */       {
/*  243 */         int split = fileLocation.indexOf(47, 18);
/*  244 */         split = fileLocation.indexOf(47, split + 1);
/*  245 */         String rtlFileLocation = fileLocation.substring(0, split) + "/rtl" + fileLocation.substring(split);
/*  246 */         String doPublishScriptWithRtl = doPublishScript + "<$isRtl=1$>";
/*  247 */         Vector v = publishedWeblayoutFiles.createEmptyRow();
/*  248 */         v.setElementAt(rtlFileLocation, fileInfos[0].m_index);
/*  249 */         v.setElementAt(template, fileInfos[1].m_index);
/*  250 */         v.setElementAt(className, fileInfos[2].m_index);
/*  251 */         v.setElementAt(loadOrder, fileInfos[3].m_index);
/*  252 */         v.setElementAt(doPublishScriptWithRtl, fileInfos[4].m_index);
/*  253 */         v.setElementAt(srcPath, fileInfos[5].m_index);
/*  254 */         publishedWeblayoutFiles.addRow(v);
/*      */       }
/*      */ 
/*  257 */       if (template.length() < 1)
/*      */       {
/*  259 */         String srcDir = resourceDir;
/*  260 */         if (componentName != null)
/*      */         {
/*  262 */           DataBinder componentBinder = ComponentLoader.getComponentBinder(componentName);
/*  263 */           if (componentBinder != null)
/*      */           {
/*  265 */             String componentDir = componentBinder.getLocal("ComponentDir");
/*  266 */             if ((componentDir != null) && (componentDir.length() > 0))
/*      */             {
/*  268 */               srcDir = componentDir;
/*      */             }
/*      */           }
/*      */         }
/*  272 */         srcPath = FileUtils.getAbsolutePath(srcDir, srcPath);
/*  273 */         if (fileLocation.endsWith("/"))
/*      */         {
/*  279 */           String filename = FileUtils.getName(srcPath);
/*  280 */           fileLocation = fileLocation + filename;
/*      */         }
/*      */       }
/*      */ 
/*  284 */       binder.putLocal("fileTemplate", template);
/*  285 */       binder.putLocal("fileClass", className);
/*  286 */       binder.putLocal("fileLocation", fileLocation);
/*  287 */       binder.putLocal("fileSource", srcPath);
/*      */       try
/*      */       {
/*  291 */         boolean publishFile = WebPublishUtils.checkForPublish(doPublishScript, binder, this.m_state.m_publishMerger);
/*  292 */         if (!publishFile)
/*      */         {
/*  294 */           break label786:
/*      */         }
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  299 */         Report.trace("publish", "checkForPublish failed", e);
/*  300 */         break label786:
/*      */       }
/*      */       try
/*      */       {
/*  304 */         fileLocation = this.m_state.m_publishMerger.evaluateScript(fileLocation);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  308 */         Report.trace("publish", null, e);
/*  309 */         break label786:
/*      */       }
/*      */ 
/*  313 */       PublishedResourceContainer.Class resourceClass = (PublishedResourceContainer.Class)this.m_state.m_publishedClasses.get(className);
/*  314 */       if (resourceClass == null)
/*      */       {
/*  316 */         resourceClass = new PublishedResourceContainer.Class(className);
/*  317 */         this.m_state.m_publishedClasses.put(className, resourceClass);
/*      */ 
/*  319 */         resourceClass.m_bundle = findBundleForClass(className);
/*      */       }
/*  321 */       if (resourceClass.m_bundle != null)
/*      */       {
/*  323 */         binder.putLocal("bundlePath", resourceClass.m_bundle.m_path);
/*      */       }
/*      */ 
/*  327 */       int loadOrderValue = NumberUtils.parseInteger(loadOrder, 0);
/*  328 */       PublishedResource resource = new PublishedResource(resourceClass, fileLocation, loadOrderValue, 1);
/*      */ 
/*  330 */       resource.m_source = ((template.length() > 0) ? template : srcPath);
/*  331 */       resource.m_component = componentName;
/*  332 */       resource.m_doPublish = true;
/*  333 */       resource.m_binder = binder.createShallowCopy();
/*  334 */       label786: PublishedResourceUtils.trackResource(resource, this.m_state);
/*      */     }
/*  336 */     if (timer == null)
/*      */       return;
/*  338 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   public void computePublishedDynamicFiles()
/*      */   {
/*  349 */     DataResultSet dynamicFiles = this.m_state.m_dynamicFiles = new DataResultSet(DYNAMIC_FILES_FIELDNAMES);
/*  350 */     for (String path : this.m_state.m_publishedResources.keySet())
/*      */     {
/*  352 */       PublishedResource resource = (PublishedResource)this.m_state.m_publishedResources.get(path);
/*  353 */       if (resource.m_publishType != 1)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  358 */       if ((resource.m_class != null) && (resource.m_class.m_bundle != null))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  363 */       String classname = (resource.m_class == null) ? "bundle" : resource.m_class.m_name;
/*  364 */       String loadOrder = String.valueOf(resource.m_loadOrder);
/*  365 */       List row = new ArrayList();
/*  366 */       row.add(path);
/*  367 */       row.add(classname);
/*  368 */       row.add(loadOrder);
/*  369 */       dynamicFiles.addRowWithList(row);
/*      */     }
/*  371 */     WebPublishUtils.sortFileSet(dynamicFiles, 0);
/*  372 */     SharedObjects.putTable("LastWebfilesPublished", this.m_state.m_dynamicFiles);
/*      */   }
/*      */ 
/*      */   public void doPublish()
/*      */     throws DataException, ServiceException
/*      */   {
/*  383 */     IdcTimer timer = this.m_state.m_timer;
/*  384 */     if (timer != null)
/*      */     {
/*  386 */       timer.start("dynamic");
/*      */     }
/*  388 */     ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "dynamicpublisher", 3, "starting dynamic publish", null);
/*      */ 
/*  390 */     boolean success = false;
/*      */     try
/*      */     {
/*  393 */       buildDynamicFilesAndBundles();
/*  394 */       if (this.m_previousDynamicFiles != null)
/*      */       {
/*  396 */         removeOldDynamicFiles();
/*      */       }
/*  398 */       saveDynamicFilesList();
/*  399 */       if (timer != null)
/*      */       {
/*  401 */         timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */       }
/*  403 */       success = true;
/*      */     }
/*      */     finally
/*      */     {
/*  407 */       if (success)
/*      */       {
/*  409 */         this.m_state.m_progress.setStateValue("latestState", "Success");
/*  410 */         ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "publish", 3, "finished dynamic publish", null);
/*      */       }
/*      */       else
/*      */       {
/*  415 */         this.m_state.m_progress.setStateValue("latestState", "Failed");
/*  416 */         ProgressStateUtils.reportProgress(this.m_state.m_progress, null, "publish", 3, "dynamic publish failed", null);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void buildDynamicFilesAndBundles()
/*      */     throws DataException, ServiceException
/*      */   {
/*  430 */     IdcTimer timer = this.m_state.m_timer;
/*  431 */     if (timer != null)
/*      */     {
/*  433 */       timer.start("build weblayout files");
/*      */     }
/*  435 */     PageMerger merger = this.m_state.m_publishMerger;
/*  436 */     boolean useGzip = this.m_useGzip;
/*  437 */     String extension = this.m_dynamicFilenameExtension;
/*      */ 
/*  439 */     ExecutionContext context = this.m_state.m_context;
/*      */ 
/*  441 */     IdcCharArrayWriter w = new IdcCharArrayWriter(16384);
/*      */ 
/*  443 */     int numContainers = this.m_state.m_sortedContainers.length;
/*  444 */     for (int c = 0; c < numContainers; ++c)
/*      */     {
/*  446 */       PublishedResourceContainer container = this.m_state.m_sortedContainers[c];
/*  447 */       boolean isBundled = container instanceof PublishedResourceContainer.Bundle;
/*  448 */       int numResources = container.m_resources.size();
/*  449 */       for (int r = 0; r < numResources; ++r)
/*      */       {
/*  451 */         PublishedResource resource = (PublishedResource)container.m_resources.get(r);
/*  452 */         if (!resource.m_doPublish) {
/*      */           continue;
/*      */         }
/*      */ 
/*  456 */         if (isBundled)
/*      */         {
/*  458 */           if (resource.m_class.m_bundle != container)
/*      */           {
/*  460 */             String msg = "resource bundle mismatch: " + resource.m_class.m_bundle + " != " + container;
/*  461 */             Report.trace("publish", msg, null);
/*      */           }
/*  464 */           else if (resource.m_publishType == 16)
/*      */           {
/*  466 */             readWeblayoutFileAndAppendToWriter(resource.m_path, w);
/*  467 */             if ((!SystemUtils.m_verbose) || (!this.m_state.m_doTrace))
/*      */               continue;
/*  469 */             String msg = "warning: bundling static resource '" + resource + "'";
/*  470 */             Report.trace("publish", msg, null);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  477 */           if (resource.m_class != container)
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  483 */           if ((resource.m_publishType != 1) && (resource.m_publishType != 8))
/*      */           {
/*      */             continue;
/*      */           }
/*      */ 
/*  489 */           String path = resource.m_path;
/*  490 */           if ((timer != null) && (SystemUtils.m_verbose))
/*      */           {
/*  492 */             timer.start(path);
/*      */           }
/*  494 */           DataBinder binder = resource.m_binder;
/*  495 */           boolean isTemplate = binder.get("fileTemplate").length() > 0;
/*  496 */           merger.setActiveBinder(binder);
/*      */ 
/*  500 */           int returnCode = PluginFilters.filter("publishWeblayoutFile", null, binder, context);
/*  501 */           if (returnCode != -1)
/*      */           {
/*  503 */             DynamicHtml html = null;
/*  504 */             if (!isTemplate)
/*      */             {
/*  506 */               html = ResourceLoader.loadPage(resource.m_source, false);
/*      */             }
/*  508 */             if (isBundled)
/*      */             {
/*  510 */               binder.putLocal("path", resource.m_class.m_bundle.m_path);
/*      */             }
/*      */             else
/*      */             {
/*  514 */               if (useGzip)
/*      */               {
/*  516 */                 path = path + extension;
/*      */               }
/*  518 */               binder.putLocal("path", path);
/*      */             }
/*  520 */             if (html == null)
/*      */             {
/*  522 */               merger.writeMergedPage(w, resource.m_source);
/*      */             }
/*      */             else
/*      */             {
/*  526 */               merger.writeMergedPage(w, html);
/*      */             }
/*  528 */             if (!isBundled)
/*      */             {
/*  530 */               doWriteWeblayoutFileIfChanged(path, w, context);
/*  531 */               w.reset();
/*      */             }
/*      */           }
/*  534 */           if (this.m_state.m_isAbort)
/*      */           {
/*  536 */             throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */           }
/*  538 */           if ((timer == null) || (!SystemUtils.m_verbose))
/*      */             continue;
/*  540 */           timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */         }
/*      */       }
/*  543 */       if (!isBundled)
/*      */         continue;
/*  545 */       PublishedResourceContainer.Bundle bundle = (PublishedResourceContainer.Bundle)container;
/*  546 */       if ((timer != null) && (SystemUtils.m_verbose))
/*      */       {
/*  548 */         timer.start(bundle.m_path);
/*      */       }
/*  550 */       doWriteWeblayoutFileIfChanged(bundle.m_path, w, context);
/*  551 */       w.reset();
/*  552 */       if ((timer == null) || (!SystemUtils.m_verbose))
/*      */         continue;
/*  554 */       timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */     }
/*      */ 
/*  558 */     FileUtils.discard(w);
/*  559 */     if (timer == null)
/*      */       return;
/*  561 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void removeOldDynamicFiles()
/*      */     throws ServiceException
/*      */   {
/*  573 */     IdcTimer timer = this.m_state.m_timer;
/*  574 */     if (timer != null)
/*      */     {
/*  576 */       timer.start("remove old files");
/*      */     }
/*      */ 
/*  581 */     String weblayoutDir = this.m_state.m_weblayoutDirectory;
/*  582 */     DataResultSet previouslyPublishedSet = this.m_previousDynamicFiles;
/*  583 */     FieldInfo fi = new FieldInfo();
/*  584 */     previouslyPublishedSet.getFieldInfo("path", fi);
/*  585 */     DataResultSet publishedFiles = this.m_state.m_dynamicFiles;
/*  586 */     for (publishedFiles.first(); publishedFiles.isRowPresent(); publishedFiles.next())
/*      */     {
/*  588 */       String fileLocation = publishedFiles.getStringValue(0);
/*  589 */       if (previouslyPublishedSet.findRow(fi.m_index, fileLocation) == null)
/*      */         continue;
/*  591 */       previouslyPublishedSet.deleteCurrentRow();
/*      */     }
/*      */ 
/*  596 */     for (previouslyPublishedSet.first(); previouslyPublishedSet.isRowPresent(); previouslyPublishedSet.next())
/*      */     {
/*  598 */       String filePath = previouslyPublishedSet.getStringValue(fi.m_index);
/*  599 */       FileUtils.deleteFile(weblayoutDir + filePath);
/*  600 */       if (this.m_state.m_isAbort)
/*      */       {
/*  602 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*      */ 
/*  606 */       String parentPath = FileUtils.getParent(weblayoutDir + filePath);
/*  607 */       boolean tryDelete = true;
/*  608 */       while ((tryDelete) && (parentPath != null) && (parentPath.length() > 0) && (!parentPath.equals(weblayoutDir)))
/*      */       {
/*  610 */         File directory = new File(parentPath);
/*  611 */         if ((directory.list() == null) || (directory.list().length == 0))
/*      */         {
/*  613 */           FileUtils.deleteDirectory(directory, true);
/*  614 */           if (this.m_state.m_isAbort)
/*      */           {
/*  616 */             throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  621 */           tryDelete = false;
/*      */         }
/*      */ 
/*  624 */         parentPath = FileUtils.getParent(parentPath);
/*      */       }
/*      */     }
/*  627 */     if (timer == null)
/*      */       return;
/*  629 */     timer.stop(this.m_state.m_timerFlags, new Object[0]);
/*      */   }
/*      */ 
/*      */   protected void saveDynamicFilesList()
/*      */     throws DataException, ServiceException
/*      */   {
/*  641 */     DataBinder newBinder = new DataBinder();
/*  642 */     newBinder.addResultSet("LastWebfilesPublished", this.m_state.m_dynamicFiles);
/*      */     try
/*      */     {
/*  645 */       this.m_state.m_CFU.writeDataBinderToName(PublishedResourceUtils.DYNAMIC_FILENAME, newBinder, null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  649 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void doWriteWeblayoutFileIfChanged(String filePath, IdcCharArrayWriter fileText, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  676 */     cxt.setCachedObject("filePath", filePath);
/*  677 */     cxt.setReturnValue(null);
/*  678 */     cxt.setCachedObject("fileContent", fileText);
/*  679 */     int status = PluginFilters.filter("writeWeblayoutFile", null, null, cxt);
/*  680 */     if (status == -1)
/*      */     {
/*  682 */       return;
/*      */     }
/*  684 */     filePath = (String)cxt.getCachedObject("filePath");
/*  685 */     Object retVal = cxt.getReturnValue();
/*      */ 
/*  689 */     IdcCharArrayWriter newFileContent = null;
/*  690 */     if ((retVal != null) && (retVal instanceof IdcCharArrayWriter))
/*      */     {
/*  692 */       newFileContent = (IdcCharArrayWriter)retVal;
/*      */     }
/*      */ 
/*  695 */     Object newFileContentObj = cxt.getCachedObject("fileContent");
/*  696 */     if ((newFileContentObj != null) && (newFileContentObj != fileText) && (newFileContentObj instanceof IdcCharArrayWriter))
/*      */     {
/*  698 */       newFileContent = (IdcCharArrayWriter)newFileContentObj;
/*      */     }
/*  700 */     if (newFileContent != null)
/*      */     {
/*  702 */       fileText = newFileContent;
/*      */     }
/*      */ 
/*  705 */     String weblayoutDir = this.m_state.m_weblayoutDirectory;
/*  706 */     int weblayoutDirLength = weblayoutDir.length();
/*  707 */     String fullPath = weblayoutDir + filePath;
/*  708 */     String parentDir = FileUtils.getParent(fullPath);
/*  709 */     if (parentDir.length() >= weblayoutDirLength)
/*      */     {
/*  711 */       String partialParent = parentDir.substring(weblayoutDir.length());
/*  712 */       FileUtils.checkOrCreateSubDirectory(weblayoutDir, partialParent + "/");
/*  713 */       if (this.m_state.m_isAbort)
/*      */       {
/*  715 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*      */     }
/*  718 */     Exception e = null;
/*      */ 
/*  720 */     boolean needToWrite = false;
/*      */ 
/*  722 */     File currentFile = new File(fullPath);
/*  723 */     long currentFileLength = currentFile.length();
/*  724 */     if (currentFileLength != fileText.m_length)
/*      */     {
/*  726 */       needToWrite = true;
/*      */     }
/*      */ 
/*  729 */     int flags = 0;
/*  730 */     if (this.m_useGzip)
/*      */     {
/*  732 */       flags |= 2;
/*      */     }
/*  734 */     if (!needToWrite)
/*      */     {
/*  736 */       char[] buf = (char[])(char[])FileUtils.createBufferForStreaming(0, 1);
/*  737 */       Reader r = null;
/*      */       try
/*      */       {
/*  740 */         if (currentFile.exists())
/*      */         {
/*  742 */           InputStream in = new FileInputStream(fullPath);
/*  743 */           r = FileUtils.openDataReaderEx(in, "ISO-8859-1", flags);
/*      */ 
/*  745 */           int offset = 0;
/*      */           do
/*      */           {
/*      */             int size;
/*  746 */             if ((size = r.read(buf)) <= 0)
/*      */               break;
/*  748 */             if (this.m_state.m_isAbort)
/*      */             {
/*  750 */               throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */             }
/*  752 */             for (int i = 0; i < size; ++i)
/*      */             {
/*  754 */               if (buf[i] == fileText.m_charArray[(offset++)])
/*      */                 continue;
/*  756 */               needToWrite = true;
/*  757 */               break;
/*      */             }
/*      */           }
/*  760 */           while (!needToWrite);
/*      */         }
/*      */         else
/*      */         {
/*  768 */           needToWrite = true;
/*      */         }
/*      */       }
/*      */       catch (IOException ioe)
/*      */       {
/*  773 */         Report.trace("publish", null, ioe);
/*  774 */         LoggingUtils.error(ioe, null, null);
/*  775 */         needToWrite = true;
/*      */       }
/*      */       finally
/*      */       {
/*  779 */         FileUtils.releaseBufferForStreaming(buf);
/*  780 */         FileUtils.closeObject(r);
/*      */       }
/*      */     }
/*      */ 
/*  784 */     if (needToWrite)
/*      */     {
/*  786 */       Writer fw = null;
/*  787 */       String tempFilePath = null;
/*  788 */       OutputStream out = null;
/*      */       try
/*      */       {
/*  791 */         out = FileUtils.openOutputStream(fullPath, 16);
/*  792 */         fw = FileUtils.openDataWriterEx(out, "ISO-8859-1", flags);
/*  793 */         fileText.writeTo(fw);
/*  794 */         if (this.m_state.m_isAbort)
/*      */         {
/*  796 */           throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */         }
/*      */       }
/*      */       catch (Exception ex)
/*      */       {
/*  801 */         e = ex;
/*      */       }
/*      */       finally
/*      */       {
/*  805 */         FileUtils.abort(out);
/*  806 */         FileUtils.closeObject(fw);
/*  807 */         FileUtils.deleteFile(tempFilePath);
/*      */       }
/*      */ 
/*  810 */       if (e != null)
/*      */       {
/*  812 */         IdcMessage msg = IdcMessageFactory.lc("csSchUnableToBuildStaticCommonFile", new Object[] { fullPath });
/*  813 */         if (e instanceof ServiceException)
/*      */         {
/*  815 */           throw new ServiceException(e, msg);
/*      */         }
/*  817 */         Report.trace("publish", null, e);
/*  818 */         throw new DataException(e, msg);
/*      */       }
/*      */     }
/*  821 */     if (newFileContent == null)
/*      */       return;
/*  823 */     newFileContent.releaseBuffers();
/*      */   }
/*      */ 
/*      */   protected void readWeblayoutFileAndAppendToWriter(String filePath, IdcCharArrayWriter writer)
/*      */     throws ServiceException
/*      */   {
/*  837 */     if ((SystemUtils.m_verbose) && (this.m_state.m_doTrace))
/*      */     {
/*  839 */       Report.trace("publish", "reading " + filePath, null);
/*      */     }
/*  841 */     String fullPath = this.m_state.m_weblayoutDirectory + filePath;
/*  842 */     Reader reader = null;
/*  843 */     int flags = (this.m_useGzip) ? 2 : 0;
/*      */     try
/*      */     {
/*  846 */       InputStream in = new FileInputStream(fullPath);
/*  847 */       reader = FileUtils.openDataReaderEx(in, "ISO-8859-1", flags);
/*  848 */       FileUtils.copyReaderToWriter(reader, writer);
/*  849 */       if (this.m_state.m_isAbort)
/*      */       {
/*  851 */         throw new ServiceException(null, "csPublishAborted", new Object[0]);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  860 */       FileUtils.closeObject(reader);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void computePublishedBundles()
/*      */     throws DataException, ServiceException
/*      */   {
/*  873 */     this.m_state.m_bundleFilters = new HashMap();
/*  874 */     loadPublishedBundleFilters();
/*      */ 
/*  876 */     this.m_state.m_bundles = new HashMap();
/*  877 */     for (String bundlePath : this.m_state.m_bundleFilters.keySet())
/*      */     {
/*  879 */       PublishedResourceContainer.Bundle bundle = new PublishedResourceContainer.Bundle(bundlePath);
/*      */ 
/*  881 */       List filterList = (List)this.m_state.m_bundleFilters.get(bundlePath);
/*  882 */       int length = filterList.size();
/*  883 */       PublishedResourceContainer.Bundle.Filter[] sortedFilters = new PublishedResourceContainer.Bundle.Filter[length];
/*      */ 
/*  885 */       filterList.toArray(sortedFilters);
/*  886 */       bundle.m_sortedFilters = sortedFilters;
/*  887 */       this.m_state.m_bundles.put(bundlePath, bundle);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadPublishedBundleFilters()
/*      */     throws DataException
/*      */   {
/*  899 */     Map filters = this.m_state.m_bundleFilters;
/*      */ 
/*  901 */     DataResultSet filtersTable = SharedObjects.getTable("PublishedBundles");
/*  902 */     String[] filtersFieldNames = { "bundlePath", "includeClass", "excludeClass", "loadOrder" };
/*  903 */     FieldInfo[] filtersFields = ResultSetUtils.createInfoList(filtersTable, filtersFieldNames, true);
/*      */ 
/*  909 */     ResultSetTreeSort sorter = new ResultSetTreeSort(filtersTable);
/*  910 */     sorter.m_sortColIndex = filtersFields[3].m_index;
/*  911 */     sorter.m_fieldSortType = 3;
/*  912 */     sorter.sort();
/*      */ 
/*  914 */     for (filtersTable.first(); filtersTable.isRowPresent(); filtersTable.next())
/*      */     {
/*  916 */       String bundlePath = filtersTable.getStringValue(filtersFields[0].m_index);
/*  917 */       String includeClass = filtersTable.getStringValue(filtersFields[1].m_index);
/*  918 */       String excludeClass = filtersTable.getStringValue(filtersFields[2].m_index);
/*  919 */       List filterList = (List)filters.get(bundlePath);
/*  920 */       if (filterList == null)
/*      */       {
/*  922 */         filterList = new ArrayList();
/*  923 */         filters.put(bundlePath, filterList);
/*      */       }
/*  925 */       if (includeClass.length() > 0)
/*      */       {
/*  927 */         PublishedResourceContainer.Bundle.Filter filter = new PublishedResourceContainer.Bundle.Filter(false, includeClass);
/*      */ 
/*  929 */         filterList.add(filter);
/*      */       }
/*  931 */       if (excludeClass.length() <= 0)
/*      */         continue;
/*  933 */       PublishedResourceContainer.Bundle.Filter filter = new PublishedResourceContainer.Bundle.Filter(true, excludeClass);
/*      */ 
/*  935 */       filterList.add(filter);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addBundlesToDynamicPublish()
/*      */   {
/*  946 */     if (!this.m_state.m_doBundling)
/*      */       return;
/*  948 */     for (String bundlePath : this.m_state.m_bundles.keySet())
/*      */     {
/*  950 */       PublishedResource overwrittenResource = (PublishedResource)this.m_state.m_publishedResources.remove(bundlePath);
/*  951 */       if (overwrittenResource != null)
/*      */       {
/*  953 */         overwrittenResource.m_class.m_resources.remove(overwrittenResource);
/*      */       }
/*      */ 
/*  956 */       PublishedResource resource = new PublishedResource(null, bundlePath, 2147483647, 1);
/*      */ 
/*  958 */       PublishedResourceUtils.trackResource(resource, this.m_state);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void bundleSortedContainers(PublishedResourceContainer.Bundle bundle, PublishedResourceContainer[] containers)
/*      */   {
/*  972 */     bundle.m_resources = new ArrayList();
/*  973 */     bundle.m_providedFeatures = new HashSet();
/*  974 */     bundle.m_requiredFeatures = new HashSet();
/*  975 */     bundle.m_optionalFeatures = new HashSet();
/*  976 */     if (containers == null)
/*      */     {
/*  978 */       return;
/*      */     }
/*  980 */     IdcStringBuilder sb = null;
/*  981 */     if ((SystemUtils.m_verbose) && (this.m_state.m_doTrace))
/*      */     {
/*  983 */       sb = new IdcStringBuilder(bundle.m_path);
/*  984 */       sb.append(" = [");
/*      */     }
/*  986 */     boolean doComma = false;
/*  987 */     int numContainers = containers.length;
/*      */ 
/*  989 */     for (int c = 0; c < numContainers; ++c)
/*      */     {
/*  991 */       PublishedResourceContainer container = containers[c];
/*  992 */       bundle.m_resources.addAll(container.m_resources);
/*  993 */       if (sb != null)
/*      */       {
/*  995 */         int numResources = container.m_resources.size();
/*  996 */         for (int r = 0; r < numResources; ++r)
/*      */         {
/*  998 */           PublishedResource resource = (PublishedResource)container.m_resources.get(r);
/*  999 */           if (doComma)
/*      */           {
/* 1001 */             sb.append(", ");
/*      */           }
/* 1003 */           sb.append(resource.m_path);
/* 1004 */           doComma = true;
/*      */         }
/*      */       }
/* 1007 */       if (container.m_providedFeatures == null)
/*      */         continue;
/* 1009 */       bundle.m_providedFeatures.addAll(container.m_providedFeatures);
/* 1010 */       bundle.m_requiredFeatures.addAll(container.m_requiredFeatures);
/* 1011 */       bundle.m_optionalFeatures.addAll(container.m_optionalFeatures);
/*      */     }
/*      */ 
/* 1014 */     if (sb == null)
/*      */       return;
/* 1016 */     sb.append(']');
/* 1017 */     Report.trace("publish", sb.toStringNoRelease(), null);
/* 1018 */     sb.setLength(0);
/*      */   }
/*      */ 
/*      */   protected PublishedResourceContainer.Bundle findBundleForClass(String classname)
/*      */   {
/* 1028 */     if (this.m_state.m_doBundling)
/*      */     {
/* 1030 */       for (String bundlePath : this.m_state.m_bundles.keySet())
/*      */       {
/* 1032 */         PublishedResourceContainer.Bundle bundle = (PublishedResourceContainer.Bundle)this.m_state.m_bundles.get(bundlePath);
/* 1033 */         PublishedResourceContainer.Bundle.Filter[] filters = bundle.m_sortedFilters;
/* 1034 */         boolean isMatch = false;
/* 1035 */         int length = filters.length;
/* 1036 */         for (int f = 0; f < length; ++f)
/*      */         {
/* 1038 */           String filterString = filters[f].m_classname;
/* 1039 */           if (!PublishedResourceUtils.classnameMatches(filterString, classname))
/*      */             continue;
/* 1041 */           isMatch = !filters[f].m_isExclusive;
/*      */         }
/*      */ 
/* 1044 */         if (isMatch)
/*      */         {
/* 1046 */           return bundle;
/*      */         }
/*      */       }
/*      */     }
/* 1050 */     return null;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1057 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95394 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.publish.DynamicPublisher
 * JD-Core Version:    0.5.4
 */