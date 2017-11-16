/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.gui.iwt.event.IwtItemEvent;
/*     */ import java.awt.Dimension;
/*     */ import java.awt.event.ItemEvent;
/*     */ import java.awt.event.ItemListener;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ import javax.swing.JTable;
/*     */ import javax.swing.ListSelectionModel;
/*     */ import javax.swing.event.ListSelectionEvent;
/*     */ 
/*     */ public class IdcTable extends JTable
/*     */ {
/*     */   public UserDrawList m_userDrawList;
/*  33 */   public int m_preferredVisibleRowCount = -1;
/*     */ 
/*     */   public IdcTable(UserDrawList list)
/*     */   {
/*  37 */     this.m_userDrawList = list;
/*     */   }
/*     */ 
/*     */   public Dimension getPreferredScrollableViewportSize()
/*     */   {
/*  43 */     if (this.m_preferredVisibleRowCount < 0)
/*     */     {
/*  45 */       return super.getPreferredScrollableViewportSize();
/*     */     }
/*     */ 
/*  48 */     int height = 0;
/*  49 */     for (int row = 0; row < this.m_preferredVisibleRowCount; ++row)
/*     */     {
/*  51 */       height += getRowHeight(row);
/*     */     }
/*  53 */     return new Dimension(super.getPreferredScrollableViewportSize().width, height);
/*     */   }
/*     */ 
/*     */   public void valueChanged(ListSelectionEvent e)
/*     */   {
/*  59 */     super.valueChanged(e);
/*     */ 
/*  61 */     if (this.m_userDrawList == null)
/*     */     {
/*  63 */       return;
/*     */     }
/*     */ 
/*  66 */     int numListeners = this.m_userDrawList.m_itemListeners.size();
/*  67 */     if (numListeners == 0)
/*     */     {
/*  69 */       return;
/*     */     }
/*     */ 
/*  72 */     DataResultSetTableModel rsetTableModel = (DataResultSetTableModel)getModel();
/*  73 */     int firstIndex = e.getFirstIndex();
/*  74 */     int lastIndex = e.getLastIndex();
/*  75 */     int curRow = rsetTableModel.m_rset.getCurrentRow();
/*     */ 
/*  77 */     for (int i = firstIndex; i <= lastIndex; ++i)
/*     */     {
/*  79 */       boolean isSelected = this.selectionModel.isSelectedIndex(i);
/*     */       int type;
/*     */       int type;
/*  81 */       if (isSelected)
/*     */       {
/*  83 */         type = 1;
/*     */       }
/*     */       else
/*     */       {
/*  87 */         type = 2;
/*     */       }
/*  89 */       int flags = 0;
/*  90 */       if (i == lastIndex)
/*     */       {
/*  92 */         flags = IwtItemEvent.FINAL_ITEM_EVENT;
/*     */       }
/*     */ 
/*  95 */       rsetTableModel.m_rset.setCurrentRow(i);
/*  96 */       Properties p = rsetTableModel.m_rset.getCurrentRowProps();
/*  97 */       ItemEvent event = new IwtItemEvent(this.m_userDrawList, 701, p, type, flags);
/*  98 */       for (int j = 0; j < numListeners; ++j)
/*     */       {
/* 101 */         ItemListener listener = (ItemListener)this.m_userDrawList.m_itemListeners.elementAt(j);
/* 102 */         listener.itemStateChanged(event);
/*     */       }
/*     */     }
/*     */ 
/* 106 */     rsetTableModel.m_rset.setCurrentRow(curRow);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 111 */     return "releaseInfo=dev,releaseRevision=$Rev: 78444 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.IdcTable
 * JD-Core Version:    0.5.4
 */