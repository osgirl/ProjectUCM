/*      */ package intradoc.server;
/*      */ 
/*      */ import intradoc.common.DataStreamWrapper;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SortUtils;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Validation;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.IdcProperties;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.data.SimpleParameters;
/*      */ import intradoc.filestore.config.ConfigFileLoader;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.io.IdcBasicIO;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.alert.AlertUtils;
/*      */ import intradoc.server.utils.CompInstallUtils;
/*      */ import intradoc.server.utils.ComponentInstaller;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.server.utils.SystemPropertiesEditor;
/*      */ import intradoc.shared.PluginFilterData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.RevisionSpec;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileReader;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Properties;
/*      */ import java.util.Set;
/*      */ import java.util.Vector;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ 
/*      */ public class AdminServiceHandler extends Service
/*      */   implements IdcBasicIO
/*      */ {
/*   42 */   public static final String[] UNSETTABLE_VARS = { "IDC_Id", "IDC_Name", "UseSecurity", "IsJdbc", "JdbcUser", "JdbcPassword", "JdbcDriver", "JdbcPasswordEncoding", "JdbcConnectionString", "UserJdbcUser", "UserJdbcPassword", "UserJdbcDriver", "UserJdbcPasswordEncoding", "UserJdbcConnectionString", "DatabasePreserveCase", "HttpServerAddress", "IntradocServerPort", "IntradocServerHostName", "SocketHostNameSecurityFilter", "SocketHostAddressSecurityFilter", "ProxyPassword", "ProxyPasswordEncoding" };
/*      */ 
/*   50 */   public static final String[] NOT_PASSED_VARS = { "IsJdbc", "JdbcUser", "JdbcPassword", "JdbcDriver", "JdbcPasswordEncoding", "JdbcConnectionString", "UserJdbcUser", "UserJdbcPassword", "UserJdbcDriver", "UserJdbcPasswordEncoding", "UserJdbcConnectionString", "ProxyPassword", "ProxyPasswordEncoding" };
/*      */   protected ConfigFileUtilities m_CFU;
/*      */ 
/*      */   public void initDelegatedObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*   63 */     super.initDelegatedObjects();
/*      */ 
/*   66 */     mergeServerData();
/*   67 */     String idcId = SharedObjects.getEnvironmentValue("IDC_Id");
/*   68 */     this.m_binder.putLocal("IDC_Id", (idcId != null) ? idcId : "");
/*   69 */     String idcName = SharedObjects.getEnvironmentValue("IDC_Name");
/*   70 */     this.m_binder.putLocal("IDC_Name", (idcName != null) ? idcName : "");
/*   71 */     this.m_binder.putLocal("isSimplifiedServer", "1");
/*   72 */     this.m_binder.putLocal("hasSimplifiedAlterStatus", "1");
/*   73 */     String originalCgiName = SharedObjects.getEnvironmentValue("OriginalCgiFileName");
/*      */ 
/*   76 */     Properties secureEnv = SharedObjects.getSecureEnvironment();
/*   77 */     IdcProperties curEnv = (IdcProperties)this.m_binder.getEnvironment();
/*   78 */     curEnv.setDefaults(secureEnv);
/*      */ 
/*   81 */     String toContentServerPath = null;
/*   82 */     if (originalCgiName == null)
/*      */     {
/*   84 */       toContentServerPath = DirectoryLocator.getEnterpriseCgiWebUrl(false);
/*      */     }
/*      */     else
/*      */     {
/*   88 */       toContentServerPath = DirectoryLocator.getRelativeCgiRoot() + originalCgiName;
/*      */     }
/*      */ 
/*   92 */     this.m_binder.putLocal("RemoteAbsoluteHttpCgiPath", toContentServerPath);
/*   93 */     String relativeWebRoot = DirectoryLocator.getWebRoot(false);
/*   94 */     this.m_binder.putLocal("RemoteAbsoluteWebRoot", relativeWebRoot);
/*      */ 
/*   96 */     loadSimplifiedServerStatus();
/*      */   }
/*      */ 
/*      */   public void loadSimplifiedServerStatus() throws ServiceException
/*      */   {
/*  101 */     this.m_binder.putLocal("IdcServerStatus", "csServerStatusRunning");
/*      */   }
/*      */ 
/*      */   public boolean getBooleanValue(Map in, String key, boolean defVal)
/*      */   {
/*  106 */     if (in == null)
/*      */     {
/*  108 */       return defVal;
/*      */     }
/*  110 */     Object val = in.get(key);
/*  111 */     return ScriptUtils.convertObjectToBool(val, defVal);
/*      */   }
/*      */ 
/*      */   public void initFileStoreObjects()
/*      */     throws DataException, ServiceException
/*      */   {
/*  120 */     if (null == ConfigFileLoader.m_defaultCFU)
/*      */     {
/*  122 */       Report.trace("system", "WARNING: AdminServiceHandler created without a ConfigFileStore", null);
/*      */ 
/*  124 */       super.initFileStoreObjects();
/*  125 */       return;
/*      */     }
/*  127 */     this.m_CFU = ConfigFileLoader.m_defaultCFU;
/*  128 */     if (this == this.m_CFU.m_context)
/*      */       return;
/*  130 */     this.m_CFU = ConfigFileUtilities.createConfigFileUtilities(this.m_CFU.m_CFS, this);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerOutput()
/*      */   {
/*  141 */     String output = " ";
/*  142 */     String clear = this.m_binder.getLocal("ClearOutput");
/*  143 */     if (StringUtils.convertToBool(clear, false))
/*      */     {
/*  145 */       IdcServerOutput.clearOutput();
/*      */     }
/*      */     else
/*      */     {
/*  149 */       output = IdcServerOutput.viewOutput();
/*      */     }
/*  151 */     this.m_binder.putLocal("ServerOutput", output);
/*      */   }
/*      */ 
/*      */   public void loadLoggedServerOutput()
/*      */     throws ServiceException
/*      */   {
/*  158 */     String dir = SharedObjects.getEnvironmentValue("EventDirectory");
/*  159 */     if (dir == null)
/*      */     {
/*  161 */       dir = SharedObjects.getEnvironmentValue("BaseLogDir");
/*  162 */       dir = dir + "/trace/event";
/*      */     }
/*      */ 
/*  165 */     File f = new File(dir);
/*  166 */     if (f.isDirectory())
/*      */     {
/*  168 */       File[] files = f.listFiles();
/*  169 */       ArrayList fileNames = new ArrayList();
/*  170 */       for (int i = 0; i < files.length; ++i)
/*      */       {
/*  172 */         fileNames.add(files[i].getName());
/*      */       }
/*  174 */       SortUtils.sortStringList(fileNames, false);
/*      */ 
/*  176 */       DataResultSet rset = new DataResultSet(new String[] { "product", "time", "logFile" });
/*  177 */       Pattern p = Pattern.compile("(.*)_([0-9]*|current).log");
/*  178 */       for (String fileName : fileNames)
/*      */       {
/*  180 */         Vector row = new IdcVector();
/*  181 */         Matcher m = p.matcher(fileName);
/*  182 */         if (!m.matches())
/*      */         {
/*  184 */           Report.trace(null, "fileName " + fileName + " doesn't match filename pattern.", null);
/*      */ 
/*  186 */           row.add(fileName);
/*  187 */           row.add("");
/*      */         }
/*      */         else
/*      */         {
/*  191 */           row.add(m.group(1));
/*  192 */           row.add(m.group(2));
/*      */         }
/*  194 */         row.add(fileName);
/*  195 */         rset.addRow(row);
/*      */       }
/*  197 */       this.m_binder.addResultSetDirect("TraceLogs", rset);
/*      */     }
/*      */ 
/*  200 */     String fileName = this.m_binder.getLocal("fileName");
/*  201 */     if (fileName == null)
/*      */     {
/*  203 */       String prefix = SharedObjects.getEnvironmentValue("IdcProductName") + "_";
/*  204 */       String clusterNodeName = SharedObjects.getEnvironmentValue("IDC_Id");
/*  205 */       if (clusterNodeName != null)
/*      */       {
/*  207 */         prefix = prefix + clusterNodeName + "_";
/*      */       }
/*  209 */       fileName = prefix + "current.log";
/*  210 */       this.m_binder.putLocal("fileName", fileName);
/*      */     }
/*      */ 
/*  213 */     f = new File(dir, fileName);
/*  214 */     BufferedReader r = null;
/*      */     try
/*      */     {
/*  217 */       IdcStringBuilder builder = new IdcStringBuilder();
/*  218 */       if (f.exists())
/*      */       {
/*  220 */         r = new BufferedReader(new FileReader(f));
/*  221 */         char[] buf = new char[1024];
/*      */ 
/*  223 */         while ((len = r.read(buf)) > 0)
/*      */         {
/*      */           int len;
/*  225 */           builder.append(buf, 0, len);
/*      */         }
/*      */       }
/*  228 */       this.m_binder.putLocal("ServerOutput", builder.toString());
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  236 */       FileUtils.closeObject(r);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void copyLocalValues(String[] list, DataBinder binder, Map props)
/*      */   {
/*  243 */     for (int i = 0; i < list.length; ++i)
/*      */     {
/*  245 */       String val = binder.getLocal(list[i]);
/*  246 */       if (val == null)
/*      */         continue;
/*  248 */       props.put(list[i], val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public InputStream getReadStream(String file)
/*      */     throws IOException
/*      */   {
/*  258 */     return FileUtilsCfgBuilder.getCfgInputStream(file);
/*      */   }
/*      */ 
/*      */   public OutputStream getWriteStream(String file)
/*      */     throws IOException
/*      */   {
/*  266 */     return FileUtilsCfgBuilder.getCfgOutputStream(file, null);
/*      */   }
/*      */ 
/*      */   public void mergeServerData() throws DataException
/*      */   {
/*  271 */     DataBinder serverBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  272 */     Properties serverData = serverBinder.getLocalData();
/*  273 */     Properties localData = this.m_binder.getLocalData();
/*  274 */     DataBinder.mergeHashTables(localData, serverData);
/*  275 */     this.m_binder.setLocalData(localData);
/*      */   }
/*      */ 
/*      */   protected void prepareComponentListEditor(ComponentListEditor compLE)
/*      */     throws ServiceException, DataException
/*      */   {
/*  286 */     this.m_binder.putLocal("fileName", "is testing of polluting of config");
/*      */ 
/*  288 */     Map env = this.m_binder.getLocalData();
/*      */ 
/*  290 */     String idcDir = this.m_binder.getLocal("IntradocDir");
/*  291 */     String configDir = this.m_binder.getLocal("ConfigDir");
/*  292 */     String compDir = this.m_binder.getLocal("ComponentsDataDir");
/*  293 */     String homeDir = this.m_binder.getLocal("IdcHomeDir");
/*      */ 
/*  295 */     compLE.setIO(this);
/*  296 */     compLE.init(idcDir, configDir, compDir, homeDir, env);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentData()
/*      */     throws ServiceException, DataException
/*      */   {
/*  306 */     loadComponentData(false);
/*      */   }
/*      */ 
/*      */   public void loadComponentData(boolean isToggle) throws ServiceException, DataException
/*      */   {
/*  311 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/*  312 */     String configDir = LegacyDirectoryLocator.getConfigDirectory();
/*  313 */     String compDir = LegacyDirectoryLocator.getAppDataDirectory() + "/components/";
/*      */ 
/*  316 */     String encoding = DataSerializeUtils.getSystemEncoding();
/*      */ 
/*  319 */     ComponentListEditor compLE = new ComponentListEditor();
/*      */ 
/*  326 */     compLE.m_isAdminServer = true;
/*  327 */     compLE.m_isListsOnly = true;
/*      */ 
/*  329 */     this.m_binder.putLocal("IntradocDir", intradocDir);
/*  330 */     this.m_binder.putLocal("ConfigDir", configDir);
/*  331 */     this.m_binder.putLocal("ComponentsDataDir", compDir);
/*  332 */     this.m_binder.putLocal("FileEncoding", encoding);
/*      */ 
/*  334 */     prepareComponentListEditor(compLE);
/*      */ 
/*  336 */     if (isToggle)
/*      */     {
/*  338 */       if (DataBinderUtils.getBoolean(this.m_binder, "isSimple", false))
/*      */       {
/*  340 */         String enableComponentList = this.m_binder.getLocal("EnableComponentList");
/*  341 */         String disableComponentList = this.m_binder.getLocal("DisableComponentList");
/*      */ 
/*  343 */         compLE.enableOrDisableComponent(enableComponentList, true);
/*  344 */         compLE.enableOrDisableComponent(disableComponentList, false);
/*      */       }
/*      */       else
/*      */       {
/*  348 */         boolean enable = StringUtils.convertToBool(this.m_binder.getLocal("isEnable"), true);
/*      */ 
/*  350 */         String components = getComponentList();
/*  351 */         compLE.enableOrDisableComponent(components, enable);
/*      */       }
/*      */ 
/*  354 */       setRestartRequired("true");
/*      */     }
/*  356 */     storeComponentResultSets(compLE, intradocDir);
/*  357 */     compLE.closeAllStreams();
/*      */   }
/*      */ 
/*      */   protected String getComponentList()
/*      */   {
/*  362 */     String components = this.m_binder.getLocal("installedComponents");
/*  363 */     if (components == null)
/*      */     {
/*  365 */       components = this.m_binder.getLocal("ComponentNames");
/*      */     }
/*  367 */     if (components == null)
/*      */     {
/*  369 */       components = this.m_binder.getLocal("EnableComponentList");
/*      */     }
/*  371 */     if ((components == null) || (components.length() == 0))
/*      */     {
/*  373 */       components = this.m_binder.getLocal("DisableComponentList");
/*      */     }
/*  375 */     return components;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void toggleComponents()
/*      */     throws ServiceException, DataException
/*      */   {
/*  385 */     String components = getComponentList();
/*  386 */     if ((components == null) || (components.length() <= 0))
/*      */       return;
/*  388 */     loadComponentData(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void contentServerSelfToggleComponents()
/*      */     throws ServiceException, DataException
/*      */   {
/*  395 */     UserData userData = getUserData();
/*  396 */     if (!SecurityUtils.isUserOfRole(userData, "sysmanager"))
/*      */     {
/*  398 */       String msg = LocaleUtils.encodeMessage("csInsufficientPrivilege", null);
/*  399 */       createServiceException(null, msg);
/*      */     }
/*  401 */     String enableComponentList = this.m_binder.getLocal("EnableComponentList");
/*  402 */     String disableComponentList = this.m_binder.getLocal("DisableComponentList");
/*      */ 
/*  404 */     ComponentListEditor compLE = new ComponentListEditor();
/*  405 */     compLE.init(false);
/*  406 */     compLE.enableOrDisableComponent(enableComponentList, true);
/*  407 */     compLE.enableOrDisableComponent(disableComponentList, false);
/*  408 */     this.m_binder.putLocal("StatusCode", "0");
/*      */   }
/*      */ 
/*      */   protected void storeComponentResultSets(ComponentListEditor compLE, String intradocDir)
/*      */     throws ServiceException, DataException
/*      */   {
/*  419 */     DataResultSet allComps = compLE.getComponentSet();
/*      */ 
/*  422 */     DataResultSet enabledSet = new DataResultSet();
/*  423 */     DataResultSet disabledSet = new DataResultSet();
/*  424 */     DataResultSet editableSet = new DataResultSet();
/*  425 */     DataResultSet downComps = new DataResultSet();
/*      */ 
/*  427 */     DataResultSet descriptionSet = new DataResultSet(new String[] { "description" });
/*  428 */     this.m_binder.m_blFieldTypes.put("description", "message");
/*  429 */     allComps.mergeWithFlags(null, descriptionSet, 16, 0);
/*  430 */     FieldInfo info = new FieldInfo();
/*  431 */     allComps.getFieldInfo("description", info);
/*  432 */     int counter = 0;
/*  433 */     for (SimpleParameters params : allComps.getSimpleParametersIterable())
/*      */     {
/*  435 */       String compName = params.get("name");
/*  436 */       allComps.setCurrentRow(counter++);
/*  437 */       allComps.setCurrentValue(info.m_index, "!csCompDesc_" + compName);
/*      */     }
/*      */ 
/*  440 */     enabledSet.copyFieldInfo(allComps);
/*  441 */     disabledSet.copyFieldInfo(allComps);
/*  442 */     editableSet.copyFieldInfo(allComps);
/*  443 */     downComps.copyFieldInfo(allComps);
/*      */ 
/*  446 */     DataResultSet tagSet = new DataResultSet(new String[] { "tag", "tagComponents" });
/*      */ 
/*  449 */     DataResultSet legacyComponentTags = compLE.getLegacyTaggedComponentSet();
/*  450 */     int nameIndex = -1;
/*  451 */     int tagIndex = -1;
/*  452 */     if (legacyComponentTags != null)
/*      */     {
/*  454 */       nameIndex = ResultSetUtils.getIndexMustExist(legacyComponentTags, "componentName");
/*  455 */       tagIndex = ResultSetUtils.getIndexMustExist(legacyComponentTags, "tags");
/*      */     }
/*      */ 
/*  458 */     boolean buildBeforeDownload = SharedObjects.getEnvValueAsBoolean("BuildComponentBeforeDownload", false);
/*      */ 
/*  460 */     for (allComps.first(); allComps.isRowPresent(); allComps.next())
/*      */     {
/*  462 */       Map map = allComps.getCurrentRowMap();
/*  463 */       Vector row = allComps.getCurrentRowValues();
/*      */ 
/*  465 */       String name = (String)map.get("name");
/*  466 */       String status = (String)map.get("status");
/*  467 */       String installID = (String)map.get("installID");
/*  468 */       String tags = (String)map.get("componentTags");
/*  469 */       boolean hasPreferenceData = StringUtils.convertToBool((String)map.get("hasPreferenceData"), false);
/*      */ 
/*  471 */       if (status.equalsIgnoreCase("disabled"))
/*      */       {
/*  473 */         disabledSet.addRow((Vector)row.clone());
/*      */       }
/*      */       else
/*      */       {
/*  477 */         enabledSet.addRow((Vector)row.clone());
/*      */       }
/*      */ 
/*  480 */       if ((installID != null) && (installID.length() > 0) && (hasPreferenceData))
/*      */       {
/*  482 */         editableSet.addRow((Vector)row.clone());
/*      */       }
/*      */ 
/*  485 */       boolean isLocal = ComponentLocationUtils.isLocal(map);
/*  486 */       if (isLocal)
/*      */       {
/*  490 */         String location = ComponentLocationUtils.determineComponentLocationWithEnv(map, 1, this.m_binder.getLocalData(), false);
/*      */ 
/*  492 */         String dir = FileUtils.getDirectory(location);
/*      */ 
/*  494 */         String dwnLocation = null;
/*  495 */         if (buildBeforeDownload)
/*      */         {
/*  497 */           dwnLocation = FileUtils.getAbsolutePath(dir, "manifest.hda");
/*      */         }
/*      */         else
/*      */         {
/*  502 */           dwnLocation = FileUtils.getAbsolutePath(dir, name + ".zip");
/*      */         }
/*      */ 
/*  505 */         if (FileUtils.checkFile(dwnLocation, true, false) >= 0)
/*      */         {
/*  507 */           downComps.addRow((Vector)row.clone());
/*      */         }
/*  509 */         else if (!buildBeforeDownload)
/*      */         {
/*  512 */           dwnLocation = FileUtils.getAbsolutePath(dir, "manifest.zip");
/*  513 */           if (FileUtils.checkFile(dwnLocation, true, false) >= 0)
/*      */           {
/*  515 */             downComps.addRow((Vector)row.clone());
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  521 */       List tList = StringUtils.makeListFromSequenceSimple(tags);
/*  522 */       int size = tList.size();
/*      */ 
/*  525 */       if ((size == 0) && (legacyComponentTags != null))
/*      */       {
/*  527 */         String sysTags = null;
/*  528 */         Vector sysRow = legacyComponentTags.findRow(nameIndex, name);
/*  529 */         if (sysRow != null)
/*      */         {
/*  531 */           sysTags = legacyComponentTags.getStringValue(tagIndex);
/*  532 */           List sList = StringUtils.makeListFromSequenceSimple(sysTags);
/*  533 */           for (int i = 0; i < sList.size(); ++i)
/*      */           {
/*  535 */             String tag = (String)sList.get(i);
/*  536 */             if (tList.contains(tag))
/*      */               continue;
/*  538 */             tList.add(tag);
/*      */           }
/*      */         }
/*      */ 
/*  542 */         size = tList.size();
/*      */       }
/*      */ 
/*  545 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  547 */         String tag = (String)tList.get(i);
/*  548 */         Vector tRow = tagSet.findRow(0, tag);
/*  549 */         if (tRow == null)
/*      */         {
/*  551 */           tRow = tagSet.createEmptyRow();
/*  552 */           tRow.setElementAt(tag, 0);
/*  553 */           tagSet.addRow(tRow);
/*      */         }
/*      */ 
/*  557 */         String cmpStr = (String)tRow.elementAt(1);
/*  558 */         if (cmpStr.length() > 0)
/*      */         {
/*  560 */           cmpStr = cmpStr + ",";
/*      */         }
/*  562 */         cmpStr = cmpStr + name;
/*  563 */         tRow.setElementAt(cmpStr, 1);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  568 */     String enabledName = this.m_currentAction.getParamAt(0);
/*  569 */     String disabledName = this.m_currentAction.getParamAt(1);
/*  570 */     String downloadableName = this.m_currentAction.getParamAt(2);
/*  571 */     String editableName = this.m_currentAction.getParamAt(3);
/*  572 */     String tagSetName = this.m_currentAction.getParamAt(4);
/*      */ 
/*  574 */     this.m_binder.addResultSet(enabledName, enabledSet);
/*  575 */     this.m_binder.addResultSet(disabledName, disabledSet);
/*  576 */     this.m_binder.addResultSet(editableName, editableSet);
/*  577 */     this.m_binder.addResultSet(downloadableName, downComps);
/*  578 */     this.m_binder.addResultSet(tagSetName, tagSet);
/*      */ 
/*  580 */     this.m_binder.addResultSet("Components", allComps);
/*      */ 
/*  583 */     ResultSetUtils.sortResultSet(disabledSet, new String[] { "name" });
/*  584 */     ResultSetUtils.sortResultSet(downComps, new String[] { "name" });
/*  585 */     ResultSetUtils.sortResultSet(tagSet, new String[] { "tag" });
/*  586 */     ResultSetUtils.sortResultSet(allComps, new String[] { "name" });
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void updateComponentConfig()
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/*  596 */     getOrUpdateComponentConfig(true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void getComponentConfig()
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/*  604 */     getOrUpdateComponentConfig(false);
/*      */   }
/*      */ 
/*      */   protected void getOrUpdateComponentConfig(boolean isUpdate)
/*      */     throws ServiceException, IOException, DataException
/*      */   {
/*  611 */     if (!SharedObjects.getEnvValueAsBoolean("AllowUpdateComponentConfig", true))
/*      */     {
/*  613 */       createServiceException(null, "!csAdminUpdateCompoentConfigNotAllowed");
/*      */     }
/*      */ 
/*  616 */     String installID = this.m_binder.getLocal("installID");
/*  617 */     if ((installID == null) || (installID.length() == 0))
/*      */     {
/*  619 */       createServiceException(null, "!csInstallIdRequired");
/*      */     }
/*      */ 
/*  622 */     String compName = this.m_binder.getLocal("ComponentName");
/*  623 */     if ((compName == null) || (compName.length() == 0))
/*      */     {
/*  625 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/*  629 */     String compPath = ComponentLocationUtils.computeAbsoluteComponentLocation(compName);
/*  630 */     DataBinder cmpBinder = ComponentLoader.getComponentBinder(compName);
/*  631 */     if (cmpBinder == null)
/*      */     {
/*  633 */       cmpBinder = ComponentLoader.getDisabledComponentBinder(compName);
/*      */     }
/*  635 */     String useType = (cmpBinder != null) ? cmpBinder.getLocal("useType") : "local";
/*  636 */     boolean isLocal = (useType != null) && (useType.equals("local"));
/*  637 */     if (compPath == null)
/*      */       return;
/*  639 */     String compDir = FileUtils.getDirectory(compPath);
/*      */ 
/*  641 */     String compDataDir = CompInstallUtils.getInstallConfPath(installID, compName);
/*      */ 
/*  644 */     ComponentPreferenceData prefData = new ComponentPreferenceData(compDir, compDataDir);
/*  645 */     prefData.setCanUpdate(isLocal);
/*  646 */     prefData.load();
/*  647 */     prefData.addResultSetsToBinder(prefData.getPreferenceResources(), this.m_binder);
/*  648 */     prefData.loadPreferenceStrings();
/*  649 */     DataResultSet prefTable = prefData.getPreferenceTable();
/*      */ 
/*  652 */     Properties settings = null;
/*  653 */     if (DataBinderUtils.getBoolean(this.m_binder, "revertToInstall", false))
/*      */     {
/*  655 */       settings = prefData.m_installData;
/*      */     }
/*      */     else
/*      */     {
/*  659 */       settings = prefData.m_configData;
/*      */     }
/*      */ 
/*  663 */     DataResultSet configRset = prefTable.shallowClone();
/*  664 */     ResultSetUtils.addColumnsWithDefaultValues(configRset, null, new String[] { "" }, new String[] { "pCurrVal" });
/*      */ 
/*  666 */     FieldInfo[] fi = ResultSetUtils.createInfoList(prefTable, new String[] { "pName", "pCurrVal" }, true);
/*      */ 
/*  668 */     for (configRset.first(); configRset.isRowPresent(); configRset.next())
/*      */     {
/*  670 */       String name = configRset.getStringValue(fi[0].m_index);
/*  671 */       String currVal = settings.getProperty(name);
/*      */ 
/*  674 */       if (isUpdate)
/*      */       {
/*  676 */         currVal = this.m_binder.getLocal(name);
/*  677 */         if (currVal != null)
/*      */         {
/*  679 */           SharedObjects.putEnvironmentValue(name, currVal);
/*  680 */           settings.put(name, currVal);
/*      */         }
/*      */       }
/*      */ 
/*  684 */       if ((currVal == null) || (currVal.length() <= 0))
/*      */         continue;
/*  686 */       configRset.setCurrentValue(fi[1].m_index, currVal);
/*      */     }
/*      */ 
/*  690 */     if (isUpdate)
/*      */     {
/*  692 */       prefData.save();
/*  693 */       String msg = "!csComponentCfgUpdateDone";
/*  694 */       this.m_binder.putLocal("StatusMessageKey", msg);
/*  695 */       this.m_binder.putLocal("StatusMessage", msg);
/*  696 */       this.m_binder.putLocal("StatusCode", "0");
/*      */ 
/*  699 */       String alertId = "csComponentUpdateNeedRestart";
/*  700 */       String alertMsg = "<$lcMessage('!csComponentUpdateNeedRestart')$>";
/*  701 */       addAlert(alertId, alertMsg, 1);
/*      */     }
/*      */     else
/*      */     {
/*  705 */       String rsetName = this.m_currentAction.getParamAt(0);
/*  706 */       this.m_binder.addResultSet(rsetName, configRset);
/*  707 */       addToBinder("ComponentName", compName);
/*  708 */       addToBinder("installID", installID);
/*      */     }
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void uninstallComponent()
/*      */     throws DataException, ServiceException
/*      */   {
/*  719 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUnintall", true))
/*      */     {
/*  721 */       createServiceException(null, "!csAdminUninstallNotAllowed");
/*      */     }
/*      */ 
/*  724 */     ComponentListManager.init();
/*  725 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/*  726 */     compLE.loadComponents();
/*      */ 
/*  728 */     String compName = this.m_binder.getLocal("ComponentName");
/*  729 */     if (compName == null)
/*      */     {
/*  731 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/*  734 */     DataResultSet components = compLE.getComponentSet();
/*  735 */     int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/*  736 */     Vector row = components.findRow(nameIndex, compName);
/*  737 */     if (row == null)
/*      */     {
/*  739 */       String msg = LocaleUtils.encodeMessage("csAdminComponentDoesNotExist", null, compName);
/*      */ 
/*  741 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  744 */     Map map = components.getCurrentRowMap();
/*  745 */     doComponentInstallUninstall(compName, map, false);
/*  746 */     String msg = "!csUninstallCompleted";
/*  747 */     this.m_binder.putLocal("StatusMessageKey", msg);
/*  748 */     this.m_binder.putLocal("StatusMessage", msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void executeManifest()
/*      */     throws DataException, ServiceException
/*      */   {
/*  759 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/*  761 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/*  764 */     String compName = this.m_binder.getLocal("ComponentName");
/*  765 */     if (compName == null)
/*      */     {
/*  767 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/*  770 */     Exception error = null;
/*      */     try
/*      */     {
/*  773 */       doComponentInstallUninstall(compName, this.m_binder.getLocalData(), true);
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  777 */       error = e;
/*      */     }
/*      */ 
/*  780 */     if (error != null)
/*      */     {
/*  782 */       this.m_binder.putLocal("installedComponents", compName);
/*  783 */       this.m_binder.putLocal("errorResourceInclude", "component_install_error_msg");
/*  784 */       createServiceException(error, LocaleUtils.encodeMessage("csCannotInstallComponentToLocation", null, compName));
/*      */     }
/*      */     else
/*      */     {
/*  790 */       String enabledStr = this.m_binder.getLocal("enabledComponents");
/*  791 */       List enabledList = StringUtils.makeListFromSequenceSimple(enabledStr);
/*  792 */       if (enabledList.size() > 0)
/*      */       {
/*  796 */         ComponentListManager.init();
/*  797 */         ComponentListEditor compLE = ComponentListManager.getEditor();
/*      */ 
/*  799 */         String compListStr = this.m_binder.getLocal("installedComponents");
/*  800 */         compLE.enableOrDisableComponent(compListStr, true);
/*  801 */         this.m_binder.putLocal("didEnabled", "1");
/*      */       }
/*      */     }
/*  804 */     if (this.m_binder.getLocal("StatusMessage") != null) {
/*      */       return;
/*      */     }
/*  807 */     this.m_binder.putLocal("StatusMessage", "");
/*  808 */     this.m_binder.putLocal("StatusMessageKey", "");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentInstallInfo()
/*      */     throws DataException, ServiceException
/*      */   {
/*  819 */     String compName = this.m_binder.getLocal("ComponentName");
/*  820 */     String location = this.m_binder.getLocal("location");
/*  821 */     String zipPath = parseManifestZipPath();
/*      */ 
/*  823 */     ComponentInstaller installer = new ComponentInstaller();
/*  824 */     DataBinder manifestData = installer.readManifestInfoFromZip(zipPath);
/*  825 */     processComponentInstallStep(manifestData, zipPath, compName, location, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadComponentInstallSettings() throws ServiceException, DataException
/*      */   {
/*  831 */     String compName = this.m_binder.getLocal("ComponentName");
/*  832 */     String location = this.m_binder.getLocal("location");
/*      */ 
/*  834 */     if (compName == null)
/*      */     {
/*  836 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*  838 */     if (location == null)
/*      */     {
/*  840 */       createServiceException(null, "!csComponentLocationRequired");
/*      */     }
/*      */ 
/*  843 */     String zipPath = parseManifestZipPath();
/*  844 */     processComponentInstallStep(null, zipPath, compName, location, true);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveComponentZip()
/*      */     throws DataException, ServiceException
/*      */   {
/*  854 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/*  856 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/*  859 */     Vector tempFiles = this.m_binder.getTempFiles();
/*  860 */     if (tempFiles.size() == 0)
/*      */     {
/*  862 */       createServiceException(null, "!csAdminNoManifest");
/*      */     }
/*      */ 
/*  865 */     if (tempFiles.size() > 1)
/*      */     {
/*  867 */       createServiceException(null, "!csAdminNoMultipleFileUpload");
/*      */     }
/*      */ 
/*  871 */     String zipName = (String)tempFiles.elementAt(0);
/*      */ 
/*  873 */     ComponentInstaller installer = new ComponentInstaller();
/*  874 */     DataBinder tmpBinder = installer.readManifestInfoFromZip(zipName);
/*  875 */     if (tmpBinder == null)
/*      */     {
/*  877 */       createServiceException(null, "csAdminUnableToLoadManifestInfo");
/*      */     }
/*      */ 
/*  880 */     String[] retVal = installer.retrieveComponentNameAndLocation(tmpBinder);
/*  881 */     String location = retVal[0];
/*  882 */     String compName = retVal[1];
/*      */ 
/*  884 */     DataBinder cmpBinder = installer.readFileAsBinder(zipName, "component/" + location.toLowerCase());
/*      */ 
/*  886 */     String[] paths = computeManifestZipPaths(cmpBinder, compName);
/*      */ 
/*  888 */     String absPath = paths[0];
/*  889 */     String keyedPath = paths[1];
/*  890 */     FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(absPath), 2);
/*  891 */     FileUtils.copyFile(zipName, absPath);
/*  892 */     addToBinder("ComponentName", compName);
/*  893 */     addToBinder("location", location);
/*  894 */     addToBinder("componentDir", keyedPath);
/*      */   }
/*      */ 
/*      */   protected void doComponentInstallUninstall(String compName, Map<String, String> cmpMap, boolean isInstall)
/*      */     throws DataException, ServiceException
/*      */   {
/*  902 */     ComponentInstaller installer = new ComponentInstaller();
/*  903 */     DataBinder manifestData = null;
/*  904 */     String fileName = null;
/*      */ 
/*  906 */     String location = (String)cmpMap.get("location");
/*  907 */     String compDir = null;
/*  908 */     if (isInstall)
/*      */     {
/*  910 */       compDir = FileUtils.getDirectory(location);
/*  911 */       fileName = parseManifestZipPath();
/*      */     }
/*      */     else
/*      */     {
/*  915 */       boolean isLocal = ComponentLocationUtils.isLocal(cmpMap);
/*  916 */       if (!isLocal)
/*      */       {
/*  918 */         createServiceException(null, "!csAdminUninstallNonlocalComponentError");
/*      */       }
/*      */ 
/*  922 */       location = ComponentLocationUtils.determineComponentLocation(cmpMap, 1);
/*      */ 
/*  924 */       compDir = FileUtils.getDirectory(location);
/*      */ 
/*  927 */       fileName = FileUtils.getAbsolutePath(compDir, "manifest.hda");
/*      */ 
/*  929 */       if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */       {
/*  931 */         String tempLoc = FileUtils.getDirectory(location) + "/" + compName + ".zip";
/*  932 */         fileName = FileUtils.getAbsolutePath(compDir, tempLoc);
/*  933 */         if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */         {
/*  935 */           tempLoc = FileUtils.getDirectory(location) + "/manifest.zip";
/*  936 */           fileName = FileUtils.getAbsolutePath(compDir, tempLoc);
/*  937 */           if (FileUtils.checkFile(fileName, true, false) < 0)
/*      */           {
/*  940 */             createServiceException(null, "!csAdminUninstallNoBuildSettings");
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/*  946 */     boolean isZipFile = true;
/*  947 */     DataBinder compData = null;
/*  948 */     Map args = new HashMap();
/*  949 */     if (fileName.endsWith(".hda"))
/*      */     {
/*  951 */       isZipFile = false;
/*  952 */       String dir = FileUtils.getDirectory(fileName);
/*      */ 
/*  955 */       manifestData = new DataBinder();
/*  956 */       ResourceUtils.serializeDataBinder(dir, "manifest.hda", manifestData, false, true);
/*      */ 
/*  959 */       compData = new DataBinder();
/*  960 */       ResourceUtils.serializeDataBinder(dir, compName + ".hda", compData, false, true);
/*      */     }
/*      */     else
/*      */     {
/*  964 */       manifestData = ZipFunctions.extractFileAsDataBinder(fileName, "manifest.hda");
/*  965 */       String[] r = installer.retrieveComponentNameAndLocation(manifestData);
/*  966 */       compData = ZipFunctions.extractFileAsDataBinder(fileName, "component/" + r[0]);
/*      */ 
/*  971 */       DataBinder componentDef = ZipFunctions.extractFileAsDataBinder(fileName, "component/" + location);
/*      */ 
/*  973 */       boolean doNotBackupZip = DataBinderUtils.getBoolean(componentDef, "disableZipFileBackup", false);
/*      */ 
/*  976 */       if (!doNotBackupZip)
/*      */       {
/*  978 */         args.put("Backup", "true");
/*  979 */         args.put("Overwrite", "false");
/*      */       }
/*      */       else
/*      */       {
/*  983 */         args.put("Backup", "false");
/*  984 */         args.put("Overwrite", "true");
/*      */       }
/*      */ 
/*  989 */       Properties props = componentDef.getLocalData();
/*  990 */       if (props.containsKey("disableZipFileBackup"))
/*      */       {
/*  992 */         this.m_binder.putLocal("disableZipFileBackup", props.getProperty("disableZipFileBackup"));
/*      */       }
/*      */     }
/*      */ 
/*  996 */     String installID = this.m_binder.getLocal("installID");
/*  997 */     if (!isInstall)
/*      */     {
/* 1001 */       installID = CompInstallUtils.getInstallID(compName);
/* 1002 */       executeUninstallFilter(compData, manifestData, fileName, compName, location, isZipFile);
/*      */     }
/*      */ 
/* 1005 */     String backupName = installer.getComponentBackupPath(installID, compName);
/*      */ 
/* 1008 */     String type = "Uninstall";
/* 1009 */     if (isInstall)
/*      */     {
/* 1011 */       type = "Install";
/*      */     }
/*      */ 
/* 1014 */     args.put(type, "true");
/* 1015 */     if (isZipFile)
/*      */     {
/* 1017 */       args.put("ZipName", fileName);
/*      */     }
/*      */     else
/*      */     {
/* 1021 */       args.remove("ZipName");
/*      */     }
/* 1023 */     args.put("BackupZipName", backupName);
/*      */ 
/* 1025 */     installer.executeInstaller(compData, manifestData, installID, compName, args);
/* 1026 */     if (!isInstall)
/*      */       return;
/* 1028 */     installer.doInstallExtra(this.m_binder, compName, location, fileName, installID);
/*      */ 
/* 1031 */     this.m_binder.putLocal("logDataDir", installer.getLogDataDir());
/* 1032 */     this.m_binder.putLocal("logFileName", installer.getLogFileName());
/* 1033 */     Vector compList = installer.getSucessfulComponents();
/* 1034 */     if ((compList != null) && (compList.size() > 0))
/*      */     {
/* 1036 */       this.m_binder.putLocal("installedComponents", StringUtils.createStringSimple(compList));
/*      */     }
/* 1038 */     Vector enabledList = installer.getEnabledComponents();
/* 1039 */     if ((enabledList == null) || (enabledList.size() <= 0))
/*      */       return;
/* 1041 */     this.m_binder.putLocal("enabledComponents", StringUtils.createStringSimple(enabledList));
/*      */   }
/*      */ 
/*      */   protected void executeUninstallFilter(DataBinder compData, DataBinder manifest, String fileName, String compName, String compLoc, boolean isZipFile)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1050 */     if (manifest == null)
/*      */     {
/* 1052 */       return;
/*      */     }
/*      */ 
/* 1055 */     CompInstallUtils.hideComponentDocMetaFields(this.m_workspace, compName, null);
/*      */ 
/* 1057 */     SharedObjects.putEnvironmentValue("ComponentName", compName);
/* 1058 */     String filterType = compName + "ComponentUninstallFilter";
/* 1059 */     if (!PluginFilters.hasFilter(filterType))
/*      */     {
/* 1061 */       DataBinder binder = null;
/* 1062 */       String idcDir = SharedObjects.getEnvironmentValue("IntradocDir");
/* 1063 */       if (isZipFile)
/*      */       {
/* 1065 */         ResultSet manifestSet = manifest.getResultSet("Manifest");
/* 1066 */         String tempLoc = ResultSetUtils.findValue(manifestSet, "entryType", "component", "location");
/*      */ 
/* 1068 */         binder = ZipFunctions.extractFileAsDataBinder(fileName, "component/" + tempLoc);
/*      */       }
/*      */       else
/*      */       {
/* 1072 */         String tempName = FileUtils.getAbsolutePath(idcDir, compLoc);
/*      */ 
/* 1074 */         binder = new DataBinder();
/*      */ 
/* 1076 */         ResourceUtils.serializeDataBinder(FileUtils.getDirectory(tempName), FileUtils.getName(tempName), binder, false, true);
/*      */       }
/*      */ 
/* 1080 */       if (binder == null)
/*      */       {
/* 1082 */         return;
/*      */       }
/*      */ 
/* 1085 */       DataResultSet rset = (DataResultSet)binder.getResultSet("Filters");
/* 1086 */       if ((rset == null) || (rset.isEmpty()))
/*      */       {
/* 1088 */         return;
/*      */       }
/*      */ 
/* 1091 */       String[] fields = { "type", "location", "parameter", "loadOrder" };
/* 1092 */       FieldInfo[] info = ResultSetUtils.createInfoList(rset, fields, false);
/* 1093 */       Vector filterList = new IdcVector();
/*      */ 
/* 1095 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*      */       {
/* 1097 */         String type = rset.getStringValue(info[0].m_index);
/* 1098 */         if (!type.equals(filterType))
/*      */           continue;
/* 1100 */         PluginFilterData data = new PluginFilterData();
/* 1101 */         data.m_filterType = type;
/* 1102 */         data.m_location = rset.getStringValue(info[1].m_index);
/* 1103 */         if (info[2].m_index >= 0)
/*      */         {
/* 1105 */           data.m_parameter = rset.getStringValue(info[2].m_index);
/*      */         }
/* 1107 */         if (info[3].m_index >= 0)
/*      */         {
/* 1109 */           String order = rset.getStringValue(info[3].m_index);
/* 1110 */           data.m_order = PluginFilterLoader.parseOrder(order);
/*      */         }
/*      */ 
/* 1113 */         filterList.addElement(data);
/*      */       }
/*      */ 
/* 1117 */       PluginFilters.registerFilters(filterList);
/*      */     }
/* 1119 */     PluginFilters.filter(filterType, this.m_workspace, null, this);
/*      */   }
/*      */ 
/*      */   protected void processComponentInstallStep(DataBinder tmpBinder, String zipName, String compName, String location, boolean isPrefSetup)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1127 */     String hasPrefStr = null;
/* 1128 */     String hasInstStr = null;
/* 1129 */     String installID = null;
/* 1130 */     String reqFeatures = null;
/* 1131 */     String provFeatures = null;
/* 1132 */     String addComps = null;
/* 1133 */     String version = null;
/* 1134 */     String preventDowngrades = null;
/* 1135 */     String disableComps = null;
/* 1136 */     DataBinder binder = null;
/*      */ 
/* 1139 */     binder = ZipFunctions.extractFileAsDataBinder(zipName, "component/" + location);
/* 1140 */     if (binder == null)
/*      */     {
/* 1142 */       createServiceException(null, LocaleUtils.encodeMessage("csUnableToLoadResourceDefinition", null, zipName));
/*      */     }
/*      */ 
/* 1146 */     hasPrefStr = binder.getLocal("hasPreferenceData");
/* 1147 */     hasInstStr = binder.getLocal("hasInstallStrings");
/* 1148 */     installID = binder.getLocal("installID");
/* 1149 */     reqFeatures = binder.getLocal("requiredFeatures");
/* 1150 */     provFeatures = binder.getLocal("featureExtensions");
/* 1151 */     addComps = binder.getLocal("additionalComponents");
/* 1152 */     disableComps = binder.getLocal("componentsToDisable");
/* 1153 */     version = binder.getLocal("version");
/* 1154 */     preventDowngrades = binder.getLocal("preventAdditionalComponentDowngrade");
/*      */ 
/* 1156 */     ComponentInstaller installer = new ComponentInstaller();
/* 1157 */     installer.checkVersion(compName, binder);
/*      */ 
/* 1159 */     if (isPrefSetup)
/*      */     {
/* 1162 */       ComponentPreferenceData prefData = new ComponentPreferenceData();
/* 1163 */       installer.retrievePreferenceData(prefData, zipName, compName, installID);
/*      */ 
/* 1165 */       ResourceContainer prefResources = new ResourceContainer();
/*      */ 
/* 1167 */       if (DataBinderUtils.getBoolean(this.m_binder, "hasInstallStrings", false))
/*      */       {
/* 1169 */         installer.retrievePreferenceResources(zipName, compName, prefResources);
/*      */       }
/*      */ 
/* 1173 */       prefData.addResultSetsToBinder(prefResources, this.m_binder);
/*      */ 
/* 1175 */       this.m_binder.addResultSet("PreferenceData", prefData.getPreferenceTable());
/*      */     }
/*      */     else
/*      */     {
/* 1179 */       ResultSet rset = tmpBinder.getResultSet("Manifest");
/* 1180 */       this.m_binder.addResultSet("Manifest", rset);
/*      */     }
/*      */ 
/* 1183 */     if ((installID == null) || (installID.length() == 0))
/*      */     {
/* 1185 */       if (isPrefSetup)
/*      */       {
/* 1187 */         createServiceException(null, "!csInstallIdRequired");
/*      */       }
/*      */ 
/* 1190 */       installID = compName;
/*      */     }
/* 1192 */     addToBinder("ComponentName", compName);
/* 1193 */     addToBinder("location", location);
/* 1194 */     addToBinder("hasInstallStrings", hasInstStr);
/* 1195 */     addToBinder("hasPreferenceData", hasPrefStr);
/* 1196 */     addToBinder("installID", installID);
/* 1197 */     addToBinder("requiredFeatures", reqFeatures);
/* 1198 */     addToBinder("featureExtensions", provFeatures);
/* 1199 */     addToBinder("additionalComponents", addComps);
/* 1200 */     addToBinder("preventAdditionalComponentDowngrade", preventDowngrades);
/* 1201 */     addToBinder("componentsToDisable", disableComps);
/* 1202 */     addToBinder("version", version);
/*      */   }
/*      */ 
/*      */   protected void addToBinder(String name, String val)
/*      */   {
/* 1207 */     if (val == null)
/*      */     {
/* 1209 */       val = "";
/*      */     }
/* 1211 */     this.m_binder.putLocal(name, val);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void setRestartRequired()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1219 */     setRestartRequired("true");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void cancelComponentInstall()
/*      */     throws ServiceException
/*      */   {
/* 1228 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentUpload", true))
/*      */     {
/* 1230 */       createServiceException(null, "!csAdminUploadNotAllowed");
/*      */     }
/*      */ 
/* 1234 */     String zipName = parseManifestZipPath();
/* 1235 */     if (FileUtils.checkFile(zipName, true, false) >= 0)
/*      */     {
/* 1237 */       FileUtils.deleteFile(zipName);
/*      */     }
/* 1239 */     String msg = "!csAdminInstallCancelled";
/* 1240 */     this.m_binder.putLocal("StatusMessageKey", msg);
/* 1241 */     this.m_binder.putLocal("StatusMessage", msg);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void downloadComponent()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1255 */     setConditionVar("SuppressCacheControlHeader", true);
/*      */ 
/* 1258 */     if (!SharedObjects.getEnvValueAsBoolean("AllowComponentDownload", true))
/*      */     {
/* 1260 */       createServiceException(null, "!csAdminDownloadNotAllowed");
/*      */     }
/*      */ 
/* 1263 */     String componentName = this.m_binder.getLocal("ComponentName");
/* 1264 */     if (componentName == null)
/*      */     {
/* 1266 */       createServiceException(null, "!csComponentNameRequired");
/*      */     }
/*      */ 
/* 1270 */     ComponentListManager.init();
/* 1271 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/*      */ 
/* 1273 */     DataResultSet components = compLE.getComponentSet();
/* 1274 */     int nameIndex = ResultSetUtils.getIndexMustExist(components, "name");
/* 1275 */     Vector row = components.findRow(nameIndex, componentName);
/* 1276 */     if (row == null)
/*      */     {
/* 1278 */       String msg = LocaleUtils.encodeMessage("csAdminComponentDoesNotExist", null, componentName);
/*      */ 
/* 1280 */       throw new DataException(msg);
/*      */     }
/* 1282 */     Map map = components.getCurrentRowMap();
/*      */ 
/* 1285 */     if (!ComponentLocationUtils.isLocal(map))
/*      */     {
/* 1287 */       createServiceException(null, "!csAdminComponentMustBeLocal");
/*      */     }
/*      */ 
/* 1291 */     boolean fileAvailable = false;
/* 1292 */     String compDefFile = ComponentLocationUtils.determineComponentLocation(map, 1);
/*      */ 
/* 1294 */     String compDir = FileUtils.getDirectory(compDefFile);
/* 1295 */     String location = FileUtils.getAbsolutePath(compDir, componentName + ".zip");
/*      */ 
/* 1298 */     boolean buildBeforeDownload = SharedObjects.getEnvValueAsBoolean("BuildComponentBeforeDownload", false);
/*      */ 
/* 1300 */     if (buildBeforeDownload)
/*      */     {
/* 1303 */       ComponentInstaller installer = new ComponentInstaller();
/* 1304 */       boolean isUpdate = false;
/* 1305 */       String buildSettings = FileUtils.getAbsolutePath(compDir, "manifest.hda");
/*      */ 
/* 1308 */       if (FileUtils.checkFile(buildSettings, true, false) < 0)
/*      */       {
/* 1310 */         throw new DataException(LocaleUtils.encodeMessage("csAdminManifestFileMissing", null, compDir));
/*      */       }
/*      */ 
/* 1315 */       if (FileUtils.checkFile(compDefFile, true, false) < 0)
/*      */       {
/* 1317 */         throw new DataException(LocaleUtils.encodeMessage("csAdminManifestFileMissing", null, compDir));
/*      */       }
/*      */ 
/* 1322 */       DataBinder manifestData = ResourceUtils.readDataBinderFromPath(buildSettings);
/*      */ 
/* 1325 */       DataBinder compData = ResourceUtils.readDataBinderFromPath(compDefFile);
/*      */ 
/* 1327 */       if (manifestData == null)
/*      */       {
/* 1329 */         throw new DataException(LocaleUtils.encodeMessage("csAdminUnableToReadManifestFile", null, compDir));
/*      */       }
/*      */ 
/* 1333 */       Map args = new HashMap();
/* 1334 */       String backupName = FileUtils.getDirectory(location) + "/" + componentName + "_backup_" + Long.toString(new Date().getTime()) + ".zip";
/*      */ 
/* 1336 */       args.put("Build", "true");
/* 1337 */       args.put("NewZipName", location);
/* 1338 */       args.put("BackupZipName", backupName);
/*      */ 
/* 1341 */       if (FileUtils.checkFile(location, true, false) >= 0)
/*      */       {
/* 1343 */         FileUtils.renameFile(location, backupName);
/*      */       }
/*      */ 
/* 1347 */       installer.initEx(componentName, compData, manifestData, args);
/* 1348 */       installer.executeManifest();
/*      */ 
/* 1351 */       Map exceptions = installer.getExceptions();
/* 1352 */       if ((exceptions != null) && (!exceptions.isEmpty()))
/*      */       {
/* 1354 */         ServiceException se = new ServiceException("!csAdminUnableToBuildComponent");
/* 1355 */         for (Iterator i$ = exceptions.entrySet().iterator(); i$.hasNext(); ) { Object entry = i$.next();
/*      */ 
/* 1357 */           Exception ex = (Exception)((Map.Entry)entry).getValue();
/* 1358 */           se.addCause(ex); }
/*      */ 
/*      */ 
/* 1362 */         if (isUpdate)
/*      */         {
/* 1364 */           FileUtils.renameFile(backupName, location);
/*      */         }
/*      */ 
/* 1367 */         throw se;
/*      */       }
/*      */ 
/* 1370 */       fileAvailable = true;
/*      */     }
/* 1374 */     else if (FileUtils.checkFile(location, true, false) >= 0)
/*      */     {
/* 1376 */       fileAvailable = true;
/*      */     }
/*      */     else
/*      */     {
/* 1380 */       location = FileUtils.getAbsolutePath(compDir, "manifest.zip");
/* 1381 */       if (FileUtils.checkFile(location, true, false) >= 0)
/*      */       {
/* 1383 */         fileAvailable = true;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1388 */     String downloadName = FileUtils.getName(location);
/* 1389 */     DataStreamWrapper streamWrapper = getDownloadStream(true);
/* 1390 */     streamWrapper.setSimpleFileData(location, downloadName, "application/zip");
/* 1391 */     streamWrapper.m_clientFileName = downloadName;
/*      */ 
/* 1394 */     streamWrapper.m_useStream = fileAvailable;
/* 1395 */     streamWrapper.m_determinedExistence = true;
/* 1396 */     streamWrapper.m_streamLocationExists = fileAvailable;
/*      */ 
/* 1398 */     if (fileAvailable)
/*      */       return;
/* 1400 */     String error = LocaleUtils.encodeMessage("csAdminDownloadFileDoesNotExist", null, componentName, location);
/*      */ 
/* 1402 */     createServiceException(null, error);
/*      */   }
/*      */ 
/*      */   protected SystemPropertiesEditor prepareSysPropsEditor(SystemPropertiesEditor editor, String intradocDir, String encoding, boolean doWrite, String idcFile)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1415 */     return prepareSysPropsEditorEx(editor, intradocDir, encoding, doWrite, idcFile);
/*      */   }
/*      */ 
/*      */   protected SystemPropertiesEditor prepareSysPropsEditorEx(SystemPropertiesEditor editor, String intradocDir, String encoding, boolean doWrite, String idcFile)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1427 */     return editor;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadSysProps()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1437 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/* 1438 */     String idcFile = SystemUtils.getCfgFilePath();
/*      */ 
/* 1441 */     String encoding = DataSerializeUtils.getSystemEncoding();
/*      */ 
/* 1444 */     SystemPropertiesEditor editor = new SystemPropertiesEditor(idcFile);
/* 1445 */     editor = prepareSysPropsEditor(editor, intradocDir, encoding, false, idcFile);
/* 1446 */     editor.loadProperties();
/* 1447 */     editor.closeAllStreams();
/* 1448 */     Properties props = this.m_binder.getLocalData();
/* 1449 */     Properties newProps = editor.getConfig();
/* 1450 */     Properties idcProps = editor.getIdc();
/*      */ 
/* 1453 */     for (int i = 0; i < NOT_PASSED_VARS.length; ++i)
/*      */     {
/* 1455 */       newProps.remove(NOT_PASSED_VARS[i]);
/* 1456 */       idcProps.remove(NOT_PASSED_VARS[i]);
/*      */     }
/*      */ 
/* 1459 */     DataBinder.mergeHashTables(props, idcProps);
/* 1460 */     DataBinder.mergeHashTables(props, newProps);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void saveSysProps()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1469 */     this.m_binder.removeLocal("IdcService");
/* 1470 */     this.m_binder.removeLocal("CurrentTab");
/* 1471 */     Properties cfgProps = this.m_binder.getLocalData();
/*      */ 
/* 1475 */     Properties tempProps = new Properties(cfgProps);
/* 1476 */     this.m_binder.setLocalData(tempProps);
/*      */ 
/* 1479 */     String majRev = this.m_binder.getLocal("MajorRevSeq");
/* 1480 */     String minRev = this.m_binder.getLocal("MinorRevSeq");
/*      */ 
/* 1482 */     boolean minRevPresent = (minRev != null) && (minRev.length() > 0);
/* 1483 */     boolean majRevPresent = (majRev != null) && (majRev.length() > 0);
/* 1484 */     if ((minRevPresent) && (!majRevPresent))
/*      */     {
/* 1486 */       throw new ServiceException("!csMajorRevRangeMissing");
/*      */     }
/* 1488 */     if (majRevPresent)
/*      */     {
/* 1490 */       String oldMajRev = "";
/* 1491 */       String oldMinRev = "";
/*      */       try
/*      */       {
/* 1497 */         oldMajRev = SharedObjects.getEnvironmentValue("MajorRevSeq");
/* 1498 */         SharedObjects.putEnvironmentValue("MajorRevSeq", majRev);
/* 1499 */         if (minRevPresent)
/*      */         {
/* 1501 */           oldMinRev = SharedObjects.getEnvironmentValue("MinorRevSeq");
/* 1502 */           SharedObjects.putEnvironmentValue("MinorRevSeq", minRev);
/*      */         }
/* 1504 */         RevisionSpec.initImplementor();
/*      */       }
/*      */       catch (ServiceException s)
/*      */       {
/* 1510 */         if (oldMajRev == null)
/*      */         {
/* 1512 */           SharedObjects.removeEnvironmentValue("MajorRevSeq");
/*      */         }
/*      */         else
/*      */         {
/* 1516 */           SharedObjects.putEnvironmentValue("MajorRevSeq", oldMajRev);
/*      */         }
/*      */ 
/* 1519 */         if (minRevPresent)
/*      */         {
/* 1521 */           if (oldMajRev == null)
/*      */           {
/* 1523 */             SharedObjects.removeEnvironmentValue("MinorRevSeq");
/*      */           }
/*      */           else
/*      */           {
/* 1527 */             SharedObjects.putEnvironmentValue("MinorRevSeq", oldMinRev);
/*      */           }
/*      */         }
/* 1530 */         throw s;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1535 */     String autoNum = this.m_binder.getLocal("AutoNumberPrefix");
/* 1536 */     if ((autoNum != null) && 
/* 1539 */       (autoNum.indexOf("<$") < 0))
/*      */     {
/* 1542 */       int result = Validation.checkUrlFileSegment(autoNum);
/* 1543 */       String error = null;
/* 1544 */       switch (result)
/*      */       {
/*      */       case -2:
/* 1547 */         error = "!csAutoPrefixNoSpaces";
/* 1548 */         break;
/*      */       case -3:
/* 1551 */         error = LocaleUtils.encodeMessage("csAutoPrefixIllegalChars", null, ";/\\?:@&=+\"#%<>*~|[]ıİ");
/*      */       }
/*      */ 
/* 1556 */       if (autoNum.length() > 15)
/*      */       {
/* 1558 */         error = "!csAutoPrefixTooLong";
/*      */       }
/*      */ 
/* 1561 */       if (error != null)
/*      */       {
/* 1563 */         throw new ServiceException(error);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1568 */     String ntlmSecurityType = this.m_binder.getLocal("NtlmSecurityType");
/* 1569 */     if (ntlmSecurityType != null)
/*      */     {
/* 1572 */       boolean useNtlm = ntlmSecurityType.equalsIgnoreCase("ntlm");
/* 1573 */       boolean useAdsi = ntlmSecurityType.equalsIgnoreCase("adsi");
/* 1574 */       cfgProps.put("UseNtlm", (useNtlm) ? "Yes" : "");
/* 1575 */       cfgProps.put("UseAdsi", (useAdsi) ? "Yes" : "");
/* 1576 */       cfgProps.put("NtlmSecurityEnabled", ((useNtlm) || (useAdsi)) ? "Yes" : "");
/*      */     }
/*      */ 
/* 1581 */     String intradocDir = LegacyDirectoryLocator.getIntradocDir();
/* 1582 */     String idcFile = SystemUtils.getCfgFilePath();
/*      */ 
/* 1585 */     String encoding = DataSerializeUtils.getSystemEncoding();
/*      */ 
/* 1588 */     SystemPropertiesEditor editor = new SystemPropertiesEditor(idcFile);
/* 1589 */     editor = prepareSysPropsEditor(editor, intradocDir, encoding, true, idcFile);
/* 1590 */     editor.loadProperties();
/*      */     try
/*      */     {
/* 1595 */       String extras = cfgProps.getProperty("cfgExtraVariables");
/* 1596 */       if (extras != null)
/*      */       {
/* 1598 */         Properties newExtraProps = new Properties();
/* 1599 */         FileUtils.loadProperties(newExtraProps, new ByteArrayInputStream(extras.getBytes(FileUtils.m_javaSystemEncoding)));
/*      */ 
/* 1603 */         for (int i = 0; i < UNSETTABLE_VARS.length; ++i)
/*      */         {
/* 1605 */           String newValue = newExtraProps.getProperty(UNSETTABLE_VARS[i]);
/* 1606 */           String oldValue = editor.searchForValue(UNSETTABLE_VARS[i]);
/* 1607 */           if ((newValue == null) || ((oldValue != null) && (newValue.equals(oldValue))))
/*      */             continue;
/* 1609 */           String msg = LocaleUtils.encodeMessage("csAdminIllegalChange", null, UNSETTABLE_VARS[i], oldValue, newValue);
/*      */ 
/* 1611 */           createServiceException(null, msg);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1618 */       createServiceException(null, "!csAdminUnableToParse");
/*      */     }
/*      */ 
/* 1622 */     IdcMessage info = new AdminLogHandler().getLogInfo(cfgProps, editor);
/*      */ 
/* 1624 */     if (info != null)
/*      */     {
/* 1626 */       IdcMessage msg = IdcMessageFactory.lc("csAdminValueChangeLog", new Object[] { cfgProps.get("dUser"), cfgProps.get("IDC_Id") });
/*      */ 
/* 1628 */       msg.m_prior = info;
/* 1629 */       Report.info(null, null, msg);
/*      */     }
/*      */ 
/* 1633 */     editor.mergePropertyValues(null, cfgProps);
/* 1634 */     editor.saveConfig();
/* 1635 */     editor.closeAllStreams();
/* 1636 */     setRestartRequired("true");
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void determineRootPage()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1645 */     String replacementServiceName = computeSimpleServerLandingPage();
/*      */ 
/* 1648 */     this.m_binder.putLocal("isSimple", "1");
/* 1649 */     this.m_requestImplementor.executeReplacementService(this, replacementServiceName);
/*      */   }
/*      */ 
/*      */   public String computeSimpleServerLandingPage()
/*      */   {
/* 1661 */     String landingPage = SharedObjects.getEnvironmentValue("AdminSimpleServerLandingPage");
/* 1662 */     if ((landingPage == null) || (landingPage.length() == 0))
/*      */     {
/* 1664 */       landingPage = "GET_COMPONENT_DATA";
/*      */     }
/* 1666 */     return landingPage;
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadServerStatus()
/*      */     throws ServiceException, DataException
/*      */   {
/* 1676 */     determineSimplifiedServerStatus();
/*      */   }
/*      */ 
/*      */   public String determineSimplifiedServerStatus()
/*      */   {
/* 1683 */     String statusStr = "csServerStatusRunning";
/* 1684 */     this.m_binder.putLocal("IdcServerStatus", statusStr);
/* 1685 */     return statusStr;
/*      */   }
/*      */ 
/*      */   protected void setRestartRequired(String restartRequired)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1694 */     updateInternalRestartRequiredState(restartRequired);
/* 1695 */     String alertId = "csRestartServerToApplyChanges";
/* 1696 */     if (StringUtils.convertToBool(restartRequired, false))
/*      */     {
/* 1698 */       String alertMsg = "<$lcMessage('!csRestartServerToApplyChanges')$>";
/* 1699 */       addAlert(alertId, alertMsg, 1);
/*      */     }
/*      */     else
/*      */     {
/* 1703 */       AlertUtils.deleteAlertSimple(alertId);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void updateInternalRestartRequiredState(String restartRequired)
/*      */   {
/* 1709 */     this.m_binder.putLocal("restartRequired", restartRequired);
/*      */   }
/*      */ 
/*      */   protected String[] computeManifestZipPaths(DataBinder cmpBinder, String componentName)
/*      */     throws ServiceException
/*      */   {
/* 1715 */     String[] paths = new String[2];
/* 1716 */     String dir = ComponentLocationUtils.computeDefaultComponentDir(cmpBinder.getLocalData(), 1, false, new boolean[1]);
/*      */ 
/* 1719 */     String keyedDir = ComponentLocationUtils.computeDefaultComponentDir(cmpBinder.getLocalData(), 1, true, new boolean[1]);
/*      */ 
/* 1724 */     paths[0] = (FileUtils.directorySlashes(dir) + componentName + "/manifest.zip");
/*      */ 
/* 1727 */     paths[1] = (keyedDir + componentName + "/");
/* 1728 */     return paths;
/*      */   }
/*      */ 
/*      */   protected String parseManifestZipPath() throws ServiceException
/*      */   {
/* 1733 */     String location = this.m_binder.getLocal("componentDir");
/* 1734 */     if ((location == null) || (location.length() == 0))
/*      */     {
/* 1736 */       createServiceException(null, "!csComponentLocationRequired");
/*      */     }
/*      */ 
/* 1739 */     String path = FileUtils.computePathFromSubstitutionMap(SharedObjects.getSecureEnvironment(), location) + "manifest.zip";
/*      */ 
/* 1741 */     return path;
/*      */   }
/*      */ 
/*      */   protected static void mergeNewDataOnly(DataResultSet currentSet, DataResultSet newSet, String fieldName, boolean errorOnOverwrite)
/*      */     throws DataException
/*      */   {
/* 1758 */     if (currentSet == null)
/*      */     {
/* 1760 */       if (newSet != null)
/*      */       {
/* 1762 */         currentSet = new DataResultSet();
/* 1763 */         currentSet.copy(newSet);
/*      */       }
/* 1765 */       return;
/*      */     }
/*      */ 
/* 1772 */     int curInd = ResultSetUtils.getIndexMustExist(currentSet, fieldName);
/* 1773 */     int newInd = ResultSetUtils.getIndexMustExist(newSet, fieldName);
/* 1774 */     for (currentSet.first(); currentSet.isRowPresent(); currentSet.next())
/*      */     {
/* 1776 */       String curFieldValue = currentSet.getStringValue(curInd);
/*      */ 
/* 1779 */       for (newSet.first(); newSet.isRowPresent(); newSet.next())
/*      */       {
/* 1781 */         String newFieldValue = newSet.getStringValue(newInd);
/* 1782 */         if (!newFieldValue.equals(curFieldValue)) {
/*      */           continue;
/*      */         }
/* 1785 */         Properties currentProps = currentSet.getCurrentRowProps();
/* 1786 */         Properties newProps = newSet.getCurrentRowProps();
/* 1787 */         boolean overwrite = false;
/*      */ 
/* 1789 */         Enumeration e = currentProps.keys();
/*      */         while (true) { if (!e.hasMoreElements())
/*      */             break label224;
/* 1792 */           String key = (String)e.nextElement();
/* 1793 */           String currentKeyValue = currentProps.getProperty(key);
/* 1794 */           String newKeyValue = newProps.getProperty(key);
/*      */ 
/* 1796 */           if (currentKeyValue != null)
/*      */           {
/* 1798 */             if ((newKeyValue == null) || (!newKeyValue.equals(currentKeyValue))) {
/* 1799 */               overwrite = true;
/*      */             }
/*      */ 
/*      */           }
/* 1803 */           else if (newKeyValue != null) {
/* 1804 */             overwrite = true;
/*      */           }
/* 1806 */           label224: if ((overwrite) && (errorOnOverwrite))
/*      */           {
/* 1808 */             Report.warning(null, null, "csCannotMergeSetsFieldValueDoesNotMatch", new Object[] { curFieldValue, key });
/*      */           }
/*      */  }
/*      */ 
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1818 */     currentSet.merge(fieldName, newSet, false);
/*      */   }
/*      */ 
/*      */   @IdcServiceAction
/*      */   public void loadLog() throws ServiceException
/*      */   {
/* 1824 */     ComponentInstaller installer = new ComponentInstaller();
/* 1825 */     installer.setLogLocation(this.m_binder.getLocal("logDataDir"), this.m_binder.getLocal("logFileName"));
/* 1826 */     installer.readLog();
/* 1827 */     this.m_binder.merge(installer.getLog());
/*      */   }
/*      */ 
/*      */   protected void addAlert(String alertId, String alertMsg, int typeFlag) throws ServiceException, DataException
/*      */   {
/* 1832 */     boolean alertExists = AlertUtils.existsAlert(alertId, typeFlag);
/* 1833 */     if (alertExists)
/*      */       return;
/* 1835 */     DataBinder binder = new DataBinder();
/* 1836 */     binder.putLocal("alertId", alertId);
/* 1837 */     binder.putLocal("alertMsg", alertMsg);
/* 1838 */     binder.putLocal("flags", "" + typeFlag);
/* 1839 */     binder.putLocal("role", "admin");
/* 1840 */     AlertUtils.setAlert(binder);
/*      */ 
/* 1842 */     String msg = LocaleUtils.encodeMessage(alertId, null);
/* 1843 */     Report.warning(null, msg, null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1849 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104909 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.AdminServiceHandler
 * JD-Core Version:    0.5.4
 */