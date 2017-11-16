/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.BufferPool;
/*     */ import intradoc.common.FileUtils;
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataStreamValue;
/*     */ import intradoc.data.DatabaseConfigData;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.Workspace;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.File;
/*     */ import java.io.FileInputStream;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.io.Reader;
/*     */ import java.sql.Blob;
/*     */ import java.sql.Clob;
/*     */ import java.sql.PreparedStatement;
/*     */ import java.sql.ResultSetMetaData;
/*     */ import java.sql.SQLException;
/*     */ import java.sql.Statement;
/*     */ import java.sql.Timestamp;
/*     */ import java.util.Date;
/*     */ import java.util.Hashtable;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcResultSet
/*     */   implements intradoc.data.ResultSet, DataStreamValue
/*     */ {
/*  39 */   protected Properties m_columnMap = null;
/*  40 */   protected Statement m_statement = null;
/*  41 */   protected String m_query = null;
/*     */   protected IdcDateFormat m_dateFormat;
/*     */   public java.sql.ResultSet m_resultSet;
/*     */   protected ResultSetMetaData m_metaData;
/*     */   protected Hashtable m_fieldMapping;
/*     */   protected Vector m_fieldList;
/*     */   protected boolean m_columnCountWasRetrieved;
/*     */   protected int m_columnCount;
/*     */   protected Object[] m_cachedRow;
/*     */   JdbcManager m_manager;
/*     */   JdbcConnection m_con;
/*     */   DatabaseConfigData m_config;
/*     */   protected boolean m_isRowPresent;
/*     */   protected boolean m_isEmpty;
/*     */   protected boolean m_isFirst;
/*     */   protected Hashtable m_taminoDateFields;
/*  66 */   protected int m_bytesPerChar = 1;
/*     */   protected BufferPool m_bufferPool;
/*     */ 
/*     */   public JdbcResultSet(JdbcManager manager)
/*     */   {
/*  72 */     this.m_statement = null;
/*     */ 
/*  74 */     this.m_resultSet = null;
/*  75 */     this.m_fieldMapping = null;
/*  76 */     this.m_metaData = null;
/*     */ 
/*  78 */     this.m_cachedRow = null;
/*     */ 
/*  80 */     this.m_isRowPresent = false;
/*  81 */     this.m_isEmpty = true;
/*  82 */     this.m_isFirst = true;
/*     */ 
/*  84 */     this.m_manager = manager;
/*     */ 
/*  86 */     this.m_config = manager.m_config;
/*  87 */     this.m_con = null;
/*  88 */     this.m_columnMap = manager.getColumnMap();
/*  89 */     this.m_dateFormat = LocaleUtils.m_odbcDateFormat;
/*  90 */     this.m_bytesPerChar = this.m_config.getValueAsInt("NumBytesPerCharInDB", 1);
/*     */ 
/*  92 */     this.m_bufferPool = BufferPool.getBufferPool("JdbcResultSet");
/*     */   }
/*     */ 
/*     */   public boolean isMutable()
/*     */   {
/*  97 */     return false;
/*     */   }
/*     */ 
/*     */   public boolean hasRawObjects()
/*     */   {
/* 102 */     return true;
/*     */   }
/*     */ 
/*     */   public ResultSetMetaData getMetaData() throws SQLException
/*     */   {
/* 107 */     return this.m_resultSet.getMetaData();
/*     */   }
/*     */ 
/*     */   public void setQueryInfo(Statement stmt, String query, JdbcConnection con, java.sql.ResultSet rset)
/*     */   {
/* 115 */     this.m_statement = stmt;
/* 116 */     this.m_query = query;
/* 117 */     this.m_con = con;
/* 118 */     this.m_resultSet = rset;
/*     */     try
/*     */     {
/* 123 */       this.m_metaData = this.m_resultSet.getMetaData();
/*     */ 
/* 125 */       this.m_fieldMapping = new Hashtable();
/* 126 */       this.m_fieldList = new IdcVector();
/* 127 */       FieldInfo info = null;
/* 128 */       FieldInfo finfo = null;
/*     */ 
/* 130 */       int count = this.m_metaData.getColumnCount();
/* 131 */       for (int i = 1; i <= count; ++i)
/*     */       {
/* 133 */         info = getJdbcInfo(i);
/* 134 */         if (this.m_columnMap != null)
/*     */         {
/* 136 */           String alias = this.m_columnMap.getProperty(info.m_name.toUpperCase());
/* 137 */           if (alias != null)
/*     */           {
/* 139 */             info.m_name = alias;
/*     */           }
/*     */ 
/*     */         }
/*     */ 
/* 144 */         finfo = (FieldInfo)this.m_fieldMapping.get(info.m_name);
/* 145 */         if (finfo != null)
/*     */         {
/* 147 */           info = finfo;
/*     */         }
/*     */ 
/* 150 */         this.m_fieldMapping.put(info.m_name, info);
/* 151 */         this.m_fieldList.addElement(info);
/*     */       }
/*     */ 
/* 154 */       this.m_isEmpty = (!next());
/* 155 */       if (!this.m_isEmpty)
/*     */       {
/* 157 */         this.m_isRowPresent = true;
/*     */       }
/* 159 */       this.m_columnCountWasRetrieved = true;
/* 160 */       this.m_columnCount = count;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 164 */       Report.trace(null, "JdbcResultSet.setQueryInfo Error", e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Workspace getWorkspace()
/*     */   {
/* 171 */     return null;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty()
/*     */   {
/* 176 */     return this.m_isEmpty;
/*     */   }
/*     */ 
/*     */   public boolean isRowPresent()
/*     */   {
/* 181 */     return this.m_isRowPresent;
/*     */   }
/*     */ 
/*     */   public int getNumFields()
/*     */   {
/* 186 */     if (this.m_con == null)
/*     */     {
/* 188 */       return -1;
/*     */     }
/* 190 */     if (this.m_columnCountWasRetrieved)
/*     */     {
/* 192 */       return this.m_columnCount;
/*     */     }
/* 194 */     int count = 0;
/*     */     try
/*     */     {
/* 197 */       count = this.m_metaData.getColumnCount();
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 201 */       Report.trace(null, null, e);
/*     */     }
/*     */ 
/* 204 */     return count;
/*     */   }
/*     */ 
/*     */   public String getFieldName(int index)
/*     */   {
/* 209 */     FieldInfo fi = (FieldInfo)this.m_fieldList.elementAt(index);
/* 210 */     return fi.m_name;
/*     */   }
/*     */ 
/*     */   protected FieldInfo getJdbcInfo(int index)
/*     */   {
/* 215 */     FieldInfo fieldInfo = new FieldInfo();
/*     */     try
/*     */     {
/* 218 */       fieldInfo.m_index = (index - 1);
/* 219 */       if (this.m_manager.isInformix() == true)
/*     */       {
/* 224 */         fieldInfo.m_name = this.m_metaData.getColumnName(index).toUpperCase();
/*     */       }
/* 226 */       else if (this.m_config.getValueAsBool("JdbcUseColumnLabelForColumnName", false))
/*     */       {
/* 229 */         fieldInfo.m_name = this.m_metaData.getColumnLabel(index);
/*     */       }
/*     */       else
/*     */       {
/* 233 */         fieldInfo.m_name = this.m_metaData.getColumnName(index);
/*     */       }
/* 235 */       fieldInfo.m_isFixedLen = true;
/* 236 */       fieldInfo.m_maxLen = 8;
/*     */ 
/* 238 */       int type = this.m_metaData.getColumnType(index);
/* 239 */       switch (type)
/*     */       {
/*     */       case -7:
/* 242 */         fieldInfo.m_type = 1;
/* 243 */         break;
/*     */       case -5:
/* 245 */         fieldInfo.m_maxLen = 19;
/*     */       case -6:
/*     */       case 2:
/*     */       case 3:
/*     */       case 4:
/*     */       case 5:
/* 256 */         if ((this.m_manager.isTamino3()) && (this.m_taminoDateFields.get(fieldInfo.m_name.toLowerCase()) != null))
/*     */         {
/* 259 */           fieldInfo.m_type = 5;
/*     */         }
/*     */         else
/*     */         {
/* 263 */           fieldInfo.m_type = 3;
/* 264 */           if (fieldInfo.m_maxLen == 8)
/*     */           {
/* 266 */             fieldInfo.m_maxLen = 10;
/*     */           }
/*     */           try
/*     */           {
/* 270 */             if ((this.m_manager.isOracle()) || (this.m_manager.isDB2()) || (this.m_manager.isTamino()) || (type == 3) || (type == 2))
/*     */             {
/* 273 */               fieldInfo.m_maxLen = this.m_metaData.getPrecision(index);
/*     */             }
/*     */           }
/*     */           catch (Exception ignore)
/*     */           {
/*     */           }
/*     */         }
/*     */ 
/* 281 */         if ((type == 3) || (type == 2))
/*     */         {
/*     */           try
/*     */           {
/* 287 */             int scale = this.m_metaData.getScale(index);
/* 288 */             if (scale > 0)
/*     */             {
/* 290 */               fieldInfo.m_type = 11;
/* 291 */               fieldInfo.m_scale = scale;
/*     */             }
/*     */           }
/*     */           catch (Exception ignore) {
/*     */           }
/*     */         }
/* 297 */         break;
/*     */       case 6:
/*     */       case 7:
/*     */       case 8:
/* 303 */         fieldInfo.m_type = 4;
/* 304 */         break;
/*     */       case -101:
/*     */       case 91:
/*     */       case 92:
/*     */       case 93:
/* 311 */         fieldInfo.m_type = 5;
/* 312 */         fieldInfo.m_maxLen = 20;
/* 313 */         break;
/*     */       case -4:
/*     */       case -3:
/*     */       case -2:
/* 317 */         fieldInfo.m_type = 7;
/* 318 */         fieldInfo.m_isFixedLen = false;
/* 319 */         break;
/*     */       case -1:
/* 321 */         fieldInfo.m_isFixedLen = true;
/*     */ 
/* 324 */         fieldInfo.m_type = 6;
/* 325 */         fieldInfo.m_maxLen = this.m_metaData.getColumnDisplaySize(index);
/* 326 */         if ((fieldInfo.m_maxLen == 0) || (fieldInfo.m_maxLen >= JdbcFunctions.m_memoMandateLength) || (this.m_manager.isDB2()))
/*     */         {
/* 329 */           fieldInfo.m_isFixedLen = false;
/*     */         }
/* 333 */         else if (this.m_bytesPerChar > 1)
/*     */         {
/* 335 */           fieldInfo.m_maxLen /= this.m_bytesPerChar; } break;
/*     */       case -9:
/*     */       case 12:
/* 343 */         fieldInfo.m_type = 6;
/* 344 */         fieldInfo.m_maxLen = this.m_metaData.getColumnDisplaySize(index);
/* 345 */         if ((fieldInfo.m_maxLen == 0) || (fieldInfo.m_maxLen >= JdbcFunctions.m_memoMandateLength))
/*     */         {
/* 347 */           fieldInfo.m_isFixedLen = false;
/*     */         }
/* 351 */         else if (this.m_bytesPerChar > 1)
/*     */         {
/* 353 */           fieldInfo.m_maxLen /= this.m_bytesPerChar; } break;
/*     */       case 1:
/* 357 */         fieldInfo.m_type = 2;
/* 358 */         fieldInfo.m_maxLen = this.m_metaData.getColumnDisplaySize(index);
/*     */ 
/* 361 */         if ((this.m_bytesPerChar > 1) && (fieldInfo.m_maxLen >= this.m_bytesPerChar))
/*     */         {
/* 363 */           fieldInfo.m_maxLen /= this.m_bytesPerChar;
/*     */         }
/* 365 */         if (fieldInfo.m_maxLen > 1)
/*     */         {
/* 367 */           fieldInfo.m_type = 6; } break;
/*     */       case 2004:
/* 371 */         fieldInfo.m_type = 9;
/* 372 */         break;
/*     */       case 2005:
/* 375 */         if (this.m_manager.isSqlServer())
/*     */         {
/* 377 */           fieldInfo.m_type = 6;
/* 378 */           fieldInfo.m_maxLen = this.m_metaData.getColumnDisplaySize(index);
/*     */         }
/*     */         else
/*     */         {
/* 382 */           fieldInfo.m_type = 10;
/*     */         }
/* 384 */         break;
/*     */       default:
/* 386 */         fieldInfo.m_type = 6;
/* 387 */         fieldInfo.m_isFixedLen = false;
/*     */       }
/*     */ 
/* 390 */       if (!fieldInfo.m_isFixedLen)
/*     */       {
/* 392 */         fieldInfo.m_maxLen = 0;
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 397 */       Report.trace(null, "JdbcResultSet.getJdbcInfo:getting info error.", e);
/*     */     }
/*     */ 
/* 401 */     return fieldInfo;
/*     */   }
/*     */ 
/*     */   public void getIndexFieldInfo(int index, FieldInfo fieldInfo)
/*     */   {
/* 406 */     FieldInfo info = (FieldInfo)this.m_fieldList.elementAt(index);
/*     */ 
/* 408 */     fieldInfo.copy(info);
/*     */   }
/*     */ 
/*     */   public boolean getFieldInfo(String fieldName, FieldInfo fieldInfo)
/*     */   {
/* 413 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.get(fieldName);
/* 414 */     if (info == null)
/*     */     {
/* 416 */       info = (FieldInfo)this.m_fieldMapping.get(fieldName.toUpperCase());
/* 417 */       if (info == null) {
/* 418 */         return false;
/*     */       }
/*     */     }
/* 421 */     fieldInfo.copy(info);
/* 422 */     return true;
/*     */   }
/*     */ 
/*     */   public int getFieldInfoIndex(String fieldName)
/*     */   {
/* 433 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.get(fieldName);
/* 434 */     if (info == null)
/*     */     {
/* 436 */       return -1;
/*     */     }
/*     */ 
/* 439 */     return info.m_index;
/*     */   }
/*     */ 
/*     */   public void setDateFormat(IdcDateFormat format)
/*     */   {
/* 444 */     this.m_dateFormat = format;
/*     */   }
/*     */ 
/*     */   public IdcDateFormat getDateFormat()
/*     */   {
/* 449 */     return this.m_dateFormat;
/*     */   }
/*     */ 
/*     */   public String getStringValue(int index)
/*     */   {
/* 454 */     if (!this.m_isRowPresent)
/*     */     {
/* 456 */       return "";
/*     */     }
/*     */ 
/* 459 */     Object value = this.m_cachedRow[index];
/*     */     String str;
/*     */     String str;
/* 461 */     if (value instanceof Date)
/*     */     {
/* 463 */       str = this.m_dateFormat.format(value);
/*     */     }
/*     */     else
/*     */     {
/* 467 */       str = (String)value;
/* 468 */       if ((this.m_manager.isTamino3()) && (str != null))
/*     */       {
/* 470 */         str = str.trim();
/*     */       }
/*     */     }
/*     */ 
/* 474 */     if ((str == null) || (str.equals(" ")))
/*     */     {
/* 476 */       str = "";
/*     */     }
/*     */ 
/* 479 */     return str;
/*     */   }
/*     */ 
/*     */   public String getStringValueByName(String name)
/*     */   {
/* 484 */     if ((this.m_isRowPresent == true) && (name != null))
/*     */     {
/* 486 */       FieldInfo fi = (FieldInfo)this.m_fieldMapping.get(name);
/* 487 */       if (fi != null)
/*     */       {
/* 489 */         return getStringValue(fi.m_index);
/*     */       }
/*     */     }
/*     */ 
/* 493 */     return null;
/*     */   }
/*     */ 
/*     */   public Date getDateValue(int index)
/*     */   {
/* 498 */     if (this.m_isRowPresent == true)
/*     */     {
/* 500 */       return (Date)this.m_cachedRow[index];
/*     */     }
/* 502 */     return null;
/*     */   }
/*     */ 
/*     */   public Date getDateValueByName(String name)
/*     */   {
/* 507 */     if ((this.m_isRowPresent == true) && (name != null))
/*     */     {
/* 509 */       FieldInfo fi = (FieldInfo)this.m_fieldMapping.get(name);
/* 510 */       if (fi != null)
/*     */       {
/* 512 */         return (Date)this.m_cachedRow[fi.m_index];
/*     */       }
/*     */     }
/* 515 */     return null;
/*     */   }
/*     */ 
/*     */   public InputStream getDataStream(String colName) throws DataException, IOException
/*     */   {
/* 520 */     String errMsg = null;
/* 521 */     InputStream iStream = null;
/* 522 */     int colType = -1;
/* 523 */     Exception e = null;
/* 524 */     FieldInfo info = new FieldInfo();
/*     */     try
/*     */     {
/* 527 */       boolean exists = getFieldInfo(colName, info);
/* 528 */       if (exists)
/*     */       {
/* 530 */         colType = info.m_type;
/* 531 */         switch (colType)
/*     */         {
/*     */         case 7:
/* 534 */           iStream = this.m_resultSet.getBinaryStream(colName);
/* 535 */           if (iStream == null)
/*     */           {
/* 537 */             iStream = new ByteArrayInputStream(new byte[0]); } break;
/*     */         case 6:
/* 541 */           iStream = this.m_resultSet.getAsciiStream(colName);
/* 542 */           if (iStream == null)
/*     */           {
/* 544 */             iStream = new ByteArrayInputStream(new byte[0]); } break;
/*     */         case 9:
/* 548 */           Blob blob = this.m_resultSet.getBlob(colName);
/* 549 */           if (blob != null)
/*     */           {
/* 551 */             iStream = blob.getBinaryStream();
/*     */           }
/*     */           else
/*     */           {
/* 555 */             iStream = new ByteArrayInputStream(new byte[0]);
/*     */           }
/* 557 */           break;
/*     */         case 10:
/* 560 */           Clob clob = this.m_resultSet.getClob(colName);
/* 561 */           if (clob != null)
/*     */           {
/* 563 */             iStream = clob.getAsciiStream();
/*     */           }
/*     */           else
/*     */           {
/* 567 */             iStream = new ByteArrayInputStream(new byte[0]);
/*     */           }
/* 569 */           break;
/*     */         case 8:
/*     */         default:
/* 572 */           errMsg = "csJdbcTypeStreamNotSupported";
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (SQLException se)
/*     */     {
/* 578 */       Report.trace(null, "Unable to retrieve stream for " + colName, se);
/*     */ 
/* 580 */       errMsg = "csJdbcDataStreamError";
/* 581 */       e = se;
/*     */     }
/* 583 */     if ((errMsg == null) && (iStream == null))
/*     */     {
/* 585 */       errMsg = "csJdbcDataStreamUnknownError";
/*     */     }
/* 587 */     if (errMsg != null)
/*     */     {
/* 589 */       String type = info.getTypeName();
/* 590 */       String err = LocaleUtils.encodeMessage(errMsg, null, colName, type);
/* 591 */       DataException de = new DataException(err);
/* 592 */       de.initCause(e);
/* 593 */       throw de;
/*     */     }
/* 595 */     return iStream;
/*     */   }
/*     */ 
/*     */   public Reader getCharacterReader(String colName) throws DataException, IOException
/*     */   {
/*     */     try
/*     */     {
/* 602 */       Reader reader = this.m_resultSet.getCharacterStream(colName);
/* 603 */       return reader;
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 607 */       String err = "csJdbcGetReaderError";
/* 608 */       throw new DataException(err, e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public Object updateBlob(String clmn, File file) throws DataException, IOException
/*     */   {
/* 614 */     InputStream fis = null;
/* 615 */     byte[] buf = (byte[])(byte[])this.m_bufferPool.getBuffer(16384, 0);
/* 616 */     Blob b = null;
/*     */     try
/*     */     {
/* 619 */       fis = new FileInputStream(file);
/* 620 */       b = this.m_resultSet.getBlob(clmn);
/* 621 */       b.truncate(0L);
/*     */ 
/* 623 */       OutputStream os = b.setBinaryStream(1L);
/*     */ 
/* 625 */       int nread = 0;
/* 626 */       while ((nread = fis.read(buf)) != -1)
/*     */       {
/* 628 */         os.write(buf, 0, nread);
/*     */       }
/* 630 */       os.flush();
/* 631 */       os.close();
/* 632 */       this.m_resultSet.updateBlob(clmn, b);
/* 633 */       this.m_resultSet.updateRow();
/*     */     }
/*     */     catch (SQLException se)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/*     */       String msg;
/* 642 */       this.m_bufferPool.releaseBuffer(buf);
/* 643 */       FileUtils.closeFiles(null, fis);
/*     */     }
/* 645 */     return b;
/*     */   }
/*     */ 
/*     */   public boolean next()
/*     */   {
/*     */     try
/*     */     {
/* 652 */       if (this.m_con == null)
/*     */       {
/* 654 */         return false;
/*     */       }
/*     */ 
/* 657 */       this.m_isRowPresent = this.m_resultSet.next();
/*     */ 
/* 660 */       if ((this.m_isRowPresent) && (1003 != this.m_resultSet.getType()))
/*     */       {
/* 662 */         this.m_isFirst = this.m_resultSet.isFirst();
/*     */       }
/*     */ 
/* 666 */       if (!this.m_isRowPresent)
/*     */       {
/* 668 */         return this.m_isRowPresent;
/*     */       }
/*     */ 
/* 671 */       cacheCurrentRow();
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 675 */       Report.trace(null, "JdbcResultSet.next:Unable to retrieve next row.", e);
/* 676 */       this.m_isRowPresent = false;
/*     */     }
/* 678 */     return this.m_isRowPresent;
/*     */   }
/*     */ 
/*     */   public boolean first()
/*     */   {
/* 683 */     if (this.m_isEmpty)
/*     */     {
/* 685 */       return false;
/*     */     }
/* 687 */     if (this.m_isFirst == true)
/*     */     {
/* 689 */       return true;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/* 694 */       if (this.m_resultSet.getType() != 1003)
/*     */       {
/* 696 */         this.m_isFirst = this.m_resultSet.first();
/* 697 */         if (this.m_isFirst)
/*     */         {
/* 699 */           this.m_isRowPresent = true;
/* 700 */           cacheCurrentRow();
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 706 */       Report.trace(null, "Unable to reposition result set to first row.", e);
/*     */ 
/* 708 */       this.m_isFirst = false;
/*     */     }
/*     */ 
/* 711 */     return this.m_isFirst;
/*     */   }
/*     */ 
/*     */   public int skip(int numRows)
/*     */   {
/* 716 */     if ((this.m_isEmpty) || (!this.m_isRowPresent) || (numRows == 0))
/*     */     {
/* 719 */       return 0;
/*     */     }
/*     */ 
/* 722 */     int numSkipped = 0;
/*     */     try
/*     */     {
/* 725 */       if (this.m_resultSet.getType() != 1003)
/*     */       {
/* 727 */         int rowNum = this.m_resultSet.getRow();
/* 728 */         boolean skipped = this.m_resultSet.relative(numRows);
/* 729 */         if (skipped)
/*     */         {
/* 731 */           numSkipped = numRows;
/*     */         }
/*     */         else
/*     */         {
/* 735 */           if (numRows > 0)
/*     */           {
/* 737 */             skipped = this.m_resultSet.last();
/*     */           }
/* 739 */           else if (numRows < 0)
/*     */           {
/* 741 */             skipped = this.m_resultSet.first();
/*     */           }
/* 743 */           if (skipped)
/*     */           {
/* 745 */             numSkipped = this.m_resultSet.getRow() - rowNum;
/*     */           }
/*     */           else
/*     */           {
/* 750 */             Report.trace(null, "Skip error. Unable to position to a valid row.", null);
/* 751 */             this.m_resultSet.absolute(rowNum);
/*     */           }
/*     */         }
/*     */ 
/* 755 */         if (skipped)
/*     */         {
/* 757 */           this.m_isRowPresent = true;
/*     */         }
/* 759 */         if (this.m_isRowPresent)
/*     */         {
/* 761 */           cacheCurrentRow();
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (SQLException e)
/*     */     {
/* 767 */       Report.trace(null, "JdbcResultSet.skip: Unable to skip. Error:", e);
/* 768 */       this.m_isRowPresent = false;
/*     */     }
/* 770 */     return numSkipped;
/*     */   }
/*     */ 
/*     */   protected void cacheCurrentRow()
/*     */     throws SQLException
/*     */   {
/* 777 */     int numColumns = this.m_fieldList.size();
/* 778 */     if (this.m_cachedRow == null)
/*     */     {
/* 780 */       this.m_cachedRow = new Object[numColumns];
/*     */     }
/* 782 */     for (int i = 0; i < numColumns; ++i)
/*     */     {
/* 784 */       FieldInfo info = (FieldInfo)this.m_fieldList.elementAt(i);
/* 785 */       switch (info.m_type)
/*     */       {
/*     */       case 5:
/* 789 */         Timestamp dte = null;
/* 790 */         if (this.m_manager.isTamino3())
/*     */         {
/* 792 */           int date = this.m_resultSet.getInt(i + 1);
/* 793 */           if (date != 0)
/*     */           {
/* 795 */             dte = new Timestamp(date * 1000L);
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 800 */           dte = this.m_resultSet.getTimestamp(i + 1);
/*     */         }
/* 802 */         this.m_cachedRow[i] = dte;
/*     */ 
/* 804 */         break;
/*     */       case 3:
/* 807 */         this.m_cachedRow[i] = this.m_resultSet.getString(i + 1);
/* 808 */         if ((this.m_cachedRow[i] == null) || (!this.m_cachedRow[i].equals("0.0")))
/*     */           continue;
/* 810 */         this.m_cachedRow[i] = "0"; break;
/*     */       case 7:
/*     */       case 9:
/*     */       case 10:
/* 819 */         this.m_cachedRow[i] = "";
/* 820 */         break;
/*     */       case 4:
/*     */       case 6:
/*     */       case 8:
/*     */       default:
/* 823 */         this.m_cachedRow[i] = this.m_resultSet.getString(i + 1);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean canRenameFields()
/*     */   {
/* 830 */     return true;
/*     */   }
/*     */ 
/*     */   public boolean renameField(String from, String to)
/*     */   {
/* 841 */     if ((from == null) || (from.length() == 0) || (to == null) || (to.length() == 0))
/*     */     {
/* 843 */       return false;
/*     */     }
/*     */ 
/* 846 */     if (this.m_fieldMapping.get(to) != null)
/*     */     {
/* 848 */       return false;
/*     */     }
/*     */ 
/* 851 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.remove(from);
/* 852 */     if (info == null)
/*     */     {
/* 854 */       return false;
/*     */     }
/*     */ 
/* 857 */     info.m_name = to;
/* 858 */     this.m_fieldMapping.put(to, info);
/* 859 */     return true;
/*     */   }
/*     */ 
/*     */   public void closeInternals()
/*     */   {
/*     */     try
/*     */     {
/* 866 */       if (this.m_con != null)
/*     */       {
/* 868 */         this.m_isEmpty = true;
/* 869 */         this.m_isRowPresent = false;
/* 870 */         this.m_resultSet.close();
/* 871 */         if (!this.m_statement instanceof PreparedStatement)
/*     */         {
/* 873 */           if (Report.m_verbose)
/*     */           {
/* 875 */             this.m_manager.debugMsg("Closing statement in closing internals");
/*     */           }
/* 877 */           this.m_statement.close();
/*     */         }
/* 879 */         this.m_manager.releaseAccess(this.m_con, false);
/*     */ 
/* 881 */         this.m_con = null;
/* 882 */         this.m_statement = null;
/* 883 */         this.m_resultSet = null;
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (Throwable t)
/*     */     {
/* 889 */       Report.trace(null, "Error cleaning up JDBC result set.", t);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 895 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 101215 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcResultSet
 * JD-Core Version:    0.5.4
 */