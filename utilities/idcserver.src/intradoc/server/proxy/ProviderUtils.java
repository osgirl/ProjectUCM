/*     */ package intradoc.server.proxy;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.Validation;
/*     */ import intradoc.conversion.CryptoPasswordUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataBinderUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.provider.Provider;
/*     */ import intradoc.provider.Providers;
/*     */ import intradoc.server.utils.ServerInstallUtils;
/*     */ import intradoc.shared.PluginFilters;
/*     */ import intradoc.shared.SecurityUtils;
/*     */ import intradoc.shared.SharedLoader;
/*     */ import intradoc.shared.UserData;
/*     */ import intradoc.util.IdcMessage;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Enumeration;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ProviderUtils
/*     */ {
/*  37 */   public static final String[] DEFAULT_PROVIDER_FIELDS = { "ProviderName", "ProviderDescription", "IDC_Name", "InstanceMenuLabel", "InstanceDescription", "IntradocServerHostName", "HttpRelativeWebRoot", "IsImplicitlySearched", "UserAccounts" };
/*     */ 
/*     */   public static DataResultSet createProviderListForUser(String type, String booleanName, String userCriteriaPrefix, UserData userData, boolean strictAccessChecks, String[] additionalFields)
/*     */   {
/*  67 */     String[] fieldList = DEFAULT_PROVIDER_FIELDS;
/*  68 */     if (additionalFields != null)
/*     */     {
/*  70 */       int l1 = DEFAULT_PROVIDER_FIELDS.length;
/*  71 */       int l2 = additionalFields.length;
/*  72 */       fieldList = new String[l1 + l2];
/*  73 */       System.arraycopy(DEFAULT_PROVIDER_FIELDS, 0, fieldList, 0, l1);
/*  74 */       System.arraycopy(additionalFields, 0, fieldList, l1, l2);
/*     */     }
/*     */ 
/*  77 */     DataResultSet collections = new DataResultSet(fieldList);
/*     */ 
/*  79 */     Vector list = Providers.getProviderList();
/*  80 */     if (type != null)
/*     */     {
/*  82 */       type = type.toLowerCase();
/*     */     }
/*     */ 
/*  85 */     int size = list.size();
/*  86 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  88 */       Provider p = (Provider)list.elementAt(i);
/*  89 */       DataBinder binder = p.getProviderData();
/*     */ 
/*  93 */       if (type != null)
/*     */       {
/*  95 */         String pType = binder.getLocal("ProviderType");
/*  96 */         if (pType != null)
/*     */         {
/*  98 */           pType = pType.toLowerCase();
/*     */         }
/* 100 */         if (pType.indexOf(type) < 0) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 106 */       if (booleanName != null)
/*     */       {
/* 108 */         String val = binder.getAllowMissing(booleanName);
/* 109 */         if (!StringUtils.convertToBool(val, false)) {
/*     */           continue;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 115 */       SharedLoader.initIdcName(binder.getLocalData(), 0);
/*     */ 
/* 117 */       String[] accounts = new String[1];
/* 118 */       if (!checkProviderAccess(userData, binder, userCriteriaPrefix, accounts, strictAccessChecks))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 124 */       Vector v = new IdcVector();
/* 125 */       for (int j = 0; j < fieldList.length; ++j)
/*     */       {
/* 127 */         String name = fieldList[j];
/* 128 */         String value = null;
/* 129 */         if (name.equals("UserAccounts"))
/*     */         {
/* 131 */           value = accounts[0];
/*     */         }
/*     */         else
/*     */         {
/* 135 */           value = binder.getAllowMissing(name);
/*     */         }
/* 137 */         if (value == null)
/*     */         {
/* 139 */           value = "";
/*     */         }
/* 141 */         v.addElement(value);
/*     */       }
/* 143 */       collections.addRow(v);
/*     */     }
/*     */ 
/* 146 */     return collections;
/*     */   }
/*     */ 
/*     */   public static boolean checkProviderAccess(UserData userData, DataBinder binder, String prefix, String[] accountList, boolean strict)
/*     */   {
/* 164 */     if (prefix == null)
/*     */     {
/* 166 */       prefix = "";
/*     */     }
/*     */ 
/* 169 */     boolean deny = false;
/* 170 */     String tmp = binder.getLocal(prefix + "RequiredRoles");
/* 171 */     if ((tmp == null) && (strict))
/*     */     {
/* 173 */       tmp = "admin";
/*     */     }
/*     */ 
/* 176 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 178 */       deny = true;
/* 179 */       Vector list = StringUtils.parseArray(tmp, ',', '^');
/* 180 */       int size = list.size();
/* 181 */       for (int j = 0; (j < size) && (deny); ++j)
/*     */       {
/* 183 */         if (!SecurityUtils.isUserOfRole(userData, (String)list.elementAt(j)))
/*     */           continue;
/* 185 */         deny = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 190 */     String accounts = SecurityUtils.getAccountPackagedList(userData);
/* 191 */     tmp = binder.getLocal(prefix + "AccountFilter");
/* 192 */     if ((tmp == null) && (strict))
/*     */     {
/* 194 */       tmp = "#all";
/*     */     }
/* 196 */     if ((tmp != null) && (tmp.length() > 0))
/*     */     {
/* 198 */       if (accounts.equals("#none"))
/*     */       {
/* 200 */         accounts = "";
/*     */       }
/* 202 */       else if (!accounts.equals("#all"))
/*     */       {
/* 208 */         Vector tmpAccounts = SecurityUtils.getUserAccountsWithPrivilege(userData, 1, true);
/*     */ 
/* 210 */         String[] allowedAccounts = new String[tmpAccounts.size()];
/* 211 */         tmpAccounts.copyInto(allowedAccounts);
/*     */ 
/* 213 */         Vector finalAccounts = new IdcVector();
/*     */ 
/* 216 */         Vector knownAccounts = StringUtils.parseArray(tmp, ',', '^');
/*     */ 
/* 218 */         SecurityUtils.addAccountsFiltered(userData, finalAccounts, knownAccounts, allowedAccounts, true, false, null);
/*     */ 
/* 220 */         accounts = StringUtils.createString(finalAccounts, ',', '^');
/*     */       }
/*     */     }
/* 223 */     if (accounts.length() == 0)
/*     */     {
/* 225 */       deny = true;
/*     */     }
/* 227 */     if (accountList != null)
/*     */     {
/* 229 */       accountList[0] = accounts;
/*     */     }
/*     */ 
/* 232 */     return !deny;
/*     */   }
/*     */ 
/*     */   public static Provider getProvider(DataBinder providerData)
/*     */   {
/* 237 */     String provName = providerData.getAllowMissing("ProviderName");
/* 238 */     Provider provider = null;
/* 239 */     if (provName != null)
/*     */     {
/* 241 */       provider = Providers.getProvider(provName);
/*     */     }
/* 243 */     return provider;
/*     */   }
/*     */ 
/*     */   public static void addOrEditProvider(boolean isValidate, Vector keysToChange, DataBinder pBinder, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 256 */       PluginFilters.filter("preWriteProviderData", ws, pBinder, cxt);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 260 */       Report.trace("provider", "Unable to complete preWriterProviderData filter", e);
/*     */     }
/*     */ 
/* 263 */     String pName = pBinder.get("pName");
/* 264 */     String pType = pBinder.get("pType");
/* 265 */     boolean isEdit = StringUtils.convertToBool(pBinder.getLocal("isEdit"), false);
/*     */ 
/* 267 */     if (Providers.isReservedName(pName))
/*     */     {
/* 269 */       throw new ServiceException(null, "csProviderSystemEditError", new Object[] { pName });
/*     */     }
/*     */ 
/* 272 */     if (isValidate)
/*     */     {
/* 275 */       validateProvider(pName, pType, pBinder, ws, cxt);
/*     */     }
/*     */ 
/* 278 */     String providerDir = ProviderFileUtils.m_directory;
/* 279 */     FileUtils.reserveDirectory(providerDir, true);
/*     */     try
/*     */     {
/* 283 */       DataBinder provData = createProviderData(pName, pType, isEdit, keysToChange, pBinder, ws, cxt);
/*     */ 
/* 286 */       DataBinder binder = ProviderFileUtils.checkAndLoadProviders();
/* 287 */       DataResultSet drset = (DataResultSet)binder.getResultSet("Providers");
/* 288 */       Vector row = drset.findRow(0, pName);
/*     */ 
/* 290 */       Provider provider = null;
/* 291 */       boolean isStandard = false;
/* 292 */       if (isEdit)
/*     */       {
/* 294 */         provider = Providers.getFromAllProviders(pName);
/*     */ 
/* 298 */         DataBinder curData = Providers.getProviderData(pName);
/* 299 */         isStandard = DataBinderUtils.getBoolean(curData, "IsAllowConfigSystemProvider", false);
/*     */       }
/* 301 */       boolean forceWrite = false;
/* 302 */       if ((isEdit) && (!isStandard))
/*     */       {
/* 304 */         if (row == null)
/*     */         {
/* 306 */           String msg = LocaleUtils.encodeMessage("csProviderUnableToEdit", null, pName);
/*     */ 
/* 308 */           msg = LocaleUtils.appendMessage("!csProviderDoesNotExist3", msg);
/* 309 */           throw new ServiceException(null, msg, new Object[0]);
/*     */         }
/*     */ 
/* 314 */         int index = ResultSetUtils.getIndexMustExist(drset, "pDescription");
/* 315 */         String oldDesc = (String)row.elementAt(index);
/* 316 */         String newDesc = pBinder.getLocal("pDescription");
/*     */ 
/* 320 */         if (((oldDesc == null) && (newDesc != null)) || (!oldDesc.equals(newDesc)))
/*     */         {
/* 322 */           row.setElementAt(newDesc, index);
/* 323 */           forceWrite = true;
/*     */         }
/*     */ 
/* 326 */         oldDesc = provData.getLocal("pDescription");
/* 327 */         if ((oldDesc != null) && (!oldDesc.equals(newDesc)))
/*     */         {
/* 329 */           provData.putLocal("pDescription", newDesc);
/* 330 */           provData.putLocal("ProviderDescription", newDesc);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 335 */         if (row != null)
/*     */         {
/* 337 */           throw new ServiceException(null, -17, "csProviderAlreadyExists", new Object[0]);
/*     */         }
/*     */ 
/* 341 */         row = drset.createRow(pBinder);
/* 342 */         drset.addRow(row);
/* 343 */         forceWrite = true;
/*     */       }
/*     */ 
/* 346 */       ProviderFileUtils.writeProviderFile(pName, provData, false);
/* 347 */       if (forceWrite)
/*     */       {
/* 349 */         ProviderFileUtils.writeProvidersFile(binder, false);
/*     */       }
/*     */ 
/* 353 */       String state = "editAdd";
/* 354 */       if (provider == null)
/*     */       {
/* 356 */         provider = new Provider(provData);
/* 357 */         Providers.addToAllProviderList(pName, provider);
/*     */ 
/* 361 */         if (ServerInstallUtils.isCatalogServer())
/*     */         {
/* 363 */           provider.markState("enabled");
/* 364 */           provider.init();
/* 365 */           Providers.addProvider(pName, provider);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 370 */         state = "edit";
/* 371 */         Properties providerState = provider.getProviderState();
/* 372 */         if (isStandard)
/*     */         {
/* 374 */           DataBinder curProvData = provider.getProviderData();
/* 375 */           curProvData.removeLocal("IsAllowConfigSystemProvider");
/*     */         }
/* 377 */         boolean isNew = StringUtils.convertToBool(providerState.getProperty("IsNew"), false);
/* 378 */         if (isNew)
/*     */         {
/* 380 */           provider.setProviderData(provData);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 385 */       provData.putLocal("ProviderName", pName);
/* 386 */       provData.putLocal("ProviderDescription", pBinder.getLocal("pDescription"));
/*     */ 
/* 388 */       Providers.addProviderData(pName, provData);
/* 389 */       provider.markState(state);
/*     */       try
/*     */       {
/* 393 */         PluginFilters.filter("postWriteProviderData", ws, binder, cxt);
/*     */       }
/*     */       catch (Exception e)
/*     */       {
/* 397 */         Report.trace("provider", "Unable to complete postWriterProviderData filter", e);
/*     */       }
/*     */     }
/*     */     finally
/*     */     {
/* 402 */       FileUtils.releaseDirectory(providerDir, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static DataBinder createProviderData(String name, String type, boolean isEdit, List keysToChange, DataBinder pBinder, Workspace ws, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 410 */     DataBinder provData = null;
/* 411 */     if (isEdit)
/*     */     {
/* 413 */       Provider provider = Providers.getFromAllProviders(name);
/* 414 */       DataBinder pd = provider.getProviderData();
/* 415 */       DataBinder curProvData = Providers.getProviderData(name);
/* 416 */       boolean isStandard = DataBinderUtils.getBoolean(pd, "IsAllowConfigSystemProvider", false);
/* 417 */       if (isStandard)
/*     */       {
/* 419 */         provData = new DataBinder();
/* 420 */         provData.merge(curProvData);
/* 421 */         provData.removeLocal("IsAllowConfigSystemProvider");
/*     */       }
/*     */       else
/*     */       {
/* 425 */         provData = ProviderFileUtils.readProviderFile(name, false);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 430 */       provData = new DataBinder();
/*     */     }
/*     */ 
/* 434 */     DataBinder oldProvData = new DataBinder();
/* 435 */     oldProvData.merge(provData);
/* 436 */     cxt.setCachedObject("OldProviderData", oldProvData);
/* 437 */     cxt.setCachedObject("ProviderData", provData);
/*     */ 
/* 440 */     String str = pBinder.getLocal("ExtraFields");
/* 441 */     List extraFields = StringUtils.parseArray(str, ',', '^');
/* 442 */     Object[] filterParams = { name, type, (isEdit) ? Boolean.TRUE : Boolean.FALSE, keysToChange, extraFields };
/*     */ 
/* 444 */     cxt.setCachedObject("providerGetStandardFields:params", filterParams);
/* 445 */     executeFilter("providerGetStandardFields", ws, pBinder, cxt);
/*     */ 
/* 448 */     type = (String)filterParams[1];
/* 449 */     isEdit = ((Boolean)(Boolean)filterParams[2]).booleanValue();
/* 450 */     keysToChange = (List)(List)filterParams[3];
/* 451 */     extraFields = (List)(List)filterParams[4];
/*     */ 
/* 453 */     List stdFields = null;
/* 454 */     if (keysToChange != null)
/*     */     {
/* 456 */       stdFields = keysToChange;
/*     */     }
/*     */     else
/*     */     {
/* 460 */       stdFields = ProviderValidation.getStandardFields(type);
/*     */     }
/*     */ 
/* 463 */     int stdNum = stdFields.size();
/* 464 */     int total = extraFields.size() + stdNum;
/* 465 */     for (int i = 0; i < total; ++i)
/*     */     {
/* 467 */       String key = null;
/* 468 */       String keyType = "text";
/* 469 */       if (i < stdNum)
/*     */       {
/* 471 */         String[] keyMap = (String[])(String[])stdFields.get(i);
/* 472 */         key = keyMap[0];
/* 473 */         keyType = keyMap[1];
/*     */       }
/*     */       else
/*     */       {
/* 477 */         key = (String)extraFields.get(i - stdNum);
/*     */       }
/*     */ 
/* 481 */       String value = pBinder.getLocal(key);
/* 482 */       String[] encKey = new String[1];
/* 483 */       if ((CryptoPasswordUtils.isPasswordField(key, encKey)) && (value != null))
/*     */       {
/* 485 */         if (!value.equals("*****"))
/*     */         {
/* 487 */           provData.putLocal(key, value);
/* 488 */           provData.putLocal(encKey[0], "ClearText");
/*     */         }
/*     */         else
/*     */         {
/* 493 */           provData.putLocal("IsStripPasswords", "0");
/*     */         }
/*     */       }
/* 496 */       else if (value != null)
/*     */       {
/* 498 */         provData.putLocal(key, value);
/*     */       }
/* 500 */       else if (keyType.equals("bool"))
/*     */       {
/* 502 */         provData.removeLocal(key);
/*     */       }
/*     */       else
/*     */       {
/* 506 */         provData.putLocal(key, "");
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 511 */     str = pBinder.getLocal("AdditionalSettings");
/* 512 */     if (str != null)
/*     */     {
/* 514 */       removeOldExtraSettings(provData);
/* 515 */       provData.putLocal("AdditionalSettings", str);
/*     */     }
/* 517 */     Properties props = new Properties();
/* 518 */     StringUtils.parseProperties(props, str);
/*     */ 
/* 520 */     for (Enumeration keys = props.propertyNames(); keys.hasMoreElements(); )
/*     */     {
/* 522 */       String key = (String)keys.nextElement();
/* 523 */       if (provData.getLocal(key) == null)
/*     */       {
/* 525 */         String value = props.getProperty(key);
/* 526 */         provData.putLocal(key, value);
/*     */       }
/*     */       else
/*     */       {
/* 530 */         Report.trace("provider", "Replacing key (" + key + ") via AdditionalSettings is not permitted.", null);
/*     */ 
/* 532 */         throw new ServiceException(null, "csReplaceKeyViaAdditionalSetting", new Object[] { key });
/*     */       }
/*     */     }
/*     */ 
/* 536 */     PluginFilters.filter("createProviderData", ws, pBinder, cxt);
/* 537 */     return provData;
/*     */   }
/*     */ 
/*     */   public static void validateProvider(String name, String type, DataBinder pBinder, Workspace ws, ExecutionContext cxt)
/*     */     throws ServiceException, DataException
/*     */   {
/* 545 */     String[][] requiredFields = { { "pName", "ProviderName" }, { "pDescription", "ProviderDescription" }, { "pType", "ProviderType" }, { "ProviderClass", "ProviderClass" } };
/*     */ 
/* 551 */     int num = requiredFields.length;
/* 552 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 554 */       String value = pBinder.getLocal(requiredFields[i][0]);
/* 555 */       if ((value == null) || (value.trim().length() == 0))
/*     */       {
/* 557 */         throw new ServiceException(null, "csRequiredFieldMissing", new Object[] { "csProviderFieldName_" + requiredFields[i][1] });
/*     */       }
/*     */ 
/* 560 */       pBinder.putLocal(requiredFields[i][1], value);
/*     */     }
/*     */ 
/* 564 */     IdcMessage errMsg = Validation.checkUrlFileSegmentForDB(name, "csProviderFieldName_ProviderName", 0, null);
/*     */ 
/* 566 */     if (errMsg != null)
/*     */     {
/* 568 */       throw new ServiceException(null, errMsg);
/*     */     }
/*     */ 
/* 571 */     ProviderValidation.validateDefaults(type, pBinder);
/*     */ 
/* 573 */     PluginFilters.filter("validateProvider", ws, pBinder, cxt);
/*     */   }
/*     */ 
/*     */   public static void removeOldExtraSettings(DataBinder binder)
/*     */   {
/* 578 */     String oldSettings = binder.getLocal("AdditionalSettings");
/* 579 */     if ((oldSettings == null) || (oldSettings.length() == 0))
/*     */     {
/* 581 */       return;
/*     */     }
/*     */ 
/* 584 */     Properties props = new Properties();
/* 585 */     StringUtils.parseProperties(props, oldSettings);
/*     */ 
/* 587 */     for (Enumeration keys = props.propertyNames(); keys.hasMoreElements(); )
/*     */     {
/* 589 */       String key = (String)keys.nextElement();
/* 590 */       binder.removeLocal(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean executeFilter(String filter, Workspace ws, DataBinder binder, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/*     */     try
/*     */     {
/* 600 */       int ret = PluginFilters.filter(filter, ws, binder, cxt);
/* 601 */       if (ret == -1)
/*     */       {
/* 603 */         return false;
/*     */       }
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 608 */       throw new ServiceException(e, "csFilterError", new Object[] { filter });
/*     */     }
/* 610 */     return true;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 615 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99231 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.proxy.ProviderUtils
 * JD-Core Version:    0.5.4
 */