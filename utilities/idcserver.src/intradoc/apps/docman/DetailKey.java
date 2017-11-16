/*     */ package intradoc.apps.docman;
/*     */ 
/*     */ class DetailKey
/*     */ {
/*     */   public String m_docName;
/*     */   public String m_scpType;
/*     */   public String m_userAlias;
/*     */ 
/*     */   public DetailKey(String docName, String aliasName, String aliasType, String scpType)
/*     */   {
/* 576 */     this.m_docName = docName;
/* 577 */     this.m_scpType = scpType;
/* 578 */     this.m_userAlias = (aliasType + ":" + aliasName);
/*     */   }
/*     */ 
/*     */   public int hashCode()
/*     */   {
/* 584 */     return this.m_docName.hashCode() * this.m_scpType.hashCode() * this.m_userAlias.hashCode();
/*     */   }
/*     */ 
/*     */   public boolean equals(Object that)
/*     */   {
/* 590 */     DetailKey t = (DetailKey)that;
/* 591 */     return (this.m_docName.equals(t.m_docName)) && (this.m_scpType.equals(t.m_scpType)) && (this.m_userAlias.equals(t.m_userAlias));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 598 */     return "releaseInfo=dev,releaseRevision=$Rev: 83339 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.apps.docman.DetailKey
 * JD-Core Version:    0.5.4
 */