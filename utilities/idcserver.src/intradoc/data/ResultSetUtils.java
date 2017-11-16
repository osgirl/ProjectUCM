/*      */ package intradoc.data;
/*      */ 
/*      */ import intradoc.common.HashVector;
/*      */ import intradoc.common.IdcComparator;
/*      */ import intradoc.common.IdcLinguisticComparatorAdapter;
/*      */ import intradoc.common.LocaleUtils;
/*      */ import intradoc.common.Sort;
/*      */ import intradoc.common.StringUtils;
/*      */ import intradoc.util.IdcVector;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.Enumeration;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Properties;
/*      */ import java.util.Vector;
/*      */ 
/*      */ public class ResultSetUtils
/*      */ {
/*      */   public static final int F_MUST_EXIST = 1;
/*      */   public static final int F_ELIMINATE_MISSING = 2;
/*      */ 
/*      */   public static String[][] createFilteredStringTable(ResultSet rset, String[] keys, String filterVal)
/*      */     throws DataException
/*      */   {
/*   63 */     return createFilteredStringTableEx(rset, keys, filterVal, false, true);
/*      */   }
/*      */ 
/*      */   public static String[][] createFilteredStringTableIgnoreCase(ResultSet rset, String[] keys, String filterVal)
/*      */     throws DataException
/*      */   {
/*   90 */     return createFilteredStringTableEx(rset, keys, filterVal, true, false);
/*      */   }
/*      */ 
/*      */   public static String[][] createFilteredStringTableEx(ResultSet rset, String[] keys, String filterVal, boolean ignoreCase, boolean isPattern)
/*      */     throws DataException
/*      */   {
/*  118 */     int nretkeys = keys.length - 1;
/*      */ 
/*  120 */     List rows = new IdcVector();
/*  121 */     FieldInfo[] infoList = createInfoList(rset, keys, true);
/*  122 */     int filterIndex = infoList[0].m_index;
/*      */ 
/*  124 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  126 */       String rsetVal = rset.getStringValue(filterIndex);
/*  127 */       boolean isMatch = false;
/*      */ 
/*  129 */       if (filterVal != null)
/*      */       {
/*  131 */         if (isPattern)
/*      */         {
/*  133 */           isMatch = StringUtils.matchEx(rsetVal, filterVal, true, ignoreCase);
/*      */         }
/*  135 */         else if (ignoreCase)
/*      */         {
/*  137 */           isMatch = rsetVal.equalsIgnoreCase(filterVal);
/*      */         }
/*      */         else
/*      */         {
/*  141 */           isMatch = rsetVal.equals(filterVal);
/*      */         }
/*      */       }
/*      */ 
/*  145 */       if ((filterVal != null) && (!isMatch))
/*      */         continue;
/*  147 */       String[] row = new String[nretkeys];
/*  148 */       for (int i = 0; i < nretkeys; ++i)
/*      */       {
/*  150 */         int index = infoList[(i + 1)].m_index;
/*  151 */         row[i] = rset.getStringValue(index);
/*      */       }
/*  153 */       rows.add(row);
/*      */     }
/*      */ 
/*  157 */     int nrows = rows.size();
/*  158 */     String[][] table = new String[nrows][];
/*  159 */     rows.toArray(table);
/*  160 */     return table;
/*      */   }
/*      */ 
/*      */   public static String[][] createStringTable(ResultSet rset, String[] keys)
/*      */     throws DataException
/*      */   {
/*  178 */     List rows = new IdcVector();
/*  179 */     FieldInfo[] infoList = createInfoList(rset, keys, true);
/*  180 */     int nretkeys = infoList.length;
/*      */ 
/*  182 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  184 */       String[] row = new String[nretkeys];
/*  185 */       for (int i = 0; i < nretkeys; ++i)
/*      */       {
/*  187 */         int index = infoList[i].m_index;
/*  188 */         row[i] = rset.getStringValue(index);
/*      */       }
/*  190 */       rows.add(row);
/*      */     }
/*      */ 
/*  193 */     int nrows = rows.size();
/*  194 */     String[][] table = new String[nrows][];
/*  195 */     rows.toArray(table);
/*  196 */     return table;
/*      */   }
/*      */ 
/*      */   public static String[] createFilteredStringArrayForColumn(ResultSet rset, String columnKey, String filterKey, String filterValue, boolean ignoreCase, boolean isPattern)
/*      */     throws DataException
/*      */   {
/*  214 */     List rows = new ArrayList();
/*  215 */     int columnIndex = 0; int filterIndex = -1;
/*  216 */     if (null != columnKey)
/*      */     {
/*  218 */       FieldInfo columnField = new FieldInfo();
/*  219 */       if (false == rset.getFieldInfo(columnKey, columnField))
/*      */       {
/*  221 */         String msg = LocaleUtils.encodeMessage("syColumnDoesNotExist", null, columnKey);
/*  222 */         throw new DataException(msg);
/*      */       }
/*  224 */       columnIndex = columnField.m_index;
/*      */     }
/*  226 */     if (null != filterKey)
/*      */     {
/*  228 */       FieldInfo filterField = new FieldInfo();
/*  229 */       if (false == rset.getFieldInfo(filterKey, filterField))
/*      */       {
/*  231 */         String msg = LocaleUtils.encodeMessage("syColumnDoesNotExist", null, filterKey);
/*  232 */         throw new DataException(msg);
/*      */       }
/*  234 */       filterIndex = filterField.m_index;
/*      */     }
/*      */ 
/*  237 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  239 */       if (null != filterKey)
/*      */       {
/*  241 */         String value = rset.getStringValue(filterIndex);
/*  242 */         boolean isMatch = false;
/*      */ 
/*  244 */         if (isPattern)
/*      */         {
/*  246 */           isMatch = StringUtils.matchEx(value, filterValue, true, ignoreCase);
/*      */         }
/*  248 */         else if (ignoreCase)
/*      */         {
/*  250 */           isMatch = value.equalsIgnoreCase(filterValue);
/*      */         }
/*      */         else
/*      */         {
/*  254 */           isMatch = value.equals(filterValue);
/*      */         }
/*      */ 
/*  257 */         if (!isMatch) {
/*      */           continue;
/*      */         }
/*      */       }
/*      */ 
/*  262 */       String value = rset.getStringValue(columnIndex);
/*  263 */       rows.add(value);
/*      */     }
/*      */ 
/*  266 */     int numRows = rows.size();
/*  267 */     String[] columnArray = new String[numRows];
/*  268 */     rows.toArray(columnArray);
/*  269 */     return columnArray;
/*      */   }
/*      */ 
/*      */   public static FieldInfo[] createInfoList(ResultSet rset, String[] fields, boolean mustExist)
/*      */     throws DataException
/*      */   {
/*  285 */     return createInfoListWithFlags(rset, fields, (mustExist) ? 1 : 0);
/*      */   }
/*      */ 
/*      */   public static FieldInfo[] createInfoListWithFlags(ResultSet rset, String[] fields, int flags)
/*      */     throws DataException
/*      */   {
/*  303 */     FieldInfo[] infoList = null;
/*      */ 
/*  306 */     if (fields == null)
/*      */     {
/*  308 */       int numFields = rset.getNumFields();
/*  309 */       infoList = new FieldInfo[numFields];
/*  310 */       for (int i = 0; i < numFields; ++i)
/*      */       {
/*  312 */         FieldInfo info = new FieldInfo();
/*  313 */         String name = rset.getFieldName(i);
/*  314 */         rset.getFieldInfo(name, info);
/*  315 */         infoList[i] = info;
/*      */       }
/*      */     }
/*      */     else
/*      */     {
/*  320 */       boolean mustExist = (flags & 0x1) != 0;
/*  321 */       boolean eliminateDuplicates = (flags & 0x2) != 0;
/*      */ 
/*  323 */       infoList = new FieldInfo[fields.length];
/*  324 */       int infoListIndex = 0;
/*  325 */       for (int i = 0; i < fields.length; ++i)
/*      */       {
/*  327 */         FieldInfo info = new FieldInfo();
/*  328 */         boolean addIt = true;
/*  329 */         assert (rset != null) : "Null ResultSet";
/*  330 */         if (!rset.getFieldInfo(fields[i], info))
/*      */         {
/*  332 */           if (mustExist)
/*      */           {
/*  334 */             throw new DataException(LocaleUtils.encodeMessage("syColumnDoesNotExist", null, fields[i]));
/*      */           }
/*      */ 
/*  339 */           info.m_name = fields[i];
/*  340 */           if (eliminateDuplicates)
/*      */           {
/*  342 */             addIt = false;
/*      */           }
/*      */         }
/*  345 */         if (!addIt)
/*      */           continue;
/*  347 */         infoList[(infoListIndex++)] = info;
/*      */       }
/*      */ 
/*  350 */       if (infoListIndex < fields.length)
/*      */       {
/*  352 */         FieldInfo[] newList = new FieldInfo[infoListIndex];
/*  353 */         System.arraycopy(infoList, 0, newList, 0, infoListIndex);
/*  354 */         infoList = newList;
/*      */       }
/*      */     }
/*  357 */     return infoList;
/*      */   }
/*      */ 
/*      */   public static String[] getFieldListAsStringArray(ResultSet rset)
/*      */   {
/*  368 */     String[] strArray = new String[rset.getNumFields()];
/*  369 */     for (int i = 0; i < strArray.length; ++i)
/*      */     {
/*  371 */       strArray[i] = rset.getFieldName(i);
/*      */     }
/*  373 */     return strArray;
/*      */   }
/*      */ 
/*      */   public static String getValue(ResultSet rset, String key)
/*      */   {
/*  386 */     FieldInfo info = new FieldInfo();
/*  387 */     if (rset.getFieldInfo(key, info))
/*      */     {
/*  389 */       return rset.getStringValue(info.m_index);
/*      */     }
/*  391 */     return null;
/*      */   }
/*      */ 
/*      */   public static Date getDateValue(ResultSet rset, String key)
/*      */   {
/*  409 */     Date date = null;
/*  410 */     FieldInfo info = new FieldInfo();
/*  411 */     if (rset.getFieldInfo(key, info))
/*      */     {
/*  413 */       date = rset.getDateValue(info.m_index);
/*      */     }
/*  415 */     return date;
/*      */   }
/*      */ 
/*      */   public static Object getResultSetObject(ResultSet rset, FieldInfo info)
/*      */   {
/*  428 */     if (info.m_type == 5)
/*      */     {
/*  430 */       return rset.getDateValue(info.m_index);
/*      */     }
/*  432 */     return rset.getStringValue(info.m_index);
/*      */   }
/*      */ 
/*      */   public static int getIndexMustExist(ResultSet rset, String key)
/*      */     throws DataException
/*      */   {
/*  444 */     FieldInfo info = new FieldInfo();
/*  445 */     if (!rset.getFieldInfo(key, info))
/*      */     {
/*  447 */       throw new DataException(LocaleUtils.encodeMessage("syColumnDoesNotExist", null, key));
/*      */     }
/*      */ 
/*  451 */     return info.m_index;
/*      */   }
/*      */ 
/*      */   public static String findValue(ResultSet rset, String lookupKey, String lookupFilter, String resultKey)
/*      */     throws DataException
/*      */   {
/*  470 */     return findValueEx(rset, lookupKey, lookupFilter, resultKey, false);
/*      */   }
/*      */ 
/*      */   public static String findValueIgnoreCase(ResultSet rset, String lookupKey, String lookupFilter, String resultKey)
/*      */     throws DataException
/*      */   {
/*  489 */     return findValueEx(rset, lookupKey, lookupFilter, resultKey, true);
/*      */   }
/*      */ 
/*      */   public static String findValueEx(ResultSet rset, String lookupKey, String lookupFilter, String resultKey, boolean ignoreCase)
/*      */     throws DataException
/*      */   {
/*  510 */     String[] keys = { lookupKey, resultKey };
/*  511 */     String[][] results = createFilteredStringTable(rset, keys, lookupFilter);
/*  512 */     if (results.length == 0)
/*      */     {
/*  514 */       return null;
/*      */     }
/*  516 */     return results[0][0];
/*      */   }
/*      */ 
/*      */   public static Vector loadValuesFromSetEx(ResultSet rset, String lookupKey, boolean mustExist)
/*      */     throws DataException
/*      */   {
/*  531 */     HashVector values = new HashVector();
/*      */ 
/*  533 */     if ((rset == null) || (rset.isEmpty()))
/*      */     {
/*  535 */       return values.m_values;
/*      */     }
/*      */ 
/*  538 */     String[] fieldList = new String[1];
/*  539 */     fieldList[0] = lookupKey;
/*  540 */     FieldInfo[] infos = createInfoList(rset, fieldList, mustExist);
/*      */ 
/*  542 */     int findex = infos[0].m_index;
/*  543 */     if (findex < 0)
/*      */     {
/*  545 */       return new IdcVector();
/*      */     }
/*      */ 
/*  548 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  550 */       values.addValue(rset.getStringValue(findex));
/*      */     }
/*      */ 
/*  553 */     return values.m_values;
/*      */   }
/*      */ 
/*      */   public static Vector loadValuesFromSet(ResultSet rset, String lookupKey)
/*      */     throws DataException
/*      */   {
/*  566 */     return loadValuesFromSetEx(rset, lookupKey, true);
/*      */   }
/*      */ 
/*      */   public static void sortResultSet(DataResultSet rset, String[] keys)
/*      */     throws DataException
/*      */   {
/*  586 */     sortResultSet(rset, keys, 2);
/*      */   }
/*      */ 
/*      */   public static void sortResultSet(DataResultSet rset, String[] keys, int sortStrength)
/*      */     throws DataException
/*      */   {
/*  607 */     if (rset == null)
/*      */     {
/*  609 */       return;
/*      */     }
/*      */ 
/*  612 */     String[] sortKeys = keys;
/*  613 */     FieldInfo[] infos = createInfoList(rset, keys, true);
/*      */ 
/*  615 */     IdcLinguisticComparatorAdapter cmp = new IdcLinguisticComparatorAdapter(sortKeys, infos)
/*      */     {
/*      */       public int compare(Object obj1, Object obj2)
/*      */       {
/*  620 */         Vector v1 = (Vector)obj1;
/*  621 */         Vector v2 = (Vector)obj2;
/*      */ 
/*  623 */         String s1 = null;
/*  624 */         String s2 = null;
/*  625 */         int num = this.val$sortKeys.length;
/*  626 */         int result = 0;
/*  627 */         for (int i = 0; i < num; ++i)
/*      */         {
/*  629 */           s1 = (String)v1.elementAt(this.val$infos[i].m_index);
/*  630 */           s2 = (String)v2.elementAt(this.val$infos[i].m_index);
/*  631 */           if (this.val$infos[i].m_type == 3)
/*      */           {
/*  633 */             result = Integer.parseInt(s1) - Integer.parseInt(s2);
/*      */           }
/*      */           else
/*      */           {
/*  637 */             result = super.compare(s1, s2);
/*      */           }
/*      */ 
/*  640 */           if (result != 0) {
/*      */             break;
/*      */           }
/*      */         }
/*      */ 
/*  645 */         return result;
/*      */       }
/*      */     };
/*  650 */     cmp.init(IdcLinguisticComparatorAdapter.m_defaultRule);
/*      */ 
/*  653 */     cmp.setComparatorLevel(sortStrength);
/*  654 */     sortResultSet(rset, cmp);
/*      */   }
/*      */ 
/*      */   public static void sortResultSet(DataResultSet rset, IdcComparator cmp)
/*      */     throws DataException
/*      */   {
/*  670 */     if (rset == null)
/*      */     {
/*  672 */       return;
/*      */     }
/*      */ 
/*  675 */     int num = rset.getNumRows();
/*  676 */     Object[] s = new Object[num];
/*      */ 
/*  678 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  680 */       s[i] = rset.getRowValues(i);
/*      */     }
/*      */ 
/*  683 */     Sort.sort(s, 0, num - 1, cmp);
/*      */ 
/*  685 */     for (int i = 0; i < num; ++i)
/*      */     {
/*  687 */       rset.setRowValues((Vector)s[i], i);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static Vector<FieldInfo> createFieldInfo(String[] fields, int defLen)
/*      */   {
/*  708 */     Vector infos = new IdcVector();
/*  709 */     for (int i = 0; i < fields.length; ++i)
/*      */     {
/*  711 */       FieldInfo fi = new FieldInfo();
/*  712 */       fi.m_name = fields[i];
/*  713 */       fi.m_isFixedLen = (defLen > 0);
/*  714 */       fi.m_maxLen = defLen;
/*      */ 
/*  716 */       infos.addElement(fi);
/*      */     }
/*  718 */     return infos;
/*      */   }
/*      */ 
/*      */   public static DataResultSet createResultSetFromList(String name, List list, String columnHeader)
/*      */   {
/*  733 */     if ((null == name) || (null == list))
/*      */     {
/*  735 */       return null;
/*      */     }
/*  737 */     if (null == columnHeader)
/*      */     {
/*  739 */       columnHeader = "element";
/*      */     }
/*  741 */     DataResultSet result = new DataResultSet(new String[] { columnHeader });
/*  742 */     for (int i = 0; i < list.size(); ++i)
/*      */     {
/*  744 */       Vector row = new IdcVector(1);
/*  745 */       row.addElement(list.get(i));
/*  746 */       result.addRow(row);
/*      */     }
/*  748 */     return result;
/*      */   }
/*      */ 
/*      */   public static DataResultSet createResultSetFromProperties(Properties props)
/*      */   {
/*  753 */     Vector keys = new IdcVector();
/*  754 */     Vector values = new IdcVector();
/*  755 */     Enumeration keysEnum = props.keys();
/*  756 */     while (keysEnum.hasMoreElements())
/*      */     {
/*  758 */       String key = (String)keysEnum.nextElement();
/*  759 */       String val = props.getProperty(key);
/*  760 */       keys.addElement(key);
/*  761 */       values.addElement(val);
/*      */     }
/*  763 */     String[] keysStrArray = StringUtils.convertListToArray(keys);
/*  764 */     DataResultSet drset = new DataResultSet(keysStrArray);
/*  765 */     drset.addRow(values);
/*  766 */     drset.first();
/*  767 */     return drset;
/*      */   }
/*      */ 
/*      */   public static DataResultSet createResultSetFromProperties(Properties props, String[] keys)
/*      */   {
/*  772 */     Vector values = new IdcVector();
/*  773 */     for (int i = 0; i < keys.length; ++i)
/*      */     {
/*  775 */       String val = props.getProperty(keys[i]);
/*  776 */       values.addElement(val);
/*      */     }
/*  778 */     DataResultSet drset = new DataResultSet(keys);
/*  779 */     drset.addRow(values);
/*  780 */     drset.first();
/*  781 */     return drset;
/*      */   }
/*      */ 
/*      */   public static DataResultSet getMutableResultSet(DataBinder binder, String key, boolean mustBeEditable, boolean mustBeOnValidRow)
/*      */   {
/*  787 */     ResultSet rset = binder.getResultSet(key);
/*  788 */     if (rset == null)
/*      */     {
/*  790 */       return null;
/*      */     }
/*  792 */     if (!rset instanceof DataResultSet)
/*      */     {
/*  794 */       String msg = LocaleUtils.encodeMessage("csResultSetNotAllowRandomRowAccess", null, key);
/*      */ 
/*  796 */       throw new IllegalArgumentException(msg);
/*      */     }
/*  798 */     DataResultSet drset = (DataResultSet)rset;
/*  799 */     if ((mustBeOnValidRow) && (!drset.isRowPresent()))
/*      */     {
/*  801 */       String msg = LocaleUtils.encodeMessage("csResultSetNotOnLegalRow", null, key);
/*      */ 
/*  803 */       throw new IllegalArgumentException(msg);
/*      */     }
/*  805 */     if ((!drset.isMutable()) && (mustBeEditable))
/*      */     {
/*  807 */       int currentRow = drset.getCurrentRow();
/*  808 */       DataResultSet drsetCopy = new DataResultSet();
/*  809 */       drsetCopy.copy(drset);
/*  810 */       drsetCopy.setCurrentRow(currentRow);
/*  811 */       drset.setCurrentRow(currentRow);
/*      */ 
/*  815 */       binder.addResultSetDirect(key, drsetCopy);
/*  816 */       drset = drsetCopy;
/*      */     }
/*  818 */     return drset;
/*      */   }
/*      */ 
/*      */   public static void addColumnsWithDefaultValues(DataResultSet rset, DataBinder data, String[] src, Object[] dst)
/*      */     throws DataException
/*      */   {
/*  854 */     int length = dst.length;
/*  855 */     FieldInfo[] newFields = new FieldInfo[length];
/*  856 */     List vFields = new ArrayList(length);
/*  857 */     for (int i = 0; i < length; ++i)
/*      */     {
/*  859 */       String columnName = null;
/*  860 */       if (dst[i] instanceof FieldInfo)
/*      */       {
/*  862 */         columnName = ((FieldInfo)dst[i]).m_name;
/*      */       }
/*      */       else
/*      */       {
/*  866 */         columnName = dst[i].toString();
/*      */       }
/*  868 */       if (null == columnName) {
/*      */         continue;
/*      */       }
/*      */ 
/*  872 */       FieldInfo info = new FieldInfo();
/*  873 */       if (false != rset.getFieldInfo(columnName, info))
/*      */       {
/*      */         continue;
/*      */       }
/*      */ 
/*  881 */       info.m_name = columnName;
/*  882 */       newFields[i] = info;
/*  883 */       vFields.add(info);
/*      */     }
/*  885 */     if (vFields.size() < 1)
/*      */     {
/*  888 */       return;
/*      */     }
/*      */ 
/*  892 */     rset.mergeFieldsWithFlags(vFields, 2);
/*      */ 
/*  895 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  897 */       for (int i = 0; i < length; ++i)
/*      */       {
/*  899 */         if (null == newFields[i])
/*      */         {
/*      */           continue;
/*      */         }
/*      */ 
/*  905 */         String value = src[i];
/*      */ 
/*  907 */         if ((null != data) && (null != value))
/*      */         {
/*  909 */           value = data.getAllowMissing(src[i]);
/*      */         }
/*  911 */         if (null == value)
/*      */         {
/*  913 */           value = "";
/*      */         }
/*  915 */         rset.setCurrentValue(newFields[i].m_index, value);
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   public static void addColumnWithMappedValues(DataResultSet rset, String defaultValue, String existingColumnAsMapKey, Object newColumn, Map<String, String> map)
/*      */     throws DataException
/*      */   {
/*  944 */     int keyColumnIndex = rset.getFieldInfoIndex(existingColumnAsMapKey);
/*  945 */     if (keyColumnIndex < 0)
/*      */     {
/*  947 */       return;
/*      */     }
/*      */ 
/*  951 */     String[] src = { defaultValue };
/*  952 */     Object[] dst = { newColumn };
/*  953 */     String newColumnName = (newColumn instanceof FieldInfo) ? ((FieldInfo)newColumn).m_name : newColumn.toString();
/*  954 */     addColumnsWithDefaultValues(rset, null, src, dst);
/*  955 */     int newColumnIndex = rset.getFieldInfoIndex(newColumnName);
/*      */ 
/*  959 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*      */     {
/*  961 */       String value = (String)map.get(rset.getStringValue(keyColumnIndex));
/*  962 */       if (value == null)
/*      */         continue;
/*  964 */       rset.setCurrentValue(newColumnIndex, value);
/*      */     }
/*      */   }
/*      */ 
/*      */   public static int[] createColumnMap(DataResultSet from, DataResultSet to)
/*      */   {
/*  981 */     int[] result = new int[from.getNumFields()];
/*      */ 
/*  983 */     int curToStartIndex = 0;
/*  984 */     int numToFields = to.getNumFields();
/*  985 */     for (int i = 0; i < result.length; ++i)
/*      */     {
/*  987 */       FieldInfo fromFi = (FieldInfo)from.m_fieldList.get(i);
/*      */ 
/*  989 */       boolean foundIt = false;
/*      */ 
/*  992 */       for (int j = curToStartIndex; j < numToFields; ++j)
/*      */       {
/*  994 */         FieldInfo toFi = (FieldInfo)to.m_fieldList.get(j);
/*  995 */         if (!toFi.m_name.equalsIgnoreCase(fromFi.m_name))
/*      */           continue;
/*  997 */         foundIt = true;
/*  998 */         curToStartIndex = j + 1;
/*  999 */         result[i] = j;
/* 1000 */         break;
/*      */       }
/*      */ 
/* 1003 */       if (!foundIt)
/*      */       {
/* 1005 */         for (int j = 0; j < curToStartIndex; ++j)
/*      */         {
/* 1007 */           FieldInfo toFi = (FieldInfo)to.m_fieldList.get(j);
/* 1008 */           if (!toFi.m_name.equalsIgnoreCase(fromFi.m_name))
/*      */             continue;
/* 1010 */           foundIt = true;
/* 1011 */           curToStartIndex = j + 1;
/* 1012 */           result[i] = j;
/* 1013 */           break;
/*      */         }
/*      */       }
/*      */ 
/* 1017 */       if (foundIt)
/*      */         continue;
/* 1019 */       result[i] = -1;
/*      */     }
/*      */ 
/* 1022 */     return result;
/*      */   }
/*      */ 
/*      */   public static Properties getCurrentRowProps(ResultSet rset)
/*      */   {
/* 1028 */     if (!rset.isRowPresent())
/*      */     {
/* 1030 */       return null;
/*      */     }
/* 1032 */     Properties map = new Properties();
/* 1033 */     int size = rset.getNumFields();
/* 1034 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1036 */       String name = rset.getFieldName(i);
/* 1037 */       String value = rset.getStringValue(i);
/* 1038 */       map.setProperty(name, value);
/*      */     }
/* 1040 */     return map;
/*      */   }
/*      */ 
/*      */   public static HashMap getCurrentRowMap(ResultSet rset)
/*      */   {
/* 1045 */     if (!rset.isRowPresent())
/*      */     {
/* 1047 */       return null;
/*      */     }
/* 1049 */     HashMap map = new HashMap();
/* 1050 */     int size = rset.getNumFields();
/* 1051 */     for (int i = 0; i < size; ++i)
/*      */     {
/* 1053 */       String name = rset.getFieldName(i);
/* 1054 */       String value = rset.getStringValue(i);
/* 1055 */       map.put(name, value);
/*      */     }
/* 1057 */     return map;
/*      */   }
/*      */ 
/*      */   public static String getStringValueIfExists(ResultSet rset, int index)
/*      */   {
/* 1062 */     if (index == -1)
/*      */     {
/* 1064 */       return null;
/*      */     }
/*      */ 
/* 1067 */     return rset.getStringValue(index);
/*      */   }
/*      */ 
/*      */   public static List findDualIndexedRow(DataResultSet drset, String field1, String field2, int field1Index, int field2Index)
/*      */   {
/* 1073 */     List fRow = null;
/* 1074 */     List r = drset.findRow(field1Index, field1, 0, 2);
/* 1075 */     while (r != null)
/*      */     {
/* 1077 */       String s = drset.getStringValue(field2Index);
/* 1078 */       if (s.equals(field2))
/*      */       {
/* 1080 */         fRow = r;
/* 1081 */         break;
/*      */       }
/* 1083 */       r = drset.findRow(field2Index, field1, drset.getCurrentRow() + 1, 2);
/*      */     }
/*      */ 
/* 1086 */     return fRow;
/*      */   }
/*      */ 
/*      */   public static DataResultSet sortedMerge(ResultSet sourceResultset1, ResultSet sourceResultset2, String sortField, String sortOrder)
/*      */   {
/* 1101 */     return sortedMerge(sourceResultset1, sourceResultset2, sortField, sortOrder, 0, -1);
/*      */   }
/*      */ 
/*      */   public static DataResultSet sortedMerge(ResultSet sourceResultset1, ResultSet sourceResultset2, String sortField, String sortOrder, int startRow, int rowCount)
/*      */   {
/* 1118 */     return sortedMerge(sourceResultset1, sourceResultset2, sortField, sortOrder, startRow, rowCount, null, null, null);
/*      */   }
/*      */ 
/*      */   public static DataResultSet sortedMerge(ResultSet sourceResultset1, ResultSet sourceResultset2, String sortField, String sortOrder, int startRow, int rowCount, String labelFieldName, String sourceLabel1, String sourceLabel2)
/*      */   {
/* 1139 */     if ((sourceResultset1 == null) || (sourceResultset2 == null))
/*      */     {
/* 1141 */       return null;
/*      */     }
/* 1143 */     DataResultSet mergedDataResultSet = new DataResultSet();
/* 1144 */     mergedDataResultSet.mergeFields(sourceResultset1);
/* 1145 */     int numFetchFields = mergedDataResultSet.getNumFields();
/* 1146 */     FieldInfo labelFieldInfo = null;
/* 1147 */     if (labelFieldName != null)
/*      */     {
/* 1149 */       labelFieldInfo = new FieldInfo();
/* 1150 */       labelFieldInfo.m_name = labelFieldName;
/* 1151 */       List fieldInfosList = new ArrayList();
/* 1152 */       fieldInfosList.add(labelFieldInfo);
/* 1153 */       mergedDataResultSet.mergeFieldsWithFlags(fieldInfosList, 2);
/*      */     }
/* 1155 */     int currentRowIndex = 0;
/* 1156 */     int currentRowCount = 0;
/* 1157 */     sourceResultset1.first();
/* 1158 */     sourceResultset2.first();
/*      */ 
/* 1162 */     while ((sourceResultset1.isRowPresent()) && (sourceResultset2.isRowPresent()))
/*      */     {
/* 1165 */       ResultSet currentResultSet = null;
/* 1166 */       String currentLabel = null;
/* 1167 */       if ((rowCount != -1) && (currentRowCount >= rowCount)) {
/*      */         break;
/*      */       }
/*      */ 
/* 1171 */       String source1SortValue = sourceResultset1.getStringValueByName(sortField);
/* 1172 */       String source2SortValue = sourceResultset2.getStringValueByName(sortField);
/* 1173 */       if (sortOrder.equalsIgnoreCase("ASC"))
/*      */       {
/* 1175 */         currentResultSet = (source1SortValue.compareTo(source2SortValue) < 0) ? sourceResultset1 : sourceResultset2;
/* 1176 */         currentLabel = (source1SortValue.compareTo(source2SortValue) < 0) ? sourceLabel1 : sourceLabel2;
/*      */       }
/*      */       else
/*      */       {
/* 1180 */         currentResultSet = (source1SortValue.compareTo(source2SortValue) > 0) ? sourceResultset1 : sourceResultset2;
/* 1181 */         currentLabel = (source1SortValue.compareTo(source2SortValue) > 0) ? sourceLabel1 : sourceLabel2;
/*      */       }
/*      */ 
/* 1184 */       if (currentRowIndex >= startRow)
/*      */       {
/* 1186 */         List currentRow = mergedDataResultSet.getResultSetValues(currentResultSet, numFetchFields);
/* 1187 */         if (labelFieldInfo != null)
/*      */         {
/* 1189 */           currentRow.add(currentLabel);
/*      */         }
/* 1191 */         mergedDataResultSet.addRowWithList(currentRow);
/* 1192 */         ++currentRowCount;
/*      */       }
/* 1194 */       currentResultSet.next();
/* 1195 */       ++currentRowIndex;
/*      */     }
/*      */ 
/* 1198 */     while ((((rowCount == -1) || (currentRowCount < rowCount))) && (sourceResultset1.isRowPresent()))
/*      */     {
/* 1200 */       if (currentRowIndex >= startRow)
/*      */       {
/* 1202 */         List currentRow = mergedDataResultSet.getResultSetValues(sourceResultset1, numFetchFields);
/* 1203 */         if (labelFieldInfo != null)
/*      */         {
/* 1205 */           currentRow.add(sourceLabel1);
/*      */         }
/* 1207 */         mergedDataResultSet.addRowWithList(currentRow);
/* 1208 */         ++currentRowCount;
/*      */       }
/* 1210 */       sourceResultset1.next();
/* 1211 */       ++currentRowIndex;
/*      */     }
/* 1213 */     while ((((rowCount == -1) || (currentRowCount < rowCount))) && (sourceResultset2.isRowPresent()))
/*      */     {
/* 1215 */       if (currentRowIndex >= startRow)
/*      */       {
/* 1217 */         List currentRow = mergedDataResultSet.getResultSetValues(sourceResultset2, numFetchFields);
/* 1218 */         if (labelFieldInfo != null)
/*      */         {
/* 1220 */           currentRow.add(sourceLabel2);
/*      */         }
/* 1222 */         mergedDataResultSet.addRowWithList(currentRow);
/* 1223 */         ++currentRowCount;
/*      */       }
/* 1225 */       sourceResultset2.next();
/* 1226 */       ++currentRowIndex;
/*      */     }
/*      */ 
/* 1229 */     return mergedDataResultSet;
/*      */   }
/*      */ 
/*      */   public static Object idcVersionInfo(Object arg) {
/* 1233 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 99124 $";
/*      */   }
/*      */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.ResultSetUtils
 * JD-Core Version:    0.5.4
 */