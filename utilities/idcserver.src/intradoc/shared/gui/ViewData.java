/*     */ package intradoc.shared.gui;
/*     */ 
/*     */ import intradoc.shared.SharedObjects;
/*     */ 
/*     */ public class ViewData
/*     */ {
/*     */   public static final int DEFAULT_VIEW = 0;
/*     */   public static final int DOC_VIEW = 1;
/*     */   public static final int USER_VIEW = 2;
/*     */   public static final int TABLE_VIEW = 3;
/*     */   public static final int SCHEMA_VIEW = 4;
/*  34 */   public int m_viewType = 1;
/*     */ 
/*  36 */   public boolean m_isViewOnly = true;
/*  37 */   public boolean m_isMultipleMode = false;
/*     */ 
/*  39 */   public boolean m_useInDate = true;
/*  40 */   public boolean m_inDateState = true;
/*  41 */   public boolean m_useFilter = false;
/*     */ 
/*  44 */   public String m_viewName = "";
/*     */ 
/*  46 */   public int m_maxRows = 0;
/*     */ 
/*  49 */   public String m_listTitle = null;
/*  50 */   public String m_dataSource = null;
/*  51 */   public String m_tableName = null;
/*  52 */   public String m_colNames = null;
/*  53 */   public String m_orderClause = null;
/*  54 */   public String m_action = "GET_DATARESULTSET";
/*     */ 
/*  56 */   public String m_idColumn = null;
/*  57 */   public String m_iconColumn = null;
/*     */ 
/*  59 */   public String m_msg = "";
/*     */ 
/*  62 */   public boolean m_hasExtraWhereClause = false;
/*  63 */   public String m_extraWhereClause = null;
/*  64 */   public String m_extraLabel = null;
/*     */ 
/*     */   public ViewData(int type)
/*     */   {
/*  68 */     this.m_viewType = type;
/*  69 */     switch (type)
/*     */     {
/*     */     case 1:
/*  72 */       initDocViewData("Documents", "DOCUMENTS");
/*  73 */       break;
/*     */     case 2:
/*  76 */       initUserViewData("Users", "UserList");
/*  77 */       break;
/*     */     case 3:
/*  80 */       throw new IllegalArgumentException("TableView Called without table name");
/*     */     case 4:
/*  83 */       initSchemaViewData(null, null);
/*     */     }
/*     */   }
/*     */ 
/*     */   public ViewData(int type, String dataSource, String tableName)
/*     */   {
/*  90 */     this.m_viewType = type;
/*  91 */     switch (type)
/*     */     {
/*     */     case 1:
/*  94 */       initDocViewData(dataSource, tableName);
/*  95 */       break;
/*     */     case 2:
/*  98 */       initUserViewData(dataSource, tableName);
/*  99 */       break;
/*     */     case 3:
/* 102 */       initTableViewData(dataSource, tableName);
/* 103 */       break;
/*     */     case 4:
/* 106 */       initSchemaViewData(dataSource, tableName);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void initDocViewData(String dataSource, String tableName)
/*     */   {
/* 113 */     this.m_dataSource = dataSource;
/* 114 */     this.m_tableName = tableName;
/*     */ 
/* 116 */     this.m_listTitle = "apTitleContent";
/* 117 */     this.m_idColumn = "dID";
/* 118 */     this.m_iconColumn = "dDocName";
/* 119 */     this.m_orderClause = "ORDER by Revisions.dDocName";
/* 120 */     this.m_msg = "content";
/* 121 */     this.m_useInDate = true;
/* 122 */     this.m_viewName = "DocView";
/*     */   }
/*     */ 
/*     */   public void initUserViewData(String dataSource, String tableName)
/*     */   {
/* 127 */     this.m_dataSource = dataSource;
/* 128 */     this.m_tableName = tableName;
/*     */ 
/* 130 */     this.m_listTitle = "apTitleUsers";
/* 131 */     this.m_idColumn = "dName";
/* 132 */     this.m_maxRows = SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500);
/* 133 */     this.m_orderClause = "ORDER by dName";
/* 134 */     this.m_msg = "user";
/* 135 */     this.m_useInDate = false;
/* 136 */     this.m_useFilter = true;
/* 137 */     this.m_viewName = "UserView";
/*     */   }
/*     */ 
/*     */   public void initTableViewData(String dataSource, String tableName)
/*     */   {
/* 142 */     this.m_dataSource = dataSource;
/* 143 */     this.m_tableName = tableName;
/*     */ 
/* 145 */     this.m_listTitle = "apTitleTables";
/* 146 */     this.m_maxRows = SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500);
/* 147 */     this.m_viewName = "TableView";
/* 148 */     this.m_msg = "table";
/* 149 */     this.m_useInDate = false;
/* 150 */     this.m_action = "GET_ARCHIVETABLECONTENT";
/*     */   }
/*     */ 
/*     */   public void initSchemaViewData(String dataSource, String tableName)
/*     */   {
/* 155 */     this.m_dataSource = dataSource;
/* 156 */     this.m_tableName = tableName;
/*     */ 
/* 158 */     this.m_listTitle = "apTitleViewValues";
/* 159 */     this.m_maxRows = SharedObjects.getEnvironmentInt("MaxStandardDatabaseResults", 500);
/* 160 */     this.m_viewName = "SchemaView";
/* 161 */     this.m_msg = "schema";
/* 162 */     this.m_useInDate = false;
/* 163 */     this.m_action = "GET_SCHEMA_VIEW_EDIT_INFO";
/*     */   }
/*     */ 
/*     */   public void initSelectView(String filterName)
/*     */   {
/* 168 */     this.m_viewName = filterName;
/* 169 */     this.m_inDateState = false;
/*     */   }
/*     */ 
/*     */   public void setExtraWhereClause(String whereClause, String label)
/*     */   {
/* 174 */     this.m_hasExtraWhereClause = true;
/* 175 */     this.m_extraWhereClause = whereClause;
/* 176 */     this.m_extraLabel = label;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 181 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 79291 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.gui.ViewData
 * JD-Core Version:    0.5.4
 */