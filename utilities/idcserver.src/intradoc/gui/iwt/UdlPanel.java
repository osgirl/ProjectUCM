/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.IdcComparator;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.gui.CustomLabel;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.Image;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.MouseAdapter;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.lang.reflect.Constructor;
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JMenuItem;
/*     */ import javax.swing.JPanel;
/*     */ import javax.swing.JPopupMenu;
/*     */ import javax.swing.JTable;
/*     */ import javax.swing.JWindow;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ import javax.swing.table.TableColumn;
/*     */ import javax.swing.table.TableColumnModel;
/*     */ import javax.swing.table.TableModel;
/*     */ 
/*     */ public class UdlPanel extends PanePanel
/*     */ {
/*     */   public static final int HIGHLIGHT = 0;
/*     */   public static final int CHECKBOX = 1;
/*  74 */   protected String m_resultSetName = null;
/*  75 */   protected DataResultSet m_rset = null;
/*  76 */   protected int m_lastSelectedIndex = -1;
/*     */ 
/*  78 */   protected String m_title = null;
/*  79 */   protected CustomLabel m_titleLabel = null;
/*  80 */   protected int m_rowWidth = 20;
/*     */   public UserDrawList m_list;
/*     */   protected boolean m_hasSort;
/*  85 */   protected Vector m_controlComponents = null;
/*     */ 
/*  87 */   protected Hashtable<String, DisplayStringCallback> m_displayCallbacksMap = null;
/*  88 */   protected boolean m_internalEnableDisable = false;
/*     */ 
/*  91 */   protected JPanel m_btnPanel = null;
/*     */ 
/*  94 */   protected int m_idIndex = 0;
/*  95 */   protected String m_idColumnName = null;
/*  96 */   protected Hashtable<String, ColumnInfo> m_columnInfoMap = new Hashtable();
/*     */ 
/*  99 */   protected String m_stateColumn = null;
/* 100 */   protected int m_stateIndex = -1;
/*     */ 
/* 106 */   protected Hashtable m_comparators = new Hashtable();
/*     */ 
/* 108 */   protected JPopupMenu m_popup = null;
/* 109 */   protected JWindow m_hoverPopup = null;
/*     */ 
/* 111 */   public Dimension m_iconSize = null;
/* 112 */   protected String m_iconField = null;
/* 113 */   protected Hashtable m_icons = new Hashtable();
/* 114 */   public IdcDateFormat m_dateFormat = null;
/*     */ 
/*     */   public UdlPanel(String title, DataRetrievalHelper dataHelper, int width, int rows, String resultSetName, boolean useColumns)
/*     */   {
/* 129 */     this.m_resultSetName = resultSetName;
/* 130 */     this.m_title = title;
/*     */ 
/* 132 */     int flags = 0;
/* 133 */     if (useColumns)
/*     */     {
/* 135 */       flags = 2;
/*     */     }
/*     */ 
/* 138 */     this.m_list = new UserDrawList(new ColumnInfo[0], flags);
/* 139 */     this.m_displayCallbacksMap = new Hashtable();
/*     */ 
/* 141 */     this.m_controlComponents = new IdcVector();
/* 142 */     this.m_rowWidth = width;
/* 143 */     this.m_list.setVisibleRowCount(rows);
/* 144 */     this.m_hasSort = true;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/* 149 */     setLayout(new BorderLayout());
/* 150 */     JPanel wrapper = new PanePanel();
/* 151 */     add("North", wrapper);
/* 152 */     wrapper.setLayout(new BorderLayout());
/* 153 */     if (this.m_title != null)
/*     */     {
/* 155 */       this.m_titleLabel = new CustomLabel(this.m_title, 1);
/* 156 */       wrapper.add("West", this.m_titleLabel);
/*     */     }
/* 158 */     this.m_btnPanel = new PanePanel();
/* 159 */     wrapper.add("East", this.m_btnPanel);
/*     */ 
/* 161 */     this.m_list.init();
/*     */ 
/* 163 */     if (this.m_hasSort)
/*     */     {
/*     */       try
/*     */       {
/* 167 */         Class tableClass = JTable.class;
/* 168 */         Class rowSorterClass = Class.forName("javax.swing.RowSorter");
/* 169 */         Class idcRowSorterClass = Class.forName("intradoc.gui.iwt.IdcTableRowSorter");
/* 170 */         Constructor constructor = idcRowSorterClass.getConstructor(new Class[] { TableModel.class });
/* 171 */         Object tableRowSorter = constructor.newInstance(new Object[] { this.m_list.m_table.getModel() });
/* 172 */         Method initMethod = idcRowSorterClass.getMethod("init", new Class[] { UdlPanel.class });
/* 173 */         initMethod.invoke(tableRowSorter, new Object[] { this });
/* 174 */         Method setRowSorterMethod = tableClass.getMethod("setRowSorter", new Class[] { rowSorterClass });
/* 175 */         setRowSorterMethod.invoke(this.m_list.m_table, new Object[] { tableRowSorter });
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 183 */     add("Center", this.m_list);
/*     */   }
/*     */ 
/*     */   public void setDateFormat(IdcDateFormat fmt)
/*     */   {
/* 188 */     this.m_dateFormat = fmt;
/*     */   }
/*     */ 
/*     */   public JPanel getButtonPanel()
/*     */   {
/* 193 */     return this.m_btnPanel;
/*     */   }
/*     */ 
/*     */   public Hashtable getDisplayCallbackMap()
/*     */   {
/* 198 */     return this.m_displayCallbacksMap;
/*     */   }
/*     */ 
/*     */   public void setDisplayCallbackMap(Hashtable map)
/*     */   {
/* 203 */     this.m_displayCallbacksMap = map;
/*     */   }
/*     */ 
/*     */   public void useDefaultListener()
/*     */   {
/* 208 */     ListSelectionListener iListener = new ListSelectionListener()
/*     */     {
/*     */       public void valueChanged(ListSelectionEvent e)
/*     */       {
/* 212 */         int index = UdlPanel.this.getSelectedIndex();
/* 213 */         boolean isSelected = true;
/* 214 */         if (index < 0)
/*     */         {
/* 216 */           isSelected = false;
/*     */         }
/* 218 */         UdlPanel.this.enableDisable(isSelected);
/*     */       }
/*     */     };
/* 221 */     this.m_list.m_table.getSelectionModel().addListSelectionListener(iListener);
/* 222 */     this.m_internalEnableDisable = true;
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener listener)
/*     */   {
/* 227 */     this.m_list.addItemListener(listener);
/*     */   }
/*     */ 
/*     */   public void addListSelectionListener(ListSelectionListener listener)
/*     */   {
/* 232 */     this.m_list.m_table.getSelectionModel().addListSelectionListener(listener);
/*     */   }
/*     */ 
/*     */   public int refreshList(DataBinder binder, String selectedObj)
/*     */   {
/* 237 */     DataResultSet drset = (DataResultSet)binder.getResultSet(this.m_resultSetName);
/* 238 */     if (drset == null)
/*     */     {
/* 240 */       Report.trace(null, "Unable to find table " + this.m_resultSetName + " in data binder.", null);
/* 241 */       return -1;
/*     */     }
/* 243 */     return refreshList(drset, selectedObj);
/*     */   }
/*     */ 
/*     */   public Vector refreshListEx(DataBinder binder, String[] selectedObjs)
/*     */   {
/* 248 */     DataResultSet drset = (DataResultSet)binder.getResultSet(this.m_resultSetName);
/* 249 */     if (drset == null)
/*     */     {
/* 251 */       Report.trace(null, "Unable to find table " + this.m_resultSetName + " in data binder.", null);
/* 252 */       return null;
/*     */     }
/* 254 */     return refreshListEx(drset, selectedObjs);
/*     */   }
/*     */ 
/*     */   public int refreshList(ResultSet rset, String selectedObj)
/*     */   {
/* 259 */     String[] selectedObjs = new String[1];
/* 260 */     selectedObjs[0] = selectedObj;
/*     */ 
/* 262 */     int selIndex = -1;
/* 263 */     Vector selIndices = refreshListEx(rset, selectedObjs);
/* 264 */     if (selIndices.size() > 0)
/*     */     {
/* 266 */       Integer sel = (Integer)selIndices.elementAt(0);
/* 267 */       selIndex = sel.intValue();
/*     */     }
/*     */ 
/* 270 */     IdcDateFormat fmt = this.m_rset.getDateFormat();
/* 271 */     if (fmt != null)
/*     */     {
/* 273 */       setDateFormat(fmt);
/*     */     }
/* 275 */     return selIndex;
/*     */   }
/*     */ 
/*     */   public int reloadList(String selectedObj)
/*     */   {
/* 280 */     return refreshList(this.m_rset, selectedObj);
/*     */   }
/*     */ 
/*     */   public Vector refreshListEx(ResultSet rset, String[] selectedObjs)
/*     */   {
/* 285 */     this.m_lastSelectedIndex = this.m_list.getSelectedIndex();
/*     */ 
/* 287 */     if (this.m_internalEnableDisable)
/*     */     {
/* 289 */       enableDisable(false);
/*     */     }
/*     */ 
/* 293 */     DataResultSet drset = null;
/* 294 */     if (rset instanceof DataResultSet)
/*     */     {
/* 296 */       drset = (DataResultSet)rset;
/*     */     }
/*     */     else
/*     */     {
/* 300 */       drset = new DataResultSet();
/* 301 */       drset.copy(rset);
/*     */     }
/*     */ 
/* 304 */     if ((this.m_idIndex < 0) && (this.m_idColumnName != null))
/*     */     {
/* 306 */       this.m_idIndex = drset.getFieldInfoIndex(this.m_idColumnName);
/* 307 */       if (this.m_idIndex < 0)
/*     */       {
/* 309 */         this.m_idIndex = 0;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 314 */     int size = 0;
/* 315 */     if (selectedObjs != null)
/*     */     {
/* 317 */       size = selectedObjs.length;
/*     */     }
/* 319 */     Vector indicesToSelect = new IdcVector();
/* 320 */     int nrows = drset.getNumRows();
/* 321 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 323 */       String selObj = selectedObjs[i];
/* 324 */       Vector row = drset.findRow(this.m_idIndex, selObj);
/* 325 */       if (row == null)
/*     */         continue;
/* 327 */       int index = drset.getCurrentRow();
/* 328 */       indicesToSelect.addElement(new Integer(index));
/*     */     }
/*     */ 
/* 332 */     TableColumnModel columnModel = this.m_list.m_table.getColumnModel();
/* 333 */     for (int i = 0; i < columnModel.getColumnCount(); ++i)
/*     */     {
/* 335 */       TableColumn c = columnModel.getColumn(i);
/* 336 */       FieldInfo fi = new FieldInfo();
/* 337 */       drset.getFieldInfo((String)c.getIdentifier(), fi);
/* 338 */       if (fi.m_index == -1)
/*     */       {
/* 340 */         ColumnInfo colInfo = getColumnInfo((String)c.getIdentifier());
/* 341 */         if (colInfo.m_isCheckbox)
/*     */         {
/* 343 */           fi.m_name = ((String)c.getIdentifier());
/* 344 */           ArrayList l = new ArrayList();
/* 345 */           l.add(fi);
/* 346 */           drset.mergeFieldsWithFlags(l, 2);
/*     */         }
/*     */       }
/*     */ 
/* 350 */       c.setModelIndex(fi.m_index);
/*     */     }
/*     */ 
/* 353 */     this.m_rset = drset;
/* 354 */     IdcDateFormat fmt = this.m_rset.getDateFormat();
/* 355 */     if (fmt != null)
/*     */     {
/* 357 */       setDateFormat(fmt);
/*     */     }
/*     */ 
/* 360 */     DataResultSetTableModel dataModel = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 361 */     dataModel.m_rset = this.m_rset;
/* 362 */     dataModel.m_displayCallbacksMap = this.m_displayCallbacksMap;
/* 363 */     dataModel.fireTableDataChanged();
/* 364 */     if (indicesToSelect.size() > 0)
/*     */     {
/* 366 */       if (this.m_internalEnableDisable)
/*     */       {
/* 368 */         enableDisable(true);
/*     */       }
/*     */ 
/* 371 */       setSelectedIndexes(indicesToSelect);
/*     */     }
/*     */     else
/*     */     {
/* 375 */       if (this.m_lastSelectedIndex >= nrows)
/*     */       {
/* 377 */         this.m_lastSelectedIndex = (nrows - 1);
/*     */       }
/*     */ 
/* 380 */       if (this.m_lastSelectedIndex >= 0)
/*     */       {
/* 382 */         setSelectedIndex(this.m_lastSelectedIndex);
/*     */       }
/*     */     }
/*     */ 
/* 386 */     return indicesToSelect;
/*     */   }
/*     */ 
/*     */   public void enableDisable(boolean enable)
/*     */   {
/* 391 */     int size = this.m_controlComponents.size();
/* 392 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 394 */       Object obj = this.m_controlComponents.elementAt(i);
/* 395 */       if (obj instanceof Component)
/*     */       {
/* 397 */         Component cmpt = (Component)obj;
/* 398 */         cmpt.setEnabled(enable);
/*     */       } else {
/* 400 */         if (!obj instanceof JMenuItem)
/*     */           continue;
/* 402 */         JMenuItem menuItem = (JMenuItem)obj;
/* 403 */         menuItem.setEnabled(enable);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public JButton addButton(String label, boolean isControlled)
/*     */   {
/* 410 */     JButton btn = new JButton(label);
/* 411 */     if (isControlled)
/*     */     {
/* 413 */       this.m_controlComponents.addElement(btn);
/*     */     }
/* 415 */     return btn;
/*     */   }
/*     */ 
/*     */   public void addControlComponent(JMenuItem cmpt)
/*     */   {
/* 420 */     this.m_controlComponents.addElement(cmpt);
/*     */   }
/*     */ 
/*     */   public void addControlComponent(Component cmpt)
/*     */   {
/* 425 */     this.m_controlComponents.addElement(cmpt);
/*     */   }
/*     */ 
/*     */   public void setVisibleColumns(String clmns)
/*     */   {
/* 430 */     setVisibleColumnsEx(clmns, null);
/*     */   }
/*     */ 
/*     */   public void setVisibleColumnsEx(String clmns, Vector labels)
/*     */   {
/* 435 */     boolean hasLabels = labels != null;
/* 436 */     Vector v = StringUtils.parseArrayEx(clmns, ',', ',', true);
/*     */ 
/* 439 */     Hashtable oldColumnsMap = new Hashtable();
/* 440 */     int count = this.m_list.getColumnCount();
/* 441 */     ColumnInfo[] oldColumns = new ColumnInfo[count];
/* 442 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 444 */       ColumnInfo info = this.m_list.getColumnInfo(i);
/* 445 */       oldColumns[i] = info;
/* 446 */       oldColumnsMap.put(info.m_fieldId, info);
/*     */     }
/*     */ 
/* 449 */     int ncols = v.size();
/* 450 */     Hashtable newColumns = new Hashtable();
/* 451 */     this.m_list.m_tableDataModel.m_checkboxFields = new HashSet();
/* 452 */     for (int i = 0; i < ncols; ++i)
/*     */     {
/* 454 */       String name = (String)v.elementAt(i);
/*     */ 
/* 456 */       ColumnInfo info = getColumnInfo(name);
/* 457 */       if (info == null)
/*     */       {
/* 459 */         info = new ColumnInfo(name, name, 10.0D);
/*     */       }
/*     */       else
/*     */       {
/* 463 */         ColumnInfo info2 = (ColumnInfo)oldColumnsMap.get(name);
/* 464 */         if (info2 != null)
/*     */         {
/* 466 */           info.m_weight = info2.m_weight;
/* 467 */           info.m_width = info2.m_width;
/*     */         }
/*     */       }
/* 470 */       if (hasLabels)
/*     */       {
/* 473 */         info.m_labelText = ((String)labels.elementAt(i));
/*     */       }
/* 475 */       setColumnInfo(info);
/* 476 */       newColumns.put(name, info);
/*     */ 
/* 478 */       if (!info.m_isCheckbox)
/*     */         continue;
/* 480 */       this.m_list.m_tableDataModel.m_checkboxFields.add(info.m_fieldId);
/*     */     }
/*     */ 
/* 484 */     this.m_list.removeColumns();
/*     */ 
/* 486 */     for (int i = 0; i < ncols; ++i)
/*     */     {
/* 488 */       String name = (String)v.elementAt(i);
/* 489 */       this.m_list.addColumn(getColumnInfo(name));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setIDColumn(String columnName)
/*     */   {
/* 495 */     this.m_idColumnName = columnName;
/* 496 */     this.m_idIndex = -1;
/*     */   }
/*     */ 
/*     */   public void setStateColumn(String stateColumn)
/*     */   {
/* 501 */     this.m_stateColumn = stateColumn;
/* 502 */     this.m_stateIndex = -1;
/*     */   }
/*     */ 
/*     */   public void setTitle(String title)
/*     */   {
/* 507 */     this.m_title = title;
/* 508 */     if (this.m_titleLabel == null)
/*     */       return;
/* 510 */     this.m_titleLabel.setText(title);
/* 511 */     this.m_titleLabel.invalidate();
/* 512 */     validate();
/*     */   }
/*     */ 
/*     */   public void setMultipleMode(boolean isMulti)
/*     */   {
/* 518 */     this.m_list.setMultipleMode(isMulti);
/*     */   }
/*     */ 
/*     */   public void setHasSort(boolean hasSort)
/*     */   {
/* 523 */     this.m_hasSort = hasSort;
/*     */   }
/*     */ 
/*     */   public void setDisplayCallback(String fieldName, DisplayStringCallback callback)
/*     */   {
/* 529 */     this.m_displayCallbacksMap.put(fieldName, callback);
/*     */   }
/*     */ 
/*     */   public ResultSet getResultSet()
/*     */   {
/* 534 */     return this.m_rset;
/*     */   }
/*     */ 
/*     */   protected void setSelectedIndexes(Vector selIndexes)
/*     */   {
/* 539 */     if (this.m_list.m_table.getRowSelectionAllowed())
/*     */     {
/* 541 */       int num = selIndexes.size();
/* 542 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 544 */         Integer selIndex = (Integer)selIndexes.elementAt(i);
/* 545 */         int index = selIndex.intValue();
/* 546 */         this.m_list.select(index);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 551 */       FieldInfo fi = new FieldInfo();
/* 552 */       if (!this.m_rset.getFieldInfo("IsSelected", fi))
/*     */         return;
/*     */       try
/*     */       {
/* 556 */         int i = 0;
/* 557 */         for (this.m_rset.first(); this.m_rset.isRowPresent(); this.m_rset.next())
/*     */         {
/* 559 */           if (selIndexes.contains(new Integer(i)))
/*     */           {
/* 561 */             this.m_rset.setCurrentValue(fi.m_index, "1");
/*     */           }
/*     */           else
/*     */           {
/* 565 */             this.m_rset.setCurrentValue(fi.m_index, "");
/*     */           }
/* 567 */           ++i;
/*     */         }
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void setSelectedIndex(int selectedIndex)
/*     */   {
/* 580 */     this.m_list.select(selectedIndex);
/*     */   }
/*     */ 
/*     */   public int getSelectedIndex()
/*     */   {
/* 585 */     return this.m_list.getSelectedIndex();
/*     */   }
/*     */ 
/*     */   public int[] getSelectedIndexes()
/*     */   {
/* 590 */     return this.m_list.getSelectedIndexes();
/*     */   }
/*     */ 
/*     */   public String getSelectedObj()
/*     */   {
/* 595 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 596 */     int nrows = model.getRowCount();
/* 597 */     if (nrows == 0)
/*     */     {
/* 599 */       return null;
/*     */     }
/*     */ 
/* 602 */     int selIndex = getSelectedIndex();
/* 603 */     if ((selIndex < 0) || (selIndex >= nrows))
/*     */     {
/* 605 */       return null;
/*     */     }
/*     */ 
/* 608 */     Vector row = model.m_rset.getRowValues(selIndex);
/* 609 */     if (this.m_idIndex < 0)
/*     */     {
/* 611 */       return (String)row.elementAt(0);
/*     */     }
/* 613 */     return (String)row.elementAt(this.m_idIndex);
/*     */   }
/*     */ 
/*     */   public String[] getSelectedObjs()
/*     */   {
/* 618 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 619 */     int nrows = model.getRowCount();
/* 620 */     if (nrows == 0)
/*     */     {
/* 622 */       return null;
/*     */     }
/*     */ 
/* 625 */     int[] selIndexes = getSelectedIndexes();
/* 626 */     int num = selIndexes.length;
/* 627 */     if (num == 0)
/*     */     {
/* 629 */       return null;
/*     */     }
/*     */ 
/* 632 */     String[] selObjs = new String[num];
/* 633 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 635 */       Vector row = model.m_rset.getRowValues(selIndexes[i]);
/* 636 */       if (this.m_idIndex < 0)
/*     */       {
/* 638 */         selObjs[i] = ((String)row.elementAt(0));
/*     */       }
/*     */       else
/*     */       {
/* 642 */         selObjs[i] = ((String)row.elementAt(this.m_idIndex));
/*     */       }
/*     */     }
/*     */ 
/* 646 */     return selObjs;
/*     */   }
/*     */ 
/*     */   public DataResultSet getSelectedAsResultSet()
/*     */   {
/* 651 */     if (this.m_rset == null)
/*     */     {
/* 653 */       return null;
/*     */     }
/*     */ 
/* 656 */     DataResultSetTableModel model = this.m_list.m_tableDataModel;
/* 657 */     DataResultSet drset = new DataResultSet();
/* 658 */     drset.copyFieldInfo(model.m_rset);
/* 659 */     int[] selected = getSelectedIndexes();
/*     */ 
/* 661 */     for (int i = 0; i < selected.length; ++i)
/*     */     {
/* 663 */       Vector v = model.m_rset.getRowValues(selected[i]);
/* 664 */       drset.addRow(v);
/*     */     }
/*     */ 
/* 667 */     return drset;
/*     */   }
/*     */ 
/*     */   public Properties getDataAt(int index)
/*     */   {
/* 672 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 673 */     int curRow = model.m_rset.getCurrentRow();
/*     */ 
/* 675 */     model.m_rset.setCurrentRow(index);
/* 676 */     Properties p = model.m_rset.getCurrentRowProps();
/* 677 */     model.m_rset.setCurrentRow(curRow);
/*     */ 
/* 679 */     return p;
/*     */   }
/*     */ 
/*     */   public FieldInfo getFieldInfo(String fieldName)
/*     */   {
/* 689 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 690 */     FieldInfo fi = new FieldInfo();
/* 691 */     model.m_rset.getFieldInfo(fieldName, fi);
/* 692 */     return fi;
/*     */   }
/*     */ 
/*     */   public int findRowPrimaryField(String str)
/*     */   {
/* 704 */     return findRow(0, str);
/*     */   }
/*     */ 
/*     */   public int findRow(int colIndex, String str)
/*     */   {
/* 716 */     FieldInfo finfo = getColumnInfo(colIndex);
/* 717 */     int rsIndex = finfo.m_index;
/* 718 */     if ((rsIndex < 0) || (str == null))
/*     */     {
/* 720 */       return -1;
/*     */     }
/*     */ 
/* 723 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 724 */     int nrows = model.getRowCount();
/* 725 */     for (int i = 0; i < nrows; ++i)
/*     */     {
/* 727 */       Vector v = model.m_rset.getRowValues(i);
/* 728 */       String colVal = (String)v.elementAt(rsIndex);
/* 729 */       if ((colVal != null) && (colVal.equalsIgnoreCase(str)))
/*     */       {
/* 731 */         return i;
/*     */       }
/*     */     }
/*     */ 
/* 735 */     return -1;
/*     */   }
/*     */ 
/*     */   public void setIconColumn(String column)
/*     */   {
/* 744 */     this.m_iconField = column;
/*     */   }
/*     */ 
/*     */   public ColumnInfo getColumnInfo(String column)
/*     */   {
/* 754 */     return (ColumnInfo)this.m_columnInfoMap.get(column);
/*     */   }
/*     */ 
/*     */   public FieldInfo getColumnInfo(int colIndex)
/*     */   {
/* 764 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_list.m_table.getModel();
/* 765 */     FieldInfo fi = new FieldInfo();
/* 766 */     model.m_rset.getFieldInfoByIndex(colIndex, fi);
/* 767 */     return fi;
/*     */   }
/*     */ 
/*     */   public void setColumnInfo(ColumnInfo info)
/*     */   {
/* 776 */     ColumnInfo old = (ColumnInfo)this.m_columnInfoMap.get(info.m_fieldId);
/* 777 */     if (old == null)
/*     */     {
/* 779 */       this.m_columnInfoMap.put(info.m_fieldId, info);
/*     */     }
/*     */     else
/*     */     {
/* 783 */       old.m_labelText = info.m_labelText;
/*     */     }
/*     */ 
/* 786 */     if (!info.m_isCheckbox)
/*     */       return;
/* 788 */     this.m_list.m_tableDataModel.m_checkboxFields.add(info.m_fieldId);
/*     */   }
/*     */ 
/*     */   public void setRowIcons(String rowId, Image[] icons)
/*     */   {
/* 798 */     this.m_icons.put(rowId, icons);
/*     */   }
/*     */ 
/*     */   public IdcComparator getComparator(String column)
/*     */   {
/* 807 */     return (IdcComparator)this.m_comparators.get(column);
/*     */   }
/*     */ 
/*     */   public IdcComparator setComparator(String column, IdcComparator c)
/*     */   {
/* 818 */     IdcComparator old = getComparator(column);
/* 819 */     this.m_comparators.put(column, c);
/* 820 */     return old;
/*     */   }
/*     */ 
/*     */   public int getNumRows()
/*     */   {
/* 825 */     return this.m_list.getItemCount();
/*     */   }
/*     */ 
/*     */   public void addPopup(String title, String[] commands, ActionListener listener)
/*     */   {
/* 833 */     int len = commands.length;
/* 834 */     String[][] actionCommands = new String[len][len];
/* 835 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 837 */       actionCommands[i][0] = commands[i];
/* 838 */       actionCommands[i][1] = commands[i];
/*     */     }
/* 840 */     addPopupEx(title, actionCommands, listener);
/*     */   }
/*     */ 
/*     */   public void addPopupEx(String title, String[][] commands, ActionListener listener)
/*     */   {
/* 845 */     this.m_popup = new JPopupMenu(title);
/*     */ 
/* 847 */     for (int i = 0; i < commands.length; ++i)
/*     */     {
/* 849 */       String cmdStr = commands[i][0];
/* 850 */       if (cmdStr == null) {
/*     */         continue;
/*     */       }
/*     */ 
/* 854 */       if (cmdStr.equalsIgnoreCase("separator"))
/*     */       {
/* 856 */         this.m_popup.addSeparator();
/*     */       }
/*     */       else
/*     */       {
/* 860 */         JMenuItem mi = new JMenuItem(cmdStr);
/* 861 */         mi.setActionCommand(commands[i][1]);
/* 862 */         mi.addActionListener(listener);
/* 863 */         this.m_popup.add(mi);
/*     */       }
/*     */     }
/*     */ 
/* 867 */     this.m_list.m_table.addMouseListener(new MouseAdapter()
/*     */     {
/*     */       public void mouseReleased(MouseEvent e)
/*     */       {
/* 872 */         if (!e.isPopupTrigger())
/*     */           return;
/* 874 */         showPopup(e);
/*     */       }
/*     */ 
/*     */       public void mousePressed(MouseEvent e)
/*     */       {
/* 881 */         if (!e.isPopupTrigger())
/*     */           return;
/* 883 */         showPopup(e);
/*     */       }
/*     */ 
/*     */       public void showPopup(MouseEvent e)
/*     */       {
/* 889 */         UdlPanel.this.m_popup.show(e.getComponent(), e.getX(), e.getY());
/*     */       }
/*     */     });
/*     */   }
/*     */ 
/*     */   public JPopupMenu getPopupMenu()
/*     */   {
/* 896 */     return this.m_popup;
/*     */   }
/*     */ 
/*     */   public void actionPerformed(ActionEvent event)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setSort(ColumnInfo column, boolean reverse)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 922 */     return "releaseInfo=dev,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UdlPanel
 * JD-Core Version:    0.5.4
 */