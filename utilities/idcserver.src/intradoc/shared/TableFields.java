/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TableFields
/*     */ {
/*     */   public Vector m_fields;
/*  52 */   public static String[][] m_dataSources = (String[][])null;
/*     */ 
/*  55 */   public static final String[][] m_defaultDataSources = { { "ArchiveHistory", "apTitleArchiveHistory" }, { "DocTypes", "apTitleContentTypes" }, { "Users", "apTitleUsers" }, { "DocInfo", "apTitleInformationFields" }, { "DocHistory", "apTitleContentHistory" }, { "WorkflowDocs", "apTitleWorkflowItems" }, { "WorkflowHistory", "apTitleWorkflowHistory" } };
/*     */ 
/*  69 */   public static final String[][] m_archiveHistory = { { "dDocName", "apTitleDocName", "", "text" }, { "dDocTitle", "apTitleDocTitle", "", "text" }, { "dRevLabel", "apTitleRevisionLabel", "", "text" }, { "dDocType", "apTitleDocType", "docTypes", "text" }, { "dSecurityGroup", "apTitleSecurityGroup", "securityGroups", "text" }, { "dDocAccount", "apTitleAccount", "", "text" }, { "dActionDate", "apTitleActionDate", "", "date" }, { "dArchiveName", "apTitleArchiveName", "", "text" }, { "dBatchFile", "apTitleBatchFile", "", "text" } };
/*     */ 
/*  82 */   public static final String[][] m_docTypes = { { "dDocType", "apTitleDocType", "docTypes", "text" }, { "dDescription", "apTitleDescription", "", "text" }, { "dGif", "apTitleImageFileName", "", "text" } };
/*     */ 
/*  89 */   public static final String[][] m_users = { { "dName", "apTitleUserName", "docAuthors", "text" }, { "dFullName", "apTitleFullName", "", "text" }, { "dEmail", "apTitleEmailAddress", "", "text" } };
/*     */ 
/*  96 */   public static final String[][] m_docInfo = { { "dCaption", "apTitleContentInformation", "metaDefNames", "text" }, { "dType", "apTitleFieldType", "MetafieldTypes", "text" }, { "dOrder", "apTitleFieldOrder", "", "number" }, { "dIsRequired", "apTitleRequired", "YesNo", "yes/no" }, { "dIsEnabled", "apTitleEnabledOnUI", "YesNo", "yes/no" }, { "dIsSearchable", "apTitleSearchable", "YesNo", "yes/no" }, { "dIsOptionList", "apTitleEnabledOptionList", "YesNo", "yes/no" } };
/*     */ 
/* 107 */   public static final String[][] m_docHistory = { { "dDocName", "apTitleDocName", "", "text" }, { "dID", "apTitleID", "", "number" }, { "dSecurityGroup", "apTitleSecurityGroup", "securityGroups", "text" }, { "dUser", "apTitleUserName", "docAuthors", "text" }, { "dActionDate", "apTitleActionDate", "", "date" }, { "dAction", "apTitleAction", "DocActions", "text" } };
/*     */ 
/* 117 */   public static final String[][] m_workflowDoc = { { "dWfName", "apTitleWorkflowName", "", "text" }, { "dDocName", "apTitleDocName", "", "text" }, { "dSecurityGroup", "apTitleSecurityGroup", "securityGroups", "text" }, { "dWfStatus", "apTitleWorkflowStatus", "WorkflowStates", "text" } };
/*     */ 
/* 125 */   public static final String[][] m_workflowHistory = { { "dWfName", "apTitleWorkflowName", "", "text" }, { "dDocName", "apTitleDocName", "", "text" }, { "dID", "apTitleID", "", "number" }, { "dSecurityGroup", "apTitleSecurityGroup", "securityGroups", "text" }, { "dActionDate", "apTitleActionDate", "", "date" }, { "dAction", "apTitleAction", "WorkflowActions", "text" } };
/*     */ 
/* 139 */   public static final String[][] YESNO_OPTIONLIST = { { "1", "apLabelYes" }, { "0", "apLabelNo" } };
/*     */ 
/* 145 */   public static final String[][] TIME_OPTIONLIST = { { "1", "apOneDay" }, { "7", "apOneWeek" }, { "28", "apFourWeeks" } };
/*     */ 
/* 152 */   public static final String[][] METAFIELD_TYPES_OPTIONSLIST = { { "Text", "apLabelTextFieldType" }, { "BigText", "apLabelLongTextFieldType" }, { "Date", "apLabelDateFieldType" }, { "Memo", "apLabelMemoFieldType" }, { "Int", "apLabelIntFieldType" } };
/*     */ 
/* 164 */   public static final String[][] METAFIELD_TYPE_DECIMAL_OPTION = { { "Decimal", "apLabelDecimalFieldType" } };
/*     */ 
/* 169 */   public static final String[][] METAFIELD_OPTIONLISTTYPE_OPTIONSLIST = { { "choice", "apLabelSelectListValidated" }, { "chunval", "apLabelSelectListNotValidated" }, { "combo", "apLabelEditandSelectList" }, { "multi2", "apLabelMultiselectList" }, { "multi", "apLabelEditandMultiselectList" } };
/*     */ 
/* 179 */   public static final String[][] SCHEMAFIELD_TYPES_OPTIONSLIST = { { "varchar", "varchar", "1", "1" }, { "date", "date", "0", "0" }, { "int", "int", "0", "1" } };
/*     */ 
/* 186 */   public static final String[][] STATUS_OPTIONLIST = { { "DONE", "apStatusDone", "apStatusDoneDescription" }, { "EDIT", "apStatusEdit" }, { "GENWWW", "apStatusGenWWW", "apStatusGenWWWDescription" }, { "REVIEW", "apStatusReview" }, { "PENDING", "apStatusPending" }, { "RELEASED", "apStatusReleased" }, { "EXPIRED", "apStatusExpired" }, { "DELETED", "apStatusDeleted" } };
/*     */ 
/* 197 */   public static final String[][] ARCHIVER_STATUS_OPTIONLIST = { { "DONE", "apStatusDone" }, { "GENWWW", "apStatusGenWWW" }, { "RELEASED", "apStatusReleased" }, { "EXPIRED", "apStatusExpired" }, { "DELETED", "apStatusDeleted" } };
/*     */ 
/* 205 */   public static final String[][] ARCHIVER_EDIT_LIST = { { "ARCHIVED", "apStatusArchived" }, { "DELETED", "apStatusDeleted" } };
/*     */ 
/* 210 */   public static final String[][] RELEASESTATE_OPTIONLIST = { { "N", "apStateNew", "apStateNewDescription" }, { "Y", "apStateCurrent" }, { "O", "apStateOld" }, { "E", "apStateWorkflow" }, { "R", "apStateProcessing", "apStateIndexProcessingDescription" }, { "U", "apStateUpdate", "apStateUpdateDescription" }, { "I", "apStateIndexing", "apStateIndexingDescription" } };
/*     */ 
/* 220 */   public static final String[][] INDEXERSTATE_OPTIONLIST = { { "", "apStateIdle" }, { "A", "apStateActiveLoading", "apStateActiveLoadingDescription" }, { "B", "apStateActiveIndexed", "apStateActiveIndexedDescription" }, { "C", "apStateRebuildLoading", "apStateRebuildLoadingDescription" }, { "D", "apStateRebuildIndexed", "apStateRebuildIndexedDescription" }, { "X", "apStateRebuilt", "apStateRebuiltDescription" }, { "Y", "apStateRebuiltUpdated", "apStateRebuiltUpdatedDescription" } };
/*     */ 
/* 230 */   public static final String[][] WORKFLOWSTATE_OPTIONLIST = { { "", "apWfStateIdle" }, { "W", "apTitleReviewerContributor" }, { "E", "apTitleContributor" }, { "R", "apTitleReviewer" }, { "P", "apTitlePending" } };
/*     */ 
/* 238 */   public static final String[][] ARCHIVER_RELEASESTATE_OPTIONLIST = { { "N", "apStateNew" }, { "Y", "apStateCurrent" }, { "O", "apStateOld" }, { "R", "apStateProcessing" }, { "U", "apStateUpdate" }, { "I", "apStateIndexing" } };
/*     */ 
/* 247 */   public static final String[][] PROCESSINGSTATE_OPTIONLIST = { { "Y", "apStateConverted" }, { "C", "apStateProcessing", "apStateRefineryProcessingDescription" }, { "F", "apStateFailed", "apStateFailedDescription" }, { "M", "apStateMetaOnly", "apStateMetaOnlyDescription" }, { "P", "apStateRefineryPass", "apStateRefineryPassDescription" }, { "I", "apStateIncompleteConversion", "apStateIncompleteConversionDescription" }, { "W", "apStateWaiting" }, { "E", "apStateFolderExpiration", "apStateFolderExpirationDescription" } };
/*     */ 
/* 258 */   public static final String[][] PUBLISHSTATE_OPTIONLIST = { { "", "apPublishStateBlank" }, { "P", "apPublishStatePublished" }, { "S", "apPublishStateStaging" }, { "W", "apPublishStateWorkflow" } };
/*     */ 
/* 265 */   public static final String[][] PUBLISHTYPE_OPTIONLIST = { { "", "apPubTypeBlank" }, { "C", "apPubTypeContributor" }, { "G", "apPubTypeGallery" }, { "H", "apPubTypeHome" }, { "P", "apPubTypePage" }, { "N", "apPubTypeNavigation" }, { "O", "apPubTypeOther" }, { "S", "apPubTypeSupport" } };
/*     */ 
/* 276 */   public static final String[][] DOC_ACTIONS_OPTIONLIST = { { "Checkin", "apActionCheckin" }, { "Check out", "apActionCheckout" }, { "Delete Document", "apActionDeleteAll" }, { "Delete Revision", "apActionDeleteRev" }, { "Undo Checkout", "apActionUndoCheckout" }, { "Update", "apActionUpdate" }, { "Resubmit", "apActionResubmit" } };
/*     */ 
/* 286 */   public static final String[][] WORKFLOW_STATE_OPTIONLIST = { { "INIT", "apWorkflowInit" }, { "INPROCESS", "apWorkflowInProcess" } };
/*     */ 
/* 291 */   public static final String[][] WORKFLOW_ACTIONS_OPTIONLIST = { { "Checkin", "apActionCheckin" }, { "Approve", "apActionApprove" }, { "Reject", "apActionReject" }, { "Exit", "apActionExit" }, { "Start", "apActionStart" }, { "Cancel", "apActionCancel" } };
/*     */ 
/* 300 */   public static final String[][] IMPORT_OPTIONLIST = { { "update", "apImportUpdate" }, { "insertRev", "apImportInsertRev" }, { "insertCreate", "apImportInsertCreate" }, { "deleteRev", "apImportDeleteRev" }, { "deleteDoc", "apImportDeleteAll" } };
/*     */ 
/* 308 */   public static final String[][] DOCPROFILERULEFIELD_TYPES_OPTIONLIST = { { "edit", "apDpFieldEdit" }, { "infoOnly", "apDpFieldInfoOnly" }, { "hidden", "apDpFieldHidden" }, { "excluded", "apDpFieldExcluded" }, { "required", "apDpFieldRequired" } };
/*     */ 
/* 316 */   public static final String[][] DOCPROFILERULEFIELD_PRIORITIES_OPTIONLIST = { { "top", "apDpFieldTop" }, { "middle", "apDpFieldMiddle" }, { "bottom", "apDpFieldBottom" } };
/*     */ 
/* 322 */   public static final String[][] DOCPROFILE_EVENTS = { { "OnRequest", "apDpOnRequest" }, { "OnSubmit", "apDpOnSubmit" }, { "OnImport", "apDpOnImport" } };
/*     */ 
/* 328 */   public static final String[][] DOCPROFILE_ACTIONS = { { "CheckinNew", "apDpCheckinNew" }, { "CheckinSel", "apDpCheckinSel" }, { "Info", "apDpInfo" }, { "Update", "apDpUpdate" }, { "Search", "apDpSearch" } };
/*     */ 
/* 340 */   public static final String[][] USER_AUTH_TYPES = { { "LOCAL", "apUserLocal" }, { "GLOBAL", "apUserGlobal" }, { "EXTERNAL", "apUserExternal" } };
/*     */ 
/* 347 */   public static final String[][] NEW_USER_AUTH_TYPES = { { "LOCAL", "apUserLocal" }, { "GLOBAL", "apUserGlobal" } };
/*     */ 
/* 354 */   public static final String[][] LOCALIZATION_LIST = { { "YesNo", "1" }, { "TimeList", "1" }, { "MetafieldTypes", "1" }, { "MetafieldTypesDecimal", "1" }, { "MetafieldOptionTypes", "1" }, { "StatusList", "1,2" }, { "ArchiverStatusList", "1" }, { "ArchiverEditList", "1" }, { "ReleaseStateList", "1,2" }, { "IndexerStateList", "1,2" }, { "WorkflowStateList", "1,2" }, { "ArchiverReleaseStateList", "1" }, { "ProcessingStateList", "1,2" }, { "PublishStateList", "1" }, { "PublishTypeList", "1" }, { "DocActions", "1" }, { "WorkflowStates", "1" }, { "WorkflowActions", "1" }, { "ImportList", "1" }, { "DocProfileRuleFieldTypes", "1" }, { "DocProfileRuleFieldPriorities", "1" }, { "DocProfileEvents", "1" }, { "DocProfileTypes", "1" }, { "UserAuthTypes", "1" }, { "NewUserAuthTypes", "1" } };
/*     */ 
/* 385 */   public Hashtable m_tableDefs = null;
/* 386 */   public Hashtable m_displayMaps = null;
/* 387 */   public Properties m_optListTypesMap = null;
/*     */ 
/*     */   public TableFields()
/*     */   {
/* 391 */     this.m_fields = new IdcVector();
/* 392 */     this.m_tableDefs = new Hashtable();
/* 393 */     this.m_displayMaps = new Hashtable();
/* 394 */     this.m_optListTypesMap = new Properties();
/*     */ 
/* 398 */     this.m_displayMaps.put("YesNo", YESNO_OPTIONLIST);
/* 399 */     this.m_displayMaps.put("TimeList", TIME_OPTIONLIST);
/* 400 */     this.m_displayMaps.put("MetafieldTypes", METAFIELD_TYPES_OPTIONSLIST);
/* 401 */     this.m_displayMaps.put("MetafieldTypesDecimal", METAFIELD_TYPE_DECIMAL_OPTION);
/* 402 */     this.m_displayMaps.put("MetafieldOptionTypes", METAFIELD_OPTIONLISTTYPE_OPTIONSLIST);
/* 403 */     this.m_displayMaps.put("SchemaFieldTypes", SCHEMAFIELD_TYPES_OPTIONSLIST);
/* 404 */     this.m_displayMaps.put("DocActions", DOC_ACTIONS_OPTIONLIST);
/* 405 */     this.m_displayMaps.put("WorkflowStates", WORKFLOW_STATE_OPTIONLIST);
/* 406 */     this.m_displayMaps.put("WorkflowActions", WORKFLOW_ACTIONS_OPTIONLIST);
/* 407 */     this.m_displayMaps.put("StatusList", STATUS_OPTIONLIST);
/* 408 */     this.m_displayMaps.put("ArchiverStatusList", ARCHIVER_STATUS_OPTIONLIST);
/* 409 */     this.m_displayMaps.put("ArchiverEditList", ARCHIVER_EDIT_LIST);
/* 410 */     this.m_displayMaps.put("ReleaseStateList", RELEASESTATE_OPTIONLIST);
/*     */ 
/* 413 */     this.m_displayMaps.put("IndexerStateList", INDEXERSTATE_OPTIONLIST);
/* 414 */     this.m_displayMaps.put("WorkflowStateList", WORKFLOWSTATE_OPTIONLIST);
/* 415 */     this.m_displayMaps.put("ArchiverReleaseStateList", ARCHIVER_RELEASESTATE_OPTIONLIST);
/* 416 */     this.m_displayMaps.put("ProcessingStateList", PROCESSINGSTATE_OPTIONLIST);
/* 417 */     this.m_displayMaps.put("PublishStateList", PUBLISHSTATE_OPTIONLIST);
/* 418 */     this.m_displayMaps.put("PublishTypeList", PUBLISHTYPE_OPTIONLIST);
/* 419 */     this.m_displayMaps.put("ImportList", IMPORT_OPTIONLIST);
/* 420 */     this.m_displayMaps.put("DocProfileRuleFieldTypes", DOCPROFILERULEFIELD_TYPES_OPTIONLIST);
/* 421 */     this.m_displayMaps.put("DocProfileRuleFieldPriorities", DOCPROFILERULEFIELD_PRIORITIES_OPTIONLIST);
/* 422 */     this.m_displayMaps.put("DocProfileEvents", DOCPROFILE_EVENTS);
/* 423 */     this.m_displayMaps.put("DocProfileTypes", DOCPROFILE_ACTIONS);
/*     */ 
/* 425 */     this.m_displayMaps.put("UserAuthTypes", USER_AUTH_TYPES);
/* 426 */     this.m_displayMaps.put("NewUserAuthTypes", NEW_USER_AUTH_TYPES);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 432 */     if (initDataSources())
/*     */       return;
/* 434 */     initDataSourceDefaults();
/*     */   }
/*     */ 
/*     */   public boolean initDataSources()
/*     */   {
/* 445 */     DataResultSet stdSources = SharedObjects.getTable("StdReportDataSources");
/* 446 */     if (stdSources == null)
/*     */     {
/* 448 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 454 */       ResultSetUtils.sortResultSet(stdSources, new String[] { "name" });
/* 455 */       m_dataSources = ResultSetUtils.createFilteredStringTable(stdSources, new String[] { "name", "name", "caption" }, null);
/*     */ 
/* 461 */       int tableNameIndex = ResultSetUtils.getIndexMustExist(stdSources, "name");
/* 462 */       int resourceNameIndex = ResultSetUtils.getIndexMustExist(stdSources, "resourceName");
/* 463 */       for (stdSources.first(); stdSources.isRowPresent(); stdSources.next())
/*     */       {
/* 465 */         String tableName = stdSources.getStringValue(tableNameIndex);
/* 466 */         String resourceName = stdSources.getStringValue(resourceNameIndex);
/* 467 */         ResultSet rset = SharedObjects.getTable(resourceName);
/* 468 */         if (rset == null)
/*     */           continue;
/* 470 */         String[][] table = ResultSetUtils.createStringTable(rset, null);
/* 471 */         this.m_tableDefs.put(tableName, table);
/*     */       }
/*     */ 
/* 476 */       DataResultSet stdMaps = SharedObjects.getTable("StdReportDisplayMaps");
/* 477 */       if (stdMaps != null)
/*     */       {
/* 480 */         int mapNameIndex = ResultSetUtils.getIndexMustExist(stdMaps, "name");
/* 481 */         resourceNameIndex = ResultSetUtils.getIndexMustExist(stdMaps, "resourceName");
/* 482 */         FieldInfo fi = new FieldInfo();
/* 483 */         stdMaps.getFieldInfo("optListType", fi);
/* 484 */         int optListTypeIndex = fi.m_index;
/*     */ 
/* 486 */         for (stdMaps.first(); stdMaps.isRowPresent(); stdMaps.next())
/*     */         {
/* 488 */           String mapName = stdMaps.getStringValue(mapNameIndex);
/* 489 */           String resourceName = stdMaps.getStringValue(resourceNameIndex);
/* 490 */           ResultSet rset = SharedObjects.getTable(resourceName);
/* 491 */           if (rset == null)
/*     */             continue;
/* 493 */           String[][] table = ResultSetUtils.createStringTable(rset, null);
/* 494 */           this.m_displayMaps.put(mapName, table);
/* 495 */           if (optListTypeIndex < 0)
/*     */             continue;
/* 497 */           this.m_optListTypesMap.put(mapName, stdMaps.getStringValue(optListTypeIndex));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 502 */       return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 506 */       IdcMessage msg = IdcMessageFactory.lc(e);
/* 507 */       String errMsg = LocaleResources.localizeMessage(null, msg, null).toString();
/* 508 */       Report.trace(null, "Unable to load standard report resources. " + errMsg, e);
/*     */     }
/* 510 */     return false;
/*     */   }
/*     */ 
/*     */   protected void initDataSourceDefaults()
/*     */   {
/* 516 */     m_dataSources = m_defaultDataSources;
/* 517 */     this.m_tableDefs.put("ArchiveHistory", m_archiveHistory);
/* 518 */     this.m_tableDefs.put("DocTypes", m_docTypes);
/* 519 */     this.m_tableDefs.put("Users", m_users);
/* 520 */     this.m_tableDefs.put("DocInfo", m_docInfo);
/* 521 */     this.m_tableDefs.put("DocHistory", m_docHistory);
/* 522 */     this.m_tableDefs.put("WorkflowDocs", m_workflowDoc);
/* 523 */     this.m_tableDefs.put("WorkflowHistory", m_workflowHistory);
/*     */   }
/*     */ 
/*     */   public String getDataSourceDisplayString(String dataSource)
/*     */   {
/* 528 */     return StringUtils.getPresentationString(m_dataSources, dataSource);
/*     */   }
/*     */ 
/*     */   public Hashtable getDisplayMaps()
/*     */   {
/* 533 */     return this.m_displayMaps;
/*     */   }
/*     */ 
/*     */   public String[][] getDisplayMap(String key)
/*     */   {
/* 538 */     return (String[][])(String[][])this.m_displayMaps.get(key);
/*     */   }
/*     */ 
/*     */   public Vector createTableFieldsList(String dataSource)
/*     */     throws DataException
/*     */   {
/* 547 */     this.m_fields = new IdcVector();
/*     */ 
/* 549 */     String[][] tableDef = (String[][])(String[][])this.m_tableDefs.get(dataSource);
/* 550 */     if (tableDef == null)
/*     */     {
/* 552 */       String msg = LocaleUtils.encodeMessage("apDataSourceNotDefined", null, dataSource);
/*     */ 
/* 554 */       throw new DataException(msg);
/*     */     }
/* 556 */     for (int i = 0; i < tableDef.length; ++i)
/*     */     {
/* 558 */       ViewFieldDef tempDef = addFieldDef(tableDef[i][0], tableDef[i][1]);
/* 559 */       tempDef.m_isOptionList = true;
/* 560 */       tempDef.m_optionListKey = tableDef[i][2];
/* 561 */       tempDef.m_isOptionList = (tempDef.m_optionListKey.length() > 0);
/* 562 */       tempDef.m_type = tableDef[i][3];
/* 563 */       if (!tempDef.m_isOptionList)
/*     */         continue;
/* 565 */       String optListType = this.m_optListTypesMap.getProperty(tempDef.m_optionListKey);
/* 566 */       if (optListType == null)
/*     */         continue;
/* 568 */       tempDef.m_optionListType = optListType;
/*     */     }
/*     */ 
/* 572 */     return this.m_fields;
/*     */   }
/*     */ 
/*     */   public String getDisplayString(String fieldName, ResultSet curRow, String val, ExecutionContext cxt)
/*     */   {
/* 579 */     int nfields = this.m_fields.size();
/* 580 */     for (int i = 0; i < nfields; ++i)
/*     */     {
/* 582 */       FieldDef fieldDef = (FieldDef)this.m_fields.elementAt(i);
/* 583 */       if (!fieldDef.m_name.equals(fieldName))
/*     */         continue;
/* 585 */       if (!fieldDef.m_isOptionList)
/*     */         break;
/* 587 */       String optKey = fieldDef.m_optionListKey;
/* 588 */       String[][] displayMap = (String[][])(String[][])this.m_displayMaps.get(optKey);
/* 589 */       if (displayMap != null)
/*     */       {
/* 591 */         val = StringUtils.getPresentationString(displayMap, val);
/* 592 */         val = LocaleResources.getString(val, cxt);
/* 593 */         return val;
/*     */       }
/* 595 */       break;
/*     */     }
/*     */ 
/* 599 */     return val;
/*     */   }
/*     */ 
/*     */   public ViewFieldDef addFieldDef(String name, String caption)
/*     */   {
/* 608 */     ViewFieldDef fieldDef = new ViewFieldDef();
/* 609 */     fieldDef.m_name = name;
/* 610 */     fieldDef.m_caption = caption;
/* 611 */     this.m_fields.addElement(fieldDef);
/* 612 */     return fieldDef;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 617 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94630 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.TableFields
 * JD-Core Version:    0.5.4
 */