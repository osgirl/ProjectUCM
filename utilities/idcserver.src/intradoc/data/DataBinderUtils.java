/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcNumberFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ParseStringException;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.common.Table;
/*     */ import intradoc.common.TableUtils;
/*     */ import intradoc.common.TimeZoneFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Collections;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Iterator;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Set;
/*     */ import java.util.TimeZone;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataBinderUtils
/*     */ {
/*     */   public static int getInteger(DataBinder binder, String name, int def)
/*     */   {
/*  37 */     String value = binder.getAllowMissing(name);
/*  38 */     return NumberUtils.parseInteger(value, def);
/*     */   }
/*     */ 
/*     */   public static int getLocalInteger(DataBinder binder, String name, int def)
/*     */   {
/*  43 */     String value = binder.getLocal(name);
/*  44 */     return NumberUtils.parseInteger(value, def);
/*     */   }
/*     */ 
/*     */   public static long getLong(DataBinder binder, String name, long def)
/*     */   {
/*  49 */     String value = binder.getAllowMissing(name);
/*  50 */     return NumberUtils.parseLong(value, def);
/*     */   }
/*     */ 
/*     */   public static long getLocalLong(DataBinder binder, String name, long def)
/*     */   {
/*  55 */     String value = binder.getLocal(name);
/*  56 */     return NumberUtils.parseLong(value, def);
/*     */   }
/*     */ 
/*     */   public static boolean getBoolean(DataBinder binder, String name, boolean def)
/*     */   {
/*  69 */     String value = binder.getAllowMissing(name);
/*  70 */     return StringUtils.convertToBool(value, def);
/*     */   }
/*     */ 
/*     */   public static boolean getLocalBoolean(DataBinder binder, String name, boolean def)
/*     */   {
/*  83 */     String value = binder.getLocal(name);
/*  84 */     return StringUtils.convertToBool(value, def);
/*     */   }
/*     */ 
/*     */   public static Vector getList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 101 */     Vector v = null;
/* 102 */     String list = binder.get(name);
/* 103 */     if (list != null)
/*     */     {
/* 105 */       v = StringUtils.parseArray(list, sep1, sep2);
/*     */     }
/* 107 */     return v;
/*     */   }
/*     */ 
/*     */   public static Vector getLocalList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 124 */     Vector v = null;
/* 125 */     String list = binder.getEx(name, false, true, false, true);
/* 126 */     if (list != null)
/*     */     {
/* 128 */       v = StringUtils.parseArray(list, sep1, sep2);
/*     */     }
/* 130 */     return v;
/*     */   }
/*     */ 
/*     */   public static List getArrayList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 146 */     List l = null;
/* 147 */     String list = binder.get(name);
/* 148 */     if (list != null)
/*     */     {
/* 150 */       l = StringUtils.makeListFromSequence(list, sep1, sep2, 32);
/*     */     }
/* 152 */     return l;
/*     */   }
/*     */ 
/*     */   public static List getLocalArrayList(DataBinder binder, String name, char sep1, char sep2)
/*     */     throws DataException
/*     */   {
/* 168 */     List l = null;
/* 169 */     String list = binder.getEx(name, false, true, false, true);
/* 170 */     if (list != null)
/*     */     {
/* 172 */       l = StringUtils.makeListFromSequence(list, sep1, sep2, 32);
/*     */     }
/* 174 */     return l;
/*     */   }
/*     */ 
/*     */   public static void setOdbcDate(DataBinder binder, String name, long dateVal)
/*     */   {
/* 190 */     String dateStr = LocaleUtils.formatODBC(new Date(dateVal));
/* 191 */     binder.putLocal(name, dateStr);
/*     */   }
/*     */ 
/*     */   public static Properties createMergedProperties(DataBinder binder)
/*     */   {
/* 207 */     Properties props = new Properties();
/*     */ 
/* 209 */     Enumeration en = binder.getResultSetList();
/* 210 */     while (en.hasMoreElements())
/*     */     {
/* 212 */       String name = (String)en.nextElement();
/* 213 */       ResultSet rset = binder.getResultSet(name);
/* 214 */       if ((rset instanceof DataResultSet) && (rset.isRowPresent()))
/*     */       {
/* 216 */         DataResultSet drset = (DataResultSet)rset;
/* 217 */         Properties rowProps = drset.getCurrentRowProps();
/* 218 */         DataBinder.mergeHashTables(props, rowProps);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 223 */     Properties localData = binder.getLocalData();
/* 224 */     DataBinder.mergeHashTables(props, localData);
/*     */ 
/* 226 */     return props;
/*     */   }
/*     */ 
/*     */   public static Map createMergedMap(DataBinder binder)
/*     */   {
/* 241 */     Map props = new HashMap();
/*     */ 
/* 243 */     Enumeration en = binder.getResultSetList();
/* 244 */     while (en.hasMoreElements())
/*     */     {
/* 246 */       String name = (String)en.nextElement();
/* 247 */       ResultSet rset = binder.getResultSet(name);
/* 248 */       if ((rset instanceof DataResultSet) && (rset.isRowPresent()))
/*     */       {
/* 250 */         DataResultSet drset = (DataResultSet)rset;
/* 251 */         Map rowProps = drset.getCurrentRowMap();
/* 252 */         DataBinder.mergeHashTables(props, rowProps);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 257 */     Properties localData = binder.getLocalData();
/* 258 */     DataBinder.mergeHashTables(props, localData);
/*     */ 
/* 260 */     return props;
/*     */   }
/*     */ 
/*     */   public static Map getInternalMap(Parameters params)
/*     */   {
/* 265 */     Map map = null;
/* 266 */     if (params instanceof PropParameters)
/*     */     {
/* 268 */       PropParameters pp = (PropParameters)params;
/* 269 */       map = pp.m_properties;
/*     */     }
/* 271 */     else if (params instanceof DataBinder)
/*     */     {
/* 273 */       DataBinder data = (DataBinder)params;
/* 274 */       map = data.getLocalData();
/*     */     }
/* 276 */     return map;
/*     */   }
/*     */ 
/*     */   public static DataBinder createBinderFromParameters(Parameters params, ExecutionContext cxt)
/*     */   {
/* 281 */     DataBinder binder = new DataBinder();
/* 282 */     PropParameters pp = null;
/* 283 */     MapParameters mp = null;
/* 284 */     Parameters dflts = null;
/* 285 */     boolean isAllowDefaults = false;
/* 286 */     boolean hasDateFormat = false;
/* 287 */     if (params instanceof PropParameters)
/*     */     {
/* 289 */       pp = (PropParameters)params;
/* 290 */       binder.setLocalData(pp.m_properties);
/* 291 */       dflts = pp.m_defaultValues;
/* 292 */       isAllowDefaults = pp.m_allowDefaults;
/*     */     }
/* 294 */     else if (params instanceof MapParameters)
/*     */     {
/* 296 */       mp = (MapParameters)params;
/* 297 */       DataBinder.mergeHashTables(binder.getLocalData(), mp.m_map);
/* 298 */       dflts = mp.m_defaultValues;
/* 299 */       isAllowDefaults = mp.m_allowDefaults;
/*     */     }
/* 301 */     else if (params instanceof DataBinder)
/*     */     {
/* 303 */       binder = (DataBinder)params;
/* 304 */       hasDateFormat = true;
/*     */     }
/* 306 */     else if (cxt != null)
/*     */     {
/* 308 */       DataBinder data = (DataBinder)cxt.getCachedObject("DataBinder");
/* 309 */       if (data != null)
/*     */       {
/* 311 */         hasDateFormat = true;
/* 312 */         binder = data;
/* 313 */         if (!SystemUtils.m_verbose);
/*     */       }
/*     */     }
/*     */     try {
/* 317 */       throw new ServiceException("DataBinder in context is null.");
/*     */     }
/*     */     catch (Exception dfltsBinder)
/*     */     {
/* 321 */       Report.trace(null, null, e);
/*     */ 
/* 332 */       if (!hasDateFormat)
/*     */       {
/*     */         try
/*     */         {
/* 336 */           if (dflts instanceof DataBinder)
/*     */           {
/* 338 */             IdcDateFormat fmt = ((DataBinder)dflts).m_blDateFormat;
/* 339 */             if (fmt != null)
/*     */             {
/* 341 */               binder.m_blDateFormat = fmt;
/* 342 */               hasDateFormat = true;
/*     */             }
/*     */           }
/*     */           else
/*     */           {
/* 347 */             String dateFormat = params.getSystem("blDateFormat");
/* 348 */             if ((dateFormat != null) && (dateFormat.length() > 0))
/*     */             {
/* 350 */               IdcDateFormat fmt = new IdcDateFormat();
/* 351 */               fmt.init(dateFormat);
/* 352 */               binder.m_blDateFormat = fmt;
/* 353 */               hasDateFormat = true;
/*     */             }
/*     */           }
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 359 */           Report.trace("system", "Unable to compute date format.", e);
/*     */         }
/*     */       }
/*     */ 
/* 363 */       if ((!hasDateFormat) && (cxt != null))
/*     */       {
/* 365 */         IdcDateFormat blDateFormat = (IdcDateFormat)cxt.getLocaleResource(3);
/* 366 */         if (blDateFormat != null)
/*     */         {
/* 368 */           binder.m_blDateFormat = blDateFormat;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/* 375 */       if (binder.m_localeDateFormat == null)
/*     */       {
/* 377 */         IdcDateFormat dateFormat = null;
/* 378 */         DataBinder dataBinder = (DataBinder)cxt.getCachedObject("DataBinder");
/* 379 */         if (dataBinder != null)
/*     */         {
/* 381 */           String userDateFormat = dataBinder.getAllowMissing("UserDateFormat");
/* 382 */           String userTimeZone = dataBinder.getAllowMissing("UserTimeZone");
/* 383 */           if (userDateFormat != null)
/*     */           {
/*     */             try
/*     */             {
/* 387 */               dateFormat = new IdcDateFormat();
/* 388 */               if (userTimeZone != null)
/*     */               {
/* 390 */                 TimeZone tz = TimeZone.getTimeZone(userTimeZone);
/* 391 */                 TimeZoneFormat tzf = new TimeZoneFormat();
/* 392 */                 dateFormat.init(userDateFormat, tz, tzf, new IdcNumberFormat());
/*     */               }
/*     */               else
/*     */               {
/* 396 */                 dateFormat.init(userDateFormat);
/*     */               }
/* 398 */               dateFormat.setPattern(userDateFormat);
/* 399 */               binder.putLocal("UserDateFormat", userDateFormat);
/*     */             }
/*     */             catch (ParseStringException e)
/*     */             {
/* 403 */               String msg = LocaleUtils.encodeMessage("csUnableToSetUserDateFormat", e.getMessage());
/*     */ 
/* 405 */               Report.trace("system", msg, e);
/*     */             }
/*     */           }
/*     */         }
/*     */ 
/* 410 */         if (dateFormat == null)
/*     */         {
/* 412 */           dateFormat = (IdcDateFormat)cxt.getCachedObject("UserDateFormat");
/*     */         }
/*     */ 
/* 415 */         if (dateFormat != null)
/*     */         {
/* 417 */           binder.m_localeDateFormat = dateFormat;
/*     */ 
/* 422 */           if (!binder.m_determinedDataDateFormat)
/*     */           {
/* 424 */             binder.m_blDateFormat = dateFormat;
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 429 */       if ((!isAllowDefaults) || (dflts == null))
/*     */         break label681;
/* 431 */       Map map = null;
/* 432 */       if (dflts instanceof PropParameters)
/*     */       {
/* 434 */         pp = (PropParameters)dflts;
/* 435 */         map = pp.m_properties;
/* 436 */         dflts = pp.m_defaultValues;
/* 437 */         isAllowDefaults = pp.m_allowDefaults;
/*     */       }
/* 439 */       else if (dflts instanceof MapParameters)
/*     */       {
/* 441 */         mp = (MapParameters)dflts;
/* 442 */         map = mp.m_map;
/* 443 */         dflts = mp.m_defaultValues;
/* 444 */         isAllowDefaults = mp.m_allowDefaults;
/*     */       }
/* 446 */       else if (dflts instanceof DataBinder)
/*     */       {
/* 448 */         DataBinder dfltsBinder = (DataBinder)dflts;
/*     */ 
/* 453 */         Properties env = dfltsBinder.getEnvironment();
/* 454 */         binder.setEnvironment(env);
/*     */ 
/* 456 */         map = dfltsBinder.getLocalData();
/* 457 */         pp = null;
/* 458 */         isAllowDefaults = false;
/* 459 */         binder.copyResultSetStateShallow(dfltsBinder);
/*     */       }
/*     */       else
/*     */       {
/* 463 */         Report.trace(null, "DataUtils.createBinderFromParameters:unrecognized parameter object " + dflts.getClass().getName(), null);
/*     */ 
/* 465 */         dflts = null;
/*     */       }
/* 467 */       if (map != null)
/*     */       {
/* 469 */         addToBinder(map, binder);
/*     */       }
/*     */     }
/* 472 */     label681: return binder;
/*     */   }
/*     */ 
/*     */   public static void addToBinder(Map map, DataBinder binder)
/*     */   {
/* 477 */     Object[] allKeys = null;
/* 478 */     Properties props = null;
/* 479 */     if (map instanceof Properties)
/*     */     {
/* 481 */       props = (Properties)map;
/* 482 */       Enumeration iter = props.propertyNames();
/* 483 */       List allList = Collections.list(iter);
/* 484 */       allKeys = allList.toArray();
/*     */     }
/*     */     else
/*     */     {
/* 488 */       Set keys = map.keySet();
/* 489 */       allKeys = keys.toArray();
/*     */     }
/* 491 */     for (int i = 0; i < allKeys.length; ++i)
/*     */     {
/* 493 */       String key = (String)allKeys[i];
/* 494 */       String val = null;
/* 495 */       if (props != null)
/*     */       {
/* 497 */         val = props.getProperty(key);
/*     */       }
/*     */       else
/*     */       {
/* 501 */         val = (String)map.get(key);
/*     */       }
/*     */ 
/* 504 */       String curVal = binder.getLocal(key);
/* 505 */       if (curVal != null)
/*     */         continue;
/* 507 */       if (val != null)
/*     */       {
/* 509 */         binder.putLocal(key, val);
/*     */       } else {
/* 511 */         if (!SystemUtils.m_verbose)
/*     */           continue;
/*     */         try
/*     */         {
/* 515 */           throw new ServiceException("null value for key " + key);
/*     */         }
/*     */         catch (Exception e)
/*     */         {
/* 519 */           Report.trace(null, null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void localizeResultSetFieldsIntoColumn(MutableResultSet rs, String keyName, String[] argNames, String targetName, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 552 */     if ((null == targetName) || (targetName.length() < 1))
/*     */     {
/* 554 */       targetName = "label";
/*     */     }
/* 556 */     int targetIndex = rs.getFieldInfoIndex(targetName);
/* 557 */     if ((null == keyName) || (keyName.length() < 1))
/*     */     {
/* 559 */       keyName = targetName + "Key";
/*     */     }
/* 561 */     int keyIndex = rs.getFieldInfoIndex(keyName);
/* 562 */     if (keyIndex < 0)
/*     */     {
/* 564 */       throw new DataException(null, "syColumnDoesNotExist", new Object[] { keyName });
/*     */     }
/*     */ 
/* 567 */     int[] argIndices = null;
/* 568 */     if (null == argNames)
/*     */     {
/* 570 */       List argList = new ArrayList();
/* 571 */       int num = 1;
/* 572 */       IdcStringBuilder argName = new IdcStringBuilder();
/*     */       while (true)
/*     */       {
/* 575 */         argName.append(targetName);
/* 576 */         argName.append("Arg");
/* 577 */         argName.append(num);
/* 578 */         int index = rs.getFieldInfoIndex(argName.toStringNoRelease());
/* 579 */         if (index < 0) {
/*     */           break;
/*     */         }
/*     */ 
/* 583 */         argList.add(Integer.valueOf(index));
/* 584 */         argName.setLength(0);
/*     */       }
/* 586 */       if (--num > 0)
/*     */       {
/* 588 */         argIndices = new int[num];
/* 589 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 591 */           argIndices[i] = ((Integer)argList.get(i)).intValue();
/*     */         }
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 597 */       FieldInfo[] finfos = ResultSetUtils.createInfoListWithFlags(rs, argNames, 1);
/* 598 */       int num = finfos.length;
/* 599 */       argIndices = new int[num];
/* 600 */       for (int i = 0; i < num; ++i)
/*     */       {
/* 602 */         argIndices[i] = finfos[i].m_index;
/*     */       }
/*     */     }
/*     */ 
/* 606 */     if (targetIndex < 0)
/*     */     {
/* 608 */       List newFields = new ArrayList(1);
/* 609 */       FieldInfo targetField = new FieldInfo();
/* 610 */       targetField.m_name = targetName;
/* 611 */       newFields.add(targetField);
/* 612 */       rs.mergeFieldsWithFlags(newFields, 2);
/* 613 */       targetIndex = targetField.m_index;
/*     */     }
/* 615 */     localizeResultSetFieldsIntoColumn(rs, keyIndex, argIndices, targetIndex, cxt);
/*     */   }
/*     */ 
/*     */   public static void localizeResultSetFieldsIntoColumn(MutableResultSet rs, int keyField, int[] argFields, int targetField, ExecutionContext cxt)
/*     */     throws DataException
/*     */   {
/* 637 */     int numFields = rs.getNumFields();
/* 638 */     if ((keyField < 0) || (keyField >= numFields))
/*     */     {
/* 640 */       throw new DataException(null, "syInvalidColumnIndex", new Object[] { Integer.valueOf(keyField) });
/*     */     }
/* 642 */     if ((targetField < 0) || (targetField >= numFields))
/*     */     {
/* 644 */       throw new DataException(null, "syInvalidColumnIndex", new Object[] { Integer.valueOf(targetField) });
/*     */     }
/* 646 */     int numArgs = 0;
/* 647 */     String[] args = null;
/* 648 */     if (null != argFields)
/*     */     {
/* 650 */       numArgs = argFields.length;
/* 651 */       for (int i = 0; i < numArgs; ++i)
/*     */       {
/* 653 */         if ((argFields[i] >= 0) && (argFields[i] < numFields))
/*     */           continue;
/* 655 */         throw new DataException(null, "syInvalidColumnIndex", new Object[] { Integer.valueOf(argFields[i]) });
/*     */       }
/*     */ 
/* 658 */       args = new String[numArgs];
/*     */     }
/* 660 */     int currentRow = rs.getCurrentRow();
/*     */     try
/*     */     {
/* 663 */       for (rs.first(); rs.isRowPresent(); rs.next())
/*     */       {
/* 665 */         String value = rs.getStringValue(targetField);
/* 666 */         if ((null != value) && (value.length() > 0)) {
/*     */           continue;
/*     */         }
/*     */ 
/* 670 */         String key = rs.getStringValue(keyField);
/*     */ 
/* 672 */         if (null == args)
/*     */         {
/* 674 */           value = LocaleResources.getString(key, cxt);
/*     */         }
/*     */         else
/*     */         {
/* 678 */           for (int i = 0; i < numArgs; ++i)
/*     */           {
/* 680 */             args[i] = rs.getStringValue(argFields[i]);
/*     */           }
/*     */ 
/* 683 */           value = LocaleResources.getString(key, cxt, (Object[])args);
/*     */         }
/* 685 */         rs.setCurrentValue(targetField, value);
/*     */       }
/*     */ 
/*     */     }
/*     */     finally
/*     */     {
/* 691 */       rs.setCurrentRow(currentRow);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void mergeKeyValueVersionTableIntoBinder(Table t, DataBinder data, String version)
/*     */   {
/* 705 */     int[] fields = TableUtils.getIndexList(t, new String[] { "key", "value", "version" });
/* 706 */     for (int i = 0; i < t.getNumRows(); ++i)
/*     */     {
/* 708 */       boolean addValue = version == null;
/* 709 */       if (!addValue)
/*     */       {
/* 711 */         String v = t.getString(i, fields[2]);
/* 712 */         addValue = SystemUtils.isOlderVersion(version, v);
/*     */       }
/*     */ 
/* 715 */       if (!addValue)
/*     */         continue;
/* 717 */       String key = t.getString(i, fields[0]);
/* 718 */       String value = t.getString(i, fields[1]);
/* 719 */       data.putLocal(key, value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static boolean isDateField(String fieldName, DataBinder binder)
/*     */   {
/* 726 */     boolean isDate = isFieldOfType(fieldName, binder, "date");
/* 727 */     return isDate;
/*     */   }
/*     */ 
/*     */   public static boolean isIntField(String fieldName, DataBinder binder)
/*     */   {
/* 732 */     boolean isInt = isFieldOfType(fieldName, binder, "int");
/* 733 */     return isInt;
/*     */   }
/*     */ 
/*     */   public static boolean isDecimalField(String fieldName, DataBinder binder)
/*     */   {
/* 738 */     boolean isDecimal = isFieldOfType(fieldName, binder, "decimal");
/* 739 */     return isDecimal;
/*     */   }
/*     */ 
/*     */   public static boolean isTextField(String fieldName, DataBinder binder)
/*     */   {
/* 744 */     boolean isText = isFieldOfType(fieldName, binder, "string");
/* 745 */     return isText;
/*     */   }
/*     */ 
/*     */   public static boolean isFieldOfType(String fieldName, DataBinder binder, String inputType)
/*     */   {
/* 750 */     boolean isOfType = false;
/* 751 */     String fieldType = binder.getFieldType(fieldName);
/* 752 */     if (fieldType == null)
/*     */     {
/* 754 */       fieldType = binder.getFieldType(fieldName.toLowerCase());
/*     */     }
/* 756 */     if (((fieldType != null) && (fieldType.equalsIgnoreCase(inputType))) || ((fieldType == null) && ("string".equalsIgnoreCase(inputType))))
/*     */     {
/* 759 */       isOfType = true;
/*     */     }
/* 761 */     return isOfType;
/*     */   }
/*     */ 
/*     */   public static Map getFieldsOfType(DataBinder binder, String inputTypes)
/*     */   {
/* 766 */     Map requestedFields = new HashMap();
/* 767 */     List inputTypesList = StringUtils.makeListFromSequenceSimple(inputTypes);
/*     */ 
/* 769 */     Map fieldTypes = binder.getFieldTypes();
/* 770 */     Iterator fieldIterator = fieldTypes.keySet().iterator();
/*     */ 
/* 772 */     while (fieldIterator.hasNext())
/*     */     {
/* 774 */       String fieldName = (String)fieldIterator.next();
/* 775 */       String fieldType = (String)fieldTypes.get(fieldName);
/*     */ 
/* 777 */       if (inputTypesList.contains(fieldType))
/*     */       {
/* 779 */         requestedFields.put(fieldName, fieldType);
/*     */       }
/*     */     }
/*     */ 
/* 783 */     return requestedFields;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 788 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 97947 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataBinderUtils
 * JD-Core Version:    0.5.4
 */