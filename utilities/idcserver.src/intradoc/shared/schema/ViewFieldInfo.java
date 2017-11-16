/*    */ package intradoc.shared.schema;
/*    */ 
/*    */ import intradoc.data.DataException;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class ViewFieldInfo
/*    */ {
/*    */   public boolean m_needsLocalization;
/*    */   public List m_fieldList;
/*    */   public Map m_fieldMap;
/* 38 */   public DataException m_localizationException = null;
/*    */ 
/*    */   public ViewFieldInfo(List fieldList, Map fieldMap)
/*    */   {
/* 42 */     this.m_fieldList = fieldList;
/* 43 */     this.m_fieldMap = fieldMap;
/* 44 */     this.m_needsLocalization = true;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 49 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.ViewFieldInfo
 * JD-Core Version:    0.5.4
 */