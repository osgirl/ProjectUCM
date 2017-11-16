/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.FieldInfo;
/*    */ 
/*    */ public class SchemaRelationData extends SchemaData
/*    */ {
/*    */   public int m_table1TableIndex;
/*    */   public int m_table1ColumnIndex;
/*    */   public int m_table2TableIndex;
/*    */   public int m_table2ColumnIndex;
/*    */ 
/*    */   public SchemaRelationData()
/*    */   {
/* 26 */     this.m_table1TableIndex = -1;
/* 27 */     this.m_table1ColumnIndex = -1;
/* 28 */     this.m_table2TableIndex = -1;
/* 29 */     this.m_table2ColumnIndex = -1;
/*    */   }
/*    */ 
/*    */   public String getNameField()
/*    */   {
/* 34 */     return "schRelationName";
/*    */   }
/*    */ 
/*    */   public String getCanonicalNameField()
/*    */   {
/* 39 */     return "schCanonicalName";
/*    */   }
/*    */ 
/*    */   public String getTimestampField()
/*    */   {
/* 45 */     return "schRelationLastLoaded";
/*    */   }
/*    */ 
/*    */   public String getIsUpToDateField()
/*    */   {
/* 51 */     return "schRelationIsUpToDate";
/*    */   }
/*    */ 
/*    */   protected void initIndexes()
/*    */   {
/* 57 */     super.initIndexes();
/* 58 */     FieldInfo info = new FieldInfo();
/*    */ 
/* 60 */     this.m_resultSet.getFieldInfo("schTable1Table", info);
/* 61 */     this.m_table1TableIndex = info.m_index;
/* 62 */     this.m_resultSet.getFieldInfo("schTable1Column", info);
/* 63 */     this.m_table1ColumnIndex = info.m_index;
/* 64 */     this.m_resultSet.getFieldInfo("schTable2Table", info);
/* 65 */     this.m_table2TableIndex = info.m_index;
/* 66 */     this.m_resultSet.getFieldInfo("schTable2Column", info);
/* 67 */     this.m_table2ColumnIndex = info.m_index;
/*    */   }
/*    */ 
/*    */   public void update(DataBinder binder)
/*    */     throws DataException
/*    */   {
/* 76 */     super.update(binder);
/*    */   }
/*    */ 
/*    */   public void updateEx(DataBinder binder)
/*    */   {
/* 82 */     super.updateEx(binder);
/*    */   }
/*    */ 
/*    */   public void populateBinder(DataBinder binder)
/*    */   {
/* 88 */     super.populateBinder(binder);
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 94 */     return this.m_data.toString();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 99 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaRelationData
 * JD-Core Version:    0.5.4
 */