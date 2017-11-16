/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.filestore.FileStoreProvider;
/*     */ import intradoc.filestore.FileStoreProviderLoader;
/*     */ import intradoc.filestore.IdcFileDescriptor;
/*     */ import intradoc.shared.LegacyDocumentPathBuilder;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class DirectoryLocator extends LegacyDirectoryLocator
/*     */ {
/*  37 */   public static boolean m_disableServices = false;
/*     */ 
/*  39 */   public static FileStoreProvider m_fileStore = null;
/*  40 */   public static Workspace m_workspace = null;
/*     */ 
/*     */   public static String getTempDirectory()
/*     */   {
/*  44 */     String tempDir = SharedObjects.getEnvironmentValue("TempDir");
/*  45 */     if (tempDir != null)
/*     */     {
/*  47 */       return tempDir;
/*     */     }
/*     */ 
/*  50 */     tempDir = SharedObjects.getEnvironmentValue("VaultTempDir");
/*  51 */     if (tempDir != null)
/*     */     {
/*  53 */       return tempDir;
/*     */     }
/*     */     try
/*     */     {
/*  57 */       NativeOsUtils utils = new NativeOsUtils();
/*  58 */       tempDir = utils.getEnv("IDC_TMPDIR");
/*  59 */       if (tempDir != null)
/*     */       {
/*  61 */         return tempDir;
/*     */       }
/*  63 */       tempDir = utils.getEnv("TMPDIR");
/*  64 */       if (tempDir != null)
/*     */       {
/*  66 */         return tempDir;
/*     */       }
/*  68 */       tempDir = utils.getEnv("TEMP");
/*  69 */       if (tempDir != null)
/*     */       {
/*  71 */         return tempDir;
/*     */       }
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/*  76 */       Report.trace(null, null, ignore);
/*     */     }
/*  78 */     String vaultDir = LegacyDirectoryLocator.getVaultDirectory();
/*  79 */     if (FileUtils.checkFile(vaultDir, false, true) == 0)
/*     */     {
/*  81 */       tempDir = vaultDir + "/~temp";
/*  82 */       return tempDir;
/*     */     }
/*  84 */     String[] lastResorts = { "/var/tmp", "/usr/tmp", "c:/tmp", "c:/temp", "/tmp" };
/*     */ 
/*  88 */     for (int i = 0; i < lastResorts.length; ++i)
/*     */     {
/*  90 */       tempDir = lastResorts[i];
/*  91 */       if (FileUtils.checkFile(tempDir, false, true) == 0)
/*     */       {
/*  93 */         return tempDir;
/*     */       }
/*     */     }
/*  96 */     throw new AssertionError("Unable to find a temporary directory.");
/*     */   }
/*     */ 
/*     */   public static String getNativeDirectory()
/*     */   {
/* 105 */     String nativeDir = SharedObjects.getEnvironmentValue("IdcNativeDir");
/* 106 */     if (nativeDir == null)
/*     */     {
/* 108 */       String homeDir = getHomeDirectory();
/* 109 */       nativeDir = FileUtils.getAbsolutePath(homeDir, "native");
/*     */     }
/* 111 */     return nativeDir;
/*     */   }
/*     */ 
/*     */   public static String getLocalDataDir(String subDir)
/*     */   {
/* 125 */     String dir = SharedObjects.getEnvironmentValue("LocalDataDir");
/* 126 */     if (dir != null)
/*     */     {
/* 128 */       return FileUtils.getAbsolutePath(dir, subDir);
/*     */     }
/* 130 */     dir = SharedObjects.getEnvironmentValue("ClusterNodeIntradocDir");
/* 131 */     if (dir != null)
/*     */     {
/* 133 */       return FileUtils.getAbsolutePath(dir + "/data/local", subDir);
/*     */     }
/* 135 */     return null;
/*     */   }
/*     */ 
/*     */   protected static IdcFileDescriptor openFileWithRetryQuery(Parameters info, ExecutionContext context, String renditionId)
/*     */     throws DataException, ServiceException
/*     */   {
/* 142 */     Map map = new HashMap();
/* 143 */     MapParameters params = new MapParameters(map, info);
/* 144 */     map.put("RenditionId", renditionId);
/* 145 */     IdcFileDescriptor file = null;
/*     */     try
/*     */     {
/* 148 */       file = m_fileStore.createDescriptor(params, null, context);
/* 149 */       return file;
/*     */     }
/*     */     catch (DataException dDocName)
/*     */     {
/* 153 */       if (SystemUtils.m_verbose)
/*     */       {
/* 155 */         Report.debug("filestore", "unable to open file so running a query to retry.", e);
/*     */       }
/*     */ 
/* 160 */       String dDocName = info.get("dDocName");
/* 161 */       String dRevLabel = info.get("dRevLabel");
/* 162 */       String dID = info.get("dID");
/* 163 */       if ((dDocName == null) && (dID == null))
/*     */       {
/* 165 */         String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "dDocName");
/*     */ 
/* 167 */         throw new DataException(msg);
/*     */       }
/*     */       String query;
/*     */       String query;
/* 170 */       if (dID != null)
/*     */       {
/* 172 */         map.put("dID", dID);
/* 173 */         query = "QdocInfo";
/*     */       }
/*     */       else
/*     */       {
/* 177 */         if (dRevLabel == null)
/*     */         {
/* 179 */           String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "dRevLabel");
/*     */ 
/* 181 */           throw new DataException(msg);
/*     */         }
/* 183 */         map.put("dDocName", dDocName);
/* 184 */         map.put("dRevLabel", dRevLabel);
/* 185 */         query = "QdocInfoByRevLabel";
/* 186 */         map.put("dID", dID);
/*     */       }
/*     */ 
/* 191 */       params.m_defaultValues = null;
/* 192 */       ResultSet rset = m_workspace.createResultSet(query, params);
/* 193 */       DataResultSet drset = new DataResultSet();
/* 194 */       drset.copy(rset);
/* 195 */       rset = null;
/*     */ 
/* 197 */       Properties props = new Properties();
/* 198 */       if (drset.isRowPresent())
/*     */       {
/* 200 */         props = drset.getCurrentRowProps();
/*     */       }
/* 202 */       PropParameters pp = new PropParameters(props, params);
/* 203 */       file = m_fileStore.createDescriptor(pp, null, context);
/* 204 */     }return file;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebGroupRootDirectory(String securityGroup)
/*     */   {
/* 213 */     checkDisabled();
/* 214 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.getWebGroupRootDirectory()");
/*     */ 
/* 216 */     if (m_fileStore == null)
/*     */     {
/* 218 */       return LegacyDirectoryLocator.getWebGroupRootDirectory(securityGroup);
/*     */     }
/* 220 */     return SharedObjects.getEnvironmentValue("WeblayoutDir") + "groups/" + securityGroup.toLowerCase() + "/";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getVaultDirectory()
/*     */   {
/* 230 */     checkDisabled();
/* 231 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.getVaultDirectory()");
/*     */ 
/* 233 */     if (m_fileStore == null)
/*     */     {
/* 235 */       return LegacyDirectoryLocator.getVaultDirectory();
/*     */     }
/* 237 */     return SharedObjects.getEnvironmentValue("VaultDir");
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   protected static String createAbsoluteAndRelativePair(String suffix, String defaultRel, String baseRoot, String absRoot, String relRoot)
/*     */   {
/* 247 */     checkDisabled();
/* 248 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.createAbsouteAndRelativePair()");
/*     */ 
/* 250 */     if (m_fileStore == null)
/*     */     {
/* 252 */       return LegacyDirectoryLocator.createAbsoluteAndRelativePair(suffix, defaultRel, baseRoot, absRoot);
/*     */     }
/*     */ 
/* 255 */     String relValue = SharedObjects.getEnvironmentValue("Http" + suffix);
/* 256 */     if (relValue == null)
/*     */     {
/* 258 */       relValue = defaultRel;
/* 259 */       SharedObjects.putEnvironmentValue("Http" + suffix, relValue);
/*     */     }
/*     */ 
/* 262 */     String absValue = SharedObjects.getEnvironmentValue("HttpAbsolute" + suffix);
/* 263 */     if (absValue == null)
/*     */     {
/* 265 */       absValue = relValue;
/* 266 */       if (absValue.startsWith("/"))
/*     */       {
/* 268 */         absValue = baseRoot + absValue;
/*     */       }
/* 270 */       else if (!absValue.toLowerCase().startsWith("http://"))
/*     */       {
/* 272 */         absValue = absRoot + absValue;
/*     */       }
/* 274 */       SharedObjects.putEnvironmentValue("HttpAbsolute" + suffix, absValue);
/*     */     }
/*     */ 
/* 277 */     return relValue;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String getWebSecurityGroupRoot(String group, boolean isAbsolute)
/*     */   {
/* 286 */     checkDisabled();
/* 287 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.getWebSecurityGroupRoot()");
/*     */ 
/* 289 */     if (m_fileStore == null)
/*     */     {
/* 291 */       return LegacyDirectoryLocator.getWebSecurityGroupRoot(group, isAbsolute);
/*     */     }
/*     */ 
/* 294 */     String envVarRoot = "Http" + group + "Root";
/* 295 */     String str = SharedObjects.getEnvironmentValue(envVarRoot);
/* 296 */     if (str == null)
/*     */     {
/* 298 */       str = getWebRoot(isAbsolute) + "groups/" + group.toLowerCase() + "/";
/*     */     }
/* 300 */     return str;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeVaultPath(String fileName, Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 311 */     checkDisabled();
/* 312 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeVaultPath()");
/* 313 */     if (m_fileStore == null)
/*     */     {
/* 315 */       return LegacyDirectoryLocator.computeVaultPath(fileName, docInfo);
/*     */     }
/*     */     try
/*     */     {
/* 319 */       ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/*     */ 
/* 321 */       Map localData = new HashMap();
/* 322 */       localData.put("RenditionId", "primaryFile");
/* 323 */       MapParameters params = new MapParameters(localData, docInfo);
/* 324 */       IdcFileDescriptor file = m_fileStore.createDescriptor(params, null, context);
/*     */ 
/* 326 */       String path = m_fileStore.getFilesystemPath(file, context);
/* 327 */       int index = path.lastIndexOf("/");
/* 328 */       String realName = path.substring(index + 1);
/* 329 */       if (!realName.equals(fileName))
/*     */       {
/* 331 */         Report.trace("filestore", "computeVaultPath() computed a name different from its fileName argument.", null);
/*     */ 
/* 333 */         Report.trace("deprecation", "computeVaultPath() computed a name different from its fileName argument.", null);
/*     */       }
/*     */ 
/* 336 */       return path;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 340 */       String msg = LocaleUtils.encodeMessage("csFsUnableToComputePath", null);
/* 341 */       throw new DataException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeVaultFileName(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 351 */     checkDisabled();
/* 352 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeVaultFileName()");
/*     */ 
/* 354 */     if (m_fileStore == null)
/*     */     {
/* 356 */       return LegacyDirectoryLocator.computeVaultFileName(docInfo);
/*     */     }
/*     */     try
/*     */     {
/* 360 */       ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/*     */ 
/* 362 */       IdcFileDescriptor file = openFileWithRetryQuery(docInfo, context, "primaryFile");
/*     */ 
/* 364 */       String path = m_fileStore.getFilesystemPath(file, context);
/* 365 */       int index = path.lastIndexOf("/");
/* 366 */       String fileName = path.substring(index + 1);
/* 367 */       return fileName;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 371 */       String msg = LocaleUtils.encodeMessage("csFsUnableToComputePath", null);
/*     */ 
/* 373 */       throw new DataException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebPathDir(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 383 */     checkDisabled();
/* 384 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeWebPathDir()");
/*     */ 
/* 386 */     if (m_fileStore == null)
/*     */     {
/* 388 */       return LegacyDirectoryLocator.computeWebPathDir(docInfo);
/*     */     }
/* 390 */     String wPathDir = computeDirectory(docInfo, "webViewableFile");
/* 391 */     return wPathDir;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeCurrentReleaseWebPath(String name, Parameters params)
/*     */     throws DataException
/*     */   {
/* 401 */     checkDisabled();
/* 402 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeCurrentReleaseWebPath()");
/* 403 */     if (m_fileStore == null)
/*     */     {
/* 405 */       return LegacyDirectoryLocator.computeCurrentReleaseWebPath(name, params);
/*     */     }
/*     */ 
/* 408 */     String path = null;
/*     */     try
/*     */     {
/* 411 */       ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/* 412 */       IdcFileDescriptor file = openFileWithRetryQuery(params, context, "webViewableFile");
/*     */ 
/* 414 */       path = m_fileStore.getFilesystemPath(file, context);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 418 */       String msg = LocaleUtils.encodeMessage("csFsUnableToComputePath", null);
/* 419 */       throw new DataException(msg, e);
/*     */     }
/* 421 */     return path;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebSecurityPathDir(Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 430 */     checkDisabled();
/* 431 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeWebSecurityPathDir()");
/*     */ 
/* 433 */     if (m_fileStore == null)
/*     */     {
/* 435 */       return LegacyDirectoryLocator.computeWebSecurityPathDir(docInfo);
/*     */     }
/* 437 */     String securityGroup = docInfo.get("dSecurityGroup");
/* 438 */     String groupDir = getWebGroupRootDirectory(securityGroup);
/*     */ 
/* 440 */     String wPathDir = groupDir + LegacyDocumentPathBuilder.computeWebDirSecuritySuffix(docInfo);
/* 441 */     return wPathDir;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String computeWebPath(String fileName, Parameters docInfo)
/*     */     throws DataException
/*     */   {
/* 450 */     checkDisabled();
/* 451 */     SystemUtils.reportDeprecatedUsage("DirectoryLocator.computeWebPath()");
/*     */ 
/* 453 */     if (m_fileStore == null)
/*     */     {
/* 455 */       return LegacyDirectoryLocator.computeWebPath(fileName, docInfo);
/*     */     }
/* 457 */     String wPath = computeWebPathDir(docInfo) + fileName.toLowerCase();
/* 458 */     return wPath;
/*     */   }
/*     */ 
/*     */   public static void checkDisabled()
/*     */   {
/* 463 */     if (m_disableServices)
/*     */     {
/* 465 */       throw new AssertionError("!csFsDirectoryLocatorDisabled");
/*     */     }
/* 467 */     if (m_fileStore != null)
/*     */       return;
/*     */     try
/*     */     {
/* 471 */       m_fileStore = FileStoreProviderLoader.initFileStore(new ExecutionContextAdaptor());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 476 */       Report.trace("filestore", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String computeDirectory(Parameters params, String rendition)
/*     */     throws DataException
/*     */   {
/* 484 */     if (m_fileStore == null)
/*     */     {
/* 486 */       throw new AssertionError("DirectoryLocator.computeDirectory() called before initialization complete.");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 491 */       ExecutionContextAdaptor context = new ExecutionContextAdaptor();
/*     */ 
/* 493 */       Properties localData = new Properties();
/* 494 */       params = new PropParameters(localData, params);
/*     */ 
/* 496 */       localData.put("RenditionId", rendition);
/* 497 */       IdcFileDescriptor descriptor = m_fileStore.createDescriptor(params, null, context);
/* 498 */       String dir = m_fileStore.getContainerPath(descriptor, null, context);
/* 499 */       return dir;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 503 */       DataException de = new DataException(e.getMessage());
/* 504 */       SystemUtils.setExceptionCause(de, e);
/* 505 */       throw de;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 511 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80368 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DirectoryLocator
 * JD-Core Version:    0.5.4
 */