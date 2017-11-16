/*     */ package intradoc.server.vcr;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.DocProfileManager;
/*     */ import intradoc.server.IdcServiceAction;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHandler;
/*     */ import intradoc.server.schema.SchemaUtils;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.shared.schema.SchemaSecurityFilter;
/*     */ import intradoc.shared.schema.SchemaViewConfig;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.PrintStream;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.net.URLEncoder;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class VcrServiceHandler extends ServiceHandler
/*     */ {
/*  67 */   public static String GLOBAL_PROFILE_CONTENT_TYPE = "IDC:GlobalProfile";
/*  68 */   public static String PROFILE_CONTENT_TYPE_PREFIX = "IDC:Profile:";
/*  69 */   public static String FILE_REFERENCE_CONTENT_TYPE = "IDC:FileReference";
/*     */ 
/*  71 */   public static String[] CONTENT_TYPES_COLUMNS = { "name", "description", "parent", "isAbstract", "creationDate", "modifiedDate", "isSearchable" };
/*     */ 
/*  73 */   public static int[] CONTENT_TYPES_COL_TYPES = { 6, 6, 6, 6, 5, 5, 6 };
/*     */ 
/*  76 */   public static String[] PROPERTY_CHOICES_COLUMNS = { "value" };
/*     */ 
/*  78 */   public static String[] NODE_COLUMNS = { "name", "objectClass", "parentID", "path", "createdBy", "createdDate", "modifiedBy", "modifiedDate" };
/*     */ 
/*  80 */   public static int[] NODE_COL_TYPES = { 6, 6, 6, 6, 6, 5, 6, 5 };
/*     */ 
/*  83 */   public static String[] FILE_REFERENCE_COLUMNS = { "type", "name", "contentType", "size", "url" };
/*  84 */   public static int[] FILE_REFERENCE_COL_TYPES = { 6, 6, 6, 3, 6 };
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void vcrGetAllContentTypes()
/*     */     throws ServiceException, DataException
/*     */   {
/*  90 */     DataResultSet vcrContentTypes = createResultSetFromColumnsAndTypes(CONTENT_TYPES_COLUMNS, CONTENT_TYPES_COL_TYPES);
/*     */ 
/*  93 */     loadProfilesInformation();
/*     */ 
/*  96 */     addContentTypeInfoFileReference(vcrContentTypes);
/*  97 */     addContentTypeInfoGlobalProfile(vcrContentTypes);
/*     */ 
/* 100 */     DataResultSet dpSet = SharedObjects.getTable("DocumentProfiles");
/* 101 */     if ((dpSet != null) && (!dpSet.isEmpty()))
/*     */     {
/* 103 */       int index = ResultSetUtils.getIndexMustExist(dpSet, "dpName");
/* 104 */       for (dpSet.first(); dpSet.isRowPresent(); dpSet.next())
/*     */       {
/* 106 */         String profileName = dpSet.getStringValue(index);
/* 107 */         DocProfileData profileData = DocProfileManager.getProfile(profileName);
/* 108 */         addContentTypeInfoForProfile(profileData, vcrContentTypes);
/*     */       }
/*     */     }
/*     */ 
/* 112 */     this.m_binder.addResultSet("VcrContentTypes", vcrContentTypes);
/* 113 */     this.m_binder.removeResultSet("DesignDocumentProfiles");
/*     */ 
/* 115 */     PluginFilters.filter("vcrGetAllContentTypes", this.m_workspace, this.m_binder, this.m_service);
/*     */   }
/*     */ 
/*     */   public void addContentTypeInfoGlobalProfile(DataResultSet drset)
/*     */     throws ServiceException, DataException
/*     */   {
/* 121 */     Map map = new HashMap();
/* 122 */     Parameters params = new MapParameters(map);
/* 123 */     map.put("name", GLOBAL_PROFILE_CONTENT_TYPE);
/* 124 */     map.put("description", LocaleResources.getString("csVcrGlobalProfileContentTypeDescription", this.m_service));
/*     */ 
/* 126 */     map.put("isAbstract", "0");
/* 127 */     map.put("isSearchable", "1");
/* 128 */     drset.addRow(drset.createRow(params));
/*     */   }
/*     */ 
/*     */   public void addContentTypeInfoFileReference(DataResultSet drset)
/*     */     throws ServiceException, DataException
/*     */   {
/* 134 */     Map map = new HashMap();
/* 135 */     Parameters params = new MapParameters(map);
/* 136 */     map.put("name", FILE_REFERENCE_CONTENT_TYPE);
/* 137 */     map.put("description", LocaleResources.getString("csVcrFileReferenceContentTypeDescription", this.m_service));
/*     */ 
/* 139 */     map.put("isAbstract", "1");
/* 140 */     map.put("isSearchable", "0");
/* 141 */     drset.addRow(drset.createRow(params));
/*     */   }
/*     */ 
/*     */   public void addContentTypeInfoForProfile(DocProfileData profileData, DataResultSet drset)
/*     */     throws ServiceException, DataException
/*     */   {
/* 147 */     String isSearchableStr = null;
/* 148 */     DataResultSet profiles = (DataResultSet)this.m_binder.getResultSet("DesignDocumentProfiles");
/* 149 */     if (profiles != null)
/*     */     {
/* 151 */       isSearchableStr = ResultSetUtils.findValue(profiles, "dpName", profileData.getName(), "dpIsSearch");
/*     */     }
/*     */ 
/* 155 */     boolean isSearchable = StringUtils.convertToBool(isSearchableStr, false);
/* 156 */     isSearchableStr = (isSearchable) ? "1" : "0";
/*     */ 
/* 158 */     Map map = new HashMap();
/* 159 */     Parameters params = new MapParameters(map);
/* 160 */     DataBinder profileBinder = profileData.getData();
/* 161 */     String description = profileBinder.get("dpDescription");
/* 162 */     if (description.length() > 0)
/*     */     {
/* 164 */       description = LocaleResources.getString(description, this.m_service);
/*     */     }
/*     */ 
/* 167 */     map.put("name", PROFILE_CONTENT_TYPE_PREFIX + profileData.getName());
/* 168 */     map.put("description", description);
/* 169 */     map.put("isAbstract", "0");
/* 170 */     map.put("isSearchable", isSearchableStr);
/* 171 */     drset.addRow(drset.createRow(params));
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void vcrGetContentTypeInfo() throws ServiceException, DataException
/*     */   {
/* 177 */     int ret = PluginFilters.filter("vcrGetContentTypeInfo", this.m_workspace, this.m_binder, this.m_service);
/* 178 */     if (ret != 0)
/*     */     {
/* 180 */       return;
/*     */     }
/*     */ 
/* 184 */     String name = this.m_binder.get("vcrContentType");
/* 185 */     String triggerValue = this.m_binder.getLocal("triggerValue");
/* 186 */     boolean appendDocumentProperties = this.m_service.isConditionVarTrue("vcrAppendDocumentProperties");
/* 187 */     DataResultSet vcrContentType = (DataResultSet)this.m_binder.getResultSet("VcrContentType");
/* 188 */     DataResultSet vcrProperties = (DataResultSet)this.m_binder.getResultSet("VcrProperties");
/* 189 */     if (vcrContentType == null)
/*     */     {
/* 191 */       vcrContentType = createResultSetFromColumnsAndTypes(CONTENT_TYPES_COLUMNS, CONTENT_TYPES_COL_TYPES);
/*     */     }
/*     */ 
/* 194 */     if (vcrProperties == null)
/*     */     {
/* 196 */       vcrProperties = new DataResultSet();
/*     */     }
/*     */ 
/* 200 */     if (vcrContentType.getNumRows() == 0)
/*     */     {
/* 202 */       if (name.equals(GLOBAL_PROFILE_CONTENT_TYPE))
/*     */       {
/* 204 */         triggerValue = "";
/* 205 */         appendDocumentProperties = true;
/* 206 */         loadProfilesInformation();
/* 207 */         addContentTypeInfoGlobalProfile(vcrContentType);
/*     */       }
/* 209 */       else if (name.startsWith(PROFILE_CONTENT_TYPE_PREFIX))
/*     */       {
/* 211 */         loadProfilesInformation();
/* 212 */         String profileName = name.substring(PROFILE_CONTENT_TYPE_PREFIX.length());
/* 213 */         if (profileName.length() > 0)
/*     */         {
/* 216 */           DocProfileData profileData = DocProfileManager.getProfile(profileName);
/* 217 */           if (profileData != null)
/*     */           {
/* 219 */             appendDocumentProperties = true;
/* 220 */             triggerValue = profileData.getValue("dpTriggerValue");
/* 221 */             addContentTypeInfoForProfile(profileData, vcrContentType);
/*     */           }
/*     */         }
/*     */       }
/* 225 */       else if (name.equals(FILE_REFERENCE_CONTENT_TYPE))
/*     */       {
/* 227 */         addContentTypeInfoFileReference(vcrContentType);
/*     */ 
/* 230 */         DataResultSet vcrFieldDefs = SharedObjects.getTable("VcrFileReferenceFieldDefinitions");
/* 231 */         vcrProperties.copy(vcrFieldDefs);
/*     */ 
/* 234 */         FieldInfo[] vcrPropertiesFields = ResultSetUtils.createInfoList(vcrProperties, new String[] { "name", "description" }, true);
/*     */ 
/* 236 */         for (vcrProperties.first(); vcrProperties.isRowPresent(); vcrProperties.next())
/*     */         {
/* 238 */           String description = vcrProperties.getStringValue(vcrPropertiesFields[1].m_index);
/* 239 */           if (description.length() <= 0)
/*     */             continue;
/* 241 */           vcrProperties.setCurrentValue(vcrPropertiesFields[1].m_index, LocaleResources.getString(description, this.m_service));
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 249 */     if (vcrContentType.getNumRows() == 0)
/*     */     {
/* 251 */       String msg = LocaleUtils.encodeMessage("csVcrInvalidContentType", null, name);
/* 252 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 255 */     if (appendDocumentProperties)
/*     */     {
/* 258 */       DataResultSet vcrFieldDefs = SharedObjects.getTable("VcrDocumentFieldDefinitions");
/* 259 */       vcrProperties.copy(vcrFieldDefs);
/* 260 */       FieldInfo[] vcrPropertiesFields = ResultSetUtils.createInfoList(vcrProperties, new String[] { "name", "isSearchable", "description" }, true);
/*     */ 
/* 264 */       DataResultSet docMetaDef = SharedObjects.getTable("DocMetaDefinition");
/* 265 */       FieldInfo[] docMetaDefFields = ResultSetUtils.createInfoList(docMetaDef, new String[] { "dName", "dType", "dIsRequired", "dIsEnabled", "dIsSearchable", "dCaption", "dIsOptionList", "dOptionListKey", "dOptionListType", "dIsPlaceholderField" }, true);
/*     */ 
/* 270 */       Collection allFields = new HashSet();
/* 271 */       for (vcrProperties.first(); vcrProperties.isRowPresent(); vcrProperties.next())
/*     */       {
/* 273 */         allFields.add(vcrProperties.getStringValue(vcrPropertiesFields[0].m_index));
/*     */ 
/* 276 */         String description = vcrProperties.getStringValue(vcrPropertiesFields[2].m_index);
/* 277 */         if (description.length() <= 0)
/*     */           continue;
/* 279 */         vcrProperties.setCurrentValue(vcrPropertiesFields[2].m_index, LocaleResources.getString(description, this.m_service));
/*     */       }
/*     */ 
/* 283 */       for (docMetaDef.first(); docMetaDef.isRowPresent(); docMetaDef.next())
/*     */       {
/* 285 */         allFields.add(docMetaDef.getStringValue(docMetaDefFields[0].m_index));
/*     */       }
/*     */ 
/* 288 */       SchemaUtils schemaUtils = (SchemaUtils)ComponentClassFactory.createClassInstance("SchemaUtils", "intradoc.server.schema.SchemaUtils", null);
/*     */ 
/* 291 */       Collection removedForSearch = new HashSet();
/* 292 */       Collection removedForInfo = new HashSet();
/* 293 */       if (triggerValue != null)
/*     */       {
/* 296 */         determineRemovedFieldsForProfile(triggerValue, "Search", removedForSearch, allFields);
/* 297 */         determineRemovedFieldsForProfile(triggerValue, "Info", removedForInfo, allFields);
/*     */ 
/* 300 */         vcrProperties.first();
/* 301 */         while (vcrProperties.isRowPresent())
/*     */         {
/* 303 */           boolean deleteCurrentRow = false;
/*     */ 
/* 305 */           String fieldName = vcrProperties.getStringValue(vcrPropertiesFields[0].m_index);
/* 306 */           if (removedForSearch.contains(fieldName))
/*     */           {
/* 308 */             if (removedForInfo.contains(fieldName))
/*     */             {
/* 310 */               deleteCurrentRow = true;
/*     */             }
/*     */             else
/*     */             {
/* 314 */               vcrProperties.setCurrentValue(vcrPropertiesFields[1].m_index, "0");
/*     */             }
/*     */           }
/*     */ 
/* 318 */           if (deleteCurrentRow)
/*     */           {
/* 320 */             vcrProperties.deleteCurrentRow();
/*     */           }
/*     */           else
/*     */           {
/* 324 */             vcrProperties.next();
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 330 */       boolean useAccounts = SharedObjects.getEnvValueAsBoolean("UseAccounts", false);
/* 331 */       if ((!useAccounts) && 
/* 333 */         (vcrProperties.findRow(vcrPropertiesFields[0].m_index, "dDocAccount") != null))
/*     */       {
/* 335 */         vcrProperties.deleteCurrentRow();
/*     */       }
/*     */ 
/* 340 */       FieldInfo isMandatoryfieldInfo = new FieldInfo();
/* 341 */       vcrProperties.getFieldInfo("isMandatory", isMandatoryfieldInfo);
/* 342 */       for (vcrProperties.first(); vcrProperties.isRowPresent(); vcrProperties.next())
/*     */       {
/* 344 */         String fieldName = vcrProperties.getStringValue(vcrPropertiesFields[0].m_index);
/* 345 */         String viewName = null;
/*     */ 
/* 347 */         if (fieldName.equals("dDocType"))
/*     */         {
/* 349 */           viewName = "docTypes";
/*     */         }
/* 351 */         else if (fieldName.equals("dSecurityGroup"))
/*     */         {
/* 353 */           viewName = "SecurityGroups";
/*     */         }
/* 355 */         else if (fieldName.equals("dDocAccount"))
/*     */         {
/* 357 */           viewName = "DocumentAccounts";
/*     */         }
/* 359 */         else if (fieldName.equals("dDocName"))
/*     */         {
/* 361 */           vcrProperties.setCurrentValue(isMandatoryfieldInfo.m_index, (SharedObjects.getEnvValueAsBoolean("IsAutoNumber", false)) ? "0" : "1");
/*     */         }
/*     */ 
/* 364 */         if (viewName == null)
/*     */           continue;
/* 366 */         addPropertyChoicesForFieldAndView(fieldName, viewName, null, schemaUtils);
/*     */       }
/*     */ 
/* 371 */       for (docMetaDef.first(); docMetaDef.isRowPresent(); docMetaDef.next())
/*     */       {
/* 373 */         String fieldName = docMetaDef.getStringValue(docMetaDefFields[0].m_index);
/* 374 */         if ((removedForSearch.contains(fieldName)) && (removedForInfo.contains(fieldName)))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 379 */         String isEnabledStr = docMetaDef.getStringValue(docMetaDefFields[3].m_index);
/* 380 */         if (!StringUtils.convertToBool(isEnabledStr, true))
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 385 */         String fieldType = docMetaDef.getStringValue(docMetaDefFields[1].m_index);
/* 386 */         String isRequiredStr = docMetaDef.getStringValue(docMetaDefFields[2].m_index);
/* 387 */         String isSearchableStr = docMetaDef.getStringValue(docMetaDefFields[4].m_index);
/* 388 */         String caption = docMetaDef.getStringValue(docMetaDefFields[5].m_index);
/* 389 */         String isOptionListStr = docMetaDef.getStringValue(docMetaDefFields[6].m_index);
/* 390 */         String optionListKey = docMetaDef.getStringValue(docMetaDefFields[7].m_index);
/* 391 */         String optionListType = docMetaDef.getStringValue(docMetaDefFields[8].m_index);
/* 392 */         boolean isOptionList = StringUtils.convertToBool(isOptionListStr, false);
/* 393 */         boolean isSearchable = StringUtils.convertToBool(isSearchableStr, false);
/* 394 */         String isRestrictedStr = "0";
/* 395 */         String isMultiValuedStr = "0";
/*     */ 
/* 398 */         String propertyType = "STRING";
/* 399 */         if ((fieldType.equals("Int")) || (fieldType.equals("Integer")))
/*     */         {
/* 401 */           propertyType = "LONG";
/*     */         }
/* 403 */         else if (fieldType.equals("Date"))
/*     */         {
/* 405 */           propertyType = "CALENDAR";
/*     */         }
/* 407 */         else if (fieldType.equals("Decimal"))
/*     */         {
/* 409 */           propertyType = "DOUBLE";
/*     */         }
/*     */ 
/* 413 */         if ((isOptionList) && ((
/* 417 */           (optionListKey.equals(SchemaHelper.VIEW_PREFIX + "YesNoView")) || ((optionListKey.startsWith(SchemaHelper.VIEW_PREFIX)) && (optionListKey.endsWith("TrueFalseView"))))))
/*     */         {
/* 421 */           propertyType = "BOOLEAN";
/*     */         }
/*     */ 
/* 426 */         if ((isSearchable) && (removedForSearch.contains(fieldName)))
/*     */         {
/* 428 */           isSearchableStr = "0";
/*     */         }
/*     */ 
/* 432 */         if (isOptionList)
/*     */         {
/* 434 */           if ((optionListType.equals("choice")) || (optionListType.equals("chunval")) || (optionListType.equals("multi2")))
/*     */           {
/* 437 */             isRestrictedStr = "1";
/*     */           }
/*     */ 
/* 440 */           if ((optionListType.equals("multi")) || (optionListType.equals("multi2")))
/*     */           {
/* 442 */             isMultiValuedStr = "1";
/*     */           }
/*     */         }
/*     */ 
/* 446 */         Map map = new HashMap();
/* 447 */         map.put("name", fieldName);
/* 448 */         map.put("description", LocaleResources.getString(caption, this.m_service));
/* 449 */         map.put("propertyType", propertyType);
/* 450 */         map.put("isMandatory", isRequiredStr);
/* 451 */         map.put("isReadOnly", "0");
/* 452 */         map.put("isSearchable", isSearchableStr);
/* 453 */         map.put("propertyDefinitionType", "NATIVE");
/* 454 */         map.put("isPrimary", "0");
/* 455 */         map.put("isRestricted", isRestrictedStr);
/* 456 */         map.put("isMultiValued", isMultiValuedStr);
/* 457 */         Parameters params = new MapParameters(map);
/* 458 */         vcrProperties.addRow(vcrProperties.createRow(params));
/*     */ 
/* 460 */         if (!isOptionList)
/*     */           continue;
/* 462 */         DataResultSet vcrProperty = null;
/*     */ 
/* 465 */         if (optionListKey.startsWith(SchemaHelper.VIEW_PREFIX))
/*     */         {
/* 467 */           String viewName = optionListKey.substring(SchemaHelper.VIEW_PREFIX.length());
/* 468 */           addPropertyChoicesForFieldAndView(fieldName, viewName, fieldType, schemaUtils);
/*     */         }
/* 470 */         else if (!optionListKey.startsWith(SchemaHelper.TREE_PREFIX))
/*     */         {
/* 472 */           String optionListName = optionListKey;
/* 473 */           Map args = new HashMap();
/* 474 */           args.put("dKey", optionListName);
/* 475 */           ResultSet rset = this.m_workspace.createResultSet("QoptionList", new MapParameters(args));
/* 476 */           int optionIndex = ResultSetUtils.getIndexMustExist(rset, "dOption");
/*     */           int[] type;
/*     */           int[] type;
/* 479 */           if (propertyType.equals("CALENDAR"))
/*     */           {
/* 481 */             type = new int[] { 5 };
/*     */           }
/*     */           else
/*     */           {
/* 485 */             type = new int[] { 6 };
/*     */           }
/* 487 */           vcrProperty = createResultSetFromColumnsAndTypes(PROPERTY_CHOICES_COLUMNS, type);
/*     */ 
/* 489 */           for (rset.first(); rset.isRowPresent(); rset.next())
/*     */           {
/* 491 */             Vector v = new IdcVector();
/* 492 */             v.add(rset.getStringValue(optionIndex));
/* 493 */             vcrProperty.addRow(v);
/*     */           }
/*     */ 
/* 496 */           this.m_binder.addResultSet("VcrPropertyChoices:" + fieldName, vcrProperty);
/*     */         }
/*     */ 
/* 499 */         if (vcrProperty == null)
/*     */           continue;
/* 501 */         this.m_binder.addResultSet("VcrPropertyChoices:" + fieldName, vcrProperty);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 507 */     this.m_binder.addResultSet("VcrContentType", vcrContentType);
/* 508 */     this.m_binder.addResultSet("VcrProperties", vcrProperties);
/*     */ 
/* 510 */     PluginFilters.filter("vcrPostGetContentTypeInfo", this.m_workspace, this.m_binder, this.m_service);
/*     */   }
/*     */ 
/*     */   public void addPropertyChoicesForFieldAndView(String fieldName, String viewName, String fieldType, SchemaUtils schemaUtils)
/*     */     throws ServiceException, DataException
/*     */   {
/* 516 */     SchemaViewConfig views = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/* 517 */     SchemaViewData svd = (SchemaViewData)views.getData(viewName);
/* 518 */     String internalColumn = svd.get("schInternalColumn");
/*     */ 
/* 520 */     SchemaSecurityFilter securityFilter = schemaUtils.getSecurityImplementor(svd, this.m_service);
/* 521 */     ResultSet rset = svd.getAllViewValuesWithFilter(securityFilter);
/* 522 */     int internalIndex = ResultSetUtils.getIndexMustExist(rset, internalColumn);
/*     */     int[] type;
/*     */     int[] type;
/* 525 */     if ((fieldType != null) && (fieldType.equals("Date")))
/*     */     {
/* 527 */       type = new int[] { 5 };
/*     */     }
/*     */     else
/*     */     {
/* 531 */       type = new int[] { 6 };
/*     */     }
/*     */ 
/* 534 */     DataResultSet vcrProperty = createResultSetFromColumnsAndTypes(PROPERTY_CHOICES_COLUMNS, type);
/*     */ 
/* 536 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 538 */       Vector v = new IdcVector();
/* 539 */       v.add(rset.getStringValue(internalIndex));
/* 540 */       vcrProperty.addRow(v);
/*     */     }
/*     */ 
/* 543 */     this.m_binder.addResultSet("VcrPropertyChoices:" + fieldName, vcrProperty);
/*     */   }
/*     */ 
/*     */   public void determineRemovedFieldsForProfile(String triggerValue, String action, Collection removedFields, Collection<String> allFields)
/*     */   {
/* 550 */     Properties oldProps = this.m_binder.getLocalData();
/* 551 */     Collection oldRsetNames = getCurrentResultSetNames();
/*     */     try
/*     */     {
/* 556 */       this.m_binder.putLocal("dpAction", action);
/* 557 */       this.m_binder.putLocal("dpTriggerValue", triggerValue);
/* 558 */       PageMerger pageMerger = this.m_service.getPageMerger();
/* 559 */       pageMerger.evaluateResourceInclude("load_document_profile");
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 563 */       Report.trace(null, null, e);
/*     */     }
/*     */ 
/* 567 */     for (String fieldName : allFields)
/*     */     {
/* 569 */       if (DataBinderUtils.getLocalBoolean(this.m_binder, fieldName + ":isExcluded", false))
/*     */       {
/* 571 */         removedFields.add(fieldName);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 576 */     Collection newRsetNames = getCurrentResultSetNames();
/* 577 */     for (String rsetName : newRsetNames)
/*     */     {
/* 579 */       if (!oldRsetNames.contains(rsetName))
/*     */       {
/* 581 */         this.m_binder.removeResultSet(rsetName);
/*     */       }
/*     */     }
/* 584 */     this.m_binder.setLocalData(oldProps);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void vcrGetDocInfo() throws ServiceException, DataException
/*     */   {
/* 590 */     this.m_service.executeServiceSimple("DOC_INFO");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void vcrGetDocInfoByName() throws ServiceException, DataException
/*     */   {
/* 596 */     this.m_service.executeServiceSimple("DOC_INFO_BY_NAME");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void vcrCollateDocInfo()
/*     */     throws ServiceException, DataException
/*     */   {
/* 603 */     DataResultSet docInfo = (DataResultSet)this.m_binder.getResultSet("DOC_INFO");
/* 604 */     docInfo.first();
/* 605 */     String dID = ResultSetUtils.getValue(docInfo, "dID");
/* 606 */     String dDocName = ResultSetUtils.getValue(docInfo, "dDocName");
/* 607 */     String modifiedBy = ResultSetUtils.getValue(docInfo, "dDocLastModifier");
/* 608 */     String createdBy = ResultSetUtils.getValue(docInfo, "dDocCreator");
/* 609 */     String modifiedDate = ResultSetUtils.getValue(docInfo, "dDocLastModifiedDate");
/* 610 */     String createdDate = ResultSetUtils.getValue(docInfo, "dDocCreatedDate");
/*     */ 
/* 613 */     String objectClass = null;
/* 614 */     String triggerField = DocProfileManager.getTriggerField();
/* 615 */     String triggerValue = ResultSetUtils.getValue(docInfo, triggerField);
/* 616 */     if ((triggerValue != null) && (triggerValue.length() > 0))
/*     */     {
/* 618 */       DataResultSet dpSet = SharedObjects.getTable("DocumentProfiles");
/* 619 */       if (dpSet != null)
/*     */       {
/* 621 */         int nameIndex = ResultSetUtils.getIndexMustExist(dpSet, "dpName");
/* 622 */         int triggerIndex = ResultSetUtils.getIndexMustExist(dpSet, "dpTriggerValue");
/* 623 */         for (dpSet.first(); dpSet.isRowPresent(); dpSet.next())
/*     */         {
/* 625 */           if (!dpSet.getStringValue(triggerIndex).equals(triggerValue))
/*     */             continue;
/* 627 */           objectClass = PROFILE_CONTENT_TYPE_PREFIX + dpSet.getStringValue(nameIndex);
/* 628 */           break;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 633 */     if (objectClass == null)
/*     */     {
/* 635 */       objectClass = GLOBAL_PROFILE_CONTENT_TYPE;
/*     */     }
/*     */ 
/* 639 */     DataResultSet vcrNode = createResultSetFromColumnsAndTypes(NODE_COLUMNS, NODE_COL_TYPES);
/* 640 */     Map map = new HashMap();
/* 641 */     map.put("name", this.m_binder.get("dOriginalName"));
/* 642 */     map.put("objectClass", objectClass);
/* 643 */     map.put("path", "");
/* 644 */     map.put("createdBy", createdBy);
/* 645 */     map.put("createdDate", createdDate);
/* 646 */     map.put("modifiedBy", modifiedBy);
/* 647 */     map.put("modifiedDate", modifiedDate);
/* 648 */     vcrNode.addRow(vcrNode.createRow(new MapParameters(map)));
/*     */ 
/* 651 */     String dFormat = ResultSetUtils.getValue(docInfo, "dFormat");
/* 652 */     if ((!dFormat.equals("idcmeta/html")) && (dFormat.length() > 0))
/*     */     {
/* 654 */       String dOriginalName = ResultSetUtils.getValue(docInfo, "dOriginalName");
/* 655 */       String dFileSize = ResultSetUtils.getValue(docInfo, "dFileSize");
/* 656 */       String cgiWebUrl = DirectoryLocator.getCgiWebUrl(true);
/*     */ 
/* 659 */       String rsetName = dDocName + ":idcPrimaryFile";
/* 660 */       DataResultSet primaryFileInfo = createResultSetFromColumnsAndTypes(FILE_REFERENCE_COLUMNS, FILE_REFERENCE_COL_TYPES);
/*     */ 
/* 662 */       Vector v = new IdcVector();
/* 663 */       v.add("vault");
/* 664 */       v.add(dOriginalName);
/* 665 */       v.add(dFormat);
/* 666 */       v.add(dFileSize);
/* 667 */       v.add(cgiWebUrl + "?IdcService=GET_FILE&dID=" + dID + "&dDocName=" + dDocName + "&allowInterrupt=1");
/* 668 */       primaryFileInfo.addRow(v);
/* 669 */       this.m_binder.addResultSet(rsetName, primaryFileInfo);
/*     */ 
/* 672 */       FieldInfo primaryFileField = new FieldInfo();
/* 673 */       if (!docInfo.getFieldInfo("idcPrimaryFile", primaryFileField))
/*     */       {
/* 675 */         primaryFileField.m_name = "idcPrimaryFile";
/* 676 */         Vector fields = new IdcVector();
/* 677 */         fields.add(primaryFileField);
/* 678 */         docInfo.mergeFieldsWithFlags(fields, 0);
/* 679 */         docInfo.getFieldInfo("idcPrimaryFile", primaryFileField);
/*     */       }
/* 681 */       docInfo.setCurrentValue(primaryFileField.m_index, rsetName);
/*     */     }
/*     */ 
/* 685 */     DataResultSet renditionsInfo = createResultSetFromColumnsAndTypes(FILE_REFERENCE_COLUMNS, FILE_REFERENCE_COL_TYPES);
/*     */ 
/* 689 */     String docUrl = this.m_binder.getLocal("DocUrl");
/* 690 */     if (docUrl != null)
/*     */     {
/* 692 */       Map args = new HashMap();
/* 693 */       args.put("dID", dID);
/* 694 */       ResultSet documentsInfo = this.m_workspace.createResultSet("QdocumentsWeb", new MapParameters(args));
/*     */ 
/* 696 */       if (documentsInfo.first())
/*     */       {
/* 698 */         FieldInfo[] documentsFields = ResultSetUtils.createInfoList(documentsInfo, new String[] { "dOriginalName", "dFormat", "dExtension", "dFileSize" }, true);
/*     */ 
/* 701 */         String webExtension = ResultSetUtils.getValue(docInfo, "dWebExtension");
/* 702 */         String name = dDocName;
/* 703 */         if ((webExtension != null) && (webExtension.length() > 0))
/*     */         {
/* 705 */           name = name + '.' + webExtension;
/*     */         }
/*     */ 
/* 708 */         IdcVector v = new IdcVector();
/* 709 */         v.add("web");
/* 710 */         v.add(name);
/* 711 */         v.add(documentsInfo.getStringValue(documentsFields[1].m_index));
/* 712 */         v.add(documentsInfo.getStringValue(documentsFields[3].m_index));
/* 713 */         v.add(docUrl);
/* 714 */         renditionsInfo.addRow(v);
/*     */ 
/* 717 */         String renFlag = ResultSetUtils.getValue(docInfo, "dRendition1");
/* 718 */         String revLabel = ResultSetUtils.getValue(docInfo, "dRevLabel");
/*     */ 
/* 720 */         if ((renFlag != null) && (((renFlag.equals("T")) || (renFlag.equals("G")) || (renFlag.equals("P")))))
/*     */         {
/* 722 */           AdditionalRenditions renSet = (AdditionalRenditions)SharedObjects.getTable("AdditionalRenditions");
/* 723 */           String ext = null;
/*     */           try
/*     */           {
/* 726 */             ext = ResultSetUtils.findValue(renSet, "renFlag", renFlag, "renExtension");
/*     */           }
/*     */           catch (DataException e)
/*     */           {
/* 730 */             Report.trace(null, null, e);
/*     */           }
/*     */ 
/* 733 */           String path = "";
/* 734 */           int i = docUrl.lastIndexOf("/");
/* 735 */           if (i >= 0)
/*     */           {
/* 737 */             path = docUrl.substring(0, i + 1);
/*     */           }
/* 739 */           String curName = dDocName.toLowerCase();
/*     */ 
/* 741 */           String tail = "@" + renFlag + "~" + revLabel;
/* 742 */           if ((ext != null) && (ext.trim().length() > 0))
/*     */           {
/* 744 */             tail = tail + "." + ext;
/*     */           }
/* 746 */           curName = curName + tail.toLowerCase();
/* 747 */           String Result = path + curName;
/* 748 */           int size = sizeOfThumbnail(Result);
/*     */           try
/*     */           {
/* 751 */             Result = URLEncoder.encode(Result, "UTF-8");
/* 752 */             curName = URLEncoder.encode(curName, "UTF-8");
/*     */           }
/*     */           catch (IOException ioe)
/*     */           {
/* 756 */             Report.trace(null, null, ioe);
/*     */           }
/* 758 */           IdcVector v1 = new IdcVector();
/* 759 */           v1.add("thumbnail");
/* 760 */           v1.add(curName);
/* 761 */           v1.add(ext);
/* 762 */           v1.add(Integer.valueOf(size));
/* 763 */           v1.add(Result);
/* 764 */           renditionsInfo.addRow(v1);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 771 */     if (renditionsInfo.getNumRows() > 0)
/*     */     {
/* 773 */       String rsetName = dDocName + ":idcRenditions";
/* 774 */       FieldInfo renditionsField = new FieldInfo();
/* 775 */       if (!docInfo.getFieldInfo("idcRenditions", renditionsField))
/*     */       {
/* 777 */         renditionsField.m_name = "idcRenditions";
/* 778 */         Vector fields = new IdcVector();
/* 779 */         fields.add(renditionsField);
/* 780 */         docInfo.mergeFieldsWithFlags(fields, 0);
/* 781 */         docInfo.getFieldInfo("idcRenditions", renditionsField);
/*     */       }
/* 783 */       docInfo.setCurrentValue(renditionsField.m_index, rsetName);
/* 784 */       this.m_binder.addResultSet(rsetName, renditionsInfo);
/*     */     }
/*     */ 
/* 788 */     this.m_binder.addResultSet("VcrNode", vcrNode);
/* 789 */     this.m_binder.addResultSet("VcrPropertyValues", docInfo);
/* 790 */     this.m_binder.removeResultSet("DOC_INFO");
/* 791 */     this.m_binder.removeResultSet("AssociatedTopFields");
/* 792 */     this.m_binder.removeResultSet("RestrictedLists");
/*     */ 
/* 794 */     PluginFilters.filter("vcrCollateDocInfo", this.m_workspace, this.m_binder, this.m_service);
/*     */   }
/*     */ 
/*     */   public int sizeOfThumbnail(String URLPath)
/*     */   {
/* 801 */     int size = 0;
/*     */     try
/*     */     {
/* 804 */       URL url = new URL(URLPath);
/* 805 */       URLConnection conn = url.openConnection();
/* 806 */       size = conn.getContentLength();
/* 807 */       if (size < 0)
/* 808 */         System.out.println("Could not determine file size.");
/* 809 */       conn.getInputStream().close();
/*     */     }
/*     */     catch (Exception e) {
/* 812 */       Report.trace(null, null, e);
/*     */     }
/* 814 */     return size;
/*     */   }
/*     */ 
/*     */   public void loadProfilesInformation()
/*     */   {
/*     */     try
/*     */     {
/* 821 */       PageMerger pageMerger = this.m_service.getPageMerger();
/* 822 */       pageMerger.evaluateScript("<$utLoadDocumentProfiles('pne_portal', 'PneDocumentProfiles', 'DesignDocumentProfiles', '1')$>");
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 827 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   private Collection getCurrentResultSetNames()
/*     */   {
/* 833 */     Collection c = new HashSet();
/* 834 */     for (String rsetName : this.m_binder.getResultSets().keySet())
/*     */     {
/* 836 */       c.add(rsetName);
/*     */     }
/*     */ 
/* 839 */     return c;
/*     */   }
/*     */ 
/*     */   private DataResultSet createResultSetFromColumnsAndTypes(String[] cols, int[] types)
/*     */   {
/* 844 */     DataResultSet drset = new DataResultSet();
/* 845 */     List fields = new ArrayList();
/* 846 */     for (int i = 0; i < cols.length; ++i)
/*     */     {
/* 848 */       FieldInfo fi = new FieldInfo();
/* 849 */       fi.m_name = cols[i];
/* 850 */       fi.m_type = types[i];
/* 851 */       fields.add(fi);
/*     */     }
/*     */ 
/* 854 */     drset.mergeFieldsWithFlags(fields, 0);
/* 855 */     drset.setDateFormat(this.m_binder.m_localeDateFormat);
/*     */ 
/* 857 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 862 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96436 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.vcr.VcrServiceHandler
 * JD-Core Version:    0.5.4
 */