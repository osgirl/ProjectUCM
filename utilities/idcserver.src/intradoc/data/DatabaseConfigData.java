/*     */ package intradoc.data;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class DatabaseConfigData
/*     */ {
/*     */   protected DataBinder m_providerData;
/*     */   protected HashMap m_configMaps;
/*     */   public String m_currentLabel;
/*     */   public IdcDateFormat m_dateFormat;
/*     */   protected HashMap m_upperCaseColumns;
/*     */ 
/*     */   public DatabaseConfigData()
/*     */   {
/*  26 */     this.m_providerData = null;
/*     */ 
/*  28 */     this.m_currentLabel = "ORACLE";
/*     */ 
/*  30 */     this.m_dateFormat = null;
/*  31 */     this.m_upperCaseColumns = new HashMap();
/*     */   }
/*     */ 
/*     */   public void init(DataBinder data) {
/*  35 */     this.m_providerData = data;
/*  36 */     this.m_configMaps = new HashMap();
/*     */   }
/*     */ 
/*     */   public void initConfigurations()
/*     */   {
/*  41 */     ResultSet rset = this.m_providerData.getResultSet("DatabaseConnectionConfigurations");
/*  42 */     if (rset == null)
/*     */     {
/*  44 */       return;
/*     */     }
/*     */ 
/*  47 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  49 */       String rawKey = ResultSetUtils.getValue(rset, "dccKey");
/*  50 */       String key = StringUtils.createSubstr(rawKey, ")", null, 0);
/*     */ 
/*  52 */       String label = StringUtils.createSubstr(rawKey, "(", ")", 12);
/*     */ 
/*  54 */       if (key == null) continue; if ((label != null) && (!label.equals(this.m_currentLabel)))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  59 */       String type = ResultSetUtils.getValue(rset, "dccType");
/*  60 */       String value = this.m_providerData.getAllowMissing(this.m_currentLabel + "." + key);
/*  61 */       if (value == null)
/*     */       {
/*  63 */         value = this.m_providerData.getAllowMissing(key);
/*     */       }
/*  65 */       if (value == null)
/*     */       {
/*  67 */         value = ResultSetUtils.getValue(rset, "dccValue");
/*     */       }
/*     */ 
/*  70 */       Object valueObj = value;
/*  71 */       if (type.equalsIgnoreCase("int"))
/*     */       {
/*  73 */         valueObj = Integer.valueOf(value);
/*     */       }
/*  75 */       else if (type.equalsIgnoreCase("bool"))
/*     */       {
/*  77 */         if (StringUtils.convertToBool(value, false))
/*     */         {
/*  79 */           valueObj = Boolean.TRUE;
/*     */         }
/*     */         else
/*     */         {
/*  83 */           valueObj = Boolean.FALSE;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  88 */       HashMap config = getConfigurations(label);
/*  89 */       config.put(key, valueObj);
/*     */     }
/*     */ 
/*  92 */     HashMap config = getConfigurations(null);
/*  93 */     config.put("ParsedUpperCaseColumns", this.m_upperCaseColumns);
/*     */   }
/*     */ 
/*     */   public void loadUpperCaseColumns(String str)
/*     */   {
/*  98 */     Vector v = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 100 */     int size = v.size();
/* 101 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 103 */       String value = (String)v.elementAt(i);
/* 104 */       if (this.m_upperCaseColumns.get(value) != null)
/*     */         continue;
/* 106 */       this.m_upperCaseColumns.put(value.trim(), value.trim());
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isValueUpperCaseNeeded(String column)
/*     */   {
/* 113 */     return this.m_upperCaseColumns.get(column) != null;
/*     */   }
/*     */ 
/*     */   public boolean getValueAsBool(String key, boolean defValue)
/*     */   {
/* 118 */     Object value = getValue(key);
/* 119 */     boolean result = defValue;
/* 120 */     if (value instanceof Boolean)
/*     */     {
/* 122 */       result = ((Boolean)value).booleanValue();
/*     */     }
/* 124 */     else if (value != null)
/*     */     {
/* 127 */       result = StringUtils.convertToBool("" + value, defValue);
/*     */     }
/* 129 */     return result;
/*     */   }
/*     */ 
/*     */   public String getValueAsString(String key)
/*     */   {
/* 134 */     Object value = getValue(key);
/* 135 */     String result = null;
/*     */ 
/* 137 */     if (value instanceof String)
/*     */     {
/* 139 */       result = (String)value;
/*     */     }
/* 141 */     else if (value != null)
/*     */     {
/* 143 */       result = "" + value;
/*     */     }
/*     */ 
/* 146 */     return result;
/*     */   }
/*     */ 
/*     */   public int getValueAsInt(String key, int defValue)
/*     */   {
/* 151 */     Object value = getValue(key);
/* 152 */     int result = defValue;
/*     */ 
/* 154 */     if (value instanceof Integer)
/*     */     {
/* 156 */       result = ((Integer)value).intValue();
/*     */     }
/* 158 */     else if (value instanceof String)
/*     */     {
/* 160 */       result = NumberUtils.parseInteger((String)value, defValue);
/*     */     }
/* 162 */     else if (value instanceof Boolean)
/*     */     {
/* 164 */       if (((Boolean)value).booleanValue())
/*     */       {
/* 166 */         result = 1;
/*     */       }
/*     */       else
/*     */       {
/* 170 */         result = 0;
/*     */       }
/*     */     }
/* 173 */     return result;
/*     */   }
/*     */ 
/*     */   public Object getValue(String key)
/*     */   {
/* 185 */     Object value = null;
/*     */ 
/* 187 */     HashMap map = (HashMap)this.m_configMaps.get(this.m_currentLabel);
/* 188 */     if (map != null)
/*     */     {
/* 190 */       value = map.get(key);
/*     */     }
/* 192 */     if (value == null)
/*     */     {
/* 194 */       map = (HashMap)this.m_configMaps.get("DEFAULT");
/* 195 */       if (map != null)
/*     */       {
/* 197 */         value = map.get(key);
/*     */       }
/*     */     }
/*     */ 
/* 201 */     if (value == null)
/*     */     {
/* 203 */       value = this.m_providerData.getAllowMissing(this.m_currentLabel + "." + key);
/*     */     }
/*     */ 
/* 206 */     if (value == null)
/*     */     {
/* 208 */       value = this.m_providerData.getAllowMissing(key);
/*     */     }
/* 210 */     return value;
/*     */   }
/*     */ 
/*     */   public void setConfigValue(String key, Object value)
/*     */   {
/* 215 */     HashMap map = getConfigurations(this.m_currentLabel);
/* 216 */     map.put(key, value);
/*     */   }
/*     */ 
/*     */   public HashMap getConfigMap()
/*     */   {
/* 221 */     return this.m_configMaps;
/*     */   }
/*     */ 
/*     */   public HashMap getConfigurations(String label)
/*     */   {
/* 226 */     if (label == null)
/*     */     {
/* 228 */       label = "DEFAULT";
/*     */     }
/*     */ 
/* 231 */     HashMap map = (HashMap)this.m_configMaps.get(label);
/* 232 */     if (map == null)
/*     */     {
/* 234 */       map = new HashMap();
/* 235 */       this.m_configMaps.put(label, map);
/*     */     }
/* 237 */     return map;
/*     */   }
/*     */ 
/*     */   public void setDatabaseLabel(String label)
/*     */   {
/* 242 */     if (label == null)
/*     */       return;
/* 244 */     this.m_currentLabel = label.toUpperCase();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 250 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 82175 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.data.DatabaseConfigData
 * JD-Core Version:    0.5.4
 */