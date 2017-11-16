/*     */ package intradoc.autosuggest.datastore;
/*     */ 
/*     */ import intradoc.autosuggest.AutoSuggestConstants;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ 
/*     */ public class ContextInfoStorage
/*     */ {
/*     */   public static final String FIELDS_TABLE = "AutoSuggestFields";
/*     */   public static Map m_contextMap;
/*     */ 
/*     */   public static void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     m_contextMap = new ConcurrentHashMap();
/*  52 */     readContextStore();
/*     */ 
/*  56 */     DataResultSet defaultAutoSuggestFields = SharedObjects.getTable("AutoSuggestFields");
/*  57 */     processContextsResultset(defaultAutoSuggestFields);
/*     */ 
/*  63 */     String autoSuggestFields = SharedObjects.getEnvironmentValue("AutoSuggestFields");
/*  64 */     addFields(autoSuggestFields);
/*     */ 
/*  69 */     String disabledAutoSuggestFields = SharedObjects.getEnvironmentValue("DisabledAutoSuggestFields");
/*  70 */     disableFields(disabledAutoSuggestFields);
/*     */ 
/*  74 */     prepareContextInfo();
/*     */ 
/*  78 */     writeContextStore();
/*     */   }
/*     */ 
/*     */   public static void processContextsResultset(DataResultSet contextsResultset)
/*     */   {
/*     */     Map autoSuggestFieldsRow;
/*     */     String table;
/*  86 */     for (contextsResultset.first(); contextsResultset.isRowPresent(); contextsResultset.next())
/*     */     {
/*  88 */       autoSuggestFieldsRow = contextsResultset.getCurrentRowMap();
/*  89 */       table = contextsResultset.getStringValueByName("table");
/*  90 */       String fields = contextsResultset.getStringValueByName("fields");
/*  91 */       if ((fields == null) || (fields.length() == 0))
/*     */       {
/*  93 */         fields = contextsResultset.getStringValueByName("field");
/*     */       }
/*  95 */       List fieldList = StringUtils.makeListFromSequenceSimple(fields);
/*  96 */       for (String field : fieldList)
/*     */       {
/*  98 */         autoSuggestFieldsRow.put("field", field);
/*  99 */         String contextKey = getContextKey(table, field);
/*     */ 
/* 101 */         ContextInfo existingContextInfo = getContextInfo(contextKey);
/* 102 */         if (existingContextInfo != null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 106 */         ContextInfo contextInfo = new ContextInfo();
/* 107 */         contextInfo.init(autoSuggestFieldsRow);
/* 108 */         putContextInfo(contextKey, contextInfo);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void prepareContextInfo()
/*     */     throws DataException
/*     */   {
/* 120 */     Workspace systemWorkspace = WorkspaceUtils.getWorkspace("system");
/* 121 */     Workspace userWorkspace = WorkspaceUtils.getWorkspace("user");
/*     */ 
/* 123 */     Iterator iterator = getContextsIterator();
/* 124 */     while (iterator.hasNext())
/*     */     {
/* 126 */       String contextKey = (String)iterator.next();
/* 127 */       ContextInfo contextInfo = getContextInfo(contextKey);
/* 128 */       if (contextInfo != null)
/*     */       {
/* 130 */         Workspace contextWorkspace = systemWorkspace;
/* 131 */         FieldInfo contextFieldInfo = WorkspaceUtils.getColumnInfo(contextInfo.getTable(), contextInfo.getField(), contextWorkspace);
/* 132 */         if (contextFieldInfo == null)
/*     */         {
/* 134 */           contextWorkspace = userWorkspace;
/* 135 */           contextFieldInfo = WorkspaceUtils.getColumnInfo(contextInfo.getTable(), contextInfo.getField(), contextWorkspace);
/*     */         }
/* 137 */         if (contextFieldInfo == null)
/*     */         {
/* 140 */           Report.trace("autosuggest", "Context is disabled as field is not found in database." + contextInfo.toString(), null);
/* 141 */           contextInfo.disable();
/*     */         }
/*     */         else
/*     */         {
/* 145 */           contextInfo.m_contextWorkspace = contextWorkspace;
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void readContextStore()
/*     */     throws ServiceException
/*     */   {
/* 156 */     DataBinder contextStoreBinder = new DataBinder();
/* 157 */     ResourceUtils.serializeDataBinder(AutoSuggestConstants.AUTO_SUGGEST_DIR, AutoSuggestConstants.AUTO_SUGGEST_CONTEXTS_FILE, contextStoreBinder, false, false);
/* 158 */     DataResultSet contextsResultSet = (DataResultSet)contextStoreBinder.getResultSet("AutoSuggestFields");
/* 159 */     if (contextsResultSet == null)
/*     */       return;
/* 161 */     processContextsResultset(contextsResultSet);
/*     */   }
/*     */ 
/*     */   public static void writeContextStore()
/*     */     throws ServiceException
/*     */   {
/* 170 */     DataBinder contextStoreBinder = new DataBinder();
/* 171 */     Iterator iterator = getContextsIterator();
/* 172 */     DataResultSet contextsResultSet = new DataResultSet();
/* 173 */     List contextFields = ContextInfo.getFields();
/* 174 */     contextsResultSet.mergeFieldsWithFlags(contextFields, 0);
/* 175 */     while (iterator.hasNext())
/*     */     {
/* 177 */       String contextKey = (String)iterator.next();
/* 178 */       ContextInfo contextInfo = getContextInfo(contextKey);
/* 179 */       if (contextInfo == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 183 */       List contextFieldValues = contextInfo.getFieldValues();
/* 184 */       contextsResultSet.addRowWithList(contextFieldValues);
/*     */     }
/* 186 */     contextStoreBinder.addResultSet("AutoSuggestFields", contextsResultSet);
/* 187 */     ResourceUtils.serializeDataBinder(AutoSuggestConstants.AUTO_SUGGEST_DIR, AutoSuggestConstants.AUTO_SUGGEST_CONTEXTS_FILE, contextStoreBinder, true, false);
/*     */   }
/*     */ 
/*     */   public static boolean isContextEnabled(String contextKey)
/*     */   {
/* 196 */     ContextInfo contextInfo = getContextInfo(contextKey);
/*     */ 
/* 199 */     return (contextInfo != null) && (!contextInfo.isDisabled());
/*     */   }
/*     */ 
/*     */   public static boolean addFields(String autoSuggestFields)
/*     */   {
/* 211 */     boolean isSuccess = false;
/* 212 */     List autoSuggestFieldsList = StringUtils.makeListFromSequence(autoSuggestFields, '|', '|', 0);
/* 213 */     for (String autoSuggestField : autoSuggestFieldsList)
/*     */     {
/* 215 */       Properties fieldProps = new Properties();
/* 216 */       StringUtils.parsePropertiesEx(fieldProps, autoSuggestField, ':', '\\', '=');
/* 217 */       isSuccess = addFields(fieldProps);
/*     */     }
/* 219 */     return isSuccess;
/*     */   }
/*     */ 
/*     */   public static boolean addFields(DataBinder binder)
/*     */   {
/* 229 */     return addFields(binder.getLocalData());
/*     */   }
/*     */ 
/*     */   public static boolean addFields(Properties contextProperties)
/*     */   {
/* 239 */     String fields = contextProperties.getProperty("fields");
/* 240 */     if ((fields == null) || (fields.length() == 0))
/*     */     {
/* 242 */       fields = contextProperties.getProperty("field");
/*     */     }
/* 244 */     String table = contextProperties.getProperty("table");
/* 245 */     if ((fields == null) || (fields.length() == 0) || (table == null) || (table.length() == 0))
/*     */     {
/* 247 */       return false;
/*     */     }
/* 249 */     List fieldList = StringUtils.makeListFromSequenceSimple(fields);
/* 250 */     for (String field : fieldList)
/*     */     {
/* 252 */       contextProperties.put("field", field);
/* 253 */       String contextKey = getContextKey(table, field);
/* 254 */       ContextInfo contextInfo = new ContextInfo();
/* 255 */       contextInfo.init(contextProperties);
/* 256 */       putContextInfo(contextKey, contextInfo);
/*     */     }
/* 258 */     return true;
/*     */   }
/*     */ 
/*     */   public static String getDefaultSecurityContext(String table)
/*     */   {
/* 267 */     String defaultSecurityContext = null;
/* 268 */     DataResultSet defaultAutoSuggestFields = SharedObjects.getTable("AutoSuggestFields");
/* 269 */     FieldInfo tableFieldInfo = new FieldInfo();
/* 270 */     defaultAutoSuggestFields.getFieldInfo("table", tableFieldInfo);
/* 271 */     FieldInfo fieldsFieldInfo = new FieldInfo();
/* 272 */     defaultAutoSuggestFields.getFieldInfo("fields", fieldsFieldInfo);
/* 273 */     FieldInfo isSecurityContextFieldInfo = new FieldInfo();
/* 274 */     defaultAutoSuggestFields.getFieldInfo("isSecurityContext", isSecurityContextFieldInfo);
/*     */ 
/* 276 */     for (defaultAutoSuggestFields.first(); defaultAutoSuggestFields.isRowPresent(); defaultAutoSuggestFields.next())
/*     */     {
/* 278 */       String candidateTable = defaultAutoSuggestFields.getStringValue(tableFieldInfo.m_index);
/* 279 */       String isSecurityContextString = defaultAutoSuggestFields.getStringValue(isSecurityContextFieldInfo.m_index);
/* 280 */       String field = defaultAutoSuggestFields.getStringValue(fieldsFieldInfo.m_index);
/* 281 */       boolean isSecurityContext = StringUtils.convertToBool(isSecurityContextString, false);
/* 282 */       if ((!candidateTable.equalsIgnoreCase(table)) || (!isSecurityContext))
/*     */         continue;
/* 284 */       defaultSecurityContext = candidateTable + "." + field;
/* 285 */       break;
/*     */     }
/*     */ 
/* 288 */     return defaultSecurityContext;
/*     */   }
/*     */ 
/*     */   public static void disableFields(String disabledAutoSuggestFields)
/*     */   {
/* 298 */     List disabledAutoSuggestFieldsList = StringUtils.makeListFromSequence(disabledAutoSuggestFields, '|', '|', 0);
/* 299 */     for (String disabledAutoSuggestField : disabledAutoSuggestFieldsList)
/*     */     {
/* 301 */       Properties fieldProps = new Properties();
/* 302 */       StringUtils.parsePropertiesEx(fieldProps, disabledAutoSuggestField, ':', '\\', '=');
/* 303 */       String table = fieldProps.getProperty("table");
/* 304 */       String fields = fieldProps.getProperty("fields");
/* 305 */       disableFields(fields, table);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void disableFields(String fields, String table)
/*     */   {
/* 315 */     List fieldList = StringUtils.makeListFromSequenceSimple(fields);
/* 316 */     for (String field : fieldList)
/*     */     {
/* 318 */       String contextKey = getContextKey(table, field);
/* 319 */       ContextInfo contextInfo = getContextInfo(contextKey);
/* 320 */       if (contextInfo != null)
/*     */       {
/* 322 */         contextInfo.disable();
/* 323 */         putContextInfo(contextKey, contextInfo);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void enableFields(String fields, String table)
/*     */   {
/* 334 */     List fieldList = StringUtils.makeListFromSequenceSimple(fields);
/* 335 */     for (String field : fieldList)
/*     */     {
/* 337 */       String contextKey = getContextKey(table, field);
/* 338 */       ContextInfo contextInfo = getContextInfo(contextKey);
/* 339 */       if (contextInfo != null)
/*     */       {
/* 341 */         contextInfo.enable();
/* 342 */         putContextInfo(contextKey, contextInfo);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Iterator getContextsIterator()
/*     */   {
/* 352 */     if (m_contextMap == null)
/*     */     {
/* 354 */       return null;
/*     */     }
/* 356 */     Report.trace("autosuggest", "Context storage contains " + m_contextMap.keySet().size() + " entries", null);
/* 357 */     Iterator contextsIterator = m_contextMap.keySet().iterator();
/* 358 */     return contextsIterator;
/*     */   }
/*     */ 
/*     */   public static String getAddDataSource(String contextKey)
/*     */   {
/* 367 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 368 */     return contextInfo.m_addDataSource;
/*     */   }
/*     */ 
/*     */   public static String getDeleteDataSource(String contextKey)
/*     */   {
/* 379 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 380 */     return contextInfo.m_deleteDataSource;
/*     */   }
/*     */ 
/*     */   public static String getInitDataSource(String contextKey)
/*     */   {
/* 392 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 393 */     return contextInfo.m_initDataSource;
/*     */   }
/*     */ 
/*     */   public static String getTable(String contextKey)
/*     */   {
/* 402 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 403 */     return contextInfo.m_table;
/*     */   }
/*     */ 
/*     */   public static String getField(String contextKey) {
/* 407 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 408 */     return contextInfo.m_field;
/*     */   }
/*     */ 
/*     */   public static String getExtraParameters(String contextKey) {
/* 412 */     ContextInfo contextInfo = getContextInfo(contextKey);
/* 413 */     return contextInfo.m_extraParameters;
/*     */   }
/*     */ 
/*     */   public static String getContextKey(String table, String fieldName)
/*     */   {
/* 422 */     return table + "." + fieldName;
/*     */   }
/*     */ 
/*     */   public static ContextInfo getContextInfo(String table, String fieldName) {
/* 426 */     String contextKey = getContextKey(table, fieldName);
/* 427 */     return getContextInfo(contextKey);
/*     */   }
/*     */ 
/*     */   public static ContextInfo getContextInfo(String contextKey) {
/* 431 */     if (m_contextMap == null)
/*     */     {
/* 433 */       return null;
/*     */     }
/*     */ 
/* 436 */     contextKey = contextKey.toLowerCase();
/* 437 */     ContextInfo contextInfo = (ContextInfo)m_contextMap.get(contextKey);
/*     */ 
/* 439 */     if (contextInfo == null)
/*     */     {
/* 441 */       int indexOfDot = contextKey.indexOf(".");
/* 442 */       while ((contextInfo == null) && (indexOfDot > 0))
/*     */       {
/* 444 */         contextKey = contextKey.substring(indexOfDot + 1);
/* 445 */         contextInfo = (ContextInfo)m_contextMap.get(contextKey);
/* 446 */         indexOfDot = contextKey.indexOf(".");
/*     */       }
/*     */     }
/* 449 */     return contextInfo;
/*     */   }
/*     */ 
/*     */   public static int getNumberOfContexts() {
/* 453 */     if (m_contextMap == null)
/*     */     {
/* 455 */       return 0;
/*     */     }
/* 457 */     return m_contextMap.keySet().size();
/*     */   }
/*     */ 
/*     */   public static void putContextInfo(String contextKey, ContextInfo contextInfo)
/*     */   {
/* 462 */     contextKey = contextKey.toLowerCase();
/* 463 */     m_contextMap.put(contextKey, contextInfo);
/* 464 */     Report.trace("autosuggest", "Updating context - " + contextKey + " - " + contextInfo.toString(), null);
/* 465 */     Report.trace("autosuggest", "Context storage contains " + m_contextMap.keySet().size() + " entries", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 469 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103581 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.datastore.ContextInfoStorage
 * JD-Core Version:    0.5.4
 */