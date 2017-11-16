/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.project.ProjectFileUtils;
/*     */ import intradoc.server.project.ProjectInfo;
/*     */ import intradoc.server.project.Projects;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.io.File;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProjectService extends Service
/*     */ {
/*  34 */   protected DataBinder m_decoder = null;
/*     */ 
/*     */   public ProjectService()
/*     */   {
/*  40 */     this.m_decoder = new DataBinder();
/*  41 */     this.m_decoder.m_isCgi = true;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void registerProject()
/*     */     throws DataException, ServiceException
/*     */   {
/*  48 */     String projectID = this.m_binder.get("dProjectID");
/*  49 */     ResultSet rset = this.m_binder.getResultSet("RegisteredProject");
/*  50 */     boolean isUpdate = rset.isRowPresent();
/*     */ 
/*  53 */     String xmlPath = this.m_binder.getLocal("xml:path");
/*  54 */     String functionStr = this.m_binder.get("functions");
/*  55 */     String urlPath = this.m_binder.getLocal("urlPath");
/*  56 */     String sourcePath = this.m_binder.getLocal("sourcePath");
/*  57 */     String desc = this.m_binder.getLocal("description");
/*     */ 
/*  59 */     String oldFunctionStr = null;
/*     */ 
/*  62 */     if (isUpdate)
/*     */     {
/*  65 */       oldFunctionStr = this.m_binder.getFromSets("dPrjFunctions");
/*     */     }
/*     */     else
/*     */     {
/*  69 */       if (urlPath == null)
/*     */       {
/*  71 */         urlPath = "";
/*     */       }
/*  73 */       if (sourcePath == null)
/*     */       {
/*  75 */         sourcePath = "";
/*     */       }
/*  77 */       if (desc == null)
/*     */       {
/*  79 */         desc = projectID;
/*     */       }
/*  81 */       this.m_binder.putLocal("dPrjFunctions", functionStr);
/*     */     }
/*     */ 
/*  84 */     if (urlPath != null)
/*     */     {
/*  86 */       this.m_binder.putLocal("dPrjUrlPath", urlPath);
/*     */     }
/*  88 */     if (sourcePath != null)
/*     */     {
/*  90 */       this.m_binder.putLocal("dPrjSourcePath", sourcePath);
/*     */     }
/*  92 */     if (desc != null)
/*     */     {
/*  94 */       this.m_binder.putLocal("dPrjDescription", desc);
/*  95 */       if (!isUpdate)
/*     */       {
/*  99 */         rset = this.m_workspace.createResultSet("QregisteredProjectByDesc", this.m_binder);
/* 100 */         if (rset.isRowPresent())
/*     */         {
/* 102 */           createServiceException(null, "!csProjAlreadyExists");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 107 */     Parameters params = null;
/* 108 */     List xmlNodes = null;
/* 109 */     File xmlFile = null;
/*     */ 
/* 112 */     if ((functionStr.indexOf("preview") >= 0) || (functionStr.indexOf("stagingworkflow") >= 0))
/*     */     {
/* 114 */       if ((xmlPath == null) || (xmlPath.length() == 0))
/*     */       {
/* 116 */         createServiceException(null, "!csXmlFunctionMustBeSpecfied");
/*     */       }
/*     */ 
/* 119 */       if ((xmlPath != null) && (xmlPath.length() > 0))
/*     */       {
/*     */         try
/*     */         {
/* 125 */           xmlNodes = ProjectFileUtils.readXmlFile(xmlPath);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 129 */           createServiceException(e, "!csXmlFileError");
/*     */         }
/*     */ 
/* 133 */         xmlFile = new File(xmlPath);
/*     */       }
/*     */     }
/*     */ 
/* 137 */     ProjectInfo info = null;
/* 138 */     if (functionStr.length() > 0)
/*     */     {
/* 140 */       info = checkAndLoadProjectInfo(projectID, xmlNodes);
/* 141 */       String projectDir = DirectoryLocator.getProjectDirectory();
/* 142 */       projectDir = projectDir + projectID.toLowerCase() + "/";
/*     */ 
/* 145 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(projectDir, 2, true);
/*     */ 
/* 147 */       FileUtils.reserveDirectory(projectDir);
/*     */       try
/*     */       {
/* 151 */         Vector oldFunctions = StringUtils.parseArray(oldFunctionStr, ',', '^');
/*     */ 
/* 153 */         boolean isRegistered = false;
/* 154 */         boolean isRegisterWf = false;
/* 155 */         Vector functions = StringUtils.parseArray(functionStr, ',', '^');
/* 156 */         int num = functions.size();
/* 157 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 159 */           String function = (String)functions.elementAt(i);
/* 160 */           String xmlName = null;
/* 161 */           if (function.equalsIgnoreCase("stagingworkflow"))
/*     */           {
/* 163 */             isRegisterWf = true;
/* 164 */             xmlName = "workflow.xml";
/*     */           }
/* 166 */           else if (function.equalsIgnoreCase("preview"))
/*     */           {
/* 168 */             xmlName = "preview.xml";
/*     */           }
/* 170 */           else if (function.equals("registered"))
/*     */           {
/* 172 */             isRegistered = true;
/*     */           }
/*     */ 
/* 175 */           if ((xmlName == null) && (!isRegistered))
/*     */           {
/* 177 */             String msg = LocaleUtils.encodeMessage("csProjUnrecognizedFunction", null, function);
/*     */ 
/* 179 */             throw new DataException(msg);
/*     */           }
/* 181 */           if (xmlName != null)
/*     */           {
/* 183 */             FileUtils.copyFile(xmlFile.getAbsolutePath(), projectDir + xmlName);
/*     */           }
/*     */ 
/* 187 */           boolean isFound = false;
/* 188 */           int oldNum = oldFunctions.size();
/* 189 */           for (int j = 0; j < oldNum; ++j)
/*     */           {
/* 191 */             String oldFunction = (String)oldFunctions.elementAt(j);
/* 192 */             if (!oldFunction.equals(function))
/*     */               continue;
/* 194 */             isFound = true;
/* 195 */             break;
/*     */           }
/*     */ 
/* 198 */           if (isFound)
/*     */             continue;
/* 200 */           oldFunctions.addElement(function);
/*     */         }
/*     */ 
/* 204 */         functionStr = StringUtils.createString(oldFunctions, ',', '^');
/* 205 */         info.put("dPrjFunctions", functionStr);
/*     */ 
/* 207 */         Projects.updateProjectXml(info, functions, xmlNodes);
/* 208 */         if (isRegisterWf)
/*     */         {
/* 210 */           info.remove("IsWfProjectRemovalPending");
/*     */         }
/* 212 */         params = new PropParameters(info.m_properties);
/*     */       }
/*     */       finally
/*     */       {
/* 216 */         FileUtils.releaseDirectory(projectDir);
/* 217 */         deleteFile(xmlFile);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 222 */       params = this.m_binder;
/*     */     }
/*     */ 
/* 225 */     if (isUpdate)
/*     */     {
/* 227 */       this.m_workspace.execute("UregisteredProject", params);
/*     */     }
/*     */     else
/*     */     {
/* 231 */       this.m_workspace.execute("IregisteredProject", params);
/*     */     }
/*     */ 
/* 234 */     if (info == null)
/*     */       return;
/* 236 */     Projects.addProjectInfo(info);
/*     */   }
/*     */ 
/*     */   protected void deleteFile(File file)
/*     */   {
/* 242 */     if (file == null)
/*     */       return;
/*     */     try
/*     */     {
/* 246 */       file.delete();
/*     */     }
/*     */     catch (Throwable ignore)
/*     */     {
/* 250 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 252 */       Report.debug("workflow", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected ProjectInfo checkAndLoadProjectInfo(String projectID, List xmlNodes)
/*     */     throws DataException, ServiceException
/*     */   {
/* 261 */     ProjectInfo info = Projects.getProjectInfo(projectID);
/* 262 */     Properties props = null;
/* 263 */     if (info == null)
/*     */     {
/* 266 */       props = new Properties();
/* 267 */       props.put("dProjectID", projectID);
/*     */ 
/* 269 */       info = new ProjectInfo();
/* 270 */       info.init(props);
/*     */     }
/*     */     else
/*     */     {
/* 274 */       ProjectInfo pi = new ProjectInfo();
/* 275 */       pi.copyShallow(info);
/* 276 */       info = pi;
/* 277 */       props = info.m_properties;
/*     */     }
/*     */ 
/* 281 */     String[] stdFields = { "dPrjFunctions", "dPrjUrlPath", "dPrjSourcePath", "dPrjDescription" };
/* 282 */     int num = stdFields.length;
/* 283 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 285 */       String key = stdFields[i];
/* 286 */       String value = this.m_binder.getAllowMissing(key);
/* 287 */       props.put(key, value);
/*     */     }
/*     */ 
/* 291 */     String desc = null;
/* 292 */     if ((xmlNodes != null) && (xmlNodes.size() > 0))
/*     */     {
/* 294 */       PropertiesTreeNode node = (PropertiesTreeNode)xmlNodes.get(0);
/* 295 */       Properties nProps = node.m_properties;
/* 296 */       desc = nProps.getProperty("description");
/*     */     }
/* 298 */     if (desc != null)
/*     */     {
/* 300 */       desc = this.m_decoder.decode(desc);
/* 301 */       props.put("dPrjDescription", desc);
/*     */     }
/*     */ 
/* 304 */     validateDBFieldLengths("IregisteredProject", this.m_binder);
/*     */ 
/* 306 */     return info;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void unregisterProject() throws DataException, ServiceException
/*     */   {
/* 312 */     String projectID = this.m_binder.getLocal("dProjectID");
/* 313 */     ProjectInfo info = Projects.getProjectInfo(projectID);
/* 314 */     if (info == null)
/*     */     {
/* 316 */       return;
/*     */     }
/* 318 */     setCachedObject("ProjectInfo", info);
/*     */ 
/* 320 */     String str = this.m_binder.getLocal("functions");
/* 321 */     Vector functions = StringUtils.parseArray(str, ',', '^');
/*     */ 
/* 323 */     str = info.get("dPrjFunctions");
/* 324 */     Vector oldFunctions = StringUtils.parseArray(str, ',', '^');
/*     */ 
/* 326 */     boolean isUnregisterWf = false;
/* 327 */     int num = functions.size();
/* 328 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 330 */       String function = (String)functions.elementAt(i);
/*     */ 
/* 332 */       int numOld = oldFunctions.size();
/* 333 */       for (int j = 0; j < numOld; ++j)
/*     */       {
/* 335 */         String oldFunction = (String)oldFunctions.elementAt(j);
/* 336 */         if (!oldFunction.equalsIgnoreCase(function))
/*     */           continue;
/* 338 */         oldFunctions.removeElementAt(j);
/* 339 */         if ((isUnregisterWf) || (!function.equals("stagingworkflow")))
/*     */           break;
/* 341 */         isUnregisterWf = true; break;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 348 */     Parameters params = null;
/* 349 */     boolean isUnregisterAll = StringUtils.convertToBool(this.m_binder.getLocal("isUnregisterAll"), false);
/*     */ 
/* 351 */     if (isUnregisterAll)
/*     */     {
/* 353 */       params = this.m_binder;
/*     */     }
/*     */     else
/*     */     {
/* 357 */       str = StringUtils.createString(oldFunctions, ',', '^');
/* 358 */       info.put("dPrjFunctions", str);
/* 359 */       info.updateFunctionFlags();
/*     */ 
/* 361 */       params = new PropParameters(info.m_properties);
/* 362 */       isUnregisterAll = (str == null) || (str.length() == 0);
/*     */     }
/*     */ 
/* 365 */     if (isUnregisterWf)
/*     */     {
/* 368 */       info.put("IsWfProjectRemovalPending", "1");
/*     */ 
/* 371 */       DataBinder workData = new DataBinder();
/*     */ 
/* 373 */       workData.setLocalData(info.m_properties);
/* 374 */       workData.putLocal("ProjectAction", "unregisterWorkflow");
/* 375 */       workData.putLocal("isUnregisterAll", String.valueOf(isUnregisterAll));
/* 376 */       workData.putLocal("dProjectID", projectID);
/*     */ 
/* 378 */       String[][] querySetMap = { { "QprojectDocuments", "ProjectDocuments" }, { "QworkflowProjects", "ProjectWorkflows" } };
/*     */ 
/* 383 */       for (int i = 0; i < querySetMap.length; ++i)
/*     */       {
/* 385 */         ResultSet rset = this.m_workspace.createResultSet(querySetMap[i][0], workData);
/* 386 */         DataResultSet drset = new DataResultSet();
/* 387 */         drset.copy(rset);
/* 388 */         workData.addResultSet(querySetMap[i][1], drset);
/*     */       }
/*     */ 
/* 391 */       doProjectWork(workData);
/*     */     }
/*     */     else
/*     */     {
/* 395 */       unregisterOrDeleteProject(params, projectID, isUnregisterAll);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void computeProjectInfo() throws DataException
/*     */   {
/* 402 */     String str = this.m_binder.get("dPrjFunctions");
/* 403 */     Vector functions = StringUtils.parseArray(str, ',', '^');
/* 404 */     int num = functions.size();
/* 405 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 407 */       String function = (String)functions.elementAt(i);
/* 408 */       this.m_binder.putLocal("Has" + function, "1");
/*     */     }
/*     */ 
/* 411 */     String projectID = this.m_binder.getLocal("dProjectID");
/* 412 */     ProjectInfo info = Projects.getProjectInfo(projectID);
/* 413 */     DataBinder.mergeHashTables(this.m_binder.getLocalData(), info.m_properties);
/*     */   }
/*     */ 
/*     */   protected void doProjectWork(DataBinder workData)
/*     */   {
/* 418 */     DataBinder workBinder = workData;
/* 419 */     int timeout = this.m_workspace.getThreadTimeout();
/* 420 */     Runnable run = new Runnable(workBinder, timeout)
/*     */     {
/*     */       public void run()
/*     */       {
/* 424 */         String action = this.val$workBinder.getLocal("ProjectAction");
/*     */         try
/*     */         {
/* 427 */           ProjectService.this.m_workspace.setThreadTimeout(this.val$timeout);
/* 428 */           if (action.equals("unregisterWorkflow"))
/*     */           {
/* 430 */             ProjectService.this.unregisterWorkflowProject(this.val$workBinder);
/*     */           }
/*     */         }
/*     */         finally
/*     */         {
/* 435 */           ProjectService.this.m_workspace.releaseConnection();
/* 436 */           ProjectService.this.m_workspace.clearThreadTimeout();
/*     */         }
/*     */       }
/*     */     };
/* 441 */     Thread bgThread = new Thread(run, "project work");
/* 442 */     bgThread.setDaemon(true);
/* 443 */     bgThread.start();
/*     */   }
/*     */ 
/*     */   protected void unregisterWorkflowProject(DataBinder workBinder)
/*     */   {
/* 449 */     String projectID = workBinder.getLocal("dProjectID");
/*     */     try
/*     */     {
/* 452 */       setConditionVar("IsPublish", true);
/* 453 */       executeResultSetCommand("DELETE_BYNAME", "ProjectDocuments", workBinder);
/* 454 */       executeResultSetCommand("CRITERIAWORKFLOW_DISABLE_SUB", "ProjectWorkflows", workBinder);
/*     */ 
/* 456 */       boolean isUnregisterAll = StringUtils.convertToBool(workBinder.getLocal("isUnregisterAll"), false);
/*     */ 
/* 459 */       this.m_workspace.execute("DprojectProblemReports", workBinder);
/* 460 */       if (!isUnregisterAll)
/*     */       {
/* 462 */         ProjectFileUtils.deleteReportsDirectory(projectID);
/*     */       }
/*     */ 
/* 466 */       unregisterOrDeleteProject(workBinder, projectID, isUnregisterAll);
/*     */     }
/*     */     catch (Exception info)
/*     */     {
/*     */       ProjectInfo info;
/* 470 */       String msg = LocaleUtils.encodeMessage("csProjUnableToRegisterForWorkflow", null, projectID);
/*     */ 
/* 472 */       Report.error(null, msg, e);
/*     */     }
/*     */     finally
/*     */     {
/*     */       ProjectInfo info;
/* 476 */       this.m_workspace.releaseConnection();
/* 477 */       refreshAfterProjectChange();
/*     */ 
/* 479 */       ProjectInfo info = (ProjectInfo)getCachedObject("ProjectInfo");
/* 480 */       info.remove("IsWfProjectRemovalPending");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshAfterProjectChange()
/*     */   {
/*     */     try
/*     */     {
/* 489 */       SubjectManager.refreshSubjectAll("projects", this.m_binder, this);
/* 490 */       SubjectManager.refreshSubjectAll("workflows", this.m_binder, this);
/* 491 */       SubjectManager.refreshSubjectAll("documents", this.m_binder, this);
/* 492 */       SubjectManager.notifyChanged("projects");
/* 493 */       SubjectManager.notifyChanged("workflows");
/* 494 */       SubjectManager.notifyChanged("documents");
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 498 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 500 */       Report.debug("workflow", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void unregisterOrDeleteProject(Parameters params, String projectID, boolean isUnregisterAll)
/*     */     throws DataException
/*     */   {
/* 508 */     if (isUnregisterAll)
/*     */     {
/* 511 */       this.m_workspace.execute("DregisteredProject", params);
/* 512 */       Projects.deleteProject(projectID);
/*     */     }
/*     */     else
/*     */     {
/* 516 */       ProjectInfo info = (ProjectInfo)getCachedObject("ProjectInfo");
/*     */ 
/* 519 */       this.m_workspace.execute("UregisteredProject", params);
/* 520 */       ProjectFileUtils.removeFunctionFiles(info, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void executeResultSetCommand(String cmd, String rsName, DataBinder binder) throws DataException, ServiceException
/*     */   {
/* 526 */     DataResultSet rset = (DataResultSet)binder.getResultSet(rsName);
/* 527 */     if (rset == null)
/*     */     {
/* 530 */       return;
/*     */     }
/*     */ 
/* 534 */     Properties localData = binder.getLocalData();
/*     */ 
/* 536 */     DataBinder oldBinder = this.m_binder;
/*     */     try
/*     */     {
/* 539 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 542 */         Properties props = (Properties)localData.clone();
/* 543 */         Properties rowProps = rset.getCurrentRowProps();
/*     */ 
/* 545 */         DataBinder.mergeHashTables(props, rowProps);
/* 546 */         this.m_binder.setLocalData(props);
/*     */         try
/*     */         {
/* 549 */           executeService(cmd);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 553 */           Report.error(null, "!csProjUnableToDoWork", e);
/*     */         }
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 559 */       this.m_binder = oldBinder;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void validateDBFieldLengths(String query, Parameters data)
/*     */     throws DataException, ServiceException
/*     */   {
/* 568 */     String[] queryParams = this.m_workspace.getQueryParameters(query);
/*     */ 
/* 570 */     int num = queryParams.length;
/* 571 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 573 */       String name = queryParams[i];
/* 574 */       String val = data.get(name);
/*     */ 
/* 576 */       String envKey = name + ":maxLength";
/* 577 */       int maxLength = SharedObjects.getEnvironmentInt(envKey, 30);
/* 578 */       if (val.length() <= maxLength)
/*     */         continue;
/* 580 */       String msg = LocaleUtils.encodeMessage("csCheckinFieldTooLong2", null, name, "" + maxLength);
/*     */ 
/* 582 */       createServiceException(null, msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyStartPublish()
/*     */     throws DataException, ServiceException
/*     */   {
/* 594 */     String projectID = this.m_binder.getLocal("dProjectID");
/* 595 */     Projects.removeWorkMap(projectID);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void notifyEndPublish()
/*     */     throws DataException, ServiceException
/*     */   {
/* 602 */     String projectID = this.m_binder.getLocal("dProjectID");
/* 603 */     Hashtable workQueue = Projects.removeWorkMap(projectID);
/* 604 */     if (workQueue == null)
/*     */     {
/* 607 */       return;
/*     */     }
/*     */ 
/* 611 */     ExecutionContext context = this;
/* 612 */     ExecutionContext oldCtxt = context;
/* 613 */     DataBinder data = (DataBinder)oldCtxt.getCachedObject("DataBinder");
/* 614 */     DataBinder binder = new DataBinder();
/* 615 */     if (data != null)
/*     */     {
/* 617 */       binder.merge(data);
/* 618 */       binder.setEnvironment(data.getEnvironment());
/*     */     }
/*     */ 
/* 622 */     ExecutionContext newCtxt = null;
/*     */     try
/*     */     {
/* 627 */       if (context instanceof Service)
/*     */       {
/* 629 */         Service service = (Service)context;
/* 630 */         ServiceData serviceData = service.getServiceData();
/* 631 */         Workspace ws = service.getWorkspace();
/*     */ 
/* 633 */         Service newService = ServiceManager.createService(serviceData.m_classID, ws, null, binder, serviceData);
/*     */ 
/* 635 */         newService.initDelegatedObjects();
/*     */ 
/* 637 */         newCtxt = newService;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 642 */       Report.trace("workflow", "Unable to create service object for sending mail. Defaulting to a generic execution context adaptor.", e);
/*     */     }
/*     */ 
/* 646 */     if (newCtxt == null)
/*     */     {
/* 648 */       newCtxt = new ExecutionContextAdaptor();
/*     */     }
/*     */ 
/* 651 */     ExecutionContext ctxt = newCtxt;
/* 652 */     Workspace workspace = this.m_workspace;
/*     */ 
/* 658 */     ctxt.setCachedObject("DataBinder", binder);
/* 659 */     ctxt.setCachedObject("PageMerger", new PageMerger(binder, ctxt));
/* 660 */     ctxt.setCachedObject("UserData", oldCtxt.getCachedObject("UserData"));
/*     */ 
/* 662 */     Runnable run = new Runnable(workQueue, workspace, ctxt, binder)
/*     */     {
/*     */       public void run()
/*     */       {
/* 666 */         for (Enumeration en = this.val$workQueue.keys(); en.hasMoreElements(); )
/*     */         {
/* 668 */           String user = (String)en.nextElement();
/* 669 */           Vector docs = (Vector)this.val$workQueue.get(user);
/*     */           try
/*     */           {
/* 673 */             UserData userData = UserStorage.retrieveUserDatabaseProfileData(user, this.val$workspace, this.val$ctxt);
/* 674 */             String mailStr = userData.getProperty("dEmail");
/* 675 */             if ((mailStr == null) || (mailStr.length() == 0))
/*     */             {
/* 715 */               this.val$workspace.releaseConnection();
/* 716 */               if (this.val$ctxt instanceof Service);
/* 718 */               ((Service)this.val$ctxt).clear();
/*     */             }
/* 680 */             DataResultSet docSet = null;
/* 681 */             int num = docs.size();
/* 682 */             for (int i = 0; i < num; ++i)
/*     */             {
/* 684 */               Properties props = (Properties)docs.elementAt(i);
/* 685 */               DataResultSet drset = ResultSetUtils.createResultSetFromProperties(props);
/* 686 */               if (docSet == null)
/*     */               {
/* 688 */                 docSet = drset;
/*     */               }
/*     */               else
/*     */               {
/* 692 */                 docSet.merge(null, drset, false);
/*     */               }
/*     */             }
/* 695 */             if (docSet != null)
/*     */             {
/* 697 */               docSet.first();
/* 698 */               this.val$binder.addResultSet("CollatedDocs", docSet);
/*     */ 
/* 700 */               String subjectTmp = "<$include wf_approve_mail_subject$>";
/* 701 */               if (num > 1)
/*     */               {
/* 703 */                 subjectTmp = "<$exec isMulti=1$>" + subjectTmp;
/*     */               }
/* 705 */               InternetFunctions.sendMailTo(mailStr, "REVIEWER_MAIL", subjectTmp, this.val$ctxt);
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 711 */             Report.error(null, null, e);
/*     */           }
/*     */           finally
/*     */           {
/* 715 */             this.val$workspace.releaseConnection();
/* 716 */             if (this.val$ctxt instanceof Service)
/*     */             {
/* 718 */               ((Service)this.val$ctxt).clear();
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     };
/* 727 */     Date dte = new Date();
/* 728 */     Thread bgThread = new Thread(run, "notify_publish_" + dte.getTime());
/* 729 */     bgThread.setDaemon(true);
/* 730 */     bgThread.start();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 735 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ProjectService
 * JD-Core Version:    0.5.4
 */