/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class CommonQueryData extends ClausesData
/*     */ {
/*  30 */   public static final String[][] ALIASMAP = { { "dateLE", "dateLess" }, { "dateGreater", "dateGE" } };
/*     */   protected DataResultSet m_opCodes;
/*     */   protected DataResultSet m_opMap;
/*     */   protected String[][] m_escapeChar;
/*     */   protected String m_escapeCharConvert;
/*     */   protected boolean m_isApplet;
/*     */   protected String m_queryFormat;
/*     */   protected Hashtable m_configs;
/*     */   protected Hashtable m_exprMap;
/*     */   protected static final int CONJUNCTION = 0;
/*     */   protected static final int ESCAPECHAR = 1;
/*     */   protected static final int FIELDCOMPARISONINDEX = 2;
/*     */ 
/*     */   public CommonQueryData()
/*     */   {
/*  37 */     this.m_escapeChar = ((String[][])null);
/*     */ 
/*  41 */     this.m_queryFormat = "UNIVERSAL";
/*  42 */     this.m_configs = null;
/*  43 */     this.m_exprMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public void init(String engineName, boolean isApplet)
/*     */     throws ServiceException
/*     */   {
/*  55 */     if (engineName != null)
/*     */     {
/*  57 */       this.m_queryFormat = engineName;
/*     */     }
/*     */ 
/*  60 */     this.m_opCodes = getTable("SearchQueryOpStrMap");
/*     */ 
/*  62 */     this.m_operatorCodes = convertToStringArray(this.m_opCodes);
/*     */ 
/*  64 */     this.m_opMap = getTable("SearchQueryOpMap");
/*  65 */     this.m_operatorMap = convertToStringArray(this.m_opMap);
/*     */ 
/*  67 */     String fieldCompIndex = getEnvironmentValue("FieldComparisonIndex");
/*  68 */     this.m_fieldComparisonIndexStart = getComparisonIndex(fieldCompIndex);
/*  69 */     this.m_conjunction = (" " + getEnvironmentValue("SearchConjunction") + "\n");
/*     */ 
/*  72 */     String escape = getEnvironmentValue("EscapeChars");
/*  73 */     if (escape != null)
/*     */     {
/*  75 */       Vector v = StringUtils.parseArray(escape, ',', ',');
/*  76 */       this.m_escapeChar = new String[v.size()][2];
/*  77 */       int size = v.size();
/*  78 */       for (int i = 0; i < size; ++i)
/*     */       {
/*  80 */         String line = (String)v.elementAt(i);
/*  81 */         Vector escVec = StringUtils.parseArray(line, ':', ':');
/*  82 */         this.m_escapeChar[i][0] = ((String)escVec.elementAt(0));
/*  83 */         this.m_escapeChar[i][1] = ((String)escVec.elementAt(1));
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  90 */       int nameIndex = ResultSetUtils.getIndexMustExist(this.m_opMap, "OperatorName");
/*  91 */       int exprIndex = ResultSetUtils.getIndexMustExist(this.m_opMap, "Expression");
/*  92 */       for (this.m_opMap.first(); this.m_opMap.isRowPresent(); this.m_opMap.next())
/*     */       {
/*  94 */         String op = this.m_opMap.getStringValue(nameIndex);
/*  95 */         String expr = this.m_opMap.getStringValue(exprIndex);
/*  96 */         String[] parsed = parseQuery(expr);
/*  97 */         this.m_exprMap.put(op, parsed);
/*     */       }
/*     */     }
/*     */     catch (DataException ignore)
/*     */     {
/* 102 */       if (!SystemUtils.m_verbose)
/*     */         return;
/* 104 */       Report.debug("searchquery", null, ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String[] parseQuery(String query)
/*     */   {
/* 112 */     char[] queryArray = query.toCharArray();
/* 113 */     int[] index = new int[20];
/* 114 */     int arrayLen = 1;
/* 115 */     index[0] = 0;
/* 116 */     int len = queryArray.length - 1;
/* 117 */     for (int i = 0; i < len; ++i)
/*     */     {
/* 119 */       if ((queryArray[i] != '%') || (
/* 121 */         (queryArray[(i + 1)] != 'F') && (queryArray[(i + 1)] != 'V')))
/*     */         continue;
/* 123 */       if (index[(arrayLen - 1)] != i)
/*     */       {
/* 125 */         index[arrayLen] = i;
/* 126 */         ++arrayLen;
/*     */       }
/* 128 */       if (i + 1 >= len)
/*     */         continue;
/* 130 */       index[arrayLen] = (i + 2);
/* 131 */       ++arrayLen;
/*     */     }
/*     */ 
/* 136 */     index[arrayLen] = queryArray.length;
/*     */ 
/* 138 */     String[] parsed = new String[arrayLen];
/* 139 */     for (int i = 0; i < arrayLen; ++i)
/*     */     {
/* 141 */       parsed[i] = new String(queryArray, index[i], index[(i + 1)] - index[i]);
/*     */     }
/* 143 */     return parsed;
/*     */   }
/*     */ 
/*     */   protected short[] getComparisonIndex(String indexValue)
/*     */   {
/* 148 */     List indexList = StringUtils.parseArray(indexValue, ',', '\\');
/* 149 */     String[] index = StringUtils.convertListToArray(indexList);
/* 150 */     short[] result = new short[index.length];
/* 151 */     for (int i = 0; i < index.length; ++i)
/*     */     {
/* 153 */       result[i] = Short.parseShort(index[i]);
/*     */     }
/* 155 */     return result;
/*     */   }
/*     */ 
/*     */   protected DataResultSet getTable(String tableName)
/*     */   {
/* 160 */     DataResultSet result = SharedObjects.getTable(tableName);
/*     */ 
/* 162 */     if (result == null)
/*     */     {
/* 164 */       Hashtable configs = getQueryConfig();
/* 165 */       if (configs != null)
/*     */       {
/* 167 */         result = (DataResultSet)configs.get(tableName);
/*     */       }
/*     */     }
/* 170 */     return result;
/*     */   }
/*     */ 
/*     */   protected String getEnvironmentValue(String key)
/*     */   {
/* 175 */     String result = SharedObjects.getEnvironmentValue(key);
/*     */ 
/* 177 */     if (result == null)
/*     */     {
/* 179 */       Hashtable configs = getQueryConfig();
/* 180 */       if (configs != null)
/*     */       {
/* 182 */         result = (String)configs.get(key);
/*     */       }
/*     */     }
/* 185 */     return result;
/*     */   }
/*     */ 
/*     */   protected Hashtable getQueryConfig()
/*     */   {
/* 190 */     if (this.m_configs != null)
/*     */     {
/* 192 */       return this.m_configs;
/*     */     }
/* 194 */     String queryFormat = SharedObjects.getEnvironmentValue("SearchQueryFormat");
/* 195 */     if (queryFormat != null)
/*     */     {
/* 197 */       this.m_queryFormat = queryFormat;
/*     */     }
/* 199 */     if ((this.m_queryFormat != null) && (this.m_queryFormat.equalsIgnoreCase("native")))
/*     */     {
/* 201 */       this.m_queryFormat = SharedObjects.getEnvironmentValue("SearchIndexerEngineName");
/*     */     }
/*     */ 
/* 204 */     Hashtable containers = (Hashtable)SharedObjects.getObject("globalObjects", "CommonSearchClientObjects");
/*     */ 
/* 206 */     if (containers != null)
/*     */     {
/* 208 */       this.m_configs = ((Hashtable)containers.get(this.m_queryFormat));
/*     */     }
/* 210 */     return this.m_configs;
/*     */   }
/*     */ 
/*     */   protected String[][] convertToStringArray(DataResultSet drset)
/*     */   {
/* 215 */     if (drset == null)
/*     */     {
/* 217 */       return (String[][])null;
/*     */     }
/* 219 */     String[][] array = new String[drset.getNumRows()][drset.getNumFields()];
/* 220 */     for (int i = 0; i < array.length; ++i)
/*     */     {
/* 222 */       array[i] = StringUtils.convertListToArray(drset.getRowValues(i));
/*     */     }
/* 224 */     return array;
/*     */   }
/*     */ 
/*     */   public void appendOpAndValue(IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 238 */     op = findOperatorFromAlias(op);
/* 239 */     String[] expr = (String[])(String[])this.m_exprMap.get(op);
/* 240 */     IdcStringBuilder valueBuf = StringUtils.escapeCharArray(value.toCharArray(), this.m_escapeChar);
/*     */ 
/* 242 */     for (int i = 0; i < expr.length; ++i)
/*     */     {
/* 244 */       String querySub = expr[i];
/* 245 */       if (expr[i].equals("%F"))
/*     */       {
/* 247 */         querySub = field;
/*     */       }
/* 249 */       else if (expr[i].equals("%V"))
/*     */       {
/* 251 */         querySub = valueBuf.toString();
/*     */       }
/* 253 */       query.append(querySub);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String findOperatorFromAlias(String str)
/*     */   {
/* 261 */     str = QueryOperatorUtils.findOperatorFromAlias(str, ALIASMAP);
/* 262 */     return str;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 267 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.CommonQueryData
 * JD-Core Version:    0.5.4
 */