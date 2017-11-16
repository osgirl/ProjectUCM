/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.data.DataBinder;
/*    */ import java.io.IOException;
/*    */ 
/*    */ public class SoapCustomContainerData extends SoapCustomComplexData
/*    */ {
/*    */   public void sendResponse(DataBinder data, StringBuffer buffer, SoapCustomComplexElementData soapElementData)
/*    */     throws IOException
/*    */   {
/* 32 */     buffer.append("<idc:" + this.m_name + ">\r\n");
/* 33 */     super.sendResponse(data, buffer, soapElementData);
/* 34 */     buffer.append("</idc:" + this.m_name + ">\r\n");
/*    */   }
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 39 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomContainerData
 * JD-Core Version:    0.5.4
 */