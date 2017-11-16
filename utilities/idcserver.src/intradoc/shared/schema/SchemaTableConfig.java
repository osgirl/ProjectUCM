/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.FieldInfo;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SchemaTableConfig extends SchemaResultSet
/*    */ {
/*    */   public static final int TABLE_NAME = 0;
/*    */   public static final int CANONICAL_NAME = 1;
/*    */   public static final int TABLE_DESCRIPTION = 2;
/*    */   public static final int COLUMN_LIST = 3;
/*    */   public static final int PRIMARY_KEY = 4;
/*    */   public static final int ROW_CREATE_TIMESTAMP_COLUMN = 5;
/*    */   public static final int ROW_MODIFY_TIMESTAMP_COLUMN = 6;
/*    */   public static final int TABLE_LAST_LOADED = 7;
/*    */   public static final int TABLE_UPTODATE = 8;
/*    */   public static final int TABLE_SYSTEM = 9;
/* 41 */   public static String[] TABLE_COLUMNS = { "schTableName", "schTableCanonicalName", "schTableDescription", "schColumnList", "schTablePrimaryKey", "schTableRowCreateTimestamp", "schTableRowModifyTimestamp", "schTableLastLoaded", "schTableIsUpToDate", "schIsSystemObject" };
/*    */ 
/* 55 */   public static String[] TABLE_DEFINITION_COLUMNS = { "ColumnName", "ColumnType", "ColumnLength", "IsPrimaryKey" };
/*    */ 
/*    */   public SchemaTableConfig()
/*    */   {
/* 62 */     super("SchemaTableData", TABLE_COLUMNS);
/* 63 */     this.m_infos = new FieldInfo[TABLE_COLUMNS.length];
/* 64 */     this.m_indexes = new int[TABLE_COLUMNS.length];
/* 65 */     for (int i = 0; i < TABLE_COLUMNS.length; ++i)
/*    */     {
/* 67 */       this.m_infos[i] = new FieldInfo();
/* 68 */       getIndexFieldInfo(i, this.m_infos[i]);
/* 69 */       this.m_indexes[i] = this.m_infos[i].m_index;
/*    */     }
/*    */   }
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 76 */     SchemaTableConfig rset = new SchemaTableConfig();
/* 77 */     initShallow(rset);
/* 78 */     return rset;
/*    */   }
/*    */ 
/*    */   public void addRow(Vector row)
/*    */   {
/* 84 */     super.addRow(row);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 89 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaTableConfig
 * JD-Core Version:    0.5.4
 */