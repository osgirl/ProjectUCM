/*     */ package intradoc.jdbc;
/*     */ 
/*     */ import intradoc.common.IdcDateFormat;
/*     */ import intradoc.common.NumberUtils;
/*     */ import intradoc.common.StringUtils;
/*     */ import intradoc.data.DataBinder;
/*     */ import intradoc.data.ResultSet;
/*     */ import intradoc.data.ResultSetUtils;
/*     */ import java.util.HashMap;
/*     */ import java.util.Vector;
/*     */ 
/*     */ public class JdbcConfigData
/*     */ {
/*     */   protected DataBinder m_providerData;
/*     */   protected HashMap m_configMaps;
/*     */   public String m_currentLabel;
/*     */   public IdcDateFormat m_dateFormat;
/*     */   protected HashMap m_upperCaseColumns;
/*     */ 
/*     */   public JdbcConfigData()
/*     */   {
/*  27 */     this.m_providerData = null;
/*     */ 
/*  29 */     this.m_currentLabel = "ORACLE";
/*     */ 
/*  31 */     this.m_dateFormat = null;
/*  32 */     this.m_upperCaseColumns = new HashMap();
/*     */   }
/*     */ 
/*     */   public void init(DataBinder data) {
/*  36 */     this.m_providerData = data;
/*  37 */     this.m_configMaps = new HashMap();
/*     */   }
/*     */ 
/*     */   public void initConfigurations()
/*     */   {
/*  42 */     ResultSet rset = this.m_providerData.getResultSet("DatabaseConnectionConfigurations");
/*  43 */     if (rset == null)
/*     */     {
/*  45 */       return;
/*     */     }
/*     */ 
/*  48 */     for (rset.first(); rset.isRowPresent(); rset.next())
/*     */     {
/*  50 */       String rawKey = ResultSetUtils.getValue(rset, "dccKey");
/*  51 */       String key = StringUtils.createSubstr(rawKey, ")", null, 0);
/*     */ 
/*  53 */       String label = StringUtils.createSubstr(rawKey, "(", ")", 12);
/*     */ 
/*  55 */       if (key == null) continue; if ((label != null) && (!label.equals(this.m_currentLabel)))
/*     */       {
/*     */         continue;
/*     */       }
/*     */ 
/*  60 */       String type = ResultSetUtils.getValue(rset, "dccType");
/*  61 */       String value = this.m_providerData.getAllowMissing(this.m_currentLabel + "." + key);
/*  62 */       if (value == null)
/*     */       {
/*  64 */         value = this.m_providerData.getAllowMissing(key);
/*     */       }
/*  66 */       if (value == null)
/*     */       {
/*  68 */         value = ResultSetUtils.getValue(rset, "dccValue");
/*     */       }
/*     */ 
/*  71 */       Object valueObj = value;
/*  72 */       if (type.equalsIgnoreCase("int"))
/*     */       {
/*  74 */         valueObj = Integer.valueOf(value);
/*     */       }
/*  76 */       else if (type.equalsIgnoreCase("bool"))
/*     */       {
/*  78 */         if (StringUtils.convertToBool(value, false))
/*     */         {
/*  80 */           valueObj = Boolean.TRUE;
/*     */         }
/*     */         else
/*     */         {
/*  84 */           valueObj = Boolean.FALSE;
/*     */         }
/*     */ 
/*     */       }
/*     */ 
/*  89 */       HashMap config = getConfigurations(label);
/*  90 */       config.put(key, valueObj);
/*     */     }
/*     */ 
/*  93 */     HashMap config = getConfigurations(null);
/*  94 */     config.put("ParsedUpperCaseColumns", this.m_upperCaseColumns);
/*     */   }
/*     */ 
/*     */   public void loadUpperCaseColumns(String str)
/*     */   {
/*  99 */     Vector v = StringUtils.parseArray(str, ',', ',');
/*     */ 
/* 101 */     int size = v.size();
/* 102 */     for (int i = 0; i < size; ++i)
/*     */     {
/* 104 */       String value = (String)v.elementAt(i);
/* 105 */       if (this.m_upperCaseColumns.get(value) != null)
/*     */         continue;
/* 107 */       this.m_upperCaseColumns.put(value.trim(), value.trim());
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean isValueUpperCaseNeeded(String column)
/*     */   {
/* 114 */     return this.m_upperCaseColumns.get(column) != null;
/*     */   }
/*     */ 
/*     */   public boolean getValueAsBool(String key, boolean defValue)
/*     */   {
/* 119 */     Object value = getValue(key);
/* 120 */     boolean result = defValue;
/* 121 */     if (value instanceof Boolean)
/*     */     {
/* 123 */       result = ((Boolean)value).booleanValue();
/*     */     }
/* 125 */     else if (value != null)
/*     */     {
/* 128 */       result = StringUtils.convertToBool("" + value, defValue);
/*     */     }
/* 130 */     return result;
/*     */   }
/*     */ 
/*     */   public String getValueAsString(String key)
/*     */   {
/* 135 */     Object value = getValue(key);
/* 136 */     String result = null;
/*     */ 
/* 138 */     if (value instanceof String)
/*     */     {
/* 140 */       result = (String)value;
/*     */     }
/* 142 */     else if (value != null)
/*     */     {
/* 144 */       result = "" + value;
/*     */     }
/*     */ 
/* 147 */     return result;
/*     */   }
/*     */ 
/*     */   public int getValueAsInt(String key, int defValue)
/*     */   {
/* 152 */     Object value = getValue(key);
/* 153 */     int result = defValue;
/*     */ 
/* 155 */     if (value instanceof Integer)
/*     */     {
/* 157 */       result = ((Integer)value).intValue();
/*     */     }
/* 159 */     else if (value instanceof String)
/*     */     {
/* 161 */       result = NumberUtils.parseInteger((String)value, defValue);
/*     */     }
/* 163 */     else if (value instanceof Boolean)
/*     */     {
/* 165 */       if (((Boolean)value).booleanValue())
/*     */       {
/* 167 */         result = 1;
/*     */       }
/*     */       else
/*     */       {
/* 171 */         result = 0;
/*     */       }
/*     */     }
/* 174 */     return result;
/*     */   }
/*     */ 
/*     */   public Object getValue(String key)
/*     */   {
/* 186 */     Object value = null;
/*     */ 
/* 188 */     HashMap map = (HashMap)this.m_configMaps.get(this.m_currentLabel);
/* 189 */     if (map != null)
/*     */     {
/* 191 */       value = map.get(key);
/*     */     }
/* 193 */     if (value == null)
/*     */     {
/* 195 */       map = (HashMap)this.m_configMaps.get("DEFAULT");
/* 196 */       if (map != null)
/*     */       {
/* 198 */         value = map.get(key);
/*     */       }
/*     */     }
/*     */ 
/* 202 */     if (value == null)
/*     */     {
/* 204 */       value = this.m_providerData.getAllowMissing(this.m_currentLabel + "." + key);
/*     */     }
/*     */ 
/* 207 */     if (value == null)
/*     */     {
/* 209 */       value = this.m_providerData.getAllowMissing(key);
/*     */     }
/* 211 */     return value;
/*     */   }
/*     */ 
/*     */   public void setConfigValue(String key, Object value)
/*     */   {
/* 216 */     HashMap map = getConfigurations(this.m_currentLabel);
/* 217 */     map.put(key, value);
/*     */   }
/*     */ 
/*     */   public HashMap getConfigurations(String label)
/*     */   {
/* 222 */     if (label == null)
/*     */     {
/* 224 */       label = "DEFAULT";
/*     */     }
/*     */ 
/* 227 */     HashMap map = (HashMap)this.m_configMaps.get(label);
/* 228 */     if (map == null)
/*     */     {
/* 230 */       map = new HashMap();
/* 231 */       this.m_configMaps.put(label, map);
/*     */     }
/* 233 */     return map;
/*     */   }
/*     */ 
/*     */   public void setDatabaseLabel(String label)
/*     */   {
/* 238 */     if (label == null)
/*     */       return;
/* 240 */     this.m_currentLabel = label.toUpperCase();
/*     */   }
/*     */ 
/*     */   public static Object idcVersionInfo(Object arg)
/*     */   {
/* 246 */     return "releaseInfo=7.3.5.185,relengDate=2013-07-11 17:07:21Z,releaseRevision=$Rev: 66660 $";
/*     */   }
/*     */ }

/* Location:           C:\Documents and Settings\rastogia.EMEA\My Documents\idcserver\
 * Qualified Name:     intradoc.jdbc.JdbcConfigData
 * JD-Core Version:    0.5.4
 */