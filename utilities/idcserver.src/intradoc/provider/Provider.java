/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.server.utils.ServerInstallUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class Provider
/*     */ {
/*  35 */   protected DataBinder m_providerData = null;
/*     */ 
/*  37 */   protected Properties m_providerState = null;
/*     */ 
/*  39 */   protected Hashtable m_providerObjects = null;
/*     */ 
/*  42 */   protected ProviderInterface m_provider = null;
/*     */ 
/*  44 */   protected ProviderConfig m_providerConfig = null;
/*     */ 
/*     */   public Provider(DataBinder prData)
/*     */   {
/*  48 */     this.m_providerData = prData;
/*  49 */     this.m_providerState = new Properties();
/*  50 */     this.m_providerObjects = new Hashtable();
/*     */   }
/*     */ 
/*     */   public Provider(DataBinder prData, Map providerObjects)
/*     */   {
/*  55 */     this.m_providerData = prData;
/*  56 */     this.m_providerState = new Properties();
/*  57 */     this.m_providerObjects = new Hashtable();
/*  58 */     if (providerObjects == null)
/*     */       return;
/*  60 */     this.m_providerObjects.putAll(providerObjects);
/*     */   }
/*     */ 
/*     */   public void init()
/*     */     throws DataException
/*     */   {
/*  69 */     this.m_provider = ((ProviderInterface)createClass("ProviderClass", null));
/*  70 */     if ((this.m_provider == null) || (!isEnabled()))
/*     */       return;
/*  72 */     this.m_provider.init(this);
/*  73 */     this.m_providerState.put("IsInitialized", "1");
/*     */   }
/*     */ 
/*     */   public void startProvider(boolean isOnDemand)
/*     */     throws DataException, ServiceException
/*     */   {
/*  79 */     this.m_providerState.put("IsOnDemand", String.valueOf(isOnDemand));
/*  80 */     startProvider();
/*     */   }
/*     */ 
/*     */   public void startProvider() throws DataException, ServiceException
/*     */   {
/*  85 */     if ((this.m_provider == null) || (!isEnabled()))
/*     */       return;
/*  87 */     this.m_provider.startProvider();
/*  88 */     markState("started");
/*  89 */     markState("ready");
/*     */   }
/*     */ 
/*     */   public String getReportString(String key)
/*     */   {
/*  95 */     if (this.m_provider != null)
/*     */     {
/*  97 */       return this.m_provider.getReportString(key);
/*     */     }
/*  99 */     return "";
/*     */   }
/*     */ 
/*     */   public Object createClass(String className, String defClass)
/*     */     throws DataException
/*     */   {
/* 105 */     String providerClassName = this.m_providerData.getLocal(className);
/* 106 */     Object obj = null;
/*     */     try
/*     */     {
/* 109 */       if ((providerClassName == null) || (providerClassName.length() == 0))
/*     */       {
/* 111 */         providerClassName = defClass;
/*     */       }
/*     */ 
/* 114 */       if (providerClassName == null)
/*     */       {
/* 116 */         throw new DataException("!csProviderClassNotDefined");
/*     */       }
/*     */ 
/* 119 */       obj = ComponentClassFactory.createClassInstance(providerClassName, providerClassName, "");
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 123 */       String providerName = this.m_providerData.getLocal("ProviderName");
/* 124 */       String errMsg = LocaleUtils.encodeMessage("csProviderClassLoadError", null, providerName);
/*     */ 
/* 127 */       throw new DataException(errMsg, e);
/*     */     }
/* 129 */     return obj;
/*     */   }
/*     */ 
/*     */   public void configureProvider() throws DataException, ServiceException
/*     */   {
/* 134 */     if (this.m_providerConfig == null)
/*     */     {
/* 136 */       this.m_providerConfig = this.m_provider.createProviderConfig();
/*     */     }
/*     */ 
/* 139 */     if (this.m_providerConfig == null)
/*     */       return;
/* 141 */     this.m_providerConfig.init(this);
/*     */   }
/*     */ 
/*     */   public void releaseConnection()
/*     */   {
/* 147 */     if (this.m_provider == null)
/*     */       return;
/* 149 */     this.m_provider.releaseConnection();
/*     */   }
/*     */ 
/*     */   public void stopProvider()
/*     */   {
/* 155 */     stopProvider(true);
/*     */   }
/*     */ 
/*     */   public void stopProvider(boolean isClear)
/*     */   {
/* 160 */     if (isClear)
/*     */     {
/* 163 */       this.m_providerState.clear();
/*     */     }
/*     */ 
/* 166 */     if ((this.m_provider == null) || (!isEnabled()))
/*     */       return;
/* 168 */     this.m_provider.stopProvider();
/*     */   }
/*     */ 
/*     */   public boolean isProviderOfType(String type)
/*     */   {
/* 174 */     String provType = this.m_providerData.getLocal("ProviderType");
/* 175 */     return (provType != null) && (provType.indexOf(type) >= 0);
/*     */   }
/*     */ 
/*     */   public void pollConnectionState()
/*     */   {
/* 183 */     String state = "!csProviderStateProviderNotAvailable";
/* 184 */     boolean isInit = checkState("IsInitialized", false);
/* 185 */     boolean isNew = StringUtils.convertToBool(this.m_providerState.getProperty("IsNew"), false);
/* 186 */     boolean isEnabled = StringUtils.convertToBool(this.m_providerState.getProperty("IsEnabled"), true);
/* 187 */     boolean isRemoved = checkState("IsRemoved", false);
/* 188 */     if (isNew)
/*     */     {
/* 190 */       state = "!csProviderStateNew";
/*     */     }
/* 192 */     else if (isRemoved)
/*     */     {
/* 194 */       state = "!csProviderStateDeleted";
/*     */     }
/* 196 */     else if (isEnabled)
/*     */     {
/* 198 */       if ((this.m_provider != null) && (isInit))
/*     */       {
/* 200 */         this.m_providerState.remove("ConnectionState");
/* 201 */         this.m_provider.pollConnectionState(this.m_providerData, this.m_providerState);
/*     */ 
/* 203 */         state = this.m_providerState.getProperty("ConnectionState");
/* 204 */         if (state == null)
/*     */         {
/* 208 */           boolean isBadConnection = StringUtils.convertToBool(this.m_providerState.getProperty("IsBadConnection"), false);
/*     */ 
/* 210 */           if (isBadConnection)
/*     */           {
/* 212 */             int errCode = NumberUtils.parseInteger(this.m_providerState.getProperty("LastConnectionErrorCode"), -1);
/*     */ 
/* 214 */             switch (errCode)
/*     */             {
/*     */             case -26:
/* 231 */               state = "!csProviderStateMisconfigured";
/* 232 */               break;
/*     */             case -25:
/* 235 */               state = "!csProviderStateProviderNotAvailable";
/* 236 */               break;
/*     */             default:
/* 239 */               state = "!csProviderStateDown";
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 244 */             state = "!csProviderStateGood";
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else {
/* 251 */       state = "!csProviderStateDisabled";
/*     */     }
/* 253 */     this.m_providerState.put("ConnectionState", state);
/*     */   }
/*     */ 
/*     */   public void markErrorState(int errCode, Exception e)
/*     */   {
/* 258 */     this.m_providerState.put("IsBadConnection", "1");
/* 259 */     this.m_providerState.put("LastConnectionErrorCode", String.valueOf(errCode));
/* 260 */     IdcMessage msg = IdcMessageFactory.lc(e);
/* 261 */     this.m_providerState.put("LastConnectionErrorMsg", LocaleUtils.encodeMessage(msg));
/*     */   }
/*     */ 
/*     */   public void markState(String state)
/*     */   {
/* 266 */     if (state.equals("active"))
/*     */     {
/* 268 */       this.m_providerState.put("LastActivityTs", String.valueOf(System.currentTimeMillis()));
/* 269 */       this.m_providerState.put("IsUsed", "1");
/*     */ 
/* 271 */       this.m_providerState.remove("IsBadConnection");
/* 272 */       this.m_providerState.remove("LastConnectionErrorCode");
/* 273 */       this.m_providerState.remove("LastConnectionErrorMsg");
/* 274 */       this.m_providerState.remove("RetryCount");
/*     */     }
/* 276 */     else if ((state.equals("editAdd")) && (ServerInstallUtils.isCatalogServer()))
/*     */     {
/* 278 */       this.m_providerState.put("IsEnabled", "1");
/*     */     }
/* 280 */     else if (state.startsWith("edit"))
/*     */     {
/* 282 */       this.m_providerState.put("RequiresRestart", "1");
/* 283 */       this.m_providerState.put("IsEdited", "1");
/* 284 */       if (state.equals("editAdd"))
/*     */       {
/* 286 */         this.m_providerState.put("IsNew", "1");
/*     */       } else {
/* 288 */         if (!state.equals("editRemove"))
/*     */           return;
/* 290 */         this.m_providerState.put("IsRemoved", "1");
/*     */       }
/*     */     }
/* 293 */     else if (state.equals("enabled"))
/*     */     {
/* 295 */       this.m_providerState.put("IsEnabled", "1");
/*     */     }
/* 297 */     else if (state.equals("disabled"))
/*     */     {
/* 299 */       this.m_providerState.put("IsEnabled", "0");
/*     */     }
/* 301 */     else if (state.equals("ready"))
/*     */     {
/* 303 */       this.m_providerState.put("IsReady", "1");
/*     */     } else {
/* 305 */       if (!state.equals("started"))
/*     */         return;
/* 307 */       this.m_providerState.put("IsStarted", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void resetState()
/*     */   {
/* 313 */     this.m_providerState.clear();
/* 314 */     this.m_providerState.put("IsInitialized", "1");
/*     */   }
/*     */ 
/*     */   public boolean isInError()
/*     */   {
/* 319 */     boolean isBad = StringUtils.convertToBool(this.m_providerState.getProperty("IsBadConnection"), false);
/* 320 */     if (isBad)
/*     */     {
/* 322 */       int errCode = NumberUtils.parseInteger(this.m_providerState.getProperty("LastConnectionErrorCode"), 0);
/* 323 */       switch (errCode)
/*     */       {
/*     */       case -26:
/* 326 */         return true;
/*     */       }
/*     */     }
/*     */ 
/* 330 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean checkState(String key, boolean dflt)
/*     */   {
/* 335 */     return StringUtils.convertToBool(this.m_providerState.getProperty(key), dflt);
/*     */   }
/*     */ 
/*     */   public boolean isEnabled()
/*     */   {
/* 340 */     return StringUtils.convertToBool(this.m_providerState.getProperty("IsEnabled"), true);
/*     */   }
/*     */ 
/*     */   public void setEnabled(boolean isEnabled)
/*     */   {
/* 345 */     this.m_providerData.putLocal("IsEnabled", String.valueOf(isEnabled));
/*     */   }
/*     */ 
/*     */   public boolean isSystemProvider()
/*     */   {
/* 350 */     return StringUtils.convertToBool(this.m_providerData.getLocal("IsSystemProvider"), false);
/*     */   }
/*     */ 
/*     */   public Object getProvider()
/*     */   {
/* 358 */     return this.m_provider;
/*     */   }
/*     */ 
/*     */   public void setProvider(ProviderInterface provider)
/*     */   {
/* 363 */     this.m_provider = provider;
/*     */   }
/*     */ 
/*     */   public DataBinder getProviderData()
/*     */   {
/* 368 */     return this.m_providerData;
/*     */   }
/*     */ 
/*     */   public void setProviderData(DataBinder data)
/*     */   {
/* 373 */     if (data == null)
/*     */       return;
/* 375 */     this.m_providerData = data;
/*     */   }
/*     */ 
/*     */   public ProviderConfig getProviderConfig()
/*     */   {
/* 381 */     return this.m_providerConfig;
/*     */   }
/*     */ 
/*     */   public Properties getProviderState()
/*     */   {
/* 386 */     return this.m_providerState;
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 391 */     return this.m_providerData.getLocal("ProviderName");
/*     */   }
/*     */ 
/*     */   public String getLocation()
/*     */   {
/* 396 */     return this.m_providerData.getLocal("ProviderLocation");
/*     */   }
/*     */ 
/*     */   public Object getProviderObject(String name)
/*     */   {
/* 401 */     return this.m_providerObjects.get(name);
/*     */   }
/*     */ 
/*     */   public void addProviderObject(String name, Object obj)
/*     */   {
/* 406 */     this.m_providerObjects.put(name, obj);
/*     */   }
/*     */ 
/*     */   public Hashtable getProviderObjects()
/*     */   {
/* 411 */     return this.m_providerObjects;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 417 */     return getName();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 422 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97679 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.Provider
 * JD-Core Version:    0.5.4
 */