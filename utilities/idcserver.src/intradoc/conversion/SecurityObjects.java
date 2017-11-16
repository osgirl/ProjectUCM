/*      */ package intradoc.conversion;
/*      */ 
/*      */ import intradoc.common.AppObjectRepository;
/*      */ import intradoc.common.CommonDataConversion;
/*      */ import intradoc.common.CryptoCommonUtils;
/*      */ import intradoc.common.DateUtils;
/*      */ import intradoc.common.FileUtils;
/*      */ import intradoc.common.IdcMessageFactory;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptUtils;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.data.DataBinder;
/*      */ import intradoc.data.DataException;
/*      */ import intradoc.data.DataResultSet;
/*      */ import intradoc.data.DataSerialize;
/*      */ import intradoc.data.DataSerializeUtils;
/*      */ import intradoc.data.FieldInfo;
/*      */ import intradoc.data.MapParameters;
/*      */ import intradoc.data.Parameters;
/*      */ import intradoc.data.ResultSetUtils;
/*      */ import intradoc.resource.ResourceUtils;
/*      */ import intradoc.util.IdcMessage;
/*      */ import java.security.MessageDigest;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ import javax.crypto.Cipher;
/*      */ import javax.crypto.spec.SecretKeySpec;
/*      */ 
/*      */ public class SecurityObjects
/*      */ {
/*   36 */   public static int F_VALIDATE_ONLY = 1;
/*      */   protected boolean m_initialized;
/*      */   protected boolean m_pwdInitialized;
/*      */   protected boolean m_needsMasterKeys;
/*      */   protected Properties m_environment;
/*      */   protected KeyLoaderInterface m_keyLoader;
/*      */   protected String m_passwordDir;
/*      */   protected MessageDigest m_sha1Digest;
/*      */   protected DataResultSet m_masterKeyList;
/*      */   protected Map<String, Map> m_keyMap;
/*      */   protected Map<String, String> m_algorithmToKeyMap;
/*      */   protected Map<String, Cipher> m_cipherMap;
/*      */   protected Map<String, Map> m_passwordMap;
/*      */   protected Map<String, Map> m_fieldCategoryMap;
/*      */   protected List<String> m_categories;
/*   58 */   public static final String[] MASTER_KEY_COLUMNS = { "scKeyName", "scAlgorithm", "scIsActive", "scCreateTs", "scCategory", "scMasterKey" };
/*      */ 
/*   61 */   public static final String[] PASSWORD_COLUMNS = { "scPasswordField", "scPassword", "scPasswordEncoding", "scPasswordScope" };
/*      */ 
/*      */   public SecurityObjects()
/*      */   {
/*   38 */     this.m_initialized = false;
/*   39 */     this.m_pwdInitialized = false;
/*   40 */     this.m_needsMasterKeys = false;
/*      */ 
/*   44 */     this.m_keyLoader = null;
/*   45 */     this.m_passwordDir = null;
/*      */ 
/*   47 */     this.m_sha1Digest = null;
/*      */ 
/*   49 */     this.m_masterKeyList = null;
/*   50 */     this.m_keyMap = null;
/*   51 */     this.m_algorithmToKeyMap = null;
/*   52 */     this.m_cipherMap = null;
/*      */ 
/*   54 */     this.m_passwordMap = null;
/*   55 */     this.m_fieldCategoryMap = null;
/*   56 */     this.m_categories = null;
/*      */   }
/*      */ 
/*      */   public void init()
/*      */     throws ServiceException
/*      */   {
/*   66 */     if (this.m_initialized)
/*      */     {
/*   68 */       return;
/*      */     }
/*      */     try
/*      */     {
/*   72 */       this.m_keyMap = new Hashtable();
/*   73 */       this.m_algorithmToKeyMap = new Hashtable();
/*   74 */       this.m_cipherMap = new Hashtable();
/*      */ 
/*   76 */       this.m_sha1Digest = MessageDigest.getInstance("SHA-1");
/*      */ 
/*   78 */       Properties environment = this.m_environment;
/*   79 */       if (environment == null)
/*      */       {
/*   81 */         Report.deprecatedUsage("Using AppObjectRepository to get environment in SecurityObjects.");
/*      */ 
/*   83 */         environment = (Properties)AppObjectRepository.getObject("environment");
/*      */       }
/*      */ 
/*   86 */       String idcDir = environment.getProperty("IntradocDir");
/*   87 */       if (idcDir == null)
/*      */       {
/*   89 */         throw new ServiceException(null, "syParameterNotFound", new Object[] { "IntradocDir" });
/*      */       }
/*   91 */       String configDir = environment.getProperty("ConfigDir", idcDir + "/config");
/*      */ 
/*   93 */       String sdir = FileUtils.fileSlashes(configDir + "/private/");
/*   94 */       this.m_passwordDir = sdir;
/*   95 */       Map map = new HashMap();
/*   96 */       map.put("PrivateDirectory", sdir);
/*   97 */       Object skip = environment.getProperty("SkipCreatePrivateDirectoryForSecurity");
/*   98 */       if (skip != null)
/*      */       {
/*  100 */         map.put("SkipCreatePrivateDirectoryForSecurity", skip);
/*      */       }
/*      */ 
/*  106 */       String useCsfKeyLoaderStr = environment.getProperty("UseCsfKeyLoader");
/*  107 */       String loaderClass = "intradoc.conversion.StandardKeyLoader";
/*  108 */       String explicitLoaderClass = SystemUtils.getSystemPropertiesClone().getProperty("idc.conversion.keyloader");
/*  109 */       if ((useCsfKeyLoaderStr == null) || (useCsfKeyLoaderStr.length() == 0))
/*      */       {
/*  111 */         if (explicitLoaderClass != null)
/*      */         {
/*  113 */           loaderClass = explicitLoaderClass;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  118 */         boolean useCfsClass = StringUtils.convertToBool(useCsfKeyLoaderStr, true);
/*  119 */         if (useCfsClass)
/*      */         {
/*  121 */           loaderClass = (explicitLoaderClass != null) ? explicitLoaderClass : "idc.conversion.jps.CSFKeyLoader";
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/*  126 */       if (loaderClass == null)
/*      */       {
/*  128 */         loaderClass = "intradoc.conversion.StandardKeyLoader";
/*      */       }
/*  130 */       Class c = Class.forName(loaderClass);
/*  131 */       this.m_keyLoader = ((KeyLoaderInterface)c.newInstance());
/*  132 */       this.m_keyLoader.init(map);
/*  133 */       loadKeys();
/*      */ 
/*  135 */       this.m_initialized = true;
/*      */     }
/*      */     catch (Exception e)
/*      */     {
/*  139 */       String errMsg = LocaleUtils.encodeMessage("csInitSecurityError", null);
/*  140 */       throw new ServiceException(errMsg, e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean checkInit()
/*      */   {
/*  146 */     return (this.m_initialized) && (this.m_pwdInitialized);
/*      */   }
/*      */ 
/*      */   public boolean checkPasswordManagement()
/*      */   {
/*  151 */     return this.m_pwdInitialized;
/*      */   }
/*      */ 
/*      */   public boolean checkNeedsUpdating()
/*      */   {
/*  156 */     return this.m_needsMasterKeys;
/*      */   }
/*      */ 
/*      */   public boolean loadPasswordManagement(DataResultSet catSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  162 */     DataSerialize ds = DataSerializeUtils.getDataSerialize();
/*  163 */     if (ds == null)
/*      */     {
/*  168 */       String errMsg = LocaleUtils.encodeMessage("csInitCryptoPasswordError", "!csDataSerializeMissing");
/*      */ 
/*  170 */       throw new DataException(errMsg);
/*      */     }
/*  172 */     loadCategories(catSet);
/*      */ 
/*  174 */     this.m_passwordMap = new Hashtable();
/*  175 */     loadPasswords();
/*  176 */     this.m_pwdInitialized = true;
/*      */ 
/*  179 */     if ((this.m_keyLoader != null) && (this.m_keyLoader.canUpdateKeys()))
/*      */     {
/*  181 */       HashMap args = new HashMap();
/*  182 */       args.put("isTestingOnly", "1");
/*  183 */       updateExpiredKeys(args);
/*      */     }
/*  185 */     return true;
/*      */   }
/*      */ 
/*      */   public void loadCategories(DataResultSet drset)
/*      */   {
/*  190 */     Map map = new Hashtable();
/*  191 */     List categories = new ArrayList();
/*  192 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  194 */       Map rowMap = drset.getCurrentRowMap();
/*  195 */       String field = (String)rowMap.get("scCategoryField");
/*  196 */       map.put(field, rowMap);
/*      */ 
/*  198 */       String cat = (String)rowMap.get("scCategory");
/*  199 */       int index = categories.indexOf(cat);
/*  200 */       if (index >= 0)
/*      */         continue;
/*  202 */       categories.add(cat);
/*      */     }
/*      */ 
/*  206 */     this.m_fieldCategoryMap = map;
/*  207 */     this.m_categories = categories;
/*      */   }
/*      */ 
/*      */   public String determineCategory(String fieldName)
/*      */   {
/*  212 */     Map map = (Map)this.m_fieldCategoryMap.get(fieldName);
/*  213 */     if (map != null)
/*      */     {
/*  215 */       return (String)map.get("scCategory");
/*      */     }
/*  217 */     return fieldName;
/*      */   }
/*      */ 
/*      */   public Map determineCategoryFieldInfo(String fieldName)
/*      */   {
/*  222 */     if (this.m_fieldCategoryMap != null)
/*      */     {
/*  224 */       return (Map)this.m_fieldCategoryMap.get(fieldName);
/*      */     }
/*  226 */     return null;
/*      */   }
/*      */ 
/*      */   public String encrypt(PasswordInfo info)
/*      */     throws DataException
/*      */   {
/*  232 */     String result = null;
/*  233 */     if ((info.m_algorithm == null) || (info.m_algorithm.equals("ClearText")))
/*      */     {
/*  235 */       result = info.m_password;
/*      */     }
/*  237 */     else if (info.m_algorithm.equals("Intradoc"))
/*      */     {
/*  239 */       result = encryptDefault(info);
/*      */     }
/*      */     else
/*      */     {
/*  243 */       getKey(info);
/*  244 */       String key = info.m_key;
/*  245 */       if (key == null)
/*      */       {
/*  247 */         IdcMessage errMsg = IdcMessageFactory.lc("csEncryptKeyMissing", new Object[] { info.m_category, info.m_algorithm });
/*  248 */         throw new DataException(null, errMsg);
/*      */       }
/*  250 */       if (key.startsWith("["))
/*      */       {
/*  252 */         IdcMessage msg = IdcMessageFactory.lc("csEncryptKeyIncompatibleStorage", new Object[] { key, info.m_algorithm, info.m_field });
/*  253 */         throw new DataException(null, msg);
/*      */       }
/*      */       try
/*      */       {
/*  257 */         int[] length = { 0 };
/*  258 */         byte[] tmp = CommonDataConversion.uudecode(key, length);
/*  259 */         byte[] d = new byte[length[0]];
/*  260 */         System.arraycopy(tmp, 0, d, 0, length[0]);
/*  261 */         SecretKeySpec skeySpec = new SecretKeySpec(d, info.m_algorithm);
/*      */ 
/*  264 */         synchronized (this.m_cipherMap)
/*      */         {
/*  266 */           Cipher cipher = (Cipher)this.m_cipherMap.get(info.m_algorithm);
/*  267 */           if (cipher == null)
/*      */           {
/*  269 */             cipher = Cipher.getInstance(info.m_algorithm);
/*  270 */             this.m_cipherMap.put(info.m_algorithm, cipher);
/*      */           }
/*      */ 
/*  273 */           cipher.init(1, skeySpec);
/*  274 */           byte[] encrypted = cipher.doFinal(info.m_password.getBytes());
/*      */ 
/*  276 */           result = info.m_category + ":" + info.m_ts + ":" + CommonDataConversion.uuencode(encrypted, 0, encrypted.length);
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  282 */         String errMsg = LocaleUtils.encodeMessage("csEncryptError", null, info.m_category, info.m_algorithm);
/*      */ 
/*  284 */         throw new DataException(errMsg, e);
/*      */       }
/*      */     }
/*  287 */     return result;
/*      */   }
/*      */ 
/*      */   public String decrypt(PasswordInfo info) throws DataException {
/*  291 */     String[] result = { null };
/*  292 */     decryptWithFlags(info, result, 0);
/*  293 */     return result[0];
/*      */   }
/*      */ 
/*      */   public boolean validate(PasswordInfo info)
/*      */   {
/*  298 */     boolean retVal = false;
/*      */     try
/*      */     {
/*  301 */       retVal = decryptWithFlags(info, null, F_VALIDATE_ONLY);
/*      */     }
/*      */     catch (DataException e)
/*      */     {
/*  305 */       Report.trace("system", null, e);
/*      */     }
/*  307 */     return retVal;
/*      */   }
/*      */ 
/*      */   public boolean decryptWithFlags(PasswordInfo info, String[] retResult, int flags) throws DataException
/*      */   {
/*  312 */     boolean validateOnly = (flags & F_VALIDATE_ONLY) != 0;
/*  313 */     String result = null;
/*  314 */     boolean retVal = true;
/*  315 */     if ((info.m_password == null) || (info.m_password.equals("managed")))
/*      */     {
/*  317 */       lookupPassword(info);
/*      */     }
/*  319 */     else if ((info.m_algorithm == null) || (info.m_algorithm.equals("managed")))
/*      */     {
/*  322 */       info.m_algorithm = "";
/*      */     }
/*      */ 
/*  325 */     if ((info.m_algorithm == null) || (info.m_algorithm.length() == 0) || (info.m_algorithm.equals("ClearText")))
/*      */     {
/*  328 */       if (!validateOnly)
/*      */       {
/*  330 */         result = info.m_password;
/*      */       }
/*      */     }
/*  333 */     else if (info.m_algorithm.equals("Intradoc"))
/*      */     {
/*  335 */       if (!validateOnly)
/*      */       {
/*  337 */         result = decryptDefault(info);
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*  343 */       getKeyFromPassword(info);
/*  344 */       if (info.m_key == null)
/*      */       {
/*  346 */         if (!validateOnly)
/*      */         {
/*  348 */           String errMsg = LocaleUtils.encodeMessage("csDecryptKeyMissing", null, info.m_category, info.m_algorithm);
/*      */ 
/*  350 */           throw new DataException(errMsg);
/*      */         }
/*  352 */         retVal = false;
/*      */       }
/*  354 */       if (validateOnly)
/*      */       {
/*  356 */         return retVal;
/*      */       }
/*  358 */       String key = info.m_key;
/*  359 */       String type = info.m_algorithm;
/*  360 */       String password = info.m_password;
/*      */       try
/*      */       {
/*  364 */         int[] length = { 0 };
/*  365 */         byte[] tmp = CommonDataConversion.uudecode(key, length);
/*  366 */         byte[] d = new byte[length[0]];
/*  367 */         System.arraycopy(tmp, 0, d, 0, length[0]);
/*  368 */         SecretKeySpec skeySpec = new SecretKeySpec(d, type);
/*      */ 
/*  371 */         length = new int[] { 0 };
/*  372 */         tmp = CommonDataConversion.uudecode(password, length);
/*  373 */         byte[] bData = new byte[length[0]];
/*  374 */         System.arraycopy(tmp, 0, bData, 0, length[0]);
/*      */ 
/*  378 */         synchronized (this.m_cipherMap)
/*      */         {
/*  380 */           Cipher cipher = (Cipher)this.m_cipherMap.get(type);
/*  381 */           if (cipher == null)
/*      */           {
/*  383 */             cipher = Cipher.getInstance(type);
/*  384 */             this.m_cipherMap.put(type, cipher);
/*      */           }
/*  386 */           cipher.init(2, skeySpec);
/*  387 */           byte[] encResult = cipher.doFinal(bData);
/*  388 */           result = new String(encResult);
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  393 */         String errMsg = LocaleUtils.encodeMessage("csDecryptError", null, info.m_category, type);
/*      */ 
/*  395 */         throw new DataException(errMsg, e);
/*      */       }
/*      */     }
/*  398 */     if (retResult != null)
/*      */     {
/*  400 */       retResult[0] = result;
/*      */     }
/*  402 */     return retVal;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String encryptDefault(PasswordInfo info)
/*      */   {
/*  409 */     String eResult = null;
/*  410 */     if (info.m_password != null)
/*      */     {
/*  412 */       String encKey = "kshdalosjgces";
/*  413 */       byte[] tmp = (info.m_password + "                                ").getBytes();
/*  414 */       int len = (1 + tmp.length) / 2 * 2;
/*  415 */       if (len < 32)
/*      */       {
/*  417 */         len = 32;
/*      */       }
/*      */ 
/*  420 */       byte[] d = new byte[len];
/*  421 */       System.arraycopy(tmp, 0, d, 0, tmp.length);
/*  422 */       byte[] result = crypt(d, encKey.getBytes(), true);
/*      */ 
/*  424 */       eResult = CommonDataConversion.uuencode(result, 0, result.length);
/*      */     }
/*  426 */     return eResult;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public String decryptDefault(PasswordInfo info)
/*      */   {
/*  433 */     String dResult = null;
/*  434 */     if (info.m_password != null)
/*      */     {
/*  436 */       String encKey = "kshdalosjgces";
/*  437 */       int[] length = { 0 };
/*  438 */       byte[] tmp = CommonDataConversion.uudecode(info.m_password, length);
/*  439 */       byte[] d = new byte[length[0]];
/*  440 */       System.arraycopy(tmp, 0, d, 0, length[0]);
/*  441 */       byte[] result = crypt(d, encKey.getBytes(), false);
/*      */ 
/*  443 */       dResult = new String(result).trim();
/*      */     }
/*  445 */     return dResult;
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   protected byte[] crypt(byte[] data, byte[] key, boolean isEncrypt)
/*      */   {
/*  452 */     synchronized (this.m_sha1Digest)
/*      */     {
/*  454 */       int msgSize = data.length;
/*      */ 
/*  458 */       byte[] resultBuf = new byte[msgSize];
/*      */       int y2;
/*      */       int x1;
/*      */       int x2;
/*      */       int y1;
/*      */       int y2;
/*  460 */       if (isEncrypt)
/*      */       {
/*  462 */         int x1 = 0;
/*  463 */         int x2 = msgSize / 2;
/*  464 */         int y1 = x2;
/*  465 */         y2 = msgSize - y1;
/*      */       }
/*      */       else
/*      */       {
/*  469 */         x1 = (1 + msgSize) / 2;
/*  470 */         x2 = msgSize - x1;
/*  471 */         y1 = 0;
/*  472 */         y2 = x1;
/*      */       }
/*      */ 
/*  475 */       this.m_sha1Digest.update(key);
/*  476 */       this.m_sha1Digest.update(data, x1, x2);
/*  477 */       byte[] result = this.m_sha1Digest.digest();
/*  478 */       int digestSize = result.length;
/*  479 */       for (int i = 0; i < y2; ++i)
/*      */       {
/*  481 */         resultBuf[(i + y1)] = (byte)(result[(i % digestSize)] ^ data[(i + y1)]);
/*      */       }
/*      */ 
/*  484 */       this.m_sha1Digest.update(key);
/*  485 */       this.m_sha1Digest.update(resultBuf, y1, y2);
/*  486 */       result = this.m_sha1Digest.digest();
/*      */ 
/*  488 */       digestSize = result.length;
/*  489 */       for (int i = 0; i < x2; ++i)
/*      */       {
/*  491 */         resultBuf[(i + x1)] = (byte)(result[(i % digestSize)] ^ data[(i + x1)]);
/*      */       }
/*  493 */       return resultBuf;
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updateMasterKey(String category, String type, byte[] rawKey)
/*      */     throws DataException, ServiceException
/*      */   {
/*  504 */     String key = CommonDataConversion.uuencode(rawKey, 0, rawKey.length);
/*  505 */     storeKey(key, category, type);
/*      */   }
/*      */ 
/*      */   public void storeKey(String key, String category, String algorithm)
/*      */     throws DataException, ServiceException
/*      */   {
/*  512 */     Date dte = new Date();
/*  513 */     String keyName = category + ":" + dte.getTime();
/*      */ 
/*  515 */     Map map = new HashMap();
/*  516 */     map.put("scKeyName", keyName);
/*  517 */     map.put("scAlgorithm", algorithm);
/*  518 */     map.put("scCategory", category);
/*  519 */     map.put("scIsActive", "1");
/*  520 */     map.put("scCreateTs", LocaleUtils.formatODBC(dte));
/*  521 */     map.put("scMasterKey", key);
/*      */ 
/*  523 */     this.m_keyLoader.storeKey(map, this);
/*      */   }
/*      */ 
/*      */   public void loadKeys() throws DataException, ServiceException
/*      */   {
/*  528 */     this.m_keyLoader.load(this);
/*      */   }
/*      */ 
/*      */   public DataResultSet updateKeySet(Map keyParams, DataBinder binder) throws DataException
/*      */   {
/*  533 */     DataResultSet drset = (DataResultSet)binder.getResultSet("MasterKeys");
/*  534 */     if (drset == null)
/*      */     {
/*  536 */       drset = new DataResultSet(MASTER_KEY_COLUMNS);
/*  537 */       binder.addResultSet("MasterKeys", drset);
/*      */     }
/*      */ 
/*  541 */     String category = (String)keyParams.get("scCategory");
/*  542 */     FieldInfo[] fis = ResultSetUtils.createInfoList(drset, MASTER_KEY_COLUMNS, true);
/*  543 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  545 */       String cat = drset.getStringValue(fis[4].m_index);
/*  546 */       if (!category.equalsIgnoreCase(cat))
/*      */         continue;
/*  548 */       drset.setCurrentValue(fis[2].m_index, "0");
/*      */     }
/*      */ 
/*  552 */     Parameters params = new MapParameters(keyParams);
/*  553 */     Vector row = drset.createRow(params);
/*  554 */     drset.addRow(row);
/*      */ 
/*  556 */     return drset;
/*      */   }
/*      */ 
/*      */   public void loadKeys(DataResultSet drset) throws DataException
/*      */   {
/*  561 */     if (drset == null)
/*      */     {
/*  563 */       return;
/*      */     }
/*      */ 
/*  567 */     drset = drset.shallowClone();
/*      */ 
/*  569 */     Map algorithmToKey = new Hashtable();
/*  570 */     Map keyMap = new Hashtable();
/*  571 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  573 */       Map row = drset.getCurrentRowMap();
/*  574 */       String masterKey = (String)row.get("scMasterKey");
/*  575 */       String name = (String)row.get("scKeyName");
/*  576 */       String type = (String)row.get("scAlgorithm");
/*  577 */       String cat = (String)row.get("scCategory");
/*  578 */       if (masterKey.startsWith("["))
/*      */       {
/*  582 */         Report.trace("system", "Unusable password entry " + name + " with master key " + masterKey + ", entry is skipped", null);
/*      */       }
/*      */       else
/*      */       {
/*  586 */         boolean isActive = StringUtils.convertToBool((String)row.get("scIsActive"), false);
/*  587 */         if (isActive)
/*      */         {
/*  589 */           algorithmToKey.put(cat + ":" + type, name);
/*      */         }
/*      */ 
/*  592 */         keyMap.put(name, row);
/*      */       }
/*      */     }
/*  595 */     this.m_masterKeyList = drset;
/*  596 */     this.m_algorithmToKeyMap = algorithmToKey;
/*  597 */     this.m_keyMap = keyMap;
/*      */   }
/*      */ 
/*      */   public void loadPasswords()
/*      */     throws DataException, ServiceException
/*      */   {
/*  605 */     DataBinder binder = new DataBinder();
/*  606 */     readPasswordFile(binder);
/*      */ 
/*  608 */     DataResultSet drset = (DataResultSet)binder.getResultSet("Passwords");
/*  609 */     loadPasswordSet(drset);
/*      */   }
/*      */ 
/*      */   public void loadPasswordSet(DataResultSet drset)
/*      */   {
/*  614 */     Map pMap = new Hashtable();
/*  615 */     if (drset != null)
/*      */     {
/*  617 */       for (drset.first(); drset.isRowPresent(); drset.next())
/*      */       {
/*  619 */         Map map = drset.getCurrentRowMap();
/*  620 */         String key = (String)map.get("scPasswordField");
/*  621 */         String scope = (String)map.get("scPasswordScope");
/*  622 */         if (scope.length() > 0)
/*      */         {
/*  624 */           key = key + ":" + scope;
/*      */         }
/*  626 */         pMap.put(key, map);
/*      */       }
/*      */     }
/*  629 */     this.m_passwordMap = pMap;
/*      */   }
/*      */ 
/*      */   public boolean lookupPassword(PasswordInfo info)
/*      */   {
/*  634 */     boolean result = false;
/*  635 */     String key = info.m_field;
/*  636 */     if (key == null)
/*      */     {
/*  638 */       return result;
/*      */     }
/*  640 */     String scope = info.m_scope;
/*  641 */     if ((scope != null) && (scope.length() > 0))
/*      */     {
/*  643 */       key = key + ":" + scope;
/*      */     }
/*      */ 
/*  646 */     Map map = (Map)this.m_passwordMap.get(key);
/*  647 */     if (map != null)
/*      */     {
/*  649 */       info.update(map);
/*  650 */       result = true;
/*      */     }
/*  652 */     return result;
/*      */   }
/*      */ 
/*      */   public void updatePasswords(DataResultSet drset, Map props, Map args)
/*      */     throws DataException, ServiceException
/*      */   {
/*  658 */     boolean isInPlace = false;
/*  659 */     if (args != null)
/*      */     {
/*  661 */       isInPlace = StringUtils.convertToBool((String)args.get("isInPlace"), false);
/*      */     }
/*  663 */     if (isInPlace)
/*      */     {
/*  665 */       updatePasswordsInPlace(drset, props, args);
/*      */     }
/*      */     else
/*      */     {
/*  669 */       updatePasswordsAndExtract(drset);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updatePasswordsInPlace(DataResultSet drset, Map props, Map args)
/*      */     throws DataException, ServiceException
/*      */   {
/*  676 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*      */     {
/*  678 */       Map map = drset.getCurrentRowMap();
/*  679 */       String field = (String)map.get("field");
/*  680 */       String encField = (String)map.get("encodingField");
/*      */ 
/*  682 */       String password = (String)props.get(field);
/*  683 */       String algorithm = (String)props.get(encField);
/*      */ 
/*  686 */       Map catMap = (Map)this.m_fieldCategoryMap.get(field);
/*  687 */       if (catMap == null)
/*      */         continue;
/*  689 */       PasswordInfo kInfo = new PasswordInfo(password, algorithm, null, catMap);
/*      */ 
/*  691 */       boolean isUpdate = updatePassword(kInfo);
/*  692 */       if (!isUpdate)
/*      */         continue;
/*  694 */       props.put(field, kInfo.m_password);
/*  695 */       props.put(encField, kInfo.m_algorithm);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void updatePasswordsAndExtract(DataResultSet upSet)
/*      */     throws DataException, ServiceException
/*      */   {
/*  704 */     List indexList = new ArrayList();
/*  705 */     List l = preparePasswordInfos(upSet, indexList);
/*  706 */     int size = l.size();
/*  707 */     if (size == 0)
/*      */     {
/*  709 */       return;
/*      */     }
/*      */ 
/*  712 */     DataBinder binder = new DataBinder();
/*  713 */     readPasswordFile(binder);
/*  714 */     DataResultSet passSet = (DataResultSet)binder.getResultSet("Passwords");
/*  715 */     if (passSet == null)
/*      */     {
/*  717 */       passSet = new DataResultSet(PASSWORD_COLUMNS);
/*  718 */       binder.addResultSet("Passwords", passSet);
/*      */     }
/*      */ 
/*  721 */     int updateIndex = ResultSetUtils.getIndexMustExist(upSet, "isUpdated");
/*      */ 
/*  723 */     int fieldIndex = ResultSetUtils.getIndexMustExist(passSet, "scPasswordField");
/*  724 */     int scopeIndex = ResultSetUtils.getIndexMustExist(passSet, "scPasswordScope");
/*  725 */     for (int i = 0; i < size; ++i)
/*      */     {
/*  727 */       PasswordInfo kInfo = (PasswordInfo)l.get(i);
/*      */ 
/*  730 */       String encPassword = kInfo.m_password;
/*      */       try
/*      */       {
/*  733 */         if (updatePassword(kInfo))
/*      */         {
/*  735 */           encPassword = kInfo.m_password;
/*      */         }
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/*  740 */         Report.trace(null, "Unable to encrypt the password for field " + kInfo.m_field + " with scope " + kInfo.m_scope, e);
/*      */ 
/*  742 */         break label380:
/*      */       }
/*      */ 
/*  745 */       Map map = new HashMap();
/*  746 */       map.put("scPasswordField", kInfo.m_field);
/*  747 */       map.put("scPassword", encPassword);
/*  748 */       map.put("scPasswordEncoding", kInfo.m_algorithm);
/*  749 */       map.put("scPasswordScope", kInfo.m_scope);
/*      */ 
/*  751 */       Parameters params = new MapParameters(map);
/*  752 */       List row = passSet.createRowAsList(params);
/*      */ 
/*  756 */       List r = ResultSetUtils.findDualIndexedRow(passSet, kInfo.m_field, kInfo.m_scope, fieldIndex, scopeIndex);
/*      */ 
/*  758 */       if (r != null)
/*      */       {
/*  760 */         int index = passSet.getCurrentRow();
/*  761 */         passSet.setRowWithList(row, index);
/*      */       }
/*      */       else
/*      */       {
/*  765 */         passSet.addRowWithList(row);
/*      */       }
/*      */ 
/*  770 */       int index = NumberUtils.parseInteger((String)indexList.get(i), -1);
/*  771 */       if (index < 0)
/*      */         continue;
/*  773 */       upSet.setCurrentRow(index);
/*  774 */       label380: upSet.setCurrentValue(updateIndex, "1");
/*      */     }
/*      */ 
/*  777 */     writePasswordFile(binder);
/*  778 */     loadPasswordSet(passSet);
/*      */   }
/*      */ 
/*      */   protected List preparePasswordInfos(DataResultSet rset, List indexList) throws DataException
/*      */   {
/*  783 */     List cList = new ArrayList();
/*  784 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  786 */       Map map = rset.getCurrentRowMap();
/*  787 */       String field = (String)map.get("field");
/*  788 */       String curEncoding = (String)map.get("encoding");
/*      */ 
/*  790 */       Map catMap = determineCategoryFieldInfo(field);
/*  791 */       if (catMap == null)
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  798 */       PasswordInfo curInfo = new PasswordInfo((String)map.get("password"), curEncoding, (String)map.get("scope"), catMap);
/*      */ 
/*  800 */       cList.add(curInfo);
/*      */ 
/*  807 */       indexList.add("" + rset.getCurrentRow());
/*      */     }
/*  809 */     return cList;
/*      */   }
/*      */ 
/*      */   public List<String> getAlgorithmList(Map args)
/*      */   {
/*  816 */     String algoStr = null;
/*  817 */     if (args != null)
/*      */     {
/*  819 */       algoStr = (String)args.get("AlgorithmList");
/*      */     }
/*  821 */     List algoList = StringUtils.makeListFromSequenceSimple(algoStr);
/*  822 */     if (algoList.size() == 0)
/*      */     {
/*  824 */       algoList.add("AES");
/*      */     }
/*  826 */     return algoList;
/*      */   }
/*      */ 
/*      */   public boolean getBoolArgValue(Map args, String key, boolean defVal)
/*      */   {
/*  831 */     if (args == null)
/*      */     {
/*  833 */       return defVal;
/*      */     }
/*  835 */     Object val = args.get(key);
/*  836 */     return ScriptUtils.convertObjectToBool(val, defVal); } 
/*      */   public void updateExpiredKeys(Map args) throws ServiceException, DataException { // Byte code:
/*      */     //   0: aload_0
/*      */     //   1: aload_1
/*      */     //   2: ldc 69
/*      */     //   4: iconst_0
/*      */     //   5: invokevirtual 213	intradoc/conversion/SecurityObjects:getBoolArgValue	(Ljava/util/Map;Ljava/lang/String;Z)Z
/*      */     //   8: istore_3
/*      */     //   9: aload_0
/*      */     //   10: aload_1
/*      */     //   11: ldc 214
/*      */     //   13: iconst_0
/*      */     //   14: invokevirtual 213	intradoc/conversion/SecurityObjects:getBoolArgValue	(Ljava/util/Map;Ljava/lang/String;Z)Z
/*      */     //   17: istore 4
/*      */     //   19: aconst_null
/*      */     //   20: astore 5
/*      */     //   22: iload_3
/*      */     //   23: ifne +62 -> 85
/*      */     //   26: new 169	intradoc/data/DataBinder
/*      */     //   29: dup
/*      */     //   30: invokespecial 170	intradoc/data/DataBinder:<init>	()V
/*      */     //   33: astore 5
/*      */     //   35: aload_0
/*      */     //   36: getfield 5	intradoc/conversion/SecurityObjects:m_keyLoader	Lintradoc/conversion/KeyLoaderInterface;
/*      */     //   39: aload 5
/*      */     //   41: invokeinterface 215 2 0
/*      */     //   46: pop
/*      */     //   47: aload 5
/*      */     //   49: ldc 149
/*      */     //   51: invokevirtual 150	intradoc/data/DataBinder:getResultSet	(Ljava/lang/String;)Lintradoc/data/ResultSet;
/*      */     //   54: checkcast 151	intradoc/data/DataResultSet
/*      */     //   57: astore_2
/*      */     //   58: aload_2
/*      */     //   59: ifnonnull +31 -> 90
/*      */     //   62: new 151	intradoc/data/DataResultSet
/*      */     //   65: dup
/*      */     //   66: getstatic 152	intradoc/conversion/SecurityObjects:MASTER_KEY_COLUMNS	[Ljava/lang/String;
/*      */     //   69: invokespecial 153	intradoc/data/DataResultSet:<init>	([Ljava/lang/String;)V
/*      */     //   72: astore_2
/*      */     //   73: aload 5
/*      */     //   75: ldc 149
/*      */     //   77: aload_2
/*      */     //   78: invokevirtual 154	intradoc/data/DataBinder:addResultSet	(Ljava/lang/String;Lintradoc/data/ResultSet;)Lintradoc/data/ResultSet;
/*      */     //   81: pop
/*      */     //   82: goto +8 -> 90
/*      */     //   85: aload_0
/*      */     //   86: getfield 8	intradoc/conversion/SecurityObjects:m_masterKeyList	Lintradoc/data/DataResultSet;
/*      */     //   89: astore_2
/*      */     //   90: aload_2
/*      */     //   91: ifnonnull +4 -> 95
/*      */     //   94: return
/*      */     //   95: aload_0
/*      */     //   96: aload_1
/*      */     //   97: invokevirtual 216	intradoc/conversion/SecurityObjects:getAlgorithmList	(Ljava/util/Map;)Ljava/util/List;
/*      */     //   100: astore 6
/*      */     //   102: iconst_0
/*      */     //   103: istore 7
/*      */     //   105: aload_1
/*      */     //   106: ldc 217
/*      */     //   108: invokeinterface 79 2 0
/*      */     //   113: checkcast 80	java/lang/String
/*      */     //   116: bipush 120
/*      */     //   118: bipush 21
/*      */     //   120: bipush 21
/*      */     //   122: invokestatic 218	intradoc/common/NumberUtils:parseTypedInteger	(Ljava/lang/String;III)I
/*      */     //   125: istore 8
/*      */     //   127: new 73	java/util/ArrayList
/*      */     //   130: dup
/*      */     //   131: invokespecial 74	java/util/ArrayList:<init>	()V
/*      */     //   134: astore 9
/*      */     //   136: aload_2
/*      */     //   137: ldc 143
/*      */     //   139: invokestatic 190	intradoc/data/ResultSetUtils:getIndexMustExist	(Lintradoc/data/ResultSet;Ljava/lang/String;)I
/*      */     //   142: istore 10
/*      */     //   144: aload_2
/*      */     //   145: invokevirtual 219	intradoc/data/DataResultSet:getNumRows	()I
/*      */     //   148: istore 11
/*      */     //   150: iconst_0
/*      */     //   151: istore 12
/*      */     //   153: iload 12
/*      */     //   155: iload 11
/*      */     //   157: if_icmpge +299 -> 456
/*      */     //   160: aload_2
/*      */     //   161: iload 12
/*      */     //   163: invokevirtual 202	intradoc/data/DataResultSet:setCurrentRow	(I)V
/*      */     //   166: aload_2
/*      */     //   167: invokevirtual 77	intradoc/data/DataResultSet:getCurrentRowMap	()Ljava/util/Map;
/*      */     //   170: astore 13
/*      */     //   172: aload 13
/*      */     //   174: ldc 143
/*      */     //   176: invokeinterface 79 2 0
/*      */     //   181: checkcast 80	java/lang/String
/*      */     //   184: iconst_0
/*      */     //   185: invokestatic 50	intradoc/common/StringUtils:convertToBool	(Ljava/lang/String;Z)Z
/*      */     //   188: istore 14
/*      */     //   190: iload 14
/*      */     //   192: ifeq +258 -> 450
/*      */     //   195: aload 13
/*      */     //   197: ldc 142
/*      */     //   199: invokeinterface 79 2 0
/*      */     //   204: checkcast 80	java/lang/String
/*      */     //   207: astore 15
/*      */     //   209: aload 6
/*      */     //   211: aload 15
/*      */     //   213: invokeinterface 220 2 0
/*      */     //   218: ifne +6 -> 224
/*      */     //   221: goto +229 -> 450
/*      */     //   224: aload 13
/*      */     //   226: ldc 81
/*      */     //   228: invokeinterface 79 2 0
/*      */     //   233: checkcast 80	java/lang/String
/*      */     //   236: astore 16
/*      */     //   238: aload 9
/*      */     //   240: new 32	java/lang/StringBuilder
/*      */     //   243: dup
/*      */     //   244: invokespecial 33	java/lang/StringBuilder:<init>	()V
/*      */     //   247: aload 16
/*      */     //   249: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   252: ldc 111
/*      */     //   254: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   257: aload 15
/*      */     //   259: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   262: invokevirtual 36	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   265: invokeinterface 83 2 0
/*      */     //   270: pop
/*      */     //   271: aload 13
/*      */     //   273: ldc 144
/*      */     //   275: invokeinterface 79 2 0
/*      */     //   280: checkcast 80	java/lang/String
/*      */     //   283: astore 17
/*      */     //   285: aload 17
/*      */     //   287: invokestatic 221	intradoc/common/LocaleUtils:parseODBC	(Ljava/lang/String;)Ljava/util/Date;
/*      */     //   290: astore 18
/*      */     //   292: aload 13
/*      */     //   294: ldc 146
/*      */     //   296: invokeinterface 79 2 0
/*      */     //   301: checkcast 80	java/lang/String
/*      */     //   304: astore 19
/*      */     //   306: aload 19
/*      */     //   308: ldc 222
/*      */     //   310: invokevirtual 88	java/lang/String:equals	(Ljava/lang/Object;)Z
/*      */     //   313: ifne +7 -> 320
/*      */     //   316: iconst_1
/*      */     //   317: goto +4 -> 321
/*      */     //   320: iconst_0
/*      */     //   321: istore 20
/*      */     //   323: iconst_0
/*      */     //   324: istore 21
/*      */     //   326: iload 4
/*      */     //   328: ifeq +32 -> 360
/*      */     //   331: iload 8
/*      */     //   333: invokestatic 223	intradoc/common/DateUtils:getDateXDaysAgo	(I)Ljava/util/Date;
/*      */     //   336: astore 22
/*      */     //   338: iload 4
/*      */     //   340: ifeq +17 -> 357
/*      */     //   343: aload 18
/*      */     //   345: aload 22
/*      */     //   347: invokevirtual 224	java/util/Date:before	(Ljava/util/Date;)Z
/*      */     //   350: ifeq +7 -> 357
/*      */     //   353: iconst_1
/*      */     //   354: goto +4 -> 358
/*      */     //   357: iconst_0
/*      */     //   358: istore 21
/*      */     //   360: iload 21
/*      */     //   362: ifne +8 -> 370
/*      */     //   365: iload 20
/*      */     //   367: ifne +83 -> 450
/*      */     //   370: iconst_1
/*      */     //   371: istore 7
/*      */     //   373: iload_3
/*      */     //   374: ifne +76 -> 450
/*      */     //   377: iload 21
/*      */     //   379: ifeq +25 -> 404
/*      */     //   382: ldc 225
/*      */     //   384: iconst_2
/*      */     //   385: anewarray 29	java/lang/Object
/*      */     //   388: dup
/*      */     //   389: iconst_0
/*      */     //   390: aload 16
/*      */     //   392: aastore
/*      */     //   393: dup
/*      */     //   394: iconst_1
/*      */     //   395: aload 15
/*      */     //   397: aastore
/*      */     //   398: invokestatic 96	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*      */     //   401: goto +22 -> 423
/*      */     //   404: ldc 226
/*      */     //   406: iconst_2
/*      */     //   407: anewarray 29	java/lang/Object
/*      */     //   410: dup
/*      */     //   411: iconst_0
/*      */     //   412: aload 16
/*      */     //   414: aastore
/*      */     //   415: dup
/*      */     //   416: iconst_1
/*      */     //   417: aload 15
/*      */     //   419: aastore
/*      */     //   420: invokestatic 96	intradoc/common/IdcMessageFactory:lc	(Ljava/lang/String;[Ljava/lang/Object;)Lintradoc/util/IdcMessage;
/*      */     //   423: astore 22
/*      */     //   425: ldc 120
/*      */     //   427: aconst_null
/*      */     //   428: aload 22
/*      */     //   430: invokestatic 227	intradoc/common/Report:info	(Ljava/lang/String;Ljava/lang/Throwable;Lintradoc/util/IdcMessage;)V
/*      */     //   433: aload_2
/*      */     //   434: iload 10
/*      */     //   436: ldc 159
/*      */     //   438: invokevirtual 160	intradoc/data/DataResultSet:setCurrentValue	(ILjava/lang/String;)V
/*      */     //   441: aload_0
/*      */     //   442: aload_2
/*      */     //   443: aload 15
/*      */     //   445: aload 16
/*      */     //   447: invokevirtual 228	intradoc/conversion/SecurityObjects:addNewKeyRow	(Lintradoc/data/DataResultSet;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   450: iinc 12 1
/*      */     //   453: goto -300 -> 153
/*      */     //   456: aload_0
/*      */     //   457: getfield 14	intradoc/conversion/SecurityObjects:m_categories	Ljava/util/List;
/*      */     //   460: invokeinterface 229 1 0
/*      */     //   465: astore 12
/*      */     //   467: aload 12
/*      */     //   469: invokeinterface 230 1 0
/*      */     //   474: ifeq +146 -> 620
/*      */     //   477: aload 12
/*      */     //   479: invokeinterface 231 1 0
/*      */     //   484: checkcast 80	java/lang/String
/*      */     //   487: astore 13
/*      */     //   489: aload 6
/*      */     //   491: invokeinterface 229 1 0
/*      */     //   496: astore 14
/*      */     //   498: aload 14
/*      */     //   500: invokeinterface 230 1 0
/*      */     //   505: ifeq +112 -> 617
/*      */     //   508: aload 14
/*      */     //   510: invokeinterface 231 1 0
/*      */     //   515: checkcast 80	java/lang/String
/*      */     //   518: astore 15
/*      */     //   520: aload 9
/*      */     //   522: new 32	java/lang/StringBuilder
/*      */     //   525: dup
/*      */     //   526: invokespecial 33	java/lang/StringBuilder:<init>	()V
/*      */     //   529: aload 13
/*      */     //   531: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   534: ldc 111
/*      */     //   536: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   539: aload 15
/*      */     //   541: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   544: invokevirtual 36	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   547: invokeinterface 82 2 0
/*      */     //   552: istore 16
/*      */     //   554: iload 16
/*      */     //   556: iflt +6 -> 562
/*      */     //   559: goto -61 -> 498
/*      */     //   562: iconst_1
/*      */     //   563: istore 7
/*      */     //   565: iload_3
/*      */     //   566: ifne +48 -> 614
/*      */     //   569: ldc 120
/*      */     //   571: new 32	java/lang/StringBuilder
/*      */     //   574: dup
/*      */     //   575: invokespecial 33	java/lang/StringBuilder:<init>	()V
/*      */     //   578: ldc 232
/*      */     //   580: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   583: aload 13
/*      */     //   585: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   588: ldc 233
/*      */     //   590: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   593: aload 15
/*      */     //   595: invokevirtual 34	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
/*      */     //   598: invokevirtual 36	java/lang/StringBuilder:toString	()Ljava/lang/String;
/*      */     //   601: aconst_null
/*      */     //   602: invokestatic 121	intradoc/common/Report:trace	(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
/*      */     //   605: aload_0
/*      */     //   606: aload_2
/*      */     //   607: aload 15
/*      */     //   609: aload 13
/*      */     //   611: invokevirtual 228	intradoc/conversion/SecurityObjects:addNewKeyRow	(Lintradoc/data/DataResultSet;Ljava/lang/String;Ljava/lang/String;)V
/*      */     //   614: goto -116 -> 498
/*      */     //   617: goto -150 -> 467
/*      */     //   620: iload 7
/*      */     //   622: ifeq +43 -> 665
/*      */     //   625: iload_3
/*      */     //   626: ifne +34 -> 660
/*      */     //   629: aload_0
/*      */     //   630: getfield 5	intradoc/conversion/SecurityObjects:m_keyLoader	Lintradoc/conversion/KeyLoaderInterface;
/*      */     //   633: aload 5
/*      */     //   635: invokeinterface 234 2 0
/*      */     //   640: pop
/*      */     //   641: aload 5
/*      */     //   643: ldc 149
/*      */     //   645: invokevirtual 150	intradoc/data/DataBinder:getResultSet	(Ljava/lang/String;)Lintradoc/data/ResultSet;
/*      */     //   648: checkcast 151	intradoc/data/DataResultSet
/*      */     //   651: astore_2
/*      */     //   652: aload_0
/*      */     //   653: aload_2
/*      */     //   654: invokevirtual 235	intradoc/conversion/SecurityObjects:loadKeys	(Lintradoc/data/DataResultSet;)V
/*      */     //   657: goto +8 -> 665
/*      */     //   660: aload_0
/*      */     //   661: iconst_1
/*      */     //   662: putfield 4	intradoc/conversion/SecurityObjects:m_needsMasterKeys	Z
/*      */     //   665: iload_3
/*      */     //   666: ifne +8 -> 674
/*      */     //   669: aload_0
/*      */     //   670: iconst_0
/*      */     //   671: putfield 4	intradoc/conversion/SecurityObjects:m_needsMasterKeys	Z
/*      */     //   674: return } 
/*  988 */   protected void addNewKeyRow(DataResultSet keySet, String algorithm, String category) throws DataException { String rawKey = CryptoCommonUtils.generateRandomString(16);
/*  989 */     String key = CommonDataConversion.uuencode(rawKey.getBytes(), 0, rawKey.getBytes().length);
/*  990 */     Date dte = new Date();
/*  991 */     String keyName = category + ":" + dte.getTime();
/*      */ 
/*  993 */     Map row = new HashMap();
/*  994 */     row.put("scKeyName", keyName);
/*  995 */     row.put("scIsActive", "1");
/*  996 */     row.put("scAlgorithm", algorithm);
/*  997 */     row.put("scCreateTs", LocaleUtils.formatODBC(dte));
/*  998 */     row.put("scCategory", category);
/*  999 */     row.put("scMasterKey", key);
/*      */ 
/* 1001 */     Parameters params = new MapParameters(row);
/* 1002 */     Vector r = keySet.createRow(params);
/* 1003 */     keySet.addRow(r); }
/*      */ 
/*      */   public void updateExpiredPasswords(Map args)
/*      */     throws DataException, ServiceException
/*      */   {
/* 1008 */     DataBinder binder = new DataBinder();
/* 1009 */     readPasswordFile(binder);
/* 1010 */     DataResultSet passSet = (DataResultSet)binder.getResultSet("Passwords");
/*      */ 
/* 1012 */     if (passSet == null)
/*      */     {
/* 1014 */       Report.trace("system", "not updating expired passwords because there isn't a Passwords ResultSet", null);
/*      */ 
/* 1016 */       return;
/*      */     }
/*      */ 
/* 1019 */     boolean isModified = false;
/* 1020 */     int passIndex = ResultSetUtils.getIndexMustExist(passSet, "scPassword");
/* 1021 */     int encIndex = ResultSetUtils.getIndexMustExist(passSet, "scPasswordEncoding");
/* 1022 */     for (passSet.first(); passSet.isRowPresent(); passSet.next())
/*      */     {
/* 1024 */       Map map = passSet.getCurrentRowMap();
/* 1025 */       String field = (String)map.get("scPasswordField");
/* 1026 */       String algorithm = (String)map.get("scPasswordEncoding");
/* 1027 */       String scope = (String)map.get("scPasswordScope");
/*      */ 
/* 1030 */       Map catMap = (Map)this.m_fieldCategoryMap.get(field);
/* 1031 */       if (catMap != null)
/*      */       {
/* 1033 */         PasswordInfo kInfo = new PasswordInfo((String)map.get("scPassword"), algorithm, scope, catMap);
/*      */ 
/* 1036 */         boolean isUpdate = updatePassword(kInfo);
/* 1037 */         if (isUpdate)
/*      */         {
/* 1039 */           passSet.setCurrentValue(passIndex, kInfo.m_password);
/* 1040 */           passSet.setCurrentValue(encIndex, kInfo.m_algorithm);
/* 1041 */           isModified = true;
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/* 1046 */         String errMsg = LocaleUtils.encodeMessage("csPasswordFieldCategoryMissing", null, field);
/*      */ 
/* 1048 */         Report.trace("system", errMsg, null);
/*      */       }
/*      */     }
/*      */ 
/* 1052 */     if (!isModified)
/*      */       return;
/* 1054 */     writePasswordFile(binder);
/* 1055 */     loadPasswordSet(passSet);
/*      */   }
/*      */ 
/*      */   public boolean updatePassword(PasswordInfo kInfo)
/*      */   {
/* 1062 */     boolean isChanged = false;
/* 1063 */     boolean isDoEncryption = false;
/* 1064 */     boolean isUnencrypted = kInfo.isUnencrypted();
/* 1065 */     if ((kInfo.m_extraEncoding != null) && (kInfo.m_extraEncoding.length() > 0))
/*      */     {
/* 1067 */       if (isUnencrypted)
/*      */       {
/* 1070 */         String dPassword = CryptoCommonUtils.uuencodeHashWithDigest(kInfo.m_password, null, kInfo.m_extraEncoding);
/*      */ 
/* 1072 */         kInfo.m_password = dPassword;
/* 1073 */         isDoEncryption = true;
/*      */       }
/* 1075 */       else if (kInfo.m_algorithm.equals(kInfo.m_extraEncoding))
/*      */       {
/* 1077 */         isDoEncryption = true;
/*      */       }
/*      */     }
/*      */ 
/* 1081 */     if ((isDoEncryption) || (isUnencrypted) || (kInfo.m_algorithm.equals("Intradoc")))
/*      */     {
/* 1085 */       String oldAlgo = kInfo.m_algorithm;
/*      */       try
/*      */       {
/* 1088 */         kInfo.m_algorithm = "AES";
/* 1089 */         getKey(kInfo);
/* 1090 */         if (kInfo.m_key != null)
/*      */         {
/* 1092 */           if ((!isDoEncryption) && (!isUnencrypted))
/*      */           {
/* 1094 */             kInfo.m_algorithm = oldAlgo;
/* 1095 */             String clearPassword = decrypt(kInfo);
/* 1096 */             kInfo.m_password = clearPassword;
/*      */           }
/*      */ 
/* 1099 */           kInfo.m_algorithm = "AES";
/* 1100 */           String encPassword = encrypt(kInfo);
/* 1101 */           kInfo.m_password = encPassword;
/* 1102 */           isChanged = true;
/*      */         }
/* 1104 */         else if (isUnencrypted)
/*      */         {
/* 1106 */           kInfo.m_algorithm = "Intradoc";
/* 1107 */           String encPassword = encrypt(kInfo);
/* 1108 */           kInfo.m_password = encPassword;
/* 1109 */           isChanged = true;
/*      */ 
/* 1113 */           Report.trace("system", null, "csPasswordCategoryKeyMissing", new Object[] { kInfo.m_field, kInfo.m_category });
/*      */         }
/*      */         else
/*      */         {
/* 1118 */           kInfo.m_algorithm = oldAlgo;
/*      */         }
/*      */ 
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1124 */         kInfo.m_algorithm = oldAlgo;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1129 */       String oldPassword = kInfo.m_password;
/*      */       try
/*      */       {
/* 1132 */         String clearPassword = decrypt(kInfo);
/* 1133 */         kInfo.m_password = clearPassword;
/*      */ 
/* 1136 */         String password = encrypt(kInfo);
/*      */ 
/* 1140 */         isChanged = !oldPassword.equals(password);
/* 1141 */         kInfo.setPassword(password);
/*      */       }
/*      */       catch (Exception e)
/*      */       {
/* 1145 */         Report.error("system", e, "csEncryptPasswordError", new Object[] { kInfo.m_field, kInfo.m_scope });
/*      */       }
/*      */     }
/* 1148 */     return isChanged;
/*      */   }
/*      */ 
/*      */   public boolean readPasswordFile(DataBinder binder) throws ServiceException
/*      */   {
/* 1153 */     if (this.m_passwordDir != null)
/*      */     {
/* 1155 */       return ResourceUtils.serializeDataBinder(this.m_passwordDir, "passwords.hda", binder, false, false);
/*      */     }
/*      */ 
/* 1158 */     Report.trace(null, "Missing password directory for readPasswordFile().", null);
/* 1159 */     return false;
/*      */   }
/*      */ 
/*      */   public void writePasswordFile(DataBinder binder) throws ServiceException
/*      */   {
/* 1164 */     ResourceUtils.serializeDataBinder(this.m_passwordDir, "passwords.hda", binder, true, false);
/*      */   }
/*      */ 
/*      */   public void getKey(PasswordInfo info)
/*      */   {
/* 1171 */     String keyName = (String)this.m_algorithmToKeyMap.get(info.m_category + ":" + info.m_algorithm);
/* 1172 */     if (keyName == null)
/*      */       return;
/* 1174 */     Map keyMap = (Map)this.m_keyMap.get(keyName);
/* 1175 */     info.m_key = ((String)keyMap.get("scMasterKey"));
/*      */ 
/* 1177 */     List l = StringUtils.makeListFromSequence(keyName, ':', '*', 32);
/*      */ 
/* 1180 */     if (l.size() != 2)
/*      */       return;
/* 1182 */     info.m_category = ((String)l.get(0));
/* 1183 */     info.m_ts = NumberUtils.parseLong((String)l.get(1), -1L);
/*      */   }
/*      */ 
/*      */   public void getKeyFromPassword(PasswordInfo info)
/*      */   {
/* 1190 */     List l = StringUtils.makeListFromSequence(info.m_password, ':', '*', 32);
/*      */ 
/* 1193 */     if (l.size() != 3)
/*      */       return;
/* 1195 */     info.m_category = ((String)l.get(0));
/* 1196 */     info.m_ts = NumberUtils.parseLong((String)l.get(1), -1L);
/* 1197 */     info.m_password = ((String)l.get(2));
/*      */ 
/* 1199 */     String keyName = info.m_category + ":" + info.m_ts;
/* 1200 */     Map keyData = (Map)this.m_keyMap.get(keyName);
/* 1201 */     if (keyData == null)
/*      */       return;
/* 1203 */     info.m_key = ((String)keyData.get("scMasterKey"));
/*      */   }
/*      */ 
/*      */   public DataResultSet getMasterKeys()
/*      */     throws DataException, ServiceException
/*      */   {
/* 1210 */     DataBinder binder = new DataBinder();
/* 1211 */     this.m_keyLoader.readKeys(binder);
/* 1212 */     DataResultSet drset = (DataResultSet)binder.getResultSet("MasterKeys");
/* 1213 */     loadKeys(drset);
/* 1214 */     return drset;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 1219 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84565 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.SecurityObjects
 * JD-Core Version:    0.5.4
 */