/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DatabaseConfigData;
/*     */ import intradoc.data.DatabaseTypes;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.ProviderConfigUtils;
/*     */ import intradoc.provider.ProviderPoolManager;
/*     */ import intradoc.provider.WorkspaceConfigImplementor;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.sql.Connection;
/*     */ import java.sql.DatabaseMetaData;
/*     */ import java.sql.ResultSet;
/*     */ import java.sql.ResultSetMetaData;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ 
/*     */ public class JdbcConfigImplementor
/*     */   implements WorkspaceConfigImplementor
/*     */ {
/*     */   protected DatabaseConfigData m_config;
/*     */ 
/*     */   public JdbcConfigImplementor()
/*     */   {
/*  30 */     this.m_config = new DatabaseConfigData();
/*     */   }
/*     */ 
/*     */   public void validateAndUpdateConfiguration(Provider provider, ProviderPoolManager manager) throws DataException, ServiceException {
/*  34 */     DataBinder providerData = provider.getProviderData();
/*     */ 
/*  37 */     ProviderConfigUtils.loadSharedTableReplaceKey(providerData, "ColumnTranslation", "ColumnMap");
/*     */ 
/*  42 */     DataBinder modProviderData = providerData.createShallowCopyCloneResultSets();
/*  43 */     ProviderConfigUtils.loadSharedTable(modProviderData, "DatabaseConnectionConfigurations");
/*     */ 
/*  45 */     JdbcManager jdbcManager = (JdbcManager)manager;
/*  46 */     this.m_config.init(modProviderData);
/*  47 */     jdbcManager.m_config = this.m_config;
/*     */     Connection con;
/*     */     Connection con;
/*  50 */     if (!jdbcManager.usingExternalDataSource())
/*     */     {
/*  52 */       con = JdbcConnectionUtils.getConnection(manager, providerData);
/*     */     }
/*     */     else
/*     */     {
/*  56 */       con = (Connection)jdbcManager.getExternalRawConnection();
/*     */     }
/*  58 */     checkProperties(modProviderData, this.m_config, con);
/*  59 */     this.m_config.initConfigurations();
/*  60 */     initDBCollation(con);
/*     */     try
/*     */     {
/*  64 */       if (this.m_config.getValueAsBool("CheckQueryPrefixMode", false))
/*     */       {
/*  66 */         checkUnicodeColumns(con, manager, this.m_config);
/*     */       }
/*  68 */       checkNumBytesPerChar(con, manager, this.m_config);
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/*  72 */       manager.debugMsg("Unable to do discovery of query prefixes and char storage size. Error occurred:" + ignore);
/*     */     }
/*     */     finally
/*     */     {
/*  77 */       if (con != null)
/*     */       {
/*     */         try
/*     */         {
/*  81 */           con.close();
/*     */         }
/*     */         catch (Throwable ignore)
/*     */         {
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  90 */     int formatType = 0;
/*  91 */     String pattern = this.m_config.getValueAsString("JdbcDateFormat");
/*  92 */     if ((pattern == null) || (pattern.equalsIgnoreCase("odbc")) || (pattern.equalsIgnoreCase("odbcraw")) || (pattern.equalsIgnoreCase("jdbc")))
/*     */     {
/*  95 */       formatType = 0;
/*  96 */       pattern = null;
/*     */     }
/*  98 */     else if (pattern.equals("iso8601"))
/*     */     {
/* 100 */       formatType = 4;
/* 101 */       pattern = "yyyyMMdd'T'HHmmss";
/*     */     }
/* 103 */     this.m_config.setConfigValue("JdbcFormatType", new Integer(formatType));
/*     */ 
/* 105 */     addCompatibilityConfigs(manager, this.m_config);
/*     */ 
/* 107 */     manager.debugMsg("Database type: " + this.m_config.getValueAsString("DatabaseType"));
/* 108 */     manager.debugMsg("Database version: " + this.m_config.getValueAsString("DatabaseVersion"));
/* 109 */     manager.debugMsg("Database driver name: " + this.m_config.getValueAsString("JdbcDriverName"));
/* 110 */     manager.debugMsg("Database driver version: " + this.m_config.getValueAsString("JdbcDriverVersion"));
/*     */ 
/* 113 */     manager.debugMsg("DB supports column change: " + this.m_config.getValueAsBool("SupportSqlColumnChange", false));
/*     */ 
/* 115 */     manager.debugMsg("DB supports column delete: " + this.m_config.getValueAsBool("SupportSqlColumnDelete", false));
/*     */ 
/* 117 */     manager.debugMsg("DB column length adjustment factor is " + this.m_config.getValueAsString("NumBytesPerCharInDB") + ".");
/*     */ 
/* 120 */     prepareProviderData(this.m_config, providerData);
/*     */   }
/*     */ 
/*     */   public void prepareProviderData(DatabaseConfigData config, DataBinder binder)
/*     */   {
/* 125 */     String[] keys = new String[2];
/* 126 */     keys[0] = config.getValueAsString("ProviderDataRequiredKeys");
/* 127 */     keys[1] = config.getValueAsString("DBDependentProviderDataRequiredKeys");
/*     */ 
/* 129 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 131 */       List list = StringUtils.makeListFromSequenceSimple(keys[i]);
/*     */ 
/* 133 */       for (String key : list)
/*     */       {
/* 135 */         String value = config.getValueAsString(key);
/* 136 */         if (value != null)
/*     */         {
/* 138 */           binder.putLocal(key, value);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void checkProperties(DataBinder providerData, DatabaseConfigData config, Connection con)
/*     */     throws DataException
/*     */   {
/* 147 */     String dbNameOrig = "Microsoft Sql Server";
/*     */     try
/*     */     {
/* 150 */       String url = providerData.getAllowMissing("JdbcDriver");
/* 151 */       DatabaseMetaData dbMetaData = con.getMetaData();
/* 152 */       dbNameOrig = dbMetaData.getDatabaseProductName();
/* 153 */       String dbName = dbNameOrig.toLowerCase();
/*     */ 
/* 158 */       boolean supportsChangeFetchSize = true;
/* 159 */       if ((url == null) || (!url.startsWith("jdbc:freetds")))
/*     */       {
/* 161 */         supportsChangeFetchSize = true;
/*     */       }
/* 163 */       if (providerData.getAllowMissing("SupportFetchSizeChange") == null)
/*     */       {
/* 165 */         providerData.putLocal("SupportFetchSizeChange", "" + supportsChangeFetchSize);
/*     */       }
/* 167 */       boolean supportsSavepoints = false;
/*     */       try
/*     */       {
/* 170 */         supportsSavepoints = dbMetaData.supportsSavepoints();
/*     */       }
/*     */       catch (Throwable ignore)
/*     */       {
/*     */       }
/*     */ 
/* 178 */       String versionStr = dbMetaData.getDatabaseProductVersion();
/* 179 */       String version = versionStr;
/* 180 */       int index = versionStr.indexOf(".");
/* 181 */       int startIndex = -1;
/* 182 */       char seperator = ' ';
/* 183 */       if (index > 0)
/*     */       {
/* 188 */         startIndex = versionStr.lastIndexOf(seperator, index) + 1;
/* 189 */         if (!Character.isDigit(versionStr.charAt(startIndex)))
/*     */         {
/* 191 */           seperator = '/';
/* 192 */           startIndex = versionStr.lastIndexOf(seperator, index) + 1;
/*     */         }
/* 194 */         int endIndex = versionStr.indexOf(seperator, index);
/* 195 */         int verStrLen = versionStr.length();
/* 196 */         if (endIndex < 0)
/*     */         {
/* 198 */           endIndex = verStrLen;
/*     */         }
/* 200 */         version = versionStr.substring(startIndex, endIndex);
/*     */ 
/* 203 */         if (index == startIndex + 1)
/*     */         {
/* 205 */           version = "0" + version;
/*     */         }
/*     */ 
/* 208 */         if (startIndex > 0)
/*     */         {
/* 210 */           version = version + " ---" + versionStr.substring(0, startIndex);
/*     */         }
/* 212 */         if (endIndex < verStrLen)
/*     */         {
/* 214 */           version = version + " ---" + versionStr.substring(endIndex);
/*     */         }
/*     */       }
/*     */ 
/* 218 */       boolean needCheckUnicodeColumns = false;
/* 219 */       String dbType = DatabaseTypes.MSSQL;
/* 220 */       if (dbName.equals("microsoft sql server"))
/*     */       {
/* 222 */         needCheckUnicodeColumns = true;
/*     */       }
/* 224 */       else if (dbName.equals("oracle"))
/*     */       {
/* 226 */         dbType = DatabaseTypes.ORACLE;
/* 227 */         needCheckUnicodeColumns = true;
/*     */       }
/* 229 */       else if (dbName.startsWith("informix"))
/*     */       {
/* 231 */         dbType = DatabaseTypes.INFORMIX;
/*     */       }
/* 233 */       else if ((dbName.startsWith("sybase")) || (dbName.startsWith("adaptive")))
/*     */       {
/* 235 */         dbType = DatabaseTypes.SYBASE;
/* 236 */         needCheckUnicodeColumns = true;
/*     */       }
/* 238 */       else if (dbName.startsWith("db2"))
/*     */       {
/* 240 */         dbType = DatabaseTypes.DB2;
/* 241 */         needCheckUnicodeColumns = true;
/*     */       }
/* 243 */       else if (dbName.startsWith("postgresql"))
/*     */       {
/* 245 */         dbType = DatabaseTypes.POSTGRESQL;
/*     */       }
/* 247 */       else if (dbName.startsWith("apache derby"))
/*     */       {
/* 249 */         dbType = DatabaseTypes.DERBY;
/*     */       }
/* 251 */       else if (dbName.equals("mysql"))
/*     */       {
/* 253 */         dbType = DatabaseTypes.MYSQL;
/*     */       }
/* 255 */       config.setDatabaseLabel(dbType.toUpperCase());
/* 256 */       config.setConfigValue("DatabaseType", dbType);
/* 257 */       config.setConfigValue("DatabaseVersion", version);
/*     */ 
/* 259 */       config.setConfigValue("SupportsSavePoint", Boolean.valueOf(supportsSavepoints));
/* 260 */       config.setConfigValue("DatabaseName", dbNameOrig);
/* 261 */       String driverName = dbMetaData.getDriverName();
/* 262 */       config.setConfigValue("JdbcDriverName", driverName);
/* 263 */       String driverVersion = dbMetaData.getDriverVersion();
/* 264 */       config.setConfigValue("JdbcDriverVersion", driverVersion);
/*     */ 
/* 266 */       if ((dbType == DatabaseTypes.ORACLE) && (version.compareTo("11") < 0) && (providerData.getAllowMissing("UseSecureFiles") == null))
/*     */       {
/* 270 */         providerData.putLocal("UseSecureFiles", "" + Boolean.FALSE);
/*     */       }
/*     */ 
/* 273 */       if ((needCheckUnicodeColumns) && (providerData.getAllowMissing("CheckQueryPrefixMode") == null))
/*     */       {
/* 276 */         providerData.putLocal("CheckQueryPrefixMode", "" + Boolean.TRUE);
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 281 */       throw new DataException("!csJdbcCheckPropertiesError", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void checkUnicodeColumns(Connection con, ProviderPoolManager manager, DatabaseConfigData config)
/*     */   {
/* 295 */     boolean hasUnicodeColumn = false;
/* 296 */     String mode = config.getValueAsString("QueryValuePrefixMode");
/* 297 */     if ((mode == null) || (mode.length() == 0) || (mode.equalsIgnoreCase("auto")))
/*     */     {
/* 299 */       String existingTable = config.getValueAsString("ExistingTableName");
/* 300 */       String existingColumn = config.getValueAsString("ExistingColumnName");
/*     */ 
/* 302 */       if (existingTable == null)
/*     */       {
/* 304 */         existingTable = "Config";
/*     */       }
/*     */ 
/* 307 */       if (existingColumn == null)
/*     */       {
/* 309 */         existingColumn = "dSection";
/*     */       }
/* 311 */       String query = "SELECT " + existingColumn + " FROM " + existingTable + " WHERE 1 = 0";
/* 312 */       String oracleQuery = "SELECT data_type FROM user_tab_columns WHERE table_name = '" + existingTable.toUpperCase() + "' AND column_Name = '" + existingColumn.toUpperCase() + "'";
/*     */ 
/* 315 */       Statement stat = null;
/*     */       try
/*     */       {
/* 318 */         stat = con.createStatement();
/* 319 */         String dbType = config.getValueAsString("DatabaseType");
/* 320 */         if (dbType.equals(DatabaseTypes.ORACLE))
/*     */         {
/* 322 */           ResultSet rset = stat.executeQuery(oracleQuery);
/* 323 */           if (rset.next())
/*     */           {
/* 325 */             String type = rset.getString(1);
/* 326 */             if (type.equalsIgnoreCase("nvarchar2"))
/*     */             {
/* 328 */               hasUnicodeColumn = true;
/*     */             }
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 334 */           ResultSet rset = stat.executeQuery(query);
/* 335 */           ResultSetMetaData rmd = rset.getMetaData();
/* 336 */           String type = rmd.getColumnTypeName(1);
/* 337 */           if (dbType.equals(DatabaseTypes.DB2))
/*     */           {
/* 339 */             if (type.equalsIgnoreCase("vargraphic"))
/*     */             {
/* 341 */               hasUnicodeColumn = true;
/*     */             }
/*     */           }
/* 344 */           else if (dbType.equals(DatabaseTypes.SYBASE))
/*     */           {
/* 346 */             if (type.equalsIgnoreCase("univarchar"))
/*     */             {
/* 348 */               hasUnicodeColumn = true;
/*     */             }
/*     */ 
/*     */           }
/* 353 */           else if (type.equalsIgnoreCase("nvarchar"))
/*     */           {
/* 355 */             hasUnicodeColumn = true;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/* 362 */         manager.debugMsg("Unable to discover if unicode query prefixes is requried, error occurred: " + e.toString() + ". Query prefixes would not be added");
/*     */       }
/*     */       finally
/*     */       {
/* 367 */         if (stat != null)
/*     */         {
/*     */           try
/*     */           {
/* 371 */             stat.close();
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/* 380 */     else if (mode.equalsIgnoreCase("unicode"))
/*     */     {
/* 382 */       hasUnicodeColumn = true;
/*     */     }
/*     */ 
/* 385 */     config.setConfigValue("IsUniversalUnicodeDatabase", Boolean.valueOf(hasUnicodeColumn));
/*     */   }
/*     */ 
/*     */   public void checkNumBytesPerChar(Connection conn, ProviderPoolManager manager, DatabaseConfigData config)
/*     */   {
/* 391 */     int numBytes = config.getValueAsInt("NumBytesPerCharInDB", -1);
/* 392 */     if (numBytes != -1)
/*     */       return;
/* 394 */     String existingTable = config.getValueAsString("ExistingTableName");
/* 395 */     String existingColumn = config.getValueAsString("ExistingColumnName");
/*     */ 
/* 397 */     if (existingTable == null)
/*     */     {
/* 399 */       existingTable = "Config";
/*     */     }
/*     */ 
/* 402 */     if (existingColumn == null)
/*     */     {
/* 404 */       existingColumn = "dSection";
/*     */     }
/* 406 */     int defaultSize = config.getValueAsInt("ExistingVarcharColumnLength", 50);
/* 407 */     String query = "SELECT " + existingColumn + " FROM " + existingTable + " WHERE 1 = 0";
/*     */ 
/* 409 */     Statement stat = null;
/*     */     try
/*     */     {
/* 412 */       stat = conn.createStatement();
/*     */ 
/* 414 */       ResultSet rset = stat.executeQuery(query);
/* 415 */       ResultSetMetaData rsetMeta = rset.getMetaData();
/* 416 */       int size = rsetMeta.getColumnDisplaySize(1);
/*     */ 
/* 418 */       numBytes = size / defaultSize;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 422 */       manager.debugMsg("Unable to discover character storage size. Error occurred: " + e + "Character storage size set to 1.");
/*     */ 
/* 424 */       numBytes = 1;
/*     */     }
/*     */     finally
/*     */     {
/* 428 */       config.setConfigValue("NumBytesPerCharInDB", new Integer(numBytes));
/* 429 */       if (stat != null)
/*     */       {
/*     */         try
/*     */         {
/* 433 */           stat.close();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addCompatibilityConfigs(ProviderPoolManager manager, DatabaseConfigData config)
/*     */   {
/* 453 */     manager.setForceSync(config.getValueAsBool("ForceJdbcSync", false));
/*     */ 
/* 455 */     String[][] displayPropKeys = { { "IsUniversalUnicodeDatabase", "UseUnicode" }, { "NumBytesPerCharInDB", "NumBytesPerChar" }, { "UseDatabaseShortIndexName", "UseShortIndexName" } };
/*     */ 
/* 459 */     for (int i = 0; i < displayPropKeys.length; ++i)
/*     */     {
/* 461 */       Object value = config.getValue(displayPropKeys[i][0]);
/* 462 */       if (value == null)
/*     */         continue;
/* 464 */       config.setConfigValue(displayPropKeys[i][1], value);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initDBCollation(Connection dbConnection)
/*     */   {
/* 479 */     String dbType = this.m_config.getValueAsString("DatabaseType");
/* 480 */     String dbcollationQuery = null;
/* 481 */     String defaultDBCollation = "BINARY";
/* 482 */     String SqlServerDBNameQuery = "SELECT DB_NAME() AS DataBaseName";
/* 483 */     String SqlServerDBName = "";
/*     */ 
/* 485 */     if (dbType.equals(DatabaseTypes.ORACLE))
/*     */     {
/* 487 */       dbcollationQuery = "SELECT value FROM nls_session_parameters WHERE parameter = 'NLS_SORT'";
/*     */     }
/*     */ 
/* 490 */     if (dbType.equals(DatabaseTypes.MSSQL))
/*     */     {
/* 492 */       defaultDBCollation = "BINARY_CI";
/*     */     }
/*     */ 
/* 495 */     if (dbType.equals(DatabaseTypes.DB2))
/*     */     {
/* 497 */       dbcollationQuery = "SELECT value FROM sysibmadm.dbcfg WHERE name='collate_info'";
/*     */     }
/*     */ 
/* 500 */     if (dbType.equals(DatabaseTypes.MYSQL))
/*     */     {
/* 502 */       dbcollationQuery = "SELECT @@GLOBAL.collation_database";
/*     */     }
/*     */ 
/* 505 */     Statement dbStatement = null;
/* 506 */     String dbCollation = defaultDBCollation;
/*     */     try
/*     */     {
/* 510 */       dbStatement = dbConnection.createStatement();
/*     */ 
/* 512 */       if (dbType.equals(DatabaseTypes.MSSQL))
/*     */       {
/* 514 */         ResultSet dbNameRset = dbStatement.executeQuery(SqlServerDBNameQuery);
/* 515 */         if (dbNameRset.next())
/*     */         {
/* 517 */           SqlServerDBName = dbNameRset.getString(1);
/*     */         }
/* 519 */         dbcollationQuery = "SELECT DATABASEPROPERTYEX('" + SqlServerDBName + "', 'Collation') AS 'value'";
/*     */       }
/*     */ 
/* 522 */       ResultSet dbCollationRset = dbStatement.executeQuery(dbcollationQuery);
/* 523 */       if (dbCollationRset.next())
/*     */       {
/* 525 */         dbCollation = dbCollationRset.getString(1);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 530 */       IdcMessage infoMsg = IdcMessageFactory.lc("csCollationNotFound", new Object[0]);
/* 531 */       Report.info(null, null, infoMsg);
/*     */     }
/*     */ 
/* 536 */     if ((dbCollation == null) || (dbCollation.length() == 0))
/*     */     {
/* 538 */       dbCollation = defaultDBCollation;
/*     */     }
/*     */ 
/* 541 */     HashMap map = (HashMap)this.m_config.getConfigMap().get(this.m_config.m_currentLabel);
/* 542 */     map.put("DatabaseCollation", dbCollation);
/*     */ 
/* 546 */     if ((!dbType.equals(DatabaseTypes.MSSQL)) || 
/* 548 */       (dbCollation.contains("_CS") != true) || 
/* 550 */       (this.m_config.getValue("DatabasePreserveCase") != null))
/*     */       return;
/* 552 */     map.put("DatabasePreserveCase", "true");
/*     */   }
/*     */ 
/*     */   public Object retrieveConfigurationObject(String key)
/*     */   {
/* 560 */     return this.m_config.getValue(key);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 565 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 91997 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcConfigImplementor
 * JD-Core Version:    0.5.4
 */