/*     */ package intradoc.upload;
/*     */ 
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.server.ServiceSecurityImplementor;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.Arrays;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UploadSecurityImplementor extends ServiceSecurityImplementor
/*     */ {
/*  34 */   public String m_authDir = null;
/*  35 */   protected String m_authFile = "temp_auth_codes.hda";
/*  36 */   protected long m_timeout = 3600000L;
/*     */   protected boolean m_validatePassword;
/*     */   protected String m_userToValidate;
/*     */   protected String m_hashToValidate;
/*  43 */   public static final String[] AUTH_CODE_COLUMNS = { "Timestamp", "AuthCode", "UserInfo" };
/*     */ 
/*     */   public void globalSecurityCheck(Service service, DataBinder binder)
/*     */     throws ServiceException
/*     */   {
/*  57 */     super.globalSecurityCheck(service, binder);
/*     */ 
/*  60 */     String authCodeRequestID = binder.getLocal("AuthCodeRequestID");
/*  61 */     UserData userData = service.getUserData();
/*     */ 
/*  63 */     if (authCodeRequestID != null)
/*     */     {
/*  65 */       if ((userData.m_name == null) || (userData.m_name.equals("anonymous")))
/*     */       {
/*  67 */         boolean isAllowAnonymous = StringUtils.convertToBool(binder.getLocal("IsAllowAnonymous"), false);
/*  68 */         if (!isAllowAnonymous)
/*     */         {
/*  70 */           service.createServiceException(null, "!csActionRequiresLogin");
/*     */         }
/*  72 */         return;
/*     */       }
/*     */ 
/*  77 */       String authCode = CryptoCommonUtils.generateRandomStringOfSuggestedSize();
/*  78 */       binder.putLocal("TempAuthCode", authCode);
/*  79 */       String externalRoles = binder.getEnvironmentValue("EXTERNAL_ROLES");
/*  80 */       String externalAccounts = binder.getEnvironmentValue("EXTERNAL_ACCOUNTS");
/*  81 */       TempAuthData tAuthData = new TempAuthData(userData, authCodeRequestID, authCode, externalRoles, externalAccounts);
/*     */ 
/*  83 */       addAuthData(tAuthData);
/*  84 */       Report.trace("userstorage", "globalSecurityCheck - authCodeRequestID=" + authCodeRequestID + " authCode=" + authCode, null);
/*     */     }
/*     */ 
/*  88 */     if ((!this.m_validatePassword) || 
/*  90 */       (validateExplicitAuthUser(service, binder)))
/*     */       return;
/*  92 */     service.setPromptForLogin(true);
/*  93 */     service.createServiceExceptionEx(null, "!csSystemNeedsUserCredentials", -20);
/*     */   }
/*     */ 
/*     */   public String determineUser(Service service, DataBinder binder)
/*     */   {
/* 106 */     String curUser = null;
/*     */ 
/* 108 */     String authCode = binder.getLocal("UserTempAuth");
/* 109 */     if ((null != authCode) && (0 != authCode.length()))
/*     */     {
/*     */       try
/*     */       {
/* 115 */         String curUserInfo = checkAuthCode(authCode);
/* 116 */         List curInfoList = StringUtils.makeListFromSequence(curUserInfo, ' ', '%', 0);
/* 117 */         if (curInfoList.size() > 0)
/*     */         {
/* 119 */           curUser = (String)curInfoList.get(0);
/* 120 */           service.setConditionVar("SkipIdcTokenValidation", true);
/*     */         }
/* 122 */         if (curInfoList.size() >= 3)
/*     */         {
/* 124 */           String roles = (String)curInfoList.get(1);
/* 125 */           String accounts = (String)curInfoList.get(2);
/* 126 */           binder.setEnvironmentValue("EXTERNAL_ROLES", roles);
/* 127 */           binder.setEnvironmentValue("EXTERNAL_ACCOUNTS", accounts);
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 132 */         Report.trace(null, null, e);
/*     */       }
/*     */     }
/* 135 */     if (curUser == null)
/*     */     {
/* 137 */       curUser = computeExplicitAuthUser(service, binder);
/*     */     }
/* 139 */     if (curUser != null)
/*     */     {
/* 141 */       return curUser;
/*     */     }
/*     */ 
/* 144 */     return super.determineUser(service, binder);
/*     */   }
/*     */ 
/*     */   protected String computeExplicitAuthUser(Service service, DataBinder binder)
/*     */   {
/* 149 */     String userName = null;
/*     */ 
/* 151 */     if (SharedObjects.getEnvValueAsBoolean("MacIsAdminSockets", false))
/*     */     {
/* 153 */       String user = binder.getLocal("AppletUser");
/* 154 */       String hash = binder.getLocal("AppletAuth");
/* 155 */       if ((user != null) && (hash != null))
/*     */       {
/* 157 */         this.m_validatePassword = true;
/* 158 */         this.m_userToValidate = user;
/* 159 */         this.m_hashToValidate = hash;
/* 160 */         userName = user;
/*     */       }
/*     */     }
/* 163 */     return userName;
/*     */   }
/*     */ 
/*     */   protected boolean validateExplicitAuthUser(Service service, DataBinder binder)
/*     */   {
/* 168 */     UserData userData = service.getUserData();
/* 169 */     String passwd = userData.getProperty("dPassword");
/* 170 */     String mash = this.m_userToValidate + ":" + passwd + ":" + CryptoCommonUtils.getStartingRandomString();
/*     */ 
/* 172 */     byte[] dataBuf = null;
/* 173 */     boolean retVal = true;
/*     */     try
/*     */     {
/* 176 */       dataBuf = mash.getBytes(FileUtils.m_javaSystemEncoding);
/* 177 */       String realHash = CryptoCommonUtils.computeDigest(dataBuf, "SHA-1");
/* 178 */       retVal = this.m_hashToValidate.equals(realHash);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 182 */       Report.trace("system", null, e);
/* 183 */       retVal = false;
/*     */     }
/*     */ 
/* 186 */     return retVal;
/*     */   }
/*     */ 
/*     */   protected void addAuthData(TempAuthData tAuthData) throws ServiceException
/*     */   {
/* 191 */     String lookupCode = tAuthData.m_lookupCode;
/* 192 */     String userName = tAuthData.m_userData.m_name;
/*     */ 
/* 194 */     String dir = getAuthCodesDir();
/* 195 */     FileUtils.checkOrCreateDirectory(dir, 0);
/*     */ 
/* 197 */     FileUtils.reserveDirectory(dir);
/*     */     try
/*     */     {
/* 200 */       DataResultSet authCodes = loadAuthCodes();
/* 201 */       if (null == authCodes) {
/*     */         return;
/*     */       }
/*     */ 
/* 205 */       String roles = tAuthData.m_externalRoles;
/* 206 */       String userInfo = null;
/* 207 */       String[] userInfoArray = null;
/* 208 */       if ((roles != null) && (roles.length() > 0))
/*     */       {
/* 210 */         String accounts = tAuthData.m_externalAccounts;
/* 211 */         if (accounts == null)
/*     */         {
/* 213 */           accounts = "";
/*     */         }
/* 215 */         String[] list = { userName, roles, accounts };
/* 216 */         userInfoArray = list;
/*     */       }
/*     */       else
/*     */       {
/* 220 */         String[] list = { userName };
/* 221 */         userInfoArray = list;
/*     */       }
/* 223 */       List l = Arrays.asList(userInfoArray);
/* 224 */       userInfo = StringUtils.createString(l, ' ', '%');
/*     */ 
/* 226 */       pruneOldUserAuthCodes(authCodes, userInfo, lookupCode);
/*     */ 
/* 229 */       int timestampIndex = ResultSetUtils.getIndexMustExist(authCodes, "Timestamp");
/* 230 */       int authcodeIndex = ResultSetUtils.getIndexMustExist(authCodes, "AuthCode");
/* 231 */       int userIndex = ResultSetUtils.getIndexMustExist(authCodes, "UserInfo");
/*     */ 
/* 233 */       Vector newCode = authCodes.createEmptyRow();
/* 234 */       newCode.setElementAt("" + tAuthData.m_timeStamp, timestampIndex);
/* 235 */       newCode.setElementAt(lookupCode, authcodeIndex);
/* 236 */       Report.trace("userstorage", "addAuthData userInfo=" + userInfo + " timeStamp=" + tAuthData.m_timeStamp, null);
/* 237 */       newCode.setElementAt(userInfo, userIndex);
/* 238 */       authCodes.addRow(newCode);
/* 239 */       saveAuthCodes(authCodes);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 243 */       Report.trace(null, null, e);
/*     */     }
/*     */     finally {
/* 246 */       FileUtils.releaseDirectory(dir);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String getAuthCodesDir()
/*     */   {
/* 252 */     if (null == this.m_authDir)
/*     */     {
/* 254 */       this.m_authDir = (LegacyDirectoryLocator.getAppDataDirectory() + "session/");
/*     */     }
/* 256 */     return this.m_authDir;
/*     */   }
/*     */ 
/*     */   protected DataResultSet loadAuthCodes()
/*     */   {
/* 267 */     DataResultSet authCodes = null;
/*     */     try
/*     */     {
/* 270 */       String dir = getAuthCodesDir();
/* 271 */       DataBinder binder = new DataBinder();
/* 272 */       if (ResourceUtils.serializeDataBinder(dir, this.m_authFile, binder, false, false))
/*     */       {
/* 274 */         ResultSet rs = binder.getResultSet("AuthCodes");
/* 275 */         if (rs instanceof DataResultSet)
/*     */         {
/* 277 */           authCodes = (DataResultSet)rs;
/*     */         }
/*     */         else
/*     */         {
/* 281 */           authCodes = new DataResultSet();
/* 282 */           authCodes.copy(rs);
/*     */         }
/*     */ 
/* 286 */         for (String col : AUTH_CODE_COLUMNS)
/*     */         {
/* 288 */           if (authCodes.getFieldInfoIndex(col) >= 0)
/*     */             continue;
/* 290 */           authCodes = null;
/* 291 */           break;
/*     */         }
/*     */       }
/*     */ 
/* 295 */       if (authCodes == null)
/*     */       {
/* 297 */         authCodes = new DataResultSet(AUTH_CODE_COLUMNS);
/*     */       }
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 302 */       Report.trace("system", null, e);
/*     */     }
/*     */ 
/* 305 */     return authCodes;
/*     */   }
/*     */ 
/*     */   protected void saveAuthCodes(ResultSet authCodes)
/*     */     throws ServiceException
/*     */   {
/* 316 */     DataBinder binder = new DataBinder();
/* 317 */     binder.addResultSet("AuthCodes", authCodes);
/* 318 */     String dir = getAuthCodesDir();
/* 319 */     ResourceUtils.serializeDataBinder(dir, this.m_authFile, binder, true, false);
/*     */   }
/*     */ 
/*     */   protected void pruneOldUserAuthCodes(DataResultSet authCodes, String userName, String lookupCode)
/*     */     throws DataException
/*     */   {
/* 331 */     if ((null == authCodes) || (!authCodes.first()))
/*     */     {
/* 333 */       return;
/*     */     }
/*     */ 
/* 336 */     int authcodeIndex = ResultSetUtils.getIndexMustExist(authCodes, "AuthCode");
/* 337 */     int userIndex = ResultSetUtils.getIndexMustExist(authCodes, "UserInfo");
/* 338 */     int timestampIndex = ResultSetUtils.getIndexMustExist(authCodes, "Timestamp");
/* 339 */     String requestID = null;
/*     */ 
/* 341 */     if (null != lookupCode)
/*     */     {
/* 343 */       int i = lookupCode.indexOf(46);
/* 344 */       if (i > 0)
/*     */       {
/* 346 */         requestID = lookupCode.substring(0, i + 1);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 351 */     while (authCodes.isRowPresent())
/*     */     {
/* 353 */       if (!userName.equalsIgnoreCase(authCodes.getStringValue(userIndex)))
/*     */       {
/* 355 */         authCodes.next();
/*     */       }
/*     */ 
/* 358 */       String authCode = authCodes.getStringValue(authcodeIndex);
/* 359 */       if ((null != requestID) && (authCode.startsWith(requestID)))
/*     */       {
/* 361 */         String userInfo = authCodes.getStringValue(userIndex);
/* 362 */         String timestamp = authCodes.getStringValue(timestampIndex);
/* 363 */         Report.trace("userstorage", "pruneOldUserAuthCodes deletedMatchingOldAuthCode authCode=" + authCode + " userInfo=" + userInfo + " timestamp=" + timestamp, null);
/*     */ 
/* 365 */         authCodes.deleteCurrentRow();
/*     */       }
/*     */ 
/* 368 */       authCodes.next();
/*     */     }
/*     */ 
/* 373 */     int elementsToKeep = SharedObjects.getEnvironmentInt("MaxUserAuthCodes", 10);
/*     */ 
/* 375 */     if (!authCodes.first())
/*     */     {
/* 377 */       return;
/*     */     }
/* 379 */     while (authCodes.getNumRows() > elementsToKeep)
/*     */     {
/* 381 */       String userInfo = authCodes.getStringValue(userIndex);
/* 382 */       String timestamp = authCodes.getStringValue(timestampIndex);
/* 383 */       String authCode = authCodes.getStringValue(authcodeIndex);
/* 384 */       Report.trace("userstorage", "pruneOldUserAuthCodes deletedTooManyOldAuthCode authCode=" + authCode + " userInfo=" + userInfo + " timestamp=" + timestamp, null);
/*     */ 
/* 386 */       authCodes.deleteCurrentRow();
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String checkAuthCode(String authCode)
/*     */     throws DataException, ServiceException
/*     */   {
/* 399 */     String authUser = null;
/* 400 */     String dir = getAuthCodesDir();
/* 401 */     FileUtils.checkOrCreateDirectory(dir, 0);
/*     */ 
/* 403 */     FileUtils.reserveDirectory(dir);
/*     */     try {
/* 405 */       DataResultSet authCodes = loadAuthCodes();
/* 406 */       if ((null == authCodes) || (authCodes.isEmpty()) || (!authCodes.first()))
/*     */       {
/* 408 */         Report.trace("userstorage", "checkAuthCode no auth codes authCode=" + authCode, null);
/* 409 */         Object localObject1 = null;
/*     */         return localObject1;
/*     */       }
/*     */       int i;
/* 412 */       int i = 0;
/*     */ 
/* 414 */       long now = System.currentTimeMillis();
/* 415 */       int timestampIndex = ResultSetUtils.getIndexMustExist(authCodes, "Timestamp");
/* 416 */       int authcodeIndex = ResultSetUtils.getIndexMustExist(authCodes, "AuthCode");
/* 417 */       int userIndex = ResultSetUtils.getIndexMustExist(authCodes, "UserInfo");
/*     */ 
/* 419 */       while (authCodes.isRowPresent())
/*     */       {
/* 421 */         removeFlag = false;
/* 422 */         long timestamp = NumberUtils.parseLong(authCodes.getStringValue(timestampIndex), 0L);
/* 423 */         boolean isTimeout = false;
/* 424 */         if (now - timestamp > this.m_timeout)
/*     */         {
/* 426 */           isTimeout = true;
/* 427 */           removeFlag = true;
/*     */         }
/* 431 */         else if (authCodes.getStringValue(authcodeIndex).equals(authCode))
/*     */         {
/* 433 */           authUser = authCodes.getStringValue(userIndex);
/* 434 */           removeFlag = true;
/*     */         }
/*     */ 
/* 437 */         if (removeFlag)
/*     */         {
/* 439 */           String userInfo = authCodes.getStringValue(userIndex);
/* 440 */           String timestampRs = authCodes.getStringValue(timestampIndex);
/* 441 */           String authCodeRs = authCodes.getStringValue(authcodeIndex);
/* 442 */           Report.trace("userstorage", "checkAuthCode removing authCode=" + authCodeRs + " userInfo=" + userInfo + " timestamp=" + timestampRs + " isTimeout=" + isTimeout, null);
/*     */ 
/* 444 */           authCodes.deleteCurrentRow();
/* 445 */           i = 1;
/*     */         }
/*     */         else
/*     */         {
/* 449 */           authCodes.next();
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 454 */       if (i != 0)
/*     */       {
/* 456 */         saveAuthCodes(authCodes);
/*     */       }
/* 458 */       if (authUser == null)
/*     */       {
/* 460 */         Report.trace("userstorage", "checkAuthCode did not find matching user authCode=" + authCode, null);
/*     */       }
/* 462 */       boolean removeFlag = authUser;
/*     */ 
/* 465 */       return removeFlag; } finally { FileUtils.releaseDirectory(dir); }
/*     */ 
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 471 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 86038 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.upload.UploadSecurityImplementor
 * JD-Core Version:    0.5.4
 */