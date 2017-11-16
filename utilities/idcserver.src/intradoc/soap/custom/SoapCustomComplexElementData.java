/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.common.PropertiesTreeNode;
/*    */ import intradoc.data.DataBinder;
/*    */ import intradoc.soap.SoapUtils;
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ 
/*    */ public class SoapCustomComplexElementData extends SoapCustomElementData
/*    */ {
/*    */   protected SoapCustomComplexData m_complexData;
/*    */ 
/*    */   public SoapCustomComplexElementData()
/*    */   {
/* 30 */     this.m_complexData = null;
/*    */   }
/*    */ 
/*    */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*    */   {
/* 35 */     if (this.m_complexData == null)
/*    */     {
/* 37 */       return;
/*    */     }
/* 39 */     this.m_complexData.parseRequest(data, node, this);
/*    */   }
/*    */ 
/*    */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*    */     throws IOException
/*    */   {
/* 46 */     if (this.m_complexData == null)
/*    */     {
/* 48 */       return;
/*    */     }
/* 50 */     this.m_complexData.sendResponse(data, buffer, this);
/*    */   }
/*    */ 
/*    */   public void writeSchemaElement(Writer writer, int numTabs)
/*    */     throws IOException
/*    */   {
/* 57 */     if (this.m_complexData == null)
/*    */     {
/* 59 */       return;
/*    */     }
/* 61 */     SoapUtils.writeSchemaElement(this.m_name, "s0:" + this.m_complexData.m_name, this.m_complexData.m_maxOccurs, writer, numTabs);
/*    */   }
/*    */ 
/*    */   public void writeSchemaComplexType(Writer writer, int numTabs)
/*    */     throws IOException
/*    */   {
/*    */   }
/*    */ 
/*    */   public String getDataTypes()
/*    */   {
/* 75 */     return null;
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 80 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomComplexElementData
 * JD-Core Version:    0.5.4
 */