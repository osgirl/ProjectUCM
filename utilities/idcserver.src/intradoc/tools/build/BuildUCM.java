/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import intradoc.tools.common.LogPrintStreamWrapper;
/*     */ import intradoc.tools.utils.SimpleFileUtils;
/*     */ import intradoc.tools.utils.TextUtils;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.PatternFilter;
/*     */ import java.io.File;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintStream;
/*     */ import java.io.Writer;
/*     */ import java.text.DateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class BuildUCM
/*     */ {
/*     */   public BuildManager m_manager;
/*     */   public PatternFilter m_modulesFilter;
/*     */   protected final String[] m_arguments;
/*     */   protected boolean m_isSystemBootstrapped;
/*     */   protected boolean m_areModulesInitialized;
/*     */   protected boolean m_didCheckDepends;
/*     */   protected boolean m_didBuild;
/*     */   protected boolean m_didInstall;
/*     */   protected boolean m_didGenerate;
/*     */   protected boolean m_didShiphome;
/*     */   public boolean m_isQuery;
/*     */   public boolean m_areFetchesForced;
/*     */   public boolean m_areFetchChecksForced;
/*     */   public boolean m_shouldSkipGenerate;
/*     */   public static boolean s_didReassignOutput;
/*     */   public static boolean s_useDatesInLogFilenames;
/*     */   public static boolean s_shouldTruncateExistingLogFiles;
/*     */ 
/*     */   public BuildUCM(String[] args, Map<String, String> environment)
/*     */   {
/*  69 */     this.m_arguments = args;
/*  70 */     this.m_manager = new BuildManager(environment);
/*     */   }
/*     */ 
/*     */   public void usage(Object[] args)
/*     */   {
/*  80 */     if (args.length > 0)
/*     */     {
/*  82 */       StringBuilder str = new StringBuilder();
/*  83 */       for (int a = 0; a < args.length; ++a)
/*     */       {
/*  85 */         str.append(args[a]);
/*     */       }
/*  87 */       System.err.println(str.toString());
/*     */     }
/*  89 */     System.err.println(new StringBuilder().append("Usage: ").append(super.getClass().getSimpleName()).append(" [options] [target...]\n").append("       ").append(super.getClass().getSimpleName()).append(" [options] --query [queries...]\n").append("Where options are:\n").append("-h, --help\n\tShow this help text.\n").append("--modules modules_list, -m modules_list\n").append("\tSpecify (comma-separated) list of module names (or wildcards) to build.\n").append("\tPrefix names with \"+\" (default) or \"-\" to specify inclusion/exclusion\n").append("\twildcard rules.  By default all modules are built.\n").append("\tNOTE: When a module is included (to be built), all of its dependencies\n").append("\twill be included/built too.  The \"core\" module it always included.\n").append("--force-fetch\n\tForce all fetch dependencies to be downloaded again.\n").append("--force-fetch-checks\n\tDo HEAD requests to check fetch dependencies.\n").append("--skip-generate\n\tSkip the generate step; avoids calling IdcShell.\n").append("-t section_list, --trace section_list\n\tSpecify (comma-separated) trace section(s).\n").append("-v, --verbose\n\tEnable verbose output.  Use twice for debug tracing.\n").append("And target is one of:\n").append("  clean\n\tRemove build directory and intermediate files related to the build.\n").append("  check-depends\n\tVerify that all fetchable dependencies exist, but do not fetch them.\n").append("  default\n\t(or if no target is specified) same as specifying the \"all\" target.\n").append("  all\n\tAttempt every target, which is the same as specifying each of:\n").append("\n").append("  modules\n\tVerify modules and inter-dependencies.  This target is always implied.\n").append("  depends\n\tCheck and fetch all module dependencies.\n").append("  compile\n\tCompile java source code for all modules.\n").append("  package\n\tPackage all components.  Implies \"compile\".\n").append("  install\n\tInstall all components and generate component listing files.\n").append("  generate\n\tStart IdcServer and generate miscellaneous files (e.g. SQL scripts).\n").append("  shiphome\n\tPackage the remainder of the shiphome and generate zip file manifests.\n").append("Additionally, certain information can be queried for various purposes:\n").append("  classpath <module>\n\tOutput a list of Javac classpath elements for a given module.\n").append("  depends <module>\n\tOutput a list of modules that a given module depends upon.\n").append("").toString());
/*     */ 
/* 122 */     System.exit(1);
/* 123 */     throw new RuntimeException();
/*     */   }
/*     */ 
/*     */   public boolean processArgs(boolean doProcess)
/*     */   {
/* 132 */     BuildManager manager = this.m_manager;
/* 133 */     String[] args = this.m_arguments;
/* 134 */     BuildEnvironment env = manager.m_env;
/* 135 */     manager.setVerbose(false);
/* 136 */     this.m_isQuery = false;
/* 137 */     this.m_areFetchChecksForced = false;
/* 138 */     Vector traceSections = manager.m_traceSections;
/* 139 */     boolean didProcess = false;
/* 140 */     String command = null;
/*     */     try
/*     */     {
/* 143 */       for (int a = 0; a < args.length; ++a)
/*     */       {
/* 145 */         String arg = args[a];
/* 146 */         if (arg.startsWith("--"))
/*     */         {
/* 148 */           arg = arg.substring(2);
/* 149 */           if (arg.equals("help"))
/*     */           {
/* 151 */             usage(new Object[0]);
/* 152 */             return false;
/*     */           }
/* 154 */           if (arg.equals("releng"))
/*     */           {
/* 156 */             env.m_isReleng = true; continue;
/*     */           }
/* 158 */           if (arg.equals("force-fetch"))
/*     */           {
/* 160 */             this.m_areFetchesForced = true; continue;
/*     */           }
/* 162 */           if (arg.startsWith("force-fetch-check"))
/*     */           {
/* 164 */             this.m_areFetchChecksForced = true; continue;
/*     */           }
/* 166 */           if (arg.startsWith("module"))
/*     */           {
/* 168 */             if (++a >= args.length)
/*     */             {
/* 170 */               usage(new Object[] { "missing argument for --modules" });
/* 171 */               return false;
/*     */             }
/* 173 */             this.m_modulesFilter = TextUtils.createPatternFilterFromWildcards(args[a]); continue;
/*     */           }
/* 175 */           if (arg.equals("query"))
/*     */           {
/* 177 */             this.m_isQuery = true; continue;
/*     */           }
/* 179 */           if (arg.startsWith("skip-gen"))
/*     */           {
/* 181 */             this.m_shouldSkipGenerate = true; continue;
/*     */           }
/* 183 */           if (arg.equals("trace"))
/*     */           {
/* 185 */             if (++a >= args.length)
/*     */             {
/* 187 */               usage(new Object[] { "missing argument for --trace" });
/* 188 */               return false;
/*     */             }
/* 190 */             traceSections.add(args[a]); continue;
/*     */           }
/* 192 */           if (arg.equals("verbose"))
/*     */           {
/* 194 */             manager.setVerbose(true); continue;
/*     */           }
/*     */ 
/* 198 */           usage(new Object[] { "unknown option: --", arg });
/* 199 */           return false;
/*     */         }
/*     */ 
/* 202 */         if (arg.startsWith("-"))
/*     */         {
/* 204 */           int argLength = arg.length();
/* 205 */           if (argLength == 1)
/*     */           {
/* 207 */             usage(new Object[] { "missing option: -" });
/* 208 */             return false;
/*     */           }
/* 210 */           for (int i = 1; i < argLength; ++i)
/*     */           {
/* 212 */             char ch = arg.charAt(i);
/* 213 */             switch (ch)
/*     */             {
/*     */             case 'h':
/* 216 */               usage(new Object[0]);
/* 217 */               return false;
/*     */             case 'm':
/* 219 */               if (++a >= args.length)
/*     */               {
/* 221 */                 usage(new Object[] { "missing argument for -m" });
/* 222 */                 return false;
/*     */               }
/* 224 */               this.m_modulesFilter = TextUtils.createPatternFilterFromWildcards(args[a]);
/* 225 */               break;
/*     */             case 't':
/* 227 */               if (++a >= args.length)
/*     */               {
/* 229 */                 usage(new Object[] { "missing argument for -t" });
/* 230 */                 return false;
/*     */               }
/* 232 */               traceSections.add(args[a]);
/* 233 */               break;
/*     */             case 'v':
/* 235 */               manager.setVerbose(true);
/* 236 */               break;
/*     */             default:
/* 238 */               usage(new Object[] { "unknown option: -", Character.valueOf(ch) });
/* 239 */               return false;
/*     */             }
/*     */           }
/*     */         }
/*     */         else {
/* 244 */           if (!doProcess)
/*     */             continue;
/* 246 */           command = args[a];
/* 247 */           if (this.m_isQuery)
/*     */           {
/* 249 */             int numCommandArgs = args.length - ++a;
/* 250 */             String[] commandArgs = new String[numCommandArgs];
/* 251 */             System.arraycopy(args, a, commandArgs, 0, numCommandArgs);
/* 252 */             return processQuery(command, commandArgs);
/*     */           }
/* 254 */           if (!processTarget(command))
/*     */           {
/* 256 */             usage(new Object[] { "unknown target: ", command });
/* 257 */             return false;
/*     */           }
/* 259 */           didProcess = true;
/*     */         }
/*     */       }
/* 262 */       if ((doProcess) && (!didProcess))
/*     */       {
/* 264 */         if (this.m_isQuery)
/*     */         {
/* 266 */           usage(new Object[] { "missing query" });
/* 267 */           return false;
/*     */         }
/* 269 */         processTarget((env.m_isReleng) ? "all" : "default");
/*     */       }
/*     */     }
/*     */     catch (IdcException e)
/*     */     {
/* 274 */       if (this.m_isQuery)
/*     */       {
/* 276 */         manager.report(3, new Object[] { "Unable to query \"", command, "\"" });
/*     */       }
/* 278 */       else if (command != null)
/*     */       {
/* 280 */         manager.report(3, new Object[] { "Unable to build UCM with target \"", command, "\"" });
/*     */       }
/*     */       else
/*     */       {
/* 284 */         manager.report(3, new Object[] { "Unable to build UCM with default target" });
/*     */       }
/* 286 */       e.printStackTrace();
/* 287 */       return false;
/*     */     }
/* 289 */     return true;
/*     */   }
/*     */ 
/*     */   public Module processQueryModule(String[] args) throws IdcException
/*     */   {
/* 294 */     initModules();
/* 295 */     if (args.length != 1)
/*     */     {
/* 297 */       usage(new Object[] { "missing module name" });
/* 298 */       return null;
/*     */     }
/* 300 */     String moduleName = args[0];
/* 301 */     Module module = (Module)this.m_manager.m_env.m_modules.get(moduleName);
/* 302 */     if (module == null)
/*     */     {
/* 304 */       usage(new Object[] { "unknown module: ", moduleName });
/*     */     }
/* 306 */     return module;
/*     */   }
/*     */ 
/*     */   public boolean processQuery(String command, String[] args) throws IdcException
/*     */   {
/* 311 */     bootstrapSystemConfig();
/*     */ 
/* 313 */     if (command.equals("classpath"))
/*     */     {
/* 315 */       Module module = processQueryModule(args);
/* 316 */       if (module == null)
/*     */       {
/* 318 */         return false;
/*     */       }
/* 320 */       List classpath = new ArrayList();
/* 321 */       module.appendClasspathTo(classpath, 0);
/* 322 */       for (String element : classpath)
/*     */       {
/* 324 */         System.out.println(element);
/*     */       }
/*     */     }
/* 327 */     else if (command.equals("depends"))
/*     */     {
/* 329 */       Module module = processQueryModule(args);
/* 330 */       if (module == null)
/*     */       {
/* 332 */         return false;
/*     */       }
/* 334 */       String[] depends = module.m_requiredModules;
/* 335 */       if (depends != null)
/*     */       {
/* 337 */         for (String depend : depends)
/*     */         {
/* 339 */           System.out.println(depend);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 345 */       usage(new Object[] { "unknown query: ", command });
/* 346 */       return false;
/*     */     }
/* 348 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean processTarget(String command) throws IdcException
/*     */   {
/* 353 */     initModules();
/*     */ 
/* 355 */     if (command.equals("default"))
/*     */     {
/* 357 */       command = "all";
/*     */     }
/* 359 */     if (command.equals("all"))
/*     */     {
/* 361 */       checkModuleDepends(false);
/* 362 */       buildModules(false);
/* 363 */       installModules();
/* 364 */       makeServerGeneratedFiles();
/* 365 */       packageShiphome();
/*     */     }
/* 367 */     else if (command.equals("check-depends"))
/*     */     {
/* 369 */       checkModuleDepends(true);
/* 370 */       if (this.m_manager.m_env.m_didValidateErrorOccur)
/*     */       {
/* 372 */         throw new IdcException();
/*     */       }
/*     */     }
/* 375 */     else if (command.equals("clean"))
/*     */     {
/* 377 */       clean();
/*     */     }
/* 379 */     else if (command.equals("compile"))
/*     */     {
/* 381 */       buildModules(true);
/*     */     }
/* 383 */     else if (command.equals("depends"))
/*     */     {
/* 385 */       checkModuleDepends(false);
/*     */     }
/* 387 */     else if (command.equals("generate"))
/*     */     {
/* 389 */       makeServerGeneratedFiles();
/*     */     }
/* 391 */     else if (command.equals("install"))
/*     */     {
/* 393 */       installModules();
/*     */     }
/* 395 */     else if (!command.equals("modules"))
/*     */     {
/* 399 */       if (command.equals("package"))
/*     */       {
/* 401 */         buildModules(false);
/*     */       }
/* 403 */       else if (command.equals("shiphome"))
/*     */       {
/* 405 */         packageShiphome();
/*     */       }
/*     */       else
/*     */       {
/* 409 */         return false;
/*     */       }
/*     */     }
/* 411 */     return true;
/*     */   }
/*     */ 
/*     */   public void bootstrapSystemConfig()
/*     */     throws IdcException
/*     */   {
/* 417 */     if (this.m_isSystemBootstrapped)
/*     */       return;
/* 419 */     BuildManager manager = this.m_manager;
/* 420 */     manager.initBuildDirectories();
/* 421 */     BuildEnvironment env = manager.m_env;
/* 422 */     File buildDir = env.m_buildDir;
/* 423 */     initLogging(buildDir, manager.m_startTime);
/*     */ 
/* 425 */     String buildDirpath = buildDir.getPath();
/* 426 */     SystemUtils.setBinDir(buildDirpath);
/* 427 */     manager.writeSystemConfigForProduct("all");
/*     */ 
/* 429 */     this.m_manager.initSystemMinimal();
/*     */ 
/* 431 */     File binDir = new File(this.m_manager.m_env.m_intradocDir, "bin");
/* 432 */     binDir.mkdir();
/* 433 */     File configFile = new File(binDir, "intradoc.cfg");
/* 434 */     BuildUtils.writeUTF8FileSafely(configFile, new String[] { "<?cfg jcharset=\"UTF-8\"?>\n" });
/*     */ 
/* 436 */     this.m_isSystemBootstrapped = true;
/*     */   }
/*     */ 
/*     */   public void initModules()
/*     */     throws IdcException
/*     */   {
/* 442 */     bootstrapSystemConfig();
/*     */ 
/* 444 */     if (this.m_areModulesInitialized)
/*     */       return;
/* 446 */     BuildManager manager = this.m_manager;
/* 447 */     manager.loadBuildState();
/* 448 */     int flags = 0;
/* 449 */     if (this.m_areFetchesForced)
/*     */     {
/* 451 */       flags |= 1;
/*     */     }
/* 453 */     if (this.m_areFetchChecksForced)
/*     */     {
/* 455 */       flags |= 2;
/*     */     }
/* 457 */     manager.m_defaultUpdateModuleDependenciesFlags = flags;
/* 458 */     manager.loadModules();
/* 459 */     manager.applyModulesFilter(this.m_modulesFilter);
/* 460 */     manager.sortModuleDepends();
/*     */ 
/* 462 */     this.m_areModulesInitialized = true;
/*     */   }
/*     */ 
/*     */   public void clean()
/*     */     throws IdcException
/*     */   {
/* 470 */     this.m_isSystemBootstrapped = false;
/* 471 */     this.m_didCheckDepends = (this.m_didBuild = this.m_didInstall = this.m_didGenerate = this.m_didShiphome = 0);
/* 472 */     long timeStart = System.currentTimeMillis();
/* 473 */     BuildManager manager = this.m_manager;
/* 474 */     BuildEnvironment env = manager.m_env;
/* 475 */     Map modules = env.m_modules;
/* 476 */     List moduleNames = env.m_sortedModuleNames;
/* 477 */     for (int m = moduleNames.size() - 1; m >= 0; --m)
/*     */     {
/* 479 */       String moduleName = (String)moduleNames.get(m);
/* 480 */       Module module = (Module)modules.get(moduleName);
/* 481 */       if (module.m_isExcludedFromBuild) {
/*     */         continue;
/*     */       }
/*     */ 
/* 485 */       module.clean();
/*     */     }
/*     */ 
/* 488 */     File buildDir = env.m_buildDir;
/* 489 */     FileUtils.deleteDirectory(buildDir, true);
/*     */ 
/* 491 */     long timeStop = System.currentTimeMillis();
/* 492 */     long ms = timeStop - timeStart;
/* 493 */     manager.report(6, new Object[] { "finished cleaning (", Long.valueOf(ms), " ms)" });
/*     */   }
/*     */ 
/*     */   public void checkModuleDepends(boolean isCheckOnly) throws IdcException
/*     */   {
/* 498 */     BuildManager manager = this.m_manager;
/* 499 */     BuildEnvironment env = manager.m_env;
/* 500 */     if (this.m_didCheckDepends)
/*     */     {
/* 502 */       return;
/*     */     }
/* 504 */     manager.report(6, new Object[] { "checking dependencies ..." });
/* 505 */     List moduleNames = env.m_sortedModuleNames;
/* 506 */     int numModules = moduleNames.size();
/* 507 */     Map modules = env.m_modules;
/* 508 */     int flags = (isCheckOnly) ? 272 : 0;
/*     */ 
/* 510 */     for (int m = 0; m < numModules; ++m)
/*     */     {
/* 512 */       String moduleName = (String)moduleNames.get(m);
/* 513 */       Module module = (Module)modules.get(moduleName);
/* 514 */       if (module.m_isExcludedFromBuild) {
/*     */         continue;
/*     */       }
/*     */ 
/* 518 */       module.reload();
/* 519 */       manager.checkModuleDepends(module, flags);
/*     */     }
/* 521 */     this.m_didCheckDepends = true;
/*     */   }
/*     */ 
/*     */   public void buildModules(boolean isCompileOnly) throws IdcException
/*     */   {
/* 526 */     if (this.m_didBuild)
/*     */     {
/* 528 */       return;
/*     */     }
/* 530 */     BuildManager manager = this.m_manager;
/* 531 */     manager.initCompiler();
/* 532 */     long timeStart = System.currentTimeMillis();
/* 533 */     BuildEnvironment env = manager.m_env;
/* 534 */     env.m_defaultClasspath = new ArrayList();
/* 535 */     File componentZipsDir = env.m_componentZipsDir;
/* 536 */     componentZipsDir.mkdir();
/* 537 */     Set modulesWithJava = env.m_modulesWithJava = new HashSet();
/* 538 */     Map modules = env.m_modules;
/* 539 */     List moduleNames = env.m_sortedModuleNames;
/* 540 */     int numModules = moduleNames.size();
/* 541 */     int numComponents = 0;
/* 542 */     for (int m = 0; m < numModules; ++m)
/*     */     {
/* 544 */       String moduleName = (String)moduleNames.get(m);
/* 545 */       Module module = (Module)modules.get(moduleName);
/* 546 */       String action = (module.m_isExcludedFromBuild) ? "skipping " : "building ";
/* 547 */       manager.report(6, new Object[] { action, moduleName, " (", Integer.valueOf(m), " of ", Integer.valueOf(numModules), " modules)..." });
/*     */ 
/* 549 */       if (module.m_isExcludedFromBuild) {
/*     */         continue;
/*     */       }
/*     */ 
/* 553 */       module.updateDependencies(144, manager);
/* 554 */       if (isCompileOnly)
/*     */       {
/* 556 */         module.compileJava();
/*     */       }
/* 560 */       else if (module.buildAndPackage(componentZipsDir))
/*     */       {
/* 562 */         ++numComponents;
/*     */       }
/*     */ 
/* 565 */       if ((!module.m_hasJava) || (moduleName.equals("core")))
/*     */         continue;
/* 567 */       modulesWithJava.add(moduleName);
/*     */     }
/*     */ 
/* 570 */     if (isCompileOnly)
/*     */       return;
/* 572 */     this.m_didBuild = true;
/* 573 */     long timeEnd = System.currentTimeMillis();
/* 574 */     long timeInSeconds = (timeEnd - timeStart + 500L) / 1000L;
/* 575 */     manager.report(6, new Object[] { "finished updating: ", Integer.valueOf(numComponents), " components, ", Integer.valueOf(numModules), " modules (", Long.valueOf(timeInSeconds), " seconds)\n" });
/*     */   }
/*     */ 
/*     */   public void installModules()
/*     */     throws IdcException
/*     */   {
/* 582 */     BuildManager manager = this.m_manager;
/* 583 */     if (this.m_didInstall)
/*     */     {
/* 585 */       return;
/*     */     }
/* 587 */     manager.report(6, new Object[] { "installing components ..." });
/* 588 */     manager.loadBuildState();
/* 589 */     manager.initSystemForComponentList();
/* 590 */     long timeStart = System.currentTimeMillis();
/* 591 */     int numInstalled = 0;
/*     */ 
/* 593 */     BuildEnvironment env = manager.m_env;
/* 594 */     ComponentListEditor editor = env.m_componentListEditor = ComponentListManager.getEditor();
/* 595 */     Map modules = env.m_modules;
/* 596 */     List moduleNames = env.m_sortedModuleNames;
/* 597 */     int numModules = moduleNames.size();
/* 598 */     for (int m = 0; m < numModules; ++m)
/*     */     {
/* 600 */       String moduleName = (String)moduleNames.get(m);
/* 601 */       Module module = (Module)modules.get(moduleName);
/* 602 */       if (module.m_isExcludedFromBuild)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 607 */       if (!module instanceof Component)
/*     */         continue;
/* 609 */       Component component = (Component)module;
/* 610 */       component.install();
/* 611 */       if (component.m_wasInstalled)
/*     */       {
/* 613 */         ++numInstalled;
/*     */       }
/* 615 */       if (!component.m_haveTagsChanged)
/*     */         continue;
/* 617 */       env.m_isServerGenerationNeeded = true;
/*     */     }
/*     */ 
/* 621 */     if (numInstalled > 0)
/*     */     {
/* 623 */       env.m_isServerGenerationNeeded = true;
/*     */ 
/* 631 */       editor.loadComponents();
/* 632 */       editor.save();
/*     */ 
/* 634 */       File componentBinderFile = new File(env.m_intradocDir, "data/components/all_components.hda");
/* 635 */       String componentBinderFilename = componentBinderFile.getPath();
/* 636 */       DataBinder componentBinder = ResourceLoader.loadDataBinderFromFile(componentBinderFilename);
/* 637 */       DataResultSet componentsRSet = (DataResultSet)componentBinder.getResultSet("Components");
/* 638 */       int nameIndex = componentsRSet.getFieldInfoIndex("name");
/* 639 */       ResultSetTreeSort sort = new ResultSetTreeSort(componentsRSet, nameIndex, false);
/* 640 */       sort.sort();
/* 641 */       BuildUtils.writeSortedDataBinder(componentBinder, componentBinderFile);
/*     */     }
/* 643 */     manager.saveBuildState();
/* 644 */     long timeStop = System.currentTimeMillis();
/* 645 */     long timeInSeconds = (timeStop - timeStart + 500L) / 1000L;
/* 646 */     manager.report(6, new Object[] { "installed ", Integer.valueOf(numInstalled), " components (", Long.valueOf(timeInSeconds), " seconds)" });
/*     */ 
/* 649 */     if (env.m_isServerGenerationNeeded)
/*     */     {
/* 651 */       manager.report(6, new Object[] { "generating component listings and state.cfg files..." });
/* 652 */       Properties props = env.m_properties;
/* 653 */       String intradocDirpath = props.getProperty("IntradocDir");
/* 654 */       String configDirpath = props.getProperty("ConfigDir");
/* 655 */       String componentsDirpath = new StringBuilder().append(intradocDirpath).append("/data/components/").toString();
/* 656 */       Map editorEnv = new HashMap();
/* 657 */       DataResultSet allComponentsSet = editor.getComponentSet();
/* 658 */       Collection allComponents = ResultSetUtils.loadValuesFromSet(allComponentsSet, "name");
/* 659 */       Set componentsExcluded = new HashSet();
/* 660 */       StringBuilder componentsDisabled = new StringBuilder();
/*     */ 
/* 662 */       String[] productNames = env.m_productNames;
/* 663 */       for (String productName : productNames)
/*     */       {
/* 665 */         editorEnv.put("StateCfgFilename", new StringBuilder().append("state-").append(productName).append(".cfg").toString());
/* 666 */         editorEnv.put("IsInstallerEnv", "1");
/*     */ 
/* 668 */         editor.init(intradocDirpath, configDirpath, componentsDirpath, intradocDirpath, editorEnv);
/* 669 */         editor.setProductName(productName);
/*     */ 
/* 671 */         componentsExcluded.clear();
/* 672 */         componentsExcluded.addAll(allComponents);
/*     */ 
/* 674 */         componentsDisabled.setLength(0);
/* 675 */         for (int m = numModules - 1; m >= 0; --m)
/*     */         {
/* 677 */           String moduleName = (String)moduleNames.get(m);
/* 678 */           Module module = (Module)modules.get(moduleName);
/* 679 */           if (!module instanceof Component)
/*     */             continue;
/* 681 */           Component component = (Component)module;
/* 682 */           Component.ProductTag productTag = (Component.ProductTag)component.m_productTags.get(productName);
/* 683 */           if (productTag == null)
/*     */             continue;
/* 685 */           componentsExcluded.remove(module.m_moduleName);
/* 686 */           if (productTag.m_isEnabled)
/*     */             continue;
/* 688 */           if (componentsDisabled.length() > 0)
/*     */           {
/* 690 */             componentsDisabled.append(',');
/*     */           }
/* 692 */           componentsDisabled.append(module.m_moduleName);
/*     */         }
/*     */ 
/* 698 */         editor.save();
/*     */ 
/* 700 */         editor.deleteComponents(componentsExcluded);
/*     */ 
/* 702 */         editor.enableOrDisableComponentEx(componentsDisabled.toString(), false, true);
/*     */       }
/*     */     }
/* 705 */     this.m_didInstall = true;
/*     */   }
/*     */ 
/*     */   public void makeServerGeneratedFiles() throws IdcException
/*     */   {
/* 710 */     BuildManager manager = this.m_manager;
/* 711 */     BuildEnvironment env = manager.m_env;
/* 712 */     if (this.m_didGenerate)
/*     */     {
/* 714 */       return;
/*     */     }
/* 716 */     List manifest = env.m_labelManifest = new ArrayList();
/* 717 */     manifest.add("#");
/* 718 */     manifest.add("# this file is auto-generated by BuildECM");
/* 719 */     manifest.add("#");
/* 720 */     Map modules = env.m_modules;
/* 721 */     List moduleNames = env.m_sortedModuleNames;
/* 722 */     int numModules = moduleNames.size();
/* 723 */     for (int m = 0; m < numModules; ++m)
/*     */     {
/* 725 */       String moduleName = (String)moduleNames.get(m);
/* 726 */       Module module = (Module)modules.get(moduleName);
/* 727 */       if (module.m_isExcludedFromBuild) {
/*     */         continue;
/*     */       }
/*     */ 
/* 731 */       module.prepareShiphome(false);
/*     */     }
/* 733 */     List manifestLines = env.m_labelManifest;
/* 734 */     if ((manifestLines != null) && (!manifestLines.isEmpty()))
/*     */     {
/* 736 */       manager.report(6, new Object[] { "writing label.manifest ..." });
/* 737 */       File manifestFile = new File(env.m_buildDir, "label.manifest");
/*     */       try
/*     */       {
/* 740 */         FileOutputStream fos = new FileOutputStream(manifestFile);
/* 741 */         OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
/* 742 */         for (String line : manifestLines)
/*     */         {
/* 744 */           writer.write(line);
/* 745 */           writer.write(10);
/*     */         }
/* 747 */         writer.close();
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 751 */         String msg = new StringBuilder().append("unable to write ").append(manifestFile).toString();
/* 752 */         throw new ServiceException(msg, ioe);
/*     */       }
/*     */     }
/*     */ 
/* 756 */     if (this.m_shouldSkipGenerate)
/*     */     {
/* 758 */       return;
/*     */     }
/* 760 */     manager.checkInitServerForProduct("idccs");
/* 761 */     for (int m = 0; m < numModules; ++m)
/*     */     {
/* 763 */       String moduleName = (String)moduleNames.get(m);
/* 764 */       Module module = (Module)modules.get(moduleName);
/* 765 */       if (module.m_isExcludedFromBuild) {
/*     */         continue;
/*     */       }
/*     */ 
/* 769 */       module.prepareShiphome(true);
/*     */     }
/*     */ 
/* 772 */     env.m_isServerGenerationNeeded = false;
/* 773 */     manager.saveBuildState();
/* 774 */     this.m_didGenerate = true;
/*     */   }
/*     */ 
/*     */   public void packageShiphome() throws IdcException
/*     */   {
/* 779 */     if (this.m_didShiphome)
/*     */     {
/* 781 */       return;
/*     */     }
/* 783 */     BuildManager manager = this.m_manager;
/* 784 */     BuildEnvironment env = manager.m_env;
/* 785 */     manager.report(6, new Object[] { "making shiphome zips package lists..." });
/* 786 */     Module coreModule = manager.m_coreModule;
/* 787 */     File coreModuleDir = coreModule.m_moduleDir;
/* 788 */     DataResultSet rules = (DataResultSet)coreModule.m_buildConfig.getResultSet("ShiphomePackageRules");
/* 789 */     int sourceIndex = ResultSetUtils.getIndexMustExist(rules, "source");
/* 790 */     int targetIndex = ResultSetUtils.getIndexMustExist(rules, "target");
/* 791 */     PackageRule.Group packages = coreModule.createPackageListFromResultSet(rules, sourceIndex, targetIndex, null);
/*     */ 
/* 793 */     long lastTime = System.currentTimeMillis();
/* 794 */     for (PackageRule pkg : packages)
/*     */     {
/* 796 */       File packageFile = pkg.getTargetFile(coreModuleDir);
/* 797 */       String packageFilename = packageFile.getName();
/* 798 */       manager.report(7, new Object[] { "generating manifest for ", packageFilename, " ..." });
/* 799 */       File targetDir = packageFile.getParentFile();
/* 800 */       targetDir.mkdirs();
/* 801 */       int dotIndex = packageFilename.lastIndexOf(46);
/* 802 */       String filenamePrefix = (dotIndex < 0) ? packageFilename : packageFilename.substring(0, dotIndex);
/* 803 */       String manifestFilename = new StringBuilder().append(filenamePrefix).append(".manifest").toString();
/* 804 */       File manifestFile = new File(targetDir, manifestFilename);
/*     */       Writer writer;
/*     */       try
/*     */       {
/* 808 */         FileOutputStream fos = new FileOutputStream(manifestFile);
/* 809 */         writer = new OutputStreamWriter(fos, "UTF-8");
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 813 */         throw new ServiceException(t);
/*     */       }
/*     */ 
/* 816 */       for (PackageRule.Item item : pkg)
/*     */       {
/* 818 */         String sourceDirname = item.m_dirname;
/* 819 */         File sourceDir = null;
/* 820 */         if (!FileUtils.isAbsolutePath(sourceDirname))
/*     */         {
/* 822 */           sourceDir = env.m_shiphomeDir;
/*     */         }
/* 824 */         sourceDir = new File(sourceDir, sourceDirname);
/* 825 */         PatternFilter filter = item.m_filter;
/* 826 */         List filenames = SimpleFileUtils.scanFilesFiltered(sourceDir, filter, null, null);
/* 827 */         int numFiles = filenames.size();
/* 828 */         if (numFiles == 0)
/*     */         {
/* 830 */           manager.report(4, new Object[] { "file scan filter returned no files in ", sourceDirname, "\nfilter:\n", filter });
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 835 */           writer.write("@cd ");
/* 836 */           writer.write(sourceDir.getPath());
/* 837 */           writer.write("\n@filelist ");
/* 838 */           writer.write(Integer.toString(numFiles));
/* 839 */           writer.write(10);
/* 840 */           for (String filename : filenames)
/*     */           {
/* 842 */             writer.write(filename);
/* 843 */             writer.write(10);
/*     */           }
/*     */         }
/*     */         catch (Throwable t)
/*     */         {
/* 848 */           throw new ServiceException(t);
/*     */         }
/*     */       }
/* 851 */       FileUtils.closeObject(writer);
/*     */     }
/* 853 */     long now = System.currentTimeMillis();
/* 854 */     long ms = now - lastTime;
/* 855 */     manager.report(6, new Object[] { "finished manifests (", Long.valueOf(ms), " ms)" });
/* 856 */     this.m_didShiphome = true;
/*     */   }
/*     */ 
/*     */   public static void initLogging(File logDir, long timestamp)
/*     */   {
/* 865 */     if (s_didReassignOutput)
/*     */     {
/* 867 */       return;
/*     */     }
/*     */     String logFilename;
/*     */     String errFilename;
/*     */     String outFilename;
/*     */     String logFilename;
/* 870 */     if (s_useDatesInLogFilenames)
/*     */     {
/* 872 */       DateFormat iso8601 = BuildEnvironment.s_iso8601;
/* 873 */       String dateString = iso8601.format(Long.valueOf(timestamp));
/* 874 */       String errFilename = new StringBuilder().append("BuildUCM-").append(dateString).append(".err").toString();
/* 875 */       String outFilename = new StringBuilder().append("BuildUCM-").append(dateString).append(".out").toString();
/* 876 */       logFilename = new StringBuilder().append("BuildUCM-").append(dateString).append(".log").toString();
/*     */     }
/*     */     else
/*     */     {
/* 880 */       errFilename = "BuildUCM.err";
/* 881 */       outFilename = "BuildUCM.out";
/* 882 */       logFilename = "BuildUCM.log";
/*     */     }
/*     */     try
/*     */     {
/* 886 */       File errFile = new File(logDir, errFilename);
/* 887 */       File outFile = new File(logDir, outFilename);
/* 888 */       File logFile = new File(logDir, logFilename);
/*     */ 
/* 890 */       if (s_shouldTruncateExistingLogFiles)
/*     */       {
/* 892 */         errFile.delete();
/* 893 */         outFile.delete();
/* 894 */         logFile.delete();
/*     */       }
/* 896 */       FileOutputStream errStream = new FileOutputStream(errFile, true);
/* 897 */       FileOutputStream outStream = new FileOutputStream(outFile, true);
/* 898 */       FileOutputStream logStream = new FileOutputStream(logFile, true);
/* 899 */       PrintStream err = System.err;
/* 900 */       PrintStream out = System.out;
/* 901 */       PrintStream logErr = new LogPrintStreamWrapper(err, new FileOutputStream[] { errStream, logStream });
/* 902 */       PrintStream logOut = new LogPrintStreamWrapper(out, new FileOutputStream[] { outStream, logStream });
/* 903 */       System.setErr(logErr);
/* 904 */       System.setOut(logOut);
/* 905 */       s_didReassignOutput = true;
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 909 */       t.printStackTrace();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void main(String[] args)
/*     */   {
/* 915 */     Map env = System.getenv();
/* 916 */     int rc = main(args, env);
/* 917 */     System.exit(rc);
/*     */   }
/*     */ 
/*     */   public static int main(String[] args, Map<String, String> env)
/*     */   {
/* 922 */     BuildUCM build = new BuildUCM(args, env);
/* 923 */     long timeStart = build.m_manager.m_startTime;
/* 924 */     if (!build.processArgs(false))
/*     */     {
/* 926 */       return 1;
/*     */     }
/*     */     boolean wasSuccessful;
/*     */     try
/*     */     {
/* 931 */       wasSuccessful = build.processArgs(true);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 935 */       t.printStackTrace();
/* 936 */       wasSuccessful = false;
/*     */     }
/* 938 */     long timeDone = System.currentTimeMillis();
/* 939 */     long secs = (timeDone - timeStart) / 1000L;
/* 940 */     long ms = (timeDone - timeStart + 50L) / 100L % 10L;
/* 941 */     StringBuilder msg = new StringBuilder();
/* 942 */     msg.append("BuildUCM ");
/* 943 */     if (wasSuccessful)
/*     */     {
/* 945 */       msg.append("finished successfully.");
/*     */     }
/*     */     else
/*     */     {
/* 949 */       msg.append("FAILED!");
/*     */     }
/* 951 */     msg.append("  Build took ");
/* 952 */     msg.append(secs);
/* 953 */     msg.append('.');
/* 954 */     msg.append(ms);
/* 955 */     msg.append(" seconds.");
/* 956 */     System.err.println(msg.toString());
/* 957 */     return (wasSuccessful) ? 0 : 1;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 963 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101703 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.BuildUCM
 * JD-Core Version:    0.5.4
 */