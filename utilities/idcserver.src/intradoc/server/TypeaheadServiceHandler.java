/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.shared.ResultSetTreeSort;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcMessage;
/*     */ import java.util.List;
/*     */ 
/*     */ public class TypeaheadServiceHandler extends ServiceHandler
/*     */ {
/*     */   protected boolean m_isDBCaseSensitive;
/*     */   protected Boolean m_useMemoFieldsForDefaultColumns;
/*     */ 
/*     */   public TypeaheadServiceHandler()
/*     */   {
/*  38 */     this.m_isDBCaseSensitive = SharedObjects.getEnvValueAsBoolean("DatabasePreserveCase", true);
/*     */   }
/*     */ 
/*     */   protected void appendColumnNameToQuery(IdcStringBuilder whereClause, String columnName)
/*     */   {
/*  44 */     if (this.m_isDBCaseSensitive)
/*     */     {
/*  46 */       whereClause.append("LOWER(");
/*     */     }
/*  48 */     whereClause.append(columnName);
/*  49 */     if (!this.m_isDBCaseSensitive)
/*     */       return;
/*  51 */     whereClause.append(')');
/*     */   }
/*     */ 
/*     */   protected void appendStringToQuery(IdcStringBuilder whereClause, String str, boolean preWildcard, boolean postWildcard)
/*     */   {
/*  57 */     if (this.m_isDBCaseSensitive)
/*     */     {
/*  59 */       whereClause.append("LOWER(");
/*     */     }
/*  61 */     if (preWildcard)
/*     */     {
/*  63 */       whereClause.append("'%");
/*     */     }
/*     */     else
/*     */     {
/*  67 */       whereClause.append('\'');
/*     */     }
/*  69 */     whereClause.append(str);
/*  70 */     if (postWildcard)
/*     */     {
/*  72 */       whereClause.append("%'");
/*     */     }
/*     */     else
/*     */     {
/*  76 */       whereClause.append('\'');
/*     */     }
/*  78 */     if (!this.m_isDBCaseSensitive)
/*     */       return;
/*  80 */     whereClause.append(')');
/*     */   }
/*     */ 
/*     */   protected void prepareTypeaheadQuery(boolean doPrefix)
/*     */     throws DataException, ServiceException
/*     */   {
/*  86 */     boolean doSubstring = !doPrefix;
/*  87 */     String filterStringKey = this.m_currentAction.getParamAt(0);
/*  88 */     String searchColumnsKey = this.m_currentAction.getParamAt(1);
/*     */ 
/*  90 */     String filterString = determineSearchFilterFromKey(filterStringKey);
/*  91 */     String[] searchColumns = determineSearchColumnNamesFromKey(searchColumnsKey);
/*     */ 
/*  93 */     IdcStringBuilder whereClause = new IdcStringBuilder("(");
/*  94 */     boolean didClause = false;
/*  95 */     for (int i = 0; i < searchColumns.length; ++i)
/*     */     {
/*  97 */       if (null == searchColumns[i]) continue; if (searchColumns[i].length() < 1) {
/*     */         continue;
/*     */       }
/*     */ 
/* 101 */       if (didClause)
/*     */       {
/* 103 */         whereClause.append(" OR (");
/*     */       }
/*     */       else
/*     */       {
/* 107 */         whereClause.append('(');
/*     */       }
/* 109 */       if (!doPrefix)
/*     */       {
/* 111 */         whereClause.append("NOT ");
/*     */       }
/* 113 */       appendColumnNameToQuery(whereClause, searchColumns[i]);
/* 114 */       whereClause.append(" LIKE ");
/* 115 */       appendStringToQuery(whereClause, filterString, false, true);
/* 116 */       if (doSubstring)
/*     */       {
/* 118 */         whereClause.append(" AND ");
/* 119 */         appendColumnNameToQuery(whereClause, searchColumns[i]);
/* 120 */         whereClause.append(" LIKE ");
/* 121 */         appendStringToQuery(whereClause, filterString, true, true);
/*     */       }
/* 123 */       whereClause.append(')');
/* 124 */       didClause = true;
/*     */     }
/* 126 */     String extraWhere = this.m_binder.getLocal("extraWhereClause");
/* 127 */     if ((null != extraWhere) && (extraWhere.length() > 0))
/*     */     {
/* 129 */       whereClause.append(") AND (");
/* 130 */       whereClause.append(extraWhere);
/*     */     }
/* 132 */     whereClause.append(')');
/* 133 */     this.m_binder.putLocal("whereClause", whereClause.toString());
/*     */   }
/*     */ 
/*     */   protected String convertSQLWildcards(String str)
/*     */   {
/* 142 */     int numChars = str.length();
/* 143 */     char[] chars = new char[numChars];
/* 144 */     str.getChars(0, numChars, chars, 0);
/* 145 */     boolean hasWildcards = false;
/* 146 */     for (int i = 0; i < numChars; ++i)
/*     */     {
/* 148 */       if ('*' == chars[i])
/*     */       {
/* 150 */         hasWildcards = true;
/* 151 */         chars[i] = '%';
/*     */       } else {
/* 153 */         if ('?' != chars[i])
/*     */           continue;
/* 155 */         hasWildcards = true;
/* 156 */         chars[i] = '_';
/*     */       }
/*     */     }
/* 159 */     if (hasWildcards)
/*     */     {
/* 161 */       return new String(chars);
/*     */     }
/* 163 */     return null;
/*     */   }
/*     */ 
/*     */   protected String determineSearchFilterFromKey(String filterStringKey)
/*     */     throws DataException, ServiceException
/*     */   {
/* 171 */     String filter = this.m_binder.getAllowMissing(filterStringKey);
/* 172 */     if (null == filter)
/*     */     {
/* 174 */       return "%";
/*     */     }
/* 176 */     String converted = convertSQLWildcards(filter);
/* 177 */     return (null == converted) ? filter : converted;
/*     */   }
/*     */ 
/*     */   protected String[] determineSearchColumnNamesFromKey(String searchColumnsKey) throws DataException, ServiceException
/*     */   {
/* 182 */     String searchColumnsString = this.m_binder.getAllowMissing(searchColumnsKey);
/* 183 */     if (null != searchColumnsString)
/*     */     {
/* 185 */       List searchColumns = StringUtils.makeListFromSequenceSimple(searchColumnsString);
/* 186 */       int numColumns = searchColumns.size();
/* 187 */       if (numColumns > 0)
/*     */       {
/* 189 */         String[] columns = new String[numColumns];
/* 190 */         searchColumns.toArray(columns);
/* 191 */         return columns;
/*     */       }
/*     */     }
/* 194 */     IdcMessage msg = new IdcMessage("syParameterNotFound", new Object[] { searchColumnsKey });
/* 195 */     throw new DataException(null, msg);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareTypeaheadPrefixQuery()
/*     */     throws DataException, ServiceException
/*     */   {
/* 204 */     prepareTypeaheadQuery(true);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void prepareTypeaheadSubstringQuery() throws DataException, ServiceException
/*     */   {
/* 210 */     prepareTypeaheadQuery(false);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void createTypeaheadResults() throws DataException, ServiceException
/*     */   {
/* 216 */     String targetKey = this.m_currentAction.getParamAt(0);
/* 217 */     String targetName = this.m_binder.getLocal(targetKey);
/* 218 */     if (null == targetName)
/*     */     {
/* 220 */       targetName = "TYPEAHEAD_RESULTS";
/*     */     }
/* 222 */     String[] fieldNames = { "isTypeaheadPrefix" };
/* 223 */     DataResultSet target = new DataResultSet(fieldNames);
/*     */ 
/* 226 */     String limitRowsString = this.m_binder.getLocal("filterLimit");
/* 227 */     int limit = NumberUtils.parseInteger(limitRowsString, 0);
/* 228 */     this.m_binder.putLocal("MaxQueryRows", (limit > 0) ? String.valueOf(limit) : "");
/* 229 */     this.m_service.executeService("GET_TYPEAHEAD_PREFIX_RESULTS");
/* 230 */     ResultSet source = this.m_binder.getResultSet("TYPEAHEAD_PREFIX");
/* 231 */     target.mergeFields(source);
/* 232 */     target.merge(null, source, false);
/* 233 */     target.fillField(0, "1");
/*     */ 
/* 236 */     if (!StringUtils.convertToBool(this.m_binder.getActiveAllowMissing("NoTypeaheadSubstringQuery"), false))
/*     */     {
/* 238 */       boolean doQuery = true;
/* 239 */       if (limit > 0)
/*     */       {
/* 241 */         int numRows = target.getNumRows();
/* 242 */         if (numRows >= limit)
/*     */         {
/* 244 */           doQuery = false;
/*     */         }
/* 246 */         limit -= numRows;
/*     */       }
/* 248 */       if (doQuery)
/*     */       {
/* 250 */         this.m_binder.putLocal("MaxQueryRows", (limit > 0) ? String.valueOf(limit) : "");
/* 251 */         this.m_service.executeService("GET_TYPEAHEAD_SUBSTRING_RESULTS");
/* 252 */         source = this.m_binder.getResultSet("TYPEAHEAD_SUBSTRING");
/* 253 */         target.mergeFields(source);
/* 254 */         target.merge(null, source, false);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 259 */     String sortColumnsString = this.m_binder.getLocal("sortColumns");
/* 260 */     if (null != sortColumnsString)
/*     */     {
/* 262 */       List sortColumnsList = StringUtils.makeListFromSequenceSimple(sortColumnsString);
/* 263 */       int numColumns = sortColumnsList.size();
/* 264 */       if (numColumns > 0)
/*     */       {
/* 266 */         String[] sortColumns = new String[numColumns];
/* 267 */         sortColumnsList.toArray(sortColumns);
/* 268 */         FieldInfo[] sortFields = ResultSetUtils.createInfoList(target, sortColumns, true);
/*     */ 
/* 270 */         ResultSetTreeSort sorter = new ResultSetTreeSort(target);
/* 271 */         sorter.m_isMulticolumnSort = true;
/* 272 */         sorter.m_isAscending = true;
/* 273 */         sorter.m_sortColIndices = new int[numColumns + 1];
/* 274 */         sorter.m_fieldSortTypes = new int[numColumns + 1];
/*     */ 
/* 276 */         sorter.m_fieldSortTypes[0] = 3;
/* 277 */         for (int i = 0; i < numColumns; ++i)
/*     */         {
/* 279 */           sorter.m_sortColIndices[(i + 1)] = sortFields[i].m_index;
/*     */         }
/* 281 */         sorter.sort();
/*     */       }
/*     */     }
/*     */ 
/* 285 */     this.m_binder.addResultSet(targetName, target);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 292 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79417 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.TypeaheadServiceHandler
 * JD-Core Version:    0.5.4
 */