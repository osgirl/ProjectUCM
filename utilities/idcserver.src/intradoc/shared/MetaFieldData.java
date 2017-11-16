/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ScriptUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.FieldInfoUtils;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.Service;
/*     */ import intradoc.shared.schema.SchemaHelper;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.math.BigDecimal;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class MetaFieldData extends DataResultSet
/*     */ {
/*     */   public static final String m_tableName = "DocMetaDefinition";
/*     */   public static final String m_queryName = "Qmetadefs";
/*  53 */   public static final String[] m_extraFields = { "dOptionListType", "dIsPlaceholderField", "dCategory", "dExtraDefinition" };
/*     */   public static final short TEXT_INDEX = 0;
/*     */   public static final short BIGTEXT_INDEX = 1;
/*     */   public static final short DATE_INDEX = 2;
/*     */   public static final short MEMO_INDEX = 3;
/*     */   public int m_nameIndex;
/*     */   public int m_typeIndex;
/*     */   public int m_isRequiredIndex;
/*     */   public int m_isEnabledIndex;
/*     */   public int m_isSearchIndex;
/*     */   public int m_defaultIndex;
/*     */   public int m_captionIndex;
/*     */   public int m_isOptionListIndex;
/*     */   public int m_optionsListKeyIndex;
/*     */   public int m_optionListTypeIndex;
/*     */   public int m_isPlaceholderIndex;
/*     */   public int m_decimalScaleIndex;
/*     */   public int m_docMetaSetIndex;
/*     */   public int m_validationErrorCode;
/*     */   protected SchemaHelper m_schHelper;
/*     */ 
/*     */   public MetaFieldData()
/*     */   {
/*  60 */     this.m_nameIndex = -1;
/*  61 */     this.m_typeIndex = -1;
/*  62 */     this.m_isRequiredIndex = -1;
/*  63 */     this.m_isEnabledIndex = -1;
/*  64 */     this.m_isSearchIndex = -1;
/*  65 */     this.m_defaultIndex = -1;
/*  66 */     this.m_captionIndex = -1;
/*  67 */     this.m_isOptionListIndex = -1;
/*  68 */     this.m_optionsListKeyIndex = -1;
/*  69 */     this.m_optionListTypeIndex = -1;
/*  70 */     this.m_isPlaceholderIndex = -1;
/*  71 */     this.m_decimalScaleIndex = -1;
/*  72 */     this.m_docMetaSetIndex = -1;
/*  73 */     this.m_validationErrorCode = -64;
/*     */   }
/*     */ 
/*     */   public void init(ResultSet rset)
/*     */     throws DataException, ServiceException
/*     */   {
/*  84 */     super.copy(rset);
/*  85 */     addExtraFields();
/*  86 */     initIndexes();
/*  87 */     this.m_schHelper = ((SchemaHelper)ComponentClassFactory.createClassInstance("SchemaHelper", "intradoc.shared.schema.SchemaHelper", null));
/*     */   }
/*     */ 
/*     */   public void addExtraFields()
/*     */   {
/*  94 */     Vector newFields = new IdcVector();
/*     */ 
/*  96 */     for (int i = 0; i < m_extraFields.length; ++i)
/*     */     {
/*  98 */       FieldInfo fi = new FieldInfo();
/*  99 */       if (getFieldInfo(m_extraFields[i], fi)) {
/*     */         continue;
/*     */       }
/*     */ 
/* 103 */       fi.m_name = m_extraFields[i];
/* 104 */       newFields.addElement(fi);
/*     */     }
/*     */ 
/* 107 */     mergeFieldsWithFlags(newFields, 0);
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/* 113 */     MetaFieldData rset = new MetaFieldData();
/* 114 */     initShallow(rset);
/* 115 */     rset.m_schHelper = this.m_schHelper;
/*     */ 
/* 117 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/* 123 */     super.initShallow(rset);
/* 124 */     MetaFieldData metaSet = (MetaFieldData)rset;
/*     */ 
/* 126 */     metaSet.m_nameIndex = this.m_nameIndex;
/* 127 */     metaSet.m_typeIndex = this.m_typeIndex;
/* 128 */     metaSet.m_isRequiredIndex = this.m_isRequiredIndex;
/* 129 */     metaSet.m_isEnabledIndex = this.m_isEnabledIndex;
/* 130 */     metaSet.m_isSearchIndex = this.m_isSearchIndex;
/* 131 */     metaSet.m_defaultIndex = this.m_defaultIndex;
/* 132 */     metaSet.m_captionIndex = this.m_captionIndex;
/* 133 */     metaSet.m_isOptionListIndex = this.m_isOptionListIndex;
/* 134 */     metaSet.m_optionsListKeyIndex = this.m_optionsListKeyIndex;
/* 135 */     metaSet.m_optionListTypeIndex = this.m_optionListTypeIndex;
/* 136 */     metaSet.m_isPlaceholderIndex = this.m_isPlaceholderIndex;
/* 137 */     metaSet.m_decimalScaleIndex = this.m_decimalScaleIndex;
/* 138 */     metaSet.m_docMetaSetIndex = this.m_docMetaSetIndex;
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 143 */     return "DocMetaDefinition";
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static String[] getMetaFieldTypes()
/*     */   {
/* 152 */     return MetaFieldUtils.getMetaFieldTypes();
/*     */   }
/*     */ 
/*     */   protected void initIndexes() throws DataException
/*     */   {
/*     */     try
/*     */     {
/* 159 */       this.m_nameIndex = ResultSetUtils.getIndexMustExist(this, "dName");
/* 160 */       this.m_typeIndex = ResultSetUtils.getIndexMustExist(this, "dType");
/* 161 */       this.m_isRequiredIndex = ResultSetUtils.getIndexMustExist(this, "dIsRequired");
/* 162 */       this.m_isEnabledIndex = ResultSetUtils.getIndexMustExist(this, "dIsEnabled");
/* 163 */       this.m_isSearchIndex = ResultSetUtils.getIndexMustExist(this, "dIsSearchable");
/* 164 */       this.m_defaultIndex = ResultSetUtils.getIndexMustExist(this, "dDefaultValue");
/* 165 */       this.m_captionIndex = ResultSetUtils.getIndexMustExist(this, "dCaption");
/* 166 */       this.m_isOptionListIndex = ResultSetUtils.getIndexMustExist(this, "dIsOptionList");
/* 167 */       this.m_optionsListKeyIndex = ResultSetUtils.getIndexMustExist(this, "dOptionListKey");
/* 168 */       this.m_optionListTypeIndex = ResultSetUtils.getIndexMustExist(this, "dOptionListType");
/* 169 */       this.m_isPlaceholderIndex = ResultSetUtils.getIndexMustExist(this, "dIsPlaceholderField");
/* 170 */       this.m_decimalScaleIndex = ResultSetUtils.getIndexMustExist(this, "dDecimalScale");
/* 171 */       this.m_docMetaSetIndex = ResultSetUtils.getIndexMustExist(this, "dDocMetaSet");
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 175 */       throw new DataException(e, "apErrorWithTable", new Object[] { "DocMetaDefinition" });
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getCaption(ExecutionContext cxt)
/*     */   {
/* 181 */     return LocaleResources.getString(getStringValue(this.m_captionIndex), cxt);
/*     */   }
/*     */ 
/*     */   public void validate(DataBinder binder, Workspace workspace, ExecutionContext cxt)
/*     */     throws ServiceException
/*     */   {
/* 188 */     boolean isAllowEmptyRequiredFields = false;
/* 189 */     if (cxt instanceof Service)
/*     */     {
/* 191 */       Service service = (Service)cxt;
/* 192 */       isAllowEmptyRequiredFields = service.isConditionVarTrue("AllowEmptyRequiredCheckinFields");
/*     */     }
/*     */ 
/* 199 */     for (this.m_currentRow = 0; this.m_currentRow < this.m_numRows; this.m_currentRow += 1)
/*     */     {
/* 201 */       String name = getStringValue(this.m_nameIndex);
/* 202 */       String type = getStringValue(this.m_typeIndex);
/*     */ 
/* 209 */       String value = binder.getSearchAllAllowMissing(name);
/* 210 */       if (value != null)
/*     */       {
/* 212 */         value = value.trim();
/*     */       }
/*     */ 
/* 215 */       boolean isEnabled = getIsEnabled();
/* 216 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 218 */         value = "";
/*     */ 
/* 220 */         boolean isRequiredByProfileRule = getIsRequiredByProfileRule(name, binder);
/* 221 */         if ((isEnabled) && (isRequiredByProfileRule))
/*     */         {
/* 223 */           String requiredMsgFromProfileRule = getRequiredMsgFromProfileRule(name, binder);
/* 224 */           if (requiredMsgFromProfileRule != null)
/*     */           {
/* 226 */             throw new ServiceException(null, this.m_validationErrorCode, requiredMsgFromProfileRule, new Object[0]);
/*     */           }
/*     */         }
/*     */ 
/* 230 */         if ((isEnabled) && (((isRequiredByProfileRule) || (getIsRequired()))) && (!isAllowEmptyRequiredFields))
/*     */         {
/* 233 */           throw new ServiceException(null, this.m_validationErrorCode, "apValueRequired", new Object[] { getCaption(cxt) });
/*     */         }
/*     */ 
/* 238 */         if (type.equalsIgnoreCase("int"))
/*     */         {
/* 240 */           value = "0";
/*     */         }
/*     */ 
/* 244 */         binder.putLocal(name, value);
/*     */       }
/*     */       else
/*     */       {
/* 249 */         if (!isEnabled)
/*     */         {
/*     */           continue;
/*     */         }
/*     */ 
/* 254 */         String[] foundValue = new String[1];
/* 255 */         foundValue[0] = value;
/*     */         try
/*     */         {
/* 259 */           if (!hasAllowableValueInOptionLists(name, value, foundValue, binder, workspace, cxt))
/*     */           {
/* 262 */             throw new ServiceException(null, this.m_validationErrorCode, "apValueNotInList", new Object[] { getCaption(cxt) });
/*     */           }
/*     */ 
/*     */         }
/*     */         catch (DataException e)
/*     */         {
/* 268 */           throw new ServiceException(e, "apValueInvalid", new Object[] { getCaption(cxt) });
/*     */         }
/*     */ 
/* 271 */         binder.putLocal(name, foundValue[0]);
/* 272 */         if (type.equalsIgnoreCase("date"))
/*     */         {
/*     */           try
/*     */           {
/* 277 */             LocaleResources.parseDate(value, cxt);
/*     */           }
/*     */           catch (Exception e)
/*     */           {
/* 281 */             throw new ServiceException(e, this.m_validationErrorCode, "apValueNotDate", new Object[] { getCaption(cxt) });
/*     */           }
/*     */         }
/* 284 */         else if (type.equalsIgnoreCase("int"))
/*     */         {
/* 286 */           long max = 9223372036854775807L;
/* 287 */           long min = -9223372036854775808L;
/*     */           try
/*     */           {
/* 290 */             String tmp = SharedObjects.getEnvironmentValue("MaxIntegerAllowed");
/*     */ 
/* 292 */             if (tmp != null)
/*     */             {
/* 294 */               max = Long.parseLong(tmp);
/*     */             }
/*     */           }
/*     */           catch (NumberFormatException e)
/*     */           {
/* 299 */             Report.trace(null, "NumberFormatException while parsing MaxIntegerAllowed.", e);
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 304 */             String tmp = SharedObjects.getEnvironmentValue("MinIntegerAllowed");
/* 305 */             if (tmp != null)
/*     */             {
/* 307 */               min = Long.parseLong(tmp);
/*     */             }
/*     */           }
/*     */           catch (NumberFormatException e)
/*     */           {
/* 312 */             Report.trace(null, "NumberFormatException while parsing MinIntegerAllowed.", e);
/*     */           }
/*     */ 
/*     */           long l;
/*     */           try
/*     */           {
/* 318 */             l = Long.parseLong(value);
/*     */           }
/*     */           catch (NumberFormatException e)
/*     */           {
/* 324 */             throw new ServiceException(null, this.m_validationErrorCode, "apValueNotIntegerForField", new Object[] { value, getCaption(cxt) });
/*     */           }
/*     */ 
/* 328 */           if (l > max)
/*     */           {
/* 330 */             throw new ServiceException(null, this.m_validationErrorCode, "apValueTooBig", new Object[] { value, getCaption(cxt) });
/*     */           }
/*     */ 
/* 333 */           if (l < min)
/*     */           {
/* 335 */             throw new ServiceException(null, this.m_validationErrorCode, "apValueTooSmall", new Object[] { value, getCaption(cxt), "" + min });
/*     */           }
/*     */ 
/*     */         }
/*     */         else
/*     */         {
/* 341 */           int maxlen = 0;
/* 342 */           String placeholderStr = getStringValue(this.m_isPlaceholderIndex);
/* 343 */           boolean isPlaceholder = StringUtils.convertToBool(placeholderStr, false);
/* 344 */           if (isPlaceholder)
/*     */           {
/* 347 */             String str = binder.getLocal(name + ":maxLength");
/* 348 */             maxlen = NumberUtils.parseInteger(str, 0);
/*     */           }
/*     */           else
/*     */           {
/* 352 */             maxlen = 30;
/* 353 */             if (type.equalsIgnoreCase("memo"))
/*     */             {
/* 355 */               maxlen = SharedObjects.getEnvironmentInt("MemoFieldSize", 2000);
/*     */             }
/* 357 */             else if (type.equalsIgnoreCase("bigtext"))
/*     */             {
/* 359 */               maxlen = 200;
/*     */             }
/*     */           }
/*     */ 
/* 363 */           maxlen = SharedObjects.getEnvironmentInt(name + ":maxLength", maxlen);
/* 364 */           if (maxlen <= 0) {
/*     */             continue;
/*     */           }
/*     */ 
/* 368 */           int len = 0;
/* 369 */           if (type.equalsIgnoreCase("decimal"))
/*     */           {
/*     */             try
/*     */             {
/* 373 */               BigDecimal decimalValue = new BigDecimal(value);
/* 374 */               len = decimalValue.precision();
/*     */             }
/*     */             catch (Exception e)
/*     */             {
/* 378 */               throw new ServiceException(e, this.m_validationErrorCode, "apValueNotDecimalForField", new Object[] { value, getCaption(cxt) });
/*     */             }
/*     */ 
/*     */           }
/*     */           else
/*     */           {
/* 384 */             len = value.length();
/*     */           }
/*     */ 
/* 387 */           if (len <= maxlen)
/*     */             continue;
/* 389 */           throw new ServiceException(null, this.m_validationErrorCode, "apValueTooLong", new Object[] { getCaption(cxt), "" + maxlen });
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean hasAllowableValueInOptionLists(String name, String value, String[] replacementValue, DataBinder binder, Workspace workspace, ExecutionContext cxt)
/*     */     throws DataException, ServiceException
/*     */   {
/* 401 */     boolean isOptList = StringUtils.convertToBool(getStringValue(this.m_isOptionListIndex), false);
/*     */ 
/* 403 */     String optionListKey = getStringValue(this.m_optionsListKeyIndex).trim();
/* 404 */     String optionListType = getStringValue(this.m_optionListTypeIndex).trim();
/* 405 */     boolean optListMandatory = (optionListType.indexOf("multi") < 0) && (optionListType.indexOf("combo") < 0) && (optionListType.indexOf("unval") < 0);
/*     */ 
/* 407 */     int retVal = PluginFilters.filter(name + ":validate", workspace, binder, cxt);
/* 408 */     if (retVal != 0)
/*     */     {
/* 410 */       if ((retVal == 1) && 
/* 412 */         (cxt != null))
/*     */       {
/* 414 */         return ScriptUtils.getBooleanVal(cxt.getReturnValue());
/*     */       }
/*     */ 
/* 417 */       return true;
/*     */     }
/*     */ 
/* 420 */     if ((((replacementValue != null) ? 1 : 0) & ((replacementValue.length > 0) ? 1 : 0)) != 0)
/*     */     {
/* 422 */       replacementValue[0] = value;
/*     */     }
/* 424 */     if ((isOptList == true) && (optionListKey != null) && (optionListKey.length() > 0))
/*     */     {
/* 427 */       boolean found = isValueInOptionListEx(optionListKey, value, replacementValue);
/* 428 */       if ((!found) && (optListMandatory == true))
/*     */       {
/* 430 */         return false;
/*     */       }
/*     */     }
/* 433 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean getIsRequiredByProfileRule(String name, DataBinder binder)
/*     */   {
/* 438 */     String isRequired = binder.getLocal(name + ":isRequired");
/* 439 */     return (isRequired != null) && (isRequired.equals("1"));
/*     */   }
/*     */ 
/*     */   public String getRequiredMsgFromProfileRule(String name, DataBinder binder)
/*     */   {
/* 444 */     return binder.getLocal(name + ":requiredMsg");
/*     */   }
/*     */ 
/*     */   public boolean getIsRequired()
/*     */   {
/* 449 */     return StringUtils.convertToBool(getStringValue(this.m_isRequiredIndex), false);
/*     */   }
/*     */ 
/*     */   public boolean getIsEnabled()
/*     */   {
/* 454 */     return StringUtils.convertToBool(getStringValue(this.m_isEnabledIndex), false);
/*     */   }
/*     */ 
/*     */   public boolean getIsSearchable()
/*     */   {
/* 459 */     return StringUtils.convertToBool(getStringValue(this.m_isSearchIndex), false);
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 464 */     return getStringValue(this.m_nameIndex);
/*     */   }
/*     */ 
/*     */   public String getDefaultValue()
/*     */   {
/* 469 */     return getStringValue(this.m_defaultIndex);
/*     */   }
/*     */ 
/*     */   public String getDocMetaSet()
/*     */   {
/* 474 */     return getStringValue(this.m_docMetaSetIndex);
/*     */   }
/*     */ 
/*     */   public boolean isValueInOptionList(String optionListKey, String value)
/*     */     throws ServiceException
/*     */   {
/* 480 */     return isValueInOptionListEx(optionListKey, value, null);
/*     */   }
/*     */ 
/*     */   public boolean isValueInOptionListEx(String optionListKey, String value, String[] foundVal)
/*     */     throws ServiceException
/*     */   {
/* 487 */     if (this.m_schHelper.isComplexListType(optionListKey))
/*     */     {
/* 496 */       Report.trace("schema", "skipping validation for a view or tree", null);
/* 497 */       if (foundVal != null)
/*     */       {
/* 499 */         foundVal[0] = value;
/*     */       }
/* 501 */       return true;
/*     */     }
/* 503 */     Vector opts = SharedObjects.getOptList(optionListKey);
/* 504 */     if (opts == null)
/*     */     {
/* 506 */       throw new ServiceException(null, "apNoOptionListDefined", new Object[] { optionListKey });
/*     */     }
/*     */ 
/* 509 */     int num = opts.size();
/* 510 */     boolean found = false;
/* 511 */     for (int i = 0; i < num; ++i)
/*     */     {
/* 513 */       String optValue = (String)opts.elementAt(i);
/* 514 */       if (!value.equalsIgnoreCase(optValue.trim()))
/*     */         continue;
/* 516 */       if ((foundVal != null) && (foundVal.length > 0))
/*     */       {
/* 518 */         foundVal[0] = optValue;
/*     */       }
/* 520 */       found = true;
/*     */     }
/*     */ 
/* 524 */     return found;
/*     */   }
/*     */ 
/*     */   public boolean createDiffList(ResultSet metaResultSet, Vector add, Vector change, Vector delete)
/*     */   {
/* 530 */     return createDiffListEx(metaResultSet, add, change, delete, null);
/*     */   }
/*     */ 
/*     */   public boolean createDiffListEx(ResultSet metaResultSet, Vector add, Vector change, Vector delete, DataBinder binder)
/*     */   {
/* 536 */     return createDiffListMultiEx("DocMeta", metaResultSet, add, change, delete, binder);
/*     */   }
/*     */ 
/*     */   public boolean createDiffListMultiEx(String table, ResultSet metaResultSet, Vector add, Vector change, Vector delete, DataBinder binder)
/*     */   {
/* 546 */     int numMetaFields = metaResultSet.getNumFields();
/* 547 */     boolean changesNeeded = false;
/*     */ 
/* 549 */     FieldInfo finfo = new FieldInfo();
/* 550 */     boolean[] found = new boolean[numMetaFields];
/* 551 */     for (int i = 0; i < numMetaFields; ++i)
/*     */     {
/* 553 */       found[i] = false;
/*     */     }
/*     */ 
/* 556 */     boolean isBounceOnChange = false;
/* 557 */     if (binder != null)
/*     */     {
/* 559 */       isBounceOnChange = StringUtils.convertToBool(binder.getLocal("isBounceOnChange"), false);
/*     */     }
/*     */ 
/* 562 */     boolean isBounced = false;
/* 563 */     Vector bounced = new IdcVector();
/*     */ 
/* 566 */     for (i = 0; i < this.m_numRows; ++i)
/*     */     {
/* 568 */       Vector v = getRowValues(i);
/* 569 */       String docMetaSet = (String)v.elementAt(this.m_docMetaSetIndex);
/*     */ 
/* 571 */       if (!docMetaSet.equalsIgnoreCase(table)) { if ((docMetaSet != null) && (docMetaSet.length() != 0)) continue; if (!table.equalsIgnoreCase("DocMeta")) {
/*     */           continue;
/*     */         } }
/*     */ 
/* 575 */       String name = (String)v.elementAt(this.m_nameIndex);
/* 576 */       String type = (String)v.elementAt(this.m_typeIndex);
/* 577 */       String decimalScale = (String)v.elementAt(this.m_decimalScaleIndex);
/* 578 */       boolean isPlaceholder = StringUtils.convertToBool((String)v.elementAt(this.m_isPlaceholderIndex), false);
/*     */ 
/* 580 */       if (metaResultSet.getFieldInfo(name, finfo))
/*     */       {
/* 582 */         String metaType = getMetaType(finfo);
/*     */ 
/* 585 */         if ((!type.equalsIgnoreCase(metaType)) && (!isPlaceholder))
/*     */         {
/* 587 */           if ((isBounceOnChange) && ((
/* 590 */             (type.equalsIgnoreCase("Text")) || ((type.equalsIgnoreCase("BigText")) && (metaType.equalsIgnoreCase("Memo"))))))
/*     */           {
/* 593 */             isBounced = true;
/*     */ 
/* 595 */             StringBuffer buff = new StringBuffer(name);
/* 596 */             buff.append(',');
/* 597 */             buff.append(type);
/* 598 */             buff.append(',');
/* 599 */             buff.append(metaType);
/* 600 */             bounced.add(buff.toString());
/*     */           }
/*     */ 
/* 604 */           FieldInfo fi = MetaFieldUtils.createFieldInfo(name, type, decimalScale);
/* 605 */           change.addElement(fi);
/* 606 */           changesNeeded = true;
/*     */         }
/*     */ 
/* 611 */         found[finfo.m_index] = true;
/*     */       } else {
/* 613 */         if (isPlaceholder)
/*     */           continue;
/* 615 */         FieldInfo fi = MetaFieldUtils.createFieldInfo(name, type, decimalScale);
/*     */ 
/* 617 */         if (type.equalsIgnoreCase("Memo"))
/*     */         {
/* 619 */           boolean setMemoToMaxSize = SharedObjects.getEnvValueAsBoolean(name + ":isSetMemoToMax", false);
/* 620 */           if (setMemoToMaxSize)
/*     */           {
/* 622 */             FieldInfoUtils.setFieldOption(fi, "setMemoToMaxSize", "1");
/*     */           }
/*     */         }
/*     */ 
/* 626 */         add.addElement(fi);
/* 627 */         changesNeeded = true;
/*     */       }
/*     */     }
/*     */ 
/* 631 */     if (isBounced)
/*     */     {
/* 633 */       String existingBounced = binder.getLocal("BouncedFields");
/* 634 */       String str = StringUtils.createString(bounced, ':', '*');
/* 635 */       if (existingBounced != null)
/*     */       {
/* 637 */         str = existingBounced + ":" + str;
/*     */       }
/* 639 */       binder.putLocal("isBounced", "1");
/* 640 */       binder.putLocal("BouncedFields", str);
/*     */     }
/* 642 */     else if (binder != null)
/*     */     {
/* 644 */       binder.putLocal("isBounced", "0");
/*     */     }
/*     */ 
/* 648 */     for (i = 0; i < numMetaFields; ++i)
/*     */     {
/* 650 */       if (found[i] != 0)
/*     */         continue;
/* 652 */       FieldInfo delInfo = new FieldInfo();
/* 653 */       metaResultSet.getIndexFieldInfo(i, delInfo);
/* 654 */       if (delInfo.m_name.equals("dID")) {
/*     */         continue;
/*     */       }
/* 657 */       delete.addElement(delInfo);
/* 658 */       changesNeeded = true;
/*     */     }
/*     */ 
/* 663 */     return changesNeeded;
/*     */   }
/*     */ 
/*     */   public static String getMetaType(FieldInfo finfo)
/*     */   {
/* 668 */     String metaType = "Illegal";
/* 669 */     if (finfo.m_type == 3)
/*     */     {
/* 671 */       metaType = "Int";
/*     */     }
/* 673 */     else if (finfo.m_type == 5)
/*     */     {
/* 675 */       metaType = "Date";
/*     */     }
/* 677 */     else if (finfo.m_type == 6)
/*     */     {
/* 679 */       int maxlen = SharedObjects.getEnvironmentInt("MinMemoFieldSize", 255);
/* 680 */       if ((finfo.m_isFixedLen) && (finfo.m_maxLen < maxlen))
/*     */       {
/* 682 */         if (finfo.m_maxLen >= 50)
/*     */         {
/* 684 */           metaType = "BigText";
/*     */         }
/*     */         else
/*     */         {
/* 688 */           metaType = "Text";
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/* 693 */         metaType = "Memo";
/*     */       }
/*     */     }
/* 696 */     else if (finfo.m_type == 11)
/*     */     {
/* 698 */       metaType = "Decimal";
/*     */     }
/*     */ 
/* 701 */     return metaType;
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static FieldInfo createFieldInfo(String name, String type)
/*     */   {
/* 710 */     return MetaFieldUtils.createFieldInfo(name, type);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean isTextField(String type)
/*     */   {
/* 718 */     return MetaFieldUtils.isTextField(type);
/*     */   }
/*     */ 
/*     */   @Deprecated
/*     */   public static boolean allowOptionList(String type)
/*     */   {
/* 726 */     return MetaFieldUtils.allowOptionList(type);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 731 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98823 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.MetaFieldData
 * JD-Core Version:    0.5.4
 */