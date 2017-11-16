/*     */ package intradoc.client;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.ClassHelperUtils;
/*     */ import intradoc.common.DynamicData;
/*     */ import intradoc.common.DynamicDataParser;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.IntervalData;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseSyntaxException;
/*     */ import intradoc.common.ReportProgress;
/*     */ import intradoc.common.ResourceContainer;
/*     */ import intradoc.common.ScriptContext;
/*     */ import intradoc.common.ScriptExtensions;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.IdcProperties;
/*     */ import intradoc.data.SimpleParameters;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.provider.ServerRequest;
/*     */ import intradoc.provider.ServerRequestUtils;
/*     */ import intradoc.provider.SocketOutgoingProvider;
/*     */ import intradoc.serialize.DataBinderSerializer;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.LocaleLoader;
/*     */ import intradoc.shared.ResourceDataParser;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.net.MalformedURLException;
/*     */ import java.net.Proxy;
/*     */ import java.net.URL;
/*     */ import java.net.URLConnection;
/*     */ import java.net.URLStreamHandler;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.TimeZone;
/*     */ 
/*     */ public class ClientUtils
/*     */   implements Cloneable
/*     */ {
/*  72 */   protected static ClientUtils m_baseUtils = null;
/*     */   protected Properties m_connectionData;
/*     */   protected SocketOutgoingProvider m_outProvider;
/*     */ 
/*     */   public ClientUtils()
/*     */   {
/*  74 */     this.m_connectionData = null;
/*     */   }
/*     */ 
/*     */   public static void init() throws ServiceException
/*     */   {
/*  79 */     if (m_baseUtils != null)
/*     */       return;
/*  81 */     syncInit(new IdcProperties());
/*     */   }
/*     */ 
/*     */   public static void init(Properties config)
/*     */     throws ServiceException
/*     */   {
/*  87 */     if (m_baseUtils != null)
/*     */       return;
/*  89 */     syncInit(config);
/*     */   }
/*     */ 
/*     */   protected static synchronized void syncInit(Properties config)
/*     */     throws ServiceException
/*     */   {
/*  96 */     if (m_baseUtils != null)
/*     */       return;
/*  98 */     doBaseInit(config);
/*     */   }
/*     */ 
/*     */   public static synchronized void doBaseInit(Properties config)
/*     */     throws ServiceException
/*     */   {
/* 105 */     IntervalData timer = new IntervalData("ClientUtils init");
/* 106 */     SystemUtils.getAppProperties().setProperty("IdcUseNativeOSUtils", "0");
/* 107 */     ClientUtils utils = new ClientUtils();
/*     */ 
/* 109 */     SharedObjects.init();
/*     */ 
/* 111 */     DataSerializeUtils.setDataSerialize(new DataBinderSerializer());
/* 112 */     ClassLoader cl = utils.getClass().getClassLoader();
/*     */ 
/* 114 */     IdcMessage msg = IdcMessageFactory.lc();
/* 115 */     msg.m_msgSimple = "Unable to load resources.  Consider adding the $IdcHomeDir/jlib directory to the classpath.";
/*     */     try
/*     */     {
/* 119 */       ResourceContainer res = SharedObjects.getResources();
/* 120 */       parseAndAddResources(res, cl, "core/tables/std_encoding.htm");
/* 121 */       timer.trace("startup", "load std_encoding ");
/*     */ 
/* 123 */       parseAndAddResources(res, cl, "core/tables/std_locale.htm");
/* 124 */       timer.trace("startup", "load std_locale ");
/*     */ 
/* 126 */       DataBinder binder = new DataBinder();
/* 127 */       for (Object name : res.m_tables.keySet())
/*     */       {
/* 129 */         Table table = res.getTable((String)name);
/* 130 */         DataResultSet drset = new DataResultSet();
/* 131 */         drset.init(table);
/* 132 */         SharedObjects.putTable((String)name, drset);
/* 133 */         binder.addResultSet((String)name, drset);
/*     */       }
/* 135 */       timer.trace("startup", "create DataResultSets ");
/*     */ 
/* 137 */       DataResultSet encMap = SharedObjects.getTable("IsoJavaEncodingMap");
/* 138 */       DataSerializeUtils.setEncodingMap(encMap);
/* 139 */       timer.trace("startup", "setEncodingMap ");
/*     */ 
/* 141 */       DataResultSet aliasMap = SharedObjects.getTable("AliasesEncodingMap");
/* 142 */       if (aliasMap != null)
/*     */       {
/* 144 */         Properties aliasesMap = new Properties();
/* 145 */         for (aliasMap.first(); aliasMap.isRowPresent(); aliasMap.next())
/*     */         {
/* 147 */           String alias = aliasMap.getStringValue(0);
/* 148 */           String encoding = aliasMap.getStringValue(1);
/* 149 */           if ((alias == null) || (alias.length() == 0) || (encoding == null) || (encoding.length() == 0)) {
/*     */             continue;
/*     */           }
/* 152 */           aliasesMap.put(alias.toLowerCase(), encoding);
/*     */         }
/*     */ 
/* 155 */         LocaleResources.m_encodingAliasesMap = aliasesMap;
/*     */       }
/* 157 */       timer.trace("startup", "alias map ");
/*     */ 
/* 162 */       for (String prefix : new String[] { "sy", "ap", "cs", "ww" })
/*     */       {
/* 164 */         String path = "core/lang/" + prefix + "_strings.htm";
/* 165 */         BufferedReader reader = ClassHelperUtils.createBufferedReaderForClassResource(cl, path);
/*     */         try
/*     */         {
/* 168 */           res.parseAndAddResourcesWithFlags(reader, path, "", null, 0);
/*     */         }
/*     */         catch (IOException ioe)
/*     */         {
/* 172 */           throw new ServiceException(ioe, -1, "csErrorLoadingResourceFile", new Object[] { path });
/*     */         }
/*     */         catch (ParseSyntaxException pse)
/*     */         {
/* 176 */           throw new ServiceException(pse, -1, "csErrorLoadingResourceFile", new Object[] { path });
/*     */         }
/* 178 */         res.m_stringsList.add("!reset");
/*     */       }
/* 180 */       timer.trace("startup", "load strings ");
/* 181 */       LocaleResources.initStrings(res);
/* 182 */       timer.trace("startup", "init strings ");
/* 183 */       SharedLoader.loadInitialConfig();
/* 184 */       timer.trace("startup", "loadInitialConfig() ");
/*     */ 
/* 187 */       String tz = config.getProperty("SystemTimeZone");
/* 188 */       if (tz == null)
/*     */       {
/* 190 */         tz = SharedObjects.getEnvironmentValue("SystemTimeZone");
/*     */       }
/* 192 */       if (tz == null)
/*     */       {
/* 194 */         TimeZone timezone = TimeZone.getDefault();
/* 195 */         tz = timezone.getID();
/*     */       }
/* 197 */       binder.putLocal("SystemTimeZone", tz);
/*     */ 
/* 199 */       String systemLocale = config.getProperty("SystemLocale");
/* 200 */       if (systemLocale == null)
/*     */       {
/* 202 */         systemLocale = SharedObjects.getEnvironmentValue("SystemLocale");
/*     */       }
/* 204 */       if (systemLocale == null)
/*     */       {
/* 206 */         systemLocale = LocaleLoader.determineDefaultLocale();
/*     */       }
/* 208 */       binder.putLocal("SystemLocale", systemLocale);
/*     */ 
/* 210 */       LocaleResources.initStrings(res);
/* 211 */       LocaleLoader.loadLocaleConfig(null, new HashMap(), binder);
/* 212 */       timer.trace("startup", "loadLocaleConfig() ");
/*     */ 
/* 214 */       DynamicDataParser dataParser = new ResourceDataParser();
/* 215 */       DynamicData.addParser(dataParser);
/*     */ 
/* 217 */       parseAndAddResources(res, cl, "core/tables/std_resources.htm");
/* 218 */       timer.trace("startup", "load std_resources.htm ");
/*     */ 
/* 220 */       ScriptContext scriptContext = new ScriptContext();
/* 221 */       AppObjectRepository.putObject("DefaultScriptContext", scriptContext);
/* 222 */       DataResultSet drset = new DataResultSet();
/* 223 */       drset.init(res.getTable("ClientIdocScriptExtensions"));
/* 224 */       for (SimpleParameters props : drset.getSimpleParametersIterable())
/*     */       {
/* 226 */         String name = props.get("name");
/* 227 */         String location = props.get("class");
/* 228 */         ScriptExtensions extensions = (ScriptExtensions)ComponentClassFactory.createClassInstance(name, location, null);
/*     */ 
/* 230 */         extensions.load(scriptContext);
/*     */       }
/* 232 */       timer.trace("startup", "setup DefaultScriptContext");
/* 233 */       timer.stop();
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 237 */       throw new ServiceException(e, msg);
/*     */     }
/*     */ 
/* 240 */     m_baseUtils = utils;
/*     */   }
/*     */ 
/*     */   public static void parseAndAddResources(ResourceContainer rc, ClassLoader loader, String path)
/*     */     throws ServiceException
/*     */   {
/* 246 */     BufferedReader reader = ClassHelperUtils.createBufferedReaderForClassResource(loader, path);
/*     */     try
/*     */     {
/* 249 */       rc.parseAndAddResources(reader, path);
/*     */     }
/*     */     catch (IOException ioe)
/*     */     {
/* 253 */       throw new ServiceException(ioe, -1, "csErrorLoadingResourceFile", new Object[] { path });
/*     */     }
/*     */     catch (ParseSyntaxException pse)
/*     */     {
/* 257 */       throw new ServiceException(pse, -1, "csErrorLoadingResourceFile", new Object[] { path });
/*     */     }
/*     */   }
/*     */ 
/*     */   public static ClientUtils createUtils(Properties connectionData)
/*     */     throws ServiceException, DataException
/*     */   {
/* 264 */     if (m_baseUtils == null)
/*     */     {
/* 266 */       throw new ServiceException("AJK: createUtils() called without init.");
/*     */     }
/* 268 */     ClientUtils utils = m_baseUtils.clone();
/* 269 */     utils.initConnection(connectionData);
/* 270 */     return utils;
/*     */   }
/*     */ 
/*     */   public void initConnection(Properties connectionData) throws DataException
/*     */   {
/* 275 */     this.m_connectionData = connectionData;
/* 276 */     DataBinder binder = new DataBinder();
/* 277 */     binder.setLocalData(connectionData);
/* 278 */     updatePropertiesWithUrl(connectionData, connectionData.getProperty("url"));
/* 279 */     String[][] defaults = { { "ProviderName", "ServerConnection" }, { "ProviderDescription", "Server Connection" }, { "ProviderType", "outgoing" }, { "ProviderClass", "intradoc.provider.SocketOutgoingProvider" }, { "ProviderConnection", "intradoc.provider.SocketOutgoingConnection" } };
/*     */ 
/* 287 */     for (String[] defaultInfo : defaults)
/*     */     {
/* 289 */       if (binder.getLocal(defaultInfo[0]) != null)
/*     */         continue;
/* 291 */       binder.putLocal(defaultInfo[0], defaultInfo[1]);
/*     */     }
/*     */ 
/* 295 */     Provider provider = new Provider(binder);
/* 296 */     provider.markState("enabled");
/* 297 */     provider.init();
/* 298 */     Providers.addProvider(binder.getLocal("ProviderName"), provider);
/*     */ 
/* 300 */     this.m_outProvider = ((SocketOutgoingProvider)provider.getProvider());
/*     */   }
/*     */ 
/*     */   public void updatePropertiesWithUrl(Properties connectionData, String urltext)
/*     */     throws DataException
/*     */   {
/* 306 */     if (urltext == null)
/*     */     {
/* 308 */       return;
/*     */     }
/*     */     try
/*     */     {
/* 312 */       URLStreamHandler dummy = new URLStreamHandler()
/*     */       {
/*     */         public URLConnection openConnection(URL u)
/*     */         {
/* 317 */           throw new AssertionError("Unsupported method.");
/*     */         }
/*     */ 
/*     */         public URLConnection openConnection(URL u, Proxy p)
/*     */         {
/* 322 */           throw new AssertionError("Unsupported method.");
/*     */         }
/*     */       };
/* 326 */       URL spec = new URL(null, "idc://localhost:4444/", dummy);
/* 327 */       URL url = new URL(spec, urltext, dummy);
/* 328 */       if (!url.getProtocol().equals("idc"))
/*     */       {
/* 330 */         String msg = LocaleUtils.encodeMessage("csClientUrlTypeError", null);
/* 331 */         throw new MalformedURLException(msg);
/*     */       }
/*     */ 
/* 334 */       String host = url.getHost();
/* 335 */       connectionData.put("IntradocServerHostName", host);
/* 336 */       int port = url.getPort();
/* 337 */       connectionData.put("ServerPort", "" + port);
/* 338 */       String query = url.getQuery();
/* 339 */       while (query != null)
/*     */       {
/* 341 */         int index = query.indexOf("&");
/* 342 */         String data = query;
/* 343 */         if (index >= 0)
/*     */         {
/* 345 */           data = query.substring(0, index);
/* 346 */           query = query.substring(index + 1);
/*     */         }
/*     */         else
/*     */         {
/* 350 */           query = null;
/*     */         }
/* 352 */         int index2 = data.indexOf("=");
/* 353 */         String key = data;
/* 354 */         String value = "";
/* 355 */         if (index2 >= 0)
/*     */         {
/* 357 */           key = data.substring(0, index2);
/* 358 */           value = data.substring(index2 + 1);
/*     */         }
/* 360 */         connectionData.put(key, value);
/*     */       }
/*     */     }
/*     */     catch (MalformedURLException e)
/*     */     {
/* 365 */       throw new DataException(e, "csSubjectManagerInvalidUrl", new Object[] { urltext });
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataBinder testConnection(ExecutionContext context) throws ServiceException, DataException
/*     */   {
/* 371 */     DataBinder data = new DataBinder();
/* 372 */     this.m_outProvider.testConnection(data, context);
/* 373 */     return data;
/*     */   }
/*     */ 
/*     */   public DataBinder doRequest(String user, String service, DataBinder requestData, ExecutionContext context, ReportProgress report)
/*     */     throws DataException, ServiceException
/*     */   {
/* 380 */     if (requestData == null)
/*     */     {
/* 382 */       requestData = new DataBinder();
/*     */     }
/* 384 */     requestData.setEnvironmentValue("REMOTE_USER", user);
/* 385 */     requestData.putLocal("IdcService", service);
/* 386 */     return doRequest(requestData, context, report);
/*     */   }
/*     */ 
/*     */   public DataBinder doRequest(DataBinder requestData, ExecutionContext context, ReportProgress report)
/*     */     throws DataException, ServiceException
/*     */   {
/* 393 */     if (requestData == null)
/*     */     {
/* 395 */       requestData = new DataBinder();
/*     */     }
/* 397 */     DataBinder responseData = new DataBinder();
/* 398 */     ServerRequest sr = null;
/* 399 */     String isJava = null;
/*     */     try
/*     */     {
/* 402 */       isJava = requestData.getLocal("IsJava");
/* 403 */       requestData.putLocal("IsJava", "1");
/* 404 */       sr = ServerRequestUtils.createAdminProxyRequest(this.m_outProvider.getProvider(), requestData, context, report);
/*     */ 
/* 406 */       String remoteUser = requestData.getEnvironmentValue("REMOTE_USER");
/* 407 */       if (remoteUser != null)
/*     */       {
/* 409 */         requestData.setEnvironmentValue("PROXY_USER", remoteUser);
/*     */       }
/* 411 */       sr.doRequest(requestData, responseData, context);
/*     */     }
/*     */     finally
/*     */     {
/* 415 */       if (isJava != null)
/*     */       {
/* 417 */         requestData.putLocal("IsJava", isJava);
/*     */       }
/*     */       else
/*     */       {
/* 421 */         requestData.removeLocal("IsJava");
/*     */       }
/* 423 */       if ((sr != null) && (!responseData.m_isSuspended))
/*     */       {
/* 425 */         sr.closeRequest(context);
/*     */       }
/*     */     }
/* 428 */     return responseData;
/*     */   }
/*     */ 
/*     */   public ClientUtils clone()
/*     */   {
/* 434 */     ClientUtils utils = new ClientUtils();
/* 435 */     m_baseUtils.m_connectionData = this.m_connectionData;
/* 436 */     return utils;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 441 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 87113 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.client.ClientUtils
 * JD-Core Version:    0.5.4
 */