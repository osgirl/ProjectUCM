/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.data.WorkspaceUtils;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ 
/*     */ public class UserStorage
/*     */ {
/*  38 */   public static boolean m_isInit = false;
/*  39 */   public static int m_userAccessCount = 0;
/*  40 */   public static int m_maxUserAccessCount = 100;
/*  41 */   public static boolean[] m_synObject = new boolean[1];
/*  42 */   public static UserStorageImplementor m_userStorageImpl = null;
/*     */ 
/*     */   public static void checkInit() throws ServiceException
/*     */   {
/*  46 */     if (m_isInit)
/*     */       return;
/*  48 */     synchronized (m_synObject)
/*     */     {
/*  50 */       if (!m_isInit)
/*     */       {
/*  52 */         m_isInit = true;
/*     */ 
/*  54 */         if (m_userStorageImpl == null)
/*     */         {
/*  57 */           m_userStorageImpl = (UserStorageImplementor)ComponentClassFactory.createClassInstance("intradoc.server.UserStorageImplementor", "intradoc.server.UserStorageImplementor", "!csUserStorageImplementorError");
/*     */ 
/*  61 */           m_userStorageImpl.init();
/*     */         }
/*     */ 
/*  64 */         int maxUserThreadCount = SharedObjects.getEnvironmentInt("MaxUserAccessThreadCount", -1);
/*     */ 
/*  66 */         if (maxUserThreadCount < 0)
/*     */         {
/*  68 */           maxUserThreadCount = SharedObjects.getEnvironmentInt("MaxRequestThreadCount", m_maxUserAccessCount);
/*     */         }
/*     */ 
/*  71 */         m_maxUserAccessCount = maxUserThreadCount;
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static UserData retrieveUserDatabaseProfileData(String name, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*  84 */     return retrieveUserDatabaseProfileDataFull(name, ws, null, cxt, false, true);
/*     */   }
/*     */ 
/*     */   public static UserData retrieveUserDatabaseProfileDataFull(String name, Workspace ws, DataBinder credentialData, ExecutionContext cxt, boolean isLoadAttributes, boolean isGetUnknownFromProvider)
/*     */     throws DataException, ServiceException
/*     */   {
/*  91 */     boolean isOtherUser = credentialData == null;
/*     */ 
/*  93 */     if (isOtherUser)
/*     */     {
/*  95 */       credentialData = new DataBinder();
/*  96 */       credentialData.putLocal("getUserInfo", "1");
/*     */     }
/*     */ 
/*  99 */     UserData userData = retrieveUserDatabaseProfileDataEx(name, ws, credentialData, cxt, isLoadAttributes, isGetUnknownFromProvider);
/*     */ 
/* 108 */     if ((isOtherUser) && (!UserUtils.isUserDataEmpty(userData)))
/*     */     {
/* 110 */       String extendedUserInfo = credentialData.getLocal("userExtendedInfo");
/* 111 */       if (extendedUserInfo != null)
/*     */       {
/* 113 */         UserUtils.unpackageExtendedInfo(extendedUserInfo, credentialData);
/* 114 */         storeUserDatabaseProfileData(userData, credentialData, ws, cxt);
/*     */       }
/*     */     }
/*     */ 
/* 118 */     return userData;
/*     */   }
/*     */ 
/*     */   public static UserData retrieveUserDatabaseProfileDataEx(String name, Workspace ws, DataBinder credentialData, ExecutionContext cxt, boolean isLoadAttributes, boolean isGetUnknownFromProvider)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 134 */       synchronized (m_synObject)
/*     */       {
/* 136 */         m_userAccessCount += 1;
/* 137 */         if (SystemUtils.m_verbose)
/*     */         {
/* 139 */           m_userStorageImpl.printThreadMsg("At enter, user storage access count is " + m_userAccessCount);
/*     */         }
/*     */       }
/* 142 */       if (m_userAccessCount > m_maxUserAccessCount)
/*     */       {
/* 144 */         errMsg = LocaleUtils.encodeMessage("csUserTooManySimultaneousCredentialChecks", null, name);
/*     */ 
/* 148 */         synchronized (m_synObject)
/*     */         {
/* 150 */           int tooBusyCount = SharedObjects.getEnvironmentInt("ServerTooBusyCount", 0);
/* 151 */           ++tooBusyCount;
/* 152 */           SharedObjects.putEnvironmentValue("ServerTooBusyCount", Integer.toString(tooBusyCount));
/*     */         }
/*     */ 
/* 156 */         throw new ServiceException(errMsg);
/*     */       }
/* 158 */       checkInit();
/* 159 */       String errMsg = m_userStorageImpl.retrieveUserDatabaseProfileDataImplement(name, ws, credentialData, cxt, isLoadAttributes, isGetUnknownFromProvider);
/*     */ 
/* 171 */       return errMsg;
/*     */     }
/*     */     finally
/*     */     {
/* 164 */       synchronized (m_synObject)
/*     */       {
/* 166 */         m_userAccessCount -= 1;
/* 167 */         if (SystemUtils.m_verbose)
/*     */         {
/* 169 */           m_userStorageImpl.printThreadMsg("At exit, user storage access count is " + m_userAccessCount);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void storeUserDatabaseProfileData(UserData curData, DataBinder newData, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 189 */     if (curData == null)
/*     */     {
/* 191 */       String user = newData.getLocal("dName");
/* 192 */       curData = UserUtils.createUserData(user);
/*     */     }
/*     */ 
/* 196 */     String name = curData.m_name;
/* 197 */     if ((name == null) || (name.length() == 0) || (name.equalsIgnoreCase("anonymous")))
/*     */     {
/* 199 */       throw new DataException("!csAnonymousUserCannotSave");
/*     */     }
/*     */ 
/* 203 */     boolean copyAll = StringUtils.convertToBool(newData.getLocal("copyAll"), false);
/* 204 */     boolean doAdminFields = StringUtils.convertToBool(newData.getLocal("doAdminFields"), false);
/* 205 */     boolean alwaysSave = StringUtils.convertToBool(newData.getLocal("alwaysSave"), false);
/* 206 */     boolean userDataFromDb = StringUtils.convertToBool(curData.getProperty("userDataFromDb"), true);
/*     */ 
/* 208 */     checkInit();
/* 209 */     m_userStorageImpl.storeUserDatabaseProfileData(curData, newData, ws, cxt, copyAll, doAdminFields, alwaysSave, userDataFromDb);
/*     */   }
/*     */ 
/*     */   public static UserData getUserData(String name)
/*     */   {
/* 223 */     UserData userData = null;
/*     */     try
/*     */     {
/* 227 */       checkInit();
/* 228 */       if (m_userStorageImpl.m_userTempCache.isExternalLoadExpired(name))
/*     */       {
/* 230 */         Workspace ws = WorkspaceUtils.getWorkspace("system");
/* 231 */         userData = refreshCachedUserData(ws, name);
/*     */       }
/*     */       else
/*     */       {
/* 235 */         userData = m_userStorageImpl.getUserData(name);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 240 */       if (SystemUtils.m_verbose)
/*     */       {
/* 242 */         Report.debug(null, null, e);
/*     */       }
/*     */     }
/*     */ 
/* 246 */     return userData;
/*     */   }
/*     */ 
/*     */   public static void putCachedUserData(String name, UserData userData)
/*     */   {
/*     */     try
/*     */     {
/* 253 */       checkInit();
/* 254 */       m_userStorageImpl.m_userTempCache.putCachedUserData(name, userData);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 258 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 260 */       Report.debug(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void refreshCachedUserData()
/*     */   {
/* 274 */     m_userStorageImpl.m_userTempCache.m_lastExternalRefreshTime = System.currentTimeMillis();
/*     */   }
/*     */ 
/*     */   public static UserData refreshCachedUserData(Workspace ws, String name)
/*     */   {
/* 279 */     UserData data = null;
/*     */     try
/*     */     {
/* 283 */       data = m_userStorageImpl.m_userTempCache.getCachedUserData(name);
/* 284 */       if (data != null)
/*     */       {
/* 288 */         data.restoreAttributesFromExternal();
/* 289 */         ExecutionContext context = new ExecutionContextAdaptor();
/* 290 */         context.setCachedObject("TargetUserData", data);
/*     */ 
/* 292 */         PluginFilters.filter("updatingUserCache", ws, null, context);
/*     */ 
/* 294 */         m_userStorageImpl.m_userTempCache.replaceCachedUserData(name, data);
/*     */       }
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 299 */       if (SystemUtils.m_verbose)
/*     */       {
/* 301 */         Report.debug(null, null, e);
/*     */       }
/*     */     }
/* 304 */     return data;
/*     */   }
/*     */ 
/*     */   public static UserData getCachedUserData(String name)
/*     */   {
/* 309 */     UserData userData = null;
/*     */     try
/*     */     {
/* 313 */       checkInit();
/* 314 */       userData = m_userStorageImpl.m_userTempCache.getCachedUserData(name);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 318 */       if (SystemUtils.m_verbose)
/*     */       {
/* 320 */         Report.debug(null, null, e);
/*     */       }
/*     */     }
/*     */ 
/* 324 */     return userData;
/*     */   }
/*     */ 
/*     */   public static void removeCachedUserData(String name)
/*     */   {
/*     */     try
/*     */     {
/* 331 */       checkInit();
/* 332 */       m_userStorageImpl.m_userTempCache.removeCachedUserData(name);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 336 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 338 */       Report.debug(null, null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String createExtendedInfoString(UserData userData)
/*     */   {
/* 348 */     return UserUtils.createExtendedInfoString(userData);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static void unpackageExtendedInfo(String extendedInfo, DataBinder data)
/*     */   {
/* 356 */     UserUtils.unpackageExtendedInfo(extendedInfo, data);
/*     */   }
/*     */ 
/*     */   public static void synchronizeOptionLists(DataBinder binder, boolean hasNewData, boolean loadBinder)
/*     */     throws ServiceException
/*     */   {
/* 362 */     checkInit();
/* 363 */     m_userStorageImpl.synchronizeOptionLists(binder, hasNewData, loadBinder);
/*     */   }
/*     */ 
/*     */   public static void loadUserNameCache(Workspace ws)
/*     */     throws DataException
/*     */   {
/* 374 */     ResultSet rset = ws.createResultSet("QallUserNames", null);
/* 375 */     DataResultSet drset = new DataResultSet();
/* 376 */     drset.copy(rset);
/*     */ 
/* 378 */     SharedObjects.putTable("UserNames", drset);
/*     */   }
/*     */ 
/*     */   public static UserStorageImplementor getUserStorageImplementor()
/*     */   {
/* 383 */     UserStorageImplementor impl = null;
/*     */     try
/*     */     {
/* 386 */       checkInit();
/* 387 */       impl = m_userStorageImpl;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 391 */       if (SystemUtils.m_verbose)
/*     */       {
/* 393 */         Report.debug(null, null, e);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 398 */     return impl;
/*     */   }
/*     */ 
/*     */   public static void setUserStorageImplementor(UserStorageImplementor impl)
/*     */   {
/* 403 */     m_userStorageImpl = impl;
/*     */   }
/*     */ 
/*     */   public static boolean loadUserFromDatabase(String name, UserData userData, Workspace ws, boolean isGetUnknownFromProvider, UserStorageImplementorData vars)
/*     */     throws DataException
/*     */   {
/* 409 */     return m_userStorageImpl.loadUserFromDatabase(name, userData, ws, isGetUnknownFromProvider, vars);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 414 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 104244 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserStorage
 * JD-Core Version:    0.5.4
 */