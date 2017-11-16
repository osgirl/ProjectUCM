/*     */ package intradoc.serialize;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.text.ParseException;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DataBinderLocalizer
/*     */ {
/*     */   public static final int ALL = -1;
/*     */   public static final int DATE = 1;
/*     */   public static final int MESSAGE = 2;
/*     */   public static final int MESSAGE2 = 6;
/*     */   public static final int KEY = 8;
/*     */   public static final String TYPE_DATE = "date";
/*     */   public static final String TYPE_MESSAGE = "message";
/*     */   public static final String TYPE_MESSAGE2 = "message2";
/*     */   public static final String TYPE_KEY = "key";
/*  44 */   public static final Object[][] TYPE_MAP = { { new Integer(1), "date" }, { new Integer(2), "message" }, { new Integer(6), "message2" }, { new Integer(8), "key" } };
/*     */   protected DataBinder m_binder;
/*     */   protected ExecutionContext m_cxt;
/*     */   protected IdcDateFormat m_cxtFormat;
/*  56 */   protected boolean m_localeDateFormatMatch = false;
/*  57 */   protected boolean m_blDateFormatMatch = false;
/*     */   protected Map m_newLocalizedFields;
/*     */   protected int m_typeMask;
/*     */ 
/*     */   public DataBinderLocalizer(DataBinder binder, ExecutionContext cxt)
/*     */   {
/*  63 */     this.m_binder = binder;
/*  64 */     this.m_cxt = cxt;
/*     */   }
/*     */ 
/*     */   public void localizeBinder(int typeMask)
/*     */   {
/*  69 */     this.m_cxtFormat = ((IdcDateFormat)this.m_cxt.getLocaleResource(3));
/*  70 */     localizeBinderWithDateFormat(typeMask, this.m_cxtFormat);
/*     */   }
/*     */ 
/*     */   public void localizeBinderWithDateFormat(int typeMask, IdcDateFormat fmt)
/*     */   {
/*  79 */     this.m_typeMask = typeMask;
/*  80 */     if (fmt == null)
/*     */     {
/*  82 */       fmt = LocaleResources.m_odbcFormat;
/*     */     }
/*     */ 
/*  85 */     if (this.m_binder.m_localeDateFormat != null)
/*     */     {
/*  87 */       this.m_localeDateFormatMatch = fmt.equals(this.m_binder.m_localeDateFormat);
/*     */     }
/*  89 */     if (this.m_binder.m_blDateFormat != null)
/*     */     {
/*  91 */       this.m_blDateFormatMatch = fmt.equals(this.m_binder.m_blDateFormat);
/*     */     }
/*     */ 
/*  94 */     this.m_newLocalizedFields = new HashMap();
/*     */ 
/*  97 */     Enumeration en = this.m_binder.m_localData.keys();
/*  98 */     while (en.hasMoreElements())
/*     */     {
/* 100 */       String key = (String)en.nextElement();
/* 101 */       String value = this.m_binder.m_localData.getProperty(key);
/*     */       try
/*     */       {
/* 104 */         String newValue = localizeField(key, value, fmt);
/* 105 */         if ((SystemUtils.m_verbose) && (newValue != null))
/*     */         {
/* 107 */           Report.debug("localization", "localized " + value + " into " + newValue + " for key " + key, null);
/*     */         }
/*     */ 
/* 110 */         value = newValue;
/*     */       }
/*     */       catch (ParseException e)
/*     */       {
/* 114 */         value = "(NaD)";
/*     */       }
/* 116 */       if (value != null)
/*     */       {
/* 118 */         this.m_binder.putLocal(key, value);
/*     */       }
/*     */     }
/*     */ 
/* 122 */     en = this.m_binder.getResultSetList();
/* 123 */     while (en.hasMoreElements())
/*     */     {
/* 125 */       int localTypeMask = typeMask;
/* 126 */       String name = (String)en.nextElement();
/* 127 */       if (this.m_binder.m_localizedResultSets.get(name) != null)
/*     */       {
/* 130 */         localTypeMask &= -7;
/*     */       }
/* 132 */       ResultSet rset = this.m_binder.getResultSet(name);
/* 133 */       DataResultSet drset = coerceResultSet(rset, fmt, localTypeMask);
/* 134 */       if (drset != null)
/*     */       {
/* 136 */         this.m_binder.addResultSet(name, drset);
/*     */       }
/* 138 */       if ((this.m_typeMask & 0x2) != 0)
/*     */       {
/* 141 */         this.m_binder.m_localizedResultSets.put(name, "");
/*     */       }
/*     */     }
/*     */ 
/* 145 */     if ((this.m_typeMask & 0x1) != 0)
/*     */     {
/* 147 */       this.m_binder.m_blDateFormat = fmt;
/* 148 */       this.m_binder.m_localeDateFormat = null;
/* 149 */       this.m_binder.m_determinedDataDateFormat = true;
/* 150 */       this.m_binder.m_convertDatabaseDateFormats = false;
/*     */     }
/* 152 */     DataBinder.mergeHashTables(this.m_binder.m_localizedFields, this.m_newLocalizedFields);
/* 153 */     this.m_binder.m_checkLocalLocalization = false;
/* 154 */     this.m_binder.m_checkResultSetLocalization = false;
/*     */   }
/*     */ 
/*     */   public DataResultSet coerceResultSet(ResultSet rset, IdcDateFormat fmt, int localTypeMask)
/*     */   {
/* 168 */     DataResultSet retRset = null;
/*     */ 
/* 171 */     boolean isMutable = rset.isMutable();
/*     */ 
/* 175 */     boolean hasRawObjects = rset.hasRawObjects();
/*     */ 
/* 178 */     boolean isDataResultSet = rset instanceof DataResultSet;
/*     */ 
/* 181 */     boolean dateFormatMatches = fmt.equals(rset.getDateFormat());
/* 182 */     boolean convertOnlyJdbcDates = false;
/* 183 */     if (dateFormatMatches)
/*     */     {
/* 185 */       if (!this.m_binder.m_convertDatabaseDateFormats)
/*     */       {
/* 187 */         localTypeMask &= -2;
/*     */       }
/*     */       else
/*     */       {
/* 191 */         convertOnlyJdbcDates = true;
/*     */       }
/*     */     }
/*     */ 
/* 195 */     if ((hasRawObjects) && ((localTypeMask & 0x1) != 0))
/*     */     {
/* 197 */       rset.setDateFormat(fmt);
/* 198 */       localTypeMask &= -2;
/*     */     }
/*     */ 
/* 201 */     if ((localTypeMask == 0) || ((!isMutable) && (!isDataResultSet)))
/*     */     {
/* 203 */       return retRset;
/*     */     }
/*     */ 
/* 206 */     int count = rset.getNumFields();
/* 207 */     Vector fieldList = new IdcVector();
/* 208 */     Vector fieldTypes = new IdcVector();
/* 209 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 211 */       String key = rset.getFieldName(i);
/* 212 */       String typeStr = this.m_binder.getFieldType(key);
/*     */ 
/* 214 */       if (typeStr == null)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 221 */       int type = getType(typeStr);
/* 222 */       boolean needsWork = (type & localTypeMask) != 0;
/* 223 */       if (!needsWork)
/*     */         continue;
/* 225 */       FieldInfo info = new FieldInfo();
/* 226 */       rset.getFieldInfo(key, info);
/* 227 */       fieldList.addElement(info);
/* 228 */       fieldTypes.addElement(typeStr);
/*     */     }
/*     */ 
/* 232 */     int workFieldCount = fieldList.size();
/* 233 */     if (workFieldCount > 0)
/*     */     {
/* 235 */       boolean madeCopy = false;
/*     */       DataResultSet drsetVersion;
/*     */       DataResultSet drsetVersion;
/* 237 */       if ((isDataResultSet) && (isMutable))
/*     */       {
/* 239 */         drsetVersion = (DataResultSet)rset;
/*     */       }
/*     */       else
/*     */       {
/* 250 */         drsetVersion = new DataResultSet();
/* 251 */         boolean isRowPresent = rset.isRowPresent();
/* 252 */         int rowNbr = -1;
/* 253 */         if (isDataResultSet)
/*     */         {
/* 255 */           rowNbr = ((DataResultSet)rset).getCurrentRow();
/*     */         }
/* 257 */         drsetVersion.copy(rset, 0);
/* 258 */         if (rowNbr != -1)
/*     */         {
/* 260 */           drsetVersion.setCurrentRow(rowNbr);
/*     */         }
/* 262 */         else if (!isRowPresent)
/*     */         {
/* 264 */           drsetVersion.setCurrentRow(drsetVersion.getNumRows());
/*     */         }
/*     */ 
/* 267 */         rset = null;
/* 268 */         madeCopy = true;
/*     */       }
/*     */ 
/* 271 */       if ((drsetVersion.getSupportedFeatures() & 1L) == 0L)
/*     */       {
/* 273 */         return drsetVersion;
/*     */       }
/*     */ 
/* 276 */       DataResultSet iterator = drsetVersion.shallowClone();
/* 277 */       if (madeCopy)
/*     */       {
/* 279 */         retRset = drsetVersion;
/*     */       }
/*     */ 
/* 283 */       FieldInfo[] infos = new FieldInfo[workFieldCount];
/* 284 */       fieldList.copyInto(infos);
/* 285 */       int[] types = new int[workFieldCount];
/* 286 */       for (int i = 0; i < workFieldCount; ++i)
/*     */       {
/* 288 */         String type = (String)fieldTypes.elementAt(i);
/* 289 */         types[i] = getType(type);
/*     */       }
/* 291 */       for (iterator.first(); iterator.isRowPresent(); iterator.next())
/*     */       {
/* 293 */         Vector v = iterator.getCurrentRowValues();
/* 294 */         for (int i = 0; i < workFieldCount; ++i)
/*     */         {
/* 296 */           String value = null;
/* 297 */           switch (types[i])
/*     */           {
/*     */           case 1:
/* 301 */             boolean convertDate = true;
/* 302 */             if (convertOnlyJdbcDates)
/*     */             {
/* 308 */               value = iterator.getStringValue(infos[i].m_index);
/* 309 */               if ((value == null) || (!value.startsWith("{ts '")))
/*     */               {
/* 311 */                 convertDate = false;
/*     */               }
/*     */             }
/* 314 */             if (convertDate)
/*     */             {
/* 316 */               Date d = iterator.getDateValue(infos[i].m_index);
/* 317 */               if (d != null)
/*     */               {
/* 319 */                 value = fmt.format(d);
/*     */               }
/*     */               else
/*     */               {
/* 323 */                 value = iterator.getStringValue(infos[i].m_index);
/*     */               }
/*     */ 
/*     */             }
/*     */ 
/* 325 */             break;
/*     */           case 2:
/* 330 */             value = (String)v.elementAt(infos[i].m_index);
/* 331 */             value = LocaleResources.localizeMessage(value, this.m_cxt);
/* 332 */             break;
/*     */           case 6:
/* 336 */             value = (String)v.elementAt(infos[i].m_index);
/* 337 */             value = localizeMessage2(value);
/* 338 */             break;
/*     */           case 8:
/* 342 */             value = (String)v.elementAt(infos[i].m_index);
/* 343 */             value = LocaleResources.getString(value, this.m_cxt);
/*     */           case 3:
/*     */           case 4:
/*     */           case 5:
/*     */           case 7:
/*     */           }
/*     */ 
/* 350 */           if (value == null)
/*     */             continue;
/* 352 */           v.setElementAt(value, infos[i].m_index);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 358 */     if ((localTypeMask & 0x1) != 0)
/*     */     {
/* 360 */       ResultSet curRset = (retRset != null) ? retRset : rset;
/*     */ 
/* 363 */       curRset.setDateFormat(fmt);
/*     */     }
/* 365 */     return retRset;
/*     */   }
/*     */ 
/*     */   public String localizeField(String key, String value, IdcDateFormat fmt) throws ParseException
/*     */   {
/* 370 */     String typeStr = this.m_binder.getFieldType(key);
/* 371 */     if (typeStr == null)
/*     */     {
/* 373 */       return null;
/*     */     }
/*     */ 
/* 376 */     int type = getType(typeStr);
/* 377 */     if ((this.m_typeMask & type) == 0)
/*     */     {
/* 379 */       return null;
/*     */     }
/*     */ 
/* 382 */     boolean isLocalized = this.m_binder.m_localizedFields.get(key) != null;
/* 383 */     switch (type)
/*     */     {
/*     */     case 1:
/* 388 */       if ((value == null) || (value.length() == 0))
/*     */       {
/* 390 */         value = "";
/*     */       }
/*     */       else {
/* 393 */         Date d = null;
/* 394 */         if ((isLocalized) && (this.m_binder.m_localeDateFormat != null) && (!this.m_localeDateFormatMatch))
/*     */         {
/* 396 */           d = this.m_binder.m_localeDateFormat.parseDate(value);
/* 397 */           if (d == null)
/*     */           {
/* 399 */             value = "(NaD)";
/*     */           }
/*     */         }
/* 402 */         else if ((!isLocalized) && (!this.m_blDateFormatMatch))
/*     */         {
/* 404 */           d = this.m_binder.m_blDateFormat.parseDate(value);
/* 405 */           if (d == null)
/*     */           {
/* 407 */             value = "(NaD)";
/*     */           }
/*     */ 
/*     */         }
/* 411 */         else if (value.startsWith("{ts '"))
/*     */         {
/* 413 */           d = LocaleResources.m_odbcFormat.parseDate(value);
/*     */         }
/* 417 */         else if (NumberUtils.isInteger(value))
/*     */         {
/* 420 */           long longVal = NumberUtils.parseLong(value, 0L);
/* 421 */           if (longVal != 0L)
/*     */           {
/* 423 */             d = new Date(longVal);
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 428 */         if (d != null)
/*     */         {
/* 430 */           value = fmt.format(d); } 
/* 430 */       }break;
/*     */     case 2:
/* 436 */       if (!isLocalized)
/*     */       {
/* 438 */         value = LocaleResources.localizeMessage(value, this.m_cxt); } break;
/*     */     case 6:
/* 444 */       if (!isLocalized)
/*     */       {
/* 446 */         value = localizeMessage2(value); } break;
/*     */     case 8:
/* 452 */       if (!isLocalized)
/*     */       {
/* 454 */         value = LocaleResources.getString(value, this.m_cxt);
/*     */       }
/*     */     case 3:
/*     */     case 4:
/*     */     case 5:
/*     */     case 7:
/*     */     }
/*     */ 
/* 462 */     this.m_newLocalizedFields.put(key, "");
/* 463 */     return value;
/*     */   }
/*     */ 
/*     */   public int getType(String type)
/*     */   {
/* 468 */     int value = 0;
/* 469 */     for (Object[] typeData : TYPE_MAP)
/*     */     {
/* 471 */       if (!type.equals(typeData[1]))
/*     */         continue;
/* 473 */       value = ((Integer)typeData[0]).intValue();
/* 474 */       break;
/*     */     }
/*     */ 
/* 477 */     return value;
/*     */   }
/*     */ 
/*     */   public String localizeMessage2(String value)
/*     */   {
/* 482 */     int index = value.indexOf("!");
/* 483 */     if (index >= 0)
/*     */     {
/* 485 */       String l = value.substring(0, index);
/* 486 */       String r = value.substring(index);
/* 487 */       Vector v = StringUtils.parseArray(r, ',', '\\');
/* 488 */       if (v.size() == 1)
/*     */       {
/* 490 */         r = (String)v.elementAt(0);
/*     */       }
/* 492 */       r = LocaleResources.localizeMessage(r, this.m_cxt);
/* 493 */       IdcStringBuilder builder = new IdcStringBuilder(r.length() + value.length());
/* 494 */       builder.append(l);
/* 495 */       builder.append(StringUtils.addEscapeChars(r, ',', '\\'));
/* 496 */       return builder.toString();
/*     */     }
/* 498 */     return value;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 503 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 84490 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.serialize.DataBinderLocalizer
 * JD-Core Version:    0.5.4
 */