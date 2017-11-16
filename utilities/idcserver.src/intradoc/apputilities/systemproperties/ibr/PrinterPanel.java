/*     */ package intradoc.apputilities.systemproperties.ibr;
/*     */ 
/*     */ import intradoc.apputilities.systemproperties.SystemPropertiesPanel;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.CustomTextField;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.GuiText;
/*     */ import intradoc.gui.IdcFileFilter;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.IdcFileChooser;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class PrinterPanel extends SystemPropertiesPanel
/*     */ {
/*     */   protected CustomTextField m_prnNameFld;
/*     */   protected CustomTextField m_prnPortFld;
/*     */   protected CustomTextField m_prnDriverFld;
/*     */   protected CustomTextField m_prnInfFileFld;
/*     */   protected JButton m_prnInvBrowseBtn;
/*     */   protected JButton m_installPrinterBtn;
/*     */   protected boolean m_didInstallPrinter;
/*     */   protected boolean m_canInstallPrinter;
/*     */   boolean m_disableSystemPropertiesPrinterUtils;
/*     */   protected Win32PrinterHelper m_printerHelper;
/*     */   protected boolean m_useGSDriverData;
/*     */ 
/*     */   protected void initUI()
/*     */     throws ServiceException
/*     */   {
/*  72 */     this.m_didInstallPrinter = false;
/*  73 */     this.m_disableSystemPropertiesPrinterUtils = SharedObjects.getEnvValueAsBoolean("DisableSystemPropertiesPrinterUtils", false);
/*     */ 
/*  75 */     if (!this.m_disableSystemPropertiesPrinterUtils)
/*     */     {
/*  77 */       this.m_printerHelper = new Win32PrinterHelper();
/*  78 */       this.m_useGSDriverData = (!this.m_printerHelper.isWin8SignedMicrosoftPSDriverAvailable());
/*     */     }
/*     */ 
/*  82 */     JPanel infoPanel = new CustomPanel();
/*  83 */     this.m_helper.makePanelGridBag(infoPanel, 1);
/*  84 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/*     */ 
/*  87 */     this.m_helper.addPanelTitle(infoPanel, LocaleResources.getString("csIBRPrinterPanelTitle", null));
/*  88 */     gridBag.m_gc.weighty = 0.0D;
/*  89 */     gridBag.prepareAddLastRowElement();
/*  90 */     this.m_helper.addComponent(infoPanel, new CustomLabel(""));
/*     */ 
/*  92 */     JPanel subPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/*  94 */     gridBag.prepareAddLastRowElement(18);
/*  95 */     this.m_helper.addComponent(subPanel, new CustomLabel(LocaleResources.getString("csPrinterPanelLabelINFPath", null), 1));
/*     */ 
/*  98 */     gridBag.m_gc.weighty = 1.0D;
/*  99 */     gridBag.addEmptyRow(infoPanel);
/*     */ 
/* 101 */     gridBag.prepareAddLastRowElement(18);
/* 102 */     addPrinterInfFileBrowseComponent(subPanel, 50, LocaleResources.getString("csPrinterPanelBrowserINFFile", null), "PrinterInfPath", "*.inf", true);
/*     */ 
/* 104 */     JPanel advPanel = addNewSubPanel(infoPanel, 1);
/*     */ 
/* 106 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(""));
/* 107 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(LocaleResources.getString("csPrinterPanelLabelPrinterName", null), 1));
/*     */ 
/* 110 */     gridBag.prepareAddLastRowElement(18);
/* 111 */     this.m_prnNameFld = new CustomTextField(50);
/* 112 */     this.m_helper.addExchangeComponent(advPanel, this.m_prnNameFld, "PostscriptPrinterName");
/*     */ 
/* 114 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(""));
/* 115 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(LocaleResources.getString("csPrinterPanelLabelDriverName", null), 1));
/*     */ 
/* 118 */     gridBag.prepareAddLastRowElement(18);
/* 119 */     this.m_prnDriverFld = new CustomTextField(50);
/* 120 */     this.m_helper.addExchangeComponent(advPanel, this.m_prnDriverFld, "PrintDriverName");
/*     */ 
/* 122 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(""));
/* 123 */     this.m_helper.addLastComponentInRow(advPanel, new CustomLabel(LocaleResources.getString("csPrinterPanelLabelPortPath", null), 1));
/*     */ 
/* 126 */     gridBag.prepareAddLastRowElement(18);
/* 127 */     this.m_prnPortFld = new CustomTextField(50);
/* 128 */     this.m_helper.addExchangeComponent(advPanel, this.m_prnPortFld, "PrinterPortPath");
/*     */ 
/* 130 */     this.m_installPrinterBtn = new JButton(LocaleResources.getString("csPrinterPanelLabelInstallPrinter", null));
/* 131 */     ActionListener installBtnListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 135 */         PrinterPanel.this.installPrinter();
/*     */       }
/*     */     };
/* 138 */     this.m_installPrinterBtn.addActionListener(installBtnListener);
/* 139 */     infoPanel.add(this.m_installPrinterBtn);
/*     */ 
/* 141 */     setLayout(new BorderLayout());
/* 142 */     add("Center", infoPanel);
/*     */   }
/*     */ 
/*     */   protected void initPrinterHelper(String prnName)
/*     */   {
/* 147 */     this.m_canInstallPrinter = false;
/* 148 */     if (!this.m_disableSystemPropertiesPrinterUtils)
/*     */     {
/* 150 */       boolean hasPrinterAccess = this.m_printerHelper.isPrinterUtilsReady();
/* 151 */       if (hasPrinterAccess)
/*     */       {
/* 153 */         this.m_canInstallPrinter = (!this.m_printerHelper.isPrinterInstalled(prnName));
/*     */       }
/*     */     }
/* 156 */     enableDisableFields(this.m_canInstallPrinter);
/*     */   }
/*     */ 
/*     */   protected void enableDisableFields(boolean enable)
/*     */   {
/* 161 */     this.m_prnNameFld.setEnabled(enable);
/* 162 */     this.m_prnInfFileFld.setEnabled(enable);
/* 163 */     this.m_prnInvBrowseBtn.setEnabled(enable);
/* 164 */     this.m_prnDriverFld.setEnabled(enable);
/* 165 */     this.m_prnPortFld.setEnabled(enable);
/* 166 */     this.m_installPrinterBtn.setEnabled(enable);
/*     */   }
/*     */ 
/*     */   protected void installPrinter()
/*     */   {
/* 171 */     String prnName = this.m_prnNameFld.getText();
/* 172 */     String prnPortPath = FileUtils.fileSlashes(this.m_prnPortFld.getText());
/* 173 */     String prnDriverName = this.m_prnDriverFld.getText();
/* 174 */     String prnInfFile = FileUtils.fileSlashes(this.m_prnInfFileFld.getText());
/*     */ 
/* 182 */     IdcMessage errMsg = validateValues("PrinterInfPath", prnInfFile);
/* 183 */     if (errMsg != null)
/*     */     {
/* 185 */       MessageBox.reportError(this.m_sysInterface, errMsg);
/* 186 */       return;
/*     */     }
/*     */ 
/* 189 */     errMsg = validateValues("PrinterPortPath", prnPortPath);
/* 190 */     if (errMsg != null)
/*     */     {
/* 192 */       MessageBox.reportError(this.m_sysInterface, errMsg);
/* 193 */       return;
/*     */     }
/*     */ 
/* 196 */     errMsg = validateValues("PostscriptPrinterName", prnName);
/* 197 */     if (errMsg != null)
/*     */     {
/* 199 */       MessageBox.reportError(this.m_sysInterface, errMsg);
/* 200 */       return;
/*     */     }
/*     */ 
/* 203 */     errMsg = validateValues("PrintDriverName", prnDriverName);
/* 204 */     if (errMsg != null)
/*     */     {
/* 206 */       MessageBox.reportError(this.m_sysInterface, errMsg);
/* 207 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 212 */       boolean installed = this.m_printerHelper.installPrinterWithInf(prnName, prnPortPath, prnDriverName, prnInfFile);
/* 213 */       if (installed)
/*     */       {
/* 215 */         this.m_didInstallPrinter = this.m_printerHelper.isPrinterInstalled(prnName);
/* 216 */         if (this.m_didInstallPrinter)
/*     */         {
/* 218 */           MessageBox.doMessage(this.m_sysInterface, IdcMessageFactory.lc("csPrinterSuccessfullyInstalled", new Object[] { prnName, prnPortPath }), 1);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 226 */         MessageBox.reportError(this.m_sysInterface, this.m_printerHelper.getErrorMsg());
/*     */       }
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 231 */       Report.trace(null, null, exp);
/*     */     }
/* 233 */     enableDisableFields(!this.m_didInstallPrinter);
/*     */   }
/*     */ 
/*     */   public void addPrinterInfFileBrowseComponent(JPanel mainPanel, int textFieldSize, String title, String name, String defaultFile, boolean isRowEnd)
/*     */   {
/* 239 */     this.m_prnInfFileFld = new CustomTextField(textFieldSize);
/* 240 */     this.m_prnInvBrowseBtn = new JButton(GuiText.m_browseLabel);
/*     */ 
/* 242 */     String fileDlgTitle = title;
/* 243 */     String defaultFilename = defaultFile;
/*     */ 
/* 245 */     ActionListener bListener = new ActionListener(fileDlgTitle, defaultFilename)
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 249 */         JFileChooser fileDlg = new IdcFileChooser();
/* 250 */         fileDlg.setDialogTitle(this.val$fileDlgTitle);
/*     */ 
/* 252 */         if (this.val$defaultFilename != null)
/*     */         {
/* 254 */           IdcFileFilter filter = new IdcFileFilter();
/* 255 */           filter.m_pattern = this.val$defaultFilename;
/* 256 */           fileDlg.setFileFilter(filter);
/*     */         }
/* 258 */         fileDlg.showOpenDialog(null);
/*     */ 
/* 261 */         File file = fileDlg.getSelectedFile();
/* 262 */         if (file == null)
/*     */           return;
/* 264 */         PrinterPanel.this.m_prnInfFileFld.setText(file.getAbsolutePath());
/*     */       }
/*     */     };
/* 269 */     this.m_prnInvBrowseBtn.addActionListener(bListener);
/*     */ 
/* 271 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/* 272 */     GridBagConstraints gc = gridBag.m_gc;
/* 273 */     int oldfill = gc.fill;
/* 274 */     double oldweightx = gc.weightx;
/*     */ 
/* 276 */     gridBag.prepareAddRowElement(17);
/* 277 */     gc.gridwidth = 2;
/* 278 */     gc.fill = 2;
/* 279 */     gc.weightx = 5.0D;
/* 280 */     this.m_helper.addExchangeComponent(mainPanel, this.m_prnInfFileFld, name);
/*     */ 
/* 282 */     if (isRowEnd)
/*     */     {
/* 284 */       gridBag.prepareAddLastRowElement();
/*     */     }
/*     */     else
/*     */     {
/* 288 */       gridBag.prepareAddRowElement();
/*     */     }
/* 290 */     gc.fill = 0;
/* 291 */     gc.weightx = 0.5D;
/* 292 */     this.m_helper.addComponent(mainPanel, this.m_prnInvBrowseBtn);
/* 293 */     gc.weightx = oldweightx;
/* 294 */     gc.fill = oldfill;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 306 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */ 
/* 308 */     String name = exchange.m_compName;
/* 309 */     String val = exchange.m_compValue;
/* 310 */     if (updateComponent)
/*     */     {
/* 312 */       if (val == null)
/*     */       {
/* 314 */         val = "";
/*     */       }
/* 316 */       if (name.equals("PostscriptPrinterName"))
/*     */       {
/* 318 */         if (val.length() == 0)
/*     */         {
/* 320 */           exchange.m_compValue = "IDC PDF Converter";
/*     */         }
/* 322 */         initPrinterHelper(exchange.m_compValue);
/*     */       }
/* 324 */       else if ((name.equals("PrintDriverName")) && (val.length() == 0))
/*     */       {
/* 326 */         if (this.m_useGSDriverData)
/*     */         {
/* 328 */           exchange.m_compValue = "Ghostscript PDF";
/*     */         }
/*     */         else
/*     */         {
/* 332 */           exchange.m_compValue = "Microsoft PS Class Driver";
/*     */         }
/*     */       }
/* 335 */       else if (name.equals("PrinterPortPath"))
/*     */       {
/* 337 */         if (val.length() == 0)
/*     */         {
/* 340 */           exchange.m_compValue = "c:/temp/idcoutput.ps";
/*     */         }
/* 342 */         exchange.m_compValue = FileUtils.fileSlashes(exchange.m_compValue);
/*     */       } else {
/* 344 */         if (!name.equals("PrinterInfPath"))
/*     */           return;
/* 346 */         if (val.length() == 0)
/*     */         {
/* 348 */           if (this.m_useGSDriverData)
/*     */           {
/* 351 */             String dir = FileUtils.fileSlashes("ghostpdf.inf");
/* 352 */             exchange.m_compValue = dir;
/*     */           }
/*     */           else
/*     */           {
/* 356 */             exchange.m_compValue = this.m_printerHelper.win8SignedMicrosoftPSDriverPath();
/*     */           }
/*     */         }
/* 359 */         exchange.m_compValue = FileUtils.fileSlashes(exchange.m_compValue);
/*     */       }
/*     */ 
/*     */     }
/* 364 */     else if (this.m_didInstallPrinter == true)
/*     */     {
/* 366 */       if ((!name.equals("PrinterInfPath")) && (!name.equals("PrinterPortPath")))
/*     */         return;
/* 368 */       exchange.m_compValue = FileUtils.fileSlashes(exchange.m_compValue);
/*     */     }
/*     */     else
/*     */     {
/* 373 */       this.m_helper.m_props.remove(name);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected IdcMessage validateValues(String name, String val)
/*     */   {
/* 380 */     IdcMessage errMsg = null;
/*     */ 
/* 382 */     if ((name.equals("PrinterInfPath")) && (val.length() != 0))
/*     */     {
/* 384 */       errMsg = validatePath(val, 1);
/*     */     }
/* 386 */     else if ((name.equals("PrinterPortPath")) && (val.length() != 0))
/*     */     {
/* 389 */       String portDir = FileUtils.getDirectory(val);
/*     */       try
/*     */       {
/* 392 */         FileUtils.checkOrCreateDirectory(portDir, 99);
/*     */       }
/*     */       catch (ServiceException badDir)
/*     */       {
/* 396 */         return badDir.getIdcMessage();
/*     */       }
/* 398 */       errMsg = validatePath(portDir, 2);
/*     */ 
/* 401 */       String ext = FileUtils.getExtension(val);
/* 402 */       if (!ext.equalsIgnoreCase("ps"))
/*     */       {
/* 404 */         errMsg = IdcMessageFactory.lc("csPrinterPanelPortMustBePSFile", new Object[0]);
/*     */       }
/*     */     }
/* 407 */     else if (name.equals("PostscriptPrinterName"))
/*     */     {
/* 409 */       if (val.length() == 0)
/*     */       {
/* 411 */         errMsg = IdcMessageFactory.lc("csPrinterPanelPrinterNameRequiredErr", new Object[0]);
/*     */       }
/*     */     }
/* 414 */     else if ((name.equals("PrintDriverName")) && 
/* 416 */       (val.length() == 0))
/*     */     {
/* 418 */       errMsg = IdcMessageFactory.lc("csPrinterPanelPrinterDriverNameRequiredErr", new Object[0]);
/*     */     }
/*     */ 
/* 421 */     return errMsg;
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 427 */     String name = exchange.m_compName;
/* 428 */     String val = exchange.m_compValue;
/*     */ 
/* 430 */     if (this.m_didInstallPrinter == true)
/*     */     {
/* 432 */       IdcMessage errMsg = validateValues(name, val);
/* 433 */       if (errMsg != null)
/*     */       {
/* 435 */         exchange.m_errorMessage = errMsg;
/* 436 */         return false;
/*     */       }
/*     */     }
/* 439 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 444 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 103830 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.systemproperties.ibr.PrinterPanel
 * JD-Core Version:    0.5.4
 */