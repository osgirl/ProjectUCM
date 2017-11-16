/*     */ package intradoc.filterdata;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserData;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class FilterDataInputEventImplement
/*     */ {
/*     */   public static final int F_IS_SCRIPTABLE = 1;
/*     */   public static final int F_ALWAYS_COPY_OVER_VALUES = 2;
/*     */   public static final int F_IS_NO_PAGE_RESPONSE = 4;
/*     */   public static final int F_IS_ADMIN_SERVICE = 8;
/*  48 */   public boolean m_isInit = false;
/*     */ 
/*  53 */   public int m_filterInputLevel = 0;
/*     */ 
/*  58 */   public int m_scriptableFilterInputLevel = 0;
/*     */ 
/*  63 */   public boolean m_encodeDocUserFieldsAsExceptSafe = false;
/*     */ 
/*  70 */   public String[] m_environmentVarsToFilter = { "QUERY_STRING", "ORIGINALURL", "DESTINATION" };
/*     */ 
/*  77 */   public Map m_specialFilterRules = new HashMap();
/*     */ 
/*     */   public void checkInit(ExecutionContext cxt)
/*     */   {
/*  93 */     if (this.m_isInit)
/*     */       return;
/*  95 */     checkInitImplement(cxt);
/*     */   }
/*     */ 
/*     */   protected synchronized void checkInitImplement(ExecutionContext cxt)
/*     */   {
/* 104 */     if (this.m_isInit)
/*     */     {
/* 106 */       return;
/*     */     }
/* 108 */     HtmlFilterUtils.checkInit();
/*     */ 
/* 110 */     String filterLevel = SharedObjects.getEnvironmentValue("HtmlDataInputFilterLevel");
/* 111 */     if ((filterLevel == null) || (filterLevel.length() == 0))
/*     */     {
/* 113 */       filterLevel = "unsafe";
/*     */     }
/*     */ 
/* 117 */     this.m_filterInputLevel = HtmlFilterUtils.translateEncodingRule(filterLevel, this.m_filterInputLevel);
/* 118 */     if (this.m_filterInputLevel > 1)
/*     */     {
/* 120 */       this.m_scriptableFilterInputLevel = 1;
/*     */     }
/*     */     else
/*     */     {
/* 124 */       this.m_scriptableFilterInputLevel = this.m_filterInputLevel;
/*     */     }
/* 126 */     String scriptableFilterLevel = SharedObjects.getEnvironmentValue("HtmlDataScriptableInputFilterLevel");
/* 127 */     if ((scriptableFilterLevel != null) && (scriptableFilterLevel.length() > 0))
/*     */     {
/* 129 */       this.m_scriptableFilterInputLevel = HtmlFilterUtils.translateEncodingRule(filterLevel, this.m_scriptableFilterInputLevel);
/*     */     }
/*     */ 
/* 133 */     this.m_encodeDocUserFieldsAsExceptSafe = SharedObjects.getEnvValueAsBoolean("HtmlDataInputEncodeDocAndUserFieldsAsExceptSafe", false);
/*     */ 
/* 136 */     DataResultSet encodingRulesTable = SharedObjects.getTable("HtmlDataInputEncodingRulesForSpecialFields");
/* 137 */     int fieldNameIndex = -1;
/* 138 */     if (encodingRulesTable != null)
/*     */     {
/* 140 */       FieldInfo fi = new FieldInfo();
/* 141 */       if (encodingRulesTable.getFieldInfo("hdiField", fi))
/*     */       {
/* 143 */         fieldNameIndex = fi.m_index;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 148 */     extractSpecialEncodingRules(encodingRulesTable, fieldNameIndex, "hdiEncodingLevel", "HtmlDataInputCustomFieldLevels");
/* 149 */     extractFieldValues(encodingRulesTable, fieldNameIndex, "hdiEncodeDoubleQuotes", "HtmlDataInputEncodeDoubleQuotes", "HtmlDataInputEncodeDoubleQuotesExtraKeys", 0);
/*     */ 
/* 151 */     extractFieldValues(encodingRulesTable, fieldNameIndex, "hdiEncodeFullyAsXmlValue", "HtmlDataInputEncodeFully", "HtmlDataInputEncodeFullyExtraKeys", 1);
/*     */ 
/* 155 */     extractFieldValues(encodingRulesTable, fieldNameIndex, "hdiIsScriptingProtected", "HtmlDataInputProtectedScriptingParameters", "HtmlDataInputProtectedScriptingParametersExtraKeys", 2);
/*     */ 
/* 160 */     FilterDataInputSpecialOptions defaultSpecialOptions = HtmlFilterUtils.getDefaultOptions();
/* 161 */     defaultSpecialOptions.m_escapePotentiallyUnsafeCharacters = SharedObjects.getEnvValueAsBoolean("HtmlDataInputEscapePotentiallyUnsafeCharacters", defaultSpecialOptions.m_escapePotentiallyUnsafeCharacters);
/*     */ 
/* 164 */     String specialChars = SharedObjects.getEnvironmentValue("HtmlDataInputPotentiallyUnsafeCharacters");
/* 165 */     if (specialChars != null)
/*     */     {
/* 167 */       defaultSpecialOptions.m_potentiallyUnsafeCharacters = specialChars.toCharArray();
/*     */     }
/* 169 */     defaultSpecialOptions.m_encodeAdditionalUnsafe = SharedObjects.getEnvValueAsBoolean("HtmlDataInputEscapeAdditionalUnsafeTags", defaultSpecialOptions.m_encodeAdditionalUnsafe);
/*     */ 
/* 174 */     List extraList = SharedObjects.getEnvValueAsList("HtmlDataInputExtraUnsafeTags");
/* 175 */     if (extraList != null)
/*     */     {
/* 177 */       for (int i = 0; i < extraList.size(); ++i)
/*     */       {
/* 179 */         String tag = (String)extraList.get(i);
/* 180 */         if (tag.length() <= 0)
/*     */           continue;
/* 182 */         HtmlFilterUtils.setTagSafetyState(tag, false);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 187 */     List additionalList = SharedObjects.getEnvValueAsList("HtmlDataInputAdditionalUnsafeTags");
/* 188 */     if (additionalList != null)
/*     */     {
/* 190 */       String[] additionalTags = new String[additionalList.size()];
/* 191 */       for (int i = 0; i < additionalTags.length; ++i)
/*     */       {
/* 193 */         additionalTags[i] = ((String)additionalList.get(i));
/*     */       }
/*     */ 
/* 196 */       HtmlFilterUtils.setAdditionalUnsafeTags(additionalTags);
/*     */     }
/*     */ 
/* 199 */     this.m_isInit = true;
/*     */   }
/*     */ 
/*     */   public void extractSpecialEncodingRules(DataResultSet drset, int nameIndex, String fieldKey, String envAdditionalKey)
/*     */   {
/* 208 */     List levels = new ArrayList();
/* 209 */     if ((drset != null) && (nameIndex >= 0))
/*     */     {
/* 211 */       FieldInfo fi = new FieldInfo();
/* 212 */       if (drset.getFieldInfo(fieldKey, fi))
/*     */       {
/* 214 */         for (drset.first(); drset.isRowPresent(); drset.next())
/*     */         {
/* 216 */           String key = drset.getStringValue(nameIndex);
/* 217 */           String val = drset.getStringValue(fi.m_index);
/*     */ 
/* 219 */           levels.add(key);
/* 220 */           levels.add(val);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 225 */     SharedObjects.appendEnvValueAsList(envAdditionalKey, levels);
/*     */ 
/* 227 */     for (int i = 0; i < levels.size() - 1; i += 2)
/*     */     {
/* 229 */       String key = (String)levels.get(i);
/* 230 */       String rule = (String)levels.get(i + 1);
/* 231 */       FilterDataInputSpecialRules sRules = getOrCreateSpecialRule(key);
/*     */ 
/* 233 */       if (rule.length() <= 0)
/*     */         continue;
/* 235 */       sRules.m_encodingLevel = HtmlFilterUtils.translateEncodingRule(rule, 1);
/* 236 */       sRules.m_encodingLevelExplicitlySet = true;
/*     */     }
/*     */   }
/*     */ 
/*     */   public void extractFieldValues(DataResultSet drset, int nameIndex, String fieldKey, String envKey, String envAdditionalKey, int memberFieldID)
/*     */   {
/* 246 */     List list = SharedObjects.getEnvValueAsList(fieldKey);
/* 247 */     if (list == null)
/*     */     {
/* 250 */       list = new ArrayList();
/* 251 */       if ((drset != null) && (nameIndex >= 0))
/*     */       {
/* 253 */         FieldInfo fi = new FieldInfo();
/* 254 */         if (drset.getFieldInfo(fieldKey, fi))
/*     */         {
/* 256 */           for (drset.first(); drset.isRowPresent(); drset.next())
/*     */           {
/* 258 */             String val = drset.getStringValue(fi.m_index);
/* 259 */             if (!StringUtils.convertToBool(val, false))
/*     */               continue;
/* 261 */             String key = drset.getStringValue(nameIndex);
/* 262 */             list.add(key);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 268 */     SharedObjects.appendEnvValueAsList(envAdditionalKey, list);
/* 269 */     for (int i = 0; i < list.size(); ++i)
/*     */     {
/* 271 */       String key = (String)list.get(i);
/* 272 */       FilterDataInputSpecialRules sRules = getOrCreateSpecialRule(key);
/* 273 */       sRules.setBooleanAttribute(memberFieldID, true);
/*     */     }
/*     */   }
/*     */ 
/*     */   public FilterDataInputSpecialRules getOrCreateSpecialRule(String key)
/*     */   {
/* 282 */     FilterDataInputSpecialRules sRules = (FilterDataInputSpecialRules)this.m_specialFilterRules.get(key);
/* 283 */     if (sRules == null)
/*     */     {
/* 285 */       sRules = new FilterDataInputSpecialRules();
/* 286 */       this.m_specialFilterRules.put(key, sRules);
/*     */     }
/* 288 */     return sRules;
/*     */   }
/*     */ 
/*     */   public void filterEnvironmentData(DataBinder binder, ExecutionContext cxt, int flags, IdcStringBuilder sb)
/*     */     throws DataException, ServiceException
/*     */   {
/* 298 */     boolean isScriptable = (flags & 0x1) != 0;
/* 299 */     for (int i = 0; i < this.m_environmentVarsToFilter.length; ++i)
/*     */     {
/* 301 */       String key = this.m_environmentVarsToFilter[i];
/* 302 */       String value = binder.getEnvironmentValue(key);
/* 303 */       if ((value == null) || (value.length() <= 0))
/*     */         continue;
/* 305 */       FilterDataInputSpecialRules specialRule = (FilterDataInputSpecialRules)this.m_specialFilterRules.get(key);
/* 306 */       int rule = getRuleForKey(key, specialRule, isScriptable);
/* 307 */       if (rule > 1)
/*     */       {
/* 309 */         rule = 1;
/*     */       }
/* 311 */       sb.setLength(0);
/* 312 */       if ((!HtmlFilterUtils.encodeForHtmlView(value, rule, null, sb, cxt)) || 
/* 314 */         (sb.compareTo(0, sb.length(), value, 0, value.length(), false) == 0))
/*     */         continue;
/* 316 */       String convertedVal = sb.toStringNoRelease();
/*     */ 
/* 321 */       boolean isActiveTrace = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("encoding"));
/* 322 */       reportChangedEncoding(key, value, convertedVal, specialRule, rule, true, true, isActiveTrace, cxt);
/* 323 */       binder.setEnvironmentValue(key, convertedVal);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void filterDataInput(Map inData, Map outData, ExecutionContext cxt, int flags, IdcStringBuilder sb)
/*     */     throws DataException, ServiceException
/*     */   {
/* 339 */     boolean isScriptable = (flags & 0x1) != 0;
/* 340 */     boolean alwaysMoveOverValue = (flags & 0x2) != 0;
/* 341 */     boolean isAdmin = (flags & 0x8) != 0;
/* 342 */     boolean noPageResponse = (flags & 0x4) != 0;
/* 343 */     FilterDataInputSpecialOptions specialOptions = HtmlFilterUtils.shallowCloneDefaultOptions();
/* 344 */     boolean originalEncodeAdditional = specialOptions.m_encodeAdditionalUnsafe;
/* 345 */     Iterator it = inData.keySet().iterator();
/* 346 */     while (it.hasNext())
/*     */     {
/* 348 */       String key = (String)it.next();
/* 349 */       String val = (String)inData.get(key);
/* 350 */       String convertedVal = val;
/* 351 */       boolean changedIt = false;
/* 352 */       boolean reportEncoding = false;
/*     */ 
/* 355 */       specialOptions.m_encodeAdditionalUnsafe = originalEncodeAdditional;
/*     */ 
/* 358 */       boolean doHtmlEncoding = true;
/* 359 */       int rule = 0;
/* 360 */       FilterDataInputSpecialRules specialRule = (FilterDataInputSpecialRules)this.m_specialFilterRules.get(key);
/* 361 */       if ((specialRule != null) && (convertedVal != null))
/*     */       {
/* 363 */         if (specialRule.m_encodeFullyAsXmlValue)
/*     */         {
/* 365 */           sb.setLength(0);
/* 366 */           if (HtmlFilterUtils.encodeFullyForHtml(convertedVal, sb, cxt))
/*     */           {
/* 368 */             convertedVal = sb.toStringNoRelease();
/* 369 */             changedIt = true;
/*     */ 
/* 372 */             reportEncoding = true;
/*     */           }
/*     */ 
/* 376 */           doHtmlEncoding = false;
/*     */         }
/* 378 */         else if (specialRule.m_encodeDoubleQuotes)
/*     */         {
/* 381 */           String newVal = encodeDoubleQuotes(convertedVal);
/* 382 */           changedIt = !convertedVal.equals(newVal);
/*     */ 
/* 386 */           specialOptions.m_encodeAdditionalUnsafe = true;
/*     */ 
/* 389 */           reportEncoding = true;
/*     */         }
/* 391 */         else if (specialRule.m_isScriptingProtected)
/*     */         {
/* 396 */           specialOptions.m_encodeAdditionalUnsafe = true;
/*     */ 
/* 399 */           reportEncoding = true;
/*     */         }
/*     */       }
/* 402 */       if (doHtmlEncoding)
/*     */       {
/* 404 */         rule = getRuleForKey(key, specialRule, isScriptable);
/* 405 */         if (rule != 0)
/*     */         {
/* 407 */           if ((isAdmin) && (((!isScriptable) || (noPageResponse))) && (rule > 1))
/*     */           {
/* 409 */             rule = 1;
/*     */           }
/* 411 */           sb.setLength(0);
/* 412 */           if ((HtmlFilterUtils.encodeForHtmlView(convertedVal, rule, specialOptions, sb, cxt)) && 
/* 414 */             (sb.compareTo(0, sb.length(), convertedVal, 0, convertedVal.length(), false) != 0))
/*     */           {
/* 416 */             convertedVal = sb.toStringNoRelease();
/* 417 */             changedIt = true;
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 423 */       if ((changedIt) || (alwaysMoveOverValue))
/*     */       {
/* 425 */         if (changedIt)
/*     */         {
/* 428 */           boolean isActiveTrace = (SystemUtils.m_verbose) && (SystemUtils.isActiveTrace("encoding"));
/* 429 */           if ((isActiveTrace) || (reportEncoding))
/*     */           {
/* 431 */             reportChangedEncoding(key, val, convertedVal, specialRule, rule, doHtmlEncoding, reportEncoding, isActiveTrace, cxt);
/*     */           }
/*     */         }
/* 434 */         outData.put(key, convertedVal);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public void reportChangedEncoding(String key, String val, String convertedVal, FilterDataInputSpecialRules specialRule, int rule, boolean doHtmlEncoding, boolean reportEncoding, boolean isActiveTrace, ExecutionContext cxt)
/*     */   {
/* 442 */     if (isActiveTrace)
/*     */     {
/* 444 */       Report.trace("encoding", "Rule=" + rule + " doEncoding=" + doHtmlEncoding, null);
/* 445 */       if (specialRule != null)
/*     */       {
/* 447 */         Report.trace("encoding", "Special rule=" + specialRule.m_encodingLevel + " specified=" + specialRule.m_encodingLevelExplicitlySet + " doublequotes=" + specialRule.m_encodeDoubleQuotes + " fullyasxml=" + specialRule.m_encodeFullyAsXmlValue + " protected=" + specialRule.m_isScriptingProtected, null);
/*     */       }
/*     */       else
/*     */       {
/* 455 */         Report.trace("encoding", "No special rule for encoding", null);
/*     */       }
/*     */     }
/*     */ 
/* 459 */     String traceSection = (reportEncoding) ? "system" : "encoding";
/* 460 */     String reportMsg = null;
/* 461 */     if (reportEncoding)
/*     */     {
/* 463 */       reportMsg = "Unexpected need to encode " + key + " to have value " + convertedVal + " from original value " + val;
/*     */     }
/*     */     else
/*     */     {
/* 467 */       reportMsg = "Encoded " + key + " to have value " + convertedVal + " from original value " + val;
/*     */     }
/* 469 */     Report.trace(traceSection, reportMsg, null);
/* 470 */     String msg = createUserInfoReport(cxt, null);
/* 471 */     if (msg == null)
/*     */       return;
/* 473 */     msg = LocaleResources.localizeMessage(msg, null);
/* 474 */     Report.trace(traceSection, msg, null);
/*     */   }
/*     */ 
/*     */   public String createUserInfoReport(ExecutionContext cxt, String priorMsg)
/*     */   {
/* 480 */     String userHost = null;
/* 481 */     String user = null;
/* 482 */     String msg = null;
/* 483 */     if (cxt instanceof Service)
/*     */     {
/* 485 */       Service service = (Service)cxt;
/* 486 */       msg = service.encodeRefererMessage(null);
/* 487 */       UserData userData = service.getUserData();
/* 488 */       DataBinder binder = service.getBinder();
/* 489 */       userHost = binder.getEnvironmentValue("HTTP_HOST");
/* 490 */       user = (userData != null) ? userData.m_name : null;
/* 491 */       if ((userHost == null) || (userHost.length() == 0))
/*     */       {
/* 493 */         userHost = binder.getEnvironmentValue("RemoteClientHostName");
/* 494 */         if ((userHost == null) || (userHost.length() == 0))
/*     */         {
/* 496 */           userHost = binder.getEnvironmentValue("RemoteClientHostAddress");
/*     */         }
/*     */       }
/*     */     }
/* 500 */     if ((userHost != null) && (userHost.length() > 0) && (user != null) && (user.length() > 0))
/*     */     {
/* 502 */       msg = LocaleUtils.encodeMessage("csUserEventMessage", priorMsg, user, userHost);
/*     */     }
/*     */ 
/* 505 */     return msg;
/*     */   }
/*     */ 
/*     */   public String encodeDoubleQuotes(String val)
/*     */   {
/* 510 */     StringBuffer buf = new StringBuffer();
/* 511 */     for (int i = 0; i < val.length(); ++i)
/*     */     {
/* 513 */       char ch = val.charAt(i);
/* 514 */       if (ch == '"')
/*     */       {
/* 516 */         buf.append("&quot;");
/*     */       }
/*     */       else
/*     */       {
/* 520 */         buf.append(ch);
/*     */       }
/*     */     }
/* 523 */     return buf.toString();
/*     */   }
/*     */ 
/*     */   public int getRuleForKey(String key, FilterDataInputSpecialRules specialRule, boolean isScriptable)
/*     */   {
/* 528 */     int rule = (isScriptable) ? this.m_scriptableFilterInputLevel : this.m_filterInputLevel;
/* 529 */     if ((specialRule != null) && (specialRule.m_encodingLevelExplicitlySet))
/*     */     {
/* 531 */       rule = specialRule.m_encodingLevel;
/*     */     }
/* 535 */     else if (rule > 1)
/*     */     {
/* 541 */       if (((specialRule != null) && (specialRule.m_isScriptingProtected)) || ((!isScriptable) && (((key.endsWith(":default")) || (key.endsWith("wfCustomScript")) || (key.endsWith("wfAdditionalExitCondition"))))))
/*     */       {
/* 545 */         rule = 1;
/*     */       }
/*     */     }
/* 548 */     else if ((this.m_encodeDocUserFieldsAsExceptSafe) && (!isScriptable) && 
/* 550 */       (key.length() > 0))
/*     */     {
/* 553 */       char ch = key.charAt(0);
/* 554 */       if ((((ch == 'd') || (ch == 'x') || (ch == 'u'))) && (!key.endsWith(":default")))
/*     */       {
/* 556 */         rule = 2;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 562 */     return rule;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 568 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 69354 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.filterdata.FilterDataInputEventImplement
 * JD-Core Version:    0.5.4
 */