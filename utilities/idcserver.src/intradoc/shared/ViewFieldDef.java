/*    */ package intradoc.shared;
/*    */ 
/*    */ public class ViewFieldDef extends FieldDef
/*    */ {
/*    */   public boolean m_isSearchable;
/*    */   public boolean m_isZoneField;
/*    */   public boolean m_isCustomMeta;
/*    */   public boolean m_hasView;
/*    */   public boolean m_hasNamedRelation;
/*    */   public boolean m_isStandardDateField;
/*    */   public boolean m_isAppField;
/*    */ 
/*    */   public ViewFieldDef()
/*    */   {
/* 34 */     this.m_isSearchable = true;
/*    */ 
/* 37 */     this.m_isZoneField = false;
/*    */ 
/* 40 */     this.m_isCustomMeta = false;
/*    */ 
/* 43 */     this.m_hasView = false;
/*    */ 
/* 46 */     this.m_hasNamedRelation = false;
/*    */ 
/* 50 */     this.m_isStandardDateField = false;
/*    */ 
/* 53 */     this.m_isAppField = false;
/*    */   }
/*    */ 
/*    */   public void copy(ViewFieldDef fd)
/*    */   {
/* 58 */     super.copy(fd);
/*    */ 
/* 60 */     this.m_isSearchable = fd.m_isSearchable;
/* 61 */     this.m_isCustomMeta = fd.m_isCustomMeta;
/* 62 */     this.m_hasView = fd.m_hasView;
/* 63 */     this.m_hasNamedRelation = fd.m_hasNamedRelation;
/* 64 */     this.m_isStandardDateField = fd.m_isStandardDateField;
/* 65 */     this.m_isAppField = fd.m_isAppField;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 70 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ViewFieldDef
 * JD-Core Version:    0.5.4
 */