/*     */ package intradoc.autosuggest;
/*     */ 
/*     */ import intradoc.autosuggest.datastore.ContextInfoStorage;
/*     */ import intradoc.autosuggest.datastore.MetaStorage;
/*     */ import intradoc.autosuggest.records.ContextInfo;
/*     */ import intradoc.autosuggest.records.MetaInfo;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.util.List;
/*     */ 
/*     */ public class AutoSuggestContext
/*     */ {
/*     */   public static List<String> m_specialAuthGroups;
/*     */   public Workspace m_workspace;
/*     */   public Service m_service;
/*     */   public String m_contextKey;
/*     */   public String m_activeIndex;
/*     */ 
/*     */   public AutoSuggestContext(String context, Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  51 */     init(context, null, workspace);
/*     */   }
/*     */ 
/*     */   public AutoSuggestContext(String context, Service service, Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  63 */     init(context, service, workspace);
/*     */   }
/*     */ 
/*     */   public void init(String context, Service service, Workspace workspace)
/*     */     throws DataException, ServiceException
/*     */   {
/*  75 */     this.m_contextKey = context;
/*  76 */     this.m_contextKey = this.m_contextKey.toLowerCase();
/*  77 */     ContextInfo contextInfo = ContextInfoStorage.getContextInfo(this.m_contextKey);
/*  78 */     this.m_workspace = (((contextInfo != null) && (contextInfo.m_contextWorkspace != null)) ? contextInfo.m_contextWorkspace : workspace);
/*  79 */     DataBinder binder = new DataBinder();
/*  80 */     if (service != null)
/*     */     {
/*  82 */       binder.merge(service.getBinder());
/*  83 */       this.m_service = AutoSuggestManager.createDummyService(binder, this.m_workspace, service.getUserData());
/*     */     }
/*     */     else
/*     */     {
/*  87 */       this.m_service = AutoSuggestManager.createDummyService(new DataBinder(), this.m_workspace);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareActiveContext()
/*     */     throws DataException, ServiceException
/*     */   {
/*  97 */     MetaStorage metaStorage = new MetaStorage(this);
/*  98 */     MetaInfo metaInfo = metaStorage.get(this.m_contextKey);
/*  99 */     prepareActiveContext(metaInfo);
/*     */   }
/*     */ 
/*     */   public void prepareActiveContext(MetaInfo metaInfo)
/*     */     throws DataException, ServiceException
/*     */   {
/* 108 */     if ((metaInfo != null) && (metaInfo.m_activeIndex != null) && (metaInfo.m_activeIndex.length() > 0))
/*     */     {
/* 110 */       prepareActiveContext(metaInfo.getActiveIndex());
/*     */     }
/*     */     else
/*     */     {
/* 114 */       prepareActiveContext(AutoSuggestConstants.AUTO_SUGGEST_PRIMARY_INDEX);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void prepareActiveContext(String activeIndex)
/*     */     throws DataException, ServiceException
/*     */   {
/* 123 */     String baseContextKey = getBaseContextKey();
/* 124 */     this.m_activeIndex = activeIndex;
/* 125 */     this.m_contextKey = (this.m_activeIndex + "." + baseContextKey);
/*     */   }
/*     */ 
/*     */   public String getActiveIndex() {
/* 129 */     return this.m_activeIndex;
/*     */   }
/*     */ 
/*     */   public String getBaseContextKey()
/*     */   {
/* 137 */     String baseContext = this.m_contextKey;
/* 138 */     if ((this.m_activeIndex != null) && (this.m_contextKey.startsWith(this.m_activeIndex.toLowerCase())))
/*     */     {
/* 140 */       baseContext = this.m_contextKey.substring(this.m_activeIndex.length() + 1);
/*     */     }
/* 142 */     return baseContext;
/*     */   }
/*     */ 
/*     */   public static void initEnvironment()
/*     */   {
/* 149 */     String specialAuthGroupsStr = SharedObjects.getEnvironmentValue("SpecialAuthGroups");
/* 150 */     if ((specialAuthGroupsStr == null) || (specialAuthGroupsStr.length() <= 0))
/*     */       return;
/* 152 */     specialAuthGroupsStr = specialAuthGroupsStr.toLowerCase();
/* 153 */     m_specialAuthGroups = StringUtils.makeListFromSequenceSimple(specialAuthGroupsStr);
/*     */   }
/*     */ 
/*     */   public boolean lock()
/*     */     throws ServiceException
/*     */   {
/* 162 */     String baseContextKey = getBaseContextKey();
/* 163 */     String lockDir = AutoSuggestConstants.AUTO_SUGGEST_LOCK_DIR + "/" + baseContextKey;
/* 164 */     FileUtils.checkOrCreateDirectory(lockDir, 5);
/* 165 */     int defaultTimeout = (SystemUtils.m_isDevelopmentEnvironment) ? 1200 : 180000;
/* 166 */     int timeout = SharedObjects.getEnvironmentInt("AutoSuggestIndexTimeOut", defaultTimeout);
/* 167 */     timeout *= 60;
/* 168 */     boolean isLocked = FileUtils.reserveLongTermLock(lockDir, baseContextKey, "autosuggest", timeout, false);
/* 169 */     return isLocked;
/*     */   }
/*     */ 
/*     */   public void release()
/*     */   {
/* 176 */     String baseContextKey = getBaseContextKey();
/* 177 */     String lockDir = AutoSuggestConstants.AUTO_SUGGEST_LOCK_DIR + "/" + baseContextKey;
/* 178 */     FileUtils.releaseLongTermLock(lockDir, baseContextKey, "autosuggest");
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 183 */     return this.m_contextKey;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg) {
/* 187 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99166 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.autosuggest.AutoSuggestContext
 * JD-Core Version:    0.5.4
 */