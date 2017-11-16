/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class Users extends DataResultSet
/*     */ {
/*     */   public static final String m_localUsersQuery = "QlocalUsers";
/*     */   protected Vector m_localUsers;
/*     */   protected Hashtable m_localUserMap;
/*     */   protected boolean m_isAllLocalAttributesLoaded;
/*     */   public static final String m_passwordDash = "-----";
/*     */   protected String m_defaultPasswordEncoding;
/*     */ 
/*     */   public Users()
/*     */   {
/*  45 */     this.m_localUsers = new IdcVector();
/*  46 */     this.m_localUserMap = new Hashtable();
/*  47 */     this.m_isAllLocalAttributesLoaded = false;
/*     */ 
/*  49 */     String enc = SharedObjects.getEnvironmentValue("DefaultPasswordEncoding");
/*  50 */     if ((enc == null) || (enc.length() == 0))
/*     */     {
/*  52 */       enc = "SHA1-CB";
/*     */     }
/*  54 */     this.m_defaultPasswordEncoding = enc;
/*     */ 
/*  56 */     setDateFormat(LocaleResources.m_odbcFormat);
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  62 */     DataResultSet rset = new Users();
/*  63 */     initShallow(rset);
/*     */ 
/*  65 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  71 */     super.initShallow(rset);
/*  72 */     Users users = (Users)rset;
/*  73 */     users.m_dateFormat = new IdcDateFormat[1];
/*  74 */     users.setDateFormat(LocaleResources.m_odbcFormat);
/*  75 */     users.m_localUsers = this.m_localUsers;
/*  76 */     users.m_localUserMap = this.m_localUserMap;
/*  77 */     users.m_isAllLocalAttributesLoaded = this.m_isAllLocalAttributesLoaded;
/*     */ 
/*  79 */     users.m_defaultPasswordEncoding = this.m_defaultPasswordEncoding;
/*     */   }
/*     */ 
/*     */   public void load(ResultSet rset) throws DataException
/*     */   {
/*  84 */     this.m_isAllLocalAttributesLoaded = false;
/*     */ 
/*  86 */     int pswrdIndex = ResultSetUtils.getIndexMustExist(rset, "dPassword");
/*     */ 
/*  89 */     Vector users = new IdcVector();
/*  90 */     Hashtable userMap = new Hashtable();
/*     */ 
/*  92 */     IdcDateFormat tmp = rset.getDateFormat();
/*  93 */     rset.setDateFormat(getDateFormat());
/*     */     try
/*     */     {
/*  96 */       copy(rset);
/*     */     }
/*     */     finally
/*     */     {
/* 100 */       rset.setDateFormat(tmp);
/*     */     }
/*     */ 
/* 104 */     Object authors = new IdcVector();
/*     */ 
/* 107 */     for (int i = 0; isRowPresent(); ++i)
/*     */     {
/* 109 */       Properties props = getCurrentRowProps();
/* 110 */       UserData data = UserUtils.createUserData();
/* 111 */       data.setProperties(props);
/*     */ 
/* 113 */       String name = props.getProperty("dName");
/* 114 */       addOption(name, (Vector)authors, null);
/* 115 */       Vector row = getCurrentRowValues();
/*     */ 
/* 118 */       row.setElementAt("-----", pswrdIndex);
/*     */ 
/* 121 */       users.addElement(data);
/* 122 */       String lookupName = name.toUpperCase();
/* 123 */       userMap.put(lookupName, data);
/*     */ 
/* 107 */       next();
/*     */     }
/*     */ 
/* 127 */     this.m_localUsers = users;
/* 128 */     this.m_localUserMap = userMap;
/*     */ 
/* 130 */     SharedObjects.putOptList("docAuthors", (Vector)authors);
/*     */   }
/*     */ 
/*     */   protected void addOption(String value, Vector options, Hashtable optMap)
/*     */   {
/* 135 */     if ((value == null) || (value.length() == 0))
/*     */     {
/* 137 */       return;
/*     */     }
/*     */ 
/* 140 */     if (optMap != null)
/*     */     {
/* 142 */       if (optMap.get(value) != null)
/*     */       {
/* 145 */         return;
/*     */       }
/* 147 */       optMap.put(value, value);
/*     */     }
/*     */ 
/* 150 */     options.addElement(value);
/*     */   }
/*     */ 
/*     */   public void loadAllAttributes(ResultSet rset) throws DataException
/*     */   {
/* 155 */     int userNameIndex = ResultSetUtils.getIndexMustExist(rset, "dUserName");
/* 156 */     int attributeTypeIndex = ResultSetUtils.getIndexMustExist(rset, "dAttributeType");
/* 157 */     int attributeNameIndex = ResultSetUtils.getIndexMustExist(rset, "dAttributeName");
/* 158 */     int attributePrivilegeIndex = ResultSetUtils.getIndexMustExist(rset, "dAttributePrivilege");
/* 159 */     Vector oldUsers = this.m_localUsers;
/*     */ 
/* 165 */     int nusers = oldUsers.size();
/* 166 */     Vector users = new IdcVector();
/* 167 */     users.setSize(nusers);
/* 168 */     Hashtable userMap = new Hashtable();
/* 169 */     for (int i = 0; i < nusers; ++i)
/*     */     {
/* 171 */       UserData oldUserData = (UserData)oldUsers.elementAt(i);
/* 172 */       UserData userData = UserUtils.createUserData();
/* 173 */       userData.copyUserProfile(oldUserData);
/* 174 */       userData.checkCreateAttributes(true);
/* 175 */       userData.m_hasAttributesLoaded = true;
/* 176 */       userData.checkCreateAttributes(true);
/* 177 */       users.setElementAt(userData, i);
/* 178 */       userMap.put(userData.m_name.toUpperCase(), userData);
/*     */     }
/*     */ 
/* 182 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/* 184 */       String user = rset.getStringValue(userNameIndex);
/* 185 */       String type = rset.getStringValue(attributeTypeIndex);
/* 186 */       String name = rset.getStringValue(attributeNameIndex);
/* 187 */       String privilege = rset.getStringValue(attributePrivilegeIndex);
/*     */ 
/* 189 */       UserData userData = (UserData)userMap.get(user.toUpperCase());
/* 190 */       if (userData == null) continue; if (userData.getAttributesMap() == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 195 */       userData.addAttribute(type, name, privilege);
/*     */     }
/*     */ 
/* 202 */     this.m_localUsers = users;
/* 203 */     this.m_localUserMap = userMap;
/* 204 */     this.m_isAllLocalAttributesLoaded = true;
/*     */   }
/*     */ 
/*     */   public boolean checkLocalUserPassword(String user, String password)
/*     */   {
/* 214 */     String userKey = user.toUpperCase();
/* 215 */     UserData userData = (UserData)this.m_localUserMap.get(userKey);
/* 216 */     return checkUserPassword(userData, password);
/*     */   }
/*     */ 
/*     */   public boolean checkUserPassword(UserData userData, String password)
/*     */   {
/* 221 */     if ((userData == null) || (userData.m_name == null))
/*     */     {
/* 223 */       return false;
/*     */     }
/*     */ 
/* 226 */     String passwordEncoding = userData.getProperty("dPasswordEncoding");
/* 227 */     String cmpPswd = UserUtils.encodePassword(userData.m_name, password, passwordEncoding);
/* 228 */     String pswrd = userData.getProperty("dPassword");
/*     */ 
/* 231 */     return (pswrd != null) && (pswrd.equals(cmpPswd));
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public String encodePassword(String user, String password, String encoding)
/*     */   {
/* 244 */     return UserUtils.encodePassword(user, password, encoding);
/*     */   }
/*     */ 
/*     */   public Vector getLocalUsers()
/*     */   {
/* 249 */     return this.m_localUsers;
/*     */   }
/*     */ 
/*     */   public static String getPasswordDash()
/*     */   {
/* 254 */     return "-----";
/*     */   }
/*     */ 
/*     */   public String getLocalUsersQuery()
/*     */   {
/* 259 */     return "QlocalUsers";
/*     */   }
/*     */ 
/*     */   public boolean isAllLocalAttributesLoaded()
/*     */   {
/* 264 */     return this.m_isAllLocalAttributesLoaded;
/*     */   }
/*     */ 
/*     */   public String getDefaultPasswordEncoding()
/*     */   {
/* 269 */     return this.m_defaultPasswordEncoding;
/*     */   }
/*     */ 
/*     */   public UserData getLocalUserData(String name)
/*     */   {
/* 274 */     return (UserData)this.m_localUserMap.get(name.toUpperCase());
/*     */   }
/*     */ 
/*     */   public UserData getLocalUserData(int index)
/*     */   {
/* 279 */     return (UserData)this.m_localUsers.elementAt(index);
/*     */   }
/*     */ 
/*     */   public void setDateFormat(IdcDateFormat fmt)
/*     */   {
/* 285 */     super.setDateFormat(fmt);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public UserData getUserData(String name)
/*     */   {
/* 294 */     Report.trace(null, "intradoc.shared.getUserData(String name) is deprecated.", null);
/* 295 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public UserData getCachedUserData(String name)
/*     */   {
/* 304 */     Report.trace(null, "intradoc.shared.Users.getCachedUserData(String name) is deprecated.", null);
/* 305 */     return null;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void putCachedUserData(String name, UserData userData)
/*     */   {
/* 314 */     Report.trace(null, "intradoc.shared.Users.putCachedUserData(String name, UserData userData) is deprecated.", null);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public void removeCachedUserData(String name)
/*     */   {
/* 323 */     Report.trace(null, "intradoc.shared.Users.removeCachedUserData(String name) is deprecated.", null);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 328 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 93365 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.Users
 * JD-Core Version:    0.5.4
 */