/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class WorkspaceUtils
/*     */ {
/*     */   public static final String m_ebrSuffix = "_";
/*  27 */   static boolean m_ebrMode = false;
/*  28 */   static Workspace m_workspace = null;
/*  29 */   static Workspace m_userWorkspace = null;
/*     */ 
/*     */   public static boolean doesTableExist(Workspace ws, String table, String[] tableList)
/*     */   {
/*  41 */     boolean tableExists = false;
/*  42 */     if (((ws == null) && (tableList == null)) || (table == null) || (table.length() == 0))
/*     */     {
/*  44 */       return false;
/*     */     }
/*     */ 
/*  47 */     if (tableList == null)
/*     */     {
/*     */       try
/*     */       {
/*  52 */         FieldInfo[] fis = ws.getColumnList(table);
/*  53 */         if ((fis != null) && (fis.length > 0))
/*     */         {
/*  55 */           tableExists = true;
/*     */         }
/*     */ 
/*     */       }
/*     */       catch (DataException e)
/*     */       {
/*  61 */         if (SystemUtils.m_verbose)
/*     */         {
/*  63 */           Report.debug("systemdatabase", "table did not exist: " + table, e);
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/*  70 */       int index = StringUtils.findStringIndexEx(tableList, table, true);
/*  71 */       tableExists = index >= 0;
/*     */     }
/*     */ 
/*  74 */     return tableExists;
/*     */   }
/*     */ 
/*     */   public static boolean isDatabaseType(Workspace ws, String dbType)
/*     */   {
/*  79 */     if (dbType == null)
/*     */     {
/*  81 */       return false;
/*     */     }
/*  83 */     boolean isRightDB = false;
/*  84 */     String name = ws.getProperty("DatabaseType");
/*  85 */     if (name.equalsIgnoreCase(dbType))
/*     */     {
/*  87 */       isRightDB = true;
/*     */     }
/*     */     else
/*     */     {
/*  91 */       name = ws.getProperty("DatabaseName");
/*  92 */       if (name.equalsIgnoreCase(dbType))
/*     */       {
/*  94 */         isRightDB = true;
/*     */       }
/*     */     }
/*  97 */     return isRightDB;
/*     */   }
/*     */ 
/*     */   public static String[] getColumnList(String table, Workspace ws, String[] ignoredColumns) throws DataException
/*     */   {
/* 102 */     if ((table == null) || (ws == null))
/*     */     {
/* 104 */       return null;
/*     */     }
/*     */ 
/* 107 */     FieldInfo[] finfos = ws.getColumnList(table);
/* 108 */     List columnList = new ArrayList();
/* 109 */     for (FieldInfo fi : finfos)
/*     */     {
/* 111 */       if ((ignoredColumns != null) && (StringUtils.findStringIndexEx(ignoredColumns, fi.m_name, true) >= 0))
/*     */         continue;
/* 113 */       columnList.add(fi.m_name);
/*     */     }
/*     */ 
/* 117 */     String[] columns = new String[columnList.size()];
/* 118 */     columnList.toArray(columns);
/* 119 */     return columns;
/*     */   }
/*     */ 
/*     */   public static FieldInfo getColumnInfo(String table, String column, Workspace ws) throws DataException
/*     */   {
/* 124 */     if ((table == null) || (ws == null) || (column == null))
/*     */     {
/* 126 */       return null;
/*     */     }
/* 128 */     FieldInfo result = null;
/*     */ 
/* 130 */     FieldInfo[] fis = ws.getColumnList(table);
/* 131 */     for (FieldInfo fi : fis)
/*     */     {
/* 133 */       if (!fi.m_name.equalsIgnoreCase(column))
/*     */         continue;
/* 135 */       result = fi;
/* 136 */       break;
/*     */     }
/*     */ 
/* 139 */     return result;
/*     */   }
/*     */ 
/*     */   public static boolean addColumn(Workspace ws, String table, FieldInfo[] fis) throws DataException
/*     */   {
/* 144 */     String[] pKeys = ws.getPrimaryKeys(table);
/* 145 */     ws.alterTable(table, fis, null, pKeys);
/* 146 */     return true;
/*     */   }
/*     */ 
/*     */   public static int getRowCount(String table, String whereClause, Workspace ws) throws DataException
/*     */   {
/* 151 */     if ((table == null) || (ws == null))
/*     */     {
/* 153 */       return -1;
/*     */     }
/*     */ 
/* 156 */     IdcStringBuilder builder = new IdcStringBuilder("SELECT Count(*) FROM ");
/* 157 */     builder.append(table);
/* 158 */     if (whereClause != null)
/*     */     {
/* 160 */       builder.append(" WHERE ");
/* 161 */       builder.append(QueryUtils.enclosingQueryWithSafeParenthesis(whereClause));
/*     */     }
/* 163 */     ResultSet rset = ws.createResultSetSQL(builder.toString());
/*     */ 
/* 165 */     String countStr = rset.getStringValue(0);
/* 166 */     return NumberUtils.parseInteger(countStr, -1);
/*     */   }
/*     */ 
/*     */   public static void createTable(Workspace ws, String tableName, String[][] columns, String[] primaryKey)
/*     */     throws DataException
/*     */   {
/* 172 */     FieldInfo[] attribsfi = createFieldInfo(columns, 30);
/* 173 */     ws.createTable(tableName, attribsfi, primaryKey);
/*     */   }
/*     */ 
/*     */   public static FieldInfo[] createFieldInfo(String[][] fields, int defLen)
/*     */   {
/* 180 */     FieldInfo[] fi = new FieldInfo[fields.length];
/* 181 */     for (int i = 0; i < fi.length; ++i)
/*     */     {
/* 183 */       fi[i] = new FieldInfo();
/* 184 */       fi[i].m_name = fields[i][0];
/* 185 */       fi[i].m_isFixedLen = (defLen > 0);
/* 186 */       fi[i].m_maxLen = defLen;
/*     */ 
/* 188 */       String type = fields[i][1];
/* 189 */       if (type.equals("varchar"))
/*     */       {
/* 191 */         if (fields[i].length <= 2)
/*     */           continue;
/* 193 */         String lenStr = fields[i][2];
/* 194 */         int len = NumberUtils.parseInteger(lenStr, 0);
/* 195 */         fi[i].m_maxLen = len;
/* 196 */         fi[i].m_isFixedLen = (len > 0);
/*     */       }
/* 199 */       else if (type.equals("boolean"))
/*     */       {
/* 201 */         fi[i].m_type = 1;
/*     */       }
/* 203 */       else if (type.equals("int"))
/*     */       {
/* 205 */         fi[i].m_type = 3;
/*     */       }
/* 207 */       else if (type.equals("date"))
/*     */       {
/* 209 */         fi[i].m_type = 5;
/*     */       } else {
/* 211 */         if (!type.equals("blob"))
/*     */           continue;
/* 213 */         fi[i].m_type = 9;
/* 214 */         if (fields[i].length <= 2)
/*     */           continue;
/* 216 */         String lenStr = fields[i][2];
/* 217 */         int len = NumberUtils.parseTypedInteger(lenStr, 4096, 5, 1);
/*     */ 
/* 219 */         fi[i].m_maxLen = len;
/*     */       }
/*     */     }
/*     */ 
/* 223 */     return fi;
/*     */   }
/*     */ 
/*     */   public static FieldInfo[] getPrimaryKeyFields(Workspace ws, String tableName) throws DataException
/*     */   {
/* 228 */     String[] pKeys = ws.getPrimaryKeys(tableName);
/*     */     FieldInfo[] pFields;
/* 231 */     if (pKeys != null)
/*     */     {
/* 233 */       FieldInfo[] pFields = new FieldInfo[pKeys.length];
/*     */ 
/* 235 */       for (int i = 0; i < pKeys.length; ++i)
/*     */       {
/* 237 */         pFields[i] = getColumnInfo(tableName, pKeys[i], ws);
/*     */       }
/*     */     }
/*     */     else
/*     */     {
/* 242 */       pFields = new FieldInfo[0];
/*     */     }
/* 244 */     return pFields;
/*     */   }
/*     */ 
/*     */   public static String getShortIndexName(String name, int length)
/*     */   {
/* 249 */     String newName = name;
/* 250 */     if (name.length() > length)
/*     */     {
/* 252 */       int hash = name.hashCode();
/* 253 */       String hashStr = Integer.toHexString(hash).toUpperCase();
/* 254 */       int hashLen = hashStr.length();
/* 255 */       String begin = name.substring(0, length - hashLen - 2);
/* 256 */       String end = name.substring(name.length() - 2);
/* 257 */       newName = begin + hashStr + end;
/*     */     }
/* 259 */     return newName;
/*     */   }
/*     */ 
/*     */   public static int[] getRangeBoundaries(Workspace workSpace, int low, int high, int max, int size, String query)
/*     */     throws DataException
/*     */   {
/* 277 */     int[] rangeData = new int[3];
/* 278 */     int numberOfIterations = 0;
/* 279 */     DataBinder binder = new DataBinder();
/*     */ 
/* 281 */     int rangeCount = 0;
/*     */ 
/* 283 */     while ((low <= high) && (high <= max) && (numberOfIterations < 5))
/*     */     {
/* 287 */       binder.putLocal("low", Integer.toString(low));
/* 288 */       binder.putLocal("high", Integer.toString(high));
/*     */ 
/* 290 */       ResultSet rangeCountRset = workSpace.createResultSet(query, binder);
/* 291 */       String rangeCountString = rangeCountRset.getStringValue(0);
/* 292 */       rangeCount = NumberUtils.parseInteger(rangeCountString, 0);
/*     */ 
/* 294 */       int diff = high - low;
/* 295 */       if (rangeCount < 0.8D * size)
/*     */       {
/* 297 */         diff = (int)(diff * 1.25D);
/* 298 */         high = low + diff;
/*     */ 
/* 300 */         if (high <= max)
/*     */           break label163;
/* 302 */         high = max;
/* 303 */         break;
/*     */       }
/*     */ 
/* 308 */       if (rangeCount <= size * 1.5D)
/*     */         break;
/* 310 */       diff = (int)(diff * 0.75D);
/* 311 */       high = low + diff;
/*     */ 
/* 319 */       label163: numberOfIterations += 1;
/*     */     }
/*     */ 
/* 322 */     rangeData[0] = low;
/* 323 */     rangeData[1] = high;
/* 324 */     rangeData[2] = rangeCount;
/*     */ 
/* 326 */     return rangeData;
/*     */   }
/*     */ 
/*     */   public static String[] getViewList(Workspace ws, String table)
/*     */     throws DataException
/*     */   {
/* 340 */     List vlist = new Vector();
/* 341 */     String[] viewList = null;
/*     */     String query;
/*     */     String query;
/* 343 */     if (table != null)
/*     */     {
/* 345 */       query = "select view_name from all_views WHERE view_name='" + table.toUpperCase() + "'";
/*     */     }
/*     */     else
/*     */     {
/* 349 */       query = "select view_name from all_views";
/*     */     }
/* 351 */     ResultSet rset = null;
/*     */     try
/*     */     {
/* 354 */       rset = ws.createResultSetSQL(query);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 359 */       Report.trace("systemdatabase", "Failure to retrieve view list", e);
/*     */     }
/*     */ 
/* 362 */     boolean done = false;
/* 363 */     while ((!done) && (rset != null) && (!rset.isEmpty()))
/*     */     {
/* 365 */       String view = rset.getStringValue(0);
/* 366 */       if (!view.isEmpty())
/*     */       {
/* 368 */         vlist.add(view);
/*     */       }
/* 370 */       done = !rset.next();
/*     */     }
/* 372 */     viewList = StringUtils.convertListToArray(vlist);
/* 373 */     return viewList;
/*     */   }
/*     */ 
/*     */   public static FieldInfo[] getActualColumnList(Workspace ws, String tableName)
/*     */     throws DataException
/*     */   {
/* 388 */     FieldInfo[] fields = null;
/*     */ 
/* 391 */     if (EBRModeActive(ws))
/*     */     {
/* 393 */       String[] viewList = getViewList(ws, tableName);
/* 394 */       for (int i = 0; i < viewList.length; ++i)
/*     */       {
/* 396 */         if (viewList[i].compareToIgnoreCase(tableName) != 0) {
/*     */           continue;
/*     */         }
/*     */ 
/* 400 */         tableName = tableName + "_";
/*     */       }
/*     */     }
/*     */ 
/* 404 */     fields = ws.getColumnList(tableName);
/*     */ 
/* 406 */     return fields;
/*     */   }
/*     */ 
/*     */   public static boolean EBRModeActive(Workspace ws)
/*     */   {
/* 412 */     boolean isOracle = isDatabaseType(ws, DatabaseTypes.ORACLE);
/* 413 */     if (!isOracle)
/*     */     {
/* 415 */       return false;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 420 */       m_ebrMode = getViewList(ws, "Config").length > 0;
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/* 425 */       m_ebrMode = false;
/*     */     }
/* 427 */     Report.trace("systemdatabase", "EBR Mode detected to be " + m_ebrMode, null);
/*     */ 
/* 429 */     return m_ebrMode;
/*     */   }
/*     */ 
/*     */   public static ResultSet createPersistForwardOnlyResultSet(Workspace ws, String query, Parameters args)
/*     */     throws DataException
/*     */   {
/* 446 */     setPersistForwardOnyFlags(args, true, true);
/* 447 */     return ws.createResultSet(query, args);
/*     */   }
/*     */ 
/*     */   public static ResultSet createPersistForwardOnlyResultSetSQL(Workspace ws, String sql, Parameters args)
/*     */     throws DataException
/*     */   {
/* 464 */     setPersistForwardOnyFlags(args, true, true);
/* 465 */     return ws.createResultSetSQL(sql, args);
/*     */   }
/*     */ 
/*     */   public static ResultSet createForwardOnlyResultSet(Workspace ws, String query, Parameters args)
/*     */     throws DataException
/*     */   {
/* 479 */     setPersistForwardOnyFlags(args, false, true);
/* 480 */     return ws.createResultSet(query, args);
/*     */   }
/*     */ 
/*     */   public static ResultSet createForwardOnlyResultSetSQL(Workspace ws, String sql, Parameters args)
/*     */     throws DataException
/*     */   {
/* 494 */     setPersistForwardOnyFlags(args, false, true);
/* 495 */     return ws.createResultSetSQL(sql, args);
/*     */   }
/*     */ 
/*     */   protected static void setPersistForwardOnyFlags(Parameters args, boolean isPersisted, boolean isForwardOnly)
/*     */   {
/* 500 */     if (args == null)
/*     */       return;
/* 502 */     if (args instanceof DataBinder)
/*     */     {
/* 504 */       if (isPersisted)
/*     */       {
/* 506 */         ((DataBinder)args).putLocal("IsQueryObjectPersistent", "1");
/*     */       }
/*     */ 
/* 509 */       if (!isForwardOnly)
/*     */         return;
/* 511 */       ((DataBinder)args).putLocal("UseForwardOnlyCursor", "1");
/*     */     }
/*     */     else {
/* 514 */       if (!args instanceof PropParameters)
/*     */         return;
/* 516 */       if (isPersisted)
/*     */       {
/* 518 */         ((PropParameters)args).m_properties.put("IsQueryObjectPersistent", "1");
/*     */       }
/*     */ 
/* 521 */       if (!isForwardOnly)
/*     */         return;
/* 523 */       ((PropParameters)args).m_properties.put("UseForwardOnlyCursor", "1");
/*     */     }
/*     */   }
/*     */ 
/*     */   public static void addWorkspace(String name, Workspace ws)
/*     */   {
/* 533 */     if (name.equals("system"))
/*     */     {
/* 535 */       m_workspace = ws;
/*     */     } else {
/* 537 */       if (!name.equals("user"))
/*     */         return;
/* 539 */       m_userWorkspace = ws;
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Workspace getWorkspace(String name)
/*     */   {
/* 545 */     if (name.equals("system"))
/*     */     {
/* 547 */       return m_workspace;
/*     */     }
/* 549 */     if (name.equals("user"))
/*     */     {
/* 551 */       return m_userWorkspace;
/*     */     }
/*     */ 
/* 555 */     return null;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 561 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98052 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.WorkspaceUtils
 * JD-Core Version:    0.5.4
 */