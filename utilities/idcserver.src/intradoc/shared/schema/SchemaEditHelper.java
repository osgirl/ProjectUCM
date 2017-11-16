/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaEditHelper
/*     */ {
/*     */   public void checkViewUse(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/*  37 */     String viewName = binder.get("schViewName");
/*  38 */     SchemaHelper schHelper = new SchemaHelper();
/*  39 */     SchemaFieldConfig fields = schHelper.m_fields;
/*     */ 
/*  41 */     String[] view = new String[1];
/*  42 */     for (fields.first(); fields.isRowPresent(); fields.next())
/*     */     {
/*  44 */       SchemaFieldData field = (SchemaFieldData)fields.getData();
/*  45 */       boolean isOptList = field.getBoolean("dIsOptionList", false);
/*  46 */       if (!isOptList)
/*     */         continue;
/*  48 */       boolean isList = schHelper.isViewFieldEx(field, view);
/*  49 */       if ((!isList) || (!viewName.equals(view[0])))
/*     */         continue;
/*  51 */       String errMsg = LocaleUtils.encodeMessage("apSchViewInUseError", null, viewName, field.get("dCaption"));
/*     */ 
/*  53 */       throw new ServiceException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void validateViewIntegrity(DataBinder binder, SchemaViewData viewData, boolean isNew)
/*     */     throws DataException, ServiceException
/*     */   {
/*  65 */     String viewName = binder.get("schViewName");
/*  66 */     String viewType = binder.get("schViewType");
/*  67 */     String tableName = binder.getAllowMissing("schTableName");
/*  68 */     if (isNew)
/*     */       return;
/*  70 */     String curViewName = viewData.get("schViewName");
/*  71 */     if (!viewName.equals(curViewName))
/*     */     {
/*  73 */       String errMsg = LocaleUtils.encodeMessage("apSchViewChangeNameError", null, curViewName, viewName);
/*     */ 
/*  75 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/*  78 */     String oldType = viewData.get("schViewType");
/*  79 */     if (!viewType.equals(oldType))
/*     */     {
/*  81 */       String errMsg = LocaleUtils.encodeMessage("apSchViewChangeTypeError", null, oldType, viewType);
/*     */ 
/*  83 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/*  86 */     if (!oldType.equals("table"))
/*     */       return;
/*  88 */     String oldTable = viewData.get("schTableName");
/*  89 */     if (tableName.equals(oldTable))
/*     */       return;
/*  91 */     String errMsg = LocaleUtils.encodeMessage("apSchViewChangeTableError", null, oldTable, tableName);
/*     */ 
/*  93 */     throw new ServiceException(errMsg);
/*     */   }
/*     */ 
/*     */   public void checkRelationUse(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 102 */     String relName = binder.get("schRelationName");
/* 103 */     SchemaHelper schHelper = new SchemaHelper();
/*     */ 
/* 105 */     SchemaFieldData[] fieldArray = new SchemaFieldData[1];
/* 106 */     boolean isNamed = schHelper.isNamedRelationshipEx(relName, fieldArray);
/*     */ 
/* 108 */     if (!isNamed)
/*     */       return;
/* 110 */     SchemaFieldData field = fieldArray[0];
/* 111 */     String fieldName = field.get("schFieldName");
/* 112 */     String target = field.get("schFieldTarget");
/* 113 */     String msg = "apSchRelationInUseError";
/* 114 */     if ((target != null) && (target.length() > 0))
/*     */     {
/* 116 */       msg = msg + "_" + target.toLowerCase();
/*     */     }
/* 118 */     String errMsg = LocaleUtils.encodeMessage(msg, null, relName, fieldName);
/* 119 */     throw new ServiceException(errMsg);
/*     */   }
/*     */ 
/*     */   public void validateRelationIntegrity(SchemaRelationData data, DataBinder binder, boolean isNew)
/*     */     throws DataException, ServiceException
/*     */   {
/* 127 */     String relName = binder.get("schRelationName");
/* 128 */     if (isNew)
/*     */       return;
/* 130 */     SchemaHelper schHelper = new SchemaHelper();
/* 131 */     SchemaFieldData[] field = new SchemaFieldData[1];
/*     */ 
/* 133 */     boolean isUsed = schHelper.isNamedRelationshipEx(relName, field);
/* 134 */     if (!isUsed) {
/*     */       return;
/*     */     }
/* 137 */     String[][] tableInfo = { { "schTable1Table", "apSchTable1Title" }, { "schTable1Column", "apSchTableColumnTitle" }, { "schTable2Table", "apSchTable2Title" }, { "schTable2Column", "apSchTable2ColumnTitle" } };
/*     */ 
/* 145 */     int len = tableInfo.length;
/* 146 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 148 */       String key = tableInfo[i][0];
/*     */ 
/* 150 */       String newVal = data.get(key);
/* 151 */       String curVal = binder.get(key);
/* 152 */       if (curVal.equals(newVal))
/*     */         continue;
/* 154 */       SchemaFieldData fieldData = field[0];
/* 155 */       String errMsg = LocaleUtils.encodeMessage("apSchRelationInUseError", null, relName, fieldData.get("dCaption"));
/*     */ 
/* 157 */       errMsg = LocaleUtils.encodeMessage("apSchRelationChangeError", errMsg, relName, curVal, newVal);
/*     */ 
/* 159 */       throw new ServiceException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void checkTableUse(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 169 */     SchemaViewConfig viewConfig = (SchemaViewConfig)SharedObjects.getTable("SchemaViewConfig");
/*     */ 
/* 171 */     SchemaRelationConfig relConfig = (SchemaRelationConfig)SharedObjects.getTable("SchemaRelationConfig");
/*     */ 
/* 174 */     String table = binder.get("schTableName");
/* 175 */     for (viewConfig.first(); viewConfig.isRowPresent(); viewConfig.next())
/*     */     {
/* 177 */       SchemaViewData data = (SchemaViewData)viewConfig.getData();
/* 178 */       String viewType = data.get("schViewType");
/* 179 */       if (!viewType.equals("table"))
/*     */         continue;
/* 181 */       String tableName = data.get("schTableName");
/* 182 */       if (!table.equals(tableName))
/*     */         continue;
/* 184 */       String errMsg = LocaleUtils.encodeMessage("apSchTableInUseByView", null, tableName, data.get("schViewName"));
/*     */ 
/* 186 */       throw new ServiceException(errMsg);
/*     */     }
/*     */ 
/* 191 */     String[][] tableInfo = { { "schTable1Table", "apSchTable1Title" }, { "schTable2Table", "apSchTable2Title" } };
/*     */ 
/* 196 */     int len = tableInfo.length;
/* 197 */     for (relConfig.first(); relConfig.isRowPresent(); relConfig.next())
/*     */     {
/* 199 */       SchemaRelationData data = (SchemaRelationData)relConfig.getData();
/* 200 */       for (int i = 0; i < len; ++i)
/*     */       {
/* 202 */         String key = tableInfo[i][0];
/*     */ 
/* 204 */         String tableName = data.get(key);
/* 205 */         if (!table.equals(tableName))
/*     */           continue;
/* 207 */         String errMsg = LocaleUtils.encodeMessage("apSchTableInUseByRelation", null, table, data.get("schRelationName"));
/*     */ 
/* 210 */         throw new ServiceException(errMsg);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void checkFieldUse(String fieldName)
/*     */     throws DataException, ServiceException
/*     */   {
/* 218 */     SchemaFieldConfig fields = (SchemaFieldConfig)SharedObjects.getTable("SchemaFieldConfig");
/* 219 */     for (fields.first(); fields.isRowPresent(); fields.next())
/*     */     {
/* 221 */       SchemaFieldData fd = (SchemaFieldData)fields.getData();
/* 222 */       String name = fd.get("schFieldName");
/*     */ 
/* 224 */       if (name.equals(fieldName)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 228 */       boolean isOption = fd.getBoolean("dIsOptionList", false);
/* 229 */       if (!isOption)
/*     */         continue;
/* 231 */       boolean isDep = fd.getBoolean("IsDependentList", false);
/* 232 */       if (!isDep)
/*     */         continue;
/* 234 */       String depField = fd.get("DependentOnField");
/* 235 */       if (!depField.equals(fieldName))
/*     */         continue;
/* 237 */       String errMsg = LocaleUtils.encodeMessage("apSchFieldInUseByField", null, fieldName, name);
/*     */ 
/* 239 */       throw new ServiceException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void validateViewData(DataBinder newBinder, DataBinder oldBinder)
/*     */     throws ServiceException, DataException
/*     */   {
/* 255 */     String[][] config = { { "schViewName", "apSchViewName" }, { "schTableName", "apSchTableName" }, { "schInternalColumn", "apSchInternalColumn" }, { "schSortOrder", "apSchSortOrder" }, { "schViewColumns", "apSchViewColumns" } };
/*     */ 
/* 263 */     int len = config.length;
/* 264 */     String tableName = null;
/* 265 */     String viewName = null;
/* 266 */     Hashtable viewClmns = new Hashtable();
/* 267 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 269 */       String key = config[i][0];
/* 270 */       String newVal = newBinder.get(key);
/* 271 */       String oldVal = newBinder.get(key);
/*     */ 
/* 273 */       if (key.equals("schTableName"))
/*     */       {
/* 275 */         tableName = newVal;
/*     */       }
/* 277 */       else if (key.equals("schViewName"))
/*     */       {
/* 279 */         viewName = newVal;
/*     */       }
/*     */ 
/* 283 */       boolean isError = false;
/* 284 */       if (key.equals("schViewColumns"))
/*     */       {
/* 286 */         Vector newClmns = StringUtils.parseArray(newVal, ',', '^');
/* 287 */         Vector oldClmns = StringUtils.parseArray(oldVal, ',', '^');
/* 288 */         int size = newClmns.size();
/* 289 */         for (int j = 0; j < size; ++j)
/*     */         {
/* 291 */           String clmn = (String)newClmns.elementAt(j);
/* 292 */           viewClmns.put(clmn, clmn);
/* 293 */           boolean isFound = false;
/* 294 */           int oldSize = oldClmns.size();
/* 295 */           for (int k = 0; k < oldSize; ++k)
/*     */           {
/* 297 */             String oldClmn = (String)oldClmns.elementAt(k);
/* 298 */             if (!oldClmn.equals(clmn))
/*     */               continue;
/* 300 */             oldClmns.removeElementAt(k);
/* 301 */             isFound = true;
/* 302 */             break;
/*     */           }
/*     */ 
/* 305 */           if (isFound)
/*     */             continue;
/* 307 */           isError = true;
/* 308 */           break;
/*     */         }
/*     */ 
/* 312 */         size = oldClmns.size();
/* 313 */         if (size > 0)
/*     */         {
/* 315 */           isError = true;
/*     */         }
/*     */       }
/* 318 */       else if (!newVal.equals(oldVal))
/*     */       {
/* 320 */         isError = true;
/*     */       }
/* 322 */       if (!isError)
/*     */         continue;
/* 324 */       String[] args = new String[4];
/* 325 */       args[0] = viewName;
/* 326 */       args[1] = config[i][1];
/* 327 */       args[2] = oldVal;
/* 328 */       args[3] = newVal;
/* 329 */       String errMsg = LocaleUtils.encodeMessage("apSchViewDefinitionChanged", null, args);
/* 330 */       throw new DataException(errMsg);
/*     */     }
/*     */ 
/* 334 */     DataResultSet newSet = (DataResultSet)newBinder.getResultSet(tableName);
/* 335 */     DataResultSet oldSet = (DataResultSet)oldBinder.getResultSet(tableName);
/* 336 */     checkForViewResultsetChange(newSet, oldSet, viewClmns);
/*     */   }
/*     */ 
/*     */   public void checkForViewResultsetChange(DataResultSet newSet, DataResultSet oldSet, Hashtable viewClmns)
/*     */     throws DataException
/*     */   {
/* 342 */     if (oldSet == null)
/*     */     {
/* 344 */       return;
/*     */     }
/*     */ 
/* 347 */     int numNewFields = newSet.getNumFields();
/* 348 */     for (int i = 0; i < numNewFields; ++i)
/*     */     {
/* 350 */       FieldInfo info = new FieldInfo();
/* 351 */       newSet.getIndexFieldInfo(i, info);
/*     */ 
/* 354 */       String clmn = (String)viewClmns.get(info.m_name);
/* 355 */       if (clmn == null)
/*     */         continue;
/* 357 */       FieldInfo oldInfo = new FieldInfo();
/* 358 */       oldSet.getIndexFieldInfo(i, oldInfo);
/*     */ 
/* 361 */       if (!info.m_name.equals(oldInfo))
/*     */         continue;
/* 363 */       String errMsg = LocaleUtils.encodeMessage("apSchViewTableFieldChanged", null, info.m_name);
/* 364 */       throw new DataException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void detectRelationshipChange(String fieldName, String tableName, boolean isField, Vector curParentList)
/*     */     throws DataException
/*     */   {
/* 375 */     SchemaHelper schHelper = new SchemaHelper();
/* 376 */     schHelper.computeMaps();
/*     */ 
/* 378 */     Vector parentList = null;
/* 379 */     if (isField)
/*     */     {
/* 381 */       Vector fieldList = schHelper.computeParentListForField(fieldName);
/* 382 */       parentList = new IdcVector();
/* 383 */       parentList.addElement(fieldList);
/*     */     }
/*     */     else
/*     */     {
/* 387 */       parentList = schHelper.buildParentTree(tableName, fieldName);
/*     */     }
/*     */ 
/* 391 */     int size = parentList.size();
/* 392 */     int curSize = curParentList.size();
/* 393 */     if (size != curSize)
/*     */     {
/* 395 */       throw new DataException("!apSchNamedParentsChanged");
/*     */     }
/* 397 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 399 */       Vector parents = (Vector)parentList.elementAt(i);
/* 400 */       Vector curParents = (Vector)curParentList.elementAt(i);
/*     */ 
/* 402 */       int num = parents.size();
/* 403 */       int curNum = curParents.size();
/* 404 */       if (num != curNum)
/*     */       {
/* 406 */         throw new DataException("!apSchNamedParentsChanged");
/*     */       }
/*     */ 
/* 409 */       for (int j = 0; j < num; ++j)
/*     */       {
/* 411 */         NamedRelationship namedRel = (NamedRelationship)parents.elementAt(j);
/* 412 */         NamedRelationship curNamedRel = (NamedRelationship)curParents.elementAt(j);
/* 413 */         compareNamedRelations(fieldName, namedRel, curNamedRel);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void compareNamedRelations(String fieldName, NamedRelationship namedRel, NamedRelationship curNamedRel)
/*     */     throws DataException
/*     */   {
/* 421 */     String[][] config = { { "view", "schViewName", "apSchViewName" }, { "view", "schTableName", "apSchTableName" }, { "field", "schFieldName", "apSchFieldName" }, { "relation", "schRelationName", "apSchRelationName" }, { "parent", "schFieldName", "apSchParentFieldName" } };
/*     */ 
/* 429 */     int num = config.length;
/* 430 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 432 */       String obj = config[i][0];
/* 433 */       String key = config[i][1];
/*     */ 
/* 435 */       String val = null;
/* 436 */       String curVal = null;
/* 437 */       if (obj.equals("view"))
/*     */       {
/* 439 */         if (namedRel.m_view != null)
/*     */         {
/* 441 */           val = namedRel.m_view.get(key);
/*     */         }
/* 443 */         if (curNamedRel.m_view != null)
/*     */         {
/* 445 */           curVal = curNamedRel.m_view.get(key);
/*     */         }
/*     */       }
/* 448 */       else if (obj.equals("field"))
/*     */       {
/* 450 */         if (namedRel.m_field != null)
/*     */         {
/* 452 */           val = namedRel.m_field.get(key);
/*     */         }
/* 454 */         if (curNamedRel.m_field != null)
/*     */         {
/* 456 */           curVal = curNamedRel.m_field.get(key);
/*     */         }
/*     */       }
/* 459 */       else if ((obj.equals("relation")) && (namedRel.m_relation != null))
/*     */       {
/* 461 */         if (namedRel.m_relation != null)
/*     */         {
/* 463 */           val = namedRel.m_relation.get(key);
/*     */         }
/* 465 */         if (curNamedRel.m_relation != null)
/*     */         {
/* 467 */           curVal = curNamedRel.m_relation.get(key);
/*     */         }
/*     */       }
/* 470 */       else if ((obj.equals("parent")) && (namedRel.m_parentField != null))
/*     */       {
/* 472 */         if (namedRel.m_parentField != null)
/*     */         {
/* 474 */           val = namedRel.m_parentField.get(key);
/*     */         }
/* 476 */         if (curNamedRel.m_parentField != null)
/*     */         {
/* 478 */           curVal = curNamedRel.m_parentField.get(key);
/*     */         }
/*     */       }
/*     */ 
/* 482 */       if ((val == null) && (curVal == null)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 486 */       if ((val != null) && (curVal != null) && (val.equals(curVal)))
/*     */         continue;
/* 488 */       String[] args = new String[4];
/* 489 */       args[0] = fieldName;
/* 490 */       args[1] = config[i][2];
/* 491 */       args[2] = curVal;
/* 492 */       args[3] = val;
/* 493 */       String errMsg = LocaleUtils.encodeMessage("apSchNamedRelationDefChanged", null, args);
/* 494 */       throw new DataException(errMsg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 501 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaEditHelper
 * JD-Core Version:    0.5.4
 */