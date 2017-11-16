/*      */ package intradoc.provider;
/*      */ 
/*      */ import intradoc.common.ClassHelper;
/*      */ import intradoc.common.ClassHelperUtils;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.conversion.CryptoPasswordUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.util.IdcMessage;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.lang.reflect.InvocationTargetException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Map;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class LdapConnection
/*      */   implements ProviderConnection, LdapConnectionInterface, ParameterizedLdapSearchInterface
/*      */ {
/*      */   protected ProviderConnectionManager m_manager;
/*      */   protected DataBinder m_connectionData;
/*      */   protected ClassHelper m_connection;
/*      */   protected String[] m_classes;
/*      */   protected String[] m_methods;
/*      */   protected String m_serverAddress;
/*      */   protected int m_serverPort;
/*      */   protected String m_ldapUrl;
/*      */   protected String m_rootDN;
/*      */   protected Vector m_rootDNParts;
/*      */   protected String m_bindName;
/*      */   protected String m_passwd;
/*      */   protected String m_bindLookupDN;
/*      */   protected int m_ldapVersion;
/*      */   protected boolean m_isAdmin;
/*      */   protected boolean m_inError;
/*      */   protected boolean m_isInit;
/*      */   protected boolean m_isNetscape;
/*      */   protected boolean m_useSSL;
/*      */   protected boolean m_isErrorReported;
/*      */   protected IdcMessage m_lastErrorMsg;
/*      */   protected long m_connectionOpenTs;
/*      */   protected long m_connectionTimeout;
/*      */ 
/*      */   public LdapConnection()
/*      */   {
/*   42 */     this.m_manager = null;
/*      */ 
/*   47 */     this.m_connectionData = null;
/*      */ 
/*   54 */     this.m_connection = null;
/*      */ 
/*   60 */     this.m_classes = null;
/*      */ 
/*   66 */     this.m_methods = null;
/*      */ 
/*  116 */     this.m_isAdmin = false;
/*      */ 
/*  121 */     this.m_inError = false;
/*      */ 
/*  126 */     this.m_isInit = false;
/*      */ 
/*  131 */     this.m_isNetscape = false;
/*      */ 
/*  136 */     this.m_useSSL = false;
/*      */ 
/*  141 */     this.m_isErrorReported = false;
/*      */ 
/*  151 */     this.m_connectionOpenTs = 0L;
/*      */ 
/*  156 */     this.m_connectionTimeout = 0L;
/*      */   }
/*      */ 
/*      */   public void init(ProviderConnectionManager manager, DataBinder data)
/*      */     throws DataException
/*      */   {
/*  168 */     init(manager, data, null, null, 0, null);
/*      */   }
/*      */ 
/*      */   public void init(ProviderConnectionManager manager, DataBinder data, String defaultClass, Object rawConnection, int flags, Map params)
/*      */     throws DataException
/*      */   {
/*  178 */     this.m_manager = manager;
/*  179 */     this.m_connectionData = data;
/*      */ 
/*  182 */     initConfig();
/*  183 */     openConnection();
/*      */   }
/*      */ 
/*      */   public Object getRawConnection()
/*      */   {
/*  191 */     return this.m_connection;
/*      */   }
/*      */ 
/*      */   public void initConfig()
/*      */     throws DataException
/*      */   {
/*  199 */     if (this.m_isInit)
/*      */     {
/*  201 */       return;
/*      */     }
/*      */ 
/*  204 */     debug("Initializing LDAP connection.");
/*      */ 
/*  206 */     this.m_isNetscape = StringUtils.convertToBool(this.m_connectionData.getLocal("UseNetscape"), false);
/*      */ 
/*  209 */     this.m_classes = JNDI_CLASSES;
/*  210 */     this.m_methods = JNDI_METHODS;
/*  211 */     if (this.m_isNetscape)
/*      */     {
/*  213 */       this.m_classes = NETSCAPE_CLASSES;
/*  214 */       this.m_methods = NETSCAPE_METHODS;
/*      */     }
/*      */ 
/*  217 */     ClassHelper con = new ClassHelper();
/*      */     try
/*      */     {
/*  220 */       con.initWithoutInstatiate(this.m_classes[0]);
/*      */     }
/*      */     catch (ServiceException s)
/*      */     {
/*      */     }
/*      */ 
/*  227 */     this.m_serverAddress = this.m_connectionData.getLocal("LdapServer");
/*  228 */     this.m_rootDN = this.m_connectionData.getLocal("LdapSuffix");
/*  229 */     this.m_rootDNParts = StringUtils.parseArray(this.m_rootDN, ',', '^');
/*  230 */     for (int i = 0; i < this.m_rootDNParts.size(); ++i)
/*      */     {
/*  232 */       String part = (String)this.m_rootDNParts.elementAt(i);
/*  233 */       part = part.trim();
/*  234 */       part = part.toLowerCase();
/*  235 */       this.m_rootDNParts.setElementAt(part, i);
/*      */     }
/*      */ 
/*  238 */     if ((this.m_serverAddress != null) && (this.m_rootDN == null));
/*  243 */     this.m_serverPort = NumberUtils.parseInteger(this.m_connectionData.getLocal("LdapPort"), 389);
/*  244 */     this.m_ldapUrl = new StringBuilder().append("ldap://").append(this.m_serverAddress).toString();
/*  245 */     if (this.m_serverPort > 0)
/*      */     {
/*  247 */       this.m_ldapUrl = new StringBuilder().append(this.m_ldapUrl).append(":").append(this.m_serverPort).toString();
/*      */     }
/*      */ 
/*  250 */     this.m_bindName = this.m_connectionData.getLocal("LdapAdminDN");
/*  251 */     if ((this.m_bindName != null) && (this.m_bindName.length() == 0))
/*      */     {
/*  253 */       this.m_bindName = "";
/*      */     }
/*      */ 
/*  256 */     this.m_connectionTimeout = (NumberUtils.parseInteger(this.m_connectionData.getLocal("LdapConnectionTimeoutInMins"), 10) * 60000);
/*      */ 
/*  259 */     this.m_ldapVersion = NumberUtils.parseInteger(this.m_connectionData.getLocal("LdapVersion"), 3);
/*      */ 
/*  261 */     this.m_useSSL = StringUtils.convertToBool(this.m_connectionData.getLocal("UseSecureLdap"), false);
/*      */ 
/*  263 */     this.m_bindLookupDN = this.m_connectionData.getLocal("LdapBindLookupDN");
/*      */ 
/*  269 */     this.m_passwd = getServerConnectionString();
/*      */ 
/*  271 */     this.m_isInit = true;
/*      */   }
/*      */ 
/*      */   public Object getConnection()
/*      */   {
/*  287 */     Object retVal = null;
/*  288 */     reset();
/*  289 */     if (this.m_connection != null)
/*      */     {
/*  291 */       retVal = this.m_connection.getClassInstance();
/*      */     }
/*  293 */     return retVal;
/*      */   }
/*      */ 
/*      */   public void openConnection()
/*      */     throws DataException
/*      */   {
/*      */     IdcMessage msg;
/*      */     Throwable t;
/*      */     try
/*      */     {
/*  306 */       if (this.m_inError)
/*      */       {
/*  310 */         this.m_isAdmin = false;
/*  311 */         close();
/*      */       }
/*      */ 
/*  314 */       subOpenConnection();
/*      */ 
/*  316 */       ClassHelper attr = ClassHelperUtils.createClassHelperRef(this.m_classes[2]);
/*  317 */       attr.setObject(this.m_connection.invokeRaw(this.m_methods[3], new Object[] { this.m_rootDN }));
/*  318 */       if (attr.getClassInstance() == null)
/*      */       {
/*  320 */         this.m_inError = true;
/*      */       }
/*  322 */       this.m_connectionOpenTs = System.currentTimeMillis();
/*  323 */       debug(new StringBuilder().append("LDAP connection opened at").append(this.m_connectionOpenTs).toString());
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  328 */       this.m_inError = true;
/*  329 */       msg = null;
/*      */ 
/*  331 */       t = e.getCause();
/*      */ 
/*  333 */       if ((t == null) || (!t instanceof Exception))
/*      */       {
/*  335 */         throw new DataException(e.getMessage());
/*      */       }
/*      */     }
/*  337 */     Exception target = (Exception)t;
/*      */ 
/*  339 */     close();
/*      */ 
/*  341 */     if (isErrorOfType(target, 0))
/*      */     {
/*  343 */       msg = new IdcMessage(target, "csLdapConnectionErrorMsg", new Object[] { this.m_ldapUrl });
/*      */     }
/*  345 */     else if (isErrorOfType(target, 1))
/*      */     {
/*  347 */       String uri = new StringBuilder().append(this.m_ldapUrl).append("/").append(this.m_rootDN).toString();
/*      */ 
/*  349 */       if (this.m_bindName == null)
/*      */       {
/*  351 */         msg = new IdcMessage(target, "csLdapAnonymousAccessDeniedMsg", new Object[] { this.m_rootDN });
/*      */       }
/*      */       else
/*      */       {
/*  355 */         msg = new IdcMessage(target, "csLdapAdminAccessDeniedMsg", new Object[] { this.m_connectionData.getAllowMissing("ProviderName"), uri });
/*      */       }
/*      */ 
/*      */     }
/*  359 */     else if (isErrorOfType(target, 3))
/*      */     {
/*  361 */       msg = new IdcMessage(target, "csLdapErrorBindingToServerMsg", new Object[] { this.m_ldapUrl });
/*      */     }
/*      */     else
/*      */     {
/*  365 */       msg = new IdcMessage(target, "csLdapUnknownError", new Object[0]);
/*      */     }
/*      */ 
/*  368 */     if (!this.m_isErrorReported)
/*      */     {
/*  370 */       Report.error(null, e, msg);
/*      */     }
/*  372 */     this.m_lastErrorMsg = msg;
/*  373 */     this.m_isErrorReported = true;
/*      */ 
/*  375 */     throw new DataException(e, msg);
/*      */   }
/*      */ 
/*      */   public void close()
/*      */   {
/*  387 */     this.m_isAdmin = false;
/*  388 */     if ((this.m_connection == null) || (this.m_connection.getClassInstance() == null))
/*      */     {
/*  390 */       return;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  395 */       this.m_connection.invokeRaw(this.m_methods[1]);
/*  396 */       this.m_connection.setObject(null);
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  400 */       if (!isErrorOfType(e, 2))
/*      */         return;
/*  402 */       debug(new StringBuilder().append("Error closing LDAP Connection. Error: ").append(e.getMessage()).toString());
/*      */     }
/*      */   }
/*      */ 
/*      */   public void prepareUse()
/*      */   {
/*      */   }
/*      */ 
/*      */   public void reset()
/*      */   {
/*  423 */     this.m_lastErrorMsg = null;
/*      */ 
/*  425 */     boolean connectionTimedOut = this.m_connectionOpenTs + this.m_connectionTimeout < System.currentTimeMillis();
/*      */     try
/*      */     {
/*  430 */       if ((connectionTimedOut) || ((this.m_inError) && (this.m_connection != null)))
/*      */       {
/*  432 */         debug(new StringBuilder().append("Connection ").append((connectionTimedOut) ? "timed out." : "in error.").toString());
/*  433 */         close();
/*      */       }
/*      */ 
/*  436 */       if ((!this.m_isAdmin) || (this.m_connection == null) || (this.m_connection.getClassInstance() == null))
/*      */       {
/*  438 */         openConnection();
/*      */       }
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  443 */       debug(new StringBuilder().append("Error creating LDAP Connection. Error: ").append(e.getMessage()).toString());
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean isBadConnection()
/*      */   {
/*  454 */     return this.m_inError;
/*      */   }
/*      */ 
/*      */   public void subOpenConnection()
/*      */     throws ServiceException, InvocationTargetException
/*      */   {
/*  462 */     if (this.m_connection == null)
/*      */     {
/*  464 */       debug(this.m_classes[0]);
/*  465 */       this.m_connection = ClassHelperUtils.createClassHelperRef(this.m_classes[0]);
/*      */     }
/*      */ 
/*  470 */     if (this.m_isNetscape)
/*      */     {
/*  473 */       ClassHelper socketFactory = new ClassHelper();
/*  474 */       if (this.m_useSSL)
/*      */       {
/*  477 */         String trustManagerClass = "javax.net.ssl.TrustManager";
/*  478 */         String keyManagerClass = "javax.net.ssl.KeyManager";
/*      */ 
/*  480 */         ClassHelper context = null;
/*      */         try
/*      */         {
/*  483 */           context = ClassHelperUtils.createClassHelperRef("javax.net.ssl.SSLContext");
/*      */         }
/*      */         catch (ServiceException s)
/*      */         {
/*  488 */           trustManagerClass = "com.sun.net.ssl.TrustManager";
/*  489 */           keyManagerClass = "com.sun.net.ssl.KeyManager";
/*  490 */           context = ClassHelperUtils.createClassHelperRef("com.sun.net.ssl.SSLContext");
/*      */         }
/*      */ 
/*  500 */         context.setObject(context.invokeRaw("getInstance", new Object[] { "SSL" }));
/*      */ 
/*  502 */         ClassHelper factory = ClassHelperUtils.createClassHelperRef("javax.net.ssl.SSLSocketFactory");
/*      */ 
/*  505 */         ClassHelper km = ClassHelperUtils.createClassHelperRef(keyManagerClass);
/*      */ 
/*  507 */         ClassHelper tm = ClassHelperUtils.createClassHelperRef(trustManagerClass);
/*      */ 
/*  510 */         Integer one = new Integer(1);
/*      */ 
/*  512 */         ClassHelper kmArray = ClassHelperUtils.createClassHelperRef("java.lang.reflect.Array");
/*      */ 
/*  514 */         kmArray.setObject(kmArray.invokeRaw("newInstance", new Object[] { km.getClassRep(), one }));
/*      */ 
/*  517 */         ClassHelper tmArray = ClassHelperUtils.createClassHelperRef("java.lang.reflect.Array");
/*      */ 
/*  519 */         tmArray.setObject(tmArray.invokeRaw("newInstance", new Object[] { tm.getClassRep(), one }));
/*      */ 
/*  522 */         ClassHelper random = ClassHelperUtils.createClassHelper("java.security.SecureRandom");
/*      */ 
/*  527 */         context.invokeRaw("init", new Object[] { kmArray.getClassInstance(), tmArray.getClassInstance(), random.getClassInstance() });
/*      */ 
/*  530 */         factory.setObject(context.invokeRaw("getSocketFactory"));
/*      */ 
/*  532 */         String[] cipherSuites = (String[])(String[])factory.invokeRaw("getDefaultCipherSuites");
/*      */ 
/*  534 */         socketFactory.initRaw("netscape.ldap.factory.JSSESocketFactory", new Object[] { cipherSuites });
/*      */       }
/*      */ 
/*  538 */       debug("Initializing Netscape classes");
/*  539 */       if (this.m_connection.getClassInstance() == null)
/*      */       {
/*  542 */         if (this.m_useSSL)
/*      */         {
/*  544 */           this.m_connection.initRaw(this.m_classes[0], new Object[] { socketFactory.getClassInstance() });
/*      */         }
/*      */         else
/*      */         {
/*  549 */           this.m_connection.init(this.m_classes[0]);
/*      */         }
/*      */ 
/*  553 */         this.m_connection.invokeRaw(this.m_methods[0], new Object[] { this.m_serverAddress, new Integer(this.m_serverPort) });
/*      */ 
/*  555 */         this.m_isAdmin = false;
/*      */       }
/*      */ 
/*  558 */       if (!this.m_isAdmin)
/*      */       {
/*  561 */         this.m_connection.invokeRaw(this.m_methods[2], new Object[] { new Integer(this.m_ldapVersion), this.m_bindName, this.m_passwd });
/*      */ 
/*  563 */         this.m_isAdmin = true;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  569 */       Hashtable env = new Hashtable();
/*      */ 
/*  571 */       String key = (String)this.m_connection.getFieldValue("INITIAL_CONTEXT_FACTORY");
/*  572 */       env.put(key, "com.sun.jndi.ldap.LdapCtxFactory");
/*      */ 
/*  575 */       key = (String)this.m_connection.getFieldValue("SECURITY_AUTHENTICATION");
/*  576 */       if (this.m_bindName.length() == 0)
/*      */       {
/*  578 */         env.put(key, "none");
/*      */       }
/*      */       else
/*      */       {
/*  582 */         env.put(key, "simple");
/*      */       }
/*      */ 
/*  585 */       key = (String)this.m_connection.getFieldValue("PROVIDER_URL");
/*  586 */       env.put(key, this.m_ldapUrl);
/*      */ 
/*  588 */       env.put("java.naming.ldap.version", new StringBuilder().append(this.m_ldapVersion).append("").toString());
/*      */ 
/*  590 */       key = (String)this.m_connection.getFieldValue("REFERRAL");
/*  591 */       env.put(key, "follow");
/*      */ 
/*  593 */       if (!this.m_isAdmin)
/*      */       {
/*  595 */         if (this.m_bindName.length() > 0)
/*      */         {
/*  597 */           String field = (String)this.m_connection.getFieldValue("SECURITY_PRINCIPAL");
/*      */ 
/*  599 */           env.put(field, this.m_bindName);
/*      */ 
/*  601 */           field = (String)this.m_connection.getFieldValue("SECURITY_CREDENTIALS");
/*      */ 
/*  603 */           env.put(field, this.m_passwd);
/*      */         }
/*      */         else
/*      */         {
/*  607 */           String field = (String)this.m_connection.getFieldValue("SECURITY_AUTHENTICATION");
/*      */ 
/*  609 */           env.put(field, "none");
/*      */         }
/*  611 */         this.m_isAdmin = true;
/*      */       }
/*      */ 
/*  614 */       if (this.m_useSSL)
/*      */       {
/*  616 */         String field = (String)this.m_connection.getFieldValue("SECURITY_PROTOCOL");
/*      */ 
/*  618 */         env.put(field, "ssl");
/*      */       }
/*      */ 
/*  621 */       debug("Initializing JNDI classes");
/*  622 */       this.m_connection.initRaw(this.m_classes[0], new Object[] { env });
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean authenticate(String name, String passwd)
/*      */     throws ServiceException
/*      */   {
/*  637 */     boolean isValid = false;
/*      */ 
/*  639 */     Object conObj = getConnection();
/*      */ 
/*  642 */     if ((this.m_connection == null) || (name == null) || (passwd == null) || (passwd.length() == 0) || (name.length() == 0))
/*      */     {
/*  645 */       throw new ServiceException(-21, "csLdapUnableToAuthenticateUserMsg");
/*      */     }
/*      */ 
/*  650 */     if (conObj == null)
/*      */     {
/*  652 */       debug("Null connection returned for authentication.");
/*  653 */       this.m_inError = true;
/*      */ 
/*  656 */       throw new ServiceException(null, -16, this.m_lastErrorMsg);
/*      */     }
/*      */ 
/*  659 */     ClassHelper con = ClassHelperUtils.createClassHelperRef(this.m_classes[1]);
/*  660 */     con.setObject(getConnection());
/*      */     try
/*      */     {
/*  664 */       this.m_isAdmin = false;
/*  665 */       doAuthentication(con, name, passwd);
/*  666 */       isValid = true;
/*      */     }
/*      */     catch (InvocationTargetException e)
/*      */     {
/*  670 */       handleLdapException(e);
/*      */     }
/*      */ 
/*  673 */     return isValid;
/*      */   }
/*      */ 
/*      */   public Hashtable read(String dn)
/*      */     throws ServiceException
/*      */   {
/*  681 */     if (!this.m_isInit)
/*      */     {
/*  683 */       debug("Attempt to read from LDAP connection before initialization");
/*  684 */       this.m_inError = true;
/*  685 */       throw new ServiceException(null, -32, this.m_lastErrorMsg);
/*      */     }
/*      */ 
/*  688 */     Object conObj = getConnection();
/*  689 */     if (conObj == null)
/*      */     {
/*  691 */       debug("Null connection returned for authentication.");
/*  692 */       this.m_inError = true;
/*      */ 
/*  695 */       throw new ServiceException(null, -16, this.m_lastErrorMsg);
/*      */     }
/*      */ 
/*  698 */     ClassHelper con = ClassHelperUtils.createClassHelperRef(this.m_classes[1]);
/*  699 */     con.setObject(conObj);
/*      */ 
/*  701 */     ClassHelper attrs = ClassHelperUtils.createClassHelperRef(this.m_classes[2]);
/*      */     try
/*      */     {
/*  704 */       attrs.setObject(con.invokeRaw(this.m_methods[3], new Object[] { dn }));
/*      */     }
/*      */     catch (InvocationTargetException e)
/*      */     {
/*  708 */       handleLdapException(e);
/*      */     }
/*      */ 
/*  711 */     boolean inError = false;
/*  712 */     if (attrs == null)
/*      */     {
/*  714 */       inError = true;
/*      */     }
/*      */ 
/*  724 */     if (inError)
/*      */     {
/*  726 */       debug("Error. No attributes returned from read.");
/*  727 */       this.m_inError = true;
/*  728 */       String msg = LocaleUtils.encodeMessage("csLdapUnableToReadAttributesForUserMsg", dn);
/*  729 */       throw new ServiceException(-16, msg);
/*      */     }
/*      */ 
/*  732 */     Hashtable attributes = unpackLdapAttributes(attrs.invoke(this.m_methods[7]));
/*  733 */     Vector dnAttr = new IdcVector();
/*  734 */     dnAttr.addElement(dn);
/*  735 */     attributes.put("dn", dnAttr);
/*      */ 
/*  737 */     return attributes;
/*      */   }
/*      */ 
/*      */   public Vector search(String searchRoot, String filter, String[] returnAttributes, Map searchParameters)
/*      */     throws ServiceException
/*      */   {
/*  743 */     String root = searchRoot;
/*  744 */     String tmpFilter = filter;
/*  745 */     String[] attributes = returnAttributes;
/*  746 */     String scope = "SUBTREE";
/*      */ 
/*  748 */     if (searchParameters != null)
/*      */     {
/*  750 */       String tmpVal = null;
/*      */ 
/*  752 */       if ((tmpFilter == null) || (tmpFilter.length() == 0))
/*      */       {
/*  754 */         tmpVal = (String)searchParameters.get("searchFilter");
/*  755 */         if ((tmpVal != null) && (tmpVal.length() > 0))
/*      */         {
/*  757 */           tmpFilter = tmpVal;
/*      */         }
/*      */       }
/*      */ 
/*  761 */       if (attributes == null)
/*      */       {
/*  763 */         String[] tmpStrArray = (String[])(String[])searchParameters.get("searchAttributes");
/*  764 */         if ((tmpStrArray != null) && (tmpStrArray.length > 0))
/*      */         {
/*  766 */           attributes = tmpStrArray;
/*      */         }
/*      */       }
/*      */ 
/*  770 */       if ((root == null) || (root.length() == 0))
/*      */       {
/*  772 */         tmpVal = (String)searchParameters.get("searchRoot");
/*  773 */         if ((tmpVal != null) && (tmpVal.length() > 0))
/*      */         {
/*  775 */           root = tmpVal;
/*      */         }
/*      */         else
/*      */         {
/*  779 */           root = this.m_rootDN;
/*      */         }
/*      */       }
/*      */ 
/*  783 */       tmpVal = (String)searchParameters.get("searchScope");
/*  784 */       if ((tmpVal != null) && (tmpVal.length() > 0))
/*      */       {
/*  786 */         scope = tmpVal;
/*      */       }
/*      */     }
/*      */ 
/*  790 */     return searchEx(scope, root, tmpFilter, attributes);
/*      */   }
/*      */ 
/*      */   public Vector search(String filter)
/*      */     throws ServiceException
/*      */   {
/*  798 */     return searchEx("SUBTREE", this.m_rootDN, filter, null);
/*      */   }
/*      */ 
/*      */   protected Vector searchEx(String scope, String root, String filter, String[] returnAttributes)
/*      */     throws ServiceException
/*      */   {
/*  805 */     Object conObj = getConnection();
/*      */ 
/*  807 */     if (conObj == null)
/*      */     {
/*  809 */       debug("Null connection returned for authentication.");
/*  810 */       this.m_inError = true;
/*      */ 
/*  814 */       throw new ServiceException(null, -16, this.m_lastErrorMsg);
/*      */     }
/*      */ 
/*  817 */     ClassHelper con = ClassHelperUtils.createClassHelperRef(this.m_classes[1]);
/*  818 */     con.setObject(conObj);
/*      */ 
/*  820 */     Enumeration results = null;
/*      */     try
/*      */     {
/*  823 */       results = doSearch(con, scope, root, filter, returnAttributes);
/*      */     }
/*      */     catch (InvocationTargetException e)
/*      */     {
/*  827 */       handleLdapException(e);
/*      */     }
/*  829 */     if (results == null)
/*      */     {
/*  832 */       throw new ServiceException(-16, "!csLdapUnableToExecuteSearchMsg");
/*      */     }
/*      */ 
/*  835 */     Vector searchResults = new IdcVector();
/*      */ 
/*  838 */     while (results.hasMoreElements())
/*      */     {
/*      */       try
/*      */       {
/*  842 */         ClassHelper entry = ClassHelperUtils.createClassHelperRef(this.m_classes[4]);
/*  843 */         Object obj = results.nextElement();
/*  844 */         if ((!this.m_isNetscape) || (!ClassHelperUtils.isInstanceOf(this.m_classes[6], obj)));
/*  848 */         entry.setObject(obj);
/*      */ 
/*  850 */         Hashtable attrs = unpackLdapAttributes(entry.invokeRaw(this.m_methods[8]));
/*  851 */         String dn = (String)entry.invokeRaw(this.m_methods[5]);
/*  852 */         if (dn.endsWith("\""))
/*      */         {
/*  854 */           dn = dn.substring(0, dn.length() - 1);
/*      */         }
/*  856 */         if (dn.startsWith("\""))
/*      */         {
/*  858 */           dn = dn.substring(1);
/*      */         }
/*      */ 
/*  861 */         Vector dnParts = StringUtils.parseArray(dn, ',', '^');
/*  862 */         int rootLen = this.m_rootDNParts.size();
/*  863 */         int dnLen = dnParts.size();
/*  864 */         boolean dnHasRootSuffix = true;
/*  865 */         for (int i = 0; (i < rootLen) && (i < dnLen); ++i)
/*      */         {
/*  867 */           String dnVal = (String)dnParts.elementAt(dnLen - i - 1);
/*  868 */           dnVal = dnVal.trim();
/*  869 */           String rootVal = (String)this.m_rootDNParts.elementAt(rootLen - i - 1);
/*  870 */           if (dnVal.equalsIgnoreCase(rootVal))
/*      */             continue;
/*  872 */           dnHasRootSuffix = false;
/*      */         }
/*      */ 
/*  876 */         String userRoot = root;
/*  877 */         if ((userRoot == null) || (userRoot.length() == 0))
/*      */         {
/*  879 */           userRoot = this.m_rootDN;
/*      */         }
/*  881 */         if (!dnHasRootSuffix)
/*      */         {
/*  883 */           dn = new StringBuilder().append(dn).append(",").append(userRoot).toString();
/*      */         }
/*      */ 
/*  886 */         Vector dnAttr = new IdcVector();
/*  887 */         dnAttr.addElement(dn);
/*  888 */         attrs.put("dn", dnAttr);
/*      */ 
/*  891 */         searchResults.addElement(attrs);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  895 */         throw new ServiceException(new StringBuilder().append("!csLdapSearchResultsErrorMsg").append(e.getMessage()).toString());
/*      */       }
/*      */     }
/*      */ 
/*  899 */     return searchResults;
/*      */   }
/*      */ 
/*      */   protected void doAuthentication(ClassHelper con, String name, String passwd)
/*      */     throws ServiceException, InvocationTargetException
/*      */   {
/*  907 */     String bindDN = this.m_bindLookupDN;
/*  908 */     if (bindDN == null)
/*      */     {
/*  910 */       bindDN = name;
/*      */     }
/*      */ 
/*  914 */     if (this.m_isNetscape)
/*      */     {
/*  917 */       con.invokeRaw(this.m_methods[2], new Object[] { bindDN, passwd });
/*      */     }
/*      */     else
/*      */     {
/*  922 */       con.invokeRaw("addToEnvironment", new Object[] { con.getFieldValue("SECURITY_AUTHENTICATION"), "simple" });
/*  923 */       con.invokeRaw("addToEnvironment", new Object[] { con.getFieldValue("SECURITY_PRINCIPAL"), name });
/*  924 */       con.invokeRaw("addToEnvironment", new Object[] { con.getFieldValue("SECURITY_CREDENTIALS"), passwd });
/*  925 */       con.invokeRaw("lookup", new Object[] { bindDN });
/*      */     }
/*      */   }
/*      */ 
/*      */   public Enumeration doSearch(ClassHelper con, String scope, String root, String filter, String[] returnAttributes)
/*      */     throws ServiceException, InvocationTargetException
/*      */   {
/*  935 */     Enumeration e = null;
/*      */ 
/*  937 */     String rootStr = root;
/*  938 */     if ((root == null) || (root.length() == 0))
/*      */     {
/*  940 */       rootStr = this.m_rootDN;
/*      */     }
/*      */ 
/*  943 */     if (this.m_isNetscape)
/*      */     {
/*  946 */       String scopeVal = "SCOPE_SUB";
/*  947 */       if (scope.equals("BASE"))
/*      */       {
/*  949 */         scopeVal = "SCOPE_BASE";
/*      */       }
/*  951 */       else if (scope.equals("ONE"))
/*      */       {
/*  953 */         scopeVal = "SCOPE_ONE";
/*      */       }
/*  955 */       Integer scopeInt = (Integer)con.getFieldValue(scopeVal);
/*  956 */       e = (Enumeration)con.invokeRawWithTypes(this.m_methods[4], new Object[] { rootStr, scopeInt, filter, returnAttributes, new Boolean(false) }, new Object[] { rootStr, scopeInt, filter, new String[0], new Boolean(false) });
/*      */     }
/*      */     else
/*      */     {
/*  963 */       String scopeVal = "SUBTREE_SCOPE";
/*  964 */       if (scope.equals("BASE"))
/*      */       {
/*  966 */         scopeVal = "OBJECT_SCOPE";
/*      */       }
/*  968 */       else if (scope.equals("ONE"))
/*      */       {
/*  970 */         scopeVal = "ONELEVEL_SCOPE";
/*      */       }
/*      */ 
/*  973 */       ClassHelper ctls = ClassHelperUtils.createClassHelper("javax.naming.directory.SearchControls");
/*  974 */       Object obj = ctls.getFieldValue(scopeVal);
/*  975 */       ctls.invokeRaw("setSearchScope", new Object[] { obj });
/*      */ 
/*  979 */       if (returnAttributes != null)
/*      */       {
/*  981 */         ctls.invokeRaw("setReturningAttributes", new Object[] { returnAttributes });
/*      */       }
/*  983 */       e = (Enumeration)con.invokeRaw(this.m_methods[4], new Object[] { rootStr, filter, ctls.getClassInstance() });
/*      */     }
/*      */ 
/*  986 */     return e;
/*      */   }
/*      */ 
/*      */   protected Hashtable unpackLdapAttributes(Object attrSetObj)
/*      */     throws ServiceException
/*      */   {
/* 1006 */     Hashtable attributes = new Hashtable();
/*      */ 
/* 1008 */     ClassHelper attrSet = ClassHelperUtils.createClassHelperRef(this.m_classes[5]);
/* 1009 */     attrSet.setObject(attrSetObj);
/*      */ 
/* 1013 */     Enumeration en = (Enumeration)attrSet.invoke(this.m_methods[6]);
/* 1014 */     while (en.hasMoreElements())
/*      */     {
/* 1016 */       ClassHelper value = ClassHelperUtils.createClassHelperRef(this.m_classes[3]);
/* 1017 */       value.setObject(en.nextElement());
/* 1018 */       String name = (String)value.invoke(this.m_methods[9]);
/* 1019 */       if (name.equalsIgnoreCase("userpassword")) {
/*      */         continue;
/*      */       }
/*      */ 
/* 1023 */       Enumeration subEnum = (Enumeration)value.invoke(this.m_methods[10]);
/*      */ 
/* 1025 */       while (subEnum.hasMoreElements())
/*      */       {
/* 1027 */         Object obj = subEnum.nextElement();
/*      */ 
/* 1029 */         String item = null;
/* 1030 */         if (!obj instanceof String)
/*      */           continue;
/* 1032 */         item = (String)obj;
/*      */ 
/* 1040 */         Vector attrV = (Vector)attributes.get(name);
/* 1041 */         if (attrV == null)
/*      */         {
/* 1043 */           attrV = new IdcVector();
/* 1044 */           attributes.put(name, attrV);
/*      */         }
/* 1046 */         attrV.addElement(item);
/*      */       }
/*      */     }
/*      */ 
/* 1050 */     return attributes;
/*      */   }
/*      */ 
/*      */   protected boolean isErrorOfType(Exception l, int type)
/*      */   {
/* 1059 */     boolean isCorrectType = false;
/* 1060 */     if (!this.m_isNetscape)
/*      */     {
/* 1062 */       if (type == 2)
/*      */       {
/* 1064 */         isCorrectType = ClassHelperUtils.isInstanceOf("javax.naming.NamingException", l);
/*      */       }
/*      */       else
/*      */       {
/* 1069 */         switch (type)
/*      */         {
/*      */         case 0:
/* 1072 */           isCorrectType = ClassHelperUtils.isInstanceOf("javax.naming.CommunicationException", l);
/* 1073 */           break;
/*      */         case 1:
/* 1075 */           isCorrectType = ClassHelperUtils.isInstanceOf("javax.naming.NamingSecurityException", l);
/* 1076 */           break;
/*      */         case 3:
/* 1078 */           isCorrectType = ClassHelperUtils.isInstanceOf("javax.naming.NameNotFoundException", l);
/*      */         case 2:
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*      */     }
/* 1085 */     else if (type == 2)
/*      */     {
/* 1087 */       isCorrectType = ClassHelperUtils.isInstanceOf(this.m_classes[7], l);
/*      */     }
/*      */     else
/*      */     {
/*      */       try
/*      */       {
/* 1094 */         String[] exceptionTypes = null;
/* 1095 */         switch (type)
/*      */         {
/*      */         case 0:
/* 1098 */           exceptionTypes = LDAP_COMM_EXCEPTION_TYPES;
/* 1099 */           break;
/*      */         case 1:
/* 1101 */           exceptionTypes = LDAP_AUTH_EXCEPTION_TYPES;
/* 1102 */           break;
/*      */         case 3:
/* 1104 */           exceptionTypes = LDAP_BIND_EXCEPTION_TYPES;
/*      */         case 2:
/*      */         }
/*      */ 
/* 1108 */         if (exceptionTypes != null)
/*      */         {
/* 1110 */           ClassHelper ldapException = ClassHelperUtils.createClassHelperRef(this.m_classes[7]);
/*      */ 
/* 1112 */           ldapException.setObject(l);
/* 1113 */           Integer intObj = (Integer)ldapException.invokeRaw("getLDAPResultCode");
/*      */ 
/* 1115 */           for (int i = 0; i < exceptionTypes.length; ++i)
/*      */           {
/* 1117 */             Integer exType = (Integer)ldapException.getFieldValue(exceptionTypes[i]);
/* 1118 */             if (!exType.equals(intObj))
/*      */               continue;
/* 1120 */             isCorrectType = true;
/* 1121 */             break;
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception ignore)
/*      */       {
/* 1128 */         if (SystemUtils.m_verbose)
/*      */         {
/* 1130 */           Report.debug("system", null, ignore);
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1135 */     return isCorrectType;
/*      */   }
/*      */ 
/*      */   protected void handleLdapException(InvocationTargetException e)
/*      */     throws ServiceException
/*      */   {
/* 1144 */     Throwable t = e.getTargetException();
/* 1145 */     if ((t == null) || (!t instanceof Exception))
/*      */     {
/* 1147 */       throw new ServiceException(e);
/*      */     }
/* 1149 */     Exception target = (Exception)t;
/*      */ 
/* 1151 */     if (isErrorOfType(target, 0))
/*      */     {
/* 1153 */       close();
/* 1154 */       this.m_inError = true;
/* 1155 */       throw new ServiceException(-16, "!csLdapUnableToCommunicateWithServerMsg", target);
/*      */     }
/*      */ 
/* 1158 */     if (isErrorOfType(target, 1))
/*      */     {
/* 1160 */       throw new ServiceException(-21, "!csLdapUnableToAuthenticateUserMsg", target);
/*      */     }
/*      */ 
/* 1163 */     if (isErrorOfType(target, 3))
/*      */     {
/* 1165 */       close();
/* 1166 */       this.m_inError = true;
/* 1167 */       throw new ServiceException(-16, "!csLdapUnableToBindToObject", target);
/*      */     }
/*      */ 
/* 1172 */     throw new ServiceException(target);
/*      */   }
/*      */ 
/*      */   public String getLastErrorMessage()
/*      */   {
/* 1181 */     return LocaleUtils.encodeMessage(this.m_lastErrorMsg);
/*      */   }
/*      */ 
/*      */   public String getServerConnectionString() throws DataException
/*      */   {
/* 1186 */     return CryptoPasswordUtils.determinePassword("LdapAdminPassword", this.m_connectionData, false);
/*      */   }
/*      */ 
/*      */   protected void debug(String msg)
/*      */   {
/* 1195 */     Report.trace("userstorage", msg, null);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1200 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 96334 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.LdapConnection
 * JD-Core Version:    0.5.4
 */