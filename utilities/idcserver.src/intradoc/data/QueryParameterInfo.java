/*     */ package intradoc.data;
/*     */ 
/*     */ public class QueryParameterInfo extends FieldInfo
/*     */ {
/*     */   public static final short COLUMN = -101;
/*     */   public static final short TABLE = -102;
/*     */   public static final short RESULTSET = -201;
/*  43 */   public String m_default = null;
/*     */ 
/*  49 */   public String m_alternateName = null;
/*     */ 
/*  55 */   public boolean m_isOutput = false;
/*     */ 
/*  61 */   public boolean m_isInput = true;
/*     */ 
/*  66 */   public boolean m_isList = false;
/*     */ 
/*     */   public QueryParameterInfo()
/*     */   {
/*  74 */     this.m_index = -1;
/*  75 */     this.m_name = null;
/*  76 */     this.m_type = 6;
/*  77 */     this.m_isFixedLen = false;
/*  78 */     this.m_maxLen = 0;
/*     */   }
/*     */ 
/*     */   public void copy(FieldInfo info)
/*     */   {
/*  88 */     super.copy(info);
/*  89 */     if (!info instanceof QueryParameterInfo)
/*     */       return;
/*  91 */     this.m_default = ((QueryParameterInfo)info).m_default;
/*  92 */     this.m_alternateName = ((QueryParameterInfo)info).m_alternateName;
/*  93 */     this.m_isOutput = ((QueryParameterInfo)info).m_isOutput;
/*  94 */     this.m_isInput = ((QueryParameterInfo)info).m_isInput;
/*     */   }
/*     */ 
/*     */   public String getTypeName()
/*     */   {
/* 104 */     String type = null;
/*     */ 
/* 106 */     switch (this.m_type)
/*     */     {
/*     */     case -101:
/* 109 */       type = "Column";
/* 110 */       break;
/*     */     case -102:
/* 112 */       type = "Table";
/* 113 */       break;
/*     */     case -201:
/* 115 */       type = "ResultSet";
/*     */     }
/*     */ 
/* 118 */     if (type == null)
/*     */     {
/* 120 */       type = super.getTypeName();
/*     */     }
/* 122 */     return type;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 126 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.QueryParameterInfo
 * JD-Core Version:    0.5.4
 */