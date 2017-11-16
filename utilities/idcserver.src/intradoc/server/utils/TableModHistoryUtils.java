/*     */ package intradoc.server.utils;
/*     */ 
/*     */ import intradoc.common.ExecutionContextAdaptor;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.server.DataUtils;
/*     */ import intradoc.server.PageMerger;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.util.Date;
/*     */ import java.util.Random;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class TableModHistoryUtils
/*     */ {
/*  32 */   protected static Random m_randomGen = new Random();
/*     */ 
/*     */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns, boolean isDelete)
/*     */     throws DataException
/*     */   {
/*  37 */     return insertEntry(ws, table, primaryKeys, pkColumns, null, null, null, isDelete);
/*     */   }
/*     */ 
/*     */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns, String pkTypes, boolean isDelete)
/*     */     throws DataException
/*     */   {
/*  43 */     return insertEntry(ws, table, primaryKeys, pkColumns, pkTypes, null, null, isDelete);
/*     */   }
/*     */ 
/*     */   public static int insertEntry(Workspace ws, String table, DataResultSet drset, String pkColumns, boolean isDelete, boolean isSingleRowOnly)
/*     */     throws DataException
/*     */   {
/*  63 */     return insertEntry(ws, table, drset, pkColumns, "schSourceID", "schModifyTimestamp", isDelete, isSingleRowOnly);
/*     */   }
/*     */ 
/*     */   public static int insertEntry(Workspace ws, String table, DataResultSet drset, String pkColumns, String srcColName, String modColName, boolean isDelete, boolean isSingleRowOnly)
/*     */     throws DataException
/*     */   {
/*  87 */     FieldInfo fi = new FieldInfo();
/*  88 */     int srcIndex = -1;
/*  89 */     int modTimeIndex = -1;
/*  90 */     if (srcColName != null)
/*     */     {
/*  92 */       drset.getFieldInfo(srcColName, fi);
/*  93 */       srcIndex = fi.m_index;
/*     */     }
/*  95 */     if (modColName != null)
/*     */     {
/*  97 */       drset.getFieldInfo(modColName, fi);
/*  98 */       modTimeIndex = fi.m_index;
/*     */     }
/* 100 */     return insertEntry(ws, table, drset, pkColumns, srcIndex, modTimeIndex, isDelete, isSingleRowOnly);
/*     */   }
/*     */ 
/*     */   public static int insertEntry(Workspace ws, String table, DataResultSet drset, String pkColumns, int srcIndex, int modTimeIndex, boolean isDelete, boolean isSingleRowOnly)
/*     */     throws DataException
/*     */   {
/* 108 */     int succeeded = 0;
/* 109 */     if (!isSingleRowOnly)
/*     */     {
/* 111 */       drset.first();
/*     */     }
/*     */     do
/*     */     {
/* 115 */       String srcID = "";
/* 116 */       if (srcIndex != -1)
/*     */       {
/* 118 */         srcID = drset.getStringValue(srcIndex);
/*     */       }
/* 120 */       String pKeys = constructPKStringOrTypes(drset, pkColumns, false);
/* 121 */       String pkTypes = constructPKStringOrTypes(drset, pkColumns, true);
/* 122 */       String modifyDateStr = null;
/* 123 */       if (modTimeIndex >= 0)
/*     */       {
/* 125 */         Date modifyDate = drset.getDateValue(modTimeIndex);
/* 126 */         modifyDateStr = LocaleUtils.formatODBC(modifyDate);
/*     */       }
/*     */ 
/* 129 */       boolean inserted = insertEntry(ws, table, pKeys, pkColumns, pkTypes, srcID, modifyDateStr, isDelete);
/*     */ 
/* 131 */       if (!inserted)
/*     */         continue;
/* 133 */       ++succeeded;
/*     */     }
/* 135 */     while ((!isSingleRowOnly) && (drset.next()));
/* 136 */     return succeeded;
/*     */   }
/*     */ 
/*     */   public static boolean insertEntry(Workspace ws, String table, String primaryKeys, String pkColumns, String pkTypes, String sourceID, String dateStr, boolean isDelete)
/*     */     throws DataException
/*     */   {
/* 156 */     String query = "ITableChangeHistory";
/* 157 */     String dateCol = "dChangeDate";
/* 158 */     if (isDelete)
/*     */     {
/* 160 */       query = "IDeletedRows";
/* 161 */       dateCol = "dDeleteDate";
/*     */     }
/* 163 */     if ((table == null) || (table.length() == 0))
/*     */     {
/* 165 */       String msg = LocaleUtils.encodeMessage("csUnablePopulateTableNotExist", null, table);
/* 166 */       throw new DataException(msg);
/*     */     }
/* 168 */     if ((primaryKeys == null) || (primaryKeys.length() == 0))
/*     */     {
/* 170 */       String msg = LocaleUtils.encodeMessage("csUnablePopulateTablePrimaryKeysNotExist", null, table);
/* 171 */       throw new DataException(msg);
/*     */     }
/* 173 */     String nextKey = getNextKey();
/*     */ 
/* 175 */     DataBinder binder = new DataBinder();
/* 176 */     binder.putLocal("dRowID", nextKey);
/* 177 */     binder.putLocal("dTable", table);
/* 178 */     binder.putLocal("dPrimaryKeys", primaryKeys);
/* 179 */     binder.putLocal("dPKColumns", pkColumns);
/*     */ 
/* 182 */     if (pkTypes == null)
/*     */     {
/* 184 */       pkTypes = "";
/*     */     }
/* 186 */     if (sourceID == null)
/*     */     {
/* 188 */       sourceID = "";
/*     */     }
/* 190 */     Date date = new Date();
/* 191 */     String actionDateStr = LocaleUtils.formatODBC(date);
/*     */ 
/* 193 */     if ((dateStr == null) || (dateStr.length() == 0))
/*     */     {
/* 195 */       dateStr = actionDateStr;
/*     */     }
/*     */ 
/* 198 */     binder.putLocal(dateCol, dateStr);
/* 199 */     binder.putLocal("dSourceID", sourceID);
/* 200 */     binder.putLocal("dPKTypes", pkTypes);
/* 201 */     binder.putLocal("dActionDate", actionDateStr);
/*     */ 
/* 204 */     long result = ws.execute(query, binder);
/*     */ 
/* 207 */     return result != 0L;
/*     */   }
/*     */ 
/*     */   protected static synchronized String getNextKey()
/*     */   {
/* 217 */     long time = System.currentTimeMillis();
/*     */ 
/* 219 */     time >>= 3;
/* 220 */     time &= 1099511627775L;
/*     */ 
/* 227 */     Random newRandom = new Random();
/* 228 */     long halfMaxLong = 4611686018427387903L;
/* 229 */     long t1 = newRandom.nextLong();
/* 230 */     t1 %= halfMaxLong;
/* 231 */     long t2 = m_randomGen.nextLong();
/* 232 */     t2 %= halfMaxLong;
/* 233 */     long tot = t1 + t2;
/* 234 */     m_randomGen = new Random(tot);
/* 235 */     long keyLong = m_randomGen.nextLong();
/*     */ 
/* 237 */     keyLong &= 16777215L;
/* 238 */     String key = Long.toHexString(time).toUpperCase() + Long.toHexString(keyLong);
/* 239 */     return key;
/*     */   }
/*     */ 
/*     */   protected static String constructPKStringOrTypes(DataResultSet drset, String keys, boolean isTypes)
/*     */   {
/* 245 */     Vector constraints = StringUtils.parseArray(keys, ',', '\\');
/* 246 */     int size = constraints.size();
/* 247 */     Vector values = new IdcVector();
/* 248 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 250 */       String key = (String)constraints.elementAt(i);
/* 251 */       FieldInfo fi = new FieldInfo();
/*     */ 
/* 254 */       boolean foundField = false;
/* 255 */       int fieldSize = drset.getNumFields();
/* 256 */       for (int j = 0; j < fieldSize; ++j)
/*     */       {
/* 258 */         String fieldName = drset.getFieldName(j);
/* 259 */         if (!fieldName.equalsIgnoreCase(key))
/*     */           continue;
/* 261 */         foundField = true;
/* 262 */         drset.getIndexFieldInfo(j, fi);
/* 263 */         break;
/*     */       }
/*     */ 
/* 266 */       if (!foundField)
/*     */         continue;
/* 268 */       if (isTypes)
/*     */       {
/*     */         String value;
/* 270 */         switch (fi.m_type)
/*     */         {
/*     */         case 1:
/* 273 */           values.addElement("Boolean");
/* 274 */           break;
/*     */         case 5:
/* 276 */           values.addElement("Date");
/* 277 */           break;
/*     */         case 3:
/* 279 */           values.addElement("Number");
/* 280 */           break;
/*     */         case 2:
/* 282 */           value = "Char";
/* 283 */           if (fi.m_maxLen >= 0)
/*     */           {
/* 285 */             value = value + " " + fi.m_maxLen;
/*     */           }
/* 287 */           values.addElement(value);
/* 288 */           break;
/*     */         case 4:
/*     */         default:
/* 290 */           value = "Text";
/* 291 */           if (fi.m_maxLen >= 0)
/*     */           {
/* 293 */             value = value + (32 + fi.m_maxLen);
/*     */           }
/* 295 */           values.addElement(value);
/*     */         }
/*     */       }
/*     */       else
/*     */       {
/* 300 */         String value = drset.getStringValue(fi.m_index);
/* 301 */         values.addElement(value);
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 306 */     return StringUtils.createString(values, ',', '^');
/*     */   }
/*     */ 
/*     */   public static ResultSet retrieveChangedRows(Workspace ws, String table, Date beginDate, boolean isDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 322 */     Date end = new Date();
/* 323 */     return retrieveChangedRows(ws, table, beginDate, end, isDelete);
/*     */   }
/*     */ 
/*     */   public static ResultSet retrieveChangedRows(Workspace ws, String table, Date beginDate, Date endDate, boolean isDelete)
/*     */     throws DataException, ServiceException
/*     */   {
/* 340 */     return retrieveChangedRows(ws, table, beginDate, endDate, isDelete, true);
/*     */   }
/*     */ 
/*     */   public static ResultSet retrieveChangedRows(Workspace ws, String table, Date beginDate, Date endDate, boolean isDelete, boolean isFormat)
/*     */     throws DataException, ServiceException
/*     */   {
/* 360 */     DataBinder binder = new DataBinder();
/* 361 */     binder.putLocal("dTable", table);
/* 362 */     if (beginDate != null)
/*     */     {
/* 364 */       binder.putLocal("beginDate", LocaleUtils.formatODBC(beginDate));
/*     */     }
/* 366 */     binder.putLocal("endDate", LocaleUtils.formatODBC(endDate));
/*     */ 
/* 368 */     String dataSource = "";
/* 369 */     if (isDelete)
/*     */     {
/* 371 */       dataSource = "ArchiveDeletedRows";
/*     */     }
/*     */     else
/*     */     {
/* 375 */       dataSource = "ArchiveChangedRows";
/*     */     }
/*     */ 
/* 379 */     String[][] sqlInfo = DataUtils.lookupSQL(dataSource);
/* 380 */     String sql = sqlInfo[0][0];
/*     */ 
/* 383 */     ExecutionContextAdaptor cxt = new ExecutionContextAdaptor();
/* 384 */     PageMerger pageMerger = new PageMerger(binder, cxt);
/*     */     try
/*     */     {
/* 387 */       sql = pageMerger.evaluateScript(sql);
/*     */     }
/*     */     catch (Exception e)
/*     */     {
/*     */       String msg;
/* 393 */       throw new ServiceException(msg, e);
/*     */     }
/*     */     finally
/*     */     {
/* 397 */       pageMerger.releaseAllTemporary();
/*     */     }
/*     */ 
/* 400 */     ResultSet rs = ws.createResultSetSQL(sql);
/*     */ 
/* 402 */     if (isFormat)
/*     */     {
/* 404 */       rs = formatChangeRS(rs);
/*     */     }
/* 406 */     return rs;
/*     */   }
/*     */ 
/*     */   public static ResultSet formatChangeRS(ResultSet rs)
/*     */     throws DataException
/*     */   {
/* 419 */     if (!rs.first())
/*     */     {
/* 421 */       return rs;
/*     */     }
/*     */ 
/* 424 */     DataResultSet drset = createChangedDataResultSet(rs);
/*     */ 
/* 426 */     FieldInfo fi = new FieldInfo();
/* 427 */     rs.getFieldInfo("dPrimaryKeys", fi);
/* 428 */     for (; rs.isRowPresent(); rs.next())
/*     */     {
/* 430 */       String keys = rs.getStringValue(fi.m_index);
/* 431 */       Vector row = StringUtils.parseArray(keys, ',', '^');
/* 432 */       drset.addRow(row);
/*     */     }
/*     */ 
/* 435 */     return drset;
/*     */   }
/*     */ 
/*     */   protected static DataResultSet createChangedDataResultSet(ResultSet rset) throws DataException
/*     */   {
/* 440 */     FieldInfo fi = new FieldInfo();
/* 441 */     rset.getFieldInfo("dPKColumns", fi);
/* 442 */     String columnNames = rset.getStringValue(fi.m_index);
/* 443 */     rset.getFieldInfo("dPKTypes", fi);
/* 444 */     String typeNames = rset.getStringValue(fi.m_index);
/*     */ 
/* 446 */     Vector columns = StringUtils.parseArray(columnNames, ',', '^');
/* 447 */     Vector types = StringUtils.parseArray(typeNames, ',', '^');
/*     */ 
/* 449 */     if ((types.size() != 0) && (types.size() != columns.size()))
/*     */     {
/* 451 */       String msg = LocaleUtils.encodeMessage("csUnableCreateResultSetForChangedRowsTypeMismatch", null, columnNames, typeNames);
/*     */ 
/* 453 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 456 */     if (columns.size() == 0)
/*     */     {
/* 458 */       String msg = LocaleUtils.encodeMessage("csUnableCreateResultSetForChangedRowsColumnsNotExist", null);
/*     */ 
/* 460 */       throw new DataException(msg);
/*     */     }
/*     */ 
/* 463 */     Vector fields = new IdcVector();
/* 464 */     int size = columns.size();
/* 465 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 467 */       fi = new FieldInfo();
/*     */ 
/* 469 */       fi.m_name = ((String)columns.elementAt(i));
/* 470 */       fi.m_index = i;
/* 471 */       if (types.size() > 0)
/*     */       {
/* 473 */         String type = (String)types.elementAt(i);
/* 474 */         if (type.equals("Boolean"))
/*     */         {
/* 476 */           fi.m_type = 1;
/*     */         }
/* 478 */         else if (type.equals("Number"))
/*     */         {
/* 480 */           fi.m_type = 3;
/*     */         }
/* 482 */         else if (type.startsWith("Char"))
/*     */         {
/* 484 */           fi.m_type = 2;
/* 485 */           int index = type.lastIndexOf(32);
/* 486 */           if (index > 0)
/*     */           {
/* 488 */             ++index;
/* 489 */             String lenStr = type.substring(index);
/* 490 */             fi.m_maxLen = Integer.parseInt(lenStr);
/*     */           }
/*     */         }
/* 493 */         else if (type.startsWith("Text"))
/*     */         {
/* 495 */           fi.m_type = 6;
/* 496 */           int index = type.lastIndexOf(32);
/* 497 */           if (index > 0)
/*     */           {
/* 499 */             ++index;
/* 500 */             String lenStr = type.substring(index);
/* 501 */             fi.m_maxLen = Integer.parseInt(lenStr);
/*     */           }
/*     */         }
/*     */       }
/*     */ 
/* 506 */       fields.addElement(fi);
/*     */     }
/*     */ 
/* 509 */     DataResultSet drset = new DataResultSet();
/* 510 */     drset.mergeFieldsWithFlags(fields, 0);
/*     */ 
/* 512 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 517 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.server.utils.TableModHistoryUtils
 * JD-Core Version:    0.5.4
 */