/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.common.ServiceException;
/*    */ import java.util.Hashtable;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SoapCustomParamsData extends SoapCustomComplexData
/*    */ {
/*    */   public Hashtable m_wsdlComplexTypeMap;
/*    */ 
/*    */   public SoapCustomParamsData()
/*    */   {
/* 29 */     this.m_wsdlComplexTypeMap = null;
/*    */   }
/*    */ 
/*    */   public void addComplexElement(String dataName, String dataType, String dataIdcName)
/*    */     throws ServiceException
/*    */   {
/* 35 */     if (this.m_wsdlComplexTypeMap == null)
/*    */     {
/* 37 */       return;
/*    */     }
/*    */ 
/* 41 */     if (this.m_customDataMap.get(dataName) != null)
/*    */     {
/* 43 */       return;
/*    */     }
/*    */ 
/* 47 */     SoapCustomComplexData soapComplexData = (SoapCustomComplexData)this.m_wsdlComplexTypeMap.get(dataType);
/*    */ 
/* 49 */     if (soapComplexData == null)
/*    */     {
/* 51 */       return;
/*    */     }
/*    */ 
/* 55 */     SoapCustomComplexElementData soapElementData = new SoapCustomComplexElementData();
/* 56 */     soapElementData.m_complexData = soapComplexData;
/* 57 */     soapElementData.init(dataName, "", "", dataIdcName);
/*    */ 
/* 60 */     this.m_customDataList.addElement(soapElementData);
/* 61 */     this.m_customDataMap.put(dataName, soapElementData);
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 66 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomParamsData
 * JD-Core Version:    0.5.4
 */