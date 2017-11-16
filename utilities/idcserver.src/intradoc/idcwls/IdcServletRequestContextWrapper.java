/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.util.Map;
/*     */ 
/*     */ public class IdcServletRequestContextWrapper
/*     */   implements IdcServletRequestContext
/*     */ {
/*     */   public Object m_wrappedObject;
/*     */   public Map m_localParameters;
/*     */   public ExecutionContext m_parentContext;
/*     */   public ServletActiveLocalData m_activeData;
/*     */   public Map<String, String[]> m_parametersCopy;
/*     */   public Map<String, String> m_headersCopy;
/*     */   public DataBinder m_binder;
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  43 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96980 $";
/*     */   }
/*     */ 
/*     */   public IdcServletRequestContextWrapper(Object wrappedObject)
/*     */   {
/*  80 */     this.m_wrappedObject = wrappedObject;
/*  81 */     this.m_localParameters = getLocalParameters();
/*  82 */     this.m_parentContext = new ExecutionContextAdaptor();
/*  83 */     this.m_binder = new DataBinder(SharedObjects.getSecureEnvironment());
/*  84 */     this.m_activeData = new ServletActiveLocalData();
/*  85 */     this.m_parentContext.setCachedObject("IdcServletRequestContext", this);
/*     */   }
/*     */ 
/*     */   public IdcServletConfig getServletConfig()
/*     */   {
/*  93 */     return new IdcServletConfigWrapper(ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getServletConfig", new Object[0]));
/*     */   }
/*     */ 
/*     */   public ExecutionContext getParentExecutionContext()
/*     */   {
/* 102 */     return this.m_parentContext;
/*     */   }
/*     */ 
/*     */   public ServletActiveLocalData getActiveData()
/*     */   {
/* 107 */     return this.m_activeData;
/*     */   }
/*     */ 
/*     */   public DataBinder getParentDataBinder()
/*     */   {
/* 115 */     return this.m_binder;
/*     */   }
/*     */ 
/*     */   public void addResponseHeader(String key, String val)
/*     */   {
/* 123 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "addResponseHeader", new Object[] { key, val });
/*     */   }
/*     */ 
/*     */   public Map<String, String> getCopyRequestHeaders()
/*     */   {
/* 131 */     if (this.m_headersCopy == null)
/*     */     {
/* 133 */       this.m_headersCopy = ((Map)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getCopyRequestHeaders", new Object[0]));
/*     */     }
/*     */ 
/* 136 */     return this.m_headersCopy;
/*     */   }
/*     */ 
/*     */   public Map getLocalParameters()
/*     */   {
/* 144 */     return (Map)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getLocalParameters", new Object[0]);
/*     */   }
/*     */ 
/*     */   public Object getLocalParameter(String key)
/*     */   {
/* 153 */     return this.m_localParameters.get(key);
/*     */   }
/*     */ 
/*     */   public void setLocalParameter(String key, Object val)
/*     */   {
/* 160 */     if (key.equals("auth-user"))
/*     */     {
/* 162 */       this.m_activeData.m_authUser = ((String)val);
/*     */     }
/* 164 */     this.m_localParameters.put(key, val);
/*     */   }
/*     */ 
/*     */   public Map<String, String[]> getCopyRequestParameters()
/*     */   {
/* 169 */     if (this.m_parametersCopy == null)
/*     */     {
/* 171 */       this.m_parametersCopy = ((Map)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getCopyRequestParameters", new Object[0]));
/*     */     }
/*     */ 
/* 174 */     return this.m_parametersCopy;
/*     */   }
/*     */ 
/*     */   public Object getSessionAttribute(String key)
/*     */   {
/* 179 */     return ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getRequestSessionAttribute", new Object[] { key });
/*     */   }
/*     */ 
/*     */   public Map<String, Object> getCopySessionAttributes()
/*     */   {
/* 188 */     return (Map)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getCopyRequestSessionAttributes", new Object[0]);
/*     */   }
/*     */ 
/*     */   public InputStream getManufacturedInputStream()
/*     */   {
/* 197 */     return (InputStream)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getManufacturedInputStream", new Object[0]);
/*     */   }
/*     */ 
/*     */   public OutputStream getManufacturedOutputStream()
/*     */   {
/* 206 */     return (OutputStream)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getManufacturedOutputStream", new Object[0]);
/*     */   }
/*     */ 
/*     */   public Object getRequestAttribute(String key)
/*     */   {
/* 215 */     return ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getRequestAttribute", new Object[] { key });
/*     */   }
/*     */ 
/*     */   public String getRequestHeader(String key)
/*     */   {
/* 224 */     return (String)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getRequestHeader", new Object[] { key });
/*     */   }
/*     */ 
/*     */   public boolean getResponseSent()
/*     */   {
/* 233 */     return ((Boolean)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getResponseSent", new Object[0])).booleanValue();
/*     */   }
/*     */ 
/*     */   public boolean getSendResponseHeadersDirect()
/*     */   {
/* 242 */     return ((Boolean)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getSendResponseHeadersDirect", new Object[0])).booleanValue();
/*     */   }
/*     */ 
/*     */   public InputStream getServletInputStream()
/*     */     throws IOException
/*     */   {
/* 251 */     return (InputStream)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getServletInputStream", new Object[0]);
/*     */   }
/*     */ 
/*     */   public OutputStream getServletOutputStream()
/*     */     throws IOException
/*     */   {
/* 260 */     return (OutputStream)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "getServletOutputStream", new Object[0]);
/*     */   }
/*     */ 
/*     */   public boolean isBinaryPost()
/*     */   {
/* 269 */     return ((Boolean)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "isBinaryPost", new Object[0])).booleanValue();
/*     */   }
/*     */ 
/*     */   public boolean isHttpRequest()
/*     */   {
/* 278 */     return ((Boolean)ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "isHttpRequest", new Object[0])).booleanValue();
/*     */   }
/*     */ 
/*     */   public void sendStandardHttpErrorResponse()
/*     */     throws IOException
/*     */   {
/*     */     try
/*     */     {
/* 289 */       ClassHelperUtils.executeMethodWithArgs(this.m_wrappedObject, "sendStandardHttpErrorResponse", new Object[0]);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 294 */       throw ClassHelperUtils.convertToIOException(e, "sendStandardHttpErrorResponse");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setHttpResponseStatusCode(int code)
/*     */   {
/* 303 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setHttpResponseStatusCode", new Object[] { Integer.valueOf(code) });
/*     */   }
/*     */ 
/*     */   public void setHttpResponseErrorMessage(String errMsg)
/*     */   {
/* 312 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setHttpResponseErrorMessage", new Object[] { errMsg });
/*     */   }
/*     */ 
/*     */   public void setIsBinaryPost(boolean isBinaryPost)
/*     */   {
/* 321 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setIsBinaryPost", new Object[] { Boolean.valueOf(isBinaryPost) });
/*     */   }
/*     */ 
/*     */   public void setManufacturedInputStream(InputStream stream)
/*     */   {
/* 330 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setManufacturedInputStream", new Object[] { stream });
/*     */   }
/*     */ 
/*     */   public void setManufacturedOutputStream(OutputStream stream)
/*     */   {
/* 339 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setManufacturedOutputStream", new Object[] { stream });
/*     */   }
/*     */ 
/*     */   public void setSessionAttribute(String key, Object value)
/*     */   {
/* 345 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setRequestSessionAttribute", new Object[] { key, value });
/*     */   }
/*     */ 
/*     */   public void setRequestAttribute(String key, Object obj)
/*     */   {
/* 354 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setRequestAttribute", new Object[] { key, obj });
/*     */   }
/*     */ 
/*     */   public void setResponseHeader(String key, String val)
/*     */   {
/* 363 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setResponseHeader", new Object[] { key, val });
/*     */   }
/*     */ 
/*     */   public void setResponseSent(boolean responseSent)
/*     */   {
/* 372 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setResponseSent", new Object[] { Boolean.valueOf(responseSent) });
/*     */   }
/*     */ 
/*     */   public void setSendResponseHeadersDirect(boolean sendResponseHeadersDirect)
/*     */   {
/* 381 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setSendResponseHeadersDirect", new Object[] { Boolean.valueOf(sendResponseHeadersDirect) });
/*     */   }
/*     */ 
/*     */   public void setCurrentConfigAsOwner()
/*     */   {
/* 390 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "setCurrentConfigAsOwner", new Object[0]);
/*     */   }
/*     */ 
/*     */   public void logout()
/*     */   {
/* 399 */     ClassHelperUtils.executeMethodSuppressException(this.m_wrappedObject, "logout", new Object[0]);
/*     */   }
/*     */ 
/*     */   public void setRequestUri(String uri)
/*     */   {
/* 408 */     if (uri == null)
/*     */     {
/* 410 */       return;
/*     */     }
/*     */ 
/* 414 */     ServletActiveLocalData data = getActiveData();
/*     */ 
/* 416 */     int index = uri.indexOf("?");
/* 417 */     if (index > 0)
/*     */     {
/* 419 */       data.m_query = uri.substring(index + 1);
/* 420 */       data.m_queryUpdated = true;
/* 421 */       data.m_uri = uri.substring(0, index);
/* 422 */       data.m_delegateToContentServer = ((uri.indexOf("idcplg") > 0) && (uri.indexOf("/groups/") < 0));
/*     */     }
/*     */     else
/*     */     {
/* 426 */       data.m_uri = uri;
/*     */     }
/* 428 */     data.m_decodedUri = null;
/* 429 */     data.m_lowerCaseDecodedUri = null;
/* 430 */     data.m_haveDecodedUris = false;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.IdcServletRequestContextWrapper
 * JD-Core Version:    0.5.4
 */