/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.SchemaService;
/*     */ import intradoc.server.ServiceData;
/*     */ import intradoc.server.ServiceManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaTableData;
/*     */ import intradoc.shared.schema.SchemaViewData;
/*     */ import java.util.Iterator;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class SchemaInitUtils
/*     */ {
/*     */   public ServerSchemaManager m_manager;
/*     */ 
/*     */   public SchemaInitUtils()
/*     */   {
/*  33 */     this.m_manager = null;
/*     */   }
/*     */ 
/*     */   public void init(ServerSchemaManager manager) {
/*  37 */     this.m_manager = manager;
/*     */   }
/*     */ 
/*     */   public boolean addArrayView(String className, String memberName, String viewName)
/*     */     throws ServiceException
/*     */   {
/*  43 */     SchemaStorage viewStorage = this.m_manager.getStorageImplementor("SchemaViewConfig");
/*  44 */     SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(viewName);
/*  45 */     if (view == null)
/*     */     {
/*  47 */       DataBinder binder = new DataBinder();
/*  48 */       binder.putLocal("schIsSystemObject", "true");
/*  49 */       binder.putLocal("schViewName", viewName);
/*  50 */       binder.putLocal("schViewDescription", "System view on " + className + "." + memberName);
/*     */ 
/*  52 */       binder.putLocal("schViewType", "javaArray");
/*  53 */       binder.putLocal("schClassName", className);
/*  54 */       binder.putLocal("schFieldName", memberName);
/*  55 */       binder.putLocal("schViewColumns", "schInternal,schDisplay");
/*  56 */       binder.putLocal("schLabelColumn", "schDisplay");
/*  57 */       binder.putLocal("schInternalColumn", "schInternal");
/*  58 */       binder.putLocal("schDefaultDisplayExpression", "<$lc(schDisplay)$>");
/*     */       try
/*     */       {
/*  61 */         viewStorage.createOrUpdate(viewName, binder, false);
/*  62 */         return true;
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  66 */         Report.trace("schemainit", null, e);
/*  67 */         String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, viewName);
/*     */ 
/*  69 */         Report.error(null, msg, e);
/*     */       }
/*     */     }
/*  72 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean addSharedObjectsTableView(String tableName, String viewName, String internalColumn, String displayColumn, String[] extraSettings, String columnList)
/*     */     throws ServiceException
/*     */   {
/*  79 */     SchemaStorage viewStorage = this.m_manager.getStorageImplementor("SchemaViewConfig");
/*  80 */     SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(viewName);
/*  81 */     boolean retVal = false;
/*  82 */     if (view == null)
/*     */     {
/*  84 */       retVal = addSharedObjectsTableViewToStorage(viewStorage, null, tableName, viewName, internalColumn, displayColumn, extraSettings, columnList, false);
/*     */     }
/*     */ 
/*  87 */     return retVal;
/*     */   }
/*     */ 
/*     */   public boolean addSharedObjectsTableViewToStorage(SchemaStorage viewStorage, SchemaViewData curView, String tableName, String viewName, String internalColumn, String displayColumn, String[] extraSettings, String columnList, boolean isUpdate)
/*     */     throws ServiceException
/*     */   {
/*  95 */     DataBinder binder = new DataBinder();
/*  96 */     binder.putLocal("schIsSystemObject", "true");
/*  97 */     binder.putLocal("schViewDescription", "System view on SharedObjects table " + tableName);
/*     */ 
/*  99 */     binder.putLocal("schViewType", "SharedObjectsTable");
/* 100 */     binder.putLocal("schTableName", tableName);
/* 101 */     binder.putLocal("schLabelColumn", displayColumn);
/* 102 */     binder.putLocal("schInternalColumn", internalColumn);
/*     */ 
/* 104 */     if (columnList == null)
/*     */     {
/* 106 */       DataResultSet drset = SharedObjects.getTable(tableName);
/* 107 */       if (drset == null)
/*     */       {
/* 109 */         Report.trace("schemainit", "unable to create view " + viewName + " on SharedObjects table " + tableName + " because the table doesn't exist.", null);
/*     */ 
/* 112 */         return false;
/*     */       }
/* 114 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 115 */       for (int i = 0; i < drset.getNumFields(); ++i)
/*     */       {
/* 117 */         if (i > 0)
/*     */         {
/* 119 */           builder.append(',');
/*     */         }
/* 121 */         builder.append(drset.getFieldName(i));
/*     */       }
/* 123 */       columnList = builder.toString();
/*     */     }
/* 125 */     binder.putLocal("schViewColumns", columnList);
/* 126 */     DataBinder oldBinder = null;
/* 127 */     if ((curView != null) && (isUpdate))
/*     */     {
/* 129 */       oldBinder = curView.getData();
/*     */     }
/* 131 */     return updateOrCreateView(viewName, viewStorage, oldBinder, binder, extraSettings, isUpdate);
/*     */   }
/*     */ 
/*     */   public boolean mergeUpdateViewProperties(String viewName, DataBinder curData, SchemaStorage viewStorage, String[] extraSettings)
/*     */     throws ServiceException
/*     */   {
/* 137 */     DataBinder binder = new DataBinder();
/* 138 */     binder.copyLocalDataStateClone(curData);
/* 139 */     return updateOrCreateView(viewName, viewStorage, curData, binder, extraSettings, true);
/*     */   }
/*     */ 
/*     */   public boolean updateOrCreateView(String viewName, SchemaStorage viewStorage, DataBinder oldBinder, DataBinder binder, String[] extraSettings, boolean isUpdate)
/*     */     throws ServiceException
/*     */   {
/* 145 */     boolean hasChanged = (!isUpdate) || (oldBinder == null);
/* 146 */     binder.putLocal("schViewName", viewName);
/* 147 */     if (extraSettings != null)
/*     */     {
/* 149 */       for (int i = 0; i < extraSettings.length; ++i)
/*     */       {
/* 151 */         String settingString = extraSettings[i];
/* 152 */         int index = settingString.indexOf("=");
/* 153 */         if (index <= 0)
/*     */         {
/* 155 */           Report.trace("schemainit", "setting string '" + settingString + "' for view " + viewName + " is missing an =", null);
/*     */         }
/*     */         else
/*     */         {
/* 160 */           String key = settingString.substring(0, index);
/* 161 */           String value = settingString.substring(index + 1);
/* 162 */           if (!hasChanged)
/*     */           {
/* 164 */             String oldValue = binder.getLocal(key);
/* 165 */             if ((oldValue == null) || (!oldValue.equals(value)))
/*     */             {
/* 167 */               hasChanged = true;
/*     */             }
/*     */           }
/*     */ 
/* 171 */           binder.putLocal(key, value);
/*     */         }
/*     */       }
/*     */     }
/*     */     try {
/* 176 */       if (hasChanged)
/*     */       {
/* 178 */         viewStorage.createOrUpdate(viewName, binder, isUpdate);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 183 */       Report.trace("schemainit", null, e);
/* 184 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemView", null, viewName);
/*     */ 
/* 186 */       Report.error(null, msg, e);
/* 187 */       hasChanged = false;
/*     */     }
/* 189 */     return hasChanged;
/*     */   }
/*     */ 
/*     */   public boolean addGenericObject(String type, String name, DataBinder data, boolean isUpdate)
/*     */     throws ServiceException
/*     */   {
/* 197 */     SchemaStorage storage = this.m_manager.getStorageImplementor("Schema" + type + "Config");
/* 198 */     SchemaData oldSchemaData = storage.getSchemaData(name);
/* 199 */     boolean isChanged = !isUpdate;
/* 200 */     DataBinder oldData = null;
/* 201 */     if (oldSchemaData != null)
/*     */     {
/* 203 */       if (!isChanged)
/*     */       {
/* 205 */         oldData = oldSchemaData.getData();
/* 206 */         if (oldData == null)
/*     */         {
/* 208 */           isChanged = true;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 213 */         Report.trace("schemainit", type + " " + name + " already exists.", null);
/* 214 */         return false;
/*     */       }
/*     */     }
/*     */ 
/* 218 */     if ((!isChanged) && (data.getResultSets().size() > 0))
/*     */     {
/* 220 */       isChanged = true;
/*     */     }
/*     */     Properties localData;
/*     */     Iterator i$;
/* 222 */     if ((!isChanged) && (oldData != null))
/*     */     {
/* 224 */       localData = data.getLocalData();
/*     */ 
/* 226 */       Set s = localData.keySet();
/* 227 */       for (i$ = s.iterator(); i$.hasNext(); ) { Object key = i$.next();
/*     */ 
/* 229 */         String val = (String)localData.get(key);
/* 230 */         String oldVal = oldData.getLocal((String)key);
/* 231 */         if ((oldVal == null) || (!oldVal.equals(val)))
/*     */         {
/* 233 */           isChanged = true;
/* 234 */           break;
/*     */         } }
/*     */ 
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 241 */       if (isChanged)
/*     */       {
/* 243 */         Report.trace("schemainit", "creating or updating " + type.toLowerCase() + " " + name, null);
/*     */ 
/* 245 */         storage.createOrUpdate(name, data, isUpdate);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 250 */       Report.trace("schemainit", null, e);
/* 251 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystem" + type, null, name);
/*     */ 
/* 253 */       throw new ServiceException(msg, e);
/*     */     }
/* 255 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public void promoteOptionList(String listName, String type, boolean checkExistence)
/*     */     throws DataException, ServiceException
/*     */   {
/* 261 */     SchemaStorage viewStorage = this.m_manager.getStorageImplementor("SchemaViewConfig");
/* 262 */     if (checkExistence)
/*     */     {
/* 264 */       SchemaViewData view = (SchemaViewData)viewStorage.getSchemaData(listName);
/* 265 */       if (view != null)
/*     */       {
/* 267 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 271 */     DataBinder binder = new DataBinder();
/* 272 */     binder.putLocal("schViewName", listName);
/* 273 */     binder.putLocal("schViewParent", "");
/* 274 */     binder.putLocal("schParameterColumn", "");
/* 275 */     binder.putLocal("schViewType", type);
/* 276 */     binder.putLocal("schViewDescription", LocaleResources.getString("csPromotedOptionListDescription", null, listName));
/*     */ 
/* 278 */     binder.putLocal("schOptionList", listName);
/* 279 */     binder.putLocal("schPrimaryKey", "dOption");
/* 280 */     binder.putLocal("schInternalColumn", "dOption");
/* 281 */     binder.putLocal("schLabelColumn", "dOption");
/*     */ 
/* 283 */     if (type.equals("table"))
/*     */     {
/* 285 */       binder.putLocal("schCriteriaField0", "dKey");
/* 286 */       binder.putLocal("schCriteriaValue0", listName);
/* 287 */       binder.putLocal("schIsServerSorted", "true");
/* 288 */       binder.putLocal("schSortField", "dOrder");
/* 289 */       binder.putLocal("schSortOrder", "ascending");
/* 290 */       binder.putLocal("schViewColumns", "dKey,dOption,dOrder");
/* 291 */       binder.putLocal("schTableName", "OptionsList");
/*     */     }
/* 293 */     viewStorage.createOrUpdate(listName, binder, false);
/*     */   }
/*     */ 
/*     */   public boolean addDefaultTable(String tableName, Workspace workspace)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 301 */       SchemaStorage tables = this.m_manager.getStorageImplementor("SchemaTableConfig");
/* 302 */       SchemaTableData data = (SchemaTableData)tables.getSchemaData(tableName);
/*     */ 
/* 304 */       if (data != null)
/*     */       {
/* 306 */         return false;
/*     */       }
/*     */ 
/* 309 */       ServiceData serviceData = new ServiceData();
/*     */       try
/*     */       {
/* 312 */         serviceData.init("SCHEMA_DRIVER", "intradoc.server.SchemaService", 0, null, null, null, null);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 318 */         throw new ServiceException(e);
/*     */       }
/* 320 */       DataBinder binder = new DataBinder();
/* 321 */       binder.putLocal("schTableName", tableName);
/* 322 */       binder.putLocal("IsCreateTable", "true");
/*     */ 
/* 324 */       SchemaService service = (SchemaService)ServiceManager.createService(serviceData.m_classID, workspace, null, binder, serviceData);
/*     */ 
/* 327 */       service.initDelegatedObjects();
/* 328 */       service.getSchemaTableInfo();
/* 329 */       service.clear();
/*     */ 
/* 334 */       tables.createOrUpdate(tableName, binder, false);
/* 335 */       return true;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 339 */       String msg = LocaleUtils.encodeMessage("csUnableToCreateSystemTable", null, tableName);
/*     */ 
/* 341 */       Report.trace("schemainit", null, e);
/* 342 */       throw new ServiceException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 348 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaInitUtils
 * JD-Core Version:    0.5.4
 */