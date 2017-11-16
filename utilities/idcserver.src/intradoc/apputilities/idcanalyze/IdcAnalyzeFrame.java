/*     */ package intradoc.apputilities.idcanalyze;
/*     */ 
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.ProgressBar;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.gui.TabPanel;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Color;
/*     */ import java.awt.Component;
/*     */ import java.awt.Container;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class IdcAnalyzeFrame extends MainFrame
/*     */   implements ReportProgress, PromptHandler, ComponentBinder
/*     */ {
/*     */   protected IdcAnalyzeApp m_analyzeApp;
/*     */   protected boolean m_initialized;
/*     */   protected TabPanel m_tabPanel;
/*     */   protected ProgressBar m_taskProgressBar;
/*     */   protected CustomLabel m_taskProgressText;
/*     */   protected String m_previousProgressMsg;
/*     */   protected ProgressBar m_overallProgressBar;
/*     */   protected String m_previousOverallProgressMsg;
/*     */   protected CustomLabel m_overallProgressText;
/*     */   public JButton m_loadBtn;
/*     */   public JButton m_cancelBtn;
/*     */   protected Vector m_checkboxList;
/*     */   protected Vector m_topLevelCheckboxList;
/*     */   protected CustomTextArea m_logText;
/*     */ 
/*     */   public IdcAnalyzeFrame()
/*     */   {
/*  75 */     this.m_initialized = false;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  96 */     IdcMessage msg = null;
/*  97 */     if (title != null)
/*     */     {
/*  99 */       msg = IdcMessageFactory.lc();
/* 100 */       msg.m_msgEncoded = title;
/*     */     }
/* 102 */     init(msg, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/* 108 */     this.m_analyzeApp = new IdcAnalyzeApp(false);
/* 109 */     this.m_analyzeApp.init();
/* 110 */     this.m_analyzeApp.m_frame = this;
/*     */ 
/* 112 */     super.init(title, exitOnClose);
/*     */ 
/* 114 */     this.m_appHelper.attachToAppFrame(this, this.m_analyzeApp.m_idcProperties, null, title);
/* 115 */     this.m_appHelper.m_isCloseAllowedCallback = this;
/* 116 */     this.m_appHelper.m_componentBinder = this;
/*     */ 
/* 119 */     buildMenu();
/* 120 */     initUI();
/*     */ 
/* 122 */     this.m_appHelper.loadComponentValues();
/*     */ 
/* 124 */     pack();
/* 125 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected void buildMenu()
/*     */   {
/* 133 */     JMenuBar mb = new JMenuBar();
/* 134 */     setJMenuBar(mb);
/* 135 */     JMenu optMenu = new JMenu(LocaleResources.getString("csIDCAnalyzeOptMenu", this.m_cxt));
/*     */ 
/* 137 */     JMenuItem exitMI = new JMenuItem(LocaleResources.getString("csIDCAnalyzeExitItem", this.m_cxt));
/* 138 */     ActionListener exitListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/* 142 */         int retVal = IdcAnalyzeFrame.this.prompt();
/* 143 */         if ((retVal != 1) && (retVal != 2))
/*     */           return;
/* 145 */         IdcAnalyzeFrame.this.dispose();
/*     */       }
/*     */     };
/* 149 */     exitMI.addActionListener(exitListener);
/* 150 */     optMenu.add(exitMI);
/*     */ 
/* 152 */     mb.add(optMenu);
/* 153 */     addHelpMenu(mb);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 158 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 159 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/* 161 */     this.m_tabPanel = new TabPanel();
/*     */ 
/* 163 */     JPanel subPanel = addConfigurationPanel();
/* 164 */     this.m_tabPanel.addPane(LocaleResources.getString("csIDCAnalyzeConfigTabTitle", this.m_cxt), subPanel);
/*     */ 
/* 168 */     subPanel = addProgressPanel();
/* 169 */     this.m_tabPanel.addPane(LocaleResources.getString("csIDCAnalyzeProgressTabTitle", this.m_cxt), subPanel);
/*     */ 
/* 173 */     mainPanel.add("Center", this.m_tabPanel);
/* 174 */     addButtons(mainPanel);
/*     */   }
/*     */ 
/*     */   protected JPanel addConfigurationPanel()
/*     */   {
/* 179 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 181 */     CustomPanel tPanel = new CustomPanel();
/* 182 */     this.m_appHelper.makePanelGridBag(tPanel, 1);
/*     */ 
/* 184 */     DataResultSet drset = this.m_analyzeApp.m_defaultTaskList;
/* 185 */     gridBag.m_gc.fill = 0;
/* 186 */     this.m_checkboxList = new IdcVector();
/* 187 */     this.m_topLevelCheckboxList = new IdcVector();
/* 188 */     for (; drset.isRowPresent(); drset.next())
/*     */     {
/* 191 */       Properties props = drset.getCurrentRowProps();
/*     */ 
/* 193 */       if (props == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 197 */       JPanel p = createCheckboxGroupPanel(props);
/* 198 */       this.m_appHelper.addComponent(tPanel, p);
/*     */ 
/* 200 */       int gbcLeft = gridBag.m_gc.insets.left;
/* 201 */       int anchor = gridBag.m_gc.anchor;
/* 202 */       int oldGridHeight = gridBag.m_gc.gridheight;
/* 203 */       String desc = (String)props.get("description");
/* 204 */       if (desc == null)
/*     */       {
/* 206 */         desc = "csIDCAnalyzeEmptyDesc";
/*     */       }
/*     */ 
/* 209 */       this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/* 210 */       gridBag.m_gc.insets.left = 20;
/* 211 */       gridBag.m_gc.anchor = 18;
/* 212 */       gridBag.m_gc.gridheight = 3;
/* 213 */       this.m_appHelper.addComponent(tPanel, new CustomText(LocaleResources.localizeMessage("!" + desc, this.m_cxt), 100));
/*     */ 
/* 216 */       gridBag.m_gc.insets.left = gbcLeft;
/* 217 */       gridBag.m_gc.anchor = anchor;
/* 218 */       gridBag.m_gc.gridheight = oldGridHeight;
/*     */     }
/*     */ 
/* 221 */     JPanel panel1 = addExtraOptionsPanel();
/* 222 */     CustomPanel panel = new CustomPanel();
/* 223 */     this.m_appHelper.makePanelGridBag(panel, 1);
/*     */ 
/* 225 */     this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/* 226 */     this.m_appHelper.addComponent(panel, tPanel);
/* 227 */     this.m_appHelper.addComponent(panel, panel1);
/* 228 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel createCheckboxGroupPanel(Properties row)
/*     */   {
/* 233 */     JPanel p = new PanePanel();
/* 234 */     this.m_appHelper.makePanelGridBag(p, 1);
/* 235 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 237 */     DataResultSet drset = this.m_analyzeApp.m_taskOptionList;
/*     */ 
/* 239 */     String id = (String)row.get("id");
/* 240 */     String display = (String)row.get("displayname");
/* 241 */     String optionStr = (String)row.get("options");
/* 242 */     Vector optionList = StringUtils.parseArray(optionStr, ',', '^');
/*     */ 
/* 244 */     JCheckBox ofCheckbox = new CustomCheckbox(LocaleResources.getString(display, this.m_cxt));
/*     */ 
/* 246 */     gridBag.prepareAddLastRowElement(17);
/* 247 */     this.m_appHelper.addExchangeComponent(p, ofCheckbox, id);
/* 248 */     this.m_checkboxList.addElement(ofCheckbox);
/* 249 */     this.m_topLevelCheckboxList.addElement(ofCheckbox);
/*     */ 
/* 251 */     gridBag.prepareAddRowElement();
/*     */ 
/* 253 */     gridBag.m_gc.fill = 2;
/* 254 */     Insets oldInsets = gridBag.m_gc.insets;
/* 255 */     gridBag.m_gc.anchor = 15;
/* 256 */     gridBag.m_gc.insets = new Insets(0, 25, 0, 0);
/*     */ 
/* 259 */     Vector opList = new IdcVector();
/* 260 */     for (int i = 0; i < optionList.size(); ++i)
/*     */     {
/* 262 */       gridBag.prepareAddLastRowElement();
/* 263 */       String option = (String)optionList.elementAt(i);
/* 264 */       if (option == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 269 */       String displayName = null;
/*     */       try
/*     */       {
/* 272 */         displayName = ResultSetUtils.findValue(drset, "id", option, "displayname");
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */ 
/* 279 */       if (displayName == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 284 */       if (i == optionList.size() - 1)
/*     */       {
/* 287 */         gridBag.prepareAddRowElement();
/*     */       }
/*     */ 
/* 291 */       JCheckBox opCheckbox = new CustomCheckbox(LocaleResources.getString(displayName, this.m_cxt));
/*     */ 
/* 293 */       opCheckbox.setEnabled(false);
/* 294 */       this.m_appHelper.addExchangeComponent(p, opCheckbox, option);
/* 295 */       opList.addElement(opCheckbox);
/* 296 */       this.m_checkboxList.addElement(opCheckbox);
/*     */     }
/*     */ 
/* 299 */     ItemListener cbListener = new ItemListener(ofCheckbox, opList)
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 303 */         boolean isEnabled = this.val$ofCheckbox.isSelected();
/* 304 */         for (int i = 0; i < this.val$opList.size(); ++i)
/*     */         {
/* 306 */           JCheckBox cb = (JCheckBox)this.val$opList.elementAt(i);
/* 307 */           cb.setEnabled(isEnabled);
/*     */         }
/*     */       }
/*     */     };
/* 311 */     ofCheckbox.addItemListener(cbListener);
/*     */ 
/* 313 */     gridBag.m_gc.insets = oldInsets;
/*     */ 
/* 315 */     return p;
/*     */   }
/*     */ 
/*     */   protected JPanel addExtraOptionsPanel()
/*     */   {
/* 320 */     JPanel panel = new CustomPanel();
/* 321 */     this.m_appHelper.makePanelGridBag(panel, 1);
/*     */ 
/* 323 */     this.m_appHelper.m_gridHelper.prepareAddRowElement();
/* 324 */     this.m_appHelper.addLabelEditPairEx(panel, LocaleResources.getString("csIDCAnalyzeRangeTitle", this.m_cxt), 50, "IdcAnalyzeRange", false);
/*     */ 
/* 328 */     Insets oldInsets = this.m_appHelper.m_gridHelper.m_gc.insets;
/* 329 */     this.m_appHelper.m_gridHelper.m_gc.insets = new Insets(0, 25, 0, 0);
/* 330 */     this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/* 331 */     this.m_appHelper.addComponent(panel, new CustomText(LocaleResources.localizeMessage("!csIDCAnalyzeRangeDesc", this.m_cxt), 50));
/*     */ 
/* 334 */     this.m_appHelper.m_gridHelper.m_gc.insets = oldInsets;
/*     */ 
/* 336 */     this.m_appHelper.addLabelEditPair(panel, LocaleResources.getString("csIDCAnalyzeLogDirTitle", this.m_cxt), 50, "IdcAnalyzeLogDir");
/*     */ 
/* 339 */     return panel;
/*     */   }
/*     */ 
/*     */   protected JPanel addProgressPanel()
/*     */   {
/* 344 */     CustomPanel panel = new CustomPanel();
/* 345 */     this.m_appHelper.makePanelGridBag(panel, 1);
/* 346 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 348 */     CustomPanel subPanel = new CustomPanel();
/* 349 */     this.m_appHelper.makePanelGridBag(subPanel, 1);
/*     */ 
/* 352 */     this.m_taskProgressText = new CustomLabel(LocaleResources.localizeMessage("!csIDCAnalyzeTaskProgressTitle", this.m_cxt), 1);
/*     */ 
/* 357 */     gridBag.prepareAddLastRowElement();
/* 358 */     this.m_appHelper.addComponent(subPanel, this.m_taskProgressText);
/*     */ 
/* 361 */     JPanel barPanel = new CustomPanel();
/* 362 */     barPanel.setLayout(new BorderLayout());
/* 363 */     this.m_taskProgressBar = new ProgressBar(600, 30);
/* 364 */     barPanel.add("South", this.m_taskProgressBar);
/*     */ 
/* 366 */     gridBag.prepareAddLastRowElement();
/* 367 */     this.m_appHelper.addComponent(subPanel, barPanel);
/*     */ 
/* 369 */     this.m_overallProgressText = new CustomLabel(LocaleResources.localizeMessage("!csIDCAnalyzeOverallProgressTitle", this.m_cxt), 1);
/*     */ 
/* 373 */     gridBag.prepareAddLastRowElement();
/* 374 */     this.m_appHelper.addComponent(subPanel, this.m_overallProgressText);
/*     */ 
/* 376 */     barPanel = new CustomPanel();
/* 377 */     barPanel.setLayout(new BorderLayout());
/* 378 */     this.m_overallProgressBar = new ProgressBar(600, 30);
/* 379 */     barPanel.add("South", this.m_overallProgressBar);
/*     */ 
/* 381 */     gridBag.prepareAddLastRowElement();
/* 382 */     this.m_appHelper.addComponent(subPanel, barPanel);
/* 383 */     this.m_appHelper.addComponent(panel, subPanel);
/*     */ 
/* 385 */     barPanel = addLogPanel();
/* 386 */     gridBag.prepareAddLastRowElement();
/* 387 */     double oldWeighty = gridBag.m_gc.weighty;
/* 388 */     gridBag.m_gc.weighty = 1.0D;
/* 389 */     this.m_appHelper.addComponent(panel, barPanel);
/* 390 */     gridBag.m_gc.weighty = oldWeighty;
/*     */ 
/* 392 */     return panel;
/*     */   }
/*     */ 
/*     */   protected void addButtons(JPanel mainPanel)
/*     */   {
/* 397 */     JPanel panel = new PanePanel();
/* 398 */     panel.setLayout(new FlowLayout());
/*     */ 
/* 400 */     this.m_loadBtn = new JButton(LocaleResources.getString("csIDCAnalyzeStartDesc", this.m_cxt));
/* 401 */     ActionListener sListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 407 */           IdcAnalyzeFrame.this.m_appHelper.retrieveComponentValues();
/* 408 */           IdcAnalyzeFrame.this.changeCheckboxState(false);
/* 409 */           IdcAnalyzeFrame.this.m_tabPanel.selectPane(LocaleResources.getString("csIDCAnalyzeProgressTabTitle", IdcAnalyzeFrame.this.m_cxt));
/* 410 */           IdcAnalyzeFrame.this.m_analyzeApp.analyze();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 414 */           MessageBox.reportError(IdcAnalyzeFrame.this.m_appHelper, e, IdcMessageFactory.lc("csIDCAnalyzeError", new Object[0]));
/*     */ 
/* 416 */           IdcAnalyzeFrame.this.m_analyzeApp.cancelAnalysis();
/*     */         }
/*     */       }
/*     */     };
/* 420 */     this.m_loadBtn.addActionListener(sListener);
/* 421 */     panel.add(this.m_loadBtn);
/*     */ 
/* 423 */     this.m_cancelBtn = new JButton(LocaleResources.getString("csIDCAnalyzeCancel", this.m_cxt));
/* 424 */     sListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/* 428 */         int retVal = MessageBox.doMessage(IdcAnalyzeFrame.this.m_appHelper, "!csIDCAnalyzeCancelAfterCurrentTask", 4);
/*     */ 
/* 431 */         if (retVal != 2)
/*     */           return;
/* 433 */         IdcAnalyzeFrame.this.m_analyzeApp.cancelAnalysis();
/*     */       }
/*     */     };
/* 437 */     this.m_cancelBtn.addActionListener(sListener);
/* 438 */     this.m_cancelBtn.setEnabled(false);
/* 439 */     panel.add(this.m_cancelBtn);
/* 440 */     mainPanel.add("South", panel);
/*     */   }
/*     */ 
/*     */   protected JPanel addLogPanel()
/*     */   {
/* 445 */     JPanel panel = new CustomPanel();
/* 446 */     panel.setLayout(new BorderLayout());
/*     */ 
/* 448 */     this.m_logText = new CustomTextArea(2, 80);
/* 449 */     this.m_logText.setEditable(false);
/* 450 */     this.m_logText.setBackground(Color.white);
/*     */ 
/* 452 */     panel.add("Center", this.m_logText);
/* 453 */     return panel;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 461 */     if (msg == null)
/*     */     {
/* 463 */       msg = this.m_previousProgressMsg;
/*     */     }
/*     */     else
/*     */     {
/* 467 */       this.m_previousProgressMsg = msg;
/*     */     }
/* 469 */     if (msg != null)
/*     */     {
/* 471 */       this.m_taskProgressText.setText(LocaleResources.localizeMessage(msg, this.m_cxt));
/*     */     }
/* 473 */     validate();
/* 474 */     float tmp = amtDone * 100.0F / max;
/* 475 */     this.m_taskProgressBar.updateProgressBar((int)tmp);
/*     */   }
/*     */ 
/*     */   public void reportOverallProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 480 */     if (msg == null)
/*     */     {
/* 482 */       msg = this.m_previousOverallProgressMsg;
/*     */     }
/*     */     else
/*     */     {
/* 486 */       this.m_previousOverallProgressMsg = msg;
/*     */     }
/* 488 */     if (msg != null)
/*     */     {
/* 490 */       this.m_overallProgressText.setText(LocaleResources.localizeMessage(msg, this.m_cxt));
/*     */     }
/* 492 */     validate();
/* 493 */     float tmp = amtDone * 100.0F / max;
/* 494 */     this.m_overallProgressBar.updateProgressBar((int)tmp);
/*     */   }
/*     */ 
/*     */   public void appendToOutputFrame(String msg)
/*     */   {
/* 499 */     this.m_logText.append(msg);
/*     */   }
/*     */ 
/*     */   public void resetOutputFrame(String msg)
/*     */   {
/* 504 */     String logMsg = msg;
/* 505 */     if ((logMsg != null) && (logMsg.length() > 0))
/*     */     {
/* 507 */       logMsg = LocaleResources.getString(logMsg, this.m_cxt);
/*     */     }
/* 509 */     this.m_logText.setText(logMsg);
/*     */   }
/*     */ 
/*     */   public void changeCheckboxState(boolean isEnabled)
/*     */   {
/* 514 */     Vector list = (isEnabled) ? this.m_topLevelCheckboxList : this.m_checkboxList;
/* 515 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/* 517 */       JCheckBox cb = (JCheckBox)list.elementAt(i);
/* 518 */       cb.setEnabled(isEnabled);
/* 519 */       if (!cb.isSelected()) {
/*     */         continue;
/*     */       }
/* 522 */       Component[] cList = cb.getParent().getComponents();
/* 523 */       for (int j = 0; j < cList.length; ++j)
/*     */       {
/* 525 */         if (!cList[j] instanceof JCheckBox)
/*     */           continue;
/* 527 */         JCheckBox item = (JCheckBox)cList[j];
/* 528 */         item.setEnabled(true);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 540 */     int retVal = 2;
/*     */ 
/* 542 */     if (this.m_analyzeApp.m_analyzerStarted)
/*     */     {
/* 544 */       retVal = MessageBox.doMessage(this.m_appHelper, IdcMessageFactory.lc("csIDCAnalyzeEarlyExit", new Object[0]), 4);
/*     */ 
/* 547 */       if (retVal == 1)
/*     */       {
/* 549 */         this.m_analyzeApp.m_isExiting = true;
/*     */       }
/*     */     }
/*     */ 
/* 553 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 562 */     this.m_appHelper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 567 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 572 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 94748 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.idcanalyze.IdcAnalyzeFrame
 * JD-Core Version:    0.5.4
 */