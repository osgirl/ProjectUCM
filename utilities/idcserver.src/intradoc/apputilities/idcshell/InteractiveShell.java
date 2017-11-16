/*     */ package intradoc.apputilities.idcshell;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.server.script.ScriptExtensionUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.OutputStreamWriter;
/*     */ import java.io.PrintStream;
/*     */ import java.io.PrintWriter;
/*     */ import java.util.List;
/*     */ 
/*     */ public class InteractiveShell extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public static final int M_SIMPLE = 0;
/*     */   public static final int M_IDOC = 1;
/*     */   public static final int M_HCSP = 2;
/*     */   public static final int M_HCST = 3;
/*     */   public static final int M_INTERACTIVE = 4;
/*  64 */   public static String[] SHELL_MODES = { "simple", "idoc", "hcsp", "hcst", "interactive" };
/*     */ 
/*  72 */   public boolean m_hasPrefFile = false;
/*  73 */   public NativeOsUtils m_utils = null;
/*     */   public BufferedReader m_in;
/*     */   public PrintWriter m_out;
/*     */ 
/*     */   public InteractiveShell()
/*     */   {
/*  79 */     this.m_functionTable = new String[] { "doService", "formatBinder", "getWithTrace", "promptUser", "setEnv", "loadFile" };
/*     */ 
/*  88 */     this.m_functionDefinitionTable = new int[][] { { 0, 1, 0, -1, 0 }, { 1, 0, -1, -1, 0 }, { 2, 1, 0, -1, 0 }, { 3, -1, 0, 0, 0 }, { 4, 2, 0, 0, -1 }, { 5, 2, 0, 0, -1 } };
/*     */     try
/*     */     {
/* 100 */       this.m_utils = new NativeOsUtils();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 104 */       Report.trace("idcshell", null, t);
/*     */     }
/*     */ 
/* 107 */     this.m_in = new BufferedReader(new InputStreamReader(System.in));
/* 108 */     this.m_out = new PrintWriter(new OutputStreamWriter(System.out));
/*     */   }
/*     */ 
/*     */   public void setData(DataBinder binder, String arg)
/*     */   {
/* 113 */     int index = arg.indexOf("=");
/* 114 */     if (index == -1)
/*     */     {
/* 116 */       throw new AssertionError("!$setData() passed a string without an =");
/*     */     }
/* 118 */     String key = arg.substring(0, index);
/* 119 */     String value = arg.substring(index + 1);
/* 120 */     binder.putLocal(key, value);
/*     */   }
/*     */ 
/*     */   public void interact(BufferedReader in, ExecutionContext context, int shellMode) throws ServiceException
/*     */   {
/* 125 */     DataBinder binder = (DataBinder)context.getCachedObject("DataBinder");
/* 126 */     PageMerger merger = ScriptExtensionUtils.getOrCreatePageMerger(binder, context);
/* 127 */     List scriptArgs = (List)context.getCachedObject("script_arguments");
/*     */ 
/* 130 */     int promptFlag = 0;
/* 131 */     if (in == null)
/*     */     {
/* 133 */       in = this.m_in;
/*     */     }
/*     */     else
/*     */     {
/* 137 */       promptFlag = -1;
/* 138 */       scriptArgs = null;
/*     */     }
/* 140 */     IdcStringBuilder buffer = new IdcStringBuilder();
/*     */ 
/* 144 */     String line = null;
/*     */     while (true)
/*     */     {
/*     */       try
/*     */       {
/* 149 */         if (scriptArgs != null)
/*     */         {
/* 151 */           if (scriptArgs.size() == 0)
/*     */           {
/* 153 */             if (buffer.length() > 0)
/*     */             {
/* 155 */               line = buffer.toStringNoRelease();
/*     */             }
/*     */             else
/*     */             {
/* 160 */               line = "exit";
/*     */             }
/*     */ 
/* 163 */             break label463:
/*     */           }
/* 165 */           line = (String)scriptArgs.remove(0);
/*     */         }
/*     */         else
/*     */         {
/* 169 */           String prompt = binder.getLocal("prompt");
/* 170 */           if (prompt == null)
/*     */           {
/* 172 */             if (promptFlag == 0)
/*     */             {
/* 174 */               if ((this.m_utils == null) || (this.m_utils.readConsole(0x2 | 0x4) != null))
/*     */               {
/* 177 */                 promptFlag = 1;
/*     */               }
/*     */               else
/*     */               {
/* 181 */                 promptFlag = -1;
/*     */               }
/*     */             }
/* 184 */             switch (promptFlag)
/*     */             {
/*     */             case 1:
/* 187 */               prompt = "<$IDC_Name$>> ";
/* 188 */               break;
/*     */             case -1:
/* 190 */               prompt = "";
/*     */             }
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 196 */             prompt = merger.evaluateScript(prompt);
/*     */           }
/*     */           catch (IOException e)
/*     */           {
/* 201 */             e.printStackTrace();
/*     */           }
/* 203 */           System.out.print(prompt);
/* 204 */           line = in.readLine();
/* 205 */           if (line == null) {
/*     */             break label463;
/*     */           }
/*     */         }
/*     */ 
/* 210 */         if (shellMode == 1)
/*     */         {
/* 212 */           buffer.append(line);
/* 213 */           line = buffer.toStringNoRelease();
/* 214 */           break label463:
/*     */         }
/* 216 */         if (shellMode == 0)
/*     */         {
/* 218 */           String trimmed = line.trim();
/* 219 */           boolean exit = false;
/* 220 */           if (trimmed.equals("help"))
/*     */           {
/* 222 */             trimmed = "include shell_help;";
/*     */           }
/* 224 */           else if (trimmed.equals("exit")) {
/*     */               break label463;
/*     */             }
/*     */ 
/* 228 */           if (trimmed.endsWith(";"))
/*     */           {
/* 230 */             exit = true;
/* 231 */             trimmed = trimmed.substring(0, trimmed.length() - 1);
/*     */           }
/* 233 */           if ((!exit) && (trimmed.length() == 0))
/*     */           {
/* 235 */             if (buffer.length() > 0)
/*     */             {
/* 237 */               System.err.println("Enter ; to execute this script:\n" + buffer.toStringNoRelease());
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 242 */             buffer.append("<$");
/* 243 */             buffer.append(trimmed);
/* 244 */             buffer.append("$>");
/*     */           }
/* 246 */           if (exit)
/*     */           {
/* 248 */             line = buffer.toStringNoRelease();
/* 249 */             label463: break label463:
/*     */           }
/*     */         }
/*     */       }
/*     */       catch (IOException resetBuffer)
/*     */       {
/* 255 */         e.printStackTrace();
/* 256 */         line = null;
/*     */ 
/* 260 */         if (line != null) if (!line.trim().equals("exit"))
/*     */           {
/* 264 */             boolean resetBuffer = true;
/*     */             try
/*     */             {
/* 267 */               if (SystemUtils.isActiveTrace("idcshell"))
/*     */               {
/* 269 */                 System.out.print(line);
/* 270 */                 System.out.print(" ==> ");
/*     */               }
/* 272 */               context.setCachedObject("HtmlResourcesIsChecked", null);
/* 273 */               DataLoader.checkSharedCachedResources(context);
/*     */ 
/* 276 */               String result = merger.evaluateScript(line);
/* 277 */               System.out.println(result);
/*     */             }
/*     */             catch (IllegalArgumentException e)
/*     */             {
/* 282 */               String msg = e.getMessage();
/* 283 */               if ((msg.endsWith("!csDynHTMLUnterminatedLoop")) || (msg.endsWith("!csDynHTMLUnterminatedIf")))
/*     */               {
/* 286 */                 Report.trace("idcshell", "incomplete control structure", null);
/*     */ 
/* 291 */                 buffer.append('\n');
/* 292 */                 resetBuffer = false;
/*     */               }
/*     */               else
/*     */               {
/* 296 */                 e.printStackTrace();
/*     */               }
/*     */             }
/*     */             catch (IOException e)
/*     */             {
/* 301 */               e.printStackTrace();
/*     */             }
/* 303 */             if (resetBuffer)
/*     */             {
/* 305 */               buffer.setLength(0);
/*     */             }
/*     */           }
/* 308 */         if (buffer.length() > 0)
/*     */         {
/* 310 */           String unfinished = buffer.toStringNoRelease();
/* 311 */           Report.trace(null, "Incomplete script not executed: " + unfinished, null);
/*     */         }
/* 313 */         buffer.releaseBuffers();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context) throws ServiceException
/*     */   {
/* 320 */     int[] config = (int[])(int[])info.m_entry;
/* 321 */     String function = info.m_key;
/* 322 */     int nargs = args.length - 1;
/* 323 */     int allowedParams = config[1];
/* 324 */     DataBinder binder = (DataBinder)context.getCachedObject("DataBinder");
/* 325 */     String insufficientArgsMsg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/* 327 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 329 */       throw new IllegalArgumentException(insufficientArgsMsg);
/*     */     }
/*     */ 
/* 339 */     String sArg1 = null;
/* 340 */     String sArg2 = null;
/*     */ 
/* 342 */     long lArg2 = 0L;
/* 343 */     lArg2 += 0L;
/* 344 */     if (nargs > 0)
/*     */     {
/* 346 */       if (config[2] == 0)
/*     */       {
/* 348 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/*     */     }
/* 350 */     if ((config[2] != 1) || 
/* 356 */       (nargs > 1))
/*     */     {
/* 358 */       if (config[3] == 0)
/*     */       {
/* 360 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 362 */       else if (config[3] == 1)
/*     */       {
/* 364 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 369 */     boolean bResult = false;
/* 370 */     int iResult = 0;
/* 371 */     double dResult = 0.0D;
/*     */ 
/* 373 */     Object oResult = null;
/*     */ 
/* 375 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 379 */       ServiceManager manager = new ServiceManager();
/* 380 */       manager.setExecutionContext(context);
/* 381 */       Provider wsProvider = Providers.getProvider("SystemDatabase");
/* 382 */       Workspace ws = null;
/* 383 */       if (wsProvider != null)
/*     */       {
/* 385 */         ws = (Workspace)wsProvider.getProvider();
/*     */       }
/* 387 */       context.setCachedObject("LastException", null);
/* 388 */       Exception exception = null;
/*     */       try
/*     */       {
/* 392 */         binder.removeLocal("StatusCode");
/* 393 */         binder.removeLocal("StatusMessage");
/*     */ 
/* 395 */         binder.putLocal("IdcService", sArg1);
/* 396 */         manager.init(binder, ws);
/* 397 */         manager.setOutputStream(System.out);
/* 398 */         manager.processCommand();
/*     */ 
/* 400 */         binder.putLocal("rc", "0");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 404 */         context.setCachedObject("LastException", e);
/* 405 */         exception = e;
/* 406 */         if (e instanceof ServiceException)
/*     */         {
/* 408 */           binder.putLocal("rc", "" + ((ServiceException)e).m_errorCode);
/*     */         }
/*     */         else
/*     */         {
/* 412 */           binder.putLocal("rc", "1");
/*     */         }
/* 414 */         oResult = binder.getLocal("StatusMessage");
/* 415 */         if (oResult == null)
/*     */         {
/* 417 */           IdcMessage msg = IdcMessageFactory.lc(e);
/* 418 */           oResult = LocaleResources.localizeMessage(null, msg, context);
/* 419 */           binder.putLocal("StatusMessageKey", LocaleUtils.encodeMessage(msg));
/* 420 */           binder.putLocal("StatusMessage", oResult.toString());
/*     */         }
/*     */       }
/*     */       finally
/*     */       {
/* 425 */         oResult = binder.getLocal("StatusMessage");
/* 426 */         if (oResult != null)
/*     */         {
/* 428 */           Report.trace("idcshell", "doService() error: " + oResult, exception);
/*     */         }
/*     */ 
/* 431 */         if (ws != null)
/*     */         {
/* 433 */           ws.releaseConnection();
/*     */         }
/*     */       }
/* 436 */       break;
/*     */     case 1:
/* 440 */       oResult = binder.toString();
/* 441 */       break;
/*     */     case 2:
/* 445 */       DataException exception = null;
/* 446 */       String src = null;
/*     */       try
/*     */       {
/* 449 */         oResult = binder.getEx(sArg1, true, false, false, false);
/* 450 */         if (oResult != null)
/*     */         {
/* 452 */           src = "active";
/*     */         }
/* 454 */         else if ((oResult = binder.getEx(sArg1, false, true, false, false)) != null)
/*     */         {
/* 456 */           src = "local";
/*     */         }
/* 458 */         else if ((oResult = binder.getEx(sArg1, false, false, true, false)) != null)
/*     */         {
/* 460 */           src = "resultsets";
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 465 */         exception = e;
/*     */       }
/*     */ 
/* 468 */       if (src != null)
/*     */       {
/* 470 */         Report.trace(null, "found value " + oResult + " for " + sArg1 + " from " + src, exception);
/*     */       }
/*     */       else
/*     */       {
/* 474 */         Report.trace(null, "unable to find value for " + sArg1, exception);
/*     */       }
/* 476 */       break;
/*     */     case 3:
/* 480 */       Report.trace("idcshell", "prompting user", null);
/* 481 */       this.m_utils.writeConsole(sArg1, 0);
/* 482 */       int flags = ((sArg2 != null) && (sArg2.indexOf("NO_ECHO") >= 0)) ? 1 : 0;
/* 483 */       oResult = this.m_utils.readConsole(flags);
/* 484 */       break;
/*     */     case 4:
/* 488 */       Report.trace("idcshell", "setting " + sArg1 + "=" + sArg2, null);
/* 489 */       SharedObjects.putEnvironmentValue(sArg1, sArg2);
/* 490 */       break;
/*     */     case 5:
/* 494 */       Report.trace("idcshell", "loadFile(" + sArg1 + ", " + sArg2 + ")", null);
/* 495 */       String file = sArg1;
/* 496 */       BufferedReader reader = null;
/*     */       try
/*     */       {
/* 499 */         reader = FileUtils.openDataReader(file);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 503 */         Report.trace(null, null, e);
/* 504 */         return false;
/*     */       }
/* 506 */       String modeString = sArg2;
/* 507 */       int shellMode = 0;
/* 508 */       for (int i = 0; i < SHELL_MODES.length; ++i)
/*     */       {
/* 510 */         String mode = SHELL_MODES[i];
/* 511 */         if (!mode.equals(modeString))
/*     */           continue;
/* 513 */         shellMode = i;
/*     */       }
/*     */ 
/* 516 */       interact(reader, context, shellMode);
/* 517 */       FileUtils.closeObject(reader);
/*     */ 
/* 519 */       break;
/*     */     default:
/* 522 */       return false;
/*     */     }
/*     */ 
/* 525 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 528 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 533 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99018 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcshell.InteractiveShell
 * JD-Core Version:    0.5.4
 */