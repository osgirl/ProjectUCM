/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PosixStructStat;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.FileStoreUtils;
/*     */ import intradoc.filestore.IdcDescriptorState;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class WebViewableConverterOutput
/*     */ {
/*     */   public DocServiceHandler m_docServiceHandler;
/*     */   public Service m_service;
/*     */   public DataBinder m_binder;
/*     */   public Workspace m_workspace;
/*     */   public FileStoreProvider m_fileStore;
/*     */   public FileStoreProviderHelper m_fileUtils;
/*     */   public Map m_deleteFlags;
/*     */   public boolean m_isResubmit;
/*     */   public boolean m_createNewDocId;
/*     */   public boolean m_hasWebViewable;
/*     */   public String m_webViewableFile;
/*     */   public String m_format;
/*     */   public boolean m_isPrimary;
/*     */   public String m_id;
/*     */   public String m_docName;
/*     */   public String m_revLabel;
/*     */   public String m_processingState;
/*     */   public String m_fileName;
/*     */   public String m_extension;
/*     */   public String m_webExtension;
/*     */   public String m_webFormat;
/*     */   public String m_webFileName;
/*     */   public String m_convType;
/*     */   public String m_originalName;
/*     */   public String m_webOriginalName;
/*     */   public String m_orgPathKey;
/*     */   public String m_orgPath;
/*     */   public IdcFileDescriptor m_orgDescriptor;
/*     */   public String m_webPath;
/*     */   public IdcFileDescriptor m_webDescriptor;
/*     */   public boolean m_isPublish;
/*     */   public boolean m_determinedWebExtension;
/*     */   public boolean m_determinedConversionOption;
/*     */   public boolean m_submitToConversion;
/*     */   public boolean m_submittedToQueue;
/*     */   public boolean m_didWebFileCreation;
/*     */   public boolean m_isNotLatestRev;
/*     */   public boolean m_redirectToVaultFile;
/*     */   public boolean m_isWebviewableLinkedToVault;
/*     */   public String m_webviewableToVaultLinkType;
/*     */   public String m_redirectToVaultExclusionFormats;
/*     */   public boolean m_isForceNoLink;
/*     */   public boolean m_supportsWebless;
/*     */ 
/*     */   public WebViewableConverterOutput()
/*     */   {
/*  33 */     this.m_docServiceHandler = null;
/*  34 */     this.m_service = null;
/*  35 */     this.m_binder = null;
/*  36 */     this.m_workspace = null;
/*  37 */     this.m_fileStore = null;
/*  38 */     this.m_fileUtils = null;
/*  39 */     this.m_deleteFlags = null;
/*     */ 
/*  42 */     this.m_isResubmit = false;
/*  43 */     this.m_createNewDocId = false;
/*  44 */     this.m_hasWebViewable = false;
/*  45 */     this.m_webViewableFile = null;
/*  46 */     this.m_format = null;
/*  47 */     this.m_isPrimary = false;
/*     */ 
/*  50 */     this.m_id = null;
/*  51 */     this.m_docName = null;
/*  52 */     this.m_revLabel = null;
/*     */ 
/*  55 */     this.m_processingState = null;
/*  56 */     this.m_fileName = null;
/*  57 */     this.m_extension = null;
/*  58 */     this.m_webExtension = null;
/*  59 */     this.m_webFormat = null;
/*  60 */     this.m_webFileName = null;
/*  61 */     this.m_convType = null;
/*  62 */     this.m_originalName = null;
/*  63 */     this.m_webOriginalName = null;
/*     */ 
/*  65 */     this.m_orgPathKey = null;
/*  66 */     this.m_orgPath = null;
/*  67 */     this.m_orgDescriptor = null;
/*  68 */     this.m_webPath = null;
/*  69 */     this.m_webDescriptor = null;
/*     */ 
/*  71 */     this.m_isPublish = false;
/*  72 */     this.m_determinedWebExtension = false;
/*  73 */     this.m_determinedConversionOption = false;
/*  74 */     this.m_submitToConversion = false;
/*  75 */     this.m_submittedToQueue = false;
/*  76 */     this.m_didWebFileCreation = false;
/*  77 */     this.m_isNotLatestRev = false;
/*     */ 
/*  80 */     this.m_redirectToVaultFile = false;
/*  81 */     this.m_isWebviewableLinkedToVault = false;
/*  82 */     this.m_webviewableToVaultLinkType = null;
/*  83 */     this.m_redirectToVaultExclusionFormats = "*/hcs*|*/ttp|*/xsl|*/wml|*template*|*/jsp*";
/*  84 */     this.m_isForceNoLink = false;
/*  85 */     this.m_supportsWebless = false;
/*     */   }
/*     */ 
/*     */   public void init(DocServiceHandler docHandler, Service service, boolean isResubmit, boolean createNewDocId, boolean hasWebViewable, String webViewableFile)
/*     */     throws DataException, ServiceException
/*     */   {
/*  91 */     this.m_docServiceHandler = docHandler;
/*  92 */     this.m_service = service;
/*  93 */     this.m_binder = this.m_service.getBinder();
/*  94 */     this.m_workspace = this.m_service.getWorkspace();
/*     */ 
/*  96 */     this.m_fileStore = service.m_fileStore;
/*  97 */     this.m_fileUtils = service.m_fileUtils;
/*  98 */     this.m_deleteFlags = new HashMap();
/*     */ 
/* 100 */     this.m_isResubmit = isResubmit;
/* 101 */     this.m_createNewDocId = createNewDocId;
/* 102 */     this.m_hasWebViewable = hasWebViewable;
/* 103 */     this.m_webViewableFile = webViewableFile;
/* 104 */     String publishState = this.m_binder.getAllowMissing("dPublishState");
/* 105 */     if ((publishState != null) && (publishState.length() > 0))
/*     */     {
/* 107 */       this.m_isPublish = true;
/*     */     }
/* 109 */     this.m_isNotLatestRev = DataBinderUtils.getLocalBoolean(this.m_binder, "IsNotLatestRev", false);
/*     */ 
/* 111 */     this.m_isWebviewableLinkedToVault = DataBinderUtils.getBoolean(this.m_binder, "IsWebviewableLinkedToVault", false);
/* 112 */     if (this.m_isWebviewableLinkedToVault)
/*     */     {
/* 114 */       this.m_webviewableToVaultLinkType = this.m_binder.getAllowMissing("WebviewableToVaultLinkType");
/*     */     }
/*     */     else
/*     */     {
/* 118 */       this.m_redirectToVaultFile = ((DataBinderUtils.getBoolean(this.m_binder, "IndexVaultFile", false)) || (DataBinderUtils.getBoolean(this.m_binder, "CreateWebviewableRedirectFile", false)));
/*     */ 
/* 120 */       this.m_supportsWebless = ((BaseFileStore)this.m_fileStore).getConfigBoolean("SupportsWeblessStorage", null, false, false);
/*     */ 
/* 122 */       if ((this.m_redirectToVaultFile) || (this.m_supportsWebless))
/*     */       {
/* 124 */         String val = this.m_binder.getAllowMissing("IndexVaultExclusionWildcardFormats");
/* 125 */         if (val != null)
/*     */         {
/* 127 */           this.m_redirectToVaultExclusionFormats = val;
/*     */         }
/*     */       }
/*     */     }
/* 131 */     this.m_isPrimary = DataBinderUtils.getBoolean(this.m_binder, "dIsPrimary", true);
/*     */ 
/* 134 */     this.m_service.setCachedObject("WebViewableOutput", this);
/*     */   }
/*     */ 
/*     */   public void doConversion()
/*     */     throws DataException, ServiceException
/*     */   {
/* 140 */     this.m_id = this.m_binder.get("dID");
/* 141 */     this.m_docName = this.m_binder.get("dDocName");
/* 142 */     this.m_revLabel = this.m_binder.get("dRevLabel");
/*     */ 
/* 145 */     this.m_extension = this.m_binder.get("dExtension").toLowerCase();
/* 146 */     this.m_webExtension = this.m_binder.getLocal("curWebExtension");
/* 147 */     if (this.m_webExtension == null)
/*     */     {
/* 149 */       this.m_webExtension = this.m_extension;
/*     */     }
/* 151 */     this.m_webExtension = this.m_webExtension.toLowerCase();
/*     */ 
/* 153 */     if (this.m_format == null)
/*     */     {
/* 155 */       this.m_format = this.m_binder.get("dFormat");
/*     */     }
/*     */ 
/* 158 */     computeSourcePath();
/* 159 */     prepareForWebViewable();
/* 160 */     if (this.m_isResubmit)
/*     */     {
/* 162 */       createWebFileName();
/* 163 */       cleanOldWebViewables();
/*     */     }
/* 165 */     prepareConversionOption();
/*     */ 
/* 169 */     this.m_originalName = this.m_binder.getLocal("dOriginalName");
/*     */ 
/* 172 */     if (this.m_originalName == null)
/*     */     {
/* 174 */       this.m_originalName = this.m_fileName;
/*     */     }
/*     */ 
/* 183 */     this.m_binder.putLocal("dStatus", "GENWWW");
/*     */ 
/* 185 */     if (!doCustomOverRides())
/*     */     {
/* 187 */       return;
/*     */     }
/*     */ 
/* 190 */     if (this.m_hasWebViewable)
/*     */     {
/* 192 */       this.m_webExtension = this.m_binder.getLocal("dWebExtension");
/* 193 */       if (this.m_webExtension == null)
/*     */       {
/* 195 */         this.m_webExtension = "";
/*     */       }
/*     */       else
/*     */       {
/* 199 */         this.m_webExtension = this.m_webExtension.toLowerCase();
/*     */       }
/* 201 */       createWebFileName();
/*     */ 
/* 204 */       this.m_binder.putLocal("dExtension", this.m_webExtension);
/*     */ 
/* 207 */       this.m_webFormat = this.m_binder.getLocal("webViewableFile:format");
/* 208 */       if (this.m_webFormat == null)
/*     */       {
/* 210 */         if ((((this.m_webExtension != null) ? 1 : 0) & ((this.m_webExtension.length() > 0) ? 1 : 0)) != 0)
/*     */         {
/* 212 */           this.m_webFormat = ("application/" + this.m_webExtension);
/*     */         }
/*     */         else
/*     */         {
/* 216 */           this.m_webFormat = "unknown/binary";
/*     */         }
/*     */       }
/* 219 */       this.m_binder.putLocal("dFormat", this.m_webFormat);
/* 220 */       this.m_determinedWebExtension = true;
/*     */     }
/* 224 */     else if (!this.m_determinedWebExtension)
/*     */     {
/* 227 */       this.m_webExtension = this.m_extension;
/* 228 */       this.m_determinedWebExtension = true;
/*     */     }
/*     */     else
/*     */     {
/* 234 */       this.m_binder.putLocal("dExtension", this.m_webExtension);
/*     */     }
/*     */ 
/* 239 */     if (this.m_createNewDocId)
/*     */     {
/* 242 */       this.m_docServiceHandler.addIncrementID(null, 2, "dDocID");
/*     */     }
/*     */ 
/* 245 */     if (!this.m_determinedConversionOption)
/*     */     {
/* 247 */       computeConversionOption();
/* 248 */       this.m_determinedConversionOption = true;
/*     */     }
/*     */ 
/* 251 */     if (this.m_submitToConversion)
/*     */     {
/* 253 */       if (!this.m_submittedToQueue)
/*     */       {
/* 255 */         createWebFileName();
/*     */ 
/* 265 */         this.m_binder.putLocal("reserveLocation", "1");
/* 266 */         prepareWebFileCreation();
/* 267 */         addToConversionQueue();
/* 268 */         preparePostConversionQueueDocumentRowEntry();
/* 269 */         if (this.m_isResubmit)
/*     */         {
/* 271 */           reportQueueAdditionToWorkflowHistory();
/*     */         }
/*     */         else
/*     */         {
/* 275 */           reportDelayedConversionToWorkflow();
/*     */         }
/* 277 */         this.m_submittedToQueue = true;
/*     */       }
/*     */ 
/*     */     }
/* 282 */     else if (!this.m_didWebFileCreation)
/*     */     {
/* 284 */       createWebFileName();
/* 285 */       prepareWebFileCreation();
/*     */ 
/* 290 */       if (!this.m_service.executeFilter("createWebViewableFiles"))
/*     */       {
/* 292 */         return;
/*     */       }
/*     */ 
/* 295 */       if ((this.m_redirectToVaultFile) && (!this.m_didWebFileCreation))
/*     */       {
/* 298 */         createRedirectionFile();
/*     */       }
/* 300 */       if ((this.m_isWebviewableLinkedToVault) && (!this.m_didWebFileCreation) && (this.m_orgPath != null) && (this.m_webPath != null))
/*     */       {
/* 303 */         createLinkToVaultFile();
/*     */       }
/* 305 */       if (!this.m_didWebFileCreation)
/*     */       {
/* 307 */         copyWebFile();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 313 */     this.m_service.setCachedObject("WebViewableOutput", "");
/*     */ 
/* 317 */     this.m_binder.putLocal("dExtension", this.m_extension);
/*     */   }
/*     */ 
/*     */   public boolean doCustomOverRides()
/*     */     throws DataException, ServiceException
/*     */   {
/* 323 */     DataBinder computeBinder = new DataBinder();
/* 324 */     computeBinder.copyLocalDataStateClone(this.m_binder);
/* 325 */     computeBinder.copyResultSetStateShallow(this.m_binder);
/* 326 */     PageMerger pm = new PageMerger(computeBinder, null);
/*     */     try
/*     */     {
/* 329 */       pm.evaluateResourceInclude("pre_submit_to_conversion");
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 337 */       pm.releaseAllTemporary();
/* 338 */       pm = null;
/*     */     }
/*     */ 
/* 341 */     String conversionType = computeBinder.getLocal("dConversion");
/* 342 */     if ((conversionType != null) && (conversionType.length() > 0) && (!conversionType.equals(this.m_convType)))
/*     */     {
/* 344 */       Report.trace("docconversion", "The 'pre_submit_to_conversion' include changed dConversion from: " + this.m_convType + "; to: " + conversionType, null);
/*     */ 
/* 346 */       this.m_binder.putLocal("dConversion", conversionType);
/* 347 */       this.m_convType = conversionType;
/*     */     }
/*     */ 
/* 352 */     return this.m_service.executeFilter("createWebViewable");
/*     */   }
/*     */ 
/*     */   public void prepareForWebViewable()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void createWebFileName()
/*     */   {
/* 365 */     this.m_webFileName = (this.m_docName + "~" + this.m_revLabel);
/* 366 */     if (this.m_webExtension.length() <= 0)
/*     */       return;
/* 368 */     this.m_webFileName = (this.m_webFileName + "." + this.m_webExtension);
/*     */   }
/*     */ 
/*     */   public void cleanOldWebViewables()
/*     */     throws DataException, ServiceException
/*     */   {
/* 375 */     if (!this.m_service.executeFilter("cleanOldWebViewables"))
/*     */     {
/* 377 */       return;
/*     */     }
/*     */ 
/* 384 */     String oldState = this.m_binder.get("dReleaseState");
/* 385 */     String newState = oldState;
/* 386 */     if ("YUI".indexOf(oldState) >= 0)
/*     */     {
/* 388 */       newState = "U";
/*     */     }
/* 390 */     else if ("ED".indexOf(oldState) < 0)
/*     */     {
/* 392 */       newState = "N";
/*     */     }
/* 394 */     this.m_binder.putLocal("dReleaseState", newState);
/*     */ 
/* 396 */     this.m_binder.putLocal("RenditionId", "webViewableFile");
/* 397 */     IdcFileDescriptor file = this.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/* 398 */     this.m_fileUtils.deleteFile(file, this.m_deleteFlags, this.m_service);
/*     */ 
/* 401 */     IdcDescriptorState state = (IdcDescriptorState)this.m_service.getCachedObject("DescriptorStates");
/*     */ 
/* 403 */     state.clearWebless(this.m_service);
/*     */ 
/* 406 */     if ((this.m_format != null) && (this.m_format.startsWith("idcmeta")) && (!SharedObjects.getEnvValueAsBoolean("AlwaysCleanAdditionalRenditions", false)))
/*     */       return;
/* 408 */     cleanOldAdditionalRenditions();
/*     */   }
/*     */ 
/*     */   public void cleanOldAdditionalRenditions()
/*     */     throws DataException, ServiceException
/*     */   {
/* 414 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 417 */     ResultSet rset = this.m_workspace.createResultSet("QdocInfo", this.m_binder);
/*     */ 
/* 420 */     DataResultSet docInfo = new DataResultSet();
/* 421 */     docInfo.copy(rset);
/*     */ 
/* 423 */     boolean renditionPresent = false;
/* 424 */     Boolean allowClearAllRenditions = Boolean.TRUE;
/*     */ 
/* 426 */     for (int i = 1; i <= AdditionalRenditions.m_maxNum; ++i)
/*     */     {
/* 428 */       String key = "dRendition" + i;
/* 429 */       String renFlag = ResultSetUtils.getValue(docInfo, key);
/* 430 */       if ((renFlag == null) || (renFlag.length() <= 0))
/*     */         continue;
/* 432 */       renditionPresent = true;
/* 433 */       String[][] info = ResultSetUtils.createFilteredStringTable(renSet, new String[] { "renFlag", "renExtension" }, renFlag);
/*     */ 
/* 436 */       if (info.length > 1)
/*     */       {
/* 438 */         String msg = LocaleUtils.encodeMessage("csTooManyRenditionsMatch", null, "" + info.length, renFlag);
/*     */ 
/* 440 */         this.m_service.createServiceException(null, msg);
/*     */       }
/* 442 */       else if (info.length == 0)
/*     */       {
/* 444 */         String msg = LocaleUtils.encodeMessage("csUnknownRenditionFlag", null, renFlag);
/*     */ 
/* 446 */         this.m_service.createServiceException(null, msg);
/*     */       }
/* 448 */       Object[] params = { docInfo, key, renFlag, info, allowClearAllRenditions };
/* 449 */       this.m_service.setCachedObject("cleanOldAdditionalRendition:params", params);
/* 450 */       if (this.m_service.executeFilter("cleanOldAdditionalRendition"))
/*     */       {
/* 452 */         this.m_binder.putLocal("RenditionId", "rendition:" + renFlag);
/*     */ 
/* 454 */         IdcFileDescriptor d = this.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/* 455 */         this.m_fileUtils.deleteFile(d, this.m_deleteFlags, this.m_service);
/*     */       }
/* 457 */       allowClearAllRenditions = (Boolean)params[4];
/*     */     }
/*     */ 
/* 460 */     if ((!renditionPresent) || (!allowClearAllRenditions.booleanValue()))
/*     */       return;
/* 462 */     this.m_workspace.execute("UrenditionsClear", this.m_binder);
/*     */   }
/*     */ 
/*     */   public void prepareConversionOption()
/*     */     throws DataException, ServiceException
/*     */   {
/* 470 */     this.m_convType = this.m_binder.getLocal("dConversion");
/*     */ 
/* 472 */     if (SystemUtils.m_verbose)
/*     */     {
/* 474 */       Report.debug("docconversion", "prepareConversionOption: m_convType=" + this.m_convType + " m_format=" + this.m_format + " m_redirectToVaultFile=" + this.m_redirectToVaultFile + " m_hasWebViewable=" + this.m_hasWebViewable, null);
/*     */     }
/*     */ 
/* 479 */     if ((this.m_redirectToVaultFile) && (!this.m_hasWebViewable) && (((this.m_convType == null) || (this.m_convType.equalsIgnoreCase("PASSTHRU")) || (this.m_convType.equalsIgnoreCase("PassThru")))) && (!StringUtils.match(this.m_format, this.m_redirectToVaultExclusionFormats, true)) && (this.m_isPrimary))
/*     */     {
/* 486 */       this.m_convType = "PASSTHRU";
/* 487 */       this.m_webExtension = "hcst";
/* 488 */       this.m_determinedWebExtension = true;
/*     */     }
/*     */     else
/*     */     {
/* 492 */       if ((this.m_supportsWebless) && 
/* 494 */         (StringUtils.match(this.m_format, this.m_redirectToVaultExclusionFormats, true)))
/*     */       {
/* 496 */         this.m_isForceNoLink = true;
/*     */       }
/*     */ 
/* 500 */       this.m_redirectToVaultFile = false;
/*     */ 
/* 503 */       if (this.m_convType == null)
/*     */       {
/* 505 */         this.m_convType = "PassThru";
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 510 */     this.m_processingState = "C";
/*     */   }
/*     */ 
/*     */   public void computeConversionOption() throws DataException, ServiceException
/*     */   {
/* 515 */     this.m_submitToConversion = ((!this.m_convType.equalsIgnoreCase("PASSTHRU")) && (!this.m_convType.equalsIgnoreCase("PassThru")) && (!this.m_hasWebViewable) && (!this.m_isPublish));
/*     */   }
/*     */ 
/*     */   public void addToConversionQueue()
/*     */     throws DataException, ServiceException
/*     */   {
/* 521 */     String docId = this.m_binder.get("dDocID");
/* 522 */     QueueProcessor.addDocToConversionQueue(docId, this.m_binder, this.m_fileName, this.m_webFileName, this.m_service);
/*     */   }
/*     */ 
/*     */   public void preparePostConversionQueueDocumentRowEntry() throws DataException, ServiceException
/*     */   {
/* 527 */     if (!this.m_isResubmit)
/*     */     {
/* 532 */       this.m_binder.putLocal("dOriginalName", "");
/* 533 */       this.m_binder.putLocal("dFormat", "");
/* 534 */       this.m_binder.putLocal("dExtension", "");
/* 535 */       this.m_binder.putLocal("dFileSize", "0");
/*     */     }
/* 537 */     this.m_service.executeFilter("postConversionQueueSubmit");
/*     */   }
/*     */ 
/*     */   public void reportQueueAdditionToWorkflowHistory()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 545 */       DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, this.m_isNotLatestRev, false, true, true, this.m_service);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 550 */       Report.warning(null, e, "csWfHistoryUpdateError", new Object[] { this.m_docName });
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reportDelayedConversionToWorkflow() throws DataException, ServiceException
/*     */   {
/* 556 */     DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, this.m_isNotLatestRev, false, false, true, this.m_service);
/*     */   }
/*     */ 
/*     */   public void computeSourcePath()
/*     */     throws DataException, ServiceException
/*     */   {
/* 564 */     this.m_fileName = this.m_id;
/* 565 */     if ((this.m_extension != null) && (this.m_extension.length() > 0))
/*     */     {
/* 567 */       this.m_fileName = (this.m_fileName + "." + this.m_extension);
/*     */     }
/* 569 */     boolean fileExists = false;
/* 570 */     if (this.m_hasWebViewable)
/*     */     {
/* 572 */       this.m_orgPath = this.m_webViewableFile;
/*     */     }
/*     */     else
/*     */     {
/* 576 */       String rendition = "primaryFile";
/* 577 */       this.m_orgPathKey = "primaryFile:path";
/* 578 */       if (!this.m_isPrimary)
/*     */       {
/* 580 */         rendition = "alternateFile";
/* 581 */         this.m_orgPathKey = "alternateFile:path";
/*     */       }
/* 583 */       this.m_binder.putLocal("RenditionId", rendition);
/* 584 */       this.m_orgDescriptor = this.m_fileStore.createDescriptor(this.m_binder, null, this.m_service);
/*     */ 
/* 591 */       if (checkIfHasOrgPath())
/*     */       {
/* 593 */         File file = new File(this.m_orgPath);
/* 594 */         fileExists = file.exists();
/* 595 */         this.m_binder.putLocal("VaultfilePath", this.m_orgPath);
/*     */       }
/*     */       else
/*     */       {
/* 599 */         fileExists = this.m_fileUtils.fileExists(this.m_orgDescriptor, this.m_service);
/*     */       }
/* 601 */       if (fileExists)
/*     */         return;
/* 603 */       String msg = LocaleUtils.encodeMessage("csWebViewableCannotAccessNativeFile", null, this.m_orgPath);
/*     */ 
/* 605 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean checkIfHasOrgPath()
/*     */     throws DataException, ServiceException
/*     */   {
/* 612 */     if (FileStoreUtils.isStoredOnFileSystem(this.m_orgDescriptor, this.m_fileStore))
/*     */     {
/* 614 */       this.m_orgPath = this.m_fileStore.getFilesystemPath(this.m_orgDescriptor, this.m_service);
/*     */     }
/* 616 */     else if (this.m_orgPathKey != null)
/*     */     {
/* 618 */       this.m_orgPath = ((String)this.m_service.getCachedObject(this.m_orgPathKey));
/* 619 */       boolean isForceToFile = DataBinderUtils.getLocalBoolean(this.m_binder, "forceNativeToFile", false);
/* 620 */       if ((isForceToFile) && (this.m_orgPath == null))
/*     */       {
/* 622 */         this.m_orgPath = this.m_fileStore.getFilesystemPath(this.m_orgDescriptor, this.m_service);
/*     */       }
/*     */     }
/* 625 */     return this.m_orgPath != null;
/*     */   }
/*     */ 
/*     */   public boolean checkIfHasWebPath() throws DataException, ServiceException
/*     */   {
/* 630 */     boolean isForceToFile = DataBinderUtils.getLocalBoolean(this.m_binder, "forceWebToFile", false);
/* 631 */     if ((isForceToFile) || (FileStoreUtils.isStoredOnFileSystem(this.m_webDescriptor, this.m_fileStore)))
/*     */     {
/* 633 */       this.m_webPath = this.m_fileStore.getFilesystemPath(this.m_webDescriptor, this.m_service);
/*     */     }
/* 635 */     return this.m_webPath != null;
/*     */   }
/*     */ 
/*     */   public void prepareWebFileCreation() throws DataException, ServiceException
/*     */   {
/* 640 */     this.m_binder.putLocal("dWebExtension", this.m_webExtension);
/* 641 */     this.m_binder.putLocal("RenditionId", "webViewableFile");
/* 642 */     Map args = new HashMap();
/* 643 */     args.put("isNew", "1");
/* 644 */     args.put("forceNoLink", "" + this.m_isForceNoLink);
/* 645 */     args.put("isRetainMetadata", "1");
/* 646 */     if (this.m_binder.getAllowMissing("reserveLocation") == null)
/*     */     {
/* 648 */       this.m_binder.putLocal("reserveLocation", "" + this.m_redirectToVaultFile);
/*     */     }
/* 650 */     this.m_webDescriptor = this.m_fileStore.createDescriptor(this.m_binder, args, this.m_service);
/* 651 */     if (!checkIfHasWebPath())
/*     */       return;
/* 653 */     this.m_binder.putLocal("WebfilePath", this.m_webPath);
/*     */ 
/* 656 */     args = new HashMap();
/* 657 */     args.put("doContainerPath", "1");
/* 658 */     args.put("doCreateContainers", "1");
/* 659 */     this.m_fileStore.getFilesystemPathWithArgs(this.m_webDescriptor, args, this.m_service);
/*     */   }
/*     */ 
/*     */   public void createRedirectionFile()
/*     */     throws DataException, ServiceException
/*     */   {
/* 669 */     Writer writer = null;
/* 670 */     File webFile = null;
/*     */     try
/*     */     {
/* 673 */       String template = this.m_binder.getLocal("RedirectionFileTemplate");
/* 674 */       if ((template == null) || (template.length() == 0))
/*     */       {
/* 676 */         template = "REDIRECTION_FILE_TEMPLATE";
/*     */       }
/* 678 */       String dhtmlOutput = this.m_service.createMergedPage(template);
/*     */ 
/* 681 */       String webPath = this.m_webDescriptor.getProperty("path");
/* 682 */       String dir = FileUtils.getDirectory(webPath);
/* 683 */       FileUtils.checkOrCreateDirectory(dir, 10);
/* 684 */       webFile = new File(webPath);
/* 685 */       writer = FileUtils.openDataWriter(webFile, FileUtils.m_javaSystemEncoding);
/*     */ 
/* 687 */       writer.write(dhtmlOutput);
/*     */ 
/* 689 */       postProcessWebFileCreation(webFile.length());
/* 690 */       this.m_didWebFileCreation = true;
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 698 */       closeWriter(writer);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 703 */       if (this.m_didWebFileCreation)
/*     */       {
/* 705 */         HashMap args = new HashMap();
/* 706 */         args.put("isNew", "1");
/* 707 */         args.put("localInPlace", "1");
/* 708 */         this.m_fileStore.storeFromLocalFile(this.m_webDescriptor, webFile, args, this.m_service);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 713 */       throw new ServiceException("!csMetaFileUnableToCreate", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createLinkToVaultFile() throws DataException, ServiceException
/*     */   {
/* 719 */     NativeOsUtils osUtils = new NativeOsUtils();
/*     */ 
/* 724 */     HashMap map = new HashMap();
/* 725 */     map.put("isNotFileStoreCreate", "1");
/* 726 */     map.put("isNew", "1");
/*     */     try
/*     */     {
/* 731 */       this.m_fileStore.forceToFilesystemPath(this.m_webDescriptor, map, this.m_service);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 735 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 739 */     File webFile = new File(this.m_webPath);
/* 740 */     webFile.delete();
/* 741 */     if (webFile.exists())
/*     */     {
/* 743 */       String msg = LocaleUtils.encodeMessage("csWebViewableFileCannotBeDeleted", null, this.m_webPath);
/* 744 */       throw new DataException(msg);
/*     */     }
/* 746 */     String linkType = "hardlink";
/* 747 */     int retVal = -1;
/* 748 */     if ((this.m_webviewableToVaultLinkType != null) && (this.m_webviewableToVaultLinkType.equalsIgnoreCase("symlink")))
/*     */     {
/* 750 */       linkType = "symlink";
/* 751 */       retVal = osUtils.symlink(this.m_orgPath, this.m_webPath);
/*     */     }
/*     */     else
/*     */     {
/* 755 */       retVal = osUtils.link(this.m_orgPath, this.m_webPath);
/*     */     }
/* 757 */     if ((!webFile.exists()) || (retVal < 0))
/*     */     {
/* 759 */       Object[] params = { linkType, this.m_orgPath, this.m_webPath };
/* 760 */       String msg = LocaleUtils.encodeMessage("csWebViewableFileCannotBeCreatedByLink", null, params);
/* 761 */       throw new DataException(msg);
/*     */     }
/* 763 */     postProcessWebFileCreation(webFile.length());
/* 764 */     this.m_didWebFileCreation = true;
/*     */   }
/*     */ 
/*     */   public void copyWebFile() throws DataException, ServiceException
/*     */   {
/* 769 */     Map storageData = null;
/* 770 */     long length = -1L;
/*     */     try
/*     */     {
/* 773 */       Map args = new HashMap();
/* 774 */       args.put("isNew", "1");
/* 775 */       if (this.m_orgDescriptor == null)
/*     */       {
/* 777 */         File orgFile = new File(this.m_orgPath);
/* 778 */         length = orgFile.length();
/* 779 */         this.m_fileStore.storeFromLocalFile(this.m_webDescriptor, orgFile, args, this.m_service);
/*     */       }
/*     */       else
/*     */       {
/* 783 */         this.m_fileStore.duplicateFile(this.m_orgDescriptor, this.m_webDescriptor, args, this.m_service);
/* 784 */         storageData = this.m_fileStore.getStorageData(this.m_orgDescriptor, null, this.m_service);
/*     */ 
/* 786 */         String size = (String)storageData.get("fileSize");
/* 787 */         length = NumberUtils.parseLong(size, -1L);
/*     */       }
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 792 */       throw new ServiceException(e);
/*     */     }
/* 794 */     postProcessWebFileCreation(length);
/* 795 */     this.m_didWebFileCreation = true;
/*     */   }
/*     */ 
/*     */   public void postProcessWebFileCreation(File webFile) throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 802 */       Map args = new HashMap();
/* 803 */       args.put("isNew", "1");
/* 804 */       args.put("localInPlace", "1");
/* 805 */       this.m_fileStore.storeFromLocalFile(this.m_webDescriptor, webFile, args, this.m_service);
/* 806 */       postProcessWebFileCreation(webFile.length());
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 810 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void postProcessWebFileCreation(long length) throws DataException, ServiceException
/*     */   {
/* 816 */     this.m_processingState = "Y";
/*     */ 
/* 818 */     DocStateTransition.advanceDocumentState(this.m_binder, this.m_workspace, this.m_isNotLatestRev, true, false, true, this.m_service);
/*     */ 
/* 823 */     if (this.m_webOriginalName == null)
/*     */     {
/* 825 */       this.m_webOriginalName = this.m_binder.getLocal("webViewableFile:name");
/* 826 */       if ((this.m_webOriginalName == null) || (this.m_webOriginalName.length() == 0))
/*     */       {
/* 829 */         this.m_webOriginalName = this.m_originalName;
/*     */         String rootFileName;
/* 835 */         if ((SharedObjects.getEnvValueAsBoolean("UseSecondaryWebFileOriginalNameScheme", false)) && (this.m_originalName != null))
/*     */         {
/* 838 */           String rootFileName = FileUtils.getName(this.m_originalName);
/* 839 */           int index = rootFileName.lastIndexOf(46);
/* 840 */           if (index >= 0)
/*     */           {
/* 842 */             rootFileName = rootFileName.substring(0, index);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 847 */           rootFileName = this.m_docName + "~" + this.m_revLabel;
/*     */         }
/*     */ 
/* 852 */         String webPath = this.m_binder.getLocal("webViewableFile");
/*     */         String extension;
/*     */         String extension;
/* 853 */         if ((webPath != null) && (webPath.length() > 0))
/*     */         {
/* 855 */           extension = FileUtils.getExtension(webPath);
/*     */         }
/*     */         else
/*     */         {
/* 859 */           extension = this.m_webExtension;
/*     */         }
/*     */ 
/* 863 */         if ((rootFileName != null) && (rootFileName.length() != 0) && (extension != null) && (extension.length() != 0))
/*     */         {
/* 866 */           this.m_webOriginalName = (rootFileName + "." + extension);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 872 */     this.m_binder.putLocal("dOriginalName", this.m_originalName);
/* 873 */     this.m_binder.putLocal("dWebOriginalName", this.m_webOriginalName);
/* 874 */     this.m_binder.putLocal("dExtension", this.m_webExtension);
/*     */ 
/* 877 */     if (this.m_webFormat != null)
/*     */     {
/* 879 */       this.m_binder.putLocal("dFormat", this.m_webFormat);
/*     */     }
/*     */ 
/* 883 */     this.m_binder.putLocal("dFileSize", "" + length);
/* 884 */     this.m_service.executeFilter("postWebfileCreation");
/*     */   }
/*     */ 
/*     */   public boolean checkRecreateWebviewableOnUpdate(IdcFileDescriptor conversionSource, IdcFileDescriptor oldWebFile, IdcFileDescriptor newWebFile)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 893 */     if (!this.m_isWebviewableLinkedToVault)
/*     */     {
/* 895 */       return false;
/*     */     }
/* 897 */     this.m_orgDescriptor = conversionSource;
/* 898 */     if (!checkIfHasOrgPath())
/*     */     {
/* 900 */       return false;
/*     */     }
/* 902 */     this.m_webDescriptor = oldWebFile;
/* 903 */     if (!checkIfHasWebPath())
/*     */     {
/* 905 */       return false;
/*     */     }
/* 907 */     if (!FileStoreUtils.isStoredOnFileSystem(newWebFile, this.m_fileStore))
/*     */     {
/* 909 */       return false;
/*     */     }
/*     */ 
/* 912 */     String newWebFilename = this.m_fileStore.getFilesystemPath(newWebFile, this.m_service);
/*     */ 
/* 914 */     NativeOsUtils osUtils = new NativeOsUtils();
/* 915 */     PosixStructStat stat = new PosixStructStat();
/* 916 */     boolean retVal = false;
/* 917 */     int lstatRetVal = osUtils.lstat(this.m_webPath, stat);
/* 918 */     if (SystemUtils.m_verbose)
/*     */     {
/* 920 */       Report.debug("system", "Checking webviewable recreation, lstat=" + lstatRetVal + ", S_IFLNK=" + NativeOsUtils.S_IFLNK + " for file " + this.m_webPath, null);
/*     */     }
/*     */ 
/* 924 */     if ((lstatRetVal >= 0) && 
/* 926 */       ((stat.st_mode & NativeOsUtils.S_IFLNK) != 0))
/*     */     {
/* 928 */       retVal = true;
/*     */     }
/*     */ 
/* 931 */     if (retVal)
/*     */     {
/* 934 */       Map map = this.m_docServiceHandler.createDeleteFileArguments(true);
/* 935 */       this.m_fileStore.deleteFile(oldWebFile, map, this.m_service);
/*     */ 
/* 937 */       File webFile = new File(newWebFilename);
/* 938 */       String linkType = "symlink";
/* 939 */       int symLinkRetVal = osUtils.symlink(this.m_orgPath, newWebFilename);
/* 940 */       if ((symLinkRetVal < 0) || (!webFile.exists()))
/*     */       {
/* 942 */         Object[] params = { linkType, this.m_orgPath, newWebFilename };
/* 943 */         String msg = LocaleUtils.encodeMessage("csWebViewableFileCannotBeCreatedByLink", null, params);
/* 944 */         throw new DataException(msg);
/*     */       }
/*     */     }
/*     */ 
/* 948 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void closeWriter(Writer w)
/*     */   {
/*     */     try
/*     */     {
/* 955 */       if (w != null)
/*     */       {
/* 957 */         w.close();
/*     */       }
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/* 962 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 964 */       Report.debug("system", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 971 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96563 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WebViewableConverterOutput
 * JD-Core Version:    0.5.4
 */