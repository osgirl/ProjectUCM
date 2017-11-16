/*    */ package intradoc.soap.generic;
/*    */ 
/*    */ import intradoc.common.PropertiesTreeNode;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.soap.SoapUtils;
/*    */ import java.util.Vector;
/*    */ 
/*    */ public class SoapGenericFileData
/*    */   implements SoapGenericData
/*    */ {
/*    */   public void init(SoapGenericSerializer serializer)
/*    */   {
/*    */   }
/*    */ 
/*    */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*    */   {
/* 37 */     String fieldName = SoapUtils.getNodeProperty(node, "name");
/* 38 */     if (fieldName == null)
/*    */     {
/* 40 */       return;
/*    */     }
/*    */ 
/* 43 */     String filePath = SoapUtils.getNodeProperty(node, "href");
/* 44 */     if (filePath == null)
/*    */     {
/* 46 */       return;
/*    */     }
/*    */ 
/* 49 */     data.putLocal(fieldName, filePath);
/*    */ 
/* 52 */     Vector subNodes = node.m_subNodes;
/* 53 */     if (subNodes == null)
/*    */     {
/* 55 */       return;
/*    */     }
/* 57 */     SoapUtils.decodeFileContent(data, fieldName);
/*    */   }
/*    */ 
/*    */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*    */   {
/* 62 */     String fileName = data.getEnvironmentValue("SOAP:downloadName");
/* 63 */     if (fileName == null)
/*    */     {
/* 65 */       return;
/*    */     }
/* 67 */     fileName = SoapUtils.encodeXmlValue(fileName);
/*    */ 
/* 70 */     String serializerName = data.getEnvironmentValue("SOAP:Serializer");
/* 71 */     if ((serializerName != null) && (!serializerName.equals("xml"))) {
/*    */       return;
/*    */     }
/* 74 */     buffer.append("<idc:file name=\"downloadFile\" href=\"" + fileName + "\">");
/*    */ 
/* 77 */     int length = buffer.length();
/* 78 */     data.setEnvironmentValue("SOAP:startFileContentIndex", "" + length);
/*    */ 
/* 80 */     buffer.append("</idc:file>\r\n");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 87 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericFileData
 * JD-Core Version:    0.5.4
 */