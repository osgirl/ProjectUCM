/*     */ package intradoc.apputilities.systemproperties;
/*     */ 
/*     */ import intradoc.common.CommonLocalizationHandler;
/*     */ import intradoc.common.CommonLocalizationHandlerFactory;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcLocale;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.resource.ResourceLoader;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.server.IdcSystemConfig;
/*     */ import intradoc.server.utils.IdcUtilityLoader;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.GridBagLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.text.ParseException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class LocalizationPanel extends SystemPropertiesPanel
/*     */   implements ActionListener, ItemListener, DisplayStringCallback, ResultSetFilter
/*     */ {
/*     */   protected String m_languageDir;
/*     */   protected UdlPanel m_list;
/*     */   protected String[] m_booleanLabels;
/*     */   protected String[] m_btnLabels;
/*     */   protected JButton[] m_btns;
/*     */   protected DataBinder m_systemLocaleConfig;
/*     */   protected DataBinder m_userLocaleConfig;
/*     */   protected DataResultSet m_mergedConfig;
/*     */   protected boolean m_configNeedsSave;
/*     */   protected CommonLocalizationHandler m_commonLocalizationHandler;
/*     */ 
/*     */   public LocalizationPanel()
/*     */   {
/*  83 */     this.m_btnLabels = new String[] { "csButtonAddLocale", "csButtonEditLocale", "csButtonDeleteLocale", "csButtonEnableLocale", "csButtonDisableLocale" };
/*     */ 
/*  88 */     this.m_btns = new JButton[this.m_btnLabels.length];
/*     */ 
/*  93 */     this.m_configNeedsSave = false;
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/*     */     try
/*     */     {
/* 102 */       this.m_languageDir = (DirectoryLocator.getResourcesDirectory() + "core/lang");
/* 103 */       this.m_booleanLabels = new String[] { LocaleResources.getString("apFalse", null), LocaleResources.getString("apTrue", null) };
/*     */ 
/* 107 */       loadLocaleConfigInfo();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 111 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadLocalization", e.getMessage());
/* 112 */       msg = LocaleResources.localizeMessage(msg, null);
/* 113 */       CustomLabel label = new CustomLabel(msg);
/* 114 */       add("Center", label);
/* 115 */       return;
/*     */     }
/*     */ 
/* 118 */     this.m_commonLocalizationHandler = CommonLocalizationHandlerFactory.createInstance();
/* 119 */     LocaleResources.localizeArray(this.m_btnLabels, null);
/*     */ 
/* 122 */     JPanel panel = new CustomPanel();
/* 123 */     this.m_helper.makePanelGridBag(panel, 1);
/* 124 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/* 127 */     this.m_helper.addPanelTitle(panel, LocaleResources.getString("csLocalizationPanelTitle", null));
/* 128 */     gridBag.m_gc.weighty = 0.0D;
/* 129 */     gridBag.prepareAddLastRowElement();
/* 130 */     this.m_helper.addComponent(panel, new CustomLabel(""));
/*     */ 
/* 132 */     JPanel subPanel = addNewSubPanel(panel, 1);
/*     */ 
/* 134 */     this.m_list = new UdlPanel(LocaleResources.getString("csTitleLocales", null), null, 660, 14, "LocaleConfig", true);
/*     */ 
/* 136 */     this.m_list.setDisplayCallback("lcLocaleId", this);
/* 137 */     this.m_list.setDisplayCallback("lcIsEnabled", this);
/* 138 */     this.m_list.setDisplayCallback("lcDateTimeFormat", this);
/* 139 */     this.m_list.setDisplayCallback("lcDisplayDateFormat", this);
/* 140 */     this.m_list.setDisplayCallback("lcAlternateParseDateFormats", this);
/* 141 */     this.m_list.setDisplayCallback("lcTimeZone", this);
/* 142 */     this.m_list.m_list.addActionListener(this);
/* 143 */     this.m_list.m_list.addItemListener(this);
/* 144 */     this.m_list.setMultipleMode(true);
/*     */ 
/* 146 */     this.m_list.init();
/* 147 */     String[][] columns = { { "lcLocaleId", "csLabelLocale" }, { "lcIsEnabled", "apLabelEnabled" }, { "lcLanguageId", "csLabelLanguageId" }, { "lcIsoEncoding", "csLabelEncoding" }, { "lcDateTimeFormat", "csLabelDateTimeFormat" }, { "lcDisplayDateFormat", "csLabelDisplayDateFormat" }, { "lcAlternateParseDateFormats", "csLabelAlternateParseDateFormats" }, { "lcTimeZone", "csLabelTimeZone" } };
/*     */ 
/* 158 */     StringBuffer buf = new StringBuffer();
/* 159 */     buf.append(columns[0][0]);
/* 160 */     for (int i = 1; i < columns.length; ++i)
/*     */     {
/* 162 */       buf.append(",");
/* 163 */       buf.append(columns[i][0]);
/*     */     }
/* 165 */     LocaleResources.localizeDoubleArray(columns, null, 1);
/* 166 */     this.m_list.setVisibleColumns(buf.toString());
/*     */ 
/* 168 */     mergeConfig();
/*     */ 
/* 170 */     for (int i = 0; i < columns.length; ++i)
/*     */     {
/* 172 */       ColumnInfo info = new ColumnInfo(columns[i][1], columns[i][0], 10.0D);
/* 173 */       this.m_list.setColumnInfo(info);
/*     */     }
/*     */ 
/* 176 */     this.m_list.setVisibleColumns(buf.toString());
/*     */ 
/* 178 */     gridBag.m_gc.weighty = 1.0D;
/* 179 */     Insets tmp = gridBag.m_gc.insets;
/* 180 */     gridBag.m_gc.insets = new Insets(10, 10, 10, 10);
/* 181 */     gridBag.prepareAddLastRowElement(18);
/* 182 */     this.m_helper.addComponent(subPanel, this.m_list);
/* 183 */     gridBag.m_gc.insets = tmp;
/*     */ 
/* 185 */     JPanel buttons = new PanePanel();
/* 186 */     buttons.setLayout(new GridBagLayout());
/* 187 */     gridBag.prepareAddRowElement();
/* 188 */     gridBag.m_gc.weightx = 1.0D;
/* 189 */     gridBag.m_gc.insets = new Insets(0, 4, 0, 4);
/* 190 */     for (int i = 0; i < this.m_btns.length; ++i)
/*     */     {
/* 192 */       this.m_btns[i] = new JButton(this.m_btnLabels[i]);
/* 193 */       this.m_btns[i].addActionListener(this);
/* 194 */       this.m_helper.addComponent(buttons, this.m_btns[i]);
/*     */     }
/*     */ 
/* 198 */     gridBag.m_gc.weightx = 0.0D;
/* 199 */     gridBag.m_gc.fill = 0;
/* 200 */     gridBag.prepareAddLastRowElement(10);
/* 201 */     this.m_helper.addComponent(subPanel, buttons);
/*     */ 
/* 203 */     gridBag.m_gc.fill = 1;
/* 204 */     setLayout(new GridBagLayout());
/* 205 */     this.m_helper.addComponent(this, subPanel);
/*     */ 
/* 207 */     itemStateChanged(null);
/*     */   }
/*     */ 
/*     */   public void loadLocaleConfigInfo() throws DataException, ServiceException
/*     */   {
/* 212 */     this.m_systemLocaleConfig = new DataBinder();
/* 213 */     this.m_userLocaleConfig = new DataBinder();
/*     */ 
/* 215 */     String resDir = DirectoryLocator.getResourcesDirectory();
/* 216 */     String dataDir = IdcUtilityLoader.getDataDir();
/*     */ 
/* 218 */     IdcSystemConfig.loadEncodingMap(resDir + "core/tables");
/* 219 */     IdcSystemConfig.loadSystemEncodingInfo();
/*     */ 
/* 221 */     ResourceContainer container = new ResourceContainer();
/* 222 */     ResourceLoader.loadResourceFileEx(container, "LocalizationPanel", resDir + "core/tables/std_locale.htm", false, null, 0L, null);
/*     */ 
/* 224 */     Iterator it = container.m_tables.keySet().iterator();
/* 225 */     while (it.hasNext())
/*     */     {
/* 227 */       String key = (String)it.next();
/* 228 */       Table tbl = (Table)container.m_tables.get(key);
/* 229 */       DataResultSet drset = new DataResultSet();
/* 230 */       drset.init(tbl);
/* 231 */       this.m_systemLocaleConfig.addResultSet(key, drset);
/*     */     }
/*     */ 
/* 234 */     ResourceUtils.serializeDataBinder(dataDir + "locale", "locale_config.hda", this.m_userLocaleConfig, false, false);
/*     */   }
/*     */ 
/*     */   public void mergeConfig()
/*     */   {
/*     */     try
/*     */     {
/* 242 */       this.m_mergedConfig = new DataResultSet();
/* 243 */       this.m_mergedConfig.copy(this.m_systemLocaleConfig.getResultSet("LocaleConfig"));
/* 244 */       Enumeration iter = this.m_userLocaleConfig.getResultSetList();
/* 245 */       while (iter.hasMoreElements())
/*     */       {
/* 247 */         String key = (String)iter.nextElement();
/* 248 */         if (key.startsWith("LocaleConfig"))
/*     */         {
/* 250 */           this.m_mergedConfig.merge("lcLocaleId", this.m_userLocaleConfig.getResultSet(key), false);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 257 */       MessageBox.reportError(this.m_sysInterface, e);
/*     */     }
/* 259 */     this.m_list.refreshListEx(this.m_mergedConfig, this.m_list.getSelectedObjs());
/*     */ 
/* 261 */     if (!this.m_sysInterface instanceof SystemPropertiesFrame)
/*     */       return;
/* 263 */     DataBinder binder = new DataBinder();
/* 264 */     DataResultSet drset = new DataResultSet();
/* 265 */     drset.copyFiltered(this.m_mergedConfig, "lcLanguageId", this);
/* 266 */     binder.addResultSet("LocaleConfig", drset);
/* 267 */     ((SystemPropertiesFrame)this.m_sysInterface).notifyLocaleObservers(binder);
/*     */   }
/*     */ 
/*     */   public int checkRow(String value, int curNumRows, Vector row)
/*     */   {
/* 274 */     FieldInfo info = new FieldInfo();
/* 275 */     this.m_mergedConfig.getFieldInfo("lcLocaleId", info);
/* 276 */     String locale = (String)row.get(info.m_index);
/* 277 */     if (locale.equals(LocaleResources.getSystemLocale().m_name))
/*     */     {
/* 281 */       Report.trace("localization", "allowing system locale " + locale, null);
/* 282 */       return 1;
/*     */     }
/*     */ 
/* 286 */     info = new FieldInfo();
/* 287 */     this.m_mergedConfig.getFieldInfo("lcIsEnabled", info);
/* 288 */     boolean isEnabled = StringUtils.convertToBool((String)row.get(info.m_index), false);
/* 289 */     if (isEnabled)
/*     */     {
/* 291 */       return 1;
/*     */     }
/*     */ 
/* 295 */     String[] languageFiles = { "sy_strings.htm", "cs_strings.htm", "ap_strings.htm", "ww_strings.htm" };
/*     */ 
/* 299 */     String resDir = this.m_languageDir;
/*     */     List dirs;
/*     */     try
/*     */     {
/* 303 */       dirs = LocaleUtils.getLanguageDirectoryList(null, resDir);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 307 */       String msg = LocaleUtils.encodeMessage("Unable to create language directory list.", e.getMessage());
/*     */ 
/* 309 */       Report.trace(null, msg, e);
/* 310 */       dirs = new ArrayList();
/* 311 */       dirs.add(resDir);
/*     */     }
/*     */ 
/* 314 */     for (int i = 0; i < dirs.size(); ++i)
/*     */     {
/* 316 */       resDir = (String)dirs.get(i);
/* 317 */       if (FileUtils.checkFile(resDir + "/" + value, false, false) != 0)
/*     */         continue;
/* 319 */       for (int j = 0; j < languageFiles.length; ++j)
/*     */       {
/* 321 */         String file = resDir + "/" + value + "/" + languageFiles[j];
/* 322 */         if (FileUtils.checkFile(file, true, false) == 0)
/*     */         {
/* 324 */           return 1;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 329 */     return 0;
/*     */   }
/*     */ 
/*     */   public void deleteCustomLocales(DataResultSet rset)
/*     */   {
/* 334 */     FieldInfo idInfo = new FieldInfo();
/* 335 */     rset.getFieldInfo("lcLocaleId", idInfo);
/*     */ 
/* 337 */     DataResultSet customSet = (DataResultSet)this.m_userLocaleConfig.getResultSet("LocaleConfig");
/*     */ 
/* 339 */     if (customSet == null)
/*     */     {
/* 341 */       return;
/*     */     }
/* 343 */     FieldInfo newIdInfo = new FieldInfo();
/* 344 */     customSet.getFieldInfo("lcLocaleId", newIdInfo);
/* 345 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 347 */       String name = rset.getStringValue(idInfo.m_index);
/* 348 */       if (customSet.findRow(newIdInfo.m_index, name) == null)
/*     */         continue;
/* 350 */       Enumeration sets = this.m_userLocaleConfig.getResultSetList();
/* 351 */       while (sets.hasMoreElements())
/*     */       {
/* 353 */         String key = (String)sets.nextElement();
/* 354 */         DataResultSet set = (DataResultSet)this.m_userLocaleConfig.getResultSet(key);
/* 355 */         FieldInfo keyField = new FieldInfo();
/* 356 */         if ((set.getFieldInfo("lcLocaleId", keyField)) && 
/* 358 */           (set.findRow(keyField.m_index, name) != null))
/*     */         {
/* 360 */           set.deleteRow(set.getCurrentRow());
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 367 */     mergeConfig();
/*     */   }
/*     */ 
/*     */   public void enableDisableLocales(DataResultSet rset, boolean enable)
/*     */   {
/* 372 */     FieldInfo idInfo = new FieldInfo();
/* 373 */     rset.getFieldInfo("lcLocaleId", idInfo);
/*     */ 
/* 376 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 378 */       String locale = rset.getStringValue(idInfo.m_index);
/* 379 */       enableDisableLocale(locale, enable);
/*     */     }
/*     */ 
/* 382 */     mergeConfig();
/*     */   }
/*     */ 
/*     */   public void enableDisableLocale(String key, boolean enable)
/*     */   {
/* 387 */     FieldInfo enabledIdInfo = new FieldInfo();
/* 388 */     DataResultSet enabledSet = (DataResultSet)this.m_userLocaleConfig.getResultSet("LocaleConfig");
/*     */ 
/* 390 */     if (enabledSet != null)
/*     */     {
/* 393 */       enabledSet.getFieldInfo("lcLocaleId", enabledIdInfo);
/* 394 */       if (enabledSet.findRow(enabledIdInfo.m_index, key) == null)
/*     */       {
/* 396 */         enabledSet = null;
/*     */       }
/*     */     }
/* 399 */     if (enabledSet == null)
/*     */     {
/* 401 */       enabledSet = (DataResultSet)this.m_userLocaleConfig.getResultSet("LocaleConfig_lcIsEnabled");
/*     */ 
/* 403 */       if (enabledSet == null)
/*     */       {
/* 405 */         enabledSet = new DataResultSet(new String[] { "lcLocaleId", "lcIsEnabled" });
/* 406 */         this.m_userLocaleConfig.addResultSet("LocaleConfig_lcIsEnabled", enabledSet);
/*     */       }
/*     */     }
/*     */ 
/* 410 */     FieldInfo enabledInfo = new FieldInfo();
/* 411 */     enabledSet.getFieldInfo("lcIsEnabled", enabledInfo);
/* 412 */     enabledSet.getFieldInfo("lcLocaleId", enabledIdInfo);
/* 413 */     Vector row = enabledSet.findRow(enabledIdInfo.m_index, key);
/* 414 */     if (row == null)
/*     */     {
/* 416 */       row = enabledSet.createEmptyRow();
/* 417 */       row.setElementAt(key, enabledIdInfo.m_index);
/* 418 */       enabledSet.addRow(row);
/* 419 */       enabledSet.last();
/*     */     }
/*     */     try
/*     */     {
/* 423 */       enabledSet.setCurrentValue(enabledInfo.m_index, (enable) ? "true" : "false");
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void saveChanges()
/*     */     throws ServiceException
/*     */   {
/* 434 */     String dataDir = IdcUtilityLoader.getDataDir();
/* 435 */     this.m_userLocaleConfig.m_blDateFormat = LocaleResources.m_iso8601Format;
/* 436 */     FileUtils.checkOrCreateDirectory(dataDir + "locale", 1);
/* 437 */     ResourceUtils.serializeDataBinder(dataDir + "locale", "locale_config.hda", this.m_userLocaleConfig, true, false);
/*     */ 
/* 439 */     if (!this.m_configNeedsSave) {
/*     */       return;
/*     */     }
/* 442 */     DataBinder binder = new DataBinder();
/* 443 */     if (!ResourceUtils.serializeDataBinder(dataDir + "publish", "startup.hda", binder, false, false))
/*     */       return;
/* 445 */     binder.putLocal("PublishStrings", "true");
/* 446 */     ResourceUtils.serializeDataBinder(dataDir + "publish", "startup.hda", binder, true, true);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/* 453 */     int[] indexes = this.m_list.getSelectedIndexes();
/* 454 */     Object source = event.getSource();
/* 455 */     int rc = -1;
/* 456 */     if (source == this.m_btns[0])
/*     */     {
/* 458 */       EditLocaleDialog dlg = new EditLocaleDialog(this.m_sysInterface, LocaleResources.getString("csTitleConfigureLocale", null));
/*     */ 
/* 460 */       dlg.init(null, this.m_systemLocaleConfig, this.m_userLocaleConfig);
/* 461 */       rc = dlg.prompt();
/* 462 */       mergeConfig();
/* 463 */       refreshSelection();
/*     */     } else {
/* 465 */       if (indexes.length == 0)
/*     */       {
/* 467 */         return;
/*     */       }
/* 469 */       if ((source == this.m_btns[1]) || (source == this.m_list.m_list))
/*     */       {
/* 471 */         EditLocaleDialog dlg = new EditLocaleDialog(this.m_sysInterface, LocaleResources.getString("csTitleConfigureLocale", null));
/*     */ 
/* 473 */         dlg.init(this.m_list.getSelectedObj(), this.m_systemLocaleConfig, this.m_userLocaleConfig);
/* 474 */         rc = dlg.prompt();
/* 475 */         mergeConfig();
/* 476 */         refreshSelection();
/*     */       }
/* 478 */       else if (source == this.m_btns[2])
/*     */       {
/* 480 */         deleteCustomLocales(this.m_list.getSelectedAsResultSet());
/* 481 */         refreshSelection();
/* 482 */         rc = 1;
/*     */       }
/* 484 */       else if (source == this.m_btns[3])
/*     */       {
/* 486 */         enableDisableLocales(this.m_list.getSelectedAsResultSet(), true);
/* 487 */         refreshSelection();
/* 488 */         rc = 1;
/*     */       }
/* 490 */       else if (source == this.m_btns[4])
/*     */       {
/* 492 */         enableDisableLocales(this.m_list.getSelectedAsResultSet(), false);
/* 493 */         refreshSelection();
/* 494 */         rc = 1;
/*     */       }
/*     */     }
/* 497 */     if (rc != 1)
/*     */       return;
/* 499 */     this.m_configNeedsSave = true;
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent event)
/*     */   {
/* 505 */     refreshSelection();
/*     */   }
/*     */ 
/*     */   public void refreshSelection()
/*     */   {
/* 510 */     int[] indexes = this.m_list.getSelectedIndexes();
/* 511 */     boolean isSingle = indexes.length == 1;
/* 512 */     boolean isNone = indexes.length == 0;
/* 513 */     boolean isEnable = false;
/* 514 */     boolean isDisable = false;
/* 515 */     boolean isCore = true;
/*     */ 
/* 517 */     this.m_btns[1].setEnabled(isSingle);
/* 518 */     this.m_btns[2].setEnabled(!isNone);
/*     */ 
/* 520 */     DataResultSet drset = this.m_list.getSelectedAsResultSet();
/* 521 */     DataResultSet userSet = (DataResultSet)this.m_userLocaleConfig.getResultSet("LocaleConfig");
/* 522 */     FieldInfo enabledInfo = new FieldInfo();
/* 523 */     FieldInfo idInfo = new FieldInfo();
/* 524 */     FieldInfo userIdInfo = new FieldInfo();
/* 525 */     drset.getFieldInfo("lcIsEnabled", enabledInfo);
/* 526 */     drset.getFieldInfo("lcLocaleId", idInfo);
/* 527 */     if (userSet != null)
/*     */     {
/* 529 */       userSet.getFieldInfo("lcLocaleId", userIdInfo);
/*     */     }
/* 531 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 533 */       String id = drset.getStringValue(idInfo.m_index);
/* 534 */       String val = drset.getStringValue(enabledInfo.m_index);
/* 535 */       boolean enabled = StringUtils.convertToBool(val, false);
/* 536 */       if (enabled)
/*     */       {
/* 538 */         isDisable = true;
/*     */       }
/*     */       else
/*     */       {
/* 542 */         isEnable = true;
/*     */       }
/*     */ 
/* 545 */       if ((userSet == null) || (userSet.findRow(userIdInfo.m_index, id) == null))
/*     */         continue;
/* 547 */       isCore = false;
/*     */     }
/*     */ 
/* 550 */     this.m_btns[2].setEnabled(!isCore);
/* 551 */     this.m_btns[3].setEnabled(isEnable);
/* 552 */     this.m_btns[4].setEnabled(isDisable);
/*     */   }
/*     */ 
/*     */   public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 558 */     String ret = value;
/* 559 */     if (name.equals("lcIsEnabled"))
/*     */     {
/* 561 */       if (StringUtils.convertToBool(value, false))
/*     */       {
/* 563 */         ret = this.m_booleanLabels[1];
/*     */       }
/*     */       else
/*     */       {
/* 567 */         ret = this.m_booleanLabels[0];
/*     */       }
/*     */     }
/* 570 */     else if (name.equals("lcTimeZone"))
/*     */     {
/* 572 */       ret = this.m_commonLocalizationHandler.getTimeZoneDisplayName(value, 2, null);
/*     */     }
/*     */ 
/* 576 */     return ret;
/*     */   }
/*     */ 
/*     */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/*     */     String[] values;
/*     */     String line1;
/*     */     String line2;
/*     */     String line3;
/* 581 */     if ((name.indexOf("DateFormat") >= 0) || (name.indexOf("TimeFormat") >= 0))
/*     */     {
/* 583 */       if ((value == null) || (value.trim().length() == 0))
/*     */       {
/* 585 */         return "";
/*     */       }
/* 587 */       values = new String[] { value };
/* 588 */       if (name.endsWith("Formats"))
/*     */       {
/* 590 */         Vector v = StringUtils.parseArray(value, '|', '^');
/* 591 */         values = StringUtils.convertListToArray(v);
/*     */       }
/* 593 */       line1 = null;
/* 594 */       line2 = null;
/* 595 */       line3 = null;
/*     */     }
/*     */     try
/*     */     {
/* 599 */       for (int i = 0; i < values.length; ++i)
/*     */       {
/* 601 */         IdcDateFormat fmt = new IdcDateFormat();
/* 602 */         fmt.init(values[i]);
/* 603 */         Date now = new Date();
/* 604 */         line1 = appendDateFormatTip(line1, fmt.format(now));
/* 605 */         line2 = appendDateFormatTip(line2, fmt.format(now, 1));
/* 606 */         line3 = appendDateFormatTip(line3, fmt.format(now, 2));
/*     */       }
/* 608 */       return line1 + "\n" + line2 + "\n" + line3;
/*     */     }
/*     */     catch (ParseException ignore)
/*     */     {
/* 612 */       if (SystemUtils.m_verbose)
/*     */       {
/* 614 */         Report.debug("systemparse", null, ignore);
/*     */       }
/*     */ 
/* 617 */       break label312:
/* 618 */       if (name.equals("lcTimeZone"))
/*     */       {
/* 620 */         if ((value == null) || (value.trim().length() == 0))
/*     */         {
/* 622 */           return "";
/*     */         }
/*     */ 
/* 625 */         return this.m_commonLocalizationHandler.getTimeZoneDisplayName(value.trim(), 3, null);
/*     */       }
/*     */ 
/* 628 */       if (name.equals("lcLocaleId"))
/*     */       {
/* 630 */         String rc = LocaleResources.getStringInternal("syLocaleName_" + value, null);
/* 631 */         return rc;
/*     */       }
/*     */     }
/* 633 */     label312: return null;
/*     */   }
/*     */ 
/*     */   protected String appendDateFormatTip(String line, String tip)
/*     */   {
/* 638 */     if (line == null)
/*     */     {
/* 640 */       line = tip;
/*     */     }
/*     */     else
/*     */     {
/* 644 */       line = line + " | " + tip;
/*     */     }
/* 646 */     return line;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 651 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.LocalizationPanel
 * JD-Core Version:    0.5.4
 */