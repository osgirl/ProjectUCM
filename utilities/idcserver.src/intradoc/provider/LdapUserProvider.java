/*      */ package intradoc.provider;
/*      */ 
/*      */ import intradoc.common.ExecutionContext;
/*      */ import intradoc.common.ExecutionContextAdaptor;
/*      */ import intradoc.common.IdcCharArrayWriter;
/*      */ import intradoc.common.IdcLocale;
/*      */ import intradoc.common.IdcNumberFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataBinderUtils;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.shared.PluginFilters;
/*      */ import intradoc.shared.SecurityUtils;
/*      */ import intradoc.shared.SharedObjects;
/*      */ import intradoc.shared.UserData;
/*      */ import intradoc.shared.UserUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.IOException;
/*      */ import java.util.Enumeration;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class LdapUserProvider
/*      */   implements ProviderInterface, UserProvider
/*      */ {
/*      */   protected Provider m_provider;
/*      */   protected ProviderPoolManager m_ldapManager;
/*      */   protected String m_root;
/*      */   protected String m_userSearchRoot;
/*      */   protected String m_groupSearchRoot;
/*      */   protected String m_searchCriteria;
/*      */   protected String m_groupFilter;
/*      */   protected Vector m_prefixList;
/*      */   protected boolean m_validateDnRoot;
/*      */   protected boolean m_returnSearchAttributes;
/*      */   protected boolean m_doAccountCharMap;
/*      */   protected char m_AccountCharMapSource;
/*      */   protected char m_AccountCharMapTarget;
/*      */ 
/*      */   public LdapUserProvider()
/*      */   {
/*   32 */     this.m_provider = null;
/*   33 */     this.m_ldapManager = null;
/*      */ 
/*   41 */     this.m_userSearchRoot = null;
/*   42 */     this.m_groupSearchRoot = null;
/*      */ 
/*   47 */     this.m_searchCriteria = "(&(objectclass=person)(uid=<user>))";
/*      */ 
/*   52 */     this.m_groupFilter = "(&(objectclass=groupofuniquenames)(uniquemember=<user>))";
/*      */ 
/*   59 */     this.m_validateDnRoot = false;
/*      */ 
/*   61 */     this.m_returnSearchAttributes = false;
/*      */ 
/*   63 */     this.m_doAccountCharMap = false;
/*   64 */     this.m_AccountCharMapSource = '%';
/*   65 */     this.m_AccountCharMapTarget = '/';
/*      */   }
/*      */ 
/*      */   public void init(Provider provider)
/*      */     throws DataException
/*      */   {
/*   72 */     this.m_provider = provider;
/*      */ 
/*   74 */     this.m_ldapManager = new ProviderPoolManager();
/*   75 */     this.m_ldapManager.init(provider);
/*   76 */     this.m_ldapManager.setForceSync(false);
/*      */ 
/*   78 */     debug("LdapProvider.init()");
/*      */ 
/*   80 */     DataBinder provData = this.m_provider.getProviderData();
/*   81 */     this.m_root = provData.get("LdapSuffix");
/*   82 */     if (this.m_root == null)
/*      */     {
/*   84 */       throw new DataException("!csLdapConfigurationErrorMsg");
/*      */     }
/*   86 */     debug(new StringBuilder().append("LdapSuffix: ").append(this.m_root).toString());
/*      */ 
/*   88 */     String newSearchCriteria = provData.getAllowMissing("LdapUserSearchFilter");
/*   89 */     if ((newSearchCriteria != null) && (newSearchCriteria.length() != 0))
/*      */     {
/*   91 */       this.m_searchCriteria = newSearchCriteria.trim();
/*      */     }
/*   93 */     debug(new StringBuilder().append("LdapUserSearchFilter: ").append(this.m_searchCriteria).toString());
/*      */ 
/*   95 */     this.m_userSearchRoot = this.m_root;
/*   96 */     String newUserSearchRoot = provData.getAllowMissing("LdapUserSearchRoot");
/*   97 */     if ((newUserSearchRoot != null) && (newUserSearchRoot.length() != 0))
/*      */     {
/*   99 */       this.m_userSearchRoot = newUserSearchRoot.trim();
/*      */     }
/*  101 */     debug(new StringBuilder().append("LdapUserSearchRoot: ").append(this.m_userSearchRoot).toString());
/*      */ 
/*  103 */     String newGroupFilter = provData.getAllowMissing("LdapGroupSearchFilter");
/*  104 */     if ((newGroupFilter != null) && (newGroupFilter.length() != 0))
/*      */     {
/*  106 */       this.m_groupFilter = newGroupFilter.trim();
/*      */     }
/*  108 */     debug(new StringBuilder().append("LdapGroupSearchFilter: ").append(this.m_groupFilter).toString());
/*      */ 
/*  110 */     this.m_groupSearchRoot = this.m_root;
/*  111 */     String newGroupSearchRoot = provData.getAllowMissing("LdapGroupSearchRoot");
/*  112 */     if ((newGroupSearchRoot != null) && (newGroupSearchRoot.length() != 0))
/*      */     {
/*  114 */       this.m_groupSearchRoot = newGroupSearchRoot.trim();
/*      */     }
/*  116 */     debug(new StringBuilder().append("LdapGroupSearchRoot: ").append(this.m_groupSearchRoot).toString());
/*      */ 
/*  118 */     this.m_prefixList = new IdcVector();
/*  119 */     String prefixStr = provData.getLocal("RolePrefix");
/*  120 */     if ((prefixStr == null) || (prefixStr.length() == 0))
/*      */     {
/*  123 */       prefixStr = provData.getLocal("LdapRolePrefix");
/*      */     }
/*  125 */     debug(new StringBuilder().append("RolePrefix: ").append(prefixStr).toString());
/*      */ 
/*  127 */     Vector prefixList = StringUtils.parseArray(prefixStr, ';', '^');
/*  128 */     for (int i = 0; i < prefixList.size(); ++i)
/*      */     {
/*  130 */       String prefix = (String)prefixList.elementAt(i);
/*  131 */       if (prefix.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  135 */       LdapGroupPrefix p = new LdapGroupPrefix(prefix, "role");
/*  136 */       this.m_prefixList.addElement(p);
/*      */     }
/*      */ 
/*  139 */     prefixStr = provData.getLocal("AcctPrefix");
/*  140 */     if ((prefixStr == null) || (prefixStr.length() == 0))
/*      */     {
/*  143 */       prefixStr = provData.getLocal("LdapAccountPrefix");
/*      */     }
/*  145 */     debug(new StringBuilder().append("AccountPrefix: ").append(prefixStr).toString());
/*      */ 
/*  147 */     prefixList = StringUtils.parseArray(prefixStr, ';', '^');
/*  148 */     for (int i = 0; i < prefixList.size(); ++i)
/*      */     {
/*  150 */       String prefix = (String)prefixList.elementAt(i);
/*  151 */       if (prefix.length() == 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  155 */       LdapGroupPrefix p = new LdapGroupPrefix(prefix, "account");
/*  156 */       this.m_prefixList.addElement(p);
/*      */     }
/*      */ 
/*  159 */     this.m_validateDnRoot = DataBinderUtils.getBoolean(provData, "LdapValidateDnRoots", false);
/*  160 */     this.m_returnSearchAttributes = DataBinderUtils.getBoolean(provData, "LdapReturnAllSearchAttributes", false);
/*      */ 
/*  162 */     this.m_doAccountCharMap = DataBinderUtils.getBoolean(provData, "DoAccountCharMap", false);
/*  163 */     debug(new StringBuilder().append("DoAccountCharMap: ").append(this.m_doAccountCharMap).toString());
/*      */ 
/*  165 */     String newMapSource = provData.getAllowMissing("AccountCharMapSource");
/*  166 */     if ((newMapSource != null) && (newMapSource.length() != 0))
/*      */     {
/*  168 */       if (newMapSource.length() != 1)
/*      */       {
/*  170 */         debug(new StringBuilder().append("AccountCharMapSource must be 1 character, using default instead. Read: '").append(newMapSource).append("']").toString());
/*      */       }
/*      */       else
/*      */       {
/*  174 */         this.m_AccountCharMapSource = newMapSource.charAt(0);
/*      */       }
/*      */     }
/*  177 */     debug(new StringBuilder().append("AccountCharMapSource: ").append(this.m_AccountCharMapSource).toString());
/*      */ 
/*  179 */     String newMapTarget = provData.getAllowMissing("AccountCharMapTarget");
/*  180 */     if ((newMapTarget != null) && (newMapTarget.length() != 0))
/*      */     {
/*  182 */       if (newMapTarget.length() != 1)
/*      */       {
/*  184 */         debug(new StringBuilder().append("AccountCharMapSource must be 1 character, using default instead. Read: '").append(newMapTarget).append("']").toString());
/*      */       }
/*      */       else
/*      */       {
/*  188 */         this.m_AccountCharMapTarget = newMapTarget.charAt(0);
/*      */       }
/*      */     }
/*  191 */     debug(new StringBuilder().append("AccountCharMapTarget: ").append(this.m_AccountCharMapTarget).toString());
/*      */   }
/*      */ 
/*      */   public void startProvider()
/*      */     throws DataException, ServiceException
/*      */   {
/*  203 */     DataBinder providerData = this.m_provider.getProviderData();
/*  204 */     int numConnections = NumberUtils.parseInteger(providerData.getLocal("NumConnections"), 3);
/*      */ 
/*  206 */     for (int i = 0; i < numConnections; ++i)
/*      */     {
/*  211 */       this.m_ldapManager.addConnectionToPool();
/*      */     }
/*  213 */     debug("LdapProvider.startProvider()");
/*      */   }
/*      */ 
/*      */   public void stopProvider()
/*      */   {
/*  222 */     this.m_ldapManager.cleanUp();
/*  223 */     debug("LdapProvider.stopProvider()");
/*      */   }
/*      */ 
/*      */   public Provider getProvider()
/*      */   {
/*  228 */     return this.m_provider;
/*      */   }
/*      */ 
/*      */   public String getReportString(String key)
/*      */   {
/*  236 */     if (key.equals("startup"))
/*      */     {
/*  238 */       return "!csLdapStartedProvider";
/*      */     }
/*  240 */     return "";
/*      */   }
/*      */ 
/*      */   public ProviderConfig createProviderConfig()
/*      */     throws DataException
/*      */   {
/*  249 */     return (ProviderConfig)this.m_provider.createClass("ProviderConfig", "intradoc.provider.ProviderConfigImpl");
/*      */   }
/*      */ 
/*      */   public void testConnection(DataBinder binder, ExecutionContext ctxt)
/*      */     throws DataException, ServiceException
/*      */   {
/*  262 */     debug("Testing the connection.");
/*      */ 
/*  266 */     if (this.m_ldapManager.getAllConnections().size() <= 0)
/*      */     {
/*  268 */       debug("Attempting to restart provider.");
/*  269 */       startProvider();
/*  270 */       if (this.m_ldapManager.getAllConnections().size() <= 0)
/*      */       {
/*  274 */         throw new ServiceException("csLdapConnetionInError");
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*  279 */     ProviderConnection con = (ProviderConnection)getLdapConnection();
/*  280 */     if (con.isBadConnection())
/*      */     {
/*  282 */       debug("Connection is bad.");
/*  283 */       String errMsg = ((LdapConnectionInterface)con).getLastErrorMessage();
/*  284 */       throw new ServiceException(errMsg);
/*      */     }
/*  286 */     LdapConnectionInterface idcCon = (LdapConnectionInterface)con;
/*  287 */     Hashtable results = idcCon.read(this.m_root);
/*      */     Enumeration e;
/*  288 */     if (SystemUtils.m_verbose)
/*      */     {
/*  290 */       debug(new StringBuilder().append(this.m_root).append(" Attributes:").toString());
/*  291 */       for (e = results.keys(); e.hasMoreElements(); )
/*      */       {
/*  293 */         String key = (String)e.nextElement();
/*  294 */         if (key.equals("dn")) {
/*      */           continue;
/*      */         }
/*      */ 
/*  298 */         Vector v = (Vector)results.get(key);
/*  299 */         if ((v != null) && (v.size() > 0))
/*      */         {
/*  301 */           for (int i = 0; i < v.size(); ++i)
/*      */           {
/*  303 */             debug(new StringBuilder().append(key).append(": ").append(v.elementAt(i)).toString());
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  308 */     debug("Finished testing the connection");
/*      */   }
/*      */ 
/*      */   public void pollConnectionState(DataBinder provData, Properties provState)
/*      */   {
/*  321 */     Vector connections = this.m_ldapManager.getAllConnections();
/*  322 */     int num = connections.size();
/*  323 */     int numGood = 0;
/*  324 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  326 */       ProviderConnection connection = (ProviderConnection)connections.elementAt(i);
/*  327 */       if (connection.isBadConnection())
/*      */         continue;
/*  329 */       ++numGood;
/*      */     }
/*      */ 
/*  333 */     String str = LocaleUtils.encodeMessage("csJdbcConnectionGoodMessage", null, new StringBuilder().append("").append(numGood).toString(), new StringBuilder().append("").append(num).toString());
/*      */ 
/*  335 */     provState.put("ConnectionState", str);
/*  336 */     if (numGood >= num)
/*      */       return;
/*  338 */     provState.put("IsBadConnection", "1");
/*      */   }
/*      */ 
/*      */   public boolean checkSynchronization(Properties userProps)
/*      */   {
/*  355 */     boolean doCheck = false;
/*  356 */     String dn = userProps.getProperty("dUserOrgPath");
/*  357 */     if (dn != null)
/*      */     {
/*  361 */       doCheck = !matchSuffix(dn, this.m_root);
/*      */     }
/*  363 */     debug(new StringBuilder().append("LdapProvider.checkSynchronozation() returning ").append((doCheck) ? "true" : "false").toString());
/*  364 */     return doCheck;
/*      */   }
/*      */ 
/*      */   public void checkCredentials(UserData userData, DataBinder inBinder, boolean isLoadAttributes)
/*      */     throws ServiceException
/*      */   {
/*  401 */     debug("LdapProvider.checkCredentials() started");
/*  402 */     long startTime = 0L;
/*  403 */     if (SystemUtils.m_verbose)
/*      */     {
/*  405 */       startTime = System.currentTimeMillis();
/*      */     }
/*      */ 
/*  408 */     if (userData == null)
/*      */     {
/*  410 */       debug("UserData object null.  Returning.");
/*  411 */       return;
/*      */     }
/*      */ 
/*  414 */     String user = userData.m_name;
/*      */ 
/*  420 */     if (user.equals("anonymous"))
/*      */     {
/*  422 */       debug("Anonymous user.  Returning.");
/*  423 */       return;
/*      */     }
/*      */ 
/*  426 */     debug(new StringBuilder().append("user: ").append(user).toString());
/*      */ 
/*  428 */     boolean authenticateUser = false;
/*  429 */     if (inBinder != null)
/*      */     {
/*  431 */       authenticateUser = StringUtils.convertToBool(inBinder.getLocal("authenticateUser"), false);
/*      */     }
/*  433 */     boolean hasAuthenticatedUser = false;
/*  434 */     debug(new StringBuilder().append("authenticateUser: ").append(authenticateUser).toString());
/*      */ 
/*  436 */     boolean loadExtendedInfo = false;
/*  437 */     if ((userData.m_isExpired) || (!StringUtils.convertToBool(userData.getProperty("hasExtendedInfo"), false)))
/*      */     {
/*  440 */       debug("User is new to this provider.");
/*  441 */       loadExtendedInfo = true;
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/*  448 */       String dn = userData.getProperty("dUserOrgPath");
/*  449 */       debug(new StringBuilder().append("DN ").append((dn == null) ? "not" : dn).append(" found in local data.").toString());
/*      */ 
/*  451 */       if ((dn == null) || (dn.length() == 0))
/*      */       {
/*  453 */         debug("Retrieving DN from LDAP");
/*      */         try
/*      */         {
/*  459 */           dn = findDistinguishedName(user);
/*      */         }
/*      */         catch (ServiceException s)
/*      */         {
/*  463 */           debug(new StringBuilder().append("Error retrieving DN from LDAP: ").append(s.getMessage()).toString());
/*  464 */           if (authenticateUser)
/*      */           {
/*  469 */             throw s;
/*      */           }
/*      */         }
/*      */ 
/*  473 */         if (dn == null)
/*      */         {
/*  475 */           debug(new StringBuilder().append("DN for user ").append(user).append(" not found. Returning.").toString());
/*  476 */           throw new ServiceException(-16, new StringBuilder().append("DN for user ").append(user).append(" not found.").toString());
/*      */         }
/*      */ 
/*  480 */         debug(new StringBuilder().append("DN ").append(dn).append(" found for user ").append(user).toString());
/*      */ 
/*  484 */         userData.setProperty("dUserOrgPath", dn);
/*  485 */         if (inBinder != null)
/*      */         {
/*  487 */           inBinder.putLocal("dUserOrgPath", dn);
/*      */           try
/*      */           {
/*  498 */             String sourcePath = this.m_provider.m_providerData.get("SourcePath");
/*  499 */             String oldSourcePath = userData.getProperty("dUserSourceOrgPath");
/*  500 */             String oldOrgPath = inBinder.getAllowMissing("oldUserOrgPath");
/*      */ 
/*  502 */             if ((oldOrgPath != null) && (oldOrgPath.equals(dn)) && (oldSourcePath != null) && (oldSourcePath.equals(sourcePath)))
/*      */             {
/*  508 */               debug(new StringBuilder().append("DN ").append(dn).append(" already checked, returning.").toString());
/*  509 */               throw new ServiceException(-21, "Login failed.");
/*      */             }
/*      */ 
/*      */           }
/*      */           catch (DataException d)
/*      */           {
/*  518 */             debug("SourcePath missing from provider data. Cannot determine if this provider has been previously checked");
/*      */           }
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  524 */       if ((this.m_validateDnRoot) && (!matchSuffix(dn, this.m_root)))
/*      */       {
/*  526 */         debug(new StringBuilder().append("DN doesn't belong to ").append(this.m_root).append(". Returning.").toString());
/*  527 */         throw new ServiceException(-16, new StringBuilder().append("DN doesn't belong to ").append(this.m_root).append(". Returning.").toString());
/*      */       }
/*      */ 
/*  531 */       if (authenticateUser)
/*      */       {
/*  533 */         debug(new StringBuilder().append("Attempting to authenticate user ").append(user).toString());
/*  534 */         hasAuthenticatedUser = authenticateUser(dn, userData, inBinder);
/*  535 */         debug(new StringBuilder().append("hasAuthenticatedUser: ").append(hasAuthenticatedUser).toString());
/*      */       }
/*      */ 
/*  538 */       if ((loadExtendedInfo) || (isLoadAttributes))
/*      */       {
/*      */         try
/*      */         {
/*  543 */           loadExtendedInfo(dn, userData, inBinder);
/*      */ 
/*  546 */           userData.setProperty("hasExtendedInfo", "true");
/*  547 */           debug(new StringBuilder().append("Loaded extended info for user ").append(user).toString());
/*      */         }
/*      */         catch (ServiceException e)
/*      */         {
/*  561 */           debug(new StringBuilder().append("Unable to load extended info for ").append(dn).append(". Reason: ").append(e.getMessage()).toString());
/*  562 */           throw e;
/*      */         }
/*      */ 
/*      */       }
/*      */       else {
/*  567 */         debug("Using cached version of extended info");
/*      */       }
/*      */ 
/*  570 */       if (isLoadAttributes)
/*      */       {
/*  572 */         debug(new StringBuilder().append("Loading Attributes for user ").append(user).toString());
/*  573 */         boolean attributesLoaded = false;
/*      */ 
/*  576 */         loadSecurityInfo(dn, userData, inBinder);
/*      */ 
/*  578 */         attributesLoaded = true;
/*      */ 
/*  580 */         if (attributesLoaded)
/*      */         {
/*  582 */           if (inBinder != null)
/*      */           {
/*  584 */             inBinder.putLocal("hasSecurityInfo", "1");
/*      */           }
/*  586 */           userData.m_hasAttributesLoaded = attributesLoaded;
/*      */ 
/*  591 */           userData.setProperty("lastLoadedTs", String.valueOf(System.currentTimeMillis()));
/*      */         }
/*  593 */         debug(new StringBuilder().append("Attributes ").append((attributesLoaded) ? "" : "not").append("loaded").toString());
/*      */       }
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/*      */       Properties provState;
/*      */       long endTime;
/*      */       double total;
/*      */       IdcNumberFormat fmt;
/*  605 */       if ((authenticateUser) && (inBinder != null))
/*      */       {
/*  607 */         inBinder.putLocal("hasAuthenticatedUser", String.valueOf(hasAuthenticatedUser));
/*      */       }
/*      */ 
/*  610 */       Properties provState = this.m_provider.getProviderState();
/*  611 */       provState.put("LastActivityTs", String.valueOf(System.currentTimeMillis()));
/*  612 */       releaseConnection();
/*  613 */       if (SystemUtils.m_verbose)
/*      */       {
/*  615 */         long endTime = System.currentTimeMillis();
/*  616 */         double total = (endTime - startTime) / 1000.0D;
/*  617 */         IdcNumberFormat fmt = new IdcNumberFormat();
/*  618 */         fmt.setDecimalSeparatorAlwaysShown(true);
/*  619 */         fmt.setMaximumFractionDigits(5);
/*  620 */         fmt.setMinimumFractionDigits(5);
/*  621 */         debug(new StringBuilder().append("LdapProvider.checkCredentials() finished in ").append(total).append(" seconds.").toString());
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   protected void loadExtendedInfo(String dn, UserData userData, DataBinder inBinder)
/*      */     throws ServiceException
/*      */   {
/*  637 */     LdapConnectionInterface con = getLdapConnection();
/*      */ 
/*  639 */     if (con == null)
/*      */     {
/*  641 */       SystemUtils.trace("userstorage", "loadExtendedInfo, unable to get LDAP connection");
/*  642 */       throw new ServiceException("csLdapNoConnectionsAvailableMsg");
/*      */     }
/*      */ 
/*  645 */     Hashtable attrs = con.read(dn);
/*  646 */     if (attrs == null)
/*      */     {
/*  648 */       return;
/*      */     }
/*      */ 
/*  651 */     String extendedInfoStr = null;
/*  652 */     DataBinder userBinder = null;
/*  653 */     if (inBinder != null)
/*      */     {
/*  655 */       extendedInfoStr = inBinder.getLocal("userExtendedInfo");
/*  656 */       userBinder = new DataBinder();
/*  657 */       UserUtils.unpackageExtendedInfo(extendedInfoStr, userBinder);
/*      */     }
/*  659 */     DataBinder providerData = this.m_provider.getProviderData();
/*  660 */     DataResultSet localeConfig = SharedObjects.getTable("LanguageLocaleMap");
/*      */ 
/*  662 */     boolean usePrefLang = DataBinderUtils.getLocalBoolean(providerData, "UsePreferredLanguageAttribute", true);
/*      */ 
/*  664 */     boolean useLangCodes = DataBinderUtils.getLocalBoolean(providerData, "UseAttributeLanguageCodes", true);
/*      */ 
/*  666 */     boolean clearMissing = DataBinderUtils.getLocalBoolean(providerData, "ClearMissingAttributes", false);
/*      */ 
/*  668 */     boolean allowMultiValuedAttrs = DataBinderUtils.getLocalBoolean(providerData, "AllowMultiValuedAttributes", true);
/*      */ 
/*  672 */     IdcLocale locale = null;
/*  673 */     String localeStr = userData.getProperty("dUserLocale");
/*      */ 
/*  675 */     Vector langPref = null;
/*      */ 
/*  678 */     for (Enumeration e = attrs.keys(); e.hasMoreElements(); )
/*      */     {
/*  680 */       String key = (String)e.nextElement();
/*  681 */       if (key.equalsIgnoreCase("preferredLanguage"))
/*      */       {
/*  683 */         langPref = (Vector)attrs.get(key);
/*  684 */         debug(new StringBuilder().append("found 'preferredLanguage' as: '").append(key).append("', value is: '").append(langPref.elementAt(0)).toString());
/*  685 */         break;
/*      */       }
/*      */     }
/*      */ 
/*  689 */     if ((usePrefLang) && (langPref != null))
/*      */     {
/*  691 */       String langID = (String)langPref.elementAt(0);
/*      */ 
/*  694 */       langID = langID.replace('-', '.');
/*      */ 
/*  696 */       Vector v = localeConfig.findRow(0, langID);
/*  697 */       if (v != null)
/*      */       {
/*  699 */         DataResultSet drset = SharedObjects.getTable("UserMetaDefinition");
/*      */         try
/*      */         {
/*  702 */           String overrideBitStr = ResultSetUtils.findValue(drset, "umdName", "dUserLocale", "umdOverrideBitFlag");
/*      */ 
/*  704 */           int overrideBit = NumberUtils.parseInteger(overrideBitStr, 0);
/*      */ 
/*  706 */           int userOverrideBits = NumberUtils.parseInteger(userData.getProperty("dUserSourceFlags"), 0);
/*      */ 
/*  709 */           if ((overrideBit & userOverrideBits) == 0)
/*      */           {
/*  711 */             localeStr = (String)v.elementAt(1);
/*      */           }
/*      */         }
/*      */         catch (DataException d)
/*      */         {
/*  716 */           throw new ServiceException(LocaleUtils.encodeMessage("syColumnDoesNotExist", null, "umdName"));
/*      */         }
/*      */ 
/*      */       }
/*      */       else
/*      */       {
/*  722 */         debug(new StringBuilder().append("Locale not found for language ID: '").append(langID).append("'.").toString());
/*      */       }
/*      */     }
/*      */ 
/*  726 */     if ((localeStr != null) && (localeStr.length() > 0))
/*      */     {
/*  728 */       debug(new StringBuilder().append("User locale: '").append(localeStr).append("'").toString());
/*  729 */       locale = LocaleResources.getLocale(localeStr);
/*      */     }
/*      */ 
/*  732 */     if (locale == null)
/*      */     {
/*  734 */       locale = LocaleResources.getSystemLocale();
/*      */     }
/*      */ 
/*  737 */     userBinder.putLocal("dUserLocale", locale.m_name);
/*      */ 
/*  739 */     String attrMap = providerData.getLocal("AttributeMap");
/*  740 */     if ((attrMap == null) || (attrMap.length() == 0))
/*      */     {
/*  742 */       attrMap = "mail:dEmail;cn:dFullName;title:dUserType";
/*      */     }
/*      */ 
/*  745 */     debug(new StringBuilder().append("Mapping attributes using map: '").append(attrMap).append("'").toString());
/*  746 */     debug(new StringBuilder().append("  raw attributes: '").append(attrs).append("'").toString());
/*  747 */     Vector attrsV = StringUtils.parseArray(attrMap, ';', '^');
/*  748 */     for (int i = 0; i < attrsV.size(); ++i)
/*      */     {
/*  750 */       String lookup = (String)attrsV.elementAt(i);
/*  751 */       int index = lookup.indexOf(":");
/*  752 */       if (index < 0) {
/*      */         continue;
/*      */       }
/*      */ 
/*  756 */       String target = lookup.substring(index + 1);
/*  757 */       target = target.trim();
/*  758 */       lookup = lookup.substring(0, index);
/*  759 */       lookup = lookup.trim();
/*      */ 
/*  761 */       Vector tmp = (Vector)attrs.get(lookup);
/*      */ 
/*  763 */       if (tmp != null)
/*      */       {
/*  765 */         debug(new StringBuilder().append("  looked for: '").append(lookup).append("', found; value is: '").append(tmp.elementAt(0)).append("'").toString());
/*      */       }
/*      */       else
/*      */       {
/*  769 */         debug(new StringBuilder().append("  looked for: '").append(lookup).append("', not found").toString());
/*      */       }
/*      */ 
/*  772 */       if (useLangCodes)
/*      */       {
/*  776 */         String langStr = locale.m_languageId.replace('.', '-');
/*      */ 
/*  780 */         String localeKey = new StringBuilder().append(lookup).append(";lang-").append(langStr).toString();
/*  781 */         Vector localeAttr = (Vector)attrs.get(localeKey);
/*  782 */         if (localeAttr != null)
/*      */         {
/*  784 */           tmp = localeAttr;
/*  785 */           debug(new StringBuilder().append("  looked for: '").append(localeKey).append("', found; value is: '").append(tmp.elementAt(0)).append("'").toString());
/*      */         }
/*      */         else
/*      */         {
/*  789 */           debug(new StringBuilder().append("  looked for: '").append(localeKey).append("', not found;").toString());
/*      */         }
/*      */       }
/*      */ 
/*  793 */       String value = null;
/*  794 */       if (tmp != null)
/*      */       {
/*  800 */         if (lookup.equalsIgnoreCase("preferredlanguage"))
/*      */         {
/*  802 */           String langID = (String)tmp.elementAt(0);
/*      */ 
/*  806 */           langID = langID.replace('-', '.');
/*      */ 
/*  808 */           Vector v = localeConfig.findRow(0, langID);
/*  809 */           if (v != null)
/*      */           {
/*  811 */             tmp.setElementAt(v.elementAt(1), 0);
/*      */           }
/*      */           else
/*      */           {
/*  815 */             debug(new StringBuilder().append("While mapping preferredLanuage and attempting to map the value to lcLocaleId, lcLanguageId '").append(langID).append("' not found.").toString());
/*      */           }
/*      */ 
/*      */         }
/*      */ 
/*  822 */         for (int j = 0; j < tmp.size(); ++j)
/*      */         {
/*  824 */           String newVal = (String)tmp.elementAt(j);
/*  825 */           if (value == null)
/*      */           {
/*  827 */             value = newVal;
/*      */           } else {
/*  829 */             if (!allowMultiValuedAttrs)
/*      */               continue;
/*  831 */             value = new StringBuilder().append(value).append(",").append(newVal).toString();
/*      */           }
/*      */         }
/*      */ 
/*  835 */         int maxLength = SharedObjects.getEnvironmentInt(new StringBuilder().append(target).append(":maxLength").toString(), -1);
/*  836 */         if ((maxLength >= 0) && (value != null) && (value.length() > maxLength))
/*      */         {
/*  838 */           String warningMsg = LocaleUtils.encodeMessage("wwLdapAttributeTooLargeMsg", null, value, target, new StringBuilder().append(maxLength).append("").toString());
/*      */ 
/*  840 */           Report.warning(null, null, "wwLdapAttributeTooLargeMsg", new Object[] { value, target, new StringBuilder().append(maxLength).append("").toString() });
/*      */ 
/*  842 */           debug(LocaleResources.localizeMessage(warningMsg, null));
/*  843 */           continue;
/*      */         }
/*      */       }
/*  846 */       else if (clearMissing)
/*      */       {
/*  848 */         value = "";
/*      */       }
/*      */ 
/*  851 */       if (value == null)
/*      */         continue;
/*  853 */       if (inBinder != null)
/*      */       {
/*  855 */         debug(new StringBuilder().append("    setting: '").append(target).append("' = '").append(value).append("'").toString());
/*  856 */         userBinder.putLocal(target, value);
/*  857 */         inBinder.putLocal(target, value);
/*      */       }
/*  859 */       if (userData == null) {
/*      */         continue;
/*      */       }
/*      */ 
/*  863 */       userData.setProperty(target, value);
/*      */     }
/*      */ 
/*  868 */     if (inBinder == null)
/*      */       return;
/*      */     try
/*      */     {
/*  872 */       IdcCharArrayWriter sw = new IdcCharArrayWriter();
/*  873 */       userBinder.send(sw);
/*  874 */       extendedInfoStr = sw.toStringRelease();
/*      */     }
/*      */     catch (IOException ignore)
/*      */     {
/*  878 */       if (SystemUtils.m_verbose)
/*      */       {
/*  880 */         Report.debug("system", null, ignore);
/*      */       }
/*      */     }
/*  883 */     inBinder.putLocal("userExtendedInfo", extendedInfoStr);
/*      */   }
/*      */ 
/*      */   public boolean authenticateUser(String dn, UserData userData, DataBinder binder)
/*      */     throws ServiceException
/*      */   {
/*  890 */     String pswrd = null;
/*      */ 
/*  892 */     if (binder != null)
/*      */     {
/*  894 */       debug("Using password from inBinder.");
/*  895 */       pswrd = binder.getLocal("userPassword");
/*      */     }
/*      */ 
/*  898 */     if (pswrd == null)
/*      */     {
/*  900 */       debug("Using password from userData.");
/*  901 */       pswrd = userData.getProperty("dPassword");
/*      */     }
/*      */ 
/*  905 */     LdapConnectionInterface con = getLdapConnection();
/*      */ 
/*  907 */     return con.authenticate(dn, pswrd);
/*      */   }
/*      */ 
/*      */   protected void loadSecurityInfo(String dn, UserData userData, DataBinder inBinder)
/*      */     throws ServiceException
/*      */   {
/*  913 */     DataBinder providerData = this.m_provider.getProviderData();
/*      */ 
/*  926 */     boolean useFullGroupNameGlobal = false;
/*  927 */     useFullGroupNameGlobal = StringUtils.convertToBool(providerData.getLocal("UseFullGroupNames"), false);
/*      */ 
/*  929 */     useFullGroupNameGlobal = StringUtils.convertToBool(providerData.getLocal("UseFullGroupName"), false);
/*      */ 
/*  931 */     debug(new StringBuilder().append("UseFullGroupName ").append((useFullGroupNameGlobal) ? "true" : "false").toString());
/*      */ 
/*  933 */     boolean filterGroups = StringUtils.convertToBool(providerData.getLocal("UseGroupFilter"), false);
/*      */ 
/*  935 */     debug(new StringBuilder().append("UseGroupFilter ").append((filterGroups) ? "true" : "false").toString());
/*      */ 
/*  937 */     Vector groups = retrieveGroups(dn);
/*      */ 
/*  939 */     int numGroups = groups.size();
/*  940 */     if (numGroups == 0)
/*      */     {
/*  942 */       debug(new StringBuilder().append("No groups found for user ").append(dn).toString());
/*      */     }
/*      */ 
/*  945 */     Vector roles = new IdcVector();
/*  946 */     Vector accounts = new IdcVector();
/*      */ 
/*  948 */     boolean isAdmin = false;
/*      */ 
/*  950 */     for (int i = 0; i < numGroups; ++i)
/*      */     {
/*  952 */       boolean isValid = true;
/*  953 */       boolean isRole = true;
/*  954 */       boolean useFullGroupNameForPrefix = false;
/*      */ 
/*  957 */       String group = (String)groups.elementAt(i);
/*  958 */       Vector groupElts = StringUtils.parseArrayEx(group, ',', '^', true);
/*  959 */       group = StringUtils.createStringRemoveEmpty(groupElts, ',', '^');
/*      */ 
/*  961 */       debug(new StringBuilder().append("Checking group ").append(group).toString());
/*      */ 
/*  965 */       if ((this.m_validateDnRoot) && (!matchSuffix(group, this.m_root)))
/*      */       {
/*  967 */         debug(new StringBuilder().append("Group \"").append(group).append("\" does not end with root \"").append(this.m_root).append("\"").toString());
/*      */       }
/*      */       else
/*      */       {
/*  971 */         if (filterGroups)
/*      */         {
/*  973 */           isValid = false;
/*      */ 
/*  976 */           for (int j = 0; j < this.m_prefixList.size(); ++j)
/*      */           {
/*  978 */             LdapGroupPrefix prefix = (LdapGroupPrefix)this.m_prefixList.elementAt(j);
/*      */ 
/*  980 */             if (prefix.m_value.length() == 0)
/*      */             {
/*  982 */               debug("Group prefix is empty. Skipping");
/*      */             }
/*      */             else
/*      */             {
/*  987 */               int index = group.toLowerCase().indexOf(prefix.m_value.toLowerCase());
/*  988 */               if (index < 0)
/*      */               {
/*  990 */                 debug(new StringBuilder().append(group).append(" does not match prefix ").append(prefix.m_value).toString());
/*      */               }
/*      */               else {
/*  993 */                 debug(new StringBuilder().append(group).append(" matches prefix ").append(prefix.m_value).toString());
/*      */ 
/*  998 */                 group = group.substring(0, index - 1);
/*  999 */                 int depth = StringUtils.parseArray(group, ',', '^').size() - 1;
/* 1000 */                 if (depth <= prefix.m_allowedDepth)
/*      */                 {
/* 1002 */                   isValid = true;
/*      */ 
/* 1006 */                   useFullGroupNameForPrefix = prefix.m_useFullGroupName;
/* 1007 */                   isRole = prefix.m_type.equals("role");
/* 1008 */                   break;
/*      */                 }
/* 1010 */                 debug(new StringBuilder().append(group).append(" is nested to deep to be allowed as a group.").toString());
/* 1011 */                 debug(new StringBuilder().append("\tChange the depth value for the prefix \"").append(prefix.m_value).append("\" to allow this group").toString());
/*      */               }
/*      */             }
/*      */           }
/*      */         }
/* 1015 */         if (!isValid)
/*      */           continue;
/* 1017 */         boolean useFullName = useFullGroupNameForPrefix & useFullGroupNameGlobal;
/* 1018 */         group = collapseName(group, "/", useFullName);
/* 1019 */         if (isRole)
/*      */         {
/* 1021 */           if (group.equals("admin"))
/*      */           {
/* 1023 */             isAdmin = true;
/*      */           }
/* 1025 */           debug(new StringBuilder().append("Add role: ").append(group).toString());
/* 1026 */           roles.addElement(group);
/*      */         }
/*      */         else
/*      */         {
/* 1030 */           String ldapPrivDelim = providerData.getLocal("AcctPermDelim");
/* 1031 */           if ((ldapPrivDelim == null) || (ldapPrivDelim.length() == 0))
/*      */           {
/* 1033 */             ldapPrivDelim = "_";
/*      */           }
/* 1035 */           group = decodeAccountPrivileges(group, ldapPrivDelim);
/*      */ 
/* 1037 */           if (this.m_doAccountCharMap)
/*      */           {
/* 1039 */             debug(new StringBuilder().append("Doing account character map on: ").append(group).toString());
/* 1040 */             group = group.replace(this.m_AccountCharMapSource, this.m_AccountCharMapTarget);
/*      */           }
/*      */ 
/* 1043 */           debug(new StringBuilder().append("Add acct: ").append(group).toString());
/* 1044 */           accounts.addElement(group);
/*      */         }
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/*      */     try
/*      */     {
/* 1052 */       LdapConnectionInterface con = getLdapConnection();
/*      */ 
/* 1054 */       ExecutionContext cxt = new ExecutionContextAdaptor();
/* 1055 */       cxt.setCachedObject("FilterRoles", roles);
/* 1056 */       cxt.setCachedObject("FilterAccounts", accounts);
/* 1057 */       cxt.setCachedObject("UserData", userData);
/* 1058 */       cxt.setCachedObject("LdapConnection", con);
/* 1059 */       cxt.setCachedObject("dn", dn);
/*      */ 
/* 1061 */       int result = PluginFilters.filter("computeLdapCredentials", null, inBinder, cxt);
/*      */ 
/* 1063 */       if (result == -1)
/*      */       {
/* 1065 */         return;
/*      */       }
/*      */ 
/* 1068 */       int len = -1;
/* 1069 */       len = roles.size();
/* 1070 */       for (int i = 0; i < len; ++i)
/*      */       {
/* 1072 */         String role = (String)roles.elementAt(i);
/* 1073 */         if (!role.equals("admin"))
/*      */           continue;
/* 1075 */         isAdmin = true;
/*      */       }
/*      */ 
/*      */     }
/*      */     catch (DataException d)
/*      */     {
/* 1081 */       throw new ServiceException(d);
/*      */     }
/*      */ 
/* 1084 */     if (isAdmin)
/*      */     {
/* 1086 */       debug(new StringBuilder().append(dn).append(" is an admin.").toString());
/* 1087 */       accounts.addElement("#all");
/*      */     }
/* 1089 */     String defaultNetworkAccounts = providerData.getLocal("DefaultNetworkAccounts");
/* 1090 */     if ((defaultNetworkAccounts != null) && (defaultNetworkAccounts.length() != 0))
/*      */     {
/* 1092 */       debug(new StringBuilder().append("Adding default network account '").append(defaultNetworkAccounts).append("\" to ").append(dn).toString());
/* 1093 */       Vector defAccounts = StringUtils.parseArray(defaultNetworkAccounts, ',', '^');
/* 1094 */       for (int i = 0; i < defAccounts.size(); ++i)
/*      */       {
/* 1096 */         accounts.addElement(defAccounts.elementAt(i));
/*      */       }
/*      */     }
/*      */ 
/* 1100 */     String defaultNetworkRoles = providerData.getLocal("DefaultNetworkRoles");
/* 1101 */     if ((defaultNetworkRoles != null) && (defaultNetworkRoles.length() != 0))
/*      */     {
/* 1103 */       debug(new StringBuilder().append("Adding default network role '").append(defaultNetworkRoles).append("\" to ").append(dn).toString());
/* 1104 */       Vector defRoles = StringUtils.parseArray(defaultNetworkRoles, ',', '^');
/* 1105 */       for (int i = 0; i < defRoles.size(); ++i)
/*      */       {
/* 1107 */         roles.addElement(defRoles.elementAt(i));
/*      */       }
/*      */     }
/*      */ 
/* 1111 */     SecurityUtils.loadExternalSecurityAttributes(userData, "role", roles, null, true);
/* 1112 */     SecurityUtils.loadExternalSecurityAttributes(userData, "account", accounts, null, true);
/*      */   }
/*      */ 
/*      */   public static String decodeAccountPrivileges(String group, String delim)
/*      */   {
/* 1117 */     String result = group;
/* 1118 */     int index = group.indexOf(delim);
/* 1119 */     if (index > 0)
/*      */     {
/* 1121 */       String groupPart = group.substring(0, index);
/* 1122 */       int len = delim.length();
/* 1123 */       String permPart = group.substring(index + len);
/* 1124 */       result = new StringBuilder().append(groupPart).append("(").append(permPart).append(")").toString();
/*      */     }
/* 1126 */     return result;
/*      */   }
/*      */ 
/*      */   public static String collapseName(String name, String sep, boolean preserveNesting)
/*      */   {
/* 1131 */     StringBuffer result = new StringBuffer();
/* 1132 */     Vector parsedString = StringUtils.parseArray(name, ',', '^');
/*      */ 
/* 1134 */     if ((parsedString == null) || (parsedString.isEmpty()))
/*      */     {
/* 1136 */       return "";
/*      */     }
/*      */ 
/* 1139 */     boolean isFirst = true;
/* 1140 */     for (int i = 0; i < parsedString.size(); ++i)
/*      */     {
/* 1142 */       String elt = (String)parsedString.elementAt(i);
/* 1143 */       elt = elt.trim();
/* 1144 */       int index = elt.indexOf(61);
/* 1145 */       if (index >= 0)
/*      */       {
/* 1147 */         elt = elt.substring(index + 1);
/*      */       }
/*      */ 
/* 1151 */       if (elt.length() == 0)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1156 */       if (!isFirst)
/*      */       {
/* 1158 */         result.insert(0, sep);
/*      */       }
/*      */       else
/*      */       {
/* 1162 */         if (!preserveNesting)
/*      */         {
/* 1164 */           return elt;
/*      */         }
/* 1166 */         isFirst = false;
/*      */       }
/* 1168 */       result.insert(0, elt.trim());
/*      */     }
/* 1170 */     return result.toString();
/*      */   }
/*      */ 
/*      */   public static boolean matchSuffix(String target, String suffix)
/*      */   {
/* 1175 */     Vector targetParts = StringUtils.parseArray(target, ',', '^');
/* 1176 */     Vector suffixParts = StringUtils.parseArray(suffix, ',', '^');
/* 1177 */     int targetLen = targetParts.size();
/* 1178 */     int suffixLen = suffixParts.size();
/* 1179 */     boolean isMatch = true;
/*      */ 
/* 1181 */     if (targetLen < suffixLen)
/*      */     {
/* 1183 */       return false;
/*      */     }
/*      */ 
/* 1186 */     for (int i = 0; (i < targetLen) && (i < suffixLen); ++i)
/*      */     {
/* 1188 */       String targetVal = (String)targetParts.elementAt(targetLen - i - 1);
/* 1189 */       targetVal = targetVal.trim();
/* 1190 */       String suffixVal = (String)suffixParts.elementAt(suffixLen - i - 1);
/* 1191 */       suffixVal = suffixVal.trim();
/*      */ 
/* 1193 */       if (targetVal.equalsIgnoreCase(suffixVal))
/*      */         continue;
/* 1195 */       isMatch = false;
/*      */     }
/*      */ 
/* 1198 */     return isMatch;
/*      */   }
/*      */ 
/*      */   protected Vector retrieveGroups(String dn)
/*      */     throws ServiceException
/*      */   {
/* 1210 */     String groupFilter = null;
/* 1211 */     int index = this.m_groupFilter.indexOf("<user>");
/* 1212 */     if (index >= 0)
/*      */     {
/* 1214 */       groupFilter = new StringBuilder().append(this.m_groupFilter.substring(0, index)).append(StringUtils.encodeUrlStyle(dn, '\\', false)).toString();
/*      */ 
/* 1216 */       groupFilter = new StringBuilder().append(groupFilter).append(this.m_groupFilter.substring(index + "<user>".length())).toString();
/*      */     }
/*      */ 
/* 1219 */     debug(new StringBuilder().append("Searching for groups containing user ").append(dn).toString());
/* 1220 */     debug(new StringBuilder().append("\tUsing search filter ").append(groupFilter).toString());
/* 1221 */     LdapConnectionInterface con = getLdapConnection();
/* 1222 */     Vector results = null;
/* 1223 */     if (con instanceof ParameterizedLdapSearchInterface)
/*      */     {
/* 1225 */       String[] attrList = null;
/* 1226 */       if (!this.m_returnSearchAttributes)
/*      */       {
/* 1230 */         attrList = new String[] { "dn" };
/*      */       }
/* 1232 */       debug(new StringBuilder().append("\tSearching for groups based at DN ").append(this.m_groupSearchRoot).toString());
/* 1233 */       ParameterizedLdapSearchInterface searchCon = (ParameterizedLdapSearchInterface)con;
/*      */ 
/* 1235 */       results = searchCon.search(this.m_groupSearchRoot, groupFilter, attrList, null);
/*      */     }
/*      */     else
/*      */     {
/* 1240 */       results = con.search(groupFilter);
/*      */     }
/*      */ 
/* 1243 */     Vector attributes = new IdcVector();
/* 1244 */     for (int i = 0; i < results.size(); ++i)
/*      */     {
/* 1246 */       Hashtable attrs = (Hashtable)results.elementAt(i);
/* 1247 */       Vector attr = (Vector)attrs.get("dn");
/* 1248 */       String group = (String)attr.elementAt(0);
/* 1249 */       attributes.addElement(group);
/*      */     }
/* 1251 */     return attributes;
/*      */   }
/*      */ 
/*      */   public String findDistinguishedName(String user)
/*      */     throws ServiceException
/*      */   {
/* 1265 */     String queryFilter = null;
/* 1266 */     int index = this.m_searchCriteria.indexOf("<user>");
/* 1267 */     if (index >= 0)
/*      */     {
/* 1269 */       queryFilter = new StringBuilder().append(this.m_searchCriteria.substring(0, index)).append(StringUtils.encodeUrlStyle(user, '\\', false)).toString();
/*      */ 
/* 1271 */       queryFilter = new StringBuilder().append(queryFilter).append(this.m_searchCriteria.substring(index + "<user>".length())).toString();
/*      */     }
/*      */ 
/* 1274 */     debug(new StringBuilder().append("Searching for user ").append(user).append(" with filter ").append(queryFilter).toString());
/* 1275 */     LdapConnectionInterface con = getLdapConnection();
/*      */ 
/* 1277 */     Vector results = null;
/* 1278 */     if (con instanceof ParameterizedLdapSearchInterface)
/*      */     {
/* 1280 */       String[] attrList = null;
/* 1281 */       if (!this.m_returnSearchAttributes)
/*      */       {
/* 1285 */         attrList = new String[] { "dn" };
/*      */       }
/* 1287 */       debug(new StringBuilder().append("\tSearching for user based at DN ").append(this.m_userSearchRoot).toString());
/* 1288 */       ParameterizedLdapSearchInterface searchCon = (ParameterizedLdapSearchInterface)con;
/*      */ 
/* 1290 */       results = searchCon.search(this.m_userSearchRoot, queryFilter, attrList, null);
/*      */     }
/*      */     else
/*      */     {
/* 1295 */       results = con.search(queryFilter);
/*      */     }
/*      */ 
/* 1298 */     String dn = null;
/* 1299 */     if ((results != null) && (results.size() > 0))
/*      */     {
/* 1301 */       Hashtable attrs = (Hashtable)results.elementAt(0);
/* 1302 */       if (attrs != null)
/*      */       {
/* 1304 */         Vector attr = (Vector)attrs.get("dn");
/* 1305 */         dn = (String)attr.elementAt(0);
/*      */       }
/*      */     }
/*      */ 
/* 1309 */     if (dn != null)
/*      */     {
/* 1311 */       Vector v = parseArrayWithLeadingEscape(dn, ',', '\\', true);
/* 1312 */       dn = StringUtils.createString(v, ',', ',');
/*      */     }
/*      */ 
/* 1315 */     return dn;
/*      */   }
/*      */ 
/*      */   protected LdapConnectionInterface getLdapConnection()
/*      */     throws ServiceException
/*      */   {
/* 1324 */     Thread thrd = Thread.currentThread();
/* 1325 */     String name = thrd.getName();
/*      */ 
/* 1327 */     ProviderConnection con = null;
/*      */ 
/* 1333 */     if (this.m_ldapManager.getAllConnections().size() <= 0)
/*      */     {
/* 1335 */       throw new ServiceException("csLdapNoConnectionsAvailableMsg");
/*      */     }
/*      */     try
/*      */     {
/* 1339 */       con = this.m_ldapManager.getConnection(name);
/*      */     }
/*      */     catch (DataException d)
/*      */     {
/*      */     }
/*      */     finally
/*      */     {
/* 1347 */       if (con == null)
/*      */       {
/* 1349 */         releaseConnection(name);
/*      */       }
/*      */     }
/* 1352 */     this.m_provider.markState("active");
/*      */ 
/* 1354 */     return (LdapConnectionInterface)con;
/*      */   }
/*      */ 
/*      */   public void releaseConnection()
/*      */   {
/* 1364 */     Thread curThread = Thread.currentThread();
/* 1365 */     releaseConnection(curThread.getName());
/*      */   }
/*      */ 
/*      */   public void releaseConnection(String threadName)
/*      */   {
/* 1375 */     this.m_ldapManager.releaseConnection(threadName);
/*      */   }
/*      */ 
/*      */   protected void releaseAccess(ProviderConnection con)
/*      */   {
/* 1384 */     if (con == null)
/*      */       return;
/* 1386 */     this.m_ldapManager.releaseAccess(con, false);
/*      */   }
/*      */ 
/*      */   protected void debug(String msg)
/*      */   {
/* 1397 */     Report.trace("userstorage", msg, null);
/*      */   }
/*      */ 
/*      */   public Vector parseArrayWithLeadingEscape(String str, char sep, char esc, boolean doTrim)
/*      */   {
/* 1403 */     Vector strArray = new IdcVector();
/*      */ 
/* 1405 */     if ((str == null) || (str.length() == 0))
/*      */     {
/* 1407 */       return strArray;
/*      */     }
/*      */ 
/* 1410 */     StringBuffer buf = new StringBuffer();
/* 1411 */     int len = str.length();
/*      */ 
/* 1413 */     char prevChar = '\000';
/* 1414 */     for (int i = 0; i < len; ++i)
/*      */     {
/* 1416 */       char ch = str.charAt(i);
/* 1417 */       if ((ch == sep) && (prevChar != esc))
/*      */       {
/* 1420 */         strArray.addElement(buf.toString().trim());
/* 1421 */         buf = new StringBuffer();
/*      */       }
/*      */       else
/*      */       {
/* 1425 */         buf.append(ch);
/*      */       }
/* 1427 */       prevChar = ch;
/*      */     }
/*      */ 
/* 1430 */     strArray.addElement(buf.toString().trim());
/*      */ 
/* 1432 */     return strArray;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1437 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 90867 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.provider.LdapUserProvider
 * JD-Core Version:    0.5.4
 */