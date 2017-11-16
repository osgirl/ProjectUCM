/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicDataParser;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.StatusBar;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.utils.SystemPropertiesEditor;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.ResourceDataParser;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Observer;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SystemPropertiesFrame extends JFrame
/*     */   implements SystemInterface
/*     */ {
/*     */   public String m_appName;
/*     */   public StatusBar m_statusBar;
/*  76 */   public AppFrameHelper m_appHelper = null;
/*     */   public ExecutionContext m_cxt;
/*     */   protected TabPanel m_tabPanel;
/*     */   protected Properties m_cfgProperties;
/*     */   protected Properties m_idcProperties;
/*     */   protected String m_idcFile;
/*     */   protected Properties m_allCfgEntries;
/*  84 */   protected final String[] m_configFiles = { "intradoc.cfg", "config.cfg" };
/*  85 */   protected SystemPropertiesPanel[] m_panels = null;
/*  86 */   protected Hashtable m_localeObservers = new Hashtable();
/*     */   protected boolean m_isSimpleForm;
/*  88 */   protected boolean m_restrictAllowablePanels = false;
/*  89 */   protected HashMap m_allowablePanels = new HashMap();
/*     */ 
/*  92 */   protected SystemPropertiesEditor m_loader = null;
/*     */ 
/*  95 */   protected String[][] m_panelInfo = { { "OptionsPanel", "intradoc.apputilities.systemproperties.OptionsPanel", "csSysPropsFrameTabNameOptions", "config.cfg" }, { "DocSecurityPanel", "intradoc.apputilities.systemproperties.DocSecurityPanel", "csSysPropsFrameTabNameContentSec", "config.cfg" }, { "InternetPanel", "intradoc.apputilities.systemproperties.InternetPanel", "csSysPropsFrameTabNameInternet", "config.cfg" }, { "DBPanel", "intradoc.apputilities.systemproperties.DBPanel", "csSysPropsFrameTabNameDB", "config.cfg" }, { "ServerPanel", "intradoc.apputilities.systemproperties.ServerPanel", "csSysPropsFrameTabNameServer", "config.cfg" }, { "LocalizationPanel", "intradoc.apputilities.systemproperties.LocalizationPanel", "csSysPropsFrameTabNameLocalization", "config.cfg" }, { "PathsPanel", "intradoc.apputilities.systemproperties.PathsPanel", "csSysPropsFrameTabNamePaths", "intradoc.cfg" }, { "IBRPrintPanel", "intradoc.apputilities.systemproperties.ibr.PrinterPanel", "csSysPropsPrinterTabName", "intradoc.cfg" } };
/*     */ 
/* 113 */   protected Vector m_additionalPanelForSimpleForm = null;
/*     */ 
/* 115 */   protected final String[][] OBSOLETE_CONFIG_MAP = { { "isOverrideFormat", "IsOverrideFormat" }, { "isJdbc", "IsJdbc" } };
/*     */ 
/*     */   public SystemPropertiesFrame()
/*     */   {
/* 126 */     this.m_appHelper = new AppFrameHelper();
/* 127 */     this.m_cxt = new ExecutionContextAdaptor();
/*     */ 
/* 129 */     this.m_cfgProperties = new Properties();
/* 130 */     this.m_idcProperties = new Properties();
/*     */ 
/* 133 */     this.m_idcFile = SystemUtils.getCfgFilePath();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean closeOnExit, boolean isRefinery)
/*     */     throws ServiceException
/*     */   {
/* 143 */     initEx(IdcMessageFactory.lc(title, new Object[0]), closeOnExit, isRefinery, null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void initEx(String title, boolean closeOnExit, boolean isSimpleForm, String additionalTab)
/*     */     throws ServiceException
/*     */   {
/* 151 */     initEx(IdcMessageFactory.lc(title, new Object[0]), closeOnExit, isSimpleForm, additionalTab);
/*     */   }
/*     */ 
/*     */   public void initEx(IdcMessage title, boolean closeOnExit, boolean isSimpleForm, String additionalTab)
/*     */     throws ServiceException
/*     */   {
/* 158 */     loadAllCfgs();
/*     */ 
/* 161 */     loadPasswordInfo();
/*     */ 
/* 163 */     String restrictedPanels = SharedObjects.getEnvironmentValue("SystemPropertiesPanels");
/* 164 */     if ((restrictedPanels != null) && (restrictedPanels.trim().length() > 0))
/*     */     {
/* 166 */       this.m_restrictAllowablePanels = true;
/* 167 */       List l = StringUtils.makeListFromSequence(restrictedPanels, ',', ',', 32);
/*     */ 
/* 169 */       for (int i = 0; i < l.size(); ++i)
/*     */       {
/* 171 */         this.m_allowablePanels.put(l.get(i), "1");
/*     */       }
/*     */     }
/*     */ 
/* 175 */     IntervalData interval = new IntervalData("systemload");
/* 176 */     this.m_isSimpleForm = isSimpleForm;
/* 177 */     this.m_additionalPanelForSimpleForm = StringUtils.parseArray(additionalTab, ',', ',');
/* 178 */     initPanelList();
/* 179 */     interval.traceAndRestart("startup", "initPanelList()");
/* 180 */     String idcName = this.m_cfgProperties.getProperty("IDC_Name");
/*     */     IdcMessage titleMsg;
/*     */     IdcMessage titleMsg;
/* 182 */     if ((idcName != null) && (idcName.length() > 0))
/*     */     {
/* 184 */       titleMsg = IdcMessageFactory.lc("sySysSpecifier", new Object[] { title, idcName });
/*     */     }
/*     */     else
/*     */     {
/* 188 */       titleMsg = title;
/*     */     }
/* 190 */     setTitle(LocaleResources.localizeMessage(null, titleMsg, null).toString());
/*     */ 
/* 192 */     this.m_appHelper.m_exitOnClose = closeOnExit;
/*     */ 
/* 194 */     this.m_appHelper.attachToAppFrame(this, null, null, titleMsg);
/*     */ 
/* 197 */     setBounds(100, 100, 50, 50);
/*     */ 
/* 200 */     this.m_tabPanel = new TabPanel();
/* 201 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 202 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/* 204 */     this.m_tabPanel = initInfoPanel();
/* 205 */     interval.traceAndRestart("startup", "initInfoPanel()");
/*     */ 
/* 208 */     mainPanel.add("Center", this.m_tabPanel);
/*     */ 
/* 210 */     JPanel btnPanel = new PanePanel();
/* 211 */     btnPanel.setLayout(new FlowLayout());
/*     */ 
/* 213 */     JButton okBtn = new JButton(LocaleResources.getString("csSysPropsFrameOK", this.m_cxt));
/* 214 */     ActionListener okListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 218 */         SystemPropertiesFrame.this.onOK();
/*     */       }
/*     */     };
/* 222 */     okBtn.addActionListener(okListener);
/* 223 */     btnPanel.add(okBtn);
/*     */ 
/* 225 */     JButton cancelBtn = new JButton(LocaleResources.getString("csSysPropsFrameCancel", this.m_cxt));
/* 226 */     ActionListener cancelListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 230 */         System.exit(0);
/*     */       }
/*     */     };
/* 233 */     cancelBtn.addActionListener(cancelListener);
/* 234 */     btnPanel.add(cancelBtn);
/*     */ 
/* 236 */     mainPanel.add("South", btnPanel);
/* 237 */     pack();
/* 238 */     setVisible(true);
/* 239 */     interval.traceAndRestart("startup", "packAndSetVisibleTrue()");
/*     */   }
/*     */ 
/*     */   protected void initPanelList()
/*     */   {
/* 244 */     String[] panelList = { "OptionsPanel", "DocSecurityPanel", "InternetPanel", "DBPanel", "ServerPanel", "LocalizationPanel", "PathsPanel" };
/*     */ 
/* 250 */     if (this.m_isSimpleForm == true)
/*     */     {
/* 252 */       panelList = new String[] { "InternetPanel", "ServerPanel", "LocalizationPanel", "PathsPanel", "IBRPrintPanel" };
/*     */ 
/* 257 */       String[] printKeys = { "PrinterInfPath", "PrinterPortPath", "PostscriptPrinterName", "PrintDriverName" };
/*     */ 
/* 261 */       String addKeys = "";
/* 262 */       Properties idcProps = this.m_loader.getIdc();
/* 263 */       for (int k = 0; k < printKeys.length; ++k)
/*     */       {
/* 265 */         String key = printKeys[k];
/* 266 */         String val = idcProps.getProperty(key);
/* 267 */         if ((val != null) && (val.length() != 0))
/*     */           continue;
/* 269 */         addKeys = addKeys + key + ",";
/*     */       }
/*     */ 
/* 272 */       if (addKeys.length() > 0)
/*     */       {
/* 274 */         addKeys = addKeys.substring(0, addKeys.length() - 1);
/* 275 */         this.m_loader.addKeys(addKeys, null);
/*     */       }
/*     */     }
/* 278 */     for (int i = 0; i < panelList.length; ++i)
/*     */     {
/* 280 */       if (this.m_additionalPanelForSimpleForm.contains(panelList[i]))
/*     */         continue;
/* 282 */       this.m_additionalPanelForSimpleForm.add(panelList[i]);
/*     */     }
/*     */ 
/* 285 */     String[][] panelInfo = new String[this.m_additionalPanelForSimpleForm.size()][];
/* 286 */     int index = 0;
/* 287 */     for (int i = 0; i < this.m_panelInfo.length; ++i)
/*     */     {
/* 289 */       if (!this.m_additionalPanelForSimpleForm.contains(this.m_panelInfo[i][0]))
/*     */         continue;
/* 291 */       panelInfo[(index++)] = this.m_panelInfo[i];
/*     */     }
/*     */ 
/* 294 */     this.m_panelInfo = panelInfo;
/*     */   }
/*     */ 
/*     */   public boolean onOK()
/*     */   {
/* 302 */     if (!this.m_tabPanel.validateAllPanes())
/*     */     {
/* 304 */       return false;
/*     */     }
/*     */ 
/* 307 */     boolean result = false;
/* 308 */     Properties oldIdcProps = (Properties)this.m_idcProperties.clone();
/* 309 */     Properties oldCfgProps = (Properties)this.m_cfgProperties.clone();
/*     */     try
/*     */     {
/* 312 */       this.m_loader.replacePropertyValues(this.m_idcProperties, this.m_cfgProperties);
/* 313 */       for (int i = 0; i < this.m_panels.length; ++i)
/*     */       {
/* 316 */         if (this.m_panels[i] == null)
/*     */           continue;
/* 318 */         this.m_panels[i].saveChanges();
/*     */       }
/*     */ 
/* 322 */       Map args = new HashMap();
/* 323 */       args.put("PasswordScope", "system");
/* 324 */       CryptoPasswordUtils.extractAndUpdatePasswords(this.m_idcProperties, this.m_loader.getIdcFile(), args);
/*     */ 
/* 326 */       CryptoPasswordUtils.extractAndUpdatePasswords(this.m_cfgProperties, this.m_loader.getCfgFile(), args);
/*     */ 
/* 329 */       this.m_loader.saveIdc();
/* 330 */       this.m_loader.saveConfig();
/* 331 */       result = true;
/* 332 */       System.exit(0);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 336 */       reportError(e, IdcMessageFactory.lc("csUnabletoSaveChanges", new Object[0]));
/*     */     }
/*     */     finally
/*     */     {
/* 340 */       if (!result)
/*     */       {
/* 342 */         this.m_idcProperties = oldIdcProps;
/* 343 */         this.m_cfgProperties = oldCfgProps;
/*     */       }
/*     */     }
/* 346 */     return result;
/*     */   }
/*     */ 
/*     */   public void reportError(Throwable cause, IdcMessage msg)
/*     */   {
/* 354 */     if (cause != null)
/*     */     {
/* 356 */       msg.setPrior(IdcMessageFactory.lc(cause));
/* 357 */       Report.trace(null, cause, msg);
/*     */     }
/* 359 */     MessageBox.reportError(this, this, msg, IdcMessageFactory.lc("csSysPropsFrameErrorMsgDesc", new Object[0]));
/*     */   }
/*     */ 
/*     */   public void reportFatal(Throwable cause, IdcMessage msg)
/*     */     throws ServiceException
/*     */   {
/* 369 */     reportError(cause, msg);
/* 370 */     throw new ServiceException(cause, msg);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportError(String msg, Throwable t)
/*     */   {
/* 380 */     Report.deprecatedUsage("obsolete reportError() called.");
/* 381 */     IdcMessage errMsg = IdcMessageFactory.lc(t, msg, new Object[0]);
/* 382 */     MessageBox.reportError(this, this, errMsg, IdcMessageFactory.lc("csSysPropsFrameErrorMsgDesc", new Object[0]));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void reportFatal(String msg, Throwable t)
/*     */   {
/* 393 */     Report.deprecatedUsage("obsolete reportFatal() called.");
/* 394 */     reportError(msg, t);
/* 395 */     System.exit(0);
/*     */   }
/*     */ 
/*     */   protected TabPanel initInfoPanel()
/*     */     throws ServiceException
/*     */   {
/* 403 */     TabPanel tab = new TabPanel();
/*     */ 
/* 405 */     int numPanels = this.m_panelInfo.length;
/*     */ 
/* 407 */     this.m_panels = new SystemPropertiesPanel[numPanels];
/*     */ 
/* 409 */     for (int i = 0; i < numPanels; ++i)
/*     */     {
/* 411 */       if (!checkAllowLoadPanel(this.m_panelInfo[i][0]))
/*     */         continue;
/* 413 */       this.m_panels[i] = ((SystemPropertiesPanel)ComponentClassFactory.createClassInstance(this.m_panelInfo[i][0], this.m_panelInfo[i][1], LocaleUtils.encodeMessage("csSysPropsFrameTabLoadError", null, this.m_panelInfo[i][0])));
/*     */ 
/* 418 */       String opt = this.m_panelInfo[i][3];
/* 419 */       opt.trim();
/*     */ 
/* 421 */       if (opt.equals(this.m_configFiles[0]))
/*     */       {
/* 423 */         this.m_panels[i].init(this.m_idcProperties, this, this.m_isSimpleForm);
/*     */       }
/*     */       else
/*     */       {
/* 427 */         this.m_panels[i].init(this.m_cfgProperties, this, this.m_isSimpleForm);
/*     */       }
/*     */ 
/* 430 */       tab.addPane(LocaleResources.getString(this.m_panelInfo[i][2], this.m_cxt), this.m_panels[i], this.m_panels[i]);
/*     */     }
/*     */ 
/* 434 */     return tab;
/*     */   }
/*     */ 
/*     */   protected boolean checkAllowLoadPanel(String panelId)
/*     */   {
/* 442 */     return (!this.m_restrictAllowablePanels) || (this.m_allowablePanels.get(panelId) != null);
/*     */   }
/*     */ 
/*     */   protected void loadAllCfgs()
/*     */     throws ServiceException
/*     */   {
/* 454 */     this.m_loader = new SystemPropertiesEditor(this.m_idcFile);
/*     */     try
/*     */     {
/* 457 */       this.m_loader.initIdc();
/* 458 */       this.m_idcProperties = this.m_loader.getIdc();
/* 459 */       this.m_loader.initConfig();
/* 460 */       this.m_cfgProperties = this.m_loader.getConfig();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 464 */       reportFatal(e, IdcMessageFactory.lc("csSysPropsConfigLoadError", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadPasswordInfo()
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 475 */       LegacyDirectoryLocator.buildRootDirectories();
/* 476 */       String resDir = LegacyDirectoryLocator.getResourcesDirectory();
/*     */ 
/* 479 */       ResourceContainer container = new ResourceContainer();
/* 480 */       DynamicDataParser dataParser = new ResourceDataParser();
/* 481 */       DynamicData.addParser(dataParser);
/* 482 */       ResourceLoader.loadResourceFileEx(container, "LocalizationPanel", resDir + "core/tables/std_resources.htm", false, null, 0L, null);
/*     */ 
/* 485 */       Table table = container.getTable("SecurityCategories");
/* 486 */       DataResultSet drset = new DataResultSet();
/* 487 */       drset.init(table);
/*     */ 
/* 489 */       CryptoPasswordUtils.loadPasswordManagement(drset);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 493 */       reportFatal(e, IdcMessageFactory.lc("csSysPropsUnableToLoadPasswords", new Object[0]));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addLocaleObserver(Observer obs)
/*     */   {
/* 502 */     this.m_localeObservers.put(obs, obs);
/*     */   }
/*     */ 
/*     */   public void notifyLocaleObservers(DataBinder binder)
/*     */   {
/* 507 */     Enumeration en = this.m_localeObservers.keys();
/* 508 */     while (en.hasMoreElements())
/*     */     {
/* 510 */       Observer obs = (Observer)en.nextElement();
/* 511 */       obs.update(null, binder);
/*     */     }
/*     */   }
/*     */ 
/*     */   public JFrame getMainWindow()
/*     */   {
/* 522 */     return this;
/*     */   }
/*     */ 
/*     */   public void displayStatus(String str)
/*     */   {
/* 527 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public void displayStatus(IdcMessage msg)
/*     */   {
/* 532 */     String str = localizeMessage(msg);
/* 533 */     this.m_statusBar.setText(str);
/*     */   }
/*     */ 
/*     */   public String getAppName()
/*     */   {
/* 538 */     return this.m_appName;
/*     */   }
/*     */ 
/*     */   public ExecutionContext getExecutionContext()
/*     */   {
/* 543 */     return this.m_cxt;
/*     */   }
/*     */ 
/*     */   public String localizeMessage(String msg)
/*     */   {
/* 548 */     return LocaleResources.localizeMessage(msg, this.m_cxt);
/*     */   }
/*     */ 
/*     */   public String localizeMessage(IdcMessage msg)
/*     */   {
/* 553 */     return LocaleResources.localizeMessage(null, msg, this.m_cxt).toString();
/*     */   }
/*     */ 
/*     */   public String localizeCaption(String msg)
/*     */   {
/* 558 */     msg = LocaleUtils.encodeMessage("syCaptionWrapper", null, msg);
/*     */ 
/* 560 */     msg = LocaleResources.localizeMessage(msg, this.m_cxt);
/* 561 */     return msg;
/*     */   }
/*     */ 
/*     */   public String getString(String str)
/*     */   {
/* 566 */     return LocaleResources.getString(str, this.m_cxt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String getValidationErrorMessage(String fieldName, String fieldValue)
/*     */   {
/* 574 */     String keyName = "apFieldValidationError_" + fieldName;
/* 575 */     String messageText = LocaleResources.getStringInternal(keyName, this.m_cxt);
/*     */ 
/* 577 */     if (messageText != null)
/*     */     {
/* 579 */       return LocaleUtils.encodeMessage(keyName, null, fieldName, fieldValue);
/*     */     }
/*     */ 
/* 582 */     return null;
/*     */   }
/*     */ 
/*     */   public IdcMessage getValidationErrorMessageObject(String fieldName, String fieldValue, Map options)
/*     */   {
/* 588 */     String keyName = "apFieldValidationError_" + fieldName;
/* 589 */     String messageText = LocaleResources.getStringInternal(keyName, this.m_cxt);
/*     */ 
/* 591 */     if (messageText != null)
/*     */     {
/* 593 */       IdcMessage idcmsg = IdcMessageFactory.lc(keyName, new Object[] { fieldName, fieldValue });
/* 594 */       idcmsg.m_msgLocalized = messageText;
/*     */     }
/* 596 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 601 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80355 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.SystemPropertiesFrame
 * JD-Core Version:    0.5.4
 */