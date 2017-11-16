/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.CallableResults;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.QueryParameterInfo;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.InputStream;
/*     */ import java.io.Reader;
/*     */ import java.io.StringReader;
/*     */ import java.sql.Blob;
/*     */ import java.sql.CallableStatement;
/*     */ import java.sql.Clob;
/*     */ import java.sql.SQLException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcCallableResults
/*     */   implements CallableResults
/*     */ {
/*  32 */   protected JdbcManager m_manager = null;
/*  33 */   protected CallableStatement m_statement = null;
/*  34 */   protected JdbcConnection m_conn = null;
/*  35 */   protected String m_query = null;
/*  36 */   protected Hashtable m_infos = new Hashtable();
/*  37 */   protected JdbcResultSet m_resultSet = null;
/*  38 */   protected Vector m_registeredObj = new IdcVector();
/*     */   protected static final int BINARY_INPUT_STREAM = 1000;
/*     */   protected static final int CHARACTER_READER = 1001;
/*     */   protected static final int OTHER_OBJECT = 1002;
/*     */ 
/*     */   public JdbcCallableResults(JdbcManager manager)
/*     */   {
/*  46 */     this.m_manager = manager;
/*     */   }
/*     */ 
/*     */   public void init(String query, CallableStatement stmt, JdbcQueryDef qDef, JdbcConnection conn, boolean isRsetFirst)
/*     */     throws DataException
/*     */   {
/*  52 */     this.m_statement = stmt;
/*  53 */     this.m_conn = conn;
/*  54 */     this.m_query = query;
/*     */ 
/*  56 */     int size = qDef.m_parameters.size();
/*  57 */     for (int i = 0; i < size; ++i)
/*     */     {
/*  59 */       QueryParameterInfo info = (QueryParameterInfo)qDef.m_parameters.elementAt(i);
/*  60 */       this.m_infos.put(info.m_name, info);
/*     */     }
/*  62 */     if (!isRsetFirst)
/*     */       return;
/*     */     try
/*     */     {
/*  66 */       java.sql.ResultSet rset = stmt.getResultSet();
/*  67 */       this.m_resultSet = prepareResultSet(rset);
/*  68 */       registerObject(this.m_resultSet);
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/*  72 */       if (!SystemUtils.m_verbose)
/*     */         return;
/*  74 */       Report.debug("systemdatabase", null, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public String getString(String name)
/*     */     throws DataException
/*     */   {
/*  82 */     String result = (String)getData(name, 6);
/*  83 */     if (result == null)
/*     */     {
/*  85 */       result = "";
/*     */     }
/*  87 */     return result;
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String name) throws DataException
/*     */   {
/*  92 */     Boolean result = (Boolean)getData(name, 1);
/*  93 */     return result.booleanValue();
/*     */   }
/*     */ 
/*     */   public int getInteger(String name) throws DataException
/*     */   {
/*  98 */     Long result = (Long)getData(name, 3);
/*  99 */     return result.intValue();
/*     */   }
/*     */ 
/*     */   public long getLong(String name) throws DataException
/*     */   {
/* 104 */     Long result = (Long)getData(name, 3);
/* 105 */     return result.longValue();
/*     */   }
/*     */ 
/*     */   public InputStream getBinaryInputStream(String name) throws DataException
/*     */   {
/* 110 */     InputStream is = (InputStream)getData(name, 1000);
/* 111 */     registerObject(is);
/* 112 */     return is;
/*     */   }
/*     */ 
/*     */   public Reader getReader(String name) throws DataException
/*     */   {
/* 117 */     Reader reader = (Reader)getData(name, 1001);
/* 118 */     registerObject(reader);
/* 119 */     return reader;
/*     */   }
/*     */ 
/*     */   public Object getObject(String name) throws DataException
/*     */   {
/* 124 */     Object obj = getData(name, 1002);
/* 125 */     if (obj instanceof java.sql.ResultSet)
/*     */     {
/* 127 */       obj = prepareResultSet((java.sql.ResultSet)obj);
/*     */     }
/* 129 */     registerObject(obj);
/* 130 */     return obj;
/*     */   }
/*     */ 
/*     */   public intradoc.data.ResultSet getResultSet() throws DataException
/*     */   {
/* 135 */     intradoc.data.ResultSet rset = this.m_resultSet;
/* 136 */     if (rset == null)
/*     */     {
/* 138 */       rset = (intradoc.data.ResultSet)getData(null, -201);
/* 139 */       registerObject(rset);
/*     */     }
/*     */ 
/* 142 */     return rset;
/*     */   }
/*     */ 
/*     */   public Object next()
/*     */   {
/* 147 */     Object obj = this.m_resultSet;
/*     */     try
/*     */     {
/* 150 */       if (obj == null)
/*     */       {
/* 152 */         this.m_resultSet = null;
/* 153 */         if (this.m_statement.getMoreResults())
/*     */         {
/* 155 */           obj = getData(null, -201);
/* 156 */           obj = prepareResultSet((java.sql.ResultSet)obj);
/* 157 */           registerObject(obj);
/*     */         }
/*     */         else
/*     */         {
/* 161 */           int i = this.m_statement.getUpdateCount();
/* 162 */           obj = new Integer(i);
/*     */         }
/*     */ 
/*     */       }
/*     */       else
/*     */       {
/* 168 */         this.m_resultSet = null;
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 173 */       this.m_manager.debugMsg(e.getMessage());
/* 174 */       return new Integer(-1);
/*     */     }
/*     */     catch (DataException e)
/*     */     {
/* 178 */       this.m_manager.debugMsg(e.getMessage());
/* 179 */       return new Integer(-1);
/*     */     }
/* 181 */     return obj;
/*     */   }
/*     */ 
/*     */   protected QueryParameterInfo getParameterInfo(String name) throws DataException
/*     */   {
/* 186 */     QueryParameterInfo info = (QueryParameterInfo)this.m_infos.get(name);
/* 187 */     if (info == null)
/*     */     {
/* 189 */       String msg = LocaleUtils.encodeMessage("csUnableToFindOutputParameter", null, name);
/* 190 */       throw new DataException(msg);
/*     */     }
/* 192 */     return info;
/*     */   }
/*     */ 
/*     */   protected Object getData(String name, int type) throws DataException
/*     */   {
/* 197 */     QueryParameterInfo info = null;
/* 198 */     if (name != null)
/*     */     {
/* 200 */       info = getParameterInfo(name);
/*     */     }
/* 202 */     Object result = null;
/*     */     try
/*     */     {
/* 205 */       switch (type)
/*     */       {
/*     */       case 3:
/* 209 */         long value = this.m_statement.getLong(info.m_index);
/* 210 */         result = new Long(value);
/* 211 */         break;
/*     */       case 1:
/* 215 */         boolean value = this.m_statement.getBoolean(info.m_index);
/* 216 */         result = new Boolean(value);
/* 217 */         break;
/*     */       case -201:
/* 221 */         result = this.m_statement.getResultSet();
/* 222 */         if (result instanceof java.sql.ResultSet)
/*     */         {
/* 224 */           JdbcResultSet rset = new JdbcResultSet(this.m_manager);
/* 225 */           rset.setQueryInfo(this.m_statement, this.m_query, this.m_conn, (java.sql.ResultSet)result);
/*     */         }
/* 227 */         else if (result != null)
/*     */         {
/* 230 */           this.m_manager.debugMsg("Tried to get resultset on parameter '" + name + "'returns non-resultset object");
/*     */ 
/* 232 */           result = null; } break;
/*     */       case 1000:
/* 238 */         if (info.m_type == 9)
/*     */         {
/* 240 */           Blob blob = this.m_statement.getBlob(info.m_index);
/* 241 */           result = blob.getBinaryStream();
/*     */         }
/*     */         else
/*     */         {
/* 245 */           byte[] bytes = this.m_statement.getBytes(info.m_index);
/* 246 */           if (bytes == null)
/*     */           {
/* 248 */             bytes = new byte[0];
/*     */           }
/* 250 */           result = new ByteArrayInputStream(bytes);
/*     */         }
/* 252 */         break;
/*     */       case 1001:
/* 256 */         if (info.m_type == 10)
/*     */         {
/* 258 */           Clob clob = this.m_statement.getClob(info.m_index);
/* 259 */           result = clob.getCharacterStream();
/*     */         }
/*     */         else
/*     */         {
/* 263 */           String str = this.m_statement.getString(info.m_index);
/* 264 */           if (str == null)
/*     */           {
/* 266 */             str = "";
/*     */           }
/* 268 */           result = new StringReader(str);
/*     */         }
/* 270 */         break;
/*     */       case 1002:
/* 274 */         result = this.m_statement.getObject(info.m_index);
/* 275 */         break;
/*     */       default:
/* 279 */         result = this.m_statement.getString(info.m_index);
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 285 */       DataException de = new DataException(e, "csJdbcCallableUnableRetrieveData", new Object[] { name });
/* 286 */       throw de;
/*     */     }
/*     */ 
/* 290 */     return result;
/*     */   }
/*     */ 
/*     */   public void registerObject(Object obj)
/*     */   {
/* 295 */     if (this.m_registeredObj.contains(obj))
/*     */       return;
/* 297 */     this.m_registeredObj.addElement(obj);
/*     */   }
/*     */ 
/*     */   public void close()
/*     */   {
/* 303 */     int index = this.m_registeredObj.size();
/* 304 */     for (; index > 0; --index)
/*     */     {
/*     */       try
/*     */       {
/* 308 */         Object obj = this.m_registeredObj.elementAt(index - 1);
/* 309 */         if (obj instanceof InputStream)
/*     */         {
/* 311 */           ((InputStream)obj).close();
/*     */         }
/* 313 */         else if (obj instanceof Reader)
/*     */         {
/* 315 */           ((Reader)obj).close();
/*     */         }
/* 317 */         else if (obj instanceof intradoc.data.ResultSet)
/*     */         {
/* 319 */           ((intradoc.data.ResultSet)obj).closeInternals();
/*     */         }
/* 321 */         this.m_registeredObj.remove(index - 1);
/*     */       }
/*     */       catch (Throwable t)
/*     */       {
/* 325 */         this.m_manager.debugMsg("Error cleaning up callable results " + t.getMessage());
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected JdbcResultSet prepareResultSet(java.sql.ResultSet jrset)
/*     */   {
/* 332 */     if (jrset == null)
/*     */     {
/* 334 */       return null;
/*     */     }
/* 336 */     JdbcResultSet rset = new JdbcResultSet(this.m_manager);
/* 337 */     rset.setQueryInfo(this.m_statement, this.m_query, this.m_conn, jrset);
/* 338 */     return rset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 343 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 80661 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcCallableResults
 * JD-Core Version:    0.5.4
 */