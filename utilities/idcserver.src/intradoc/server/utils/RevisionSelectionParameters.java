/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.data.DataResultSet;
/*     */ 
/*     */ public class RevisionSelectionParameters
/*     */ {
/*     */   public boolean m_isBatchSelection;
/*     */   public String m_revisionSelectionMethod;
/*     */   public boolean m_selectionMethodIsValid;
/*     */   public boolean m_doesLatestReleased;
/*     */   public boolean m_useLatestReleasedDocInfoCache;
/*     */   public boolean m_suppressErrorRetrievingDocInfo;
/*     */   public boolean m_computeDocInfo;
/*     */   public boolean m_haveDocInfo;
/*     */   public boolean m_haveRevID;
/*     */   public String m_docName;
/*     */   public String[] m_docNames;
/*     */   public String m_id;
/*     */   public String[] m_ids;
/*     */   public String m_computationType;
/*     */   public boolean m_isResource;
/*     */   public String m_queryKey;
/*     */   public String m_docInfoQuery;
/*     */   public String m_computeIdQuery;
/*     */   public long m_currentTime;
/*     */   public DataResultSet m_docInfo;
/*     */   public boolean m_isError;
/*     */   public String m_errMsg;
/*     */ 
/*     */   public RevisionSelectionParameters(String revisionSelectionMethod, String docInfoQuery, String queryKey)
/*     */   {
/* 144 */     this.m_revisionSelectionMethod = revisionSelectionMethod;
/* 145 */     this.m_docInfoQuery = docInfoQuery;
/* 146 */     this.m_queryKey = queryKey;
/* 147 */     this.m_computeDocInfo = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 152 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.RevisionSelectionParameters
 * JD-Core Version:    0.5.4
 */