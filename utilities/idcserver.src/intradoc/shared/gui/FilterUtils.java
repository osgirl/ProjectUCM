/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.common.LocaleResources;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.PropParameters;
/*     */ import intradoc.shared.ProfileUtils;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class FilterUtils
/*     */ {
/*  33 */   public static String[] DATE_AND_INT_OPERATORS = { ">=", "<" };
/*     */ 
/*     */   public static Vector createFilterData(ViewFields viewFields, Hashtable filterData, DataResultSet defaults, boolean isSplitFields)
/*     */   {
/*  40 */     Vector fields = viewFields.m_viewFields;
/*  41 */     Vector filterDataList = new IdcVector();
/*     */ 
/*  43 */     int size = fields.size();
/*  44 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  46 */       ViewFieldDef fieldDef = (ViewFieldDef)fields.elementAt(i);
/*  47 */       String name = fieldDef.m_name;
/*     */ 
/*  49 */       int numBuddies = 1;
/*  50 */       String ftype = fieldDef.m_type.toLowerCase();
/*  51 */       if ((ftype.equals("date")) && (!isSplitFields))
/*     */       {
/*  53 */         numBuddies = 2;
/*     */       }
/*  55 */       else if (ftype.length() == 0)
/*     */       {
/*  57 */         numBuddies = 0;
/*     */       }
/*     */ 
/*  61 */       Vector names = new IdcVector();
/*  62 */       if ((isSplitFields) && (((ftype.equals("date")) || (ftype.equals("int")))))
/*     */       {
/*  66 */         for (int j = 0; j < 2; ++j)
/*     */         {
/*  68 */           names.addElement(name + ":split_" + j);
/*     */         }
/*     */ 
/*     */       }
/*     */       else {
/*  73 */         names.addElement(name);
/*     */       }
/*     */ 
/*  76 */       int num = names.size();
/*  77 */       for (int j = 0; j < num; ++j)
/*     */       {
/*  79 */         String filterName = (String)names.elementAt(j);
/*     */ 
/*  81 */         ViewFieldDef fdCopy = new ViewFieldDef();
/*  82 */         fdCopy.copy(fieldDef);
/*     */ 
/*  84 */         if (num > 1)
/*     */         {
/*     */           ViewFieldDef tmp243_241 = fdCopy; tmp243_241.m_caption = (tmp243_241.m_caption + "  " + DATE_AND_INT_OPERATORS[j]);
/*     */         }
/*     */ 
/*  90 */         FilterData fd = createInfo(filterName, fdCopy, filterData, defaults, numBuddies, j);
/*  91 */         filterDataList.addElement(fd);
/*     */       }
/*     */     }
/*  94 */     return filterDataList;
/*     */   }
/*     */ 
/*     */   protected static FilterData createInfo(String name, ViewFieldDef fieldDef, Hashtable filterData, DataResultSet defaults, int numBuddies, int index)
/*     */   {
/* 100 */     FilterData fd = (FilterData)filterData.get(name);
/* 101 */     if (fd == null)
/*     */     {
/* 103 */       fd = new FilterData(name, fieldDef, numBuddies);
/* 104 */       filterData.put(name, fd);
/*     */     }
/*     */     else
/*     */     {
/* 108 */       fd.m_fieldDef = fieldDef;
/*     */     }
/*     */ 
/* 111 */     if (numBuddies > 0)
/*     */     {
/* 113 */       String ftype = fieldDef.m_type.toLowerCase();
/* 114 */       String op = fd.getOperatorAt(0);
/* 115 */       if ((op == null) || (op.length() == 0))
/*     */       {
/* 118 */         if (fieldDef.isMandatoryOptionList())
/*     */         {
/* 120 */           fd.setOperatorAt("LIKE", 0);
/*     */         }
/* 122 */         else if ((ftype.indexOf("text") >= 0) || (ftype.indexOf("memo") >= 0))
/*     */         {
/* 124 */           fd.setOperatorAt("LIKE", 0);
/*     */         }
/* 126 */         else if ((ftype.equals("date")) || (ftype.equals("int")))
/*     */         {
/* 128 */           if (numBuddies == 1)
/*     */           {
/* 130 */             fd.setOperatorAt(DATE_AND_INT_OPERATORS[index], 0);
/*     */           }
/* 132 */           else if (numBuddies == 2)
/*     */           {
/* 134 */             fd.setOperatorAt(DATE_AND_INT_OPERATORS[0], 0);
/* 135 */             fd.setOperatorAt(DATE_AND_INT_OPERATORS[1], 1);
/*     */           }
/*     */           else
/*     */           {
/* 139 */             fd.setOperatorAt(DATE_AND_INT_OPERATORS[0], 0);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 146 */     if (defaults != null)
/*     */     {
/* 148 */       Vector row = defaults.findRow(0, name);
/* 149 */       if (row != null)
/*     */       {
/* 151 */         Properties props = defaults.getCurrentRowProps();
/* 152 */         updateFilterData(fd, props);
/*     */       }
/*     */     }
/* 155 */     return fd;
/*     */   }
/*     */ 
/*     */   public static void updateFilterData(FilterData fd, Properties props)
/*     */   {
/* 160 */     fd.m_isUsed = StringUtils.convertToBool(props.getProperty("isEnabled"), false);
/*     */ 
/* 163 */     int num = fd.m_values.size();
/*     */ 
/* 166 */     String valStr = props.getProperty("filterValue");
/* 167 */     fd.m_values = StringUtils.parseArray(valStr, ',', '^');
/*     */ 
/* 170 */     int newNum = fd.m_values.size();
/* 171 */     for (int i = newNum; i < num; ++i)
/*     */     {
/* 173 */       fd.m_values.addElement("");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String buildWhereClause(Hashtable filterData)
/*     */     throws ServiceException
/*     */   {
/* 182 */     StringBuffer whereClause = new StringBuffer();
/* 183 */     for (Enumeration en = filterData.elements(); en.hasMoreElements(); )
/*     */     {
/* 185 */       FilterData fd = (FilterData)en.nextElement();
/* 186 */       if (!fd.m_isUsed)
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 191 */       String newClause = createFilterClause(fd);
/* 192 */       if ((newClause != null) && (newClause.length() > 0))
/*     */       {
/* 194 */         if (whereClause.length() > 0)
/*     */         {
/* 196 */           whereClause.append(" AND ");
/*     */         }
/* 198 */         whereClause.append(newClause);
/*     */       }
/*     */     }
/* 201 */     return whereClause.toString();
/*     */   }
/*     */ 
/*     */   public static String createFilterClause(FilterData fd) throws ServiceException
/*     */   {
/* 206 */     StringBuffer newClause = null;
/* 207 */     String type = fd.m_fieldDef.m_type;
/* 208 */     boolean isDate = type.equalsIgnoreCase("date");
/*     */ 
/* 210 */     int num = fd.m_values.size();
/* 211 */     if (num > 0)
/*     */     {
/* 213 */       newClause = new StringBuffer();
/*     */ 
/* 215 */       String value = fd.getValueAt(0);
/* 216 */       String op = fd.getOperatorAt(0);
/*     */ 
/* 218 */       if (!isDate)
/*     */       {
/* 220 */         newClause.append("(");
/*     */ 
/* 223 */         if (fd.m_fieldDef.m_name.equalsIgnoreCase("dDocName"))
/*     */         {
/* 225 */           newClause.append(fd.m_table);
/* 226 */           newClause.append('.');
/*     */         }
/* 228 */         newClause.append(fd.m_fieldDef.m_name);
/* 229 */         newClause.append(" ");
/*     */       }
/*     */ 
/* 232 */       if (type.equalsIgnoreCase("yes/no"))
/*     */       {
/* 234 */         boolean bValue = StringUtils.convertToBool(value, false);
/* 235 */         if (bValue == true)
/*     */         {
/* 237 */           newClause.append("<>0");
/*     */         }
/*     */         else
/*     */         {
/* 241 */           newClause.append("=0");
/*     */         }
/*     */       }
/* 244 */       else if (isDate)
/*     */       {
/* 246 */         Date dte = null;
/* 247 */         for (int i = 0; i < num; ++i)
/*     */         {
/* 249 */           value = fd.getValueAt(i);
/* 250 */           op = fd.getOperatorAt(i);
/* 251 */           if (value.length() <= 0)
/*     */             continue;
/* 253 */           if (newClause.length() == 0)
/*     */           {
/* 255 */             newClause.append(" (");
/*     */           }
/*     */           else
/*     */           {
/* 259 */             newClause.append(" AND ");
/*     */           }
/* 261 */           newClause.append(fd.m_fieldDef.m_name);
/* 262 */           newClause.append(op);
/*     */           try
/*     */           {
/* 265 */             dte = LocaleResources.parseDate(value, null);
/* 266 */             newClause.append(LocaleUtils.formatODBC(dte));
/*     */           }
/*     */           catch (ServiceException ignore)
/*     */           {
/* 270 */             newClause.append(value);
/*     */           }
/*     */         }
/*     */ 
/*     */       }
/* 275 */       else if (type.equalsIgnoreCase("int"))
/*     */       {
/* 277 */         newClause.append(op);
/* 278 */         newClause.append(value);
/*     */       }
/*     */       else
/*     */       {
/* 282 */         newClause.append(op);
/* 283 */         newClause.append(" '");
/* 284 */         newClause.append(StringUtils.createQuotableString(value));
/* 285 */         newClause.append("' ");
/* 286 */         if (value.length() == 0)
/*     */         {
/* 288 */           newClause.append("OR ");
/* 289 */           newClause.append(fd.m_table);
/* 290 */           newClause.append('.');
/* 291 */           newClause.append(fd.m_fieldDef.m_name);
/* 292 */           newClause.append(" IS NULL");
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 297 */     if ((fd.m_clause != null) && (fd.m_clause.length() > 0))
/*     */     {
/* 299 */       if (newClause.length() > 0)
/*     */       {
/* 301 */         newClause.append(" AND (");
/*     */       }
/* 303 */       newClause.append(fd.m_clause);
/*     */     }
/*     */ 
/* 306 */     if (newClause.length() > 0)
/*     */     {
/* 308 */       newClause.append(")");
/*     */     }
/*     */ 
/* 311 */     return newClause.toString();
/*     */   }
/*     */ 
/*     */   public static boolean compareFilterToDefault(FilterData fd, Properties props)
/*     */   {
/* 323 */     if (props == null)
/*     */     {
/* 325 */       return !fd.isSet();
/*     */     }
/*     */ 
/* 328 */     boolean isUsed = StringUtils.convertToBool(props.getProperty("isEnabled"), false);
/* 329 */     if (fd.m_isUsed != isUsed)
/*     */     {
/* 331 */       return false;
/*     */     }
/*     */ 
/* 334 */     String str = props.getProperty("filterValue");
/* 335 */     Vector values = StringUtils.parseArray(str, ',', '^');
/*     */ 
/* 337 */     int num1 = fd.m_values.size();
/* 338 */     int num2 = values.size();
/* 339 */     if (num1 != num2)
/*     */     {
/* 341 */       return false;
/*     */     }
/*     */ 
/* 344 */     for (int i = 0; i < num1; ++i)
/*     */     {
/* 346 */       String val1 = (String)fd.m_values.elementAt(i);
/* 347 */       String val2 = (String)values.elementAt(i);
/* 348 */       if (!val1.equals(val2))
/*     */       {
/* 350 */         return false;
/*     */       }
/*     */     }
/* 353 */     return true;
/*     */   }
/*     */ 
/*     */   public static DataResultSet createEmptyFilterSet(DataResultSet defSet)
/*     */   {
/* 358 */     if (defSet == null)
/*     */     {
/* 360 */       return new DataResultSet(new String[] { "filterColumn", "filterValue", "isEnabled" });
/*     */     }
/*     */ 
/* 364 */     DataResultSet drset = new DataResultSet();
/* 365 */     drset.copyFieldInfo(defSet);
/* 366 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Vector createFilterRow(FilterData data, DataResultSet drset)
/*     */   {
/* 371 */     Vector row = null;
/* 372 */     if (data.isSet())
/*     */     {
/* 374 */       Properties props = new Properties();
/* 375 */       props.put("filterColumn", data.m_id);
/* 376 */       props.put("filterValue", StringUtils.createString(data.m_values, ',', '^'));
/* 377 */       props.put("isEnabled", String.valueOf(data.m_isUsed));
/*     */       try
/*     */       {
/* 381 */         PropParameters args = new PropParameters(props);
/* 382 */         row = drset.createRow(args);
/*     */       }
/*     */       catch (DataException ignore)
/*     */       {
/* 386 */         if (SystemUtils.m_verbose)
/*     */         {
/* 388 */           Report.debug("system", null, ignore);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 393 */     return row;
/*     */   }
/*     */ 
/*     */   public static void createTopicEdits(String name, DataBinder binder, DataResultSet filterDefaults)
/*     */   {
/* 398 */     if (filterDefaults == null)
/*     */     {
/* 401 */       return;
/*     */     }
/*     */ 
/* 404 */     DataBinder filterEditData = new DataBinder();
/* 405 */     filterEditData.addResultSet(name, filterDefaults);
/*     */ 
/* 407 */     ProfileUtils.addTopicEdit("appcommongui", "updateSets", name, filterEditData, binder);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 412 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 95023 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.FilterUtils
 * JD-Core Version:    0.5.4
 */