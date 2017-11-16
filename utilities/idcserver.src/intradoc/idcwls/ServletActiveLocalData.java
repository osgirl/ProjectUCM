/*     */ package intradoc.idcwls;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcDebugOutput;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.List;
/*     */ 
/*     */ public class ServletActiveLocalData
/*     */   implements IdcDebugOutput
/*     */ {
/*     */   public String m_method;
/*     */   public long m_contentLength;
/*     */   public String m_contextRoot;
/*     */   public boolean m_treatAsProxiedServerRequest;
/*     */   public boolean m_delegateToContentServer;
/*     */   public boolean m_contentExecutedByApplicationServer;
/*     */   public boolean m_isProxiedPath;
/*     */   public boolean m_isPromptLogin;
/*     */   public boolean m_isFinishedAuth;
/*     */   public boolean m_isError;
/*     */   public boolean m_isSessionLogin;
/*     */   public boolean m_showLoginForm;
/*     */   public Throwable m_errorException;
/*     */   public String m_authUser;
/*     */   public boolean m_authUserUpdated;
/*     */   public String m_query;
/*     */   public String m_uri;
/*     */   public String m_lowerCaseDecodedUri;
/*     */   public String m_decodedUri;
/*     */   public boolean m_haveDecodedUris;
/*     */   public String m_uriPath;
/*     */   public boolean m_queryUpdated;
/*     */   public List<String[]> m_additionalHeaders;
/*     */   public IdcStringBuilder m_serverRequestHeaders;
/*     */   public boolean m_inContentServerResponse;
/*     */   public int m_statusCode;
/*     */   public String m_responseHeaders;
/*     */   public int m_responseHeadersOffset;
/*     */   public UserData m_validateUrlUserData;
/*     */ 
/*     */   public ServletActiveLocalData()
/*     */   {
/*  84 */     this.m_isFinishedAuth = false;
/*     */   }
/*     */ 
/*     */   public void appendDebugFormat(IdcAppendable appendable)
/*     */   {
/* 214 */     appendable.append(this.m_method);
/* 215 */     appendable.append(" (");
/* 216 */     StringUtils.appendDebugProperty(appendable, "contentLength", "" + this.m_contentLength, false);
/* 217 */     StringUtils.appendDebugProperty(appendable, "contextRoot", this.m_contextRoot, true);
/* 218 */     if (this.m_uri != null)
/*     */     {
/* 220 */       StringUtils.appendDebugProperty(appendable, "uri", this.m_uri, true);
/*     */     }
/* 222 */     if (this.m_query != null)
/*     */     {
/* 224 */       StringUtils.appendDebugProperty(appendable, "query", this.m_query, true);
/*     */     }
/* 226 */     appendable.append(")");
/* 227 */     if (this.m_authUser == null)
/*     */       return;
/* 229 */     StringUtils.appendDebugProperty(appendable, "authUser", this.m_authUser, true);
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 240 */     IdcStringBuilder output = new IdcStringBuilder();
/* 241 */     appendDebugFormat(output);
/* 242 */     return output.toString();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 248 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87581 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.idcwls.ServletActiveLocalData
 * JD-Core Version:    0.5.4
 */