/*     */ package intradoc.apps.docconfig;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomText;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.AppContextUtils;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JPanel;
/*     */ 
/*     */ public class DocClassPanel extends DocConfigPanel
/*     */   implements ItemListener, ActionListener, DisplayStringCallback
/*     */ {
/*     */   protected UdlPanel m_docClassList;
/*     */   protected UdlPanel m_docMetaSetList;
/*     */   protected UdlPanel m_metaList;
/*     */   protected JButton m_btnDeleteClass;
/*     */   protected JButton m_btnAddMetaset;
/*     */   protected JButton m_btnRemoveMetaset;
/*     */   protected JButton m_btnUpdateDatabase;
/*  71 */   protected String m_currentClass = "";
/*  72 */   protected String m_currentMetaSet = "";
/*     */ 
/*  74 */   protected String m_docClassColumns = "dDocClass,dDefaultProfile,dDocClassDescription";
/*     */ 
/*     */   public DocClassPanel()
/*     */   {
/*  78 */     this.m_subject = "docclasses,dynamicqueries";
/*     */   }
/*     */ 
/*     */   public void init(SystemInterface sys)
/*     */     throws ServiceException
/*     */   {
/*  84 */     super.init(sys);
/*  85 */     initUI();
/*     */   }
/*     */ 
/*     */   protected void initUI() throws ServiceException
/*     */   {
/*  90 */     JPanel mainPanel = new PanePanel();
/*  91 */     mainPanel.setLayout(new BorderLayout());
/*     */ 
/*  93 */     JPanel docClassesPnl = createClassList();
/*  94 */     JPanel metaSetsPnl = createDocMetaSetList();
/*  95 */     createMetaList();
/*     */ 
/*  97 */     mainPanel.add(docClassesPnl, "North");
/*  98 */     mainPanel.add(metaSetsPnl, "West");
/*  99 */     mainPanel.add(this.m_metaList, "East");
/*     */ 
/* 101 */     this.m_helper.makePanelGridBag(this, 1);
/*     */ 
/* 103 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/* 104 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 105 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*     */ 
/* 107 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 108 */     this.m_helper.addLastComponentInRow(this, mainPanel);
/*     */ 
/* 110 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/* 111 */     this.m_helper.addLastComponentInRow(this, new CustomText(""));
/*     */ 
/* 114 */     refreshView();
/*     */   }
/*     */ 
/*     */   protected JPanel createClassList()
/*     */   {
/* 119 */     JPanel dcpnl = new JPanel();
/* 120 */     GridBagHelper gh = this.m_helper.m_gridHelper;
/*     */ 
/* 122 */     gh.useGridBag(dcpnl);
/* 123 */     gh.m_gc.fill = 1;
/* 124 */     gh.m_gc.weightx = 1.0D;
/* 125 */     gh.m_gc.weighty = 1.0D;
/*     */ 
/* 127 */     String columns = "dDocClass,dDefaultProfile,dDocClassDescription";
/* 128 */     this.m_docClassList = new UdlPanel(this.m_systemInterface.getString("apDocClassInfo"), null, 500, 10, "DocClassesInfo", true);
/*     */ 
/* 132 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDcNameColumn"), "dDocClass", 8.0D);
/* 133 */     this.m_docClassList.setColumnInfo(info);
/* 134 */     info = new ColumnInfo(this.m_systemInterface.getString("apDcProfileColumn"), "dDefaultProfile", 8.0D);
/* 135 */     this.m_docClassList.setColumnInfo(info);
/* 136 */     info = new ColumnInfo(this.m_systemInterface.getString("apDcDescColumn"), "dDocClassDescription", 14.0D);
/* 137 */     this.m_docClassList.setColumnInfo(info);
/*     */ 
/* 139 */     this.m_docClassList.setVisibleColumns(columns);
/* 140 */     this.m_docClassList.setIDColumn("dDocClass");
/* 141 */     this.m_docClassList.setStateColumn("isValid");
/* 142 */     this.m_docClassList.useDefaultListener();
/* 143 */     this.m_docClassList.m_list.addActionListener(this);
/* 144 */     this.m_docClassList.m_list.addItemListener(this);
/* 145 */     this.m_docClassList.init();
/*     */ 
/* 147 */     this.m_helper.addLastComponentInRow(dcpnl, this.m_docClassList);
/*     */ 
/* 149 */     JPanel btnPanel = new PanePanel();
/* 150 */     JButton btn = this.m_docClassList.addButton(LocaleResources.getString("apDcButtonAddClass", this.m_ctx), false);
/* 151 */     btn.setActionCommand("addClass");
/* 152 */     btn.addActionListener(this);
/* 153 */     btnPanel.add(btn);
/*     */ 
/* 155 */     btn = this.m_docClassList.addButton(LocaleResources.getString("apDcButtonDeleteClass", this.m_ctx), true);
/* 156 */     this.m_btnDeleteClass = btn;
/* 157 */     btn.setActionCommand("deleteClass");
/* 158 */     btn.addActionListener(this);
/* 159 */     btnPanel.add(btn);
/*     */ 
/* 161 */     btn = this.m_docClassList.addButton(LocaleResources.getString("apDcButtonEditClass", this.m_ctx), true);
/* 162 */     btn.setActionCommand("editClass");
/* 163 */     btn.addActionListener(this);
/* 164 */     btnPanel.add(btn);
/*     */ 
/* 166 */     btn = this.m_docClassList.addButton(LocaleResources.getString("apDcButtonUpdateDatabase", this.m_ctx), false);
/* 167 */     this.m_btnUpdateDatabase = btn;
/* 168 */     this.m_btnUpdateDatabase.setEnabled(false);
/* 169 */     btn.setActionCommand("updateDatabase");
/* 170 */     btn.addActionListener(this);
/* 171 */     btnPanel.add(btn);
/* 172 */     checkEnableUpdateDatabaseBtn();
/*     */ 
/* 174 */     this.m_helper.addLastComponentInRow(dcpnl, btnPanel);
/* 175 */     return dcpnl;
/*     */   }
/*     */ 
/*     */   protected JPanel createDocMetaSetList()
/*     */   {
/* 180 */     JPanel dcpnl = new JPanel();
/* 181 */     dcpnl.setLayout(new BorderLayout());
/*     */ 
/* 183 */     String columns = "dDocMetaSet,dStatus";
/* 184 */     this.m_docMetaSetList = new UdlPanel(this.m_systemInterface.getString("apDocMetaSetInfo"), null, 200, 10, "DocMetaSets", true);
/*     */ 
/* 188 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apDcMetaSetColumn"), "dDocMetaSet", 8.0D);
/* 189 */     this.m_docMetaSetList.setColumnInfo(info);
/* 190 */     info = new ColumnInfo(this.m_systemInterface.getString("apDcMetaSetStatus"), "dStatus", 8.0D);
/* 191 */     this.m_docMetaSetList.setColumnInfo(info);
/*     */ 
/* 193 */     this.m_docMetaSetList.setVisibleColumns(columns);
/* 194 */     this.m_docMetaSetList.setIDColumn("dDocMetaSet");
/* 195 */     this.m_docMetaSetList.setStateColumn("isValid");
/* 196 */     this.m_docMetaSetList.useDefaultListener();
/* 197 */     this.m_docMetaSetList.m_list.addActionListener(this);
/* 198 */     this.m_docMetaSetList.m_list.addItemListener(this);
/* 199 */     this.m_docMetaSetList.init();
/* 200 */     this.m_docMetaSetList.setDisplayCallback("dStatus", this);
/*     */ 
/* 202 */     dcpnl.add("Center", this.m_docMetaSetList);
/*     */ 
/* 205 */     JPanel btnPanel = new PanePanel();
/* 206 */     JButton btn = this.m_docMetaSetList.addButton(LocaleResources.getString("apDcButtonAddMetaset", this.m_ctx), true);
/* 207 */     this.m_btnAddMetaset = btn;
/* 208 */     this.m_btnAddMetaset.setEnabled(false);
/* 209 */     btn.setActionCommand("addMetaset");
/* 210 */     btn.addActionListener(this);
/* 211 */     btnPanel.add(btn);
/*     */ 
/* 214 */     btn = this.m_docMetaSetList.addButton(LocaleResources.getString("apDcButtonRemoveMetaset", this.m_ctx), true);
/* 215 */     this.m_btnRemoveMetaset = btn;
/* 216 */     this.m_btnRemoveMetaset.setEnabled(false);
/* 217 */     btn.setActionCommand("removeMetaset");
/* 218 */     btn.addActionListener(this);
/* 219 */     btnPanel.add(btn);
/*     */ 
/* 221 */     dcpnl.add("South", btnPanel);
/*     */ 
/* 223 */     return dcpnl;
/*     */   }
/*     */ 
/*     */   protected void createMetaList()
/*     */   {
/* 228 */     String columns = "dName,dType,dIsEnabled,dIsSearchable";
/* 229 */     this.m_metaList = new UdlPanel(this.m_systemInterface.getString("apLabelFieldInfo"), null, 200, 10, "DocMetaSetFields", true);
/*     */ 
/* 233 */     ColumnInfo info = new ColumnInfo(this.m_systemInterface.getString("apTitleName"), "dName", 8.0D);
/* 234 */     this.m_metaList.setColumnInfo(info);
/* 235 */     info = new ColumnInfo(this.m_systemInterface.getString("apTitleType"), "dType", 8.0D);
/* 236 */     this.m_metaList.setColumnInfo(info);
/* 237 */     info = new ColumnInfo(this.m_systemInterface.getString("apTitleEnabled"), "dIsEnabled", 8.0D);
/* 238 */     this.m_metaList.setColumnInfo(info);
/* 239 */     info = new ColumnInfo(this.m_systemInterface.getString("apTitleSearchable"), "dIsSearchable", 8.0D);
/* 240 */     this.m_metaList.setColumnInfo(info);
/*     */ 
/* 242 */     this.m_metaList.setVisibleColumns(columns);
/* 243 */     this.m_metaList.setIDColumn("dName");
/* 244 */     this.m_metaList.setStateColumn("isValid");
/* 245 */     this.m_metaList.useDefaultListener();
/* 246 */     this.m_metaList.init();
/* 247 */     this.m_metaList.setDisplayCallback("dName", this);
/* 248 */     this.m_metaList.setDisplayCallback("dType", this);
/* 249 */     this.m_metaList.setDisplayCallback("dIsEnabled", this);
/* 250 */     this.m_metaList.setDisplayCallback("dIsSearchable", this);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */   {
/* 256 */     String selObj = this.m_docClassList.getSelectedObj();
/* 257 */     refreshClassData(selObj);
/*     */   }
/*     */ 
/*     */   protected void refreshClassData(String selName)
/*     */   {
/* 262 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 267 */       executeService("GET_DOCCLASSES", binder, false);
/* 268 */       this.m_docClassList.refreshList(binder, selName);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 272 */       reportError(e);
/* 273 */       return;
/*     */     }
/*     */ 
/* 277 */     DocProfilePanel dp = (DocProfilePanel)this.m_ctx.getCachedObject("profilesPanel");
/* 278 */     if (dp == null)
/*     */       return;
/* 280 */     dp.setClassListChanged();
/*     */   }
/*     */ 
/*     */   protected void refreshMetaSetData(String selName, boolean isForceRefresh)
/*     */   {
/* 288 */     if ((!isForceRefresh) && (this.m_currentClass.equals(selName)))
/*     */       return;
/* 290 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 293 */       binder.putLocal("dDocClass", selName);
/* 294 */       executeService("GET_DOCCLASS_INFO", binder, false);
/*     */ 
/* 297 */       DataResultSet drset = (DataResultSet)binder.getResultSet("DocMetaSets");
/* 298 */       Vector row = drset.createEmptyRow();
/* 299 */       row.setElementAt("DocMeta", drset.getFieldInfoIndex("dDocMetaSet"));
/* 300 */       row.setElementAt("OK", drset.getFieldInfoIndex("dStatus"));
/* 301 */       drset.addRow(row);
/*     */ 
/* 303 */       this.m_docMetaSetList.refreshList(binder, null);
/*     */ 
/* 305 */       this.m_currentClass = selName;
/* 306 */       if (this.m_docMetaSetList.m_list.getItemCount() == 0)
/*     */       {
/* 308 */         refreshMetaData(null);
/*     */       }
/*     */       else
/*     */       {
/* 312 */         this.m_docMetaSetList.m_list.select(0);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 317 */       reportError(e);
/* 318 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void refreshMetaData(String selName)
/*     */   {
/* 327 */     if (this.m_currentMetaSet.equals(selName))
/*     */       return;
/* 329 */     DataBinder binder = new DataBinder();
/*     */ 
/* 331 */     if (selName != null)
/*     */     {
/*     */       try
/*     */       {
/* 335 */         binder.putLocal("dDocMetaSet", selName);
/* 336 */         executeService("GET_DOCMETASET_FIELDS", binder, false);
/* 337 */         this.m_metaList.refreshList(binder, null);
/* 338 */         this.m_currentMetaSet = selName;
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 342 */         reportError(e);
/* 343 */         return;
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 349 */       MetaFieldData drset = new MetaFieldData();
/* 350 */       binder.addResultSet("DocMetaSetFields", drset);
/* 351 */       this.m_metaList.refreshList(binder, null);
/* 352 */       this.m_currentMetaSet = "";
/*     */     }
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent e)
/*     */   {
/* 359 */     String cmd = e.getActionCommand();
/* 360 */     if (cmd.equals("removeMetaset"))
/*     */     {
/* 365 */       String docClass = this.m_docClassList.getSelectedObj();
/*     */ 
/* 367 */       int index = this.m_docMetaSetList.getSelectedIndex();
/* 368 */       Properties props = this.m_docMetaSetList.getDataAt(index);
/* 369 */       String metaSet = props.getProperty("dDocMetaSet");
/* 370 */       String metaSetStatus = props.getProperty("dStatus");
/*     */       try
/*     */       {
/* 374 */         removeMetasetFromDocClass(docClass, metaSet, metaSetStatus);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 378 */         reportError(se);
/*     */       }
/*     */ 
/* 381 */       refreshMetaSetData(docClass, true);
/* 382 */       checkEnableUpdateDatabaseBtn();
/*     */     }
/* 384 */     else if (cmd.equals("restoreMetaset"))
/*     */     {
/* 386 */       String docClass = this.m_docClassList.getSelectedObj();
/* 387 */       String metaSet = this.m_docMetaSetList.getSelectedObj();
/* 388 */       Properties props = new Properties();
/* 389 */       props.put("dDocClass", docClass);
/* 390 */       props.put("dDocMetaSet", metaSet);
/*     */       try
/*     */       {
/* 394 */         AppContextUtils.executeService(this, "RESTORE_DMS_FROM_DOCCLASS_DELAYED", props);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 398 */         reportError(se);
/*     */       }
/*     */ 
/* 401 */       refreshMetaSetData(docClass, true);
/* 402 */       checkEnableUpdateDatabaseBtn();
/*     */     }
/* 404 */     else if (cmd.equals("addMetaset"))
/*     */     {
/* 406 */       String docClass = this.m_docClassList.getSelectedObj();
/*     */       try
/*     */       {
/* 409 */         addDMSTable(docClass);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 413 */         reportError(se);
/*     */       }
/*     */ 
/* 416 */       refreshMetaSetData(docClass, true);
/* 417 */       this.m_btnUpdateDatabase.setEnabled(true);
/*     */     }
/* 419 */     else if (cmd.equals("addClass"))
/*     */     {
/*     */       try
/*     */       {
/* 423 */         addOrEditClass(false);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 427 */         reportError(se);
/*     */       }
/* 429 */       refreshView();
/*     */     }
/* 431 */     else if (cmd.equals("editClass"))
/*     */     {
/*     */       try
/*     */       {
/* 435 */         addOrEditClass(true);
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 439 */         reportError(se);
/*     */       }
/* 441 */       refreshView();
/*     */     }
/* 443 */     else if (cmd.equals("deleteClass"))
/*     */     {
/*     */       try
/*     */       {
/* 447 */         deleteClass();
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 451 */         reportError(se);
/*     */       }
/* 453 */       refreshView();
/* 454 */       checkEnableUpdateDatabaseBtn();
/*     */     } else {
/* 456 */       if (!cmd.equals("updateDatabase"))
/*     */         return;
/*     */       try
/*     */       {
/* 460 */         commitChanges();
/*     */       }
/*     */       catch (ServiceException se)
/*     */       {
/* 464 */         reportError(se);
/*     */       }
/*     */ 
/* 467 */       String docClass = this.m_docClassList.getSelectedObj();
/* 468 */       if (docClass != null)
/*     */       {
/* 470 */         refreshMetaSetData(docClass, true);
/*     */       }
/* 472 */       checkEnableUpdateDatabaseBtn();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void itemStateChanged(ItemEvent e)
/*     */   {
/* 478 */     if (e.getSource().equals(this.m_docClassList.m_list))
/*     */     {
/* 480 */       String selectedItem = this.m_docClassList.getSelectedObj();
/* 481 */       if (selectedItem != null)
/*     */       {
/* 483 */         if ("Base".equals(selectedItem))
/*     */         {
/* 485 */           this.m_btnDeleteClass.setEnabled(false);
/* 486 */           this.m_btnAddMetaset.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 490 */           this.m_btnDeleteClass.setEnabled(true);
/* 491 */           this.m_btnAddMetaset.setEnabled(true);
/*     */         }
/* 493 */         refreshMetaSetData(selectedItem, false);
/*     */       }
/*     */     } else {
/* 496 */       if (!e.getSource().equals(this.m_docMetaSetList.m_list))
/*     */         return;
/* 498 */       int index = this.m_docMetaSetList.getSelectedIndex();
/* 499 */       Properties props = this.m_docMetaSetList.getDataAt(index);
/* 500 */       if (props != null)
/*     */       {
/* 502 */         String selectedItem = props.getProperty("dDocMetaSet");
/* 503 */         String selectedStatus = props.getProperty("dStatus");
/* 504 */         if ("DocMeta".equals(selectedItem))
/*     */         {
/* 506 */           this.m_btnRemoveMetaset.setEnabled(false);
/*     */         }
/*     */         else
/*     */         {
/* 510 */           this.m_btnRemoveMetaset.setEnabled(true);
/*     */         }
/*     */ 
/* 514 */         if ("REMOVE".equalsIgnoreCase(selectedStatus))
/*     */         {
/* 516 */           this.m_btnRemoveMetaset.setText(LocaleResources.getString("apDcButtonRestoreMetaset", this.m_ctx));
/* 517 */           this.m_btnRemoveMetaset.setActionCommand("restoreMetaset");
/*     */         }
/*     */         else
/*     */         {
/* 521 */           this.m_btnRemoveMetaset.setText(LocaleResources.getString("apDcButtonRemoveMetaset", this.m_ctx));
/* 522 */           this.m_btnRemoveMetaset.setActionCommand("removeMetaset");
/*     */         }
/* 524 */         refreshMetaData(selectedItem);
/*     */       }
/*     */       else
/*     */       {
/* 528 */         this.m_btnRemoveMetaset.setEnabled(false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadPanelInformation()
/*     */     throws DataException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 546 */     if (name.equals("dName"))
/*     */     {
/* 548 */       return MetaFieldGui.createDisplayName(value);
/*     */     }
/* 550 */     if (name.equals("dType"))
/*     */     {
/* 552 */       if ("Decimal".equalsIgnoreCase(value))
/*     */       {
/* 554 */         return StringUtils.getPresentationString(TableFields.METAFIELD_TYPE_DECIMAL_OPTION, value);
/*     */       }
/*     */ 
/* 557 */       return StringUtils.getPresentationString(TableFields.METAFIELD_TYPES_OPTIONSLIST, value);
/*     */     }
/*     */ 
/* 560 */     if ((name.equals("dIsEnabled")) || (name.equals("dIsSearchable")))
/*     */     {
/* 563 */       return (StringUtils.convertToBool(value, false)) ? TableFields.YESNO_OPTIONLIST[0][1] : TableFields.YESNO_OPTIONLIST[1][1];
/*     */     }
/*     */ 
/* 567 */     if (name.equals("dStatus"))
/*     */     {
/* 569 */       if ((value == null) || (value.length() == 0) || (value.equalsIgnoreCase("OK")))
/*     */       {
/* 571 */         return LocaleResources.getString("apDcMetasetStatusOK", this.m_ctx);
/*     */       }
/* 573 */       if (value.equalsIgnoreCase("ADD"))
/*     */       {
/* 575 */         return LocaleResources.getString("apDcMetasetStatusAdd", this.m_ctx);
/*     */       }
/* 577 */       if (value.equalsIgnoreCase("REMOVE"))
/*     */       {
/* 579 */         return LocaleResources.getString("apDcMetasetStatusRemove", this.m_ctx);
/*     */       }
/*     */     }
/* 582 */     return value;
/*     */   }
/*     */ 
/*     */   public String createExtendedDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*     */   {
/* 588 */     return null;
/*     */   }
/*     */ 
/*     */   protected void removeMetasetFromDocClass(String docClass, String docMetaSet, String status)
/*     */     throws ServiceException
/*     */   {
/* 599 */     Properties props = new Properties();
/* 600 */     props.put("dDocClass", docClass);
/* 601 */     props.put("dDocMetaSet", docMetaSet);
/*     */ 
/* 603 */     if ((status == null) || (status.length() == 0) || (status.equalsIgnoreCase("OK")))
/*     */     {
/* 605 */       AppContextUtils.executeService(this, "DEL_DMS_FROM_DOCCLASS_DELAYED", props);
/*     */     } else {
/* 607 */       if (!status.equalsIgnoreCase("ADD"))
/*     */         return;
/* 609 */       AppContextUtils.executeService(this, "DEL_UNCOMMITTED_DMS_FROM_DOCCLASS", props);
/* 610 */       checkUnusedDocMetaSet(docMetaSet);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void addDMSTable(String docClass) throws ServiceException
/*     */   {
/* 616 */     String helpPage = DialogHelpTable.getHelpPage("AddMetaTable");
/* 617 */     AddDocMetaTableDlg dlg = new AddDocMetaTableDlg(this.m_systemInterface, this, this.m_systemInterface.localizeMessage("!apDcAddDocMetaTableTitle"), helpPage);
/*     */ 
/* 619 */     Properties props = new IdcProperties();
/* 620 */     props.put("dDocClass", docClass);
/* 621 */     dlg.init(props);
/*     */ 
/* 623 */     if (dlg.prompt() != 1)
/*     */       return;
/* 625 */     String tableName = props.getProperty("dDocMetaSet");
/* 626 */     if ((tableName == null) || (tableName.length() <= 0))
/*     */       return;
/* 628 */     AppContextUtils.executeService(this, "ADD_DMS_TO_DOCCLASS_DELAYED", props);
/*     */   }
/*     */ 
/*     */   protected void checkUnusedDocMetaSet(String metaSet)
/*     */     throws ServiceException
/*     */   {
/* 635 */     DataBinder binder = new DataBinder();
/* 636 */     binder.putLocal("dDocMetaSet", metaSet);
/* 637 */     AppContextUtils.executeService(this, "GET_DOCMETASET_INFO", binder);
/* 638 */     DataResultSet fieldInfo = (DataResultSet)binder.getResultSet("DocMetaSetInfo");
/* 639 */     if ((fieldInfo == null) || (fieldInfo.getNumFields() > 1))
/*     */       return;
/* 641 */     DataResultSet classes = (DataResultSet)binder.getResultSet("DocMetaSetClasses");
/* 642 */     if (!classes.isEmpty())
/*     */       return;
/* 644 */     IdcMessage msg = IdcMessageFactory.lc("apDcRemoveDMSTablePrompt", new Object[] { metaSet });
/* 645 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 646 */     if (result != 2)
/*     */       return;
/* 648 */     AppContextUtils.executeService(this, "DROP_DMS_TABLE", binder);
/*     */   }
/*     */ 
/*     */   protected void addOrEditClass(boolean isEdit)
/*     */     throws ServiceException
/*     */   {
/* 657 */     String helpPage = DialogHelpTable.getHelpPage("EditDocClass");
/* 658 */     EditDocClassDlg dlg = new EditDocClassDlg(this.m_systemInterface, this, (isEdit) ? this.m_systemInterface.localizeMessage("!apDCEditClassTitle") : this.m_systemInterface.localizeMessage("!apDCNewClassTitle"), helpPage, isEdit);
/*     */ 
/* 662 */     Properties newprops = new IdcProperties();
/* 663 */     if (isEdit)
/*     */     {
/* 665 */       int index = this.m_docClassList.getSelectedIndex();
/* 666 */       Properties props = this.m_docClassList.getDataAt(index);
/* 667 */       String docClass = props.getProperty("dDocClass");
/* 668 */       String defaultProfile = props.getProperty("dDefaultProfile");
/* 669 */       String description = props.getProperty("dDocClassDescription");
/*     */ 
/* 671 */       newprops.put("dDocClass", docClass);
/* 672 */       newprops.put("dDocClassDescription", description);
/* 673 */       newprops.put("dDefaultProfile", defaultProfile);
/*     */     }
/* 675 */     dlg.init(newprops);
/*     */ 
/* 677 */     if (dlg.prompt() != 1)
/*     */       return;
/* 679 */     String value = newprops.getProperty("dDefaultProfile");
/* 680 */     if ((value != null) && (value.length() == 0))
/*     */     {
/* 682 */       newprops.remove("dDefaultProfile");
/*     */     }
/* 684 */     value = newprops.getProperty("dDocClassDescription");
/* 685 */     if ((value != null) && (value.length() == 0))
/*     */     {
/* 687 */       newprops.remove("dDocClassDescription");
/*     */     }
/* 689 */     if (isEdit)
/*     */     {
/* 691 */       AppContextUtils.executeService(this, "EDIT_DOCCLASS", newprops);
/*     */     }
/*     */     else
/*     */     {
/* 695 */       AppContextUtils.executeService(this, "ADD_DOCCLASS", newprops);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void deleteClass()
/*     */     throws ServiceException
/*     */   {
/* 702 */     this.m_btnDeleteClass.setEnabled(false);
/* 703 */     IdcMessage msg = IdcMessageFactory.lc("apDCConfirmClassDeletePrompt", new Object[0]);
/* 704 */     int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 705 */     Properties props = new Properties();
/* 706 */     props.put("dDocClass", this.m_docClassList.getSelectedObj());
/* 707 */     if (result == 2)
/*     */     {
/* 709 */       AppContextUtils.executeService(this, "DELETE_DOCCLASS", props);
/*     */     }
/*     */     else
/*     */     {
/* 713 */       this.m_btnDeleteClass.setEnabled(true);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void commitChanges() throws ServiceException
/*     */   {
/* 719 */     DataBinder binder = new DataBinder();
/* 720 */     executeService("GET_UNCOMMITTED_DOCMETASETS", binder, false);
/* 721 */     DataResultSet rset = (DataResultSet)binder.getResultSet("DocMetaSets");
/* 722 */     Set classesWithRemoval = new HashSet();
/* 723 */     if (!rset.isEmpty())
/*     */     {
/* 725 */       int index = rset.getFieldInfoIndex("dStatus");
/* 726 */       for (rset.first(); rset.isRowPresent(); rset.next())
/*     */       {
/* 728 */         String status = rset.getStringValue(index);
/* 729 */         if (!"REMOVE".equals(status))
/*     */           continue;
/* 731 */         classesWithRemoval.add(rset.getStringValueByName("dDocClass"));
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 737 */       return;
/*     */     }
/*     */ 
/* 740 */     if (!classesWithRemoval.isEmpty())
/*     */     {
/* 742 */       List classesList = new ArrayList(classesWithRemoval);
/* 743 */       String classesStr = StringUtils.createString(classesList, ',', ',');
/* 744 */       IdcMessage msg = IdcMessageFactory.lc("apDCCommitWarning", new Object[] { classesStr });
/* 745 */       int result = MessageBox.doMessage(this.m_systemInterface, msg, 4);
/* 746 */       if (result != 2)
/*     */       {
/* 748 */         return;
/*     */       }
/*     */     }
/*     */ 
/* 752 */     executeService("COMMIT_DOCCLASSDEF_CHANGES", binder, false);
/*     */   }
/*     */ 
/*     */   protected void checkEnableUpdateDatabaseBtn()
/*     */   {
/* 757 */     DataBinder binder = new DataBinder();
/*     */     try
/*     */     {
/* 761 */       executeService("GET_UNCOMMITTED_DOCMETASETS", binder, false);
/* 762 */       DataResultSet rset = (DataResultSet)binder.getResultSet("DocMetaSets");
/* 763 */       if (rset.isEmpty())
/*     */       {
/* 765 */         this.m_btnUpdateDatabase.setEnabled(false);
/*     */       }
/*     */       else
/*     */       {
/* 769 */         this.m_btnUpdateDatabase.setEnabled(true);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 774 */       reportError(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 780 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98092 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docconfig.DocClassPanel
 * JD-Core Version:    0.5.4
 */