/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ConfigFileParameters;
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicDataParser;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.FileUtilsCfgBuilder;
/*     */ import intradoc.common.IdcMethodHolder;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ResourceObject;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TracerReportUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.IdcConfigFile;
/*     */ import intradoc.jdbc.JdbcConnectionUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.serialize.DataBinderSerializer;
/*     */ import intradoc.server.datastoredesign.DataDesignGenerator;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.shared.ResourceDataParser;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.File;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class IdcSystemRunTimeConfig
/*     */ {
/*     */   public static Connection m_connection;
/*     */ 
/*     */   public static void initRawDBConnection()
/*     */     throws ServiceException
/*     */   {
/*  69 */     boolean useExternalDataSource = SharedObjects.getEnvValueAsBoolean("SystemDatabase:UseDataSource", false);
/*     */ 
/*  71 */     if (useExternalDataSource)
/*     */     {
/*  73 */       initRawDBConnectionFromDataSource();
/*     */     }
/*     */     else
/*     */     {
/*  77 */       initRawDBConnectionFromString();
/*     */     }
/*  79 */     IdcConfigFile.setRawDBConnection(m_connection);
/*     */   }
/*     */ 
/*     */   public static void initRawDBConnectionFromDataSource()
/*     */     throws ServiceException
/*     */   {
/*  87 */     String externalDataSource = SharedObjects.getEnvironmentValue("SystemDatabase:DataSource");
/*     */     try
/*     */     {
/*  90 */       if ((externalDataSource == null) || (externalDataSource.length() == 0))
/*     */       {
/*  92 */         throw new DataException(null, "csInitDBExternalDataSourceNameMissing", new Object[0]);
/*     */       }
/*     */ 
/*  95 */       Object externalConnectionSource = null;
/*     */ 
/*  97 */       String initialContextClassName = SharedObjects.getEnvironmentValue("InitialContextClass");
/*  98 */       if ((initialContextClassName == null) || (initialContextClassName.length() < 0))
/*     */       {
/* 100 */         initialContextClassName = "javax.naming.InitialContext";
/*     */       }
/* 102 */       Class initialContextClass = Class.forName(initialContextClassName);
/* 103 */       Object initialContext = initialContextClass.newInstance();
/*     */ 
/* 105 */       String lookupMethodName = SharedObjects.getEnvironmentValue("ConnectionLookupMethodName");
/* 106 */       if ((lookupMethodName == null) || (lookupMethodName.length() < 0))
/*     */       {
/* 108 */         lookupMethodName = "lookup";
/*     */       }
/* 110 */       externalConnectionSource = ClassHelperUtils.executeMethodConvertToStandardExceptions(initialContext, lookupMethodName, new Object[] { externalDataSource });
/*     */ 
/* 114 */       if (externalConnectionSource == null)
/*     */       {
/* 116 */         throw new DataException(null, "csInitDBCannotFindExternalDataSource", new Object[] { externalDataSource });
/*     */       }
/*     */ 
/* 120 */       String getConnectionMethodName = SharedObjects.getEnvironmentValue("GetConnectionFromPoolMethodName");
/* 121 */       if ((getConnectionMethodName == null) || (getConnectionMethodName.length() < 0))
/*     */       {
/* 123 */         getConnectionMethodName = "getConnection";
/*     */       }
/* 125 */       IdcMethodHolder connectionGetMethod = ClassHelperUtils.getMethodHolder(externalConnectionSource.getClass(), getConnectionMethodName, null, 1);
/*     */ 
/* 129 */       m_connection = (Connection)ClassHelperUtils.executeIdcMethodConvertToStandardExceptions(externalConnectionSource, connectionGetMethod, new Object[0]);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 135 */       throw new ServiceException(e, "Failed to initialize jdbc connection from data source '" + externalDataSource + "'.", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initRawDBConnectionFromString()
/*     */     throws ServiceException
/*     */   {
/* 146 */     String jdbcConn = SharedObjects.getEnvironmentValue("JdbcConnectionString");
/*     */     try
/*     */     {
/* 149 */       if ((jdbcConn != null) && (jdbcConn.length() > 0))
/*     */       {
/* 151 */         DataBinder connData = new DataBinder();
/* 152 */         connData.putLocal("JdbcConnectionString", jdbcConn);
/* 153 */         connData.putLocal("JdbcUser", SharedObjects.getEnvironmentValue("JdbcUser"));
/* 154 */         connData.putLocal("JdbcPassword", SharedObjects.getEnvironmentValue("JdbcPassword"));
/* 155 */         connData.putLocal("JdbcPasswordEncoding", SharedObjects.getEnvironmentValue("JdbcPasswordEncoding"));
/*     */ 
/* 157 */         String driver = SharedObjects.getEnvironmentValue("JdbcDriver");
/* 158 */         Class.forName(driver);
/*     */ 
/* 160 */         m_connection = JdbcConnectionUtils.getConnection(null, connData);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 166 */       throw new ServiceException(e, "Failed to initialize jdbc connection from string '" + jdbcConn + "'.", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void releaseRawDBConnection()
/*     */     throws ServiceException
/*     */   {
/* 175 */     if (m_connection != null)
/*     */     {
/*     */       try
/*     */       {
/* 179 */         m_connection.close();
/* 180 */         m_connection = null;
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/* 187 */     IdcConfigFile.releaseRawDBConnection();
/*     */   }
/*     */ 
/*     */   public static void initConfigFileParameters()
/*     */     throws ServiceException
/*     */   {
/* 196 */     if (DataSerializeUtils.getDataSerialize() == null)
/*     */     {
/* 198 */       DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/*     */     }
/* 200 */     boolean enabled = SharedObjects.getEnvValueAsBoolean("EnableClusterFileSystem", false);
/* 201 */     if (enabled)
/*     */     {
/* 203 */       initRawDBConnection();
/*     */ 
/* 206 */       loadRunTimeParameters();
/* 207 */       initSharedConfigParams();
/*     */ 
/* 210 */       String loadLocation = ConfigFileParameters.getLoadFromLocation();
/* 211 */       if (loadLocation.equalsIgnoreCase("Filesystem"))
/*     */       {
/* 213 */         FileUtilsCfgBuilder.setCfgDescriptorFactory(new ConfigFileDescriptorFactoryBaseImplementor());
/*     */       }
/* 215 */       else if (loadLocation.equalsIgnoreCase("Database"))
/*     */       {
/* 217 */         FileUtilsCfgBuilder.setCfgDescriptorFactory(new ConfigFileDescriptorFactoryDBImplementor());
/*     */       }
/*     */ 
/* 221 */       String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/* 222 */       String archiveDir = LegacyDirectoryLocator.getDefaultCollection();
/* 223 */       ConfigFileParameters.init(intradocDir, archiveDir);
/* 224 */       IdcConfigFile.setIntradocDir(intradocDir);
/*     */     }
/*     */     else
/*     */     {
/* 228 */       ConfigFileParameters.setLoadFromLocation("Filesystem");
/* 229 */       ConfigFileParameters.setMigrateDestination("Filesystem");
/* 230 */       FileUtilsCfgBuilder.setCfgDescriptorFactory(new ConfigFileDescriptorFactoryBaseImplementor());
/*     */     }
/*     */ 
/* 234 */     String loadLocation = ConfigFileParameters.getLoadFromLocation();
/* 235 */     String storeLocation = ConfigFileParameters.getMigrateDestination();
/* 236 */     if (!loadLocation.equalsIgnoreCase(storeLocation))
/*     */     {
/* 238 */       initTracing(loadLocation);
/* 239 */       ConfigFileMigration.migrateSharedConfigFiles(m_connection, loadLocation, storeLocation);
/*     */     }
/*     */ 
/* 243 */     LegacyDirectoryLocator.buildBaseDirectories(storeLocation);
/*     */ 
/* 245 */     String baseBinaryDir = LegacyDirectoryLocator.getSystemBaseDirectory("binary");
/* 246 */     FileUtils.checkOrCreateDirectory(baseBinaryDir, 2);
/*     */ 
/* 248 */     String baseSearchDir = baseBinaryDir + "search/";
/* 249 */     FileUtils.checkOrCreateDirectory(baseSearchDir, 1);
/*     */ 
/* 251 */     String baseDataDir = LegacyDirectoryLocator.getSystemBaseDirectory("data");
/* 252 */     FileUtils.checkOrCreateDirectory(baseDataDir, 1);
/*     */ 
/* 254 */     String baseLogDir = LegacyDirectoryLocator.getSystemBaseDirectory("log");
/* 255 */     FileUtils.checkOrCreateDirectory(baseLogDir, 1);
/*     */ 
/* 257 */     LegacyDirectoryLocator.buildCollectionExport(storeLocation);
/* 258 */     String exportDir = LegacyDirectoryLocator.getCollectionExport();
/* 259 */     FileUtils.checkOrCreateDirectory(exportDir, 1);
/*     */   }
/*     */ 
/*     */   public static void initTracing(String loadLocation)
/*     */     throws ServiceException
/*     */   {
/* 269 */     if (loadLocation.equalsIgnoreCase("Filesystem"))
/*     */     {
/* 271 */       SharedObjects.putEnvironmentValue("BaseLogDir", DirectoryLocator.getAppDataDirectory());
/*     */     }
/* 273 */     else if (loadLocation.equalsIgnoreCase("Database"))
/*     */     {
/* 275 */       SharedObjects.putEnvironmentValue("BaseLogDir", DirectoryLocator.getSystemLogDirectory());
/*     */     }
/*     */ 
/* 278 */     int extraTraces = TracerReportUtils.m_traceSectionTypes.length;
/* 279 */     String[] tracingColumns = new String[extraTraces + 3];
/* 280 */     System.arraycopy(SharedLoader.m_tracingColumns, 0, tracingColumns, 0, 3);
/* 281 */     for (int i = 0; i < extraTraces; ++i)
/*     */     {
/* 283 */       tracingColumns[(i + 3)] = ("its" + TracerReportUtils.m_traceSectionTypes[i]);
/*     */     }
/*     */ 
/* 286 */     SharedLoader.m_tracingColumns = tracingColumns;
/*     */ 
/* 288 */     String dataDir = LegacyDirectoryLocator.getAppDataDirectory();
/* 289 */     String dir = dataDir + "config";
/* 290 */     if (FileUtils.checkFile(dir, false, false) == 0)
/*     */     {
/* 292 */       DataBinder tracing = new DataBinder();
/* 293 */       ResourceUtils.serializeDataBinder(dir, "tracing.hda", tracing, false, false);
/* 294 */       SharedLoader.configureTracing(tracing);
/*     */     }
/*     */     else
/*     */     {
/* 298 */       SharedLoader.configureTracing(null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int getInstanceType()
/*     */   {
/* 307 */     if (m_connection == null)
/*     */     {
/* 310 */       return 1;
/*     */     }
/*     */ 
/* 313 */     File dataDir = new File(LegacyDirectoryLocator.getIntradocDir() + "data/");
/* 314 */     if (dataDir.exists())
/*     */     {
/* 318 */       return 4;
/*     */     }
/* 320 */     return 2;
/*     */   }
/*     */ 
/*     */   public static void initSharedConfigParams()
/*     */     throws ServiceException
/*     */   {
/* 329 */     String forceLoadLocation = SharedObjects.getEnvironmentValue("ForceLoadConfig");
/* 330 */     if ((forceLoadLocation != null) && (forceLoadLocation.length() > 0))
/*     */     {
/* 332 */       if ((!forceLoadLocation.equalsIgnoreCase("filesystem")) && (!forceLoadLocation.equalsIgnoreCase("database")))
/*     */       {
/* 334 */         throw new ServiceException(null, "Invalid ForceLoadConfig value:'" + forceLoadLocation + "'." + " Has to be 'Filesystem' or 'Database'.", new Object[0]);
/*     */       }
/*     */ 
/* 337 */       ConfigFileParameters.setLoadFromLocation(forceLoadLocation);
/* 338 */       ConfigFileParameters.setMigrateDestination(forceLoadLocation);
/* 339 */       updateRunTimeParameters("LoadConfig", forceLoadLocation);
/* 340 */       return;
/*     */     }
/*     */ 
/* 344 */     String loadLocation = SharedObjects.getEnvironmentValue("LoadConfig");
/* 345 */     String storeLocation = SharedObjects.getEnvironmentValue("ShareConfigDestination");
/* 346 */     if ((loadLocation != null) && (loadLocation.length() > 0))
/*     */     {
/* 349 */       ConfigFileParameters.setLoadFromLocation(loadLocation);
/* 350 */       if ((storeLocation != null) && (storeLocation.length() > 0))
/*     */       {
/* 352 */         if ((!storeLocation.equalsIgnoreCase("filesystem")) && (!storeLocation.equalsIgnoreCase("database")))
/*     */         {
/* 354 */           throw new ServiceException(null, "Invalid ShareConfigDestination value:'" + storeLocation + "'." + " Has to be 'Filesystem' or 'Database'.", new Object[0]);
/*     */         }
/*     */ 
/* 357 */         ConfigFileParameters.setMigrateDestination(storeLocation);
/*     */       }
/*     */       else
/*     */       {
/* 362 */         storeLocation = loadLocation;
/* 363 */         ConfigFileParameters.setMigrateDestination(loadLocation);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 369 */       int instanceType = getInstanceType();
/* 370 */       switch (instanceType)
/*     */       {
/*     */       case 1:
/* 374 */         loadLocation = "Filesystem";
/* 375 */         storeLocation = "Filesystem";
/* 376 */         break;
/*     */       case 2:
/* 380 */         if (storeLocation != null)
/*     */         {
/* 382 */           loadLocation = storeLocation;
/*     */         }
/*     */         else
/*     */         {
/* 386 */           loadLocation = "Filesystem";
/* 387 */           storeLocation = "Filesystem";
/*     */         }
/* 389 */         break;
/*     */       case 4:
/* 393 */         loadLocation = "Filesystem";
/* 394 */         if (storeLocation == null)
/*     */         {
/* 396 */           storeLocation = "Filesystem";
/*     */         }
/*     */       case 3:
/*     */       }
/*     */ 
/* 401 */       ConfigFileParameters.setLoadFromLocation(loadLocation);
/* 402 */       ConfigFileParameters.setMigrateDestination(storeLocation);
/*     */ 
/* 405 */       updateRunTimeParameters("LoadConfig", loadLocation);
/*     */     }
/*     */ 
/* 409 */     if ((!storeLocation.equalsIgnoreCase("Database")) || (!loadLocation.equalsIgnoreCase("FileSystem")))
/*     */       return;
/* 411 */     checkAndCreateRunTimeTable("RunTimeConfigData");
/*     */   }
/*     */ 
/*     */   public static void loadRunTimeParameters()
/*     */     throws ServiceException
/*     */   {
/* 421 */     if (m_connection == null)
/*     */     {
/* 423 */       return;
/*     */     }
/*     */ 
/* 426 */     Statement stmt = null;
/*     */     try
/*     */     {
/* 429 */       checkAndCreateRunTimeTable("RunTimeConfigOptions");
/*     */ 
/* 433 */       stmt = m_connection.createStatement();
/* 434 */       String query = "SELECT dRTName, dRTValue FROM RunTimeConfigOptions WHERE dRTName = 'LoadConfig'";
/*     */ 
/* 436 */       ResultSet rset = stmt.executeQuery(query);
/* 437 */       if (rset.next())
/*     */       {
/* 439 */         String key = rset.getString(1);
/* 440 */         String value = rset.getString(2);
/* 441 */         SharedObjects.putEnvironmentValueAllowOverwrite(key, value, "RunTimeConfigOptions", true);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 452 */         if (stmt != null)
/*     */         {
/* 454 */           stmt.close();
/* 455 */           stmt = null;
/*     */         }
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void updateRunTimeParameters(String key, String value)
/*     */     throws ServiceException
/*     */   {
/* 471 */     if (m_connection == null)
/*     */     {
/* 473 */       return;
/*     */     }
/*     */ 
/* 476 */     Statement stmt = null;
/*     */     try
/*     */     {
/* 479 */       key = StringUtils.createQuotableString(key);
/* 480 */       value = StringUtils.createQuotableString(value);
/*     */ 
/* 482 */       checkAndCreateRunTimeTable("RunTimeConfigOptions");
/* 483 */       stmt = m_connection.createStatement();
/* 484 */       String executeSQL = "DELETE FROM RunTimeConfigOptions WHERE dRTName = '" + key + "'";
/* 485 */       stmt.executeUpdate(executeSQL);
/* 486 */       executeSQL = "INSERT INTO RunTimeConfigOptions (dRTName, dRTValue) VALUES ('" + key + "', '" + value + "')";
/*     */ 
/* 488 */       stmt.executeUpdate(executeSQL);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 498 */         stmt.close();
/* 499 */         stmt = null;
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void initRunTimeConfigOptions()
/*     */     throws ServiceException
/*     */   {
/* 514 */     Statement stmt = null;
/*     */     try
/*     */     {
/* 517 */       stmt = m_connection.createStatement();
/* 518 */       String query = "SELECT dRTName, dRTValue FROM RunTimeConfigOptions";
/* 519 */       ResultSet rset = stmt.executeQuery(query);
/* 520 */       while (rset.next())
/*     */       {
/* 522 */         SharedObjects.putEnvironmentValueAllowOverwrite(rset.getString(1), rset.getString(2), "RunTimeConfigOptions", true);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 534 */         stmt.close();
/* 535 */         stmt = null;
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void checkAndCreateRunTimeTable(String table)
/*     */     throws ServiceException
/*     */   {
/* 549 */     if (m_connection == null)
/*     */     {
/* 551 */       return;
/*     */     }
/*     */ 
/* 554 */     Statement stmt = null;
/*     */     try
/*     */     {
/* 557 */       table = StringUtils.createQuotableString(table);
/* 558 */       stmt = m_connection.createStatement();
/* 559 */       String query = "SELECT * FROM " + table;
/* 560 */       stmt.executeQuery(query);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*     */       try
/*     */       {
/* 566 */         ResourceContainer res = SharedObjects.getResources();
/* 567 */         String resourceDir = LegacyDirectoryLocator.getResourcesDirectory();
/* 568 */         String[] fileList = { "core/idoc/std_data_store_design.idoc", "core/tables/std_resources.htm" };
/*     */ 
/* 570 */         int size = fileList.length;
/* 571 */         List infos = new ArrayList();
/* 572 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 574 */           String fileName = resourceDir + fileList[i];
/* 575 */           res.m_resourceList = null;
/* 576 */           DataLoader.cacheResourceFile(res, fileName);
/* 577 */           List info = res.m_resourceList;
/* 578 */           infos.add(info);
/*     */         }
/*     */ 
/* 581 */         for (int i = size - 1; i >= 0; --i)
/*     */         {
/* 583 */           Vector resList = (Vector)infos.get(i);
/* 584 */           int num = resList.size();
/* 585 */           for (int j = 0; j < num; ++j)
/*     */           {
/* 587 */             ResourceObject resObj = (ResourceObject)resList.elementAt(j);
/* 588 */             if ((resObj.m_type != 1) && (resObj.m_type != 4)) {
/*     */               continue;
/*     */             }
/* 591 */             String name = resObj.m_name;
/* 592 */             if (SharedObjects.getTable(name) != null) {
/*     */               continue;
/*     */             }
/*     */ 
/* 596 */             Table tble = (Table)resObj.m_resource;
/* 597 */             DataResultSet rset = new DataResultSet();
/* 598 */             rset.init(tble);
/* 599 */             ComponentLoader.addExtraComponentColumn(name, rset, null);
/* 600 */             SharedObjects.putTable(name, rset);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 605 */         DatabaseMetaData dbMetaData = m_connection.getMetaData();
/* 606 */         String dbNameOrig = dbMetaData.getDatabaseProductName();
/* 607 */         String dbName = dbNameOrig.toLowerCase();
/* 608 */         DataDesignGenerator myGenerator = new DataDesignGenerator();
/* 609 */         DynamicDataParser dataParser = new ResourceDataParser();
/* 610 */         DynamicData.addParser(dataParser);
/* 611 */         myGenerator.init(dbName);
/* 612 */         String tableSyntax = myGenerator.generateRunTimeConfigTableSQLDefinition(table);
/*     */ 
/* 614 */         stmt = m_connection.createStatement();
/* 615 */         stmt.executeUpdate(tableSyntax);
/*     */ 
/* 617 */         Map tables = (Map)AppObjectRepository.getObject("tables");
/* 618 */         tables.clear();
/* 619 */         res.m_resourceList = null;
/* 620 */         res.m_tables.clear();
/*     */       }
/*     */       catch (Exception e0)
/*     */       {
/* 624 */         throw new ServiceException(e0, "Failed to create table RunTimeConfigData", new Object[0]);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*     */       try
/*     */       {
/* 631 */         stmt.close();
/* 632 */         stmt = null;
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void cleanLoadConfig()
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 652 */       String value = SharedObjects.getEnvironmentValue("LoadConfig");
/* 653 */       if (value != null)
/*     */       {
/* 656 */         SharedObjects.removeEnvironmentValue("LoadConfig");
/*     */ 
/* 658 */         if (value.equalsIgnoreCase("Database"))
/*     */         {
/* 660 */           updateRunTimeParameters("LoadConfig", "Database");
/*     */         }
/*     */ 
/* 663 */         String idcFile = SystemUtils.getCfgFilePath();
/* 664 */         SystemPropertiesEditor sysEditor = new SystemPropertiesEditor(idcFile);
/* 665 */         sysEditor.initIdc();
/* 666 */         sysEditor.removePropertyValues(new String[] { "LoadConfig" }, null);
/* 667 */         sysEditor.saveIdc();
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 672 */       throw new ServiceException(e, "Unable to remove LoadConfig parameter from intradoc.cfg.", new Object[0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 678 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104684 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.IdcSystemRunTimeConfig
 * JD-Core Version:    0.5.4
 */