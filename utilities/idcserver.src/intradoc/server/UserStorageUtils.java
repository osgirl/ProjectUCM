/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.RoleDefinitions;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.Users;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Map;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserStorageUtils
/*     */ {
/*     */   public static final int F_IS_ANONYMOUS = 1;
/*     */   public static final int F_GET_UNKNOWN_FROM_PROVIDER = 2;
/*     */   public static final int F_IS_PROXIED_REQUEST = 4;
/*     */   public static final int F_IS_PROXIED_USER = 8;
/*     */   public static final int F_ALLOW_LOAD_EXTERNAL_INFO = 16;
/*  48 */   public static final String[][] ATTRIB_ENV_VARS = { { "EXTERNAL_ROLES", "role" }, { "EXTERNAL_ACCOUNTS", "account" } };
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/*  54 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98020 $";
/*     */   }
/*     */ 
/*     */   public static UserData loadUserData(String user, DataBinder binder, Map curBinderParams, Workspace ws, ExecutionContext cxt, int flags)
/*     */     throws DataException, ServiceException
/*     */   {
/*  60 */     UserData userData = null;
/*  61 */     boolean getUnknownFromProvider = (flags & 0x2) != 0;
/*  62 */     if ((!getUnknownFromProvider) && 
/*  64 */       (binder.getEnvironmentValue(ATTRIB_ENV_VARS[0][0]) == null))
/*     */     {
/*  66 */       getUnknownFromProvider = true;
/*     */     }
/*     */ 
/*  69 */     boolean isAnonymous = (flags & 0x1) != 0;
/*  70 */     boolean hasExternalData = (!getUnknownFromProvider) && (!isAnonymous);
/*  71 */     boolean loadExternalData = hasExternalData;
/*  72 */     boolean isProxiedRequest = (flags & 0x4) != 0;
/*  73 */     boolean isProxiedUser = (flags & 0x8) != 0;
/*  74 */     boolean allowLoadExternalInfo = (flags & 0x10) != 0;
/*     */ 
/*  77 */     if (binder.getLocal("userExtendedInfo") != null)
/*     */     {
/*  79 */       binder.putLocal("userExtendedInfo", "");
/*     */     }
/*     */ 
/*  82 */     if (!isAnonymous)
/*     */     {
/*  86 */       if (ws != null)
/*     */       {
/*  88 */         userData = UserStorage.retrieveUserDatabaseProfileDataEx(user, ws, binder, cxt, true, getUnknownFromProvider);
/*     */       }
/*     */       else
/*     */       {
/*  93 */         Users userList = (Users)SharedObjects.getTable("Users");
/*  94 */         if (userList != null)
/*     */         {
/*  96 */           UserData listEntry = userList.getLocalUserData(user);
/*  97 */           if (listEntry != null)
/*     */           {
/*  99 */             userData = listEntry;
/*     */           }
/*     */         }
/* 102 */         if (userData == null)
/*     */         {
/* 104 */           userData = UserUtils.createUserData(user);
/* 105 */           userData.checkCreateAttributes(false);
/* 106 */           userData.m_hasAttributesLoaded = true;
/*     */         }
/*     */       }
/*     */     }
/* 110 */     if (userData != null)
/*     */     {
/* 113 */       curBinderParams.put("dUser", userData.m_name);
/*     */ 
/* 115 */       String authType = userData.getProperty("dUserAuthType");
/* 116 */       if (authType != null)
/*     */       {
/* 118 */         loadExternalData = (authType.equalsIgnoreCase("EXTERNAL")) || (SharedObjects.getEnvValueAsBoolean("AllowServerCredentialsOverride", false));
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 125 */       if (user == null)
/*     */       {
/* 127 */         user = "anonymous";
/*     */       }
/* 129 */       userData = UserUtils.createUserData(user);
/* 130 */       userData.checkCreateAttributes(false);
/* 131 */       userData.m_hasAttributesLoaded = true;
/* 132 */       UserUtils.getOrCreateCachedProfile(userData, "ProfileSystemUser");
/*     */     }
/*     */ 
/* 140 */     if ((hasExternalData) && (((loadExternalData) || (isProxiedRequest))))
/*     */     {
/* 145 */       Map originalAttributes = null;
/* 146 */       if (!loadExternalData)
/*     */       {
/* 149 */         originalAttributes = userData.getAttributesMap();
/* 150 */         userData.checkCreateAttributes(true);
/* 151 */         userData.m_hasAttributesLoaded = true;
/*     */       }
/*     */ 
/* 154 */       RoleDefinitions roleDefs = (RoleDefinitions)SharedObjects.getTable("RoleDefinition");
/*     */ 
/* 157 */       for (int i = 0; i < ATTRIB_ENV_VARS.length; ++i)
/*     */       {
/* 159 */         String attribName = ATTRIB_ENV_VARS[i][1];
/*     */ 
/* 165 */         if ((i == 0) && (originalAttributes != null) && (!isProxiedUser))
/*     */         {
/* 167 */           Vector v = (Vector)originalAttributes.get(attribName);
/* 168 */           if (v == null)
/*     */             continue;
/* 170 */           userData.putAttributes(attribName, v);
/*     */         }
/*     */         else
/*     */         {
/* 175 */           Vector attribs = new IdcVector();
/* 176 */           String key = ATTRIB_ENV_VARS[i][0];
/* 177 */           String externalAttribs = binder.getEnvironmentValue(key);
/* 178 */           if (externalAttribs != null)
/*     */           {
/* 180 */             attribs = StringUtils.parseArray(externalAttribs, ',', '^');
/*     */           }
/*     */ 
/* 185 */           if ((attribs.size() > 0) && (isAnonymous))
/*     */           {
/* 187 */             binder.setEnvironmentValue(key, "<AnonymousDoesNotAllow>");
/* 188 */             Report.trace("userstorage", "Anonymous users should not supply " + key + " header parameter as part of the request, the parameter is being ignored.", null);
/*     */           }
/*     */           else
/*     */           {
/* 193 */             SecurityUtils.loadExternalSecurityAttributes(userData, attribName, attribs, roleDefs, false);
/*     */           }
/*     */         }
/*     */       }
/* 196 */       if (SystemUtils.isActiveTrace("userstorage"))
/*     */       {
/* 198 */         String roles = SecurityUtils.getRolePackagedList(userData);
/* 199 */         String accounts = SecurityUtils.getFullExportedAccountslist(userData);
/* 200 */         Report.trace("userstorage", "Caller assigned Roles=" + roles + " Accounts=" + accounts + " for " + userData.m_name, null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 205 */     DataBinder newData = null;
/* 206 */     if ((loadExternalData) && (allowLoadExternalInfo))
/*     */     {
/* 208 */       newData = new DataBinder();
/* 209 */       Service service = (Service)cxt;
/*     */ 
/* 212 */       String extendedInfoEncoded = binder.getEnvironmentValue("EXTERNAL_EXTENDEDUSERINFO");
/* 213 */       if (extendedInfoEncoded == null)
/*     */       {
/* 216 */         extendedInfoEncoded = binder.getLocal("userExtendedInfo");
/*     */       }
/*     */ 
/* 220 */       if ((extendedInfoEncoded != null) && (extendedInfoEncoded.length() > 0))
/*     */       {
/* 223 */         String extendedInfo = StringUtils.decodeLiteralStringEscapeSequence(extendedInfoEncoded);
/* 224 */         UserUtils.unpackageExtendedInfo(extendedInfo, newData);
/*     */       }
/*     */ 
/* 230 */       newData.putLocal("dUserAuthType", "EXTERNAL");
/*     */ 
/* 233 */       String orgSource = binder.getAllowMissing("dUserSourceOrgPath");
/* 234 */       if (orgSource != null)
/*     */       {
/* 236 */         newData.putLocal("dUserSourceOrgPath", orgSource);
/*     */       }
/*     */       else
/*     */       {
/* 240 */         orgSource = binder.getEnvironmentValue("EXTERNAL_USERSOURCE");
/* 241 */         if (orgSource != null)
/*     */         {
/* 243 */           newData.putLocal("dUserSourceOrgPath", orgSource);
/*     */         }
/*     */       }
/*     */ 
/* 247 */       String orgPath = binder.getAllowMissing("dUserOrgPath");
/* 248 */       if (orgPath != null)
/*     */       {
/* 250 */         newData.putLocal("dUserOrgPath", orgPath);
/*     */       }
/*     */       else
/*     */       {
/* 254 */         orgPath = binder.getEnvironmentValue("EXTERNAL_USERORG");
/* 255 */         if (orgPath != null)
/*     */         {
/* 257 */           newData.putLocal("dUserOrgPath", orgPath);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 262 */       if (ws != null)
/*     */       {
/* 264 */         UserStorage.storeUserDatabaseProfileData(userData, newData, ws, service);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 269 */     if ((!isAnonymous) && (userData.getProperty("dUserAuthType") == null))
/*     */     {
/* 271 */       Report.trace("userstorage", "User " + userData.m_name + " improperly defined user not auth type determined, defaulting to EXTERNAL", null);
/*     */ 
/* 273 */       userData.setProperty("dUserAuthType", "EXTERNAL");
/*     */     }
/*     */ 
/* 276 */     if ((ws == null) || (isAnonymous))
/*     */     {
/* 280 */       cxt.setCachedObject("TargetUserData", userData);
/* 281 */       if ((loadExternalData) && (ws == null))
/*     */       {
/* 283 */         Object[] params = { newData };
/* 284 */         cxt.setCachedObject("afterLoadNoWorkspaceUserAttributesParams", params);
/*     */       }
/*     */     }
/*     */ 
/* 288 */     return userData;
/*     */   }
/*     */ 
/*     */   public static String getTenantIDForUser(UserData userData)
/*     */   {
/* 299 */     String tenantId = null;
/* 300 */     int indexOfSeparator = userData.m_name.indexOf(95);
/* 301 */     if (indexOfSeparator > 0)
/*     */     {
/* 303 */       tenantId = userData.m_name.substring(0, indexOfSeparator);
/*     */     }
/* 305 */     return tenantId;
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.UserStorageUtils
 * JD-Core Version:    0.5.4
 */