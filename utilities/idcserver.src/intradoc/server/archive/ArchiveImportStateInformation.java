/*     */ package intradoc.server.archive;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.HashVector;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ParseStringException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.TimeZoneFormat;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.server.ValueMapData;
/*     */ import intradoc.shared.AdditionalRenditions;
/*     */ import intradoc.shared.ClausesData;
/*     */ import intradoc.shared.MetaFieldData;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.UserUtils;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.text.ParseException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashSet;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ArchiveImportStateInformation
/*     */ {
/*  42 */   public Workspace m_workspace = null;
/*  43 */   public ExecutionContext m_cxt = null;
/*  44 */   public DataBinder m_binder = null;
/*     */ 
/*  48 */   public DataBinder m_propsWrapper = null;
/*     */ 
/*  51 */   public boolean m_isNativeImport = false;
/*  52 */   public boolean m_useRevLabel = false;
/*  53 */   public boolean m_isDeleteImport = false;
/*  54 */   public boolean m_isImportValidOnly = false;
/*     */ 
/*  57 */   public boolean m_isTranslateDate = false;
/*  58 */   public IdcDateFormat m_dateFormat = null;
/*  59 */   protected TimeZone m_timeZone = null;
/*     */ 
/*  62 */   public int m_importCount = 0;
/*  63 */   public int m_numBatchFiles = 0;
/*  64 */   public int m_numProcessedBatchFiles = 0;
/*     */ 
/*  67 */   public Hashtable m_fieldMaps = new Hashtable();
/*  68 */   public Hashtable m_valueMaps = new Hashtable();
/*  69 */   public Hashtable m_usedValueMaps = null;
/*  70 */   public Hashtable m_invalidOptions = null;
/*  71 */   public Set m_testedFields = new HashSet();
/*     */ 
/*  74 */   public String[][] m_commonDocFields = (String[][])null;
/*     */ 
/*  77 */   public String[] m_allFileFields = null;
/*     */ 
/*  80 */   public int m_additionalRenditionsOffset = -1;
/*     */ 
/*  83 */   public DataResultSet m_lastFileSet = null;
/*     */ 
/*  86 */   protected final String[] EXCLUDED_FIELDS = { "dIsCheckedOut", "dCheckoutUser", "dStatus", "dReleaseState", "dFlag1", "dProcessingState", "dMessage", "dIndexerState", "dPublishState" };
/*     */ 
/*  92 */   public static final String[] CORE_FILE_FIELDS = { "primaryFile", "alternateFile", "webViewableFile" };
/*     */ 
/*  97 */   protected final String[] NATIVE_FIELDS = { "dID", "dRevClassID", "dRevisionID" };
/*     */ 
/* 102 */   protected final String[] REQUIRED_DELETE_FIELDS = { "dID", "dRevClassID", "dDocName" };
/*     */ 
/* 107 */   protected final String[] REQUIRED_OPTION_FIELDS = { "dDocAuthor", "dSecurityGroup", "dDocType" };
/*     */ 
/* 112 */   protected final String[] REQUIRED_OPTIONLISTS = { "docAuthors", "securityGroups", "docTypes" };
/*     */ 
/*     */   public ArchiveImportStateInformation()
/*     */   {
/* 124 */     this.m_propsWrapper = new DataBinder(SharedObjects.getSecureEnvironment());
/*     */ 
/* 126 */     int numRenditions = AdditionalRenditions.m_maxNum;
/* 127 */     this.m_allFileFields = createAllFieldsArray(numRenditions);
/* 128 */     System.arraycopy(CORE_FILE_FIELDS, 0, this.m_allFileFields, 0, CORE_FILE_FIELDS.length);
/* 129 */     prepareAdditionalRenditions(this.m_allFileFields, numRenditions);
/*     */   }
/*     */ 
/*     */   public void init(DataBinder binder, Workspace ws, ExecutionContext cxt)
/*     */   {
/* 134 */     this.m_binder = binder;
/* 135 */     this.m_workspace = ws;
/* 136 */     this.m_cxt = cxt;
/*     */ 
/* 140 */     this.m_propsWrapper = new DataBinder(SharedObjects.getSecureEnvironment());
/*     */ 
/* 142 */     int numRenditions = AdditionalRenditions.m_maxNum;
/* 143 */     this.m_allFileFields = createAllFieldsArray(numRenditions);
/* 144 */     System.arraycopy(CORE_FILE_FIELDS, 0, this.m_allFileFields, 0, CORE_FILE_FIELDS.length);
/* 145 */     prepareAdditionalRenditions(this.m_allFileFields, numRenditions);
/*     */   }
/*     */ 
/*     */   public String[] createAllFieldsArray(int numRenditions)
/*     */   {
/* 150 */     this.m_additionalRenditionsOffset = CORE_FILE_FIELDS.length;
/* 151 */     return new String[CORE_FILE_FIELDS.length + numRenditions];
/*     */   }
/*     */ 
/*     */   public void prepareAdditionalRenditions(String[] allFileFields, int numRenditions)
/*     */   {
/* 156 */     int index = this.m_additionalRenditionsOffset;
/* 157 */     for (int i = 0; i < numRenditions; ++i)
/*     */     {
/* 159 */       allFileFields[(index++)] = ("dRendition" + (i + 1) + ":path");
/*     */     }
/*     */   }
/*     */ 
/*     */   public void buildMaps(DataBinder archiveData)
/*     */   {
/* 169 */     this.m_fieldMaps.clear();
/* 170 */     this.m_testedFields.clear();
/*     */ 
/* 173 */     String fieldMapStr = archiveData.getLocal("aFieldMaps");
/* 174 */     ClausesData fieldMapData = new ClausesData();
/* 175 */     fieldMapData.parse(fieldMapStr);
/*     */ 
/* 177 */     Vector fields = fieldMapData.m_clauses;
/* 178 */     int size = fields.size();
/* 179 */     String[] mappedFields = new String[size];
/* 180 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 182 */       Vector elts = (Vector)fields.elementAt(i);
/* 183 */       String field = (String)elts.elementAt(0);
/* 184 */       String mField = (String)elts.elementAt(1);
/*     */ 
/* 187 */       String fieldNameKey = field.toLowerCase();
/*     */ 
/* 190 */       mField = getColumnName(mField);
/*     */ 
/* 192 */       ArchiveImportFieldData data = new ArchiveImportFieldData(mField);
/*     */ 
/* 194 */       Vector v = (Vector)this.m_fieldMaps.get(fieldNameKey);
/* 195 */       if (v == null)
/*     */       {
/* 197 */         v = new IdcVector();
/*     */       }
/* 199 */       v.addElement(data);
/* 200 */       this.m_fieldMaps.put(fieldNameKey, v);
/* 201 */       mappedFields[i] = mField;
/*     */     }
/*     */ 
/* 204 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 206 */       Vector v = (Vector)this.m_fieldMaps.get(mappedFields[i]);
/* 207 */       if (v == null)
/*     */         continue;
/* 209 */       int num = v.size();
/* 210 */       for (int j = 0; j < num; ++j)
/*     */       {
/* 212 */         ArchiveImportFieldData data = (ArchiveImportFieldData)v.elementAt(j);
/* 213 */         data.m_isRetained = false;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 219 */     String valueMapStr = archiveData.getLocal("aValueMaps");
/* 220 */     ClausesData valueMapData = new ClausesData();
/* 221 */     valueMapData.parse(valueMapStr);
/*     */ 
/* 223 */     Vector values = valueMapData.m_clauses;
/* 224 */     size = values.size();
/* 225 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 227 */       Vector elts = (Vector)values.elementAt(i);
/* 228 */       boolean isAll = StringUtils.convertToBool((String)elts.elementAt(0), false);
/* 229 */       String input = (String)elts.elementAt(1);
/* 230 */       String field = (String)elts.elementAt(2);
/* 231 */       String output = (String)elts.elementAt(3);
/*     */ 
/* 233 */       ValueMapData data = (ValueMapData)this.m_valueMaps.get(field);
/* 234 */       if (data == null)
/*     */       {
/* 237 */         data = new ValueMapData();
/* 238 */         data.setDynamicHtmlMerger(new PageMerger(this.m_propsWrapper, this.m_cxt));
/* 239 */         this.m_valueMaps.put(field, data);
/*     */       }
/* 241 */       data.addMap(field, isAll, input, output);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected boolean isExcludedField(String fieldName)
/*     */   {
/* 247 */     if (this.m_isDeleteImport)
/*     */     {
/* 252 */       return (checkField(this.REQUIRED_DELETE_FIELDS, fieldName) < 0) && (((this.m_useRevLabel != true) || (!fieldName.equals("dRevLabel"))));
/*     */     }
/*     */ 
/* 256 */     if ((checkField(this.EXCLUDED_FIELDS, fieldName) >= 0) || ((!this.m_isNativeImport) && (checkField(this.NATIVE_FIELDS, fieldName) >= 0)) || ((!this.m_useRevLabel) && (fieldName.equals("dRevLabel"))))
/*     */     {
/* 263 */       return (((!fieldName.equalsIgnoreCase("dRevClassID")) || (!StringUtils.convertToBool(this.m_binder.getLocal("UseRevClassFromImport"), false)))) && (((!fieldName.equalsIgnoreCase("dID")) || (!StringUtils.convertToBool(this.m_binder.getLocal("UseDIDFromImport"), false))));
/*     */     }
/*     */ 
/* 267 */     return false;
/*     */   }
/*     */ 
/*     */   protected boolean isFileField(String fieldName)
/*     */   {
/* 273 */     if (fieldName.equals("dOriginalName"))
/*     */     {
/* 275 */       return false;
/*     */     }
/*     */ 
/* 279 */     return checkField(this.m_allFileFields, fieldName) >= 0;
/*     */   }
/*     */ 
/*     */   protected int checkField(String[] fields, String fieldName)
/*     */   {
/* 286 */     int size = fields.length;
/* 287 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 289 */       if (fieldName.equalsIgnoreCase(fields[i]))
/*     */       {
/* 291 */         return i;
/*     */       }
/*     */     }
/* 294 */     return -1;
/*     */   }
/*     */ 
/*     */   protected int checkField(String[][] fields, String fieldName)
/*     */   {
/* 299 */     int size = fields.length;
/* 300 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 302 */       if (fieldName.equalsIgnoreCase(fields[i][0]))
/*     */       {
/* 304 */         return i;
/*     */       }
/*     */     }
/* 307 */     return -1;
/*     */   }
/*     */ 
/*     */   protected String getColumnName(String fieldName)
/*     */   {
/* 312 */     String[][] fields = getCommonDocFields();
/* 313 */     int index = checkField(fields, fieldName);
/* 314 */     if (index >= 0)
/*     */     {
/* 316 */       return fields[index][0];
/*     */     }
/*     */ 
/* 320 */     MetaFieldData metaData = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/* 321 */     Vector rowValues = metaData.findRow(metaData.m_nameIndex, fieldName);
/* 322 */     if (rowValues != null)
/*     */     {
/* 324 */       fieldName = (String)rowValues.elementAt(metaData.m_nameIndex);
/*     */     }
/*     */ 
/* 327 */     return fieldName;
/*     */   }
/*     */ 
/*     */   public String[][] getCommonDocFields()
/*     */   {
/* 332 */     if (this.m_commonDocFields == null)
/*     */     {
/* 334 */       ViewFields docFields = new ViewFields(this.m_cxt);
/* 335 */       this.m_commonDocFields = docFields.createArchiverCommonFieldsList();
/*     */     }
/* 337 */     return this.m_commonDocFields;
/*     */   }
/*     */ 
/*     */   protected String validateOptionValue(String field, String value)
/*     */   {
/* 346 */     MetaFieldData metaFields = (MetaFieldData)SharedObjects.getTable("DocMetaDefinition");
/*     */ 
/* 348 */     int index = checkField(this.REQUIRED_OPTION_FIELDS, field);
/* 349 */     String optKey = null;
/* 350 */     boolean isRequired = false;
/* 351 */     String importValue = value;
/*     */ 
/* 353 */     if (index >= 0)
/*     */     {
/* 355 */       isRequired = true;
/* 356 */       if ((!field.equals("dDocAuthor")) || (!UserUtils.hasExternalUsers()))
/*     */       {
/* 362 */         optKey = this.REQUIRED_OPTIONLISTS[index];
/*     */       }
/* 364 */       if (optKey != null)
/*     */       {
/* 366 */         importValue = getOptionValue(optKey, value);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 372 */       Vector info = metaFields.findRow(metaFields.m_nameIndex, field);
/* 373 */       if (info != null)
/*     */       {
/* 375 */         boolean isEnabled = metaFields.getIsEnabled();
/* 376 */         if ((importValue != null) && (importValue.length() == 0))
/*     */         {
/* 378 */           importValue = "";
/*     */         }
/* 380 */         else if (isEnabled)
/*     */         {
/* 382 */           String[] outValue = new String[1];
/* 383 */           outValue[0] = value;
/* 384 */           if (this.m_isImportValidOnly)
/*     */           {
/* 386 */             importValue = null;
/*     */           }
/*     */ 
/*     */           try
/*     */           {
/* 391 */             if (metaFields.hasAllowableValueInOptionLists(field, value, outValue, this.m_propsWrapper, this.m_workspace, this.m_cxt))
/*     */             {
/* 394 */               importValue = outValue[0];
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/* 399 */             Report.trace(null, null, ignore);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 404 */         isRequired = (metaFields.getIsRequired()) && (isEnabled);
/*     */       }
/*     */     }
/*     */ 
/* 408 */     boolean isNotValid = false;
/* 409 */     if (importValue == null)
/*     */     {
/* 411 */       if (this.m_isImportValidOnly)
/*     */       {
/* 413 */         isNotValid = true;
/*     */       }
/*     */     }
/* 416 */     else if ((isRequired) && (importValue.length() == 0))
/*     */     {
/* 418 */       isNotValid = true;
/*     */     }
/* 420 */     if (isNotValid)
/*     */     {
/* 422 */       markNotValid(field, value);
/* 423 */       return value;
/*     */     }
/*     */ 
/* 426 */     return (importValue == null) ? value : importValue;
/*     */   }
/*     */ 
/*     */   protected String getOptionValue(String optKey, String value)
/*     */   {
/* 431 */     Vector optionList = SharedObjects.getOptList(optKey);
/* 432 */     if (optionList == null)
/*     */     {
/* 434 */       return value;
/*     */     }
/*     */ 
/* 437 */     int size = optionList.size();
/* 438 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 440 */       String opt = (String)optionList.elementAt(i);
/* 441 */       if (value.equalsIgnoreCase(opt))
/*     */       {
/* 443 */         return opt;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 448 */     return null;
/*     */   }
/*     */ 
/*     */   public void markNotValid(String fieldName, String value)
/*     */   {
/* 454 */     if (this.m_invalidOptions == null)
/*     */     {
/* 456 */       this.m_invalidOptions = new Hashtable();
/*     */     }
/*     */ 
/* 459 */     HashVector values = (HashVector)this.m_invalidOptions.get(fieldName);
/* 460 */     if (values == null)
/*     */     {
/* 462 */       values = new HashVector();
/* 463 */       this.m_invalidOptions.put(fieldName, values);
/*     */     }
/* 465 */     values.addValue(value);
/*     */   }
/*     */ 
/*     */   public void preprocessDoc(DataResultSet fileSet, Vector docRows, String archiveDir)
/*     */     throws ServiceException
/*     */   {
/* 477 */     this.m_lastFileSet = fileSet;
/*     */ 
/* 479 */     Properties props = new Properties(this.m_binder.getLocalData());
/* 480 */     this.m_propsWrapper.addResultSet("FileSet", fileSet);
/*     */ 
/* 484 */     this.m_usedValueMaps = ((Hashtable)this.m_valueMaps.clone());
/*     */ 
/* 486 */     int numFields = fileSet.getNumFields();
/* 487 */     for (int i = 0; i < numFields; ++i)
/*     */     {
/* 489 */       FieldInfo fieldInfo = new FieldInfo();
/* 490 */       fileSet.getIndexFieldInfo(i, fieldInfo);
/* 491 */       String fieldName = fieldInfo.m_name;
/*     */ 
/* 493 */       if (isFileField(fieldName))
/*     */       {
/* 495 */         String fileValue = fileSet.getStringValue(fieldInfo.m_index);
/* 496 */         if (fileValue.length() > 0)
/*     */         {
/* 498 */           fileValue = FileUtils.getAbsolutePath(archiveDir, fileValue);
/*     */         }
/* 500 */         props.put(fieldName, fileValue);
/*     */       }
/*     */ 
/* 503 */       String value = fileSet.getStringValue(fieldInfo.m_index);
/* 504 */       if ((this.m_isTranslateDate) && (fieldInfo.m_type == 5) && (value.length() > 0) && 
/* 506 */         (this.m_dateFormat != null)) {
/*     */         try
/*     */         {
/*     */           Date dte;
/*     */           Date dte;
/* 511 */           if (value.startsWith("{ts '"))
/*     */           {
/* 513 */             dte = LocaleResources.m_odbcFormat.parseDateWithTimeZone(value, this.m_timeZone, null, 0);
/*     */           }
/*     */           else
/*     */           {
/* 517 */             dte = this.m_dateFormat.parseDateWithTimeZone(value, this.m_timeZone, null, 256);
/*     */           }
/*     */ 
/* 520 */           value = LocaleUtils.formatODBC(dte);
/*     */         }
/*     */         catch (ParseException e)
/*     */         {
/* 524 */           throw new ServiceException(e);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 529 */       addFieldValuePair(props, fieldName, value);
/*     */     }
/*     */ 
/* 534 */     Enumeration fields = this.m_usedValueMaps.keys();
/* 535 */     while (fields.hasMoreElements())
/*     */     {
/* 537 */       String field = (String)fields.nextElement();
/* 538 */       if (!isExcludedField(field))
/*     */       {
/* 540 */         String value = this.m_propsWrapper.getAllowMissing(field);
/* 541 */         if (value == null)
/*     */         {
/* 543 */           value = "";
/*     */         }
/* 545 */         mapValue(field, value, props);
/*     */       }
/*     */       else
/*     */       {
/* 549 */         Report.trace("archiver", "Field " + field + " cannot be used as a value map target. It is a reserved system field.", null);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 555 */     docRows.addElement(props);
/*     */ 
/* 557 */     this.m_propsWrapper.removeResultSet("FileSet");
/*     */   }
/*     */ 
/*     */   public void parseAndSetTranslateDate(boolean isTranslate, String dateFormat) throws DataException
/*     */   {
/* 562 */     this.m_isTranslateDate = isTranslate;
/*     */     try
/*     */     {
/* 565 */       this.m_dateFormat = new IdcDateFormat();
/* 566 */       this.m_dateFormat.init(dateFormat);
/*     */     }
/*     */     catch (ParseStringException ps)
/*     */     {
/* 570 */       throw new DataException(null, ps);
/*     */     }
/* 572 */     if (!this.m_isTranslateDate)
/*     */       return;
/* 574 */     this.m_timeZone = parseTimeZone(dateFormat);
/*     */   }
/*     */ 
/*     */   protected TimeZone parseTimeZone(String pattern)
/*     */     throws DataException
/*     */   {
/* 580 */     TimeZone tz = null;
/* 581 */     if (pattern != null)
/*     */     {
/* 583 */       int index = pattern.lastIndexOf("!");
/* 584 */       if (index >= 0)
/*     */       {
/* 586 */         String text = pattern.substring(index);
/* 587 */         pattern = pattern.substring(0, index);
/* 588 */         char c = text.charAt(1);
/* 589 */         text = text.substring(2);
/* 590 */         if (c == 't')
/*     */         {
/* 592 */           tz = LocaleResources.m_systemTimeZoneFormat.parseTimeZone(null, text, 0);
/* 593 */           if (tz == null)
/*     */           {
/* 595 */             String msg = LocaleUtils.encodeMessage("syUnableToParseTimeZone2", null, pattern);
/* 596 */             throw new DataException(msg);
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 602 */     if (tz == null)
/*     */     {
/* 604 */       tz = LocaleResources.getSystemTimeZone();
/*     */     }
/* 606 */     return tz;
/*     */   }
/*     */ 
/*     */   protected void addFieldValuePair(Properties props, String fieldName, String value)
/*     */     throws ServiceException
/*     */   {
/* 612 */     if (fieldName.equals("dID"))
/*     */     {
/* 616 */       props.put("archiveID", value);
/*     */     }
/*     */ 
/* 619 */     String fieldNameKey = fieldName.toLowerCase();
/* 620 */     Vector fields = (Vector)this.m_fieldMaps.get(fieldNameKey);
/* 621 */     if (fields != null)
/*     */     {
/* 623 */       int num = fields.size();
/* 624 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 626 */         ArchiveImportFieldData fieldData = (ArchiveImportFieldData)fields.elementAt(i);
/* 627 */         String toField = fieldData.m_mappedField;
/* 628 */         boolean isRetained = fieldData.m_isRetained;
/* 629 */         mapValueEx(fieldName, toField, value, props, isRetained);
/*     */       }
/*     */     } else {
/* 632 */       if (props.get(fieldName) != null) {
/*     */         return;
/*     */       }
/* 635 */       if (this.m_testedFields.contains(fieldName))
/*     */       {
/* 637 */         mapValueEx(fieldName, fieldName, value, props, false);
/*     */       }
/*     */       else
/*     */       {
/* 645 */         String toField = getColumnName(fieldName);
/* 646 */         this.m_testedFields.add(fieldName);
/* 647 */         if (!fieldName.equals(toField))
/*     */         {
/* 650 */           ArchiveImportFieldData data = new ArchiveImportFieldData(toField);
/* 651 */           data.m_isRetained = false;
/* 652 */           fields = new IdcVector();
/* 653 */           fields.add(data);
/* 654 */           this.m_fieldMaps.put(fieldNameKey, fields);
/*     */ 
/* 656 */           SystemUtils.trace("archiver", "No field mapping found between \"" + fieldName + "\" (source) and \"" + toField + "\" (target).  " + "Mapping automatically.");
/*     */         }
/*     */ 
/* 661 */         mapValueEx(fieldName, toField, value, props, false);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void mapValueEx(String fieldName, String toField, String value, Properties props, boolean isRetained)
/*     */     throws ServiceException
/*     */   {
/* 671 */     if (!isExcludedField(toField))
/*     */     {
/* 673 */       if (this.m_usedValueMaps != null)
/*     */       {
/* 675 */         this.m_usedValueMaps.remove(toField);
/*     */       }
/* 677 */       mapValue(toField, value, props);
/*     */     }
/* 679 */     if ((!isRetained) || (isExcludedField(fieldName)))
/*     */       return;
/* 681 */     if (this.m_usedValueMaps != null)
/*     */     {
/* 683 */       this.m_usedValueMaps.remove(fieldName);
/*     */     }
/* 685 */     mapValue(fieldName, value, props);
/*     */   }
/*     */ 
/*     */   protected void mapValue(String field, String value, Properties props)
/*     */     throws ServiceException
/*     */   {
/* 691 */     String mValue = value;
/* 692 */     ValueMapData valueData = (ValueMapData)this.m_valueMaps.get(field);
/* 693 */     if (valueData != null)
/*     */     {
/* 695 */       mValue = valueData.getMappedValue(field, value);
/*     */     }
/*     */ 
/* 698 */     mValue = validateOptionValue(field, mValue);
/* 699 */     props.put(field, mValue);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 704 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95262 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.archive.ArchiveImportStateInformation
 * JD-Core Version:    0.5.4
 */