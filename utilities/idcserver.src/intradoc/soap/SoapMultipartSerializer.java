/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.DataStreamWrapperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.PropertiesTreeNode;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.serialize.DataBinderSerializer;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHttpImplementor;
/*     */ import java.io.BufferedInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Properties;
/*     */ import java.util.Random;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SoapMultipartSerializer
/*     */ {
/*     */   public static final String MTOM_CONTENT_TYPE = "application/xop+xml";
/*     */ 
/*     */   public static void parseRequest(DataBinder data)
/*     */     throws IOException, DataException
/*     */   {
/*  37 */     Properties props = getContentTypeProps(data);
/*  38 */     String boundary = props.getProperty("boundary");
/*  39 */     String startID = props.getProperty("start");
/*     */ 
/*  41 */     boolean isFirstAttachment = true;
/*  42 */     boolean isLastAttachment = false;
/*     */ 
/*  44 */     while ((!data.m_isSuspended) && (!isLastAttachment))
/*     */     {
/*  46 */       String contentID = parseAttachmentHeader(data, boundary);
/*  47 */       if (contentID == null)
/*     */       {
/*     */         return;
/*     */       }
/*     */ 
/*  53 */       if ((isFirstAttachment) && (!startID.equals(contentID)))
/*     */       {
/*  55 */         String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartSoapContentFirst", null);
/*  56 */         throw new DataException(errorMsg);
/*     */       }
/*     */ 
/*  59 */       DataBinderSerializer serializer = (DataBinderSerializer)DataSerializeUtils.getDataSerialize();
/*     */ 
/*  61 */       if (isFirstAttachment)
/*     */       {
/*  63 */         serializer.readFile(data, "soap.xml", "soapFile");
/*     */ 
/*  65 */         long fileLength = 0L;
/*  66 */         String soapFilePath = data.getLocal("soapFile:path");
/*  67 */         File file = new File(soapFilePath);
/*  68 */         if (file.exists())
/*     */         {
/*  70 */           fileLength = file.length();
/*     */         }
/*     */ 
/*  73 */         BufferedInputStream bis = null;
/*  74 */         FileInputStream fis = null;
/*     */         try
/*     */         {
/*  77 */           fis = new FileInputStream(soapFilePath);
/*  78 */           bis = new BufferedInputStream(fis);
/*  79 */           SoapXmlSerializer.parseRequestEx(data, bis, fileLength);
/*     */         }
/*     */         finally
/*     */         {
/*  83 */           if (bis != null)
/*     */           {
/*  85 */             bis.close();
/*     */           }
/*     */ 
/*  88 */           if (fis != null)
/*     */           {
/*  90 */             fis.close();
/*     */           }
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/*  96 */         String filePath = data.getLocal(contentID);
/*  97 */         if (filePath == null)
/*     */         {
/*  99 */           filePath = "temp.txt";
/*     */         }
/*     */ 
/* 103 */         String fileKey = data.getLocal(contentID + ":fileKey");
/* 104 */         if (fileKey != null)
/*     */         {
/* 106 */           contentID = fileKey;
/*     */         }
/*     */ 
/* 109 */         serializer.readFile(data, filePath, contentID);
/*     */       }
/*     */ 
/* 115 */       isFirstAttachment = false;
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static Properties getContentTypeProps(DataBinder data)
/*     */     throws IOException, DataException
/*     */   {
/* 122 */     String contentType = data.getEnvironmentValue("CONTENT_TYPE");
/* 123 */     Properties props = new Properties();
/*     */ 
/* 126 */     Vector nameValueList = StringUtils.parseArray(contentType, ';', '^');
/* 127 */     int numValues = nameValueList.size();
/* 128 */     for (int i = 0; i < numValues; ++i)
/*     */     {
/* 130 */       String nameValueStr = (String)nameValueList.elementAt(i);
/* 131 */       String[] nameValue = SoapUtils.getNameValuePair(nameValueStr, '=');
/* 132 */       props.put(nameValue[0].toLowerCase(), nameValue[1]);
/*     */     }
/*     */ 
/* 136 */     String boundary = props.getProperty("boundary");
/* 137 */     if (boundary == null)
/*     */     {
/* 140 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartInvalidContentTypeValue", null, "boundary", "null");
/*     */ 
/* 142 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 145 */     data.m_environment.put("BOUNDARY", boundary);
/*     */ 
/* 147 */     if (data.m_boundary == null)
/*     */     {
/* 149 */       data.m_boundary = ("--" + boundary);
/* 150 */       String encoding = DataSerializeUtils.determineEncoding(data, null);
/* 151 */       data.m_boundaryBytes = StringUtils.getBytes(data.m_boundary, encoding);
/* 152 */       if (data.m_boundary.length() != data.m_boundaryBytes.length)
/*     */       {
/* 154 */         throw new DataException("Boundary separator for upload post contains illegal characters.");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 159 */     String type = props.getProperty("type");
/*     */ 
/* 162 */     if ((type != null) && (type.indexOf("application/xop+xml") >= 0))
/*     */     {
/* 164 */       data.m_environment.put("SOAP:IsMtomRequest", "1");
/* 165 */       data.putLocal("soapResponseType", SoapSerializer.MULTIPART_CONTENT_TYPE);
/*     */     }
/* 167 */     else if ((type == null) || (type.indexOf("text/xml") < 0))
/*     */     {
/* 169 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartInvalidContentTypeValue", null, "type", type);
/*     */ 
/* 171 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 175 */     String start = props.getProperty("start");
/* 176 */     if (start == null)
/*     */     {
/* 178 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartInvalidContentTypeValue", null, "start", null);
/*     */ 
/* 180 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 183 */     return props;
/*     */   }
/*     */ 
/*     */   public static String parseAttachmentHeader(DataBinder data, String boundary)
/*     */     throws IOException, DataException
/*     */   {
/* 189 */     String startBoundary = "--" + boundary;
/* 190 */     String endBoundary = "--" + boundary + "--";
/*     */ 
/* 193 */     String line = readBlankLines(data);
/*     */ 
/* 195 */     if (line.startsWith("$$"))
/*     */     {
/* 197 */       return null;
/*     */     }
/*     */ 
/* 201 */     if (line.indexOf(startBoundary) < 0)
/*     */     {
/* 203 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartInvalidBoundary", null, startBoundary);
/*     */ 
/* 205 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 208 */     if (line.indexOf(endBoundary) >= 0)
/*     */     {
/* 211 */       int lineCount = 0;
/*     */       label133: 
/*     */       while (true)
/* 213 */         if (!line.endsWith("$$"))
/*     */         {
/* 215 */           line = readLine(data);
/*     */ 
/* 219 */           ++lineCount;
/* 220 */           if (lineCount <= 50)
/*     */           {
/*     */             break label133;
/*     */           }
/*     */ 
/* 226 */           return null;
/*     */         }
/*     */     }
/* 229 */     String contentID = null;
/*     */ 
/* 231 */     while (!line.equals(""))
/*     */     {
/* 233 */       line = readLine(data);
/* 234 */       if (line.endsWith("$$"))
/*     */       {
/* 236 */         return null;
/*     */       }
/*     */ 
/* 239 */       String[] nameValue = SoapUtils.getNameValuePair(line, ':');
/* 240 */       if (nameValue[0].equalsIgnoreCase("content-id"))
/*     */       {
/* 242 */         contentID = nameValue[1];
/*     */       }
/*     */     }
/*     */ 
/* 246 */     if (contentID == null)
/*     */     {
/* 248 */       String errorMsg = LocaleUtils.encodeMessage("csSoapMultipartMissingHeaderValue", null, contentID);
/*     */ 
/* 250 */       throw new DataException(errorMsg);
/*     */     }
/*     */ 
/* 253 */     return contentID;
/*     */   }
/*     */ 
/*     */   public static String readBlankLines(DataBinder data)
/*     */     throws IOException
/*     */   {
/* 259 */     String line = "";
/*     */ 
/* 261 */     while (line.equals(""))
/*     */     {
/* 263 */       line = readLine(data);
/*     */     }
/*     */ 
/* 266 */     return line;
/*     */   }
/*     */ 
/*     */   public static String readLine(DataBinder data) throws IOException
/*     */   {
/* 271 */     String line = DataSerializeUtils.readLineEx(data, data.m_inStream, true, true, null);
/*     */ 
/* 273 */     if (line == null)
/*     */     {
/* 275 */       line = "";
/*     */     }
/*     */ 
/* 278 */     return line;
/*     */   }
/*     */ 
/*     */   public static byte[] sendResponse(DataBinder data, ExecutionContext cxt, String encoding)
/*     */     throws IOException
/*     */   {
/* 284 */     boolean isMtom = isMtomRequest(data);
/* 285 */     String isoEncoding = DataSerializeUtils.getIsoEncoding(data.m_clientEncoding);
/*     */ 
/* 288 */     String boundary = getMimeBoundary(data);
/*     */ 
/* 291 */     byte[] soapHeaderBytes = getSoapHeader(boundary, isMtom, isoEncoding);
/*     */ 
/* 294 */     String soapBody = SoapXmlSerializer.sendResponse(data, cxt);
/* 295 */     byte[] soapBodyBytes = StringUtils.getBytes(soapBody, data.m_clientEncoding);
/*     */ 
/* 298 */     byte[] endBoundaryBytes = getEndBoundary(boundary);
/*     */ 
/* 301 */     String contentType = getMultipartContentType(boundary, isMtom, isoEncoding);
/* 302 */     data.setContentType(contentType);
/*     */ 
/* 304 */     ByteArrayOutputStream os = new ByteArrayOutputStream();
/* 305 */     os.write(soapHeaderBytes);
/* 306 */     os.write(soapBodyBytes);
/* 307 */     os.write(endBoundaryBytes);
/*     */ 
/* 309 */     return os.toByteArray();
/*     */   }
/*     */ 
/*     */   public static void sendStreamResponse(DataBinder data, Service service, DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor)
/*     */     throws IOException
/*     */   {
/* 315 */     boolean isMtom = isMtomRequest(data);
/* 316 */     String isoEncoding = DataSerializeUtils.getIsoEncoding(data.m_clientEncoding);
/*     */ 
/* 319 */     String boundary = getMimeBoundary(data);
/*     */ 
/* 322 */     byte[] soapHeaderBytes = getSoapHeader(boundary, isMtom, isoEncoding);
/*     */ 
/* 325 */     String soapBody = SoapXmlSerializer.sendResponse(data, service);
/* 326 */     byte[] soapBodyBytes = StringUtils.getBytes(soapBody, data.m_clientEncoding);
/*     */ 
/* 329 */     String fileHeader = null;
/* 330 */     String downloadName = streamWrapper.m_clientFileName;
/* 331 */     String format = streamWrapper.m_dataType;
/* 332 */     if (isMtom)
/*     */     {
/* 334 */       String fileContentID = data.m_environment.getProperty("SOAP:MtomFileContentID");
/* 335 */       if (fileContentID == null)
/*     */       {
/* 337 */         fileContentID = downloadName;
/*     */       }
/*     */ 
/* 340 */       fileHeader = "\r\n--" + boundary + "\r\n" + "content-id: <" + fileContentID + ">\r\n" + "content-type: " + format + "\r\n" + "content-transfer-encoding: binary\r\n\r\n";
/*     */     }
/*     */     else
/*     */     {
/* 347 */       fileHeader = "\r\n--" + boundary + "\r\n" + "Content-Type: " + format + "\r\n" + "Content-ID: " + downloadName + "\r\n\r\n";
/*     */     }
/*     */ 
/* 352 */     byte[] fileHeaderBytes = fileHeader.getBytes();
/*     */ 
/* 355 */     long fileLength = streamWrapper.m_streamLength;
/*     */ 
/* 358 */     byte[] endBoundaryBytes = getEndBoundary(boundary);
/*     */ 
/* 361 */     String contentType = getMultipartContentType(boundary, isMtom, isoEncoding);
/* 362 */     long contentLength = soapHeaderBytes.length + soapBodyBytes.length + fileHeaderBytes.length + fileLength + endBoundaryBytes.length;
/*     */ 
/* 364 */     data.m_contentType = (contentType + "\r\nContent-Length: " + contentLength);
/*     */ 
/* 366 */     String httpHeader = httpImplementor.createHttpResponseHeader();
/*     */ 
/* 369 */     OutputStream os = service.getOutput();
/*     */ 
/* 371 */     os.write(httpHeader.getBytes());
/* 372 */     os.write(soapHeaderBytes);
/* 373 */     os.write(soapBodyBytes);
/* 374 */     os.write(fileHeaderBytes);
/* 375 */     DataStreamWrapperUtils.copyInStreamToOutputStream(streamWrapper, os);
/* 376 */     os.write(endBoundaryBytes);
/*     */   }
/*     */ 
/*     */   public static String getMimeBoundary(DataBinder data)
/*     */   {
/* 382 */     Random random = new Random();
/* 383 */     String boundary = "----------------" + String.valueOf(random.nextLong());
/* 384 */     data.m_environment.put("BOUNDARY", boundary);
/*     */ 
/* 386 */     return boundary;
/*     */   }
/*     */ 
/*     */   public static byte[] getSoapHeader(String boundary, boolean isMtom, String encoding)
/*     */   {
/* 392 */     String soapHeader = null;
/* 393 */     if (isMtom)
/*     */     {
/* 395 */       soapHeader = "--" + boundary + "\r\n" + "Content-ID: <SoapContent>\r\n" + "Content-Type: " + "application/xop+xml" + "; charset=" + encoding + "; " + "type=\"text/xml; charset=" + encoding + "\"\r\n" + "Content-Transfer-Encoding: binary\r\n\r\n";
/*     */     }
/*     */     else
/*     */     {
/* 403 */       soapHeader = "--" + boundary + "\r\n" + "Content-Type: text/xml\r\n" + "Content-ID: <SoapContent>\r\n\r\n";
/*     */     }
/*     */ 
/* 409 */     byte[] soapHeaderBytes = soapHeader.getBytes();
/* 410 */     return soapHeaderBytes;
/*     */   }
/*     */ 
/*     */   public static byte[] getEndBoundary(String boundary)
/*     */   {
/* 415 */     String endBoundary = "\r\n--" + boundary + "--\r\n";
/* 416 */     byte[] endBoundaryBytes = endBoundary.getBytes();
/*     */ 
/* 418 */     return endBoundaryBytes;
/*     */   }
/*     */ 
/*     */   public static String getMultipartContentType(String boundary, boolean isMtom, String encoding)
/*     */   {
/* 424 */     String contentType = null;
/* 425 */     if (isMtom)
/*     */     {
/* 427 */       contentType = "multipart/related; type=\"application/xop+xml\"; boundary=" + boundary + "; " + "start=\"<SoapContent>\"; " + "start-info=\"text/xml; charset=" + encoding + "\"";
/*     */     }
/*     */     else
/*     */     {
/* 434 */       contentType = "Multipart/Related; boundary=" + boundary + "; type=\"text/xml\"; start=\"<SoapContent>\"";
/*     */     }
/*     */ 
/* 438 */     return contentType;
/*     */   }
/*     */ 
/*     */   public static boolean isMtomRequest(DataBinder binder)
/*     */   {
/* 443 */     boolean isMtom = StringUtils.convertToBool(binder.m_environment.getProperty("SOAP:IsMtomRequest"), false);
/*     */ 
/* 445 */     return isMtom;
/*     */   }
/*     */ 
/*     */   public static boolean processMtomFileContent(DataBinder data, String fileNodeName, PropertiesTreeNode fileContentNode, String fileName)
/*     */   {
/* 451 */     if (fileName == null)
/*     */     {
/* 453 */       fileName = "test.txt";
/*     */     }
/*     */ 
/* 458 */     PropertiesTreeNode mtomNode = SoapUtils.getFirstSubNode(fileContentNode.m_subNodes);
/* 459 */     if ((mtomNode == null) || (mtomNode.m_properties == null))
/*     */     {
/* 461 */       return false;
/*     */     }
/*     */ 
/* 464 */     String mtomNodeName = SoapUtils.getNodeName(mtomNode);
/* 465 */     if (!mtomNodeName.equals("Include"))
/*     */     {
/* 467 */       return false;
/*     */     }
/*     */ 
/* 470 */     String href = mtomNode.m_properties.getProperty("href");
/* 471 */     if (href == null)
/*     */     {
/* 473 */       return false;
/*     */     }
/*     */ 
/* 476 */     String fileKey = null;
/* 477 */     int index = href.indexOf("cid:");
/* 478 */     if (index < 0)
/*     */     {
/* 480 */       fileKey = href;
/*     */     }
/*     */     else
/*     */     {
/* 484 */       fileKey = href.substring(index + 4);
/*     */     }
/*     */ 
/* 487 */     data.putLocal(fileKey, fileName);
/* 488 */     data.putLocal(fileKey + ":fileKey", fileNodeName);
/*     */ 
/* 490 */     return true;
/*     */   }
/*     */ 
/*     */   public static void sendMtomFileResponse(DataBinder data, StringBuffer buffer, String fileNodeName, String fileName)
/*     */   {
/* 496 */     buffer.append("<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:" + fileNodeName + "\"/>");
/*     */ 
/* 499 */     data.m_environment.put("SOAP:MtomFileContentID", fileNodeName);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 504 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapMultipartSerializer
 * JD-Core Version:    0.5.4
 */