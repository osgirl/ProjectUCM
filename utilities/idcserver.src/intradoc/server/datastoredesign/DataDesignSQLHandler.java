/*      */ package intradoc.server.datastoredesign;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.DynamicData;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.server.Action;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.IdcServiceAction;
/*      */ import intradoc.server.LegacyDirectoryLocator;
/*      */ import intradoc.server.PageMerger;
/*      */ import intradoc.server.Service;
/*      */ import intradoc.server.ServiceHandler;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataDesignSQLHandler extends ServiceHandler
/*      */ {
/*      */   protected String m_fileList;
/*      */   protected String m_dropallSyntax;
/*      */   protected String m_dbName;
/*      */   protected String m_componentName;
/*      */   protected IdcStringBuilder m_SQLCommands;
/*      */   protected String m_format;
/*      */   protected String m_endformat;
/*      */   protected DataDesignGenerator m_myGenerator;
/*      */   protected BufferedWriter m_out;
/*      */ 
/*      */   public DataDesignSQLHandler()
/*      */   {
/*   35 */     this.m_componentName = "";
/*      */   }
/*      */ 
/*      */   public void init()
/*      */   {
/*   50 */     this.m_fileList = "dsdFilesList";
/*   51 */     this.m_dropallSyntax = "generateSqlDropallSyntax";
/*   52 */     this.m_format = "";
/*   53 */     this.m_endformat = "";
/*   54 */     this.m_SQLCommands = new IdcStringBuilder();
/*   55 */     this.m_myGenerator = new DataDesignGenerator();
/*   56 */     boolean expandedDBSupport = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("expandedDBSupport"), false);
/*   57 */     if (!expandedDBSupport)
/*      */       return;
/*   59 */     this.m_binder.putLocal("dsdExpandedDBSupport", "1");
/*      */   }
/*      */ 
/*      */   public void generateSQL()
/*      */     throws DataException, ServiceException
/*      */   {
/*   71 */     init();
/*      */ 
/*   74 */     String generatedTablesList = this.m_binder.getLocal("generatedTablesList");
/*   75 */     if (generatedTablesList == null)
/*      */     {
/*   77 */       generatedTablesList = "core";
/*   78 */       this.m_binder.putLocal("generatedTablesList", generatedTablesList);
/*      */     }
/*   80 */     generatedTablesList = StringUtils.removeWhitespace(generatedTablesList);
/*   81 */     Vector scriptVect = StringUtils.parseArray(generatedTablesList, ',', '^');
/*   82 */     Set scriptSet = new HashSet(scriptVect);
/*      */ 
/*   85 */     Set components = getComponentSet();
/*   86 */     DataResultSet componentDrset = new DataResultSet(new String[] { "name" });
/*   87 */     Iterator iter = components.iterator();
/*   88 */     while (iter.hasNext())
/*      */     {
/*   90 */       String componentName = (String)iter.next();
/*   91 */       Vector v = new Vector();
/*   92 */       v.add(componentName);
/*   93 */       componentDrset.addRow(v);
/*      */     }
/*   95 */     this.m_binder.addResultSet("componentSet", componentDrset);
/*      */ 
/*   97 */     boolean delete = false;
/*   98 */     String database = this.m_binder.getLocal("dbType");
/*   99 */     String deleteOperation = this.m_binder.getLocal("delete");
/*  100 */     if (deleteOperation != null)
/*      */     {
/*  102 */       delete = deleteOperation.compareTo("1") == 0;
/*      */     }
/*  104 */     if (delete)
/*      */     {
/*  107 */       deleteGeneratedScriptInfo(this.m_binder.getLocal("db"));
/*  108 */       database = null;
/*      */     }
/*      */ 
/*  112 */     String rcu = this.m_binder.getLocal("rcu");
/*  113 */     boolean rcu_requested = StringUtils.convertToBool(rcu, false);
/*  114 */     String ebr = this.m_binder.getLocal("ebr");
/*  115 */     boolean ebr_requested = StringUtils.convertToBool(ebr, false);
/*      */ 
/*  117 */     if (database == null)
/*      */     {
/*  121 */       loadGeneratedScriptInfo();
/*  122 */       return;
/*      */     }
/*  124 */     if (database.compareTo("") == 0)
/*      */     {
/*  127 */       String msg = "wwDsdDbNotSelected";
/*  128 */       this.m_binder.putLocal("warning", msg);
/*  129 */       return;
/*      */     }
/*  131 */     if (database.compareTo("all") == 0)
/*      */     {
/*  133 */       generateAllSQL(rcu_requested, ebr_requested);
/*  134 */       return;
/*      */     }
/*      */ 
/*  137 */     String unicode = this.m_binder.getLocal("unicode");
/*  138 */     if ((unicode != null) && 
/*  140 */       (unicode.compareTo("1") == 0))
/*      */     {
/*  144 */       database = database + "_unicode";
/*      */     }
/*      */ 
/*  148 */     if ((rcu_requested) && (((database.startsWith("mssql")) || (database.startsWith("db2")))))
/*      */     {
/*  153 */       database = database + "_rcu";
/*      */     }
/*      */ 
/*  156 */     this.m_dbName = database;
/*  157 */     this.m_myGenerator.init(database);
/*  158 */     String generatedDBType = "";
/*      */ 
/*  160 */     if (scriptSet.contains("core"))
/*      */     {
/*  162 */       generatedDBType = generatedDBType + database;
/*      */ 
/*  165 */       DataResultSet fileset = getTable(this.m_fileList);
/*      */ 
/*  167 */       for (int i = 0; i < fileset.getNumRows(); ++i)
/*      */       {
/*  169 */         List files = fileset.getRowAsList(i);
/*  170 */         String filename = (String)files.get(0);
/*      */         try
/*      */         {
/*  173 */           createFile(filename);
/*  174 */           generateFile(filename);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*  186 */           FileUtils.closeObject(this.m_out);
/*      */ 
/*  190 */           this.m_binder.putLocal("dbName", this.m_dbName);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  198 */     iter = components.iterator();
/*  199 */     while (iter.hasNext())
/*      */     {
/*  201 */       String componentName = (String)iter.next();
/*  202 */       if ((scriptSet.contains("components")) || (scriptSet.contains(componentName)))
/*      */       {
/*      */         try
/*      */         {
/*  211 */           this.m_componentName = ("_" + componentName);
/*  212 */           String filepath = createFile(componentName + "_default");
/*  213 */           boolean fileGenerated = generateComponentFile(componentName, "default");
/*  214 */           FileUtils.closeObject(this.m_out);
/*  215 */           if (!fileGenerated)
/*      */           {
/*  217 */             FileUtils.deleteFile(filepath);
/*      */           }
/*  219 */           createFile(componentName);
/*  220 */           generateComponentFile(componentName, "tables");
/*  221 */           generatedDBType = generatedDBType + "|" + database + this.m_componentName;
/*  222 */           this.m_componentName = "";
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*  234 */           FileUtils.closeObject(this.m_out);
/*      */ 
/*  238 */           this.m_binder.putLocal("dbName", this.m_dbName);
/*      */         }
/*      */       }
/*      */     }
/*  242 */     this.m_binder.putLocal("generatedDBType", generatedDBType);
/*      */ 
/*  245 */     loadGeneratedScriptInfo();
/*      */   }
/*      */ 
/*      */   protected void generateAllSQL(boolean rcu, boolean ebr)
/*      */     throws DataException, ServiceException
/*      */   {
/*  257 */     DataResultSet dbConv = getTable("dsdConversionDefinitions");
/*  258 */     for (int i = 0; i < dbConv.getNumRows(); ++i)
/*      */     {
/*  260 */       List conversionFields = dbConv.getRowAsList(i);
/*  261 */       String database = (String)conversionFields.get(0);
/*  262 */       if ((!rcu) && (database.endsWith("rcu")))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  267 */       if ((ebr) && (!database.contains("ebr")))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  272 */       this.m_dbName = database;
/*  273 */       this.m_myGenerator.init(database);
/*      */ 
/*  276 */       DataResultSet fileset = getTable(this.m_fileList);
/*      */ 
/*  278 */       for (int j = 0; j < fileset.getNumRows(); ++j)
/*      */       {
/*  280 */         List files = fileset.getRowAsList(j);
/*  281 */         String filename = (String)files.get(0);
/*      */         try
/*      */         {
/*  284 */           createFile(filename);
/*  285 */           generateFile(filename);
/*      */         }
/*      */         catch (DataException e)
/*      */         {
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*      */         }
/*      */         finally
/*      */         {
/*  297 */           FileUtils.closeObject(this.m_out);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  302 */       if (!ebr)
/*      */         continue;
/*  304 */       generateAllComponentSQL();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void generateAllComponentSQL()
/*      */     throws DataException, ServiceException
/*      */   {
/*  319 */     Set components = getComponentSet();
/*  320 */     for (String componentName : components)
/*      */     {
/*      */       try
/*      */       {
/*  327 */         this.m_componentName = ("_" + componentName);
/*  328 */         String filepath = createFile(componentName + "_default");
/*  329 */         boolean fileGenerated = generateComponentFile(componentName, "default");
/*  330 */         FileUtils.closeObject(this.m_out);
/*  331 */         if (!fileGenerated)
/*      */         {
/*  333 */           FileUtils.deleteFile(filepath);
/*      */         }
/*  335 */         createFile(componentName);
/*  336 */         generateComponentFile(componentName, "tables");
/*  337 */         this.m_componentName = "";
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*      */       }
/*      */       finally
/*      */       {
/*  349 */         FileUtils.closeObject(this.m_out);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getSQLDir()
/*      */   {
/*  356 */     String sqldir = SharedObjects.getEnvironmentValue("DatabaseScriptDir");
/*  357 */     if (sqldir != null)
/*      */     {
/*  359 */       return FileUtils.directorySlashes(sqldir);
/*      */     }
/*  361 */     String basedir = LegacyDirectoryLocator.getIntradocDir();
/*  362 */     return FileUtils.getAbsolutePath(basedir, "data/datastoredesign") + "/";
/*      */   }
/*      */ 
/*      */   protected Set<String> getComponentSet()
/*      */     throws ServiceException, DataException
/*      */   {
/*  373 */     Set componentSet = new HashSet();
/*  374 */     DataResultSet tableset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignTableList", "TableList", DataDesignInstallUtils.getFieldNameUpgradeMap());
/*      */ 
/*  376 */     for (int i = 0; i < tableset.getNumRows(); ++i)
/*      */     {
/*  378 */       List tableEntry = tableset.getRowAsList(i);
/*  379 */       String entryComponent = (String)tableEntry.get(1);
/*  380 */       if ((entryComponent.compareTo("SystemTable") == 0) || 
/*  382 */         (componentSet.contains(entryComponent)))
/*      */         continue;
/*  384 */       componentSet.add(entryComponent);
/*      */     }
/*      */ 
/*  388 */     return componentSet;
/*      */   }
/*      */ 
/*      */   protected void loadGeneratedScriptInfo()
/*      */   {
/*  393 */     String[] columns = { "dbname", "timestamp" };
/*  394 */     DataResultSet scriptSet = new DataResultSet(columns);
/*      */ 
/*  397 */     String sqldir = getSQLDir();
/*  398 */     File scripts = FileUtilsCfgBuilder.getCfgFile(sqldir, "datastoredesign", true);
/*  399 */     String[] files = scripts.list();
/*  400 */     IdcDateFormat dateFormat = new IdcDateFormat();
/*  401 */     dateFormat.initDefault();
/*  402 */     for (int i = 0; (files != null) && (i < files.length); ++i)
/*      */     {
/*  404 */       File dFile = FileUtilsCfgBuilder.getCfgFile(sqldir + files[i], "datastoredesign", false);
/*  405 */       if (!dFile.isDirectory())
/*      */         continue;
/*  407 */       Vector v = new Vector();
/*  408 */       v.add(files[i]);
/*  409 */       long epoch_ms = dFile.lastModified();
/*  410 */       Date date = new Date(epoch_ms);
/*  411 */       v.add(LocaleResources.m_odbcFormat.format(date));
/*  412 */       scriptSet.addRow(v);
/*      */     }
/*      */ 
/*  415 */     this.m_binder.m_blFieldTypes.put("timestamp", "date");
/*  416 */     this.m_binder.addResultSet("scriptSet", scriptSet);
/*      */   }
/*      */ 
/*      */   protected void deleteGeneratedScriptInfo(String database)
/*      */   {
/*  421 */     String db = database;
/*  422 */     if (database == null)
/*      */       return;
/*  424 */     db = db.replaceAll("<b>", "");
/*  425 */     db = db.replaceAll("</b>", "");
/*  426 */     String sqldir = getSQLDir();
/*  427 */     String scriptdir = FileUtils.directorySlashes(sqldir + db + "/");
/*  428 */     File deleteme = FileUtilsCfgBuilder.getCfgFile(scriptdir, "datastoredesign", true);
/*  429 */     String[] files = deleteme.list();
/*  430 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/*  432 */       File dFile = FileUtilsCfgBuilder.getCfgFile(scriptdir + files[i], "datastoredesign", false);
/*  433 */       dFile.delete();
/*      */     }
/*  435 */     deleteme.delete();
/*      */ 
/*  439 */     String zipdir = DirectoryLocator.getSystemBaseDirectory("data") + "datastoredesign/";
/*  440 */     if (zipdir.equals(sqldir))
/*      */       return;
/*  442 */     zipdir = FileUtils.directorySlashes(zipdir + db + "/");
/*  443 */     File zipfile = new File(zipdir);
/*  444 */     String[] ziplist = zipfile.list();
/*  445 */     if ((ziplist != null) && (ziplist.length > 0))
/*      */     {
/*  447 */       for (int i = 0; i < ziplist.length; ++i)
/*      */       {
/*  449 */         File dzip = new File(zipdir + ziplist[i]);
/*  450 */         dzip.delete();
/*      */       }
/*      */     }
/*  453 */     zipfile.delete();
/*      */   }
/*      */ 
/*      */   public static DataResultSet getTable(String tablename)
/*      */     throws ServiceException
/*      */   {
/*  467 */     DataResultSet tableDefinition = SharedObjects.getTable(tablename);
/*      */ 
/*  469 */     return tableDefinition;
/*      */   }
/*      */ 
/*      */   protected void generateFile(String filename)
/*      */     throws ServiceException, DataException
/*      */   {
/*  481 */     if (filename.compareTo("dropall") == 0)
/*      */     {
/*  489 */       DataResultSet fileset = getTable(this.m_fileList);
/*  490 */       for (int i = 0; i < fileset.getNumRows(); ++i)
/*      */       {
/*  492 */         List files = fileset.getRowAsList(i);
/*  493 */         String file = (String)files.get(0);
/*  494 */         if ((file.compareTo("dropall") == 0) || (file.compareTo("default") == 0)) {
/*      */           continue;
/*      */         }
/*  497 */         writeDropStatements(getTable("dsd" + file + "Definition"));
/*      */       }
/*      */ 
/*      */     }
/*  501 */     else if (filename.compareTo("default") == 0)
/*      */     {
/*  503 */       DataResultSet tableset = getTable("DataStoreDesignTableList");
/*  504 */       for (int i = 0; i < tableset.getNumRows(); ++i)
/*      */       {
/*  506 */         List tableEntry = tableset.getRowAsList(i);
/*  507 */         String component = (String)tableEntry.get(1);
/*  508 */         boolean isDynamicTable = StringUtils.convertToBool((String)tableEntry.get(5), false);
/*  509 */         if (component.compareTo("SystemTable") != 0)
/*      */           continue;
/*  511 */         String tablename = (String)tableEntry.get(0);
/*  512 */         if (getTable("SystemTable." + tablename + "." + "DefaultValues") == null)
/*      */           continue;
/*  514 */         writeSQL(this.m_myGenerator.generateSQLDefaultStatement("SystemTable", tablename, isDynamicTable));
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  522 */       DataResultSet fileDefinition = getTable("dsd" + filename + "Definition");
/*      */ 
/*  529 */       for (int i = 0; i < fileDefinition.getNumRows(); ++i)
/*      */       {
/*  531 */         List tables = fileDefinition.getRowAsList(i);
/*      */ 
/*  533 */         String tablename = (String)tables.get(0);
/*  534 */         boolean isDynamicTable = false;
/*      */ 
/*  537 */         DataResultSet tableset = getTable("DataStoreDesignTableList");
/*  538 */         for (int j = 0; j < tableset.getNumRows(); ++j)
/*      */         {
/*  540 */           List tableEntry = tableset.getRowAsList(j);
/*  541 */           String component = (String)tableEntry.get(1);
/*  542 */           if (component.compareTo("SystemTable") != 0)
/*      */             continue;
/*  544 */           String tableNameDef = (String)tableEntry.get(0);
/*  545 */           if (tableNameDef.compareToIgnoreCase(tablename) != 0)
/*      */             continue;
/*  547 */           isDynamicTable = StringUtils.convertToBool((String)tableEntry.get(5), false);
/*  548 */           break;
/*      */         }
/*      */ 
/*  552 */         writeSQL(this.m_myGenerator.generateSQLTableDefinition("SystemTable", tablename, isDynamicTable));
/*  553 */         writeSQL("\n");
/*  554 */         if (!isDynamicTable)
/*      */         {
/*  556 */           writeSQL(this.m_myGenerator.generateSQLViewDefinition("SystemTable", tablename));
/*  557 */           writeSQL("\n");
/*      */         }
/*  559 */         String[] indexSyntax = this.m_myGenerator.generateSQLIndexDefinition(tablename, getIndexes(tablename), isDynamicTable);
/*  560 */         for (int j = 0; j < indexSyntax.length; ++j)
/*      */         {
/*  562 */           writeSQL(indexSyntax[j]);
/*      */         }
/*  564 */         writeSQL("\n");
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean generateComponentFile(String component, String filename)
/*      */     throws ServiceException, DataException
/*      */   {
/*  580 */     boolean fileGenerated = false;
/*  581 */     Map componentTables = new HashMap();
/*  582 */     DataResultSet tableset = DataDesignInstallUtils.getUpgradedAndRenamedTable("DataStoreDesignTableList", "TableList", DataDesignInstallUtils.getFieldNameUpgradeMap());
/*      */ 
/*  585 */     for (int i = 0; i < tableset.getNumRows(); ++i)
/*      */     {
/*  587 */       List tableEntry = tableset.getRowAsList(i);
/*  588 */       String entryComponent = (String)tableEntry.get(tableset.getFieldInfoIndex("dsdComponentName"));
/*  589 */       String dynamicTable = (String)tableEntry.get(tableset.getFieldInfoIndex("dsdIsDynamicTable"));
/*  590 */       if (entryComponent.compareToIgnoreCase(component) != 0)
/*      */         continue;
/*  592 */       String tablename = (String)tableEntry.get(0);
/*      */ 
/*  597 */       DataResultSet coreTable = getTable("SystemTable." + tablename);
/*  598 */       if (coreTable != null) {
/*      */         continue;
/*      */       }
/*  601 */       componentTables.put(tablename, dynamicTable);
/*      */     }
/*      */ 
/*  606 */     if (filename.compareTo("default") == 0)
/*      */     {
/*  608 */       Set keys = componentTables.keySet();
/*  609 */       Iterator iter = keys.iterator();
/*  610 */       while (iter.hasNext())
/*      */       {
/*  612 */         String tablename = (String)iter.next();
/*  613 */         if (getTable(component + "." + tablename + "." + "DefaultValues") != null)
/*      */         {
/*  615 */           boolean isDynamicTable = StringUtils.convertToBool((String)componentTables.get(tablename), false);
/*  616 */           writeSQL(this.m_myGenerator.generateSQLDefaultStatement(component, tablename, isDynamicTable));
/*  617 */           fileGenerated = true;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  624 */       String[] columns = { "tables" };
/*  625 */       DataResultSet fileDefinition = new DataResultSet(columns);
/*  626 */       Set keys = componentTables.keySet();
/*  627 */       Iterator iter = keys.iterator();
/*  628 */       while (iter.hasNext())
/*      */       {
/*  630 */         String tablename = (String)iter.next();
/*  631 */         Vector v = new Vector();
/*  632 */         v.add(tablename);
/*  633 */         fileDefinition.addRow(v);
/*      */       }
/*      */ 
/*  637 */       writeDropStatements(fileDefinition);
/*      */ 
/*  640 */       for (int i = 0; i < fileDefinition.getNumRows(); ++i)
/*      */       {
/*  642 */         List tables = fileDefinition.getRowAsList(i);
/*      */ 
/*  644 */         String tablename = (String)tables.get(0);
/*  645 */         boolean isDynamicTable = StringUtils.convertToBool((String)componentTables.get(tablename), false);
/*  646 */         writeSQL(this.m_myGenerator.generateSQLTableDefinition(component, tablename, isDynamicTable));
/*  647 */         writeSQL("\n");
/*  648 */         if (!isDynamicTable)
/*      */         {
/*  650 */           writeSQL(this.m_myGenerator.generateSQLViewDefinition(component, tablename));
/*  651 */           writeSQL("\n");
/*      */         }
/*  653 */         String[] indexSyntax = this.m_myGenerator.generateSQLIndexDefinition(tablename, getIndexes(tablename), isDynamicTable);
/*  654 */         for (int j = 0; j < indexSyntax.length; ++j)
/*      */         {
/*  656 */           writeSQL(indexSyntax[j]);
/*      */         }
/*  658 */         writeSQL("\n");
/*  659 */         fileGenerated = true;
/*      */       }
/*      */     }
/*      */ 
/*  663 */     return fileGenerated;
/*      */   }
/*      */ 
/*      */   protected void writeDropStatements(DataResultSet tableSet) throws ServiceException
/*      */   {
/*  668 */     for (int i = 0; i < tableSet.getNumRows(); ++i)
/*      */     {
/*  670 */       List tables = tableSet.getRowAsList(i);
/*  671 */       String tableName = (String)tables.get(0);
/*  672 */       writeSQL(this.m_myGenerator.generateSQLDropStatement(tableName));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static DataResultSet getIndexes(String tableName) throws ServiceException
/*      */   {
/*  678 */     String[] columns = { "indexName", "indexColumns" };
/*  679 */     DataResultSet results = new DataResultSet(columns);
/*      */ 
/*  686 */     DataResultSet indexTable = getTable("dsdIndexTable");
/*  687 */     DataResultSet componentIndexesLegacy = getTable("ColumnsIndexed");
/*  688 */     DataResultSet componentIndexes = getTable("DataStoreDesignColumnsIndexed");
/*  689 */     DataResultSet tableIndexes = new DataResultSet();
/*  690 */     DataResultSet componentIndexesLegacyFiltered = new DataResultSet();
/*  691 */     DataResultSet componentIndexesFiltered = new DataResultSet();
/*      */ 
/*  695 */     tableIndexes.copySimpleFiltered(indexTable, "table", tableName);
/*  696 */     componentIndexesLegacyFiltered.copySimpleFiltered(componentIndexesLegacy, "dTableName", tableName);
/*  697 */     componentIndexesFiltered.copySimpleFiltered(componentIndexes, "dsdTableName", tableName);
/*      */ 
/*  701 */     mergeIndexes(componentIndexesLegacyFiltered, tableIndexes);
/*  702 */     mergeIndexes(componentIndexesFiltered, tableIndexes);
/*      */ 
/*  704 */     for (int i = 0; i < tableIndexes.getNumRows(); ++i)
/*      */     {
/*  706 */       String name = (String)tableIndexes.getRowAsList(i).get(1);
/*      */ 
/*  708 */       Vector indexCols = tableIndexes.getRowValues(i);
/*      */ 
/*  711 */       Vector v = new Vector();
/*  712 */       v.add(name);
/*  713 */       v.add(indexCols);
/*  714 */       results.addRow(v);
/*      */     }
/*      */ 
/*  717 */     return results;
/*      */   }
/*      */ 
/*      */   protected static void mergeIndexes(DataResultSet srcIndexes, DataResultSet destIndexes)
/*      */   {
/*  742 */     if (srcIndexes.isEmpty())
/*      */       return;
/*  744 */     srcIndexes.first();
/*  745 */     boolean done = false;
/*  746 */     while (!done)
/*      */     {
/*  748 */       Vector src = srcIndexes.getCurrentRowValues();
/*      */ 
/*  750 */       Vector v = new Vector();
/*  751 */       v.add(src.get(1));
/*  752 */       v.add(src.get(2));
/*  753 */       v.add(src.get(2));
/*  754 */       v.add("");
/*  755 */       v.add("");
/*  756 */       v.add("");
/*  757 */       v.add("");
/*  758 */       v.add("");
/*  759 */       v.add("");
/*  760 */       destIndexes.addRow(v);
/*  761 */       done = !srcIndexes.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void writeSQL(String statement)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  777 */       this.m_out.write(statement);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  781 */       String msg = LocaleUtils.encodeMessage("csUnableToSaveContent", null);
/*  782 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String createFile(String filename)
/*      */     throws ServiceException
/*      */   {
/*  790 */     String sqldir = getSQLDir() + this.m_dbName + this.m_componentName + "/";
/*  791 */     String filepath = sqldir + filename + ".sql";
/*  792 */     this.m_binder.putLocal("fileLoc", sqldir);
/*      */ 
/*  794 */     FileUtils.checkOrCreateDirectory(sqldir, 3);
/*  795 */     Date now = new Date();
/*  796 */     File myDir = FileUtilsCfgBuilder.getCfgFile(sqldir, "datastoredesign", true);
/*  797 */     myDir.setLastModified(now.getTime());
/*      */     try
/*      */     {
/*  801 */       this.m_out = new BufferedWriter(FileUtilsCfgBuilder.getCfgWriter(filepath, "datastoredesign"));
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  805 */       String msg = LocaleUtils.encodeMessage("csUnableToSaveContent", null, sqldir + filename + ".sql");
/*  806 */       this.m_service.createServiceException(null, msg);
/*  807 */       filepath = "failure to create file";
/*      */     }
/*      */ 
/*  810 */     return filepath;
/*      */   }
/*      */ 
/*      */   public void download()
/*      */     throws ServiceException, DataException
/*      */   {
/*  816 */     String db = this.m_binder.getLocal("db");
/*      */ 
/*  818 */     if (db == null)
/*      */       return;
/*  820 */     db = db.replaceAll("<b>", "");
/*  821 */     db = db.replaceAll("</b>", "");
/*  822 */     String[] fieldColumns = { "fileName", "filePath" };
/*  823 */     DataResultSet zipsrc = new DataResultSet(fieldColumns);
/*  824 */     String basedir = LegacyDirectoryLocator.getIntradocDir();
/*  825 */     String sqldir = basedir + "data/datastoredesign/" + db + "/";
/*  826 */     String zipdir = DirectoryLocator.getSystemBaseDirectory("data") + "datastoredesign/" + db + "/";
/*  827 */     FileUtils.checkOrCreateDirectory(zipdir, 3);
/*  828 */     String zipfile = zipdir + db + "_sql_scripts.zip";
/*      */ 
/*  830 */     init();
/*      */ 
/*  833 */     File scripts = FileUtilsCfgBuilder.getCfgFile(sqldir, "datastoredesign", true);
/*  834 */     String[] files = scripts.list();
/*      */ 
/*  836 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/*  839 */       ArrayList row = new ArrayList();
/*  840 */       row.add(files[i]);
/*  841 */       row.add(sqldir + files[i]);
/*  842 */       zipsrc.addRowWithList(row);
/*      */     }
/*      */ 
/*  845 */     ZipFunctions.createZipFile(zipfile, zipsrc);
/*      */ 
/*  847 */     DataStreamWrapper streamWrapper = this.m_service.getDownloadStream(true);
/*  848 */     String resultFileName = db + "_sql_scripts.zip";
/*  849 */     String resultFilePath = zipfile;
/*  850 */     String resultFileFormat = "Application/zip";
/*  851 */     streamWrapper.setSimpleFileData(resultFilePath, resultFileName, resultFileFormat);
/*  852 */     streamWrapper.m_useStream = true;
/*  853 */     return;
/*      */   }
/*      */ 
/*      */   public void view()
/*      */     throws ServiceException, DataException
/*      */   {
/*  860 */     this.m_binder.putLocal("viewPage", "1");
/*      */ 
/*  862 */     String db = this.m_binder.getLocal("db");
/*      */ 
/*  864 */     if (db == null) {
/*      */       return;
/*      */     }
/*  867 */     db = db.replaceAll("<b>", "");
/*  868 */     db = db.replaceAll("</b>", "");
/*  869 */     String sqldir = getSQLDir() + db + "/";
/*      */ 
/*  871 */     DataResultSet scriptDrset = new DataResultSet(new String[] { "name", "script" });
/*  872 */     File scriptLoc = FileUtilsCfgBuilder.getCfgFile(sqldir, "datastoredesign", true);
/*  873 */     File[] scripts = scriptLoc.listFiles();
/*  874 */     for (int i = 0; i < scripts.length; ++i)
/*      */     {
/*  876 */       String scriptname = scripts[i].getName();
/*  877 */       String scriptContents = readScript(scripts[i]);
/*      */ 
/*  879 */       if (!scriptname.endsWith(".sql"))
/*      */         continue;
/*  881 */       Vector v = new Vector();
/*  882 */       v.add(scriptname);
/*  883 */       v.add(scriptContents);
/*  884 */       scriptDrset.addRow(v);
/*      */     }
/*      */ 
/*  887 */     this.m_binder.addResultSet("scriptSet", scriptDrset);
/*      */   }
/*      */ 
/*      */   protected String readScript(File inputfile)
/*      */     throws ServiceException
/*      */   {
/*  893 */     IdcStringBuilder contents = new IdcStringBuilder();
/*      */     try
/*      */     {
/*  896 */       BufferedReader input = new BufferedReader(FileUtilsCfgBuilder.getCfgReader(inputfile));
/*      */       try
/*      */       {
/*  899 */         String line = null;
/*  900 */         while ((line = input.readLine()) != null)
/*      */         {
/*  902 */           contents.append(line);
/*  903 */           contents.append(System.getProperty("line.separator"));
/*      */         }
/*      */       }
/*      */       finally
/*      */       {
/*  908 */         input.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ex)
/*      */     {
/*  913 */       ex.printStackTrace();
/*  914 */       String msg = LocaleUtils.encodeMessage("csCouldNotLoadFile", null, inputfile.getAbsoluteFile());
/*  915 */       this.m_service.createServiceException(null, msg);
/*      */     }
/*      */ 
/*  918 */     return contents.toString();
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void computeDataStoreDesignIdocResources() throws DataException
/*      */   {
/*  924 */     String resourceListTableName = this.m_currentAction.getParamAt(0);
/*  925 */     PageMerger merger = this.m_service.getPageMerger();
/*  926 */     DataResultSet resources = new DataResultSet();
/*  927 */     IdcMessage msg = new IdcMessage("csUnableToFindTable", new Object[] { resourceListTableName });
/*      */     try
/*      */     {
/*  930 */       DynamicData dd = merger.getDynamicDataResource(resourceListTableName, null);
/*  931 */       if (dd == null)
/*      */       {
/*  933 */         throw new DataException(null, msg);
/*      */       }
/*  935 */       resources.init(dd.m_mergedTable);
/*      */     }
/*      */     catch (ParseSyntaxException pse)
/*      */     {
/*  939 */       throw new DataException(pse, msg);
/*      */     }
/*  941 */     int iResourceType = resources.getFieldInfoIndex("resourceType");
/*  942 */     if (iResourceType < 0)
/*      */     {
/*  944 */       msg = new IdcMessage("csColumnNotFound", new Object[] { "resourceType", resourceListTableName });
/*  945 */       throw new DataException(null, msg);
/*      */     }
/*  947 */     int iResourceName = resources.getFieldInfoIndex("resourceName");
/*  948 */     if (iResourceName < 0)
/*      */     {
/*  950 */       msg = new IdcMessage("csColumnNotFound", new Object[] { "resourceName", resourceListTableName });
/*  951 */       throw new DataException(null, msg);
/*      */     }
/*  953 */     SharedObjects.putTable(resourceListTableName, resources);
/*      */ 
/*  955 */     DataResultSet tableList = SharedObjects.getTable("DataStoreDesignTableList");
/*  956 */     if (tableList == null)
/*      */     {
/*  958 */       msg = new IdcMessage("csUnableToFindTable", new Object[] { "DataStoreDesignTableList" });
/*  959 */       throw new DataException(null, msg);
/*      */     }
/*  961 */     int iComponentName = tableList.getFieldInfoIndex("dsdComponentName");
/*  962 */     if (iComponentName < 0)
/*      */     {
/*  964 */       msg = new IdcMessage("csColumnNotFound", new Object[] { "dsdComponentName", "DataStoreDesignTableList" });
/*  965 */       throw new DataException(null, msg);
/*      */     }
/*  967 */     int iTableName = tableList.getFieldInfoIndex("dsdTableName");
/*  968 */     if (iTableName < 0)
/*      */     {
/*  970 */       msg = new IdcMessage("csColumnNotFound", new Object[] { "dsdTableName", "DataStoreDesignTableList" });
/*  971 */       throw new DataException(null, msg);
/*      */     }
/*      */ 
/*  974 */     for (tableList.first(); tableList.isRowPresent(); tableList.next())
/*      */     {
/*  976 */       String componentName = tableList.getStringValue(iComponentName);
/*  977 */       String table = tableList.getStringValue(iTableName);
/*  978 */       String tableName = componentName + '.' + table;
/*  979 */       if (SharedObjects.getTable(tableName) == null)
/*      */       {
/*  981 */         msg = new IdcMessage("csUnableToFindTable", new Object[] { tableName });
/*  982 */         throw new DataException(null, msg);
/*      */       }
/*  984 */       Vector row = resources.createEmptyRow();
/*  985 */       row.set(iResourceType, "table");
/*  986 */       row.set(iResourceName, tableName);
/*  987 */       resources.addRow(row);
/*      */ 
/*  989 */       tableName = tableName + ".DefaultValues";
/*  990 */       if (SharedObjects.getTable(tableName) == null)
/*      */         continue;
/*  992 */       row = resources.createEmptyRow();
/*  993 */       row.set(iResourceType, "table");
/*  994 */       row.set(iResourceName, tableName);
/*  995 */       resources.addRow(row);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1003 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97630 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignSQLHandler
 * JD-Core Version:    0.5.4
 */