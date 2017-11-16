/*      */ package intradoc.apputilities.componentwizard;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.ParseOutput;
/*      */ import intradoc.common.ParseSyntaxException;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataFormatHTML;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceLoader;
/*      */ import intradoc.server.DataLoader;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.BufferedOutputStream;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Reader;
/*      */ import java.io.Writer;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ResourceFileInfo
/*      */ {
/*      */   public static final int NEW = 0;
/*      */   public static final int APPEND = 1;
/*      */   public static final int EDIT = 2;
/*      */   public static final int DELETE = 3;
/*      */   public static final int RENAME = 4;
/*      */   public static final int INFO = 5;
/*      */   public String m_type;
/*      */   public String m_filename;
/*      */   public String m_tableStr;
/*   76 */   public String m_loadOrder = "1";
/*   77 */   public long m_lastModified = -1L;
/*      */   public String m_errMsg;
/*      */   public String m_compDir;
/*      */   public String m_compName;
/*   82 */   public Hashtable m_tables = new Hashtable();
/*   83 */   public Map m_htmlIncludes = new Hashtable();
/*   84 */   public Map m_dataIncludes = new Hashtable();
/*   85 */   public Map m_strings = new Hashtable();
/*   86 */   public Properties m_environments = new Properties();
/*      */   public DataBinder m_binder;
/*      */   protected CWResourceContainer m_resources;
/*      */   protected Vector m_loadTables;
/*      */ 
/*      */   public ResourceFileInfo(String compName, String compDir, String type, String filename, String tables, String loadOrder)
/*      */   {
/*   95 */     this.m_compName = compName;
/*   96 */     this.m_compDir = compDir;
/*   97 */     this.m_type = type;
/*   98 */     this.m_filename = filename;
/*   99 */     this.m_tableStr = tables;
/*  100 */     this.m_loadOrder = loadOrder;
/*      */ 
/*  102 */     if (((!type.equals("query")) && (!type.equals("service"))) || (tables == null) || (tables.length() <= 0) || (tables.equalsIgnoreCase("null"))) {
/*      */       return;
/*      */     }
/*      */ 
/*  106 */     this.m_loadTables = StringUtils.parseArray(tables, ',', '^');
/*      */   }
/*      */ 
/*      */   public void load()
/*      */     throws ServiceException, DataException
/*      */   {
/*  112 */     if (this.m_filename.endsWith(".hda"))
/*      */     {
/*  114 */       cacheDynamicResourceFile();
/*      */     }
/*  116 */     else if (this.m_filename.endsWith(".cfg"))
/*      */     {
/*  118 */       this.m_environments = CWizardUtils.loadPropertiesFromFile(this.m_filename);
/*      */     }
/*  120 */     else if ((this.m_filename.endsWith(".htm")) || (this.m_filename.endsWith(".html")) || (this.m_filename.endsWith(".idoc")))
/*      */     {
/*  123 */       cacheStaticResourceFile();
/*      */     }
/*      */     else
/*      */     {
/*  127 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizInvalidResourceFileFormat", null, this.m_filename));
/*      */     }
/*      */ 
/*  132 */     this.m_lastModified = CWizardUtils.getLastModified(this.m_filename);
/*      */   }
/*      */ 
/*      */   public void createOrEditResource(int editType, String cwType, Properties props)
/*      */     throws ServiceException, IOException, DataException, ParseSyntaxException
/*      */   {
/*  138 */     String dir = FileUtils.getDirectory(this.m_filename);
/*      */ 
/*  140 */     if (editType == 0)
/*      */     {
/*  142 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(dir, 0, true);
/*      */     }
/*      */ 
/*  145 */     if (this.m_filename.endsWith(".hda"))
/*      */     {
/*  147 */       createOrEditDynamicResource(editType, cwType, props);
/*      */     }
/*  149 */     else if ((this.m_filename.endsWith(".htm")) || (this.m_filename.endsWith("html")) || (this.m_filename.endsWith(".idoc")))
/*      */     {
/*  152 */       createOrEditStaticResource(editType, cwType, props);
/*      */     }
/*  154 */     else if (this.m_filename.endsWith(".cfg"))
/*      */     {
/*  156 */       createOrEditConfigResource(editType);
/*      */     }
/*      */     else
/*      */     {
/*  160 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizInvalidResourceFileFormat", null, this.m_filename));
/*      */     }
/*      */ 
/*  165 */     load();
/*      */   }
/*      */ 
/*      */   public void renameResource(String newPrefix, String expectedPrefix)
/*      */     throws ServiceException, DataException
/*      */   {
/*      */     try
/*      */     {
/*  173 */       if (this.m_filename.endsWith(".hda"))
/*      */       {
/*  175 */         renameDynamicResource(newPrefix, expectedPrefix);
/*      */       }
/*  177 */       else if (this.m_filename.endsWith(".htm"))
/*      */       {
/*  179 */         renameStaticResource(newPrefix, expectedPrefix);
/*      */       }
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  184 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Vector getLoadTables()
/*      */   {
/*  190 */     return this.m_loadTables;
/*      */   }
/*      */ 
/*      */   protected void cacheStaticResourceFile() throws ServiceException
/*      */   {
/*  195 */     this.m_resources = new CWResourceContainer();
/*      */ 
/*  197 */     DataLoader.cacheResourceFile(this.m_resources, this.m_filename);
/*      */ 
/*  200 */     if (!this.m_resources.m_tables.isEmpty())
/*      */     {
/*  202 */       Map temptables = this.m_resources.m_tables;
/*  203 */       Iterator it = temptables.keySet().iterator();
/*  204 */       while (it.hasNext())
/*      */       {
/*  206 */         String tablename = (String)it.next();
/*  207 */         Table table = (Table)temptables.get(tablename);
/*  208 */         DataResultSet drset = new DataResultSet();
/*  209 */         if (table != null)
/*      */         {
/*  211 */           drset.init(table);
/*  212 */           this.m_tables.put(tablename, drset);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  217 */       if (this.m_loadTables != null)
/*      */       {
/*  219 */         String errMsg = "";
/*  220 */         for (int i = 0; i < this.m_loadTables.size(); ++i)
/*      */         {
/*  222 */           String temp = (String)this.m_loadTables.elementAt(i);
/*  223 */           if (this.m_tables.get(temp) != null)
/*      */             continue;
/*  225 */           String msg = LocaleUtils.encodeMessage("csCompWizTableInvalid", null, temp, this.m_filename);
/*      */ 
/*  227 */           errMsg = errMsg + msg;
/*      */         }
/*      */ 
/*  231 */         if (errMsg.length() > 0)
/*      */         {
/*  233 */           throw new ServiceException(errMsg);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  239 */       Iterator it = this.m_resources.m_tables.keySet().iterator();
/*  240 */       while (it.hasNext())
/*      */       {
/*  242 */         String name = (String)it.next();
/*  243 */         Table tble = (Table)this.m_resources.m_tables.get(name);
/*  244 */         DataResultSet rset = new DataResultSet();
/*  245 */         rset.init(tble);
/*  246 */         this.m_tables.put(name, rset);
/*      */       }
/*      */     }
/*      */ 
/*  250 */     if (!this.m_type.equals("resource"))
/*      */       return;
/*  252 */     this.m_htmlIncludes = this.m_resources.m_dynamicHtml;
/*  253 */     this.m_dataIncludes = this.m_resources.m_dynamicData;
/*  254 */     this.m_strings = this.m_resources.m_stringObjMap;
/*      */   }
/*      */ 
/*      */   protected void cacheDynamicResourceFile()
/*      */     throws ServiceException
/*      */   {
/*  260 */     String dir = FileUtils.getDirectory(this.m_filename);
/*  261 */     String filename = FileUtils.getName(this.m_filename);
/*      */ 
/*  263 */     this.m_binder = CWizardUtils.readFile(dir, filename, dir);
/*      */ 
/*  265 */     if (this.m_binder == null)
/*      */       return;
/*  267 */     for (Enumeration en = this.m_binder.getResultSetList(); en.hasMoreElements(); )
/*      */     {
/*  269 */       boolean addTable = true;
/*  270 */       String tableName = (String)en.nextElement();
/*      */ 
/*  273 */       if ((this.m_loadTables != null) && (this.m_loadTables.size() > 0))
/*      */       {
/*  275 */         addTable = false;
/*      */ 
/*  277 */         for (int i = 0; i < this.m_loadTables.size(); ++i)
/*      */         {
/*  279 */           String tempName = (String)this.m_loadTables.elementAt(i);
/*  280 */           tempName = tempName.trim();
/*  281 */           if (!tableName.equals(tempName))
/*      */             continue;
/*  283 */           addTable = true;
/*  284 */           break;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  289 */       if (addTable)
/*      */       {
/*  291 */         DataResultSet rset = (DataResultSet)this.m_binder.getResultSet(tableName);
/*  292 */         this.m_tables.put(tableName, rset);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void createOrEditDynamicResource(int editType, String cwType, Properties props)
/*      */     throws ServiceException, IOException, DataException, ParseSyntaxException
/*      */   {
/*  303 */     String dir = FileUtils.getDirectory(this.m_filename);
/*  304 */     String fname = FileUtils.getName(this.m_filename);
/*      */ 
/*  306 */     if (editType == 0)
/*      */     {
/*  308 */       FileUtils.checkOrCreateDirectory(dir, 0);
/*  309 */       this.m_binder = new DataBinder();
/*      */     }
/*      */     else
/*      */     {
/*  313 */       this.m_binder = CWizardUtils.readFile(dir, fname, dir);
/*      */     }
/*      */ 
/*  316 */     DataResultSet drset = null;
/*  317 */     String tablename = props.getProperty("tablename");
/*  318 */     Vector v = getColumns(props);
/*      */ 
/*  320 */     if (v != null)
/*      */     {
/*  322 */       int size = v.size();
/*  323 */       String[] finfo = new String[size];
/*  324 */       Vector newRow = new IdcVector();
/*  325 */       String primaryKey = (String)v.elementAt(0);
/*      */ 
/*  327 */       if (editType != 3)
/*      */       {
/*  329 */         for (int i = 0; i < size; ++i)
/*      */         {
/*  331 */           String colName = (String)v.elementAt(i);
/*  332 */           String val = null;
/*      */ 
/*  334 */           if ((cwType.equals("template")) && (colName.equals("filename")))
/*      */           {
/*  336 */             boolean isCopy = StringUtils.convertToBool(props.getProperty("isCopy"), false);
/*      */ 
/*  338 */             String name = props.getProperty("name");
/*  339 */             val = props.getProperty("resfilename");
/*      */ 
/*  341 */             if (editType != 2)
/*      */             {
/*  343 */               String targetPath = FileUtils.getDirectory(this.m_filename);
/*  344 */               targetPath = FileUtils.getAbsolutePath(targetPath, val);
/*      */ 
/*  346 */               if (isCopy)
/*      */               {
/*  348 */                 String copyPath = props.getProperty("copyPath");
/*  349 */                 if ((copyPath != null) && (copyPath.length() > 0))
/*      */                 {
/*  351 */                   FileUtils.copyFile(copyPath, targetPath);
/*      */                 }
/*      */               }
/*      */               else
/*      */               {
/*  356 */                 createHtmlFile(name, targetPath);
/*      */               }
/*      */             }
/*      */           }
/*      */           else
/*      */           {
/*  362 */             val = props.getProperty(colName);
/*      */           }
/*      */ 
/*  365 */           finfo[i] = colName;
/*  366 */           if ((val != null) && (val.length() > 0))
/*      */           {
/*  368 */             newRow.addElement(val);
/*      */           }
/*      */           else
/*      */           {
/*  372 */             newRow.addElement("My" + colName);
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  377 */       drset = (DataResultSet)this.m_binder.getResultSet(tablename);
/*      */ 
/*  379 */       if (drset == null)
/*      */       {
/*  381 */         drset = new DataResultSet(finfo);
/*      */       }
/*      */ 
/*  384 */       if (drset.getNumFields() != size)
/*      */       {
/*  386 */         throw new ServiceException("!csCompWizColumnMismatch");
/*      */       }
/*      */ 
/*  389 */       if ((editType == 0) || (editType == 1))
/*      */       {
/*  391 */         drset.createEmptyRow();
/*  392 */         drset.addRow(newRow);
/*      */       }
/*      */       else
/*      */       {
/*  396 */         String val = props.getProperty(primaryKey);
/*  397 */         if ((val == null) || (val.length() == 0))
/*      */         {
/*  399 */           throw new ServiceException(LocaleUtils.encodeMessage("csCompWizTableUpdateError", null, tablename, primaryKey));
/*      */         }
/*      */ 
/*  403 */         if (drset.findRow(0, val) == null)
/*      */         {
/*  405 */           throw new ServiceException(LocaleUtils.encodeMessage("csCompWizTableUpdateError", null, tablename, primaryKey));
/*      */         }
/*      */ 
/*  409 */         if (editType == 3)
/*      */         {
/*  411 */           drset.deleteCurrentRow();
/*      */         }
/*      */         else
/*      */         {
/*  415 */           int index = drset.getCurrentRow();
/*  416 */           drset.setRowValues(newRow, index);
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  422 */       drset = new DataResultSet();
/*      */     }
/*      */ 
/*  425 */     this.m_binder.addResultSet(tablename, drset);
/*  426 */     CWizardUtils.writeFile(dir, fname, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void createOrEditStaticResource(int editType, String cwType, Properties props)
/*      */     throws ServiceException, IOException, DataException, ParseSyntaxException
/*      */   {
/*  433 */     String dir = FileUtils.getDirectory(this.m_filename);
/*      */ 
/*  435 */     String temp = FileUtils.directorySlashes(dir) + "temp.htm";
/*  436 */     StringBuffer buffer = new StringBuffer();
/*  437 */     File tempFile = new File(temp);
/*      */ 
/*  439 */     if (editType == 0)
/*      */     {
/*  441 */       BufferedWriter writer = null;
/*      */       try
/*      */       {
/*  445 */         writer = FileUtils.openDataWriter(tempFile, DataSerializeUtils.getSystemEncoding());
/*      */ 
/*  447 */         FileUtils.checkOrCreateDirectory(dir, 0);
/*  448 */         IdcStringBuilder str = new IdcStringBuilder();
/*  449 */         DataFormatHTML.appendBegin(str, this.m_compName + ' ' + cwType);
/*  450 */         writer.write(str.toString());
/*      */ 
/*  453 */         if (cwType.equals("htmlIncludeOrString"))
/*      */         {
/*  455 */           int resourceType = CWizardUtils.determineCoreResourceTypeFromCheckboxes(props);
/*  456 */           writeHtmlIncludeOrString(props, writer, resourceType);
/*      */         }
/*      */         else
/*      */         {
/*  460 */           props.put("createNew", "true");
/*  461 */           writeTableInfo(editType, cwType, props, writer);
/*      */         }
/*      */ 
/*  464 */         str.setLength(0);
/*  465 */         DataFormatHTML.appendEnd(str);
/*  466 */         writer.write(str.toString());
/*      */       }
/*      */       finally
/*      */       {
/*  470 */         if (writer != null)
/*      */         {
/*  472 */           writer.close();
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  478 */       updateStaticResource(editType, cwType, props, buffer, tempFile);
/*      */     }
/*      */ 
/*  481 */     FileUtils.renameFile(tempFile.getAbsolutePath(), this.m_filename);
/*      */   }
/*      */ 
/*      */   protected void updateStaticResource(int editType, String cwType, Properties props, StringBuffer buffer, File outFile)
/*      */     throws IOException, ParseSyntaxException, ServiceException
/*      */   {
/*  488 */     ParseOutput parseOutput = new ParseOutput();
/*      */ 
/*  491 */     boolean isEntryPresent = false;
/*  492 */     boolean isIncludeOrString = cwType.equals("htmlIncludeOrString");
/*  493 */     int resourceType = CWizardUtils.determineCoreResourceTypeFromCheckboxes(props);
/*      */ 
/*  495 */     if (!isIncludeOrString)
/*      */     {
/*  497 */       String tablename = props.getProperty("tablename");
/*  498 */       if ((tablename == null) || (tablename.length() == 0))
/*      */       {
/*  500 */         throw new ServiceException("!csCompWizTablenameMissing");
/*      */       }
/*  502 */       if ((this.m_tables != null) && (this.m_tables.get(tablename) != null))
/*      */       {
/*  504 */         isEntryPresent = true;
/*      */       }
/*      */     }
/*      */ 
/*  508 */     BufferedReader reader = null;
/*  509 */     BufferedWriter writer = null;
/*      */     try
/*      */     {
/*  512 */       BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(this.m_filename));
/*      */ 
/*  514 */       String encoding = ResourceLoader.detectEncoding(bstream, ResourceLoader.F_IS_HTML);
/*  515 */       reader = FileUtils.openDataReader(bstream, encoding);
/*  516 */       writer = FileUtils.openDataWriter(outFile, encoding);
/*  517 */       parseOutput.m_writer = writer;
/*  518 */       String tag = null;
/*      */ 
/*  522 */       if (((isIncludeOrString) && (editType == 1)) || ((!isIncludeOrString) && (!isEntryPresent) && (editType == 1)))
/*      */       {
/*  525 */         if ((!this.m_tables.isEmpty()) || (!this.m_htmlIncludes.isEmpty()) || (!this.m_dataIncludes.isEmpty()) || (!this.m_strings.isEmpty()))
/*      */         {
/*  527 */           if (!findLastScriptTag(reader, parseOutput))
/*      */           {
/*  529 */             throw new ServiceException("!csCompWizScriptTagError");
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  534 */           int count = 0;
/*  535 */           while (CWParser.findHtmlTagEx(reader, parseOutput, "/body", true, true) == true)
/*      */           {
/*  537 */             ++count;
/*  538 */             if (count > 1)
/*      */             {
/*  540 */               throw new ServiceException(LocaleUtils.encodeMessage("csCompWizMultBodyTagError", null, this.m_filename));
/*      */             }
/*      */ 
/*  544 */             tag = parseOutput.waitingBufferAsString().trim();
/*  545 */             parseOutput.writePending();
/*      */           }
/*      */ 
/*  548 */           if (count == 0)
/*      */           {
/*  550 */             throw new ServiceException(LocaleUtils.encodeMessage("csCompWizBodyTagMissing", null, this.m_filename));
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  555 */         if (isIncludeOrString)
/*      */         {
/*  557 */           writeHtmlIncludeOrString(props, parseOutput.m_writer, resourceType);
/*      */         }
/*      */         else
/*      */         {
/*  561 */           props.put("createNew", "true");
/*  562 */           writeTableInfo(editType, cwType, props, parseOutput.m_writer);
/*      */         }
/*      */ 
/*  565 */         if ((tag != null) && (tag.length() > 0))
/*      */         {
/*  567 */           parseOutput.m_writer.write("<" + tag + ">");
/*      */         }
/*      */ 
/*  570 */         parseOutput.writePending();
/*      */       }
/*      */       else
/*      */       {
/*  574 */         findAndUpdateStaticResource(editType, cwType, props, parseOutput, reader);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  584 */       if (reader != null)
/*      */       {
/*  586 */         reader.close();
/*      */       }
/*  588 */       if (writer != null)
/*      */       {
/*  590 */         writer.close();
/*      */       }
/*  592 */       parseOutput.releaseBuffers();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void findAndUpdateStaticResource(int editType, String cwType, Properties props, ParseOutput parseOutput, Reader reader)
/*      */     throws IOException, ParseSyntaxException, DataException, ServiceException
/*      */   {
/*  600 */     String resTag = "table";
/*  601 */     String resName = "";
/*  602 */     boolean found = false;
/*  603 */     boolean result = false;
/*  604 */     boolean isIncludeOrString = cwType.equals("htmlIncludeOrString");
/*  605 */     int resType = CWizardUtils.determineCoreResourceTypeFromCheckboxes(props);
/*  606 */     boolean isInclude = (isIncludeOrString) && (resType != 7);
/*      */ 
/*  608 */     if (isIncludeOrString)
/*      */     {
/*  610 */       if (isInclude)
/*      */       {
/*  612 */         resTag = (resType == 1) ? "dynamicdata" : "dynamichtml";
/*      */       }
/*      */ 
/*  616 */       resName = props.getProperty("includeOrString");
/*      */     }
/*      */     else
/*      */     {
/*  620 */       resName = props.getProperty("tablename");
/*      */     }
/*      */     do
/*      */     {
/*  624 */       if (CWParser.findScriptTagEx(reader, parseOutput, '@', true, true) != true) {
/*      */         break;
/*      */       }
/*  627 */       String value = null;
/*  628 */       String name = null;
/*  629 */       char ch = ' ';
/*  630 */       String tag = parseOutput.waitingBufferAsString().trim();
/*      */ 
/*  632 */       if ((isIncludeOrString) && (!isInclude))
/*      */       {
/*  634 */         ch = '=';
/*      */       }
/*      */ 
/*  637 */       int loc = tag.indexOf(ch);
/*      */ 
/*  639 */       if (loc > 0)
/*      */       {
/*  641 */         value = tag.substring(loc + 1).trim();
/*  642 */         name = tag.substring(0, loc).trim();
/*      */ 
/*  644 */         if (((isIncludeOrString) && (!isInclude) && (name.equals(resName))) || ((name.equalsIgnoreCase(resTag)) && (value.equals(resName))))
/*      */         {
/*  647 */           parseOutput.writePending();
/*  648 */           found = true;
/*      */         }
/*      */       }
/*      */ 
/*  652 */       if ((found) && (isIncludeOrString)) {
/*      */         break;
/*      */       }
/*      */ 
/*  656 */       writeToPendingBuffer(parseOutput, "<@");
/*  657 */       parseOutput.copyToPending(true, true);
/*  658 */       writeToPendingBuffer(parseOutput, "@>");
/*  659 */       parseOutput.writePending();
/*  660 */     }while (!found);
/*      */ 
/*  667 */     if (!found)
/*      */     {
/*  669 */       throw new ServiceException(LocaleUtils.encodeMessage("csFieldNotFound", null, resName));
/*      */     }
/*      */ 
/*  673 */     if (isIncludeOrString)
/*      */     {
/*  675 */       if (isInclude)
/*      */       {
/*  677 */         result = CWParser.findScriptTagEx(reader, parseOutput, '@', true, false);
/*      */ 
/*  679 */         if (!result)
/*      */         {
/*  681 */           parseOutput.createParsingException("!csCompWizScriptTagMissing");
/*      */         }
/*      */       }
/*      */ 
/*  685 */       parseOutput.clearPending();
/*      */ 
/*  687 */       if (editType == 2)
/*      */       {
/*  689 */         writeHtmlIncludeOrString(props, parseOutput.m_writer, resType);
/*      */       }
/*      */ 
/*      */     }
/*  694 */     else if (editType == 1)
/*      */     {
/*  696 */       result = CWParser.findHtmlTagEx(reader, parseOutput, "/table", false, true);
/*      */ 
/*  699 */       if (!result)
/*      */       {
/*  701 */         parseOutput.createParsingException(LocaleUtils.encodeMessage("csCompWizTableTagMissing", null, resName));
/*      */       }
/*      */ 
/*  704 */       parseOutput.writePending();
/*  705 */       writeTableInfo(editType, cwType, props, parseOutput.m_writer);
/*  706 */       parseOutput.m_writer.write("\n</table>");
/*      */     }
/*      */     else
/*      */     {
/*  710 */       replaceOrDeleteRow(editType, cwType, resName, props, parseOutput, reader);
/*      */     }
/*      */ 
/*  716 */     findLastScriptTag(reader, parseOutput);
/*  717 */     parseOutput.writePending();
/*      */   }
/*      */ 
/*      */   protected void replaceOrDeleteRow(int editType, String cwType, String resName, Properties props, ParseOutput parseOutput, Reader reader)
/*      */     throws IOException, ParseSyntaxException, DataException, ServiceException
/*      */   {
/*  725 */     boolean done = false;
/*  726 */     boolean foundRow = false;
/*  727 */     String columnName = props.getProperty("columnName");
/*  728 */     String colname = props.getProperty(columnName);
/*  729 */     String[] columnTags = { "td", "/td" };
/*  730 */     String[] rowTags = { "tr", "/tr" };
/*  731 */     int beginName = 0;
/*  732 */     int endName = 0;
/*  733 */     boolean writeToOutput = true;
/*  734 */     boolean isRow = true;
/*  735 */     String[] tags = null;
/*      */ 
/*  737 */     while (!done)
/*      */     {
/*  739 */       if (isRow)
/*      */       {
/*  741 */         tags = rowTags;
/*      */       }
/*      */       else
/*      */       {
/*  745 */         tags = columnTags;
/*      */       }
/*  747 */       int match = CWParser.findHtmlTagsEx(reader, parseOutput, tags, false, writeToOutput);
/*      */ 
/*  750 */       switch (match)
/*      */       {
/*      */       case 0:
/*  753 */         if (isRow)
/*      */         {
/*  755 */           parseOutput.writePending();
/*  756 */           writeToPendingBuffer(parseOutput, "<tr>");
/*  757 */           isRow = false;
/*      */         }
/*      */         else
/*      */         {
/*  761 */           writeToPendingBuffer(parseOutput, "<td>");
/*  762 */           beginName = parseOutput.m_numPending;
/*      */         }
/*  764 */         break;
/*      */       case 1:
/*  766 */         if (isRow)
/*      */         {
/*  768 */           if (foundRow)
/*      */           {
/*  770 */             done = true;
/*      */ 
/*  772 */             parseOutput.clearPending();
/*  773 */             if (editType == 2)
/*      */             {
/*  775 */               writeTableInfo(editType, cwType, props, parseOutput.m_writer);
/*      */             }
/*      */ 
/*      */           }
/*      */           else
/*      */           {
/*  781 */             writeToPendingBuffer(parseOutput, "</tr>");
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  786 */           endName = parseOutput.m_numPending;
/*  787 */           if (beginName < endName)
/*      */           {
/*  789 */             String tempStr = new String(parseOutput.m_pendingBuf, beginName, endName - beginName);
/*      */ 
/*  791 */             if (colname.equals(tempStr.trim()))
/*      */             {
/*  793 */               parseOutput.clearPending();
/*  794 */               foundRow = true;
/*  795 */               writeToOutput = false;
/*      */             }
/*      */           }
/*      */ 
/*  799 */           if (!foundRow)
/*      */           {
/*  801 */             writeToPendingBuffer(parseOutput, "</td>");
/*      */           }
/*      */ 
/*  804 */           isRow = true;
/*      */         }
/*  806 */         break;
/*      */       default:
/*  808 */         parseOutput.createParsingException(LocaleUtils.encodeMessage("csCompWizColMissing", null, colname, resName));
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void renameDynamicResource(String newPrefix, String expectedPrefix)
/*      */     throws ServiceException, DataException
/*      */   {
/*  817 */     boolean isChanged = false;
/*  818 */     cacheDynamicResourceFile();
/*      */ 
/*  820 */     if ((this.m_tables == null) || (this.m_tables.isEmpty()))
/*      */       return;
/*  822 */     for (Enumeration en = this.m_tables.keys(); en.hasMoreElements(); )
/*      */     {
/*  824 */       String tablename = (String)en.nextElement();
/*  825 */       String copytable = tablename;
/*  826 */       tablename = tablename.toLowerCase();
/*  827 */       if (tablename.startsWith(expectedPrefix))
/*      */       {
/*  829 */         tablename = newPrefix + copytable.substring(expectedPrefix.length(), tablename.length());
/*      */ 
/*  831 */         DataResultSet drset = (DataResultSet)this.m_tables.get(copytable);
/*  832 */         this.m_tables.remove(copytable);
/*  833 */         this.m_binder.removeResultSet(copytable);
/*      */ 
/*  835 */         FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "filename" }, false);
/*      */ 
/*  840 */         if (info[0].m_index >= 0)
/*      */         {
/*  842 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*      */           {
/*  844 */             String filename = ResultSetUtils.getValue(drset, info[0].m_name);
/*  845 */             if (filename == null) {
/*      */               break;
/*      */             }
/*      */ 
/*  849 */             String dir = FileUtils.getDirectory(filename);
/*  850 */             String fname = FileUtils.getName(filename);
/*  851 */             String oldfname = filename;
/*  852 */             if ((dir != null) && (dir.length() > 0))
/*      */             {
/*  854 */               filename = dir;
/*      */             }
/*      */             else
/*      */             {
/*  858 */               filename = "";
/*      */             }
/*      */ 
/*  861 */             if (!fname.startsWith(expectedPrefix))
/*      */               continue;
/*  863 */             filename = filename + newPrefix.toLowerCase() + fname.substring(expectedPrefix.length(), fname.length());
/*      */ 
/*  865 */             drset.setCurrentValue(info[0].m_index, filename);
/*      */ 
/*  868 */             String absDir = FileUtils.getDirectory(this.m_filename);
/*  869 */             absDir = FileUtils.directorySlashes(absDir);
/*  870 */             FileUtils.renameFile(absDir + oldfname, absDir + filename);
/*      */           }
/*      */         }
/*      */ 
/*  874 */         this.m_tables.put(tablename, drset);
/*  875 */         this.m_binder.addResultSet(tablename, drset);
/*  876 */         isChanged = true;
/*      */       }
/*      */     }
/*      */ 
/*  880 */     if (!isChanged)
/*      */       return;
/*  882 */     CWizardUtils.writeFile(FileUtils.getDirectory(this.m_filename), FileUtils.getName(this.m_filename), this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void renameStaticResource(String newPrefix, String expectedPrefix)
/*      */     throws ServiceException, IOException
/*      */   {
/*  891 */     String dir = FileUtils.getDirectory(this.m_filename);
/*      */ 
/*  893 */     String temp = FileUtils.directorySlashes(dir) + "temp.htm";
/*  894 */     File tempFile = new File(temp);
/*  895 */     BufferedWriter writer = FileUtils.openDataWriter(tempFile);
/*      */ 
/*  897 */     ParseOutput parseOutput = new ParseOutput();
/*  898 */     parseOutput.m_writer = writer;
/*      */ 
/*  900 */     BufferedReader reader = null;
/*      */     try
/*      */     {
/*  903 */       BufferedInputStream bstream = new BufferedInputStream(new FileInputStream(this.m_filename));
/*      */ 
/*  905 */       String encoding = ResourceLoader.detectEncoding(bstream, ResourceLoader.F_IS_HTML);
/*  906 */       reader = FileUtils.openDataReader(bstream, encoding);
/*      */ 
/*  908 */       while (CWParser.findScriptTagEx(reader, parseOutput, '@', false, true) == true)
/*      */       {
/*  910 */         boolean isChanged = false;
/*      */ 
/*  913 */         String tag = parseOutput.waitingBufferAsString().trim();
/*      */ 
/*  916 */         int loc = tag.indexOf(32);
/*  917 */         String value = null;
/*  918 */         String name = null;
/*  919 */         String tempValue = null;
/*      */ 
/*  921 */         if (loc > 0)
/*      */         {
/*  923 */           value = tag.substring(loc + 1).trim();
/*  924 */           name = tag.substring(0, loc).trim();
/*  925 */           tempValue = value.toLowerCase();
/*  926 */           if (tempValue.startsWith(expectedPrefix))
/*      */           {
/*  928 */             isChanged = true;
/*  929 */             value = newPrefix + value.substring(expectedPrefix.length(), tempValue.length());
/*      */           }
/*      */ 
/*  932 */           parseOutput.writePending();
/*      */         }
/*      */ 
/*  935 */         writeToPendingBuffer(parseOutput, "<@");
/*  936 */         if (isChanged)
/*      */         {
/*  938 */           writeToPendingBuffer(parseOutput, name + " " + value + "@");
/*      */         }
/*      */         else
/*      */         {
/*  942 */           parseOutput.copyToPending(true, false);
/*      */         }
/*  944 */         writeToPendingBuffer(parseOutput, ">");
/*  945 */         parseOutput.writePending();
/*      */       }
/*      */ 
/*  948 */       parseOutput.writePending();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  956 */       writer.close();
/*  957 */       if (reader != null)
/*      */       {
/*  959 */         reader.close();
/*      */       }
/*  961 */       parseOutput.releaseBuffers();
/*      */     }
/*      */ 
/*  964 */     FileUtils.renameFile(tempFile.getAbsolutePath(), this.m_filename);
/*      */   }
/*      */ 
/*      */   protected void writeTableInfo(int editType, String cwType, Properties props, Writer writer)
/*      */     throws IOException, ParseSyntaxException, DataException, ServiceException
/*      */   {
/*  971 */     boolean createNew = StringUtils.convertToBool(props.getProperty("createNew"), false);
/*  972 */     String tablename = props.getProperty("tablename");
/*  973 */     Vector cols = getColumns(props);
/*  974 */     if (createNew)
/*      */     {
/*  976 */       writer.write("\n<@table " + tablename + "@>\n");
/*      */     }
/*      */ 
/*  980 */     if ((cwType.equals("service")) || (cwType.equals("query")))
/*      */     {
/*  982 */       DataBinder data = new DataBinder(props);
/*  983 */       String includeName = null;
/*      */ 
/*  985 */       if (cwType.equals("query"))
/*      */       {
/*  987 */         if (createNew)
/*      */         {
/*  989 */           includeName = "query_new_table";
/*      */         }
/*      */         else
/*      */         {
/*  993 */           includeName = "query_row";
/*      */         }
/*  995 */         DataResultSet params = (DataResultSet)props.get("parametersData");
/*  996 */         if (params != null)
/*      */         {
/*  998 */           data.addResultSet("parametersData", params);
/*  999 */           props.remove("parametersData");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1004 */         if (createNew)
/*      */         {
/* 1006 */           includeName = "service_new_table";
/*      */         }
/*      */         else
/*      */         {
/* 1010 */           includeName = "service_row";
/*      */         }
/* 1012 */         DataResultSet actions = (DataResultSet)props.get("actionsData");
/* 1013 */         if (actions != null)
/*      */         {
/* 1015 */           data.addResultSet("actionsData", actions);
/* 1016 */           props.remove("actionsData");
/*      */         }
/*      */       }
/* 1019 */       writer.write(CWizardUtils.retrieveDynamicHtml(data, includeName));
/*      */     }
/*      */     else
/*      */     {
/* 1023 */       StringBuffer buffer = new StringBuffer();
/* 1024 */       buffer.append("<table border=1><caption><strong>\n");
/* 1025 */       addColumnInfo(cols, buffer);
/* 1026 */       buffer.append("</table>\n");
/* 1027 */       writer.write(buffer.toString());
/*      */     }
/*      */ 
/* 1030 */     if (!createNew)
/*      */       return;
/* 1032 */     writer.write("\n<@end@>\n");
/*      */   }
/*      */ 
/*      */   protected void writeHtmlIncludeOrString(Properties props, Writer writer, int resourceType)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1039 */     StringBuffer buffer = new StringBuffer();
/*      */ 
/* 1041 */     String incname = props.getProperty("includeOrString");
/* 1042 */     String incdata = props.getProperty("includeOrStringData");
/*      */ 
/* 1044 */     if ((incname == null) || (incname.length() == 0))
/*      */     {
/* 1046 */       throw new ServiceException("!csCompWizIncludeNameNotFound");
/*      */     }
/*      */ 
/* 1049 */     if ((incdata == null) || (incdata.length() == 0))
/*      */     {
/* 1051 */       incdata = "";
/*      */     }
/* 1053 */     boolean isInclude = resourceType != 7;
/*      */ 
/* 1055 */     if (isInclude)
/*      */     {
/* 1057 */       String beginStr = (resourceType == 1) ? "\n<@dynamicdata " : "\n<@dynamichtml ";
/*      */ 
/* 1059 */       buffer.append(beginStr + incname + "@>\n");
/* 1060 */       buffer.append(incdata.trim() + "\n");
/* 1061 */       buffer.append("<@end@>\n");
/*      */     }
/*      */     else
/*      */     {
/* 1065 */       buffer.append("\n<@" + incname + "=" + incdata + "@>\n");
/*      */     }
/*      */ 
/* 1068 */     writer.write(buffer.toString());
/*      */   }
/*      */ 
/*      */   protected void createOrEditConfigResource(int editType)
/*      */     throws DataException, ServiceException, IOException
/*      */   {
/* 1074 */     Properties cfgProps = new Properties();
/* 1075 */     Vector keys = new IdcVector();
/* 1076 */     Vector extraKeys = new IdcVector();
/* 1077 */     if (editType != 0)
/*      */       return;
/* 1079 */     cfgProps.put("MyEnvironmentParameter", "MyValue");
/* 1080 */     keys.addElement("MyEnvironmentParameter");
/*      */ 
/* 1083 */     OutputStream output = new BufferedOutputStream(FileUtilsCfgBuilder.getCfgOutputStream(this.m_filename, null));
/*      */ 
/* 1085 */     SystemPropertiesEditor.writeFile(cfgProps, keys, extraKeys, output, null);
/*      */   }
/*      */ 
/*      */   protected void createHtmlFile(String title, String filename)
/*      */     throws IOException, ServiceException
/*      */   {
/* 1092 */     IdcStringBuilder buffer = new IdcStringBuilder();
/* 1093 */     String dir = FileUtils.getDirectory(filename);
/*      */ 
/* 1095 */     String temp = FileUtils.directorySlashes(dir) + "temp.htm";
/* 1096 */     File tempFile = new File(temp);
/* 1097 */     BufferedWriter writer = FileUtils.openDataWriter(tempFile);
/*      */ 
/* 1099 */     DataFormatHTML.appendBegin(buffer, this.m_compName + ' ' + title);
/* 1100 */     DataFormatHTML.appendEnd(buffer);
/* 1101 */     writer.write(buffer.toString());
/* 1102 */     writer.close();
/*      */ 
/* 1104 */     FileUtils.renameFile(tempFile.getAbsolutePath(), filename);
/*      */   }
/*      */ 
/*      */   protected void addColumnInfo(Vector mergeColumnList, StringBuffer buffer)
/*      */     throws DataException
/*      */   {
/* 1110 */     addColumn(mergeColumnList, buffer, true);
/* 1111 */     addColumn(mergeColumnList, buffer, false);
/*      */   }
/*      */ 
/*      */   protected void addColumn(Vector mergeColumnList, StringBuffer buffer, boolean isColumnName)
/*      */   {
/* 1117 */     if (mergeColumnList == null)
/*      */       return;
/* 1119 */     int size = mergeColumnList.size();
/*      */ 
/* 1121 */     buffer.append("\n<tr>\n");
/* 1122 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1124 */       String colName = (String)mergeColumnList.elementAt(i);
/* 1125 */       if (isColumnName)
/*      */       {
/* 1127 */         buffer.append("<td>" + colName + "</td>");
/*      */       }
/*      */       else
/*      */       {
/* 1131 */         buffer.append("<td>My" + colName + "</td>");
/*      */       }
/*      */     }
/*      */ 
/* 1135 */     buffer.append("\n</tr>\n");
/*      */   }
/*      */ 
/*      */   protected Vector getColumns(Properties props)
/*      */   {
/* 1141 */     Vector v = null;
/* 1142 */     String mergecols = props.getProperty("mergeTableColumns");
/*      */ 
/* 1144 */     if ((mergecols != null) && (mergecols.length() > 0))
/*      */     {
/* 1146 */       v = StringUtils.parseArray(mergecols, ',', '^');
/*      */     }
/*      */ 
/* 1149 */     return v;
/*      */   }
/*      */ 
/*      */   protected boolean findLastScriptTag(Reader reader, ParseOutput parseOutput)
/*      */     throws IOException, ParseSyntaxException
/*      */   {
/* 1155 */     boolean isScriptExist = false;
/*      */ 
/* 1157 */     while (CWParser.findScriptTagEx(reader, parseOutput, '@', true, true) == true)
/*      */     {
/* 1159 */       writeToPendingBuffer(parseOutput, "<@");
/* 1160 */       parseOutput.copyToPending(true, true);
/* 1161 */       writeToPendingBuffer(parseOutput, "@>");
/* 1162 */       parseOutput.writePending();
/* 1163 */       isScriptExist = true;
/*      */     }
/* 1165 */     return isScriptExist;
/*      */   }
/*      */ 
/*      */   protected void writeToPendingBuffer(ParseOutput parseOutput, String buf)
/*      */   {
/* 1170 */     for (int i = 0; i < buf.length(); ++i)
/*      */     {
/* 1172 */       parseOutput.m_pendingBuf[(parseOutput.m_numPending++)] = buf.charAt(i);
/*      */     }
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 1179 */     return this.m_filename;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1184 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97206 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ResourceFileInfo
 * JD-Core Version:    0.5.4
 */