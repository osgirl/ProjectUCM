/*      */ package intradoc.apps.archiver;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemInterface;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.ResultSet;
/*      */ import intradoc.data.ResultSetFilter;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.gui.CustomText;
/*      */ import intradoc.gui.DialogHelper;
/*      */ import intradoc.gui.DisplayStringCallback;
/*      */ import intradoc.gui.DisplayStringCallbackAdaptor;
/*      */ import intradoc.gui.DynamicComponentExchange;
/*      */ import intradoc.gui.GridBagHelper;
/*      */ import intradoc.gui.MessageBox;
/*      */ import intradoc.gui.PanePanel;
/*      */ import intradoc.gui.StatusBar;
/*      */ import intradoc.gui.iwt.UdlPanel;
/*      */ import intradoc.shared.CollectionData;
/*      */ import intradoc.shared.SharedContext;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.TableFields;
/*      */ import intradoc.shared.ViewFieldDef;
/*      */ import intradoc.shared.ViewFields;
/*      */ import intradoc.shared.gui.BaseView;
/*      */ import intradoc.shared.gui.DocView;
/*      */ import intradoc.shared.gui.FilterData;
/*      */ import intradoc.shared.gui.RefreshView;
/*      */ import intradoc.shared.gui.ViewData;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.awt.Component;
/*      */ import java.awt.event.ActionEvent;
/*      */ import java.awt.event.ActionListener;
/*      */ import java.awt.event.ItemEvent;
/*      */ import java.awt.event.ItemListener;
/*      */ import java.util.Date;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.swing.JButton;
/*      */ import javax.swing.JPanel;
/*      */ 
/*      */ public class EditBatchDlg
/*      */   implements ActionListener, RefreshView
/*      */ {
/*   79 */   protected DialogHelper m_helper = null;
/*   80 */   protected CollectionContext m_collectionContext = null;
/*   81 */   protected ExecutionContext m_cxt = null;
/*      */ 
/*   83 */   protected BaseView m_docView = null;
/*   84 */   protected StatusBar m_statusBar = null;
/*      */ 
/*   86 */   protected String m_batchName = null;
/*      */ 
/*   89 */   protected DataBinder m_binder = null;
/*   90 */   protected DataResultSet m_docSet = null;
/*   91 */   protected DataResultSet m_dirtySet = null;
/*      */ 
/*   94 */   protected int m_maxRows = 0;
/*   95 */   protected int m_numRows = 0;
/*   96 */   protected int m_currentPage = 0;
/*   97 */   protected boolean m_isLastPage = false;
/*      */ 
/*   99 */   protected int[] m_starts = null;
/*  100 */   protected boolean m_hasFilterChanged = true;
/*      */ 
/*  103 */   protected Hashtable m_buttonMap = null;
/*      */ 
/*  105 */   protected boolean m_isDocView = true;
/*  106 */   protected static String m_deleteService = "DELETE_BATCH_FILE_DOCUMENTS";
/*  107 */   protected String m_viewName = null;
/*      */ 
/*  109 */   protected final int FIRST = 0;
/*  110 */   protected final int LAST = 1;
/*  111 */   protected final int PREVIOUS = 2;
/*  112 */   protected final int NEXT = 3;
/*  113 */   protected final int NO_CHANGE = 4;
/*      */ 
/*      */   public EditBatchDlg(SystemInterface sys, String title, CollectionContext context)
/*      */   {
/*  117 */     this.m_helper = new DialogHelper(sys, title, true);
/*  118 */     this.m_cxt = sys.getExecutionContext();
/*  119 */     this.m_collectionContext = context;
/*      */ 
/*  121 */     this.m_binder = new DataBinder();
/*  122 */     this.m_binder.putLocal("StartRow", "0");
/*      */ 
/*  124 */     this.m_docSet = new DataResultSet();
/*  125 */     this.m_buttonMap = new Hashtable();
/*      */ 
/*  127 */     this.m_viewName = "ArchiverBatchView";
/*      */   }
/*      */ 
/*      */   public boolean init(String batchFileName, Vector filterData)
/*      */   {
/*  133 */     this.m_batchName = batchFileName;
/*  134 */     this.m_binder.putLocal("aBatchFile", batchFileName);
/*      */ 
/*  136 */     DataResultSet drset = this.m_collectionContext.getBatchFiles();
/*      */     try
/*      */     {
/*  139 */       this.m_isDocView = StringUtils.convertToBool(ResultSetUtils.findValue(drset, "aBatchFile", batchFileName, "aDocumentExport"), true);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*      */     }
/*      */ 
/*  147 */     JPanel mainPanel = this.m_helper.m_mainPanel;
/*  148 */     this.m_helper.makePanelGridBag(mainPanel, 1);
/*      */ 
/*  150 */     initUI(mainPanel, filterData);
/*      */     try
/*      */     {
/*  154 */       this.m_docView.refreshView();
/*  155 */       this.m_helper.prompt();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  159 */       this.m_collectionContext.reportError(e);
/*  160 */       return false;
/*      */     }
/*  162 */     return true;
/*      */   }
/*      */ 
/*      */   public void initUI(JPanel pnl, Vector filterData)
/*      */   {
/*  168 */     JPanel docPanel = new PanePanel();
/*      */ 
/*  170 */     ViewData viewData = initView();
/*  171 */     viewData.m_useInDate = false;
/*  172 */     viewData.m_viewName = this.m_viewName;
/*      */ 
/*  175 */     String showColumnList = getColumnList();
/*  176 */     this.m_docView.setShowColumnInfo(showColumnList);
/*      */ 
/*  180 */     ViewFields filterFields = createFilterFields();
/*  181 */     this.m_docView.setFilterFields(filterFields);
/*      */ 
/*  184 */     this.m_docView.setFilter(filterData);
/*      */ 
/*  186 */     String[] persistentCols = getPersistentColumns();
/*  187 */     if (persistentCols != null)
/*      */     {
/*  189 */       this.m_docView.setPersistentColumns(persistentCols[0], persistentCols[1]);
/*      */     }
/*      */ 
/*  192 */     ViewFields columnFields = createShowColumnsFields();
/*  193 */     this.m_docView.setShowColumnFields(columnFields);
/*      */ 
/*  195 */     this.m_docView.initUI(viewData, docPanel);
/*      */ 
/*  197 */     ItemListener iListener = new ItemListener()
/*      */     {
/*      */       public void itemStateChanged(ItemEvent e)
/*      */       {
/*  201 */         int state = e.getStateChange();
/*  202 */         switch (state)
/*      */         {
/*      */         case 1:
/*  205 */           EditBatchDlg.this.checkSelection();
/*      */         case 2:
/*      */         }
/*      */       }
/*      */     };
/*  213 */     UdlPanel docList = this.m_docView.getList();
/*  214 */     docList.addItemListener(iListener);
/*  215 */     additionalInit();
/*      */ 
/*  218 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  219 */     this.m_helper.m_gridHelper.m_gc.weighty = 10.0D;
/*  220 */     this.m_helper.addComponent(pnl, docPanel);
/*      */ 
/*  222 */     createToolBar(docList);
/*      */   }
/*      */ 
/*      */   protected ViewData initView()
/*      */   {
/*  227 */     this.m_docView = new DocView(this.m_helper, this, null);
/*  228 */     return new ViewData(1, "", "ExportResults");
/*      */   }
/*      */ 
/*      */   protected String getColumnList()
/*      */   {
/*  233 */     return "dDocName,dRevLabel,dID,editStatus";
/*      */   }
/*      */ 
/*      */   protected String[] getPersistentColumns()
/*      */   {
/*  238 */     return new String[] { "dDocName", "Content ID" };
/*      */   }
/*      */ 
/*      */   protected void additionalInit()
/*      */   {
/*  243 */     DisplayStringCallback displayCallback = new DisplayStringCallbackAdaptor()
/*      */     {
/*      */       public String createDisplayString(FieldInfo finfo, String name, String value, Vector row)
/*      */       {
/*  249 */         String[][] displayMap = (String[][])null;
/*  250 */         if (name.equals("editStatus"))
/*      */         {
/*  252 */           displayMap = TableFields.ARCHIVER_EDIT_LIST;
/*      */         }
/*  254 */         else if (name.equals("dDocName"))
/*      */         {
/*  256 */           int size = row.size();
/*  257 */           int rowCount = Integer.parseInt((String)row.elementAt(size - 2));
/*  258 */           ++rowCount;
/*  259 */           value = rowCount + ": " + value;
/*      */         }
/*      */ 
/*  262 */         if (displayMap == null)
/*      */         {
/*  264 */           return value;
/*      */         }
/*      */ 
/*  267 */         String displayStr = StringUtils.getPresentationString(displayMap, value);
/*  268 */         if (displayStr == null)
/*      */         {
/*  270 */           displayStr = LocaleResources.getString("apLabelArchived", EditBatchDlg.this.m_cxt);
/*      */         }
/*  272 */         return displayStr;
/*      */       }
/*      */     };
/*  275 */     this.m_docView.setDisplayCallbackInfo(displayCallback, new String[] { "editStatus", "dDocName" });
/*      */ 
/*  277 */     UdlPanel docList = this.m_docView.getList();
/*      */ 
/*  279 */     IdcComparator cmp = new IdcComparator()
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/*  283 */         String s1 = (String)obj1;
/*  284 */         String s2 = (String)obj2;
/*      */ 
/*  286 */         int index = s1.indexOf(":");
/*  287 */         if (index >= 0)
/*      */         {
/*  289 */           s1 = s1.substring(index + 1);
/*      */         }
/*      */ 
/*  292 */         index = s2.indexOf(":");
/*  293 */         if (index >= 0)
/*      */         {
/*  295 */           s2 = s2.substring(index + 1);
/*      */         }
/*      */ 
/*  298 */         return s1.toLowerCase().compareTo(s2.toLowerCase());
/*      */       }
/*      */     };
/*  301 */     docList.setComparator("dDocName", cmp);
/*      */   }
/*      */ 
/*      */   public void createToolBar(UdlPanel docList)
/*      */   {
/*  307 */     String[][] BUTTON_INFO = { { "|<", "first", "false" }, { "<<", "previous", "false" }, { "", "separator", "false" }, { LocaleResources.getString("apLabelImport", this.m_cxt), "import", "true" }, { LocaleResources.getString("apLabelDelete", this.m_cxt), "delete", "false" }, { LocaleResources.getString("apLabelUndo", this.m_cxt), "undo", "false" }, { LocaleResources.getString("apLabelApply", this.m_cxt), "apply", "false" }, { LocaleResources.getString("apLabelRefresh", this.m_cxt), "refresh", "false" }, { LocaleResources.getString("apLabelClose", this.m_cxt), "close", "false" }, { "", "separator", "false" }, { ">>", "next", "false" }, { ">|", "last", "false" } };
/*      */ 
/*  324 */     JPanel toolbar = this.m_helper.m_toolbar;
/*  325 */     docList.useDefaultListener();
/*      */ 
/*  327 */     CollectionData curCollection = this.m_collectionContext.getCurrentCollection();
/*  328 */     boolean isProxied = curCollection.isProxied();
/*      */ 
/*  330 */     for (int i = 0; i < BUTTON_INFO.length; ++i)
/*      */     {
/*  332 */       Component cmp = null;
/*  333 */       String cmd = BUTTON_INFO[i][1];
/*  334 */       if (cmd.equals("separator"))
/*      */       {
/*  337 */         cmp = new CustomText("", 50);
/*      */       }
/*      */       else
/*      */       {
/*  341 */         boolean controlled = StringUtils.convertToBool(BUTTON_INFO[i][2], false);
/*  342 */         if ((cmd.equals("import")) && (isProxied))
/*      */         {
/*  345 */           controlled = false;
/*      */         }
/*      */ 
/*  348 */         JButton btn = docList.addButton(BUTTON_INFO[i][0], controlled);
/*  349 */         btn.setActionCommand(cmd);
/*  350 */         btn.addActionListener(this);
/*  351 */         cmp = btn;
/*      */ 
/*  353 */         this.m_buttonMap.put(cmd, btn);
/*      */ 
/*  355 */         if ((cmd.equals("apply")) || (cmd.equals("undo")) || (cmd.equals("delete")) || (cmd.equals("import")))
/*      */         {
/*  358 */           btn.setEnabled(false);
/*      */         }
/*      */         else
/*      */         {
/*  362 */           btn.setEnabled(!controlled);
/*      */         }
/*      */       }
/*  365 */       toolbar.add(cmp);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected ViewFields createStandardFields(boolean isFilter, boolean hasIDField)
/*      */   {
/*  372 */     ViewFields stdFields = new ViewFields(this.m_cxt);
/*      */ 
/*  374 */     stdFields.addStandardDocFields();
/*      */ 
/*  376 */     if (hasIDField)
/*      */     {
/*  379 */       stdFields.addField("dID", LocaleResources.getString("apTitleID", this.m_cxt));
/*      */     }
/*      */ 
/*  382 */     String dateCaption = null;
/*  383 */     if (isFilter)
/*      */     {
/*  385 */       dateCaption = LocaleResources.getString("apLabelReleaseDate", this.m_cxt);
/*      */     }
/*      */     else
/*      */     {
/*  389 */       dateCaption = LocaleResources.getString("apLabelReleaseDate", this.m_cxt);
/*      */     }
/*  391 */     ViewFieldDef fd = stdFields.addViewFieldDef("dInDate", dateCaption);
/*  392 */     fd.m_type = "date";
/*      */ 
/*  394 */     fd = stdFields.addViewFieldDef("editStatus", LocaleResources.getString("apLabelStatus", this.m_cxt));
/*  395 */     fd.m_isOptionList = true;
/*  396 */     fd.m_optionListKey = "ArchiverEditList";
/*  397 */     fd.m_type = "text";
/*      */ 
/*  399 */     stdFields.addRenditions();
/*  400 */     stdFields.addFlags(ViewFields.PUBLISH_FLAG_INFO);
/*  401 */     stdFields.addFlags(ViewFields.DOCUMENT_FORMATS);
/*      */ 
/*  403 */     return stdFields;
/*      */   }
/*      */ 
/*      */   protected ViewFields createFilterFields()
/*      */   {
/*  409 */     ViewFields filterFields = createStandardFields(true, false);
/*      */     try
/*      */     {
/*  414 */       DataResultSet metaSet = this.m_collectionContext.getBatchMetaSet(false, this.m_batchName);
/*  415 */       if (metaSet == null)
/*      */       {
/*  417 */         return filterFields;
/*      */       }
/*  419 */       String[][] metaInfo = ResultSetUtils.createStringTable(metaSet, new String[] { "dName", "dCaption", "dType" });
/*      */ 
/*  424 */       Vector fields = filterFields.m_viewFields;
/*  425 */       for (int i = 0; i < metaInfo.length; ++i)
/*      */       {
/*  427 */         String type = metaInfo[i][2];
/*  428 */         if (type.equalsIgnoreCase("date")) continue; if (type.equalsIgnoreCase("int"))
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  433 */         ViewFieldDef fd = new ViewFieldDef();
/*  434 */         fd.m_caption = metaInfo[i][1];
/*  435 */         fd.m_name = metaInfo[i][0];
/*  436 */         fd.m_type = type;
/*  437 */         fd.m_isCustomMeta = true;
/*  438 */         fields.addElement(fd);
/*      */       }
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  443 */       if (SystemUtils.m_verbose)
/*      */       {
/*  445 */         Report.debug(null, null, ignore);
/*      */       }
/*      */     }
/*      */ 
/*  449 */     return filterFields;
/*      */   }
/*      */ 
/*      */   protected ViewFields createShowColumnsFields()
/*      */   {
/*  454 */     ViewFields metaFields = createStandardFields(false, true);
/*      */     try
/*      */     {
/*  458 */       DataResultSet metaSet = this.m_collectionContext.getBatchMetaSet(false, this.m_batchName);
/*  459 */       metaFields.addMetaFields(metaSet);
/*      */     }
/*      */     catch (DataException ignore)
/*      */     {
/*  463 */       if (SystemUtils.m_verbose)
/*      */       {
/*  465 */         Report.debug(null, null, ignore);
/*      */       }
/*      */     }
/*      */ 
/*  469 */     return metaFields;
/*      */   }
/*      */ 
/*      */   protected DataResultSet getNextRows(Vector filter) throws ServiceException, DataException
/*      */   {
/*  474 */     if (this.m_hasFilterChanged)
/*      */     {
/*  477 */       this.m_currentPage = 0;
/*  478 */       if (this.m_maxRows > 0)
/*      */       {
/*  480 */         int maxPages = this.m_numRows / this.m_maxRows;
/*  481 */         if ((this.m_numRows % this.m_maxRows == 0) && (maxPages > 0))
/*      */         {
/*  483 */           --maxPages;
/*      */         }
/*  485 */         this.m_starts = new int[maxPages + 1];
/*      */       }
/*      */       else
/*      */       {
/*  490 */         getRowsFromServer(0);
/*  491 */         getNextRows(filter);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  496 */     return computeRows(filter);
/*      */   }
/*      */ 
/*      */   protected DataResultSet computeRows(Vector filterData) throws ServiceException, DataException
/*      */   {
/*  501 */     if (this.m_currentPage >= this.m_starts.length)
/*      */     {
/*  503 */       return new DataResultSet();
/*      */     }
/*      */ 
/*  506 */     if (this.m_docSet.isEmpty())
/*      */     {
/*  508 */       this.m_currentPage = 0;
/*  509 */       this.m_isLastPage = true;
/*  510 */       return new DataResultSet();
/*      */     }
/*      */ 
/*  513 */     boolean mustCompute = false;
/*  514 */     int startRow = this.m_starts[this.m_currentPage];
/*  515 */     if ((startRow == 0) && (this.m_currentPage > 0))
/*      */     {
/*  517 */       if (filterData.size() == 0)
/*      */       {
/*  520 */         for (int i = 0; i < this.m_starts.length; ++i)
/*      */         {
/*  522 */           this.m_starts[i] = (i * this.m_maxRows);
/*      */         }
/*  524 */         startRow = this.m_starts[this.m_currentPage];
/*      */       }
/*      */       else
/*      */       {
/*  528 */         mustCompute = true;
/*      */       }
/*      */     }
/*      */ 
/*  532 */     if (mustCompute)
/*      */     {
/*  534 */       DataResultSet rset = null;
/*  535 */       for (int i = 0; i <= this.m_currentPage; ++i)
/*      */       {
/*  537 */         startRow = this.m_starts[i];
/*  538 */         rset = getRowSet(filterData, startRow, i);
/*  539 */         if ((i >= this.m_currentPage) || (this.m_starts[(i + 1)] != 0)) {
/*      */           continue;
/*      */         }
/*  542 */         this.m_currentPage = i;
/*  543 */         this.m_isLastPage = true;
/*  544 */         break;
/*      */       }
/*      */ 
/*  547 */       return rset;
/*      */     }
/*  549 */     return getRowSet(filterData, startRow, this.m_currentPage);
/*      */   }
/*      */ 
/*      */   protected DataResultSet getRowSet(Vector filterData, int startRow, int pageNum)
/*      */     throws ServiceException, DataException
/*      */   {
/*  555 */     MultiColumnResultSetFilter rsFilter = new MultiColumnResultSetFilter(this.m_maxRows + 1, this.m_cxt);
/*  556 */     rsFilter.setInfo(this.m_docSet, filterData, startRow);
/*      */ 
/*  558 */     FieldInfo info = new FieldInfo();
/*  559 */     this.m_docSet.getFieldInfo("counter", info);
/*  560 */     int counterIndex = info.m_index;
/*  561 */     Vector v = this.m_docSet.findRow(counterIndex, String.valueOf(startRow));
/*  562 */     if (v == null)
/*      */     {
/*  565 */       getRowsFromServer(startRow);
/*  566 */       v = this.m_docSet.findRow(counterIndex, String.valueOf(startRow));
/*      */     }
/*      */ 
/*  569 */     DataResultSet fSet = new DataResultSet();
/*  570 */     fSet.copyFilteredEx(this.m_docSet, "counter", rsFilter, false);
/*      */ 
/*  572 */     this.m_isLastPage = false;
/*  573 */     while ((rsFilter.m_isSkipRow) || (fSet.getNumRows() < this.m_maxRows))
/*      */     {
/*  575 */       if (this.m_docSet.isRowPresent())
/*      */       {
/*  578 */         String str = this.m_docSet.getStringValue(counterIndex);
/*  579 */         int rowCount = Integer.parseInt(str);
/*  580 */         if (rowCount + 1 == this.m_numRows)
/*      */         {
/*  582 */           this.m_isLastPage = true;
/*      */         }
/*      */       }
/*  585 */       else if (this.m_docSet.previous())
/*      */       {
/*  588 */         String s = this.m_docSet.getStringValue(counterIndex);
/*  589 */         int rc = Integer.parseInt(s);
/*  590 */         if (rc + 1 == this.m_numRows)
/*      */         {
/*  592 */           this.m_docSet.next();
/*  593 */           this.m_isLastPage = true;
/*      */         }
/*      */ 
/*  596 */         this.m_docSet.next();
/*      */       }
/*      */ 
/*  599 */       if (this.m_isLastPage)
/*      */       {
/*      */         break;
/*      */       }
/*      */ 
/*  605 */       startRow = rsFilter.m_prevRow + 1;
/*  606 */       getRowsFromServer(startRow);
/*  607 */       v = this.m_docSet.findRow(counterIndex, String.valueOf(startRow));
/*  608 */       if (v == null)
/*      */         break;
/*  610 */       rsFilter.m_isSkipRow = false;
/*  611 */       DataResultSet tmpSet = new DataResultSet();
/*  612 */       rsFilter.m_maxRows = (this.m_maxRows + 1 - fSet.getNumRows());
/*  613 */       tmpSet.copyFilteredEx(this.m_docSet, "counter", rsFilter, false);
/*  614 */       fSet.merge("counter", tmpSet, false);
/*      */     }
/*      */ 
/*  623 */     if (!fSet.isEmpty())
/*      */     {
/*  625 */       if (fSet.getNumRows() > this.m_maxRows)
/*      */       {
/*  628 */         fSet.last();
/*  629 */         fSet.deleteCurrentRow();
/*      */       }
/*      */ 
/*  632 */       if ((this.m_isLastPage) || ((this.m_docSet.getNumRows() == this.m_numRows) && (!this.m_docSet.isRowPresent())))
/*      */       {
/*  635 */         this.m_isLastPage = true;
/*  636 */         return fSet;
/*      */       }
/*      */ 
/*  639 */       fSet.last();
/*  640 */       String lastRow = fSet.getStringValue(counterIndex);
/*  641 */       int lr = Integer.parseInt(lastRow);
/*  642 */       ++lr;
/*  643 */       if (lr < this.m_numRows)
/*      */       {
/*  645 */         this.m_starts[(++pageNum)] = lr;
/*      */       }
/*      */     }
/*  648 */     return fSet;
/*      */   }
/*      */ 
/*      */   protected void getRowsFromServer(int startRow)
/*      */     throws ServiceException, DataException
/*      */   {
/*  654 */     Properties props = this.m_binder.getLocalData();
/*  655 */     this.m_binder = new DataBinder();
/*  656 */     this.m_binder.setLocalData(props);
/*      */ 
/*  658 */     this.m_binder.putLocal("StartRow", String.valueOf(startRow));
/*  659 */     SharedContext shContext = this.m_collectionContext.getSharedContext();
/*  660 */     shContext.executeService("GET_BATCH_FILE_DOCUMENTS", this.m_binder, false);
/*      */ 
/*  662 */     DataResultSet rset = (DataResultSet)this.m_binder.getResultSet("ExportResults");
/*  663 */     if (rset == null)
/*      */     {
/*  665 */       throw new ServiceException(LocaleResources.getString("apContentInfoForBatchNotFound", this.m_cxt));
/*      */     }
/*      */ 
/*  668 */     setDocSet(rset);
/*      */   }
/*      */ 
/*      */   protected void setDocSet(DataResultSet rset)
/*      */     throws DataException
/*      */   {
/*  674 */     this.m_numRows = getIntValue("NumRows", 0);
/*  675 */     this.m_maxRows = getIntValue("MaxRowsPerPage", 50);
/*  676 */     int startRow = getIntValue("StartRow", 0);
/*      */ 
/*  680 */     Vector finfo = ResultSetUtils.createFieldInfo(new String[] { "counter", "editStatus" }, 30);
/*  681 */     rset.mergeFieldsWithFlags(finfo, 2);
/*  682 */     int counterIndex = rset.getNumFields() - 2;
/*  683 */     int statusIndex = counterIndex + 1;
/*      */ 
/*  685 */     if (this.m_docSet.isEmpty())
/*      */     {
/*  688 */       this.m_docSet.copyFieldInfo(rset);
/*      */     }
/*      */ 
/*  691 */     for (this.m_docSet.first(); this.m_docSet.isRowPresent(); this.m_docSet.next())
/*      */     {
/*  693 */       String str = this.m_docSet.getStringValue(counterIndex);
/*  694 */       int docCount = Integer.parseInt(str);
/*      */ 
/*  696 */       if (startRow <= docCount) {
/*      */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  702 */     int currentRow = this.m_docSet.getCurrentRow();
/*  703 */     int counter = startRow;
/*  704 */     for (; rset.isRowPresent(); ++counter)
/*      */     {
/*  706 */       String counterStr = String.valueOf(counter);
/*  707 */       Vector rowValues = this.m_docSet.findRow(counterIndex, counterStr);
/*  708 */       if (rowValues == null)
/*      */       {
/*  714 */         rset.setCurrentValue(counterIndex, String.valueOf(counterStr));
/*  715 */         rset.setCurrentValue(statusIndex, "ARCHIVED");
/*      */ 
/*  717 */         Vector v = rset.getCurrentRowValues();
/*  718 */         this.m_docSet.insertRowAt(v, currentRow++);
/*      */       }
/*  704 */       rset.next();
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataBinder refresh(String rsetName, Vector filterData, DataResultSet defSet)
/*      */     throws ServiceException
/*      */   {
/*  728 */     Vector filter = new IdcVector();
/*  729 */     int num = filterData.size();
/*  730 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  732 */       FilterData data = (FilterData)filterData.elementAt(i);
/*  733 */       if (data.m_isUsed != true)
/*      */         continue;
/*  735 */       filter.addElement(data);
/*      */     }
/*      */ 
/*  739 */     DataResultSet drset = null;
/*      */     try
/*      */     {
/*  742 */       drset = getNextRows(filter);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  746 */       throw new ServiceException(LocaleResources.getString("apUnableToRetrieveRecords", this.m_cxt));
/*      */     }
/*      */ 
/*  749 */     if (drset == null)
/*      */     {
/*  751 */       drset = new DataResultSet();
/*      */     }
/*      */ 
/*  754 */     if (drset.isEmpty())
/*      */     {
/*  756 */       this.m_currentPage = 0;
/*  757 */       this.m_isLastPage = true;
/*      */     }
/*      */ 
/*  760 */     enableDisable();
/*      */ 
/*  762 */     this.m_binder.addResultSet("ExportResults", drset);
/*  763 */     return this.m_binder;
/*      */   }
/*      */ 
/*      */   public SharedContext getSharedContext()
/*      */   {
/*  768 */     return this.m_collectionContext.getSharedContext();
/*      */   }
/*      */ 
/*      */   public DataResultSet getMetaData()
/*      */   {
/*  773 */     return this.m_collectionContext.getBatchMetaSet(false, this.m_batchName);
/*      */   }
/*      */ 
/*      */   public void actionPerformed(ActionEvent e)
/*      */   {
/*  779 */     String cmd = e.getActionCommand();
/*  780 */     if (cmd.equals("close"))
/*      */     {
/*  782 */       closeDialog();
/*      */     }
/*  784 */     else if (cmd.equals("import"))
/*      */     {
/*  786 */       importSelected();
/*      */     }
/*  788 */     else if (cmd.equals("delete"))
/*      */     {
/*  790 */       markSelectedDocument("DELETED", false);
/*      */     }
/*  792 */     else if (cmd.equals("undo"))
/*      */     {
/*  794 */       markSelectedDocument("DONE", true);
/*      */     }
/*  796 */     else if (cmd.equals("apply"))
/*      */     {
/*  798 */       applyChanges();
/*      */     }
/*  800 */     else if (cmd.equals("refresh"))
/*      */     {
/*  802 */       refreshRowsFromServer();
/*      */     }
/*  804 */     else if (cmd.equals("first"))
/*      */     {
/*  806 */       refreshRows(0);
/*      */     }
/*  808 */     else if (cmd.equals("last"))
/*      */     {
/*  810 */       refreshRows(1);
/*      */     }
/*  812 */     else if (cmd.equals("previous"))
/*      */     {
/*  814 */       refreshRows(2);
/*      */     } else {
/*  816 */       if (!cmd.equals("next"))
/*      */         return;
/*  818 */       refreshRows(3);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void closeDialog()
/*      */   {
/*  824 */     if ((this.m_dirtySet != null) && (!this.m_dirtySet.isEmpty()))
/*      */     {
/*  826 */       int result = MessageBox.doMessage(this.m_helper.m_exchange.m_sysInterface, IdcMessageFactory.lc("apVerifyCloseWithoutSaving", new Object[0]), 8);
/*      */ 
/*  828 */       switch (result)
/*      */       {
/*      */       case 2:
/*  831 */         if (!applyChanges())
/*      */         {
/*  833 */           return;
/*      */         }
/*      */ 
/*      */       case 3:
/*  838 */         break;
/*      */       case 0:
/*  842 */         return;
/*      */       case 1:
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  846 */     this.m_helper.close();
/*      */   }
/*      */ 
/*      */   protected void importSelected()
/*      */   {
/*  851 */     int index = this.m_docView.getSelectedIndex();
/*  852 */     if (index < 0)
/*      */     {
/*  854 */       return;
/*      */     }
/*      */ 
/*  857 */     DataBinder binder = new DataBinder();
/*      */ 
/*  859 */     String batchFile = this.m_binder.getLocal("aBatchFile");
/*  860 */     binder.putLocal("aBatchFile", batchFile);
/*      */ 
/*  863 */     Properties props = this.m_docView.getDataAt(index);
/*  864 */     binder.putLocal("dID", props.getProperty("dID"));
/*  865 */     binder.putLocal("dDocName", props.getProperty("dDocName"));
/*      */ 
/*  867 */     boolean isFailed = false;
/*      */     try
/*      */     {
/*  870 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/*  871 */       shContext.executeService("IMPORT_DOCUMENT", binder, false);
/*      */     }
/*      */     catch (Exception statusMsg)
/*      */     {
/*      */       String statusMsg;
/*      */       String docName;
/*  875 */       isFailed = true;
/*  876 */       this.m_collectionContext.reportError(e, LocaleResources.getString("apUnableToImportFile", this.m_cxt));
/*      */     }
/*      */     finally
/*      */     {
/*      */       String statusMsg;
/*      */       String docName;
/*  881 */       if (!isFailed)
/*      */       {
/*  883 */         String statusMsg = binder.getLocal("StatusMessage");
/*  884 */         if (statusMsg == null)
/*      */         {
/*  886 */           String docName = props.getProperty("dDocName");
/*  887 */           statusMsg = LocaleResources.getString("apSuccessfullyImportedFile", this.m_cxt, docName);
/*      */         }
/*      */ 
/*  890 */         this.m_collectionContext.reportError(statusMsg);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void markSelectedDocument(String status, boolean isUndoDirty)
/*      */   {
/*  897 */     String id = this.m_docView.getSelectedObj();
/*  898 */     if (id == null)
/*      */     {
/*  900 */       return;
/*      */     }
/*      */ 
/*  903 */     UdlPanel docList = this.m_docView.getList();
/*  904 */     DataResultSet docSet = (DataResultSet)docList.getResultSet();
/*      */ 
/*  906 */     if (this.m_dirtySet == null)
/*      */     {
/*  908 */       if (isUndoDirty)
/*      */       {
/*  911 */         return;
/*      */       }
/*      */ 
/*  914 */       this.m_dirtySet = new DataResultSet();
/*  915 */       this.m_dirtySet.copyFieldInfo(docSet);
/*      */     }
/*      */     try
/*      */     {
/*  919 */       FieldInfo[] info = ResultSetUtils.createInfoList(docSet, new String[] { "editStatus", "counter" }, true);
/*      */ 
/*  923 */       int stIndex = info[0].m_index;
/*  924 */       int counterIndex = info[1].m_index;
/*      */ 
/*  928 */       Vector row = docList.getSelectedAsResultSet().getRowValues(0);
/*  929 */       if (row == null)
/*      */       {
/*  932 */         return;
/*      */       }
/*      */ 
/*  935 */       String counterID = (String)row.elementAt(counterIndex);
/*  936 */       Vector values = this.m_docSet.findRow(counterIndex, counterID);
/*  937 */       if (values == null)
/*      */       {
/*  940 */         this.m_collectionContext.reportError(LocaleResources.getString("apUnableToSetStatus", this.m_cxt, status) + LocaleResources.getString("apUnableToFindRecordInMasterList", this.m_cxt, counterID));
/*      */ 
/*  943 */         return;
/*      */       }
/*  945 */       values.setElementAt(status, stIndex);
/*  946 */       row.setElementAt(status, stIndex);
/*      */ 
/*  949 */       if (isUndoDirty)
/*      */       {
/*  951 */         values = this.m_dirtySet.findRow(counterIndex, counterID);
/*  952 */         if (values != null)
/*      */         {
/*  954 */           this.m_dirtySet.deleteCurrentRow();
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  959 */         this.m_dirtySet.addRow(values);
/*      */       }
/*      */ 
/*  962 */       boolean enable = this.m_dirtySet.getNumRows() > 0;
/*  963 */       enableButton("apply", enable);
/*      */ 
/*  965 */       refreshRows(4);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  969 */       String msg = LocaleResources.getString("apUnableToMarkDeleted", this.m_cxt);
/*  970 */       if (isUndoDirty)
/*      */       {
/*  972 */         msg = LocaleResources.getString("apUnableToUndoDelete", this.m_cxt);
/*      */       }
/*  974 */       this.m_collectionContext.reportError(e, msg);
/*      */     }
/*      */   }
/*      */ 
/*      */   protected String getIdFields()
/*      */   {
/*  980 */     return "dID";
/*      */   }
/*      */ 
/*      */   protected boolean applyChanges()
/*      */   {
/*  987 */     if (this.m_dirtySet == null)
/*      */     {
/*  989 */       return true;
/*      */     }
/*      */ 
/*  992 */     this.m_binder.addResultSet("DeletedRows", this.m_dirtySet);
/*      */     try
/*      */     {
/*  995 */       SharedContext shContext = this.m_collectionContext.getSharedContext();
/*  996 */       shContext.executeService(m_deleteService, this.m_binder, false);
/*  997 */       this.m_dirtySet = null;
/*  998 */       enableButton("apply", false);
/*      */ 
/* 1001 */       DataResultSet drset = (DataResultSet)this.m_binder.getResultSet("ExportResults");
/* 1002 */       this.m_currentPage = 0;
/*      */ 
/* 1004 */       this.m_docSet.reset();
/* 1005 */       setDocSet(drset);
/*      */ 
/* 1007 */       this.m_docView.refreshView();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1011 */       this.m_collectionContext.reportError(e);
/* 1012 */       return false;
/*      */     }
/* 1014 */     return true;
/*      */   }
/*      */ 
/*      */   protected void refreshRows(int type)
/*      */   {
/* 1019 */     switch (type)
/*      */     {
/*      */     case 0:
/* 1022 */       this.m_currentPage = 0;
/* 1023 */       break;
/*      */     case 1:
/* 1026 */       this.m_currentPage = (this.m_numRows / this.m_maxRows);
/* 1027 */       if ((this.m_numRows % this.m_maxRows == 0) && (this.m_currentPage > 0))
/*      */       {
/* 1029 */         this.m_currentPage -= 1; } break;
/*      */     case 2:
/* 1034 */       if (this.m_currentPage > 0)
/*      */       {
/* 1036 */         this.m_currentPage -= 1; } break;
/*      */     case 3:
/* 1041 */       if (this.m_currentPage * this.m_maxRows + this.m_maxRows < this.m_numRows)
/*      */       {
/* 1043 */         this.m_currentPage += 1;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1050 */       this.m_hasFilterChanged = false;
/* 1051 */       this.m_docView.refreshView();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1055 */       this.m_collectionContext.reportError(e);
/*      */     }
/*      */     finally
/*      */     {
/* 1059 */       this.m_hasFilterChanged = true;
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void refreshRowsFromServer()
/*      */   {
/* 1065 */     DataResultSet oldDocSet = this.m_docSet;
/* 1066 */     DataResultSet oldDirtySet = this.m_dirtySet;
/*      */ 
/* 1068 */     this.m_docSet = new DataResultSet();
/* 1069 */     this.m_dirtySet = null;
/*      */ 
/* 1071 */     this.m_maxRows = 0;
/* 1072 */     boolean hasFailed = false;
/*      */     try
/*      */     {
/* 1075 */       this.m_currentPage = 0;
/* 1076 */       this.m_docView.refreshView();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/* 1080 */       hasFailed = true;
/* 1081 */       this.m_collectionContext.reportError(e);
/*      */     }
/*      */     finally
/*      */     {
/* 1085 */       if (hasFailed)
/*      */       {
/* 1088 */         this.m_docSet = oldDocSet;
/* 1089 */         this.m_dirtySet = oldDirtySet;
/*      */       }
/*      */       else
/*      */       {
/* 1093 */         enableButton("apply", false);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void checkSelection()
/*      */   {
/* 1100 */     int index = this.m_docView.getSelectedIndex();
/* 1101 */     if (index < 0)
/*      */     {
/* 1103 */       enableButton("delete", false);
/* 1104 */       enableButton("undo", false);
/* 1105 */       return;
/*      */     }
/*      */ 
/* 1108 */     Properties props = this.m_docView.getDataAt(index);
/*      */ 
/* 1111 */     String status = props.getProperty("editStatus");
/* 1112 */     boolean isDeleted = status.equals("DELETED");
/* 1113 */     enableButton("delete", !isDeleted);
/* 1114 */     enableButton("undo", isDeleted);
/*      */   }
/*      */ 
/*      */   protected void enableDisable()
/*      */   {
/* 1119 */     boolean isFirst = this.m_currentPage == 0;
/* 1120 */     enableButton("first", !isFirst);
/* 1121 */     enableButton("previous", !isFirst);
/*      */ 
/* 1123 */     enableButton("next", !this.m_isLastPage);
/* 1124 */     enableButton("last", !this.m_isLastPage);
/*      */   }
/*      */ 
/*      */   protected int getIntValue(String key, int defaultValue)
/*      */   {
/* 1132 */     String str = this.m_binder.getLocal(key);
/* 1133 */     int value = defaultValue;
/* 1134 */     if ((str != null) && (str.length() > 0))
/*      */     {
/*      */       try
/*      */       {
/* 1138 */         value = Integer.parseInt(str);
/*      */       }
/*      */       catch (Throwable ignore)
/*      */       {
/* 1142 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1144 */           Report.debug("systemparse", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/* 1148 */     return value;
/*      */   }
/*      */ 
/*      */   public Vector getFilterData()
/*      */   {
/* 1153 */     return this.m_docView.buildFilterEx(true);
/*      */   }
/*      */ 
/*      */   protected void enableButton(String name, boolean isEnabled)
/*      */   {
/* 1158 */     JButton btn = (JButton)this.m_buttonMap.get(name);
/* 1159 */     if (btn == null)
/*      */       return;
/* 1161 */     btn.setEnabled(isEnabled);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1167 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 92435 $";
/*      */   }
/*      */ 
/*      */   static class MultiColumnResultSetFilter
/*      */     implements ResultSetFilter
/*      */   {
/* 1173 */     public FilterData[] m_filterData = null;
/* 1174 */     public FieldInfo[] m_fieldInfo = null;
/*      */ 
/* 1176 */     public int m_maxRows = 0;
/* 1177 */     public int m_prevRow = -1;
/* 1178 */     public boolean m_isSkipRow = false;
/*      */ 
/* 1180 */     public ExecutionContext m_cxt = null;
/*      */ 
/*      */     public MultiColumnResultSetFilter(int maxRows, ExecutionContext cxt)
/*      */     {
/* 1184 */       this.m_maxRows = maxRows;
/* 1185 */       this.m_cxt = cxt;
/*      */     }
/*      */ 
/*      */     public void setInfo(ResultSet rset, Vector filterData, int startRow)
/*      */       throws DataException
/*      */     {
/* 1191 */       this.m_prevRow = (startRow - 1);
/* 1192 */       this.m_isSkipRow = false;
/*      */ 
/* 1194 */       int num = filterData.size();
/* 1195 */       String[] fields = new String[num];
/* 1196 */       this.m_filterData = new FilterData[num];
/*      */ 
/* 1198 */       String wildCards = SharedObjects.getEnvironmentValue("DatabaseWildcards");
/* 1199 */       if (wildCards == null)
/*      */       {
/* 1201 */         wildCards = "%_";
/*      */       }
/*      */ 
/* 1204 */       for (int i = 0; i < num; ++i)
/*      */       {
/* 1206 */         FilterData fDef = (FilterData)filterData.elementAt(i);
/* 1207 */         this.m_filterData[i] = fDef;
/* 1208 */         fields[i] = fDef.m_fieldDef.m_name;
/*      */ 
/* 1211 */         String pattern = convertWildCards(this.m_filterData[i].getValueAt(0), wildCards);
/* 1212 */         this.m_filterData[i].m_values.setElementAt(pattern, 0);
/*      */       }
/*      */ 
/* 1215 */       this.m_fieldInfo = ResultSetUtils.createInfoList(rset, fields, true);
/*      */     }
/*      */ 
/*      */     protected String convertWildCards(String pattern, String wildCards)
/*      */     {
/* 1221 */       if ((pattern == null) || (wildCards.equals("*?")) || (wildCards.length() < 2))
/*      */       {
/* 1223 */         return pattern.toLowerCase();
/*      */       }
/*      */ 
/* 1226 */       char wMany = wildCards.charAt(0);
/* 1227 */       char wOne = wildCards.charAt(1);
/*      */ 
/* 1229 */       int len = pattern.length();
/* 1230 */       String newPattern = "";
/* 1231 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1233 */         char c = pattern.charAt(i);
/* 1234 */         if (c == wMany)
/*      */         {
/* 1236 */           c = '*';
/*      */         }
/* 1238 */         else if (c == wOne)
/*      */         {
/* 1240 */           c = '?';
/*      */         }
/* 1242 */         newPattern = newPattern + Character.toLowerCase(c);
/*      */       }
/* 1244 */       return newPattern;
/*      */     }
/*      */ 
/*      */     public int checkRow(String val, int curNumRows, Vector row)
/*      */     {
/* 1249 */       if (curNumRows == this.m_maxRows)
/*      */       {
/* 1251 */         return -1;
/*      */       }
/*      */ 
/* 1254 */       if ((this.m_filterData == null) || (this.m_filterData.length == 0))
/*      */       {
/* 1256 */         return 1;
/*      */       }
/*      */ 
/* 1260 */       int counter = Integer.parseInt(val);
/* 1261 */       if (counter == this.m_prevRow + 1)
/*      */       {
/* 1263 */         this.m_prevRow = counter;
/*      */       }
/*      */       else
/*      */       {
/* 1267 */         this.m_isSkipRow = true;
/* 1268 */         return -1;
/*      */       }
/*      */ 
/* 1273 */       int len = this.m_filterData.length;
/* 1274 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1276 */         FieldInfo finfo = this.m_fieldInfo[i];
/* 1277 */         if (finfo == null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/* 1282 */         FilterData fd = this.m_filterData[i];
/* 1283 */         String type = fd.m_fieldDef.m_type;
/* 1284 */         String value = (String)row.elementAt(finfo.m_index);
/*      */ 
/* 1286 */         boolean result = false;
/* 1287 */         if ((type.equalsIgnoreCase("int")) || (type.equalsIgnoreCase("date")))
/*      */         {
/* 1289 */           int num = fd.m_values.size();
/* 1290 */           for (int j = 0; j < num; ++j)
/*      */           {
/* 1292 */             String operator = fd.getOperatorAt(j);
/* 1293 */             String pattern = fd.getValueAt(j);
/*      */ 
/* 1295 */             if (pattern == null) continue; if (pattern.length() == 0)
/*      */             {
/*      */               continue;
/*      */             }
/*      */ 
/* 1300 */             long p = 0L;
/* 1301 */             long v = 0L;
/* 1302 */             if (type.equalsIgnoreCase("int"))
/*      */             {
/*      */               try
/*      */               {
/* 1306 */                 p = Long.parseLong(pattern);
/* 1307 */                 v = Long.parseLong(value);
/*      */               }
/*      */               catch (Exception e)
/*      */               {
/* 1311 */                 return 0;
/*      */               }
/*      */             }
/* 1314 */             else if (type.equalsIgnoreCase("date"))
/*      */             {
/*      */               try
/*      */               {
/* 1318 */                 Date pDte = LocaleResources.parseDate(pattern, this.m_cxt);
/* 1319 */                 p = pDte.getTime();
/* 1320 */                 Date vDte = LocaleResources.parseDate(value, this.m_cxt);
/* 1321 */                 v = vDte.getTime();
/*      */               }
/*      */               catch (Exception e)
/*      */               {
/* 1325 */                 return 0;
/*      */               }
/*      */             }
/*      */ 
/* 1329 */             result = compareInts(v, p, operator);
/* 1330 */             if (!result) {
/*      */               break;
/*      */             }
/*      */           }
/*      */ 
/*      */         }
/*      */         else
/*      */         {
/* 1338 */           String pattern = this.m_filterData[i].getValueAt(0);
/* 1339 */           value = value.toLowerCase();
/* 1340 */           result = StringUtils.match(value, pattern, true);
/*      */         }
/*      */ 
/* 1343 */         if (!result)
/*      */         {
/* 1345 */           return 0;
/*      */         }
/*      */       }
/* 1348 */       return 1;
/*      */     }
/*      */ 
/*      */     protected boolean compareInts(long v, long p, String operator)
/*      */     {
/* 1353 */       boolean result = false;
/* 1354 */       if (operator.equals(">"))
/*      */       {
/* 1356 */         result = v > p;
/*      */       }
/* 1358 */       else if (operator.equals(">="))
/*      */       {
/* 1360 */         result = v >= p;
/*      */       }
/* 1362 */       else if (operator.equals("<"))
/*      */       {
/* 1364 */         result = v < p;
/*      */       }
/* 1366 */       else if (operator.equals("<="))
/*      */       {
/* 1368 */         result = v <= p;
/*      */       }
/*      */       else
/*      */       {
/* 1372 */         result = v == p;
/*      */       }
/* 1374 */       return result;
/*      */     }
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.archiver.EditBatchDlg
 * JD-Core Version:    0.5.4
 */