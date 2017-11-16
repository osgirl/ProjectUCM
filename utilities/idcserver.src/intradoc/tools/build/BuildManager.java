/*      */ package intradoc.tools.build;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.PathUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.io.HTTPDownloader.State;
/*      */ import intradoc.io.HTTPDownloader.StateListener;
/*      */ import intradoc.loader.IdcClassLoader;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.serialize.DataBinderSerializer;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.DataLoader;
/*      */ import intradoc.server.IdcSystemConfig;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.tools.common.IdcClassLoaderWrapper;
/*      */ import intradoc.tools.common.JavaCompileManager;
/*      */ import intradoc.tools.common.SVNManager;
/*      */ import intradoc.tools.common.TextFileReader;
/*      */ import intradoc.tools.common.VersionInfoClassFileEditor;
/*      */ import intradoc.util.CollectionUtils;
/*      */ import intradoc.util.GenericTracingCallback;
/*      */ import intradoc.util.IdcException;
/*      */ import intradoc.util.IdcMessageUtils;
/*      */ import intradoc.util.PatternFilter;
/*      */ import intradoc.zip.IdcZipFunctions;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.lang.reflect.Constructor;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.lang.reflect.Method;
/*      */ import java.net.URL;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ 
/*      */ public class BuildManager
/*      */   implements GenericTracingCallback, HTTPDownloader.StateListener
/*      */ {
/*      */   public static final String BRANCH_URL_PRODUCT_PREFIX = "svn/idc/products/";
/*      */   public static final String CFG_HEADER = "<?cfg jcharset=\"UTF-8\"?>\n";
/*      */   public static final String DEFAULT_TRACE_SECTIONS = "idcshell,system";
/*      */   public static final String SVNKIT_MODULE_PATH = "$BranchDir/integrations/SVNKit/";
/*      */   public static final String USE_PARENT_CLASSES = "intradoc.common.NativeOsUtilsBase intradoc.common.PosixStructStat intradoc.common.StructStatFS";
/*      */   public static final String VERSIONINFO_JAVAPATH = "sources/java/intradoc/common/VersionInfo.java";
/*      */   public static final int INDEX_FETCHED_SOURCE = 0;
/*      */   public static final int INDEX_FETCHED_MD5 = 1;
/*      */   public static final int INDEX_FETCHED_SIZE = 2;
/*      */   public static final int INDEX_FETCHED_TIMESTAMP = 3;
/*      */   public static DateFormat s_localTimestamp;
/*      */   public boolean m_shouldReportFullURLs;
/*      */   public long m_startTime;
/*      */   public BuildEnvironment m_env;
/*      */   public int m_defaultUpdateModuleDependenciesFlags;
/*      */   public boolean m_hasConsole;
/*      */   public boolean m_hasComplianceSources;
/*      */   public Vector<String> m_traceSections;
/*      */   public Module m_svnkitModule;
/*      */   public Module m_coreModule;
/*      */   public DataResultSet m_fetchedResources;
/*      */   public int[] m_fetchedResourcesIndices;
/*      */   protected IdcClassLoaderWrapper m_serverLoader;
/*      */   protected Map<String, IdcClassLoaderWrapper> m_productLoaders;
/*      */   protected boolean m_isSystemInitialized;
/*      */   protected boolean m_isNativeOSUtilsInitialized;
/*      */   protected String m_lastURL;
/*      */   protected boolean m_needsNewline;
/*      */   protected int m_lastPercent;
/*      */   protected int m_lastChars;
/*      */   protected long m_lastUpdate;
/*      */   protected long m_lastStartTime;
/*      */ 
/*      */   public BuildManager(Map<String, String> environment)
/*      */   {
/*  116 */     this.m_startTime = System.currentTimeMillis();
/*  117 */     if (s_localTimestamp == null)
/*      */     {
/*  119 */       s_localTimestamp = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
/*      */     }
/*  121 */     BuildEnvironment env = this.m_env = new BuildEnvironment();
/*  122 */     String traceLevel = System.getProperty("idc.build.verbosity");
/*  123 */     env.m_traceLevel = NumberUtils.parseInteger(traceLevel, 6);
/*  124 */     env.m_trace = this;
/*  125 */     env.init(environment);
/*  126 */     if (env.m_loader == null)
/*      */     {
/*  128 */       throw new RuntimeException("must be run from IdcClassLoader");
/*      */     }
/*  130 */     env.m_loader.m_name = "build";
/*  131 */     env.m_downloadStateListener = this;
/*  132 */     this.m_traceSections = new Vector();
/*      */ 
/*  134 */     String buildDate = BuildEnvironment.s_iso8601.format(Long.valueOf(this.m_startTime));
/*      */ 
/*  137 */     env.m_productVersionName = new StringBuilder().append(VersionInfo.getProductVersion()).append("-").append(buildDate).toString();
/*  138 */     env.m_productBuild = new StringBuilder().append(VersionInfo.getProductBuildInfo()).append("-").append(buildDate).toString();
/*  139 */     env.m_productVersionNumber = VersionInfo.getProductVersionInfo();
/*      */ 
/*  141 */     this.m_hasConsole = (System.console() != null);
/*  142 */     this.m_productLoaders = new ConcurrentHashMap();
/*      */   }
/*      */ 
/*      */   public Vector<String> getTraceSections()
/*      */   {
/*  147 */     Vector sections = this.m_traceSections;
/*  148 */     if (sections.size() > 0)
/*      */     {
/*  150 */       return sections;
/*      */     }
/*  152 */     String[] defaultSections = "idcshell,system".split(",");
/*  153 */     sections = new Vector(defaultSections.length);
/*  154 */     for (String section : defaultSections)
/*      */     {
/*  156 */       sections.add(section);
/*      */     }
/*  158 */     return sections;
/*      */   }
/*      */ 
/*      */   public void setVerbose(boolean isVerbose)
/*      */   {
/*  163 */     BuildEnvironment env = this.m_env;
/*  164 */     if (isVerbose)
/*      */     {
/*  166 */       env.m_traceLevel += 1;
/*      */     }
/*      */     else
/*      */     {
/*  170 */       env.m_traceLevel = 5;
/*      */     }
/*  172 */     env.m_loader.m_zipenv.m_verbosity = (this.m_env.m_traceLevel - 1);
/*  173 */     env.m_isVerbose = isVerbose;
/*  174 */     SystemUtils.m_verbose = isVerbose;
/*      */   }
/*      */ 
/*      */   public void preinitSystem()
/*      */   {
/*  180 */     if (!SharedObjects.isInit())
/*      */     {
/*  182 */       SharedObjects.init();
/*      */     }
/*  184 */     if (!IdcMessageUtils.m_isInit)
/*      */     {
/*  186 */       IdcMessageUtils.init();
/*      */     }
/*  188 */     intradoc.common.EnvUtils.m_useNativeOSUtils = false;
/*  189 */     Vector traceSections = getTraceSections();
/*  190 */     SystemUtils.setActiveTraces(traceSections);
/*      */   }
/*      */ 
/*      */   public File locateBranchDir() throws IdcException
/*      */   {
/*  195 */     BuildEnvironment env = this.m_env;
/*  196 */     File branchDir = env.m_branchDir;
/*  197 */     if (branchDir == null)
/*      */     {
/*  199 */       preinitSystem();
/*  200 */       String workingDirpath = System.getProperty("user.dir");
/*  201 */       File workingDir = new File(workingDirpath);
/*  202 */       if (tryBranchDir(workingDir))
/*      */       {
/*  204 */         return workingDir;
/*      */       }
/*      */ 
/*  207 */       String intradocDirpath = LegacyDirectoryLocator.getIntradocDir();
/*  208 */       File intradocDir = new File(intradocDirpath);
/*  209 */       if (tryBranchDir(intradocDir))
/*      */       {
/*  211 */         return intradocDir;
/*      */       }
/*  213 */       String msg = "cannot determine IntradocDir";
/*  214 */       throw new ServiceException("cannot determine IntradocDir");
/*      */     }
/*  216 */     return branchDir;
/*      */   }
/*      */ 
/*      */   protected boolean tryBranchDir(File branchDir)
/*      */   {
/*  221 */     File versionInfoFile = new File(branchDir, "sources/java/intradoc/common/VersionInfo.java");
/*      */ 
/*  224 */     return versionInfoFile.exists();
/*      */   }
/*      */ 
/*      */   public void initBuildDirectories()
/*      */     throws IdcException
/*      */   {
/*  231 */     BuildEnvironment env = this.m_env;
/*  232 */     Properties props = env.m_properties;
/*  233 */     File branchDir = env.m_branchDir = locateBranchDir();
/*  234 */     props.put("BranchDir", branchDir.getPath());
/*  235 */     File resourcesDir = env.m_resourcesDir;
/*  236 */     if (resourcesDir == null)
/*      */     {
/*  238 */       resourcesDir = env.m_resourcesDir = new File(branchDir, "sources");
/*      */     }
/*  240 */     props.put("IdcResourcesDir", resourcesDir.getPath());
/*  241 */     File buildDir = env.m_buildDir;
/*  242 */     if (buildDir == null)
/*      */     {
/*  244 */       buildDir = env.m_buildDir = new File(branchDir, "build");
/*      */     }
/*  246 */     buildDir.mkdir();
/*  247 */     if (!buildDir.isDirectory())
/*      */     {
/*  249 */       String msg = new StringBuilder().append("cannot create directory ").append(buildDir.getPath()).toString();
/*  250 */       throw new ServiceException(msg);
/*      */     }
/*  252 */     props.put("BuildDir", buildDir.getPath());
/*  253 */     env.m_buildStateFile = new File(buildDir, "build-state.hda");
/*  254 */     File shiphomeDir = env.m_shiphomeDir;
/*  255 */     if (shiphomeDir == null)
/*      */     {
/*  257 */       shiphomeDir = env.m_shiphomeDir = new File(buildDir, "idc-shiphome-generic");
/*      */     }
/*  259 */     props.put("IdcShiphomeDir", shiphomeDir.getPath());
/*  260 */     File intradocDir = env.m_intradocDir;
/*  261 */     if (intradocDir == null)
/*      */     {
/*  263 */       intradocDir = env.m_intradocDir = new File(shiphomeDir, "ucm/idc");
/*      */     }
/*  265 */     intradocDir.mkdirs();
/*  266 */     if (!intradocDir.isDirectory())
/*      */     {
/*  268 */       String msg = new StringBuilder().append("cannot create directory ").append(intradocDir.getPath()).toString();
/*  269 */       throw new ServiceException(msg);
/*      */     }
/*  271 */     props.put("IntradocDir", intradocDir.getPath());
/*  272 */     if (env.m_configDir == null)
/*      */     {
/*  274 */       env.m_configDir = new File(intradocDir, "config");
/*      */     }
/*  276 */     props.put("ConfigDir", env.m_configDir);
/*  277 */     File componentsDir = env.m_componentsDir;
/*  278 */     if (componentsDir == null)
/*      */     {
/*  280 */       componentsDir = env.m_componentsDir = new File(intradocDir, "components");
/*      */     }
/*  282 */     props.put("ComponentDir", componentsDir.getPath());
/*  283 */     if (env.m_componentZipsDir != null)
/*      */       return;
/*  285 */     env.m_componentZipsDir = new File(buildDir, "components");
/*      */   }
/*      */ 
/*      */   public void initSystemMinimal()
/*      */     throws IdcException
/*      */   {
/*  291 */     if (this.m_isSystemInitialized)
/*      */       return;
/*  293 */     long timeStart = System.currentTimeMillis();
/*      */ 
/*  295 */     FileUtils.m_minLockTimeout = 0;
/*  296 */     DataSerializeUtils.m_useOverrideEncodingHeaderVersion = true;
/*  297 */     DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*  298 */     IdcZipFunctions.initZipEnvironment();
/*  299 */     IdcZipFunctions.m_defaultZipEnvironment.m_trace = this;
/*  300 */     DataLoader.cacheSystemProperties();
/*  301 */     LegacyDirectoryLocator.buildRootDirectories();
/*  302 */     DataSerializeUtils.setSystemEncoding("UTF-8");
/*      */ 
/*  319 */     String productName = SharedObjects.getEnvironmentValue("IdcProductName");
/*  320 */     String resourcesDirname = LegacyDirectoryLocator.getResourcesDirectory(productName);
/*  321 */     String tablesDirname = FileUtils.getAbsolutePath(resourcesDirname, "core/tables");
/*  322 */     IdcSystemConfig.loadEncodingMap(tablesDirname);
/*  323 */     IdcSystemConfig.loadSystemEncodingInfo();
/*      */ 
/*  325 */     this.m_isSystemInitialized = true;
/*  326 */     BuildEnvironment env = this.m_env;
/*  327 */     if (env.m_traceLevel < 6)
/*      */       return;
/*  329 */     long timeStop = System.currentTimeMillis();
/*  330 */     long ms = timeStop - timeStart;
/*  331 */     report(6, new Object[] { "system initialized (", Long.valueOf(ms), " ms)" });
/*      */   }
/*      */ 
/*      */   public void initSystemForComponentList()
/*      */     throws IdcException
/*      */   {
/*  338 */     long timeInitStart = System.currentTimeMillis();
/*      */ 
/*  340 */     BuildEnvironment env = this.m_env;
/*  341 */     String intradocDir = FileUtils.directorySlashes(env.m_intradocDir.getPath());
/*  342 */     SharedObjects.putEnvironmentValue("IdcHomeDir", intradocDir);
/*  343 */     SharedObjects.putEnvironmentValue("UseHomeDirComponents", "false");
/*  344 */     SharedObjects.putEnvironmentValue("LoadScriptExtensions", "false");
/*  345 */     SharedObjects.putEnvironmentValue("IsInstallerEnv", "true");
/*  346 */     ComponentListManager.reset();
/*  347 */     ComponentListManager.init();
/*      */ 
/*  352 */     ComponentLoader.reset();
/*      */ 
/*  354 */     ComponentLoader.initDefaults();
/*  355 */     IdcSystemLoader.loadResourcesEx(true);
/*      */ 
/*  367 */     long timeInitStop = System.currentTimeMillis();
/*  368 */     long timeInitMillis = timeInitStop - timeInitStart;
/*  369 */     report(6, new Object[] { "system initialization complete (", Long.valueOf(timeInitMillis), " ms)" });
/*      */   }
/*      */ 
/*      */   public void initSVNKit() throws IdcException
/*      */   {
/*  374 */     if (this.m_svnkitModule != null)
/*      */       return;
/*  376 */     BuildEnvironment env = this.m_env;
/*  377 */     ExecutionContext context = env.m_context;
/*  378 */     int pathFlags = PathUtils.F_VARS_MUST_EXIST;
/*  379 */     Properties props = env.m_properties;
/*  380 */     String svnkitModulePath = PathUtils.substitutePathVariables("$BranchDir/integrations/SVNKit/", props, null, pathFlags, context);
/*      */ 
/*  382 */     File parentDir = (FileUtils.isAbsolutePath(svnkitModulePath)) ? null : env.m_branchDir;
/*  383 */     File svnkitModuleDir = new File(parentDir, svnkitModulePath);
/*  384 */     Module svnkit = this.m_svnkitModule = Module.createAndLoadModule(this, svnkitModuleDir);
/*  385 */     checkModuleDepends(svnkit, 0);
/*  386 */     File svnkitJar = new File(svnkitModuleDir, "svnkit.jar");
/*  387 */     if (!svnkitJar.exists())
/*      */     {
/*  389 */       throw new ServiceException("svnkit.jar is missing, cannot continue!");
/*      */     }
/*      */     try
/*      */     {
/*  393 */       env.m_loader.addClassPathElement(svnkitJar.getPath(), 64);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  397 */       throw new ServiceException("unable to add svnkit.jar to the classpath", e);
/*      */     }
/*  399 */     SVNManager.initSVNKit();
/*      */   }
/*      */ 
/*      */   public void loadModules()
/*      */     throws IdcException
/*      */   {
/*  405 */     BuildEnvironment env = this.m_env;
/*  406 */     Map modules = env.m_modules;
/*  407 */     if (!modules.isEmpty())
/*      */       return;
/*  409 */     File branchDir = env.m_branchDir;
/*  410 */     File sourcesDir = new File(branchDir, "sources");
/*  411 */     Module coreModule = this.m_coreModule = Module.createAndLoadModule(this, sourcesDir);
/*  412 */     coreModule.m_moduleName = "core";
/*  413 */     modules.put("core", coreModule);
/*      */ 
/*  415 */     Map containers = env.m_modulesContainers;
/*  416 */     File sourceManifestFile = new File(branchDir, "source.manifest");
/*  417 */     TextFileReader reader = new TextFileReader(sourceManifestFile);
/*      */     try
/*      */     {
/*  420 */       reader.open();
/*      */ 
/*  422 */       while ((line = reader.readLine()) != null)
/*      */       {
/*  424 */         String line;
/*  424 */         if (line.length() == 0) continue; if (line.startsWith("#")) {
/*      */           continue;
/*      */         }
/*      */ 
/*  428 */         String[] words = line.split("\\s+");
/*  429 */         if (words.length == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*  433 */         String manifestType = words[0];
/*  434 */         if ((!manifestType.startsWith("moddir")) && (!manifestType.startsWith("svnco"))) {
/*      */           continue;
/*      */         }
/*      */ 
/*  438 */         boolean isOptional = manifestType.endsWith("?");
/*      */ 
/*  440 */         ModulesContainer container = null;
/*  441 */         File modulesDir = new File(branchDir, words[1]);
/*  442 */         DataBinder binder = loadBuildConfigBinder(modulesDir, false);
/*  443 */         if (binder != null)
/*      */         {
/*  445 */           String classname = binder.getLocal("ModulesContainerClassname");
/*  446 */           if (classname != null)
/*      */           {
/*      */             try
/*      */             {
/*  450 */               Class containerClass = Class.forName(classname);
/*  451 */               container = (ModulesContainer)containerClass.newInstance();
/*      */             }
/*      */             catch (Throwable t)
/*      */             {
/*  455 */               String msg = new StringBuilder().append("unable to instantiate class \"").append(classname).append('"').toString();
/*  456 */               throw new ServiceException(t, msg, new Object[0]);
/*      */             }
/*      */           }
/*      */         }
/*  460 */         if (container == null)
/*      */         {
/*  462 */           container = new ModulesContainer();
/*      */         }
/*  464 */         report(7, new Object[] { "inspecting ", modulesDir, " ..." });
/*  465 */         container.init(this, modulesDir, binder, !isOptional);
/*  466 */         containers.put(words[1], container);
/*  467 */         if (container.m_isIncluded)
/*      */         {
/*  469 */           report(6, new Object[] { "loading modules from ", modulesDir, " ..." });
/*  470 */           container.loadModules();
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (IOException ioe)
/*      */     {
/*      */       String msg;
/*  477 */       throw new ServiceException(msg, ioe);
/*      */     }
/*      */     finally
/*      */     {
/*  481 */       reader.close();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void applyModulesFilter(PatternFilter filter)
/*      */   {
/*  488 */     BuildEnvironment env = this.m_env;
/*  489 */     Map modules = env.m_modules;
/*  490 */     if (filter != null)
/*      */     {
/*  493 */       for (String moduleName : modules.keySet())
/*      */       {
/*  495 */         if (moduleName.equals("core")) {
/*      */           continue;
/*      */         }
/*      */ 
/*  499 */         if (!filter.isIncluded(moduleName))
/*      */         {
/*  501 */           Module module = (Module)modules.get(moduleName);
/*  502 */           module.m_isExcludedFromBuild = true;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  507 */     if (!this.m_hasComplianceSources)
/*      */     {
/*  509 */       modules.remove("UpgradeAssistant");
/*      */     }
/*      */ 
/*  512 */     for (String moduleName : modules.keySet())
/*      */     {
/*  514 */       Module module = (Module)modules.get(moduleName);
/*  515 */       if (!module.m_isExcludedFromBuild)
/*      */       {
/*  517 */         enableModuleAndDependencies(module);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void enableModuleAndDependencies(Module module)
/*      */   {
/*  524 */     module.m_isExcludedFromBuild = false;
/*  525 */     String[] dependStrings = module.m_requiredModules;
/*  526 */     if (dependStrings == null)
/*      */       return;
/*  528 */     Map modules = this.m_env.m_modules;
/*  529 */     for (String moduleName : dependStrings)
/*      */     {
/*  531 */       Module dependModule = (Module)modules.get(moduleName);
/*  532 */       if (dependModule == null)
/*      */         continue;
/*  534 */       enableModuleAndDependencies(dependModule);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void sortModuleDepends()
/*      */   {
/*  542 */     BuildEnvironment env = this.m_env;
/*  543 */     Map modules = env.m_modules;
/*  544 */     int numModules = modules.size();
/*  545 */     String[] sortedModuleNames = new String[numModules];
/*  546 */     Set unsortedNamesSet = new HashSet(modules.keySet());
/*  547 */     Set sortedNamesSet = new HashSet(numModules);
/*      */ 
/*  549 */     unsortedNamesSet.remove("core");
/*  550 */     sortedModuleNames[0] = "core";
/*      */ 
/*  552 */     int sortFirst = 1; int sortNext = 1;
/*  553 */     for (String moduleName : unsortedNamesSet)
/*      */     {
/*  555 */       Module module = (Module)modules.get(moduleName);
/*  556 */       String[] required = module.m_requiredModules;
/*  557 */       if ((required == null) || (required.length == 0))
/*      */       {
/*  559 */         sortedModuleNames[(sortNext++)] = moduleName;
/*      */       }
/*      */       else
/*      */       {
/*  564 */         for (int r = required.length - 1; r >= 0; --r)
/*      */         {
/*  566 */           String requiredName = required[r];
/*  567 */           if (unsortedNamesSet.contains(requiredName))
/*      */             continue;
/*  569 */           String msg = new StringBuilder().append("Unknown module \"").append(requiredName).append("\" required by ").append(moduleName).toString();
/*  570 */           throw new RuntimeException(msg);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  575 */     int firstDependentModule = sortNext;
/*      */     do
/*      */     {
/*  579 */       if (sortFirst == sortNext)
/*      */       {
/*  581 */         StringBuilder msg = new StringBuilder("Found a dependency cycle in the following modules:");
/*  582 */         for (String moduleName : unsortedNamesSet)
/*      */         {
/*  584 */           Module module = (Module)modules.get(moduleName);
/*  585 */           msg.append("\n\t");
/*  586 */           appendModuleWithModuleDependencies(module, msg);
/*      */         }
/*  588 */         throw new RuntimeException(msg.toString());
/*      */       }
/*  590 */       Arrays.sort(sortedModuleNames, sortFirst, sortNext);
/*  591 */       while (sortFirst < sortNext)
/*      */       {
/*  593 */         String moduleName = sortedModuleNames[(sortFirst++)];
/*  594 */         sortedNamesSet.add(moduleName);
/*  595 */         unsortedNamesSet.remove(moduleName);
/*      */       }
/*      */ 
/*  598 */       for (String moduleName : unsortedNamesSet)
/*      */       {
/*  600 */         Module module = (Module)modules.get(moduleName);
/*  601 */         String[] required = module.m_requiredModules;
/*  602 */         for (int r = required.length - 1; r >= 0; --r)
/*      */         {
/*  604 */           String requiredName = required[r];
/*  605 */           if (!sortedNamesSet.contains(requiredName));
/*      */         }
/*      */ 
/*  610 */         sortedModuleNames[(sortNext++)] = moduleName;
/*      */       }
/*      */     }
/*  612 */     while (sortFirst < numModules);
/*  613 */     env.m_sortedModuleNames = CollectionUtils.appendToList(null, (Object[])sortedModuleNames);
/*  614 */     if (!env.m_isVerbose)
/*      */       return;
/*  616 */     StringBuilder sb = new StringBuilder("located the following Modules, sorted in dependency order:\n");
/*  617 */     for (int m = 0; m < numModules; ++m)
/*      */     {
/*  619 */       if ((m > 0) && (m == firstDependentModule))
/*      */       {
/*  621 */         sb.append("\t----(dependent)----\n");
/*      */       }
/*  623 */       sb.append('\t');
/*  624 */       String moduleName = sortedModuleNames[m];
/*  625 */       Module module = (Module)modules.get(moduleName);
/*  626 */       appendModuleWithModuleDependencies(module, sb);
/*  627 */       sb.append('\n');
/*      */     }
/*  629 */     report(6, new Object[] { sb });
/*      */   }
/*      */ 
/*      */   protected void appendModuleWithModuleDependencies(Module module, StringBuilder sb)
/*      */   {
/*  635 */     if (module.m_isExcludedFromBuild)
/*      */     {
/*  637 */       sb.append("[skip] ");
/*      */     }
/*  639 */     sb.append(module.m_moduleName);
/*  640 */     String[] requiredModules = module.m_requiredModules;
/*  641 */     if (requiredModules == null)
/*      */       return;
/*  643 */     sb.append(" --> ");
/*  644 */     for (int r = 0; r < requiredModules.length; ++r)
/*      */     {
/*  646 */       if (r > 0)
/*      */       {
/*  648 */         sb.append(',');
/*      */       }
/*  650 */       sb.append(requiredModules[r]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkModuleDepends(Module module, int flags)
/*      */     throws IdcException
/*      */   {
/*  657 */     flags ^= this.m_defaultUpdateModuleDependenciesFlags;
/*  658 */     BuildEnvironment env = this.m_env;
/*  659 */     if (env.m_isVerbose)
/*      */     {
/*  661 */       report(7, new Object[] { "checking dependencies for ", module.m_moduleName, " ..." });
/*      */     }
/*  663 */     module.updateDependencies(flags, this);
/*      */   }
/*      */ 
/*      */   public void initCompiler()
/*      */   {
/*  668 */     BuildEnvironment env = this.m_env;
/*  669 */     JavaCompileManager compiler = env.m_javaCompiler;
/*  670 */     if (compiler == null)
/*      */     {
/*  672 */       compiler = env.m_javaCompiler = new JavaCompileManager();
/*      */     }
/*  674 */     compiler.m_trace = this;
/*  675 */     if (env.m_isReleng)
/*      */     {
/*  677 */       VersionInfoClassFileEditor editor = new VersionInfoClassFileEditor();
/*  678 */       editor.init(env, Long.valueOf(this.m_startTime));
/*  679 */       compiler.m_editor = editor;
/*      */ 
/*  681 */       report(6, new Object[] { "Loaded Version Info Editor." });
/*      */     }
/*      */     else
/*      */     {
/*  686 */       compiler.m_editor = null;
/*      */     }
/*  688 */     compiler.m_isShowCommand = env.m_isVerbose;
/*  689 */     env.m_defaultClasspath = new ArrayList();
/*      */   }
/*      */ 
/*      */   public String computeUCMBranchFromSVNURL(String repoURL, String productToMatch)
/*      */   {
/*  704 */     int prefixLength = "svn/idc/products/".length();
/*  705 */     int indexFirst = (repoURL == null) ? -1 : repoURL.indexOf("svn/idc/products/");
/*  706 */     if (indexFirst < 0)
/*      */     {
/*  708 */       return null;
/*      */     }
/*  710 */     int indexProductSlash = repoURL.indexOf(47, indexFirst + prefixLength);
/*  711 */     if (indexProductSlash < 0)
/*      */     {
/*  713 */       return null;
/*      */     }
/*  715 */     if (productToMatch != null)
/*      */     {
/*  717 */       String productName = repoURL.substring(indexFirst + prefixLength, indexProductSlash);
/*  718 */       if (!productName.equals(productToMatch))
/*      */       {
/*  720 */         return null;
/*      */       }
/*      */     }
/*  723 */     if (!repoURL.substring(indexProductSlash).startsWith("/branches/"))
/*      */     {
/*  725 */       return null;
/*      */     }
/*  727 */     int indexBranch = indexProductSlash + 10;
/*  728 */     int indexLast = repoURL.indexOf(47, indexBranch);
/*  729 */     String branch = (indexLast < 0) ? repoURL.substring(indexBranch) : repoURL.substring(indexBranch, indexLast);
/*      */ 
/*  731 */     return branch;
/*      */   }
/*      */ 
/*      */   public String computeUCMBranchFromWorkingCopyDir(File wcDir)
/*      */     throws IdcException
/*      */   {
/*  741 */     initSVNKit();
/*  742 */     BuildEnvironment env = this.m_env;
/*      */     Map props;
/*      */     try
/*      */     {
/*  746 */       SVNManager svnManager = SVNManager.getOrCreateSharedSVNManager();
/*  747 */       props = this.m_env.m_wcProps = svnManager.getWCInfo(wcDir);
/*      */     }
/*      */     catch (IOException t)
/*      */     {
/*  751 */       throw new ServiceException(t);
/*      */     }
/*  753 */     String repoURL = (String)props.get("svn:entry:url");
/*  754 */     String branch = env.m_branch = computeUCMBranchFromSVNURL(repoURL, "cs");
/*  755 */     if (branch == null)
/*      */     {
/*  757 */       throw new ServiceException(new StringBuilder().append("unknown repository URL ").append(repoURL).toString());
/*      */     }
/*  759 */     return branch;
/*      */   }
/*      */ 
/*      */   public File getBuildConfigFile(File dir)
/*      */   {
/*  770 */     return new File(dir, "build-config.hda");
/*      */   }
/*      */ 
/*      */   public DataBinder loadBuildConfigBinder(File dir, boolean mustExist)
/*      */     throws IdcException
/*      */   {
/*  783 */     File file = getBuildConfigFile(dir);
/*  784 */     String pathname = file.getPath();
/*  785 */     if (!file.exists())
/*      */     {
/*  787 */       if (mustExist)
/*      */       {
/*  789 */         throw new ServiceException(null, "syFileDoesNotExist", new Object[] { pathname });
/*      */       }
/*  791 */       return null;
/*      */     }
/*  793 */     String dirPath = dir.getPath();
/*  794 */     String filename = file.getName();
/*      */     try
/*      */     {
/*  797 */       return ResourceUtils.readDataBinder(dirPath, filename);
/*      */     }
/*      */     catch (ServiceException se)
/*      */     {
/*  801 */       String msg = new StringBuilder().append("unable to load ").append(pathname).toString();
/*  802 */       throw new ServiceException(msg, se);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataBinder loadOrCreateBinder(File file)
/*      */     throws IdcException
/*      */   {
/*  816 */     DataBinder binder = new DataBinder();
/*  817 */     String dirname = file.getParent();
/*  818 */     String filename = file.getName();
/*  819 */     ResourceUtils.serializeDataBinderWithEncoding(dirname, filename, binder, 0, null);
/*  820 */     return binder;
/*      */   }
/*      */ 
/*      */   public void loadBuildState() throws IdcException
/*      */   {
/*  825 */     BuildEnvironment env = this.m_env;
/*  826 */     DataBinder state = loadOrCreateBinder(env.m_buildStateFile);
/*  827 */     env.m_isServerGenerationNeeded = StringUtils.convertToBool(state.getLocal("isServerGenerationNeeded"), true);
/*  828 */     Map componentsLastInstalled = this.m_env.m_componentsLastInstalled = new HashMap();
/*  829 */     Map componentsLastProductTags = this.m_env.m_componentsLastProductTags = new HashMap();
/*      */ 
/*  831 */     DataResultSet componentsRSet = (DataResultSet)state.getResultSet("Components");
/*  832 */     if (componentsRSet != null)
/*      */     {
/*  834 */       int componentNameIndex = componentsRSet.getFieldInfoIndex("componentName");
/*  835 */       int lastInstalledIndex = componentsRSet.getFieldInfoIndex("lastInstalled");
/*  836 */       int lastProductTagsIndex = componentsRSet.getFieldInfoIndex("lastProductTags");
/*  837 */       if ((componentNameIndex >= 0) && (lastInstalledIndex >= 0) && (lastProductTagsIndex >= 0))
/*      */       {
/*  839 */         DateFormat iso8601 = BuildEnvironment.s_iso8601;
/*  840 */         for (componentsRSet.first(); componentsRSet.isRowPresent(); componentsRSet.next())
/*      */         {
/*  842 */           String componentName = componentsRSet.getStringValue(componentNameIndex);
/*      */ 
/*  844 */           String lastInstalledString = componentsRSet.getStringValue(lastInstalledIndex);
/*      */           Long lastInstalled;
/*      */           try
/*      */           {
/*  848 */             Date lastInstalledDate = iso8601.parse(lastInstalledString);
/*  849 */             lastInstalled = Long.valueOf(lastInstalledDate.getTime());
/*      */           }
/*      */           catch (ParseException pe)
/*      */           {
/*  853 */             lastInstalled = Long.valueOf(0L);
/*      */           }
/*  855 */           componentsLastInstalled.put(componentName, lastInstalled);
/*      */ 
/*  857 */           String lastProductTagsString = componentsRSet.getStringValue(lastProductTagsIndex);
/*  858 */           Component.ProductTags productTags = new Component.ProductTags(lastProductTagsString);
/*  859 */           componentsLastProductTags.put(componentName, productTags);
/*      */         }
/*      */       }
/*      */     }
/*  863 */     loadFetchedResources(state);
/*      */   }
/*      */ 
/*      */   protected void loadFetchedResources(DataBinder state) throws IdcException
/*      */   {
/*  868 */     String[] fieldNames = { "source", "md5", "size", "timestamp" };
/*  869 */     List fields = new ArrayList();
/*  870 */     for (String fieldName : fieldNames)
/*      */     {
/*  872 */       fields.add(fieldName);
/*      */     }
/*  874 */     DataResultSet fetchedResources = this.m_fetchedResources = (DataResultSet)state.getResultSet("FetchedResources");
/*  875 */     if (fetchedResources == null)
/*      */     {
/*  877 */       fetchedResources = this.m_fetchedResources = new DataResultSet(fieldNames);
/*      */     }
/*      */     else
/*      */     {
/*  881 */       fetchedResources.mergeFieldsWithFlags(fields, 0);
/*      */     }
/*  883 */     int[] fieldIndices = new int[4];
/*  884 */     fieldIndices[0] = ResultSetUtils.getIndexMustExist(fetchedResources, "source");
/*  885 */     fieldIndices[1] = ResultSetUtils.getIndexMustExist(fetchedResources, "md5");
/*  886 */     fieldIndices[2] = ResultSetUtils.getIndexMustExist(fetchedResources, "size");
/*  887 */     fieldIndices[3] = ResultSetUtils.getIndexMustExist(fetchedResources, "timestamp");
/*  888 */     this.m_fetchedResourcesIndices = fieldIndices;
/*      */   }
/*      */ 
/*      */   public void saveBuildState() throws IdcException
/*      */   {
/*  893 */     BuildEnvironment env = this.m_env;
/*  894 */     DataBinder state = new DataBinder();
/*  895 */     state.putLocal("isServerGenerationNeeded", (env.m_isServerGenerationNeeded) ? "1" : "0");
/*      */ 
/*  897 */     Map componentsLastInstalled = this.m_env.m_componentsLastInstalled;
/*  898 */     Set componentNamesSet = componentsLastInstalled.keySet();
/*  899 */     int numComponentNames = componentNamesSet.size();
/*  900 */     if (numComponentNames > 0)
/*      */     {
/*  902 */       Map componentsLastProductTags = this.m_env.m_componentsLastProductTags;
/*  903 */       String[] componentsRSetFieldNames = { "componentName", "lastInstalled", "lastProductTags" };
/*  904 */       DataResultSet componentsRSet = new DataResultSet(componentsRSetFieldNames);
/*  905 */       state.addResultSet("Components", componentsRSet);
/*  906 */       String[] componentNamesSorted = new String[numComponentNames];
/*  907 */       componentNamesSet.toArray(componentNamesSorted);
/*  908 */       Arrays.sort(componentNamesSorted);
/*  909 */       DateFormat iso8601 = BuildEnvironment.s_iso8601;
/*  910 */       for (int c = 0; c < numComponentNames; ++c)
/*      */       {
/*  912 */         String componentName = componentNamesSorted[c];
/*  913 */         Long lastUpdated = (Long)componentsLastInstalled.get(componentName);
/*  914 */         Date lastUpdatedDate = new Date(lastUpdated.longValue());
/*  915 */         String lastUpdatedString = iso8601.format(lastUpdatedDate);
/*  916 */         Component.ProductTags productTags = (Component.ProductTags)componentsLastProductTags.get(componentName);
/*  917 */         String lastProductTagsString = productTags.toString();
/*  918 */         Vector row = componentsRSet.createEmptyRow();
/*  919 */         row.set(0, componentName);
/*  920 */         row.set(1, lastUpdatedString);
/*  921 */         row.set(2, lastProductTagsString);
/*  922 */         componentsRSet.addRow(row);
/*      */       }
/*      */     }
/*      */ 
/*  926 */     List moduleNames = env.m_sortedModuleNames;
/*  927 */     Set modulesWithJava = env.m_modulesWithJava;
/*      */     DataResultSet modulesRSet;
/*  928 */     if ((moduleNames != null) && (modulesWithJava != null))
/*      */     {
/*  930 */       String[] modulesRSetFieldNames = { "moduleName", "hasJava" };
/*  931 */       modulesRSet = new DataResultSet(modulesRSetFieldNames);
/*  932 */       state.addResultSet("Modules", modulesRSet);
/*  933 */       for (String moduleName : moduleNames)
/*      */       {
/*  935 */         Vector row = modulesRSet.createEmptyRow();
/*  936 */         row.set(0, moduleName);
/*  937 */         if ((modulesWithJava != null) && (modulesWithJava.contains(moduleName)))
/*      */         {
/*  939 */           row.set(1, "1");
/*      */         }
/*  941 */         modulesRSet.addRow(row);
/*      */       }
/*      */     }
/*      */ 
/*  945 */     state.addResultSet("FetchedResources", this.m_fetchedResources);
/*  946 */     BuildUtils.writeSortedDataBinder(state, env.m_buildStateFile);
/*      */   }
/*      */ 
/*      */   protected void setupServerWrapper(IdcClassLoaderWrapper wrapper)
/*      */   {
/*  953 */     BuildEnvironment env = this.m_env;
/*  954 */     wrapper.setVerbosity(env.m_traceLevel - 1);
/*      */ 
/*  956 */     wrapper.setUseStaticMapForZipfiles(true);
/*      */   }
/*      */ 
/*      */   public IdcClassLoaderWrapper getOrInitServerLoader() throws IdcException
/*      */   {
/*  961 */     BuildEnvironment env = this.m_env;
/*  962 */     IdcClassLoaderWrapper serverLoader = this.m_serverLoader;
/*  963 */     if (serverLoader == null)
/*      */     {
/*  965 */       serverLoader = this.m_serverLoader = new IdcClassLoaderWrapper(env.m_loader, "server");
/*  966 */       setupServerWrapper(serverLoader);
/*  967 */       serverLoader.clearUseParentForClasses();
/*  968 */       String loaderClasspath = new StringBuilder().append(env.m_intradocDir.getPath()).append("/jlib/idcloader.jar").toString();
/*      */       try
/*      */       {
/*  971 */         serverLoader.init(loaderClasspath);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/*  975 */         throw new ServiceException(t);
/*      */       }
/*  977 */       if (env.m_isVerbose)
/*      */       {
/*  979 */         env.m_trace.report(6, new Object[] { "classpath for server loader: ", loaderClasspath });
/*      */       }
/*      */     }
/*  982 */     return serverLoader;
/*      */   }
/*      */ 
/*      */   public void initNativeOSUtils() throws IdcException
/*      */   {
/*  987 */     if (this.m_isNativeOSUtilsInitialized) {
/*      */       return;
/*      */     }
/*  990 */     BuildEnvironment env = this.m_env;
/*  991 */     Module module = (Module)env.m_modules.get("NativeOsUtils");
/*  992 */     List classpathList = new ArrayList();
/*  993 */     module.appendClasspathTo(classpathList, 0);
/*  994 */     IdcClassLoaderWrapper wrapper = getOrInitServerLoader();
/*  995 */     for (String classpath : classpathList)
/*      */     {
/*      */       try
/*      */       {
/*  999 */         wrapper.addClassPathElement(classpath, 32);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1003 */         throw new ServiceException(t);
/*      */       }
/*      */     }
/* 1006 */     ClassLoader loader = wrapper.m_loader;
/*      */ 
/* 1009 */     String libDir = new StringBuilder().append(module.m_moduleDirname).append("/lib/").toString();
/* 1010 */     Map argsMap = new HashMap();
/* 1011 */     argsMap.put("NativeOSUtilsLibDir", libDir);
/*      */     try
/*      */     {
/* 1017 */       Class clNativeOsUtils = Class.forName("intradoc.common.NativeOsUtils", true, loader);
/* 1018 */       Constructor constructor = clNativeOsUtils.getConstructor(new Class[] { Map.class });
/* 1019 */       constructor.newInstance(new Object[] { argsMap });
/*      */     }
/*      */     catch (InvocationTargetException ite)
/*      */     {
/* 1023 */       Throwable t = ite.getCause();
/* 1024 */       if (t instanceof IdcException)
/*      */       {
/* 1026 */         throw ((IdcException)t);
/*      */       }
/* 1028 */       throw new ServiceException(t);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1032 */       throw new ServiceException(t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public IdcClassLoaderWrapper initNewClassLoaderForProduct(String productName)
/*      */     throws IdcException
/*      */   {
/* 1039 */     IdcClassLoaderWrapper serverLoader = getOrInitServerLoader();
/* 1040 */     IdcClassLoaderWrapper loader = new IdcClassLoaderWrapper(serverLoader.m_loader, productName);
/* 1041 */     setupServerWrapper(loader);
/*      */ 
/* 1044 */     String[] parentClasses = "intradoc.common.NativeOsUtilsBase intradoc.common.PosixStructStat intradoc.common.StructStatFS".split("\\s");
/* 1045 */     for (String parentClass : parentClasses)
/*      */     {
/* 1047 */       loader.setUseParentForClass(parentClass, true);
/*      */     }
/* 1049 */     return loader;
/*      */   }
/*      */ 
/*      */   public IdcClassLoaderWrapper checkInitServerForProduct(String productName)
/*      */     throws IdcException
/*      */   {
/* 1061 */     initNativeOSUtils();
/* 1062 */     if (productName == null)
/*      */     {
/* 1064 */       productName = "all";
/*      */     }
/* 1066 */     boolean isAllProduct = productName.equals("all");
/* 1067 */     Map serverLoaders = this.m_productLoaders;
/* 1068 */     IdcClassLoaderWrapper wrapper = (IdcClassLoaderWrapper)serverLoaders.get(productName);
/* 1069 */     if (wrapper == null)
/*      */     {
/* 1072 */       report(6, new Object[] { "initializing server for product ", productName, " ..." });
/* 1073 */       long timeStart = System.currentTimeMillis();
/* 1074 */       BuildEnvironment env = this.m_env;
/* 1075 */       wrapper = initNewClassLoaderForProduct(productName);
/* 1076 */       ClassLoader loader = wrapper.m_loader;
/* 1077 */       System.setProperty("idc.bin.dir", env.m_buildDir.getPath());
/*      */ 
/* 1080 */       Map modules = env.m_modules;
/* 1081 */       List moduleNames = env.m_sortedModuleNames;
/* 1082 */       int numModules = moduleNames.size();
/* 1083 */       String idcserverPathname = new StringBuilder().append(env.m_intradocDir.getPath()).append("/jlib/idcserver.jar").toString();
/* 1084 */       List classpathList = CollectionUtils.appendToList(null, new Object[] { idcserverPathname });
/* 1085 */       for (int m = 0; m < numModules; ++m)
/*      */       {
/* 1087 */         String moduleName = (String)moduleNames.get(m);
/* 1088 */         Module module = (Module)modules.get(moduleName);
/* 1089 */         if (module.m_isExcludedFromBuild) continue; if (moduleName.equals("NativeOsUtils")) {
/*      */           continue;
/*      */         }
/*      */ 
/* 1093 */         if ((!isAllProduct) && (module instanceof Component))
/*      */         {
/* 1095 */           Component component = (Component)module;
/* 1096 */           Component.ProductTag productTag = (Component.ProductTag)component.m_productTags.get(productName);
/* 1097 */           if (productTag == null) continue; if (!productTag.m_isEnabled) {
/*      */             continue;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/* 1103 */         module.appendClasspathTo(classpathList, 1);
/*      */       }
/* 1105 */       if (env.m_isVerbose)
/*      */       {
/* 1107 */         env.m_trace.report(6, new Object[] { "classpath for product ", productName, ": ", classpathList });
/*      */       }
/*      */       try
/*      */       {
/* 1111 */         wrapper.initWithList(classpathList);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1115 */         throw new ServiceException(t, "Unable to initialize IdcClassLoader for server", new Object[0]);
/*      */       }
/*      */ 
/* 1118 */       writeSystemConfigForProduct(productName);
/*      */       try
/*      */       {
/* 1124 */         Class clIdcSystemConfig = Class.forName("intradoc.server.IdcSystemConfig", true, loader);
/* 1125 */         Method loadInitialConfig = clIdcSystemConfig.getMethod("loadInitialConfig", new Class[0]);
/* 1126 */         loadInitialConfig.invoke(null, new Object[0]);
/*      */       }
/*      */       catch (InvocationTargetException ite)
/*      */       {
/* 1130 */         Throwable t = ite.getCause();
/* 1131 */         if (t instanceof IdcException)
/*      */         {
/* 1133 */           throw ((IdcException)t);
/*      */         }
/* 1135 */         throw new ServiceException(t);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1139 */         throw new ServiceException(t);
/*      */       }
/*      */ 
/*      */       try
/*      */       {
/* 1144 */         Class clSystemUtils = Class.forName("intradoc.common.SystemUtils", true, loader);
/* 1145 */         Vector traceSections = getTraceSections();
/* 1146 */         if (traceSections.size() > 0)
/*      */         {
/* 1151 */           Method setActiveTraces = clSystemUtils.getMethod("setActiveTraces", new Class[] { Vector.class });
/* 1152 */           setActiveTraces.invoke(null, new Object[] { traceSections });
/*      */         }
/*      */       }
/*      */       catch (InvocationTargetException ite)
/*      */       {
/* 1157 */         Throwable t = ite.getCause();
/* 1158 */         throw new RuntimeException(t);
/*      */       }
/*      */       catch (Throwable t)
/*      */       {
/* 1162 */         throw new RuntimeException(t);
/*      */       }
/*      */ 
/* 1165 */       long timeStop = System.currentTimeMillis();
/* 1166 */       long ms = timeStop - timeStart;
/* 1167 */       report(6, new Object[] { "finished loading initial config, took ", Long.valueOf(ms), " ms" });
/* 1168 */       serverLoaders.put(productName, wrapper);
/*      */     }
/* 1170 */     return wrapper;
/*      */   }
/*      */ 
/*      */   public void writeSystemConfigForProduct(String productName)
/*      */     throws IdcException
/*      */   {
/* 1182 */     BuildEnvironment env = this.m_env;
/* 1183 */     File binDir = env.m_buildDir;
/*      */ 
/* 1185 */     File intradocCfgFile = new File(binDir, "intradoc.cfg");
/* 1186 */     String resourcesDirname = FileUtils.directorySlashes(env.m_resourcesDir.getPath());
/* 1187 */     String intradocDir = FileUtils.directorySlashes(env.m_intradocDir.getPath());
/* 1188 */     String modulesDir = FileUtils.directorySlashes(env.m_properties.getProperty("ORACLE_COMMON_MODULES_DIR"));
/* 1189 */     StringBuilder sb = new StringBuilder("<?cfg jcharset=\"UTF-8\"?>\n");
/* 1190 */     if (productName == null)
/*      */     {
/* 1193 */       sb.append("LanguageSource_oracle=");
/* 1194 */       sb.append(resourcesDirname);
/* 1195 */       sb.append("/core/lang");
/*      */     }
/*      */     else
/*      */     {
/* 1199 */       sb.append("IdcProductName=");
/* 1200 */       sb.append(productName);
/*      */     }
/* 1202 */     sb.append("\nIdcResourcesDir=");
/* 1203 */     sb.append(resourcesDirname);
/* 1204 */     sb.append("\nIntradocDir=");
/* 1205 */     sb.append(intradocDir);
/* 1206 */     if (modulesDir != null)
/*      */     {
/* 1208 */       sb.append("\nORACLE_COMMON_MODULES_DIR=");
/* 1209 */       sb.append(modulesDir);
/*      */     }
/* 1211 */     sb.append('\n');
/* 1212 */     String config = sb.toString();
/* 1213 */     BuildUtils.writeUTF8FileSafely(intradocCfgFile, new String[] { config });
/* 1214 */     File configDir = env.m_configDir;
/* 1215 */     configDir.mkdir();
/* 1216 */     File configCfgFile = new File(configDir, "config.cfg");
/* 1217 */     BuildUtils.writeUTF8FileSafely(configCfgFile, new String[] { "<?cfg jcharset=\"UTF-8\"?>\n", "IgnoreComponentLoadError=1\nTolerateLocalizationFailure=1\n" });
/*      */   }
/*      */ 
/*      */   protected void outputNewline()
/*      */   {
/* 1226 */     StringBuilder sb = new StringBuilder();
/* 1227 */     if (this.m_lastURL != null)
/*      */     {
/* 1229 */       long now = System.currentTimeMillis();
/* 1230 */       long ms = now - this.m_lastStartTime;
/* 1231 */       sb.append(" (");
/* 1232 */       sb.append(ms);
/* 1233 */       sb.append(" ms)");
/* 1234 */       this.m_lastURL = null;
/*      */     }
/* 1236 */     sb.append('\n');
/* 1237 */     System.out.print(sb);
/*      */   }
/*      */ 
/*      */   public void report(int level, Object[] args)
/*      */   {
/* 1243 */     if (this.m_needsNewline)
/*      */     {
/* 1245 */       outputNewline();
/* 1246 */       this.m_needsNewline = false;
/*      */     }
/* 1248 */     if (level > this.m_env.m_traceLevel)
/*      */       return;
/* 1250 */     Date now = new Date();
/* 1251 */     String dateString = s_localTimestamp.format(now);
/* 1252 */     StringBuilder sb = new StringBuilder(dateString);
/* 1253 */     sb.append(' ');
/* 1254 */     sb.append(LEVEL_NAMES[level]);
/* 1255 */     sb.append(": ");
/* 1256 */     for (Object arg : args)
/*      */     {
/* 1258 */       if (arg instanceof Throwable)
/*      */       {
/* 1260 */         Throwable t = (Throwable)arg;
/* 1261 */         sb.append(t.toString());
/* 1262 */         Throwable cause = t.getCause();
/* 1263 */         while (cause != null)
/*      */         {
/* 1265 */           sb.append("\nCaused by: ");
/* 1266 */           sb.append(cause.toString());
/* 1267 */           cause = cause.getCause();
/*      */         }
/*      */       }
/* 1270 */       else if (arg instanceof String)
/*      */       {
/* 1272 */         sb.append((String)arg);
/*      */       }
/*      */       else
/*      */       {
/* 1276 */         sb.append(arg.toString());
/*      */       }
/*      */     }
/* 1279 */     if (sb.charAt(sb.length() - 1) != '\n')
/*      */     {
/* 1281 */       sb.append('\n');
/*      */     }
/* 1283 */     PrintStream stream = (level <= 4) ? System.err : System.out;
/* 1284 */     stream.print(sb.toString());
/*      */   }
/*      */ 
/*      */   public void updateState(HTTPDownloader.State state)
/*      */   {
/* 1292 */     BuildEnvironment env = this.m_env;
/* 1293 */     String URL = state.m_URL.toString();
/* 1294 */     StringBuilder sb = new StringBuilder();
/* 1295 */     long contentLength = (state.m_isFinished) ? state.m_position : state.m_contentLength;
/* 1296 */     float fraction = (contentLength <= 0L) ? 0.0F : (float)state.m_position / (float)contentLength;
/* 1297 */     int percent = (int)(fraction * 100.0F);
/* 1298 */     long now = System.currentTimeMillis();
/* 1299 */     if (!URL.equals(this.m_lastURL))
/*      */     {
/* 1301 */       if (this.m_needsNewline)
/*      */       {
/* 1303 */         outputNewline();
/* 1304 */         this.m_needsNewline = false;
/*      */       }
/* 1306 */       this.m_lastStartTime = now;
/* 1307 */       this.m_lastURL = URL;
/* 1308 */       sb.append("fetching ");
/* 1309 */       if (env.m_isVerbose)
/*      */       {
/* 1311 */         int length = URL.length();
/* 1312 */         if ((this.m_shouldReportFullURLs) || (length < 64))
/*      */         {
/* 1314 */           sb.append(URL);
/*      */         }
/*      */         else
/*      */         {
/* 1318 */           int dotIndex = URL.indexOf(46);
/* 1319 */           sb.append(URL.substring(0, dotIndex));
/* 1320 */           sb.append("...");
/* 1321 */           int slashIndex = URL.indexOf(47, length - 64 + dotIndex);
/* 1322 */           if (slashIndex < 0)
/*      */           {
/* 1324 */             slashIndex = length - 64 + dotIndex;
/*      */           }
/* 1326 */           sb.append(URL.substring(slashIndex));
/*      */         }
/* 1328 */         sb.append(" [");
/* 1329 */         int flags = 258;
/* 1330 */         if (contentLength < 0L)
/*      */         {
/* 1332 */           sb.append('?');
/*      */         }
/*      */         else
/*      */         {
/* 1336 */           String bytes = NumberUtils.formatHumanizedBytes(state.m_contentLength, flags, env.m_context);
/* 1337 */           sb.append(bytes);
/*      */         }
/* 1339 */         sb.append(']');
/*      */       }
/*      */       else
/*      */       {
/* 1343 */         String filename = state.m_URL.getPath();
/* 1344 */         int length = filename.length();
/* 1345 */         int lastSlash = filename.lastIndexOf(47, length - 2);
/* 1346 */         if (lastSlash >= 0)
/*      */         {
/* 1348 */           filename = filename.substring(lastSlash + 1);
/*      */         }
/* 1350 */         sb.append(filename);
/*      */       }
/* 1352 */       sb.append(" ... ");
/* 1353 */       System.out.print(sb.toString());
/* 1354 */       System.out.flush();
/* 1355 */       sb.setLength(0);
/* 1356 */       this.m_needsNewline = true;
/* 1357 */       this.m_lastPercent = 0;
/* 1358 */       this.m_lastChars = 0;
/* 1359 */       this.m_lastUpdate = 0L;
/* 1360 */       if (!this.m_hasConsole)
/*      */       {
/* 1362 */         return;
/*      */       }
/*      */     }
/* 1365 */     else if (percent == this.m_lastPercent)
/*      */     {
/* 1368 */       return;
/*      */     }
/*      */ 
/* 1371 */     if ((percent < 100) && (((!this.m_hasConsole) || (now - this.m_lastUpdate < 500L))))
/*      */     {
/* 1373 */       return;
/*      */     }
/* 1375 */     this.m_lastUpdate = now;
/* 1376 */     this.m_lastPercent = percent;
/* 1377 */     while (this.m_lastChars-- > 0)
/*      */     {
/* 1379 */       sb.append('\b');
/*      */     }
/* 1381 */     int fetchLength = sb.length();
/* 1382 */     sb.append(percent);
/* 1383 */     sb.append('%');
/* 1384 */     this.m_lastChars = (sb.length() - fetchLength);
/* 1385 */     System.out.print(sb.toString());
/* 1386 */     System.out.flush();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1393 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104052 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.BuildManager
 * JD-Core Version:    0.5.4
 */