/*     */ package intradoc.apputilities.installer;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NativeOsUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class QueryInstaller
/*     */   implements SectionInstaller
/*     */ {
/*     */   public Vector m_instanceData;
/*     */   public SysInstaller m_installer;
/*     */   public String[] m_settingList;
/*     */   public String[] m_settingDefaults;
/*     */   public Properties m_knownServers;
/*     */ 
/*     */   public QueryInstaller()
/*     */   {
/*  40 */     this.m_settingList = new String[] { "IntradocDir", "HttpRelativeWebRoot", "IntradocServerPort", "IdcAdminServerPort", "DtmPort", "NtlmSecurityEnabled", "UseAdsi", "HttpServerAddress", "MailServer", "SysAdminAddress", "IsJdbc", "JdbcUser", "JdbcDriver", "JdbcConnectionString", "CgiFileName", "WeblayoutDir", "VaultDir", "WebBrowserPath", "IDC_Name", "JAVA_EXE", "UseMicrosoftVM", "SocketHostAddressSecurityFilter" };
/*     */ 
/*  54 */     this.m_settingDefaults = new String[] { null, "/ucm/", null, null, null, null, null, null, null, null, null, null, null, null, "idc_cgi_isapi.dll", "${IntradocDir}/weblayout", "${IntradocDir}/vault", null, null, null, null, null };
/*     */ 
/*  68 */     this.m_knownServers = new Properties();
/*     */   }
/*     */ 
/*     */   public int installSection(String name, String disposition, String arg, SysInstaller installer, Properties config)
/*     */     throws ServiceException
/*     */   {
/*  74 */     this.m_instanceData = new IdcVector();
/*  75 */     this.m_installer = installer;
/*     */ 
/*  78 */     if (this.m_settingList.length != this.m_settingDefaults.length)
/*     */     {
/*  80 */       Exception e = new StackTrace("m_settingList is not the same length as m_settingDefaults.");
/*     */ 
/*  82 */       Report.trace("install", null, e);
/*     */     }
/*     */ 
/*  88 */     boolean docRefinery = name.equals("query_docrefinery_configuration");
/*     */ 
/*  90 */     String idcDir = installer.m_idcDir + "/admin";
/*  91 */     String adminServerDir = this.m_installer.getInstallValue("AdminServerDir", null);
/*  92 */     String masterServerDir = this.m_installer.getInstallValue("MasterServerDir", null);
/*  93 */     if (masterServerDir != null)
/*     */     {
/*  95 */       idcDir = masterServerDir + "/admin";
/*     */     }
/*  97 */     if (adminServerDir != null)
/*     */     {
/*  99 */       idcDir = adminServerDir;
/*     */     }
/* 101 */     idcDir = FileUtils.directorySlashes(idcDir);
/* 102 */     Vector list = new IdcVector();
/* 103 */     DataBinder binder = new DataBinder();
/*     */     String configFile;
/*     */     String serverFileDir;
/*     */     String serverFileName;
/*     */     String rsetName;
/*     */     String connectionNameField;
/*     */     String connectionPathField;
/*     */     String connectionPathFallbackField;
/*     */     String connectionPathPrefix;
/*     */     String connectionPathFile;
/*     */     boolean isHda;
/*     */     String configFile;
/* 111 */     if (docRefinery)
/*     */     {
/* 113 */       String serverFileDir = idcDir + "/shared";
/* 114 */       String serverFileName = "DRList.hda";
/* 115 */       String rsetName = "DocumentRefineryNames";
/* 116 */       String connectionNameField = "drConnectionName";
/* 117 */       String connectionPathField = "drPath";
/* 118 */       String connectionPathFallbackField = null;
/* 119 */       String connectionPathPrefix = "";
/* 120 */       String connectionPathFile = "intradoc.cfg";
/* 121 */       boolean isHda = false;
/* 122 */       configFile = "connections/main/intradoc.cfg";
/*     */     }
/*     */     else
/*     */     {
/* 126 */       serverFileDir = idcDir + "data/servers";
/* 127 */       serverFileName = "servers.hda";
/* 128 */       rsetName = "ServerDefinition";
/* 129 */       connectionNameField = "IDC_Name";
/* 130 */       connectionPathField = "IDC_Id";
/* 131 */       connectionPathFallbackField = "IDC_Name";
/* 132 */       connectionPathPrefix = idcDir + "data/servers/";
/* 133 */       connectionPathFile = "server.hda";
/* 134 */       isHda = true;
/* 135 */       configFile = "config/config.cfg";
/*     */     }
/*     */ 
/* 138 */     ResourceUtils.serializeDataBinder(serverFileDir, serverFileName, binder, false, false);
/*     */ 
/* 140 */     DataResultSet drset = (DataResultSet)binder.getResultSet(rsetName);
/* 141 */     if (drset != null)
/*     */     {
/* 143 */       FieldInfo serverName = new FieldInfo();
/* 144 */       FieldInfo serverPathComponent = new FieldInfo();
/* 145 */       FieldInfo serverPathFallbackComponent = new FieldInfo();
/* 146 */       if (!drset.getFieldInfo(connectionNameField, serverName))
/*     */       {
/* 148 */         throw new ServiceException(LocaleUtils.encodeMessage("csInstallerQryInvalidField", null, connectionNameField, serverFileName));
/*     */       }
/*     */ 
/* 151 */       if ((!drset.getFieldInfo(connectionPathField, serverPathComponent)) && (!drset.getFieldInfo(connectionPathFallbackField, serverPathFallbackComponent)))
/*     */       {
/* 154 */         throw new ServiceException(LocaleUtils.encodeMessage("csInstallerQryMissingField", null, connectionPathField, serverFileName));
/*     */       }
/*     */ 
/* 157 */       drset.getFieldInfo(connectionPathFallbackField, serverPathFallbackComponent);
/*     */ 
/* 160 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*     */       {
/* 162 */         String connectionName = drset.getStringValue(serverName.m_index);
/*     */         String connectionPath;
/*     */         String connectionPath;
/* 164 */         if (serverPathComponent.m_index >= 0)
/*     */         {
/* 166 */           connectionPath = drset.getStringValue(serverPathComponent.m_index);
/*     */         }
/*     */         else
/*     */         {
/* 171 */           connectionPath = drset.getStringValue(serverPathFallbackComponent.m_index);
/*     */         }
/*     */ 
/* 177 */         Properties overrides = new Properties();
/*     */         String idc;
/*     */         String idc;
/* 179 */         if (isHda)
/*     */         {
/* 181 */           DataBinder serverData = new DataBinder();
/* 182 */           ResourceUtils.serializeDataBinder(connectionPathPrefix + connectionPath, connectionPathFile, serverData, false, true);
/*     */ 
/* 185 */           idc = serverData.getAllowMissing("IntradocDir");
/*     */         }
/*     */         else
/*     */         {
/* 189 */           Vector v1 = new IdcVector();
/* 190 */           Vector v2 = new IdcVector();
/* 191 */           SystemPropertiesEditor.readFile(overrides, v1, v2, connectionPathPrefix + connectionPath + connectionPathFile, null);
/*     */ 
/* 193 */           idc = overrides.getProperty("IntradocDir");
/*     */         }
/*     */ 
/*     */         try
/*     */         {
/* 198 */           if (idc == null)
/*     */           {
/* 200 */             Report.trace("install", LocaleResources.localizeMessage(LocaleUtils.encodeMessage("csInstallerQryIntradocDirCfgMissing", null, connectionName, connectionPathFile), null), null);
/*     */           }
/*     */           else
/*     */           {
/* 205 */             processServer(idc, connectionName, overrides);
/* 206 */             list.addElement(connectionName);
/*     */           }
/*     */         }
/*     */         catch (Exception e) {
/* 210 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 216 */       String cfgPath = installer.computeDestination(configFile);
/* 217 */       Properties overrides = new Properties();
/* 218 */       if (FileUtils.checkFile(cfgPath, true, false) == 0)
/*     */       {
/*     */         String idc;
/*     */         String idc;
/* 221 */         if (docRefinery)
/*     */         {
/* 223 */           Vector v1 = new IdcVector();
/* 224 */           Vector v2 = new IdcVector();
/* 225 */           SystemPropertiesEditor.readFile(overrides, v1, v2, cfgPath, null);
/* 226 */           idc = overrides.getProperty("IntradocDir");
/*     */         }
/*     */         else
/*     */         {
/* 230 */           idc = installer.m_idcDir;
/*     */         }
/*     */         try
/*     */         {
/*     */           String connectionName;
/*     */           String connectionName;
/* 236 */           if (docRefinery)
/*     */           {
/* 238 */             connectionName = "main";
/*     */           }
/*     */           else
/*     */           {
/* 242 */             connectionName = installer.getConfigValue("IDC_Name");
/*     */           }
/* 244 */           processServer(idc, connectionName, overrides);
/* 245 */           list.addElement(connectionName);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 249 */           throw new ServiceException(e);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 254 */     if (this.m_installer.m_utils != null)
/*     */     {
/* 256 */       if (this.m_installer.m_utils.isWindowsRegistrySupported())
/*     */       {
/* 258 */         NativeOsUtils utils = this.m_installer.m_utils;
/* 259 */         String[] stellentProductKeys = { "Content Server", "Dtm Server" };
/*     */ 
/* 265 */         for (int i = 0; i < stellentProductKeys.length; ++i)
/*     */         {
/* 267 */           String stellentProductKey = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Stellent\\" + stellentProductKeys[i];
/*     */ 
/* 269 */           String instances = utils.getRegistryValue(stellentProductKey + "\\ListOfInstalledInstances");
/*     */ 
/* 271 */           if (instances == null)
/*     */           {
/* 273 */             Report.trace("install", "QueryInstaller: registry key ListOfInstalledInstances is null.", null);
/*     */           }
/*     */ 
/* 277 */           if (instances == null)
/*     */             continue;
/* 279 */           Vector v = StringUtils.parseArray(instances, ',', '^');
/* 280 */           for (int j = 0; j < v.size(); ++j)
/*     */           {
/* 282 */             String instance = (String)v.elementAt(j);
/* 283 */             if (instance.length() <= 0)
/*     */               continue;
/* 285 */             String path = utils.getRegistryValue(stellentProductKey + "\\Servers\\" + instance + "\\CoreDir");
/*     */             try
/*     */             {
/* 289 */               String idcName = processServer(path, null, null);
/* 290 */               list.addElement(idcName);
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 295 */               Report.trace("install", null, e);
/*     */             }
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 303 */       String envInstances = this.m_installer.m_utils.getEnv("IdcInstanceList");
/* 304 */       if (envInstances != null)
/*     */       {
/* 306 */         Vector v = StringUtils.parseArray(envInstances, ',', '^');
/* 307 */         for (int i = 0; i < v.size(); ++i)
/*     */         {
/* 309 */           String path = (String)v.elementAt(i);
/*     */           try
/*     */           {
/* 312 */             String idcName = processServer(path, null, null);
/* 313 */             list.addElement(idcName);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 318 */             Report.trace("install", null, e);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 323 */     Writer w = null;
/*     */     try
/*     */     {
/* 326 */       String dest = installer.computeDestination("install/idc_info.txt");
/* 327 */       w = new BufferedWriter(new FileWriter(dest));
/*     */ 
/* 329 */       String l = StringUtils.createString(list, ',', ',');
/* 330 */       w.write(l + "\r\n");
/*     */ 
/* 332 */       for (int i = 0; i < this.m_instanceData.size(); ++i)
/*     */       {
/* 334 */         String line = (String)this.m_instanceData.elementAt(i) + "\r\n";
/* 335 */         w.write(line);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 345 */       FileUtils.closeObject(w);
/*     */     }
/* 347 */     return 0;
/*     */   }
/*     */ 
/*     */   public String processServer(String idcDir, String connectionName, Properties overrides)
/*     */     throws DataException, IOException, ServiceException
/*     */   {
/* 353 */     if (overrides == null)
/*     */     {
/* 355 */       overrides = new Properties();
/*     */     }
/* 357 */     if (idcDir == null)
/*     */     {
/* 359 */       Report.trace("install", "processServer() was passed null IntradocDir", null);
/* 360 */       return null;
/*     */     }
/*     */ 
/* 363 */     Report.trace("install", "QueryInstaller processing server at " + idcDir, null);
/* 364 */     SysInstaller installer = this.m_installer.deriveInstaller(idcDir);
/* 365 */     installer.m_overrideProps = new Properties();
/* 366 */     installer.loadConfig();
/* 367 */     String idcName = installer.getConfigValue("IDC_Name");
/* 368 */     String knownServerPath = null;
/* 369 */     if (idcName != null)
/*     */     {
/* 371 */       knownServerPath = this.m_knownServers.getProperty(idcName);
/*     */     }
/* 373 */     if (knownServerPath != null)
/*     */     {
/* 375 */       Report.trace("install", "found duplicate server \"" + idcName + "\" at \"" + knownServerPath + "\" and \"" + idcDir + "\"", null);
/*     */ 
/* 378 */       return idcName;
/*     */     }
/* 380 */     if (idcName != null)
/*     */     {
/* 382 */       this.m_knownServers.put(idcName, idcDir);
/*     */     }
/* 384 */     if (connectionName == null)
/*     */     {
/* 386 */       connectionName = idcName;
/*     */     }
/*     */ 
/* 389 */     for (int i = 0; i < this.m_settingList.length; ++i)
/*     */     {
/* 391 */       String name = this.m_settingList[i];
/* 392 */       String value = overrides.getProperty(this.m_settingList[i]);
/* 393 */       Report.trace("install", "override value for " + name + " is " + value, null);
/* 394 */       if (value == null)
/*     */       {
/* 396 */         value = installer.getConfigValue(this.m_settingList[i]);
/* 397 */         Report.trace("install", "config value for " + name + " is " + value, null);
/*     */       }
/* 399 */       if (value == null)
/*     */       {
/* 401 */         value = installer.substituteVariables(this.m_settingDefaults[i], null);
/* 402 */         Report.trace("install", "default value for " + name + " is " + value, null);
/*     */       }
/*     */ 
/* 405 */       if (value == null)
/*     */         continue;
/* 407 */       long flags = 0L;
/* 408 */       Properties entryInfo = this.m_installer.getInstallerTable("ConfigEntries", name);
/*     */ 
/* 410 */       if (entryInfo == null)
/*     */       {
/* 412 */         Report.trace("install", "ConfigEntries row missing for \"" + name + "\"", null);
/*     */       }
/*     */       else
/*     */       {
/* 417 */         String flagsString = entryInfo.getProperty("Flags");
/* 418 */         flags = this.m_installer.parseFlags(flagsString, null);
/* 419 */         value = this.m_installer.handleFlags(value, flags);
/*     */       }
/* 421 */       this.m_instanceData.addElement(connectionName + "/" + name + "=" + value);
/*     */     }
/*     */ 
/* 424 */     return idcName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 429 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.installer.QueryInstaller
 * JD-Core Version:    0.5.4
 */