/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DocClassUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Arrays;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocClassService extends Service
/*     */ {
/*     */   @IdcServiceAction
/*     */   public void verifyNewDocClass()
/*     */     throws ServiceException, DataException
/*     */   {
/*  48 */     String className = this.m_binder.get("dDocClass");
/*  49 */     if (!DocClassUtils.doesDocClassExist(className, false))
/*     */       return;
/*  51 */     String msg = LocaleUtils.encodeMessage("csDocClassExists", null, className);
/*     */ 
/*  53 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void verifyDocClassExists()
/*     */     throws ServiceException, DataException
/*     */   {
/*  61 */     String className = this.m_binder.get("dDocClass");
/*  62 */     if (DocClassUtils.doesDocClassExist(className, true))
/*     */       return;
/*  64 */     String msg = LocaleUtils.encodeMessage("csDocClassDoesNotExist", null, className);
/*     */ 
/*  66 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void verifyClassNotDefault()
/*     */     throws ServiceException, DataException
/*     */   {
/*  73 */     String className = this.m_binder.get("dDocClass");
/*  74 */     if (!className.equalsIgnoreCase("Base"))
/*     */       return;
/*  76 */     String msg = LocaleUtils.encodeMessage("csUnableToDeleteDefaultDocClass", null);
/*     */ 
/*  78 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void verifyDMSTableIsInClass()
/*     */     throws ServiceException, DataException
/*     */   {
/*  86 */     String className = this.m_binder.get("dDocClass");
/*  87 */     String dmsName = this.m_binder.get("dDocMetaSet");
/*  88 */     if (DocClassUtils.isSetInClass(className, dmsName))
/*     */       return;
/*  90 */     String msg = LocaleUtils.encodeMessage("csDMStableIsNotInDocClass", null, dmsName, className);
/*     */ 
/*  92 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void verifyDMSTableIsNotInClass()
/*     */     throws ServiceException, DataException
/*     */   {
/* 100 */     String className = this.m_binder.get("dDocClass");
/* 101 */     String dmsName = this.m_binder.get("dDocMetaSet");
/* 102 */     if (!DocClassUtils.isSetInClass(className, dmsName))
/*     */       return;
/* 104 */     String msg = LocaleUtils.encodeMessage("csDMStableIsInDocClass", null, dmsName, className);
/*     */ 
/* 106 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void validateDMSTableName()
/*     */     throws ServiceException, DataException
/*     */   {
/* 113 */     String dmsName = this.m_binder.get("dDocMetaSet");
/* 114 */     if ((dmsName != null) && (dmsName.length() != 0) && (dmsName.startsWith("DMS")))
/*     */       return;
/* 116 */     String msg = LocaleUtils.encodeMessage("csInvalidDMSTable", null, dmsName);
/*     */ 
/* 118 */     createServiceException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void addDocsToDMSTable()
/*     */     throws DataException
/*     */   {
/* 126 */     String rsname = this.m_currentAction.getParamAt(0);
/* 127 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(rsname);
/* 128 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 130 */       return;
/*     */     }
/*     */ 
/* 133 */     DataBinder binder = new DataBinder();
/* 134 */     binder.putLocal("dDocMetaSet", this.m_binder.get("dDocMetaSet"));
/* 135 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 137 */       binder.putLocal("dID", drset.getStringValueByName("dID"));
/* 138 */       this.m_workspace.addBatch("IdocMetaSetDoc", binder);
/*     */     }
/* 140 */     this.m_workspace.executeBatch();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void delDocsFromDMSTable()
/*     */     throws DataException
/*     */   {
/* 147 */     String rsname = this.m_currentAction.getParamAt(0);
/* 148 */     DataResultSet drset = (DataResultSet)this.m_binder.getResultSet(rsname);
/* 149 */     if ((drset == null) || (drset.isEmpty()))
/*     */     {
/* 151 */       return;
/*     */     }
/*     */ 
/* 154 */     DataBinder binder = new DataBinder();
/* 155 */     binder.putLocal("dDocMetaSet", this.m_binder.get("dDocMetaSet"));
/* 156 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 158 */       binder.putLocal("dID", drset.getStringValueByName("dID"));
/* 159 */       this.m_workspace.addBatch("DdocMetaSetDoc", binder);
/*     */     }
/* 161 */     this.m_workspace.executeBatch();
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareDefaultDocProfileValues() throws DataException, ServiceException
/*     */   {
/* 167 */     String className = this.m_binder.get("dDocClass");
/*     */ 
/* 169 */     String defProf = this.m_binder.getAllowMissing("dDefaultProfile");
/* 170 */     if (defProf == null)
/*     */     {
/* 172 */       defProf = "DCP_" + className;
/* 173 */       this.m_binder.putLocal("dDefaultProfile", defProf);
/*     */     }
/*     */ 
/* 177 */     this.m_binder.putLocal("isValidateTrigger", "1");
/* 178 */     this.m_binder.putLocal("dpName", defProf);
/* 179 */     this.m_binder.putLocal("dpDescription", LocaleResources.getString("csBaseProfileDescription", null, className));
/* 180 */     String trigger = this.m_binder.getAllowMissing("dpTriggerValue");
/* 181 */     if ((trigger == null) || (trigger.length() == 0))
/*     */     {
/* 183 */       trigger = className;
/*     */     }
/* 185 */     this.m_binder.putLocal("dpTriggerValue", trigger);
/* 186 */     this.m_binder.putLocal("dpDisplayLabel", className);
/*     */ 
/* 189 */     this.m_binder.putLocal("editViewValueAction", "add");
/* 190 */     this.m_binder.putLocal("schViewName", "ProfileTriggerValues");
/* 191 */     this.m_binder.putLocal("dProfileTriggerValue", trigger);
/* 192 */     this.m_binder.putLocal("dProfileTriggerOrder", "1");
/* 193 */     FieldInfo[] origInfos = this.m_workspace.getColumnList("ProfileTriggerValues");
/* 194 */     Vector origInfoVector = new IdcVector();
/* 195 */     origInfoVector.addAll(Arrays.asList(origInfos));
/* 196 */     DataResultSet prepSet = new DataResultSet();
/* 197 */     prepSet.mergeFieldsWithFlags(origInfoVector, 0);
/* 198 */     Vector v = prepSet.createRow(this.m_binder);
/* 199 */     prepSet.addRow(v);
/* 200 */     prepSet.first();
/* 201 */     this.m_binder.addResultSet("ProfileTriggerValues", prepSet);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void deleteDocClassProfilesAndTriggerValues()
/*     */     throws DataException, ServiceException
/*     */   {
/* 208 */     String className = this.m_binder.get("dDocClass");
/* 209 */     DataResultSet profiles = SharedObjects.getTable("DocumentProfiles");
/* 210 */     Vector classProfiles = new Vector();
/* 211 */     Vector triggerValues = new Vector();
/* 212 */     if ((profiles == null) || (profiles.isEmpty()))
/*     */     {
/* 214 */       return;
/*     */     }
/*     */ 
/* 217 */     for (profiles.first(); profiles.isRowPresent(); profiles.next())
/*     */     {
/* 219 */       String profileDocClass = profiles.getStringValueByName("dDocClass");
/* 220 */       if (!className.equals(profileDocClass))
/*     */         continue;
/* 222 */       classProfiles.add(profiles.getStringValueByName("dpName"));
/* 223 */       triggerValues.add(profiles.getStringValueByName("dpTriggerValue"));
/*     */     }
/*     */ 
/* 228 */     this.m_binder.putLocal("schViewName", "ProfileTriggerValues");
/* 229 */     this.m_binder.putLocal("editViewValueAction", "delete");
/* 230 */     DataBinder workBinder = new DataBinder();
/* 231 */     workBinder.putLocal("dProfileTriggerOrder", "1");
/* 232 */     FieldInfo[] origInfos = this.m_workspace.getColumnList("ProfileTriggerValues");
/* 233 */     Vector origInfoVector = new IdcVector();
/* 234 */     origInfoVector.addAll(Arrays.asList(origInfos));
/* 235 */     DataResultSet prepSet = new DataResultSet();
/* 236 */     prepSet.mergeFieldsWithFlags(origInfoVector, 0);
/* 237 */     for (int i = 0; i < triggerValues.size(); ++i)
/*     */     {
/* 239 */       workBinder.putLocal("dProfileTriggerValue", (String)triggerValues.get(i));
/* 240 */       Vector v = prepSet.createRow(workBinder);
/* 241 */       prepSet.addRow(v);
/*     */     }
/* 243 */     prepSet.first();
/* 244 */     this.m_binder.addResultSet("ProfileTriggerValues", prepSet);
/* 245 */     executeService("EDIT_SCHEMA_VIEW_VALUES_SUB");
/*     */ 
/* 248 */     for (int i = 0; i < classProfiles.size(); ++i)
/*     */     {
/* 250 */       this.m_binder.putLocal("dpName", (String)classProfiles.get(i));
/* 251 */       executeService("DELETE_DOCPROFILE_SUB");
/*     */     }
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void createDMSTable()
/*     */     throws DataException, ServiceException
/*     */   {
/* 259 */     String tableName = this.m_binder.get("dDocMetaSet");
/*     */ 
/* 262 */     if (WorkspaceUtils.doesTableExist(this.m_workspace, tableName, null))
/*     */     {
/* 264 */       String msg = LocaleUtils.encodeMessage("csDMSTableExists", null, tableName);
/*     */ 
/* 266 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 269 */     FieldInfo fi = new FieldInfo();
/* 270 */     fi.m_name = "dID";
/* 271 */     fi.m_type = 3;
/*     */ 
/* 273 */     this.m_workspace.createTable(tableName, new FieldInfo[] { fi }, new String[] { "dID" });
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void dropDMSTable()
/*     */     throws DataException, ServiceException
/*     */   {
/* 280 */     String tableName = this.m_binder.get("dDocMetaSet");
/*     */ 
/* 283 */     FieldInfo[] fis = null;
/*     */     try
/*     */     {
/* 286 */       fis = this.m_workspace.getColumnList(tableName);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*     */     }
/*     */ 
/* 293 */     if ((fis == null) || (fis.length == 0))
/*     */     {
/* 295 */       String msg = LocaleUtils.encodeMessage("csDMSTableDoesNotExists", null, tableName);
/*     */ 
/* 297 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 301 */     if (fis.length > 1)
/*     */     {
/* 303 */       String msg = LocaleUtils.encodeMessage("csDMSTableNotEmpty", null, tableName);
/*     */ 
/* 305 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 309 */     MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 310 */     for (metaData.first(); metaData.isRowPresent(); metaData.next())
/*     */     {
/* 312 */       if (!tableName.equals(metaData.getDocMetaSet()))
/*     */         continue;
/* 314 */       String msg = LocaleUtils.encodeMessage("csDMSTableContainsField", null, tableName, metaData.getName());
/*     */ 
/* 316 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 321 */     if (DocClassUtils.isSetUsed(tableName))
/*     */     {
/* 323 */       String msg = LocaleUtils.encodeMessage("csDMSTablePartOfDocClass", null, tableName);
/*     */ 
/* 325 */       createServiceException(null, msg);
/*     */     }
/*     */ 
/* 328 */     this.m_workspace.deleteTable(tableName);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void commitChangesInDocClassDef() throws DataException, ServiceException
/*     */   {
/* 334 */     String rsname = this.m_currentAction.getParamAt(0);
/* 335 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet(rsname);
/* 336 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 338 */       this.m_binder.putLocal("dDocClass", rset.getStringValueByName("dDocClass"));
/* 339 */       this.m_binder.putLocal("dDocMetaSet", rset.getStringValueByName("dDocMetaSet"));
/* 340 */       String status = rset.getStringValueByName("dStatus");
/*     */ 
/* 342 */       if ("REMOVE".equals(status))
/*     */       {
/* 344 */         executeService("DEL_DMS_FROM_DOCCLASS_SUB");
/*     */       } else {
/* 346 */         if (!"ADD".equals(status))
/*     */           continue;
/* 348 */         executeService("ADD_DMS_TO_DOCCLASS_SUB");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 355 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98186 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocClassService
 * JD-Core Version:    0.5.4
 */