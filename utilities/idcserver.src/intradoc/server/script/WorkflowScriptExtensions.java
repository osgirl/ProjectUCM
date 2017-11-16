/*     */ package intradoc.server.script;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptExtensionsAdaptor;
/*     */ import intradoc.common.ScriptInfo;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DocService;
/*     */ import intradoc.server.FileService;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.WorkflowService;
/*     */ import intradoc.server.workflow.WfCompanionData;
/*     */ import intradoc.server.workflow.WfDesignData;
/*     */ import intradoc.server.workflow.WfDesignManager;
/*     */ import intradoc.server.workflow.WorkflowStates;
/*     */ import intradoc.server.workflow.WorkflowUtils;
/*     */ import intradoc.shared.AliasData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.workflow.JumpClausesData;
/*     */ import intradoc.shared.workflow.WfStepData;
/*     */ import intradoc.shared.workflow.WorkflowInfo;
/*     */ import intradoc.shared.workflow.WorkflowScriptUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkflowScriptExtensions extends ScriptExtensionsAdaptor
/*     */ {
/*     */   public WorkflowScriptExtensions()
/*     */   {
/*  39 */     this.m_variableTable = new String[] { "WfStart" };
/*     */ 
/*  44 */     this.m_functionTable = new String[] { "wfSet", "wfGet", "wfCurrentSet", "wfCurrentGet", "wfCurrentStep", "wfExit", "wfNotify", "wfAddUser", "wfUpdateMetaData", "wfLoadDesign", "wfDisplayCondition", "wfReleaseDocument", "wfIsReleasable", "wfGetStepTypeLabel", "wfAddActionHistoryEvent", "wfComputeStepUserList", "wfIsFinishedDocConversion", "wfIsNotifyingUsers", "wfSetIsNotifyingUsers", "wfComputeAllowedActions", "wfSetSuppressNotification", "wfIsNotificationSuppressed" };
/*     */ 
/*  76 */     this.m_functionDefinitionTable = new int[][] { { 0, 2, 0, 0, -1 }, { 1, 1, 0, -1, 0 }, { 2, 2, 0, 0, -1 }, { 3, 1, 0, -1, 0 }, { 4, 1, 1, -1, 0 }, { 5, 2, 1, 1, -1 }, { 6, -1, 0, 0, -1 }, { 7, 2, 0, 0, -1 }, { 8, 2, 0, 0, 1 }, { 9, 1, 0, -1, 1 }, { 10, -1, 0, 0, -1 }, { 11, 0, -1, -1, -1 }, { 12, 1, -1, -1, 1 }, { 13, 1, 0, -1, 0 }, { 14, 3, 0, 0, -1 }, { 15, 0, -1, -1, 0 }, { 16, 0, -1, -1, 1 }, { 17, 0, -1, -1, 1 }, { 18, 1, 0, -1, -1 }, { 19, 0, -1, -1, 1 }, { 20, 1, 0, -1, -1 }, { 21, 0, -1, -1, 1 } };
/*     */   }
/*     */ 
/*     */   public boolean evaluateFunction(ScriptInfo info, Object[] args, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 107 */     int[] config = (int[])(int[])info.m_entry;
/* 108 */     String function = info.m_key;
/*     */ 
/* 110 */     int nargs = args.length - 1;
/* 111 */     int allowedParams = config[1];
/* 112 */     if ((allowedParams >= 0) && (allowedParams != nargs))
/*     */     {
/* 114 */       String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "" + allowedParams);
/*     */ 
/* 116 */       throw new IllegalArgumentException(msg);
/*     */     }
/*     */ 
/* 121 */     boolean checkIsFullWorkflowContext = (config[0] != 1) && (config[0] != 7) && (config[0] != 9) && (config[0] != 10) && (config[0] != 12) && (config[0] != 13);
/*     */ 
/* 127 */     boolean checkIsServiceOnly = config[0] == 19;
/* 128 */     Service service = getAndCheckService(function, "csScriptMustBeInService", "csScriptMustBeInWorkflowContext", checkIsFullWorkflowContext, checkIsServiceOnly, context);
/*     */ 
/* 132 */     DataBinder binder = service.getBinder();
/* 133 */     DataBinder wfResultData = (DataBinder)service.getCachedObject("WorkflowScriptResult");
/*     */ 
/* 138 */     String sArg1 = null;
/* 139 */     String sArg2 = null;
/* 140 */     long lArg1 = 0L;
/* 141 */     long lArg2 = 0L;
/* 142 */     if (nargs > 0)
/*     */     {
/* 144 */       if (config[2] == 0)
/*     */       {
/* 146 */         sArg1 = ScriptUtils.getDisplayString(args[0], context);
/*     */       }
/* 148 */       else if (config[2] == 1)
/*     */       {
/* 150 */         lArg1 = ScriptUtils.getLongVal(args[0], context);
/*     */       }
/*     */     }
/*     */ 
/* 154 */     if (nargs > 1)
/*     */     {
/* 156 */       if (config[3] == 0)
/*     */       {
/* 158 */         sArg2 = ScriptUtils.getDisplayString(args[1], context);
/*     */       }
/* 160 */       else if (config[3] == 1)
/*     */       {
/* 162 */         lArg2 = ScriptUtils.getLongVal(args[1], context);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 167 */     boolean bResult = false;
/* 168 */     int iResult = 0;
/* 169 */     double dResult = 0.0D;
/*     */ 
/* 171 */     Object oResult = null;
/*     */ 
/* 173 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 177 */       wfResultData.putLocal(sArg1, sArg2);
/* 178 */       break;
/*     */     case 1:
/* 184 */       if (wfResultData != null)
/*     */       {
/* 186 */         oResult = wfResultData.getAllowMissing(sArg1);
/*     */       }
/* 188 */       if (oResult == null)
/*     */       {
/* 190 */         WfCompanionData cmpData = (WfCompanionData)service.getCachedObject("WorkflowCompanionData");
/*     */ 
/* 192 */         if (cmpData != null)
/*     */         {
/* 194 */           oResult = cmpData.m_data.getAllowMissing(sArg1);
/*     */         }
/*     */         else
/*     */         {
/* 198 */           String errMsg = LocaleUtils.encodeMessage("csScriptMustBeInWorkflowContext", null, info.m_key);
/*     */ 
/* 200 */           throw new ServiceException(errMsg);
/*     */         }
/*     */       }
/* 202 */       break;
/*     */     case 2:
/* 207 */       String prefix = (String)service.getCachedObject("WorkflowPrefix");
/* 208 */       String key = prefix + ":" + sArg1;
/* 209 */       wfResultData.putLocal(key, sArg2);
/* 210 */       break;
/*     */     case 3:
/* 215 */       String prefix = (String)service.getCachedObject("WorkflowPrefix");
/* 216 */       String key = prefix + ":" + sArg1;
/* 217 */       oResult = wfResultData.getAllowMissing(key);
/*     */ 
/* 219 */       if (oResult == null)
/*     */       {
/* 221 */         WfCompanionData cmpData = (WfCompanionData)service.getCachedObject("WorkflowCompanionData");
/* 222 */         oResult = cmpData.m_data.getAllowMissing(key);
/* 223 */       }break;
/*     */     case 4:
/* 229 */       WorkflowInfo wfInfo = (WorkflowInfo)service.getCachedObject("WorkflowInfo");
/* 230 */       WfStepData stepSet = (WfStepData)service.getCachedObject("WorkflowSteps");
/* 231 */       if (stepSet.isRowPresent())
/*     */       {
/* 233 */         String type = wfInfo.m_wfType;
/* 234 */         boolean isSubWorkflow = type.equalsIgnoreCase("subworkflow");
/*     */ 
/* 238 */         int firstStep = (isSubWorkflow) ? 1 : 0;
/* 239 */         int curRow = stepSet.getCurrentRow();
/* 240 */         int nextRow = curRow + (int)lArg1;
/* 241 */         if (nextRow < firstStep)
/*     */         {
/* 244 */           nextRow = firstStep;
/*     */         }
/* 246 */         else if (nextRow >= stepSet.getNumRows())
/*     */         {
/* 249 */           nextRow = stepSet.getNumRows() - 1;
/*     */         }
/*     */ 
/* 252 */         String wfName = binder.getLocal("dWfName");
/* 253 */         stepSet.setCurrentRow(nextRow);
/* 254 */         String stepName = stepSet.getStringValue(0);
/* 255 */         oResult = stepName + "@" + wfName;
/*     */ 
/* 258 */         stepSet.setCurrentRow(curRow);
/*     */       }
/*     */       else
/*     */       {
/* 263 */         oResult = service.getCachedObject("WorkflowPrefix");
/*     */       }
/* 265 */       break;
/*     */     case 5:
/* 271 */       Vector parents = (Vector)service.getCachedObject("WorkflowParents");
/* 272 */       wfResultData.putLocal("isWfExit", "1");
/*     */ 
/* 274 */       Report.trace("workflow", "Workflow wfExit() function called", null);
/*     */ 
/* 277 */       int count = 0;
/* 278 */       int numParents = parents.size();
/* 279 */       String parent = null;
/* 280 */       for (int i = 0; i < numParents; ++i)
/*     */       {
/* 282 */         String str = (String)parents.elementAt(0);
/* 283 */         parents.removeElementAt(0);
/*     */ 
/* 285 */         if (str.charAt(0) == '*')
/*     */         {
/* 287 */           ++count;
/*     */         }
/* 289 */         if (count - 1 != lArg1)
/*     */           continue;
/* 291 */         parent = str.substring(1);
/* 292 */         break;
/*     */       }
/*     */ 
/* 296 */       String exitStep = null;
/* 297 */       if (parent != null)
/*     */       {
/* 299 */         if (lArg2 == 0L)
/*     */         {
/* 302 */           exitStep = parent;
/*     */         }
/*     */         else
/*     */         {
/*     */           try
/*     */           {
/* 309 */             String[] target = WorkflowScriptUtils.parseTarget(parent);
/* 310 */             DataBinder rBinder = new DataBinder();
/* 311 */             rBinder.putLocal("dWfName", target[1]);
/* 312 */             rBinder.putLocal("dWfStepName", target[0]);
/*     */ 
/* 315 */             Workspace ws = service.getWorkspace();
/* 316 */             ResultSet rset = ws.createResultSet("Qworkflow", rBinder);
/* 317 */             String type = ResultSetUtils.getValue(rset, "dWfType");
/* 318 */             boolean isSubWorkflow = type.equalsIgnoreCase("subworkflow");
/*     */ 
/* 320 */             rset = ws.createResultSet("QworkflowSteps", rBinder);
/* 321 */             DataResultSet drset = new DataResultSet();
/* 322 */             drset.copy(rset);
/*     */ 
/* 324 */             int stepIndex = ResultSetUtils.getIndexMustExist(drset, "dWfStepName");
/* 325 */             Vector row = drset.findRow(stepIndex, target[0]);
/* 326 */             if (row != null)
/*     */             {
/* 329 */               int firstRow = (isSubWorkflow) ? 1 : 0;
/* 330 */               int curRow = drset.getCurrentRow();
/* 331 */               int nextRow = curRow + (int)lArg2;
/* 332 */               int numRows = drset.getNumRows();
/* 333 */               if ((nextRow < numRows) && (nextRow >= firstRow))
/*     */               {
/* 335 */                 wfResultData.addResultSet("WorkflowSteps", drset);
/* 336 */                 drset.setCurrentRow(nextRow);
/* 337 */                 String stepName = drset.getStringValue(stepIndex);
/* 338 */                 exitStep = stepName + "@" + target[1];
/*     */               }
/*     */             }
/*     */ 
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 345 */             Report.trace(null, "In wfExit, unable to compute step to go to.", e);
/*     */           }
/*     */         }
/*     */ 
/* 349 */         if (exitStep == null)
/*     */         {
/* 352 */           numParents = parents.size();
/* 353 */           for (int i = 0; i < numParents; ++i)
/*     */           {
/* 355 */             String str = (String)parents.elementAt(0);
/* 356 */             parents.removeElementAt(0);
/* 357 */             if (str.charAt(0) != '*') {
/*     */               continue;
/*     */             }
/* 360 */             exitStep = str.substring(1);
/* 361 */             break;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 366 */         if (exitStep != null)
/*     */         {
/* 368 */           wfResultData.putLocal("wfExitStep", exitStep); } 
/* 368 */       }break;
/*     */     case 6:
/* 375 */       if (nargs < 2)
/*     */       {
/* 377 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "2");
/*     */ 
/* 379 */         throw new IllegalArgumentException(msg);
/*     */       }
/*     */ 
/* 382 */       String nameStr = sArg1;
/* 383 */       String type = sArg2;
/*     */ 
/* 386 */       String oldTemplate = binder.getLocal("wfMailTemplate");
/* 387 */       if (nargs > 2)
/*     */       {
/* 389 */         binder.putLocal("wfMailTemplate", ScriptUtils.getDisplayString(args[2], context));
/*     */       }
/*     */ 
/* 392 */       Vector users = new IdcVector();
/* 393 */       AliasData aliasData = (AliasData)SharedObjects.getTable(AliasData.m_tableName);
/* 394 */       WfStepData stepData = (WfStepData)service.getCachedObject("WorkflowSteps");
/* 395 */       WfCompanionData cmpData = (WfCompanionData)service.getCachedObject("WorkflowCompanionData");
/* 396 */       WorkflowInfo wfInfo = (WorkflowInfo)service.getCachedObject("WorkflowInfo");
/*     */ 
/* 398 */       Boolean bUseLocal = (Boolean)service.getCachedObject("WorkflowUseLocal");
/* 399 */       boolean useLocal = false;
/* 400 */       if (bUseLocal != null)
/*     */       {
/* 402 */         useLocal = bUseLocal.booleanValue();
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 407 */         Vector names = StringUtils.parseArrayEx(nameStr, ',', '^', true);
/* 408 */         int num = names.size();
/* 409 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 411 */           String name = (String)names.elementAt(i);
/*     */           try
/*     */           {
/* 414 */             WorkflowUtils.createUserList(users, name, type, aliasData, false, true, useLocal, service.getWorkspace(), service);
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 421 */             String msg = LocaleUtils.encodeMessage("csScriptUnableToNotifyUsers", null, stepData.getStepName(), binder.getLocal("dWfName"));
/*     */ 
/* 423 */             Report.error(null, msg, e);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 428 */         Properties props = WorkflowUtils.buildInformStepUsers(users, binder, stepData, service, cmpData, wfInfo);
/* 429 */         if (props != null)
/*     */         {
/* 432 */           Vector mailQueue = (Vector)service.getCachedObject("MailQueue");
/* 433 */           mailQueue.addElement(props);
/*     */         }
/*     */       }
/*     */       finally
/*     */       {
/* 438 */         if ((oldTemplate != null) && (oldTemplate.length() > 0))
/*     */         {
/* 440 */           binder.putLocal("wfMailTemplate", oldTemplate);
/*     */         }
/*     */         else
/*     */         {
/* 444 */           binder.removeLocal("wfMailTemplate");
/*     */         }
/*     */       }
/* 447 */       break;
/*     */     case 7:
/* 451 */       String tokenUsers = binder.getLocal("tokenUsers");
/* 452 */       Vector users = StringUtils.parseArrayEx(sArg1, ',', '^', true);
/*     */ 
/* 454 */       Vector v = new IdcVector(2);
/* 455 */       int num = users.size();
/* 456 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 458 */         String user = (String)users.elementAt(i);
/* 459 */         if (user.length() == 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 463 */         String type = sArg2;
/* 464 */         if (user.charAt(0) == '@')
/*     */         {
/* 466 */           type = "alias";
/* 467 */           user = user.substring(1);
/*     */         }
/*     */ 
/* 470 */         v.removeAllElements();
/* 471 */         v.addElement(user);
/* 472 */         v.addElement(type);
/*     */ 
/* 474 */         if ((tokenUsers == null) || (tokenUsers.length() == 0))
/*     */         {
/* 476 */           tokenUsers = "";
/*     */         }
/*     */         else
/*     */         {
/* 480 */           tokenUsers = tokenUsers + ",";
/*     */         }
/* 482 */         tokenUsers = tokenUsers + StringUtils.createString(v, ',', '^');
/*     */       }
/* 484 */       if (tokenUsers != null)
/*     */       {
/* 486 */         binder.putLocal("tokenUsers", tokenUsers); } break;
/*     */     case 8:
/* 499 */       Properties oldData = binder.getLocalData();
/* 500 */       Properties newData = (Properties)oldData.clone();
/* 501 */       DataBinder temp = new DataBinder();
/* 502 */       temp.copyResultSetStateShallow(binder);
/*     */ 
/* 505 */       WorkflowInfo wfInfo = (WorkflowInfo)service.getCachedObject("WorkflowInfo");
/* 506 */       Workspace ws = service.getWorkspace();
/*     */ 
/* 508 */       boolean isCgiEncode = binder.m_isCgi;
/*     */       try
/*     */       {
/* 511 */         binder.setLocalData(newData);
/* 512 */         binder.putLocal(sArg1, sArg2);
/*     */ 
/* 514 */         ResultSet rs = ws.createResultSet("QdocID", binder);
/* 515 */         if (rs.isEmpty())
/*     */         {
/* 517 */           String msg = LocaleUtils.encodeMessage("csWfUpdateMetaDataHasNoDocToUpdate", null, wfInfo.m_wfName);
/* 518 */           throw new ServiceException(msg);
/*     */         }
/* 520 */         String oldGroup = wfInfo.m_properties.getProperty("dSecurityGroup");
/* 521 */         String docGroup = ResultSetUtils.getValue(rs, "dSecurityGroup");
/* 522 */         if ((oldGroup == null) || (!oldGroup.equalsIgnoreCase(docGroup)))
/*     */         {
/* 524 */           String msg = LocaleUtils.encodeMessage("csWfUpdateMetaDataDocHasDifferentGroup", null, wfInfo.m_wfName, ResultSetUtils.getValue(rs, "dDocName"));
/*     */ 
/* 526 */           throw new ServiceException(msg);
/*     */         }
/* 528 */         rs.closeInternals();
/*     */ 
/* 530 */         binder.m_isCgi = true;
/* 531 */         service.executeService("UPDATE_METADATA");
/* 532 */         oResult = "";
/*     */ 
/* 535 */         oldData.put(sArg1, sArg2);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/*     */         String statusCode;
/*     */         String statusMsg;
/*     */         String statusMsgKey;
/* 545 */         String statusCode = newData.getProperty("StatusCode");
/* 546 */         if (statusCode != null)
/*     */         {
/* 548 */           oldData.put("StatusCode", statusCode);
/*     */         }
/* 550 */         String statusMsg = newData.getProperty("StatusMessage");
/* 551 */         if (statusMsg != null)
/*     */         {
/* 553 */           oldData.put("StatusMessage", statusMsg);
/*     */         }
/* 555 */         String statusMsgKey = newData.getProperty("StatusMessageKey");
/* 556 */         if (statusMsgKey != null)
/*     */         {
/* 558 */           oldData.put("StatusMessageKey", statusMsgKey);
/*     */         }
/* 560 */         binder.copyResultSetStateShallow(temp);
/* 561 */         binder.setLocalData(oldData);
/* 562 */         binder.m_isCgi = isCgiEncode;
/*     */       }
/* 564 */       break;
/*     */     case 9:
/* 569 */       WfDesignData wfDesign = WfDesignManager.getWorkflowDesign(sArg1);
/*     */ 
/* 571 */       service.setCachedObject("WorkflowDesign", wfDesign);
/* 572 */       binder.merge(wfDesign.m_designData);
/* 573 */       bResult = true;
/* 574 */       break;
/*     */     case 10:
/* 583 */       if (nargs < 2)
/*     */       {
/* 585 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "2");
/*     */ 
/* 587 */         throw new IllegalArgumentException(msg);
/*     */       }
/*     */ 
/* 591 */       WfDesignData wfDesign = (WfDesignData)service.getCachedObject("WorkflowDesign");
/* 592 */       if (wfDesign == null)
/*     */       {
/* 594 */         wfDesign = WfDesignManager.getWorkflowDesign(sArg1);
/* 595 */         service.setCachedObject("WorkflowDesign", wfDesign);
/*     */       }
/*     */ 
/* 597 */       String condName = ScriptUtils.getDisplayString(args[2], context);
/*     */ 
/* 599 */       String str = binder.getLocal(sArg2 + ":" + condName);
/* 600 */       JumpClausesData clausesData = new JumpClausesData(true);
/* 601 */       clausesData.setClauseDisplay(null, " and<br>");
/* 602 */       clausesData.parse(str);
/* 603 */       oResult = clausesData.createQueryString();
/* 604 */       break;
/*     */     case 11:
/* 608 */       service.setCachedObject("WfReleaseDocumentState", new Integer(1));
/* 609 */       break;
/*     */     case 12:
/*     */       try
/*     */       {
/* 615 */         String dReleaseState = binder.get("dReleaseState");
/* 616 */         bResult = !dReleaseState.equals("E");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 620 */         throw new ServiceException(e);
/*     */       }
/*     */     case 13:
/* 626 */       oResult = WorkflowScriptUtils.formatLocalizedStepTypeDescription(sArg1, service);
/* 627 */       break;
/*     */     case 14:
/* 642 */       if (nargs < 3)
/*     */       {
/* 644 */         String msg = LocaleUtils.encodeMessage("csScriptEvalNotEnoughArgs", null, function, "3");
/*     */ 
/* 646 */         throw new IllegalArgumentException(msg);
/*     */       }
/* 648 */       String sArg3 = ScriptUtils.getDisplayString(args[2], context);
/* 649 */       Workspace ws = service.getWorkspace();
/*     */       WfCompanionData wfCompanionData;
/*     */       try
/*     */       {
/* 653 */         wfCompanionData = WorkflowUtils.retrieveCompanionData(binder, context, ws);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 657 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 659 */       DataResultSet drset = WorkflowUtils.getOrCreateWorkflowActionHistory(wfCompanionData);
/* 660 */       DataBinder workBinder = WorkflowUtils.createWorkflowActionHistoryBinder(drset, binder, context);
/* 661 */       workBinder.putLocal("wfAction", sArg1);
/* 662 */       workBinder.putLocal("wfMessage", sArg2);
/* 663 */       workBinder.putLocal("wfUsers", sArg3);
/* 664 */       WorkflowUtils.updateWorkflowActionHistory(drset, workBinder);
/* 665 */       break;
/*     */     case 15:
/* 675 */       Workspace ws = service.getWorkspace();
/*     */       Vector userDataList;
/*     */       try
/*     */       {
/* 679 */         userDataList = WorkflowUtils.computeStepUsers(ws, binder, false, context, false, true);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 686 */         throw new ServiceException(e);
/*     */       }
/* 688 */       int numUsers = userDataList.size();
/* 689 */       if (numUsers < 1)
/*     */       {
/* 691 */         oResult = "";
/*     */       }
/*     */       else {
/* 694 */         Vector userList = new IdcVector(numUsers);
/* 695 */         for (int i = 0; i < numUsers; ++i)
/*     */         {
/* 697 */           UserData userData = (UserData)userDataList.elementAt(i);
/* 698 */           String user = userData.getProperty("dName");
/* 699 */           userList.add(user);
/*     */         }
/* 701 */         String users = StringUtils.createString(userList, ',', '^');
/*     */ 
/* 703 */         oResult = users;
/* 704 */       }break;
/*     */     case 16:
/* 709 */       bResult = true;
/* 710 */       String status = binder.getAllowMissing("dStatus");
/* 711 */       if (status.equalsIgnoreCase("GENWWW"))
/*     */       {
/* 713 */         WorkflowStates wfStates = (WorkflowStates)service.getCachedObject("WorkflowStates");
/* 714 */         if (wfStates != null)
/*     */         {
/* 716 */           bResult = wfStates.m_docConverted;
/*     */         }
/*     */       }
/* 718 */       break;
/*     */     case 17:
/* 728 */       WorkflowStates wfStates = (WorkflowStates)service.getCachedObject("WorkflowStates");
/* 729 */       if (wfStates != null)
/*     */       {
/* 731 */         bResult = wfStates.m_suppressWorkflowNotification; } break;
/*     */     case 18:
/* 741 */       WorkflowStates wfStates = (WorkflowStates)service.getCachedObject("WorkflowStates");
/* 742 */       if (wfStates != null)
/*     */       {
/* 744 */         wfStates.m_suppressWorkflowNotification = StringUtils.convertToBool(sArg1, false); } break;
/*     */     case 19:
/* 750 */       WorkflowUtils.computeAllowedStepActions(binder, service.getUserData(), service);
/* 751 */       break;
/*     */     case 20:
/* 755 */       WorkflowStates wfStates = (WorkflowStates)service.getCachedObject("WorkflowStates");
/* 756 */       if (wfStates != null)
/*     */       {
/* 758 */         wfStates.m_suppressWorkflowNotification = StringUtils.convertToBool(sArg1, false); } break;
/*     */     case 21:
/* 764 */       WorkflowStates wfStates = (WorkflowStates)service.getCachedObject("WorkflowStates");
/* 765 */       if (wfStates != null)
/*     */       {
/* 767 */         bResult = wfStates.m_suppressWorkflowNotification; } break;
/*     */     default:
/* 772 */       return false;
/*     */     }
/*     */ 
/* 775 */     args[nargs] = ScriptExtensionUtils.computeReturnObject(config[4], bResult, iResult, dResult, oResult);
/*     */ 
/* 779 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean evaluateValue(ScriptInfo info, boolean[] bVal, String[] sVal, ExecutionContext context, boolean isConditional)
/*     */     throws ServiceException
/*     */   {
/* 787 */     int[] config = (int[])(int[])info.m_entry;
/* 788 */     sVal[0] = "0";
/* 789 */     bVal[0] = false;
/*     */ 
/* 791 */     Service service = getAndCheckService(info.m_key, "csScriptVarMustBeInService", "csScriptVarMustBeInWorkflowContext", true, false, context);
/*     */ 
/* 794 */     DataBinder binder = service.getBinder();
/* 795 */     DataBinder wfResultData = (DataBinder)context.getCachedObject("WorkflowScriptResult");
/*     */ 
/* 797 */     switch (config[0])
/*     */     {
/*     */     case 0:
/* 803 */       wfResultData.putLocal("isWfStart", "1");
/*     */ 
/* 805 */       WfStepData stepData = (WfStepData)context.getCachedObject("WorkflowSteps");
/* 806 */       stepData.setCurrentRow(0);
/* 807 */       sVal[0] = (stepData.getStepName() + "@" + binder.getLocal("dWfName"));
/* 808 */       bVal[0] = true;
/* 809 */       return true;
/*     */     }
/*     */ 
/* 812 */     return false;
/*     */   }
/*     */ 
/*     */   public Service getAndCheckService(String tokenId, String notServiceMsgKey, String notAllowedMsgKey, boolean checkIsFullWorkflowContext, boolean isServiceOnly, ExecutionContext context)
/*     */     throws ServiceException
/*     */   {
/* 820 */     String msg = LocaleUtils.encodeMessage(notServiceMsgKey, null, tokenId, "Service");
/*     */ 
/* 822 */     Service service = ScriptExtensionUtils.getService(context, msg);
/*     */ 
/* 824 */     if (isServiceOnly)
/*     */     {
/* 826 */       return service;
/*     */     }
/*     */ 
/* 829 */     if ((!service instanceof WorkflowService) && (!service instanceof DocService) && (!service instanceof FileService))
/*     */     {
/* 832 */       msg = LocaleUtils.encodeMessage(notServiceMsgKey, null, tokenId, "WorkflowService");
/*     */ 
/* 834 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 837 */     if ((checkIsFullWorkflowContext) && (!service.isConditionVarTrue("allowWorkflowIdocScript")))
/*     */     {
/* 839 */       msg = LocaleUtils.encodeMessage(notAllowedMsgKey, null, tokenId);
/*     */ 
/* 841 */       throw new ServiceException(msg);
/*     */     }
/* 843 */     return service;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 848 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98120 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.script.WorkflowScriptExtensions
 * JD-Core Version:    0.5.4
 */