/*     */ package intradoc.server;
/*     */ 
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import java.io.IOException;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ 
/*     */ public class ListBoxService extends Service
/*     */ {
/*     */   public static final String OP_EQ = "eq";
/*     */   public static final String OP_GE = "ge";
/*     */   public static final String OP_LE = "le";
/*     */   public static final String OP_GT = "gt";
/*     */   public static final String OP_LT = "lt";
/*     */   public static final String OP_NEAR = "near";
/*     */   public static final String OP_INSERT = "insert";
/*     */   public static final String OP_DELETE = "delete";
/*     */   public static final String OP_UPDATE = "update";
/*     */   public static final String OP_ALL = "all";
/*     */   public static final String OP_EXTREMES = "extremes";
/*     */   public static final String OP_CACHED = "cached";
/*     */   public static final String OP_ASCENDING = "asc";
/*     */   public static final String OP_DESCENDING = "desc";
/*  48 */   private static Hashtable m_sqlRelOps = null;
/*     */ 
/*     */   public void createHandlersForService()
/*     */     throws ServiceException, DataException
/*     */   {
/*  59 */     super.createHandlersForService();
/*  60 */     createHandlers("ListBoxService");
/*     */   }
/*     */ 
/*     */   protected static String lookupOp(String key)
/*     */   {
/*  65 */     if (m_sqlRelOps == null)
/*     */     {
/*  67 */       m_sqlRelOps = new Hashtable();
/*     */ 
/*  69 */       m_sqlRelOps.put("eq", "=");
/*  70 */       m_sqlRelOps.put("ge", ">=");
/*  71 */       m_sqlRelOps.put("le", "<=");
/*  72 */       m_sqlRelOps.put("gt", ">");
/*  73 */       m_sqlRelOps.put("lt", "<");
/*  74 */       m_sqlRelOps.put("near", "");
/*  75 */       m_sqlRelOps.put("insert", "");
/*  76 */       m_sqlRelOps.put("delete", "");
/*  77 */       m_sqlRelOps.put("update", "");
/*  78 */       m_sqlRelOps.put("all", "");
/*  79 */       m_sqlRelOps.put("extremes", "");
/*  80 */       m_sqlRelOps.put("cached", "");
/*  81 */       m_sqlRelOps.put("desc", "DESC");
/*  82 */       m_sqlRelOps.put("asc", "ASC");
/*     */     }
/*     */ 
/*  85 */     return (String)m_sqlRelOps.get(key);
/*     */   }
/*     */ 
/*     */   @IdcServiceAction
/*     */   public void downloadItems()
/*     */     throws DataException, ServiceException
/*     */   {
/*  96 */     String dataSource = this.m_binder.getLocal("dataSource");
/*  97 */     if (dataSource == null)
/*     */     {
/*  99 */       throw new DataException("!csListBoxServiceDataSourceMissing");
/*     */     }
/*     */ 
/* 102 */     String op = this.m_binder.getLocal("op");
/* 103 */     if (op == null)
/*     */     {
/* 105 */       op = "eq";
/*     */     }
/* 109 */     else if (lookupOp(op) == null)
/*     */     {
/* 111 */       String msg = LocaleUtils.encodeMessage("csListBoxServiceIllegalOp", null, op);
/*     */ 
/* 113 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 117 */     String listValue = this.m_binder.getLocal("listValue");
/* 118 */     if ((!op.equals("all")) && (listValue == null))
/*     */     {
/* 120 */       throw new DataException("!csListBoxNullValue");
/*     */     }
/*     */ 
/* 123 */     int limit = NumberUtils.parseInteger(this.m_binder.getLocal("limit"), 100);
/*     */ 
/* 126 */     DataResultSet drset = null;
/* 127 */     String cacheName = this.m_binder.getLocal("cacheName");
/* 128 */     if (cacheName != null)
/*     */     {
/* 130 */       drset = SharedObjects.getTable(cacheName);
/*     */     }
/* 132 */     if (drset == null)
/*     */     {
/* 134 */       loadData(dataSource, op, listValue, limit);
/*     */     }
/*     */     else
/*     */     {
/* 138 */       this.m_binder.addResultSet("ListBoxServiceItems", drset);
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void loadData(String dataSource, String op, String listValue, int limit)
/*     */     throws DataException, ServiceException
/*     */   {
/* 146 */     DataResultSet rset = new DataResultSet(new String[] { "junk" });
/*     */     try
/*     */     {
/* 151 */       DataUtils.lookupSQL(dataSource);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 155 */       String msg = LocaleUtils.encodeMessage("csListBoxDataSourceNotDefined", null, dataSource);
/*     */ 
/* 157 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 162 */     if ((op.equals("delete")) || (op.equals("insert")) || (op.equals("update")))
/*     */     {
/* 167 */       DataResultSet querySet = SharedObjects.getTable("ListBoxServiceQueries");
/* 168 */       querySet.first();
/*     */ 
/* 170 */       while ((!querySet.getStringValue(0).equals(dataSource)) && (querySet.next()));
/*     */       String queryName;
/*     */       String queryName;
/* 176 */       if (op.equals("insert"))
/*     */       {
/* 178 */         queryName = querySet.getStringValue(1);
/*     */       }
/*     */       else
/*     */       {
/*     */         String queryName;
/* 180 */         if (op.equals("update"))
/*     */         {
/* 182 */           queryName = querySet.getStringValue(2);
/*     */         }
/*     */         else
/*     */         {
/* 186 */           queryName = querySet.getStringValue(3);
/*     */         }
/*     */       }
/*     */ 
/* 190 */       this.m_workspace.execute(queryName, this.m_binder);
/*     */ 
/* 193 */       String postOp = this.m_binder.getLocal("postOp");
/* 194 */       if (postOp == null)
/*     */       {
/* 196 */         op = "near";
/* 197 */         limit = 4;
/*     */       }
/*     */       else
/*     */       {
/* 201 */         op = postOp;
/* 202 */         if (lookupOp(op) == null)
/*     */         {
/* 204 */           String msg = LocaleUtils.encodeMessage("csListBoxServiceIllegalOp", null, op);
/*     */ 
/* 206 */           throw new DataException(msg);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 211 */     if (op.equals("extremes"))
/*     */     {
/* 213 */       DataResultSet rset2 = new DataResultSet();
/* 214 */       doQuery(rset2, dataSource, "desc", null, limit / 2);
/* 215 */       this.m_binder.addResultSet("ListBoxServiceItems2", rset2);
/* 216 */       doQuery(rset, dataSource, "asc", null, limit / 2);
/*     */     }
/* 218 */     else if (op.equals("near"))
/*     */     {
/* 220 */       doQuery(rset, dataSource, "lt", listValue, limit / 2);
/*     */ 
/* 222 */       DataResultSet near = new DataResultSet();
/* 223 */       doQuery(near, dataSource, "ge", listValue, limit / 2);
/*     */ 
/* 226 */       for (near.first(); near.isRowPresent(); near.next())
/*     */       {
/* 228 */         rset.addRow(near.getCurrentRowValues());
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 233 */       doQuery(rset, dataSource, op, listValue, limit);
/*     */     }
/*     */ 
/* 236 */     this.m_binder.addResultSet("ListBoxServiceItems", rset);
/*     */   }
/*     */ 
/*     */   protected void doQuery(DataResultSet dset, String dataSource, String op, String listValue, int limit)
/*     */     throws DataException, ServiceException
/*     */   {
/* 242 */     String relOp = lookupOp(op);
/* 243 */     String orderDirection = "";
/* 244 */     if (relOp.indexOf("<") >= 0)
/*     */     {
/* 246 */       orderDirection = "DESC";
/*     */     }
/* 248 */     else if (relOp.indexOf(">") >= 0)
/*     */     {
/* 250 */       orderDirection = "ASC";
/*     */     }
/* 252 */     else if ((relOp.equalsIgnoreCase("DESC")) || (relOp.equalsIgnoreCase("ASC")))
/*     */     {
/* 255 */       orderDirection = relOp;
/* 256 */       relOp = "";
/*     */     }
/*     */ 
/* 259 */     if (op.equals("all"))
/*     */     {
/* 261 */       limit = 0;
/*     */     }
/*     */ 
/* 265 */     if ((relOp.length() == 0) || (listValue == null))
/*     */     {
/* 267 */       relOp = "is not";
/* 268 */       listValue = "";
/*     */     }
/*     */ 
/* 271 */     Properties oldProps = this.m_binder.getLocalData();
/* 272 */     Properties newProps = new Properties(oldProps);
/* 273 */     this.m_binder.setLocalData(newProps);
/* 274 */     this.m_binder.putLocal("relOp", relOp);
/* 275 */     this.m_binder.putLocal("listValue", listValue);
/* 276 */     this.m_binder.putLocal("orderDirection", orderDirection);
/* 277 */     this.m_binder.putLocal("MaxQueryRows", Integer.toString(limit));
/*     */ 
/* 279 */     String orderClause = this.m_binder.getLocal("orderClause");
/* 280 */     if (orderClause != null)
/*     */     {
/*     */       try
/*     */       {
/* 284 */         orderClause = this.m_pageMerger.evaluateScript(orderClause);
/*     */       }
/*     */       catch (IOException e)
/*     */       {
/* 288 */         createServiceException(e, "");
/*     */       }
/*     */ 
/* 291 */       this.m_binder.putLocal("orderClause", orderClause);
/*     */     }
/*     */ 
/* 294 */     this.m_binder.putLocal("resultName", "ListBoxResultSet");
/*     */ 
/* 296 */     doCode("createResultSetSQL");
/*     */ 
/* 298 */     ResultSet rset = this.m_binder.getResultSet("ListBoxResultSet");
/*     */ 
/* 301 */     dset.copy(rset, limit);
/*     */ 
/* 303 */     this.m_binder.removeResultSet("ListBoxResultSet");
/*     */ 
/* 305 */     rset.closeInternals();
/*     */ 
/* 308 */     this.m_binder.setLocalData(oldProps);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 313 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 70705 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.ListBoxService
 * JD-Core Version:    0.5.4
 */