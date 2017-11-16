/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.Parameters;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.util.ArrayList;
/*     */ 
/*     */ public class QueryBatchData
/*     */ {
/*  29 */   public static int BATCH_SIZE_MAX = 1000;
/*     */   protected Statement m_statement;
/*     */   protected JdbcQueryDef m_qDef;
/*     */   protected ArrayList m_queries;
/*     */   protected JdbcWorkspace m_workspace;
/*     */   protected JdbcManager m_manager;
/*     */ 
/*     */   public QueryBatchData()
/*     */   {
/*  31 */     this.m_statement = null;
/*  32 */     this.m_qDef = null;
/*  33 */     this.m_queries = new ArrayList();
/*     */ 
/*  35 */     this.m_workspace = null;
/*  36 */     this.m_manager = null;
/*     */   }
/*     */ 
/*     */   public void init(JdbcWorkspace ws, JdbcManager manager) {
/*  40 */     this.m_workspace = ws;
/*  41 */     this.m_manager = manager;
/*     */   }
/*     */ 
/*     */   public void initPreparedBatch(JdbcQueryDef qDef) throws DataException
/*     */   {
/*     */     try
/*     */     {
/*  48 */       this.m_qDef = qDef;
/*  49 */       this.m_statement = qDef.m_statement;
/*     */     }
/*     */     finally
/*     */     {
/*  53 */       clearBatchFromStatement(this.m_qDef.m_statement);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Statement prepareExecution(JdbcConnection jCon) throws SQLException, DataException
/*     */   {
/*  59 */     if (this.m_statement == null)
/*     */     {
/*  61 */       this.m_statement = this.m_workspace.createStatement(jCon, this.m_qDef);
/*  62 */       boolean hasError = false;
/*     */       try
/*     */       {
/*  65 */         initStatement(this.m_statement);
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*     */       }
/*     */       finally
/*     */       {
/*     */         Statement stmt;
/*  74 */         if ((hasError) && (this.m_statement != null))
/*     */         {
/*  76 */           this.m_manager.debugMsg("Error preparing batch execution, reset statement");
/*  77 */           Statement stmt = this.m_statement;
/*  78 */           this.m_statement = null;
/*  79 */           stmt.clearBatch();
/*  80 */           stmt.close();
/*  81 */           if (this.m_qDef != null)
/*     */           {
/*  83 */             this.m_qDef.m_statement = null;
/*  84 */             this.m_manager.debugMsg("Error preparing batch execution, Nullify prepared statement.");
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*  89 */     return this.m_statement;
/*     */   }
/*     */ 
/*     */   protected void initStatement(Statement stmt) throws DataException
/*     */   {
/*  94 */     String[] queries = new String[this.m_queries.size()];
/*  95 */     this.m_queries.toArray(queries);
/*  96 */     addQueriesToStatement(stmt, queries);
/*     */   }
/*     */ 
/*     */   protected void addQueriesToStatement(Statement stmt, String[] queries) throws DataException
/*     */   {
/* 101 */     String query = null;
/*     */     try
/*     */     {
/* 104 */       for (int i = 0; i < queries.length; ++i)
/*     */       {
/* 106 */         query = queries[i];
/* 107 */         stmt.addBatch(query);
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 112 */       String msg = LocaleUtils.encodeMessage("csJdbcUnableToAddQueryToStatement", null, query);
/* 113 */       throw new DataException(msg, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public ArrayList getBatch()
/*     */   {
/* 119 */     return this.m_queries;
/*     */   }
/*     */ 
/*     */   public String addBatchSQL(String query) throws DataException
/*     */   {
/* 124 */     if (this.m_statement != null)
/*     */     {
/* 126 */       addQueriesToStatement(this.m_statement, new String[] { query });
/*     */     }
/* 128 */     this.m_queries.add(query);
/*     */ 
/* 130 */     return query;
/*     */   }
/*     */ 
/*     */   public String addBatch(JdbcQueryDef qDef, Parameters params) throws DataException
/*     */   {
/* 135 */     String query = qDef.m_name;
/* 136 */     if (qDef.m_isPrepared)
/*     */     {
/* 138 */       if (this.m_qDef == null)
/*     */       {
/* 140 */         initPreparedBatch(qDef);
/*     */       }
/* 142 */       else if (this.m_qDef != qDef)
/*     */       {
/* 144 */         String msg = LocaleUtils.encodeMessage("csJdbcConflictBatchQueryInsertionAttempted", null, this.m_qDef.m_name, qDef.m_name);
/* 145 */         throw new DataException(msg);
/*     */       }
/*     */ 
/* 148 */       JdbcQueryUtils.buildPreparedQuery(qDef, params, this.m_workspace, this.m_manager);
/* 149 */       int index = this.m_queries.size();
/*     */       try
/*     */       {
/* 152 */         ((PreparedStatement)this.m_statement).addBatch();
/* 153 */         this.m_queries.add(new Object[] { qDef.m_name, params });
/* 154 */         this.m_workspace.reportStartAction(null, "Added query at index " + index + ": " + qDef.m_name, true, false);
/* 155 */         this.m_manager.debugMsg("Query: " + qDef.m_query);
/*     */       }
/*     */       catch (SQLException e)
/*     */       {
/* 159 */         throw new DataException("csJdbcUnableToAddQueryToBatch", e);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 165 */       query = JdbcQueryUtils.buildQuery(qDef, params, this.m_workspace, this.m_manager);
/* 166 */       addBatchSQL(query);
/*     */     }
/* 168 */     return query;
/*     */   }
/*     */ 
/*     */   public void cleanBatch()
/*     */   {
/* 173 */     if (this.m_statement != null)
/*     */     {
/* 175 */       clearBatchFromStatement(this.m_statement);
/*     */     }
/* 177 */     this.m_queries.clear();
/* 178 */     this.m_qDef = null;
/* 179 */     this.m_statement = null;
/*     */   }
/*     */ 
/*     */   public int getSize()
/*     */   {
/* 184 */     return this.m_queries.size();
/*     */   }
/*     */ 
/*     */   protected void clearBatchFromStatement(Statement stmt)
/*     */   {
/* 189 */     if (this.m_workspace.isStatementClosed(stmt))
/*     */     {
/* 191 */       this.m_manager.debugMsg("clearBatch called on closed statement");
/* 192 */       return;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 197 */       stmt.clearBatch();
/*     */     }
/*     */     catch (Exception ignore)
/*     */     {
/* 202 */       this.m_manager.debugMsg("Unable to clear batch from statement: " + ignore.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 208 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98198 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.QueryBatchData
 * JD-Core Version:    0.5.4
 */