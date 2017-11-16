/*    */ package intradoc.soap.custom;
/*    */ 
/*    */ import intradoc.common.PropertiesTreeNode;
/*    */ import intradoc.data.DataBinder;
/*    */ import java.io.IOException;
/*    */ import java.io.Writer;
/*    */ 
/*    */ public abstract class SoapCustomElementData
/*    */ {
/*    */   public String m_name;
/*    */   public String m_type;
/*    */   public String m_subType;
/*    */   public String m_idcName;
/*    */   public String m_wsdlType;
/*    */   public int m_maxOccurs;
/*    */ 
/*    */   public SoapCustomElementData()
/*    */   {
/* 29 */     this.m_name = null;
/* 30 */     this.m_type = null;
/* 31 */     this.m_subType = null;
/* 32 */     this.m_idcName = null;
/* 33 */     this.m_wsdlType = null;
/* 34 */     this.m_maxOccurs = 1;
/*    */   }
/*    */ 
/*    */   public void init(String name, String type, String subType, String idcName) {
/* 38 */     this.m_name = name;
/* 39 */     this.m_type = type;
/* 40 */     this.m_subType = subType;
/* 41 */     this.m_idcName = idcName;
/*    */ 
/* 43 */     if ((this.m_idcName != null) && (!this.m_idcName.trim().equals("")))
/*    */       return;
/* 45 */     this.m_idcName = name;
/*    */   }
/*    */ 
/*    */   public abstract String getDataTypes();
/*    */ 
/*    */   public abstract void parseRequest(DataBinder paramDataBinder, PropertiesTreeNode paramPropertiesTreeNode);
/*    */ 
/*    */   public abstract void sendResponse(DataBinder paramDataBinder, StringBuffer paramStringBuffer)
/*    */     throws IOException;
/*    */ 
/*    */   public abstract void writeSchemaElement(Writer paramWriter, int paramInt)
/*    */     throws IOException;
/*    */ 
/*    */   public abstract void writeSchemaComplexType(Writer paramWriter, int paramInt)
/*    */     throws IOException;
/*    */ 
/*    */   public static Object idcVersionInfo(Object arg)
/*    */   {
/* 64 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 81032 $";
/*    */   }
/*    */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomElementData
 * JD-Core Version:    0.5.4
 */