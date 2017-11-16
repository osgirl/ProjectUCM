/*     */ package intradoc.shared;
/*     */ 
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetFilter;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class AliasData extends DataResultSet
/*     */ {
/*  32 */   int m_aliasIndex = -1;
/*  33 */   int m_descriptionIndex = -1;
/*     */ 
/*  35 */   protected Hashtable m_aliasTable = null;
/*     */ 
/*  38 */   public static String m_tableName = "Alias";
/*  39 */   public static String m_aliasUserTableName = "AliasUserMap";
/*     */   protected String m_curTable;
/*  45 */   protected DataResultSet m_aliasUserSet = null;
/*  46 */   protected Hashtable m_aliasUserMap = null;
/*  47 */   protected Hashtable m_userAliasMap = null;
/*     */ 
/*     */   public AliasData()
/*     */   {
/*  51 */     this.m_curTable = m_tableName;
/*  52 */     this.m_aliasTable = new Hashtable();
/*  53 */     this.m_aliasUserMap = new Hashtable();
/*  54 */     this.m_userAliasMap = new Hashtable();
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/*  60 */     DataResultSet rset = new AliasData();
/*  61 */     initShallow(rset);
/*     */ 
/*  63 */     return rset;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet rset)
/*     */   {
/*  69 */     super.initShallow(rset);
/*  70 */     AliasData data = (AliasData)rset;
/*  71 */     data.m_aliasTable = this.m_aliasTable;
/*  72 */     data.m_aliasIndex = this.m_aliasIndex;
/*  73 */     data.m_descriptionIndex = this.m_descriptionIndex;
/*  74 */     data.m_curTable = this.m_curTable;
/*  75 */     data.m_aliasUserSet = this.m_aliasUserSet;
/*  76 */     data.m_aliasUserMap = this.m_aliasUserMap;
/*  77 */     data.m_userAliasMap = this.m_userAliasMap;
/*     */   }
/*     */ 
/*     */   public void loadAliases(ResultSet rSet) throws DataException
/*     */   {
/*  82 */     this.m_curTable = m_tableName;
/*     */ 
/*  84 */     if (rSet == null)
/*     */     {
/*  86 */       throw new DataException("!apNoAliasInfo");
/*     */     }
/*  88 */     copy(rSet);
/*     */ 
/*  90 */     this.m_aliasTable = new Hashtable();
/*     */ 
/*  93 */     this.m_aliasIndex = ResultSetUtils.getIndexMustExist(rSet, "dAlias");
/*  94 */     this.m_descriptionIndex = ResultSetUtils.getIndexMustExist(rSet, "dAliasDescription");
/*     */ 
/*  97 */     for (; isRowPresent(); next())
/*     */     {
/*  99 */       String key = getStringValue(this.m_aliasIndex);
/* 100 */       this.m_aliasTable.put(key.toLowerCase().trim(), this.m_values.get(this.m_currentRow));
/*     */     }
/*     */   }
/*     */ 
/*     */   public void loadUsers(ResultSet rset) throws DataException
/*     */   {
/* 106 */     if (rset == null)
/*     */     {
/* 108 */       throw new DataException("!apNoUserInfo");
/*     */     }
/*     */ 
/* 111 */     this.m_aliasUserSet = new DataResultSet();
/* 112 */     this.m_aliasUserSet.copy(rset);
/* 113 */     SharedObjects.putTable(m_aliasUserTableName, this.m_aliasUserSet);
/*     */ 
/* 116 */     this.m_aliasUserMap.clear();
/* 117 */     this.m_userAliasMap.clear();
/*     */   }
/*     */ 
/*     */   public String[][] getUsers(String aliasName)
/*     */     throws DataException
/*     */   {
/* 123 */     String[][] users = (String[][])null;
/* 124 */     String[] keys = { "dAlias", "dUserName" };
/* 125 */     DataResultSet rset = (DataResultSet)this.m_aliasUserMap.get(aliasName);
/* 126 */     if (rset == null)
/*     */     {
/* 129 */       rset = this.m_aliasUserSet.shallowClone();
/*     */     }
/* 131 */     users = ResultSetUtils.createFilteredStringTable(rset, keys, aliasName);
/*     */ 
/* 133 */     return users;
/*     */   }
/*     */ 
/*     */   public DataResultSet getUserSet(String aliasName)
/*     */     throws DataException
/*     */   {
/* 139 */     DataResultSet rset = (DataResultSet)this.m_aliasUserMap.get(aliasName);
/* 140 */     if (rset == null)
/*     */     {
/* 142 */       DataResultSet dataSet = this.m_aliasUserSet.shallowClone();
/*     */ 
/* 144 */       String name = aliasName;
/* 145 */       ResultSetFilter filter = new ResultSetFilter(name)
/*     */       {
/*     */         public int checkRow(String val, int curNumRows, Vector row)
/*     */         {
/* 149 */           if (val.equals(this.val$name))
/*     */           {
/* 151 */             return 1;
/*     */           }
/* 153 */           return 0;
/*     */         }
/*     */       };
/* 157 */       rset = new DataResultSet();
/* 158 */       rset.copyFiltered(dataSet, "dAlias", filter);
/*     */ 
/* 160 */       this.m_aliasUserMap.put(aliasName, rset);
/*     */     }
/*     */     else
/*     */     {
/* 164 */       rset = rset.shallowClone();
/*     */     }
/*     */ 
/* 167 */     return rset;
/*     */   }
/*     */ 
/*     */   public Vector getUserList(String aliasName) throws DataException
/*     */   {
/* 172 */     DataResultSet users = getUserSet(aliasName);
/*     */ 
/* 174 */     int index = ResultSetUtils.getIndexMustExist(users, "dUserName");
/* 175 */     Vector userList = new IdcVector();
/* 176 */     for (users.first(); users.isRowPresent(); users.next())
/*     */     {
/* 178 */       userList.addElement(users.getStringValue(index));
/*     */     }
/*     */ 
/* 181 */     return userList;
/*     */   }
/*     */ 
/*     */   public String[][] getAliasesForUser(String userName) throws DataException
/*     */   {
/* 186 */     String[][] aliases = (String[][])(String[][])this.m_userAliasMap.get(userName);
/* 187 */     if (aliases == null)
/*     */     {
/* 190 */       String[] keys = { "dUserName", "dAlias" };
/*     */ 
/* 192 */       DataResultSet drset = this.m_aliasUserSet.shallowClone();
/* 193 */       aliases = ResultSetUtils.createFilteredStringTable(drset, keys, userName);
/*     */ 
/* 195 */       this.m_userAliasMap.put(userName, aliases);
/*     */     }
/*     */ 
/* 198 */     return aliases;
/*     */   }
/*     */ 
/*     */   public String getTableName()
/*     */   {
/* 203 */     return m_tableName;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 208 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99807 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.AliasData
 * JD-Core Version:    0.5.4
 */