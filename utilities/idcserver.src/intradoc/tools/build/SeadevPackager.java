/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.io.HTTPDownloader.StateListener;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.File;
/*     */ import java.net.InetAddress;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class SeadevPackager extends ModulesContainer
/*     */ {
/*     */   public static final String SVN_USERNAME = "adebuild";
/*     */   public static final String SVN_PASSWORD = "readonly";
/*     */   public static final String COSMOS_HOSTNAME = "cosmos.us.oracle.com";
/*     */   public Map<String, String> m_seadevHeaders;
/*     */   public String m_seadevURLBase;
/*     */   public String m_canonicalHostname;
/*     */ 
/*     */   public void initHeaders()
/*     */     throws IdcException
/*     */   {
/*  51 */     DataBinder config = this.m_buildConfig;
/*  52 */     BuildEnvironment env = this.m_manager.m_env;
/*  53 */     Properties props = env.m_properties;
/*  54 */     int pathFlags = PathUtils.F_KEEP_UNKNOWN_VARS;
/*  55 */     String base = config.getLocal("URLBase");
/*  56 */     base = PathUtils.substitutePathVariables(base, props, null, pathFlags, env.m_context);
/*  57 */     this.m_seadevURLBase = base;
/*  58 */     String authorization = config.getLocal("Authorization");
/*  59 */     if (authorization != null)
/*     */     {
/*  61 */       Map headers = this.m_seadevHeaders = new HashMap();
/*  62 */       headers.put("Authorization", authorization);
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  67 */       InetAddress address = InetAddress.getByName("cosmos.us.oracle.com");
/*  68 */       this.m_canonicalHostname = address.getCanonicalHostName();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/*  72 */       throw new ServiceException("unable to determine hostname for cosmos.us.oracle.com", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadModules()
/*     */     throws IdcException
/*     */   {
/*  80 */     File modulesDir = this.m_modulesDir;
/*  81 */     BuildManager manager = this.m_manager;
/*  82 */     BuildEnvironment env = manager.m_env;
/*  83 */     GenericTracingCallback trace = env.m_trace;
/*  84 */     DataBinder buildConfig = this.m_buildConfig;
/*  85 */     initHeaders();
/*  86 */     DataResultSet modulesTable = (DataResultSet)buildConfig.getResultSet("Modules");
/*  87 */     Map modules = env.m_modules;
/*  88 */     int nameIndex = ResultSetUtils.getIndexMustExist(modulesTable, "name");
/*  89 */     int dDocNameIndex = ResultSetUtils.getIndexMustExist(modulesTable, "dDocName");
/*  90 */     for (modulesTable.first(); modulesTable.isRowPresent(); modulesTable.next())
/*     */     {
/*  92 */       String moduleName = modulesTable.getStringValue(nameIndex);
/*  93 */       String dDocName = modulesTable.getStringValue(dDocNameIndex);
/*  94 */       File moduleDir = new File(modulesDir, moduleName);
/*  95 */       moduleDir.mkdir();
/*  96 */       DataBinder binder = manager.loadBuildConfigBinder(moduleDir, false);
/*  97 */       SeadevModule module = new SeadevModule(dDocName);
/*  98 */       module.m_buildConfig = binder;
/*  99 */       module.init(manager, moduleDir);
/* 100 */       module.reload();
/* 101 */       moduleName = module.m_moduleName = "seadev-" + moduleName;
/* 102 */       if (modules.containsKey(moduleName))
/*     */       {
/* 104 */         trace.report(4, new Object[] { "Module ", moduleName, " already exists" });
/*     */       }
/*     */       else
/* 107 */         modules.put(moduleName, module);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 422 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100460 $";
/*     */   }
/*     */ 
/*     */   public class SeadevComponent extends Component
/*     */   {
/*     */     public String m_parentModuleName;
/*     */     public String m_componentLocation;
/*     */ 
/*     */     protected SeadevComponent(String parentModuleName, String componentLocation)
/*     */     {
/* 356 */       this.m_parentModuleName = parentModuleName;
/* 357 */       this.m_componentLocation = componentLocation;
/*     */     }
/*     */ 
/*     */     public void init(BuildManager manager, File moduleDir, String componentName) throws IdcException
/*     */     {
/* 362 */       super.init(manager, moduleDir, null);
/* 363 */       this.m_moduleName = componentName;
/* 364 */       String[] depends = this.m_requiredModules = new String[1];
/* 365 */       depends[0] = this.m_parentModuleName;
/* 366 */       if (this.m_componentLocation.endsWith(".hda"))
/*     */       {
/* 368 */         this.m_componentBinderFile = new File(moduleDir, this.m_componentLocation);
/* 369 */         this.m_moduleDir = this.m_componentBinderFile.getParentFile();
/*     */       }
/*     */       else
/*     */       {
/* 373 */         this.m_componentBinderFile = null;
/*     */       }
/* 375 */       this.m_componentZipFile = new File(moduleDir, componentName + ".zip");
/*     */     }
/*     */ 
/*     */     public void appendClasspathTo(List<String> classpath, int flags)
/*     */       throws IdcException
/*     */     {
/* 386 */       if (this.m_componentBinder == null)
/*     */       {
/* 392 */         DataBinder componentBinder = this.m_componentBinder = new DataBinder();
/* 393 */         String filename = this.m_moduleName + ".hda";
/* 394 */         ResourceUtils.serializeDataBinderWithEncoding(this.m_moduleDirname, filename, componentBinder, 0, null);
/*     */       }
/* 396 */       super.appendClasspathTo(classpath, flags);
/*     */     }
/*     */ 
/*     */     public boolean buildAndPackage(File targetDir)
/*     */       throws IdcException
/*     */     {
/* 405 */       BuildEnvironment env = this.m_manager.m_env;
/*     */ 
/* 407 */       super.buildAndPackage(targetDir);
/*     */ 
/* 409 */       if (this.m_componentLocation.endsWith(".zip"))
/*     */       {
/* 411 */         File componentZipFile = this.m_componentZipFile = new File(this.m_moduleDir, this.m_componentLocation);
/* 412 */         File targetFile = new File(env.m_componentZipsDir, componentZipFile.getName());
/* 413 */         BuildUtils.copyOutdatedFile(componentZipFile, targetFile, env.m_trace);
/*     */       }
/* 415 */       return true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public class SeadevModule extends Module
/*     */   {
/*     */     public boolean m_isInitialized;
/*     */     public String m_dDocName;
/*     */     public DataBinder m_docInfoBinder;
/*     */     protected File m_docInfoBinderFile;
/*     */     protected Module.FetchRule m_docInfoFetchRule;
/*     */     protected Module.FetchRule m_getFileFetchRule;
/*     */     protected Module.ExtractRule m_extractRule;
/*     */ 
/*     */     protected SeadevModule(String dDocName)
/*     */     {
/* 123 */       this.m_dDocName = dDocName;
/*     */     }
/*     */ 
/*     */     public void updateDependencies(int flags, HTTPDownloader.StateListener callback)
/*     */       throws IdcException
/*     */     {
/* 133 */       if ((flags & 0x80) != 0)
/*     */         return;
/* 135 */       if ((flags & 0x8) == 0)
/*     */       {
/* 138 */         checkFetchPrebuiltBundle(flags, callback);
/*     */       }
/* 140 */       if ((flags & 0x4) != 0) {
/*     */         return;
/*     */       }
/* 143 */       super.updateDependencies(flags, callback);
/*     */     }
/*     */ 
/*     */     protected void checkFetchPrebuiltBundle(int flags, HTTPDownloader.StateListener callback)
/*     */       throws IdcException
/*     */     {
/* 151 */       String moduleDirname = this.m_moduleDirname; String moduleName = this.m_moduleName;
/* 152 */       String dDocName = this.m_dDocName;
/* 153 */       StringBuilder sb = new StringBuilder();
/*     */ 
/* 155 */       File docInfoBinderFile = this.m_docInfoBinderFile;
/*     */       String docInfoBinderPath;
/* 157 */       if (docInfoBinderFile == null)
/*     */       {
/* 159 */         sb.append(moduleDirname);
/* 160 */         sb.append('/');
/* 161 */         sb.append(moduleName);
/* 162 */         sb.append(".hda");
/* 163 */         String docInfoBinderPath = sb.toString();
/* 164 */         sb.setLength(0);
/* 165 */         docInfoBinderFile = this.m_docInfoBinderFile = new File(docInfoBinderPath);
/*     */       }
/*     */       else
/*     */       {
/* 169 */         docInfoBinderPath = docInfoBinderFile.getPath();
/*     */       }
/*     */ 
/* 172 */       DataBinder docInfo = this.m_manager.loadOrCreateBinder(docInfoBinderFile);
/* 173 */       String dID = docInfo.getAllowMissing("dID");
/* 174 */       String dOriginalNameOriginal = docInfo.getAllowMissing("dOriginalName");
/*     */ 
/* 177 */       Module.FetchRule rule = this.m_docInfoFetchRule;
/* 178 */       if (rule == null)
/*     */       {
/* 180 */         sb.append(SeadevPackager.this.m_seadevURLBase);
/* 181 */         sb.append("?IdcService=DOC_INFO_BY_NAME&IsJava=1&dDocName=");
/* 182 */         sb.append(dDocName);
/* 183 */         String sourceURL = sb.toString();
/* 184 */         sb.setLength(0);
/* 185 */         rule = this.m_docInfoFetchRule = createFetchRule(sourceURL, null, null);
/* 186 */         rule.m_targetPath = docInfoBinderPath;
/* 187 */         rule.m_targetFile = docInfoBinderFile;
/* 188 */         rule.m_extraHTTPHeaders = SeadevPackager.this.m_seadevHeaders;
/*     */       }
/* 190 */       docInfoBinderFile.delete();
/* 191 */       checkFetchDependency(rule.m_sourcePath, rule.m_targetPath, rule, flags | 0x10, callback);
/* 192 */       if ((flags & 0x100) != 0)
/*     */       {
/* 194 */         return;
/*     */       }
/* 196 */       String binderFilename = docInfoBinderFile.getName();
/* 197 */       docInfo = new DataBinder();
/* 198 */       ResourceUtils.serializeDataBinderWithEncoding(moduleDirname, binderFilename, docInfo, 0, null);
/* 199 */       this.m_docInfoBinder = docInfo;
/* 200 */       String dOriginalName = docInfo.get("dOriginalName");
/* 201 */       this.m_properties.setProperty("seadevFile", dOriginalName);
/* 202 */       if ((dOriginalNameOriginal != null) && (!dOriginalName.equals(dOriginalNameOriginal)))
/*     */       {
/* 204 */         File originalFile = new File(moduleDirname, dOriginalNameOriginal);
/* 205 */         originalFile.delete();
/*     */       }
/*     */ 
/* 209 */       rule = this.m_getFileFetchRule;
/* 210 */       if (rule == null)
/*     */       {
/* 212 */         rule = this.m_getFileFetchRule = new Module.FetchRule();
/*     */ 
/* 214 */         sb.append(SeadevPackager.this.m_seadevURLBase);
/* 215 */         sb.append("?IdcService=GET_FILE&RevisionSelectionMethod=LatestReleased&dDocName=");
/* 216 */         sb.append(dDocName);
/* 217 */         rule.m_source = (rule.m_sourcePath = sb.toString());
/* 218 */         sb.setLength(0);
/* 219 */         rule.m_isSourceRemote = true;
/* 220 */         rule.m_extraHTTPHeaders = SeadevPackager.this.m_seadevHeaders;
/* 221 */         rule.m_shouldIgnoreLastModified = true;
/*     */ 
/* 223 */         this.m_extractRules = new ArrayList();
/* 224 */         Module.ExtractRule extractRule = this.m_extractRule = new Module.ExtractRule();
/* 225 */         this.m_extractRules.add(extractRule);
/* 226 */         extractRule.m_targetFile = this.m_moduleDir;
/* 227 */         extractRule.m_targetFilename = new StringBuilder().append(moduleDirname).append('/').toString();
/* 228 */         extractRule.m_isTargetDirectory = true;
/*     */       }
/* 230 */       Module.ExtractRule extractRule = this.m_extractRule;
/* 231 */       sb.append(moduleDirname);
/* 232 */       sb.append('/');
/* 233 */       sb.append(dOriginalName);
/* 234 */       rule.m_targetPath = sb.toString();
/* 235 */       File bundleFile = rule.m_targetFile = new File(moduleDirname, dOriginalName);
/* 236 */       extractRule.m_sourceFile = bundleFile;
/* 237 */       if ((dID != null) && (!dID.equals(docInfo.get("dID"))))
/*     */       {
/* 240 */         bundleFile.delete();
/*     */       }
/*     */       try
/*     */       {
/* 244 */         checkFetchDependency(rule.m_sourcePath, rule.m_targetPath, rule, flags, callback);
/*     */       }
/*     */       catch (IdcException e)
/*     */       {
/* 248 */         bundleFile.delete();
/* 249 */         throw e;
/*     */       }
/*     */ 
/* 252 */       updateFromPrebuiltBundle();
/*     */     }
/*     */ 
/*     */     protected void updateFromPrebuiltBundle() throws IdcException
/*     */     {
/* 257 */       if (this.m_isInitialized)
/*     */         return;
/* 259 */       BuildManager manager = this.m_manager;
/* 260 */       BuildEnvironment env = manager.m_env;
/* 261 */       ExecutionContext context = env.m_context;
/* 262 */       Properties props = this.m_properties;
/* 263 */       int pathUtilsFlags = PathUtils.F_VARS_MUST_EXIST;
/* 264 */       Map modules = env.m_modules;
/* 265 */       List moduleNames = env.m_sortedModuleNames;
/* 266 */       String moduleName = this.m_moduleName;
/* 267 */       File moduleDir = this.m_moduleDir;
/* 268 */       DataBinder binder = this.m_buildConfig;
/* 269 */       DataResultSet components = (DataResultSet)binder.getResultSet("Components");
/* 270 */       if (components != null)
/*     */       {
/* 273 */         int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 274 */         int locationIndex = ResultSetUtils.getIndexMustExist(components, "location");
/* 275 */         int productTagsIndex = ResultSetUtils.getIndexMustExist(components, "productTags");
/* 276 */         for (components.first(); components.isRowPresent(); components.next())
/*     */         {
/* 278 */           String componentName = components.getStringValue(nameIndex);
/* 279 */           String productTagsString = components.getStringValue(productTagsIndex);
/* 280 */           String locationString = components.getStringValue(locationIndex);
/* 281 */           locationString = PathUtils.substitutePathVariables(locationString, props, null, pathUtilsFlags, context);
/*     */ 
/* 283 */           if ((!locationString.endsWith(".zip")) && (!locationString.endsWith(".hda")))
/*     */           {
/* 285 */             String msg = new StringBuilder().append("bad component location: ").append(locationString).toString();
/* 286 */             throw new ServiceException(msg);
/*     */           }
/* 288 */           SeadevPackager.SeadevComponent component = new SeadevPackager.SeadevComponent(SeadevPackager.this, moduleName, locationString);
/* 289 */           component.init(manager, moduleDir, componentName);
/* 290 */           component.m_productTags.addFromString(productTagsString);
/* 291 */           component.m_buildConfig = manager.loadBuildConfigBinder(moduleDir, false);
/* 292 */           component.reload();
/* 293 */           modules.put(componentName, component);
/* 294 */           moduleNames.add(componentName);
/*     */         }
/*     */       }
/*     */ 
/* 298 */       this.m_isInitialized = true;
/*     */     }
/*     */ 
/*     */     public void addToLabelManifest()
/*     */       throws IdcException
/*     */     {
/* 305 */       DataBinder docInfo = this.m_docInfoBinder;
/* 306 */       if (docInfo != null)
/*     */       {
/* 308 */         String xSourceLocation = docInfo.getAllowMissing("xSourceLocation");
/* 309 */         String xSourceRevision = docInfo.getAllowMissing("xSourceRevision");
/* 310 */         if ((xSourceLocation != null) && (xSourceLocation.length() > 0))
/*     */         {
/* 312 */           List manifest = this.m_manager.m_env.m_labelManifest;
/* 313 */           StringBuilder sb = new StringBuilder("source\t");
/* 314 */           sb.append("source/cosmos/");
/* 315 */           sb.append(this.m_moduleDir.getName());
/* 316 */           sb.append(" DO checkout \\");
/* 317 */           manifest.add(sb.toString());
/* 318 */           sb.setLength(0);
/* 319 */           sb.append("\t\t");
/*     */ 
/* 321 */           int cosmosIndex = xSourceLocation.indexOf("cosmos.us.oracle.com");
/* 322 */           if (cosmosIndex < 0)
/*     */           {
/* 324 */             sb.append(xSourceLocation);
/*     */           }
/*     */           else
/*     */           {
/* 328 */             sb.append(xSourceLocation.substring(0, cosmosIndex));
/* 329 */             sb.append(SeadevPackager.this.m_canonicalHostname);
/* 330 */             sb.append(xSourceLocation.substring(cosmosIndex + "cosmos.us.oracle.com".length()));
/*     */           }
/* 332 */           if ((xSourceRevision != null) && (xSourceRevision.length() > 0))
/*     */           {
/* 334 */             sb.append(" --revision ");
/* 335 */             sb.append(xSourceRevision);
/*     */           }
/* 337 */           sb.append(" --username ");
/* 338 */           sb.append("adebuild");
/* 339 */           sb.append(" --password ");
/* 340 */           sb.append("readonly");
/* 341 */           manifest.add(sb.toString());
/*     */         }
/*     */       }
/* 344 */       super.addToLabelManifest();
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.SeadevPackager
 * JD-Core Version:    0.5.4
 */