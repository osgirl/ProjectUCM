/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.data.DataException;
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.data.FieldInfo;
/*    */ import intradoc.data.ResultSetUtils;
/*    */ import java.util.Hashtable;
/*    */ 
/*    */ public class DocFieldUtils
/*    */ {
/* 29 */   public static String[] m_docComputedFields = { "dID", "dRevClassID", "dStatus", "dReleaseState", "dIndexerState", "dWorkflowState", "dRevRank", "dCreateDate", "dProcessingState", "dIsCheckedOut", "dCheckoutUser", "dRevLabel", "dWebExtension", "dDocID", "dIsPrimary", "dIsWebFormat", "dLocation", "dOriginalName", "dFormat", "dExtension", "dFileSize" };
/*    */ 
/* 34 */   public static Hashtable m_checkDocComputedFields = null;
/*    */ 
/*    */   public static void setFieldTypes(DataBinder binder) throws DataException {
/* 37 */     DataResultSet drset = SharedObjects.getTable("DocMetaDefinition");
/* 38 */     if (drset != null)
/*    */     {
/* 40 */       FieldInfo[] infos = ResultSetUtils.createInfoList(drset, new String[] { "dName", "dType" }, true);
/*    */ 
/* 42 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*    */       {
/* 44 */         String name = drset.getStringValue(infos[0].m_index);
/* 45 */         String type = drset.getStringValue(infos[1].m_index);
/* 46 */         binder.setFieldType(name, type.toLowerCase());
/*    */       }
/*    */     }
/*    */ 
/* 50 */     binder.setFieldType("dInDate", "date");
/* 51 */     binder.setFieldType("dOutDate", "date");
/* 52 */     binder.setFieldType("dReleaseDate", "date");
/* 53 */     binder.setFieldType("dCreateDate", "date");
/*    */ 
/* 55 */     binder.setFieldType("dMessage", "message");
/*    */   }
/*    */ 
/*    */   public static boolean isDocComputedField(String key)
/*    */   {
/* 60 */     if (key == null)
/*    */     {
/* 62 */       return false;
/*    */     }
/* 64 */     if (m_checkDocComputedFields == null)
/*    */     {
/* 66 */       Hashtable table = new Hashtable();
/* 67 */       for (int i = 0; i < m_docComputedFields.length; ++i)
/*    */       {
/* 69 */         table.put(m_docComputedFields[i], Boolean.TRUE);
/*    */       }
/* 71 */       m_checkDocComputedFields = table;
/*    */     }
/* 73 */     if (key.startsWith("dRendition"))
/*    */     {
/* 75 */       return true;
/*    */     }
/* 77 */     return m_checkDocComputedFields.get(key) != null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 82 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87113 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.DocFieldUtils
 * JD-Core Version:    0.5.4
 */