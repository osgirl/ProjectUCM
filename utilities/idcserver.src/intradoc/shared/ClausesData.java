/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class ClausesData
/*     */ {
/*     */   public static final short TEXT_OP = 0;
/*     */   public static final short DATE_OP = 1;
/*     */   public static final short BOOL_OP = 2;
/*     */   public static final short NUMBER_OP = 3;
/*     */   public static final short ZONE_OP = 4;
/*  45 */   public String m_dispStr = "";
/*     */ 
/*  48 */   public String m_customQuery = "";
/*     */ 
/*  51 */   public boolean m_isCustom = false;
/*     */   public Vector m_props;
/*     */   public Vector m_clauses;
/*     */   protected String[][] m_operatorCodes;
/*     */   protected String[][] m_operatorMap;
/*     */   protected String[][] m_legacyOpMap;
/*     */   protected short[] m_fieldComparisonIndexStart;
/*  75 */   protected String m_conjunction = " AND\n";
/*     */ 
/*  78 */   protected ClauseDisplay m_clauseDisplay = null;
/*     */ 
/*     */   public ClausesData()
/*     */   {
/*  83 */     this.m_isCustom = false;
/*  84 */     this.m_dispStr = "";
/*  85 */     this.m_props = new IdcVector();
/*  86 */     this.m_clauses = new IdcVector();
/*     */   }
/*     */ 
/*     */   public short[] comparisonIndexRange(int fieldType)
/*     */   {
/*  93 */     short startIndex = this.m_fieldComparisonIndexStart[fieldType];
/*  94 */     if (startIndex < 0)
/*     */     {
/*  96 */       fieldType = 0;
/*  97 */       startIndex = this.m_fieldComparisonIndexStart[0];
/*     */     }
/*     */ 
/* 100 */     short endIndex = (short)this.m_operatorCodes.length;
/* 101 */     int endLookup = fieldType + 1;
/* 102 */     while (endLookup < this.m_fieldComparisonIndexStart.length)
/*     */     {
/* 104 */       short e = this.m_fieldComparisonIndexStart[endLookup];
/* 105 */       if (e >= 0)
/*     */       {
/* 107 */         endIndex = e;
/* 108 */         break;
/*     */       }
/* 110 */       ++endLookup;
/*     */     }
/*     */ 
/* 113 */     return new short[] { startIndex, endIndex };
/*     */   }
/*     */ 
/*     */   public void parse(String query)
/*     */   {
/* 120 */     this.m_props.setSize(0);
/* 121 */     this.m_clauses.setSize(0);
/*     */ 
/* 124 */     Vector props = StringUtils.parseArray(query, '\t', '&');
/*     */ 
/* 126 */     int nprops = props.size();
/* 127 */     if (nprops == 0)
/*     */     {
/* 129 */       this.m_isCustom = false;
/* 130 */       this.m_customQuery = "";
/* 131 */       return;
/*     */     }
/*     */ 
/* 136 */     this.m_dispStr = ((String)props.elementAt(0));
/*     */ 
/* 139 */     for (int i = 1; i < nprops; ++i)
/*     */     {
/* 141 */       String nameValueStr = (String)props.elementAt(i);
/* 142 */       Vector nameValue = StringUtils.parseArray(nameValueStr, ' ', '%');
/*     */ 
/* 144 */       String name = (String)nameValue.elementAt(0);
/* 145 */       String val = (String)nameValue.elementAt(1);
/*     */ 
/* 149 */       if (name.equals("Clauses"))
/*     */       {
/* 151 */         Vector clauses = StringUtils.parseArray(val, ',', '^');
/* 152 */         int nclauses = clauses.size();
/* 153 */         for (int j = 0; j < nclauses; ++j)
/*     */         {
/* 155 */           String clause = (String)clauses.elementAt(j);
/* 156 */           Vector clauseElements = StringUtils.parseArray(clause, ':', '*');
/* 157 */           this.m_clauses.addElement(clauseElements);
/*     */         }
/*     */       }
/* 160 */       else if (name.equals("CustomQuery"))
/*     */       {
/* 162 */         this.m_customQuery = val;
/*     */       }
/* 164 */       else if (name.equals("IsCustom"))
/*     */       {
/* 166 */         this.m_isCustom = StringUtils.convertToBool(val, false);
/*     */       }
/*     */       else
/*     */       {
/* 170 */         this.m_props.addElement(nameValue);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String formatString()
/*     */   {
/* 178 */     int nprops = this.m_props.size();
/* 179 */     Vector outList = new IdcVector();
/* 180 */     outList.setSize(nprops + 4);
/* 181 */     outList.setElementAt(this.m_dispStr, 0);
/*     */ 
/* 183 */     for (int i = 0; i < nprops; ++i)
/*     */     {
/* 185 */       Vector nameValue = (Vector)this.m_props.elementAt(i);
/* 186 */       String nameValueStr = StringUtils.createString(nameValue, ' ', '%');
/* 187 */       outList.setElementAt(nameValueStr, i + 1);
/*     */     }
/*     */ 
/* 190 */     Vector clauses = new IdcVector();
/* 191 */     int nclauses = this.m_clauses.size();
/* 192 */     clauses.setSize(nclauses);
/* 193 */     for (int j = 0; j < nclauses; ++j)
/*     */     {
/* 195 */       Vector clauseElements = (Vector)this.m_clauses.elementAt(j);
/* 196 */       String clause = StringUtils.createString(clauseElements, ':', '*');
/* 197 */       clauses.setElementAt(clause, j);
/*     */     }
/* 199 */     String clausesValStr = StringUtils.createString(clauses, ',', '^');
/*     */ 
/* 201 */     setNameValueFormatAt(outList, nprops + 1, "Clauses", clausesValStr);
/* 202 */     setNameValueFormatAt(outList, nprops + 2, "CustomQuery", this.m_customQuery);
/*     */ 
/* 204 */     String isCustomStr = (this.m_isCustom) ? "1" : "0";
/* 205 */     setNameValueFormatAt(outList, nprops + 3, "IsCustom", isCustomStr);
/*     */ 
/* 208 */     String retVal = StringUtils.createString(outList, '\t', '&');
/* 209 */     return retVal;
/*     */   }
/*     */ 
/*     */   public String getQueryProp(String key)
/*     */   {
/* 216 */     if (key.equals("CustomQuery"))
/*     */     {
/* 218 */       return this.m_customQuery;
/*     */     }
/* 220 */     if (key.equals("IsCustom"))
/*     */     {
/* 222 */       return (this.m_isCustom) ? "1" : "0";
/*     */     }
/*     */ 
/* 225 */     int nprops = this.m_props.size();
/* 226 */     for (int i = 0; i < nprops; ++i)
/*     */     {
/* 228 */       Vector nameValue = (Vector)this.m_props.elementAt(i);
/* 229 */       String name = (String)nameValue.elementAt(0);
/* 230 */       if (name.equals(key))
/*     */       {
/* 232 */         return (String)nameValue.elementAt(1);
/*     */       }
/*     */     }
/*     */ 
/* 236 */     return null;
/*     */   }
/*     */ 
/*     */   public void setQueryProp(String key, String val)
/*     */   {
/* 242 */     if (key.equals("CustomQuery"))
/*     */     {
/* 244 */       this.m_customQuery = val;
/*     */     }
/* 246 */     else if (key.equals("IsCustom"))
/*     */     {
/* 248 */       this.m_isCustom = val.equals("1");
/*     */     }
/*     */     else
/*     */     {
/* 252 */       int nprops = this.m_props.size();
/* 253 */       for (int i = 0; i < nprops; ++i)
/*     */       {
/* 255 */         Vector nameValue = (Vector)this.m_props.elementAt(i);
/* 256 */         String name = (String)nameValue.elementAt(0);
/* 257 */         if (!name.equals(key))
/*     */           continue;
/* 259 */         nameValue.setElementAt(val, 1);
/* 260 */         return;
/*     */       }
/*     */ 
/* 264 */       Vector nameValue = createNameValue(key, val);
/* 265 */       this.m_props.addElement(nameValue);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void markQueryPropForUrl(String key)
/*     */   {
/* 273 */     int nprops = this.m_props.size();
/* 274 */     for (int i = 0; i < nprops; ++i)
/*     */     {
/* 276 */       Vector nameValue = (Vector)this.m_props.elementAt(i);
/* 277 */       String name = (String)nameValue.elementAt(0);
/* 278 */       if (!name.equals(key))
/*     */         continue;
/* 280 */       if (nameValue.size() >= 3)
/*     */       {
/* 282 */         return;
/*     */       }
/* 284 */       nameValue.addElement("AddToURL");
/* 285 */       return;
/*     */     }
/*     */ 
/* 288 */     Vector nameValue = new IdcVector();
/* 289 */     nameValue.addElement(key);
/* 290 */     nameValue.addElement("");
/* 291 */     nameValue.addElement("AddToURL");
/* 292 */     this.m_props.addElement(nameValue);
/*     */   }
/*     */ 
/*     */   public String createQueryString()
/*     */     throws ServiceException
/*     */   {
/* 300 */     if (this.m_isCustom)
/*     */     {
/* 303 */       return this.m_customQuery;
/*     */     }
/*     */ 
/* 307 */     int nclauses = this.m_clauses.size();
/* 308 */     IdcStringBuilder query = new IdcStringBuilder();
/* 309 */     for (int i = 0; i < nclauses; ++i)
/*     */     {
/* 311 */       Vector clause = (Vector)this.m_clauses.elementAt(i);
/* 312 */       createClauseString(clause, query);
/* 313 */       if (i >= nclauses - 1)
/*     */         continue;
/* 315 */       query.append(this.m_conjunction);
/*     */     }
/*     */ 
/* 319 */     return query.toString();
/*     */   }
/*     */ 
/*     */   public void createClauseString(Vector clause, IdcStringBuilder query) throws ServiceException
/*     */   {
/* 324 */     if (this.m_clauseDisplay == null)
/*     */     {
/* 326 */       String field = (String)clause.elementAt(0);
/* 327 */       String op = (String)clause.elementAt(1);
/* 328 */       String value = (String)clause.elementAt(2);
/*     */ 
/* 330 */       appendClause(field, query, op, value);
/*     */     }
/*     */     else
/*     */     {
/* 334 */       this.m_clauseDisplay.createClauseString(clause, query);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void appendClause(String field, IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 341 */     query.append(field);
/* 342 */     query.append(" ");
/* 343 */     appendOpAndValue(query, op, value);
/*     */   }
/*     */ 
/*     */   public void appendOpAndValue(IdcStringBuilder query, String op, String value)
/*     */     throws ServiceException
/*     */   {
/* 349 */     String dataOp = StringUtils.getPresentationString(this.m_operatorMap, op);
/* 350 */     query.append(dataOp);
/* 351 */     query.append(" ");
/* 352 */     value = prepareQueryValue(dataOp, value);
/* 353 */     query.append(value);
/*     */   }
/*     */ 
/*     */   public String prepareQueryValue(String dataOp, String value)
/*     */   {
/* 359 */     return value;
/*     */   }
/*     */ 
/*     */   public void setClauseDisplay(ClauseDisplay display, String conj)
/*     */   {
/* 364 */     this.m_clauseDisplay = display;
/* 365 */     this.m_conjunction = conj;
/*     */   }
/*     */ 
/*     */   public ClauseDisplay getClauseDisplay()
/*     */   {
/* 370 */     return this.m_clauseDisplay;
/*     */   }
/*     */ 
/*     */   public Vector createNameValue(String name, String value)
/*     */   {
/* 378 */     Vector nameValue = new IdcVector();
/* 379 */     nameValue.setSize(2);
/* 380 */     nameValue.setElementAt(name, 0);
/* 381 */     nameValue.setElementAt(value, 1);
/* 382 */     return nameValue;
/*     */   }
/*     */ 
/*     */   public void setNameValueFormatAt(Vector list, int index, String name, String value)
/*     */   {
/* 387 */     Vector nameValue = createNameValue(name, value);
/* 388 */     String nameValueStr = StringUtils.createString(nameValue, ' ', '%');
/* 389 */     list.setElementAt(nameValueStr, index);
/*     */   }
/*     */ 
/*     */   public String[][] getOperatorCodes()
/*     */   {
/* 395 */     return this.m_operatorCodes;
/*     */   }
/*     */ 
/*     */   public boolean find(int index, Vector newClause)
/*     */   {
/* 400 */     String newValue = (String)newClause.elementAt(index);
/*     */ 
/* 402 */     int numClauses = this.m_clauses.size();
/* 403 */     for (int i = 0; i < numClauses; ++i)
/*     */     {
/* 405 */       Vector clause = (Vector)this.m_clauses.elementAt(i);
/* 406 */       String value = (String)clause.elementAt(index);
/*     */ 
/* 408 */       if (value.equals(newValue))
/*     */       {
/* 410 */         return true;
/*     */       }
/*     */     }
/* 413 */     return false;
/*     */   }
/*     */ 
/*     */   public String findOperatorFromAlias(String str)
/*     */   {
/* 419 */     return str;
/*     */   }
/*     */ 
/*     */   public String getOperatorString(String str)
/*     */   {
/* 426 */     String val = findOperatorFromAlias(str);
/* 427 */     if (val == null)
/*     */     {
/* 429 */       val = str;
/*     */     }
/*     */ 
/* 432 */     if (this.m_legacyOpMap != null)
/*     */     {
/* 434 */       String val2 = StringUtils.getInternalString(this.m_legacyOpMap, val);
/* 435 */       if (val2 != null)
/*     */       {
/* 437 */         val = val2;
/*     */       }
/*     */     }
/* 440 */     return val;
/*     */   }
/*     */ 
/*     */   public String getPresentationOperatorString(String str)
/*     */   {
/* 445 */     String val = StringUtils.getPresentationString(this.m_operatorMap, str);
/* 446 */     if (val == null)
/*     */     {
/* 448 */       val = str;
/*     */     }
/* 450 */     return val;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 455 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.ClausesData
 * JD-Core Version:    0.5.4
 */