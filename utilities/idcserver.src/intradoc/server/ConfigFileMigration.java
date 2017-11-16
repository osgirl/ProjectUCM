/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.ConfigFileParameters;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.server.archive.ArchiveUtils;
/*      */ import intradoc.shared.SharedLoader;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Reader;
/*      */ import java.sql.Connection;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.ResultSet;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.sql.Timestamp;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Date;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ConfigFileMigration
/*      */ {
/*      */   protected static String m_intradocDir;
/*      */   protected static String m_systemDir;
/*      */   protected static String m_dataDir;
/*      */   protected static String m_searchDir;
/*      */   protected static String m_archiveDir;
/*      */   protected static String m_systemDataDir;
/*      */   protected static String m_systemLogDir;
/*      */   protected static String m_traceDir;
/*      */   protected static String m_eventDir;
/*   66 */   protected static String m_dirTemp = "_temp";
/*      */   protected static String m_loadConfigFrom;
/*      */   protected static String m_saveConfigTo;
/*      */   protected static Connection m_connection;
/*      */ 
/*      */   public static void migrateSharedConfigFiles(Connection connection, String loadLocation, String storeLocation)
/*      */     throws ServiceException
/*      */   {
/*   78 */     if (connection == null)
/*      */     {
/*   80 */       return;
/*      */     }
/*      */ 
/*   83 */     m_connection = connection;
/*   84 */     m_loadConfigFrom = loadLocation;
/*   85 */     m_saveConfigTo = storeLocation;
/*      */ 
/*   87 */     m_intradocDir = DirectoryLocator.getIntradocDir();
/*   88 */     m_systemDir = DirectoryLocator.getSystemDirectory();
/*   89 */     m_dataDir = DirectoryLocator.getAppDataDirectory();
/*   90 */     m_searchDir = DirectoryLocator.getSearchDirectory();
/*   91 */     m_archiveDir = DirectoryLocator.getDefaultCollection();
/*   92 */     m_systemDataDir = DirectoryLocator.getSystemDataDirectory();
/*   93 */     m_systemLogDir = DirectoryLocator.getSystemLogDirectory();
/*   94 */     m_traceDir = SharedObjects.getEnvironmentValue("TraceDirectory");
/*   95 */     m_eventDir = SharedObjects.getEnvironmentValue("EventDirectory");
/*      */ 
/*   99 */     FileUtilsCfgBuilder.setCfgDescriptorFactory(new ConfigFileDescriptorFactoryBaseImplementor());
/*      */ 
/*  101 */     if ((m_loadConfigFrom.equalsIgnoreCase("Filesystem")) && (m_saveConfigTo.equalsIgnoreCase("Database")))
/*      */     {
/*  104 */       String msg = "Migrating shared config files from file system to database...";
/*  105 */       SystemUtils.outln(msg);
/*      */ 
/*  107 */       transferDirectorytoTable();
/*  108 */       if (SystemUtils.m_verbose)
/*      */       {
/*  110 */         Report.trace("configMigration", "Migration succeed!", null);
/*      */       }
/*      */     }
/*  113 */     else if ((m_loadConfigFrom.equalsIgnoreCase("Database")) && (m_saveConfigTo.equalsIgnoreCase("Filesystem")))
/*      */     {
/*  116 */       String msg = "Migrating shared config files from database to file system...";
/*  117 */       SystemUtils.outln(msg);
/*      */ 
/*  119 */       transferTableToDirectory();
/*  120 */       if (SystemUtils.m_verbose)
/*      */       {
/*  122 */         Report.trace("configMigration", "Migration succeed!", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  127 */     ConfigFileParameters.setLoadFromLocation(m_saveConfigTo);
/*  128 */     updateLoadConfigValue(m_saveConfigTo);
/*  129 */     transferTracingDir();
/*      */ 
/*  132 */     if (m_saveConfigTo.equals("Database"))
/*      */     {
/*  134 */       FileUtilsCfgBuilder.setCfgDescriptorFactory(new ConfigFileDescriptorFactoryDBImplementor());
/*      */     }
/*      */ 
/*  138 */     SharedLoader.configureTracing(null);
/*  139 */     String msg = "Finish shared config files migration.";
/*  140 */     SystemUtils.outln(msg);
/*      */   }
/*      */ 
/*      */   public static void updateLoadConfigValue(String newVal)
/*      */   {
/*  150 */     Statement stmt = null;
/*      */     try
/*      */     {
/*  153 */       stmt = m_connection.createStatement();
/*  154 */       newVal = StringUtils.createQuotableString(newVal);
/*  155 */       String executeSQL = "UPDATE RunTimeConfigOptions SET dRTValue = '" + newVal + "' WHERE dRTName = 'LoadConfig'";
/*      */ 
/*  157 */       stmt.executeUpdate(executeSQL);
/*  158 */       if (SystemUtils.m_verbose)
/*      */       {
/*  160 */         Report.trace("configMigration", "Update LoadConfig value to " + newVal + " in table RunTimeConfigOptions", null);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  166 */       Report.trace("configMigration", "Unable to update parameter LoadConfig value in RunTimeConfigOptions table", e);
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  173 */         stmt.close();
/*      */       }
/*      */       catch (SQLException e)
/*      */       {
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String[] mapFileToRow(String path)
/*      */     throws ServiceException
/*      */   {
/*  190 */     String[] params = new String[6];
/*      */ 
/*  193 */     String dir = FileUtils.getDirectory(path);
/*  194 */     dir = FileUtils.directorySlashes(dir);
/*  195 */     String fileName = FileUtils.getName(path);
/*      */ 
/*  198 */     params[0] = (dir + fileName);
/*  199 */     String feature = ConfigFileParameters.getFeature(dir);
/*  200 */     params[1] = feature;
/*  201 */     String relativeRoot = ConfigFileParameters.getRoot(dir);
/*  202 */     params[2] = relativeRoot;
/*  203 */     params[3] = dir;
/*  204 */     params[4] = fileName;
/*      */ 
/*  206 */     File f = new File(m_intradocDir + path);
/*  207 */     FileInputStream fis = null;
/*      */     try
/*      */     {
/*  211 */       if (!f.exists())
/*      */       {
/*  213 */         throw new ServiceException(-16, LocaleUtils.encodeMessage("csConfigFileMigrationNoResource", null, path));
/*      */       }
/*      */ 
/*  218 */       fis = new FileInputStream(f);
/*  219 */       byte[] textByte = new byte[(int)f.length()];
/*  220 */       fis.read(textByte);
/*      */ 
/*  223 */       String textStr = new String(textByte, 0, textByte.length, FileUtils.m_javaSystemEncoding);
/*  224 */       params[5] = textStr;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  228 */       String msg = LocaleUtils.encodeMessage("csConfigFileMigrationReadFileError", null, path);
/*      */       ServiceException se;
/*  230 */       throw se;
/*      */     }
/*      */     finally
/*      */     {
/*  234 */       FileUtils.closeFiles(null, fis);
/*      */     }
/*  236 */     return params;
/*      */   }
/*      */ 
/*      */   public static void transferFileToRow(String path)
/*      */     throws ServiceException
/*      */   {
/*  247 */     String[] params = mapFileToRow(path);
/*  248 */     PreparedStatement execStmt = null;
/*      */     try
/*      */     {
/*  251 */       execStmt = m_connection.prepareStatement("INSERT INTO RunTimeConfigData (dRTFileID, dRTFeature, dRTRoot, dRTDir, dRTFileName, dRTCreateDate, dRTLastModified, dRTTextObject) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
/*      */ 
/*  254 */       execStmt.setString(1, params[0]);
/*  255 */       execStmt.setString(2, params[1]);
/*  256 */       execStmt.setString(3, params[2]);
/*  257 */       execStmt.setString(4, params[3]);
/*  258 */       execStmt.setString(5, params[4]);
/*  259 */       Date d = new Date();
/*  260 */       Timestamp time = new Timestamp(d.getTime());
/*  261 */       execStmt.setTimestamp(6, time);
/*  262 */       execStmt.setTimestamp(7, time);
/*  263 */       execStmt.setString(8, params[5]);
/*  264 */       execStmt.executeUpdate();
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  268 */       String msg = LocaleUtils.encodeMessage("csConfigFileMigrationTrasferFileError", null, path);
/*      */       ServiceException se;
/*  270 */       throw se;
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  276 */         execStmt.close();
/*  277 */         execStmt = null;
/*      */       }
/*      */       catch (SQLException e)
/*      */       {
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void transferDirectorytoTable()
/*      */     throws ServiceException
/*      */   {
/*  291 */     List paths = new ArrayList();
/*  292 */     boolean isTran = false;
/*      */     try
/*      */     {
/*  296 */       m_connection.setAutoCommit(false);
/*  297 */       isTran = true;
/*      */ 
/*  299 */       cleanSharedFileTable();
/*  300 */       deleteDirectory(new File(m_systemDir), false);
/*      */ 
/*  302 */       getDataDirFiles(m_dataDir, paths);
/*  303 */       int last = 0;
/*  304 */       int total = paths.size();
/*  305 */       int num = total;
/*  306 */       if (SystemUtils.m_verbose)
/*      */       {
/*  308 */         Report.trace("configMigration", "Copy " + num + " files under directory '" + m_intradocDir + "data/' to table RunTimeConfigData", null);
/*      */       }
/*      */ 
/*  312 */       getSearchDirFiles(m_searchDir, paths);
/*  313 */       last = total;
/*  314 */       total = paths.size();
/*  315 */       num = total - last;
/*  316 */       if (SystemUtils.m_verbose)
/*      */       {
/*  318 */         Report.trace("configMigration", "Copy " + num + " files under directory '" + m_intradocDir + "search/' to table RunTimeConfigData", null);
/*      */       }
/*      */ 
/*  322 */       getArchiveDirFiles(m_archiveDir, paths);
/*  323 */       last = total;
/*  324 */       total = paths.size();
/*  325 */       num = total - last;
/*  326 */       if (SystemUtils.m_verbose)
/*      */       {
/*  328 */         Report.trace("configMigration", "Copy " + num + " files under directory '" + m_intradocDir + "archives/' to table RunTimeConfigData", null);
/*      */       }
/*      */ 
/*  332 */       File cmuFile = new File(m_intradocDir + "cmu/");
/*  333 */       if (cmuFile.exists())
/*      */       {
/*  335 */         getCMUDirFiles(m_intradocDir + "cmu/", paths);
/*  336 */         last = total;
/*  337 */         total = paths.size();
/*  338 */         num = total - last;
/*  339 */         if (SystemUtils.m_verbose)
/*      */         {
/*  341 */           Report.trace("configMigration", "Copy " + num + " files under directory '" + m_intradocDir + "cmu/' to table RunTimeConfigData", null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  346 */       for (String file : paths)
/*      */       {
/*  349 */         file = file.replace(m_intradocDir, "");
/*  350 */         transferFileToRow(file);
/*      */       }
/*      */ 
/*  353 */       isTran = false;
/*  354 */       m_connection.commit();
/*  355 */       m_connection.setAutoCommit(true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */       try
/*      */       {
/*  361 */         if (isTran)
/*      */         {
/*  363 */           m_connection.rollback();
/*  364 */           m_connection.setAutoCommit(true);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (SQLException ex)
/*      */       {
/*      */       }
/*      */ 
/*  372 */       String msg = LocaleUtils.encodeMessage("csConfigFileMigrationTrasferDirError", null);
/*  373 */       ServiceException se = new ServiceException(null, msg, new Object[] { e });
/*  374 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void mapRowToFile(ResultSet rset, Vector v)
/*      */     throws ServiceException
/*      */   {
/*  386 */     String path = null;
/*  387 */     Date date = null;
/*  388 */     Reader reader = null;
/*      */     try
/*      */     {
/*  392 */       String fileID = rset.getString(1);
/*  393 */       String root = rset.getString(2);
/*  394 */       date = rset.getDate(3);
/*  395 */       reader = rset.getCharacterStream(4);
/*      */ 
/*  398 */       fileID = fileID.replace(root + "/", root + m_dirTemp + "/");
/*  399 */       path = m_intradocDir + fileID;
/*      */ 
/*  402 */       v.addElement(path);
/*  403 */       v.addElement(date);
/*  404 */       v.addElement(reader);
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*  408 */       String msg = LocaleUtils.encodeMessage("csConfigFileMigrationReadRowError", null, path);
/*  409 */       ServiceException se = new ServiceException(-18, msg, e);
/*  410 */       throw se;
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void transferRowToFile(ResultSet rset)
/*      */     throws ServiceException
/*      */   {
/*  421 */     Reader reader = null;
/*  422 */     BufferedReader bufReader = null;
/*  423 */     BufferedWriter bufWriter = null;
/*  424 */     OutputStream out = null;
/*  425 */     String path = null;
/*  426 */     String dir = null;
/*  427 */     Date date = null;
/*  428 */     Vector v = new IdcVector();
/*      */ 
/*  430 */     mapRowToFile(rset, v);
/*      */     try
/*      */     {
/*  434 */       path = (String)v.elementAt(0);
/*  435 */       date = (Date)v.elementAt(1);
/*  436 */       reader = (Reader)v.elementAt(2);
/*      */ 
/*  438 */       File f = new File(path);
/*      */ 
/*  440 */       dir = FileUtils.directorySlashes(f.getParent());
/*  441 */       FileUtils.checkOrCreateDirectory(dir, 5, 1);
/*  442 */       FileUtils.reserveDirectory(dir);
/*      */ 
/*  445 */       String name = f.getName();
/*  446 */       if ((name.equals("lockwait.dat")) || (name.matches("lockon[1-9]*.*")))
/*      */       {
/*      */         return;
/*      */       }
/*      */ 
/*  452 */       IdcStringBuilder contents = new IdcStringBuilder();
/*  453 */       if (reader != null)
/*      */       {
/*  455 */         bufReader = new BufferedReader(reader);
/*  456 */         String line = null;
/*  457 */         while ((line = bufReader.readLine()) != null)
/*      */         {
/*  459 */           contents.append(line);
/*  460 */           contents.append(System.getProperty("line.separator"));
/*      */         }
/*  462 */         out = FileUtilsCfgBuilder.getCfgOutputStream(f);
/*  463 */         bufWriter = FileUtils.openDataWriterEx(out, null, 1);
/*  464 */         bufWriter.write(contents.toString());
/*      */       }
/*      */ 
/*  467 */       FileUtils.setLastModified(path, date.getTime());
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */       String msg;
/*      */       ServiceException se;
/*  473 */       throw se;
/*      */     }
/*      */     finally
/*      */     {
/*  477 */       FileUtils.closeReader(reader);
/*  478 */       FileUtils.closeReader(bufReader);
/*  479 */       FileUtils.closeObjects(bufWriter, out);
/*  480 */       FileUtils.releaseDirectory(dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void transferSystemDir()
/*      */     throws ServiceException
/*      */   {
/*  491 */     String tempDataDir = m_dataDir.substring(0, m_dataDir.length() - 1) + m_dirTemp + "/";
/*  492 */     File systemData = new File(m_systemDataDir);
/*  493 */     if (systemData.exists())
/*      */     {
/*  495 */       File tempData = new File(tempDataDir);
/*  496 */       FileUtils.copyDirectoryWithFlags(systemData, tempData, 1, null, 2);
/*  497 */       if (SystemUtils.m_verbose)
/*      */       {
/*  499 */         Report.trace("configMigration", "Copy directory '" + m_systemDataDir + "' to '" + tempDataDir + "'", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  505 */     File systemLog = new File(m_systemLogDir);
/*  506 */     if (systemLog.exists())
/*      */     {
/*  508 */       File tempData = new File(tempDataDir);
/*  509 */       FileUtils.copyDirectoryWithFlags(systemLog, tempData, 1, null, 2);
/*  510 */       if (SystemUtils.m_verbose)
/*      */       {
/*  512 */         Report.trace("configMigration", "Copy directory '" + m_systemLogDir + "' to '" + tempDataDir + "'", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  518 */     String systemSearchDir = m_searchDir.replace(m_intradocDir, m_systemDir);
/*  519 */     String tempSearchDir = m_searchDir.substring(0, m_searchDir.length() - 1) + m_dirTemp + "/";
/*  520 */     File systemSearch = new File(systemSearchDir);
/*  521 */     if (systemSearch.exists())
/*      */     {
/*  523 */       File tempSearch = new File(tempSearchDir);
/*  524 */       FileUtils.copyDirectoryWithFlags(systemSearch, tempSearch, 1, null, 2);
/*  525 */       if (SystemUtils.m_verbose)
/*      */       {
/*  527 */         Report.trace("configMigration", "Copy directory '" + systemSearchDir + "' to '" + tempSearchDir + "'", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  533 */     String systemArchiveDir = m_archiveDir.replace(m_intradocDir, m_systemDir);
/*  534 */     String tempArchiveDir = m_archiveDir.substring(0, m_archiveDir.length() - 1) + m_dirTemp + "/";
/*  535 */     File systemArchive = new File(systemArchiveDir);
/*  536 */     if (systemArchive.exists())
/*      */     {
/*  538 */       File tempArchive = new File(tempArchiveDir);
/*  539 */       FileUtils.copyDirectoryWithFlags(systemArchive, tempArchive, 3, null, 2);
/*  540 */       if (SystemUtils.m_verbose)
/*      */       {
/*  542 */         Report.trace("configMigration", "Copy directory '" + systemArchiveDir + "' to '" + tempArchiveDir + "'", null);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  548 */     File systemCmu = new File(m_systemDir + "cmu");
/*  549 */     if (!systemCmu.exists())
/*      */       return;
/*  551 */     File tempCmu = new File(m_intradocDir + "cmu" + m_dirTemp);
/*  552 */     FileUtils.copyDirectoryWithFlags(systemCmu, tempCmu, 1, null, 2);
/*  553 */     if (!SystemUtils.m_verbose)
/*      */       return;
/*  555 */     Report.trace("configMigration", "Copy directory '" + m_systemDir + "cmu' to '" + m_intradocDir + "cmu" + m_dirTemp + "'", null);
/*      */   }
/*      */ 
/*      */   public static void transferTableToDirectory()
/*      */     throws ServiceException
/*      */   {
/*  566 */     Statement stmt = null;
/*      */     try
/*      */     {
/*  569 */       Vector sharedDirectories = ConfigFileParameters.getSharedDirectories();
/*  570 */       Iterator i = sharedDirectories.iterator();
/*  571 */       stmt = m_connection.createStatement(1004, 1007);
/*      */ 
/*  575 */       if (SystemUtils.m_verbose)
/*      */       {
/*  577 */         Report.trace("configMigration", "Start moving config files from table RunTimeConfigData to file system.", null);
/*      */       }
/*      */ 
/*  580 */       while (i.hasNext())
/*      */       {
/*  582 */         String dir = (String)i.next();
/*  583 */         dir = dir.substring(0, dir.length() - 1);
/*      */ 
/*  586 */         deleteDirectory(new File(dir + m_dirTemp), false);
/*  587 */         dir = dir.replace(m_intradocDir, "");
/*  588 */         dir = StringUtils.createQuotableString(dir);
/*  589 */         String querySQL = "SELECT dRTFileID, dRTRoot, dRTLastModified, dRTTextObject FROM RunTimeConfigData Where dRTDir LIKE '" + dir + "/%' ORDER BY dRTFileID";
/*      */ 
/*  591 */         ResultSet rset = stmt.executeQuery(querySQL);
/*      */ 
/*  593 */         while (rset.next())
/*      */         {
/*  595 */           transferRowToFile(rset);
/*      */         }
/*  597 */         rset.last();
/*  598 */         int num = rset.getRow();
/*  599 */         if ((num > 0) && (SystemUtils.m_verbose))
/*      */         {
/*  601 */           Report.trace("configMigration", "Migrate " + num + " files in '" + dir + "/' directory from table RunTimeConfigData to file system", null);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  607 */       transferSystemDir();
/*      */ 
/*  610 */       i = sharedDirectories.iterator();
/*  611 */       while (i.hasNext())
/*      */       {
/*  613 */         String dir = (String)i.next();
/*  614 */         dir = dir.substring(0, dir.length() - 1);
/*  615 */         File orgDir = new File(dir);
/*  616 */         deleteDirectory(orgDir, true);
/*  617 */         File newDir = new File(dir + m_dirTemp);
/*  618 */         if (newDir.exists())
/*      */         {
/*  620 */           newDir.renameTo(orgDir);
/*  621 */           if (SystemUtils.m_verbose)
/*      */           {
/*  623 */             Report.trace("configMigration", "Rename directory '" + dir + m_dirTemp + "' to '" + dir + "'", null);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  631 */       String msg = LocaleUtils.encodeMessage("csConfigFileMigrationTransferTableError", null);
/*      */       ServiceException se;
/*  633 */       throw se;
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  639 */         stmt.close();
/*  640 */         stmt = null;
/*      */       }
/*      */       catch (SQLException e)
/*      */       {
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void cleanSharedFileTable()
/*      */     throws SQLException
/*      */   {
/*  654 */     PreparedStatement stmt = m_connection.prepareStatement("DELETE FROM RunTimeConfigData");
/*  655 */     stmt.executeUpdate();
/*  656 */     stmt.close();
/*  657 */     stmt = null;
/*  658 */     if (!SystemUtils.m_verbose)
/*      */       return;
/*  660 */     Report.trace("configMigration", "Clean all file records in table RunTimeConfigData", null);
/*      */   }
/*      */ 
/*      */   public static void cleanSharedFileTable(Connection conn, String dir)
/*      */     throws SQLException
/*      */   {
/*  669 */     dir = StringUtils.createQuotableString(dir);
/*  670 */     PreparedStatement stmt = conn.prepareStatement("DELETE FROM RunTimeConfigData WHERE dRTDir LIKE '" + dir + "/%'");
/*      */ 
/*  672 */     stmt.executeUpdate();
/*  673 */     stmt.close();
/*  674 */     stmt = null;
/*  675 */     if (!SystemUtils.m_verbose)
/*      */       return;
/*  677 */     Report.trace("configMigration", "Clean file records under directory '" + dir + "' in table RunTimeConfigData", null);
/*      */   }
/*      */ 
/*      */   public static void cleanSharedDirectory(File dir)
/*      */   {
/*  687 */     String[] files = dir.list();
/*  688 */     if (files == null)
/*      */     {
/*  690 */       return;
/*      */     }
/*      */ 
/*  693 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/*  695 */       String path = FileUtils.directorySlashes(dir.getAbsolutePath());
/*  696 */       path = path + files[i];
/*  697 */       File dFile = new File(path);
/*  698 */       if (dFile.isDirectory() == true)
/*      */       {
/*  700 */         cleanSharedDirectory(dFile);
/*      */       }
/*      */       else
/*      */       {
/*  704 */         dFile.delete();
/*      */       }
/*      */     }
/*  707 */     dir.delete();
/*      */   }
/*      */ 
/*      */   public static void getSubDirAndFiles(String prefix, String path, List<String> dirs, List<String> paths)
/*      */     throws IOException
/*      */   {
/*  721 */     File f = new File(prefix + path);
/*      */ 
/*  723 */     if (f.isDirectory())
/*      */     {
/*  726 */       String[] entries = f.list();
/*  727 */       Arrays.sort(entries);
/*  728 */       int numEntries = entries.length;
/*  729 */       for (int e = 0; e < numEntries; ++e)
/*      */       {
/*  731 */         String entry = path + '/' + entries[e];
/*  732 */         getSubDirAndFiles(prefix, entry, dirs, paths);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  738 */       paths.add(path);
/*      */ 
/*  741 */       String dir = FileUtils.getParent(path);
/*  742 */       dir = FileUtils.directorySlashes(dir);
/*  743 */       if (dirs.contains(dir))
/*      */         return;
/*  745 */       dirs.add(dir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void getDataDirFiles(String path, List<String> paths)
/*      */     throws ServiceException
/*      */   {
/*  756 */     File f = new File(path);
/*      */ 
/*  758 */     if (f.isDirectory())
/*      */     {
/*  760 */       path = FileUtils.directorySlashes(path);
/*  761 */       if (path.startsWith(m_traceDir)) return; if (path.startsWith(m_eventDir)) {
/*      */         return;
/*      */       }
/*      */ 
/*  765 */       if ((path.contains("log/")) || (path.contains("trace/")))
/*      */       {
/*  768 */         String newPath = path.replace(m_dataDir, m_systemLogDir);
/*  769 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/*  770 */         if (SystemUtils.m_verbose)
/*      */         {
/*  772 */           Report.trace("configMigration", "Copy directory '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */       }
/*  775 */       else if (path.contains("users/"))
/*      */       {
/*  778 */         String newPath = path.replace(m_dataDir, m_systemDataDir);
/*  779 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/*  780 */         if (SystemUtils.m_verbose)
/*      */         {
/*  782 */           Report.trace("configMigration", "Copy directory '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */       }
/*  785 */       else if (path.contains("sescrawlerexport/datafeeds/"))
/*      */       {
/*  789 */         String newPath = path.replace(m_dataDir, m_systemDataDir);
/*  790 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/*  791 */         if (SystemUtils.m_verbose)
/*      */         {
/*  793 */           Report.trace("configMigration", "Copy directory '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  799 */         String[] entries = f.list();
/*  800 */         Arrays.sort(entries);
/*  801 */         int numEntries = entries.length;
/*  802 */         for (int e = 0; e < numEntries; ++e)
/*      */         {
/*  804 */           String entry = path + "/" + entries[e];
/*  805 */           getDataDirFiles(entry, paths);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  811 */       path = FileUtils.fileSlashes(path);
/*  812 */       String name = FileUtils.getName(path);
/*  813 */       if (path.endsWith("_sql_scripts.zip"))
/*      */       {
/*  817 */         String newPath = path.replace(m_dataDir, m_systemDataDir);
/*  818 */         FileUtils.checkOrCreateDirectory(newPath.substring(0, newPath.lastIndexOf("/")), 3);
/*  819 */         FileUtils.copyFile(path, newPath);
/*  820 */         if (SystemUtils.m_verbose)
/*      */         {
/*  822 */           Report.trace("configMigration", "Copy zip file '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */       }
/*  825 */       else if ((name.equalsIgnoreCase("keystore.jks")) || (name.equalsIgnoreCase("truststore.jks")))
/*      */       {
/*  829 */         String newPath = path.replace(m_dataDir, m_systemDataDir);
/*  830 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/*  831 */         if (SystemUtils.m_verbose)
/*      */         {
/*  833 */           Report.trace("configMigration", "Copy key store file '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  839 */         paths.add(path);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void getSearchDirFiles(String path, List<String> paths)
/*      */     throws ServiceException
/*      */   {
/*  850 */     File f = new File(path);
/*      */ 
/*  852 */     if (f.isDirectory())
/*      */     {
/*  854 */       path = FileUtils.directorySlashes(path);
/*  855 */       if (path.contains("bulkload/"))
/*      */       {
/*  858 */         String newPath = path.replace(m_intradocDir, m_systemDir);
/*  859 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/*  860 */         if (SystemUtils.m_verbose)
/*      */         {
/*  862 */           Report.trace("configMigration", "Copy directory '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  868 */         String[] entries = f.list();
/*  869 */         Arrays.sort(entries);
/*  870 */         int numEntries = entries.length;
/*  871 */         for (int e = 0; e < numEntries; ++e)
/*      */         {
/*  873 */           String entry = path + '/' + entries[e];
/*  874 */           getSearchDirFiles(entry, paths);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  881 */       path = FileUtils.fileSlashes(path);
/*  882 */       paths.add(path);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void getCMUDirFiles(String path, List<String> paths)
/*      */     throws ServiceException
/*      */   {
/*  892 */     File f = new File(path);
/*      */ 
/*  894 */     if (f.isDirectory())
/*      */     {
/*  897 */       path = FileUtils.directorySlashes(path);
/*  898 */       String[] entries = f.list();
/*  899 */       Arrays.sort(entries);
/*  900 */       int numEntries = entries.length;
/*  901 */       for (int e = 0; e < numEntries; ++e)
/*      */       {
/*  903 */         String entry = path + '/' + entries[e];
/*  904 */         getCMUDirFiles(entry, paths);
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  909 */       path = FileUtils.fileSlashes(path);
/*  910 */       if (path.endsWith(".zip"))
/*      */       {
/*  913 */         String newPath = path.replace(m_intradocDir, m_systemDir);
/*  914 */         FileUtils.checkOrCreateDirectory(newPath.substring(0, newPath.lastIndexOf("/")), 5);
/*  915 */         FileUtils.copyFile(path, newPath);
/*  916 */         if (SystemUtils.m_verbose)
/*      */         {
/*  918 */           Report.trace("configMigration", "Copy zip file '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  924 */         paths.add(path);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void getArchiveDirFiles(String path, List paths)
/*      */     throws ServiceException
/*      */   {
/*  934 */     Vector exportList = new IdcVector();
/*      */ 
/*  937 */     DataBinder data = ArchiveUtils.readCollectionData(path, false);
/*  938 */     if (data != null)
/*      */     {
/*  941 */       DataResultSet archiveList = (DataResultSet)data.getResultSet("Archives");
/*  942 */       if (archiveList != null)
/*      */       {
/*  944 */         for (archiveList.first(); archiveList.isRowPresent(); archiveList.next())
/*      */         {
/*  947 */           String archiveName = archiveList.getStringValueByName(ArchiveUtils.ARCHIVE_COLUMNS[0]);
/*  948 */           archiveName = archiveName.toLowerCase();
/*  949 */           String archivePath = path + archiveName;
/*  950 */           DataBinder archiveData = ArchiveUtils.readExportsFile(archivePath, null);
/*  951 */           if (archiveData == null) {
/*      */             continue;
/*      */           }
/*  954 */           DataResultSet rset = (DataResultSet)archiveData.getResultSet("BatchFiles");
/*  955 */           if (rset == null) {
/*      */             break;
/*      */           }
/*      */ 
/*  959 */           for (rset.first(); rset.isRowPresent(); rset.next())
/*      */           {
/*  961 */             String fileName = rset.getStringValueByName("aBatchFile");
/*  962 */             int index = fileName.lastIndexOf(47);
/*  963 */             String batchDir = null;
/*  964 */             if (index > 0)
/*      */             {
/*  966 */               batchDir = fileName.substring(0, index + 1);
/*      */             }
/*  968 */             if (batchDir == null) {
/*      */               continue;
/*      */             }
/*  971 */             String oldPath = archivePath + "/" + batchDir;
/*  972 */             exportList.add(oldPath);
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  980 */     getArchiveDirFilesEx(path, paths, exportList);
/*      */   }
/*      */ 
/*      */   public static void getArchiveDirFilesEx(String path, List<String> paths, Vector exportList)
/*      */     throws ServiceException
/*      */   {
/*  990 */     File f = new File(path);
/*      */ 
/*  992 */     if (f.isDirectory())
/*      */     {
/*  994 */       path = FileUtils.directorySlashes(path);
/*  995 */       if (exportList.contains(path))
/*      */       {
/*  998 */         String newPath = path.replace(m_intradocDir, m_systemDir);
/*  999 */         FileUtils.copyDirectoryWithFlags(f, new File(newPath), 4, null, 1);
/* 1000 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1002 */           Report.trace("configMigration", "Copy directory '" + path + "' to '" + newPath + "'", null);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1008 */         String[] entries = f.list();
/* 1009 */         Arrays.sort(entries);
/* 1010 */         int numEntries = entries.length;
/* 1011 */         for (int e = 0; e < numEntries; ++e)
/*      */         {
/* 1013 */           String entry = path + '/' + entries[e];
/* 1014 */           getArchiveDirFilesEx(entry, paths, exportList);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1021 */       path = FileUtils.fileSlashes(path);
/* 1022 */       paths.add(path);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void transferTracingDir() throws ServiceException
/*      */   {
/* 1028 */     if ((m_loadConfigFrom.equalsIgnoreCase("Filesystem")) && (m_saveConfigTo.equalsIgnoreCase("Database")))
/*      */     {
/* 1030 */       Iterator i = ConfigFileParameters.getSharedDirectories().iterator();
/* 1031 */       while (i.hasNext())
/*      */       {
/* 1033 */         String sharedDir = (String)i.next();
/* 1034 */         if (m_traceDir.startsWith(sharedDir))
/*      */         {
/* 1036 */           String newTraceDir = m_traceDir.replace(sharedDir, m_systemLogDir);
/* 1037 */           FileUtils.copyDirectoryWithFlags(new File(m_traceDir), new File(newTraceDir), 4, null, 1);
/*      */ 
/* 1039 */           SharedObjects.putEnvironmentValue("TraceDirectory", newTraceDir);
/*      */         }
/* 1041 */         if (m_eventDir.startsWith(sharedDir))
/*      */         {
/* 1043 */           String newEventDir = m_eventDir.replace(sharedDir, m_systemLogDir);
/* 1044 */           if (!m_eventDir.startsWith(m_traceDir))
/*      */           {
/* 1046 */             FileUtils.copyDirectoryWithFlags(new File(m_eventDir), new File(newEventDir), 4, null, 1);
/*      */           }
/*      */ 
/* 1049 */           SharedObjects.putEnvironmentValue("EventDirectory", newEventDir);
/*      */         }
/*      */       }
/*      */     } else {
/* 1053 */       if ((!m_loadConfigFrom.equalsIgnoreCase("Database")) || (!m_saveConfigTo.equalsIgnoreCase("Filesystem")))
/*      */         return;
/* 1055 */       if (m_traceDir.startsWith(m_systemLogDir))
/*      */       {
/* 1057 */         String newTraceDir = m_traceDir.replace(m_systemLogDir, m_dataDir);
/* 1058 */         FileUtils.copyDirectoryWithFlags(new File(m_traceDir), new File(newTraceDir), 4, null, 1);
/*      */ 
/* 1060 */         SharedObjects.putEnvironmentValue("TraceDirectory", newTraceDir);
/*      */       }
/* 1062 */       if (!m_eventDir.startsWith(m_systemLogDir))
/*      */         return;
/* 1064 */       String newEventDir = m_eventDir.replace(m_systemLogDir, m_dataDir);
/* 1065 */       if (!m_eventDir.startsWith(m_traceDir))
/*      */       {
/* 1067 */         FileUtils.copyDirectoryWithFlags(new File(m_eventDir), new File(newEventDir), 4, null, 1);
/*      */       }
/*      */ 
/* 1070 */       SharedObjects.putEnvironmentValue("EventDirectory", newEventDir);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void deleteDirectory(File dir, boolean deleteSelf)
/*      */     throws ServiceException
/*      */   {
/* 1077 */     String[] files = dir.list();
/* 1078 */     if (files == null)
/*      */     {
/* 1080 */       return;
/*      */     }
/*      */ 
/* 1083 */     for (int i = 0; i < files.length; ++i)
/*      */     {
/* 1085 */       String path = FileUtils.directorySlashes(dir.getAbsolutePath());
/* 1086 */       path = path + files[i];
/* 1087 */       File dFile = new File(path);
/* 1088 */       if (dFile.isDirectory() == true)
/*      */       {
/* 1090 */         deleteDirectory(dFile, true);
/*      */       }
/*      */       else
/*      */       {
/* 1094 */         dFile.delete();
/*      */       }
/*      */     }
/* 1097 */     if (deleteSelf != true)
/*      */       return;
/* 1099 */     dir.delete();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1105 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103654 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ConfigFileMigration
 * JD-Core Version:    0.5.4
 */