/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DynamicHtml;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.filestore.FileStoreProvider;
/*      */ import intradoc.filestore.FileStoreProviderHelper;
/*      */ import intradoc.filestore.FileStoreUtils;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.DataTransformationUtils;
/*      */ import intradoc.resource.ResourceCacheInfo;
/*      */ import intradoc.resource.ResourceCacheState;
/*      */ import intradoc.server.archive.ArchiveUtils;
/*      */ import intradoc.server.jsp.JspProvider;
/*      */ import intradoc.server.schema.SchemaUtils;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.FileRevisionSelectionUtils;
/*      */ import intradoc.server.utils.RevisionSelectionParameters;
/*      */ import intradoc.server.workflow.WorkflowUtils;
/*      */ import intradoc.shared.AdditionalRenditions;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.DocumentPathBuilder;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import java.io.CharConversionException;
/*      */ import java.io.File;
/*      */ import java.io.InputStream;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class FileService extends Service
/*      */ {
/*      */   public static final String JSP_GROUPS_LIST_SEPARATOR = ",";
/*   53 */   public boolean m_isWebViewable = false;
/*      */ 
/*   58 */   public boolean m_didSecurityCheck = false;
/*      */ 
/*   68 */   public boolean m_useNativeForWebViewable = false;
/*      */ 
/*   74 */   public boolean m_isWebUrlDownload = false;
/*      */ 
/*   80 */   public String m_cgiParams = null;
/*      */ 
/*   85 */   public boolean m_isJsp = false;
/*      */ 
/*   90 */   public String m_resourceId = null;
/*      */ 
/*   95 */   public ResourceContainer m_cachedResources = null;
/*      */ 
/*  100 */   public static final Object m_syncObj = new Object();
/*      */ 
/*  105 */   public JspProvider m_tomcat = null;
/*      */ 
/*  110 */   public String m_requestUri = null;
/*      */ 
/*  115 */   public boolean m_hasDynamicOption = false;
/*      */ 
/*  121 */   public String m_dynamicFileTemplateName = null;
/*      */ 
/*  126 */   public long m_currentTime = 0L;
/*      */ 
/*      */   @Deprecated
/*      */   public String getFile()
/*      */   {
/*  145 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*  146 */     if (streamWrapper == null)
/*      */     {
/*  148 */       return null;
/*      */     }
/*  150 */     if (isConditionVarTrue("ForceDownloadStreamToFilepath"))
/*      */     {
/*      */       try
/*      */       {
/*  154 */         FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/*  159 */         Report.trace("system", null, ignore);
/*      */       }
/*      */     }
/*  162 */     return streamWrapper.m_filePath;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public InputStream getFileInputStream()
/*      */   {
/*  177 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*  178 */     if (streamWrapper == null)
/*      */     {
/*  180 */       return null;
/*      */     }
/*  182 */     return streamWrapper.m_inStream;
/*      */   }
/*      */ 
/*      */   public String getDownloadName()
/*      */   {
/*  187 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*  188 */     if (streamWrapper == null)
/*      */     {
/*  190 */       return null;
/*      */     }
/*  192 */     return streamWrapper.m_clientFileName;
/*      */   }
/*      */ 
/*      */   public void setDownloadName(String downloadName)
/*      */   {
/*  197 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/*  198 */     streamWrapper.m_clientFileName = downloadName;
/*      */   }
/*      */ 
/*      */   public String getDownloadFormat()
/*      */   {
/*  203 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*  204 */     if (streamWrapper == null)
/*      */     {
/*  206 */       return null;
/*      */     }
/*  208 */     return streamWrapper.m_dataType;
/*      */   }
/*      */ 
/*      */   public void setDownloadFormat(String downloadFormat)
/*      */   {
/*  213 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/*  214 */     streamWrapper.m_dataType = downloadFormat;
/*      */   }
/*      */ 
/*      */   public boolean getSendFile()
/*      */   {
/*  221 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*  222 */     if (streamWrapper == null)
/*      */     {
/*  224 */       return false;
/*      */     }
/*  226 */     return streamWrapper.m_useStream;
/*      */   }
/*      */ 
/*      */   public void setSendFile(boolean sendFile)
/*      */   {
/*  234 */     DataStreamWrapper streamWrapper = getDownloadStream(sendFile);
/*  235 */     if (streamWrapper != null)
/*      */     {
/*  237 */       streamWrapper.m_useStream = sendFile;
/*  238 */       if (!sendFile)
/*      */         return;
/*  240 */       setDisableSendFile(false);
/*      */     }
/*      */     else {
/*  243 */       if (sendFile)
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/*  251 */       setDisableSendFile(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*  258 */     super.createHandlersForService();
/*  259 */     createHandlers("FileService");
/*  260 */     this.m_currentTime = System.currentTimeMillis();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void useNativeForWebViewable()
/*      */   {
/*  266 */     this.m_useNativeForWebViewable = true;
/*  267 */     setConditionVar("UseNativeForWebViewable", true);
/*      */   }
/*      */ 
/*      */   public void checkSecurity(ResultSet rset)
/*      */     throws ServiceException, DataException
/*      */   {
/*  275 */     boolean sendFile = getSendFile();
/*  276 */     if ((!this.m_isWebViewable) && (sendFile) && (rset != null) && (!this.m_useNativeForWebViewable))
/*      */     {
/*  281 */       boolean canDoAtReadLevel = SharedObjects.getEnvValueAsBoolean("GetCopyAccess", true);
/*  282 */       if (!canDoAtReadLevel)
/*      */       {
/*  285 */         boolean isUserInWorkflow = false;
/*  286 */         String workflowState = ResultSetUtils.getValue(rset, "dWorkflowState");
/*  287 */         if ((workflowState != null) && (workflowState.trim().length() > 0))
/*      */         {
/*  289 */           ResultSet wfSet = this.m_workspace.createResultSet("QworkflowDocInfo", this.m_binder);
/*  290 */           if ((wfSet != null) && (!wfSet.isEmpty()))
/*      */           {
/*  293 */             String wfState = ResultSetUtils.getValue(wfSet, "dWfStatus");
/*  294 */             if (!wfState.equalsIgnoreCase("INIT"))
/*      */             {
/*  296 */               DataResultSet drset = new DataResultSet();
/*  297 */               drset.copy(wfSet);
/*  298 */               this.m_binder.addResultSet("WF_INFO", drset);
/*  299 */               String user = getUserData().m_name;
/*      */ 
/*  302 */               String wfID = ResultSetUtils.getValue(drset, "dWfID");
/*  303 */               String wfCurrentStepID = ResultSetUtils.getValue(drset, "dWfCurrentStepID");
/*  304 */               String wfDir = ResultSetUtils.getValue(drset, "dWfDirectory");
/*      */ 
/*  306 */               this.m_binder.putLocal("dWfID", wfID);
/*  307 */               this.m_binder.putLocal("dWfCurrentStepID", wfCurrentStepID);
/*  308 */               this.m_binder.putLocal("dWfDirectory", wfDir);
/*      */ 
/*  310 */               ResultSet wfStepSet = this.m_workspace.createResultSet("QwfCurrentStep", this.m_binder);
/*  311 */               if ((wfStepSet != null) && (!wfStepSet.isEmpty()))
/*      */               {
/*  313 */                 drset = new DataResultSet();
/*  314 */                 drset.copy(wfStepSet);
/*  315 */                 this.m_binder.addResultSet("WorkflowStep", drset);
/*      */ 
/*  318 */                 this.m_binder.putLocal("dWfStepID", wfCurrentStepID);
/*      */ 
/*  320 */                 if (WorkflowUtils.isUserInStep(this.m_workspace, this.m_binder, user, this))
/*      */                 {
/*  322 */                   isUserInWorkflow = true;
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/*  329 */         if (!isUserInWorkflow)
/*      */         {
/*  331 */           this.m_serviceData.m_accessLevel = 2;
/*      */         }
/*      */       }
/*  334 */       if (!this.m_isWebViewable)
/*      */       {
/*  336 */         String pubType = ResultSetUtils.getValue(rset, "dPublishType");
/*  337 */         if ((pubType != null) && (pubType.equalsIgnoreCase("N")))
/*      */         {
/*  339 */           this.m_serviceData.m_accessLevel = 8;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  344 */     super.checkSecurity(rset);
/*      */ 
/*  347 */     this.m_didSecurityCheck = true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeDocID()
/*      */     throws DataException, ServiceException
/*      */   {
/*  354 */     String method = this.m_binder.getAllowMissing("RevisionSelectionMethod");
/*  355 */     if (method == null)
/*      */     {
/*  357 */       method = "Specific";
/*  358 */       String dRevLabel = this.m_binder.getAllowMissing("dRevLabel");
/*  359 */       if ((dRevLabel != null) && (dRevLabel.length() > 0))
/*      */       {
/*  361 */         method = "RevLabel";
/*      */       }
/*      */     }
/*  364 */     String query = null;
/*  365 */     String queryKey = null;
/*  366 */     int numParams = this.m_currentAction.getNumParams();
/*  367 */     if (numParams > 2)
/*      */     {
/*  369 */       query = this.m_currentAction.getParamAt(1);
/*  370 */       queryKey = this.m_currentAction.getParamAt(2);
/*      */     }
/*  372 */     RevisionSelectionParameters params = new RevisionSelectionParameters(method, query, queryKey);
/*      */ 
/*  374 */     if (numParams > 0)
/*      */     {
/*  376 */       String computationType = this.m_currentAction.getParamAt(0);
/*  377 */       params.m_computationType = computationType;
/*  378 */       params.m_isResource = ((computationType != null) && (computationType.equalsIgnoreCase("resource")));
/*      */     }
/*  380 */     params.m_currentTime = this.m_currentTime;
/*  381 */     params.m_useLatestReleasedDocInfoCache = DataBinderUtils.getBoolean(this.m_binder, "useDocInfoCacheDefault", true);
/*      */ 
/*  385 */     FileRevisionSelectionUtils.computeDocumentRevisionMethod(this.m_binder, this, this.m_workspace, params);
/*      */ 
/*  388 */     String rendition = this.m_binder.getAllowMissing("Rendition");
/*      */ 
/*  390 */     if ((rendition != null) && (rendition.equalsIgnoreCase("Web")))
/*      */     {
/*  392 */       this.m_isWebViewable = true;
/*      */     }
/*  394 */     this.m_useNativeForWebViewable = isConditionVarTrue("UseNativeForWebViewable");
/*      */ 
/*  397 */     boolean sendFile = !getDisableSendFile();
/*  398 */     if (params.m_isResource)
/*      */     {
/*  400 */       String id = "idocid://" + params.m_id;
/*      */ 
/*  402 */       id = id + "/" + method;
/*  403 */       if (rendition != null)
/*      */       {
/*  405 */         id = id + "/" + rendition;
/*      */       }
/*  407 */       this.m_resourceId = id;
/*      */ 
/*  414 */       checkForCachedResource(params);
/*  415 */       sendFile = false;
/*      */     }
/*      */ 
/*  422 */     DataStreamWrapper streamWrapper = createNewDownloadStream();
/*  423 */     streamWrapper.m_useStream = sendFile;
/*      */ 
/*  426 */     ResultSet doc_info = this.m_binder.getResultSet("DOC_INFO");
/*  427 */     if (doc_info != null)
/*      */     {
/*  429 */       this.m_binder.addResultSet(queryKey, doc_info);
/*  430 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  436 */       FileRevisionSelectionUtils.computeDocumentRevisionInfo(this.m_binder, this, this.m_workspace, params);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  441 */       createServiceException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void validateThumbnail() throws DataException, ServiceException
/*      */   {
/*  448 */     String dRendition1 = this.m_binder.get("dRendition1");
/*  449 */     boolean isValid = true;
/*  450 */     if ((dRendition1 == null) || (dRendition1.length() == 0))
/*      */     {
/*  452 */       isValid = false;
/*      */     }
/*      */     else
/*      */     {
/*  456 */       isValid = (dRendition1.equals("T")) || (dRendition1.equals("G")) || (dRendition1.equals("P"));
/*      */     }
/*      */ 
/*  459 */     if (!isValid)
/*      */     {
/*  461 */       String dID = this.m_binder.get("dID");
/*  462 */       String dDocName = this.m_binder.get("dDocName");
/*  463 */       throw new ServiceException(LocaleUtils.encodeMessage("csItemNoThumbnail", null, dID, dDocName));
/*      */     }
/*      */ 
/*  466 */     String renditionParam = this.m_binder.getAllowMissing("Rendition");
/*  467 */     String rendition = "rendition:" + dRendition1;
/*  468 */     if ((renditionParam != null) && (renditionParam.length() > 0) && (!renditionParam.equals(rendition)))
/*      */     {
/*  471 */       String dID = this.m_binder.get("dID");
/*  472 */       String dDocName = this.m_binder.get("dDocName");
/*  473 */       throw new ServiceException(LocaleUtils.encodeMessage("csItemNoThumbnailType", null, dID, dDocName, renditionParam));
/*      */     }
/*      */ 
/*  476 */     this.m_binder.putLocal("Rendition", rendition);
/*      */ 
/*  478 */     String noSaveAs = this.m_binder.getLocal("noSaveAs");
/*  479 */     if ((noSaveAs != null) && (noSaveAs.length() != 0))
/*      */       return;
/*  481 */     this.m_binder.putLocal("SuppressContentDisposition", "1");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createFileName()
/*      */     throws DataException, ServiceException
/*      */   {
/*  489 */     if (this.m_cachedResources != null)
/*      */     {
/*  491 */       return;
/*      */     }
/*      */ 
/*  494 */     DataBinder fileParams = null;
/*  495 */     String rsetName = null;
/*  496 */     boolean isTempCopy = false;
/*  497 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/*  499 */       isTempCopy = true;
/*  500 */       rsetName = this.m_currentAction.getParamAt(0);
/*  501 */       fileParams = this.m_binder.createLocaleEquivalentDataBinder();
/*  502 */       fileParams.m_isJava = this.m_binder.m_isJava;
/*  503 */       fileParams.m_isCgi = this.m_binder.m_isCgi;
/*  504 */       ResultSet drset = this.m_binder.getResultSet(rsetName);
/*  505 */       fileParams.addResultSet(rsetName, drset);
/*      */ 
/*  509 */       String dbDocName = ResultSetUtils.getValue(drset, "dDocName");
/*  510 */       if ((dbDocName == null) || (dbDocName.length() == 0))
/*      */       {
/*  512 */         Report.trace("system", "FileService.createFileName--ResultSet with name " + rsetName + " did not have a valid dDocName defined on its current row", null);
/*      */       }
/*      */ 
/*  515 */       this.m_binder.putLocal("dDocName", dbDocName);
/*      */     }
/*      */     else
/*      */     {
/*  519 */       fileParams = this.m_binder;
/*      */     }
/*      */ 
/*  524 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*      */ 
/*  531 */     String rendition = this.m_binder.getAllowMissing("Rendition");
/*  532 */     if ((rendition == null) || (rendition.equalsIgnoreCase("primary")))
/*      */     {
/*  534 */       rendition = "primaryFile";
/*      */     }
/*  536 */     boolean isPrimaryRendition = rendition.startsWith("primaryFile");
/*  537 */     if ((!isPrimaryRendition) && (this.m_useNativeForWebViewable))
/*      */     {
/*  539 */       rendition = "primaryFile";
/*  540 */       isPrimaryRendition = true;
/*      */     }
/*      */ 
/*  543 */     if (isPrimaryRendition)
/*      */     {
/*  546 */       boolean computedPath = false;
/*  547 */       boolean isAllowWebForm = isConditionVarTrue("AllowWebFormSource");
/*  548 */       streamWrapper.m_dataType = fileParams.get("dFormat");
/*      */ 
/*  550 */       if (!this.m_useNativeForWebViewable)
/*      */       {
/*  552 */         streamWrapper.m_clientFileName = fileParams.get("dOriginalName");
/*      */       }
/*  554 */       else if ((!isAllowWebForm) && (streamWrapper.m_dataType.indexOf("hcsw") >= 0))
/*      */       {
/*  556 */         computeWebDownloadFileInfo(fileParams, "web", true);
/*  557 */         boolean isInternalLoadOnly = isConditionVarTrue("IsInternalLoadOnly");
/*  558 */         if (isInternalLoadOnly)
/*      */         {
/*  563 */           FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/*      */         }
/*  565 */         computedPath = true;
/*      */       }
/*      */ 
/*  568 */       IdcFileDescriptor d = null;
/*  569 */       if (!computedPath)
/*      */       {
/*  571 */         fileParams.putLocal("RenditionId", rendition);
/*  572 */         d = this.m_fileStore.createDescriptor(fileParams, null, this);
/*  573 */         setDescriptor(d);
/*      */       }
/*      */ 
/*  576 */       if ((d != null) && (rendition.length() > "primaryFile".length()))
/*      */       {
/*  579 */         String prefix = d.getProperty("fileNamePrefix");
/*  580 */         if ((prefix != null) && (prefix.length() > 0))
/*      */         {
/*  582 */           streamWrapper.m_clientFileName = (d.getProperty("fileNamePrefix") + streamWrapper.m_clientFileName);
/*      */         }
/*      */ 
/*  585 */         computedPath = true;
/*      */       }
/*      */ 
/*  588 */       if (!computedPath)
/*      */       {
/*  590 */         boolean isInternalLoadOnly = isConditionVarTrue("IsInternalLoadOnly");
/*      */ 
/*  592 */         if (isInternalLoadOnly)
/*      */         {
/*  597 */           FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/*      */         }
/*      */ 
/*  602 */         if ((this.m_useNativeForWebViewable) && (!isConditionVarTrue("IsInternalLoadOnly")))
/*      */         {
/*  606 */           computeWebDownloadFileInfo(fileParams, "web", false);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  611 */       String filePath = (streamWrapper.m_filePath != null) ? streamWrapper.m_filePath : "<no-file>";
/*      */ 
/*  613 */       setCachedObject("PrimaryFilePath", filePath);
/*  614 */       setCachedObject("DownloadFormat", streamWrapper.m_dataType);
/*      */     }
/*      */     else
/*      */     {
/*  618 */       computeWebDownloadFileInfo(fileParams, rendition, true);
/*      */     }
/*      */ 
/*  621 */     if (this.m_isStandAlone)
/*      */     {
/*  625 */       FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/*  626 */       this.m_binder.putLocal("FilePath", streamWrapper.m_filePath);
/*      */     }
/*  628 */     if (isTempCopy)
/*      */     {
/*  631 */       String fileExt = fileParams.getAllowMissing("dExtension");
/*  632 */       if (fileExt != null)
/*      */       {
/*  634 */         this.m_binder.putLocal("dExtension", fileExt);
/*      */       }
/*      */     }
/*      */ 
/*  638 */     setCachedObject("DownloadStream", streamWrapper);
/*  639 */     setCachedObject("FileParameters", fileParams);
/*      */ 
/*  641 */     PluginFilters.filter("processDownloadStream", this.m_workspace, this.m_binder, this);
/*  642 */     PluginFilters.filter("postCreateFileName", this.m_workspace, this.m_binder, this);
/*      */   }
/*      */ 
/*      */   protected void computeWebDownloadFileInfo(DataBinder fileParams, String rendition, boolean computeFilePath)
/*      */     throws DataException, ServiceException
/*      */   {
/*  658 */     if (rendition.equalsIgnoreCase("web"))
/*      */     {
/*  660 */       rendition = "webViewableFile";
/*      */     }
/*  662 */     boolean isWebViewable = rendition.startsWith("webViewableFile");
/*  663 */     boolean isWeb = (rendition.startsWith("rendition")) || (isWebViewable);
/*      */ 
/*  665 */     ResultSet rset = executeQueryWebInfo(isWeb, rendition, fileParams);
/*      */ 
/*  668 */     String docName = fileParams.get("dDocName");
/*  669 */     String downloadFileName = docName;
/*      */ 
/*  671 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/*      */ 
/*  674 */     streamWrapper.m_dataType = ResultSetUtils.getValue(rset, "dFormat");
/*      */ 
/*  676 */     if (isWeb)
/*      */     {
/*  679 */       String downloadExtension = ResultSetUtils.getValue(rset, "dWebExtension");
/*      */ 
/*  682 */       if (computeFilePath)
/*      */       {
/*  684 */         String status = ResultSetUtils.getValue(rset, "dStatus");
/*  685 */         if ((status == null) || (status.equalsIgnoreCase("GENWWW")))
/*      */         {
/*  687 */           String dID = ResultSetUtils.getValue(rset, "dID");
/*  688 */           String errMsg = LocaleUtils.encodeMessage("csGetFileWebviewableNotAvailableForGenwww", null, dID);
/*  689 */           throw new ServiceException(-16, errMsg);
/*      */         }
/*      */ 
/*  692 */         fileParams.putLocal("RenditionId", rendition);
/*  693 */         IdcFileDescriptor d = this.m_fileStore.createDescriptor(fileParams, null, this);
/*  694 */         setDescriptor(d);
/*  695 */         String descriptorWebExtension = (String)d.get("dWebExtension");
/*  696 */         if (descriptorWebExtension != null)
/*      */         {
/*  699 */           downloadExtension = descriptorWebExtension;
/*      */         }
/*  701 */         if (rendition.startsWith("rendition"))
/*      */         {
/*  703 */           DataBinder renditionBinder = new DataBinder(this.m_binder.getLocalData());
/*  704 */           AdditionalRenditions renditions = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*      */ 
/*  706 */           String renditionFlag = rendition.substring("rendition".length() + 1);
/*  707 */           String renditionExtension = renditions.getExtension(renditionFlag);
/*  708 */           downloadFileName = docName + "-" + renditionFlag;
/*  709 */           downloadExtension = renditionExtension;
/*  710 */           String path = (String)d.get("path");
/*  711 */           String[] dummyConvType = { "" };
/*  712 */           DocFormats docFormats = (DocFormats)SharedObjects.getTable("DocFormats");
/*  713 */           if (docFormats == null)
/*      */           {
/*  715 */             throw new DataException("!csCheckinFormatsMissing");
/*      */           }
/*  717 */           renditionBinder.putLocal("dExtension", renditionExtension);
/*  718 */           String format = docFormats.determineFormat(renditionBinder, dummyConvType, null, false, path);
/*  719 */           streamWrapper.m_dataType = format;
/*      */         }
/*      */       }
/*  722 */       fileParams.putLocal("dExtension", downloadExtension);
/*  723 */       if ((downloadExtension != null) && (downloadExtension.length() > 0))
/*      */       {
/*  725 */         downloadExtension = "." + downloadExtension;
/*      */       }
/*      */ 
/*  728 */       streamWrapper.m_clientFileName = (downloadFileName + downloadExtension);
/*      */ 
/*  730 */       if (!isConditionVarTrue("IsInternalLoadOnly"))
/*      */       {
/*  733 */         prepareWebViewableDeliveryEx(fileParams);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  738 */       if (rendition.equalsIgnoreCase("alternate"))
/*      */       {
/*  740 */         rendition = "alternateFile";
/*      */       }
/*  742 */       if (!rendition.startsWith("alternateFile"))
/*      */       {
/*  744 */         String msg = LocaleUtils.encodeMessage("csGetFileRenditionNotAllowed", null, docName, rendition);
/*  745 */         throw new DataException(msg);
/*      */       }
/*  747 */       streamWrapper.m_clientFileName = null;
/*  748 */       if (!SharedObjects.getEnvValueAsBoolean("NoOriginalFileNameForAlternateDownload", false))
/*      */       {
/*  750 */         streamWrapper.m_clientFileName = ResultSetUtils.getValue(rset, "dOriginalName");
/*      */       }
/*  752 */       boolean createDownloadName = (streamWrapper.m_clientFileName == null) || (streamWrapper.m_clientFileName.length() == 0);
/*      */ 
/*  754 */       String extension = ResultSetUtils.getValue(rset, "dExtension");
/*      */ 
/*  757 */       if (extension != null)
/*      */       {
/*  759 */         fileParams.putLocal("dExtension", extension);
/*  760 */         if (createDownloadName)
/*      */         {
/*  762 */           streamWrapper.m_clientFileName = (docName + "." + extension);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  767 */         fileParams.removeLocal("dExtension");
/*  768 */         if (createDownloadName)
/*      */         {
/*  770 */           streamWrapper.m_clientFileName = docName;
/*      */         }
/*      */       }
/*      */ 
/*  774 */       if (!computeFilePath)
/*      */         return;
/*  776 */       fileParams.putLocal("RenditionId", "alternateFile");
/*  777 */       IdcFileDescriptor d = this.m_fileStore.createDescriptor(fileParams, null, this);
/*  778 */       setDescriptor(d);
/*      */ 
/*  781 */       if ((rendition.length() <= "alternateFile".length()) || (streamWrapper.m_clientFileName == null)) {
/*      */         return;
/*      */       }
/*  784 */       String prefix = d.getProperty("fileNamePrefix");
/*  785 */       if ((prefix == null) || (prefix.length() <= 0))
/*      */         return;
/*  787 */       streamWrapper.m_clientFileName = (d.getProperty("fileNamePrefix") + streamWrapper.m_clientFileName);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected ResultSet executeQueryWebInfo(boolean isWeb, String rendition, DataBinder fileParams)
/*      */     throws DataException, ServiceException
/*      */   {
/*  799 */     fileParams.putLocal("docmetaColumns", "DocMeta.dID");
/*  800 */     ResultSet rset = this.m_workspace.createResultSet((isWeb) ? "QdocWebInfo" : "QalternateDocument", fileParams);
/*      */ 
/*  802 */     fileParams.removeLocal("docmetaColumns");
/*      */ 
/*  804 */     if (!rset.isRowPresent())
/*      */     {
/*  806 */       if (rendition == null)
/*      */       {
/*  808 */         rendition = "web";
/*      */       }
/*  810 */       String docName = fileParams.get("dDocName");
/*  811 */       String msg = LocaleUtils.encodeMessage("csGetFileRenditionNotFound", null, docName, "csGetFileRenditionLabel_" + rendition.toLowerCase());
/*      */ 
/*  813 */       throw new ServiceException(msg);
/*      */     }
/*  815 */     return rset;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createWebFileNameFromRelativeUrl() throws DataException, ServiceException
/*      */   {
/*  821 */     String relativeRoot = this.m_binder.getLocal("fileUrl");
/*  822 */     String parameters = null;
/*  823 */     if (relativeRoot == null)
/*      */     {
/*  830 */       relativeRoot = this.m_binder.getEnvironmentValue("FILE_URI");
/*  831 */       if (relativeRoot == null)
/*      */       {
/*  833 */         throw new DataException(null, "syMissingArgument", new Object[] { "fileUrl" });
/*      */       }
/*  835 */       parameters = this.m_binder.getEnvironmentValue("FILE_QUERY");
/*  836 */       if ((parameters != null) && (parameters.length() == 0))
/*      */       {
/*  838 */         parameters = null;
/*      */       }
/*      */     }
/*      */ 
/*  842 */     DataBinder binder = null;
/*  843 */     String rsetName = null;
/*  844 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/*  846 */       rsetName = this.m_currentAction.getParamAt(0);
/*  847 */       binder = new DataBinder();
/*      */     }
/*      */     else
/*      */     {
/*  851 */       binder = this.m_binder.createShallowCopy();
/*      */     }
/*      */ 
/*  856 */     boolean forceLogin = false;
/*  857 */     String authType = null;
/*      */ 
/*  859 */     if (parameters == null)
/*      */     {
/*  861 */       int index = relativeRoot.indexOf(63);
/*  862 */       if (index > 0)
/*      */       {
/*  864 */         parameters = relativeRoot.substring(index + 1);
/*  865 */         relativeRoot = relativeRoot.substring(0, index);
/*  866 */         this.m_cgiParams = parameters;
/*      */       }
/*      */     }
/*  869 */     if (parameters != null)
/*      */     {
/*  872 */       DataBinder tempBinder = this.m_binder.createShallowCopy();
/*  873 */       tempBinder.m_determinedEncoding = this.m_binder.m_determinedEncoding;
/*  874 */       tempBinder.m_clientEncoding = this.m_binder.m_clientEncoding;
/*  875 */       tempBinder.setEncodeFlags(true, false);
/*      */ 
/*  877 */       DataSerializeUtils.parseLocalParameters(tempBinder, parameters, "&", null);
/*      */ 
/*  882 */       forceLogin = StringUtils.convertToBool(tempBinder.getLocal("forceLogin"), false);
/*  883 */       authType = tempBinder.getLocal("Auth");
/*  884 */       this.m_pageMerger.checkConfigInit();
/*      */     }
/*      */ 
/*  887 */     this.m_requestUri = relativeRoot;
/*      */ 
/*  890 */     int index = relativeRoot.indexOf(35);
/*  891 */     if (index > 0)
/*      */     {
/*  893 */       String controls = relativeRoot.substring(index + 1);
/*  894 */       this.m_binder.putLocal("addonPath", controls);
/*      */ 
/*  896 */       relativeRoot = relativeRoot.substring(0, index);
/*      */     }
/*      */ 
/*  903 */     index = relativeRoot.indexOf(59);
/*  904 */     if (index > 0)
/*      */     {
/*  906 */       String sessionInfo = relativeRoot.substring(index + 1);
/*  907 */       this.m_binder.putLocal("ClientSessionID", sessionInfo);
/*      */ 
/*  909 */       relativeRoot = relativeRoot.substring(0, index);
/*      */     }
/*      */ 
/*  913 */     boolean hasSecurityInfo = this.m_fileUtils.parseDocInfoFromInternalPath(relativeRoot, binder.getLocalData(), this);
/*      */ 
/*  917 */     DataResultSet drset = null;
/*  918 */     if (hasSecurityInfo)
/*      */     {
/*  920 */       String dExtension = binder.getLocal("dExtension");
/*  921 */       drset = this.m_fileUtils.createFileReference(binder.getLocalData(), this.m_binder, this.m_workspace, this, false);
/*      */ 
/*  923 */       if (drset != null)
/*      */       {
/*  925 */         String dWebExtension = ResultSetUtils.getValue(drset, "dWebExtension");
/*  926 */         if (!dExtension.equals(dWebExtension))
/*      */         {
/*  928 */           drset = null;
/*      */         }
/*      */       }
/*      */     }
/*  932 */     if (drset == null)
/*      */     {
/*  937 */       String intradocRelativeUrlRoot = DocumentPathBuilder.getRelativeWebRoot();
/*  938 */       String weblayoutRelativePath = relativeRoot.substring(intradocRelativeUrlRoot.length());
/*  939 */       boolean isTargetingServer = true;
/*  940 */       if (relativeRoot.length() < intradocRelativeUrlRoot.length())
/*      */       {
/*  942 */         isTargetingServer = false;
/*      */       }
/*      */ 
/*  945 */       if (isTargetingServer)
/*      */       {
/*  947 */         String prefix = relativeRoot.substring(0, intradocRelativeUrlRoot.length());
/*  948 */         if (!prefix.equalsIgnoreCase(intradocRelativeUrlRoot))
/*      */         {
/*  950 */           isTargetingServer = false;
/*      */         }
/*      */       }
/*  953 */       if (!isTargetingServer)
/*      */       {
/*  955 */         String msg = LocaleUtils.encodeMessage("csFileServiceRelUrlPrefixError", null, intradocRelativeUrlRoot);
/*      */ 
/*  957 */         createServiceExceptionEx(null, msg, -16);
/*      */       }
/*      */ 
/*  960 */       String webPath = SharedObjects.getEnvironmentValue("WeblayoutDir") + weblayoutRelativePath;
/*      */ 
/*  962 */       webPath = FileUtils.fileSlashes(webPath);
/*      */ 
/*  965 */       if (webPath.indexOf("..") >= 0)
/*      */       {
/*  971 */         String msg = LocaleUtils.encodeMessage("syFileDoesNotExist", null, webPath);
/*      */ 
/*  974 */         createServiceExceptionEx(null, msg, -16);
/*      */       }
/*  976 */       setFile(webPath);
/*      */     }
/*  978 */     this.m_isWebViewable = true;
/*  979 */     this.m_isWebUrlDownload = true;
/*      */ 
/*  982 */     if (rsetName != null)
/*      */     {
/*  984 */       if (drset == null)
/*      */       {
/*  986 */         drset = ResultSetUtils.createResultSetFromProperties(binder.getLocalData());
/*      */       }
/*  988 */       this.m_binder.addResultSet(rsetName, drset);
/*      */     }
/*      */ 
/*  991 */     UserData userData = getUserData();
/*  992 */     String user = userData.m_name;
/*  993 */     if ((((user == null) || (user.length() == 0) || (user.equals("anonymous")))) && ((
/*  995 */       (forceLogin) || ((authType != null) && (authType.length() > 0)))))
/*      */     {
/*  997 */       setPromptForLogin(true);
/*  998 */       createServiceException(null, "!csSystemNeedsLogin");
/*      */     }
/*      */ 
/* 1002 */     if (!hasSecurityInfo)
/*      */     {
/* 1006 */       String group = binder.getLocal("dSecurityGroup");
/* 1007 */       if ((group != null) && (group.length() > 0))
/*      */       {
/* 1009 */         hasSecurityInfo = true;
/*      */       }
/*      */     }
/*      */ 
/* 1013 */     if (hasSecurityInfo)
/*      */     {
/* 1015 */       checkSecurity();
/*      */     }
/*      */     else
/*      */     {
/* 1020 */       executeFilter("unmanagedDocCheckAccess");
/*      */     }
/*      */ 
/* 1024 */     prepareWebViewableDeliveryEx(binder);
/*      */ 
/* 1028 */     checkForCustomErrorDelivery(relativeRoot, binder);
/*      */ 
/* 1031 */     if (rsetName == null)
/*      */       return;
/* 1033 */     ResultSet rset = this.m_binder.removeResultSet(rsetName);
/* 1034 */     if ((rset == null) || (!rset.isRowPresent()))
/*      */       return;
/* 1036 */     int n = rset.getNumFields();
/* 1037 */     FieldInfo fi = new FieldInfo();
/* 1038 */     for (int i = 0; i < n; ++i)
/*      */     {
/* 1040 */       rset.getIndexFieldInfo(i, fi);
/* 1041 */       String refFieldName = "ref:" + fi.m_name;
/* 1042 */       this.m_binder.putLocal(refFieldName, rset.getStringValue(i));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkForCustomErrorDelivery(String weblayoutRelativePath, DataBinder fileParams)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1053 */     String extension = FileUtils.getExtension(weblayoutRelativePath);
/* 1054 */     if ((extension == null) || (!extension.equals("js")))
/*      */       return;
/* 1056 */     String relativeSchemaPath = null;
/* 1057 */     String testSchemaPath = "resources/schema/views/";
/* 1058 */     int index = weblayoutRelativePath.indexOf(testSchemaPath);
/* 1059 */     if (index >= 0)
/*      */     {
/* 1061 */       relativeSchemaPath = weblayoutRelativePath.substring(index + testSchemaPath.length());
/*      */ 
/* 1064 */       index = weblayoutRelativePath.indexOf("/", index + 1);
/*      */     }
/* 1066 */     if (index < 0) {
/*      */       return;
/*      */     }
/* 1069 */     String relationName = null;
/* 1070 */     String parentValue = null;
/* 1071 */     if (relativeSchemaPath.endsWith(".js"))
/*      */     {
/* 1073 */       relativeSchemaPath = relativeSchemaPath.substring(0, relativeSchemaPath.length() - 3);
/*      */     }
/*      */ 
/* 1076 */     String msg = LocaleUtils.encodeMessage("csSchemaUnableToParsePath", null, relativeSchemaPath);
/*      */     String viewName;
/*      */     String viewName;
/* 1078 */     if (relativeSchemaPath.endsWith("/all"))
/*      */     {
/* 1080 */       viewName = relativeSchemaPath.substring(0, relativeSchemaPath.length() - 4);
/*      */     }
/*      */     else
/*      */     {
/* 1084 */       index = relativeSchemaPath.indexOf("/");
/* 1085 */       if (index < 0)
/*      */       {
/* 1087 */         throw new ServiceException(msg);
/*      */       }
/* 1089 */       viewName = relativeSchemaPath.substring(0, index++);
/* 1090 */       int index2 = relativeSchemaPath.indexOf("/", index);
/* 1091 */       if (index2 < 0)
/*      */       {
/* 1093 */         throw new ServiceException(msg);
/*      */       }
/* 1095 */       relationName = relativeSchemaPath.substring(index, index2++);
/* 1096 */       parentValue = relativeSchemaPath.substring(index2);
/*      */     }
/*      */     try
/*      */     {
/* 1100 */       viewName = StringUtils.decodeJavascriptFilename(viewName);
/* 1101 */       relationName = StringUtils.decodeJavascriptFilename(relationName);
/* 1102 */       if ((parentValue != null) && (!parentValue.startsWith("@@")))
/*      */       {
/* 1104 */         parentValue = StringUtils.decodeJavascriptFilename(parentValue);
/*      */       }
/*      */     }
/*      */     catch (CharConversionException e)
/*      */     {
/* 1109 */       Report.trace("schemapagecreation", null, e);
/* 1110 */       throw new ServiceException(msg, e);
/*      */     }
/*      */ 
/* 1113 */     SchemaHelper helper = (SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null);
/*      */ 
/* 1117 */     SchemaUtils utils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
/*      */ 
/* 1120 */     utils.init();
/*      */ 
/* 1123 */     this.m_hasDynamicOption = true;
/* 1124 */     this.m_dynamicFileTemplateName = utils.prepareSendSchemaViewFragment(this, this.m_binder, helper, viewName, relationName, parentValue);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void prepareWebViewableDelivery()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1134 */     DataBinder fileParams = null;
/* 1135 */     String rsetName = null;
/* 1136 */     if (this.m_currentAction.getNumParams() > 0)
/*      */     {
/* 1138 */       rsetName = this.m_currentAction.getParamAt(0);
/* 1139 */       fileParams = new DataBinder();
/* 1140 */       ResultSet drset = this.m_binder.getResultSet(rsetName);
/* 1141 */       fileParams.addResultSet(rsetName, drset);
/*      */     }
/*      */     else
/*      */     {
/* 1145 */       fileParams = this.m_binder;
/*      */     }
/* 1147 */     prepareWebViewableDeliveryEx(fileParams);
/*      */   }
/*      */ 
/*      */   public void prepareWebViewableDeliveryEx(DataBinder fileParams)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1155 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/* 1156 */     if ((streamWrapper == null) || (streamWrapper.m_descriptor == null) || (streamWrapper.m_streamId == null))
/*      */     {
/* 1160 */       throw new ServiceException("!csFileServiceCannotPrepareWebViewable");
/*      */     }
/*      */ 
/* 1164 */     String downloadName = streamWrapper.m_clientFileName;
/* 1165 */     String fileId = streamWrapper.m_filePath;
/* 1166 */     if (fileId == null)
/*      */     {
/* 1168 */       fileId = streamWrapper.m_streamId;
/*      */     }
/* 1170 */     if ((downloadName == null) || (downloadName.length() == 0))
/*      */     {
/* 1172 */       downloadName = FileUtils.getName(fileId);
/*      */     }
/*      */ 
/* 1175 */     String originalExtension = this.m_binder.getLocal("dExtension");
/* 1176 */     if ((originalExtension == null) || (originalExtension.length() == 0))
/*      */     {
/* 1178 */       originalExtension = FileUtils.getExtension(downloadName);
/*      */     }
/* 1180 */     streamWrapper.m_clientFileName = downloadName;
/*      */ 
/* 1187 */     String extension = originalExtension.toLowerCase();
/*      */ 
/* 1189 */     boolean sendFile = true;
/* 1190 */     boolean isRegularTemplate = extension.equals("hcst");
/* 1191 */     boolean isXmlTemplate = (!isRegularTemplate) && (((extension.equals("hcsp")) || (extension.equals("hcsf"))));
/* 1192 */     boolean isJsp = false;
/* 1193 */     if ((!isRegularTemplate) && (!isXmlTemplate))
/*      */     {
/* 1201 */       boolean isJspServerEnabled = SharedObjects.getEnvValueAsBoolean("IsJspServerEnabled", false);
/*      */ 
/* 1203 */       boolean isExtensionJsp = extension.startsWith("jsp");
/* 1204 */       boolean isOcsh = fileId.indexOf("_ocsh") >= 0;
/* 1205 */       boolean isJspEnabledGroup = false;
/*      */ 
/* 1211 */       String dSecurityGroup = null;
/*      */ 
/* 1214 */       ResultSet docInfo = this.m_binder.getResultSet("TEMPLATE_URL_INFO");
/* 1215 */       if (docInfo == null)
/*      */       {
/* 1217 */         docInfo = this.m_binder.getResultSet("DOC_INFO");
/*      */       }
/*      */ 
/* 1220 */       if (docInfo != null)
/*      */       {
/* 1222 */         dSecurityGroup = docInfo.getStringValueByName("dSecurityGroup");
/*      */       }
/*      */ 
/* 1227 */       if (dSecurityGroup == null)
/*      */       {
/* 1229 */         isJsp = false;
/*      */       }
/* 1234 */       else if ((isJspServerEnabled) && (((isExtensionJsp) || (isOcsh))))
/*      */       {
/* 1236 */         String jspGroupsEnv = SharedObjects.getEnvironmentValue("JspEnabledGroups");
/*      */ 
/* 1239 */         if ((jspGroupsEnv != null) && (jspGroupsEnv.length() > 0))
/*      */         {
/* 1243 */           String[] jspGroupsList = jspGroupsEnv.split(",");
/*      */ 
/* 1245 */           int jspGroupsListLength = jspGroupsList.length;
/* 1246 */           for (int i = 0; i < jspGroupsListLength; ++i)
/*      */           {
/* 1248 */             jspGroupsList[i] = jspGroupsList[i].trim();
/*      */           }
/*      */ 
/* 1253 */           if (dSecurityGroup != null)
/*      */           {
/* 1255 */             for (int i = 0; i < jspGroupsListLength; ++i)
/*      */             {
/* 1257 */               if (!dSecurityGroup.equals(jspGroupsList[i]))
/*      */                 continue;
/* 1259 */               isJspEnabledGroup = true;
/* 1260 */               break;
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1265 */         isJsp = isJspEnabledGroup;
/*      */       }
/*      */     }
/* 1268 */     if ((isRegularTemplate) || (isXmlTemplate) || (isJsp))
/*      */     {
/* 1270 */       FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/* 1271 */       if ((isRegularTemplate) || (isXmlTemplate))
/*      */       {
/* 1275 */         DynamicHtml dynHtml = null;
/* 1276 */         String filePath = null;
/*      */         try
/*      */         {
/* 1280 */           filePath = streamWrapper.m_filePath;
/* 1281 */           dynHtml = DataLoader.loadDynamicPage(filePath, filePath, this.m_currentTime, isXmlTemplate, this);
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/* 1285 */           createServiceException(e, null);
/*      */         }
/*      */ 
/* 1289 */         if (!DataBinderUtils.getBoolean(this.m_binder, "isResourceInclude", false))
/*      */         {
/*      */           try
/*      */           {
/* 1294 */             DataTransformationUtils.mergeInDynamicData(this.m_workspace, this.m_binder, dynHtml);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/* 1299 */             String msg = LocaleUtils.encodeMessage("csFileServiceDynamicAppFileError", e.getMessage(), filePath);
/*      */ 
/* 1301 */             this.m_binder.putLocal("StatusCode", "-1");
/* 1302 */             this.m_binder.putLocal("StatusMessageKey", msg);
/* 1303 */             this.m_binder.putLocal("StatusMessage", msg);
/* 1304 */             Report.trace(null, LocaleResources.localizeMessage(msg, new ExecutionContextAdaptor()), e);
/*      */           }
/*      */         }
/*      */ 
/* 1308 */         setCachedObject("ParsedResponseTemplate", dynHtml);
/*      */       }
/*      */       else
/*      */       {
/* 1312 */         this.m_isJsp = true;
/*      */       }
/*      */ 
/* 1315 */       streamWrapper.m_dataType = ("text/" + extension);
/* 1316 */       this.m_binder.putLocal("TemplateClass", "IdcDynamicFile");
/* 1317 */       this.m_binder.putLocal("TemplateType", extension);
/*      */ 
/* 1320 */       if (!DataBinderUtils.getBoolean(this.m_binder, "isResourceInclude", false))
/*      */       {
/* 1323 */         executeService("LOAD_DOC_ENVIRONMENT");
/* 1324 */         executeFilter("dynamicFileLoadDocEnvironment");
/*      */       }
/*      */ 
/* 1327 */       sendFile = false;
/*      */     }
/*      */ 
/* 1331 */     streamWrapper.m_useStream = sendFile;
/*      */ 
/* 1333 */     setCachedObject("FileParams", fileParams);
/* 1334 */     if (!executeFilter("prepareCustomWebViewableDelivery"))
/*      */     {
/* 1336 */       return;
/*      */     }
/*      */ 
/* 1340 */     String downloadFormat = streamWrapper.m_dataType;
/* 1341 */     if (((downloadFormat != null) && (!downloadFormat.equals("application/x-unknown"))) || 
/* 1343 */       (extension.length() <= 0))
/*      */       return;
/* 1345 */     this.m_binder.putLocal("dExtension", extension);
/* 1346 */     DocFormats docFormats = (DocFormats)SharedObjects.getTable("DocFormats");
/* 1347 */     if (docFormats != null)
/*      */     {
/* 1349 */       String[] conv = new String[1];
/* 1350 */       downloadFormat = docFormats.determineFormat(this.m_binder, conv, null, false, "webFile");
/*      */     }
/*      */     else
/*      */     {
/* 1354 */       downloadFormat = "application/" + extension;
/*      */     }
/* 1356 */     streamWrapper.m_dataType = downloadFormat;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void createArchiveFileName()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1368 */     String archiveLocation = this.m_binder.get("dArchiveName");
/* 1369 */     String[] locData = ArchiveUtils.parseLocation(archiveLocation);
/* 1370 */     String collectionName = locData[0];
/* 1371 */     String archiveName = locData[1];
/*      */ 
/* 1374 */     CollectionData collData = ArchiveUtils.getCollection(collectionName);
/* 1375 */     String archiveExportDir = collData.m_exportLocation + archiveName.toLowerCase();
/* 1376 */     String batchFile = this.m_binder.get("dBatchFile");
/*      */ 
/* 1379 */     DataResultSet fileSet = ArchiveUtils.readBatchFile(archiveExportDir, batchFile);
/*      */ 
/* 1381 */     FieldInfo info = new FieldInfo();
/* 1382 */     fileSet.getFieldInfo("dID", info);
/*      */ 
/* 1385 */     String id = this.m_binder.get("dID");
/* 1386 */     Vector row = fileSet.findRow(info.m_index, id);
/* 1387 */     if (row == null)
/*      */     {
/* 1389 */       createServiceException(null, "!csFileServiceUnableToFindFileInfo");
/*      */     }
/*      */ 
/* 1392 */     String rendition = this.m_binder.getAllowMissing("Rendition");
/* 1393 */     if (rendition == null)
/*      */     {
/* 1395 */       throw new DataException("!csGetFileUnknownRendition");
/*      */     }
/*      */ 
/* 1398 */     String key = null;
/*      */ 
/* 1400 */     if (rendition.equalsIgnoreCase("web"))
/*      */     {
/* 1402 */       key = "webViewableFile";
/*      */     }
/* 1404 */     else if (rendition.equalsIgnoreCase("primary"))
/*      */     {
/* 1406 */       key = "primaryFile";
/*      */     }
/* 1408 */     else if (rendition.equalsIgnoreCase("alternate"))
/*      */     {
/* 1410 */       key = "alternateFile";
/*      */     }
/*      */ 
/* 1413 */     if (key == null)
/*      */     {
/* 1415 */       throw new DataException("!csGetFileUnknownRendition");
/*      */     }
/*      */ 
/* 1419 */     String filename = ResultSetUtils.getValue(fileSet, key);
/* 1420 */     if ((filename == null) || (filename.length() == 0))
/*      */     {
/* 1422 */       String msg = LocaleUtils.encodeMessage("csFileServiceRenditionNotAvailable", null, "csGetFileRenditionLabel_" + rendition.toLowerCase());
/*      */ 
/* 1424 */       createServiceException(null, msg);
/*      */     }
/*      */ 
/* 1427 */     String originalName = ResultSetUtils.getValue(fileSet, "dOriginalName");
/* 1428 */     setFile(archiveExportDir + "/" + filename);
/* 1429 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/* 1430 */     streamWrapper.m_clientFileName = ((originalName != null) ? originalName : filename);
/* 1431 */     streamWrapper.m_dataType = ResultSetUtils.getValue(fileSet, key + ":format");
/*      */   }
/*      */ 
/*      */   protected void checkForCachedResource(RevisionSelectionParameters params) throws DataException
/*      */   {
/* 1436 */     if (this.m_resourceId == null)
/*      */     {
/* 1438 */       return;
/*      */     }
/* 1440 */     ResourceCacheInfo cacheInfo = ResourceCacheState.getTemporaryCache(this.m_resourceId, this.m_currentTime);
/* 1441 */     if ((cacheInfo == null) || (cacheInfo.m_agedTS <= this.m_currentTime) || (cacheInfo.m_agedTS <= 0L) || (cacheInfo.m_associatedInfo == null) || (!cacheInfo.m_associatedInfo instanceof DataResultSet) || (cacheInfo.m_resourceObj == null) || (!cacheInfo.m_resourceObj instanceof ResourceContainer))
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/* 1447 */     DataResultSet drset = (DataResultSet)cacheInfo.m_associatedInfo;
/* 1448 */     this.m_cachedResources = ((ResourceContainer)cacheInfo.m_resourceObj);
/* 1449 */     params.m_haveDocInfo = true;
/* 1450 */     params.m_haveRevID = true;
/* 1451 */     params.m_docInfo = drset.shallowClone();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void checkForRefreshingCachedResources()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1458 */     ResourceContainer res = this.m_cachedResources;
/*      */ 
/* 1460 */     if (res == null)
/*      */     {
/* 1462 */       if (this.m_resourceId == null)
/*      */       {
/* 1464 */         throw new DataException("!csFileServiceIdNotComputed");
/*      */       }
/* 1466 */       DataStreamWrapper streamWrapper = getDownloadStream(false);
/*      */ 
/* 1469 */       if ((streamWrapper == null) || (streamWrapper.m_descriptor == null) || (streamWrapper.m_streamId == null) || (streamWrapper.m_dataType == null))
/*      */       {
/* 1472 */         throw new ServiceException("!csFileServiceFileRefNotDetermined");
/*      */       }
/* 1474 */       if (!streamWrapper.m_dataType.toLowerCase().startsWith("application/idoc"))
/*      */       {
/* 1476 */         createServiceException(null, "!csFileServiceBadFormatOrExtension");
/*      */       }
/*      */ 
/* 1480 */       DataResultSet drset = null;
/* 1481 */       int numParams = this.m_currentAction.getNumParams();
/* 1482 */       if (numParams > 0)
/*      */       {
/* 1485 */         String queryKey = this.m_currentAction.getParamAt(0);
/* 1486 */         drset = (DataResultSet)this.m_binder.getResultSet(queryKey);
/*      */       }
/*      */ 
/* 1489 */       FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/* 1490 */       res = DataLoader.loadDynamicResource(this.m_resourceId, streamWrapper.m_filePath, drset.shallowClone(), this.m_currentTime, this);
/*      */     }
/*      */ 
/* 1496 */     if (res == null)
/*      */       return;
/* 1498 */     setCachedObject("ResourceContainer", res);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void appendFileCachingMessage()
/*      */   {
/* 1505 */     boolean isFullReport = StringUtils.convertToBool(this.m_binder.getLocal("FullPageCacheReport"), false);
/*      */ 
/* 1507 */     ResourceCacheState.getReport(isFullReport, this.m_binder);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   @IdcServiceAction
/*      */   public void copyDownloadFileInfoToParent()
/*      */   {
/*      */   }
/*      */ 
/*      */   public void doResponse(boolean isError, ServiceException err)
/*      */     throws ServiceException
/*      */   {
/* 1529 */     Object[] filterParams = { new Boolean(isError), err };
/* 1530 */     setCachedObject("preDoResponse:parameters", filterParams);
/* 1531 */     if (!executeFilter("preDoFileResponse"))
/*      */     {
/* 1533 */       return;
/*      */     }
/* 1535 */     isError = ((Boolean)filterParams[0]).booleanValue();
/* 1536 */     err = (ServiceException)filterParams[1];
/*      */ 
/* 1538 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*      */ 
/* 1541 */     boolean isRealStream = (streamWrapper != null) && (streamWrapper.m_descriptor != null) && (streamWrapper.m_streamId != null);
/*      */ 
/* 1543 */     if ((!isRealStream) && 
/* 1545 */       (streamWrapper != null) && (streamWrapper.m_useStream))
/*      */     {
/* 1547 */       Report.trace("system", "DataStreamWrapper not completely filled out", null);
/* 1548 */       streamWrapper.m_useStream = false;
/*      */     }
/*      */ 
/* 1553 */     String section = this.m_binder.getLocal("Section");
/* 1554 */     if (((section != null) && (section.equals("data"))) || (StringUtils.convertToBool(this.m_binder.getLocal("IsXml"), false)))
/*      */     {
/* 1557 */       setConditionVar("getDataSection", true);
/*      */       try
/*      */       {
/* 1560 */         FileStoreUtils.forceDownloadStreamToFilePath(streamWrapper, this.m_fileStore, this);
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1564 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/* 1572 */       this.m_binder.putLocal("xmlFilePath", streamWrapper.m_filePath);
/* 1573 */       buildResponsePage(isError);
/* 1574 */       return;
/*      */     }
/*      */ 
/* 1578 */     if ((this.m_isJsp) && (!isError))
/*      */     {
/* 1580 */       executeJsp();
/* 1581 */       return;
/*      */     }
/*      */ 
/* 1585 */     boolean sendFile = (streamWrapper != null) && (streamWrapper.m_useStream);
/* 1586 */     if ((sendFile) && (!isError) && 
/* 1588 */       (this.m_binder.getLocal("ResourceTemplate") != null))
/*      */     {
/* 1590 */       streamWrapper.m_useStream = false;
/* 1591 */       sendFile = false;
/*      */     }
/*      */ 
/* 1595 */     boolean setMessage = false;
/* 1596 */     String oldMsg = "";
/* 1597 */     boolean resetNoSaveAs = false;
/*      */     try
/*      */     {
/* 1601 */       if (sendFile)
/*      */       {
/* 1603 */         oldMsg = (String)getCachedObject("ParentPageMessage");
/* 1604 */         prepareForFileResponse(oldMsg);
/* 1605 */         setMessage = true;
/*      */ 
/* 1608 */         if (this.m_isWebUrlDownload)
/*      */         {
/* 1610 */           String curNoSaveAs = this.m_binder.getLocal("noSaveAs");
/* 1611 */           if ((curNoSaveAs == null) || (curNoSaveAs.length() == 0))
/*      */           {
/* 1613 */             this.m_binder.putLocal("noSaveAs", "1");
/* 1614 */             resetNoSaveAs = true;
/*      */           }
/*      */         }
/*      */       }
/* 1618 */       super.doResponse(isError, err);
/*      */     }
/*      */     finally
/*      */     {
/* 1622 */       if (resetNoSaveAs)
/*      */       {
/* 1624 */         this.m_binder.putLocal("noSaveAs", "");
/*      */       }
/* 1626 */       if ((setMessage) && 
/* 1628 */         (oldMsg != null))
/*      */       {
/* 1630 */         setCachedObject("ParentPageMessage", oldMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareForFileResponse(String oldMsg)
/*      */     throws ServiceException
/*      */   {
/* 1640 */     DataStreamWrapper streamWrapper = getDownloadStream(false);
/*      */     try
/*      */     {
/* 1643 */       if (PluginFilters.filter("prepareForFileResponse", this.m_workspace, this.m_binder, this) == -1)
/*      */       {
/* 1645 */         return;
/*      */       }
/* 1647 */       boolean sendFile = (streamWrapper != null) && (streamWrapper.m_useStream);
/*      */ 
/* 1649 */       if ((this.m_hasDynamicOption) && (this.m_dynamicFileTemplateName != null) && (sendFile))
/*      */       {
/* 1651 */         this.m_serviceData.m_htmlPage = this.m_dynamicFileTemplateName;
/*      */ 
/* 1655 */         this.m_binder.putLocal("fileErrorStartResponse", "HTTP/1.1 200 OK\r\n");
/* 1656 */         this.m_binder.putLocal("fileErrorOverridePage", this.m_dynamicFileTemplateName);
/*      */ 
/* 1659 */         if (!determineExists(streamWrapper))
/*      */         {
/* 1661 */           Report.trace("pagecreation", "Static file " + streamWrapper.m_streamId + " not found, using template " + this.m_dynamicFileTemplateName + " instead.", null);
/*      */ 
/* 1667 */           streamWrapper.m_useStream = false;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1674 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/* 1677 */     String streamId = "<no-file>";
/* 1678 */     if ((streamWrapper != null) && (streamWrapper.m_streamId != null))
/*      */     {
/* 1680 */       streamId = streamWrapper.m_streamId;
/*      */     }
/* 1682 */     String newMsg = LocaleUtils.encodeMessage("csFileServiceContainingPage", oldMsg, streamId);
/* 1683 */     setCachedObject("ParentPageMessage", newMsg);
/*      */   }
/*      */ 
/*      */   public boolean determineExists(DataStreamWrapper streamWrapper)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1689 */     if (!streamWrapper.m_determinedExistence)
/*      */     {
/* 1695 */       if (streamWrapper.m_filePath != null)
/*      */       {
/* 1697 */         File f = new File(streamWrapper.m_filePath);
/* 1698 */         streamWrapper.m_determinedExistence = true;
/* 1699 */         streamWrapper.m_streamLocationExists = f.exists();
/*      */       }
/* 1703 */       else if ((streamWrapper.m_descriptor != null) && (streamWrapper instanceof IdcFileDescriptor))
/*      */       {
/* 1706 */         loadDescriptorStorageData(streamWrapper);
/*      */       }
/*      */     }
/*      */ 
/* 1710 */     return streamWrapper.m_streamLocationExists;
/*      */   }
/*      */ 
/*      */   public void prepareErrorResponse(ServiceException serviceErr)
/*      */   {
/* 1722 */     boolean doSpecialErrorCheck = (!this.m_binder.m_isJava) || (DataBinderUtils.getBoolean(this.m_binder, "UseHttpHeadersForFailedFileAccessForIsJava", false));
/*      */ 
/* 1724 */     String startResponse = null;
/* 1725 */     int errorCode = -1;
/* 1726 */     if (serviceErr != null)
/*      */     {
/* 1728 */       errorCode = serviceErr.m_errorCode;
/*      */     }
/* 1730 */     if (doSpecialErrorCheck)
/*      */     {
/* 1732 */       if ((errorCode == -18) || (errorCode == -20))
/*      */       {
/* 1742 */         startResponse = "HTTP/1.1 403 Forbidden\r\n";
/*      */       }
/* 1744 */       else if (errorCode == -26)
/*      */       {
/* 1746 */         startResponse = "HTTP/1.1 500 Internal Server Error\r\n";
/*      */       }
/* 1748 */       if (startResponse == null)
/*      */       {
/* 1750 */         ServiceRequestImplementor srI = getRequestImplementor();
/* 1751 */         if ((!srI.m_isSevereError) || (errorCode == -16))
/*      */         {
/* 1753 */           startResponse = this.m_binder.getLocal("fileErrorStartResponse");
/* 1754 */           if (startResponse == null)
/*      */           {
/* 1756 */             if (errorCode == -1)
/*      */             {
/* 1761 */               errorCode = -16;
/*      */ 
/* 1765 */               if (isErrorStatusCodeSet())
/*      */               {
/* 1767 */                 this.m_binder.putLocal("StatusCode", "-16");
/*      */               }
/*      */             }
/* 1770 */             startResponse = "HTTP/1.1 404 File not found\r\n";
/*      */           }
/*      */         }
/* 1773 */         String overrideErrorPage = this.m_binder.getLocal("fileErrorOverridePage");
/* 1774 */         if (overrideErrorPage != null)
/*      */         {
/* 1776 */           setOverrideErrorPage(overrideErrorPage);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1782 */       boolean isAdfWccUI = StringUtils.convertToBool(this.m_binder.getLocal("IsAdfWccUI"), false);
/* 1783 */       if ((isAdfWccUI) && (this.m_serviceData.m_name.equals("GET_FILE")))
/*      */       {
/* 1786 */         if (errorCode == -16)
/*      */         {
/* 1788 */           startResponse = "HTTP/1.1 404 Not Found\r\n";
/*      */         }
/*      */ 
/* 1791 */         this.m_binder.putLocal("showSlimError", "1");
/* 1792 */         this.m_binder.putLocal("hideLogoForSlimError", "1");
/* 1793 */         setOverrideErrorPage("ERROR_PAGE");
/*      */       }
/*      */ 
/* 1803 */       if (startResponse != null)
/*      */       {
/* 1805 */         this.m_binder.setEnvironmentValue("HTTP_DEFAULT_RESPONSE_HEADER", startResponse);
/*      */       }
/*      */     }
/* 1808 */     super.prepareErrorResponse(serviceErr);
/*      */   }
/*      */ 
/*      */   public void logError(Exception e, String msg)
/*      */   {
/* 1814 */     logFileRequestError(e, msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setFileConversionInfo()
/*      */     throws ServiceException
/*      */   {
/* 1821 */     String outputFilePath = this.m_binder.getLocal("outputFilePath");
/* 1822 */     if (outputFilePath != null)
/*      */     {
/* 1824 */       setFile(outputFilePath);
/*      */     }
/* 1826 */     setSendFile(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void executeJsp() throws ServiceException
/*      */   {
/* 1832 */     if (this.m_cgiParams != null)
/*      */     {
/* 1834 */       this.m_binder.setEnvironmentValue("QUERY_STRING", this.m_cgiParams);
/*      */     }
/*      */     else
/*      */     {
/* 1839 */       DataStreamWrapper wrapper = getDownloadStream(false);
/* 1840 */       if ((wrapper != null) && (wrapper.m_filePath.indexOf("_ocsh") >= 0))
/*      */       {
/* 1844 */         this.m_binder.setEnvironmentValue("QUERY_STRING", "");
/*      */       }
/*      */     }
/*      */ 
/* 1848 */     this.m_binder.setEnvironmentValue("REQUESTURI", this.m_requestUri);
/* 1849 */     setCachedObject("OutputStream", this.m_output);
/* 1850 */     setCachedObject("Workspace", this.m_workspace);
/*      */     try
/*      */     {
/* 1853 */       if (PluginFilters.filter("JspExecution", this.m_workspace, this.m_binder, this) != 0)
/*      */       {
/* 1855 */         return;
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1860 */       throw new ServiceException(e);
/*      */     }
/* 1862 */     this.m_isJsp = true;
/* 1863 */     String prvdName = "SystemJspServer";
/* 1864 */     Provider provider = Providers.getProvider(prvdName);
/* 1865 */     JspProvider tp = (JspProvider)provider.getProvider();
/*      */ 
/* 1867 */     tp.process(this);
/*      */   }
/*      */ 
/*      */   public void computeVaultPath(Parameters fileParams)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1873 */     IdcFileDescriptor d = this.m_fileUtils.createDescriptorForRendition(fileParams, "primaryFile");
/*      */ 
/* 1875 */     setDescriptor(d);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void downloadBundle() throws ServiceException, DataException
/*      */   {
/* 1881 */     String id = this.m_binder.getLocal("clientDownloadID");
/* 1882 */     DataResultSet rset = SharedObjects.getTable("DeliverableList");
/* 1883 */     if (id != null)
/*      */     {
/* 1885 */       FieldInfo fi = new FieldInfo();
/* 1886 */       rset.getFieldInfo("dlClientDownloadID", fi);
/* 1887 */       if (rset.findRow(fi.m_index, id) == null)
/*      */       {
/* 1889 */         throw new DataException(null, "csUnableToDownloadclientDownloadIDNotExists", new Object[] { id });
/*      */       }
/* 1891 */       String location = rset.getStringValueByName("dlClientDownloadPath");
/* 1892 */       if (location.indexOf("..") >= 0)
/*      */       {
/* 1894 */         throw new DataException(null, "csUnableToDownloadValidatePath", new Object[] { location });
/*      */       }
/* 1896 */       if (location.startsWith("\\"))
/*      */       {
/* 1898 */         throw new DataException(null, "csUnableToDownloadValidatePath", new Object[] { location });
/*      */       }
/* 1900 */       location = FileUtils.fileSlashes(location);
/* 1901 */       String componentName = rset.getStringValueByName("idcComponentName");
/* 1902 */       if (!componentName.equalsIgnoreCase("Default"))
/*      */       {
/* 1904 */         String componentDir = ComponentLocationUtils.computeAbsoluteComponentDirectory(componentName);
/*      */ 
/* 1906 */         location = componentDir + '/' + location;
/*      */       }
/*      */       else
/*      */       {
/* 1910 */         String intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 1911 */         location = intradocDir + location;
/*      */       }
/* 1913 */       setFile(location);
/*      */ 
/* 1915 */       String downloadName = FileUtils.getName(location);
/* 1916 */       setDownloadName(downloadName);
/*      */ 
/* 1918 */       DocFormats docFormats = (DocFormats)SharedObjects.getTable("DocFormats");
/* 1919 */       if (docFormats != null)
/*      */       {
/* 1921 */         String ext = FileUtils.getExtension(location);
/* 1922 */         this.m_binder.putLocal("dExtension", ext);
/* 1923 */         String format = docFormats.determineFormat(this.m_binder, null, null, false, downloadName);
/* 1924 */         setDownloadFormat(format);
/*      */       }
/* 1926 */       setConditionVar("SuppressCacheControlHeader", true);
/* 1927 */       setSendFile(true);
/*      */     }
/* 1929 */     if (rset == null)
/*      */       return;
/* 1931 */     this.m_binder.addResultSet("DeliverableList", rset);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1937 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105362 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.FileService
 * JD-Core Version:    0.5.4
 */