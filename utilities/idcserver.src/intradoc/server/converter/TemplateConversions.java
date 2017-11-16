/*     */ package intradoc.server.converter;
/*     */ 
/*     */ import intradoc.common.ExecutionContext;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.resource.ResourceUtils;
/*     */ import intradoc.server.LegacyDirectoryLocator;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.shared.ViewFieldDef;
/*     */ import intradoc.shared.ViewFields;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TemplateConversions
/*     */ {
/*  35 */   public static String m_directory = null;
/*  36 */   public static String m_fileName = "cvtemplates.hda";
/*  37 */   public static String m_tableName = "DCTemplateConversions";
/*  38 */   public static String[] COLUMNS = { "dcCriteriaName", "dcCriteria", "dcCriteriaTemplate" };
/*     */ 
/*  42 */   public static int m_criteriaRows = 2;
/*  43 */   public static int m_newCriterias = 2;
/*     */ 
/*  46 */   public static String m_tableName46 = "TemplateConversions";
/*  47 */   public static String[] COLUMNS46 = { "fieldName", "fieldValue", "dDocName" };
/*     */ 
/*     */   public static void initDirectory()
/*     */     throws ServiceException
/*     */   {
/*  54 */     m_directory = LegacyDirectoryLocator.getAppDataDirectory() + "conversion/";
/*  55 */     FileUtils.checkOrCreateDirectory(m_directory, 1);
/*     */   }
/*     */ 
/*     */   public static void refresh()
/*     */     throws DataException, ServiceException
/*     */   {
/*  61 */     initDirectory();
/*     */ 
/*  64 */     DataBinder binder = new DataBinder();
/*  65 */     ResourceUtils.serializeDataBinder(m_directory, m_fileName, binder, false, false);
/*     */ 
/*  67 */     if (Report.m_verbose)
/*     */     {
/*  69 */       Report.debug(null, "TemplateConversions#refresh: Reading  m_directory=" + m_directory + "  m_fileName=" + m_fileName, null);
/*     */     }
/*     */ 
/*  73 */     DataResultSet drset = null;
/*  74 */     boolean isSaveFile = false;
/*     */ 
/*  77 */     DataResultSet drset46 = (DataResultSet)binder.getResultSet(m_tableName46);
/*  78 */     if (drset46 != null)
/*     */     {
/*  80 */       drset = upgrade46Table(drset46, binder);
/*  81 */       isSaveFile = true;
/*     */     }
/*     */     else
/*     */     {
/*  86 */       drset = (DataResultSet)binder.getResultSet(m_tableName);
/*  87 */       if (drset == null)
/*     */       {
/*  90 */         drset = new DataResultSet(COLUMNS);
/*  91 */         binder.addResultSet(m_tableName, drset);
/*  92 */         binder.putLocal("conversionFormat", "");
/*     */ 
/*  94 */         isSaveFile = true;
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*  99 */     if (isSaveFile)
/*     */     {
/* 101 */       if (Report.m_verbose)
/*     */       {
/* 103 */         Report.debug(null, "TemplateConversions#refresh: save file " + m_directory + m_fileName, null);
/*     */       }
/*     */ 
/* 106 */       FileUtils.reserveDirectory(m_directory);
/*     */       try
/*     */       {
/* 109 */         ResourceUtils.serializeDataBinder(m_directory, m_fileName, binder, true, false);
/*     */       }
/*     */       finally
/*     */       {
/* 114 */         FileUtils.releaseDirectory(m_directory);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 119 */     SharedObjects.putTable(m_tableName, drset);
/*     */ 
/* 121 */     String conversionFormat = binder.getLocal("conversionFormat");
/* 122 */     if (conversionFormat == null)
/*     */     {
/* 124 */       conversionFormat = "";
/*     */     }
/* 126 */     SharedObjects.putEnvironmentValue("conversionFormat", conversionFormat);
/*     */   }
/*     */ 
/*     */   public static DataResultSet upgrade46Table(DataResultSet drset46, DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 132 */     DataResultSet drset = new DataResultSet(COLUMNS);
/* 133 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, COLUMNS, true);
/* 134 */     FieldInfo[] oldfi = ResultSetUtils.createInfoList(drset46, COLUMNS46, true);
/*     */ 
/* 136 */     int rowCounter = 1;
/* 137 */     for (drset46.first(); drset46.isRowPresent(); drset46.next())
/*     */     {
/* 140 */       String oldFieldName = drset46.getStringValue(oldfi[0].m_index);
/* 141 */       String oldFieldValue = drset46.getStringValue(oldfi[1].m_index);
/* 142 */       String oldTemplateName = drset46.getStringValue(oldfi[2].m_index);
/*     */ 
/* 145 */       Vector row = new IdcVector();
/* 146 */       row.setSize(COLUMNS.length);
/* 147 */       row.setElementAt(String.valueOf(rowCounter), fi[0].m_index);
/* 148 */       row.setElementAt(oldFieldName + " " + oldFieldValue, fi[1].m_index);
/* 149 */       row.setElementAt(oldTemplateName, fi[2].m_index);
/* 150 */       drset.addRow(row);
/*     */ 
/* 152 */       ++rowCounter;
/*     */     }
/*     */ 
/* 156 */     binder.removeResultSet(m_tableName46);
/* 157 */     binder.addResultSet(m_tableName, drset);
/*     */ 
/* 159 */     return drset;
/*     */   }
/*     */ 
/*     */   public static void load(ExecutionContext cxt, DataBinder binder, boolean isDisplay)
/*     */     throws DataException
/*     */   {
/* 165 */     String formats = SharedObjects.getEnvironmentValue("conversionFormat");
/* 166 */     if (formats == null)
/*     */     {
/* 168 */       formats = "";
/*     */     }
/*     */ 
/* 172 */     DataResultSet drset = SharedObjects.getTable(m_tableName);
/* 173 */     if (drset == null)
/*     */     {
/* 175 */       String msg = LocaleUtils.encodeMessage("csUnableToLoadTable", null, m_tableName);
/*     */ 
/* 177 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 181 */     String[] convFields = new String[3 + m_criteriaRows * 2];
/* 182 */     convFields[0] = "dcName";
/* 183 */     convFields[1] = "dcTemplate";
/* 184 */     convFields[2] = "isNew";
/* 185 */     for (int i = 0; i < m_criteriaRows; ++i)
/*     */     {
/* 187 */       convFields[(3 + i * 2)] = ("dcField" + i);
/* 188 */       convFields[(4 + i * 2)] = ("dcValue" + i);
/*     */     }
/* 190 */     DataResultSet convSet = new DataResultSet(convFields);
/* 191 */     int numConvFields = convFields.length;
/*     */ 
/* 194 */     FieldInfo[] fi = ResultSetUtils.createInfoList(drset, COLUMNS, true);
/* 195 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 197 */       String dcName = drset.getStringValue(fi[0].m_index);
/* 198 */       String dcCriteria = drset.getStringValue(fi[1].m_index);
/* 199 */       String dcTemplate = drset.getStringValue(fi[2].m_index);
/*     */ 
/* 201 */       Vector row = new IdcVector();
/* 202 */       row.setSize(numConvFields);
/* 203 */       for (int i = 0; i < numConvFields; ++i)
/*     */       {
/* 205 */         row.setElementAt("", i);
/*     */       }
/* 207 */       row.setElementAt(dcName, 0);
/* 208 */       row.setElementAt(dcTemplate, 1);
/* 209 */       row.setElementAt("0", 2);
/*     */ 
/* 212 */       Vector criteriaList = StringUtils.parseArray(dcCriteria, '\t', '^');
/* 213 */       int critSize = criteriaList.size();
/* 214 */       if (critSize > m_criteriaRows)
/*     */       {
/* 216 */         critSize = m_criteriaRows;
/*     */       }
/* 218 */       for (int i = 0; i < critSize; ++i)
/*     */       {
/* 220 */         String nameValuePair = (String)criteriaList.elementAt(i);
/* 221 */         int index = nameValuePair.indexOf(" ");
/* 222 */         if (index <= 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 226 */         String name = nameValuePair.substring(0, index);
/* 227 */         String value = nameValuePair.substring(index + 1);
/*     */ 
/* 229 */         row.setElementAt(name, i * 2 + 3);
/* 230 */         row.setElementAt(value, i * 2 + 4);
/*     */       }
/* 232 */       convSet.addRow(row);
/*     */     }
/*     */ 
/* 236 */     if (isDisplay)
/*     */     {
/* 238 */       for (int i = 0; i < m_newCriterias; ++i)
/*     */       {
/* 240 */         Vector row = new IdcVector();
/* 241 */         row.setSize(numConvFields);
/* 242 */         for (int j = 0; j < numConvFields; ++j)
/*     */         {
/* 244 */           row.setElementAt("", j);
/*     */         }
/* 246 */         row.setElementAt("new", 0);
/* 247 */         row.setElementAt("1", 2);
/* 248 */         convSet.addRow(row);
/*     */       }
/*     */ 
/* 252 */       loadDocFieldMapping(cxt, binder);
/*     */ 
/* 255 */       DataResultSet formatSet = SharedObjects.getTable("DocFormats");
/* 256 */       if (formatSet == null)
/*     */       {
/* 258 */         String msg = LocaleUtils.encodeMessage("csUnableToLoadTable", null, "DocFormats");
/*     */ 
/* 260 */         throw new DataException(msg);
/*     */       }
/* 262 */       binder.addResultSet("DocFormats", formatSet);
/*     */     }
/*     */ 
/* 265 */     binder.putLocal("dcFormats", formats);
/* 266 */     binder.addResultSet("DCConversions", convSet);
/* 267 */     binder.putLocal("dcCriteriaRows", String.valueOf(m_criteriaRows));
/* 268 */     binder.putLocal("dcCriteriaTotal", String.valueOf(convSet.getNumRows()));
/*     */   }
/*     */ 
/*     */   public static void loadDocFieldMapping(ExecutionContext cxt, DataBinder binder)
/*     */     throws DataException
/*     */   {
/* 275 */     ViewFields fields = new ViewFields(cxt);
/* 276 */     fields.addStandardDocFields();
/* 277 */     DataResultSet docMetaSet = SharedObjects.getTable("DocMetaDefinition");
/* 278 */     fields.addMetaFields(docMetaSet);
/*     */ 
/* 280 */     String[] docMappingList = { "docField", "caption" };
/* 281 */     DataResultSet drset = new DataResultSet(docMappingList);
/*     */ 
/* 284 */     Vector emptyRow = new IdcVector();
/* 285 */     emptyRow.addElement(" ");
/* 286 */     emptyRow.addElement(" ");
/* 287 */     drset.addRow(emptyRow);
/*     */ 
/* 290 */     Vector docFields = fields.m_viewFields;
/* 291 */     int numDocFields = docFields.size();
/* 292 */     for (int i = 0; i < numDocFields; ++i)
/*     */     {
/* 294 */       ViewFieldDef fieldDef = (ViewFieldDef)docFields.elementAt(i);
/* 295 */       String name = fieldDef.m_name;
/* 296 */       String caption = fieldDef.m_caption;
/*     */ 
/* 299 */       if (name.equals("dRevLabel"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 305 */       if (fieldDef.m_type.equalsIgnoreCase("memo"))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/* 310 */       Vector row = new IdcVector();
/* 311 */       row.addElement(name);
/* 312 */       row.addElement(caption);
/* 313 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 317 */     Vector row = new IdcVector();
/* 318 */     row.addElement("dFormat");
/* 319 */     row.addElement("wwFormat");
/* 320 */     drset.addRow(row);
/*     */ 
/* 323 */     row = new IdcVector();
/* 324 */     row.addElement("HTTP_USER_AGENT");
/* 325 */     row.addElement("wwUserAgent");
/* 326 */     drset.addRow(row);
/*     */ 
/* 328 */     binder.addResultSet("DocFieldMapping", drset);
/*     */   }
/*     */ 
/*     */   public static void save(DataBinder binder)
/*     */     throws DataException, ServiceException
/*     */   {
/* 334 */     initDirectory();
/* 335 */     DataBinder convBinder = new DataBinder();
/* 336 */     ResourceUtils.serializeDataBinder(m_directory, m_fileName, convBinder, false, false);
/*     */ 
/* 339 */     boolean isSaveConversions = StringUtils.convertToBool(binder.getLocal("saveConversions"), false);
/*     */ 
/* 343 */     if (isSaveConversions)
/*     */     {
/* 345 */       boolean hasNextName = false;
/* 346 */       int nextNameValue = -1;
/*     */ 
/* 348 */       DataResultSet drset = (DataResultSet)convBinder.getResultSet(m_tableName);
/* 349 */       if (drset == null)
/*     */       {
/* 351 */         drset = new DataResultSet(COLUMNS);
/* 352 */         convBinder.addResultSet(m_tableName, drset);
/*     */       }
/* 354 */       FieldInfo[] fi = ResultSetUtils.createInfoList(drset, COLUMNS, true);
/*     */ 
/* 357 */       String updateListStr = binder.getLocal("updateCriteriaList");
/* 358 */       if (updateListStr == null)
/*     */       {
/* 360 */         updateListStr = "";
/*     */       }
/* 362 */       Vector updateList = StringUtils.parseArray(updateListStr, ',', '^');
/* 363 */       int updateSize = updateList.size();
/* 364 */       for (int i = 0; i < updateSize; ++i)
/*     */       {
/* 366 */         Vector row = null;
/* 367 */         boolean isCreateRow = false;
/*     */ 
/* 369 */         String dcName = (String)updateList.elementAt(i);
/* 370 */         if (dcName.equalsIgnoreCase("new"))
/*     */         {
/* 372 */           isCreateRow = true;
/* 373 */           if (hasNextName)
/*     */           {
/* 375 */             ++nextNameValue;
/*     */           }
/*     */           else
/*     */           {
/* 379 */             nextNameValue = getNextNameValue(drset);
/* 380 */             hasNextName = true;
/*     */           }
/* 382 */           dcName = String.valueOf(nextNameValue);
/*     */         }
/*     */         else
/*     */         {
/* 386 */           row = drset.findRow(fi[0].m_index, dcName);
/* 387 */           if (row == null)
/*     */           {
/* 389 */             isCreateRow = true;
/*     */           }
/*     */         }
/* 392 */         if (isCreateRow)
/*     */         {
/* 394 */           row = new IdcVector();
/* 395 */           row.setSize(COLUMNS.length);
/*     */ 
/* 397 */           row.setElementAt(dcName, 0);
/*     */         }
/*     */ 
/* 401 */         StringBuffer criteriaBuf = new StringBuffer();
/* 402 */         boolean isFirstTime = true;
/*     */ 
/* 404 */         for (int j = 0; j < m_criteriaRows; ++j)
/*     */         {
/* 406 */           String dcField = binder.getLocal("dcField" + j + ":" + i);
/* 407 */           String dcValue = binder.getLocal("dcValue" + j + ":" + i);
/* 408 */           if ((dcField.equals("")) || (dcValue.equals("")))
/*     */             continue;
/* 410 */           if (isFirstTime)
/*     */           {
/* 412 */             criteriaBuf.append(dcField + " " + dcValue);
/* 413 */             isFirstTime = false;
/*     */           }
/*     */           else
/*     */           {
/* 417 */             criteriaBuf.append("\t" + dcField + " " + dcValue);
/*     */           }
/*     */         }
/*     */ 
/* 421 */         String dcCriteria = criteriaBuf.toString();
/* 422 */         if (dcCriteria.equals("")) {
/*     */           continue;
/*     */         }
/*     */ 
/* 426 */         row.setElementAt(dcCriteria, 1);
/*     */ 
/* 429 */         String dcTemplate = binder.getLocal("dcTemplate" + i);
/* 430 */         if (dcTemplate == null) {
/*     */           continue;
/*     */         }
/*     */ 
/* 434 */         row.setElementAt(dcTemplate, 2);
/*     */ 
/* 436 */         if (!isCreateRow)
/*     */           continue;
/* 438 */         drset.addRow(row);
/*     */       }
/*     */ 
/* 443 */       String deleteListStr = binder.getLocal("deleteCriteriaList");
/* 444 */       if (deleteListStr == null)
/*     */       {
/* 446 */         deleteListStr = "";
/*     */       }
/* 448 */       Vector deleteList = StringUtils.parseArray(deleteListStr, ',', '^');
/* 449 */       int deleteSize = deleteList.size();
/* 450 */       for (int i = 0; i < deleteSize; ++i)
/*     */       {
/* 452 */         String dcName = (String)deleteList.elementAt(i);
/* 453 */         Vector row = drset.findRow(fi[0].m_index, dcName);
/* 454 */         if (row == null)
/*     */           continue;
/* 456 */         drset.deleteCurrentRow();
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 463 */       String dcFormats = binder.getLocal("dcFormats");
/* 464 */       if (dcFormats == null)
/*     */       {
/* 466 */         dcFormats = "";
/*     */       }
/* 468 */       dcFormats = dcFormats.trim();
/* 469 */       convBinder.putLocal("conversionFormat", dcFormats);
/*     */     }
/*     */ 
/* 473 */     FileUtils.reserveDirectory(m_directory);
/*     */     try
/*     */     {
/* 476 */       ResourceUtils.serializeDataBinder(m_directory, m_fileName, convBinder, true, false);
/*     */     }
/*     */     finally
/*     */     {
/* 481 */       FileUtils.releaseDirectory(m_directory);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static int getNextNameValue(DataResultSet drset) throws DataException
/*     */   {
/* 487 */     int nextNameValue = 1;
/*     */ 
/* 489 */     int index = ResultSetUtils.getIndexMustExist(drset, "dcCriteriaName");
/* 490 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 492 */       String currentName = drset.getStringValue(index);
/* 493 */       int currentValue = Integer.parseInt(currentName);
/* 494 */       if (currentValue <= nextNameValue)
/*     */         continue;
/* 496 */       nextNameValue = currentValue;
/*     */     }
/*     */ 
/* 500 */     ++nextNameValue;
/* 501 */     return nextNameValue;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 506 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.converter.TemplateConversions
 * JD-Core Version:    0.5.4
 */