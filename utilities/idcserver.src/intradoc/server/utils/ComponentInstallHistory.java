/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.VersionInfo;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DataLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.GenericTracingCallback;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ComponentInstallHistory
/*     */ {
/*     */   public String m_dir;
/*     */   public boolean m_isChanged;
/*     */   public GenericTracingCallback m_traceCallback;
/*     */   public DataBinder m_listBinder;
/*     */   public DataBinder m_newBinder;
/*  63 */   public String m_tstamp = null;
/*     */ 
/*     */   public ComponentInstallHistory(String dir, DataBinder newBinder, String tstamp, GenericTracingCallback traceCallback)
/*     */   {
/*  68 */     assert (dir != null);
/*  69 */     this.m_dir = dir;
/*  70 */     this.m_newBinder = newBinder;
/*  71 */     this.m_tstamp = tstamp;
/*  72 */     this.m_traceCallback = traceCallback;
/*  73 */     this.m_listBinder = new DataBinder();
/*     */   }
/*     */ 
/*     */   public void compareAndUpdateComponents(String cmpDir, Map<String, String> props)
/*     */     throws ServiceException, DataException
/*     */   {
/*  79 */     String listFile = this.m_dir + ComponentListUtils.computeListingFileName("history_");
/*  80 */     Map lastMap = null;
/*  81 */     int r = FileUtils.checkFile(listFile, true, false);
/*  82 */     if (r == 0)
/*     */     {
/*  85 */       this.m_traceCallback.report(6, new Object[] { "Read history file " + listFile, null });
/*     */ 
/*  87 */       this.m_listBinder = ResourceLoader.loadDataBinderFromFileWithFlags(listFile, 16);
/*     */ 
/*  92 */       DataResultSet drset = (DataResultSet)this.m_listBinder.getResultSet("ComponentHistory");
/*  93 */       int lstRow = drset.getNumRows() - 1;
/*  94 */       if (lstRow >= 0)
/*     */       {
/*  96 */         drset.setCurrentRow(lstRow);
/*  97 */         lastMap = drset.getCurrentRowMap();
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 102 */     listFile = ComponentListUtils.computeListingFilePath(cmpDir);
/* 103 */     DataBinder curBinder = ComponentListUtils.readListingFile(listFile, null, null);
/* 104 */     long curVersion = NumberUtils.parseLong(curBinder.getLocal("versionTS"), 0L);
/* 105 */     long newVersion = NumberUtils.parseLong(this.m_newBinder.getLocal("versionTS"), 0L);
/*     */ 
/* 107 */     DataBinder oldBinder = null;
/* 108 */     long lastVersion = 0L;
/* 109 */     boolean isUpdateFromHome = true;
/* 110 */     if ((lastMap != null) && (curVersion > 0L))
/*     */     {
/* 113 */       newVersion = NumberUtils.parseLong(this.m_newBinder.getLocal("versionTS"), 0L);
/* 114 */       lastVersion = NumberUtils.parseLong((String)lastMap.get("versionTS"), 0L);
/*     */ 
/* 116 */       isUpdateFromHome = newVersion != lastVersion;
/* 117 */       if (isUpdateFromHome)
/*     */       {
/* 120 */         String filename = this.m_dir + (String)lastMap.get("filename");
/* 121 */         r = FileUtils.checkFile(filename, true, false);
/* 122 */         if (r == 0)
/*     */         {
/* 124 */           oldBinder = ComponentListUtils.readListingFile(filename, null, null);
/* 125 */           this.m_traceCallback.report(6, new Object[] { "Loaded latest version " + lastVersion + " of component listing from " + filename, null });
/*     */         }
/*     */         else
/*     */         {
/* 130 */           this.m_traceCallback.report(4, new Object[] { "Unable to locate latest version " + lastVersion + " of component listing from " + filename, null });
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 138 */     List delComponents = new ArrayList();
/* 139 */     List updatedComponents = new ArrayList();
/* 140 */     List addedComponents = new ArrayList();
/* 141 */     if (isUpdateFromHome)
/*     */     {
/* 143 */       this.m_isChanged = true;
/* 144 */       DataResultSet curComponents = (DataResultSet)curBinder.getResultSet("Components");
/* 145 */       DataResultSet newComponents = (DataResultSet)this.m_newBinder.getResultSet("Components");
/*     */ 
/* 147 */       int curNameIndex = ResultSetUtils.getIndexMustExist(curComponents, "name");
/* 148 */       int newNameIndex = ResultSetUtils.getIndexMustExist(newComponents, "name");
/*     */ 
/* 151 */       DataResultSet renamedComponentsTable = SharedObjects.getTable("RenamedComponents");
/* 152 */       if (renamedComponentsTable == null)
/*     */       {
/* 154 */         String resourcesDirname = LegacyDirectoryLocator.getResourcesDirectory();
/* 155 */         String resourcesFilename = resourcesDirname + "core/tables/std_resources.htm";
/* 156 */         resourcesFilename = FileUtils.fileSlashes(resourcesFilename);
/* 157 */         ResourceContainer rc = new ResourceContainer();
/* 158 */         DataLoader.cacheResourceFile(rc, resourcesFilename);
/* 159 */         Table table = (Table)rc.m_tables.get("RenamedComponents");
/* 160 */         if (table == null)
/*     */         {
/* 162 */           Report.trace(null, "unable to locate RenamedComponents table", null);
/*     */         }
/*     */         else
/*     */         {
/* 166 */           renamedComponentsTable = new DataResultSet();
/* 167 */           renamedComponentsTable.init(table);
/*     */         }
/*     */       }
/* 170 */       Map renamedComponents = new HashMap();
/* 171 */       if (renamedComponentsTable != null)
/*     */       {
/* 173 */         int renameOldNameIndex = ResultSetUtils.getIndexMustExist(renamedComponentsTable, "oldComponentName");
/* 174 */         int renameNewNameIndex = ResultSetUtils.getIndexMustExist(renamedComponentsTable, "newComponentName");
/* 175 */         for (renamedComponentsTable.first(); renamedComponentsTable.isRowPresent(); renamedComponentsTable.next())
/*     */         {
/* 177 */           String oldName = renamedComponentsTable.getStringValue(renameOldNameIndex);
/* 178 */           String newName = renamedComponentsTable.getStringValue(renameNewNameIndex);
/* 179 */           renamedComponents.put(newName, oldName);
/*     */         }
/*     */       }
/*     */ 
/* 183 */       MapParameters params = new MapParameters(null);
/* 184 */       for (newComponents.first(); newComponents.isRowPresent(); newComponents.next())
/*     */       {
/* 186 */         Map map = newComponents.getCurrentRowMap();
/* 187 */         String name = (String)map.get("name");
/*     */ 
/* 189 */         List curRow = curComponents.findRow(curNameIndex, name);
/* 190 */         if (curRow == null)
/*     */         {
/* 193 */           String oldName = (String)renamedComponents.get(name);
/* 194 */           if (oldName != null)
/*     */           {
/* 196 */             String oldStatus = ResultSetUtils.findValue(curComponents, "name", oldName, "status");
/* 197 */             if (oldStatus != null)
/*     */             {
/* 199 */               map.put("status", oldStatus);
/* 200 */               String msg = "setting status to " + oldStatus + " for component renamed from " + oldName + " to " + name;
/*     */ 
/* 202 */               Report.trace(null, msg, null);
/*     */             }
/*     */           }
/*     */ 
/* 206 */           params.m_map = map;
/* 207 */           List row = curComponents.createRowAsList(params);
/* 208 */           curComponents.addRowWithList(row);
/* 209 */           addedComponents.add(map);
/*     */         }
/*     */         else
/*     */         {
/* 213 */           Map chValues = updateComponent(map, curComponents, curComponents.getCurrentRow());
/*     */ 
/* 215 */           updatedComponents.add(chValues);
/*     */         }
/*     */       }
/*     */ 
/* 219 */       if (oldBinder != null)
/*     */       {
/* 221 */         DataResultSet oldComponents = (DataResultSet)oldBinder.getResultSet("Components");
/* 222 */         for (oldComponents.first(); oldComponents.isRowPresent(); oldComponents.next())
/*     */         {
/* 224 */           Map map = oldComponents.getCurrentRowMap();
/* 225 */           String name = (String)map.get("name");
/*     */ 
/* 227 */           List oldRow = newComponents.findRow(newNameIndex, name);
/* 228 */           if (oldRow != null) {
/*     */             continue;
/*     */           }
/*     */ 
/* 232 */           delComponents.add(map);
/* 233 */           List row = curComponents.findRow(curNameIndex, name);
/* 234 */           if (row == null)
/*     */             continue;
/* 236 */           Map curMap = curComponents.getCurrentRowMap();
/* 237 */           if (ComponentLocationUtils.isLocal(curMap))
/*     */             continue;
/* 239 */           curComponents.deleteCurrentRow();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 249 */     boolean isChanged = enableAdditionalComponents(curBinder, props);
/* 250 */     if (isChanged)
/*     */     {
/* 252 */       this.m_isChanged = true;
/*     */     }
/*     */ 
/* 256 */     curBinder.putLocal("lastInstallTS", "" + newVersion);
/* 257 */     ComponentListUtils.saveListingFile(listFile, curBinder, 0, null, null);
/* 258 */     this.m_traceCallback.report(6, new Object[] { "Saved modified components listing to directory " + cmpDir, null });
/*     */ 
/* 262 */     Map defaultValues = new HashMap();
/* 263 */     defaultValues.put("updateTs", this.m_tstamp);
/* 264 */     defaultValues.put("newVersionTS", "" + newVersion);
/* 265 */     defaultValues.put("oldVersionTS", "" + lastVersion);
/* 266 */     defaultValues.put("curVersionTS", "" + curVersion);
/* 267 */     saveComponentUpdates(addedComponents, updatedComponents, delComponents, defaultValues);
/*     */   }
/*     */ 
/*     */   public boolean registerAdditionalComponents(DataBinder cmpBinder, Map<String, String> props)
/*     */     throws DataException, ServiceException
/*     */   {
/* 273 */     boolean isChanged = false;
/* 274 */     DataResultSet components = (DataResultSet)cmpBinder.getResultSet("Components");
/* 275 */     String registerComponentsString = (String)props.get("AdditionalRegisteredComponents");
/*     */     int nameIndex;
/*     */     int locationIndex;
/*     */     int statusIndex;
/*     */     int componentTypeIndex;
/*     */     int componentTagIndex;
/*     */     int versionIndex;
/*     */     String mediaComponentsDir;
/* 276 */     if ((registerComponentsString != null) && (components != null))
/*     */     {
/* 278 */       String msg = "The AdditionalRegisteredComponents " + registerComponentsString + " will be registered.";
/* 279 */       this.m_traceCallback.report(6, new Object[] { msg, null });
/*     */ 
/* 281 */       nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 282 */       locationIndex = ResultSetUtils.getIndexMustExist(components, "location");
/* 283 */       statusIndex = ResultSetUtils.getIndexMustExist(components, "status");
/* 284 */       componentTypeIndex = ResultSetUtils.getIndexMustExist(components, "componentType");
/* 285 */       componentTagIndex = ResultSetUtils.getIndexMustExist(components, "componentTags");
/* 286 */       versionIndex = ResultSetUtils.getIndexMustExist(components, "version");
/* 287 */       Properties appProps = SystemUtils.getAppProperties();
/* 288 */       String idcHomeDir = appProps.getProperty("IdcHomeDir");
/* 289 */       mediaComponentsDir = FileUtils.fixDirectorySlashes(idcHomeDir, 78) + "components/";
/*     */ 
/* 294 */       Map historyDefaults = new HashMap();
/* 295 */       historyDefaults.put("updateTs", this.m_tstamp);
/*     */ 
/* 297 */       List registerComponents = StringUtils.makeListFromSequenceSimple(registerComponentsString);
/* 298 */       for (String componentName : registerComponents)
/*     */       {
/*     */         String componentPath;
/* 301 */         if (componentName.endsWith(".hda"))
/*     */         {
/* 303 */           componentPath = componentName;
/*     */         }
/*     */         else
/*     */         {
/*     */           String componentPath;
/* 305 */           if ((componentName.indexOf(47) < 0) && (componentName.indexOf(92) < 0))
/*     */           {
/* 307 */             componentPath = mediaComponentsDir + componentName + "/" + componentName + ".hda";
/*     */           }
/*     */           else
/*     */           {
/* 312 */             IdcMessage message = new IdcMessage();
/* 313 */             throw new ServiceException(null, message);
/*     */           }
/*     */         }
/*     */         String componentPath;
/* 315 */         if (0 != FileUtils.checkFile(componentPath, 1))
/*     */         {
/* 317 */           IdcMessage message = new IdcMessage("csCompWizCompDefFileMissing", new Object[] { componentPath });
/* 318 */           throw new ServiceException(null, message);
/*     */         }
/* 320 */         DataBinder componentBinder = ResourceUtils.readDataBinderFromPath(componentPath);
/* 321 */         componentName = componentBinder.get("ComponentName");
/* 322 */         if (components.findRow(nameIndex, componentName, 0, 0) != null)
/*     */         {
/* 324 */           String currentVersion = components.getStringValue(versionIndex);
/* 325 */           String message = LocaleResources.getString("csCompWizCompAlreadyInstalled", null, new Object[] { componentName, componentName, currentVersion });
/*     */ 
/* 327 */           Report.info(null, message, null);
/*     */         }
/*     */ 
/* 330 */         String componentTags = componentBinder.get("componentTags");
/* 331 */         if (componentTags == null)
/*     */         {
/* 333 */           componentTags = "";
/*     */         }
/* 335 */         componentTags = componentTags + ",home";
/* 336 */         List row = components.createEmptyRowAsList();
/* 337 */         row.set(nameIndex, componentName);
/* 338 */         row.set(locationIndex, componentPath);
/* 339 */         row.set(statusIndex, "Disabled");
/* 340 */         row.set(componentTypeIndex, "home");
/* 341 */         row.set(componentTagIndex, componentTags);
/* 342 */         components.addRowWithList(row);
/* 343 */         isChanged = true;
/*     */       }
/*     */     }
/* 346 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public boolean disableComponents(DataBinder cmpBinder, Map<String, String> props) throws DataException
/*     */   {
/* 351 */     boolean isChanged = false;
/* 352 */     DataResultSet cmpSet = (DataResultSet)cmpBinder.getResultSet("Components");
/* 353 */     String disabledCmpStr = (String)props.get("DisabledComponents");
/*     */     int nameIndex;
/*     */     int statusIndex;
/*     */     Iterator i$;
/* 354 */     if ((disabledCmpStr != null) && (cmpSet != null))
/*     */     {
/* 356 */       this.m_traceCallback.report(6, new Object[] { "The DisabledComponents " + disabledCmpStr + " will be disabled.", null });
/*     */ 
/* 358 */       nameIndex = ResultSetUtils.getIndexMustExist(cmpSet, "name");
/* 359 */       statusIndex = ResultSetUtils.getIndexMustExist(cmpSet, "status");
/* 360 */       List disCmps = StringUtils.makeListFromSequenceSimple(disabledCmpStr);
/* 361 */       for (i$ = disCmps.iterator(); i$.hasNext(); ) { Object k = i$.next();
/*     */ 
/* 363 */         String name = (String)k;
/* 364 */         Vector v = cmpSet.findRow(nameIndex, name);
/* 365 */         if (v != null)
/*     */         {
/* 367 */           cmpSet.setCurrentValue(statusIndex, "Disabled");
/* 368 */           isChanged = true;
/*     */         }
/*     */         else
/*     */         {
/* 372 */           this.m_traceCallback.report(6, new Object[] { "The DisabledComponent " + name + " does not exist in the component listing file.", null });
/*     */         } }
/*     */ 
/*     */     }
/*     */ 
/* 377 */     return isChanged;
/*     */   }
/*     */ 
/*     */   public boolean enableAdditionalComponents(DataBinder cmpBinder, Map<String, String> props)
/*     */     throws DataException
/*     */   {
/* 383 */     boolean isChanged = false;
/* 384 */     DataResultSet cmpSet = (DataResultSet)cmpBinder.getResultSet("Components");
/* 385 */     String extraEnabledCmpStr = (String)props.get("AdditionalEnabledComponents");
/*     */     int nameIndex;
/*     */     int statusIndex;
/*     */     Iterator i$;
/* 386 */     if ((extraEnabledCmpStr != null) && (cmpSet != null))
/*     */     {
/* 388 */       this.m_traceCallback.report(6, new Object[] { "The AdditionalEnabledComponents " + extraEnabledCmpStr + " will be enabled.", null });
/*     */ 
/* 390 */       nameIndex = ResultSetUtils.getIndexMustExist(cmpSet, "name");
/* 391 */       statusIndex = ResultSetUtils.getIndexMustExist(cmpSet, "status");
/* 392 */       List enCmps = StringUtils.makeListFromSequenceSimple(extraEnabledCmpStr);
/* 393 */       for (i$ = enCmps.iterator(); i$.hasNext(); ) { Object k = i$.next();
/*     */ 
/* 395 */         String name = (String)k;
/* 396 */         Vector v = cmpSet.findRow(nameIndex, name);
/* 397 */         if (v != null)
/*     */         {
/* 399 */           cmpSet.setCurrentValue(statusIndex, "Enabled");
/* 400 */           isChanged = true;
/*     */         }
/*     */         else
/*     */         {
/* 404 */           this.m_traceCallback.report(6, new Object[] { "The AdditionalEnabledComponent " + name + " does not exist in the componetn listing file.", null });
/*     */         } }
/*     */ 
/*     */ 
/*     */     }
/*     */ 
/* 410 */     return isChanged;
/*     */   }
/*     */ 
/*     */   protected Map updateComponent(Map<String, String> map, DataResultSet curComponents, int curRowIndex)
/*     */     throws DataException
/*     */   {
/* 417 */     Map curMap = curComponents.getCurrentRowMap();
/*     */ 
/* 420 */     String status = (String)curMap.get("status");
/* 421 */     map.put("status", status);
/*     */ 
/* 424 */     MapParameters params = new MapParameters(map);
/* 425 */     List newRow = curComponents.createRowAsList(params);
/* 426 */     curComponents.setRowWithList(newRow, curRowIndex);
/*     */ 
/* 429 */     Map chMap = new HashMap();
/* 430 */     for (String key : curMap.keySet())
/*     */     {
/* 432 */       String newVal = (String)map.get(key);
/* 433 */       String curVal = (String)curMap.get(key);
/*     */ 
/* 435 */       IdcStringBuilder builder = new IdcStringBuilder();
/* 436 */       builder.append("new: ");
/* 437 */       if (newVal != null)
/*     */       {
/* 439 */         builder.append(newVal);
/*     */       }
/* 441 */       builder.append(" old: ");
/* 442 */       if (curVal != null)
/*     */       {
/* 444 */         builder.append(curVal);
/*     */       }
/*     */ 
/* 447 */       chMap.put(key, builder.toString());
/*     */     }
/* 449 */     return chMap;
/*     */   }
/*     */ 
/*     */   public Map addComponentListing() throws DataException
/*     */   {
/* 454 */     DataResultSet drset = (DataResultSet)this.m_listBinder.getResultSet("ComponentHistory");
/* 455 */     if (drset == null)
/*     */     {
/* 457 */       drset = createComponentListing();
/* 458 */       this.m_listBinder.addResultSet("ComponentHistory", drset);
/*     */     }
/*     */ 
/* 462 */     Map map = new HashMap();
/* 463 */     Parameters params = new MapParameters(map);
/*     */ 
/* 465 */     String versionTS = this.m_newBinder.getLocal("versionTS");
/* 466 */     if (versionTS == null)
/*     */     {
/* 468 */       versionTS = "0";
/*     */     }
/* 470 */     map.put("versionTS", versionTS);
/*     */ 
/* 473 */     Date dte = new Date();
/* 474 */     String installTS = LocaleUtils.formatODBC(dte);
/* 475 */     map.put("installTS", installTS);
/*     */ 
/* 478 */     map.put("productVersion", VersionInfo.getProductVersion());
/*     */ 
/* 481 */     String filename = ComponentListUtils.computeListingFileName(this.m_tstamp);
/* 482 */     map.put("filename", filename);
/*     */ 
/* 484 */     Vector row = drset.createRow(params);
/* 485 */     drset.addRow(row);
/*     */ 
/* 487 */     return map;
/*     */   }
/*     */ 
/*     */   protected DataResultSet createComponentListing()
/*     */   {
/* 492 */     String[] clmns = { "versionTS", "filename", "installTS", "productVersion" };
/*     */ 
/* 494 */     DataResultSet drset = new DataResultSet(clmns);
/* 495 */     return drset;
/*     */   }
/*     */ 
/*     */   public void saveLists() throws ServiceException, DataException
/*     */   {
/* 500 */     if (!this.m_isChanged) {
/*     */       return;
/*     */     }
/* 503 */     Map rowMap = addComponentListing();
/* 504 */     String saveFile = (String)rowMap.get("filename");
/* 505 */     FileUtils.checkOrCreateDirectory(this.m_dir, 1);
/* 506 */     ResourceUtils.serializeDataBinderWithEncoding(this.m_dir, saveFile, this.m_newBinder, 1, null);
/*     */ 
/* 508 */     ResourceUtils.serializeDataBinderWithEncoding(this.m_dir, ComponentListUtils.computeListingFileName("history_"), this.m_listBinder, 1, null);
/*     */   }
/*     */ 
/*     */   protected DataResultSet getOrCreateComponentHistoryTable()
/*     */     throws DataException
/*     */   {
/* 517 */     DataResultSet newComponents = (DataResultSet)this.m_newBinder.getResultSet("Components");
/* 518 */     String[] historyClmns = { "updateTs", "action", "oldVersionTS", "newVersionTS" };
/* 519 */     int size = newComponents.getNumFields();
/* 520 */     String[] clmns = new String[size + historyClmns.length];
/* 521 */     int count = 0;
/* 522 */     for (int i = 0; i < size; ++count)
/*     */     {
/* 524 */       clmns[count] = newComponents.getFieldName(i);
/*     */ 
/* 522 */       ++i;
/*     */     }
/*     */ 
/* 527 */     for (int i = 0; i < historyClmns.length; ++count)
/*     */     {
/* 529 */       clmns[count] = historyClmns[i];
/*     */ 
/* 527 */       ++i;
/*     */     }
/*     */ 
/* 532 */     DataResultSet drset = (DataResultSet)this.m_listBinder.getResultSet("ComponentUpdates");
/* 533 */     if (drset == null)
/*     */     {
/* 535 */       drset = new DataResultSet(clmns);
/* 536 */       this.m_listBinder.addResultSet("ComponentUpdates", drset);
/*     */     }
/*     */ 
/* 543 */     return drset;
/*     */   }
/*     */ 
/*     */   protected void saveComponentUpdates(List addedComponents, List updatedComponents, List delComponents, Map defaultValues)
/*     */     throws DataException
/*     */   {
/* 549 */     DataResultSet drset = getOrCreateComponentHistoryTable();
/* 550 */     MapParameters defaultParams = new MapParameters(defaultValues);
/* 551 */     addComponentHistoryRows(drset, defaultParams, addedComponents, "add");
/* 552 */     addComponentHistoryRows(drset, defaultParams, updatedComponents, "update");
/* 553 */     addComponentHistoryRows(drset, defaultParams, delComponents, "delete");
/*     */   }
/*     */ 
/*     */   protected void addComponentHistoryRows(DataResultSet drset, MapParameters defaultValues, List cmpList, String action)
/*     */     throws DataException
/*     */   {
/* 559 */     MapParameters params = new MapParameters(null, defaultValues);
/* 560 */     defaultValues.m_map.put("action", action);
/*     */ 
/* 562 */     int size = cmpList.size();
/* 563 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 565 */       Map map = (Map)cmpList.get(i);
/* 566 */       params.m_map = map;
/*     */ 
/* 568 */       Vector row = drset.createRow(params);
/* 569 */       drset.addRow(row);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 575 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101744 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.ComponentInstallHistory
 * JD-Core Version:    0.5.4
 */