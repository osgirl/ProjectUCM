/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.apps.shared.AppLauncher;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DialogHelper;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.DataResultSetTableModel;
/*     */ import intradoc.gui.iwt.IdcTable;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocFormatsDlg
/*     */   implements ActionListener, DisplayStringCallback
/*     */ {
/*  64 */   protected DialogHelper m_helper = null;
/*  65 */   protected SystemInterface m_systemInterface = null;
/*  66 */   protected ExecutionContext m_ctx = null;
/*  67 */   protected String m_helpPage = null;
/*     */   protected UdlPanel m_extensionList;
/*     */   protected UdlPanel m_formatList;
/*     */   protected DataResultSetTableModel m_formatListModel;
/*  73 */   protected Map<String, JButton> m_buttons = new HashMap();
/*     */ 
/*  75 */   protected int m_formatIndex = -1;
/*     */ 
/*     */   public DocFormatsDlg(SystemInterface sys, String title, String helpPage)
/*     */   {
/*  79 */     this.m_systemInterface = sys;
/*  80 */     this.m_ctx = sys.getExecutionContext();
/*  81 */     this.m_helper = new DialogHelper(sys, title, true);
/*  82 */     this.m_helpPage = helpPage;
/*     */   }
/*     */ 
/*     */   public void init() throws ServiceException
/*     */   {
/*  87 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  88 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*     */ 
/*  90 */     JPanel extPanel = initExtensionGUI();
/*  91 */     JPanel formatPanel = initFormatGUI();
/*     */ 
/*  93 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  94 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  95 */     this.m_helper.addLastComponentInRow(mainPanel, formatPanel);
/*  96 */     this.m_helper.addLastComponentInRow(mainPanel, extPanel);
/*     */ 
/*  98 */     refreshLists();
/*     */ 
/* 100 */     this.m_formatListModel = ((DataResultSetTableModel)this.m_formatList.m_list.m_table.getModel());
/*     */ 
/* 104 */     this.m_formatIndex = this.m_formatListModel.m_rset.getFieldInfoIndex("dFormat");
/*     */ 
/* 106 */     this.m_helper.prompt();
/*     */   }
/*     */ 
/*     */   public JPanel initExtensionGUI()
/*     */   {
/* 111 */     String columns = "dExtension,dFormat,dIsEnabled";
/* 112 */     this.m_extensionList = new UdlPanel(LocaleResources.getString("apLabelFileExtensions", this.m_ctx), null, 400, 10, "ExtensionFormatMap", true);
/*     */ 
/* 116 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleExtension", this.m_ctx), "dExtension", 10.0D);
/* 117 */     this.m_extensionList.setColumnInfo(info);
/* 118 */     info = new ColumnInfo(LocaleResources.getString("apTitleMapToFormat", this.m_ctx), "dFormat", 10.0D);
/* 119 */     this.m_extensionList.setColumnInfo(info);
/* 120 */     info = new ColumnInfo(LocaleResources.getString("apLabelEnabled", this.m_ctx), "dIsEnabled", 10.0D);
/* 121 */     this.m_extensionList.setColumnInfo(info);
/*     */ 
/* 123 */     this.m_extensionList.setVisibleColumns(columns);
/* 124 */     this.m_extensionList.setIDColumn("dExtension");
/* 125 */     this.m_extensionList.init();
/* 126 */     this.m_extensionList.setDisplayCallback("dIsEnabled", this);
/*     */ 
/* 128 */     ItemListener itemListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 132 */         DocFormatsDlg.this.selectExtensionFormat();
/*     */       }
/*     */     };
/* 135 */     this.m_extensionList.m_list.addItemListener(itemListener);
/*     */ 
/* 138 */     JPanel btnPanel = new PanePanel();
/* 139 */     btnPanel.setLayout(new FlowLayout());
/*     */ 
/* 141 */     String[][] btnInfo = { { "apDlgButtonAdd", "0", "addExtension", "apReadableAddFileExtension" }, { "apDlgButtonEdit", "1", "editExtension", "apReadableEditFileExtension" }, { "apLabelDelete", "1", "deleteExtension", "apReadableDeleteFileExtension" }, { "apLabelDisableExtension", "1", "toggleExtensionEnabled", "apReadableEnableFileExtension" } };
/*     */ 
/* 148 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 150 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][1], false);
/* 151 */       JButton btn = this.m_extensionList.addButton(LocaleResources.getString(btnInfo[i][0], this.m_ctx), isControlled);
/*     */ 
/* 153 */       btn.setActionCommand(btnInfo[i][2]);
/* 154 */       btn.getAccessibleContext().setAccessibleName(LocaleResources.getString(btnInfo[i][3], this.m_ctx));
/* 155 */       btn.addActionListener(this);
/* 156 */       btnPanel.add(btn);
/* 157 */       this.m_buttons.put(btnInfo[i][2], btn);
/*     */     }
/*     */ 
/* 161 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 165 */         DocFormatsDlg.this.addOrEditExtension(false);
/*     */       }
/*     */     };
/* 168 */     this.m_extensionList.m_list.addActionListener(editListener);
/* 169 */     this.m_extensionList.add("South", btnPanel);
/*     */ 
/* 171 */     JPanel wrapper = new CustomPanel();
/* 172 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 173 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 30, 5, 30);
/* 174 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 175 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 176 */     this.m_helper.addComponent(wrapper, this.m_extensionList);
/*     */ 
/* 178 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected void selectExtensionFormat()
/*     */   {
/* 183 */     this.m_systemInterface.displayStatus(LocaleResources.getString("apLabelReady", this.m_ctx));
/* 184 */     int index = this.m_extensionList.getSelectedIndex();
/* 185 */     this.m_extensionList.enableDisable(index >= 0);
/* 186 */     if (index >= 0)
/*     */     {
/* 188 */       Properties props = this.m_extensionList.getDataAt(index);
/* 189 */       String isEnabled = props.getProperty("dIsEnabled");
/* 190 */       String key = "apLabelEnableExtension";
/* 191 */       String readable = "apReadableEnableFileExtension";
/* 192 */       if (StringUtils.convertToBool(isEnabled, false))
/*     */       {
/* 194 */         key = "apLabelDisableExtension";
/* 195 */         readable = "apReadableDisableFileExtension";
/*     */       }
/* 197 */       JButton enabledBtn = (JButton)this.m_buttons.get("toggleExtensionEnabled");
/* 198 */       enabledBtn.setText(this.m_systemInterface.getString(key));
/* 199 */       enabledBtn.getAccessibleContext().setAccessibleDescription(LocaleResources.getString(readable, this.m_ctx));
/*     */ 
/* 201 */       String overrideStatus = props.getProperty("overrideStatus");
/* 202 */       key = "apLabelRevertExtension";
/* 203 */       if (overrideStatus.length() == 0)
/*     */       {
/* 205 */         key = "apLabelDelete";
/*     */       }
/* 207 */       JButton deleteBtn = (JButton)this.m_buttons.get("deleteExtension");
/* 208 */       deleteBtn.setText(this.m_systemInterface.getString(key));
/*     */ 
/* 210 */       String compName = props.getProperty("idcComponentName");
/* 211 */       if (compName.length() != 0)
/*     */       {
/* 213 */         deleteBtn.setEnabled(false);
/*     */       }
/*     */ 
/* 216 */       String format = props.getProperty("dFormat");
/*     */ 
/* 218 */       int size = this.m_formatListModel.getRowCount();
/* 219 */       for (int i = 0; i < size; ++i)
/*     */       {
/* 221 */         String str = (String)this.m_formatListModel.getValueAt(i, this.m_formatIndex);
/* 222 */         if (!str.equalsIgnoreCase(format))
/*     */           continue;
/* 224 */         this.m_formatList.m_list.select(i);
/* 225 */         return;
/*     */       }
/*     */ 
/* 228 */       this.m_systemInterface.displayStatus(LocaleResources.getString("apExtensionNotMappedToFormat", this.m_ctx, props.getProperty("dExtension")));
/*     */     }
/*     */ 
/* 232 */     index = this.m_formatList.getSelectedIndex();
/* 233 */     if (index < 0)
/*     */       return;
/* 235 */     this.m_formatList.m_list.deselect(index);
/*     */   }
/*     */ 
/*     */   public JPanel initFormatGUI()
/*     */   {
/* 241 */     String columns = "dFormat,dConversion,dDescription,dIsEnabled";
/* 242 */     this.m_formatList = new UdlPanel(LocaleResources.getString("apLabelFileFormats", this.m_ctx), null, 400, 10, "DocFormats", true);
/*     */ 
/* 246 */     ColumnInfo info = new ColumnInfo(LocaleResources.getString("apTitleFormat", this.m_ctx), "dFormat", 10.0D);
/* 247 */     this.m_formatList.setColumnInfo(info);
/* 248 */     info = new ColumnInfo(LocaleResources.getString("apTitleConversion", this.m_ctx), "dConversion", 10.0D);
/* 249 */     this.m_formatList.setColumnInfo(info);
/* 250 */     info = new ColumnInfo(LocaleResources.getString("apTitleDescription", this.m_ctx), "dDescription", 20.0D);
/* 251 */     this.m_formatList.setColumnInfo(info);
/* 252 */     info = new ColumnInfo(LocaleResources.getString("apLabelEnabled", this.m_ctx), "dIsEnabled", 10.0D);
/* 253 */     this.m_formatList.setColumnInfo(info);
/*     */ 
/* 255 */     this.m_formatList.setVisibleColumns(columns);
/* 256 */     this.m_formatList.setIDColumn("dFormat");
/* 257 */     this.m_formatList.init();
/*     */ 
/* 260 */     DisplayStringCallback displayCallback = new DisplayStringCallbackAdaptor()
/*     */     {
/*     */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */       {
/* 266 */         if ((value == null) || (value.length() == 0))
/*     */         {
/* 268 */           return "";
/*     */         }
/*     */ 
/* 271 */         String desc = null;
/* 272 */         if ((name.equals("dDescription")) && 
/* 274 */           (value.length() > 0))
/*     */         {
/* 276 */           desc = LocaleResources.getString(value, DocFormatsDlg.this.m_ctx);
/*     */         }
/*     */ 
/* 280 */         if (desc == null)
/*     */         {
/* 282 */           desc = value;
/*     */         }
/* 284 */         return desc;
/*     */       }
/*     */     };
/* 287 */     this.m_formatList.setDisplayCallback("dDescription", displayCallback);
/* 288 */     this.m_formatList.setDisplayCallback("dIsEnabled", this);
/*     */ 
/* 290 */     ItemListener formatListener = new ItemListener()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 294 */         DocFormatsDlg.this.updateFormatListControls();
/*     */       }
/*     */     };
/* 297 */     this.m_formatList.m_list.addItemListener(formatListener);
/*     */ 
/* 300 */     JPanel btnPanel = new PanePanel();
/* 301 */     btnPanel.setLayout(new FlowLayout());
/*     */ 
/* 303 */     String[][] btnInfo = { { "apDlgButtonAdd", "0", "addFormat", "apReadableAddFileFormat" }, { "apDlgButtonEdit", "1", "editFormat", "apReadableEditFileFormat" }, { "apLabelDelete", "1", "deleteFormat", "apReadableDeleteFileFormat" }, { "apLabelDisableFormat", "1", "toggleFormatEnabled", "apReadableEnableFileFormat" }, { "apLabelClose", "0", "close", "apLabelClose" } };
/*     */ 
/* 312 */     for (int i = 0; i < btnInfo.length; ++i)
/*     */     {
/* 314 */       boolean isControlled = StringUtils.convertToBool(btnInfo[i][1], false);
/* 315 */       JButton btn = this.m_formatList.addButton(LocaleResources.getString(btnInfo[i][0], this.m_ctx), isControlled);
/*     */ 
/* 318 */       String cmd = btnInfo[i][2];
/* 319 */       btn.setActionCommand(cmd);
/* 320 */       this.m_buttons.put(cmd, btn);
/* 321 */       if (cmd.equals("close"))
/*     */       {
/* 323 */         this.m_helper.addCommandButtonEx(btn, this);
/*     */       }
/*     */       else
/*     */       {
/* 327 */         btn.addActionListener(this);
/* 328 */         btnPanel.add(btn);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 333 */     this.m_helper.addHelpInfo(this.m_helpPage);
/*     */ 
/* 336 */     ActionListener editListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 340 */         DocFormatsDlg.this.addOrEditFormat(false);
/*     */       }
/*     */     };
/* 343 */     this.m_formatList.m_list.addActionListener(editListener);
/* 344 */     this.m_formatList.add("South", btnPanel);
/*     */ 
/* 347 */     JPanel wrapper = new CustomPanel();
/* 348 */     this.m_helper.makePanelGridBag(wrapper, 1);
/* 349 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 30, 5, 30);
/* 350 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 351 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 352 */     this.m_helper.addComponent(wrapper, this.m_formatList);
/*     */ 
/* 354 */     return wrapper;
/*     */   }
/*     */ 
/*     */   protected void updateFormatListControls()
/*     */   {
/* 359 */     int index = this.m_formatList.getSelectedIndex();
/* 360 */     boolean isSelected = index >= 0;
/* 361 */     this.m_formatList.enableDisable(isSelected);
/* 362 */     if (!isSelected)
/*     */       return;
/* 364 */     Properties props = this.m_formatList.getDataAt(index);
/* 365 */     String isEnabled = props.getProperty("dIsEnabled");
/* 366 */     String key = "apLabelEnableFormat";
/* 367 */     String readable = "apReadableEnableFileFormat";
/* 368 */     if (StringUtils.convertToBool(isEnabled, false))
/*     */     {
/* 370 */       key = "apLabelDisableFormat";
/* 371 */       readable = "apReadableDisableFileFormat";
/*     */     }
/* 373 */     JButton enabledBtn = (JButton)this.m_buttons.get("toggleFormatEnabled");
/* 374 */     enabledBtn.setText(this.m_systemInterface.getString(key));
/* 375 */     enabledBtn.getAccessibleContext().setAccessibleName(LocaleResources.getString(readable, this.m_ctx));
/*     */ 
/* 377 */     String overrideStatus = props.getProperty("overrideStatus");
/* 378 */     key = "apLabelRevertFormat";
/*     */ 
/* 380 */     if (overrideStatus.length() == 0)
/*     */     {
/* 382 */       key = "apLabelDelete";
/*     */     }
/* 384 */     JButton deleteBtn = (JButton)this.m_buttons.get("deleteFormat");
/* 385 */     deleteBtn.setText(this.m_systemInterface.getString(key));
/*     */ 
/* 387 */     String compName = props.getProperty("idcComponentName");
/* 388 */     deleteBtn.setEnabled(compName.length() == 0);
/*     */   }
/*     */ 
/*     */   protected void refreshLists()
/*     */     throws ServiceException
/*     */   {
/* 394 */     refreshExtensionList();
/* 395 */     refreshFormatList();
/*     */   }
/*     */ 
/*     */   protected void refreshExtensionList() throws ServiceException
/*     */   {
/* 400 */     refreshExtensionList(null, "");
/*     */   }
/*     */ 
/*     */   protected void refreshExtensionList(DataBinder binder, String selectedObj)
/*     */     throws ServiceException
/*     */   {
/* 406 */     refreshExtensionList(binder, new String[] { selectedObj });
/*     */   }
/*     */ 
/*     */   protected void refreshExtensionList(DataBinder binder, String[] selectedObjs)
/*     */     throws ServiceException
/*     */   {
/* 412 */     if (binder == null)
/*     */     {
/* 415 */       binder = new DataBinder();
/*     */ 
/* 421 */       AppLauncher.executeService("GET_DOCEXTENSIONS", binder);
/*     */     }
/*     */ 
/* 424 */     Vector indices = this.m_extensionList.refreshListEx(binder, selectedObjs);
/* 425 */     boolean isSelected = indices.size() > 0;
/* 426 */     this.m_extensionList.enableDisable(isSelected);
/* 427 */     selectExtensionFormat();
/*     */   }
/*     */ 
/*     */   protected void refreshFormatList() throws ServiceException
/*     */   {
/* 432 */     refreshFormatList(null, "");
/*     */   }
/*     */ 
/*     */   protected void refreshFormatList(DataBinder binder, String selectedObj)
/*     */     throws ServiceException
/*     */   {
/* 438 */     refreshFormatList(binder, new String[] { selectedObj });
/*     */   }
/*     */ 
/*     */   protected void refreshFormatList(DataBinder binder, String[] selectedObjs)
/*     */     throws ServiceException
/*     */   {
/* 444 */     if (binder == null)
/*     */     {
/* 447 */       binder = new DataBinder();
/* 448 */       AppLauncher.executeService("GET_DOCFORMATS", binder);
/*     */     }
/*     */ 
/* 451 */     Vector indices = this.m_formatList.refreshListEx(binder, selectedObjs);
/* 452 */     boolean isSelected = indices.size() > 0;
/* 453 */     this.m_formatList.enableDisable(isSelected);
/* 454 */     updateFormatListControls();
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 460 */     String selectedExt = this.m_extensionList.getSelectedObj();
/* 461 */     String selectedFormat = this.m_formatList.getSelectedObj();
/*     */ 
/* 463 */     refreshExtensionList(null, selectedExt);
/* 464 */     refreshFormatList(null, selectedFormat);
/*     */   }
/*     */ 
/*     */   protected void reportError(IdcMessage msg)
/*     */   {
/* 469 */     MessageBox.reportError(this.m_systemInterface, msg);
/*     */   }
/*     */ 
/*     */   protected void reportError(Exception e)
/*     */   {
/* 474 */     MessageBox.reportError(this.m_systemInterface, e);
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 482 */     String cmd = e.getActionCommand();
/* 483 */     if (cmd.equals("addExtension"))
/*     */     {
/* 485 */       addOrEditExtension(true);
/*     */     }
/* 487 */     else if (cmd.equals("editExtension"))
/*     */     {
/* 489 */       addOrEditExtension(false);
/*     */     }
/* 491 */     else if (cmd.equals("deleteExtension"))
/*     */     {
/* 493 */       deleteExtension();
/*     */     }
/* 495 */     else if (cmd.equals("addFormat"))
/*     */     {
/* 497 */       addOrEditFormat(true);
/*     */     }
/* 499 */     else if (cmd.equals("editFormat"))
/*     */     {
/* 501 */       addOrEditFormat(false);
/*     */     }
/* 503 */     else if (cmd.equals("deleteFormat"))
/*     */     {
/* 505 */       deleteFormat();
/*     */     }
/* 507 */     else if (cmd.equals("toggleExtensionEnabled"))
/*     */     {
/* 509 */       toggleEnabled(this.m_extensionList, "DOCEXTENSION", "dExtension");
/*     */     }
/* 511 */     else if (cmd.equals("toggleFormatEnabled"))
/*     */     {
/* 513 */       toggleEnabled(this.m_formatList, "DOCFORMAT", "dFormat");
/*     */     } else {
/* 515 */       if (!cmd.equals("close"))
/*     */         return;
/* 517 */       this.m_helper.close();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addOrEditExtension(boolean isAdd)
/*     */   {
/* 523 */     Properties props = null;
/* 524 */     String title = LocaleResources.getString("apAddFileExtensions", this.m_ctx);
/* 525 */     String helpPageName = "AddExtensions";
/*     */ 
/* 527 */     if (!isAdd)
/*     */     {
/* 529 */       int index = this.m_extensionList.getSelectedIndex();
/* 530 */       if (index < 0)
/*     */       {
/* 532 */         reportError(IdcMessageFactory.lc("apSelectExtensionToEdit", new Object[0]));
/* 533 */         return;
/*     */       }
/* 535 */       props = this.m_extensionList.getDataAt(index);
/* 536 */       title = LocaleResources.getString("apEditFileExtension", this.m_ctx, props.getProperty("dExtension"));
/* 537 */       helpPageName = "EditExtensions";
/*     */     }
/*     */ 
/* 540 */     EditExtensionsDlg dlg = new EditExtensionsDlg(this.m_systemInterface, title, this.m_extensionList.getResultSet(), DialogHelpTable.getHelpPage(helpPageName));
/*     */ 
/* 542 */     dlg.init(props, this.m_formatListModel.m_rset, this.m_formatIndex);
/* 543 */     if (dlg.prompt() != 1) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 548 */       refreshExtensionList(dlg.getBinder(), dlg.getExtension());
/* 549 */       selectExtensionFormat();
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 553 */       reportError(exp.getIdcMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteExtension()
/*     */   {
/* 560 */     int index = this.m_extensionList.getSelectedIndex();
/* 561 */     if (index < 0)
/*     */     {
/* 564 */       reportError(IdcMessageFactory.lc("apSelectExtensionToDelete", new Object[0]));
/* 565 */       return;
/*     */     }
/*     */ 
/* 568 */     Properties props = this.m_extensionList.getDataAt(index);
/* 569 */     String name = props.getProperty("dExtension");
/*     */ 
/* 571 */     String key = "apVerifyExtensionDelete";
/* 572 */     String overrideStatus = props.getProperty("overrideStatus");
/* 573 */     if (overrideStatus.length() > 0)
/*     */     {
/* 575 */       key = "apVerifyExtensionRevert";
/*     */     }
/* 577 */     IdcMessage msg = IdcMessageFactory.lc(key, new Object[] { name });
/* 578 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) == 2)
/*     */     {
/* 581 */       DataBinder binder = new DataBinder();
/*     */       try
/*     */       {
/* 584 */         binder.setLocalData(props);
/* 585 */         AppLauncher.executeService("DELETE_DOCEXTENSION", binder);
/* 586 */         refreshExtensionList(binder, "");
/* 587 */         this.m_extensionList.enableDisable(false);
/*     */       }
/*     */       catch (Exception exp)
/*     */       {
/* 591 */         reportError(exp);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 596 */         refreshExtensionList(binder, name);
/*     */       }
/*     */       catch (ServiceException exp)
/*     */       {
/* 600 */         reportError(exp);
/*     */       }
/*     */     }
/* 603 */     this.m_systemInterface.displayStatus(LocaleResources.getString("apLabelReady", this.m_ctx));
/*     */   }
/*     */ 
/*     */   public void toggleEnabled(UdlPanel list, String serviceSuffix, String primaryKeyColumn)
/*     */   {
/* 608 */     int[] indexes = list.getSelectedIndexes();
/* 609 */     if (indexes == null)
/*     */     {
/* 611 */       return;
/*     */     }
/* 613 */     String[] selectedItems = new String[indexes.length];
/* 614 */     DataBinder binder = null;
/* 615 */     for (int i = 0; i < indexes.length; ++i)
/*     */     {
/* 617 */       int index = indexes[i];
/* 618 */       Properties props = list.getDataAt(index);
/* 619 */       selectedItems[i] = props.getProperty(primaryKeyColumn);
/* 620 */       binder = new DataBinder();
/* 621 */       binder.setLocalData(props);
/* 622 */       String service = "EDIT_" + serviceSuffix;
/* 623 */       String action = "enable";
/* 624 */       if (StringUtils.convertToBool(props.getProperty("dIsEnabled"), false))
/*     */       {
/* 626 */         action = "disable";
/* 627 */         binder.putLocal("dIsEnabled", "0");
/*     */       }
/*     */       else
/*     */       {
/* 631 */         binder.putLocal("dIsEnabled", "1");
/*     */       }
/* 633 */       String overrideStatus = props.getProperty("overrideStatus");
/* 634 */       String compName = props.getProperty("idcComponentName");
/* 635 */       boolean isPartial = overrideStatus.equals("partial");
/* 636 */       if ((isPartial) || ((overrideStatus.equals("")) && (compName.length() > 0)))
/*     */       {
/* 644 */         if (!isPartial)
/*     */         {
/* 646 */           service = "ADD_" + serviceSuffix;
/*     */         }
/* 648 */         props.put("dConversion", "override");
/*     */       }
/* 650 */       this.m_systemInterface.displayStatus(IdcMessageFactory.lc("apExtensionProcessingProgress_" + action, new Object[] { Integer.valueOf(i + 1), Integer.valueOf(indexes.length) }));
/*     */       try
/*     */       {
/* 654 */         AppLauncher.executeService(service, binder);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 659 */         reportError(e.getIdcMessage());
/* 660 */         this.m_systemInterface.displayStatus(IdcMessageFactory.lc("apLabelReady", new Object[0]));
/*     */ 
/* 662 */         return;
/*     */       }
/*     */     }
/*     */     try
/*     */     {
/* 667 */       if (list == this.m_extensionList)
/*     */       {
/* 669 */         refreshExtensionList(binder, selectedItems);
/*     */       }
/* 671 */       else if (list == this.m_formatList)
/*     */       {
/* 673 */         refreshFormatList(binder, selectedItems);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 678 */       reportError(e);
/*     */     }
/* 680 */     this.m_systemInterface.displayStatus(IdcMessageFactory.lc("apLabelReady", new Object[0]));
/*     */   }
/*     */ 
/*     */   public void addOrEditFormat(boolean isAdd)
/*     */   {
/* 685 */     Properties props = null;
/* 686 */     String title = this.m_systemInterface.getString("apAddNewFileFormats");
/* 687 */     String helpPageName = "AddFormats";
/*     */ 
/* 689 */     if (!isAdd)
/*     */     {
/* 691 */       int index = this.m_formatList.getSelectedIndex();
/* 692 */       if (index < 0)
/*     */       {
/* 694 */         reportError(IdcMessageFactory.lc("apSelectFormatToEdit", new Object[0]));
/* 695 */         return;
/*     */       }
/* 697 */       props = this.m_formatList.getDataAt(index);
/* 698 */       title = LocaleResources.getString("apEditFileFormat", this.m_ctx, props.getProperty("dFormat"));
/* 699 */       helpPageName = "EditFormats";
/*     */     }
/*     */ 
/* 702 */     if (props != null)
/*     */     {
/* 704 */       props.put("isAdd", (isAdd) ? "1" : "0");
/*     */     }
/*     */ 
/* 707 */     EditFormatsDlg dlg = new EditFormatsDlg(this.m_systemInterface, title, this.m_formatList.getResultSet(), DialogHelpTable.getHelpPage(helpPageName));
/*     */     try
/*     */     {
/* 712 */       dlg.init(props);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 716 */       MessageBox.reportError(this.m_systemInterface, e, IdcMessageFactory.lc("apUnableToOpenDialog", new Object[0]));
/*     */     }
/*     */ 
/* 719 */     if (dlg.prompt() != 1) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 724 */       refreshFormatList(dlg.getBinder(), dlg.getFormat());
/*     */     }
/*     */     catch (ServiceException exp)
/*     */     {
/* 728 */       reportError(exp);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deleteFormat()
/*     */   {
/* 735 */     int index = this.m_formatList.getSelectedIndex();
/* 736 */     if (index < 0)
/*     */     {
/* 739 */       reportError(IdcMessageFactory.lc("apSelectFormatToDelete", new Object[0]));
/* 740 */       return;
/*     */     }
/*     */ 
/* 743 */     Properties props = this.m_formatList.getDataAt(index);
/* 744 */     String name = props.getProperty("dFormat");
/*     */ 
/* 746 */     String action = "DELETE_DOCFORMAT";
/* 747 */     String key = "apVerifyFormatDelete";
/* 748 */     String overrideStatus = props.getProperty("overrideStatus");
/* 749 */     if (overrideStatus.length() > 0)
/*     */     {
/* 751 */       key = "apVerifyFormatRevert";
/* 752 */       action = "DELETE_DOCFORMAT_UNCONDITIONAL";
/*     */     }
/* 754 */     IdcMessage msg = IdcMessageFactory.lc(key, new Object[] { name });
/* 755 */     if (MessageBox.doMessage(this.m_systemInterface, msg, 4) != 2) {
/*     */       return;
/*     */     }
/*     */     try
/*     */     {
/* 760 */       DataBinder binder = new DataBinder();
/* 761 */       binder.setLocalData(props);
/* 762 */       AppLauncher.executeService(action, binder);
/* 763 */       refreshFormatList(binder, "");
/*     */     }
/*     */     catch (Exception exp)
/*     */     {
/* 767 */       reportError(LocaleUtils.createMessageListFromThrowable(exp));
/*     */     }
/*     */   }
/*     */ 
/*     */   public String createDisplayString(FieldInfo info, String name, String value, Vector row)
/*     */   {
/* 778 */     boolean enabled = StringUtils.convertToBool(value, false);
/* 779 */     String key = (enabled) ? "apTrue" : "apFalse";
/* 780 */     return this.m_systemInterface.getString(key);
/*     */   }
/*     */ 
/*     */   public String createExtendedDisplayString(FieldInfo info, String name, String value, Vector row)
/*     */   {
/* 785 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 791 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 85069 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocFormatsDlg
 * JD-Core Version:    0.5.4
 */