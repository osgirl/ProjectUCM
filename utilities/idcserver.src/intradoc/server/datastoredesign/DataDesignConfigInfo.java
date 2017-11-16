/*    */ package intradoc.server.datastoredesign;
/*    */ 
/*    */ import java.util.HashMap;
/*    */ import java.util.Map;
/*    */ 
/*    */ public class DataDesignConfigInfo
/*    */ {
/*    */   public String m_queryStr;
/*    */   public String m_sectionPrefix;
/*    */   public Map<String, String> m_curConfigTableValues;
/*    */   public Map<String, String> m_versionMap;
/*    */   public Map<String, String> m_configTableValues;
/*    */ 
/*    */   public DataDesignConfigInfo()
/*    */   {
/* 36 */     this.m_curConfigTableValues = new HashMap();
/* 37 */     this.m_configTableValues = new HashMap();
/* 38 */     this.m_versionMap = new HashMap();
/*    */   }
/*    */ 
/*    */   public DataDesignConfigInfo(Map<String, String> curConfigTableValues, Map<String, String> configTableValues)
/*    */   {
/* 44 */     this.m_curConfigTableValues = curConfigTableValues;
/* 45 */     this.m_configTableValues = configTableValues;
/* 46 */     this.m_versionMap = new HashMap();
/*    */   }
/*    */ 
/*    */   public void setQueryInfo(String queryStr, String sectionPrefix)
/*    */   {
/* 51 */     this.m_queryStr = queryStr;
/* 52 */     this.m_sectionPrefix = sectionPrefix;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 57 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.datastoredesign.DataDesignConfigInfo
 * JD-Core Version:    0.5.4
 */