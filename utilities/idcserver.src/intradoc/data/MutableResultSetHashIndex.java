/*     */ package intradoc.data;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class MutableResultSetHashIndex
/*     */   implements ResultSetIndex
/*     */ {
/*     */   MutableResultSet m_rset;
/*     */   int[] m_columns;
/*     */   Map<String, List<Integer>> m_index;
/*     */ 
/*     */   public int getSupportedFeatures()
/*     */   {
/*  35 */     return 0;
/*     */   }
/*     */ 
/*     */   public void createIndex(ResultSet rset, int[] columns)
/*     */   {
/*  40 */     if (!rset instanceof MutableResultSet)
/*     */     {
/*  42 */       throw new IllegalArgumentException("rset must be a MutableResultSet");
/*     */     }
/*     */ 
/*  45 */     if (columns.length > 1)
/*     */     {
/*  47 */       throw new IllegalArgumentException("Multi-column indices are not supported.");
/*     */     }
/*     */ 
/*  50 */     this.m_rset = ((MutableResultSet)rset);
/*  51 */     this.m_columns = columns;
/*  52 */     this.m_index = new HashMap();
/*     */ 
/*  54 */     int oldRowNum = this.m_rset.getCurrentRow();
/*  55 */     for (this.m_rset.first(); this.m_rset.isRowPresent(); this.m_rset.next())
/*     */     {
/*  57 */       String val = this.m_rset.getStringValue(this.m_columns[0]);
/*  58 */       List currentVals = (List)this.m_index.get(val);
/*  59 */       if (currentVals == null)
/*     */       {
/*  61 */         currentVals = new ArrayList();
/*  62 */         this.m_index.put(val, currentVals);
/*     */       }
/*     */ 
/*  65 */       currentVals.add(Integer.valueOf(this.m_rset.getCurrentRow()));
/*     */     }
/*     */ 
/*  68 */     this.m_rset.setCurrentRow(oldRowNum);
/*     */   }
/*     */ 
/*     */   public void addRow(int rowNumber, List values)
/*     */   {
/*  73 */     createIndex(this.m_rset, this.m_columns);
/*     */   }
/*     */ 
/*     */   public void modifyRow(int rowNumber, List values)
/*     */   {
/*  78 */     createIndex(this.m_rset, this.m_columns);
/*     */   }
/*     */ 
/*     */   public void deleteRow(int rowNumber, List values)
/*     */   {
/*  83 */     createIndex(this.m_rset, this.m_columns);
/*     */   }
/*     */ 
/*     */   public int[] getRowNumbers(Object[] keys)
/*     */   {
/*  88 */     if (keys.length > 1)
/*     */     {
/*  90 */       throw new IllegalArgumentException("Multi-column indices are not supported.");
/*     */     }
/*     */ 
/*  93 */     List l = (List)this.m_index.get(keys[0]);
/*  94 */     if (l == null)
/*     */     {
/*  96 */       return new int[0];
/*     */     }
/*     */ 
/*  99 */     int[] arr = new int[l.size()];
/* 100 */     for (int i = 0; i < arr.length; ++i)
/*     */     {
/* 102 */       Integer theInteger = (Integer)l.get(i);
/* 103 */       arr[i] = theInteger.intValue();
/*     */     }
/*     */ 
/* 106 */     return arr;
/*     */   }
/*     */ 
/*     */   public List[] getRows(Object[] keys)
/*     */   {
/* 111 */     if (keys.length > 1)
/*     */     {
/* 113 */       throw new IllegalArgumentException("Multi-column indices are not supported.");
/*     */     }
/*     */ 
/* 116 */     List rowNumbers = (List)this.m_index.get(keys[0]);
/* 117 */     if (rowNumbers == null)
/*     */     {
/* 119 */       return new List[0];
/*     */     }
/*     */ 
/* 122 */     List[] rows = new List[rowNumbers.size()];
/* 123 */     for (int i = 0; i < rows.length; ++i)
/*     */     {
/* 125 */       rows[i] = this.m_rset.getCurrentRowValues();
/*     */     }
/*     */ 
/* 128 */     return rows;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 133 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.MutableResultSetHashIndex
 * JD-Core Version:    0.5.4
 */