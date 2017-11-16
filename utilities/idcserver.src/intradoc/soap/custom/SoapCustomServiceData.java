/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.soap.SoapUtils;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomServiceData
/*     */ {
/*     */   public String m_name;
/*     */   public String m_idcName;
/*     */   public SoapCustomWsdlData m_wsdlData;
/*     */   public String m_requestParamsTableName;
/*     */   public String m_responseParamsTableName;
/*     */   public SoapCustomParamsData m_requestParamsData;
/*     */   public SoapCustomParamsData m_responseParamsData;
/*     */ 
/*     */   public SoapCustomServiceData()
/*     */   {
/*  30 */     this.m_name = null;
/*  31 */     this.m_idcName = null;
/*  32 */     this.m_wsdlData = null;
/*     */ 
/*  34 */     this.m_requestParamsTableName = null;
/*  35 */     this.m_responseParamsTableName = null;
/*     */ 
/*  37 */     this.m_requestParamsData = null;
/*  38 */     this.m_responseParamsData = null;
/*     */   }
/*     */ 
/*     */   public void init(SoapCustomWsdlData wsdlData, String name, String idcName) throws DataException, ServiceException
/*     */   {
/*  43 */     this.m_name = name;
/*  44 */     this.m_idcName = idcName;
/*  45 */     this.m_wsdlData = wsdlData;
/*     */ 
/*  47 */     String tableNamePrefix = "SoapCustom:Wsdl:" + this.m_wsdlData.m_name + ":Service:" + this.m_name + ":";
/*     */ 
/*  49 */     this.m_requestParamsTableName = (tableNamePrefix + "RequestParams");
/*  50 */     this.m_responseParamsTableName = (tableNamePrefix + "ResponseParams");
/*     */ 
/*  53 */     this.m_requestParamsData = new SoapCustomParamsData();
/*  54 */     this.m_responseParamsData = new SoapCustomParamsData();
/*     */ 
/*  57 */     initParameters(true);
/*  58 */     initParameters(false);
/*     */ 
/*  61 */     this.m_requestParamsData.addSimpleElement("extraProps", "propertylist", "");
/*     */ 
/*  64 */     createStatusInfoData();
/*  65 */     this.m_responseParamsData.addComplexElement("StatusInfo", "StatusInfo", "");
/*     */   }
/*     */ 
/*     */   public void initParameters(boolean isRequest)
/*     */     throws DataException, ServiceException
/*     */   {
/*  71 */     SoapCustomParamsData paramsData = null;
/*  72 */     if (isRequest)
/*     */     {
/*  74 */       paramsData = this.m_requestParamsData;
/*     */     }
/*     */     else
/*     */     {
/*  78 */       paramsData = this.m_responseParamsData;
/*     */     }
/*     */ 
/*  82 */     DataResultSet drset = SoapCustomUtils.getServiceElements(this.m_wsdlData.m_name, this.m_name, isRequest);
/*     */ 
/*  85 */     paramsData.m_wsdlComplexTypeMap = this.m_wsdlData.m_complexTypeMap;
/*  86 */     paramsData.init("ServiceParameters", drset);
/*  87 */     paramsData.load();
/*     */   }
/*     */ 
/*     */   protected void createStatusInfoData() throws DataException, ServiceException
/*     */   {
/*  92 */     if (this.m_wsdlData.m_complexTypeMap.get("StatusInfo") != null)
/*     */       return;
/*  94 */     SoapCustomContainerData statusInfoComplexData = new SoapCustomContainerData();
/*  95 */     statusInfoComplexData.init("StatusInfo", null);
/*     */ 
/*  97 */     statusInfoComplexData.addSimpleElement("statusCode", "field:int", "StatusCode");
/*  98 */     statusInfoComplexData.addSimpleElement("statusMessage", "field:string", "StatusMessage");
/*     */ 
/* 100 */     this.m_wsdlData.m_complexTypeList.addElement(statusInfoComplexData);
/* 101 */     this.m_wsdlData.m_complexTypeMap.put("StatusInfo", statusInfoComplexData);
/*     */   }
/*     */ 
/*     */   public void parseRequest(DataBinder data, PropertiesTreeNode node)
/*     */   {
/* 107 */     data.putLocal("IdcService", this.m_idcName);
/* 108 */     this.m_requestParamsData.parseRequest(data, node, null);
/*     */   }
/*     */ 
/*     */   public void sendResponse(DataBinder data, StringBuffer buffer)
/*     */     throws IOException
/*     */   {
/* 114 */     buffer.append("<idc:" + this.m_name + "Response xmlns:idc=\"" + this.m_wsdlData.m_namespace + "\">\r\n");
/*     */ 
/* 116 */     buffer.append("<idc:" + this.m_name + "Result>\r\n");
/*     */ 
/* 118 */     this.m_responseParamsData.sendResponse(data, buffer, null);
/*     */ 
/* 120 */     buffer.append("</idc:" + this.m_name + "Result>\r\n");
/* 121 */     buffer.append("</idc:" + this.m_name + "Response>\r\n");
/*     */   }
/*     */ 
/*     */   public void writeSchemaElement(Writer writer, int numTabs) throws IOException
/*     */   {
/* 126 */     numTabs = SoapUtils.writeSchemaComplexElement(this.m_name, true, writer, numTabs);
/* 127 */     this.m_requestParamsData.writeSchemaElement(writer, numTabs);
/* 128 */     numTabs = SoapUtils.writeSchemaComplexElement(null, false, writer, numTabs);
/*     */ 
/* 130 */     numTabs = SoapUtils.writeSchemaComplexElement(this.m_name + "Response", true, writer, numTabs);
/*     */ 
/* 132 */     SoapUtils.writeSchemaElement(this.m_name + "Result", "s0:" + this.m_name + "Result", 1, writer, numTabs);
/*     */ 
/* 134 */     numTabs = SoapUtils.writeSchemaComplexElement(null, false, writer, numTabs);
/*     */ 
/* 136 */     numTabs = SoapUtils.writeSchemaComplexType(this.m_name + "Result", true, writer, numTabs);
/* 137 */     this.m_responseParamsData.writeSchemaElement(writer, numTabs);
/* 138 */     numTabs = SoapUtils.writeSchemaComplexType(null, false, writer, numTabs);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 143 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomServiceData
 * JD-Core Version:    0.5.4
 */