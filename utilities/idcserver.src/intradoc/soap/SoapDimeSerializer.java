/*     */ package intradoc.soap;
/*     */ 
/*     */ import intradoc.common.DataStreamWrapper;
/*     */ import intradoc.common.DataStreamWrapperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceHttpImplementor;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.FileOutputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.OutputStream;
/*     */ 
/*     */ public class SoapDimeSerializer
/*     */ {
/*     */   public static void parseRequest(DataBinder data)
/*     */     throws IOException, DataException
/*     */   {
/*  33 */     boolean isFirstRecord = false;
/*  34 */     boolean isLastRecord = false;
/*     */ 
/*  36 */     FileOutputStream fos = null;
/*     */     try
/*     */     {
/*  41 */       while ((!data.m_isSuspended) && (!isLastRecord))
/*     */       {
/*  43 */         SoapDimeRecord record = new SoapDimeRecord();
/*  44 */         record.parse(data.m_inStream);
/*     */ 
/*  46 */         isFirstRecord = record.m_isMsgBegin;
/*  47 */         isLastRecord = record.m_isMsgEnd;
/*     */ 
/*  50 */         if (isFirstRecord)
/*     */         {
/*  52 */           SoapXmlSerializer.parseRequestEx(data, data.m_inStream, record.m_dataLength);
/*     */         }
/*     */         else
/*     */         {
/*  57 */           fos = parseFileRecord(data, record, fos);
/*     */         }
/*     */ 
/*  61 */         record.readPadding(data.m_inStream, record.m_dataLength);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/*  66 */       FileUtils.closeObject(fos);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static FileOutputStream parseFileRecord(DataBinder data, SoapDimeRecord record, FileOutputStream fos)
/*     */     throws IOException
/*     */   {
/*  74 */     String filePath = SoapUtils.getTempFile(data, record.m_recordID, null);
/*     */     try
/*     */     {
/*  79 */       if (fos == null)
/*     */       {
/*  81 */         fos = new FileOutputStream(filePath);
/*     */       }
/*     */ 
/*  84 */       int readSize = 10000;
/*  85 */       byte[] b = new byte[readSize];
/*  86 */       int bytesLeft = record.m_dataLength;
/*     */ 
/*  88 */       while (bytesLeft > 0)
/*     */       {
/*  90 */         int numToRead = readSize;
/*  91 */         if (bytesLeft < readSize)
/*     */         {
/*  93 */           numToRead = bytesLeft;
/*     */         }
/*     */ 
/*  96 */         b = SoapUtils.readStream(data.m_inStream, numToRead, false);
/*  97 */         fos.write(b, 0, numToRead);
/*     */ 
/*  99 */         bytesLeft -= numToRead;
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 106 */       if ((record.m_chunkFlag == 0) && (fos != null))
/*     */       {
/* 108 */         fos.close();
/* 109 */         fos = null;
/*     */       }
/*     */     }
/*     */ 
/* 113 */     return fos;
/*     */   }
/*     */ 
/*     */   public static byte[] sendResponse(DataBinder data, ExecutionContext cxt, String encoding)
/*     */     throws IOException
/*     */   {
/* 119 */     byte[] responseBytes = null;
/* 120 */     responseBytes = getSoapRecord(data, cxt, true);
/* 121 */     data.setContentType(SoapSerializer.DIME_CONTENT_TYPE);
/* 122 */     return responseBytes;
/*     */   }
/*     */ 
/*     */   public static void sendStreamResponse(DataBinder data, Service service, DataStreamWrapper streamWrapper, ServiceHttpImplementor httpImplementor)
/*     */     throws IOException
/*     */   {
/* 129 */     byte[] soapRecord = getSoapRecord(data, service, false);
/*     */ 
/* 132 */     long fileLength = streamWrapper.m_streamLength;
/* 133 */     String downloadName = streamWrapper.m_clientFileName;
/* 134 */     String format = streamWrapper.m_dataType;
/*     */ 
/* 137 */     SoapDimeRecord fileRecord = new SoapDimeRecord();
/* 138 */     fileRecord.m_isMsgBegin = false;
/* 139 */     fileRecord.m_isMsgEnd = true;
/* 140 */     fileRecord.m_recordID = downloadName;
/* 141 */     fileRecord.m_typeFormat = 1;
/* 142 */     fileRecord.m_recordType = format;
/* 143 */     fileRecord.m_dataLength = (int)fileLength;
/*     */ 
/* 145 */     byte[] fileRecordBody = fileRecord.send();
/* 146 */     byte[] fileDataPadding = fileRecord.getPaddingBytes((int)fileLength);
/*     */ 
/* 149 */     long fileRecordLength = fileRecordBody.length + fileLength + fileDataPadding.length;
/* 150 */     long contentLength = soapRecord.length + fileRecordLength;
/* 151 */     data.m_contentType = (SoapSerializer.DIME_CONTENT_TYPE + "\r\nContent-Length:  " + contentLength);
/*     */ 
/* 153 */     String httpHeader = httpImplementor.createHttpResponseHeader();
/*     */ 
/* 156 */     OutputStream os = service.getOutput();
/*     */ 
/* 158 */     os.write(httpHeader.getBytes());
/* 159 */     os.write(soapRecord);
/* 160 */     os.write(fileRecordBody);
/* 161 */     DataStreamWrapperUtils.copyInStreamToOutputStream(streamWrapper, os);
/* 162 */     os.write(fileDataPadding);
/*     */   }
/*     */ 
/*     */   protected static byte[] getSoapRecord(DataBinder data, ExecutionContext cxt, boolean isLastRecord)
/*     */     throws IOException
/*     */   {
/* 169 */     String soapStr = SoapXmlSerializer.sendResponse(data, cxt);
/* 170 */     byte[] soapBytes = StringUtils.getBytes(soapStr, data.m_clientEncoding);
/*     */ 
/* 172 */     String namespace = data.getEnvironmentValue("SOAP:Namespace");
/* 173 */     if (namespace == null)
/*     */     {
/* 175 */       namespace = SoapXmlSerializer.NAMESPACE_11;
/*     */     }
/*     */ 
/* 178 */     SoapDimeRecord record = new SoapDimeRecord();
/* 179 */     record.m_isMsgBegin = true;
/* 180 */     record.m_isMsgEnd = isLastRecord;
/* 181 */     record.m_recordID = "SoapContent";
/* 182 */     record.m_recordType = data.getEnvironmentValue("SOAP:Namespace");
/* 183 */     record.m_typeFormat = 2;
/* 184 */     record.m_dataLength = soapBytes.length;
/*     */ 
/* 186 */     byte[] soapRecordBody = record.send();
/* 187 */     byte[] soapDataPadding = record.getPaddingBytes(record.m_dataLength);
/*     */ 
/* 190 */     ByteArrayOutputStream os = new ByteArrayOutputStream();
/* 191 */     os.write(soapRecordBody);
/* 192 */     os.write(soapBytes);
/* 193 */     os.write(soapDataPadding);
/*     */ 
/* 195 */     return os.toByteArray();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 200 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95352 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.soap.SoapDimeSerializer
 * JD-Core Version:    0.5.4
 */