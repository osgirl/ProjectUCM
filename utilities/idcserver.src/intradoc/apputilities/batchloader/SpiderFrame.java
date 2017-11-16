/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.Sort;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.ProgressBar;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.server.ComponentLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class SpiderFrame extends MainFrame
/*     */   implements ReportProgress, PromptHandler, ComponentBinder, IdcComparator
/*     */ {
/*     */   protected SpiderApp m_spiderApp;
/*     */   protected JCheckBox m_externalCheckBox;
/*     */   protected ProgressBar m_progressBar;
/*     */   protected CustomLabel m_progressText;
/*     */   protected JButton m_buildBtn;
/*     */   protected JPanel m_mapPanel;
/*     */   protected JPanel m_collectionPanel;
/*     */   protected CustomChoice m_collectionChoice;
/*     */   protected JButton m_editCollectionBtn;
/*     */   protected CustomChoice m_usageChoice;
/*     */   protected CustomTextField m_usageFieldsText;
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  88 */     IdcMessage msg = null;
/*  89 */     if (title != null)
/*     */     {
/*  91 */       msg = IdcMessageFactory.lc();
/*  92 */       msg.m_msgEncoded = title;
/*     */     }
/*  94 */     init(title, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/* 100 */     this.m_spiderApp = new SpiderApp(null, false);
/* 101 */     this.m_spiderApp.init();
/* 102 */     this.m_spiderApp.m_frame = this;
/*     */ 
/* 104 */     super.init(title, exitOnClose);
/* 105 */     this.m_spiderApp.m_cxt = this.m_cxt;
/*     */ 
/* 107 */     this.m_appHelper.attachToAppFrame(this, this.m_spiderApp.m_idcProperties, null, title);
/* 108 */     this.m_appHelper.m_isCloseAllowedCallback = this;
/* 109 */     this.m_appHelper.m_componentBinder = this;
/*     */ 
/* 112 */     buildMenu();
/* 113 */     initUI();
/*     */ 
/* 115 */     reloadMaps();
/*     */ 
/* 117 */     if (SharedObjects.getEnvValueAsBoolean("BatchBuilderEnableExternalCollections", false))
/*     */     {
/* 119 */       reloadCollections();
/* 120 */       loadUsages();
/* 121 */       enableExternalFields(this.m_spiderApp.m_doExternal);
/*     */     }
/*     */ 
/* 124 */     this.m_appHelper.loadComponentValues();
/*     */ 
/* 126 */     pack();
/* 127 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected void buildMenu()
/*     */   {
/* 135 */     JMenuBar mb = new JMenuBar();
/* 136 */     setJMenuBar(mb);
/* 137 */     JMenu optMenu = new JMenu(LocaleResources.getString("csBatchLoaderOptMenu", this.m_cxt));
/*     */ 
/* 139 */     JMenuItem saveMI = new JMenuItem(LocaleResources.getString("csBatchLoaderSaveConfigItem", this.m_cxt));
/* 140 */     ActionListener saveListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 146 */           if (SpiderFrame.this.m_appHelper.retrieveComponentValues())
/*     */           {
/* 148 */             SpiderFrame.this.m_spiderApp.writeIntradocCfgFile();
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 153 */           SpiderFrame.this.reportError(e);
/*     */         }
/*     */       }
/*     */     };
/* 158 */     saveMI.addActionListener(saveListener);
/* 159 */     optMenu.add(saveMI);
/*     */ 
/* 161 */     JMenuItem batchMI = new JMenuItem(LocaleResources.getString("csSpiderMenuBatchLoaderItem", this.m_cxt));
/* 162 */     ActionListener batchListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 168 */           AppLauncher.launch("BatchLoader");
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 172 */           SpiderFrame.this.reportError(e);
/*     */         }
/*     */       }
/*     */     };
/* 177 */     batchMI.addActionListener(batchListener);
/* 178 */     optMenu.add(batchMI);
/*     */ 
/* 180 */     JMenuItem exitMI = new JMenuItem(LocaleResources.getString("csBatchLoaderExitItem", this.m_cxt));
/* 181 */     ActionListener exitListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/* 185 */         int retVal = SpiderFrame.this.prompt();
/* 186 */         if ((retVal != 1) && (retVal != 2))
/*     */           return;
/* 188 */         SpiderFrame.this.dispose();
/*     */       }
/*     */     };
/* 192 */     exitMI.addActionListener(exitListener);
/* 193 */     optMenu.add(exitMI);
/*     */ 
/* 195 */     mb.add(optMenu);
/* 196 */     addHelpMenu(mb);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 201 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 202 */     this.m_appHelper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 204 */     addConfigurationPanel(mainPanel);
/* 205 */     addProgressBarPanel(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void addConfigurationPanel(JPanel mainPanel)
/*     */   {
/* 211 */     JPanel configPanel = addNewSubPanel(mainPanel, 1);
/* 212 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 215 */     String directoryLabel = LocaleResources.getString("csSpiderDirectoryLabel", this.m_cxt);
/* 216 */     gridBag.prepareAddRowElement(17);
/* 217 */     gridBag.m_gc.fill = 1;
/* 218 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(directoryLabel, 1));
/*     */ 
/* 220 */     gridBag.prepareAddLastRowElement(17);
/* 221 */     gridBag.m_gc.fill = 0;
/* 222 */     this.m_appHelper.addExchangeComponent(configPanel, new CustomTextField(50), "SpiderDirectory");
/*     */ 
/* 226 */     String fileLabel = LocaleResources.getString("csBatchLoaderDesc", this.m_cxt);
/* 227 */     gridBag.prepareAddRowElement(17);
/* 228 */     gridBag.m_gc.fill = 1;
/* 229 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(fileLabel, 1));
/*     */ 
/* 231 */     this.m_appHelper.addFilePathComponent(configPanel, 50, LocaleResources.getString("csBatchLoaderDesc", this.m_cxt), "SpiderBatchFile");
/*     */ 
/* 233 */     gridBag.addEmptyRow(configPanel);
/*     */ 
/* 236 */     String mappingLabel = LocaleResources.getString("csSpiderMappingLabel", this.m_cxt);
/* 237 */     gridBag.prepareAddRowElement(17);
/* 238 */     gridBag.m_gc.fill = 1;
/* 239 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(mappingLabel, 1));
/*     */ 
/* 242 */     this.m_mapPanel = new PanePanel();
/* 243 */     this.m_appHelper.makePanelGridBag(this.m_mapPanel, 1);
/* 244 */     gridBag.m_gc.fill = 0;
/* 245 */     gridBag.prepareAddLastRowElement(17);
/* 246 */     this.m_appHelper.addComponent(configPanel, this.m_mapPanel);
/*     */ 
/* 248 */     CustomChoice mapChoice = new CustomChoice();
/* 249 */     mapChoice.setMinWidth(150);
/* 250 */     gridBag.m_gc.fill = 0;
/* 251 */     gridBag.prepareAddRowElement(17);
/* 252 */     this.m_appHelper.addExchangeComponent(this.m_mapPanel, mapChoice, "SpiderMapping");
/*     */ 
/* 254 */     gridBag.m_gc.fill = 0;
/* 255 */     gridBag.prepareAddRowElement(17);
/* 256 */     JButton editMapBtn = new JButton(LocaleResources.getString("csButtonEditLocale", this.m_cxt));
/* 257 */     this.m_appHelper.addComponent(this.m_mapPanel, editMapBtn);
/*     */ 
/* 259 */     ActionListener mapListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 265 */           SpiderMapListDialog dlg = new SpiderMapListDialog(SpiderFrame.this.m_appHelper);
/* 266 */           dlg.init();
/* 267 */           dlg.prompt();
/*     */ 
/* 269 */           SpiderFrame.this.m_spiderApp.loadMappings();
/* 270 */           SpiderFrame.this.reloadMaps();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 274 */           if (!SystemUtils.m_verbose)
/*     */             return;
/* 276 */           Report.debug("applet", null, e);
/*     */         }
/*     */       }
/*     */     };
/* 281 */     editMapBtn.addActionListener(mapListener);
/*     */ 
/* 284 */     String filterLabel = LocaleResources.getString("csSpiderFilterLabel", this.m_cxt);
/* 285 */     gridBag.prepareAddRowElement(17);
/* 286 */     gridBag.m_gc.fill = 0;
/* 287 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(filterLabel, 1));
/*     */ 
/* 290 */     JPanel filterPanel = new PanePanel();
/* 291 */     this.m_appHelper.makePanelGridBag(filterPanel, 1);
/* 292 */     gridBag.m_gc.fill = 0;
/* 293 */     gridBag.prepareAddLastRowElement(17);
/* 294 */     this.m_appHelper.addComponent(configPanel, filterPanel);
/*     */ 
/* 296 */     gridBag.prepareAddRowElement(17);
/* 297 */     gridBag.m_gc.fill = 0;
/* 298 */     this.m_appHelper.addExchangeComponent(filterPanel, new CustomTextField(40), "SpiderFileFilter");
/*     */ 
/* 301 */     gridBag.m_gc.fill = 0;
/* 302 */     gridBag.prepareAddRowElement(13);
/* 303 */     this.m_appHelper.addExchangeComponent(filterPanel, new CustomCheckbox(LocaleResources.getString("csSpiderExcludeFilter", this.m_cxt)), "SpiderExcludeFilter");
/*     */ 
/* 307 */     if (!SharedObjects.getEnvValueAsBoolean("BatchBuilderEnableExternalCollections", false)) {
/*     */       return;
/*     */     }
/* 310 */     this.m_externalCheckBox = new CustomCheckbox(LocaleResources.getString("csSpiderExternal", this.m_cxt));
/* 311 */     if (!isLMCEnabled())
/*     */     {
/* 313 */       this.m_externalCheckBox.setEnabled(false);
/*     */     }
/* 315 */     gridBag.prepareAddRowElement(17);
/* 316 */     gridBag.m_gc.fill = 0;
/* 317 */     gridBag.prepareAddLastRowElement(17);
/* 318 */     this.m_appHelper.addComponent(configPanel, this.m_externalCheckBox);
/*     */ 
/* 321 */     ItemListener externalListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 325 */         SpiderFrame.this.enableExternalFields(SpiderFrame.this.m_externalCheckBox.isSelected());
/*     */       }
/*     */     };
/* 328 */     this.m_externalCheckBox.addItemListener(externalListener);
/* 329 */     this.m_externalCheckBox.setSelected(this.m_spiderApp.m_doExternal);
/*     */ 
/* 332 */     String collectionLabel = LocaleResources.getString("csSpiderCollectionLabel", this.m_cxt);
/* 333 */     gridBag.prepareAddRowElement(17);
/* 334 */     gridBag.m_gc.fill = 1;
/* 335 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(collectionLabel, 1));
/*     */ 
/* 338 */     this.m_collectionPanel = new PanePanel();
/* 339 */     this.m_appHelper.makePanelGridBag(this.m_collectionPanel, 1);
/* 340 */     gridBag.m_gc.fill = 0;
/* 341 */     gridBag.prepareAddLastRowElement(17);
/* 342 */     this.m_appHelper.addComponent(configPanel, this.m_collectionPanel);
/*     */ 
/* 344 */     this.m_collectionChoice = new CustomChoice();
/* 345 */     this.m_collectionChoice.setMinWidth(150);
/* 346 */     gridBag.m_gc.fill = 0;
/* 347 */     gridBag.prepareAddRowElement(17);
/* 348 */     this.m_appHelper.addExchangeComponent(this.m_collectionPanel, this.m_collectionChoice, "ExternalCollection");
/*     */ 
/* 350 */     gridBag.m_gc.fill = 0;
/* 351 */     gridBag.prepareAddRowElement(17);
/* 352 */     this.m_editCollectionBtn = new JButton(LocaleResources.getString("csButtonEditLocale", this.m_cxt));
/* 353 */     this.m_appHelper.addComponent(this.m_collectionPanel, this.m_editCollectionBtn);
/*     */ 
/* 355 */     ActionListener collectionListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 361 */           SpiderCollectionListDialog dlg = new SpiderCollectionListDialog(SpiderFrame.this.m_appHelper);
/* 362 */           dlg.init();
/* 363 */           dlg.prompt();
/*     */ 
/* 365 */           SpiderFrame.this.m_spiderApp.loadCollections();
/* 366 */           SpiderFrame.this.reloadCollections();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 370 */           if (!SystemUtils.m_verbose)
/*     */             return;
/* 372 */           Report.debug("applet", null, e);
/*     */         }
/*     */       }
/*     */     };
/* 377 */     this.m_editCollectionBtn.addActionListener(collectionListener);
/*     */ 
/* 380 */     String usageLabel = LocaleResources.getString("csSpiderUsageLabel", this.m_cxt);
/* 381 */     gridBag.prepareAddRowElement(17);
/* 382 */     gridBag.m_gc.fill = 1;
/* 383 */     this.m_appHelper.addComponent(configPanel, new CustomLabel(usageLabel, 1));
/*     */ 
/* 386 */     JPanel usagePanel = new PanePanel();
/* 387 */     this.m_appHelper.makePanelGridBag(usagePanel, 1);
/* 388 */     gridBag.m_gc.fill = 0;
/* 389 */     gridBag.prepareAddLastRowElement(17);
/* 390 */     this.m_appHelper.addComponent(configPanel, usagePanel);
/*     */ 
/* 392 */     this.m_usageChoice = new CustomChoice();
/* 393 */     this.m_usageChoice.setMinWidth(150);
/* 394 */     gridBag.m_gc.fill = 0;
/* 395 */     gridBag.prepareAddRowElement(17);
/* 396 */     this.m_appHelper.addExchangeComponent(usagePanel, this.m_usageChoice, "MetaDataUsage");
/*     */ 
/* 398 */     this.m_usageFieldsText = new CustomTextField(30);
/* 399 */     this.m_usageFieldsText.setEnabled(false);
/* 400 */     gridBag.m_gc.fill = 0;
/* 401 */     gridBag.prepareAddRowElement(13);
/* 402 */     this.m_appHelper.addExchangeComponent(usagePanel, this.m_usageFieldsText, "UsageFields");
/*     */ 
/* 405 */     ItemListener itemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 409 */         int state = e.getStateChange();
/* 410 */         switch (state)
/*     */         {
/*     */         case 1:
/* 413 */           SpiderFrame.this.enableDisableUsageFields();
/*     */         case 2:
/*     */         }
/*     */       }
/*     */     };
/* 421 */     this.m_usageChoice.addItemListener(itemListener);
/*     */   }
/*     */ 
/*     */   protected void addProgressBarPanel(JPanel mainPanel)
/*     */   {
/* 427 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/* 428 */     gridBag.m_gc.fill = 1;
/* 429 */     JPanel panel = addNewSubPanel(mainPanel, 1);
/*     */ 
/* 432 */     this.m_appHelper.addPanelTitle(panel, LocaleResources.getString("csSpiderProgressTitle", this.m_cxt));
/*     */ 
/* 435 */     this.m_progressText = new CustomLabel("  ", 1);
/* 436 */     gridBag.prepareAddLastRowElement();
/* 437 */     this.m_appHelper.addComponent(panel, this.m_progressText);
/*     */ 
/* 440 */     JPanel barPanel = new CustomPanel();
/* 441 */     barPanel.setLayout(new BorderLayout());
/* 442 */     this.m_progressBar = new ProgressBar(300, 30);
/* 443 */     barPanel.add("South", this.m_progressBar);
/* 444 */     gridBag.prepareAddLastRowElement();
/* 445 */     this.m_appHelper.addComponent(panel, barPanel);
/*     */ 
/* 447 */     this.m_buildBtn = new JButton(LocaleResources.getString("csSpiderBuildButton", this.m_cxt));
/* 448 */     ActionListener sListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 454 */           SpiderFrame.this.m_spiderApp.createBatchFile();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 458 */           if (!SystemUtils.m_verbose)
/*     */             return;
/* 460 */           Report.debug("applet", null, e);
/*     */         }
/*     */       }
/*     */     };
/* 465 */     this.m_buildBtn.addActionListener(sListener);
/*     */ 
/* 468 */     gridBag.prepareAddLastRowElement();
/* 469 */     gridBag.m_gc.fill = 0;
/* 470 */     this.m_appHelper.addComponent(panel, this.m_buildBtn);
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel, int anchor)
/*     */   {
/* 476 */     CustomPanel panel = new CustomPanel();
/* 477 */     this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 479 */     this.m_appHelper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 480 */     this.m_appHelper.addComponent(mainPanel, panel);
/* 481 */     this.m_appHelper.makePanelGridBag(panel, anchor);
/*     */ 
/* 483 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void reloadMaps()
/*     */   {
/* 488 */     boolean hasItems = false;
/*     */ 
/* 490 */     CustomChoice choice = (CustomChoice)this.m_mapPanel.getComponent(0);
/* 491 */     choice.removeAllItems();
/*     */ 
/* 493 */     Vector mapList = this.m_spiderApp.m_mapList;
/* 494 */     Sort.sortVector(mapList, this);
/*     */ 
/* 496 */     int numMaps = mapList.size();
/* 497 */     for (int i = 0; i < numMaps; ++i)
/*     */     {
/* 499 */       String mapName = (String)mapList.elementAt(i);
/* 500 */       choice.addItem(mapName);
/*     */ 
/* 502 */       hasItems = true;
/*     */     }
/*     */ 
/* 505 */     choice.setEnabled(hasItems);
/*     */   }
/*     */ 
/*     */   protected void reloadCollections()
/*     */   {
/* 510 */     boolean hasItems = false;
/*     */ 
/* 512 */     CustomChoice choice = (CustomChoice)this.m_collectionPanel.getComponent(0);
/* 513 */     choice.removeAllItems();
/*     */ 
/* 515 */     Vector collectionList = this.m_spiderApp.m_collectionList;
/* 516 */     Sort.sortVector(collectionList, this);
/*     */ 
/* 518 */     int numCollections = collectionList.size();
/* 519 */     for (int i = 0; i < numCollections; ++i)
/*     */     {
/* 521 */       String collectionID = (String)collectionList.elementAt(i);
/* 522 */       choice.addItem(collectionID);
/*     */ 
/* 524 */       hasItems = true;
/*     */     }
/*     */ 
/* 527 */     choice.setEnabled(hasItems);
/*     */   }
/*     */ 
/*     */   protected void loadUsages()
/*     */   {
/* 532 */     this.m_usageChoice.removeAllItems();
/* 533 */     this.m_usageChoice.addItem(LocaleResources.getString("csSpiderBatchloadOption", this.m_cxt));
/* 534 */     this.m_usageChoice.addItem(LocaleResources.getString("csSpiderInheritedOption", this.m_cxt));
/* 535 */     this.m_usageChoice.addItem(LocaleResources.getString("csSpiderSpecifiedOption", this.m_cxt));
/*     */ 
/* 537 */     this.m_usageChoice.setEnabled(true);
/*     */   }
/*     */ 
/*     */   protected void enableExternalFields(boolean isEnable)
/*     */   {
/* 542 */     this.m_spiderApp.m_doExternal = isEnable;
/*     */ 
/* 544 */     this.m_collectionChoice.setEnabled(isEnable);
/* 545 */     this.m_editCollectionBtn.setEnabled(isEnable);
/* 546 */     this.m_usageChoice.setEnabled(isEnable);
/*     */ 
/* 548 */     enableDisableUsageFields();
/*     */   }
/*     */ 
/*     */   public void enableDisableUsageFields()
/*     */   {
/* 553 */     if (this.m_usageChoice.isEnabled())
/*     */     {
/* 555 */       String usage = (String)this.m_usageChoice.getSelectedItem();
/* 556 */       if (usage.equalsIgnoreCase("specified"))
/*     */       {
/* 558 */         this.m_usageFieldsText.setEnabled(true);
/*     */       }
/*     */       else
/*     */       {
/* 562 */         this.m_usageFieldsText.setText("");
/* 563 */         this.m_usageFieldsText.setEnabled(false);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 568 */       this.m_usageFieldsText.setEnabled(false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isLMCEnabled()
/*     */   {
/* 575 */     DataBinder binder = ComponentLoader.getComponentBinder("LightlyManagedContent");
/* 576 */     return binder != null;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 584 */     this.m_progressText.setText(LocaleResources.localizeMessage(msg, this.m_cxt));
/* 585 */     validate();
/* 586 */     this.m_progressBar.updateProgressBar((int)amtDone);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 594 */     int retVal = 2;
/*     */ 
/* 596 */     if (this.m_spiderApp.m_spiderStarted)
/*     */     {
/* 598 */       retVal = MessageBox.doMessage(this.m_appHelper, IdcMessageFactory.lc("csSpiderExitMessage", new Object[0]), 4);
/*     */ 
/* 600 */       if (retVal == 1)
/*     */       {
/* 602 */         this.m_spiderApp.m_isExiting = true;
/*     */       }
/*     */     }
/*     */ 
/* 606 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 615 */     this.m_appHelper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 620 */     String name = exchange.m_compName;
/* 621 */     String value = exchange.m_compValue;
/* 622 */     String[] errMsg = new String[1];
/*     */ 
/* 624 */     if (!this.m_spiderApp.validateProperty(name, value, errMsg))
/*     */     {
/* 626 */       exchange.m_errorMessage = IdcMessageFactory.lc(errMsg[0], new Object[0]);
/* 627 */       return false;
/*     */     }
/*     */ 
/* 630 */     return true;
/*     */   }
/*     */ 
/*     */   public int compare(Object obj1, Object obj2)
/*     */   {
/* 638 */     String s1 = (String)obj1;
/* 639 */     String s2 = (String)obj2;
/*     */ 
/* 641 */     return s1.compareTo(s2);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 646 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.SpiderFrame
 * JD-Core Version:    0.5.4
 */