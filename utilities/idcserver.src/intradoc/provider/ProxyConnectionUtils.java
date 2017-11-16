/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StackTrace;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Date;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ProxyConnectionUtils
/*     */ {
/*  32 */   static final String[] PROXY_PARAMETERS = { "HOST", "RELATIVEURL", "CGIPATHROOT", "SERVER-NAME", "SERVER-PORT", "SERVER-PROTOCOL" };
/*     */ 
/*     */   public static void copyOverProxyHeaders(DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*  44 */     if (binder == null)
/*     */     {
/*  46 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  51 */       if (PluginFilters.filter("copyOverProxyHeaders", null, binder, cxt) == -1)
/*     */       {
/*  54 */         return;
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/*  59 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/*  63 */     for (int i = 0; i < PROXY_PARAMETERS.length; ++i)
/*     */     {
/*  65 */       String key = PROXY_PARAMETERS[i];
/*  66 */       String proxyKey = "IDCPROXY-" + key;
/*  67 */       String val = binder.getEnvironmentValue(proxyKey);
/*  68 */       if (val == null)
/*     */         continue;
/*  70 */       String trueKey = null;
/*  71 */       if (key.startsWith("SERVER-"))
/*     */       {
/*  73 */         key = key.replace('-', '_');
/*  74 */         trueKey = key;
/*     */       }
/*     */       else
/*     */       {
/*  78 */         trueKey = "HTTP_" + key;
/*     */       }
/*  80 */       String origKey = "IDCAGENT_" + key;
/*  81 */       String origVal = binder.getEnvironmentValue(trueKey);
/*  82 */       binder.setEnvironmentValue(trueKey, val);
/*  83 */       if (origVal == null)
/*     */         continue;
/*  85 */       binder.setEnvironmentValue(origKey, origVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean incomingProxyAuth(Map proxyParams, DataBinder binder, String clientHostName, String clientHostAddress, IncomingConnection conn, ExecutionContext cxt)
/*     */     throws IOException, DataException, ServiceException
/*     */   {
/* 101 */     cxt.setCachedObject("ProxyAuthParams", proxyParams);
/* 102 */     binder.removeLocal("isIncomingSocketProxyAuthorized");
/* 103 */     int retVal = PluginFilters.filter("incomingSocketProxyAuth", null, binder, cxt);
/* 104 */     if (retVal != 0)
/*     */     {
/* 106 */       if (retVal == -1)
/*     */       {
/* 108 */         return false;
/*     */       }
/* 110 */       if (retVal == 1)
/*     */       {
/* 115 */         return (DataBinderUtils.getBoolean(binder, "isIncomingSocketProxyAuthorized", false)) || (SharedObjects.getEnvValueAsBoolean("DoNotRequireIsIncomingSocketProxyAuthorized", false));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 122 */     String pDataStr = binder.getEnvironmentValue("IDCPROXYAUTH");
/*     */ 
/* 124 */     DataBinder b = new DataBinder();
/* 125 */     if (pDataStr != null)
/*     */     {
/* 127 */       pDataStr = StringUtils.decodeLiteralStringEscapeSequence(pDataStr);
/* 128 */       BufferedReader r = new BufferedReader(new StringReader(pDataStr));
/* 129 */       b.receive(r);
/*     */     }
/* 131 */     String proxyName = b.getLocal("name");
/* 132 */     String enc = b.getLocal("encoding");
/* 133 */     String proxyPasswd = null;
/* 134 */     if ((proxyName != null) && (proxyName.length() > 0) && (!proxyName.equals("system")))
/*     */     {
/* 141 */       DataResultSet drset = SharedObjects.getTable("ProxiedConnections");
/* 142 */       if (drset != null)
/*     */       {
/* 144 */         int colIndex = ResultSetUtils.getIndexMustExist(drset, "pcName");
/* 145 */         if (drset.findRow(colIndex, proxyName) != null)
/*     */         {
/* 147 */           Properties rowProps = drset.getCurrentRowProps();
/* 148 */           proxyParams.putAll(rowProps);
/* 149 */           proxyPasswd = (String)proxyParams.get("pcPassword");
/* 150 */           String pxEnc = (String)proxyParams.get("pcPasswordEncoding");
/* 151 */           if ((pxEnc == null) || (!pxEnc.equalsIgnoreCase("SHA1")))
/*     */           {
/* 153 */             proxyPasswd = CryptoCommonUtils.sha1UuencodeHash(proxyPasswd, null);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 158 */           String msg = LocaleUtils.encodeMessage("csUnableToFindProxyEntry", null, proxyName);
/*     */ 
/* 160 */           ServiceException e = new ServiceException(msg);
/* 161 */           Report.trace(null, LocaleResources.localizeMessage(msg, null), e);
/* 162 */           throw e;
/*     */         }
/*     */       }
/*     */     }
/* 166 */     if ((proxyPasswd == null) && (conn != null))
/*     */     {
/* 168 */       DataBinder providerData = conn.getProviderData();
/* 169 */       proxyPasswd = CryptoPasswordUtils.determinePassword("ProxyPassword", providerData, false);
/*     */     }
/*     */ 
/* 178 */     if ((proxyPasswd != null) && (proxyPasswd.length() > 0))
/*     */     {
/* 185 */       while ((enc != null) && (enc.length() > 0))
/*     */       {
/* 187 */         int index = enc.indexOf("|");
/* 188 */         String key = enc;
/* 189 */         if (index >= 0)
/*     */         {
/* 191 */           key = enc.substring(0, index);
/* 192 */           enc = enc.substring(index + 1);
/*     */         }
/*     */         else
/*     */         {
/* 196 */           enc = null;
/*     */         }
/* 198 */         String val = null;
/* 199 */         if (key.length() > 0)
/*     */         {
/* 203 */           val = b.getLocal(key);
/*     */         }
/* 205 */         proxyPasswd = CryptoCommonUtils.sha1UuencodeHash(proxyPasswd, val);
/*     */       }
/*     */ 
/* 209 */       String expiresStr = b.getLocal("expires");
/* 210 */       if (expiresStr != null)
/*     */       {
/* 212 */         Date expiresDate = LocaleUtils.parseRFC1123Date(expiresStr);
/* 213 */         long expires = expiresDate.getTime();
/* 214 */         long curTime = System.currentTimeMillis();
/* 215 */         if (curTime > expires)
/*     */         {
/* 217 */           throw new ServiceException("!csProxyIdExpired");
/*     */         }
/* 219 */         if (curTime < expires - 86400000L)
/*     */         {
/* 221 */           throw new ServiceException("!csProxyIdLifeTooLong");
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 226 */       String hosts = b.getLocal("hosts");
/* 227 */       if ((hosts != null) && (hosts.length() > 0))
/*     */       {
/* 229 */         boolean goodClient = false;
/* 230 */         if ((clientHostName != null) && 
/* 232 */           (StringUtils.match(clientHostName, hosts, true)))
/*     */         {
/* 234 */           goodClient = true;
/*     */         }
/*     */ 
/* 237 */         if ((!goodClient) && (clientHostAddress != null) && 
/* 239 */           (StringUtils.match(clientHostAddress, hosts, true)))
/*     */         {
/* 241 */           goodClient = true;
/*     */         }
/*     */ 
/* 245 */         if (!goodClient)
/*     */         {
/* 247 */           String errMsg = LocaleUtils.encodeMessage("csBadClientMessage", null, clientHostName, clientHostAddress);
/*     */ 
/* 249 */           throw new ServiceException(-20, errMsg);
/*     */         }
/*     */       }
/* 252 */       else if (!SharedObjects.getEnvValueAsBoolean("AllowProxyConnectionWithoutIPFilter", false))
/*     */       {
/* 254 */         return false;
/*     */       }
/*     */ 
/* 258 */       String tPasswrd = getRemotePassword(b);
/* 259 */       if (!proxyPasswd.equals(tPasswrd))
/*     */       {
/* 261 */         Report.trace("socketprotocol", "Proxy password provided in connection not equal to one stored locally.", null);
/*     */ 
/* 263 */         if (SystemUtils.m_verbose)
/*     */         {
/* 265 */           Report.debug("socketprotocol", "local hashing became " + proxyPasswd + " and provided hash is " + tPasswrd, null);
/*     */         }
/*     */ 
/* 268 */         throw new ServiceException("!csConnectionAuthFailed");
/*     */       }
/*     */ 
/* 271 */       return true;
/*     */     }
/*     */ 
/* 276 */     return (proxyName != null) && (proxyName.equals("system")) && (SharedObjects.getEnvValueAsBoolean("AllowSystemProxyConnectionWithoutPassword", false));
/*     */   }
/*     */ 
/*     */   public static String getRemotePassword(DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/* 285 */     String password = binder.getLocal("password");
/* 286 */     if (password == null)
/*     */     {
/* 288 */       throw new ServiceException("!csConnectionAuthFailed");
/*     */     }
/* 290 */     return password;
/*     */   }
/*     */ 
/*     */   public static void loadIncomingProviderProxyConfig(IncomingProvider inProvider, DataBinder serverData, ExecutionContext cxt)
/*     */   {
/* 299 */     ProviderConfigUtils.loadSharedVariable(serverData, "ProxyPassword");
/* 300 */     ProviderConfigUtils.loadSharedVariable(serverData, "ProxyPasswordEncoding");
/* 301 */     ProviderConfigUtils.loadSharedTable(serverData, "ProxiedConnections");
/*     */     try
/*     */     {
/* 305 */       if (PluginFilters.filter("loadIncomingProviderProxyConfig", null, serverData, cxt) == -1)
/*     */       {
/* 308 */         return;
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 313 */       Report.trace(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void prepareOutgoingAuthKey(Properties props, DataBinder inBinder, DataBinder providerData, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 326 */     if (PluginFilters.filter("prepareOutgoingAuthKey", null, providerData, cxt) != 0)
/*     */     {
/* 329 */       return;
/*     */     }
/*     */ 
/* 337 */     String activeProxyAuth = inBinder.getLocal("IdcProxyAuth");
/* 338 */     Properties oldLocalData = null;
/* 339 */     if (activeProxyAuth != null)
/*     */     {
/* 341 */       if (SystemUtils.m_verbose)
/*     */       {
/* 343 */         String msg = "IdcProxyAuth present in request binder when being sent to proxy server. The stack trace is for debugging purposes;  it is not an indication of an error.";
/*     */ 
/* 345 */         Throwable t = new StackTrace(msg);
/* 346 */         Report.debug("socketprotocol", null, t);
/*     */       }
/* 348 */       oldLocalData = inBinder.getLocalData();
/* 349 */       inBinder.setLocalData((Properties)oldLocalData.clone());
/* 350 */       inBinder.removeLocal("IdcProxyAuth");
/*     */     }
/*     */ 
/* 354 */     DataBinder authBinder = providerData;
/* 355 */     String auth = providerData.getLocal("IdcProxyAuth");
/* 356 */     String alreadyAuth = props.getProperty("IdcProxyAuth");
/* 357 */     if ((auth != null) && (alreadyAuth == null))
/*     */     {
/* 360 */       authBinder = new DataBinder();
/*     */       try
/*     */       {
/* 363 */         authBinder.receive(new BufferedReader(new StringReader(auth)));
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 367 */         throw new AssertionError(e);
/*     */       }
/*     */     }
/*     */ 
/* 371 */     String p = authBinder.getLocal("password");
/* 372 */     if (p == null)
/*     */       return;
/* 374 */     String authEnc = authBinder.getLocal("encoding");
/* 375 */     if (authEnc == null)
/*     */     {
/* 377 */       authEnc = "expires";
/* 378 */       p = CryptoCommonUtils.sha1UuencodeHash(p, null);
/*     */     }
/*     */     else
/*     */     {
/* 382 */       authEnc = authEnc + "|expires";
/*     */     }
/* 384 */     String timeoutStr = providerData.getLocal("PasswordExpirationTimeout");
/* 385 */     String timeoutStrDepr = providerData.getLocal("PasswordExpireTimeout");
/* 386 */     if ((timeoutStr == null) && (timeoutStrDepr != null))
/*     */     {
/* 388 */       timeoutStr = timeoutStrDepr;
/* 389 */       SystemUtils.reportDeprecatedUsage("PasswordExpireTimeout is deprecated.  Use PasswordExpirationTimeout instead.");
/*     */     }
/*     */ 
/* 393 */     int timeout = 900000;
/* 394 */     if (timeoutStr != null)
/*     */     {
/* 396 */       timeout = NumberUtils.parseInteger(timeoutStr, timeout);
/*     */     }
/*     */ 
/* 399 */     long expireTime = System.currentTimeMillis() + timeout;
/* 400 */     String expireStr = LocaleUtils.formatRFC1123Date(new Date(expireTime));
/* 401 */     authBinder.putLocal("expires", expireStr);
/* 402 */     authBinder.putLocal("encoding", authEnc);
/*     */ 
/* 405 */     p = CryptoCommonUtils.sha1UuencodeHash(p, expireStr);
/* 406 */     authBinder.putLocal("password", p);
/* 407 */     IdcCharArrayWriter s = new IdcCharArrayWriter();
/*     */ 
/* 411 */     Properties authProps = authBinder.getLocalData();
/* 412 */     DataBinder temp = new DataBinder();
/* 413 */     temp.setLocalData(authProps);
/* 414 */     temp.m_blDateFormat = null;
/*     */     try
/*     */     {
/* 417 */       temp.sendEx(s, false);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 421 */       throw new AssertionError(e);
/*     */     }
/* 423 */     String newAuth = s.toStringRelease();
/* 424 */     newAuth = StringUtils.encodeLiteralStringEscapeSequence(newAuth.trim());
/* 425 */     props.put("IDCPROXYAUTH", newAuth);
/*     */   }
/*     */ 
/*     */   public static void addAdditionalOutgoingProxyHeaders(Properties headers, DataBinder inBinder, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 443 */     String str = inBinder.getLocal("ActAsAnonymous");
/* 444 */     boolean isAnonymous = StringUtils.convertToBool(str, false);
/* 445 */     if (isAnonymous)
/*     */       return;
/* 447 */     headers.put("IDCALLOWPROXYAUTH", "1");
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 453 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104051 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.ProxyConnectionUtils
 * JD-Core Version:    0.5.4
 */