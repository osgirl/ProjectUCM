/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.RevisionSpec;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Date;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ArchiveCheckinHandler extends ServiceHandler
/*     */ {
/*     */   public boolean m_queryForDocMeta;
/*     */ 
/*     */   public ArchiveCheckinHandler()
/*     */   {
/*  48 */     this.m_queryForDocMeta = false;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void determineCheckin()
/*     */     throws ServiceException, DataException
/*     */   {
/*  61 */     if (this.m_service.isConditionVarTrue("HasNoCheckinDocument"))
/*     */     {
/*  63 */       return;
/*     */     }
/*     */ 
/*  66 */     boolean docExists = doesDocExist(false);
/*  67 */     boolean revExists = false;
/*  68 */     boolean isInWF = false;
/*     */ 
/*  72 */     String event = "OnImport";
/*  73 */     if (this.m_service.isConditionVarTrue("IsSubmit"))
/*     */     {
/*  76 */       event = "OnSubmit";
/*     */     }
/*  78 */     this.m_binder.putLocal("dpEvent", event);
/*     */ 
/*  81 */     String revLabel = this.m_binder.getLocal("dRevLabel");
/*  82 */     boolean updatingRev = false;
/*     */ 
/*  84 */     if (docExists)
/*     */     {
/*  86 */       checkPublishState();
/*  87 */       if (revLabel != null)
/*     */       {
/*  89 */         revExists = doesRevLabelExist(false);
/*  90 */         String latestID = this.m_binder.getLocal("latestID");
/*  91 */         String curID = this.m_binder.getLocal("dID");
/*  92 */         if ((latestID != null) && (curID != null) && (!latestID.equals(curID)))
/*     */         {
/*  94 */           String msg = LocaleUtils.encodeMessage("csCheckinRevLabelExistsForOlderRev", null, revLabel);
/*  95 */           this.m_service.createServiceException(null, msg);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 101 */       String workflowState = this.m_binder.get("dWorkflowState");
/* 102 */       isInWF = workflowState.length() > 0;
/*     */     }
/*     */ 
/* 105 */     String command = null;
/* 106 */     if (!docExists)
/*     */     {
/* 108 */       command = "CHECKIN_NEW_SUB";
/*     */     }
/* 110 */     else if (isInWF)
/*     */     {
/* 112 */       updatingRev = true;
/*     */ 
/* 114 */       command = "WORKFLOW_CHECKIN_SUB";
/*     */     }
/* 116 */     else if (revExists)
/*     */     {
/* 119 */       this.m_service.createServiceException(null, "!csCheckinItemExists");
/*     */     }
/*     */     else
/*     */     {
/* 123 */       if (revLabel == null)
/*     */       {
/* 126 */         String latestRevLabel = this.m_binder.getLocal("latestRevLabel");
/* 127 */         String nextRev = RevisionSpec.getNext(latestRevLabel);
/* 128 */         if (nextRev == null)
/*     */         {
/* 130 */           nextRev = RevisionSpec.getInvalidLabel();
/*     */         }
/* 132 */         this.m_binder.putLocal("dRevLabel", nextRev);
/*     */       }
/*     */ 
/* 140 */       String isForceCheckout = this.m_binder.getLocal("isForceCheckout");
/* 141 */       if ((isForceCheckout != null) && (isForceCheckout.equals("1")) && (((SharedObjects.getEnvValueAsBoolean("AutoContributorAdvancesOnUnlock", false)) || (this.m_service.isConditionVarTrue("AutoContributorAdvancesOnUnlock")))) && 
/* 145 */         (this.m_binder.getLocal("isFinished") == null))
/*     */       {
/* 147 */         this.m_binder.putLocal("isFinished", "1");
/*     */       }
/*     */ 
/* 152 */       command = "CHECKIN_SEL_SUB";
/*     */     }
/*     */ 
/* 155 */     if (!updatingRev)
/*     */     {
/* 158 */       this.m_binder.removeResultSet("DOC_INFO");
/*     */     }
/*     */ 
/* 161 */     if (docExists)
/*     */     {
/* 163 */       this.m_service.doCode("checkAndValidateRevClassesRow");
/*     */     }
/*     */ 
/* 166 */     if (command != null)
/*     */     {
/* 168 */       this.m_service.executeService(command);
/*     */     }
/*     */     else
/*     */     {
/* 173 */       this.m_service.createServiceException(null, "!csCheckinTypeUnknown");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void processCheckinArchive() throws ServiceException, DataException
/*     */   {
/* 180 */     boolean doDocSecurityCheck = StringUtils.convertToBool(this.m_binder.getAllowMissing("doDocSecurityCheck"), false);
/*     */ 
/* 182 */     this.m_queryForDocMeta = SharedObjects.getEnvValueAsBoolean("ArchiveCheckinQueryAllDocMeta", false);
/* 183 */     this.m_binder.removeLocal("isChangingMetaData");
/*     */ 
/* 186 */     String command = determineCommand();
/*     */ 
/* 188 */     if (doDocSecurityCheck)
/*     */     {
/* 190 */       checkArchiveDocActionSecurity();
/*     */     }
/*     */ 
/* 194 */     PluginFilters.filter("preProcessCheckinArchive", this.m_workspace, this.m_binder, this.m_service);
/*     */ 
/* 196 */     this.m_service.executeService(command);
/*     */   }
/*     */ 
/*     */   protected void checkArchiveDocActionSecurity()
/*     */     throws DataException, ServiceException
/*     */   {
/* 203 */     int nloops = (this.m_binder.getLocal("isChangingMetaData") != null) ? 2 : 1;
/* 204 */     ServiceData serviceData = this.m_service.getServiceData();
/*     */ 
/* 206 */     for (int i = 0; i < nloops; ++i)
/*     */     {
/* 208 */       if ((this.m_binder.getResultSet("DOC_INFO") != null) && (i == 0))
/*     */       {
/* 210 */         this.m_binder.putLocal("SecurityProfileResultSet", "DOC_INFO");
/*     */       }
/*     */       else
/*     */       {
/* 214 */         this.m_binder.removeLocal("SecurityProfileResultSet");
/*     */       }
/*     */ 
/* 217 */       if (this.m_service.checkAccess(this.m_binder, serviceData.m_accessLevel))
/*     */         continue;
/* 219 */       ServiceException se = new ServiceException(-20, "");
/* 220 */       this.m_service.createServiceException(se, "!csCheckinAdminPermissionDenied");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String determineCommand()
/*     */     throws ServiceException, DataException
/*     */   {
/* 228 */     String action = this.m_binder.getLocal("Action");
/* 229 */     if (action == null)
/*     */     {
/* 231 */       throw new DataException("!csActionParamMissing");
/*     */     }
/*     */ 
/* 236 */     this.m_binder.putLocal("dpEvent", "OnImport");
/*     */ 
/* 238 */     String dRevLabel = this.m_binder.getLocal("dRevLabel");
/* 239 */     String command = null;
/* 240 */     boolean isDeleting = action.equalsIgnoreCase("delete");
/* 241 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/*     */     String errMsg;
/*     */     String errMsg;
/* 245 */     if (isDeleting)
/*     */     {
/*     */       String errMsg;
/* 247 */       if (dRevLabel != null)
/*     */       {
/* 249 */         errMsg = LocaleUtils.encodeMessage("csDeleteFailedWithRevLabel(dDocName,dRevLabel)", null);
/*     */       }
/*     */       else
/*     */       {
/* 254 */         errMsg = LocaleUtils.encodeMessage("csDeleteFailed(dDocName)", null);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/*     */       String errMsg;
/* 259 */       if (dRevLabel != null)
/*     */       {
/* 261 */         errMsg = LocaleUtils.encodeMessage("csCheckinFailedWithRevLabel(dDocName,dRevLabel)", null);
/*     */       }
/*     */       else
/*     */       {
/* 266 */         errMsg = LocaleUtils.encodeMessage("csCheckinFailed(dDocName)", null);
/*     */       }
/*     */     }
/* 269 */     this.m_service.setCurrentErrorMsg(errMsg);
/*     */ 
/* 273 */     String webViewable = this.m_binder.getLocal("webViewableFile");
/* 274 */     if ((webViewable == null) || (webViewable.length() == 0))
/*     */     {
/* 276 */       int count = 1;
/* 277 */       for (int i = 0; i < AdditionalRenditions.m_maxNum; ++count)
/*     */       {
/* 279 */         String key = "dRendition" + count;
/* 280 */         this.m_binder.putLocal(key, "");
/*     */ 
/* 277 */         ++i;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 284 */     if (StringUtils.convertToBool(this.m_binder.getLocal("IsNative"), false))
/*     */     {
/* 287 */       command = determineNativeCommand(action);
/* 288 */       if (command != null)
/*     */       {
/* 290 */         this.m_binder.putLocal("IsNotLatestRev", "1");
/* 291 */         return command;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 296 */     if (!doesDocExist(isDeleting))
/*     */     {
/* 298 */       if (isDeleting)
/*     */       {
/* 301 */         ServiceException se = new ServiceException(-33, "");
/* 302 */         this.m_service.createServiceException(se, "!csContentItemMissing");
/*     */       }
/*     */       else
/*     */       {
/* 306 */         validatePublishStates(true);
/* 307 */         if (isPublish)
/*     */         {
/* 309 */           return "CHECKIN_NEW_SUB";
/*     */         }
/* 311 */         return "INSERT_NEW";
/*     */       }
/*     */     }
/*     */ 
/* 315 */     this.m_service.doCode("checkAndValidateRevClassesRow");
/* 316 */     if (!isDeleting)
/*     */     {
/* 318 */       validatePublishStates(false);
/* 319 */       if (isPublish)
/*     */       {
/* 321 */         if (dRevLabel == null)
/*     */         {
/* 323 */           String revLabel = this.m_binder.get("dRevLabel");
/* 324 */           this.m_binder.putLocal("dRevLabel", revLabel);
/*     */         }
/* 326 */         return determineUpdateCommand();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 331 */     boolean revLabelExists = false;
/* 332 */     if (dRevLabel != null)
/*     */     {
/* 334 */       revLabelExists = doesRevLabelExist(isDeleting);
/*     */     }
/*     */ 
/* 339 */     String workflowState = this.m_binder.getLocal("dWorkflowState");
/* 340 */     if (workflowState.length() > 0)
/*     */     {
/* 342 */       this.m_service.createServiceException(null, "!csWorkflowItemActive");
/*     */     }
/*     */ 
/* 345 */     if (revLabelExists)
/*     */     {
/* 347 */       if (isDeleting)
/*     */       {
/* 349 */         command = "DELETE_BYREV";
/*     */       }
/* 351 */       else if (action.equalsIgnoreCase("insert"))
/*     */       {
/* 353 */         this.m_service.createServiceException(null, "!csCheckinFailedAlreadyExists");
/*     */       }
/*     */       else
/*     */       {
/* 357 */         command = determineUpdateCommand();
/*     */       }
/*     */ 
/*     */     }
/* 362 */     else if (isDeleting)
/*     */     {
/* 364 */       if (dRevLabel != null)
/*     */       {
/* 367 */         this.m_service.createServiceExceptionEx(null, LocaleUtils.encodeMessage("csContentItemMissingWithRevLabel", null, dRevLabel), -33);
/*     */       }
/*     */       else
/*     */       {
/* 372 */         command = "DELETE_BYCLASS";
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 377 */       this.m_binder.putLocal("canInsertNew", "1");
/* 378 */       String errorMsg = "!syUnknownError";
/*     */ 
/* 381 */       this.m_service.doCode("doDateComparisonTestAndSupplyDefaults");
/*     */ 
/* 385 */       int ret = PluginFilters.filter("archiveCheckinRevisionComparisonTest", this.m_workspace, this.m_binder, this.m_service);
/* 386 */       if (ret != -1)
/*     */       {
/* 388 */         boolean result = DataBinderUtils.getLocalBoolean(this.m_binder, "canInsertNew", false);
/* 389 */         if (result)
/*     */         {
/* 393 */           boolean isInherit = StringUtils.convertToBool(this.m_binder.getLocal("InheritPreRevValues"), false);
/*     */ 
/* 395 */           if (isInherit)
/*     */           {
/* 397 */             DataResultSet infoSet = (DataResultSet)this.m_binder.getResultSet("DOC_INFO");
/* 398 */             if ((infoSet != null) && (!infoSet.isEmpty()))
/*     */             {
/* 400 */               infoSet.first();
/* 401 */               String docID = ResultSetUtils.getValue(infoSet, "dID");
/* 402 */               Properties queryProps = new Properties();
/* 403 */               queryProps.put("dID", docID);
/* 404 */               PropParameters args = new PropParameters(queryProps);
/* 405 */               ResultSet rset = this.m_workspace.createResultSet("QdocInfo", args);
/* 406 */               DataResultSet oldInfoSet = new DataResultSet();
/* 407 */               oldInfoSet.copy(rset);
/* 408 */               this.m_binder.addResultSet("OLD_DOC_INFO", oldInfoSet);
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 413 */           this.m_binder.removeResultSet("DOC_INFO");
/*     */ 
/* 416 */           command = "INSERT_NEW";
/*     */         }
/*     */         else
/*     */         {
/* 420 */           String errorDateMsg = this.m_binder.getLocal("errorDateMsg");
/* 421 */           if ((errorDateMsg != null) && (errorDateMsg.length() > 0))
/*     */           {
/* 423 */             errorMsg = errorDateMsg;
/*     */           }
/* 425 */           this.m_service.createServiceException(null, errorMsg);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 431 */     return command;
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void doDateComparisonTestAndSupplyDefaults() throws DataException, ServiceException
/*     */   {
/* 437 */     Date newDocDate = null;
/* 438 */     Date latestDocDate = null;
/*     */ 
/* 441 */     String dateMsg = "csCheckinCreateDateError";
/* 442 */     String newCreateDateStr = this.m_binder.getLocal("dCreateDate");
/* 443 */     String newInDateStr = this.m_binder.getLocal("dInDate");
/* 444 */     Date newCreateDate = null;
/* 445 */     Date newInDate = null;
/* 446 */     Date tempNewDate = null;
/* 447 */     String tempNewDateStr = null;
/*     */ 
/* 450 */     if ((newCreateDateStr == null) || (newCreateDateStr.length() == 0))
/*     */     {
/* 452 */       tempNewDate = new Date();
/* 453 */       tempNewDateStr = LocaleUtils.formatODBC(tempNewDate);
/* 454 */       this.m_binder.putLocal("dCreateDate", tempNewDateStr);
/* 455 */       newCreateDate = tempNewDate;
/*     */     }
/* 457 */     if ((newInDateStr == null) || (newInDateStr.length() == 0))
/*     */     {
/* 459 */       if (tempNewDate == null)
/*     */       {
/* 461 */         tempNewDate = new Date();
/* 462 */         tempNewDateStr = LocaleUtils.formatODBC(tempNewDate);
/*     */       }
/* 464 */       this.m_binder.putLocal("dInDate", tempNewDateStr);
/* 465 */       newInDate = tempNewDate;
/*     */     }
/*     */ 
/* 468 */     String latestCreateDateStr = this.m_binder.getLocal("latestCreateDate");
/*     */ 
/* 470 */     boolean isUseCreateDate = SharedObjects.getEnvValueAsBoolean("UseRevisionCreateDate", false);
/* 471 */     if ((isUseCreateDate) && (latestCreateDateStr != null) && (latestCreateDateStr.length() > 0))
/*     */     {
/* 473 */       newDocDate = newCreateDate;
/* 474 */       if (newCreateDate == null)
/*     */       {
/* 476 */         newDocDate = this.m_binder.parseDate("dCreateDate", newCreateDateStr);
/*     */       }
/* 478 */       latestDocDate = this.m_binder.parseDate("latestCreateDate", latestCreateDateStr);
/*     */     }
/*     */     else
/*     */     {
/* 482 */       dateMsg = "csCheckinReleaseDateError";
/* 483 */       newDocDate = newInDate;
/* 484 */       if (newDocDate == null)
/*     */       {
/* 486 */         newDocDate = this.m_binder.parseDate("dInDate", newInDateStr);
/*     */       }
/* 488 */       latestDocDate = this.m_binder.parseDate("latestInDate", this.m_binder.getLocal("latestInDate"));
/*     */     }
/*     */ 
/* 491 */     boolean result = false;
/* 492 */     if (newDocDate != null)
/*     */     {
/* 494 */       boolean isAllowMatches = SharedObjects.getEnvValueAsBoolean("AllowMatchesInDateCheck", false);
/*     */ 
/* 496 */       long newTime = newDocDate.getTime();
/* 497 */       long latestTime = latestDocDate.getTime();
/* 498 */       if (isAllowMatches)
/*     */       {
/* 500 */         result = newTime >= latestTime;
/*     */       }
/*     */       else
/*     */       {
/* 507 */         result = newTime > latestTime + 998L;
/*     */       }
/*     */     }
/* 510 */     if (result)
/*     */       return;
/* 512 */     String errorDateMsg = LocaleUtils.encodeMessage(dateMsg, null, newDocDate, latestDocDate);
/* 513 */     this.m_binder.putLocal("errorDateMsg", errorDateMsg);
/* 514 */     this.m_binder.putLocal("canInsertNew", "0");
/*     */   }
/*     */ 
/*     */   protected String determineUpdateCommand()
/*     */     throws DataException
/*     */   {
/* 520 */     String command = null;
/*     */ 
/* 522 */     String primaryFileStr = this.m_binder.getLocal("primaryFile");
/* 523 */     this.m_binder.putLocal("isChangingMetaData", "1");
/* 524 */     boolean isCreatePrimaryMetaFile = StringUtils.convertToBool(this.m_binder.getAllowMissing("createPrimaryMetaFile"), false);
/*     */ 
/* 526 */     if (isCreatePrimaryMetaFile == true)
/*     */     {
/* 528 */       command = "UPDATE_DOCINFO_METAFILE_BYREV";
/*     */     }
/* 530 */     else if ((primaryFileStr == null) || (primaryFileStr.length() == 0))
/*     */     {
/* 532 */       command = "UPDATE_DOCINFO_BYREV";
/*     */     }
/*     */     else
/*     */     {
/* 536 */       setUpdateState();
/* 537 */       command = "UPDATE_BYREV";
/*     */     }
/*     */ 
/* 540 */     return command;
/*     */   }
/*     */ 
/*     */   protected String determineNativeCommand(String action)
/*     */     throws DataException, ServiceException
/*     */   {
/* 547 */     boolean isDelete = action.equalsIgnoreCase("delete");
/* 548 */     boolean doesExist = false;
/* 549 */     DataResultSet drset = new DataResultSet();
/*     */ 
/* 551 */     String qDocID = (this.m_queryForDocMeta) ? "QdocIDMeta" : "QdocID";
/* 552 */     ResultSet rset = this.m_workspace.createResultSet(qDocID, this.m_binder);
/* 553 */     if (!rset.isEmpty())
/*     */     {
/* 558 */       drset.copy(rset);
/*     */ 
/* 560 */       if (!isDelete)
/*     */       {
/* 562 */         checkPublishState(drset);
/*     */       }
/*     */ 
/* 566 */       String docName = ResultSetUtils.getValue(drset, "dDocName");
/* 567 */       String newName = this.m_binder.getLocal("dDocName");
/* 568 */       if ((newName != null) && (!docName.equalsIgnoreCase(newName)))
/*     */       {
/* 571 */         return null;
/*     */       }
/*     */ 
/* 575 */       String[][] map = { { "dStatus", "dStatus" }, { "dReleaseState", "dReleaseState" }, { "dProcessingState", "dProcessingState" } };
/*     */ 
/* 581 */       mapValues(drset, map);
/*     */ 
/* 584 */       if (isDelete)
/*     */       {
/* 586 */         String[][] deletemap = { { "dRevClassID", "dRevClassID" }, { "dID", "dID" }, { "dCreateDate", "dCreateDate" }, { "dInDate", "dInDate" }, { "dRevLabel", "dRevLabel" }, { "dWorkflowState", "dWorkflowState" }, { "dPublishState", "dPublishState" } };
/*     */ 
/* 597 */         mapValues(drset, deletemap);
/*     */       }
/*     */ 
/* 600 */       String releaseState = this.m_binder.getLocal("dReleaseState");
/* 601 */       if (releaseState.equals("E"))
/*     */       {
/* 604 */         this.m_service.createServiceException(null, "!csWorkflowItemActive");
/*     */       }
/*     */ 
/* 607 */       String status = this.m_binder.getLocal("dStatus");
/* 608 */       boolean hasBeenDeleted = status.equals("DELETED");
/* 609 */       this.m_service.setConditionVar("hasBeenDeleted", hasBeenDeleted);
/*     */ 
/* 611 */       this.m_binder.addResultSet("DOC_INFO", drset);
/* 612 */       if (isDelete)
/*     */       {
/* 614 */         return "DELETE_BYREV";
/*     */       }
/* 616 */       if ((!hasBeenDeleted) && (action.equalsIgnoreCase("update")))
/*     */       {
/* 618 */         this.m_binder.putLocal("isChangingMetaData", "1");
/* 619 */         setUpdateState();
/* 620 */         this.m_service.doCode("checkAndValidateRevClassesRow");
/* 621 */         return "UPDATE_BYREV";
/*     */       }
/*     */ 
/* 624 */       doesExist = true;
/*     */     }
/*     */ 
/* 627 */     if (isDelete)
/*     */     {
/* 630 */       return null;
/*     */     }
/*     */ 
/* 634 */     this.m_service.doCode("checkAndValidateRevClassesRow");
/*     */ 
/* 639 */     String revClassId = this.m_binder.getLocal("dRevClassID");
/* 640 */     checkIdentical("QdocNameCheckRevClassID", "dRevClassID", null, "csCheckinItemExistsDiffRevClass");
/* 641 */     checkIdentical("QdocRevClassCheckDocName", "dDocName", revClassId, "csCheckinItemExistsDiffDocName");
/*     */ 
/* 644 */     if (doesExist)
/*     */     {
/* 646 */       this.m_binder.addResultSet("DOC_INFO", drset);
/* 647 */       setUpdateState();
/* 648 */       this.m_binder.putLocal("isChangingMetaData", "1");
/* 649 */       return "UPDATE_BYREV";
/*     */     }
/*     */ 
/* 652 */     this.m_service.doCode("checkAndValidateRevClassesRow");
/* 653 */     return "INSERT_NATIVE";
/*     */   }
/*     */ 
/*     */   protected void checkIdentical(String query, String key, String errorArg1, String errMsg)
/*     */     throws DataException, ServiceException
/*     */   {
/* 659 */     ResultSet rset = this.m_workspace.createResultSet(query, this.m_binder);
/* 660 */     if (rset.isEmpty())
/*     */       return;
/* 662 */     String dbVal = ResultSetUtils.getValue(rset, key);
/* 663 */     String curVal = this.m_binder.getLocal(key);
/* 664 */     if ((curVal == null) || (curVal.equalsIgnoreCase(dbVal)))
/*     */       return;
/* 666 */     if (errorArg1 == null)
/*     */     {
/* 668 */       errorArg1 = curVal;
/*     */     }
/* 670 */     errMsg = LocaleUtils.encodeMessage(errMsg, null, errorArg1, dbVal);
/* 671 */     this.m_service.createServiceException(null, errMsg);
/*     */   }
/*     */ 
/*     */   protected void setUpdateState()
/*     */     throws DataException
/*     */   {
/* 678 */     this.m_binder.putLocal("dStatus", "GENWWW");
/* 679 */     String state = this.m_binder.get("dReleaseState");
/*     */ 
/* 682 */     this.m_binder.putLocal("prevReleaseState", state);
/* 683 */     if ((state.equals("O")) || (state.equals("N")))
/*     */     {
/* 685 */       state = "N";
/*     */     }
/* 687 */     else if (state.equals("E"))
/*     */     {
/* 689 */       state = "E";
/*     */     }
/*     */     else
/*     */     {
/* 693 */       state = "U";
/*     */     }
/* 695 */     this.m_binder.putLocal("IsUpdate", "1");
/*     */ 
/* 697 */     this.m_binder.putLocal("dReleaseState", state);
/*     */   }
/*     */ 
/*     */   protected boolean doesDocExist(boolean isDeleting) throws ServiceException, DataException
/*     */   {
/* 702 */     String docName = this.m_binder.getLocal("dDocName");
/*     */ 
/* 707 */     this.m_binder.putLocal("DocExists", "");
/*     */ 
/* 709 */     if ((!isDeleting) && (((docName == null) || (docName.trim().length() == 0))))
/*     */     {
/* 711 */       return false;
/*     */     }
/*     */ 
/* 715 */     Object o = this.m_service.getCachedObject("LatestDocInfo");
/*     */     DataResultSet drset;
/*     */     DataResultSet drset;
/* 716 */     if ((o != null) && (o instanceof DataResultSet))
/*     */     {
/* 718 */       drset = (DataResultSet)o;
/*     */     }
/*     */     else
/*     */     {
/* 722 */       ResultSet rset = this.m_workspace.createResultSet("QlatestDocName", this.m_binder);
/*     */ 
/* 728 */       rset.setDateFormat(LocaleUtils.m_odbcDateFormat);
/*     */ 
/* 730 */       drset = new DataResultSet();
/* 731 */       drset.copy(rset, 1);
/*     */     }
/*     */ 
/* 734 */     if (!loadStandardDocFields(drset, isDeleting))
/*     */     {
/* 736 */       return false;
/*     */     }
/* 738 */     String dId = this.m_binder.getLocal("dID");
/* 739 */     this.m_binder.putLocal("dCurRevID", dId);
/*     */ 
/* 742 */     this.m_binder.putLocal("DocExists", "1");
/*     */ 
/* 744 */     String[][] map = { { "dRevClassID", "dRevClassID" }, { "dID", "latestID" }, { "dCreateDate", "latestCreateDate" }, { "dInDate", "latestInDate" }, { "dRevLabel", "latestRevLabel" }, { "dProcessingState", "dProcessingState" }, { "dReleaseState", "latestReleaseState" }, { "dWorkflowState", "dWorkflowState" }, { "dStatus", "latestStatus" }, { "dPublishState", "latestPublishState" } };
/*     */ 
/* 758 */     mapValues(drset, map);
/* 759 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean doesRevLabelExist(boolean isDelete) throws ServiceException, DataException
/*     */   {
/* 764 */     ResultSet rset = this.m_workspace.createResultSet("QdocRev", this.m_binder);
/*     */ 
/* 766 */     DataResultSet drset = new DataResultSet();
/* 767 */     drset.copy(rset);
/* 768 */     if (!loadStandardDocFields(drset, isDelete))
/*     */     {
/* 770 */       return false;
/*     */     }
/*     */ 
/* 774 */     String[][] map = { { "dStatus", "dStatus" }, { "dReleaseState", "dReleaseState" } };
/*     */ 
/* 779 */     mapValues(drset, map);
/*     */ 
/* 781 */     return true;
/*     */   }
/*     */ 
/*     */   protected boolean loadStandardDocFields(DataResultSet rset, boolean isDelete)
/*     */   {
/* 786 */     if ((rset == null) || (rset.isEmpty()))
/*     */     {
/* 788 */       return false;
/*     */     }
/*     */ 
/* 791 */     this.m_binder.addResultSet("DOC_INFO", rset);
/*     */ 
/* 795 */     String[][] map = { { "dID", "dID" }, { "dRevisionID", "dRevisionID" } };
/*     */ 
/* 800 */     mapValues(rset, map);
/*     */ 
/* 802 */     if (isDelete)
/*     */     {
/* 804 */       String[] keys = { "dSecurityGroup", "dDocType", "dDocAccount" };
/* 805 */       for (int i = 0; i < keys.length; ++i)
/*     */       {
/* 807 */         String key = keys[i];
/* 808 */         String value = ResultSetUtils.getValue(rset, key);
/* 809 */         this.m_binder.putLocal(key, value);
/*     */       }
/* 811 */       for (int i = 0; i < AdditionalRenditions.m_maxNum; ++i)
/*     */       {
/* 813 */         String key = "dRendition" + (i + 1);
/* 814 */         String value = ResultSetUtils.getValue(rset, key);
/* 815 */         if (value == null)
/*     */         {
/* 817 */           value = "";
/*     */         }
/* 819 */         this.m_binder.putLocal(key, value);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 825 */       String docAccount = this.m_binder.getAllowMissing("dDocAccount");
/* 826 */       if (docAccount == null)
/*     */       {
/* 828 */         this.m_binder.putLocal("dDocAccount", "");
/*     */       }
/*     */ 
/* 832 */       String pubState = this.m_binder.getAllowMissing("dPublishState");
/* 833 */       if (pubState == null)
/*     */       {
/* 835 */         this.m_binder.putLocal("dPublishState", "");
/*     */       }
/*     */     }
/*     */ 
/* 839 */     return true;
/*     */   }
/*     */ 
/*     */   protected void mapValues(DataResultSet drset, String[][] map)
/*     */   {
/* 844 */     int num = map.length;
/* 845 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 847 */       String value = ResultSetUtils.getValue(drset, map[i][0]);
/* 848 */       if ((!StringUtils.convertToBool(this.m_binder.getLocal("UseDIDFromImport"), false)) || (!"dID".equals(map[i][0])))
/* 849 */         this.m_binder.putLocal(map[i][1], value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkPublishState()
/*     */     throws DataException, ServiceException
/*     */   {
/* 857 */     String pubState = this.m_binder.get("dPublishState");
/* 858 */     if (pubState.length() <= 0)
/*     */       return;
/* 860 */     this.m_service.createServiceException(null, "!csCheckinItemPublished");
/*     */   }
/*     */ 
/*     */   protected void checkPublishState(DataResultSet drset)
/*     */     throws DataException, ServiceException
/*     */   {
/* 866 */     String publishState = ResultSetUtils.getValue(drset, "dPublishState");
/* 867 */     if (publishState.length() <= 0)
/*     */       return;
/* 869 */     this.m_service.createServiceException(null, "!csCheckinItemPublished");
/*     */   }
/*     */ 
/*     */   protected void validatePublishStates(boolean isNew)
/*     */     throws ServiceException
/*     */   {
/* 875 */     boolean isPublish = this.m_service.isConditionVarTrue("IsPublish");
/* 876 */     String curState = this.m_binder.getLocal("dPublishState");
/*     */ 
/* 878 */     boolean isNewPublishState = (curState != null) && (curState.length() > 0);
/* 879 */     if (!isNew)
/*     */     {
/* 881 */       String oldState = this.m_binder.getLocal("latestPublishState");
/* 882 */       boolean isOldPublishState = (oldState != null) && (oldState.length() > 0);
/* 883 */       comparePublishStates(isNewPublishState, isOldPublishState);
/*     */ 
/* 885 */       this.m_binder.putLocal("dPublishState", oldState);
/*     */     }
/* 887 */     comparePublishStates(isPublish, isNewPublishState);
/*     */   }
/*     */ 
/*     */   protected void comparePublishStates(boolean newState, boolean oldState) throws ServiceException
/*     */   {
/* 892 */     if (newState)
/*     */     {
/* 894 */       if (oldState)
/*     */         return;
/* 896 */       this.m_service.createServiceException(null, "!csCheckinNotPublishState");
/*     */     }
/*     */     else
/*     */     {
/* 901 */       if (!oldState)
/*     */         return;
/* 903 */       this.m_service.createServiceException(null, "!csCheckinPublishOverride");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 910 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104451 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ArchiveCheckinHandler
 * JD-Core Version:    0.5.4
 */