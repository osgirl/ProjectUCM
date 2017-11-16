/*    */ package intradoc.data;
/*    */ 
/*    */ import intradoc.util.IdcVector;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SimpleQueryInfo
/*    */ {
/*    */   public static final short SELECT_TYPE = 0;
/*    */   public static final short UPDATE_TYPE = 1;
/*    */   public int m_type;
/*    */   public String m_name;
/*    */   public String m_dataEntityName;
/*    */   public Vector m_fields;
/*    */   public Vector m_qualifiers;
/*    */ 
/*    */   public SimpleQueryInfo()
/*    */   {
/* 78 */     this.m_name = null;
/* 79 */     this.m_dataEntityName = null;
/* 80 */     this.m_fields = new IdcVector();
/* 81 */     this.m_qualifiers = new IdcVector();
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 86 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.SimpleQueryInfo
 * JD-Core Version:    0.5.4
 */