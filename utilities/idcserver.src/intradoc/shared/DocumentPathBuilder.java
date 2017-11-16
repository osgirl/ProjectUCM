/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.DynamicHtmlMerger;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import java.util.HashMap;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DocumentPathBuilder
/*     */ {
/*  38 */   public static boolean m_allLowerCase = true;
/*  39 */   protected static FileStoreProvider m_fileStore = null;
/*  40 */   protected static boolean m_initDone = false;
/*     */ 
/*     */   public static String getRelativeWebRoot()
/*     */   {
/*  48 */     String str = SharedObjects.getEnvironmentValue("HttpRelativeWebRoot");
/*  49 */     if (str == null)
/*     */     {
/*  51 */       return str = "/";
/*     */     }
/*  53 */     return str;
/*     */   }
/*     */ 
/*     */   public static String getAbsoluteWebRoot()
/*     */   {
/*  63 */     String str = SharedObjects.getEnvironmentValue("HttpAbsoluteWebRoot");
/*  64 */     if (str == null)
/*     */     {
/*  66 */       String domain = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  67 */       String relRoot = getRelativeWebRoot();
/*  68 */       if (domain == null)
/*     */       {
/*  70 */         return relRoot;
/*     */       }
/*  72 */       boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*     */ 
/*  74 */       String httpPrefix = (isSSL) ? "https://" : "http://";
/*  75 */       str = httpPrefix + domain + relRoot;
/*     */     }
/*  77 */     return str;
/*     */   }
/*     */ 
/*     */   public static String getBaseAbsoluteRoot()
/*     */   {
/*  87 */     String str = SharedObjects.getEnvironmentValue("HttpAbsoluteWebRoot");
/*  88 */     if (str == null)
/*     */     {
/*  90 */       String domain = SharedObjects.getEnvironmentValue("HttpServerAddress");
/*  91 */       if (domain == null)
/*     */       {
/*  93 */         domain = "localhost";
/*     */       }
/*  95 */       boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*     */ 
/*  97 */       String httpPrefix = (isSSL) ? "https://" : "http://";
/*  98 */       str = httpPrefix + domain;
/*     */     }
/* 100 */     return str;
/*     */   }
/*     */ 
/*     */   public static void init(ExecutionContext context)
/*     */   {
/* 105 */     if (m_initDone)
/*     */     {
/* 107 */       return;
/*     */     }
/* 109 */     Exception exception = null;
/*     */     try
/*     */     {
/* 112 */       if (context == null)
/*     */       {
/* 114 */         context = new ExecutionContextAdaptor();
/*     */       }
/* 116 */       m_fileStore = FileStoreProviderLoader.initFileStore(context);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 120 */       exception = e;
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 124 */       exception = e;
/*     */     }
/* 126 */     if (exception != null)
/*     */     {
/* 128 */       Report.trace("filestore", null, exception);
/*     */     }
/* 130 */     m_initDone = true;
/*     */   }
/*     */ 
/*     */   public static void deprecatedMethod(String name) throws DataException
/*     */   {
/* 135 */     init(null);
/* 136 */     String msg = LocaleUtils.encodeMessage("csFsDeprecatedAccessToFileSystem", null, name);
/*     */ 
/* 138 */     SystemUtils.reportDeprecatedUsage(msg);
/*     */   }
/*     */ 
/*     */   public static boolean useLegacy()
/*     */   {
/* 143 */     init(null);
/* 144 */     return m_fileStore == null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirSecuritySuffix(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 155 */     deprecatedMethod("computeWebDirSecuritySuffix");
/* 156 */     if (useLegacy())
/*     */     {
/* 158 */       return LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(docInfo, isActive);
/*     */     }
/*     */ 
/* 161 */     String docAccount = docInfo.get("dDocAccount", isActive);
/* 162 */     String webDirSuffix = "";
/* 163 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 165 */       webDirSuffix = "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/* 167 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirSecuritySuffix(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 176 */     deprecatedMethod("computeWebDirSecuritySuffix");
/* 177 */     if (useLegacy())
/*     */     {
/* 179 */       return LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(docInfo);
/*     */     }
/* 181 */     String docAccount = docInfo.get("dDocAccount");
/* 182 */     String webDirSuffix = "";
/* 183 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 185 */       webDirSuffix = "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/* 187 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirSuffix(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 200 */     deprecatedMethod("computeWebDirSuffix");
/* 201 */     if (useLegacy())
/*     */     {
/* 203 */       return LegacyDocumentPathBuilder.computeWebDirSuffix(docInfo, isActive);
/*     */     }
/* 205 */     String webDirSuffix = computeWebDirSecuritySuffix(docInfo, isActive);
/* 206 */     webDirSuffix = webDirSuffix + "documents/" + docInfo.get("dDocType", isActive).toLowerCase() + "/";
/*     */ 
/* 208 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirSuffix(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 217 */     deprecatedMethod("computeWebDirSuffix");
/* 218 */     if (useLegacy())
/*     */     {
/* 220 */       return LegacyDocumentPathBuilder.computeWebDirSuffix(docInfo);
/*     */     }
/* 222 */     String webDirSuffix = computeWebDirSecuritySuffix(docInfo);
/* 223 */     webDirSuffix = webDirSuffix + "documents/" + docInfo.get("dDocType").toLowerCase() + "/";
/*     */ 
/* 225 */     return webDirSuffix;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirPartialPath(DataBinder docInfo, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 236 */     deprecatedMethod("computeWebDirPartialPath");
/* 237 */     if (useLegacy())
/*     */     {
/* 239 */       return LegacyDocumentPathBuilder.computeWebDirPartialPath(docInfo, isActive);
/*     */     }
/* 241 */     String securityGroup = docInfo.get("dSecurityGroup", isActive);
/* 242 */     String wPartialPath = "groups/" + securityGroup.toLowerCase() + "/";
/* 243 */     wPartialPath = wPartialPath + computeWebDirSuffix(docInfo, isActive);
/* 244 */     return wPartialPath;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebDirPartialPath(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 253 */     deprecatedMethod("computeWebDirPartialPath");
/* 254 */     if (useLegacy())
/*     */     {
/* 256 */       return LegacyDocumentPathBuilder.computeWebDirPartialPath(docInfo);
/*     */     }
/* 258 */     String securityGroup = docInfo.get("dSecurityGroup");
/* 259 */     String wPartialPath = "groups/" + securityGroup.toLowerCase() + "/";
/* 260 */     wPartialPath = wPartialPath + computeWebDirSuffix(docInfo);
/* 261 */     return wPartialPath;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebUrlDir(DataBinder docInfo, boolean isAbsolute, boolean isActive)
/*     */     throws DataException
/*     */   {
/* 276 */     deprecatedMethod("computeWebUrlDir");
/* 277 */     if (useLegacy())
/*     */     {
/* 279 */       return LegacyDocumentPathBuilder.computeWebUrlDir(docInfo, isAbsolute, isActive);
/*     */     }
/*     */     String root;
/*     */     String root;
/* 282 */     if (isAbsolute)
/*     */     {
/* 284 */       root = getAbsoluteWebRoot();
/*     */     }
/*     */     else
/*     */     {
/* 288 */       root = getRelativeWebRoot();
/*     */     }
/* 290 */     String wDir = root + computeWebDirPartialPath(docInfo, isActive);
/* 291 */     return wDir;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebUrlDir(Parameters docInfo, boolean isAbsolute)
/*     */     throws DataException
/*     */   {
/* 301 */     deprecatedMethod("computeWebUrlDir");
/* 302 */     if (useLegacy())
/*     */     {
/* 304 */       return LegacyDocumentPathBuilder.computeWebUrlDir(docInfo, isAbsolute);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 309 */       Properties localData = new Properties();
/* 310 */       docInfo = new PropParameters(localData, docInfo);
/* 311 */       localData.put("RenditionId", "webViewableFile");
/*     */ 
/* 315 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/* 316 */       Provider provider = Providers.getProvider("SystemDatabase");
/* 317 */       Workspace workspace = (Workspace)provider.getProvider();
/* 318 */       cxt.setCachedObject("Workspace", workspace);
/*     */ 
/* 320 */       IdcFileDescriptor descriptor = m_fileStore.createDescriptor(docInfo, null, cxt);
/* 321 */       HashMap args = new HashMap();
/* 322 */       args.put("useAbsolute", (isAbsolute) ? "1" : "0");
/* 323 */       String url = m_fileStore.getClientURL(descriptor, null, args, cxt);
/*     */ 
/* 327 */       String cgiRoot = SharedObjects.getEnvironmentValue("HttpRelativeCgiRoot");
/*     */ 
/* 329 */       if (cgiRoot == null)
/*     */       {
/* 331 */         cgiRoot = getRelativeWebRoot();
/*     */       }
/* 333 */       String cgiFileName = SharedObjects.getEnvironmentValue("CgiFileName");
/* 334 */       if (cgiFileName == null)
/*     */       {
/* 336 */         cgiFileName = "idcplg";
/*     */       }
/* 338 */       String cgiUrl = cgiRoot + cgiFileName;
/* 339 */       if (url.indexOf(cgiUrl) >= 0)
/*     */       {
/* 341 */         int index = url.indexOf("/", 1);
/* 342 */         if (index > 0)
/*     */         {
/* 344 */           url = url.substring(index + 1);
/*     */         }
/* 346 */         return url + "&webFileName=";
/*     */       }
/* 348 */       String dir = FileUtils.getDirectory(url);
/* 349 */       return FileUtils.directorySlashes(dir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 355 */       DataException de = new DataException("!apUnableToViewUrl");
/* 356 */       if (SystemUtils.m_verbose)
/*     */       {
/* 358 */         Report.debug("filestore", null, e);
/*     */       }
/* 360 */       SystemUtils.setExceptionCause(de, e);
/* 361 */       throw de;
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeRelativeVaultDir(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 372 */     deprecatedMethod("computeRelativeVaultDir");
/* 373 */     if (useLegacy())
/*     */     {
/* 375 */       return LegacyDocumentPathBuilder.computeRelativeVaultDir(docInfo);
/*     */     }
/* 377 */     String docType = docInfo.get("dDocType");
/* 378 */     String docAccount = docInfo.get("dDocAccount");
/*     */ 
/* 380 */     String relativeVaultDir = docType.toLowerCase() + "/";
/* 381 */     if ((docAccount != null) && (docAccount.trim().length() > 0))
/*     */     {
/* 383 */       relativeVaultDir = relativeVaultDir + "@" + convertToAccountDirStr(docAccount) + "/";
/*     */     }
/*     */ 
/* 386 */     return relativeVaultDir;
/*     */   }
/*     */ 
/*     */   protected static String convertToAccountDirStr(String acct)
/*     */   {
/* 391 */     int len = acct.length();
/* 392 */     StringBuffer b = new StringBuffer(len * 2);
/*     */ 
/* 394 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 396 */       char ch = acct.charAt(i);
/* 397 */       if (m_allLowerCase)
/*     */       {
/* 399 */         ch = Character.toLowerCase(ch);
/*     */       }
/* 401 */       b.append(ch);
/* 402 */       if (ch != '/')
/*     */         continue;
/* 404 */       b.append('@');
/*     */     }
/*     */ 
/* 408 */     return b.toString();
/*     */   }
/*     */ 
/*     */   public static String computeRenditionFilename(Parameters docInfo, int counter)
/*     */     throws DataException
/*     */   {
/* 414 */     String docName = docInfo.get("dDocName");
/* 415 */     String renFlag = docInfo.get("dRendition" + counter);
/* 416 */     String revLabel = docInfo.get("dRevLabel");
/* 417 */     String ext = docInfo.get("renExtension" + counter);
/*     */ 
/* 419 */     return computeRenditionFilename(docName, renFlag, revLabel, ext);
/*     */   }
/*     */ 
/*     */   public static String computeRenditionFilename(String docName, String renFlag, String revLabel, String ext)
/*     */   {
/* 424 */     String filename = docName.toLowerCase() + computeRenditionTail(renFlag, revLabel, ext);
/* 425 */     return filename;
/*     */   }
/*     */ 
/*     */   public static String computeRenditionTail(String renFlag, String revLabel, String ext)
/*     */   {
/* 430 */     String tail = "@" + renFlag + "~" + revLabel;
/* 431 */     if ((ext != null) && (ext.trim().length() > 0))
/*     */     {
/* 433 */       tail = tail + "." + ext;
/*     */     }
/*     */ 
/* 436 */     return tail.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String computeWebFileExtension(Parameters docInfo, boolean isReleased)
/*     */     throws DataException
/*     */   {
/* 442 */     String revLabel = docInfo.get("dRevLabel").toLowerCase();
/* 443 */     String ext = docInfo.get("dWebExtension").toLowerCase();
/*     */ 
/* 445 */     String tail = "";
/*     */ 
/* 447 */     if (!isReleased)
/*     */     {
/* 449 */       tail = tail + "~" + revLabel;
/*     */     }
/*     */ 
/* 452 */     if ((ext != null) && (ext.trim().length() > 0))
/*     */     {
/* 454 */       tail = tail + "." + ext;
/*     */     }
/*     */ 
/* 457 */     return tail.toLowerCase();
/*     */   }
/*     */ 
/*     */   public static String evaluatePathScript(String path, DataBinder binder, int flags, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 475 */     if ((path == null) || (path.indexOf('$') < 0))
/*     */     {
/* 478 */       return path;
/*     */     }
/* 480 */     if (cxt == null)
/*     */     {
/* 482 */       cxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 485 */     DynamicHtmlMerger merger = (DynamicHtmlMerger)cxt.getCachedObject("PageMerger");
/* 486 */     PathVariableLookupForScript lookupForScript = new PathVariableLookupForScript(binder, cxt, merger);
/* 487 */     Properties envProps = SharedObjects.getSecureEnvironment();
/* 488 */     flags |= PathUtils.F_SEARCH_PROPS_LAST;
/* 489 */     return PathUtils.substitutePathVariables(path, envProps, lookupForScript, flags, cxt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String extractFileNameFromUrl(String path)
/*     */   {
/* 498 */     return LegacyDocumentPathBuilder.extractFileNameFromUrl(path);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String extractDocNameFromFileName(String filename)
/*     */   {
/* 506 */     return LegacyDocumentPathBuilder.extractDocNameFromFileName(filename);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 511 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70184 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DocumentPathBuilder
 * JD-Core Version:    0.5.4
 */