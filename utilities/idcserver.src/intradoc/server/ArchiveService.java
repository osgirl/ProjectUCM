/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.serialize.DataBinderLocalizer;
/*      */ import intradoc.server.archive.ArchiveHandler;
/*      */ import intradoc.server.archive.ArchiveImportStateInformation;
/*      */ import intradoc.server.archive.ArchiveUtils;
/*      */ import intradoc.server.archive.ArchiverMonitor;
/*      */ import intradoc.server.archive.ReplicationData;
/*      */ import intradoc.server.archive.TransferUtils;
/*      */ import intradoc.server.proxy.OutgoingProviderMonitor;
/*      */ import intradoc.shared.ArchiveCollections;
/*      */ import intradoc.shared.ArchiveData;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.ComponentClassFactory;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.File;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ArchiveService extends Service
/*      */ {
/*   42 */   public CollectionData m_currentCollection = null;
/*   43 */   public DataBinder m_collectionData = null;
/*   44 */   public DataResultSet m_archivesSet = null;
/*   45 */   public String m_archiveName = null;
/*   46 */   public String m_archiveDir = null;
/*   47 */   public String m_archiveExportDir = null;
/*      */ 
/*   50 */   public ArchiveImportStateInformation m_importStateInfo = null;
/*      */ 
/*   52 */   public final int NO_LOCK = 0;
/*   53 */   public final int ARCHIVES_LOCK = 1;
/*   54 */   public final int COLLECTIONS_LOCK = 2;
/*   55 */   public final int REGISTER_LOCK = 3;
/*      */ 
/*   57 */   public final int NO_DATA = 0;
/*   58 */   public final int COLLECTION_DATA = 1;
/*   59 */   public final int ARCHIVE_DATA = 2;
/*      */ 
/*   61 */   public int m_dataType = 0;
/*   62 */   public int m_lockType = 0;
/*      */ 
/*   66 */   protected final String[][] FUNCTION_INFO = { { "getArchives", "1", "1" }, { "getBatchFiles", "2", "1" }, { "deleteBatchFile", "2", "1" }, { "getBatchFileDocuments", "2", "1" }, { "deleteBatchFileDocuments", "2", "1" }, { "getBatchSchema", "2", "1" }, { "getBatchValues", "2", "1" }, { "copyArchive", "2", "1" }, { "addArchive", "2", "1" }, { "editArchive", "2", "1" }, { "editArchiveData", "2", "1" }, { "deleteArchive", "2", "1" }, { "addCollection", "0", "2" }, { "removeCollection", "0", "2" }, { "addProxiedCollection", "0", "2" }, { "removeProxiedCollection", "0", "2" }, { "registerExporter", "2", "3" }, { "registerImporter", "2", "3" }, { "removeExporter", "1", "3" }, { "removeImporter", "1", "3" }, { "getReplicationData", "1", "3" }, { "exportArchive", "0", "0" }, { "importArchive", "0", "0" }, { "cancelArchive", "0", "0" }, { "importDocument", "0", "0" } };
/*      */ 
/*      */   public void createHandlersForService()
/*      */     throws ServiceException, DataException
/*      */   {
/*  103 */     super.createHandlersForService();
/*  104 */     createHandlers("ArchiveService");
/*      */   }
/*      */ 
/*      */   public void initHandlers()
/*      */     throws DataException, ServiceException
/*      */   {
/*  110 */     this.m_importStateInfo = ((ArchiveImportStateInformation)ComponentClassFactory.createClassInstance("ArchiveImportStateInformation", "intradoc.server.archive.ArchiveImportStateInformation", ""));
/*      */ 
/*  112 */     this.m_importStateInfo.init(this.m_binder, this.m_workspace, this);
/*  113 */     setCachedObject("importState", this.m_importStateInfo);
/*  114 */     super.initHandlers();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void executeArchiveMethod() throws DataException, ServiceException
/*      */   {
/*  120 */     int numParams = this.m_currentAction.getNumParams();
/*  121 */     if (numParams < 1)
/*      */     {
/*  123 */       throw new DataException("!csArchiverRequiresArg");
/*      */     }
/*  125 */     String function = this.m_currentAction.getParamAt(0);
/*      */ 
/*  127 */     if (numParams > 2)
/*      */     {
/*  129 */       String dataTypeStr = this.m_currentAction.getParamAt(1);
/*  130 */       String lockTypeStr = this.m_currentAction.getParamAt(2);
/*  131 */       if (dataTypeStr.equals("archiveData"))
/*      */       {
/*  133 */         this.m_dataType = 2;
/*      */       }
/*  135 */       else if (dataTypeStr.equals("collectionData"))
/*      */       {
/*  137 */         this.m_dataType = 1;
/*      */       }
/*      */ 
/*  140 */       if (lockTypeStr.equals("arLock"))
/*      */       {
/*  142 */         this.m_lockType = 1;
/*      */       }
/*  144 */       else if (lockTypeStr.equals("crwLock"))
/*      */       {
/*  146 */         this.m_lockType = 2;
/*      */       }
/*  148 */       else if (lockTypeStr.equals("crLock"))
/*      */       {
/*  150 */         this.m_lockType = 3;
/*      */       }
/*      */     }
/*      */ 
/*  154 */     boolean isProxiable = false;
/*  155 */     if (numParams > 3)
/*      */     {
/*  157 */       isProxiable = StringUtils.convertToBool(this.m_currentAction.getParamAt(3), false);
/*      */     }
/*      */ 
/*  160 */     String lockDir = null;
/*      */     try
/*      */     {
/*  163 */       lockDir = getCollectionAndLockDirectory(this.m_lockType);
/*  164 */       if (lockDir != null)
/*      */       {
/*  166 */         FileUtils.reserveDirectory(lockDir, true);
/*      */       }
/*  168 */       setData(this.m_dataType);
/*      */ 
/*  170 */       if ((this.m_lockType != 3) && (this.m_currentCollection != null) && (this.m_currentCollection.isProxied()))
/*      */       {
/*  172 */         if (!isProxiable)
/*      */         {
/*  174 */           throw new DataException(LocaleUtils.encodeMessage("csRequestCannotBeProxied", null, function));
/*      */         }
/*      */ 
/*  177 */         performProxiedRequest();
/*      */       }
/*      */       else
/*      */       {
/*  181 */         super.doCode(function);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/*  186 */       if (lockDir != null)
/*      */       {
/*  188 */         FileUtils.releaseDirectory(lockDir, true);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void notifyMoniker()
/*      */   {
/*  195 */     if (this.m_currentCollection == null)
/*      */       return;
/*  197 */     String monikerString = this.m_currentCollection.getMoniker();
/*  198 */     MonikerWatcher.notifyChanged(monikerString);
/*  199 */     MonikerWatcher.updateMoniker(monikerString, this.m_binder);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getArchives()
/*      */     throws ServiceException, DataException
/*      */   {
/*  210 */     this.m_binder.addResultSet("Archives", this.m_archivesSet);
/*      */ 
/*  213 */     ReplicationData.loadFromFile();
/*  214 */     DataResultSet exporters = SharedObjects.getTable(ReplicationData.m_tableNames[0]);
/*  215 */     DataResultSet importers = SharedObjects.getTable(ReplicationData.m_tableNames[1]);
/*      */ 
/*  221 */     ArchiveData archiveData = new ArchiveData();
/*  222 */     this.m_binder.addResultSet("ArchiveData", archiveData);
/*  223 */     String dir = this.m_currentCollection.m_location;
/*      */ 
/*  225 */     String[] keys = ArchiveUtils.ARCHIVE_COLUMNS;
/*  226 */     FieldInfo[] fields = ResultSetUtils.createInfoList(this.m_archivesSet, keys, true);
/*  227 */     int numFields = fields.length;
/*  228 */     boolean isNotify = false;
/*  229 */     for (this.m_archivesSet.first(); this.m_archivesSet.isRowPresent(); this.m_archivesSet.next())
/*      */     {
/*  231 */       String name = this.m_archivesSet.getStringValue(fields[0].m_index);
/*      */ 
/*  234 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*  235 */       DataBinder binder = null;
/*      */       try
/*      */       {
/*  238 */         binder = ArchiveUtils.readArchiveFile(dir, name, false);
/*  239 */         boolean isChanged = checkArchive(name, binder, exporters, importers);
/*  240 */         if (isChanged)
/*      */         {
/*  250 */           String msg = LocaleUtils.encodeMessage("csArchiverValidateError", null, name, this.m_currentCollection.m_name);
/*      */ 
/*  252 */           report(msg);
/*      */         }
/*  254 */         DataBinderLocalizer localizer = new DataBinderLocalizer(binder, this);
/*  255 */         localizer.localizeBinder(3);
/*  256 */         binder.send(sw);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  260 */         IdcMessage idcMsg = IdcMessageFactory.lc(e);
/*  261 */         binder = createErrorBinder(LocaleUtils.encodeMessage("csArchiverUnableToRead", LocaleUtils.encodeMessage(idcMsg), name));
/*      */       }
/*      */ 
/*  265 */       Vector values = new IdcVector();
/*  266 */       values.addElement(name);
/*  267 */       for (int i = 1; i < numFields; ++i)
/*      */       {
/*  269 */         String fieldValue = this.m_archivesSet.getStringValue(fields[i].m_index);
/*  270 */         values.addElement(fieldValue);
/*      */       }
/*  272 */       values.addElement(sw.toStringRelease());
/*      */ 
/*  274 */       archiveData.addRow(values);
/*      */     }
/*      */ 
/*  277 */     String monikerString = this.m_currentCollection.getMoniker();
/*  278 */     if (isNotify)
/*      */     {
/*  280 */       MonikerWatcher.notifyChanged(monikerString);
/*      */     }
/*  282 */     MonikerWatcher.updateMoniker(monikerString, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected boolean checkArchive(String archiveName, DataBinder archiveData, DataResultSet exporters, DataResultSet importers)
/*      */     throws ServiceException
/*      */   {
/*  288 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*  289 */     String location = ArchiveUtils.buildLocation(this.m_currentCollection.m_name, archiveName);
/*      */ 
/*  291 */     boolean isChanged = false;
/*  292 */     boolean isAuto = StringUtils.convertToBool(archiveData.getLocal("aIsAutomatedExport"), false);
/*      */ 
/*  294 */     if (isAuto)
/*      */     {
/*  297 */       isChanged = checkRegisteredExporter(archiveData, idcName, true, exporters, location);
/*      */     }
/*      */ 
/*  301 */     String regImporter = archiveData.getLocal("aRegisteredImporter");
/*  302 */     if ((regImporter != null) && (regImporter.equals(idcName)))
/*      */     {
/*  304 */       Vector v = importers.findRow(0, location);
/*  305 */       if (v == null)
/*      */       {
/*  307 */         isChanged = true;
/*  308 */         archiveData.putLocal("aRegisteredImporter", "");
/*  309 */         archiveData.putLocal("aImportLogonUser", "");
/*      */       }
/*      */     }
/*      */ 
/*  313 */     return isChanged;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void copyArchive() throws ServiceException, DataException
/*      */   {
/*  319 */     String newName = this.m_binder.getLocal("CopyName").toLowerCase();
/*  320 */     String newPath = this.m_binder.getLocal("CopyPath");
/*  321 */     newPath = FileUtils.directorySlashes(newPath);
/*  322 */     String newDirName = FileUtils.getDirectory(newPath);
/*  323 */     newDirName = FileUtils.directorySlashes(newDirName);
/*      */ 
/*  325 */     copyAndResetArchive(this.m_archiveDir, this.m_archiveExportDir, newDirName, newName);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editArchive()
/*      */     throws ServiceException, DataException
/*      */   {
/*  332 */     Vector v = this.m_archivesSet.findRow(0, this.m_archiveName);
/*  333 */     if (v != null)
/*      */     {
/*  335 */       this.m_archivesSet.deleteCurrentRow();
/*      */     }
/*      */ 
/*  339 */     v = this.m_archivesSet.createRow(this.m_binder);
/*  340 */     this.m_archivesSet.addRow(v);
/*  341 */     this.m_archivesSet.first();
/*      */ 
/*  344 */     ArchiveUtils.writeCollectionData(this.m_currentCollection.m_location, this.m_collectionData);
/*      */ 
/*  346 */     String msg = LocaleUtils.encodeMessage("csArchiverEditingArchive", null, this.m_archiveName, this.m_currentCollection.m_name);
/*      */ 
/*  348 */     report(msg);
/*      */ 
/*  350 */     notifyMoniker();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addArchive()
/*      */     throws ServiceException, DataException
/*      */   {
/*  358 */     boolean isCopyFrom = StringUtils.convertToBool(this.m_binder.getLocal("IsCopyFrom"), false);
/*      */ 
/*  360 */     boolean doesExist = false;
/*      */ 
/*  363 */     Vector v = this.m_archivesSet.findRow(0, this.m_archiveName);
/*  364 */     if (v != null)
/*      */     {
/*  366 */       createServiceException(null, LocaleUtils.encodeMessage("csArchiverAlreadyExists", null, this.m_archiveName));
/*      */     }
/*      */ 
/*  371 */     File df = FileUtilsCfgBuilder.getCfgFile(this.m_archiveDir, "Archive", true);
/*  372 */     if (df.exists())
/*      */     {
/*  374 */       doesExist = true;
/*  375 */       if ((df.isDirectory() == true) && (!isCopyFrom))
/*      */       {
/*  378 */         createServiceException(null, LocaleUtils.encodeMessage("csArchiverDirectoryExists", null, this.m_archiveDir));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  384 */     v = this.m_archivesSet.createRow(this.m_binder);
/*  385 */     this.m_archivesSet.addRow(v);
/*  386 */     this.m_archivesSet.first();
/*      */ 
/*  389 */     ArchiveUtils.writeCollectionData(this.m_currentCollection.m_location, this.m_collectionData);
/*      */ 
/*  391 */     if (!doesExist)
/*      */     {
/*  394 */       if (isCopyFrom)
/*      */       {
/*  396 */         String copyPath = this.m_binder.getLocal("CopyPath");
/*  397 */         String copyDirPath = FileUtils.getDirectory(copyPath);
/*  398 */         copyAndResetArchive(copyDirPath, null, this.m_currentCollection.m_location, this.m_archiveName);
/*      */       }
/*      */       else
/*      */       {
/*  402 */         createArchiveProperties(this.m_archiveDir, this.m_archiveExportDir);
/*      */       }
/*      */     }
/*      */ 
/*  406 */     String msg = LocaleUtils.encodeMessage("csArchiverAdding", null, this.m_archiveName, this.m_currentCollection.m_name);
/*      */ 
/*  408 */     report(msg);
/*      */ 
/*  410 */     notifyMoniker();
/*      */   }
/*      */ 
/*      */   protected void copyAndResetArchive(String copyDirPath, String copyContentDirPath, String newDirPath, String newName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  416 */     File copyDir = FileUtilsCfgBuilder.getCfgFile(copyDirPath, null, true);
/*  417 */     String newPath = FileUtils.getAbsolutePath(newDirPath, newName.toLowerCase());
/*  418 */     File newDir = FileUtilsCfgBuilder.getCfgFile(newPath, null, true);
/*  419 */     FileUtils.copyDirectoryWithFlags(copyDir, newDir, 1, null, 1);
/*      */ 
/*  421 */     if ((copyContentDirPath != null) && (!copyContentDirPath.equalsIgnoreCase(copyDirPath)))
/*      */     {
/*  423 */       File copyContentDir = new File(copyContentDirPath);
/*  424 */       String newExportDir = newDirPath;
/*  425 */       int flag = 2;
/*  426 */       if (FileUtils.storeInDB(newExportDir))
/*      */       {
/*  428 */         newExportDir = LegacyDirectoryLocator.getCollectionExport();
/*  429 */         flag = 1;
/*      */       }
/*  431 */       File newContentDir = new File(newExportDir, newName.toLowerCase());
/*  432 */       FileUtils.copyDirectoryWithFlags(copyContentDir, newContentDir, 1, null, flag);
/*      */     }
/*      */ 
/*  435 */     DataBinder archiveData = ArchiveUtils.readArchiveFile(newDirPath, newName, false);
/*  436 */     String[] keys = { "aIsAutomatedExport", "aRegisteredImporter", "aImportLogonUser", "aIsAutomatedTransfer" };
/*      */ 
/*  438 */     int len = keys.length;
/*  439 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  441 */       archiveData.putLocal(keys[i], "");
/*      */     }
/*  443 */     ArchiveUtils.writeArchiveFile(newDir.getPath(), archiveData, true);
/*      */ 
/*  445 */     String msg = LocaleUtils.encodeMessage("csArchiverCopying", null, newName, this.m_currentCollection.m_name);
/*      */ 
/*  447 */     report(msg);
/*      */   }
/*      */ 
/*      */   public void createArchiveProperties(String dir, String exportDir)
/*      */     throws ServiceException
/*      */   {
/*  453 */     FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 0, true);
/*  454 */     DataBinder archiveData = createArchiveContextData();
/*      */ 
/*  457 */     if (!exportDir.equalsIgnoreCase(dir))
/*      */     {
/*  459 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(exportDir, 1, true);
/*      */     }
/*      */ 
/*  463 */     archiveData.putLocal("aCopyWebDocuments", "1");
/*      */     try
/*      */     {
/*  466 */       PluginFilters.filter("archiveCreationAdditionalProperties", this.m_workspace, archiveData, this);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  470 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  473 */     ArchiveUtils.writeArchiveFile(dir, archiveData, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteArchive()
/*      */     throws ServiceException, DataException
/*      */   {
/*  482 */     DataBinder archiveData = ArchiveUtils.readArchiveFile(this.m_currentCollection.m_location, this.m_archiveName, false);
/*      */ 
/*  485 */     boolean isExported = StringUtils.convertToBool(archiveData.getLocal("aIsAutomatedExport"), false);
/*      */ 
/*  487 */     boolean isImported = false;
/*  488 */     String importers = archiveData.getLocal("aRegisteredImporter");
/*  489 */     if ((importers != null) && (importers.length() > 0))
/*      */     {
/*  491 */       isImported = true;
/*      */     }
/*      */ 
/*  494 */     if ((isExported) || (isImported))
/*      */     {
/*  496 */       createServiceException(null, "!csArchiverReplicating");
/*      */     }
/*      */ 
/*  499 */     boolean isAutoTransfer = StringUtils.convertToBool(archiveData.getLocal("aIsAutomatedTransfer"), false);
/*      */ 
/*  501 */     if (isAutoTransfer)
/*      */     {
/*  503 */       createServiceException(null, "!csArchiverIsAutomated");
/*      */     }
/*      */ 
/*  507 */     FieldInfo info = new FieldInfo();
/*  508 */     this.m_archivesSet.getFieldInfo("aArchiveName", info);
/*  509 */     if (this.m_archivesSet.findRow(info.m_index, this.m_archiveName) == null)
/*      */     {
/*  512 */       createServiceException(null, LocaleUtils.encodeMessage("csArchiverNoLongerExists", null, this.m_archiveName));
/*      */     }
/*      */ 
/*  515 */     this.m_archivesSet.deleteCurrentRow();
/*      */ 
/*  518 */     File f1 = FileUtilsCfgBuilder.getCfgFile(this.m_archiveDir, "Archive", true);
/*  519 */     FileUtils.deleteDirectory(f1, true);
/*      */ 
/*  521 */     File f2 = new File(this.m_archiveExportDir);
/*  522 */     FileUtils.deleteDirectory(f2, true);
/*      */ 
/*  525 */     ArchiveUtils.writeCollectionData(this.m_currentCollection.m_location, this.m_collectionData);
/*      */ 
/*  527 */     String msg = LocaleUtils.encodeMessage("csArchiverDeleting", null, this.m_archiveName, this.m_currentCollection.m_name);
/*      */ 
/*  529 */     report(msg);
/*      */ 
/*  531 */     notifyMoniker();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editArchiveData()
/*      */     throws ServiceException, DataException
/*      */   {
/*  538 */     editArchiveDataForCollection(this.m_currentCollection);
/*      */   }
/*      */ 
/*      */   protected void editArchiveDataForCollection(CollectionData collData)
/*      */     throws ServiceException, DataException
/*      */   {
/*  545 */     DataBinder archiveData = ArchiveUtils.readArchiveFile(collData.m_location, this.m_archiveName, false);
/*      */ 
/*  548 */     String[] msg = new String[1];
/*  549 */     editItems(archiveData, this.m_binder, msg);
/*      */ 
/*  552 */     ArchiveUtils.writeArchiveFile(this.m_archiveDir, archiveData, true);
/*      */ 
/*  554 */     String str = LocaleUtils.encodeMessage("csArchiverEditItems", null, this.m_archiveName, this.m_currentCollection.m_name, msg[0]);
/*      */ 
/*  556 */     report(str);
/*      */ 
/*  558 */     notifyMoniker();
/*      */   }
/*      */ 
/*      */   protected void editItems(DataBinder archiveData, DataBinder binder, String[] chMsg)
/*      */     throws DataException, ServiceException
/*      */   {
/*  565 */     String str = "";
/*  566 */     String editItemsStr = binder.getLocal("EditItems");
/*  567 */     Vector editItems = StringUtils.parseArray(editItemsStr, ',', ',');
/*  568 */     boolean isDelete = DataBinderUtils.getBoolean(binder, "isDelete", false);
/*  569 */     int size = editItems.size();
/*  570 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  572 */       String name = (String)editItems.elementAt(i);
/*  573 */       if (isDelete)
/*      */       {
/*  575 */         archiveData.removeLocal(name);
/*  576 */         str = str + "\n" + name;
/*      */       }
/*      */       else {
/*  579 */         String value = binder.getLocal(name);
/*      */ 
/*  582 */         if (value == null)
/*      */         {
/*  585 */           throw new DataException(LocaleUtils.encodeMessage("csUnableToFindValue", null, name));
/*      */         }
/*      */ 
/*  588 */         archiveData.putLocal(name, value);
/*      */ 
/*  590 */         str = str + "\n";
/*  591 */         str = str + name + " = " + value;
/*      */       }
/*      */     }
/*  593 */     chMsg[0] = str;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void exportArchive() throws ServiceException, DataException
/*      */   {
/*  599 */     doArchiving(true, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void importArchive() throws ServiceException, DataException
/*      */   {
/*  605 */     doArchiving(false, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void transferArchive() throws ServiceException, DataException
/*      */   {
/*  611 */     doArchiving(false, true);
/*      */   }
/*      */ 
/*      */   public void doArchiving(boolean isExport, boolean isTransfer) throws ServiceException, DataException
/*      */   {
/*  616 */     if (isExport)
/*      */     {
/*  618 */       this.m_binder.putLocal("IsExport", "1");
/*      */     }
/*  620 */     else if (isTransfer)
/*      */     {
/*  622 */       this.m_binder.putLocal("IsTransfer", "1");
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  627 */       ArchiverMonitor.reserveAndStartArchiving(this.m_binder);
/*  628 */       ArchiverMonitor.watchArchiving();
/*      */ 
/*  632 */       SystemUtils.sleep(1000L);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  636 */       createServiceException(null, e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void cancelArchive() throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  645 */       ArchiverMonitor.cancelArchiving();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  649 */       createServiceException(null, e.getMessage());
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addQueuedAutomatedImport() throws DataException, ServiceException
/*      */   {
/*  656 */     String archiveName = ArchiveUtils.getArchiveName(this.m_binder);
/*  657 */     String collName = ArchiveUtils.getCollectionName(this.m_binder);
/*  658 */     String userName = getUserData().m_name;
/*  659 */     ReplicationData.registerQueuedImport(collName, archiveName, userName, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void importBatch() throws ServiceException, DataException
/*      */   {
/*  665 */     Object obj = ComponentClassFactory.createClassInstance("ArchiveHandler", "intradoc.server.archive.ArchiveHandler", "!csArchiveHandlerError");
/*      */ 
/*  668 */     ArchiveHandler archiver = (ArchiveHandler)obj;
/*  669 */     archiver.initObjects(this.m_workspace, null, null);
/*  670 */     archiver.init(this.m_binder, false);
/*  671 */     archiver.importBatch();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void importDocument() throws ServiceException, DataException
/*      */   {
/*  677 */     Object obj = ComponentClassFactory.createClassInstance("ArchiveHandler", "intradoc.server.archive.ArchiveHandler", "!csArchiveHandlerError");
/*      */ 
/*  680 */     ArchiveHandler archiver = (ArchiveHandler)obj;
/*  681 */     archiver.initObjects(this.m_workspace, null, null);
/*  682 */     archiver.init(this.m_binder, false);
/*  683 */     archiver.importDocument(false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void importTableEntry() throws ServiceException, DataException
/*      */   {
/*  689 */     Object obj = ComponentClassFactory.createClassInstance("ArchiveHandler", "intradoc.server.archive.ArchiveHandler", "!csArchiveHandlerError");
/*      */ 
/*  692 */     ArchiveHandler archiver = (ArchiveHandler)obj;
/*  693 */     archiver.initObjects(this.m_workspace, null, null);
/*  694 */     archiver.init(this.m_binder, false);
/*  695 */     archiver.importTableEntries();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addCollection()
/*      */     throws ServiceException, DataException
/*      */   {
/*  704 */     addRemoveCollection(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeCollection() throws ServiceException, DataException
/*      */   {
/*  710 */     addRemoveCollection(false);
/*      */   }
/*      */ 
/*      */   public void addRemoveCollection(boolean isAdd)
/*      */     throws ServiceException, DataException
/*      */   {
/*  716 */     DataBinder data = ArchiveUtils.readCollectionsData(false);
/*  717 */     DataResultSet rset = (DataResultSet)data.getResultSet(ArchiveCollections.m_tableName);
/*      */ 
/*  719 */     String name = this.m_binder.get("IDC_Name");
/*  720 */     Vector collData = rset.findRow(ArchiveCollections.m_idIndex, name);
/*  721 */     if (isAdd)
/*      */     {
/*  723 */       if (collData != null)
/*      */       {
/*  726 */         createServiceException(null, "!csArchiverCollectionExists");
/*      */       }
/*  728 */       boolean isCreate = StringUtils.convertToBool(this.m_binder.getLocal("IsCreate"), false);
/*  729 */       if (isCreate)
/*      */       {
/*  731 */         String dir = this.m_binder.getLocal("aCollectionLocation");
/*  732 */         ArchiveUtils.createCollection(dir, name);
/*      */       }
/*  734 */       int id = ArchiveCollections.createID();
/*  735 */       this.m_binder.putLocal("IDC_ID", String.valueOf(id));
/*  736 */       String exportDir = this.m_binder.getLocal("aCollectionExportLocation");
/*  737 */       if ((exportDir == null) || (exportDir.length() <= 0))
/*      */       {
/*  739 */         this.m_binder.putLocal("aCollectionExportLocation", this.m_binder.getLocal("aCollectionLocation"));
/*      */       }
/*  741 */       Vector v = rset.createRow(this.m_binder);
/*  742 */       rset.addRow(v);
/*      */     }
/*      */     else
/*      */     {
/*  746 */       if (collData == null)
/*      */       {
/*  749 */         createServiceException(null, "!csArchiverCollectionNoLongerExists");
/*      */       }
/*      */ 
/*  753 */       if (collData.elementAt(0).equals("0"))
/*      */       {
/*  755 */         createServiceException(null, "!csArchiverDefaultCollectionDelete");
/*      */       }
/*      */ 
/*  759 */       String collName = (String)collData.elementAt(ArchiveCollections.m_idIndex);
/*  760 */       Vector[] archives = ReplicationData.getReplicatedArchives(collName);
/*      */ 
/*  762 */       Vector exporters = archives[0];
/*  763 */       Vector importers = archives[1];
/*  764 */       if ((exporters.size() > 0) || (importers.size() > 0))
/*      */       {
/*  767 */         String msg = "!csArchiverUnableToRemove!$\n\n";
/*  768 */         int size = exporters.size();
/*  769 */         if (size > 0)
/*      */         {
/*  771 */           msg = msg + "!csArchiverExportingTo";
/*  772 */           msg = msg + "!$\n";
/*  773 */           for (int i = 0; i < size; ++i)
/*      */           {
/*  775 */             msg = msg + "!$- ->";
/*  776 */             msg = msg + "!$" + (String)exporters.elementAt(i);
/*  777 */             msg = msg + "!$\n";
/*      */           }
/*      */         }
/*  780 */         size = importers.size();
/*  781 */         if (size > 0)
/*      */         {
/*  783 */           msg = msg + "!csArchiverImportingFrom";
/*  784 */           msg = msg + "!$\n";
/*  785 */           for (int i = 0; i < size; ++i)
/*      */           {
/*  787 */             msg = msg + "!$- ->";
/*  788 */             msg = msg + "!$" + (String)importers.elementAt(i);
/*  789 */             msg = msg + "!$\n";
/*      */           }
/*      */         }
/*      */ 
/*  793 */         createServiceException(null, msg);
/*      */       }
/*      */ 
/*  796 */       rset.deleteCurrentRow();
/*      */     }
/*      */ 
/*  799 */     ArchiveCollections collections = (ArchiveCollections)SharedObjects.getTable(ArchiveCollections.m_tableName);
/*      */ 
/*  801 */     collections.load(data);
/*  802 */     ArchiveUtils.writeCollections(collections);
/*      */ 
/*  804 */     String str = null;
/*  805 */     if (isAdd)
/*      */     {
/*  807 */       str = LocaleUtils.encodeMessage("csArchiverAddCollection", null, name);
/*      */     }
/*      */     else
/*      */     {
/*  811 */       str = LocaleUtils.encodeMessage("csArchiverDeleteCollection", null, name);
/*      */     }
/*  813 */     report(str);
/*      */ 
/*  815 */     this.m_binder.addResultSet(ArchiveCollections.m_tableName, collections);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getCollectionFromDataSource()
/*      */     throws DataException, ServiceException
/*      */   {
/*  822 */     String providerName = this.m_binder.get("ProviderName");
/*  823 */     Provider provider = Providers.getProvider(providerName);
/*  824 */     if ((provider.isInError()) || (!provider.isEnabled()))
/*      */     {
/*  826 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverCollectionNoProviderConnection", null, providerName));
/*      */     }
/*      */ 
/*  831 */     Workspace ws = (Workspace)provider.getProvider();
/*  832 */     DataBinder params = new DataBinder();
/*  833 */     params.putLocal("dRTFileName", "collection.hda");
/*  834 */     ResultSet rset = null;
/*      */     try
/*      */     {
/*  837 */       rset = ws.createResultSet("QdirRunTimeConfigData", params);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  841 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverCollectionNotFoundInProvider", null, providerName));
/*      */     }
/*      */ 
/*  844 */     if (rset.isEmpty())
/*      */     {
/*  846 */       throw new ServiceException(LocaleUtils.encodeMessage("csArchiverCollectionNotFoundInProvider", null, providerName));
/*      */     }
/*      */ 
/*  851 */     String[] cols = { "CollectionDir", "CollectionFile" };
/*  852 */     DataResultSet drset = new DataResultSet(cols);
/*  853 */     drset.setDateFormat(LocaleResources.m_odbcFormat);
/*      */ 
/*  855 */     Vector row = new IdcVector(2);
/*  856 */     row.addElement(rset.getStringValue(0));
/*  857 */     row.addElement("collection.hda");
/*      */ 
/*  859 */     drset.addRow(row);
/*  860 */     this.m_binder.addResultSet("Collection", drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void addProxiedCollection() throws DataException, ServiceException
/*      */   {
/*  866 */     boolean isProxied = StringUtils.convertToBool(this.m_binder.getLocal("IsProxied"), false);
/*  867 */     if (!isProxied)
/*      */     {
/*  869 */       return;
/*      */     }
/*      */ 
/*  873 */     DataBinder data = ArchiveUtils.readCollectionsData(false);
/*  874 */     DataResultSet rset = (DataResultSet)data.getResultSet(ArchiveCollections.m_tableName);
/*      */ 
/*  876 */     String psName = this.m_binder.get("psIDC_Name");
/*  877 */     String collName = this.m_binder.get("IDC_Name");
/*      */ 
/*  879 */     Vector collData = rset.findRow(ArchiveCollections.m_idIndex, collName);
/*  880 */     if (collData != null)
/*      */     {
/*  883 */       createServiceException(null, "!csArchiverCollectionExists");
/*      */     }
/*      */ 
/*  886 */     int id = ArchiveCollections.createID();
/*  887 */     this.m_binder.putLocal("IDC_ID", String.valueOf(id));
/*  888 */     this.m_binder.putLocal("IDC_Name", collName);
/*  889 */     this.m_binder.putLocal("aCollectionLocation", "idc://" + psName);
/*  890 */     this.m_binder.putLocal("aCollectionExportLocation", "idc://" + psName);
/*  891 */     this.m_binder.putLocal("aVaultDir", "");
/*  892 */     this.m_binder.putLocal("aWeblayoutDir", "");
/*  893 */     Vector v = rset.createRow(this.m_binder);
/*  894 */     rset.addRow(v);
/*      */ 
/*  896 */     ArchiveCollections collections = (ArchiveCollections)SharedObjects.getTable(ArchiveCollections.m_tableName);
/*      */ 
/*  898 */     collections.load(data);
/*  899 */     ArchiveUtils.writeCollections(collections);
/*      */ 
/*  901 */     String msg = LocaleUtils.encodeMessage("csArchiverAddProxiedCollection", null, collName);
/*  902 */     report(msg);
/*      */ 
/*  904 */     this.m_binder.addResultSet(ArchiveCollections.m_tableName, collections);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getReplicationData() throws ServiceException, DataException
/*      */   {
/*  910 */     ReplicationData.loadFromFile();
/*  911 */     for (int i = 0; i < ReplicationData.m_tableNames.length; ++i)
/*      */     {
/*  913 */       String tableName = ReplicationData.m_tableNames[i];
/*  914 */       DataResultSet rset = SharedObjects.getTable(tableName);
/*  915 */       if (rset == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  920 */       createReplicationInfoSet(rset, tableName);
/*  921 */       this.m_binder.addResultSet(tableName, rset);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createReplicationInfoSet(DataResultSet rset, String tableName)
/*      */     throws ServiceException, DataException
/*      */   {
/*  928 */     ArchiveCollections colls = (ArchiveCollections)SharedObjects.getTable("ArchiveCollections");
/*      */ 
/*  931 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*      */ 
/*  934 */     tableName = tableName.toLowerCase();
/*  935 */     boolean isExport = true;
/*  936 */     boolean isTransfer = false;
/*  937 */     boolean isQueued = false;
/*  938 */     if (tableName.indexOf("export") < 0)
/*      */     {
/*  940 */       isExport = false;
/*      */     }
/*  942 */     if (tableName.indexOf("transfer") >= 0)
/*      */     {
/*  944 */       isTransfer = true;
/*      */     }
/*  946 */     else if (tableName.indexOf("queue") >= 0)
/*      */     {
/*  948 */       isQueued = true;
/*      */     }
/*      */ 
/*  952 */     Vector finfos = ResultSetUtils.createFieldInfo(new String[] { "aStatus" }, 30);
/*  953 */     rset.mergeFieldsWithFlags(finfos, 2);
/*      */ 
/*  955 */     int statusIndex = ((FieldInfo)finfos.get(0)).m_index;
/*  956 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  958 */       String location = rset.getStringValue(0);
/*  959 */       String[] locData = ArchiveUtils.parseLocation(location);
/*      */ 
/*  961 */       String collectionName = locData[0];
/*  962 */       String archiveName = locData[1];
/*      */ 
/*  967 */       boolean isConnected = false;
/*  968 */       CollectionData collectionData = colls.getCollectionData(collectionName);
/*  969 */       if (collectionData != null)
/*      */       {
/*  971 */         if (isTransfer)
/*      */         {
/*  973 */           isConnected = checkAutomatedTransferStatus(idcName, collectionData, archiveName);
/*      */         }
/*      */         else
/*      */         {
/*  977 */           DataBinder archiveData = ArchiveUtils.readArchiveFile(collectionData.m_location, archiveName, true);
/*      */ 
/*  980 */           if (isExport)
/*      */           {
/*  982 */             boolean isAuto = StringUtils.convertToBool(archiveData.getLocal("aIsAutomatedExport"), false);
/*      */ 
/*  984 */             if (isAuto)
/*      */             {
/*  986 */               isConnected = checkRegisteredExporter(archiveData, idcName, false, null, null);
/*      */             }
/*      */           }
/*  989 */           else if (isQueued)
/*      */           {
/*  992 */             isConnected = archiveData.getLocal("IDC_Name") != null;
/*      */           }
/*      */           else
/*      */           {
/*  996 */             String importer = archiveData.getLocal("aRegisteredImporter");
/*  997 */             if ((importer != null) && (importer.equals(idcName)))
/*      */             {
/*  999 */               isConnected = true;
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/* 1004 */       rset.setCurrentValue(statusIndex, String.valueOf(isConnected));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean checkAutomatedTransferStatus(String idcName, CollectionData srcCollection, String srcArchive)
/*      */   {
/* 1010 */     boolean isConnected = false;
/*      */     try
/*      */     {
/* 1013 */       if (srcCollection.isProxied())
/*      */       {
/* 1016 */         isConnected = checkProxiedState(srcCollection.m_name);
/*      */       }
/*      */       else
/*      */       {
/* 1020 */         DataBinder archiveData = ArchiveUtils.readArchiveFile(srcCollection.m_location, srcArchive, true);
/*      */ 
/* 1023 */         String target = archiveData.getLocal("aTargetArchive");
/* 1024 */         if (target == null)
/*      */         {
/* 1026 */           return isConnected;
/*      */         }
/* 1028 */         String[] loc = ArchiveUtils.parseLocation(target);
/* 1029 */         String pxName = loc[0];
/* 1030 */         CollectionData targetCollection = ArchiveUtils.getCollection(pxName);
/* 1031 */         if (targetCollection == null)
/*      */         {
/* 1033 */           return isConnected;
/*      */         }
/* 1035 */         if (targetCollection.isProxied())
/*      */         {
/* 1038 */           isConnected = checkProxiedState(pxName);
/*      */         }
/*      */         else
/*      */         {
/* 1042 */           isConnected = true;
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1048 */       Report.trace(null, "ArchiveService.checkAutomatedTransferStatus:", e);
/*      */     }
/* 1050 */     return isConnected;
/*      */   }
/*      */ 
/*      */   protected boolean checkProxiedState(String psName)
/*      */   {
/* 1055 */     Provider provider = OutgoingProviderMonitor.getOutgoingProvider(psName);
/* 1056 */     if (provider == null)
/*      */     {
/* 1059 */       return false;
/*      */     }
/*      */ 
/* 1062 */     return !provider.checkState("IsBadConnection", false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getBatchFiles()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1071 */     DataBinder batchData = ArchiveUtils.readExportsFile(this.m_archiveDir, null);
/*      */ 
/* 1074 */     DataResultSet rset = (DataResultSet)batchData.getResultSet("BatchFiles");
/* 1075 */     if (rset == null)
/*      */     {
/* 1078 */       return;
/*      */     }
/*      */ 
/* 1081 */     this.m_binder.addResultSet("BatchFiles", rset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getBatchSchema()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1088 */     boolean isTable = DataBinderUtils.getBoolean(this.m_binder, "isTableArchive", false);
/*      */ 
/* 1091 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 1092 */     if ((filename == null) || (filename.length() == 0))
/*      */     {
/* 1094 */       throw new DataException("!csArchiverBatchFileNotSpecified");
/*      */     }
/*      */ 
/* 1097 */     String batchFilePath = FileUtils.getAbsolutePath(this.m_archiveExportDir, filename);
/* 1098 */     String dir = FileUtils.getDirectory(batchFilePath);
/* 1099 */     String tableName = "DocMetaDefinition";
/* 1100 */     String metaFileName = tableName.toLowerCase() + ".hda";
/* 1101 */     if (isTable)
/*      */     {
/* 1103 */       metaFileName = FileUtils.getName(filename);
/*      */ 
/* 1105 */       int index = metaFileName.lastIndexOf(46);
/* 1106 */       if (index < 0)
/*      */       {
/* 1108 */         String msg = LocaleUtils.encodeMessage("csArchiverBatchTableSchemaFailed", null, filename);
/* 1109 */         createServiceException(null, msg);
/*      */       }
/* 1111 */       tableName = metaFileName.substring(0, index);
/*      */     }
/*      */ 
/* 1115 */     DataBinder metaBinder = null;
/*      */     try
/*      */     {
/* 1119 */       File metaFile = new File(dir, metaFileName);
/* 1120 */       if ((!metaFile.exists()) && (!isTable))
/*      */       {
/* 1122 */         metaFileName = tableName + ".hda";
/*      */       }
/* 1124 */       metaBinder = ResourceUtils.readDataBinder(dir, metaFileName);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1128 */       createServiceException(e, "!csArchiverBatchSchemaFailure");
/*      */     }
/*      */ 
/* 1131 */     DataResultSet drset = (DataResultSet)metaBinder.getResultSet(tableName);
/*      */ 
/* 1133 */     if (drset == null)
/*      */     {
/* 1135 */       drset = (DataResultSet)metaBinder.getResultSet("ExportResults");
/*      */     }
/* 1137 */     if (drset != null)
/*      */     {
/* 1139 */       DataResultSet metaSet = drset;
/* 1140 */       if (isTable)
/*      */       {
/* 1143 */         metaSet = retrieveMetaData(drset);
/*      */       }
/* 1145 */       this.m_binder.addResultSet("BatchFields", metaSet);
/*      */     }
/*      */     else
/*      */     {
/* 1149 */       throw new DataException("!csArchiverUnableToLocateTable");
/*      */     }
/*      */   }
/*      */ 
/*      */   protected DataResultSet retrieveMetaData(DataResultSet drset)
/*      */   {
/* 1155 */     DataResultSet result = new DataResultSet(new String[] { "dName", "dCaption" });
/*      */ 
/* 1157 */     int size = drset.getNumFields();
/* 1158 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1160 */       String dName = drset.getFieldName(i);
/* 1161 */       Vector row = new IdcVector();
/* 1162 */       row.addElement(dName);
/* 1163 */       row.addElement(dName);
/*      */ 
/* 1165 */       result.addRow(row);
/*      */     }
/* 1167 */     return result;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getBatchValues() throws ServiceException, DataException
/*      */   {
/* 1173 */     if (this.m_currentAction.getNumParams() < 1)
/*      */     {
/* 1175 */       throw new DataException("!csArchiverInvalidParamCount");
/*      */     }
/*      */ 
/* 1178 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 1179 */     DataBinder binder = ArchiveUtils.readBatchData(this.m_archiveExportDir, filename);
/* 1180 */     DataResultSet bset = (DataResultSet)binder.getResultSet("ExportResults");
/*      */ 
/* 1182 */     String fieldName = this.m_binder.getLocal("batchFieldName");
/* 1183 */     if (fieldName == null)
/*      */     {
/* 1186 */       throw new DataException("!csArchiverMustDefineFieldName");
/*      */     }
/*      */ 
/* 1189 */     if (fieldName.indexOf(46) > 0)
/*      */     {
/* 1191 */       bset = addTableToFieldName(binder, bset);
/*      */     }
/*      */ 
/* 1194 */     Vector values = ResultSetUtils.loadValuesFromSetEx(bset, fieldName, false);
/*      */ 
/* 1197 */     String optName = "BatchValues";
/* 1198 */     this.m_binder.addOptionList(optName, values);
/* 1199 */     SharedObjects.putOptList(optName, values);
/*      */   }
/*      */ 
/*      */   protected DataResultSet addTableToFieldName(DataBinder binder, DataResultSet drset)
/*      */   {
/* 1206 */     String parentTables = binder.getLocal("parentTables");
/* 1207 */     String table = binder.getLocal("tableName");
/* 1208 */     Vector tables = StringUtils.parseArray(parentTables, ',', '^');
/* 1209 */     tables.insertElementAt(table, 0);
/*      */ 
/* 1211 */     int len = tables.size();
/* 1212 */     int index = -1;
/* 1213 */     DataResultSet results = new DataResultSet();
/* 1214 */     Vector fInfos = new IdcVector();
/* 1215 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1217 */       table = (String)tables.elementAt(i);
/* 1218 */       int numFields = Integer.parseInt(binder.getLocal("numFields" + table));
/* 1219 */       for (int j = 0; j < numFields; ++j)
/*      */       {
/* 1221 */         ++index;
/* 1222 */         FieldInfo fi = new FieldInfo();
/* 1223 */         drset.getIndexFieldInfo(index, fi);
/* 1224 */         String name = fi.m_name;
/* 1225 */         String newName = table + "." + name;
/* 1226 */         fi.m_name = newName;
/* 1227 */         fInfos.addElement(fi);
/*      */       }
/*      */     }
/* 1230 */     results.mergeFieldsWithFlags(fInfos, 0);
/*      */ 
/* 1232 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1234 */       Vector row = drset.getCurrentRowValues();
/* 1235 */       results.addRow(row);
/*      */     }
/* 1237 */     return results;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getBatchProperties() throws ServiceException, DataException
/*      */   {
/* 1243 */     String fileName = this.m_binder.getLocal("aBatchFile");
/* 1244 */     DataBinder binder = ArchiveUtils.readBatchData(this.m_archiveExportDir, fileName);
/*      */ 
/* 1246 */     Properties prop = binder.getLocalData();
/* 1247 */     for (Enumeration en = prop.propertyNames(); en.hasMoreElements(); )
/*      */     {
/* 1249 */       String name = (String)en.nextElement();
/* 1250 */       this.m_binder.putLocal(fileName + "-" + name, prop.getProperty(name));
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getBatchFileDocuments()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1261 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 1262 */     DataResultSet bset = ArchiveUtils.readBatchFile(this.m_archiveExportDir, filename);
/*      */ 
/* 1266 */     DataResultSet bsetCopy = new DataResultSet();
/* 1267 */     bsetCopy.copy(bset);
/* 1268 */     getRowsAt(bsetCopy, "ExportResults");
/*      */   }
/*      */ 
/*      */   protected void getRowsAt(DataResultSet bset, String resultSetName)
/*      */   {
/* 1273 */     int maxRows = 50;
/* 1274 */     String maxStr = this.m_binder.getAllowMissing("MaxRowsPerPage");
/* 1275 */     if ((maxStr != null) && (maxStr.length() > 0))
/*      */     {
/*      */       try
/*      */       {
/* 1279 */         maxRows = Integer.parseInt(maxStr);
/*      */       }
/*      */       catch (Throwable ignore)
/*      */       {
/* 1283 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1285 */           Report.debug("systemparse", null, ignore);
/*      */         }
/*      */       }
/*      */ 
/* 1289 */       if (maxRows == 0)
/*      */       {
/* 1292 */         maxRows = 50;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1297 */     this.m_binder.putLocal("MaxRowsPerPage", String.valueOf(maxRows));
/*      */ 
/* 1300 */     int startRow = 0;
/*      */     try
/*      */     {
/* 1303 */       String startRowStr = this.m_binder.getLocal("StartRow");
/* 1304 */       if (startRowStr != null)
/*      */       {
/* 1306 */         startRow = Integer.parseInt(startRowStr);
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1311 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1313 */         Report.debug("systemparse", null, t);
/*      */       }
/*      */     }
/*      */ 
/* 1317 */     int numRows = bset.getNumRows();
/* 1318 */     if (startRow < 0)
/*      */     {
/* 1320 */       startRow = 0;
/*      */     }
/* 1322 */     else if (startRow > numRows)
/*      */     {
/* 1324 */       startRow = numRows / maxRows * maxRows;
/*      */     }
/*      */ 
/* 1327 */     bset.setCurrentRow(startRow);
/*      */ 
/* 1329 */     DataResultSet rset = new DataResultSet();
/* 1330 */     ResultSetFilter filter = rset.createMaxNumResultSetFilter(maxRows);
/* 1331 */     String key = "dID";
/*      */ 
/* 1333 */     FieldInfo fi = new FieldInfo();
/* 1334 */     bset.getFieldInfo(key, fi);
/* 1335 */     if (fi.m_index == -1)
/*      */     {
/* 1337 */       key = bset.getFieldName(0);
/*      */     }
/* 1339 */     rset.copyFilteredEx(bset, key, filter, false);
/* 1340 */     this.m_binder.addResultSet(resultSetName, rset);
/*      */ 
/* 1343 */     this.m_binder.putLocal("StartRow", String.valueOf(startRow));
/* 1344 */     this.m_binder.putLocal("NumRows", String.valueOf(numRows));
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteBatchFileDocuments()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1355 */     DataResultSet drset = (DataResultSet)this.m_binder.removeResultSet("DeletedRows");
/* 1356 */     if ((drset == null) || (drset.isEmpty()))
/*      */     {
/* 1358 */       throw new DataException("!csArchiverNoDeletedRows");
/*      */     }
/*      */ 
/* 1361 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 1362 */     DataBinder batchData = ArchiveUtils.readBatchData(this.m_archiveExportDir, filename);
/* 1363 */     DataResultSet bset = (DataResultSet)batchData.getResultSet("ExportResults");
/* 1364 */     if (bset == null)
/*      */     {
/* 1366 */       createServiceException(null, "!csArchiverNoInfo");
/*      */     }
/*      */ 
/* 1369 */     if (bset.isEmpty())
/*      */     {
/* 1372 */       this.m_binder.addResultSet("ExportResults", bset);
/* 1373 */       return;
/*      */     }
/*      */ 
/* 1377 */     FieldInfo[] finfo = ResultSetUtils.createInfoList(drset, new String[] { "dID" }, true);
/* 1378 */     int idIndex = finfo[0].m_index;
/*      */ 
/* 1380 */     String[] keys = this.m_importStateInfo.m_allFileFields;
/*      */ 
/* 1382 */     FieldInfo[] fileInfo = ResultSetUtils.createInfoList(drset, keys, false);
/*      */ 
/* 1384 */     for (; drset.isRowPresent(); bset.first())
/*      */     {
/* 1386 */       String str = drset.getStringValue(idIndex);
/* 1387 */       Vector row = bset.findRow(idIndex, str);
/* 1388 */       if (row != null)
/*      */       {
/* 1393 */         Vector values = bset.getCurrentRowValues();
/* 1394 */         bset.deleteCurrentRow();
/*      */ 
/* 1397 */         for (int i = 0; i < this.m_importStateInfo.m_allFileFields.length; ++i)
/*      */         {
/* 1399 */           deleteFile(fileInfo[i], values);
/*      */         }
/*      */       }
/* 1384 */       drset.next();
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1406 */       String numRowsStr = String.valueOf(bset.getNumRows());
/* 1407 */       batchData.putLocal("NumRows", numRowsStr);
/*      */ 
/* 1409 */       ResourceUtils.serializeDataBinder(this.m_archiveExportDir, filename, batchData, true, false);
/*      */ 
/* 1412 */       this.m_binder.putLocal("StartRow", "0");
/* 1413 */       getRowsAt(bset, "ExportResults");
/*      */ 
/* 1416 */       File batchFile = new File(this.m_archiveExportDir, filename);
/* 1417 */       long ts = batchFile.lastModified();
/* 1418 */       ArchiveUtils.addTemporaryResourceCache(filename, batchData, ts);
/*      */ 
/* 1421 */       DataBinder exports = ArchiveUtils.readExportsFile(this.m_archiveDir, null);
/* 1422 */       DataResultSet rset = (DataResultSet)exports.getResultSet("BatchFiles");
/* 1423 */       if ((rset != null) && (!rset.isEmpty()))
/*      */       {
/* 1425 */         FieldInfo info = new FieldInfo();
/* 1426 */         if (!rset.getFieldInfo("aNumDocuments", info))
/*      */         {
/* 1428 */           return;
/*      */         }
/*      */ 
/* 1431 */         Vector row = rset.findRow(0, filename);
/* 1432 */         if (row != null)
/*      */         {
/* 1434 */           row.setElementAt(numRowsStr, info.m_index);
/* 1435 */           ArchiveUtils.writeExportsFile(this.m_archiveDir, exports);
/*      */ 
/* 1437 */           String str = LocaleUtils.encodeMessage("csArchiverDeletingBatch", null, filename, this.m_archiveName, this.m_currentCollection.m_name);
/*      */ 
/* 1439 */           report(str);
/*      */ 
/* 1441 */           String monikerString = this.m_currentCollection.getMoniker();
/* 1442 */           MonikerWatcher.notifyChanged(monikerString);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1448 */       createServiceException(e, LocaleUtils.encodeMessage("csArchiverFileMissing", null, filename));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean deleteFile(FieldInfo info, Vector rowValues)
/*      */   {
/* 1454 */     int fileIndex = info.m_index;
/* 1455 */     if (fileIndex < 0)
/*      */     {
/* 1458 */       return false;
/*      */     }
/*      */ 
/* 1461 */     String filename = (String)rowValues.elementAt(fileIndex);
/*      */     try
/*      */     {
/* 1464 */       if (filename.length() > 0)
/*      */       {
/* 1466 */         String filePath = FileUtils.getAbsolutePath(this.m_archiveExportDir, filename);
/* 1467 */         File aFile = new File(filePath);
/* 1468 */         if (aFile.exists())
/*      */         {
/* 1470 */           aFile.delete();
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Throwable t)
/*      */     {
/* 1476 */       Report.appWarning("archiver", null, LocaleUtils.encodeMessage("syUnableToDeleteFile", t.getMessage(), filename), t);
/*      */     }
/*      */ 
/* 1479 */     return true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void deleteBatchFile()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1495 */     String filename = this.m_binder.getLocal("aBatchFile");
/* 1496 */     if ((filename == null) || (filename.length() == 0))
/*      */     {
/* 1498 */       throw new DataException("!csArchiverBatchFileNotSpecified");
/*      */     }
/*      */ 
/* 1506 */     String archiveName = this.m_binder.get("aArchiveName");
/* 1507 */     String archiveDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_location, archiveName);
/* 1508 */     String archiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_exportLocation, archiveName);
/*      */ 
/* 1510 */     ArchiveUtils.deleteBatchFile(filename, archiveDir, archiveExportDir, this.m_currentCollection);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void registerExporter()
/*      */     throws ServiceException
/*      */   {
/* 1519 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1520 */     String collectionName = this.m_binder.getLocal("IDC_Name");
/*      */ 
/* 1522 */     boolean isFound = checkRegisteredExporter(this.m_binder, idcName, false, null, null);
/* 1523 */     boolean isAuto = StringUtils.convertToBool(this.m_binder.getLocal("aIsAutomatedExport"), false);
/*      */ 
/* 1526 */     boolean isAdd = (isFound) && (isAuto);
/* 1527 */     ReplicationData.registerExporter(collectionName, this.m_archiveName, isAdd);
/*      */ 
/* 1529 */     String msg = "csArchiverRegisterExporter";
/* 1530 */     if (!isAdd)
/*      */     {
/* 1532 */       msg = "csArchiverUnregisterExporter";
/*      */     }
/* 1534 */     msg = LocaleUtils.encodeMessage(msg, null, collectionName, idcName, collectionName);
/* 1535 */     report(msg);
/*      */   }
/*      */ 
/*      */   protected boolean checkRegisteredExporter(DataBinder archiveData, String idcName, boolean isRemove, DataResultSet autoExporters, String location)
/*      */   {
/* 1541 */     String regExportersStr = archiveData.getLocal("aRegisteredExporters");
/* 1542 */     Vector regExporters = StringUtils.parseArray(regExportersStr, ',', ',');
/*      */ 
/* 1544 */     int num = regExporters.size();
/* 1545 */     int expIndex = -1;
/* 1546 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1548 */       String exporter = (String)regExporters.elementAt(i);
/* 1549 */       if (!exporter.equals(idcName))
/*      */         continue;
/* 1551 */       expIndex = i;
/* 1552 */       break;
/*      */     }
/*      */ 
/* 1556 */     if ((expIndex < 0) || (!isRemove))
/*      */     {
/* 1558 */       return expIndex >= 0;
/*      */     }
/*      */ 
/* 1563 */     if (autoExporters != null)
/*      */     {
/* 1565 */       Vector v = autoExporters.findRow(0, location);
/* 1566 */       if (v != null)
/*      */       {
/* 1568 */         return false;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1573 */     regExporters.removeElementAt(expIndex);
/* 1574 */     regExportersStr = StringUtils.createString(regExporters, ',', ',');
/* 1575 */     archiveData.putLocal("aRegisteredExporters", regExportersStr);
/*      */ 
/* 1577 */     if (regExporters.size() == 0)
/*      */     {
/* 1579 */       archiveData.putLocal("aIsAutomatedExport", "false");
/*      */     }
/*      */ 
/* 1582 */     return true;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void registerImporter() throws ServiceException, DataException
/*      */   {
/* 1588 */     boolean isAdd = StringUtils.convertToBool(this.m_binder.getLocal("IsRegister"), false);
/* 1589 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1590 */     String regImport = this.m_currentCollection.m_name;
/* 1591 */     if (!isAdd)
/*      */     {
/* 1594 */       this.m_binder.putLocal("aRegisteredImporter", "");
/* 1595 */       this.m_binder.putLocal("aImportLogonUser", "");
/*      */     }
/*      */     else
/*      */     {
/* 1600 */       this.m_binder.putLocal("aRegisteredImporter", idcName);
/* 1601 */       if (this.m_userData != null)
/*      */       {
/* 1603 */         this.m_binder.putLocal("aImportLogonUser", this.m_userData.m_name);
/*      */       }
/*      */     }
/* 1606 */     ReplicationData.registerImporter(regImport, this.m_archiveName, isAdd);
/*      */ 
/* 1608 */     String msg = "csArchiverRegisterImporter";
/* 1609 */     if (!isAdd)
/*      */     {
/* 1611 */       msg = "csArchiverUnregisterImporter";
/*      */     }
/* 1613 */     String str = LocaleUtils.encodeMessage(msg, null, regImport, this.m_archiveName, idcName);
/* 1614 */     report(str);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeExporter() throws ServiceException
/*      */   {
/* 1620 */     removeAutomater(0);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeImporter() throws ServiceException
/*      */   {
/* 1626 */     removeAutomater(1);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeTransfer() throws ServiceException
/*      */   {
/* 1632 */     removeAutomater(2);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void removeQueuedImport() throws ServiceException
/*      */   {
/* 1638 */     removeAutomater(3);
/*      */   }
/*      */ 
/*      */   protected void removeAutomater(int autoType) throws ServiceException
/*      */   {
/* 1643 */     String location = this.m_binder.getLocal("aArchiveLocation");
/* 1644 */     String[] locData = ArchiveUtils.parseLocation(location);
/*      */ 
/* 1646 */     String collName = locData[0];
/* 1647 */     String archiveName = locData[1];
/*      */ 
/* 1649 */     this.m_binder.putLocal("IDC_Name", collName);
/* 1650 */     this.m_binder.putLocal("aArchiveName", archiveName);
/*      */ 
/* 1652 */     ReplicationData.changeTable(autoType, collName, archiveName, false);
/*      */ 
/* 1654 */     ArchiveCollections colls = (ArchiveCollections)SharedObjects.getTable("ArchiveCollections");
/*      */ 
/* 1656 */     CollectionData collectionData = colls.getCollectionData(collName);
/*      */     try
/*      */     {
/* 1660 */       if ((!ReplicationData.isTransfer(autoType)) && (!ReplicationData.isQueued(autoType)) && (collectionData != null))
/*      */       {
/* 1662 */         updateArchiveAutomationInfo(autoType, archiveName, collectionData);
/*      */       }
/* 1664 */       else if (ReplicationData.isQueued(autoType))
/*      */       {
/* 1666 */         String str = LocaleUtils.encodeMessage("csArchiverRemoveQueuedImport", null, archiveName, collectionData.m_name);
/*      */ 
/* 1668 */         report(str);
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1673 */       if ((collectionData != null) && (!this.m_currentCollection.isProxied()) && (this.m_currentCollection.m_name.equals(collectionData.m_name)))
/*      */       {
/* 1676 */         MonikerWatcher.updateMoniker(collectionData.getMoniker(), this.m_binder);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateArchiveAutomationInfo(int autoType, String archiveName, CollectionData collectionData)
/*      */   {
/* 1684 */     String collDir = collectionData.m_location;
/*      */     try
/*      */     {
/* 1687 */       FileUtils.reserveDirectory(collDir);
/* 1688 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(collDir, archiveName, false);
/*      */ 
/* 1690 */       String msg = null;
/* 1691 */       if (autoType == 0)
/*      */       {
/* 1693 */         msg = "csArchiverRemoveAutoExporter";
/* 1694 */         String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/* 1695 */         checkRegisteredExporter(archiveData, idcName, true, null, null);
/*      */       }
/*      */       else
/*      */       {
/* 1700 */         msg = "csArchiverRemoveAutoImporter";
/* 1701 */         archiveData.putLocal("aRegisteredImporter", "");
/* 1702 */         archiveData.putLocal("aImportLogonUser", "");
/*      */       }
/*      */ 
/* 1705 */       String archiveDir = collDir + archiveName.toLowerCase();
/* 1706 */       ArchiveUtils.writeArchiveFile(archiveDir, archiveData, true);
/*      */ 
/* 1708 */       String str = LocaleUtils.encodeMessage(msg, null, archiveName, collectionData.m_name);
/* 1709 */       report(str);
/*      */ 
/* 1711 */       MonikerWatcher.notifyChanged(collectionData.getMoniker());
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 1716 */       Report.appWarning("archiver", null, LocaleUtils.encodeMessage("csArchiverRemoveAutomationFailed", null, archiveName, collectionData.m_name), ignore);
/*      */     }
/*      */     finally
/*      */     {
/* 1722 */       FileUtils.releaseDirectory(collDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void editTransferOptions()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1732 */     boolean isUpdated = StringUtils.convertToBool(this.m_binder.getLocal("IsUpdatedTransfers"), false);
/* 1733 */     if (isUpdated)
/*      */     {
/* 1735 */       return;
/*      */     }
/*      */ 
/* 1738 */     String str = this.m_binder.getLocal("aIsAutomatedTransfer");
/* 1739 */     boolean isAuto = StringUtils.convertToBool(str, false);
/* 1740 */     boolean isEdit = StringUtils.convertToBool(this.m_binder.getLocal("isEditAutomated"), false);
/* 1741 */     String tOwner = this.m_binder.getLocal("aTransferOwner");
/*      */ 
/* 1743 */     if ((tOwner == null) || (tOwner.trim().length() <= 0) || ((!isEdit) && (!isAuto)))
/*      */       return;
/* 1745 */     String archiveName = this.m_binder.getLocal("aArchiveName");
/* 1746 */     String targetArchive = this.m_binder.getLocal("aTargetArchive");
/* 1747 */     if ((isAuto) && (((targetArchive == null) || (targetArchive.length() == 0))))
/*      */     {
/* 1749 */       createServiceException(null, "!csArchiverTargetNotDefined");
/*      */     }
/*      */ 
/* 1753 */     CollectionData ownerData = ArchiveUtils.getCollection(tOwner);
/* 1754 */     if (isAuto)
/*      */     {
/* 1757 */       if ((isEdit) && (ownerData.isProxied()))
/*      */       {
/* 1759 */         createServiceException(null, "!csArchiverActivateNotOwner");
/*      */       }
/* 1761 */       if ((!isEdit) && (this.m_currentCollection.isProxied()))
/*      */       {
/* 1763 */         createServiceException(null, "!csArchiverAutomationTurnOff");
/*      */       }
/*      */     }
/*      */ 
/* 1767 */     String statusMsg = null;
/* 1768 */     if (isEdit)
/*      */     {
/* 1770 */       if (ownerData.isProxied())
/*      */       {
/* 1773 */         String archiveLocation = ArchiveUtils.buildLocation(tOwner, archiveName);
/* 1774 */         DataBinder binder = new DataBinder();
/* 1775 */         binder.putLocal("aArchiveLocation", archiveLocation);
/* 1776 */         binder.putLocal("IDC_Name", tOwner);
/* 1777 */         TransferUtils.executeProxiedRequest(ownerData.getProxiedServer(), "REMOVE_PROXIEDTRANSFER", binder, binder, this, null);
/*      */       }
/*      */       else
/*      */       {
/* 1782 */         ReplicationData.registerTranfer(this.m_currentCollection.m_name, archiveName, targetArchive, isAuto);
/* 1783 */         if (isAuto)
/*      */         {
/* 1785 */           statusMsg = "csArchiverAddAutoTransfer";
/*      */         }
/*      */         else
/*      */         {
/* 1789 */           statusMsg = "csArchiverRemoveAutoTransfer";
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1799 */       DataBinder archiveData = ArchiveUtils.readArchiveFile(this.m_currentCollection.m_location, archiveName, false);
/* 1800 */       String oldOwner = archiveData.getLocal("aTransferOwner");
/* 1801 */       CollectionData oldOwnerData = null;
/* 1802 */       if (oldOwner != null)
/*      */       {
/* 1804 */         oldOwnerData = ArchiveUtils.getCollection(oldOwner);
/*      */       }
/*      */ 
/* 1807 */       if ((((oldOwnerData == null) || (!oldOwnerData.isProxied()))) && (!ownerData.isProxied()))
/*      */       {
/* 1809 */         ReplicationData.registerTranfer(this.m_currentCollection.m_name, archiveName, targetArchive, true);
/*      */ 
/* 1811 */         statusMsg = "csArchiverUpdateTransfer";
/*      */       }
/*      */       else
/*      */       {
/* 1818 */         String archiveLocation = ArchiveUtils.buildLocation(this.m_currentCollection.m_name, archiveName);
/* 1819 */         DataBinder binder = new DataBinder();
/* 1820 */         binder.putLocal("aArchiveLocation", archiveLocation);
/* 1821 */         binder.putLocal("IDC_Name", oldOwner);
/* 1822 */         TransferUtils.executeProxiedRequest(oldOwnerData.getProxiedServer(), "REMOVE_PROXIEDTRANSFER", binder, binder, this, null);
/*      */ 
/* 1825 */         ReplicationData.registerTranfer(this.m_currentCollection.m_name, archiveName, targetArchive, true);
/*      */ 
/* 1827 */         statusMsg = "csArchiverUpdateTransfer";
/*      */       }
/*      */     }
/* 1830 */     this.m_binder.putLocal("IsUpdatedTransfers", "1");
/* 1831 */     if (statusMsg == null)
/*      */       return;
/* 1833 */     str = LocaleUtils.encodeMessage(statusMsg, null, this.m_currentCollection.m_name, archiveName, targetArchive);
/*      */ 
/* 1835 */     report(str);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getOutgoingProviders()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1843 */     String[] columns = { "psIDC_Name", "psHttpRelativeWebRoot" };
/* 1844 */     DataResultSet drset = new DataResultSet(columns);
/*      */ 
/* 1846 */     Hashtable providers = OutgoingProviderMonitor.get0utgoingProviders();
/* 1847 */     for (Enumeration en = providers.elements(); en.hasMoreElements(); )
/*      */     {
/* 1849 */       Provider prov = (Provider)en.nextElement();
/* 1850 */       DataBinder provData = prov.getProviderData();
/*      */ 
/* 1852 */       Vector row = new IdcVector();
/* 1853 */       for (int i = 0; i < columns.length; ++i)
/*      */       {
/* 1855 */         String value = provData.get(columns[i].substring(2));
/* 1856 */         row.addElement(value);
/*      */       }
/*      */ 
/* 1860 */       String relWRoot = (String)row.elementAt(1);
/* 1861 */       String csRoot = DirectoryLocator.getRelativeAdminRoot();
/* 1862 */       if (!relWRoot.equals(csRoot))
/*      */       {
/* 1864 */         drset.addRow(row);
/*      */       }
/*      */     }
/*      */ 
/* 1868 */     this.m_binder.addResultSet("OutgoingProviders", drset);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void performProxiedRequest()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1877 */     String psName = this.m_currentCollection.getProxiedServer();
/* 1878 */     if (psName == null)
/*      */     {
/* 1880 */       createServiceException(null, "!csArchiverProxiedCollectionNoProxiedServer");
/*      */     }
/*      */ 
/* 1883 */     TransferUtils.executeProxiedRequest(psName, this.m_serviceData.m_name, this.m_binder, this.m_binder, this, null);
/*      */   }
/*      */ 
/*      */   protected String getCollectionAndLockDirectory(int type)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1891 */     String lockDir = null;
/* 1892 */     switch (type)
/*      */     {
/*      */     case 1:
/* 1896 */       if (this.m_currentCollection == null)
/*      */       {
/* 1898 */         this.m_currentCollection = ArchiveUtils.getCollection(this.m_binder);
/*      */       }
/* 1900 */       if (!this.m_currentCollection.isProxied())
/*      */       {
/* 1902 */         lockDir = this.m_currentCollection.m_location; } break;
/*      */     case 2:
/* 1908 */       lockDir = DirectoryLocator.getCollectionsDirectory();
/* 1909 */       break;
/*      */     case 3:
/* 1913 */       if (this.m_currentCollection == null)
/*      */       {
/* 1915 */         this.m_currentCollection = ArchiveUtils.getCollection(this.m_binder);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1920 */     if (this.m_currentCollection != null)
/*      */     {
/* 1922 */       setCachedObject("CurrentCollection", this.m_currentCollection);
/*      */     }
/*      */ 
/* 1925 */     return lockDir;
/*      */   }
/*      */ 
/*      */   protected void setData(int dataType) throws ServiceException, DataException
/*      */   {
/* 1930 */     if ((dataType != 0) && (this.m_currentCollection == null))
/*      */     {
/* 1933 */       this.m_currentCollection = ArchiveUtils.getCollection(this.m_binder);
/*      */     }
/*      */ 
/* 1936 */     if ((dataType == 0) || ((this.m_currentCollection != null) && (this.m_currentCollection.isProxied())))
/*      */     {
/* 1938 */       return;
/*      */     }
/*      */ 
/* 1941 */     this.m_collectionData = ArchiveUtils.readCollectionData(this.m_currentCollection.m_location, false);
/*      */ 
/* 1943 */     this.m_archivesSet = ((DataResultSet)this.m_collectionData.getResultSet("Archives"));
/*      */ 
/* 1945 */     setCachedObject("CollectionData", this.m_collectionData);
/* 1946 */     if (this.m_archivesSet != null)
/*      */     {
/* 1948 */       setCachedObject("ArchivesSet", this.m_archivesSet);
/*      */     }
/*      */ 
/* 1951 */     if (this.m_archivesSet == null)
/*      */     {
/* 1954 */       createServiceException(null, "!csArchiverUnableToAccess");
/*      */     }
/*      */ 
/* 1957 */     if (dataType != 2)
/*      */       return;
/* 1959 */     this.m_archiveName = this.m_binder.get("aArchiveName");
/* 1960 */     if (this.m_archiveName == null)
/*      */       return;
/* 1962 */     this.m_archiveDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_location, this.m_archiveName);
/* 1963 */     setCachedObject("ArchiveDir", this.m_archiveDir);
/* 1964 */     this.m_archiveExportDir = ArchiveUtils.buildArchiveDirectory(this.m_currentCollection.m_exportLocation, this.m_archiveName);
/* 1965 */     setCachedObject("ArchiveExportDir", this.m_archiveExportDir);
/*      */   }
/*      */ 
/*      */   protected DataBinder createErrorBinder(String msg)
/*      */   {
/* 1972 */     DataBinder binder = new DataBinder();
/* 1973 */     binder.putLocal("StatusMessageKey", msg);
/* 1974 */     binder.putLocal("StatusMessage", msg);
/* 1975 */     binder.putLocal("StatusCode", "-1");
/* 1976 */     return binder;
/*      */   }
/*      */ 
/*      */   protected DataBinder createArchiveContextData()
/*      */   {
/* 1981 */     DataBinder binder = new DataBinder();
/* 1982 */     if (this.m_currentCollection != null)
/*      */     {
/* 1984 */       binder.putLocal("IDC_Name", this.m_currentCollection.m_name);
/*      */     }
/*      */     else
/*      */     {
/* 1988 */       binder.putLocal("IDC_Name", "<UNKNOWN>");
/*      */     }
/*      */ 
/* 1991 */     return binder;
/*      */   }
/*      */ 
/*      */   public void doResponse(boolean isError, ServiceException err)
/*      */     throws ServiceException
/*      */   {
/* 2000 */     boolean isDownload = StringUtils.convertToBool(this.m_binder.getLocal("IsDelayDownload"), false);
/* 2001 */     if (!isDownload)
/*      */     {
/* 2003 */       super.doResponse(isError, err);
/* 2004 */       return;
/*      */     }
/*      */ 
/* 2007 */     if (isError)
/*      */     {
/* 2009 */       if (this.m_binder.m_isJava)
/*      */       {
/* 2011 */         sendDataBinder(isError);
/*      */       }
/*      */       else
/*      */       {
/* 2015 */         buildResponsePage(isError);
/*      */       }
/* 2017 */       return;
/*      */     }
/*      */ 
/* 2021 */     sendFileResponse();
/*      */   }
/*      */ 
/*      */   public void sendFileResponse() throws ServiceException
/*      */   {
/* 2026 */     this.m_httpImplementor.sendMultiPartResponse(this.m_binder);
/*      */   }
/*      */ 
/*      */   public void report(String msg)
/*      */   {
/* 2031 */     String userHost = this.m_binder.getEnvironmentValue("HTTP_HOST");
/* 2032 */     String user = (this.m_userData != null) ? this.m_userData.m_name : null;
/* 2033 */     if ((userHost == null) || (userHost.length() == 0))
/*      */     {
/* 2035 */       userHost = this.m_binder.getEnvironmentValue("RemoteClientHostName");
/* 2036 */       if ((userHost == null) || (userHost.length() == 0))
/*      */       {
/* 2038 */         userHost = this.m_binder.getEnvironmentValue("RemoteClientHostAddress");
/*      */       }
/*      */     }
/* 2041 */     if ((userHost != null) && (userHost.length() > 0) && (user != null) && (user.length() > 0))
/*      */     {
/* 2043 */       if (msg == null)
/*      */       {
/* 2045 */         msg = "";
/*      */       }
/* 2047 */       msg = LocaleUtils.encodeMessage("csUserEventMessage", msg, user, userHost);
/*      */     }
/*      */ 
/* 2051 */     Report.appInfo("archiver", null, msg, null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2056 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98955 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ArchiveService
 * JD-Core Version:    0.5.4
 */