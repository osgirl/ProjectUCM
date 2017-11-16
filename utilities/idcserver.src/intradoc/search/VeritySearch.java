/*    */ package intradoc.search;
/*    */ 
/*    */ public class VeritySearch
/*    */ {
/*    */   public long m_handle;
/*    */ 
/*    */   public VeritySearch()
/*    */   {
/* 44 */     this.m_handle = 0L; } 
/*    */   public native String openSession(String paramString1, String paramString2, String paramString3, String paramString4);
/*    */ 
/*    */   public native String attachCollection(String paramString);
/*    */ 
/*    */   public native String showAttachedCollections();
/*    */ 
/*    */   public native String detachCollection(String paramString);
/*    */ 
/*    */   public native String doQuery(String paramString1, String paramString2, int paramInt1, int paramInt2, int paramInt3, String paramString3);
/*    */ 
/*    */   public native String retrieveHighlightInfo(String paramString1, String paramString2, String paramString3, int paramInt, String paramString4, String paramString5);
/*    */ 
/*    */   public native String viewDoc(String paramString1, String paramString2, String paramString3, int paramInt);
/*    */ 
/*    */   public native String getResult();
/*    */ 
/*    */   public native String retrieveDocInfo(String paramString1, String paramString2, int paramInt);
/*    */ 
/*    */   public native void closeSession();
/*    */ 
/* 53 */   public static Object idcVersionInfo(Object arg) { return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $"; }
/*    */ 
/*    */ 
/*    */   static
/*    */   {
/* 48 */     System.loadLibrary(SearchConfig.getLibName());
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.VeritySearch
 * JD-Core Version:    0.5.4
 */