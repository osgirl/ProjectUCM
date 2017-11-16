/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.IdcCharArrayWriter;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.shared.SharedObjects;
/*     */ import intradoc.util.IdcVector;
/*     */ import java.io.IOException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Arrays;
/*     */ import java.util.Enumeration;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaData
/*     */ {
/*     */   public static final String OP_CREATE = "create";
/*     */   public static final String OP_MODIFY = "modify";
/*     */   public static final String OP_RENAME = "rename";
/*     */   public static final String OP_SYNCHRONIZE = "synchronize";
/*     */   public static final String OP_DELETE = "delete";
/*  38 */   public String m_name = null;
/*  39 */   public String m_canonicalName = null;
/*     */   public long m_timestamp;
/*     */   public boolean m_isUpToDate;
/*  43 */   public int m_nameIndex = -1;
/*  44 */   public int m_canonicalNameIndex = -1;
/*  45 */   public int m_timestampIndex = -1;
/*  46 */   public int m_isUpToDateIndex = -1;
/*     */ 
/*  48 */   protected DataBinder m_data = null;
/*     */   protected SchemaResultSet m_resultSet;
/*  52 */   protected ArrayList m_localDataCruftKeys = new ArrayList();
/*  53 */   protected ArrayList m_resultSetCruftKeys = new ArrayList();
/*  54 */   protected ArrayList m_baseResultSets = new ArrayList();
/*     */ 
/*     */   public SchemaData()
/*     */   {
/*  58 */     initCruftKeys();
/*     */   }
/*     */ 
/*     */   public void initCruftKeys()
/*     */   {
/*  64 */     this.m_localDataCruftKeys = new ArrayList();
/*  65 */     this.m_localDataCruftKeys.addAll(Arrays.asList(new String[] { "dUser", "monitoredSubjects", "refreshSubjects", "changedSubjects", "watchedMonikers", "refreshMonikers", "changedMonikers", "monitoredTopics", "refreshTopics", "changedTopics", "subjectNotifyChanged", "monikerNotifyChanged", "topicNotifyChanged", "CurrentArchiverStatus", "GetCurrentArchiverStatus", "GetCurrentIndexingStatus", "NoHttpHeaders", "StatusCode", "StatusMessageKey", "StatusMessage", "ErrorStackTrace", "IsNew", "ClientEncoding", "IsJava", "IdcService", "forceLogin", "idcToken" }));
/*     */ 
/*  75 */     this.m_resultSetCruftKeys = new ArrayList();
/*  76 */     this.m_resultSetCruftKeys.addAll(Arrays.asList(new String[] { "UpdatedUserTopics", "SchemaViewConfig", "SchemaTableConfig", "SchemaFieldConfig", "SchemaRelationConfig", "SchemaConfigData" }));
/*     */   }
/*     */ 
/*     */   public void removeAll(Map data, ArrayList itemList)
/*     */   {
/*  86 */     for (int i = 0; i < itemList.size(); ++i)
/*     */     {
/*  88 */       String key = (String)itemList.get(i);
/*  89 */       data.remove(key);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void init(SchemaResultSet set)
/*     */   {
/* 100 */     this.m_resultSet = set;
/* 101 */     initIndexes();
/* 102 */     List v = set.getValidResultSets();
/* 103 */     for (int i = 0; i < v.size(); ++i)
/*     */     {
/* 105 */       this.m_baseResultSets.add(v.get(i));
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void initIndexes()
/*     */   {
/* 116 */     FieldInfo info = new FieldInfo();
/*     */ 
/* 118 */     String tmp = getNameField();
/* 119 */     if (tmp != null)
/*     */     {
/* 121 */       this.m_resultSet.getFieldInfo(tmp, info);
/* 122 */       this.m_nameIndex = info.m_index;
/*     */     }
/*     */ 
/* 125 */     this.m_resultSet.getFieldInfo("schCanonicalName", info);
/* 126 */     this.m_canonicalNameIndex = info.m_index;
/*     */ 
/* 128 */     tmp = getTimestampField();
/* 129 */     if (tmp != null)
/*     */     {
/* 131 */       this.m_resultSet.getFieldInfo(tmp, info);
/* 132 */       this.m_timestampIndex = info.m_index;
/*     */     }
/*     */ 
/* 135 */     tmp = getIsUpToDateField();
/* 136 */     if (tmp == null)
/*     */       return;
/* 138 */     this.m_resultSet.getFieldInfo(tmp, info);
/* 139 */     this.m_isUpToDateIndex = info.m_index;
/*     */   }
/*     */ 
/*     */   public String getNameField()
/*     */   {
/* 148 */     FieldInfo info = new FieldInfo();
/* 149 */     this.m_resultSet.getIndexFieldInfo(0, info);
/* 150 */     return info.m_name;
/*     */   }
/*     */ 
/*     */   public String getTimestampField()
/*     */   {
/* 158 */     FieldInfo info = new FieldInfo();
/* 159 */     this.m_resultSet.getIndexFieldInfo(2, info);
/* 160 */     return info.m_name;
/*     */   }
/*     */ 
/*     */   public String getIsUpToDateField()
/*     */   {
/* 168 */     FieldInfo info = new FieldInfo();
/* 169 */     this.m_resultSet.getIndexFieldInfo(3, info);
/* 170 */     return info.m_name;
/*     */   }
/*     */ 
/*     */   public String canonicalName(String name)
/*     */   {
/* 182 */     return this.m_resultSet.canonicalName(name);
/*     */   }
/*     */ 
/*     */   public void update(DataBinder binder) throws DataException
/*     */   {
/* 187 */     String binderName = binder.getLocal(getNameField());
/* 188 */     if ((binderName == null) || ((this.m_canonicalName != null) && (!canonicalName(binderName).equals(this.m_canonicalName))))
/*     */     {
/* 193 */       throw new DataException("Attempting to update '" + this.m_name + "' with data for '" + binderName + "'.");
/*     */     }
/*     */ 
/* 198 */     updateEx(binder);
/*     */   }
/*     */ 
/*     */   public void updateEx(DataBinder binder)
/*     */   {
/* 211 */     if (this.m_data == null)
/*     */     {
/* 213 */       this.m_data = new DataBinder();
/*     */     }
/* 215 */     this.m_data.merge(binder);
/* 216 */     removeServerCruft(this.m_data);
/* 217 */     Enumeration en = this.m_data.m_localData.keys();
/*     */ 
/* 221 */     Vector removeList = new IdcVector();
/* 222 */     while (en.hasMoreElements())
/*     */     {
/* 224 */       String key = (String)en.nextElement();
/* 225 */       if (binder.getLocal(key) == null)
/*     */       {
/* 227 */         removeList.addElement(key);
/*     */       }
/*     */     }
/* 230 */     for (int i = 0; i < removeList.size(); ++i)
/*     */     {
/* 232 */       this.m_data.removeLocal((String)removeList.elementAt(i));
/*     */     }
/* 234 */     this.m_name = this.m_data.getLocal(getNameField());
/* 235 */     this.m_canonicalName = canonicalName(this.m_name);
/*     */   }
/*     */ 
/*     */   protected void removeServerCruft(DataBinder binder)
/*     */   {
/* 240 */     for (int i = 0; i < this.m_localDataCruftKeys.size(); ++i)
/*     */     {
/* 242 */       binder.removeLocal((String)this.m_localDataCruftKeys.get(i));
/*     */     }
/* 244 */     Properties props = binder.getLocalData();
/* 245 */     Enumeration en = props.keys();
/* 246 */     ArrayList localKeys = new ArrayList();
/* 247 */     while (en.hasMoreElements())
/*     */     {
/* 249 */       String key = (String)en.nextElement();
/* 250 */       String value = props.getProperty(key);
/* 251 */       if ((key.startsWith("_")) || (value.length() == 0))
/*     */       {
/* 253 */         localKeys.add(key);
/*     */       }
/*     */     }
/* 256 */     removeAll(props, localKeys);
/*     */ 
/* 258 */     for (int i = 0; i < this.m_resultSetCruftKeys.size(); ++i)
/*     */     {
/* 260 */       binder.removeResultSet((String)this.m_resultSetCruftKeys.get(i));
/*     */     }
/*     */ 
/* 263 */     ArrayList optionLists = new ArrayList();
/* 264 */     en = binder.m_optionLists.keys();
/* 265 */     while (en.hasMoreElements())
/*     */     {
/* 267 */       Object key = en.nextElement();
/* 268 */       optionLists.add(key);
/*     */     }
/* 270 */     removeAll(binder.m_optionLists, optionLists);
/*     */ 
/* 272 */     Vector validResultSets = getValidResultSets();
/* 273 */     en = binder.getResultSetList();
/* 274 */     ArrayList resultSets = new ArrayList();
/* 275 */     while (en.hasMoreElements())
/*     */     {
/* 277 */       String key = (String)en.nextElement();
/* 278 */       if (validResultSets.indexOf(key) == -1)
/*     */       {
/* 280 */         resultSets.add(key);
/*     */       }
/*     */     }
/* 283 */     removeAll(binder.getResultSets(), resultSets);
/*     */   }
/*     */ 
/*     */   public Vector getValidResultSets()
/*     */   {
/* 288 */     Vector validResultSets = getVector("ViewResultSets");
/* 289 */     for (int i = 0; i < this.m_baseResultSets.size(); ++i)
/*     */     {
/* 291 */       validResultSets.addElement(this.m_baseResultSets.get(i));
/*     */     }
/*     */ 
/* 294 */     return validResultSets;
/*     */   }
/*     */ 
/*     */   public void updateRow(Vector row)
/*     */   {
/* 299 */     row.setElementAt(this.m_name, this.m_nameIndex);
/* 300 */     row.setElementAt(this.m_canonicalName, this.m_canonicalNameIndex);
/* 301 */     row.setElementAt("" + this.m_timestamp, this.m_timestampIndex);
/* 302 */     row.setElementAt("" + this.m_isUpToDate, this.m_isUpToDateIndex);
/*     */ 
/* 304 */     String[] columns = this.m_resultSet.m_columns;
/* 305 */     int[] indexes = this.m_resultSet.m_indexes;
/* 306 */     int num = columns.length;
/* 307 */     for (int i = 4; i < num; ++i)
/*     */     {
/* 309 */       String name = columns[i];
/* 310 */       int index = indexes[i];
/* 311 */       String val = this.m_data.getLocal(name);
/* 312 */       if (val == null)
/*     */       {
/* 314 */         val = "";
/*     */       }
/* 316 */       row.setElementAt(val, index);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void populateBinder(DataBinder binder)
/*     */   {
/* 322 */     populateBinder(binder, "");
/*     */   }
/*     */ 
/*     */   public void populateBinder(DataBinder binder, String prefix)
/*     */   {
/* 327 */     if (prefix == null)
/*     */     {
/* 329 */       prefix = "";
/*     */     }
/* 331 */     Properties props = this.m_data.getLocalData();
/* 332 */     Enumeration en = props.keys();
/* 333 */     while (en.hasMoreElements())
/*     */     {
/* 335 */       String key = (String)en.nextElement();
/* 336 */       String value = props.getProperty(key);
/* 337 */       binder.putLocal(prefix + key, value);
/*     */     }
/*     */ 
/* 340 */     en = this.m_data.getResultSetList();
/* 341 */     while (en.hasMoreElements())
/*     */     {
/* 343 */       String name = (String)en.nextElement();
/* 344 */       ResultSet rset = this.m_data.getResultSet(name);
/* 345 */       binder.addResultSet(prefix + name, rset);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void clearBinder(DataBinder binder)
/*     */   {
/* 351 */     clearBinder(binder, "");
/*     */   }
/*     */ 
/*     */   public void clearBinder(DataBinder binder, String prefix)
/*     */   {
/* 356 */     if (prefix == null)
/*     */     {
/* 358 */       prefix = "";
/*     */     }
/* 360 */     Properties props = this.m_data.getLocalData();
/* 361 */     Enumeration en = props.keys();
/* 362 */     ArrayList list = new ArrayList();
/* 363 */     while (en.hasMoreElements())
/*     */     {
/* 365 */       String key = (String)en.nextElement();
/* 366 */       list.add(prefix + key);
/*     */     }
/* 368 */     removeAll(binder.getLocalData(), list);
/*     */   }
/*     */ 
/*     */   public String get(String key)
/*     */   {
/* 373 */     return this.m_data.getLocal(key);
/*     */   }
/*     */ 
/*     */   public String getRequired(String key) throws DataException
/*     */   {
/* 378 */     String value = get(key);
/* 379 */     if (value == null)
/*     */     {
/* 381 */       String msg = LocaleUtils.encodeMessage("csRequiredFieldMissing2", null, key);
/*     */ 
/* 383 */       throw new DataException(msg);
/*     */     }
/* 385 */     return value;
/*     */   }
/*     */ 
/*     */   public String get(String key, String defaultValue)
/*     */   {
/* 394 */     String value = this.m_data.getLocal(key);
/* 395 */     if (value == null)
/*     */     {
/* 397 */       if (defaultValue == null)
/*     */       {
/* 399 */         defaultValue = SharedObjects.getEnvironmentValue(key);
/*     */       }
/* 401 */       value = defaultValue;
/*     */     }
/* 403 */     return value;
/*     */   }
/*     */ 
/*     */   public DataBinder getData()
/*     */   {
/* 408 */     return this.m_data;
/*     */   }
/*     */ 
/*     */   public boolean getBoolean(String key, boolean defaultValue)
/*     */   {
/* 413 */     String value = this.m_data.getLocal(key);
/* 414 */     boolean v = StringUtils.convertToBool(value, defaultValue);
/* 415 */     return v;
/*     */   }
/*     */ 
/*     */   public int getInt(String key, int defaultValue)
/*     */   {
/* 420 */     String value = this.m_data.getLocal(key);
/* 421 */     int v = NumberUtils.parseInteger(value, defaultValue);
/* 422 */     return v;
/*     */   }
/*     */ 
/*     */   public Vector getVector(String key)
/*     */   {
/* 427 */     Vector v = new IdcVector();
/* 428 */     String value = this.m_data.getLocal(key);
/* 429 */     if (value != null)
/*     */     {
/* 431 */       v = StringUtils.parseArray(value, ',', '^');
/*     */     }
/* 433 */     return v;
/*     */   }
/*     */ 
/*     */   public List getMatchingKeys(String keyFilter, String valueFilter)
/*     */   {
/* 438 */     ArrayList list = new ArrayList();
/* 439 */     Enumeration en = this.m_data.m_localData.keys();
/* 440 */     while (en.hasMoreElements())
/*     */     {
/* 442 */       String key = (String)en.nextElement();
/* 443 */       if ((keyFilter != null) && (StringUtils.match(key, keyFilter, false)))
/*     */       {
/* 445 */         list.add(key);
/*     */       }
/*     */       String value;
/* 449 */       if ((valueFilter != null) && ((value = this.m_data.m_localData.getProperty(key)) != null) && (StringUtils.match(value, valueFilter, false)))
/*     */       {
/* 453 */         list.add(key);
/*     */       }
/*     */     }
/*     */ 
/* 457 */     return list;
/*     */   }
/*     */ 
/*     */   public String createStringRepresentation() throws DataException
/*     */   {
/* 462 */     if (this.m_timestampIndex >= 0)
/*     */     {
/* 464 */       this.m_data.putLocal(getTimestampField(), "" + this.m_timestamp);
/*     */     }
/* 466 */     IdcCharArrayWriter w = new IdcCharArrayWriter();
/*     */     try
/*     */     {
/* 469 */       this.m_data.send(w);
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */     finally
/*     */     {
/* 478 */       if (this.m_timestampIndex >= 0)
/*     */       {
/* 480 */         this.m_data.removeLocal(getTimestampField());
/*     */       }
/*     */     }
/* 483 */     return w.toStringRelease();
/*     */   }
/*     */ 
/*     */   public String toString()
/*     */   {
/* 489 */     return super.toString() + ":" + this.m_name;
/*     */   }
/*     */ 
/*     */   public void addResultSet(String name, DataResultSet drset)
/*     */   {
/* 494 */     this.m_data.addResultSet(name, drset);
/*     */   }
/*     */ 
/*     */   public DataResultSet getResultSet(String name)
/*     */   {
/* 499 */     DataResultSet drset = (DataResultSet)this.m_data.getResultSet(name);
/* 500 */     if (drset != null)
/*     */     {
/* 502 */       drset = drset.shallowClone();
/*     */     }
/* 504 */     return drset;
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 510 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 98846 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaData
 * JD-Core Version:    0.5.4
 */