/*     */ package intradoc.serialize;
/*     */ 
/*     */ import intradoc.common.IdcAppendable;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.List;
/*     */ 
/*     */ public class HttpHeaders
/*     */ {
/*     */   public String m_httpStartLines;
/*     */   public List<HttpHeader> m_httpHeaders;
/*     */   public String m_httpCookiePath;
/*     */ 
/*     */   public HttpHeaders()
/*     */   {
/*  31 */     this.m_httpStartLines = null;
/*  32 */     this.m_httpHeaders = new ArrayList();
/*  33 */     this.m_httpCookiePath = "/";
/*     */   }
/*     */ 
/*     */   public void setHeader(String key, String value)
/*     */   {
/*  73 */     boolean didSet = false;
/*     */ 
/*  75 */     for (int i = 0; i < this.m_httpHeaders.size(); ++i)
/*     */     {
/*  77 */       HttpHeader header = (HttpHeader)this.m_httpHeaders.get(i);
/*  78 */       if (!StringUtils.isConfigValueCaseInsensitiveEquals(key, header.m_key))
/*     */         continue;
/*  80 */       if (!didSet)
/*     */       {
/*  82 */         header.m_value = value;
/*  83 */         didSet = true;
/*     */       }
/*     */       else
/*     */       {
/*  87 */         this.m_httpHeaders.remove(i);
/*  88 */         --i;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  93 */     if (didSet)
/*     */       return;
/*  95 */     HttpHeader header = new HttpHeader(key, value);
/*  96 */     this.m_httpHeaders.add(header);
/*     */   }
/*     */ 
/*     */   public void setHeaderIfEmpty(String key, String value)
/*     */   {
/* 107 */     boolean found = false;
/*     */ 
/* 109 */     int size = this.m_httpHeaders.size();
/* 110 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 112 */       HttpHeader header = (HttpHeader)this.m_httpHeaders.get(i);
/* 113 */       if (!StringUtils.isConfigValueCaseInsensitiveEquals(key, header.m_key))
/*     */         continue;
/* 115 */       found = true;
/* 116 */       break;
/*     */     }
/*     */ 
/* 120 */     if (found)
/*     */       return;
/* 122 */     HttpHeader header = new HttpHeader(key, value);
/* 123 */     this.m_httpHeaders.add(header);
/*     */   }
/*     */ 
/*     */   public void appendHeader(String key, String value)
/*     */   {
/* 135 */     HttpHeader header = new HttpHeader(key, value, true);
/* 136 */     this.m_httpHeaders.add(header);
/*     */   }
/*     */ 
/*     */   public void appendCookie(String name, String value, long timeout)
/*     */   {
/* 147 */     appendCookie(name, value, timeout, this.m_httpCookiePath);
/*     */   }
/*     */ 
/*     */   public void appendCookie(String name, String value, long timeout, String path)
/*     */   {
/* 159 */     IdcStringBuilder cookieValue = new IdcStringBuilder();
/* 160 */     cookieValue.append(name);
/* 161 */     cookieValue.append('=');
/* 162 */     cookieValue.append(StringUtils.encodeHttpHeaderStyle(value, true));
/* 163 */     cookieValue.append("; path=");
/* 164 */     cookieValue.append(path);
/* 165 */     cookieValue.append(";");
/*     */ 
/* 167 */     if (timeout != 0L)
/*     */     {
/* 170 */       Date dte = new Date();
/* 171 */       dte = new Date(dte.getTime() + timeout);
/* 172 */       String tstamp = LocaleUtils.formatRFC1123Date(dte);
/*     */ 
/* 174 */       cookieValue.append(" Expires=");
/* 175 */       cookieValue.append(tstamp);
/* 176 */       cookieValue.append(';');
/*     */     }
/*     */ 
/* 179 */     appendHeader("Set-Cookie", cookieValue.toString());
/*     */   }
/*     */ 
/*     */   public boolean removeHeader(String key)
/*     */   {
/* 189 */     boolean didRemove = false;
/* 190 */     for (int i = 0; i < this.m_httpHeaders.size(); ++i)
/*     */     {
/* 192 */       HttpHeader header = (HttpHeader)this.m_httpHeaders.get(i);
/* 193 */       if (!StringUtils.isConfigValueCaseInsensitiveEquals(key, header.m_key))
/*     */         continue;
/* 195 */       this.m_httpHeaders.remove(i);
/* 196 */       didRemove = true;
/* 197 */       --i;
/*     */     }
/*     */ 
/* 201 */     return didRemove;
/*     */   }
/*     */ 
/*     */   public void merge(HttpHeaders headers)
/*     */   {
/* 210 */     if ((headers.m_httpStartLines != null) && (headers.m_httpStartLines.length() > 0))
/*     */     {
/* 212 */       this.m_httpStartLines = headers.m_httpStartLines;
/*     */     }
/*     */ 
/* 215 */     int size = headers.m_httpHeaders.size();
/* 216 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 218 */       HttpHeader header = (HttpHeader)headers.m_httpHeaders.get(i);
/* 219 */       if (header.m_isAppend)
/*     */       {
/* 221 */         appendHeader(header.m_key, header.m_value);
/*     */       }
/*     */       else
/*     */       {
/* 225 */         setHeader(header.m_key, header.m_value);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void write(IdcAppendable buffer)
/*     */   {
/* 236 */     if ((this.m_httpStartLines != null) && (this.m_httpStartLines.length() > 0))
/*     */     {
/* 238 */       buffer.append(this.m_httpStartLines);
/* 239 */       if (!this.m_httpStartLines.endsWith("\n"))
/*     */       {
/* 241 */         buffer.append("\r\n");
/*     */       }
/*     */     }
/*     */ 
/* 245 */     int size = this.m_httpHeaders.size();
/* 246 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 248 */       HttpHeader header = (HttpHeader)this.m_httpHeaders.get(i);
/* 249 */       buffer.append(header.m_key);
/* 250 */       buffer.append(": ");
/* 251 */       buffer.append(header.m_value);
/* 252 */       buffer.append("\r\n");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setDefaultCookiePath(String defaultPath)
/*     */   {
/* 262 */     if (defaultPath == null)
/*     */     {
/* 264 */       return;
/*     */     }
/* 266 */     this.m_httpCookiePath = defaultPath;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 271 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87885 $";
/*     */   }
/*     */ 
/*     */   public class HttpHeader
/*     */   {
/*     */     public String m_key;
/*     */     public String m_value;
/*     */     public boolean m_isAppend;
/*     */ 
/*     */     public HttpHeader(String key, String value)
/*     */     {
/*  46 */       this.m_key = key;
/*  47 */       this.m_value = value;
/*  48 */       this.m_isAppend = false;
/*     */     }
/*     */ 
/*     */     public HttpHeader(String key, String value, boolean isAppend)
/*     */     {
/*  53 */       this.m_key = key;
/*  54 */       this.m_value = value;
/*  55 */       this.m_isAppend = isAppend;
/*     */     }
/*     */ 
/*     */     public String toString()
/*     */     {
/*  61 */       return this.m_key + ": " + this.m_value;
/*     */     }
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.serialize.HttpHeaders
 * JD-Core Version:    0.5.4
 */