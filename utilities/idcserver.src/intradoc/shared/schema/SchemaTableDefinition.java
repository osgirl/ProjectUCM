/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.common.SystemUtils;
/*    */ import intradoc.data.FieldInfo;
/*    */ 
/*    */ public class SchemaTableDefinition
/*    */ {
/* 27 */   public static String[] FIELD_NAMES = { "ColumnName", "ColumnType", "ColumnLength", "IsPrimaryKey" };
/*    */ 
/* 35 */   public static int COLUMN_NAME_INDEX = 0;
/* 36 */   public static int COLUMN_TYPE_INDEX = 1;
/* 37 */   public static int COLUMN_LENGTH_INDEX = 2;
/* 38 */   public static int PRIMARY_KEY_INDEX = 3;
/*    */   public String m_tableName;
/*    */   public String m_tableDescription;
/*    */   public FieldInfo[] m_columns;
/*    */   public FieldInfo[] m_primaryKeyColumns;
/*    */   public FieldInfo m_createTimestampColumn;
/*    */   public FieldInfo m_modifyTimestampColumn;
/*    */ 
/*    */   @Deprecated
/*    */   private SchemaTableDefinition()
/*    */   {
/* 52 */     SystemUtils.reportDeprecatedUsage("SchemaTableDefinition");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaTableDefinition
 * JD-Core Version:    0.5.4
 */