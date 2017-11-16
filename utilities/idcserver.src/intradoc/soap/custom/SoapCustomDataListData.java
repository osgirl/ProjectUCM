/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import intradoc.data.DataException;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class SoapCustomDataListData extends SoapCustomComplexData
/*    */ {
/* 30 */   public static Properties m_chainedListMap = null;
/*    */   public boolean m_isLoaded;
/*    */ 
/*    */   public SoapCustomDataListData()
/*    */   {
/* 32 */     this.m_isLoaded = false;
/*    */   }
/*    */ 
/*    */   public void load() throws DataException, ServiceException
/*    */   {
/* 37 */     if (this.m_isLoaded)
/*    */       return;
/* 39 */     m_chainedListMap.put(this.m_name, "1");
/*    */ 
/* 41 */     super.load();
/*    */ 
/* 43 */     m_chainedListMap.remove(this.m_name);
/* 44 */     this.m_isLoaded = true;
/*    */   }
/*    */ 
/*    */   public void addDataListElements(String dataListName)
/*    */     throws DataException, ServiceException
/*    */   {
/* 54 */     if (m_chainedListMap.getProperty(dataListName) != null)
/*    */     {
/* 57 */       return;
/*    */     }
/*    */ 
/* 60 */     super.addDataListElements(dataListName);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 66 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomDataListData
 * JD-Core Version:    0.5.4
 */