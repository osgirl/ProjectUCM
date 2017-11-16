/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.DocProfileData;
/*     */ import intradoc.shared.DocProfileScriptUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DocProfileStorage
/*     */   implements ProfileStorage
/*     */ {
/*     */   protected String m_type;
/*     */   protected String m_metadataSetName;
/*     */   protected String m_storageName;
/*     */   protected String m_dir;
/*     */   protected String m_name;
/*     */   protected boolean m_isDataCached;
/*     */   protected DataBinder m_binder;
/*     */   protected long m_lastLoadedTs;
/*     */   protected Hashtable m_map;
/*     */   protected Vector m_triggerList;
/*     */   protected String m_triggerField;
/*     */   protected Vector m_globalList;
/*     */   protected String m_globalKey;
/*     */   protected String m_filePrefix;
/*     */   protected String m_tableName;
/*     */   protected String m_sharedTableName;
/*     */   protected DataResultSet m_resultSet;
/*     */   protected String[] m_columns;
/*  83 */   public static final String[] DOCPROFILE_COLUMNS = { "dpName", "dpDescription", "dpTriggerValue", "dpDisplayLabel", "dDocClass" };
/*     */ 
/*  93 */   public static final String[] DOCRULE_COLUMNS = { "dpRuleName", "dpRuleDescription" };
/*     */ 
/*     */   public DocProfileStorage()
/*     */   {
/*  32 */     this.m_type = null;
/*     */ 
/*  35 */     this.m_metadataSetName = null;
/*     */ 
/*  38 */     this.m_storageName = null;
/*     */ 
/*  41 */     this.m_dir = null;
/*     */ 
/*  44 */     this.m_name = null;
/*     */ 
/*  47 */     this.m_isDataCached = false;
/*     */ 
/*  50 */     this.m_binder = null;
/*     */ 
/*  53 */     this.m_lastLoadedTs = -2L;
/*     */ 
/*  56 */     this.m_map = null;
/*     */ 
/*  59 */     this.m_triggerList = null;
/*  60 */     this.m_triggerField = null;
/*     */ 
/*  64 */     this.m_globalList = null;
/*  65 */     this.m_globalKey = "dpRuleIsGlobal";
/*     */ 
/*  68 */     this.m_filePrefix = "";
/*     */ 
/*  71 */     this.m_tableName = null;
/*     */ 
/*  74 */     this.m_sharedTableName = null;
/*     */ 
/*  77 */     this.m_resultSet = null;
/*     */ 
/*  80 */     this.m_columns = null;
/*     */   }
/*     */ 
/*     */   public void init(String type, Properties props)
/*     */     throws DataException, ServiceException
/*     */   {
/* 104 */     if (type.equals("profile"))
/*     */     {
/* 106 */       this.m_tableName = "DocumentProfiles";
/* 107 */       this.m_name = "dpprofiles";
/* 108 */       this.m_columns = DOCPROFILE_COLUMNS;
/*     */     }
/*     */     else
/*     */     {
/* 112 */       this.m_tableName = "DocumentRules";
/* 113 */       this.m_name = "dprules";
/* 114 */       this.m_columns = DOCRULE_COLUMNS;
/*     */     }
/*     */ 
/* 117 */     this.m_type = type;
/* 118 */     this.m_filePrefix = (type + "_");
/*     */ 
/* 120 */     this.m_isDataCached = true;
/*     */ 
/* 122 */     this.m_sharedTableName = this.m_tableName;
/*     */ 
/* 124 */     this.m_metadataSetName = props.getProperty("psMetadataSet");
/* 125 */     this.m_storageName = props.getProperty("psName");
/*     */ 
/* 127 */     initDir();
/*     */   }
/*     */ 
/*     */   public void initDir() throws ServiceException
/*     */   {
/* 132 */     this.m_dir = DocProfileUtils.getDocumentDir();
/* 133 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(this.m_dir, 2, true);
/*     */   }
/*     */ 
/*     */   public void load() throws DataException, ServiceException
/*     */   {
/* 138 */     if (!this.m_isDataCached)
/*     */     {
/* 140 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 146 */       FileUtils.reserveDirectory(this.m_dir);
/*     */ 
/* 148 */       DataBinder binder = readListingFile(null);
/*     */ 
/* 151 */       if (this.m_sharedTableName.equals("DocumentProfiles"))
/*     */       {
/* 153 */         DataResultSet drset = (DataResultSet)binder.getResultSet(this.m_sharedTableName);
/* 154 */         if ((drset != null) && (!drset.isEmpty()) && (drset.getNumFields() < DOCPROFILE_COLUMNS.length) && (drset.getFieldInfoIndex("dDocClass") < 0))
/*     */         {
/* 156 */           String[] columnNames = { "dDocClass" };
/* 157 */           String[] defaultValues = { "Base" };
/* 158 */           ResultSetUtils.addColumnsWithDefaultValues(drset, null, defaultValues, columnNames);
/* 159 */           writeListingFile(binder, null);
/*     */         }
/*     */       }
/*     */ 
/* 163 */       loadFromBinder(binder);
/*     */     }
/*     */     finally
/*     */     {
/* 167 */       FileUtils.releaseDirectory(this.m_dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Properties getConfiguration()
/*     */     throws ServiceException
/*     */   {
/* 176 */     Properties localData = this.m_binder.getLocalData();
/* 177 */     localData = (Properties)localData.clone();
/* 178 */     return localData;
/*     */   }
/*     */ 
/*     */   public String getConfigValue(String key) throws ServiceException
/*     */   {
/* 183 */     return this.m_binder.getLocal(key);
/*     */   }
/*     */ 
/*     */   public void updateConfigValue(String key, String value)
/*     */     throws DataException, ServiceException
/*     */   {
/* 189 */     DataBinder binder = readListingFile(null);
/* 190 */     binder.putLocal(key, value);
/*     */ 
/* 193 */     writeListingFile(binder, null);
/*     */ 
/* 196 */     loadFromBinder(binder);
/*     */   }
/*     */ 
/*     */   public DocProfileData getData(String name, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 204 */     if ((this.m_map == null) || (this.m_map.isEmpty()))
/*     */     {
/*     */       try
/*     */       {
/* 208 */         DataBinder binder = readListingFile(null);
/* 209 */         loadFromBinder(binder);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 213 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/* 216 */     DocProfileData profileData = (DocProfileData)this.m_map.get(name.toLowerCase());
/* 217 */     return profileData;
/*     */   }
/*     */ 
/*     */   public void createOrUpdate(String name, DataBinder data, boolean isNew, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 223 */     if ((name.indexOf(92) >= 0) || (name.indexOf(47) >= 0))
/*     */     {
/* 225 */       char slash = (name.indexOf(92) >= 0) ? '\\' : '/';
/* 226 */       String msg = LocaleUtils.encodeMessage("csDpNameInvalid_" + this.m_type, null, name, Character.valueOf(slash));
/* 227 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 230 */     deleteProfileKruft(data);
/*     */ 
/* 233 */     DataBinder binder = readListingFile(cxt);
/* 234 */     DataResultSet rset = (DataResultSet)binder.getResultSet(this.m_tableName);
/* 235 */     if (rset == null)
/*     */     {
/* 238 */       rset = new DataResultSet(this.m_columns);
/* 239 */       binder.addResultSet(this.m_tableName, rset);
/*     */     }
/*     */ 
/* 243 */     String lookupKey = name.toLowerCase();
/* 244 */     Vector values = rset.findRow(0, lookupKey);
/* 245 */     if ((isNew) && (values != null))
/*     */     {
/* 247 */       String msg = LocaleUtils.encodeMessage("csDpNameNotUnique_" + this.m_type, null, name);
/* 248 */       throw new ServiceException(msg);
/*     */     }
/*     */ 
/* 251 */     if ((this.m_type.equals("profile")) && (data.getAllowMissing("dDocClass") == null))
/*     */     {
/* 254 */       data.putLocal("dDocClass", "");
/*     */     }
/* 256 */     Vector row = rset.createRow(data);
/* 257 */     if (values == null)
/*     */     {
/* 259 */       rset.addRow(row);
/*     */     }
/*     */     else
/*     */     {
/* 263 */       int index = rset.getCurrentRow();
/* 264 */       rset.setRowValues(row, index);
/*     */     }
/*     */ 
/* 267 */     Workspace ws = null;
/* 268 */     if ((cxt != null) && (cxt instanceof Service))
/*     */     {
/* 270 */       ws = ((Service)cxt).getWorkspace();
/*     */     }
/* 272 */     Object[] params = new Object[4];
/* 273 */     params[0] = name;
/* 274 */     params[1] = binder;
/* 275 */     params[2] = data;
/* 276 */     params[3] = ((isNew) ? Boolean.TRUE : Boolean.FALSE);
/* 277 */     cxt.setCachedObject("docProfileCreateOrUpdate:params", params);
/* 278 */     PluginFilters.filter("docProfileCreateOrUpdate", ws, binder, cxt);
/*     */ 
/* 281 */     String filename = this.m_filePrefix + lookupKey;
/* 282 */     writeDefinitionFile(filename, data, cxt);
/* 283 */     writeListingFile(binder, cxt);
/*     */ 
/* 285 */     loadFromBinder(binder);
/*     */   }
/*     */ 
/*     */   public void deleteItem(String name, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 292 */     DataBinder binder = readListingFile(cxt);
/* 293 */     DataResultSet rset = (DataResultSet)binder.getResultSet(this.m_tableName);
/* 294 */     if (rset == null)
/*     */     {
/* 298 */       throw new ServiceException("!csDpDefMissing_" + this.m_type);
/*     */     }
/*     */ 
/* 301 */     String lookupKey = name.toLowerCase();
/* 302 */     String filename = this.m_filePrefix + lookupKey;
/* 303 */     Vector row = rset.findRow(0, lookupKey);
/* 304 */     if (row != null)
/*     */     {
/* 306 */       rset.deleteCurrentRow();
/*     */ 
/* 308 */       Workspace ws = null;
/* 309 */       if ((cxt != null) && (cxt instanceof Service))
/*     */       {
/* 311 */         ws = ((Service)cxt).getWorkspace();
/*     */       }
/* 313 */       Object[] params = new Object[2];
/* 314 */       params[0] = name;
/* 315 */       params[1] = binder;
/* 316 */       cxt.setCachedObject("docProfileDelete:params", params);
/* 317 */       PluginFilters.filter("docProfileDelete", ws, binder, cxt);
/*     */ 
/* 320 */       writeListingFile(binder, cxt);
/*     */ 
/* 323 */       DataBinder serviceBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 324 */       deleteDefinitionFile(filename, serviceBinder, cxt);
/*     */ 
/* 327 */       loadFromBinder(binder);
/*     */     }
/*     */     else
/*     */     {
/* 331 */       String msg = LocaleUtils.encodeMessage("csDpNameNonexistent_" + this.m_type, null, name);
/* 332 */       throw new ServiceException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataResultSet getListingSet(ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 341 */     DataResultSet drset = SharedObjects.getTable(this.m_sharedTableName);
/* 342 */     if (drset == null)
/*     */     {
/* 344 */       drset = new DataResultSet(DOCPROFILE_COLUMNS);
/*     */     }
/* 346 */     return drset;
/*     */   }
/*     */ 
/*     */   public Vector getTriggerMapList(ExecutionContext cxt) throws ServiceException
/*     */   {
/* 351 */     return this.m_triggerList;
/*     */   }
/*     */ 
/*     */   public Vector getGlobalRules(ExecutionContext cxt) throws ServiceException
/*     */   {
/* 356 */     return this.m_globalList;
/*     */   }
/*     */ 
/*     */   public String getMetadataSetName()
/*     */   {
/* 364 */     return this.m_metadataSetName;
/*     */   }
/*     */ 
/*     */   public String getStorageName()
/*     */   {
/* 369 */     return this.m_storageName;
/*     */   }
/*     */ 
/*     */   public DataBinder readListingFile(ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 377 */     return DocProfileUtils.readListingFile(this.m_dir, this.m_name, false);
/*     */   }
/*     */ 
/*     */   public void writeListingFile(DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 383 */     DocProfileUtils.writeListingFile(this.m_dir, this.m_name, binder);
/*     */   }
/*     */ 
/*     */   public DataBinder readDefinitionFile(String filename, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 392 */     return DocProfileUtils.readDefinitionFile(this.m_dir, filename, false);
/*     */   }
/*     */ 
/*     */   public void writeDefinitionFile(String filename, DataBinder binder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 398 */     DocProfileUtils.writeDefinitionFile(this.m_dir, filename, binder);
/*     */   }
/*     */ 
/*     */   public void deleteDefinitionFile(String filename, DataBinder serviceBinder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 404 */     DocProfileUtils.deleteDefinitionFile(this.m_dir, filename, serviceBinder);
/*     */   }
/*     */ 
/*     */   public void loadFromBinder(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 412 */     if (!this.m_isDataCached)
/*     */     {
/* 414 */       return;
/*     */     }
/*     */ 
/* 417 */     DataResultSet drset = (DataResultSet)binder.getResultSet(this.m_tableName);
/* 418 */     boolean defExists = DataBinderUtils.getBoolean(binder, "defFileExists", false);
/* 419 */     Hashtable map = new Hashtable();
/* 420 */     Vector globals = new IdcVector();
/* 421 */     Vector triggers = new IdcVector();
/*     */ 
/* 423 */     DataResultSet sharedSet = SharedObjects.getTable(this.m_sharedTableName);
/* 424 */     boolean isSharedPresent = (sharedSet != null) && (!sharedSet.isEmpty());
/* 425 */     if ((!defExists) && (isSharedPresent))
/*     */     {
/* 430 */       Throwable t = new ServiceException("Debug exception for profile tracking.");
/* 431 */       Report.error("system", t, "csDpLoadError", new Object[] { this.m_tableName, DocProfileUtils.getDocumentDir() });
/*     */ 
/* 433 */       return;
/*     */     }
/*     */ 
/* 436 */     if (drset == null)
/*     */     {
/* 439 */       drset = new DataResultSet(this.m_columns);
/*     */     }
/*     */ 
/* 447 */     String triggerField = null;
/* 448 */     if (this.m_type.equals("profile"))
/*     */     {
/* 450 */       triggerField = binder.getLocal("dpTriggerField");
/*     */     }
/*     */ 
/* 453 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, this.m_columns[0]);
/* 454 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 456 */       String name = drset.getStringValue(nameIndex);
/* 457 */       updateData(name, map, globals, triggers, triggerField);
/*     */     }
/* 459 */     drset.first();
/*     */ 
/* 461 */     this.m_resultSet = drset;
/* 462 */     this.m_map = map;
/* 463 */     this.m_globalList = globals;
/* 464 */     this.m_triggerField = triggerField;
/* 465 */     this.m_triggerList = triggers;
/* 466 */     this.m_binder = binder;
/* 467 */     this.m_lastLoadedTs = DocProfileUtils.checkFile(this.m_dir, this.m_name);
/*     */ 
/* 469 */     SharedObjects.putTable(this.m_sharedTableName, this.m_resultSet);
/*     */   }
/*     */ 
/*     */   protected void updateData(String name, Hashtable profileMap, Vector globals, Vector triggerList, String triggerField)
/*     */     throws ServiceException
/*     */   {
/* 475 */     String lookupKey = name.toLowerCase();
/* 476 */     String filename = this.m_filePrefix + lookupKey;
/* 477 */     boolean isReload = true;
/* 478 */     long timestamp = DocProfileUtils.checkFile(this.m_dir, filename);
/*     */ 
/* 480 */     DocProfileData profile = null;
/* 481 */     if (this.m_map != null)
/*     */     {
/* 483 */       profile = (DocProfileData)this.m_map.get(lookupKey);
/* 484 */       if (profile != null)
/*     */       {
/* 486 */         long lastLoadedTs = profile.getLastLoadedTs();
/* 487 */         isReload = lastLoadedTs != timestamp;
/*     */       }
/*     */     }
/*     */ 
/* 491 */     if (isReload)
/*     */     {
/* 493 */       DataBinder profileData = readDefinitionFile(filename, null);
/* 494 */       profile = new DocProfileData(this.m_columns[0]);
/* 495 */       profile.init(profileData, timestamp);
/*     */     }
/* 497 */     profileMap.put(lookupKey, profile);
/*     */ 
/* 499 */     if (this.m_type.equals("rule"))
/*     */     {
/* 501 */       boolean isGlobal = StringUtils.convertToBool(profile.getValue(this.m_globalKey), false);
/* 502 */       if (isGlobal)
/*     */       {
/* 504 */         globals.addElement(profile);
/*     */       }
/*     */     } else {
/* 507 */       if (!this.m_type.equals("profile"))
/*     */         return;
/* 509 */       String triggerValue = profile.getData().getLocal("dpTriggerValue");
/* 510 */       if (triggerValue == null)
/*     */       {
/* 512 */         triggerValue = "";
/*     */       }
/* 514 */       String[] triggerMap = new String[3];
/* 515 */       triggerMap[0] = triggerValue;
/* 516 */       triggerMap[1] = name;
/* 517 */       triggerMap[2] = this.m_storageName;
/* 518 */       triggerList.addElement(triggerMap);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteProfileKruft(DataBinder data)
/*     */   {
/* 525 */     data.removeResultSet("DocumentRules");
/* 526 */     data.removeResultSet("DocumentProfiles");
/*     */ 
/* 528 */     List suffixList = DocProfileScriptUtils.getTableSuffixes(this.m_type);
/* 529 */     Map newMap = new HashMap();
/* 530 */     Map rsetMap = data.getResultSets();
/* 531 */     Set set = rsetMap.keySet();
/* 532 */     for (String key : set)
/*     */     {
/* 534 */       boolean isAccepted = key.startsWith("Dp");
/* 535 */       if (!isAccepted)
/*     */       {
/* 537 */         for (String suffix : suffixList)
/*     */         {
/* 539 */           if (key.endsWith(suffix))
/*     */           {
/* 541 */             isAccepted = true;
/* 542 */             break;
/*     */           }
/*     */         }
/*     */       }
/* 546 */       if (isAccepted)
/*     */       {
/* 548 */         Object rset = rsetMap.get(key);
/* 549 */         newMap.put(key, rset);
/*     */       }
/*     */     }
/* 552 */     data.setResultSets(newMap);
/*     */   }
/*     */ 
/*     */   public String getDir() throws ServiceException
/*     */   {
/* 557 */     return this.m_dir;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 568 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 100168 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.DocProfileStorage
 * JD-Core Version:    0.5.4
 */