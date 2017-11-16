/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.subject.SearchConfigSubjectCallback;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TargetedQuickSearchHandler extends ServiceHandler
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void saveAdminTargetedQuickSearch()
/*     */     throws ServiceException, DataException
/*     */   {
/*  42 */     String role = SharedObjects.getEnvironmentValue("TargetedQuickSearchAdminRole");
/*  43 */     if ((role == null) || (role.length() == 0))
/*     */     {
/*  45 */       role = "admin";
/*     */     }
/*  47 */     if (!SecurityUtils.isUserOfRole(this.m_service.getUserData(), role))
/*     */     {
/*  49 */       String msg = LocaleUtils.encodeMessage("csAdminTargetedQuickSearchPermissionDenied", null);
/*  50 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/*  53 */     String action = this.m_binder.get("tqsAction");
/*  54 */     String key = this.m_binder.getLocal("tqsKey");
/*  55 */     String label = this.m_binder.getLocal("tqsLabel");
/*  56 */     String searchFormType = this.m_binder.getLocal("searchFormType");
/*  57 */     String queryText = this.m_binder.getLocal("QueryText");
/*  58 */     String queryFullText = this.m_binder.getLocal("QueryFullText");
/*  59 */     String queryFieldValues = this.m_binder.getLocal("QueryFieldValues");
/*  60 */     String resultCount = this.m_binder.getLocal("ResultCount");
/*  61 */     String sortField = this.m_binder.getLocal("SortField");
/*  62 */     String sortOrder = this.m_binder.getLocal("SortOrder");
/*  63 */     String searchProviders = this.m_binder.getLocal("SearchProviders");
/*  64 */     String searchQueryFormat = this.m_binder.getLocal("SearchQueryFormat");
/*     */ 
/*  66 */     FileUtils.reserveDirectory(SearchConfigSubjectCallback.DIRECTORY);
/*     */     try
/*     */     {
/*  69 */       DataBinder binder = ResourceUtils.readDataBinder(SearchConfigSubjectCallback.DIRECTORY, "targeted_quick_searches.hda");
/*     */ 
/*  71 */       DataResultSet rset = (DataResultSet)binder.getResultSet("AdminTargetedQuickSearches");
/*     */ 
/*  73 */       FieldInfo fi = new FieldInfo();
/*  74 */       rset.getFieldInfo("tqsKey", fi);
/*     */ 
/*  76 */       if (action.equalsIgnoreCase("edit"))
/*     */       {
/*  78 */         String tqsOldKey = this.m_binder.get("tqsOldKey");
/*  79 */         if (rset.findRow(fi.m_index, tqsOldKey) != null)
/*     */         {
/*  81 */           rset.deleteCurrentRow();
/*     */         }
/*     */ 
/*     */       }
/*  86 */       else if (rset.findRow(fi.m_index, key) != null)
/*     */       {
/*  88 */         String msg = LocaleUtils.encodeMessage("csTargetedQuickSearchAlreadyExists", null, key);
/*  89 */         this.m_service.createServiceException(null, msg);
/*     */       }
/*     */ 
/*  93 */       DataResultSet newRowResultSet = new DataResultSet(SearchConfigSubjectCallback.TARGETED_QUICK_SEARCH_COLUMNS);
/*  94 */       Vector v = new IdcVector();
/*  95 */       v.addElement(key);
/*  96 */       v.addElement(label);
/*  97 */       v.addElement(searchFormType);
/*  98 */       v.addElement(queryText);
/*  99 */       v.addElement(queryFullText);
/* 100 */       v.addElement(queryFieldValues);
/* 101 */       v.addElement(searchQueryFormat);
/* 102 */       v.addElement(resultCount);
/* 103 */       v.addElement(sortField);
/* 104 */       v.addElement(sortOrder);
/* 105 */       v.addElement(searchProviders);
/*     */ 
/* 107 */       newRowResultSet.addRow(v);
/*     */ 
/* 109 */       rset.mergeFields(newRowResultSet);
/* 110 */       rset.merge("tqsKey", newRowResultSet, false);
/*     */ 
/* 112 */       ResultSetUtils.sortResultSet(rset, new String[] { "tqsKey" });
/* 113 */       ResourceUtils.serializeDataBinder(SearchConfigSubjectCallback.DIRECTORY, "targeted_quick_searches.hda", binder, true, true);
/*     */ 
/* 117 */       SharedObjects.putTable("AdminTargetedQuickSearches", rset);
/*     */     }
/*     */     finally
/*     */     {
/* 121 */       FileUtils.releaseDirectory(SearchConfigSubjectCallback.DIRECTORY);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void saveUserTargetedQuickSearch() throws ServiceException, DataException
/*     */   {
/* 128 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/* 130 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 131 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 134 */     String topicString = "addMruRow:pne_portal:UserTargetedQuickSearches:tqsKey,tqsLabel,searchFormType,QueryText,QueryFullText,QueryFieldValues,SearchQueryFormat,ResultCount,SortField,SortOrder,SearchProviders,listTemplateId";
/*     */ 
/* 137 */     this.m_binder.putLocal("topicString1", topicString);
/* 138 */     this.m_binder.putLocal("topicString2", "updateKeys:pne_portal:touchCacheKey:1");
/*     */ 
/* 140 */     String key = this.m_binder.getLocal("tqsKey");
/* 141 */     String oldKey = this.m_binder.getLocal("tqsOldKey");
/*     */ 
/* 143 */     loadUserTargetedQuickSearches();
/* 144 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet("UserTargetedQuickSearches");
/* 145 */     if ((oldKey != null) && (oldKey.length() > 0) && (!key.equals(oldKey)) && (rset != null))
/*     */     {
/* 147 */       String deleteTopic = "deleteRows:pne_portal:UserTargetedQuickSearches:" + StringUtils.urlEncode(oldKey);
/*     */ 
/* 149 */       this.m_binder.putLocal("topicString3", deleteTopic);
/* 150 */       this.m_binder.putLocal("numTopics", "3");
/*     */     }
/*     */     else
/*     */     {
/* 154 */       if ((oldKey == null) && (rset != null))
/*     */       {
/* 156 */         FieldInfo fi = new FieldInfo();
/* 157 */         rset.getFieldInfo("tqsKey", fi);
/* 158 */         if (rset.findRow(fi.m_index, key) != null)
/*     */         {
/* 160 */           String msg = LocaleUtils.encodeMessage("csTargetedQuickSearchAlreadyExists", null, key);
/* 161 */           this.m_service.createServiceException(null, msg);
/*     */         }
/*     */       }
/*     */ 
/* 165 */       this.m_binder.putLocal("numTopics", "2");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteAdminTargetedQuickSearch() throws ServiceException, DataException
/*     */   {
/* 172 */     String role = SharedObjects.getEnvironmentValue("TargetedQuickSearchAdminRole");
/* 173 */     if ((role == null) || (role.length() == 0))
/*     */     {
/* 175 */       role = "admin";
/*     */     }
/* 177 */     boolean allowed = SecurityUtils.isUserOfRole(this.m_service.getUserData(), role);
/* 178 */     if (!allowed)
/*     */     {
/* 180 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 181 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 184 */     String key = this.m_binder.get("tqsKey");
/*     */ 
/* 186 */     FileUtils.reserveDirectory(SearchConfigSubjectCallback.DIRECTORY);
/*     */     try
/*     */     {
/* 189 */       DataBinder binder = ResourceUtils.readDataBinder(SearchConfigSubjectCallback.DIRECTORY, "targeted_quick_searches.hda");
/*     */ 
/* 191 */       DataResultSet rset = (DataResultSet)binder.getResultSet("AdminTargetedQuickSearches");
/*     */ 
/* 193 */       FieldInfo fi = new FieldInfo();
/* 194 */       rset.getFieldInfo("tqsKey", fi);
/* 195 */       rset.findRow(fi.m_index, key);
/*     */ 
/* 197 */       rset.deleteCurrentRow();
/*     */ 
/* 199 */       ResourceUtils.serializeDataBinder(SearchConfigSubjectCallback.DIRECTORY, "targeted_quick_searches.hda", binder, true, true);
/*     */ 
/* 203 */       SharedObjects.putTable("AdminTargetedQuickSearches", rset);
/*     */     }
/*     */     finally
/*     */     {
/* 207 */       FileUtils.releaseDirectory(SearchConfigSubjectCallback.DIRECTORY);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteUserTargetedQuickSearch() throws ServiceException, DataException
/*     */   {
/* 214 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/* 216 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 217 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 220 */     String key = this.m_binder.get("tqsKey");
/* 221 */     String topicString = "deleteRows:pne_portal:UserTargetedQuickSearches:" + StringUtils.urlEncode(key);
/* 222 */     this.m_binder.putLocal("topicString1", topicString);
/* 223 */     this.m_binder.putLocal("numTopics", "1");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadUserTargetedQuickSearchDef() throws ServiceException, DataException
/*     */   {
/* 229 */     String key = this.m_binder.get("tqsKey");
/*     */ 
/* 231 */     loadUserTargetedQuickSearches();
/*     */ 
/* 233 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet("UserTargetedQuickSearches");
/* 234 */     if (rset == null)
/*     */     {
/* 236 */       String msg = LocaleUtils.encodeMessage("csNoSuchUserTargetedQuickSearch", null, key);
/* 237 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 240 */     FieldInfo fi = new FieldInfo();
/* 241 */     rset.getFieldInfo("tqsKey", fi);
/*     */ 
/* 243 */     if (rset.findRow(fi.m_index, key) == null)
/*     */     {
/* 245 */       String msg = LocaleUtils.encodeMessage("csNoSuchUserTargetedQuickSearch", null, key);
/* 246 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */     else
/*     */     {
/* 250 */       this.m_binder.mergeResultSetRowIntoLocalData(rset);
/*     */     }
/*     */ 
/* 253 */     this.m_binder.putLocal("tqsOldKey", key);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadAdminTargetedQuickSearchDef() throws ServiceException, DataException
/*     */   {
/* 259 */     String key = this.m_binder.get("tqsKey");
/* 260 */     DataBinder binder = ResourceUtils.readDataBinder(SearchConfigSubjectCallback.DIRECTORY, "targeted_quick_searches.hda");
/*     */ 
/* 262 */     DataResultSet rset = (DataResultSet)binder.getResultSet("AdminTargetedQuickSearches");
/*     */ 
/* 264 */     FieldInfo fi = new FieldInfo();
/* 265 */     rset.getFieldInfo("tqsKey", fi);
/*     */ 
/* 267 */     if (rset.findRow(fi.m_index, key) == null)
/*     */     {
/* 269 */       String msg = LocaleUtils.encodeMessage("csNoSuchAdminTargetedQuickSearch", null, key);
/* 270 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */     else
/*     */     {
/* 274 */       this.m_binder.mergeResultSetRowIntoLocalData(rset);
/*     */     }
/*     */ 
/* 277 */     this.m_binder.putLocal("tqsOldKey", key);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void enableAdminTargetedQuickSearch() throws ServiceException, DataException
/*     */   {
/* 283 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/* 285 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 286 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 289 */     String key = this.m_binder.get("tqsKey");
/* 290 */     key = StringUtils.encodeUrlStyle(key, '%', false, "full", "UTF8");
/* 291 */     String topicString = "deleteKeys:pne_portal:adminTargetedQuickSearch!" + key + "!isDisabled";
/* 292 */     this.m_binder.putLocal("topicString1", topicString);
/* 293 */     this.m_binder.putLocal("numTopics", "1");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void disableAdminTargetedQuickSearch() throws ServiceException, DataException
/*     */   {
/* 299 */     if ((this.m_service.m_userData == null) || (this.m_service.m_userData.m_name.equals("anonymous")))
/*     */     {
/* 301 */       String msg = LocaleUtils.encodeMessage("csAccessDenied", null);
/* 302 */       this.m_service.createServiceException(null, msg);
/*     */     }
/*     */ 
/* 305 */     String key = this.m_binder.get("tqsKey");
/* 306 */     key = StringUtils.encodeUrlStyle(key, '%', false, "full", "UTF8");
/* 307 */     String topicString = "updateKeys:pne_portal:adminTargetedQuickSearch!" + key + "!isDisabled:1";
/* 308 */     this.m_binder.putLocal("topicString1", topicString);
/* 309 */     this.m_binder.putLocal("numTopics", "1");
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadUserTargetedQuickSearches()
/*     */   {
/* 315 */     PageMerger pm = this.m_service.getPageMerger();
/*     */     try
/*     */     {
/* 319 */       pm.evaluateScript("<$exec utLoadResultSet(\"pne_portal\", \"UserTargetedQuickSearches\")$>");
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 323 */       Report.trace(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void loadAdminTargetedQuickSearchesForUser()
/*     */   {
/* 330 */     DataResultSet atqs = new DataResultSet();
/* 331 */     atqs.copy(SharedObjects.getTable("AdminTargetedQuickSearches"));
/*     */ 
/* 333 */     if (atqs == null)
/*     */       return;
/* 335 */     FieldInfo fi = new FieldInfo();
/* 336 */     Vector vfi = new IdcVector();
/* 337 */     fi.m_name = "isDisabled";
/* 338 */     vfi.addElement(fi);
/* 339 */     atqs.mergeFieldsWithFlags(vfi, 2);
/* 340 */     int newColIndex = fi.m_index;
/*     */ 
/* 342 */     TopicInfo topicInfo = UserProfileUtils.getTopicInfo(this.m_service, "pne_portal");
/*     */ 
/* 344 */     for (atqs.first(); atqs.isRowPresent(); atqs.next())
/*     */     {
/* 346 */       Vector rowValues = atqs.getCurrentRowValues();
/* 347 */       rowValues.setElementAt("false", newColIndex);
/*     */ 
/* 349 */       if (topicInfo == null)
/*     */         continue;
/* 351 */       String tqsKey = atqs.getStringValueByName("tqsKey");
/* 352 */       tqsKey = StringUtils.encodeUrlStyle(tqsKey, '%', false, "full", "UTF8");
/* 353 */       String disabledKey = "adminTargetedQuickSearch!" + tqsKey + "!isDisabled";
/* 354 */       String tqsIsDisabled = topicInfo.m_data.getLocal(disabledKey);
/*     */ 
/* 356 */       if ((tqsIsDisabled == null) || (!StringUtils.convertToBool(tqsIsDisabled, false)))
/*     */         continue;
/* 358 */       rowValues.setElementAt("true", newColIndex);
/*     */     }
/*     */ 
/* 363 */     this.m_binder.addResultSet("AdminTargetedQuickSearches", atqs);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 369 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92074 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.TargetedQuickSearchHandler
 * JD-Core Version:    0.5.4
 */