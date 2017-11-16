/*      */ package intradoc.data;
/*      */ 
/*      */ import intradoc.common.IdcAppendable;
/*      */ import intradoc.common.IdcDateFormat;
/*      */ import intradoc.common.LocaleResources;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.NumberUtils;
/*      */ import intradoc.common.ParseStringException;
/*      */ import intradoc.common.Report;
/*      */ import intradoc.common.ScriptObject;
/*      */ import intradoc.common.ServiceException;
/*      */ import intradoc.common.SystemUtils;
/*      */ import intradoc.common.Table;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.IOException;
/*      */ import java.io.Reader;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Hashtable;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class DataResultSet
/*      */   implements MutableResultSet, ScriptObject, Iterable<List<String>>
/*      */ {
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_APPEND_ONLY = 1;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_REPLACE_ONLY = 2;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_REPLACE_ONLY_FIRST_ROW_FOUND = 4;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_MERGE_FIELDS = 8;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_MERGE_USE_WILDCARD = 16;
/*      */ 
/*      */   @Deprecated
/*      */   public static final int F_MERGE_ALLOW_DUPLICATES = 1;
/*      */   protected IdcDateFormat[] m_dateFormat;
/*      */   protected boolean m_isMutable;
/*      */   protected Map<String, FieldInfo> m_fieldMapping;
/*      */   protected List<FieldInfo> m_fieldList;
/*      */   protected List<Vector> m_values;
/*      */   protected int m_currentRow;
/*      */   protected int m_numRows;
/*      */   protected boolean m_copyAborted;
/*      */   protected DataFormatter m_dataFormat;
/*      */   protected String m_dataFormatOptions;
/*      */   protected Map<Integer, ResultSetIndex> m_indices;
/*      */ 
/*      */   public DataResultSet()
/*      */   {
/*  130 */     reset();
/*      */   }
/*      */ 
/*      */   public DataResultSet(String[] fieldNames)
/*      */   {
/*  142 */     reset();
/*      */ 
/*  144 */     for (int i = 0; i < fieldNames.length; ++i)
/*      */     {
/*  146 */       FieldInfo info = new FieldInfo();
/*  147 */       info.m_name = fieldNames[i];
/*  148 */       info.m_index = i;
/*  149 */       this.m_fieldMapping.put(info.m_name, info);
/*  150 */       this.m_fieldList.add(info);
/*      */     }
/*      */   }
/*      */ 
/*      */   public DataResultSet(List fieldNames)
/*      */   {
/*  163 */     reset();
/*  164 */     int len = fieldNames.size();
/*  165 */     for (int i = 0; i < len; ++i)
/*      */     {
/*  167 */       FieldInfo info = new FieldInfo();
/*  168 */       info.m_name = fieldNames.get(i).toString();
/*  169 */       info.m_index = i;
/*  170 */       this.m_fieldMapping.put(info.m_name, info);
/*  171 */       this.m_fieldList.add(info);
/*      */     }
/*      */   }
/*      */ 
/*      */   public long getSupportedFeatures()
/*      */   {
/*  180 */     return -1L;
/*      */   }
/*      */ 
/*      */   public boolean isMutable()
/*      */   {
/*  185 */     return this.m_isMutable;
/*      */   }
/*      */ 
/*      */   public boolean hasRawObjects()
/*      */   {
/*  190 */     return false;
/*      */   }
/*      */ 
/*      */   public void setDateFormat(IdcDateFormat fmt)
/*      */   {
/*  195 */     if ((this.m_dateFormat[0] != null) && (!this.m_dateFormat[0].equals(fmt)) && (this.m_values.size() > 0))
/*      */     {
/*  197 */       changeDateFormat(fmt);
/*      */     }
/*  199 */     this.m_dateFormat[0] = fmt;
/*      */   }
/*      */ 
/*      */   public IdcDateFormat getDateFormat()
/*      */   {
/*  204 */     return this.m_dateFormat[0];
/*      */   }
/*      */ 
/*      */   private void changeDateFormat(IdcDateFormat fmt)
/*      */   {
/*  209 */     if ((this.m_dateFormat[0] == null) || (this.m_dateFormat[0].equals(fmt)) || (this.m_values.size() <= 0)) {
/*      */       return;
/*      */     }
/*  212 */     ArrayList dateIndexes = new ArrayList();
/*  213 */     for (int i = 0; i < this.m_fieldList.size(); ++i)
/*      */     {
/*  215 */       if (((FieldInfo)this.m_fieldList.get(i)).m_type != 5)
/*      */         continue;
/*  217 */       dateIndexes.add(Integer.valueOf(i));
/*      */     }
/*      */ 
/*  221 */     if (dateIndexes.size() <= 0)
/*      */       return;
/*  223 */     for (int r = 0; r < this.m_values.size(); ++r)
/*      */     {
/*  225 */       Vector v = (Vector)this.m_values.get(r);
/*  226 */       for (int i = 0; i < dateIndexes.size(); ++i)
/*      */       {
/*  228 */         int index = ((Integer)dateIndexes.get(i)).intValue();
/*  229 */         String dateStr = v.get(index).toString();
/*  230 */         if (dateStr == null) continue; if (dateStr.length() == 0) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */         try
/*      */         {
/*  236 */           Date dte = this.m_dateFormat[0].parseDate(dateStr);
/*  237 */           String newDateStr = fmt.format(dte);
/*  238 */           v.set(index, newDateStr);
/*      */         }
/*      */         catch (ParseStringException e)
/*      */         {
/*      */         }
/*      */       }
/*      */ 
/*  245 */       this.m_values.set(r, v);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean init(Table tble)
/*      */   {
/*  260 */     if (this.m_fieldList.size() > 0)
/*      */     {
/*  262 */       throw new AssertionError("DataResultSet is already initialized");
/*      */     }
/*  264 */     String[] colNames = tble.m_colNames;
/*  265 */     if (colNames == null)
/*      */     {
/*  267 */       return false;
/*      */     }
/*  269 */     for (int i = 0; i < colNames.length; ++i)
/*      */     {
/*  271 */       FieldInfo info = new FieldInfo();
/*  272 */       info.m_name = colNames[i];
/*  273 */       info.m_index = i;
/*  274 */       this.m_fieldMapping.put(info.m_name, info);
/*  275 */       this.m_fieldList.add(info);
/*      */     }
/*      */ 
/*  278 */     List rows = tble.m_rows;
/*  279 */     if (rows != null)
/*      */     {
/*  281 */       this.m_numRows = rows.size();
/*  282 */       for (int i = 0; i < this.m_numRows; ++i)
/*      */       {
/*  284 */         String[] row = (String[])(String[])rows.get(i);
/*  285 */         List values = createNewRowList(row.length);
/*  286 */         for (int j = 0; j < row.length; ++j)
/*      */         {
/*  288 */           values.add(row[j]);
/*      */         }
/*      */ 
/*  291 */         this.m_values.add(values);
/*      */       }
/*      */     }
/*      */ 
/*  295 */     return true;
/*      */   }
/*      */ 
/*      */   public void init(Reader reader, DataDecode decoder)
/*      */     throws IOException
/*      */   {
/*  310 */     initEx(reader, decoder, false);
/*      */   }
/*      */ 
/*      */   public void initEx(Reader reader, DataDecode decoder, boolean isHeaderOnly)
/*      */     throws IOException
/*      */   {
/*  328 */     this.m_currentRow = 0;
/*  329 */     this.m_numRows = 0;
/*      */ 
/*  331 */     readSimple((BufferedReader)reader, decoder, isHeaderOnly);
/*      */   }
/*      */ 
/*      */   public DataResultSet shallowClone()
/*      */   {
/*  339 */     DataResultSet rset = new DataResultSet();
/*      */ 
/*  354 */     initShallow(rset);
/*      */ 
/*  356 */     return rset;
/*      */   }
/*      */ 
/*      */   public void cloneFieldInfoInPlace()
/*      */   {
/*  361 */     List fieldList = new ArrayList();
/*  362 */     Map fieldMapping = new HashMap();
/*      */ 
/*  364 */     for (Object o : this.m_fieldList)
/*      */     {
/*  366 */       FieldInfo fi = (FieldInfo)o;
/*  367 */       FieldInfo newFi = new FieldInfo();
/*  368 */       newFi.copy(fi);
/*      */ 
/*  370 */       fieldList.add(newFi);
/*  371 */       fieldMapping.put(newFi.m_name, newFi);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void initShallow(DataResultSet rset)
/*      */   {
/*  383 */     rset.m_isMutable = false;
/*  384 */     rset.m_fieldMapping = this.m_fieldMapping;
/*  385 */     rset.m_fieldList = this.m_fieldList;
/*  386 */     rset.m_indices = this.m_indices;
/*  387 */     rset.m_values = cloneList(this.m_values);
/*  388 */     rset.m_numRows = this.m_values.size();
/*  389 */     rset.m_dateFormat = this.m_dateFormat;
/*      */   }
/*      */ 
/*      */   public List cloneList(List list)
/*      */   {
/*  397 */     List outList = null;
/*  398 */     if (list instanceof Vector)
/*      */     {
/*  400 */       Vector v = (Vector)list;
/*  401 */       outList = (List)v.clone();
/*      */     }
/*  403 */     else if (list instanceof ArrayList)
/*      */     {
/*  405 */       ArrayList a = (ArrayList)list;
/*  406 */       outList = (List)a.clone();
/*      */     }
/*      */ 
/*  409 */     outList.isEmpty();
/*  410 */     return outList;
/*      */   }
/*      */ 
/*      */   public Map cloneMap(Map map)
/*      */   {
/*  418 */     if (map == null)
/*      */     {
/*  420 */       return map;
/*      */     }
/*  422 */     Map outMap = null;
/*  423 */     if (map instanceof HashMap)
/*      */     {
/*  425 */       HashMap m = (HashMap)map;
/*  426 */       outMap = (Map)m.clone();
/*      */     }
/*  428 */     else if (map instanceof Hashtable)
/*      */     {
/*  430 */       Hashtable m = (Hashtable)map;
/*  431 */       outMap = (Hashtable)m.clone();
/*      */     }
/*      */ 
/*  434 */     outMap.isEmpty();
/*  435 */     return outMap;
/*      */   }
/*      */ 
/*      */   public List createNewRowList(int capacity)
/*      */   {
/*  444 */     return new IdcVector(capacity);
/*      */   }
/*      */ 
/*      */   public List createNewResultSetList(int capacity)
/*      */   {
/*  453 */     return new ArrayList(capacity);
/*      */   }
/*      */ 
/*      */   public void copy(ResultSet rset, int maxRows)
/*      */   {
/*  467 */     copyEx(rset, maxRows, false);
/*      */   }
/*      */ 
/*      */   public void copyEx(ResultSet rset, int maxRows, boolean useDbFormat)
/*      */   {
/*  472 */     if (rset == null)
/*      */     {
/*  474 */       return;
/*      */     }
/*      */ 
/*  477 */     if (rset == this)
/*      */     {
/*  479 */       return;
/*      */     }
/*  481 */     if (this.m_dateFormat[0] == null)
/*      */     {
/*  483 */       if (useDbFormat)
/*      */       {
/*  485 */         if (rset.hasRawObjects())
/*      */         {
/*  487 */           rset.setDateFormat(LocaleUtils.m_odbcDateFormat);
/*      */         }
/*  489 */         this.m_dateFormat[0] = LocaleUtils.m_odbcDateFormat;
/*      */       }
/*      */       else
/*      */       {
/*  493 */         this.m_dateFormat[0] = rset.getDateFormat();
/*      */       }
/*      */     }
/*      */ 
/*  497 */     int curRow = -1;
/*  498 */     DataResultSet drset = null;
/*  499 */     if (rset instanceof DataResultSet)
/*      */     {
/*  501 */       drset = (DataResultSet)rset;
/*  502 */       curRow = drset.getCurrentRow();
/*      */     }
/*      */ 
/*  505 */     copyFieldInfo(rset);
/*  506 */     int numFields = this.m_fieldList.size();
/*  507 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  509 */       List values = getResultSetValuesEx(rset, numFields, useDbFormat);
/*  510 */       addRow((Vector)values);
/*  511 */       if ((maxRows <= 0) || (maxRows != this.m_numRows))
/*      */         continue;
/*  513 */       this.m_copyAborted = true;
/*  514 */       return;
/*      */     }
/*      */ 
/*  518 */     if (curRow < 0) {
/*      */       return;
/*      */     }
/*  521 */     setCurrentRow(curRow);
/*  522 */     drset.setCurrentRow(curRow);
/*      */   }
/*      */ 
/*      */   protected List getResultSetValues(ResultSet rset, int numFields)
/*      */   {
/*  535 */     return getResultSetValuesEx(rset, numFields, false);
/*      */   }
/*      */ 
/*      */   protected List getResultSetValuesEx(ResultSet rset, int numFields, boolean useDbFormat)
/*      */   {
/*  540 */     List values = createNewRowList(numFields);
/*      */ 
/*  542 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/*  544 */       values.add("");
/*      */     }
/*      */ 
/*  547 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/*  549 */       FieldInfo info = (FieldInfo)this.m_fieldList.get(i);
/*  550 */       String v = "";
/*      */ 
/*  552 */       if ((useDbFormat == true) && (info.m_type == 5))
/*      */       {
/*  554 */         Date dte = rset.getDateValue(i);
/*  555 */         if (dte != null)
/*      */         {
/*  557 */           v = LocaleUtils.formatODBC(dte);
/*      */         }
/*      */       }
/*      */       else
/*      */       {
/*  562 */         v = rset.getStringValue(i);
/*      */       }
/*      */ 
/*  565 */       if ((v.length() > 0) && (info.m_index != i))
/*      */       {
/*  567 */         String preV = (String)values.get(info.m_index);
/*  568 */         if ((preV == null) || (preV.length() == 0))
/*      */         {
/*  570 */           values.set(info.m_index, v);
/*      */         }
/*      */       }
/*  573 */       values.set(i, v);
/*      */     }
/*      */ 
/*  576 */     return values;
/*      */   }
/*      */ 
/*      */   public void copy(ResultSet rset)
/*      */   {
/*  588 */     copy(rset, 0);
/*      */   }
/*      */ 
/*      */   public void copyFiltered(ResultSet rset, String key, ResultSetFilter filter)
/*      */   {
/*  604 */     copyFilteredEx(rset, key, filter, true);
/*      */   }
/*      */ 
/*      */   public void copySimpleFiltered(ResultSet rset, String key, String val, boolean ignorecase)
/*      */   {
/*  621 */     ResultSetFilter filter = createSimpleResultSetFilter(val, ignorecase);
/*  622 */     copyFiltered(rset, key, filter);
/*      */   }
/*      */ 
/*      */   public void copySimpleFiltered(ResultSet rset, String key, String val)
/*      */   {
/*  638 */     ResultSetFilter filter = createSimpleResultSetFilter(val);
/*  639 */     copyFiltered(rset, key, filter);
/*      */   }
/*      */ 
/*      */   public ResultSetFilter createSimpleResultSetFilter(String lookupVal)
/*      */   {
/*  653 */     SimpleResultSetFilter filter = new SimpleResultSetFilter(lookupVal);
/*  654 */     return filter;
/*      */   }
/*      */ 
/*      */   public ResultSetFilter createSimpleResultSetFilter(String lookupVal, boolean ignoreCase)
/*      */   {
/*  669 */     SimpleResultSetFilter filter = new SimpleResultSetFilter(lookupVal, ignoreCase);
/*  670 */     return filter;
/*      */   }
/*      */ 
/*      */   public ResultSetFilter createMaxNumResultSetFilter(int maxnum)
/*      */   {
/*  685 */     ResultSetFilter filter = new SimpleResultSetFilter(maxnum);
/*  686 */     return filter;
/*      */   }
/*      */ 
/*      */   public void copyFilteredEx(ResultSet rset, String key, ResultSetFilter filter, boolean startAtFirst)
/*      */   {
/*  703 */     copyFilteredDbFormatEx(rset, key, filter, startAtFirst, false);
/*      */   }
/*      */ 
/*      */   public void copyFilteredDbFormatEx(ResultSet rset, String key, ResultSetFilter filter, boolean startAtFirst, boolean useDbFormat)
/*      */   {
/*  720 */     if (rset != this)
/*      */     {
/*  722 */       copyFieldInfo(rset);
/*      */     }
/*      */ 
/*  725 */     if (startAtFirst)
/*      */     {
/*  727 */       rset.first();
/*      */     }
/*      */ 
/*  730 */     int numFields = this.m_fieldList.size();
/*      */ 
/*  732 */     List rowValues = createNewResultSetList(32);
/*  733 */     int numRows = 0;
/*      */ 
/*  735 */     FieldInfo fi = null;
/*  736 */     if (key != null)
/*      */     {
/*  738 */       fi = (FieldInfo)this.m_fieldMapping.get(key);
/*  739 */       if (fi == null)
/*      */       {
/*  741 */         return;
/*      */       }
/*      */     }
/*      */ 
/*  745 */     for (; rset.isRowPresent(); rset.next())
/*      */     {
/*  747 */       List values = getResultSetValuesEx(rset, numFields, useDbFormat);
/*  748 */       values = cloneList(values);
/*  749 */       String val = null;
/*  750 */       if (fi != null)
/*      */       {
/*  752 */         val = (String)values.get(fi.m_index);
/*      */       }
/*      */ 
/*  756 */       int retVal = filter.checkRow(val, numRows, (Vector)values);
/*      */ 
/*  758 */       if (retVal == -1)
/*      */       {
/*  760 */         this.m_copyAborted = true;
/*  761 */         break;
/*      */       }
/*  763 */       if (retVal != 1)
/*      */         continue;
/*  765 */       rowValues.add(values);
/*  766 */       ++numRows;
/*      */     }
/*      */ 
/*  770 */     this.m_values = rowValues;
/*  771 */     this.m_numRows = numRows;
/*      */   }
/*      */ 
/*      */   public Iterator<List<String>> iterator()
/*      */   {
/*  776 */     return new DataResultSetIterator(this);
/*      */   }
/*      */ 
/*      */   public Iterable<SimpleParameters> getSimpleParametersIterable()
/*      */   {
/*  781 */     return new DataResultSetIterableOnSimpleParameters(this);
/*      */   }
/*      */ 
/*      */   public boolean isCopyAborted()
/*      */   {
/*  790 */     return this.m_copyAborted;
/*      */   }
/*      */ 
/*      */   public void copyFieldInfo(ResultSet rset)
/*      */   {
/*  803 */     copyFieldInfoWithFlags(rset, 1);
/*      */   }
/*      */ 
/*      */   public void copyFieldInfoWithFlags(ResultSet rset, int flags)
/*      */   {
/*  818 */     if (rset == this)
/*      */     {
/*  820 */       return;
/*      */     }
/*      */ 
/*  823 */     reset();
/*  824 */     int numFields = rset.getNumFields();
/*  825 */     int newIndex = 0;
/*  826 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/*  828 */       FieldInfo info = new FieldInfo();
/*  829 */       rset.getIndexFieldInfo(i, info);
/*      */ 
/*  831 */       if ((flags & 0x1) == 0)
/*      */       {
/*  833 */         if (this.m_fieldMapping.get(info.m_name) != null)
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  842 */         info.m_index = (newIndex++);
/*      */       }
/*  844 */       this.m_fieldMapping.put(info.m_name, info);
/*  845 */       this.m_fieldList.add(info);
/*      */     }
/*      */ 
/*  849 */     this.m_dateFormat[0] = rset.getDateFormat();
/*      */   }
/*      */ 
/*      */   public void merge(String colKey, ResultSet rset, boolean replaceOnly)
/*      */     throws DataException
/*      */   {
/*  870 */     mergeEx(colKey, rset, replaceOnly, 0);
/*      */   }
/*      */ 
/*      */   public void mergeEx(String colKey, ResultSet rset, boolean replaceOnly, int maxRows) throws DataException
/*      */   {
/*  875 */     int flags = 4;
/*  876 */     if (replaceOnly)
/*      */     {
/*  878 */       flags |= 2;
/*      */     }
/*  880 */     mergeWithFlags(colKey, rset, flags, maxRows);
/*      */   }
/*      */ 
/*      */   public void mergeWithFlags(String colKey, ResultSet rset, int flags, int maxRows)
/*      */     throws DataException
/*      */   {
/*  886 */     ResultSetMerge rsetMerge = new ResultSetMerge(this, rset, flags);
/*  887 */     rsetMerge.m_colKey = colKey;
/*  888 */     rsetMerge.m_maxRows = maxRows;
/*      */     try
/*      */     {
/*  891 */       rsetMerge.merge();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  895 */       throw new DataException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void mergeDelete(String colKey, ResultSet rset, boolean isCaseSensitive)
/*      */     throws DataException
/*      */   {
/*  913 */     int flags = 8;
/*  914 */     if (!isCaseSensitive)
/*      */     {
/*  916 */       flags += 64;
/*      */     }
/*  918 */     ResultSetMerge rsetMerge = new ResultSetMerge(this, rset, flags);
/*  919 */     rsetMerge.m_colKey = colKey;
/*      */     try
/*      */     {
/*  922 */       rsetMerge.merge();
/*      */     }
/*      */     catch (ServiceException e)
/*      */     {
/*  926 */       throw new DataException(e, null);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void mergeFields(ResultSet rset)
/*      */   {
/*  939 */     Vector newFields = new IdcVector();
/*  940 */     int numFields = rset.getNumFields();
/*  941 */     FieldInfo source = new FieldInfo(); FieldInfo target = new FieldInfo();
/*  942 */     for (int fieldIndex = 0; fieldIndex < numFields; ++fieldIndex)
/*      */     {
/*  944 */       rset.getIndexFieldInfo(fieldIndex, source);
/*  945 */       if (getFieldInfo(source.m_name, target))
/*      */         continue;
/*  947 */       newFields.add(source);
/*  948 */       source = new FieldInfo();
/*      */     }
/*      */ 
/*  951 */     mergeFieldsWithFlags(newFields, 0);
/*      */   }
/*      */ 
/*      */   public Vector createEmptyRow()
/*      */   {
/*  960 */     int nfields = this.m_fieldList.size();
/*  961 */     List row = createNewRowList(nfields);
/*  962 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/*  964 */       row.add("");
/*      */     }
/*  966 */     return (Vector)row;
/*      */   }
/*      */ 
/*      */   public Vector createEmptyRowInitializedWithList(List startValues)
/*      */   {
/*  978 */     int nfields = this.m_fieldList.size();
/*  979 */     Vector row = (Vector)createNewRowList(nfields);
/*  980 */     row.addAll(startValues);
/*  981 */     int curLen = row.size();
/*  982 */     for (int i = curLen; i < nfields; ++i)
/*      */     {
/*  984 */       row.add("");
/*      */     }
/*  986 */     if (curLen > nfields)
/*      */     {
/*  988 */       row.setSize(nfields);
/*      */     }
/*  990 */     return row;
/*      */   }
/*      */ 
/*      */   public void createAndAddRowInitializedWithList(List startValues)
/*      */   {
/* 1001 */     Vector v = createEmptyRowInitializedWithList(startValues);
/* 1002 */     addRow(v);
/*      */   }
/*      */ 
/*      */   public List createEmptyRowAsList()
/*      */   {
/* 1011 */     int nfields = this.m_fieldList.size();
/* 1012 */     List row = createNewResultSetList(nfields);
/* 1013 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 1015 */       row.add("");
/*      */     }
/* 1017 */     return row;
/*      */   }
/*      */ 
/*      */   public Vector findRow(int colIndex, String val)
/*      */   {
/* 1031 */     List l = findRow(colIndex, val, 0, 2);
/* 1032 */     if (l == null)
/*      */     {
/* 1034 */       return null;
/*      */     }
/*      */ 
/* 1037 */     if (l instanceof Vector)
/*      */     {
/* 1039 */       return (Vector)l;
/*      */     }
/*      */ 
/* 1042 */     return new IdcVector(l);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Vector findRowEx(int colIndex, String val, boolean isIncreasing)
/*      */   {
/* 1051 */     int flags = 2;
/* 1052 */     if (!isIncreasing)
/*      */     {
/* 1054 */       flags |= 1;
/*      */     }
/*      */ 
/* 1057 */     List l = findRow(colIndex, val, this.m_currentRow, flags);
/* 1058 */     if (l == null)
/*      */     {
/* 1060 */       return null;
/*      */     }
/*      */ 
/* 1063 */     if (l instanceof Vector)
/*      */     {
/* 1065 */       return (Vector)l;
/*      */     }
/*      */ 
/* 1068 */     return new IdcVector(l);
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public Vector findNextRow(int colIndex, String value, int rowOffset, boolean incDirection)
/*      */   {
/* 1077 */     int flags = 2;
/* 1078 */     if (!incDirection)
/*      */     {
/* 1080 */       flags |= 1;
/*      */     }
/*      */ 
/* 1083 */     List l = findRow(colIndex, value, this.m_currentRow + rowOffset, flags);
/* 1084 */     if (l == null)
/*      */     {
/* 1086 */       return null;
/*      */     }
/*      */ 
/* 1089 */     if (l instanceof Vector)
/*      */     {
/* 1091 */       return (Vector)l;
/*      */     }
/*      */ 
/* 1094 */     return new IdcVector(l);
/*      */   }
/*      */ 
/*      */   public List findRow(int colIndex, String val, int startRow, int flags)
/*      */   {
/* 1109 */     List theRow = null;
/* 1110 */     boolean isBackwards = (flags & 0x1) != 0;
/* 1111 */     boolean isCaseInsensitive = (flags & 0x2) != 0;
/*      */ 
/* 1113 */     int increment = (isBackwards) ? -1 : 1;
/* 1114 */     int currentRow = startRow;
/*      */ 
/* 1117 */     ResultSetIndex index = (ResultSetIndex)this.m_indices.get(Integer.valueOf(colIndex));
/* 1118 */     if ((index != null) && 
/* 1120 */       (isCaseInsensitive))
/*      */     {
/* 1122 */       int indexFeatures = index.getSupportedFeatures();
/* 1123 */       if ((indexFeatures & 0x2) == 0)
/*      */       {
/* 1125 */         index = null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1130 */     if (index != null)
/*      */     {
/* 1133 */       int[] matchingRows = index.getRowNumbers(new Object[] { val });
/*      */       int tmpIndex;
/*      */       int tmpIndex;
/* 1135 */       if (isBackwards)
/*      */       {
/* 1137 */         tmpIndex = matchingRows.length - 1;
/*      */       }
/*      */       else
/*      */       {
/* 1141 */         tmpIndex = 0;
/*      */       }
/*      */ 
/* 1144 */       for (; (tmpIndex >= 0) && (tmpIndex < matchingRows.length); tmpIndex += increment)
/*      */       {
/* 1146 */         if ((((!isBackwards) || (matchingRows[tmpIndex] > startRow))) && (((isBackwards) || (matchingRows[tmpIndex] < startRow)))) {
/*      */           continue;
/*      */         }
/* 1149 */         this.m_currentRow = matchingRows[tmpIndex];
/* 1150 */         theRow = getCurrentRowValues();
/* 1151 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/* 1158 */       for (; (currentRow >= 0) && (currentRow < this.m_numRows); currentRow += increment)
/*      */       {
/* 1160 */         Vector v = getRowValues(currentRow);
/*      */ 
/* 1162 */         String colVal = (String)v.elementAt(colIndex);
/* 1163 */         if (colVal == null)
/*      */         {
/* 1166 */           colVal = "";
/*      */         }
/*      */ 
/* 1169 */         boolean foundRow = false;
/* 1170 */         if (isCaseInsensitive)
/*      */         {
/* 1172 */           foundRow = colVal.equalsIgnoreCase(val);
/*      */         }
/*      */         else
/*      */         {
/* 1176 */           foundRow = colVal.equals(val);
/*      */         }
/*      */ 
/* 1179 */         if (!foundRow)
/*      */           continue;
/* 1181 */         this.m_currentRow = currentRow;
/* 1182 */         theRow = v;
/* 1183 */         break;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1188 */     return theRow;
/*      */   }
/*      */ 
/*      */   public List[] findRows(int colIndex, String val, int startRow, int flags)
/*      */   {
/* 1203 */     int currentRow = this.m_currentRow;
/* 1204 */     List l = new ArrayList();
/* 1205 */     boolean isBackwards = (flags & 0x1) != 0;
/* 1206 */     boolean isCaseInsensitive = (flags & 0x2) != 0;
/*      */ 
/* 1209 */     ResultSetIndex index = (ResultSetIndex)this.m_indices.get(Integer.valueOf(colIndex));
/* 1210 */     if ((index != null) && 
/* 1212 */       (isCaseInsensitive))
/*      */     {
/* 1214 */       int indexFeatures = index.getSupportedFeatures();
/* 1215 */       if ((indexFeatures & 0x2) == 0)
/*      */       {
/* 1217 */         index = null;
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1222 */     if (index != null)
/*      */     {
/* 1225 */       int[] matchingRows = index.getRowNumbers(new Object[] { val });
/*      */       int tmpIndex;
/*      */       int tmpIndex;
/* 1227 */       if (isBackwards)
/*      */       {
/* 1229 */         tmpIndex = matchingRows.length - 1;
/*      */       }
/*      */       else
/*      */       {
/* 1233 */         tmpIndex = 0;
/*      */       }
/*      */ 
/* 1236 */       int increment = (isBackwards) ? -1 : 1;
/* 1237 */       for (; (tmpIndex >= 0) && (tmpIndex < matchingRows.length); tmpIndex += increment)
/*      */       {
/* 1239 */         if ((((!isBackwards) || (matchingRows[tmpIndex] > startRow))) && (((isBackwards) || (matchingRows[tmpIndex] < startRow)))) {
/*      */           continue;
/*      */         }
/* 1242 */         l.add(this.m_values.get(matchingRows[tmpIndex]));
/*      */       }
/*      */ 
/*      */     }
/*      */     else
/*      */     {
/*      */       while (true)
/*      */       {
/* 1251 */         List row = findRow(colIndex, val, startRow, flags);
/* 1252 */         if (row == null)
/*      */         {
/*      */           break;
/*      */         }
/*      */ 
/* 1257 */         l.add(row);
/*      */ 
/* 1259 */         if ((flags & 0x1) != 0)
/*      */         {
/* 1261 */           startRow = this.m_currentRow - 1;
/*      */         }
/*      */         else
/*      */         {
/* 1265 */           startRow = this.m_currentRow + 1;
/*      */         }
/*      */       }
/*      */     }
/*      */ 
/* 1270 */     this.m_currentRow = currentRow;
/* 1271 */     List[] arr = new List[l.size()];
/* 1272 */     l.toArray(arr);
/*      */ 
/* 1274 */     return arr;
/*      */   }
/*      */ 
/*      */   public void fillField(int colIndex, String value)
/*      */   {
/* 1286 */     for (int row = 0; row < this.m_numRows; ++row)
/*      */     {
/* 1288 */       Vector v = getRowValues(row);
/* 1289 */       v.setElementAt(value, colIndex);
/*      */     }
/*      */   }
/*      */ 
/*      */   @Deprecated
/*      */   public void appendFields(Vector finfo)
/*      */   {
/* 1314 */     int flags = 3;
/* 1315 */     mergeFieldsWithFlags(finfo, flags);
/*      */   }
/*      */ 
/*      */   public void mergeFieldsWithFlags(List finfo, int flags)
/*      */   {
/* 1331 */     int addlen = finfo.size();
/* 1332 */     int oldlen = this.m_fieldList.size();
/* 1333 */     int numAdded = 0;
/* 1334 */     for (int i = 0; i < addlen; ++i)
/*      */     {
/* 1336 */       FieldInfo f = new FieldInfo();
/* 1337 */       Object o = finfo.get(i);
/* 1338 */       FieldInfo nf = null;
/* 1339 */       String name = null;
/* 1340 */       if (o instanceof FieldInfo)
/*      */       {
/* 1342 */         nf = (FieldInfo)o;
/* 1343 */         name = nf.m_name;
/* 1344 */         f.copy(nf);
/*      */       }
/*      */       else
/*      */       {
/* 1348 */         name = o.toString();
/* 1349 */         f.m_name = name;
/*      */       }
/* 1351 */       if (getFieldInfo(f.m_name, f))
/*      */       {
/* 1353 */         if ((nf != null) && 
/* 1355 */           ((flags & 0x2) != 0))
/*      */         {
/* 1358 */           nf.m_index = f.m_index;
/*      */         }
/*      */ 
/* 1361 */         if (0 == (flags & 0x1)) {
/*      */           continue;
/*      */         }
/*      */ 
/*      */       }
/*      */ 
/* 1367 */       f.m_index = (oldlen + numAdded++);
/* 1368 */       if ((nf != null) && 
/* 1370 */         ((flags & 0x2) != 0))
/*      */       {
/* 1373 */         nf.m_index = f.m_index;
/*      */       }
/*      */ 
/* 1377 */       this.m_fieldList.add(f);
/* 1378 */       this.m_fieldMapping.put(f.m_name, f);
/*      */     }
/*      */ 
/* 1381 */     String empty = "";
/* 1382 */     int nrows = this.m_values.size();
/* 1383 */     int newlen = this.m_fieldList.size();
/* 1384 */     for (int j = 0; j < nrows; ++j)
/*      */     {
/* 1386 */       List v = (List)this.m_values.get(j);
/* 1387 */       int start = v.size();
/* 1388 */       for (int k = start; k < newlen; ++k)
/*      */       {
/* 1390 */         v.add(empty);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void mergeFields(DataResultSet rset)
/*      */   {
/* 1403 */     List finfo = rset.m_fieldList;
/* 1404 */     int addlen = finfo.size();
/* 1405 */     int oldlen = this.m_fieldList.size();
/* 1406 */     int newlen = oldlen;
/* 1407 */     for (int i = 0; i < addlen; ++i)
/*      */     {
/* 1409 */       FieldInfo newInfo = (FieldInfo)finfo.get(i);
/*      */ 
/* 1412 */       FieldInfo info = new FieldInfo();
/* 1413 */       if (getFieldInfo(newInfo.m_name, info))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/* 1418 */       FieldInfo f = new FieldInfo();
/* 1419 */       f.copy(newInfo);
/* 1420 */       f.m_index = newlen;
/* 1421 */       this.m_fieldList.add(f);
/* 1422 */       this.m_fieldMapping.put(f.m_name, f);
/* 1423 */       ++newlen;
/*      */     }
/*      */ 
/* 1426 */     String empty = "";
/* 1427 */     int nrows = this.m_values.size();
/* 1428 */     for (int j = 0; j < nrows; ++j)
/*      */     {
/* 1430 */       List v = (List)this.m_values.get(j);
/* 1431 */       int start = v.size();
/* 1432 */       for (int k = start; k < newlen; ++k)
/*      */       {
/* 1434 */         v.add(empty);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public void removeFields(String[] fieldNames)
/*      */   {
/* 1444 */     boolean[] removeIt = new boolean[this.m_fieldList.size()];
/* 1445 */     for (int i = 0; i < removeIt.length; ++i)
/*      */     {
/* 1447 */       removeIt[i] = false;
/*      */     }
/* 1449 */     for (int i = 0; i < fieldNames.length; ++i)
/*      */     {
/* 1451 */       FieldInfo info = (FieldInfo)this.m_fieldMapping.get(fieldNames[i]);
/* 1452 */       if ((info == null) || (info.m_index < 0) || (info.m_index >= removeIt.length))
/*      */         continue;
/* 1454 */       removeIt[info.m_index] = true;
/*      */     }
/*      */ 
/* 1457 */     removeColumns(removeIt, false);
/*      */   }
/*      */ 
/*      */   protected void removeColumns(boolean[] removeIt, boolean isDuplicate)
/*      */   {
/* 1464 */     int nrows = this.m_values.size();
/* 1465 */     for (int i = removeIt.length - 1; i >= 0; --i)
/*      */     {
/* 1467 */       if (removeIt[i] == 0)
/*      */         continue;
/* 1469 */       for (int j = 0; j < nrows; ++j)
/*      */       {
/* 1471 */         Vector v = (Vector)this.m_values.get(j);
/* 1472 */         v.removeElementAt(i);
/*      */       }
/* 1474 */       FieldInfo info = (FieldInfo)this.m_fieldList.get(i);
/* 1475 */       this.m_fieldList.remove(i);
/* 1476 */       if (!isDuplicate)
/*      */       {
/* 1478 */         this.m_fieldMapping.remove(info.m_name);
/*      */       }
/* 1480 */       int size = this.m_fieldList.size();
/* 1481 */       for (int j = i; j < size; ++j)
/*      */       {
/* 1483 */         info = (FieldInfo)this.m_fieldList.get(j);
/* 1484 */         info.m_index -= 1;
/*      */       }
/* 1486 */       this.m_indices.remove(Integer.valueOf(i));
/*      */     }
/*      */   }
/*      */ 
/*      */   public void deDuplicate()
/*      */   {
/* 1498 */     Map map = new HashMap();
/* 1499 */     int num = this.m_fieldList.size();
/* 1500 */     boolean[] removeIt = new boolean[num];
/* 1501 */     for (int i = 0; i < num; ++i)
/*      */     {
/* 1503 */       FieldInfo fi = (FieldInfo)this.m_fieldList.get(i);
/* 1504 */       String field = fi.m_name;
/*      */ 
/* 1506 */       FieldInfo info = (FieldInfo)map.get(field);
/* 1507 */       if (info != null)
/*      */       {
/* 1509 */         removeIt[i] = true;
/*      */       }
/*      */       else
/*      */       {
/* 1513 */         removeIt[i] = false;
/* 1514 */         map.put(field, fi);
/*      */       }
/*      */ 
/*      */     }
/*      */ 
/* 1519 */     removeColumns(removeIt, true);
/*      */   }
/*      */ 
/*      */   public boolean renameField(String from, String to)
/*      */   {
/* 1533 */     if ((from == null) || (from.length() == 0) || (to == null) || (to.length() == 0))
/*      */     {
/* 1535 */       return false;
/*      */     }
/*      */ 
/* 1538 */     if (this.m_fieldMapping.get(to) != null)
/*      */     {
/* 1540 */       return false;
/*      */     }
/*      */ 
/* 1543 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.remove(from);
/* 1544 */     if (info == null)
/*      */     {
/* 1546 */       return false;
/*      */     }
/*      */ 
/* 1549 */     info.m_name = to;
/* 1550 */     this.m_fieldMapping.put(to, info);
/* 1551 */     return true;
/*      */   }
/*      */ 
/*      */   public void reset()
/*      */   {
/* 1559 */     this.m_dateFormat = new IdcDateFormat[1];
/* 1560 */     this.m_dateFormat[0] = null;
/*      */ 
/* 1562 */     this.m_isMutable = true;
/* 1563 */     this.m_fieldMapping = new HashMap();
/* 1564 */     this.m_fieldList = new ArrayList();
/* 1565 */     this.m_values = createNewResultSetList(32);
/* 1566 */     this.m_indices = new HashMap();
/*      */ 
/* 1568 */     this.m_currentRow = 0;
/* 1569 */     this.m_numRows = 0;
/* 1570 */     this.m_copyAborted = false;
/*      */   }
/*      */ 
/*      */   public void removeAll()
/*      */   {
/* 1578 */     this.m_values = createNewResultSetList(32);
/* 1579 */     this.m_currentRow = 0;
/* 1580 */     this.m_numRows = 0;
/*      */   }
/*      */ 
/*      */   public void readSimple(BufferedReader bReader, DataDecode decoder, boolean isHeaderOnly)
/*      */     throws IOException
/*      */   {
/* 1601 */     int numFields = readHeader(bReader, decoder);
/*      */ 
/* 1604 */     this.m_values = createNewResultSetList(32);
/*      */ 
/* 1606 */     if (isHeaderOnly)
/*      */     {
/* 1608 */       return;
/*      */     }
/*      */ 
/* 1612 */     String line = null;
/* 1613 */     boolean foundAt = false;
/*      */ 
/* 1615 */     while ((line = bReader.readLine()) != null)
/*      */     {
/* 1617 */       if ((line.length() > 0) && (line.charAt(0) == '@'))
/*      */       {
/* 1619 */         foundAt = true;
/* 1620 */         break;
/*      */       }
/* 1622 */       IdcVector values = new IdcVector();
/* 1623 */       String value = decoder.decode(line);
/* 1624 */       values.add(value);
/* 1625 */       for (int j = 1; j < numFields; ++j)
/*      */       {
/* 1627 */         line = bReader.readLine();
/* 1628 */         if (line == null)
/*      */         {
/* 1630 */           String msg = LocaleUtils.encodeMessage("syParsingErrorAtRow", null, "" + this.m_numRows, "" + j);
/*      */ 
/* 1632 */           throw new IOException(msg);
/*      */         }
/* 1634 */         values.add(decoder.decode(line));
/*      */       }
/* 1636 */       addRow(values);
/*      */     }
/*      */ 
/* 1640 */     if ((foundAt) && (line.startsWith("@end")))
/*      */       return;
/* 1642 */     throw new IOException("!syMalFormedResultSet");
/*      */   }
/*      */ 
/*      */   public int readHeader(BufferedReader bReader, DataDecode decoder)
/*      */     throws IOException
/*      */   {
/* 1659 */     String line = bReader.readLine();
/* 1660 */     int numFields = Integer.parseInt(line);
/* 1661 */     this.m_fieldMapping = new HashMap();
/* 1662 */     this.m_fieldList = new ArrayList();
/* 1663 */     this.m_indices = new HashMap();
/* 1664 */     for (int i = 0; i < numFields; ++i)
/*      */     {
/* 1666 */       line = bReader.readLine();
/* 1667 */       String fieldData = decoder.decode(line);
/* 1668 */       FieldInfo info = createFieldInfoFromFieldData(fieldData);
/* 1669 */       info.m_index = i;
/* 1670 */       this.m_fieldMapping.put(info.m_name, info);
/* 1671 */       this.m_fieldList.add(info);
/*      */     }
/*      */ 
/* 1674 */     return numFields;
/*      */   }
/*      */ 
/*      */   protected FieldInfo createFieldInfoFromFieldData(String fieldData)
/*      */   {
/* 1679 */     FieldInfo info = new FieldInfo();
/* 1680 */     int index = fieldData.indexOf(" ");
/* 1681 */     if (index > 0)
/*      */     {
/* 1683 */       info.m_name = fieldData.substring(0, index);
/* 1684 */       fieldData = fieldData.substring(index).trim();
/*      */ 
/* 1688 */       if (fieldData.length() > 0)
/*      */       {
/* 1690 */         int fieldType = getFieldType(fieldData);
/* 1691 */         if (fieldType > 0)
/*      */         {
/* 1693 */           info.m_type = fieldType;
/*      */         }
/*      */ 
/* 1696 */         if (((fieldType >= 1) && (fieldType <= 7)) || (fieldType == 11))
/*      */         {
/* 1699 */           fieldData = fieldData.substring(String.valueOf(fieldType).length()).trim();
/*      */ 
/* 1701 */           if (fieldData.length() > 0)
/*      */           {
/* 1703 */             if (fieldType == 11)
/*      */             {
/* 1705 */               int decimalSeparator = fieldData.indexOf(44);
/* 1706 */               String precisionStr = fieldData;
/* 1707 */               String scaleStr = null;
/* 1708 */               if (decimalSeparator > 0) {
/* 1709 */                 precisionStr = fieldData.substring(0, decimalSeparator).trim();
/*      */ 
/* 1711 */                 if (fieldData.length() > decimalSeparator + 1)
/*      */                 {
/* 1713 */                   scaleStr = fieldData.substring(decimalSeparator + 1).trim();
/*      */                 }
/*      */               }
/*      */ 
/* 1717 */               info.m_maxLen = NumberUtils.parseInteger(precisionStr, 1);
/* 1718 */               info.m_scale = NumberUtils.parseInteger(scaleStr, 1);
/*      */             }
/*      */             else
/*      */             {
/* 1722 */               info.m_maxLen = NumberUtils.parseInteger(fieldData, 0);
/*      */             }
/*      */ 
/*      */           }
/*      */ 
/* 1727 */           if (info.m_maxLen > 0) {
/* 1728 */             info.m_isFixedLen = true;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/* 1735 */       info.m_name = fieldData;
/*      */     }
/* 1737 */     return info;
/*      */   }
/*      */ 
/*      */   protected int getFieldType(String fieldData) {
/* 1741 */     char typeChar = fieldData.charAt(0);
/* 1742 */     String typeStr = String.valueOf(typeChar);
/* 1743 */     if (fieldData.length() > 1) {
/* 1744 */       typeChar = fieldData.charAt(1);
/* 1745 */       if ((typeChar >= '0') && (typeChar <= '9')) {
/* 1746 */         typeStr = typeStr + String.valueOf(typeChar);
/*      */       }
/*      */     }
/* 1749 */     int fieldType = Integer.parseInt(typeStr);
/* 1750 */     return fieldType;
/*      */   }
/*      */ 
/*      */   public int getCurrentRow()
/*      */   {
/* 1758 */     return this.m_currentRow;
/*      */   }
/*      */ 
/*      */   public void setCurrentRow(int currentRow)
/*      */   {
/* 1766 */     this.m_currentRow = currentRow;
/*      */   }
/*      */ 
/*      */   public int getNumRows()
/*      */   {
/* 1774 */     return this.m_numRows;
/*      */   }
/*      */ 
/*      */   public Vector getRowValues(int index)
/*      */   {
/* 1784 */     return (Vector)this.m_values.get(index);
/*      */   }
/*      */ 
/*      */   public List getRowAsList(int index)
/*      */   {
/* 1795 */     List list = (List)this.m_values.get(index);
/* 1796 */     return new ArrayList(list);
/*      */   }
/*      */ 
/*      */   public Vector getCurrentRowValues()
/*      */   {
/* 1805 */     if ((this.m_currentRow >= this.m_values.size()) || (this.m_currentRow < 0))
/*      */     {
/* 1808 */       return null;
/*      */     }
/*      */ 
/* 1812 */     return (Vector)this.m_values.get(this.m_currentRow);
/*      */   }
/*      */ 
/*      */   public List getCurrentRowAsList()
/*      */   {
/* 1821 */     if ((this.m_currentRow >= this.m_values.size()) || (this.m_currentRow < 0))
/*      */     {
/* 1824 */       return null;
/*      */     }
/* 1826 */     List list = (List)this.m_values.get(this.m_currentRow);
/* 1827 */     return new ArrayList(list);
/*      */   }
/*      */ 
/*      */   public Properties getCurrentRowProps()
/*      */   {
/* 1835 */     Vector values = getCurrentRowValues();
/* 1836 */     if (values == null)
/*      */     {
/* 1838 */       return null;
/*      */     }
/*      */ 
/* 1841 */     Properties props = new Properties();
/* 1842 */     populateMapWithValues(props, values, 1);
/* 1843 */     return props;
/*      */   }
/*      */ 
/*      */   public Map getCurrentRowMap()
/*      */   {
/* 1852 */     Vector values = getCurrentRowValues();
/* 1853 */     if (values == null)
/*      */     {
/* 1855 */       return null;
/*      */     }
/*      */ 
/* 1858 */     HashMap map = new HashMap();
/* 1859 */     populateMapWithValues(map, values, 1);
/* 1860 */     return map;
/*      */   }
/*      */ 
/*      */   public void populateMapWithValues(Map m, Vector values, int flags)
/*      */   {
/* 1868 */     int nfields = this.m_fieldList.size();
/* 1869 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 1871 */       FieldInfo info = (FieldInfo)this.m_fieldList.get(i);
/* 1872 */       Object val = values.get(info.m_index);
/* 1873 */       if (((flags & 0x1) != 0) && (!val instanceof String))
/*      */       {
/* 1875 */         val = val.toString();
/*      */       }
/*      */ 
/* 1878 */       m.put(info.m_name, val);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setRowValues(Vector vect, int i)
/*      */   {
/* 1889 */     if (vect.size() != this.m_fieldList.size())
/*      */     {
/* 1891 */       throw new Error("!syFailedSetRowValues");
/*      */     }
/* 1893 */     this.m_values.set(i, vect);
/*      */   }
/*      */ 
/*      */   public void setRowWithList(List list, int i)
/*      */   {
/* 1903 */     if (list.size() != this.m_fieldList.size())
/*      */     {
/* 1905 */       throw new Error("!syFailedSetRowValues");
/*      */     }
/*      */ 
/* 1910 */     Vector vfinfo = new IdcVector(list);
/* 1911 */     this.m_values.set(i, vfinfo);
/*      */   }
/*      */ 
/*      */   public void setCurrentValue(int index, String val)
/*      */     throws DataException
/*      */   {
/* 1922 */     List v = getCurrentRowValues();
/* 1923 */     if (v == null)
/*      */     {
/* 1925 */       throw new DataException("!syInvalidRow");
/*      */     }
/* 1927 */     if ((index < 0) || (index >= v.size()))
/*      */     {
/* 1929 */       throw new DataException("!syInvalidColumnIndex");
/*      */     }
/* 1931 */     v.set(index, val);
/*      */   }
/*      */ 
/*      */   public void deleteRow(int index)
/*      */   {
/* 1939 */     this.m_values.remove(index);
/* 1940 */     this.m_numRows = this.m_values.size();
/* 1941 */     if (this.m_currentRow <= this.m_numRows)
/*      */       return;
/* 1943 */     this.m_currentRow = this.m_numRows;
/*      */   }
/*      */ 
/*      */   public boolean deleteCurrentRow()
/*      */   {
/* 1953 */     if (!isRowPresent())
/* 1954 */       return false;
/* 1955 */     deleteRow(this.m_currentRow);
/* 1956 */     return true;
/*      */   }
/*      */ 
/*      */   public void addRow(Vector vect)
/*      */   {
/* 1966 */     if (vect.size() != this.m_fieldList.size())
/*      */     {
/* 1968 */       throw new Error("!syFailedAppendRow");
/*      */     }
/*      */ 
/* 1971 */     this.m_values.add(vect);
/* 1972 */     this.m_numRows = this.m_values.size();
/*      */   }
/*      */ 
/*      */   public void addRowWithList(List list)
/*      */   {
/* 1982 */     if (list.size() != this.m_fieldList.size())
/*      */     {
/* 1984 */       throw new Error("!syFailedAppendRow");
/*      */     }
/*      */ 
/* 1987 */     Vector v = new IdcVector(list);
/* 1988 */     this.m_values.add(v);
/* 1989 */     this.m_numRows = this.m_values.size();
/*      */   }
/*      */ 
/*      */   public void insertRowAt(Vector vect, int i)
/*      */   {
/* 1999 */     if (vect.size() != this.m_fieldList.size())
/*      */     {
/* 2001 */       throw new Error("!syFailedInsertRow");
/*      */     }
/* 2003 */     this.m_values.add(i, vect);
/* 2004 */     this.m_numRows = this.m_values.size();
/*      */   }
/*      */ 
/*      */   public void insertRowWithListAt(List list, int i)
/*      */   {
/* 2014 */     if (list.size() != this.m_fieldList.size())
/*      */     {
/* 2016 */       throw new Error("!syFailedInsertRow");
/*      */     }
/* 2018 */     Vector v = new IdcVector(list);
/* 2019 */     this.m_values.add(i, v);
/* 2020 */     this.m_numRows = this.m_values.size();
/*      */   }
/*      */ 
/*      */   public Vector createRow(Parameters params)
/*      */     throws DataException
/*      */   {
/* 2033 */     Vector retVal = new IdcVector();
/* 2034 */     retVal.setSize(this.m_fieldList.size());
/* 2035 */     populateListWithValues(retVal, params, 0);
/* 2036 */     return retVal;
/*      */   }
/*      */ 
/*      */   public List createRowAsList(Parameters params)
/*      */     throws DataException
/*      */   {
/* 2049 */     ArrayList retVal = new ArrayList();
/* 2050 */     retVal.ensureCapacity(this.m_fieldList.size());
/* 2051 */     populateListWithValues(retVal, params, 0);
/* 2052 */     return retVal;
/*      */   }
/*      */ 
/*      */   public void populateListWithValues(List l, Parameters params, int flags)
/*      */     throws DataException
/*      */   {
/* 2066 */     int nfields = this.m_fieldList.size();
/* 2067 */     int lSize = l.size();
/*      */ 
/* 2069 */     for (int i = 0; i < nfields; ++i)
/*      */     {
/* 2071 */       String temp = null;
/* 2072 */       if (params != null)
/*      */       {
/* 2074 */         FieldInfo info = (FieldInfo)this.m_fieldList.get(i);
/* 2075 */         temp = params.get(info.m_name);
/*      */       }
/* 2077 */       if (temp == null)
/*      */       {
/* 2079 */         temp = "";
/*      */       }
/* 2081 */       if (i < lSize)
/*      */       {
/* 2083 */         l.set(i, temp);
/*      */       }
/*      */       else
/*      */       {
/* 2087 */         l.add(temp);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public Workspace getWorkspace()
/*      */   {
/* 2101 */     return null;
/*      */   }
/*      */ 
/*      */   public int getNumFields()
/*      */   {
/* 2111 */     return this.m_fieldList.size();
/*      */   }
/*      */ 
/*      */   public boolean isEmpty()
/*      */   {
/* 2123 */     return this.m_values.size() == 0;
/*      */   }
/*      */ 
/*      */   public boolean isRowPresent()
/*      */   {
/* 2138 */     return (this.m_currentRow >= 0) && (this.m_currentRow < this.m_values.size());
/*      */   }
/*      */ 
/*      */   public String getFieldName(int index)
/*      */   {
/* 2151 */     FieldInfo info = (FieldInfo)this.m_fieldList.get(index);
/* 2152 */     if (info != null)
/*      */     {
/* 2154 */       return info.m_name;
/*      */     }
/* 2156 */     return null;
/*      */   }
/*      */ 
/*      */   public boolean getFieldInfo(String fieldName, FieldInfo fieldInfo)
/*      */   {
/* 2168 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.get(fieldName);
/* 2169 */     if (info == null)
/*      */     {
/* 2171 */       return false;
/*      */     }
/*      */ 
/* 2174 */     if (fieldInfo != null)
/*      */     {
/* 2176 */       fieldInfo.copy(info);
/*      */     }
/* 2178 */     return true;
/*      */   }
/*      */ 
/*      */   public void getFieldInfoByIndex(int index, FieldInfo fieldInfo)
/*      */   {
/* 2188 */     FieldInfo info = (FieldInfo)this.m_fieldList.get(index);
/*      */ 
/* 2190 */     fieldInfo.copy(info);
/*      */   }
/*      */ 
/*      */   public int getFieldInfoIndex(String fieldName)
/*      */   {
/* 2201 */     FieldInfo info = (FieldInfo)this.m_fieldMapping.get(fieldName);
/* 2202 */     if (info == null)
/*      */     {
/* 2204 */       return -1;
/*      */     }
/*      */ 
/* 2207 */     return info.m_index;
/*      */   }
/*      */ 
/*      */   public void getIndexFieldInfo(int index, FieldInfo fieldInfo)
/*      */   {
/* 2218 */     FieldInfo info = (FieldInfo)this.m_fieldList.get(index);
/* 2219 */     fieldInfo.copy(info);
/*      */   }
/*      */ 
/*      */   public String getStringValue(int index)
/*      */   {
/* 2230 */     List v = (List)this.m_values.get(this.m_currentRow);
/* 2231 */     Object value = v.get(index);
/* 2232 */     if (value == null)
/*      */     {
/* 2234 */       if (SystemUtils.m_isDevelopmentEnvironment)
/*      */       {
/* 2236 */         throw new AssertionError("Invalid NULL value retrieved in ResultSet");
/*      */       }
/* 2238 */       if (SystemUtils.m_verbose)
/*      */       {
/* 2240 */         Throwable t = new Throwable("Invalid NULL value retrieved in ResultSet");
/* 2241 */         Report.debug(null, null, t);
/*      */       }
/*      */       else
/*      */       {
/* 2245 */         Report.trace(null, "Invalid NULL value retrieved in ResultSet", null);
/*      */       }
/* 2247 */       return "";
/*      */     }
/*      */ 
/* 2250 */     return value.toString();
/*      */   }
/*      */ 
/*      */   public String getStringValueByName(String name)
/*      */   {
/* 2261 */     String result = null;
/* 2262 */     if (name != null)
/*      */     {
/* 2264 */       FieldInfo fi = (FieldInfo)this.m_fieldMapping.get(name);
/* 2265 */       if (fi != null)
/*      */       {
/* 2267 */         result = getStringValue(fi.m_index);
/*      */       }
/*      */     }
/* 2270 */     return result;
/*      */   }
/*      */ 
/*      */   public Date getDateValue(int index)
/*      */   {
/* 2282 */     List v = (List)this.m_values.get(this.m_currentRow);
/* 2283 */     Object obj = v.get(index);
/*      */ 
/* 2285 */     String str = (String)obj;
/* 2286 */     return parseDateValue(str, index);
/*      */   }
/*      */ 
/*      */   public Date parseDateValue(String str, int index)
/*      */   {
/* 2299 */     Date dte = null;
/*      */     try
/*      */     {
/* 2302 */       if (str.length() > 1)
/*      */       {
/* 2304 */         if (this.m_dateFormat[0] != null)
/*      */         {
/* 2306 */           dte = this.m_dateFormat[0].parseDate(str);
/*      */         }
/*      */         else
/*      */         {
/* 2310 */           dte = LocaleResources.parseDate(str, null);
/*      */         }
/*      */       }
/*      */     }
/*      */     catch (Exception ignore)
/*      */     {
/* 2316 */       if (this.m_dateFormat[0] != null)
/*      */       {
/* 2318 */         FieldInfo fi = null;
/* 2319 */         if (index >= 0)
/*      */         {
/* 2321 */           fi = (FieldInfo)this.m_fieldList.get(index);
/*      */         }
/*      */         else
/*      */         {
/* 2325 */           fi = new FieldInfo();
/* 2326 */           fi.m_name = "<not-specified>";
/* 2327 */           fi.m_type = 6;
/*      */         }
/* 2329 */         Report.trace("system", "Date '" + str + "' cannot be parsed with '" + this.m_dateFormat[0].toPattern() + "' on field " + fi.m_name + " of type " + fi.m_type, ignore);
/*      */       }
/*      */ 
/* 2333 */       Report.trace("system", null, ignore);
/*      */     }
/* 2335 */     return dte;
/*      */   }
/*      */ 
/*      */   public Date getDateValueByName(String name)
/*      */   {
/* 2347 */     Date result = null;
/* 2348 */     if (name != null)
/*      */     {
/* 2350 */       FieldInfo fi = (FieldInfo)this.m_fieldMapping.get(name);
/* 2351 */       if (fi != null)
/*      */       {
/* 2353 */         result = getDateValue(fi.m_index);
/*      */       }
/*      */     }
/* 2356 */     return result;
/*      */   }
/*      */ 
/*      */   public boolean next()
/*      */   {
/* 2367 */     int size = this.m_values.size();
/* 2368 */     this.m_currentRow += 1;
/* 2369 */     if (this.m_currentRow >= size)
/*      */     {
/* 2371 */       this.m_currentRow = size;
/* 2372 */       return false;
/*      */     }
/* 2374 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean previous()
/*      */   {
/* 2382 */     if (this.m_currentRow <= 0)
/*      */     {
/* 2384 */       return false;
/*      */     }
/* 2386 */     this.m_currentRow -= 1;
/* 2387 */     return true;
/*      */   }
/*      */ 
/*      */   public boolean first()
/*      */   {
/* 2397 */     this.m_currentRow = 0;
/*      */ 
/* 2400 */     return this.m_values.size() != 0;
/*      */   }
/*      */ 
/*      */   public boolean last()
/*      */   {
/* 2411 */     if (this.m_values.size() == 0)
/*      */     {
/* 2413 */       return false;
/*      */     }
/* 2415 */     this.m_currentRow = (this.m_values.size() - 1);
/* 2416 */     return true;
/*      */   }
/*      */ 
/*      */   public int skip(int numRows)
/*      */   {
/* 2425 */     int size = this.m_values.size();
/* 2426 */     if ((size == 0) || (numRows == 0) || ((this.m_currentRow < 0) && (numRows < 0)) || ((this.m_currentRow >= size) && (numRows > 0)))
/*      */     {
/* 2430 */       return 0;
/*      */     }
/*      */ 
/* 2433 */     int targetRow = this.m_currentRow + numRows;
/* 2434 */     if (targetRow < 0)
/*      */     {
/* 2436 */       targetRow = 0;
/*      */     }
/* 2438 */     else if (targetRow > size)
/*      */     {
/* 2440 */       targetRow = size;
/*      */     }
/*      */ 
/* 2443 */     int skipped = targetRow - this.m_currentRow;
/* 2444 */     this.m_currentRow = targetRow;
/* 2445 */     return skipped;
/*      */   }
/*      */ 
/*      */   public void setRows(List rows)
/*      */   {
/* 2458 */     if (rows == null)
/*      */     {
/* 2460 */       rows = createNewResultSetList(32);
/*      */     }
/*      */ 
/* 2463 */     this.m_values = rows;
/* 2464 */     this.m_currentRow = 0;
/* 2465 */     this.m_numRows = rows.size();
/*      */   }
/*      */ 
/*      */   public void appendCompatibleRows(DataResultSet drset)
/*      */   {
/* 2478 */     this.m_values.addAll(drset.m_values);
/* 2479 */     this.m_numRows = this.m_values.size();
/*      */   }
/*      */ 
/*      */   public boolean canRenameFields()
/*      */   {
/* 2487 */     return true;
/*      */   }
/*      */ 
/*      */   public void closeInternals()
/*      */   {
/*      */   }
/*      */ 
/*      */   public String toString()
/*      */   {
/* 2505 */     if (null == this.m_dataFormatOptions)
/*      */     {
/* 2507 */       this.m_dataFormatOptions = "text,rows=0";
/*      */     }
/* 2509 */     if ((null == this.m_dataFormat) || (!this.m_dataFormatOptions.equals(this.m_dataFormat.m_formatOptions)))
/*      */     {
/* 2511 */       this.m_dataFormat = new DataFormatter(this.m_dataFormatOptions, false);
/*      */     }
/* 2513 */     this.m_dataFormat.clear();
/* 2514 */     DataFormatUtils.appendResultSet(this.m_dataFormat, "", this, 3);
/* 2515 */     return this.m_dataFormat.toString();
/*      */   }
/*      */ 
/*      */   public void setDataFormatOptions(String options)
/*      */   {
/* 2520 */     this.m_dataFormatOptions = options;
/*      */   }
/*      */ 
/*      */   public int getType()
/*      */   {
/* 2530 */     return 2;
/*      */   }
/*      */ 
/*      */   public void appendRepresentativeString(IdcAppendable appendable)
/*      */   {
/* 2539 */     appendable.append(getRepresentativeString());
/*      */   }
/*      */ 
/*      */   public String getRepresentativeString()
/*      */   {
/* 2550 */     if ((isRowPresent()) && (this.m_fieldList.size() > 0))
/*      */     {
/* 2552 */       return getStringValue(0);
/*      */     }
/* 2554 */     return "";
/*      */   }
/*      */ 
/*      */   public void createIndex(Map options, int[] colIndices)
/*      */   {
/* 2565 */     MutableResultSetHashIndex index = new MutableResultSetHashIndex();
/* 2566 */     index.createIndex(this, new int[] { colIndices[0] });
/* 2567 */     this.m_indices.put(Integer.valueOf(colIndices[0]), index);
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg)
/*      */   {
/* 2572 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99688 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DataResultSet
 * JD-Core Version:    0.5.4
 */