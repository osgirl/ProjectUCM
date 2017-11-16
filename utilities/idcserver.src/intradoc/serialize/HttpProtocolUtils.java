/*     */ package intradoc.serialize;
/*     */ 
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class HttpProtocolUtils
/*     */ {
/*  26 */   protected static boolean m_isInit = false;
/*  27 */   protected static boolean[] m_syncObject = { true };
/*     */   public static final String HTTP_PREFIX = "HTTP/1.1";
/*     */   public static final int SC_CONTINUE = 100;
/*     */   public static final int SC_SWITCHING_PROTOCOLS = 101;
/*     */   public static final int SC_PROCESSING = 102;
/*     */   public static final int SC_OK = 200;
/*     */   public static final int SC_CREATED = 201;
/*     */   public static final int SC_ACCEPTED = 202;
/*     */   public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
/*     */   public static final int SC_NO_CONTENT = 204;
/*     */   public static final int SC_RESET_CONTENT = 205;
/*     */   public static final int SC_PARTIAL_CONTENT = 206;
/*     */   public static final int SC_MULTISTATUS = 207;
/*     */   public static final int SC_MULTIPLE_CHOICES = 300;
/*     */   public static final int SC_MOVED_PERMANENTLY = 301;
/*     */   public static final int SC_FOUND = 302;
/*     */   public static final int SC_SEE_OTHER = 303;
/*     */   public static final int SC_NOT_MODIFIED = 304;
/*     */   public static final int SC_USE_PROXY = 305;
/*     */   public static final int SC_TEMPORARY_REDIRECT = 307;
/*     */   public static final int SC_BAD_REQUEST = 400;
/*     */   public static final int SC_UNAUTHORIZED = 401;
/*     */   public static final int SC_PAYMENT_REQUIRED = 402;
/*     */   public static final int SC_FORBIDDEN = 403;
/*     */   public static final int SC_NOT_FOUND = 404;
/*     */   public static final int SC_METHOD_NOT_ALLOWED = 405;
/*     */   public static final int SC_NOT_ACCEPTABLE = 406;
/*     */   public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
/*     */   public static final int SC_REQUEST_TIMEOUT = 408;
/*     */   public static final int SC_CONFLICT = 409;
/*     */   public static final int SC_GONE = 410;
/*     */   public static final int SC_LENGTH_REQUIRED = 411;
/*     */   public static final int SC_PRECONDITION_FAILED = 412;
/*     */   public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
/*     */   public static final int SC_REQUEST_URI_TOO_LONG = 414;
/*     */   public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
/*     */   public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
/*     */   public static final int SC_EXPECTATION_FAILED = 417;
/*     */   public static final int SC_UNPROCESSABLE_ENTITY = 422;
/*     */   public static final int SC_LOCKED = 423;
/*     */   public static final int SC_FAILED_DEPENDENCY = 424;
/*     */   public static final int SC_INTERNAL_SERVER_ERROR = 500;
/*     */   public static final int SC_NOT_IMPLEMENTED = 501;
/*     */   public static final int SC_BAD_GATEWAY = 502;
/*     */   public static final int SC_SERVICE_UNAVAILABLE = 503;
/*     */   public static final int SC_GATEWAY_TIMEOUT = 504;
/*     */   public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
/*     */   public static final int SC_INSUFFICIENT_STORAGE = 507;
/*  87 */   public static final Object[][] m_statusCodeMessages = { { new Integer(100), "Continue" }, { new Integer(101), "Switching Protocols" }, { new Integer(102), "Processing" }, { new Integer(200), "OK" }, { new Integer(201), "Created" }, { new Integer(202), "Accepted" }, { new Integer(203), "Non-Authoritative Information" }, { new Integer(204), "No Content" }, { new Integer(205), "Reset Content" }, { new Integer(206), "Partial Content" }, { new Integer(207), "Multi-Status" }, { new Integer(300), "Multiple Choices" }, { new Integer(301), "Moved Permanently" }, { new Integer(302), "Found" }, { new Integer(303), "See Other" }, { new Integer(304), "Not Modified" }, { new Integer(305), "Use Proxy" }, { new Integer(307), "Temporary Redirect" }, { new Integer(400), "Bad Request" }, { new Integer(401), "Unauthorized" }, { new Integer(402), "Payment Required" }, { new Integer(403), "Forbidden" }, { new Integer(404), "Not Found" }, { new Integer(405), "Method Not Allowed" }, { new Integer(406), "Not Acceptable" }, { new Integer(407), "Proxy Authentication Required" }, { new Integer(408), "Request Timeout" }, { new Integer(409), "Conflict" }, { new Integer(410), "Gone" }, { new Integer(411), "Length Required" }, { new Integer(412), "Precondition Failed" }, { new Integer(413), "Request Entity Too Large" }, { new Integer(414), "Request-URI Too Long" }, { new Integer(415), "Unsupported Media Type" }, { new Integer(416), "Requested Range Not Satisfiable" }, { new Integer(417), "Expectation Failed" }, { new Integer(422), "Unprocessable Entity" }, { new Integer(423), "Locked" }, { new Integer(424), "Failed Dependency" }, { new Integer(500), "Internal Server Error" }, { new Integer(501), "Not Implemented" }, { new Integer(502), "Bad Gateway" }, { new Integer(503), "Service Unavailable" }, { new Integer(504), "Gateway Timeout" }, { new Integer(505), "HTTP Version Not Supported" }, { new Integer(507), "Insufficient Storage" } };
/*     */ 
/* 140 */   protected static Map m_statusCodeMessageMap = null;
/*     */ 
/*     */   protected static void checkInit()
/*     */   {
/* 144 */     if (m_isInit)
/*     */       return;
/* 146 */     synchronized (m_syncObject)
/*     */     {
/* 148 */       if (!m_isInit)
/*     */       {
/* 150 */         m_statusCodeMessageMap = new HashMap();
/* 151 */         for (int i = 0; i < m_statusCodeMessages.length; ++i)
/*     */         {
/* 153 */           m_statusCodeMessageMap.put(m_statusCodeMessages[i][0], m_statusCodeMessages[i][1]);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getMessageForStatusCode(int code)
/*     */   {
/* 162 */     checkInit();
/*     */ 
/* 164 */     return (String)m_statusCodeMessageMap.get(new Integer(code));
/*     */   }
/*     */ 
/*     */   public static String getOpeningHeaderString(int code)
/*     */   {
/* 169 */     return "HTTP/1.1 " + code + " " + getMessageForStatusCode(code);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 174 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.serialize.HttpProtocolUtils
 * JD-Core Version:    0.5.4
 */