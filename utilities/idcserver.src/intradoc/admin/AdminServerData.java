/*      */ package intradoc.admin;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcStringBuilder;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.filestore.IdcFileDescriptor;
/*      */ import intradoc.filestore.config.ConfigFileStore;
/*      */ import intradoc.filestore.config.ConfigFileUtilities;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.StringReader;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class AdminServerData extends DataResultSet
/*      */ {
/*   43 */   protected Hashtable m_serverMap = null;
/*      */ 
/*   49 */   protected DataResultSet m_serverActions = null;
/*      */ 
/*   55 */   protected DataBinder m_serverData = null;
/*      */   protected long m_lastModified;
/*      */   protected boolean m_isSimpleAdmin;
/*   73 */   protected boolean m_isInit = false;
/*      */ 
/*      */   public AdminServerData()
/*      */   {
/*   80 */     this.m_serverMap = new Hashtable();
/*   81 */     this.m_serverData = new DataBinder();
/*      */   }
/*      */ 
/*      */   public void save(ConfigFileUtilities CFU)
/*      */     throws DataException, ServiceException
/*      */   {
/*   93 */     OutputStream out = null;
/*      */     try
/*      */     {
/*   96 */       out = CFU.getOutputStreamByName("$AdminServersDir/servers.hda", null);
/*   97 */       ResourceUtils.writeDataBinderToStream(out, this.m_serverData, 0, CFU.getFilesystemPathByName("$AdminServersDir/servers.hda"));
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*  106 */       FileUtils.closeObject(out);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void load(ConfigFileUtilities CFU) throws DataException, ServiceException
/*      */   {
/*  119 */     IdcFileDescriptor serversDescriptor = CFU.createDescriptorByName("$AdminServersDir/servers.hda", null);
/*      */     Map storageData;
/*      */     try
/*      */     {
/*  122 */       storageData = CFU.m_CFS.getStorageData(serversDescriptor, null, null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  126 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  129 */     if (0L != this.m_lastModified)
/*      */     {
/*  132 */       Object lastModifiedObj = storageData.get("lastModified");
/*  133 */       String lastModifiedString = (String)lastModifiedObj;
/*  134 */       long lastModified = NumberUtils.parseLong(lastModifiedString, 0L);
/*  135 */       if (lastModified == this.m_lastModified)
/*      */       {
/*  137 */         return;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  142 */     String doesExistStr = (String)storageData.get("fileExists");
/*  143 */     boolean doesExist = StringUtils.convertToBool(doesExistStr, false);
/*  144 */     if (doesExist)
/*      */     {
/*  146 */       Map args = CFU.createMapFromOptionsString("mustExist", ',');
/*  147 */       InputStream in = null;
/*      */       try
/*      */       {
/*  150 */         in = CFU.m_CFS.getInputStream(serversDescriptor, args);
/*  151 */         ResourceUtils.readDataBinderFromStream(in, this.m_serverData, 0, serversDescriptor.getProperty("path"));
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*      */         String msg;
/*  158 */         throw new ServiceException(msg, e);
/*      */       }
/*      */       finally
/*      */       {
/*  162 */         FileUtils.closeObject(in);
/*      */       }
/*      */     }
/*  165 */     updateResultSets(CFU);
/*  166 */     loadFromDataBinder(this.m_serverData, CFU);
/*      */     try
/*      */     {
/*  171 */       String attr = CFU.getStorageDataAttribute(serversDescriptor, "lastModified", null);
/*      */ 
/*  174 */       this.m_lastModified = NumberUtils.parseLong(attr, 0L);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  178 */       throw new ServiceException(e);
/*      */     }
/*  180 */     SharedObjects.putTable("ServerDefinition", this);
/*      */   }
/*      */ 
/*      */   public void initAsSimpleServer()
/*      */   {
/*  189 */     this.m_isSimpleAdmin = true;
/*  190 */     DataBinder thisServerBinder = new DataBinder(SharedObjects.getSecureEnvironment());
/*      */ 
/*  194 */     this.m_serverMap.put("", thisServerBinder);
/*  195 */     SharedObjects.putTable("ServerDefinition", this);
/*      */   }
/*      */ 
/*      */   protected void updateResultSets(ConfigFileUtilities CFU)
/*      */     throws DataException, ServiceException
/*      */   {
/*  205 */     boolean changed = false;
/*  206 */     String idcAdminName = SharedObjects.getEnvironmentValue("IDC_Admin_Name");
/*  207 */     if (idcAdminName == null)
/*      */     {
/*  209 */       String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "IDC_Admin_Name");
/*  210 */       throw new DataException(msg);
/*      */     }
/*  212 */     String clusterNodeName = SharedObjects.getEnvironmentValue("ClusterNodeName");
/*  213 */     String clusterGroup = SharedObjects.getEnvironmentValue("ClusterGroup");
/*  214 */     String idcAdminId = idcAdminName;
/*  215 */     if (clusterNodeName != null)
/*      */     {
/*  217 */       idcAdminId = idcAdminName + "-" + clusterNodeName;
/*  218 */       this.m_serverData.putLocal("ClusterNodeName", clusterNodeName);
/*  219 */       if (clusterGroup == null)
/*      */       {
/*  221 */         clusterGroup = idcAdminName;
/*  222 */         SharedObjects.putEnvironmentValue("ClusterGroup", idcAdminName);
/*  223 */         this.m_serverData.putLocal("ClusterGroup", idcAdminName);
/*      */       }
/*      */     }
/*  226 */     this.m_serverData.putLocal("IDC_Admin_Id", idcAdminId);
/*  227 */     DataResultSet adminServers = (DataResultSet)this.m_serverData.getResultSet("AdminServers");
/*  228 */     if (adminServers == null)
/*      */     {
/*  230 */       adminServers = SharedObjects.getTable("DefaultAdminServerDefinition");
/*  231 */       this.m_serverData.addResultSet("AdminServers", adminServers);
/*  232 */       addCurrentServerToKnownAdminServers(adminServers);
/*  233 */       changed = true;
/*      */     }
/*      */     else
/*      */     {
/*  238 */       String id = ResultSetUtils.findValue(adminServers, "IDC_Admin_Id", idcAdminId, "IDC_Admin_Id");
/*  239 */       if (id == null)
/*      */       {
/*  241 */         addCurrentServerToKnownAdminServers(adminServers);
/*  242 */         changed = true;
/*      */       }
/*      */ 
/*  245 */       ResultSet defaults = SharedObjects.getTable("DefaultAdminServerDefinition");
/*  246 */       int oldNum = adminServers.getNumFields();
/*  247 */       int numFields = defaults.getNumFields();
/*  248 */       if (oldNum != numFields) {
/*  249 */         changed = true;
/*      */       }
/*  251 */       String[] keys = new String[numFields];
/*  252 */       String[] defaultValues = new String[numFields];
/*  253 */       for (int i = 0; i < numFields; ++i)
/*      */       {
/*  255 */         keys[i] = defaults.getFieldName(i);
/*  256 */         defaultValues[i] = this.m_serverData.getAllowMissing(keys[i]);
/*      */       }
/*  258 */       ResultSetUtils.addColumnsWithDefaultValues(adminServers, null, defaultValues, keys);
/*      */     }
/*      */ 
/*  262 */     DataResultSet servers = (DataResultSet)this.m_serverData.getResultSet("ServerDefinition");
/*  263 */     if (servers == null)
/*      */     {
/*  265 */       servers = SharedObjects.getTable("DefaultServerDefinition");
/*  266 */       this.m_serverData.addResultSet("ServerDefinition", servers);
/*  267 */       copy(servers);
/*  268 */       changed = true;
/*      */     }
/*      */     else
/*      */     {
/*  272 */       int numCols = servers.getNumFields();
/*  273 */       DataBinder data = new DataBinder(SharedObjects.getSecureEnvironment());
/*  274 */       data.addResultSet("rset", servers);
/*  275 */       String[] lookupKeys = { "IDC_Name" };
/*  276 */       String[] columnName = { "IDC_Id" };
/*  277 */       ResultSetUtils.addColumnsWithDefaultValues(servers, data, lookupKeys, columnName);
/*  278 */       String[] defaultValues = { idcAdminName, clusterGroup, null };
/*  279 */       String[] columnNames = { "IDC_Admin_Name", "ClusterGroup", "ClusterNodeName" };
/*  280 */       ResultSetUtils.addColumnsWithDefaultValues(servers, data, defaultValues, columnNames);
/*  281 */       int newNumCols = servers.getNumFields();
/*  282 */       if (numCols != newNumCols)
/*      */       {
/*  284 */         copy(servers);
/*  285 */         changed = true;
/*      */       }
/*      */     }
/*      */ 
/*  289 */     this.m_serverData.m_localData.clear();
/*  290 */     if (!changed)
/*      */       return;
/*  292 */     save(CFU);
/*      */   }
/*      */ 
/*      */   protected void addCurrentServerToKnownAdminServers(DataResultSet knownServers)
/*      */   {
/*  298 */     int numFields = knownServers.getNumFields();
/*  299 */     Vector newRow = knownServers.createEmptyRow();
/*  300 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/*  302 */       String key = knownServers.getFieldName(i);
/*  303 */       String value = this.m_serverData.getAllowMissing(key);
/*  304 */       if (value == null)
/*  305 */         value = SharedObjects.getEnvironmentValue(key);
/*  306 */       if (value == null)
/*  307 */         value = "";
/*  308 */       newRow.setElementAt(value, i);
/*      */     }
/*  310 */     knownServers.addRow(newRow);
/*      */   }
/*      */ 
/*      */   protected void loadFromDataBinder(DataBinder binder, ConfigFileUtilities CFU)
/*      */     throws DataException, ServiceException
/*      */   {
/*  321 */     boolean isWin = EnvUtils.isFamily("windows");
/*      */ 
/*  324 */     this.m_serverActions = ((DataResultSet)binder.getResultSet("AdminAction"));
/*  325 */     if (this.m_serverActions == null)
/*      */     {
/*  327 */       String defaultAdminActionName = (isWin) ? "DefaultWinAdminAction" : "DefaultUnixAdminAction";
/*      */ 
/*  329 */       this.m_serverActions = SharedObjects.getTable(defaultAdminActionName);
/*  330 */       binder.addResultSet("AdminAction", this.m_serverActions);
/*      */     }
/*      */ 
/*  333 */     DataResultSet drset = (DataResultSet)binder.getResultSet("ServerDefinition");
/*  334 */     copy(drset);
/*      */ 
/*  337 */     Hashtable tempTable = new Hashtable();
/*      */ 
/*  339 */     int idIndex = ResultSetUtils.getIndexMustExist(drset, "IDC_Id");
/*  340 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  342 */       String serverName = drset.getStringValue(idIndex);
/*  343 */       Properties serverProps = drset.getCurrentRowProps();
/*      */       try
/*      */       {
/*  347 */         DataBinder serverBinder = loadServer(serverName, serverProps, CFU);
/*      */ 
/*  349 */         tempTable.put(serverName, serverBinder);
/*      */       }
/*      */       catch (ServiceException e)
/*      */       {
/*  353 */         Report.error("system", e, null);
/*      */       }
/*      */     }
/*  356 */     this.m_serverMap = tempTable;
/*      */   }
/*      */ 
/*      */   protected DataBinder loadServer(String serverName, Properties serverProps, ConfigFileUtilities CFU)
/*      */     throws DataException, ServiceException
/*      */   {
/*  372 */     DataBinder returnBinder = null;
/*  373 */     DataBinder serverBinder = new DataBinder();
/*  374 */     serverBinder.setLocalData(serverProps);
/*      */ 
/*  377 */     String serverPath = computeServerDataPath(serverName);
/*  378 */     Map args = CFU.createMapFromOptionsString("mustExist", ',');
/*  379 */     boolean result = false;
/*      */     try
/*      */     {
/*  382 */       result = CFU.readDataBinderFromName(serverPath, serverBinder, args);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  386 */       throw new ServiceException(e);
/*      */     }
/*  388 */     if (result)
/*      */     {
/*  391 */       boolean doActionsMerge = null == serverBinder.getResultSet("AdminAction");
/*  392 */       if (doActionsMerge)
/*      */       {
/*  394 */         serverBinder.addResultSet("AdminAction", this.m_serverActions);
/*      */       }
/*      */ 
/*  398 */       updateServerDataHDAFile(serverBinder, serverProps, serverName, doActionsMerge, CFU);
/*  399 */       returnBinder = serverBinder;
/*      */     }
/*      */ 
/*  402 */     return returnBinder;
/*      */   }
/*      */ 
/*      */   protected void updateServerDataHDAFile(DataBinder serverData, Properties serverProps, String serverId, boolean hasChanged, ConfigFileUtilities CFU)
/*      */     throws DataException, ServiceException
/*      */   {
/*  413 */     String serverPath = computeServerDataPath(serverId);
/*  414 */     IdcFileDescriptor desc = CFU.createDescriptorByName(serverPath, null);
/*  415 */     if (!hasChanged)
/*      */     {
/*      */       try
/*      */       {
/*  419 */         String existsStr = CFU.getStorageDataAttribute(desc, "fileExists", null);
/*      */ 
/*  421 */         if (!StringUtils.convertToBool(existsStr, false))
/*      */         {
/*  423 */           hasChanged = true;
/*      */         }
/*      */       }
/*      */       catch (IOException e)
/*      */       {
/*  428 */         hasChanged = true;
/*      */       }
/*      */ 
/*  431 */       String id = serverData.getAllowMissing("IDC_Id");
/*  432 */       if ((id == null) || (id.length() == 0))
/*      */       {
/*  434 */         id = serverProps.getProperty("IDC_Id");
/*  435 */         serverData.putLocal("IDC_Id", id);
/*  436 */         hasChanged = true;
/*      */       }
/*      */     }
/*      */ 
/*  440 */     if (!hasChanged)
/*      */       return;
/*  442 */     writeServerData(serverId, serverData, CFU);
/*      */   }
/*      */ 
/*      */   protected void serializeAdminServersData(DataBinder binder, ConfigFileUtilities CFU)
/*      */     throws ServiceException, DataException
/*      */   {
/*  452 */     DataResultSet fullServerData = (DataResultSet)binder.removeResultSet("FullServerData");
/*  453 */     if (fullServerData != null)
/*      */     {
/*  455 */       for (fullServerData.first(); fullServerData.isRowPresent(); fullServerData.next())
/*      */       {
/*  457 */         String serverId = fullServerData.getStringValue(0);
/*      */         try
/*      */         {
/*  460 */           String serverDataStr = fullServerData.getStringValue(1);
/*  461 */           DataBinder serverData = new DataBinder();
/*  462 */           serverData.receive(new BufferedReader(new StringReader(serverDataStr)));
/*  463 */           serverData.putLocal("IDC_Id", serverId);
/*      */ 
/*  465 */           updateServerDataHDAFile(serverData, null, serverId, false, CFU);
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/*  469 */           String msg = LocaleUtils.encodeMessage("syUnableToParse2", null);
/*  470 */           DataException de = new DataException(msg);
/*  471 */           SystemUtils.setExceptionCause(de, e);
/*  472 */           throw de;
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  478 */     save(CFU);
/*  479 */     load(CFU);
/*      */   }
/*      */ 
/*      */   protected void setServerSpecificUrls(DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  487 */     String idcID = binder.getAllowMissing("IDC_Id");
/*  488 */     String remotePath = binder.getLocal("RemoteAbsoluteHttpCgiPath");
/*  489 */     String remoteWebRoot = binder.getLocal("RemoteAbsoluteWebRoot");
/*      */ 
/*  491 */     Properties serverData = getLocalData(idcID);
/*  492 */     String serverAddress = serverData.getProperty("HttpServerAddress");
/*  493 */     if (null == serverAddress)
/*      */     {
/*  495 */       String serverPath = computeServerDataPath(idcID);
/*  496 */       String msg = LocaleUtils.encodeMessage("syMissingArgument2", null, "HttpServerAddress", serverPath);
/*      */ 
/*  498 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  501 */     boolean isSSL = StringUtils.convertToBool(SharedObjects.getEnvironmentValue("UseSSL"), false);
/*      */ 
/*  503 */     String httpPrefix = (isSSL) ? "https://" : "http://";
/*  504 */     if (serverAddress.indexOf("//") < 0) {
/*  505 */       serverAddress = httpPrefix + serverAddress;
/*      */     }
/*  507 */     String cgiRoot = serverData.getProperty("HttpRelativeWebRoot");
/*  508 */     cgiRoot = serverData.getProperty("HttpRelativeCgiRoot", cgiRoot);
/*  509 */     remotePath = serverAddress + cgiRoot + serverData.getProperty("CgiFileName");
/*      */ 
/*  511 */     binder.putLocal("RemoteAbsoluteHttpCgiPath", remotePath);
/*      */ 
/*  513 */     remoteWebRoot = serverAddress + serverData.getProperty("HttpRelativeWebRoot");
/*  514 */     binder.putLocal("RemoteAbsoluteWebRoot", remoteWebRoot);
/*      */   }
/*      */ 
/*      */   public void addOrEditServerReference(DataBinder data, ConfigFileUtilities CFU)
/*      */     throws ServiceException, DataException
/*      */   {
/*  524 */     String idcName = data.getLocal("IDC_Id");
/*  525 */     data.removeLocal("dUser");
/*  526 */     data.removeLocal("isEdit");
/*  527 */     data.removeLocal("IdcService");
/*      */ 
/*  530 */     String actions = data.getLocal("serverActions").toLowerCase();
/*  531 */     if ((actions == null) || (actions.toLowerCase().indexOf("query") < 0))
/*      */     {
/*  534 */       throw new DataException("!csAdminQueryMustBeAllowed");
/*      */     }
/*      */ 
/*  539 */     load(CFU);
/*  540 */     if (!serverExists(idcName))
/*      */     {
/*  542 */       addServerReference(data, idcName, CFU);
/*      */     }
/*      */     else
/*      */     {
/*  547 */       data.removeLocal("serverActions");
/*      */ 
/*  550 */       Properties currentData = getLocalData(idcName);
/*  551 */       DataBinder.mergeHashTables(currentData, (Properties)data.getLocalData().clone());
/*      */ 
/*  553 */       setLocalData(idcName, currentData);
/*  554 */       DataBinder curServerData = (DataBinder)this.m_serverMap.get(idcName);
/*      */ 
/*  557 */       writeServerData(idcName, curServerData, CFU);
/*      */ 
/*  560 */       this.m_serverMap.put(idcName, curServerData);
/*      */ 
/*  563 */       int idcIndex = ResultSetUtils.getIndexMustExist(this, "IDC_Id");
/*  564 */       int actIndex = ResultSetUtils.getIndexMustExist(this, "serverActions");
/*  565 */       Vector row = findRow(idcIndex, idcName);
/*  566 */       if (row != null) {
/*  567 */         row.setElementAt(actions, actIndex);
/*      */       }
/*      */ 
/*  570 */       this.m_serverData.addResultSet("ServerDefinition", this);
/*  571 */       save(CFU);
/*      */     }
/*      */ 
/*  574 */     load(CFU);
/*      */   }
/*      */   public void addServerReference(DataBinder data, String idcName, ConfigFileUtilities CFU) throws ServiceException, DataException {
/*  586 */     Enumeration e = this.m_serverMap.keys();
/*      */     String serverName;
/*      */     do {
/*  586 */       if (!e.hasMoreElements())
/*      */         break label61;
/*  588 */       serverName = (String)e.nextElement();
/*  589 */     }while (!serverName.equalsIgnoreCase(idcName));
/*      */ 
/*  591 */     String msg = LocaleUtils.encodeMessage("csServerNameAlreadyExists", null, serverName, idcName);
/*      */ 
/*  593 */     throw new ServiceException(msg);
/*      */ 
/*  598 */     label61: DataBinder newServer = new DataBinder();
/*  599 */     newServer.setLocalData((Properties)data.getLocalData().clone());
/*      */ 
/*  601 */     if (EnvUtils.isFamily("unix"))
/*      */     {
/*  603 */       if (newServer.getLocal("processController") == null)
/*      */       {
/*  605 */         newServer.putLocal("processController", "bin/UnixProcCtrl");
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  610 */       if (newServer.getLocal("processController") == null)
/*      */       {
/*  612 */         newServer.putLocal("processController", "bin/NtProcCtrl.exe");
/*      */       }
/*  614 */       if (newServer.getLocal("serviceName") == null)
/*      */       {
/*  616 */         newServer.putLocal("serviceName", "IdcContentService " + idcName);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  621 */     String desc = newServer.getLocal("description");
/*  622 */     if (desc == null)
/*      */     {
/*  624 */       desc = "";
/*      */     }
/*  626 */     newServer.putLocal("description", desc);
/*      */ 
/*  629 */     newServer.removeLocal("serverActions");
/*      */ 
/*  632 */     writeServerData(idcName, newServer, CFU);
/*      */ 
/*  635 */     this.m_serverMap.put(idcName, newServer);
/*      */ 
/*  638 */     int numFields = getNumFields();
/*  639 */     Vector newRow = createEmptyRow();
/*  640 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/*  642 */       String key = getFieldName(i);
/*  643 */       String value = data.getAllowMissing(key);
/*  644 */       if (value == null)
/*  645 */         value = "";
/*  646 */       newRow.setElementAt(value, i);
/*      */     }
/*  648 */     addRow(newRow);
/*      */ 
/*  651 */     this.m_serverData.addResultSet("ServerDefinition", this);
/*  652 */     save(CFU);
/*      */   }
/*      */ 
/*      */   private String computeServerDataPath(String serverName)
/*      */   {
/*  657 */     IdcStringBuilder pathBuilder = new IdcStringBuilder("$AdminServersDir/");
/*  658 */     pathBuilder.append(serverName);
/*  659 */     pathBuilder.append("/server.hda");
/*  660 */     String serverPath = pathBuilder.toString();
/*  661 */     return serverPath;
/*      */   }
/*      */ 
/*      */   public void writeServerData(String idcName, DataBinder data, ConfigFileUtilities CFU)
/*      */     throws ServiceException
/*      */   {
/*  670 */     String serverPath = computeServerDataPath(idcName);
/*  671 */     if (null == data.getLocal("HttpServerAddress"))
/*      */     {
/*  673 */       String msg = "WARNING: " + serverPath + " is being written without HttpServerAddress!";
/*  674 */       ServiceException e = new ServiceException(msg);
/*  675 */       Report.trace("system", msg, e);
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  680 */       CFU.writeDataBinderToName(serverPath, data, null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  684 */       if (e instanceof ServiceException)
/*      */       {
/*  686 */         throw ((ServiceException)e);
/*      */       }
/*  688 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void removeServerReference(String idcName, ConfigFileUtilities CFU)
/*      */     throws ServiceException, DataException
/*      */   {
/*  701 */     String pathname = null;
/*      */     try
/*      */     {
/*  704 */       pathname = "$AdminDataDir/servers/" + idcName;
/*  705 */       IdcFileDescriptor desc = CFU.createDescriptorByName(pathname, null);
/*  706 */       CFU.m_CFS.deleteFile(desc, null, CFU.m_context);
/*  707 */       pathname = "$AdminDataDir/servers/";
/*  708 */       desc = CFU.createDescriptorByName(pathname, null);
/*  709 */       CFU.m_CFS.deleteFile(desc, null, CFU.m_context);
/*      */     }
/*      */     catch (SecurityException e)
/*      */     {
/*  714 */       String msg = LocaleUtils.encodeMessage("csAdminCannotDelete", null, pathname);
/*  715 */       throw new ServiceException(msg);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  719 */       throw new ServiceException(e);
/*      */     }
/*      */ 
/*  723 */     int idcIndex = ResultSetUtils.getIndexMustExist(this, "IDC_Id");
/*  724 */     if (findRow(idcIndex, idcName) == null)
/*      */     {
/*  726 */       String msg = LocaleUtils.encodeMessage("csAdminUnableToRemoveReference", null, idcName);
/*      */ 
/*  728 */       throw new ServiceException(msg);
/*      */     }
/*  730 */     deleteCurrentRow();
/*      */ 
/*  733 */     this.m_serverMap.remove(idcName);
/*      */ 
/*  736 */     DataBinder serverData = new DataBinder();
/*  737 */     serverData.addResultSet("AdminServers", this.m_serverData.getResultSet("AdminServers"));
/*  738 */     serverData.addResultSet("ServerDefinition", this);
/*  739 */     serverData.addResultSet("AdminAction", this.m_serverActions);
/*      */     try
/*      */     {
/*  742 */       CFU.writeDataBinderToName("$AdminServersDir/servers.hda", serverData, null);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*  746 */       throw new ServiceException(e);
/*      */     }
/*  748 */     load(CFU);
/*      */   }
/*      */ 
/*      */   public String getValue(String server, String key)
/*      */     throws DataException
/*      */   {
/*  756 */     if ((server == null) || (server.length() == 0))
/*      */     {
/*  758 */       server = "";
/*      */     }
/*  760 */     DataBinder serverBinder = (DataBinder)this.m_serverMap.get(server);
/*  761 */     if (serverBinder == null)
/*      */     {
/*  764 */       String target = computeTargetName(server);
/*  765 */       String msg = LocaleUtils.encodeMessage("csAdminUnableToFindConfigData", null, target, "$AdminServersDir");
/*      */ 
/*  767 */       throw new DataException(msg);
/*      */     }
/*      */ 
/*  773 */     return (server.length() == 0) ? serverBinder.getAllowMissing(key) : serverBinder.getLocal(key);
/*      */   }
/*      */ 
/*      */   public String computeTargetName(String server)
/*      */   {
/*  778 */     if ((server == null) || (server.length() == 0))
/*      */     {
/*  780 */       return SharedObjects.getEnvironmentValue("IDC_Name");
/*      */     }
/*  782 */     return server;
/*      */   }
/*      */ 
/*      */   public String getValueMustExist(String server, String key)
/*      */     throws DataException
/*      */   {
/*  791 */     String value = getValue(server, key);
/*  792 */     if (value == null)
/*      */     {
/*  795 */       String msg = LocaleUtils.encodeMessage("csAdminUnableToFindRequiredValue", null, key, server, "$AdminServersDir/" + server + "/server.hda");
/*      */ 
/*  797 */       throw new DataException(msg);
/*      */     }
/*  799 */     return value;
/*      */   }
/*      */ 
/*      */   public String getActionValue(String actionName, String lookup)
/*      */     throws DataException
/*      */   {
/*  808 */     if (actionName == null)
/*      */     {
/*  810 */       throw new DataException("!csAdminNoAction");
/*      */     }
/*  812 */     if (this.m_serverActions == null)
/*      */     {
/*  814 */       throw new DataException("!csAdminNoServerActions");
/*      */     }
/*      */ 
/*  817 */     String retVal = ResultSetUtils.findValue(this.m_serverActions, "actionName", actionName, lookup);
/*      */ 
/*  819 */     return retVal;
/*      */   }
/*      */ 
/*      */   public Properties getLocalData(String serverId)
/*      */   {
/*  827 */     Properties props = null;
/*  828 */     DataBinder binder = (DataBinder)this.m_serverMap.get(serverId);
/*  829 */     if (binder != null)
/*      */     {
/*  831 */       props = (Properties)binder.getLocalData().clone();
/*      */     }
/*  833 */     return props;
/*      */   }
/*      */ 
/*      */   public DataResultSet buildFullInfoForAllServers()
/*      */     throws DataException
/*      */   {
/*  841 */     DataResultSet fullInfo = new DataResultSet(new String[] { "IDC_Id", "binder" });
/*  842 */     Enumeration e = this.m_serverMap.keys();
/*  843 */     Exception error = null;
/*  844 */     while (e.hasMoreElements())
/*      */     {
/*  846 */       String id = (String)e.nextElement();
/*  847 */       DataBinder binder = (DataBinder)this.m_serverMap.get(id);
/*  848 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*      */       try
/*      */       {
/*  851 */         binder.send(sw);
/*  852 */         Vector row = fullInfo.createEmptyRow();
/*  853 */         row.setElementAt(id, 0);
/*  854 */         row.setElementAt(sw.toString(), 1);
/*  855 */         fullInfo.addRow(row);
/*  856 */         FileUtils.close(sw);
/*      */       }
/*      */       catch (IOException ex)
/*      */       {
/*  860 */         error = ex;
/*      */       }
/*      */       finally
/*      */       {
/*  864 */         FileUtils.discard(sw);
/*      */       }
/*  866 */       if (error != null)
/*      */       {
/*  869 */         DataException de = new DataException("!$Unable to format data.");
/*  870 */         SystemUtils.setExceptionCause(de, error);
/*  871 */         throw de;
/*      */       }
/*      */     }
/*  874 */     return fullInfo;
/*      */   }
/*      */ 
/*      */   public void setLocalData(String serverName, Properties props)
/*      */   {
/*  882 */     DataBinder binder = (DataBinder)this.m_serverMap.get(serverName);
/*  883 */     if (binder == null)
/*      */       return;
/*  885 */     binder.setLocalData(props);
/*      */   }
/*      */ 
/*      */   public boolean serverExists(String serverName)
/*      */   {
/*  894 */     boolean result = false;
/*  895 */     if ((serverName == null) || (serverName.length() == 0))
/*      */     {
/*  897 */       result = true;
/*      */     }
/*  901 */     else if (this.m_serverMap.containsKey(serverName))
/*      */     {
/*  903 */       result = true;
/*      */     }
/*      */ 
/*  906 */     return result;
/*      */   }
/*      */ 
/*      */   public void mergeLocalData(String serverName, DataBinder binder)
/*      */     throws DataException
/*      */   {
/*  917 */     if ((serverName == null) || (serverName.length() == 0))
/*      */     {
/*  920 */       binder.putLocal("BinDir", SharedObjects.getEnvironmentValue("BinDir"));
/*  921 */       binder.putLocal("ConfigDir", DirectoryLocator.getConfigDirectory());
/*  922 */       return;
/*      */     }
/*      */ 
/*  925 */     DataBinder serverBinder = (DataBinder)this.m_serverMap.get(serverName);
/*  926 */     if ((serverBinder == null) || (binder == null)) {
/*  927 */       return;
/*      */     }
/*  929 */     Properties serverData = serverBinder.getLocalData();
/*  930 */     Properties localData = binder.getLocalData();
/*  931 */     DataBinder.mergeHashTables(localData, serverData);
/*  932 */     binder.setLocalData(localData);
/*      */ 
/*  937 */     setServerSpecificUrls(binder);
/*      */   }
/*      */ 
/*      */   public boolean validateAction(String server, String action)
/*      */     throws DataException
/*      */   {
/*  945 */     if ((server == null) || (server.length() == 0))
/*      */     {
/*  947 */       throw new DataException("!csAdminTargetNotSpecified");
/*      */     }
/*  949 */     if ((action == null) || (action.length() == 0))
/*      */     {
/*  951 */       throw new DataException("!csAdminTargetNotSpecified2");
/*      */     }
/*  953 */     String validActions = ResultSetUtils.findValue(this, "IDC_Id", server, "serverActions");
/*      */ 
/*  956 */     Vector actionVect = StringUtils.parseArray(validActions, ',', '\\');
/*      */ 
/*  958 */     for (int i = 0; i < actionVect.size(); ++i)
/*      */     {
/*  960 */       if (((String)actionVect.elementAt(i)).trim().equals(action.trim()))
/*      */       {
/*  962 */         return true;
/*      */       }
/*      */     }
/*      */ 
/*  966 */     return false;
/*      */   }
/*      */ 
/*      */   public DataResultSet getActionDataList()
/*      */   {
/*  975 */     DataResultSet drset = new DataResultSet();
/*  976 */     drset.copy(this.m_serverActions);
/*  977 */     return drset;
/*      */   }
/*      */ 
/*      */   public DataResultSet getAdminServers()
/*      */   {
/*  986 */     return (DataResultSet)this.m_serverData.getResultSet("AdminServers");
/*      */   }
/*      */ 
/*      */   public boolean isSimpleAdmin()
/*      */   {
/*  991 */     return this.m_isSimpleAdmin;
/*      */   }
/*      */ 
/*      */   public void setIsSimpleAdmin(boolean isSimpleAdmin)
/*      */   {
/*  996 */     this.m_isSimpleAdmin = isSimpleAdmin;
/*      */   }
/*      */ 
/*      */   public DataResultSet shallowClone()
/*      */   {
/* 1002 */     AdminServerData rset = new AdminServerData();
/* 1003 */     initShallow(rset);
/*      */ 
/* 1005 */     rset.m_serverMap = this.m_serverMap;
/* 1006 */     rset.m_serverActions = this.m_serverActions;
/* 1007 */     rset.m_serverData = this.m_serverData;
/*      */ 
/* 1009 */     rset.m_lastModified = this.m_lastModified;
/*      */ 
/* 1011 */     return rset;
/*      */   }
/*      */ 
/*      */   public void initShallow(DataResultSet rset)
/*      */   {
/* 1020 */     super.initShallow(rset);
/* 1021 */     AdminServerData data = (AdminServerData)rset;
/* 1022 */     data.m_serverActions = this.m_serverActions;
/* 1023 */     data.m_serverMap = this.m_serverMap;
/* 1024 */     data.m_serverData = this.m_serverData;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1029 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101067 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.admin.AdminServerData
 * JD-Core Version:    0.5.4
 */