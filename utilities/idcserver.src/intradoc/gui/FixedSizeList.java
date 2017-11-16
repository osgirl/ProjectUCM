/*     */ package intradoc.gui;
/*     */ 
/*     */ import intradoc.gui.iwt.IdcList;
/*     */ import java.awt.GridLayout;
/*     */ import java.awt.ItemSelectable;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import javax.swing.DefaultListModel;
/*     */ import javax.swing.DefaultListSelectionModel;
/*     */ import javax.swing.JScrollPane;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ import javax.swing.event.ListSelectionListener;
/*     */ 
/*     */ public class FixedSizeList extends PanePanel
/*     */   implements ListSelectionListener, ItemSelectable
/*     */ {
/*     */   public JScrollPane m_scrollPane;
/*     */   public IdcList m_list;
/*     */   public DefaultListModel m_dataModel;
/*     */   public DefaultListSelectionModel m_selectionModel;
/*     */   public Collection<ItemListener> m_itemListeners;
/*  48 */   public boolean m_isEmptyList = true;
/*     */ 
/*     */   public FixedSizeList()
/*     */   {
/*  52 */     this(-1, 200, false);
/*     */   }
/*     */ 
/*     */   public FixedSizeList(int nrows)
/*     */   {
/*  57 */     this(nrows, 200, false);
/*     */   }
/*     */ 
/*     */   public FixedSizeList(int nrows, boolean multipleMode)
/*     */   {
/*  62 */     this(nrows, 200, multipleMode);
/*     */   }
/*     */ 
/*     */   public FixedSizeList(int nrows, int width)
/*     */   {
/*  67 */     this(nrows, width, false);
/*     */   }
/*     */ 
/*     */   public FixedSizeList(int nrows, int width, boolean multipleMode)
/*     */   {
/*  74 */     this.m_list = new IdcList(new DefaultListModel());
/*  75 */     this.m_list.m_actionSource = this;
/*  76 */     this.m_list.setEnabled(false);
/*  77 */     this.m_scrollPane = new JScrollPane(this.m_list);
/*  78 */     this.m_dataModel = ((DefaultListModel)this.m_list.getModel());
/*  79 */     this.m_dataModel.addElement(" ");
/*  80 */     this.m_selectionModel = ((DefaultListSelectionModel)this.m_list.getSelectionModel());
/*  81 */     this.m_selectionModel.addListSelectionListener(this);
/*  82 */     this.m_itemListeners = new HashSet();
/*  83 */     if (nrows > 0)
/*     */     {
/*  85 */       this.m_list.setVisibleRowCount(nrows);
/*     */     }
/*  87 */     if (width > 0)
/*     */     {
/*  89 */       this.m_list.setFixedCellWidth(width);
/*     */     }
/*  91 */     setMultipleMode(multipleMode);
/*     */ 
/*  93 */     setLayout(new GridLayout());
/*  94 */     add(this.m_scrollPane);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void removeAll()
/*     */   {
/* 102 */     removeAllItems();
/*     */   }
/*     */ 
/*     */   public void removeAllItems()
/*     */   {
/* 107 */     this.m_dataModel.removeAllElements();
/* 108 */     this.m_isEmptyList = true;
/* 109 */     this.m_dataModel.addElement(" ");
/* 110 */     this.m_list.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public void setMultipleMode(boolean isMultiple)
/*     */   {
/* 115 */     if (isMultiple)
/*     */     {
/* 117 */       this.m_selectionModel.setSelectionMode(2);
/*     */     }
/*     */     else
/*     */     {
/* 121 */       this.m_selectionModel.setSelectionMode(0);
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isMultipleMode()
/*     */   {
/* 129 */     return this.m_selectionModel.getSelectionMode() == 2;
/*     */   }
/*     */ 
/*     */   public void select(int index)
/*     */   {
/* 137 */     this.m_list.setSelectedIndex(index);
/*     */   }
/*     */ 
/*     */   public void deselect(int index)
/*     */   {
/* 142 */     this.m_list.removeSelectionInterval(index, index);
/*     */   }
/*     */ 
/*     */   public void add(String item)
/*     */   {
/* 147 */     if (this.m_isEmptyList)
/*     */     {
/* 149 */       this.m_dataModel.removeAllElements();
/* 150 */       this.m_list.setEnabled(true);
/* 151 */       this.m_isEmptyList = false;
/*     */     }
/*     */ 
/* 154 */     this.m_dataModel.addElement(item);
/*     */   }
/*     */ 
/*     */   public void remove(String item)
/*     */   {
/* 159 */     this.m_dataModel.removeElement(item);
/*     */ 
/* 161 */     if (getItemCount() != 0)
/*     */       return;
/* 163 */     this.m_isEmptyList = true;
/* 164 */     this.m_dataModel.addElement(" ");
/* 165 */     this.m_list.setEnabled(false);
/*     */   }
/*     */ 
/*     */   public int getItemCount()
/*     */   {
/* 171 */     return this.m_dataModel.getSize();
/*     */   }
/*     */ 
/*     */   public String getSelectedItem()
/*     */   {
/* 176 */     return (String)this.m_list.getSelectedValue();
/*     */   }
/*     */ 
/*     */   public String[] getSelectedItems()
/*     */   {
/* 181 */     Object[] arr = this.m_list.getSelectedValues();
/* 182 */     String[] ret = new String[arr.length];
/* 183 */     System.arraycopy(arr, 0, ret, 0, arr.length);
/* 184 */     return ret;
/*     */   }
/*     */ 
/*     */   public int[] getSelectedIndexes()
/*     */   {
/* 189 */     return this.m_list.getSelectedIndices();
/*     */   }
/*     */ 
/*     */   public int getSelectedIndex()
/*     */   {
/* 194 */     return this.m_list.getSelectedIndex();
/*     */   }
/*     */ 
/*     */   public Object[] getSelectedObjects()
/*     */   {
/* 199 */     return getSelectedItems();
/*     */   }
/*     */ 
/*     */   public void addItemListener(ItemListener l)
/*     */   {
/* 204 */     if (l == null)
/*     */     {
/* 206 */       return;
/*     */     }
/*     */ 
/* 209 */     this.m_itemListeners.add(l);
/*     */   }
/*     */ 
/*     */   public void removeItemListener(ItemListener l)
/*     */   {
/* 214 */     if (l == null)
/*     */     {
/* 216 */       return;
/*     */     }
/*     */ 
/* 219 */     this.m_itemListeners.remove(l);
/*     */   }
/*     */ 
/*     */   public void addActionListener(ActionListener l)
/*     */   {
/* 224 */     if (l == null)
/*     */     {
/* 226 */       return;
/*     */     }
/*     */ 
/* 229 */     this.m_list.addActionListener(l);
/*     */   }
/*     */ 
/*     */   public void removeActionListener(ActionListener l)
/*     */   {
/* 234 */     if (l == null)
/*     */     {
/* 236 */       return;
/*     */     }
/*     */ 
/* 239 */     this.m_list.removeActionListener(l);
/*     */   }
/*     */ 
/*     */   public void valueChanged(ListSelectionEvent e)
/*     */   {
/* 244 */     if (this.m_itemListeners.size() == 0)
/*     */     {
/* 246 */       return;
/*     */     }
/*     */ 
/* 249 */     int start = e.getFirstIndex();
/* 250 */     int end = e.getLastIndex();
/*     */     ItemEvent itemEvent;
/* 251 */     for (int i = start; (i <= end) && 
/* 253 */       (i >= 0); ++i)
/*     */     {
/* 253 */       if (this.m_dataModel.size() <= i)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/* 258 */       boolean isSelected = this.m_selectionModel.isSelectedIndex(i);
/* 259 */       int stateChange = (isSelected) ? 1 : 2;
/* 260 */       Object item = this.m_dataModel.elementAt(i);
/* 261 */       itemEvent = new ItemEvent(this, 701, item, stateChange);
/*     */ 
/* 263 */       for (ItemListener l : this.m_itemListeners)
/*     */       {
/* 265 */         l.itemStateChanged(itemEvent);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 272 */     return "releaseInfo=dev,releaseRevision=$Rev: 79101 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.FixedSizeList
 * JD-Core Version:    0.5.4
 */