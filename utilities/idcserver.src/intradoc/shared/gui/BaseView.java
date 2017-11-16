/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.DateUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemInterface;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.CustomChoice;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.CustomPanel;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.DynamicComponentExchange;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.MessageBox;
/*     */ import intradoc.gui.iwt.ColumnInfo;
/*     */ import intradoc.gui.iwt.UdlPanel;
/*     */ import intradoc.gui.iwt.UserDrawList;
/*     */ import intradoc.shared.DialogHelpTable;
/*     */ import intradoc.shared.DocumentLocalizedProfile;
/*     */ import intradoc.shared.FieldDef;
/*     */ import intradoc.shared.SharedContext;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.TableFields;
/*     */ import intradoc.shared.TopicInfo;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.FlowLayout;
/*     */ import java.awt.Insets;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.accessibility.AccessibleContext;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JCheckBox;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPopupMenu;
/*     */ 
/*     */ public class BaseView
/*     */ {
/*  76 */   public SystemInterface m_systemInterface = null;
/*  77 */   public ExecutionContext m_cxt = null;
/*  78 */   protected ContainerHelper m_helper = null;
/*  79 */   protected RefreshView m_refresher = null;
/*     */ 
/*  82 */   protected DocumentLocalizedProfile m_docProfile = null;
/*     */ 
/*  84 */   protected ViewData m_viewData = null;
/*     */ 
/*  87 */   protected UdlPanel m_list = null;
/*     */   protected JCheckBox m_useFilterBox;
/*     */   protected JCheckBox m_useInDateBox;
/*     */   protected CustomChoice m_inDate;
/*  93 */   protected Hashtable m_filterData = new Hashtable();
/*     */ 
/*  96 */   public DisplayStringCallback m_displayCallback = null;
/*  97 */   public String[] m_displayColumns = null;
/*     */ 
/*  99 */   public ShowColumnData m_columnData = null;
/*     */ 
/* 102 */   protected ViewFields m_filterFields = null;
/*     */ 
/* 105 */   protected boolean m_isFilterChanged = false;
/* 106 */   protected DataResultSet m_filterDefaults = null;
/* 107 */   protected boolean m_customMetaInSeparatePanel = true;
/* 108 */   protected boolean m_isFieldOnlyFilter = true;
/* 109 */   protected String m_filterHelpPage = "Filter";
/*     */ 
/* 112 */   protected boolean m_isFixedFilter = false;
/* 113 */   protected boolean m_isFixedShowColumns = false;
/*     */ 
/*     */   public BaseView(ContainerHelper helper, RefreshView refresher, DocumentLocalizedProfile docProfile)
/*     */   {
/* 121 */     this.m_helper = helper;
/* 122 */     this.m_systemInterface = helper.m_exchange.m_sysInterface;
/* 123 */     this.m_cxt = this.m_systemInterface.getExecutionContext();
/* 124 */     this.m_refresher = refresher;
/* 125 */     this.m_columnData = new ShowColumnData();
/*     */ 
/* 127 */     this.m_docProfile = docProfile;
/*     */   }
/*     */ 
/*     */   public void init(ViewData viewData)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initUI(ViewData viewData)
/*     */   {
/* 137 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/* 138 */     initUI(viewData, mainPanel);
/*     */   }
/*     */ 
/*     */   public void initUI(ViewData viewData, JPanel mainPanel)
/*     */   {
/* 143 */     this.m_viewData = viewData;
/* 144 */     this.m_columnData.m_viewName = viewData.m_viewName;
/*     */ 
/* 146 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/* 147 */     this.m_helper.m_gridHelper.m_gc.insets = new Insets(5, 20, 5, 20);
/*     */ 
/* 150 */     buildFilterUI(mainPanel, this.m_viewData.m_useInDate);
/*     */ 
/* 153 */     this.m_list = new UdlPanel(LocaleResources.getString(this.m_viewData.m_listTitle, this.m_cxt), null, 350, 20, this.m_viewData.m_tableName, true);
/*     */ 
/* 155 */     this.m_list.init();
/* 156 */     this.m_list.setMultipleMode(this.m_viewData.m_isMultipleMode);
/*     */ 
/* 158 */     addDisplayMaps();
/*     */ 
/* 161 */     if (this.m_viewData.m_idColumn != null)
/*     */     {
/* 163 */       this.m_list.setIDColumn(this.m_viewData.m_idColumn);
/*     */     }
/* 165 */     if (this.m_viewData.m_iconColumn != null)
/*     */     {
/* 167 */       this.m_list.setIconColumn(this.m_viewData.m_iconColumn);
/*     */     }
/*     */ 
/* 170 */     configureFilter(true);
/* 171 */     initDefaults();
/* 172 */     configureColumnInfos(this.m_columnData.m_columnFields);
/*     */ 
/* 175 */     createShowColumns();
/* 176 */     this.m_list.setVisibleColumns(this.m_columnData.m_columnStr);
/*     */ 
/* 178 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 179 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/* 180 */     this.m_helper.addComponent(mainPanel, this.m_list);
/* 181 */     this.m_helper.m_gridHelper.m_gc.weighty = 0.0D;
/*     */   }
/*     */ 
/*     */   protected void addDisplayMaps()
/*     */   {
/* 186 */     if (this.m_displayColumns == null)
/*     */     {
/* 188 */       return;
/*     */     }
/*     */ 
/* 191 */     int num = this.m_displayColumns.length;
/* 192 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 194 */       this.m_list.setDisplayCallback(this.m_displayColumns[i], this.m_displayCallback);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void buildFilterUI(JPanel mainPanel, boolean useInDate)
/*     */   {
/* 203 */     ItemListener iListener = new Object()
/*     */     {
/*     */       public void itemStateChanged(ItemEvent e)
/*     */       {
/* 207 */         Object cmp = e.getItemSelectable();
/* 208 */         if ((((cmp != BaseView.this.m_inDate) || (!BaseView.this.m_useInDateBox.isSelected()))) && (cmp == BaseView.this.m_inDate)) {
/*     */           return;
/*     */         }
/*     */         try
/*     */         {
/* 213 */           BaseView.this.refreshView();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 217 */           MessageBox.reportError(BaseView.this.m_systemInterface, exp);
/*     */         }
/*     */       }
/*     */     };
/* 222 */     this.m_useFilterBox = new JCheckBox(LocaleResources.getString("apUseFilterBoxLabel", this.m_cxt));
/*     */ 
/* 224 */     this.m_useFilterBox.addItemListener(iListener);
/* 225 */     this.m_useFilterBox.setSelected(this.m_viewData.m_useFilter);
/*     */ 
/* 227 */     JButton filterBtn = new JButton(LocaleResources.getString("apDefineFilterButtonLabel", this.m_cxt));
/*     */ 
/* 229 */     ActionListener listener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 233 */         BaseView.this.configureFilter(!BaseView.this.m_isFixedFilter);
/* 234 */         FilterDlg dlg = new FilterDlg(BaseView.this.m_systemInterface, LocaleResources.getString("apDefineFilterTitle", BaseView.this.m_cxt), DialogHelpTable.getHelpPage(BaseView.this.m_filterHelpPage), BaseView.this.m_refresher.getSharedContext());
/*     */ 
/* 237 */         dlg.setCustomMetaInSeparatePanel(BaseView.this.m_customMetaInSeparatePanel);
/* 238 */         dlg.setIsFieldOnly(BaseView.this.m_isFieldOnlyFilter);
/* 239 */         dlg.addIgnoreField("dRevLabel");
/* 240 */         dlg.init(BaseView.this.m_filterData, BaseView.this.m_docProfile, BaseView.this.m_filterFields, BaseView.this.m_viewData);
/* 241 */         if ((dlg.prompt() != 1) || (!BaseView.this.m_useFilterBox.isSelected()))
/*     */           return;
/*     */         try
/*     */         {
/* 245 */           BaseView.this.refreshView();
/*     */         }
/*     */         catch (Exception exp)
/*     */         {
/* 249 */           MessageBox.reportError(BaseView.this.m_systemInterface, exp);
/*     */         }
/*     */       }
/*     */     };
/* 254 */     filterBtn.addActionListener(listener);
/*     */ 
/* 256 */     if (useInDate)
/*     */     {
/* 258 */       createInDate(iListener);
/*     */     }
/*     */ 
/* 262 */     JPanel filterPanel = new CustomPanel();
/* 263 */     filterPanel.setLayout(new FlowLayout());
/* 264 */     filterPanel.add(this.m_useFilterBox);
/* 265 */     filterPanel.add(filterBtn);
/*     */ 
/* 267 */     if (useInDate)
/*     */     {
/* 269 */       filterPanel.add(new CustomLabel(LocaleResources.getString("apLabelAndSeperator", this.m_cxt)));
/*     */ 
/* 271 */       filterPanel.add(this.m_useInDateBox);
/* 272 */       filterPanel.add(this.m_inDate);
/*     */     }
/*     */ 
/* 276 */     this.m_helper.m_gridHelper.prepareAddLastRowElement();
/* 277 */     this.m_helper.addComponent(mainPanel, filterPanel);
/*     */   }
/*     */ 
/*     */   protected void createInDate(ItemListener iListener)
/*     */   {
/* 282 */     this.m_useInDateBox = new JCheckBox(LocaleResources.getString("apLabelReleaseDateSince", this.m_cxt));
/*     */ 
/* 284 */     this.m_useInDateBox.setSelected(this.m_viewData.m_inDateState);
/* 285 */     this.m_useInDateBox.addItemListener(iListener);
/*     */ 
/* 287 */     this.m_inDate = new CustomChoice();
/* 288 */     String[][] displayMap = TableFields.TIME_OPTIONLIST;
/* 289 */     for (int i = 0; i < displayMap.length; ++i)
/*     */     {
/* 291 */       this.m_inDate.addItem(displayMap[i][1]);
/*     */     }
/* 293 */     this.m_inDate.addItemListener(iListener);
/* 294 */     this.m_inDate.getAccessibleContext().setAccessibleName(LocaleResources.getString("apTitleInDate", this.m_cxt));
/*     */   }
/*     */ 
/*     */   public void setFilterHelpPage(String helpPage)
/*     */   {
/* 299 */     this.m_filterHelpPage = helpPage;
/*     */   }
/*     */ 
/*     */   protected int getInDateNumDays()
/*     */   {
/* 307 */     String value = (String)this.m_inDate.getSelectedItem();
/* 308 */     int numDays = 1;
/* 309 */     if (value != null)
/*     */     {
/* 311 */       String numStr = StringUtils.getInternalString(TableFields.TIME_OPTIONLIST, value);
/* 312 */       numDays = Integer.parseInt(numStr);
/*     */     }
/* 314 */     return numDays;
/*     */   }
/*     */ 
/*     */   public void setFilter(Vector filter)
/*     */   {
/* 319 */     if (filter == null)
/*     */     {
/* 321 */       return;
/*     */     }
/*     */ 
/* 324 */     int num = filter.size();
/* 325 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 327 */       FilterData fd = (FilterData)filter.elementAt(i);
/* 328 */       String name = fd.m_fieldDef.m_name;
/* 329 */       this.m_filterData.put(name, fd);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Vector buildFilter()
/*     */   {
/* 335 */     return buildFilterEx(false);
/*     */   }
/*     */ 
/*     */   public Vector buildFilterEx(boolean isAll)
/*     */   {
/* 340 */     this.m_isFilterChanged = false;
/* 341 */     Vector filter = new IdcVector();
/*     */ 
/* 343 */     FilterData fd = null;
/* 344 */     if ((this.m_useInDateBox != null) && (this.m_useInDateBox.isSelected()))
/*     */     {
/* 346 */       int numDays = getInDateNumDays();
/* 347 */       fd = new FilterData("dInDate", "date", ">=", String.valueOf(numDays));
/* 348 */       fd.m_isUsed = true;
/* 349 */       filter.addElement(fd);
/*     */     }
/*     */ 
/* 352 */     if ((!isAll) && (!this.m_useFilterBox.isSelected()))
/*     */     {
/* 354 */       return filter;
/*     */     }
/*     */ 
/* 357 */     DataResultSet drset = FilterUtils.createEmptyFilterSet(this.m_filterDefaults);
/* 358 */     for (Enumeration en = this.m_filterData.elements(); en.hasMoreElements(); )
/*     */     {
/* 360 */       fd = (FilterData)en.nextElement();
/* 361 */       if (fd.m_isUsed == true)
/*     */       {
/* 363 */         filter.addElement(fd);
/*     */       }
/*     */ 
/* 367 */       if ((this.m_filterDefaults != null) && 
/* 369 */         (!this.m_isFilterChanged))
/*     */       {
/* 371 */         Vector row = this.m_filterDefaults.findRow(0, fd.m_id);
/* 372 */         Properties props = null;
/* 373 */         if (row != null)
/*     */         {
/* 375 */           props = this.m_filterDefaults.getCurrentRowProps();
/*     */         }
/* 377 */         this.m_isFilterChanged = (!FilterUtils.compareFilterToDefault(fd, props));
/*     */       }
/*     */ 
/* 381 */       Vector row = FilterUtils.createFilterRow(fd, drset);
/* 382 */       if (row != null)
/*     */       {
/* 384 */         drset.addRow(row);
/*     */       }
/*     */     }
/*     */ 
/* 388 */     if ((this.m_filterDefaults == null) && (!drset.isEmpty()))
/*     */     {
/* 390 */       this.m_isFilterChanged = true;
/*     */     }
/* 392 */     this.m_filterDefaults = drset;
/*     */ 
/* 394 */     return filter;
/*     */   }
/*     */ 
/*     */   public void configureFilter(boolean forceRefresh)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void initDefaults()
/*     */   {
/* 404 */     SharedContext shContext = this.m_refresher.getSharedContext();
/* 405 */     UserData userData = shContext.getUserData();
/*     */ 
/* 407 */     TopicInfo info = userData.getTopicInfo("appcommongui");
/* 408 */     if ((info != null) && (info.m_data != null))
/*     */     {
/* 410 */       String viewName = this.m_viewData.m_viewName;
/* 411 */       if ((viewName != null) && (viewName.length() > 0))
/*     */       {
/* 413 */         String filterName = viewName + ":filter";
/* 414 */         DataResultSet filterDefaults = (DataResultSet)info.m_data.getResultSet(filterName);
/* 415 */         if (filterDefaults != null)
/*     */         {
/* 417 */           this.m_filterDefaults = filterDefaults;
/*     */         }
/*     */ 
/* 420 */         String columnStr = info.m_data.getLocal(viewName + ":columns");
/* 421 */         if ((columnStr != null) && (columnStr.length() > 0))
/*     */         {
/* 423 */           this.m_columnData.m_columnStr = columnStr;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 429 */     FilterUtils.createFilterData(this.m_filterFields, this.m_filterData, this.m_filterDefaults, true);
/*     */   }
/*     */ 
/*     */   public void configureShowColumns(boolean forceRefresh)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void configureColumnInfos(ViewFields fields)
/*     */   {
/* 442 */     if (fields == null)
/*     */     {
/* 444 */       configureShowColumns(true);
/* 445 */       fields = this.m_columnData.m_columnFields;
/*     */     }
/*     */ 
/* 448 */     int size = fields.m_viewFields.size();
/* 449 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 451 */       FieldDef fdef = (FieldDef)fields.m_viewFields.elementAt(i);
/* 452 */       ColumnInfo cinfo = makeColumnInfo(fdef);
/* 453 */       this.m_list.setColumnInfo(cinfo);
/*     */     }
/*     */ 
/* 457 */     String str = this.m_columnData.m_columnStr;
/* 458 */     Vector columns = StringUtils.parseArray(str, ',', ',');
/* 459 */     int num = columns.size();
/* 460 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 462 */       String name = (String)columns.elementAt(i);
/* 463 */       if (this.m_list.getColumnInfo(name) != null)
/*     */         continue;
/* 465 */       columns.removeElementAt(i);
/* 466 */       --i;
/* 467 */       num = columns.size();
/*     */     }
/*     */ 
/* 470 */     this.m_columnData.m_columnStr = StringUtils.createString(columns, ',', ',');
/*     */   }
/*     */ 
/*     */   protected ColumnInfo makeColumnInfo(FieldDef fdef)
/*     */   {
/* 475 */     ColumnInfo cinfo = new ColumnInfo(fdef.m_caption, fdef.m_name, 2.0D);
/*     */ 
/* 477 */     if (fdef.m_type.equalsIgnoreCase("date"))
/*     */     {
/* 479 */       cinfo.m_weight = 10.0D;
/*     */     }
/* 481 */     else if (fdef.m_type.equalsIgnoreCase("memo"))
/*     */     {
/* 483 */       cinfo.m_weight = 20.0D;
/*     */     }
/* 485 */     else if (fdef.m_type.equalsIgnoreCase("bigtext"))
/*     */     {
/* 487 */       cinfo.m_weight = 15.0D;
/*     */     }
/* 489 */     else if (fdef.m_type.equalsIgnoreCase("text"))
/*     */     {
/* 491 */       cinfo.m_weight = 8.0D;
/*     */     }
/* 493 */     else if (fdef.m_type.equalsIgnoreCase("yes/no"))
/*     */     {
/* 495 */       cinfo.m_weight = 3.0D;
/*     */     }
/* 497 */     else if (fdef.m_type.equalsIgnoreCase("int"))
/*     */     {
/* 499 */       cinfo.m_weight = 7.0D;
/* 500 */       cinfo.m_columnAlignment = 12;
/*     */     }
/*     */ 
/* 503 */     return cinfo;
/*     */   }
/*     */ 
/*     */   protected void createShowColumns()
/*     */   {
/* 508 */     JButton showClmns = new JButton(LocaleResources.getString("apShowColumnsButtonLabel", this.m_cxt));
/*     */ 
/* 510 */     ActionListener showListener = new ActionListener()
/*     */     {
/*     */       public void actionPerformed(ActionEvent e)
/*     */       {
/* 514 */         ShowColumnDlg dlg = new ShowColumnDlg(BaseView.this.m_systemInterface, LocaleResources.getString("apShowColumnsTitle", BaseView.this.m_cxt));
/*     */ 
/* 518 */         BaseView.this.configureShowColumns(!BaseView.this.m_isFixedShowColumns);
/* 519 */         UserDrawList list = BaseView.this.m_list.m_list;
/* 520 */         int numColumns = list.getColumnCount();
/* 521 */         Vector columns = new IdcVector();
/* 522 */         for (int i = 0; i < numColumns; ++i)
/*     */         {
/* 524 */           ColumnInfo info = list.getColumnInfo(i);
/*     */ 
/* 526 */           ShowColumnInfo showInfo = new ShowColumnInfo();
/* 527 */           showInfo.m_name = info.m_fieldId;
/* 528 */           showInfo.m_label = info.m_labelText;
/* 529 */           showInfo.m_order = i;
/*     */ 
/* 531 */           columns.addElement(showInfo);
/*     */         }
/* 533 */         BaseView.this.m_columnData.m_columns = columns;
/*     */ 
/* 535 */         dlg.initEx(BaseView.this.m_columnData, true, BaseView.this.m_refresher.getSharedContext());
/* 536 */         if (dlg.prompt() != 1)
/*     */           return;
/* 538 */         BaseView.this.m_list.setVisibleColumnsEx(BaseView.this.m_columnData.m_columnStr, BaseView.this.m_columnData.m_columnLabels);
/*     */ 
/* 541 */         String sel = BaseView.this.m_list.getSelectedObj();
/* 542 */         BaseView.this.m_list.reloadList(sel);
/*     */       }
/*     */     };
/* 546 */     showClmns.addActionListener(showListener);
/*     */ 
/* 548 */     JPanel btnPanel = this.m_list.getButtonPanel();
/* 549 */     btnPanel.add(showClmns);
/*     */   }
/*     */ 
/*     */   public void refreshView()
/*     */     throws ServiceException
/*     */   {
/* 555 */     if (this.m_list == null)
/*     */       return;
/* 557 */     String[] selObjs = this.m_list.getSelectedObjs();
/* 558 */     refreshView(selObjs);
/*     */   }
/*     */ 
/*     */   public void refreshView(String obj)
/*     */     throws ServiceException
/*     */   {
/* 564 */     refreshView(new String[] { obj });
/*     */   }
/*     */ 
/*     */   public void refreshView(String[] selectedObjs) throws ServiceException
/*     */   {
/* 569 */     if (this.m_refresher == null)
/*     */     {
/* 571 */       throw new ServiceException("!apNoRefreshViewError");
/*     */     }
/*     */ 
/* 574 */     Vector filter = buildFilter();
/* 575 */     DataResultSet defSet = null;
/* 576 */     if (this.m_isFilterChanged)
/*     */     {
/* 578 */       defSet = this.m_filterDefaults;
/*     */     }
/* 580 */     DataBinder binder = this.m_refresher.refresh(this.m_viewData.m_tableName, filter, defSet);
/*     */ 
/* 582 */     prepareForView(binder);
/*     */ 
/* 584 */     this.m_list.refreshListEx(binder, selectedObjs);
/* 585 */     this.m_refresher.checkSelection();
/*     */ 
/* 587 */     updateStatusMessage(binder);
/*     */   }
/*     */ 
/*     */   public void updateStatusMessage(DataBinder binder)
/*     */   {
/* 593 */     String msg = LocaleResources.getString("apLabelReady", this.m_cxt);
/* 594 */     int num = this.m_list.getNumRows();
/* 595 */     if (this.m_list.getNumRows() == 0)
/*     */     {
/* 597 */       msg = LocaleResources.getString("apNoItemsMatchedCriteria_" + this.m_viewData.m_msg, this.m_cxt);
/*     */     }
/* 600 */     else if (StringUtils.convertToBool(binder.getLocal("copyAborted"), false))
/*     */     {
/* 604 */       msg = LocaleResources.getString("apResultsLimited_" + this.m_viewData.m_msg, this.m_cxt, "" + num);
/*     */     }
/*     */ 
/* 608 */     this.m_systemInterface.displayStatus(msg);
/*     */   }
/*     */ 
/*     */   public void prepareForView(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public String buildSQL(Vector filter)
/*     */   {
/* 618 */     StringBuffer whereClause = new StringBuffer();
/* 619 */     String tail = null;
/*     */ 
/* 621 */     int size = filter.size();
/* 622 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 624 */       FilterData fd = (FilterData)filter.elementAt(i);
/*     */ 
/* 626 */       String name = fd.m_fieldDef.m_name;
/* 627 */       if (name.equals("isLatestRev"))
/*     */       {
/* 629 */         tail = "Revisions.dRevRank = 0";
/*     */       }
/*     */       else
/*     */       {
/* 633 */         String type = fd.m_fieldDef.m_type;
/* 634 */         if ((name.equals("dInDate")) && (type.equalsIgnoreCase("date")))
/*     */         {
/* 636 */           Date dte = null;
/*     */           try
/*     */           {
/* 639 */             int numDays = Integer.parseInt(fd.getValueAt(0));
/* 640 */             dte = DateUtils.getDateXDaysAgo(numDays);
/*     */ 
/* 642 */             if (SharedObjects.getEnvValueAsBoolean("UseTamino", false))
/*     */             {
/* 644 */               fd.setValueAt(String.valueOf(dte.getTime() / 1000L), 0);
/* 645 */               fd.m_fieldDef.m_type = "Int";
/*     */             }
/*     */             else
/*     */             {
/* 649 */               fd.setValueAt(LocaleUtils.formatODBC(dte), 0);
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/* 654 */             if (SystemUtils.m_verbose)
/*     */             {
/* 656 */               Report.debug("systemparse", null, ignore);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 661 */         String newClause = null;
/*     */         try
/*     */         {
/* 664 */           newClause = FilterUtils.createFilterClause(fd);
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 668 */           Report.trace(null, "Unable to create the filter clause.", e);
/*     */         }
/* 670 */         if ((newClause == null) || (newClause.length() <= 0))
/*     */           continue;
/* 672 */         if (whereClause.length() > 0)
/*     */         {
/* 674 */           whereClause.append(" AND ");
/*     */         }
/* 676 */         whereClause.append(newClause);
/*     */       }
/*     */     }
/*     */ 
/* 680 */     if (tail != null)
/*     */     {
/* 682 */       if (whereClause.length() > 0)
/*     */       {
/* 684 */         whereClause.append(" AND ");
/*     */       }
/* 686 */       whereClause.append(tail);
/*     */     }
/*     */ 
/* 689 */     return whereClause.toString();
/*     */   }
/*     */ 
/*     */   public void addPopup(String title, String[][] commands, ActionListener listener)
/*     */   {
/* 697 */     this.m_list.addPopupEx(title, commands, listener);
/*     */   }
/*     */ 
/*     */   public JPopupMenu getPopupMenu()
/*     */   {
/* 702 */     return this.m_list.getPopupMenu();
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener listener)
/*     */   {
/* 707 */     this.m_list.addItemListener(listener);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener listener)
/*     */   {
/* 712 */     this.m_list.m_list.addActionListener(listener);
/*     */   }
/*     */ 
/*     */   public int getSelectedIndex()
/*     */   {
/* 720 */     return this.m_list.getSelectedIndex();
/*     */   }
/*     */ 
/*     */   public int[] getSelectedIndexes()
/*     */   {
/* 725 */     return this.m_list.getSelectedIndexes();
/*     */   }
/*     */ 
/*     */   public String getSelectedObj()
/*     */   {
/* 730 */     return this.m_list.getSelectedObj();
/*     */   }
/*     */ 
/*     */   public String[] getSelectedObjs()
/*     */   {
/* 735 */     return this.m_list.getSelectedObjs();
/*     */   }
/*     */ 
/*     */   public Properties getDataAt(int index)
/*     */   {
/* 740 */     return this.m_list.getDataAt(index);
/*     */   }
/*     */ 
/*     */   public void addButtonToolbar(JPanel pnl)
/*     */   {
/* 745 */     this.m_list.add("South", pnl);
/*     */   }
/*     */ 
/*     */   public UdlPanel getList()
/*     */   {
/* 753 */     return this.m_list;
/*     */   }
/*     */ 
/*     */   public ViewData getViewData()
/*     */   {
/* 758 */     return this.m_viewData;
/*     */   }
/*     */ 
/*     */   public void setShowColumnFields(ViewFields fields)
/*     */   {
/* 768 */     this.m_isFixedShowColumns = true;
/* 769 */     this.m_columnData.m_columnFields = fields;
/*     */   }
/*     */ 
/*     */   public void setShowColumnInfo(String columnStr)
/*     */   {
/* 774 */     this.m_columnData.m_columnStr = columnStr;
/* 775 */     if (this.m_list == null)
/*     */       return;
/* 777 */     this.m_list.setVisibleColumns(columnStr);
/*     */   }
/*     */ 
/*     */   public void setPersistentColumns(String columnStr, String headerStr)
/*     */   {
/* 783 */     Vector columns = StringUtils.parseArray(columnStr, ',', ',');
/* 784 */     Vector headers = StringUtils.parseArray(headerStr, ',', ',');
/*     */ 
/* 786 */     int num = columns.size();
/* 787 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 789 */       ShowColumnInfo info = new ShowColumnInfo();
/* 790 */       info.m_name = ((String)columns.elementAt(i));
/* 791 */       info.m_label = ((String)headers.elementAt(i));
/* 792 */       info.m_order = i;
/*     */ 
/* 794 */       this.m_columnData.m_persistentColumns.addElement(info);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDisplayCallbackInfo(DisplayStringCallback displayCbk, String[] displayColumns)
/*     */   {
/* 800 */     this.m_displayCallback = displayCbk;
/* 801 */     this.m_displayColumns = displayColumns;
/*     */   }
/*     */ 
/*     */   public void setFilterFields(ViewFields fields)
/*     */   {
/* 811 */     this.m_isFixedFilter = true;
/* 812 */     this.m_filterFields = fields;
/*     */   }
/*     */ 
/*     */   public DataResultSet getFilterDefaults()
/*     */   {
/* 817 */     return this.m_filterDefaults;
/*     */   }
/*     */ 
/*     */   public boolean getIsFilterChanged()
/*     */   {
/* 822 */     return this.m_isFilterChanged;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 827 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.BaseView
 * JD-Core Version:    0.5.4
 */