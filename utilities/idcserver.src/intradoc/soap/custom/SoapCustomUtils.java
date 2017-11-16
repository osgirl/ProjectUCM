/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.data.DataResultSet;
/*    */ import intradoc.shared.SharedObjects;
/*    */ 
/*    */ public class SoapCustomUtils
/*    */ {
/*    */   public static DataResultSet getDataLists()
/*    */   {
/* 29 */     String tableName = "SoapCustom:DataLists";
/* 30 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 31 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getDataListElements(String dataListName)
/*    */   {
/* 36 */     String tableName = "SoapCustom:DataList:" + dataListName;
/* 37 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 38 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getWsdlList()
/*    */   {
/* 43 */     String tableName = "SoapCustom:Wsdls";
/* 44 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 45 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getComplexTypeList(String wsdlName)
/*    */   {
/* 50 */     String tableName = "SoapCustom:Wsdl:" + wsdlName + ":ComplexTypes";
/* 51 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 52 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getComplexTypeElements(String wsdlName, String complexName)
/*    */   {
/* 58 */     String tableName = "SoapCustom:Wsdl:" + wsdlName + ":ComplexType:" + complexName;
/*    */ 
/* 60 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 61 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getServiceList(String wsdlName)
/*    */   {
/* 66 */     String tableName = "SoapCustom:Wsdl:" + wsdlName + ":Services";
/* 67 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 68 */     return drset;
/*    */   }
/*    */ 
/*    */   public static DataResultSet getServiceElements(String wsdlName, String serviceName, boolean isRequest)
/*    */   {
/* 74 */     String tableName = "SoapCustom:Wsdl:" + wsdlName + ":Service:" + serviceName;
/*    */ 
/* 76 */     if (isRequest)
/*    */     {
/* 78 */       tableName = tableName + ":RequestParams";
/*    */     }
/*    */     else
/*    */     {
/* 82 */       tableName = tableName + ":ResponseParams";
/*    */     }
/*    */ 
/* 85 */     DataResultSet drset = SharedObjects.getTable(tableName);
/* 86 */     return drset;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 91 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomUtils
 * JD-Core Version:    0.5.4
 */