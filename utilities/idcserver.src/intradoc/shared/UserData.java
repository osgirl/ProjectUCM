/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class UserData
/*     */ {
/*  32 */   protected Properties m_props = null;
/*     */ 
/*  35 */   public String m_name = "";
/*     */ 
/*  37 */   public String m_defaultAccount = "";
/*  38 */   public boolean m_hasAttributesLoaded = false;
/*  39 */   public boolean m_isExpired = false;
/*     */ 
/*  45 */   public Hashtable m_attributes = null;
/*     */ 
/*  47 */   public Hashtable m_externalAttributes = null;
/*     */ 
/*  49 */   public final int ATTRIBUTE_TYPE_INDEX = 0;
/*  50 */   public final int ATTRIBUTE_NAME_INDEX = 1;
/*  51 */   public final int ATTRIBUTE_PRIVILEGE_INDEX = 2;
/*  52 */   public final int NUM_ATTRIBUTE_FIELDS = 3;
/*     */ 
/*  56 */   public static String[][] m_specialAccountTranslations = { { "#none", "!apDocumentsWithoutAccounts" }, { "#all", "!apAllAccounts" } };
/*     */ 
/*  63 */   protected UserProfileData m_profileData = null;
/*     */ 
/*     */   @Deprecated
/*     */   public UserData()
/*     */   {
/*  70 */     this.m_name = "";
/*     */ 
/*  72 */     this.m_props = new Properties();
/*  73 */     this.m_props.put("dName", "");
/*  74 */     this.m_props.put("dPasswordEncoding", "OpenText");
/*     */ 
/*  76 */     this.m_defaultAccount = "";
/*  77 */     this.m_hasAttributesLoaded = false;
/*  78 */     this.m_attributes = null;
/*  79 */     this.m_externalAttributes = null;
/*     */ 
/*  81 */     this.m_profileData = new UserProfileData();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public UserData(String name)
/*     */   {
/*  88 */     this.m_props = new Properties();
/*  89 */     this.m_props.put("dName", name);
/*  90 */     this.m_props.put("dFullName", "");
/*  91 */     this.m_props.put("dPassword", "");
/*  92 */     this.m_props.put("dPasswordEncoding", "");
/*  93 */     this.m_props.put("dEmail", "");
/*     */ 
/*  95 */     this.m_name = name;
/*     */ 
/*  97 */     this.m_profileData = new UserProfileData();
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public UserData(Properties props)
/*     */   {
/* 104 */     setProperties(props);
/* 105 */     this.m_profileData = new UserProfileData();
/*     */   }
/*     */ 
/*     */   protected UserData(String name, Properties props)
/*     */   {
/* 110 */     if (name == null)
/*     */     {
/* 112 */       name = "";
/*     */     }
/* 114 */     if (props == null)
/*     */     {
/* 116 */       props = new Properties();
/* 117 */       props.put("dName", name);
/* 118 */       props.put("dFullName", "");
/* 119 */       props.put("dPassword", "");
/* 120 */       props.put("dPasswordEncoding", "");
/* 121 */       props.put("dEmail", "");
/*     */     }
/* 123 */     setProperties(props);
/* 124 */     this.m_profileData = new UserProfileData();
/*     */   }
/*     */ 
/*     */   public void setProperties(Properties props)
/*     */   {
/* 129 */     this.m_props = props;
/*     */ 
/* 131 */     this.m_name = props.getProperty("dName");
/*     */   }
/*     */ 
/*     */   public Properties getProperties()
/*     */   {
/* 136 */     return this.m_props;
/*     */   }
/*     */ 
/*     */   public Map getAttributesMap()
/*     */   {
/* 141 */     return this.m_attributes;
/*     */   }
/*     */ 
/*     */   public Map getExternalAttributesMap()
/*     */   {
/* 146 */     return this.m_externalAttributes;
/*     */   }
/*     */ 
/*     */   public void checkCreateAttributes(boolean forceRecreation)
/*     */   {
/* 151 */     if ((this.m_attributes == null) || (forceRecreation))
/*     */     {
/* 153 */       this.m_attributes = new Hashtable();
/*     */     }
/* 155 */     if ((this.m_externalAttributes != null) && (!forceRecreation))
/*     */       return;
/* 157 */     this.m_externalAttributes = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void copyUserProfile(UserData source)
/*     */   {
/* 166 */     this.m_props = ((Properties)source.m_props.clone());
/*     */ 
/* 173 */     this.m_profileData = source.m_profileData;
/*     */ 
/* 175 */     this.m_name = source.m_name;
/*     */   }
/*     */ 
/*     */   public void copyAttributesReference(UserData source)
/*     */   {
/* 183 */     this.m_attributes = source.m_attributes;
/* 184 */     this.m_externalAttributes = source.m_externalAttributes;
/*     */   }
/*     */ 
/*     */   public void copyAttributes(UserData source)
/*     */   {
/* 192 */     if ((source != null) && (!source.m_hasAttributesLoaded))
/*     */     {
/* 194 */       return;
/*     */     }
/*     */ 
/* 197 */     this.m_attributes = new Hashtable();
/*     */ 
/* 199 */     if (source != null)
/*     */     {
/* 202 */       for (Enumeration e = source.m_attributes.keys(); e.hasMoreElements(); )
/*     */       {
/* 204 */         String key = (String)e.nextElement();
/* 205 */         Vector uaiList = (Vector)source.m_attributes.get(key);
/* 206 */         Vector newuaiList = new IdcVector();
/* 207 */         newuaiList.setSize(uaiList.size());
/*     */ 
/* 209 */         for (int i = 0; i < uaiList.size(); ++i)
/*     */         {
/* 211 */           UserAttribInfo uai = (UserAttribInfo)uaiList.elementAt(i);
/* 212 */           UserAttribInfo newuai = new UserAttribInfo();
/* 213 */           newuai.m_attribType = uai.m_attribType;
/* 214 */           newuai.m_attribName = uai.m_attribName;
/* 215 */           newuai.m_attribPrivilege = uai.m_attribPrivilege;
/* 216 */           newuaiList.setElementAt(newuai, i);
/* 217 */           if (!uai.m_attribType.equals("defacct"))
/*     */             continue;
/* 219 */           this.m_defaultAccount = uai.m_attribName;
/*     */         }
/*     */ 
/* 223 */         this.m_attributes.put(key, newuaiList);
/*     */       }
/* 225 */       copyExternalAttributes(source);
/*     */     }
/*     */ 
/* 228 */     this.m_hasAttributesLoaded = true;
/*     */   }
/*     */ 
/*     */   public void copyExternalAttributes(UserData source)
/*     */   {
/* 233 */     if ((source != null) && (source.m_externalAttributes == null))
/*     */     {
/* 235 */       return;
/*     */     }
/*     */ 
/* 238 */     this.m_externalAttributes = new Hashtable();
/*     */ 
/* 240 */     if (source == null) {
/*     */       return;
/*     */     }
/* 243 */     for (Enumeration e = source.m_externalAttributes.keys(); e.hasMoreElements(); )
/*     */     {
/* 245 */       String key = (String)e.nextElement();
/* 246 */       Vector uaiList = (Vector)source.m_externalAttributes.get(key);
/* 247 */       Vector newuaiList = new IdcVector();
/* 248 */       newuaiList.setSize(uaiList.size());
/*     */ 
/* 250 */       for (int i = 0; i < uaiList.size(); ++i)
/*     */       {
/* 252 */         UserAttribInfo uai = (UserAttribInfo)uaiList.elementAt(i);
/* 253 */         UserAttribInfo newuai = new UserAttribInfo();
/* 254 */         newuai.m_attribType = uai.m_attribType;
/* 255 */         newuai.m_attribName = uai.m_attribName;
/* 256 */         newuai.m_attribPrivilege = uai.m_attribPrivilege;
/* 257 */         newuaiList.setElementAt(newuai, i);
/*     */       }
/*     */ 
/* 260 */       this.m_externalAttributes.put(key, newuaiList);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void setAttributes(String[][] attribList)
/*     */   {
/* 268 */     this.m_attributes = new Hashtable();
/* 269 */     this.m_hasAttributesLoaded = true;
/* 270 */     if ((attribList == null) || (attribList.length == 0) || (attribList[0].length < 3))
/*     */     {
/* 272 */       return;
/*     */     }
/*     */ 
/* 275 */     for (int i = 0; i < attribList.length; ++i)
/*     */     {
/* 277 */       addAttribute(attribList[i][0], attribList[i][1], attribList[i][2]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void addAttribute(String type, String name, String priv)
/*     */   {
/* 285 */     UserAttribInfo uai = null;
/*     */ 
/* 287 */     Vector v = getAttributes(type);
/* 288 */     if (v == null)
/*     */     {
/* 290 */       v = new IdcVector();
/* 291 */       putAttributes(type, v);
/*     */     }
/*     */ 
/* 296 */     int nattribs = v.size();
/* 297 */     for (int i = 0; i < nattribs; ++i)
/*     */     {
/* 299 */       UserAttribInfo uaiTemp = (UserAttribInfo)v.elementAt(i);
/* 300 */       if (!uaiTemp.m_attribName.equalsIgnoreCase(name))
/*     */         continue;
/* 302 */       uai = uaiTemp;
/* 303 */       break;
/*     */     }
/*     */ 
/* 307 */     boolean isNew = false;
/* 308 */     if (uai == null)
/*     */     {
/* 310 */       isNew = true;
/* 311 */       uai = new UserAttribInfo();
/* 312 */       uai.m_attribType = type;
/* 313 */       v.addElement(uai);
/*     */     }
/*     */ 
/* 316 */     uai.m_attribName = name;
/* 317 */     if ((priv != null) && (priv.length() > 0))
/*     */     {
/* 319 */       if (isNew)
/*     */       {
/* 321 */         uai.m_attribPrivilege = 0;
/*     */       }
/* 323 */       uai.m_attribPrivilege |= Integer.parseInt(priv);
/*     */     }
/*     */ 
/* 327 */     if (!type.equals("defacct"))
/*     */       return;
/* 329 */     this.m_defaultAccount = name;
/*     */   }
/*     */ 
/*     */   public boolean removeAttribute(String type, String name)
/*     */   {
/* 335 */     Vector v = getAttributes(type);
/* 336 */     if (v == null)
/*     */     {
/* 338 */       return false;
/*     */     }
/* 340 */     int nattribs = v.size();
/*     */ 
/* 342 */     for (int i = 0; i < nattribs; ++i)
/*     */     {
/* 344 */       UserAttribInfo uaiTemp = (UserAttribInfo)v.elementAt(i);
/* 345 */       if (!uaiTemp.m_attribName.equalsIgnoreCase(name))
/*     */         continue;
/* 347 */       v.removeElementAt(i);
/* 348 */       return true;
/*     */     }
/*     */ 
/* 352 */     return false;
/*     */   }
/*     */ 
/*     */   public void setDefaultAccount(String defAccount)
/*     */   {
/* 357 */     removeAttributes("defacct");
/* 358 */     if (defAccount == null)
/*     */       return;
/* 360 */     addAttribute("defacct", defAccount, "15");
/*     */   }
/*     */ 
/*     */   public String getAccountPresentationString(String accountInternal)
/*     */   {
/* 366 */     return getAccountPresentationString(accountInternal, null);
/*     */   }
/*     */ 
/*     */   public String getAccountPresentationString(String accountInternal, ExecutionContext cxt)
/*     */   {
/* 371 */     String str = StringUtils.getPresentationString(m_specialAccountTranslations, accountInternal);
/*     */ 
/* 373 */     if (str == null)
/*     */     {
/* 376 */       str = accountInternal;
/*     */     }
/* 378 */     if (str.startsWith("!"))
/*     */     {
/* 380 */       str = LocaleResources.localizeMessage(str, cxt);
/*     */     }
/* 382 */     return str;
/*     */   }
/*     */ 
/*     */   public String getAccountInternalString(String accountPresentation, ExecutionContext cxt)
/*     */   {
/* 391 */     int len = m_specialAccountTranslations.length;
/* 392 */     String[][] localizedTranslations = new String[len][];
/* 393 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 395 */       localizedTranslations[i] = new String[2];
/* 396 */       localizedTranslations[i][0] = m_specialAccountTranslations[i][0];
/* 397 */       localizedTranslations[i][1] = LocaleResources.localizeMessage(m_specialAccountTranslations[i][1], cxt);
/*     */     }
/* 399 */     return StringUtils.getInternalString(localizedTranslations, accountPresentation);
/*     */   }
/*     */ 
/*     */   public void addSpecialAccountsChoices(Vector choices)
/*     */   {
/* 404 */     for (int i = 0; i < m_specialAccountTranslations.length; ++i)
/*     */     {
/* 406 */       choices.addElement(m_specialAccountTranslations[i][0]);
/*     */     }
/*     */   }
/*     */ 
/*     */   public TopicInfo getTopicInfo(String topic)
/*     */   {
/* 415 */     return this.m_profileData.getTopic(topic);
/*     */   }
/*     */ 
/*     */   public void updateTopics(DataBinder binder)
/*     */   {
/*     */     try
/*     */     {
/* 422 */       ResultSet rset = binder.getResultSet("UpdatedUserTopics");
/* 423 */       this.m_profileData.updateTopics(rset);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 427 */       Report.trace(null, "Unable to update the user " + this.m_name + " topics.", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public UserProfileData getUserProfile()
/*     */   {
/* 433 */     return this.m_profileData;
/*     */   }
/*     */ 
/*     */   public void setUserProfile(UserProfileData profileData)
/*     */   {
/* 438 */     this.m_profileData = profileData;
/*     */   }
/*     */ 
/*     */   public void putAttributes(String type, Vector attribs)
/*     */   {
/* 446 */     this.m_attributes.put(type, attribs);
/*     */   }
/*     */ 
/*     */   public Vector getAttributes(String type)
/*     */   {
/* 451 */     return (Vector)this.m_attributes.get(type);
/*     */   }
/*     */ 
/*     */   public void removeAttributes(String type)
/*     */   {
/* 456 */     Vector v = getAttributes(type);
/* 457 */     if (v == null)
/*     */       return;
/* 459 */     v.removeAllElements();
/*     */   }
/*     */ 
/*     */   public void setName(String name)
/*     */   {
/* 468 */     this.m_name = name;
/* 469 */     this.m_props.put("dName", name);
/*     */   }
/*     */ 
/*     */   public String getProperty(String key)
/*     */   {
/* 474 */     return this.m_props.getProperty(key);
/*     */   }
/*     */ 
/*     */   public void setProperty(String key, String value)
/*     */   {
/* 479 */     this.m_props.put(key, value);
/*     */   }
/*     */ 
/*     */   public UserProfileData getProfileData()
/*     */   {
/* 484 */     return this.m_profileData;
/*     */   }
/*     */ 
/*     */   public void copyAttributesToExternal()
/*     */   {
/* 489 */     if (this.m_attributes == null)
/*     */       return;
/* 491 */     this.m_externalAttributes = new Hashtable();
/* 492 */     for (Enumeration e = this.m_attributes.keys(); e.hasMoreElements(); )
/*     */     {
/* 494 */       String key = (String)e.nextElement();
/* 495 */       Vector uaiList = (Vector)this.m_attributes.get(key);
/* 496 */       Vector newuaiList = new IdcVector();
/* 497 */       newuaiList.setSize(uaiList.size());
/*     */ 
/* 499 */       for (int i = 0; i < uaiList.size(); ++i)
/*     */       {
/* 501 */         UserAttribInfo uai = (UserAttribInfo)uaiList.elementAt(i);
/* 502 */         UserAttribInfo newuai = new UserAttribInfo();
/* 503 */         newuai.m_attribType = uai.m_attribType;
/* 504 */         newuai.m_attribName = uai.m_attribName;
/* 505 */         newuai.m_attribPrivilege = uai.m_attribPrivilege;
/* 506 */         newuaiList.setElementAt(newuai, i);
/*     */       }
/*     */ 
/* 509 */       this.m_externalAttributes.put(key, newuaiList);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void restoreAttributesFromExternal()
/*     */   {
/* 516 */     if (this.m_externalAttributes == null)
/*     */       return;
/* 518 */     this.m_attributes = new Hashtable();
/* 519 */     for (Enumeration e = this.m_externalAttributes.keys(); e.hasMoreElements(); )
/*     */     {
/* 521 */       String key = (String)e.nextElement();
/* 522 */       Vector uaiList = (Vector)this.m_externalAttributes.get(key);
/* 523 */       Vector newuaiList = new IdcVector();
/* 524 */       newuaiList.setSize(uaiList.size());
/*     */ 
/* 526 */       for (int i = 0; i < uaiList.size(); ++i)
/*     */       {
/* 528 */         UserAttribInfo uai = (UserAttribInfo)uaiList.elementAt(i);
/* 529 */         UserAttribInfo newuai = new UserAttribInfo();
/* 530 */         newuai.m_attribType = uai.m_attribType;
/* 531 */         newuai.m_attribName = uai.m_attribName;
/* 532 */         newuai.m_attribPrivilege = uai.m_attribPrivilege;
/* 533 */         newuaiList.setElementAt(newuai, i);
/*     */       }
/*     */ 
/* 536 */       this.m_attributes.put(key, newuaiList);
/*     */     }
/* 538 */     this.m_hasAttributesLoaded = true;
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 545 */     return "Name: '" + this.m_name + "'\nExpired: " + this.m_isExpired + "\nAttributes:" + this.m_attributes + "\nProperties:" + this.m_props;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 550 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 102518 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.UserData
 * JD-Core Version:    0.5.4
 */