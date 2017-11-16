/*     */ package intradoc.apputilities.componentwizard;
/*     */ 
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomCheckbox;
/*     */ import intradoc.gui.CustomTextArea;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.IdcFileChooser;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.GridBagConstraints;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.File;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JFileChooser;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JTextArea;
/*     */ import javax.swing.JTextField;
/*     */ 
/*     */ public class EditTemplatePanel extends EditBasePanel
/*     */ {
/*  63 */   protected JCheckBox m_isCopyBox = null;
/*  64 */   protected JButton m_viewBtn = null;
/*  65 */   protected JButton m_browseBtn = null;
/*  66 */   protected JTextField m_copyPathField = null;
/*     */ 
/*     */   public void initUI(int editType)
/*     */   {
/*     */     try
/*     */     {
/*  78 */       GridBagHelper gbh = this.m_helper.m_gridHelper;
/*  79 */       boolean addShowAll = false;
/*  80 */       boolean addSortList = false;
/*  81 */       ItemListener showAllListener = null;
/*  82 */       if (this.m_extraInfo.equals("IntradocTemplates"))
/*     */       {
/*  84 */         addShowAll = true;
/*  85 */         addSortList = true;
/*  86 */         showAllListener = new ItemListener()
/*     */         {
/*     */           public void itemStateChanged(ItemEvent e)
/*     */           {
/*     */             try
/*     */             {
/*  92 */               boolean isAll = EditTemplatePanel.this.m_showAll.isSelected();
/*  93 */               EditTemplatePanel.this.initSelectListData(isAll);
/*  94 */               EditTemplatePanel.this.refreshSelectList(null);
/*     */             }
/*     */             catch (Exception excp)
/*     */             {
/*  98 */               CWizardGuiUtils.reportError(EditTemplatePanel.this.m_systemInterface, excp, (IdcMessage)null);
/*     */             }
/*     */           }
/*     */         };
/*     */       }
/*     */ 
/* 104 */       JPanel panel = new PanePanel();
/* 105 */       this.m_helper.makePanelGridBag(panel, 1);
/*     */ 
/* 107 */       if ((editType == 0) || (editType == 1))
/*     */       {
/* 109 */         addNewOrUseExistingPanel(panel, "name", addShowAll, addSortList, showAllListener, false, null);
/*     */ 
/* 111 */         addInfoPanel(panel);
/*     */       }
/*     */ 
/* 114 */       addCommonPanel(panel);
/*     */ 
/* 116 */       gbh.prepareAddLastRowElement();
/* 117 */       this.m_helper.addComponent(this, panel);
/*     */ 
/* 119 */       if (this.m_resWizard != null)
/*     */       {
/* 121 */         gbh.m_gc.weighty = 1.0D;
/* 122 */         this.m_helper.m_gridHelper.addEmptyRow(this);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 127 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addInfoPanel(JPanel panel) throws ServiceException
/*     */   {
/* 133 */     this.m_copyPathField = new JTextField(20);
/* 134 */     this.m_isCopyBox = new CustomCheckbox(LocaleResources.getString("csCompWizLabelCopyFrom", null));
/*     */ 
/* 136 */     this.m_helper.m_gridHelper.prepareAddRowElement();
/* 137 */     this.m_helper.addExchangeComponent(panel, this.m_isCopyBox, "isCopy");
/*     */ 
/* 139 */     addFilePath(panel);
/*     */ 
/* 141 */     this.m_viewBtn = new JButton(LocaleResources.getString("csCompWizCommandView", null));
/* 142 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 143 */     this.m_helper.addComponent(panel, this.m_viewBtn);
/*     */ 
/* 145 */     ActionListener viewListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/*     */         try
/*     */         {
/* 151 */           String path = EditTemplatePanel.this.m_helper.m_props.getProperty("copyPath");
/* 152 */           if ((path == null) || (path.length() == 0))
/*     */           {
/* 154 */             throw new ServiceException("!csCompWizFileNotSpecified");
/*     */           }
/* 156 */           EditTemplatePanel.this.viewTemplate(path);
/*     */         }
/*     */         catch (Exception excp)
/*     */         {
/* 160 */           CWizardGuiUtils.reportError(EditTemplatePanel.this.m_systemInterface, excp, (IdcMessage)null);
/*     */         }
/*     */       }
/*     */     };
/* 164 */     this.m_viewBtn.addActionListener(viewListener);
/*     */ 
/* 166 */     ItemListener copylistener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 170 */         boolean flag = false;
/* 171 */         if (EditTemplatePanel.this.m_isCopyBox.isSelected())
/*     */         {
/* 173 */           flag = true;
/*     */         }
/*     */ 
/* 176 */         EditTemplatePanel.this.enabledDisabledCopyComponents(flag);
/*     */       }
/*     */     };
/* 179 */     this.m_isCopyBox.addItemListener(copylistener);
/*     */ 
/* 181 */     this.m_isCopyBox.setEnabled(true);
/* 182 */     enabledDisabledCopyComponents(false);
/*     */   }
/*     */ 
/*     */   public void addFilePath(JPanel mainPanel)
/*     */   {
/* 187 */     this.m_browseBtn = new JButton(LocaleResources.getString("csCompWizCommandBrowse", null));
/* 188 */     ActionListener bListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 192 */         JFileChooser fileDlg = new IdcFileChooser();
/* 193 */         fileDlg.setDialogTitle(LocaleResources.getString("csCompWizLabelCopyPath", null));
/* 194 */         fileDlg.showOpenDialog(null);
/*     */ 
/* 197 */         File f = fileDlg.getSelectedFile();
/* 198 */         if (f == null)
/*     */           return;
/* 200 */         EditTemplatePanel.this.m_copyPathField.setText(f.getAbsolutePath());
/*     */       }
/*     */     };
/* 205 */     this.m_browseBtn.addActionListener(bListener);
/*     */ 
/* 207 */     GridBagHelper gridBag = this.m_helper.m_gridHelper;
/* 208 */     GridBagConstraints gc = gridBag.m_gc;
/* 209 */     int oldfill = gc.fill;
/* 210 */     double oldweightx = gc.weightx;
/*     */ 
/* 212 */     gridBag.prepareAddRowElement(17);
/* 213 */     gc.gridwidth = 2;
/* 214 */     gc.fill = 2;
/* 215 */     gc.weightx = 5.0D;
/* 216 */     this.m_helper.addExchangeComponent(mainPanel, this.m_copyPathField, "copyPath");
/*     */ 
/* 218 */     gridBag.prepareAddRowElement();
/* 219 */     gc.fill = 0;
/* 220 */     gc.weightx = 0.5D;
/* 221 */     this.m_helper.addComponent(mainPanel, this.m_browseBtn);
/* 222 */     gc.weightx = oldweightx;
/* 223 */     gc.fill = oldfill;
/*     */   }
/*     */ 
/*     */   protected void addCommonPanel(JPanel panel) throws ServiceException, DataException
/*     */   {
/* 228 */     if (this.m_extraInfo.equals("IntradocTemplates"))
/*     */     {
/* 230 */       addChoiceList(panel, this.m_extraInfo, "class", LocaleResources.getString("csCompWizLabelClass2", null), "class");
/*     */     }
/*     */ 
/* 234 */     if (this.m_extraInfo.equals("IntradocReports"))
/*     */     {
/* 236 */       addChoiceList(panel, this.m_extraInfo, "datasource", LocaleResources.getString("csCompWizLabelDataSource", null), "datasource");
/*     */     }
/*     */     else
/*     */     {
/* 241 */       addChoiceList(panel, this.m_extraInfo, "formtype", LocaleResources.getString("csCompWizLabelFormType2", null), "formtype");
/*     */     }
/*     */ 
/* 245 */     this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelFileName2", null), 40, "resfilename");
/*     */ 
/* 248 */     if (this.m_extraInfo.equals("SearchResultTemplates"))
/*     */     {
/* 250 */       this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelOutFilename", null), 40, "outfilename");
/*     */ 
/* 252 */       this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelFlexData2", null), 40, "flexdata");
/*     */     }
/*     */ 
/* 256 */     this.m_helper.addLabelEditPair(panel, LocaleResources.getString("csCompWizLabelDesc2", null), 40, "description");
/*     */   }
/*     */ 
/*     */   protected boolean onSelect()
/*     */   {
/*     */     try
/*     */     {
/* 265 */       if (this.m_selectList.getSelectedIndex() < 0)
/*     */       {
/* 267 */         throw new ServiceException("!csCompWizChooseItem");
/*     */       }
/* 269 */       String item = this.m_selectList.getSelectedObj();
/* 270 */       this.m_helper.m_props.put("name", item);
/* 271 */       loadTemplateData(item);
/* 272 */       this.m_helper.loadComponentValues();
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 276 */       CWizardGuiUtils.reportError(this.m_systemInterface, exp, (IdcMessage)null);
/* 277 */       return false;
/*     */     }
/* 279 */     return true;
/*     */   }
/*     */ 
/*     */   protected void initSelectListData(boolean isAll)
/*     */     throws DataException, ServiceException
/*     */   {
/* 285 */     String tablename = this.m_extraInfo;
/* 286 */     if (isAll)
/*     */     {
/* 288 */       tablename = this.m_extraInfo;
/*     */     }
/* 290 */     else if (this.m_extraInfo.equalsIgnoreCase("IntradocTemplates"))
/*     */     {
/* 292 */       tablename = this.m_extraInfo + ".common";
/*     */     }
/*     */ 
/* 295 */     DataResultSet drset = SharedObjects.getTable(tablename);
/*     */ 
/* 297 */     if (drset == null)
/*     */     {
/* 299 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, tablename));
/*     */     }
/*     */ 
/* 302 */     if ((isAll) || (!this.m_extraInfo.equalsIgnoreCase("IntradocTemplates")))
/*     */     {
/* 304 */       this.m_selectListData = new DataResultSet(new String[] { "name", "description" });
/* 305 */       for (drset.first(); ; drset.next()) { if (!drset.isRowPresent())
/*     */           return;
/* 307 */         String name = ResultSetUtils.getValue(drset, "name");
/* 308 */         String desc = ResultSetUtils.getValue(drset, "description");
/* 309 */         Vector v = this.m_selectListData.createEmptyRow();
/* 310 */         v.setElementAt(name, 0);
/* 311 */         v.setElementAt(desc, 1);
/* 312 */         this.m_selectListData.addRow(v); }
/*     */ 
/*     */ 
/*     */     }
/*     */ 
/* 317 */     FieldInfo finfo = new FieldInfo();
/* 318 */     drset.getFieldInfo("description", finfo);
/*     */ 
/* 321 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 323 */       Vector v = drset.getCurrentRowValues();
/* 324 */       String desc = (String)v.elementAt(finfo.m_index);
/* 325 */       int n = drset.getCurrentRow();
/*     */ 
/* 327 */       v.setElementAt(LocaleResources.getString(desc, null), finfo.m_index);
/* 328 */       drset.setRowValues(v, n);
/*     */     }
/* 330 */     this.m_selectListData = drset;
/*     */   }
/*     */ 
/*     */   protected void loadTemplateData(String item)
/*     */     throws DataException, ServiceException
/*     */   {
/* 336 */     String[] fields = null;
/* 337 */     DataResultSet drset = SharedObjects.getTable(this.m_extraInfo);
/*     */ 
/* 339 */     if (drset == null)
/*     */     {
/* 341 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, this.m_extraInfo));
/*     */     }
/*     */ 
/* 344 */     if (this.m_extraInfo.equals("IntradocTemplates"))
/*     */     {
/* 346 */       fields = new String[] { "name", "class", "formtype", "filename", "description" };
/*     */     }
/* 348 */     else if (this.m_extraInfo.equals("SearchResultTemplates"))
/*     */     {
/* 350 */       fields = new String[] { "name", "formtype", "filename", "outfilename", "flexdata", "description" };
/*     */     }
/*     */     else
/*     */     {
/* 355 */       fields = new String[] { "name", "datasource", "filename", "description" };
/*     */     }
/*     */ 
/* 358 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, fields, true);
/* 359 */     Vector v = drset.findRow(info[0].m_index, item);
/*     */ 
/* 361 */     for (int i = 1; i < info.length; ++i)
/*     */     {
/* 363 */       boolean isChoice = false;
/* 364 */       String name = info[i].m_name;
/* 365 */       String val = (String)v.elementAt(info[i].m_index);
/* 366 */       if ((name.equals("class")) || (name.equals("formtype")) || (name.equals("dataSource")))
/*     */       {
/* 368 */         isChoice = true;
/*     */       }
/*     */ 
/* 371 */       if (name.equals("filename"))
/*     */       {
/* 373 */         name = "resfilename";
/*     */ 
/* 375 */         String filename = FileUtils.getName(val);
/* 376 */         val = CWizardUtils.removeSpaces(this.m_component.m_name) + "_" + filename;
/*     */       }
/* 378 */       setComponentDefault(name, val, isChoice);
/*     */     }
/*     */ 
/* 382 */     this.m_helper.m_props.put("isCopy", "true");
/* 383 */     enabledDisabledCopyComponents(true);
/*     */ 
/* 385 */     setComponentDefault("copyPath", findTemplateFile(item), false);
/*     */   }
/*     */ 
/*     */   protected void enabledDisabledCopyComponents(boolean flag)
/*     */   {
/* 390 */     this.m_viewBtn.setEnabled(flag);
/* 391 */     this.m_browseBtn.setEnabled(flag);
/* 392 */     this.m_copyPathField.setEnabled(flag);
/*     */   }
/*     */ 
/*     */   protected void setComponentDefault(String name, String val, boolean isChoice)
/*     */   {
/* 397 */     if (val == null)
/*     */     {
/* 399 */       return;
/*     */     }
/*     */ 
/* 402 */     if (isChoice)
/*     */     {
/* 404 */       this.m_helper.m_props.put(name + "choice", val);
/*     */     }
/*     */ 
/* 407 */     this.m_helper.m_props.put(name, val);
/*     */   }
/*     */ 
/*     */   protected void viewTemplate(String filename)
/*     */   {
/*     */     try
/*     */     {
/* 414 */       this.m_dlgHelper = new DialogHelper(this.m_systemInterface, filename, true);
/* 415 */       JPanel mainPanel = this.m_dlgHelper.m_mainPanel;
/* 416 */       this.m_dlgHelper.makePanelGridBag(mainPanel, 1);
/* 417 */       this.m_dlgHelper.addOK(null);
/*     */ 
/* 419 */       BufferedReader br = FileUtils.openDataReader(new File(filename));
/* 420 */       StringBuffer buffer = new StringBuffer();
/* 421 */       String line = null;
/*     */ 
/* 424 */       while ((line = br.readLine()) != null)
/*     */       {
/* 426 */         buffer.append(line + "\n");
/*     */       }
/*     */ 
/* 429 */       br.close();
/*     */ 
/* 431 */       JTextArea ta = new CustomTextArea(buffer.toString(), 20, 80);
/* 432 */       ta.setEditable(false);
/* 433 */       this.m_dlgHelper.addLastComponentInRow(mainPanel, ta);
/* 434 */       this.m_dlgHelper.prompt();
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 438 */       CWizardGuiUtils.reportError(this.m_systemInterface, e, (IdcMessage)null);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String findTemplateFile(String name) throws ServiceException, DataException
/*     */   {
/* 444 */     DataResultSet drset = SharedObjects.getTable(this.m_extraInfo);
/*     */ 
/* 446 */     if (drset == null)
/*     */     {
/* 449 */       throw new ServiceException(LocaleUtils.encodeMessage("csTableNotLoaded", null, this.m_extraInfo));
/*     */     }
/*     */ 
/* 452 */     FieldInfo[] info = ResultSetUtils.createInfoList(drset, new String[] { "name", "filename" }, true);
/*     */ 
/* 454 */     Vector v = drset.findRow(info[0].m_index, name);
/*     */ 
/* 456 */     if (v == null)
/*     */     {
/* 458 */       return null;
/*     */     }
/* 460 */     String filename = (String)v.elementAt(info[1].m_index);
/* 461 */     filename = FileUtils.getAbsolutePath(this.m_component.m_absCompDir, filename);
/*     */ 
/* 463 */     if ((filename == null) || (filename.length() == 0))
/*     */     {
/* 465 */       throw new ServiceException(LocaleUtils.encodeMessage("csCompWizTemplateFileNotFound", null, name));
/*     */     }
/*     */ 
/* 468 */     return filename;
/*     */   }
/*     */ 
/*     */   public void exchangeComponentValue(DynamicComponentExchange exchange, boolean updateComponent)
/*     */   {
/* 477 */     String name = exchange.m_compName;
/* 478 */     String val = exchange.m_compValue;
/* 479 */     if (updateComponent)
/*     */     {
/* 481 */       if (((name.equals("outfilename")) && (this.m_extraInfo.equals("SearchResultTemplates"))) || (name.equals("description")))
/*     */       {
/* 484 */         boolean removeNullStr = false;
/* 485 */         if ((val == null) || (val.length() == 0))
/*     */         {
/* 487 */           String tempVal = this.m_helper.m_props.getProperty(name);
/* 488 */           if ((tempVal != null) && (tempVal.equalsIgnoreCase("null")))
/*     */           {
/* 490 */             removeNullStr = true;
/*     */           }
/*     */         }
/* 493 */         else if (val.equalsIgnoreCase("null"))
/*     */         {
/* 495 */           removeNullStr = true;
/*     */         }
/*     */ 
/* 498 */         if (removeNullStr)
/*     */         {
/* 500 */           this.m_helper.m_props.put(name, "");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/* 506 */     else if (((name.equals("outfilename")) && (this.m_extraInfo.equals("SearchResultTemplates"))) || ((name.equals("description")) && (((val == null) || (val.length() == 0)))))
/*     */     {
/* 509 */       exchange.m_compValue = "null";
/*     */     }
/*     */ 
/* 513 */     super.exchangeComponentValue(exchange, updateComponent);
/*     */   }
/*     */ 
/*     */   public boolean validateComponentValue(DynamicComponentExchange exchange)
/*     */   {
/* 519 */     String name = exchange.m_compName;
/* 520 */     String val = exchange.m_compValue;
/*     */ 
/* 522 */     IdcMessage errMsg = null;
/*     */ 
/* 524 */     if (name.equals("name"))
/*     */     {
/* 526 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelTemplateName", new Object[0]), false, false);
/*     */     }
/* 528 */     else if (name.equals("resfilename"))
/*     */     {
/* 530 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelFileName", new Object[0]), false, true);
/*     */     }
/* 532 */     else if (name.equals("formtype"))
/*     */     {
/* 534 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelFormType", new Object[0]), false, false);
/*     */     }
/* 536 */     else if ((name.equals("class")) && (this.m_extraInfo.equals("IntradocTemplates")))
/*     */     {
/* 538 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelClass", new Object[0]), false, false);
/*     */     }
/* 540 */     else if ((name.equals("outfilename")) && (this.m_extraInfo.equals("SearchTemplates")))
/*     */     {
/* 542 */       if ((val != null) && (val.length() > 0))
/*     */       {
/* 544 */         errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelOutFilename", new Object[0]), false, true);
/*     */       }
/*     */     }
/* 547 */     else if ((name.equals("flexdata")) && (this.m_extraInfo.equals("SearchTemplates")))
/*     */     {
/* 549 */       errMsg = checkField(val, IdcMessageFactory.lc("csCompWizLabelFlexData", new Object[0]), true, true);
/*     */     }
/*     */     else
/*     */     {
/* 553 */       return super.validateComponentValue(exchange);
/*     */     }
/*     */ 
/* 556 */     if (errMsg != null)
/*     */     {
/* 558 */       exchange.m_errorMessage = errMsg;
/* 559 */       return false;
/*     */     }
/* 561 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 566 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 78493 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apputilities.componentwizard.EditTemplatePanel
 * JD-Core Version:    0.5.4
 */