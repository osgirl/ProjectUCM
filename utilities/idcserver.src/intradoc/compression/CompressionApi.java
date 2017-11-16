/*    */ package intradoc.compression;
/*    */ 
/*    */ public class CompressionApi
/*    */ {
/*    */   public native String getImageInfo(String paramString1, String paramString2);
/*    */ 
/*    */   public native String compressDecompressImage(String[] paramArrayOfString, String paramString, boolean paramBoolean);
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 58 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ 
/*    */   static
/*    */   {
/* 53 */     System.loadLibrary(CompressionConfig.getLibName());
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.compression.CompressionApi
 * JD-Core Version:    0.5.4
 */