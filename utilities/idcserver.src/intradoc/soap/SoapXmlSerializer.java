/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.DataStreamWrapperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.conversion.IdcEncodeInputStream;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.XmlDataMerger;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHttpImplementor;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.io.StringReader;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapXmlSerializer
/*     */ {
/*     */   public static final int FAULT_CLIENT_CODE = 0;
/*     */   public static final int FAULT_SERVER_CODE = 1;
/*  40 */   public static String NAMESPACE_11 = "http://schemas.xmlsoap.org/soap/envelope/";
/*  41 */   public static String NAMESPACE_12 = "http://www.w3.org/2003/05/soap-envelope";
/*     */ 
/*  43 */   static String m_serializerTableName = "SoapServiceSerializers";
/*  44 */   static SoapServiceSerializer m_defaultSerializer = null;
/*     */ 
/*  47 */   static Vector m_serializerList = null;
/*  48 */   static Hashtable m_serializerMap = null;
/*     */ 
/*  50 */   public static String m_xmlVersion = "1.0";
/*  51 */   public static boolean m_useCDATA = false;
/*  52 */   public static boolean m_omitIllegalChars = false;
/*  53 */   public static boolean m_replaceIllegalChars = false;
/*     */ 
/*     */   public static void init()
/*     */     throws DataException, ServiceException
/*     */   {
/*  58 */     DataResultSet drset = SharedObjects.getTable(m_serializerTableName);
/*  59 */     if (drset == null)
/*     */     {
/*  61 */       String errorMsg = LocaleUtils.encodeMessage("csSoapTableMissing", null, m_serializerTableName);
/*     */ 
/*  63 */       throw new DataException(errorMsg);
/*     */     }
/*  65 */     int nameIndex = ResultSetUtils.getIndexMustExist(drset, "name");
/*  66 */     int locationIndex = ResultSetUtils.getIndexMustExist(drset, "location");
/*     */ 
/*  68 */     String xmlVersion = SharedObjects.getEnvironmentValue("SoapXmlVersion");
/*  69 */     if (xmlVersion != null)
/*     */     {
/*  71 */       m_xmlVersion = xmlVersion;
/*     */     }
/*  73 */     m_useCDATA = SharedObjects.getEnvValueAsBoolean("SoapUseCdataForFieldData", false);
/*     */ 
/*  75 */     m_omitIllegalChars = SharedObjects.getEnvValueAsBoolean("SoapOmitIllegalXmlChars", false);
/*     */ 
/*  77 */     m_replaceIllegalChars = SharedObjects.getEnvValueAsBoolean("SoapReplaceIllegalXmlChars", true);
/*     */ 
/*  81 */     m_serializerList = new IdcVector();
/*  82 */     m_serializerMap = new Hashtable();
/*     */ 
/*  84 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/*  86 */       String name = drset.getStringValue(nameIndex);
/*  87 */       String location = drset.getStringValue(locationIndex);
/*     */ 
/*  89 */       if (m_serializerMap.get(name) != null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  94 */       SoapServiceSerializer serializer = (SoapServiceSerializer)ComponentClassFactory.createClassInstance(location, location, "!csSoapSerializerCreateError");
/*     */ 
/*  97 */       serializer.init();
/*     */ 
/*  99 */       if (name.equals("generic"))
/*     */       {
/* 101 */         m_defaultSerializer = serializer;
/*     */       }
/*     */ 
/* 104 */       String className = serializer.getClass().getName();
/*     */ 
/* 106 */       m_serializerList.addElement(serializer);
/* 107 */       m_serializerMap.put(className, serializer);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void parseRequest(DataBinder data)
/*     */     throws IOException, DataException
/*     */   {
/* 114 */     long contentLength = NumberUtils.parseLong(data.getEnvironmentValue("CONTENT_LENGTH"), 0L);
/*     */ 
/* 116 */     parseRequestEx(data, data.m_inStream, contentLength);
/*     */   }
/*     */ 
/*     */   public static void parseRequestEx(DataBinder data, BufferedInputStream bis, long contentLength)
/*     */     throws IOException, DataException
/*     */   {
/* 123 */     setClientEncoding(data, bis, contentLength);
/*     */ 
/* 126 */     SoapXmlFileParser parser = new SoapXmlFileParser();
/* 127 */     String soapMessage = parser.removeFileContent(data, bis, contentLength);
/* 128 */     StringReader reader = new StringReader(soapMessage);
/*     */ 
/* 131 */     XmlDataMerger dataMerger = new XmlDataMerger();
/* 132 */     dataMerger.parse(reader, null);
/* 133 */     List nodeList = dataMerger.getNodes();
/*     */ 
/* 136 */     PropertiesTreeNode serviceNode = parseSoapNodes(data, nodeList);
/*     */ 
/* 139 */     parseServiceNode(data, serviceNode);
/*     */ 
/* 142 */     String primaryFileName = data.getLocal("primaryFile");
/* 143 */     String alternateFileName = data.getLocal("alternateFile");
/*     */ 
/* 145 */     if ((primaryFileName == null) || (primaryFileName.length() <= 0) || (alternateFileName == null) || (alternateFileName.length() <= 0)) {
/*     */       return;
/*     */     }
/* 148 */     String primaryExtension = FileUtils.getExtension(primaryFileName);
/* 149 */     String alternateExtension = FileUtils.getExtension(alternateFileName);
/*     */ 
/* 151 */     if (!primaryExtension.equalsIgnoreCase(alternateExtension))
/*     */       return;
/* 153 */     data.m_environment.put("IsFileExtensionsEqualOverride", "1");
/*     */   }
/*     */ 
/*     */   public static void setClientEncoding(DataBinder data, BufferedInputStream bis, long contentLength)
/*     */     throws IOException, DataException
/*     */   {
/* 161 */     String clientEncoding = null;
/*     */ 
/* 164 */     clientEncoding = data.getLocal("ClientEncoding");
/*     */ 
/* 167 */     if (clientEncoding == null)
/*     */     {
/* 169 */       clientEncoding = data.getEnvironmentValue("ClientEncoding");
/*     */     }
/*     */ 
/* 173 */     if (clientEncoding == null)
/*     */     {
/* 175 */       String contentType = data.getEnvironmentValue("CONTENT_TYPE");
/* 176 */       int firstIndex = contentType.indexOf("charset=");
/* 177 */       if (firstIndex >= 0)
/*     */       {
/* 179 */         String isoEncoding = null;
/*     */ 
/* 181 */         int lastIndex = contentType.indexOf(";", firstIndex);
/* 182 */         if (lastIndex > 0)
/*     */         {
/* 184 */           isoEncoding = contentType.substring(firstIndex + 8, lastIndex);
/*     */         }
/*     */         else
/*     */         {
/* 188 */           isoEncoding = contentType.substring(firstIndex + 8);
/*     */         }
/*     */ 
/* 191 */         isoEncoding = isoEncoding.toLowerCase().trim();
/* 192 */         clientEncoding = DataSerializeUtils.getJavaEncoding(isoEncoding);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 197 */     if (clientEncoding == null)
/*     */     {
/* 199 */       String isoEncoding = parseXmlNode(data, bis, contentLength);
/* 200 */       if (isoEncoding != null)
/*     */       {
/* 202 */         clientEncoding = DataSerializeUtils.getJavaEncoding(isoEncoding);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 207 */     if (clientEncoding == null)
/*     */     {
/* 209 */       clientEncoding = DataSerializeUtils.getSystemEncoding();
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 215 */       String str = "Test string";
/* 216 */       StringUtils.getBytes(str, clientEncoding);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 221 */       data.m_determinedEncoding = true;
/* 222 */       data.m_clientEncoding = DataSerializeUtils.getSystemEncoding();
/* 223 */       data.putLocal("ClientEncoding", data.m_clientEncoding);
/*     */ 
/* 225 */       throw new DataException("The encoding '" + clientEncoding + "' is invalid.");
/*     */     }
/*     */ 
/* 229 */     data.m_determinedEncoding = true;
/* 230 */     data.m_clientEncoding = clientEncoding;
/* 231 */     data.putLocal("ClientEncoding", clientEncoding);
/*     */   }
/*     */ 
/*     */   public static String parseXmlNode(DataBinder data, BufferedInputStream bis, long length)
/*     */     throws IOException
/*     */   {
/* 238 */     int numBytes = 200;
/* 239 */     if (length < numBytes)
/*     */     {
/* 241 */       numBytes = (int)length;
/*     */     }
/* 243 */     byte[] b = SoapUtils.readStream(bis, numBytes, true);
/* 244 */     String str = new String(b, 0, b.length);
/*     */ 
/* 247 */     int startQuestionIndex = str.indexOf("<?");
/* 248 */     int endQuestionIndex = str.indexOf("?>");
/* 249 */     if ((startQuestionIndex < 0) || (endQuestionIndex < 0) || (startQuestionIndex > endQuestionIndex))
/*     */     {
/* 252 */       return null;
/*     */     }
/*     */ 
/* 256 */     str = str.substring(startQuestionIndex + 2, endQuestionIndex);
/* 257 */     if (str.indexOf("xml") < 0)
/*     */     {
/* 259 */       return null;
/*     */     }
/*     */ 
/* 263 */     int encodingIndex = str.indexOf("encoding=");
/* 264 */     if (encodingIndex < 0)
/*     */     {
/* 266 */       return null;
/*     */     }
/*     */ 
/* 269 */     String encoding = null;
/* 270 */     int spaceIndex = str.indexOf(" ", encodingIndex);
/* 271 */     if (spaceIndex < 0)
/*     */     {
/* 273 */       encoding = str.substring(encodingIndex + 9);
/*     */     }
/*     */     else
/*     */     {
/* 277 */       encoding = str.substring(encodingIndex + 9, spaceIndex);
/*     */     }
/*     */ 
/* 281 */     encoding = SoapUtils.stripEdgeChars(encoding, '\'', '\'');
/* 282 */     encoding = SoapUtils.stripEdgeChars(encoding, '"', '"');
/*     */ 
/* 284 */     return encoding.toLowerCase().trim();
/*     */   }
/*     */ 
/*     */   public static PropertiesTreeNode parseSoapNodes(DataBinder data, List nodeList)
/*     */     throws DataException
/*     */   {
/* 291 */     PropertiesTreeNode node = SoapUtils.getFirstSubNode(nodeList);
/* 292 */     String nodeName = SoapUtils.getNodeName(node);
/* 293 */     if ((nodeName == null) || (!nodeName.equalsIgnoreCase("Envelope")))
/*     */     {
/* 295 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMissingRequiredNode", null, "Envelope");
/*     */ 
/* 297 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 301 */     String version = null;
/* 302 */     String namespace = SoapUtils.getNodeNamespace(node);
/* 303 */     if (namespace.equals(NAMESPACE_12))
/*     */     {
/* 305 */       version = "1.2";
/*     */     }
/*     */     else
/*     */     {
/* 309 */       version = "1.1";
/* 310 */       namespace = NAMESPACE_11;
/*     */     }
/* 312 */     data.setEnvironmentValue("SOAP:Version", version);
/* 313 */     data.setEnvironmentValue("SOAP:Namespace", namespace);
/*     */ 
/* 316 */     boolean isBodyFound = false;
/* 317 */     Vector subNodes = node.m_subNodes;
/* 318 */     int numSubNodes = subNodes.size();
/* 319 */     for (int i = 0; i < numSubNodes; ++i)
/*     */     {
/* 321 */       node = (PropertiesTreeNode)subNodes.elementAt(i);
/* 322 */       nodeName = SoapUtils.getNodeName(node);
/* 323 */       if (!nodeName.equalsIgnoreCase("Body"))
/*     */         continue;
/* 325 */       isBodyFound = true;
/* 326 */       break;
/*     */     }
/*     */ 
/* 329 */     if (!isBodyFound)
/*     */     {
/* 331 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMissingRequiredNode", null, "Body");
/*     */ 
/* 333 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 337 */     PropertiesTreeNode serviceNode = SoapUtils.getFirstSubNode(node.m_subNodes);
/* 338 */     return serviceNode;
/*     */   }
/*     */ 
/*     */   public static void parseServiceNode(DataBinder data, PropertiesTreeNode node)
/*     */     throws IOException, DataException
/*     */   {
/* 345 */     if (node == null)
/*     */     {
/* 347 */       String errorMsg = LocaleUtils.encodeMessage("csSoapInvalidServiceNode", null, "(null)");
/*     */ 
/* 349 */       throw new DataException(errorMsg);
/*     */     }
/* 351 */     String serviceName = SoapUtils.getNodeName(node);
/*     */ 
/* 354 */     boolean isParsed = false;
/*     */ 
/* 356 */     int numSerializers = m_serializerList.size();
/* 357 */     for (int i = 0; i < numSerializers; ++i)
/*     */     {
/* 359 */       SoapServiceSerializer serializer = (SoapServiceSerializer)m_serializerList.elementAt(i);
/*     */ 
/* 361 */       if (!serializer.canParseRequest(data, serviceName))
/*     */         continue;
/* 363 */       String className = serializer.getClass().getName();
/* 364 */       data.setEnvironmentValue("SOAP:ServiceSerializer", className);
/* 365 */       serializer.parseRequest(data, node, serviceName);
/* 366 */       isParsed = true;
/*     */ 
/* 368 */       break;
/*     */     }
/*     */ 
/* 372 */     if (isParsed)
/*     */       return;
/* 374 */     String errorMsg = LocaleUtils.encodeMessage("csSoapInvalidServiceNode", null, serviceName);
/*     */ 
/* 376 */     throw new DataException(errorMsg);
/*     */   }
/*     */ 
/*     */   public static String sendResponse(DataBinder data, ExecutionContext cxt)
/*     */     throws IOException
/*     */   {
/* 384 */     String contentType = getResponseContentType(data);
/* 385 */     data.setContentType(contentType);
/*     */ 
/* 387 */     StringBuffer buffer = new StringBuffer();
/*     */ 
/* 390 */     String isoEncoding = DataSerializeUtils.getIsoEncoding(data.m_clientEncoding);
/* 391 */     if (isoEncoding == null)
/*     */     {
/* 393 */       isoEncoding = data.m_clientEncoding;
/*     */     }
/* 395 */     buffer.append("<?xml version='" + m_xmlVersion + "' encoding='" + isoEncoding + "' ?>\r\n");
/*     */ 
/* 399 */     String namespace = data.getEnvironmentValue("SOAP:Namespace");
/* 400 */     if (namespace == null)
/*     */     {
/* 402 */       namespace = NAMESPACE_11;
/*     */     }
/* 404 */     buffer.append("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"" + namespace + "\">\r\n");
/* 405 */     buffer.append("<SOAP-ENV:Body>\r\n");
/*     */ 
/* 408 */     boolean isFault = StringUtils.convertToBool(data.getEnvironmentValue("SOAP:IsFault"), false);
/*     */ 
/* 410 */     if (isFault)
/*     */     {
/* 412 */       sendFaultResponse(data, buffer, cxt);
/*     */     }
/*     */     else
/*     */     {
/* 417 */       SoapServiceSerializer serializer = null;
/* 418 */       String serializerName = data.getEnvironmentValue("SOAP:ServiceSerializer");
/* 419 */       if (serializerName != null)
/*     */       {
/* 421 */         serializer = (SoapServiceSerializer)m_serializerMap.get(serializerName);
/*     */       }
/* 423 */       if (serializer == null)
/*     */       {
/* 425 */         serializer = m_defaultSerializer;
/*     */       }
/* 427 */       serializer.sendResponse(data, buffer);
/*     */     }
/*     */ 
/* 431 */     buffer.append("</SOAP-ENV:Body>\r\n");
/* 432 */     buffer.append("</SOAP-ENV:Envelope>");
/*     */ 
/* 434 */     return buffer.toString();
/*     */   }
/*     */ 
/*     */   public static void sendStreamResponse(DataBinder data, Service service, DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor)
/*     */     throws IOException
/*     */   {
/* 442 */     String soapResponseStr = sendResponse(data, service);
/*     */ 
/* 445 */     byte[] startSoapResponseBytes = null;
/* 446 */     byte[] endSoapResponseBytes = null;
/*     */ 
/* 448 */     int startFileContentIndex = NumberUtils.parseInteger(data.getEnvironmentValue("SOAP:startFileContentIndex"), -1);
/*     */ 
/* 450 */     if (startFileContentIndex >= 0)
/*     */     {
/* 452 */       String startResponseStr = soapResponseStr.substring(0, startFileContentIndex);
/* 453 */       startSoapResponseBytes = StringUtils.getBytes(startResponseStr, data.m_clientEncoding);
/*     */ 
/* 456 */       String endResponseStr = soapResponseStr.substring(startFileContentIndex);
/* 457 */       endSoapResponseBytes = StringUtils.getBytes(endResponseStr, data.m_clientEncoding);
/*     */     }
/*     */     else
/*     */     {
/* 462 */       startSoapResponseBytes = StringUtils.getBytes(soapResponseStr, data.m_clientEncoding);
/*     */ 
/* 464 */       endSoapResponseBytes = new byte[0];
/*     */     }
/*     */ 
/* 468 */     IdcEncodeInputStream encodingStream = new IdcEncodeInputStream(streamWrapper.m_inStream, streamWrapper.m_streamLength, "uuencode");
/*     */ 
/* 470 */     long encodedFileLength = encodingStream.getPredictedEncodedLength();
/* 471 */     DataStreamWrapper encodingStreamWrapper = streamWrapper.shallowClone();
/* 472 */     encodingStreamWrapper.initWithInputStream(encodingStream, encodedFileLength);
/*     */     try
/*     */     {
/* 477 */       long contentLength = startSoapResponseBytes.length + endSoapResponseBytes.length + encodedFileLength;
/*     */ 
/* 479 */       String contentType = getResponseContentType(data);
/* 480 */       data.m_contentType = (contentType + "\r\nContent-Length: " + contentLength);
/* 481 */       String httpHeader = httpImplementor.createHttpResponseHeader();
/*     */ 
/* 484 */       OutputStream os = service.getOutput();
/*     */ 
/* 486 */       os.write(httpHeader.getBytes());
/* 487 */       os.write(startSoapResponseBytes);
/* 488 */       DataStreamWrapperUtils.copyInStreamToOutputStream(encodingStreamWrapper, os);
/*     */ 
/* 490 */       os.write(endSoapResponseBytes);
/*     */     }
/*     */     finally
/*     */     {
/* 496 */       encodingStreamWrapper.release();
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void setFaultMessage(DataBinder data, int code, String message)
/*     */   {
/* 502 */     data.setEnvironmentValue("SOAP:IsFault", "1");
/* 503 */     data.setEnvironmentValue("SOAP:FaultCode", "" + code);
/* 504 */     data.setEnvironmentValue("SOAP:FaultMessage", message);
/* 505 */     data.setEnvironmentValue("IdcService", "SOAP_FAULT");
/*     */   }
/*     */ 
/*     */   public static void sendFaultResponse(DataBinder data, StringBuffer buffer, ExecutionContext cxt)
/*     */   {
/* 512 */     int faultCodeInt = NumberUtils.parseInteger(data.getEnvironmentValue("SOAP:FaultCode"), 1);
/*     */ 
/* 515 */     String faultMessage = data.getEnvironmentValue("SOAP:FaultMessage");
/* 516 */     if (faultMessage == null)
/*     */     {
/* 518 */       faultMessage = LocaleResources.getString("csSoapRequestError", null);
/*     */     }
/* 520 */     faultMessage = LocaleResources.localizeMessage(faultMessage, cxt);
/*     */ 
/* 522 */     String version = data.getEnvironmentValue("SOAP:Version");
/* 523 */     if (version == null)
/*     */     {
/* 525 */       version = "1.1";
/*     */     }
/*     */ 
/* 528 */     data.putLocal("IdcService", "SOAP_FAULT");
/*     */ 
/* 530 */     if (version.equals("1.1"))
/*     */     {
/* 532 */       String faultCode = null;
/* 533 */       if (faultCodeInt == 0)
/*     */       {
/* 535 */         faultCode = "Client";
/*     */       }
/*     */       else
/*     */       {
/* 539 */         faultCode = "Server";
/*     */       }
/*     */ 
/* 542 */       buffer.append("<SOAP-ENV:Fault>\r\n");
/* 543 */       buffer.append("<faultcode>" + faultCode + "</faultcode>\r\n");
/* 544 */       buffer.append("<faultstring>" + faultMessage + "</faultstring>\r\n");
/* 545 */       buffer.append("</SOAP-ENV:Fault>\r\n");
/*     */     } else {
/* 547 */       if (!version.equals("1.2"))
/*     */         return;
/* 549 */       String faultCode = null;
/* 550 */       if (faultCodeInt == 0)
/*     */       {
/* 552 */         faultCode = "Sender";
/*     */       }
/*     */       else
/*     */       {
/* 556 */         faultCode = "Receiver";
/*     */       }
/*     */ 
/* 559 */       buffer.append("<SOAP-ENV:Fault>\r\n");
/*     */ 
/* 561 */       buffer.append("<SOAP-ENV:Code>\r\n");
/* 562 */       buffer.append("<SOAP-ENV:Value>" + faultCode + "</SOAP-ENV:Value>\r\n");
/* 563 */       buffer.append("</SOAP-ENV:Code>\r\n");
/*     */ 
/* 565 */       buffer.append("<SOAP-ENV:Reason>\r\n");
/* 566 */       buffer.append("<SOAP-ENV:Text>" + faultMessage + "</SOAP-ENV:Text>\r\n");
/* 567 */       buffer.append("</SOAP-ENV:Reason>\r\n");
/*     */ 
/* 569 */       buffer.append("</SOAP-ENV:Fault>\r\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static String getResponseContentType(DataBinder data)
/*     */   {
/* 576 */     boolean isXmlRequest = false;
/* 577 */     String contentType = data.getEnvironmentValue("CONTENT_TYPE");
/* 578 */     if (contentType == null)
/*     */     {
/* 580 */       contentType = "text/xml";
/*     */     }
/* 582 */     contentType = contentType.toLowerCase().trim();
/* 583 */     int numContentTypes = SoapSerializer.XML_CONTENT_TYPES.length;
/* 584 */     for (int i = 0; i < numContentTypes; ++i)
/*     */     {
/* 586 */       String xmlContentType = SoapSerializer.XML_CONTENT_TYPES[i];
/* 587 */       if (!contentType.startsWith(xmlContentType))
/*     */         continue;
/* 589 */       isXmlRequest = true;
/* 590 */       break;
/*     */     }
/*     */ 
/* 595 */     if (!isXmlRequest)
/*     */     {
/* 597 */       contentType = "text/xml";
/*     */     }
/*     */ 
/* 601 */     if (data.m_clientEncoding == null)
/*     */     {
/* 603 */       data.m_determinedEncoding = true;
/* 604 */       data.m_clientEncoding = DataSerializeUtils.getSystemEncoding();
/* 605 */       data.putLocal("ClientEncoding", data.m_clientEncoding);
/*     */     }
/*     */ 
/* 609 */     if (contentType.startsWith("text/xml"))
/*     */     {
/* 611 */       String isoEncoding = DataSerializeUtils.getIsoEncoding(data.m_clientEncoding);
/* 612 */       contentType = "text/xml; charset=" + isoEncoding;
/*     */     }
/*     */ 
/* 615 */     return contentType;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 620 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87892 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapXmlSerializer
 * JD-Core Version:    0.5.4
 */