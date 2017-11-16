/*     */ package intradoc.gui.iwt;
/*     */ 
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.gui.DisplayStringCallback;
/*     */ import java.util.Collection;
/*     */ import java.util.HashSet;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ import javax.swing.table.AbstractTableModel;
/*     */ 
/*     */ public class DataResultSetTableModel extends AbstractTableModel
/*     */ {
/*     */   public DataResultSet m_rset;
/*     */   public Hashtable<String, DisplayStringCallback> m_displayCallbacksMap;
/*  36 */   public Collection<String> m_checkboxFields = new HashSet();
/*     */ 
/*     */   public DataResultSetTableModel()
/*     */   {
/*     */   }
/*     */ 
/*     */   public DataResultSetTableModel(DataResultSet drset)
/*     */   {
/*  45 */     this.m_rset = drset;
/*     */   }
/*     */ 
/*     */   public int getColumnCount()
/*     */   {
/*  50 */     if (this.m_rset == null)
/*     */     {
/*  52 */       return 0;
/*     */     }
/*     */ 
/*  55 */     return this.m_rset.getNumFields();
/*     */   }
/*     */ 
/*     */   public String getColumnName(int columnIndex)
/*     */   {
/*  61 */     if ((this.m_rset == null) || (columnIndex < 0))
/*     */     {
/*  63 */       return null;
/*     */     }
/*     */ 
/*  66 */     return this.m_rset.getFieldName(columnIndex);
/*     */   }
/*     */ 
/*     */   public int getRowCount()
/*     */   {
/*  71 */     if (this.m_rset == null)
/*     */     {
/*  73 */       return 0;
/*     */     }
/*     */ 
/*  76 */     return this.m_rset.getNumRows();
/*     */   }
/*     */ 
/*     */   public Object getValueAt(int rowIndex, int columnIndex)
/*     */   {
/*  81 */     if ((this.m_rset == null) || (columnIndex == -1))
/*     */     {
/*  83 */       return null;
/*     */     }
/*     */ 
/*  86 */     Vector v = this.m_rset.getRowValues(rowIndex);
/*  87 */     Object retVal = v.elementAt(columnIndex);
/*     */ 
/*  89 */     String fieldName = this.m_rset.getFieldName(columnIndex);
/*  90 */     DisplayStringCallback callback = (DisplayStringCallback)this.m_displayCallbacksMap.get(fieldName);
/*  91 */     if (callback != null)
/*     */     {
/*  93 */       FieldInfo fi = new FieldInfo();
/*  94 */       this.m_rset.getFieldInfo(fieldName, fi);
/*  95 */       retVal = callback.createDisplayString(fi, fieldName, (String)retVal, v);
/*     */     }
/*     */ 
/*  98 */     if (this.m_checkboxFields.contains(getColumnName(columnIndex)))
/*     */     {
/* 100 */       boolean val = StringUtils.convertToBool((String)retVal, false);
/* 101 */       retVal = new Boolean(val);
/*     */     }
/*     */ 
/* 104 */     return retVal;
/*     */   }
/*     */ 
/*     */   public boolean isCellEditable(int rowIndex, int columnIndex)
/*     */   {
/* 112 */     return this.m_checkboxFields.contains(getColumnName(columnIndex));
/*     */   }
/*     */ 
/*     */   public void setValueAt(Object value, int rowIndex, int columnIndex)
/*     */   {
/* 121 */     if ((this.m_rset == null) || (columnIndex < 0))
/*     */     {
/* 123 */       return;
/*     */     }
/*     */ 
/* 126 */     if (value instanceof Boolean)
/*     */     {
/* 128 */       Boolean b = (Boolean)value;
/* 129 */       if (b.booleanValue())
/*     */       {
/* 131 */         value = "1";
/*     */       }
/*     */       else
/*     */       {
/* 135 */         value = "0";
/*     */       }
/*     */     }
/*     */ 
/* 139 */     Vector v = this.m_rset.getRowValues(rowIndex);
/* 140 */     v.setElementAt(value, columnIndex);
/*     */   }
/*     */ 
/*     */   public Class getColumnClass(int c)
/*     */   {
/* 146 */     if ((c >= 0) && (this.m_checkboxFields.contains(getColumnName(c))))
/*     */     {
/* 148 */       return Boolean.class;
/*     */     }
/*     */ 
/* 151 */     return String.class;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 156 */     return "releaseInfo=dev,releaseRevision=$Rev: 80730 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.gui.iwt.DataResultSetTableModel
 * JD-Core Version:    0.5.4
 */