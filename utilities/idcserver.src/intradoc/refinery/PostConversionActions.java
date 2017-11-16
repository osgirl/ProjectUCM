/*     */ package intradoc.refinery;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.BaseFileStore;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderHelper;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class PostConversionActions
/*     */ {
/*     */   public String m_tempDir;
/*     */   public DataBinder m_binder;
/*     */   protected Workspace m_workspace;
/*     */   protected ExecutionContext m_context;
/*     */   protected FileStoreProvider m_fileStore;
/*     */   protected FileStoreProviderHelper m_fileUtils;
/*     */ 
/*     */   public PostConversionActions(DataBinder binder, Workspace ws)
/*     */   {
/*  65 */     this.m_binder = binder;
/*  66 */     this.m_workspace = ws;
/*  67 */     this.m_context = new ExecutionContextAdaptor();
/*  68 */     this.m_context.setCachedObject("Workspace", ws);
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException, DataException
/*     */   {
/*  73 */     this.m_tempDir = this.m_binder.getLocal("TempRecieveDir");
/*     */ 
/*  75 */     this.m_context.setCachedObject("ConversionTempDirectory", this.m_tempDir);
/*     */ 
/*  81 */     String refineryConvertedFile = this.m_binder.getLocal("RefineryConvertedFile");
/*  82 */     if (refineryConvertedFile != null)
/*     */     {
/*  84 */       this.m_binder.putLocal("OutFile", refineryConvertedFile);
/*     */     }
/*     */ 
/*  87 */     this.m_fileStore = FileStoreProviderLoader.initFileStore(this.m_context);
/*  88 */     this.m_fileUtils = FileStoreProviderHelper.getFileStoreProviderUtils(this.m_fileStore, this.m_context);
/*  89 */     this.m_context.setCachedObject("FileStoreProvider", this.m_fileStore);
/*     */   }
/*     */ 
/*     */   public Properties getConvertedProps()
/*     */   {
/*  94 */     return this.m_binder.getLocalData();
/*     */   }
/*     */ 
/*     */   public void cleanup()
/*     */   {
/*  99 */     if ((this.m_tempDir == null) || (!RefineryUtils.m_refineryTransferCleanUp))
/*     */       return;
/*     */     try
/*     */     {
/* 103 */       File dir = new File(this.m_tempDir);
/* 104 */       FileUtils.deleteDirectory(dir, true);
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 108 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 110 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void processRefineryConversionResults()
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 119 */     RefineryUtils.getCurrentJobInfo(this.m_binder);
/* 120 */     String docID = this.m_binder.get("dDocID");
/* 121 */     String dDocName = this.m_binder.get("dDocName");
/* 122 */     String dConvJobID = this.m_binder.getLocal("dConvJobID");
/*     */ 
/* 125 */     DataBinder sanitizedBinder = new DataBinder();
/* 126 */     RefineryUtils.buildCurrentDocData(this.m_workspace, sanitizedBinder, this.m_binder.getLocalData());
/*     */     try
/*     */     {
/* 129 */       Object[] o = { this, this.m_binder };
/* 130 */       this.m_context.setCachedObject("processRefineryConversionResults:parameters", o);
/* 131 */       int ret = PluginFilters.filter("processRefineryConversionResults", this.m_workspace, sanitizedBinder, this.m_context);
/*     */ 
/* 133 */       if (ret == -1)
/*     */       {
/* 135 */         String errMsg = LocaleResources.localizeMessage("!csFilterError,processRefineryConversionResults", null);
/*     */ 
/* 137 */         throw new DataException(errMsg);
/*     */       }
/* 139 */       if (ret == 1)
/*     */       {
/* 141 */         return;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 146 */       String errMsg = LocaleUtils.encodeMessage("csFilterError", e.getMessage(), "processRefineryConversionResults");
/*     */ 
/* 148 */       Report.trace(null, errMsg, e);
/* 149 */       throw new DataException(errMsg, e);
/*     */     }
/*     */ 
/* 153 */     String conversionState = this.m_binder.getLocal("dConversionState");
/* 154 */     Report.trace("ibrsupport", "Processing conversion results for JobID: " + dConvJobID + "; ContentID: " + dDocName + "; dDocID: " + docID + "; conversion state (post filter): " + conversionState, null);
/*     */ 
/* 158 */     if (!conversionState.equalsIgnoreCase("Failed"))
/*     */     {
/* 162 */       processSuccessfulConversion(sanitizedBinder);
/*     */     }
/*     */     else
/*     */     {
/* 166 */       boolean allowRefineryPassthru = SharedObjects.getEnvValueAsBoolean("AllowRefineryPassthruFailConvert", false);
/* 167 */       if (allowRefineryPassthru == true)
/*     */       {
/* 170 */         refineryPassthru(sanitizedBinder);
/* 171 */         this.m_binder.putLocal("dConversionState", "PassThru");
/*     */       }
/*     */     }
/*     */ 
/* 175 */     ((BaseFileStore)this.m_fileStore).closeTransactionListener(0, this.m_context);
/*     */   }
/*     */ 
/*     */   protected void refineryPassthru(DataBinder jobBinder)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/* 183 */     String dExtension = jobBinder.get("dExtension");
/* 184 */     jobBinder.putLocal("dWebExtension", dExtension);
/* 185 */     this.m_binder.putLocal("dExtension", dExtension);
/* 186 */     this.m_binder.putLocal("dWebExtension", dExtension);
/*     */ 
/* 188 */     jobBinder.putLocal("reserveLocation", "1");
/* 189 */     jobBinder.putLocal("RenditionId", "webViewableFile");
/*     */ 
/* 191 */     jobBinder.putLocal("RenditionId", "primaryFile");
/* 192 */     IdcFileDescriptor nativeDescriptor = this.m_fileStore.createDescriptor(jobBinder, null, this.m_context);
/*     */ 
/* 194 */     Map args = buildFileStoreMapParameters();
/* 195 */     jobBinder.putLocal("RenditionId", "webViewableFile");
/* 196 */     IdcFileDescriptor webDescriptor = this.m_fileStore.createDescriptor(jobBinder, args, this.m_context);
/*     */ 
/* 198 */     String webPath = webDescriptor.getProperty("path");
/* 199 */     if (webPath != null)
/*     */     {
/* 201 */       jobBinder.putLocal("WebfilePath", webPath);
/*     */     }
/*     */ 
/* 204 */     this.m_fileStore.duplicateFile(nativeDescriptor, webDescriptor, null, this.m_context);
/*     */   }
/*     */ 
/*     */   protected void processSuccessfulConversion(DataBinder jobBinder)
/*     */     throws ServiceException, DataException, IOException
/*     */   {
/*     */     try
/*     */     {
/* 212 */       Object[] o = { this, this.m_binder };
/* 213 */       this.m_context.setCachedObject("storeRefineryConvertedFiles:parameters", o);
/* 214 */       int ret = PluginFilters.filter("processSuccessfulConversion", this.m_workspace, jobBinder, this.m_context);
/* 215 */       if (ret == -1)
/*     */       {
/* 217 */         String errMsg = LocaleResources.localizeMessage("!csFilterError,processSuccessfulConversion", null);
/* 218 */         throw new DataException(errMsg);
/*     */       }
/* 220 */       if (ret == 1)
/*     */       {
/* 222 */         return;
/*     */       }
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 227 */       SystemUtils.err(t, "!csRefineryErrorProcessSuccessfulConversionFilter");
/* 228 */       Report.trace(null, "There was an error caught from the processSuccessfulConversion filter. The conversion will still be processed as a Successful conversion", t);
/*     */     }
/*     */ 
/* 233 */     String convertedFile = this.m_binder.getLocal("RefineryConvertedFile");
/* 234 */     String convertedWebViewable = this.m_tempDir + convertedFile;
/*     */ 
/* 237 */     Map args = buildFileStoreMapParameters();
/* 238 */     jobBinder.putLocal("RenditionId", "webViewableFile");
/* 239 */     jobBinder.putLocal("reserveLocation", "1");
/* 240 */     IdcFileDescriptor d = this.m_fileStore.createDescriptor(jobBinder, args, this.m_context);
/* 241 */     this.m_fileStore.storeFromLocalFile(d, new File(convertedWebViewable), args, this.m_context);
/*     */ 
/* 243 */     String additionalRenditions = this.m_binder.getLocal("AdditionalRenditions");
/* 244 */     if ((additionalRenditions == null) || (additionalRenditions.length() <= 0))
/*     */       return;
/* 246 */     AdditionalRenditions additionRend = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 248 */     Vector rendList = StringUtils.parseArray(additionalRenditions, ',', ',');
/* 249 */     for (int i = 0; i < rendList.size(); ++i)
/*     */     {
/* 251 */       String renProductionStep = (String)rendList.elementAt(i);
/* 252 */       String renFile = this.m_binder.getLocal(renProductionStep + ":filename");
/* 253 */       String renExtension = FileUtils.getExtension(renFile);
/* 254 */       String renFlag = additionRend.getFlag(renExtension);
/* 255 */       String convertedRenFile = this.m_tempDir + renFile;
/*     */ 
/* 257 */       jobBinder.putLocal("RenditionId", "rendition:" + renFlag);
/* 258 */       IdcFileDescriptor renDesc = this.m_fileStore.createDescriptor(jobBinder, args, this.m_context);
/* 259 */       this.m_fileStore.storeFromLocalFile(renDesc, new File(convertedRenFile), args, this.m_context);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Map buildFileStoreMapParameters()
/*     */   {
/* 266 */     HashMap args = new HashMap();
/* 267 */     args.put("isNew", "1");
/* 268 */     args.put("isRetainMetadata", "1");
/* 269 */     args.put("isDoBackup", "0");
/* 270 */     args.put("isForce", "1");
/* 271 */     args.put("isMove", "1");
/* 272 */     args.put("isAllowFailure", "1");
/* 273 */     return args;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 278 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96034 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.PostConversionActions
 * JD-Core Version:    0.5.4
 */