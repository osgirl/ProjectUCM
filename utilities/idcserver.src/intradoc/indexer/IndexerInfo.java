/*    */ package intradoc.indexer;
/*    */ 
/*    */ public class IndexerInfo
/*    */   implements Cloneable
/*    */ {
/*    */   public static final int WAITING = -1;
/*    */   public static final int INDEXED = 0;
/*    */   public static final int SKIPPED = 1;
/*    */   public static final int TIMEOUT = 2;
/*    */   public static final int FAILURE = 3;
/*    */   public static final int INDEX_ABORT = 4;
/*    */   public static final int INDEXING = 5;
/*    */   public static final int CONVERTED = 0;
/*    */   public static final int ENCRYPTED = 1;
/*    */   public static final int NOTSUPPORTED = 4;
/* 39 */   public String m_dID = null;
/* 40 */   public String m_dRevClassID = null;
/* 41 */   public String m_indexKey = null;
/* 42 */   public String m_indexError = null;
/* 43 */   public String m_encodedKey = null;
/* 44 */   public boolean m_isMetaDataOnly = false;
/* 45 */   public boolean m_indexWebFile = false;
/* 46 */   public int m_indexStatus = -1;
/* 47 */   public int m_conversionStatus = -1;
/* 48 */   public boolean m_isDelete = false;
/* 49 */   public boolean m_isUpdate = false;
/* 50 */   public boolean m_processedAlone = false;
/* 51 */   public long m_size = -1L;
/*    */ 
/*    */   public IndexerInfo()
/*    */   {
/*    */   }
/*    */ 
/*    */   public IndexerInfo(String dID, String dRevClassID, String key, boolean metaDataOnly, boolean isSkipped)
/*    */   {
/* 61 */     this.m_dID = dID;
/* 62 */     this.m_dRevClassID = dRevClassID;
/* 63 */     this.m_indexKey = key;
/* 64 */     this.m_isMetaDataOnly = metaDataOnly;
/* 65 */     if (!isSkipped)
/*    */       return;
/* 67 */     this.m_indexStatus = 1;
/*    */   }
/*    */ 
/*    */   public String toString()
/*    */   {
/* 73 */     return super.toString() + " dID:" + this.m_dID + " key:" + this.m_encodedKey + " mdo:" + this.m_isMetaDataOnly + " iwf:" + this.m_indexWebFile + " sta:" + this.m_indexStatus + " conv:" + this.m_conversionStatus + " alone:" + this.m_processedAlone;
/*    */   }
/*    */ 
/*    */   protected Object clone()
/*    */   {
/*    */     try
/*    */     {
/* 83 */       return super.clone();
/*    */     }
/*    */     catch (CloneNotSupportedException e) {
/*    */     }
/* 87 */     return null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 93 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96895 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.indexer.IndexerInfo
 * JD-Core Version:    0.5.4
 */