/*     */ package intradoc.tools.build;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PathUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.loader.IdcClassLoader;
/*     */ import intradoc.tools.common.ComponentListWrapper;
/*     */ import intradoc.tools.common.IdcClassLoaderWrapper;
/*     */ import intradoc.tools.common.IdcShellWrapper;
/*     */ import intradoc.tools.common.JavaCompileManager;
/*     */ import intradoc.tools.common.TextFileReader;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcException;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.text.DateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class CoreModule extends Module
/*     */ {
/*     */   public File m_sourcesDir;
/*     */   public File m_classesDir;
/*     */   protected boolean m_didCompileJava;
/*     */   protected boolean m_isSetVersionInfo;
/*     */   protected boolean m_isSetRevisionInfo;
/*     */ 
/*     */   public CoreModule()
/*     */   {
/*  59 */     this.m_isSetVersionInfo = true;
/*  60 */     this.m_isSetRevisionInfo = true;
/*     */   }
/*     */ 
/*     */   public void init(BuildManager manager, File moduleDir)
/*     */     throws IdcException
/*     */   {
/*  68 */     super.init(manager, moduleDir);
/*  69 */     BuildEnvironment env = manager.m_env;
/*  70 */     this.m_moduleName = "core";
/*  71 */     this.m_sourcesDir = moduleDir;
/*     */ 
/*  73 */     this.m_moduleDir = env.m_shiphomeDir;
/*  74 */     this.m_javaDir = new File(env.m_branchDir, "sources/java");
/*  75 */     this.m_hasJava = true;
/*     */ 
/*  77 */     this.m_classesDir = new File(env.m_buildDir, "classes-core");
/*     */ 
/*  79 */     DataBinder binder = this.m_buildConfig;
/*  80 */     String productNamesString = binder.getLocal("IdcProductNames");
/*  81 */     env.m_productNames = productNamesString.split(",");
/*     */   }
/*     */ 
/*     */   public void reload()
/*     */     throws IdcException
/*     */   {
/*  87 */     BuildManager manager = this.m_manager;
/*  88 */     BuildEnvironment env = manager.m_env;
/*     */ 
/*  91 */     super.reload();
/*     */ 
/*  93 */     manager.initSVNKit();
/*  94 */     if (env.m_branch == null)
/*     */     {
/*  96 */       String branch = manager.computeUCMBranchFromWorkingCopyDir(env.m_branchDir);
/*  97 */       report(6, new Object[] { "subversion working copy branch ", branch });
/*  98 */       env.m_branch = branch;
/*  99 */       env.m_properties.put("branch", branch);
/*     */     }
/*     */ 
/* 102 */     String branchDirname = new StringBuilder().append(env.m_buildDir.getPath()).append('/').toString();
/* 103 */     String branchManifestURL = this.m_properties.getProperty("branchManifest");
/* 104 */     checkFetchDependency(branchManifestURL, branchDirname, null, 0, manager);
/*     */ 
/* 107 */     String labelSeries = this.m_properties.getProperty("UCMLabelSeries");
/* 108 */     if ((labelSeries == null) || (labelSeries.length() == 0))
/*     */     {
/* 110 */       labelSeries = null;
/* 111 */       String wcBranch = env.m_branch;
/* 112 */       String wcRevisionString = (String)env.m_wcProps.get("revision");
/* 113 */       int wcRevision = NumberUtils.parseInteger(wcRevisionString, 0);
/* 114 */       if (wcRevision < 1)
/*     */       {
/* 116 */         throw new ServiceException(new StringBuilder().append("bad working copy revision: ").append(wcRevisionString).toString());
/*     */       }
/* 118 */       manager.report(6, new Object[] { "subversion working copy revision ", Integer.valueOf(wcRevision) });
/*     */ 
/* 120 */       boolean foundRevision = false;
/* 121 */       File manifestFile = new File(env.m_buildDir, "branch.manifest");
/* 122 */       manager.report(7, new Object[] { "reading ", manifestFile, " ..." });
/* 123 */       TextFileReader reader = new TextFileReader(manifestFile);
/*     */       try
/*     */       {
/* 126 */         reader.open();
/*     */ 
/* 128 */         while ((line = reader.readLine()) != null)
/*     */         {
/* 130 */           String line;
/* 130 */           if (line.length() == 0) continue; if (line.startsWith("#")) {
/*     */             continue;
/*     */           }
/*     */ 
/* 134 */           String[] words = line.split("\\s+");
/* 135 */           if (words.length == 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 139 */           if (!foundRevision)
/*     */           {
/* 141 */             if (words.length != 1) {
/*     */               break;
/*     */             }
/*     */ 
/* 145 */             String minRevisionString = words[0];
/* 146 */             if (minRevisionString.startsWith("r"))
/*     */             {
/* 148 */               int minRevision = NumberUtils.parseInteger(minRevisionString.substring(1), 0);
/* 149 */               if (minRevision < 1)
/*     */               {
/* 151 */                 throw new ServiceException(new StringBuilder().append("bad minimum revision, line ").append(reader.m_lineNumber).toString());
/*     */               }
/* 153 */               manager.report(7, new Object[] { "comparing to minimum revision ", Integer.valueOf(minRevision) });
/* 154 */               if (minRevision > wcRevision)
/*     */               {
/* 156 */                 String msg = new StringBuilder().append("This working copy is too old.  Please update to r").append(wcRevision).toString();
/* 157 */                 throw new ServiceException(msg);
/*     */               }
/*     */             }
/*     */             else
/*     */             {
/* 162 */               throw new ServiceException(new StringBuilder().append("missing minimum revision, line ").append(reader.m_lineNumber).toString());
/*     */             }
/* 164 */             foundRevision = true;
/*     */           }
/*     */           else
/*     */           {
/* 169 */             if (words.length != 2) {
/*     */               break;
/*     */             }
/*     */ 
/* 173 */             manager.report(7, new Object[] { "subversion branch \"", words[0], "\" -> ADE series \"", words[1], "\"" });
/*     */ 
/* 175 */             if (words[0].equals(wcBranch))
/*     */             {
/* 177 */               labelSeries = words[1];
/* 178 */               break;
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/*     */         String msg;
/* 186 */         throw new ServiceException(msg, ioe);
/*     */       }
/*     */       finally
/*     */       {
/* 190 */         reader.close();
/*     */       }
/*     */ 
/* 193 */       if (!foundRevision)
/*     */       {
/* 195 */         throw new ServiceException(new StringBuilder().append("missing minimum revision in ").append(manifestFile).toString());
/*     */       }
/* 197 */       if (labelSeries == null)
/*     */       {
/* 199 */         throw new ServiceException("unable to determine UCMLabelSeries");
/*     */       }
/* 201 */       manager.report(6, new Object[] { "setting UCMLabelSeries=", labelSeries });
/* 202 */       env.m_properties.setProperty("UCMLabelSeries", labelSeries);
/*     */ 
/* 204 */       super.reload();
/*     */     }
/* 206 */     StringBuilder sb = new StringBuilder();
/* 207 */     String[] labelSeriesSplit = labelSeries.split("_");
/* 208 */     if ((this.m_isSetVersionInfo) && (labelSeriesSplit.length > 1))
/*     */     {
/* 210 */       String buildDate = BuildEnvironment.s_iso8601.format(Long.valueOf(this.m_manager.m_startTime));
/* 211 */       sb.append(labelSeriesSplit[1]);
/* 212 */       sb.append('-');
/* 213 */       sb.append(buildDate);
/* 214 */       env.m_productVersionName = sb.toString();
/* 215 */       sb.setLength(0);
/*     */ 
/* 217 */       this.m_isSetVersionInfo = false;
/*     */     }
/* 219 */     String labelName = System.getenv("UCM_LABEL");
/* 220 */     if ((!this.m_isSetRevisionInfo) || (labelName == null))
/*     */       return;
/* 222 */     env.m_productBuild = labelName;
/*     */ 
/* 224 */     int indexOfRev = labelName.lastIndexOf(46);
/* 225 */     if (indexOfRev > 0)
/*     */     {
/* 227 */       String rev = labelName.substring(indexOfRev + 1);
/* 228 */       if (rev.length() > 4)
/*     */       {
/* 230 */         env.m_productVersionName = new StringBuilder().append(env.m_productVersionName).append("-r").append(rev).toString();
/*     */       }
/*     */     }
/* 233 */     this.m_isSetRevisionInfo = false;
/*     */   }
/*     */ 
/*     */   public PackageRule.Group loadPackageRules()
/*     */     throws IdcException
/*     */   {
/* 240 */     DataResultSet packageRules = this.m_packageRulesTable;
/* 241 */     if (packageRules == null)
/*     */     {
/* 243 */       packageRules = this.m_packageRulesTable = (DataResultSet)this.m_buildConfig.getResultSet("CorePackageRules");
/* 244 */       String[] columnNames = { "source" };
/* 245 */       String[] defaultValues = { "$BuildDir/classes-core" };
/* 246 */       ResultSetUtils.addColumnsWithDefaultValues(packageRules, null, defaultValues, columnNames);
/*     */     }
/* 248 */     return super.loadPackageRules();
/*     */   }
/*     */ 
/*     */   public void compileJava()
/*     */     throws IdcException
/*     */   {
/* 254 */     if (this.m_didCompileJava)
/*     */     {
/* 256 */       return;
/*     */     }
/* 258 */     DataBinder buildConfig = this.m_buildConfig;
/* 259 */     BuildEnvironment env = this.m_manager.m_env;
/* 260 */     File javaDir = this.m_javaDir;
/* 261 */     File classesDir = this.m_classesDir;
/* 262 */     long timeStart = System.currentTimeMillis();
/*     */ 
/* 265 */     DataResultSet javaPackagesResultSet = (DataResultSet)buildConfig.getResultSet("CoreJavaPackages");
/* 266 */     int packageIndex = ResultSetUtils.getIndexMustExist(javaPackagesResultSet, "package");
/* 267 */     List packageFilters = new ArrayList();
/* 268 */     for (javaPackagesResultSet.first(); javaPackagesResultSet.isRowPresent(); javaPackagesResultSet.next())
/*     */     {
/* 270 */       CoreJavaPackageFilter filter = new CoreJavaPackageFilter();
/* 271 */       String packagePrefix = filter.m_packagePrefix = javaPackagesResultSet.getStringValue(packageIndex);
/* 272 */       filter.m_pathPrefix = packagePrefix.replace('.', '/');
/* 273 */       filter.m_matchingFiles = new ArrayList();
/* 274 */       packageFilters.add(filter);
/*     */     }
/* 276 */     CoreJavaPackageFilter allFilter = new CoreJavaPackageFilter();
/* 277 */     allFilter.m_matchingFiles = new ArrayList();
/* 278 */     packageFilters.add(allFilter);
/* 279 */     scanCoreJavaFiles("", javaDir, packageFilters);
/*     */ 
/* 282 */     JavaCompileManager compiler = this.m_manager.m_env.m_javaCompiler;
/* 283 */     int numFilters = packageFilters.size();
/* 284 */     for (int p = 0; p < numFilters; ++p)
/*     */     {
/* 286 */       CoreJavaPackageFilter filter = (CoreJavaPackageFilter)packageFilters.get(p);
/* 287 */       compiler.m_description = ((filter.m_packagePrefix != null) ? new StringBuilder().append(filter.m_packagePrefix).append(".*").toString() : "*");
/*     */       try
/*     */       {
/* 290 */         compiler.compileOutdated(javaDir, classesDir, filter.m_matchingFiles);
/*     */       }
/*     */       catch (IOException ioe)
/*     */       {
/* 294 */         throw new ServiceException(ioe);
/*     */       }
/*     */     }
/* 297 */     if (this.m_manager.m_env.m_trace != null)
/*     */     {
/* 299 */       long timeAfterCompile = System.currentTimeMillis();
/* 300 */       long seconds = (timeAfterCompile - timeStart + 500L) / 1000L;
/* 301 */       this.m_manager.m_env.m_trace.report(6, new Object[] { "total compilation took ", Long.valueOf(seconds), " seconds" });
/*     */     }
/* 303 */     this.m_didCompileJava = true;
/*     */ 
/* 305 */     env.m_defaultClasspath.add(classesDir.getPath());
/*     */     try
/*     */     {
/* 310 */       String classesDirpath = classesDir.getPath();
/* 311 */       env.m_loader.addClassPathElement(classesDirpath, 16);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 315 */       throw new ServiceException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean buildAndPackage(File targetDir) throws IdcException
/*     */   {
/* 321 */     super.buildAndPackage(targetDir);
/*     */ 
/* 323 */     BuildEnvironment env = this.m_manager.m_env;
/*     */ 
/* 327 */     if (env.m_isReleng)
/*     */     {
/* 329 */       JarSigningModule.setupSignerAndSignPackages(this.m_manager, this);
/*     */     }
/*     */ 
/* 332 */     return true;
/*     */   }
/*     */ 
/*     */   public void scanCoreJavaFiles(String prefix, File dir, List<CoreJavaPackageFilter> filters)
/*     */   {
/* 337 */     int numFilters = filters.size();
/* 338 */     File[] files = dir.listFiles();
/* 339 */     Arrays.sort(files);
/* 340 */     for (int f = 0; f < files.length; ++f)
/*     */     {
/* 342 */       File file = files[f];
/* 343 */       String filename = file.getName();
/* 344 */       String pathname = new StringBuilder().append(prefix).append(filename).toString();
/* 345 */       if (file.isDirectory())
/*     */       {
/* 347 */         if (filename.equals(".svn"))
/*     */           continue;
/* 349 */         scanCoreJavaFiles(new StringBuilder().append(pathname).append('/').toString(), file, filters);
/*     */       }
/*     */       else
/*     */       {
/* 353 */         if (!filename.endsWith(".java")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 357 */         for (int p = 0; p < numFilters; ++p)
/*     */         {
/* 359 */           CoreJavaPackageFilter filter = (CoreJavaPackageFilter)filters.get(p);
/* 360 */           String matchPrefix = filter.m_pathPrefix;
/* 361 */           if ((matchPrefix != null) && (!pathname.startsWith(matchPrefix)))
/*     */             continue;
/* 363 */           filter.m_matchingFiles.add(pathname);
/* 364 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public List<List<String>> computeActionsFromTable(String tableName)
/*     */     throws IdcException
/*     */   {
/* 381 */     BuildEnvironment env = this.m_manager.m_env;
/* 382 */     DataBinder binder = this.m_buildConfig;
/* 383 */     DataResultSet commands = (DataResultSet)binder.getResultSet(tableName);
/* 384 */     int actionIndex = ResultSetUtils.getIndexMustExist(commands, "action");
/* 385 */     int argumentsIndex = commands.getFieldInfoIndex("arguments");
/* 386 */     int argumentTablesIndex = commands.getFieldInfoIndex("argumentTables");
/* 387 */     List actions = new ArrayList();
/* 388 */     ExecutionContext context = env.m_context;
/* 389 */     Properties props = env.m_properties;
/* 390 */     int flags = PathUtils.F_KEEP_UNKNOWN_VARS;
/* 391 */     for (commands.first(); commands.isRowPresent(); commands.next())
/*     */     {
/* 393 */       String action = commands.getStringValue(actionIndex);
/* 394 */       List arguments = new ArrayList();
/* 395 */       arguments.add(action);
/* 396 */       if (argumentsIndex >= 0)
/*     */       {
/* 398 */         String argumentsString = commands.getStringValue(argumentsIndex);
/* 399 */         if (argumentsString.length() > 0)
/*     */         {
/* 401 */           String[] argumentsArray = argumentsString.split(" ");
/* 402 */           for (int a = 0; a < argumentsArray.length; ++a)
/*     */           {
/* 404 */             String argument = argumentsArray[a];
/* 405 */             argument = PathUtils.substitutePathVariables(argument, props, null, flags, context);
/* 406 */             arguments.add(argument);
/*     */           }
/*     */         }
/*     */       }
/* 410 */       if (argumentTablesIndex >= 0)
/*     */       {
/* 412 */         String tablesString = commands.getStringValue(argumentTablesIndex);
/* 413 */         if (tablesString.length() > 0)
/*     */         {
/* 415 */           String[] tables = tablesString.split(",");
/* 416 */           for (int t = 0; t < tables.length; ++t)
/*     */           {
/* 418 */             String argTableName = tables[t];
/* 419 */             DataResultSet argTable = (DataResultSet)binder.getResultSet(argTableName);
/* 420 */             int argumentIndex = ResultSetUtils.getIndexMustExist(argTable, "argument");
/* 421 */             for (argTable.first(); argTable.isRowPresent(); argTable.next())
/*     */             {
/* 423 */               String argument = argTable.getStringValue(argumentIndex);
/* 424 */               argument = PathUtils.substitutePathVariables(argument, props, null, flags, context);
/* 425 */               arguments.add(argument);
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/* 430 */       actions.add(arguments);
/*     */     }
/*     */ 
/* 433 */     return actions;
/*     */   }
/*     */ 
/*     */   public void prepareShiphome(boolean shouldIncludeGeneratedFiles)
/*     */     throws IdcException
/*     */   {
/* 439 */     BuildManager manager = this.m_manager;
/* 440 */     BuildEnvironment env = manager.m_env;
/* 441 */     boolean didInstalledComponentsChange = env.m_isServerGenerationNeeded;
/*     */ 
/* 444 */     if ((shouldIncludeGeneratedFiles) && (didInstalledComponentsChange))
/*     */     {
/* 446 */       List actions = computeActionsFromTable("MakeServerGeneratedFilesActions");
/* 447 */       for (List action : actions)
/*     */       {
/* 449 */         String actionName = (String)action.get(0);
/* 450 */         if (actionName.startsWith("IdcShell"))
/*     */         {
/* 452 */           handleIdcShell(action);
/*     */         }
/* 454 */         else if (actionName.equals("component"))
/*     */         {
/* 456 */           handleComponentAction(action);
/*     */         }
/*     */         else
/*     */         {
/* 460 */           throw new ServiceException(new StringBuilder().append("unknown action: ").append(actionName).toString());
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 465 */     super.prepareShiphome(shouldIncludeGeneratedFiles);
/*     */   }
/*     */ 
/*     */   public void handleIdcShell(List<String> action) throws IdcException
/*     */   {
/* 470 */     BuildManager manager = this.m_manager;
/* 471 */     BuildEnvironment env = manager.m_env;
/* 472 */     StringBuilder sb = new StringBuilder();
/*     */ 
/* 474 */     String actionName = (String)action.get(0);
/* 475 */     String[] actionParams = actionName.split("\\s");
/* 476 */     if ((actionParams.length != 3) || (!actionParams[1].equals("for")))
/*     */     {
/* 478 */       throw new ServiceException(new StringBuilder().append("bad IdcShell action: ").append(actionName).toString());
/*     */     }
/* 480 */     String productName = actionParams[2];
/* 481 */     manager.writeSystemConfigForProduct(productName);
/* 482 */     sb.append("--set-IdcProductName=");
/* 483 */     sb.append(productName);
/* 484 */     action.set(0, sb.toString());
/* 485 */     sb.setLength(0);
/* 486 */     sb.append("IdcShell");
/* 487 */     int numArgs = action.size();
/* 488 */     String[] args = new String[numArgs];
/* 489 */     for (int a = 0; a < numArgs; ++a)
/*     */     {
/* 491 */       String arg = args[a] =  = (String)action.get(a);
/* 492 */       sb.append(' ');
/* 493 */       sb.append(arg);
/*     */     }
/* 495 */     env.m_trace.report(6, new Object[] { "Running: ", sb.toString() });
/*     */ 
/* 497 */     IdcClassLoaderWrapper loader = manager.checkInitServerForProduct(productName);
/* 498 */     long timeStart = System.currentTimeMillis();
/* 499 */     IdcShellWrapper shell = new IdcShellWrapper(loader);
/* 500 */     shell.init(null);
/* 501 */     int rc = shell.runShell(args);
/* 502 */     if (rc != 0)
/*     */     {
/* 504 */       throw new ServiceException(rc, "unable to run IdcShell");
/*     */     }
/* 506 */     long timeStop = System.currentTimeMillis();
/* 507 */     long ms = timeStop - timeStart;
/* 508 */     long timeInSeconds = (timeStop - timeStart) / 1000L;
/* 509 */     env.m_trace.report(6, new Object[] { new StringBuilder().append("finished IdcShell for productName ").append(productName).append(", took ").toString(), Long.valueOf(timeInSeconds), " Seconds" });
/*     */   }
/*     */ 
/*     */   public void handleComponentAction(List<String> action) throws IdcException
/*     */   {
/* 514 */     BuildManager manager = this.m_manager;
/* 515 */     BuildEnvironment env = manager.m_env;
/* 516 */     StringBuilder sb = new StringBuilder();
/*     */ 
/* 518 */     int actionLength = action.size();
/* 519 */     String[] args = new String[actionLength - 1];
/* 520 */     for (int a = 0; a < actionLength; ++a)
/*     */     {
/* 522 */       String arg = (String)action.get(a);
/* 523 */       if (a > 0)
/*     */       {
/* 525 */         sb.append(' ');
/* 526 */         args[(a - 1)] = arg;
/*     */       }
/* 528 */       sb.append(arg);
/*     */     }
/* 530 */     if (args.length < 1)
/*     */     {
/* 532 */       sb.insert(0, "missing component action: ");
/* 533 */       throw new DataException(sb.toString());
/*     */     }
/* 535 */     String enableOrDisableString = args[0];
/*     */     boolean isEnable;
/* 537 */     if (enableOrDisableString.equals("enable"))
/*     */     {
/* 539 */       isEnable = true;
/*     */     }
/*     */     else
/*     */     {
/*     */       boolean isEnable;
/* 541 */       if (enableOrDisableString.equals("disable"))
/*     */       {
/* 543 */         isEnable = false;
/*     */       }
/*     */       else
/*     */       {
/* 547 */         sb.insert(0, "unknown component action: ");
/* 548 */         throw new DataException(sb.toString());
/*     */       }
/*     */     }
/*     */     boolean isEnable;
/* 550 */     String enablingString = (isEnable) ? "enabling" : "disabling";
/* 551 */     List componentNames = new ArrayList();
/* 552 */     List productNames = new ArrayList();
/* 553 */     int a = 1;
/* 554 */     while (a < args.length)
/*     */     {
/* 556 */       String componentName = args[(a++)];
/* 557 */       if (componentName.equals("for"))
/*     */       {
/* 559 */         while (a < args.length)
/*     */         {
/* 561 */           String productName = args[(a++)];
/* 562 */           productNames.add(productName);
/*     */         }
/* 564 */         if (productNames.size() != 0)
/*     */           break;
/* 566 */         sb.insert(0, "missing product name(s) after \"for\" in: ");
/* 567 */         throw new DataException(sb.toString());
/*     */       }
/*     */ 
/* 571 */       componentNames.add(componentName);
/*     */     }
/* 573 */     if (componentNames.size() == 0)
/*     */     {
/* 575 */       sb.insert(0, "missing component name(s): ");
/* 576 */       throw new DataException(sb.toString());
/*     */     }
/* 578 */     String componentNamesString = StringUtils.createStringSimple(componentNames);
/* 579 */     if (productNames.size() == 0)
/*     */     {
/* 581 */       String[] names = env.m_productNames;
/* 582 */       for (int p = 0; p < names.length; ++p)
/*     */       {
/* 584 */         productNames.add(names[p]);
/*     */       }
/*     */     }
/*     */ 
/* 588 */     GenericTracingCallback trace = env.m_trace;
/* 589 */     for (String productName : productNames)
/*     */     {
/* 591 */       IdcClassLoaderWrapper loader = manager.checkInitServerForProduct(productName);
/* 592 */       trace.report(6, new Object[] { enablingString, " component(s) ", componentNamesString, " for ", productName });
/* 593 */       long timeStart = System.currentTimeMillis();
/* 594 */       ComponentListWrapper list = new ComponentListWrapper(loader);
/* 595 */       list.init(null, productName);
/* 596 */       sb.setLength(0);
/* 597 */       for (String componentName : componentNames)
/*     */       {
/* 599 */         if (list.isComponentInstalled(componentName))
/*     */         {
/* 601 */           if (sb.length() > 0)
/*     */           {
/* 603 */             sb.append(',');
/*     */           }
/* 605 */           sb.append(componentName);
/*     */         }
/*     */       }
/* 608 */       if (sb.length() == 0) {
/*     */         continue;
/*     */       }
/*     */ 
/* 612 */       list.enableOrDisableComponent(sb.toString(), isEnable);
/* 613 */       long timeStop = System.currentTimeMillis();
/* 614 */       long ms = timeStop - timeStart;
/* 615 */       env.m_trace.report(6, new Object[] { "finished ", enablingString, " components, took ", Long.valueOf(ms), " ms" });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 623 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105370 $";
/*     */   }
/*     */ 
/*     */   public class CoreJavaPackageFilter
/*     */   {
/*     */     public String m_packagePrefix;
/*     */     public String m_pathPrefix;
/*     */     public List<String> m_matchingFiles;
/*     */ 
/*     */     public CoreJavaPackageFilter()
/*     */     {
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.tools.build.CoreModule
 * JD-Core Version:    0.5.4
 */