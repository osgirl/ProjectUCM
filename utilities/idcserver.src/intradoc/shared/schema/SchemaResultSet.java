/*     */ package intradoc.shared.schema;
/*     */ 
/*     */ import intradoc.common.IdcStringBuilder;
/*     */ import intradoc.common.LocaleUtils;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.Report;
/*     */ import intradoc.common.ServiceException;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.common.SystemUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.DataException;
/*     */ import intradoc.data.DataResultSet;
/*     */ import intradoc.data.FieldInfo;
/*     */ import intradoc.shared.ComponentClassFactory;
/*     */ import java.io.CharConversionException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Hashtable;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class SchemaResultSet extends DataResultSet
/*     */ {
/*     */   public String[] m_columns;
/*     */   public FieldInfo[] m_infos;
/*     */   public int[] m_indexes;
/*     */   public String m_type;
/*     */   public int m_nameIndex;
/*     */   public int m_timestampIndex;
/*  43 */   protected Hashtable m_data = new Hashtable();
/*     */ 
/*     */   public SchemaResultSet(String dataClass, String[] columns)
/*     */   {
/*  47 */     super(columns);
/*     */ 
/*  49 */     this.m_type = dataClass;
/*  50 */     this.m_columns = columns;
/*     */ 
/*  52 */     this.m_infos = new FieldInfo[this.m_columns.length];
/*  53 */     this.m_indexes = new int[this.m_columns.length];
/*  54 */     for (int i = 0; i < this.m_columns.length; ++i)
/*     */     {
/*  56 */       this.m_infos[i] = new FieldInfo();
/*  57 */       getIndexFieldInfo(i, this.m_infos[i]);
/*  58 */       this.m_indexes[i] = this.m_infos[i].m_index;
/*     */     }
/*     */ 
/*     */     try
/*     */     {
/*  71 */       SchemaData tmpData = newData();
/*  72 */       String nameField = tmpData.getNameField();
/*  73 */       String timestampField = tmpData.getTimestampField();
/*  74 */       for (int i = 0; i < this.m_columns.length; ++i)
/*     */       {
/*  76 */         if ((nameField != null) && (this.m_columns[i].equals(nameField)))
/*     */         {
/*  79 */           this.m_nameIndex = i;
/*     */         }
/*     */         else {
/*  82 */           if ((timestampField == null) || (!this.m_columns[i].equals(timestampField))) {
/*     */             continue;
/*     */           }
/*  85 */           this.m_timestampIndex = i;
/*     */         }
/*     */       }
/*     */ 
/*     */     }
/*     */     catch (ServiceException ignore)
/*     */     {
/*  92 */       Report.trace("schema", "Unable to instantiate SchemaData object.", ignore);
/*     */     }
/*     */   }
/*     */ 
/*     */   public DataResultSet shallowClone()
/*     */   {
/* 100 */     SchemaResultSet set = new SchemaResultSet(this.m_type, this.m_columns);
/* 101 */     initShallow(set);
/* 102 */     return set;
/*     */   }
/*     */ 
/*     */   public void initShallow(DataResultSet drset)
/*     */   {
/* 108 */     SchemaResultSet set = (SchemaResultSet)drset;
/* 109 */     set.m_data = ((Hashtable)this.m_data.clone());
/*     */ 
/* 111 */     super.initShallow(set);
/*     */   }
/*     */ 
/*     */   public SchemaData newData() throws ServiceException
/*     */   {
/* 116 */     String msg = LocaleUtils.encodeMessage("csSchemaUnableToCreateDataObject", null, this.m_type);
/*     */ 
/* 118 */     SchemaData data = (SchemaData)ComponentClassFactory.createClassInstance(this.m_type, "intradoc.shared.schema." + this.m_type, msg);
/*     */ 
/* 121 */     data.init(this);
/* 122 */     return data;
/*     */   }
/*     */ 
/*     */   public void update(DataResultSet drset)
/*     */     throws DataException, ServiceException
/*     */   {
/* 130 */     int count = drset.getNumFields();
/* 131 */     FieldInfo[] drsetInfos = new FieldInfo[count];
/* 132 */     for (int i = 0; i < count; ++i)
/*     */     {
/* 134 */       drsetInfos[i] = new FieldInfo();
/* 135 */       drset.getIndexFieldInfo(i, drsetInfos[i]);
/*     */     }
/*     */ 
/* 138 */     for (drset.first(); drset.isRowPresent(); drset.next())
/*     */     {
/* 140 */       Vector row = drset.getCurrentRowValues();
/* 141 */       DataBinder binder = new DataBinder();
/* 142 */       for (int i = 0; i < count; ++i)
/*     */       {
/* 144 */         String value = (String)row.elementAt(i);
/* 145 */         binder.putLocal(drsetInfos[i].m_name, value);
/*     */       }
/*     */ 
/* 148 */       updateEx(binder, 0L, false);
/*     */     }
/*     */   }
/*     */ 
/*     */   public SchemaData update(DataBinder binder, long timestamp)
/*     */     throws DataException, ServiceException
/*     */   {
/* 155 */     return updateEx(binder, timestamp, true);
/*     */   }
/*     */ 
/*     */   public SchemaData updateEx(DataBinder binder, long timestamp, boolean markUpToDate)
/*     */     throws DataException, ServiceException
/*     */   {
/* 161 */     String name = binder.getLocal(this.m_columns[0]);
/* 162 */     if (name == null)
/*     */     {
/* 165 */       DataException e = new DataException(this.m_columns[0] + " was null when updating a view.");
/* 166 */       Report.trace("schema", null, e);
/* 167 */       throw e;
/*     */     }
/* 169 */     SchemaData data = getData(name);
/* 170 */     if (data == null)
/*     */     {
/* 172 */       data = newData();
/*     */     }
/* 174 */     data.update(binder);
/*     */ 
/* 183 */     String timestampColumn = data.getTimestampField();
/* 184 */     if ((markUpToDate) || (timestampColumn == null))
/*     */     {
/* 186 */       if ((timestamp == 0L) && (timestampColumn != null))
/*     */       {
/* 189 */         timestamp = NumberUtils.parseLong(binder.getLocal(timestampColumn), 0L);
/*     */       }
/*     */ 
/* 192 */       data.m_timestamp = timestamp;
/* 193 */       data.m_isUpToDate = true;
/* 194 */       if (SystemUtils.m_verbose)
/*     */       {
/* 196 */         Report.debug("schemastorage", this.m_type + " '" + name + "' is now up to date.", null);
/*     */       }
/*     */ 
/*     */     }
/*     */     else
/*     */     {
/* 202 */       long newTimestamp = NumberUtils.parseLong(binder.getLocal(timestampColumn), 0L);
/*     */ 
/* 204 */       if (data.m_timestamp != newTimestamp)
/*     */       {
/* 206 */         data.m_isUpToDate = false;
/* 207 */         if (SystemUtils.m_verbose)
/*     */         {
/* 209 */           Report.debug("schemastorage", this.m_type + " '" + name + "' is no longer up to date.", null);
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*     */     }
/*     */ 
/* 216 */     Vector row = findRow(1, data.m_canonicalName);
/* 217 */     boolean appendRow = false;
/* 218 */     if (row == null)
/*     */     {
/* 220 */       row = createEmptyRow();
/* 221 */       appendRow = true;
/*     */     }
/* 223 */     data.updateRow(row);
/* 224 */     if (appendRow)
/*     */     {
/* 226 */       addRow(row);
/*     */     }
/* 228 */     this.m_data.put(data.m_canonicalName, data);
/*     */ 
/* 230 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaData delete(String name)
/*     */   {
/* 235 */     SchemaData data = (SchemaData)this.m_data.remove(canonicalName(name));
/* 236 */     if (findRow(this.m_nameIndex, name) != null)
/*     */     {
/* 238 */       deleteCurrentRow();
/*     */     }
/*     */ 
/* 241 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaData getData()
/*     */   {
/* 246 */     SchemaData data = null;
/* 247 */     if (isRowPresent())
/*     */     {
/* 249 */       String name = getStringValue(this.m_infos[this.m_nameIndex].m_index);
/* 250 */       data = getData(name);
/* 251 */       if (data == null)
/*     */       {
/* 253 */         Report.trace("schema", "getData() missing object '" + name + "' for type '" + this.m_type + "'", null);
/*     */       }
/*     */     }
/*     */ 
/* 257 */     return data;
/*     */   }
/*     */ 
/*     */   public SchemaData getData(String name)
/*     */   {
/* 262 */     if (name == null)
/*     */     {
/* 264 */       return getData();
/*     */     }
/* 266 */     SchemaData data = (SchemaData)this.m_data.get(canonicalName(name));
/* 267 */     if (data == null)
/*     */     {
/*     */       try
/*     */       {
/* 271 */         String tmpName = StringUtils.decodeJavascriptFilename(name);
/* 272 */         data = (SchemaData)this.m_data.get(canonicalName(tmpName));
/*     */       }
/*     */       catch (CharConversionException e)
/*     */       {
/* 276 */         if (SystemUtils.m_verbose)
/*     */         {
/* 278 */           Report.debug("schema", null, e);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 283 */     if ((SystemUtils.m_verbose) && (data == null))
/*     */     {
/* 285 */       Report.debug("schema", "SchemaResultSet: got null when looking for " + canonicalName(name), null);
/*     */     }
/*     */ 
/* 289 */     return data;
/*     */   }
/*     */ 
/*     */   public String canonicalName(String name)
/*     */   {
/* 302 */     if ((name == null) || (name.length() == 0))
/*     */     {
/* 304 */       return "(nil)";
/*     */     }
/*     */ 
/* 307 */     name = name.toLowerCase();
/* 308 */     IdcStringBuilder buf = null;
/* 309 */     int size = name.length();
/* 310 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 312 */       char c = name.charAt(i);
/* 313 */       if (!Character.isJavaIdentifierPart(c))
/*     */       {
/* 315 */         if (buf == null)
/*     */         {
/* 317 */           buf = new IdcStringBuilder();
/* 318 */           buf.append(name.substring(0, i));
/*     */         }
/* 320 */         if (Character.isWhitespace(c))
/*     */         {
/* 322 */           buf.append("_");
/*     */         }
/*     */         else
/*     */         {
/* 326 */           buf.append("%");
/* 327 */           int value = c;
/* 328 */           if (value < 16384)
/*     */           {
/* 330 */             buf.append("0");
/*     */           }
/* 332 */           if (value < 256)
/*     */           {
/* 334 */             buf.append("0");
/*     */           }
/* 336 */           if (value < 16)
/*     */           {
/* 338 */             buf.append("0");
/*     */           }
/* 340 */           String hexString = Integer.toHexString(value);
/* 341 */           buf.append(hexString);
/*     */         }
/*     */       } else {
/* 344 */         if (buf == null)
/*     */           continue;
/* 346 */         buf.append(c);
/*     */       }
/*     */     }
/* 349 */     return (buf != null) ? buf.toString() : name;
/*     */   }
/*     */ 
/*     */   public List getValidResultSets()
/*     */   {
/* 354 */     return new ArrayList();
/*     */   }
/*     */ 
/*     */   public String getName()
/*     */   {
/* 360 */     int index = this.m_infos[this.m_nameIndex].m_index;
/* 361 */     return getStringValue(index);
/*     */   }
/*     */ 
/*     */   public String getTimestamp()
/*     */   {
/* 366 */     int index = this.m_infos[this.m_timestampIndex].m_index;
/* 367 */     return getStringValue(index);
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 372 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.shared.schema.SchemaResultSet
 * JD-Core Version:    0.5.4
 */