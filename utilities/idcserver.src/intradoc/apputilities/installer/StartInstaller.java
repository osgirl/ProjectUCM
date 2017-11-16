/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.BufferPool;
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.Help;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Log;
/*      */ import intradoc.common.LogDirInfo;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ReportProgress;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StackTrace;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.common.TracerReportUtils;
/*      */ import intradoc.common.VersionInfo;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.serialize.DataBinderSerializer;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.IdcSystemConfig;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.shared.Features;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileWriter;
/*      */ import java.io.IOException;
/*      */ import java.io.PrintStream;
/*      */ import java.lang.reflect.Constructor;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class StartInstaller
/*      */   implements ReportProgress, IdcComparator
/*      */ {
/*      */   final String PROGRESS_FILE_OVERWRITE = "overwrite";
/*      */   final String PROGRESS_FILE_APPEND = "append";
/*      */   public int m_updateThrottle;
/*      */   public long m_lastUpdate;
/*      */   public String m_lastMessage;
/*      */   public String m_progressFile;
/*      */   public String m_progressUpdateBehavior;
/*      */   public String m_targetDir;
/*      */   public boolean m_quiet;
/*      */   public NativeOsUtils m_utils;
/*      */   public SysInstaller m_installer;
/*      */   public InstallLog m_log;
/*      */   public PromptUser m_promptUser;
/*      */   public int m_maxMsgLength;
/*      */   public int m_lastPercentReported;
/*      */   public boolean m_localizationLoaded;
/*      */   public boolean m_isPreinstallationCheck;
/*      */   public Properties m_overrideProps;
/*      */   public Properties m_stdinProps;
/*      */   public List<String> m_components;
/*      */   public String m_localeName;
/*      */   public String[] m_args;
/*      */ 
/*      */   public StartInstaller()
/*      */   {
/*   94 */     this.PROGRESS_FILE_OVERWRITE = "overwrite";
/*   95 */     this.PROGRESS_FILE_APPEND = "append";
/*      */ 
/*   97 */     this.m_updateThrottle = 300;
/*      */ 
/*   99 */     this.m_lastMessage = "";
/*      */ 
/*  101 */     this.m_progressUpdateBehavior = "overwrite";
/*      */ 
/*  103 */     this.m_quiet = false;
/*      */ 
/*  110 */     this.m_maxMsgLength = 78;
/*  111 */     this.m_lastPercentReported = 0;
/*      */ 
/*  113 */     this.m_localizationLoaded = false;
/*      */ 
/*  118 */     this.m_isPreinstallationCheck = false;
/*      */ 
/*  120 */     this.m_overrideProps = new Properties();
/*  121 */     this.m_stdinProps = null;
/*      */ 
/*  123 */     this.m_components = new ArrayList();
/*      */ 
/*  125 */     this.m_localeName = null;
/*      */   }
/*      */ 
/*      */   public int main(String[] args)
/*      */   {
/*  132 */     this.m_args = args;
/*      */ 
/*  135 */     SharedObjects.init();
/*      */ 
/*  145 */     bootstrapL10N();
/*      */ 
/*  147 */     String versionLabel = VersionInfo.getProductVersion();
/*  148 */     String versionNumber = VersionInfo.getProductVersionInfo();
/*  149 */     String buildInfo = VersionInfo.getProductBuildInfo();
/*      */ 
/*  153 */     String promptUserName = System.getProperty("prompt.class");
/*  154 */     if (promptUserName == null)
/*      */     {
/*  156 */       promptUserName = "ConsolePromptUser";
/*      */     }
/*  158 */     if (promptUserName.indexOf(".") == -1)
/*      */     {
/*  160 */       promptUserName = "intradoc.apputilities.installer." + promptUserName;
/*      */     }
/*      */     try
/*      */     {
/*  164 */       Class promptUserClass = Class.forName(promptUserName);
/*  165 */       PromptUser promptUser = (PromptUser)promptUserClass.newInstance();
/*  166 */       this.m_promptUser = promptUser;
/*      */     }
/*      */     catch (ClassNotFoundException e)
/*      */     {
/*  170 */       e.printStackTrace();
/*  171 */       this.m_promptUser = new ConsolePromptUser();
/*      */     }
/*      */     catch (InstantiationException e)
/*      */     {
/*  175 */       e.printStackTrace();
/*  176 */       this.m_promptUser = new ConsolePromptUser();
/*      */     }
/*      */     catch (IllegalAccessException e)
/*      */     {
/*  180 */       e.printStackTrace();
/*  181 */       this.m_promptUser = new ConsolePromptUser();
/*      */     }
/*  183 */     int rc = 0;
/*      */ 
/*  185 */     String progressFile = null;
/*  186 */     intradoc.common.IdcDateFormat.m_defaultUseSTZ = true;
/*      */ 
/*  188 */     bootstrapLogging();
/*  189 */     boolean removeWaitFile = false;
/*      */     try
/*      */     {
/*      */       try
/*      */       {
/*  194 */         this.m_utils = new NativeOsUtils();
/*      */       }
/*      */       catch (Throwable ignore)
/*      */       {
/*  198 */         if (SystemUtils.m_verbose)
/*      */         {
/*  200 */           Report.debug("install", null, ignore);
/*      */         }
/*      */       }
/*      */ 
/*  204 */       String width = System.getProperty("tty.columns");
/*  205 */       String height = System.getProperty("tty.rows");
/*  206 */       if ((width == null) && (this.m_utils != null))
/*      */       {
/*      */         try
/*      */         {
/*  210 */           int[] d = new int[2];
/*  211 */           if ((this.m_utils.isScreenSizeSupported()) && (this.m_utils.getScreenSize(d) == 0))
/*      */           {
/*  214 */             width = "" + d[0];
/*  215 */             height = "" + d[1];
/*      */           }
/*  217 */           if (d[0] == 0)
/*      */           {
/*  219 */             width = this.m_utils.getEnv("IDC_TTY_COLUMNS");
/*      */           }
/*  221 */           if (d[1] == 0)
/*      */           {
/*  223 */             height = this.m_utils.getEnv("IDC_TTY_ROWS");
/*      */           }
/*      */         }
/*      */         catch (Throwable t)
/*      */         {
/*  228 */           Report.trace("install", "unable to compute screen size", t);
/*      */         }
/*      */       }
/*      */ 
/*  232 */       if (width != null)
/*      */       {
/*  234 */         int maxMsgLength = NumberUtils.parseInteger(width, -1);
/*  235 */         this.m_promptUser.setLineLength(maxMsgLength);
/*  236 */         this.m_maxMsgLength = (maxMsgLength - 2);
/*      */       }
/*  238 */       if (height != null)
/*      */       {
/*  240 */         int screenHeight = NumberUtils.parseInteger(height, -1);
/*  241 */         this.m_promptUser.setScreenHeight(screenHeight);
/*      */       }
/*  243 */       int argc = args.length;
/*  244 */       int[] argOffset = { 0 };
/*  245 */       boolean didWork = false;
/*  246 */       while (argOffset[0] < argc)
/*      */       {
/*      */         int tmp443_442 = 0;
/*      */         int[] tmp443_440 = argOffset;
/*      */         int tmp445_444 = tmp443_440[tmp443_442]; tmp443_440[tmp443_442] = (tmp445_444 + 1); String arg = args[tmp445_444];
/*  249 */         if (arg.endsWith("-get-registry-value"))
/*      */         {
/*  251 */           NativeOsUtils utils = new NativeOsUtils();
/*  252 */           didWork = true;
/*  253 */           while (argOffset[0] < argc)
/*      */           {
/*      */             int tmp487_486 = 0;
/*      */             int[] tmp487_484 = argOffset;
/*      */             int tmp489_488 = tmp487_484[tmp487_486]; tmp487_484[tmp487_486] = (tmp489_488 + 1); String key = args[tmp489_488];
/*  256 */             String value = utils.getRegistryValue(key);
/*  257 */             label3303: SystemUtils.outln(key + "=" + value);
/*      */           }
/*      */         } else {
/*  260 */           if (arg.endsWith("-report-properties"))
/*      */           {
/*  262 */             if (argOffset[0] == argc)
/*      */             {
/*  264 */               props = System.getProperties();
/*  265 */               Enumeration en = props.keys();
/*  266 */               while (en.hasMoreElements())
/*      */               {
/*  268 */                 String key = (String)en.nextElement();
/*  269 */                 SystemUtils.outln(key + "=" + props.getProperty(key));
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/*  275 */               while (argOffset[0] < argc)
/*      */               {
/*      */                 int tmp644_643 = 0;
/*      */                 int[] tmp644_641 = argOffset;
/*      */                 int tmp646_645 = tmp644_641[tmp644_643]; tmp644_641[tmp644_643] = (tmp646_645 + 1); arg = args[tmp646_645];
/*  278 */                 SystemUtils.outln(arg + "=" + System.getProperty(arg));
/*  279 */                 didWork = true;
/*      */               }
/*      */             }
/*  282 */             Properties props = 0;
/*      */             String msg;
/*      */             return props;
/*      */           }
/*      */           String value;
/*  284 */           if (arg.endsWith("-version"))
/*      */           {
/*  286 */             while (argOffset[0] < argc)
/*      */             {
/*  288 */               didWork = true;
/*      */               int tmp809_808 = 0;
/*      */               int[] tmp809_806 = argOffset;
/*      */               int tmp811_810 = tmp809_806[tmp809_808]; tmp809_806[tmp809_808] = (tmp811_810 + 1); arg = args[tmp811_810];
/*      */               try
/*      */               {
/*  293 */                 Class cl = Class.forName(arg);
/*  294 */                 value = ClassHelperUtils.executeStaticMethodSuppressException(cl, "idcVersionInfo", new Object[] { (Object)null }).toString();
/*      */               }
/*      */               catch (Exception e)
/*      */               {
/*  299 */                 Report.trace("install", "unable to get version info from " + arg, e);
/*      */ 
/*  301 */                 value = e.toString();
/*      */               }
/*  303 */               SystemUtils.outln(arg + "=" + value);
/*      */             }
/*  305 */             if (!didWork)
/*      */             {
/*  307 */               SystemUtils.outln("Version Label: \t" + versionLabel);
/*  308 */               SystemUtils.outln("Version Number:\t" + versionNumber);
/*  309 */               if (SystemUtils.m_isDevelopmentEnvironment)
/*      */               {
/*  311 */                 SystemUtils.outln("Version Info:\t" + idcVersionInfo(null));
/*      */               }
/*      */             }
/*  314 */             value = 0;
/*      */             String msg;
/*      */             return value;
/*      */           }
/*  316 */           if (arg.endsWith("-version-label"))
/*      */           {
/*  318 */             SystemUtils.outln(versionLabel);
/*  319 */             value = 0;
/*      */             String msg;
/*      */             return value;
/*      */           }
/*  321 */           if (arg.endsWith("-version-number"))
/*      */           {
/*  323 */             SystemUtils.outln(versionNumber);
/*  324 */             value = 0;
/*      */             String msg;
/*      */             return value;
/*      */           }
/*  326 */           if (arg.endsWith("-version-info"))
/*      */           {
/*  328 */             SystemUtils.outln(idcVersionInfo(null).toString());
/*  329 */             value = 0;
/*      */             String msg;
/*      */             return value;
/*      */           }
/*      */           String[][] cfgs;
/*      */           String description;
/*  331 */           if (arg.endsWith("-list-configurations"))
/*      */           {
/*  333 */             doInstallInit(null);
/*  334 */             cfgs = this.m_installer.getInstallerTableAsArray("InstallConfigurations");
/*  335 */             for (int i = 0; i < cfgs.length; ++i)
/*      */             {
/*  337 */               String cfg = cfgs[i][0];
/*  338 */               description = "!csInstallConfigDescription_" + cfg;
/*  339 */               description = LocaleResources.getString("csInstallConfigDescription", null, cfg, description);
/*      */ 
/*  341 */               this.m_installer.m_promptUser.outputMessage(description);
/*      */             }
/*  343 */             i = 0;
/*      */             String msg;
/*      */             return i;
/*      */           }
/*  345 */           if ((arg.endsWith("-help")) || (arg.equals("-h")))
/*      */           {
/*  347 */             cfgs = usage(null);
/*      */             String msg;
/*      */             return cfgs;
/*      */           }
/*      */           String traceArg;
/*  349 */           if ((arg.equals("-v")) || (arg.endsWith("-verbose")))
/*      */           {
/*  351 */             Report.m_verbose = true;
/*  352 */             SystemUtils.m_verbose = true;
/*  353 */             for (int i = 0; i < argc; ++i)
/*      */             {
/*  355 */               traceArg = "arg " + String.valueOf(i) + " = " + args[i];
/*  356 */               Report.debug(null, traceArg, null);
/*      */             }
/*      */           }
/*  359 */           else if ((arg.equals("-q")) || (arg.endsWith("-quiet")))
/*      */           {
/*  361 */             this.m_promptUser.setQuiet(true);
/*  362 */             this.m_quiet = true;
/*      */           }
/*      */           else
/*      */           {
/*      */             String msg;
/*  364 */             if (arg.equals("-t"))
/*      */             {
/*  366 */               if (argOffset[0] >= argc)
/*      */               {
/*  368 */                 msg = LocaleUtils.encodeMessage("syOptionRequiresValue", null, "-t");
/*      */ 
/*  370 */                 traceArg = usage(msg);
/*      */                 String msg;
/*      */                 return traceArg;
/*      */               }
/*      */               int tmp1990_1989 = 0;
/*      */               int[] tmp1990_1987 = argOffset;
/*      */               int tmp1992_1991 = tmp1990_1987[tmp1990_1989]; tmp1990_1987[tmp1990_1989] = (tmp1992_1991 + 1); arg = args[tmp1992_1991];
/*  373 */               SystemUtils.addAsActiveTrace(arg);
/*      */             } else {
/*  375 */               if (arg.endsWith("-report-version"))
/*      */               {
/*  377 */                 SystemUtils.outln(versionNumber);
/*  378 */                 msg = 0;
/*      */                 String msg;
/*      */                 return msg;
/*      */               }
/*  380 */               if (arg.endsWith("-report-version-label"))
/*      */               {
/*  382 */                 SystemUtils.outln(versionLabel);
/*  383 */                 msg = 0;
/*      */                 String msg;
/*      */                 return msg;
/*      */               }
/*      */               String msg;
/*  385 */               if ((arg.equals("--set")) || (arg.equals("-set")))
/*      */               {
/*  389 */                 if ((argOffset[0] >= argc) || (args[argOffset[0]].charAt(0) == '-'))
/*      */                 {
/*  391 */                   String msg = LocaleUtils.encodeMessage("syOptionRequiresValue", null, arg);
/*      */ 
/*  393 */                   description = usage(msg);
/*      */                   return description;
/*      */                 }
/*      */                 int tmp2377_2376 = 0;
/*      */                 int[] tmp2377_2374 = argOffset;
/*      */                 int tmp2379_2378 = tmp2377_2374[tmp2377_2376]; tmp2377_2374[tmp2377_2376] = (tmp2379_2378 + 1); arg = args[tmp2379_2378];
/*  396 */                 int equalsIndex = arg.indexOf("=");
/*      */                 String value;
/*      */                 String name;
/*      */                 String value;
/*  397 */                 if (equalsIndex < 0)
/*      */                 {
/*  399 */                   if (argOffset[0] >= argc)
/*      */                   {
/*  401 */                     String msg = LocaleUtils.encodeMessage("syOptionRequiresValue", null, arg);
/*      */ 
/*  403 */                     msg = usage(msg);
/*      */                     String msg;
/*      */                     return msg;
/*      */                   }
/*  405 */                   String name = arg;
/*      */                   int tmp2525_2524 = 0;
/*      */                   int[] tmp2525_2522 = argOffset;
/*      */                   int tmp2527_2526 = tmp2525_2522[tmp2525_2524]; tmp2525_2522[tmp2525_2524] = (tmp2527_2526 + 1); value = args[tmp2527_2526];
/*      */                 }
/*      */                 else
/*      */                 {
/*  410 */                   name = arg.substring(0, equalsIndex);
/*  411 */                   value = arg.substring(equalsIndex + 1);
/*      */                 }
/*  413 */                 this.m_overrideProps.put(name, value);
/*      */               }
/*      */               else
/*      */               {
/*      */                 String msg;
/*  415 */                 if ((arg.startsWith("--set-")) || (arg.startsWith("-set-")))
/*      */                 {
/*  419 */                   int equalsIndex = arg.indexOf("=");
/*      */                   String value;
/*      */                   String name;
/*      */                   String value;
/*  420 */                   if (equalsIndex < 0)
/*      */                   {
/*  422 */                     if (argOffset[0] >= argc)
/*      */                     {
/*  424 */                       msg = LocaleUtils.encodeMessage("syOptionRequiresValue", null, arg);
/*      */ 
/*  426 */                       msg = usage(msg);
/*      */                       String msg;
/*      */                       return msg;
/*      */                     }
/*  428 */                     String name = arg.substring(arg.indexOf("-", 2) + 1);
/*      */                     int tmp2745_2744 = 0;
/*      */                     int[] tmp2745_2742 = argOffset;
/*      */                     int tmp2747_2746 = tmp2745_2742[tmp2745_2744]; tmp2745_2742[tmp2745_2744] = (tmp2747_2746 + 1); value = args[tmp2747_2746];
/*      */                   }
/*      */                   else
/*      */                   {
/*  433 */                     name = arg.substring(arg.indexOf("-", 2) + 1, equalsIndex);
/*  434 */                     value = arg.substring(equalsIndex + 1);
/*      */                   }
/*  436 */                   this.m_overrideProps.put(name, value);
/*      */                 }
/*  438 */                 else if ((arg.startsWith("--register-component")) || (arg.startsWith("-register-component")) || (arg.startsWith("--unregister-component")) || (arg.startsWith("-unregister-component")))
/*      */                 {
/*  444 */                   int equalsIndex = arg.indexOf("=");
/*      */                   String value;
/*      */                   String value;
/*  445 */                   if (equalsIndex < 0)
/*      */                   {
/*  447 */                     if (argOffset[0] >= argc)
/*      */                     {
/*  449 */                       String msg = LocaleUtils.encodeMessage("syOptionRequiresValue", null, arg);
/*      */ 
/*  451 */                       msg = usage(msg);
/*      */                       String msg;
/*      */                       return msg;
/*      */                     }
/*      */                     int tmp2977_2976 = 0;
/*      */                     int[] tmp2977_2974 = argOffset;
/*      */                     int tmp2979_2978 = tmp2977_2974[tmp2977_2976]; tmp2977_2974[tmp2977_2976] = (tmp2979_2978 + 1); value = args[tmp2979_2978];
/*      */                   }
/*      */                   else
/*      */                   {
/*  457 */                     value = arg.substring(equalsIndex + 1);
/*      */                   }
/*  459 */                   if (arg.indexOf("unregister") < equalsIndex)
/*      */                   {
/*  461 */                     this.m_components.remove(value);
/*      */                   }
/*      */                   else
/*      */                   {
/*  465 */                     this.m_components.add(value);
/*      */                   }
/*      */                 }
/*      */                 else
/*      */                 {
/*      */                   Vector list;
/*  468 */                   if (arg.startsWith("--trace="))
/*      */                   {
/*  470 */                     int index = arg.indexOf("=");
/*  471 */                     arg = arg.substring(index + 1);
/*  472 */                     list = StringUtils.parseArray(arg, ',', '^');
/*  473 */                     SystemUtils.setActiveTraces(list);
/*      */                   }
/*  477 */                   else if ((arg.equals("--")) || (!arg.startsWith("-")))
/*      */                   {
/*  479 */                     handleSpecialSettings();
/*  480 */                     removeWaitFile = true;
/*  481 */                     didWork = true;
/*  482 */                     if (!arg.equals("--"))
/*      */                     {
/*  484 */                       argOffset[0] -= 1;
/*      */                     }
/*  488 */                     else if (argOffset[0] == argc)
/*      */                     {
/*  490 */                       doInstall(null);
/*      */                     }
/*      */                     do {
/*  493 */                       if (argOffset[0] >= argc)
/*      */                         break label3303;
/*      */                       int tmp3170_3169 = 0;
/*      */                       int[] tmp3170_3167 = argOffset;
/*      */                       int tmp3172_3171 = tmp3170_3167[tmp3170_3169]; tmp3170_3167[tmp3170_3169] = (tmp3172_3171 + 1); arg = args[tmp3172_3171];
/*  496 */                       rc = doInstall(arg);
/*  497 */                     }while (rc == 0);
/*      */                   }
/*      */                   else
/*      */                   {
/*  505 */                     String msg = LocaleUtils.encodeMessage("syOptionUnknown", null, arg);
/*      */ 
/*  507 */                     list = usage(msg);
/*      */                     String msg;
/*      */                     return list;
/*      */                   }
/*      */                 }
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*  511 */       if (!didWork)
/*      */       {
/*  513 */         Report.trace("install", "version " + versionLabel + " (" + versionNumber + ')', null);
/*  514 */         Report.trace("install", "build " + buildInfo, null);
/*  515 */         if (SystemUtils.m_verbose)
/*      */         {
/*  517 */           Report.debug("install", "calling configureInstallation()", null);
/*      */         }
/*  519 */         handleSpecialSettings();
/*  520 */         rc = configureInstallation();
/*      */       }
/*      */     }
/*      */     catch (Throwable msg)
/*      */     {
/*      */       String msg;
/*  525 */       reportError(progressFile, t);
/*  526 */       Report.trace("install", null, t);
/*  527 */       rc = 1;
/*      */     }
/*      */     finally
/*      */     {
/*      */       String msg;
/*  531 */       if (this.m_targetDir != null)
/*      */       {
/*  533 */         FileUtils.deleteFile(this.m_targetDir + "/install/" + "wait.dat");
/*      */       }
/*  537 */       else if (removeWaitFile)
/*      */       {
/*  539 */         String msg = "The file wait.dat wasn't removed.";
/*  540 */         if (this.m_localizationLoaded)
/*      */         {
/*  542 */           msg = LocaleResources.getString("csInstallerWaitDatError", null);
/*      */         }
/*      */ 
/*  545 */         if (!this.m_isPreinstallationCheck)
/*      */         {
/*  547 */           this.m_promptUser.outputMessage(msg);
/*      */         }
/*      */       }
/*      */ 
/*  551 */       this.m_log = null;
/*      */     }
/*  553 */     this.m_promptUser.finalizeOutput();
/*  554 */     return rc;
/*      */   }
/*      */ 
/*      */   public String getFromPropsOrEnvironment(Properties props, String key)
/*      */   {
/*  559 */     String value = props.getProperty(key);
/*  560 */     if ((value == null) && (this.m_utils != null))
/*      */     {
/*  562 */       value = this.m_utils.getEnv(key);
/*      */     }
/*  564 */     return value;
/*      */   }
/*      */ 
/*      */   public void handleSpecialSettings()
/*      */   {
/*  569 */     String value = getFromPropsOrEnvironment(this.m_overrideProps, "tty.columns");
/*  570 */     if (value != null)
/*      */     {
/*  572 */       int w = NumberUtils.parseInteger(value, 0);
/*  573 */       this.m_promptUser.setLineLength(w);
/*      */     }
/*      */ 
/*  576 */     value = getFromPropsOrEnvironment(this.m_overrideProps, "tty.rows");
/*  577 */     if (value != null)
/*      */     {
/*  579 */       int h = NumberUtils.parseInteger(value, 0);
/*  580 */       this.m_promptUser.setScreenHeight(h);
/*      */     }
/*      */ 
/*  583 */     value = getFromPropsOrEnvironment(this.m_overrideProps, "UpdateThrottle");
/*  584 */     if (value != null)
/*      */     {
/*  586 */       this.m_updateThrottle = NumberUtils.parseInteger(value, 100);
/*      */     }
/*  588 */     String isRelaunch = getFromPropsOrEnvironment(this.m_overrideProps, "IsRelaunch");
/*  589 */     if ((!StringUtils.convertToBool(isRelaunch, false)) || (!this.m_promptUser instanceof ConsolePromptUser) || 
/*  592 */       (!this.m_promptUser instanceof ConsolePromptUser))
/*      */       return;
/*  594 */     ((ConsolePromptUser)this.m_promptUser).m_useNative = false;
/*      */   }
/*      */ 
/*      */   public Properties bootstrapConfig(String filePath, boolean isSecondTry)
/*      */     throws ServiceException, IOException
/*      */   {
/*  602 */     Properties config = new Properties();
/*  603 */     boolean foundConfig = false;
/*      */ 
/*  605 */     if (SystemUtils.m_isDevelopmentEnvironment)
/*      */     {
/*  610 */       intradoc.common.FileUtilsLockDirectory.m_noNewFiles = true;
/*      */     }
/*  612 */     intradoc.common.ParseOutput.m_defaultBufferPool = BufferPool.getBufferPool(null);
/*  613 */     IdcStringBuilder.m_defaultBufferPool = BufferPool.getBufferPool(null);
/*      */ 
/*  615 */     String tmp = null;
/*  616 */     if (filePath != null)
/*      */     {
/*  618 */       tmp = findFile(filePath, isSecondTry);
/*      */     }
/*      */ 
/*  621 */     if (tmp == null)
/*      */     {
/*  627 */       tmp = findFile("intradoc.cfg", isSecondTry);
/*  628 */       if (tmp != null)
/*      */       {
/*  630 */         FileUtils.loadProperties(config, tmp);
/*  631 */         String idcDir = config.getProperty("IntradocDir");
/*      */         String cfgFile;
/*      */         String cfgFile;
/*  633 */         if (idcDir != null)
/*      */         {
/*  635 */           cfgFile = FileUtils.getAbsolutePath(idcDir, "config/config.cfg");
/*      */         }
/*      */         else
/*      */         {
/*  639 */           cfgFile = findFile("../config/config.cfg", isSecondTry);
/*      */         }
/*      */ 
/*  642 */         if (cfgFile != null)
/*      */         {
/*  644 */           FileUtils.loadProperties(config, cfgFile);
/*  645 */           FileUtils.loadProperties(config, tmp);
/*      */         }
/*      */ 
/*  648 */         String product = config.getProperty("IdcProductName");
/*  649 */         if ((idcDir == null) && (product != null))
/*      */         {
/*  653 */           tmp = FileUtils.getAbsolutePath(tmp);
/*  654 */           idcDir = FileUtils.getParent(FileUtils.getParent(tmp));
/*  655 */           Report.trace("install", "Using " + product + " product at " + idcDir, null);
/*      */         }
/*  657 */         if (idcDir == null)
/*      */         {
/*  659 */           String idcHome = config.getProperty("IdcHomeDir");
/*  660 */           if (idcHome == null)
/*      */           {
/*  662 */             tmp = FileUtils.getAbsolutePath(tmp);
/*  663 */             String binDir = FileUtils.getParent(tmp);
/*  664 */             String configFile = FileUtils.directorySlashes(binDir) + "../config/config.cfg";
/*      */ 
/*  666 */             if (FileUtils.checkFile(configFile, true, false) == 0)
/*      */             {
/*  668 */               idcDir = FileUtils.getParent(binDir);
/*  669 */               idcDir = FileUtils.getAbsolutePath(idcDir);
/*  670 */               config.put("IdcHomeDir", idcDir);
/*  671 */               config.put("IsHomeOnly", "true");
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  676 */             foundConfig = true;
/*      */           }
/*      */         }
/*  679 */         if (idcDir != null)
/*      */         {
/*  681 */           if (cfgFile == null)
/*      */           {
/*  683 */             cfgFile = FileUtils.fileSlashes(idcDir + "/config/config.cfg");
/*      */           }
/*  685 */           if (FileUtils.checkFile(cfgFile, true, false) == 0)
/*      */           {
/*  689 */             Report.trace("install", "running from server at " + idcDir, null);
/*      */ 
/*  691 */             String stateFile = FileUtils.fileSlashes(idcDir + "/config/state.cfg");
/*      */ 
/*  693 */             if (FileUtils.checkFile(stateFile, true, false) == 0)
/*      */             {
/*  695 */               FileUtils.loadProperties(config, stateFile);
/*      */             }
/*  697 */             FileUtils.loadProperties(config, cfgFile);
/*  698 */             FileUtils.loadProperties(config, tmp);
/*  699 */             String resourcesDir = config.getProperty("IdcResourcesDir");
/*  700 */             if (resourcesDir == null)
/*      */             {
/*  702 */               String homeDir = config.getProperty("IdcHomeDir");
/*  703 */               if (homeDir != null)
/*      */               {
/*  705 */                 resourcesDir = homeDir + "/resources";
/*      */               }
/*      */               else
/*      */               {
/*  709 */                 resourcesDir = idcDir + "/resources";
/*      */               }
/*  711 */               config.put("IdcResourcesDir", resourcesDir);
/*      */             }
/*  713 */             Report.trace("install", "ResourcesDir=" + resourcesDir, null);
/*  714 */             config.put("RunningFromInstalledServer", "true");
/*  715 */             if (filePath != null)
/*      */             {
/*  717 */               config.put("InstallConfiguration", filePath);
/*  718 */               String msg = LocaleUtils.encodeMessage("csInstallerLogInitMsg2", null, new Date(), filePath);
/*      */ 
/*  721 */               this.m_log.setInitMessage(msg);
/*      */             }
/*  723 */             foundConfig = true;
/*      */           }
/*      */         }
/*  726 */         else if (filePath == null)
/*      */         {
/*  729 */           foundConfig = true;
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  735 */       foundConfig = true;
/*  736 */       filePath = tmp;
/*  737 */       FileUtils.loadProperties(config, filePath);
/*  738 */       String[] traceProps = { "IntradocDir", "IdcHomeDir", "ConfigDir", "IdcResourcesDir", "SourceDirectory", "InstallConfiguration" };
/*      */ 
/*  743 */       for (int i = 0; i < traceProps.length; ++i)
/*      */       {
/*  745 */         String prop = config.getProperty(traceProps[i], null);
/*  746 */         if (null == prop)
/*      */           continue;
/*  748 */         Report.trace("install", traceProps[i] + '=' + prop, null);
/*      */       }
/*      */ 
/*  752 */       String installConfiguration = config.getProperty("InstallConfiguration");
/*      */ 
/*  754 */       if (installConfiguration != null)
/*      */       {
/*  756 */         String msg = LocaleUtils.encodeMessage("csInstallerLogInitMsg2", null, new Date(), filePath);
/*      */ 
/*  759 */         this.m_log.setInitMessage(msg);
/*      */       }
/*      */     }
/*      */ 
/*  763 */     if (foundConfig)
/*      */     {
/*  765 */       if (this.m_localeName == null)
/*      */       {
/*  767 */         this.m_localeName = this.m_overrideProps.getProperty("UserLocale");
/*      */       }
/*  769 */       if (this.m_localeName == null)
/*      */       {
/*  771 */         this.m_localeName = config.getProperty("UserLocale");
/*      */       }
/*  773 */       if (this.m_localeName == null)
/*      */       {
/*  775 */         this.m_localeName = config.getProperty("SystemLocale");
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  781 */       if ((!isSecondTry) && (filePath != null))
/*      */       {
/*  783 */         return bootstrapConfig(filePath, true);
/*      */       }
/*      */       String msg;
/*      */       String msg;
/*  787 */       if (this.m_localizationLoaded)
/*      */       {
/*  789 */         msg = LocaleUtils.encodeMessage("syUnableToFindFile", null, filePath);
/*      */       }
/*      */       else
/*      */       {
/*  794 */         msg = "!$Unable to find file \"" + filePath + "\".";
/*      */       }
/*  796 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  799 */     return config;
/*      */   }
/*      */ 
/*      */   public Properties doInstallInit(String filePath)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/*  805 */     Properties config = bootstrapConfig(filePath, false);
/*      */ 
/*  807 */     String readFromStdin = this.m_overrideProps.getProperty("ReadPropertiesFromStdin");
/*  808 */     if (readFromStdin == null)
/*      */     {
/*  810 */       readFromStdin = config.getProperty("ReadPropertiesFromStdin");
/*      */     }
/*  812 */     String relaunch = this.m_overrideProps.getProperty("IsRelaunch");
/*  813 */     boolean isRelaunch = StringUtils.convertToBool(relaunch, false);
/*  814 */     if (StringUtils.convertToBool(readFromStdin, false))
/*      */     {
/*  818 */       if (!isRelaunch)
/*      */       {
/*  820 */         String prompt = this.m_overrideProps.getProperty("ReadPropertiesFromStdinPrompt");
/*  821 */         if (prompt == null)
/*      */         {
/*  823 */           prompt = config.getProperty("ReadPropertiesFromStdinPrompt");
/*      */         }
/*  825 */         if (prompt != null)
/*      */         {
/*  827 */           System.out.println(prompt);
/*      */         }
/*      */       }
/*  830 */       this.m_stdinProps = new IdcProperties();
/*  831 */       Report.trace("install", "reading props from stdin.", null);
/*  832 */       FileUtils.loadProperties(this.m_stdinProps, System.in);
/*  833 */       Enumeration en = this.m_stdinProps.keys();
/*  834 */       while (en.hasMoreElements())
/*      */       {
/*  836 */         String key = (String)en.nextElement();
/*  837 */         String value = this.m_stdinProps.getProperty(key);
/*  838 */         Report.trace("install", "read property " + key + " from stdin.", null);
/*  839 */         this.m_overrideProps.put(key, value);
/*      */       }
/*      */ 
/*  842 */       if (!isRelaunch)
/*      */       {
/*  844 */         String prompt = this.m_overrideProps.getProperty("ReadPropertiesFromStdinFinishedMessage");
/*  845 */         if (prompt == null)
/*      */         {
/*  847 */           prompt = config.getProperty("ReadPropertiesFromStdinFinishedMessage");
/*      */         }
/*  849 */         if (prompt != null)
/*      */         {
/*  851 */           System.out.println(prompt);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  856 */     String srcDir = this.m_overrideProps.getProperty("SourceDirectory");
/*  857 */     if (srcDir == null)
/*      */     {
/*  859 */       srcDir = config.getProperty("SourceDirectory");
/*  860 */       if (srcDir == null)
/*      */       {
/*  865 */         srcDir = SystemUtils.getBinDir();
/*  866 */         String idcCfg = srcDir + "/intradoc.cfg";
/*  867 */         if (FileUtils.checkFile(idcCfg, true, false) == 0)
/*      */         {
/*  869 */           Properties tmp = new Properties();
/*  870 */           FileUtils.loadProperties(tmp, idcCfg);
/*  871 */           srcDir = tmp.getProperty("SourceDirectory");
/*  872 */           if (srcDir == null)
/*      */           {
/*  874 */             Report.trace("install", "defaulting SourceDirectory to SystemUtils.getBinDir()", null);
/*      */ 
/*  876 */             srcDir = SystemUtils.getBinDir();
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  881 */     srcDir = FileUtils.directorySlashes(srcDir);
/*  882 */     boolean fromInstalledServer = StringUtils.convertToBool(config.getProperty("RunningFromInstalledServer"), false);
/*      */ 
/*  884 */     config.put("SourceDirectory", srcDir);
/*  885 */     Report.trace("install", "SourceDirectory is " + srcDir, null);
/*      */     Properties props;
/*      */     Enumeration en;
/*  886 */     if (!fromInstalledServer)
/*      */     {
/*  888 */       props = new Properties();
/*  889 */       FileUtils.loadProperties(props, srcDir + "/intradoc.cfg");
/*  890 */       for (en = props.propertyNames(); en.hasMoreElements(); )
/*      */       {
/*  892 */         String key = (String)en.nextElement();
/*  893 */         if (config.getProperty(key) == null)
/*      */         {
/*  895 */           config.put(key, props.getProperty(key));
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  900 */     String isPreinstallationCheck = this.m_overrideProps.getProperty("IsPreinstallationCheck");
/*      */ 
/*  902 */     this.m_isPreinstallationCheck = StringUtils.convertToBool(isPreinstallationCheck, false);
/*      */ 
/*  904 */     syncEnvironment(config);
/*  905 */     String resourcesDir = computeResourcesDir(config);
/*  906 */     if (fromInstalledServer)
/*      */     {
/*  908 */       String dataDir = config.getProperty("DataDir");
/*  909 */       if (dataDir == null)
/*      */       {
/*  911 */         dataDir = SystemUtils.getBinDir() + "/../data/";
/*      */       }
/*  913 */       loadLocalization(resourcesDir, dataDir);
/*      */     }
/*      */     else
/*      */     {
/*  917 */       loadLocalization(resourcesDir, resourcesDir);
/*      */     }
/*      */ 
/*  920 */     if (!isRelaunch)
/*      */     {
/*  922 */       promptForPasswords(config);
/*      */     }
/*      */ 
/*  925 */     DataBinder installerDefinition = readInstallerDefinition(config);
/*      */ 
/*  928 */     Report.trace("install", "configuring install system", null);
/*  929 */     configureSystem(srcDir, config);
/*      */ 
/*  931 */     Enumeration en = this.m_overrideProps.propertyNames();
/*  932 */     while (en.hasMoreElements())
/*      */     {
/*  934 */       String key = (String)en.nextElement();
/*  935 */       String value = this.m_overrideProps.getProperty(key);
/*  936 */       if (SystemUtils.m_verbose)
/*      */       {
/*  938 */         Report.debug("install", "overriding " + key + " with value " + value, null);
/*      */       }
/*      */ 
/*  941 */       config.put(key, value);
/*      */     }
/*  943 */     this.m_installer = new SysInstaller();
/*  944 */     this.m_installer.init(installerDefinition, config, this.m_overrideProps, this.m_log, this, this.m_promptUser);
/*      */ 
/*  947 */     String tz = config.getProperty("SystemTimeZone");
/*  948 */     if (tz != null)
/*      */     {
/*  950 */       SharedObjects.putEnvironmentValue("WarnAboutTimeZone", "false");
/*  951 */       SharedObjects.putEnvironmentValue("SystemTimeZone", tz);
/*      */     }
/*      */     else
/*      */     {
/*  955 */       tz = SharedObjects.getEnvironmentValue("SystemTimeZone");
/*      */     }
/*  957 */     if (tz != null)
/*      */     {
/*  959 */       this.m_installer.m_installerConfig.put("SystemTimeZone", tz);
/*  960 */       if (SharedObjects.getEnvValueAsBoolean("WarnAboutTimeZone", false))
/*      */       {
/*  962 */         String msg = LocaleUtils.encodeMessage("csSettingDefaultTimeZone", null, tz);
/*  963 */         this.m_log.warning(msg);
/*      */       }
/*      */     }
/*      */ 
/*  967 */     String term = null;
/*  968 */     if (this.m_utils != null)
/*      */     {
/*  970 */       term = this.m_utils.getEnv("TERM");
/*      */     }
/*  972 */     if (term == null)
/*      */     {
/*  974 */       term = System.getProperty("idc.term.type");
/*      */     }
/*  976 */     if (term == null)
/*      */     {
/*  978 */       term = this.m_installer.getInstallValue("TerminalType", null);
/*      */     }
/*  980 */     if ((term != null) && (this.m_installer.getInstallerTable("AnsiCompatibleTerminals", term) != null) && (this.m_promptUser instanceof ConsolePromptUser))
/*      */     {
/*  984 */       ConsolePromptUser promptUser = (ConsolePromptUser)this.m_promptUser;
/*      */ 
/*  986 */       promptUser.setSelectionStrings("\033[1m", "\033[0m");
/*      */     }
/*      */ 
/*  990 */     return config;
/*      */   }
/*      */ 
/*      */   public void promptForPasswords(Properties config)
/*      */     throws ServiceException
/*      */   {
/*  996 */     String passwordList = this.m_overrideProps.getProperty("PasswordPromptList", config.getProperty("PasswordPromptList"));
/*      */ 
/*  998 */     if (passwordList == null)
/*      */     {
/* 1000 */       return;
/*      */     }
/* 1002 */     if (this.m_stdinProps == null)
/*      */     {
/* 1004 */       this.m_stdinProps = new IdcProperties();
/*      */     }
/* 1006 */     List list = StringUtils.makeListFromSequenceSimple(passwordList);
/* 1007 */     for (String passwordKey : list)
/*      */     {
/* 1009 */       if (this.m_utils == null)
/*      */       {
/* 1012 */         throw new ServiceException(null, "syNativeOsUtilsNotLoaded", new Object[0]);
/*      */       }
/* 1014 */       String value = this.m_overrideProps.getProperty(passwordKey);
/* 1015 */       if (value != null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1020 */       String msgKey = passwordKey + "_message";
/* 1021 */       String msg = this.m_overrideProps.getProperty(msgKey, config.getProperty(msgKey));
/*      */ 
/* 1023 */       if (msg == null)
/*      */       {
/* 1025 */         msg = LocaleResources.localizeMessage(null, IdcMessageFactory.lc("csInstallerGenericPasswordPrompt", new Object[] { passwordKey }), null).toString();
/*      */       }
/*      */ 
/* 1028 */       this.m_utils.writeConsole(msg, 0);
/* 1029 */       String passwordValue = this.m_utils.readConsole(1);
/* 1030 */       passwordValue = passwordValue.trim();
/* 1031 */       this.m_stdinProps.put(passwordKey, passwordValue);
/* 1032 */       this.m_overrideProps.put(passwordKey, passwordValue);
/* 1033 */       this.m_utils.writeConsole("\n", 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addCustomLocalization(SysInstaller tmpInstaller, DataBinder installerDefinition)
/*      */     throws ServiceException
/*      */   {
/* 1040 */     Report.trace("install", "adding localization", null);
/* 1041 */     DataResultSet drset = (DataResultSet)installerDefinition.getResultSet("LocalizationDirectories");
/*      */ 
/* 1043 */     if (drset == null)
/*      */       return;
/* 1045 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1047 */       Properties props = drset.getCurrentRowProps();
/* 1048 */       String name = props.getProperty("Name");
/* 1049 */       String dir = props.getProperty("Directory");
/* 1050 */       String enabled = props.getProperty("Enabled");
/* 1051 */       Report.trace("install", "loading extra localization for " + name, null);
/* 1052 */       if (!StringUtils.convertToBool(enabled, false))
/*      */         continue;
/* 1054 */       dir = tmpInstaller.computeDestinationEx(dir, false);
/* 1055 */       dir = FileUtils.directorySlashes(dir);
/* 1056 */       ResourceContainer res = SharedObjects.getResources();
/* 1057 */       ResourceLoader.loadAllLocalizationStrings(res, dir + "/lang/", null, null, 0);
/*      */     }
/*      */   }
/*      */ 
/*      */   public int doInstall(String filePath)
/*      */     throws ServiceException, DataException, IOException
/*      */   {
/* 1068 */     Properties config = doInstallInit(filePath);
/*      */ 
/* 1071 */     this.m_targetDir = config.getProperty("TargetDir");
/* 1072 */     if (this.m_targetDir == null)
/*      */     {
/* 1075 */       this.m_targetDir = config.getProperty("IntradocDir");
/* 1076 */       if (this.m_targetDir == null)
/*      */       {
/* 1078 */         this.m_targetDir = (SystemUtils.getBinDir() + "/../");
/*      */ 
/* 1081 */         this.m_installer.m_targetDir = (this.m_installer.m_idcDir = this.m_targetDir);
/*      */       }
/*      */     }
/* 1084 */     this.m_targetDir = FileUtils.directorySlashes(this.m_targetDir);
/* 1085 */     config.put("TargetDir", this.m_targetDir);
/*      */ 
/* 1087 */     String runChecks = config.getProperty("RunChecks");
/* 1088 */     String jdbcClasspath = config.getProperty("InstallerJdbcClasspath");
/* 1089 */     String isRelaunchStr = this.m_installer.getInstallValue("IsRelaunch", null);
/* 1090 */     boolean isRelaunch = StringUtils.convertToBool(isRelaunchStr, false);
/* 1091 */     String driver = config.getProperty("JdbcDriverClass");
/* 1092 */     this.m_installer.isRefineryInstall();
/* 1093 */     this.m_installer.loadConfig();
/*      */ 
/* 1097 */     if ((jdbcClasspath == null) || (jdbcClasspath.length() == 0))
/*      */     {
/* 1099 */       jdbcClasspath = this.m_installer.getConfigValue("JDBC_JAVA_CLASSPATH_customjdbc");
/*      */ 
/* 1102 */       if (jdbcClasspath != null)
/*      */       {
/* 1104 */         Report.trace("install", "using custom JDBC driver " + jdbcClasspath, null);
/*      */ 
/* 1113 */         config.put("InstallerJdbcClasspath", jdbcClasspath);
/*      */       }
/*      */       else
/*      */       {
/* 1117 */         jdbcClasspath = config.getProperty("JdbcDriverPackageSourceFiles");
/* 1118 */         if (jdbcClasspath != null)
/*      */         {
/* 1120 */           config.put("InstallerJdbcClasspath", jdbcClasspath);
/*      */         }
/*      */       }
/*      */     }
/* 1124 */     String databaseType = this.m_installer.getInstallValue("DatabaseType", null);
/*      */ 
/* 1126 */     Properties databaseProps = null;
/* 1127 */     if (databaseType != null)
/*      */     {
/* 1129 */       Report.trace("install", "configuring for database " + databaseType, null);
/*      */ 
/* 1131 */       databaseProps = this.m_installer.getInstallerTable("DatabaseServerTable", databaseType);
/*      */ 
/* 1133 */       if (databaseProps == null)
/*      */       {
/* 1135 */         Report.trace("install", "unable to find database properties for " + databaseType, null);
/*      */       }
/*      */ 
/* 1139 */       if (driver == null)
/*      */       {
/* 1141 */         driver = this.m_installer.getConfigValue("JdbcDriver");
/* 1142 */         Report.trace("install", "server config JdbcDriver=" + driver, null);
/*      */       }
/*      */ 
/* 1145 */       if (driver == null)
/*      */       {
/* 1147 */         driver = this.m_installer.getInstallValue("JdbcDriver", null);
/* 1148 */         Report.trace("install", "install config JdbcDriver=" + driver, null);
/*      */       }
/*      */ 
/* 1151 */       if ((driver == null) && (databaseProps != null))
/*      */       {
/* 1153 */         driver = databaseProps.getProperty("DefaultJdbcDriver");
/* 1154 */         Report.trace("install", "using default driver " + driver + " for database " + databaseType, null);
/*      */       }
/*      */     }
/*      */ 
/* 1158 */     if ((jdbcClasspath == null) && (driver != null))
/*      */     {
/* 1160 */       SystemUtils.trace("install", "using JDBC driver " + driver + " without having classpath specified.  " + "Computing classpath.");
/*      */ 
/* 1163 */       this.m_installer.setJdbcDriverAndClasspath(driver, null);
/* 1164 */       jdbcClasspath = this.m_installer.m_installerConfig.getProperty("InstallerJdbcClasspath");
/* 1165 */       Report.trace("install", "InstallerJdbcClasspath is " + jdbcClasspath, null);
/*      */     }
/* 1167 */     if ((StringUtils.convertToBool(runChecks, true)) && (jdbcClasspath != null) && (jdbcClasspath.length() > 0) && (!isRelaunch))
/*      */     {
/* 1175 */       boolean driverLoads = false;
/* 1176 */       if ((driver == null) || (driver.length() == 0))
/*      */       {
/* 1178 */         Report.trace("install", "unable to compute database driver", null);
/*      */       }
/*      */       else
/*      */       {
/*      */         try
/*      */         {
/* 1184 */           Class.forName(driver);
/* 1185 */           driverLoads = true;
/* 1186 */           Report.trace("install", "loaded driver " + driver, null);
/*      */         }
/*      */         catch (ClassNotFoundException e)
/*      */         {
/* 1190 */           Report.trace("install", "unable to load " + driver, e);
/*      */         }
/*      */ 
/* 1193 */         if (!driverLoads)
/*      */         {
/* 1195 */           String classpath = System.getProperty("java.class.path");
/*      */ 
/* 1198 */           if (classpath.indexOf(jdbcClasspath) == -1)
/*      */           {
/* 1200 */             Report.trace("install", "configuring to launch child.  Classpath is " + classpath + ", jdbc driver is " + jdbcClasspath, null);
/*      */           }
/*      */ 
/* 1205 */           Vector args = new IdcVector();
/* 1206 */           Map installerSettings = this.m_installer.determinePlatformInstallerSettings(config, filePath);
/*      */ 
/* 1209 */           config.put("InstallerJdbcClasspath", jdbcClasspath);
/*      */ 
/* 1211 */           this.m_installer.prepareTargetInstallDirectory(installerSettings);
/* 1212 */           this.m_installer.prepareTargetIntradocConfig(config, installerSettings);
/*      */ 
/* 1214 */           args.add(installerSettings.get("InstallerPath"));
/* 1215 */           args.add("--set-IsRelaunch=true");
/*      */ 
/* 1217 */           String srcConfFile = (String)installerSettings.get("InstallerDefSrcFile");
/* 1218 */           String dstConfFile = (String)installerSettings.get("InstallerDefDstFile");
/*      */ 
/* 1220 */           if ((this.m_stdinProps != null) && (this.m_stdinProps.size() > 0))
/*      */           {
/* 1222 */             args.add("--set-ReadPropertiesFromStdin=true");
/* 1223 */             args.add("--set-ReadPropertiesFromStdinPrompt=");
/*      */           }
/*      */           else
/*      */           {
/* 1227 */             args.add("--set-ReadPropertiesFromStdin=false");
/*      */           }
/* 1229 */           for (int i = 0; i < this.m_args.length; ++i)
/*      */           {
/* 1231 */             if ((srcConfFile != null) && (this.m_args[i].equals(srcConfFile)))
/*      */             {
/* 1234 */               if (dstConfFile != null)
/*      */                 break;
/* 1236 */               dstConfFile = this.m_args[i]; break;
/*      */             }
/*      */ 
/* 1240 */             args.add(this.m_args[i]);
/*      */           }
/* 1242 */           this.m_installer.addCommonArguments(args);
/* 1243 */           args.add(dstConfFile);
/* 1244 */           int rc = this.m_installer.runInstallCommand(args, null, this.m_stdinProps);
/* 1245 */           return rc;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1250 */     String progressFileName = config.getProperty("ProgressFileName");
/* 1251 */     if (progressFileName == null)
/*      */     {
/* 1253 */       progressFileName = "install/progress.dat";
/*      */     }
/* 1255 */     if (FileUtils.isAbsolutePath(progressFileName))
/*      */     {
/* 1257 */       this.m_progressFile = progressFileName;
/*      */     }
/*      */     else
/*      */     {
/* 1261 */       this.m_progressFile = (this.m_targetDir + "/" + progressFileName);
/* 1262 */       this.m_progressFile = FileUtils.directorySlashes(this.m_progressFile);
/*      */     }
/* 1264 */     this.m_progressUpdateBehavior = config.getProperty("ProgressUpdateBehavior");
/* 1265 */     if (this.m_progressUpdateBehavior == null)
/*      */     {
/* 1267 */       this.m_progressUpdateBehavior = "overwrite";
/*      */     }
/* 1269 */     this.m_log.setLogDirectory(this.m_targetDir + "/install");
/*      */ 
/* 1271 */     if (this.m_targetDir != null)
/*      */     {
/* 1273 */       String file = this.m_targetDir + "/install/" + "wait.dat";
/* 1274 */       FileUtils.deleteFile(file);
/*      */     }
/* 1276 */     String logDir = this.m_targetDir + "/install/";
/* 1277 */     this.m_installer.verifyDirectory(logDir, 1);
/* 1278 */     Log.setLogDirectory(logDir);
/*      */ 
/* 1280 */     Vector results = new IdcVector();
/* 1281 */     int rc = this.m_installer.doInstall(results);
/* 1282 */     if (rc != 0)
/*      */     {
/* 1284 */       String msg = LocaleUtils.encodeMessage("csInstallerUnknownError", null);
/*      */ 
/* 1286 */       int size = results.size();
/* 1287 */       Object lastResult = null;
/* 1288 */       if (size > 0)
/*      */       {
/* 1290 */         lastResult = results.elementAt(size - 1);
/* 1291 */         if (lastResult instanceof Throwable)
/*      */         {
/* 1293 */           Throwable t = (Throwable)lastResult;
/* 1294 */           Report.trace("install", null, t);
/* 1295 */           msg = t.getMessage();
/*      */         }
/* 1297 */         else if (lastResult instanceof String)
/*      */         {
/* 1299 */           msg = (String)lastResult;
/*      */         }
/* 1301 */         if (msg == null)
/*      */         {
/* 1303 */           msg = "!syNullPointerException";
/*      */         }
/*      */       }
/* 1306 */       reportProgressQuiet(this.m_progressFile, "ERROR: " + LocaleResources.localizeMessage(msg, null));
/*      */ 
/* 1308 */       this.m_installer.m_installLog.error(msg);
/* 1309 */       if ((lastResult != null) && (lastResult instanceof Throwable))
/*      */       {
/* 1311 */         throw new ServiceException((Throwable)lastResult);
/*      */       }
/* 1313 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 1316 */     long kilobytes = this.m_installer.m_fileOutputBytes / 1024L;
/* 1317 */     long seconds = this.m_installer.m_fileOutputTime / 1000L;
/* 1318 */     if (seconds > 0L)
/*      */     {
/* 1320 */       String throughputMessage = "Installation wrote " + kilobytes + " KB in " + seconds + " seconds (" + kilobytes / seconds + "KBps)";
/*      */ 
/* 1324 */       Report.trace("install", throughputMessage, null);
/*      */     }
/*      */ 
/* 1327 */     String finishedMessage = null;
/* 1328 */     if ((this.m_log.hasWarnings()) && (this.m_log.hasErrors()))
/*      */     {
/* 1330 */       finishedMessage = "csInstallerDone3";
/*      */     }
/* 1332 */     else if (this.m_log.hasErrors())
/*      */     {
/* 1334 */       finishedMessage = "csInstallerDone4";
/*      */     }
/* 1336 */     else if (this.m_log.hasWarnings())
/*      */     {
/* 1338 */       finishedMessage = "csInstallerDone2";
/*      */     }
/*      */     else
/*      */     {
/* 1342 */       finishedMessage = "csInstallerDone1";
/*      */     }
/*      */ 
/* 1345 */     String finalMessage = null;
/* 1346 */     if (!this.m_isPreinstallationCheck)
/*      */     {
/* 1348 */       Date d = new Date();
/* 1349 */       finalMessage = LocaleUtils.encodeMessage(finishedMessage, null, LocaleResources.localizeDate(d, null), this.m_installer.getInstallValue("InstallConfiguration", null), this.m_installer.computeDestinationEx("install/log.txt", false));
/*      */ 
/* 1354 */       this.m_log.notice(finalMessage);
/*      */     }
/*      */ 
/* 1357 */     if (this.m_targetDir == null)
/*      */     {
/* 1359 */       this.m_targetDir = this.m_installer.getConfigValue("IntradocDir");
/*      */     }
/* 1361 */     this.m_targetDir = FileUtils.directorySlashes(this.m_targetDir);
/* 1362 */     if (this.m_targetDir.endsWith("/"))
/*      */     {
/* 1364 */       this.m_targetDir = this.m_targetDir.substring(0, this.m_targetDir.length() - 1);
/*      */     }
/* 1366 */     if ((finalMessage != null) && (!this.m_quiet))
/*      */     {
/* 1368 */       finalMessage = LocaleResources.localizeMessage(finalMessage, null);
/*      */ 
/* 1370 */       this.m_promptUser.outputMessage(finalMessage);
/*      */     }
/* 1372 */     finishedMessage = LocaleResources.getString("csInstallerFinished", null);
/* 1373 */     reportProgressQuiet(this.m_progressFile, "FINISHED: " + finishedMessage);
/*      */ 
/* 1375 */     return rc;
/*      */   }
/*      */ 
/*      */   protected int configureInstallation()
/*      */     throws ServiceException, DataException, IOException, InterruptedException
/*      */   {
/* 1382 */     Properties installerProps = bootstrapConfig(null, false);
/* 1383 */     Enumeration en = this.m_overrideProps.propertyNames();
/* 1384 */     while (en.hasMoreElements())
/*      */     {
/* 1386 */       String key = (String)en.nextElement();
/* 1387 */       String value = this.m_overrideProps.getProperty(key);
/* 1388 */       installerProps.put(key, value);
/*      */     }
/*      */ 
/* 1391 */     boolean fromInstalledServer = StringUtils.convertToBool(installerProps.getProperty("RunningFromInstalledServer"), false);
/*      */ 
/* 1394 */     String srcDir = installerProps.getProperty("SourceDirectory");
/* 1395 */     if (srcDir == null)
/*      */     {
/* 1397 */       srcDir = SystemUtils.getBinDir();
/*      */     }
/* 1399 */     srcDir = FileUtils.directorySlashes(srcDir);
/* 1400 */     String resourcesDir = computeResourcesDir(installerProps);
/* 1401 */     syncEnvironment(installerProps);
/* 1402 */     if (fromInstalledServer)
/*      */     {
/* 1404 */       String dataDir = SystemUtils.getAppProperty("DataDir");
/* 1405 */       if (dataDir == null)
/*      */       {
/* 1407 */         if (fromInstalledServer)
/*      */         {
/* 1409 */           dataDir = installerProps.getProperty("IdcHomeDir");
/* 1410 */           if (dataDir == null)
/*      */           {
/* 1412 */             dataDir = installerProps.getProperty("IntradocDir");
/*      */           }
/* 1414 */           dataDir = dataDir + "/data";
/*      */         }
/*      */         else
/*      */         {
/* 1418 */           dataDir = srcDir + "/../data/";
/*      */         }
/*      */       }
/* 1421 */       loadLocalization(resourcesDir, dataDir);
/*      */     }
/*      */     else
/*      */     {
/* 1425 */       loadLocalization(resourcesDir, resourcesDir);
/*      */     }
/*      */ 
/* 1428 */     configureSystem(srcDir, installerProps);
/* 1429 */     installerProps.put("SourceDirectory", srcDir);
/*      */ 
/* 1431 */     DataBinder installerDefinition = readInstallerDefinition(installerProps);
/*      */ 
/* 1434 */     if ((installerProps.getProperty("RunningFromInstalledServer") != null) && (installerProps.getProperty("IsHomeOnly") == null))
/*      */     {
/* 1437 */       usage(null);
/* 1438 */       return 1;
/*      */     }
/*      */ 
/* 1441 */     SysInstaller tmpInstaller = new SysInstaller();
/* 1442 */     tmpInstaller.init(installerDefinition, installerProps, this.m_overrideProps, this.m_log, this, this.m_promptUser);
/*      */ 
/* 1446 */     addCustomLocalization(tmpInstaller, installerDefinition);
/*      */ 
/* 1448 */     InteractiveInstaller installer = null;
/* 1449 */     String className = "<unknown>";
/*      */     try
/*      */     {
/* 1452 */       className = installerProps.getProperty("InteractiveInstaller");
/* 1453 */       if (className == null)
/*      */       {
/* 1455 */         String productNameKey = installerProps.getProperty("InstallProduct");
/* 1456 */         if (productNameKey == null)
/*      */         {
/* 1458 */           productNameKey = "default";
/*      */         }
/* 1460 */         Properties props = tmpInstaller.getInstallerTable("InteractiveInstaller", productNameKey);
/*      */ 
/* 1462 */         if (props != null)
/*      */         {
/* 1464 */           className = props.getProperty("InteractiveInstallerClass");
/*      */         }
/*      */       }
/* 1467 */       if (className == null)
/*      */       {
/* 1469 */         className = "ManualInstaller";
/*      */       }
/* 1471 */       if (className.indexOf(".") + className.indexOf("/") == -2)
/*      */       {
/* 1473 */         className = "intradoc.apputilities.installer." + className;
/*      */       }
/* 1475 */       Class theClass = Class.forName(className);
/* 1476 */       Class[] argumentTypes = new Class[3];
/* 1477 */       argumentTypes[0] = Class.forName("java.util.Properties");
/* 1478 */       argumentTypes[1] = Class.forName("java.util.Properties");
/* 1479 */       argumentTypes[2] = Class.forName("intradoc.apputilities.installer.PromptUser");
/* 1480 */       Constructor constructor = theClass.getConstructor(argumentTypes);
/* 1481 */       Object[] arguments = { installerProps, this.m_overrideProps, this.m_promptUser };
/* 1482 */       installer = (InteractiveInstaller)constructor.newInstance(arguments);
/* 1483 */       Report.trace("install", "using the InteractiveInstaller class '" + className + "'", null);
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1488 */       Report.trace("install", "unable to instantiate class '" + className + "'", t);
/*      */ 
/* 1490 */       throw new ServiceException(t);
/*      */     }
/*      */ 
/* 1493 */     if (installer == null)
/*      */     {
/* 1495 */       installer = new ManualInstaller(installerProps, this.m_overrideProps, this.m_promptUser);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1501 */       return installer.doInstall(installerDefinition, this.m_log, this);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/* 1506 */       if (e.m_errorCode == -64)
/*      */       {
/* 1508 */         String msg = LocaleResources.getString("csInstallerAbortMsg", null);
/*      */ 
/* 1510 */         this.m_promptUser.outputMessage(msg);
/* 1511 */         return 1;
/*      */       }
/* 1513 */       throw e;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String computeResourcesDir(Properties props) throws IOException
/*      */   {
/* 1519 */     String resourcesDir = SystemUtils.getAppProperty("IdcResourcesDir");
/* 1520 */     if (resourcesDir == null)
/*      */     {
/* 1522 */       resourcesDir = System.getProperty("IdcResourcesDir");
/*      */     }
/* 1524 */     if (resourcesDir == null)
/*      */     {
/* 1526 */       if (props.getProperty("RunningFromInstalledServer") != null)
/*      */       {
/* 1528 */         resourcesDir = props.getProperty("IdcHomeDir");
/* 1529 */         if (resourcesDir != null)
/*      */         {
/* 1531 */           resourcesDir = resourcesDir + "/resources";
/*      */         }
/*      */         else
/*      */         {
/* 1535 */           resourcesDir = props.getProperty("IntradocDir");
/* 1536 */           resourcesDir = resourcesDir + "/resources";
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1541 */         resourcesDir = props.getProperty("IdcHomeDir");
/* 1542 */         if (resourcesDir != null)
/*      */         {
/* 1544 */           resourcesDir = resourcesDir + "/resources";
/*      */         }
/*      */         else
/*      */         {
/* 1548 */           resourcesDir = props.getProperty("SourceDirectory");
/* 1549 */           if (resourcesDir != null)
/*      */           {
/* 1551 */             resourcesDir = resourcesDir + "/../resources/";
/*      */           }
/*      */           else
/*      */           {
/* 1555 */             resourcesDir = SystemUtils.getBinDir() + "/../resources/";
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1560 */     SharedObjects.putEnvironmentValue("IdcResourcesDir", resourcesDir);
/* 1561 */     return resourcesDir;
/*      */   }
/*      */ 
/*      */   protected void syncEnvironment(Properties sourceProps)
/*      */   {
/* 1566 */     Enumeration en = sourceProps.propertyNames();
/* 1567 */     while (en.hasMoreElements())
/*      */     {
/* 1569 */       Object obj = en.nextElement();
/* 1570 */       if (obj instanceof String)
/*      */       {
/* 1572 */         String key = (String)obj;
/* 1573 */         String value = sourceProps.getProperty(key);
/* 1574 */         if (value != null)
/*      */         {
/* 1576 */           SharedObjects.putEnvironmentValue(key, value);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void bootstrapL10N()
/*      */   {
/* 1588 */     String[][] l10nStrings = { { "csInstallerUsageText", "Usage: Installer [OPTION]... [--] [input-file]...\n\n  --report-properties [prop-list]...\n                   Output the value of each property in prop-list.\n  --version        Output version information\n  --version classname ...                   Output version information for classname\n  --version-label  Output the version label\n  --version-number Output the version number\n  --list-configurations\n                   Lists the configurations for the installer.\n  --set-PROPERTY=VALUE, --set-PROPERTY VALUE\n                   Sets the installation property PROPERTY to VALUE\n  -v, --verbose    enable verbose tracing\n  -q, --quiet      suppress progress output\n  -t section       add section to tracing\n  --trace=list     trace sections in list\n  -h, --help       display this help text\n\nWith no input files, Installer starts an interactive installer for the Content Server.  If input files are specified, each input-file is processed.  \n\nIf --report-properties is specified, every remaining argument is the name of a Java property.  The name and value of that property is output on stdout.\n\nIf running from an installed Content Server, you can replace [input-file]... with the name of an install configuration.\n" }, { "syOptionRequiresValue", "The option '{1}' requires a value." }, { "syOptionUnknown", "The option '{1}' is not known." }, { "csResourceLoaderFileAccessError", "Unable to access resource file '{1}'." }, { "csLogCouldNotLogFile", "Could not log message to {1}. Type: {2}\n{3}." } };
/*      */ 
/* 1636 */     for (int i = 0; i < l10nStrings.length; ++i)
/*      */     {
/* 1638 */       String key = l10nStrings[i][0];
/* 1639 */       String value = l10nStrings[i][1];
/* 1640 */       String tmp = LocaleResources.getString(key, null);
/* 1641 */       Map m = new HashMap();
/* 1642 */       if ((tmp == null) || (tmp.length() == 0) || (tmp.equals(key)))
/*      */       {
/* 1644 */         m.put(key, value);
/*      */       }
/* 1646 */       LocaleResources.initStrings(m);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void bootstrapLogging()
/*      */   {
/* 1657 */     this.m_log = new InstallLog(this.m_promptUser);
/* 1658 */     LogDirInfo l = new LogDirInfo("system");
/* 1659 */     l.m_dir = null;
/* 1660 */     l.m_logWriters[0] = new HashMap();
/* 1661 */     l.m_logWriters[0].put("writer", this.m_log);
/* 1662 */     Log.m_defaultInfo = l;
/*      */   }
/*      */ 
/*      */   public int usage(String errorText)
/*      */   {
/* 1667 */     if (errorText != null)
/*      */     {
/* 1669 */       errorText = LocaleResources.localizeMessage(errorText, null);
/* 1670 */       this.m_promptUser.outputMessage(errorText);
/*      */     }
/* 1672 */     String localizedUsageText = LocaleResources.getString("csInstallerUsageText", null);
/*      */ 
/* 1675 */     while ((index = localizedUsageText.indexOf("\n")) >= 0)
/*      */     {
/*      */       int index;
/* 1677 */       String line = localizedUsageText.substring(0, index);
/* 1678 */       localizedUsageText = localizedUsageText.substring(index + 1);
/* 1679 */       this.m_promptUser.outputMessage(line);
/*      */     }
/* 1681 */     this.m_promptUser.outputMessage(localizedUsageText);
/* 1682 */     return 1;
/*      */   }
/*      */ 
/*      */   public void reportProgress(int type, String msg, float amtDone, float max)
/*      */   {
/* 1687 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 1688 */     int percent = (int)(100.0F * (amtDone / max) + 0.5D);
/*      */ 
/* 1690 */     if ((percent < this.m_lastPercentReported) || (percent > 100))
/*      */     {
/* 1692 */       percent = this.m_lastPercentReported;
/*      */     }
/*      */     else
/*      */     {
/* 1696 */       if (percent != this.m_lastPercentReported)
/*      */       {
/* 1698 */         this.m_lastUpdate -= this.m_updateThrottle;
/*      */       }
/* 1700 */       this.m_lastPercentReported = percent;
/*      */     }
/* 1702 */     String percentString = "" + percent;
/*      */ 
/* 1704 */     if (max < 0.0F)
/*      */     {
/* 1706 */       percentString = "WORKING";
/*      */     }
/*      */ 
/* 1709 */     boolean trimMsg = false;
/* 1710 */     switch (type)
/*      */     {
/*      */     case -1:
/* 1713 */       buf.append("ERROR:\t");
/* 1714 */       this.m_lastUpdate -= this.m_updateThrottle;
/* 1715 */       break;
/*      */     case 1:
/* 1718 */       trimMsg = true;
/* 1719 */       buf.append(percentString + ": ");
/* 1720 */       break;
/*      */     case 0:
/* 1723 */       trimMsg = true;
/* 1724 */       this.m_lastUpdate -= this.m_updateThrottle;
/* 1725 */       buf.append(percentString + ": ");
/* 1726 */       break;
/*      */     case 2:
/* 1729 */       buf.append("100: ");
/* 1730 */       this.m_lastUpdate -= this.m_updateThrottle;
/*      */     }
/*      */ 
/* 1734 */     buf.append(LocaleResources.localizeMessage(msg, null));
/*      */ 
/* 1736 */     long now = System.currentTimeMillis();
/* 1737 */     if (now - this.m_lastUpdate < this.m_updateThrottle)
/*      */       return;
/* 1739 */     this.m_lastUpdate = now;
/* 1740 */     msg = buf.toString();
/* 1741 */     buf.releaseBuffers();
/* 1742 */     if (msg.equals(this.m_lastMessage))
/*      */       return;
/* 1744 */     this.m_lastMessage = msg;
/* 1745 */     if (trimMsg)
/*      */     {
/* 1747 */       msg = this.m_promptUser.trimStringMid(msg);
/*      */     }
/* 1749 */     reportProgress(this.m_progressFile, msg);
/*      */   }
/*      */ 
/*      */   public void reportError(String progressFile, Throwable t)
/*      */   {
/* 1756 */     IdcStringBuilder builder = new IdcStringBuilder();
/* 1757 */     LocaleUtils.appendThrowableMessagesToAppendable(t, builder);
/*      */ 
/* 1760 */     String msg = LocaleResources.localizeMessage(builder.toString(), null);
/* 1761 */     reportProgressEx(progressFile, "ERROR:\t" + msg, true);
/* 1762 */     if (t instanceof NullPointerException)
/*      */     {
/* 1764 */       Report.trace(null, null, t);
/*      */     }
/*      */     else
/*      */     {
/* 1768 */       this.m_promptUser.outputMessage(msg.toString());
/* 1769 */       Report.trace("install", null, t);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String findFile(String name, boolean isSecondTry)
/*      */   {
/* 1775 */     name = FileUtils.fileSlashes(name);
/* 1776 */     Vector pathList = new IdcVector();
/* 1777 */     pathList.addElement(name);
/* 1778 */     if (!name.startsWith("/"))
/*      */     {
/* 1781 */       String idcOrigPath = System.getProperty("idc.pwd");
/* 1782 */       if (idcOrigPath != null)
/*      */       {
/* 1784 */         idcOrigPath = FileUtils.directorySlashes(idcOrigPath);
/* 1785 */         pathList.addElement(idcOrigPath + name);
/*      */       }
/* 1787 */       idcOrigPath = SystemUtils.getBinDir();
/* 1788 */       if (idcOrigPath != null)
/*      */       {
/* 1790 */         idcOrigPath = FileUtils.directorySlashes(idcOrigPath);
/* 1791 */         pathList.addElement(idcOrigPath + name);
/*      */       }
/*      */     }
/*      */ 
/* 1795 */     int size = pathList.size();
/* 1796 */     for (int i = 0; i < size; ++i)
/*      */     {
/*      */       String path;
/*      */       String path;
/* 1799 */       if (isSecondTry)
/*      */       {
/* 1801 */         path = (String)pathList.elementAt(size - 1 - i);
/*      */       }
/*      */       else
/*      */       {
/* 1805 */         path = (String)pathList.elementAt(i);
/*      */       }
/* 1807 */       Report.trace("install", "looking for \"" + name + "\" in \"" + path + "\".", null);
/*      */ 
/* 1809 */       switch (FileUtils.checkFile(path, true, false))
/*      */       {
/*      */       case -24:
/*      */       case -19:
/*      */       case -18:
/*      */       case -16:
/* 1815 */         break;
/*      */       case -23:
/*      */       case -22:
/*      */       case -21:
/*      */       case -20:
/*      */       case -17:
/*      */       default:
/* 1817 */         return path;
/*      */       }
/*      */     }
/*      */ 
/* 1821 */     return null;
/*      */   }
/*      */ 
/*      */   public void reportProgress(String progressFile, String msg)
/*      */   {
/* 1826 */     reportProgressEx(progressFile, msg, false);
/*      */   }
/*      */ 
/*      */   public void reportProgressQuiet(String progressFile, String msg)
/*      */   {
/* 1831 */     reportProgressEx(progressFile, msg, true);
/*      */   }
/*      */ 
/*      */   public void reportProgressEx(String progressFile, String msg, boolean quiet)
/*      */   {
/*      */     try
/*      */     {
/* 1838 */       msg = msg.trim();
/* 1839 */       if (progressFile != null)
/*      */       {
/* 1841 */         boolean doWrite = StringUtils.convertToBool(this.m_progressUpdateBehavior, true);
/* 1842 */         boolean append = false;
/* 1843 */         if (this.m_progressUpdateBehavior.equalsIgnoreCase("append"))
/*      */         {
/* 1845 */           append = true;
/*      */         }
/* 1847 */         else if (this.m_progressUpdateBehavior.equalsIgnoreCase("overwrite"))
/*      */         {
/* 1849 */           append = false;
/*      */         }
/* 1851 */         if (doWrite)
/*      */         {
/* 1853 */           FileWriter w = new FileWriter(progressFile, append);
/*      */           try
/*      */           {
/* 1856 */             w.write(msg);
/* 1857 */             w.write("\r\n");
/*      */           }
/*      */           finally
/*      */           {
/* 1861 */             FileUtils.closeObject(w);
/*      */           }
/*      */         }
/*      */       }
/* 1865 */       if (!quiet)
/*      */       {
/* 1867 */         this.m_promptUser.updateMessage(msg);
/*      */       }
/*      */     }
/*      */     catch (Throwable t2)
/*      */     {
/* 1872 */       msg = LocaleResources.getString("csInstallerReportErrorStatus", null, new Object[] { msg, progressFile, t2 });
/*      */ 
/* 1874 */       this.m_promptUser.outputMessage(msg);
/* 1875 */       Report.trace("install", null, t2);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataBinder readInstallerDefinition(Properties config)
/*      */     throws IOException, ServiceException, DataException
/*      */   {
/* 1882 */     String srcDir = config.getProperty("SourceDirectory");
/* 1883 */     int code = FileUtils.checkFile(srcDir, false, false);
/* 1884 */     if (code != 0)
/*      */     {
/* 1886 */       String msg = LocaleUtils.encodeMessage("csInstallerLogSrcDirNotFound", null, srcDir, "" + code);
/*      */ 
/* 1888 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/* 1891 */     Properties idcCfg = new Properties();
/* 1892 */     String cfgFile = srcDir + "/intradoc.cfg";
/*      */     try
/*      */     {
/* 1895 */       if (config.getProperty("RunningFromInstalledServer") != null)
/*      */       {
/* 1897 */         cfgFile = config.getProperty("IdcHomeDir");
/* 1898 */         if (cfgFile == null)
/*      */         {
/* 1900 */           cfgFile = config.getProperty("IntradocDir");
/*      */         }
/* 1902 */         cfgFile = cfgFile + "/bin/intradoc.cfg";
/*      */       }
/* 1904 */       FileUtils.loadProperties(idcCfg, cfgFile);
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1908 */       Report.trace("install", "unable to read " + cfgFile, ignore);
/*      */     }
/* 1910 */     String mediaDir = idcCfg.getProperty("MEDIA_DIR");
/* 1911 */     if (mediaDir == null)
/*      */     {
/* 1913 */       Report.trace("install", "using legacy MEDIA_DIR", null);
/* 1914 */       mediaDir = srcDir + "../../";
/*      */     }
/*      */     else
/*      */     {
/* 1918 */       int index = mediaDir.indexOf("$BIN_DIR");
/* 1919 */       if (index >= 0)
/*      */       {
/* 1921 */         mediaDir = mediaDir.substring(0, index) + srcDir + mediaDir.substring(index + "$BIN_DIR".length());
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1926 */     mediaDir = FileUtils.directorySlashes(mediaDir);
/* 1927 */     config.put("MediaDirectory", mediaDir);
/* 1928 */     Report.trace("install", "MediaDirectory: " + mediaDir, null);
/*      */ 
/* 1930 */     String resourcesDir = computeResourcesDir(config);
/* 1931 */     Report.trace("install", "source: " + srcDir, null);
/* 1932 */     Report.trace("install", "resources: " + resourcesDir, null);
/*      */ 
/* 1934 */     DataBinder binder = new DataBinder();
/* 1935 */     binder.setLocalData(config);
/* 1936 */     DataResultSet files = new DataResultSet(new String[] { "FileName" });
/* 1937 */     Vector row = new IdcVector();
/*      */ 
/* 1939 */     row.addElement("install/install_info.htm");
/* 1940 */     files.addRow(row);
/*      */ 
/* 1942 */     if (FileUtils.checkFile(resourcesDir + "/tables/resource_files.htm", true, false) == 0)
/*      */     {
/* 1945 */       row = new IdcVector();
/* 1946 */       row.add("tables/resource_files.htm");
/* 1947 */       files.addRow(row);
/*      */     }
/*      */ 
/* 1950 */     HashMap loadedFiles = new HashMap();
/* 1951 */     Enumeration en = config.propertyNames();
/* 1952 */     ArrayList list = new ArrayList();
/* 1953 */     boolean foundInstallDefSetting = false;
/* 1954 */     while (en.hasMoreElements())
/*      */     {
/* 1956 */       String key = (String)en.nextElement();
/* 1957 */       if (key.startsWith("InstallDefinitionFile"))
/*      */       {
/* 1959 */         String value = config.getProperty(key);
/* 1960 */         if (value.length() > 0)
/*      */         {
/* 1962 */           foundInstallDefSetting = true;
/* 1963 */           list.add(key.replace('=', '_') + "=" + value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1968 */     if (!foundInstallDefSetting)
/*      */     {
/* 1970 */       String prodName = this.m_overrideProps.getProperty("IdcProductName");
/* 1971 */       if (prodName == null)
/*      */       {
/* 1973 */         prodName = config.getProperty("IdcProductName");
/*      */       }
/* 1975 */       if (prodName == null)
/*      */       {
/* 1977 */         prodName = SharedObjects.getEnvironmentValue("IdcProductName");
/*      */       }
/* 1979 */       if (prodName == null)
/*      */       {
/* 1981 */         Report.trace("install", "IdcProductName not set.", null);
/*      */       }
/*      */       else
/*      */       {
/* 1985 */         row = new IdcVector();
/* 1986 */         String installFile = prodName + "_install_info.htm";
/* 1987 */         row.addElement("install/" + installFile);
/* 1988 */         files.addRow(row);
/*      */       }
/*      */     }
/*      */ 
/* 1992 */     Sort.sortList(list, this);
/* 1993 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/* 1995 */       row = new IdcVector();
/* 1996 */       String el = (String)list.get(i);
/* 1997 */       int index = el.indexOf(61);
/* 1998 */       el = el.substring(index + 1);
/* 1999 */       row.add(el);
/* 2000 */       files.addRow(row);
/*      */     }
/*      */ 
/* 2003 */     files.first();
/* 2004 */     readInstallerDefinitionFiles(resourcesDir + "/core", binder, files, loadedFiles);
/* 2005 */     return binder;
/*      */   }
/*      */ 
/*      */   public void readInstallerDefinitionFiles(String srcDir, DataBinder installDef, DataResultSet files, HashMap loadedFiles)
/*      */     throws FileNotFoundException, ServiceException, DataException
/*      */   {
/* 2012 */     while (files.isRowPresent()) {
/*      */       while (true) {
/* 2014 */         fileName = ResultSetUtils.getValue(files, "FileName");
/* 2015 */         if (loadedFiles.get(fileName) == null)
/*      */           break;
/* 2017 */         files.next();
/*      */       }
/*      */ 
/* 2023 */       int i = fileName.indexOf(36);
/* 2024 */       int j = fileName.indexOf(47, i);
/* 2025 */       if ((i >= 0) && (j > i) && (fileName.charAt(i + 1) != '{') && (fileName.charAt(j - 1) != '}'))
/*      */       {
/* 2027 */         fileName = fileName.substring(0, i) + "${" + fileName.substring(i + 1, j) + "}" + fileName.substring(j);
/*      */       }
/*      */ 
/* 2031 */       SysInstaller installer = new SysInstaller();
/* 2032 */       String fileName = installer.substituteVariables(fileName, installDef.getLocalData());
/* 2033 */       fileName = FileUtils.getAbsolutePath(srcDir, fileName);
/* 2034 */       loadedFiles.put(fileName, fileName);
/* 2035 */       Report.trace("install", "reading " + fileName, null);
/* 2036 */       ResourceContainer container = new ResourceContainer();
/* 2037 */       ResourceLoader.loadResourceFile(container, fileName);
/* 2038 */       Table configFiles = container.getTable("InstallConfigFiles");
/* 2039 */       if (configFiles != null)
/*      */       {
/* 2043 */         DataResultSet configFileSet = new DataResultSet();
/* 2044 */         configFileSet.init(configFiles);
/* 2045 */         readInstallerDefinitionFiles(srcDir, installDef, configFileSet, loadedFiles);
/*      */       }
/* 2047 */       Iterator it = container.m_tables.keySet().iterator();
/* 2048 */       while (it.hasNext())
/*      */       {
/* 2050 */         String name = (String)it.next();
/* 2051 */         DataResultSet drset = new DataResultSet();
/* 2052 */         drset.init(container.getTable(name));
/* 2053 */         DataResultSet origSet = (DataResultSet)installDef.getResultSet(name);
/* 2054 */         if (origSet == null)
/*      */         {
/* 2056 */           Report.trace("install", "adding ResultSet " + name, null);
/* 2057 */           installDef.addResultSet(name, drset);
/*      */         }
/*      */         else
/*      */         {
/* 2061 */           DataResultSet installerTables = (DataResultSet)installDef.getResultSet("InstallerTables");
/*      */ 
/* 2063 */           String keyName = null;
/* 2064 */           if ((installerTables == null) || ((keyName = ResultSetUtils.findValue(installerTables, "TableName", name, "KeyName")) == null))
/*      */           {
/* 2068 */             Report.trace("install", "appending to " + name, null);
/* 2069 */             origSet.mergeFields(drset);
/* 2070 */             origSet.merge(null, drset, false);
/*      */           }
/*      */           else
/*      */           {
/* 2074 */             Report.trace("install", "merging ResultSet " + name, null);
/* 2075 */             FieldInfo origMergeField = new FieldInfo();
/* 2076 */             FieldInfo newMergeField = new FieldInfo();
/* 2077 */             if (keyName.indexOf(",") > 0)
/*      */             {
/* 2079 */               if (!origSet.getFieldInfo(keyName, origMergeField))
/*      */               {
/* 2081 */                 Report.trace("install", "adding " + keyName + " to " + name, null);
/*      */ 
/* 2083 */                 addMergedField(origSet, keyName);
/*      */               }
/* 2085 */               if (!drset.getFieldInfo(keyName, newMergeField))
/*      */               {
/* 2087 */                 Report.trace("install", "adding " + keyName + " to " + name, null);
/*      */ 
/* 2089 */                 addMergedField(drset, keyName);
/*      */               }
/*      */             }
/* 2092 */             origSet.mergeFields(drset);
/* 2093 */             origSet.merge(keyName, drset, false);
/*      */           }
/*      */         }
/*      */       }
/* 2097 */       Report.trace("install", "finished merging " + fileName, null);
/*      */ 
/* 2099 */       files.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void addMergedField(DataResultSet drset, String fieldList)
/*      */     throws DataException
/*      */   {
/* 2106 */     List fields = StringUtils.makeListFromSequence(fieldList, ',', '^', 0);
/* 2107 */     String[] fieldArray = (String[])(String[])fields.toArray(new String[0]);
/* 2108 */     FieldInfo[] infos = ResultSetUtils.createInfoList(drset, fieldArray, true);
/*      */ 
/* 2110 */     FieldInfo newField = new FieldInfo();
/* 2111 */     newField.m_name = fieldList;
/* 2112 */     Vector v = new IdcVector();
/* 2113 */     v.addElement(newField);
/* 2114 */     drset.mergeFieldsWithFlags(v, 2);
/* 2115 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 2117 */       IdcStringBuilder buf = new IdcStringBuilder();
/* 2118 */       for (int i = 0; i < fieldArray.length; ++i)
/*      */       {
/* 2120 */         String value = drset.getStringValue(infos[i].m_index);
/* 2121 */         if (i > 0)
/*      */         {
/* 2123 */           buf.append(',');
/*      */         }
/* 2125 */         buf.append(value);
/*      */       }
/* 2127 */       Vector row = drset.getCurrentRowValues();
/* 2128 */       row.set(newField.m_index, buf.toString());
/*      */     }
/* 2130 */     drset.first();
/*      */   }
/*      */ 
/*      */   public void configureSystem(String srcDir, Properties props)
/*      */     throws DataException, ServiceException
/*      */   {
/* 2136 */     if (this.m_utils != null)
/*      */     {
/* 2138 */       String traceIsVerbose = this.m_utils.getEnv("TraceIsVerbose");
/* 2139 */       String traceSections = this.m_utils.getEnv("TraceSectionsList");
/* 2140 */       String withoutTimestamp = this.m_utils.getEnv("TraceWithoutTimestamp");
/* 2141 */       if (traceSections != null)
/*      */       {
/* 2143 */         boolean isVerbose = StringUtils.convertToBool(traceIsVerbose, false);
/*      */ 
/* 2145 */         boolean isWithoutTimestamp = StringUtils.convertToBool(withoutTimestamp, false);
/*      */ 
/* 2147 */         SharedLoader.configureTracingEx(isVerbose, traceSections, isWithoutTimestamp);
/* 2148 */         Report.trace("install", "StartInstaller set TraceSectionsList to " + traceSections, null);
/*      */       }
/*      */ 
/* 2152 */       for (int i = 0; i < TracerReportUtils.m_traceSectionTypes.length; ++i)
/*      */       {
/* 2154 */         String traceType = TracerReportUtils.m_traceSectionTypes[i];
/* 2155 */         String list = this.m_utils.getEnv("Trace" + traceType + "List");
/* 2156 */         Vector traceList = StringUtils.parseArrayEx(list, ',', '^', true);
/* 2157 */         if ((list == null) || (list.length() < 0))
/*      */           continue;
/* 2159 */         SystemUtils.setActiveTraces(traceList, traceType);
/*      */       }
/*      */ 
/* 2163 */       String failOnReplacementCharacter = this.m_utils.getEnv("FailOnReplacementCharacter");
/*      */ 
/* 2165 */       boolean isFailOnReplacementCharacter = StringUtils.convertToBool(failOnReplacementCharacter, false);
/*      */ 
/* 2167 */       SystemUtils.setFailOnReplacementCharacterDefault(isFailOnReplacementCharacter);
/*      */     }
/*      */ 
/* 2170 */     SharedLoader.initFeatures();
/* 2171 */     if (this.m_localeName != null)
/*      */     {
/* 2173 */       IdcLocale locale = LocaleResources.getLocale(this.m_localeName);
/* 2174 */       if (locale == null)
/*      */       {
/* 2176 */         Report.trace("install", "the locale " + this.m_localeName + " is undefined.", null);
/*      */       }
/*      */       else
/*      */       {
/* 2180 */         LocaleResources.m_defaultContext.setCachedObject("UserLocale", locale);
/*      */       }
/*      */     }
/*      */ 
/* 2184 */     SharedObjects.putEnvironmentValue("PrimaryResourceTable", "InstallResourceFiles");
/*      */ 
/* 2186 */     String productName = this.m_overrideProps.getProperty("IdcProductName", props.getProperty("IdcProductName"));
/*      */ 
/* 2188 */     if (productName == null)
/*      */     {
/* 2190 */       Report.trace("install", "Unable to find value for IdcProductName, defaulting to idccs.", new StackTrace());
/*      */ 
/* 2192 */       productName = "idccs";
/*      */     }
/* 2194 */     SharedObjects.putEnvironmentValue("IdcProductName", productName);
/* 2195 */     ComponentLoader.reset();
/* 2196 */     ComponentLoader.initDefaults();
/* 2197 */     ComponentLoader.sortComponents();
/* 2198 */     IdcSystemLoader.loadResourcesEx(true);
/* 2199 */     ResourceContainer res = SharedObjects.getResources();
/* 2200 */     res.m_isFullyLoadedSharedResources = false;
/* 2201 */     IdcSystemLoader.mergeResourceTables();
/* 2202 */     IdcSystemLoader.loadIdocScriptExtensions();
/* 2203 */     DataResultSet features = SharedObjects.getTable("CoreFeatures");
/* 2204 */     Features.registerFromResultSet(features, null);
/*      */ 
/* 2207 */     features = SharedObjects.getTable("JDBCFeatures");
/* 2208 */     if (features == null)
/*      */       return;
/* 2210 */     Features.registerFromResultSet(features, null);
/*      */   }
/*      */ 
/*      */   public void loadLocalization(String resDir, String dataDir)
/*      */   {
/* 2221 */     resDir = FileUtils.directorySlashes(resDir);
/* 2222 */     Report.trace("install", "StartInstaller.loadLocalization(" + resDir + ", " + dataDir + ")", null);
/*      */ 
/* 2227 */     DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*      */     try
/*      */     {
/* 2231 */       IdcSystemConfig.loadEncodingMap(resDir + "core/tables");
/* 2232 */       IdcSystemConfig.loadSystemEncodingInfo();
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2236 */       Report.trace("install", "Exception while loading encoding map.", ignore);
/*      */     }
/*      */ 
/* 2239 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2241 */       Report.debug("install", "Encoding maps loaded.", null);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2246 */       ResourceLoader.loadLocalizationStrings(resDir + "core", dataDir, null);
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2250 */       Report.trace("install", "Exception loading language support.", ignore);
/*      */     }
/* 2252 */     if (SystemUtils.m_verbose)
/*      */     {
/* 2254 */       Report.debug("install", "Strings loaded.", null);
/*      */     }
/*      */ 
/* 2258 */     DataResultSet drset = SharedObjects.getTable("LocaleConfig");
/* 2259 */     if (drset != null)
/*      */     {
/*      */       try
/*      */       {
/* 2263 */         Vector list = new IdcVector();
/* 2264 */         FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "lcIsEnabled", "lcLanguageId" }, true);
/*      */ 
/* 2267 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/* 2269 */           String enabled = drset.getStringValue(infos[0].m_index);
/* 2270 */           String lang = drset.getStringValue(infos[1].m_index);
/* 2271 */           if (!StringUtils.convertToBool(enabled, false)) continue; list.addElement(lang);
/*      */         }
/* 2273 */         if (list.size() > 0)
/*      */         {
/* 2275 */           Help.setValidHelpLangs(list);
/*      */         }
/*      */       }
/*      */       catch (DataException ignore)
/*      */       {
/* 2280 */         if (SystemUtils.m_verbose)
/*      */         {
/* 2282 */           Report.debug("applet", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 2289 */       IdcSystemConfig.configLocalization();
/* 2290 */       this.m_localizationLoaded = true;
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2294 */       Report.trace("install", null, ignore);
/* 2295 */       String msg = ignore.getMessage();
/* 2296 */       if ((msg != null) && (msg.startsWith("!apSystemTimeZoneNotDefined")))
/*      */       {
/* 2298 */         SharedObjects.putEnvironmentValue("SystemTimeZone", "UTC");
/* 2299 */         SharedObjects.putEnvironmentValue("OverrideSystemTimeZone", "1");
/* 2300 */         SharedObjects.putEnvironmentValue("WarnAboutTimeZone", "1");
/*      */         try
/*      */         {
/* 2303 */           IdcSystemConfig.configLocalization();
/* 2304 */           msg = null;
/*      */         }
/*      */         catch (Exception ignore2)
/*      */         {
/* 2308 */           msg = LocaleResources.localizeMessage(msg, null);
/* 2309 */           this.m_promptUser.outputMessage(msg);
/* 2310 */           msg = ignore2.getMessage();
/*      */         }
/*      */       }
/* 2313 */       if (msg != null)
/*      */       {
/* 2315 */         msg = LocaleResources.localizeMessage(msg, null);
/* 2316 */         this.m_promptUser.outputMessage(msg);
/*      */       }
/*      */     }
/* 2319 */     if (!SystemUtils.m_verbose)
/*      */       return;
/* 2321 */     Report.debug("install", "Localization configured.", null);
/*      */   }
/*      */ 
/*      */   public int compare(Object obj1, Object obj2)
/*      */   {
/* 2327 */     String s1 = (String)obj1;
/* 2328 */     String s2 = (String)obj2;
/* 2329 */     return s1.compareTo(s2);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2334 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 105081 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.StartInstaller
 * JD-Core Version:    0.5.4
 */