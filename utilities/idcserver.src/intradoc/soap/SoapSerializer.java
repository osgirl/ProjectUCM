/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.DataStreamWrapperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHttpImplementor;
/*     */ import java.io.IOException;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapSerializer
/*     */ {
/*     */   public static final String XML_SERIALIZER_NAME = "xml";
/*     */   public static final String MULTIPART_SERIALIZER_NAME = "multipart";
/*     */   public static final String DIME_SERIALIZER_NAME = "dime";
/*  37 */   public static Properties m_contentTypeToNameMap = null;
/*  38 */   public static Properties m_serializerNameProps = null;
/*     */ 
/*  41 */   public static final String[] XML_CONTENT_TYPES = { "text/xml", "application/xml", "soap/application+xml" };
/*     */ 
/*  45 */   public static String MULTIPART_CONTENT_TYPE = "multipart/related";
/*  46 */   public static String DIME_CONTENT_TYPE = "application/dime";
/*     */ 
/*  48 */   public static boolean m_isValid = false;
/*     */ 
/*     */   public static void init(Workspace ws, DataBinder data, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  53 */     m_isValid = false;
/*     */ 
/*  55 */     SoapXmlSerializer.init();
/*     */ 
/*  58 */     m_contentTypeToNameMap = new Properties();
/*     */ 
/*  60 */     int numXmlTypes = XML_CONTENT_TYPES.length;
/*  61 */     for (int i = 0; i < numXmlTypes; ++i)
/*     */     {
/*  63 */       m_contentTypeToNameMap.put(XML_CONTENT_TYPES[i], "xml");
/*     */     }
/*  65 */     m_contentTypeToNameMap.put(MULTIPART_CONTENT_TYPE, "multipart");
/*  66 */     m_contentTypeToNameMap.put(DIME_CONTENT_TYPE, "dime");
/*     */ 
/*  68 */     m_serializerNameProps = new Properties();
/*  69 */     m_serializerNameProps.put("xml", "1");
/*  70 */     m_serializerNameProps.put("multipart", "1");
/*  71 */     m_serializerNameProps.put("dime", "1");
/*     */ 
/*  73 */     m_isValid = true;
/*     */   }
/*     */ 
/*     */   public static boolean parseRequest(DataBinder data)
/*     */   {
/*  79 */     String contentType = data.getEnvironmentValue("CONTENT_TYPE");
/*  80 */     if (contentType == null)
/*     */     {
/*  82 */       return false;
/*     */     }
/*     */ 
/*  85 */     int index = contentType.indexOf(";");
/*  86 */     if (index > 0)
/*     */     {
/*  88 */       contentType = contentType.substring(0, index);
/*     */     }
/*  90 */     contentType = contentType.toLowerCase().trim();
/*     */ 
/*  93 */     String serializerName = m_contentTypeToNameMap.getProperty(contentType);
/*  94 */     if (serializerName == null)
/*     */     {
/*  96 */       return false;
/*     */     }
/*     */ 
/* 100 */     setRequestValues(data);
/*     */ 
/* 103 */     String queryString = data.getEnvironmentValue("QUERY_STRING");
/* 104 */     if (queryString != null)
/*     */     {
/* 106 */       DataSerializeUtils.parseLocalParameters(data, queryString, "&", null);
/* 107 */       data.m_rawData.clear();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 115 */       translateEnvironment(data);
/*     */ 
/* 118 */       if (serializerName.equals("xml"))
/*     */       {
/* 120 */         SoapXmlSerializer.parseRequest(data);
/*     */       }
/* 122 */       else if (serializerName.equals("multipart"))
/*     */       {
/* 124 */         SoapMultipartSerializer.parseRequest(data);
/*     */       }
/* 126 */       else if (serializerName.equals("dime"))
/*     */       {
/* 128 */         SoapDimeSerializer.parseRequest(data);
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 133 */       SoapXmlSerializer.setFaultMessage(data, 0, e.getMessage());
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 138 */       SoapXmlSerializer.setFaultMessage(data, 1, e.getMessage());
/*     */     }
/*     */ 
/* 142 */     return true;
/*     */   }
/*     */ 
/*     */   public static void translateEnvironment(DataBinder data) throws IOException
/*     */   {
/* 147 */     DataSerializeUtils.translateEnvironment(data);
/*     */   }
/*     */ 
/*     */   public static void postParseRequest(DataBinder data)
/*     */   {
/* 152 */     boolean isSoapRequest = StringUtils.convertToBool(data.getEnvironmentValue("SOAP:IsSoapRequest"), false);
/*     */ 
/* 156 */     String isSoapStr = data.getLocal("IsSoap");
/* 157 */     if (isSoapStr != null)
/*     */     {
/* 159 */       boolean isSoapFlag = StringUtils.convertToBool(isSoapStr, false);
/*     */ 
/* 161 */       if ((isSoapFlag & !isSoapRequest))
/*     */       {
/* 164 */         setRequestValues(data);
/* 165 */         isSoapRequest = true;
/*     */       }
/* 167 */       else if ((!isSoapFlag) && (isSoapRequest))
/*     */       {
/* 170 */         data.setEnvironmentValue("SOAP:IsSoapRequest", "0");
/* 171 */         isSoapRequest = false;
/*     */ 
/* 173 */         boolean isJava = StringUtils.convertToBool(data.getLocal("IsJava"), false);
/* 174 */         if (!isJava)
/*     */         {
/* 176 */           data.m_isJava = false;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 182 */     if (isSoapRequest)
/*     */     {
/* 184 */       String serializerName = "xml";
/* 185 */       String soapResponseType = data.getLocal("soapResponseType");
/* 186 */       if (soapResponseType != null)
/*     */       {
/* 188 */         data.m_environment.put("SOAP:IsResponseTypeSpecified", "1");
/*     */ 
/* 190 */         soapResponseType = soapResponseType.toLowerCase().trim();
/* 191 */         serializerName = m_contentTypeToNameMap.getProperty(soapResponseType);
/* 192 */         if (serializerName == null)
/*     */         {
/* 194 */           serializerName = soapResponseType;
/*     */         }
/*     */ 
/* 198 */         if (m_serializerNameProps.getProperty(serializerName) == null)
/*     */         {
/* 200 */           serializerName = "xml";
/*     */         }
/*     */       }
/* 203 */       data.setEnvironmentValue("SOAP:Serializer", serializerName);
/*     */     }
/*     */ 
/* 207 */     boolean isNoHttpHeaders = StringUtils.convertToBool(data.getEnvironmentValue("NoHttpHeaders"), false);
/*     */ 
/* 209 */     if (!isNoHttpHeaders)
/*     */       return;
/* 211 */     data.putLocal("NoHttpHeaders", "1");
/*     */   }
/*     */ 
/*     */   public static byte[] sendResponse(DataBinder data, ExecutionContext cxt, String encoding)
/*     */     throws IOException
/*     */   {
/* 219 */     boolean isSoapRequest = StringUtils.convertToBool(data.getEnvironmentValue("SOAP:IsSoapRequest"), false);
/*     */ 
/* 221 */     if (!isSoapRequest)
/*     */     {
/* 223 */       return null;
/*     */     }
/*     */ 
/* 227 */     byte[] responseBytes = null;
/* 228 */     String serializerName = data.getEnvironmentValue("SOAP:Serializer");
/* 229 */     if (serializerName == null)
/*     */     {
/* 231 */       serializerName = "xml";
/*     */     }
/*     */ 
/* 234 */     if (serializerName.equals("xml"))
/*     */     {
/* 236 */       String response = SoapXmlSerializer.sendResponse(data, cxt);
/* 237 */       responseBytes = StringUtils.getBytes(response, encoding);
/*     */     }
/* 239 */     else if (serializerName.equals("multipart"))
/*     */     {
/* 241 */       responseBytes = SoapMultipartSerializer.sendResponse(data, cxt, encoding);
/*     */     }
/* 243 */     else if (serializerName.equals("dime"))
/*     */     {
/* 245 */       responseBytes = SoapDimeSerializer.sendResponse(data, cxt, encoding);
/*     */     }
/*     */ 
/* 248 */     return responseBytes;
/*     */   }
/*     */ 
/*     */   public static boolean sendStreamResponse(DataBinder data, Service service, DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor)
/*     */     throws IOException
/*     */   {
/* 256 */     boolean isSoapRequest = StringUtils.convertToBool(data.getEnvironmentValue("SOAP:IsSoapRequest"), false);
/*     */ 
/* 258 */     if (!isSoapRequest)
/*     */     {
/* 260 */       return false;
/*     */     }
/*     */ 
/* 263 */     if ((streamWrapper.m_isSimpleFileStream) && (!streamWrapper.m_inStreamActive))
/*     */     {
/* 265 */       DataStreamWrapperUtils.openFileStream(streamWrapper);
/*     */     }
/*     */ 
/* 268 */     data.setEnvironmentValue("SOAP:downloadName", streamWrapper.m_clientFileName);
/*     */ 
/* 271 */     String serializerName = null;
/*     */ 
/* 274 */     boolean isResponsetTypeSpecified = StringUtils.convertToBool(data.m_environment.getProperty("SOAP:IsResponseTypeSpecified"), false);
/*     */ 
/* 276 */     if (!isResponsetTypeSpecified)
/*     */     {
/* 278 */       serializerName = data.getEnvironmentValue("SOAP:DefaultDownloadType");
/*     */     }
/* 280 */     if (serializerName == null)
/*     */     {
/* 282 */       serializerName = data.getEnvironmentValue("SOAP:Serializer");
/*     */     }
/*     */ 
/* 285 */     if (serializerName == null)
/*     */     {
/* 287 */       serializerName = "xml";
/*     */     }
/* 289 */     if (serializerName.equals("xml"))
/*     */     {
/* 291 */       SoapXmlSerializer.sendStreamResponse(data, service, streamWrapper, httpImplementor);
/*     */     }
/* 293 */     else if (serializerName.equals("multipart"))
/*     */     {
/* 295 */       SoapMultipartSerializer.sendStreamResponse(data, service, streamWrapper, httpImplementor);
/*     */     }
/* 297 */     else if (serializerName.equals("dime"))
/*     */     {
/* 299 */       SoapDimeSerializer.sendStreamResponse(data, service, streamWrapper, httpImplementor);
/*     */     }
/*     */ 
/* 302 */     return true;
/*     */   }
/*     */ 
/*     */   public static void setRequestValues(DataBinder data)
/*     */   {
/* 308 */     data.setEnvironmentValue("SOAP:IsSoapRequest", "1");
/* 309 */     data.setEnvironmentValue("SOAP:Serializer", "xml");
/* 310 */     data.m_isJava = true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 315 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapSerializer
 * JD-Core Version:    0.5.4
 */