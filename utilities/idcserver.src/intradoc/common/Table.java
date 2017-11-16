/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ 
/*     */ public class Table
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public String[] m_colNames;
/*     */   public List m_rows;
/*     */ 
/*     */   public Table()
/*     */   {
/*  36 */     this.m_colNames = new String[0];
/*  37 */     this.m_rows = new ArrayList();
/*     */   }
/*     */ 
/*     */   public Table(String[] colNames, List rows)
/*     */   {
/*  42 */     this.m_colNames = colNames;
/*  43 */     this.m_rows = rows;
/*     */   }
/*     */ 
/*     */   public int getNumRows()
/*     */   {
/*  48 */     return this.m_rows.size();
/*     */   }
/*     */ 
/*     */   public String[] getRow(int x)
/*     */   {
/*  53 */     return (String[])(String[])this.m_rows.get(x);
/*     */   }
/*     */ 
/*     */   public String getString(int rowNum, int colNum)
/*     */   {
/*  58 */     String[] row = (String[])(String[])this.m_rows.get(rowNum);
/*  59 */     return row[colNum];
/*     */   }
/*     */ 
/*     */   public Table shallowClone()
/*     */   {
/*  64 */     String[] colNames = (String[])this.m_colNames.clone();
/*  65 */     ArrayList newRows = new ArrayList();
/*  66 */     newRows.addAll(this.m_rows);
/*  67 */     return new Table(colNames, newRows);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  72 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81138 $";
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/*  77 */     if ((this.m_colNames == null) || (this.m_rows == null))
/*     */     {
/*  79 */       appendable.append("<not init>");
/*  80 */       return;
/*     */     }
/*  82 */     appendable.append("[colNames=");
/*  83 */     StringUtils.appendForDebug(appendable, this.m_colNames, 0);
/*  84 */     appendable.append(']');
/*  85 */     appendable.append("\n");
/*     */ 
/*  87 */     appendable.append('[');
/*     */ 
/*  89 */     for (int i = 0; i < this.m_rows.size(); ++i)
/*     */     {
/*  91 */       String[] row = (String[])(String[])this.m_rows.get(i);
/*  92 */       StringUtils.appendForDebug(appendable, row, 0);
/*  93 */       if (i >= this.m_rows.size() - 1)
/*     */         continue;
/*  95 */       appendable.append("\n");
/*     */     }
/*     */ 
/*  98 */     appendable.append(']');
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 105 */     IdcStringBuilder strBuf = new IdcStringBuilder(256);
/*     */ 
/* 107 */     appendDebugFormat(strBuf);
/* 108 */     return strBuf.toString();
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.Table
 * JD-Core Version:    0.5.4
 */