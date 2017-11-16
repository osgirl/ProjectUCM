/*     */ package intradoc.apputilities.batchloader;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.apps.shared.MainFrame;
/*     */ import intradoc.apps.shared.ReflectionMethodAddMenuItems;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.gui.AppFrameHelper;
/*     */ import intradoc.gui.ComponentBinder;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.ProgressBar;
/*     */ import intradoc.gui.PromptHandler;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JMenu;
/*     */ import javax.swing.JMenuBar;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class BatchLoaderFrame extends MainFrame
/*     */   implements ReportProgress, PromptHandler, ComponentBinder
/*     */ {
/*     */   protected BatchLoaderApp m_batchApp;
/*     */   protected boolean m_initialized;
/*     */   protected JCheckBox m_cleanUpCheckbox;
/*     */   protected JCheckBox m_enableErrorCheckbox;
/*     */   protected ProgressBar m_progressBar;
/*     */   protected CustomLabel m_progressText;
/*     */   protected JButton m_loadBtn;
/*     */ 
/*     */   public BatchLoaderFrame()
/*     */   {
/*  66 */     this.m_initialized = false;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void init(String title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  79 */     IdcMessage msg = null;
/*  80 */     if (title != null)
/*     */     {
/*  82 */       msg = IdcMessageFactory.lc();
/*  83 */       msg.m_msgEncoded = title;
/*     */     }
/*  85 */     init(msg, exitOnClose);
/*     */   }
/*     */ 
/*     */   public void init(IdcMessage title, boolean exitOnClose)
/*     */     throws ServiceException
/*     */   {
/*  91 */     this.m_batchApp = new BatchLoaderApp(null, false);
/*  92 */     this.m_batchApp.init();
/*  93 */     this.m_batchApp.m_frame = this;
/*     */ 
/*  95 */     super.init(title, exitOnClose);
/*  96 */     this.m_batchApp.m_cxt = this.m_cxt;
/*     */ 
/*  98 */     this.m_appHelper.attachToAppFrame(this, this.m_batchApp.m_idcProperties, null, title);
/*  99 */     this.m_appHelper.m_isCloseAllowedCallback = this;
/* 100 */     this.m_appHelper.m_componentBinder = this;
/*     */ 
/* 103 */     buildMenu();
/* 104 */     initUI();
/*     */ 
/* 106 */     this.m_appHelper.loadComponentValues();
/*     */ 
/* 108 */     pack();
/* 109 */     setVisible(true);
/*     */   }
/*     */ 
/*     */   protected void buildMenu()
/*     */   {
/* 117 */     JMenuBar mb = new JMenuBar();
/* 118 */     setJMenuBar(mb);
/* 119 */     JMenu optMenu = new JMenu(LocaleResources.getString("csBatchLoaderOptMenu", this.m_cxt));
/*     */ 
/* 121 */     JMenuItem saveMI = new JMenuItem(LocaleResources.getString("csBatchLoaderSaveConfigItem", this.m_cxt));
/* 122 */     ActionListener saveListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 128 */           if (BatchLoaderFrame.this.m_appHelper.retrieveComponentValues())
/*     */           {
/* 130 */             BatchLoaderFrame.this.m_batchApp.setBatchLoaderPath(BatchLoaderFrame.this.m_batchApp.m_idcProperties.getProperty("BatchLoaderPath"));
/* 131 */             BatchLoaderFrame.this.m_batchApp.writeIntradocCfgFile();
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 136 */           BatchLoaderFrame.this.reportError(e);
/*     */         }
/*     */       }
/*     */     };
/* 141 */     saveMI.addActionListener(saveListener);
/* 142 */     optMenu.add(saveMI);
/*     */ 
/* 144 */     JMenuItem spiderMI = new JMenuItem(LocaleResources.getString("csBatchLoaderSpiderMenuItem", this.m_cxt));
/* 145 */     ActionListener spiderListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 151 */           AppLauncher.launch("Spider");
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 155 */           BatchLoaderFrame.this.reportError(e);
/*     */         }
/*     */       }
/*     */     };
/* 160 */     spiderMI.addActionListener(spiderListener);
/* 161 */     optMenu.add(spiderMI);
/*     */ 
/* 163 */     ReflectionMethodAddMenuItems.addReflectionMethodMenuItemsFromStandardTable(optMenu, this, "*batch*");
/*     */ 
/* 165 */     JMenuItem exitMI = new JMenuItem(LocaleResources.getString("csBatchLoaderExitItem", this.m_cxt));
/* 166 */     ActionListener exitListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/* 170 */         int retVal = BatchLoaderFrame.this.prompt();
/* 171 */         if ((retVal != 1) && (retVal != 2))
/*     */           return;
/* 173 */         BatchLoaderFrame.this.dispose();
/*     */       }
/*     */     };
/* 177 */     exitMI.addActionListener(exitListener);
/* 178 */     optMenu.add(exitMI);
/*     */ 
/* 180 */     mb.add(optMenu);
/* 181 */     addHelpMenu(mb);
/*     */   }
/*     */ 
/*     */   protected void initUI()
/*     */   {
/* 186 */     JPanel mainPanel = this.m_appHelper.m_mainPanel;
/* 187 */     this.m_appHelper.makePanelGridBag(mainPanel, 1);
/*     */ 
/* 189 */     addConfigurationPanel(mainPanel);
/* 190 */     addProgressBarPanel(mainPanel);
/*     */   }
/*     */ 
/*     */   protected void addConfigurationPanel(JPanel mainPanel)
/*     */   {
/* 195 */     JPanel panel = addNewSubPanel(mainPanel, 1);
/* 196 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 198 */     gridBag.prepareAddRowElement(13);
/* 199 */     this.m_appHelper.addComponent(panel, new CustomLabel(LocaleResources.getString("csBatchLoaderDesc", this.m_cxt), 1));
/*     */ 
/* 202 */     this.m_appHelper.addFilePathComponent(panel, 50, LocaleResources.getString("csBatchLoaderDesc", this.m_cxt), "BatchLoaderPath");
/*     */ 
/* 204 */     gridBag.addEmptyRow(panel);
/* 205 */     gridBag.m_gc.fill = 0;
/*     */ 
/* 207 */     gridBag.prepareAddRowElement();
/* 208 */     this.m_appHelper.addComponent(panel, new CustomLabel(LocaleResources.getString("csBatchLoaderMaxErrorsDesc", this.m_cxt), 1));
/*     */ 
/* 213 */     gridBag.prepareAddLastRowElement(17);
/* 214 */     this.m_appHelper.addExchangeComponent(panel, new CustomTextField(10), "MaxErrorsAllowed");
/* 215 */     gridBag.m_gc.fill = 1;
/*     */ 
/* 218 */     this.m_cleanUpCheckbox = new CustomCheckbox(LocaleResources.getString("csBatchLoaderCleanUpDesc", this.m_cxt));
/* 219 */     gridBag.addEmptyRowElement(panel);
/* 220 */     gridBag.addEmptyRowElement(panel);
/* 221 */     gridBag.prepareAddLastRowElement(13);
/* 222 */     this.m_appHelper.addExchangeComponent(panel, this.m_cleanUpCheckbox, "CleanUp");
/*     */ 
/* 225 */     this.m_enableErrorCheckbox = new CustomCheckbox(LocaleResources.getString("csBatchLoaderEnableErrorFileDesc", this.m_cxt));
/*     */ 
/* 227 */     gridBag.addEmptyRowElement(panel);
/* 228 */     gridBag.addEmptyRowElement(panel);
/* 229 */     gridBag.prepareAddLastRowElement(13);
/* 230 */     this.m_appHelper.addExchangeComponent(panel, this.m_enableErrorCheckbox, "EnableErrorFile");
/*     */   }
/*     */ 
/*     */   protected void addProgressBarPanel(JPanel mainPanel)
/*     */   {
/* 235 */     JPanel panel = addNewSubPanel(mainPanel, 1);
/* 236 */     GridBagHelper gridBag = this.m_appHelper.m_gridHelper;
/*     */ 
/* 239 */     this.m_appHelper.addPanelTitle(panel, LocaleResources.getString("csBatchLoaderProgressTitle", this.m_cxt));
/*     */ 
/* 242 */     this.m_progressText = new CustomLabel("  ", 1);
/* 243 */     gridBag.prepareAddLastRowElement();
/* 244 */     this.m_appHelper.addComponent(panel, this.m_progressText);
/*     */ 
/* 247 */     JPanel barPanel = new CustomPanel();
/* 248 */     barPanel.setLayout(new BorderLayout());
/* 249 */     this.m_progressBar = new ProgressBar(300, 30);
/* 250 */     barPanel.add("South", this.m_progressBar);
/* 251 */     gridBag.prepareAddLastRowElement();
/* 252 */     this.m_appHelper.addComponent(panel, barPanel);
/*     */ 
/* 254 */     this.m_loadBtn = new JButton(LocaleResources.getString("csBatchLoaderLoadDesc", this.m_cxt));
/* 255 */     ActionListener sListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent ae)
/*     */       {
/*     */         try
/*     */         {
/* 261 */           BatchLoaderFrame.this.m_batchApp.loadBatchLoader();
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 265 */           if (!SystemUtils.m_verbose)
/*     */             return;
/* 267 */           Report.debug("applet", null, e);
/*     */         }
/*     */       }
/*     */     };
/* 272 */     this.m_loadBtn.addActionListener(sListener);
/*     */ 
/* 275 */     gridBag.prepareAddLastRowElement();
/* 276 */     gridBag.m_gc.fill = 0;
/* 277 */     this.m_appHelper.addComponent(panel, this.m_loadBtn);
/*     */   }
/*     */ 
/*     */   protected JPanel addNewSubPanel(JPanel mainPanel, int anchor)
/*     */   {
/* 283 */     CustomPanel panel = new CustomPanel();
/* 284 */     this.m_appHelper.m_gridHelper.prepareAddLastRowElement();
/*     */ 
/* 286 */     this.m_appHelper.m_gridHelper.m_gc.insets = new Insets(2, 5, 2, 5);
/* 287 */     this.m_appHelper.addComponent(mainPanel, panel);
/* 288 */     this.m_appHelper.makePanelGridBag(panel, anchor);
/*     */ 
/* 290 */     return panel;
/*     */   }
/*     */ 
/*     */   public void reportProgress(int type, String msg, float amtDone, float max)
/*     */   {
/* 298 */     this.m_progressText.setText(LocaleResources.localizeMessage(msg, null));
/* 299 */     validate();
/* 300 */     this.m_progressBar.updateProgressBar((int)amtDone);
/*     */   }
/*     */ 
/*     */   public int prompt()
/*     */   {
/* 308 */     int retVal = 2;
/*     */ 
/* 310 */     if (this.m_batchApp.m_batchLoaderStarted)
/*     */     {
/* 312 */       retVal = MessageBox.doMessage(this.m_appHelper, IdcMessageFactory.lc("csBatchLoaderEarlyExit", new Object[0]), 4);
/*     */ 
/* 315 */       if (retVal == 1)
/*     */       {
/* 317 */         this.m_batchApp.m_isExiting = true;
/*     */       }
/*     */     }
/*     */ 
/* 321 */     return retVal;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 330 */     this.m_appHelper.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 335 */     String name = exchange.m_compName;
/* 336 */     String value = exchange.m_compValue;
/* 337 */     String[] errMsg = new String[1];
/*     */ 
/* 339 */     if (!this.m_batchApp.validateProperty(name, value, errMsg))
/*     */     {
/* 341 */       exchange.m_errorMessage = IdcMessageFactory.lc();
/* 342 */       exchange.m_errorMessage.m_msgEncoded = errMsg[0];
/* 343 */       return false;
/*     */     }
/* 345 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 350 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.batchloader.BatchLoaderFrame
 * JD-Core Version:    0.5.4
 */