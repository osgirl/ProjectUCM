/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.gui.ComponentValidator;
/*     */ import java.io.BufferedReader;
/*     */ import java.io.IOException;
/*     */ import java.io.StringReader;
/*     */ import java.io.UnsupportedEncodingException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Map.Entry;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserUtils
/*     */ {
/*  38 */   public static String[] USER_FIELDS = { "umdName", "umdType" };
/*     */ 
/*  41 */   public static final String[] EXTENDED_USER_FIELD_SKIP_LIST = { "dName", "dPassword", "dPasswordEncoding", "dUserSourceFlags", "dUserOrgPath", "dUserSourceOrgPath" };
/*     */   public static final int CREATE_DOC_ACCOUNT = 1;
/*     */   public static final int ADD_ACCOUNT_TO_USER = 2;
/*     */ 
/*     */   public static UserData createUserData()
/*     */   {
/*  55 */     if (SharedObjects.getEnvValueAsBoolean("TrackUserDataObjects", false))
/*     */     {
/*  57 */       return new TrackedUserData(null, null);
/*     */     }
/*  59 */     return new UserData(null, null);
/*     */   }
/*     */ 
/*     */   public static UserData createUserData(String username)
/*     */   {
/*  64 */     if (SharedObjects.getEnvValueAsBoolean("TrackUserDataObjects", false))
/*     */     {
/*  66 */       return new TrackedUserData(username, null);
/*     */     }
/*  68 */     return new UserData(username, null);
/*     */   }
/*     */ 
/*     */   public static UserData createUserData(Properties props)
/*     */   {
/*  73 */     if (SharedObjects.getEnvValueAsBoolean("TrackUserDataObjects", false))
/*     */     {
/*  75 */       return new TrackedUserData(null, props);
/*     */     }
/*  77 */     return new UserData(null, props);
/*     */   }
/*     */ 
/*     */   public static boolean hasExternalUsers()
/*     */   {
/*  85 */     String hasExternalStr = SharedObjects.getEnvironmentValue("HasExternalUsers");
/*  86 */     if (hasExternalStr != null)
/*     */     {
/*  88 */       return StringUtils.convertToBool(hasExternalStr, false);
/*     */     }
/*  90 */     return true;
/*     */   }
/*     */ 
/*     */   public static void serializeAttribInfoNoError(DataBinder binder, UserData userData, boolean toBinder, boolean isForCache)
/*     */   {
/*     */     try
/*     */     {
/* 101 */       serializeAttribInfo(binder, userData, toBinder, isForCache);
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/* 105 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 107 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void serializeAttribInfo(DataBinder binder, UserData userData, boolean toBinder, boolean isForCache)
/*     */     throws ServiceException
/*     */   {
/* 118 */     ResultSet rset = binder.getResultSet("UserAttribInfo");
/* 119 */     DataResultSet drset = null;
/* 120 */     Vector v = null;
/*     */ 
/* 122 */     if ((!userData.m_hasAttributesLoaded) && (toBinder == true))
/*     */     {
/* 124 */       return;
/*     */     }
/*     */ 
/* 127 */     if (rset == null)
/*     */     {
/* 129 */       if (!toBinder)
/*     */       {
/* 131 */         return;
/*     */       }
/* 133 */       drset = new DataResultSet(new String[] { "dUserName", "AttributeInfo" });
/*     */     }
/* 138 */     else if (!toBinder)
/*     */     {
/* 140 */       drset = new DataResultSet();
/* 141 */       drset.copySimpleFiltered(rset, "dUserName", userData.m_name);
/* 142 */       if (drset.isEmpty())
/*     */       {
/* 144 */         return;
/*     */       }
/* 146 */       v = drset.getRowValues(0);
/*     */     }
/*     */     else
/*     */     {
/* 150 */       drset = (DataResultSet)rset;
/* 151 */       v = drset.findRow(0, userData.m_name);
/*     */     }
/*     */ 
/* 155 */     if ((!userData.m_hasAttributesLoaded) || (!toBinder))
/*     */     {
/* 157 */       userData.checkCreateAttributes(true);
/*     */     }
/*     */ 
/* 160 */     if (v == null)
/*     */     {
/* 162 */       v = drset.createEmptyRow();
/* 163 */       drset.addRow(v);
/* 164 */       v.setElementAt(userData.m_name, 0);
/*     */     }
/*     */ 
/* 169 */     FieldInfo[] fi = null;
/*     */     try
/*     */     {
/* 172 */       fi = ResultSetUtils.createInfoList(drset, new String[] { "dUserName", "AttributeInfo" }, true);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 177 */       throw new ServiceException(e);
/*     */     }
/*     */ 
/* 180 */     if (toBinder)
/*     */     {
/* 182 */       StringBuffer encodeStr = new StringBuffer();
/* 183 */       Map attributes = userData.getAttributesMap();
/* 184 */       Set entrySet = attributes.entrySet();
/* 185 */       for (Iterator entryList = entrySet.iterator(); entryList.hasNext(); )
/*     */       {
/* 187 */         Map.Entry entry = (Map.Entry)entryList.next();
/* 188 */         List uaiList = (List)entry.getValue();
/* 189 */         for (int i = 0; i < uaiList.size(); ++i)
/*     */         {
/* 191 */           UserAttribInfo uai = (UserAttribInfo)uaiList.get(i);
/*     */ 
/* 195 */           if (isForCache)
/*     */           {
/* 197 */             if (!uai.m_attribType.equalsIgnoreCase("account")) continue; if ((uai.m_attribPrivilege & 0x1) == 0)
/*     */             {
/*     */               continue;
/*     */             }
/*     */ 
/*     */           }
/*     */ 
/* 204 */           if (encodeStr.length() > 0)
/*     */           {
/* 206 */             encodeStr.append(",");
/*     */           }
/* 208 */           encodeStr.append(uai.m_attribType);
/* 209 */           encodeStr.append(",");
/* 210 */           encodeStr.append(uai.m_attribName);
/* 211 */           encodeStr.append(",");
/* 212 */           encodeStr.append(uai.m_attribPrivilege);
/*     */         }
/*     */       }
/* 215 */       v.setElementAt(encodeStr.toString(), fi[1].m_index);
/*     */ 
/* 217 */       binder.addResultSet("UserAttribInfo", drset);
/*     */     }
/*     */     else
/*     */     {
/* 221 */       String attribInfo = (String)v.elementAt(fi[1].m_index);
/*     */ 
/* 223 */       Vector strs = StringUtils.parseArray(attribInfo, ',', '^');
/* 224 */       int nstrs = strs.size();
/* 225 */       if (nstrs % 3 != 0)
/*     */       {
/* 227 */         throw new ServiceException("!apMalformedUserAttribInfo");
/*     */       }
/* 229 */       for (int i = 0; i < nstrs; i += 3)
/*     */       {
/* 231 */         userData.addAttribute((String)strs.elementAt(i), (String)strs.elementAt(i + 1), (String)strs.elementAt(i + 2));
/*     */       }
/*     */ 
/* 234 */       userData.m_hasAttributesLoaded = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object localizeUserValue(String key, String value, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 241 */     String[][] userData = (String[][])(String[][])cxt.getCachedObject("UserMetaDefinition");
/*     */ 
/* 244 */     if (userData == null)
/*     */     {
/*     */       try
/*     */       {
/* 248 */         DataResultSet userMeta = SharedObjects.getTable("UserMetaDefinition");
/* 249 */         userData = ResultSetUtils.createStringTable(userMeta, USER_FIELDS);
/* 250 */         cxt.setCachedObject("UserMetaDefinition", userData);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/* 254 */         Report.trace(null, "Error getting user meta definition.", e);
/* 255 */         throw new ServiceException(e);
/*     */       }
/*     */     }
/*     */ 
/* 259 */     for (int i = 0; i < userData.length; ++i)
/*     */     {
/* 261 */       if ((!userData[i][0].equals(key)) || (!userData[i][1].equalsIgnoreCase("date")))
/*     */         continue;
/* 263 */       Date d = LocaleUtils.parseODBC(value);
/* 264 */       return d;
/*     */     }
/*     */ 
/* 268 */     return value;
/*     */   }
/*     */ 
/*     */   public static Object getLocalizedUserValue(UserData data, String key, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 274 */     Object result = null;
/*     */ 
/* 276 */     if ((data != null) && (!key.equals("dPassword")))
/*     */     {
/* 278 */       String value = data.getProperty(key);
/* 279 */       if ((value != null) && (!value.equals("")))
/*     */       {
/* 281 */         result = localizeUserValue(key, value, cxt);
/*     */       }
/*     */       else
/*     */       {
/* 285 */         return value;
/*     */       }
/*     */     }
/* 288 */     return result;
/*     */   }
/*     */ 
/*     */   public static String createExtendedInfoString(UserData userData)
/*     */   {
/* 293 */     DataBinder extendedUserInfo = new DataBinder();
/* 294 */     Properties extendedData = extendedUserInfo.getLocalData();
/* 295 */     Properties userInfo = userData.getProperties();
/*     */ 
/* 297 */     for (Enumeration e = userInfo.keys(); e.hasMoreElements(); )
/*     */     {
/* 299 */       String key = (String)e.nextElement();
/* 300 */       if (StringUtils.findStringIndex(EXTENDED_USER_FIELD_SKIP_LIST, key) < 0)
/*     */       {
/* 302 */         extendedData.put(key, userInfo.get(key));
/*     */       }
/*     */     }
/* 305 */     IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 308 */       extendedUserInfo.sendEx(sw, false);
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 312 */       if (SystemUtils.m_verbose)
/*     */       {
/* 314 */         Report.debug(null, null, ignore);
/*     */       }
/*     */     }
/*     */ 
/* 318 */     return sw.toStringRelease();
/*     */   }
/*     */ 
/*     */   public static void unpackageExtendedInfo(String extendedInfo, DataBinder data)
/*     */   {
/* 323 */     if ((extendedInfo == null) || (extendedInfo.length() <= 0))
/*     */       return;
/* 325 */     StringReader sr = new StringReader(extendedInfo);
/*     */     try
/*     */     {
/* 328 */       data.receive(new BufferedReader(sr));
/*     */     }
/*     */     catch (IOException ignore)
/*     */     {
/* 332 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 334 */       Report.debug(null, null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String encodePassword(String user, String password, String encoding)
/*     */   {
/* 342 */     if ((encoding != null) && (encoding.equals("SHA1-CB")))
/*     */     {
/* 344 */       String dataStr = user + ":" + password;
/* 345 */       String systemEncoding = DataSerializeUtils.getSystemEncoding();
/* 346 */       byte[] dataBuf = null;
/*     */       try
/*     */       {
/* 349 */         if (systemEncoding != null)
/*     */         {
/* 351 */           dataBuf = dataStr.getBytes(systemEncoding);
/*     */         }
/*     */         else
/*     */         {
/* 355 */           dataBuf = dataStr.getBytes();
/*     */         }
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 360 */         String errMsg = LocaleUtils.encodeMessage("apPasswordGetByteError", null, user, systemEncoding);
/*     */ 
/* 362 */         Report.trace("system", errMsg, e);
/*     */       }
/* 364 */       if (dataBuf == null)
/*     */       {
/*     */         try
/*     */         {
/* 368 */           dataBuf = dataStr.getBytes("UTF8");
/*     */         }
/*     */         catch (UnsupportedEncodingException e)
/*     */         {
/* 372 */           String errMsg = LocaleUtils.encodeMessage("apPasswordGetByteError", null, user, "UTF8");
/*     */ 
/* 374 */           Report.trace("system", errMsg, e);
/* 375 */           dataBuf = dataStr.getBytes();
/*     */         }
/*     */       }
/*     */       try
/*     */       {
/* 380 */         password = CryptoCommonUtils.computeDigest(dataBuf, "SHA-1");
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 384 */         String errMsg = LocaleUtils.encodeMessage("apEncodePasswordError", null, user);
/* 385 */         Report.trace("system", errMsg, e);
/*     */       }
/*     */     }
/* 388 */     return password;
/*     */   }
/*     */ 
/*     */   public static boolean isUserDataEmpty(UserData userData)
/*     */   {
/* 398 */     if (userData == null)
/*     */     {
/* 400 */       return true;
/*     */     }
/* 402 */     String authType = userData.getProperty("dUserAuthType");
/* 403 */     return (authType == null) || (authType.length() == 0);
/*     */   }
/*     */ 
/*     */   public static boolean isValidAccountName(String docAccount, String[] errMsg, int options, ExecutionContext ctx)
/*     */   {
/* 420 */     if ((options != 1) && (options != 2))
/*     */     {
/* 422 */       Report.trace(null, "no option set for isValidAccountName, setting it to account creation.", null);
/*     */ 
/* 424 */       options = 1;
/*     */     }
/*     */ 
/* 428 */     boolean useCollaboration = SharedObjects.getEnvValueAsBoolean("UseCollaboration", false);
/*     */ 
/* 431 */     DataResultSet drset = SharedObjects.getTable("DocumentAccounts");
/* 432 */     ComponentValidator cmpVal = new ComponentValidator(drset);
/* 433 */     int maxLength = cmpVal.getMaxLength("dDocAccount", 30);
/*     */ 
/* 436 */     boolean isError = true;
/* 437 */     boolean isAppendMsg = true;
/* 438 */     String msg = LocaleUtils.encodeMessage("apFormatErrorInAccountName", null, docAccount);
/*     */ 
/* 441 */     if ((docAccount == null) || (docAccount.length() == 0))
/*     */     {
/* 443 */       msg = "!apEmptyAccountName";
/* 444 */       isAppendMsg = false;
/*     */     }
/* 447 */     else if ((docAccount.equalsIgnoreCase("prj")) && (((options == 1) || ((options == 2) && (!useCollaboration)))))
/*     */     {
/* 451 */       msg = LocaleUtils.encodeMessage("apReservedAccountName", null, "prj");
/* 452 */       isAppendMsg = false;
/*     */     }
/* 455 */     else if (docAccount.length() > maxLength)
/*     */     {
/* 457 */       msg = LocaleUtils.encodeMessage("apAccountNameExceedsMaxLength", null, docAccount, "" + maxLength);
/* 458 */       isAppendMsg = false;
/*     */     }
/* 461 */     else if (docAccount.indexOf(44) >= 0)
/*     */     {
/* 463 */       msg = LocaleUtils.encodeMessage("apInvalidCharsInAccountName", null, docAccount);
/*     */     }
/*     */     else
/*     */     {
/* 467 */       if ((options != 2) || ((!docAccount.equals("#all")) && (!docAccount.equals("#none"))))
/*     */       {
/* 470 */         docAccount = FileUtils.fileSlashes(docAccount);
/*     */       }
/*     */ 
/* 475 */       switch (Validation.checkUrlFilePathPart(docAccount))
/*     */       {
/*     */       case 0:
/* 478 */         isError = false;
/* 479 */         break;
/*     */       case -2:
/* 481 */         msg = LocaleUtils.encodeMessage("apSpacesInAccountName", null, docAccount);
/* 482 */         break;
/*     */       default:
/* 484 */         msg = LocaleUtils.encodeMessage("apInvalidCharsInAccountName", null, docAccount);
/* 485 */         break label289:
/*     */ 
/* 490 */         isError = false;
/*     */       }
/*     */     }
/*     */ 
/* 494 */     if (isError)
/*     */     {
/* 496 */       if (isAppendMsg)
/*     */       {
/* 498 */         label289: errMsg[0] = LocaleUtils.appendMessage("!apAccountErrorDesc", msg);
/* 499 */         errMsg[0] = LocaleResources.getString(errMsg[0], ctx);
/*     */       }
/*     */       else
/*     */       {
/* 503 */         errMsg[0] = msg;
/*     */       }
/*     */ 
/* 506 */       return false;
/*     */     }
/*     */ 
/* 510 */     return true;
/*     */   }
/*     */ 
/*     */   public static void getOrCreateCachedProfile(UserData userData, String key)
/*     */   {
/* 518 */     Object obj = SharedObjects.getObject(key, userData.m_name);
/* 519 */     if ((obj == null) || (!obj instanceof UserProfileData))
/*     */     {
/* 521 */       UserProfileData upData = userData.getUserProfile();
/* 522 */       SharedObjects.putObject(key, userData.m_name, upData);
/*     */     }
/*     */     else
/*     */     {
/* 526 */       UserProfileData upData = (UserProfileData)obj;
/* 527 */       userData.setUserProfile(upData);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected static String getLocalHostName()
/*     */   {
/* 535 */     String hostname = SharedObjects.getEnvironmentValue("ServerHostName");
/* 536 */     if (hostname == null)
/*     */     {
/* 538 */       hostname = "unknown";
/*     */       try
/*     */       {
/* 541 */         InetAddress addr = InetAddress.getLocalHost();
/* 542 */         hostname = addr.toString();
/* 543 */         hostname = addr.getCanonicalHostName();
/*     */       }
/*     */       catch (UnknownHostException e)
/*     */       {
/* 547 */         Report.trace(null, null, e);
/*     */       }
/* 549 */       SharedObjects.putEnvironmentValue("ServerHostName", hostname);
/*     */     }
/* 551 */     return hostname;
/*     */   }
/*     */ 
/*     */   public static UserData constructInternalSystemUser()
/*     */   {
/* 559 */     String hostname = getLocalHostName();
/* 560 */     UserData u = createUserData("@@internal-" + System.getProperty("user.name") + "@" + hostname);
/*     */ 
/* 562 */     fillInSystemUserData(u);
/*     */ 
/* 564 */     return u;
/*     */   }
/*     */ 
/*     */   public static UserData constructExternalSystemUser(String task, String remoteHost)
/*     */   {
/* 574 */     String hostname = getLocalHostName();
/* 575 */     UserData u = createUserData("@@remote-" + remoteHost + task + "@" + hostname);
/* 576 */     fillInSystemUserData(u);
/* 577 */     return u;
/*     */   }
/*     */ 
/*     */   protected static void fillInSystemUserData(UserData u)
/*     */   {
/* 584 */     u.setProperty("dPasswordEncoding", "*");
/* 585 */     u.setProperty("dPassword", "*");
/* 586 */     u.setProperty("dEmail", SharedObjects.getEnvironmentValue("SysAdminAddress"));
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 591 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 89332 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.UserUtils
 * JD-Core Version:    0.5.4
 */