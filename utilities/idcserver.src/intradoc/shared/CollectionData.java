/*    */ package intradoc.shared;
/*    */ 
/*    */ import intradoc.common.FileUtils;
/*    */ 
/*    */ public class CollectionData
/*    */   implements MonikerInterface
/*    */ {
/* 28 */   public int m_id = 0;
/*    */   public String m_name;
/*    */   public String m_location;
/*    */   public String m_exportLocation;
/*    */   public String m_vaultDir;
/*    */   public String m_weblayoutDir;
/*    */ 
/*    */   public CollectionData(int id, String name, String location, String exportLocation, String vaultDir, String weblayoutDir)
/*    */   {
/* 39 */     this.m_id = id;
/* 40 */     this.m_name = name;
/* 41 */     this.m_location = location;
/* 42 */     this.m_exportLocation = exportLocation;
/* 43 */     if (!isProxied())
/*    */     {
/* 45 */       this.m_location = FileUtils.directorySlashes(location);
/* 46 */       this.m_exportLocation = FileUtils.directorySlashes(exportLocation);
/*    */     }
/* 48 */     this.m_vaultDir = FileUtils.directorySlashes(vaultDir);
/* 49 */     this.m_weblayoutDir = FileUtils.directorySlashes(weblayoutDir);
/*    */   }
/*    */ 
/*    */   public String getMonikerName()
/*    */   {
/* 54 */     return this.m_name;
/*    */   }
/*    */ 
/*    */   public String getMonikerLocation()
/*    */   {
/* 59 */     return this.m_location;
/*    */   }
/*    */ 
/*    */   public String getMoniker()
/*    */   {
/* 64 */     return "archives://" + this.m_name + "/collection.mrk";
/*    */   }
/*    */ 
/*    */   public String getSubMoniker(String subName)
/*    */   {
/* 69 */     return "archives://" + this.m_name + "/" + subName.toLowerCase() + "/exports.hda";
/*    */   }
/*    */ 
/*    */   public boolean isProxied()
/*    */   {
/* 74 */     return (this.m_location.startsWith("idc://")) && (!this.m_location.startsWith("idc://idcproviders/"));
/*    */   }
/*    */ 
/*    */   public String getProxiedServer()
/*    */   {
/* 79 */     int index = this.m_location.indexOf("idc://");
/* 80 */     if (index < 0)
/*    */     {
/* 82 */       return null;
/*    */     }
/*    */ 
/* 85 */     return this.m_location.substring(index + 6);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 90 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97779 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CollectionData
 * JD-Core Version:    0.5.4
 */