/*     */ package intradoc.json;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LengthLimitedInputStream;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.FilterImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStreamReader;
/*     */ import java.io.Reader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JsonSerializer
/*     */   implements FilterImplementor
/*     */ {
/*     */   public static final String JSON_CONTENT_TYPE = "application/json";
/*  33 */   public static String m_defaultJsonRequestEncoding = "UTF8";
/*     */   protected Workspace m_ws;
/*     */   protected DataBinder m_binder;
/*     */   protected ExecutionContext m_cxt;
/*     */ 
/*     */   public JsonSerializer()
/*     */   {
/*  35 */     this.m_ws = null;
/*  36 */     this.m_binder = null;
/*  37 */     this.m_cxt = null;
/*     */   }
/*     */ 
/*     */   public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt) throws DataException, ServiceException
/*     */   {
/*  42 */     this.m_ws = ws;
/*  43 */     this.m_binder = binder;
/*  44 */     this.m_cxt = cxt;
/*     */ 
/*  46 */     int returnCode = 0;
/*  47 */     String parameter = (String)this.m_cxt.getCachedObject("filterParameter");
/*  48 */     if (parameter == null)
/*     */     {
/*  50 */       return returnCode;
/*     */     }
/*     */ 
/*  53 */     if (parameter.equals("parseDataForServiceRequest"))
/*     */     {
/*  55 */       returnCode = parseDataForServiceRequest();
/*     */     }
/*  57 */     else if ((parameter.equals("postParseDataForServiceRequest")) || (parameter.equals("postContinueParseDataForServiceRequest")))
/*     */     {
/*  60 */       returnCode = postParseDataForServiceRequest();
/*     */     }
/*  62 */     else if (parameter.equals("sendDataForServerResponseBytes"))
/*     */     {
/*  64 */       returnCode = sendDataForServerResponseBytes();
/*     */     }
/*     */ 
/*  67 */     return returnCode;
/*     */   }
/*     */ 
/*     */   public int parseDataForServiceRequest() throws DataException, ServiceException
/*     */   {
/*  72 */     boolean isJsonRequest = false;
/*     */ 
/*  75 */     String contentType = this.m_binder.getEnvironmentValue("CONTENT_TYPE");
/*  76 */     if (contentType != null)
/*     */     {
/*  78 */       int index = contentType.indexOf(";");
/*  79 */       if (index > 0)
/*     */       {
/*  81 */         contentType = contentType.substring(0, index);
/*     */       }
/*  83 */       contentType = contentType.toLowerCase().trim();
/*     */ 
/*  86 */       if (contentType.indexOf("json") >= 0)
/*     */       {
/*  88 */         isJsonRequest = true;
/*     */       }
/*     */     }
/*     */ 
/*  92 */     if (isJsonRequest)
/*     */     {
/*  95 */       setRequestValues();
/*     */ 
/*  98 */       String queryString = this.m_binder.getEnvironmentValue("QUERY_STRING");
/*  99 */       if (queryString != null)
/*     */       {
/* 101 */         DataSerializeUtils.parseLocalParameters(this.m_binder, queryString, "&", null);
/* 102 */         this.m_binder.m_rawData.clear();
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 110 */         String jsonRequestEncoding = getJsonRequestEncoding(this.m_binder);
/*     */ 
/* 112 */         DataSerializeUtils.translateEnvironment(this.m_binder);
/* 113 */         this.m_binder.m_clientEncoding = jsonRequestEncoding;
/*     */ 
/* 115 */         BufferedReader reader = new BufferedReader(new InputStreamReader(new LengthLimitedInputStream(this.m_binder.m_inStream, this.m_binder.m_remainingLength), jsonRequestEncoding));
/*     */ 
/* 118 */         List newResultSets = new ArrayList();
/* 119 */         loadJsonRequestStream(reader, newResultSets);
/*     */ 
/* 121 */         DataSerializeUtils.determineParameterizedLocalization(this.m_binder, newResultSets);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 125 */         setFaultMessage(-1, e.getMessage());
/* 126 */         Report.trace(null, null, e);
/*     */       }
/*     */     }
/*     */ 
/* 130 */     return (isJsonRequest) ? 1 : 0;
/*     */   }
/*     */ 
/*     */   public void loadJsonRequestStream(Reader reader, List newResultSets) throws IOException
/*     */   {
/* 135 */     IdcCharArrayWriter buffer = new IdcCharArrayWriter();
/* 136 */     FileUtils.copyReaderToWriter(reader, buffer);
/* 137 */     Map jsonBinder = JsonUtils.createObject(buffer.m_charArray, new int[] { 0 }, buffer.m_length);
/* 138 */     buffer = buffer.release();
/* 139 */     JsonUtils.mergeIntoDataBinder(jsonBinder, this.m_binder, newResultSets);
/*     */   }
/*     */ 
/*     */   public static String getJsonRequestEncoding(DataBinder binder)
/*     */   {
/* 149 */     String jsonRequestEncoding = binder.getLocal("JsonRequestEncodingFormat");
/*     */ 
/* 151 */     if ((jsonRequestEncoding == null) || (jsonRequestEncoding.length() == 0))
/*     */     {
/* 153 */       jsonRequestEncoding = SharedObjects.getEnvironmentValue("JsonRequestEncodingFormat");
/*     */     }
/* 155 */     if ((jsonRequestEncoding == null) || (jsonRequestEncoding.length() == 0))
/*     */     {
/* 157 */       jsonRequestEncoding = m_defaultJsonRequestEncoding;
/*     */     }
/*     */ 
/* 160 */     return jsonRequestEncoding;
/*     */   }
/*     */ 
/*     */   public int postParseDataForServiceRequest()
/*     */   {
/* 165 */     String strIsJsonRequest = getRequestValue();
/* 166 */     boolean isJsonRequest = StringUtils.convertToBool(strIsJsonRequest, false);
/*     */ 
/* 169 */     String isJsonStr = getRequestFlag();
/* 170 */     if (isJsonStr != null)
/*     */     {
/* 172 */       boolean isJsonFlag = StringUtils.convertToBool(isJsonStr, false);
/*     */ 
/* 174 */       if ((isJsonFlag) && (!isJsonRequest))
/*     */       {
/* 177 */         setRequestValues();
/* 178 */         isJsonRequest = true;
/*     */       }
/* 180 */       else if ((!isJsonFlag) && (isJsonRequest))
/*     */       {
/* 183 */         setRequestValue("0");
/* 184 */         isJsonRequest = false;
/*     */ 
/* 186 */         boolean isJava = StringUtils.convertToBool(this.m_binder.getLocal("IsJava"), false);
/* 187 */         if (!isJava)
/*     */         {
/* 189 */           this.m_binder.m_isJava = false;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 194 */     return 0;
/*     */   }
/*     */ 
/*     */   public int sendDataForServerResponseBytes()
/*     */   {
/* 199 */     int returnValue = 0;
/*     */ 
/* 201 */     String isJson = getRequestValue();
/* 202 */     if (StringUtils.convertToBool(isJson, false))
/*     */     {
/* 204 */       String isJsonFault = this.m_binder.getEnvironmentValue("JSON:IsFault");
/* 205 */       if (StringUtils.convertToBool(isJsonFault, false))
/*     */       {
/* 207 */         this.m_binder.putLocal("StatusCode", this.m_binder.getEnvironmentValue("JSON:FaultCode"));
/* 208 */         String faultMessage = this.m_binder.getEnvironmentValue("JSON:FaultMessage");
/* 209 */         String faultMessageKey = faultMessage;
/* 210 */         if (faultMessage == null)
/*     */         {
/* 212 */           faultMessageKey = "!csJsonRequestError";
/* 213 */           faultMessage = LocaleResources.getString("csJsonRequestError", null);
/*     */         }
/* 215 */         faultMessage = LocaleResources.localizeMessage(faultMessage, this.m_cxt);
/*     */ 
/* 217 */         if (faultMessageKey.startsWith("!"))
/*     */         {
/* 219 */           this.m_binder.putLocal("StatusMessageKey", faultMessageKey);
/*     */         }
/* 221 */         this.m_binder.putLocal("StatusMessage", faultMessage);
/*     */       }
/*     */ 
/*     */       try
/*     */       {
/* 226 */         String jsonString = getJsonString();
/* 227 */         byte[] buffer = StringUtils.getBytes(jsonString, "UTF8");
/*     */ 
/* 229 */         this.m_cxt.setCachedObject("responseBytes", buffer);
/*     */ 
/* 232 */         String contentType = this.m_binder.getLocal("JSONContentType");
/* 233 */         if ((contentType == null) || (contentType.length() == 0))
/*     */         {
/* 235 */           contentType = SharedObjects.getEnvironmentValue("JSONContentType");
/* 236 */           if ((contentType == null) || (contentType.length() == 0))
/*     */           {
/* 238 */             contentType = "application/json";
/*     */           }
/*     */         }
/* 241 */         this.m_binder.setContentType(contentType);
/* 242 */         returnValue = 1;
/*     */       }
/*     */       catch (UnsupportedEncodingException e)
/*     */       {
/* 246 */         Report.trace(null, null, e);
/*     */       }
/*     */     }
/*     */ 
/* 250 */     return returnValue;
/*     */   }
/*     */ 
/*     */   public String getJsonString()
/*     */   {
/* 255 */     return JsonUtils.binderToJsonString(this.m_binder);
/*     */   }
/*     */ 
/*     */   public String getRequestFlag()
/*     */   {
/* 260 */     return this.m_binder.getLocal("IsJson");
/*     */   }
/*     */ 
/*     */   public String getRequestValue()
/*     */   {
/* 265 */     return this.m_binder.getEnvironmentValue("JSON:IsJsonRequest");
/*     */   }
/*     */ 
/*     */   public void setRequestValue(String value)
/*     */   {
/* 270 */     this.m_binder.setEnvironmentValue("JSON:IsJsonRequest", value);
/*     */   }
/*     */ 
/*     */   public void setRequestValues()
/*     */   {
/* 275 */     this.m_binder.m_isJava = true;
/* 276 */     setRequestValue("1");
/*     */   }
/*     */ 
/*     */   public void setFaultMessage(int code, String message)
/*     */   {
/* 281 */     this.m_binder.setEnvironmentValue("JSON:IsFault", "1");
/* 282 */     this.m_binder.setEnvironmentValue("JSON:FaultCode", "" + code);
/* 283 */     this.m_binder.setEnvironmentValue("JSON:FaultMessage", message);
/* 284 */     this.m_binder.setEnvironmentValue("IdcService", "JSON_FAULT");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 289 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99218 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.json.JsonSerializer
 * JD-Core Version:    0.5.4
 */