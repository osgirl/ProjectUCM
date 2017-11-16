/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.ContainerHelper;
/*     */ import intradoc.gui.GridBagHelper;
/*     */ import intradoc.gui.PanePanel;
/*     */ import intradoc.gui.iwt.event.IwtListener;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.awt.ItemSelectable;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.awt.event.MouseEvent;
/*     */ import java.awt.event.MouseListener;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.table.TableColumn;
/*     */ import javax.swing.table.TableColumnModel;
/*     */ 
/*     */ public class UserDrawList extends PanePanel
/*     */   implements ItemSelectable, MouseListener
/*     */ {
/*     */   public static final int MULTISELECT = 1;
/*     */   public static final int VISIBLE_LABELS = 2;
/*     */   public static final int VERTICAL_SCROLLBAR = 4;
/*     */   public static final int HORIZONTAL_SCROLLBAR = 8;
/*     */   public static final int FORCE_VERTICAL_SCROLLBAR = 2048;
/*     */   public static final int FORCE_HORIZONTAL_SCROLLBAR = 4096;
/*     */   public static final int SIMPLE_MULTISELECT = 256;
/*     */   public static final int ADVANCED_MULTISELECT = 512;
/*     */   public JScrollPane m_scrollPane;
/*     */   public IdcTable m_table;
/*     */   public DataResultSetTableModel m_tableDataModel;
/*     */   public Vector<ColumnInfo> m_columns;
/*     */   public int m_flags;
/*  63 */   public String m_actionCommand = "";
/*     */   protected Vector m_itemListeners;
/*     */   protected Vector m_iwtListeners;
/*     */   protected Vector m_actionListeners;
/*     */   protected ContainerHelper m_helper;
/*     */ 
/*     */   public UserDrawList(ColumnInfo[] columns, int flags)
/*     */   {
/*  74 */     this.m_table = new IdcTable(this);
/*  75 */     this.m_columns = new IdcVector();
/*  76 */     this.m_scrollPane = new JScrollPane(this.m_table);
/*     */ 
/*  78 */     this.m_tableDataModel = new DataResultSetTableModel();
/*  79 */     this.m_table.setModel(this.m_tableDataModel);
/*  80 */     this.m_table.setAutoCreateColumnsFromModel(false);
/*  81 */     this.m_table.setRowSelectionAllowed(true);
/*  82 */     this.m_table.addMouseListener(this);
/*     */ 
/*  84 */     for (int i = 0; i < columns.length; ++i)
/*     */     {
/*  86 */       addColumn(columns[i]);
/*     */     }
/*     */ 
/*  89 */     this.m_actionListeners = new IdcVector();
/*  90 */     this.m_iwtListeners = new IdcVector();
/*  91 */     this.m_itemListeners = new IdcVector();
/*     */ 
/*  93 */     setFlags(flags);
/*     */ 
/*  95 */     this.m_helper = new ContainerHelper();
/*  96 */     this.m_helper.makePanelGridBag(this, 1);
/*  97 */     this.m_helper.m_gridHelper.m_gc.weightx = 1.0D;
/*  98 */     this.m_helper.m_gridHelper.m_gc.weighty = 1.0D;
/*  99 */     this.m_helper.addComponent(this, this.m_scrollPane);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void setFlags(int flags)
/*     */   {
/* 109 */     this.m_flags = flags;
/* 110 */     if ((this.m_flags & 0x1) != 0)
/*     */     {
/* 112 */       if ((this.m_flags & 0x100) != 0)
/*     */       {
/* 114 */         this.m_table.setSelectionMode(1);
/*     */       }
/*     */       else
/*     */       {
/* 118 */         this.m_table.setSelectionMode(2);
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 123 */       this.m_table.setSelectionMode(0);
/*     */     }
/*     */ 
/* 126 */     if ((this.m_flags & 0x800) != 0)
/*     */     {
/* 128 */       this.m_scrollPane.setVerticalScrollBarPolicy(22);
/*     */     }
/* 130 */     if ((this.m_flags & 0x1000) != 0)
/*     */     {
/* 132 */       this.m_scrollPane.setHorizontalScrollBarPolicy(32);
/*     */     }
/*     */ 
/* 135 */     if ((this.m_flags & 0x2) != 0)
/*     */       return;
/* 137 */     this.m_table.setTableHeader(null);
/*     */   }
/*     */ 
/*     */   public void addColumn(ColumnInfo columnInfo)
/*     */   {
/* 143 */     this.m_columns.addElement(columnInfo);
/* 144 */     int modalIndex = this.m_columns.size() - 1;
/* 145 */     TableColumn c = new TableColumn(modalIndex, columnInfo.m_width);
/* 146 */     c.setIdentifier(columnInfo.m_fieldId);
/* 147 */     if (columnInfo.m_labelText != null)
/*     */     {
/* 149 */       c.setHeaderValue(columnInfo.m_labelText);
/*     */     }
/* 151 */     c.setResizable(true);
/*     */ 
/* 153 */     this.m_table.getColumnModel().addColumn(c);
/*     */   }
/*     */ 
/*     */   public boolean checkFlag(int flag)
/*     */   {
/* 162 */     return (this.m_flags & flag) > 0;
/*     */   }
/*     */ 
/*     */   public boolean isMultipleMode()
/*     */   {
/* 170 */     return checkFlag(1);
/*     */   }
/*     */ 
/*     */   public int getItemCount()
/*     */   {
/* 175 */     return this.m_table.getRowCount();
/*     */   }
/*     */ 
/*     */   public int getColumnCount()
/*     */   {
/* 180 */     return this.m_columns.size();
/*     */   }
/*     */ 
/*     */   public int getSelectedIndex()
/*     */   {
/* 185 */     int row = this.m_table.getSelectedRow();
/* 186 */     if (row >= 0)
/*     */     {
/* 188 */       Object o = ClassHelperUtils.executeMethodSuppressException(this.m_table, "convertRowIndexToModel", new Object[] { new Integer(row) });
/*     */ 
/* 190 */       if (o != null)
/*     */       {
/* 192 */         row = ((Integer)o).intValue();
/*     */       }
/*     */     }
/*     */ 
/* 196 */     return row;
/*     */   }
/*     */ 
/*     */   public int[] getSelectedIndexes()
/*     */   {
/* 201 */     int[] viewRows = this.m_table.getSelectedRows();
/* 202 */     int[] rows = new int[viewRows.length];
/* 203 */     for (int i = 0; i < rows.length; ++i)
/*     */     {
/* 205 */       Object o = ClassHelperUtils.executeMethodSuppressException(this.m_table, "convertRowIndexToModel", new Object[] { new Integer(viewRows[i]) });
/*     */ 
/* 207 */       if (o == null)
/*     */         continue;
/* 209 */       rows[i] = ((Integer)o).intValue();
/*     */     }
/*     */ 
/* 213 */     return rows;
/*     */   }
/*     */ 
/*     */   public Object[] getSelectedObjects()
/*     */   {
/* 218 */     int[] indexes = getSelectedIndexes();
/* 219 */     Object[] arr = new Object[indexes.length];
/* 220 */     DataResultSetTableModel model = (DataResultSetTableModel)this.m_table.getModel();
/* 221 */     for (int i = 0; i < arr.length; ++i)
/*     */     {
/* 223 */       arr[i] = model.m_rset.getRowAsList(indexes[i]);
/*     */     }
/*     */ 
/* 226 */     return arr;
/*     */   }
/*     */ 
/*     */   public ColumnInfo getColumnInfo(int i)
/*     */   {
/* 236 */     return (ColumnInfo)this.m_columns.elementAt(i);
/*     */   }
/*     */ 
/*     */   public void removeColumns()
/*     */   {
/* 244 */     this.m_columns.removeAllElements();
/*     */ 
/* 246 */     TableColumnModel model = this.m_table.getColumnModel();
/* 247 */     int numcols = model.getColumnCount();
/* 248 */     for (int i = numcols - 1; i >= 0; --i)
/*     */     {
/* 250 */       TableColumn c = model.getColumn(i);
/* 251 */       model.removeColumn(c);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setMultipleMode(boolean isMultiple)
/*     */   {
/* 261 */     if (isMultiple)
/*     */     {
/* 263 */       this.m_flags |= 1;
/* 264 */       if ((this.m_flags & 0x100) != 0)
/*     */       {
/* 266 */         this.m_table.setSelectionMode(1);
/*     */       }
/*     */       else
/*     */       {
/* 270 */         this.m_table.setSelectionMode(2);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 275 */       this.m_flags &= -2;
/* 276 */       this.m_table.setSelectionMode(0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void select(int i)
/*     */   {
/* 286 */     boolean isMultiple = isMultipleMode();
/* 287 */     boolean isSimple = checkFlag(256);
/* 288 */     ListSelectionModel model = this.m_table.getSelectionModel();
/*     */ 
/* 290 */     if ((model.isSelectedIndex(i)) && (isMultiple) && (isSimple))
/*     */     {
/* 292 */       model.removeSelectionInterval(i, i);
/*     */     }
/*     */     else
/*     */     {
/* 296 */       if (!isMultiple)
/*     */       {
/* 298 */         model.clearSelection();
/*     */       }
/*     */ 
/* 301 */       model.addSelectionInterval(i, i);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void deselect(int i)
/*     */   {
/* 307 */     ListSelectionModel model = this.m_table.getSelectionModel();
/* 308 */     model.removeIndexInterval(i, i);
/*     */   }
/*     */ 
/*     */   public void deselectAll()
/*     */   {
/* 313 */     int count = getItemCount();
/* 314 */     if (count <= 0)
/*     */       return;
/* 316 */     ListSelectionModel model = this.m_table.getSelectionModel();
/* 317 */     model.removeIndexInterval(0, count - 1);
/*     */   }
/*     */ 
/*     */   public void addIwtListener(IwtListener l)
/*     */   {
/* 323 */     this.m_iwtListeners.addElement(l);
/*     */   }
/*     */ 
/*     */   public void removeIwtListener(IwtListener listener)
/*     */   {
/* 328 */     this.m_iwtListeners.removeElement(listener);
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener listener)
/*     */   {
/* 333 */     this.m_itemListeners.addElement(listener);
/*     */   }
/*     */ 
/*     */   public void removeItemListener(ItemListener listener)
/*     */   {
/* 338 */     this.m_itemListeners.removeElement(listener);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener l)
/*     */   {
/* 343 */     this.m_actionListeners.addElement(l);
/*     */   }
/*     */ 
/*     */   public void removeActionListener(ActionListener l)
/*     */   {
/* 348 */     this.m_actionListeners.removeElement(l);
/*     */   }
/*     */ 
/*     */   public void setVisibleRowCount(int rows)
/*     */   {
/* 353 */     this.m_table.m_preferredVisibleRowCount = rows;
/*     */   }
/*     */ 
/*     */   public void setActionCommand(String command)
/*     */   {
/* 358 */     this.m_actionCommand = command;
/*     */   }
/*     */ 
/*     */   public void mouseClicked(MouseEvent e)
/*     */   {
/* 363 */     if (e.getClickCount() != 2)
/*     */       return;
/* 365 */     int size = this.m_actionListeners.size();
/* 366 */     ActionEvent newEvent = new ActionEvent(this, 1001, this.m_actionCommand, e.getModifiers());
/*     */ 
/* 369 */     while (size-- > 0)
/*     */     {
/* 372 */       ActionListener l = (ActionListener)this.m_actionListeners.elementAt(size);
/* 373 */       l.actionPerformed(newEvent);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void mousePressed(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseReleased(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseEntered(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public void mouseExited(MouseEvent e)
/*     */   {
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 400 */     return "releaseInfo=dev,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.UserDrawList
 * JD-Core Version:    0.5.4
 */