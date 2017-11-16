/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import java.util.ArrayList;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SchemaCacheEntry
/*    */ {
/* 26 */   public static int m_pointerSize = 4;
/*    */   public List m_rows;
/*    */   public int m_size;
/*    */ 
/*    */   public SchemaCacheEntry()
/*    */   {
/* 36 */     updateSize();
/*    */   }
/*    */ 
/*    */   public SchemaCacheEntry(List rows)
/*    */   {
/* 41 */     setRows(rows);
/*    */   }
/*    */ 
/*    */   public int setRows(List rows)
/*    */   {
/* 46 */     this.m_rows = rows;
/* 47 */     return updateSize();
/*    */   }
/*    */ 
/*    */   public int addRow(Vector row)
/*    */   {
/* 52 */     if (this.m_rows == null)
/*    */     {
/* 54 */       this.m_rows = new ArrayList();
/*    */     }
/*    */ 
/* 57 */     this.m_rows.add(row);
/* 58 */     return updateSize();
/*    */   }
/*    */ 
/*    */   public int updateSize()
/*    */   {
/* 63 */     int size = 4 * m_pointerSize + 4 * m_pointerSize;
/* 64 */     if (this.m_rows != null)
/*    */     {
/* 66 */       Iterator it = this.m_rows.iterator();
/* 67 */       while (it.hasNext())
/*    */       {
/* 69 */         Vector v = (Vector)it.next();
/* 70 */         String[] rowData = (String[])(String[])v.toArray(new String[0]);
/* 71 */         for (int i = 0; i < rowData.length; ++i)
/*    */         {
/* 73 */           size += rowData[i].length() * 2;
/*    */         }
/*    */       }
/*    */     }
/* 77 */     return this.m_size = size;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 83 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaCacheEntry
 * JD-Core Version:    0.5.4
 */