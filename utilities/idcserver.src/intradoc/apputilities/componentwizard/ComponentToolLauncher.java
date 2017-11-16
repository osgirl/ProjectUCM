/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.apputilities.installer.ConsolePromptUser;
/*     */ import intradoc.apputilities.installer.InstallComponents;
/*     */ import intradoc.common.ClassHelper;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.IdcSystemLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.utils.ComponentInstaller;
/*     */ import intradoc.server.utils.ComponentListEditor;
/*     */ import intradoc.server.utils.ComponentListManager;
/*     */ import intradoc.server.utils.ComponentPreferenceData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.tools.build.BuildUtils;
/*     */ import intradoc.tools.build.ComponentPackager;
/*     */ import intradoc.util.IdcException;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.zip.ZipFunctions;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.PrintStream;
/*     */ import java.io.Reader;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import java.util.zip.ZipEntry;
/*     */ import java.util.zip.ZipFile;
/*     */ 
/*     */ public class ComponentToolLauncher
/*     */ {
/*     */   public static final int F_CHECK_ONLY = 1;
/*     */   public static final int F_NO_DASHES = 2;
/*     */   public static final int F_ENABLE = 4;
/*     */   public static final int F_DISABLE_SAFE_MODE = 8;
/*     */   public static final int F_STRICT = 16;
/*     */   public static final int F_PRUNE = 32;
/*     */   public DataBinder m_prefData;
/*     */   public boolean m_hasPrefFile;
/*     */   public boolean m_verbose;
/*     */ 
/*     */   public ComponentToolLauncher()
/*     */   {
/*  88 */     this.m_prefData = new DataBinder();
/*  89 */     this.m_hasPrefFile = false;
/*  90 */     this.m_verbose = false;
/*     */   }
/*     */ 
/*     */   public int launch(String[] args)
/*     */   {
/*     */     try {
/*  96 */       for (String arg : args)
/*     */       {
/*  98 */         if (!arg.equals("-v"))
/*     */           continue;
/* 100 */         this.m_verbose = true;
/*     */       }
/*     */ 
/* 103 */       init();
/* 104 */       String failedArg = processArguments(args, 1);
/* 105 */       if (failedArg != null)
/*     */       {
/* 107 */         usage(failedArg);
/* 108 */         return 1;
/*     */       }
/* 110 */       processArguments(args, 0);
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 114 */       if (SystemUtils.isActiveTrace("comptool"))
/*     */       {
/* 116 */         this.m_verbose = true;
/*     */       }
/* 118 */       if (this.m_verbose)
/*     */       {
/* 120 */         System.out.println("");
/* 121 */         t.printStackTrace();
/*     */       }
/* 123 */       System.out.println("");
/* 124 */       IdcMessage msg = new IdcMessage(t);
/* 125 */       System.out.println(LocaleResources.localizeMessage(null, msg, null));
/* 126 */       return 1;
/*     */     }
/* 128 */     return 0;
/*     */   }
/*     */ 
/*     */   public String processArguments(String[] args, int flags) throws IdcException
/*     */   {
/* 133 */     boolean doOperations = (flags & 0x1) == 0;
/* 134 */     String lastOperation = "install";
/* 135 */     boolean didWork = false;
/* 136 */     for (int i = 0; i < args.length; ++i)
/*     */     {
/* 138 */       String arg = args[i];
/* 139 */       boolean isFlag = (flags & 0x2) != 0;
/* 140 */       boolean isDouble = false;
/* 141 */       if (!isFlag)
/*     */       {
/* 144 */         if (arg.startsWith("-"))
/*     */         {
/* 146 */           arg = arg.substring(1);
/* 147 */           isFlag = true;
/*     */         }
/* 149 */         if (arg.startsWith("-"))
/*     */         {
/* 151 */           arg = arg.substring(1);
/* 152 */           isDouble = true;
/*     */         }
/*     */       }
/* 155 */       String nextArg = null;
/* 156 */       if (i + 1 < args.length)
/*     */       {
/* 158 */         nextArg = args[(i + 1)];
/*     */       }
/*     */ 
/* 161 */       Report.debug("comptool", "doOperations: " + doOperations + " isFlag: " + isFlag + " isDoubleFlag: " + isDouble + " arg: " + arg + " nextArg: " + nextArg, null);
/*     */ 
/* 164 */       if ((isFlag) && (arg.equals("help")))
/*     */       {
/* 166 */         return "";
/*     */       }
/* 168 */       if ((isFlag) && (!isDouble) && (((arg.equals("v")) || (arg.equals("vv")))))
/*     */       {
/* 170 */         SystemUtils.addAsActiveTrace("comptool");
/* 171 */         SystemUtils.addAsActiveTrace("componentinstaller");
/* 172 */         SystemUtils.addAsActiveTrace("system");
/* 173 */         Report.trace("comptool", "enabling verbose", null);
/* 174 */         if (!arg.equals("vv"))
/*     */           continue;
/* 176 */         SystemUtils.m_verbose = true;
/*     */       }
/* 179 */       else if ((isFlag) && (!isDouble) && (arg.equals("t")) && (nextArg != null))
/*     */       {
/* 181 */         ++i;
/* 182 */         SystemUtils.addAsActiveTrace(nextArg);
/*     */       } else {
/* 184 */         if ((isFlag) && (!isDouble) && (arg.equals("console"))) {
/*     */           continue;
/*     */         }
/*     */ 
/* 188 */         if ((isFlag) && (arg.equals("interact")))
/*     */         {
/* 190 */           didWork = true;
/* 191 */           if (!doOperations) {
/*     */             continue;
/*     */           }
/*     */ 
/* 195 */           BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
/*     */           while (true)
/*     */           {
/* 199 */             String line = null;
/*     */             try
/*     */             {
/* 202 */               System.out.print("comptool> ");
/* 203 */               line = in.readLine();
/*     */             }
/*     */             catch (IOException ignore)
/*     */             {
/* 207 */               if (SystemUtils.m_verbose)
/*     */               {
/* 209 */                 Report.debug(null, "IOException reading from console", ignore);
/*     */               }
/*     */             }
/* 212 */             if (line == null) break; if (line.trim().equals("exit")) {
/*     */               break;
/*     */             }
/*     */ 
/* 216 */             List parsedLine = StringUtils.makeListFromSequence(line, ' ', '_', 32);
/*     */ 
/* 218 */             String failedArg = processArguments((String[])(String[])parsedLine.toArray(new String[parsedLine.size()]), 2);
/*     */ 
/* 220 */             if (failedArg != null)
/*     */             {
/* 222 */               usage(failedArg);
/*     */             }
/*     */           }
/*     */         }
/* 226 */         else if ((isFlag) && (arg.equals("list-enabled")))
/*     */         {
/* 228 */           lastOperation = null;
/* 229 */           didWork = true;
/* 230 */           if (!doOperations)
/*     */             continue;
/* 232 */           listComponents(true, false);
/*     */         }
/* 235 */         else if ((isFlag) && (arg.equals("list-disabled")))
/*     */         {
/* 237 */           lastOperation = null;
/* 238 */           didWork = true;
/* 239 */           if (!doOperations)
/*     */             continue;
/* 241 */           listComponents(false, true);
/*     */         }
/* 244 */         else if ((isFlag) && (arg.equals("list")))
/*     */         {
/* 246 */           lastOperation = null;
/* 247 */           didWork = true;
/* 248 */           if (!doOperations)
/*     */             continue;
/* 250 */           listComponents(true, true);
/*     */         }
/* 253 */         else if ((isFlag) && (nextArg != null) && (((arg.equals("install")) || (arg.equals("enable")) || (arg.equals("disable")) || (arg.equals("package")) || (arg.equals("check-depends")) || (arg.equals("fetch-depends")))))
/*     */         {
/* 257 */           lastOperation = arg;
/* 258 */           didWork = true;
/*     */         }
/* 260 */         else if ((isFlag) && (arg.equals("sync")))
/*     */         {
/* 262 */           lastOperation = null;
/* 263 */           didWork = true;
/* 264 */           if (!doOperations)
/*     */             continue;
/* 266 */           syncData();
/*     */         }
/* 269 */         else if ((isFlag) && (arg.equals("preferences")) && (nextArg != null))
/*     */         {
/* 271 */           ++i;
/* 272 */           lastOperation = arg;
/* 273 */           String[] files = expandPath(nextArg);
/* 274 */           for (int j = 0; j < files.length; ++j)
/*     */           {
/* 276 */             DataBinder tempPrefs = ResourceUtils.readDataBinderFromPath(files[j]);
/* 277 */             if ((tempPrefs.m_resultSets.size() == 0) && (tempPrefs.m_localData.size() == 0))
/*     */             {
/* 280 */               IdcMessage msg = new IdcMessage("csCompWizPreferencesEmpty", new Object[] { files[j] });
/* 281 */               System.out.println(LocaleResources.localizeMessage(null, msg, null));
/*     */             }
/* 283 */             this.m_prefData.merge(tempPrefs);
/* 284 */             this.m_hasPrefFile = true;
/*     */           }
/*     */         }
/* 287 */         else if ((((!isFlag) || ((flags & 0x2) != 0))) && (lastOperation != null))
/*     */         {
/* 289 */           didWork = true;
/* 290 */           if (!doOperations)
/*     */             continue;
/* 292 */           if (lastOperation.equals("enable"))
/*     */           {
/* 294 */             enableComponent(arg);
/*     */           }
/* 296 */           else if (lastOperation.equals("disable"))
/*     */           {
/* 298 */             disableComponent(arg);
/*     */           }
/* 300 */           else if (lastOperation.equals("check-depends"))
/*     */           {
/* 302 */             updateComponentDependencies(arg, false);
/*     */           }
/* 304 */           else if (lastOperation.equals("fetch-depends"))
/*     */           {
/* 306 */             updateComponentDependencies(arg, true);
/*     */           }
/* 308 */           else if (lastOperation.equals("package"))
/*     */           {
/* 310 */             packageComponent(arg);
/*     */           } else {
/* 312 */             if (!lastOperation.equals("install"))
/*     */               continue;
/* 314 */             String[] files = expandPath(arg);
/* 315 */             for (int j = 0; j < files.length; ++j)
/*     */             {
/* 317 */               installComponent(files[j], 4);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 324 */           return arg;
/*     */         }
/*     */       }
/*     */     }
/* 327 */     return (didWork) ? null : "";
/*     */   }
/*     */ 
/*     */   public static void usage(String failedArg)
/*     */   {
/* 332 */     if (failedArg.length() > 0)
/*     */     {
/* 334 */       SystemUtils.outln("Unknown argument: " + failedArg);
/*     */     }
/* 336 */     SystemUtils.out("Usage: ComponentTool [-v|-vv] [-t trace_section ] ... --enable|--disable name\n       ComponentTool [-v|-vv] [-t trace_section ] ... --sync\n       ComponentTool [-v|-vv] [-t trace_section ] ... [ --install ] path.hda|path.zip [ --preferences path.hda ]\n       ComponentTool [-v|-vv] [-t trace_section ] ... --check-depends name\n       ComponentTool [-v|-vv] [-t trace_section ] ... --package name\n       ComponentTool [-v|-vv] [-t trace_section ] ... --list-enabled|--list-disabled|--list|--interact|--help\n");
/*     */   }
/*     */ 
/*     */   public static String[] expandPath(String path)
/*     */     throws ServiceException
/*     */   {
/* 348 */     String dir = FileUtils.getDirectory(path);
/* 349 */     String fileName = FileUtils.getName(path);
/* 350 */     String[] matches = FileUtils.getMatchingFileNames(dir, fileName);
/* 351 */     if ((matches == null) || (matches.length == 0))
/*     */     {
/* 353 */       throw new ServiceException(null, -16, "syFileUtilsFileNotFound", new Object[] { path });
/*     */     }
/*     */ 
/* 356 */     for (int i = 0; i < matches.length; ++i)
/*     */     {
/* 358 */       matches[i] = FileUtils.getAbsolutePath(dir, matches[i]);
/*     */     }
/* 360 */     return matches;
/*     */   }
/*     */ 
/*     */   public static void listComponents(boolean listEnabled, boolean listDisabled)
/*     */     throws DataException, ServiceException
/*     */   {
/* 366 */     ComponentListManager.init();
/* 367 */     ComponentListEditor editor = ComponentListManager.getEditor();
/*     */ 
/* 369 */     DataResultSet set = null;
/* 370 */     if ((listEnabled) && (!listDisabled))
/*     */     {
/* 372 */       set = editor.getEnabledComponentList();
/*     */     }
/* 374 */     else if ((!listEnabled) && (listDisabled))
/*     */     {
/* 376 */       set = editor.getDisabledComponentList();
/*     */     }
/* 378 */     else if ((listEnabled) && (listDisabled))
/*     */     {
/* 380 */       set = editor.getComponentSet();
/*     */     }
/* 382 */     if (set == null)
/*     */     {
/* 384 */       return;
/*     */     }
/*     */ 
/* 387 */     for (set.first(); set.isRowPresent(); set.next())
/*     */     {
/* 389 */       Properties props = set.getCurrentRowProps();
/* 390 */       StringBuffer buf = new StringBuffer();
/* 391 */       buf.append(props.getProperty("name"));
/* 392 */       buf.append('\t');
/* 393 */       buf.append(props.getProperty("location"));
/* 394 */       SystemUtils.outln(buf.toString());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void init() throws DataException, ServiceException
/*     */   {
/* 400 */     IdcSystemConfig.loadInitialConfig();
/* 401 */     SharedObjects.putEnvironmentValue("IgnoreComponentLoadError", "true");
/* 402 */     SharedObjects.putEnvironmentValue("TolerateLocalizationFailure", "true");
/* 403 */     IdcSystemConfig.loadAppConfigInfo();
/* 404 */     IdcSystemConfig.initLocalization(IdcSystemConfig.F_STANDARD_SERVER);
/* 405 */     IdcSystemConfig.configLocalization();
/* 406 */     IdcSystemLoader.initLogInfo();
/* 407 */     intradoc.server.ComponentLoader.m_quiet = true;
/* 408 */     IdcSystemLoader.initComponentData();
/* 409 */     IdcSystemLoader.loadComponentDataEx(false);
/*     */     try
/*     */     {
/* 413 */       ComponentWizardManager.initFullEnvironment(false);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 417 */       Report.trace("comptool", e, "csIDCCommandStandaloneInitError", new Object[0]);
/* 418 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 419 */       String text = LocaleResources.localizeMessage(null, msg, null).toString();
/* 420 */       System.err.println(text);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void syncData()
/*     */     throws DataException, ServiceException
/*     */   {
/* 427 */     Report.trace("comptool", "synchronizing data", null);
/* 428 */     ComponentListManager.init();
/* 429 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 430 */     editor.init(false);
/*     */   }
/*     */ 
/*     */   public static void enableComponent(String name)
/*     */     throws DataException, ServiceException
/*     */   {
/* 436 */     Report.trace("comptool", "enabling " + name, null);
/* 437 */     ComponentListManager.init();
/* 438 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 439 */     editor.enableOrDisableComponent(name, true);
/*     */   }
/*     */ 
/*     */   public static void disableComponent(String name)
/*     */     throws DataException, ServiceException
/*     */   {
/* 445 */     Report.trace("comptool", "disabling " + name, null);
/* 446 */     ComponentListManager.init();
/* 447 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 448 */     editor.enableOrDisableComponent(name, false);
/*     */   }
/*     */ 
/*     */   public void updateComponentDependencies(String name, boolean forceFetch) throws IdcException
/*     */   {
/* 453 */     ComponentListManager.init();
/* 454 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 455 */     if (name.equals("all"))
/*     */     {
/* 457 */       DataResultSet components = editor.getComponentSet();
/* 458 */       int index = components.getFieldInfoIndex("name");
/* 459 */       for (components.first(); components.isRowPresent(); components.next())
/*     */       {
/* 461 */         String componentName = components.getStringValue(index);
/* 462 */         updateComponentDependencies(componentName, forceFetch);
/*     */       }
/* 464 */       return;
/*     */     }
/*     */     String dir;
/* 467 */     if (name.contains("/"))
/*     */     {
/* 469 */       String dir = name;
/* 470 */       name = FileUtils.getName(name);
/*     */     }
/*     */     else
/*     */     {
/* 474 */       DataBinder componentBinder = editor.getComponentData(name);
/* 475 */       if (componentBinder == null)
/*     */       {
/* 477 */         throw new ServiceException(null, "csComponentMissingFromListing", new Object[] { name });
/*     */       }
/* 479 */       dir = componentBinder.getLocal("ComponentDir");
/*     */     }
/* 481 */     Report.trace("comptool", "updating dependencies for " + name, null);
/* 482 */     int flags = (forceFetch) ? 1 : 0;
/* 483 */     BuildUtils.updateDependencies(dir, flags, null);
/*     */   }
/*     */ 
/*     */   public void packageComponent(String name) throws IdcException
/*     */   {
/* 488 */     ComponentListManager.init();
/* 489 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 490 */     if (name.equals("all"))
/*     */     {
/* 492 */       DataResultSet components = editor.getComponentSet();
/* 493 */       int index = components.getFieldInfoIndex("name");
/* 494 */       for (components.first(); components.isRowPresent(); components.next())
/*     */       {
/* 496 */         String componentName = components.getStringValue(index);
/* 497 */         packageComponent(componentName);
/*     */       }
/* 499 */       return;
/*     */     }
/* 501 */     DataBinder componentBinder = editor.getComponentData(name);
/* 502 */     if (componentBinder == null)
/*     */     {
/* 504 */       throw new ServiceException(null, "csComponentMissingFromListing", new Object[] { name });
/*     */     }
/* 506 */     ComponentPackager packager = new ComponentPackager();
/* 507 */     String componentDirname = componentBinder.getLocal("ComponentDir");
/* 508 */     File componentDir = new File(componentDirname);
/* 509 */     packager.init(componentDir);
/* 510 */     packager.packageComponent(null);
/*     */   }
/*     */ 
/*     */   public void installComponent(String path, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 516 */     Report.trace("comptool", "installing " + path, null);
/* 517 */     IntervalData interval = new IntervalData("component install ");
/* 518 */     if (FileUtils.checkFile(path, true, false) == 0)
/*     */     {
/* 520 */       if (path.endsWith(".hda"))
/*     */       {
/* 522 */         addComponentRegistration(path, flags);
/*     */       }
/*     */       else
/*     */       {
/* 526 */         installComponentZip(path, flags);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 531 */       addComponentRegistration(path, flags);
/*     */     }
/*     */ 
/* 534 */     interval.stop();
/* 535 */     interval.trace("comptool", "component install ");
/*     */   }
/*     */ 
/*     */   public static void addComponentRegistration(String path, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 541 */     ComponentListManager.init();
/* 542 */     ComponentListEditor editor = ComponentListManager.getEditor();
/* 543 */     DataBinder binder = ResourceUtils.readDataBinderFromPath(path);
/* 544 */     Properties props = new Properties();
/* 545 */     String name = binder.get("ComponentName");
/* 546 */     props.put("name", name);
/* 547 */     props.put("location", path);
/* 548 */     props.put("status", "Disabled");
/* 549 */     editor.addComponent(props, null);
/* 550 */     editor.loadComponents();
/* 551 */     editor.enableOrDisableComponent(name, (flags & 0x4) != 0);
/*     */   }
/*     */ 
/*     */   public void installComponentZip(String path, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 557 */     DataBinder manifest = ZipFunctions.extractFileAsDataBinder(path, "manifest.hda");
/*     */ 
/* 560 */     Map args = new HashMap();
/* 561 */     args.put("Install", "true");
/* 562 */     args.put("ZipName", path);
/* 563 */     if ((flags & 0x10) != 0)
/*     */     {
/* 565 */       args.put("Strict", "true");
/*     */     }
/* 567 */     if ((flags & 0x20) != 0)
/*     */     {
/* 569 */       args.put("PruneFiles", "true");
/*     */     }
/*     */ 
/* 572 */     DataResultSet drset = (DataResultSet)manifest.getResultSet("Manifest");
/*     */ 
/* 574 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 576 */       Properties props = drset.getCurrentRowProps();
/* 577 */       String type = props.getProperty("entryType");
/* 578 */       if (!type.equals("component"))
/*     */         continue;
/* 580 */       String location = props.getProperty("location");
/* 581 */       location = type + "/" + location;
/* 582 */       DataBinder compInfo = ZipFunctions.extractFileAsDataBinder(path, location);
/*     */ 
/* 586 */       String name = compInfo.getLocal("ComponentName");
/* 587 */       String installID = compInfo.getLocal("installID");
/* 588 */       if (installID == null)
/*     */       {
/* 590 */         installID = name;
/*     */       }
/*     */ 
/* 594 */       ResourceContainer rc = SharedObjects.getResources();
/* 595 */       DataResultSet resourceDefinition = new DataResultSet();
/* 596 */       resourceDefinition.copy(compInfo.getResultSet("ResourceDefinition"));
/* 597 */       ZipFile zip = null;
/*     */       try
/*     */       {
/* 600 */         DataResultSet tmp = drset.shallowClone();
/* 601 */         for (tmp.first(); tmp.isRowPresent(); tmp.next())
/*     */         {
/* 603 */           Properties manifestProps = tmp.getCurrentRowProps();
/* 604 */           String tmpType = manifestProps.getProperty("entryType");
/* 605 */           String tmpLocation = manifestProps.getProperty("location");
/* 606 */           if ((!tmpType.equals("componentExtra")) || (!tmpLocation.endsWith("/install_strings.htm"))) {
/*     */             continue;
/*     */           }
/*     */           try
/*     */           {
/* 611 */             FieldInfo[] infos = ResultSetUtils.createInfoList(resourceDefinition, new String[] { "type", "filename", "tables", "loadOrder" }, true);
/*     */ 
/* 614 */             Vector row = resourceDefinition.createEmptyRow();
/* 615 */             row.setElementAt("resource", infos[0].m_index);
/* 616 */             row.setElementAt("install_strings.htm", infos[1].m_index);
/* 617 */             row.setElementAt("", infos[2].m_index);
/* 618 */             row.setElementAt("0", infos[3].m_index);
/* 619 */             resourceDefinition.addRow(row);
/*     */           }
/*     */           catch (DataException ignore)
/*     */           {
/* 623 */             Report.trace("comptool", "unable to setup for install_strings.htm", ignore);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 628 */         Hashtable entries = new Hashtable();
/* 629 */         zip = ZipFunctions.readZipFile(path, entries);
/* 630 */         resourceDefinition.first();
/* 631 */         while (resourceDefinition.isRowPresent())
/*     */         {
/* 634 */           Properties resource = resourceDefinition.getCurrentRowProps();
/* 635 */           String resourceType = resource.getProperty("type");
/* 636 */           String fileName = resource.getProperty("filename");
/* 637 */           if (resourceType.equals("resource"))
/*     */           {
/* 641 */             fileName = "component/" + name + "/" + fileName;
/*     */ 
/* 644 */             ZipEntry entry = (ZipEntry)entries.get(fileName.toLowerCase());
/* 645 */             if (entry == null)
/*     */             {
/* 647 */               Report.trace("comptool", "Missing ZipEntry for resource file " + fileName, null);
/*     */             }
/*     */             else
/*     */             {
/* 651 */               BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
/*     */ 
/* 653 */               Reader r = FileUtils.openDataReader(in, null);
/*     */               try
/*     */               {
/* 656 */                 Report.trace("comptool", "reading resources from " + path + ":" + fileName, null);
/*     */ 
/* 658 */                 rc.parseAndAddResources(r, fileName);
/*     */               }
/*     */               catch (ParseSyntaxException e)
/*     */               {
/* 662 */                 Report.trace("comptool", "Unable to load localization from " + fileName, e);
/*     */               }
/*     */               finally
/*     */               {
/* 667 */                 FileUtils.closeObject(r);
/*     */               }
/*     */             }
/*     */           }
/* 632 */           resourceDefinition.next();
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 673 */         Report.trace("comptool", "Unable to load localization from " + path, e);
/*     */       }
/*     */       finally
/*     */       {
/* 677 */         FileUtils.closeObjects(zip, null);
/*     */       }
/* 679 */       LocaleResources.initStrings(rc);
/*     */ 
/* 681 */       ComponentInstaller installer = null;
/* 682 */       if (SharedObjects.getEnvValueAsBoolean("UseParallelInstaller", false))
/*     */       {
/* 684 */         ClassHelper helper = new ClassHelper();
/* 685 */         helper.init("intradoc.server.ParallelComponentInstaller");
/* 686 */         installer = (ComponentInstaller)helper.m_obj;
/*     */       }
/*     */       else
/*     */       {
/* 690 */         installer = new ComponentInstaller();
/*     */       }
/*     */ 
/* 696 */       if (StringUtils.convertToBool(compInfo.getLocal("hasPreferenceData"), false) == true)
/*     */       {
/* 699 */         if (this.m_hasPrefFile)
/*     */         {
/* 702 */           PageMerger pageMerger = new PageMerger(compInfo, null);
/* 703 */           DataResultSet prefs = ResultSetUtils.getMutableResultSet(this.m_prefData, "PreferenceData", false, false);
/*     */ 
/* 705 */           if (prefs != null)
/*     */           {
/* 707 */             prefs = ComponentPreferenceData.upgradePrefData(prefs);
/* 708 */             FieldInfo[] infos = ResultSetUtils.createInfoList(prefs, new String[] { "pValue" }, true);
/*     */ 
/* 710 */             for (prefs.first(); prefs.isRowPresent(); prefs.next())
/*     */             {
/*     */               try
/*     */               {
/* 714 */                 String idocVal = pageMerger.evaluateScript(prefs.getStringValue(infos[0].m_index));
/*     */ 
/* 716 */                 prefs.setCurrentValue(infos[0].m_index, idocVal);
/*     */               }
/*     */               catch (IOException ignore)
/*     */               {
/* 720 */                 Report.trace("comptool", null, ignore);
/*     */               }
/*     */             }
/* 723 */             InstallComponents.handlePreferenceData(compInfo, prefs);
/*     */           }
/* 725 */           compInfo.merge(this.m_prefData);
/*     */         }
/*     */         else
/*     */         {
/* 729 */           ComponentPreferenceData prefData = new ComponentPreferenceData();
/* 730 */           installer.retrievePreferenceData(prefData, path, name, installID);
/* 731 */           prefData.upgrade();
/* 732 */           promptUser(compInfo, prefData.getPreferenceTable());
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 737 */       String[] retStr = installer.retrieveComponentNameAndLocation(manifest);
/* 738 */       installer.m_disableSafeMode = ((flags & 0x8) != 0);
/* 739 */       installer.executeInstaller(compInfo, manifest, name, name, args);
/* 740 */       ComponentListEditor editor = ComponentListManager.getEditor();
/* 741 */       editor.enableOrDisableComponent(name, (flags & 0x4) > 0);
/* 742 */       editor.loadComponents();
/* 743 */       installer.doInstallExtra(compInfo, name, retStr[0], path, installID);
/*     */ 
/* 745 */       if ((flags & 0x4) == 0) {
/*     */         continue;
/*     */       }
/* 748 */       Vector installedComps = installer.getSucessfulComponents();
/* 749 */       for (String compName : installedComps)
/*     */       {
/* 751 */         enableComponent(compName);
/*     */       }
/* 753 */       installedComps.removeAllElements();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void promptUser(DataBinder compInfo, DataResultSet prefData)
/*     */     throws DataException
/*     */   {
/* 761 */     if (prefData.isEmpty())
/*     */       return;
/* 763 */     ConsolePromptUser userPrompt = new ConsolePromptUser();
/* 764 */     String width = System.getProperty("tty.columns");
/* 765 */     String height = System.getProperty("tty.rows");
/* 766 */     NativeOsUtils utils = null;
/*     */     try
/*     */     {
/* 769 */       utils = new NativeOsUtils();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 773 */       Report.trace("comptool", "Unable to instantiate NativeOsUtils", t);
/*     */     }
/* 775 */     if ((width == null) && (utils != null))
/*     */     {
/*     */       try
/*     */       {
/* 779 */         int[] d = new int[2];
/* 780 */         if ((utils.isScreenSizeSupported()) && (utils.getScreenSize(d) == 0))
/*     */         {
/* 783 */           width = "" + d[0];
/* 784 */           height = "" + d[1];
/*     */         }
/* 786 */         if (d[0] == 0)
/*     */         {
/* 788 */           width = utils.getEnv("IDC_TTY_COLUMNS");
/*     */         }
/* 790 */         if (d[1] == 0)
/*     */         {
/* 792 */           height = utils.getEnv("IDC_TTY_ROWS");
/*     */         }
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 797 */         Report.trace("comptool", "unable to compute screen size", t);
/*     */       }
/*     */     }
/*     */ 
/* 801 */     if (width != null)
/*     */     {
/* 803 */       int maxMsgLength = NumberUtils.parseInteger(width, -1);
/* 804 */       userPrompt.setLineLength(maxMsgLength);
/*     */     }
/* 806 */     if (height != null)
/*     */     {
/* 808 */       int screenHeight = NumberUtils.parseInteger(height, -1);
/* 809 */       userPrompt.setScreenHeight(screenHeight);
/*     */     }
/*     */ 
/* 812 */     FieldInfo[] infos = ResultSetUtils.createInfoList(prefData, ComponentPreferenceData.PREF_FIELD_INFO, true);
/*     */ 
/* 815 */     for (prefData.first(); prefData.isRowPresent(); prefData.next())
/*     */     {
/* 818 */       if (!prefData.getStringValue(infos[2].m_index).equalsIgnoreCase("prompt"))
/*     */         continue;
/* 820 */       String name = prefData.getStringValue(infos[0].m_index);
/* 821 */       String defVal = prefData.getStringValue(infos[6].m_index);
/* 822 */       String promptType = prefData.getStringValue(infos[3].m_index);
/* 823 */       String message = prefData.getStringValue(infos[1].m_index);
/* 824 */       if (message.indexOf("!") != 0)
/*     */       {
/* 826 */         Report.deprecatedUsage("converting bad message " + message);
/* 827 */         message = "!" + message;
/*     */       }
/* 829 */       String userVal = null;
/*     */ 
/* 832 */       if (promptType.equalsIgnoreCase("options"))
/*     */       {
/* 834 */         String optListName = prefData.getStringValue(infos[4].m_index);
/* 835 */         String optListCol = prefData.getStringValue(infos[5].m_index);
/* 836 */         DataResultSet drset = SharedObjects.getTable(optListName);
/* 837 */         FieldInfo[] optListInfo = ResultSetUtils.createInfoList(drset, new String[] { optListCol }, true);
/* 838 */         int numRows = drset.getNumRows();
/* 839 */         String[][] data = new String[numRows][];
/* 840 */         drset.first();
/* 841 */         for (int i = 0; i < numRows; ++i)
/*     */         {
/* 843 */           String val = drset.getStringValue(optListInfo[0].m_index);
/* 844 */           String[] row = { val, val };
/* 845 */           data[i] = row;
/* 846 */           drset.next();
/*     */         }
/* 848 */         userVal = userPrompt.prompt(1, name, defVal, data, message);
/*     */       }
/* 852 */       else if (promptType.equalsIgnoreCase("boolean"))
/*     */       {
/* 854 */         boolean defValAsBool = StringUtils.convertToBool(defVal, false);
/* 855 */         if (!defValAsBool)
/*     */         {
/* 857 */           defVal = "0";
/*     */         }
/*     */         else
/*     */         {
/* 861 */           defVal = "1";
/*     */         }
/* 863 */         String[][] data = TableFields.YESNO_OPTIONLIST;
/* 864 */         LocaleResources.localizeStaticDoubleArray(data, null, 1);
/* 865 */         userVal = userPrompt.prompt(1, name, defVal, data, message);
/*     */       }
/*     */       else
/*     */       {
/* 869 */         if (promptType.equalsIgnoreCase("integer"))
/*     */         {
/*     */           while (true)
/*     */           {
/* 873 */             userVal = userPrompt.prompt(0, name, defVal, null, message);
/*     */             try
/*     */             {
/* 876 */               Integer.parseInt(userVal);
/*     */             }
/*     */             catch (NumberFormatException ignore)
/*     */             {
/* 881 */               String msg = LocaleUtils.encodeMessage("csNumberInvalidCharacters", null, userVal);
/* 882 */               msg = LocaleResources.localizeMessage(msg, null);
/* 883 */               userPrompt.outputMessage(msg);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 891 */         userVal = userPrompt.prompt(0, name, defVal, null, message);
/*     */       }
/*     */ 
/* 895 */       compInfo.putLocal(name, userVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 904 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ComponentToolLauncher
 * JD-Core Version:    0.5.4
 */