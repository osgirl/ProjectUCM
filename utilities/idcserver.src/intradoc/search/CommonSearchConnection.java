/*     */ package intradoc.search;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.provider.ProviderConnectionManager;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.SearchConnection;
/*     */ import intradoc.server.SearchIndexerUtils;
/*     */ import intradoc.shared.CommonSearchConfig;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import java.io.IOException;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CommonSearchConnection
/*     */   implements SearchConnection
/*     */ {
/*     */   public ProviderConnectionManager m_manager;
/*     */   public DataBinder m_connectionData;
/*     */   public CommonSearchConfig m_queryConfig;
/*     */   public ExecutionContext m_ctxt;
/*     */   public Vector m_collections;
/*     */   public Vector m_attachedColls;
/*     */   public boolean m_isPrepared;
/*     */   public boolean m_isAttached;
/*     */   public boolean m_openEachTime;
/*     */   public boolean m_isBadConnection;
/*     */   public long m_timeStampLastOpen;
/*     */   public boolean m_isCollectionInited;
/*     */   protected int m_debugWaitConnection;
/*     */   public String m_sharedDir;
/*     */   public String m_searchDir;
/*     */   protected SearchImplementor m_connection;
/*     */   protected String m_id;
/*     */ 
/*     */   public CommonSearchConnection()
/*     */   {
/*  33 */     this.m_manager = null;
/*  34 */     this.m_connectionData = null;
/*     */ 
/*  37 */     this.m_ctxt = null;
/*     */ 
/*  39 */     this.m_collections = null;
/*  40 */     this.m_attachedColls = null;
/*     */ 
/*  42 */     this.m_isPrepared = false;
/*  43 */     this.m_isAttached = false;
/*  44 */     this.m_openEachTime = false;
/*  45 */     this.m_isBadConnection = false;
/*  46 */     this.m_timeStampLastOpen = 0L;
/*  47 */     this.m_isCollectionInited = false;
/*     */ 
/*  50 */     this.m_debugWaitConnection = 0;
/*     */ 
/*  56 */     this.m_connection = null;
/*  57 */     this.m_id = null;
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager manager, DataBinder data) throws DataException {
/*  61 */     init(manager, data, null, null, 0, null);
/*     */   }
/*     */ 
/*     */   public void init(ProviderConnectionManager manager, DataBinder data, String defaultClass, Object rawCon, int flags, Map params)
/*     */     throws DataException
/*     */   {
/*  72 */     this.m_manager = manager;
/*  73 */     this.m_connectionData = data;
/*     */ 
/*  75 */     this.m_isPrepared = false;
/*  76 */     this.m_openEachTime = DataBinderUtils.getBoolean(data, "CloseSearchConnections", false);
/*  77 */     if (!this.m_openEachTime)
/*     */     {
/*  79 */       this.m_openEachTime = (!DataBinderUtils.getBoolean(data, "IsPersistSearchConnection", true));
/*     */     }
/*  81 */     this.m_debugWaitConnection = DataBinderUtils.getInteger(data, "SearchDebugWaitConnectInMillis", 0);
/*  82 */     this.m_sharedDir = LegacyDirectoryLocator.getSharedDirectory();
/*  83 */     this.m_searchDir = LegacyDirectoryLocator.getSearchDirectory();
/*     */     try
/*     */     {
/*  87 */       if (rawCon != null)
/*     */       {
/*  89 */         this.m_connection = ((SearchImplementor)rawCon);
/*     */       }
/*     */       else
/*     */       {
/*  93 */         this.m_connection = ((SearchImplementor)ComponentClassFactory.createClassInstance("SearchImplementor", defaultClass, "csUnableCreateSearchImplementor"));
/*     */ 
/*  95 */         this.m_connection.init(this);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 101 */       Report.trace("search", null, e);
/* 102 */       return;
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object getRawConnection()
/*     */   {
/* 108 */     return null;
/*     */   }
/*     */ 
/*     */   public void prepareUse()
/*     */   {
/* 113 */     prepareUse(null);
/*     */   }
/*     */ 
/*     */   public void prepareUse(ExecutionContext ctxt)
/*     */   {
/* 118 */     this.m_ctxt = ctxt;
/* 119 */     this.m_queryConfig = SearchIndexerUtils.retrieveSearchConfig(ctxt);
/*     */ 
/* 121 */     if (!this.m_isPrepared)
/*     */     {
/* 123 */       this.m_isPrepared = this.m_connection.prepareUse(ctxt);
/*     */     }
/*     */ 
/* 126 */     if (!this.m_isPrepared)
/*     */       return;
/* 128 */     this.m_timeStampLastOpen = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public void initCollection(Vector collection)
/*     */   {
/* 135 */     if (this.m_isCollectionInited)
/*     */       return;
/* 137 */     if (this.m_debugWaitConnection > 0)
/*     */     {
/*     */       try
/*     */       {
/* 141 */         Thread.sleep(this.m_debugWaitConnection);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 145 */         Report.trace(null, null, t);
/*     */       }
/*     */     }
/* 148 */     this.m_isCollectionInited = this.m_connection.initCollection(collection);
/*     */   }
/*     */ 
/*     */   public String doQuery(DataBinder binder)
/*     */   {
/* 154 */     if (!this.m_isPrepared)
/*     */     {
/* 156 */       return createErrorMsg(null, "csCmnSrchIndxEngineNotPrepared");
/*     */     }
/* 158 */     return this.m_connection.doQuery(binder);
/*     */   }
/*     */ 
/*     */   public String retrieveHighlightInfo(DataBinder binder, int hlType, String hlBegin, String hlEnd)
/*     */   {
/* 164 */     return this.m_connection.retrieveHighlightInfo(binder, hlType, hlBegin, hlEnd);
/*     */   }
/*     */ 
/*     */   public String viewDoc(DataBinder binder, int viewType)
/*     */   {
/* 169 */     return this.m_connection.viewDoc(binder, viewType);
/*     */   }
/*     */ 
/*     */   public String getResult()
/*     */   {
/* 174 */     return this.m_connection.getResult();
/*     */   }
/*     */ 
/*     */   public DataBinder getResultAsBinder()
/*     */   {
/* 179 */     return this.m_connection.getResultAsBinder();
/*     */   }
/*     */ 
/*     */   public String retrieveDocInfo(String docKey, String fields, int numFields)
/*     */   {
/* 184 */     return this.m_connection.retrieveDocInfo(docKey, fields, numFields);
/*     */   }
/*     */ 
/*     */   public synchronized void reset()
/*     */   {
/* 190 */     if ((this.m_openEachTime) || (this.m_isBadConnection))
/*     */     {
/* 192 */       closeSession();
/*     */     }
/* 194 */     this.m_isBadConnection = false;
/*     */   }
/*     */ 
/*     */   public void closeSession()
/*     */   {
/* 202 */     if (this.m_isPrepared)
/*     */     {
/* 204 */       this.m_isPrepared = false;
/* 205 */       this.m_isCollectionInited = false;
/* 206 */       this.m_timeStampLastOpen = 0L;
/* 207 */       this.m_connection.closeSession();
/*     */     }
/* 209 */     this.m_isBadConnection = false;
/*     */   }
/*     */ 
/*     */   public Object getConnection()
/*     */   {
/* 214 */     return this.m_connection;
/*     */   }
/*     */ 
/*     */   public void setId(String id)
/*     */   {
/* 219 */     this.m_id = id;
/*     */   }
/*     */ 
/*     */   public String getId()
/*     */   {
/* 224 */     return this.m_id;
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 230 */     if (this.m_isPrepared)
/*     */     {
/* 232 */       closeSession();
/*     */     }
/* 234 */     this.m_connection = null;
/*     */   }
/*     */ 
/*     */   public void setIsBadConnection(boolean isBadConnection)
/*     */   {
/* 239 */     this.m_isBadConnection = isBadConnection;
/*     */   }
/*     */ 
/*     */   public boolean isBadConnection()
/*     */   {
/* 244 */     return this.m_isBadConnection;
/*     */   }
/*     */ 
/*     */   public long getTimeStampLastOpen()
/*     */   {
/* 249 */     return this.m_timeStampLastOpen;
/*     */   }
/*     */ 
/*     */   public static String createErrorMsg(Exception e, String msg)
/*     */   {
/* 254 */     DataBinder binder = new DataBinder();
/* 255 */     binder.putLocal("StatusCode", "-1");
/*     */ 
/* 257 */     if (e != null)
/*     */     {
/* 259 */       Report.trace("search", null, e);
/*     */     }
/* 261 */     String statusMsg = LocaleUtils.encodeMessage(msg, null);
/* 262 */     binder.putLocal("StatusMessageKey", statusMsg);
/* 263 */     binder.putLocal("StatusMessage", statusMsg);
/* 264 */     binder.putLocal("ExceptionMessage", (e != null) ? e.getMessage() : "");
/*     */ 
/* 266 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 269 */       binder.send(sw);
/* 270 */       return sw.toStringRelease();
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 275 */       Report.trace("search", null, ignore);
/* 276 */     }return "";
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 286 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.search.CommonSearchConnection
 * JD-Core Version:    0.5.4
 */