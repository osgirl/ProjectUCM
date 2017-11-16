/*      */ package intradoc.apputilities.componentwizard;
/*      */ 
/*      */ import intradoc.common.EnvUtils;
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ResourceContainer;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.ContainerHelper;
/*      */ import intradoc.gui.CustomCheckbox;
/*      */ import intradoc.gui.CustomLabel;
/*      */ import intradoc.gui.DialogCallback;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DisplayChoice;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.iwt.IdcFileChooser;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.server.DirectoryLocator;
/*      */ import intradoc.server.utils.ComponentInstaller;
/*      */ import intradoc.server.utils.ComponentListEditor;
/*      */ import intradoc.server.utils.ComponentListManager;
/*      */ import intradoc.server.utils.ComponentLocationUtils;
/*      */ import intradoc.server.utils.ComponentPreferenceData;
/*      */ import intradoc.shared.DialogHelpTable;
/*      */ import intradoc.shared.PluginFilterData;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.tools.build.ComponentPackager;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import intradoc.zip.ZipFunctions;
/*      */ import java.awt.GridBagConstraints;
/*      */ import java.awt.Insets;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.io.ByteArrayInputStream;
/*      */ import java.io.File;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.IOException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.TimeZone;
/*      */ import java.util.Vector;
/*      */ import java.util.zip.ZipOutputStream;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JCheckBox;
/*      */ import javax.swing.JComboBox;
/*      */ import javax.swing.JFileChooser;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class ManifestEditorDlg extends CWizardBaseDlg
/*      */   implements ActionListener, ItemListener
/*      */ {
/*  104 */   public static final String[][] MANIFEST_COL_MAP = { { "displayEntryType", "!csCompWizLabelEntryType", "10" }, { "rootPrefix", "!csCompWizLabelRootPrefix", "20" }, { "location", "!csCompWizLabelLocation", "20" } };
/*      */ 
/*  111 */   public static final String[][] FILE_TYPES = { { "componentClasses", "csCompWizTypeComponentClass" }, { "classes", "csCompWizTypeClass" }, { "componentExtra", "csCompWizTypeComponentExtra" }, { "component", "csCompWizTypeComponent" }, { "componentLib", "csCompWizTypeComponentLib" }, { "common", "csCompWizTypeCommon" }, { "help", "csCompWizTypeHelp" }, { "images", "csCompWizTypeImages" }, { "jsp", "csCompWizTypeJsp" }, { "resources", "csCompWizTypeResources" }, { "weblayout", "csCompWizTypeWeblayout" } };
/*      */ 
/*  126 */   protected final String[][] COMMAND_LIST = { { "csCompWizCommandAdd", "add", "0" }, { "csCompWizCommandRemove", "remove", "1" } };
/*      */ 
/*  132 */   public final String[] MANIFEST_FIELD_INFO = { "entryType", "location" };
/*  133 */   protected final String[] MANIFEST_DISPLAY_FIELD_INFO = { "id", "entryType", "displayEntryType", "rootPrefix", "location" };
/*      */ 
/*  136 */   protected String m_manifestName = "manifest.hda";
/*  137 */   protected String m_manifestTableName = "Manifest";
/*  138 */   protected DataBinder m_manifestData = null;
/*  139 */   protected ComponentWizardManager m_manager = null;
/*      */ 
/*  141 */   protected DataBinder m_resDefData = new DataBinder();
/*      */ 
/*  144 */   public boolean m_isError = false;
/*  145 */   public boolean m_isRemove = false;
/*  146 */   protected String m_dateStr = null;
/*  147 */   protected String m_backupZipPath = null;
/*      */ 
/*  150 */   protected final int BUILD_SETTINGS = 0;
/*  151 */   protected final int BUILD = 1;
/*  152 */   protected final int INSTALL = 2;
/*  153 */   protected final int UNINSTALL = 3;
/*      */ 
/*  156 */   protected UdlPanel m_list = null;
/*  157 */   protected DataResultSet m_listData = null;
/*  158 */   protected DialogHelper m_dlgHelper = null;
/*  159 */   protected JComboBox m_targetChoice = null;
/*  160 */   protected JCheckBox m_makeJarCheckbox = null;
/*  161 */   protected JCheckBox m_incJavaSourceCheckbox = null;
/*  162 */   protected JCheckBox m_hasPrefCheckbox = null;
/*      */ 
/*  165 */   protected ComponentInstaller m_installer = null;
/*  166 */   protected int m_buildType = 0;
/*      */ 
/*  168 */   protected int m_idCount = 0;
/*  169 */   protected String m_intradocDir = null;
/*  170 */   protected String m_version = null;
/*      */ 
/*  172 */   protected String m_sourcePath = null;
/*  173 */   protected String m_root = null;
/*  174 */   protected boolean m_addSourcePath = false;
/*  175 */   protected String m_location = null;
/*      */ 
/*      */   public ManifestEditorDlg(SystemInterface sys, String title, String helpPage, IntradocComponent component, ComponentWizardManager manager)
/*      */   {
/*  181 */     super(sys, title, helpPage);
/*  182 */     this.m_component = component;
/*  183 */     this.m_manager = manager;
/*  184 */     this.m_intradocDir = SharedObjects.getEnvironmentValue("IntradocDir");
/*  185 */     if (this.m_component != null)
/*      */     {
/*  187 */       this.m_resDefData = this.m_component.m_binder;
/*      */     }
/*  189 */     SimpleDateFormat frmt = new SimpleDateFormat("-yyyyMMddHHmmss");
/*  190 */     frmt.setTimeZone(TimeZone.getDefault());
/*  191 */     this.m_dateStr = frmt.format(new Date());
/*      */   }
/*      */ 
/*      */   public void init(String buildType)
/*      */   {
/*  197 */     if (buildType.equals("build"))
/*      */     {
/*  199 */       this.m_buildType = 1;
/*      */     }
/*  201 */     else if (buildType.equals("install"))
/*      */     {
/*  203 */       this.m_buildType = 2;
/*      */     }
/*  205 */     else if (buildType.equals("uninstall"))
/*      */     {
/*  207 */       this.m_buildType = 3;
/*      */     }
/*      */ 
/*  210 */     if (this.m_buildType != 0)
/*      */     {
/*  212 */       this.m_backupZipPath = (FileUtils.directorySlashes(DirectoryLocator.getAppDataDirectory()) + "components/");
/*      */     }
/*      */ 
/*  218 */     LocaleResources.localizeStaticDoubleArray(FILE_TYPES, null, 1);
/*      */ 
/*  220 */     initInstaller();
/*  221 */     initUI();
/*      */ 
/*  223 */     if ((this.m_isError) && (!this.m_isRemove))
/*      */       return;
/*  225 */     if (this.m_buildType == 3)
/*      */     {
/*  227 */       this.m_helper.m_props.put("name", this.m_component.m_name);
/*      */     }
/*  229 */     this.m_helper.loadComponentValues();
/*      */   }
/*      */ 
/*      */   protected void initInstaller()
/*      */   {
/*  235 */     this.m_installer = new ComponentInstaller();
/*  236 */     if (this.m_buildType == 2)
/*      */     {
/*  240 */       this.m_installer.loadIdcDir();
/*      */     }
/*      */     else
/*      */     {
/*  244 */       Properties props = this.m_helper.m_props;
/*  245 */       boolean isCreate = false;
/*      */       try
/*      */       {
/*  249 */         isCreate = readDefinitionFiles();
/*      */ 
/*  252 */         String componentName = this.m_resDefData.getLocal("ComponentName");
/*  253 */         this.m_installer.initEx(componentName, this.m_resDefData, this.m_manifestData, new HashMap());
/*  254 */         initListData();
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  258 */         this.m_isError = true;
/*  259 */         if (Report.m_verbose)
/*      */         {
/*  261 */           Report.debug("componentwizard", "ManifiestEditorDlg.initInstaller: Error", e);
/*      */         }
/*  263 */         CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizUnableToInitManifestReader", new Object[] { this.m_component.m_name }), 1);
/*      */ 
/*  266 */         return;
/*      */       }
/*      */ 
/*  271 */       String customDir = ComponentLocationUtils.computeDefaultCustomComponentDir(1, false, new boolean[1]);
/*      */ 
/*  273 */       String systemDir = ComponentLocationUtils.computeDefaultSystemComponentDir(1, false, new boolean[1]);
/*      */ 
/*  275 */       String cmpSystemDir = systemDir;
/*  276 */       String absPath = this.m_component.m_absLocation;
/*  277 */       String cmpIntradocDir = this.m_intradocDir;
/*  278 */       String cmpAbsPath = absPath;
/*  279 */       String cmpCustomDir = customDir;
/*  280 */       if (EnvUtils.isFamily("windows"))
/*      */       {
/*  282 */         cmpIntradocDir = cmpIntradocDir.toLowerCase();
/*  283 */         cmpAbsPath = cmpAbsPath.toLowerCase();
/*  284 */         cmpCustomDir = cmpCustomDir.toLowerCase();
/*  285 */         cmpSystemDir = cmpSystemDir.toLowerCase();
/*      */       }
/*  287 */       String source = null;
/*  288 */       String location = null;
/*      */ 
/*  293 */       String rootPath = null;
/*  294 */       int index = this.m_component.m_absCompDir.indexOf("/" + this.m_component.m_name + "/");
/*  295 */       if (index >= 0)
/*      */       {
/*  297 */         rootPath = this.m_component.m_absCompDir.substring(0, index + 1);
/*      */       }
/*      */       else
/*      */       {
/*  301 */         rootPath = customDir;
/*      */       }
/*  303 */       String intradocDir = this.m_intradocDir;
/*  304 */       boolean calcSource = true;
/*      */ 
/*  306 */       if (cmpAbsPath.startsWith(cmpCustomDir))
/*      */       {
/*  308 */         location = absPath.substring(customDir.length(), absPath.length());
/*  309 */         calcSource = false;
/*      */       }
/*  311 */       else if (cmpAbsPath.startsWith(cmpSystemDir))
/*      */       {
/*  313 */         location = absPath.substring(systemDir.length(), absPath.length());
/*  314 */         calcSource = false;
/*      */       }
/*  316 */       else if (cmpAbsPath.startsWith(cmpIntradocDir))
/*      */       {
/*  318 */         location = absPath.substring(intradocDir.length(), absPath.length());
/*      */       }
/*      */ 
/*  322 */       if (calcSource)
/*      */       {
/*  324 */         source = FileUtils.getDirectory(absPath);
/*  325 */         index = source.lastIndexOf(47);
/*  326 */         source = source.substring(index + 1, source.length());
/*      */       }
/*      */ 
/*  330 */       this.m_sourcePath = source;
/*  331 */       this.m_root = rootPath;
/*  332 */       this.m_addSourcePath = calcSource;
/*  333 */       this.m_location = location;
/*      */ 
/*  335 */       if ((this.m_buildType == 1) || (this.m_buildType == 3))
/*      */       {
/*  337 */         props.put("filePath", this.m_component.m_absCompDir + this.m_manifestName);
/*      */       }
/*      */ 
/*  340 */       String version = this.m_component.m_binder.getLocal("version");
/*  341 */       if (version == null)
/*      */       {
/*  343 */         version = "";
/*      */       }
/*  345 */       if ((this.m_buildType == 0) && (version.length() == 0))
/*      */       {
/*  347 */         IdcDateFormat fmt = new IdcDateFormat();
/*      */         try
/*      */         {
/*  350 */           fmt.init("yyyy_MM_dd");
/*  351 */           version = fmt.format(new Date());
/*  352 */           version = version + LocaleResources.localizeMessage("!csCompWizInitialBuildLabel", null);
/*      */         }
/*      */         catch (Exception ignore)
/*      */         {
/*  357 */           if (SystemUtils.m_verbose)
/*      */           {
/*  359 */             Report.debug("systemparse", null, ignore);
/*      */           }
/*      */         }
/*      */       }
/*  363 */       props.put("version", version);
/*      */ 
/*  365 */       addToProperties(props, "hasPreferenceData", "false", this.m_component.m_binder, true);
/*      */ 
/*  368 */       String[][] settingInfo = CWizardUtils.ADVANCED_SETTINGS_INFO;
/*  369 */       for (int i = 0; i < settingInfo.length; ++i)
/*      */       {
/*  371 */         String name = settingInfo[i][0];
/*  372 */         String type = settingInfo[i][3];
/*  373 */         String defValue = "";
/*  374 */         boolean isBool = false;
/*  375 */         if (type.equals("bool"))
/*      */         {
/*  377 */           defValue = "false";
/*  378 */           isBool = true;
/*      */         }
/*  380 */         addToProperties(props, name, defValue, this.m_component.m_binder, isBool);
/*      */       }
/*      */ 
/*  383 */       updateListData(isCreate);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean readDefinitionFiles()
/*      */     throws ServiceException, DataException
/*      */   {
/*  396 */     boolean isCreate = false;
/*  397 */     this.m_manifestData = new DataBinder();
/*      */ 
/*  401 */     if (this.m_component == null)
/*      */     {
/*  403 */       throw new ServiceException("!csCompWizComponentInfoNotLoaded");
/*      */     }
/*      */ 
/*  406 */     String tempFile = this.m_component.m_absCompDir + this.m_manifestName;
/*      */ 
/*  413 */     isCreate = FileUtils.checkFile(tempFile, true, false) == -16;
/*  414 */     if (!isCreate)
/*      */     {
/*  416 */       ResourceUtils.serializeDataBinder(this.m_component.m_absCompDir, this.m_manifestName, this.m_manifestData, false, true);
/*      */     }
/*      */ 
/*  420 */     if ((this.m_buildType == 3) && (isCreate))
/*      */     {
/*  422 */       String tmpZipPath = this.m_component.m_absCompDir + this.m_component.m_name + ".zip";
/*  423 */       if (FileUtils.checkFile(tmpZipPath, true, false) == 0)
/*      */       {
/*  425 */         this.m_manifestData = this.m_installer.readManifestInfoFromZip(tmpZipPath);
/*  426 */         if (this.m_manifestData == null)
/*      */         {
/*  428 */           throw new ServiceException(null, "csCompWizUnableToReadZipContents", new Object[] { tmpZipPath });
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  434 */         tmpZipPath = this.m_component.m_absCompDir + "manifest.zip";
/*  435 */         if (FileUtils.checkFile(tempFile, true, false) == -16)
/*      */         {
/*  437 */           if (CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizUnableToUninstallNoManifest", new Object[] { this.m_component.m_name }), 4) == 2)
/*      */           {
/*  441 */             this.m_isRemove = true;
/*      */           }
/*      */           else
/*      */           {
/*  445 */             this.m_isError = true;
/*      */           }
/*      */         }
/*      */         else
/*      */         {
/*  450 */           this.m_manifestData = this.m_installer.readManifestInfoFromZip(tmpZipPath);
/*  451 */           if (this.m_manifestData == null)
/*      */           {
/*  453 */             throw new ServiceException(null, "csCompWizUnableToReadZipContents", new Object[] { tmpZipPath });
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  459 */       if ((!this.m_isError) && (!this.m_isRemove))
/*      */       {
/*  461 */         this.m_helper.m_props.put("filePath", tmpZipPath);
/*  462 */         isCreate = false;
/*      */       }
/*      */     }
/*      */ 
/*  466 */     if ((this.m_isError) || (this.m_isRemove))
/*      */     {
/*  468 */       isCreate = false;
/*      */     }
/*  470 */     return isCreate;
/*      */   }
/*      */ 
/*      */   protected void updateListData(boolean isCreate)
/*      */   {
/*  475 */     String location = this.m_location;
/*  476 */     if (this.m_addSourcePath)
/*      */     {
/*  478 */       location = this.m_sourcePath + "/" + this.m_component.m_filename;
/*  479 */       this.m_manifestData.putLocal("component@" + location + ".source", this.m_root);
/*      */     }
/*      */ 
/*  482 */     if (isCreate)
/*      */     {
/*  484 */       addListDataRow("component", location);
/*      */     }
/*      */ 
/*  487 */     String readmePath = this.m_component.m_absCompDir + "readme.txt";
/*  488 */     if (FileUtils.checkFile(readmePath, true, false) >= 0)
/*      */     {
/*  490 */       if (this.m_addSourcePath)
/*      */       {
/*  492 */         this.m_manifestData.putLocal("componentextra@" + this.m_sourcePath + "/" + "readme.txt.source", this.m_root);
/*      */       }
/*      */ 
/*  495 */       readmePath = location.substring(0, location.lastIndexOf("/") + 1) + "readme.txt";
/*      */ 
/*  497 */       if (isCreate)
/*      */       {
/*  499 */         addListDataRow("componentExtra", readmePath);
/*      */       }
/*      */     }
/*      */ 
/*  503 */     if (!StringUtils.convertToBool(this.m_component.m_binder.getLocal("hasInstallStrings"), false)) {
/*      */       return;
/*      */     }
/*  506 */     String stringFile = this.m_component.m_absCompDir + "install_strings.htm";
/*  507 */     if (FileUtils.checkFile(stringFile, true, false) < 0)
/*      */       return;
/*  509 */     if (this.m_addSourcePath)
/*      */     {
/*  511 */       this.m_manifestData.putLocal("componentextra@" + this.m_sourcePath + "/" + "install_strings.htm.source", this.m_root);
/*      */     }
/*      */ 
/*  514 */     stringFile = location.substring(0, location.lastIndexOf("/") + 1) + "install_strings.htm";
/*      */ 
/*  517 */     if (isCreate)
/*      */     {
/*  519 */       addListDataRow("componentExtra", stringFile);
/*      */     }
/*      */     else
/*      */     {
/*  524 */       DataResultSet drset = (DataResultSet)this.m_manifestData.getResultSet(this.m_manifestTableName);
/*      */ 
/*  526 */       if ((drset == null) || (drset.findRow(1, stringFile) != null))
/*      */         return;
/*  528 */       addListDataRow("componentExtra", stringFile);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addToProperties(Properties props, String name, String defVal, DataBinder binder, boolean isBool)
/*      */   {
/*  538 */     String val = null;
/*  539 */     if (binder != null)
/*      */     {
/*  541 */       val = binder.getLocal(name);
/*      */     }
/*  543 */     if ((val == null) || (val.length() == 0))
/*      */     {
/*  545 */       val = defVal;
/*      */     }
/*  547 */     if (isBool)
/*      */     {
/*  549 */       val = (StringUtils.convertToBool(val, false)) ? "1" : "0";
/*      */     }
/*  551 */     props.put(name, val);
/*      */   }
/*      */ 
/*      */   public void initUI()
/*      */   {
/*      */     try
/*      */     {
/*  559 */       DialogCallback okCallback = new DialogCallback()
/*      */       {
/*      */         public boolean handleDialogEvent(ActionEvent e)
/*      */         {
/*  564 */           boolean retVal = true;
/*      */           try
/*      */           {
/*  567 */             retVal = ManifestEditorDlg.this.processInstall();
/*      */           }
/*      */           catch (Exception exp)
/*      */           {
/*  571 */             Report.trace("system", null, exp);
/*  572 */             CWizardGuiUtils.reportError(ManifestEditorDlg.this.m_systemInterface, exp, (IdcMessage)null);
/*  573 */             retVal = false;
/*      */ 
/*  576 */             ManifestEditorDlg.this.enableDisableComponent(false);
/*      */           }
/*      */ 
/*  579 */           return retVal;
/*      */         }
/*      */       };
/*  583 */       JPanel mainPanel = getDialogHelper().initStandard(this, okCallback, 1, true, this.m_helpPage);
/*      */ 
/*  586 */       GridBagHelper gbh = this.m_helper.m_gridHelper;
/*  587 */       this.m_list = createUdlPanel("", 500, 15, this.m_manifestTableName, true, MANIFEST_COL_MAP, "id", false);
/*      */ 
/*  590 */       this.m_hasPrefCheckbox = new CustomCheckbox("   ", 1);
/*  591 */       this.m_hasPrefCheckbox.addItemListener(this);
/*  592 */       if (this.m_buildType == 0)
/*      */       {
/*  594 */         this.m_helper.addLabelEditPair(mainPanel, LocaleResources.getString("csCompWizLabelVersion", null), 10, "version");
/*      */ 
/*  597 */         this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizHasPreferenceData", null), this.m_hasPrefCheckbox, "hasPreferenceData");
/*      */ 
/*  601 */         addAdvButton(mainPanel, false);
/*  602 */         JPanel btnPanel = new PanePanel();
/*  603 */         addCommandButtons(btnPanel);
/*  604 */         this.m_list.add("East", btnPanel);
/*      */       }
/*  606 */       else if ((this.m_buildType == 1) || (this.m_buildType == 3))
/*      */       {
/*  608 */         Properties props = this.m_helper.m_props;
/*  609 */         gbh.prepareAddLastRowElement(17);
/*  610 */         LongTextCustomLabel cl = new LongTextCustomLabel();
/*  611 */         cl.setMinWidth(400);
/*  612 */         cl.getClass(); cl.setBreakValue(-1);
/*  613 */         this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizManifestPath", null), cl, "filePath");
/*      */ 
/*  616 */         String version = props.getProperty("version");
/*  617 */         if ((version != null) && (version.length() > 0))
/*      */         {
/*  619 */           this.m_helper.addLabelDisplayPair(mainPanel, LocaleResources.getString("csCompWizLabelVersion", null), 30, "version");
/*      */         }
/*      */ 
/*  623 */         if (StringUtils.convertToBool(props.getProperty("hasPreferenceData"), false))
/*      */         {
/*  626 */           this.m_helper.addLabelFieldPair(mainPanel, LocaleResources.getString("csCompWizHasPreferenceData", null), this.m_hasPrefCheckbox, "hasPreferenceData");
/*      */ 
/*  629 */           this.m_hasPrefCheckbox.setEnabled(false);
/*      */         }
/*      */ 
/*  632 */         addAdvButton(mainPanel, true);
/*      */       }
/*      */       else
/*      */       {
/*  637 */         this.m_list.add("North", addZipFilePath());
/*      */       }
/*      */ 
/*  640 */       gbh.m_gc.weighty = 1.0D;
/*  641 */       gbh.m_gc.fill = 1;
/*  642 */       gbh.prepareAddLastRowElement();
/*  643 */       this.m_helper.addComponent(mainPanel, this.m_list);
/*  644 */       gbh.m_gc.weighty = 0.0D;
/*      */ 
/*  646 */       refreshList(null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  650 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*  651 */       this.m_isError = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataResultSet getManifestData()
/*      */   {
/*  658 */     DataResultSet data = null;
/*  659 */     if (this.m_manifestData != null)
/*      */     {
/*  661 */       data = (DataResultSet)this.m_manifestData.getResultSet(this.m_manifestTableName);
/*      */     }
/*  663 */     return data;
/*      */   }
/*      */ 
/*      */   public String calculateRootPrefix(String entryType)
/*      */   {
/*  668 */     String rootPrefix = null;
/*  669 */     if ((entryType.equals("component")) || (entryType.equals("componentExtra")) || (entryType.equals("componentClasses")) || (entryType.equals("componentLib")))
/*      */     {
/*  672 */       int index = -1;
/*  673 */       if (this.m_component != null)
/*      */       {
/*  675 */         index = this.m_component.m_absCompDir.indexOf("/" + this.m_component.m_name + "/");
/*      */       }
/*  677 */       if (index >= 0)
/*      */       {
/*  679 */         rootPrefix = this.m_component.m_absCompDir.substring(0, index + 1);
/*      */       }
/*      */     }
/*      */ 
/*  683 */     if (rootPrefix == null)
/*      */     {
/*  685 */       rootPrefix = this.m_installer.expandSourceDirectory(entryType);
/*      */     }
/*  687 */     return rootPrefix;
/*      */   }
/*      */ 
/*      */   public Vector getSucessfulComponents()
/*      */   {
/*  692 */     return this.m_installer.getSucessfulComponents();
/*      */   }
/*      */ 
/*      */   public String getFilePath()
/*      */   {
/*  697 */     return this.m_helper.m_props.getProperty("filePath");
/*      */   }
/*      */ 
/*      */   protected void addCommandButtons(JPanel panel)
/*      */   {
/*  702 */     this.m_helper.makePanelGridBag(panel, 2);
/*      */ 
/*  704 */     Insets stdIns = new Insets(5, 5, 5, 5);
/*  705 */     this.m_helper.m_gridHelper.m_gc.insets = stdIns;
/*      */ 
/*  707 */     for (int i = 0; i < this.COMMAND_LIST.length; ++i)
/*      */     {
/*  709 */       if (i == 0)
/*      */       {
/*  711 */         stdIns = new Insets(30, 5, 5, 5);
/*      */       }
/*  713 */       boolean isControlled = StringUtils.convertToBool(this.COMMAND_LIST[i][2], false);
/*  714 */       JButton btn = this.m_list.addButton(LocaleResources.getString(this.COMMAND_LIST[i][0], null), isControlled);
/*      */ 
/*  716 */       this.m_helper.addLastComponentInRow(panel, btn);
/*      */ 
/*  718 */       btn.setActionCommand(this.COMMAND_LIST[i][1]);
/*  719 */       btn.addActionListener(this);
/*      */     }
/*      */   }
/*      */ 
/*      */   public JPanel addZipFilePath()
/*      */   {
/*  725 */     JPanel mainPanel = new PanePanel();
/*  726 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*      */ 
/*  728 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*  729 */     GridBagConstraints gc = gbh.m_gc;
/*  730 */     int oldfill = gc.fill;
/*      */ 
/*  732 */     JButton btn = new JButton(LocaleResources.getString("apLabelSelectButton", null));
/*      */ 
/*  734 */     ActionListener bListener = new ActionListener()
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/*  738 */         JFileChooser fileDlg = new IdcFileChooser();
/*  739 */         fileDlg.setDialogTitle(LocaleResources.getString("csCompWizLabelZipFilePath", null));
/*  740 */         fileDlg.showOpenDialog(null);
/*      */ 
/*  743 */         File f = fileDlg.getSelectedFile();
/*  744 */         if (f == null)
/*      */           return;
/*  746 */         String path = f.getAbsolutePath();
/*  747 */         if (!path.endsWith(".zip"))
/*      */         {
/*  749 */           CWizardGuiUtils.reportError(ManifestEditorDlg.this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizZipFileExtError", new Object[0]));
/*      */ 
/*  751 */           return;
/*      */         }
/*  753 */         ManifestEditorDlg.this.m_helper.m_props.put("filePath", path);
/*  754 */         ManifestEditorDlg.this.m_helper.loadComponentValues();
/*  755 */         ManifestEditorDlg.this.retrieveManifestInfo();
/*      */       }
/*      */     };
/*  760 */     btn.addActionListener(bListener);
/*      */ 
/*  762 */     gbh.prepareAddRowElement(17);
/*  763 */     this.m_helper.addComponent(mainPanel, new CustomLabel(LocaleResources.getString("csCompWizSelectZip", null), 1));
/*      */ 
/*  767 */     gc.fill = 0;
/*  768 */     gbh.prepareAddRowElement(17);
/*  769 */     this.m_helper.addComponent(mainPanel, btn);
/*  770 */     gc.fill = oldfill;
/*      */ 
/*  772 */     gbh.addEmptyRow(mainPanel);
/*      */ 
/*  774 */     return mainPanel;
/*      */   }
/*      */ 
/*      */   protected void addListDataRow(String entryType, String location)
/*      */   {
/*  779 */     Vector v = this.m_listData.createEmptyRow();
/*      */ 
/*  781 */     v.setElementAt(Integer.toString(this.m_idCount++), 0);
/*  782 */     v.setElementAt(entryType, 1);
/*  783 */     v.setElementAt(CWizardUtils.findDisplayName(FILE_TYPES, entryType), 2);
/*  784 */     v.setElementAt(calculateRootPrefix(entryType), 3);
/*  785 */     v.setElementAt(location, 4);
/*  786 */     this.m_listData.addRow(v);
/*      */   }
/*      */ 
/*      */   protected void enableDisableComponent(boolean doEnable)
/*      */   {
/*      */     try
/*      */     {
/*  793 */       String componentName = this.m_resDefData.getLocal("ComponentName");
/*  794 */       ComponentListEditor cle = ((CWizardFrame)getDialogHelper().m_parent).m_manager.m_editor;
/*  795 */       if (cle.isComponentEnabled(componentName) != doEnable)
/*      */       {
/*  797 */         cle.enableOrDisableComponent(componentName, doEnable);
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  802 */       Report.error(null, null, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean processInstall() throws ServiceException, DataException
/*      */   {
/*  808 */     boolean retVal = true;
/*  809 */     String errMsg = null;
/*  810 */     String filePath = this.m_helper.m_props.getProperty("filePath");
/*  811 */     if (this.m_buildType == 0)
/*      */     {
/*  813 */       writeManifest();
/*      */     }
/*  815 */     else if (this.m_buildType == 1)
/*      */     {
/*  818 */       ResourceUtils.serializeDataBinder(this.m_component.m_absCompDir, this.m_manifestName, this.m_manifestData, false, false);
/*      */     }
/*  821 */     else if (this.m_buildType == 2)
/*      */     {
/*  823 */       if ((filePath == null) || (filePath.length() == 0))
/*      */       {
/*  825 */         throw new ServiceException("!csCompWizSelectZip");
/*      */       }
/*      */ 
/*  828 */       int result = FileUtils.checkFile(filePath, true, false);
/*  829 */       if (result != 0)
/*      */       {
/*  831 */         errMsg = FileUtils.getErrorMsg(filePath, true, result);
/*  832 */         throw new ServiceException(LocaleUtils.appendMessage(errMsg, LocaleUtils.encodeMessage("csCompWizZipFileError", null, filePath)));
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  837 */     if (this.m_buildType != 0)
/*      */     {
/*  842 */       DataBinder workingBinder = this.m_manifestData.createShallowCopy();
/*  843 */       DataResultSet tempRset = (DataResultSet)this.m_manifestData.getResultSet("Manifest");
/*  844 */       if (tempRset == null)
/*      */       {
/*  846 */         throw new ServiceException(null, "csCompWizUnableToReadManifestFile", new Object[] { this.m_component.m_absCompDir });
/*      */       }
/*      */ 
/*  849 */       DataResultSet manifest = new DataResultSet();
/*      */ 
/*  851 */       manifest.copy(tempRset);
/*  852 */       workingBinder.m_localData = ((Properties)this.m_manifestData.m_localData.clone());
/*  853 */       workingBinder.addResultSet(this.m_manifestTableName, tempRset);
/*      */ 
/*  855 */       String compName = null;
/*  856 */       String location = null;
/*  857 */       String installID = null;
/*  858 */       Map args = new HashMap();
/*  859 */       if ((this.m_buildType == 2) || (this.m_buildType == 3))
/*      */       {
/*  862 */         String[] retStr = this.m_installer.retrieveComponentNameAndLocation(this.m_manifestData);
/*  863 */         location = retStr[0];
/*  864 */         compName = retStr[1];
/*      */ 
/*  866 */         if (this.m_buildType == 2)
/*      */         {
/*  870 */           this.m_resDefData = ZipFunctions.extractFileAsDataBinder(filePath, "component/" + location);
/*      */ 
/*  872 */           if (this.m_resDefData == null)
/*      */           {
/*  874 */             throw new ServiceException("!csUnableToLoadResourceDefinition");
/*      */           }
/*      */ 
/*  878 */           checkAndInitializeEnvironment(compName, true);
/*      */ 
/*  880 */           installID = this.m_resDefData.getLocal("installID");
/*  881 */           args.put("install", "true");
/*  882 */           args.put("BackupZipName", this.m_installer.getComponentBackupPath(installID, compName));
/*      */ 
/*  884 */           args.put("ZipName", filePath);
/*      */ 
/*  886 */           this.m_installer.checkVersion(compName, this.m_resDefData);
/*      */ 
/*  888 */           if (StringUtils.convertToBool(this.m_resDefData.getLocal("hasPreferenceData"), false))
/*      */           {
/*  893 */             this.m_installer.initEx(compName, this.m_resDefData, this.m_manifestData, args);
/*      */ 
/*  896 */             ComponentPreferenceData prefData = new ComponentPreferenceData();
/*  897 */             this.m_installer.retrievePreferenceData(prefData, filePath, compName, installID);
/*      */ 
/*  899 */             ResourceContainer prefResources = new ResourceContainer();
/*  900 */             if (DataBinderUtils.getBoolean(this.m_resDefData, "hasInstallStrings", false))
/*      */             {
/*  902 */               this.m_installer.retrievePreferenceResources(filePath, compName, prefResources);
/*      */             }
/*      */ 
/*  907 */             CWizardComponentConfigDlg config = new CWizardComponentConfigDlg(this.m_systemInterface, "MyTitle", "MyHelpPage", prefData, prefResources, true);
/*      */ 
/*  910 */             config.init();
/*  911 */             if (config.hasPreferencesToConfigure())
/*      */             {
/*  915 */               retVal = config.configureComponent();
/*  916 */               if (retVal)
/*      */               {
/*  918 */                 this.m_resDefData.m_localData.putAll(prefData.m_configData);
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/*  923 */         else if (this.m_buildType == 3)
/*      */         {
/*  925 */           if ((filePath != null) && (filePath.length() > 0) && (filePath.endsWith(".zip")))
/*      */           {
/*  927 */             args.put("ZipName", filePath);
/*  928 */             this.m_resDefData = ZipFunctions.extractFileAsDataBinder(filePath, "component/" + location);
/*      */           }
/*      */           else
/*      */           {
/*  933 */             args.remove("ZipName");
/*  934 */             ResourceUtils.serializeDataBinder(this.m_component.m_absCompDir, this.m_manifestName, this.m_resDefData, false, false);
/*      */           }
/*      */ 
/*  937 */           installID = this.m_resDefData.getLocal("installID");
/*  938 */           args.put("Uninstall", "true");
/*  939 */           args.put("BackupZipName", this.m_installer.getComponentBackupPath(installID, compName));
/*      */ 
/*  943 */           if (this.m_component != null)
/*      */           {
/*  946 */             checkAndInitializeEnvironment(compName, false);
/*      */           }
/*  948 */           args.put("AbsoluteDir", this.m_component.m_absCompDir);
/*      */         }
/*      */ 
/*  951 */         boolean doNotBackupZip = DataBinderUtils.getBoolean(this.m_resDefData, "disableZipFileBackup", false);
/*      */ 
/*  953 */         if (!doNotBackupZip)
/*      */         {
/*  955 */           args.put("Backup", "true");
/*  956 */           args.put("Overwrite", "false");
/*      */         }
/*      */         else
/*      */         {
/*  960 */           args.put("Backup", "false");
/*  961 */           args.put("Overwrite", "true");
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  966 */         validateData();
/*  967 */         workingBinder.copyLocalDataStateShallow(this.m_manifestData);
/*  968 */         args.put("Build", "true");
/*  969 */         args.put("NewZipName", this.m_component.m_absCompDir + this.m_component.m_name + ".zip");
/*      */ 
/*  971 */         args.put("BackupZipName", this.m_component.m_absCompDir + this.m_component.m_name + this.m_dateStr + ".zip");
/*      */ 
/*  973 */         args.put("AbsoluteDir", this.m_component.m_absCompDir);
/*      */ 
/*  975 */         String filter = this.m_component.m_binder.getLocal("DirectoryFilter");
/*  976 */         if (filter != null)
/*      */         {
/*  978 */           workingBinder.putLocal("DirectoryFilter", filter);
/*      */         }
/*      */ 
/*  981 */         compName = this.m_component.m_name;
/*      */ 
/*  984 */         ComponentPackager packager = new ComponentPackager();
/*  985 */         File componentDir = new File(this.m_component.m_absCompDir);
/*  986 */         packager.init(componentDir);
/*  987 */         packager.m_componentBinder = this.m_component.m_binder;
/*  988 */         packager.stampVersion();
/*  989 */         this.m_component.updateResDefFile();
/*      */       }
/*      */ 
/*  992 */       if (retVal)
/*      */       {
/*  994 */         workingBinder.putLocal("ComponentName", compName);
/*  995 */         this.m_installer.executeInstaller(this.m_resDefData, workingBinder, installID, compName, args);
/*      */ 
/*  997 */         if (this.m_buildType == 2)
/*      */         {
/*  999 */           doInstallExtra(workingBinder, compName, location, filePath, installID);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1004 */     return retVal;
/*      */   }
/*      */ 
/*      */   protected void writeManifest() throws ServiceException, DataException
/*      */   {
/* 1009 */     Properties props = this.m_helper.m_props;
/* 1010 */     boolean hasPrefData = StringUtils.convertToBool(props.getProperty("hasPreferenceData"), false);
/*      */ 
/* 1012 */     String instId = props.getProperty("installID");
/* 1013 */     if ((hasPrefData) && (((instId == null) || (instId.length() == 0))))
/*      */     {
/* 1015 */       throw new ServiceException("!csInstallIdRequired");
/*      */     }
/*      */ 
/* 1018 */     String version = props.getProperty("version");
/* 1019 */     if ((version != null) && (version.length() > 0))
/*      */     {
/* 1021 */       this.m_component.m_binder.putLocal("version", version);
/*      */     }
/*      */ 
/* 1024 */     this.m_component.m_binder.putLocal("ComponentName", this.m_component.m_name);
/*      */ 
/* 1027 */     DataResultSet drset = new DataResultSet(this.MANIFEST_FIELD_INFO);
/*      */ 
/* 1030 */     int entryIndex = ResultSetUtils.getIndexMustExist(this.m_listData, "entryType");
/* 1031 */     int locIndex = ResultSetUtils.getIndexMustExist(this.m_listData, "location");
/* 1032 */     for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*      */     {
/* 1034 */       Vector v = this.m_listData.getCurrentRowValues();
/* 1035 */       Vector copyVector = drset.createEmptyRow();
/* 1036 */       copyVector.setElementAt(v.elementAt(entryIndex), 0);
/* 1037 */       copyVector.setElementAt(v.elementAt(locIndex), 1);
/* 1038 */       drset.addRow(copyVector);
/*      */     }
/* 1040 */     this.m_manifestData.addResultSet(this.m_manifestTableName, drset);
/*      */ 
/* 1043 */     addToBinder(props, "hasPreferenceData", "bool", this.m_component.m_binder);
/* 1044 */     String[][] settingsInfo = CWizardUtils.ADVANCED_SETTINGS_INFO;
/* 1045 */     for (int i = 0; i < settingsInfo.length; ++i)
/*      */     {
/* 1047 */       String name = settingsInfo[i][0];
/* 1048 */       String type = settingsInfo[i][3];
/* 1049 */       addToBinder(props, name, type, this.m_component.m_binder);
/*      */     }
/*      */ 
/* 1053 */     this.m_component.updateResDefFile();
/* 1054 */     ResourceUtils.serializeDataBinder(this.m_component.m_absCompDir, this.m_manifestName, this.m_manifestData, true, false);
/*      */   }
/*      */ 
/*      */   protected boolean doInstallExtra(DataBinder workingBinder, String compName, String location, String filePath, String installID)
/*      */     throws ServiceException, DataException
/*      */   {
/* 1061 */     this.m_installer.doInstallExtra(this.m_resDefData, compName, location, filePath, installID);
/*      */ 
/* 1065 */     Vector preventedComponents = this.m_installer.getPreventedBundledComponents();
/* 1066 */     if (preventedComponents.size() > 0)
/*      */     {
/* 1068 */       String componentString = StringUtils.createStringSimple(preventedComponents);
/* 1069 */       IdcMessage msg = IdcMessageFactory.lc("csComponentsInstallPrevented", new Object[] { componentString, compName });
/*      */ 
/* 1071 */       CWizardGuiUtils.doMessage(this.m_systemInterface, null, msg, 1);
/*      */     }
/*      */ 
/* 1075 */     Vector v = this.m_installer.getSucessfulComponents();
/* 1076 */     if ((v == null) || (v.size() == 0))
/*      */     {
/* 1078 */       return false;
/*      */     }
/* 1080 */     String installedComponents = StringUtils.createString(v, '\n', '^');
/* 1081 */     ComponentListEditor compLE = ComponentListManager.getEditor();
/* 1082 */     compLE.loadComponents();
/*      */ 
/* 1085 */     Vector enabledList = this.m_installer.getEnabledComponents();
/*      */     boolean enableTheseComponents;
/*      */     boolean enableTheseComponents;
/* 1087 */     if ((enabledList != null) && (enabledList.size() > 0))
/*      */     {
/* 1090 */       CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizInstallEnabledComponentsInfoMsg", new Object[] { installedComponents }), 1);
/*      */ 
/* 1093 */       enableTheseComponents = true;
/*      */     }
/*      */     else
/*      */     {
/* 1097 */       enableTheseComponents = CWizardGuiUtils.doMessage(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizAskEnableInstalledComponents", new Object[] { installedComponents }), 4) == 2;
/*      */     }
/*      */ 
/* 1101 */     if (enableTheseComponents)
/*      */     {
/* 1103 */       for (int i = 0; i < v.size(); ++i)
/*      */       {
/* 1105 */         String componentName = (String)v.elementAt(i);
/* 1106 */         compLE.enableOrDisableComponent(componentName, true);
/*      */       }
/*      */     }
/* 1109 */     return true;
/*      */   }
/*      */ 
/*      */   protected void checkAndInitializeEnvironment(String compName, boolean isInstall)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1119 */     boolean isLightWeight = CWizardUtils.m_isLightWeightCW;
/*      */ 
/* 1121 */     if (!ComponentWizardManager.m_isEnvironmentLoaded)
/*      */     {
/* 1123 */       ComponentWizardManager.initFullEnvironment(isLightWeight);
/*      */ 
/* 1125 */       if ((!isLightWeight) && (ComponentWizardManager.m_workspace == null))
/*      */       {
/* 1128 */         throw new ServiceException(null, "csCompWizUnableToRunUninstallFilter", new Object[] { compName });
/*      */       }
/*      */     }
/*      */ 
/* 1132 */     SharedObjects.putEnvironmentValue("ComponentName", compName);
/* 1133 */     String filterType = compName + "ComponentUninstallFilter";
/* 1134 */     DataResultSet filters = null;
/* 1135 */     if (!isInstall)
/*      */     {
/* 1137 */       filters = this.m_component.getFiltersTable();
/*      */     }
/*      */ 
/* 1141 */     ExecutionContext cxt = new ExecutionContextAdaptor();
/* 1142 */     if ((filters == null) || (filters.isEmpty()))
/*      */       return;
/* 1144 */     Vector cuFilter = filters.findRow(0, filterType);
/* 1145 */     if (cuFilter == null) {
/*      */       return;
/*      */     }
/* 1148 */     if (!PluginFilters.hasFilter(filterType))
/*      */     {
/* 1150 */       Vector filterList = new IdcVector();
/* 1151 */       PluginFilterData data = new PluginFilterData();
/* 1152 */       data.m_filterType = ((String)cuFilter.elementAt(0));
/* 1153 */       data.m_location = ((String)cuFilter.elementAt(1));
/* 1154 */       data.m_parameter = ((String)cuFilter.elementAt(2));
/* 1155 */       filterList.addElement(data);
/*      */ 
/* 1158 */       PluginFilters.registerFilters(filterList);
/*      */     }
/* 1160 */     PluginFilters.filter(filterType, ComponentWizardManager.m_workspace, null, cxt);
/*      */   }
/*      */ 
/*      */   protected void validateData()
/*      */     throws ServiceException
/*      */   {
/* 1167 */     String installId = this.m_component.m_binder.getLocal("installID");
/* 1168 */     boolean hasPrefData = StringUtils.convertToBool(this.m_component.m_binder.getLocal("hasPreferenceData"), false);
/*      */ 
/* 1170 */     if ((hasPrefData) && (((installId == null) || (installId.length() == 0))))
/*      */     {
/* 1172 */       throw new ServiceException("!csCompWizInstallIDNotSpecified");
/*      */     }
/*      */ 
/* 1175 */     String addComps = this.m_component.m_binder.getLocal("additionalComponents");
/* 1176 */     if ((addComps == null) || (addComps.length() <= 0))
/*      */       return;
/* 1178 */     Vector v = StringUtils.parseArray(addComps, ',', '^');
/* 1179 */     for (int i = 0; i < v.size(); ++i)
/*      */     {
/* 1181 */       String temp = (String)v.elementAt(i);
/* 1182 */       if ((temp == null) || (temp.length() <= 0))
/*      */         continue;
/* 1184 */       Vector vv = StringUtils.parseArray(temp, ':', '^');
/* 1185 */       if ((vv == null) || (vv.size() != 3))
/*      */         continue;
/* 1187 */       String aCompName = (String)vv.elementAt(0);
/* 1188 */       String aCompZip = (String)vv.elementAt(1);
/* 1189 */       String param = (String)vv.elementAt(2);
/* 1190 */       if ((aCompName == null) || (aCompName.length() == 0))
/*      */       {
/* 1192 */         throw new ServiceException(null, "csAdditionalCompNameMissing", new Object[0]);
/*      */       }
/*      */ 
/* 1195 */       if ((aCompZip != null) && (aCompZip.length() > 0))
/*      */       {
/* 1197 */         boolean isZipIncluded = false;
/* 1198 */         for (this.m_listData.first(); this.m_listData.isRowPresent(); this.m_listData.next())
/*      */         {
/* 1200 */           String entryType = this.m_listData.getStringValue(1);
/* 1201 */           String location = this.m_listData.getStringValue(4);
/* 1202 */           if ((!entryType.equalsIgnoreCase("componentExtra")) || (!location.endsWith(aCompZip))) {
/*      */             continue;
/*      */           }
/* 1205 */           isZipIncluded = true;
/* 1206 */           break;
/*      */         }
/*      */ 
/* 1210 */         if (!isZipIncluded)
/*      */         {
/* 1212 */           throw new ServiceException(null, "csCompWizMustIncludeAddCompZip", new Object[] { aCompZip });
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1218 */         throw new ServiceException(null, "csAdditionalCompZipNameMissing", new Object[0]);
/*      */       }
/*      */ 
/* 1221 */       if ((param == null) || (param.length() <= 0) || (param.startsWith("<$")))
/*      */         continue;
/* 1223 */       if (!hasPrefData)
/*      */       {
/* 1225 */         throw new ServiceException(null, "csCompWizMustIncludePrefData", new Object[0]);
/*      */       }
/* 1227 */       String prefPath = this.m_component.m_absCompDir;
/* 1228 */       if (FileUtils.checkFile(prefPath + "preference.hda", true, false) != 0)
/*      */       {
/* 1231 */         throw new ServiceException("!csCompWizMustDefineIncludePrefData");
/*      */       }
/* 1233 */       ComponentPreferenceData prefData = new ComponentPreferenceData(prefPath, null);
/* 1234 */       prefData.load();
/* 1235 */       DataResultSet drset = prefData.getPreferenceTable();
/* 1236 */       if (drset.findRow(0, param) != null)
/*      */         continue;
/* 1238 */       throw new ServiceException(null, "csCompWizMustDefineParamInPrefData", new Object[] { param });
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addToBinder(Properties props, String name, String type, DataBinder binder)
/*      */   {
/* 1249 */     String val = props.getProperty(name);
/* 1250 */     if (val == null)
/*      */     {
/* 1252 */       if (type.equals("bool"))
/*      */       {
/* 1254 */         val = "false";
/*      */       }
/* 1256 */       else if (type.equals("int"))
/*      */       {
/* 1258 */         val = "1";
/*      */       }
/*      */       else
/*      */       {
/* 1262 */         val = "";
/*      */       }
/*      */     }
/* 1265 */     if (val.length() == 0)
/*      */     {
/* 1267 */       binder.removeLocal(name);
/*      */     }
/*      */     else
/*      */     {
/* 1271 */       binder.putLocal(name, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void retrieveManifestInfo()
/*      */   {
/*      */     try
/*      */     {
/* 1279 */       this.m_helper.retrieveComponentValues();
/* 1280 */       String zipFilePath = this.m_helper.m_props.getProperty("filePath");
/* 1281 */       this.m_manifestData = this.m_installer.readManifestInfoFromZip(zipFilePath);
/* 1282 */       if (this.m_manifestData == null)
/*      */       {
/* 1284 */         throw new ServiceException(null, "csCompWizUnableToReadManifestHda", new Object[] { zipFilePath });
/*      */       }
/*      */ 
/* 1287 */       initListData();
/* 1288 */       refreshList(null);
/*      */     }
/*      */     catch (Exception exception)
/*      */     {
/* 1292 */       CWizardGuiUtils.reportError(this.m_systemInterface, exception, (IdcMessage)null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void actionPerformed(ActionEvent e)
/*      */   {
/* 1301 */     String cmdStr = e.getActionCommand();
/*      */ 
/* 1303 */     if (cmdStr.equals("remove"))
/*      */     {
/* 1305 */       remove();
/*      */     } else {
/* 1307 */       if (!cmdStr.equals("add"))
/*      */         return;
/* 1309 */       add();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void add()
/*      */   {
/* 1315 */     this.m_dlgHelper = new DialogHelper(this.m_systemInterface, LocaleResources.getString("csCompWizCommandAdd2", null), true);
/*      */ 
/* 1317 */     this.m_dlgHelper.m_helpPage = DialogHelpTable.getHelpPage("CW_AddBuildSettingsEntry");
/*      */ 
/* 1319 */     JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 1320 */     this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/* 1321 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/*      */ 
/* 1323 */     DisplayChoice choice = new DisplayChoice();
/* 1324 */     choice.init(FILE_TYPES);
/*      */ 
/* 1326 */     ItemListener listener = new ItemListener(choice)
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/* 1330 */         int state = e.getStateChange();
/* 1331 */         if (state != 1)
/*      */         {
/* 1333 */           return;
/*      */         }
/*      */ 
/* 1336 */         String entryType = this.val$choice.getSelectedInternalValue();
/* 1337 */         String rootPrefix = ManifestEditorDlg.this.calculateRootPrefix(entryType);
/* 1338 */         String location = ManifestEditorDlg.this.m_component.m_name;
/* 1339 */         boolean isCompLib = entryType.equals("componentLib");
/* 1340 */         boolean isCompClasses = entryType.equals("componentClasses");
/* 1341 */         boolean isClasses = entryType.equals("classes");
/*      */ 
/* 1343 */         if (isCompLib)
/*      */         {
/* 1345 */           location = ManifestEditorDlg.this.m_component.m_name + "/lib";
/*      */         }
/* 1347 */         else if (isCompClasses)
/*      */         {
/* 1349 */           location = ManifestEditorDlg.this.m_component.m_name + "/classes";
/*      */         }
/*      */ 
/* 1353 */         boolean flag = (isCompClasses) || (isClasses);
/* 1354 */         ManifestEditorDlg.this.m_makeJarCheckbox.setEnabled(flag);
/* 1355 */         ManifestEditorDlg.this.m_incJavaSourceCheckbox.setEnabled(false);
/* 1356 */         ManifestEditorDlg.this.m_makeJarCheckbox.setSelected(false);
/* 1357 */         ManifestEditorDlg.this.m_incJavaSourceCheckbox.setSelected(false);
/*      */ 
/* 1359 */         ManifestEditorDlg.this.m_dlgHelper.m_exchange.setComponentValue("location", location);
/* 1360 */         ManifestEditorDlg.this.m_dlgHelper.m_exchange.setComponentValue("rootPrefix", rootPrefix);
/*      */       }
/*      */     };
/* 1363 */     choice.addItemListener(listener);
/*      */ 
/* 1365 */     this.m_dlgHelper.addLabelFieldPairEx(mainPanel, LocaleResources.getString("csCompWizLabelEntryType2", null), choice, "entryType", false);
/*      */ 
/* 1367 */     this.m_dlgHelper.m_gridHelper.addEmptyRow(mainPanel);
/* 1368 */     this.m_dlgHelper.addLabelDisplayPair(mainPanel, LocaleResources.getString("csCompWizLabelRootPrefix2", null), 40, "rootPrefix");
/*      */ 
/* 1370 */     this.m_dlgHelper.addLabelEditPair(mainPanel, LocaleResources.getString("csCompWizLabelSubDirOrFile", null), 40, "location");
/*      */ 
/* 1373 */     JPanel cbPanel = new PanePanel();
/* 1374 */     this.m_dlgHelper.makePanelGridBag(cbPanel, 1);
/* 1375 */     this.m_makeJarCheckbox = new CustomCheckbox(LocaleResources.getString("csCompWizMakeJar", null), 1);
/*      */ 
/* 1377 */     this.m_incJavaSourceCheckbox = new CustomCheckbox(LocaleResources.getString("csCompWizIncJavaSource", null), 1);
/*      */ 
/* 1379 */     gbh.prepareAddRowElement(13);
/* 1380 */     this.m_dlgHelper.addComponent(cbPanel, this.m_makeJarCheckbox);
/* 1381 */     gbh.prepareAddLastRowElement(17);
/* 1382 */     this.m_dlgHelper.addComponent(cbPanel, this.m_incJavaSourceCheckbox);
/* 1383 */     this.m_dlgHelper.addLabelFieldPair(mainPanel, "", cbPanel, "");
/* 1384 */     this.m_incJavaSourceCheckbox.setEnabled(false);
/*      */ 
/* 1386 */     ItemListener jarListener = new ItemListener()
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/* 1390 */         int state = e.getStateChange();
/* 1391 */         boolean flag = false;
/* 1392 */         switch (state)
/*      */         {
/*      */         case 1:
/* 1395 */           flag = true;
/*      */         }
/*      */ 
/* 1398 */         ManifestEditorDlg.this.m_incJavaSourceCheckbox.setSelected(false);
/* 1399 */         ManifestEditorDlg.this.m_incJavaSourceCheckbox.setEnabled(flag);
/*      */       }
/*      */     };
/* 1402 */     this.m_makeJarCheckbox.addItemListener(jarListener);
/*      */ 
/* 1404 */     DialogCallback okCallback = new DialogCallback()
/*      */     {
/*      */       public boolean handleDialogEvent(ActionEvent e)
/*      */       {
/* 1409 */         return ManifestEditorDlg.this.onOk();
/*      */       }
/*      */     };
/* 1413 */     this.m_dlgHelper.addOK(okCallback);
/* 1414 */     this.m_dlgHelper.addCancel(null);
/* 1415 */     this.m_dlgHelper.addHelp(null);
/*      */ 
/* 1418 */     this.m_dlgHelper.m_props.put("entryType", "componentClasses");
/* 1419 */     this.m_dlgHelper.m_props.put("rootPrefix", calculateRootPrefix("componentClasses"));
/* 1420 */     this.m_dlgHelper.m_props.put("location", this.m_component.m_name + "/classes");
/*      */ 
/* 1422 */     FieldInfo[] fi = null;
/*      */     try
/*      */     {
/* 1426 */       fi = ResultSetUtils.createInfoList(this.m_listData, new String[] { "entryType", "location" }, true);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1431 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 1432 */       return;
/*      */     }
/*      */ 
/* 1435 */     if (this.m_dlgHelper.prompt() != 1)
/*      */       return;
/* 1437 */     String entryType = choice.getSelectedInternalValue();
/* 1438 */     String location = this.m_dlgHelper.m_props.getProperty("location");
/*      */ 
/* 1440 */     Vector v = this.m_listData.findRow(fi[1].m_index, location);
/* 1441 */     if ((v != null) && (((String)v.elementAt(fi[0].m_index)).equalsIgnoreCase(entryType)))
/*      */     {
/* 1443 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizPromptExistEntryWithLocation", new Object[] { entryType, location }));
/*      */ 
/* 1445 */       return;
/*      */     }
/* 1447 */     addListDataRow(entryType, location);
/* 1448 */     refreshList(null);
/*      */   }
/*      */ 
/*      */   protected void remove()
/*      */   {
/* 1454 */     int index = this.m_list.getSelectedIndex();
/* 1455 */     if (index < 0)
/*      */     {
/* 1457 */       CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizSelectItemToRemove", new Object[0]));
/*      */ 
/* 1459 */       return;
/*      */     }
/*      */ 
/* 1462 */     Properties props = this.m_list.getDataAt(index);
/* 1463 */     String type = props.getProperty("entryType");
/* 1464 */     String location = props.getProperty("location");
/*      */ 
/* 1466 */     IdcMessage prompt = IdcMessageFactory.lc("csCompWizPromptRemove", new Object[] { location });
/* 1467 */     int response = CWizardGuiUtils.doMessage(this.m_systemInterface, null, prompt, 4);
/* 1468 */     if (response != 2)
/*      */       return;
/*      */     try
/*      */     {
/* 1472 */       String id = props.getProperty("id");
/* 1473 */       int idColumn = ResultSetUtils.getIndexMustExist(this.m_listData, "id");
/* 1474 */       this.m_listData.findRow(idColumn, id);
/* 1475 */       this.m_listData.deleteCurrentRow();
/* 1476 */       String expRoot = this.m_installer.expandSourceDirectory(type);
/* 1477 */       String rootPrefix = props.getProperty("rootPrefix");
/*      */ 
/* 1479 */       if (EnvUtils.isFamily("unix"))
/*      */       {
/* 1481 */         rootPrefix = rootPrefix.toLowerCase();
/*      */       }
/* 1483 */       if (!rootPrefix.equals(expRoot))
/*      */       {
/* 1485 */         this.m_manifestData.removeLocal(type.toLowerCase() + "@" + location + ".source");
/*      */       }
/*      */ 
/* 1488 */       refreshList(null);
/*      */     }
/*      */     catch (Exception exp)
/*      */     {
/* 1492 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, IdcMessageFactory.lc("csCompWizRemoveEntryTypeError", new Object[] { type }));
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void refreshList(String selObj)
/*      */   {
/* 1500 */     this.m_list.refreshList(this.m_listData, selObj);
/*      */   }
/*      */ 
/*      */   protected void initListData()
/*      */   {
/* 1505 */     this.m_listData = new DataResultSet(this.MANIFEST_DISPLAY_FIELD_INFO);
/* 1506 */     DataResultSet drset = (DataResultSet)this.m_manifestData.getResultSet(this.m_manifestTableName);
/*      */ 
/* 1508 */     if (drset == null)
/*      */     {
/* 1510 */       drset = new DataResultSet(this.MANIFEST_FIELD_INFO);
/*      */     }
/*      */ 
/* 1513 */     this.m_idCount = 0;
/*      */ 
/* 1515 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/* 1517 */       String entryType = drset.getStringValue(0);
/* 1518 */       String location = drset.getStringValue(1);
/*      */ 
/* 1520 */       addListDataRow(entryType, location);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean onOk()
/*      */   {
/* 1526 */     Properties props = this.m_dlgHelper.m_props;
/* 1527 */     boolean retVal = true;
/*      */     try
/*      */     {
/* 1530 */       String type = props.getProperty("entryType");
/* 1531 */       String location = props.getProperty("location");
/* 1532 */       String rootPrefix = props.getProperty("rootPrefix");
/* 1533 */       String source = FileUtils.getAbsolutePath(rootPrefix, location);
/*      */ 
/* 1535 */       if ((type.equals("component")) && 
/* 1537 */         (!location.endsWith(".hda")))
/*      */       {
/* 1539 */         throw new ServiceException("!csCompWizComponentFileExtError");
/*      */       }
/*      */ 
/* 1543 */       if ((location == null) || (location.length() == 0))
/*      */       {
/* 1545 */         throw new ServiceException("!csCompWizSubDirOrFileNeeded");
/*      */       }
/*      */ 
/* 1548 */       if (EnvUtils.isFamily("windows"))
/*      */       {
/* 1550 */         rootPrefix = rootPrefix.toLowerCase();
/* 1551 */         source = source.toLowerCase();
/*      */       }
/*      */ 
/* 1555 */       boolean isDirectory = false;
/* 1556 */       int result = FileUtils.checkFile(source, true, false);
/*      */ 
/* 1559 */       if (result < 0)
/*      */       {
/* 1561 */         result = FileUtils.checkFile(source, false, false);
/* 1562 */         isDirectory = true;
/*      */       }
/*      */ 
/* 1566 */       if (result < 0)
/*      */       {
/* 1568 */         throw new ServiceException(null, "csCompWizResourceMissing", new Object[] { source });
/*      */       }
/*      */ 
/* 1571 */       if (isDirectory)
/*      */       {
/* 1573 */         location = FileUtils.directorySlashes(location);
/*      */       }
/*      */ 
/* 1576 */       if ((((type.equals("componentClasses")) || (type.equals("classes")))) && (this.m_makeJarCheckbox.isSelected()))
/*      */       {
/* 1579 */         location = makeJar(type, location, source);
/*      */       }
/*      */ 
/* 1582 */       if ((type.equals("component")) || (type.equals("componentExtra")) || (type.equals("componentClasses")) || (type.equals("componentLib")))
/*      */       {
/* 1585 */         String intradocDir = this.m_intradocDir;
/* 1586 */         String customDir = this.m_installer.getComponentDir();
/* 1587 */         boolean calcSource = true;
/*      */ 
/* 1589 */         if (EnvUtils.isFamily("windows"))
/*      */         {
/* 1591 */           intradocDir = intradocDir.toLowerCase();
/* 1592 */           customDir = customDir.toLowerCase();
/*      */         }
/*      */ 
/* 1595 */         if (source.startsWith(customDir))
/*      */         {
/* 1597 */           calcSource = false;
/*      */         }
/*      */ 
/* 1601 */         if (calcSource)
/*      */         {
/* 1603 */           this.m_manifestData.putLocal(type.toLowerCase() + "@" + location + ".source", rootPrefix);
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1608 */       this.m_dlgHelper.m_props.put("location", location);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1612 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/* 1613 */       retVal = false;
/*      */     }
/*      */ 
/* 1616 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*      */   {
/* 1622 */     return super.validateComponentValue(exchange);
/*      */   }
/*      */ 
/*      */   public void itemStateChanged(ItemEvent e)
/*      */   {
/* 1627 */     int state = e.getStateChange();
/*      */ 
/* 1629 */     String preFileName = "preference.hda";
/* 1630 */     String entryType = "componentExtra";
/* 1631 */     String tempDir = this.m_location.substring(0, this.m_location.lastIndexOf("/") + 1);
/* 1632 */     String prepath = null;
/* 1633 */     if (this.m_addSourcePath)
/*      */     {
/* 1635 */       prepath = this.m_sourcePath + "/" + preFileName;
/*      */     }
/*      */     else
/*      */     {
/* 1639 */       prepath = tempDir + "/" + preFileName;
/*      */     }
/* 1641 */     prepath = FileUtils.fileSlashes(prepath);
/*      */ 
/* 1643 */     switch (state)
/*      */     {
/*      */     case 1:
/* 1646 */       if (FileUtils.checkFile(this.m_component.m_absCompDir + preFileName, true, false) >= 0)
/*      */       {
/* 1648 */         if (this.m_addSourcePath)
/*      */         {
/* 1650 */           this.m_manifestData.putLocal("componentextra@" + prepath + ".source", this.m_root);
/*      */         }
/* 1652 */         addListDataRow(entryType, prepath);
/*      */       }
/*      */       else
/*      */       {
/* 1656 */         CWizardGuiUtils.reportError(this.m_systemInterface, null, IdcMessageFactory.lc("csCompWizMustDefinePreferenceData", new Object[0]));
/*      */ 
/* 1658 */         this.m_hasPrefCheckbox.setSelected(false);
/*      */       }
/*      */ 
/* 1661 */       break;
/*      */     case 2:
/* 1664 */       FieldInfo fi = new FieldInfo();
/* 1665 */       if ((this.m_listData.getFieldInfo("location", fi)) && 
/* 1667 */         (this.m_listData.findRow(fi.m_index, prepath) != null))
/*      */       {
/* 1669 */         this.m_listData.deleteCurrentRow();
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1674 */     refreshList(null);
/*      */   }
/*      */ 
/*      */   protected String makeJar(String type, String location, String source)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1680 */     boolean isCompClasses = type.equals("componentClasses");
/* 1681 */     ZipOutputStream zos = null;
/* 1682 */     ByteArrayInputStream bais = null;
/* 1683 */     boolean incJava = this.m_incJavaSourceCheckbox.isSelected();
/* 1684 */     String jarFileName = this.m_component.m_name + ".jar";
/* 1685 */     String relJarPath = null;
/* 1686 */     String jarFile = null;
/*      */ 
/* 1689 */     if (isCompClasses)
/*      */     {
/* 1691 */       String tmpStr = this.m_component.m_name + "/classes";
/* 1692 */       jarFile = FileUtils.directorySlashes(this.m_component.m_absCompDir) + "classes/" + jarFileName;
/* 1693 */       relJarPath = this.m_component.m_name + "/classes/" + jarFileName;
/* 1694 */       int index = location.indexOf(tmpStr);
/* 1695 */       if (index >= 0)
/*      */       {
/* 1697 */         location = location.substring(tmpStr.length());
/* 1698 */         if (location.startsWith("/"))
/*      */         {
/* 1700 */           if (location.length() > 1)
/*      */           {
/* 1702 */             location = location.substring(1);
/*      */           }
/*      */           else
/*      */           {
/* 1706 */             location = "";
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1713 */       jarFile = FileUtils.directorySlashes(this.m_intradocDir) + "classes/" + jarFileName;
/* 1714 */       relJarPath = jarFileName;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1719 */       FileUtils.checkOrCreateDirectory(FileUtils.getDirectory(jarFile), 5);
/* 1720 */       zos = new ZipOutputStream(new FileOutputStream(jarFile));
/*      */ 
/* 1722 */       addFileToJar(location, source, zos, incJava);
/*      */     }
/*      */     catch (IOException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1731 */       FileUtils.closeObjects(zos, bais);
/*      */     }
/* 1733 */     return relJarPath;
/*      */   }
/*      */ 
/*      */   protected void addFileToJar(String location, String source, ZipOutputStream zos, boolean incJava)
/*      */     throws ServiceException
/*      */   {
/*      */     try
/*      */     {
/* 1741 */       if (FileUtils.checkFile(source, true, false) == 0)
/*      */       {
/* 1744 */         source = FileUtils.directorySlashesEx(source, false);
/* 1745 */         location = FileUtils.directorySlashesEx(location, false);
/* 1746 */         if ((source.endsWith(".class")) || ((source.endsWith(".java")) && (incJava)))
/*      */         {
/* 1748 */           ZipFunctions.addFile(location, source, zos);
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/* 1754 */         source = FileUtils.directorySlashes(source);
/* 1755 */         location = FileUtils.directorySlashes(location);
/* 1756 */         File directory = new File(source);
/* 1757 */         String[] files = directory.list();
/* 1758 */         for (int i = 0; i < files.length; ++i)
/*      */         {
/* 1760 */           addFileToJar(location + files[i], source + files[i], zos, incJava);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1766 */       throw new ServiceException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void addAdvButton(JPanel pnl, boolean isView)
/*      */   {
/* 1772 */     GridBagHelper gbh = this.m_helper.m_gridHelper;
/* 1773 */     String label = "csCompWizConfigAdvSettings";
/* 1774 */     if (isView)
/*      */     {
/* 1776 */       label = "csCompWizViewAdvSettings";
/*      */     }
/*      */ 
/* 1779 */     int labelStyle = 1;
/* 1780 */     GridBagConstraints oldgc = (GridBagConstraints)gbh.m_gc.clone();
/* 1781 */     GridBagConstraints gc = gbh.m_gc;
/* 1782 */     gbh.prepareAddRowElement(13);
/* 1783 */     gc.weightx = 0.0D;
/* 1784 */     gc.fill = 0;
/*      */ 
/* 1786 */     CustomLabel labelComponent = new CustomLabel(LocaleResources.getString(label, null), labelStyle);
/* 1787 */     this.m_helper.addComponent(pnl, labelComponent);
/* 1788 */     gc.weightx = 1.0D;
/*      */ 
/* 1790 */     JButton advButton = new JButton(LocaleResources.getString("csCompWizAdvancedButton", null));
/* 1791 */     gbh.prepareAddLastRowElement(17);
/* 1792 */     this.m_helper.addComponent(pnl, advButton);
/*      */ 
/* 1794 */     gbh.m_gc = oldgc;
/*      */ 
/* 1796 */     ActionListener advListener = new ActionListener(isView)
/*      */     {
/*      */       public void actionPerformed(ActionEvent e)
/*      */       {
/* 1800 */         ComponentWizardManager manager = ((CWizardFrame)ManifestEditorDlg.this.getDialogHelper().m_parent).m_manager;
/* 1801 */         AdvancedSettingsDlg dlg = new AdvancedSettingsDlg(manager, ManifestEditorDlg.this.m_systemInterface);
/* 1802 */         boolean hasPref = ManifestEditorDlg.this.m_hasPrefCheckbox.isSelected();
/* 1803 */         dlg.showAdvanceBuildSettings(ManifestEditorDlg.this.m_helper.m_props, this.val$isView, hasPref);
/*      */       }
/*      */     };
/* 1806 */     advButton.addActionListener(advListener);
/*      */ 
/* 1808 */     if ((isView) && (!isSpecified("installID")) && (!isSpecified("classpath")) && (!isSpecified("libpath")) && (!isSpecified("featureExtensions")) && (!isSpecified("requiredFeatures")) && (!isSpecified("additionalComponents")) && (!isSpecified("componentsToDisable")) && (!isSpecified("preventAdditionalComponentDowngrade")))
/*      */     {
/* 1814 */       advButton.setEnabled(false);
/*      */     }
/*      */     else
/*      */     {
/* 1818 */       advButton.setEnabled(true);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected boolean isSpecified(String name)
/*      */   {
/* 1824 */     boolean retVal = false;
/* 1825 */     String val = this.m_helper.m_props.getProperty(name);
/* 1826 */     if ((val != null) && (val.length() > 0))
/*      */     {
/* 1828 */       retVal = true;
/*      */     }
/*      */ 
/* 1831 */     return retVal;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1836 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94535 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.ManifestEditorDlg
 * JD-Core Version:    0.5.4
 */