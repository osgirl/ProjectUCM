/*      */ package intradoc.apputilities.componentwizard;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.PropParameters;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.Workspace;
/*      */ import intradoc.server.utils.CompInstallUtils;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.IOException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class IntradocComponent
/*      */ {
/*   57 */   public static final String[][] RES_DEF = { { "htmlIncludeOrString", "!csCompWizResourceHTMLIncludeDesc", "resource", "htm" }, { "dynResTable", "!csCompWizResourceDynTableDesc", "resource", "hda" }, { "staticResTable", "!csCompWizResourceStaticTableDesc", "resource", "htm" }, { "query", "!csCompWizQueryDesc", "query", "htm" }, { "service", "!csCompWizServiceDesc", "service", "htm" }, { "template", "!csCompWizTemplateDesc", "template", "hda" }, { "environment", "!csCompWizEnvDesc", "environment", "cfg" } };
/*      */   public static final String RES_DEF_TABLE_NAME = "ResourceDefinition";
/*      */   public static final String MERGE_RULE_TABLE_NAME = "MergeRules";
/*      */   public static final String CLASS_TABLE_NAME = "ClassAliases";
/*      */   public static final String FILTERS_TABLE_NAME = "Filters";
/*   75 */   public static final String[] RES_DEF_FIELD_INFO = { "type", "filename", "tables", "loadOrder" };
/*   76 */   public static final String[] MERGE_RULE_FIELD_INFO = { "fromTable", "toTable", "column", "loadOrder" };
/*   77 */   public static final String[] CLASS_FIELD_INFO = { "classname", "location", "loadOrder" };
/*   78 */   public static final String[] FILTERS_FIELD_INFO = { "type", "location", "parameter", "loadOrder" };
/*      */   protected Vector m_fileInfo;
/*      */   protected Hashtable m_tables;
/*      */   protected String m_name;
/*      */   protected String m_filename;
/*      */   protected String m_absCompDir;
/*      */   protected String m_absLocation;
/*      */   protected long m_lastModified;
/*      */   public DataBinder m_binder;
/*      */   protected String m_errorMsg;
/*      */   protected Workspace m_workspace;
/*      */   public ComponentPreferenceData m_prefData;
/*      */ 
/*      */   public IntradocComponent()
/*      */   {
/*   81 */     this.m_fileInfo = null;
/*   82 */     this.m_tables = null;
/*      */ 
/*   85 */     this.m_name = null;
/*   86 */     this.m_filename = null;
/*   87 */     this.m_absCompDir = null;
/*   88 */     this.m_absLocation = null;
/*   89 */     this.m_lastModified = -1L;
/*      */ 
/*   91 */     this.m_binder = null;
/*   92 */     this.m_errorMsg = null;
/*   93 */     this.m_workspace = null;
/*      */ 
/*   95 */     this.m_prefData = null;
/*      */   }
/*      */ 
/*      */   public void init(String name, Map<String, String> map, Map<String, String> args, boolean isEdit) throws ServiceException
/*      */   {
/*  100 */     this.m_name = name;
/*      */ 
/*  103 */     this.m_absLocation = ((String)args.get("absPath"));
/*  104 */     if (this.m_absLocation == null)
/*      */     {
/*  106 */       this.m_absLocation = ComponentLocationUtils.determineComponentLocation(map, 1);
/*      */     }
/*      */ 
/*  109 */     this.m_absCompDir = FileUtils.directorySlashes(FileUtils.getDirectory(this.m_absLocation));
/*      */ 
/*  112 */     String location = (String)map.get("location");
/*  113 */     if (location == null)
/*      */     {
/*  115 */       location = this.m_absLocation;
/*      */     }
/*  117 */     location = FileUtils.fileSlashes(location);
/*  118 */     this.m_filename = FileUtils.getName(location);
/*      */ 
/*  120 */     boolean isNew = StringUtils.convertToBool((String)args.get("isNew"), false);
/*  121 */     if (isNew)
/*      */     {
/*  123 */       this.m_binder = new DataBinder();
/*  124 */       setTables();
/*  125 */       this.m_lastModified = CWizardUtils.writeFile(this.m_absCompDir, this.m_filename, this.m_binder);
/*      */     }
/*      */     else
/*      */     {
/*  129 */       String path = this.m_absLocation;
/*  130 */       int retVal = FileUtils.checkFile(path, true, false);
/*  131 */       if ((retVal == -16) && (!isEdit) && (!ComponentLocationUtils.isHomeLocal(null)))
/*      */       {
/*  136 */         path = ComponentLocationUtils.determineComponentLocation(map, 2);
/*      */ 
/*  138 */         retVal = FileUtils.checkFile(path, true, false);
/*      */       }
/*  140 */       if (retVal == -16)
/*      */       {
/*  142 */         String errMsg = LocaleUtils.appendMessage("!csCompWizCompDefFileError", FileUtils.getErrorMsg(path, true, retVal));
/*      */ 
/*  144 */         throw new ServiceException(retVal, errMsg);
/*      */       }
/*      */ 
/*  148 */       this.m_absCompDir = FileUtils.directorySlashes(FileUtils.getDirectory(path));
/*      */ 
/*  150 */       this.m_binder = CWizardUtils.readFile(this.m_absCompDir, this.m_filename, this.m_absCompDir);
/*      */ 
/*  152 */       if ((this.m_binder.getResultSet("ResourceDefinition") == null) && (this.m_binder.getResultSet("ClassAliases") == null) && (this.m_binder.getResultSet("Filters") == null))
/*      */       {
/*  156 */         throw new ServiceException(LocaleUtils.encodeMessage("csCompWizFileContainsInvalidDefTable", null, location));
/*      */       }
/*      */ 
/*  160 */       setTables();
/*  161 */       this.m_lastModified = CWizardUtils.getLastModified(this.m_absCompDir + this.m_filename);
/*      */     }
/*      */ 
/*  165 */     loadPreferenceData();
/*      */   }
/*      */ 
/*      */   public boolean cacheFileInfo()
/*      */     throws ServiceException, DataException
/*      */   {
/*  171 */     this.m_fileInfo = new IdcVector();
/*  172 */     DataResultSet resDef = getResourceDefTable();
/*  173 */     boolean isError = false;
/*      */ 
/*  176 */     String[] keys = { "type", "filename", "tables", "loadOrder" };
/*  177 */     String[][] table = ResultSetUtils.createStringTable(resDef, keys);
/*      */ 
/*  179 */     for (int i = 0; i < table.length; ++i)
/*      */     {
/*  181 */       String type = table[i][0];
/*  182 */       String filename = table[i][1];
/*  183 */       String tables = table[i][2];
/*  184 */       String loadOrder = table[i][3];
/*      */ 
/*  186 */       filename = FileUtils.getAbsolutePath(this.m_absCompDir, filename);
/*  187 */       ResourceFileInfo fileInfo = new ResourceFileInfo(this.m_name, this.m_absCompDir, type, filename, tables, loadOrder);
/*      */       try
/*      */       {
/*  192 */         fileInfo.load();
/*      */ 
/*  195 */         validateTableFormat(fileInfo, type, filename);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  199 */         fileInfo.m_errMsg = e.getMessage();
/*  200 */         fileInfo.m_errMsg += "!csLinefeed";
/*      */       }
/*      */       finally
/*      */       {
/*  204 */         this.m_fileInfo.addElement(fileInfo);
/*  205 */         if (fileInfo.m_errMsg != null)
/*      */         {
/*  207 */           isError = true;
/*  208 */           this.m_errorMsg = fileInfo.m_errMsg;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  215 */     cleanupTables();
/*      */ 
/*  219 */     DataResultSet mergeRules = getMergeRulesTable();
/*  220 */     for (mergeRules.first(); mergeRules.isRowPresent(); mergeRules.next())
/*      */     {
/*  222 */       String fromTable = mergeRules.getStringValue(0);
/*  223 */       if ((fromTable == null) || (fromTable.length() <= 0))
/*      */         continue;
/*  225 */       boolean isUnique = isNameUnique(fromTable, true);
/*      */ 
/*  227 */       if (!isUnique)
/*      */         continue;
/*  229 */       this.m_errorMsg = LocaleUtils.encodeMessage("csCompWizMergeTableNotFoundError", this.m_errorMsg, fromTable);
/*  230 */       isError = true;
/*      */     }
/*      */ 
/*  236 */     DataResultSet filtersTable = getFiltersTable();
/*      */ 
/*  238 */     if (filtersTable.getNumFields() < 4)
/*      */     {
/*  240 */       Vector infos = ResultSetUtils.createFieldInfo(new String[] { "parameter" }, 50);
/*  241 */       filtersTable.mergeFieldsWithFlags(infos, 0);
/*  242 */       this.m_tables.put("Filters", filtersTable);
/*      */     }
/*      */ 
/*  245 */     return isError;
/*      */   }
/*      */ 
/*      */   public void cleanupTables()
/*      */   {
/*  250 */     int size = this.m_fileInfo.size();
/*      */     ResourceFileInfo fileInfo;
/*      */     Enumeration en;
/*  251 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  253 */       fileInfo = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*  254 */       if ((!fileInfo.m_type.equals("resource")) || 
/*  256 */         (fileInfo.m_tables.isEmpty()))
/*      */         continue;
/*  258 */       Hashtable infoTables = (Hashtable)fileInfo.m_tables.clone();
/*  259 */       for (en = infoTables.keys(); en.hasMoreElements(); )
/*      */       {
/*  261 */         String table = (String)en.nextElement();
/*  262 */         boolean isFound = isQueryOrServiceTable(table);
/*  263 */         if (isFound)
/*      */         {
/*  265 */           fileInfo.m_tables.remove(table);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isQueryOrServiceTable(String table)
/*      */   {
/*  275 */     boolean result = false;
/*  276 */     int size = this.m_fileInfo.size();
/*  277 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  279 */       ResourceFileInfo fileInfo = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*  280 */       if ((!fileInfo.m_type.equals("service")) && (!fileInfo.m_type.equals("query")))
/*      */         continue;
/*  282 */       result = fileInfo.m_tables.get(table) != null;
/*  283 */       if (result) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  289 */     return result;
/*      */   }
/*      */ 
/*      */   public String createReadmeFile() throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  296 */       String readmePath = this.m_absCompDir + "readme.txt";
/*  297 */       if (FileUtils.checkFile(readmePath, true, false) < 0)
/*      */       {
/*  299 */         File file = new File(readmePath);
/*  300 */         BufferedWriter writer = FileUtils.openDataWriter(file);
/*  301 */         DataBinder data = new DataBinder();
/*  302 */         data.putLocal("ComponentName", this.m_name);
/*      */ 
/*  304 */         writer.write(CWizardUtils.retrieveDynamicHtml(data, "readme_info"));
/*  305 */         writer.close();
/*      */       }
/*      */ 
/*  308 */       return readmePath;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  312 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizCreateReadmeError", e.getMessage()));
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean createInstallStringFile(String filePath) throws ServiceException
/*      */   {
/*  318 */     boolean isCreated = false;
/*      */     try
/*      */     {
/*  321 */       if (FileUtils.checkFile(filePath, true, false) < 0)
/*      */       {
/*  323 */         isCreated = true;
/*  324 */         DataBinder params = new DataBinder(SharedObjects.getSecureEnvironment());
/*  325 */         CWizardUtils.writeTemplateFile(params, "INSTALL_STRINGS_TEMPLATE", null, filePath);
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  331 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizCreateInstallStringsError", e.getMessage(), filePath));
/*      */     }
/*      */ 
/*  334 */     return isCreated;
/*      */   }
/*      */ 
/*      */   public void loadPreferenceData()
/*      */   {
/*  345 */     String dataPath = null;
/*  346 */     if (DataBinderUtils.getBoolean(this.m_binder, "hasPreferenceData", false))
/*      */     {
/*  348 */       dataPath = CompInstallUtils.getInstallConfPath(this.m_binder.getLocal("installID"), this.m_binder.getLocal("ComponentName"));
/*      */     }
/*      */ 
/*  352 */     this.m_prefData = new ComponentPreferenceData(this.m_absCompDir, dataPath);
/*      */     try
/*      */     {
/*  356 */       this.m_prefData.load();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  360 */       Report.trace("componentwizard", LocaleUtils.encodeMessage("csCompWizLoadPreferenceData", null), e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean reloadFileInfo()
/*      */   {
/*  367 */     boolean isError = false;
/*      */ 
/*  369 */     for (int i = 0; i < this.m_fileInfo.size(); ++i)
/*      */     {
/*  371 */       ResourceFileInfo info = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*  372 */       if (!CWizardUtils.isTimeStampChanged(info.m_filename, info.m_lastModified))
/*      */         continue;
/*  374 */       info = new ResourceFileInfo(this.m_name, this.m_absCompDir, info.m_type, info.m_filename, info.m_tableStr, info.m_loadOrder);
/*      */       try
/*      */       {
/*  378 */         info.load();
/*      */ 
/*  380 */         validateTableFormat(info, info.m_type, info.m_filename);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  384 */         isError = true;
/*  385 */         info.m_errMsg = e.getMessage();
/*  386 */         info.m_lastModified = CWizardUtils.getLastModified(info.m_filename);
/*      */       }
/*      */       finally
/*      */       {
/*  390 */         this.m_fileInfo.setElementAt(info, i);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  395 */     cleanupTables();
/*  396 */     return isError;
/*      */   }
/*      */ 
/*      */   public String getMergeTableListName(String cwType)
/*      */     throws ServiceException
/*      */   {
/*  402 */     String mergetable = null;
/*      */ 
/*  404 */     if (cwType.equals("template"))
/*      */     {
/*  406 */       mergetable = "TemplateMergeTables";
/*      */     }
/*  408 */     else if (cwType.equals("dynResTable"))
/*      */     {
/*  410 */       mergetable = "DynamicResourceMergeTables";
/*      */     }
/*  412 */     else if (cwType.equals("staticResTable"))
/*      */     {
/*  414 */       mergetable = "StaticResourceMergeTables";
/*      */     }
/*      */     else
/*      */     {
/*  418 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizMergeTableNotDefined", null, cwType));
/*      */     }
/*      */ 
/*  421 */     return mergetable;
/*      */   }
/*      */ 
/*      */   protected void validateTableFormat(ResourceFileInfo fileInfo, String type, String filename)
/*      */   {
/*  426 */     DataResultSet mergeRules = getMergeRulesTable();
/*  427 */     Hashtable tables = (Hashtable)fileInfo.m_tables.clone();
/*  428 */     for (Enumeration en = tables.keys(); en.hasMoreElements(); )
/*      */     {
/*  430 */       String tablename = (String)en.nextElement();
/*  431 */       DataResultSet table = (DataResultSet)fileInfo.m_tables.get(tablename);
/*  432 */       String errMsg = null;
/*  433 */       if (type.equals("query"))
/*      */       {
/*  435 */         errMsg = verifyColumns(filename, tablename, table, new String[] { "name", "queryStr", "parameters" }, false);
/*      */       }
/*  438 */       else if (type.equals("service"))
/*      */       {
/*  440 */         errMsg = verifyColumns(filename, tablename, table, new String[] { "Name", "Attributes", "Actions" }, false);
/*      */       }
/*  443 */       else if ((type.equals("template")) || (type.equals("resource")))
/*      */       {
/*  445 */         Vector v = mergeRules.findRow(0, tablename);
/*  446 */         if (v == null)
/*      */         {
/*  448 */           if (type.equals("template"))
/*      */           {
/*  450 */             String location = FileUtils.directorySlashes(this.m_absCompDir) + this.m_filename;
/*  451 */             errMsg = LocaleUtils.encodeMessage("csCompWizMergeRulesTemplateNotDefined", null, tablename, location);
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/*  457 */           Vector mv = mergeRules.findRow(0, tablename);
/*  458 */           String toTable = (String)mv.elementAt(1);
/*  459 */           Vector mergeCols = null;
/*  460 */           String mergeTablesName = null;
/*      */ 
/*  462 */           if (type.equals("resource"))
/*      */           {
/*  464 */             if (filename.endsWith(".hda"))
/*      */             {
/*  466 */               mergeTablesName = "DynamicResourceMergeTables";
/*      */             }
/*      */             else
/*      */             {
/*  470 */               mergeTablesName = "StaticResourceMergeTables";
/*      */             }
/*      */ 
/*      */           }
/*      */           else {
/*  475 */             mergeTablesName = "TemplateMergeTables";
/*      */           }
/*      */ 
/*  478 */           DataResultSet mergeTables = SharedObjects.getTable(mergeTablesName);
/*  479 */           if (mergeTables == null)
/*      */           {
/*  481 */             errMsg = "!csCompWizMergeTablesInfoMissing";
/*      */           }
/*      */           else
/*      */           {
/*  485 */             Vector mvTableInfo = mergeTables.findRow(0, toTable);
/*  486 */             if (mvTableInfo != null)
/*      */             {
/*  488 */               String tempCols = (String)mvTableInfo.elementAt(1);
/*  489 */               mergeCols = StringUtils.parseArray(tempCols, ',', '^');
/*  490 */               String[] infoFields = new String[mergeCols.size()];
/*  491 */               for (int i = 0; i < infoFields.length; ++i)
/*      */               {
/*  493 */                 infoFields[i] = ((String)mergeCols.elementAt(i));
/*      */               }
/*  495 */               errMsg = verifyColumns(filename, tablename, table, infoFields, true);
/*      */             }
/*      */           }
/*      */         }
/*      */       }
/*      */ 
/*  501 */       if (errMsg != null)
/*      */       {
/*  503 */         fileInfo.m_tables.remove(tablename);
/*  504 */         if (fileInfo.m_errMsg == null)
/*      */         {
/*  506 */           fileInfo.m_errMsg = errMsg;
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String verifyColumns(String filename, String tablename, DataResultSet drset, String[] fieldInfo, boolean isTolerant)
/*      */   {
/*  515 */     String errMsg = null;
/*      */     try
/*      */     {
/*  518 */       ResultSetUtils.createInfoList(drset, fieldInfo, true);
/*      */     }
/*      */     catch (DataException de)
/*      */     {
/*  522 */       if (!isTolerant)
/*      */       {
/*  524 */         errMsg = LocaleUtils.appendMessage(de.getMessage(), LocaleUtils.encodeMessage("csCompWizMergeTableIncorrectlyDefined", null, tablename, filename, this.m_name));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  530 */     return errMsg;
/*      */   }
/*      */ 
/*      */   public ResourceFileInfo retrieveResourceFileInfo(Properties props)
/*      */   {
/*  535 */     if ((this.m_fileInfo == null) || (this.m_fileInfo.size() == 0))
/*      */     {
/*  537 */       return null;
/*      */     }
/*      */ 
/*  540 */     String type = props.getProperty("type");
/*  541 */     String filename = props.getProperty("filename");
/*  542 */     String tables = props.getProperty("tables");
/*      */ 
/*  544 */     return retrieveResourceFileInfo(type, filename, tables, true);
/*      */   }
/*      */ 
/*      */   public ResourceFileInfo retrieveResourceFileInfo(String type, String filename, String tables, boolean checkTables)
/*      */   {
/*  551 */     ResourceFileInfo fileInfo = null;
/*  552 */     filename = FileUtils.getAbsolutePath(this.m_absCompDir, filename);
/*      */ 
/*  554 */     if ((type == null) || (type.length() == 0) || (filename == null) || (filename.length() == 0))
/*      */     {
/*  556 */       return null;
/*      */     }
/*      */ 
/*  560 */     boolean found = false;
/*  561 */     for (int i = 0; i < this.m_fileInfo.size(); ++i)
/*      */     {
/*  563 */       fileInfo = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*      */ 
/*  565 */       if ((!type.equals(fileInfo.m_type)) || (!filename.equals(fileInfo.m_filename))) {
/*      */         continue;
/*      */       }
/*  568 */       if ((checkTables) && (tables != null) && (tables.length() > 0) && (!tables.equalsIgnoreCase("null")))
/*      */       {
/*  571 */         checkForTableListMismatch(fileInfo, tables);
/*      */       }
/*      */ 
/*  574 */       found = true;
/*  575 */       break;
/*      */     }
/*      */ 
/*  579 */     if (!found)
/*      */     {
/*  581 */       fileInfo = null;
/*      */     }
/*      */ 
/*  584 */     return fileInfo;
/*      */   }
/*      */ 
/*      */   public void checkForTableListMismatch(ResourceFileInfo fileInfo, String tables)
/*      */   {
/*  589 */     Vector fileTables = StringUtils.parseArray(fileInfo.m_tableStr, ',', '^');
/*  590 */     Vector resListTables = StringUtils.parseArray(tables, ',', '^');
/*  591 */     Vector unReferencedTables = new IdcVector();
/*      */ 
/*  593 */     for (int i = 0; i < fileTables.size(); ++i)
/*      */     {
/*  595 */       String fileTable = (String)fileTables.elementAt(i);
/*  596 */       int matchIndex = -1;
/*  597 */       for (int j = 0; j < resListTables.size(); ++j)
/*      */       {
/*  599 */         String resListTable = (String)resListTables.elementAt(j);
/*  600 */         if (!fileTable.equals(resListTable))
/*      */           continue;
/*  602 */         matchIndex = j;
/*      */       }
/*      */ 
/*  606 */       if (matchIndex < 0)
/*      */       {
/*  608 */         unReferencedTables.addElement(fileTable);
/*      */       }
/*      */       else
/*      */       {
/*  612 */         resListTables.removeElementAt(matchIndex);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  617 */     String errMsg = "";
/*  618 */     if (unReferencedTables.size() > 0)
/*      */     {
/*  620 */       errMsg = "!csCompWizUnreferencedTableList";
/*  621 */       errMsg = errMsg + createIndentedDisplayList(unReferencedTables);
/*      */     }
/*  623 */     if (resListTables.size() > 0)
/*      */     {
/*  625 */       errMsg = errMsg + LocaleUtils.encodeMessage("csCompWizTableResourcesMissingTables", null, fileInfo.m_filename);
/*      */ 
/*  627 */       errMsg = errMsg + createIndentedDisplayList(resListTables);
/*      */     }
/*  629 */     if (errMsg.length() <= 0)
/*      */       return;
/*  631 */     fileInfo.m_errMsg = errMsg;
/*      */   }
/*      */ 
/*      */   public String createIndentedDisplayList(Vector strs)
/*      */   {
/*  637 */     String dispStr = "";
/*  638 */     for (int i = 0; i < strs.size(); ++i)
/*      */     {
/*  640 */       dispStr = dispStr + "--> ";
/*  641 */       dispStr = dispStr + (String)strs.elementAt(i);
/*  642 */       dispStr = dispStr + "\n";
/*      */     }
/*      */ 
/*  645 */     return dispStr;
/*      */   }
/*      */ 
/*      */   public void createOrEditResourceFileInfo(int editType, Properties props, ResourceFileInfo fileInfo)
/*      */     throws ServiceException
/*      */   {
/*  652 */     String cwType = props.getProperty("type");
/*      */ 
/*  655 */     String type = StringUtils.findString(RES_DEF, cwType, 0, 2);
/*  656 */     props.put("type", type);
/*      */ 
/*  659 */     String tablename = props.getProperty("tablename");
/*  660 */     String mergetable = props.getProperty("mergeTable");
/*  661 */     String mergecols = props.getProperty("mergeTableColumns");
/*  662 */     String mergeColumn = props.getProperty("mergeColumn");
/*  663 */     String loadOrder = props.getProperty("loadOrder");
/*      */ 
/*  665 */     Vector cols = null;
/*  666 */     String tables = "null";
/*      */     try
/*      */     {
/*  671 */       boolean isFileInfoCreated = false;
/*  672 */       if ((mergecols != null) && (mergecols.length() > 0))
/*      */       {
/*  674 */         cols = StringUtils.parseArray(mergecols, ',', '^');
/*      */       }
/*      */ 
/*  677 */       String filename = props.getProperty("filename");
/*  678 */       String loadorder = props.getProperty("loadOrder");
/*  679 */       filename = FileUtils.getAbsolutePath(this.m_absCompDir, filename);
/*      */ 
/*  681 */       if ((editType == 1) && (fileInfo == null))
/*      */       {
/*  683 */         fileInfo = retrieveResourceFileInfo(type, filename, null, false);
/*      */       }
/*      */ 
/*  686 */       if (fileInfo == null)
/*      */       {
/*  688 */         if ((((cwType.equals("query")) || (cwType.equals("service")))) && 
/*  690 */           (tablename != null) && (tablename.length() > 0))
/*      */         {
/*  692 */           tables = tablename;
/*      */         }
/*      */ 
/*  695 */         fileInfo = new ResourceFileInfo(this.m_name, this.m_absCompDir, type, filename, tables, loadorder);
/*      */ 
/*  697 */         props.put("tables", tables);
/*  698 */         isFileInfoCreated = true;
/*      */       }
/*      */ 
/*  702 */       if ((!isFileInfoCreated) && (editType == 1))
/*      */       {
/*  704 */         String name = null;
/*  705 */         if (cwType.equals("htmlIncludeOrString"))
/*      */         {
/*  707 */           name = props.getProperty("includeOrString");
/*  708 */           Object o = fileInfo.m_htmlIncludes.get(name);
/*  709 */           if (o == null)
/*      */           {
/*  711 */             o = fileInfo.m_dataIncludes.get(name);
/*      */           }
/*  713 */           if (o != null)
/*      */           {
/*  715 */             throw new ServiceException(LocaleUtils.encodeMessage("csCompWizEntryNotUnique", null, name));
/*      */           }
/*      */ 
/*      */         }
/*  721 */         else if ((cols != null) && (cols.size() > 0))
/*      */         {
/*  723 */           name = props.getProperty((String)cols.elementAt(0));
/*  724 */           DataResultSet tempset = (DataResultSet)fileInfo.m_tables.get(tablename);
/*  725 */           if ((tempset != null) && (tempset.findRow(0, name) != null))
/*      */           {
/*  727 */             throw new ServiceException(LocaleUtils.encodeMessage("csCompWizEntryNotUnique", null, name));
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  735 */       if ((!isFileInfoCreated) && (((cwType.equals("query")) || (cwType.equals("service")))) && 
/*  738 */         (fileInfo.m_loadTables != null))
/*      */       {
/*  740 */         boolean entryExist = false;
/*      */ 
/*  742 */         for (int i = 0; i < fileInfo.m_loadTables.size(); ++i)
/*      */         {
/*  744 */           String tempTable = (String)fileInfo.m_loadTables.elementAt(i);
/*      */ 
/*  746 */           if ((tempTable == null) || (!tempTable.equals(tablename)))
/*      */             continue;
/*  748 */           entryExist = true;
/*  749 */           break;
/*      */         }
/*      */ 
/*  754 */         if (!entryExist)
/*      */         {
/*  756 */           fileInfo.m_loadTables.addElement(tablename);
/*  757 */           fileInfo.m_tableStr = StringUtils.createString(fileInfo.m_loadTables, ',', '^');
/*  758 */           tables = fileInfo.m_tableStr;
/*  759 */           props.put("tables", tables);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  765 */       DataResultSet drset = getResourceDefTable();
/*  766 */       Vector row = null;
/*      */ 
/*  768 */       if (isFileInfoCreated)
/*      */       {
/*  770 */         PropParameters params = new PropParameters(props);
/*  771 */         Vector nRow = drset.createRow(params);
/*  772 */         drset.addRow(nRow);
/*      */       }
/*      */       else
/*      */       {
/*  776 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*      */         {
/*  778 */           String tempType = ResultSetUtils.getValue(drset, "type");
/*  779 */           String tempFilename = FileUtils.getAbsolutePath(this.m_absCompDir, ResultSetUtils.getValue(drset, "filename"));
/*      */ 
/*  781 */           if ((!tempType.equals(type)) || (!tempFilename.equals(filename)))
/*      */             continue;
/*  783 */           row = drset.getCurrentRowValues();
/*  784 */           if (row == null)
/*      */             continue;
/*  786 */           if ((tables != null) && (!tables.equals("null")))
/*      */           {
/*  788 */             row.setElementAt(tables, 2);
/*      */           }
/*      */ 
/*  791 */           if (loadOrder == null)
/*      */             continue;
/*  793 */           row.setElementAt(loadOrder, 3);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  800 */       this.m_binder.addResultSet("ResourceDefinition", drset);
/*  801 */       this.m_tables.put("ResourceDefinition", drset);
/*      */ 
/*  805 */       if ((((cwType.equals("template")) || (cwType.equals("dynResTable")) || (cwType.equals("staticResTable")))) && 
/*  809 */         (mergetable != null) && (mergetable.length() > 0))
/*      */       {
/*  811 */         DataResultSet mergeRules = getMergeRulesTable();
/*  812 */         FieldInfo info = new FieldInfo();
/*  813 */         int orderIndex = -1;
/*  814 */         if (mergeRules.getFieldInfo("loadOrder", info))
/*      */         {
/*  816 */           orderIndex = info.m_index;
/*      */         }
/*      */ 
/*  820 */         Vector v = mergeRules.findRow(0, tablename);
/*      */ 
/*  822 */         if (v == null)
/*      */         {
/*  824 */           Vector newRow = mergeRules.createEmptyRow();
/*  825 */           newRow.setElementAt(tablename, 0);
/*  826 */           newRow.setElementAt(mergetable, 1);
/*      */ 
/*  828 */           if ((mergeColumn == null) || (mergeColumn.length() == 0))
/*      */           {
/*  830 */             if ((cols != null) && (cols.size() > 0))
/*      */             {
/*  832 */               cols = StringUtils.parseArray(mergecols, ',', '^');
/*  833 */               newRow.setElementAt(cols.elementAt(0), 2);
/*      */             }
/*      */             else
/*      */             {
/*  837 */               newRow.setElementAt("", 2);
/*      */             }
/*      */ 
/*      */           }
/*      */           else {
/*  842 */             newRow.setElementAt(mergeColumn, 2);
/*      */           }
/*      */ 
/*  845 */           if (orderIndex > -1)
/*      */           {
/*  847 */             if ((loadOrder == null) || (loadOrder.length() == 0))
/*      */             {
/*  849 */               loadOrder = "1";
/*      */             }
/*  851 */             newRow.setElementAt(loadOrder, 3);
/*      */           }
/*      */ 
/*  854 */           mergeRules.addRow(newRow);
/*  855 */           this.m_binder.addResultSet("MergeRules", mergeRules);
/*  856 */           this.m_tables.put("MergeRules", mergeRules);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  861 */       fileInfo.createOrEditResource(editType, cwType, props);
/*      */ 
/*  864 */       validateTableFormat(fileInfo, fileInfo.m_type, fileInfo.m_filename);
/*      */ 
/*  867 */       if (isFileInfoCreated)
/*      */       {
/*  869 */         if (fileInfo.m_errMsg != null)
/*      */         {
/*  871 */           throw new ServiceException(fileInfo.m_errMsg);
/*      */         }
/*      */ 
/*  874 */         this.m_fileInfo.addElement(fileInfo);
/*      */       }
/*      */ 
/*  878 */       this.m_lastModified = CWizardUtils.writeFile(this.m_absCompDir, this.m_filename, this.m_binder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  882 */       props.put("type", cwType);
/*  883 */       String msg = "!csCompWizAddResourceError";
/*  884 */       if (editType == 3)
/*      */       {
/*  886 */         msg = "!csCompWizDeleteResourceError";
/*      */       }
/*  888 */       else if (editType == 2)
/*      */       {
/*  890 */         msg = "!csCompWizEditResourceError";
/*      */       }
/*  892 */       undoChanges(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void removeResourceFileInfo(Properties props) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  900 */       String type = props.getProperty("type");
/*  901 */       String filename = props.getProperty("filename");
/*  902 */       String tables = props.getProperty("tables");
/*      */ 
/*  904 */       boolean found = false;
/*  905 */       DataResultSet drset = getResourceDefTable();
/*  906 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, new String[] { "type", "filename", "tables" }, true);
/*      */ 
/*  909 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  911 */         String tempType = drset.getStringValue(fi[0].m_index);
/*  912 */         String tempFilename = drset.getStringValue(fi[1].m_index);
/*  913 */         String tempTables = drset.getStringValue(fi[2].m_index);
/*      */ 
/*  915 */         if ((!tempType.equals(type)) || (!tempFilename.equals(filename)) || ((tables != null) && (!tables.equalsIgnoreCase("null")) && (!tables.equals(tempTables)))) {
/*      */           continue;
/*      */         }
/*      */ 
/*  919 */         int index = drset.getCurrentRow();
/*  920 */         drset.deleteRow(index);
/*  921 */         this.m_binder.addResultSet("ResourceDefinition", drset);
/*  922 */         found = true;
/*  923 */         break;
/*      */       }
/*      */ 
/*  927 */       if (!found)
/*      */       {
/*  929 */         return;
/*      */       }
/*      */ 
/*  933 */       ResourceFileInfo finfo = retrieveResourceFileInfo(props);
/*      */ 
/*  936 */       if ((finfo != null) && (!finfo.m_tables.isEmpty()))
/*      */       {
/*  938 */         DataResultSet mergeRules = getMergeRulesTable();
/*      */ 
/*  941 */         for (Enumeration en = finfo.m_tables.keys(); en.hasMoreElements(); )
/*      */         {
/*  943 */           String tablename = (String)en.nextElement();
/*  944 */           Vector values = mergeRules.findRow(0, tablename);
/*  945 */           if (values != null)
/*      */           {
/*  947 */             int index = mergeRules.getCurrentRow();
/*  948 */             mergeRules.deleteRow(index);
/*      */           }
/*      */         }
/*  951 */         this.m_binder.addResultSet("MergeRules", mergeRules);
/*  952 */         this.m_tables.put("MergeRules", mergeRules);
/*      */       }
/*      */ 
/*  955 */       if (finfo != null)
/*      */       {
/*  957 */         this.m_fileInfo.removeElement(finfo);
/*      */       }
/*      */ 
/*  960 */       this.m_lastModified = CWizardUtils.writeFile(this.m_absCompDir, this.m_filename, this.m_binder);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  965 */       undoChanges("!csCompWizRemoveResourceError", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean addJavaCode(Properties props, boolean isFilter, boolean isInstall, boolean createFile) throws ServiceException
/*      */   {
/*  971 */     DataResultSet drset = null;
/*  972 */     String location = props.getProperty("location");
/*  973 */     String filename = props.getProperty("filename");
/*  974 */     String classname = null;
/*  975 */     boolean isCreated = false;
/*  976 */     String[] fields = null;
/*  977 */     String typeOrClassName = null;
/*      */ 
/*  979 */     if (isFilter)
/*      */     {
/*  981 */       typeOrClassName = "type";
/*  982 */       drset = getFiltersTable();
/*  983 */       fields = new String[] { "type", "location" };
/*      */     }
/*      */     else
/*      */     {
/*  987 */       typeOrClassName = "classname";
/*  988 */       classname = props.getProperty("classname");
/*  989 */       drset = getClassAliasesTable();
/*  990 */       fields = new String[] { "classname", "location" };
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  996 */       String newName = props.getProperty(typeOrClassName);
/*  997 */       String newLocaton = props.getProperty("location");
/*  998 */       boolean isUnique = true;
/*      */ 
/* 1000 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, fields, true);
/* 1001 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1003 */         String name = drset.getStringValue(fi[0].m_index);
/* 1004 */         String loc = drset.getStringValue(fi[1].m_index);
/* 1005 */         if ((!newName.equals(name)) || (!newLocaton.equals(loc)))
/*      */           continue;
/* 1007 */         isUnique = false;
/* 1008 */         break;
/*      */       }
/*      */ 
/* 1012 */       if (isUnique)
/*      */       {
/* 1014 */         PropParameters params = new PropParameters(props);
/* 1015 */         Vector nRow = drset.createRow(params);
/* 1016 */         drset.addRow(nRow);
/*      */ 
/* 1018 */         if ((createFile) && 
/* 1020 */           (FileUtils.checkFile(filename, true, false) < 0))
/*      */         {
/* 1022 */           createClassFile(filename, location, classname, isFilter, isInstall);
/* 1023 */           isCreated = true;
/*      */         }
/*      */ 
/* 1028 */         saveJavaCodeChanges(isFilter, drset);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1033 */       String errMsg = LocaleUtils.encodeMessage("csCompWizAddClassAliasError", null, filename);
/* 1034 */       if (isFilter)
/*      */       {
/* 1036 */         errMsg = LocaleUtils.encodeMessage("csCompWizRemoveFilterError", null, filename);
/*      */       }
/*      */ 
/* 1039 */       undoChanges(errMsg, e);
/*      */     }
/*      */ 
/* 1042 */     return isCreated;
/*      */   }
/*      */ 
/*      */   public void deleteJavaCode(Properties props, boolean isFilter) throws ServiceException
/*      */   {
/* 1047 */     String typeOrClassName = null;
/* 1048 */     DataResultSet drset = null;
/* 1049 */     String[] fields = null;
/*      */ 
/* 1051 */     if (isFilter)
/*      */     {
/* 1053 */       typeOrClassName = props.getProperty("type");
/* 1054 */       drset = getFiltersTable();
/* 1055 */       fields = new String[] { "type", "location" };
/*      */     }
/*      */     else
/*      */     {
/* 1059 */       typeOrClassName = props.getProperty("classname");
/* 1060 */       drset = getClassAliasesTable();
/* 1061 */       fields = new String[] { "classname", "location" };
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1066 */       String newName = typeOrClassName;
/* 1067 */       String newLoc = props.getProperty("location");
/*      */ 
/* 1069 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, fields, true);
/* 1070 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/* 1072 */         String name = drset.getStringValue(fi[0].m_index);
/* 1073 */         String loc = drset.getStringValue(fi[1].m_index);
/* 1074 */         if ((!newName.equals(name)) || (!newLoc.equals(loc)))
/*      */           continue;
/* 1076 */         int index = drset.getCurrentRow();
/* 1077 */         drset.deleteRow(index);
/* 1078 */         break;
/*      */       }
/*      */ 
/* 1083 */       saveJavaCodeChanges(isFilter, drset);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1087 */       String errMsg = LocaleUtils.encodeMessage("csCompWizAddClassAliasError", null, typeOrClassName);
/* 1088 */       if (isFilter)
/*      */       {
/* 1090 */         errMsg = LocaleUtils.encodeMessage("csCompWizRemoveFilterError", null, typeOrClassName);
/*      */       }
/*      */ 
/* 1094 */       undoChanges(errMsg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isNameUnique(String name, boolean isTable)
/*      */   {
/* 1100 */     boolean isUnique = true;
/*      */ 
/* 1102 */     if ((this.m_fileInfo == null) || (this.m_fileInfo.size() == 0))
/*      */     {
/* 1104 */       return isUnique;
/*      */     }
/*      */ 
/* 1107 */     if (!isTable)
/*      */     {
/* 1109 */       name = FileUtils.getAbsolutePath(this.m_absCompDir, name);
/*      */     }
/* 1111 */     for (int i = 0; i < this.m_fileInfo.size(); ++i)
/*      */     {
/* 1113 */       ResourceFileInfo fileInfo = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*      */ 
/* 1116 */       if (isTable)
/*      */       {
/* 1118 */         if ((fileInfo.m_tables.isEmpty()) || 
/* 1120 */           (fileInfo.m_tables.get(name) == null))
/*      */           continue;
/* 1122 */         isUnique = false;
/* 1123 */         break;
/*      */       }
/*      */ 
/* 1129 */       if (!fileInfo.m_filename.equals(name))
/*      */         continue;
/* 1131 */       isUnique = false;
/* 1132 */       break;
/*      */     }
/*      */ 
/* 1137 */     return isUnique;
/*      */   }
/*      */ 
/*      */   public String retrieveDefaultTableName(String cwType, boolean isTargetTable)
/*      */   {
/* 1142 */     if (cwType == null)
/*      */     {
/* 1144 */       return null;
/*      */     }
/*      */ 
/* 1147 */     String tablename = CWizardUtils.removeSpaces(this.m_name);
/* 1148 */     tablename = tablename + "_";
/*      */ 
/* 1150 */     if (isTargetTable)
/*      */     {
/* 1152 */       tablename = tablename + cwType;
/*      */     }
/* 1156 */     else if (cwType.equals("query"))
/*      */     {
/* 1158 */       tablename = tablename + "Queries";
/*      */     }
/* 1160 */     else if (cwType.equals("template"))
/*      */     {
/* 1162 */       tablename = tablename + "Templates";
/*      */     }
/*      */     else
/*      */     {
/* 1166 */       tablename = tablename + "Services";
/*      */     }
/*      */ 
/* 1170 */     String orgTablename = tablename;
/* 1171 */     boolean isUnique = false;
/* 1172 */     int count = 1;
/*      */ 
/* 1174 */     while (!isUnique)
/*      */     {
/* 1176 */       if (isNameUnique(tablename, true))
/*      */       {
/* 1178 */         isUnique = true;
/*      */       }
/*      */ 
/* 1181 */       if (isUnique)
/*      */         continue;
/* 1183 */       tablename = orgTablename + ++count;
/*      */     }
/*      */ 
/* 1187 */     return tablename;
/*      */   }
/*      */ 
/*      */   public boolean isTypeDefined(String typeOrClassname, boolean isFilter)
/*      */   {
/* 1192 */     DataResultSet drset = null;
/*      */ 
/* 1194 */     if (isFilter)
/*      */     {
/* 1196 */       drset = getFiltersTable();
/*      */     }
/*      */     else
/*      */     {
/* 1200 */       drset = getClassAliasesTable();
/*      */     }
/*      */ 
/* 1203 */     Vector v = drset.findRow(0, typeOrClassname);
/*      */ 
/* 1206 */     return v != null;
/*      */   }
/*      */ 
/*      */   public Vector retreiveMergeTableInfo(String cwtype, String mergetable)
/*      */     throws ServiceException
/*      */   {
/* 1214 */     Vector v = null;
/* 1215 */     String mergeTableColumns = "";
/* 1216 */     String mergeColumn = "";
/*      */ 
/* 1218 */     if ((cwtype.equals("template")) || (cwtype.equals("staticResTable")) || (cwtype.equals("dynResTable")))
/*      */     {
/* 1221 */       if (mergetable != null)
/*      */       {
/* 1223 */         v = getTableInfo(cwtype, mergetable);
/* 1224 */         if ((v == null) || (v.size() == 0))
/*      */         {
/* 1226 */           v = new IdcVector();
/* 1227 */           Vector cols = retrieveColumnInfo(mergetable);
/* 1228 */           if (cols != null)
/*      */           {
/* 1230 */             mergeTableColumns = StringUtils.createString(cols, ',', '^');
/* 1231 */             mergeColumn = (String)cols.elementAt(0);
/* 1232 */             v.addElement(mergetable);
/* 1233 */             v.addElement(mergeTableColumns);
/* 1234 */             v.addElement(mergeColumn);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/* 1239 */     else if ((cwtype.equals("query")) || (cwtype.equals("service")))
/*      */     {
/* 1241 */       v = new IdcVector();
/* 1242 */       String tablename = "";
/*      */ 
/* 1244 */       if (cwtype.equals("service"))
/*      */       {
/* 1246 */         tablename = "StandardServices";
/* 1247 */         mergeTableColumns = "Name,Attributes,Actions";
/* 1248 */         mergeColumn = "Name";
/*      */       }
/*      */       else
/*      */       {
/* 1252 */         tablename = "QueryTable";
/* 1253 */         mergeTableColumns = "name,queryStr,parameters";
/* 1254 */         mergeColumn = "name";
/*      */       }
/* 1256 */       v.addElement(tablename);
/* 1257 */       v.addElement(mergeTableColumns);
/* 1258 */       v.addElement(mergeColumn);
/*      */     }
/*      */ 
/* 1261 */     return v;
/*      */   }
/*      */ 
/*      */   public DataResultSet getResourceDefTable()
/*      */   {
/* 1266 */     return (DataResultSet)this.m_tables.get("ResourceDefinition");
/*      */   }
/*      */ 
/*      */   public DataResultSet getMergeRulesTable()
/*      */   {
/* 1271 */     return (DataResultSet)this.m_tables.get("MergeRules");
/*      */   }
/*      */ 
/*      */   public DataResultSet getClassAliasesTable()
/*      */   {
/* 1276 */     return (DataResultSet)this.m_tables.get("ClassAliases");
/*      */   }
/*      */ 
/*      */   public DataResultSet getFiltersTable()
/*      */   {
/* 1281 */     return (DataResultSet)this.m_tables.get("Filters");
/*      */   }
/*      */ 
/*      */   public void updateResDefFile() throws ServiceException
/*      */   {
/* 1286 */     this.m_lastModified = CWizardUtils.writeFile(this.m_absCompDir, this.m_filename, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected Vector getTableInfo(String cwType, String mergeTable) throws ServiceException {
/* 1290 */     String mergetListTable = getMergeTableListName(cwType);
/* 1291 */     DataResultSet drset = SharedObjects.getTable(mergetListTable);
/*      */ 
/* 1293 */     if (drset == null)
/*      */     {
/* 1295 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizMergeTableNotLoaded", null, mergetListTable));
/*      */     }
/*      */ 
/* 1299 */     return drset.findRow(0, mergeTable);
/*      */   }
/*      */ 
/*      */   protected Vector retrieveColumnInfo(String tablename)
/*      */   {
/* 1304 */     Vector cols = null;
/*      */ 
/* 1306 */     if ((this.m_fileInfo == null) || (this.m_fileInfo.size() == 0))
/*      */     {
/* 1308 */       return cols;
/*      */     }
/*      */ 
/* 1311 */     for (int i = 0; i < this.m_fileInfo.size(); ++i)
/*      */     {
/* 1313 */       ResourceFileInfo fileInfo = (ResourceFileInfo)this.m_fileInfo.elementAt(i);
/*      */ 
/* 1316 */       if ((fileInfo.m_tables == null) || (fileInfo.m_tables.isEmpty()))
/*      */         continue;
/* 1318 */       DataResultSet drset = (DataResultSet)fileInfo.m_tables.get(tablename);
/* 1319 */       if (drset == null)
/*      */         continue;
/* 1321 */       int numFields = drset.getNumFields();
/* 1322 */       cols = new IdcVector();
/*      */ 
/* 1324 */       for (int j = 0; j < numFields; ++j)
/*      */       {
/* 1326 */         cols.addElement(drset.getFieldName(j));
/*      */       }
/* 1328 */       break;
/*      */     }
/*      */ 
/* 1333 */     return cols;
/*      */   }
/*      */ 
/*      */   protected void setTables() throws ServiceException
/*      */   {
/* 1338 */     this.m_tables = new Hashtable();
/*      */ 
/* 1340 */     setTable("ResourceDefinition", RES_DEF_FIELD_INFO);
/* 1341 */     setTable("MergeRules", MERGE_RULE_FIELD_INFO);
/* 1342 */     setTable("ClassAliases", CLASS_FIELD_INFO);
/* 1343 */     setTable("Filters", FILTERS_FIELD_INFO);
/*      */   }
/*      */ 
/*      */   protected void setTable(String tablename, String[] finfo)
/*      */     throws ServiceException
/*      */   {
/* 1349 */     DataResultSet drset = setTableInfo(tablename, finfo);
/*      */ 
/* 1351 */     if (drset == null)
/*      */     {
/* 1353 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizTableMissing", null, tablename));
/*      */     }
/* 1355 */     this.m_tables.put(tablename, drset);
/*      */   }
/*      */ 
/*      */   protected DataResultSet setTableInfo(String tableName, String[] finfo) throws ServiceException
/*      */   {
/* 1360 */     DataResultSet drset = null;
/*      */ 
/* 1362 */     drset = (DataResultSet)this.m_binder.getResultSet(tableName);
/*      */ 
/* 1364 */     if (drset == null)
/*      */     {
/* 1366 */       drset = new DataResultSet(finfo);
/* 1367 */       this.m_binder.addResultSet(tableName, drset);
/*      */     }
/*      */ 
/* 1370 */     return drset;
/*      */   }
/*      */ 
/*      */   protected void undoChanges(String errMsg, Exception e) throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1377 */       this.m_binder = CWizardUtils.readFile(this.m_absCompDir, this.m_filename, this.m_absCompDir);
/* 1378 */       setTables();
/*      */     }
/*      */     catch (Exception undoE)
/*      */     {
/* 1382 */       errMsg = LocaleResources.localizeMessage(errMsg, null);
/* 1383 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizUndoError", undoE.getMessage(), errMsg), e);
/*      */     }
/*      */ 
/* 1386 */     throw new ServiceException(errMsg, e);
/*      */   }
/*      */ 
/*      */   protected String getDefaultFileName(String cwType, String mergeTable)
/*      */   {
/* 1391 */     String filename = null;
/* 1392 */     String type = StringUtils.findString(RES_DEF, cwType, 0, 2);
/* 1393 */     String resExt = StringUtils.findString(RES_DEF, cwType, 0, 3);
/* 1394 */     filename = CWizardUtils.removeSpaces(this.m_name).toLowerCase() + "_" + type + "." + resExt;
/*      */ 
/* 1396 */     if ((type.equals("template")) && (mergeTable != null) && (mergeTable.equals("IntradocReports")))
/*      */     {
/* 1399 */       filename = "reports/reports.hda";
/*      */     }
/* 1401 */     else if (type.equals("template"))
/*      */     {
/* 1403 */       filename = "templates/" + filename;
/*      */     }
/* 1405 */     else if (!type.equals("environment"))
/*      */     {
/* 1407 */       filename = "resources/" + filename;
/*      */     }
/*      */ 
/* 1410 */     int count = 1;
/*      */ 
/* 1412 */     while (FileUtils.checkFile(FileUtils.getAbsolutePath(this.m_absCompDir, filename), true, false) != -16)
/*      */     {
/* 1415 */       int index = filename.lastIndexOf(46);
/* 1416 */       if (index < 0)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/* 1421 */       int tempIndex = index;
/* 1422 */       if (count > 1)
/*      */       {
/* 1424 */         tempIndex = index - 1;
/*      */       }
/* 1426 */       filename = filename.substring(0, tempIndex) + count + filename.substring(index, filename.length());
/*      */ 
/* 1428 */       ++count;
/*      */     }
/*      */ 
/* 1431 */     return filename;
/*      */   }
/*      */ 
/*      */   protected void saveJavaCodeChanges(boolean isFilter, DataResultSet drset)
/*      */     throws ServiceException
/*      */   {
/* 1437 */     if (isFilter)
/*      */     {
/* 1439 */       this.m_binder.addResultSet("Filters", drset);
/* 1440 */       this.m_tables.put("Filters", drset);
/*      */     }
/*      */     else
/*      */     {
/* 1444 */       this.m_binder.addResultSet("ClassAliases", drset);
/* 1445 */       this.m_tables.put("ClassAliases", drset);
/*      */     }
/* 1447 */     this.m_lastModified = CWizardUtils.writeFile(this.m_absCompDir, this.m_filename, this.m_binder);
/*      */   }
/*      */ 
/*      */   protected void createClassFile(String filename, String location, String classname, boolean isFilter, boolean isInstall)
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/* 1453 */     String newClassName = null;
/* 1454 */     String pkg = "";
/*      */ 
/* 1456 */     int index = location.lastIndexOf(46);
/*      */ 
/* 1459 */     if (index > 0)
/*      */     {
/* 1461 */       pkg = location.substring(0, index);
/*      */     }
/*      */ 
/* 1464 */     newClassName = location.substring(index + 1, location.length());
/*      */ 
/* 1466 */     FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(filename), 1);
/* 1467 */     DataBinder params = new DataBinder(SharedObjects.getSecureEnvironment());
/* 1468 */     params.putLocal("packageName", pkg);
/* 1469 */     params.putLocal("newClassName", newClassName);
/* 1470 */     if (isFilter)
/*      */     {
/* 1472 */       params.putLocal("isFilter", "true");
/*      */     }
/*      */     else
/*      */     {
/* 1476 */       params.putLocal("className", classname);
/*      */     }
/*      */ 
/* 1479 */     if (isInstall)
/*      */     {
/* 1481 */       params.putLocal("isInstallFilter", "true");
/*      */     }
/* 1483 */     CWizardUtils.writeTemplateFile(params, "CLASS_FILTER_TEMPLATE", null, filename);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1488 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80491 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.IntradocComponent
 * JD-Core Version:    0.5.4
 */