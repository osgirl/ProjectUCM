/*      */ package intradoc.preview;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PropertiesTreeNode;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.provider.ServerRequestUtils;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.DatedCacheUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcServiceAction;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceHandler;
/*      */ import intradoc.server.project.ProjectInfo;
/*      */ import intradoc.server.project.ProjectUtils;
/*      */ import intradoc.server.project.Projects;
/*      */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class PreviewHandler extends ServiceHandler
/*      */ {
/*      */   @IdcServiceAction
/*      */   public void cacheCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/*   84 */     String dir = buildVaultScratchDir(false);
/*      */ 
/*   86 */     String action = "checkin";
/*   87 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/*   89 */       action = this.m_currentAction.getParamAt(0);
/*      */     }
/*      */ 
/*   92 */     DataBinder cachedData = null;
/*   93 */     ResultSet rset = this.m_binder.getResultSet("CacheInfo");
/*   94 */     boolean isUpdate = false;
/*   95 */     if ((rset != null) && (rset.isRowPresent()))
/*      */     {
/*   97 */       isUpdate = true;
/*      */       try
/*      */       {
/*  100 */         cachedData = ResourceUtils.readDataBinder(dir, "metadata.hda");
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  110 */     moveAndCheckFile("primaryFile", dir, true);
/*  111 */     moveAndCheckFile("alternateFile", dir, false);
/*      */ 
/*  114 */     removeKruft(this.m_binder);
/*      */ 
/*  117 */     ResourceUtils.serializeDataBinder(dir, "metadata.hda", this.m_binder, true, false);
/*      */ 
/*  120 */     addOrUpdatePreviewInfo(cachedData, action, isUpdate);
/*      */   }
/*      */ 
/*      */   protected void addOrUpdatePreviewInfo(DataBinder data, String action, boolean isUpdate)
/*      */     throws DataException
/*      */   {
/*  127 */     Date dte = new Date();
/*  128 */     String createDate = LocaleUtils.formatODBC(dte);
/*  129 */     this.m_binder.putLocal("dLastUsedDate", createDate);
/*  130 */     this.m_binder.putLocal("dCacheAction", action);
/*      */ 
/*  133 */     String val = this.m_binder.getAllowMissing("dCacheHash");
/*  134 */     if (val == null)
/*      */     {
/*  136 */       this.m_binder.putLocal("dCacheHash", "" + dte.getTime());
/*      */     }
/*      */ 
/*  140 */     String query = null;
/*  141 */     if (isUpdate)
/*      */     {
/*  143 */       query = "Ucache";
/*  144 */       if (data != null)
/*      */       {
/*  146 */         String docName = data.getLocal("dDocName");
/*      */         try
/*      */         {
/*  150 */           Map args = new HashMap();
/*  151 */           args.put("isContainer", "1");
/*      */ 
/*  153 */           data.putLocal("RenditionId", "webViewableFile");
/*  154 */           IdcFileDescriptor oldDesc = this.m_service.m_fileStore.createDescriptor(data, args, this.m_service);
/*      */ 
/*  156 */           this.m_binder.putLocal("RenditionId", "webViewableFile");
/*  157 */           IdcFileDescriptor curDesc = this.m_service.m_fileStore.createDescriptor(this.m_binder, args, this.m_service);
/*      */ 
/*  160 */           if (!this.m_service.m_fileStore.compareDescriptors(oldDesc, curDesc, null, this.m_service))
/*      */           {
/*  163 */             String dir = this.m_service.m_fileStore.getContainerPath(oldDesc, null, this.m_service);
/*  164 */             String webDir = dir + "~" + docName.toLowerCase();
/*  165 */             File wd = new File(webDir);
/*  166 */             FileUtils.deleteDirectory(wd, true);
/*      */           }
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*  171 */           Report.warning(null, e, "csUnableToDeleteCachedDirectory", new Object[] { docName });
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  177 */       query = "Icache";
/*      */     }
/*  179 */     this.m_workspace.execute(query, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void buildPreviewList()
/*      */     throws DataException, ServiceException
/*      */   {
/*  187 */     Hashtable projects = Projects.getProjects();
/*  188 */     Hashtable matchingProjects = new Hashtable();
/*  189 */     for (Enumeration en = projects.elements(); en.hasMoreElements(); )
/*      */     {
/*  191 */       ProjectInfo pInfo = (ProjectInfo)en.nextElement();
/*  192 */       if (!pInfo.m_hasPreview) continue; if (pInfo.m_previewXml == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  196 */       Vector resNodes = new IdcVector();
/*  197 */       PreviewUtils.parseProject(pInfo.m_previewXml, this.m_binder, resNodes);
/*      */ 
/*  199 */       if (resNodes.size() > 0)
/*      */       {
/*  201 */         matchingProjects.put(pInfo, resNodes);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  206 */     DataResultSet prSet = ProjectUtils.computePreviewResultSet(matchingProjects);
/*  207 */     if ((prSet == null) || (prSet.isEmpty()))
/*      */     {
/*  209 */       this.m_service.createServiceException(null, "!csItemDoesntMatchProjectCriteria");
/*      */     }
/*      */     else
/*      */     {
/*  213 */       this.m_binder.addResultSet("Projects", prSet);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void retrieveCachedInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  221 */     String dir = buildVaultScratchDir(true);
/*  222 */     DataBinder binder = null;
/*      */     try
/*      */     {
/*  225 */       binder = ResourceUtils.readDataBinder(dir, "metadata.hda");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  229 */       this.m_service.createServiceException(null, "!csUnableToReadCachedData");
/*      */     }
/*      */ 
/*  232 */     this.m_binder.setLocalData(binder.getLocalData());
/*      */ 
/*  235 */     removeKruft(this.m_binder);
/*      */ 
/*  238 */     int num = this.m_currentAction.getNumParams();
/*  239 */     if (num <= 0)
/*      */       return;
/*  241 */     String param = this.m_currentAction.getParamAt(0);
/*  242 */     if (param.equals("checkin"))
/*      */     {
/*  244 */       this.m_binder.m_isExternalRequest = false;
/*      */     } else {
/*  246 */       if (!param.equals("webinfo")) {
/*      */         return;
/*      */       }
/*      */       try
/*      */       {
/*  251 */         String url = computeAndExtractManifest(false, true, false, null);
/*      */ 
/*  253 */         this.m_binder.putLocal("DocUrl", url);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  260 */         this.m_binder.putLocal("DocUrl", "");
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void continueCheckin()
/*      */     throws DataException, ServiceException
/*      */   {
/*  271 */     String[] keys = { "dRevClassID", "dRevisionID", "dDocID", "dRawDocID", "latestID", "dIsWebFormat", "dIsPrimary", "dReleaseState" };
/*      */ 
/*  273 */     int num = keys.length;
/*  274 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  276 */       this.m_binder.removeLocal(keys[i]);
/*      */     }
/*      */ 
/*  280 */     String oldService = this.m_binder.getLocal("IdcService");
/*      */ 
/*  282 */     boolean isWorkflow = DataBinderUtils.getBoolean(this.m_binder, "IsWorkflow", false);
/*      */ 
/*  284 */     String cmd = "CHECKIN_NEW_SUB";
/*  285 */     if (isWorkflow)
/*      */     {
/*  287 */       cmd = "WORKFLOW_CHECKIN_SUB";
/*      */     }
/*      */     else
/*      */     {
/*  291 */       String prevID = this.m_binder.getLocal("prevID");
/*  292 */       if (prevID != null)
/*      */       {
/*  294 */         this.m_binder.putLocal("dID", prevID);
/*  295 */         cmd = "CHECKIN_SEL_SUB";
/*      */       }
/*      */       else
/*      */       {
/*  299 */         this.m_binder.removeLocal("dID");
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  305 */       this.m_binder.putLocal("IdcService", cmd);
/*  306 */       this.m_service.executeService(cmd);
/*      */     }
/*      */     finally
/*      */     {
/*  310 */       if (oldService != null)
/*      */       {
/*  312 */         this.m_binder.putLocal("IdcService", oldService);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void cleanup(boolean isError)
/*      */   {
/*  320 */     if (!isError)
/*      */       return;
/*  322 */     boolean isPreviewUpload = this.m_service.isConditionVarTrue("IsPreviewUpload");
/*  323 */     if (!isPreviewUpload)
/*      */       return;
/*      */     try
/*      */     {
/*  327 */       this.m_workspace.beginTran();
/*  328 */       doCachedCheckinCleanup();
/*  329 */       this.m_workspace.commitTran();
/*      */     }
/*      */     catch (Throwable e)
/*      */     {
/*  333 */       this.m_workspace.rollbackTran();
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareFormContinue()
/*      */     throws DataException, ServiceException
/*      */   {
/*  342 */     this.m_binder.removeResultSet("CheckinInfo");
/*      */ 
/*  344 */     boolean isPreviewName = DataBinderUtils.getBoolean(this.m_binder, "IsPreviewName", false);
/*  345 */     if (!isPreviewName)
/*      */       return;
/*  347 */     this.m_binder.putLocal("dDocName", "");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doCachedCleanup()
/*      */     throws DataException, ServiceException
/*      */   {
/*  354 */     String action = this.m_currentAction.getParamAt(0);
/*  355 */     doCachedCheckinCleanup(action);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void doCachedCheckinCleanup() throws DataException, ServiceException
/*      */   {
/*  361 */     doCachedCheckinCleanup("checkin");
/*      */   }
/*      */ 
/*      */   public void doCachedCheckinCleanup(String action) throws DataException, ServiceException
/*      */   {
/*  366 */     this.m_binder.putLocal("dCacheAction", action);
/*  367 */     this.m_workspace.execute("Dcache", this.m_binder);
/*      */ 
/*  369 */     DatedCacheUtils.cleanUpCheckinDirectories(this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void performPreview() throws DataException, ServiceException
/*      */   {
/*  375 */     String projectID = this.m_binder.getLocal("projectID");
/*  376 */     ProjectInfo pInfo = Projects.getProjectInfo(projectID);
/*  377 */     if ((pInfo == null) || (!pInfo.m_hasPreview) || (pInfo.m_previewXml == null))
/*      */     {
/*  379 */       this.m_service.createServiceException(null, "!csProjectNoLongerRegistered");
/*      */     }
/*      */ 
/*  383 */     String pagePathID = this.m_binder.getLocal("pagePathID");
/*  384 */     List xmlNodes = pInfo.m_previewXml;
/*      */ 
/*  386 */     Properties props = ProjectUtils.findNode(pagePathID, xmlNodes);
/*  387 */     if (props == null)
/*      */     {
/*  389 */       this.m_service.createServiceException(null, "!csUnableToFindBodyElement");
/*      */     }
/*      */ 
/*  392 */     performPreviewRequest(projectID, props);
/*      */   }
/*      */ 
/*      */   protected String buildVaultScratchDir(boolean mustExist)
/*      */     throws DataException, ServiceException
/*      */   {
/*  400 */     String docName = this.m_binder.get("dDocName").toLowerCase();
/*      */ 
/*  402 */     String checkinDirRoot = DirectoryLocator.getTempDirectory() + "~checkin/";
/*  403 */     String dir = checkinDirRoot + "~" + docName;
/*      */ 
/*  405 */     File targetDir = new File(dir);
/*  406 */     File rootDir = new File(checkinDirRoot);
/*      */     try
/*      */     {
/*  410 */       String cDir = targetDir.getCanonicalPath();
/*  411 */       String cRootDir = rootDir.getCanonicalPath();
/*  412 */       if (!cDir.startsWith(cRootDir))
/*      */       {
/*  414 */         IdcMessage msg = IdcMessageFactory.lc("csInvalidCachedCheckInDirectory", new Object[] { cDir });
/*  415 */         this.m_service.createServiceException(msg);
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*  420 */       this.m_service.createServiceException(ioe, "!csInvalidCachedCheckInDirectory");
/*      */     }
/*      */ 
/*  423 */     if (mustExist)
/*      */     {
/*  425 */       if (!targetDir.exists())
/*      */       {
/*  427 */         this.m_service.createServiceException(null, "!csCachedCheckInDirectoryNoLongerExists");
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  433 */       FileUtils.checkOrCreateDirectory(dir, 2);
/*      */     }
/*      */ 
/*  436 */     return dir;
/*      */   }
/*      */ 
/*      */   protected void moveAndCheckFile(String key, String dir, boolean isCheckSize) throws ServiceException
/*      */   {
/*  441 */     String filePath = getFilePath(key);
/*  442 */     if (filePath == null)
/*      */     {
/*  445 */       return;
/*      */     }
/*      */ 
/*  448 */     filePath = filePath.trim();
/*  449 */     if (filePath.length() == 0)
/*      */     {
/*  451 */       return;
/*      */     }
/*      */ 
/*  454 */     String name = this.m_binder.getLocal("dDocName");
/*  455 */     String toName = name.toLowerCase();
/*      */ 
/*  458 */     String fileName = FileUtils.fileSlashes(filePath);
/*  459 */     int extIndex = fileName.lastIndexOf(46);
/*  460 */     int nameIndex = fileName.lastIndexOf(47);
/*      */ 
/*  462 */     if (extIndex < nameIndex)
/*      */     {
/*  466 */       extIndex = -1;
/*      */     }
/*  468 */     String extension = "";
/*  469 */     if (extIndex >= 0)
/*      */     {
/*  471 */       extension = fileName.substring(extIndex + 1);
/*  472 */       toName = toName + "." + extension;
/*      */     }
/*      */ 
/*  475 */     String toFilePath = dir + "/" + toName;
/*  476 */     File fromFile = new File(fileName);
/*  477 */     File toFile = new File(toFilePath);
/*      */ 
/*  480 */     if (isCheckSize)
/*      */     {
/*  482 */       int maxSize = SharedObjects.getEnvironmentInt("PreviewMaxFileSize", 2000);
/*  483 */       maxSize *= 1000;
/*  484 */       long size = fromFile.length();
/*  485 */       if (size > maxSize)
/*      */       {
/*  487 */         this.m_service.createServiceException(null, "!csFileTooBig");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  492 */     FileUtils.renameFile(fromFile.getAbsolutePath(), toFile.getAbsolutePath());
/*      */ 
/*  494 */     this.m_binder.putLocal(key + ":path", toFilePath);
/*      */   }
/*      */ 
/*      */   protected void performPreviewRequest(String projectID, Properties nodeProps)
/*      */     throws DataException, ServiceException
/*      */   {
/*  506 */     ProjectInfo pInfo = Projects.getProjectInfo(projectID);
/*  507 */     if ((pInfo == null) || (!pInfo.m_hasPreview) || (pInfo.m_previewXml == null))
/*      */     {
/*  509 */       this.m_service.createServiceException(null, "!csProjectNotRegistered");
/*      */     }
/*      */ 
/*  512 */     List previewXml = pInfo.m_previewXml;
/*  513 */     if (previewXml.size() == 0)
/*      */     {
/*  515 */       this.m_service.createServiceException(null, "!csProjectNotRegisteredError");
/*      */     }
/*  517 */     PropertiesTreeNode node = (PropertiesTreeNode)previewXml.get(0);
/*      */ 
/*  520 */     String sourcePath = node.m_properties.getProperty("sourcePath");
/*  521 */     if (sourcePath == null)
/*      */     {
/*  523 */       this.m_service.createServiceException(null, "!csProjectInvalid");
/*      */     }
/*      */ 
/*  526 */     Properties srcProps = ProjectUtils.parseSourcePath(sourcePath, "IDC_Name", "idc://");
/*      */ 
/*  529 */     Vector providers = Providers.getProvidersOfType("preview");
/*  530 */     if ((providers == null) || (providers.size() == 0))
/*      */     {
/*  532 */       this.m_service.createServiceException(null, "!csPreviewProviderMissing");
/*      */     }
/*      */ 
/*  537 */     Provider provider = (Provider)providers.elementAt(0);
/*      */ 
/*  541 */     boolean isDoPreview = true;
/*  542 */     boolean isSimple = StringUtils.convertToBool(this.m_binder.getLocal("IsSimplePreview"), false);
/*      */ 
/*  544 */     if (isSimple)
/*      */     {
/*  548 */       isDoPreview = retrieveActiveInfo(projectID, nodeProps);
/*      */     }
/*      */ 
/*  551 */     if (!isDoPreview)
/*      */       return;
/*  553 */     String zipName = buildPreviewZip(srcProps, nodeProps);
/*  554 */     sendPreviewRequest(provider, zipName);
/*      */   }
/*      */ 
/*      */   protected boolean retrieveActiveInfo(String projectID, Properties nodeProps)
/*      */     throws DataException, ServiceException
/*      */   {
/*  562 */     ResultSet rset = this.m_workspace.createResultSet("QdocInfo", this.m_binder);
/*  563 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/*  565 */       this.m_service.createServiceException(null, "!csItemNoLongerInSystem");
/*      */     }
/*      */ 
/*  569 */     DataResultSet drset = new DataResultSet();
/*  570 */     drset.copy(rset);
/*  571 */     this.m_binder.addResultSet("DocInfo", drset);
/*      */ 
/*  574 */     this.m_binder.putLocal("RenditionId", "primaryFile");
/*  575 */     IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/*  576 */     String filePath = this.m_service.m_fileStore.getFilesystemPath(d, this.m_service);
/*  577 */     this.m_binder.putLocal("FilePath", filePath);
/*      */ 
/*  580 */     File file = new File(filePath);
/*  581 */     if (!file.exists())
/*      */     {
/*  583 */       throw new ServiceException("!csPreviewFileMissing");
/*      */     }
/*  585 */     long lastModified = file.lastModified();
/*      */ 
/*  588 */     String str = projectID + nodeProps.getProperty("pagePathID") + lastModified;
/*  589 */     int hashCode = str.hashCode();
/*      */ 
/*  593 */     boolean hasBeenPreviewed = false;
/*  594 */     boolean isDoPreview = true;
/*  595 */     this.m_binder.putLocal("dCacheAction", "simple");
/*  596 */     rset = this.m_workspace.createResultSet("Qcache", this.m_binder);
/*  597 */     if ((rset != null) && (rset.isRowPresent()))
/*      */     {
/*  599 */       hasBeenPreviewed = true;
/*  600 */       str = ResultSetUtils.getValue(rset, "dCacheHash");
/*  601 */       int oldHash = NumberUtils.parseInteger(str, 0);
/*  602 */       if (oldHash == hashCode)
/*      */       {
/*      */         try
/*      */         {
/*  607 */           String webRoot = computeAndExtractManifest(true, false, false, null);
/*      */ 
/*  609 */           this.m_binder.putLocal("EntryPointRoot", webRoot);
/*  610 */           isDoPreview = false;
/*      */         }
/*      */         catch (Exception e)
/*      */         {
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  619 */     if (isDoPreview)
/*      */     {
/*  621 */       this.m_binder.putLocal("dCacheHash", "" + hashCode);
/*  622 */       DataBinder data = null;
/*  623 */       boolean isUpdate = false;
/*  624 */       if (hasBeenPreviewed)
/*      */       {
/*  626 */         isUpdate = true;
/*  627 */         data = this.m_binder;
/*      */       }
/*  629 */       addOrUpdatePreviewInfo(data, "simple", isUpdate);
/*      */     }
/*  631 */     return isDoPreview;
/*      */   }
/*      */ 
/*      */   protected String buildPreviewZip(Properties prjProps, Properties nodeProps)
/*      */     throws DataException, ServiceException
/*      */   {
/*  639 */     DataBinder prvBinder = new DataBinder();
/*      */ 
/*  641 */     DataResultSet fileSet = new DataResultSet(ZipFunctions.ZIP_FILE_COLUMNS);
/*  642 */     String dir = DataBinder.getTemporaryDirectory();
/*      */ 
/*  644 */     retrieveProjectFile(prjProps, dir, prvBinder, fileSet);
/*      */ 
/*  646 */     boolean isSimple = StringUtils.convertToBool(this.m_binder.getLocal("IsSimplePreview"), false);
/*      */ 
/*  648 */     if (isSimple)
/*      */     {
/*  650 */       retrieveActiveDocument(dir, prvBinder, fileSet);
/*      */     }
/*      */     else
/*      */     {
/*  654 */       retrieveStoredDocument(dir, prvBinder, fileSet);
/*      */     }
/*      */ 
/*  657 */     retrieveTemplate(nodeProps, dir, prvBinder, fileSet);
/*      */ 
/*  660 */     String str = SharedObjects.getEnvironmentValue("PreviewOutputExtension");
/*  661 */     if ((str == null) || (str.length() == 0))
/*      */     {
/*  664 */       str = nodeProps.getProperty("ext");
/*  665 */       if ((str == null) || (str.length() == 0))
/*      */       {
/*  668 */         str = "hcsp";
/*      */       }
/*      */     }
/*      */ 
/*  672 */     prvBinder.putLocal("OutputExt", str);
/*      */ 
/*  674 */     loadExtraPreviewConfig(prvBinder);
/*      */ 
/*  677 */     String filename = DataBinder.getNextFileCounter() + ".hda";
/*  678 */     ResourceUtils.serializeDataBinder(dir, filename, prvBinder, true, false);
/*  679 */     this.m_binder.addTempFile(dir + filename);
/*      */ 
/*  681 */     Vector row = new IdcVector();
/*  682 */     row.addElement("manifest.hda");
/*  683 */     row.addElement(dir + filename);
/*  684 */     fileSet.addRow(row);
/*      */ 
/*  687 */     String zipName = dir + DataBinder.getNextFileCounter() + ".zip";
/*  688 */     ZipFunctions.createZipFile(zipName, fileSet);
/*      */ 
/*  690 */     this.m_binder.addTempFile(zipName);
/*      */ 
/*  692 */     return zipName;
/*      */   }
/*      */ 
/*      */   protected void retrieveProjectFile(Properties prjProps, String dir, DataBinder binder, DataResultSet fileSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  698 */     String srcIdcName = prjProps.getProperty("IDC_Name");
/*  699 */     String dDocName = prjProps.getProperty("dDocName");
/*  700 */     String dID = prjProps.getProperty("dID");
/*      */ 
/*  702 */     DataBinder prjBinder = new DataBinder();
/*  703 */     String downloadName = dDocName.toLowerCase() + ".tcp";
/*      */ 
/*  705 */     String toPath = null;
/*      */ 
/*  707 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  708 */     if (srcIdcName.equals(idcName))
/*      */     {
/*  711 */       prjBinder.putLocal("dDocName", dDocName);
/*  712 */       prjBinder.putLocal("dID", dID);
/*  713 */       ResultSet rset = this.m_workspace.createResultSet("QdocInfo", prjBinder);
/*  714 */       if (rset.isEmpty())
/*      */       {
/*  716 */         this.m_service.createServiceException(null, "!csProjectNoLongerExists");
/*      */       }
/*  718 */       prjBinder.addResultSet("DocInfo", rset);
/*      */ 
/*  720 */       prjBinder.putLocal("RenditionId", "primaryFile");
/*  721 */       IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(prjBinder, null, this.m_service);
/*      */ 
/*  723 */       String file = this.m_service.m_fileStore.getFilesystemPath(d, this.m_service);
/*      */ 
/*  725 */       toPath = dir + DataBinder.getNextFileCounter() + ".tcp";
/*      */ 
/*  728 */       FileUtils.copyFile(file, toPath);
/*  729 */       this.m_binder.addTempFile(toPath);
/*      */     }
/*      */     else
/*      */     {
/*  734 */       Provider provider = OutgoingProviderMonitor.getOutgoingProvider(srcIdcName);
/*  735 */       if (provider == null)
/*      */       {
/*  737 */         String msg = LocaleUtils.encodeMessage("csSourceProviderNotConfigured", "!csUnableToRetrieveProjectFile", srcIdcName);
/*      */ 
/*  739 */         this.m_service.createServiceException(null, msg);
/*      */       }
/*      */ 
/*  743 */       prjBinder.putLocal("IdcService", "GET_FILE");
/*  744 */       prjBinder.putLocal("dDocName", dDocName);
/*  745 */       prjBinder.putLocal("dID", dID);
/*  746 */       prjBinder.putLocal("Rendition", "primary");
/*      */ 
/*  748 */       doPreviewRequest(prjBinder, provider);
/*      */ 
/*  750 */       toPath = prjBinder.getLocal("downloadFile:path");
/*  751 */       if (toPath == null)
/*      */       {
/*  754 */         String errCode = prjBinder.getLocal("StatusCode");
/*  755 */         String errMessage = prjBinder.getLocal("StatusMessage");
/*  756 */         int code = -1;
/*  757 */         if (errCode != null)
/*      */         {
/*  759 */           code = NumberUtils.parseInteger(errCode, -1);
/*      */         }
/*  761 */         ServiceException se = null;
/*  762 */         if ((code < 0) && (errMessage != null))
/*      */         {
/*  764 */           se = new ServiceException(code, errMessage);
/*      */         }
/*  766 */         this.m_service.createServiceException(se, "!csUnableToRetrieveProjectFile");
/*      */       }
/*      */     }
/*      */ 
/*  770 */     binder.putLocal("ProjectFile", downloadName);
/*      */ 
/*  773 */     Vector row = new IdcVector();
/*  774 */     row.addElement(downloadName);
/*  775 */     row.addElement(toPath);
/*  776 */     fileSet.addRow(row);
/*      */   }
/*      */ 
/*      */   protected void retrieveStoredDocument(String dir, DataBinder binder, DataResultSet fileSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  783 */     String docName = this.m_binder.getLocal("dDocName").toLowerCase();
/*  784 */     String vaultDir = DirectoryLocator.getTempDirectory();
/*  785 */     String docDir = vaultDir + "~checkin/~" + docName;
/*      */ 
/*  787 */     DataBinder metadata = null;
/*      */     try
/*      */     {
/*  790 */       metadata = ResourceUtils.readDataBinder(docDir, "metadata.hda");
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  794 */       this.m_service.createServiceException(null, "!csUnableToReadCachedData");
/*      */     }
/*      */ 
/*  799 */     String[] keys = { "StatusCode", "StatusMessage", "IdcService" };
/*  800 */     int num = keys.length;
/*  801 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  803 */       metadata.removeLocal(keys[i]);
/*      */     }
/*  805 */     metadata.removeResultSet("CacheInfo");
/*      */ 
/*  808 */     this.m_binder.merge(metadata);
/*      */ 
/*  811 */     binder.merge(metadata);
/*      */ 
/*  813 */     String filePath = metadata.getLocal("primaryFile:path");
/*      */ 
/*  815 */     binder.putLocal("PreviewDocument", docName);
/*      */ 
/*  818 */     Vector row = new IdcVector();
/*  819 */     row.addElement(docName);
/*  820 */     row.addElement(filePath);
/*  821 */     fileSet.addRow(row);
/*      */   }
/*      */ 
/*      */   protected void retrieveActiveDocument(String dir, DataBinder binder, DataResultSet fileSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  827 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("DocInfo");
/*  828 */     Properties props = drset.getCurrentRowProps();
/*  829 */     DataBinder.mergeHashTables(binder.getLocalData(), props);
/*      */ 
/*  832 */     String docName = binder.getLocal("dDocName").toLowerCase();
/*  833 */     String filePath = this.m_binder.getLocal("FilePath");
/*      */ 
/*  835 */     binder.putLocal("PreviewDocument", docName);
/*      */ 
/*  838 */     Vector row = new IdcVector();
/*  839 */     row.addElement(docName);
/*  840 */     row.addElement(filePath);
/*  841 */     fileSet.addRow(row);
/*      */   }
/*      */ 
/*      */   protected void retrieveTemplate(Properties nodeProps, String dir, DataBinder binder, DataResultSet fileSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  849 */     String template = nodeProps.getProperty("template");
/*  850 */     binder.putLocal("Template", template);
/*      */   }
/*      */ 
/*      */   protected void loadExtraPreviewConfig(DataBinder prvBinder)
/*      */     throws DataException, ServiceException
/*      */   {
/*  859 */     MetaFieldData metaSetOriginal = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*      */ 
/*  862 */     DataResultSet metaSet = new DataResultSet();
/*  863 */     metaSet.copy(metaSetOriginal);
/*      */ 
/*  865 */     prvBinder.addResultSet("DocMetaDefinition", metaSet);
/*      */ 
/*  868 */     String[] keys = { "dName", "dType", "dCaption" };
/*  869 */     FieldInfo[] fi = ResultSetUtils.createInfoList(metaSet, keys, true);
/*  870 */     int nameIndex = fi[0].m_index;
/*  871 */     int typeIndex = fi[1].m_index;
/*  872 */     int captionIndex = fi[2].m_index;
/*      */ 
/*  874 */     String[][] fieldLabels = ViewFields.FIELD_LABELS;
/*  875 */     int num = fieldLabels.length;
/*  876 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  878 */       String caption = LocaleResources.getString(fieldLabels[i][1], this.m_service);
/*      */ 
/*  880 */       Vector row = metaSet.createEmptyRow();
/*  881 */       row.setElementAt(fieldLabels[i][0], nameIndex);
/*  882 */       row.setElementAt(caption, captionIndex);
/*  883 */       row.setElementAt(fieldLabels[i][2], typeIndex);
/*      */ 
/*  885 */       metaSet.addRow(row);
/*      */     }
/*      */ 
/*  889 */     String str = SharedObjects.getEnvironmentValue("ExtraPreviewParameters");
/*  890 */     Vector params = StringUtils.parseArray(str, ',', '^');
/*      */ 
/*  892 */     num = params.size();
/*  893 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  895 */       String key = (String)params.elementAt(i);
/*  896 */       String val = SharedObjects.getEnvironmentValue(key);
/*  897 */       if (val == null)
/*      */         continue;
/*  899 */       prvBinder.putLocal(key, val);
/*      */     }
/*      */ 
/*  903 */     PluginFilters.filter("addExtraPreviewParameters", this.m_workspace, prvBinder, this.m_service);
/*      */   }
/*      */ 
/*      */   protected void sendPreviewRequest(Provider provider, String zipFile) throws DataException, ServiceException
/*      */   {
/*  908 */     DataBinder binder = new DataBinder(this.m_binder.getEnvironment());
/*  909 */     binder.putLocal("ZipFile:path", zipFile);
/*  910 */     binder.putLocal("IdcService", "PREVIEW");
/*  911 */     binder.m_hasAttachedFiles = true;
/*      */     try
/*      */     {
/*  915 */       doPreviewRequest(binder, provider);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  919 */       this.m_service.createServiceException(e, "!csUnableToProcessPreviewRequest");
/*      */     }
/*      */ 
/*  922 */     String resultFile = binder.getLocal("downloadFile:path");
/*  923 */     if (resultFile != null)
/*      */     {
/*  926 */       this.m_binder.addTempFile(resultFile);
/*  927 */       unpackageResultFile(resultFile);
/*      */     }
/*      */     else
/*      */     {
/*  932 */       String statusMessage = binder.getLocal("StatusMessage");
/*  933 */       if (statusMessage == null)
/*      */       {
/*  935 */         statusMessage = "!csPreviewFileCreationError";
/*      */       }
/*  937 */       this.m_service.createServiceException(null, statusMessage);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void unpackageResultFile(String resultFile)
/*      */     throws DataException, ServiceException
/*      */   {
/*  945 */     String webRoot = computeAndExtractManifest(true, false, true, resultFile);
/*      */ 
/*  947 */     this.m_binder.putLocal("EntryPointRoot", webRoot);
/*  948 */     PluginFilters.filter("postUnpackageResultFile", this.m_workspace, this.m_binder, this.m_service);
/*      */   }
/*      */ 
/*      */   protected void doPreviewRequest(DataBinder binder, Provider provider)
/*      */     throws DataException, ServiceException
/*      */   {
/*  954 */     this.m_workspace.releaseConnection();
/*  955 */     ServerRequestUtils.doAdminProxyRequest(provider, binder, binder, this.m_service);
/*      */   }
/*      */ 
/*      */   protected String getFilePath(String fileKey)
/*      */   {
/*  961 */     String filePath = this.m_binder.getAllowMissing(fileKey + ":path");
/*  962 */     if ((filePath == null) || (filePath.trim().length() == 0))
/*      */     {
/*  964 */       filePath = this.m_binder.getAllowMissing(fileKey);
/*      */     }
/*  966 */     return filePath;
/*      */   }
/*      */ 
/*      */   public String computeAndExtractManifest(boolean isMerge, boolean isEncode, boolean isExtract, String zipFile)
/*      */     throws ServiceException, DataException
/*      */   {
/*  973 */     this.m_binder.putLocal("RenditionId", "webViewableFile");
/*  974 */     Map args = new HashMap();
/*  975 */     args.put("isContainer", "1");
/*  976 */     IdcFileDescriptor d = this.m_service.m_fileStore.createDescriptor(this.m_binder, args, this.m_service);
/*      */ 
/*  978 */     String dirSuffix = "~" + this.m_binder.getLocal("dDocName").toLowerCase();
/*  979 */     String webDir = this.m_service.m_fileStore.getContainerPath(d, args, this.m_service) + dirSuffix;
/*      */ 
/*  981 */     if ((isExtract) && (zipFile != null))
/*      */     {
/*  984 */       FileUtils.checkOrCreateDirectory(webDir, 5);
/*  985 */       ZipFunctions.extractZipFiles(zipFile, webDir);
/*      */     }
/*      */ 
/*  988 */     DataBinder manifestData = ResourceUtils.readDataBinder(webDir, "manifest.hda");
/*  989 */     if (isMerge)
/*      */     {
/*  991 */       this.m_binder.merge(manifestData);
/*      */     }
/*      */ 
/*  994 */     args.put("useAbsolute", "0");
/*  995 */     String webRoot = this.m_service.m_fileStore.getClientURL(d, null, args, this.m_service);
/*  996 */     webRoot = FileUtils.getDirectory(webRoot) + "/" + dirSuffix + "/";
/*  997 */     if (isEncode)
/*      */     {
/*  999 */       String entryPoint = manifestData.getLocal("EntryPoint");
/* 1000 */       webRoot = webRoot + StringUtils.urlEncodeEx(entryPoint, false);
/*      */     }
/*      */ 
/* 1003 */     return webRoot;
/*      */   }
/*      */ 
/*      */   protected void removeKruft(DataBinder binder)
/*      */   {
/* 1009 */     String[] prefix = { "Status", "is" };
/* 1010 */     Properties props = (Properties)this.m_binder.getLocalData().clone();
/* 1011 */     for (Enumeration en = props.keys(); en.hasMoreElements(); )
/*      */     {
/* 1013 */       String key = (String)en.nextElement();
/* 1014 */       for (int i = 0; i < prefix.length; ++i)
/*      */       {
/* 1016 */         if (!key.startsWith(prefix[i]))
/*      */           continue;
/* 1018 */         this.m_binder.removeLocal(key);
/* 1019 */         break;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1027 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91866 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.preview.PreviewHandler
 * JD-Core Version:    0.5.4
 */