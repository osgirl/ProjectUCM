/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.provider.OutgoingProvider;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.provider.ServerRequestUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SubjectEventMonitor;
/*     */ import intradoc.server.SubjectManager;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.BufferedWriter;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class OutgoingProviderManager
/*     */ {
/*  36 */   public static final String[] PROXIED_COLUMNS = { "psIDC_Name", "psHttpRelativeWebRoot", "psUserDB", "psIntradocServerHostName", "psIntradocServerPort", "psExportedRolesMap", "psAllowedExportedAccounts" };
/*     */ 
/*  40 */   protected DataResultSet m_proxiedServers = null;
/*     */ 
/*  42 */   protected Hashtable m_outgoingProviders = null;
/*  43 */   protected boolean m_isOnDemand = false;
/*     */ 
/*     */   public OutgoingProviderManager()
/*     */   {
/*  47 */     this.m_outgoingProviders = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(boolean isOnDemand, ExecutionContext ctxt)
/*     */   {
/*  56 */     DataResultSet proxiedServers = new DataResultSet(PROXIED_COLUMNS);
/*  57 */     this.m_isOnDemand = isOnDemand;
/*     */ 
/*  59 */     Vector providers = Providers.getProviderList();
/*  60 */     int num = providers.size();
/*  61 */     for (int i = 0; i < num; ++i)
/*     */     {
/*  63 */       Provider provider = (Provider)providers.elementAt(i);
/*     */ 
/*  65 */       DataBinder providerData = provider.getProviderData();
/*     */ 
/*  68 */       Object providerObj = provider.getProvider();
/*  69 */       if (!providerObj instanceof OutgoingProvider)
/*     */         continue;
/*  71 */       initOutgoingProvider(provider, providerData, proxiedServers, ctxt);
/*     */     }
/*     */ 
/*  75 */     this.m_proxiedServers = proxiedServers;
/*  76 */     SharedObjects.putTable("ProxiedServers", this.m_proxiedServers);
/*     */ 
/*  78 */     if (isOnDemand) {
/*     */       return;
/*     */     }
/*     */ 
/*  82 */     SharedObjects.removeTable("WebServerProxiedServers");
/*     */   }
/*     */ 
/*     */   protected void initOutgoingProvider(Provider provider, DataBinder providerData, DataResultSet proxiedServers, ExecutionContext ctxt)
/*     */   {
/*  89 */     boolean isNotifyTarget = StringUtils.convertToBool(providerData.getLocal("IsNotifyTarget"), false);
/*  90 */     boolean isProxiedServer = StringUtils.convertToBool(providerData.getLocal("IsProxiedServer"), false);
/*     */ 
/*  92 */     boolean isOK = true;
/*  93 */     String errMsg = "!csOutgoingProviderWillBeExcluded";
/*     */     try
/*     */     {
/*  96 */       if ((isProxiedServer) || (isNotifyTarget))
/*     */       {
/*  98 */         ProviderValidation.validateDefaults("outgoing", providerData);
/*  99 */         if (isNotifyTarget)
/*     */         {
/* 101 */           createNotifyTargetInfo(provider);
/*     */         }
/*     */ 
/* 104 */         if (isProxiedServer)
/*     */         {
/* 106 */           createProxiedServerInfo(provider, proxiedServers, ctxt);
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (DataException idcName)
/*     */     {
/*     */       String idcName;
/* 112 */       ProviderStateUtils.reportError(e, provider, errMsg);
/* 113 */       isOK = false;
/*     */     }
/*     */     catch (Exception idcName)
/*     */     {
/*     */       String idcName;
/* 117 */       isOK = ProviderStateUtils.handleRequestError(provider, e);
/* 118 */       ProviderStateUtils.reportError(e, provider, errMsg);
/*     */     }
/*     */     finally
/*     */     {
/*     */       String idcName;
/* 122 */       if (isOK)
/*     */       {
/* 124 */         String idcName = providerData.getLocal("IDC_Name");
/* 125 */         if (idcName != null)
/*     */         {
/* 127 */           this.m_outgoingProviders.put(idcName, provider);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createNotifyTargetInfo(Provider provider)
/*     */     throws DataException
/*     */   {
/* 143 */     DataBinder provData = provider.getProviderData();
/* 144 */     String str = provData.getLocal("NotifySubjects");
/* 145 */     if (str == null)
/*     */     {
/* 147 */       String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, "NotifySubjects");
/*     */ 
/* 149 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 152 */     Properties provState = provider.getProviderState();
/* 153 */     Vector subjects = StringUtils.parseArray(str, ',', ',');
/* 154 */     int num = subjects.size();
/* 155 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 157 */       String subject = (String)subjects.elementAt(i);
/* 158 */       SubjectEventMonitor monitor = new OutgoingProviderSubjectMonitor(provider);
/* 159 */       SubjectManager.addSubjectMonitor(subject, monitor);
/*     */ 
/* 163 */       provState.put(subject + ":isNotify", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void createProxiedServerInfo(Provider provider, DataResultSet proxiedServers, ExecutionContext ctxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 180 */     DataBinder provData = provider.getProviderData();
/* 181 */     Properties props = extractProxyData(provData);
/*     */ 
/* 184 */     String location = LegacyDirectoryLocator.getUserPublishCacheDir() + "proxied" + props.getProperty("psHttpRelativeWebRoot");
/*     */ 
/* 186 */     props.put("psUserDB", location + "userdb.txt");
/*     */ 
/* 189 */     PropParameters params = new PropParameters(props);
/* 190 */     Vector v = proxiedServers.createRow(params);
/* 191 */     proxiedServers.addRow(v);
/*     */ 
/* 193 */     if (this.m_isOnDemand)
/*     */       return;
/* 195 */     requestAndSaveSecurityInfo(provider, provData, location, ctxt);
/*     */   }
/*     */ 
/*     */   protected Properties extractProxyData(DataBinder provData)
/*     */     throws DataException
/*     */   {
/* 204 */     Properties props = new Properties();
/*     */ 
/* 206 */     int num = PROXIED_COLUMNS.length;
/* 207 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 210 */       props.put(PROXIED_COLUMNS[i], "");
/*     */     }
/*     */ 
/* 213 */     String[][] columnInfo = ProviderValidation.PROXY_COLUMNS;
/* 214 */     num = columnInfo.length;
/* 215 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 217 */       String key = columnInfo[i][0];
/* 218 */       String str = provData.getLocal(key);
/* 219 */       boolean isRequired = StringUtils.convertToBool(columnInfo[i][1], false);
/*     */ 
/* 221 */       if ((isRequired) && (((str == null) || (str.length() == 0))))
/*     */       {
/* 223 */         String msg = LocaleUtils.encodeMessage("csRequiredConfigFieldMissing", null, columnInfo[i][0]);
/*     */ 
/* 225 */         throw new DataException(msg);
/*     */       }
/*     */ 
/* 228 */       if (str == null)
/*     */         continue;
/* 230 */       props.put("ps" + key, str);
/*     */     }
/*     */ 
/* 233 */     return props;
/*     */   }
/*     */ 
/*     */   protected DataBinder requestAndSaveSecurityInfo(Provider provider, DataBinder provData, String location, ExecutionContext ctxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 243 */     DataBinder binder = requestSecurityInfo(provider, provData, ctxt);
/*     */ 
/* 246 */     ProviderStateUtils.checkReturnData(provider, binder);
/*     */ 
/* 248 */     DataBinder securityData = new DataBinder();
/* 249 */     String securityStr = binder.getLocal("SecurityInfo");
/* 250 */     if (securityStr == null)
/*     */     {
/* 252 */       throw new ServiceException("!csProviderUnknownErrorRetrievingSecurityInfo");
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 257 */       StringReader reader = new StringReader(securityStr);
/* 258 */       BufferedReader br = new BufferedReader(reader);
/* 259 */       securityData.receive(br);
/*     */ 
/* 262 */       FileUtils.checkOrCreateDirectoryPrepareForLocks(location, 2, true);
/*     */ 
/* 264 */       ResourceUtils.serializeDataBinder(location, "SecurityInfo.hda", securityData, true, false);
/*     */ 
/* 268 */       BufferedWriter bw = FileUtils.openDataWriter(location, "userdb.txt");
/* 269 */       bw.write("anonymous\t\tnull\tnull\tPublic");
/* 270 */       bw.close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/* 274 */       throw new ServiceException("!csProviderErrorSavingSecurityInfo", e);
/*     */     }
/* 276 */     return binder;
/*     */   }
/*     */ 
/*     */   protected DataBinder requestSecurityInfo(Provider provider, DataBinder provData, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 286 */     DataBinder inBinder = new DataBinder();
/* 287 */     inBinder.setLocalData(provData.getLocalData());
/*     */ 
/* 289 */     inBinder.putLocal("IdcService", "REQUEST_SECURITYINFO");
/*     */ 
/* 291 */     return doSecureRequest(provider, inBinder, cxt);
/*     */   }
/*     */ 
/*     */   public static DataBinder doSecureRequest(Provider provider, DataBinder requestData, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 297 */     Object providerObj = provider.getProvider();
/* 298 */     if (!providerObj instanceof OutgoingProvider)
/*     */     {
/* 300 */       throw new DataException("!csProviderNotOutgoing");
/*     */     }
/*     */ 
/* 303 */     DataBinder outBinder = new DataBinder();
/* 304 */     boolean isSuccess = true;
/*     */     try
/*     */     {
/* 307 */       ServerRequestUtils.doAdminProxyRequest(provider, requestData, outBinder, cxt);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 312 */       throw e;
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 317 */       throw e;
/*     */     }
/*     */     finally
/*     */     {
/* 321 */       ProviderStateUtils.updateProviderConnectionState(provider, isSuccess);
/*     */     }
/* 323 */     return outBinder;
/*     */   }
/*     */ 
/*     */   public Hashtable getOutgoingProviders()
/*     */   {
/* 331 */     return this.m_outgoingProviders;
/*     */   }
/*     */ 
/*     */   public Provider getOutgoingProvider(String name)
/*     */   {
/* 336 */     return (Provider)this.m_outgoingProviders.get(name);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 341 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.OutgoingProviderManager
 * JD-Core Version:    0.5.4
 */