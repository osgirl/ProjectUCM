/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.data.FieldInfo;
/*     */ 
/*     */ class ArchiveImportFieldData
/*     */ {
/* 710 */   public boolean m_isRetained = true;
/* 711 */   public String m_mappedField = null;
/* 712 */   public FieldInfo m_fieldInfo = null;
/*     */ 
/*     */   public ArchiveImportFieldData(String field)
/*     */   {
/* 716 */     this.m_mappedField = field;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 721 */     return "releaseInfo=dev,releaseRevision=$Rev: 95262 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveImportFieldData
 * JD-Core Version:    0.5.4
 */