/*     */ package intradoc.soap.custom;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.server.DirectoryLocator;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.File;
/*     */ import java.io.FileWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.Writer;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapCustomWsdlData
/*     */ {
/*     */   public String m_name;
/*     */   public String m_description;
/*     */   public String m_namespace;
/*     */   public Vector m_complexTypeList;
/*     */   public Hashtable m_complexTypeMap;
/*     */   public String m_complexTypesTableName;
/*     */   public Vector m_serviceList;
/*     */   public Hashtable m_serviceMap;
/*     */   public String m_servicesTableName;
/*  47 */   public static Vector m_simpleDataTypeList = null;
/*  48 */   public static Hashtable m_simpleDataTypeMap = null;
/*     */   public static final String WSDL_HTTP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/http/";
/*     */   public static final String WSDL_SOAP_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
/*     */   public static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
/*     */   public static final String WSDL_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/";
/*     */ 
/*     */   public SoapCustomWsdlData()
/*     */   {
/*  32 */     this.m_name = null;
/*  33 */     this.m_description = null;
/*  34 */     this.m_namespace = null;
/*     */ 
/*  37 */     this.m_complexTypeList = null;
/*  38 */     this.m_complexTypeMap = null;
/*  39 */     this.m_complexTypesTableName = null;
/*     */ 
/*  42 */     this.m_serviceList = null;
/*  43 */     this.m_serviceMap = null;
/*  44 */     this.m_servicesTableName = null;
/*     */   }
/*     */ 
/*     */   public void init(String name, String description)
/*     */     throws DataException, ServiceException
/*     */   {
/*  59 */     this.m_name = name;
/*  60 */     this.m_description = description;
/*  61 */     this.m_namespace = ("http://www.stellent.com/" + this.m_name + "/");
/*     */ 
/*  63 */     initComplexTypes();
/*  64 */     initServices();
/*     */   }
/*     */ 
/*     */   public void initComplexTypes()
/*     */     throws DataException, ServiceException
/*     */   {
/*  70 */     this.m_complexTypeList = new IdcVector();
/*  71 */     this.m_complexTypeMap = new Hashtable();
/*     */ 
/*  74 */     DataResultSet drset = SoapCustomUtils.getComplexTypeList(this.m_name);
/*  75 */     if (drset == null)
/*     */     {
/*  77 */       return;
/*     */     }
/*  79 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "complexName");
/*  80 */     int typeIndex = ResultSetUtils.getIndexMustExist(drset, "complexType");
/*     */ 
/*  82 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  84 */       String complexName = drset.getStringValue(nameIndex);
/*  85 */       String complexType = drset.getStringValue(typeIndex);
/*     */ 
/*  87 */       if (this.m_complexTypeMap.get(complexName) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  93 */       Class dataClass = (Class)SoapCustomSerializer.m_complexDataClassMap.get(complexType);
/*  94 */       if (dataClass == null) {
/*     */         continue;
/*     */       }
/*     */ 
/*  98 */       SoapCustomComplexData soapComplexData = null;
/*     */       try
/*     */       {
/* 101 */         soapComplexData = (SoapCustomComplexData)dataClass.newInstance();
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 105 */         throw new ServiceException(e);
/*     */       }
/*     */ 
/* 108 */       DataResultSet elementSet = SoapCustomUtils.getComplexTypeElements(this.m_name, complexName);
/*     */ 
/* 110 */       soapComplexData.init(complexName, elementSet);
/* 111 */       soapComplexData.load();
/*     */ 
/* 114 */       this.m_complexTypeList.addElement(soapComplexData);
/* 115 */       this.m_complexTypeMap.put(complexName, soapComplexData);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initServices()
/*     */     throws DataException, ServiceException
/*     */   {
/* 122 */     this.m_serviceList = new IdcVector();
/* 123 */     this.m_serviceMap = new Hashtable();
/* 124 */     Hashtable globalServiceMap = SoapCustomSerializer.m_globalServiceMap;
/*     */ 
/* 127 */     DataResultSet drset = SoapCustomUtils.getServiceList(this.m_name);
/* 128 */     if (drset == null)
/*     */     {
/* 130 */       return;
/*     */     }
/* 132 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "serviceName");
/* 133 */     int idcNameIndex = ResultSetUtils.getIndexMustExist(drset, "idcServiceName");
/*     */ 
/* 135 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 137 */       String serviceName = drset.getStringValue(nameIndex);
/* 138 */       String idcServiceName = drset.getStringValue(idcNameIndex);
/*     */ 
/* 141 */       if (globalServiceMap.get(serviceName) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 146 */       SoapCustomServiceData serviceData = new SoapCustomServiceData();
/* 147 */       serviceData.init(this, serviceName, idcServiceName);
/*     */ 
/* 149 */       this.m_serviceList.addElement(serviceData);
/* 150 */       this.m_serviceMap.put(serviceName, serviceData);
/* 151 */       globalServiceMap.put(serviceName, serviceData);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void generateWsdl(String wsdlDir)
/*     */     throws IOException
/*     */   {
/* 158 */     String filePath = wsdlDir + this.m_name + ".wsdl";
/* 159 */     File file = new File(filePath);
/* 160 */     if (file.exists())
/*     */     {
/* 162 */       file.delete();
/*     */     }
/*     */ 
/* 166 */     m_simpleDataTypeList = new IdcVector();
/* 167 */     m_simpleDataTypeMap = new Hashtable();
/*     */ 
/* 169 */     FileWriter writer = null;
/*     */     try
/*     */     {
/* 172 */       writer = new FileWriter(filePath);
/*     */ 
/* 174 */       writer.write("<?xml version='1.0' encoding='utf-8' ?>\r\n");
/* 175 */       writeWsdlDefinitions(writer);
/* 176 */       writeWsdlSchema(writer);
/* 177 */       writeWsdlMessages(writer);
/* 178 */       writeWsdlPortTypes(writer);
/* 179 */       writeWsdlBinding(writer);
/* 180 */       writeWsdlService(writer);
/*     */ 
/* 182 */       writer.write("</definitions>\r\n");
/*     */     }
/*     */     finally
/*     */     {
/* 187 */       if (writer != null)
/*     */       {
/* 189 */         writer.close();
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void writeWsdlDefinitions(Writer writer) throws IOException
/*     */   {
/* 196 */     writer.write("<definitions xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\"\r\n\txmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\"\r\n\txmlns:s=\"http://www.w3.org/2001/XMLSchema\"\r\n\txmlns:s0=\"" + this.m_namespace + "\"\r\n" + "\ttargetNamespace=\"" + this.m_namespace + "\"\r\n" + "\txmlns=\"" + "http://schemas.xmlsoap.org/wsdl/" + "\">\r\n");
/*     */   }
/*     */ 
/*     */   protected void writeWsdlSchema(Writer writer)
/*     */     throws IOException
/*     */   {
/* 207 */     writer.write("\t<types>\r\n");
/* 208 */     writer.write("\t\t<s:schema elementFormDefault=\"qualified\" targetNamespace=\"" + this.m_namespace + "\">\r\n");
/*     */ 
/* 211 */     int numServices = this.m_serviceList.size();
/* 212 */     for (int i = 0; i < numServices; ++i)
/*     */     {
/* 214 */       SoapCustomServiceData serviceData = (SoapCustomServiceData)this.m_serviceList.elementAt(i);
/*     */ 
/* 217 */       serviceData.writeSchemaElement(writer, 3);
/*     */     }
/*     */ 
/* 221 */     int numComplexData = this.m_complexTypeList.size();
/* 222 */     for (int i = 0; i < numComplexData; ++i)
/*     */     {
/* 224 */       SoapCustomComplexData soapData = (SoapCustomComplexData)this.m_complexTypeList.elementAt(i);
/*     */ 
/* 226 */       soapData.writeSchemaComplexType(writer, 3);
/*     */     }
/*     */ 
/* 230 */     int numElementData = m_simpleDataTypeList.size();
/* 231 */     for (int i = 0; i < numElementData; ++i)
/*     */     {
/* 233 */       SoapCustomElementData soapData = (SoapCustomElementData)m_simpleDataTypeList.elementAt(i);
/*     */ 
/* 235 */       soapData.writeSchemaComplexType(writer, 3);
/*     */     }
/*     */ 
/* 238 */     writer.write("\t\t</s:schema>\r\n");
/* 239 */     writer.write("\t</types>\r\n");
/*     */   }
/*     */ 
/*     */   protected void writeWsdlMessages(Writer writer) throws IOException
/*     */   {
/* 244 */     int numServices = this.m_serviceList.size();
/* 245 */     for (int i = 0; i < numServices; ++i)
/*     */     {
/* 247 */       SoapCustomServiceData serviceData = (SoapCustomServiceData)this.m_serviceList.elementAt(i);
/*     */ 
/* 250 */       String service = serviceData.m_name;
/* 251 */       String serviceResponse = service + "Response";
/* 252 */       String serviceSoapIn = service + "SoapIn";
/* 253 */       String serviceSoapOut = service + "SoapOut";
/*     */ 
/* 255 */       writer.write("\t<message name=\"" + serviceSoapIn + "\">\r\n");
/* 256 */       writer.write("\t\t<part name=\"parameters\" element=\"s0:" + service + "\" />\r\n");
/* 257 */       writer.write("\t</message>\r\n");
/*     */ 
/* 259 */       writer.write("\t<message name=\"" + serviceSoapOut + "\">\r\n");
/* 260 */       writer.write("\t\t<part name=\"parameters\" element=\"s0:" + serviceResponse + "\" />\r\n");
/* 261 */       writer.write("\t</message>\r\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void writeWsdlPortTypes(Writer writer) throws IOException
/*     */   {
/* 267 */     String wsdlSoap = this.m_name + "Soap";
/*     */ 
/* 269 */     writer.write("\t<portType name=\"" + wsdlSoap + "\">\r\n");
/*     */ 
/* 271 */     int numServices = this.m_serviceList.size();
/* 272 */     for (int i = 0; i < numServices; ++i)
/*     */     {
/* 274 */       SoapCustomServiceData serviceData = (SoapCustomServiceData)this.m_serviceList.elementAt(i);
/*     */ 
/* 277 */       String service = serviceData.m_name;
/* 278 */       String serviceSoapIn = "s0:" + service + "SoapIn";
/* 279 */       String serviceSoapOut = "s0:" + service + "SoapOut";
/*     */ 
/* 281 */       writer.write("\t\t<operation name=\"" + service + "\">\r\n");
/* 282 */       writer.write("\t\t\t<input message=\"" + serviceSoapIn + "\" />\r\n");
/* 283 */       writer.write("\t\t\t<output message=\"" + serviceSoapOut + "\" />\r\n");
/* 284 */       writer.write("\t\t</operation>\r\n");
/*     */     }
/*     */ 
/* 287 */     writer.write("\t</portType>\r\n");
/*     */   }
/*     */ 
/*     */   protected void writeWsdlBinding(Writer writer) throws IOException
/*     */   {
/* 292 */     String wsdlSoap = this.m_name + "Soap";
/*     */ 
/* 294 */     writer.write("\t<binding name=\"" + wsdlSoap + "\" type=\"s0:" + wsdlSoap + "\">\r\n");
/* 295 */     writer.write("\t\t<soap:binding transport=\"http://schemas.xmlsoap.org/soap/http\" style=\"document\" />\r\n");
/*     */ 
/* 298 */     int numServices = this.m_serviceList.size();
/* 299 */     for (int i = 0; i < numServices; ++i)
/*     */     {
/* 301 */       SoapCustomServiceData serviceData = (SoapCustomServiceData)this.m_serviceList.elementAt(i);
/*     */ 
/* 304 */       String service = serviceData.m_name;
/*     */ 
/* 306 */       writer.write("\t\t<operation name=\"" + service + "\">\r\n");
/* 307 */       writer.write("\t\t\t<soap:operation soapAction=\"" + this.m_namespace + "\" style=\"document\" />\r\n");
/*     */ 
/* 310 */       writer.write("\t\t\t<input>\r\n");
/* 311 */       writer.write("\t\t\t\t<soap:body use=\"literal\" />\r\n");
/* 312 */       writer.write("\t\t\t</input>\r\n");
/*     */ 
/* 314 */       writer.write("\t\t\t<output>\r\n");
/* 315 */       writer.write("\t\t\t\t<soap:body use=\"literal\" />\r\n");
/* 316 */       writer.write("\t\t\t</output>\r\n");
/*     */ 
/* 318 */       writer.write("\t\t</operation>\r\n");
/*     */     }
/*     */ 
/* 321 */     writer.write("\t</binding>\r\n");
/*     */   }
/*     */ 
/*     */   protected void writeWsdlService(Writer writer) throws IOException
/*     */   {
/* 326 */     IdcStringBuilder cgiPath = new IdcStringBuilder();
/* 327 */     DirectoryLocator.appendAbsoluteEnterpriseWebRoot(cgiPath);
/* 328 */     cgiPath.append("/_dav");
/* 329 */     cgiPath.append(DirectoryLocator.getRelativeCgiRoot());
/* 330 */     cgiPath.append(DirectoryLocator.getCgiFileName());
/*     */ 
/* 332 */     String wsdlSoap = this.m_name + "Soap";
/*     */ 
/* 334 */     writer.write("\t<service name=\"" + this.m_name + "\">\r\n");
/* 335 */     writer.write("\t\t<port name=\"" + wsdlSoap + "\" " + "binding=\"s0:" + wsdlSoap + "\">\r\n");
/*     */ 
/* 337 */     writer.write("\t\t\t<soap:address location=\"" + cgiPath.toString() + "\" />\r\n");
/*     */ 
/* 339 */     writer.write("\t\t</port>\r\n");
/* 340 */     writer.write("\t</service>\r\n");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 345 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89430 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.custom.SoapCustomWsdlData
 * JD-Core Version:    0.5.4
 */