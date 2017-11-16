/*     */ package intradoc.common;
/*     */ 
/*     */ import java.util.Comparator;
/*     */ import java.util.Locale;
/*     */ 
/*     */ public class IdcLinguisticComparatorAdapter
/*     */   implements IdcLinguisticComparator
/*     */ {
/*     */   public Comparator m_comparator;
/*  31 */   public static boolean m_reportedError = false;
/*     */   public boolean m_isBaseComparator;
/*     */   public boolean m_isLinguisticCompAvail;
/*     */   public int m_caseSensitivityStrenthLevel;
/*  38 */   public static String m_defaultRule = "BINARY";
/*     */   public CommonLocalizationHandler m_commonLocHandlerObj;
/*     */ 
/*     */   public IdcLinguisticComparatorAdapter()
/*     */   {
/*  32 */     this.m_isBaseComparator = false;
/*  33 */     this.m_isLinguisticCompAvail = true;
/*     */   }
/*     */ 
/*     */   public void init()
/*     */   {
/*  46 */     Locale locale = Locale.getDefault();
/*  47 */     initComparator(locale, null, 2, null);
/*     */   }
/*     */ 
/*     */   public void init(ExecutionContext context)
/*     */   {
/*  59 */     init();
/*     */   }
/*     */ 
/*     */   public void init(IdcLocale idcLocale)
/*     */   {
/*  69 */     Locale locale = idcLocale.m_locale;
/*  70 */     initComparator(locale, null, 2, null);
/*     */   }
/*     */ 
/*     */   public void init(String sortRule, int compLevel)
/*     */   {
/*  83 */     String nlsSortRule = sortRule;
/*     */ 
/*  85 */     if ((nlsSortRule == null) || (nlsSortRule.length() == 0))
/*     */     {
/*  87 */       nlsSortRule = m_defaultRule;
/*     */     }
/*     */ 
/*  90 */     if ((nlsSortRule.equalsIgnoreCase("BINARY")) && (compLevel != 0))
/*     */     {
/*  92 */       this.m_isBaseComparator = true;
/*     */     }
/*     */ 
/*  95 */     initComparator(null, nlsSortRule, compLevel, null);
/*     */   }
/*     */ 
/*     */   public void init(String sortRule)
/*     */   {
/* 106 */     if ((sortRule == null) || (sortRule.length() == 0))
/*     */     {
/* 108 */       sortRule = m_defaultRule;
/*     */     }
/*     */ 
/* 111 */     if (sortRule.equalsIgnoreCase("BINARY"))
/*     */     {
/* 113 */       this.m_isBaseComparator = true;
/*     */     }
/*     */ 
/* 116 */     initComparator(null, sortRule, 2, null);
/*     */   }
/*     */ 
/*     */   public void init(IdcLocale idcLocale, String sortRule, int compLevel)
/*     */   {
/* 136 */     if ((sortRule == null) || (sortRule.length() == 0))
/*     */     {
/* 138 */       sortRule = m_defaultRule;
/*     */     }
/*     */ 
/* 141 */     Locale locale = idcLocale.m_locale;
/*     */ 
/* 143 */     String nlsSortRule = sortRule;
/*     */ 
/* 147 */     if ((nlsSortRule.equalsIgnoreCase("BINARY")) && (compLevel != 0))
/*     */     {
/* 149 */       this.m_isBaseComparator = true;
/*     */     }
/*     */ 
/* 152 */     initComparator(locale, nlsSortRule, compLevel, null);
/*     */   }
/*     */ 
/*     */   public void setComparatorLevel(int strength)
/*     */   {
/* 168 */     if (this.m_caseSensitivityStrenthLevel == strength)
/*     */     {
/* 172 */       return;
/*     */     }
/*     */ 
/* 175 */     if (this.m_comparator != null)
/*     */     {
/* 177 */       switch (strength)
/*     */       {
/*     */       case 0:
/* 183 */         if (this.m_isLinguisticCompAvail == true)
/*     */         {
/* 185 */           String sortRule = getSortRule();
/* 186 */           sortRule = stripBaseSortRule(sortRule);
/* 187 */           this.m_isBaseComparator = false;
/* 188 */           initComparator(null, sortRule, strength, null);
/*     */         }
/*     */ 
/* 189 */         break;
/*     */       case 1:
/*     */       case 2:
/*     */         try
/*     */         {
/* 198 */           ClassHelperUtils.executeMethodConvertToStandardExceptions(this.m_comparator, "setStrength", new Object[] { Integer.valueOf(strength) });
/*     */         }
/*     */         catch (ServiceException e)
/*     */         {
/* 202 */           String sortRule = getSortRule();
/* 203 */           sortRule = stripBaseSortRule(sortRule);
/* 204 */           this.m_isBaseComparator = false;
/* 205 */           initComparator(null, sortRule, strength, null);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 212 */     this.m_caseSensitivityStrenthLevel = strength;
/*     */   }
/*     */ 
/*     */   public String stripBaseSortRule(String sortRule)
/*     */   {
/* 217 */     int indexOfUnderScore = sortRule.lastIndexOf("_");
/*     */ 
/* 219 */     if (indexOfUnderScore != -1)
/*     */     {
/* 221 */       String sortOrder = sortRule.substring(indexOfUnderScore);
/*     */ 
/* 223 */       if ((sortOrder.equalsIgnoreCase("_CI")) || (sortOrder.equalsIgnoreCase("_AI")))
/*     */       {
/* 225 */         sortRule = sortRule.substring(0, indexOfUnderScore);
/*     */       }
/*     */     }
/*     */ 
/* 229 */     return sortRule;
/*     */   }
/*     */ 
/*     */   public String addComparatorLevelToRule(String sortRule, int compLevel)
/*     */   {
/* 234 */     switch (compLevel)
/*     */     {
/*     */     case 0:
/* 238 */       sortRule = sortRule + "_AI";
/* 239 */       break;
/*     */     case 1:
/* 243 */       sortRule = sortRule + "_CI";
/*     */     case 2:
/*     */     }
/*     */ 
/* 250 */     return sortRule;
/*     */   }
/*     */ 
/*     */   public String getSortRule()
/*     */   {
/*     */     String sortRule;
/*     */     String sortRule;
/* 257 */     if (this.m_isBaseComparator == true)
/*     */     {
/* 259 */       sortRule = "BINARY";
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 265 */         sortRule = (String)ClassHelperUtils.executeMethodConvertToStandardExceptions(this.m_comparator, "getName", new Object[0]);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 269 */         sortRule = "BINARY";
/*     */       }
/*     */     }
/* 272 */     return sortRule;
/*     */   }
/*     */ 
/*     */   public String getCharSet()
/*     */   {
/*     */     String charSet;
/*     */     String charSet;
/* 278 */     if (this.m_isBaseComparator == true)
/*     */     {
/* 280 */       charSet = "UTF16";
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 286 */         charSet = (String)ClassHelperUtils.executeMethodConvertToStandardExceptions(this.m_comparator, "getCharSet", new Object[0]);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 290 */         charSet = "UTF16";
/*     */       }
/*     */     }
/* 293 */     return charSet;
/*     */   }
/*     */ 
/*     */   public int getComposition()
/*     */   {
/*     */     int composition;
/*     */     int composition;
/* 299 */     if (this.m_isBaseComparator == true)
/*     */     {
/* 301 */       composition = 0;
/*     */     }
/*     */     else
/*     */     {
/*     */       try
/*     */       {
/* 307 */         Object compositionObject = ClassHelperUtils.executeMethodConvertToStandardExceptions(this.m_comparator, "getComposition", new Object[0]);
/* 308 */         composition = Integer.parseInt((String)compositionObject);
/*     */       }
/*     */       catch (ServiceException e)
/*     */       {
/* 312 */         composition = 0;
/*     */       }
/*     */     }
/* 315 */     return composition;
/*     */   }
/*     */ 
/*     */   public void setComposition(int composition)
/*     */   {
/* 326 */     if (this.m_isBaseComparator == true)
/*     */       return;
/*     */     try
/*     */     {
/* 330 */       ClassHelperUtils.executeMethodConvertToStandardExceptions(this.m_comparator, "setComposition", new Object[] { Integer.valueOf(composition) });
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initComparator(Locale locale, String sortRule, int compLevel, ExecutionContext context)
/*     */   {
/* 341 */     this.m_caseSensitivityStrenthLevel = compLevel;
/* 342 */     if ((!sortRule.endsWith("_CI")) && (!sortRule.endsWith("_AI")))
/*     */     {
/* 345 */       sortRule = addComparatorLevelToRule(sortRule, compLevel);
/*     */     }
/*     */ 
/* 348 */     this.m_commonLocHandlerObj = CommonLocalizationHandlerFactory.createInstance(this.m_isBaseComparator);
/*     */     try
/*     */     {
/* 352 */       this.m_comparator = this.m_commonLocHandlerObj.createComparator(locale, sortRule, context);
/*     */     }
/*     */     catch (ServiceException e)
/*     */     {
/* 356 */       this.m_comparator = new ComparatorAdapter();
/* 357 */       this.m_isLinguisticCompAvail = false;
/*     */ 
/* 359 */       if (!m_reportedError)
/*     */       {
/* 361 */         String infoMsg = "Unable to initialize Linguistic Comparator";
/*     */ 
/* 365 */         if ((FileUtils.m_javaSystemEncoding != null) && (LocaleResources.getSystemLocale() != null))
/*     */         {
/* 367 */           Report.info(null, infoMsg, e);
/* 368 */           m_reportedError = true;
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 373 */     if (!this.m_comparator instanceof ComparatorAdapter)
/*     */       return;
/* 375 */     this.m_isBaseComparator = true;
/*     */   }
/*     */ 
/*     */   public int compare(Object obj1, Object obj2)
/*     */   {
/* 381 */     int comparison = 0;
/* 382 */     comparison = this.m_comparator.compare(obj1, obj2);
/* 383 */     return comparison;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 388 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84221 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.common.IdcLinguisticComparatorAdapter
 * JD-Core Version:    0.5.4
 */