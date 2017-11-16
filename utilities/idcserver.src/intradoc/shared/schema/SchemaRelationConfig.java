/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.FieldInfo;
/*    */ 
/*    */ public class SchemaRelationConfig extends SchemaResultSet
/*    */ {
/*    */   public static final int RELATION_NAME = 0;
/*    */   public static final int CANONICAL_NAME = 1;
/*    */   public static final int RELATION_LAST_LOADED = 2;
/*    */   public static final int RELATION_UPTODATE = 3;
/*    */   public static final int RELATION_SYSTEM = 4;
/* 34 */   public static String[] RELATION_COLUMNS = { "schRelationName", "schCanonicalName", "schRelationLastLoaded", "schRelationIsUpToDate", "schTable1Table", "schTable1Column", "schTable2Table", "schTable2Column", "schIsSystemObject" };
/*    */ 
/*    */   public SchemaRelationConfig()
/*    */   {
/* 49 */     super("SchemaRelationData", RELATION_COLUMNS);
/* 50 */     this.m_infos = new FieldInfo[RELATION_COLUMNS.length];
/* 51 */     this.m_indexes = new int[RELATION_COLUMNS.length];
/* 52 */     for (int i = 0; i < RELATION_COLUMNS.length; ++i)
/*    */     {
/* 54 */       this.m_infos[i] = new FieldInfo();
/* 55 */       getIndexFieldInfo(i, this.m_infos[i]);
/* 56 */       this.m_indexes[i] = this.m_infos[i].m_index;
/*    */     }
/*    */   }
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 63 */     SchemaRelationConfig rset = new SchemaRelationConfig();
/* 64 */     initShallow(rset);
/* 65 */     return rset;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaRelationConfig
 * JD-Core Version:    0.5.4
 */