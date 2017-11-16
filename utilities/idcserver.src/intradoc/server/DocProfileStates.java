/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.shared.DocProfileData;
/*      */ import intradoc.shared.DocProfileScriptUtils;
/*      */ import intradoc.shared.MetaFieldData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.schema.SchemaData;
/*      */ import intradoc.shared.schema.SchemaFieldConfig;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DocProfileStates
/*      */ {
/*      */   protected DataBinder m_binder;
/*      */   protected ExecutionContext m_cxt;
/*      */   protected Hashtable m_fieldDataMap;
/*      */   protected Properties m_fieldToRuleMap;
/*      */   protected Hashtable m_fieldBuddyMap;
/*      */   protected Hashtable m_fieldToInfoMap;
/*      */   protected boolean m_isRequest;
/*      */   protected boolean m_isSubmit;
/*      */   protected boolean m_isPreview;
/*      */   protected MetaFieldData m_metaSet;
/*      */   protected DataResultSet m_stdFieldSet;
/*      */   protected SchemaFieldConfig m_fields;
/*   55 */   protected static final String[][] FIELD_TYPE_MAP = { { "edit", "" }, { "label", "isInfoOnly" }, { "infoOnly", "isInfoOnly" }, { "hidden", "isHidden" }, { "excluded", "isExcluded" }, { "required", "isRequired" } };
/*      */ 
/*   65 */   public static final String[][] COMPUTE_VALUE_MAP = { { "default", "dprFieldHasDefault", "dprDefaultValue", "0" }, { "derived", "dprFieldIsDerived", "dprDerivedValue", "1" } };
/*      */ 
/*   72 */   public static final String[] DP_RESTRICTED_LIST_COLUMNS = { "fieldName", "listType" };
/*      */ 
/*      */   public DocProfileStates()
/*      */   {
/*   36 */     this.m_binder = null;
/*   37 */     this.m_cxt = null;
/*      */ 
/*   40 */     this.m_fieldDataMap = null;
/*   41 */     this.m_fieldToRuleMap = null;
/*   42 */     this.m_fieldBuddyMap = null;
/*   43 */     this.m_fieldToInfoMap = null;
/*      */ 
/*   46 */     this.m_isRequest = false;
/*   47 */     this.m_isSubmit = false;
/*   48 */     this.m_isPreview = false;
/*      */ 
/*   51 */     this.m_metaSet = null;
/*   52 */     this.m_stdFieldSet = null;
/*   53 */     this.m_fields = null;
/*      */   }
/*      */ 
/*      */   public void init(DataBinder binder, ExecutionContext cxt)
/*      */   {
/*   79 */     this.m_binder = binder;
/*   80 */     this.m_cxt = cxt;
/*      */ 
/*   82 */     this.m_fieldDataMap = new Hashtable();
/*   83 */     this.m_fieldToRuleMap = new Properties();
/*   84 */     this.m_fieldBuddyMap = new Hashtable();
/*   85 */     this.m_fieldToInfoMap = new Hashtable();
/*      */ 
/*   87 */     if (this.m_cxt == null)
/*      */     {
/*   89 */       this.m_cxt = new ExecutionContextAdaptor();
/*      */     }
/*      */ 
/*   92 */     DataBinder promoteData = new DataBinder();
/*   93 */     this.m_cxt.setCachedObject("DpPromoteBinder", promoteData);
/*      */ 
/*   96 */     this.m_cxt.setCachedObject("FieldMap", this.m_fieldDataMap);
/*   97 */     this.m_cxt.setCachedObject("FieldRuleMap", this.m_fieldToRuleMap);
/*   98 */     this.m_cxt.setCachedObject("FieldBuddyMap", this.m_fieldBuddyMap);
/*   99 */     this.m_cxt.setCachedObject("FieldInfoMap", this.m_fieldToInfoMap);
/*      */   }
/*      */ 
/*      */   public void loadDocumentProfile(String metadataSetName, boolean isIncoming, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  105 */     if (SharedObjects.getEnvValueAsBoolean("DisableContentProfiles", false))
/*      */     {
/*  107 */       return;
/*      */     }
/*      */ 
/*  110 */     boolean isDocProfileDone = DataBinderUtils.getBoolean(this.m_binder, "isDocProfileDone", false);
/*  111 */     if (isDocProfileDone)
/*      */     {
/*  113 */       return;
/*      */     }
/*      */ 
/*  116 */     String storageName = null;
/*  117 */     String profileName = null;
/*  118 */     String triggerField = DocProfileManager.getTriggerField(metadataSetName, cxt);
/*  119 */     if ((triggerField != null) && (triggerField.length() > 0))
/*      */     {
/*  122 */       boolean useLocalOnly = StringUtils.convertToBool(this.m_binder.getLocal("useOnlyLocalForDpTrigger"), false);
/*  123 */       String triggerValue = null;
/*  124 */       this.m_binder.putLocal("dpTriggerField", triggerField);
/*  125 */       if (useLocalOnly)
/*      */       {
/*  127 */         triggerValue = this.m_binder.getLocal("dpTriggerValue");
/*      */       }
/*      */       else
/*      */       {
/*  131 */         triggerValue = this.m_binder.getAllowMissing("dpTriggerValue");
/*      */       }
/*      */ 
/*  134 */       if (triggerValue == null)
/*      */       {
/*  136 */         if (useLocalOnly)
/*      */         {
/*  138 */           triggerValue = this.m_binder.getLocal(triggerField);
/*      */         }
/*      */         else
/*      */         {
/*  142 */           triggerValue = this.m_binder.getAllowMissing(triggerField);
/*      */ 
/*  144 */           boolean isDefault = DataBinderUtils.getBoolean(this.m_binder, new StringBuilder().append(triggerField).append(":isSetDefault").toString(), false);
/*  145 */           if (isDefault)
/*      */           {
/*  147 */             triggerValue = null;
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  154 */       if (triggerValue != null)
/*      */       {
/*  157 */         Vector triggerMap = DocProfileManager.getTriggerMapList(metadataSetName, cxt);
/*  158 */         if (triggerMap != null)
/*      */         {
/*  160 */           int size = triggerMap.size();
/*  161 */           for (int i = 0; i < size; ++i)
/*      */           {
/*  163 */             String[] map = (String[])(String[])triggerMap.elementAt(i);
/*  164 */             String val = map[0];
/*  165 */             if (val == null)
/*      */             {
/*  167 */               val = "";
/*      */             }
/*      */ 
/*  170 */             boolean isInProfile = StringUtils.matchEx(triggerValue, val, true, false);
/*  171 */             if (!isInProfile)
/*      */               continue;
/*  173 */             profileName = map[1];
/*  174 */             storageName = map[2];
/*  175 */             this.m_binder.putLocal(triggerField, triggerValue);
/*  176 */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  184 */     determineAction(this.m_binder);
/*  185 */     determineEvent(this.m_binder, isIncoming);
/*      */ 
/*  187 */     boolean isGlobalOnly = StringUtils.convertToBool(this.m_binder.getLocal("isDpGlobalOnly"), false);
/*  188 */     String dpName = profileName;
/*  189 */     if (isGlobalOnly)
/*      */     {
/*  191 */       dpName = null;
/*      */     }
/*  193 */     DataBinder resultBinder = evaluateGlobalRulesAndProfile(metadataSetName, storageName, dpName, false, cxt);
/*      */ 
/*  195 */     this.m_binder.merge(resultBinder);
/*      */ 
/*  197 */     this.m_cxt.setCachedObject("DataBinder", this.m_binder);
/*      */ 
/*  199 */     this.m_binder.putLocal("isDocProfileDone", "1");
/*  200 */     this.m_binder.putLocal("isDocProfileUsed", new StringBuilder().append("").append(!isGlobalOnly).toString());
/*  201 */     if (profileName == null)
/*      */       return;
/*  203 */     this.m_binder.putLocal("dpName", profileName);
/*      */   }
/*      */ 
/*      */   public DataBinder evaluateGlobalRulesAndProfile(String metadataSetName, String storageName, String profileName, boolean isPreview, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  226 */     int result = PluginFilters.filter("evaluateGlobalRulesAndProfile", null, this.m_binder, this.m_cxt);
/*  227 */     if (result == 1)
/*      */     {
/*  229 */       return this.m_binder;
/*      */     }
/*      */ 
/*  232 */     PageMerger pageMerger = (PageMerger)this.m_cxt.getCachedObject("PageMerger");
/*      */ 
/*  234 */     DocProfileData data = null;
/*  235 */     DataBinder profileData = null;
/*  236 */     DataResultSet drset = null;
/*      */ 
/*  238 */     boolean isGlobalOnly = true;
/*  239 */     if (profileName != null)
/*      */     {
/*  241 */       isGlobalOnly = false;
/*  242 */       data = DocProfileManager.getProfile(storageName, profileName, cxt);
/*  243 */       profileData = data.getData();
/*      */ 
/*  246 */       String docClass = profileData.getActiveAllowMissing("dDocClass");
/*  247 */       if (docClass != null)
/*      */       {
/*  249 */         this.m_binder.putLocal("dDocClass", docClass);
/*      */       }
/*      */ 
/*  252 */       drset = (DataResultSet)profileData.getResultSet("ProfileRules");
/*  253 */       if (drset == null)
/*      */       {
/*  255 */         drset = new DataResultSet(DocProfileScriptUtils.DP_DOCRULE_COLUMNS);
/*      */       }
/*      */ 
/*  259 */       this.m_cxt.setCachedObject("ProfileName", profileName);
/*  260 */       this.m_cxt.setCachedObject("ProfileData", data);
/*      */     }
/*      */ 
/*  263 */     String event = this.m_binder.getLocal("dpEvent");
/*  264 */     if (event == null)
/*      */     {
/*  266 */       Report.trace("docprofile", "evaluateGlobalRulesAndProfile: unable to evaluate . Missing parameter dpEvent.", null);
/*      */ 
/*  268 */       return null;
/*      */     }
/*  270 */     this.m_isRequest = event.equals("OnRequest");
/*  271 */     this.m_isSubmit = ((event.equals("OnSubmit")) || (event.equals("OnImport")));
/*  272 */     this.m_isPreview = isPreview;
/*      */ 
/*  275 */     DataBinder dpWorkBinder = new DataBinder();
/*  276 */     this.m_cxt.setCachedObject("DpWorkBinder", dpWorkBinder);
/*      */ 
/*  279 */     Vector activeRules = new IdcVector();
/*      */ 
/*  281 */     Vector globals = DocProfileManager.getGlobalRules(metadataSetName, cxt);
/*  282 */     Hashtable usedGlobals = new Hashtable();
/*  283 */     int size = globals.size();
/*  284 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  286 */       DocProfileData ruleData = (DocProfileData)globals.elementAt(i);
/*      */ 
/*  289 */       boolean hasActivation = StringUtils.convertToBool(ruleData.getValue("dpRuleHasActivationCondition"), false);
/*      */ 
/*  291 */       boolean isActive = !hasActivation;
/*  292 */       if (hasActivation)
/*      */       {
/*  294 */         isActive = computeIsRuleActivated(ruleData, pageMerger);
/*      */       }
/*  296 */       if (!isActive)
/*      */         continue;
/*  298 */       evaluateSideEffects(ruleData, pageMerger);
/*  299 */       activeRules.addElement(ruleData);
/*      */ 
/*  304 */       String ruleName = ruleData.getName();
/*  305 */       usedGlobals.put(ruleName, new StringBuilder().append("").append(i).toString());
/*      */     }
/*      */ 
/*  308 */     if (!isGlobalOnly)
/*      */     {
/*  310 */       int index = ResultSetUtils.getIndexMustExist(drset, "dpRuleName");
/*  311 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  313 */         String ruleName = drset.getStringValue(index);
/*  314 */         DocProfileData ruleData = DocProfileManager.getRule(storageName, ruleName, cxt);
/*      */ 
/*  316 */         if (ruleData == null)
/*      */         {
/*  318 */           Report.trace("docprofile", new StringBuilder().append("evaluateGlobalRulesAndProfile: missing data for rule ").append(ruleName).toString(), null);
/*      */         }
/*      */         else
/*      */         {
/*  322 */           boolean hasActivation = StringUtils.convertToBool(ruleData.getValue("dpRuleHasActivationCondition"), false);
/*      */ 
/*  324 */           boolean isActive = !hasActivation;
/*  325 */           if (hasActivation)
/*      */           {
/*  327 */             isActive = computeIsRuleActivated(ruleData, pageMerger);
/*      */           }
/*      */ 
/*  330 */           if (!isActive)
/*      */             continue;
/*  332 */           boolean usedGlobal = false;
/*  333 */           boolean isGlobal = StringUtils.convertToBool(ruleData.getValue("dpRuleIsGlobal"), false);
/*  334 */           if (isGlobal)
/*      */           {
/*  336 */             Object obj = usedGlobals.get(ruleName);
/*  337 */             if (obj != null)
/*      */             {
/*  340 */               int loc = NumberUtils.parseInteger((String)obj, 0);
/*  341 */               for (int i = loc; i >= 0; --i)
/*      */               {
/*  343 */                 DocProfileData gData = (DocProfileData)activeRules.get(i);
/*  344 */                 String gName = gData.getName();
/*  345 */                 if (!gName.equals(ruleName))
/*      */                   continue;
/*  347 */                 usedGlobal = true;
/*  348 */                 activeRules.remove(i);
/*  349 */                 break;
/*      */               }
/*      */             }
/*      */           }
/*      */ 
/*  354 */           if (!usedGlobal)
/*      */           {
/*  357 */             evaluateSideEffects(ruleData, pageMerger);
/*      */           }
/*  359 */           activeRules.addElement(ruleData);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  365 */     int num = activeRules.size();
/*  366 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  368 */       DocProfileData ruleData = (DocProfileData)activeRules.elementAt(i);
/*  369 */       evaluateRule(ruleData, profileName, pageMerger);
/*      */     }
/*      */ 
/*  372 */     DataBinder resultBinder = finishProfileEvaluation((this.m_isRequest) || (this.m_isPreview));
/*  373 */     if (!isGlobalOnly)
/*      */     {
/*  377 */       boolean isExclude = StringUtils.convertToBool(profileData.getLocal("dpExludeNonRuleFields"), false);
/*      */ 
/*  379 */       if (isExclude)
/*      */       {
/*  381 */         String exclRuleName = LocaleResources.getString("csDpExclusionRule", this.m_cxt);
/*  382 */         prepareMetaAndStandardFieldInfo();
/*  383 */         for (this.m_fields.first(); this.m_fields.isRowPresent(); this.m_fields.next())
/*      */         {
/*  385 */           String field = this.m_fields.getName();
/*  386 */           Object obj = this.m_fieldToRuleMap.getProperty(field);
/*  387 */           if (obj != null)
/*      */             continue;
/*  389 */           resultBinder.putLocal(new StringBuilder().append(field).append(":isExcluded").toString(), "1");
/*  390 */           this.m_fieldToRuleMap.put(field, exclRuleName);
/*      */         }
/*      */ 
/*  395 */         int fieldIndex = ResultSetUtils.getIndexMustExist(this.m_stdFieldSet, "fieldName");
/*  396 */         for (this.m_stdFieldSet.first(); this.m_stdFieldSet.isRowPresent(); this.m_stdFieldSet.next())
/*      */         {
/*  398 */           String field = this.m_stdFieldSet.getStringValue(fieldIndex);
/*  399 */           Object obj = this.m_fieldToRuleMap.getProperty(field);
/*  400 */           if (obj != null)
/*      */             continue;
/*  402 */           resultBinder.putLocal(new StringBuilder().append(field).append(":isExcluded").toString(), "1");
/*  403 */           this.m_fieldToRuleMap.put(field, exclRuleName);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  409 */     if (isPreview)
/*      */     {
/*  411 */       String errMsg = LocaleResources.getString("csDpFieldNotInSystem", this.m_cxt);
/*      */ 
/*  415 */       prepareMetaAndStandardFieldInfo();
/*  416 */       int fieldIndex = ResultSetUtils.getIndexMustExist(this.m_stdFieldSet, "fieldName");
/*      */ 
/*  418 */       Vector fieldRuleList = new IdcVector();
/*  419 */       for (Enumeration en = this.m_fieldToRuleMap.keys(); en.hasMoreElements(); )
/*      */       {
/*  421 */         String field = (String)en.nextElement();
/*  422 */         String rule = this.m_fieldToRuleMap.getProperty(field);
/*      */ 
/*  424 */         Object obj = this.m_fields.findRow(this.m_fields.m_nameIndex, field);
/*  425 */         if (obj == null)
/*      */         {
/*  427 */           obj = this.m_stdFieldSet.findRow(fieldIndex, field);
/*  428 */           if (obj == null)
/*      */           {
/*  430 */             resultBinder.putLocal(new StringBuilder().append(field).append(":error").toString(), errMsg);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  435 */         field = StringUtils.addEscapeChars(field, ':', '*');
/*  436 */         rule = StringUtils.addEscapeChars(rule, ':', '*');
/*  437 */         fieldRuleList.addElement(new StringBuilder().append(field).append(":").append(rule).toString());
/*      */       }
/*      */ 
/*  440 */       String str = StringUtils.createString(fieldRuleList, ',', '^');
/*  441 */       resultBinder.putLocal("FieldRuleList", str);
/*      */     }
/*      */ 
/*  444 */     return resultBinder;
/*      */   }
/*      */ 
/*      */   protected void evaluateRule(DocProfileData ruleData, String profileName, PageMerger pageMerger)
/*      */     throws DataException, ServiceException
/*      */   {
/*  450 */     String ruleName = ruleData.getName();
/*  451 */     DataBinder ruleBinder = ruleData.getData();
/*      */ 
/*  453 */     this.m_cxt.setCachedObject("RuleData", ruleData);
/*  454 */     int result = PluginFilters.filter("evaluateRule", null, null, this.m_cxt);
/*  455 */     if (result == 1)
/*      */     {
/*  459 */       return;
/*      */     }
/*      */ 
/*  463 */     DataResultSet fieldSet = (DataResultSet)ruleBinder.getResultSet("RuleFields");
/*  464 */     FieldInfo[] infos = ResultSetUtils.createInfoList(fieldSet, DocProfileScriptUtils.DP_FIELD_RULE_COLUMNS, true);
/*      */ 
/*  466 */     if (fieldSet == null)
/*      */     {
/*  468 */       fieldSet = new DataResultSet(DocProfileScriptUtils.DP_FIELD_RULE_COLUMNS);
/*      */     }
/*  470 */     int fieldIndex = infos[0].m_index;
/*  471 */     int typeIndex = infos[1].m_index;
/*      */ 
/*  473 */     boolean isError = false;
/*  474 */     Vector fieldList = new IdcVector();
/*  475 */     for (fieldSet.first(); fieldSet.isRowPresent(); fieldSet.next())
/*      */     {
/*  477 */       String fieldName = fieldSet.getStringValue(fieldIndex);
/*  478 */       DataBinder fieldBinder = new DataBinder();
/*  479 */       fieldBinder.putLocal(new StringBuilder().append(fieldName).append(":rule").toString(), ruleName);
/*      */ 
/*  482 */       this.m_fieldDataMap.put(fieldName, fieldBinder);
/*  483 */       this.m_fieldToRuleMap.put(fieldName, ruleName);
/*      */ 
/*  485 */       String key = null;
/*  486 */       String val = null;
/*  487 */       String valType = "";
/*      */       try
/*      */       {
/*  490 */         String type = fieldSet.getStringValue(typeIndex);
/*  491 */         type = getTypeScriptValue(type);
/*  492 */         if ((type != null) && (type.length() > 0) && (type.equals("isRequired")))
/*      */         {
/*  494 */           fieldBinder.putLocal(new StringBuilder().append(fieldName).append(":isRequired").toString(), "1");
/*  495 */           String reqMsg = ruleBinder.getLocal(new StringBuilder().append(fieldName).append(".dprFieldRequiredMsg").toString());
/*  496 */           if ((reqMsg != null) && (reqMsg.length() > 0))
/*      */           {
/*  498 */             fieldBinder.putLocal(new StringBuilder().append(fieldName).append(":requiredMsg").toString(), reqMsg);
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  503 */         if (this.m_isRequest)
/*      */         {
/*  506 */           if ((type != null) && (type.length() > 0) && (!type.equals("isRequired")))
/*      */           {
/*  508 */             fieldBinder.putLocal(new StringBuilder().append(fieldName).append(":").append(type).toString(), "1");
/*      */           }
/*  510 */           else if (this.m_isPreview)
/*      */           {
/*  513 */             fieldBinder.putLocal(fieldName, "1");
/*      */           }
/*      */ 
/*  517 */           valType = "default";
/*  518 */           key = new StringBuilder().append(fieldName).append(":defaultValue").toString();
/*      */ 
/*  520 */           val = computeValue(COMPUTE_VALUE_MAP[0], fieldName, ruleData, pageMerger);
/*      */ 
/*  522 */           if (val != null)
/*      */           {
/*  524 */             if (this.m_isPreview)
/*      */             {
/*  526 */               fieldBinder.putLocal(key, val);
/*      */             }
/*      */             else
/*      */             {
/*  530 */               fieldBinder.putLocal(fieldName, val);
/*      */             }
/*      */           }
/*      */ 
/*  534 */           computeCustomFieldData(fieldName, ruleData, fieldBinder);
/*      */ 
/*  537 */           computeRestrictedList(fieldName, ruleData, fieldBinder);
/*      */ 
/*  539 */           fieldList.addElement(fieldName);
/*      */         }
/*      */ 
/*  542 */         if (this.m_isSubmit)
/*      */         {
/*  545 */           valType = "derived";
/*  546 */           key = new StringBuilder().append(fieldName).append(":derivedValue").toString();
/*  547 */           val = computeValue(COMPUTE_VALUE_MAP[1], fieldName, ruleData, pageMerger);
/*      */ 
/*  549 */           if (val != null)
/*      */           {
/*  551 */             if (this.m_isPreview)
/*      */             {
/*  553 */               fieldBinder.putLocal(key, val);
/*      */             }
/*      */             else
/*      */             {
/*  557 */               fieldBinder.putLocal(fieldName, val);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  564 */         isError = true;
/*      */ 
/*  566 */         if (this.m_isPreview)
/*      */         {
/*  568 */           IdcMessage tmpMsg = IdcMessageFactory.lc(e, new StringBuilder().append("csDpComputeError_").append(valType).toString(), new Object[] { fieldName });
/*  569 */           val = LocaleResources.localizeMessage(null, tmpMsg, this.m_cxt).toString();
/*  570 */           fieldBinder.putLocal(key, val);
/*      */         }
/*      */         else
/*      */         {
/*  574 */           handleError(e, profileName, ruleName, fieldName, valType, this.m_isSubmit);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  579 */     if ((!isError) && (this.m_isRequest))
/*      */     {
/*      */       try
/*      */       {
/*  584 */         computeBuddies(fieldList, ruleData);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  588 */         if (this.m_isPreview)
/*      */         {
/*  590 */           String msg = LocaleUtils.encodeMessage("csDpUnableToEvaluateBuddies", null, profileName, ruleName);
/*      */ 
/*  592 */           String err = this.m_binder.getLocal("ErrorMsg");
/*  593 */           if ((err != null) && (err.length() > 0))
/*      */           {
/*  595 */             err = new StringBuilder().append(err).append("!$\n\n").append(msg).toString();
/*      */           }
/*  597 */           this.m_binder.putLocal(ruleName, err);
/*      */         }
/*      */         else
/*      */         {
/*  601 */           Report.warning(null, e, "csDpUnableToEvaluateBuddies", new Object[] { profileName, ruleName });
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  606 */     PluginFilters.filter("postEvaluateRule", null, null, this.m_cxt);
/*      */   }
/*      */ 
/*      */   protected boolean computeIsRuleActivated(DocProfileData ruleData, PageMerger pageMerger)
/*      */     throws ServiceException
/*      */   {
/*  612 */     DataBinder ruleBinder = ruleData.getData();
/*  613 */     String ruleName = ruleData.getName();
/*  614 */     String val = null;
/*  615 */     String errStub = "csDpUnableToCreateActivationCondition";
/*      */ 
/*  617 */     DataBinder saveBinder = null;
/*      */     try
/*      */     {
/*  620 */       String script = DocProfileScriptUtils.computeScriptString("", ruleBinder, "activation", false);
/*  621 */       if ((script == null) || (script.length() == 0))
/*      */       {
/*  623 */         int i = 1;
/*      */         return i;
/*      */       }
/*  625 */       saveBinder = (DataBinder)this.m_cxt.getCachedObject("DataBinder");
/*  626 */       DataBinder workBinder = new DataBinder();
/*  627 */       workBinder.merge(saveBinder);
/*  628 */       this.m_cxt.setCachedObject("DataBinder", workBinder);
/*  629 */       pageMerger.setDataBinder(workBinder);
/*      */ 
/*  633 */       StringBuffer buff = new StringBuffer("<$if ");
/*  634 */       buff.append(script);
/*  635 */       buff.append("$> <$isActive=1$> <$endif$>");
/*  636 */       script = buff.toString();
/*      */ 
/*  638 */       Report.trace("docprofile", new StringBuilder().append("computeIsRuleActivated: rule=").append(ruleName).append(" script: ").append(script).toString(), null);
/*      */ 
/*  641 */       errStub = "csDpComputeActivation";
/*  642 */       pageMerger.evaluateScriptReportError(script);
/*  643 */       val = workBinder.getLocal("isActive");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  651 */       if (saveBinder != null)
/*      */       {
/*  653 */         this.m_cxt.setCachedObject("DataBinder", saveBinder);
/*  654 */         pageMerger.setDataBinder(saveBinder);
/*      */       }
/*      */     }
/*      */ 
/*  658 */     return StringUtils.convertToBool(val, false);
/*      */   }
/*      */ 
/*      */   protected void evaluateSideEffects(DocProfileData ruleData, PageMerger pageMerger)
/*      */     throws ServiceException
/*      */   {
/*  664 */     String script = ruleData.getValue("dprSideEffects");
/*  665 */     if ((script == null) || (script.length() == 0))
/*      */     {
/*  667 */       return;
/*      */     }
/*      */ 
/*  670 */     String ruleName = ruleData.getName();
/*  671 */     String errStub = "csDpUnableToEvaluateSideEffects";
/*      */     try
/*      */     {
/*  674 */       Report.trace("docprofile", new StringBuilder().append("evaluateSideEffects: rule=").append(ruleName).append(" script: ").append(script).toString(), null);
/*      */ 
/*  677 */       errStub = "csDpSideEffects";
/*  678 */       pageMerger.evaluateScriptReportError(script);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  682 */       throw new ServiceException(e, errStub, new Object[] { ruleName });
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String computeValue(String[] typeInfo, String fieldName, DocProfileData ruleInfo, PageMerger pageMerger)
/*      */     throws ParseSyntaxException, IOException
/*      */   {
/*  690 */     String type = typeInfo[0];
/*  691 */     String paramName = typeInfo[1];
/*  692 */     String paramVar = typeInfo[2];
/*  693 */     boolean isOverwrite = StringUtils.convertToBool(typeInfo[3], false);
/*  694 */     String val = null;
/*      */ 
/*  696 */     DataBinder saveBinder = (DataBinder)this.m_cxt.getCachedObject("DataBinder");
/*  697 */     boolean isSetDefault = true;
/*  698 */     if (!isOverwrite)
/*      */     {
/*  703 */       String oldVal = saveBinder.getAllowMissing(fieldName);
/*  704 */       if ((oldVal != null) && (oldVal.length() > 0))
/*      */       {
/*  706 */         isSetDefault = DataBinderUtils.getBoolean(saveBinder, new StringBuilder().append(fieldName).append(":isSetDefault").toString(), false);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  711 */     DataBinder ruleData = ruleInfo.getData();
/*  712 */     boolean isCompute = StringUtils.convertToBool(ruleData.getLocal(new StringBuilder().append(fieldName).append(".").append(paramName).toString()), false);
/*      */ 
/*  714 */     if (isCompute)
/*      */     {
/*  716 */       String script = DocProfileScriptUtils.computeScriptString(new StringBuilder().append(fieldName).append(".").toString(), ruleData, type, false);
/*      */ 
/*  718 */       Report.trace("docprofile", new StringBuilder().append("computeValue: ").append(type).append(" field: ").append(fieldName).append(" script: ").append(script).toString(), null);
/*      */       try
/*      */       {
/*  722 */         DataBinder workBinder = new DataBinder();
/*  723 */         workBinder.m_blDateFormat = saveBinder.m_blDateFormat;
/*  724 */         workBinder.merge(saveBinder);
/*  725 */         this.m_cxt.setCachedObject("DataBinder", workBinder);
/*  726 */         pageMerger.setDataBinder(workBinder);
/*      */ 
/*  728 */         pageMerger.evaluateScriptReportError(script);
/*  729 */         if (isSetDefault)
/*      */         {
/*  731 */           val = workBinder.getLocal(paramVar);
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/*  736 */         if (saveBinder != null)
/*      */         {
/*  738 */           this.m_cxt.setCachedObject("DataBinder", saveBinder);
/*  739 */           pageMerger.setDataBinder(saveBinder);
/*      */         }
/*      */       }
/*      */     }
/*  743 */     return val;
/*      */   }
/*      */ 
/*      */   protected void computeCustomFieldData(String fieldName, DocProfileData ruleInfo, DataBinder resultBinder)
/*      */     throws DataException
/*      */   {
/*  749 */     DataResultSet drset = SharedObjects.getTable("DpRuleKeyMap");
/*  750 */     if (drset == null)
/*      */     {
/*  752 */       throw new DataException("!csDpMissingActionTable");
/*      */     }
/*      */ 
/*  755 */     int testIndex = ResultSetUtils.getIndexMustExist(drset, "dpTestKey");
/*  756 */     int lookupIndex = ResultSetUtils.getIndexMustExist(drset, "dpLookupKey");
/*  757 */     int mapIndex = ResultSetUtils.getIndexMustExist(drset, "dpMapToKey");
/*  758 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  760 */       String testVal = drset.getStringValue(testIndex);
/*  761 */       boolean isEnabled = StringUtils.convertToBool(ruleInfo.getValue(new StringBuilder().append(fieldName).append(".").append(testVal).toString()), false);
/*      */ 
/*  763 */       if (!isEnabled)
/*      */         continue;
/*  765 */       String key = drset.getStringValue(lookupIndex);
/*  766 */       String map = drset.getStringValue(mapIndex);
/*      */ 
/*  768 */       String val = "1";
/*  769 */       if (key.length() > 0)
/*      */       {
/*  771 */         val = ruleInfo.getValue(new StringBuilder().append(fieldName).append(".").append(key).toString());
/*      */       }
/*  773 */       if ((val == null) || (val.length() <= 0))
/*      */         continue;
/*  775 */       resultBinder.putLocal(new StringBuilder().append(fieldName).append(":").append(map).toString(), val);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void computeRestrictedList(String fieldName, DocProfileData ruleInfo, DataBinder resultBinder)
/*      */   {
/*  784 */     boolean hasRestrictedList = StringUtils.convertToBool(ruleInfo.getValue(new StringBuilder().append(fieldName).append(".dprFieldIsRestricted").toString()), false);
/*      */ 
/*  786 */     if (!hasRestrictedList)
/*      */       return;
/*  788 */     String tableName = new StringBuilder().append(fieldName).append(".RestrictedList").toString();
/*  789 */     ResultSet rset = ruleInfo.getData().getResultSet(tableName);
/*  790 */     if (rset == null)
/*      */     {
/*  792 */       Report.trace("docprofile", new StringBuilder().append("computeRestrictedList: missing table ").append(tableName).toString(), null);
/*  793 */       return;
/*      */     }
/*  795 */     resultBinder.addResultSet(tableName, rset);
/*  796 */     String type = ruleInfo.getValue(new StringBuilder().append(fieldName).append(".dprFieldListType").toString());
/*  797 */     resultBinder.putLocal("dprFieldListType", type);
/*  798 */     resultBinder.putLocal(new StringBuilder().append(fieldName).append(":isRestricted").toString(), "1");
/*      */   }
/*      */ 
/*      */   protected void computeBuddies(Vector fieldList, DocProfileData ruleData)
/*      */     throws ServiceException
/*      */   {
/*  829 */     DataBinder ruleBinder = ruleData.getData();
/*  830 */     boolean isGrouped = StringUtils.convertToBool(ruleBinder.getLocal("dpRuleIsGroup"), false);
/*  831 */     if (!isGrouped)
/*      */       return;
/*  833 */     int size = fieldList.size();
/*  834 */     if (size <= 0)
/*      */       return;
/*  836 */     String parent = (String)fieldList.elementAt(0);
/*  837 */     Properties props = (Properties)ruleBinder.getLocalData().clone();
/*      */ 
/*  841 */     BuddyInfo parentInfo = (BuddyInfo)this.m_fieldToInfoMap.get(parent);
/*  842 */     Vector curBuddies = null;
/*  843 */     boolean hasGrandParent = false;
/*  844 */     if ((parentInfo != null) && (parentInfo.m_parent != null))
/*      */     {
/*  846 */       parent = parentInfo.m_parent;
/*  847 */       curBuddies = (Vector)this.m_fieldBuddyMap.get(parent);
/*      */ 
/*  850 */       hasGrandParent = true;
/*      */     }
/*      */     else
/*      */     {
/*  854 */       curBuddies = (Vector)this.m_fieldBuddyMap.get(parent);
/*  855 */       if (curBuddies == null)
/*      */       {
/*  857 */         curBuddies = new IdcVector();
/*      */       }
/*      */ 
/*  861 */       if (parentInfo == null)
/*      */       {
/*  863 */         parentInfo = new BuddyInfo();
/*  864 */         parentInfo.m_fieldName = parent;
/*  865 */         parentInfo.m_ruleProps = props;
/*  866 */         this.m_fieldToInfoMap.put(parent, parentInfo);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  873 */     Vector newBuddies = new IdcVector();
/*  874 */     for (int i = 1; i < size; ++i)
/*      */     {
/*  876 */       String field = (String)fieldList.elementAt(i);
/*      */ 
/*  879 */       for (Enumeration en = this.m_fieldToInfoMap.elements(); en.hasMoreElements(); )
/*      */       {
/*  881 */         BuddyInfo info = (BuddyInfo)en.nextElement();
/*  882 */         if ((info.m_parent != null) && (field.equals(info.m_parent)))
/*      */         {
/*  884 */           throw new ServiceException(null, "csDpBuddyIsParent", new Object[] { field });
/*      */         }
/*      */       }
/*      */ 
/*  888 */       BuddyInfo info = new BuddyInfo();
/*  889 */       info.m_fieldName = field;
/*  890 */       info.m_parent = parent;
/*  891 */       info.m_ruleProps = props;
/*  892 */       newBuddies.addElement(info);
/*      */     }
/*      */ 
/*  897 */     size = newBuddies.size();
/*  898 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  903 */       BuddyInfo buddyInfo = (BuddyInfo)newBuddies.elementAt(i);
/*  904 */       String field = buddyInfo.m_fieldName;
/*      */ 
/*  908 */       BuddyInfo oldInfo = (BuddyInfo)this.m_fieldToInfoMap.get(field);
/*  909 */       if ((oldInfo == null) || (oldInfo.m_parent == null))
/*      */       {
/*  912 */         int num = curBuddies.size();
/*  913 */         for (int j = 0; j < num; ++j)
/*      */         {
/*  915 */           int index = curBuddies.indexOf(buddyInfo);
/*  916 */           if (index < 0)
/*      */             continue;
/*  918 */           curBuddies.removeElementAt(index);
/*  919 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  926 */         Vector oldBuddies = (Vector)this.m_fieldBuddyMap.get(oldInfo.m_parent);
/*      */ 
/*  929 */         int index = oldBuddies.indexOf(oldInfo);
/*  930 */         if (index >= 0)
/*      */         {
/*  932 */           oldBuddies.removeElementAt(index);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  939 */       if (hasGrandParent)
/*      */       {
/*  943 */         int index = curBuddies.indexOf(parentInfo);
/*  944 */         curBuddies.insertElementAt(buddyInfo, index + i + 1);
/*      */       }
/*      */       else
/*      */       {
/*  948 */         curBuddies.insertElementAt(buddyInfo, i);
/*      */       }
/*  950 */       this.m_fieldToInfoMap.put(field, buddyInfo);
/*  951 */       this.m_fieldBuddyMap.remove(field);
/*      */     }
/*  953 */     this.m_fieldBuddyMap.put(parent, curBuddies);
/*      */   }
/*      */ 
/*      */   protected DataBinder finishProfileEvaluation(boolean isRequest)
/*      */     throws DataException, ServiceException
/*      */   {
/*  962 */     DataBinder resultBinder = new DataBinder();
/*  963 */     DataResultSet restrictedListSet = null;
/*  964 */     if (isRequest)
/*      */     {
/*  966 */       restrictedListSet = new DataResultSet(DP_RESTRICTED_LIST_COLUMNS);
/*  967 */       resultBinder.addResultSet("RestrictedLists", restrictedListSet);
/*      */     }
/*      */ 
/*  971 */     DataBinder promoteBinder = (DataBinder)this.m_cxt.getCachedObject("DpPromoteBinder");
/*  972 */     this.m_binder.merge(promoteBinder);
/*      */ 
/*  974 */     this.m_cxt.setCachedObject("IsRequest", new Boolean(isRequest));
/*  975 */     int result = PluginFilters.filter("finishProfileEvaluation", null, resultBinder, this.m_cxt);
/*  976 */     if (result == 1)
/*      */     {
/*  978 */       return resultBinder;
/*      */     }
/*      */     DataResultSet topSet;
/*      */     String[] buddyClmns;
/*      */     int fieldIndex;
/*      */     int dspIndex;
/*      */     int typeIndex;
/*      */     Enumeration en;
/*      */     Enumeration en;
/*  980 */     if (isRequest)
/*      */     {
/*  982 */       for (Enumeration en = this.m_fieldDataMap.keys(); en.hasMoreElements(); )
/*      */       {
/*  984 */         String fieldName = (String)en.nextElement();
/*  985 */         DataBinder binder = (DataBinder)this.m_fieldDataMap.get(fieldName);
/*      */ 
/*  987 */         boolean isRestricted = StringUtils.convertToBool(binder.getLocal(new StringBuilder().append(fieldName).append(":isRestricted").toString()), false);
/*      */ 
/*  989 */         if (isRestricted)
/*      */         {
/*  991 */           String type = binder.getLocal("dprFieldListType");
/*  992 */           if ((type == null) || (type.length() == 0))
/*      */           {
/*  994 */             type = "strict";
/*      */           }
/*  996 */           binder.removeLocal("dprFieldListType");
/*      */ 
/*  998 */           Vector row = restrictedListSet.createEmptyRow();
/*  999 */           row.setElementAt(fieldName, 0);
/* 1000 */           row.setElementAt(type, 1);
/*      */ 
/* 1002 */           restrictedListSet.addRow(row);
/*      */         }
/* 1004 */         resultBinder.merge(binder);
/*      */       }
/*      */ 
/* 1008 */       String[] clmns = { "parentField" };
/* 1009 */       topSet = new DataResultSet(clmns);
/* 1010 */       resultBinder.addResultSet("AssociatedTopFields", topSet);
/*      */ 
/* 1012 */       buddyClmns = new String[] { "dpFieldName", "type", "displayScript", "dName", "dType", "dIsRequired", "dIsEnabled", "dIsSearchable", "dCaption", "dIsOptionList", "dOptionListKey", "dDefaultValue", "dOptionListType", "schFieldTarget" };
/*      */ 
/* 1017 */       prepareMetaAndStandardFieldInfo();
/* 1018 */       String[] stdClmns = { "fieldName", "displayScript", "type" };
/* 1019 */       FieldInfo[] infos = ResultSetUtils.createInfoList(this.m_stdFieldSet, stdClmns, true);
/* 1020 */       fieldIndex = infos[0].m_index;
/* 1021 */       dspIndex = infos[1].m_index;
/* 1022 */       typeIndex = infos[2].m_index;
/*      */ 
/* 1024 */       for (en = this.m_fieldBuddyMap.keys(); en.hasMoreElements(); )
/*      */       {
/* 1026 */         String parent = (String)en.nextElement();
/*      */ 
/* 1028 */         IdcStringBuilder fieldBuff = new IdcStringBuilder();
/* 1029 */         addFieldToList(parent, resultBinder, fieldBuff);
/*      */ 
/* 1032 */         BuddyInfo parentInfo = (BuddyInfo)this.m_fieldToInfoMap.get(parent);
/* 1033 */         Properties ruleProps = parentInfo.m_ruleProps;
/* 1034 */         populateGroupRuleInfo(parent, ruleProps, resultBinder);
/*      */ 
/* 1037 */         Vector buddies = (Vector)this.m_fieldBuddyMap.get(parent);
/* 1038 */         int num = buddies.size();
/* 1039 */         if (num > 0)
/*      */         {
/* 1041 */           Vector row = topSet.createEmptyRow();
/* 1042 */           row.setElementAt(parent, 0);
/* 1043 */           topSet.addRow(row);
/*      */ 
/* 1045 */           DataResultSet buddySet = new DataResultSet(buddyClmns);
/* 1046 */           resultBinder.addResultSet(new StringBuilder().append("AssociatedFields:").append(parent).toString(), buddySet);
/* 1047 */           for (int i = 0; i < num; ++i)
/*      */           {
/* 1049 */             BuddyInfo info = (BuddyInfo)buddies.elementAt(i);
/* 1050 */             String fieldName = info.m_fieldName;
/* 1051 */             resultBinder.putLocal(new StringBuilder().append(fieldName).append(":isRelocated").toString(), "1");
/*      */ 
/* 1053 */             boolean isFieldVisible = addFieldToList(fieldName, resultBinder, fieldBuff);
/*      */ 
/* 1055 */             if (isFieldVisible)
/*      */             {
/* 1057 */               Vector stdFieldRow = this.m_stdFieldSet.findRow(fieldIndex, fieldName);
/*      */ 
/* 1060 */               row = buddySet.createEmptyRow();
/* 1061 */               row.setElementAt(fieldName, 0);
/*      */ 
/* 1063 */               if (stdFieldRow != null)
/*      */               {
/* 1066 */                 row.setElementAt("system", 1);
/* 1067 */                 row.setElementAt(this.m_stdFieldSet.getStringValue(dspIndex), 2);
/* 1068 */                 row.setElementAt(this.m_stdFieldSet.getStringValue(typeIndex), 4);
/*      */               }
/*      */               else
/*      */               {
/* 1072 */                 row.setElementAt("meta", 1);
/* 1073 */                 row.setElementAt("std_meta_field_display", 2);
/*      */               }
/* 1075 */               row.setElementAt(fieldName, 3);
/*      */ 
/* 1078 */               mergeInFieldInfo(fieldName, row, buddyClmns);
/* 1079 */               buddySet.addRow(row);
/*      */             }
/*      */             else
/*      */             {
/* 1085 */               boolean isHidden = StringUtils.convertToBool(resultBinder.getLocal(new StringBuilder().append(fieldName).append(":isHidden").toString()), false);
/*      */ 
/* 1088 */               if (!isHidden)
/*      */                 continue;
/* 1090 */               resultBinder.putLocal(new StringBuilder().append(fieldName).append(":isRelocated").toString(), "");
/*      */             }
/*      */           }
/*      */         }
/*      */ 
/* 1095 */         resultBinder.putLocal(new StringBuilder().append(parent).append(":groupFieldList").toString(), fieldBuff.toString());
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1100 */       for (en = this.m_fieldDataMap.keys(); en.hasMoreElements(); )
/*      */       {
/* 1102 */         String fieldName = (String)en.nextElement();
/* 1103 */         DataBinder binder = (DataBinder)this.m_fieldDataMap.get(fieldName);
/* 1104 */         resultBinder.merge(binder);
/*      */       }
/*      */     }
/*      */ 
/* 1108 */     return resultBinder;
/*      */   }
/*      */ 
/*      */   protected boolean addFieldToList(String name, DataBinder resultBinder, IdcStringBuilder fieldBuff)
/*      */   {
/* 1114 */     boolean isVisible = false;
/* 1115 */     boolean isHidden = StringUtils.convertToBool(resultBinder.getLocal(new StringBuilder().append(name).append(":isHidden").toString()), false);
/*      */ 
/* 1118 */     boolean isExcluded = false;
/* 1119 */     if (!isHidden)
/*      */     {
/* 1121 */       isExcluded = StringUtils.convertToBool(resultBinder.getLocal(new StringBuilder().append(name).append(":isExcluded").toString()), false);
/*      */     }
/*      */ 
/* 1125 */     if ((!isHidden) && (!isExcluded))
/*      */     {
/* 1129 */       boolean isCountExcluded = StringUtils.convertToBool(resultBinder.getLocal(new StringBuilder().append(name).append(":excludeFromCount").toString()), false);
/*      */ 
/* 1132 */       if (!isCountExcluded)
/*      */       {
/* 1134 */         if (fieldBuff.length() > 0)
/*      */         {
/* 1136 */           fieldBuff.append(',');
/*      */         }
/* 1138 */         fieldBuff.append(name);
/*      */       }
/* 1140 */       isVisible = true;
/*      */     }
/* 1142 */     return isVisible;
/*      */   }
/*      */ 
/*      */   protected void mergeInFieldInfo(String fieldName, Vector row, String[] columns)
/*      */   {
/* 1147 */     SchemaData data = this.m_fields.getData(fieldName);
/* 1148 */     if (data == null)
/*      */     {
/* 1150 */       Vector r = this.m_stdFieldSet.findRow(0, fieldName);
/* 1151 */       if (r == null)
/*      */       {
/* 1155 */         row.setElementAt("non_std_meta_field_display", 2);
/*      */       }
/* 1157 */       return;
/*      */     }
/* 1159 */     for (int i = 0; i < columns.length; ++i)
/*      */     {
/* 1161 */       String value = (String)row.elementAt(i);
/* 1162 */       if ((value != null) && (value.length() != 0)) {
/*      */         continue;
/*      */       }
/* 1165 */       String column = columns[i];
/* 1166 */       value = data.get(column);
/* 1167 */       if (value == null)
/*      */       {
/* 1169 */         String altColumn = null;
/* 1170 */         if (column.equals("dName"))
/*      */         {
/* 1172 */           altColumn = "schFieldName";
/*      */         }
/* 1174 */         else if (column.equals("dType"))
/*      */         {
/* 1176 */           altColumn = "schFieldType";
/*      */         }
/* 1178 */         else if (column.equals("dCaption"))
/*      */         {
/* 1180 */           altColumn = "schFieldCaption";
/*      */         }
/* 1182 */         if (altColumn != null)
/*      */         {
/* 1184 */           value = data.get(altColumn);
/*      */         }
/* 1186 */         if (value == null)
/*      */         {
/* 1188 */           value = "";
/*      */         }
/*      */       }
/* 1191 */       row.setElementAt(value, i);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void populateGroupRuleInfo(String field, Properties ruleProps, DataBinder resultBinder)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1199 */     boolean hasHeader = StringUtils.convertToBool(ruleProps.getProperty("dpRuleHasHeader"), false);
/*      */ 
/* 1201 */     if (!hasHeader)
/*      */       return;
/* 1203 */     this.m_cxt.setCachedObject("GroupField", field);
/* 1204 */     this.m_cxt.setCachedObject("GroupRuleProps", ruleProps);
/* 1205 */     PluginFilters.filter("populateGroupRuleInfo", null, resultBinder, this.m_cxt);
/*      */ 
/* 1207 */     DataResultSet drset = SharedObjects.getTable("DpGroupKeyMap");
/* 1208 */     if (drset == null)
/*      */     {
/* 1210 */       throw new DataException("!csDpMissingActionTable");
/*      */     }
/* 1212 */     int ruleIndex = ResultSetUtils.getIndexMustExist(drset, "dpRuleKey");
/* 1213 */     int idocIndex = ResultSetUtils.getIndexMustExist(drset, "dpIdocKey");
/* 1214 */     int isBooleanIndex = ResultSetUtils.getIndexMustExist(drset, "dpIsBoolean");
/*      */ 
/* 1216 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1218 */       String key = drset.getStringValue(ruleIndex);
/* 1219 */       String isBoolStr = drset.getStringValue(isBooleanIndex);
/* 1220 */       String str = ruleProps.getProperty(key);
/* 1221 */       boolean isBoolean = StringUtils.convertToBool(isBoolStr, false);
/* 1222 */       if (isBoolean)
/*      */       {
/* 1224 */         boolean isTrue = StringUtils.convertToBool(str, false);
/* 1225 */         if (isTrue)
/*      */         {
/* 1227 */           str = "1";
/*      */         }
/*      */         else
/*      */         {
/* 1231 */           str = "";
/*      */         }
/*      */       }
/* 1234 */       if ((str == null) || (str.length() <= 0))
/*      */         continue;
/* 1236 */       String idocKey = drset.getStringValue(idocIndex);
/* 1237 */       resultBinder.putLocal(new StringBuilder().append(field).append(":").append(idocKey).toString(), str);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void prepareMetaAndStandardFieldInfo()
/*      */   {
/* 1245 */     if (this.m_metaSet != null)
/*      */       return;
/* 1247 */     this.m_metaSet = ((MetaFieldData)SharedObjects.getTable("DocMetaDefinition"));
/* 1248 */     this.m_stdFieldSet = SharedObjects.getTable("StandardFieldIncludes");
/*      */ 
/* 1250 */     this.m_fields = ((SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig"));
/*      */   }
/*      */ 
/*      */   protected void handleError(Exception e, String profileName, String ruleName, String fieldName, String valType, boolean isSubmit)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1258 */     if (isSubmit)
/*      */     {
/* 1261 */       boolean isReport = DataBinderUtils.getBoolean(this.m_binder, "isReportToErrorPage", false);
/* 1262 */       if (isReport)
/*      */       {
/* 1264 */         throw new ServiceException(e);
/*      */       }
/*      */ 
/* 1268 */       boolean isFatal = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("IsDpSubmitErrorFatal"), false);
/*      */ 
/* 1270 */       if (isFatal)
/*      */       {
/* 1272 */         IdcMessage errMsg = null;
/* 1273 */         if (profileName != null)
/*      */         {
/* 1275 */           errMsg = IdcMessageFactory.lc("csDpUnableToEvaluateProfile", new Object[] { profileName });
/*      */         }
/* 1277 */         String stub = "csDpUnableToEvaluateFieldRule";
/* 1278 */         if ((valType != null) && (valType.length() > 0))
/*      */         {
/* 1280 */           stub = new StringBuilder().append(stub).append("_").append(valType).toString();
/*      */         }
/* 1282 */         errMsg = IdcMessageFactory.lc(errMsg, stub, new Object[] { ruleName, fieldName });
/* 1283 */         throw new ServiceException(e, errMsg);
/*      */       }
/*      */     }
/*      */ 
/* 1287 */     String errMsg = "Error occurred during profiling at";
/* 1288 */     if (profileName != null)
/*      */     {
/* 1290 */       errMsg = new StringBuilder().append(errMsg).append(" profile=").append(profileName).toString();
/*      */     }
/* 1292 */     errMsg = new StringBuilder().append(errMsg).append(" rule=").append(ruleName).toString();
/* 1293 */     errMsg = new StringBuilder().append(errMsg).append(" field=").append(fieldName).toString();
/* 1294 */     if (valType != null)
/*      */     {
/* 1296 */       errMsg = new StringBuilder().append(errMsg).append(" valueType=").append(valType).toString();
/*      */     }
/* 1298 */     Report.trace("docprofile", errMsg, e);
/*      */   }
/*      */ 
/*      */   protected void determineEvent(DataBinder binder, boolean isIncoming)
/*      */   {
/* 1306 */     String event = binder.getLocal("dpEvent");
/* 1307 */     if ((event == null) || (event.length() == 0))
/*      */     {
/* 1310 */       if (isIncoming)
/*      */       {
/* 1312 */         event = "OnSubmit";
/*      */       }
/*      */       else
/*      */       {
/* 1316 */         event = "OnRequest";
/*      */       }
/*      */     }
/* 1319 */     Report.trace("docprofile", new StringBuilder().append("determineEvent:").append(event).toString(), null);
/* 1320 */     binder.putLocal("dpEvent", event);
/*      */   }
/*      */ 
/*      */   protected void determineAction(DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 1326 */     String action = binder.getLocal("dpAction");
/* 1327 */     if ((action != null) && (action.length() > 0))
/*      */     {
/* 1333 */       return;
/*      */     }
/* 1335 */     DataResultSet drset = SharedObjects.getTable("DpActionTypes");
/* 1336 */     if (drset == null)
/*      */     {
/* 1338 */       throw new DataException("!csDpMissingActionTable");
/*      */     }
/*      */ 
/* 1341 */     int actionIndex = ResultSetUtils.getIndexMustExist(drset, "dpActionType");
/* 1342 */     int trueIndex = ResultSetUtils.getIndexMustExist(drset, "dpTrueParams");
/* 1343 */     int falseIndex = ResultSetUtils.getIndexMustExist(drset, "dpFalseParams");
/*      */ 
/* 1346 */     String actionType = "NoAction";
/* 1347 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1349 */       Vector paramTrueList = StringUtils.parseArray(drset.getStringValue(trueIndex), ',', '^');
/* 1350 */       Vector paramFalseList = StringUtils.parseArray(drset.getStringValue(falseIndex), ',', '^');
/*      */ 
/* 1352 */       boolean isCorrect = checkParams(paramTrueList, binder, true);
/* 1353 */       if (!isCorrect)
/*      */         continue;
/* 1355 */       isCorrect = checkParams(paramFalseList, binder, false);
/* 1356 */       if (!isCorrect)
/*      */         continue;
/* 1358 */       actionType = drset.getStringValue(actionIndex);
/* 1359 */       break;
/*      */     }
/*      */ 
/* 1363 */     Report.trace("docprofile", new StringBuilder().append("determineAction:").append(actionType).toString(), null);
/* 1364 */     binder.putLocal("dpAction", actionType);
/*      */   }
/*      */ 
/*      */   public String determinePageForTest(DataBinder binder)
/*      */     throws DataException
/*      */   {
/* 1370 */     String templatePage = null;
/* 1371 */     String action = binder.getLocal("dpAction");
/* 1372 */     if (action == null)
/*      */     {
/* 1374 */       return null;
/*      */     }
/*      */ 
/* 1377 */     DataResultSet drset = SharedObjects.getTable("DpActionTypes");
/* 1378 */     if (drset == null)
/*      */     {
/* 1380 */       throw new DataException("!csDpMissingActionTable");
/*      */     }
/*      */ 
/* 1383 */     String[] keys = { "dpActionType", "dpTrueParams", "dpFalseParams", "dpTemplate" };
/* 1384 */     String[][] table = ResultSetUtils.createStringTable(drset, keys);
/*      */ 
/* 1386 */     for (int i = 0; i < table.length; ++i)
/*      */     {
/* 1388 */       String actionType = table[i][0];
/* 1389 */       if (!action.equals(actionType))
/*      */         continue;
/* 1391 */       Vector paramTrueList = StringUtils.parseArray(table[i][1], ',', '^');
/* 1392 */       Vector paramFalseList = StringUtils.parseArray(table[i][2], ',', '^');
/*      */ 
/* 1394 */       setParams(binder, paramTrueList, true);
/* 1395 */       setParams(binder, paramFalseList, false);
/*      */ 
/* 1397 */       templatePage = table[i][3];
/* 1398 */       break;
/*      */     }
/*      */ 
/* 1402 */     return templatePage;
/*      */   }
/*      */ 
/*      */   protected boolean checkParams(Vector keyList, DataBinder binder, boolean expectedResult)
/*      */   {
/* 1407 */     boolean isCorrect = true;
/* 1408 */     int size = keyList.size();
/* 1409 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1411 */       String key = (String)keyList.elementAt(i);
/* 1412 */       boolean result = StringUtils.convertToBool(binder.getLocal(key), false);
/* 1413 */       if (result == expectedResult)
/*      */         continue;
/* 1415 */       isCorrect = false;
/* 1416 */       break;
/*      */     }
/*      */ 
/* 1419 */     return isCorrect;
/*      */   }
/*      */ 
/*      */   protected void setParams(DataBinder binder, Vector params, boolean expectedResult)
/*      */   {
/* 1424 */     int size = params.size();
/* 1425 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1427 */       String key = (String)params.elementAt(i);
/* 1428 */       binder.putLocal(key, new StringBuilder().append("").append(expectedResult).toString());
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getTypeScriptValue(String type)
/*      */   {
/* 1434 */     int len = FIELD_TYPE_MAP.length;
/* 1435 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1437 */       if (type.equals(FIELD_TYPE_MAP[i][0]))
/*      */       {
/* 1439 */         return FIELD_TYPE_MAP[i][1];
/*      */       }
/*      */     }
/* 1442 */     return null;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1447 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98902 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocProfileStates
 * JD-Core Version:    0.5.4
 */