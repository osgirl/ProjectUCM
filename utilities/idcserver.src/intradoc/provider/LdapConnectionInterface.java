/*     */ package intradoc.provider;
/*     */ 
/*     */ import intradoc.common.ServiceException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public abstract interface LdapConnectionInterface
/*     */ {
/*     */   public static final String IDC_VERSION_INFO = "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   public static final int CONNECT = 0;
/*     */   public static final int DISCONNECT = 1;
/*     */   public static final int BIND = 2;
/*     */   public static final int READ = 3;
/*     */   public static final int SEARCH = 4;
/*     */   public static final int GET_DN = 5;
/*     */   public static final int GET_ATTRIBUTES = 6;
/*     */   public static final int GET_READ_ATTRIBUTE_SET = 7;
/*     */   public static final int GET_SEARCH_ATTRIBUTE_SET = 8;
/*     */   public static final int GET_ATTRIBUTE_NAME = 9;
/*     */   public static final int GET_ATTRIBUTE_VALUES = 10;
/*     */   public static final int LDAP_INITIAL_CONNECTION_CLASS = 0;
/*     */   public static final int LDAP_CONNECTION_CLASS = 1;
/*     */   public static final int LDAP_ENTRY_CLASS = 2;
/*     */   public static final int LDAP_ATTRIBUTE_CLASS = 3;
/*     */   public static final int LDAP_SEARCH_RESULT_CLASS = 4;
/*     */   public static final int LDAP_ATTRIBUTE_SET_CLASS = 5;
/*     */   public static final int LDAP_REFERRAL_EXCEPTION_CLASS = 6;
/*     */   public static final int LDAP_EXCEPTION_CLASS = 7;
/*  50 */   public static final String[] JNDI_METHODS = { "", "close", "", "getAttributes", "search", "getName", "getAll", "clone", "getAttributes", "getID", "getAll" };
/*     */ 
/*  63 */   public static final String[] JNDI_CLASSES = { "javax.naming.directory.InitialDirContext", "javax.naming.directory.DirContext", "javax.naming.directory.Attributes", "javax.naming.directory.Attribute", "javax.naming.directory.SearchResult", "javax.naming.directory.Attributes", "", "" };
/*     */ 
/*  73 */   public static final String[] NETSCAPE_METHODS = { "connect", "disconnect", "bind", "read", "search", "getDN", "getAttributes", "getAttributeSet", "getAttributeSet", "getName", "getStringValues" };
/*     */ 
/*  86 */   public static final String[] NETSCAPE_CLASSES = { "netscape.ldap.LDAPConnection", "netscape.ldap.LDAPConnection", "netscape.ldap.LDAPEntry", "netscape.ldap.LDAPAttribute", "netscape.ldap.LDAPEntry", "netscape.ldap.LDAPAttributeSet", "netscape.ldap.LDAPReferralException", "netscape.ldap.LDAPException" };
/*     */ 
/*  96 */   public static final String[] LDAP_COMM_EXCEPTION_TYPES = { "CONNECT_ERROR", "OPERATION_ERROR", "PARAM_ERROR", "PROTOCOL_ERROR", "SERVER_DOWN", "UNAVAILABLE" };
/*     */ 
/* 104 */   public static final String[] LDAP_AUTH_EXCEPTION_TYPES = { "ADMIN_LIMIT_EXCEEDED", "CONFIDENTIALITY_REQUIRED", "INAPPROPRIATE_AUTHENTICATION", "INSUFFICIENT_ACCESS_RIGHTS", "INVALID_CREDENTIALS" };
/*     */ 
/* 111 */   public static final String[] LDAP_BIND_EXCEPTION_TYPES = { "NO_SUCH_OBJECT" };
/*     */   public static final int COMM_ERROR = 0;
/*     */   public static final int AUTH_ERROR = 1;
/*     */   public static final int LDAP_ERROR = 2;
/*     */   public static final int BIND_ERROR = 3;
/*     */ 
/*     */   public abstract Hashtable read(String paramString)
/*     */     throws ServiceException;
/*     */ 
/*     */   public abstract Vector search(String paramString)
/*     */     throws ServiceException;
/*     */ 
/*     */   public abstract boolean authenticate(String paramString1, String paramString2)
/*     */     throws ServiceException;
/*     */ 
/*     */   public abstract String getLastErrorMessage();
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.LdapConnectionInterface
 * JD-Core Version:    0.5.4
 */