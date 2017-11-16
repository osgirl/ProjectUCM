/*     */ package intradoc.provider;
/*     */ 
/*     */ class FileInfo
/*     */ {
/*     */   public String m_key;
/*     */   public String m_filename;
/*     */   public String m_filePostStr;
/*     */ 
/*     */   public FileInfo(String key, String filename)
/*     */   {
/* 423 */     this.m_key = key;
/* 424 */     this.m_filename = filename;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 429 */     return "releaseInfo=dev,releaseRevision=$Rev: 90487 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.FileInfo
 * JD-Core Version:    0.5.4
 */