/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.FieldInfo;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ public class SchemaTargetConfig extends SchemaResultSet
/*    */ {
/*    */   public static final int TARGET_NAME = 0;
/*    */   public static final int CANONICAL_NAME = 1;
/*    */   public static final int TARGET_LAST_LOADED = 2;
/*    */   public static final int TARGET_UPTODATE = 3;
/*    */   public static final int TARGET_SYSTEM = 4;
/* 35 */   public static String[] TARGET_COLUMNS = { "schTargetName", "schCanonicalName", "schTargetLastLoaded", "schTargetIsUpToDate", "schTargetDisplayName", "schIsSystemObject" };
/*    */ 
/*    */   public SchemaTargetConfig()
/*    */   {
/* 47 */     super("SchemaData", TARGET_COLUMNS);
/* 48 */     this.m_infos = new FieldInfo[TARGET_COLUMNS.length];
/* 49 */     this.m_indexes = new int[TARGET_COLUMNS.length];
/* 50 */     for (int i = 0; i < TARGET_COLUMNS.length; ++i)
/*    */     {
/* 52 */       this.m_infos[i] = new FieldInfo();
/* 53 */       getIndexFieldInfo(i, this.m_infos[i]);
/* 54 */       this.m_indexes[i] = this.m_infos[i].m_index;
/*    */     }
/*    */   }
/*    */ 
/*    */   public DataResultSet shallowClone()
/*    */   {
/* 61 */     SchemaTargetConfig rset = new SchemaTargetConfig();
/* 62 */     initShallow(rset);
/* 63 */     return rset;
/*    */   }
/*    */ 
/*    */   public List getValidResultSets()
/*    */   {
/* 69 */     ArrayList sets = new ArrayList();
/* 70 */     sets.add("TargetFieldInfo");
/* 71 */     return sets;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 76 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaTargetConfig
 * JD-Core Version:    0.5.4
 */