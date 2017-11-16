/*    */ package intradoc.search;
/*    */ 
/*    */ public class AvsSearch
/*    */ {
/*    */   public long m_handle;
/*    */ 
/*    */   public AvsSearch()
/*    */   {
/* 29 */     this.m_handle = 0L;
/*    */   }
/*    */ 
/*    */   public native String init(String paramString1, String paramString2);
/*    */ 
/*    */   public native String reloadValues(String paramString1, String paramString2);
/*    */ 
/*    */   public native String close();
/*    */ 
/*    */   public native String doQuery(String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, boolean paramBoolean);
/*    */ 
/*    */   public native String getStemWords(String paramString1, String paramString2, String paramString3);
/*    */ 
/*    */   public native String getThesaurus(String paramString1, String paramString2, String paramString3);
/*    */ 
/*    */   public native String getCorrectSpelling(String paramString1, String paramString2, String paramString3);
/*    */ 
/*    */   public native String getCJKCompoundWords(String paramString1, String paramString2, String paramString3);
/*    */ 
/*    */   public native String convertCJKQuery(String paramString1, String paramString2);
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg) {
/* 51 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ 
/*    */   static
/*    */   {
/* 46 */     System.loadLibrary("AvsSearch");
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.AvsSearch
 * JD-Core Version:    0.5.4
 */