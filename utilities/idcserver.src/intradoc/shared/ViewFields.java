/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataExchange;
/*     */ import intradoc.data.DataExchangeBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ViewFields
/*     */   implements DataExchangeBinder
/*     */ {
/*  55 */   public ExecutionContext m_cxt = null;
/*     */ 
/*  58 */   public Vector m_viewFields = null;
/*     */ 
/*  61 */   public boolean m_searchableOnly = false;
/*     */ 
/*  64 */   public boolean m_enabledOnly = false;
/*     */ 
/*  67 */   public boolean m_isOptionListOnly = false;
/*     */ 
/*  71 */   public boolean m_isTextOnly = false;
/*     */ 
/*  74 */   public boolean m_isAllowPlaceHolderFields = true;
/*     */ 
/*  77 */   public TableFields m_tableFields = null;
/*     */ 
/*  80 */   public boolean m_hasDocAccount = false;
/*     */ 
/*  83 */   public boolean m_useAltaVista = false;
/*     */ 
/*  86 */   public static final String[] BOUND_DOCMETA_FIELDS = { "dIsEnabled", "dIsSearchable", "dName", "dType", "dCaption", "dDefaultValue", "dOptionListKey", "dIsOptionList", "dOptionListType", "dIsPlaceholderField" };
/*     */   public static final int SEARCHABLE_ONLY = 1;
/*     */   public static final int ADD_STD_DOC_FIELDS = 2;
/*     */   public static final int ADD_DOC_FORMAT_FIELDS = 4;
/*     */   public static final int ADD_SPECIAL_SEARCH_FIELDS = 8;
/*     */   public static final int ADD_FILE_FIELDS = 16;
/*  99 */   public static final String[][] FIELD_LABELS = { { "dDocName", "apTitleDocName", "text" }, { "dDocTitle", "apTitleDocTitle", "text" }, { "dDocAuthor", "apTitleDocAuthor", "text" }, { "dDocType", "apTitleDocType", "text" }, { "dSecurityGroup", "apTitleSecurityGroup", "text" }, { "dRevLabel", "apTitleRevision", "text" }, { "dDocAccount", "apTitleAccount", "text" }, { "dCreateDate", "apTitleCreateDate", "date" }, { "dReleaseDate", "apTitleReleaseDate", "date" }, { "dInDate", "apTitleInDate", "date" }, { "dOutDate", "apTitleOutDate", "date" } };
/*     */ 
/* 116 */   public static final String[][] DOCUMENT_FLAG_INFO = { { "dIsCheckedOut", "apTitleCheckedOut", "YesNo", "yes/no", "0" }, { "dCheckoutUser", "apTitleCheckedOutBy", "userView", "text", "1" }, { "dStatus", "apTitleRevisionStatus", "StatusList", "text", "0" }, { "dReleaseState", "apTitleIndexerStatus", "ReleaseStateList", "text", "0" }, { "dProcessingState", "apTitleConversionStatus", "ProcessingStateList", "text", "0" }, { "dIndexerState", "apTitleIndexerCycle", "IndexerStateList", "text", "0" }, { "dWorkflowState", "apTitleWorkflowState", "WorkflowStateList", "text", "0" }, { "dRevRank", "apTitleRevRank", "", "int", "0" } };
/*     */ 
/* 128 */   public static final String[][] EXTRA_DOCUMENT_FLAG_INFO = { { "dRevClassID", "apTitleRevisionID", "", "int", "0" }, { "dFileSize", "apTitleVaultFileSize", "", "int", "0" }, { "dExtension", "apTitleExtension", "", "text", "0" }, { "dWebExtension", "apTitleWebExtension", "", "text", "0" }, { "dMessage", "apTitleErrorMessage", "", "text", "0" } };
/*     */ 
/* 137 */   public static final String[][] PUBLISH_FLAG_INFO = { { "dPublishType", "apTitlePublishType", "PublishTypeList", "text", "0" }, { "dPublishState", "apTitlePublishStatus", "PublishStateList", "text", "0" } };
/*     */ 
/* 143 */   public static final String[][] DOCUMENT_REV_INFO = { { "isLatestRev", "apTitleLatestRevision", "", "", "" } };
/*     */ 
/* 148 */   public static final String[][] RENDITION_INFO = { { "dRendition", "apTitleAdditionalRendition", "AdditionalRenditions", "text", "0" } };
/*     */ 
/* 153 */   public static final String[][] DOCUMENT_FORMATS = { { "primaryFile:format", "apTitlePrimaryFormat", "", "text", "0" }, { "alternateFile:format", "apTitleAlternateFormat", "", "text", "0" }, { "webViewableFile:format", "apTitleWebViewableFormat", "", "text", "0" } };
/*     */ 
/* 160 */   public static final String[][] SPECIAL_INDEX_FIELDS = { { "dOriginalName", "apTitleOriginalFileName" }, { "dFormat", "apTitleVaultFileFormat" }, { "AlternateFormat", "apTitleAlternateFileFormat" }, { "VaultFileSize", "apTitleVaultFileSize" }, { "AlternateFileSize", "apTitleAlternateFileSize" }, { "WebFileSize", "apTitleWebFileSize" } };
/*     */ 
/* 170 */   public static final String[][] AVS_SPECIAL_INDEX_FIELDS = { { "dOriginalName", "apTitleOriginalFileName" }, { "dFormat", "apTitleVaultFileFormat" }, { "AlternateFormat", "apTitleAlternateFileFormat" }, { "VaultSize", "apTitleVaultFileSize" }, { "AltSize", "apTitleAlternateFileSize" }, { "WebSize", "apTitleWebFileSize" } };
/*     */ 
/* 180 */   public static final String[][] DOCUMENT_FILE_INFO = { { "dWebExtension", "apTitleWebExtension", "", "text", "0" }, { "dOriginalName", "apTitleOriginalName", "", "text", "0" }, { "alternateFile:name", "apTitleAlternateOriginalName", "", "text", "0" }, { "webViewableFile:name", "apTitleWebViewableOriginalName", "", "text", "0" } };
/*     */ 
/* 191 */   public static final String[] BOUND_USERMETA_FIELDS = { "<unusedIsEnabled>", "<unusedIsSearchable>", "umdName", "umdType", "umdCaption", "<unusedDefaultValue>", "umdOptionListKey", "umdIsOptionList", "umdOptionListType" };
/*     */ 
/* 197 */   public static final String[][] USER_FIELD_INFO = { { "dName", "apTitleUserName", "", "text", "0" }, { "dFullName", "apTitleFullName", "", "text", "0" }, { "dUserType", "apTitleType", "", "text", "0" }, { "dUserAuthType", "apTitleAuthType", "UserAuthTypes", "text", "0" }, { "dEmail", "apTitleEmailAddress", "", "bigtext", "0" }, { "dUserOrgPath", "apTitleOrganization", "Users_OrgPathList", "text", "0" }, { "dUserSourceOrgPath", "apTitleSource", "", "text", "0" } };
/*     */ 
/* 208 */   public Hashtable m_localizationMap = new Hashtable();
/* 209 */   public Properties m_fieldLabels = new Properties();
/*     */ 
/*     */   public ViewFields(ExecutionContext cxt)
/*     */   {
/* 214 */     this.m_cxt = cxt;
/* 215 */     this.m_viewFields = new IdcVector();
/*     */ 
/* 217 */     this.m_tableFields = new TableFields();
/* 218 */     this.m_tableFields.init();
/*     */ 
/* 220 */     this.m_localizationMap.put("FieldLabels", FIELD_LABELS);
/* 221 */     this.m_localizationMap.put("DocumentFlagInfo", DOCUMENT_FLAG_INFO);
/* 222 */     this.m_localizationMap.put("ExtraDocumentFlagInfo", EXTRA_DOCUMENT_FLAG_INFO);
/* 223 */     this.m_localizationMap.put("PublishFlagInfo", PUBLISH_FLAG_INFO);
/* 224 */     this.m_localizationMap.put("DocumentRevInfo", DOCUMENT_REV_INFO);
/* 225 */     this.m_localizationMap.put("RenditionInfo", RENDITION_INFO);
/* 226 */     this.m_localizationMap.put("DocumentFormats", DOCUMENT_FORMATS);
/* 227 */     this.m_localizationMap.put("DocumentFileInfo", DOCUMENT_FILE_INFO);
/*     */ 
/* 229 */     this.m_useAltaVista = SharedObjects.getEnvValueAsBoolean("UseAltaVista", false);
/* 230 */     if (this.m_useAltaVista)
/*     */     {
/* 232 */       this.m_localizationMap.put("SpecialIndexFields", AVS_SPECIAL_INDEX_FIELDS);
/*     */     }
/*     */     else
/*     */     {
/* 236 */       this.m_localizationMap.put("SpecialIndexFields", SPECIAL_INDEX_FIELDS);
/*     */     }
/* 238 */     this.m_localizationMap.put("UserFieldInfo", USER_FIELD_INFO);
/*     */ 
/* 240 */     int num = FIELD_LABELS.length;
/* 241 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 243 */       this.m_fieldLabels.put(FIELD_LABELS[i][0], FIELD_LABELS[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public ViewFieldDef addViewFieldDef(String name, String caption)
/*     */   {
/* 251 */     ViewFieldDef fieldDef = new ViewFieldDef();
/* 252 */     fieldDef.m_name = name;
/* 253 */     fieldDef.m_caption = caption;
/* 254 */     this.m_viewFields.addElement(fieldDef);
/* 255 */     return fieldDef;
/*     */   }
/*     */ 
/*     */   public ViewFieldDef addViewFieldDefFromInfo(FieldInfo fi)
/*     */   {
/* 260 */     ViewFieldDef fieldDef = new ViewFieldDef();
/* 261 */     fieldDef.m_name = fi.m_name;
/* 262 */     fieldDef.m_caption = fi.m_name;
/* 263 */     this.m_viewFields.addElement(fieldDef);
/*     */ 
/* 265 */     switch (fi.m_type)
/*     */     {
/*     */     case 5:
/* 268 */       fieldDef.m_type = "date";
/* 269 */       break;
/*     */     case 3:
/* 271 */       fieldDef.m_type = "int";
/* 272 */       break;
/*     */     case 1:
/* 274 */       fieldDef.m_type = "yes/no";
/* 275 */       break;
/*     */     case 2:
/*     */     case 4:
/*     */     default:
/* 277 */       fieldDef.m_type = "text";
/*     */     }
/*     */ 
/* 280 */     return fieldDef;
/*     */   }
/*     */ 
/*     */   public Vector createDocumentFieldsList(ResultSet metaFields)
/*     */     throws DataException
/*     */   {
/* 287 */     int flags = 2;
/* 288 */     return createDocumentFieldsListWithFlags(metaFields, flags);
/*     */   }
/*     */ 
/*     */   public Vector createSearchableFieldsList(ResultSet metaFields)
/*     */     throws DataException
/*     */   {
/* 294 */     int flags = 11;
/* 295 */     return createDocumentFieldsListWithFlags(metaFields, flags);
/*     */   }
/*     */ 
/*     */   public Vector createDocumentFieldsListEx(ResultSet metaFields, boolean allDocFields, boolean searchableOnly)
/*     */     throws DataException
/*     */   {
/* 302 */     int flags = 0;
/* 303 */     if (allDocFields)
/*     */     {
/* 305 */       flags += 2;
/*     */     }
/* 307 */     if (searchableOnly)
/*     */     {
/* 309 */       ++flags;
/*     */     }
/*     */ 
/* 312 */     return createDocumentFieldsListWithFlags(metaFields, flags);
/*     */   }
/*     */ 
/*     */   public Vector createAllDocumentFieldsList(ResultSet metaFields, boolean allDocFields, boolean searchableOnly, boolean hasDocFormats, boolean hasSpecialSearchFields)
/*     */     throws DataException
/*     */   {
/* 319 */     int flags = 0;
/* 320 */     if (allDocFields)
/*     */     {
/* 322 */       flags += 2;
/*     */     }
/* 324 */     if (searchableOnly)
/*     */     {
/* 326 */       ++flags;
/*     */     }
/* 328 */     if (hasDocFormats)
/*     */     {
/* 330 */       flags += 4;
/*     */     }
/* 332 */     if (hasSpecialSearchFields)
/*     */     {
/* 334 */       flags += 8;
/*     */     }
/*     */ 
/* 337 */     return createDocumentFieldsListWithFlags(metaFields, flags);
/*     */   }
/*     */ 
/*     */   public Vector createDocumentFieldsListWithFlags(ResultSet metaFields, int flags) throws DataException
/*     */   {
/* 342 */     this.m_viewFields = new IdcVector();
/* 343 */     this.m_searchableOnly = ((flags & 0x1) != 0);
/*     */ 
/* 345 */     if ((flags & 0x2) != 0)
/*     */     {
/* 349 */       addStandardDocFields();
/* 350 */       addDocDateFields(false, false);
/* 351 */       addFlags(PUBLISH_FLAG_INFO, 1);
/*     */     }
/*     */ 
/* 354 */     if ((flags & 0x4) != 0)
/*     */     {
/* 356 */       addDocumentFormatFields();
/*     */     }
/*     */ 
/* 359 */     if ((flags & 0x8) != 0)
/*     */     {
/* 361 */       addSpecialSearchFields();
/*     */     }
/*     */ 
/* 364 */     if ((flags & 0x10) != 0)
/*     */     {
/* 366 */       addDocumentFileFields();
/*     */     }
/*     */ 
/* 369 */     addMetaFields(metaFields);
/* 370 */     return this.m_viewFields;
/*     */   }
/*     */ 
/*     */   public String[][] createArchiverCommonFieldsList()
/*     */   {
/* 375 */     this.m_viewFields = new IdcVector();
/* 376 */     addStandardDocFields();
/*     */ 
/* 378 */     addDocDateFields(false, false);
/* 379 */     addFlags(PUBLISH_FLAG_INFO, 1);
/* 380 */     addDocumentFormatFields();
/*     */ 
/* 382 */     int size = this.m_viewFields.size();
/* 383 */     String[][] temp = new String[size][];
/*     */ 
/* 385 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 387 */       FieldDef fd = (FieldDef)this.m_viewFields.elementAt(i);
/* 388 */       temp[i] = { fd.m_name, LocaleResources.getString(fd.m_caption, this.m_cxt) };
/*     */     }
/* 390 */     return temp;
/*     */   }
/*     */ 
/*     */   public Vector createUserViewFields(ResultSet userMetaFields)
/*     */     throws DataException
/*     */   {
/* 396 */     this.m_viewFields = new IdcVector();
/* 397 */     addMetaFieldsWithBoundList(userMetaFields, BOUND_USERMETA_FIELDS, false, false);
/* 398 */     Vector metaFields = this.m_viewFields;
/*     */ 
/* 401 */     this.m_viewFields = new IdcVector();
/* 402 */     addFlags(USER_FIELD_INFO);
/*     */ 
/* 406 */     mergeFields(metaFields, "dEmail");
/*     */ 
/* 408 */     return this.m_viewFields;
/*     */   }
/*     */ 
/*     */   public Vector createAppViewFields(String target) throws DataException
/*     */   {
/* 413 */     this.m_viewFields = new IdcVector();
/*     */ 
/* 415 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/* 416 */     SchemaViewData view = (SchemaViewData)views.getData("ApplicationFields");
/* 417 */     DataResultSet rset = (DataResultSet)view.getAllViewValues();
/*     */ 
/* 419 */     ResultSetUtils.sortResultSet(rset, new String[] { "schFieldName" });
/* 420 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 422 */       Properties props = rset.getCurrentRowProps();
/* 423 */       ViewFieldDef fieldDef = addViewFieldDef(props.getProperty("schFieldName"), props.getProperty("schFieldCaption"));
/*     */ 
/* 427 */       fieldDef.m_type = props.getProperty("schFieldType");
/* 428 */       fieldDef.m_isOptionList = StringUtils.convertToBool(props.getProperty("dIsOptionList"), false);
/* 429 */       if (fieldDef.m_isOptionList)
/*     */       {
/* 431 */         fieldDef.m_optionListType = props.getProperty("dOptionListType");
/* 432 */         fieldDef.m_optionListKey = props.getProperty("dOptionListKey");
/*     */       }
/* 434 */       fieldDef.m_isAppField = true;
/*     */     }
/*     */ 
/* 437 */     return this.m_viewFields;
/*     */   }
/*     */ 
/*     */   public void mergeFields(Vector newFields, String fieldAfter)
/*     */   {
/* 442 */     int afterIndex = findFieldIndex(this.m_viewFields, fieldAfter);
/* 443 */     if (afterIndex < 0)
/*     */     {
/* 446 */       afterIndex = this.m_viewFields.size();
/*     */     }
/*     */     else
/*     */     {
/* 451 */       ++afterIndex;
/*     */     }
/*     */ 
/* 454 */     int nNewFields = newFields.size();
/* 455 */     for (int i = 0; i < nNewFields; ++i)
/*     */     {
/* 457 */       ViewFieldDef vf = (ViewFieldDef)newFields.elementAt(i);
/* 458 */       int locIndex = findFieldIndex(this.m_viewFields, vf.m_name);
/* 459 */       if (locIndex >= 0)
/*     */       {
/* 463 */         this.m_viewFields.setElementAt(vf, locIndex);
/*     */       }
/*     */       else
/*     */       {
/* 468 */         this.m_viewFields.insertElementAt(vf, afterIndex);
/* 469 */         ++afterIndex;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int findFieldIndex(Vector viewFields, String fieldName)
/*     */   {
/* 476 */     int size = viewFields.size();
/* 477 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 479 */       ViewFieldDef vf = (ViewFieldDef)viewFields.elementAt(i);
/* 480 */       if (vf.m_name.equals(fieldName))
/*     */       {
/* 482 */         return i;
/*     */       }
/*     */     }
/* 485 */     return -1;
/*     */   }
/*     */ 
/*     */   public void addMetaFields(ResultSet metaFields) throws DataException
/*     */   {
/* 490 */     addMetaFieldsEx(metaFields, false, false);
/*     */   }
/*     */ 
/*     */   public void addMetaFieldsEx(ResultSet metaFields, boolean isTextOnly, boolean optionListOnly)
/*     */     throws DataException
/*     */   {
/* 497 */     addMetaFieldsWithBoundList(metaFields, BOUND_DOCMETA_FIELDS, isTextOnly, optionListOnly);
/*     */   }
/*     */ 
/*     */   public void addMetaFieldsWithBoundList(ResultSet metaFields, String[] boundList, boolean isTextOnly, boolean optionListOnly)
/*     */     throws DataException
/*     */   {
/* 504 */     if (metaFields == null)
/*     */     {
/* 506 */       String msg = LocaleUtils.encodeMessage("csUnableToFindTable", null, "MetaDefinition");
/*     */ 
/* 508 */       throw new DataException(msg);
/*     */     }
/* 510 */     this.m_isTextOnly = isTextOnly;
/* 511 */     this.m_isOptionListOnly = optionListOnly;
/* 512 */     DataExchange exch = new DataExchange(metaFields, boundList);
/* 513 */     exch.doExchange(this, false);
/*     */   }
/*     */ 
/*     */   public void addStandardDocFields()
/*     */   {
/* 518 */     ViewFieldDef tempDef = null;
/*     */ 
/* 520 */     tempDef = addViewFieldDef("dDocName", this.m_fieldLabels.getProperty("dDocName"));
/* 521 */     tempDef.m_hasView = true;
/* 522 */     tempDef.m_optionListKey = "docView";
/*     */ 
/* 524 */     tempDef = addViewFieldDef("dDocTitle", this.m_fieldLabels.getProperty("dDocTitle"));
/*     */ 
/* 526 */     addDocOptionFields();
/*     */ 
/* 528 */     addSpecialExtensionFields();
/*     */ 
/* 530 */     tempDef = addViewFieldDef("dRevLabel", this.m_fieldLabels.getProperty("dRevLabel"));
/*     */   }
/*     */ 
/*     */   public void addSpecialExtensionFields()
/*     */   {
/* 536 */     if ((!SharedObjects.getEnvValueAsBoolean("UseAccounts", false)) && (!SharedObjects.getEnvValueAsBoolean("UseCollaboration", false))) {
/*     */       return;
/*     */     }
/* 539 */     ViewFieldDef tempDef = null;
/*     */ 
/* 541 */     tempDef = addViewFieldDef("dDocAccount", this.m_fieldLabels.getProperty("dDocAccount"));
/* 542 */     tempDef.m_isOptionList = true;
/* 543 */     tempDef.m_optionListKey = "docAccounts";
/* 544 */     tempDef.m_optionListType = "combo";
/* 545 */     this.m_hasDocAccount = true;
/*     */   }
/*     */ 
/*     */   public void addSpecialProfileRuleFields()
/*     */   {
/* 551 */     String specialProfileRuleFields = SharedObjects.getEnvironmentValue("SpecialProfileRuleFields");
/* 552 */     if ((specialProfileRuleFields == null) || (specialProfileRuleFields.length() <= 0))
/*     */       return;
/* 554 */     List fields = StringUtils.makeListFromSequenceSimple(specialProfileRuleFields);
/*     */ 
/* 556 */     if (fields.size() % 2 == 1)
/*     */     {
/* 558 */       Report.trace("system", "SpecialProfileRuleFields variable has odd number of items (even number is required).", null);
/* 559 */       return;
/*     */     }
/*     */ 
/* 562 */     for (int i = 0; i < fields.size(); ++i)
/*     */     {
/* 564 */       String fieldName = (String)fields.get(i);
/* 565 */       String fieldLabel = (String)fields.get(++i);
/* 566 */       addViewFieldDef(fieldName, fieldLabel);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addDocumentFormatFields()
/*     */   {
/* 573 */     for (int i = 0; i < DOCUMENT_FORMATS.length; ++i)
/*     */     {
/* 575 */       ViewFieldDef tempDef = addViewFieldDef(DOCUMENT_FORMATS[i][0], DOCUMENT_FORMATS[i][1]);
/* 576 */       tempDef.m_isOptionList = true;
/* 577 */       tempDef.m_optionListKey = "docFormats";
/* 578 */       tempDef.m_optionListType = "combo";
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addDocumentFileFields()
/*     */   {
/* 584 */     for (int i = 0; i < DOCUMENT_FILE_INFO.length; ++i)
/*     */     {
/* 586 */       addViewFieldDef(DOCUMENT_FILE_INFO[i][0], DOCUMENT_FILE_INFO[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addSpecialSearchFields()
/*     */   {
/* 592 */     String[][] specialFields = (String[][])null;
/* 593 */     if (this.m_useAltaVista)
/*     */     {
/* 595 */       specialFields = AVS_SPECIAL_INDEX_FIELDS;
/*     */     }
/*     */     else
/*     */     {
/* 599 */       specialFields = SPECIAL_INDEX_FIELDS;
/*     */     }
/*     */ 
/* 602 */     for (int i = 0; i < specialFields.length; ++i)
/*     */     {
/* 604 */       addViewFieldDef(specialFields[i][0], specialFields[i][1]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addArchiverDocFlags()
/*     */   {
/* 610 */     addRenditions();
/* 611 */     addFlags(DOCUMENT_FLAG_INFO, 0);
/* 612 */     addFlags(PUBLISH_FLAG_INFO);
/*     */   }
/*     */ 
/*     */   public void addDocFlags(boolean isAddRev)
/*     */   {
/* 617 */     addRenditions();
/*     */ 
/* 619 */     addFlags(DOCUMENT_FLAG_INFO);
/* 620 */     addFlags(EXTRA_DOCUMENT_FLAG_INFO);
/* 621 */     addFlags(PUBLISH_FLAG_INFO);
/*     */ 
/* 623 */     if (!isAddRev)
/*     */       return;
/* 625 */     addFlags(DOCUMENT_REV_INFO);
/*     */   }
/*     */ 
/*     */   public void addFlags(String[][] flagInfo, int less)
/*     */   {
/* 631 */     ViewFieldDef tempDef = null;
/* 632 */     int len = flagInfo.length - less;
/* 633 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 635 */       tempDef = addViewFieldDef(flagInfo[i][0], flagInfo[i][1]);
/* 636 */       tempDef.m_optionListKey = flagInfo[i][2];
/* 637 */       if (tempDef.m_optionListKey.length() > 0)
/*     */       {
/* 639 */         boolean hasView = StringUtils.convertToBool(flagInfo[i][4], false);
/* 640 */         tempDef.m_hasView = hasView;
/* 641 */         tempDef.m_isOptionList = (!hasView);
/*     */       }
/* 643 */       tempDef.m_type = flagInfo[i][3];
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addFlags(String[][] flagInfo)
/*     */   {
/* 649 */     addFlags(flagInfo, 0);
/*     */   }
/*     */ 
/*     */   public void addRenditions()
/*     */   {
/* 654 */     int numRenditions = SharedObjects.getEnvironmentInt("NumAdditionalRenditions", 0);
/* 655 */     if (numRenditions == 0)
/*     */     {
/* 657 */       return;
/*     */     }
/*     */ 
/* 660 */     if (numRenditions > AdditionalRenditions.m_maxNum)
/*     */     {
/* 662 */       numRenditions = AdditionalRenditions.m_maxNum;
/*     */     }
/*     */ 
/* 666 */     AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/*     */ 
/* 668 */     if (renSet != null)
/*     */     {
/* 670 */       String[][] rendDisplayMap = renSet.createDisplayMap(this.m_cxt);
/* 671 */       this.m_tableFields.m_displayMaps.put("AdditionalRenditions", rendDisplayMap);
/*     */     }
/*     */ 
/* 674 */     for (int i = 0; i < numRenditions; ++i)
/*     */     {
/* 676 */       int counter = i + 1;
/* 677 */       String name = RENDITION_INFO[0][0] + counter;
/* 678 */       String label = RENDITION_INFO[0][1];
/* 679 */       if (numRenditions > 1)
/*     */       {
/* 681 */         label = label + " " + counter;
/*     */       }
/*     */ 
/* 684 */       ViewFieldDef tempDef = addViewFieldDef(name, label);
/* 685 */       tempDef.m_optionListKey = RENDITION_INFO[0][2];
/* 686 */       tempDef.m_isOptionList = true;
/* 687 */       tempDef.m_type = RENDITION_INFO[0][3];
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addDocDateFields(boolean useCreateDate, boolean useReleaseDate)
/*     */   {
/* 693 */     ViewFieldDef tempDef = null;
/*     */ 
/* 695 */     if (useCreateDate)
/*     */     {
/* 697 */       tempDef = addViewFieldDef("dCreateDate", this.m_fieldLabels.getProperty("dCreateDate"));
/* 698 */       tempDef.m_type = "date";
/* 699 */       tempDef.m_isStandardDateField = true;
/*     */     }
/*     */ 
/* 702 */     if (useReleaseDate)
/*     */     {
/* 704 */       tempDef = addViewFieldDef("dReleaseDate", this.m_fieldLabels.getProperty("dReleaseDate"));
/* 705 */       tempDef.m_type = "date";
/* 706 */       tempDef.m_isStandardDateField = true;
/*     */     }
/*     */ 
/* 709 */     tempDef = addViewFieldDef("dInDate", this.m_fieldLabels.getProperty("dInDate"));
/* 710 */     tempDef.m_type = "date";
/* 711 */     tempDef.m_isStandardDateField = true;
/*     */ 
/* 713 */     tempDef = addViewFieldDef("dOutDate", this.m_fieldLabels.getProperty("dOutDate"));
/* 714 */     tempDef.m_type = "date";
/* 715 */     tempDef.m_isStandardDateField = true;
/*     */   }
/*     */ 
/*     */   public void addDocOptionFields()
/*     */   {
/* 720 */     addDocOptionFieldsEx(true);
/*     */   }
/*     */ 
/*     */   public void addDocOptionFieldsEx(boolean isAddUser)
/*     */   {
/* 725 */     ViewFieldDef tempDef = null;
/*     */ 
/* 727 */     if (isAddUser)
/*     */     {
/* 729 */       tempDef = addViewFieldDef("dDocAuthor", this.m_fieldLabels.getProperty("dDocAuthor"));
/* 730 */       tempDef.m_hasView = true;
/* 731 */       tempDef.m_optionListKey = "userView";
/* 732 */       if (SharedObjects.getEnvValueAsBoolean("HasExternalUsers", false))
/*     */       {
/* 734 */         tempDef.m_optionListType = "combo";
/*     */       }
/*     */     }
/*     */ 
/* 738 */     tempDef = addViewFieldDef("dDocType", this.m_fieldLabels.getProperty("dDocType"));
/* 739 */     tempDef.m_isOptionList = true;
/* 740 */     tempDef.m_optionListKey = "docTypes";
/*     */ 
/* 742 */     tempDef = addViewFieldDef("dSecurityGroup", this.m_fieldLabels.getProperty("dSecurityGroup"));
/* 743 */     tempDef.m_isOptionList = true;
/* 744 */     tempDef.m_optionListKey = "securityGroups";
/* 745 */     tempDef.m_default = "Public";
/*     */   }
/*     */ 
/*     */   public ViewFieldDef addField(String name, String caption)
/*     */   {
/* 750 */     return addViewFieldDef(name, caption);
/*     */   }
/*     */ 
/*     */   public boolean prepareNextRow(DataExchange exch, boolean writeToResultSet)
/*     */   {
/* 758 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean exchange(DataExchange exch, int index, boolean writeToResultSet)
/*     */     throws DataException
/*     */   {
/* 765 */     if (writeToResultSet == true)
/*     */     {
/* 767 */       return false;
/*     */     }
/*     */ 
/* 771 */     boolean isTrue = false;
/* 772 */     String val = null;
/* 773 */     if ((index < 2) || (index == 7) || (index == 9))
/*     */     {
/* 775 */       isTrue = exch.getCurValAsBoolean();
/*     */     }
/*     */     else
/*     */     {
/* 779 */       val = exch.getCurValAsString();
/*     */     }
/*     */ 
/* 783 */     if (index < 2)
/*     */     {
/* 785 */       isTrue = exch.getCurValAsBoolean();
/* 786 */       if (!isTrue)
/*     */       {
/* 788 */         if (index == 0)
/*     */         {
/* 790 */           if (this.m_enabledOnly)
/*     */           {
/* 792 */             return false;
/*     */           }
/*     */         }
/* 795 */         else if (this.m_searchableOnly == true)
/*     */         {
/* 797 */           return false;
/*     */         }
/*     */       }
/* 800 */       return true;
/*     */     }
/* 802 */     if (index == 9)
/*     */     {
/* 806 */       return (isTrue != true) || (this.m_isAllowPlaceHolderFields);
/*     */     }
/*     */ 
/* 813 */     ViewFieldDef fieldDef = null;
/* 814 */     if (index == 2)
/*     */     {
/* 816 */       fieldDef = new ViewFieldDef();
/* 817 */       exch.m_curObj = fieldDef;
/* 818 */       fieldDef.m_name = val;
/* 819 */       fieldDef.m_isCustomMeta = true;
/* 820 */       return true;
/*     */     }
/*     */ 
/* 824 */     fieldDef = (ViewFieldDef)exch.m_curObj;
/*     */ 
/* 827 */     switch (index)
/*     */     {
/*     */     case 3:
/* 831 */       if ((this.m_isTextOnly) && ((
/* 835 */         (val.equalsIgnoreCase("Text")) || (val.equalsIgnoreCase("BigText")))))
/*     */       {
/* 837 */         return false;
/*     */       }
/*     */ 
/* 840 */       fieldDef.m_type = val;
/* 841 */       break;
/*     */     case 4:
/* 843 */       fieldDef.m_caption = LocaleResources.getString(val, this.m_cxt);
/* 844 */       break;
/*     */     case 5:
/* 846 */       fieldDef.m_default = val;
/* 847 */       break;
/*     */     case 6:
/* 849 */       fieldDef.m_optionListKey = val;
/* 850 */       break;
/*     */     case 7:
/* 852 */       fieldDef.m_isOptionList = isTrue;
/* 853 */       if ((!this.m_isOptionListOnly) || (isTrue))
/*     */         break label300;
/* 855 */       return false;
/*     */     case 8:
/* 859 */       fieldDef.m_optionListType = val;
/*     */     }
/*     */ 
/* 863 */     label300: return true;
/*     */   }
/*     */ 
/*     */   public void finalizeObject(DataExchange exch, boolean writeToResultSet)
/*     */     throws DataException
/*     */   {
/* 869 */     this.m_viewFields.addElement(exch.m_curObj);
/*     */   }
/*     */ 
/*     */   public Hashtable getDisplayMaps()
/*     */   {
/* 877 */     return this.m_tableFields.getDisplayMaps();
/*     */   }
/*     */ 
/*     */   public String[][] getDisplayMap(String key)
/*     */   {
/* 882 */     return this.m_tableFields.getDisplayMap(key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 887 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102751 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ViewFields
 * JD-Core Version:    0.5.4
 */