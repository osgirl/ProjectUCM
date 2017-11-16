/*     */ package intradoc.conversion;
/*     */ 
/*     */ import intradoc.common.AppObjectRepository;
/*     */ import intradoc.common.CryptoCommonUtils;
/*     */ import intradoc.common.IdcMessageFactory;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.DataSerialize;
/*     */ import intradoc.data.DataSerializeUtils;
/*     */ import intradoc.data.MapParameters;
/*     */ import intradoc.data.Parameters;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CryptoPasswordUtils
/*     */ {
/*  30 */   protected static boolean m_isInitialized = false;
/*  31 */   protected static SecurityObjects m_sObjects = null;
/*  32 */   protected static Properties m_environment = null;
/*     */ 
/*  38 */   public static int m_securityBits = 64;
/*     */ 
/*  40 */   public static String F_AES_ENCODING = "AES";
/*  41 */   public static String F_IDC_ENCODING = "Intradoc";
/*     */ 
/*     */   public static void setEnvironment(Properties environment)
/*     */   {
/*  45 */     m_isInitialized = false;
/*  46 */     String bits = environment.getProperty("CryptoPasswordUtilsEncryptionBits");
/*  47 */     if (bits != null)
/*     */     {
/*  49 */       Integer val = new Integer(bits);
/*  50 */       m_securityBits = val.intValue();
/*  51 */       if (m_securityBits < 32)
/*     */       {
/*  53 */         Report.trace("system", "moving CryptoPasswordUtils.m_securityBits to 32", null);
/*  54 */         m_securityBits = 32;
/*     */       }
/*     */     }
/*  57 */     m_environment = environment;
/*     */   }
/*     */ 
/*     */   public static Properties getEnvironment()
/*     */   {
/*  62 */     return m_environment;
/*     */   }
/*     */ 
/*     */   protected static void initSecurityObjects()
/*     */     throws ServiceException
/*     */   {
/*  70 */     DataSerialize obj = DataSerializeUtils.getDataSerialize();
/*  71 */     if (obj == null)
/*     */       return;
/*  73 */     m_sObjects = new SecurityObjects();
/*  74 */     m_sObjects.m_environment = m_environment;
/*  75 */     m_sObjects.init();
/*  76 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   protected static void initSecurityObjectsWithDataException(IdcMessage parentMessage)
/*     */     throws DataException
/*     */   {
/*  86 */     DataSerialize obj = DataSerializeUtils.getDataSerialize();
/*  87 */     if (obj == null)
/*     */       return;
/*  89 */     m_sObjects = new SecurityObjects();
/*  90 */     m_sObjects.m_environment = m_environment;
/*     */     try
/*     */     {
/*  93 */       m_sObjects.init();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*  97 */       throw new DataException(e, parentMessage);
/*     */     }
/*  99 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   protected static void initSecurityObjectsWithServiceException(IdcMessage parentMessage)
/*     */     throws ServiceException
/*     */   {
/* 109 */     DataSerialize obj = DataSerializeUtils.getDataSerialize();
/* 110 */     if (obj == null)
/*     */       return;
/* 112 */     m_sObjects = new SecurityObjects();
/* 113 */     m_sObjects.m_environment = m_environment;
/*     */     try
/*     */     {
/* 116 */       m_sObjects.init();
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 120 */       throw new ServiceException(e, parentMessage);
/*     */     }
/* 122 */     m_isInitialized = true;
/*     */   }
/*     */ 
/*     */   public static boolean loadPasswordManagement(DataResultSet drset)
/*     */     throws DataException, ServiceException
/*     */   {
/* 134 */     if (!m_isInitialized)
/*     */     {
/* 136 */       initSecurityObjects();
/*     */     }
/* 138 */     return m_sObjects.loadPasswordManagement(drset);
/*     */   }
/*     */ 
/*     */   public static boolean needsUpdate()
/*     */   {
/* 143 */     if (m_sObjects == null)
/*     */     {
/* 145 */       return false;
/*     */     }
/* 147 */     return m_sObjects.checkNeedsUpdating();
/*     */   }
/*     */ 
/*     */   public static boolean hasMasterKeys()
/*     */   {
/* 152 */     if (m_sObjects == null)
/*     */     {
/* 154 */       return false;
/*     */     }
/* 156 */     return m_sObjects.m_masterKeyList != null;
/*     */   }
/*     */ 
/*     */   public static String encrypt(PasswordInfo info)
/*     */     throws DataException, ServiceException
/*     */   {
/* 166 */     if (!m_isInitialized)
/*     */     {
/* 168 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csEncryptPasswordError", new Object[] { info.m_field, info.m_category }));
/*     */     }
/*     */ 
/* 171 */     return m_sObjects.encrypt(info);
/*     */   }
/*     */ 
/*     */   public static String decrypt(PasswordInfo info)
/*     */     throws DataException, ServiceException
/*     */   {
/* 181 */     if (!m_isInitialized)
/*     */     {
/* 183 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csDecryptPasswordError", new Object[] { info.m_field, info.m_category }));
/*     */     }
/*     */ 
/* 186 */     return m_sObjects.decrypt(info);
/*     */   }
/*     */ 
/*     */   public static boolean validate(PasswordInfo info)
/*     */     throws DataException, ServiceException
/*     */   {
/* 199 */     if (!m_isInitialized)
/*     */     {
/* 201 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csDecryptPasswordError", new Object[] { info.m_field, info.m_category }));
/*     */     }
/*     */ 
/* 204 */     return m_sObjects.validate(info);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String encryptDefault(PasswordInfo info)
/*     */     throws ServiceException
/*     */   {
/* 214 */     if (!m_isInitialized)
/*     */     {
/* 216 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csEncryptPasswordError", new Object[] { info.m_field, info.m_category }));
/*     */     }
/*     */ 
/* 219 */     return m_sObjects.encryptDefault(info);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String decryptDefault(PasswordInfo info)
/*     */     throws ServiceException
/*     */   {
/* 229 */     if (!m_isInitialized)
/*     */     {
/* 231 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csDecryptPasswordError", new Object[] { info.m_field, info.m_category }));
/*     */     }
/*     */ 
/* 234 */     return m_sObjects.decryptDefault(info);
/*     */   }
/*     */ 
/*     */   public static void createKey(String category, String algorithm, int size)
/*     */     throws DataException, ServiceException
/*     */   {
/* 245 */     if (!m_isInitialized)
/*     */     {
/* 247 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csMasterKeysInitError", new Object[0]));
/*     */     }
/* 249 */     String raw = CryptoCommonUtils.generateRandomString(size * 2 / 8);
/* 250 */     m_sObjects.updateMasterKey(category, algorithm, raw.getBytes());
/*     */   }
/*     */ 
/*     */   public static void updateExpiredKeys(Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 262 */     if (!m_isInitialized)
/*     */     {
/* 264 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csMasterKeysInitError", new Object[0]));
/*     */     }
/* 266 */     m_sObjects.updateExpiredKeys(args);
/*     */   }
/*     */ 
/*     */   public static void updateExpiredPasswords(Map args) throws DataException, ServiceException
/*     */   {
/* 271 */     if (!m_isInitialized)
/*     */     {
/* 273 */       initSecurityObjectsWithServiceException(IdcMessageFactory.lc("csMasterKeysInitError", new Object[0]));
/*     */     }
/* 275 */     m_sObjects.updateExpiredPasswords(args);
/*     */   }
/*     */ 
/*     */   public static void updatePasswords(DataResultSet rset, Map props, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 281 */     if (!m_isInitialized)
/*     */     {
/* 283 */       initSecurityObjects();
/*     */     }
/* 285 */     m_sObjects.updatePasswords(rset, props, args);
/*     */   }
/*     */ 
/*     */   public static void updatePasswordsInPlace(Map props, String source, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 303 */     if (!m_isInitialized)
/*     */     {
/* 305 */       initSecurityObjects();
/*     */     }
/* 307 */     if (args == null)
/*     */     {
/* 309 */       args = new HashMap();
/*     */     }
/* 311 */     args.put("isInPlace", "1");
/* 312 */     extractAndUpdatePasswords(props, source, args);
/*     */   }
/*     */ 
/*     */   public static DataResultSet extractAndUpdatePasswords(Map props, String source, Map args)
/*     */     throws DataException, ServiceException
/*     */   {
/* 331 */     DataResultSet passSet = createSecuritySet();
/* 332 */     populatePasswordSet(props, passSet, source, args, true);
/*     */ 
/* 334 */     updatePasswords(passSet, props, args);
/*     */ 
/* 336 */     boolean isInPlace = StringUtils.convertToBool((String)args.get("isInPlace"), false);
/* 337 */     if (!isInPlace)
/*     */     {
/* 339 */       for (passSet.first(); passSet.isRowPresent(); passSet.next())
/*     */       {
/* 341 */         Map map = passSet.getCurrentRowMap();
/* 342 */         boolean isUpdated = StringUtils.convertToBool((String)map.get("isUpdated"), false);
/* 343 */         if (!isUpdated)
/*     */           continue;
/* 345 */         String field = (String)map.get("field");
/* 346 */         String encField = (String)map.get("encodingField");
/*     */ 
/* 348 */         props.put(field, "managed");
/* 349 */         props.put(encField, "managed");
/*     */       }
/*     */     }
/*     */ 
/* 353 */     return passSet;
/*     */   }
/*     */ 
/*     */   public static void populatePasswordSet(Map props, DataResultSet passSet, String source, Map args, boolean allowOverwrite)
/*     */     throws DataException, ServiceException
/*     */   {
/* 359 */     Map paramMap = new HashMap();
/* 360 */     Parameters params = new MapParameters(paramMap);
/* 361 */     paramMap.put("source", source);
/* 362 */     paramMap.put("isUpdated", "0");
/*     */ 
/* 364 */     int fieldIndex = ResultSetUtils.getIndexMustExist(passSet, "field");
/* 365 */     int scopeIndex = ResultSetUtils.getIndexMustExist(passSet, "scope");
/*     */ 
/* 367 */     Set set = props.keySet();
/* 368 */     for (String key : set)
/*     */     {
/* 370 */       String[] encKey = new String[1];
/* 371 */       boolean isPasswordField = isPasswordField(key, encKey);
/* 372 */       if (!isPasswordField)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 377 */       String passwrd = (String)props.get(key);
/* 378 */       if (passwrd.equals("managed"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 385 */       String scope = (String)props.get("PasswordScope");
/* 386 */       if (scope == null)
/*     */       {
/* 388 */         if (args != null)
/*     */         {
/* 390 */           scope = (String)args.get("PasswordScope");
/*     */         }
/* 392 */         if (scope == null)
/*     */         {
/* 394 */           scope = "";
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 399 */       List row = ResultSetUtils.findDualIndexedRow(passSet, key, scope, fieldIndex, scopeIndex);
/*     */ 
/* 401 */       if ((row != null) && (!allowOverwrite))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 406 */       String encType = (String)props.get(encKey[0]);
/* 407 */       if (encType == null)
/*     */       {
/* 409 */         encType = "";
/*     */       }
/*     */ 
/* 412 */       paramMap.put("field", key);
/* 413 */       paramMap.put("password", passwrd);
/* 414 */       paramMap.put("encoding", encType);
/* 415 */       paramMap.put("encodingField", encKey[0]);
/* 416 */       paramMap.put("scope", scope);
/*     */ 
/* 418 */       Vector newRow = passSet.createRow(params);
/* 419 */       if (row == null)
/*     */       {
/* 421 */         passSet.addRow(newRow);
/*     */       }
/*     */       else
/*     */       {
/* 425 */         int index = passSet.getCurrentRow();
/* 426 */         passSet.setRowValues(newRow, index);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataResultSet createSecuritySet()
/*     */   {
/* 433 */     DataResultSet passSet = new DataResultSet(new String[] { "field", "password", "encoding", "source", "encodingField", "scope", "isUpdated" });
/*     */ 
/* 435 */     return passSet;
/*     */   }
/*     */ 
/*     */   public static String determineCategory(String field)
/*     */     throws DataException
/*     */   {
/* 441 */     if (!m_isInitialized)
/*     */     {
/* 443 */       initSecurityObjectsWithDataException(IdcMessageFactory.lc("csUnableToLookupPassword", new Object[] { field }));
/*     */     }
/* 445 */     return m_sObjects.determineCategory(field);
/*     */   }
/*     */ 
/*     */   public static boolean isPasswordField(String key, String[] encKey)
/*     */     throws DataException
/*     */   {
/* 463 */     boolean isPasswordField = false;
/* 464 */     if (!m_isInitialized)
/*     */     {
/* 466 */       initSecurityObjectsWithDataException(IdcMessageFactory.lc("csUnableToLookupPassword", new Object[] { key }));
/*     */     }
/*     */ 
/* 477 */     Map catInfo = null;
/* 478 */     if (m_sObjects != null)
/*     */     {
/* 480 */       catInfo = m_sObjects.determineCategoryFieldInfo(key);
/*     */     }
/* 482 */     if ((catInfo != null) || ((!m_isInitialized) && (key.endsWith("Password")) && (!key.startsWith("#")) && (((!key.startsWith("Use")) || (key.startsWith("User")))) && (!key.startsWith("Is"))))
/*     */     {
/* 486 */       isPasswordField = true;
/* 487 */       if ((encKey != null) && (encKey.length > 0))
/*     */       {
/* 489 */         if (catInfo != null)
/*     */         {
/* 491 */           encKey[0] = ((String)catInfo.get("scCategoryEncodingField"));
/*     */         }
/*     */         else
/*     */         {
/* 495 */           encKey[0] = (key + "Encoding");
/*     */         }
/*     */       }
/*     */     }
/* 499 */     return isPasswordField;
/*     */   }
/*     */ 
/*     */   public static boolean lookupPasswordInfo(PasswordInfo info)
/*     */     throws DataException
/*     */   {
/* 505 */     if (!m_isInitialized)
/*     */     {
/* 507 */       initSecurityObjectsWithDataException(IdcMessageFactory.lc("csUnableToLookupPassword", new Object[] { info.m_field }));
/*     */     }
/* 509 */     if (m_sObjects != null)
/*     */     {
/* 511 */       return m_sObjects.lookupPassword(info);
/*     */     }
/* 513 */     return false;
/*     */   }
/*     */ 
/*     */   public static String determinePassword(String field, DataBinder data, boolean isEnv)
/*     */     throws DataException
/*     */   {
/* 519 */     if (!m_isInitialized)
/*     */     {
/* 521 */       initSecurityObjectsWithDataException(IdcMessageFactory.lc("csDecryptPasswordError", new Object[] { field, null }));
/*     */     }
/*     */ 
/* 524 */     Map catMap = null;
/* 525 */     if (m_sObjects != null)
/*     */     {
/* 527 */       catMap = m_sObjects.determineCategoryFieldInfo(field);
/*     */     }
/* 529 */     String encField = field + "Encoding";
/* 530 */     if (catMap != null)
/*     */     {
/* 532 */       encField = (String)catMap.get("scCategoryEncodingField");
/*     */     }
/*     */ 
/* 535 */     String password = null;
/* 536 */     String algorithm = null;
/* 537 */     String scope = null;
/* 538 */     if ((data == null) || (isEnv))
/*     */     {
/* 540 */       Properties environment = m_environment;
/* 541 */       if (environment == null)
/*     */       {
/* 543 */         Report.deprecatedUsage("Using AppObjectRepository to get environment in CryptoPasswordUtils.");
/*     */ 
/* 545 */         environment = (Properties)AppObjectRepository.getObject("environment");
/*     */       }
/* 547 */       password = environment.getProperty(field);
/* 548 */       algorithm = environment.getProperty(encField);
/* 549 */       scope = environment.getProperty("PasswordScope");
/*     */     }
/*     */     else
/*     */     {
/* 553 */       password = data.getAllowMissing(field);
/* 554 */       algorithm = data.getAllowMissing(encField);
/* 555 */       scope = data.getAllowMissing("PasswordScope");
/*     */     }
/* 557 */     if ((password == null) || (password.length() == 0))
/*     */     {
/* 559 */       return password;
/*     */     }
/* 561 */     if (scope == null)
/*     */     {
/* 563 */       if (data != null)
/*     */       {
/* 565 */         scope = data.getAllowMissing("pName");
/*     */       }
/* 567 */       if (scope == null)
/*     */       {
/* 569 */         scope = "system";
/*     */       }
/*     */     }
/*     */ 
/* 573 */     PasswordInfo info = new PasswordInfo(password, algorithm, scope, catMap);
/* 574 */     if (info.isUnencrypted())
/*     */     {
/* 576 */       if (info.hasExtraEncoding())
/*     */       {
/* 578 */         password = CryptoCommonUtils.uuencodeHashWithDigest(password, null, info.m_extraEncoding);
/*     */       }
/*     */ 
/*     */     }
/* 584 */     else if (catMap != null)
/*     */     {
/* 586 */       info.m_field = field;
/* 587 */       info.m_scope = scope;
/* 588 */       lookupPasswordInfo(info);
/*     */       try
/*     */       {
/* 591 */         password = decrypt(info);
/*     */       }
/*     */       catch (ServiceException initException)
/*     */       {
/* 597 */         AssertionError err = new AssertionError("!$Initialization error thrown  after initialization complete.");
/*     */ 
/* 599 */         SystemUtils.setExceptionCause(err, initException);
/* 600 */         throw err;
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 605 */       Report.warning("system", null, "csPasswordFieldCategoryMissing", new Object[] { field });
/*     */     }
/*     */ 
/* 608 */     return password;
/*     */   }
/*     */ 
/*     */   public static DataResultSet loadMasterKeys() throws DataException, ServiceException
/*     */   {
/* 613 */     if (!m_isInitialized)
/*     */     {
/* 615 */       initSecurityObjects();
/*     */     }
/* 617 */     return m_sObjects.getMasterKeys();
/*     */   }
/*     */ 
/*     */   public static Map loadMasterKeyForCategory(String category) throws DataException, ServiceException
/*     */   {
/* 622 */     DataResultSet masterKeys = loadMasterKeys();
/* 623 */     if (masterKeys != null)
/*     */     {
/* 625 */       int categoryIndex = masterKeys.getFieldInfoIndex("scCategory");
/* 626 */       int masterKeyIndex = masterKeys.getFieldInfoIndex("scMasterKey");
/* 627 */       int isActiveIndex = masterKeys.getFieldInfoIndex("scIsActive");
/* 628 */       for (masterKeys.first(); masterKeys.isRowPresent(); masterKeys.next())
/*     */       {
/* 630 */         String cat = masterKeys.getStringValue(categoryIndex);
/* 631 */         if ((cat.equals(category)) && (!masterKeys.getStringValue(masterKeyIndex).startsWith("[")) && (StringUtils.convertToBool(masterKeys.getStringValue(isActiveIndex), false)))
/*     */         {
/* 634 */           return masterKeys.getCurrentRowMap();
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 639 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 644 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98095 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.conversion.CryptoPasswordUtils
 * JD-Core Version:    0.5.4
 */