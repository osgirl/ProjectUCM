/*      */ package intradoc.data;
/*      */ 
/*      */ import intradoc.common.ConfigFileParameters;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FilenameFilter;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.Reader;
/*      */ import java.sql.Connection;
/*      */ import java.sql.PreparedStatement;
/*      */ import java.sql.SQLException;
/*      */ import java.sql.Statement;
/*      */ import java.sql.Timestamp;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IdcConfigFile extends File
/*      */ {
/*      */   public static Connection m_connection;
/*      */   public static String m_intradocDir;
/*   57 */   public Workspace m_workspace = null;
/*   58 */   public String m_prefix = null;
/*      */ 
/*   60 */   public boolean m_isUpdate = true;
/*   61 */   public long m_length = 0L;
/*      */ 
/*   63 */   protected Boolean m_exists = null;
/*   64 */   protected Boolean m_isFile = null;
/*   65 */   protected Boolean m_isDir = null;
/*   66 */   protected String[] m_list = null;
/*      */   public String m_fileID;
/*      */   protected String m_feature;
/*      */   protected String m_fileName;
/*      */   protected String m_relativeDir;
/*      */   protected String m_relativeRoot;
/*   73 */   public long m_lastModified = -1L;
/*      */ 
/*      */   public IdcConfigFile(String path)
/*      */   {
/*   77 */     super(path);
/*      */   }
/*      */ 
/*      */   public IdcConfigFile(String path, String feature, Workspace workspace)
/*      */   {
/*   82 */     super(path);
/*      */ 
/*   84 */     this.m_workspace = workspace;
/*   85 */     this.m_prefix = m_intradocDir;
/*      */ 
/*   87 */     if (path.endsWith("/"))
/*      */     {
/*   90 */       path = FileUtils.directorySlashes(path);
/*   91 */       this.m_fileName = "";
/*   92 */       this.m_relativeDir = path.replace(this.m_prefix, "");
/*   93 */       this.m_isDir = Boolean.valueOf(true);
/*      */     }
/*      */     else
/*      */     {
/*   97 */       path = FileUtils.fileSlashes(path);
/*   98 */       int index = path.lastIndexOf("/");
/*   99 */       this.m_fileName = path.substring(index + 1);
/*  100 */       String dir = path.substring(0, index + 1);
/*  101 */       this.m_relativeDir = dir.replace(this.m_prefix, "");
/*      */     }
/*  103 */     this.m_feature = feature;
/*  104 */     this.m_relativeRoot = ConfigFileParameters.getRoot(this.m_relativeDir);
/*  105 */     this.m_fileID = (this.m_relativeDir + this.m_fileName);
/*      */   }
/*      */ 
/*      */   public IdcConfigFile(String path, String feature, boolean isDir, Workspace workspace)
/*      */   {
/*  110 */     super(path);
/*      */ 
/*  112 */     this.m_workspace = workspace;
/*  113 */     this.m_prefix = m_intradocDir;
/*      */ 
/*  115 */     if (isDir)
/*      */     {
/*  118 */       path = FileUtils.directorySlashes(path);
/*  119 */       this.m_fileName = "";
/*  120 */       this.m_relativeDir = path.replace(this.m_prefix, "");
/*  121 */       this.m_isDir = Boolean.valueOf(true);
/*      */     }
/*      */     else
/*      */     {
/*  125 */       path = FileUtils.fileSlashes(path);
/*  126 */       int index = path.lastIndexOf("/");
/*  127 */       this.m_fileName = path.substring(index + 1);
/*  128 */       String dir = path.substring(0, index + 1);
/*  129 */       this.m_relativeDir = dir.replace(this.m_prefix, "");
/*      */     }
/*      */ 
/*  133 */     if ((feature == null) || (feature.length() == 0))
/*      */     {
/*  135 */       feature = ConfigFileParameters.getFeature(this.m_relativeDir);
/*      */     }
/*  137 */     this.m_feature = feature;
/*  138 */     this.m_relativeRoot = ConfigFileParameters.getRoot(this.m_relativeDir);
/*  139 */     this.m_fileID = (this.m_relativeDir + this.m_fileName);
/*      */   }
/*      */ 
/*      */   public static void setIntradocDir(String intradocDir)
/*      */   {
/*  144 */     m_intradocDir = intradocDir;
/*      */   }
/*      */ 
/*      */   public static void setRawDBConnection(Connection conn)
/*      */   {
/*  154 */     m_connection = conn;
/*      */   }
/*      */ 
/*      */   public static void releaseRawDBConnection()
/*      */   {
/*  162 */     if (m_connection == null)
/*      */       return;
/*      */     try
/*      */     {
/*  166 */       m_connection.close();
/*  167 */       m_connection = null;
/*      */     }
/*      */     catch (Throwable ignore)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean canRead()
/*      */   {
/*  180 */     if (this.m_exists == null)
/*      */     {
/*  182 */       getValues();
/*      */     }
/*  184 */     if (this.m_exists == null)
/*      */     {
/*  186 */       return false;
/*      */     }
/*  188 */     return this.m_exists.booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean canWrite()
/*      */   {
/*  195 */     if (this.m_exists == null)
/*      */     {
/*  197 */       getValues();
/*      */     }
/*  199 */     if (this.m_exists == null)
/*      */     {
/*  201 */       return false;
/*      */     }
/*  203 */     return this.m_exists.booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean delete()
/*      */   {
/*  209 */     if (!isFile())
/*      */     {
/*  211 */       return false;
/*      */     }
/*      */ 
/*  214 */     boolean isTran = false;
/*      */     try
/*      */     {
/*  218 */       if (this.m_workspace == null)
/*      */       {
/*  220 */         boolean bool1 = deleteStmt();
/*      */         return bool1;
/*      */       }
/*  222 */       DataBinder binder = new DataBinder();
/*  223 */       binder.putLocal("dRTFileID", this.m_fileID);
/*  224 */       isTran = beginTran();
/*  225 */       this.m_workspace.execute("DRunTimeConfigData", binder);
/*  226 */       if (isTran)
/*      */       {
/*  228 */         isTran = false;
/*  229 */         commitTran();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  234 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileDeleteError", e.getMessage(), this.m_fileID);
/*  235 */       Report.error(null, msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*  239 */       if (isTran)
/*      */       {
/*  241 */         isTran = false;
/*  242 */         rollbackTran();
/*      */       }
/*      */     }
/*      */ 
/*  246 */     this.m_exists = Boolean.valueOf(false);
/*  247 */     this.m_isFile = Boolean.valueOf(false);
/*  248 */     this.m_isDir = Boolean.valueOf(false);
/*  249 */     this.m_length = 0L;
/*  250 */     this.m_list = null;
/*  251 */     this.m_lastModified = -1L;
/*      */ 
/*  253 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean equals(Object obj)
/*      */   {
/*  260 */     String path1 = FileUtils.fileSlashes(getAbsolutePath());
/*  261 */     String path2 = FileUtils.fileSlashes(((File)obj).getAbsolutePath());
/*      */ 
/*  264 */     return path1.equals(path2);
/*      */   }
/*      */ 
/*      */   public boolean exists()
/*      */   {
/*  272 */     if (this.m_exists == null)
/*      */     {
/*  274 */       getValues();
/*      */     }
/*  276 */     if (this.m_exists == null)
/*      */     {
/*  278 */       return false;
/*      */     }
/*  280 */     return this.m_exists.booleanValue();
/*      */   }
/*      */ 
/*      */   public String getAbsolutePath()
/*      */   {
/*  286 */     return this.m_prefix + this.m_fileID;
/*      */   }
/*      */ 
/*      */   public String getFeature()
/*      */   {
/*  294 */     return this.m_feature;
/*      */   }
/*      */ 
/*      */   public String getName()
/*      */   {
/*  300 */     String path = FileUtils.fileSlashes(this.m_fileID);
/*  301 */     int index = path.lastIndexOf("/");
/*  302 */     if (index < 0)
/*      */     {
/*  304 */       return path;
/*      */     }
/*      */ 
/*  307 */     return path.substring(index + 1);
/*      */   }
/*      */ 
/*      */   public String getParent()
/*      */   {
/*  313 */     String path = FileUtils.fileSlashes(this.m_fileID);
/*  314 */     int index = path.lastIndexOf("/");
/*  315 */     if (index < 0)
/*      */     {
/*  317 */       return null;
/*      */     }
/*      */ 
/*  320 */     return this.m_prefix + path.substring(0, index);
/*      */   }
/*      */ 
/*      */   public String getPath()
/*      */   {
/*  326 */     return this.m_prefix + this.m_fileID;
/*      */   }
/*      */ 
/*      */   public boolean isAbsolute()
/*      */   {
/*  332 */     return super.isAbsolute();
/*      */   }
/*      */ 
/*      */   public boolean isDirectory()
/*      */   {
/*  338 */     if (this.m_isDir == null)
/*      */     {
/*  340 */       getValues();
/*      */     }
/*  342 */     if (this.m_isDir == null)
/*      */     {
/*  344 */       return false;
/*      */     }
/*  346 */     return this.m_isDir.booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean isFile()
/*      */   {
/*  352 */     if (this.m_isFile == null)
/*      */     {
/*  354 */       getValues();
/*      */     }
/*  356 */     if (this.m_isFile == null)
/*      */     {
/*  358 */       return false;
/*      */     }
/*  360 */     return this.m_isFile.booleanValue();
/*      */   }
/*      */ 
/*      */   public long lastModified()
/*      */   {
/*  367 */     if (this.m_lastModified == -1L)
/*      */     {
/*  369 */       getValues();
/*      */     }
/*  371 */     if (this.m_lastModified == -1L)
/*      */     {
/*  373 */       return 0L;
/*      */     }
/*  375 */     return this.m_lastModified;
/*      */   }
/*      */ 
/*      */   public long length()
/*      */   {
/*  383 */     if (!this.m_isUpdate)
/*      */     {
/*  385 */       return this.m_length;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  392 */       byte[] buf = readToByteArray();
/*  393 */       if (buf != null)
/*      */       {
/*  395 */         this.m_length = buf.length;
/*      */       }
/*      */       else
/*      */       {
/*  399 */         this.m_length = 0L;
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  404 */       this.m_length = 0L;
/*      */     }
/*  406 */     this.m_isUpdate = false;
/*  407 */     return this.m_length;
/*      */   }
/*      */ 
/*      */   public String[] list()
/*      */   {
/*  416 */     if (this.m_list == null)
/*      */     {
/*  418 */       getValues();
/*      */     }
/*  420 */     if (this.m_list == null)
/*      */     {
/*  422 */       return new String[0];
/*      */     }
/*  424 */     return this.m_list;
/*      */   }
/*      */ 
/*      */   public String[] list(FilenameFilter filter)
/*      */   {
/*  430 */     if (this.m_list == null)
/*      */     {
/*  432 */       getValues();
/*      */     }
/*      */ 
/*  435 */     String[] results = new String[0];
/*  436 */     if (this.m_list != null)
/*      */     {
/*  438 */       Vector contents = new IdcVector();
/*  439 */       for (String item : this.m_list)
/*      */       {
/*  441 */         if (!filter.accept(this, item))
/*      */           continue;
/*  443 */         contents.add(item);
/*      */       }
/*      */ 
/*  446 */       results = new String[contents.size()];
/*  447 */       contents.toArray(results);
/*      */     }
/*      */ 
/*  450 */     return results;
/*      */   }
/*      */ 
/*      */   public File[] listFiles()
/*      */   {
/*  459 */     if (this.m_list == null)
/*      */     {
/*  461 */       getValues();
/*      */     }
/*      */ 
/*  464 */     IdcConfigFile[] files = new IdcConfigFile[0];
/*  465 */     if ((this.m_list != null) && (this.m_list.length > 0))
/*      */     {
/*  467 */       files = new IdcConfigFile[this.m_list.length];
/*  468 */       for (int i = 0; i < this.m_list.length; ++i)
/*      */       {
/*  470 */         files[i] = new IdcConfigFile(this.m_fileID + this.m_list[i], null, this.m_workspace);
/*      */       }
/*      */     }
/*  473 */     return files;
/*      */   }
/*      */ 
/*      */   public boolean mkdir()
/*      */   {
/*  480 */     return true;
/*      */   }
/*      */ 
/*      */   public Reader readToReader()
/*      */     throws IOException
/*      */   {
/*  489 */     Reader reader = null;
/*      */     try
/*      */     {
/*  492 */       if (this.m_workspace == null)
/*      */       {
/*  494 */         return readToReaderStmt();
/*      */       }
/*      */ 
/*  497 */       DataBinder binder = new DataBinder();
/*  498 */       binder.putLocal("dRTFileID", this.m_fileID);
/*  499 */       DataStreamValue drset = (DataStreamValue)this.m_workspace.createResultSet("QtextbyIDRunTimeConfigData", binder);
/*  500 */       if (((ResultSet)drset).isRowPresent())
/*      */       {
/*  502 */         reader = drset.getCharacterReader("dRTTextObject");
/*      */       }
/*      */       else
/*      */       {
/*  506 */         throw new FileNotFoundException(this.m_fileID);
/*      */       }
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  511 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileReadError", e.getMessage(), this.m_fileID);
/*  512 */       throw new IOException(msg, e);
/*      */     }
/*  514 */     return reader;
/*      */   }
/*      */ 
/*      */   public byte[] readToByteArray()
/*      */     throws IOException
/*      */   {
/*  523 */     byte[] buf = null;
/*  524 */     Reader reader = readToReader();
/*  525 */     if (reader == null)
/*      */     {
/*  527 */       return null;
/*      */     }
/*      */ 
/*  530 */     BufferedReader bufReader = null;
/*  531 */     String content = "";
/*  532 */     String line = null;
/*      */     try
/*      */     {
/*  535 */       bufReader = new BufferedReader(reader);
/*      */ 
/*  537 */       while ((line = bufReader.readLine()) != null)
/*      */       {
/*  539 */         content = content.concat(line);
/*  540 */         content = content.concat("\n");
/*      */       }
/*  542 */       buf = StringUtils.getBytes(content, FileUtils.m_javaSystemEncoding);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */       String msg;
/*  547 */       throw new IOException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*  551 */       FileUtils.closeObject(bufReader);
/*      */     }
/*      */ 
/*  554 */     return buf;
/*      */   }
/*      */ 
/*      */   public InputStream readToInputStream()
/*      */     throws IOException
/*      */   {
/*  563 */     InputStream is = null;
/*  564 */     byte[] buf = readToByteArray();
/*  565 */     if (buf != null)
/*      */     {
/*  567 */       is = new ByteArrayInputStream(buf, 0, buf.length);
/*      */     }
/*  569 */     return is;
/*      */   }
/*      */ 
/*      */   public boolean renameTo(File file)
/*      */   {
/*  580 */     if ((!exists()) || (isDirectory()))
/*      */     {
/*  582 */       return false;
/*      */     }
/*      */ 
/*  585 */     String oldFileID = this.m_fileID;
/*      */ 
/*  588 */     IdcConfigFile cfgFile = null;
/*  589 */     if (file instanceof IdcConfigFile)
/*      */     {
/*  591 */       cfgFile = (IdcConfigFile)file;
/*      */     }
/*      */     else
/*      */     {
/*  595 */       cfgFile = new IdcConfigFile(file.getAbsolutePath(), null, false, this.m_workspace);
/*      */     }
/*  597 */     String newFileID = cfgFile.m_fileID;
/*  598 */     String newFileName = cfgFile.m_fileName;
/*  599 */     String newRoot = cfgFile.m_relativeRoot;
/*  600 */     String newDir = cfgFile.m_relativeDir;
/*      */ 
/*  603 */     if (file.exists())
/*      */     {
/*  605 */       file.delete();
/*      */     }
/*      */ 
/*  609 */     Date date = new Date();
/*  610 */     cfgFile.m_lastModified = date.getTime();
/*  611 */     String dateStr = LocaleUtils.formatODBC(date);
/*      */ 
/*  613 */     newFileID = StringUtils.createQuotableString(newFileID);
/*  614 */     newFileName = StringUtils.createQuotableString(newFileName);
/*  615 */     newDir = StringUtils.createQuotableString(newDir);
/*  616 */     newRoot = StringUtils.createQuotableString(newRoot);
/*  617 */     oldFileID = StringUtils.createQuotableString(oldFileID);
/*      */ 
/*  619 */     String updateSQL = "UPDATE RunTimeConfigData SET dRTFileID = '" + newFileID + "', dRTFileName = '" + newFileName + "', dRTDir = '" + newDir + "', dRTRoot = '" + newRoot + "', dRTCreateDate = " + dateStr + ", dRTLastModified = " + dateStr + " WHERE dRTFileID = '" + oldFileID + "'";
/*      */ 
/*  624 */     Statement stmt = null;
/*  625 */     boolean isTran = false;
/*      */     try
/*      */     {
/*  628 */       if (this.m_workspace == null)
/*      */       {
/*  630 */         stmt = m_connection.createStatement();
/*  631 */         stmt.executeUpdate(updateSQL);
/*      */       }
/*      */       else
/*      */       {
/*  635 */         isTran = beginTran();
/*  636 */         this.m_workspace.executeSQL(updateSQL);
/*  637 */         if (isTran)
/*      */         {
/*  639 */           isTran = false;
/*  640 */           commitTran();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  645 */       this.m_exists = Boolean.valueOf(false);
/*  646 */       this.m_isFile = Boolean.valueOf(false);
/*  647 */       this.m_isDir = Boolean.valueOf(false);
/*  648 */       this.m_length = 0L;
/*  649 */       this.m_isUpdate = true;
/*  650 */       this.m_lastModified = -1L;
/*  651 */       cfgFile.m_exists = Boolean.valueOf(true);
/*  652 */       cfgFile.m_isFile = Boolean.valueOf(true);
/*  653 */       cfgFile.m_isDir = Boolean.valueOf(false);
/*  654 */       cfgFile.m_length = 0L;
/*  655 */       cfgFile.m_isUpdate = true;
/*      */ 
/*  657 */       int i = 1;
/*      */ 
/*  682 */       return i;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  661 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileRenameError", e.getMessage(), oldFileID, newFileID);
/*  662 */       Report.error(null, msg, e);
/*  663 */       int j = 0;
/*      */ 
/*  682 */       return j;
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/*  669 */         if (stmt != null)
/*      */         {
/*  671 */           stmt.close();
/*  672 */           stmt = null;
/*      */         }
/*      */       }
/*      */       catch (SQLException e)
/*      */       {
/*      */       }
/*      */ 
/*  679 */       if (isTran)
/*      */       {
/*  681 */         isTran = false;
/*  682 */         rollbackTran();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean setLastModified(long time)
/*      */   {
/*  690 */     boolean isTran = false;
/*      */     String dateStr;
/*      */     try {
/*  693 */       if (this.m_workspace == null)
/*      */       {
/*  695 */         boolean bool1 = setLastModifiedStmt(time);
/*      */         return bool1;
/*      */       }
/*  698 */       DataBinder binder = new DataBinder();
/*  699 */       Date date = new Date(time);
/*  700 */       this.m_lastModified = date.getTime();
/*  701 */       dateStr = LocaleUtils.formatODBC(date);
/*  702 */       binder.putLocal("dRTLastModified", dateStr);
/*  703 */       binder.putLocal("dRTFileID", this.m_fileID);
/*  704 */       isTran = beginTran();
/*  705 */       this.m_workspace.execute("UlastModifiedbyIDRunTimeConfigData", binder);
/*  706 */       if (isTran)
/*      */       {
/*  708 */         isTran = false;
/*  709 */         commitTran();
/*      */       }
/*  711 */       int i = 1;
/*      */ 
/*  724 */       return i;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  715 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileSetLastModifiedError", e.getMessage(), this.m_fileID);
/*  716 */       Report.error(null, msg, e);
/*  717 */       dateStr = 0;
/*      */ 
/*  724 */       return dateStr;
/*      */     }
/*      */     finally
/*      */     {
/*  721 */       if (isTran)
/*      */       {
/*  723 */         isTran = false;
/*  724 */         rollbackTran();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void write(String content)
/*      */   {
/*  734 */     write(content, false);
/*      */   }
/*      */ 
/*      */   public void write(byte[] b, int length, boolean append)
/*      */     throws IOException
/*      */   {
/*  743 */     String content = new String(b, 0, length, FileUtils.m_javaSystemEncoding);
/*  744 */     write(content, append);
/*      */   }
/*      */ 
/*      */   public void write(String content, boolean append)
/*      */   {
/*  752 */     boolean isTran = false;
/*      */     try
/*      */     {
/*  755 */       if (this.m_workspace == null)
/*      */       {
/*  757 */         writeStmt(content, append);
/*      */         return;
/*      */       }
/*      */ 
/*  761 */       DataBinder binder = new DataBinder();
/*  762 */       binder.putLocal("dRTFileID", this.m_fileID);
/*      */ 
/*  764 */       ResultSet rset = this.m_workspace.createResultSet("QtextbyIDRunTimeConfigData", binder);
/*  765 */       if (rset.isRowPresent())
/*      */       {
/*  767 */         if (append)
/*      */         {
/*  773 */           content = rset.getStringValue(0) + content;
/*      */         }
/*      */ 
/*  776 */         binder.putLocal("dRTTextObject", content);
/*  777 */         Date date = new Date();
/*  778 */         this.m_lastModified = date.getTime();
/*  779 */         String dateStr = LocaleUtils.formatODBC(date);
/*  780 */         binder.putLocal("dRTLastModified", dateStr);
/*      */ 
/*  782 */         isTran = beginTran();
/*  783 */         this.m_workspace.execute("UtextbyIDRunTimeConfigData", binder);
/*  784 */         if (isTran)
/*      */         {
/*  786 */           isTran = false;
/*  787 */           commitTran();
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  793 */         binder.putLocal("dRTTextObject", content);
/*  794 */         binder.putLocal("dRTFeature", this.m_feature);
/*  795 */         binder.putLocal("dRTRoot", this.m_relativeRoot);
/*  796 */         binder.putLocal("dRTDir", this.m_relativeDir);
/*  797 */         binder.putLocal("dRTFileName", this.m_fileName);
/*  798 */         Date date = new Date();
/*  799 */         this.m_lastModified = date.getTime();
/*  800 */         String dateStr = LocaleUtils.formatODBC(date);
/*  801 */         binder.putLocal("dRTCreateDate", dateStr);
/*  802 */         binder.putLocal("dRTLastModified", dateStr);
/*      */ 
/*  804 */         isTran = beginTran();
/*  805 */         this.m_workspace.execute("ItextRunTimeConfigData", binder);
/*  806 */         if (isTran)
/*      */         {
/*  808 */           isTran = false;
/*  809 */           commitTran();
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  814 */       byte[] b = StringUtils.getBytes(content, FileUtils.m_javaSystemEncoding);
/*  815 */       this.m_length = b.length;
/*  816 */       this.m_isUpdate = false;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  820 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileWriteError", e.getMessage(), this.m_fileID);
/*  821 */       Report.error(null, msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*  825 */       if (isTran)
/*      */       {
/*  827 */         isTran = false;
/*  828 */         rollbackTran();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void getValues()
/*      */   {
/*      */     try
/*      */     {
/*  837 */       if (this.m_workspace == null)
/*      */       {
/*  839 */         getValuesStmt();
/*  840 */         return;
/*      */       }
/*  842 */       DataBinder binder = new DataBinder();
/*  843 */       String path = this.m_fileID;
/*  844 */       if (this.m_fileID.endsWith("/"))
/*      */       {
/*  846 */         path = this.m_fileID.substring(0, this.m_fileID.length() - 1);
/*      */       }
/*  848 */       binder.putLocal("dRTFileID", path);
/*  849 */       binder.putLocal("dRTDir", path + "/%");
/*  850 */       ResultSet rset = this.m_workspace.createResultSet("QvalueRunTimeConfigData", binder);
/*      */ 
/*  852 */       if (rset.isRowPresent())
/*      */       {
/*  854 */         this.m_exists = Boolean.valueOf(true);
/*  855 */         if (rset.getStringValue(0).equals(path))
/*      */         {
/*  857 */           this.m_fileID = path;
/*  858 */           int num = this.m_fileID.lastIndexOf("/");
/*  859 */           this.m_relativeDir = this.m_fileID.substring(0, num + 1);
/*  860 */           this.m_fileName = this.m_fileID.substring(num + 1);
/*  861 */           this.m_isFile = Boolean.valueOf(true);
/*  862 */           this.m_isDir = Boolean.valueOf(false);
/*  863 */           this.m_list = new String[0];
/*  864 */           this.m_lastModified = rset.getDateValue(2).getTime();
/*      */         }
/*      */         else
/*      */         {
/*  868 */           this.m_fileID = (path + "/");
/*  869 */           this.m_relativeDir = this.m_fileID;
/*  870 */           this.m_fileName = "";
/*  871 */           this.m_isFile = Boolean.valueOf(false);
/*  872 */           this.m_isDir = Boolean.valueOf(true);
/*  873 */           this.m_lastModified = rset.getDateValue(2).getTime();
/*      */ 
/*  875 */           Vector contents = new IdcVector();
/*  876 */           for (rset.first(); rset.isRowPresent(); rset.next())
/*      */           {
/*  879 */             String content = rset.getStringValue(0).replace(this.m_fileID, "");
/*  880 */             int index = content.indexOf("/");
/*  881 */             if (index > 0)
/*      */             {
/*  886 */               content = content.substring(0, index);
/*  887 */               if (contents.contains(content))
/*      */                 continue;
/*  889 */               contents.add(content);
/*      */             }
/*      */             else
/*      */             {
/*  896 */               contents.add(content);
/*      */             }
/*      */           }
/*      */ 
/*  900 */           this.m_list = new String[contents.size()];
/*  901 */           contents.toArray(this.m_list);
/*      */         }
/*      */       }
/*  904 */       else if ((this.m_isDir != null) && (this.m_isDir.booleanValue()))
/*      */       {
/*  906 */         this.m_fileID = (path + "/");
/*  907 */         this.m_relativeDir = this.m_fileID;
/*  908 */         this.m_fileName = "";
/*      */ 
/*  910 */         this.m_exists = Boolean.valueOf(true);
/*  911 */         this.m_isFile = Boolean.valueOf(false);
/*  912 */         this.m_list = new String[0];
/*  913 */         this.m_lastModified = 0L;
/*      */       }
/*      */       else
/*      */       {
/*  917 */         this.m_exists = Boolean.valueOf(false);
/*  918 */         this.m_isFile = Boolean.valueOf(false);
/*  919 */         this.m_isDir = Boolean.valueOf(false);
/*  920 */         this.m_list = new String[0];
/*  921 */         this.m_lastModified = 0L;
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  927 */       String msg = LocaleUtils.encodeMessage("csIdcConfigFileIsDirectoryError", e.getMessage(), this.m_fileID);
/*  928 */       Report.error(null, msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void getValuesStmt()
/*      */     throws SQLException
/*      */   {
/*  941 */     Statement stmt = null;
/*      */     try
/*      */     {
/*  945 */       stmt = m_connection.createStatement(1004, 1007);
/*      */ 
/*  948 */       String path = this.m_fileID;
/*  949 */       if (this.m_fileID.endsWith("/"))
/*      */       {
/*  951 */         path = this.m_fileID.substring(0, this.m_fileID.length() - 1);
/*      */       }
/*  953 */       path = StringUtils.createQuotableString(path);
/*  954 */       String querySQL = "SELECT dRTFileID, dRTDir, dRTLastModified FROM RunTimeConfigData WHERE dRTFileID = '" + path + "' OR dRTDir LIKE '" + path + "/%'";
/*      */ 
/*  957 */       java.sql.ResultSet sqlset = stmt.executeQuery(querySQL);
/*  958 */       if (sqlset.next())
/*      */       {
/*  960 */         this.m_exists = Boolean.valueOf(true);
/*  961 */         if (sqlset.getString(1).equals(path))
/*      */         {
/*  963 */           this.m_fileID = path;
/*  964 */           int num = this.m_fileID.lastIndexOf("/");
/*  965 */           this.m_relativeDir = this.m_fileID.substring(0, num + 1);
/*  966 */           this.m_fileName = this.m_fileID.substring(num + 1);
/*  967 */           this.m_isFile = Boolean.valueOf(true);
/*  968 */           this.m_isDir = Boolean.valueOf(false);
/*  969 */           this.m_list = new String[0];
/*  970 */           this.m_lastModified = sqlset.getTimestamp(3).getTime();
/*      */         }
/*      */         else
/*      */         {
/*  974 */           this.m_fileID = (path + "/");
/*  975 */           this.m_relativeDir = this.m_fileID;
/*  976 */           this.m_fileName = "";
/*  977 */           this.m_isFile = Boolean.valueOf(false);
/*  978 */           this.m_isDir = Boolean.valueOf(true);
/*  979 */           this.m_lastModified = sqlset.getTimestamp(3).getTime();
/*      */ 
/*  981 */           Vector contents = new IdcVector();
/*  982 */           sqlset.beforeFirst();
/*  983 */           while (sqlset.next())
/*      */           {
/*  986 */             String content = sqlset.getString(1).replace(this.m_fileID, "");
/*  987 */             int index = content.indexOf("/");
/*  988 */             if (index > 0)
/*      */             {
/*  993 */               content = content.substring(0, index + 1);
/*  994 */               if (!contents.contains(content))
/*      */               {
/*  996 */                 contents.add(content);
/*      */               }
/*      */ 
/*      */             }
/*      */             else
/*      */             {
/* 1003 */               contents.add(content);
/*      */             }
/*      */           }
/*      */ 
/* 1007 */           this.m_list = new String[contents.size()];
/* 1008 */           contents.toArray(this.m_list);
/*      */         }
/*      */       }
/* 1011 */       else if ((this.m_isDir != null) && (this.m_isDir.booleanValue()))
/*      */       {
/* 1013 */         this.m_fileID = (path + "/");
/* 1014 */         this.m_relativeDir = this.m_fileID;
/* 1015 */         this.m_fileName = "";
/*      */ 
/* 1017 */         this.m_exists = Boolean.valueOf(true);
/* 1018 */         this.m_isFile = Boolean.valueOf(false);
/* 1019 */         this.m_list = new String[0];
/* 1020 */         this.m_lastModified = 0L;
/*      */       }
/*      */       else
/*      */       {
/* 1024 */         this.m_exists = Boolean.valueOf(false);
/* 1025 */         this.m_isFile = Boolean.valueOf(false);
/* 1026 */         this.m_isDir = Boolean.valueOf(false);
/* 1027 */         this.m_list = new String[0];
/* 1028 */         this.m_lastModified = 0L;
/*      */       }
/*      */     }
/*      */     finally
/*      */     {
/* 1033 */       if (stmt != null)
/*      */       {
/* 1035 */         stmt.close();
/* 1036 */         stmt = null;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean deleteStmt() throws SQLException
/*      */   {
/* 1043 */     Statement stmt = null;
/*      */     try
/*      */     {
/* 1046 */       this.m_fileID = StringUtils.createQuotableString(this.m_fileID);
/* 1047 */       String deleteSQL = "DELETE FROM RunTimeConfigData WHERE dRTFileID = '" + this.m_fileID + "'";
/* 1048 */       stmt = m_connection.createStatement();
/* 1049 */       stmt.executeUpdate(deleteSQL);
/*      */ 
/* 1051 */       this.m_exists = Boolean.valueOf(false);
/* 1052 */       this.m_isFile = Boolean.valueOf(false);
/* 1053 */       this.m_isDir = Boolean.valueOf(false);
/* 1054 */       this.m_length = 0L;
/* 1055 */       this.m_lastModified = -1L;
/*      */     }
/*      */     finally
/*      */     {
/* 1059 */       if (stmt != null)
/*      */       {
/* 1061 */         stmt.close();
/* 1062 */         stmt = null;
/*      */       }
/*      */     }
/*      */ 
/* 1066 */     return true;
/*      */   }
/*      */ 
/*      */   protected Reader readToReaderStmt() throws IOException
/*      */   {
/* 1071 */     Statement stmt = null;
/* 1072 */     Reader reader = null;
/*      */     try
/*      */     {
/* 1075 */       stmt = m_connection.createStatement();
/* 1076 */       this.m_fileID = StringUtils.createQuotableString(this.m_fileID);
/* 1077 */       String querySQL = "SELECT dRTTextObject FROM RunTimeConfigData WHERE dRTFileID = '" + this.m_fileID + "'";
/*      */ 
/* 1079 */       java.sql.ResultSet sqlset = stmt.executeQuery(querySQL);
/* 1080 */       if (sqlset.next())
/*      */       {
/* 1082 */         reader = sqlset.getCharacterStream(1);
/*      */       }
/*      */       else
/*      */       {
/* 1086 */         throw new FileNotFoundException(this.m_fileID);
/*      */       }
/*      */     }
/*      */     catch (SQLException e)
/*      */     {
/*      */       String msg;
/* 1092 */       throw new IOException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*      */       try
/*      */       {
/* 1098 */         if (stmt != null)
/*      */         {
/* 1100 */           stmt.close();
/* 1101 */           stmt = null;
/*      */         }
/*      */       }
/*      */       catch (SQLException e0)
/*      */       {
/*      */       }
/*      */     }
/*      */ 
/* 1109 */     return reader;
/*      */   }
/*      */ 
/*      */   protected boolean setLastModifiedStmt(long time) throws SQLException
/*      */   {
/* 1114 */     PreparedStatement pstmt = null;
/*      */     try
/*      */     {
/* 1117 */       pstmt = m_connection.prepareStatement("UPDATE RunTimeConfigData SET dRTLastModified = ? WHERE dRTFileID = ?");
/* 1118 */       Timestamp ts = new Timestamp(time);
/* 1119 */       this.m_lastModified = ts.getTime();
/* 1120 */       pstmt.setTimestamp(1, ts);
/* 1121 */       pstmt.setString(2, this.m_fileID);
/* 1122 */       pstmt.executeUpdate();
/*      */     }
/*      */     finally
/*      */     {
/* 1126 */       if (pstmt != null)
/*      */       {
/* 1128 */         pstmt.close();
/* 1129 */         pstmt = null;
/*      */       }
/*      */     }
/* 1132 */     return true;
/*      */   }
/*      */ 
/*      */   protected void writeStmt(String content, boolean append)
/*      */     throws SQLException, IOException
/*      */   {
/* 1140 */     Statement queryStmt = null;
/* 1141 */     PreparedStatement updateStmt = null;
/*      */     try
/*      */     {
/* 1144 */       this.m_fileID = StringUtils.createQuotableString(this.m_fileID);
/* 1145 */       queryStmt = m_connection.createStatement();
/* 1146 */       String querySQL = "SELECT dRTTextObject FROM RunTimeConfigData WHERE dRTFileID = '" + this.m_fileID + "'";
/*      */ 
/* 1148 */       java.sql.ResultSet sqlset = queryStmt.executeQuery(querySQL);
/* 1149 */       if (sqlset.next())
/*      */       {
/* 1151 */         if (append)
/*      */         {
/* 1156 */           content = sqlset.getString(1) + content;
/*      */         }
/*      */ 
/* 1159 */         updateStmt = m_connection.prepareStatement("UPDATE RunTimeConfigData SET dRTLastModified = ?, dRTTextObject = ? WHERE dRTFileID = ?");
/*      */ 
/* 1162 */         Date date = new Date();
/* 1163 */         this.m_lastModified = date.getTime();
/* 1164 */         updateStmt.setTimestamp(1, new Timestamp(date.getTime()));
/* 1165 */         updateStmt.setString(2, content);
/* 1166 */         updateStmt.setString(3, this.m_fileID);
/*      */       }
/*      */       else
/*      */       {
/* 1171 */         updateStmt = m_connection.prepareStatement("INSERT INTO RunTimeConfigData (dRTFileID, dRTFeature, dRTRoot, dRTDir, dRTFileName, dRTCreateDate, dRTLastModified, dRTTextObject) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
/*      */ 
/* 1174 */         updateStmt.setString(1, this.m_fileID);
/* 1175 */         updateStmt.setString(2, this.m_feature);
/* 1176 */         updateStmt.setString(3, this.m_relativeRoot);
/* 1177 */         updateStmt.setString(4, this.m_relativeDir);
/* 1178 */         updateStmt.setString(5, this.m_fileName);
/* 1179 */         Date date = new Date();
/* 1180 */         this.m_lastModified = date.getTime();
/* 1181 */         updateStmt.setTimestamp(6, new Timestamp(date.getTime()));
/* 1182 */         updateStmt.setTimestamp(7, new Timestamp(date.getTime()));
/* 1183 */         updateStmt.setString(8, content);
/*      */       }
/* 1185 */       updateStmt.executeUpdate();
/*      */ 
/* 1187 */       byte[] b = StringUtils.getBytes(content, FileUtils.m_javaSystemEncoding);
/* 1188 */       this.m_length = b.length;
/* 1189 */       this.m_isUpdate = false;
/*      */     }
/*      */     finally
/*      */     {
/* 1193 */       if (queryStmt != null)
/*      */       {
/* 1195 */         queryStmt.close();
/* 1196 */         queryStmt = null;
/*      */       }
/* 1198 */       if (updateStmt != null)
/*      */       {
/* 1200 */         updateStmt.close();
/* 1201 */         updateStmt = null;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean beginTran()
/*      */   {
/* 1209 */     Map state = new HashMap();
/* 1210 */     this.m_workspace.getConnectionState(state);
/* 1211 */     boolean isInTrans = ScriptUtils.convertObjectToBool(state.get("isInTransaction"), false);
/* 1212 */     if (!isInTrans)
/*      */     {
/*      */       try
/*      */       {
/* 1216 */         this.m_workspace.beginTran();
/* 1217 */         return true;
/*      */       }
/*      */       catch (DataException e)
/*      */       {
/* 1221 */         String msg = LocaleUtils.encodeMessage("csIdcConfigBeginTranError", e.getMessage());
/* 1222 */         Report.error(null, msg, e);
/*      */       }
/*      */     }
/* 1225 */     return false;
/*      */   }
/*      */ 
/*      */   public void commitTran()
/*      */   {
/*      */     try
/*      */     {
/* 1232 */       this.m_workspace.commitTran();
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/* 1236 */       String msg = LocaleUtils.encodeMessage("csIdcConfigCommitTranError", e.getMessage());
/* 1237 */       Report.error(null, msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void rollbackTran()
/*      */   {
/* 1243 */     this.m_workspace.rollbackTran();
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1248 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103947 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.IdcConfigFile
 * JD-Core Version:    0.5.4
 */