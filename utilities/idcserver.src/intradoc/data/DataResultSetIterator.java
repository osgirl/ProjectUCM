/*    */ package intradoc.data;
/*    */ 
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ 
/*    */ public class DataResultSetIterator
/*    */   implements Iterator<List<String>>
/*    */ {
/*    */   protected DataResultSet m_drset;
/* 25 */   protected int m_currentRow = 0;
/*    */   protected List m_values;
/*    */   protected int m_numRows;
/*    */ 
/*    */   public DataResultSetIterator(DataResultSet drset)
/*    */   {
/* 31 */     this.m_drset = drset;
/* 32 */     this.m_values = drset.m_values;
/* 33 */     this.m_numRows = drset.m_numRows;
/*    */   }
/*    */ 
/*    */   public List next()
/*    */   {
/* 38 */     return (List)this.m_values.get(this.m_currentRow++);
/*    */   }
/*    */ 
/*    */   public boolean hasNext()
/*    */   {
/* 43 */     return this.m_currentRow < this.m_numRows;
/*    */   }
/*    */ 
/*    */   public void remove()
/*    */   {
/* 48 */     this.m_drset.deleteRow(--this.m_currentRow);
/* 49 */     this.m_numRows -= 1;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 75945 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataResultSetIterator
 * JD-Core Version:    0.5.4
 */