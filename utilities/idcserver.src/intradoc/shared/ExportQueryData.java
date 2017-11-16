/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ExportQueryData extends SqlQueryData
/*     */ {
/*     */   protected boolean m_isTableArchive;
/*     */   protected String m_tablePrefix;
/*     */   protected String m_deleteTablePrefix;
/*     */ 
/*     */   public ExportQueryData()
/*     */   {
/*  30 */     this.m_isTableArchive = false;
/*  31 */     this.m_tablePrefix = null;
/*  32 */     this.m_deleteTablePrefix = null;
/*     */   }
/*     */ 
/*     */   public void init(boolean isTableArchive) {
/*  36 */     this.m_isTableArchive = isTableArchive;
/*     */   }
/*     */ 
/*     */   public String createQueryString(boolean isUseDate, String exportDate, boolean isAllowPublished)
/*     */     throws ServiceException
/*     */   {
/*  42 */     return createQueryStringEx(isUseDate, exportDate, isAllowPublished, true);
/*     */   }
/*     */ 
/*     */   public String createQueryStringEx(boolean isUseDate, String exportDate, boolean isAllowPublished, boolean isPreview)
/*     */     throws ServiceException
/*     */   {
/*  48 */     String query = super.createQueryString();
/*     */ 
/*  50 */     StringBuffer queryBuff = new StringBuffer();
/*  51 */     String queryStr = null;
/*  52 */     if (query.length() > 0)
/*     */     {
/*  54 */       queryBuff.append("(");
/*  55 */       queryBuff.append(query);
/*  56 */       queryBuff.append(")");
/*     */     }
/*     */ 
/*  59 */     if ((isUseDate) && 
/*  61 */       (exportDate != null))
/*     */     {
/*  63 */       if (queryBuff.length() > 0)
/*     */       {
/*  65 */         queryBuff.append(" AND ");
/*     */       }
/*  67 */       queryBuff.append("Revisions.dReleaseDate > ");
/*  68 */       queryBuff.append(exportDate);
/*     */     }
/*     */ 
/*  72 */     if (!isAllowPublished)
/*     */     {
/*  74 */       if (queryBuff.length() > 0)
/*     */       {
/*  76 */         queryBuff.append(" AND ");
/*     */       }
/*  78 */       queryBuff.append("(Revisions.dPublishState is null OR Revisions.dPublishState = '')");
/*     */     }
/*     */ 
/*  81 */     boolean isAllRevisions = StringUtils.convertToBool(getQueryProp("AllRevisions"), true);
/*  82 */     boolean isMostRecentMatching = StringUtils.convertToBool(getQueryProp("MostRecentMatching"), false);
/*  83 */     boolean showAllRevisions = SharedObjects.getEnvValueAsBoolean("ShowAllRevisionsForArchiverPreview", false);
/*     */ 
/*  85 */     if ((!isAllRevisions) && (((!isPreview) || (!showAllRevisions))))
/*     */     {
/*  87 */       String conjunction = null;
/*  88 */       if (queryBuff.length() > 0)
/*     */       {
/*  90 */         conjunction = " AND ";
/*     */       }
/*  92 */       boolean isLatest = StringUtils.convertToBool(getQueryProp("LatestRevisions"), false);
/*     */ 
/*  94 */       String latestRevQuery = null;
/*     */ 
/*  96 */       if (isMostRecentMatching)
/*     */       {
/*  98 */         latestRevQuery = getMostRecentMatchingQuery(isPreview, conjunction, queryBuff.toString());
/*     */ 
/* 100 */         if (isPreview)
/*     */         {
/* 102 */           queryStr = "Revisions.dID IN (" + latestRevQuery + ")";
/*     */         }
/*     */         else
/*     */         {
/* 106 */           queryStr = "<$if not isPerClass$>" + queryBuff.toString() + "<$else$>Revisions.dID IN (" + latestRevQuery + ")<$endif$>";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 111 */         latestRevQuery = getLatestRevisionQuery(isLatest, isPreview, conjunction);
/* 112 */         queryStr = queryBuff.append(latestRevQuery).toString();
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 117 */       queryStr = queryBuff.toString();
/*     */     }
/*     */ 
/* 120 */     return queryStr;
/*     */   }
/*     */ 
/*     */   public void parse(String query)
/*     */   {
/* 127 */     super.parse(query);
/*     */   }
/*     */ 
/*     */   public String createExportTableQuery(String lastExportDate, String currentExportDate, boolean isDelete)
/*     */     throws ServiceException
/*     */   {
/* 133 */     IdcStringBuilder queryBuff = new IdcStringBuilder();
/* 134 */     queryBuff = createExportTableQuery(queryBuff, isDelete);
/* 135 */     queryBuff = addTimeConstraint(queryBuff, lastExportDate, currentExportDate, isDelete);
/* 136 */     return queryBuff.toString();
/*     */   }
/*     */ 
/*     */   public IdcStringBuilder createExportTableQuery(IdcStringBuilder queryBuffer, boolean isDelete) throws ServiceException
/*     */   {
/* 141 */     String query = "";
/* 142 */     if (!isDelete)
/*     */     {
/* 144 */       query = createQueryString();
/*     */     }
/*     */ 
/* 147 */     queryBuffer.append(query);
/*     */ 
/* 150 */     String sourceIdStr = getQueryProp("useSourceID");
/* 151 */     boolean useSourceID = StringUtils.convertToBool(sourceIdStr, false);
/* 152 */     String tablePrefix = getTablePrefix(isDelete);
/* 153 */     if (useSourceID)
/*     */     {
/* 155 */       String value = getQueryProp("sourceIDValue");
/* 156 */       String field = getQueryProp("sourceIDColumn");
/* 157 */       if ((field != null) && (value != null))
/*     */       {
/* 159 */         if (field.indexOf(46) < 0)
/*     */         {
/* 161 */           field = tablePrefix + field;
/*     */         }
/* 163 */         value = value + '%';
/*     */ 
/* 165 */         prepareAppend(queryBuffer, "AND", true);
/* 166 */         appendClause(field, queryBuffer, "LIKE", value);
/*     */       }
/*     */     }
/*     */ 
/* 170 */     createRelationshipQuery(queryBuffer, isDelete);
/* 171 */     return queryBuffer;
/*     */   }
/*     */ 
/*     */   public IdcStringBuilder createRelationshipQuery(IdcStringBuilder buffer, boolean isDelete)
/*     */     throws ServiceException
/*     */   {
/* 177 */     if (isDelete)
/*     */     {
/* 179 */       String tablePrefix = getTablePrefix(true);
/* 180 */       prepareAppend(buffer, "AND", true);
/* 181 */       buffer.append('(');
/* 182 */       String field = tablePrefix + "dTable";
/* 183 */       String value = getQueryProp("aTableName");
/* 184 */       appendClause(field, buffer, "=", value);
/*     */ 
/* 186 */       String parentIDs = getQueryProp("aParentTables");
/* 187 */       if ((parentIDs != null) && (parentIDs.length() != 0))
/*     */       {
/* 189 */         Vector ids = StringUtils.parseArray(parentIDs, ',', ',');
/*     */ 
/* 191 */         int size = ids.size();
/*     */ 
/* 193 */         for (int i = 0; i < size; ++i)
/*     */         {
/* 195 */           String id = (String)ids.elementAt(i);
/* 196 */           prepareAppend(buffer, "OR", false);
/* 197 */           appendClause(field, buffer, "=", id);
/*     */         }
/*     */       }
/*     */ 
/* 201 */       buffer.append(')');
/*     */     }
/* 203 */     return buffer;
/*     */   }
/*     */ 
/*     */   public IdcStringBuilder addTimeConstraint(IdcStringBuilder queryBuff, String lastExportDate, String currentExportDate, boolean isDelete)
/*     */   {
/* 209 */     if ((((lastExportDate == null) || (lastExportDate.length() == 0))) && (((currentExportDate == null) || (currentExportDate.length() == 0))))
/*     */     {
/* 212 */       return queryBuff;
/*     */     }
/*     */ 
/* 215 */     String[] columnKeys = { "aCreateTimeStamp", "aModifiedTimeStamp" };
/* 216 */     if (isDelete)
/*     */     {
/* 218 */       columnKeys = new String[] { "aDeletedTimeStamp" };
/*     */     }
/* 220 */     String tablePrefix = getTablePrefix(isDelete);
/*     */ 
/* 225 */     String[] exportDates = { " BETWEEN ", lastExportDate, " AND ", currentExportDate };
/*     */ 
/* 227 */     if ((lastExportDate == null) || (lastExportDate.length() == 0))
/*     */     {
/* 229 */       exportDates = new String[] { " <= ", currentExportDate };
/*     */     }
/*     */ 
/* 232 */     boolean isFirstColumn = true;
/* 233 */     boolean needTail = false;
/* 234 */     for (int i = 0; i < columnKeys.length; ++i)
/*     */     {
/* 236 */       String column = getQueryProp(columnKeys[i]);
/* 237 */       if (isDelete)
/*     */       {
/* 239 */         column = "dDeleteDate";
/*     */       }
/* 241 */       if ((column == null) || (column.length() == 0))
/*     */         continue;
/* 243 */       if (isFirstColumn)
/*     */       {
/* 245 */         if (queryBuff.length() > 0)
/*     */         {
/* 247 */           prepareAppend(queryBuff, "AND", true);
/* 248 */           needTail = true;
/*     */         }
/* 250 */         isFirstColumn = false;
/*     */       }
/*     */       else
/*     */       {
/* 254 */         queryBuff.append(" OR ");
/*     */       }
/* 256 */       queryBuff.append("(");
/*     */ 
/* 258 */       if (column.indexOf(46) < 0)
/*     */       {
/* 260 */         queryBuff.append(tablePrefix);
/*     */       }
/* 262 */       queryBuff.append(column);
/* 263 */       for (int j = 0; j < exportDates.length; ++j)
/*     */       {
/* 265 */         queryBuff.append(exportDates[j]);
/*     */       }
/* 267 */       queryBuff.append(") ");
/*     */     }
/*     */ 
/* 270 */     if ((!isFirstColumn) && (needTail))
/*     */     {
/* 272 */       queryBuff.append(") ");
/*     */     }
/*     */ 
/* 275 */     if (queryBuff.length() > 0)
/*     */     {
/* 277 */       queryBuff.insert(0, '(');
/* 278 */       queryBuff.append(')');
/*     */     }
/* 280 */     return queryBuff;
/*     */   }
/*     */ 
/*     */   public String getLatestRevisionQuery(boolean isLatest, boolean isPreview, String conjunction)
/*     */   {
/* 285 */     StringBuffer buff = new StringBuffer();
/*     */ 
/* 287 */     if (!isPreview)
/*     */     {
/* 289 */       buff.append("<$if isPerClass$>");
/*     */     }
/* 291 */     if (conjunction != null)
/*     */     {
/* 293 */       buff.append(conjunction);
/*     */     }
/* 295 */     buff.append("Revisions.dID");
/* 296 */     if (!isLatest)
/*     */     {
/* 298 */       buff.append(" NOT");
/*     */     }
/*     */ 
/* 301 */     buff.append(" IN (SELECT MAX(Rev2.dID) From Revisions Rev2 WHERE Rev2.dReleaseState <> 'E'");
/* 302 */     if (isPreview)
/*     */     {
/* 304 */       buff.append(" AND Rev2.dRevClassID = Revisions.dRevClassID)");
/*     */     }
/* 306 */     if (!isPreview)
/*     */     {
/* 308 */       buff.append(" AND Rev2.dRevClassID=<$dRevClassID$>)<$endif$>");
/*     */     }
/*     */ 
/* 311 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public String getMostRecentMatchingQuery(boolean isPreview, String conjunction, String mainWhereClause)
/*     */   {
/* 316 */     StringBuffer buff = new StringBuffer();
/*     */ 
/* 320 */     buff.append("SELECT MAX(Rev2.dID) FROM Revisions Rev2, DocMeta, Documents");
/* 321 */     buff.append(" WHERE Rev2.dID = Documents.dID");
/* 322 */     buff.append(" AND Rev2.dID = DocMeta.dID");
/* 323 */     buff.append(" AND Documents.dIsPrimary <> 0");
/* 324 */     buff.append(" AND Rev2.dReleaseState <> 'E' AND Rev2.dReleaseState <> 'N'");
/* 325 */     buff.append(" AND ");
/* 326 */     buff.append(mainWhereClause);
/* 327 */     if (isPreview)
/*     */     {
/* 329 */       buff.append(" AND Rev2.dRevClassID = Revisions.dRevClassID");
/*     */     }
/*     */     else
/*     */     {
/* 333 */       buff.append(" AND Rev2.dRevClassID=<$dRevClassID$>");
/*     */     }
/*     */ 
/* 336 */     return buff.toString();
/*     */   }
/*     */ 
/*     */   public void addExportQueryOptions(Properties props, String[] keys)
/*     */   {
/* 341 */     if ((keys == null) || (props == null))
/*     */     {
/* 343 */       return;
/*     */     }
/* 345 */     for (int i = 0; i < keys.length; ++i)
/*     */     {
/* 347 */       String value = props.getProperty(keys[i]);
/* 348 */       if (value == null)
/*     */         continue;
/* 350 */       setQueryProp(keys[i], value);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void createClauseString(Vector clause, IdcStringBuilder query)
/*     */     throws ServiceException
/*     */   {
/* 359 */     if (this.m_clauseDisplay == null)
/*     */     {
/* 361 */       String field = (String)clause.elementAt(0);
/* 362 */       String tablePrefix = "";
/* 363 */       if (this.m_isTableArchive)
/*     */       {
/* 365 */         tablePrefix = getQueryProp("aTableName");
/* 366 */         if ((tablePrefix != null) && (tablePrefix.length() != 0))
/*     */         {
/* 368 */           tablePrefix = tablePrefix + ".";
/*     */         }
/*     */ 
/*     */       }
/* 373 */       else if ((field.equalsIgnoreCase("dDocName")) || (field.equalsIgnoreCase("dID")) || (field.equalsIgnoreCase("dRevClassID")))
/*     */       {
/* 376 */         tablePrefix = "Revisions.";
/*     */       }
/*     */ 
/* 379 */       if (field.indexOf(46) < 0)
/*     */       {
/* 381 */         field = tablePrefix + field;
/*     */       }
/* 383 */       String op = (String)clause.elementAt(1);
/* 384 */       String value = (String)clause.elementAt(2);
/*     */ 
/* 386 */       appendClause(field, query, op, value);
/*     */     }
/*     */     else
/*     */     {
/* 390 */       this.m_clauseDisplay.createClauseString(clause, query);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getTablePrefix(boolean isDelete)
/*     */   {
/* 396 */     String tablePrefix = null;
/* 397 */     if (!isDelete)
/*     */     {
/* 399 */       if (this.m_tablePrefix == null)
/*     */       {
/* 401 */         String useParentTSStr = getQueryProp("aUseParentTS");
/* 402 */         boolean useParentTS = StringUtils.convertToBool(useParentTSStr, false);
/* 403 */         if (!useParentTS)
/*     */         {
/* 405 */           tablePrefix = getQueryProp("aTableName");
/*     */         }
/*     */         else
/*     */         {
/* 409 */           tablePrefix = getQueryProp("aParentTables");
/*     */         }
/* 411 */         if ((tablePrefix != null) && (tablePrefix.length() != 0))
/*     */         {
/* 413 */           tablePrefix = tablePrefix + ".";
/*     */         }
/*     */         else
/*     */         {
/* 417 */           tablePrefix = "";
/*     */         }
/* 419 */         this.m_tablePrefix = tablePrefix;
/*     */       }
/*     */       else
/*     */       {
/* 423 */         tablePrefix = this.m_tablePrefix;
/*     */       }
/*     */ 
/*     */     }
/* 428 */     else if (this.m_deleteTablePrefix == null)
/*     */     {
/* 430 */       tablePrefix = getQueryProp("aDeletedTimeStamp");
/* 431 */       if ((tablePrefix != null) && (tablePrefix.length() != 0))
/*     */       {
/* 433 */         int index = tablePrefix.indexOf(46);
/* 434 */         if (index > 0)
/*     */         {
/* 436 */           tablePrefix = tablePrefix.substring(0, index + 1);
/*     */         }
/*     */         else
/*     */         {
/* 440 */           tablePrefix = "";
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 445 */         tablePrefix = "";
/*     */       }
/* 447 */       this.m_deleteTablePrefix = tablePrefix;
/*     */     }
/*     */     else
/*     */     {
/* 451 */       tablePrefix = this.m_deleteTablePrefix;
/*     */     }
/*     */ 
/* 454 */     return tablePrefix;
/*     */   }
/*     */ 
/*     */   protected void prepareAppend(IdcStringBuilder buf, String conjunction, boolean useParenthesis)
/*     */   {
/* 459 */     if ((buf.length() == 0) || (conjunction.length() == 0))
/*     */     {
/* 461 */       return;
/*     */     }
/* 463 */     if (useParenthesis)
/*     */     {
/* 465 */       buf.insert(0, '(');
/* 466 */       buf.append(')');
/*     */     }
/* 468 */     if (conjunction.length() != 0)
/*     */     {
/* 470 */       buf.append(' ');
/* 471 */       buf.append(conjunction);
/* 472 */       buf.append(' ');
/*     */     }
/* 474 */     if (!useParenthesis)
/*     */       return;
/* 476 */     buf.append('(');
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 483 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79753 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ExportQueryData
 * JD-Core Version:    0.5.4
 */