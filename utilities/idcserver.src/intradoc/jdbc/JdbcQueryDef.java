/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.QueryParameterInfo;
/*     */ import intradoc.data.QueryUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.sql.Connection;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.SQLException;
/*     */ import java.util.StringTokenizer;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcQueryDef
/*     */ {
/*  34 */   public static int PREPARED = 1;
/*  35 */   public static int CALLABLE = 17;
/*     */   public String m_name;
/*     */   public String m_query;
/*     */   public String m_parametersSource;
/*  41 */   public Vector m_parameters = null;
/*  42 */   public boolean m_isPrepared = false;
/*  43 */   public boolean m_isCallable = false;
/*  44 */   public PreparedStatement m_statement = null;
/*     */ 
/*     */   public JdbcQueryDef(String name, String query, boolean isPrepared)
/*     */   {
/*  48 */     this(name, query, isPrepared, false);
/*     */   }
/*     */ 
/*     */   public JdbcQueryDef(String name, String query, boolean isPrepared, boolean isCallable)
/*     */   {
/*  53 */     this.m_name = name;
/*  54 */     this.m_query = query;
/*  55 */     this.m_isPrepared = ((isPrepared) || (isCallable));
/*  56 */     this.m_isCallable = isCallable;
/*  57 */     this.m_parameters = new IdcVector();
/*  58 */     this.m_statement = null;
/*     */   }
/*     */ 
/*     */   public void init(Connection con) throws DataException
/*     */   {
/*     */     try
/*     */     {
/*  65 */       if (this.m_isCallable)
/*     */       {
/*  67 */         this.m_statement = con.prepareCall(this.m_query);
/*     */       }
/*  69 */       else if (this.m_isPrepared)
/*     */       {
/*  71 */         this.m_statement = con.prepareStatement(this.m_query);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*  77 */       Report.trace(null, null, e);
/*  78 */       String msg = LocaleUtils.encodeMessage("csJdbcUnableToPrepareQuery", e.getMessage(), this.m_query);
/*     */ 
/*  80 */       throw new DataException(msg);
/*     */     }
/*     */   }
/*     */ 
/*     */   public JdbcQueryDef copy(Connection con) throws DataException
/*     */   {
/*  86 */     JdbcQueryDef qDef = new JdbcQueryDef(this.m_name, this.m_query, this.m_isPrepared, this.m_isCallable);
/*     */ 
/*  88 */     int size = this.m_parameters.size();
/*  89 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  91 */       qDef.m_parameters.addElement(this.m_parameters.elementAt(i));
/*     */     }
/*     */ 
/*  94 */     if (this.m_isPrepared)
/*     */     {
/*  96 */       qDef.init(con);
/*     */     }
/*  98 */     qDef.m_parametersSource = this.m_parametersSource;
/*  99 */     return qDef;
/*     */   }
/*     */ 
/*     */   public void addParam(String param, int type)
/*     */   {
/* 104 */     addParamEx(param, type, false, true, false);
/*     */   }
/*     */ 
/*     */   public void addParamEx(String param, int type, boolean isOutput, boolean isInput, boolean isListValue)
/*     */   {
/* 109 */     QueryParameterInfo info = new QueryParameterInfo();
/* 110 */     String name = param;
/* 111 */     String defaultValue = null;
/* 112 */     int index = param.indexOf(':');
/* 113 */     if (index != -1)
/*     */     {
/* 115 */       name = param.substring(0, index);
/* 116 */       if (index < param.length() - 1)
/*     */       {
/* 118 */         defaultValue = param.substring(index + 1);
/*     */       }
/*     */       else
/*     */       {
/* 122 */         defaultValue = "";
/*     */       }
/*     */     }
/* 125 */     info.m_name = name;
/* 126 */     info.m_alternateName = getAlternateName(name);
/* 127 */     info.m_type = type;
/* 128 */     info.m_default = defaultValue;
/* 129 */     info.m_index = (this.m_parameters.size() + 1);
/* 130 */     info.m_isOutput = isOutput;
/* 131 */     info.m_isInput = isInput;
/* 132 */     info.m_isList = isListValue;
/* 133 */     this.m_parameters.addElement(info);
/*     */   }
/*     */ 
/*     */   public void addParam(String param, String typeStr)
/*     */   {
/* 138 */     int type = QueryUtils.convertInfoStringToType(typeStr);
/*     */ 
/* 140 */     boolean isOutput = false;
/* 141 */     boolean isInput = true;
/* 142 */     String tmp = typeStr.toLowerCase();
/* 143 */     if (tmp.indexOf("out:") >= 0)
/*     */     {
/* 145 */       isOutput = true;
/* 146 */       if (!tmp.startsWith("inout:"))
/*     */       {
/* 148 */         isInput = false;
/*     */       }
/*     */     }
/* 151 */     boolean hasListValue = QueryUtils.hasListAsValue(typeStr);
/* 152 */     addParamEx(param, type, isOutput, isInput, hasListValue);
/*     */   }
/*     */ 
/*     */   public void parseAndAddParams(String params)
/*     */   {
/* 158 */     this.m_parametersSource = params;
/* 159 */     if ((params == null) || (params.length() == 0))
/*     */     {
/* 161 */       return;
/*     */     }
/*     */ 
/* 164 */     String name = null;
/* 165 */     String type = null;
/* 166 */     StringTokenizer tknz = new StringTokenizer(params, "\n");
/* 167 */     while (tknz.hasMoreElements())
/*     */     {
/* 169 */       String token = tknz.nextToken().trim();
/*     */ 
/* 171 */       int index = token.lastIndexOf(' ');
/* 172 */       if (index >= 0)
/*     */       {
/* 174 */         name = token.substring(0, index).trim();
/* 175 */         type = token.substring(index + 1).trim();
/*     */       }
/*     */       else
/*     */       {
/* 179 */         name = token;
/* 180 */         type = "varchar";
/*     */       }
/* 182 */       if ((name != null) && (name.length() > 0))
/*     */       {
/* 184 */         addParam(name, type);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getAlternateName(String name)
/*     */   {
/* 191 */     if ((name == null) || (name.length() < 3) || (!name.startsWith("qv")))
/*     */     {
/* 193 */       return null;
/*     */     }
/*     */ 
/* 196 */     return "QV" + name.substring(2);
/*     */   }
/*     */ 
/*     */   public boolean isEquivalentQuery(JdbcQueryDef otherQuery)
/*     */   {
/* 201 */     return (compareStringsAllowNull(otherQuery.m_name, this.m_name)) && (compareStringsAllowNull(otherQuery.m_query, this.m_query)) && (compareStringsAllowNull(otherQuery.m_parametersSource, this.m_parametersSource)) && (otherQuery.m_isCallable == this.m_isCallable) && (otherQuery.m_isPrepared == this.m_isPrepared);
/*     */   }
/*     */ 
/*     */   public boolean compareStringsAllowNull(String str1, String str2)
/*     */   {
/* 210 */     if (str1 == null)
/*     */     {
/* 212 */       return str2 == null;
/*     */     }
/*     */ 
/* 215 */     if (str2 == null)
/*     */     {
/* 217 */       return false;
/*     */     }
/* 219 */     return str1.equals(str2);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 224 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 73513 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcQueryDef
 * JD-Core Version:    0.5.4
 */