/*     */ package intradoc.filestore.config;
/*     */ 
/*     */ import intradoc.common.DynamicHtml;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseOutput;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.process.ProcessLogger;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ServerData
/*     */ {
/*  75 */   public DataBinder m_binder = new DataBinder();
/*     */   private Map m_actions;
/*  85 */   protected Map m_keyPathMap = new HashMap();
/*     */ 
/*  89 */   protected List m_keyPathPairs = new ArrayList();
/*     */ 
/*  93 */   protected Map m_pathnameCache = new HashMap();
/*     */ 
/*  95 */   protected String[][] DEFAULT_KEYPATH_PAIRS = { { "BinDir", "$IntradocDir/bin/" }, { "ConfigDir", "$IntradocDir/config/" }, { "DataDir", "$IntradocDir/data/" }, { "IdcResourcesDir", "$IdcHomeDir/resources/" }, { "SystemTemplatesDir", "$IdcResourcesDir/core/templates/" } };
/*     */ 
/* 519 */   protected String SERVER_ACTION_READ = "read";
/* 520 */   protected String SERVER_ACTION_WRITE = "write";
/*     */ 
/*     */   public ServerData()
/*     */   {
/* 114 */     Properties env = SharedObjects.getSecureEnvironment();
/* 115 */     DataBinder data = new DataBinder(env);
/* 116 */     init(data);
/*     */   }
/*     */ 
/*     */   public ServerData(DataBinder data)
/*     */   {
/* 127 */     init(data);
/*     */   }
/*     */ 
/*     */   public void init(DataBinder data)
/*     */   {
/* 132 */     String idcDir = null;
/* 133 */     String idcHomeDir = null;
/* 134 */     if (null == data)
/*     */     {
/* 136 */       Report.trace("system", "ServerData created from null DataBinder", null);
/* 137 */       idcDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 138 */       idcHomeDir = SharedObjects.getEnvironmentValue("IdcHomeDir");
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 144 */         idcDir = data.getEx("IntradocDir", false, true, false, false);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 148 */         Report.trace("system", null, ignore);
/*     */       }
/*     */       try
/*     */       {
/* 152 */         idcHomeDir = data.getEx("IdcHomeDir", false, true, false, false);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 156 */         Report.trace("system", null, ignore);
/*     */       }
/*     */     }
/* 159 */     if (null != idcDir)
/*     */     {
/* 161 */       setIntradocDir(idcDir);
/* 162 */       setIdcHomeDir(idcDir);
/*     */     }
/*     */     else
/*     */     {
/* 167 */       Report.trace("system", "ServerData created without an IntradocDir", null);
/*     */     }
/* 169 */     if (null != idcHomeDir)
/*     */     {
/* 171 */       setIdcHomeDir(idcHomeDir);
/*     */     }
/* 173 */     if (null != data)
/*     */     {
/* 176 */       Properties props = data.getEnvironment();
/* 177 */       storeProperties(props, true);
/* 178 */       props = data.getLocalData();
/* 179 */       storeProperties(props, false);
/* 180 */       ResultSet rset = data.getResultSet("AdminAction");
/* 181 */       if (null != rset)
/*     */       {
/* 183 */         this.m_binder.addResultSetDirect("AdminAction", rset);
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 188 */       processKeyPathStrings(this.DEFAULT_KEYPATH_PAIRS);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 195 */       Report.trace(null, null, e);
/*     */     }
/* 197 */     computeAllowableActions();
/*     */   }
/*     */ 
/*     */   public String getIntradocDir()
/*     */   {
/* 207 */     return (String)this.m_keyPathMap.get("IntradocDir");
/*     */   }
/*     */ 
/*     */   public void setIntradocDir(String dir)
/*     */   {
/* 219 */     this.m_binder.setEnvironmentValue("IntradocDir", dir);
/* 220 */     dir = FileUtils.directorySlashes(dir);
/* 221 */     this.m_keyPathMap.put("IntradocDir", dir);
/*     */   }
/*     */ 
/*     */   public String getIdcHomeDir()
/*     */   {
/* 230 */     return (String)this.m_keyPathMap.get("IdcHomeDir");
/*     */   }
/*     */ 
/*     */   public void setIdcHomeDir(String dir)
/*     */   {
/* 242 */     this.m_binder.setEnvironmentValue("IdcHomeDir", dir);
/* 243 */     dir = FileUtils.directorySlashes(dir);
/* 244 */     this.m_keyPathMap.put("IdcHomeDir", dir);
/*     */   }
/*     */ 
/*     */   protected void storeProperties(Properties serverProps, boolean asEnvironment)
/*     */   {
/* 258 */     Enumeration keys = serverProps.propertyNames();
/* 259 */     while (keys.hasMoreElements())
/*     */     {
/* 261 */       String key = (String)keys.nextElement();
/* 262 */       if (null == key) continue; if (key.length() < 1)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 282 */       String value = this.m_binder.getAllowMissing(key);
/* 283 */       if ((null != value) && (value.length() > 0))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 288 */       value = serverProps.getProperty(key);
/* 289 */       if (null != value)
/*     */       {
/* 291 */         if (asEnvironment)
/*     */         {
/* 293 */           this.m_binder.setEnvironmentValue(key, value);
/*     */         }
/*     */         else
/*     */         {
/* 297 */           this.m_binder.putLocal(key, value);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void processKeyPathStrings(String[][] keyPathPairs)
/*     */     throws ServiceException
/*     */   {
/* 317 */     int length = keyPathPairs.length;
/* 318 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 321 */       String[] pair = { keyPathPairs[i][0], keyPathPairs[i][1] };
/* 322 */       this.m_keyPathPairs.add(pair);
/*     */     }
/* 324 */     String[][] newPairs = applyKeyPathStrings(keyPathPairs);
/* 325 */     FileUtils.addPathSubstitutionMappings(this.m_keyPathMap, newPairs);
/*     */   }
/*     */ 
/*     */   public void processKeyPathMap(Map keyPathMap)
/*     */     throws ServiceException
/*     */   {
/* 342 */     this.m_keyPathMap.putAll(keyPathMap);
/*     */   }
/*     */ 
/*     */   protected String[][] applyKeyPathStrings(String[][] keyPathPairs)
/*     */   {
/* 353 */     int length = keyPathPairs.length;
/* 354 */     String[][] newPairs = new String[length][2];
/* 355 */     for (int i = 0; i < length; ++i)
/*     */     {
/* 357 */       String key = keyPathPairs[i][0];
/* 358 */       String defaultValue = keyPathPairs[i][1];
/* 359 */       String value = this.m_binder.getLocal(key);
/*     */ 
/* 361 */       if (null == value)
/*     */       {
/*     */         try
/*     */         {
/* 372 */           value = this.m_binder.getEx(key, false, false, false, false);
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 376 */           Report.trace("system", null, e);
/*     */         }
/*     */       }
/*     */ 
/* 380 */       if (null == value)
/*     */       {
/* 382 */         value = defaultValue;
/*     */       }
/*     */ 
/* 397 */       newPairs[i][0] = key;
/* 398 */       newPairs[i][1] = value;
/*     */     }
/* 400 */     return newPairs;
/*     */   }
/*     */ 
/*     */   public void reprocessKeyPathStrings()
/*     */     throws ServiceException
/*     */   {
/* 408 */     Object[] pairsObject = this.m_keyPathPairs.toArray();
/* 409 */     int numPairs = pairsObject.length;
/* 410 */     String[][] pairs = new String[numPairs][];
/* 411 */     for (int i = 0; i < numPairs; ++i)
/*     */     {
/* 413 */       Object pair = pairsObject[i];
/* 414 */       pairs[i] = ((String[])(String[])pair);
/*     */     }
/* 416 */     String[][] newPairs = applyKeyPathStrings(pairs);
/* 417 */     String intradocDir = getIntradocDir();
/* 418 */     String idcHomeDir = getIdcHomeDir();
/* 419 */     this.m_keyPathMap.clear();
/* 420 */     this.m_keyPathMap.put("IntradocDir", intradocDir);
/* 421 */     this.m_keyPathMap.put("IdcHomeDir", idcHomeDir);
/* 422 */     FileUtils.addPathSubstitutionMappings(this.m_keyPathMap, newPairs);
/*     */ 
/* 424 */     this.m_pathnameCache.clear();
/*     */   }
/*     */ 
/*     */   protected String computeActualPathname(String precomputedPathname)
/*     */     throws ServiceException
/*     */   {
/* 438 */     String computedPath = null;
/* 439 */     computedPath = (String)this.m_pathnameCache.get(precomputedPathname);
/* 440 */     if (null != computedPath)
/*     */     {
/* 442 */       return computedPath;
/*     */     }
/*     */ 
/* 445 */     char startsWith = precomputedPathname.charAt(0);
/*     */     String fullPathname;
/*     */     String fullPathname;
/* 446 */     if ((!FileUtils.isAbsolutePath(precomputedPathname)) && ('$' != startsWith))
/*     */     {
/* 448 */       fullPathname = "$IntradocDir/" + precomputedPathname;
/*     */     }
/*     */     else
/*     */     {
/* 452 */       fullPathname = precomputedPathname;
/*     */     }
/* 454 */     computedPath = FileUtils.computePathFromSubstitutionMap(this.m_keyPathMap, fullPathname);
/* 455 */     this.m_pathnameCache.put(precomputedPathname, computedPath);
/* 456 */     return computedPath;
/*     */   }
/*     */ 
/*     */   public Map getKeyPathMap()
/*     */   {
/* 464 */     return this.m_keyPathMap;
/*     */   }
/*     */ 
/*     */   protected void computeAllowableActions()
/*     */   {
/* 480 */     String serverActions = this.m_binder.getLocal("serverActions");
/* 481 */     if (null == serverActions)
/*     */     {
/* 483 */       serverActions = "read";
/*     */     }
/* 485 */     Vector validActions = StringUtils.parseArray(serverActions, ',', '\\');
/* 486 */     ResultSet actionSet = this.m_binder.getResultSet("AdminAction");
/* 487 */     this.m_actions = new HashMap();
/* 488 */     for (int i = 0; i < validActions.size(); ++i)
/*     */     {
/* 490 */       String action = (String)validActions.get(i);
/*     */       String command;
/*     */       String command;
/* 492 */       if (null == actionSet)
/*     */       {
/* 494 */         if (!action.equals("read")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 498 */         command = "<$processController$> read \"<$escapeLiteralString(filename)$>\"";
/*     */       }
/*     */       else
/*     */       {
/*     */         try
/*     */         {
/* 504 */           command = ResultSetUtils.findValue(actionSet, "actionName", action, "actionCommand");
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 508 */           break label148:
/*     */         }
/* 510 */         if (null == command) continue; if (command.length() < 1) {
/*     */           continue;
/*     */         }
/*     */       }
/*     */ 
/* 515 */       label148: this.m_actions.put(action, command);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected Object execServerIO(String operation, String pathname)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 534 */     DataBinder mergeBinder = this.m_binder.createShallowCopy();
/* 535 */     mergeBinder.setLocalData(new Properties(mergeBinder.getLocalData()));
/*     */ 
/* 537 */     mergeBinder.putLocal("fileName", pathname);
/*     */ 
/* 539 */     Object stream = execServerAction(mergeBinder, operation);
/* 540 */     return stream;
/*     */   }
/*     */ 
/*     */   protected Object execServerAction(DataBinder binder, String operation)
/*     */     throws DataException, ServiceException, IOException
/*     */   {
/* 564 */     String actionString = (String)this.m_actions.get(operation);
/* 565 */     if (null == actionString)
/*     */     {
/* 567 */       String msg = LocaleUtils.encodeMessage("csAdminSpecifiedActionNotDefined", null, operation, binder.getLocal("IDC_Name"));
/*     */ 
/* 569 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 573 */     String processController = binder.getLocal("processController");
/* 574 */     if ((null == processController) || (processController.length() < 1))
/*     */     {
/* 576 */       processController = ConfigFileLoader.m_defaultCFU.getFilesystemPathByName("$AdminDir/bin/UnixProcCtrl");
/*     */     }
/* 580 */     else if (!FileUtils.isAbsolutePath(processController))
/*     */     {
/* 589 */       String adminDir = ConfigFileLoader.m_defaultCFU.getFilesystemPathByName("$AdminDir");
/*     */ 
/* 591 */       processController = FileUtils.getAbsolutePath(adminDir, processController);
/*     */     }
/*     */ 
/* 593 */     binder.putLocal("processController", processController);
/*     */ 
/* 597 */     DynamicHtml htmlCommand = new DynamicHtml();
/* 598 */     PageMerger pm = new PageMerger(binder, null);
/*     */ 
/* 600 */     ParseOutput output = null;
/*     */     List commandList;
/*     */     try
/*     */     {
/* 603 */       htmlCommand.loadHtmlInContext(new StringReader(actionString), output = new ParseOutput());
/* 604 */       actionString = pm.createMergedPage(htmlCommand);
/* 605 */       commandList = StringUtils.makeListFromEscapedString(actionString);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 613 */       if (output != null)
/*     */       {
/* 615 */         output.releaseBuffers();
/*     */       }
/*     */     }
/* 618 */     String[] commandArray = StringUtils.convertListToArray(commandList);
/*     */ 
/* 621 */     String command = commandList.toString();
/* 622 */     if (SystemUtils.m_verbose)
/*     */     {
/* 624 */       Report.debug("system", "Executing command: " + command, null);
/*     */     }
/* 626 */     Runtime runner = Runtime.getRuntime();
/* 627 */     Process proc = runner.exec(commandArray);
/* 628 */     ProcessLogger logger = new ProcessLogger(proc);
/* 629 */     logger.setHeaderMessage("Problem executing command: " + command);
/* 630 */     logger.setTraceSection("fileaccess");
/*     */ 
/* 632 */     if (operation.equals(this.SERVER_ACTION_READ))
/*     */     {
/* 634 */       proc.getOutputStream().close();
/* 635 */       return logger.getLinkedStdoutStream(0);
/*     */     }
/* 637 */     if (operation.equals(this.SERVER_ACTION_WRITE))
/*     */     {
/* 639 */       proc.getInputStream().close();
/* 640 */       return logger.getLinkedStdinStream(0);
/*     */     }
/*     */ 
/* 643 */     String msg = LocaleUtils.encodeMessage("csAdminSpecifiedActionNotDefined", null, operation, binder.getLocal("IDC_Name"));
/*     */ 
/* 645 */     throw new AssertionError(msg);
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws DataException, ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 656 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84838 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filestore.config.ServerData
 * JD-Core Version:    0.5.4
 */