/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.Features;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocProfileManager
/*     */ {
/*  31 */   public static boolean m_isInitialized = false;
/*  32 */   public static boolean m_isLoaded = false;
/*     */ 
/*  35 */   public static Hashtable<String, ProfileStorage> m_profileStorageMap = null;
/*     */ 
/*  38 */   public static Hashtable<String, Vector<ProfileStorage>> m_profileMetadataSetMap = null;
/*     */ 
/*  42 */   public static Hashtable<String, ProfileStorage> m_profilePrimaryMap = null;
/*     */ 
/*  45 */   public static Hashtable<String, ProfileStorage> m_ruleStorageMap = null;
/*     */ 
/*  48 */   public static Hashtable<String, Vector<ProfileStorage>> m_ruleMetadataSetMap = null;
/*     */   public static final String DEFAULT_METADATASET = "Document";
/*     */   public static final String DEFAULT_STORAGE = "DocumentProfiles";
/*     */ 
/*     */   public static synchronized void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  58 */     if (m_isInitialized)
/*     */       return;
/*  60 */     m_profileStorageMap = new Hashtable();
/*  61 */     m_profileMetadataSetMap = new Hashtable();
/*  62 */     m_profilePrimaryMap = new Hashtable();
/*     */ 
/*  64 */     m_ruleStorageMap = new Hashtable();
/*  65 */     m_ruleMetadataSetMap = new Hashtable();
/*     */ 
/*  67 */     DataResultSet drset = SharedObjects.getTable("ProfileStorages");
/*  68 */     if (drset == null)
/*     */     {
/*  70 */       String errorMsg = LocaleResources.getString("csTableDoesNotExist", null, "ProfileStorages");
/*     */ 
/*  72 */       throw new ServiceException(errorMsg);
/*     */     }
/*     */ 
/*  75 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  77 */       Properties storageProps = drset.getCurrentRowProps();
/*     */ 
/*  79 */       initStorageObject("profile", storageProps);
/*  80 */       initStorageObject("rule", storageProps);
/*     */     }
/*     */ 
/*  83 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static void initStorageObject(String type, Properties props)
/*     */     throws DataException, ServiceException
/*     */   {
/*  90 */     Hashtable storageMap = null;
/*  91 */     Hashtable metadataSetMap = null;
/*     */ 
/*  93 */     if (type.equals("profile"))
/*     */     {
/*  95 */       storageMap = m_profileStorageMap;
/*  96 */       metadataSetMap = m_profileMetadataSetMap;
/*     */     }
/*     */     else
/*     */     {
/* 100 */       storageMap = m_ruleStorageMap;
/* 101 */       metadataSetMap = m_ruleMetadataSetMap;
/*     */     }
/*     */ 
/* 104 */     String storageName = props.getProperty("psName");
/* 105 */     String metadataSetName = props.getProperty("psMetadataSet");
/* 106 */     String storageClassName = props.getProperty("psStorageClass");
/* 107 */     boolean isPrimary = StringUtils.convertToBool(props.getProperty("psIsPrimary"), false);
/*     */ 
/* 110 */     ProfileStorage storage = (ProfileStorage)ComponentClassFactory.createClassInstance(storageClassName, storageClassName, null);
/*     */ 
/* 112 */     storage.init(type, props);
/*     */ 
/* 114 */     storageMap.put(storageName, storage);
/*     */ 
/* 116 */     Vector storageList = (Vector)metadataSetMap.get(metadataSetName);
/* 117 */     if (storageList == null)
/*     */     {
/* 119 */       storageList = new Vector();
/* 120 */       metadataSetMap.put(metadataSetName, storageList);
/*     */     }
/* 122 */     storageList.addElement(storage);
/*     */ 
/* 124 */     if ((!type.equals("profile")) || (!isPrimary))
/*     */       return;
/* 126 */     m_profilePrimaryMap.put(metadataSetName, storage);
/*     */   }
/*     */ 
/*     */   public static void load()
/*     */     throws DataException, ServiceException
/*     */   {
/* 132 */     Enumeration en = m_profileStorageMap.keys();
/* 133 */     while (en.hasMoreElements())
/*     */     {
/* 135 */       String storageName = (String)en.nextElement();
/*     */ 
/* 137 */       ProfileStorage profileStorage = (ProfileStorage)m_profileStorageMap.get(storageName);
/* 138 */       profileStorage.load();
/*     */ 
/* 140 */       ProfileStorage ruleStorage = (ProfileStorage)m_ruleStorageMap.get(storageName);
/* 141 */       ruleStorage.load();
/*     */     }
/*     */ 
/* 146 */     if (m_isLoaded)
/*     */       return;
/* 148 */     ProfileStorage storage = getPrimaryProfileStorage("Document");
/* 149 */     String didSetDefault = storage.getConfigValue("dpDidSetDefaultTriggerField");
/* 150 */     if (!StringUtils.convertToBool(didSetDefault, false))
/*     */     {
/* 152 */       String triggerField = storage.getConfigValue("dpTriggerField");
/* 153 */       if ((triggerField == null) || (triggerField.length() == 0))
/*     */       {
/* 155 */         storage.updateConfigValue("dpTriggerField", "xIdcProfile");
/*     */       }
/*     */ 
/* 158 */       storage.updateConfigValue("dpDidSetDefaultTriggerField", "1");
/*     */     }
/*     */     try
/*     */     {
/* 162 */       createAndUpdateDefaultRules();
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 166 */       Report.warning("docprofile", t, "csDocProfileCreateDefaultRulesError", new Object[0]);
/*     */     }
/*     */ 
/* 169 */     m_isLoaded = true;
/*     */   }
/*     */ 
/*     */   public static Properties getConfiguration()
/*     */     throws DataException, ServiceException
/*     */   {
/* 178 */     return getConfiguration("Document", null);
/*     */   }
/*     */ 
/*     */   public static Properties getConfiguration(String metadataSetName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 184 */     ProfileStorage storage = getPrimaryProfileStorage(metadataSetName);
/* 185 */     return storage.getConfiguration();
/*     */   }
/*     */ 
/*     */   public static String getTriggerField()
/*     */   {
/* 190 */     return getTriggerField("Document", null);
/*     */   }
/*     */ 
/*     */   public static String getTriggerField(String metadataSetName, ExecutionContext cxt)
/*     */   {
/* 195 */     String triggerField = null;
/*     */     try
/*     */     {
/* 198 */       ProfileStorage storage = getPrimaryProfileStorage(metadataSetName);
/* 199 */       return storage.getConfigValue("dpTriggerField");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */ 
/* 207 */     return triggerField;
/*     */   }
/*     */ 
/*     */   public static void updateTrigger(String fieldName) throws DataException, ServiceException
/*     */   {
/* 212 */     updateTrigger("Document", fieldName, null);
/*     */   }
/*     */ 
/*     */   public static void updateTrigger(String metadataSetName, String fieldName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 218 */     ProfileStorage storage = getPrimaryProfileStorage(metadataSetName);
/* 219 */     storage.updateConfigValue("dpTriggerField", fieldName);
/*     */   }
/*     */ 
/*     */   public static DocProfileData getProfile(String name)
/*     */     throws DataException, ServiceException
/*     */   {
/* 227 */     return getProfile("DocumentProfiles", name, null);
/*     */   }
/*     */ 
/*     */   public static DocProfileData getProfile(String storageName, String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 233 */     ProfileStorage storage = getProfileStorage(storageName);
/* 234 */     return storage.getData(name, cxt);
/*     */   }
/*     */ 
/*     */   public static void createOrUpdateProfile(String name, DataBinder binder, boolean isNew, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 240 */     createOrUpdateProfile("DocumentProfiles", name, binder, isNew, cxt);
/*     */   }
/*     */ 
/*     */   public static void createOrUpdateProfile(String storageName, String name, DataBinder binder, boolean isNew, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 246 */     ProfileStorage storage = getProfileStorage(storageName);
/* 247 */     storage.createOrUpdate(name, binder, isNew, cxt);
/*     */   }
/*     */ 
/*     */   public static void deleteProfile(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 253 */     deleteProfile("DocumentProfiles", name, cxt);
/*     */   }
/*     */ 
/*     */   public static void deleteProfile(String storageName, String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 259 */     ProfileStorage storage = getProfileStorage(storageName);
/* 260 */     storage.deleteItem(name, cxt);
/*     */   }
/*     */ 
/*     */   public static DocProfileData getRule(String name)
/*     */     throws ServiceException
/*     */   {
/* 268 */     return getRule("DocumentProfiles", name, null);
/*     */   }
/*     */ 
/*     */   public static DocProfileData getRule(String storageName, String name, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 274 */     ProfileStorage storage = getRuleStorage(storageName);
/* 275 */     return storage.getData(name, cxt);
/*     */   }
/*     */ 
/*     */   public static void createOrUpdateRule(String name, DataBinder binder, boolean isNew, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 281 */     createOrUpdateRule("DocumentProfiles", name, binder, isNew, cxt);
/*     */   }
/*     */ 
/*     */   public static void createOrUpdateRule(String storageName, String name, DataBinder binder, boolean isNew, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 287 */     ProfileStorage storage = getRuleStorage(storageName);
/* 288 */     storage.createOrUpdate(name, binder, isNew, cxt);
/*     */   }
/*     */ 
/*     */   public static void deleteRule(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 294 */     deleteRule("DocumentProfiles", name, cxt);
/*     */   }
/*     */ 
/*     */   public static void deleteRule(String storageName, String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 300 */     ProfileStorage storage = getRuleStorage(storageName);
/* 301 */     storage.deleteItem(name, cxt);
/*     */   }
/*     */ 
/*     */   public static boolean isRuleInUse(String ruleName, String[] profileName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 307 */     return isRuleInUse("DocumentProfiles", ruleName, profileName, null);
/*     */   }
/*     */ 
/*     */   public static boolean isRuleInUse(String storageName, String ruleName, String[] profileName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 313 */     ProfileStorage storage = getProfileStorage(storageName);
/* 314 */     DataResultSet profileSet = storage.getListingSet(cxt);
/* 315 */     for (profileSet.first(); profileSet.isRowPresent(); profileSet.next())
/*     */     {
/* 317 */       String pName = ResultSetUtils.getValue(profileSet, "dpName");
/* 318 */       DocProfileData data = storage.getData(pName, cxt);
/* 319 */       DataBinder binder = data.getData();
/* 320 */       DataResultSet ruleSet = (DataResultSet)binder.getResultSet("ProfileRules");
/* 321 */       if (ruleSet == null)
/*     */         continue;
/* 323 */       for (ruleSet.first(); ruleSet.isRowPresent(); ruleSet.next())
/*     */       {
/* 325 */         String curRuleName = ResultSetUtils.getValue(ruleSet, "dpRuleName");
/* 326 */         if (!curRuleName.equalsIgnoreCase(ruleName))
/*     */           continue;
/* 328 */         if (profileName != null)
/*     */         {
/* 330 */           profileName[0] = pName;
/*     */         }
/* 332 */         return true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 337 */     return false;
/*     */   }
/*     */ 
/*     */   static void createAndUpdateDefaultRules() throws DataException, ServiceException
/*     */   {
/* 342 */     DataResultSet docMetaSet = SharedObjects.getTable("DocMetaDefinition");
/* 343 */     if (!Features.checkLevel("JDBC", null))
/*     */     {
/* 345 */       return;
/*     */     }
/*     */ 
/* 348 */     FieldInfo[] fi = ResultSetUtils.createInfoList(docMetaSet, new String[] { "dName", "dComponentName" }, true);
/*     */ 
/* 350 */     int nameIndex = fi[0].m_index;
/* 351 */     int cmpNameIndex = fi[1].m_index;
/*     */ 
/* 353 */     Map map = new HashMap();
/* 354 */     for (docMetaSet.first(); docMetaSet.isRowPresent(); docMetaSet.next())
/*     */     {
/* 356 */       String name = docMetaSet.getStringValue(nameIndex);
/* 357 */       String cmpName = docMetaSet.getStringValue(cmpNameIndex);
/* 358 */       if (cmpName.length() == 0)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 363 */       List list = (List)map.get(cmpName);
/* 364 */       if (list == null)
/*     */       {
/* 366 */         list = new ArrayList();
/* 367 */         map.put(cmpName, list);
/*     */       }
/* 369 */       list.add(name);
/*     */     }
/*     */ 
/* 372 */     String[][] ruleInfo = { { "_hide", "apDefaultHideRule_", "0", "hidden" }, { "_edit", "apDefaultEditRule_", "1", "edit" } };
/*     */ 
/* 379 */     Set keys = map.keySet();
/* 380 */     for (String key : keys)
/*     */     {
/* 382 */       List list = (List)map.get(key);
/*     */ 
/* 384 */       for (int count = 0; count < ruleInfo.length; ++count)
/*     */       {
/* 386 */         String name = key + ruleInfo[count][0];
/* 387 */         String descStub = ruleInfo[count][1];
/* 388 */         String isGroupStr = ruleInfo[count][2];
/* 389 */         String fieldType = ruleInfo[count][3];
/*     */ 
/* 391 */         DocProfileData data = getRule(name);
/* 392 */         boolean isNew = data == null;
/* 393 */         boolean hasChanged = false;
/*     */ 
/* 395 */         DataBinder binder = null;
/* 396 */         if (!isNew)
/*     */         {
/* 398 */           binder = data.getData();
/*     */         }
/*     */ 
/* 401 */         if (binder == null)
/*     */         {
/* 403 */           hasChanged = true;
/* 404 */           binder = new DataBinder();
/* 405 */           binder.putLocal("dpRuleName", name);
/* 406 */           binder.putLocal("dpRuleDescription", descStub + key);
/*     */ 
/* 408 */           boolean isGrouped = StringUtils.convertToBool(isGroupStr, false);
/* 409 */           if (isGrouped)
/*     */           {
/* 411 */             binder.putLocal("dpRuleIsGroup", "1");
/* 412 */             binder.putLocal("dpIsGroupDefaultHide", "1");
/* 413 */             binder.putLocal("dpRuleHasHeader", "1");
/* 414 */             binder.putLocal("dpRuleGroupHeader", "wwDefaultRuleHeader_" + key);
/*     */           }
/*     */         }
/*     */ 
/* 418 */         DataResultSet ruleSet = (DataResultSet)binder.getResultSet("RuleFields");
/* 419 */         if (ruleSet == null)
/*     */         {
/* 421 */           hasChanged = true;
/* 422 */           ruleSet = new DataResultSet(DocProfileScriptUtils.DP_FIELD_RULE_COLUMNS);
/* 423 */           binder.addResultSet("RuleFields", ruleSet);
/*     */         }
/*     */ 
/* 426 */         int clmnIndex = ResultSetUtils.getIndexMustExist(ruleSet, "dpRuleFieldName");
/* 427 */         int typeIndex = ResultSetUtils.getIndexMustExist(ruleSet, "dpRuleFieldType");
/* 428 */         int posIndex = ResultSetUtils.getIndexMustExist(ruleSet, "dpRuleFieldPosition");
/* 429 */         int num = list.size();
/* 430 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 432 */           String clmn = (String)list.get(i);
/* 433 */           Vector row = ruleSet.findRow(clmnIndex, clmn);
/* 434 */           if (row != null)
/*     */             continue;
/* 436 */           row = ruleSet.createEmptyRow();
/* 437 */           row.setElementAt(clmn, clmnIndex);
/* 438 */           row.setElementAt(fieldType, typeIndex);
/* 439 */           row.setElementAt("top", posIndex);
/* 440 */           ruleSet.addRow(row);
/* 441 */           hasChanged = true;
/*     */         }
/*     */ 
/* 445 */         if (!hasChanged)
/*     */           continue;
/* 447 */         createOrUpdateRule(name, binder, isNew, new ExecutionContextAdaptor());
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataResultSet getListingSet(String metadataSetName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 459 */     String[] listingFields = { "dpStorageName", "dpName", "dpDescription", "dpTriggerValue", "dpDisplayLabel", "dDocClass" };
/*     */ 
/* 464 */     DataResultSet listingSet = new DataResultSet(listingFields);
/* 465 */     PropParameters rowParams = new PropParameters(null);
/*     */ 
/* 467 */     Vector storageList = getProfileStorageList(metadataSetName);
/* 468 */     int numStorages = storageList.size();
/* 469 */     for (int i = 0; i < numStorages; ++i)
/*     */     {
/* 471 */       ProfileStorage storage = (ProfileStorage)storageList.elementAt(i);
/* 472 */       DataResultSet storageSet = storage.getListingSet(cxt);
/*     */ 
/* 475 */       String storageName = storage.getStorageName();
/* 476 */       for (storageSet.first(); storageSet.isRowPresent(); storageSet.next())
/*     */       {
/* 478 */         Properties rowProps = storageSet.getCurrentRowProps();
/* 479 */         rowProps.put("dpStorageName", storageName);
/* 480 */         rowParams.m_properties = rowProps;
/* 481 */         Vector row = listingSet.createRow(rowParams);
/* 482 */         listingSet.addRow(row);
/*     */       }
/*     */     }
/*     */ 
/* 486 */     return listingSet;
/*     */   }
/*     */ 
/*     */   public static Vector getTriggerMapList(String metadataSetName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 492 */     Vector triggerList = new Vector();
/*     */ 
/* 494 */     Vector storageList = getProfileStorageList(metadataSetName);
/* 495 */     int numStorages = storageList.size();
/* 496 */     for (int i = 0; i < numStorages; ++i)
/*     */     {
/* 498 */       ProfileStorage storage = (ProfileStorage)storageList.elementAt(i);
/* 499 */       Vector storageTriggerList = storage.getTriggerMapList(cxt);
/* 500 */       triggerList.addAll(storageTriggerList);
/*     */     }
/*     */ 
/* 503 */     return triggerList;
/*     */   }
/*     */ 
/*     */   public static Vector getGlobalRules(String metadataSetName, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 509 */     Vector globalList = new Vector();
/*     */ 
/* 511 */     Vector storageList = getRuleStorageList(metadataSetName);
/* 512 */     int numStorages = storageList.size();
/* 513 */     for (int i = 0; i < numStorages; ++i)
/*     */     {
/* 515 */       ProfileStorage storage = (ProfileStorage)storageList.elementAt(i);
/* 516 */       Vector storageGlobalList = storage.getGlobalRules(cxt);
/* 517 */       globalList.addAll(storageGlobalList);
/*     */     }
/*     */ 
/* 520 */     sortByPriority(globalList, "dpRuleGlobalPriority");
/*     */ 
/* 522 */     return globalList;
/*     */   }
/*     */ 
/*     */   protected static void sortByPriority(Vector globals, String priorityKey)
/*     */   {
/* 529 */     String prKey = priorityKey;
/* 530 */     IdcComparator cmp = new Object(prKey)
/*     */     {
/*     */       public int compare(Object obj1, Object obj2)
/*     */       {
/* 534 */         DocProfileData data1 = (DocProfileData)obj1;
/* 535 */         DocProfileData data2 = (DocProfileData)obj2;
/*     */ 
/* 537 */         int pr1 = NumberUtils.parseInteger(data1.getValue(this.val$prKey), 1);
/* 538 */         int pr2 = NumberUtils.parseInteger(data2.getValue(this.val$prKey), 1);
/*     */ 
/* 540 */         int result = 0;
/* 541 */         if (pr1 < pr2)
/*     */         {
/* 543 */           result = -1;
/*     */         }
/* 545 */         else if (pr1 > pr2)
/*     */         {
/* 547 */           result = 1;
/*     */         }
/*     */ 
/* 550 */         return result;
/*     */       }
/*     */     };
/* 553 */     Sort.sortVector(globals, cmp);
/*     */   }
/*     */ 
/*     */   public static void checkProfileLinks(String profileName, DataBinder binder, ExecutionContext cxt, PageMerger pageMerger, boolean isComputeAll)
/*     */   {
/* 562 */     checkProfileLinks("DocumentProfiles", profileName, binder, cxt, pageMerger, isComputeAll);
/*     */   }
/*     */ 
/*     */   public static void checkProfileLinks(String storageName, String profileName, DataBinder binder, ExecutionContext cxt, PageMerger pageMerger, boolean isComputeAll)
/*     */   {
/* 569 */     if (storageName == null)
/*     */     {
/* 571 */       storageName = "DocumentProfiles";
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 576 */       DocProfileData profileData = getProfile(storageName, profileName, cxt);
/* 577 */       if (profileData != null)
/*     */       {
/* 579 */         boolean hasLinkScripts = StringUtils.convertToBool(profileData.getValue("dpHasLinkScripts"), false);
/*     */ 
/* 581 */         if (hasLinkScripts)
/*     */         {
/* 583 */           computeLinks(profileData, binder, cxt, pageMerger, isComputeAll);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 589 */       Report.trace("docprofile", "DocProfileManager.checkProfileLinks:Unable to compute profile personalization links for profile " + profileName, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static void computeLinks(DocProfileData profileData, DataBinder binder, ExecutionContext cxt, PageMerger pageMerger, boolean isComputeAll)
/*     */     throws ServiceException, IOException, ParseSyntaxException
/*     */   {
/* 598 */     DataBinder profileBinder = profileData.getData();
/* 599 */     String[][] links = { { "checkinLink", "dpHasCheckinLinkScript", "dpIsCheckin", "dpCheckinEnabled" }, { "searchLink", "dpHasSearchLinkScript", "dpIsSearch", "dpSearchEnabled" } };
/*     */ 
/* 604 */     for (int i = 0; i < links.length; ++i)
/*     */     {
/* 608 */       String key = links[i][2];
/* 609 */       boolean isEnabled = StringUtils.convertToBool(binder.getLocal(key), false);
/* 610 */       if ((!isComputeAll) && (!isEnabled)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 614 */       boolean hasScript = StringUtils.convertToBool(profileBinder.getLocal(links[i][1]), false);
/* 615 */       if (!hasScript)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 621 */       String script = DocProfileScriptUtils.computeScriptString("", profileBinder, links[i][0], false);
/*     */ 
/* 623 */       if ((script == null) || (script.trim().length() <= 0))
/*     */         continue;
/* 625 */       DataBinder saveBinder = null;
/*     */       try
/*     */       {
/* 628 */         saveBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 629 */         DataBinder workBinder = new DataBinder();
/* 630 */         workBinder.merge(saveBinder);
/* 631 */         cxt.setCachedObject("DataBinder", workBinder);
/* 632 */         pageMerger.setDataBinder(workBinder);
/*     */ 
/* 634 */         Report.trace("docprofile", "computeLinks: for profile " + profileBinder.getLocal("dpName") + " with script:\n" + script, null);
/*     */ 
/* 637 */         pageMerger.evaluateScriptReportError(script);
/* 638 */         String val = workBinder.getLocal("isLinkActive");
/* 639 */         boolean bVal = StringUtils.convertToBool(val, false);
/*     */ 
/* 641 */         if (isEnabled)
/*     */         {
/* 645 */           binder.putLocal(key, "" + bVal);
/*     */         }
/* 647 */         binder.putLocal(links[i][3], "" + bVal);
/*     */       }
/*     */       finally
/*     */       {
/* 651 */         if (saveBinder != null)
/*     */         {
/* 653 */           cxt.setCachedObject("DataBinder", saveBinder);
/* 654 */           pageMerger.setDataBinder(saveBinder);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DocProfileStates createDocProfileStatesObject(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 667 */     DocProfileStates statesObj = (DocProfileStates)ComponentClassFactory.createClassInstance("DocProfileStates", "intradoc.server.DocProfileStates", "!csDocProfileStatesClassError");
/*     */ 
/* 670 */     statesObj.init(binder, cxt);
/* 671 */     return statesObj;
/*     */   }
/*     */ 
/*     */   public static void loadDocumentProfile(DataBinder binder, ExecutionContext cxt, boolean isIncoming)
/*     */     throws DataException, ServiceException
/*     */   {
/* 677 */     loadDocumentProfile("Document", binder, cxt, isIncoming);
/*     */   }
/*     */ 
/*     */   public static void loadDocumentProfile(String metadataSetName, DataBinder binder, ExecutionContext cxt, boolean isIncoming)
/*     */     throws DataException, ServiceException
/*     */   {
/* 683 */     DocProfileStates statesObj = createDocProfileStatesObject(binder, cxt);
/* 684 */     statesObj.loadDocumentProfile(metadataSetName, isIncoming, cxt);
/*     */   }
/*     */ 
/*     */   public static DataBinder evaluateGlobalRulesAndProfile(String profileName, DataBinder dataBinder, ExecutionContext cxt, boolean isPreview)
/*     */     throws DataException, ServiceException
/*     */   {
/* 691 */     return evaluateGlobalRulesAndProfile("DocumentProfiles", profileName, dataBinder, cxt, isPreview);
/*     */   }
/*     */ 
/*     */   public static DataBinder evaluateGlobalRulesAndProfile(String storageName, String profileName, DataBinder dataBinder, ExecutionContext cxt, boolean isPreview)
/*     */     throws DataException, ServiceException
/*     */   {
/* 699 */     ProfileStorage storage = getProfileStorage(storageName);
/* 700 */     String metadataSetName = storage.getMetadataSetName();
/*     */ 
/* 702 */     DocProfileStates statesObj = createDocProfileStatesObject(dataBinder, cxt);
/* 703 */     DataBinder resultBinder = statesObj.evaluateGlobalRulesAndProfile(metadataSetName, storageName, profileName, isPreview, cxt);
/*     */ 
/* 705 */     return resultBinder;
/*     */   }
/*     */ 
/*     */   public static String determinePageForTest(DataBinder binder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 711 */     DocProfileStates statesObj = createDocProfileStatesObject(binder, null);
/* 712 */     return statesObj.determinePageForTest(binder);
/*     */   }
/*     */ 
/*     */   public static ProfileStorage getProfileStorage(String storageName)
/*     */     throws DataException
/*     */   {
/* 720 */     ProfileStorage profileStorage = (ProfileStorage)m_profileStorageMap.get(storageName);
/* 721 */     if (profileStorage == null)
/*     */     {
/* 723 */       throw new DataException("The profile storage '" + storageName + "' does not exist.");
/*     */     }
/* 725 */     return profileStorage;
/*     */   }
/*     */ 
/*     */   public static Vector<ProfileStorage> getProfileStorageList(String metadataSetName)
/*     */     throws DataException
/*     */   {
/* 731 */     Vector storageList = (Vector)m_profileMetadataSetMap.get(metadataSetName);
/* 732 */     if (storageList == null)
/*     */     {
/* 734 */       throw new DataException("The profile metadata set '" + metadataSetName + "' does not exist.");
/*     */     }
/*     */ 
/* 737 */     return storageList;
/*     */   }
/*     */ 
/*     */   public static ProfileStorage getPrimaryProfileStorage(String metadataSetName)
/*     */     throws DataException
/*     */   {
/* 743 */     ProfileStorage profileStorage = (ProfileStorage)m_profilePrimaryMap.get(metadataSetName);
/* 744 */     if (profileStorage == null)
/*     */     {
/* 746 */       throw new DataException("The profile metadata set '" + metadataSetName + "' does not exist.");
/*     */     }
/*     */ 
/* 749 */     return profileStorage;
/*     */   }
/*     */ 
/*     */   public static ProfileStorage getRuleStorage(String storageName)
/*     */   {
/* 754 */     ProfileStorage ruleStorage = (ProfileStorage)m_ruleStorageMap.get(storageName);
/* 755 */     return ruleStorage;
/*     */   }
/*     */ 
/*     */   public static Vector<ProfileStorage> getRuleStorageList(String metadataSetName)
/*     */     throws DataException
/*     */   {
/* 761 */     Vector storageList = (Vector)m_ruleMetadataSetMap.get(metadataSetName);
/* 762 */     if (storageList == null)
/*     */     {
/* 764 */       throw new DataException("The profile metadata set '" + metadataSetName + "' does not exist.");
/*     */     }
/*     */ 
/* 767 */     return storageList;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 772 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96965 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocProfileManager
 * JD-Core Version:    0.5.4
 */