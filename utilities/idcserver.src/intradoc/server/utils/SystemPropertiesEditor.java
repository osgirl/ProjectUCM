/*      */ package intradoc.server.utils;
/*      */ 
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.FileUtilsCfgBuilder;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.SafeFileOutputStream;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.server.IdcInstallInfo;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedInputStream;
/*      */ import java.io.IOException;
/*      */ import java.io.InputStream;
/*      */ import java.io.OutputStream;
/*      */ import java.io.Writer;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class SystemPropertiesEditor
/*      */   implements Cloneable
/*      */ {
/*      */   protected boolean m_allowIdcSave;
/*      */   protected boolean m_allowConfigSave;
/*      */   protected Properties m_cfgProperties;
/*      */   protected Properties m_idcProperties;
/*      */   protected String m_cfgFile;
/*      */   protected String m_idcFile;
/*      */   protected Vector m_cfgVector;
/*      */   protected Vector m_idcVector;
/*      */   protected Vector m_cfgExtra;
/*      */   protected Vector m_idcExtra;
/*      */   protected OutputStream m_cfgWriter;
/*      */   protected OutputStream m_idcWriter;
/*      */   protected InputStream m_cfgReader;
/*      */   protected InputStream m_idcReader;
/*      */   protected String m_cfgCharset;
/*      */   protected String m_idcCharset;
/*      */   protected String m_configFileContents;
/*   84 */   protected static final String[][] OBSOLETE_CONFIG_MAP = { { "isOverrideFormat", "IsOverrideFormat" }, { "isJdbc", "IsJdbc" } };
/*      */ 
/*   90 */   protected static final String[] CONFIG_REMOVE_IF_EMPTY = { "AutoNumberPrefix", "MajorRevSeq", "MinorRevSeq", "SocketHostAddressSecurityFilter", "SocketHostNameSecurityFilter", "CLASSPATH", "SharedDir", "InstanceMenuLabel", "InstanceDescription", "SystemLocale", "SystemTimeZone", "NtlmSecurityEnabled", "UseAdsi", "UseNtlm" };
/*      */ 
/*      */   public SystemPropertiesEditor()
/*      */   {
/*  100 */     this.m_cfgProperties = new Properties();
/*  101 */     this.m_idcProperties = new Properties();
/*      */ 
/*  105 */     this.m_idcFile = SystemUtils.getCfgFilePath();
/*      */ 
/*  108 */     setKeyOrder();
/*      */   }
/*      */ 
/*      */   public SystemPropertiesEditor(String idcFile)
/*      */   {
/*  113 */     this.m_cfgProperties = new Properties();
/*  114 */     this.m_idcProperties = new Properties();
/*  115 */     this.m_idcFile = idcFile;
/*      */ 
/*  118 */     setKeyOrder();
/*      */   }
/*      */ 
/*      */   public SystemPropertiesEditor clone()
/*      */   {
/*  125 */     SystemPropertiesEditor props = new SystemPropertiesEditor(this.m_idcFile);
/*  126 */     props.m_allowIdcSave = this.m_allowIdcSave;
/*  127 */     props.m_allowConfigSave = this.m_allowConfigSave;
/*  128 */     props.m_cfgProperties = ((Properties)this.m_cfgProperties.clone());
/*  129 */     props.m_idcProperties = ((Properties)this.m_idcProperties.clone());
/*  130 */     props.m_cfgFile = this.m_cfgFile;
/*  131 */     props.m_cfgVector = ((Vector)this.m_cfgVector.clone());
/*  132 */     props.m_idcVector = ((Vector)this.m_idcVector.clone());
/*  133 */     props.m_cfgExtra = ((Vector)this.m_cfgExtra.clone());
/*  134 */     props.m_idcExtra = ((Vector)this.m_idcExtra.clone());
/*  135 */     props.m_cfgCharset = this.m_cfgCharset;
/*  136 */     props.m_idcCharset = this.m_idcCharset;
/*  137 */     props.m_configFileContents = this.m_configFileContents;
/*  138 */     return props;
/*      */   }
/*      */ 
/*      */   public void setInputStreams(InputStream idc, InputStream cfg)
/*      */   {
/*  148 */     this.m_idcReader = idc;
/*  149 */     this.m_cfgReader = cfg;
/*      */   }
/*      */ 
/*      */   public void setFilepaths(String idcFile, String cfgFile)
/*      */   {
/*  157 */     this.m_idcFile = idcFile;
/*  158 */     this.m_cfgFile = cfgFile;
/*      */   }
/*      */ 
/*      */   public String getIdcFile()
/*      */   {
/*  163 */     return this.m_idcFile;
/*      */   }
/*      */ 
/*      */   public String getCfgFile()
/*      */   {
/*  168 */     return this.m_cfgFile;
/*      */   }
/*      */ 
/*      */   public List getIdcVector()
/*      */   {
/*  173 */     return this.m_idcVector;
/*      */   }
/*      */ 
/*      */   public List getCfgVector()
/*      */   {
/*  178 */     return this.m_cfgVector;
/*      */   }
/*      */ 
/*      */   public List getIdcExtra()
/*      */   {
/*  183 */     return this.m_idcExtra;
/*      */   }
/*      */ 
/*      */   public List getCfgExtra()
/*      */   {
/*  188 */     return this.m_cfgExtra;
/*      */   }
/*      */ 
/*      */   public void setIdcWritable()
/*      */   {
/*  196 */     this.m_allowIdcSave = true;
/*      */   }
/*      */ 
/*      */   public boolean getIdcWritable()
/*      */   {
/*  201 */     return this.m_allowIdcSave;
/*      */   }
/*      */ 
/*      */   public void setConfigWritable()
/*      */   {
/*  206 */     this.m_allowConfigSave = true;
/*      */   }
/*      */ 
/*      */   public boolean getConfigWritable()
/*      */   {
/*  211 */     return this.m_allowConfigSave;
/*      */   }
/*      */ 
/*      */   public void setOutputStreams(OutputStream idc, OutputStream cfg)
/*      */   {
/*  220 */     this.m_idcWriter = idc;
/*  221 */     this.m_cfgWriter = cfg;
/*      */   }
/*      */ 
/*      */   public void setCharsets(String idcEnc, String cfgEnc)
/*      */   {
/*  226 */     this.m_idcCharset = idcEnc;
/*  227 */     this.m_cfgCharset = cfgEnc;
/*      */   }
/*      */ 
/*      */   public void loadProperties()
/*      */     throws ServiceException
/*      */   {
/*  235 */     initIdc();
/*  236 */     initConfig();
/*      */   }
/*      */ 
/*      */   public void saveIdc()
/*      */     throws ServiceException
/*      */   {
/*  244 */     if (!this.m_allowIdcSave) {
/*      */       return;
/*      */     }
/*  247 */     removeIfEmpty(this.m_idcProperties, CONFIG_REMOVE_IF_EMPTY);
/*      */ 
/*  249 */     writeProperties(this.m_idcProperties, this.m_idcVector, this.m_idcExtra, this.m_idcFile, this.m_idcWriter, this.m_idcCharset);
/*      */   }
/*      */ 
/*      */   public void saveConfig()
/*      */     throws ServiceException
/*      */   {
/*  259 */     if (!this.m_allowConfigSave) {
/*      */       return;
/*      */     }
/*  262 */     removeIfEmpty(this.m_cfgProperties, CONFIG_REMOVE_IF_EMPTY);
/*      */ 
/*  264 */     if (this.m_cfgFile == null)
/*      */     {
/*  268 */       String dir = FileUtils.getParent(SystemUtils.getBinDir());
/*  269 */       dir = FileUtils.getAbsolutePath(dir, "config");
/*  270 */       FileUtils.checkOrCreateDirectory(dir, 1);
/*  271 */       this.m_cfgFile = (dir + "/config.cfg");
/*      */     }
/*  273 */     writeProperties(this.m_cfgProperties, this.m_cfgVector, this.m_cfgExtra, this.m_cfgFile, this.m_cfgWriter, this.m_cfgCharset);
/*      */   }
/*      */ 
/*      */   public void removeIfEmpty(Properties props, String[] keys)
/*      */   {
/*  280 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  282 */       String val = props.getProperty(keys[i]);
/*  283 */       if ((val == null) || (val.trim().length() != 0))
/*      */         continue;
/*  285 */       props.remove(keys[i]);
/*      */     }
/*      */   }
/*      */ 
/*      */   public Properties getConfig()
/*      */   {
/*  296 */     String cfgExtraVariables = StringUtils.createString(this.m_cfgExtra, '\n', '\n');
/*  297 */     Properties props = (Properties)this.m_cfgProperties.clone();
/*  298 */     props.put("cfgExtraVariables", cfgExtraVariables);
/*  299 */     return props;
/*      */   }
/*      */ 
/*      */   public Properties getIdc()
/*      */   {
/*  307 */     Properties props = (Properties)this.m_idcProperties.clone();
/*  308 */     return props;
/*      */   }
/*      */ 
/*      */   public Properties getIdcProperties()
/*      */   {
/*  318 */     return this.m_idcProperties;
/*      */   }
/*      */ 
/*      */   public Properties getCfgProperties()
/*      */   {
/*  323 */     return this.m_cfgProperties;
/*      */   }
/*      */ 
/*      */   public String searchForValue(String key)
/*      */   {
/*  332 */     String value = (String)this.m_idcProperties.get(key);
/*  333 */     if (value == null)
/*      */     {
/*  335 */       value = (String)this.m_cfgProperties.get(key);
/*      */     }
/*  337 */     if (value == null)
/*      */     {
/*  339 */       for (int i = 0; i < this.m_idcExtra.size(); ++i)
/*      */       {
/*  341 */         String entry = (String)this.m_idcExtra.elementAt(i);
/*  342 */         int index = entry.indexOf("=");
/*  343 */         if ((index <= 0) || (!entry.substring(0, index).equals(key)))
/*      */           continue;
/*  345 */         value = entry.substring(index + 1);
/*      */       }
/*      */     }
/*      */ 
/*  349 */     if (value == null)
/*      */     {
/*  351 */       for (int i = 0; i < this.m_cfgExtra.size(); ++i)
/*      */       {
/*  353 */         String entry = (String)this.m_cfgExtra.elementAt(i);
/*  354 */         int index = entry.indexOf("=");
/*  355 */         if ((index <= 0) || (!entry.substring(0, index).equals(key)))
/*      */           continue;
/*  357 */         value = entry.substring(index + 1);
/*      */       }
/*      */     }
/*      */ 
/*  361 */     return value;
/*      */   }
/*      */ 
/*      */   public void mergePropertyValues(Properties idc, Properties cfg)
/*      */   {
/*  372 */     mergePropertyValuesEx(idc, cfg, false);
/*      */   }
/*      */ 
/*      */   public void mergePropertyValuesEx(Properties idc, Properties cfg, boolean allowAdd)
/*      */   {
/*  378 */     if ((idc != null) && (idc.size() > 0))
/*      */     {
/*  380 */       mergePropertiesEx(this.m_idcProperties, idc, (allowAdd) ? this.m_idcVector : null);
/*      */     }
/*  382 */     if ((cfg == null) || (cfg.size() <= 0))
/*      */       return;
/*  384 */     String extras = (String)cfg.get("cfgExtraVariables");
/*  385 */     if ((extras != null) && (extras.length() > 0))
/*      */     {
/*  390 */       this.m_cfgExtra = StringUtils.parseArray(extras, '\n', '\n');
/*      */     }
/*  392 */     mergePropertiesEx(this.m_cfgProperties, cfg, (allowAdd) ? this.m_cfgVector : null);
/*      */   }
/*      */ 
/*      */   public void removePropertyValues(String[] idcList, String[] cfgList)
/*      */   {
/*  398 */     removeValues(this.m_idcProperties, this.m_idcVector, this.m_idcExtra, idcList);
/*  399 */     removeValues(this.m_cfgProperties, this.m_cfgVector, this.m_cfgExtra, cfgList);
/*      */   }
/*      */ 
/*      */   public void removeValues(Properties props, Vector list1, Vector list2, String[] remove)
/*      */   {
/*  405 */     int size1 = list1.size();
/*  406 */     int size2 = list2.size();
/*  407 */     for (int i = 0; (remove != null) && (i < remove.length); ++i)
/*      */     {
/*  409 */       props.remove(remove[i]);
/*  410 */       String tmp = remove[i] + "=";
/*  411 */       for (int j = 0; j < size1; ++j)
/*      */       {
/*  413 */         String element = (String)list1.elementAt(j);
/*  414 */         if (!element.startsWith(tmp))
/*      */           continue;
/*  416 */         list1.removeElementAt(j);
/*  417 */         break;
/*      */       }
/*      */ 
/*  420 */       for (int j = 0; j < size2; ++j)
/*      */       {
/*  422 */         String element = (String)list2.elementAt(j);
/*  423 */         if (!element.startsWith(tmp))
/*      */           continue;
/*  425 */         list2.removeElementAt(j);
/*  426 */         break;
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void replacePropertyValues(Properties idc, Properties cfg)
/*      */   {
/*  438 */     if ((idc != null) && (idc.size() > 0))
/*      */     {
/*  440 */       this.m_idcProperties = idc;
/*      */     }
/*  442 */     if ((cfg == null) || (cfg.size() <= 0))
/*      */       return;
/*  444 */     String extras = (String)cfg.get("cfgExtraVariables");
/*  445 */     if ((extras != null) && (extras.length() > 0))
/*      */     {
/*  447 */       this.m_cfgExtra = StringUtils.parseArray(extras, '\n', '\n');
/*      */     }
/*  449 */     this.m_cfgProperties = cfg;
/*      */   }
/*      */ 
/*      */   protected void mergeProperties(Properties oldProps, Properties newProps)
/*      */   {
/*  459 */     mergePropertiesEx(oldProps, newProps, null);
/*      */   }
/*      */ 
/*      */   protected void mergePropertiesEx(Properties oldProps, Properties newProps, Vector list)
/*      */   {
/*  465 */     for (Enumeration e = newProps.propertyNames(); e.hasMoreElements(); )
/*      */     {
/*  467 */       String key = (String)e.nextElement();
/*  468 */       String value = (String)newProps.get(key);
/*  469 */       if ((list != null) && (oldProps.get(key) == null))
/*      */       {
/*  471 */         list.addElement(key);
/*      */       }
/*  473 */       oldProps.put(key, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void writeProperties(Properties props, Vector vector, Vector extra, String fileName, OutputStream output, String jcharset)
/*      */     throws ServiceException
/*      */   {
/*  491 */     writePropertiesEx(props, vector, extra, fileName, output, jcharset, true);
/*      */   }
/*      */ 
/*      */   public void writePropertiesEx(Map props, List vector, List extra, String fileName, OutputStream output, String jcharset, boolean useExisting)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  500 */       if (output == null)
/*      */       {
/*  503 */         String dir = FileUtils.getParent(fileName);
/*  504 */         FileUtils.checkOrCreateDirectory(dir, 1);
/*  505 */         output = FileUtils.openOutputStream(fileName, 16);
/*      */       }
/*  507 */       writeFile(props, vector, extra, output, jcharset);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  511 */       throw new ServiceException(e, "csUnableToSaveSystemProperties", new Object[] { fileName });
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initIdc()
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/*  523 */       this.m_idcCharset = readFile(this.m_idcProperties, this.m_idcVector, this.m_idcExtra, this.m_idcFile, this.m_idcReader);
/*      */ 
/*  526 */       mapObsoleteKeys(new String[][] { { "webBrowserPath", "WebBrowserPath" } }, this.m_idcProperties);
/*      */ 
/*  529 */       String rootPath = this.m_idcProperties.getProperty("IntradocDir");
/*  530 */       if (rootPath == null)
/*      */       {
/*  532 */         rootPath = FileUtils.getParent(FileUtils.getParent(this.m_idcFile));
/*      */       }
/*  534 */       this.m_cfgFile = FileUtils.fileSlashes(rootPath + "/config/config.cfg");
/*      */ 
/*  536 */       this.m_allowIdcSave = true;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  540 */       String msg = LocaleUtils.encodeMessage("csCouldNotLoadFile", null, "intradoc.cfg");
/*      */ 
/*  542 */       throw new ServiceException(msg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initConfig()
/*      */     throws ServiceException
/*      */   {
/*  551 */     if (this.m_cfgFile == null)
/*      */     {
/*  554 */       this.m_allowConfigSave = true;
/*  555 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  560 */       Map debugArgs = new HashMap();
/*  561 */       this.m_cfgCharset = readFileWithDebugInfo(this.m_cfgProperties, this.m_cfgVector, this.m_cfgExtra, this.m_cfgFile, this.m_cfgReader, debugArgs);
/*      */ 
/*  563 */       this.m_configFileContents = ((String)debugArgs.get("textString"));
/*      */ 
/*  565 */       mapObsoleteKeys(OBSOLETE_CONFIG_MAP, this.m_cfgProperties);
/*  566 */       this.m_allowConfigSave = true;
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  570 */       throw new ServiceException(e, "csCouldNotLoadFile", new Object[] { "config.cfg" });
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void mapObsoleteKeys(String[][] keyMap, Properties props)
/*      */   {
/*  580 */     for (int i = 0; i < keyMap.length; ++i)
/*      */     {
/*  582 */       String obsKey = keyMap[i][0];
/*  583 */       String newKey = keyMap[i][1];
/*      */ 
/*  585 */       String val = props.getProperty(obsKey);
/*  586 */       if ((val == null) || (props.getProperty(newKey) != null))
/*      */         continue;
/*  588 */       props.remove(obsKey);
/*  589 */       props.put(newKey, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected static String getMatchingKey(String[][] keyMap, String key)
/*      */   {
/*  596 */     for (int i = 0; i < keyMap.length; ++i)
/*      */     {
/*  598 */       if (keyMap[i][0].equals(key))
/*      */       {
/*  600 */         return keyMap[i][1];
/*      */       }
/*  602 */       if (keyMap[i][1].equals(key))
/*      */       {
/*  604 */         return keyMap[i][0];
/*      */       }
/*      */     }
/*  607 */     return null;
/*      */   }
/*      */ 
/*      */   protected void setKeyOrder()
/*      */   {
/*  615 */     this.m_cfgVector = new IdcVector();
/*  616 */     this.m_idcVector = new IdcVector();
/*  617 */     this.m_cfgExtra = new IdcVector();
/*  618 */     this.m_idcExtra = new IdcVector();
/*      */ 
/*  621 */     this.m_cfgVector.addElement("#Server System Properties");
/*  622 */     this.m_cfgVector.addElement("IDC_Name");
/*  623 */     this.m_cfgVector.addElement("IdcProductName");
/*  624 */     this.m_cfgVector.addElement("SystemLocale");
/*  625 */     this.m_cfgVector.addElement("SystemTimeZone");
/*  626 */     this.m_cfgVector.addElement("InstanceMenuLabel");
/*  627 */     this.m_cfgVector.addElement("InstanceDescription");
/*  628 */     this.m_cfgVector.addElement("SocketHostAddressSecurityFilter");
/*  629 */     this.m_cfgVector.addElement("SocketHostNameSecurityFilter");
/*  630 */     this.m_cfgVector.addElement("ProxyPassword");
/*  631 */     this.m_cfgVector.addElement("ProxyPasswordEncoding");
/*      */ 
/*  634 */     this.m_cfgVector.addElement("#Database Variables");
/*  635 */     this.m_cfgVector.addElement("IsJdbc");
/*  636 */     this.m_cfgVector.addElement("DatabaseType");
/*  637 */     this.m_cfgVector.addElement("JdbcDriver");
/*  638 */     this.m_cfgVector.addElement("JdbcConnectionString");
/*  639 */     this.m_cfgVector.addElement("JdbcUser");
/*  640 */     this.m_cfgVector.addElement("JdbcPassword");
/*  641 */     this.m_cfgVector.addElement("JdbcPasswordEncoding");
/*  642 */     this.m_cfgVector.addElement("DatabasePreserveCase");
/*  643 */     this.m_cfgVector.addElement("SystemDatabase:DataSource");
/*  644 */     this.m_cfgVector.addElement("SystemDatabase:UseDataSource");
/*  645 */     this.m_cfgVector.addElement("JDBC_JAVA_CLASSPATH_system");
/*  646 */     this.m_cfgVector.addElement("JDBC_JAVA_CLASSPATH_custom");
/*      */ 
/*  649 */     this.m_cfgVector.addElement("#Internet Variables");
/*  650 */     this.m_cfgVector.addElement("HttpServerAddress");
/*  651 */     this.m_cfgVector.addElement("MailServer");
/*  652 */     this.m_cfgVector.addElement("SysAdminAddress");
/*  653 */     this.m_cfgVector.addElement("SmtpPort");
/*  654 */     this.m_cfgVector.addElement("HttpRelativeWebRoot");
/*  655 */     this.m_cfgVector.addElement("HttpRelativeCgiRoot");
/*  656 */     this.m_cfgVector.addElement("CgiFileName");
/*  657 */     this.m_cfgVector.addElement("UseSSL");
/*  658 */     this.m_cfgVector.addElement("WebProxyAdminServer");
/*  659 */     this.m_cfgVector.addElement("IsProxiedServer");
/*  660 */     this.m_cfgVector.addElement("NtlmSecurityEnabled");
/*  661 */     this.m_cfgVector.addElement("UseAdsi");
/*  662 */     this.m_cfgVector.addElement("UseNtlm");
/*      */ 
/*  665 */     this.m_cfgVector.addElement("#General Option Variables");
/*  666 */     this.m_cfgVector.addElement("IsOverrideFormat");
/*  667 */     this.m_cfgVector.addElement("GetCopyAccess");
/*  668 */     this.m_cfgVector.addElement("ExclusiveCheckout");
/*  669 */     this.m_cfgVector.addElement("DownloadApplet");
/*  670 */     this.m_cfgVector.addElement("MultiUpload");
/*  671 */     this.m_cfgVector.addElement("IsAutoNumber");
/*  672 */     this.m_cfgVector.addElement("AutoNumberPrefix");
/*  673 */     this.m_cfgVector.addElement("MajorRevSeq");
/*  674 */     this.m_cfgVector.addElement("MinorRevSeq");
/*  675 */     this.m_cfgVector.addElement("AuthorDelete");
/*  676 */     this.m_cfgVector.addElement("ShowOnlyKnownAccounts");
/*  677 */     this.m_cfgVector.addElement("UseAccounts");
/*  678 */     this.m_cfgVector.addElement("EnterpriseSearchAsDefault");
/*  679 */     this.m_cfgVector.addElement("IsDynamicConverterEnabled");
/*  680 */     this.m_cfgVector.addElement("IsJspServerEnabled");
/*  681 */     this.m_cfgVector.addElement("JspEnabledGroups");
/*  682 */     this.m_cfgVector.addElement("IsProvisionalServer");
/*      */ 
/*  684 */     this.m_cfgVector.addElement("#Additional Variables");
/*  685 */     this.m_cfgVector.addElement("JAVA_COMMAND_LINE_SELECTION");
/*  686 */     this.m_cfgVector.addElement("LAUNCHERS_system");
/*      */ 
/*  690 */     this.m_idcVector.addElement("#Server System Properties");
/*  691 */     this.m_idcVector.addElement("IDC_Id");
/*  692 */     this.m_idcVector.addElement("IDC_Name");
/*  693 */     this.m_idcVector.addElement("IdcProductName");
/*      */ 
/*  696 */     this.m_idcVector.addElement("#Server Directory Variables");
/*  697 */     this.m_idcVector.addElement("UCM_ORACLE_HOME");
/*  698 */     this.m_idcVector.addElement("IdcHomeDir");
/*  699 */     for (String key : IdcInstallInfo.CAPTURED_APP_SERVER_PROPERTIES)
/*      */     {
/*  701 */       this.m_idcVector.addElement(key);
/*      */     }
/*  703 */     this.m_idcVector.addElement("IntradocDir");
/*  704 */     this.m_idcVector.addElement("SearchDir");
/*  705 */     this.m_idcVector.addElement("VaultDir");
/*  706 */     this.m_idcVector.addElement("WeblayoutDir");
/*  707 */     this.m_idcVector.addElement("SharedWeblayoutDir");
/*  708 */     this.m_idcVector.addElement("WebBrowserPath");
/*  709 */     this.m_idcVector.addElement("SharedDir");
/*  710 */     this.m_idcVector.addElement("JAVA_EXE");
/*  711 */     this.m_idcVector.addElement("JvmCommandLine");
/*      */ 
/*  713 */     this.m_idcVector.addElement("#Server Classpath variables");
/*  714 */     this.m_idcVector.addElement("JAVA_COMMAND_LINE_SELECTION");
/*  715 */     this.m_idcVector.addElement("JAVA_CLASSPATH_customjdbc");
/*  716 */     this.m_idcVector.addElement("JAVA_CLASSPATH_systemjdbc");
/*  717 */     this.m_idcVector.addElement("JDBC_JAVA_CLASSPATH_system");
/*  718 */     this.m_idcVector.addElement("JDBC_JAVA_CLASSPATH_custom");
/*  719 */     this.m_idcVector.addElement("JAVA_CLASSPATH_idcrefinery");
/*  720 */     this.m_idcVector.addElement("BASE_JAVA_CLASSPATH_server");
/*  721 */     this.m_idcVector.addElement("BASE_JAVA_CLASSPATH_custom");
/*      */ 
/*  723 */     this.m_idcVector.addElement("#Additional Variables");
/*  724 */     this.m_idcVector.addElement("ForceLoadConfig");
/*      */   }
/*      */ 
/*      */   public void addKeys(String idcKeys, String cfgKeys)
/*      */   {
/*  729 */     if (idcKeys != null)
/*      */     {
/*  731 */       addVectorKeys(idcKeys, this.m_idcVector);
/*      */     }
/*      */ 
/*  734 */     if (cfgKeys == null)
/*      */       return;
/*  736 */     addVectorKeys(cfgKeys, this.m_cfgVector);
/*      */   }
/*      */ 
/*      */   public void addVectorKeys(String keys, Vector keyVector)
/*      */   {
/*  742 */     Vector newKeys = StringUtils.parseArray(keys, ',', '^');
/*  743 */     int size = newKeys.size();
/*  744 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  746 */       String key = (String)newKeys.elementAt(i);
/*      */ 
/*  749 */       boolean isFound = false;
/*  750 */       int num = keyVector.size();
/*  751 */       for (int j = 0; j < num; ++j)
/*      */       {
/*  753 */         String k = (String)keyVector.elementAt(j);
/*  754 */         if (!k.equals(key))
/*      */           continue;
/*  756 */         isFound = true;
/*  757 */         break;
/*      */       }
/*      */ 
/*  760 */       if (isFound)
/*      */         continue;
/*  762 */       keyVector.addElement(key);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static String readFile(Properties props, Vector vector, Vector extra, String filePath, InputStream input)
/*      */     throws ServiceException
/*      */   {
/*  781 */     return readFileWithDebugInfo(props, vector, extra, filePath, input, null);
/*      */   }
/*      */ 
/*      */   public static String readFileWithDebugInfo(Properties props, Vector vector, Vector extra, String filePath, InputStream input, Map debugValues)
/*      */     throws ServiceException
/*      */   {
/*  800 */     String[] charset = { null };
/*      */     try
/*      */     {
/*  803 */       if (input == null)
/*      */       {
/*  806 */         int retVal = FileUtils.checkFile(filePath, true, false);
/*  807 */         if (retVal < 0)
/*      */         {
/*  809 */           throw new ServiceException("!csCheckFileExistence");
/*      */         }
/*  811 */         input = new BufferedInputStream(FileUtilsCfgBuilder.getCfgInputStream(filePath));
/*      */       }
/*  813 */       String textString = FileUtils.loadFile(input, "cfg", charset);
/*      */ 
/*  816 */       Vector lines = StringUtils.parseArray(textString, '\n', '\n');
/*      */ 
/*  818 */       int nlines = lines.size();
/*  819 */       int start = 0;
/*  820 */       if (nlines > 0)
/*      */       {
/*  822 */         String line = (String)lines.elementAt(0);
/*  823 */         int eqIndx = line.indexOf("=");
/*  824 */         int encodingStartIndex = line.indexOf("<?");
/*  825 */         if ((encodingStartIndex >= 0) && (encodingStartIndex < eqIndx))
/*      */         {
/*  827 */           start = 1;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  858 */       for (int i = start; i < lines.size(); ++i)
/*      */       {
/*  860 */         String line = ((String)lines.elementAt(i)).trim();
/*  861 */         int eqIndx = line.indexOf("=");
/*  862 */         int colonIndex = line.indexOf(": ");
/*  863 */         int idx = eqIndx;
/*  864 */         int patternSize = 1;
/*  865 */         boolean isColonSeparator = false;
/*  866 */         if ((colonIndex >= 0) && (((colonIndex < idx) || (idx < 0))))
/*      */         {
/*  876 */           isColonSeparator = true;
/*  877 */           idx = colonIndex;
/*  878 */           patternSize = 2;
/*      */         }
/*      */ 
/*  882 */         if (idx > 0)
/*      */         {
/*  884 */           String key = line.substring(0, idx);
/*  885 */           String value = line.substring(idx + patternSize);
/*      */ 
/*  887 */           if (!isColonSeparator)
/*      */           {
/*  890 */             value = StringUtils.decodeLiteralStringEscapeSequence(value);
/*      */           }
/*      */ 
/*  894 */           props.put(key, value);
/*      */ 
/*  897 */           if ((vector.indexOf(key) >= 0) || (getMatchingKey(OBSOLETE_CONFIG_MAP, key) != null)) {
/*      */             continue;
/*      */           }
/*  900 */           extra.addElement(line);
/*      */         }
/*      */         else {
/*  903 */           if ((line.length() <= 0) || (line.charAt(0) != '#') || 
/*  906 */             (vector.indexOf(line) >= 0))
/*      */             continue;
/*  908 */           extra.addElement(line);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  913 */       if (debugValues != null)
/*      */       {
/*  915 */         debugValues.put("textString", textString);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*      */       String msg;
/*  922 */       throw new ServiceException(msg, e);
/*      */     }
/*      */     finally
/*      */     {
/*  926 */       FileUtils.closeFiles(null, input);
/*      */     }
/*      */ 
/*  929 */     return charset[0];
/*      */   }
/*      */ 
/*      */   public String cfgFileContents()
/*      */   {
/*  939 */     return this.m_configFileContents;
/*      */   }
/*      */ 
/*      */   public static void writeFile(Map props, List vector, List extra, OutputStream output, String jcharset)
/*      */     throws ServiceException
/*      */   {
/*  955 */     if (jcharset == null)
/*      */     {
/*  957 */       jcharset = System.getProperty("file.encoding");
/*  958 */       jcharset = LocaleResources.getEncodingFromAlias(jcharset);
/*  959 */       if ((jcharset == null) || (jcharset.length() == 0))
/*      */       {
/*  961 */         jcharset = "iso-8859-1";
/*      */       }
/*      */     }
/*  964 */     boolean success = false;
/*  965 */     Writer writer = null;
/*      */     try
/*      */     {
/*  968 */       writer = FileUtils.openDataWriterEx(output, jcharset, 0);
/*  969 */       writeCfgHeader(writer, jcharset);
/*      */ 
/*  971 */       Properties writtenProps = new Properties();
/*  972 */       int size = vector.size();
/*  973 */       for (int i = 0; i < size; ++i)
/*      */       {
/*  975 */         String key = (String)vector.get(i);
/*  976 */         String value = (String)props.get(key);
/*      */ 
/*  978 */         if (value != null)
/*      */         {
/*  980 */           if (writtenProps.get(key) != null)
/*      */             continue;
/*  982 */           value = StringUtils.encodeLiteralStringEscapeSequence(value);
/*  983 */           writer.write(key + "=" + value + "\n");
/*  984 */           writtenProps.put(key, key);
/*      */         }
/*      */         else {
/*  987 */           if ((key.length() <= 0) || (key.charAt(0) != '#'))
/*      */             continue;
/*  989 */           if (i > 0)
/*      */           {
/*  991 */             writer.write("\n");
/*      */           }
/*  993 */           writer.write(key + "\n");
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  998 */       if (extra != null)
/*      */       {
/* 1000 */         size = extra.size();
/* 1001 */         if (size > 0)
/*      */         {
/* 1004 */           writer.write(extra.get(0).toString().trim());
/*      */         }
/* 1006 */         for (int i = 1; i < size; ++i)
/*      */         {
/* 1008 */           writer.write("\n" + extra.get(i).toString().trim());
/*      */         }
/*      */ 
/* 1011 */         writer.write("\n");
/*      */       }
/* 1013 */       success = true;
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1021 */       if ((!success) && (output != null) && (output instanceof SafeFileOutputStream))
/*      */       {
/* 1023 */         ((SafeFileOutputStream)output).abortAndClose();
/*      */       }
/* 1025 */       if (writer != null)
/*      */       {
/*      */         try
/*      */         {
/* 1029 */           writer.close();
/*      */         }
/*      */         catch (IOException e)
/*      */         {
/* 1033 */           throw new ServiceException("!syIOWriterFileError", e);
/*      */         }
/*      */       }
/* 1036 */       FileUtils.closeFiles(output, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void writeCfgHeader(Writer writer, String jcharset) throws IOException
/*      */   {
/* 1042 */     writer.write("<?cfg jcharset=\"" + jcharset + "\"?>\n");
/*      */   }
/*      */ 
/*      */   public void closeAllStreams()
/*      */   {
/* 1049 */     closeStreams(this.m_cfgReader, this.m_cfgWriter);
/* 1050 */     closeStreams(this.m_idcReader, this.m_idcWriter);
/* 1051 */     this.m_cfgReader = null;
/* 1052 */     this.m_cfgWriter = null;
/* 1053 */     this.m_idcReader = null;
/* 1054 */     this.m_idcWriter = null;
/*      */   }
/*      */ 
/*      */   public void closeStreams(InputStream inStream, OutputStream outStream)
/*      */   {
/*      */     try
/*      */     {
/* 1061 */       if (inStream != null)
/*      */       {
/* 1063 */         inStream.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1068 */       if (SystemUtils.m_verbose)
/*      */       {
/* 1070 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*      */     try
/*      */     {
/* 1075 */       if (outStream != null)
/*      */       {
/* 1077 */         outStream.close();
/*      */       }
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/* 1082 */       if (!SystemUtils.m_verbose)
/*      */         return;
/* 1084 */       Report.debug("system", null, ignore);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1091 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97521 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.SystemPropertiesEditor
 * JD-Core Version:    0.5.4
 */