/*     */ package intradoc.server.schema;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.schema.SchemaData;
/*     */ import intradoc.shared.schema.SchemaResultSet;
/*     */ import java.io.File;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ 
/*     */ public class SchemaStorage
/*     */ {
/*     */   protected String m_storageDirectory;
/*     */   protected String m_lockDirectory;
/*  38 */   protected Hashtable m_timeStamps = new Hashtable();
/*     */   protected SchemaResultSet m_resultSet;
/*     */   protected String m_shortName;
/*  41 */   protected boolean m_isInitialLoad = true;
/*  42 */   protected Hashtable m_fileObjectMap = new Hashtable();
/*  43 */   protected Hashtable m_objectFileMap = new Hashtable();
/*     */ 
/*     */   public SchemaStorage()
/*     */   {
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public SchemaStorage(SchemaResultSet resultSet, String shortName, String storageDir)
/*     */   {
/*  56 */     SystemUtils.reportDeprecatedUsage("SchemaStorage constructor");
/*  57 */     init(resultSet, shortName, storageDir, null);
/*     */   }
/*     */ 
/*     */   public void init(SchemaResultSet resultSet, String shortName, String storageDir, String lockDir)
/*     */   {
/*  63 */     this.m_resultSet = resultSet;
/*  64 */     this.m_shortName = shortName;
/*  65 */     this.m_storageDirectory = FileUtils.directorySlashes(storageDir);
/*  66 */     if (lockDir != null)
/*     */     {
/*  68 */       this.m_lockDirectory = lockDir;
/*     */     }
/*     */     else
/*     */     {
/*  72 */       this.m_lockDirectory = FileUtils.getParent(storageDir);
/*     */     }
/*     */   }
/*     */ 
/*     */   public SchemaData getSchemaData(String name)
/*     */   {
/*  78 */     return this.m_resultSet.getData(name);
/*     */   }
/*     */ 
/*     */   public String getStorageDirectory()
/*     */   {
/*  83 */     return this.m_storageDirectory;
/*     */   }
/*     */ 
/*     */   public String getLockDirectory()
/*     */   {
/*  88 */     return this.m_lockDirectory;
/*     */   }
/*     */ 
/*     */   public void setLockDirectory(String lockDir)
/*     */   {
/*  93 */     this.m_lockDirectory = lockDir;
/*     */   }
/*     */ 
/*     */   public void load()
/*     */     throws ServiceException, DataException
/*     */   {
/* 100 */     load(0);
/*     */   }
/*     */ 
/*     */   public void load(int flags) throws ServiceException, DataException
/*     */   {
/* 105 */     File dir = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory, "Schema", true);
/* 106 */     if (!dir.exists())
/*     */     {
/* 108 */       return;
/*     */     }
/* 110 */     String[] fileList = dir.list();
/* 111 */     Hashtable processed = new Hashtable();
/*     */ 
/* 114 */     int successCount = 0;
/* 115 */     int failCount = 0;
/* 116 */     for (int i = 0; i < fileList.length; ++i)
/*     */     {
/* 118 */       File file = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory + fileList[i], "Schema", false);
/* 119 */       if (!file.isFile())
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 124 */       String fileName = fileList[i];
/* 125 */       String filePath = this.m_storageDirectory + fileName;
/* 126 */       if (!fileName.endsWith(".hda"))
/*     */       {
/* 128 */         if (fileName.equals("lockwait.dat"))
/*     */           continue;
/* 130 */         Report.trace("schemastorage", "Schema definition filenames must end in \".hda\".  Skipping \"" + fileName + "\".", null);
/*     */       }
/*     */       else
/*     */       {
/* 135 */         String baseName = fileName.substring(0, fileName.length() - 4);
/* 136 */         processed.put(baseName, "1");
/*     */ 
/* 138 */         Exception exception = null;
/*     */         try
/*     */         {
/* 141 */           SchemaData data = loadFromFile(file, flags);
/* 142 */           if (data != null)
/*     */           {
/* 144 */             processed.put(data.m_canonicalName, "2");
/*     */           }
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 149 */           exception = e;
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 153 */           exception = e;
/*     */         }
/* 155 */         if (exception != null)
/*     */         {
/* 157 */           ++failCount;
/* 158 */           String msg = LocaleUtils.encodeMessage("csSchemaErrorLoadingData", exception.getMessage(), filePath);
/*     */ 
/* 160 */           Report.error("schemastorage", msg, exception);
/*     */         }
/* 162 */         ++successCount;
/*     */       }
/*     */     }
/*     */ 
/* 166 */     for (this.m_resultSet.first(); this.m_resultSet.isRowPresent(); this.m_resultSet.next())
/*     */     {
/* 168 */       SchemaData data = this.m_resultSet.getData();
/* 169 */       String tmp = (String)processed.get(data.m_name);
/* 170 */       if (tmp == null)
/*     */       {
/* 172 */         tmp = (String)processed.get(data.m_canonicalName);
/*     */       }
/* 174 */       if (tmp != null)
/*     */         continue;
/* 176 */       Report.trace("schemastorage", "Deleting " + data.m_name + " in " + this.m_storageDirectory + " because it was not found in directory listing", null);
/*     */ 
/* 179 */       if (SharedObjects.getEnvValueAsBoolean("SuppressSchemaStorageCleanupDelete", false))
/*     */         continue;
/* 181 */       delete(data.m_name);
/*     */     }
/*     */   }
/*     */ 
/*     */   public SchemaData load(String name, boolean mustExist)
/*     */     throws ServiceException, DataException
/*     */   {
/* 190 */     String lookupKey = (String)this.m_objectFileMap.get(name);
/* 191 */     if (lookupKey == null)
/*     */     {
/* 195 */       Report.trace("schemastorage", "SchemaData.load(String,boolean) - name " + name + " is assumed to be a file name", null);
/*     */ 
/* 198 */       lookupKey = this.m_resultSet.canonicalName(name);
/*     */     }
/* 200 */     SchemaData data = this.m_resultSet.getData(name);
/*     */ 
/* 202 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory + lookupKey, "Schema", false);
/* 203 */     boolean fileExists = file.exists();
/* 204 */     if (data != null)
/*     */     {
/* 206 */       if (isFileCurrent(file, new long[1]))
/*     */       {
/* 209 */         return data;
/*     */       }
/*     */     }
/* 212 */     else if (!fileExists)
/*     */     {
/* 214 */       if (mustExist)
/*     */       {
/* 216 */         String errMsg = LocaleUtils.encodeMessage("csSchMissingDefinition_" + this.m_shortName, null, name);
/*     */ 
/* 218 */         ServiceException e = new ServiceException(errMsg);
/* 219 */         Report.trace("schemastorage", null, e);
/* 220 */         throw e;
/*     */       }
/* 222 */       return null;
/*     */     }
/*     */ 
/* 225 */     if (fileExists)
/*     */     {
/* 227 */       data = loadFromFile(file);
/*     */     }
/* 229 */     if (data == null)
/*     */     {
/* 231 */       data = this.m_resultSet.getData(name);
/*     */     }
/* 233 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaData loadFromFile(File file) throws DataException, ServiceException
/*     */   {
/* 238 */     return loadFromFile(file, 0);
/*     */   }
/*     */ 
/*     */   public SchemaData loadFromFile(String fileName, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 244 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory + fileName, "Schema", false);
/* 245 */     return loadFromFile(file, flags);
/*     */   }
/*     */ 
/*     */   public SchemaData loadFromFile(File file, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/* 252 */     String fileName = file.getName();
/* 253 */     long[] loadTime = new long[1];
/*     */ 
/* 256 */     if (FileUtils.checkFile(file, true, false) != 0)
/*     */     {
/* 258 */       String msg = LocaleUtils.encodeMessage("syFileUtilsFileNotFound", null, fileName);
/*     */ 
/* 260 */       ServiceException e = new ServiceException(msg);
/* 261 */       Report.trace("schemastorage", null, e);
/* 262 */       throw e;
/*     */     }
/*     */     SchemaData data;
/* 265 */     if (((flags & 0x1) != 0) || (!isFileCurrent(file, loadTime)))
/*     */     {
/* 267 */       DataBinder binder = new DataBinder();
/* 268 */       if (SystemUtils.m_verbose)
/*     */       {
/* 270 */         Report.debug("schemastorage", "Loading data from " + fileName, null);
/*     */       }
/* 272 */       ResourceUtils.serializeDataBinder(file, binder, false, false);
/* 273 */       String requiredFeatures = binder.getLocal("schRequiredFeatures");
/* 274 */       List missingFeatures = ComponentLoader.getMissingFeatures(requiredFeatures);
/* 275 */       if (missingFeatures != null)
/*     */       {
/* 277 */         IdcStringBuilder builder = new IdcStringBuilder("Not loading data from " + fileName + " because these features are missing:");
/*     */ 
/* 280 */         for (int i = 0; i < missingFeatures.size(); ++i)
/*     */         {
/* 282 */           if (i > 0)
/*     */           {
/* 284 */             builder.append(',');
/*     */           }
/* 286 */           builder.append2(' ', missingFeatures.get(i).toString());
/*     */         }
/* 288 */         Report.trace("schemastorage", builder.toString(), null);
/* 289 */         return null;
/*     */       }
/*     */ 
/* 309 */       updateTimeStamp(fileName, loadTime);
/* 310 */       SchemaData data = this.m_resultSet.update(binder, loadTime[0]);
/* 311 */       this.m_fileObjectMap.put(fileName, data.m_name);
/* 312 */       this.m_objectFileMap.put(data.m_name, fileName);
/* 313 */       if (SystemUtils.m_verbose)
/*     */       {
/* 315 */         Report.debug("schemastorage", "Loaded schema data " + data.m_name + " from " + fileName, null);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 320 */       if (SystemUtils.m_verbose)
/*     */       {
/* 322 */         Report.debug("schemastorage", "Not loading '" + file.getAbsolutePath() + "'.  It is already up to date.", null);
/*     */       }
/*     */ 
/* 325 */       String name = (String)this.m_fileObjectMap.get(fileName);
/* 326 */       data = this.m_resultSet.getData(name);
/*     */     }
/*     */ 
/* 329 */     return data;
/*     */   }
/*     */ 
/*     */   public boolean isFileCurrent(String fileName, long[] fileLastModified)
/*     */   {
/* 334 */     File file = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory + fileName, "Schema", false);
/* 335 */     return isFileCurrent(file, fileLastModified);
/*     */   }
/*     */ 
/*     */   public boolean isFileCurrent(File file, long[] fileLastModified) {
/* 339 */     fileLastModified[0] = file.lastModified();
/* 340 */     long[] fileLastLoaded = (long[])(long[])this.m_timeStamps.get(file.getName());
/* 341 */     return (fileLastLoaded != null) && (fileLastLoaded[0] == fileLastModified[0]);
/*     */   }
/*     */ 
/*     */   public long updateTimeStamp(String fileName, long[] timeStamp)
/*     */   {
/* 346 */     if (timeStamp == null)
/*     */     {
/* 348 */       File file = FileUtilsCfgBuilder.getCfgFile(this.m_storageDirectory + fileName, "Schema", false);
/* 349 */       timeStamp = new long[] { file.lastModified() };
/*     */     }
/* 351 */     this.m_timeStamps.put(fileName, timeStamp);
/* 352 */     return timeStamp[0];
/*     */   }
/*     */ 
/*     */   public void write(SchemaData data, boolean isUpdate)
/*     */     throws ServiceException, DataException
/*     */   {
/* 361 */     String fileName = (String)this.m_objectFileMap.get(data.m_name);
/* 362 */     if (fileName == null)
/*     */     {
/* 364 */       fileName = data.m_canonicalName + ".hda";
/* 365 */       this.m_objectFileMap.put(data.m_name, fileName);
/* 366 */       this.m_fileObjectMap.put(fileName, data.m_name);
/*     */     }
/* 368 */     DataBinder binder = new DataBinder();
/* 369 */     data.populateBinder(binder);
/* 370 */     String versionNumber = binder.getLocal("schVersion");
/* 371 */     if (versionNumber == null)
/*     */     {
/* 373 */       versionNumber = "1";
/*     */     }
/*     */     else
/*     */     {
/* 377 */       int version = NumberUtils.parseInteger(versionNumber, 0);
/* 378 */       ++version;
/* 379 */       versionNumber = "" + version;
/*     */     }
/*     */ 
/* 382 */     binder.putLocal("schVersion", versionNumber);
/*     */ 
/* 385 */     data.getData().putLocal("schVersion", versionNumber);
/*     */ 
/* 387 */     boolean result = ResourceUtils.serializeDataBinder(this.m_storageDirectory, fileName, binder, true, false);
/*     */ 
/* 389 */     if (!result)
/*     */       return;
/* 391 */     updateTimeStamp(fileName, null);
/*     */   }
/*     */ 
/*     */   public void createOrUpdateAutoDetect(String name, DataBinder newData, boolean isUpdateDefault)
/*     */     throws ServiceException, DataException
/*     */   {
/* 403 */     boolean shouldBeUpdate = this.m_resultSet.getData(name) != null;
/* 404 */     if (isUpdateDefault != shouldBeUpdate)
/*     */     {
/* 406 */       if (isUpdateDefault)
/*     */       {
/* 408 */         Report.info("schemastorage", null, IdcMessageFactory.lc("csSchemaUpdateIncorrect", new Object[] { name }));
/*     */       }
/*     */       else
/*     */       {
/* 412 */         Report.info("schemastorage", null, IdcMessageFactory.lc("csSchemaCreateIncorrect", new Object[] { name }));
/*     */       }
/*     */     }
/*     */ 
/* 416 */     createOrUpdate(name, newData, shouldBeUpdate);
/*     */   }
/*     */ 
/*     */   public void createOrUpdate(String name, DataBinder newData, boolean isUpdate)
/*     */     throws ServiceException, DataException
/*     */   {
/* 422 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 425 */       String msg = LocaleUtils.encodeMessage("apSchemaNoName", null);
/* 426 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 429 */     boolean lockedDir = false;
/* 430 */     if (!FileUtils.isDirectoryLockedByThisThread(this.m_lockDirectory, null))
/*     */     {
/* 432 */       FileUtils.reserveDirectory(this.m_lockDirectory);
/* 433 */       lockedDir = true;
/*     */     }
/*     */     try
/*     */     {
/* 437 */       createOrUpdateInternal(name, newData, isUpdate);
/*     */     }
/*     */     finally
/*     */     {
/* 441 */       if (lockedDir)
/*     */       {
/* 443 */         FileUtils.releaseDirectory(this.m_lockDirectory);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createOrUpdateInternal(String name, DataBinder newData, boolean isUpdate)
/*     */     throws ServiceException, DataException
/*     */   {
/* 451 */     SchemaData data = null;
/* 452 */     SchemaData oldData = this.m_resultSet.getData(name);
/*     */ 
/* 454 */     if (isUpdate)
/*     */     {
/* 456 */       data = load(name, false);
/*     */     }
/*     */     else
/*     */     {
/* 460 */       data = this.m_resultSet.getData(name);
/*     */     }
/*     */ 
/* 463 */     if ((!isUpdate) && (oldData != null))
/*     */     {
/* 465 */       String msg = LocaleUtils.encodeMessage("apSchemaObjectExists_" + this.m_shortName, null, name);
/*     */ 
/* 467 */       throw new ServiceException(-17, msg);
/*     */     }
/* 469 */     if ((isUpdate) && (oldData == null))
/*     */     {
/* 471 */       Report.trace("schemastorage", "updating a non-existent " + this.m_shortName, null);
/*     */     }
/*     */ 
/* 474 */     long now = System.currentTimeMillis();
/* 475 */     data = this.m_resultSet.update(newData, now);
/*     */ 
/* 478 */     write(data, isUpdate);
/*     */   }
/*     */ 
/*     */   public void delete(String name)
/*     */     throws ServiceException, DataException
/*     */   {
/* 485 */     SchemaData data = this.m_resultSet.delete(name);
/* 486 */     if (data != null)
/*     */     {
/* 489 */       String filename = data.m_canonicalName + ".hda";
/* 490 */       FileUtils.deleteFile(this.m_storageDirectory + filename);
/*     */     }
/*     */     else
/*     */     {
/* 494 */       String msg = LocaleUtils.encodeMessage("csSchemaObjDoesntExist_" + this.m_shortName, "!csSchUnableToDeleteObj_" + this.m_shortName, name);
/*     */ 
/* 496 */       Report.info(null, msg, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 502 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97380 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.schema.SchemaStorage
 * JD-Core Version:    0.5.4
 */