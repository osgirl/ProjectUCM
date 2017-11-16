/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class WebRequestUtils
/*     */ {
/*  28 */   public static boolean m_allowMsieToGetNativeDesktopBytes = false;
/*  29 */   public static boolean m_httpHostDoesNotHavePort = false;
/*  30 */   public static boolean m_httpIgnoreWebServerInternalPortNumber = false;
/*  31 */   public static boolean[] m_syncInit = { false };
/*     */ 
/*  37 */   protected static long m_longCookieTimeoutInMillis = 31536000000L;
/*     */ 
/*     */   public static void checkInit()
/*     */   {
/*  41 */     if (m_syncInit[0] != 0)
/*     */       return;
/*  43 */     synchronized (m_syncInit)
/*     */     {
/*  45 */       if (m_syncInit[0] == 0)
/*     */       {
/*  47 */         init();
/*  48 */         m_syncInit[0] = true;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void init()
/*     */   {
/*  56 */     boolean allowNativeInUrlsToMsie = SharedObjects.getEnvValueAsBoolean("AllowMsieToGetNativeDesktopBytes", m_allowMsieToGetNativeDesktopBytes);
/*     */ 
/*  58 */     m_allowMsieToGetNativeDesktopBytes = allowNativeInUrlsToMsie;
/*  59 */     m_httpHostDoesNotHavePort = SharedObjects.getEnvValueAsBoolean("HttpBrowserHttpHostDoesNotHavePort", m_httpHostDoesNotHavePort);
/*     */ 
/*  61 */     m_httpIgnoreWebServerInternalPortNumber = SharedObjects.getEnvValueAsBoolean("HttpIgnoreWebServerInternalPortNumber", false);
/*  62 */     m_longCookieTimeoutInMillis = SharedObjects.getTypedEnvironmentInt("LongCookieTimeoutInDays", 365, 21, 21) * 24 * 3600 * 1000L;
/*     */   }
/*     */ 
/*     */   public static boolean determineUseFullEncoding(String url, String[] retPageCharset, DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  68 */     checkInit();
/*  69 */     boolean isMSIE = true;
/*  70 */     boolean needFullEncoding = StringUtils.getIsDefaultFullXmlEncodeMode();
/*  71 */     String encodingMode = binder.getLocal("XmlEncodingMode");
/*  72 */     if ((encodingMode != null) && (encodingMode.length() > 0))
/*     */     {
/*  74 */       needFullEncoding = encodingMode.equalsIgnoreCase("Full");
/*     */     }
/*  76 */     String pageCharset = binder.getAllowMissing("PageCharset");
/*     */ 
/*  78 */     if ((pageCharset == null) && (retPageCharset != null) && (retPageCharset.length > 0))
/*     */     {
/*  80 */       pageCharset = retPageCharset[0];
/*     */     }
/*  82 */     boolean isUtf8 = false;
/*  83 */     Service service = null;
/*  84 */     if (url == null)
/*     */     {
/*  86 */       return false;
/*     */     }
/*     */ 
/*  89 */     if (!needFullEncoding)
/*     */     {
/*  91 */       if ((pageCharset != null) && (pageCharset.equalsIgnoreCase("utf-8")))
/*     */       {
/*  93 */         isUtf8 = true;
/*     */       }
/*  95 */       if ((cxt != null) && (cxt instanceof Service))
/*     */       {
/*  97 */         service = (Service)cxt;
/*     */ 
/* 101 */         boolean isAbsoluteWeb = DataBinderUtils.getLocalBoolean(binder, "isAbsoluteWeb", false);
/* 102 */         isMSIE = (isAbsoluteWeb) || (service.getMSIEVersion() > 1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 107 */     if ((!needFullEncoding) && (isMSIE))
/*     */     {
/* 109 */       if (isUtf8)
/*     */       {
/* 111 */         boolean isPDF = false;
/*     */ 
/* 114 */         String docFormat = binder.getAllowMissing("dWebExtension");
/* 115 */         if ((docFormat != null) && (docFormat.indexOf("pdf") >= 0))
/*     */         {
/* 119 */           isPDF = true;
/*     */         }
/*     */         else
/*     */         {
/* 123 */           String testUrl = url;
/* 124 */           int index = url.indexOf(63);
/* 125 */           int index2 = url.indexOf(35);
/* 126 */           if ((index2 >= 0) && (((index2 < index) || (index < 0))))
/*     */           {
/* 128 */             index = index2;
/*     */           }
/* 130 */           if (index >= 0)
/*     */           {
/* 132 */             testUrl = url.substring(0, index);
/*     */           }
/* 134 */           String ext = FileUtils.getExtension(testUrl);
/* 135 */           if ((ext != null) && (ext.equalsIgnoreCase("pdf")))
/*     */           {
/* 137 */             isPDF = true;
/*     */           }
/*     */         }
/* 140 */         if (isPDF)
/*     */         {
/* 142 */           needFullEncoding = true;
/*     */         }
/*     */ 
/*     */       }
/* 149 */       else if (!m_allowMsieToGetNativeDesktopBytes)
/*     */       {
/* 151 */         needFullEncoding = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 156 */     if ((retPageCharset != null) && (retPageCharset.length > 0))
/*     */     {
/* 158 */       retPageCharset[0] = pageCharset;
/*     */     }
/* 160 */     return needFullEncoding;
/*     */   }
/*     */ 
/*     */   public static String encodeUrlForBrowser(String url, DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 166 */     checkInit();
/* 167 */     String[] pageCharset = { binder.m_clientEncoding };
/* 168 */     boolean needFullEncoding = determineUseFullEncoding(url, pageCharset, binder, cxt);
/* 169 */     if (needFullEncoding)
/*     */     {
/* 171 */       url = StringUtils.urlEscape7Bit(url, '%', pageCharset[0]);
/*     */     }
/* 173 */     return url;
/*     */   }
/*     */ 
/*     */   public static String encodeUrlSegmentForBrowser(String urlPart, DataBinder binder, ExecutionContext cxt)
/*     */   {
/* 188 */     checkInit();
/* 189 */     return encodeUrlSegmentForBrowserFull(urlPart, binder, cxt, true, null, null);
/*     */   }
/*     */ 
/*     */   public static String encodeUrlSegmentForBrowserFull(String urlPart, DataBinder binder, ExecutionContext cxt, boolean usePluses, String clientEncoding, String xmlEncodingMode)
/*     */   {
/* 212 */     checkInit();
/* 213 */     if (xmlEncodingMode == null)
/*     */     {
/* 218 */       xmlEncodingMode = binder.getLocal("XmlEncodingMode");
/* 219 */       if (xmlEncodingMode == null)
/*     */       {
/* 223 */         xmlEncodingMode = binder.getEnvironmentValue("UrlDefaultEncodingMode");
/*     */       }
/*     */     }
/* 226 */     if ((clientEncoding == null) || (clientEncoding.length() == 0))
/*     */     {
/* 228 */       clientEncoding = DataSerializeUtils.determineEncoding(binder, null);
/*     */     }
/*     */ 
/* 231 */     return StringUtils.encodeUrlStyle(urlPart, '%', usePluses, xmlEncodingMode, clientEncoding);
/*     */   }
/*     */ 
/*     */   public static String getBrowserHostAddress(ExecutionContext cxt, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 248 */     String isHttps = binder.getAllowMissing("IS_HTTPS");
/* 249 */     String protocol = binder.getEnvironmentValue("SERVER_PROTOCOL");
/* 250 */     String fullPath = null;
/*     */ 
/* 253 */     if ((protocol == null) && (isHttps == null))
/*     */     {
/* 255 */       return null;
/*     */     }
/*     */ 
/* 259 */     boolean useSsl = false;
/* 260 */     if (isHttps != null)
/*     */     {
/* 262 */       useSsl = StringUtils.convertToBool(isHttps, false);
/* 263 */       fullPath = (useSsl) ? "https" : "http";
/*     */     }
/*     */     else
/*     */     {
/* 267 */       int index = protocol.indexOf(47);
/* 268 */       if (index > 0)
/*     */       {
/* 270 */         fullPath = protocol.substring(0, index);
/* 271 */         fullPath = fullPath.toLowerCase();
/*     */       }
/*     */       else
/*     */       {
/* 275 */         fullPath = "http";
/*     */       }
/*     */ 
/* 278 */       useSsl = SharedObjects.getEnvValueAsBoolean("UseSSL", false);
/* 279 */       if ((useSsl) && (fullPath.equals("http")))
/*     */       {
/* 281 */         fullPath = "https";
/*     */       }
/*     */     }
/*     */ 
/* 285 */     fullPath = fullPath + "://";
/* 286 */     String serverName = binder.getEnvironmentValue("HTTP_HOST");
/* 287 */     if (serverName == null)
/*     */     {
/* 289 */       serverName = binder.getEnvironmentValue("SERVER_NAME");
/*     */     }
/* 291 */     fullPath = fullPath + serverName;
/*     */ 
/* 294 */     String portStr = null;
/* 295 */     if (m_httpHostDoesNotHavePort)
/*     */     {
/* 297 */       String serverPort = null;
/* 298 */       if (!m_httpIgnoreWebServerInternalPortNumber)
/*     */       {
/* 300 */         serverPort = binder.getEnvironmentValue("SERVER_PORT");
/*     */       }
/* 302 */       if ((serverPort != null) && (serverPort.length() > 0))
/*     */       {
/* 304 */         if (((!useSsl) && (!serverPort.equals("80"))) || ((useSsl) && (!serverPort.equals("443"))))
/*     */         {
/* 307 */           portStr = serverPort;
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 312 */         String fullHostAddress = binder.getAllowMissing("HttpServerAddress");
/* 313 */         int index = fullHostAddress.indexOf(":");
/* 314 */         if (index > 0)
/*     */         {
/* 316 */           portStr = fullHostAddress.substring(index + 1);
/* 317 */           index = portStr.indexOf(47);
/* 318 */           if (index > 0)
/*     */           {
/* 320 */             portStr = portStr.substring(0, index);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/* 325 */     if (portStr != null)
/*     */     {
/* 327 */       fullPath = fullPath + ":";
/* 328 */       fullPath = fullPath + portStr;
/*     */     }
/*     */ 
/* 331 */     return fullPath;
/*     */   }
/*     */ 
/*     */   public static boolean getAllowMsieToGetNativeDesktopBytes()
/*     */   {
/* 336 */     checkInit();
/* 337 */     return m_allowMsieToGetNativeDesktopBytes;
/*     */   }
/*     */ 
/*     */   public static void setAllowMsieToGetNativeDesktopBytes(boolean allowMsieToGetNativeDesktopBytes)
/*     */   {
/* 342 */     checkInit();
/* 343 */     m_allowMsieToGetNativeDesktopBytes = allowMsieToGetNativeDesktopBytes;
/*     */   }
/*     */ 
/*     */   public static long getLongCookieTimeoutInMillis()
/*     */   {
/* 348 */     checkInit();
/* 349 */     return m_longCookieTimeoutInMillis;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 354 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79564 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.WebRequestUtils
 * JD-Core Version:    0.5.4
 */