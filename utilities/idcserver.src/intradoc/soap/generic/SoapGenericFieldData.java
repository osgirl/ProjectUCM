/*    */ package intradoc.soap.generic;
/*    */ 
/*    */ import intradoc.common.PropertiesTreeNode;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.soap.SoapUtils;
/*    */ import intradoc.soap.SoapXmlSerializer;
/*    */ import java.util.Enumeration;
/*    */ import java.util.Properties;
/*    */ 
/*    */ public class SoapGenericFieldData
/*    */   implements SoapGenericData
/*    */ {
/*    */   protected SoapGenericSerializer m_serializer;
/*    */ 
/*    */   public SoapGenericFieldData()
/*    */   {
/* 30 */     this.m_serializer = null;
/*    */   }
/*    */ 
/*    */   public void init(SoapGenericSerializer serializer) {
/* 34 */     this.m_serializer = serializer;
/*    */   }
/*    */ 
/*    */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*    */   {
/* 39 */     String fieldName = SoapUtils.getNodeProperty(node, "name");
/* 40 */     if (fieldName == null)
/*    */       return;
/* 42 */     data.putLocal(fieldName, node.m_value);
/*    */   }
/*    */ 
/*    */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*    */   {
/* 49 */     Enumeration en = data.m_localData.keys();
/* 50 */     while (en.hasMoreElements())
/*    */     {
/* 52 */       String fieldName = (String)en.nextElement();
/* 53 */       if (this.m_serializer.canAddLocalData(fieldName, false))
/*    */       {
/* 55 */         String fieldValue = data.getLocal(fieldName);
/* 56 */         fieldValue = SoapUtils.encodeXmlValue(fieldValue);
/*    */ 
/* 58 */         buffer.append("<idc:field name=\"");
/* 59 */         buffer.append(fieldName);
/* 60 */         buffer.append("\">");
/* 61 */         if (SoapXmlSerializer.m_useCDATA)
/*    */         {
/* 63 */           buffer.append("<![CDATA[");
/*    */         }
/* 65 */         buffer.append(fieldValue);
/* 66 */         if (SoapXmlSerializer.m_useCDATA)
/*    */         {
/* 68 */           buffer.append("]]>");
/*    */         }
/* 70 */         buffer.append("</idc:field>\r\n");
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 77 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.generic.SoapGenericFieldData
 * JD-Core Version:    0.5.4
 */