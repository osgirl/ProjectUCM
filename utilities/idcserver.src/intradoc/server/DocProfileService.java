/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.schema.SchemaFieldConfig;
/*     */ import intradoc.shared.schema.SchemaFieldData;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocProfileService extends Service
/*     */ {
/*  62 */   protected String m_profileDir = null;
/*     */ 
/*     */   public DocProfileService()
/*     */   {
/*  66 */     this.m_profileDir = DocProfileUtils.getDocumentDir();
/*     */   }
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  72 */     super.createHandlersForService();
/*  73 */     createHandlers("DocumentProfileService");
/*     */   }
/*     */ 
/*     */   public void preActions()
/*     */     throws ServiceException
/*     */   {
/*  79 */     super.preActions();
/*  80 */     FileUtils.reserveDirectory(this.m_profileDir, true);
/*     */   }
/*     */ 
/*     */   public void postActions()
/*     */     throws ServiceException
/*     */   {
/*  86 */     super.postActions();
/*  87 */     FileUtils.releaseDirectory(this.m_profileDir, true);
/*     */   }
/*     */ 
/*     */   public void cleanUp(boolean isError)
/*     */   {
/*  93 */     super.cleanUp(isError);
/*  94 */     FileUtils.releaseDirectory(this.m_profileDir, true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocumentProfiles()
/*     */     throws ServiceException, DataException
/*     */   {
/* 107 */     DataResultSet profileSet = SharedObjects.getTable("DocumentProfiles");
/* 108 */     if (profileSet == null)
/*     */     {
/* 111 */       profileSet = new DataResultSet(DocProfileStorage.DOCPROFILE_COLUMNS);
/*     */     }
/*     */ 
/* 115 */     DataResultSet drset = new DataResultSet();
/* 116 */     drset.copy(profileSet);
/*     */ 
/* 118 */     Vector fields = new IdcVector();
/* 119 */     FieldInfo fi = new FieldInfo();
/* 120 */     fi.m_name = "isValid";
/* 121 */     fields.addElement(fi);
/* 122 */     drset.mergeFieldsWithFlags(fields, 2);
/*     */ 
/* 125 */     String triggerField = DocProfileManager.getTriggerField();
/* 126 */     if ((triggerField != null) && (triggerField.length() > 0))
/*     */     {
/* 128 */       MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 129 */       if (metaData != null)
/*     */       {
/* 131 */         SchemaViewData viewDef = getFieldView(triggerField);
/*     */ 
/* 133 */         int index = ResultSetUtils.getIndexMustExist(drset, "dpTriggerValue");
/* 134 */         int activeIndex = ResultSetUtils.getIndexMustExist(drset, "isValid");
/* 135 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 137 */           String value = drset.getStringValue(index);
/* 138 */           boolean isValid = isValidViewValue(triggerField, metaData, viewDef, value);
/* 139 */           drset.setCurrentValue(activeIndex, "" + isValid);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 144 */     this.m_binder.addResultSet("DocumentProfiles", drset);
/*     */ 
/* 147 */     Properties props = DocProfileManager.getConfiguration();
/* 148 */     Properties localData = this.m_binder.getLocalData();
/* 149 */     DataBinder.mergeHashTables(localData, props);
/*     */ 
/* 152 */     this.m_binder.putLocal("RelativeCgiWebRoot", LegacyDirectoryLocator.getCgiWebUrl(false));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getUserDocumentProfiles()
/*     */     throws ServiceException, DataException
/*     */   {
/* 159 */     DataResultSet profileSet = SharedObjects.getTable("DocumentProfiles");
/* 160 */     if (profileSet == null)
/*     */     {
/* 163 */       profileSet = new DataResultSet(DocProfileStorage.DOCPROFILE_COLUMNS);
/*     */     }
/* 165 */     DataResultSet searchProfileSet = new DataResultSet(DocProfileStorage.DOCPROFILE_COLUMNS);
/* 166 */     DataResultSet checkinProfileSet = new DataResultSet(DocProfileStorage.DOCPROFILE_COLUMNS);
/* 167 */     DataBinder params = new DataBinder();
/* 168 */     params.setLocalData(new Properties());
/* 169 */     params.addResultSet("DocumentProfiles", profileSet);
/* 170 */     int count = 0;
/* 171 */     for (profileSet.first(); profileSet.isRowPresent(); ++count)
/*     */     {
/* 173 */       params.putLocal("dpOrder", "" + count);
/* 174 */       params.putLocal("dpIsCheckin", "1");
/* 175 */       params.putLocal("dpIsSearch", "1");
/* 176 */       params.putLocal("dpCheckinEnabled", "1");
/* 177 */       params.putLocal("dpSearchEnabled", "1");
/* 178 */       Properties row = profileSet.getCurrentRowProps();
/*     */ 
/* 181 */       DocProfileManager.checkProfileLinks("DocumentProfiles", row.getProperty("dpName"), params, this, getPageMerger(), true);
/*     */ 
/* 183 */       boolean isCheckin = StringUtils.convertToBool(params.getLocal("dpIsCheckin"), true);
/* 184 */       boolean isSearch = StringUtils.convertToBool(params.getLocal("dpIsSearch"), true);
/* 185 */       if ((isCheckin) || (isSearch))
/*     */       {
/* 187 */         Vector v = new Vector();
/* 188 */         for (int i = 0; i < DocProfileStorage.DOCPROFILE_COLUMNS.length; ++i)
/*     */         {
/* 190 */           v.add(row.getProperty(DocProfileStorage.DOCPROFILE_COLUMNS[i]));
/*     */         }
/* 192 */         if (isCheckin)
/*     */         {
/* 194 */           checkinProfileSet.addRow(v);
/*     */         }
/* 196 */         if (isSearch)
/*     */         {
/* 198 */           searchProfileSet.addRow(v);
/*     */         }
/*     */       }
/* 171 */       profileSet.next();
/*     */     }
/*     */ 
/* 203 */     this.m_binder.addResultSet("SearchProfiles", searchProfileSet);
/* 204 */     this.m_binder.addResultSet("CheckInProfiles", checkinProfileSet);
/*     */ 
/* 207 */     PluginFilters.filter("getUserDocumentProfiles", this.m_workspace, this.m_binder, this);
/*     */ 
/* 210 */     List stdCheckin = checkinProfileSet.createEmptyRowAsList();
/* 211 */     stdCheckin.set(checkinProfileSet.getFieldInfoIndex("dpName"), "NEW_CHECK_IN");
/* 212 */     String stdCheckinLabel = "wwStandardCheckIn";
/* 213 */     String stdSearchLabel = "wwStandardSearch";
/* 214 */     String blFieldTypes = this.m_binder.getLocal("blFieldTypes");
/* 215 */     boolean localizeLabels = false;
/* 216 */     if (blFieldTypes != null)
/*     */     {
/* 218 */       localizeLabels = blFieldTypes.equalsIgnoreCase("dpDisplayLabel message");
/*     */     }
/* 220 */     if (localizeLabels)
/*     */     {
/* 222 */       stdCheckinLabel = LocaleResources.getString(stdCheckinLabel, this);
/* 223 */       stdSearchLabel = LocaleResources.getString(stdSearchLabel, this);
/*     */     }
/* 225 */     stdCheckin.set(checkinProfileSet.getFieldInfoIndex("dpDisplayLabel"), stdCheckinLabel);
/* 226 */     checkinProfileSet.addRowWithList(stdCheckin);
/* 227 */     List stdSearch = searchProfileSet.createEmptyRowAsList();
/* 228 */     stdSearch.set(searchProfileSet.getFieldInfoIndex("dpName"), "SEARCH");
/* 229 */     stdSearch.set(searchProfileSet.getFieldInfoIndex("dpDisplayLabel"), stdSearchLabel);
/* 230 */     searchProfileSet.addRowWithList(stdSearch);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void filterDocumentProfiles() throws ServiceException, DataException
/*     */   {
/* 236 */     String docClass = this.m_binder.get("dDocClass");
/* 237 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("DocumentProfiles");
/* 238 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 240 */       return;
/*     */     }
/*     */ 
/* 243 */     for (drset.first(); drset.isRowPresent(); )
/*     */     {
/* 245 */       String value = drset.getStringValueByName("dDocClass");
/*     */ 
/* 249 */       if (((value != null) && (value.length() > 0) && (!docClass.equalsIgnoreCase(value))) || ((((value == null) || (value.length() == 0))) && (!docClass.equalsIgnoreCase("Base"))))
/*     */       {
/* 252 */         drset.deleteCurrentRow();
/*     */       }
/*     */       else
/*     */       {
/* 256 */         drset.next();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocumentProfile() throws DataException, ServiceException
/*     */   {
/* 264 */     String name = this.m_binder.get("dpName");
/* 265 */     if (name == null)
/*     */     {
/* 267 */       throw new DataException("!csDpProfileNotDefined");
/*     */     }
/*     */ 
/* 270 */     DocProfileData data = DocProfileManager.getProfile(name);
/* 271 */     if (data == null)
/*     */     {
/* 273 */       String msg = LocaleUtils.encodeMessage("csDpProfileNotDefined2", null, name);
/* 274 */       createServiceException(null, msg);
/*     */     }
/* 276 */     DataBinder binder = data.getData();
/*     */ 
/* 279 */     this.m_binder.merge(binder);
/*     */ 
/* 283 */     DataResultSet docRules = SharedObjects.getTable("DocumentRules");
/* 284 */     this.m_binder.addResultSet("DocumentRules", docRules);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addDocumentProfile() throws ServiceException, DataException
/*     */   {
/* 290 */     addOrEditDocumentProfile(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editDocumentProfile() throws ServiceException, DataException
/*     */   {
/* 296 */     addOrEditDocumentProfile(false);
/*     */   }
/*     */ 
/*     */   public void addOrEditDocumentProfile(boolean isNew)
/*     */     throws ServiceException, DataException
/*     */   {
/* 304 */     boolean isValidateTrigger = StringUtils.convertToBool(this.m_binder.getLocal("isValidateTrigger"), false);
/*     */ 
/* 306 */     if (isValidateTrigger)
/*     */     {
/* 309 */       String triggerField = DocProfileManager.getTriggerField();
/* 310 */       String tValue = this.m_binder.get("dpTriggerValue");
/* 311 */       if ((triggerField != null) && (triggerField.length() > 0))
/*     */       {
/* 313 */         MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 314 */         if (metaData != null)
/*     */         {
/* 316 */           SchemaViewData viewDef = getFieldView(triggerField);
/* 317 */           boolean isValid = isValidViewValue(triggerField, metaData, viewDef, tValue);
/* 318 */           if (!isValid)
/*     */           {
/* 320 */             IdcMessage msg = IdcMessageFactory.lc("csDpInvalidTriggerValue", new Object[] { tValue, triggerField });
/* 321 */             createServiceException(msg);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 326 */       Vector triggerMap = DocProfileManager.getTriggerMapList("Document", this);
/*     */ 
/* 328 */       if (triggerMap != null)
/*     */       {
/* 330 */         String name = this.m_binder.getLocal("dpName");
/* 331 */         int size = triggerMap.size();
/* 332 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 334 */           String[] map = (String[])(String[])triggerMap.elementAt(i);
/* 335 */           if ((!tValue.equals(map[0])) || (name.equals(map[1])))
/*     */             continue;
/* 337 */           IdcMessage msg = IdcMessageFactory.lc("csDpTriggerNotUnique", new Object[] { tValue, map[1] });
/*     */ 
/* 339 */           createServiceExceptionEx(null, LocaleUtils.encodeMessage(msg), -33);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 347 */     this.m_binder.removeLocal("isValidateTrigger");
/*     */ 
/* 351 */     String name = this.m_binder.get("dpName");
/*     */ 
/* 353 */     if (this.m_binder.getActiveAllowMissing("dDocClass") == null)
/*     */     {
/* 355 */       this.m_binder.putLocal("dDocClass", "Base");
/*     */     }
/*     */ 
/* 358 */     DocProfileManager.createOrUpdateProfile(name, this.m_binder, isNew, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteDocumentProfile() throws ServiceException, DataException
/*     */   {
/* 364 */     String name = this.m_binder.getLocal("dpName");
/* 365 */     DocProfileManager.deleteProfile(name, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocumentRules()
/*     */   {
/* 375 */     DataResultSet profileSet = SharedObjects.getTable("DocumentRules");
/* 376 */     if (profileSet == null)
/*     */     {
/* 379 */       profileSet = new DataResultSet(DocProfileStorage.DOCRULE_COLUMNS);
/*     */     }
/*     */ 
/* 382 */     this.m_binder.addResultSet("DocumentRules", profileSet);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void getDocumentRule() throws DataException, ServiceException
/*     */   {
/* 388 */     String name = this.m_binder.get("dpRuleName");
/* 389 */     if (name == null)
/*     */     {
/* 391 */       String msg = LocaleUtils.encodeMessage("csDpRuleParameterMissing", null, "dpRuleName");
/*     */ 
/* 393 */       throw new DataException(msg);
/*     */     }
/* 395 */     DocProfileData data = DocProfileManager.getRule(name);
/* 396 */     if (data == null)
/*     */     {
/* 398 */       String msg = LocaleUtils.encodeMessage("csDpRuleNotDefined", null, name);
/* 399 */       createServiceException(null, msg);
/*     */     }
/* 401 */     DataBinder binder = data.getData();
/*     */ 
/* 404 */     this.m_binder.merge(binder);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addDocumentRule() throws ServiceException, DataException
/*     */   {
/* 410 */     addOrEditDocumentRule(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editDocumentRule() throws ServiceException, DataException
/*     */   {
/* 416 */     addOrEditDocumentRule(false);
/*     */   }
/*     */ 
/*     */   public void addOrEditDocumentRule(boolean isNew)
/*     */     throws ServiceException, DataException
/*     */   {
/* 423 */     String name = this.m_binder.get("dpRuleName");
/* 424 */     DocProfileManager.createOrUpdateRule(name, this.m_binder, isNew, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteDocumentRule()
/*     */     throws ServiceException, DataException
/*     */   {
/* 432 */     String ruleName = this.m_binder.getLocal("dpRuleName");
/* 433 */     String[] profileName = new String[1];
/* 434 */     boolean isInUse = DocProfileManager.isRuleInUse(ruleName, profileName);
/*     */ 
/* 436 */     if (isInUse)
/*     */     {
/* 438 */       String errMsg = LocaleUtils.encodeMessage("csDpRuleInUse", null, profileName[0]);
/* 439 */       createServiceException(null, errMsg);
/*     */     }
/*     */ 
/* 442 */     DocProfileManager.deleteRule(ruleName, this);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void editProfileTrigger()
/*     */     throws ServiceException, DataException
/*     */   {
/* 451 */     String field = this.m_binder.get("dpTriggerField");
/* 452 */     DocProfileManager.updateTrigger(field);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void previewProfile()
/*     */     throws ServiceException, DataException
/*     */   {
/*     */     try
/*     */     {
/* 463 */       String name = this.m_binder.get("dpName");
/* 464 */       if (name == null)
/*     */       {
/* 466 */         throw new DataException("!csDpProfileNotDefined");
/*     */       }
/*     */ 
/* 470 */       boolean isBrowser = StringUtils.convertToBool(this.m_binder.getLocal("isBrowser"), false);
/* 471 */       if (!isBrowser)
/*     */       {
/* 473 */         UserData userData = null;
/* 474 */         String user = this.m_binder.getLocal("userName");
/* 475 */         if ((user != null) && (user.length() > 0))
/*     */         {
/*     */           try
/*     */           {
/* 480 */             userData = UserStorage.retrieveUserDatabaseProfileData(user, this.m_workspace, this);
/* 481 */             setCachedObject("UserData", userData);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 485 */             String msg = LocaleUtils.encodeMessage("csDpUnableToRetreiveUser", null, user);
/* 486 */             createServiceException(e, msg);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 491 */       String docID = this.m_binder.getLocal("docID");
/* 492 */       if ((docID != null) && (docID.length() > 0))
/*     */       {
/* 494 */         Properties props = new Properties();
/* 495 */         props.put("dID", docID);
/* 496 */         PropParameters params = new PropParameters(props);
/* 497 */         ResultSet rset = this.m_workspace.createResultSet("QdocInfo", params);
/* 498 */         if (rset.isEmpty())
/*     */         {
/* 500 */           String msg = LocaleUtils.encodeMessage("csDpDocIDMissing", null, docID);
/* 501 */           createServiceException(null, msg);
/*     */         }
/* 503 */         DataResultSet drset = new DataResultSet();
/* 504 */         drset.copy(rset);
/* 505 */         this.m_binder.addResultSet("DOC_INFO", drset);
/*     */       }
/*     */ 
/* 511 */       String val = this.m_binder.getLocal("IsWorkflow");
/* 512 */       if ((val != null) && (val.length() > 0))
/*     */       {
/* 516 */         boolean isVal = StringUtils.convertToBool(val, false);
/* 517 */         if (!isVal)
/*     */         {
/* 519 */           this.m_binder.putLocal("IsWorkflow", "");
/*     */         }
/*     */       }
/*     */ 
/* 523 */       String triggerValue = this.m_binder.getLocal("dpTriggerValue");
/* 524 */       if ((triggerValue == null) || (triggerValue.length() == 0))
/*     */       {
/* 526 */         createServiceException(null, "!csDpMissingTriggerValue");
/*     */       }
/*     */ 
/* 529 */       if (isBrowser)
/*     */       {
/* 532 */         String templatePage = this.m_binder.getLocal("templatePage");
/* 533 */         if (templatePage == null)
/*     */         {
/* 535 */           templatePage = DocProfileManager.determinePageForTest(this.m_binder);
/*     */         }
/*     */ 
/* 538 */         if (templatePage == null)
/*     */         {
/* 540 */           createServiceException(null, "!csDpPeviewMissingPage");
/*     */         }
/*     */         else
/*     */         {
/* 544 */           this.m_serviceData.m_htmlPage = templatePage;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 549 */         DataBinder resultBinder = DocProfileManager.evaluateGlobalRulesAndProfile(name, this.m_binder, this, true);
/*     */ 
/* 552 */         if (resultBinder != null)
/*     */         {
/* 554 */           Properties resultProps = resultBinder.getLocalData();
/* 555 */           String resultStr = StringUtils.convertToString(resultProps);
/* 556 */           this.m_binder.putLocal("PreviewResults", resultStr);
/*     */ 
/* 558 */           this.m_binder.mergeResultSets(resultBinder);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 565 */       setCachedObject("UserData", this.m_userData);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected SchemaViewData getFieldView(String fieldName)
/*     */   {
/* 576 */     SchemaViewData view = null;
/* 577 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/* 579 */     SchemaFieldConfig fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/* 580 */     SchemaFieldData field = (SchemaFieldData)fields.getData(fieldName);
/*     */ 
/* 582 */     if (field != null)
/*     */     {
/* 584 */       SchemaHelper helper = new SchemaHelper();
/* 585 */       String[] viewName = new String[1];
/* 586 */       boolean isView = helper.isViewFieldEx(field, viewName);
/* 587 */       if (isView)
/*     */       {
/* 589 */         view = (SchemaViewData)views.getData(viewName[0]);
/*     */       }
/*     */     }
/* 592 */     return view;
/*     */   }
/*     */ 
/*     */   protected boolean isValidViewValue(String fieldName, MetaFieldData metaData, SchemaViewData viewData, String value)
/*     */   {
/* 600 */     boolean result = true;
/*     */     try
/*     */     {
/* 603 */       if (viewData != null)
/*     */       {
/* 605 */         Map args = new HashMap();
/* 606 */         args.put("primaryKey", new String[] { value });
/* 607 */         ResultSet rset = viewData.getViewValues(args);
/* 608 */         if (rset == null)
/*     */         {
/* 610 */           result = false;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 615 */         String optionListKey = null;
/* 616 */         if (fieldName.equals("dDocType"))
/*     */         {
/* 618 */           optionListKey = "docTypes";
/*     */         }
/* 620 */         else if (fieldName.equals("dSecurityGroup"))
/*     */         {
/* 622 */           optionListKey = "securityGroups";
/*     */         }
/*     */ 
/* 625 */         if (optionListKey != null)
/*     */         {
/* 627 */           result = metaData.isValueInOptionList(optionListKey, value);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 633 */       Report.trace("docprofile", "isValidViewValue(" + fieldName + "):", e);
/*     */     }
/*     */ 
/* 636 */     return result;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 642 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103866 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocProfileService
 * JD-Core Version:    0.5.4
 */