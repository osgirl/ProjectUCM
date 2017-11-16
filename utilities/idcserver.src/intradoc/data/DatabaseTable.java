/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.common.StringUtils;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class DatabaseTable
/*    */ {
/*    */   public String m_name;
/*    */   public String m_alias;
/*    */   public FieldInfo[] m_columns;
/*    */   public FieldInfo[] m_primaryKeys;
/*    */   public String[] m_columnNames;
/*    */   public String[] m_pkColumnNames;
/*    */   public DatabaseIndexInfo[] m_indexes;
/*    */   public List<DatabaseQueryFilter> m_filters;
/*    */ 
/*    */   public DatabaseTable()
/*    */   {
/* 38 */     this.m_filters = new ArrayList();
/*    */   }
/*    */ 
/*    */   public FieldInfo getColumn(String columnName) {
/* 42 */     int index = StringUtils.findStringIndexEx(this.m_columnNames, columnName, true);
/* 43 */     FieldInfo fi = null;
/* 44 */     if (index >= 0)
/*    */     {
/* 46 */       fi = this.m_columns[index];
/*    */     }
/* 48 */     return fi;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 53 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DatabaseTable
 * JD-Core Version:    0.5.4
 */