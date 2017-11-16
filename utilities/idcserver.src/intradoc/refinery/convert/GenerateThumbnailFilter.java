/*     */ package intradoc.refinery.convert;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Log;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.refinery.configure.OitFontUtilsHelper;
/*     */ import intradoc.server.WebViewableConverterOutput;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class GenerateThumbnailFilter
/*     */   implements FilterImplementor
/*     */ {
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  57 */     if ((!Features.checkLevel("ContentManagement", null)) || (cxt == null))
/*     */     {
/*  59 */       return 0;
/*     */     }
/*  61 */     Object param = cxt.getCachedObject("filterParameter");
/*  62 */     if ((param == null) || (!param instanceof String))
/*     */     {
/*  64 */       return 0;
/*     */     }
/*     */ 
/*  67 */     if (param.equals("extraAfterServicesLoadInit"))
/*     */     {
/*  69 */       OitFontUtilsHelper.initOitFontPathAlertIfMissing("?IdcService=GET_CONFIG_OPTIONS&ConfigData=ConfigureOIThumbnails");
/*     */     }
/*  71 */     else if (param.equals("addReleasedWebFileDirect"))
/*     */     {
/*  73 */       boolean createThumbnail = createThumbnail(ws, binder, cxt);
/*  74 */       if (createThumbnail)
/*     */       {
/*  76 */         String inputPath = binder.get("primaryFile:path");
/*  77 */         doThumbnail(inputPath, null, ws, binder, cxt);
/*     */       }
/*     */     }
/*  80 */     else if (param.equals("postWebfileCreation"))
/*     */     {
/*  82 */       WebViewableConverterOutput webOutput = (WebViewableConverterOutput)cxt.getCachedObject("WebViewableOutput");
/*  83 */       if (webOutput != null)
/*     */       {
/*  87 */         boolean hasWebFile = webOutput.m_hasWebViewable;
/*  88 */         if (hasWebFile)
/*     */         {
/*  90 */           boolean forceRecreateThumbnail = SharedObjects.getEnvValueAsBoolean("ForceRecreateThumbnailGenerator", false);
/*     */ 
/*  92 */           hasWebFile = !forceRecreateThumbnail;
/*     */         }
/*  94 */         if (!hasWebFile)
/*     */         {
/*  96 */           boolean createThumbnail = createThumbnail(ws, binder, cxt);
/*  97 */           if (createThumbnail)
/*     */           {
/*  99 */             String nativeFile = webOutput.m_orgPath;
/* 100 */             if (nativeFile == null)
/*     */             {
/* 102 */               nativeFile = webOutput.m_fileStore.getFilesystemPath(webOutput.m_orgDescriptor, webOutput.m_service);
/*     */             }
/*     */ 
/* 105 */             doThumbnail(nativeFile, webOutput, ws, binder, cxt);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 110 */           String dDocName = binder.getAllowMissing("dDocName");
/* 111 */           String dID = binder.getAllowMissing("dID");
/* 112 */           Report.trace("thumbnailgenerator", "Thumbnail generator logic skipped for item: " + dDocName + " dID: " + dID + "; Already has wenviewables: " + hasWebFile, null);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 117 */     return 0;
/*     */   }
/*     */ 
/*     */   protected boolean createThumbnail(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 122 */     String dDocName = binder.getAllowMissing("dDocName");
/* 123 */     String dID = binder.getAllowMissing("dID");
/*     */ 
/* 125 */     boolean primaryMeta = DataBinderUtils.getBoolean(binder, "createPrimaryMetaFile", false);
/* 126 */     boolean alternateMeta = DataBinderUtils.getBoolean(binder, "createAlternateMetaFile", false);
/* 127 */     if ((primaryMeta) || (alternateMeta))
/*     */     {
/* 129 */       Report.trace("thumbnailgenerator", "ThumbnailGenerator skipping item: " + dDocName + " dID: " + dID + "; document is metafile.", null);
/*     */ 
/* 131 */       return false;
/*     */     }
/*     */ 
/* 134 */     boolean enableServerSideThumbnails = SharedObjects.getEnvValueAsBoolean("EnableServerSideThumbnails", false);
/* 135 */     boolean itemRequestedThumbnail = DataBinderUtils.getBoolean(binder, "CreateServerSideThumbnail", false);
/* 136 */     boolean docIsSealed = DataBinderUtils.getBoolean(binder, "DocIsSealed", false);
/* 137 */     boolean itemSkipThumbnail = DataBinderUtils.getBoolean(binder, "SkipServerSideThumbnail", false);
/* 138 */     String exlusiveIncludeList = SharedObjects.getEnvironmentValue("ServerSideThumbnailExclusiveIncludeExtensionList");
/*     */ 
/* 140 */     if (SystemUtils.m_verbose)
/*     */     {
/* 142 */       IdcStringBuilder msg = new IdcStringBuilder("ThumbnailGenerator item: " + dDocName + " dID: " + dID + "; booleans:");
/*     */ 
/* 144 */       msg.append("\nenableServerSideThumbnails: " + enableServerSideThumbnails);
/* 145 */       msg.append("\nitemRequestedThumbnail: " + itemRequestedThumbnail);
/* 146 */       msg.append("\ndocIsSealed: " + docIsSealed);
/* 147 */       msg.append("\nitemSkipThumbnail: " + itemSkipThumbnail);
/* 148 */       msg.append("\nServerSideThumbnailExclusiveIncludeExtensionList:" + exlusiveIncludeList);
/* 149 */       Report.trace("thumbnailgenerator", msg.toString(), null);
/*     */     }
/*     */ 
/* 152 */     boolean createThumbnail = true;
/* 153 */     if (!enableServerSideThumbnails)
/*     */     {
/* 155 */       createThumbnail = false;
/*     */     }
/* 157 */     if ((itemRequestedThumbnail) && (!createThumbnail))
/*     */     {
/* 159 */       createThumbnail = true;
/* 160 */       if (itemRequestedThumbnail)
/*     */       {
/* 162 */         Report.trace("thumbnailgenerator", "Creating thumbnail. The binder has CreateServerSideThumbnail: " + itemRequestedThumbnail, null);
/*     */       }
/*     */     }
/*     */ 
/* 166 */     if (docIsSealed)
/*     */     {
/* 168 */       Report.trace("thumbnailgenerator", "ThumbnailGenerator skipping item: " + dDocName + " dID: " + dID + "; document is sealed.", null);
/*     */ 
/* 170 */       createThumbnail = false;
/*     */     }
/* 172 */     if (itemSkipThumbnail)
/*     */     {
/* 174 */       Report.trace("thumbnailgenerator", "ThumbnailGenerator skipping item: " + dDocName + " dID: " + dID + "; by request; 'SkipServerSideThumbnail' set to true.", null);
/*     */ 
/* 177 */       createThumbnail = false;
/*     */     }
/*     */ 
/* 180 */     String format = null;
/* 181 */     if (createThumbnail)
/*     */     {
/* 184 */       String dRendition1 = binder.getActiveAllowMissing("dRendition1");
/* 185 */       String dRendition2 = binder.getActiveAllowMissing("dRendition2");
/* 186 */       if ((dRendition1 != null) && (dRendition2 != null) && (dRendition1.length() > 0) && (dRendition2.length() > 0) && 
/* 189 */         (!dRendition1.contains("T")) && (!dRendition1.contains("G")) && (!dRendition1.contains("P")))
/*     */       {
/* 193 */         Report.trace("thumbnailgenerator", "ThumbnailGenerator skipping item: " + dDocName + " dID: " + dID + "; document has 2 renditions: " + "dRendition1: " + dRendition1 + "; dRendition2: " + dRendition2, null);
/*     */ 
/* 196 */         return false;
/*     */       }
/*     */ 
/* 200 */       if ((exlusiveIncludeList != null) && (exlusiveIncludeList.length() > 0))
/*     */       {
/* 202 */         format = binder.getLocal("dExtension");
/* 203 */         createThumbnail = parseListFindMatch("ServerSideThumbnailExclusiveIncludeExtensionList", format);
/*     */       }
/*     */       else
/*     */       {
/* 207 */         format = binder.getLocal("AlternateFormat");
/* 208 */         if ((format == null) || (format.length() == 0))
/*     */         {
/* 211 */           format = binder.getLocal("dFormat");
/*     */         }
/*     */ 
/* 215 */         createThumbnail = !parseListFindMatch("ThumbnailFormatsExcluded", format);
/*     */       }
/*     */     }
/*     */ 
/* 219 */     Report.trace("thumbnailgenerator", "ThumbnailGenerator format: " + format + "; create thumbnail: " + createThumbnail, null);
/*     */ 
/* 221 */     return createThumbnail;
/*     */   }
/*     */ 
/*     */   protected boolean parseListFindMatch(String listName, String key)
/*     */   {
/* 226 */     String str = SharedObjects.getEnvironmentValue(listName);
/* 227 */     Vector v = StringUtils.parseArray(str, ',', '^');
/* 228 */     int size = v.size();
/* 229 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 231 */       String pattern = ((String)v.elementAt(i)).trim();
/* 232 */       boolean isMatch = StringUtils.match(key, pattern, false);
/* 233 */       if (isMatch)
/*     */       {
/* 235 */         return true;
/*     */       }
/*     */     }
/* 238 */     return false;
/*     */   }
/*     */ 
/*     */   protected void doThumbnail(String inputPath, WebViewableConverterOutput webOutput, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 245 */     String dID = binder.get("dID");
/* 246 */     String dDocName = binder.get("dDocName");
/*     */ 
/* 248 */     String thumbnailType = SharedObjects.getEnvironmentValue("thumbnailformat");
/* 249 */     AdditionalRenditions additionRend = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 251 */     String renFlag = additionRend.getFlag(thumbnailType);
/* 252 */     if (renFlag == null)
/*     */     {
/* 256 */       IdcMessage msg = IdcMessageFactory.lc("csGenerateThumbnailRenditionUndefined", new Object[] { dDocName, dID, thumbnailType });
/*     */ 
/* 258 */       Log.errorEx2(LocaleUtils.encodeMessage(msg), null, new StackTrace());
/* 259 */       return;
/*     */     }
/*     */ 
/* 262 */     long timeout = SharedObjects.getTypedEnvironmentInt("ThumbnailTimeoutInSeconds", 120000, 18, 24);
/*     */ 
/* 267 */     cxt.setCachedObject("jobId", dID);
/* 268 */     cxt.setCachedObject("ConverterLaunchTraceSection", "thumbnailgenerator");
/* 269 */     cxt.setCachedObject("ConvertLaunchMasterTaskId", "ThumbnailExport");
/* 270 */     ExsimpleDriver driver = new ExsimpleDriver(cxt);
/* 271 */     driver.init(dID);
/* 272 */     String thumbnailPath = driver.m_workingDir + "thumbnail." + thumbnailType;
/*     */ 
/* 274 */     boolean isResubmit = (webOutput == null) ? false : webOutput.m_isResubmit;
/* 275 */     String olddRendition1 = null;
/* 276 */     if (isResubmit)
/*     */     {
/* 278 */       olddRendition1 = binder.get("dRendition1");
/* 279 */       if ((olddRendition1.contains("T")) || (olddRendition1.contains("G")) || (olddRendition1.contains("P")))
/*     */       {
/* 285 */         olddRendition1 = null;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 290 */       String thumbnailWidth = SharedObjects.getEnvironmentValue("ThumbnailWidth");
/* 291 */       if (thumbnailWidth == null)
/*     */       {
/* 293 */         thumbnailWidth = "100";
/*     */       }
/* 295 */       String thumbnailHeight = SharedObjects.getEnvironmentValue("ThumbnailHeight");
/* 296 */       if (thumbnailHeight == null)
/*     */       {
/* 298 */         thumbnailHeight = "100";
/*     */       }
/* 300 */       String pageSource = SharedObjects.getEnvironmentValue("exportstartpage");
/* 301 */       if ((pageSource == null) || (pageSource.length() == 0))
/*     */       {
/* 303 */         pageSource = "1";
/*     */       }
/* 305 */       driver.writeIXResourceForConversion(thumbnailHeight, thumbnailWidth, pageSource, thumbnailType);
/* 306 */       int retCode = driver.executeEngine(inputPath, thumbnailPath, timeout);
/* 307 */       if (retCode == 0)
/*     */       {
/* 309 */         FileStoreProvider filestore = FileStoreProviderLoader.initFileStore(cxt);
/* 310 */         binder.putLocal("RenditionId", "rendition:" + renFlag);
/* 311 */         IdcFileDescriptor renDesc = filestore.createDescriptor(binder, null, cxt);
/*     */ 
/* 313 */         HashMap args = new HashMap();
/* 314 */         args.put("isNew", "1");
/* 315 */         args.put("isRetainMetadata", "1");
/* 316 */         filestore.storeFromLocalFile(renDesc, new File(thumbnailPath), args, cxt);
/*     */ 
/* 318 */         binder.putLocal("dRendition1", renFlag);
/* 319 */         binder.putLocal("dMessage", "!csSuccess");
/* 320 */         binder.putLocal("dProcessingState", "Y");
/*     */       }
/*     */       else
/*     */       {
/* 324 */         String stderr = driver.getProcessStdErr();
/* 325 */         Report.trace("thumbnailgenerator", "Thumbnail was not created for Content ID: " + dDocName + ". Process reported: " + stderr, null);
/*     */ 
/* 328 */         IdcMessage msg = IdcMessageFactory.lc("csGenerateThumbnailProcessErr", new Object[] { dDocName, dID, stderr });
/* 329 */         Log.errorEx2(LocaleUtils.encodeMessage(msg), null, new StackTrace());
/*     */ 
/* 331 */         binder.putLocal("dRendition1", "");
/* 332 */         binder.putLocal("dMessage", "!csGenerateThumbnailShortdMessageErr");
/* 333 */         binder.putLocal("dProcessingState", "I");
/* 334 */         if (webOutput != null)
/*     */         {
/* 336 */           webOutput.m_processingState = "I";
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (IOException ioExp)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       boolean doCleanup;
/* 347 */       boolean doCleanup = SharedObjects.getEnvValueAsBoolean("GenerateThumbnailDoCleanup", true);
/* 348 */       if (doCleanup)
/*     */       {
/* 350 */         FileUtils.deleteDirectory(new File(driver.m_workingDir), true);
/*     */       }
/*     */     }
/*     */ 
/* 354 */     if (isResubmit)
/*     */     {
/* 356 */       if (olddRendition1 != null)
/*     */       {
/* 358 */         binder.putLocal("dRendition2", olddRendition1);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 363 */       binder.putLocal("dRendition2", "");
/*     */     }
/* 365 */     ws.execute("UdocConversionState", binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 370 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101791 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.refinery.convert.GenerateThumbnailFilter
 * JD-Core Version:    0.5.4
 */