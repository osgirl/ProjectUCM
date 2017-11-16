/*      */ package intradoc.shared;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.data.WorkspaceUtils;
/*      */ import intradoc.server.DocProfileManager;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceManager;
/*      */ import intradoc.server.SubjectManager;
/*      */ import intradoc.server.UserStorage;
/*      */ import intradoc.server.schema.SchemaManager;
/*      */ import intradoc.server.schema.SchemaStorage;
/*      */ import intradoc.server.schema.ServerSchemaManager;
/*      */ import intradoc.shared.schema.SchemaHelper;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class MetaFieldUtils
/*      */ {
/*   58 */   protected static final String[] m_fieldTypes = { "Text", "BigText", "Date", "Memo", "Int", "Decimal" };
/*      */   public static final int F_USE_DEFAULT = 0;
/*      */   public static final int F_ALLOW_MISSING = 1;
/*      */   public static final int UMD_HIGH_BIT_VALUE = 536870912;
/*      */   public static final int UMD_LOW_BIT_VALUE = 16;
/*      */ 
/*      */   public static String[] getMetaFieldTypes()
/*      */   {
/*   65 */     return m_fieldTypes;
/*      */   }
/*      */ 
/*      */   public static FieldInfo createFieldInfo(String name, String type)
/*      */   {
/*   70 */     return createFieldInfo(name, type, null);
/*      */   }
/*      */ 
/*      */   public static FieldInfo createFieldInfo(String name, String type, String decimalScale)
/*      */   {
/*   75 */     FieldInfo finfo = new FieldInfo();
/*   76 */     finfo.m_name = name;
/*   77 */     if (type.equalsIgnoreCase("Date"))
/*      */     {
/*   79 */       finfo.m_type = 5;
/*      */     }
/*   81 */     else if (type.equalsIgnoreCase("Text"))
/*      */     {
/*   83 */       finfo.m_isFixedLen = true;
/*   84 */       finfo.m_maxLen = 30;
/*      */     }
/*   86 */     else if (type.equalsIgnoreCase("BigText"))
/*      */     {
/*   88 */       finfo.m_isFixedLen = true;
/*   89 */       finfo.m_maxLen = 200;
/*      */     }
/*   91 */     else if (type.equalsIgnoreCase("Int"))
/*      */     {
/*   93 */       finfo.m_isFixedLen = true;
/*   94 */       finfo.m_maxLen = 15;
/*   95 */       finfo.m_type = 3;
/*      */     }
/*   97 */     else if (type.equalsIgnoreCase("Decimal"))
/*      */     {
/*   99 */       finfo.m_isFixedLen = true;
/*  100 */       finfo.m_maxLen = SharedObjects.getEnvironmentInt("NumDecimalScale", 31);
/*  101 */       finfo.m_scale = NumberUtils.parseInteger(decimalScale, 2);
/*  102 */       finfo.m_type = 11;
/*      */     }
/*  104 */     return finfo;
/*      */   }
/*      */ 
/*      */   public static boolean isTextField(String type)
/*      */   {
/*  109 */     return (type.equalsIgnoreCase("Text")) || (type.equalsIgnoreCase("BigText"));
/*      */   }
/*      */ 
/*      */   public static boolean allowOptionList(String type)
/*      */   {
/*  117 */     return (isTextField(type)) || (type.equalsIgnoreCase("Int")) || (type.equalsIgnoreCase("Memo"));
/*      */   }
/*      */ 
/*      */   public static boolean allowDecimalField(String type)
/*      */   {
/*  125 */     return type.equalsIgnoreCase("Decimal");
/*      */   }
/*      */ 
/*      */   public static boolean createDiffList(ResultSet metaResultSet, DataResultSet data, Vector add, Vector change, Vector delete)
/*      */   {
/*  139 */     int i = 0;
/*  140 */     int nameIndex = -1;
/*  141 */     int typeIndex = -1;
/*  142 */     int numRows = data.getNumRows();
/*  143 */     int numMetaFields = metaResultSet.getNumFields();
/*  144 */     boolean changesNeeded = false;
/*      */ 
/*  146 */     FieldInfo finfo = new FieldInfo();
/*  147 */     Hashtable found = new Hashtable(numMetaFields);
/*  148 */     for (i = 0; i < numMetaFields; ++i)
/*      */     {
/*  150 */       found.put(metaResultSet.getFieldName(i), "0");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  156 */       nameIndex = ResultSetUtils.getIndexMustExist(data, "umdName");
/*  157 */       typeIndex = ResultSetUtils.getIndexMustExist(data, "umdType");
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  161 */       Report.trace(null, null, e);
/*      */     }
/*      */ 
/*  165 */     for (i = 0; i < numRows; ++i)
/*      */     {
/*  167 */       Vector v = data.getRowValues(i);
/*  168 */       String name = (String)v.elementAt(nameIndex);
/*  169 */       String type = (String)v.elementAt(typeIndex);
/*  170 */       if (metaResultSet.getFieldInfo(name, finfo))
/*      */       {
/*  172 */         String metaType = "Illegal";
/*  173 */         if (finfo.m_type == 3)
/*      */         {
/*  175 */           metaType = "Int";
/*      */         }
/*  177 */         else if (finfo.m_type == 5)
/*      */         {
/*  179 */           metaType = "Date";
/*      */         }
/*  181 */         else if (finfo.m_type == 6)
/*      */         {
/*  183 */           int maxlen = SharedObjects.getEnvironmentInt("MinMemoFieldSize", 255);
/*  184 */           if ((finfo.m_isFixedLen) && (finfo.m_maxLen < maxlen))
/*      */           {
/*  186 */             if (finfo.m_maxLen >= 50)
/*      */             {
/*  188 */               metaType = "BigText";
/*      */             }
/*      */             else
/*      */             {
/*  192 */               metaType = "Text";
/*      */             }
/*      */ 
/*      */           }
/*      */           else {
/*  197 */             metaType = "Memo";
/*      */           }
/*      */         }
/*  200 */         else if (finfo.m_type == 11)
/*      */         {
/*  202 */           metaType = "Decimal";
/*      */         }
/*      */ 
/*  205 */         if (!type.equalsIgnoreCase(metaType))
/*      */         {
/*  207 */           FieldInfo fi = createFieldInfo(name, type);
/*  208 */           change.addElement(fi);
/*  209 */           changesNeeded = true;
/*      */         }
/*      */ 
/*  212 */         found.put(name, "1");
/*      */       }
/*      */       else
/*      */       {
/*  216 */         FieldInfo fi = createFieldInfo(name, type);
/*  217 */         add.addElement(fi);
/*  218 */         changesNeeded = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  223 */     for (i = 0; i < numMetaFields; ++i)
/*      */     {
/*  225 */       String fValue = (String)found.get(metaResultSet.getFieldName(i));
/*  226 */       if (StringUtils.convertToBool(fValue, false))
/*      */         continue;
/*  228 */       FieldInfo delInfo = new FieldInfo();
/*  229 */       metaResultSet.getIndexFieldInfo(i, delInfo);
/*  230 */       if (delInfo.m_name.equals("dID"))
/*      */         continue;
/*  232 */       delete.addElement(delInfo);
/*  233 */       changesNeeded = true;
/*      */     }
/*      */ 
/*  238 */     return changesNeeded;
/*      */   }
/*      */ 
/*      */   public static void getOptionList(DataBinder data, String tableName) throws DataException, ServiceException
/*      */   {
/*  243 */     getOptionList(data, tableName, false);
/*      */   }
/*      */ 
/*      */   public static void getOptionList(DataBinder data, String tableName, boolean includeViews)
/*      */     throws DataException, ServiceException
/*      */   {
/*  249 */     String keyName = "dIsOptionList";
/*  250 */     ResultSet metaDefs = SharedObjects.getTable("DocMetaDefinition");
/*      */ 
/*  252 */     int optionKeyIndex = ResultSetUtils.getIndexMustExist(metaDefs, "dOptionListKey");
/*  253 */     boolean includeViewsInFilter = includeViews;
/*      */ 
/*  255 */     ResultSetFilter filter = new ResultSetFilter(optionKeyIndex, includeViewsInFilter)
/*      */     {
/*      */       public int checkRow(String val, int curNumRows, Vector row)
/*      */       {
/*  259 */         int result = 0;
/*  260 */         boolean isOptionList = StringUtils.convertToBool(val, false);
/*  261 */         if (isOptionList)
/*      */         {
/*  263 */           String key = (String)row.elementAt(this.val$optionKeyIndex);
/*  264 */           if ((key.startsWith("view://")) && 
/*  266 */             (this.val$includeViewsInFilter))
/*      */           {
/*  268 */             result = 1;
/*      */           }
/*      */         }
/*      */ 
/*  272 */         return result;
/*      */       }
/*      */     };
/*  276 */     DataResultSet rset = new DataResultSet();
/*  277 */     rset.copyFiltered(metaDefs, keyName, filter);
/*  278 */     data.addResultSet(tableName, rset);
/*      */ 
/*  281 */     String[][] systemLists = { { "dSecurityGroup", "SystemGroup", "securityGroups" }, { "dDocType", "SystemTypes", "docTypes" }, { "dDocAuthor", "SystemAuthors", "docAuthors" }, { "roles", "SystemRoles", "roles" } };
/*      */ 
/*  290 */     DataBinder binder = new DataBinder();
/*  291 */     binder.putLocal("dType", "Text");
/*  292 */     binder.putLocal("dIsRequired", "1");
/*  293 */     binder.putLocal("dIsEnabled", "1");
/*  294 */     binder.putLocal("dIsSearchable", "1");
/*  295 */     binder.putLocal("dIsOptionList", "1");
/*  296 */     binder.putLocal("dDefaultValue", "");
/*  297 */     binder.putLocal("dOrder", "0");
/*  298 */     binder.putLocal("dOptionListType", "choice");
/*  299 */     binder.putLocal("dIsPlaceholderField", "0");
/*  300 */     binder.putLocal("dDecimalScale", "");
/*  301 */     binder.putLocal("dCategory", "");
/*  302 */     binder.putLocal("dExtraDefinition", "");
/*  303 */     binder.putLocal("dComponentName", "");
/*  304 */     binder.putLocal("dDocMetaSet", "");
/*      */ 
/*  306 */     for (int i = 0; i < systemLists.length; ++i)
/*      */     {
/*  308 */       binder.putLocal("dName", systemLists[i][0]);
/*  309 */       binder.putLocal("dCaption", systemLists[i][1]);
/*  310 */       binder.putLocal("dOptionListKey", systemLists[i][2]);
/*      */ 
/*  312 */       Vector row = rset.createRow(binder);
/*  313 */       rset.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String determineViewName(String fieldName)
/*      */   {
/*  319 */     String viewName = "";
/*      */ 
/*  322 */     DataBinder binder = new DataBinder();
/*      */     try
/*      */     {
/*  325 */       getOptionList(binder, "optLists", true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  329 */       Report.trace("search", null, e);
/*  330 */       return viewName;
/*      */     }
/*      */ 
/*  333 */     DataResultSet optListSet = (DataResultSet)binder.getResultSet("optLists");
/*  334 */     Vector row = optListSet.findRow(0, fieldName);
/*  335 */     if (row != null)
/*      */     {
/*  337 */       String rawViewName = (String)row.get(optListSet.getFieldInfoIndex("dOptionListKey"));
/*      */ 
/*  340 */       if (rawViewName.startsWith("view://"))
/*      */       {
/*  344 */         viewName = rawViewName.substring(7);
/*      */       }
/*      */       else
/*      */       {
/*  348 */         viewName = rawViewName;
/*      */       }
/*      */     }
/*      */ 
/*  352 */     return viewName;
/*      */   }
/*      */ 
/*      */   public static void validateMetaDataUpdate(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  369 */     boolean isOptionList = StringUtils.convertToBool(binder.getLocal("dIsOptionList"), false);
/*  370 */     if (isOptionList)
/*      */     {
/*  375 */       String key = null;
/*  376 */       boolean useList = StringUtils.convertToBool(binder.getLocal("UseOptionList"), false);
/*  377 */       boolean useView = StringUtils.convertToBool(binder.getLocal("UseViewList"), false);
/*  378 */       boolean useTree = StringUtils.convertToBool(binder.getLocal("UseTreeControl"), false);
/*  379 */       if (useList)
/*      */       {
/*  381 */         key = binder.getLocal("OptionListKey");
/*      */       }
/*  383 */       else if (useView)
/*      */       {
/*  385 */         key = binder.getLocal("OptionViewKey");
/*  386 */         key = SchemaHelper.VIEW_PREFIX + key;
/*      */       }
/*  388 */       else if (useTree)
/*      */       {
/*  390 */         key = binder.getLocal("TreeDefinition");
/*  391 */         if (!key.startsWith(SchemaHelper.TREE_PREFIX))
/*      */         {
/*  395 */           key = SchemaHelper.TREE_PREFIX;
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  401 */         key = binder.getLocal("dOptionListKey");
/*      */       }
/*  403 */       if (key != null)
/*      */       {
/*  405 */         binder.putLocal("dOptionListKey", key);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  411 */       binder.putLocal("dOptionListKey", "");
/*      */     }
/*      */ 
/*  416 */     String triggerField = DocProfileManager.getTriggerField();
/*  417 */     String name = binder.getLocal("dName");
/*  418 */     if ((triggerField != null) && (triggerField.equalsIgnoreCase(name)) && 
/*  420 */       (!isOptionList))
/*      */     {
/*  422 */       String msg = LocaleUtils.encodeMessage("csMetaDpTriggerMustBeOption", null, name);
/*  423 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  428 */     if ((name.equals("xIdcProfile")) && 
/*  430 */       (!SharedObjects.getEnvValueAsBoolean("AllowModifyProfileDocMetaField", false)))
/*      */     {
/*  432 */       String msg = LocaleUtils.encodeMessage("csNotAllowedToModifyField", null, name);
/*  433 */       throw new ServiceException(msg);
/*      */     }
/*      */ 
/*  438 */     String[][] validFields = TableFields.METAFIELD_TYPES_OPTIONSLIST;
/*  439 */     String type = binder.getLocal("dType");
/*  440 */     if (type != null)
/*      */     {
/*  442 */       boolean foundIt = false;
/*  443 */       for (int i = 0; i < validFields.length; ++i)
/*      */       {
/*  445 */         String compareType = validFields[i][0];
/*  446 */         if (!type.equalsIgnoreCase(compareType))
/*      */           continue;
/*  448 */         type = compareType;
/*  449 */         foundIt = true;
/*      */       }
/*      */ 
/*  454 */       if ((!foundIt) && (type.equalsIgnoreCase("decimal")))
/*      */       {
/*  456 */         type = "Decimal";
/*  457 */         foundIt = true;
/*      */       }
/*  459 */       if (!foundIt)
/*      */       {
/*  461 */         String msg = LocaleUtils.encodeMessage("csMetaDefinitionFieldTypeInvalid", null, name, type);
/*  462 */         throw new DataException(msg);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  467 */     String docMetaSet = binder.getLocal("dDocMetaSet");
/*  468 */     if ((docMetaSet == null) || (docMetaSet.length() <= 0) || 
/*  471 */       (WorkspaceUtils.doesTableExist(ws, docMetaSet, null)))
/*      */       return;
/*  473 */     String msg = LocaleUtils.encodeMessage("csDMSTableDoesNotExists", null, docMetaSet);
/*      */ 
/*  475 */     throw new DataException(msg);
/*      */   }
/*      */ 
/*      */   public static void addDocMetaSchemaField(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  493 */     String name = binder.get("dName");
/*  494 */     binder.putLocal("IsDocMetaField", "true");
/*  495 */     binder.putLocal("schFieldName", name);
/*      */ 
/*  497 */     ServerSchemaManager manager = SchemaManager.getManager(ws);
/*  498 */     manager.getStorageImplementor("SchemaFieldConfig").createOrUpdateAutoDetect(name, binder, false);
/*  499 */     SharedObjects.tableChanged("SchemaFieldConfig");
/*  500 */     SubjectManager.notifyChanged("schema");
/*      */   }
/*      */ 
/*      */   public static boolean hasDocMetaDef(String metaDefName)
/*      */   {
/*  511 */     DataResultSet docMetaDefs = SharedObjects.getTable("DocMetaDefinition");
/*      */ 
/*  513 */     if (docMetaDefs.isEmpty())
/*      */     {
/*  515 */       return false;
/*      */     }
/*      */ 
/*  520 */     return docMetaDefs.findRow(0, metaDefName) != null;
/*      */   }
/*      */ 
/*      */   public static void addMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order)
/*      */     throws ServiceException, DataException
/*      */   {
/*  550 */     updateMetaData(ws, cxt, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, true);
/*      */   }
/*      */ 
/*      */   public static void addMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String decimalPrecision, String decimalScale, String order)
/*      */     throws ServiceException, DataException
/*      */   {
/*  579 */     updateMetaData(ws, cxt, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, decimalScale, order, true);
/*      */   }
/*      */ 
/*      */   public static void hideMetaData(Workspace ws, ExecutionContext cxt, String name)
/*      */     throws ServiceException, DataException
/*      */   {
/*  593 */     DataResultSet docMetaDefs = SharedObjects.getTable("DocMetaDefinition");
/*  594 */     FieldInfo[] fi = ResultSetUtils.createInfoList(docMetaDefs, new String[] { "dName", "dCaption", "dType", "dIsRequired", "dIsEnabled", "dIsSearchable", "dIsOptionList", "dDefaultValue", "dOptionListKey", "dOptionListType", "dOrder" }, true);
/*      */ 
/*  598 */     if (docMetaDefs.isEmpty())
/*      */       return;
/*  600 */     DataBinder binder = new DataBinder();
/*  601 */     Vector v = docMetaDefs.findRow(fi[0].m_index, name);
/*  602 */     if (v == null)
/*      */       return;
/*  604 */     for (int i = 0; i < fi.length; ++i)
/*      */     {
/*  606 */       String key = fi[i].m_name;
/*  607 */       String val = docMetaDefs.getStringValue(fi[i].m_index);
/*      */ 
/*  609 */       if ((key.equals("dIsEnabled")) || (key.equals("dIsRequired")))
/*      */       {
/*  611 */         val = "0";
/*      */       }
/*  613 */       binder.putLocal(fi[i].m_name, val);
/*      */     }
/*  615 */     executeService(ws, cxt, binder, "EDIT_METADEF");
/*  616 */     executeService(ws, cxt, binder, "UPDATE_META_TABLE");
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void editMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order)
/*      */     throws ServiceException, DataException
/*      */   {
/*  630 */     updateMetaData(ws, cxt, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, true);
/*      */   }
/*      */ 
/*      */   public static void editMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String decimalScale, String order)
/*      */     throws ServiceException, DataException
/*      */   {
/*  660 */     updateMetaData(ws, cxt, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, decimalScale, order, true);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public static void updateMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String order, boolean isNew)
/*      */     throws ServiceException, DataException
/*      */   {
/*  692 */     updateMetaData(ws, cxt, name, caption, type, isRequired, isEnabled, isSearchable, isOptionList, defaultValue, optionListKey, optionListType, "", order, isNew);
/*      */   }
/*      */ 
/*      */   public static void updateMetaData(Workspace ws, ExecutionContext cxt, String name, String caption, String type, String isRequired, String isEnabled, String isSearchable, String isOptionList, String defaultValue, String optionListKey, String optionListType, String decimalScale, String order, boolean isNew)
/*      */     throws ServiceException, DataException
/*      */   {
/*  722 */     Properties props = new Properties();
/*  723 */     props.put("dCaption", caption);
/*  724 */     props.put("dType", type);
/*  725 */     props.put("dIsRequired", isRequired);
/*  726 */     props.put("dIsEnabled", isEnabled);
/*  727 */     props.put("dIsSearchable", isSearchable);
/*  728 */     props.put("dIsOptionList", isOptionList);
/*  729 */     props.put("dDefaultValue", defaultValue);
/*  730 */     props.put("dOptionListKey", optionListKey);
/*  731 */     props.put("dOptionListType", optionListType);
/*  732 */     props.put("dDecimalScale", decimalScale);
/*  733 */     if (order != null)
/*      */     {
/*  735 */       props.put("dOrder", order);
/*      */     }
/*  737 */     updateMetaDataFromProps(ws, cxt, props, name, isNew);
/*      */   }
/*      */ 
/*      */   public static void updateMetaDataFromProps(Workspace ws, ExecutionContext cxt, Properties props, String name, boolean isNew)
/*      */     throws ServiceException, DataException
/*      */   {
/*  755 */     if (null != name)
/*      */     {
/*  757 */       props.put("dName", name);
/*      */     }
/*  759 */     props.put("isNewMetaDataField", (isNew) ? "1" : "0");
/*  760 */     DataBinder binder = new DataBinder();
/*  761 */     binder.setLocalData(props);
/*  762 */     updateMetaDataFromBinder(ws, binder, cxt);
/*      */   }
/*      */ 
/*      */   public static void updateMetaDataFromBinder(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  778 */     String order = binder.getLocal("dOrder");
/*  779 */     if ((order == null) || (order.trim().length() == 0))
/*      */     {
/*  781 */       ResultSet rset = ws.createResultSetSQL("SELECT (MAX(dOrder) + 1)  as dOrder From DocMetaDefinition");
/*  782 */       order = rset.getStringValueByName("dOrder");
/*  783 */       binder.putLocal("dOrder", order);
/*      */     }
/*      */ 
/*  786 */     boolean didUpdateDocMetaDef = updateDocMetaDefinitionTable(ws, binder, cxt);
/*  787 */     boolean didUpdateDocMeta = updateDocMetaTable(ws, binder, cxt);
/*      */ 
/*  790 */     boolean doUpdateSchema = (checkIfSchemaLoaded()) && (((didUpdateDocMetaDef) || (didUpdateDocMeta)));
/*  791 */     String noUpdateString = binder.getAllowMissing("noUpdateSchema");
/*  792 */     if ((!doUpdateSchema) || (StringUtils.convertToBool(noUpdateString, false)) || (!SubjectManager.hasSubject("dynamicqueries"))) {
/*      */       return;
/*      */     }
/*  795 */     if (null == cxt)
/*      */     {
/*  797 */       cxt = new ExecutionContextAdaptor();
/*      */     }
/*  799 */     SubjectManager.refreshSubjectAll("dynamicqueries", binder, cxt);
/*      */   }
/*      */ 
/*      */   protected static boolean checkIfSchemaLoaded()
/*      */   {
/*  808 */     DataResultSet schemaViewConfig = SharedObjects.getTable("SchemaViewConfig");
/*  809 */     return null != schemaViewConfig;
/*      */   }
/*      */ 
/*      */   public static boolean updateDocMetaDefinitionTable(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  825 */     String noUpdateString = binder.getAllowMissing("noUpdateDocMetaDefinition");
/*  826 */     boolean doUpdate = !StringUtils.convertToBool(noUpdateString, false);
/*  827 */     if (!doUpdate)
/*      */     {
/*  829 */       return false;
/*      */     }
/*      */ 
/*  832 */     if (null == cxt)
/*      */     {
/*  834 */       cxt = new ExecutionContextAdaptor();
/*      */     }
/*      */ 
/*  837 */     String isNewString = binder.getAllowMissing("isNewMetaDataField");
/*  838 */     boolean isNew = StringUtils.convertToBool(isNewString, true);
/*      */ 
/*  840 */     if (isNew)
/*      */     {
/*  842 */       ResultSet rset = ws.createResultSet("Qmetadef", binder);
/*  843 */       if (!rset.isEmpty())
/*      */       {
/*  846 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*  850 */     FieldInfo[] fields = ws.getColumnList("DocMetaDefinition");
/*  851 */     Properties oldProps = null;
/*  852 */     for (FieldInfo field : fields)
/*      */     {
/*  855 */       String key = field.m_name;
/*  856 */       String value = binder.getAllowMissing(key);
/*  857 */       if (value != null)
/*      */         continue;
/*  859 */       if (oldProps == null)
/*      */       {
/*  861 */         oldProps = binder.getLocalData();
/*  862 */         binder.setLocalData(new Properties(oldProps));
/*      */       }
/*  864 */       binder.putLocal(key, "");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  870 */       if (isNew)
/*      */       {
/*  872 */         ws.execute("Imetadef", binder);
/*      */       }
/*      */       else
/*      */       {
/*  876 */         executeService(ws, cxt, binder, "EDIT_METADEF");
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  881 */       if (oldProps != null)
/*      */       {
/*  883 */         binder.setLocalData(oldProps);
/*      */       }
/*      */     }
/*      */ 
/*  887 */     if (isNew)
/*      */     {
/*  889 */       boolean isSchemaLoaded = checkIfSchemaLoaded();
/*  890 */       noUpdateString = binder.getAllowMissing("noUpdateSchema");
/*  891 */       if ((isSchemaLoaded) && (!StringUtils.convertToBool(noUpdateString, false)))
/*      */       {
/*  893 */         addDocMetaSchemaField(ws, binder, cxt);
/*  894 */         SubjectManager.refreshSubjectAll("metadata", binder, cxt);
/*      */       }
/*      */     }
/*      */ 
/*  898 */     PluginFilters.filter("updateDocMetaDefinitionTable", ws, binder, cxt);
/*  899 */     return true;
/*      */   }
/*      */ 
/*      */   public static boolean updateDocMetaTable(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/*  915 */     String noUpdateString = binder.getAllowMissing("noUpdateDocMeta");
/*  916 */     boolean doUpdate = !StringUtils.convertToBool(noUpdateString, false);
/*  917 */     if (!doUpdate)
/*      */     {
/*  919 */       return false;
/*      */     }
/*      */ 
/*  922 */     boolean isPlaceholderField = StringUtils.convertToBool(binder.getLocal("dIsPlaceholderField"), false);
/*  923 */     if (isPlaceholderField)
/*      */     {
/*  925 */       return false;
/*      */     }
/*      */ 
/*  929 */     String dDocMetaSet = binder.getAllowMissing("dDocMetaSet");
/*  930 */     if ((dDocMetaSet == null) || (dDocMetaSet.length() == 0))
/*      */     {
/*  932 */       dDocMetaSet = "DocMeta";
/*      */     }
/*  934 */     String dName = binder.get("dName");
/*  935 */     String dType = binder.get("dType");
/*      */     FieldInfo thisField;
/*      */     FieldInfo thisField;
/*  937 */     if ("decimal".equalsIgnoreCase(dType))
/*      */     {
/*  939 */       String dDecimalScale = binder.get("dDecimalScale");
/*  940 */       thisField = createFieldInfo(dName, dType, dDecimalScale);
/*      */     }
/*      */     else
/*      */     {
/*  945 */       thisField = createFieldInfo(dName, dType);
/*      */     }
/*      */ 
/*  948 */     String isNewString = binder.getAllowMissing("isNewMetaDataField");
/*  949 */     boolean isNew = StringUtils.convertToBool(isNewString, true);
/*  950 */     dName = thisField.m_name.toLowerCase();
/*  951 */     FieldInfo dbField = null;
/*  952 */     FieldInfo[] fields = ws.getColumnList(dDocMetaSet);
/*  953 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/*  955 */       String fieldName = fields[i].m_name.toLowerCase();
/*  956 */       if (!fieldName.equals(dName)) {
/*      */         continue;
/*      */       }
/*  959 */       dbField = fields[i];
/*  960 */       break;
/*      */     }
/*      */ 
/*  963 */     if ((isNew) && (null != dbField))
/*      */     {
/*  965 */       return false;
/*      */     }
/*  967 */     dName = binder.get("dName");
/*  968 */     if (!isNew)
/*      */     {
/*  970 */       if (null == dbField)
/*      */       {
/*  972 */         String msg = "WARNING: tried to update metadata field '" + dName + "' (in table '" + dDocMetaSet + "') which does not exist, adding.";
/*  973 */         Report.trace(null, msg, null);
/*      */       }
/*      */       else
/*      */       {
/*  977 */         if (thisField.m_type != dbField.m_type)
/*      */         {
/*  979 */           String msg = "ERROR: tried to change type for metadata field '" + dName + "' (in table '" + dDocMetaSet + "')!";
/*  980 */           Report.trace(null, msg, null);
/*  981 */           return false;
/*      */         }
/*  983 */         if (dbField.m_type != 6)
/*      */         {
/*  985 */           if (SystemUtils.m_verbose)
/*      */           {
/*  987 */             String msg = "INFO: update metadata field '" + dName + "' (in table '" + dDocMetaSet + "') skipped (non-string)";
/*  988 */             Report.debug(null, msg, null);
/*      */           }
/*  990 */           return false;
/*      */         }
/*  992 */         if (thisField.m_maxLen <= dbField.m_maxLen)
/*      */         {
/*  995 */           if (SystemUtils.m_verbose)
/*      */           {
/*  997 */             String msg = "INFO: size sufficient for metadata field '" + dName + "' (in table '" + dDocMetaSet + "').";
/*  998 */             Report.debug(null, msg, null);
/*      */           }
/* 1000 */           return false;
/*      */         }
/*      */ 
/* 1003 */         String msg = "INFO: increasing size of metadata field '" + dName + "' (in table '" + dDocMetaSet + "') from " + dbField.m_maxLen + " to " + thisField.m_maxLen;
/*      */ 
/* 1005 */         Report.trace(null, msg, null);
/*      */       }
/*      */     }
/*      */ 
/* 1009 */     String[] pkNames = { "dID" };
/* 1010 */     FieldInfo[] addFields = { thisField };
/* 1011 */     ws.alterTable(dDocMetaSet, addFields, null, pkNames);
/* 1012 */     if (null == cxt)
/*      */     {
/* 1014 */       cxt = new ExecutionContextAdaptor();
/*      */     }
/* 1016 */     PluginFilters.filter("updateDocMetaTable", ws, binder, cxt);
/* 1017 */     return true;
/*      */   }
/*      */ 
/*      */   public static void deleteMetaData(Workspace ws, ExecutionContext cxt, String name)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1031 */     DataBinder db = new DataBinder();
/* 1032 */     db.putLocal("dName", name);
/* 1033 */     executeService(ws, cxt, db, "DEL_METADEF");
/*      */ 
/* 1035 */     DataBinder db2 = new DataBinder();
/* 1036 */     db2.putLocal("MetaFieldsToDelete", name);
/* 1037 */     executeService(ws, cxt, db2, "UPDATE_META_TABLE");
/*      */   }
/*      */ 
/*      */   public static void setOptionList(Workspace ws, ExecutionContext cxt, String optionListKey, String optionListString)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1052 */     if (optionListString == null) {
/*      */       return;
/*      */     }
/* 1055 */     DataBinder binder = new DataBinder();
/* 1056 */     binder.putLocal("OptionListString", optionListString);
/* 1057 */     binder.putLocal("dKey", optionListKey);
/*      */ 
/* 1059 */     executeService(ws, cxt, binder, "UPDATE_OPTION_LIST");
/*      */   }
/*      */ 
/*      */   public static String getOptionListKey(String key)
/*      */   {
/* 1070 */     String retVal = null;
/* 1071 */     if (key.startsWith("x"))
/*      */     {
/* 1073 */       retVal = key.substring(1) + "List";
/*      */     }
/*      */     else
/*      */     {
/* 1077 */       retVal = key + "List";
/*      */     }
/* 1079 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static String calculateOverrideBitFlag()
/*      */     throws DataException
/*      */   {
/* 1095 */     DataResultSet data = SharedObjects.getTable("UserMetaDefinition");
/* 1096 */     FieldInfo info = new FieldInfo();
/* 1097 */     Vector usedBits = new IdcVector();
/* 1098 */     int highBit = 0;
/* 1099 */     int bit = 16;
/*      */ 
/* 1101 */     if (data.getFieldInfo("umdOverrideBitFlag", info))
/*      */     {
/* 1103 */       for (data.first(); data.isRowPresent(); data.next())
/*      */       {
/* 1105 */         String value = data.getStringValue(info.m_index);
/*      */ 
/* 1107 */         if (value == null)
/*      */           continue;
/* 1109 */         usedBits.addElement(value);
/* 1110 */         int v = Integer.parseInt(value);
/*      */ 
/* 1112 */         if ((v <= highBit) || (v < 16))
/*      */           continue;
/* 1114 */         highBit = v;
/*      */       }
/*      */ 
/* 1119 */       if (highBit >= 16)
/*      */       {
/* 1121 */         bit = highBit * 2;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1126 */     if (bit > 536870912)
/*      */     {
/* 1128 */       bit = 16;
/*      */ 
/* 1130 */       while (usedBits.contains("" + bit))
/*      */       {
/* 1132 */         bit *= 2;
/*      */       }
/*      */ 
/* 1136 */       if (bit > 536870912)
/*      */       {
/* 1138 */         throw new DataException("!apUserMetaBitValueHigh");
/*      */       }
/*      */     }
/* 1141 */     return String.valueOf(bit);
/*      */   }
/*      */ 
/*      */   public static void addUserInfoField(Workspace ws, ExecutionContext cxt, String name)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1154 */     String overrideBitFlag = null;
/*      */     try
/*      */     {
/* 1157 */       overrideBitFlag = calculateOverrideBitFlag();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1161 */       throw new ServiceException(e.getMessage());
/*      */     }
/* 1163 */     DataBinder binder = new DataBinder();
/* 1164 */     binder.putLocal("umdName", "u" + name);
/* 1165 */     binder.putLocal("umdType", "Text");
/* 1166 */     binder.putLocal("umdCaption", name);
/* 1167 */     binder.putLocal("umdIsOptionList", "0");
/* 1168 */     binder.putLocal("umdOptionListType", "");
/* 1169 */     binder.putLocal("umdOptionListKey", "");
/* 1170 */     binder.putLocal("umdIsAdminEdit", "0");
/* 1171 */     binder.putLocal("umdIsViewOnly", "0");
/* 1172 */     binder.putLocal("umdOverrideBitFlag", overrideBitFlag);
/* 1173 */     binder.putLocal("action", "ADD");
/*      */     try
/*      */     {
/* 1176 */       executeService(ws, cxt, binder, "UPDATE_USER_META");
/*      */       try
/*      */       {
/* 1179 */         executeService(ws, cxt, binder, "UPDATE_USER_META_TABLE");
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/* 1185 */         binder.putLocal("umdName", name);
/* 1186 */         binder.putLocal("action", "DELETE");
/* 1187 */         executeService(ws, cxt, binder, "UPDATE_USER_META");
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1192 */       throw new ServiceException(e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void deleteUserInfoField(Workspace ws, ExecutionContext cxt, String name)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1206 */     DataBinder binder = new DataBinder();
/* 1207 */     binder.putLocal("umdName", "u" + name);
/* 1208 */     binder.putLocal("action", "DELETE");
/*      */ 
/* 1210 */     executeService(ws, cxt, binder, "UPDATE_USER_META");
/*      */ 
/* 1212 */     binder.putLocal("UserMetaFieldsToDelete", "u" + name);
/* 1213 */     executeService(ws, cxt, binder, "UPDATE_USER_META_TABLE");
/*      */   }
/*      */ 
/*      */   public static void addToUserOptionList(Workspace ws, ExecutionContext cxt, String optionListName, String item)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1227 */     DataBinder uol = new DataBinder();
/*      */ 
/* 1229 */     UserStorage.synchronizeOptionLists(uol, false, false);
/*      */ 
/* 1231 */     Vector vx = SharedObjects.getOptList(optionListName);
/* 1232 */     String optionListItems = "";
/* 1233 */     if (vx != null)
/*      */     {
/* 1235 */       Enumeration e = vx.elements();
/* 1236 */       while (e.hasMoreElements())
/*      */       {
/* 1238 */         String listItem = (String)e.nextElement();
/* 1239 */         if (listItem.equals(item))
/*      */         {
/* 1241 */           return;
/*      */         }
/* 1243 */         optionListItems = optionListItems + listItem + "\n";
/*      */       }
/*      */     }
/* 1246 */     optionListItems = optionListItems + item;
/* 1247 */     uol.putLocal("OptionListString", optionListItems);
/* 1248 */     uol.putLocal("dKey", optionListName);
/*      */ 
/* 1250 */     executeService(ws, cxt, uol, "UPDATE_USEROPTION_LIST");
/*      */   }
/*      */ 
/*      */   public static boolean isMetaDataEnabled(String name, int flag)
/*      */     throws DataException
/*      */   {
/* 1262 */     DataResultSet docMetaDefs = SharedObjects.getTable("DocMetaDefinition");
/* 1263 */     String isEnabledStr = ResultSetUtils.findValue(docMetaDefs, "dName", name, "dIsEnabled");
/* 1264 */     if ((isEnabledStr == null) && ((flag & 0x1) == 0))
/*      */     {
/* 1266 */       throw new DataException(null, "csUnableToCheckFieldEnabledFieldNotExists", new Object[] { name });
/*      */     }
/* 1268 */     return StringUtils.convertToBool(isEnabledStr, false);
/*      */   }
/*      */ 
/*      */   public static void enableDisableMetaData(Workspace ws, ExecutionContext cxt, String name, boolean isEnable)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1285 */     boolean oldStatus = isMetaDataEnabled(name, 0);
/* 1286 */     if (oldStatus == isEnable)
/*      */       return;
/* 1288 */     DataResultSet docMetaDefs = SharedObjects.getTable("DocMetaDefinition");
/* 1289 */     FieldInfo fi = new FieldInfo();
/* 1290 */     docMetaDefs.getFieldInfo("dName", fi);
/* 1291 */     docMetaDefs.findRow(fi.m_index, name);
/* 1292 */     Properties props = docMetaDefs.getCurrentRowProps();
/*      */ 
/* 1294 */     DataBinder binder = new DataBinder();
/* 1295 */     binder.setLocalData(props);
/* 1296 */     binder.putLocal("dIsEnabled", (isEnable) ? "1" : "0");
/* 1297 */     executeService(ws, cxt, binder, "EDIT_METADEF");
/*      */   }
/*      */ 
/*      */   public static ResultSet getUserMetaDefs(Workspace ws, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1309 */     DataBinder binder = new DataBinder();
/* 1310 */     executeService(ws, cxt, binder, "GET_USER_METADEFS");
/* 1311 */     return binder.getResultSet("Users");
/*      */   }
/*      */ 
/*      */   public static void loadAdditionalDocInfo(Workspace ws, DataBinder binder, ExecutionContext cxt)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1321 */     PluginFilters.filter("loadAdditionalDocInfo", ws, binder, cxt);
/*      */     try
/*      */     {
/* 1325 */       if (cxt instanceof Service)
/*      */       {
/* 1327 */         PageMerger pageMerger = ((Service)cxt).getPageMerger();
/* 1328 */         if (pageMerger != null)
/*      */         {
/* 1330 */           pageMerger.evaluateResourceInclude("load_additional_doc_info");
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/* 1336 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static void executeService(Workspace ws, ExecutionContext cxt, DataBinder binder, String action)
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/* 1353 */       binder.putLocal("IdcService", action);
/* 1354 */       binder.setEnvironmentValue("REMOTE_USER", "sysadmin");
/* 1355 */       ServiceManager smg = new ServiceManager();
/* 1356 */       smg.init(binder, ws);
/* 1357 */       smg.processCommand();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1361 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1367 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99275 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.MetaFieldUtils
 * JD-Core Version:    0.5.4
 */