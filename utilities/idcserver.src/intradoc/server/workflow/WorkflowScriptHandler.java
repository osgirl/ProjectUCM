/*     */ package intradoc.server.workflow;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.serialize.DataBinderLocalizer;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.workflow.WfScriptStorage;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.shared.workflow.WorkflowData;
/*     */ import intradoc.shared.workflow.WorkflowInfo;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import intradoc.util.WriterToIdcAppendable;
/*     */ import java.io.IOException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowScriptHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void getWorkflowScript()
/*     */     throws DataException, ServiceException
/*     */   {
/*  42 */     String name = this.m_binder.get("wfScriptName");
/*  43 */     WfScriptStorage wfScript = WfScriptManager.getOrCreateScriptData(name);
/*     */ 
/*  45 */     this.m_binder.merge(wfScript.m_scriptData);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addWorkflowScript() throws DataException, ServiceException
/*     */   {
/*  51 */     addOrEditScript(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editWorkflowScript() throws DataException, ServiceException
/*     */   {
/*  57 */     addOrEditScript(false);
/*     */   }
/*     */ 
/*     */   protected void addOrEditScript(boolean isNew) throws DataException, ServiceException
/*     */   {
/*  62 */     String scriptName = this.m_binder.get("wfScriptName");
/*  63 */     String dir = WfScriptManager.m_scriptDir;
/*  64 */     FileUtils.reserveDirectory(dir, true);
/*     */     try
/*     */     {
/*  68 */       DataBinder binder = WfScriptManager.readScriptsFile();
/*  69 */       DataResultSet wfScripts = SharedObjects.getTable("WorkflowScripts");
/*     */ 
/*  71 */       this.m_binder.addResultSet("WorkflowScripts", wfScripts);
/*     */ 
/*  74 */       Vector values = wfScripts.findRow(0, scriptName);
/*  75 */       if ((isNew) && (values != null))
/*     */       {
/*  77 */         String errMsg = LocaleUtils.encodeMessage("csWfScriptAlreadyExists", null, scriptName);
/*  78 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/*     */ 
/*  82 */       DataBinder scriptData = WfScriptManager.prepareScriptForSave(this.m_binder);
/*  83 */       WfScriptManager.writeScript(scriptName, scriptData);
/*     */ 
/*  86 */       String summary = WorkflowScriptUtils.computeScriptString("", "", scriptData, true);
/*  87 */       this.m_binder.putLocal("wfScriptSummary", summary);
/*     */ 
/*  89 */       Vector row = wfScripts.createRow(this.m_binder);
/*  90 */       if (values == null)
/*     */       {
/*  92 */         wfScripts.addRow(row);
/*     */       }
/*     */       else
/*     */       {
/*  96 */         int index = wfScripts.getCurrentRow();
/*  97 */         wfScripts.setRowValues(row, index);
/*     */       }
/*     */ 
/* 101 */       binder.addResultSet("WorkflowScripts", wfScripts);
/* 102 */       WfScriptManager.writeScripts(binder);
/*     */     }
/*     */     finally
/*     */     {
/* 106 */       FileUtils.releaseDirectory(dir, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteWorkflowScript() throws DataException, ServiceException
/*     */   {
/* 113 */     String scriptName = this.m_binder.get("wfScriptName");
/* 114 */     String dir = WfScriptManager.m_scriptDir;
/* 115 */     FileUtils.reserveDirectory(dir, true);
/*     */     try
/*     */     {
/* 119 */       DataBinder binder = WfScriptManager.readScriptsFile();
/* 120 */       DataResultSet wfScripts = SharedObjects.getTable("WorkflowScripts");
/*     */ 
/* 122 */       this.m_binder.addResultSet("WorkflowScripts", wfScripts);
/*     */ 
/* 125 */       Vector values = wfScripts.findRow(0, scriptName);
/* 126 */       if (values != null)
/*     */       {
/* 128 */         wfScripts.deleteCurrentRow();
/*     */ 
/* 131 */         binder.addResultSet("WorkflowScripts", wfScripts);
/* 132 */         WfScriptManager.writeScriptsEx(binder, false);
/*     */ 
/* 134 */         FileUtils.deleteFile(dir + scriptName.toLowerCase() + ".hda");
/*     */ 
/* 137 */         WfScriptManager.loadScripts(binder);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 142 */       FileUtils.releaseDirectory(dir, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getCompanionHistory() throws DataException, ServiceException
/*     */   {
/* 149 */     String docName = this.m_binder.get("dDocName");
/* 150 */     String subDir = this.m_binder.get("dWfDirectory");
/*     */ 
/* 152 */     WfCompanionData wfData = WfCompanionManager.getOrCreateCompanionData(docName, subDir, this.m_workspace, this.m_binder);
/*     */ 
/* 154 */     this.m_service.setCachedObject("WorkflowCompanionData", wfData);
/*     */ 
/* 156 */     ResultSet drset = wfData.m_data.getResultSet("WorkflowActionHistory");
/* 157 */     if (drset == null)
/*     */       return;
/* 159 */     this.m_binder.addResultSet("WorkflowActionHistory", drset);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadWfCompanionInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/* 167 */     String docName = this.m_binder.get("dDocName");
/* 168 */     String subDir = this.m_binder.get("dWfDirectory");
/* 169 */     WfCompanionData wfData = WfCompanionManager.getOrCreateCompanionData(docName, subDir, this.m_workspace, this.m_binder);
/*     */ 
/* 172 */     IdcStringBuilder builder = new IdcStringBuilder();
/*     */     try
/*     */     {
/* 175 */       wfData.m_data.send(new WriterToIdcAppendable(builder));
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 179 */       String errMsg = LocaleUtils.encodeMessage("csWfUnableToReadCompanion", null, e.getMessage());
/* 180 */       this.m_service.createServiceException(null, errMsg);
/*     */     }
/*     */ 
/* 183 */     this.m_binder.putLocal("WfCompanionData", builder.toString());
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void testWorkflowScript() throws DataException, ServiceException
/*     */   {
/* 189 */     String type = "";
/* 190 */     String prefix = "";
/* 191 */     String scriptStr = WorkflowScriptUtils.computeScriptString(type, prefix, this.m_binder, false);
/*     */ 
/* 200 */     Object oldMailQueue = this.m_service.getCachedObject("MailQueue");
/* 201 */     Vector mailQueue = new IdcVector();
/* 202 */     this.m_service.setCachedObject("MailQueue", mailQueue);
/*     */ 
/* 204 */     DataBinder resultData = new DataBinder();
/* 205 */     this.m_service.setCachedObject("WorkflowScriptResult", resultData);
/*     */ 
/* 208 */     String inputStr = this.m_binder.getLocal("ScriptInput");
/* 209 */     Properties inProps = new Properties();
/* 210 */     WorkflowScriptUtils.parseScriptInput(inputStr, inProps);
/*     */ 
/* 212 */     WfCompanionData wfCmpData = new WfCompanionData("test", null);
/* 213 */     DataBinder cmpData = new DataBinder();
/* 214 */     cmpData.setLocalData(inProps);
/* 215 */     wfCmpData.m_data = cmpData;
/* 216 */     this.m_service.setCachedObject("WorkflowCompanionData", wfCmpData);
/*     */ 
/* 219 */     String[] info = null;
/* 220 */     boolean isTemplate = StringUtils.convertToBool(this.m_binder.getLocal("IsTemplateScript"), false);
/* 221 */     boolean isNewStep = false;
/* 222 */     if (isTemplate)
/*     */     {
/* 224 */       String wfStep = inProps.getProperty("WorkflowStepForTest");
/* 225 */       if (wfStep != null)
/*     */       {
/* 227 */         info = WorkflowScriptUtils.parseTarget(wfStep);
/* 228 */         this.m_binder.putLocal("dWfStepName", info[0]);
/* 229 */         this.m_binder.putLocal("dWfName", info[1]);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 234 */       info = new String[2];
/* 235 */       info[0] = this.m_binder.getLocal("dWfStepName");
/* 236 */       info[1] = this.m_binder.getLocal("dWfName");
/* 237 */       isNewStep = StringUtils.convertToBool(this.m_binder.getLocal("IsNewStep"), false);
/*     */     }
/*     */ 
/* 240 */     if (info != null)
/*     */     {
/* 242 */       WorkflowData wfData = (WorkflowData)SharedObjects.getTable(WorkflowData.m_tableName);
/* 243 */       WorkflowInfo workflowInfo = wfData.getWorkflowInfo(info[1]);
/* 244 */       if (workflowInfo == null)
/*     */       {
/* 246 */         String errMsg = LocaleUtils.encodeMessage("csWfUnableToRetrieveInfo", null, info[1]);
/* 247 */         throw new DataException(errMsg);
/*     */       }
/*     */ 
/* 250 */       this.m_binder.putLocal("dWfID", "" + workflowInfo.m_wfID);
/*     */ 
/* 253 */       WfStepData stepData = WorkflowUtils.determineSteps(this.m_workspace, this.m_binder);
/* 254 */       if (isNewStep)
/*     */       {
/* 257 */         stepData.setCurrentRow(stepData.getNumRows() - 1);
/* 258 */         String str = stepData.getStepID();
/* 259 */         int id = NumberUtils.parseInteger(str, 1) + 1;
/* 260 */         this.m_binder.putLocal("dWfStepID", "" + id);
/*     */ 
/* 262 */         Vector row = stepData.createRow(this.m_binder);
/* 263 */         stepData.addRow(row);
/* 264 */         stepData.setCurrentRow(stepData.getNumRows() - 1);
/*     */       }
/*     */       else
/*     */       {
/* 269 */         boolean isFound = false;
/* 270 */         if (stepData.isRowPresent())
/*     */         {
/* 272 */           int index = ResultSetUtils.getIndexMustExist(stepData, "dWfStepName");
/* 273 */           Vector row = stepData.findRow(index, info[0]);
/* 274 */           isFound = row != null;
/*     */         }
/* 276 */         if (!isFound)
/*     */         {
/* 278 */           String errMsg = LocaleUtils.encodeMessage("csWfMissingStep", null, info[0], info[1]);
/*     */ 
/* 280 */           this.m_service.createServiceException(null, errMsg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 285 */       String str = wfCmpData.m_data.getLocal("wfParentList");
/* 286 */       Vector parents = StringUtils.parseArray(str, '#', '^');
/* 287 */       this.m_service.setCachedObject("WorkflowParents", parents);
/*     */ 
/* 290 */       this.m_service.setCachedObject("WorkflowInfo", workflowInfo);
/* 291 */       this.m_service.setCachedObject("WorkflowSteps", stepData);
/* 292 */       this.m_service.setCachedObject("WorkflowPrefix", info[0] + "@" + info[1]);
/*     */     }
/*     */     else
/*     */     {
/* 297 */       Properties props = new Properties();
/* 298 */       props.put("dWfName", "test");
/* 299 */       props.put("dWfID", "1");
/* 300 */       props.put("dWfStatus", "INIT");
/* 301 */       props.put("dWfType", "criteria");
/* 302 */       WorkflowInfo workflowInfo = new WorkflowInfo(props);
/* 303 */       WfStepData stepData = new WfStepData();
/*     */ 
/* 306 */       this.m_service.setCachedObject("WorkflowInfo", workflowInfo);
/* 307 */       this.m_service.setCachedObject("WorkflowSteps", stepData);
/* 308 */       this.m_service.setCachedObject("WorkflowPrefix", "");
/* 309 */       this.m_service.setCachedObject("WorkflowParents", new IdcVector());
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 314 */       PageMerger pageMerger = (PageMerger)this.m_service.getCachedObject("PageMerger");
/* 315 */       this.m_service.setConditionVar("allowWorkflowIdocScript", true);
/* 316 */       pageMerger.evaluateScriptReportError(scriptStr);
/*     */ 
/* 319 */       Vector p = (Vector)this.m_service.getCachedObject("WorkflowParents");
/* 320 */       resultData.putLocal("wfParentList", StringUtils.createString(p, '#', '^'));
/*     */ 
/* 323 */       PropParameters params = new PropParameters(null);
/* 324 */       DataResultSet drset = new DataResultSet(new String[] { "mailAddress", "mailTemplate", "mailSubject" });
/* 325 */       resultData.addResultSet("MailQueue", drset);
/*     */ 
/* 327 */       int size = mailQueue.size();
/* 328 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 330 */         params.m_properties = ((Properties)mailQueue.elementAt(i));
/* 331 */         Vector row = drset.createRow(params);
/* 332 */         drset.addRow(row);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 337 */       String isScriptAbort = this.m_binder.getLocal("isReportToErrorPage");
/* 338 */       String errMsg = "";
/* 339 */       if (StringUtils.convertToBool(isScriptAbort, false))
/*     */       {
/* 341 */         errMsg = "!csWfAbortScript";
/*     */       }
/*     */       else
/*     */       {
/* 345 */         errMsg = "!csWfScriptTestExeError";
/*     */       }
/* 347 */       resultData.putLocal("StatusCode", "-1");
/* 348 */       resultData.putLocal("StatusMessageKey", errMsg + e.getMessage());
/* 349 */       resultData.putLocal("StatusMessage", errMsg + e.getMessage());
/* 350 */       resultData.removeLocal("isReportToErrorPage");
/*     */     }
/*     */     finally
/*     */     {
/* 354 */       this.m_service.setConditionVar("allowWorkflowIdocScript", false);
/* 355 */       sendResults(resultData);
/* 356 */       if (oldMailQueue != null)
/*     */       {
/* 358 */         this.m_service.setCachedObject("MailQueue", oldMailQueue);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void sendResults(DataBinder resultData) throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 367 */       resultData.setFieldType("StatusMessage", "message");
/* 368 */       DataBinderLocalizer localizer = new DataBinderLocalizer(resultData, this.m_service);
/* 369 */       localizer.localizeBinder(3);
/*     */ 
/* 371 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 372 */       resultData.send(new WriterToIdcAppendable(builder));
/* 373 */       this.m_binder.putLocal("ScriptResults", builder.toString());
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 377 */       this.m_service.createServiceException(e, "!csWfScriptTestSendError");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getWorkflowDesignInfo()
/*     */     throws DataException, ServiceException
/*     */   {
/* 388 */     String wfName = this.m_binder.get("dWfName");
/* 389 */     WfDesignData wfDesign = WfDesignManager.getWorkflowDesign(wfName);
/*     */ 
/* 391 */     this.m_binder.merge(wfDesign.m_designData);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addWorkflowStepScript() throws DataException, ServiceException
/*     */   {
/* 397 */     addOrEditStepScript(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editWorkflowStepScript() throws DataException, ServiceException
/*     */   {
/* 403 */     addOrEditStepScript(false);
/*     */   }
/*     */ 
/*     */   public void addOrEditStepScript(boolean isAdd) throws DataException, ServiceException
/*     */   {
/* 408 */     String wfName = this.m_binder.get("dWfName");
/* 409 */     String stepName = this.m_binder.get("dWfStepName");
/*     */ 
/* 411 */     FileUtils.reserveDirectory(WfDesignManager.m_designDir);
/*     */     try
/*     */     {
/* 414 */       WfDesignData wfDesign = WfDesignManager.getWorkflowDesign(wfName);
/* 415 */       DataBinder binder = wfDesign.m_designData;
/* 416 */       DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowStepEvents");
/* 417 */       if (drset == null)
/*     */       {
/* 419 */         drset = new DataResultSet(WorkflowScriptUtils.WF_EVENT_COLUMNS);
/* 420 */         binder.addResultSet("WorkflowStepEvents", drset);
/*     */       }
/* 422 */       Vector row = drset.findRow(0, stepName);
/* 423 */       boolean isAppend = row == null;
/*     */ 
/* 426 */       row = drset.createRow(this.m_binder);
/*     */ 
/* 428 */       if (isAppend)
/*     */       {
/* 430 */         drset.addRow(row);
/*     */       }
/*     */       else
/*     */       {
/* 434 */         int index = drset.getCurrentRow();
/* 435 */         drset.setRowValues(row, index);
/*     */       }
/*     */ 
/* 438 */       WorkflowScriptUtils.exchangeScriptStepInfo(stepName, this.m_binder, binder, true, false);
/*     */ 
/* 440 */       WorkflowScriptUtils.updateWorkflowStepCondition(stepName, this.m_binder, binder);
/*     */ 
/* 442 */       WfDesignManager.writeWorkflowDesign(wfName, binder);
/*     */     }
/*     */     finally
/*     */     {
/* 446 */       FileUtils.releaseDirectory(WfDesignManager.m_designDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteWorkflowStepScript() throws DataException, ServiceException
/*     */   {
/* 453 */     String wfName = this.m_binder.get("dWfName");
/* 454 */     String stepName = this.m_binder.get("dWfStepName");
/*     */ 
/* 456 */     FileUtils.reserveDirectory(WfDesignManager.m_designDir, true);
/*     */     try
/*     */     {
/* 459 */       WfDesignData wfDesign = WfDesignManager.getWorkflowDesign(wfName);
/* 460 */       DataBinder binder = wfDesign.m_designData;
/* 461 */       DataResultSet drset = (DataResultSet)binder.getResultSet("WorkflowStepEvents");
/* 462 */       if (drset == null)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 467 */       Vector row = drset.findRow(0, stepName);
/* 468 */       if (row == null) {
/*     */         return;
/*     */       }
/*     */ 
/* 472 */       drset.deleteCurrentRow();
/*     */ 
/* 475 */       String[][] events = WorkflowScriptUtils.COLUMN_EVENT_MAP;
/* 476 */       int len = events.length;
/* 477 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 479 */         String event = events[i][1];
/*     */ 
/* 482 */         WorkflowScriptUtils.removeStepScriptInfo(binder, stepName, event);
/*     */       }
/*     */ 
/* 485 */       deleteWorkflowStepConditions(stepName, binder);
/*     */ 
/* 487 */       WfDesignManager.writeWorkflowDesign(wfName, binder);
/*     */     }
/*     */     finally
/*     */     {
/* 491 */       FileUtils.releaseDirectory(WfDesignManager.m_designDir, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteWfDesign() throws DataException, ServiceException
/*     */   {
/* 498 */     String wfName = this.m_binder.get("dWfName");
/* 499 */     FileUtils.reserveDirectory(WfDesignManager.m_designDir);
/*     */     try
/*     */     {
/* 502 */       WfDesignManager.deleteWorkflowDesign(wfName);
/*     */     }
/*     */     finally
/*     */     {
/* 506 */       FileUtils.releaseDirectory(WfDesignManager.m_designDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteWorkflowStepConditions(String stepName, DataBinder binder)
/*     */   {
/* 515 */     Properties props = binder.getLocalData();
/* 516 */     Properties oldProps = (Properties)props.clone();
/* 517 */     for (Enumeration en = oldProps.keys(); en.hasMoreElements(); )
/*     */     {
/* 519 */       String key = (String)en.nextElement();
/* 520 */       if (key.startsWith(stepName + ":"))
/*     */       {
/* 522 */         binder.removeLocal(key);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addWorkflowToken()
/*     */     throws DataException, ServiceException
/*     */   {
/* 533 */     addOrEditToken(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editWorkflowToken() throws DataException, ServiceException
/*     */   {
/* 539 */     addOrEditToken(false);
/*     */   }
/*     */ 
/*     */   protected void addOrEditToken(boolean isNew) throws DataException, ServiceException
/*     */   {
/* 544 */     String tokenName = this.m_binder.get("wfTokenName");
/* 545 */     String dir = WfScriptManager.m_scriptDir;
/* 546 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 550 */       DataBinder binder = WfScriptManager.loadTokens();
/* 551 */       DataResultSet wfTokens = SharedObjects.getTable("WorkflowTokens");
/*     */ 
/* 553 */       this.m_binder.addResultSet("WorkflowTokens", wfTokens);
/*     */ 
/* 556 */       Vector values = wfTokens.findRow(0, tokenName);
/* 557 */       if ((isNew) && (values != null))
/*     */       {
/* 559 */         String errMsg = LocaleUtils.encodeMessage("csWfTokenExists", null, tokenName);
/* 560 */         this.m_service.createServiceException(null, errMsg);
/*     */       }
/*     */ 
/* 563 */       Vector row = wfTokens.createRow(this.m_binder);
/* 564 */       if (values == null)
/*     */       {
/* 566 */         wfTokens.addRow(row);
/*     */       }
/*     */       else
/*     */       {
/* 570 */         int index = wfTokens.getCurrentRow();
/* 571 */         wfTokens.setRowValues(row, index);
/*     */       }
/*     */ 
/* 575 */       binder.addResultSet("WorkflowTokens", wfTokens);
/* 576 */       WfScriptManager.writeTokens(binder);
/*     */     }
/*     */     finally
/*     */     {
/* 580 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteWorkflowToken() throws DataException, ServiceException
/*     */   {
/* 587 */     String tokenName = this.m_binder.get("wfTokenName");
/*     */ 
/* 589 */     String dir = WfScriptManager.m_tokenDir;
/* 590 */     FileUtils.reserveDirectory(WfScriptManager.m_tokenDir);
/*     */     try
/*     */     {
/* 593 */       DataBinder binder = WfScriptManager.loadTokens();
/* 594 */       DataResultSet wfTokens = SharedObjects.getTable("WorkflowTokens");
/*     */ 
/* 596 */       this.m_binder.addResultSet("WorkflowTokens", wfTokens);
/*     */ 
/* 599 */       Vector values = wfTokens.findRow(0, tokenName);
/* 600 */       if (values != null)
/*     */       {
/* 602 */         wfTokens.deleteCurrentRow();
/*     */ 
/* 605 */         binder.addResultSet("WorkflowTokens", wfTokens);
/* 606 */         WfScriptManager.writeTokens(binder);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 611 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 617 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.workflow.WorkflowScriptHandler
 * JD-Core Version:    0.5.4
 */