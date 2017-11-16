/*      */ package intradoc.apputilities.installer;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NativeOsUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.provider.Provider;
/*      */ import intradoc.provider.Providers;
/*      */ import intradoc.server.ComponentLoader;
/*      */ import intradoc.server.IdcSystemLoader;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.net.Socket;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DatabaseInstaller
/*      */   implements SectionInstaller
/*      */ {
/*   39 */   public static String FINAL_TABLE = "Config";
/*      */   public String m_odbcBase;
/*      */   public String[] m_installScriptFiles;
/*      */   public String m_statementDelimiter;
/*      */   public String[] m_allowedStatements;
/*      */   public String m_sqlServerDriver;
/*      */   public SysInstaller m_installer;
/*      */   public NativeOsUtils m_utils;
/*      */   public String m_database;
/*      */   public Properties m_dbProps;
/*      */   public Properties m_localProps;
/*      */ 
/*      */   public DatabaseInstaller()
/*      */   {
/*   41 */     this.m_odbcBase = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBC.INI";
/*   42 */     this.m_installScriptFiles = null;
/*   43 */     this.m_statementDelimiter = null;
/*   44 */     this.m_allowedStatements = null;
/*      */ 
/*   51 */     this.m_localProps = new Properties();
/*      */   }
/*      */ 
/*      */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*      */     throws ServiceException
/*      */   {
/*   57 */     this.m_installer = installer;
/*   58 */     this.m_utils = this.m_installer.m_utils;
/*   59 */     this.m_database = this.m_installer.getInstallValue("DatabaseType", null);
/*   60 */     if ((this.m_database == null) && (this.m_installer.isOlderVersion("4.0")))
/*      */     {
/*   62 */       Report.trace("install", "upgrade from 3.x, trying to detect database.", null);
/*      */ 
/*   65 */       String driver = this.m_installer.getConfigValue("JdbcDriver");
/*   66 */       if ((driver != null) && (driver.indexOf("oracle") >= 0))
/*      */       {
/*   68 */         this.m_database = "oracle";
/*      */       }
/*      */       else
/*      */       {
/*   72 */         this.m_database = "mssql";
/*      */       }
/*   74 */       Report.trace("install", "using " + this.m_database + " for the database.", null);
/*      */     }
/*      */ 
/*   77 */     if (this.m_database == null)
/*      */     {
/*   79 */       String msg = LocaleUtils.encodeMessage("csJdbcDatabaseTypeUndetermined", null);
/*      */ 
/*   81 */       this.m_installer.m_installLog.error(msg);
/*   82 */       return 1;
/*      */     }
/*      */ 
/*   88 */     this.m_dbProps = this.m_installer.getInstallerTable("DatabaseServerTable", this.m_database);
/*      */ 
/*   90 */     if (this.m_dbProps == null)
/*      */     {
/*   92 */       Report.trace("install", "the database type \"" + this.m_database + "\" is not defined in DatabaseServerTable, skipping", null);
/*      */ 
/*   94 */       return 1;
/*      */     }
/*      */ 
/*   97 */     if (name.equals("dbcheck"))
/*      */     {
/*   99 */       if (this.m_database.equals("skip_database"))
/*      */       {
/*  101 */         this.m_installer.m_installerConfig.put("SkipDatabase", "1");
/*      */       }
/*  103 */       else if (!this.m_database.equals("msde"))
/*      */       {
/*  109 */         DataBinder db = prepareDatabaseConnection();
/*  110 */         checkDatabase(db);
/*      */       }
/*      */     }
/*  113 */     if (name.equals("db"))
/*      */     {
/*  115 */       boolean createDatabase = this.m_installer.getInstallBool("CreateDatabase", false);
/*      */ 
/*  117 */       if ((createDatabase) && (this.m_installer.m_isUpdate))
/*      */       {
/*  119 */         createDatabase = false;
/*      */       }
/*  121 */       if (this.m_database.equals("skip_database"))
/*      */       {
/*  123 */         this.m_installer.m_installerConfig.put("SkipDatabase", "1");
/*      */       }
/*      */       else
/*      */       {
/*  127 */         String jdbcDriverJarString = this.m_installer.getInstallValue("JdbcDriverPackageSourceFiles", null);
/*      */ 
/*  129 */         if ((jdbcDriverJarString != null) && (jdbcDriverJarString.length() > 0))
/*      */         {
/*  133 */           this.m_installer.m_installerConfig.put("JdbcClasspath", jdbcDriverJarString);
/*      */         }
/*      */ 
/*  137 */         if (this.m_installer.getInstallBool("JdbcDriverPackageCopy", false))
/*      */         {
/*  140 */           if (jdbcDriverJarString == null)
/*      */           {
/*  142 */             Report.trace("install", "JdbcDriverPackageSourceFiles is not set", null);
/*      */           }
/*      */           else
/*      */           {
/*  147 */             String pathSep = EnvUtils.getPathSeparator();
/*  148 */             List list = StringUtils.makeListFromSequence(jdbcDriverJarString, pathSep.charAt(0), '^', 0);
/*      */ 
/*  150 */             IdcStringBuilder classpathEntry = new IdcStringBuilder();
/*      */ 
/*  152 */             for (int i = 0; i < list.size(); ++i)
/*      */             {
/*  154 */               String jarFile = (String)list.get(i);
/*  155 */               jarFile = FileUtils.fileSlashes(jarFile);
/*  156 */               int index = jarFile.lastIndexOf("/");
/*  157 */               if (index >= 0)
/*      */               {
/*  159 */                 String jarFileName = jarFile.substring(index + 1);
/*  160 */                 String target = "shared/classes/" + jarFileName;
/*  161 */                 target = this.m_installer.computeDestination(target);
/*  162 */                 FileUtils.copyFile(jarFile, target);
/*  163 */                 if (classpathEntry.length() > 0)
/*      */                 {
/*  165 */                   classpathEntry.append(pathSep);
/*      */                 }
/*  167 */                 classpathEntry.append("$SharedDir/classes/");
/*  168 */                 classpathEntry.append(jarFileName);
/*      */               }
/*      */               else
/*      */               {
/*  172 */                 Report.trace("install", "Jdbc Driver jar file " + jarFile + " doesn't look like a path", null);
/*      */               }
/*      */ 
/*      */             }
/*      */ 
/*  179 */             this.m_installer.m_installerConfig.put("JdbcClasspath", classpathEntry.toString());
/*      */           }
/*      */         }
/*      */ 
/*  183 */         configureDatabase(true, null);
/*  184 */         DataBinder db = prepareDatabaseConnection();
/*  185 */         if (createDatabase)
/*      */         {
/*  187 */           createDatabase(db);
/*      */         }
/*      */       }
/*      */     }
/*  191 */     return 0;
/*      */   }
/*      */ 
/*      */   protected String[] cleanEntry(String tmp)
/*      */   {
/*  196 */     Vector entry = StringUtils.parseArray(tmp, ',', '^');
/*  197 */     int entrySize = entry.size();
/*  198 */     if (entrySize == 0)
/*      */     {
/*  200 */       return null;
/*      */     }
/*  202 */     if (entrySize == 1)
/*      */     {
/*  204 */       entry.addElement("${" + entry.elementAt(0) + "}");
/*      */     }
/*  206 */     if (entrySize == 2)
/*      */     {
/*  208 */       entry.addElement("config/config.cfg");
/*      */     }
/*  210 */     String[] entryArray = new String[entry.size()];
/*  211 */     entry.copyInto(entryArray);
/*  212 */     return entryArray;
/*      */   }
/*      */ 
/*      */   public void configureDatabase(boolean createConfigEntries, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  218 */     Vector configEntries = this.m_installer.parseArrayTrim(this.m_dbProps, "ConfigFileEntries");
/*      */ 
/*  220 */     int count = configEntries.size();
/*      */ 
/*  222 */     if (this.m_database.equals("msde"))
/*      */     {
/*  224 */       int port = 1433;
/*  225 */       boolean found = false;
/*  226 */       if ((this.m_utils == null) || (!this.m_utils.isWindowsRegistrySupported()))
/*      */       {
/*  228 */         Report.trace("install", "no NativeOsUtils registry support, can't read registry to find MSDE port", null);
/*      */       }
/*      */       else
/*      */       {
/*  233 */         String instanceName = this.m_installer.getInstallValue("MSDEInstanceName", "idc");
/*  234 */         String regKey = "HKEY_LOCAL_MACHINE\\Software\\Microsoft\\Microsoft SQL Server\\";
/*  235 */         regKey = regKey + instanceName;
/*  236 */         regKey = regKey + "\\MSSQLServer\\SuperSocketNetLib\\Tcp\\TcpPort";
/*  237 */         String portString = this.m_utils.getRegistryValue(regKey);
/*  238 */         if (portString != null)
/*      */         {
/*  240 */           port = NumberUtils.parseInteger(portString, 0);
/*  241 */           if (port == 0)
/*      */           {
/*  243 */             port = 1433;
/*      */           }
/*      */           else
/*      */           {
/*  247 */             found = true;
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  252 */       if (!found)
/*      */       {
/*  254 */         Report.trace("install", "using sockets to find MSDE", null);
/*      */ 
/*  258 */         int[] portList = { 1433, 1096 };
/*  259 */         for (int i = 0; i < portList.length; ++i)
/*      */         {
/*      */           try
/*      */           {
/*  263 */             Socket s = new Socket("localhost", portList[i]);
/*  264 */             s.close();
/*  265 */             port = portList[i];
/*  266 */             found = true;
/*      */           }
/*      */           catch (Exception e)
/*      */           {
/*  271 */             if (SystemUtils.m_verbose)
/*      */             {
/*  273 */               Report.debug("install", null, e);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  279 */       if (!found)
/*      */       {
/*  281 */         Report.trace("install", "unable to find the MSDE port", null);
/*      */       }
/*      */       else
/*      */       {
/*  285 */         Report.trace("install", "setting DBServerPort to " + port, null);
/*  286 */         this.m_installer.m_installerConfig.put("DBServerPort", "" + port);
/*      */       }
/*      */     }
/*      */ 
/*  290 */     for (int i = 0; i < count; ++i)
/*      */     {
/*  292 */       String tmp = (String)configEntries.elementAt(i);
/*  293 */       String[] entry = cleanEntry(tmp);
/*  294 */       if (entry == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  299 */       String name = entry[0];
/*  300 */       String value = entry[1];
/*  301 */       if (this.m_installer.getInstallBool("DisableDatabaseOverrides", false))
/*      */       {
/*  303 */         this.m_installer.m_intradocConfig.remove(name);
/*      */       }
/*  305 */       value = this.m_installer.substituteVariables(value, null);
/*  306 */       if (value.length() > 0)
/*      */       {
/*  308 */         if (createConfigEntries)
/*      */         {
/*  310 */           createConfigEntry(entry[2], name, value);
/*      */         }
/*  312 */         this.m_localProps.put(name, value);
/*  313 */         if (binder == null)
/*      */           continue;
/*  315 */         binder.putLocal(name, value);
/*      */       }
/*      */       else
/*      */       {
/*  320 */         this.m_localProps.remove(name);
/*  321 */         if (binder == null)
/*      */           continue;
/*  323 */         binder.removeLocal(name);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createConfigEntry(String file, String key, String value)
/*      */     throws ServiceException
/*      */   {
/*  332 */     this.m_installer.editConfigFile(file, key, value);
/*      */   }
/*      */ 
/*      */   public DataBinder prepareDatabaseConnection()
/*      */     throws ServiceException
/*      */   {
/*  338 */     Vector providerEntries = this.m_installer.parseArrayTrim(this.m_dbProps, "AdditionalProviderEntries");
/*      */ 
/*  340 */     if (this.m_installer.getInstallBool("IsOdbcDriver", false))
/*      */     {
/*  342 */       checkOrCreateDSN();
/*      */     }
/*      */ 
/*  345 */     DataBinder db = new DataBinder();
/*  346 */     db.putLocal("ProviderName", "InstallerJdbcProvider");
/*  347 */     configureDatabase(false, db);
/*      */ 
/*  349 */     Enumeration en = this.m_localProps.keys();
/*  350 */     while (en.hasMoreElements())
/*      */     {
/*  352 */       String key = (String)en.nextElement();
/*  353 */       String value = this.m_localProps.getProperty(key);
/*  354 */       Report.trace("install", "setting database config entry " + key + " to " + value, null);
/*      */ 
/*  356 */       db.putLocal(key, value);
/*      */ 
/*  359 */       long flags = this.m_installer.getConfigFlags(key);
/*  360 */       if ((flags & 0x80) == 128L)
/*      */       {
/*  363 */         key = key + "Encoding";
/*  364 */         value = this.m_installer.getInstallValue(key, null);
/*  365 */         if ((value != null) && (value.length() > 0))
/*      */         {
/*  367 */           Report.trace("install", "setting database config entry " + key + " to " + value, null);
/*      */ 
/*  369 */           db.putLocal(key, value);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  374 */     int size = providerEntries.size();
/*  375 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  377 */       String tmp = (String)providerEntries.elementAt(i);
/*  378 */       String[] entry = cleanEntry(tmp);
/*  379 */       if (entry == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  383 */       String name = entry[0];
/*  384 */       String value = entry[1];
/*  385 */       value = this.m_installer.substituteVariables(value, null);
/*  386 */       Report.trace("install", "setting database property " + name + " to " + value, null);
/*      */ 
/*  388 */       db.putLocal(name, value);
/*      */     }
/*      */ 
/*  392 */     DataResultSet drset = new DataResultSet(new String[] { "column", "alias" });
/*      */ 
/*  394 */     db.addResultSet("ColumnMap", drset);
/*      */ 
/*  396 */     boolean disableOverrides = this.m_installer.getInstallBool("DisableDatabaseOverrides", false);
/*      */ 
/*  399 */     if ((this.m_installer.m_isUpdate) && (!disableOverrides))
/*      */     {
/*  401 */       Vector configOverrideList = this.m_installer.parseArrayTrim(this.m_dbProps, "ConfigOverrides");
/*      */ 
/*  403 */       for (int i = 0; i < configOverrideList.size(); ++i)
/*      */       {
/*  405 */         String key = (String)configOverrideList.elementAt(i);
/*  406 */         String tmp = this.m_installer.getConfigValue(key);
/*  407 */         if (tmp == null)
/*      */           continue;
/*  409 */         Report.trace("install", "setting ConfigOverrides key " + key + " to " + tmp, null);
/*      */ 
/*  411 */         db.putLocal(key, tmp);
/*      */       }
/*      */     }
/*      */ 
/*  415 */     if (!this.m_installer.m_isUpdate)
/*      */     {
/*  417 */       db.putLocal("DisableDatabaseLog", "true");
/*      */     }
/*  419 */     return db;
/*      */   }
/*      */ 
/*      */   public String fixDsnForLength(String string, int length)
/*      */     throws ServiceException
/*      */   {
/*  425 */     String origString = string;
/*  426 */     if (string.length() <= length)
/*      */     {
/*  428 */       return string;
/*      */     }
/*  430 */     boolean found = false;
/*  431 */     for (int i = 1; i < 10; ++i)
/*      */     {
/*  433 */       string = string.substring(0, length - 1) + i;
/*  434 */       String database = this.m_utils.getRegistryValue(this.m_odbcBase + "\\" + string + "\\Database");
/*      */ 
/*  436 */       if (database != null)
/*      */         continue;
/*  438 */       found = true;
/*  439 */       break;
/*      */     }
/*      */ 
/*  442 */     if (found)
/*      */     {
/*  444 */       String msg = LocaleUtils.encodeMessage("csInstallerShorteningDSN", null, origString, string);
/*      */ 
/*  446 */       this.m_installer.m_installLog.warning(msg);
/*  447 */       this.m_installer.m_installerConfig.put("OdbcDataSource", string);
/*  448 */       return string;
/*      */     }
/*      */ 
/*  451 */     String msg = LocaleUtils.encodeMessage("csInstallerUnableToShortenDSN", null, origString, "" + length);
/*      */ 
/*  453 */     this.m_installer.m_installLog.error(msg);
/*  454 */     return origString;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void checkOrCreateDSN()
/*      */     throws ServiceException
/*      */   {
/*  468 */     if (this.m_utils == null)
/*      */     {
/*  470 */       String msg = LocaleUtils.encodeMessage("syNativeOsUtilsNotLoaded", null);
/*  471 */       msg = LocaleUtils.encodeMessage("csInstallerUnableToCreateDSN", msg);
/*  472 */       this.m_installer.m_installLog.error(msg);
/*  473 */       return;
/*      */     }
/*      */ 
/*  476 */     String dsn = this.m_installer.getInstallValue("OdbcDataSource", null);
/*  477 */     if (dsn == null)
/*      */     {
/*  479 */       dsn = "IdcDataSource-" + this.m_installer.getInstallValue("IDC_Name", null);
/*      */     }
/*      */ 
/*  482 */     dsn = fixDsnForLength(dsn, 30);
/*      */ 
/*  484 */     String dsnKey = this.m_odbcBase + "\\" + dsn;
/*  485 */     String database = this.m_utils.getRegistryValue(dsnKey + "\\Database");
/*  486 */     if (database != null)
/*      */     {
/*      */       return;
/*      */     }
/*      */ 
/*  493 */     Report.trace("install", "creating new DSN", null);
/*  494 */     database = this.m_installer.getInstallValue("DBServerDatabase", null);
/*  495 */     if (database == null)
/*      */     {
/*  499 */       String msg = LocaleUtils.encodeMessage("csInstallerDatabaseNameNotSet", null);
/*      */ 
/*  501 */       msg = LocaleUtils.encodeMessage("csInstallerUnableToCreateDSN", msg);
/*  502 */       this.m_installer.m_installLog.error(msg);
/*  503 */       return;
/*      */     }
/*  505 */     String description = "Content Server \"" + this.m_installer.getInstallValue("IDC_Name", "unknown") + "\" ODBC Connection";
/*      */ 
/*  509 */     String driver = this.m_utils.getEnv("SystemRoot");
/*  510 */     driver = driver + "\\system32\\sqlsrv32.dll";
/*  511 */     String server = this.m_installer.getInstallValue("DBServerHost", "localhost");
/*      */ 
/*  513 */     this.m_utils.setRegistryValue(dsnKey + "\\Database", database, "string");
/*  514 */     this.m_utils.setRegistryValue(dsnKey + "\\Description", description, "string");
/*  515 */     this.m_utils.setRegistryValue(dsnKey + "\\Driver", driver, "string");
/*  516 */     this.m_utils.setRegistryValue(dsnKey + "\\LastUser", "sa", "string");
/*  517 */     this.m_utils.setRegistryValue(dsnKey + "\\Server", server, "string");
/*      */ 
/*  519 */     this.m_utils.setRegistryValue("HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBC.INI\\ODBC Data Sources\\" + dsn, "SQL Server", "string");
/*      */   }
/*      */ 
/*      */   public void createDatabase(DataBinder db)
/*      */     throws ServiceException
/*      */   {
/*  526 */     setCreationConfiguration();
/*  527 */     createInstance(db);
/*      */   }
/*      */ 
/*      */   public Workspace openDatabase(DataBinder db) throws DataException, ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  534 */       Map componentData = new HashMap();
/*  535 */       componentData.put("ComponentQueries", ComponentLoader.m_queries);
/*      */ 
/*  537 */       Provider dbProvider = new Provider(db, componentData);
/*  538 */       dbProvider.init();
/*  539 */       Providers.addProvider("SystemDatabase", dbProvider);
/*  540 */       Workspace ws = IdcSystemLoader.loadDatabase(1);
/*  541 */       return ws;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  545 */       throw new DataException("!csInstallerUnableOpenConnectionToDatabase", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setCreationConfiguration()
/*      */     throws ServiceException
/*      */   {
/*  552 */     String array = this.m_dbProps.getProperty("CreationScripts");
/*  553 */     Vector list = StringUtils.parseArray(array, ',', '^');
/*  554 */     this.m_installScriptFiles = new String[list.size()];
/*  555 */     list.copyInto(this.m_installScriptFiles);
/*      */ 
/*  557 */     array = this.m_dbProps.getProperty("ValidQueryPrefixList");
/*  558 */     list = StringUtils.parseArray(array, ',', '^');
/*  559 */     this.m_allowedStatements = new String[list.size()];
/*  560 */     list.copyInto(this.m_allowedStatements);
/*      */ 
/*  562 */     this.m_statementDelimiter = this.m_dbProps.getProperty("StatementDelimiter");
/*  563 */     if (this.m_statementDelimiter != null)
/*      */       return;
/*  565 */     throw new ServiceException("!$The SQL statement delimiter is not defined.");
/*      */   }
/*      */ 
/*      */   public void checkDatabase(DataBinder db)
/*      */     throws ServiceException
/*      */   {
/*  582 */     String idcInfoFile = this.m_installer.computeDestinationEx("install/idc_info.txt", false);
/*  583 */     if (FileUtils.checkFile(idcInfoFile, true, false) == 0)
/*      */     {
/*  585 */       String driver = db.getAllowMissing("JdbcDriver");
/*  586 */       String connection = db.getAllowMissing("JdbcConnectionString");
/*  587 */       String user = db.getAllowMissing("JdbcUser");
/*  588 */       BufferedReader r = null;
/*      */       try
/*      */       {
/*  591 */         r = new BufferedReader(new FileReader(idcInfoFile));
/*      */ 
/*  593 */         Hashtable instances = new Hashtable();
/*      */ 
/*  595 */         String line = r.readLine();
/*  596 */         if (line != null)
/*      */         {
/*  598 */           String myIdcName = this.m_installer.getInstallValue("IDC_Name", "");
/*  599 */           while ((line = r.readLine()) != null)
/*      */           {
/*  601 */             line = line.trim();
/*  602 */             int index = line.indexOf("/");
/*  603 */             if (index > 0)
/*      */             {
/*  605 */               String idcName = line.substring(0, index);
/*  606 */               if (idcName.equals(myIdcName))
/*      */               {
/*      */                 continue;
/*      */               }
/*      */ 
/*  611 */               String[] jdbcData = (String[])(String[])instances.get(idcName);
/*  612 */               if (jdbcData == null)
/*      */               {
/*  614 */                 jdbcData = new String[3];
/*  615 */                 instances.put(idcName, jdbcData);
/*      */               }
/*      */ 
/*  618 */               line = line.substring(index + 1);
/*  619 */               index = line.indexOf("=");
/*  620 */               if (index > 0)
/*      */               {
/*  622 */                 String key = line.substring(0, index);
/*  623 */                 String val = line.substring(index + 1);
/*  624 */                 if (key.equals("JdbcDriver"))
/*      */                 {
/*  626 */                   jdbcData[0] = val;
/*      */                 }
/*  628 */                 if (key.equals("JdbcConnectionString"))
/*      */                 {
/*  630 */                   jdbcData[1] = val;
/*      */                 }
/*  632 */                 if (key.equals("JdbcUser"))
/*      */                 {
/*  634 */                   jdbcData[2] = val;
/*      */                 }
/*      */ 
/*  637 */                 if ((jdbcData[0] != null) && (jdbcData[1] != null) && (jdbcData[2] != null) && 
/*  639 */                   (jdbcData[0].equals(driver)) && (jdbcData[1].equals(connection)) && (jdbcData[2].equals(user)))
/*      */                 {
/*  642 */                   throw new ServiceException(LocaleUtils.encodeMessage("csInstallerJDBCDataMatches", null, idcName));
/*      */                 }
/*      */               }
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (FileNotFoundException e)
/*      */       {
/*  654 */         this.m_installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerJDBCConnectionDataNotChecked", null, e));
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  659 */         this.m_installer.m_installLog.warning(LocaleUtils.encodeMessage("csInstallerJDBCConnectionDataNotChecked", null, e));
/*      */       }
/*      */       finally
/*      */       {
/*  664 */         FileUtils.closeObject(r);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  669 */       this.m_installer.m_installLog.notice(LocaleUtils.encodeMessage("csInstallerJDBCConnectionDataFileNotFound", null, FileUtils.getAbsolutePath(idcInfoFile)));
/*      */     }
/*      */ 
/*  674 */     if (this.m_installer.getInstallBool("InstallMSDE", false))
/*      */     {
/*  679 */       return;
/*      */     }
/*      */ 
/*      */     Workspace ws;
/*      */     try
/*      */     {
/*  686 */       ws = openDatabase(db);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  690 */       this.m_installer.m_installLog.notice("!csInstallerDBConnectionFailed");
/*  691 */       Report.trace("install", null, e);
/*  692 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  697 */       ws.executeSQL("DROP TABLE tmp");
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  701 */       if (SystemUtils.m_verbose)
/*      */       {
/*  703 */         Report.debug("install", null, ignore);
/*      */       }
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  709 */       ws.executeSQL("CREATE TABLE tmp (test VARCHAR(1))");
/*  710 */       ws.executeSQL("CREATE UNIQUE INDEX tmp_index ON tmp (test)");
/*  711 */       ws.executeSQL("INSERT INTO tmp VALUES (' ')");
/*  712 */       ws.executeSQL("UPDATE tmp set test=' '");
/*  713 */       ws.createResultSetSQL("SELECT * FROM tmp");
/*  714 */       ws.executeSQL("DROP TABLE tmp");
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  718 */       Report.trace("install", null, e);
/*  719 */       String msg = LocaleUtils.encodeMessage("csInstallerDBTablesWorkFailed", null, e.getMessage());
/*  720 */       this.m_installer.m_installLog.notice(msg);
/*  721 */       throw new ServiceException(-18, msg);
/*      */     }
/*      */ 
/*  724 */     if (this.m_database.equalsIgnoreCase("oracle"))
/*      */     {
/*  726 */       String user = db.getLocal("JdbcUser");
/*  727 */       checkOracleEncoding(db, ws);
/*  728 */       checkFulltextPrivileges(db, ws, user);
/*      */     }
/*      */ 
/*  731 */     boolean isCreate = this.m_installer.getInstallBool("CreateDatabase", false);
/*  732 */     if ((isCreate) && (this.m_installer.m_isUpdate))
/*      */     {
/*  734 */       isCreate = false;
/*      */     }
/*      */ 
/*  737 */     DataException theException = null;
/*      */     boolean exists;
/*      */     try {
/*  740 */       ws.createResultSetSQL("SELECT COUNT(*) FROM Config");
/*  741 */       exists = true;
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  745 */       exists = false;
/*  746 */       theException = ignore;
/*      */     }
/*  748 */     if ((exists) && (isCreate) && (!this.m_installer.getInstallBool("OverwriteDatabase", false)))
/*      */     {
/*  750 */       ServiceException e = new ServiceException(-17, "!csInstallerTablesExist");
/*  751 */       if (theException != null)
/*      */       {
/*  753 */         SystemUtils.setExceptionCause(e, theException);
/*      */       }
/*  755 */       throw e;
/*      */     }
/*  757 */     if ((exists) || (isCreate))
/*      */       return;
/*  759 */     ServiceException e = new ServiceException(-16, "!csInstallerConfigTableNotFound");
/*  760 */     if (theException != null)
/*      */     {
/*  762 */       SystemUtils.setExceptionCause(e, theException);
/*      */     }
/*  764 */     throw e;
/*      */   }
/*      */ 
/*      */   public void checkOracleEncoding(DataBinder db, Workspace ws)
/*      */     throws ServiceException
/*      */   {
/*  770 */     String msg = LocaleUtils.encodeMessage("csInstallOracleDatabaseNotUtf8", null);
/*      */ 
/*  772 */     ServiceException se = null;
/*      */     try
/*      */     {
/*  776 */       DataBinder binder = new DataBinder();
/*  777 */       binder.putLocal("parameterKey", "NLS_CHARACTERSET");
/*  778 */       ResultSet rset = ws.createResultSet("QoracleParameters", binder);
/*  779 */       String[][] values = ResultSetUtils.createStringTable(rset, new String[] { "VALUE" });
/*  780 */       String matchValue = StringUtils.findString(values, "AL32UTF8", 0, 0);
/*  781 */       if (matchValue == null)
/*      */       {
/*  783 */         se = new ServiceException(msg);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  789 */       se = new ServiceException(msg, e);
/*      */     }
/*  791 */     if (se == null)
/*      */       return;
/*  793 */     if ((DataBinderUtils.getBoolean(db, "InstallTablesWithoutUTF8Encoding", false)) || (this.m_installer.getInstallBool("InstallTablesWithoutUTF8Encoding", false)))
/*      */     {
/*  796 */       this.m_installer.m_installLog.warning(msg);
/*      */     }
/*      */     else
/*      */     {
/*  800 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkFulltextPrivileges(DataBinder db, Workspace ws, String user)
/*      */     throws ServiceException
/*      */   {
/*  807 */     boolean hasWarning = false;
/*  808 */     String warning = LocaleUtils.encodeMessage("csInstallOracleDatabaseNoFullTextPrivilege", null);
/*      */ 
/*  810 */     ServiceException se = null;
/*      */     try
/*      */     {
/*  819 */       DataBinder binder = new DataBinder();
/*  820 */       binder.putLocal("databaseUser", user.toUpperCase());
/*      */ 
/*  822 */       ResultSet rset = ws.createResultSet("QoracleUserRoles", binder);
/*  823 */       String[][] values = ResultSetUtils.createStringTable(rset, new String[] { "GRANTED_ROLE" });
/*  824 */       String matchValue = StringUtils.findString(values, "STELLENT_ROLE", 0, 0);
/*  825 */       if (matchValue == null)
/*      */       {
/*  827 */         matchValue = StringUtils.findString(values, "CONTENTSERVER_ROLE", 0, 0);
/*      */       }
/*  829 */       if (matchValue == null)
/*      */       {
/*  831 */         matchValue = StringUtils.findString(values, "CTXAPP", 0, 0);
/*  832 */         if (matchValue == null)
/*      */         {
/*  834 */           hasWarning = true;
/*      */         }
/*      */         else
/*      */         {
/*  838 */           rset = ws.createResultSet("QoracleUserPrivs", binder);
/*  839 */           values = ResultSetUtils.createStringTable(rset, new String[] { "PRIVILEGE" });
/*      */ 
/*  841 */           matchValue = StringUtils.findString(values, "CREATE TYPE", 0, 0);
/*  842 */           if (matchValue == null)
/*      */           {
/*  844 */             hasWarning = true;
/*      */           }
/*      */ 
/*  847 */           matchValue = StringUtils.findString(values, "CREATE PROCEDURE", 0, 0);
/*  848 */           if (matchValue == null)
/*      */           {
/*  850 */             hasWarning = true;
/*      */           }
/*      */         }
/*      */       }
/*  854 */       if (hasWarning)
/*      */       {
/*  856 */         se = new ServiceException(warning);
/*  857 */         this.m_installer.m_installLog.warning(warning);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  862 */       warning = LocaleUtils.encodeMessage("csInstallOracleDatabaseFullTextPrivilegeError", null);
/*  863 */       se = new ServiceException(warning, e);
/*      */     }
/*  865 */     if (se == null)
/*      */       return;
/*  867 */     if ((DataBinderUtils.getBoolean(db, "InstallTablesWithoutFullTextPriv", false)) || (this.m_installer.getInstallBool("InstallTablesWithoutFullTextPriv", false)))
/*      */     {
/*  870 */       this.m_installer.m_installLog.warning(warning);
/*      */     }
/*      */     else
/*      */     {
/*  874 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createInstance(DataBinder db)
/*      */     throws ServiceException
/*      */   {
/*  882 */     boolean ignoreDropException = true;
/*      */     Workspace ws;
/*      */     try
/*      */     {
/*  886 */       ws = openDatabase(db);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  890 */       Report.trace("install", null, e);
/*  891 */       String msg = LocaleUtils.encodeMessage("csInstallerTableCreationError", null);
/*      */ 
/*  893 */       this.m_installer.m_installLog.error(e.getMessage());
/*  894 */       this.m_installer.m_installLog.error(msg);
/*  895 */       return;
/*      */     }
/*      */ 
/*  898 */     Vector scriptFiles = new IdcVector();
/*  899 */     int totalStatementCount = 0;
/*      */ 
/*  901 */     for (int i = 0; i < this.m_installScriptFiles.length; ++i)
/*      */     {
/*  903 */       String file = this.m_installer.computeDestination(this.m_installScriptFiles[i]);
/*      */ 
/*  905 */       Reader r = null;
/*      */       Vector statements;
/*      */       try {
/*  908 */         r = new BufferedReader(new FileReader(file));
/*  909 */         statements = parseScriptFile(r);
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  913 */         String msg = LocaleUtils.encodeMessage("syUnableToReadFile", e.getMessage(), file);
/*      */ 
/*  917 */         throw new ServiceException(msg);
/*      */       }
/*      */       finally
/*      */       {
/*  921 */         FileUtils.closeObject(r);
/*      */       }
/*  923 */       scriptFiles.addElement(statements);
/*  924 */       totalStatementCount += statements.size();
/*      */     }
/*      */ 
/*  927 */     int completedStatementCount = 0;
/*  928 */     for (int i = 0; i < this.m_installScriptFiles.length; ++i)
/*      */     {
/*  930 */       Vector statements = (Vector)scriptFiles.elementAt(i);
/*  931 */       int count = statements.size();
/*  932 */       String msg = LocaleUtils.encodeMessage("csInstallerDBSource", null, this.m_installScriptFiles[i]);
/*  933 */       this.m_installer.reportProgress(0, msg, completedStatementCount, totalStatementCount);
/*      */ 
/*  935 */       boolean useUnicode = this.m_installer.getInstallBool("DatabaseUnicodeFields", false);
/*  936 */       boolean logStatements = this.m_installer.getInstallBool("LogSql", false);
/*  937 */       Vector rules = this.m_installer.parseArrayTrim(this.m_dbProps, "UnicodeTranslationRules");
/*      */ 
/*  940 */       for (int j = 0; j < count; ++j)
/*      */       {
/*  942 */         String statement = (String)statements.elementAt(j);
/*  943 */         boolean includeQuery = true;
/*  944 */         String tmp = statement.toLowerCase();
/*      */ 
/*  946 */         if (this.m_allowedStatements.length > 0)
/*      */         {
/*  948 */           includeQuery = false;
/*  949 */           for (int k = 0; k < this.m_allowedStatements.length; ++k)
/*      */           {
/*  951 */             if (!tmp.startsWith(this.m_allowedStatements[k]))
/*      */               continue;
/*  953 */             includeQuery = true;
/*  954 */             break;
/*      */           }
/*      */         }
/*      */ 
/*  958 */         if (includeQuery)
/*      */         {
/*      */           try
/*      */           {
/*  962 */             if (useUnicode)
/*      */             {
/*  964 */               statement = convertToUnicode(statement, rules);
/*      */             }
/*  966 */             if (logStatements)
/*      */             {
/*  968 */               msg = LocaleUtils.encodeMessage("csInstallerExecutingQuery", null, statement);
/*      */ 
/*  970 */               this.m_installer.m_installLog.notice(msg);
/*      */             }
/*  972 */             Report.trace("install", "running " + statement, null);
/*  973 */             ws.executeSQL(statement);
/*      */           }
/*      */           catch (DataException e)
/*      */           {
/*  977 */             if (!tmp.startsWith("drop"))
/*      */             {
/*  979 */               msg = LocaleUtils.encodeMessage("csInstallerDBCreationStatementError", null, statement);
/*      */ 
/*  981 */               this.m_installer.m_installLog.error(msg);
/*  982 */               Report.trace("install", null, e);
/*  983 */               throw new ServiceException(e);
/*      */             }
/*      */           }
/*      */         }
/*  987 */         ++completedStatementCount;
/*  988 */         this.m_installer.reportProgress(1, msg, completedStatementCount, totalStatementCount);
/*      */       }
/*      */ 
/*  992 */       if (!logStatements)
/*      */         continue;
/*  994 */       msg = LocaleUtils.encodeMessage("csInstallerScriptComplete", null, this.m_installScriptFiles[i]);
/*      */ 
/*  996 */       this.m_installer.m_installLog.notice(msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Vector parseScriptFile(Reader r)
/*      */     throws IOException
/*      */   {
/* 1003 */     Vector v = new IdcVector();
/* 1004 */     char[] delimeter = new char[this.m_statementDelimiter.length()];
/* 1005 */     IdcStringBuilder buf = new IdcStringBuilder();
/* 1006 */     int c = 0;
/* 1007 */     int delimCount = 0;
/* 1008 */     int state = 84;
/*      */ 
/* 1016 */     this.m_statementDelimiter.getChars(0, delimeter.length, delimeter, 0);
/*      */     do
/*      */     {
/* 1020 */       char last = (char)c;
/* 1021 */       c = r.read();
/*      */ 
/* 1023 */       switch (state)
/*      */       {
/*      */       case 84:
/* 1026 */         if ((c & 0xFFDF) == (delimeter[0] & 0xFFDF))
/*      */         {
/* 1028 */           delimeter[0] = (char)c;
/* 1029 */           delimCount = 1;
/* 1030 */           state = 100;
/*      */         }
/*      */         else
/*      */         {
/* 1034 */           switch (c)
/*      */           {
/*      */           case 47:
/* 1036 */             state = 99; break;
/*      */           case -1:
/* 1037 */             break;
/*      */           default:
/* 1038 */             buf.append((char)c); } 
/* 1038 */         }break;
/*      */       case 99:
/* 1044 */         switch (c)
/*      */         {
/* 1048 */         case 42:
/* 1046 */           state = 67; break;
/*      */         case -1:
/* 1047 */           buf.append(last); break;
/*      */         default:
/* 1048 */           buf.append(last); buf.append((char)c); state = 84; } break;
/*      */       case 67:
/* 1053 */         switch (c)
/*      */         {
/*      */         case 42:
/* 1055 */           state = 101; break;
/*      */         case -1:
/* 1056 */           throw new IOException("!csInstallerStreamParseError");
/*      */         }
/* 1058 */         break;
/*      */       case 101:
/* 1061 */         switch (c)
/*      */         {
/* 1066 */         case 47:
/* 1063 */           state = 84; break;
/*      */         case 42:
/* 1064 */           break;
/*      */         case -1:
/* 1065 */           throw new IOException("!csInstallerStreamParseError");
/*      */         default:
/* 1066 */           state = 67; } break;
/*      */       case 100:
/* 1071 */         if (delimCount == delimeter.length)
/*      */         {
/* 1073 */           if ((!Character.isWhitespace((char)c)) && (c != -1))
/*      */           {
/* 1075 */             buf.append(delimeter, 0, delimCount);
/* 1076 */             state = (c == 47) ? 99 : 84;
/*      */           }
/*      */           else
/*      */           {
/* 1080 */             state = 84;
/* 1081 */             String query = buf.toString();
/* 1082 */             query = query.trim();
/* 1083 */             v.addElement(query);
/* 1084 */             buf = new IdcStringBuilder();
/*      */           }
/*      */ 
/*      */         }
/* 1089 */         else if (c == -1)
/*      */         {
/* 1091 */           buf.append(last);
/*      */         }
/* 1093 */         else if ((c & 0xFFDF) == (delimeter[delimCount] & 0xFFDF))
/*      */         {
/* 1095 */           delimeter[delimCount] = (char)c;
/* 1096 */           ++delimCount;
/*      */         }
/*      */         else
/*      */         {
/* 1100 */           state = (c == 47) ? 99 : 84;
/* 1101 */           buf.append(delimeter, 0, delimCount);
/* 1102 */           if (c == 47)
/*      */           {
/* 1104 */             state = 99;
/*      */           }
/*      */           else
/*      */           {
/* 1108 */             state = 84;
/* 1109 */             buf.append((char)c);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1115 */     while (c != -1);
/*      */ 
/* 1117 */     String tmp = buf.toString().trim();
/* 1118 */     if (tmp.length() > 0)
/*      */     {
/* 1120 */       v.addElement(tmp);
/*      */     }
/* 1122 */     buf.releaseBuffers();
/*      */ 
/* 1124 */     return v;
/*      */   }
/*      */ 
/*      */   public String convertToUnicode(String query, Vector rulesVector)
/*      */   {
/* 1129 */     IdcStringBuilder buf = null;
/* 1130 */     String oldQuery = query;
/*      */ 
/* 1132 */     int ruleCount = rulesVector.size();
/* 1133 */     for (int i = 0; i < ruleCount; ++i)
/*      */     {
/* 1135 */       String tmp = (String)rulesVector.elementAt(i);
/* 1136 */       Vector v = StringUtils.parseArray(tmp, ',', '^');
/* 1137 */       if (v.size() != 2)
/*      */       {
/* 1139 */         Report.trace("install", "unicode conversion rule \"" + tmp + "\" has the wrong number of items.", null);
/*      */       }
/*      */       else
/*      */       {
/* 1143 */         int queryLen = query.length();
/* 1144 */         String oldT = (String)v.elementAt(0);
/* 1145 */         int oldTLen = oldT.length();
/* 1146 */         String newT = (String)v.elementAt(1);
/*      */ 
/* 1148 */         int oldJ = 0;
/* 1149 */         int j = 0;
/* 1150 */         while ((j = query.indexOf(oldT, j)) >= 0)
/*      */         {
/* 1152 */           boolean skipThis = false;
/* 1153 */           if ((j > 0) && (Character.isJavaIdentifierPart(query.charAt(j - 1))))
/*      */           {
/* 1155 */             skipThis = true;
/*      */           }
/* 1157 */           if ((j + oldTLen < queryLen) && (Character.isJavaIdentifierPart(query.charAt(j + oldTLen)) == Character.isJavaIdentifierPart(query.charAt(j))))
/*      */           {
/* 1161 */             skipThis = true;
/*      */           }
/* 1163 */           if (skipThis)
/*      */           {
/* 1165 */             ++j;
/*      */           }
/*      */ 
/* 1169 */           if (buf == null)
/*      */           {
/* 1171 */             buf = new IdcStringBuilder();
/*      */           }
/* 1173 */           buf.append(query.substring(oldJ, j));
/* 1174 */           buf.append(newT);
/* 1175 */           oldJ = j + oldTLen;
/* 1176 */           j = oldJ;
/*      */         }
/*      */ 
/* 1179 */         if (buf == null)
/*      */           continue;
/* 1181 */         buf.append(query.substring(oldJ));
/* 1182 */         query = buf.toString();
/* 1183 */         buf.releaseBuffers();
/* 1184 */         buf = null;
/*      */       }
/*      */     }
/*      */ 
/* 1188 */     if (oldQuery != query)
/*      */     {
/* 1190 */       Report.trace("install", "Transformed \"" + oldQuery + "\" into \"" + query + "\".", null);
/*      */     }
/*      */ 
/* 1193 */     return query;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1198 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.DatabaseInstaller
 * JD-Core Version:    0.5.4
 */